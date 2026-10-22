<script setup lang="ts">
/**
 * CẤU HÌNH HỆ THỐNG — mọi công tắc & tham số máy chủ ở MỘT chỗ (docs/cau-hinh.md).
 * Form sinh từ danh mục (CSDL cau_hinh_danh_muc + cau_hinh_nhom do API trả về; dự phòng shared/utils/cauHinhTrungTam.gen.ts), kiểm ngay khi gõ,
 * xem trước thay đổi trước khi lưu, badge "áp dụng ngay" / "cần khởi động lại", nút khởi động lại an toàn
 * (đếm ngược + lý do), lịch sử + hoàn tác, tìm kiếm. Máy chủ game xác nhận phiên bản đã áp dụng (cau_hinh_ap_dung).
 * Siêu quản trị sửa; Chỉ xem đọc. Tự tải lại mỗi 10 giây (trừ khi đang sửa).
 *
 * TRANG CẤU HÌNH DUY NHẤT (trang cũ /admin/cauhinh đã gộp vào đây). Chỉ hiện khoá NGƯỜI VẬN HÀNH CHỈNH (6 nhóm);
 * khoá cố định / luôn bật (trường `lop` của danh mục) ẩn — máy thật luôn chạy giá trị sản xuất (docs/cau-hinh.md).
 * Máy thử (dev / HTBB_CHO_PHEP_THU_NGHIEM=1) có nút "Hiện khoá cố định".
 */
import AdminTaikhoanXacNhan from '~/components/admin/taikhoan/XacNhan.vue'
import type { BanTai } from '#shared/utils/taiGame'

definePageMeta({ layout: 'admin', middleware: 'admin' })
const api = useAdminApi()
const toast = useToast()
const ch = useCauHinhTT()
const { data, status, error, refresh } = ch

const tab = ref<'cau_hinh' | 'lich_su'>('cau_hinh')
const tim = ref('')
const nhomChon = ref<string>('tat_ca')
const chiDaSua = ref(false)
const hienAn = ref(false)
const choPhepHienAn = computed(() => !!data.value?.quyen.choPhepThuNghiem)
const xemAn = computed(() => hienAn.value && choPhepHienAn.value)
const ds = computed(() => {
  void ch.phienDanhMuc.value
  let k = timTT(tim.value)
  if (nhomChon.value !== 'tat_ca') k = k.filter(x => x.nhom === nhomChon.value)
  if (chiDaSua.value) k = k.filter(x => Object.prototype.hasOwnProperty.call(ch.sua.value, x.khoa) || (data.value?.giaTri[x.khoa] ?? null) !== null)
  return nhomTT(k, xemAn.value)
})
const mucNhom = computed(() => {
  void ch.phienDanhMuc.value
  return [{ label: 'Tất cả nhóm', value: 'tat_ca' },
    ...dsNhomTT().filter(n => xemAn.value || !n.an).map(n => ({ label: n.ten, value: n.ma }))]
})
const demLop = computed(() => {
  void ch.phienDanhMuc.value
  return demLopTT()
})
const ghiDuoc = computed(() => !!data.value?.quyen.ghi)

// ---- bản đang phát từng nền tảng (web_cai_dat 'taiGame') - giải thích "Bản mới nhất / Bản tối thiểu" ----
// Số bản (BAN) không còn trả ở /api/cong-khai: đọc qua API quản trị.
const { data: cmsCk } = useAsyncData('admin-cau-hinh-tai-game', () => api<{ giaTri?: { taiGame?: { ban?: BanTai[] } } }>('/api/admin/cms/cai-dat'), { server: false })
const banTai = computed(() => (cmsCk.value?.giaTri?.taiGame?.ban ?? []).filter(b => b.ma !== 'ios' && b.hien))

useIntervalFn(() => {
  if (document.visibilityState === 'visible' && ch.soThayDoi.value === 0 && !moXemTruoc.value) refresh()
}, 10_000)

