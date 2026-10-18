<script setup lang="ts">
/** Báo bảng CMS chưa có + cách tạo. */
defineProps<{ bang?: string }>()
</script>

<template>
  <UAlert color="warning" variant="subtle" icon="i-lucide-database" title="Chưa có bảng nội dung trong CSDL">
    <template #description>
      <p>
        Website vẫn chạy bằng nội dung mặc định. Để sửa được{{ bang ? ` (${bang})` : '' }}, chạy một lần file
        <code>server/sql/web_cms.sql</code> (bản sao: <code>HTBB/sql/web_cms.sql</code>) vào database game:
      </p>
      <pre class="mt-2 overflow-x-auto rounded-lg bg-elevated p-2 text-xs">mysql -u kpah -p kpah_db &lt; server/sql/web_cms.sql
# NovaDB: mysql -h 127.0.0.1 -P 3318 -u kpah -p kpah_db &lt; server/sql/web_cms.sql</pre>
    </template>
  </UAlert>
</template>
