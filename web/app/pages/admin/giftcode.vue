<script setup lang="ts">
/**
 * Giftcode — chỉ còn hệ giftcode AN TOÀN (bảng tob_giftcode / tob_giftcode_log, components/admin/giftcode/AnToan.vue).
 *
 * Bảng giftcode cũ (xu / lượng / vật phẩm tự do) đã gỡ khỏi trang quản trị: máy chủ game chạy chế độ dài hạn
 * (sv.lau_dai luôn bật) nên DatabaseGiftCode chỉ đọc tob_giftcode, bảng cũ không còn được game đọc — sửa ở đó là
 * nút chết. Mã kiểu cũ trên máy thật vô hiệu bằng du_lieu/sql_nguon/vo_hieu_giftcode_cu.sql.
 */
definePageMeta({ layout: 'admin', middleware: 'admin' })
</script>

<template>
  <AdminGiftcodeAnToan />
</template>
