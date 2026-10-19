<script setup lang="ts">
/**
 * Nạp tiền (khu quản trị) — docs/van-hanh.md. Đơn (xác nhận tay theo mã giao dịch,
 * hoàn tiền / chargeback, cộng tay), đối soát, danh mục gói (CMS), cửa hàng lượng trong game,
 * nhật ký kiểm toán + webhook, đơn mốc cũ (chỉ xem). Ghi chỉ dành cho Siêu quản trị (vaiTro.ts).
 * Mọi thao tác ghi cả nap_nhat_ky lẫn nhật ký admin.
 */
definePageMeta({ layout: 'admin', middleware: 'admin' })

interface Don {
  id: number
  ma_don: string
  user_id: number
  username: string
  char_id: number
  charname: string
  ma_goi: string
  loai: string
  ten_goi: string
  so_tien: number
  luong: number
  trang_thai: string
  nguon: string
  nha_cung_cap: string | null
  ma_gd_ncc: string | null
  so_tien_nhan: number
  tao_luc: string
  thanh_toan_luc: string | null
  cong_luc: string | null
  da_luu_nv: number
  ly_do: string
}
interface Goi { ma: string, ten: string, loai: string, gia_vnd: number, luong: number, mo_ta: string, dang_ban: number, thu_tu: number }
interface Mon { ma: string, ten: string, nhom: string, trao: string, gia_luong: number, han_ngay: number, gioi_han: number, nhom_xoay: number, giao_dich_duoc: number, co_chi_so: number, dang_ban: number, thu_tu: number, mo_ta: string, loi: string | null }
interface CanhBao { napBat: boolean, daCoGiayPhep: boolean, sandbox: boolean, sepay: boolean, casso: boolean, taiKhoanMacDinh: boolean, yeuCauBat: boolean, lyDoTat: string[], jobSepay: boolean, demo?: boolean, demoYeuCau?: boolean, demoChiAdmin?: boolean, demoLyDoTuChoi?: string[], demoSoTaiKhoan?: string }
interface TongQuan {
  coBang: boolean
  canhBao: CanhBao
  thongKe?: { trang_thai: string, so: number, tien: number }[]
  choCongLau?: Don[]
  congChuaLuu?: Don[]
  webhookLoi?: { id: number, nha_cung_cap: string, ma_gd: string, so_tien: number, noi_dung: string, ma_don: string, ket_qua: string, tao_luc: string }[]
  theoNgay?: { ngay: string, so: number, tien: number }[]
  soCai?: { loai: string, so: number, luong: number, tien: number }[]
  serverGame?: { lanCuoiMs: number, soGiay: number } | null
  lechTien?: Don[]
  tran?: { tuan: number, thang: number }
}

const api = useAdminApi()
const toast = useToast()
const me = useAdminMe()
const laSieu = computed(() => me.vaiTro.value === 'sieu_quan_tri')

const tab = ref('don')
const tabs = [
  { label: 'Đơn nạp', value: 'don', icon: 'i-lucide-receipt' },
  { label: 'Đối soát', value: 'tong_quan', icon: 'i-lucide-scale' },
  { label: 'Gói (CMS)', value: 'goi', icon: 'i-lucide-package' },
  { label: 'Cửa hàng lượng', value: 'cua_hang', icon: 'i-lucide-store' },
  { label: 'Nhật ký', value: 'nhat_ky', icon: 'i-lucide-scroll-text' },
  { label: 'Mốc cũ', value: 'moc_cu', icon: 'i-lucide-archive' },
]
const tabsHien = computed(() => tq.value?.canhBao.demo ? [{ label: 'Đơn DEMO', value: 'demo', icon: 'i-lucide-flask-conical' }, ...tabs] : tabs)

// ---------------------------------------------------------------- tổng quan
const tq = ref<TongQuan | null>(null)
async function taiTongQuan() {
  try {
    tq.value = await api<TongQuan>('/api/admin/naptien/tong-quan')
  } catch (e) {
    toast.add({ title: loiApi(e), color: 'error' })
  }
}
const tk = computed(() => Object.fromEntries((tq.value?.thongKe ?? []).map(r => [r.trang_thai, r])))
const serverCham = computed(() => !tq.value?.serverGame || tq.value.serverGame.soGiay > 180)

// ---------------------------------------------------------------- đơn
const TAT_CA = 'tat_ca'
const locTrangThai = ref<string>(TAT_CA)
const locLoai = ref<string>(TAT_CA)
const tim = ref('')
const page = ref(1)
const dons = ref<{ dons: Don[], tong: number, page: number, perPage: number } | null>(null)
const dangTai = ref(false)
const itemsTrangThai = [{ label: 'Mọi trạng thái', value: TAT_CA }, ...Object.entries(TEN_TRANG_THAI_NAP).map(([value, label]) => ({ label, value }))]
const itemsLoai = [{ label: 'Mọi loại', value: TAT_CA }, ...Object.entries(TEN_LOAI_GOI).map(([value, label]) => ({ label, value }))]

async function taiDon() {
  dangTai.value = true
  try {
    dons.value = await api('/api/admin/naptien/don', {
      query: { trang_thai: locTrangThai.value === TAT_CA ? '' : locTrangThai.value, loai: locLoai.value === TAT_CA ? '' : locLoai.value, q: tim.value.trim(), page: page.value },
    })
  } catch (e) {
    toast.add({ title: loiApi(e), color: 'error' })
  } finally {
    dangTai.value = false
  }
}
watch([locTrangThai, locLoai], () => {
  if (page.value !== 1) page.value = 1
  else taiDon()
})
watch(page, taiDon)

const xn = ref<{ don: Don, maGd: string, ghiChu: string } | null>(null)
const ht = ref<{ don: Don, lyDo: string, chargeback: boolean } | null>(null)
const ct = ref<{ username: string, charId: number | undefined, maGoi: string, maGd: string, lyDo: string } | null>(null)
const dangGui = ref(false)

