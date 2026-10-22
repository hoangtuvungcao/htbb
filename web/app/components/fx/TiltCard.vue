<script setup lang="ts">
/**
 * Trước đây: thẻ nghiêng 3D theo chuột + vệt sáng. Bỏ hiệu ứng (website 22/10/2026 theo kiểu khung game, không
 * hiệu ứng "kính"/nghiêng). Giữ component làm khung bọc thường để các trang cũ không phải sửa.
 */
withDefaults(defineProps<{ max?: number, glare?: boolean, scale?: number, as?: string }>(), { max: 0, glare: false, scale: 1, as: 'div' })
</script>

<template>
  <component :is="as">
    <slot />
  </component>
</template>
