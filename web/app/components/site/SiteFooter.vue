<template>
  <footer class="relative border-t border-default/60 bg-default/70 backdrop-blur">
    <UContainer class="max-w-7xl py-10">
      <div class="flex flex-col items-center justify-between gap-6 md:flex-row">
        <div class="flex items-center gap-4">
          <img src="/logo-mark.svg" width="44" height="44" alt="" class="size-11">
          <div>
            <p class="font-serif text-lg font-bold text-highlighted">
              KPAH - Khí Phách Anh Hùng
            </p>
            <p class="text-sm text-muted">
              Máy chủ private phi lợi nhuận, phục vụ cộng đồng.
            </p>
          </div>
        </div>
        <nav class="flex flex-wrap justify-center gap-x-6 gap-y-2 text-sm text-muted" aria-label="Liên kết chân trang">
          <NuxtLink to="/download" class="hover:text-primary">Tải game</NuxtLink>
          <NuxtLink to="/ranking" class="hover:text-primary">Xếp hạng</NuxtLink>
          <NuxtLink to="/news" class="hover:text-primary">Tin tức</NuxtLink>
          <NuxtLink to="/download#support" class="hover:text-primary">Hỗ trợ</NuxtLink>
        </nav>
      </div>
      <USeparator class="my-6" />
      <p class="text-center text-xs text-dimmed">
        Game thuộc bản quyền TeaMobi. Trang này chỉ dùng cho mục đích học tập / máy chủ cá nhân.
      </p>
    </UContainer>
  </footer>
</template>
