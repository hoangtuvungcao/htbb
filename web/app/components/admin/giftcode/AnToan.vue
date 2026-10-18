<script setup lang="ts">
/**
 * Giftcode an toàn (tob_giftcode / tob_giftcode_log): hạn dùng, tổng lượt, cấp tối thiểu, phần
 * thưởng chỉ vật phẩm khoá có trần giá trị, ngân sách tháng toàn máy chủ, nhật ký nhận.
 * Quy tắc ở server/utils/giftcodeAnToan.ts (bản TS của GiftcodeAnToanLogic.java).
 */
import AdminTaikhoanXacNhan from '~/components/admin/taikhoan/XacNhan.vue'

defineEmits<{ cheDoCu: [] }>()
interface Ma { ma: string, phan_thuong: string, han: string | null, tong_luot: number, da_dung: number, cap_toi_thieu: number, bat: number, ghi_chu: string, tao_boi: string, tao_luc: string, giaTri: number | null, moTa: string[] }
interface DuLieu { coBang: boolean, gioiHan: { giaTriToiDa: number, nganSachThang: number, toiDaThang: number }, thang: number, nganSach: { daDung: number, luot: number, toiDa: number }, ds: Ma[] }
const api = useAdminApi()
const toast = useToast()
const me = useAdminMe()
const xacNhan = useOverlay().create(AdminTaikhoanXacNhan)
const { data, refresh } = useAsyncData('admin-gc-moi', () => api<DuLieu>('/api/admin/giftcode-moi'), { server: false })
const duocSua = computed(() => me.vaiTro.value === 'sieu_quan_tri')

const f = reactive({ ma: '', phanThuong: 'LUONGKHOA:10', han: '', tongLuot: 100, capToiThieu: 1, bat: true, ghiChu: '', sua: false })
const xem = ref<{ ok: boolean, giaTri?: number, toiDa?: number, moTa?: string[], loi?: string } | null>(null)
let hen: ReturnType<typeof setTimeout> | undefined
watch(() => f.phanThuong, (v) => {
  clearTimeout(hen)
  hen = setTimeout(async () => { try { xem.value = await api('/api/admin/giftcode-moi/kiem-tra', { method: 'POST', body: { phanThuong: v } }) } catch { xem.value = null } }, 300)
}, { immediate: true })
function themThuong(s: string) { f.phanThuong = f.phanThuong.trim() ? `${f.phanThuong.trim()},${s}` : s }
function moi() { Object.assign(f, { ma: '', phanThuong: 'LUONGKHOA:10', han: '', tongLuot: 100, capToiThieu: 1, bat: true, ghiChu: '', sua: false }) }
function sua(m: Ma) {
  Object.assign(f, { ma: m.ma, phanThuong: m.phan_thuong.replace(/:-1(?=,|$)/g, ''), han: (m.han ?? '').slice(0, 16).replace(' ', 'T'), tongLuot: Number(m.tong_luot), capToiThieu: Number(m.cap_toi_thieu), bat: Number(m.bat) === 1, ghiChu: m.ghi_chu, sua: true })
}
async function luu() {
  try {
    await api('/api/admin/giftcode-moi/luu', { method: 'POST', body: { ...f, han: f.han ? f.han.replace('T', ' ') : '' } })
    toast.add({ title: f.sua ? 'Đã lưu mã' : `Đã tạo mã ${f.ma}`, color: 'success' })
    moi()
    refresh()
  }
  catch (e) { toast.add({ title: loiApi(e), color: 'error' }) }
}
async function batTat(m: Ma) {
  try { await api('/api/admin/giftcode-moi/bat', { method: 'POST', body: { ma: m.ma, bat: Number(m.bat) !== 1 } }); refresh() }
  catch (e) { toast.add({ title: loiApi(e), color: 'error' }) }
}
async function xoa(m: Ma) {
  if (!(await xacNhan.open({ title: `Xoá mã ${m.ma}?`, nut: 'Xoá', mau: 'error' }).result)) return
  try { await api('/api/admin/giftcode-moi/xoa', { method: 'POST', body: { ma: m.ma } }); refresh() }
  catch (e) { toast.add({ title: loiApi(e), color: 'error' }) }
}

const locLog = ref('')
const { data: log } = useAsyncData('admin-gc-log', () => api<{ ds: Record<string, any>[] }>('/api/admin/giftcode-moi/log', { query: { ma: locLog.value || undefined } }), { server: false, watch: [refDebounced(locLog, 300)] })
const phanTram = computed(() => data.value ? Math.min(100, (data.value.nganSach.daDung / Math.max(1, data.value.nganSach.toiDa)) * 100) : 0)
const hetHan = (m: Ma) => !!m.han && new Date(m.han.replace(' ', 'T')) < new Date()
</script>

