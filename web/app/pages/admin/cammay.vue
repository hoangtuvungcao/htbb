<script setup lang="ts">
/**
 * Cấm đăng nhập theo MÃ MÁY (device id).
 *
 * Cấm tài khoản thì kẻ phá lập tài khoản mới trong 30 giây; mã máy đi theo thiết bị nên
 * bám dai hơn, và bảng tob_may_dang_nhap cho thấy *một máy đã dùng những tài khoản nào*.
 * Mã máy là SystemInfo.deviceUniqueIdentifier do client Unity gửi (Client/Canvas.cs:157),
 * KHÔNG phải MAC. Lệnh cấm có hiệu lực chậm nhất 60 giây (server làm mới danh sách mỗi phút).
 */
import type { CotBang } from '~/components/admin/Bang.vue'
import AdminTaikhoanXacNhan from '~/components/admin/taikhoan/XacNhan.vue'
import HoiLyDo from '~/components/admin/nguoichoi/HoiLyDo.vue'

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
const hoiLyDo = useOverlay().create(HoiLyDo)
const me = useAdminMe()
const duocCam = computed(() => me.duoc('/api/admin/cammay'))

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
  let lyDo = ''
  if (dangCam) {
    const ok = await xacNhan.open({ title: `Bỏ cấm máy ${maMay}?`, nut: 'Bỏ cấm', mau: 'primary' }).result
    if (!ok) return
  }
  else {
    const kq = await hoiLyDo.open({
      title: `Cấm máy ${maMay}?`,
      description: 'Mọi tài khoản đăng nhập từ máy này bị chặn (hiệu lực trong 60 giây). Máy dùng chung thì chặn cả nhóm.',
      nut: 'Cấm máy', mau: 'error', toiDa: 255, goiY: ['Lập hàng loạt tài khoản phá game', 'Dùng bot / phần mềm thứ ba', 'Lừa đảo nhiều lần'],
    }).result
    if (!kq) return
    lyDo = kq.lyDo
  }
  dangLam.value = maMay
  try {
    const r = await api<{ message: string }>('/api/admin/cammay', {
      method: 'POST',
      body: dangCam ? { viec: 'bo_cam', ma_may: maMay } : { viec: 'cam', ma_may: maMay, ly_do: lyDo },
    })
    toast.add({ title: r.message, color: 'success', icon: 'i-lucide-check' })
    await refresh()
  } catch (e) {
    toast.add({ title: 'Không thao tác được', description: loiApi(e), color: 'error', icon: 'i-lucide-circle-x' })
  } finally {
    dangLam.value = ''
  }
}

const cotTkCuaMay: CotBang[] = [
  { key: 'username', label: 'Tài khoản', sort: true },
  { key: 'ip', label: 'IP', sort: true },
  { key: 'so_lan', label: 'Số lần', sort: true, so: true },
  { key: 'lan_dau', label: 'Lần đầu', sort: true, anMobile: true },
  { key: 'lan_cuoi', label: 'Lần cuối', sort: true },
]
const cotMayCuaTk: CotBang[] = [
  { key: 'ma_may', label: 'Mã máy', sort: true },
  { key: 'ip', label: 'IP', sort: true, anMobile: true },
  { key: 'so_lan', label: 'Số lần', sort: true, so: true },
  { key: 'lan_cuoi', label: 'Lần cuối', sort: true, anMobile: true },
  { key: 'nut', label: '' },
]
const cotMayNhieu: CotBang[] = [
  { key: 'ma_may', label: 'Mã máy', sort: true },
  { key: 'so_tk', label: 'Tài khoản', sort: true, so: true },
  { key: 'gan_nhat', label: 'Lần cuối', sort: true, anMobile: true },
  { key: 'nut', label: '' },
]
const cotCam: CotBang[] = [
  { key: 'ma_may', label: 'Mã máy', sort: true },
  { key: 'ly_do', label: 'Lý do' },
  { key: 'boi_ai', label: 'Bởi', sort: true, anMobile: true },
  { key: 'luc', label: 'Lúc', sort: true, anMobile: true },
  { key: 'nut', label: '' },
]
</script>

