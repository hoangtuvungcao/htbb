<script setup lang="ts">
/** Báo bảng CMS chưa có + cách tạo. */
defineProps<{ bang?: string }>()
</script>

<template>
  <UAlert color="warning" variant="subtle" icon="i-lucide-database" title="Chưa có bảng nội dung trong CSDL">
    <template #description>
      <p>
        Website vẫn chạy bằng nội dung mặc định. Để sửa được{{ bang ? ` (${bang})` : '' }}, chạy một lần trên máy chủ
        <code>sudo bash /opt/htbb/vps/nang_cap_csdl.sh</code> (nạp <code>sql/nang_cap.sql</code> vào <strong>database game</strong>:
        tạo mọi bảng còn thiếu, không đụng dữ liệu người chơi).
      </p>
      <p class="mt-2 text-xs text-muted">
        File chỉ tạo bảng còn thiếu (CREATE TABLE IF NOT EXISTS), chạy lại nhiều lần không mất dữ liệu.
      </p>
    </template>
  </UAlert>
</template>
