<script lang="ts">
/** Tóm tắt một nhân vật (chi-tiet.get.ts) — NhanVat.vue dùng chung. */
export interface NvHoSo {
  id: number, charname: string, lastLv: number, xp: string, phanTramCap: number | null, class: number, loai: string, dex: number, shiny: boolean
  gold: number | string, luong: number | string, luonglock: number | string, khoXu: number | string, khoLuong: number | string, khoLuongKhoa: number | string
  ban: number, del: number, lastLog: string, totalTimePlay: number, idClan: number, pokedexDaHa: number
  camChatDen: string, dangCamChat: boolean, online: boolean | null, onlineLyDo: string
  viTri: { nguon: 'online' | 'da_luu', map: number, ten: string, khu: number | null, x: number, y: number } | null
}
</script>

<script setup lang="ts">
/**
 * Hồ sơ một tài khoản trong Trung tâm người chơi (/admin/nguoi-choi?u=…).
 * Phần đầu: thông tin + thao tác tài khoản (khoá có hạn, mở khoá, đặt mật khẩu mới).
 * Tab: Nhân vật (chi tiết sâu ở NhanVat.vue) · Đăng nhập & thiết bị · Khoá & cấm chat · Thư & quà · Nhật ký quản trị.
 * Nút nào vai trò hiện tại không dùng được thì ẩn (useAdminMe().duoc) — server vẫn kiểm lại.
 */
import type { CotBang } from '~/components/admin/Bang.vue'
import AdminTaikhoanXacNhan from '~/components/admin/taikhoan/XacNhan.vue'
import HoiLyDo from './HoiLyDo.vue'

const props = defineProps<{ u: string }>()

interface HoSo {
  tk: { id: number, username: string, email: string, phone: string, regdate: string, ban: number, fromgame: number, provider: number, laAdmin: boolean, laChinhMinh: boolean }
  khoaHienTai: { id: number, ly_do: string, het_han: string | null, admin_user: string, tao_luc: string } | null
  khoa: { id: number, loai: string, ly_do: string, het_han: string | null, admin_user: string, tao_luc: string, mo_luc: string | null }[]
  may: { ma_may: string, ip: string, lan_dau: string, lan_cuoi: string, so_lan: number, cungMay: number, biCam: boolean }[]
  nhanVat: NvHoSo[]
  thu: { id: number, charname: string, sender: string, info: string, dateSend: string }[]
  quaRieng: { ma: string, moTa: string, han: string, daDung: boolean, ghiChu: string, taoBoi: string, taoLuc: string, nhanBoi: string, nhanLuc: string }[]
  nhatKy: { id: number, admin_user: string, hanh_dong: string, doi_tuong: string, chi_tiet: string, thoi_gian: string }[]
  mayChu: { giayNhipTim: number | null, soOnline: number | null }
}

const api = useAdminApi()
const toast = useToast()
const me = useAdminMe()
const route = useRoute()
const router = useRouter()
const hoiLyDo = useOverlay().create(HoiLyDo)
const xacNhan = useOverlay().create(AdminTaikhoanXacNhan)

const { data, status, error, refresh } = useAsyncData(
  () => `admin-nc-hs-${props.u}`,
  () => api<HoSo>('/api/admin/nguoi-choi/chi-tiet', { query: { u: props.u } }),
  { server: false, watch: [() => props.u] },
)
const tk = computed(() => data.value?.tk)

