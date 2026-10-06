<script setup lang="ts">
/**
 * Cột "Phần thưởng": chuỗi thô để đối chiếu/copy, cộng bản dịch dễ đọc ngay dưới —
 * bản dịch do máy chủ dựng bằng CHÍNH danh mục của bộ chọn (gcMoTaNhieu), nên tên
 * hiện ra ở đây LUÔN khớp tên trong bộ chọn. Đoạn khó hiểu chỉ hiện "? ..." ở đúng chỗ.
 */
defineProps<{ item: string, moTa: string[] }>()
</script>

<template>
  <div v-if="item.trim() !== ''" class="min-w-56 max-w-md whitespace-normal">
    <code class="text-xs break-all text-highlighted">{{ item.trim() }}</code>
    <ul v-if="moTa.length" class="mt-1 list-disc space-y-0.5 pl-5 text-xs text-muted">
      <li v-for="(d, i) in moTa" :key="i" :class="d.startsWith('? ') ? 'text-warning' : ''">
        {{ d }}
      </li>
    </ul>
  </div>
  <span v-else class="text-dimmed">—</span>
</template>
