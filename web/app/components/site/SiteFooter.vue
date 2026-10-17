<script setup lang="ts">
const tenSite = useAppConfig().kpah.tenSite
</script>

<template>
  <footer class="relative mt-16">
    <!-- Bãi cỏ cao nối chân trang -->
    <div class="relative h-6 bg-grass-500 dark:bg-grass-800" aria-hidden="true">
      <div class="grass-edge !-top-[33px] !bottom-auto !bg-grass-500 dark:!bg-grass-800" />
    </div>
    <div class="on-navy">
      <UContainer class="max-w-7xl py-10">
        <div class="flex flex-col items-center justify-between gap-6 md:flex-row">
          <div class="flex items-center gap-4">
            <SiteLogo />
            <p class="max-w-56 text-sm text-muted">
              Máy chủ private phi lợi nhuận, phục vụ cộng đồng.
            </p>
          </div>
          <nav class="flex flex-wrap justify-center gap-x-6 gap-y-2 font-display font-semibold text-muted" aria-label="Liên kết chân trang">
            <NuxtLink to="/download" class="hover:text-primary">Tải game</NuxtLink>
            <NuxtLink to="/pokedex" class="hover:text-primary">Pokédex</NuxtLink>
            <NuxtLink to="/huong-dan" class="hover:text-primary">Hướng dẫn</NuxtLink>
            <NuxtLink to="/ranking" class="hover:text-primary">Xếp hạng</NuxtLink>
            <NuxtLink to="/news" class="hover:text-primary">Tin tức</NuxtLink>
            <NuxtLink to="/download#support" class="hover:text-primary">Hỗ trợ</NuxtLink>
            <NuxtLink to="/ghi-cong" class="hover:text-primary">Ghi công</NuxtLink>
          </nav>
        </div>
        <div class="my-6 flex items-center gap-3" aria-hidden="true">
          <span class="h-0.5 flex-1 bg-[var(--ui-border)]" /><PokeBall class="size-5" /><span class="h-0.5 flex-1 bg-[var(--ui-border)]" />
        </div>
        <p class="text-center text-xs text-dimmed">
          {{ tenSite }} là dự án fan phi thương mại, không liên quan chính thức tới Nintendo, Creatures, GAME FREAK hay The Pokémon Company.
          Pokémon © Nintendo/Creatures/GAME FREAK/The Pokémon Company. Sprite: PMDCollab (CC BY-NC 4.0) —
          <NuxtLink to="/ghi-cong" class="underline hover:text-primary">xem ghi công</NuxtLink>.
          Game gốc thuộc bản quyền TeaMobi. Trang này chỉ dùng cho mục đích học tập / máy chủ cá nhân.
        </p>
      </UContainer>
    </div>
  </footer>
</template>
