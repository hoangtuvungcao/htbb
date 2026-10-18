<script setup lang="ts">
/**
 * Chi tiết một tài khoản (/admin/taikhoan?id=...): khoá đăng nhập, kích hoạt, đổi mật khẩu,
 * reset về trắng, xoá vĩnh viễn, và danh sách nhân vật.
 * Mọi chốt chặn (tài khoản quản trị, gõ đúng tên...) đều được server kiểm lại.
 */
import type { TableColumn } from '@nuxt/ui'
import AdminTaikhoanXacNhan from './XacNhan.vue'

const props = defineProps<{ id: number }>()

interface NhanVat {
  id: number
  charname: string
  lastLv: number
  class: number
  dex: number
  gold: number | string
  luong: number | string
  luonglock: number | string
  ban: number
  del: number
  lastLog: string
  idserver: number
}
interface ChiTiet {
  taiKhoan: { id: number, username: string, email: string, phone: string, regdate: string, ban: number }
  laAdmin: boolean
  laChinhMinh: boolean
  daKichHoat: boolean
  nhanVat: NhanVat[]
}

const api = useAdminApi()
const toast = useToast()
const xacNhan = useOverlay().create(AdminTaikhoanXacNhan)

const { data, status, error, refresh } = await useAsyncData(
  () => `admin-taikhoan-${props.id}`,
  () => api<ChiTiet>(`/api/admin/taikhoan/${props.id}`),
  { server: false, watch: [() => props.id] },
)

const tk = computed(() => data.value?.taiKhoan)
const dangLam = ref('')
const mk = ref('')
const mk2 = ref('')

async function lam(viec: string, body: Record<string, unknown> = {}) {
  dangLam.value = viec
  try {
    const r = await api<{ loai: 'success' | 'warning', message: string, daXoa?: boolean }>(
      `/api/admin/taikhoan/${props.id}`, { method: 'POST', body: { viec, ...body } })
    toast.add({
      title: r.loai === 'warning' ? 'Chưa xong' : 'Đã xong',
      description: r.message,
      color: r.loai === 'warning' ? 'warning' : 'success',
      icon: r.loai === 'warning' ? 'i-lucide-triangle-alert' : 'i-lucide-check',
      duration: r.loai === 'warning' ? 12000 : 5000,
    })
    if (r.daXoa) {
      await navigateTo('/admin/taikhoan')
      return
    }
    if (viec === 'doi_mat_khau') { mk.value = ''; mk2.value = '' }
    await refresh()
  } catch (e) {
    toast.add({ title: 'Không thực hiện được', description: loiApi(e), color: 'error', icon: 'i-lucide-circle-x' })
  } finally {
    dangLam.value = ''
  }
}

async function khoaMo() {
  const t = tk.value!
  const khoa = t.ban !== 1
  const ok = await xacNhan.open({
    title: khoa ? `Khoá tài khoản ${t.username}?` : `Mở khoá tài khoản ${t.username}?`,
    description: khoa ? 'Người này sẽ không đăng nhập được nữa.' : '',
    nut: khoa ? 'Khoá tài khoản' : 'Mở khoá',
    mau: khoa ? 'error' : 'primary',
  }).result
  if (ok) await lam(khoa ? 'khoa' : 'mo_khoa')
}

async function resetTrang() {
  const t = tk.value!
  const n = data.value!.nhanVat.length
  const ok = await xacNhan.open({
    title: `RESET tài khoản ${t.username} về trắng?`,
    description: `Xoá toàn bộ ${n} nhân vật của nó.\n\nKhông thể hoàn tác.`,
    nut: 'Reset về trắng',
    mau: 'error',
    goDe: t.username,
  }).result
  if (ok) await lam('reset_trang', { xac_nhan_ten: t.username })
}

