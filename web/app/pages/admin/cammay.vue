<script setup lang="ts">
/**
 * Cấm đăng nhập theo MÃ MÁY (device id).
 *
 * Cấm tài khoản thì kẻ phá lập tài khoản mới trong 30 giây; mã máy đi theo thiết bị nên
 * bám dai hơn, và bảng tob_may_dang_nhap cho thấy *một máy đã dùng những tài khoản nào*.
 * Mã máy là SystemInfo.deviceUniqueIdentifier do client Unity gửi (Client/Canvas.cs:157),
 * KHÔNG phải MAC. Lệnh cấm có hiệu lực chậm nhất 60 giây (server làm mới danh sách mỗi phút).
 */
import type { TableColumn } from '@nuxt/ui'
import AdminTaikhoanXacNhan from '~/components/admin/taikhoan/XacNhan.vue'

definePageMeta({ layout: 'admin', middleware: 'admin' })

interface MayCam { ma_may: string, ly_do: string, boi_ai: string, luc: string }
interface MayNhieu { ma_may: string, so_tk: number, gan_nhat: string, ip: string }
interface TkCuaMay { username: string, ip: string, so_lan: number, lan_dau: string, lan_cuoi: string }
interface MayCuaTk { ma_may: string, ip: string, so_lan: number, lan_dau: string, lan_cuoi: string }
interface DuLieu {
  q: string
  coBang: boolean
  loiDoc: string
  dsCam: MayCam[]
  tong: { may: number, cam: number, nhieu_tk: number }
  mayNhieuTk: MayNhieu[]
  tkCuaMay: TkCuaMay[]
  mayCuaTk: MayCuaTk[]
  dangBiCam: boolean
}

const api = useAdminApi()
const toast = useToast()
const route = useRoute()
const router = useRouter()
const xacNhan = useOverlay().create(AdminTaikhoanXacNhan)

const qUrl = computed(() => (typeof route.query.q === 'string' ? route.query.q.trim() : ''))
const tim = ref(qUrl.value)
watch(qUrl, (v) => { tim.value = v })

const { data, status, error, refresh } = await useAsyncData(
  'admin-cammay',
  () => api<DuLieu>('/api/admin/cammay', { query: { q: qUrl.value || undefined } }),
  { server: false, watch: [qUrl] },
)

function timKiem() {
  router.push({ path: '/admin/cammay', query: tim.value.trim() ? { q: tim.value.trim() } : {} })
}

const dangLam = ref('')
async function camHayBo(maMay: string, dangCam: boolean) {
  const ok = await xacNhan.open({
    title: `${dangCam ? 'Bỏ cấm' : 'Cấm'} máy ${maMay}?`,
    nut: dangCam ? 'Bỏ cấm' : 'Cấm',
    mau: dangCam ? 'primary' : 'error',
  }).result
  if (!ok) return
  dangLam.value = maMay
  try {
    const r = await api<{ message: string }>('/api/admin/cammay', {
      method: 'POST',
      body: dangCam ? { viec: 'bo_cam', ma_may: maMay } : { viec: 'cam', ma_may: maMay, ly_do: 'cấm từ danh sách' },
    })
    toast.add({ title: r.message, color: 'success', icon: 'i-lucide-check' })
    await refresh()
  } catch (e) {
    toast.add({ title: 'Không thao tác được', description: loiApi(e), color: 'error', icon: 'i-lucide-circle-x' })
  } finally {
    dangLam.value = ''
  }
}

