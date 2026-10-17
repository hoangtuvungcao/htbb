<script setup lang="ts">
/**
 * Khung trang đăng nhập / đăng ký / đăng xuất: bên trái là cảnh trời + cỏ với 5
 * Pokémon khởi đầu và hộp thoại kiểu game, form bên phải. Màn hình hẹp chỉ hiện form.
 */
</script>

<template>
  <section class="relative isolate overflow-hidden py-10 sm:py-14">
    <FxGlowOrbs />
    <UContainer class="relative max-w-6xl">
      <div class="grid overflow-hidden rounded-3xl glass ring-gilded lg:grid-cols-[1.05fr_1fr]">
        <aside class="relative isolate hidden min-h-[36rem] flex-col justify-between overflow-hidden border-r-[3px] border-[var(--poke-outline)] lg:flex" aria-label="Thế giới Pokémon">
          <SiteHeroSky class="-z-10" />
          <div class="p-8">
            <p class="eyebrow">
              Một lời hẹn với vùng đất Pokémon
            </p>
            <div class="dialog-box mt-5">
              <p class="font-display text-3xl font-extrabold leading-tight text-highlighted">
                Bạn bè vẫn đợi.<br><span class="text-pokered-600">Tiến hoá còn phía trước.</span>
              </p>
              <p class="mt-2 text-muted">
                Trở về thế giới Pokémon, viết tiếp chuyến phiêu lưu của riêng bạn.
              </p>
            </div>
          </div>
          <div class="px-4 pb-2">
            <SiteStarterStage nho />
          </div>
        </aside>
        <div class="relative p-6 sm:p-10">
          <NuxtLink to="/" class="inline-flex items-center gap-1 font-display font-semibold text-muted hover:text-primary">
            <UIcon name="i-lucide-arrow-left" class="size-4" /> Về trang chủ
          </NuxtLink>
          <div class="relative mt-6">
            <slot />
          </div>
        </div>
      </div>
    </UContainer>
  </section>
</template>
