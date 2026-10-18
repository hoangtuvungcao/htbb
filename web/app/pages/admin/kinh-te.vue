<script setup lang="ts">
/**
 * Bảng kinh tế (chỉ đọc): nguồn vào / nơi tiêu, cung tiền từng loại, chỉ báo lạm phát, mức tập trung,
 * người giữ nhiều nhất, nguồn xu theo log, và cảnh báo (luật + ngưỡng: shared/utils/kinhTeCanhBao.ts).
 * Số liệu ngày do game ghi lúc 04:00 vào tob_kinh_te_ngay (bật sv.lau_dai).
 */
import type { CanhBaoKinhTe, ChiSoDanXuat, NgayKinhTe, NhanXuNhanVat, DotBien, NguonXu } from '#shared/utils/kinhTeCanhBao'

definePageMeta({ layout: 'admin', middleware: 'admin' })
const api = useAdminApi()

interface TongQuan {
  coBang: boolean
  ds: NgayKinhTe[]
  chiSo: ChiSoDanXuat
  canhBao: CanhBaoKinhTe[]
  luat: { ma: string, ten: string, luat: string }[]
  nhanXu: { co: boolean, soDong: number, catBot: boolean, top: NhanXuNhanVat[], dotBien: DotBien[] }
  gioiHanLog: number
}
interface NguoiGiu { id: number, ten: string, cap: number, tui: number, kho: number, tong: number, lanCuoi: string | null, khoa: boolean, daXoa: boolean }
interface Top { co: boolean, xu: NguoiGiu[], luong: NguoiGiu[], luong_khoa: NguoiGiu[], tongCong?: { xu: number, luong: number, luongKhoa: number, soNhanVat: number } }
interface Nguon {
  xu: { co: boolean, ngay: number, vao: NguonXu[], ra: NguonXu[], soDongVao: number, soDongRa: number, catBot: boolean }
  shop: { co: boolean, ngay: number, theoTien: { tien: string, soLan: number, tong: number }[], top: { info: string, tien: string, soLuong: number, tong: number }[] }
  tuan: { bang: string | null, tuan: string, dong: { ten: string, tien: string, soLuong: number, soLan: number, tong: number }[] }
  gioiHanLog: number
}

const soNgay = ref(90)
const cuaSo = ref(1)
const tienTop = ref<'xu' | 'luong' | 'luong_khoa'>('xu')

const tq = useAsyncData('admin-kinh-te-tq', () => api<TongQuan>('/api/admin/kinh-te/tong-quan', { query: { ngay: soNgay.value } }), { server: false, watch: [soNgay] })
const top = useAsyncData('admin-kinh-te-top', () => api<Top>('/api/admin/kinh-te/top', { query: { n: 20 } }), { server: false })
const nguon = useAsyncData('admin-kinh-te-nguon', () => api<Nguon>('/api/admin/kinh-te/nguon', { query: { ngay: cuaSo.value } }), { server: false, watch: [cuaSo] })

const ds = computed(() => tq.data.value?.ds ?? [])
const nhan = computed(() => ds.value.map(d => d.ngay.slice(5)))
const cuoi = computed(() => ds.value.at(-1))
const cs = computed(() => tq.data.value?.chiSo)

const MUC_GAME = [
  { t: 'Ổn định', mau: 'success', icon: 'i-lucide-circle-check' },
  { t: 'Cần theo dõi', mau: 'warning', icon: 'i-lucide-triangle-alert' },
  { t: 'Vượt ngưỡng', mau: 'error', icon: 'i-lucide-octagon-alert' },
] as const
const MUC_CB: Record<CanhBaoKinhTe['muc'], { t: string, mau: 'error' | 'warning' | 'info', icon: string }> = {
  nghiem_trong: { t: 'Nghiêm trọng', mau: 'error', icon: 'i-lucide-octagon-alert' },
  canh_bao: { t: 'Cảnh báo', mau: 'warning', icon: 'i-lucide-triangle-alert' },
  thong_tin: { t: 'Thông tin', mau: 'info', icon: 'i-lucide-info' },
}

