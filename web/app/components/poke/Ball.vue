<script setup lang="ts">
/**
 * Poké Ball vẽ bằng SVG (không dùng ảnh): logo, gạch đầu dòng, vạch ngăn, biểu tượng tải.
 *
 *   <PokeBall class="size-6" />                 trang trí (aria-hidden)
 *   <PokeBall spin label="Đang tải" />          vòng quay tải, có nhãn cho trình đọc màn hình
 *   <PokeBall wiggle />                         lắc như lúc đang bắt
 */
const props = defineProps<{ spin?: boolean, wiggle?: boolean, label?: string, mono?: boolean }>()
const vien = computed(() => (props.mono ? 'currentColor' : '#1b2233'))
</script>

<template>
  <svg
    viewBox="0 0 100 100"
    xmlns="http://www.w3.org/2000/svg"
    class="shrink-0"
    :class="{ 'animate-spin-ball': spin, 'animate-wiggle origin-[50%_85%]': wiggle }"
    :role="props.label ? 'img' : undefined"
    :aria-label="props.label"
    :aria-hidden="props.label ? undefined : 'true'"
  >
    <circle cx="50" cy="50" r="46" :fill="mono ? 'none' : '#fff'" :stroke="vien" stroke-width="6" />
    <path d="M4 50a46 46 0 0 1 92 0Z" :fill="mono ? 'currentColor' : '#e3350d'" :stroke="vien" stroke-width="6" stroke-linejoin="round" />
    <path v-if="!mono" d="M16 34a38 38 0 0 1 22-20" fill="none" stroke="#fff" stroke-opacity=".55" stroke-width="6" stroke-linecap="round" />
    <path d="M4 50h92" :stroke="vien" stroke-width="8" />
    <circle cx="50" cy="50" r="15" :fill="mono ? 'none' : '#fff'" :stroke="vien" stroke-width="7" />
    <circle v-if="!mono" cx="50" cy="50" r="6" fill="#fff" :stroke="vien" stroke-opacity=".35" stroke-width="2" />
  </svg>
</template>