const giua = { th: 'text-right', td: 'text-right tabular-nums' }
const cotTkCuaMay: TableColumn<TkCuaMay>[] = [
  { accessorKey: 'username', header: 'Tài khoản' },
  { accessorKey: 'ip', header: 'IP' },
  { accessorKey: 'so_lan', header: 'Số lần', meta: { class: giua } },
  { accessorKey: 'lan_dau', header: 'Lần đầu' },
  { accessorKey: 'lan_cuoi', header: 'Lần cuối' },
]
const cotMayCuaTk: TableColumn<MayCuaTk>[] = [
  { accessorKey: 'ma_may', header: 'Mã máy' },
  { accessorKey: 'ip', header: 'IP' },
  { accessorKey: 'so_lan', header: 'Số lần', meta: { class: giua } },
  { accessorKey: 'lan_cuoi', header: 'Lần cuối' },
  { id: 'nut', header: '' },
]
const cotMayNhieu: TableColumn<MayNhieu>[] = [
  { accessorKey: 'ma_may', header: 'Mã máy' },
  { accessorKey: 'so_tk', header: 'Tài khoản', meta: { class: giua } },
  { accessorKey: 'gan_nhat', header: 'Lần cuối' },
  { id: 'nut', header: '' },
]
const cotCam: TableColumn<MayCam>[] = [
  { accessorKey: 'ma_may', header: 'Mã máy' },
  { accessorKey: 'ly_do', header: 'Lý do' },
  { accessorKey: 'boi_ai', header: 'Bởi' },
  { accessorKey: 'luc', header: 'Lúc' },
  { id: 'nut', header: '' },
]
</script>

