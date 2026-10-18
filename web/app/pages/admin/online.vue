<script setup lang="ts">
/**
 * Ai đang online, đang ở map nào, dùng client nào — đọc bảng `tob_vi_tri_online`.
 *
 * Server ghi lại toàn bộ bảng mỗi 30 giây (src/data/GhiViTriOnline.java), nên
 * phần "đang online" không có "trang sau" hay lịch sử: luôn là ảnh chụp mới nhất, trễ tối
 * đa 30 giây. Bộ lọc nằm trên URL (?locTen=&locMap=&banNgoai=1) để chép link gửi nhau được.
 *
 * Client: PC / Android / jar và BẢN CHÍNH THỨC (NQSH-H2 của mình) hay BẢN NGOÀI (client gốc TeaMobi,
 * client lạ). Phần dưới (AdminThongKeClient) là lịch sử đăng nhập theo client + danh sách tài khoản
 * từng dùng bản ngoài để soát và khoá.
 */
definePageMeta({ layout: 'admin', middleware: 'admin' })

const api = useAdminApi()
const route = useRoute()
const router = useRouter()

interface DongViTri {
  charname: string, map_id: number, map_ten: string, khu: number, x: number, y: number, cap_nhat_luc: string
  tai_khoan?: string, client?: string, chinh_chu?: number, client_ban?: string, platf?: string
}
interface TheoClient { pc: number, android: number, jar: number, khac: number, chinhChu: number, banNgoai: number }
interface KetQua {
  dong: DongViTri[], theoMap: { ten: string, sl: number }[], theoClient: TheoClient, coCotClient: boolean
  bangChuaCo: boolean, loiDoc: string
}

const tuUrl = (k: string) => (typeof route.query[k] === 'string' ? (route.query[k] as string).trim() : '')
const loc = reactive({ ten: tuUrl('locTen'), map: tuUrl('locMap'), banNgoai: tuUrl('banNgoai') === '1' })
const dangLoc = computed(() => tuUrl('locTen') !== '' || tuUrl('locMap') !== '' || tuUrl('banNgoai') === '1')
// URL đổi từ ngoài (bấm lại mục menu, nút Lùi của trình duyệt) thì ô lọc theo URL.
watch(() => [route.query.locTen, route.query.locMap, route.query.banNgoai], () => {
  loc.ten = tuUrl('locTen')
  loc.map = tuUrl('locMap')
  loc.banNgoai = tuUrl('banNgoai') === '1'
})

const { data, status, error, refresh } = useAsyncData<KetQua>(
  'admin-online',
  () => api<KetQua>('/api/admin/online', { query: { locTen: tuUrl('locTen'), locMap: tuUrl('locMap'), banNgoai: tuUrl('banNgoai') } }),
  { server: false, watch: [() => route.query.locTen, () => route.query.locMap, () => route.query.banNgoai] },
)
const dangTai = computed(() => status.value === 'pending')

function locLai() {
  const query: Record<string, string> = {}
  if (loc.ten.trim()) query.locTen = loc.ten.trim()
  if (loc.map.trim()) query.locMap = loc.map.trim()
  if (loc.banNgoai) query.banNgoai = '1'
  router.replace({ query })
}

function boLoc() {
  loc.ten = ''
  loc.map = ''
  loc.banNgoai = false
  router.replace({ query: {} })
}

function chiBanNgoai() {
  loc.banNgoai = true
  locLai()
}

const TEN_CLIENT: Record<string, string> = { pc: 'PC', android: 'Android', jar: 'Jar', khac: 'Khác' }
const ICON_CLIENT: Record<string, string> = { pc: 'i-lucide-monitor', android: 'i-lucide-smartphone', jar: 'i-lucide-cpu', khac: 'i-lucide-circle-help' }

const cotPhai = { class: { th: 'text-right', td: 'text-right tabular-nums' } }
const columns = [
  { accessorKey: 'charname', header: 'Nhân vật' },
  { accessorKey: 'tai_khoan', header: 'Tài khoản' },
  { accessorKey: 'client', header: 'Client' },
  { accessorKey: 'map_ten', header: 'Map' },
  { accessorKey: 'khu', header: 'Khu', meta: cotPhai },
  { accessorKey: 'x', header: 'X', meta: cotPhai },
  { accessorKey: 'y', header: 'Y', meta: cotPhai },
  { accessorKey: 'cap_nhat_luc', header: 'Cập nhật lúc' },
]
</script>

