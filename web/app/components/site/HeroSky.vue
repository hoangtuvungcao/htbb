<script setup lang="ts">
/**
 * Nền hero trang chủ (thuần CSS/SVG, không WebGL): bầu trời, mây trôi, đồi xa và
 * bãi cỏ cao ở dưới. Chế độ tối: trời đêm xanh navy có sao và trăng.
 * Người bật "giảm chuyển động" thấy mây đứng yên.
 */
const may = [
  { top: '12%', w: 180, dur: 70, delay: -10, op: 1 },
  { top: '26%', w: 120, dur: 95, delay: -60, op: 0.85 },
  { top: '6%', w: 240, dur: 120, delay: -95, op: 0.9 },
  { top: '38%', w: 90, dur: 80, delay: -35, op: 0.7 },
]
</script>

<template>
  <div class="pointer-events-none absolute inset-0 overflow-hidden" aria-hidden="true">
    <!-- Trời -->
    <div class="absolute inset-0 bg-[linear-gradient(180deg,var(--poke-sky-top)_0%,var(--poke-sky-bottom)_70%)]" />
    <!-- Mặt trời (sáng) / trăng + sao (tối) -->
    <div class="absolute right-[8%] top-[9%] size-24 rounded-full bg-pikachu-300 shadow-[0_0_0_14px_rgb(255_224_74/.25),0_0_0_30px_rgb(255_224_74/.12)] dark:size-16 dark:bg-pikachu-100 dark:shadow-[0_0_40px_rgb(255_248_194/.35)]" />
    <div class="absolute inset-0 hidden bg-[radial-gradient(circle_at_1px_1px,#fff_1px,transparent_1.5px)] bg-[length:46px_46px] opacity-40 dark:block" />
    <!-- Mây (chế độ tối: mờ hẳn để chữ trắng phía trên vẫn đọc được) -->
    <div class="absolute inset-0 dark:opacity-20">
    <svg
      v-for="(m, i) in may"
      :key="i"
      viewBox="0 0 120 50"
      class="absolute left-0 animate-drift motion-reduce:animate-none"
      :style="{ top: m.top, width: `${m.w}px`, opacity: m.op, animationDuration: `${m.dur}s`, animationDelay: `${m.delay}s` }"
    >
      <path d="M14 46h92a12 12 0 0 0 2-24 18 18 0 0 0-30-10A24 24 0 0 0 36 16a14 14 0 0 0-22 12A9 9 0 0 0 14 46Z" fill="#fff" stroke="#bfe0f7" stroke-width="2" />
    </svg>
    </div>
    <!-- Đồi xa -->
    <svg viewBox="0 0 1440 220" preserveAspectRatio="none" class="absolute inset-x-0 bottom-[110px] h-[180px] w-full sm:bottom-[130px]">
      <path d="M0 160C120 90 240 70 380 110s260 80 420 20 300-110 460-60 160 60 180 70V220H0Z" class="fill-grass-300 dark:fill-grass-900" />
      <path d="M0 190c160-50 300-60 460-20s300 40 470-10 330-60 510 0V220H0Z" class="fill-grass-400 dark:fill-grass-800" />
    </svg>
    <!-- Bãi cỏ -->
    <div class="absolute inset-x-0 bottom-0 h-[118px] bg-grass-400 sm:h-[138px] dark:bg-grass-800">
      <div class="absolute inset-0 bg-[repeating-linear-gradient(90deg,transparent_0_22px,rgb(0_0_0/.05)_22px_44px)]" />
      <div class="grass-edge !-top-[28px] !bottom-auto !bg-grass-400 dark:!bg-grass-800" />
    </div>
  </div>
</template>
