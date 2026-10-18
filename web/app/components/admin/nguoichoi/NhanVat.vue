<script setup lang="ts">
/**
 * Một nhân vật trong hồ sơ người chơi: tổng quan + thao tác (cấm chat, gửi thư, về điểm an toàn, tặng quà),
 * túi đồ, Pokédex, nhiệm vụ, hồ sơ lâu dài & hệ sinh thái, PvP / Hầm Ngục / sư phạm.
 * Phần tóm tắt (prop `nv`) đến từ chi-tiet.get.ts; phần sâu tải riêng từ nhan-vat.get.ts.
 */
import type { CotBang } from '~/components/admin/Bang.vue'
import type { VatPhamGiaoDien } from '~/components/admin/nhanvat/BangVatPham.vue'
import type { DongLenhGiaoDien } from '~/components/admin/nhanvat/BangLenh.vue'
import type { NvHoSo } from './HoSo.vue'
import AdminTaikhoanXacNhan from '~/components/admin/taikhoan/XacNhan.vue'
import HoiLyDo from './HoiLyDo.vue'
import TangQua from './TangQua.vue'

const props = defineProps<{ nv: NvHoSo, taiKhoan: string, chuLaQuanTri: boolean }>()
const emit = defineEmits<{ doi: [] }>()

interface ChiTietSau {
  vatPham: { mac: VatPhamGiaoDien[], thoiTrang: VatPhamGiaoDien[], tui: VatPhamGiaoDien[], ruong: VatPhamGiaoDien[] }
  pokedex: { daHa: number, soShiny: number, tongLoai: number, moc: { moc: number, thuong: string, daNhan: boolean }[], ds: { dex: number, ten: string, soHa: number, lanDau: string, shiny: number }[] } | null
  nhiemVu: {
    cu: { id: number, ten: string, trangThai: string, chinh: boolean }[]
    coBangLauDai: boolean
    lauDai: { ma: string, ten: string, loai: string, tienDo: number, canDat: number, trangThai: number, tuan: number }[]
    tongLauDai: number
    demTheoLoai: Record<string, { xong: number, dangLam: number }>
  }
  hoSoLauDai: (Record<string, any> & { capNhat: string, tho: string }) | null
  sinhThai: (Record<string, any> & { capNhat: string }) | null
  pvp: { muaHienTai: number | null, hienTai: { mua: number, elo: number, thang: number, thua: number } | null, ds: { mua: number, elo: number, thang: number, thua: number, bac: string }[] } | null
  hamNguc: { tangCao: number, ngayCao: number, ds: { ngay: number, tang: number, ghi_luc: string | null }[] } | null
  suPham: { thay: { id: number, ten: string, batDau: string, moc: string } | null, tro: { id: number, ten: string, batDau: string, moc: string }[] } | null
  lenh: DongLenhGiaoDien[]
}

const api = useAdminApi()
const toast = useToast()
const me = useAdminMe()
const hoiLyDo = useOverlay().create(HoiLyDo)
const xacNhan = useOverlay().create(AdminTaikhoanXacNhan)
const tangQuaHop = useOverlay().create(TangQua)

const { data: sau, status, error, refresh } = useAsyncData(
  () => `admin-nc-nv-${props.nv.id}`,
  () => api<ChiTietSau>('/api/admin/nguoi-choi/nhan-vat', { query: { id: props.nv.id } }),
  { server: false, watch: [() => props.nv.id] },
)

const TAB = [
  { label: 'Tổng quan', value: 'tong-quan' },
  { label: 'Túi đồ', value: 'tui-do' },
  { label: 'Pokédex', value: 'pokedex' },
  { label: 'Nhiệm vụ', value: 'nhiem-vu' },
  { label: 'Sinh thái & lâu dài', value: 'sinh-thai' },
  { label: 'PvP · Hầm Ngục · Sư phạm', value: 'pvp' },
]
const tab = ref('tong-quan')