async function gui(url: string, body: Record<string, unknown>, xong: () => void) {
  dangGui.value = true
  try {
    const r = await api<{ message?: string }>(url, { method: 'POST', body })
    toast.add({ title: r.message ?? 'Đã lưu', color: 'success', icon: 'i-lucide-check' })
    xong()
    await Promise.all([taiDon(), taiTongQuan()])
  } catch (e) {
    toast.add({ title: loiApi(e), color: 'error', icon: 'i-lucide-circle-alert' })
  } finally {
    dangGui.value = false
  }
}
const xacNhan = () => xn.value && gui('/api/admin/naptien/xac-nhan', { id: xn.value.don.id, ma_gd: xn.value.maGd.trim(), ghi_chu: xn.value.ghiChu }, () => { xn.value = null })
const hoanTien = () => ht.value && gui('/api/admin/naptien/hoan-tien', { id: ht.value.don.id, ly_do: ht.value.lyDo, chargeback: ht.value.chargeback }, () => { ht.value = null })
const congTay = () => ct.value && gui('/api/admin/naptien/cong-tay', { username: ct.value.username, char_id: ct.value.charId, ma_goi: ct.value.maGoi, ma_gd: ct.value.maGd, ly_do: ct.value.lyDo }, () => { ct.value = null })
const giaLap = (d: Don, thieu = false) => gui('/api/admin/naptien/gia-lap', { ma_don: d.ma_don, so_tien: thieu ? Number(d.so_tien) - 1000 : undefined }, () => {})

const cotDon = [
  { accessorKey: 'id', header: '#' },
  { accessorKey: 'ma_don', header: 'Mã đơn' },
  { accessorKey: 'username', header: 'Tài khoản / Pokémon' },
  { accessorKey: 'ten_goi', header: 'Gói' },
  { accessorKey: 'so_tien', header: 'Số tiền', meta: { class: { th: 'text-right', td: 'text-right tabular-nums' } } },
  { accessorKey: 'trang_thai', header: 'Trạng thái' },
  { accessorKey: 'tao_luc', header: 'Thời gian' },
  { id: 'hanh_dong', header: '' },
]

// ---------------------------------------------------------------- gói
const goi = ref<Goi[]>([])
const suaGoi = ref<Goi | null>(null)
async function taiGoi() {
  try {
    goi.value = (await api<{ goi: Goi[] }>('/api/admin/naptien/goi')).goi
  } catch (e) {
    toast.add({ title: loiApi(e), color: 'error' })
  }
}
const luongDuKien = computed(() => suaGoi.value?.loai === 'luong' ? Math.trunc((suaGoi.value.gia_vnd || 0) / 100) : 0)
const luuGoi = () => suaGoi.value && gui('/api/admin/naptien/goi', { ...suaGoi.value, dang_ban: !!suaGoi.value.dang_ban }, () => { suaGoi.value = null; taiGoi() })

// ---------------------------------------------------------------- cửa hàng
const mon = ref<Mon[]>([])
const nhomMon = ref<string[]>([])
const traoMon = ref<string[]>([])
const suaMon = ref<Mon | null>(null)
async function taiMon() {
  try {
    const r = await api<{ ds: Mon[], nhom: string[], trao: string[] }>('/api/admin/naptien/cua-hang')
    mon.value = r.ds
    nhomMon.value = r.nhom
    traoMon.value = r.trao
  } catch (e) {
    toast.add({ title: loiApi(e), color: 'error' })
  }
}
const luuMon = () => suaMon.value && gui('/api/admin/naptien/cua-hang', { ...suaMon.value, dang_ban: !!suaMon.value.dang_ban, giao_dich_duoc: !!suaMon.value.giao_dich_duoc }, () => { suaMon.value = null; taiMon() })

// ---------------------------------------------------------------- nhật ký
const nkMaDon = ref('')
const nk = ref<{ nhatKy: any[], webhook: any[], soCai: any[] } | null>(null)
async function taiNhatKy() {
  try {
    nk.value = await api('/api/admin/naptien/nhat-ky', { query: { ma_don: nkMaDon.value.trim() } })
  } catch (e) {
    toast.add({ title: loiApi(e), color: 'error' })
  }
}
function xemNhatKy(d: Don) {
  nkMaDon.value = d.ma_don
  tab.value = 'nhat_ky'
}

// ---------------------------------------------------------------- đơn DEMO (HTBB_NAP_DEMO=1)
// Đơn demo chỉ nằm trong bộ nhớ web; "Giả lập thanh toán" chỉ đổi màn hình người tạo đơn sang thành công,
// KHÔNG ghi database, KHÔNG cộng lượng / Thành viên.
interface DonDemo { ma_don: string, username: string, ten_goi: string, loai: string, so_tien: number, charname: string, trang_thai: string, tao_luc: string, cong_luc: string | null, gia_lap_boi: string }
const demo = ref<{ demo: boolean, dons: DonDemo[], webhook: { luc: string, duongDan: string, ip: string }[] } | null>(null)
async function taiDemo() {
  try {
    demo.value = await api('/api/admin/naptien/demo-don')
  } catch (e) {
    toast.add({ title: loiApi(e), color: 'error' })
  }
}
const giaLapDemo = (d: DonDemo) => gui('/api/admin/naptien/demo-gia-lap', { ma_don: d.ma_don }, () => { taiDemo() })

// ---------------------------------------------------------------- mốc cũ
const mocCu = ref<any[]>([])
async function taiMocCu() {
  try {
    mocCu.value = (await api<{ dons: any[] }>('/api/admin/naptien/moc-cu')).dons
  } catch (e) {
    toast.add({ title: loiApi(e), color: 'error' })
  }
}

