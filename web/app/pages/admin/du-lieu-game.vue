<script setup lang="ts">
/**
 * Tra cứu dữ liệu game (CHỈ ĐỌC): vật phẩm, quái, bản đồ, nhiệm vụ, sự kiện, cửa hàng, rơi đồ, gym, boss.
 * Danh sách trắng bộ dữ liệu + nhãn cột: server/utils/duLieuGameDanhMuc.ts. Mỗi bộ tải một lần
 * (tối đa 5 000 dòng), lọc / sắp xếp / phân trang / CSV ngay trên trình duyệt qua AdminBang.
 * URL ?bo=<mã> để gửi link đúng bảng.
 */
definePageMeta({ layout: 'admin', middleware: 'admin' })
const api = useAdminApi()
const route = useRoute()
const router = useRouter()

interface CotDuLieu { key: string, label: string, so?: boolean, ma?: Record<string, string>, anMobile?: boolean }
interface BoTQ { ma: string, tab: string, ten: string, moTa: string, bang: string | null, coBang: boolean, soDong: number }
interface TongQuan { tab: { ma: string, ten: string, icon: string }[], ds: BoTQ[], nhiemVuTheoLoai: Record<string, number>, toiDa: number }
interface KetQua { ma: string, ten: string, moTa: string, bang: string | null, coBang: boolean, tong: number, dong: Record<string, unknown>[], catBot: boolean, cot: CotDuLieu[] }

const tq = useAsyncData('admin-du-lieu-game-tq', () => api<TongQuan>('/api/admin/du-lieu-game/tong-quan'), { server: false })

const bo = computed(() => (typeof route.query.bo === 'string' ? route.query.bo : ''))
const boHienTai = computed(() => tq.data.value?.ds.find(b => b.ma === bo.value) ?? tq.data.value?.ds[0] ?? null)
const tab = computed({
  get: () => boHienTai.value?.tab ?? 'vat_pham',
  set: (t: string) => {
    const dau = tq.data.value?.ds.find(b => b.tab === t)
    if (dau) chon(dau.ma)
  },
})
function chon(ma: string) {
  router.replace({ query: { ...route.query, bo: ma } })
}
const boTrongTab = computed(() => (tq.data.value?.ds ?? []).filter(b => b.tab === tab.value))
const tabItems = computed(() => (tq.data.value?.tab ?? []).map(t => ({ label: t.ten, value: t.ma, icon: t.icon })))

const ct = useAsyncData(
  'admin-du-lieu-game-bang',
  () => (boHienTai.value ? api<KetQua>(`/api/admin/du-lieu-game/${encodeURIComponent(boHienTai.value.ma)}`) : Promise.resolve(null)),
  { server: false, watch: [() => boHienTai.value?.ma] },
)

const cot = computed(() => (ct.data.value?.cot ?? []).map(c => ({
  key: c.key,
  label: c.label,
  so: !!c.so && !c.ma,
  sort: true,
  anMobile: c.anMobile,
  giaTri: c.ma ? (r: Record<string, unknown>) => { const v = r[c.key]; const t = c.ma![String(v)]; return t ? `${t}` : v } : undefined,
})))
const TEN_LOAI_NV: Record<string, string> = {
  huong_dan: 'Hướng dẫn', chinh: 'Chính tuyến', phu: 'Phụ', ngay: 'Hằng ngày', tuan: 'Hằng tuần', pokedex: 'Pokédex', doi: 'Đổi',
  su_pham: 'Sư phạm', hang_bi_an: 'Hang bí ẩn', quay_lai: 'Quay lại', su_kien: 'Sự kiện', huyen_thoai: 'Huyền thoại', an: 'Ẩn', thanh_tuu: 'Thành tựu',
}
</script>

