<script setup lang="ts">
/**
 * Nhật ký admin — mọi thao tác trên trang quản trị, kèm người làm và IP.
 * CHỈ ĐỌC: cố ý không có nút xoá — một cuốn nhật ký xoá được thì không còn là
 * bằng chứng.
 */
import type { TableColumn } from '@nuxt/ui'

definePageMeta({ layout: 'admin', middleware: 'admin' })

interface DongNhatKy { id: number, admin_user: string, hanh_dong: string, doi_tuong: string, chi_tiet: string, ip: string, thoi_gian: string }
interface NhatKyResp {
  coBang: boolean
  dbGame: string
  maxFail: number
  failPhut: number
  ai?: string
  viec?: string
  doi?: string
  trang: number
  dsViec: string[]
  dong: DongNhatKy[]
  conTrangSau: boolean
  loiDoc: string
}

const api = useAdminApi()
const route = useRoute()
const router = useRouter()
const qs = (k: string) => (typeof route.query[k] === 'string' ? route.query[k] as string : '')

const { data, status, error, refresh } = await useAsyncData(
  'admin-nhatky',
  () => api<NhatKyResp>('/api/admin/nhatky', {
    query: { ai: qs('ai'), viec: qs('viec'), doi: qs('doi'), trang: qs('trang') || '1' },
  }),
  { server: false, watch: [() => route.fullPath] },
)

// Select của Nuxt UI không nhận giá trị rỗng -> dùng một giá trị canh gác cho "Tất cả".
const TAT_CA = '__tat_ca__'
const ai = ref(qs('ai'))
const viec = ref(qs('viec') || TAT_CA)
const doi = ref(qs('doi'))
watch(data, (d) => {
  if (!d?.coBang) return
  ai.value = d.ai ?? ''
  viec.value = d.viec || TAT_CA
  doi.value = d.doi ?? ''
}, { immediate: true })

const viecItems = computed(() => [
  { label: 'Tất cả', value: TAT_CA },
  ...(data.value?.dsViec ?? []).map(v => ({ label: v, value: v })),
])

function diToi(loc: { ai: string, viec: string, doi: string }, trang: number) {
  const q: Record<string, string> = {}
  if (loc.ai) q.ai = loc.ai
  if (loc.viec) q.viec = loc.viec
  if (loc.doi) q.doi = loc.doi
  if (trang > 1) q.trang = String(trang)
  router.replace({ query: q })
}
/** Bấm "Lọc": lấy bộ lọc từ các ô đang gõ, về trang 1. */
function di() {
  diToi({ ai: ai.value.trim(), viec: viec.value === TAT_CA ? '' : viec.value, doi: doi.value.trim() }, 1)
}
/** Sang trang theo bộ lọc ĐANG ÁP DỤNG (data), không theo ô đang gõ dở. */
function sangTrang(t: number) {
  const d = data.value
  if (!d) return
  diToi({ ai: d.ai ?? '', viec: d.viec ?? '', doi: d.doi ?? '' }, t)
}
function boLoc() {
  ai.value = ''
  viec.value = TAT_CA
  doi.value = ''
  router.replace({ query: {} })
}
const coLoc = computed(() => !!(data.value?.ai || data.value?.viec || data.value?.doi))

/** Chi tiết dài (vd danh sách phần thưởng) gọn 3 dòng, bấm để xem đủ. */
const moRong = ref(new Set<number>())
function doiMoRong(id: number) {
  const s = new Set(moRong.value)
  if (s.has(id)) s.delete(id)
  else s.add(id)
  moRong.value = s
}

/** Việc nào đáng chú ý thì tô màu: khoá/xoá/đăng nhập sai đỏ, mở/tạo xanh. */
function mauViec(h: string): 'error' | 'success' | 'neutral' {
  if (h === 'dang_nhap_that_bai') return 'error'
  if (h.startsWith('khoa_') || h.startsWith('xoa_')) return 'error'
  if (h.startsWith('mo_') || h.startsWith('tao_')) return 'success'
  return 'neutral'
}

const cot: TableColumn<DongNhatKy>[] = [
  { accessorKey: 'id', header: 'ID', meta: { class: { th: 'text-right', td: 'text-right tabular-nums' } } },
  { accessorKey: 'thoi_gian', header: 'Lúc' },
  { accessorKey: 'admin_user', header: 'Người làm' },
  { accessorKey: 'hanh_dong', header: 'Việc' },
  { accessorKey: 'doi_tuong', header: 'Đối tượng' },
  { accessorKey: 'chi_tiet', header: 'Chi tiết' },
  { accessorKey: 'ip', header: 'IP' },
]
</script>

