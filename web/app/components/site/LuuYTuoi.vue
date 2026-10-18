<script setup lang="ts">
/**
 * Thông báo độ tuổi + chơi game lành mạnh (khớp huy hiệu 12+ trong game).
 *   <SiteLuuYTuoi />          khối đầy đủ (trang Đăng ký, Tải game, Trang chủ)
 *   <SiteLuuYTuoi gon />      một dòng (chân trang)
 */
defineProps<{ gon?: boolean }>()
</script>

<template>
  <p v-if="gon" class="flex flex-wrap items-center justify-center gap-2 text-xs text-muted">
    <span class="grid h-6 min-w-9 place-items-center rounded-md border-2 border-current px-1 font-display text-xs font-extrabold" aria-hidden="true">12+</span>
    <span>Trò chơi dành cho người từ 12 tuổi trở lên. Chơi quá 180 phút mỗi ngày sẽ ảnh hưởng xấu đến sức khoẻ.</span>
  </p>
  <aside v-else class="flex items-start gap-3 rounded-2xl border-2 border-pikachu-400 bg-pikachu-50 p-4 text-sm text-navy-900 dark:bg-pikachu-950/30 dark:text-pikachu-100" role="note" aria-label="Lưu ý độ tuổi">
    <span class="grid h-10 min-w-12 shrink-0 place-items-center rounded-lg border-[3px] border-pikachu-600 bg-white font-display text-lg font-extrabold text-pikachu-700 dark:bg-navy-950 dark:text-pikachu-300" aria-hidden="true">12+</span>
    <span>
      <strong class="block font-display text-base">Trò chơi dành cho người từ 12 tuổi trở lên</strong>
      Chơi quá 180 phút mỗi ngày sẽ ảnh hưởng xấu đến sức khoẻ. Hãy sắp xếp thời gian học tập, nghỉ ngơi và vận động hợp lý.
    </span>
  </aside>
</template>