// ---- tab
const TAB = [
  { label: 'Pokémon', value: 'nhan-vat', icon: 'i-lucide-paw-print' },
  { label: 'Đăng nhập & thiết bị', value: 'dang-nhap', icon: 'i-lucide-smartphone' },
  { label: 'Khoá & cấm chat', value: 'khoa', icon: 'i-lucide-shield-alert' },
  { label: 'Thư & quà', value: 'thu', icon: 'i-lucide-mail' },
  { label: 'Nhật ký quản trị', value: 'nhat-ky', icon: 'i-lucide-scroll-text' },
]
const tab = computed({
  get: () => (typeof route.query.tab === 'string' && TAB.some(t => t.value === route.query.tab) ? route.query.tab : 'nhan-vat'),
  set: v => router.replace({ query: { ...route.query, tab: v === 'nhan-vat' ? undefined : v } }),
})
const nvChon = computed({
  get: () => {
    const ds = data.value?.nhanVat ?? []
    const id = Number(route.query.nv)
    return ds.find(n => n.id === id) ?? ds[0] ?? null
  },
  set: v => router.replace({ query: { ...route.query, nv: v ? String(v.id) : undefined } }),
})

// ---- quyền
const duocKhoa = computed(() => me.duoc('/api/admin/nguoi-choi/khoa'))
const duocDoiMk = computed(() => !!tk.value && me.duoc(`/api/admin/taikhoan/${tk.value.id}`))

// ---- thao tác tài khoản
const HAN_KHOA = [
  { label: '1 giờ', value: 1 }, { label: '24 giờ', value: 24 }, { label: '3 ngày', value: 72 }, { label: '7 ngày', value: 168 },
  { label: '30 ngày', value: 720 }, { label: '90 ngày', value: 2160 }, { label: 'Vĩnh viễn', value: 0 },
]
const dangLam = ref('')
async function goi(viec: string, url: string, body: Record<string, unknown>) {
  dangLam.value = viec
  try {
    const r = await api<{ message?: string }>(url, { method: 'POST', body })
    toast.add({ title: r.message ?? 'Đã xong', color: 'success', icon: 'i-lucide-check' })
    await refresh()
    return true
  }
  catch (e) {
    toast.add({ title: 'Không thực hiện được', description: loiApi(e), color: 'error', icon: 'i-lucide-circle-x' })
    return false
  }
  finally { dangLam.value = '' }
}
async function khoa() {
  const t = tk.value!
  const kq = await hoiLyDo.open({
    title: `Khoá tài khoản ${t.username}?`,
    description: 'Chặn đăng nhập từ lần sau (login server đọc team_user.ban). Người đang online vẫn chơi tiếp tới khi thoát — game không có lệnh đá người chơi. Hết hạn thì website tự mở (kiểm mỗi 5 phút).',
    nut: 'Khoá tài khoản', mau: 'error', han: HAN_KHOA, hanMacDinh: 24,
    goiY: ['Dùng phần mềm thứ ba / bot', 'Lừa đảo người chơi khác', 'Lợi dụng lỗi game', 'Ngôn từ xúc phạm nghiêm trọng'],
  }).result
  if (kq) await goi('khoa', '/api/admin/nguoi-choi/khoa', { username: t.username, lyDo: kq.lyDo, soGio: kq.soGio })
}
async function moKhoa() {
  const t = tk.value!
  const kq = await hoiLyDo.open({ title: `Mở khoá tài khoản ${t.username}?`, nut: 'Mở khoá', mau: 'primary', batBuoc: false, nhanLyDo: 'Ghi chú' }).result
  if (kq) await goi('mo_khoa', '/api/admin/nguoi-choi/mo-khoa', { username: t.username, lyDo: kq.lyDo })
}

const mk = reactive({ a: '', b: '', hien: false })
async function doiMk() {
  const t = tk.value!
  if (mk.a !== mk.b) return toast.add({ title: 'Hai lần nhập mật khẩu không giống nhau.', color: 'error' })
  const ok = await xacNhan.open({ title: `Đặt mật khẩu mới cho ${t.username}?`, description: 'Mật khẩu cũ hết hiệu lực ngay (băm bằng PASSWORD() như login server). Hãy báo mật khẩu mới cho chủ tài khoản qua kênh đã xác minh.', nut: 'Đặt mật khẩu', mau: 'warning' }).result
  if (!ok) return
  if (await goi('doi_mk', `/api/admin/taikhoan/${t.id}`, { viec: 'doi_mat_khau', matkhau: mk.a, matkhau2: mk.b })) { mk.a = ''; mk.b = '' }
}

