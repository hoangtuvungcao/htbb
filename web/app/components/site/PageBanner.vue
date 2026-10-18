<script setup lang="ts">
/** Đầu trang cho các trang con: biển chỉ đường (đường dẫn), nhãn nhỏ, tiêu đề lớn, mô tả — trên nền trời + cỏ. */
/** `trail`: các cấp trung gian giữa "Trang chủ" và trang hiện tại (vd. Pokédex). */
defineProps<{ title: string, intro?: string, eyebrow?: string, trail?: { label: string, to: string }[] }>()
</script>

<template>
  <section class="relative isolate overflow-hidden" aria-labelledby="page-heading">
    <div class="absolute inset-0 -z-10 bg-[linear-gradient(180deg,var(--poke-sky-top),var(--poke-sky-bottom))]" aria-hidden="true" />
    <div class="absolute inset-0 -z-10 hidden bg-[radial-gradient(circle_at_1px_1px,#fff_1px,transparent_1.5px)] bg-[length:46px_46px] opacity-30 dark:block" aria-hidden="true" />
    <PokeBall class="pointer-events-none absolute -right-10 top-6 -z-10 hidden size-56 rotate-12 opacity-15 md:block dark:opacity-10" />
    <UContainer class="relative max-w-7xl pb-16 pt-10 sm:pb-20 sm:pt-12">
      <nav aria-label="Đường dẫn" class="mb-6 flex flex-wrap items-center gap-1.5 text-sm">
        <NuxtLink to="/" class="route-sign !px-3 !py-1 text-sm hover:!bg-pokeblue-500">
          <UIcon name="i-lucide-house" class="size-4" aria-hidden="true" /> Trang chủ
        </NuxtLink>
        <template v-for="t in trail" :key="t.to">
          <UIcon name="i-lucide-chevron-right" class="size-4 text-toned" aria-hidden="true" />
          <NuxtLink :to="t.to" class="route-sign !px-3 !py-1 text-sm hover:!bg-pokeblue-500">{{ t.label }}</NuxtLink>
        </template>
        <UIcon name="i-lucide-chevron-right" class="size-4 text-toned" aria-hidden="true" />
        <span aria-current="page" class="max-w-[60vw] truncate font-display font-bold text-highlighted">{{ title }}</span>
      </nav>
      <FxReveal>
        <p class="eyebrow">
          {{ eyebrow ?? useAppConfig().htbb.tenSite }}
        </p>
        <h1 id="page-heading" class="section-title mt-4 text-4xl text-highlighted sm:text-5xl">
          {{ title }}
        </h1>
        <p v-if="intro" class="mt-4 max-w-2xl text-lg text-toned">
          {{ intro }}
        </p>
      </FxReveal>
    </UContainer>
    <div class="grass-edge" aria-hidden="true" />
    <div class="absolute inset-x-0 bottom-0 h-1.5 bg-grass-500" aria-hidden="true" />
  </section>
</template>
