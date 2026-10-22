<script setup lang="ts">
/**
 * Biểu tượng Poké Ball = logo-mark của game (public/logo-mark.svg, client_jar/tools/tao_logo.py — cùng icon
 * ứng dụng), không vẽ tay bằng CSS/SVG riêng cho web.
 *
 *   <PokeBall class="size-6" />                 trang trí (aria-hidden)
 *   <PokeBall spin label="Đang tải" />          vòng quay tải, có nhãn cho trình đọc màn hình
 *   <PokeBall wiggle />                         lắc nhẹ
 *   mono                                        mờ, đen trắng (nền trang trí)
 */
const props = defineProps<{ spin?: boolean, wiggle?: boolean, label?: string, mono?: boolean }>()
</script>

<template>
  <img
    src="/logo-mark.svg"
    width="64"
    height="64"
    decoding="async"
    class="shrink-0 select-none"
    :class="{ 'animate-spin-ball': spin, 'animate-wiggle origin-[50%_85%]': wiggle, 'opacity-60 grayscale': mono }"
    :alt="props.label ?? ''"
    :role="props.label ? 'img' : undefined"
    :aria-hidden="props.label ? undefined : 'true'"
  >
</template>
