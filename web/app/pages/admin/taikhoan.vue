<script setup lang="ts">
/**
 * Tài khoản đăng nhập (bảng account.team_user).
 *   /admin/taikhoan              danh sách, 30 dòng/trang (?trang=)
 *   /admin/taikhoan?q=hoahv      tìm theo tên, email, số điện thoại hoặc ID
 *   /admin/taikhoan?id=3         chi tiết một tài khoản (các trang khác link tới dạng này)
 *
 * KHOÁ = team_user.ban = 1, cột mà login server đọc. Khoá chỉ chặn LẦN ĐĂNG NHẬP SAU —
 * người đang online vẫn chơi tiếp tới khi thoát ra.
 */
import type { CotBang } from '~/components/admin/Bang.vue'
import AdminTaikhoanXacNhan from '~/components/admin/taikhoan/XacNhan.vue'

definePageMeta({ layout: 'admin', middleware: 'admin' })

interface Dong {
  id: number
  username: string
  email: string
  phone: string
  regdate: string
  ban: number
  laAdmin: boolean
  laChinhMinh: boolean
}
interface DanhSach {
  q: string
  trang: number
  conTrangSau: boolean
  gioiHan: boolean
  ketQua: Dong[]
}

const api = useAdminApi()
const toast = useToast()
const route = useRoute()
const router = useRouter()
const xacNhan = useOverlay().create(AdminTaikhoanXacNhan)
const me = useAdminMe()
const duocXoaNhieu = computed(() => me.duoc('/api/admin/taikhoan/xoa-nhieu'))

const xemId = computed(() => {
  const n = Number(route.query.id)
  return Number.isInteger(n) && n > 0 ? n : 0
})
const qUrl = computed(() => (typeof route.query.q === 'string' ? route.query.q.trim() : ''))
const trangUrl = computed(() => Math.max(1, Number.parseInt(String(route.query.trang ?? '1'), 10) || 1))

const tim = ref(qUrl.value)
watch(qUrl, (v) => { tim.value = v })

const { data, status, error, refresh } = await useAsyncData(
  'admin-taikhoan-ds',
  () => (xemId.value > 0
    ? Promise.resolve(null)
    : api<DanhSach>('/api/admin/taikhoan', { query: { q: qUrl.value || undefined, trang: trangUrl.value } })),
  { server: false, watch: [xemId, qUrl, trangUrl] },
)

function timKiem() {
  router.push({ path: '/admin/taikhoan', query: tim.value.trim() ? { q: tim.value.trim() } : {} })
}
function denTrang(t: number) {
  router.push({ path: '/admin/taikhoan', query: t > 1 ? { trang: String(t) } : {} })
}

// ---- Chọn nhiều để xoá ------------------------------------------------------
const chon = ref<number[]>([])
const goXacNhan = ref('')
const dangXoa = ref(false)
watch(data, () => { chon.value = [] })

const chonDuoc = computed(() => (data.value?.ketQua ?? []).filter(r => !r.laAdmin && !r.laChinhMinh))
const tatCa = computed<boolean | 'indeterminate'>({
  get: () => {
    const n = chon.value.length
    if (n === 0) return false
    return n === chonDuoc.value.length ? true : 'indeterminate'
  },
  set: (v) => { chon.value = v === true ? chonDuoc.value.map(r => r.id) : [] },
})
function batTat(id: number, v: boolean | 'indeterminate') {
  chon.value = v === true ? [...new Set([...chon.value, id])] : chon.value.filter(x => x !== id)
}

