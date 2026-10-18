<script setup lang="ts">
/**
 * Máy chủ & bảo trì: biểu đồ chỉ số máy chủ game (GET /api/admin/chi-so-may-chu — game ghi mỗi 30 giây
 * vào tob_chi_so_may_chu, giữ 7 ngày) + chế độ bảo trì đăng nhập (AdminHethongBaoTri).
 * Tự tải lại mỗi 30 giây. "Im lặng > 90 giây" = cùng ngưỡng với HTBB/vps/kiem-suc-khoe.sh.
 */
definePageMeta({ layout: 'admin', middleware: 'admin' })
const api = useAdminApi()

interface Diem {
  luc: string, ketNoi: number, nhanVat: number, heapMb: number, heapToiDaMb: number, thread: number, cpu: number, taiMay: number
  goiVaoGiay: number, goiBoGiay: number, catNgap: number, loiXuLy: number, baoTri: boolean
}
interface ChiSo { bangChuaCo: boolean, gio: number, bucketGiay: number, songGanNhat: number | null, diem: Diem[] }

const gio = ref(6)
const { data, status, error, refresh } = useAsyncData('admin-he-thong-chi-so', () => api<ChiSo>('/api/admin/chi-so-may-chu', { query: { gio: gio.value } }), { server: false, watch: [gio] })
const baoTri = ref<{ refresh: () => void } | null>(null)
useIntervalFn(() => { if (document.visibilityState === 'visible') refresh() }, 30_000)

