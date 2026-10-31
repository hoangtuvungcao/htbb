<script lang="ts">
/** Một dòng lệnh đã chuẩn bị cho giao diện (lenhChoGiaoDien() phía server). */
export interface DongLenhGiaoDien {
  id: number
  loai?: string
  charname?: string
  cap_do?: number
  admin_user?: string
  trang_thai: number
  ket_qua: string
  tao_luc?: string
  xu_ly_luc?: string | null
  mo_ta: string
  ten_trang_thai: string
  mau: 'success' | 'warning' | 'error' | 'neutral'
  huy_duoc: boolean
}
</script>

<script setup lang="ts">
/**
 * Bảng lệnh đã gửi cho server game (web_lenh_nhan_vat). Dùng chung cho trang Nhân vật,
 * Giảm Level, Nâng Level toàn bộ. Nút "Huỷ" chỉ hiện khi server cho huỷ (huy_duoc).
 */
import type { TableColumn } from '@nuxt/ui'

const props = withDefaults(defineProps<{
  lenh: DongLenhGiaoDien[]
  /** Cột muốn hiện thêm: 'charname' (trang hàng loạt), 'admin' + 'luc' (trang nhân vật). */
  cot?: ('charname' | 'admin' | 'luc')[]
  /** Tiêu đề cột "Yêu cầu". */
  nhanYeuCau?: string
  empty?: string
  dangHuy?: number | null
}>(), { cot: () => ['admin', 'luc'], nhanYeuCau: 'Yêu cầu', empty: 'Chưa có lệnh nào.', dangHuy: null })

const emit = defineEmits<{ huy: [l: DongLenhGiaoDien] }>()

const columns = computed<TableColumn<DongLenhGiaoDien>[]>(() => {
  const c: TableColumn<DongLenhGiaoDien>[] = [{ accessorKey: 'id', header: 'Lệnh' }]
  if (props.cot.includes('charname')) c.push({ accessorKey: 'charname', header: 'Pokémon' })
  c.push({ accessorKey: 'mo_ta', header: props.nhanYeuCau })
  c.push({ accessorKey: 'ten_trang_thai', header: 'Trạng thái' })
  c.push({ accessorKey: 'ket_qua', header: 'Kết quả' })
  if (props.cot.includes('admin')) c.push({ accessorKey: 'admin_user', header: 'Người gửi' })
  if (props.cot.includes('luc')) c.push({ accessorKey: 'tao_luc', header: 'Lúc' })
  c.push({ id: 'thao_tac', header: '' })
  return c
})

const trang = ref(1)
const moiTrang = ref(15)
const tong = computed(() => props.lenh.length)
const lenhHienThi = computed(() => {
  if (moiTrang.value >= props.lenh.length) return props.lenh
  const start = (trang.value - 1) * moiTrang.value
  return props.lenh.slice(start, start + moiTrang.value)
})
watch(tong, (m) => {
  const max = Math.max(1, Math.ceil(m / moiTrang.value))
  if (trang.value > max) trang.value = max
})
</script>

<template>
  <div class="space-y-2">
    <UTable :data="lenhHienThi" :columns="columns" :empty="empty" class="rounded-lg border border-default">
      <template #id-cell="{ row }">
        <span class="text-xs text-muted tabular-nums">#{{ row.original.id }}</span>
      </template>
      <template #charname-cell="{ row }">
        <NuxtLink :to="`/admin/nhanvat?q=${encodeURIComponent(row.original.charname ?? '')}`" class="font-medium text-primary hover:underline">
          {{ row.original.charname }}
        </NuxtLink>
      </template>
      <template #ten_trang_thai-cell="{ row }">
        <UBadge :color="row.original.mau" variant="subtle" :label="row.original.ten_trang_thai" />
      </template>
      <template #ket_qua-cell="{ row }">
        <span class="block min-w-48 max-w-sm whitespace-normal text-xs">{{ row.original.ket_qua }}</span>
      </template>
      <template #admin_user-cell="{ row }">
        <span class="text-xs">{{ row.original.admin_user }}</span>
      </template>
      <template #tao_luc-cell="{ row }">
        <div class="whitespace-nowrap text-xs text-muted">
          <div>{{ row.original.tao_luc }}</div>
          <div v-if="row.original.xu_ly_luc">→ {{ row.original.xu_ly_luc }}</div>
        </div>
      </template>
      <template #thao_tac-cell="{ row }">
        <span v-if="!row.original.huy_duoc" />
        <UButton
          v-else
          size="xs"
          color="neutral"
          variant="outline"
          icon="i-lucide-x"
          label="Huỷ"
          :loading="dangHuy === row.original.id"
          @click="emit('huy', row.original)"
        />
      </template>
    </UTable>
    <div v-if="tong > moiTrang" class="flex items-center justify-between text-xs text-muted px-1">
      <span>Hiển thị {{ (trang - 1) * moiTrang + 1 }} - {{ Math.min(trang * moiTrang, tong) }} trên {{ tong }} lệnh</span>
      <UPagination v-model:page="trang" :total="tong" :items-per-page="moiTrang" />
    </div>
  </div>
</template>
