<script setup lang="ts">
/**
 * Hình GIẢ thay cho mã VietQR khi chạy bản DEMO. Không phải mã QR (không mã hoá dữ liệu nào,
 * không quét được): chỉ là lưới ô vuông sinh từ mã đơn để trông giống, đè chữ DEMO thật to.
 */
const props = withDefaults(defineProps<{ ma: string, kichThuoc?: number }>(), { kichThuoc: 288 })

const N = 25
function laMat(x: number, y: number): boolean {
  const o = (cx: number, cy: number) => x >= cx && x < cx + 7 && y >= cy && y < cy + 7
  return o(0, 0) || o(N - 7, 0) || o(0, N - 7)
}
function oMat(x: number, y: number): boolean {
  // Ba ô định vị kiểu QR (chỉ để trông giống).
  for (const [cx, cy] of [[0, 0], [N - 7, 0], [0, N - 7]] as const) {
    const dx = x - cx
    const dy = y - cy
    if (dx >= 0 && dx < 7 && dy >= 0 && dy < 7) {
      const vien = dx === 0 || dx === 6 || dy === 0 || dy === 6
      const loi = dx >= 2 && dx <= 4 && dy >= 2 && dy <= 4
      return vien || loi
    }
  }
  return false
}
const o = computed(() => {
  let h = 2166136261
  for (const c of `demo|${props.ma}`) h = Math.imul(h ^ c.charCodeAt(0), 16777619)
  const ra: { x: number, y: number }[] = []
  for (let y = 0; y < N; y++) {
    for (let x = 0; x < N; x++) {
      if (laMat(x, y)) {
        if (oMat(x, y)) ra.push({ x, y })
        continue
      }
      h = Math.imul(h ^ (x * 31 + y), 16777619)
      if (((h >>> 7) & 3) === 0) ra.push({ x, y })
    }
  }
  return ra
})
</script>

<template>
  <div class="relative mx-auto w-fit max-w-full rounded-2xl bg-white p-3 shadow-[0_20px_60px_rgba(10,26,51,.14)]">
    <svg
      :width="kichThuoc"
      :height="kichThuoc"
      :viewBox="`0 0 ${N} ${N}`"
      role="img"
      aria-label="Hình minh hoạ mã QR — BẢN DEMO, không quét được, không chuyển tiền"
      class="block h-auto max-w-full opacity-40"
      shape-rendering="crispEdges"
    >
      <rect width="100%" height="100%" fill="#fff" />
      <rect v-for="(c, i) in o" :key="i" :x="c.x" :y="c.y" width="1" height="1" fill="#475569" />
    </svg>
    <div class="pointer-events-none absolute inset-0 grid place-items-center">
      <span class="-rotate-[28deg] select-none rounded-xl border-[6px] border-red-600 bg-white/85 px-5 py-1 font-display text-5xl font-black sm:text-6xl tracking-widest text-red-600">
        DEMO
      </span>
    </div>
    <p class="mt-2 text-center text-xs font-bold text-red-600">
      KHÔNG PHẢI MÃ QR THẬT
    </p>
  </div>
</template>