<template>
  <AdminPage title="Nhật ký admin">
    <template #right>
      <UButton icon="i-lucide-refresh-cw" color="neutral" variant="ghost" :loading="status === 'pending'" aria-label="Tải lại" @click="refresh()" />
    </template>

    <UAlert v-if="error" color="error" variant="subtle" icon="i-lucide-circle-x" title="Không tải được dữ liệu" :description="loiApi(error)" />

    <div v-if="!data && status === 'pending'" class="flex items-center gap-2 text-sm text-muted">
      <UIcon name="i-lucide-loader-circle" class="size-4 animate-spin" /> Đang tải nhật ký…
    </div>

    <UCard v-if="data && !data.coBang">
      <template #header>
        <h3 class="font-semibold text-highlighted">
          Chưa có bảng nhật ký
        </h3>
      </template>
      <div class="space-y-2 text-sm">
        <p>Chạy một lần trên máy chủ, vào database <code>{{ data.dbGame }}</code>:</p>
        <pre class="overflow-x-auto rounded-md bg-elevated p-3 font-mono text-xs">mysql -u root {{ data.dbGame }} &lt; sql/them_bang_web_admin_log.sql</pre>
        <p class="text-muted">
          Cho tới lúc đó, mọi thao tác trên trang quản trị vẫn chạy bình thường nhưng không để lại dấu vết,
          và việc chặn dò mật khẩu chỉ đếm trong bộ nhớ tiến trình web — khởi động lại website là hết, gần như vô hiệu.
        </p>
      </div>
    </UCard>

    <template v-else-if="data">
      <p class="text-sm text-muted">
        Mọi thao tác trên trang quản trị, kèm người làm và địa chỉ IP. Không xoá được từ giao diện.
      </p>

      <UCard>
        <form class="flex flex-wrap items-end gap-3" @submit.prevent="di()">
          <UFormField label="Người làm">
            <UInput v-model="ai" placeholder="tất cả" />
          </UFormField>
          <UFormField label="Việc" class="min-w-56">
            <USelect v-model="viec" :items="viecItems" class="w-full" />
          </UFormField>
          <UFormField label="Đối tượng">
            <UInput v-model="doi" placeholder="tên tài khoản / mã" />
          </UFormField>
          <UButton type="submit" icon="i-lucide-filter" label="Lọc" />
          <UButton v-if="coLoc" color="neutral" variant="outline" icon="i-lucide-x" label="Bỏ lọc" @click="boLoc" />
        </form>
      </UCard>

      <UAlert v-if="data.loiDoc" color="error" variant="subtle" icon="i-lucide-database" title="Lỗi đọc dữ liệu" :description="data.loiDoc" />

      <UCard :ui="{ body: 'p-0 sm:p-0' }">
        <UTable :data="data.dong" :columns="cot" :loading="status === 'pending'" class="w-full">
          <template #thoi_gian-cell="{ row }">
            <span class="whitespace-nowrap text-xs text-muted">{{ row.original.thoi_gian }}</span>
          </template>
          <template #admin_user-cell="{ row }">
            <span v-if="row.original.admin_user" class="whitespace-nowrap">{{ row.original.admin_user }}</span>
            <span v-else class="text-dimmed">—</span>
          </template>
          <template #hanh_dong-cell="{ row }">
            <UBadge :color="mauViec(row.original.hanh_dong)" variant="subtle" :label="row.original.hanh_dong" class="whitespace-nowrap" />
          </template>
          <template #doi_tuong-cell="{ row }">
            <span class="whitespace-nowrap">{{ row.original.doi_tuong }}</span>
          </template>
          <template #chi_tiet-cell="{ row }">
            <span
              class="block min-w-64 max-w-xl cursor-pointer whitespace-pre-wrap break-words text-sm"
              :class="{ 'line-clamp-3': !moRong.has(row.original.id) }"
              :title="moRong.has(row.original.id) ? 'Bấm để thu gọn' : 'Bấm để xem đủ'"
              @click="doiMoRong(row.original.id)"
            >{{ row.original.chi_tiet }}</span>
          </template>
          <template #ip-cell="{ row }">
            <span class="whitespace-nowrap text-xs text-muted">{{ row.original.ip }}</span>
          </template>
          <template #empty>
            <span class="text-muted">Chưa có dòng nào khớp.</span>
          </template>
        </UTable>

        <template #footer>
          <div class="flex flex-wrap items-center justify-center gap-3 text-sm text-muted">
            <UButton v-if="data.trang > 1" size="sm" color="neutral" variant="outline" icon="i-lucide-chevron-left" label="Trang trước" @click="sangTrang(data.trang - 1)" />
            <span>Trang {{ data.trang }} · {{ data.dong.length }} dòng</span>
            <UButton v-if="data.conTrangSau" size="sm" color="neutral" variant="outline" trailing-icon="i-lucide-chevron-right" label="Trang sau" @click="sangTrang(data.trang + 1)" />
          </div>
        </template>
      </UCard>

      <UAlert color="neutral" variant="subtle" icon="i-lucide-shield-alert">
        <template #description>
          <p>
            Lọc theo việc <code>dang_nhap_that_bai</code> để xem có ai đang dò mật khẩu trang này không.
            Cùng một IP gõ sai {{ data.maxFail }} lần trong {{ data.failPhut }} phút sẽ bị chặn tạm.
          </p>
          <p class="mt-2">
            Dòng ghi “đúng mật khẩu nhưng không có quyền admin” là dấu hiệu <strong>nặng</strong>:
            có người biết mật khẩu thật của một tài khoản nào đó và đang thử vào khu quản trị.
          </p>
        </template>
      </UAlert>
    </template>
  </AdminPage>
</template>
