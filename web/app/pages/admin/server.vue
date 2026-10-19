<script setup lang="ts">
/** Trang cũ /admin/server — đã gộp vào /admin/he-thong (khối "Bật / tắt / khởi động lại"). Giữ đường dẫn cho liên kết cũ. */
definePageMeta({ layout: 'admin', middleware: 'admin' })
await navigateTo({ path: '/admin/he-thong', hash: '#dieu-khien' }, { replace: true })
</script>

<template>
  <div />
</template>
