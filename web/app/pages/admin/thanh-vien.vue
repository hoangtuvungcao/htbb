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
const ch = ref<CauHinh | null>(null)
const dangGui = ref(false)

async function taiTq() {
  try {
    tq.value = await api<TongQuan>('/api/admin/thanh-vien/tong-quan')
    ch.value = { ...tq.value.cauHinh, chucNang: [...tq.value.cauHinh.chucNang], khChucNang: [...tq.value.cauHinh.khChucNang] }
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

function batTat(ma: string, v: boolean, kh = false) {
  if (!ch.value) return
  const s = new Set(kh ? ch.value.khChucNang : ch.value.chucNang)
  if (v) s.add(ma)
  else s.delete(ma)
  const ds = (tq.value?.chucNang ?? []).map(c => c.ma).filter(m => s.has(m))
  if (kh) ch.value.khChucNang = ds
  else ch.value.chucNang = ds
}

const luuCauHinh = () => ch.value && gui('/api/admin/thanh-vien/cau-hinh', { ...ch.value })

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

    <UAlert v-if="tq && !tq.coBang" color="error" variant="subtle" icon="i-lucide-database" title="Chưa có bảng thành viên" description="Chạy HTBB/du_lieu/sql_nguon/pokemon_nap_tien.sql rồi HTBB/du_lieu/sql_nguon/pokemon_thanh_vien.sql trên htbb_db." />
    <UAlert v-if="tq?.cauHinh.banBat && !tq.ban.duoc" color="error" variant="solid" icon="i-lucide-shield-x" title="Đã bật bán nhưng CHƯA bán được (fail closed)" :description="tq.ban.lyDo.join(' · ')" />
    <UAlert v-if="tq && !tq.cauHinh.bat" color="info" variant="subtle" icon="i-lucide-flask-conical" title="Đang TẮT bắt buộc (giai đoạn thử nghiệm)" description="Không tính năng nào bị khoá. Bật khi mở game chính thức; server áp dụng sau ≤ 10 giây, không cần khởi động lại." />

    <div v-if="tq?.thongKe" class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <AdminStatCard label="Đang là thành viên" :value="tq.thongKe.conHan" :note="`${tq.thongKe.vinhVien} vĩnh viễn`" icon="i-lucide-badge-check" />
      <AdminStatCard label="Hết hạn trong 7 ngày" :value="tq.thongKe.sapHet" icon="i-lucide-hourglass" />
      <AdminStatCard label="Đã thu hồi" :value="tq.thongKe.daThuHoi" icon="i-lucide-undo-2" />
      <AdminStatCard label="Đơn mua (đã kích hoạt)" :value="tq.thongKe.soDon" :note="vnd(tq.thongKe.doanhThu)" icon="i-lucide-wallet" />
    </div>

    <UTabs v-model="tab" :items="tabs" :content="false" class="w-full" />

    <template v-if="tab === 'cau_hinh' && ch && tq">
      <UCard>
        <div class="grid gap-4 md:grid-cols-2">
          <USwitch v-model="ch.bat" :disabled="!laSieu" label="BẮT BUỘC Thành viên cho các tính năng bên dưới (mở game chính thức)" />
          <USwitch v-model="ch.banBat" :disabled="!laSieu" label="Mở BÁN trên website (cần giấy phép + cổng thanh toán an toàn)" />
          <UFormField label="Giá (đ, chẵn nghìn)">
            <UInputNumber v-model="ch.gia" :min="10000" :max="2000000" :step="1000" :disabled="!laSieu" class="w-full" />
          </UFormField>
          <UFormField label="Thời hạn (ngày, 0 = vĩnh viễn)">
            <UInputNumber v-model="ch.thoiHanNgay" :min="0" :max="36500" :disabled="!laSieu" class="w-full" />
          </UFormField>
          <UFormField label="Miễn cho Pokémon dưới cấp (0 = không miễn)">
            <UInputNumber v-model="ch.mienCap" :min="0" :max="200" :disabled="!laSieu" class="w-full" />
          </UFormField>
          <UFormField label="Thông báo khi bị chặn (trống = mặc định; {ten} {gia} {web})">
            <UInput v-model="ch.thongBao" :disabled="!laSieu" class="w-full" />
          </UFormField>
        </div>
        <USeparator class="my-4" />
        <p class="mb-2 font-semibold">
          Tính năng cần Thành viên (khi bắt buộc)
        </p>
        <div class="grid gap-2 md:grid-cols-2">
          <UCheckbox
            v-for="c in tq.chucNang"
            :key="c.ma"
            :model-value="ch.chucNang.includes(c.ma)"
            :disabled="!laSieu"
            :label="c.ten"
            :description="`${c.ma} — ${c.cho}`"
            @update:model-value="(v: boolean | 'indeterminate') => batTat(c.ma, v === true)"
          />
        </div>
        <USeparator class="my-4" />
        <p class="font-semibold">
          Kích hoạt tài khoản (miễn phí, admin cấp — bảng 5h_active)
        </p>
        <p class="mb-2 text-sm text-muted">
          Chưa kích hoạt vẫn đăng nhập và chơi; chỉ bị khoá các tính năng chọn dưới. Một cổng chung với Thành viên:
          bị chặn nếu (cần kích hoạt và chưa kích hoạt) hoặc (cần Thành viên và chưa là Thành viên). Chỉ có tác dụng khi
          server.ini sv.EnableAvtiveCheck=1.
        </p>
        <div class="grid gap-4 md:grid-cols-2">
          <USwitch v-model="ch.khChanDangNhap" :disabled="!laSieu" label="CHẶN ĐĂNG NHẬP tài khoản chưa kích hoạt (hành vi cũ)" />
          <USwitch v-model="ch.khTvLaKh" :disabled="!laSieu" label="Thành viên HTBB được coi như đã kích hoạt" />
          <UFormField label="Thông báo khi bị khoá vì chưa kích hoạt (trống = mặc định; {ten} {web})" class="md:col-span-2">
            <UInput v-model="ch.khThongBao" :disabled="!laSieu" class="w-full" />
          </UFormField>
        </div>
        <div class="mt-3 grid gap-2 md:grid-cols-2">
          <UCheckbox
            v-for="c in tq.chucNang"
            :key="`kh-${c.ma}`"
            :model-value="ch.khChucNang.includes(c.ma)"
            :disabled="!laSieu"
            :label="`${c.ten} (khi chưa kích hoạt)`"
            @update:model-value="(v: boolean | 'indeterminate') => batTat(c.ma, v === true, true)"
          />
        </div>
        <template #footer>
          <UButton v-if="laSieu" icon="i-lucide-save" :loading="dangGui" label="Lưu cấu hình" @click="luuCauHinh" />
          <span v-else class="text-sm text-muted">Chỉ Siêu quản trị sửa được.</span>
        </template>
      </UCard>
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
