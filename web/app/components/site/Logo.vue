<script setup lang="ts">
/**
 * Logo website = wordmark chung của game (client_jar/tools/tao_logo.py -> public/logo-gon.svg: chữ vàng
 * -> cam viền navy/xanh, Poké Ball thay chữ O). Không dùng logo / font Pokémon chính thức.
 */
withDefaults(defineProps<{ size?: 'sm' | 'md' }>(), { size: 'md' })
const ten = useAppConfig().htbb.tenSite
</script>

<template>
  <span class="inline-flex items-center">
    <img src="/logo-gon.svg" :alt="ten" width="796" height="409" :class="size === 'sm' ? 'h-9 w-auto' : 'h-11 w-auto'" decoding="async">
  </span>
</template>
