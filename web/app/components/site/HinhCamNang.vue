<script setup lang="ts">
/** Hình đại diện một trang cẩm nang: chân dung PMD hoặc icon vật phẩm thật (HINH_CAM_NANG); thiếu thì icon Lucide. */
const props = defineProps<{ slug: string, icon?: string }>()
const anh = computed(() => {
  const h = HINH_CAM_NANG[props.slug]
  if (h?.dex) return { src: anhChanDung(h.dex), vp: false }
  if (h?.vp !== undefined) return { src: iconVatPham(h.vp), vp: true }
  return null
})
</script>

<template>
  <span class="grid shrink-0 place-items-center overflow-hidden rounded-2xl bg-screen" aria-hidden="true">
    <img v-if="anh?.src" :src="anh.src" :width="anh.vp ? 16 : 40" :height="anh.vp ? 16 : 40" alt="" loading="lazy" class="pixelated" :class="anh.vp ? 'size-3/5' : 'size-full object-cover'">
    <UIcon v-else :name="icon || 'i-lucide-book-open'" class="size-1/2 text-primary" />
  </span>
</template>