/** Số lớn rút gọn cho thẻ (2,01 tỷ / 39,3 tr); số đầy đủ để ở ghi chú hoặc bảng. */
function gon(v: number | null | undefined): string {
  if (v == null || !Number.isFinite(v)) return '—'
  const a = Math.abs(v)
  if (a >= 1e9) return `${(v / 1e9).toLocaleString('vi-VN', { maximumFractionDigits: 2 })} tỷ`
  if (a >= 1e6) return `${(v / 1e6).toLocaleString('vi-VN', { maximumFractionDigits: 1 })} tr`
  return Math.round(v).toLocaleString('vi-VN')
}
const pt = (v: number | null | undefined) => (v == null ? '—' : `${v > 0 ? '+' : ''}${v.toLocaleString('vi-VN')}%`)
const ghiChuDoi = (a: number | null | undefined, b: number | null | undefined) => `7 ngày ${pt(a)} · 30 ngày ${pt(b)}`
const mucDoi = (v: number | null | undefined, nguong: number): '' | 'warn' => (v != null && v > nguong ? 'warn' : '')

const TEN_TIEN: Record<string, string> = { xu: 'Xu', luong: 'Lượng', luong_khoa: 'Lượng khoá' }
const dsTop = computed(() => top.data.value?.[tienTop.value] ?? [])
const cotTop = [
  { key: 'hang', label: '#', so: true, giaTri: (r: NguoiGiu & { hang: number }) => r.hang },
  { key: 'ten', label: 'Pokémon', sort: true },
  { key: 'cap', label: 'Cấp', so: true, sort: true, anMobile: true },
  { key: 'tui', label: 'Túi', so: true, sort: true, anMobile: true },
  { key: 'kho', label: 'Kho', so: true, sort: true, anMobile: true },
  { key: 'tong', label: 'Tổng', so: true, sort: true },
  { key: 'lanCuoi', label: 'Vào lần cuối', anMobile: true },
]
const dsTopHang = computed(() => dsTop.value.map((r, i) => ({ ...r, hang: i + 1 })))
const tongTien = computed(() => {
  const t = top.data.value?.tongCong
  if (!t) return 0
  return tienTop.value === 'xu' ? t.xu : tienTop.value === 'luong' ? t.luong : t.luongKhoa
})

const cotNguon = [
  { key: 'nguon', label: 'Nguồn (vị trí trong mã)', sort: true },
  { key: 'soLan', label: 'Số lần', so: true, sort: true },
  { key: 'tong', label: 'Tổng xu', so: true, sort: true },
]
const cotNhanXu = [
  { key: 'ten', label: 'Pokémon', sort: true },
  { key: 'nhan24', label: 'Nhận 24 giờ qua', so: true, sort: true },
  { key: 'nhan24Truoc', label: '24 giờ trước đó', so: true, sort: true, anMobile: true },
  { key: 'gio', label: 'Giờ thu nhập', so: true, sort: true, giaTri: (r: NhanXuNhanVat) => (cuoi.value?.chiSoGia ? Math.round((r.nhan24 / cuoi.value.chiSoGia) * 10) / 10 : null) },
]
const tenTienLog = (t: string) => ({ xu: 'Xu', luong: 'Lượng', luonglock: 'Lượng khoá', luong_khoa: 'Lượng khoá' } as Record<string, string>)[t] ?? (t || '—')

function taiLai() {
  tq.refresh()
  top.refresh()
  nguon.refresh()
}
const dangTai = computed(() => tq.status.value === 'pending' || top.status.value === 'pending' || nguon.status.value === 'pending')
</script>