// ---- bảng
const cotMay: CotBang[] = [
  { key: 'ma_may', label: 'Mã máy', sort: true },
  { key: 'ip', label: 'IP', sort: true },
  { key: 'so_lan', label: 'Số lần', sort: true, so: true },
  { key: 'lan_dau', label: 'Lần đầu', sort: true, anMobile: true },
  { key: 'lan_cuoi', label: 'Lần cuối', sort: true },
  { key: 'cungMay', label: 'TK khác cùng máy', sort: true, so: true },
]
const cotKhoa: CotBang[] = [
  { key: 'loai', label: 'Loại', giaTri: r => (r.loai === 'khoa' ? 'Khoá tài khoản' : 'Cấm chat') },
  { key: 'ly_do', label: 'Lý do' },
  { key: 'het_han', label: 'Hết hạn', giaTri: r => r.het_han ?? 'Vĩnh viễn' },
  { key: 'admin_user', label: 'Bởi', anMobile: true },
  { key: 'tao_luc', label: 'Lúc', sort: true },
  { key: 'mo_luc', label: 'Đã mở', giaTri: r => r.mo_luc ?? '' },
]
const cotThu: CotBang[] = [
  { key: 'dateSend', label: 'Lúc', sort: true },
  { key: 'charname', label: 'Gửi tới' },
  { key: 'sender', label: 'Người gửi', anMobile: true },
  { key: 'info', label: 'Nội dung' },
]
const cotQua: CotBang[] = [
  { key: 'ma', label: 'Mã' },
  { key: 'moTa', label: 'Phần thưởng' },
  { key: 'trangThai', label: 'Trạng thái', giaTri: r => (r.daDung ? `Đã nhận ${r.nhanLuc}` : r.han && r.han < gioBayGio() ? 'Hết hạn' : 'Chưa dùng') },
  { key: 'taoLuc', label: 'Tạo lúc', sort: true, anMobile: true },
  { key: 'taoBoi', label: 'Bởi', anMobile: true },
]
const cotNhatKy: CotBang[] = [
  { key: 'thoi_gian', label: 'Lúc', sort: true },
  { key: 'admin_user', label: 'Quản trị', sort: true },
  { key: 'hanh_dong', label: 'Thao tác', sort: true },
  { key: 'doi_tuong', label: 'Đối tượng', anMobile: true },
  { key: 'chi_tiet', label: 'Chi tiết', anMobile: true },
]
function gioBayGio() {
  const d = new Date()
  const p = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}
const nhanBan = (ban: number) => (ban === 1 ? { t: 'Bị khoá', m: 'error' as const } : ban === 3 ? { t: 'Chờ xác nhận email', m: 'warning' as const } : { t: 'Hoạt động', m: 'success' as const })
const coOnline = computed(() => (data.value?.nhanVat ?? []).some(n => n.online === true))
</script>

