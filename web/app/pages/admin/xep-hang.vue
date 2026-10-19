<script setup lang="ts">
/**
 * Cài đặt bảng xếp hạng công khai (/ranking): bảng nào hiện + thứ tự tab, số dòng, làm mới,
 * danh sách loại trừ (tên nhân vật / tài khoản, có tiền tố "tai0*").
 * Lưu vào web_cai_dat khoá 'xepHang' — không cần sửa code. Ghi chỉ dành cho siêu quản trị.
 */

definePageMeta({ layout: 'admin', middleware: 'admin' })

interface Muc { ma: string, ten: string, icon: string, nhom: string, moTa: string, nguon: string, trongGame: boolean, gioiHanGame: number, bang: string[], khaDung: boolean }
interface CaiDat {
  bang: Array<{ ma: string, hien: boolean }>
  soDong: number
  moiTrang: number
  lamMoiGiay: number
  loaiTru: { tenNhanVat: string[], taiKhoan: string[], anTaiKhoanKhoa: boolean, anGm: boolean }
  mocChiemGym: string
}
interface Tra { coBang: boolean, tuDb: boolean, caiDat: CaiDat, danhMuc: Muc[], gioiHan: Record<string, [number, number]>, napBat: boolean, muaHienTai: number | null }

const api = useAdminApi()
const toast = useToast()
const me = useAdminMe()

const { data, pending, error, refresh } = await useAsyncData('admin-xep-hang', () => api<Tra>('/api/admin/xep-hang'), { server: false })

const f = ref<CaiDat | null>(null)
const tenNv = ref('')
const taiKhoan = ref('')
const daSua = ref(false)
watch(data, (d) => {
  if (!d) return
  f.value = structuredClone(d.caiDat)
  tenNv.value = d.caiDat.loaiTru.tenNhanVat.join('\n')
  taiKhoan.value = d.caiDat.loaiTru.taiKhoan.join('\n')
  nextTick(() => { daSua.value = false })
}, { immediate: true })
watch([f, tenNv, taiKhoan], () => { daSua.value = true }, { deep: true })

const muc = (ma: string) => data.value?.danhMuc.find(m => m.ma === ma)
const duocGhi = computed(() => me.duoc('/api/admin/xep-hang/luu'))

function doi(i: number, buoc: number) {
  const ds = f.value!.bang
  const j = i + buoc
  if (j < 0 || j >= ds.length) return
  ;[ds[i], ds[j]] = [ds[j]!, ds[i]!]
}

const dangLuu = ref(false)
async function luu() {
  if (!f.value) return
  dangLuu.value = true
  try {
    const body = { caiDat: { ...f.value, loaiTru: { ...f.value.loaiTru, tenNhanVat: tenNv.value, taiKhoan: taiKhoan.value } } }
    await api('/api/admin/xep-hang/luu', { method: 'POST', body })
    toast.add({ title: 'Đã lưu — trang Bảng xếp hạng đổi ngay', color: 'success' })
    await refresh()
    anTai.value = false
  }
  catch (e) { toast.add({ title: loiApi(e), color: 'error' }) }
  finally { dangLuu.value = false }
}

// Xem trước danh sách bị ẩn
const anTai = ref(false)
const { data: biAn, pending: dangTaiBiAn, error: loiBiAn, execute: taiBiAn } = useAsyncData('admin-xep-hang-bi-an',
  () => api<{ ds: { ten: string, lyDo: string }[] }>('/api/admin/xep-hang/bi-an'), { server: false, immediate: false })
function xemBiAn() { anTai.value = true; taiBiAn() }
</script>

