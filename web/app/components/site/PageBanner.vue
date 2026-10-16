<script setup lang="ts">
/** Đầu trang cho các trang con: dòng nhỏ, tiêu đề lớn, mô tả. */
defineProps<{ title: string, intro?: string, eyebrow?: string }>()
</script>

<template>
  <section class="relative overflow-hidden border-b border-default/60" aria-labelledby="page-heading">
    <FxGlowOrbs />
    <UContainer class="relative max-w-7xl py-16 sm:py-20">
      <nav aria-label="Đường dẫn" class="mb-6 flex items-center gap-2 text-sm text-muted">
        <NuxtLink to="/" class="text-primary hover:underline">Trang chủ</NuxtLink>
        <UIcon name="i-lucide-chevron-right" class="size-4" aria-hidden="true" />
        <span aria-current="page">{{ title }}</span>
      </nav>
      <FxReveal>
        <p class="eyebrow">
          {{ eyebrow ?? useAppConfig().kpah.tenSite }}
        </p>
        <h1 id="page-heading" class="section-title mt-3 text-4xl text-highlighted sm:text-5xl">
          {{ title }}
        </h1>
        <p v-if="intro" class="mt-4 max-w-2xl text-lg text-muted">
          {{ intro }}
        </p>
      </FxReveal>
      <span class="pointer-events-none absolute right-6 top-1/2 hidden -translate-y-1/2 font-serif text-[9rem] leading-none text-primary/10 md:block" aria-hidden="true">✦</span>
    </UContainer>
  </section>
</template>