<template>
  <AdminPage title="Dữ liệu game">
    <template #right>
      <UButton icon="i-lucide-refresh-cw" color="neutral" variant="ghost" :loading="ct.status.value === 'pending'" aria-label="Tải lại" @click="tq.refresh(); ct.refresh()" />
    </template>

    <div>
      <h1 class="text-2xl font-bold text-highlighted">Dữ liệu game</h1>
      <p class="mt-1 text-sm text-muted">
        Chỉ đọc — số liệu máy chủ game đang dùng (các bảng <code>data_*</code> trong CSDL). Muốn sửa phải sửa CSV/SQL bên mã game rồi nạp lại.
        Đơn vị tiền: <b>xu</b> (gold), <b>lượng</b>, <b>lượng khoá</b> (không giao dịch được).
      </p>
    </div>

    <UAlert v-if="tq.error.value" color="error" variant="subtle" icon="i-lucide-circle-x" title="Không tải được danh mục dữ liệu" :description="loiApi(tq.error.value)"
      :actions="[{ label: 'Thử lại', color: 'error', variant: 'outline', onClick: () => tq.refresh() }]" />
    <div v-else-if="!tq.data.value" class="space-y-3">
      <USkeleton class="h-10 w-full" />
      <USkeleton class="h-64 w-full" />
    </div>

    <template v-else>
      <UTabs v-model="tab" :items="tabItems" :content="false" class="w-full overflow-x-auto" />

      <div class="flex flex-wrap gap-2">
        <UButton v-for="b in boTrongTab" :key="b.ma" size="sm" :color="boHienTai?.ma === b.ma ? 'primary' : 'neutral'" :variant="boHienTai?.ma === b.ma ? 'solid' : 'outline'" @click="chon(b.ma)">
          {{ b.ten }}
          <UBadge :color="b.coBang ? 'neutral' : 'warning'" variant="subtle" size="sm" class="tabular-nums">{{ b.coBang ? so(b.soDong) : 'thiếu bảng' }}</UBadge>
        </UButton>
      </div>

      <UCard v-if="boHienTai">
        <template #header>
          <div class="flex flex-wrap items-baseline gap-x-3 gap-y-1">
            <h2 class="font-semibold text-highlighted">{{ boHienTai.ten }}</h2>
            <code v-if="boHienTai.bang" class="text-xs text-muted">{{ boHienTai.bang }}</code>
          </div>
          <p class="mt-1 text-sm text-muted">{{ boHienTai.moTa }}</p>
        </template>

        <UAlert v-if="ct.data.value && !ct.data.value.coBang" color="warning" variant="subtle" icon="i-lucide-database-zap"
          :title="`CSDL chưa có bảng ${ct.data.value.bang}`" description="Bảng được tạo bởi sql/htbb.sql bên mã game (sudo bash /opt/htbb/vps/nang_cap_csdl.sh)." />
        <template v-else>
          <UAlert v-if="ct.data.value?.catBot" color="warning" variant="subtle" icon="i-lucide-scissors" class="mb-3"
            :title="`Bảng có ${so(ct.data.value.tong)} dòng — chỉ hiện ${so(ct.data.value.dong.length)} dòng đầu.`" />
          <AdminBang :rows="ct.data.value?.dong" :cot="cot" :dang-tai="ct.status.value === 'pending'" :loi="ct.error.value" :ten-csv="`du-lieu-${boHienTai.ma}`"
            tim-placeholder="Lọc mọi cột (không dấu cũng được)…" rong-tieu-de="Bảng trống" @thu-lai="ct.refresh()" />
        </template>

        <div v-if="tab === 'nhiem_vu' && Object.keys(tq.data.value.nhiemVuTheoLoai).length" class="mt-6">
          <h3 class="mb-2 text-sm font-semibold text-highlighted">Số nhiệm vụ theo loại (cẩm nang)</h3>
          <AdminHBar :data="Object.entries(tq.data.value.nhiemVuTheoLoai).sort((a, b) => b[1] - a[1]).map(([k, v]) => ({ nhan: TEN_LOAI_NV[k] ?? k, gt: v }))" />
        </div>
      </UCard>
    </template>
  </AdminPage>
</template>