async function xoaVinhVien() {
  const t = tk.value!
  const n = data.value!.nhanVat.length
  const ok = await xacNhan.open({
    title: `XOÁ VĨNH VIỄN tài khoản ${t.username}?`,
    description: `Xoá tài khoản và toàn bộ ${n} nhân vật của nó.\n\nKhông thể hoàn tác.`,
    nut: 'Xoá vĩnh viễn',
    mau: 'error',
    goDe: t.username,
  }).result
  if (ok) await lam('xoa_tai_khoan', { xac_nhan_ten: t.username })
}

function doiMatKhau() {
  if (mk.value !== mk2.value) {
    toast.add({ title: 'Hai lần nhập mật khẩu không giống nhau.', color: 'error', icon: 'i-lucide-circle-x' })
    return
  }
  lam('doi_mat_khau', { matkhau: mk.value, matkhau2: mk2.value })
}

function trangThaiBan(ban: number) {
  if (ban === 1) return { nhan: 'Bị khoá', mau: 'error' as const }
  if (ban === 3) return { nhan: 'Chờ email', mau: 'warning' as const }
  return { nhan: 'Bình thường', mau: 'success' as const }
}

const cotNV: TableColumn<NhanVat>[] = [
  { accessorKey: 'charname', header: 'Nhân vật' },
  { accessorKey: 'lastLv', header: 'Cấp', meta: { class: { th: 'text-right', td: 'text-right tabular-nums' } } },
  { accessorKey: 'luong', header: 'Lượng', meta: { class: { th: 'text-right', td: 'text-right tabular-nums' } } },
  { accessorKey: 'gold', header: 'Xu', meta: { class: { th: 'text-right', td: 'text-right tabular-nums' } } },
  { accessorKey: 'lastLog', header: 'Vào lần cuối' },
  { id: 'trangthai', header: 'Trạng thái' },
  { id: 'mo', header: '' },
]
</script>

