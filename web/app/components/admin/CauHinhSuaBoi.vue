<script setup lang="ts">
/** Dòng nhỏ "sửa bởi X lúc Y" dưới một nhóm khoá cấu hình — lấy mốc mới nhất trong nhóm. */
defineProps<{ moc?: { boi: string, luc: string } | null }>()
</script>

<template>
  <p v-if="moc" class="text-xs text-dimmed">
    Sửa lần cuối bởi <strong class="text-toned">{{ moc.boi }}</strong> lúc <span class="num">{{ moc.luc }}</span>.
  </p>
  <p v-else class="text-xs text-dimmed">
    Chưa từng sửa qua CSDL — đang dùng giá trị khởi tạo lần đầu từ server.ini.
  </p>
</template>
