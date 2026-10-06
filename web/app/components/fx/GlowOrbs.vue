<template>
  <!-- Quầng sáng mờ trôi chậm làm nền cho các trang con (nhẹ hơn nhiều so với WebGL).
       Mép trên/dưới mờ dần để không thành vạch cắt ngang giữa hai phần trang. -->
  <div class="pointer-events-none absolute inset-0 overflow-hidden [mask-image:linear-gradient(to_bottom,transparent,black_12%,black_88%,transparent)]" aria-hidden="true">
    <div class="absolute -left-32 -top-40 size-[34rem] rounded-full bg-jade-500/15 blur-3xl animate-float" />
    <div class="absolute -right-24 top-10 size-[28rem] rounded-full bg-gold-400/12 blur-3xl animate-float [animation-delay:-2s]" />
    <div class="absolute bottom-[-12rem] left-1/3 size-[30rem] rounded-full bg-jade-700/15 blur-3xl animate-float [animation-delay:-4s]" />
  </div>
</template>
