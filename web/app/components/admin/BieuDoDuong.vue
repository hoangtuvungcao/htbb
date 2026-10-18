<script setup lang="ts">
/**
 * Biểu đồ đường nhỏ (SVG) cho khu quản trị: 1–2 chuỗi, một trục y, lưới nhạt, đường 2px,
 * rê chuột có đường dóng + chú giải giá trị; ≥ 2 chuỗi có chú thích + nhãn cuối đường.
 * Màu đã kiểm bằng validate_palette (sáng #2c64ad/#e3350d, tối #4f88d6/#ec5432).
 */
interface Chuoi { ten: string, gt: number[] }
const props = defineProps<{ tieuDe: string, nhan: string[], chuoi: Chuoi[], donVi?: string }>()
const W = 520
const H = 160
const L = 48
const R = 12
const T = 10
const B = 22
const tatCa = computed(() => props.chuoi.flatMap(c => c.gt).filter(Number.isFinite))
const lo = computed(() => Math.min(0, ...tatCa.value))
const hi = computed(() => Math.max(1, ...tatCa.value))
const x = (i: number) => L + (props.nhan.length <= 1 ? 0 : (i / (props.nhan.length - 1)) * (W - L - R))
const y = (v: number) => T + (1 - (v - lo.value) / (hi.value - lo.value || 1)) * (H - T - B)
const duong = (gt: number[]) => gt.map((v, i) => `${i ? 'L' : 'M'}${x(i).toFixed(1)},${y(v).toFixed(1)}`).join(' ')
const fmt = (v: number) => Math.abs(v) >= 1e9 ? `${(v / 1e9).toFixed(1)}B` : Math.abs(v) >= 1e6 ? `${(v / 1e6).toFixed(1)}M` : Math.abs(v) >= 1e4 ? `${(v / 1e3).toFixed(0)}k` : v.toLocaleString('vi-VN', { maximumFractionDigits: 2 })
const MAU = ['bd-1', 'bd-2']
const tro = ref<number | null>(null)
function di(e: MouseEvent) {
  const r = (e.currentTarget as SVGElement).getBoundingClientRect()
  const px = ((e.clientX - r.left) / r.width) * W
  const i = Math.round(((px - L) / (W - L - R)) * (props.nhan.length - 1))
  tro.value = i >= 0 && i < props.nhan.length ? i : null
}
const luoi = computed(() => [0, 0.5, 1].map(t => lo.value + t * (hi.value - lo.value)))
</script>

<template>
  <figure class="min-w-0">
    <figcaption class="flex flex-wrap items-center gap-3 text-sm font-semibold text-highlighted">
      {{ tieuDe }}
      <span v-if="chuoi.length > 1" class="flex gap-3 text-xs font-normal text-muted">
        <span v-for="(c, k) in chuoi" :key="c.ten" class="flex items-center gap-1"><span class="h-0.5 w-4 rounded" :class="`${MAU[k]}-nen`" />{{ c.ten }}</span>
      </span>
    </figcaption>
    <div class="relative mt-1">
      <svg :viewBox="`0 0 ${W} ${H}`" class="w-full" role="img" :aria-label="`${tieuDe}: ${chuoi.map(c => `${c.ten} từ ${fmt(c.gt[0] ?? 0)} đến ${fmt(c.gt.at(-1) ?? 0)}`).join('; ')}`" @mousemove="di" @mouseleave="tro = null">
        <g class="text-[10px]" fill="currentColor">
          <template v-for="v in luoi" :key="v">
            <line :x1="L" :x2="W - R" :y1="y(v)" :y2="y(v)" class="stroke-[var(--ui-border-muted)]" stroke-width="1" />
            <text :x="L - 6" :y="y(v) + 3" text-anchor="end" class="fill-[var(--ui-text-dimmed)]">{{ fmt(v) }}</text>
          </template>
          <text v-if="nhan.length" :x="L" :y="H - 6" class="fill-[var(--ui-text-dimmed)]">{{ nhan[0] }}</text>
          <text v-if="nhan.length > 1" :x="W - R" :y="H - 6" text-anchor="end" class="fill-[var(--ui-text-dimmed)]">{{ nhan.at(-1) }}</text>
        </g>
        <path v-for="(c, k) in chuoi" :key="c.ten" :d="duong(c.gt)" fill="none" stroke-width="2" stroke-linejoin="round" stroke-linecap="round" :class="`${MAU[k]}-net`" />
        <template v-if="tro !== null">
          <line :x1="x(tro)" :x2="x(tro)" :y1="T" :y2="H - B" class="stroke-[var(--ui-text-dimmed)]" stroke-width="1" stroke-dasharray="3 3" />
          <circle v-for="(c, k) in chuoi" :key="c.ten" :cx="x(tro)" :cy="y(c.gt[tro] ?? 0)" r="4" :class="`${MAU[k]}-nen`" class="stroke-[var(--ui-bg)]" stroke-width="2" />
        </template>
        <rect :x="L" :y="T" :width="W - L - R" :height="H - T - B" fill="transparent" />
      </svg>
      <div v-if="tro !== null" class="pointer-events-none absolute top-0 rounded-lg border border-default bg-default px-2 py-1 text-xs shadow" :style="{ left: `${Math.min(70, (x(tro) / W) * 100)}%` }">
        <p class="font-semibold text-highlighted">{{ nhan[tro] }}</p>
        <p v-for="c in chuoi" :key="c.ten" class="text-toned">{{ c.ten }}: {{ (c.gt[tro] ?? 0).toLocaleString('vi-VN') }}{{ donVi ?? '' }}</p>
      </div>
    </div>
  </figure>
</template>

<style scoped>
.bd-1-net { stroke: #2c64ad; }
.bd-2-net { stroke: #e3350d; }
.bd-1-nen { background: #2c64ad; fill: #2c64ad; }
.bd-2-nen { background: #e3350d; fill: #e3350d; }
.dark .bd-1-net { stroke: #4f88d6; }
.dark .bd-2-net { stroke: #ec5432; }
.dark .bd-1-nen { background: #4f88d6; fill: #4f88d6; }
.dark .bd-2-nen { background: #ec5432; fill: #ec5432; }
</style>
