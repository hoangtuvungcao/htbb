<script setup lang="ts">
/**
 * Ai đang online, đang ở map nào — đọc bảng `tob_vi_tri_online`.
 *
 * Server ghi lại toàn bộ bảng mỗi 30 giây (src/data/GhiViTriOnline.java), nên
 * trang này không có "trang sau" hay lịch sử: luôn là ảnh chụp mới nhất, trễ tối
 * đa 30 giây. Bộ lọc nằm trên URL (?locTen=&locMap=) để chép link gửi nhau được.
 */
definePageMeta({ layout: 'admin', middleware: 'admin' })

const api = useAdminApi()
const route = useRoute()
const router = useRouter()

interface DongViTri { charname: string, map_id: number, map_ten: string, khu: number, x: number, y: number, cap_nhat_luc: string }
interface KetQua { dong: DongViTri[], theoMap: { ten: string, sl: number }[], bangChuaCo: boolean, loiDoc: string }

const tuUrl = (k: string) => (typeof route.query[k] === 'string' ? (route.query[k] as string).trim() : '')
const loc = reactive({ ten: tuUrl('locTen'), map: tuUrl('locMap') })
const dangLoc = computed(() => tuUrl('locTen') !== '' || tuUrl('locMap') !== '')
// URL đổi từ ngoài (bấm lại mục menu, nút Lùi của trình duyệt) thì ô lọc theo URL.
watch(() => [route.query.locTen, route.query.locMap], () => {
  loc.ten = tuUrl('locTen')
  loc.map = tuUrl('locMap')
})

const { data, status, error, refresh } = useAsyncData<KetQua>(
  'admin-online',
  () => api<KetQua>('/api/admin/online', { query: { locTen: tuUrl('locTen'), locMap: tuUrl('locMap') } }),
  { server: false, watch: [() => route.query.locTen, () => route.query.locMap] },
)
const dangTai = computed(() => status.value === 'pending')

function locLai() {
  const query: Record<string, string> = {}
  if (loc.ten.trim()) query.locTen = loc.ten.trim()
  if (loc.map.trim()) query.locMap = loc.map.trim()
  router.replace({ query })
}

function boLoc() {
  loc.ten = ''
  loc.map = ''
  router.replace({ query: {} })
}

const cotPhai = { class: { th: 'text-right', td: 'text-right tabular-nums' } }
const columns = [
  { accessorKey: 'charname', header: 'Nhân vật' },
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
      Vị trí (map + toạ độ) của mọi người chơi đang online — lấy mẫu lại mỗi 30 giây,
      có thể trễ tối đa 30 giây so với thực tế. Không phải log lịch sử, chỉ là ảnh chụp hiện tại.
    </p>

    <UAlert v-if="error" color="error" variant="subtle" icon="i-lucide-circle-alert" :description="loiApi(error)" />
    <UAlert v-else-if="data?.bangChuaCo" color="error" variant="subtle" icon="i-lucide-database">
      <template #description>
        Chưa có bảng <code>tob_vi_tri_online</code> — server cần khởi động lại ít nhất một lần
        sau khi cập nhật bản có tính năng này (server tự tạo bảng lúc khởi động).
      </template>
    </UAlert>
    <UAlert v-else-if="data?.loiDoc" color="error" variant="subtle" icon="i-lucide-circle-alert" :description="`Lỗi đọc dữ liệu: ${data.loiDoc}`" />

    <UCard>
      <form class="flex flex-wrap items-end gap-3" @submit.prevent="locLai">
        <UFormField label="Tên nhân vật (gần đúng)" class="min-w-56 flex-1">
          <UInput v-model="loc.ten" placeholder="vd: hoang" icon="i-lucide-user-search" class="w-full" />
        </UFormField>
        <UFormField label="Tên map (gần đúng)" class="min-w-56 flex-1">
          <UInput v-model="loc.map" placeholder="vd: Trấn Danh" icon="i-lucide-map" class="w-full" />
        </UFormField>
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
        <template #map_ten-cell="{ row }">
          <span class="whitespace-nowrap">{{ row.original.map_ten !== '' ? row.original.map_ten : '?' }}
            <span class="text-muted">({{ row.original.map_id }})</span></span>
        </template>
        <template #cap_nhat_luc-cell="{ row }">
          <span class="whitespace-nowrap text-muted">{{ row.original.cap_nhat_luc }}</span>
        </template>
      </UTable>
    </UCard>
  </AdminPage>
</template>