// ---- xem trước + lưu ----
const moXemTruoc = ref(false)
const lyDo = ref('')
const coLoiNhap = computed(() => Object.keys(ch.loiNhap.value).length > 0 || Object.keys(ch.soSanh.value.loi).length > 0)
const canKdl = computed(() => ch.soSanh.value.thayDoi.filter(d => d.apDung === 'khoi_dong_lai'))
async function luu() {
  if (await ch.luuThayDoi(lyDo.value.trim())) {
    moXemTruoc.value = false
    lyDo.value = ''
  }
}

// ---- khởi động lại an toàn ----
const moKdl = ref(false)
const kdlGiay = ref(60)
const kdlLyDo = ref('')
const kdl = computed(() => data.value?.khoiDongLai ?? null)
const kdlDangChay = computed(() => ['cho', 'dang_dem', 'huy', 'dang_thoat'].includes(kdl.value?.trang_thai ?? ''))
const bayGio = ref(Date.now())
useIntervalFn(() => { bayGio.value = Date.now() }, 1000)
const conGiay = computed(() => {
  const k = kdl.value
  if (!k || k.trang_thai !== 'dang_dem' || !k.bat_dau_luc) return null
  const t = new Date(k.bat_dau_luc.replace(' ', 'T')).getTime() + Number(k.giay) * 1000
  return Math.max(0, Math.round((t - bayGio.value) / 1000))
})
useIntervalFn(() => { if (kdlDangChay.value && document.visibilityState === 'visible') refresh() }, 3000)
const loiKdl = computed(() => {
  const r = kiemYeuCauKhoiDongLai({ giay: Number(kdlGiay.value), lyDo: kdlLyDo.value })
  return r.ok ? '' : r.loi
})
const dangGuiKdl = ref(false)
async function guiKdl() {
  dangGuiKdl.value = true
  try {
    const r = await api<{ message: string }>('/api/admin/cau-hinh-he-thong/khoi-dong-lai', { method: 'POST', body: { giay: Number(kdlGiay.value), lyDo: kdlLyDo.value.trim() } })
    toast.add({ title: r.message, color: 'success', icon: 'i-lucide-rotate-ccw' })
    moKdl.value = false
    kdlLyDo.value = ''
    await refresh()
  } catch (e) {
    toast.add({ title: 'Chưa gửi được', description: loiApi(e), color: 'error' })
  } finally {
    dangGuiKdl.value = false
  }
}
async function huyKdl() {
  try {
    const r = await api<{ message: string }>('/api/admin/cau-hinh-he-thong/huy-khoi-dong-lai', { method: 'POST' })
    toast.add({ title: r.message, color: 'success' })
    await refresh()
  } catch (e) {
    toast.add({ title: 'Không huỷ được', description: loiApi(e), color: 'error' })
  }
}

// ---- tổng hợp máy chủ ----
const dsCho = computed(() => Object.entries(data.value?.hieuLuc ?? {}).filter(([, h]) => h.cho !== null).map(([k, h]) => ({ k: khoaTT(k), khoa: k, h })))
const dsTuChoi = computed(() => Object.entries(data.value?.hieuLuc ?? {}).filter(([, h]) => h.loi).map(([k, h]) => ({ k: khoaTT(k), khoa: k, h })))

// ---- lịch sử ----
interface DongLs { id: number, khoa: string, bang: string, gia_tri_cu: string | null, gia_tri_moi: string | null, nguoi_sua: string | null, nguon: string, ly_do: string, phien_ban: number, luc: string }
const lsKhoa = ref('')
const lsTrang = ref(1)
const { data: ls, status: lsStatus, refresh: lsRefresh } = useAsyncData('admin-cau-hinh-tt-ls',
  () => api<{ dong: DongLs[], tong: number, trang: number, soDong: number }>('/api/admin/cau-hinh-he-thong/lich-su', { query: { khoa: lsKhoa.value || undefined, trang: lsTrang.value, soDong: 50 } }),
  { server: false, watch: [lsKhoa, lsTrang], immediate: false })
