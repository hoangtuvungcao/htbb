<script setup lang="ts">
/** Báo bảng CMS chưa có + cách tạo. */
defineProps<{ bang?: string }>()
</script>

<template>
  <UAlert color="warning" variant="subtle" icon="i-lucide-database" title="Chưa có bảng nội dung trong CSDL">
    <template #description>
      <p>
        Website vẫn chạy bằng nội dung mặc định. Để sửa được{{ bang ? ` (${bang})` : '' }}, chạy một lần file
        <code>du_lieu/sql_nguon/web_cms.sql</code>  vào <strong>database game</strong>
        bằng công cụ quản trị CSDL quen dùng (dòng lệnh mysql/mariadb, HeidiSQL, DBeaver...), đăng nhập bằng
        tài khoản CSDL có quyền tạo bảng — thông tin kết nối giống cấu hình của web/game server.
      </p>
      <p class="mt-2 text-xs text-muted">
        File chỉ tạo bảng còn thiếu (CREATE TABLE IF NOT EXISTS), chạy lại nhiều lần không mất dữ liệu.
      </p>
    </template>
  </UAlert>
</template>