watch(tab, (t) => {
  if (t === 'tong_quan') taiTongQuan()
  if (t === 'goi') taiGoi()
  if (t === 'cua_hang') taiMon()
  if (t === 'nhat_ky') taiNhatKy()
  if (t === 'moc_cu') taiMocCu()
  if (t === 'demo') taiDemo()
})
watch(() => tq.value?.canhBao.demo, (b) => {
  if (b && tab.value === 'don') tab.value = 'demo'
})
onMounted(() => {
  taiTongQuan()
  taiDon()
  taiGoi()
})
</script>

<template>
  <AdminPage title="Nạp tiền">
    <template #right>
      <UButton v-if="laSieu && tq?.canhBao.napBat" icon="i-lucide-hand-coins" label="Cộng tay" @click="ct = { username: '', charId: undefined, maGoi: goi[0]?.ma ?? '', maGd: '', lyDo: '' }" />
      <UButton color="neutral" variant="outline" icon="i-lucide-refresh-cw" :loading="dangTai" @click="taiDon(); taiTongQuan()">
        Tải lại
      </UButton>
    </template>

    <!-- ============ CẢNH BÁO ============ -->
    <UAlert
      v-if="tq?.canhBao.demo"
      color="error"
      variant="solid"
      icon="i-lucide-flask-conical"
      title="BẢN DEMO — không chuyển tiền. Số tài khoản chỉ để minh hoạ."
      :description="`HTBB_NAP_DEMO=1: /naptien và /thanh-vien hiện luồng minh hoạ (TK ${tq.canhBao.demoSoTaiKhoan}, hình QR giả, không mở app ngân hàng) cho ${tq.canhBao.demoChiAdmin ? 'tài khoản trong HTBB_ADMIN_ACCOUNTS' : 'MỌI người'}. Đơn demo chỉ nằm trong bộ nhớ, không vào nap_don; webhook và xác nhận tay bị từ chối; không cộng gì vào game.`"
    />
    <UAlert
      v-if="tq?.canhBao.demoYeuCau && !tq.canhBao.demo"
      color="warning"
      variant="subtle"
      icon="i-lucide-flask-conical-off"
      title="HTBB_NAP_DEMO=1 bị TỪ CHỐI — giữ chế độ hiện tại"
      :description="(tq.canhBao.demoLyDoTuChoi ?? []).join(' · ')"
    />
    <UAlert
      v-if="tq?.canhBao.napBat && !tq.canhBao.daCoGiayPhep"
      color="error"
      variant="solid"
      icon="i-lucide-octagon-alert"
      title="NẠP TIỀN ĐANG BẬT NHƯNG CHƯA XÁC NHẬN GIẤY PHÉP"
      description="Asset PMDCollab là CC BY-NC 4.0 và Pokémon là IP của Nintendo/The Pokémon Company. Thu tiền khi chưa có giấy phép là vi phạm. Tắt HTBB_NAP_BAT ngay, hoặc khi đã có giấy phép bằng văn bản thì đặt NAP_DA_CO_GIAY_PHEP=1 (docs/van-hanh.md)."
    />
    <UAlert
      v-if="tq?.canhBao.yeuCauBat && tq.canhBao.lyDoTat.length"
      color="error"
      variant="solid"
      icon="i-lucide-shield-x"
      title="HTBB_NAP_BAT=true nhưng nạp BỊ TẮT (fail closed): cấu hình chưa an toàn"
      :description="tq.canhBao.lyDoTat.join(' · ')"
    />
    <UAlert v-else-if="tq && !tq.canhBao.napBat && !tq.canhBao.demo" color="neutral" variant="subtle" icon="i-lucide-power-off" title="Nạp tiền đang TẮT (HTBB_NAP_BAT=false)" description="Trang /naptien và mọi API /api/nap/** trả 403. Có thể chuẩn bị gói/cửa hàng ở đây trước. Bật cần: giấy phép, bảng nap_* (sql/nang_cap.sql), sv.nap_tien=1 + sv.lau_dai=1 ở server game." />
    <UAlert v-if="tq?.canhBao.sandbox" color="warning" variant="subtle" icon="i-lucide-flask-conical" title="Chế độ thử (HTBB_NAP_SANDBOX=1)" description="Có nút Giả lập thanh toán và /api/nap/webhook-mock. KHÔNG bật trên máy thật." />
    <UAlert v-if="tq?.canhBao.napBat && !tq.canhBao.sepay && !tq.canhBao.casso" color="warning" variant="subtle" icon="i-lucide-triangle-alert" title="Chưa cấu hình webhook (SePay / Casso)" description="Đơn KHÔNG tự xác nhận. Đối chiếu sao kê rồi bấm Xác nhận (nhập mã giao dịch)." />
    <UAlert v-if="tq?.canhBao.napBat && tq.canhBao.taiKhoanMacDinh" color="error" variant="subtle" icon="i-lucide-landmark" title="Chưa đặt tài khoản nhận tiền" description="Đặt HTBB_TPBANK_ACCOUNT_NO / HTBB_TPBANK_ACCOUNT_NAME (và HTBB_NAP_BANK_BIN nếu không phải TPBank)." />
    <UAlert v-if="tq && !tq.coBang" color="error" variant="subtle" icon="i-lucide-database" title="Chưa có bảng nap_*" description="Chạy sql/nang_cap.sql trên htbb_db." />
    <UAlert v-else-if="tq?.canhBao.napBat && serverCham" color="warning" variant="subtle" icon="i-lucide-server-off" title="Server game không báo nhịp nạp" :description="tq.serverGame ? `Lần cuối ${tq.serverGame.soGiay} giây trước.` : 'Chưa từng báo — kiểm tra sv.nap_tien=1 và sv.lau_dai=1 trong server.ini rồi khởi động lại.'" />

    <!-- Công tắc bán / hoàn tiền / trần: CÙNG khoá với trang Cấu hình hệ thống (nhóm Nạp tiền) — không có công tắc thứ hai. -->
    <details class="rounded-lg border border-default">
      <summary class="cursor-pointer select-none px-4 py-2 text-sm font-medium">Công tắc bán, hoàn tiền, trần chi tiêu (Cấu hình hệ thống)</summary>
      <div class="p-3"><AdminHethongCauHinhNhom nhom="nap_tien" /></div>
    </details>
    <UTabs v-model="tab" :items="tabsHien" :content="false" class="w-full" />

    <!-- ============ ĐƠN DEMO ============ -->
    <template v-if="tab === 'demo'">
      <UCard>
        <template #header>
          <div class="flex items-center justify-between gap-3">
            <b>Đơn DEMO (bộ nhớ, mất khi khởi động lại web)</b>
            <UButton size="xs" color="neutral" variant="outline" icon="i-lucide-refresh-cw" @click="taiDemo">
              Tải lại
            </UButton>
          </div>
        </template>
        <p class="mb-3 text-sm text-muted">
          "Giả lập thanh toán" chỉ đổi màn hình của người tạo đơn sang thành công — không ghi database, không cộng lượng, không cấp Thành viên.
        </p>
        <ul v-if="demo?.dons.length" class="divide-y divide-default/60 text-sm">
          <li v-for="d in demo.dons" :key="d.ma_don" class="flex flex-wrap items-center justify-between gap-2 py-2">
            <span>
              <span class="font-mono">{{ d.ma_don }}</span> · {{ d.username }}{{ d.charname ? ` → ${d.charname}` : '' }} · {{ d.ten_goi }} · {{ vnd(d.so_tien) }} · <span class="text-muted">{{ d.tao_luc }}</span>
            </span>
            <span class="flex items-center gap-2">
              <UBadge :color="d.trang_thai === 'da_cong' ? 'success' : 'warning'" variant="subtle" :label="d.trang_thai === 'da_cong' ? `Đã giả lập (${d.gia_lap_boi})` : 'Chờ (demo)'" />
              <UButton v-if="laSieu && d.trang_thai === 'cho_thanh_toan'" size="xs" color="warning" variant="soft" icon="i-lucide-flask-conical" :loading="dangGui" @click="giaLapDemo(d)">
                Giả lập thanh toán
              </UButton>
            </span>
          </li>
        </ul>
        <p v-else class="text-sm text-muted">
          Chưa có đơn demo. Đăng nhập website bằng tài khoản quản trị, vào /naptien hoặc /thanh-vien để tạo.
        </p>
      </UCard>
      <UCard v-if="demo?.webhook.length">
        <template #header>
          <b>Webhook bị từ chối khi đang demo</b>
        </template>
        <ul class="text-sm">
          <li v-for="(w, i) in demo.webhook" :key="i">
            {{ w.luc }} · <span class="font-mono">{{ w.duongDan }}</span> · {{ w.ip }}
          </li>
        </ul>
      </UCard>
    </template>

    <!-- ============ ĐƠN ============ -->
    <template v-else-if="tab === 'don'">
      <UCard>
        <form class="flex flex-wrap items-end gap-3" @submit.prevent="page = 1; taiDon()">
          <UFormField label="Trạng thái">
            <USelect v-model="locTrangThai" :items="itemsTrangThai" class="w-60" />
          </UFormField>
          <UFormField label="Loại">
            <USelect v-model="locLoai" :items="itemsLoai" class="w-40" />
          </UFormField>
          <UFormField label="Tìm" hint="mã đơn, tài khoản, Pokémon hoặc #id" class="min-w-64 flex-1">
            <UInput v-model="tim" icon="i-lucide-search" placeholder="HT7F3AQ2BX / hoahv / #12" class="w-full" />
          </UFormField>
          <UButton type="submit" icon="i-lucide-search">
            Lọc
          </UButton>
        </form>
      </UCard>
      <UCard :ui="{ body: 'p-0 sm:p-0' }">
        <UTable :data="dons?.dons ?? []" :columns="cotDon" :loading="dangTai" empty="Không có đơn nào khớp bộ lọc.">
          <template #ma_don-cell="{ row }">
            <span class="font-mono">{{ row.original.ma_don }}</span>
            <div v-if="row.original.nguon === 'admin'" class="text-xs text-warning">
              cộng tay
            </div>
          </template>
          <template #username-cell="{ row }">
            <span class="font-medium text-highlighted">{{ row.original.username }}</span>
            <div class="text-xs text-muted">
              {{ row.original.charname }} (#{{ row.original.char_id }})
            </div>
          </template>
          <template #so_tien-cell="{ row }">
            {{ vnd(row.original.so_tien) }}
            <div v-if="row.original.loai === 'luong'" class="text-xs text-muted">
              {{ so(row.original.luong) }} lượng
            </div>
          </template>
          <template #trang_thai-cell="{ row }">
            <UBadge :color="MAU_TRANG_THAI_NAP[row.original.trang_thai] ?? 'neutral'" variant="subtle" :label="TEN_TRANG_THAI_NAP[row.original.trang_thai] ?? row.original.trang_thai" />
            <div v-if="row.original.ma_gd_ncc" class="mt-1 text-xs text-muted">
              {{ row.original.nha_cung_cap }} · {{ row.original.ma_gd_ncc }}
            </div>
            <div v-if="row.original.trang_thai === 'da_cong' && !Number(row.original.da_luu_nv)" class="text-xs text-error">
              server chưa lưu Pokémon
            </div>
          </template>
          <template #tao_luc-cell="{ row }">
            <span class="text-xs text-muted">{{ row.original.tao_luc }}<br>{{ row.original.thanh_toan_luc ?? '' }}</span>
          </template>
          <template #hanh_dong-cell="{ row }">
            <div class="flex flex-wrap justify-end gap-1">
              <UButton v-if="laSieu && ['cho_thanh_toan', 'that_bai'].includes(row.original.trang_thai)" size="xs" @click="xn = { don: row.original, maGd: '', ghiChu: '' }">
                Xác nhận
              </UButton>
              <UButton v-if="laSieu && tq?.canhBao.sandbox && row.original.trang_thai === 'cho_thanh_toan'" size="xs" color="warning" variant="soft" @click="giaLap(row.original)">
                Giả lập
              </UButton>
              <UButton v-if="laSieu && ['da_thanh_toan', 'da_cong'].includes(row.original.trang_thai)" size="xs" color="error" variant="soft" @click="ht = { don: row.original, lyDo: '', chargeback: false }">
                Hoàn tiền
              </UButton>
              <UButton size="xs" color="neutral" variant="ghost" icon="i-lucide-scroll-text" aria-label="Nhật ký đơn" @click="xemNhatKy(row.original)" />
            </div>
          </template>
        </UTable>
      </UCard>
      <div v-if="dons && dons.tong > dons.perPage" class="flex justify-center">
        <UPagination v-model:page="page" :total="dons.tong" :items-per-page="dons.perPage" />
      </div>
    </template>

    <!-- ============ ĐỐI SOÁT ============ -->
    <template v-else-if="tab === 'tong_quan' && tq?.coBang">
      <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <AdminStatCard label="Chờ thanh toán" :value="tk.cho_thanh_toan?.so ?? 0" :note="vnd(tk.cho_thanh_toan?.tien ?? 0)" icon="i-lucide-hourglass" />
        <AdminStatCard label="Đã thanh toán, chờ vào game" :value="tk.da_thanh_toan?.so ?? 0" :note="vnd(tk.da_thanh_toan?.tien ?? 0)" :mau="(tq.choCongLau?.length ?? 0) > 0 ? 'warn' : ''" icon="i-lucide-banknote" />
        <AdminStatCard label="Đã cộng" :value="tk.da_cong?.so ?? 0" :note="vnd(tk.da_cong?.tien ?? 0)" mau="ok" icon="i-lucide-check" />
        <AdminStatCard label="Hoàn / thu hồi" :value="(tk.hoan_tien?.so ?? 0) + (tk.cho_thu_hoi?.so ?? 0) + (tk.da_thu_hoi?.so ?? 0)" :mau="(tk.cho_thu_hoi?.so ?? 0) > 0 ? 'warn' : ''" icon="i-lucide-undo-2" />
      </div>
      <p class="text-sm text-muted">
        Trần nạp mỗi tài khoản: {{ vnd(tq.tran?.tuan ?? 0) }}/tuần, {{ vnd(tq.tran?.thang ?? 0) }}/tháng (bảng nap_cau_hinh, kẹp ≤ 5.000.000 đ).
        Server game: {{ tq.serverGame ? `${tq.serverGame.soGiay} giây trước` : 'chưa báo nhịp' }}. Job dò SePay (lỡ webhook): {{ tq.canhBao.jobSepay ? 'đang chạy' : 'tắt (HTBB_SEPAY_API_TOKEN trống)' }}.
      </p>
      <UCard v-if="tq.choCongLau?.length">
        <template #header>
          <b>Đã thanh toán &gt; 10 phút mà chưa vào game</b> <span class="text-sm text-muted">(Pokémon chưa online, hoặc server game tắt nạp)</span>
        </template>
        <ul class="text-sm">
          <li v-for="d in tq.choCongLau" :key="d.id">
            <span class="font-mono">{{ d.ma_don }}</span> · {{ d.username }} → {{ d.charname }} · {{ vnd(d.so_tien) }} · {{ d.thanh_toan_luc }}
          </li>
        </ul>
      </UCard>
      <UCard v-if="tq.lechTien?.length">
        <template #header>
          <b class="text-warning">Chuyển dư / chuyển thiếu</b> <span class="text-sm text-muted">(dư: hoàn phần dư cho khách; thiếu: xác nhận tay khi khách chuyển bù hoặc hoàn lại)</span>
        </template>
        <ul class="text-sm">
          <li v-for="d in tq.lechTien" :key="d.id">
            <span class="font-mono">{{ d.ma_don }}</span> · {{ d.username }} · phải {{ vnd(d.so_tien) }}, nhận {{ vnd(d.so_tien_nhan) }}
            ({{ d.so_tien_nhan > d.so_tien ? `dư ${vnd(d.so_tien_nhan - d.so_tien)}` : `thiếu ${vnd(d.so_tien - d.so_tien_nhan)}` }}) · {{ TEN_TRANG_THAI_NAP[d.trang_thai] }}
          </li>
        </ul>
      </UCard>
      <UCard v-if="tq.congChuaLuu?.length">
        <template #header>
          <b class="text-error">Đã cộng nhưng server chưa lưu Pokémon</b> <span class="text-sm text-muted">(kiểm tra Pokémon trong game, cộng tay nếu mất)</span>
        </template>
        <ul class="text-sm">
          <li v-for="d in tq.congChuaLuu" :key="d.id">
            <span class="font-mono">{{ d.ma_don }}</span> · {{ d.charname }} · {{ d.cong_luc }}
          </li>
        </ul>
      </UCard>
      <UCard>
        <template #header>
          <b>Tiền vào theo ngày (30 ngày) — đối chiếu sao kê</b>
        </template>
        <UTable :data="tq.theoNgay ?? []" :columns="[{ accessorKey: 'ngay', header: 'Ngày' }, { accessorKey: 'so', header: 'Số đơn' }, { accessorKey: 'tien', header: 'Tiền' }]" empty="Chưa có.">
          <template #tien-cell="{ row }">
            {{ vnd(row.original.tien) }}
          </template>
        </UTable>
      </UCard>
      <UCard>
        <template #header>
          <b>Webhook lỗi / không khớp (7 ngày)</b>
        </template>
        <UTable :data="tq.webhookLoi ?? []" :columns="[{ accessorKey: 'tao_luc', header: 'Lúc' }, { accessorKey: 'nha_cung_cap', header: 'Cổng' }, { accessorKey: 'ma_gd', header: 'Mã GD' }, { accessorKey: 'so_tien', header: 'Tiền' }, { accessorKey: 'noi_dung', header: 'Nội dung' }, { accessorKey: 'ket_qua', header: 'Kết quả' }]" empty="Không có." />
      </UCard>
      <UCard>
        <template #header>
          <b>Sổ cái theo loại</b>
        </template>
        <UTable :data="tq.soCai ?? []" :columns="[{ accessorKey: 'loai', header: 'Loại' }, { accessorKey: 'so', header: 'Dòng' }, { accessorKey: 'luong', header: 'Lượng' }, { accessorKey: 'tien', header: 'Tiền (đ)' }]" empty="Chưa có." />
      </UCard>
    </template>

    <!-- ============ GÓI ============ -->
    <template v-else-if="tab === 'goi'">
      <UAlert color="info" variant="subtle" icon="i-lucide-info" title="Luật gói" description="Gói lượng luôn đúng tỷ giá 10 lượng / 1.000 đ (không khuyến mãi %, không ngẫu nhiên). Thẻ tháng / Vé mùa không kèm lượng tức thì. Sửa giá không ảnh hưởng đơn đã tạo." />
      <UCard :ui="{ body: 'p-0 sm:p-0' }">
        <UTable :data="goi" :columns="[{ accessorKey: 'thu_tu', header: '#' }, { accessorKey: 'ma', header: 'Mã' }, { accessorKey: 'ten', header: 'Tên' }, { accessorKey: 'loai', header: 'Loại' }, { accessorKey: 'gia_vnd', header: 'Giá' }, { accessorKey: 'luong', header: 'Lượng' }, { accessorKey: 'dang_ban', header: 'Bán' }, { id: 'sua', header: '' }]">
          <template #gia_vnd-cell="{ row }">
            {{ vnd(row.original.gia_vnd) }}
          </template>
          <template #dang_ban-cell="{ row }">
            <UBadge :color="Number(row.original.dang_ban) ? 'success' : 'neutral'" variant="subtle" :label="Number(row.original.dang_ban) ? 'đang bán' : 'ngừng'" />
          </template>
          <template #sua-cell="{ row }">
            <UButton v-if="laSieu" size="xs" color="neutral" variant="ghost" icon="i-lucide-pencil" aria-label="Sửa" @click="suaGoi = { ...row.original }" />
          </template>
        </UTable>
      </UCard>
      <UButton v-if="laSieu" icon="i-lucide-plus" label="Thêm gói" class="w-fit" @click="suaGoi = { ma: '', ten: '', loai: 'luong', gia_vnd: 10000, luong: 100, mo_ta: '', dang_ban: 1, thu_tu: (goi.at(-1)?.thu_tu ?? 0) + 1 }" />
    </template>

    <!-- ============ CỬA HÀNG ============ -->
    <template v-else-if="tab === 'cua_hang'">
      <UAlert color="info" variant="subtle" icon="i-lucide-shield-check" title="Không bán sức mạnh" description="Chỉ 4 cách trao: trang_tri (sở hữu trang trí), mo_rong_tui (+42 ô hành trang), doi_ten, tang_toc (chỉ 5 mã cố định: ve_exp_15, ve_exp_2, bua_the_luc, uy_thac, keo_hiem — giá cố định, giới hạn mua/tuần, chỉ EXP, dùng chung trần 60 điểm/ngày, 210/tuần với bản khoá kiếm bằng chơi). Không chỉ số, tên không chứa từ cấm (EXP, x2, tỉ lệ, đá, chỉ số...) trừ món tăng tốc. Server game tự bỏ dòng sai luật và nạp lại sau ≤ 60 giây. Nhóm xoay 1–4 = bán theo tuần (xoay vòng 4 tuần)." />
      <UCard :ui="{ body: 'p-0 sm:p-0' }">
        <UTable :data="mon" :columns="[{ accessorKey: 'ma', header: 'Mã' }, { accessorKey: 'ten', header: 'Tên' }, { accessorKey: 'nhom', header: 'Nhóm' }, { accessorKey: 'trao', header: 'Trao' }, { accessorKey: 'gia_luong', header: 'Lượng' }, { accessorKey: 'nhom_xoay', header: 'Xoay' }, { accessorKey: 'loi', header: 'Kiểm luật' }, { id: 'sua', header: '' }]">
          <template #ten-cell="{ row }">
            <span :class="Number(row.original.dang_ban) ? '' : 'text-dimmed line-through'">{{ row.original.ten }}</span>
            <div class="text-xs text-muted">
              {{ row.original.han_ngay ? `${row.original.han_ngay} ngày` : 'vĩnh viễn' }}{{ Number(row.original.giao_dich_duoc) ? ' · giao dịch được' : '' }}{{ row.original.gioi_han ? ` · tối đa ${row.original.gioi_han}` : '' }}
            </div>
          </template>
          <template #loi-cell="{ row }">
            <UBadge v-if="row.original.loi" color="error" variant="subtle" :label="row.original.loi" />
            <UBadge v-else color="success" variant="subtle" label="hợp lệ" />
          </template>
          <template #sua-cell="{ row }">
            <UButton v-if="laSieu" size="xs" color="neutral" variant="ghost" icon="i-lucide-pencil" aria-label="Sửa" @click="suaMon = { ...row.original }" />
          </template>
        </UTable>
      </UCard>
      <UButton v-if="laSieu" icon="i-lucide-plus" label="Thêm món" class="w-fit" @click="suaMon = { ma: '', ten: '', nhom: 'skin', trao: 'trang_tri', gia_luong: 100, han_ngay: 0, gioi_han: 1, nhom_xoay: 0, giao_dich_duoc: 0, co_chi_so: 0, dang_ban: 1, thu_tu: (mon.at(-1)?.thu_tu ?? 0) + 1, mo_ta: '', loi: null }" />
    </template>

    <!-- ============ NHẬT KÝ ============ -->
    <template v-else-if="tab === 'nhat_ky'">
      <UCard>
        <form class="flex flex-wrap items-end gap-3" @submit.prevent="taiNhatKy">
          <UFormField label="Mã đơn" hint="để trống = 200 dòng gần nhất">
            <UInput v-model="nkMaDon" class="w-60" placeholder="HT7F3AQ2BX" />
          </UFormField>
          <UButton type="submit" icon="i-lucide-search">
            Xem
          </UButton>
        </form>
      </UCard>
      <UCard v-if="nk?.soCai.length">
        <template #header>
          <b>Sổ cái của đơn</b>
        </template>
        <UTable :data="nk.soCai" />
      </UCard>
      <UCard>
        <template #header>
          <b>Nhật ký kiểm toán (nap_nhat_ky)</b>
        </template>
        <UTable :data="nk?.nhatKy ?? []" empty="Chưa có." />
      </UCard>
      <UCard>
        <template #header>
          <b>Webhook nhận được (nap_webhook)</b>
        </template>
        <UTable :data="nk?.webhook ?? []" empty="Chưa có." />
      </UCard>
    </template>

    <!-- ============ MỐC CŨ ============ -->
    <template v-else-if="tab === 'moc_cu'">
      <UAlert color="neutral" variant="subtle" icon="i-lucide-archive" title="Đơn nạp mốc cũ (tob_nap_moc) — chỉ xem" description="Hệ thống mới không bán mốc (mốc cũ tặng lượng + pet có chỉ số, trái nguyên tắc không bán sức mạnh). Quà mốc trong game bị chặn khi sv.lau_dai=1." />
      <UCard :ui="{ body: 'p-0 sm:p-0' }">
        <UTable :data="mocCu" empty="Không có đơn mốc cũ." />
      </UCard>
    </template>

    <!-- ============ HỘP THOẠI ============ -->
    <UModal :open="!!xn" title="Xác nhận đã nhận tiền" @update:open="(v: boolean) => { if (!v) xn = null }">
      <template #body>
        <div v-if="xn" class="space-y-4">
          <p class="text-sm text-muted">
            Chỉ xác nhận khi sao kê có đúng <strong class="text-highlighted">{{ vnd(xn.don.so_tien) }}</strong> với nội dung
            <strong class="font-mono text-highlighted">{{ xn.don.ma_don }}</strong>. Server game sẽ cộng {{ xn.don.ten_goi }} vào {{ xn.don.charname }}.
          </p>
          <UFormField label="Mã giao dịch trên sao kê" hint="bắt buộc, duy nhất">
            <UInput v-model="xn.maGd" class="w-full" />
          </UFormField>
          <UFormField label="Ghi chú">
            <UInput v-model="xn.ghiChu" class="w-full" />
          </UFormField>
        </div>
      </template>
      <template #footer>
        <div class="flex w-full justify-end gap-2">
          <UButton color="neutral" variant="ghost" @click="xn = null">
            Huỷ
          </UButton>
          <UButton icon="i-lucide-check" :loading="dangGui" :disabled="(xn?.maGd.trim().length ?? 0) < 3" @click="xacNhan">
            Xác nhận
          </UButton>
        </div>
      </template>
    </UModal>

    <UModal :open="!!ht" title="Hoàn tiền / chargeback" @update:open="(v: boolean) => { if (!v) ht = null }">
      <template #body>
        <div v-if="ht" class="space-y-4">
          <p class="text-sm text-muted">
            Đơn <span class="font-mono">{{ ht.don.ma_don }}</span> ({{ TEN_TRANG_THAI_NAP[ht.don.trang_thai] }}).
            <template v-if="ht.don.trang_thai === 'da_cong'">
              Đã vào game: server sẽ chuyển {{ ht.don.loai === 'luong' ? `${ht.don.luong} lượng` : ht.don.ten_goi }} thành <b>nợ</b>
              (trừ dần lượng đang có / lượng nạp sau; khoá mua cửa hàng tới khi hết nợ); thẻ tháng -30 ngày; vé mùa mất cao cấp.
            </template>
            <template v-else>
              Chưa vào game: chỉ đổi trạng thái.
            </template>
            Việc chuyển tiền trả lại làm ngoài hệ thống — ghi mã giao dịch hoàn vào lý do.
          </p>
          <UFormField label="Lý do (≥ 5 ký tự)">
            <UInput v-model="ht.lyDo" class="w-full" />
          </UFormField>
          <USwitch v-model="ht.chargeback" label="Chargeback / khiếu nại ngân hàng (khoá tạo đơn mới của tài khoản)" />
        </div>
      </template>
      <template #footer>
        <div class="flex w-full justify-end gap-2">
          <UButton color="neutral" variant="ghost" @click="ht = null">
            Huỷ
          </UButton>
          <UButton color="error" icon="i-lucide-undo-2" :loading="dangGui" :disabled="(ht?.lyDo.trim().length ?? 0) < 5" @click="hoanTien">
            Hoàn tiền
          </UButton>
        </div>
      </template>
    </UModal>

    <USlideover :open="!!ct" title="Cộng tay (đền bù / tiền vào không khớp)" @update:open="(v: boolean) => { if (!v) ct = null }">
      <template #body>
        <div v-if="ct" class="space-y-3">
          <UFormField label="Tài khoản">
            <UInput v-model="ct.username" class="w-full" />
          </UFormField>
          <UFormField label="ID Pokémon (tob_char.id)">
            <UInputNumber v-model="ct.charId" :min="1" class="w-full" />
          </UFormField>
          <UFormField label="Gói">
            <USelect v-model="ct.maGoi" :items="goi.map(g => ({ label: `${g.ten} — ${vnd(g.gia_vnd)}`, value: g.ma }))" class="w-full" />
          </UFormField>
          <UFormField label="Mã giao dịch / mã phiếu" hint="chống cộng trùng">
            <UInput v-model="ct.maGd" class="w-full" />
          </UFormField>
          <UFormField label="Lý do">
            <UInput v-model="ct.lyDo" class="w-full" />
          </UFormField>
          <p class="text-xs text-muted">
            Tạo đơn nguồn "admin" + xác nhận qua cùng hàm, vào cùng sổ cái và nhật ký. Không vượt trần (trần chỉ áp dụng cho đơn của người chơi).
          </p>
        </div>
      </template>
      <template #footer>
        <UButton icon="i-lucide-hand-coins" :loading="dangGui" label="Cộng" @click="congTay" />
      </template>
    </USlideover>

    <USlideover :open="!!suaGoi" :title="suaGoi?.ma ? `Gói ${suaGoi.ma}` : 'Gói mới'" @update:open="(v: boolean) => { if (!v) suaGoi = null }">
      <template #body>
        <div v-if="suaGoi" class="space-y-3">
          <UFormField label="Mã (A-Z 0-9 _)">
            <UInput v-model="suaGoi.ma" class="w-full" />
          </UFormField>
          <UFormField label="Tên">
            <UInput v-model="suaGoi.ten" class="w-full" />
          </UFormField>
          <UFormField label="Loại">
            <USelect v-model="suaGoi.loai" :items="Object.entries(TEN_LOAI_GOI).map(([value, label]) => ({ label, value }))" class="w-full" />
          </UFormField>
          <UFormField label="Giá (đ, chẵn nghìn, 10.000–2.000.000)">
            <UInputNumber v-model="suaGoi.gia_vnd" :min="10000" :max="2000000" :step="1000" class="w-full" />
          </UFormField>
          <p class="text-sm">
            Lượng: <b>{{ suaGoi.loai === 'luong' ? so(luongDuKien) : 0 }}</b> <span class="text-muted">(tính tự động theo tỷ giá)</span>
          </p>
          <UFormField label="Mô tả">
            <UTextarea v-model="suaGoi.mo_ta" :rows="3" class="w-full" />
          </UFormField>
          <UFormField label="Thứ tự">
            <UInputNumber v-model="suaGoi.thu_tu" class="w-full" />
          </UFormField>
          <USwitch :model-value="!!Number(suaGoi.dang_ban)" label="Đang bán" @update:model-value="(v: boolean) => { if (suaGoi) suaGoi.dang_ban = v ? 1 : 0 }" />
        </div>
      </template>
      <template #footer>
        <UButton icon="i-lucide-save" :loading="dangGui" label="Lưu" @click="luuGoi" />
      </template>
    </USlideover>

    <USlideover :open="!!suaMon" :title="suaMon?.ma ? `Món ${suaMon.ma}` : 'Món mới'" @update:open="(v: boolean) => { if (!v) suaMon = null }">
      <template #body>
        <div v-if="suaMon" class="space-y-3">
          <UFormField label="Mã (a-z 0-9 _)">
            <UInput v-model="suaMon.ma" class="w-full" />
          </UFormField>
          <UFormField label="Tên">
            <UInput v-model="suaMon.ten" class="w-full" />
          </UFormField>
          <div class="grid grid-cols-2 gap-3">
            <UFormField label="Nhóm">
              <USelect v-model="suaMon.nhom" :items="nhomMon" class="w-full" />
            </UFormField>
            <UFormField label="Cách trao">
              <USelect v-model="suaMon.trao" :items="traoMon" class="w-full" />
            </UFormField>
            <UFormField label="Giá (lượng)">
              <UInputNumber v-model="suaMon.gia_luong" :min="10" :max="5000" class="w-full" />
            </UFormField>
            <UFormField label="Hạn (ngày, 0 = vĩnh viễn)">
              <UInputNumber v-model="suaMon.han_ngay" :min="0" class="w-full" />
            </UFormField>
            <UFormField label="Tối đa / Pokémon (0 = không)">
              <UInputNumber v-model="suaMon.gioi_han" :min="0" class="w-full" />
            </UFormField>
            <UFormField label="Nhóm xoay (0–4)">
              <UInputNumber v-model="suaMon.nhom_xoay" :min="0" :max="4" class="w-full" />
            </UFormField>
          </div>
          <USwitch :model-value="!!Number(suaMon.giao_dich_duoc)" label="Giao dịch được trên Chợ trang trí (chỉ trang trí vĩnh viễn)" @update:model-value="(v: boolean) => { if (suaMon) suaMon.giao_dich_duoc = v ? 1 : 0 }" />
          <USwitch :model-value="!!Number(suaMon.dang_ban)" label="Đang bán" @update:model-value="(v: boolean) => { if (suaMon) suaMon.dang_ban = v ? 1 : 0 }" />
          <UFormField label="Mô tả">
            <UInput v-model="suaMon.mo_ta" class="w-full" />
          </UFormField>
        </div>
      </template>
      <template #footer>
        <UButton icon="i-lucide-save" :loading="dangGui" label="Lưu" @click="luuMon" />
      </template>
    </USlideover>
  </AdminPage>
</template>
