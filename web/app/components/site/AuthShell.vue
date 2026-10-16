<script setup lang="ts">
/**
 * Khung trang đăng nhập / đăng ký / đăng xuất: hình minh hoạ bên trái (nghiêng
 * 3D theo chuột), form bên phải. Màn hình hẹp chỉ hiện form.
 */
</script>

<template>
  <section class="relative overflow-hidden py-10 sm:py-16">
    <FxGlowOrbs />
    <UContainer class="relative max-w-6xl">
      <div class="grid overflow-hidden rounded-3xl glass ring-gilded shadow-2xl shadow-black/40 lg:grid-cols-[1.05fr_1fr]">
        <FxTiltCard :max="6" :scale="1" as="aside" class="relative hidden min-h-[36rem] overflow-hidden lg:block" aria-label="Thế giới Pokémon">
          <img src="/auth-scene.svg" alt="" width="900" height="800" class="absolute inset-0 size-full object-cover opacity-90">
          <div class="absolute inset-0 bg-gradient-to-t from-ink-950 via-ink-950/40 to-transparent" />
          <div class="absolute inset-x-0 bottom-0 p-10 [transform:translateZ(50px)]">
            <p class="eyebrow">
              Một lời hẹn với vùng đất Pokémon
            </p>
            <p class="mt-4 font-serif text-4xl font-bold leading-tight text-highlighted">
              Bạn bè vẫn đợi.<br><em class="text-gilded">Tiến hoá còn phía trước.</em>
            </p>
            <p class="mt-3 text-muted">
              Trở về thế giới Pokémon, viết tiếp chuyến phiêu lưu của riêng bạn.
            </p>
            <div class="mt-8 flex flex-wrap items-center gap-x-3 gap-y-1 border-t border-default/60 pt-4 text-xs tracking-[0.2em] text-dimmed">
              <span class="whitespace-nowrap">KPAH POKÉMON</span><span aria-hidden="true">✦</span><span class="whitespace-nowrap">TỪNG CẤP MỘT LẦN TIẾN HOÁ</span>
            </div>
          </div>
        </FxTiltCard>
        <div class="relative p-6 sm:p-10">
          <div class="pointer-events-none absolute -right-20 -top-20 size-64 rounded-full bg-gold-400/10 blur-3xl" aria-hidden="true" />
          <NuxtLink to="/" class="inline-flex items-center gap-1 text-sm text-muted hover:text-primary">
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
