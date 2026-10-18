<script setup lang="ts">
/**
 * Nguồn vào / nơi tiêu theo ngày: cột "vào" đi lên, cột "ra" đi xuống từ cùng đường gốc (một trục,
 * đối xứng) — nhìn là thấy ngày nào in tiền nhiều hơn đốt. Rê chuột / chạm: vào, ra, ròng.
 * Màu ô 1 (xanh) = vào, ô 2 (cam) = ra, cùng bảng với AdminBieuDoDuong.
 *   <AdminKinhteVaoRa :nhan="['10-01']" :vao="[100]" :ra="[80]" don-vi="xu" />
 */
const props = withDefaults(defineProps<{ nhan: string[], vao: number[], ra: number[], donVi?: string, tieuDe?: string }>(), { donVi: '', tieuDe: '' })
const W = 520
const H = 180
const L = 52
const R = 12
const T = 10
const B = 22
const max = computed(() => Math.max(1, ...props.vao.map(v => Math.abs(v || 0)), ...props.ra.map(v => Math.abs(v || 0))))
const giua = (T + H - B) / 2
const cao = (v: number) => (Math.abs(v || 0) / max.value) * (giua - T)
const buoc = computed(() => (W - L - R) / Math.max(1, props.nhan.length))
const rong = computed(() => Math.max(1, Math.min(18, buoc.value - 2)))
const cx = (i: number) => L + buoc.value * i + buoc.value / 2
const fmt = (v: number) => Math.abs(v) >= 1e9 ? `${(v / 1e9).toFixed(1)}B` : Math.abs(v) >= 1e6 ? `${(v / 1e6).toFixed(1)}M` : Math.abs(v) >= 1e4 ? `${(v / 1e3).toFixed(0)}k` : v.toLocaleString('vi-VN')
/** Cột bo 4px ở đầu xa đường gốc, đầu sát gốc vuông. len > 0: đi lên; < 0: đi xuống. */
function hinh(i: number, h: number, len: number) {
  if (h <= 0) return ''
  const w = rong.value
  const x0 = cx(i) - w / 2
  const r = Math.min(4, w / 2, h)
  if (len > 0) { // lên
    const y0 = giua - h
    return `M${x0},${giua}V${y0 + r}Q${x0},${y0} ${x0 + r},${y0}H${x0 + w - r}Q${x0 + w},${y0} ${x0 + w},${y0 + r}V${giua}Z`
  }
  const y1 = giua + h
  return `M${x0},${giua}V${y1 - r}Q${x0},${y1} ${x0 + r},${y1}H${x0 + w - r}Q${x0 + w},${y1} ${x0 + w},${y1 - r}V${giua}Z`
}
const tro = ref<number | null>(null)
function di(e: PointerEvent) {
  const r = (e.currentTarget as SVGElement).getBoundingClientRect()
  const px = ((e.clientX - r.left) / r.width) * W
  const i = Math.floor((px - L) / buoc.value)
  tro.value = i >= 0 && i < props.nhan.length ? i : null
}
const coSo = computed(() => props.nhan.length > 0 && (props.vao.some(v => v) || props.ra.some(v => v)))
</script>

<template>
  <figure class="min-w-0">
    <figcaption class="flex flex-wrap items-center gap-x-3 gap-y-1 text-sm font-semibold text-highlighted">
      <span v-if="tieuDe">{{ tieuDe }}</span>
      <span class="flex gap-3 text-xs font-normal text-muted">
        <span class="flex items-center gap-1"><span class="size-2.5 rounded-sm vr-vao" />Vào (nguồn sinh)</span>
        <span class="flex items-center gap-1"><span class="size-2.5 rounded-sm vr-ra" />Ra (nơi tiêu)</span>
      </span>
    </figcaption>
    <p v-if="!coSo" class="mt-2 flex h-24 items-center justify-center rounded-lg border border-dashed border-default text-sm text-muted">
      Chưa có số vào / ra (game cộng dồn từ Char.addXu / subXu và ghi lúc 04:00).
    </p>
    <div v-else class="relative mt-1">
      <svg :viewBox="`0 0 ${W} ${H}`" class="w-full touch-none select-none" role="img" :aria-label="`${tieuDe || 'Vào / ra'}: ${nhan.length} ngày`" @pointermove="di" @pointerdown="di" @pointerleave="tro = null">
        <g class="text-[10px] tabular-nums">
          <line :x1="L" :x2="W - R" :y1="T" :y2="T" class="stroke-[var(--ui-border-muted)]" />
          <line :x1="L" :x2="W - R" :y1="H - B" :y2="H - B" class="stroke-[var(--ui-border-muted)]" />
          <text :x="L - 6" :y="T + 4" text-anchor="end" class="fill-[var(--ui-text-dimmed)]">+{{ fmt(max) }}</text>
          <text :x="L - 6" :y="giua + 3" text-anchor="end" class="fill-[var(--ui-text-dimmed)]">0</text>
          <text :x="L - 6" :y="H - B" text-anchor="end" class="fill-[var(--ui-text-dimmed)]">−{{ fmt(max) }}</text>
          <text :x="L" :y="H - 6" class="fill-[var(--ui-text-dimmed)]">{{ nhan[0] }}</text>
          <text v-if="nhan.length > 1" :x="W - R" :y="H - 6" text-anchor="end" class="fill-[var(--ui-text-dimmed)]">{{ nhan.at(-1) }}</text>
        </g>
        <rect v-if="tro !== null" :x="L + buoc * tro" :y="T" :width="buoc" :height="H - T - B" class="fill-[var(--ui-bg-accented)]" opacity="0.6" />
        <template v-for="(_, i) in nhan" :key="i">
          <path :d="hinh(i, cao(vao[i] ?? 0), 1)" class="vr-vao" />
          <path :d="hinh(i, cao(ra[i] ?? 0), -1)" class="vr-ra" />
        </template>
        <line :x1="L" :x2="W - R" :y1="giua" :y2="giua" class="stroke-[var(--ui-text-dimmed)]" stroke-width="1" />
        <rect :x="L" :y="T" :width="W - L - R" :height="H - T - B" fill="transparent" />
      </svg>
      <div v-if="tro !== null" class="pointer-events-none absolute top-0 z-10 rounded-lg border border-default bg-default px-2 py-1 text-xs shadow tabular-nums" :style="{ left: `${Math.min(62, Math.max(0, (cx(tro) / W) * 100 - 10))}%` }">
        <p class="font-semibold text-highlighted">{{ nhan[tro] }}</p>
        <p class="text-toned">Vào: {{ (vao[tro] ?? 0).toLocaleString('vi-VN') }} {{ donVi }}</p>
        <p class="text-toned">Ra: {{ (ra[tro] ?? 0).toLocaleString('vi-VN') }} {{ donVi }}</p>
        <p class="font-semibold text-highlighted">Ròng: {{ ((vao[tro] ?? 0) - (ra[tro] ?? 0)).toLocaleString('vi-VN') }} {{ donVi }}</p>
      </div>
    </div>
  </figure>
</template>

<style scoped>
.vr-vao { fill: #2a78d6; background: #2a78d6; }
.vr-ra { fill: #eb6834; background: #eb6834; }
.dark .vr-vao { fill: #3987e5; background: #3987e5; }
.dark .vr-ra { fill: #d95926; background: #d95926; }
</style>
