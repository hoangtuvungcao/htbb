<script lang="ts">
/** Một vật phẩm đã đọc từ equip/inven/bag (docChuoiVatPham() phía server) + tên. */
export interface VatPhamGiaoDien {
  _i: number
  template: number
  place: number
  plus: number
  level: number
  mau: number
  khoa: boolean
  tinh_luyen: number
  thue_phut: number
  ngay_tao: string
  ten: string | null
  ten_mau: string
}
</script>

<script setup lang="ts">
/**
 * Một bảng vật phẩm của nhân vật. Có `cot` thì hiện nút "Xoá" — phát sự
 * kiện `xoa` để trang cha hỏi xác nhận rồi gửi lệnh xoa_vat_pham cho server game.
 */
import type { TableColumn } from '@nuxt/ui'

const props = defineProps<{
  ds: VatPhamGiaoDien[]
  trong: string
  cot?: 'equip' | 'inven' | 'bag'
  dangXoa?: string | null
}>()
const emit = defineEmits<{ xoa: [v: VatPhamGiaoDien, cot: 'equip' | 'inven' | 'bag'] }>()

const columns = computed<TableColumn<VatPhamGiaoDien>[]>(() => {
  const c: TableColumn<VatPhamGiaoDien>[] = [
    { accessorKey: 'ten', header: 'Vật phẩm' },
    { accessorKey: 'plus', header: '+', meta: { class: { th: 'text-right', td: 'text-right' } } },
    { accessorKey: 'ten_mau', header: 'Màu' },
    { id: 'ghi_chu', header: 'Ghi chú' },
  ]
  if (props.cot) c.push({ id: 'xoa', header: '' })
  return c
})

/** Màu thật của vật phẩm (hằng số COLOR_* trong real/Item.java) — chấm màu cạnh tên màu. */
const CHAM_MAU: Record<number, string> = {
  0: 'bg-ink-100',
  1: 'bg-emerald-400',
  2: 'bg-red-500',
  3: 'bg-sky-400',
  4: 'bg-pink-400',
  5: 'bg-gold-400',
}
</script>

<template>
  <p v-if="ds.length === 0" class="text-sm text-muted">
    {{ trong }}
  </p>
  <UTable v-else :data="ds" :columns="columns" class="rounded-lg border border-default" :ui="{ td: 'py-2' }">
    <template #ten-cell="{ row }">
      <div class="min-w-48 max-w-md whitespace-normal">
        <span :class="row.original.ten ? 'text-highlighted' : 'italic text-muted'">
          {{ row.original.ten ?? '(không có trong data_item)' }}
        </span>
        <span class="ml-1 text-xs text-dimmed tabular-nums">#{{ row.original.template }}</span>
      </div>
    </template>
    <template #plus-cell="{ row }">
      <!-- Luôn render một phần tử: slot rỗng thì UTable hiện giá trị thô (số 0). -->
      <span class="font-semibold tabular-nums text-highlighted">{{ row.original.plus > 0 ? `+${row.original.plus}` : '' }}</span>
    </template>
    <template #ten_mau-cell="{ row }">
      <span class="inline-flex items-center gap-1.5 text-sm">
        <span class="size-2.5 rounded-full ring-1 ring-default" :class="CHAM_MAU[row.original.mau] ?? 'bg-ink-500'" />
        {{ row.original.ten_mau }}
      </span>
    </template>
    <template #ghi_chu-cell="{ row }">
      <div class="flex flex-wrap gap-1">
        <UBadge v-if="row.original.khoa" color="neutral" variant="subtle" size="sm" icon="i-lucide-lock" label="khoá" />
        <UBadge v-if="row.original.tinh_luyen > 0" color="neutral" variant="outline" size="sm" :label="`tinh luyện ${row.original.tinh_luyen}%`" />
        <UBadge v-if="row.original.thue_phut > 0" color="warning" variant="subtle" size="sm" icon="i-lucide-clock" label="có hạn dùng" />
        <UBadge v-if="row.original.level > 0" color="neutral" variant="outline" size="sm" :label="`cấp ${row.original.level}`" />
      </div>
    </template>
    <template #xoa-cell="{ row }">
      <UButton
        v-if="cot"
        size="xs"
        color="error"
        variant="soft"
        icon="i-lucide-trash-2"
        label="Xoá"
        :loading="dangXoa === `${cot}:${row.original._i}`"
        @click="emit('xoa', row.original, cot)"
      />
    </template>
  </UTable>
</template>