<template>
  <div class="space-y-4">
    <UButton to="/admin/taikhoan" icon="i-lucide-arrow-left" color="neutral" variant="link" label="Về danh sách" class="px-0" />

    <UAlert v-if="error" color="error" variant="subtle" icon="i-lucide-circle-x" :title="loiApi(error)" />

    <div v-else-if="status === 'pending' && !data" class="grid gap-4 lg:grid-cols-[minmax(0,380px)_1fr]">
      <USkeleton class="h-72" />
      <USkeleton class="h-72" />
    </div>

    <div v-else-if="data && tk" class="grid items-start gap-4 lg:grid-cols-[minmax(0,380px)_1fr]">
      <div class="space-y-4">
        <!-- Thông tin -->
        <UCard>
          <template #header>
            <div class="flex items-center gap-2">
              <h3 class="text-lg font-semibold text-highlighted">
                {{ tk.username }}
              </h3>
              <UBadge :color="trangThaiBan(tk.ban).mau" variant="subtle" :label="trangThaiBan(tk.ban).nhan" />
              <UBadge v-if="data.laAdmin" color="neutral" variant="outline" label="admin" />
            </div>
          </template>
          <dl class="grid grid-cols-[7rem_1fr] gap-x-3 gap-y-2 text-sm">
            <dt class="text-muted">
              ID
            </dt><dd class="tabular-nums">
              {{ tk.id }}
            </dd>
            <dt class="text-muted">
              Email
            </dt><dd class="break-all">
              {{ tk.email || '—' }}
            </dd>
            <dt class="text-muted">
              Điện thoại
            </dt><dd>{{ tk.phone || '—' }}</dd>
            <dt class="text-muted">
              Đăng ký
            </dt><dd>{{ tk.regdate || '—' }}</dd>
            <dt class="text-muted">
              Kích hoạt
            </dt>
            <dd>
              <UBadge :color="data.daKichHoat ? 'success' : 'warning'" variant="subtle" :label="data.daKichHoat ? 'Đã kích hoạt' : 'Chưa kích hoạt'" />
            </dd>
            <dt class="text-muted">
              Nhân vật
            </dt><dd class="tabular-nums">
              {{ data.nhanVat.length }}
            </dd>
          </dl>
          <UAlert
            v-if="data.laAdmin"
            class="mt-4"
            color="neutral"
            variant="subtle"
            icon="i-lucide-shield"
            title="Đây là tài khoản quản trị"
            description="Có tên trong ADMIN_ACCOUNTS nên không khoá được từ trang này."
          />
        </UCard>

        <!-- Khoá đăng nhập -->
        <UCard>
          <template #header>
            <h3 class="font-semibold text-highlighted">
              Khoá đăng nhập
            </h3>
          </template>
          <p class="mb-3 text-sm text-muted">
            <template v-if="tk.ban === 1">
              Tài khoản đang bị khoá. Khi đăng nhập, người chơi thấy “Tài khoản của bạn hiện đang bị khoá”.
            </template>
            <template v-else>
              Khoá sẽ chặn <em>lần đăng nhập sau</em>. Người đang online vẫn chơi tiếp cho tới khi thoát ra.
            </template>
          </p>
          <UButton
            :color="tk.ban === 1 ? 'primary' : 'error'"
            :label="tk.ban === 1 ? 'Mở khoá' : 'Khoá tài khoản'"
            :icon="tk.ban === 1 ? 'i-lucide-lock-open' : 'i-lucide-lock'"
            size="sm"
            :disabled="data.laAdmin"
            :loading="dangLam === 'khoa' || dangLam === 'mo_khoa'"
            @click="khoaMo"
          />
        </UCard>

        <!-- Kích hoạt -->
        <UCard>
          <template #header>
            <h3 class="font-semibold text-highlighted">
              Kích hoạt
            </h3>
          </template>
          <p class="mb-3 text-sm text-muted">
            Bảng <code>5h_active</code> — server chỉ chặn khi <code>sv.EnableAvtiveCheck</code> đang bật.
          </p>
          <UButton
            :color="data.daKichHoat ? 'neutral' : 'primary'"
            :variant="data.daKichHoat ? 'outline' : 'solid'"
            :label="data.daKichHoat ? 'Huỷ kích hoạt' : 'Kích hoạt ngay'"
            size="sm"
            :loading="dangLam === 'kich_hoat' || dangLam === 'huy_kich_hoat'"
            @click="lam(data.daKichHoat ? 'huy_kich_hoat' : 'kich_hoat')"
          />
        </UCard>

        <!-- Đổi mật khẩu -->
        <UCard>
          <template #header>
            <h3 class="font-semibold text-highlighted">
              Đổi mật khẩu
            </h3>
          </template>
          <p v-if="data.laAdmin && !data.laChinhMinh" class="text-sm text-muted">
            Không đổi được mật khẩu của một tài khoản quản trị khác.
          </p>
          <form v-else class="space-y-3" autocomplete="off" @submit.prevent="doiMatKhau">
            <UFormField label="Mật khẩu mới">
              <UInput v-model="mk" type="password" required :minlength="PASSWORD_MIN" :maxlength="PASSWORD_MAX" autocomplete="new-password" class="w-full" />
            </UFormField>
            <UFormField label="Nhập lại">
              <UInput v-model="mk2" type="password" required :minlength="PASSWORD_MIN" :maxlength="PASSWORD_MAX" autocomplete="new-password" class="w-full" />
            </UFormField>
            <UButton type="submit" size="sm" label="Đổi mật khẩu" :loading="dangLam === 'doi_mat_khau'" />
            <p class="text-xs text-dimmed">
              Băm bằng <code>PASSWORD()</code> để khớp login server.
            </p>
          </form>
        </UCard>

        <!-- Reset về trắng -->
        <UCard :ui="{ root: 'ring-error/30' }">
          <template #header>
            <h3 class="font-semibold text-error">
              Reset về trắng
            </h3>
          </template>
          <p v-if="data.laAdmin" class="text-sm text-muted">
            Không reset được tài khoản quản trị.
          </p>
          <template v-else>
            <p class="mb-3 text-sm text-muted">
              <strong class="text-highlighted">KHÔNG THỂ HOÀN TÁC.</strong> Xoá vĩnh viễn toàn bộ
              <strong class="text-highlighted">{{ data.nhanVat.length }} nhân vật</strong> của tài khoản này (cùng dữ liệu
              riêng của từng nhân vật: thú nuôi, trang trại, đá quý...) nhưng
              <strong class="text-highlighted">giữ nguyên tài khoản đăng nhập</strong> — khác với "Xoá vĩnh viễn" bên dưới.
              Đăng nhập lại được ngay và tạo nhân vật mới, y như tài khoản vừa đăng ký. Nhân vật đang online sẽ bị đẩy ra
              khỏi game trước khi xoá. Nhật ký giao dịch/log vẫn được giữ lại để tra cứu.
            </p>
            <UButton color="error" variant="soft" size="sm" icon="i-lucide-rotate-ccw" label="Reset về trắng" :loading="dangLam === 'reset_trang'" @click="resetTrang" />
          </template>
        </UCard>

        <!-- Xoá vĩnh viễn -->
        <UCard :ui="{ root: 'ring-error/30' }">
          <template #header>
            <h3 class="font-semibold text-error">
              Xoá vĩnh viễn
            </h3>
          </template>
          <p v-if="data.laAdmin" class="text-sm text-muted">
            Không xoá được tài khoản quản trị.
          </p>
          <template v-else>
            <p class="mb-3 text-sm text-muted">
              <strong class="text-highlighted">KHÔNG THỂ HOÀN TÁC.</strong> Khác hẳn "Khoá đăng nhập" ở trên (chỉ chặn đăng
              nhập, mở lại được) — xoá vĩnh viễn xoá hẳn tài khoản này khỏi cơ sở dữ liệu, cùng với
              <strong class="text-highlighted">toàn bộ {{ data.nhanVat.length }} nhân vật</strong> của nó (và dữ liệu riêng
              của từng nhân vật: thú nuôi, trang trại, đá quý...). Nhân vật đang online sẽ bị đẩy ra khỏi game trước khi
              xoá. Nhật ký giao dịch/log vẫn được giữ lại để tra cứu.
            </p>
            <UButton color="error" size="sm" icon="i-lucide-trash-2" label="Xoá vĩnh viễn" :loading="dangLam === 'xoa_tai_khoan'" @click="xoaVinhVien" />
          </template>
        </UCard>
      </div>

      <!-- Nhân vật -->
      <UCard :ui="{ body: 'p-0 sm:p-0' }">
        <template #header>
          <h3 class="font-semibold text-highlighted">
            Nhân vật của tài khoản này
          </h3>
        </template>
        <UTable :data="data.nhanVat" :columns="cotNV" empty="Tài khoản này chưa tạo nhân vật nào.">
          <template #charname-cell="{ row }">
            <strong class="text-highlighted">{{ row.original.charname }}</strong>
            <div class="text-xs text-muted">
              {{ speciesName(row.original.class, row.original.lastLv, row.original.dex) }} · máy chủ {{ row.original.idserver }}
            </div>
          </template>
          <template #luong-cell="{ row }">
            {{ so(row.original.luong) }}
          </template>
          <template #gold-cell="{ row }">
            {{ so(row.original.gold) }}
          </template>
          <template #lastLog-cell="{ row }">
            <span class="whitespace-nowrap text-xs text-muted">{{ row.original.lastLog }}</span>
          </template>
          <template #trangthai-cell="{ row }">
            <UBadge v-if="row.original.ban !== 0" color="error" variant="subtle" label="Khoá" />
            <UBadge v-else-if="row.original.del !== 1" color="neutral" variant="subtle" label="Đã xoá" />
            <UBadge v-else color="success" variant="subtle" label="Bình thường" />
          </template>
          <template #mo-cell="{ row }">
            <UButton :to="`/admin/nhanvat?id=${row.original.id}`" size="xs" color="neutral" variant="outline" label="Mở" />
          </template>
        </UTable>
      </UCard>
    </div>
  </div>
</template>
