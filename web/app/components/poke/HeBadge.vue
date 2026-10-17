<script setup lang="ts">
/** Nhãn một hệ Pokémon, tô màu theo hệ. `ngan`: chỉ hiện 2–3 chữ (bảng khắc chế). */
const props = defineProps<{ he: number, ngan?: boolean, size?: 'sm' | 'md' }>()
const ten = computed(() => tenHe(props.he))
const mau = computed(() => {
  const m = mauHe(props.he)
  return {
    ...m,
    // Viền + bóng đáy đậm hơn màu nền một chút: nhãn "nổi" như trong game.
    borderColor: `color-mix(in oklab, ${m.backgroundColor} 62%, #000)`,
    boxShadow: `inset 0 -2px 0 color-mix(in oklab, ${m.backgroundColor} 75%, #000)`,
  }
})
</script>

<template>
  <span
    class="inline-flex items-center justify-center rounded-full border font-display font-bold leading-none whitespace-nowrap"
    :class="size === 'sm' ? 'px-2 pt-[3px] pb-[4px] text-[11px]' : 'px-2.5 pt-1 pb-[5px] text-xs'"
    :style="mau"
  >
    <template v-if="ngan"><abbr :title="ten" class="no-underline">{{ tenHeNgan(he) }}</abbr></template>
    <template v-else>{{ ten }}</template>
  </span>
</template>