<template>
  <AdminPage title="Ai đang ở đâu">
    <template #right>
      <UButton icon="i-lucide-refresh-cw" color="neutral" variant="outline" size="sm" :loading="dangTai" @click="refresh()">
        Làm mới
      </UButton>
    </template>

    <p class="text-sm text-muted">
      Vị trí (map + toạ độ) và client của mọi người chơi đang online — lấy mẫu lại mỗi 30 giây,
      có thể trễ tối đa 30 giây so với thực tế. <b>Bản ngoài</b> = client không phải bản NQSH-H2 của bạn
      (client gốc TeaMobi hoặc client lạ — không gửi mã máy lúc đăng nhập).
    </p>

    <UAlert v-if="error" color="error" variant="subtle" icon="i-lucide-circle-alert" :description="loiApi(error)" />
    <UAlert v-else-if="data?.bangChuaCo" color="error" variant="subtle" icon="i-lucide-database">
      <template #description>
        Chưa có bảng <code>tob_vi_tri_online</code> — server cần khởi động lại ít nhất một lần
        sau khi cập nhật bản có tính năng này (server tự tạo bảng lúc khởi động).
      </template>
    </UAlert>
    <UAlert v-else-if="data?.loiDoc" color="error" variant="subtle" icon="i-lucide-circle-alert" :description="`Lỗi đọc dữ liệu: ${data.loiDoc}`" />

    <UAlert
      v-if="data?.coCotClient && data.theoClient.banNgoai > 0 && !loc.banNgoai"
      color="warning" variant="subtle" icon="i-lucide-shield-alert"
      :title="`${so(data.theoClient.banNgoai)} người đang online bằng bản ngoài`"
    >
      <template #description>
        <UButton size="xs" color="warning" variant="outline" icon="i-lucide-filter" class="mt-1" @click="chiBanNgoai">
          Chỉ hiện những người này
        </UButton>
      </template>
    </UAlert>

    <UCard>
      <form class="flex flex-wrap items-end gap-3" @submit.prevent="locLai">
        <UFormField label="Tên nhân vật (gần đúng)" class="min-w-56 flex-1">
          <UInput v-model="loc.ten" placeholder="vd: hoang" icon="i-lucide-user-search" class="w-full" />
        </UFormField>
        <UFormField label="Tên map (gần đúng)" class="min-w-56 flex-1">
          <UInput v-model="loc.map" placeholder="vd: Trấn Danh" icon="i-lucide-map" class="w-full" />
        </UFormField>
        <UCheckbox v-model="loc.banNgoai" label="Chỉ bản ngoài" class="pb-2" />
        <UButton type="submit" icon="i-lucide-filter">
          Lọc
        </UButton>
        <UButton v-if="dangLoc" color="neutral" variant="outline" icon="i-lucide-x" @click="boLoc">
          Bỏ lọc
        </UButton>
      </form>
    </UCard>

    <UCard :ui="{ body: 'p-0 sm:p-0' }">
      <template #header>
        <div class="space-y-2">
          <h3 class="font-semibold text-highlighted">
            Tổng {{ so(data?.dong.length ?? 0) }} người đang online{{ dangLoc ? ' (đã lọc)' : '' }}
          </h3>
          <div v-if="data?.coCotClient" class="flex flex-wrap items-center gap-1.5 text-sm text-muted">
            <span>Theo client:</span>
            <UBadge v-for="k in ['pc', 'android', 'jar']" :key="k" :icon="ICON_CLIENT[k]" color="neutral" variant="subtle">
              {{ TEN_CLIENT[k] }} <span class="ms-1 tabular-nums text-highlighted">{{ data.theoClient[k as 'pc'] }}</span>
            </UBadge>
            <UBadge color="success" variant="subtle">
              Bản của bạn <span class="ms-1 tabular-nums">{{ data.theoClient.chinhChu }}</span>
            </UBadge>
            <UBadge :color="data.theoClient.banNgoai > 0 ? 'error' : 'neutral'" variant="subtle">
              Bản ngoài <span class="ms-1 tabular-nums">{{ data.theoClient.banNgoai }}</span>
            </UBadge>
          </div>
          <div v-if="data?.theoMap.length" class="flex flex-wrap items-center gap-1.5 text-sm text-muted">
            <span>Theo map:</span>
            <UBadge v-for="m in data.theoMap" :key="m.ten" color="neutral" variant="subtle">
              {{ m.ten }} <span class="ms-1 tabular-nums text-highlighted">{{ m.sl }}</span>
            </UBadge>
          </div>
        </div>
      </template>
      <UTable :data="data?.dong ?? []" :columns="columns" :loading="dangTai">
        <template #empty>
          <span v-if="!data?.loiDoc" class="text-muted">Không có ai khớp bộ lọc (hoặc không ai đang online).</span>
          <span v-else />
        </template>
        <template #charname-cell="{ row }">
          <NuxtLink :to="`/admin/nhanvat?q=${encodeURIComponent(row.original.charname)}`" class="whitespace-nowrap font-medium text-primary hover:underline">
            {{ row.original.charname }}
          </NuxtLink>
        </template>
        <template #tai_khoan-cell="{ row }">
          <NuxtLink
            v-if="row.original.tai_khoan" :to="`/admin/taikhoan?q=${encodeURIComponent(row.original.tai_khoan)}`"
            class="whitespace-nowrap text-primary hover:underline"
          >
            {{ row.original.tai_khoan }}
          </NuxtLink>
          <span v-else class="text-muted">?</span>
        </template>
        <template #client-cell="{ row }">
          <span v-if="!row.original.client" class="text-muted">?</span>
          <span v-else class="flex items-center gap-1.5 whitespace-nowrap">
            <UBadge :icon="ICON_CLIENT[row.original.client] ?? ICON_CLIENT.khac" color="neutral" variant="subtle">
              {{ TEN_CLIENT[row.original.client] ?? row.original.client }}{{ row.original.client_ban ? ` ${row.original.client_ban}` : '' }}
            </UBadge>
            <UBadge v-if="Number(row.original.chinh_chu) === 1" color="success" variant="subtle">Bản của bạn</UBadge>
            <UBadge v-else color="error" variant="solid" :title="row.original.platf">Bản ngoài</UBadge>
          </span>
        </template>
        <template #map_ten-cell="{ row }">
          <span class="whitespace-nowrap">{{ row.original.map_ten !== '' ? row.original.map_ten : '?' }}
            <span class="text-muted">({{ row.original.map_id }})</span></span>
        </template>
        <template #cap_nhat_luc-cell="{ row }">
          <span class="whitespace-nowrap text-muted">{{ row.original.cap_nhat_luc }}</span>
        </template>
      </UTable>
    </UCard>

    <AdminThongKeClient />
  </AdminPage>
</template>