async function xoaDaChon() {
  const ds = (data.value?.ketQua ?? []).filter(r => chon.value.includes(r.id))
  if (!ds.length || goXacNhan.value !== 'XOA DA CHON') return
  const ok = await xacNhan.open({
    title: `Xoá vĩnh viễn ${ds.length} tài khoản và toàn bộ Pokémon của chúng?`,
    description: `${ds.map(r => r.username).join(', ')}\n\nKhông thể hoàn tác.`,
    nut: 'Xoá tài khoản đã chọn',
    mau: 'error',
  }).result
  if (!ok) return
  dangXoa.value = true
  try {
    const r = await api<{ loai: 'success' | 'warning', message: string }>('/api/admin/taikhoan/xoa-nhieu', {
      method: 'POST',
      body: { ids: ds.map(x => x.id), xac_nhan_xoa: goXacNhan.value },
    })
    toast.add({
      title: r.loai === 'warning' ? 'Chưa xong hết' : 'Đã xoá',
      description: r.message,
      color: r.loai === 'warning' ? 'warning' : 'success',
      icon: r.loai === 'warning' ? 'i-lucide-triangle-alert' : 'i-lucide-check',
      duration: r.loai === 'warning' ? 15000 : 6000,
    })
    goXacNhan.value = ''
    await refresh()
  } catch (e) {
    toast.add({ title: 'Không xoá được', description: loiApi(e), color: 'error', icon: 'i-lucide-circle-x' })
  } finally {
    dangXoa.value = false
  }
}

function trangThaiBan(ban: number) {
  if (ban === 1) return { nhan: 'Bị khoá', mau: 'error' as const }
  if (ban === 3) return { nhan: 'Chờ email', mau: 'warning' as const }
  return { nhan: 'Bình thường', mau: 'success' as const }
}

const cot = computed<CotBang[]>(() => [
  ...(duocXoaNhieu.value ? [{ key: 'chon', label: '' }] : []),
  { key: 'id', label: 'ID', sort: true, so: true, anMobile: true },
  { key: 'username', label: 'Tài khoản', sort: true },
  { key: 'email', label: 'Email', sort: true, anMobile: true },
  { key: 'regdate', label: 'Đăng ký', sort: true, anMobile: true },
  { key: 'trangthai', label: 'Trạng thái', giaTri: (r: Dong) => trangThaiBan(r.ban).nhan },
  { key: 'xem', label: '' },
])
</script>