<template>
  <AdminPage title="Kinh tế">
    <template #right>
      <USelect v-model="soNgay" :items="[{ label: '30 ngày', value: 30 }, { label: '90 ngày', value: 90 }, { label: '180 ngày', value: 180 }, { label: '1 năm', value: 365 }]" size="sm" class="w-28" aria-label="Khoảng thời gian" />
      <UButton icon="i-lucide-refresh-cw" color="neutral" variant="ghost" :loading="dangTai" aria-label="Tải lại" @click="taiLai" />
    </template>

    <div>
      <h1 class="text-2xl font-bold text-highlighted">Kinh tế máy chủ</h1>
      <p class="mt-1 text-sm text-muted">
        Số liệu ngày do game ghi lúc 04:00 (bảng <code>tob_kinh_te_ngay</code>, chỉ tính người hoạt động trong 7 ngày, xu = túi + kho).
        "Chỉ số giá" = số xu một giờ cày ở cấp trung vị; "cung tiền thực" = xu mỗi người tính bằng giờ thu nhập.
      </p>
    </div>

    <UAlert v-if="tq.error.value" color="error" variant="subtle" icon="i-lucide-circle-x" title="Không tải được số liệu kinh tế" :description="loiApi(tq.error.value)"
      :actions="[{ label: 'Thử lại', color: 'error', variant: 'outline', onClick: () => tq.refresh() }]" />
    <UAlert v-else-if="tq.data.value && !tq.data.value.coBang" color="neutral" variant="subtle" icon="i-lucide-info" title="Chưa có bảng tob_kinh_te_ngay"
      description="Bảng được tạo khi máy chủ chạy bản có chế độ dài hạn (sv.lau_dai=1). Phần người giữ nhiều và nguồn xu bên dưới vẫn đọc trực tiếp từ tob_char / log." />

    <!-- ============ Cảnh báo ============ -->
    <UCard>
      <template #header>
        <div class="flex flex-wrap items-center gap-2">
          <UIcon name="i-lucide-siren" class="size-5 text-muted" />
          <h2 class="font-semibold text-highlighted">Cảnh báo</h2>
          <UBadge v-if="tq.data.value" :color="tq.data.value.canhBao.some(c => c.muc === 'nghiem_trong') ? 'error' : tq.data.value.canhBao.some(c => c.muc === 'canh_bao') ? 'warning' : 'success'" variant="subtle">
            {{ tq.data.value.canhBao.filter(c => c.muc !== 'thong_tin').length }} mục cần xem
          </UBadge>
        </div>
      </template>
      <div v-if="!tq.data.value" class="space-y-2">
        <USkeleton v-for="i in 3" :key="i" class="h-10 w-full" />
      </div>
      <p v-else-if="!tq.data.value.canhBao.length" class="flex items-center gap-2 text-sm text-success">
        <UIcon name="i-lucide-circle-check" class="size-4" /> Không có cảnh báo nào theo các luật bên dưới.
      </p>
      <ul v-else class="space-y-2">
        <li v-for="(c, i) in tq.data.value.canhBao" :key="i" class="flex items-start gap-3 rounded-lg border border-default p-3">
          <UBadge :color="MUC_CB[c.muc].mau" variant="subtle" :icon="MUC_CB[c.muc].icon" class="shrink-0">{{ MUC_CB[c.muc].t }}</UBadge>
          <div class="min-w-0 text-sm">
            <p class="font-semibold text-highlighted">
              <NuxtLink v-if="c.ma === 'dot_bien'" :to="`/admin/nguoi-choi?q=${encodeURIComponent(c.tieuDe.split(':')[0] ?? '')}`" class="hover:underline">{{ c.tieuDe }}</NuxtLink>
              <template v-else>{{ c.tieuDe }}</template>
            </p>
            <p v-if="c.chiTiet" class="text-muted">{{ c.chiTiet }}</p>
          </div>
        </li>
      </ul>
      <details class="mt-4 text-sm">
        <summary class="cursor-pointer font-semibold text-primary">Luật và ngưỡng đang dùng</summary>
        <div class="mt-2 overflow-x-auto rounded-lg border border-default">
          <table class="bang-wiki">
            <thead><tr><th>Luật</th><th>Cách tính</th></tr></thead>
            <tbody>
              <tr v-for="l in tq.data.value?.luat ?? []" :key="l.ma"><td class="whitespace-nowrap font-medium">{{ l.ten }}</td><td>{{ l.luat }}</td></tr>
            </tbody>
          </table>
        </div>
        <p v-if="tq.data.value?.nhanXu.catBot" class="mt-2 text-xs text-warning">
          Log nhanxu 48 giờ vượt {{ tq.data.value.gioiHanLog.toLocaleString('vi-VN') }} dòng — chỉ xét {{ tq.data.value.gioiHanLog.toLocaleString('vi-VN') }} dòng mới nhất.
        </p>
      </details>
    </UCard>

    <!-- ============ Thẻ số ============ -->
    <div v-if="cuoi && cs" class="grid grid-cols-2 gap-3 md:grid-cols-3 2xl:grid-cols-6">
      <AdminStatCard label="Người hoạt động (7 ngày)" :value="cuoi.soNguoi" :note="`Cấp trung vị ${cuoi.capTrungVi} · ${cuoi.ngay}`" icon="i-lucide-users" />
      <AdminStatCard label="Tổng xu đang giữ" :value="gon(cuoi.tongXu)" :note="`${so(cuoi.tongXu)} · ${ghiChuDoi(cs.doiXu7, cs.doiXu30)}`" :mau="mucDoi(cs.doiXu7, 30)" icon="i-lucide-coins" />
      <AdminStatCard label="Xu mỗi người" :value="gon(cs.xuMoiNguoi)" :note="ghiChuDoi(cs.doiXuMoiNguoi7, cs.doiXuMoiNguoi30)" icon="i-lucide-user-round" />
      <AdminStatCard label="Chỉ số giá (xu / giờ)" :value="gon(cuoi.chiSoGia)" :note="ghiChuDoi(cs.doiGia7, cs.doiGia30)" icon="i-lucide-tag" />
      <AdminStatCard label="Cung tiền thực" :value="`${gon(cuoi.cungTienThuc)} giờ`" :note="`Mức game: ${MUC_GAME[cuoi.muc]?.t ?? cuoi.muc}`" :mau="cuoi.muc >= 2 ? 'bad' : cuoi.muc === 1 ? 'warn' : 'ok'" icon="i-lucide-gauge" />
      <AdminStatCard label="Top 1% giữ" :value="`${cuoi.top1.toLocaleString('vi-VN')}%`" note="% tổng xu" :mau="cuoi.top1 > 60 ? 'bad' : cuoi.top1 > 40 ? 'warn' : ''" icon="i-lucide-crown" />
    </div>
    <div v-else-if="tq.status.value === 'pending'" class="grid grid-cols-2 gap-3 md:grid-cols-3 2xl:grid-cols-6">
      <USkeleton v-for="i in 6" :key="i" class="h-24 rounded-xl" />
    </div>

    <!-- ============ Biểu đồ ============ -->
    <UCard v-if="ds.length">
      <template #header>
        <h2 class="font-semibold text-highlighted">Theo ngày ({{ ds.length }} dòng)</h2>
        <p v-if="ds.length < 2" class="text-xs text-muted">Mới có {{ ds.length }} ngày — đường xu hướng sẽ hiện khi có từ 2 ngày.</p>
      </template>
      <div class="grid gap-8 md:grid-cols-2">
        <AdminKinhteVaoRa tieu-de="Xu vào / ra mỗi ngày" :nhan="nhan" :vao="ds.map(d => d.xuVao)" :ra="ds.map(d => d.xuRa)" don-vi="xu" />
        <AdminBieuDoDuong tieu-de="Tổng xu đang giữ" :nhan="nhan" :chuoi="[{ ten: 'Xu', gt: ds.map(d => d.tongXu) }]" don-vi=" xu" />
        <AdminBieuDoDuong tieu-de="Tổng lượng đang giữ" :nhan="nhan" :chuoi="[{ ten: 'Lượng', gt: ds.map(d => d.tongLuong) }, { ten: 'Lượng khoá', gt: ds.map(d => d.tongLuongKhoa) }]" />
        <AdminBieuDoDuong tieu-de="Xu mỗi người hoạt động" :nhan="nhan" :chuoi="[{ ten: 'Xu / người', gt: ds.map(d => (d.soNguoi ? Math.round(d.tongXu / d.soNguoi) : 0)) }]" don-vi=" xu" />
        <AdminBieuDoDuong tieu-de="Chỉ số giá (xu / giờ ở cấp trung vị)" :nhan="nhan" :chuoi="[{ ten: 'Chỉ số giá', gt: ds.map(d => d.chiSoGia) }]" don-vi=" xu" />
        <AdminBieuDoDuong tieu-de="Cung tiền thực (giờ thu nhập / người)" :nhan="nhan" :chuoi="[{ ten: 'Giờ', gt: ds.map(d => d.cungTienThuc) }]" don-vi=" giờ" :nguong="{ gt: 20, nhan: 'đỏ > 20 giờ' }" />
        <AdminBieuDoDuong tieu-de="Top 1% nắm giữ (% tổng xu)" :nhan="nhan" :chuoi="[{ ten: 'Top 1%', gt: ds.map(d => d.top1) }]" don-vi="%" :nguong="{ gt: 40, nhan: 'ngưỡng 40%' }" />
        <AdminBieuDoDuong tieu-de="Người hoạt động (7 ngày)" :nhan="nhan" :chuoi="[{ ten: 'Người', gt: ds.map(d => d.soNguoi) }]" />
      </div>
      <details class="mt-6 text-sm">
        <summary class="cursor-pointer font-semibold text-primary">Xem bảng số liệu</summary>
        <div class="mt-2 max-h-96 overflow-auto rounded-lg border border-default">
          <table class="bang-wiki">
            <thead><tr><th>Ngày</th><th class="text-right">Người</th><th class="text-right">Cấp TV</th><th class="text-right">Chỉ số giá</th><th class="text-right">Tổng xu</th><th class="text-right">Lượng</th><th class="text-right">Lượng khoá</th><th class="text-right">Xu vào</th><th class="text-right">Xu ra</th><th class="text-right">Cung tiền (giờ)</th><th class="text-right">Top 1%</th><th>Mức</th></tr></thead>
            <tbody>
              <tr v-for="d in [...ds].reverse()" :key="d.ngay" class="tabular-nums">
                <td>{{ d.ngay }}</td><td class="text-right">{{ so(d.soNguoi) }}</td><td class="text-right">{{ d.capTrungVi }}</td><td class="text-right">{{ so(d.chiSoGia) }}</td>
                <td class="text-right">{{ so(d.tongXu) }}</td><td class="text-right">{{ so(d.tongLuong) }}</td><td class="text-right">{{ so(d.tongLuongKhoa) }}</td>
                <td class="text-right">{{ so(d.xuVao) }}</td><td class="text-right">{{ so(d.xuRa) }}</td><td class="text-right">{{ d.cungTienThuc.toLocaleString('vi-VN') }}</td>
                <td class="text-right">{{ d.top1.toLocaleString('vi-VN') }}%</td><td>{{ MUC_GAME[d.muc]?.t ?? d.muc }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </details>
    </UCard>

    <!-- ============ Người giữ nhiều ============ -->
    <UCard>
      <template #header>
        <div class="flex flex-wrap items-center gap-3">
          <h2 class="font-semibold text-highlighted">Người giữ nhiều nhất (bây giờ)</h2>
          <UTabs v-model="tienTop" :items="[{ label: 'Xu', value: 'xu' }, { label: 'Lượng', value: 'luong' }, { label: 'Lượng khoá', value: 'luong_khoa' }]" :content="false" size="sm" class="w-full sm:w-auto" />
        </div>
        <p class="mt-1 text-xs text-muted">
          Túi + kho, đọc trực tiếp <code>tob_char</code> (dữ liệu lưu lần cuối — người đang online có thể lệch vài phút).
          <template v-if="top.data.value?.tongCong">Toàn máy chủ: {{ so(tongTien) }} {{ TEN_TIEN[tienTop]?.toLowerCase() }} trên {{ so(top.data.value.tongCong.soNhanVat) }} Pokémon.</template>
        </p>
      </template>
      <AdminBang :rows="dsTopHang" :cot="cotTop" :dang-tai="top.status.value === 'pending'" :loi="top.error.value" :ten-csv="`top-${tienTop}`" tim-placeholder="Lọc tên Pokémon…" :moi-trang="20" rong-tieu-de="Không có ai giữ loại tiền này" @thu-lai="top.refresh()">
        <template #cell-ten="{ row }">
          <NuxtLink :to="`/admin/nguoi-choi?q=${encodeURIComponent(row.ten)}`" class="font-medium text-primary hover:underline">{{ row.ten }}</NuxtLink>
          <UBadge v-if="row.khoa" color="error" variant="subtle" size="sm" class="ml-1">Bị khoá</UBadge>
          <UBadge v-if="row.daXoa" color="neutral" variant="subtle" size="sm" class="ml-1">Đã xoá</UBadge>
        </template>
        <template #cell-tong="{ row }">
          <span class="font-semibold">{{ so(row.tong) }}</span>
          <span v-if="tongTien > 0" class="ml-1 text-xs text-dimmed">({{ ((row.tong / tongTien) * 100).toLocaleString('vi-VN', { maximumFractionDigits: 1 }) }}%)</span>
        </template>
      </AdminBang>
    </UCard>

    <!-- ============ Nguồn xu theo log ============ -->
    <UCard>
      <template #header>
        <div class="flex flex-wrap items-center gap-3">
          <h2 class="font-semibold text-highlighted">Xu đến từ đâu, đi về đâu</h2>
          <USelect v-model="cuaSo" :items="[{ label: '24 giờ', value: 1 }, { label: '3 ngày', value: 3 }, { label: '7 ngày', value: 7 }]" size="sm" class="w-28" aria-label="Cửa sổ log" />
        </div>
        <p class="mt-1 text-xs text-muted">
          Từ log <code>nhanxu</code> / <code>truxu</code> (Char.addXu / subXu, "tu vi tri …"), số trong tên nguồn gộp thành #.
          Mỗi chiều đọc tối đa {{ (nguon.data.value?.gioiHanLog ?? 100000).toLocaleString('vi-VN') }} dòng mới nhất; log tự xoá sau 15 ngày.
        </p>
      </template>
      <UAlert v-if="nguon.error.value" color="error" variant="subtle" icon="i-lucide-circle-x" :title="loiApi(nguon.error.value)" />
      <template v-else>
        <UAlert v-if="nguon.data.value?.xu.catBot" color="warning" variant="subtle" icon="i-lucide-triangle-alert" title="Log nhiều hơn giới hạn đọc — con số là của các dòng mới nhất, không phải toàn bộ cửa sổ." class="mb-4" />
        <div class="grid gap-6 lg:grid-cols-2">
          <div>
            <h3 class="mb-2 text-sm font-semibold text-highlighted">Nguồn vào ({{ so(nguon.data.value?.xu.soDongVao ?? 0) }} dòng log)</h3>
            <AdminBang :rows="nguon.data.value?.xu.vao" :cot="cotNguon" :dang-tai="nguon.status.value === 'pending'" khong-tim ten-csv="nguon-xu-vao" :moi-trang="10" rong-tieu-de="Không có log nhận xu trong cửa sổ này" />
          </div>
          <div>
            <h3 class="mb-2 text-sm font-semibold text-highlighted">Nơi tiêu ({{ so(nguon.data.value?.xu.soDongRa ?? 0) }} dòng log)</h3>
            <AdminBang :rows="nguon.data.value?.xu.ra" :cot="cotNguon" :dang-tai="nguon.status.value === 'pending'" khong-tim ten-csv="nguon-xu-ra" :moi-trang="10" rong-tieu-de="Không có log trừ xu trong cửa sổ này" />
          </div>
        </div>
        <div class="mt-6 grid gap-6 lg:grid-cols-2">
          <div>
            <h3 class="mb-2 text-sm font-semibold text-highlighted">Mua ở shop NPC theo loại tiền (<code>tob_log_buy_shop</code>)</h3>
            <AdminBang :rows="nguon.data.value?.shop.theoTien" :cot="[{ key: 'tien', label: 'Tiền', giaTri: (r: any) => tenTienLog(r.tien) }, { key: 'soLan', label: 'Số lần', so: true }, { key: 'tong', label: 'Tổng chi', so: true }]" :dang-tai="nguon.status.value === 'pending'" khong-tim :moi-trang="10" rong-tieu-de="Không có lượt mua shop trong cửa sổ này" />
          </div>
          <div>
            <h3 class="mb-2 text-sm font-semibold text-highlighted">Chi tiêu theo tuần<template v-if="nguon.data.value?.tuan.tuan"> — tuần {{ nguon.data.value.tuan.tuan }}</template></h3>
            <AdminBang :rows="nguon.data.value?.tuan.dong" :cot="[{ key: 'ten', label: 'Vật phẩm' }, { key: 'tien', label: 'Tiền', giaTri: (r: any) => tenTienLog(r.tien) }, { key: 'soLan', label: 'Lượt', so: true, anMobile: true }, { key: 'tong', label: 'Tổng', so: true }]" :dang-tai="nguon.status.value === 'pending'" khong-tim ten-csv="chi-tieu-tuan" :moi-trang="10" rong-tieu-de="Bảng tob_log_use_luong tuần này trống" />
          </div>
        </div>
      </template>
    </UCard>

    <!-- ============ Nhận xu nhiều nhất 24h ============ -->
    <UCard>
      <template #header>
        <h2 class="font-semibold text-highlighted">Nhận xu nhiều nhất 24 giờ qua</h2>
        <p class="mt-1 text-xs text-muted">So với 24 giờ trước đó; "giờ thu nhập" = số nhận chia chỉ số giá ngày mới nhất. Luật đột biến xem ở mục Cảnh báo.</p>
      </template>
      <AdminBang :rows="tq.data.value?.nhanXu.top" :cot="cotNhanXu" :dang-tai="tq.status.value === 'pending'" khong-tim ten-csv="nhan-xu-24h" :moi-trang="10"
        :rong-tieu-de="tq.data.value?.nhanXu.co === false ? 'Chưa có bảng tob_log_all_item' : 'Không ai nhận xu trong 48 giờ qua'">
        <template #cell-ten="{ row }">
          <NuxtLink :to="`/admin/nguoi-choi?q=${encodeURIComponent(row.ten)}`" class="font-medium text-primary hover:underline">{{ row.ten }}</NuxtLink>
          <UBadge v-if="tq.data.value?.nhanXu.dotBien.some(d => d.ten === row.ten)" color="warning" variant="subtle" size="sm" icon="i-lucide-triangle-alert" class="ml-1">Đột biến</UBadge>
        </template>
      </AdminBang>
    </UCard>
  </AdminPage>
</template>
