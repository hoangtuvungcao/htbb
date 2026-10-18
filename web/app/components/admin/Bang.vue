<script setup lang="ts" generic="T extends Record<string, any>">
/**
 * Bảng dữ liệu chung của khu quản trị — lọc, sắp xếp, phân trang, xuất CSV, trạng thái
 * đang tải / lỗi / trống. Làm việc trên dữ liệu đã có ở trình duyệt (phân trang phía client);
 * bảng lớn thì API tự giới hạn số dòng rồi đưa vào đây.
 *
 *   <AdminBang :rows="ds" :cot="[{ key: 'ten', label: 'Tên', sort: true }, { key: 'cap', label: 'Cấp', sort: true, so: true }]"
 *              :dang-tai="pending" :loi="error" ten-csv="nguoi-choi" tim-placeholder="Tìm tên…">
 *     <template #cell-ten="{ row }"><b>{{ row.ten }}</b></template>
 *   </AdminBang>
 */
export interface CotBang {
  key: string
  label: string
  /** Cho bấm tiêu đề để sắp xếp. */
  sort?: boolean
  /** Cột số: căn phải, định dạng có dấu chấm nghìn, sắp xếp theo số. */
  so?: boolean
  class?: string
  /** Giá trị dùng cho CSV / tìm kiếm / sắp xếp (mặc định row[key]). */
  giaTri?: (row: any) => unknown
  /** Ẩn trên màn hình hẹp. */
  anMobile?: boolean
}

const props = withDefaults(defineProps<{
  rows: T[] | null | undefined
  cot: CotBang[]
  dangTai?: boolean
  loi?: unknown
  tenCsv?: string
  timPlaceholder?: string
  /** Bỏ ô tìm (khi trang tự lọc phía server). */
  khongTim?: boolean
  moiTrang?: number
  rongTieuDe?: string
  rongMoTa?: string
  /** Khoá duy nhất của dòng (mặc định chỉ số). */
  rowKey?: string
}>(), { moiTrang: 25, rongTieuDe: 'Chưa có dữ liệu', timPlaceholder: 'Lọc trong bảng…' })

const emit = defineEmits<{ thuLai: [] }>()

const tim = ref('')
const sapXep = ref<{ key: string, giam: boolean } | null>(null)
const trang = ref(1)

const gt = (c: CotBang, r: any) => (c.giaTri ? c.giaTri(r) : r?.[c.key])
const boDau = (s: string) => s.normalize('NFD').replace(/[̀-ͯ]/g, '').replace(/đ/g, 'd').replace(/Đ/g, 'D').toLowerCase()

const daLoc = computed(() => {
  const ds = props.rows ?? []
  const t = boDau(tim.value.trim())
  if (!t) return ds
  return ds.filter(r => props.cot.some(c => boDau(String(gt(c, r) ?? '')).includes(t)))
})

const daSap = computed(() => {
  const s = sapXep.value
  if (!s) return daLoc.value
  const c = props.cot.find(x => x.key === s.key)
  if (!c) return daLoc.value
  const ds = [...daLoc.value]
  ds.sort((a, b) => {
    const x = gt(c, a), y = gt(c, b)
    let k: number
    if (c.so) k = Number(x ?? 0) - Number(y ?? 0)
    else k = String(x ?? '').localeCompare(String(y ?? ''), 'vi', { numeric: true })
    return s.giam ? -k : k
  })
  return ds
})

const trangHienTai = computed(() => {
  const bd = (trang.value - 1) * props.moiTrang
  return daSap.value.slice(bd, bd + props.moiTrang)
})

watch([tim, () => props.rows], () => { trang.value = 1 })

function bamSap(c: CotBang) {
  if (!c.sort) return
  const s = sapXep.value
  sapXep.value = !s || s.key !== c.key ? { key: c.key, giam: !!c.so } : s.giam === !!c.so ? { key: c.key, giam: !c.so } : null
}

function ariaSort(c: CotBang): 'ascending' | 'descending' | 'none' | undefined {
  if (!c.sort) return undefined
  if (sapXep.value?.key !== c.key) return 'none'
  return sapXep.value.giam ? 'descending' : 'ascending'
}