watch(tab, (t) => { if (t === 'lich_su') lsRefresh() })
const nguonLs: Record<string, string> = { web: 'Web', 'web:hoan_tac': 'Hoàn tác', phat_hien: 'Ghi ngoài trang', game: 'Trong game', swing: 'Bảng Swing' }
const xacNhan = useOverlay().create(AdminTaikhoanXacNhan)
async function hoanTac(d: DongLs) {
  const k = khoaTT(d.khoa)
  const ok = await xacNhan.open({
    title: `Hoàn tác ${k?.ten ?? d.khoa}?`,
    description: `Ghi lại giá trị cũ: ${hienThiTT(k, d.gia_tri_cu)} (hiện tại theo dòng #${d.id}: ${hienThiTT(k, d.gia_tri_moi)}).${k?.apDung === 'khoi_dong_lai' ? '\nKhoá này chỉ có hiệu lực sau khi khởi động lại.' : ''}`,
    nut: 'Hoàn tác', mau: 'warning',
  }).result
  if (!ok) return
  try {
    const r = await api<{ message: string }>('/api/admin/cau-hinh-he-thong/hoan-tac', { method: 'POST', body: { id: d.id } })
    toast.add({ title: r.message, color: 'success' })
    await Promise.all([refresh(), lsRefresh()])
  } catch (e) {
    toast.add({ title: 'Không hoàn tác được', description: loiApi(e), color: 'error' })
  }
}
</script>

