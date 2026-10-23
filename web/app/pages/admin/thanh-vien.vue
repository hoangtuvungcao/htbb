<script setup lang="ts">
/**
 * Thành viên HTBB (khu quản trị) — docs/van-hanh.md. Bắt buộc / bán (hai công tắc riêng),
 * giá, thời hạn, tính năng bị khoá, miễn cấp, thông báo; cấp / gia hạn / thu hồi (1 hoặc nhiều tài khoản);
 * danh sách + thống kê; nhật ký; tín hiệu nhiều tài khoản theo mã máy. Ghi: chỉ Siêu quản trị (quyenAdmin.ts).
 */
definePageMeta({ layout: 'admin', middleware: 'admin' })

interface CauHinh { bat: boolean, banBat: boolean, gia: number, thoiHanNgay: number, chucNang: string[], mienCap: number, thongBao: string, khChanDangNhap: boolean, khChucNang: string[], khThongBao: string, khTvLaKh: boolean }
interface TongQuan {
  coBang: boolean
  cauHinh: CauHinh
  ban: { duoc: boolean, lyDo: string[] }
  chucNang: { ma: string, ten: string, cho: string }[]
  thongKe: { conHan: number, vinhVien: number, sapHet: number, daThuHoi: number, soDon: number, doanhThu: number } | null
  daCoGiayPhep: boolean
  demo?: boolean
  demoChiAdmin?: boolean
}

const api = useAdminApi()
const toast = useToast()
const me = useAdminMe()
const laSieu = computed(() => me.vaiTro.value === 'sieu_quan_tri')

const tab = ref('cau_hinh')
const tabs = [
  { label: 'Cấu hình', value: 'cau_hinh', icon: 'i-lucide-sliders-horizontal' },
  { label: 'Thành viên', value: 'ds', icon: 'i-lucide-users' },
  { label: 'Cấp / thu hồi', value: 'cap', icon: 'i-lucide-badge-plus' },
  { label: 'Nhiều tài khoản', value: 'tin_hieu', icon: 'i-lucide-radar' },
  { label: 'Nhật ký', value: 'nhat_ky', icon: 'i-lucide-scroll-text' },
]

const tq = ref<TongQuan | null>(null)
const dangGui = ref(false)

async function taiTq() {
  try {
    tq.value = await api<TongQuan>('/api/admin/thanh-vien/tong-quan')
  } catch (e) {
    toast.add({ title: loiApi(e), color: 'error' })
  }
}

async function gui(url: string, body: Record<string, unknown>, xong?: () => void) {
  dangGui.value = true
  try {
    const r = await api<{ message?: string }>(url, { method: 'POST', body })
    toast.add({ title: r.message ?? 'Đã lưu', color: 'success', icon: 'i-lucide-check' })
    xong?.()
    await taiTq()
  } catch (e) {
    toast.add({ title: loiApi(e), color: 'error', icon: 'i-lucide-circle-alert' })
  } finally {
    dangGui.value = false
  }
}

// --- danh sách ---
const tim = ref('')
const page = ref(1)
const ds = ref<{ tong: number, ds: any[] } | null>(null)
async function taiDs() {
  try {
    ds.value = await api('/api/admin/thanh-vien/danh-sach', { query: { q: tim.value.trim(), page: page.value } })
  } catch (e) {
    toast.add({ title: loiApi(e), color: 'error' })
  }
}
watch(page, taiDs)
function hanText(ms: number): string {
  if (ms === 0) return 'vĩnh viễn'
  if (ms < 0) return 'đã thu hồi'
  return new Date(ms).toLocaleString('vi-VN')
}

// --- cấp / thu hồi ---
const cap = ref({ taiKhoan: '', ngay: 30, lyDo: '' })
const thu = ref({ taiKhoan: '', lyDo: '' })
const capTay = () => gui('/api/admin/thanh-vien/cap', { tai_khoan: cap.value.taiKhoan, ngay: cap.value.ngay, ly_do: cap.value.lyDo }, () => { cap.value.taiKhoan = '' })
const thuHoi = () => gui('/api/admin/thanh-vien/thu-hoi', { tai_khoan: thu.value.taiKhoan, ly_do: thu.value.lyDo }, () => { thu.value.taiKhoan = '' })