<template>
  <AdminPage title="Cấm theo máy">
    <div class="flex flex-wrap items-end justify-between gap-4">
      <p class="max-w-2xl text-sm text-muted">
        Cấm theo <strong class="text-highlighted">mã máy</strong> thay vì theo tài khoản. Gõ mã máy để xem nó đã dùng
        những tài khoản nào, hoặc gõ tên tài khoản để xem nó đăng nhập từ những máy nào.
      </p>
      <form class="flex items-end gap-2" @submit.prevent="timKiem">
        <UFormField label="Mã máy hoặc tên tài khoản" class="min-w-72">
          <UInput v-model="tim" placeholder="dán mã máy, hoặc gõ tên tài khoản" icon="i-lucide-search" class="w-full" />
        </UFormField>
        <UButton type="submit" label="Tìm" />
        <UButton v-if="qUrl" to="/admin/cammay" color="neutral" variant="ghost" label="Bỏ lọc" />
      </form>
    </div>

    <UAlert v-if="error" color="error" variant="subtle" icon="i-lucide-circle-x" :title="loiApi(error)" />

    <USkeleton v-else-if="status === 'pending' && !data" class="h-40" />

    <UAlert
      v-else-if="data && !data.coBang"
      color="warning"
      variant="subtle"
      icon="i-lucide-database"
      title="Chưa có bảng dữ liệu."
    >
      <template #description>
        <p>Chạy một lần trên máy chủ: <code>mysql -u root kpah_db &lt; sql/them_bang_cam_may.sql</code></p>
        <p class="mt-1 text-xs">
          Lỗi đọc được: {{ data.loiDoc }}
        </p>
      </template>
    </UAlert>

    <template v-else-if="data">
      <div class="grid gap-4 sm:grid-cols-3">
        <AdminStatCard label="Máy đã ghi nhận" :value="data.tong.may" icon="i-lucide-smartphone" />
        <AdminStatCard label="Đang bị cấm" :value="data.tong.cam" :mau="data.tong.cam > 0 ? 'bad' : ''" icon="i-lucide-lock" />
        <AdminStatCard label="Máy dùng ≥ 5 tài khoản" :value="data.tong.nhieu_tk" note="đáng soi" :mau="data.tong.nhieu_tk > 0 ? 'warn' : ''" icon="i-lucide-users" />
      </div>

      <UAlert
        v-if="data.tong.may === 0"
        color="warning"
        variant="subtle"
        icon="i-lucide-info"
        title="Bảng đã có nhưng chưa ghi nhận máy nào. Hai nguyên nhân thường gặp:"
      >
        <template #description>
          <p>
            1. Máy chủ chưa chạy bản jar có <code>CamTheoMay</code> — kiểm bằng:
            <code>jar tf KPAH2_patched.jar | grep CamTheoMay</code>
          </p>
          <p class="mt-1">
            2. <strong>Client chưa gửi mã máy</strong> — đây là nguyên nhân gần như chắc chắn. Client hiện chỉ gửi mã máy
            kèm gói <em>đăng ký</em>, mà đăng ký đã chuyển sang web và đường đó đã bị đóng. Server đọc mã máy ở
            <em>trường cuối cùng</em> của gói đăng nhập; client cũ không gửi thì bỏ qua (cố ý — để không ép ai cập nhật
            cùng lúc). Cần thêm một dòng vào cuối gói đăng nhập bên client rồi build lại:
            <code>message.writer().writeUTF(Canvas.IMEI);</code>
          </p>
        </template>
      </UAlert>

      <!-- Kết quả tìm -->
      <UCard v-if="qUrl !== ''">
        <template #header>
          <div class="flex flex-wrap items-center gap-2">
            <h3 class="font-semibold text-highlighted">
              Kết quả cho <code>{{ qUrl }}</code>
            </h3>
            <UBadge v-if="data.dangBiCam" color="error" variant="subtle" label="Đang bị cấm" />
          </div>
        </template>

        <div v-if="data.tkCuaMay.length" class="space-y-3">
          <div class="flex flex-wrap items-center justify-between gap-2">
            <h4 class="font-medium text-highlighted">
              Máy này đã dùng {{ data.tkCuaMay.length }} tài khoản
            </h4>
            <UButton
              size="sm"
              :color="data.dangBiCam ? 'neutral' : 'error'"
              :variant="data.dangBiCam ? 'outline' : 'solid'"
              :label="data.dangBiCam ? 'Bỏ cấm' : 'Cấm'"
              :loading="dangLam === qUrl"
              @click="camHayBo(qUrl, data.dangBiCam)"
            />
          </div>
          <UTable :data="data.tkCuaMay" :columns="cotTkCuaMay">
            <template #username-cell="{ row }">
              <NuxtLink :to="{ path: '/admin/taikhoan', query: { q: row.original.username } }" class="text-primary hover:underline">
                {{ row.original.username }}
              </NuxtLink>
            </template>
            <template #ip-cell="{ row }">
              <span class="whitespace-nowrap text-xs text-muted">{{ row.original.ip }}</span>
            </template>
            <template #so_lan-cell="{ row }">
              {{ so(row.original.so_lan) }}
            </template>
            <template #lan_dau-cell="{ row }">
              <span class="whitespace-nowrap text-xs text-muted">{{ row.original.lan_dau }}</span>
            </template>
            <template #lan_cuoi-cell="{ row }">
              <span class="whitespace-nowrap text-xs text-muted">{{ row.original.lan_cuoi }}</span>
            </template>
          </UTable>
        </div>

        <div v-if="data.mayCuaTk.length" class="space-y-3" :class="{ 'mt-6': data.tkCuaMay.length }">
          <h4 class="font-medium text-highlighted">
            Tài khoản này đăng nhập từ {{ data.mayCuaTk.length }} máy
          </h4>
          <UTable :data="data.mayCuaTk" :columns="cotMayCuaTk">
            <template #ma_may-cell="{ row }">
              <NuxtLink :to="{ path: '/admin/cammay', query: { q: row.original.ma_may } }" class="text-primary hover:underline">
                <code>{{ row.original.ma_may }}</code>
              </NuxtLink>
            </template>
            <template #ip-cell="{ row }">
              <span class="whitespace-nowrap text-xs text-muted">{{ row.original.ip }}</span>
            </template>
            <template #so_lan-cell="{ row }">
              {{ so(row.original.so_lan) }}
            </template>
            <template #lan_cuoi-cell="{ row }">
              <span class="whitespace-nowrap text-xs text-muted">{{ row.original.lan_cuoi }}</span>
            </template>
            <template #nut-cell="{ row }">
              <UButton size="xs" color="error" variant="soft" label="Cấm" :loading="dangLam === row.original.ma_may" @click="camHayBo(row.original.ma_may, false)" />
            </template>
          </UTable>
        </div>

        <p v-if="!data.tkCuaMay.length && !data.mayCuaTk.length" class="text-sm text-muted">
          Không có máy hay tài khoản nào khớp <code>{{ qUrl }}</code>.
        </p>
      </UCard>

      <div class="grid items-start gap-4 xl:grid-cols-2">
        <UCard :ui="{ body: 'p-0 sm:p-0' }">
          <template #header>
            <h3 class="font-semibold text-highlighted">
              Máy dùng nhiều tài khoản nhất
            </h3>
            <p class="mt-1 text-sm text-muted">
              Một máy dùng nhiều tài khoản chưa chắc là xấu — anh em trong nhà chung máy là chuyện thường. Nhưng đây là
              chỗ kẻ lập hàng loạt tài khoản lộ ra.
            </p>
          </template>
          <UTable :data="data.mayNhieuTk" :columns="cotMayNhieu" empty="Chưa có máy nào dùng từ 2 tài khoản trở lên.">
            <template #ma_may-cell="{ row }">
              <NuxtLink :to="{ path: '/admin/cammay', query: { q: row.original.ma_may } }" class="text-primary hover:underline">
                <code class="break-all">{{ row.original.ma_may }}</code>
              </NuxtLink>
            </template>
            <template #so_tk-cell="{ row }">
              {{ so(row.original.so_tk) }}
            </template>
            <template #gan_nhat-cell="{ row }">
              <span class="whitespace-nowrap text-xs text-muted">{{ row.original.gan_nhat }}</span>
            </template>
            <template #nut-cell="{ row }">
              <UButton size="xs" color="error" variant="soft" label="Cấm" :loading="dangLam === row.original.ma_may" @click="camHayBo(row.original.ma_may, false)" />
            </template>
          </UTable>
        </UCard>

        <UCard :ui="{ body: 'p-0 sm:p-0' }">
          <template #header>
            <h3 class="font-semibold text-highlighted">
              Đang bị cấm
            </h3>
          </template>
          <UTable :data="data.dsCam" :columns="cotCam" empty="Chưa cấm máy nào.">
            <template #ma_may-cell="{ row }">
              <NuxtLink :to="{ path: '/admin/cammay', query: { q: row.original.ma_may } }" class="text-primary hover:underline">
                <code class="break-all">{{ row.original.ma_may }}</code>
              </NuxtLink>
            </template>
            <template #ly_do-cell="{ row }">
              <span class="text-sm">{{ row.original.ly_do }}</span>
            </template>
            <template #boi_ai-cell="{ row }">
              <span class="whitespace-nowrap text-xs text-muted">{{ row.original.boi_ai }}</span>
            </template>
            <template #luc-cell="{ row }">
              <span class="whitespace-nowrap text-xs text-muted">{{ row.original.luc }}</span>
            </template>
            <template #nut-cell="{ row }">
              <UButton size="xs" color="neutral" variant="outline" label="Bỏ cấm" :loading="dangLam === row.original.ma_may" @click="camHayBo(row.original.ma_may, true)" />
            </template>
          </UTable>
        </UCard>
      </div>

      <UAlert color="neutral" variant="subtle" icon="i-lucide-info" title="Đọc trước khi cấm.">
        <template #description>
          <p>
            Mã máy do <em>client tự khai</em>, nên nằm trong tay người dùng: chặn được người phá lười (đa số), không chặn
            được người biết sửa client. Cài lại ứng dụng có thể sinh mã mới. Và máy dùng chung — quán net, máy trong nhà —
            thì cấm một người là chặn cả nhóm.
          </p>
          <p class="mt-1">
            Lệnh cấm có hiệu lực <strong>chậm nhất 60 giây</strong>: máy chủ giữ danh sách trong RAM và làm mới mỗi phút,
            cố ý như vậy để một trận dò mật khẩu không thành một trận dội database.
          </p>
          <p class="mt-1">
            Máy nào đã từng đăng nhập tài khoản quản trị thì <strong>không cấm được</strong> — chặn sẵn để bạn không tự
            khoá mình.
          </p>
        </template>
      </UAlert>
    </template>
  </AdminPage>
</template>