function xuatCsv() {
  const esc = (v: unknown) => {
    const s = v == null ? '' : String(v)
    // Chống chèn công thức khi mở bằng Excel (=, +, -, @ đầu ô)
    const an = /^[=+\-@\t\r]/.test(s) ? `'${s}` : s
    return /[",\n;]/.test(an) ? `"${an.replace(/"/g, '""')}"` : an
  }
  const dong = [props.cot.map(c => esc(c.label)).join(',')]
  for (const r of daSap.value) dong.push(props.cot.map(c => esc(gt(c, r))).join(','))
  const blob = new Blob([`﻿${dong.join('\r\n')}`], { type: 'text/csv;charset=utf-8' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  const d = new Date()
  a.download = `${props.tenCsv ?? 'bang'}-${d.getFullYear()}${String(d.getMonth() + 1).padStart(2, '0')}${String(d.getDate()).padStart(2, '0')}.csv`
  a.click()
  setTimeout(() => URL.revokeObjectURL(a.href), 1000)
}

const fmt = (c: CotBang, r: any) => {
  const v = gt(c, r)
  if (c.so && v != null && v !== '' && Number.isFinite(Number(v))) return Number(v).toLocaleString('vi-VN')
  return v ?? '—'
}
const loiChu = computed(() => (props.loi ? loiApi(props.loi) : ''))
</script>

<template>
  <div class="space-y-3">
    <div v-if="!khongTim || tenCsv || $slots.loc" class="flex flex-wrap items-center gap-2">
      <UInput v-if="!khongTim" v-model="tim" icon="i-lucide-search" :placeholder="timPlaceholder" class="w-full sm:w-72" :aria-label="timPlaceholder" />
      <slot name="loc" />
      <span class="ml-auto text-xs text-muted tabular-nums">{{ daLoc.length.toLocaleString('vi-VN') }} dòng</span>
      <UButton v-if="tenCsv" size="sm" color="neutral" variant="outline" icon="i-lucide-download" label="Xuất CSV" :disabled="!daSap.length" @click="xuatCsv" />
    </div>

    <UAlert v-if="loiChu" color="error" variant="subtle" icon="i-lucide-triangle-alert" :title="loiChu"
      :actions="[{ label: 'Thử lại', color: 'error', variant: 'outline', onClick: () => emit('thuLai') }]" />

    <div v-else class="overflow-x-auto rounded-lg border border-default">
      <table class="bang-wiki">
        <thead>
          <tr>
            <th v-for="c in cot" :key="c.key" :aria-sort="ariaSort(c)" :class="[c.so && 'text-right', c.anMobile && 'max-md:hidden', c.class]">
              <button v-if="c.sort" type="button" class="inline-flex items-center gap-1 uppercase hover:text-highlighted focus-visible:outline-2 focus-visible:outline-primary" @click="bamSap(c)">
                {{ c.label }}
                <UIcon :name="sapXep?.key !== c.key ? 'i-lucide-chevrons-up-down' : sapXep.giam ? 'i-lucide-arrow-down' : 'i-lucide-arrow-up'" class="size-3.5" />
              </button>
              <template v-else>{{ c.label }}</template>
            </th>
          </tr>
        </thead>
        <tbody>
          <template v-if="dangTai && !(rows?.length)">
            <tr v-for="i in 5" :key="`sk${i}`">
              <td v-for="c in cot" :key="c.key" :class="c.anMobile && 'max-md:hidden'"><USkeleton class="h-4 w-full max-w-40" /></td>
            </tr>
          </template>
          <tr v-else-if="!daSap.length">
            <td :colspan="cot.length" class="py-10 text-center">
              <UIcon name="i-lucide-inbox" class="mx-auto size-8 text-dimmed" />
              <p class="mt-2 font-semibold text-highlighted">{{ tim ? 'Không có dòng nào khớp' : rongTieuDe }}</p>
              <p v-if="!tim && rongMoTa" class="mt-1 text-sm text-muted">{{ rongMoTa }}</p>
            </td>
          </tr>
          <tr v-for="(r, i) in trangHienTai" v-else :key="rowKey ? r[rowKey] : i">
            <td v-for="c in cot" :key="c.key" :class="[c.so && 'text-right tabular-nums', c.anMobile && 'max-md:hidden', c.class]">
              <slot :name="`cell-${c.key}`" :row="r" :value="gt(c, r)">{{ fmt(c, r) }}</slot>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="daSap.length > moiTrang" class="flex justify-center">
      <UPagination v-model:page="trang" :total="daSap.length" :items-per-page="moiTrang" />
    </div>
  </div>
</template>