// --- tín hiệu ---
const nguong = ref(3)
const tinHieu = ref<{ coBang: boolean, may: any[] } | null>(null)
const lkTen = ref('')
const lienKet = ref<{ coBang: boolean, may: any[], ip: any[] } | null>(null)
async function taiTinHieu() {
  try {
    tinHieu.value = await api('/api/admin/thanh-vien/tin-hieu', { query: { nguong: nguong.value } })
  } catch (e) {
    toast.add({ title: loiApi(e), color: 'error' })
  }
}
async function taiLienKet() {
  try {
    lienKet.value = (await api<{ lienKet: any }>('/api/admin/thanh-vien/tin-hieu', { query: { tai_khoan: lkTen.value.trim() } })).lienKet
  } catch (e) {
    toast.add({ title: loiApi(e), color: 'error' })
  }
}

// --- nhật ký ---
const nkTen = ref('')
const nk = ref<any[]>([])
async function taiNk() {
  try {
    nk.value = (await api<{ ds: any[] }>('/api/admin/thanh-vien/nhat-ky', { query: { tai_khoan: nkTen.value.trim() } })).ds
  } catch (e) {
    toast.add({ title: loiApi(e), color: 'error' })
  }
}

watch(tab, (t) => {
  if (t === 'ds') taiDs()
  if (t === 'tin_hieu') taiTinHieu()
  if (t === 'nhat_ky') taiNk()
})
onMounted(taiTq)
</script>