<template>
  <AdminPage title="Bảng xếp hạng web">
    <template #right>
      <UButton to="/ranking" target="_blank" color="neutral" variant="outline" icon="i-lucide-external-link" label="Xem trang công khai" />
      <UButton v-if="duocGhi" icon="i-lucide-save" label="Lưu" :loading="dangLuu" :disabled="!f || !daSua" @click="luu" />
    </template>

    <UAlert v-if="error" color="error" variant="subtle" icon="i-lucide-triangle-alert" :title="loiApi(error)" :actions="[{ label: 'Thử lại', onClick: () => refresh() }]" />
    <AdminCmsChuaCoBang v-else-if="data && !data.coBang" bang="web_cai_dat" />
    <div v-if="pending && !f" class="space-y-3">
      <USkeleton v-for="i in 4" :key="i" class="h-16 w-full" />
    </div>

    <template v-if="f && data">
      <UAlert
        v-if="!duocGhi" color="info" variant="subtle" icon="i-lucide-eye"
        title="Bạn đang xem ở chế độ chỉ đọc" description="Chỉ siêu quản trị đổi được cài đặt bảng xếp hạng."
      />
      <UAlert
        v-if="!data.tuDb" color="neutral" variant="subtle" icon="i-lucide-info"
        title="Đang dùng cài đặt mặc định" description="Bấm Lưu một lần để ghi vào CSDL."
      />

      <UCard>
        <template #header>
          <div>
            <b>Các bảng & thứ tự tab</b>
            <p class="text-sm text-muted">
              Bảng có nhãn “Trong game” chép đúng truy vấn của menu Bảng Top trong game. Bảng thiếu bảng CSDL / tính năng chưa bật tự ẩn.
            </p>
          </div>
        </template>
        <ol class="divide-y divide-default">
          <li v-for="(b, i) in f.bang" :key="b.ma" class="flex flex-wrap items-start gap-3 py-3">
            <div class="flex flex-col gap-1">
              <UButton size="xs" color="neutral" variant="ghost" icon="i-lucide-chevron-up" :aria-label="`Đưa ${muc(b.ma)?.ten} lên`" :disabled="!duocGhi || i === 0" @click="doi(i, -1)" />
              <UButton size="xs" color="neutral" variant="ghost" icon="i-lucide-chevron-down" :aria-label="`Đưa ${muc(b.ma)?.ten} xuống`" :disabled="!duocGhi || i === f.bang.length - 1" @click="doi(i, 1)" />
            </div>
            <UIcon :name="muc(b.ma)?.icon ?? 'i-lucide-list'" class="mt-1 size-5 text-primary" />
            <div class="min-w-0 flex-1">
              <div class="flex flex-wrap items-center gap-2">
                <b class="text-highlighted">{{ muc(b.ma)?.ten }}</b>
                <UBadge size="sm" color="neutral" variant="subtle">{{ muc(b.ma)?.nhom }}</UBadge>
                <UBadge v-if="muc(b.ma)?.trongGame" size="sm" color="success" variant="subtle">Trong game · tối đa {{ muc(b.ma)?.gioiHanGame }}</UBadge>
                <UBadge v-if="!muc(b.ma)?.khaDung" size="sm" color="warning" variant="subtle">Chưa dùng được (thiếu bảng / tính năng tắt)</UBadge>
              </div>
              <p class="mt-1 text-sm text-muted">{{ muc(b.ma)?.moTa }}</p>
              <p class="mt-1 text-xs text-dimmed">Nguồn: {{ muc(b.ma)?.nguon }}</p>
            </div>
            <USwitch v-model="b.hien" :disabled="!duocGhi" :label="b.hien ? 'Hiện' : 'Ẩn'" />
          </li>
        </ol>
      </UCard>

      <div class="grid gap-6 lg:grid-cols-2">
        <UCard>
          <template #header><b>Hiển thị & làm mới</b></template>
          <div class="grid gap-4 sm:grid-cols-3">
            <UFormField label="Số dòng tối đa" :help="`${data.gioiHan.soDong?.[0]}–${data.gioiHan.soDong?.[1]}; bảng trong game còn theo giới hạn của game`">
              <UInputNumber v-model="f.soDong" :min="data.gioiHan.soDong?.[0]" :max="data.gioiHan.soDong?.[1]" :disabled="!duocGhi" class="w-full" />
            </UFormField>
            <UFormField label="Dòng mỗi trang" :help="`${data.gioiHan.moiTrang?.[0]}–${data.gioiHan.moiTrang?.[1]}`">
              <UInputNumber v-model="f.moiTrang" :min="data.gioiHan.moiTrang?.[0]" :max="data.gioiHan.moiTrang?.[1]" :disabled="!duocGhi" class="w-full" />
            </UFormField>
            <UFormField label="Làm mới (giây)" help="Game tự làm mới top mỗi 60 giây">
              <UInputNumber v-model="f.lamMoiGiay" :min="data.gioiHan.lamMoiGiay?.[0]" :max="data.gioiHan.lamMoiGiay?.[1]" :step="15" :disabled="!duocGhi" class="w-full" />
            </UFormField>
          </div>
          <UFormField label="Mốc bắt đầu đợt đua Chiếm Gym" help="Định dạng YYYY-MM-DD HH:MM:SS (giờ máy chủ)" class="mt-4">
            <UInput v-model="f.mocChiemGym" placeholder="2026-09-12 20:00:00" :disabled="!duocGhi" class="w-full" />
          </UFormField>
          <p v-if="data.muaHienTai != null" class="mt-4 text-sm text-muted">
            Mùa PvP hiện tại (theo ngày khai trương trong cấu hình game): <b>Mùa {{ data.muaHienTai + 1 }}</b>.
          </p>
        </UCard>

        <UCard>
          <template #header>
            <div>
              <b>Loại khỏi mọi bảng</b>
              <p class="text-sm text-muted">Mỗi dòng một tên; thêm * ở cuối để lọc theo tiền tố (vd. <code>tai0*</code> cho tài khoản thử).</p>
            </div>
          </template>
          <div class="space-y-4">
            <USwitch v-model="f.loaiTru.anGm" :disabled="!duocGhi" label="Ẩn GM (sv.adminAccounts)" description="Giống game ở Top sức mạnh / đại gia." />
            <USwitch v-model="f.loaiTru.anTaiKhoanKhoa" :disabled="!duocGhi" label="Ẩn tài khoản đang bị khoá" description="Pokémon bị khoá (tob_char.ban) luôn bị ẩn như game." />
            <UFormField label="Tên Pokémon">
              <UTextarea v-model="tenNv" :rows="4" autoresize :disabled="!duocGhi" placeholder="bot01&#10;thu_nghiem*" class="w-full font-mono" />
            </UFormField>
            <UFormField label="Tên đăng nhập (không bao giờ hiện ra trang công khai)">
              <UTextarea v-model="taiKhoan" :rows="4" autoresize :disabled="!duocGhi" placeholder="tai0*" class="w-full font-mono" />
            </UFormField>
            <UButton color="neutral" variant="outline" icon="i-lucide-eye-off" label="Xem ai đang bị ẩn (theo cài đặt đã lưu)" @click="xemBiAn" />
          </div>
        </UCard>
      </div>

      <UCard v-if="anTai">
        <template #header><b>Pokémon đang bị ẩn</b></template>
        <AdminBang
          :rows="biAn?.ds" :dang-tai="dangTaiBiAn" :loi="loiBiAn" ten-csv="xep-hang-bi-an"
          :cot="[{ key: 'ten', label: 'Pokémon', sort: true }, { key: 'lyDo', label: 'Lý do', sort: true }]"
          rong-tieu-de="Không ai bị ẩn" @thu-lai="taiBiAn"
        />
      </UCard>
    </template>
  </AdminPage>
</template>
