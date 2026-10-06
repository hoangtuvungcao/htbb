<script setup lang="ts">
/**
 * Thanh ngang có nhãn — phân bố nghề / cấp độ, nơi nhãn dài hơn cột.
 *   <AdminHBar :data="[{ nhan: 'Kiếm khách', gt: 40 }]" />
 */
const props = defineProps<{ data: { nhan: string, gt: number }[] }>()
const max = computed(() => Math.max(0, ...props.data.map(d => Number(d.gt) || 0)))
</script>

<template>
  <p v-if="max <= 0" class="text-sm text-muted">
    Chưa có số liệu.
  </p>
  <ul v-else class="space-y-2">
    <li v-for="d in data" :key="d.nhan" class="grid grid-cols-[8rem_1fr_4rem] items-center gap-3 text-sm">
      <span class="truncate text-muted">{{ d.nhan }}</span>
      <span class="h-2.5 overflow-hidden rounded-full bg-accented">
        <span class="block h-full rounded-full bg-gradient-to-r from-secondary-600 to-secondary-300" :style="{ width: `${Math.max(1, Math.round((Number(d.gt) || 0) * 100 / max))}%` }" />
      </span>
      <span class="text-right tabular-nums text-highlighted">{{ so(d.gt) }}</span>
    </li>
  </ul>
</template>