<template>
  <UAlert v-if="error" color="error" variant="subtle" icon="i-lucide-circle-x" :title="loiApi(error)"
    :actions="[{ label: 'Thử lại', color: 'error', variant: 'outline', onClick: () => refresh() }]" />
  <div v-else-if="status === 'pending' && !data" class="space-y-4">
    <USkeleton class="h-36" />
    <USkeleton class="h-72" />
  </div>

  <div v-else-if="data && tk" class="space-y-4">
    <!-- Đầu hồ sơ -->
    <UCard>
      <div class="flex flex-col gap-4 lg:flex-row lg:items-start">
        <div class="flex min-w-0 flex-1 items-start gap-3">
          <UIcon name="i-lucide-user-round" class="size-10 shrink-0 text-primary" />
          <div class="min-w-0">
            <h2 class="flex flex-wrap items-center gap-2 text-xl font-bold text-highlighted">
              <span class="break-all">{{ tk.username }}</span>
              <UBadge :color="nhanBan(tk.ban).m" variant="subtle" :label="nhanBan(tk.ban).t" />
              <UBadge v-if="coOnline" color="success" variant="soft" icon="i-lucide-radio" label="Đang online" />
              <UBadge v-if="tk.laAdmin" color="neutral" variant="outline" label="Quản trị" />
            </h2>
            <dl class="mt-2 grid grid-cols-[6.5rem_1fr] gap-x-3 gap-y-1 text-sm">
              <dt class="text-muted">ID</dt><dd class="tabular-nums">#{{ tk.id }}</dd>
              <dt class="text-muted">Đăng ký</dt><dd>{{ tk.regdate || '—' }} <span v-if="tk.fromgame === 1" class="text-xs text-dimmed">(tạo trong game)</span></dd>
              <dt class="text-muted">Email</dt><dd class="break-all">{{ tk.email || '—' }}</dd>
              <dt class="text-muted">Điện thoại</dt><dd>{{ tk.phone || '—' }}</dd>
              <template v-if="data.khoaHienTai">
                <dt class="text-muted">Đang khoá</dt>
                <dd class="text-error">{{ data.khoaHienTai.het_han ? `tới ${data.khoaHienTai.het_han}` : 'vĩnh viễn' }} — {{ data.khoaHienTai.ly_do }} <span class="text-xs text-dimmed">({{ data.khoaHienTai.admin_user }})</span></dd>
              </template>
            </dl>
          </div>
        </div>

        <div class="flex flex-wrap gap-2 lg:max-w-sm lg:justify-end">
          <template v-if="duocKhoa && !tk.laAdmin && !tk.laChinhMinh">
            <UButton v-if="tk.ban !== 1" color="error" icon="i-lucide-lock" label="Khoá tài khoản" :loading="dangLam === 'khoa'" @click="khoa" />
            <UButton v-else color="success" icon="i-lucide-lock-open" label="Mở khoá" :loading="dangLam === 'mo_khoa'" @click="moKhoa" />
          </template>
          <UTooltip text="Game không có lệnh đá người chơi cho web gọi. Khoá tài khoản chặn từ lần đăng nhập sau; muốn đá ngay hãy dùng lệnh GM trong game.">
            <UButton color="neutral" variant="outline" icon="i-lucide-log-out" label="Đá khỏi game" disabled />
          </UTooltip>
          <UButton color="neutral" variant="outline" icon="i-lucide-user-cog" label="Trang tài khoản" :to="`/admin/taikhoan?id=${tk.id}`" />
        </div>
      </div>
      <p class="mt-3 text-xs text-dimmed">
        <UIcon name="i-lucide-info" class="inline size-3.5 align-text-bottom" />
        Không có cơ chế đá người chơi từ web: khoá chỉ có hiệu lực ở lần đăng nhập sau; cấm chat và đưa về điểm an toàn chỉ làm được khi Pokémon offline.
      </p>
    </UCard>

    <UTabs v-model="tab" :items="TAB" :content="false" class="w-full" :ui="{ list: 'overflow-x-auto', trigger: 'shrink-0' }" />

    <!-- Nhân vật -->
    <template v-if="tab === 'nhan-vat'">
      <UCard v-if="!data.nhanVat.length" class="text-center">
        <UIcon name="i-lucide-paw-print" class="mx-auto size-8 text-dimmed" />
        <p class="mt-2 font-semibold">Tài khoản chưa tạo Pokémon nào.</p>
      </UCard>
      <template v-else>
        <div class="flex gap-2 overflow-x-auto pb-1" role="tablist" aria-label="Chọn Pokémon">
          <button v-for="n in data.nhanVat" :key="n.id" type="button" role="tab" :aria-selected="nvChon?.id === n.id"
            class="flex shrink-0 items-center gap-2 rounded-lg border px-3 py-2 text-left transition"
            :class="nvChon?.id === n.id ? 'border-primary bg-primary/10' : 'border-default hover:bg-elevated'"
            @click="nvChon = n">
            <PokeAvatar :dex="n.dex" class="size-8" />
            <span>
              <span class="block font-semibold">{{ n.charname }}</span>
              <span class="block text-xs text-muted">Lv {{ n.lastLv }} · {{ n.loai }}</span>
            </span>
            <span v-if="n.online === true" class="size-2 rounded-full bg-success" title="Đang online" />
            <UBadge v-if="n.ban" size="sm" color="error" variant="subtle" label="khoá" />
          </button>
        </div>
        <AdminNguoichoiNhanVat v-if="nvChon" :key="nvChon.id" :nv="nvChon" :tai-khoan="tk.username" :chu-la-quan-tri="tk.laAdmin" @doi="refresh" />
      </template>
    </template>

    <!-- Đăng nhập -->
    <template v-else-if="tab === 'dang-nhap'">
      <div class="grid items-start gap-4 lg:grid-cols-[1fr_22rem]">
        <UCard :ui="{ body: 'p-3 sm:p-4' }">
          <template #header>
            <h3 class="font-semibold">Máy & IP đã đăng nhập</h3>
            <p class="text-xs text-muted">Bảng tob_may_dang_nhap (game ghi mỗi lần đăng nhập). Lịch sử online theo giờ chỉ có toàn máy chủ, không theo từng người.</p>
          </template>
          <AdminBang :rows="data.may" :cot="cotMay" ten-csv="may-dang-nhap" rong-tieu-de="Chưa ghi nhận máy nào" rong-mo-ta="Client cũ không gửi mã máy, hoặc tài khoản chưa vào game.">
            <template #cell-ma_may="{ row }">
              <NuxtLink :to="{ path: '/admin/cammay', query: { q: row.ma_may } }" class="font-mono text-xs text-primary hover:underline break-all">{{ row.ma_may }}</NuxtLink>
              <UBadge v-if="row.biCam" class="ml-1" size="sm" color="error" variant="subtle" label="bị cấm" />
            </template>
            <template #cell-ip="{ row }">
              <NuxtLink :to="{ query: { q: row.ip, kieu: 'ip' } }" class="font-mono text-xs hover:text-primary">{{ row.ip || '?' }}</NuxtLink>
            </template>
            <template #cell-cungMay="{ row }">
              <NuxtLink v-if="row.cungMay > 0" :to="{ query: { q: row.ma_may, kieu: 'ma_may' } }" class="font-semibold text-warning hover:underline">{{ row.cungMay }}</NuxtLink>
              <span v-else class="text-dimmed">0</span>
            </template>
          </AdminBang>
        </UCard>
        <UCard v-if="duocDoiMk">
          <template #header><h3 class="font-semibold">Đặt mật khẩu mới</h3></template>
          <p v-if="tk.laAdmin && !tk.laChinhMinh" class="text-sm text-muted">Không đổi được mật khẩu của tài khoản quản trị khác.</p>
          <form v-else class="space-y-2" autocomplete="off" @submit.prevent="doiMk">
            <UInput v-model="mk.a" :type="mk.hien ? 'text' : 'password'" placeholder="Mật khẩu mới" autocomplete="new-password" maxlength="64" class="w-full" aria-label="Mật khẩu mới" />
            <UInput v-model="mk.b" :type="mk.hien ? 'text' : 'password'" placeholder="Nhập lại" autocomplete="new-password" maxlength="64" class="w-full" aria-label="Nhập lại mật khẩu" />
            <UCheckbox v-model="mk.hien" label="Hiện mật khẩu" />
            <UButton type="submit" color="warning" icon="i-lucide-key-round" label="Đặt mật khẩu" :disabled="!mk.a || mk.a !== mk.b" :loading="dangLam === 'doi_mk'" />
            <p class="text-xs text-dimmed">Băm bằng PASSWORD() — đúng cách login server so khớp.</p>
          </form>
        </UCard>
      </div>
    </template>

    <!-- Khoá -->
    <UCard v-else-if="tab === 'khoa'" :ui="{ body: 'p-3 sm:p-4' }">
      <template #header><h3 class="font-semibold">Lịch sử khoá tài khoản / cấm chat</h3></template>
      <AdminBang :rows="data.khoa" :cot="cotKhoa" ten-csv="lich-su-khoa" rong-tieu-de="Chưa bị khoá hay cấm chat lần nào">
        <template #cell-loai="{ row }">
          <UBadge :color="row.loai === 'khoa' ? 'error' : 'warning'" variant="subtle" :label="row.loai === 'khoa' ? 'Khoá tài khoản' : 'Cấm chat'" />
        </template>
        <template #cell-mo_luc="{ row }">
          <span v-if="row.mo_luc" class="text-xs text-muted">{{ row.mo_luc }}</span>
          <UBadge v-else size="sm" color="error" variant="soft" label="đang hiệu lực" />
        </template>
      </AdminBang>
    </UCard>

    <!-- Thư & quà -->
    <div v-else-if="tab === 'thu'" class="space-y-4">
      <UCard :ui="{ body: 'p-3 sm:p-4' }">
        <template #header><h3 class="font-semibold">Thư trong game gần đây</h3></template>
        <AdminBang :rows="data.thu" :cot="cotThu" rong-tieu-de="Chưa có thư nào">
          <template #cell-info="{ row }"><span class="whitespace-pre-line text-sm">{{ row.info }}</span></template>
        </AdminBang>
      </UCard>
      <UCard :ui="{ body: 'p-3 sm:p-4' }">
        <template #header>
          <h3 class="font-semibold">Quà riêng đã tặng</h3>
          <p class="text-xs text-muted">Mã giftcode cá nhân tạo ở tab Pokémon. Mã bị che với vai trò không phải siêu quản trị.</p>
        </template>
        <AdminBang :rows="data.quaRieng" :cot="cotQua" rong-tieu-de="Chưa tặng quà riêng nào">
          <template #cell-ma="{ row }"><code class="text-xs">{{ row.ma }}</code></template>
          <template #cell-trangThai="{ row }">
            <UBadge v-if="row.daDung" color="success" variant="subtle" :label="`Đã nhận${row.nhanBoi ? ` (${row.nhanBoi})` : ''}`" />
            <UBadge v-else-if="row.han && row.han < gioBayGio()" color="neutral" variant="subtle" label="Hết hạn" />
            <UBadge v-else color="warning" variant="subtle" :label="`Chưa dùng · hạn ${row.han}`" />
          </template>
        </AdminBang>
      </UCard>
    </div>

    <!-- Nhật ký -->
    <UCard v-else-if="tab === 'nhat-ky'" :ui="{ body: 'p-3 sm:p-4' }">
      <template #header>
        <h3 class="font-semibold">Thao tác quản trị trên tài khoản này</h3>
        <p class="text-xs text-muted">60 dòng gần nhất của web_admin_log. Trạng thái trước/sau xem ở <NuxtLink to="/admin/lich-su-thay-doi" class="text-primary hover:underline">Lịch sử thay đổi</NuxtLink>.</p>
      </template>
      <AdminBang :rows="data.nhatKy" :cot="cotNhatKy" ten-csv="nhat-ky-nguoi-choi" rong-tieu-de="Chưa có thao tác quản trị nào">
        <template #cell-hanh_dong="{ row }"><code class="text-xs">{{ row.hanh_dong }}</code></template>
      </AdminBang>
    </UCard>
  </div>
</template>
