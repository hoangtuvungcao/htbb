<script setup lang="ts">
/**
 * Ảnh đại diện tròn từ chân dung PMD 40×40 (chân dung có nền vẽ sẵn -> cắt tròn + viền mảnh
 * cho có chủ đích). Dùng cho biểu tượng nhỏ: dòng xếp hạng, chip, bảng. Thẻ lớn dùng PokeSan.
 *
 *   <PokeAvatar :dex="25" class="size-8" />
 */
const props = defineProps<{ dex?: number, src?: string, alt?: string }>()
const anh = computed(() => {
  if (props.src) return props.src
  const l = props.dex != null ? loaiTheoDex(props.dex) : undefined
  return l ? anhLoaiDex(l) : (props.dex != null ? anhPokemon(props.dex) : '')
})
</script>

<template>
  <span class="inline-grid shrink-0 place-items-center overflow-hidden rounded-full bg-elevated ring-1 ring-[color-mix(in_oklab,var(--poke-outline)_25%,transparent)] ring-offset-1 ring-offset-[var(--poke-card)]">
    <img v-if="anh" :src="anh" width="40" height="40" :alt="alt ?? ''" loading="lazy" decoding="async" class="pixelated size-full object-cover">
    <PokeBall v-else class="size-3/5 opacity-40" />
  </span>
</template>
