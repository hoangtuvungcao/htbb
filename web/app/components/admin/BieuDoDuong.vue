<script setup lang="ts">
/**
 * Biểu đồ đường (SVG tự vẽ) cho khu quản trị: 1–4 chuỗi, MỘT trục y, lưới nhạt, đường 2px,
 * rê chuột / chạm có đường dóng + chú giải giá trị; ≥ 2 chuỗi có chú thích (màu không đứng một mình:
 * chú thích ghi tên, tooltip ghi tên + số). Một điểm duy nhất thì vẽ chấm.
 * Màu: bảng phân loại tham chiếu (dataviz palette.md, đã chạy validate_palette — 4 ô đầu, thứ tự cố định):
 *   sáng #2a78d6 / #eb6834 / #1baf7a / #eda100 — tối #3987e5 / #d95926 / #199e70 / #c98500.
 * `nguong`: đường ngưỡng nét đứt (vd 90 giây im lặng, 80% heap).
 */
interface Chuoi { ten: string, gt: number[] }
const props = withDefaults(defineProps<{
  tieuDe: string
  nhan: string[]
  chuoi: Chuoi[]
  donVi?: string
  nguong?: { gt: number, nhan: string } | null
  /** Trục y bắt đầu từ 0 (mặc định có). */
  tuSo0?: boolean
}>(), { donVi: '', nguong: null, tuSo0: true })

const W = 520
const H = 168
const L = 52
const R = 12
const T = 10
const B = 22
const so = (v: unknown) => (typeof v === 'number' && Number.isFinite(v) ? v : null)
const tatCa = computed(() => [...props.chuoi.flatMap(c => c.gt).map(so).filter((v): v is number => v !== null), ...(props.nguong ? [props.nguong.gt] : [])])
const lo = computed(() => (props.tuSo0 ? Math.min(0, ...tatCa.value) : (tatCa.value.length ? Math.min(...tatCa.value) : 0)))
const hi = computed(() => {
  const m = Math.max(...tatCa.value, lo.value + 1)
  return m === lo.value ? lo.value + 1 : m
})
const x = (i: number) => L + (props.nhan.length <= 1 ? (W - L - R) / 2 : (i / (props.nhan.length - 1)) * (W - L - R))
const y = (v: number) => T + (1 - (v - lo.value) / (hi.value - lo.value || 1)) * (H - T - B)
function duong(gt: number[]) {
  let d = ''
  let ke = false
  gt.forEach((v, i) => {
    const s = so(v)
    if (s === null) { ke = false; return }
    d += `${ke ? 'L' : 'M'}${x(i).toFixed(1)},${y(s).toFixed(1)}`
    ke = true
  })
  return d
}
const fmt = (v: number) => Math.abs(v) >= 1e9 ? `${(v / 1e9).toFixed(1)}B` : Math.abs(v) >= 1e6 ? `${(v / 1e6).toFixed(1)}M` : Math.abs(v) >= 1e4 ? `${(v / 1e3).toFixed(0)}k` : v.toLocaleString('vi-VN', { maximumFractionDigits: 2 })
const MAU = ['bd-1', 'bd-2', 'bd-3', 'bd-4']
const tro = ref<number | null>(null)
function di(e: PointerEvent) {
  const r = (e.currentTarget as SVGElement).getBoundingClientRect()
  const px = ((e.clientX - r.left) / r.width) * W
  const n = props.nhan.length
  if (n === 0) return
  const i = n === 1 ? 0 : Math.round(((px - L) / (W - L - R)) * (n - 1))
  tro.value = Math.max(0, Math.min(n - 1, i))
}
const luoi = computed(() => [0, 0.5, 1].map(t => lo.value + t * (hi.value - lo.value)))
const rong = computed(() => !props.nhan.length || !tatCa.value.length)
const moTa = computed(() => `${props.tieuDe}: ${props.chuoi.map((c) => {
  const ds = c.gt.map(so).filter((v): v is number => v !== null)
  return ds.length ? `${c.ten} từ ${fmt(ds[0]!)} đến ${fmt(ds.at(-1)!)}` : `${c.ten} chưa có số`
}).join('; ')}`)
</script>