// ---- quyền + điều kiện offline
const duocCamChat = computed(() => me.duoc('/api/admin/nguoi-choi/cam-chat'))
const duocThu = computed(() => me.duoc('/api/admin/nguoi-choi/gui-thu'))
const duocVeAnToan = computed(() => me.duoc('/api/admin/nguoi-choi/ve-diem-an-toan'))
const duocTangQua = computed(() => me.duoc('/api/admin/nguoi-choi/tang-qua') && !props.chuLaQuanTri)
const offline = computed(() => props.nv.online === false)
const lyDoKhongOffline = computed(() => (props.nv.online === true ? 'Pokémon đang online — game sẽ ghi đè thay đổi. Đợi người chơi thoát.' : `Chưa chắc Pokémon offline: ${props.nv.onlineLyDo}`))

const dangLam = ref('')
async function goi(viec: string, url: string, body: Record<string, unknown>) {
  dangLam.value = viec
  try {
    const r = await api<{ message?: string }>(url, { method: 'POST', body })
    toast.add({ title: r.message ?? 'Đã xong', color: 'success', icon: 'i-lucide-check' })
    emit('doi')
    return true
  }
  catch (e) {
    toast.add({ title: 'Không thực hiện được', description: loiApi(e), color: 'error', icon: 'i-lucide-circle-x' })
    return false
  }
  finally { dangLam.value = '' }
}

const HAN_CHAT = [{ label: '1 giờ', value: 1 }, { label: '6 giờ', value: 6 }, { label: '24 giờ', value: 24 }, { label: '3 ngày', value: 72 }, { label: '7 ngày', value: 168 }, { label: '30 ngày', value: 720 }]
async function camChat() {
  const kq = await hoiLyDo.open({
    title: `Cấm chat ${props.nv.charname}?`, description: 'Ghi vào trường cấm chat của Pokémon (cùng trường lệnh GM "camchat" dùng). Có hiệu lực khi người chơi vào game.',
    nut: 'Cấm chat', mau: 'warning', han: HAN_CHAT, hanMacDinh: 24, goiY: ['Spam kênh thế giới', 'Ngôn từ xúc phạm', 'Quảng cáo / lừa đảo'],
  }).result
  if (kq) await goi('cam_chat', '/api/admin/nguoi-choi/cam-chat', { charId: props.nv.id, soGio: kq.soGio, lyDo: kq.lyDo })
}
async function boCamChat() {
  const kq = await hoiLyDo.open({ title: `Bỏ cấm chat ${props.nv.charname}?`, nut: 'Bỏ cấm', batBuoc: false, nhanLyDo: 'Ghi chú' }).result
  if (kq) await goi('bo_cam', '/api/admin/nguoi-choi/cam-chat', { charId: props.nv.id, soGio: 0, lyDo: kq.lyDo })
}
async function veAnToan() {
  const kq = await hoiLyDo.open({
    title: `Đưa ${props.nv.charname} về điểm an toàn?`,
    description: 'Đặt lại vị trí đã lưu. Lần vào game kế tiếp, chính máy chủ game đưa Pokémon về thị trấn khởi đầu của vùng (một ô hồi sinh hợp lệ). Dùng khi người chơi kẹt trong tường / bản đồ lỗi.',
    nut: 'Đưa về', mau: 'warning', goiY: ['Kẹt địa hình', 'Bản đồ lỗi không vào được', 'Người chơi yêu cầu hỗ trợ'],
  }).result
  if (kq) await goi('ve_an_toan', '/api/admin/nguoi-choi/ve-diem-an-toan', { charId: props.nv.id, lyDo: kq.lyDo })
}
const thu = ref('')
async function guiThu() {
  const ok = await xacNhan.open({ title: `Gửi thư cho ${props.nv.charname}?`, description: thu.value, nut: 'Gửi thư' }).result
  if (ok && await goi('thu', '/api/admin/nguoi-choi/gui-thu', { charId: props.nv.id, noiDung: thu.value })) thu.value = ''
}
async function tangQua() {
  const kq = await tangQuaHop.open({ charId: props.nv.id, charname: props.nv.charname, cap: props.nv.lastLv }).result
  if (kq) {
    toast.add({ title: 'Đã tặng quà', description: `${kq.message} Mã: ${kq.ma}`, color: 'success', icon: 'i-lucide-gift', duration: 15000 })
    emit('doi')
  }
}