<template>
  <AdminPage title="Cấm theo máy">
    <div class="flex flex-wrap items-end justify-between gap-4">
      <p class="max-w-2xl text-sm text-muted">
        Cấm theo <strong class="text-highlighted">mã máy</strong> thay vì theo tài khoản. Gõ mã máy để xem nó đã dùng
        những tài khoản nào, hoặc gõ tên tài khoản để xem nó đăng nhập từ những máy nào.
      </p>
      <form class="flex w-full flex-wrap items-end gap-2 sm:w-auto" @submit.prevent="timKiem">
        <UFormField label="Mã máy hoặc tên tài khoản" class="min-w-0 flex-1 sm:min-w-72">
          <UInput v-model="tim" maxlength="64" placeholder="dán mã máy, hoặc gõ tên tài khoản" icon="i-lucide-search" class="w-full" />
        </UFormField>
        <UButton type="submit" label="Tìm" />
        <UButton v-if="qUrl" to="/admin/cammay" color="neutral" variant="ghost" label="Bỏ lọc" />
      </form>
    </div>

    <UAlert v-if="error" color="error" variant="subtle" icon="i-lucide-circle-x" :title="loiApi(error)"
      :actions="[{ label: 'Thử lại', color: 'error', variant: 'outline', onClick: () => refresh() }]" />

    <USkeleton v-else-if="status === 'pending' && !data" class="h-40" />

    <UAlert
      v-else-if="data && !data.coBang"
      color="warning"
      variant="subtle"
      icon="i-lucide-database"
      title="Chưa có bảng dữ liệu."
    >
      <template #description>
        <p>Chạy một lần trên máy chủ: <code>mysql -u root htbb_db &lt; sql/them_bang_cam_may.sql</code></p>
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
            <code>jar tf htbb-server.jar | grep CamTheoMay</code>
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
              v-if="duocCam"
              size="sm"
              :color="data.dangBiCam ? 'neutral' : 'error'"
              :variant="data.dangBiCam ? 'outline' : 'solid'"
              :label="data.dangBiCam ? 'Bỏ cấm' : 'Cấm'"
              :loading="dangLam === qUrl"
              @click="camHayBo(qUrl, data.dangBiCam)"
            />
          </div>
          <AdminBang :rows="data.tkCuaMay" :cot="cotTkCuaMay" ten-csv="tai-khoan-cua-may" rong-tieu-de="Không có tài khoản nào">
            <template #cell-username="{ row }">
              <NuxtLink :to="{ path: '/admin/nguoi-choi', query: { u: row.username } }" class="font-medium text-primary hover:underline">
                {{ row.username }}
              </NuxtLink>
            </template>
            <template #cell-ip="{ row }">
              <span class="whitespace-nowrap font-mono text-xs text-muted">{{ row.ip }}</span>
            </template>
            <template #cell-lan_dau="{ row }">
              <span class="whitespace-nowrap text-xs text-muted">{{ row.lan_dau }}</span>
            </template>
            <template #cell-lan_cuoi="{ row }">
              <span class="whitespace-nowrap text-xs text-muted">{{ row.lan_cuoi }}</span>
            </template>
          </AdminBang>
        </div>

        <div v-if="data.mayCuaTk.length" class="space-y-3" :class="{ 'mt-6': data.tkCuaMay.length }">
          <h4 class="font-medium text-highlighted">
            Tài khoản này đăng nhập từ {{ data.mayCuaTk.length }} máy
          </h4>
          <AdminBang :rows="data.mayCuaTk" :cot="cotMayCuaTk" ten-csv="may-cua-tai-khoan" rong-tieu-de="Không có máy nào">
            <template #cell-ma_may="{ row }">
              <NuxtLink :to="{ path: '/admin/cammay', query: { q: row.ma_may } }" class="text-primary hover:underline">
                <code class="break-all">{{ row.ma_may }}</code>
              </NuxtLink>
            </template>
            <template #cell-ip="{ row }">
              <span class="whitespace-nowrap font-mono text-xs text-muted">{{ row.ip }}</span>
            </template>
            <template #cell-lan_cuoi="{ row }">
              <span class="whitespace-nowrap text-xs text-muted">{{ row.lan_cuoi }}</span>
            </template>
            <template #cell-nut="{ row }">
              <UButton v-if="duocCam" size="xs" color="error" variant="soft" label="Cấm" :loading="dangLam === row.ma_may" @click="camHayBo(row.ma_may, false)" />
            </template>
          </AdminBang>
        </div>

        <p v-if="!data.tkCuaMay.length && !data.mayCuaTk.length" class="text-sm text-muted">
          Không có máy hay tài khoản nào khớp <code>{{ qUrl }}</code>.
        </p>
      </UCard>

      <div class="grid items-start gap-4 xl:grid-cols-2">
        <UCard :ui="{ body: 'p-3 sm:p-4' }">
          <template #header>
            <h3 class="font-semibold text-highlighted">
              Máy dùng nhiều tài khoản nhất
            </h3>
            <p class="mt-1 text-sm text-muted">
              Một máy dùng nhiều tài khoản chưa chắc là xấu — anh em trong nhà chung máy là chuyện thường. Nhưng đây là
              chỗ kẻ lập hàng loạt tài khoản lộ ra.
            </p>
          </template>
          <AdminBang :rows="data.mayNhieuTk" :cot="cotMayNhieu" ten-csv="may-nhieu-tai-khoan" rong-tieu-de="Chưa có máy nào dùng từ 2 tài khoản trở lên">
            <template #cell-ma_may="{ row }">
              <NuxtLink :to="{ path: '/admin/cammay', query: { q: row.ma_may } }" class="text-primary hover:underline">
                <code class="break-all">{{ row.ma_may }}</code>
              </NuxtLink>
            </template>
            <template #cell-gan_nhat="{ row }">
              <span class="whitespace-nowrap text-xs text-muted">{{ row.gan_nhat }}</span>
            </template>
            <template #cell-nut="{ row }">
              <UButton v-if="duocCam" size="xs" color="error" variant="soft" label="Cấm" :loading="dangLam === row.ma_may" @click="camHayBo(row.ma_may, false)" />
            </template>
          </AdminBang>
        </UCard>

        <UCard :ui="{ body: 'p-3 sm:p-4' }">
          <template #header>
            <h3 class="font-semibold text-highlighted">
              Đang bị cấm
            </h3>
          </template>
          <AdminBang :rows="data.dsCam" :cot="cotCam" ten-csv="may-bi-cam" rong-tieu-de="Chưa cấm máy nào">
            <template #cell-ma_may="{ row }">
              <NuxtLink :to="{ path: '/admin/cammay', query: { q: row.ma_may } }" class="text-primary hover:underline">
                <code class="break-all">{{ row.ma_may }}</code>
              </NuxtLink>
            </template>
            <template #cell-luc="{ row }">
              <span class="whitespace-nowrap text-xs text-muted">{{ row.luc }}</span>
            </template>
            <template #cell-nut="{ row }">
              <UButton v-if="duocCam" size="xs" color="neutral" variant="outline" label="Bỏ cấm" :loading="dangLam === row.ma_may" @click="camHayBo(row.ma_may, true)" />
            </template>
          </AdminBang>
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