<template>
  <figure class="min-w-0">
    <figcaption class="flex flex-wrap items-center gap-x-3 gap-y-1 text-sm font-semibold text-highlighted">
      {{ tieuDe }}
      <span v-if="chuoi.length > 1" class="flex flex-wrap gap-x-3 text-xs font-normal text-muted">
        <span v-for="(c, k) in chuoi" :key="c.ten" class="flex items-center gap-1"><span class="h-0.5 w-4 rounded" :class="`${MAU[k]}-nen`" />{{ c.ten }}</span>
      </span>
    </figcaption>
    <p v-if="rong" class="mt-2 flex h-24 items-center justify-center rounded-lg border border-dashed border-default text-sm text-muted">
      Chưa có số liệu trong khoảng này.
    </p>
    <div v-else class="relative mt-1">
      <svg :viewBox="`0 0 ${W} ${H}`" class="w-full touch-none select-none" role="img" :aria-label="moTa" @pointermove="di" @pointerdown="di" @pointerleave="tro = null">
        <g class="text-[10px] tabular-nums" fill="currentColor">
          <template v-for="v in luoi" :key="v">
            <line :x1="L" :x2="W - R" :y1="y(v)" :y2="y(v)" class="stroke-[var(--ui-border-muted)]" stroke-width="1" />
            <text :x="L - 6" :y="y(v) + 3" text-anchor="end" class="fill-[var(--ui-text-dimmed)]">{{ fmt(v) }}</text>
          </template>
          <text v-if="nhan.length" :x="L" :y="H - 6" class="fill-[var(--ui-text-dimmed)]">{{ nhan[0] }}</text>
          <text v-if="nhan.length > 1" :x="W - R" :y="H - 6" text-anchor="end" class="fill-[var(--ui-text-dimmed)]">{{ nhan.at(-1) }}</text>
        </g>
        <g v-if="nguong">
          <line :x1="L" :x2="W - R" :y1="y(nguong.gt)" :y2="y(nguong.gt)" class="stroke-[var(--ui-text-muted)]" stroke-width="1" stroke-dasharray="4 3" />
          <text :x="W - R" :y="y(nguong.gt) - 4" text-anchor="end" class="fill-[var(--ui-text-muted)] text-[10px]">{{ nguong.nhan }}</text>
        </g>
        <template v-for="(c, k) in chuoi" :key="c.ten">
          <path v-if="nhan.length > 1" :d="duong(c.gt)" fill="none" stroke-width="2" stroke-linejoin="round" stroke-linecap="round" :class="`${MAU[k]}-net`" />
          <circle v-else-if="so(c.gt[0]) !== null" :cx="x(0)" :cy="y(c.gt[0]!)" r="4" :class="`${MAU[k]}-nen`" />
        </template>
        <template v-if="tro !== null">
          <line :x1="x(tro)" :x2="x(tro)" :y1="T" :y2="H - B" class="stroke-[var(--ui-text-dimmed)]" stroke-width="1" stroke-dasharray="3 3" />
          <template v-for="(c, k) in chuoi" :key="c.ten">
            <circle v-if="so(c.gt[tro]) !== null" :cx="x(tro)" :cy="y(c.gt[tro]!)" r="4" :class="`${MAU[k]}-nen`" class="stroke-[var(--ui-bg)]" stroke-width="2" />
          </template>
        </template>
        <rect :x="L" :y="T" :width="W - L - R" :height="H - T - B" fill="transparent" />
      </svg>
      <div v-if="tro !== null" class="pointer-events-none absolute top-0 z-10 rounded-lg border border-default bg-default px-2 py-1 text-xs shadow" :style="{ left: `${Math.min(62, Math.max(0, (x(tro) / W) * 100 - 10))}%` }">
        <p class="font-semibold text-highlighted">{{ nhan[tro] }}</p>
        <p v-for="(c, k) in chuoi" :key="c.ten" class="flex items-center gap-1.5 text-toned tabular-nums">
          <span class="size-2 rounded-full" :class="`${MAU[k]}-nen`" />{{ c.ten }}: {{ so(c.gt[tro]) === null ? '—' : c.gt[tro]!.toLocaleString('vi-VN') }}{{ donVi }}
        </p>
      </div>
    </div>
  </figure>
</template>

<style scoped>
.bd-1-net { stroke: #2a78d6; }
.bd-2-net { stroke: #eb6834; }
.bd-3-net { stroke: #1baf7a; }
.bd-4-net { stroke: #eda100; }
.bd-1-nen { background: #2a78d6; fill: #2a78d6; }
.bd-2-nen { background: #eb6834; fill: #eb6834; }
.bd-3-nen { background: #1baf7a; fill: #1baf7a; }
.bd-4-nen { background: #eda100; fill: #eda100; }
.dark .bd-1-net { stroke: #3987e5; }
.dark .bd-2-net { stroke: #d95926; }
.dark .bd-3-net { stroke: #199e70; }
.dark .bd-4-net { stroke: #c98500; }
.dark .bd-1-nen { background: #3987e5; fill: #3987e5; }
.dark .bd-2-nen { background: #d95926; fill: #d95926; }
.dark .bd-3-nen { background: #199e70; fill: #199e70; }
.dark .bd-4-nen { background: #c98500; fill: #c98500; }
</style>