const diem = computed(() => data.value?.diem ?? [])
function nhanThoiGian(s: string) {
  const d = new Date(String(s).includes('T') ? s : String(s).replace(' ', 'T'))
  if (Number.isNaN(d.getTime())) return String(s).slice(5, 16)
  const hh = `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
  return gio.value > 24 ? `${String(d.getDate()).padStart(2, '0')}/${String(d.getMonth() + 1).padStart(2, '0')} ${hh}` : hh
}
const nhan = computed(() => diem.value.map(d => nhanThoiGian(d.luc)))
const cuoi = computed(() => diem.value.at(-1))
const im = computed(() => data.value?.songGanNhat ?? null)
const imLang = computed(() => im.value === null || im.value > 90)
const heapPt = computed(() => (cuoi.value && cuoi.value.heapToiDaMb > 0 ? Math.round((cuoi.value.heapMb / cuoi.value.heapToiDaMb) * 100) : null))
const tong = (k: 'catNgap' | 'loiXuLy') => diem.value.reduce((t, d) => t + (d[k] || 0), 0)
const choDoc = (g: number) => (g < 60 ? `${g} giây` : g < 3600 ? `${Math.round(g / 60)} phút` : g < 86400 ? `${Math.round(g / 3600)} giờ` : `${Math.round(g / 86400)} ngày`)
</script>

<template>
  <AdminPage title="Máy chủ & bảo trì">
    <template #right>
      <USelect v-model="gio" :items="[{ label: '1 giờ', value: 1 }, { label: '6 giờ', value: 6 }, { label: '24 giờ', value: 24 }, { label: '7 ngày', value: 168 }]" size="sm" class="w-28" aria-label="Khoảng thời gian" />
      <UButton icon="i-lucide-refresh-cw" color="neutral" variant="ghost" :loading="status === 'pending'" aria-label="Tải lại" @click="refresh(); baoTri?.refresh()" />
    </template>

    <div>
      <h1 class="text-2xl font-bold text-highlighted">Máy chủ & bảo trì</h1>
      <p class="mt-1 text-sm text-muted">Game tự ghi một mẫu mỗi 30 giây (giữ 7 ngày). Trang tự tải lại mỗi 30 giây. Bật/tắt/khởi động lại máy chủ ở trang <NuxtLink to="/admin/server" class="text-primary hover:underline">Máy chủ game</NuxtLink>.</p>
    </div>

    <UAlert v-if="error" color="error" variant="subtle" icon="i-lucide-circle-x" title="Không tải được chỉ số máy chủ" :description="loiApi(error)"
      :actions="[{ label: 'Thử lại', color: 'error', variant: 'outline', onClick: () => refresh() }]" />
    <UAlert v-else-if="data?.bangChuaCo" color="neutral" variant="subtle" icon="i-lucide-info" title="Chưa có bảng tob_chi_so_may_chu"
      description="Bảng do máy chủ game tạo khi chạy bản có ChiSoMayChu (tắt bằng sv.chiSoMayChu=0 trong server.ini)." />
    <UAlert v-else-if="data && imLang" color="error" variant="subtle" icon="i-lucide-heart-crack"
      :title="im === null ? 'Máy chủ chưa ghi mẫu nào' : `Máy chủ im lặng ${choDoc(im)} (> 90 giây)`"
      description="Máy chủ game đã tắt, bị treo, hoặc mất kết nối CSDL. Cùng ngưỡng với vps/kiem-suc-khoe.sh (tự khởi động lại trên VPS)." />

    <div v-if="cuoi" class="grid grid-cols-2 gap-3 md:grid-cols-3 2xl:grid-cols-6">
      <AdminStatCard label="Pokémon online" :value="cuoi.nhanVat" :note="`${so(cuoi.ketNoi)} kết nối`" icon="i-lucide-users" />
      <AdminStatCard label="Bộ nhớ (heap)" :value="`${so(cuoi.heapMb)} MB`" :note="`/ ${so(cuoi.heapToiDaMb)} MB${heapPt !== null ? ` · ${heapPt}%` : ''}`" :mau="heapPt !== null && heapPt > 85 ? 'warn' : ''" icon="i-lucide-memory-stick" />
      <AdminStatCard label="CPU tiến trình" :value="`${cuoi.cpu}%`" :note="`Tải máy ${cuoi.taiMay.toLocaleString('vi-VN')}`" :mau="cuoi.cpu > 85 ? 'warn' : ''" icon="i-lucide-cpu" />
      <AdminStatCard label="Gói tin / giây" :value="cuoi.goiVaoGiay" :note="`${so(cuoi.thread)} thread`" icon="i-lucide-activity" />
      <AdminStatCard label="Cắt vì ngập gói" :value="tong('catNgap')" :note="`trong ${gio === 168 ? '7 ngày' : `${gio} giờ`}`" :mau="tong('catNgap') > 0 ? 'warn' : ''" icon="i-lucide-shield-alert" />
      <AdminStatCard label="Lỗi xử lý gói" :value="tong('loiXuLy')" :note="`trong ${gio === 168 ? '7 ngày' : `${gio} giờ`}`" :mau="tong('loiXuLy') > 0 ? 'warn' : ''" icon="i-lucide-bug" />
    </div>
    <div v-else-if="status === 'pending'" class="grid grid-cols-2 gap-3 md:grid-cols-3 2xl:grid-cols-6">
      <USkeleton v-for="i in 6" :key="i" class="h-24 rounded-xl" />
    </div>

    <UCard v-if="data && !data.bangChuaCo">
      <template #header>
        <h2 class="font-semibold text-highlighted">Biểu đồ ({{ so(diem.length) }} điểm, mỗi điểm gộp {{ choDoc(data.bucketGiay) }})</h2>
        <p class="text-xs text-muted">Online, heap: giá trị lớn nhất trong khoảng gộp. CPU, tải: trung bình. Gói/giây: tổng chia thời gian. Cắt ngập, lỗi: tổng số lần trong khoảng gộp.</p>
      </template>
      <div class="grid gap-8 md:grid-cols-2">
        <AdminBieuDoDuong tieu-de="Online" :nhan="nhan" :chuoi="[{ ten: 'Pokémon', gt: diem.map(d => d.nhanVat) }, { ten: 'Kết nối', gt: diem.map(d => d.ketNoi) }]" />
        <AdminBieuDoDuong tieu-de="Bộ nhớ heap (MB)" :nhan="nhan" :chuoi="[{ ten: 'Đang dùng', gt: diem.map(d => d.heapMb) }, { ten: 'Tối đa', gt: diem.map(d => d.heapToiDaMb) }]" don-vi=" MB" />
        <AdminBieuDoDuong tieu-de="CPU tiến trình (%)" :nhan="nhan" :chuoi="[{ ten: 'CPU', gt: diem.map(d => d.cpu) }]" don-vi="%" :nguong="{ gt: 85, nhan: '85%' }" />
        <AdminBieuDoDuong tieu-de="Tải máy (load average)" :nhan="nhan" :chuoi="[{ ten: 'Tải', gt: diem.map(d => d.taiMay) }]" />
        <AdminBieuDoDuong tieu-de="Gói tin vào / giây" :nhan="nhan" :chuoi="[{ ten: 'Gói/giây', gt: diem.map(d => d.goiVaoGiay) }]" />
        <AdminBieuDoDuong tieu-de="Gói bị bỏ vì ngập / giây" :nhan="nhan" :chuoi="[{ ten: 'Bỏ/giây', gt: diem.map(d => d.goiBoGiay) }]" />
        <AdminBieuDoDuong tieu-de="Kết nối bị cắt vì ngập" :nhan="nhan" :chuoi="[{ ten: 'Số lần', gt: diem.map(d => d.catNgap) }]" />
        <AdminBieuDoDuong tieu-de="Lỗi xử lý gói" :nhan="nhan" :chuoi="[{ ten: 'Số lỗi', gt: diem.map(d => d.loiXuLy) }]" />
      </div>
      <details class="mt-6 text-sm">
        <summary class="cursor-pointer font-semibold text-primary">Xem bảng số liệu</summary>
        <div class="mt-2">
          <AdminBang :rows="[...diem].reverse()" khong-tim ten-csv="chi-so-may-chu" :moi-trang="20" :cot="[
            { key: 'luc', label: 'Lúc', giaTri: (r: Diem) => nhanThoiGian(r.luc) },
            { key: 'nhanVat', label: 'Online', so: true }, { key: 'ketNoi', label: 'Kết nối', so: true, anMobile: true },
            { key: 'heapMb', label: 'Heap MB', so: true }, { key: 'cpu', label: 'CPU %', so: true }, { key: 'taiMay', label: 'Tải', so: true, anMobile: true },
            { key: 'goiVaoGiay', label: 'Gói/s', so: true, anMobile: true }, { key: 'goiBoGiay', label: 'Bỏ/s', so: true, anMobile: true },
            { key: 'catNgap', label: 'Cắt ngập', so: true, anMobile: true }, { key: 'loiXuLy', label: 'Lỗi', so: true, anMobile: true },
            { key: 'baoTri', label: 'Bảo trì', giaTri: (r: Diem) => (r.baoTri ? 'Có' : '') },
          ]" />
        </div>
      </details>
    </UCard>

    <AdminHethongBaoTri ref="baoTri" />
  </AdminPage>
</template>
