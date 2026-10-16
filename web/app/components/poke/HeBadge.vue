<script setup lang="ts">
/** Nhãn một hệ Pokémon, tô màu theo hệ. `ngan`: chỉ hiện 2–3 chữ (bảng khắc chế). */
const props = defineProps<{ he: number, ngan?: boolean, size?: 'sm' | 'md' }>()
const ten = computed(() => tenHe(props.he))
</script>

<template>
  <span
    class="inline-flex items-center justify-center rounded-md font-semibold leading-none whitespace-nowrap"
    :class="size === 'sm' ? 'px-1.5 py-1 text-[11px]' : 'px-2 py-1 text-xs'"
    :style="mauHe(he)"
  >
    <template v-if="ngan"><abbr :title="ten" class="no-underline">{{ tenHeNgan(he) }}</abbr></template>
    <template v-else>{{ ten }}</template>
  </span>
</template>
