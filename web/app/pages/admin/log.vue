<script setup lang="ts">
/**
 * Log game — xem các bảng log mà game server tự ghi trong htbb_db.
 *
 * Bộ lọc nằm trên địa chỉ trang (?bang=&loc=&loc2=&trang=) để trang Chẩn đoán
 * trỏ thẳng vào được (vd /admin/log?bang=tob_log_all_item&loc2=loiinvent).
 * Phân trang không đếm tổng (bảng có thể hàng triệu dòng): chỉ biết còn trang sau hay không.
 */
import type { TableColumn } from '@nuxt/ui'

definePageMeta({ layout: 'admin', middleware: 'admin' })

interface CotLog { key: string, nhan: string, so: boolean }
interface LogResp {
  bang: string
  bangThat: string
  /** Bảng tách theo kỳ (tuần / tháng): danh sách kỳ có dữ liệu, mới nhất trước. */
  dsKy: { bang: string, nhan: string }[]
  ky: string
  dsBang: { key: string, ten: string }[]
  cauHinh: { ten: string, mota: string, cot: CotLog[], loc: string | null, loc2: string | null, nhanLoc: string | null, nhanLoc2: string | null }
  loc: string
  loc2: string
  trang: number
  dong: Record<string, unknown>[]
  conTrangSau: boolean
  loiDoc: string
}

const api = useAdminApi()
const route = useRoute()
const router = useRouter()

const qs = (k: string) => (typeof route.query[k] === 'string' ? route.query[k] as string : '')

const { data, status, error, refresh } = await useAsyncData(
  'admin-log',
  () => api<LogResp>('/api/admin/log', {
    query: { bang: qs('bang'), ky: qs('ky'), loc: qs('loc'), loc2: qs('loc2'), trang: qs('trang') || '1' },
  }),
  { server: false, watch: [() => route.fullPath] },
)

const bang = ref(qs('bang'))
const loc = ref(qs('loc'))
const loc2 = ref(qs('loc2'))
watch(data, (d) => {
  if (!d) return
  bang.value = d.bang
  loc.value = d.loc
  loc2.value = d.loc2
}, { immediate: true })

function di(query: Record<string, string | number>) {
  const sach: Record<string, string> = {}
  for (const [k, v] of Object.entries(query)) {
    if (v !== '' && !(k === 'trang' && Number(v) <= 1)) sach[k] = String(v)
  }
  router.replace({ query: sach })
}

function xem() {
  di({ bang: bang.value, loc: loc.value.trim(), loc2: loc2.value.trim() })
}
function doiBang(v: string) {
  // Đổi bảng là xem ngay, giữ nguyên ô lọc đang gõ (bảng không có cột lọc đó thì server bỏ qua).
  bang.value = v
  xem()
}
function boLoc() {
  di({ bang: bang.value })
}
function doiKy(v: string) {
  di({ bang: bang.value, ky: v, loc: loc.value.trim(), loc2: loc2.value.trim() })
}
const dsKyItems = computed(() => (data.value?.dsKy ?? []).map(k => ({ label: k.nhan, value: k.bang })))
function sangTrang(t: number) {
  // Sang trang theo bộ lọc ĐANG ÁP DỤNG (data), không theo ô đang gõ dở.
  const d = data.value
  if (!d) return
  di({ bang: d.bang, ky: d.ky, loc: d.loc, loc2: d.loc2, trang: t })
}

const dsBangItems = computed(() => (data.value?.dsBang ?? []).map(b => ({ label: b.ten, value: b.key })))
const coLoc = computed(() => !!(data.value?.loc || data.value?.loc2))

const cot = computed<TableColumn<Record<string, unknown>>[]>(() =>
  (data.value?.cauHinh.cot ?? []).map(c => ({
    accessorKey: c.key,
    header: c.nhan,
    meta: c.so ? { class: { th: 'text-right', td: 'text-right tabular-nums' } } : undefined,
  })),
)

const gt = (r: Record<string, unknown>, k: string) => (r[k] === null || r[k] === undefined ? '' : String(r[k]))
const laTen = (k: string) => k === 'charname' || k === 'charname2'
/** Cột mặc định của server là chuỗi hai dấu nháy `""` — coi như trống. */
const tenRong = (v: string) => v === '' || v === '""'
</script>