// ---- bảng
const cotDex: CotBang[] = [
  { key: 'dex', label: 'Số', sort: true, so: true },
  { key: 'ten', label: 'Loài', sort: true },
  { key: 'soHa', label: 'Đã hạ', sort: true, so: true },
  { key: 'shiny', label: 'Shiny', sort: true, so: true },
  { key: 'lanDau', label: 'Lần đầu', sort: true, anMobile: true },
]
const cotNv: CotBang[] = [
  { key: 'ten', label: 'Nhiệm vụ', sort: true },
  { key: 'loai', label: 'Loại', sort: true },
  { key: 'tienDo', label: 'Tiến độ', giaTri: r => `${r.tienDo}/${r.canDat || '?'}` },
  { key: 'trangThai', label: 'Trạng thái', sort: true, so: true, giaTri: r => r.trangThai },
]
const cotPvp: CotBang[] = [
  { key: 'mua', label: 'Mùa', sort: true, so: true }, { key: 'elo', label: 'Elo', sort: true, so: true },
  { key: 'bac', label: 'Bậc' }, { key: 'thang', label: 'Thắng', so: true }, { key: 'thua', label: 'Thua', so: true },
]
const tenTt = (t: number) => (t === 2 ? 'Đã nhận thưởng' : t === 1 ? 'Xong, chờ nhận' : 'Đang làm')
const mauTt = (t: number) => (t === 2 ? 'success' as const : t === 1 ? 'warning' as const : 'neutral' as const)
const gio = (giay: number) => `${so(Math.floor(giay / 3600))} giờ`
const ngay8 = (n: number) => (n > 0 ? String(n).replace(/^(\d{4})(\d{2})(\d{2})$/, '$3/$2/$1') : '—')
</script>