<template>
  <AdminPage title="Tài khoản">
    <template #right>
      <UButton to="/admin/tao-hang-loat" icon="i-lucide-users-round" color="neutral" variant="outline" size="sm" label="Tạo hàng loạt" />
    </template>

    <AdminTaikhoanChiTiet v-if="xemId > 0" :id="xemId" />

    <template v-else>
      <div class="flex flex-wrap items-end justify-between gap-4">
        <p class="text-sm text-muted">
          Khoá / mở khoá đăng nhập, kích hoạt, đổi mật khẩu. Hồ sơ đầy đủ (Pokémon, IP, lịch sử) ở
          <NuxtLink to="/admin/nguoi-choi" class="text-primary hover:underline">Trung tâm người chơi</NuxtLink>.
        </p>
        <form class="flex w-full flex-wrap items-end gap-2 sm:w-auto" @submit.prevent="timKiem">
          <UFormField label="Tìm theo tên, email, số điện thoại hoặc ID" class="min-w-0 flex-1 sm:min-w-64">
            <UInput v-model="tim" placeholder="ví dụ: hoahv" icon="i-lucide-search" class="w-full" />
          </UFormField>
          <UButton type="submit" label="Tìm" />
          <UButton v-if="qUrl" to="/admin/taikhoan" color="neutral" variant="ghost" label="Bỏ lọc" />
        </form>
      </div>

      
      <UCard :ui="{ body: 'p-3 sm:p-4' }">
        <template #header>
          <div class="space-y-3">
            <h3 class="font-semibold text-highlighted">
              {{ qUrl === '' ? 'Danh sách tài khoản' : `Kết quả cho “${qUrl}”` }}
            </h3>
            <template v-if="duocXoaNhieu">
              <p class="text-sm text-muted">
                Chọn tài khoản cần xoá trên trang này. Xoá vĩnh viễn cả tài khoản và toàn bộ Pokémon; không thể hoàn tác.
              </p>
              <div class="flex flex-wrap items-center gap-3">
                <UCheckbox v-model="tatCa" label="Chọn tất cả trên trang" :disabled="chonDuoc.length === 0" />
                <span class="text-sm text-muted" aria-live="polite">Đã chọn {{ chon.length }} tài khoản</span>
                <UInput v-model="goXacNhan" autocomplete="off" size="sm" placeholder="Gõ XOA DA CHON để xác nhận" aria-label="Gõ XOA DA CHON để xác nhận" class="w-full sm:w-64" />
                <UButton
                  color="error"
                  size="sm"
                  icon="i-lucide-trash-2"
                  label="Xoá tài khoản đã chọn"
                  :disabled="chon.length === 0 || goXacNhan !== 'XOA DA CHON'"
                  :loading="dangXoa"
                  @click="xoaDaChon"
                />
              </div>
            </template>
          </div>
        </template>

        <AdminBang :rows="data?.ketQua" :cot="cot" :dang-tai="status === 'pending'" :loi="error" ten-csv="tai-khoan" row-key="id"
          :moi-trang="50" rong-tieu-de="Không có tài khoản nào khớp" @thu-lai="refresh">
          <template #cell-chon="{ row }">
            <UCheckbox
              :model-value="chon.includes(row.id)"
              :disabled="row.laAdmin || row.laChinhMinh"
              :aria-label="`Chọn ${row.username}`"
              @update:model-value="(v) => batTat(row.id, v)"
            />
          </template>
          <template #cell-username="{ row }">
            <span class="flex flex-wrap items-center gap-2">
              <NuxtLink :to="`/admin/nguoi-choi?u=${encodeURIComponent(row.username)}`" class="font-semibold text-primary hover:underline">{{ row.username }}</NuxtLink>
              <UBadge v-if="row.laAdmin" color="neutral" variant="outline" size="sm" label="admin" />
            </span>
            <span class="text-xs text-muted md:hidden">#{{ row.id }} · {{ row.email }}</span>
          </template>
          <template #cell-email="{ row }">
            <span class="break-all text-xs text-muted">{{ row.email || '—' }}</span>
          </template>
          <template #cell-regdate="{ row }">
            <span class="whitespace-nowrap text-xs text-muted">{{ row.regdate }}</span>
          </template>
          <template #cell-trangthai="{ row }">
            <UBadge :color="trangThaiBan(row.ban).mau" variant="subtle" :label="trangThaiBan(row.ban).nhan" />
          </template>
          <template #cell-xem="{ row }">
            <div class="flex justify-end gap-1">
              <UButton :to="`/admin/nguoi-choi?u=${encodeURIComponent(row.username)}`" size="xs" color="primary" variant="soft" icon="i-lucide-contact" label="Hồ sơ" />
              <UButton :to="`/admin/taikhoan?id=${row.id}`" size="xs" color="neutral" variant="outline" label="Quản lý" />
            </div>
          </template>
        </AdminBang>

        <template #footer>
          <div v-if="qUrl === ''" class="flex items-center justify-between gap-3 text-sm text-muted">
            <UButton v-if="trangUrl > 1" size="sm" color="neutral" variant="outline" icon="i-lucide-chevron-left" label="Trang trước" @click="denTrang(trangUrl - 1)" />
            <span v-else />
            <span>Trang {{ trangUrl }} · {{ data?.ketQua.length ?? 0 }} dòng</span>
            <UButton v-if="data?.conTrangSau" size="sm" color="neutral" variant="outline" trailing-icon="i-lucide-chevron-right" label="Trang sau" @click="denTrang(trangUrl + 1)" />
            <span v-else />
          </div>
          <p v-else-if="data?.gioiHan" class="text-sm text-muted">
            Chỉ hiện 50 kết quả đầu — gõ cụ thể hơn để thu hẹp.
          </p>
        </template>
      </UCard>
    </template>
  </AdminPage>
</template>