<template>
  <AdminPage title="Giftcode">
    <template #right>
      <UButton color="neutral" variant="ghost" icon="i-lucide-history" label="Mã kiểu cũ" @click="$emit('cheDoCu')" />
    </template>
    <div class="grid gap-4 md:grid-cols-3">
      <UCard>
        <p class="text-sm text-muted">Ngân sách tháng {{ String(data?.thang ?? '').replace(/(\d{4})(\d{2})/, '$2/$1') }}</p>
        <p class="text-2xl font-bold">{{ (data?.nganSach.daDung ?? 0).toLocaleString('vi-VN') }} <span class="text-base font-normal text-muted">/ {{ (data?.nganSach.toiDa ?? 0).toLocaleString('vi-VN') }}</span></p>
        <div class="mt-2 h-2 overflow-hidden rounded-full bg-elevated" role="meter" :aria-valuenow="phanTram" aria-valuemin="0" aria-valuemax="100" aria-label="Ngân sách tháng đã dùng">
          <div class="h-full rounded-full" :class="phanTram >= 90 ? 'bg-error' : phanTram >= 70 ? 'bg-warning' : 'bg-primary'" :style="{ width: `${phanTram}%` }" />
        </div>
        <p class="mt-1 text-xs text-dimmed">{{ data?.nganSach.luot ?? 0 }} lượt nhận trong tháng · đơn vị: giá trị (lượng khoá)</p>
      </UCard>
      <UCard>
        <p class="text-sm text-muted">Trần giá trị một mã</p>
        <p class="text-2xl font-bold">{{ data?.gioiHan.giaTriToiDa }}</p>
        <p class="text-xs text-dimmed">= lượng khoá + số đá + 5 × số vật phẩm</p>
      </UCard>
      <UCard>
        <p class="text-sm text-muted">Mỗi Pokémon / tháng</p>
        <p class="text-2xl font-bold">{{ data?.gioiHan.toiDaThang }} mã</p>
        <p class="text-xs text-dimmed">Mỗi tài khoản 1 lần / mã · nhập sai 5 lần khoá 10 phút (server)</p>
      </UCard>
    </div>

    <!-- Trần / ngân sách: CÙNG khoá cau_hinh với trang Cấu hình hệ thống (nhóm Kinh tế). -->
    <details v-if="duocSua" class="rounded-lg border border-default">
      <summary class="cursor-pointer select-none px-4 py-2 text-sm font-medium">Sửa trần & ngân sách giftcode (Cấu hình hệ thống)</summary>
      <div class="p-3"><AdminHethongCauHinhNhom :khoa="['giftcode.gia_tri_toi_da', 'giftcode.ngan_sach_thang', 'giftcode.toi_da_thang']" tieu-de="Trần & ngân sách giftcode" /></div>
    </details>

    <UCard v-if="duocSua">
      <template #header>
        <div class="flex items-center justify-between">
          <b>{{ f.sua ? `Sửa mã ${f.ma}` : 'Tạo mã mới' }}</b>
          <UButton v-if="f.sua" size="xs" color="neutral" variant="ghost" label="Huỷ sửa" @click="moi" />
        </div>
      </template>
      <div class="grid gap-3 md:grid-cols-4">
        <UFormField label="Mã" help="3–32 ký tự a-z 0-9 _ -"><UInput v-model="f.ma" :disabled="f.sua" placeholder="khaitruong" class="w-full" /></UFormField>
        <UFormField label="Hết hạn" help="Trống = không hạn"><UInput v-model="f.han" type="datetime-local" class="w-full" /></UFormField>
        <UFormField label="Tổng lượt (mọi tài khoản)"><UInputNumber v-model="f.tongLuot" :min="1" class="w-full" /></UFormField>
        <UFormField label="Cấp tối thiểu"><UInputNumber v-model="f.capToiThieu" :min="1" :max="100" class="w-full" /></UFormField>
        <UFormField label="Phần thưởng (chỉ vật phẩm khoá)" class="md:col-span-3" help="LUONGKHOA:n · GEM:id:n · ITEM:id:n (n ≤ 10), cách nhau dấu phẩy">
          <UInput v-model="f.phanThuong" class="w-full font-mono" />
          <div class="mt-1 flex flex-wrap gap-1">
            <UButton size="xs" color="neutral" variant="soft" label="+ 5 lượng khoá" @click="themThuong('LUONGKHOA:5')" />
            <UButton size="xs" color="neutral" variant="soft" label="+ GEM:id:1" @click="themThuong('GEM:0:1')" />
            <UButton size="xs" color="neutral" variant="soft" label="+ ITEM:id:1" @click="themThuong('ITEM:0:1')" />
          </div>
        </UFormField>
        <UFormField label="Ghi chú"><UInput v-model="f.ghiChu" class="w-full" /></UFormField>
      </div>
      <div class="mt-3 rounded-lg p-3 text-sm" :class="xem?.ok ? 'bg-success/10' : 'bg-error/10'" aria-live="polite">
        <template v-if="xem?.moTa">
          <b>Giá trị {{ xem.giaTri }} / {{ xem.toiDa }}</b> — {{ xem.moTa.join(' · ') }}
        </template>
        <p v-if="xem?.loi" class="text-error">{{ xem.loi }}</p>
      </div>
      <div class="mt-3 flex items-center gap-3">
        <USwitch v-model="f.bat" label="Đang bật" />
        <UButton class="ml-auto" icon="i-lucide-save" :label="f.sua ? 'Lưu mã' : 'Tạo mã'" :disabled="!xem?.ok || !f.ma" @click="luu" />
      </div>
    </UCard>

    <UCard :ui="{ body: 'p-0 sm:p-0' }">
      <template #header><b>Mã an toàn ({{ data?.ds.length ?? 0 }})</b></template>
      <div class="overflow-x-auto">
        <table class="bang-wiki">
          <thead><tr><th>Mã</th><th>Phần thưởng</th><th class="so">Giá trị</th><th class="so">Đã dùng</th><th>Hạn</th><th class="so">Cấp</th><th>Trạng thái</th><th /></tr></thead>
          <tbody>
            <tr v-for="m in data?.ds ?? []" :key="m.ma">
              <td class="font-mono font-semibold">{{ m.ma }}<span v-if="m.ghi_chu" class="block font-sans text-xs font-normal text-dimmed">{{ m.ghi_chu }}</span></td>
              <td class="text-xs">{{ m.moTa.join(' · ') }}</td>
              <td class="so">{{ m.giaTri ?? '—' }}</td>
              <td class="so">{{ m.da_dung }} / {{ m.tong_luot }}</td>
              <td class="whitespace-nowrap text-xs">{{ m.han ?? 'Không hạn' }}</td>
              <td class="so">{{ m.cap_toi_thieu }}</td>
              <td>
                <UBadge v-if="hetHan(m)" color="neutral" variant="subtle">Hết hạn</UBadge>
                <UBadge v-else-if="Number(m.da_dung) >= Number(m.tong_luot)" color="neutral" variant="subtle">Hết lượt</UBadge>
                <UBadge v-else :color="Number(m.bat) === 1 ? 'success' : 'warning'" variant="subtle">{{ Number(m.bat) === 1 ? 'Bật' : 'Tắt' }}</UBadge>
              </td>
              <td class="whitespace-nowrap text-right">
                <template v-if="duocSua">
                  <UButton size="xs" color="neutral" variant="ghost" icon="i-lucide-pencil" aria-label="Sửa" @click="sua(m)" />
                  <UButton size="xs" color="neutral" variant="ghost" :icon="Number(m.bat) === 1 ? 'i-lucide-pause' : 'i-lucide-play'" :aria-label="Number(m.bat) === 1 ? 'Tắt' : 'Bật'" @click="batTat(m)" />
                  <UButton v-if="Number(m.da_dung) === 0" size="xs" color="error" variant="ghost" icon="i-lucide-trash-2" aria-label="Xoá" @click="xoa(m)" />
                </template>
                <UButton size="xs" color="neutral" variant="ghost" icon="i-lucide-list" aria-label="Nhật ký" @click="locLog = m.ma" />
              </td>
            </tr>
            <tr v-if="!data?.ds.length"><td colspan="8" class="text-center text-muted">Chưa có mã nào.</td></tr>
          </tbody>
        </table>
      </div>
    </UCard>

    <UCard :ui="{ body: 'p-0 sm:p-0' }">
      <template #header>
        <div class="flex flex-wrap items-center gap-3">
          <b>Nhật ký nhận mã</b>
          <UInput v-model="locLog" size="sm" icon="i-lucide-filter" placeholder="Lọc theo mã" class="w-48" />
        </div>
      </template>
      <div class="max-h-96 overflow-auto">
        <table class="bang-wiki">
          <thead><tr><th>Lúc</th><th>Mã</th><th>Pokémon</th><th class="so">TK #</th><th>Phần thưởng</th><th class="so">Giá trị</th></tr></thead>
          <tbody>
            <tr v-for="(r, i) in log?.ds ?? []" :key="i">
              <td class="whitespace-nowrap text-xs">{{ r.luc }}</td><td class="font-mono">{{ r.ma }}</td><td>{{ r.charname }}</td><td class="so">{{ r.user_id }}</td><td class="text-xs">{{ r.phan_thuong }}</td><td class="so">{{ r.gia_tri }}</td>
            </tr>
            <tr v-if="!log?.ds?.length"><td colspan="6" class="text-center text-muted">Chưa có lượt nhận.</td></tr>
          </tbody>
        </table>
      </div>
    </UCard>
  </AdminPage>
</template>
