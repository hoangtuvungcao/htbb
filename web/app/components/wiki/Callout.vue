<script setup lang="ts">
/** Hộp ghi chú: mẹo (xanh cỏ), lưu ý (vàng), sắp ra mắt (xanh dương), vì sao (đỏ). */
const props = withDefaults(defineProps<{ kieu?: 'meo' | 'luu-y' | 'sap-ra-mat' | 'vi-sao', title?: string }>(), { kieu: 'meo' })
const CAU_HINH = {
  'meo': { icon: 'i-lucide-lightbulb', tieuDe: 'Mẹo', lop: 'border-grass-500 bg-grass-50 dark:bg-grass-950/40', iconLop: 'bg-grass-500' },
  'luu-y': { icon: 'i-lucide-triangle-alert', tieuDe: 'Lưu ý', lop: 'border-pikachu-500 bg-pikachu-50 dark:bg-pikachu-950/30', iconLop: 'bg-pikachu-500' },
  'sap-ra-mat': { icon: 'i-lucide-hammer', tieuDe: 'Đang phát triển', lop: 'border-pokeblue-500 bg-pokeblue-50 dark:bg-pokeblue-950/40', iconLop: 'bg-pokeblue-500' },
  'vi-sao': { icon: 'i-lucide-circle-help', tieuDe: 'Vì sao lại thế?', lop: 'border-pokered-400 bg-pokered-50 dark:bg-pokered-950/30', iconLop: 'bg-pokered-500' },
} as const
const c = computed(() => CAU_HINH[props.kieu])
</script>

<template>
  <aside class="my-5 flex gap-3 rounded-2xl border-l-4 p-4 text-sm" :class="c.lop">
    <span class="grid size-7 shrink-0 place-items-center rounded-full text-white" :class="c.iconLop" aria-hidden="true">
      <UIcon :name="c.icon" class="size-4" />
    </span>
    <div class="min-w-0 text-toned">
      <p class="font-display font-extrabold text-highlighted">
        {{ title ?? c.tieuDe }}
      </p>
      <div class="mt-1 space-y-2 [&_strong]:text-highlighted">
        <slot />
      </div>
    </div>
  </aside>
</template>