<template>
  <AdminPage title="Log game">
    <template #right>
      <UButton icon="i-lucide-refresh-cw" color="neutral" variant="ghost" :loading="status === 'pending'" aria-label="Tải lại" @click="refresh()" />
    </template>

    <p v-if="data" class="text-sm text-muted">
      {{ data.cauHinh.mota }}
    </p>

    <UCard>
      <form class="flex flex-wrap items-end gap-3" @submit.prevent="xem">
        <UFormField label="Bảng" class="min-w-56">
          <USelect :model-value="bang" :items="dsBangItems" class="w-full" @update:model-value="(v: any) => doiBang(String(v))" />
        </UFormField>
        <UFormField v-if="dsKyItems.length" label="Kỳ" class="min-w-44">
          <USelect :model-value="data?.ky" :items="dsKyItems" class="w-full" @update:model-value="(v: any) => doiKy(String(v))" />
        </UFormField>
        <UFormField v-if="data?.cauHinh.loc" :label="data.cauHinh.nhanLoc ?? 'Lọc'">
          <UInput v-model="loc" placeholder="tên chính xác" />
        </UFormField>
        <UFormField v-if="data?.cauHinh.loc2" :label="data.cauHinh.nhanLoc2 ?? 'Lọc 2'">
          <UInput v-model="loc2" placeholder="vd: loiinvent" />
        </UFormField>
        <UButton type="submit" icon="i-lucide-search" label="Xem" />
        <UButton v-if="coLoc" color="neutral" variant="outline" icon="i-lucide-x" label="Bỏ lọc" @click="boLoc" />
      </form>
    </UCard>

    <UAlert v-if="error" color="error" variant="subtle" icon="i-lucide-circle-x" title="Không tải được dữ liệu" :description="loiApi(error)" />

    <div v-if="!data && status === 'pending'" class="flex items-center gap-2 text-sm text-muted">
      <UIcon name="i-lucide-loader-circle" class="size-4 animate-spin" /> Đang đọc log…
    </div>

    <UAlert v-if="data?.loiDoc" color="error" variant="subtle" icon="i-lucide-database" :title="`Không đọc được bảng ${data.bangThat}`">
      <template #description>
        {{ data.loiDoc }}
        <br><span class="text-xs">Bảng này có thể chưa tồn tại trên máy chủ của bạn — không sao, chọn bảng khác.</span>
      </template>
    </UAlert>

    <UAlert v-if="data?.bangThat === 'tob_log_all_item'" color="neutral" variant="subtle" icon="i-lucide-info">
      <template #description>
        Cột <strong>Loại</strong> (<code>aclog</code>) là chỗ đáng soi nhất. Gõ <code>loiinvent</code>
        vào ô lọc để tìm những Pokémon lưu hành trang thất bại — đó là nguyên nhân khiến server
        <em>tự</em> đặt <code>ban = 1</code> và Pokémon biến mất khỏi màn chọn.
      </template>
    </UAlert>

    <UCard v-if="data" :ui="{ body: 'p-0 sm:p-0' }">
      <template #header>
        <h3 class="flex flex-wrap items-center gap-2 font-semibold text-highlighted">
          {{ data.cauHinh.ten }} · <code class="text-sm">{{ data.bangThat }}</code>
          <span v-if="data.bangThat !== data.bang && !data.ky" class="text-xs font-normal text-muted">(view lọc sẵn từ {{ data.bang }})</span>
        </h3>
      </template>

      <UTable :data="data.dong" :columns="cot" :loading="status === 'pending'" class="w-full">
        <template v-for="c in data.cauHinh.cot" :key="c.key" #[`${c.key}-cell`]="{ row }">
          <span v-if="c.so" class="tabular-nums">{{ so(gt(row.original, c.key)) }}</span>
          <template v-else-if="laTen(c.key)">
            <span v-if="tenRong(gt(row.original, c.key))" class="text-dimmed">—</span>
            <NuxtLink v-else :to="{ path: '/admin/nhanvat', query: { q: gt(row.original, c.key) } }" class="whitespace-nowrap text-primary hover:underline">
              {{ gt(row.original, c.key) }}
            </NuxtLink>
          </template>
          <span v-else-if="c.key === 'info'" class="block min-w-64 max-w-xl whitespace-pre-wrap break-words text-sm">{{ gt(row.original, c.key) }}</span>
          <span v-else-if="gt(row.original, c.key).length > 40" class="block min-w-48 max-w-xs break-all text-xs">{{ gt(row.original, c.key) }}</span>
          <span v-else class="whitespace-nowrap">{{ gt(row.original, c.key) }}</span>
        </template>
        <template #empty>
          <span class="text-muted">{{ data.loiDoc ? '' : 'Không có dòng nào khớp.' }}</span>
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

    <p class="text-sm text-muted">
      Nhật ký nhận giftcode nằm ở trang
      <NuxtLink :to="{ path: '/admin/giftcode', query: { tab: 'log' } }" class="text-primary hover:underline">Giftcode → Ai đã nhận</NuxtLink>.
      Thao tác của chính các admin nằm ở <NuxtLink to="/admin/nhatky" class="text-primary hover:underline">Nhật ký admin</NuxtLink>.
    </p>
  </AdminPage>
</template>