<template>
  <AdminPage title="Cấu hình hệ thống">
    <template #right>
      <UButton v-if="ghiDuoc" icon="i-lucide-rotate-ccw" color="warning" variant="soft" label="Khởi động lại an toàn" :disabled="kdlDangChay" @click="moKdl = true" />
      <UButton icon="i-lucide-refresh-cw" color="neutral" variant="ghost" :loading="status === 'pending'" aria-label="Tải lại" @click="refresh()" />
    </template>

    <div>
      <h1 class="text-2xl font-bold text-highlighted">Cấu hình hệ thống</h1>
      <p class="mt-1 text-sm text-muted">
        Một nơi cho mọi công tắc và tham số. Máy chủ đọc theo thứ tự <b>CSDL (trang này) → server.ini → mặc định</b>, kiểm từng giá trị
        (sai thì bị từ chối, giữ giá trị tốt gần nhất) và áp dụng trong ≤ 10 giây — trừ khoá <UBadge color="warning" variant="subtle" size="sm" label="Cần khởi động lại" />.
        Bí mật cổng thanh toán, mật khẩu CSDL, cổng mạng vẫn nằm trong biến môi trường / server.ini.
      </p>
      <p class="mt-1 text-sm text-muted">
        Chỉ hiện <b>{{ demLop.chinh }}</b> khoá cần chỉnh. <b>{{ demLop.co_dinh + demLop.luon_bat }}</b> khoá khác
        ({{ demLop.luon_bat }} tính năng luôn bật, {{ demLop.co_dinh }} tham số cố định: chống DoS, nhật ký, công thức kinh tế, luật an toàn…)
        đã đặt sẵn giá trị sản xuất và ẩn đi — đổi khẩn cấp qua server.ini (xem docs/cau-hinh.md).
      </p>
    </div>

    <UAlert v-if="error" color="error" variant="subtle" icon="i-lucide-circle-x" title="Không tải được cấu hình" :description="loiApi(error)" />
    <UAlert v-else-if="data?.loiDoc" color="error" variant="subtle" icon="i-lucide-database" title="Lỗi đọc CSDL" :description="data.loiDoc" />

    <!-- Trạng thái áp dụng -->
    <div v-if="data" class="grid gap-3 lg:grid-cols-3">
      <UCard class="lg:col-span-2">
        <div class="flex flex-wrap items-center gap-2">
          <UBadge :color="ch.trangThaiApDung.value.mau" variant="subtle" size="lg" :icon="ch.trangThaiApDung.value.mau === 'success' ? 'i-lucide-check-circle' : ch.trangThaiApDung.value.mau === 'warning' ? 'i-lucide-loader' : 'i-lucide-alert-triangle'"
            :label="ch.trangThaiApDung.value.chu" />
        </div>
        <dl class="mt-3 grid gap-x-6 gap-y-1 text-sm sm:grid-cols-2">
          <div><dt class="inline text-muted">Phiên bản CSDL: </dt><dd class="inline font-medium">{{ data.phienBan.phien_ban }}</dd>
            <span v-if="data.phienBan.sua_boi" class="text-xs text-dimmed"> · {{ data.phienBan.sua_boi }} · {{ data.phienBan.sua_luc }}</span></div>
          <template v-if="ch.mayChinh.value">
            <div><dt class="inline text-muted">Máy chủ đối chiếu lần cuối: </dt><dd class="inline">{{ ch.mayChinh.value.ap_dung_luc }}</dd></div>
            <div><dt class="inline text-muted">Khởi động lúc: </dt><dd class="inline">{{ ch.mayChinh.value.khoi_dong_luc }}</dd></div>
            <div><dt class="inline text-muted">Nhịp gần nhất: </dt><dd class="inline">{{ data.giayTuNhip ?? '?' }} giây trước</dd></div>
            <div><dt class="inline text-muted">Khoá lấy từ CSDL: </dt><dd class="inline">{{ ch.mayChinh.value.so_khoa }}</dd></div>
            <div><dt class="inline text-muted">Giá trị bị từ chối: </dt><dd class="inline" :class="ch.mayChinh.value.so_loi ? 'text-error' : ''">{{ ch.mayChinh.value.so_loi }}</dd></div>
          </template>
        </dl>
        <p v-if="ch.mayChinh.value?.thay_doi_cuoi" class="mt-2 truncate text-xs text-dimmed" :title="ch.mayChinh.value.thay_doi_cuoi">Lần áp dụng cuối: {{ ch.mayChinh.value.thay_doi_cuoi }}</p>
        <p v-if="data.mayChu.length > 1" class="mt-2 text-xs text-warning">Có {{ data.mayChu.length }} máy chủ ghi xác nhận trên CSDL này: {{ data.mayChu.map(m => `${m.may_chu} (v${m.phien_ban}, nhịp ${m.nhip_luc})`).join(' · ') }}</p>
      </UCard>

      <UCard>
        <h2 class="mb-2 font-semibold text-highlighted">Khởi động lại</h2>
        <template v-if="kdl && kdl.trang_thai">
          <UBadge :color="kdlDangChay ? 'warning' : kdl.trang_thai === 'da_xong' ? 'success' : 'neutral'" variant="subtle" :label="TRANG_THAI_KDL[kdl.trang_thai] ?? kdl.trang_thai" />
          <p v-if="conGiay !== null" class="mt-2 text-3xl font-bold tabular-nums text-warning">{{ conGiay }} giây</p>
          <p class="mt-1 text-sm text-muted">{{ kdl.ly_do }}</p>
          <p class="text-xs text-dimmed">{{ kdl.yeu_cau_boi }} · {{ kdl.yeu_cau_luc }}<span v-if="kdl.xong_luc"> → {{ kdl.xong_luc }}</span></p>
          <UButton v-if="ghiDuoc && ['cho', 'dang_dem'].includes(kdl.trang_thai)" class="mt-2" size="sm" color="neutral" variant="outline" icon="i-lucide-x" label="Huỷ khởi động lại" @click="huyKdl" />
        </template>
        <p v-else class="text-sm text-muted">Không có yêu cầu nào.</p>
        <div v-if="dsCho.length" class="mt-3 border-t border-default pt-2">
          <p class="text-sm font-medium text-warning">{{ dsCho.length }} khoá chờ khởi động lại:</p>
          <ul class="mt-1 space-y-0.5 text-xs">
            <li v-for="c in dsCho" :key="c.khoa"><span class="font-mono">{{ c.khoa }}</span>: {{ hienThiTT(c.k, c.h.giaTri) }} → <b>{{ hienThiTT(c.k, c.h.cho) }}</b></li>
          </ul>
        </div>
      </UCard>
    </div>

    <UAlert v-if="dsTuChoi.length" color="error" variant="subtle" icon="i-lucide-shield-alert" :title="`${dsTuChoi.length} giá trị bị máy chủ từ chối / cảnh báo`">
      <template #description>
        <ul class="space-y-0.5">
          <li v-for="t in dsTuChoi" :key="t.khoa"><span class="font-mono">{{ t.khoa }}</span>: {{ t.h.loi }}</li>
        </ul>
      </template>
    </UAlert>

    <UAlert v-if="data && (data.nap.yeuCauBat && !data.nap.bat || !data.nap.daCoGiayPhep)" color="warning" variant="subtle" icon="i-lucide-banknote"
      title="Thu tiền đang KHOÁ ở tầng biến môi trường (fail closed)"
      :description="[!data.nap.daCoGiayPhep ? 'Chưa xác nhận giấy phép (NAP_DA_CO_GIAY_PHEP).' : '', ...data.nap.lyDoTat].filter(Boolean).join(' · ') || 'HTBB_NAP_BAT đang tắt.'" />

    <UTabs v-model="tab" :items="[{ label: 'Cấu hình', value: 'cau_hinh', icon: 'i-lucide-sliders-horizontal' }, { label: 'Lịch sử thay đổi', value: 'lich_su', icon: 'i-lucide-history' }]" :content="false" />

    <template v-if="tab === 'cau_hinh'">
      <div class="flex flex-wrap items-center gap-2">
        <UInput v-model="tim" icon="i-lucide-search" placeholder="Tìm khoá, tên, mô tả… (không cần dấu)" class="min-w-64 flex-1" />
        <USelect v-model="nhomChon" :items="mucNhom" class="w-56" />
        <UCheckbox v-model="chiDaSua" label="Chỉ khoá đã đặt / đang sửa" />
        <UCheckbox v-if="choPhepHienAn" v-model="hienAn" label="Hiện khoá cố định (máy thử)" />
      </div>
      <p v-if="!ghiDuoc && data" class="text-sm text-muted">Bạn đang ở chế độ <b>chỉ xem</b> — chỉ siêu quản trị sửa được.</p>

      <div v-if="data" class="space-y-4">
        <UCard v-for="n in ds" :key="n.ma">
          <template #header>
            <div>
              <h2 class="font-semibold text-highlighted">{{ n.ten }} <span class="text-sm font-normal text-dimmed">({{ n.khoa.length }})</span></h2>
              <p class="text-xs text-muted">{{ n.moTa }}</p>
              <p v-if="n.ma === 'su_kien'" class="mt-1 text-xs"><NuxtLink to="/admin/sukien" class="text-primary hover:underline">Bật/tắt từng sự kiện →</NuxtLink></p>
              <p v-if="n.ma === 'nap_tien'" class="mt-1 text-xs"><NuxtLink to="/admin/naptien" class="text-primary hover:underline">Đơn, gói, hoàn tiền →</NuxtLink></p>
              <div v-if="n.ma === 'bao_tri'" class="mt-2 rounded-md border border-default bg-elevated/40 p-2 text-xs text-muted">
                <p><b class="text-highlighted">Cập nhật client — hai mức:</b> <b>Bản mới nhất</b> = “Số bản” từng nền tảng ở trang
                  <NuxtLink to="/admin/tai-game" class="text-primary hover:underline">Bản tải game</NuxtLink>
                  (client cũ hơn thấy “Đã có bản mới — Cập nhật ngay / Để sau”, nhắc 1 lần/ngày, vẫn chơi được).
                  <b>Bản tối thiểu</b> = ô “Bản tối thiểu (bắt buộc cập nhật)” dưới đây (cũ hơn thì không vào được game, hiện nút Tải bản mới).</p>
                <p v-if="banTai.length" class="mt-1">Đang phát: <span v-for="(b, i) in banTai" :key="b.ma">{{ i ? ' · ' : '' }}{{ b.ten }} <b>{{ b.ban || '—' }}</b><template v-if="b.phienBan"> ({{ b.phienBan }})</template></span></p>
              </div>
            </div>
          </template>
          <AdminHethongCauHinhKhoa v-for="k in n.khoa" :key="k.khoa" :k="k" :ch="ch" />
        </UCard>
        <p v-if="!ds.length" class="text-center text-sm text-muted">Không có khoá nào khớp.</p>
      </div>
      <USkeleton v-else class="h-64 w-full" />

      <!-- Thanh lưu dính đáy -->
      <div v-if="ch.soThayDoi.value || coLoiNhap" class="sticky bottom-3 z-10 flex flex-wrap items-center gap-2 rounded-lg border border-primary/40 bg-default/95 p-3 shadow-lg backdrop-blur">
        <UIcon name="i-lucide-pencil" class="text-primary" />
        <span class="text-sm"><b>{{ ch.soThayDoi.value }}</b> thay đổi chưa lưu<span v-if="canKdl.length">, {{ canKdl.length }} cần khởi động lại</span></span>
        <span v-if="coLoiNhap" class="text-sm text-error">· còn ô sai</span>
        <div class="ms-auto flex gap-2">
          <UButton label="Bỏ hết" color="neutral" variant="ghost" @click="ch.boSua()" />
          <UButton label="Xem trước & lưu" icon="i-lucide-eye" :disabled="coLoiNhap || !ch.soThayDoi.value" @click="moXemTruoc = true" />
        </div>
      </div>
    </template>

    <template v-else>
      <div class="flex flex-wrap items-center gap-2">
        <UInput v-model="lsKhoa" icon="i-lucide-filter" placeholder="Lọc theo khoá chính xác, vd sv.tileXu" class="w-80" @update:model-value="lsTrang = 1" />
        <UButton icon="i-lucide-refresh-cw" color="neutral" variant="ghost" :loading="lsStatus === 'pending'" @click="lsRefresh()" />
        <span v-if="ls" class="text-sm text-muted">{{ ls.tong }} dòng</span>
      </div>
      <UCard :ui="{ body: 'p-0 sm:p-0' }">
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead class="bg-elevated/50 text-left text-xs text-muted">
              <tr><th class="p-2">#</th><th class="p-2">Lúc</th><th class="p-2">Khoá</th><th class="p-2">Cũ → mới</th><th class="p-2">Ai / nguồn</th><th class="p-2">Lý do</th><th class="p-2" /></tr>
            </thead>
            <tbody>
              <tr v-for="d in ls?.dong ?? []" :key="d.id" class="border-t border-default align-top">
                <td class="p-2 text-dimmed">{{ d.id }}<br><span class="text-xs">v{{ d.phien_ban }}</span></td>
                <td class="whitespace-nowrap p-2">{{ d.luc }}</td>
                <td class="p-2"><span class="font-medium">{{ khoaTT(d.khoa)?.ten ?? d.khoa }}</span><br><button type="button" class="font-mono text-xs text-dimmed hover:text-primary" @click="lsKhoa = d.khoa">{{ d.khoa }}</button></td>
                <td class="p-2">{{ hienThiTT(khoaTT(d.khoa), d.gia_tri_cu) }} → <b>{{ hienThiTT(khoaTT(d.khoa), d.gia_tri_moi) }}</b></td>
                <td class="p-2">{{ d.nguoi_sua }}<br><span class="text-xs text-dimmed">{{ nguonLs[d.nguon] ?? d.nguon }}</span></td>
                <td class="max-w-72 p-2 text-muted">{{ d.ly_do }}</td>
                <td class="p-2"><UButton v-if="ghiDuoc && khoaTT(d.khoa) && !khoaTT(d.khoa)?.nhay && khoaTT(d.khoa)?.bang !== 'env'" size="xs" color="neutral" variant="outline" icon="i-lucide-undo-2" label="Hoàn tác" @click="hoanTac(d)" /></td>
              </tr>
              <tr v-if="ls && !ls.dong.length"><td colspan="7" class="p-6 text-center text-muted">Chưa có thay đổi nào.</td></tr>
            </tbody>
          </table>
        </div>
      </UCard>
      <div v-if="ls && ls.tong > ls.soDong" class="flex justify-center">
        <UPagination v-model:page="lsTrang" :total="ls.tong" :items-per-page="ls.soDong" />
      </div>
    </template>

    <!-- Xem trước thay đổi -->
    <UModal v-model:open="moXemTruoc" title="Xem trước thay đổi" :ui="{ content: 'sm:max-w-3xl' }">
      <template #body>
        <div class="space-y-3">
          <table class="w-full text-sm">
            <thead class="text-left text-xs text-muted"><tr><th class="py-1">Khoá</th><th class="py-1">Đang lưu</th><th class="py-1">Mới</th><th class="py-1">Hiệu lực</th></tr></thead>
            <tbody>
              <tr v-for="d in ch.soSanh.value.thayDoi" :key="d.khoa" class="border-t border-default">
                <td class="py-1.5"><span class="font-medium">{{ d.ten }}</span><br><span class="font-mono text-xs text-dimmed">{{ d.khoa }}</span></td>
                <td class="py-1.5 text-muted line-through decoration-error/60">{{ hienThiTT(khoaTT(d.khoa), d.cu) }}</td>
                <td class="py-1.5 font-medium">{{ d.moi === null ? '(xoá dòng → server.ini / mặc định)' : hienThiTT(khoaTT(d.khoa), d.moi) }}</td>
                <td class="py-1.5"><UBadge v-if="d.apDung === 'ngay'" color="success" variant="subtle" size="sm" label="≤ 10 giây" /><UBadge v-else color="warning" variant="subtle" size="sm" label="Khi khởi động lại" /></td>
              </tr>
            </tbody>
          </table>
          <UAlert v-if="canKdl.length" color="warning" variant="subtle" icon="i-lucide-rotate-ccw" title="Có khoá chỉ đổi khi khởi động lại"
            description="Sau khi lưu, trang hiện chúng ở mục 'chờ khởi động lại'. Dùng nút Khởi động lại an toàn khi sẵn sàng." />
          <UFormField label="Lý do thay đổi" required help="Lưu vào lịch sử cùng tên bạn.">
            <UInput v-model="lyDo" class="w-full" placeholder="VD: tăng rơi xu cuối tuần" autofocus @keydown.enter="lyDo.trim().length >= 3 && luu()" />
          </UFormField>
        </div>
      </template>
      <template #footer>
        <div class="flex w-full justify-end gap-2">
          <UButton label="Thôi" color="neutral" variant="ghost" @click="moXemTruoc = false" />
          <UButton :label="`Lưu ${ch.soThayDoi.value} thay đổi`" icon="i-lucide-save" :loading="ch.dangLuu.value" :disabled="lyDo.trim().length < 3" @click="luu" />
        </div>
      </template>
    </UModal>

    <!-- Khởi động lại an toàn -->
    <UModal v-model:open="moKdl" title="Khởi động lại an toàn">
      <template #body>
        <div class="space-y-3">
          <p class="text-sm text-muted">Máy chủ báo người chơi, đếm ngược, <b>lưu toàn bộ dữ liệu</b> rồi thoát; systemd (Restart=always) tự bật lại sau ~10–15 giây. Huỷ được trong lúc đếm.</p>
          <UFormField label="Đếm ngược (giây)" :help="`${KDL_GIAY_MIN}–${KDL_GIAY_MAX}`">
            <UInput v-model.number="kdlGiay" type="number" :min="KDL_GIAY_MIN" :max="KDL_GIAY_MAX" class="w-40" />
          </UFormField>
          <UFormField label="Lý do (người chơi thấy)" required>
            <UInput v-model="kdlLyDo" class="w-full" placeholder="VD: áp dụng chế độ dài hạn" />
          </UFormField>
          <p v-if="loiKdl && kdlLyDo" class="text-xs text-error">{{ loiKdl }}</p>
        </div>
      </template>
      <template #footer>
        <div class="flex w-full justify-end gap-2">
          <UButton label="Thôi" color="neutral" variant="ghost" @click="moKdl = false" />
          <UButton label="Gửi yêu cầu khởi động lại" color="warning" icon="i-lucide-rotate-ccw" :loading="dangGuiKdl" :disabled="!!loiKdl" @click="guiKdl" />
        </div>
      </template>
    </UModal>
  </AdminPage>
</template>
