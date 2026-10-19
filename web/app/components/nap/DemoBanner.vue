<script setup lang="ts">
/** Banner bắt buộc trên mọi trang nạp / Thành viên khi chạy bản DEMO (HTBB_NAP_DEMO=1). */
defineProps<{ chiAdmin?: boolean }>()
</script>

<template>
  <div class="mb-8 rounded-2xl border-4 border-dashed border-error bg-error/10 p-4 text-center sm:p-5" role="alert">
    <p class="font-display text-xl font-extrabold tracking-wide text-error sm:text-2xl">
      BẢN DEMO — không chuyển tiền. Số tài khoản chỉ để minh hoạ.
    </p>
    <p class="mt-1 text-sm text-toned">
      Không có mã QR thật, không mở app ngân hàng, đơn không được ghi nhận và không cộng lượng / Thành viên.
      <template v-if="chiAdmin">
        Chỉ tài khoản quản trị thấy trang này.
      </template>
    </p>
  </div>
</template>
