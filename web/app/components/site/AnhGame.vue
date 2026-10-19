<script setup lang="ts">
/**
 * Một ảnh chụp thật trong game (shared/utils/anhGame.ts): WebP 640/1280 theo srcset, tải lười trừ khi `uuTien`.
 *   <SiteAnhGame ma="chien-dau" sizes="(min-width: 1024px) 50vw, 100vw" />
 */
const props = withDefaults(defineProps<{ ma: string, sizes?: string, uuTien?: boolean, alt?: string }>(), { sizes: '(min-width: 1024px) 50vw, 100vw' })
const a = computed(() => ANH_GAME[props.ma])
</script>

<template>
  <img
    :src="srcAnhGame(ma)"
    :srcset="srcsetAnhGame(ma)"
    :sizes="sizes"
    width="1280"
    height="720"
    :alt="alt ?? (a ? `Ảnh trong game: ${a.tieuDe}` : 'Ảnh trong game')"
    :loading="uuTien ? 'eager' : 'lazy'"
    :fetchpriority="uuTien ? 'high' : undefined"
    decoding="async"
    class="block aspect-video w-full bg-[#5a8a3c] object-cover"
  >
</template>
