<script setup lang="ts">
/**
 * Thẻ số liệu của trang quản trị. Nhãn đứng TRƯỚC con số.
 * mau: '' | 'ok' | 'warn' | 'bad' — màu chỉ dùng khi nó mang nghĩa.
 */
const props = defineProps<{ label: string, value: string | number, note?: string, mau?: '' | 'ok' | 'warn' | 'bad', icon?: string }>()

const tone = computed(() => ({
  ok: 'text-success',
  warn: 'text-warning',
  bad: 'text-error',
  '': 'text-highlighted',
}[props.mau ?? '']))
</script>

<template>
  <UCard :ui="{ body: 'p-4 sm:p-5' }" class="bg-elevated/40">
    <div class="flex items-center justify-between text-sm text-muted">
      <span>{{ label }}</span>
      <UIcon v-if="icon" :name="icon" class="size-4" />
    </div>
    <div class="mt-1 text-2xl font-bold tabular-nums" :class="tone">
      {{ typeof value === 'number' ? so(value) : value }}
    </div>
    <div v-if="note" class="mt-1 text-xs text-dimmed">
      {{ note }}
    </div>
  </UCard>
</template>
