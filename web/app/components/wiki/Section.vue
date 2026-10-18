<script setup lang="ts">
/** Một mục (h2 có anchor) trong trang cẩm nang. */
defineProps<{ id: string, title: string, lead?: string }>()
</script>

<template>
   <section :id="id" class="mt-14 scroll-mt-24 first:mt-0" :aria-labelledby="`h-${id}`">
    <h2 :id="`h-${id}`" class="group section-title flex items-center gap-2 text-2xl text-highlighted sm:text-3xl">
      <span class="size-3 shrink-0 rotate-45 rounded-[3px] bg-pokered-500 ring-2 ring-[var(--poke-outline)]" aria-hidden="true" />
      {{ title }}
      <a :href="`#${id}`" class="text-base text-dimmed opacity-0 transition group-hover:opacity-100 focus:opacity-100" :aria-label="`Liên kết tới mục ${title}`">#</a>
    </h2>
    <p v-if="lead" class="mt-3 max-w-3xl text-muted">
      {{ lead }}
    </p>
    <div class="mt-5">
      <slot />
    </div>
  </section>
</template>
