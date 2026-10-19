<script setup lang="ts">
/** Trang cũ /admin/phan-quyen — đã gộp vào /admin/vai-tro (cấp vai trò + ma trận quyền trên một trang). */
definePageMeta({ layout: 'admin', middleware: 'admin' })
await navigateTo('/admin/vai-tro', { replace: true })
</script>

<template>
  <div />
</template>
