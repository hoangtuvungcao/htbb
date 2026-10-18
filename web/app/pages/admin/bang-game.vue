<script setup lang="ts">
/** Bảng kết quả trong game (chỉ đọc): chiếm Gym, chiến trường, quà online, giftcode... */
definePageMeta({ layout: 'admin', middleware: 'admin' })
const api = useAdminApi()
const route = useRoute()
const router = useRouter()

interface KetQua { bang: { ten: string, moTa: string }[], dong?: Record<string, unknown>[] }
const chon = computed({
  get: () => (typeof route.query.bang === 'string' ? route.query.bang : ''),
  set: (v: string) => router.replace({ query: { ...route.query, bang: v || undefined } }),
})
const { data, status, error, refresh } = useAsyncData('admin-bang-game', () => api<KetQua>('/api/admin/bang-game', { query: { bang: chon.value || undefined } }), { server: false, watch: [chon] })

const cot = computed(() => {
  const dong = data.value?.dong ?? []
  const keys = Object.keys(dong[0] ?? {})
  return keys.map(k => ({
    key: k,
    label: k,
    sort: true,
    so: dong.length > 0 && dong.every(r => r[k] === null || (typeof r[k] === 'number') || (typeof r[k] === 'string' && /^-?\d+(\.\d+)?$/.test(r[k] as string) && (r[k] as string).length < 16)),
  }))
})
const moTa = computed(() => data.value?.bang.find(b => b.ten === chon.value)?.moTa ?? '')
</script>

<template>
  <AdminPage title="Kết quả trong game">
    <template #right>
      <UButton icon="i-lucide-refresh-cw" color="neutral" variant="ghost" :loading="status === 'pending'" aria-label="Tải lại" @click="refresh()" />
    </template>

    <div>
      <h1 class="text-2xl font-bold text-highlighted">Kết quả trong game</h1>
      <p class="mt-1 text-sm text-muted">
        Chỉ đọc. Gồm các bảng kết quả có trong CSDL game (chiếm Gym tuần, quà online hằng ngày... xuất hiện khi máy chủ tạo bảng).
        Mỗi bảng hiện tối đa 200 dòng. Số liệu gốc (vật phẩm, quái, nhiệm vụ...) xem ở <NuxtLink to="/admin/du-lieu-game" class="text-primary hover:underline">Dữ liệu game</NuxtLink>.
      </p>
    </div>

    <UAlert v-if="error && !data" color="error" variant="subtle" icon="i-lucide-circle-x" title="Không tải được danh sách bảng" :description="loiApi(error)"
      :actions="[{ label: 'Thử lại', color: 'error', variant: 'outline', onClick: () => refresh() }]" />
    <div v-else-if="!data && status === 'pending'" class="flex flex-wrap gap-2">
      <USkeleton v-for="i in 5" :key="i" class="h-8 w-40" />
    </div>
    <template v-else-if="data">
      <p v-if="!data.bang.length" class="text-sm text-muted">Chưa có bảng kết quả nào trong CSDL.</p>
      <div v-else class="flex flex-wrap gap-2">
        <UButton v-for="b in data.bang" :key="b.ten" :color="chon === b.ten ? 'primary' : 'neutral'" :variant="chon === b.ten ? 'solid' : 'outline'" size="sm" :label="b.moTa" @click="chon = b.ten" />
      </div>

      <UCard v-if="chon">
        <template #header>
          <div class="flex flex-wrap items-baseline gap-x-3">
            <h2 class="font-semibold text-highlighted">{{ moTa || chon }}</h2>
            <code class="text-xs text-muted">{{ chon }}</code>
          </div>
        </template>
        <AdminBang :rows="data.dong" :cot="cot" :dang-tai="status === 'pending'" :loi="error" :ten-csv="chon" rong-tieu-de="Bảng trống" @thu-lai="refresh()" />
      </UCard>
      <p v-else-if="data.bang.length" class="text-sm text-muted">Chọn một bảng ở trên để xem.</p>
    </template>
  </AdminPage>
</template>
