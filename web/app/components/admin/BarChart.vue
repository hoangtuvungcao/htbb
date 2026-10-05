<script setup lang="ts">
/**
 * Biểu đồ cột đứng thuần CSS — không tải thư viện ngoài.
 *   <AdminBarChart :data="[{ nhan: '25/08', gt: 12 }]" don-vi="tài khoản" />
 */
const props = defineProps<{ data: { nhan: string, gt: number }[], donVi?: string }>()
const max = computed(() => Math.max(0, ...props.data.map(d => Number(d.gt) || 0)))
</script>

<template>
  <p v-if="max <= 0" class="text-sm text-muted">
    Chưa có số liệu trong khoảng thời gian này.
  </p>
  <div v-else class="flex h-48 items-end gap-1.5">
    <div
      v-for="d in data"
      :key="d.nhan"
      class="group flex h-full min-w-0 flex-1 flex-col items-center justify-end"
      :title="`${d.nhan}: ${so(d.gt)}${donVi ? ' ' + donVi : ''}`"
    >
      <div class="mb-1 text-[10px] tabular-nums text-muted opacity-0 transition group-hover:opacity-100">
        {{ so(d.gt) }}
      </div>
      <div
        class="w-full rounded-t-md bg-gradient-to-t from-primary-700 to-primary-400 transition-all group-hover:from-primary-500 group-hover:to-primary-200"
        :style="{ height: `${Math.max(2, Math.round((Number(d.gt) || 0) * 100 / max))}%` }"
      />
      <div class="mt-1 w-full truncate text-center text-[10px] text-dimmed">
        {{ d.nhan }}
      </div>
    </div>
  </div>
</template>
