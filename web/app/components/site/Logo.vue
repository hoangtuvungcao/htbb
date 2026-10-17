<script setup lang="ts">
/**
 * Logo website: Poké Ball + wordmark "Huyền Thoại / Bảo Bối" chữ vàng viền xanh.
 * Wordmark tự làm (font Baloo 2), không sao chép logo/font Pokémon chính thức.
 * Tên lấy từ app config (shared/utils/tenSite.ts): tách đôi theo khoảng trắng ở giữa.
 */
withDefaults(defineProps<{ size?: 'sm' | 'md' }>(), { size: 'md' })
const ten = useAppConfig().kpah.tenSite
const tu = ten.split(' ')
const nua = Math.ceil(tu.length / 2)
const dong1 = tu.slice(0, nua).join(' ')
const dong2 = tu.slice(nua).join(' ')
</script>

<template>
  <span class="inline-flex items-center gap-2">
    <PokeBall :class="size === 'sm' ? 'size-8' : 'size-10'" />
    <span class="text-gilded flex flex-col font-display font-extrabold leading-[.95]" aria-hidden="true">
      <span :class="size === 'sm' ? 'text-sm' : 'text-[15px]'">{{ dong1 }}</span>
      <span :class="size === 'sm' ? 'text-lg' : 'text-[22px]'">{{ dong2 }}</span>
    </span>
    <span class="sr-only">{{ ten }}</span>
  </span>
</template>