<template>
  <AdminPage title="Thành viên HTBB">
    <template #right>
      <UButton to="/thanh-vien" target="_blank" color="neutral" variant="ghost" icon="i-lucide-external-link" label="Trang công khai" />
    </template>

    <UAlert
      v-if="tq?.demo"
      color="error"
      variant="solid"
      icon="i-lucide-flask-conical"
      title="BẢN DEMO — không chuyển tiền. Số tài khoản chỉ để minh hoạ."
      :description="`HTBB_NAP_DEMO=1: /thanh-vien cho ${tq.demoChiAdmin ? 'tài khoản trong HTBB_ADMIN_ACCOUNTS' : 'mọi người'} bấm Mua và xem luồng thanh toán minh hoạ (hình QR giả). Không bán thật, không cấp Thành viên. Giả lập thanh toán: /admin/naptien › Đơn DEMO.`"
    />
    <UAlert v-if="tq && !tq.coBang" color="error" variant="subtle" icon="i-lucide-database" title="Chưa có bảng thành viên" description="Chạy sql/htbb.sql trên htbb_db." />
    <UAlert v-if="tq?.cauHinh.banBat && !tq.ban.duoc" color="error" variant="solid" icon="i-lucide-shield-x" title="Đã bật bán nhưng CHƯA bán được (khoá an toàn)" :description="tq.ban.lyDo.join(' · ')" />
    <UAlert v-if="tq && !tq.cauHinh.bat" color="info" variant="subtle" icon="i-lucide-flask-conical" title="Đang TẮT bắt buộc (giai đoạn thử nghiệm)" description="Không tính năng nào bị khoá. Bật khi mở game chính thức; server áp dụng sau ≤ 10 giây, không cần khởi động lại." />

    <div v-if="tq?.thongKe" class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <AdminStatCard label="Đang là thành viên" :value="tq.thongKe.conHan" :note="`${tq.thongKe.vinhVien} vĩnh viễn`" icon="i-lucide-badge-check" />
      <AdminStatCard label="Hết hạn trong 7 ngày" :value="tq.thongKe.sapHet" icon="i-lucide-hourglass" />
      <AdminStatCard label="Đã thu hồi" :value="tq.thongKe.daThuHoi" icon="i-lucide-undo-2" />
      <AdminStatCard label="Đơn mua (đã kích hoạt)" :value="tq.thongKe.soDon" :note="vnd(tq.thongKe.doanhThu)" icon="i-lucide-wallet" />
    </div>

    <UTabs v-model="tab" :items="tabs" :content="false" class="w-full" />

    <template v-if="tab === 'cau_hinh'">
      <!-- Một chỗ sửa duy nhất: cùng khoá, cùng lịch sử với trang Cấu hình (docs/cau-hinh.md). -->
      <AdminHethongCauHinhNhom nhom="thanh_vien" />
      <AdminHethongCauHinhNhom :khoa="['thanh_vien.ban_bat']" tieu-de="Bán Thành viên trên website" />
      <p v-if="tq" class="text-sm text-muted">
        Mã tính năng dùng trong "Tính năng cần Thành viên / khi chưa kích hoạt":
        <span v-for="c in tq.chucNang" :key="c.ma" class="mr-2 whitespace-nowrap"><code>{{ c.ma }}</code> = {{ c.ten }}</span>
      </p>
    </template>

    <template v-else-if="tab === 'ds'">
      <UCard>
        <form class="flex gap-2" @submit.prevent="page = 1; taiDs()">
          <UInput v-model="tim" placeholder="Tên tài khoản (đầu tên)" icon="i-lucide-search" class="flex-1" />
          <UButton type="submit" label="Tìm" />
        </form>
      </UCard>
      <UCard :ui="{ body: 'p-0 sm:p-0' }">
        <UTable :data="ds?.ds ?? []" :columns="[{ accessorKey: 'username', header: 'Tài khoản' }, { accessorKey: 'het_han_ms', header: 'Hạn' }, { accessorKey: 'nguon', header: 'Nguồn' }, { accessorKey: 'so_lan', header: 'Lần' }, { accessorKey: 'cap_nhat_luc', header: 'Cập nhật' }, { accessorKey: 'ghi_chu', header: 'Ghi chú' }]" empty="Chưa có thành viên.">
          <template #het_han_ms-cell="{ row }">
            <UBadge :color="row.original.con_han ? 'success' : 'neutral'" variant="subtle" :label="hanText(row.original.het_han_ms)" />
          </template>
        </UTable>
      </UCard>
      <div v-if="ds && ds.tong > 50" class="flex justify-center">
        <UPagination v-model:page="page" :total="ds.tong" :items-per-page="50" />
      </div>
    </template>

    <template v-else-if="tab === 'cap'">
      <div class="grid gap-6 md:grid-cols-2">
        <UCard>
          <template #header>
            <b>Cấp / gia hạn (1 hoặc nhiều tài khoản)</b>
          </template>
          <div class="space-y-3">
            <UFormField label="Tài khoản (phẩy / xuống dòng; tối đa 500)">
              <UTextarea v-model="cap.taiKhoan" :rows="4" class="w-full" placeholder="tester1, tester2" />
            </UFormField>
            <UFormField label="Số ngày (0 = vĩnh viễn)">
              <UInputNumber v-model="cap.ngay" :min="0" :max="36500" class="w-full" />
            </UFormField>
            <UFormField label="Lý do">
              <UInput v-model="cap.lyDo" class="w-full" placeholder="tester đợt 1 / đền bù" />
            </UFormField>
          </div>
          <template #footer>
            <UButton v-if="laSieu" icon="i-lucide-badge-plus" :loading="dangGui" label="Cấp" @click="capTay" />
          </template>
        </UCard>
        <UCard>
          <template #header>
            <b>Thu hồi</b>
          </template>
          <div class="space-y-3">
            <UFormField label="Tài khoản">
              <UInput v-model="thu.taiKhoan" class="w-full" />
            </UFormField>
            <UFormField label="Lý do">
              <UInput v-model="thu.lyDo" class="w-full" />
            </UFormField>
            <p class="text-xs text-muted">
              Không hoàn tiền. Hoàn tiền đơn mua làm ở Nạp tiền › Đơn › Hoàn tiền (tự thu hồi đúng số ngày của lần mua đó).
            </p>
          </div>
          <template #footer>
            <UButton v-if="laSieu" color="error" icon="i-lucide-undo-2" :loading="dangGui" label="Thu hồi" @click="thuHoi" />
          </template>
        </UCard>
      </div>
    </template>

    <template v-else-if="tab === 'tin_hieu'">
      <UAlert color="info" variant="subtle" icon="i-lucide-info" title="Tín hiệu rẻ để soi tài khoản phụ / clone" description="Nguồn: tob_may_dang_nhap (mã máy client gửi + IP mỗi lần đăng nhập). Nhiều tài khoản chung một máy chưa chắc là gian lận (máy gia đình, quán net) — dùng để xem xét, kết hợp Cấm theo máy." />
      <UCard>
        <form class="flex flex-wrap items-end gap-3" @submit.prevent="taiTinHieu">
          <UFormField label="Ngưỡng tài khoản / máy">
            <UInputNumber v-model="nguong" :min="2" :max="50" />
          </UFormField>
          <UButton type="submit" icon="i-lucide-radar" label="Quét" />
          <span class="flex-1" />
          <UFormField label="Liên kết máy/IP của tài khoản">
            <UInput v-model="lkTen" placeholder="tên tài khoản" />
          </UFormField>
          <UButton color="neutral" variant="outline" icon="i-lucide-link" label="Xem" @click="taiLienKet" />
        </form>
      </UCard>
      <UCard v-if="lienKet">
        <template #header>
          <b>Liên kết của {{ lkTen }}</b>
        </template>
        <p v-if="!lienKet.coBang" class="text-muted">
          Chưa có bảng tob_may_dang_nhap.
        </p>
        <ul v-else class="space-y-1 text-sm">
          <li v-for="m in lienKet.may" :key="m.ma_may">
            Máy <span class="font-mono">{{ m.ma_may }}</span> · IP {{ m.ip }} · {{ m.so_lan }} lần · {{ m.lan_cuoi }} — tài khoản khác: {{ m.taiKhoanKhac.join(', ') || '—' }}
          </li>
          <li v-for="x in lienKet.ip" :key="x.ip">
            IP {{ x.ip }} — tài khoản khác: {{ x.taiKhoanKhac.join(', ') || '—' }}
          </li>
        </ul>
      </UCard>
      <UCard v-if="tinHieu">
        <template #header>
          <b>Máy dùng chung ≥ {{ nguong }} tài khoản</b>
        </template>
        <p v-if="!tinHieu.coBang" class="text-muted">
          Chưa có bảng tob_may_dang_nhap.
        </p>
        <p v-else-if="!tinHieu.may.length" class="text-muted">
          Không có máy nào vượt ngưỡng.
        </p>
        <ul v-else class="space-y-2 text-sm">
          <li v-for="m in tinHieu.may" :key="m.ma_may">
            <span class="font-mono">{{ m.ma_may }}</span> · <b>{{ m.so_tk }}</b> tài khoản ({{ m.soThanhVien }} thành viên) · lần cuối {{ m.lan_cuoi }}
            <div class="text-xs text-muted">
              <span v-for="t in m.taiKhoan" :key="t.username" class="mr-2" :class="t.thanhVien ? 'text-success' : ''">{{ t.username }}{{ t.thanhVien ? ' ✓' : '' }}</span>
            </div>
          </li>
        </ul>
      </UCard>
    </template>

    <template v-else-if="tab === 'nhat_ky'">
      <UCard>
        <form class="flex gap-2" @submit.prevent="taiNk">
          <UInput v-model="nkTen" placeholder="Lọc theo tài khoản (trống = tất cả)" class="flex-1" />
          <UButton type="submit" label="Xem" />
        </form>
      </UCard>
      <UCard :ui="{ body: 'p-0 sm:p-0' }">
        <UTable :data="nk" empty="Chưa có." />
      </UCard>
    </template>
  </AdminPage>
</template>