<template>
  <div class="space-y-4">
    <UCard>
      <div class="flex flex-wrap items-center gap-3">
        <PokeAvatar :dex="nv.dex" class="size-12" />
        <div class="min-w-0">
          <h3 class="flex flex-wrap items-center gap-2 text-lg font-bold text-highlighted">
            {{ nv.charname }}
            <UBadge v-if="nv.shiny" size="sm" color="warning" variant="soft" label="Shiny" />
            <UBadge v-if="nv.online === true" color="success" variant="subtle" label="Đang online" />
            <UBadge v-else-if="nv.online === false" color="neutral" variant="subtle" label="Offline" />
            <UBadge v-else color="warning" variant="subtle" label="Chưa rõ online" />
            <UBadge v-if="nv.ban" color="error" variant="subtle" label="Pokémon bị khoá" />
            <UBadge v-if="nv.dangCamChat" color="warning" variant="subtle" :label="`Cấm chat tới ${nv.camChatDen}`" />
          </h3>
          <p class="text-sm text-muted">#{{ nv.id }} · {{ nv.loai }} · vào game lần cuối {{ nv.lastLog || '—' }}</p>
        </div>
        <UButton class="ml-auto" size="sm" color="neutral" variant="outline" icon="i-lucide-settings-2" label="Cấp độ, xoá đồ, khoá Pokémon" :to="`/admin/nhanvat?id=${nv.id}`" />
      </div>
    </UCard>

    <UTabs v-model="tab" :items="TAB" :content="false" variant="link" class="w-full" :ui="{ list: 'overflow-x-auto', trigger: 'shrink-0' }" />

    <!-- TỔNG QUAN -->
    <div v-if="tab === 'tong-quan'" class="space-y-4">
      <div class="grid grid-cols-2 gap-3 md:grid-cols-4">
        <UCard :ui="{ body: 'p-4' }">
          <p class="text-sm text-muted">Cấp</p>
          <p class="text-2xl font-bold tabular-nums">{{ nv.lastLv }}</p>
          <UProgress v-if="nv.phanTramCap !== null" :model-value="nv.phanTramCap" size="sm" class="mt-2" />
          <p class="mt-1 text-xs text-dimmed">{{ nv.phanTramCap !== null ? `${nv.phanTramCap.toLocaleString('vi-VN')}% vào cấp sau` : '' }} · {{ so(nv.xp) }} EXP</p>
        </UCard>
        <AdminStatCard label="Xu" :value="Number(nv.gold)" icon="i-lucide-coins" />
        <AdminStatCard label="Lượng" :value="Number(nv.luong)" icon="i-lucide-gem" />
        <AdminStatCard label="Lượng khoá" :value="Number(nv.luonglock)" icon="i-lucide-lock" />
        <AdminStatCard label="Kho: xu" :value="Number(nv.khoXu)" icon="i-lucide-warehouse" />
        <AdminStatCard label="Kho: lượng / lượng khoá" :value="`${so(nv.khoLuong)} / ${so(nv.khoLuongKhoa)}`" icon="i-lucide-warehouse" />
        <AdminStatCard label="Pokédex đã hạ" :value="`${nv.pokedexDaHa} loài`" icon="i-lucide-book-open" />
        <AdminStatCard label="Thời gian chơi ghi nhận" :value="gio(nv.totalTimePlay)" icon="i-lucide-timer" />
      </div>

      <UCard>
        <template #header><h4 class="font-semibold">Vị trí</h4></template>
        <p v-if="nv.viTri" class="text-sm">
          <b>{{ nv.viTri.ten }}</b> <span class="text-muted">(bản đồ #{{ nv.viTri.map }}<template v-if="nv.viTri.khu !== null">, khu {{ nv.viTri.khu }}</template>) · x {{ nv.viTri.x }}, y {{ nv.viTri.y }}</span>
          <UBadge class="ml-1" size="sm" variant="subtle" :color="nv.viTri.nguon === 'online' ? 'success' : 'neutral'" :label="nv.viTri.nguon === 'online' ? 'đang ở (online)' : 'đã lưu lần thoát cuối'" />
        </p>
        <p v-else class="text-sm text-muted">Không đọc được vị trí.</p>
      </UCard>

      <div class="grid gap-4 lg:grid-cols-2">
        <UCard v-if="duocCamChat || duocVeAnToan">
          <template #header>
            <h4 class="font-semibold">Thao tác khi offline</h4>
            <p class="text-xs text-muted">Ghi thẳng dữ liệu Pokémon nên chỉ làm được khi người chơi đã thoát.</p>
          </template>
          <UAlert v-if="!offline" class="mb-3" color="warning" variant="subtle" icon="i-lucide-triangle-alert" :title="lyDoKhongOffline" />
          <div class="flex flex-wrap gap-2">
            <template v-if="duocCamChat && !chuLaQuanTri">
              <UButton v-if="!nv.dangCamChat" color="warning" icon="i-lucide-message-square-off" label="Cấm chat" :disabled="!offline" :loading="dangLam === 'cam_chat'" @click="camChat" />
              <UButton v-else color="neutral" variant="outline" icon="i-lucide-message-square" label="Bỏ cấm chat" :disabled="!offline" :loading="dangLam === 'bo_cam'" @click="boCamChat" />
            </template>
            <UButton v-if="duocVeAnToan" color="warning" variant="soft" icon="i-lucide-house" label="Về điểm an toàn" :disabled="!offline" :loading="dangLam === 've_an_toan'" @click="veAnToan" />
          </div>
        </UCard>

        <UCard v-if="duocThu">
          <template #header>
            <h4 class="font-semibold">Gửi thư trong game</h4>
            <p class="text-xs text-muted">Người chơi thấy thư ở lần vào game kế tiếp. Gửi được cả khi đang online.</p>
          </template>
          <UTextarea v-model="thu" :maxlength="800" :rows="3" autoresize placeholder="Nội dung (tối đa 800 ký tự)" class="w-full" aria-label="Nội dung thư" />
          <div class="mt-2 flex items-center justify-between gap-2">
            <span class="text-xs text-dimmed tabular-nums">{{ thu.length }}/800</span>
            <UButton icon="i-lucide-send" label="Gửi thư" :disabled="!thu.trim()" :loading="dangLam === 'thu'" @click="guiThu" />
          </div>
        </UCard>

        <UCard v-if="duocTangQua">
          <template #header>
            <h4 class="font-semibold">Tặng quà</h4>
            <p class="text-xs text-muted">Chỉ siêu quản trị. Tạo mã giftcode dùng 1 lần cho riêng Pokémon này và gửi vào hộp thư.</p>
          </template>
          <UButton color="primary" icon="i-lucide-gift" label="Tặng quà…" @click="tangQua" />
        </UCard>
      </div>

      <UCard v-if="sau?.lenh?.length" :ui="{ body: 'p-0 sm:p-0' }">
        <template #header><h4 class="font-semibold">Lệnh web đã gửi cho Pokémon</h4></template>
        <AdminNhanvatBangLenh :lenh="sau.lenh" />
      </UCard>
    </div>

    <!-- PHẦN SÂU -->
    <template v-else>
      <UAlert v-if="error" color="error" variant="subtle" icon="i-lucide-circle-x" :title="loiApi(error)"
        :actions="[{ label: 'Thử lại', color: 'error', variant: 'outline', onClick: () => refresh() }]" />
      <USkeleton v-else-if="status === 'pending' && !sau" class="h-64" />

      <template v-else-if="sau">
        <!-- TÚI ĐỒ -->
        <div v-if="tab === 'tui-do'" class="grid items-start gap-4 xl:grid-cols-2">
          <UCard v-for="p in [
            { k: 'mac', t: 'Đang mặc', r: 'Không mặc gì.' }, { k: 'thoiTrang', t: 'Thời trang', r: 'Không có thời trang.' },
            { k: 'tui', t: 'Hành trang', r: 'Hành trang trống.' }, { k: 'ruong', t: 'Rương', r: 'Rương trống.' }]" :key="p.k">
            <template #header><h4 class="font-semibold">{{ p.t }} <span class="font-normal text-muted">({{ (sau.vatPham as any)[p.k].length }})</span></h4></template>
            <div class="overflow-x-auto">
              <AdminNhanvatBangVatPham :ds="(sau.vatPham as any)[p.k]" :trong="p.r" />
            </div>
          </UCard>
          <p class="text-xs text-dimmed xl:col-span-2">Xoá vật phẩm lỗi: dùng trang <NuxtLink :to="`/admin/nhanvat?id=${nv.id}`" class="text-primary hover:underline">Pokémon</NuxtLink> (đi qua máy chủ game, an toàn cả khi online).</p>
        </div>

        <!-- POKÉDEX -->
        <div v-else-if="tab === 'pokedex'" class="space-y-4">
          <UAlert v-if="!sau.pokedex" color="neutral" variant="subtle" icon="i-lucide-database" title="Chưa có bảng tob_pokedex." />
          <template v-else>
            <div class="grid grid-cols-2 gap-3 md:grid-cols-3">
              <AdminStatCard label="Loài đã hạ" :value="`${sau.pokedex.daHa} / ${sau.pokedex.tongLoai}`" icon="i-lucide-book-open" />
              <AdminStatCard label="Loài đã gặp shiny" :value="sau.pokedex.soShiny" icon="i-lucide-sparkles" />
              <AdminStatCard label="Mốc thưởng đã nhận" :value="`${sau.pokedex.moc.filter(m => m.daNhan).length} / ${sau.pokedex.moc.length}`" icon="i-lucide-trophy" />
            </div>
            <UCard>
              <template #header><h4 class="font-semibold">Mốc thưởng Pokédex</h4></template>
              <ul class="space-y-1 text-sm">
                <li v-for="m in sau.pokedex.moc" :key="m.moc" class="flex flex-wrap items-center gap-2">
                  <UIcon :name="m.daNhan ? 'i-lucide-circle-check' : sau.pokedex.daHa >= m.moc ? 'i-lucide-circle-dot' : 'i-lucide-circle'" :class="m.daNhan ? 'text-success' : sau.pokedex.daHa >= m.moc ? 'text-warning' : 'text-dimmed'" class="size-4" />
                  <b class="tabular-nums">{{ m.moc }} loài</b>
                  <span class="text-muted">{{ m.thuong }}</span>
                  <span v-if="!m.daNhan && sau.pokedex.daHa >= m.moc" class="text-xs text-warning">đủ điều kiện, chưa nhận</span>
                </li>
              </ul>
            </UCard>
            <UCard :ui="{ body: 'p-3 sm:p-4' }">
              <AdminBang :rows="sau.pokedex.ds" :cot="cotDex" ten-csv="pokedex" row-key="dex" rong-tieu-de="Chưa hạ loài nào">
                <template #cell-ten="{ row }">
                  <span class="flex items-center gap-2"><PokeAvatar :dex="row.dex" class="size-6" />{{ row.ten }}</span>
                </template>
              </AdminBang>
            </UCard>
          </template>
        </div>

        <!-- NHIỆM VỤ -->
        <div v-else-if="tab === 'nhiem-vu'" class="space-y-4">
          <UCard>
            <template #header><h4 class="font-semibold">Nhiệm vụ chuỗi cũ (tob_char_quest)</h4></template>
            <ul v-if="sau.nhiemVu.cu.length" class="space-y-1 text-sm">
              <li v-for="q in sau.nhiemVu.cu" :key="q.id"><UBadge size="sm" variant="subtle" :color="q.chinh ? 'primary' : 'neutral'" :label="q.chinh ? 'Chính' : 'Phụ'" /> #{{ q.id }} {{ q.ten }} <span class="text-xs text-dimmed">(trạng thái {{ q.trangThai }})</span></li>
            </ul>
            <p v-else class="text-sm text-muted">Không có nhiệm vụ chuỗi cũ đang nhận.</p>
          </UCard>
          <UCard :ui="{ body: 'p-3 sm:p-4' }">
            <template #header>
              <h4 class="font-semibold">Nhiệm vụ lâu dài ({{ sau.nhiemVu.tongLauDai }})</h4>
              <div v-if="Object.keys(sau.nhiemVu.demTheoLoai).length" class="mt-2 flex flex-wrap gap-1">
                <UBadge v-for="(d, l) in sau.nhiemVu.demTheoLoai" :key="l" color="neutral" variant="outline" :label="`${l}: ${d.xong} xong · ${d.dangLam} đang làm`" />
              </div>
            </template>
            <UAlert v-if="!sau.nhiemVu.coBangLauDai" color="neutral" variant="subtle" title="Chưa có bảng tob_nhiem_vu_lau_dai (máy chủ chưa bật sv.lau_dai)." />
            <AdminBang v-else :rows="sau.nhiemVu.lauDai" :cot="cotNv" ten-csv="nhiem-vu" row-key="ma" rong-tieu-de="Chưa nhận nhiệm vụ lâu dài nào">
              <template #cell-ten="{ row }"><span>{{ row.ten }}</span> <code class="text-xs text-dimmed">{{ row.ma }}</code></template>
              <template #cell-trangThai="{ row }"><UBadge size="sm" variant="subtle" :color="mauTt(row.trangThai)" :label="tenTt(row.trangThai)" /></template>
            </AdminBang>
          </UCard>
        </div>

        <!-- SINH THÁI & LÂU DÀI -->
        <div v-else-if="tab === 'sinh-thai'" class="grid items-start gap-4 lg:grid-cols-2">
          <UCard>
            <template #header>
              <h4 class="font-semibold">Hồ sơ hệ sinh thái</h4>
              <p class="text-xs text-muted">tob_hst_nhan_vat — kỹ năng sống, nhà trẻ, album, danh hiệu.</p>
            </template>
            <UAlert v-if="!sau.sinhThai" color="neutral" variant="subtle" title="Chưa có bảng tob_hst_nhan_vat (hệ sinh thái chưa bật trên máy chủ)." />
            <p v-else-if="!sau.sinhThai.hopLe" class="text-sm text-muted">Pokémon chưa có dữ liệu hệ sinh thái.</p>
            <div v-else class="space-y-3 text-sm">
              <table class="bang-wiki">
                <thead><tr><th>Kỹ năng sống</th><th class="text-right">Cấp</th><th class="text-right">EXP</th></tr></thead>
                <tbody><tr v-for="k in sau.sinhThai.kyNang" :key="k.ten"><td>{{ k.ten }}</td><td class="text-right tabular-nums">{{ k.cap }}</td><td class="text-right tabular-nums">{{ so(k.exp) }}</td></tr></tbody>
              </table>
              <dl class="grid grid-cols-[10rem_1fr] gap-x-3 gap-y-1">
                <dt class="text-muted">Thể lực sinh hoạt</dt><dd>{{ sau.sinhThai.theLuc }} / 200</dd>
                <dt class="text-muted">Vật liệu trong túi</dt><dd>{{ so(sau.sinhThai.tongVatLieu) }} ({{ sau.sinhThai.soLoaiVatLieu }} loại)</dd>
                <dt class="text-muted">Trứng đang ấp</dt><dd>{{ sau.sinhThai.soTrung }}</dd>
                <dt class="text-muted">Nhà trẻ</dt><dd>{{ sau.sinhThai.nhaTre.pet1 || sau.sinhThai.nhaTre.pet2 ? `#${sau.sinhThai.nhaTre.pet1} + #${sau.sinhThai.nhaTre.pet2} · ${so(sau.sinhThai.nhaTre.buoc)} bước` : 'Trống' }}</dd>
                <dt class="text-muted">Dòng giống / nghiên cứu</dt><dd>{{ sau.sinhThai.soDongGiong }} / {{ sau.sinhThai.soLoaiNghienCuu }} loài</dd>
                <dt class="text-muted">Ảnh album</dt><dd>{{ sau.sinhThai.soAnhAlbum }}</dd>
                <dt class="text-muted">Vùng hoàn thành</dt><dd>{{ sau.sinhThai.soVungHoanThanh }}</dd>
                <dt class="text-muted">Điểm thân thiện</dt><dd>{{ so(sau.sinhThai.thanThien) }}</dd>
              </dl>
              <div v-if="sau.sinhThai.danhHieu.length" class="flex flex-wrap gap-1">
                <UBadge v-for="d in sau.sinhThai.danhHieu" :key="d" color="primary" variant="subtle" :label="d" />
              </div>
            </div>
          </UCard>
          <UCard>
            <template #header>
              <h4 class="font-semibold">Hồ sơ lâu dài</h4>
              <p class="text-xs text-muted">tob_char_lau_dai{{ sau.hoSoLauDai?.capNhat ? ` · cập nhật ${sau.hoSoLauDai.capNhat}` : '' }}</p>
            </template>
            <UAlert v-if="!sau.hoSoLauDai" color="neutral" variant="subtle" title="Chưa có bảng tob_char_lau_dai (máy chủ chưa bật sv.lau_dai)." />
            <dl v-else class="grid grid-cols-[10rem_1fr] gap-x-3 gap-y-1 text-sm">
              <dt class="text-muted">Năng lượng huấn luyện</dt><dd>{{ sau.hoSoLauDai.nangLuong.phut100 }} phút 100% / ngày · đã dùng {{ Math.round(sau.hoSoLauDai.nangLuong.msDaDung / 60000) }} phút ({{ ngay8(sau.hoSoLauDai.nangLuong.ngay) }})</dd>
              <dt class="text-muted">Vé chuồng</dt><dd>{{ sau.hoSoLauDai.veChuong }}</dd>
              <dt class="text-muted">Tinh thông</dt><dd>{{ so(sau.hoSoLauDai.tinhThong.diem) }} điểm ({{ sau.hoSoLauDai.tinhThong.loai === 1 ? 'cộng HP' : 'cộng tấn công' }})</dd>
              <dt class="text-muted">Lịch đăng nhập 7 ngày</dt><dd>bước {{ sau.hoSoLauDai.dangNhap7Ngay.buoc }} · lần cuối {{ ngay8(sau.hoSoLauDai.dangNhap7Ngay.ngayCuoi) }}</dd>
              <dt class="text-muted">Shiny</dt><dd>đã gặp {{ sau.hoSoLauDai.shiny.so }} · bộ đếm {{ sau.hoSoLauDai.shiny.dem }}</dd>
              <dt class="text-muted">Giftcode tháng</dt><dd>{{ sau.hoSoLauDai.giftcode.dem }} mã (tháng {{ String(sau.hoSoLauDai.giftcode.thang || '—').replace(/^(\d{4})(\d{2})$/, '$2/$1') }})</dd>
              <dt class="text-muted">Huy hiệu Gym</dt><dd>{{ sau.hoSoLauDai.gym.soHuyHieu }}</dd>
              <dt class="text-muted">Ngày đầu / gần nhất</dt><dd>{{ ngay8(sau.hoSoLauDai.ngayDauTien) }} / {{ ngay8(sau.hoSoLauDai.ngayGanNhat) }}</dd>
              <dt class="text-muted">Chuỗi nhiệm vụ mới</dt><dd>{{ sau.hoSoLauDai.nhiemVu.daChuyenChuoiMoi ? 'Đã chuyển' : 'Chưa' }}</dd>
              <dt class="text-muted">Món đang nâng cấp dở</dt><dd>{{ sau.hoSoLauDai.soMonDangNangCap }}</dd>
            </dl>
            <details v-if="sau.hoSoLauDai?.tho" class="mt-3 text-xs">
              <summary class="cursor-pointer text-muted">Chuỗi gốc</summary>
              <code class="mt-1 block break-all text-dimmed">{{ sau.hoSoLauDai.tho }}</code>
            </details>
          </UCard>
        </div>

        <!-- PVP · HẦM NGỤC · SƯ PHẠM -->
        <div v-else-if="tab === 'pvp'" class="grid items-start gap-4 lg:grid-cols-3">
          <UCard>
            <template #header><h4 class="font-semibold">PvP mùa</h4></template>
            <UAlert v-if="!sau.pvp" color="neutral" variant="subtle" title="Chưa có bảng tob_pvp_mua." />
            <template v-else>
              <p v-if="sau.pvp.hienTai" class="text-sm">Mùa {{ sau.pvp.hienTai.mua }}: <b>{{ sau.pvp.hienTai.elo }} Elo</b> · {{ sau.pvp.ds[0]?.bac }} · {{ sau.pvp.hienTai.thang }} thắng / {{ sau.pvp.hienTai.thua }} thua</p>
              <p v-else class="text-sm text-muted">Chưa đánh trận nào ở mùa hiện tại{{ sau.pvp.muaHienTai !== null ? ` (mùa ${sau.pvp.muaHienTai})` : '' }}.</p>
              <AdminBang v-if="sau.pvp.ds.length" class="mt-3" :rows="sau.pvp.ds" :cot="cotPvp" khong-tim row-key="mua" />
            </template>
          </UCard>
          <UCard>
            <template #header><h4 class="font-semibold">Hầm Ngục</h4></template>
            <UAlert v-if="!sau.hamNguc" color="neutral" variant="subtle" title="Chưa có bảng tob_ham_nguc." />
            <template v-else>
              <p class="text-sm">Tầng cao nhất: <b class="text-lg tabular-nums">{{ sau.hamNguc.tangCao || '—' }}</b> <span v-if="sau.hamNguc.ngayCao" class="text-muted">({{ ngay8(sau.hamNguc.ngayCao) }})</span></p>
              <ul v-if="sau.hamNguc.ds.length > 1" class="mt-2 space-y-0.5 text-xs text-muted">
                <li v-for="h in sau.hamNguc.ds" :key="`${h.ngay}`">{{ ngay8(h.ngay) }}: tầng {{ h.tang }}</li>
              </ul>
            </template>
          </UCard>
          <UCard>
            <template #header><h4 class="font-semibold">Sư phạm</h4></template>
            <UAlert v-if="!sau.suPham" color="neutral" variant="subtle" title="Chưa có bảng tob_su_pham." />
            <div v-else class="space-y-2 text-sm">
              <p>Thầy: <b v-if="sau.suPham.thay">{{ sau.suPham.thay.ten }}</b><span v-else class="text-muted">không có</span> <span v-if="sau.suPham.thay" class="text-xs text-dimmed">từ {{ sau.suPham.thay.batDau }} · mốc {{ sau.suPham.thay.moc || '—' }}</span></p>
              <p>Trò ({{ sau.suPham.tro.length }}):</p>
              <ul v-if="sau.suPham.tro.length" class="list-disc pl-5 text-xs">
                <li v-for="t in sau.suPham.tro" :key="t.id">{{ t.ten }} <span class="text-dimmed">từ {{ t.batDau }} · mốc {{ t.moc || '—' }}</span></li>
              </ul>
            </div>
          </UCard>
        </div>
      </template>
    </template>
  </div>
</template>
