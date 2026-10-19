<script setup lang="ts">
/** Cảnh báo khi chưa có bảng web_admin_log (thao tác không được ghi nhật ký). */
const api = useAdminApi()
const { data } = useAsyncData('admin-me', () => api<{ coBangNhatKy: boolean }>('/api/admin/auth/me'), { server: false })
</script>

<template>
  <UAlert
    v-if="data && !data.coBangNhatKy"
    color="error"
    variant="subtle"
    icon="i-lucide-triangle-alert"
    title="Chưa có bảng web_admin_log"
  >
    <template #description>
      Mọi thao tác đang <strong>không được ghi nhật ký</strong> và đăng nhập chỉ chặn dò mật khẩu ở mức yếu. Chạy một lần:
      <code>mysql -u root htbb_db &lt; du_lieu/sql_nguon/them_bang_web_admin_log.sql</code>
    </template>
  </UAlert>
</template>
