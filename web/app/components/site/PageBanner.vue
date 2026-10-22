<script setup lang="ts">
/**
 * Đầu trang cho các trang con: dải key art của game (ảnh tĩnh public/keyart/nen_ngang.jpg, phủ tối để chữ
 * đọc rõ), đường dẫn, nhãn nhỏ, tiêu đề lớn, mô tả. Không vẽ trang trí bằng CSS.
 * `trail`: các cấp trung gian giữa "Trang chủ" và trang hiện tại (vd. Pokédex).
 */
defineProps<{ title: string, intro?: string, eyebrow?: string, trail?: { label: string, to: string }[] }>()
</script>

<template>
  <section class="vung-toi relative isolate overflow-hidden border-b-[3px] border-[#ffcb05]" aria-labelledby="page-heading">
    <img
      src="/keyart/nen_ngang.jpg"
      alt=""
      width="1760"
      height="800"
      decoding="async"
      fetchpriority="low"
      class="pixelated absolute inset-0 -z-10 size-full object-cover object-[50%_38%] opacity-55"
    >
    <div class="absolute inset-0 -z-10 bg-[linear-gradient(90deg,#120f24_0%,rgb(18_15_36/.82)_45%,rgb(18_15_36/.35)_100%)]" aria-hidden="true" />
    <UContainer class="relative max-w-7xl pb-10 pt-6 sm:pb-12 sm:pt-8">
      <nav aria-label="Đường dẫn" class="mb-5 flex flex-wrap items-center gap-1.5 text-sm text-muted">
        <NuxtLink to="/" class="hover:text-white hover:underline">Trang chủ</NuxtLink>
        <template v-for="t in trail" :key="t.to">
          <UIcon name="i-lucide-chevron-right" class="size-4" aria-hidden="true" />
          <NuxtLink :to="t.to" class="hover:text-white hover:underline">{{ t.label }}</NuxtLink>
        </template>
        <UIcon name="i-lucide-chevron-right" class="size-4" aria-hidden="true" />
        <span aria-current="page" class="max-w-[60vw] truncate font-semibold text-white">{{ title }}</span>
      </nav>
      <p v-if="eyebrow" class="eyebrow !text-[#ffcb05]">
        {{ eyebrow }}
      </p>
      <h1 id="page-heading" class="tieu-de-muc mt-2 text-4xl !text-white [text-shadow:0_3px_0_#06040e] sm:text-5xl">
        {{ title }}
      </h1>
      <p v-if="intro" class="mt-3 max-w-2xl text-base text-toned sm:text-lg">
        {{ intro }}
      </p>
    </UContainer>
  </section>
</template>
