<script setup lang="ts">
/** Chỉ số kinh tế hằng đêm (chỉ admin): người hoạt động, cấp trung vị, cung tiền, xu vào/ra, mức cảnh báo. */
const api = useAdminApi()
const soNgay = ref(60)
interface Dong { ngay: string, soNguoi: number, capTrungVi: number, chiSoGia: number, tongXu: number, tongLuong: number, tongLuongKhoa: number, cungTienThuc: number, top1: number, xuVao: number, xuRa: number, muc: number }
const { data } = useAsyncData('admin-kinh-te', () => api<{ coBang: boolean, ds: Dong[] }>('/api/admin/kinh-te-ngay', { query: { ngay: soNgay.value } }), { server: false, watch: [soNgay] })
const ds = computed(() => data.value?.ds ?? [])
const nhan = computed(() => ds.value.map(d => d.ngay.slice(5)))
const cuoi = computed(() => ds.value.at(-1))
const MUC = [{ t: 'Ổn định', mau: 'success', icon: 'i-lucide-circle-check' }, { t: 'Cần theo dõi', mau: 'warning', icon: 'i-lucide-triangle-alert' }, { t: 'Vượt ngưỡng', mau: 'error', icon: 'i-lucide-octagon-alert' }] as const
</script>

<template>
  <UCard v-if="data?.coBang">
    <template #header>
      <div class="flex flex-wrap items-center gap-3">
        <b>Kinh tế máy chủ (ghi mỗi đêm 04:00)</b>
        <UBadge v-if="cuoi" :color="MUC[cuoi.muc]?.mau ?? 'neutral'" variant="subtle" :icon="MUC[cuoi.muc]?.icon">{{ MUC[cuoi.muc]?.t ?? '—' }} · {{ cuoi.ngay }}</UBadge>
        <UButton to="/admin/kinh-te" size="sm" color="neutral" variant="ghost" trailing-icon="i-lucide-arrow-right" class="ml-auto" label="Bảng kinh tế đầy đủ" />
        <USelect v-model="soNgay" :items="[{ label: '30 ngày', value: 30 }, { label: '60 ngày', value: 60 }, { label: '180 ngày', value: 180 }, { label: '1 năm', value: 365 }]" size="sm" class="w-32" />
      </div>
    </template>
    <p v-if="!ds.length" class="text-sm text-muted">Chưa có dữ liệu — game ghi dòng đầu tiên vào 04:00 sau khi bật chế độ dài hạn.</p>
    <template v-else>
      <div class="grid gap-6 md:grid-cols-2 xl:grid-cols-3">
        <AdminBieuDoDuong tieu-de="Người hoạt động (7 ngày)" :nhan="nhan" :chuoi="[{ ten: 'Người', gt: ds.map(d => d.soNguoi) }]" />
        <AdminBieuDoDuong tieu-de="Cấp trung vị" :nhan="nhan" :chuoi="[{ ten: 'Cấp', gt: ds.map(d => d.capTrungVi) }]" />
        <AdminBieuDoDuong tieu-de="Xu vào / ra trong ngày" :nhan="nhan" :chuoi="[{ ten: 'Vào', gt: ds.map(d => d.xuVao) }, { ten: 'Ra', gt: ds.map(d => d.xuRa) }]" />
        <AdminBieuDoDuong tieu-de="Tổng xu đang giữ" :nhan="nhan" :chuoi="[{ ten: 'Xu', gt: ds.map(d => d.tongXu) }]" />
        <AdminBieuDoDuong tieu-de="Cung tiền thực (giờ thu nhập / người)" :nhan="nhan" :chuoi="[{ ten: 'Giờ', gt: ds.map(d => d.cungTienThuc) }]" don-vi=" giờ" />
        <AdminBieuDoDuong tieu-de="Top 1% nắm giữ (% tổng xu)" :nhan="nhan" :chuoi="[{ ten: '%', gt: ds.map(d => d.top1) }]" don-vi="%" />
      </div>
      <details class="mt-4 text-sm">
        <summary class="cursor-pointer font-semibold text-primary">Xem bảng số liệu</summary>
        <div class="mt-2 max-h-80 overflow-auto">
          <table class="bang-wiki">
            <thead><tr><th>Ngày</th><th class="so">Người</th><th class="so">Cấp TV</th><th class="so">Tổng xu</th><th class="so">Lượng</th><th class="so">Lượng khoá</th><th class="so">Xu vào</th><th class="so">Xu ra</th><th class="so">Cung tiền (giờ)</th><th class="so">Top 1%</th><th>Mức</th></tr></thead>
            <tbody>
              <tr v-for="d in [...ds].reverse()" :key="d.ngay">
                <td>{{ d.ngay }}</td><td class="so">{{ d.soNguoi.toLocaleString('vi-VN') }}</td><td class="so">{{ d.capTrungVi }}</td><td class="so">{{ d.tongXu.toLocaleString('vi-VN') }}</td>
                <td class="so">{{ d.tongLuong.toLocaleString('vi-VN') }}</td><td class="so">{{ d.tongLuongKhoa.toLocaleString('vi-VN') }}</td><td class="so">{{ d.xuVao.toLocaleString('vi-VN') }}</td>
                <td class="so">{{ d.xuRa.toLocaleString('vi-VN') }}</td><td class="so">{{ d.cungTienThuc.toLocaleString('vi-VN') }}</td><td class="so">{{ d.top1.toLocaleString('vi-VN') }}%</td><td>{{ MUC[d.muc]?.t }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </details>
    </template>
  </UCard>
</template>
