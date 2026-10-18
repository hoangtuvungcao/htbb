<script setup lang="ts">
/**
 * Kênh hỗ trợ Zalo (link + QR đặt ở /admin/cai-dat -> "Hỗ trợ qua Zalo").
 *   <SiteHoTroZalo />                 nút "Zalo hỗ trợ"
 *   <SiteHoTroZalo kieu="the" />      thẻ có ảnh QR + mô tả (trang Tải game, FAQ)
 *   <SiteHoTroZalo kieu="chan" />     nút trong chân trang (nền tối)
 * Chưa đặt link -> không render gì (không có link chết "#").
 */
withDefaults(defineProps<{ kieu?: 'nut' | 'the' | 'chan', nhan?: string, block?: boolean }>(), { kieu: 'nut', nhan: '', block: false })
const { zalo, coZalo } = useHoTro()
</script>

<template>
  <template v-if="coZalo">
    <div v-if="kieu === 'the'" class="flex flex-col items-center gap-4 rounded-2xl border border-default bg-default/60 p-4 sm:flex-row sm:items-center">
      <img
        v-if="zalo.anhQr"
        :src="zalo.anhQr"
        :alt="`Mã QR ${zalo.ten}`"
        width="128"
        height="128"
        loading="lazy"
        class="size-32 shrink-0 rounded-xl bg-white object-contain p-1.5 ring-1 ring-default"
      >
      <div class="min-w-0 text-center sm:text-left">
        <p class="font-display text-lg font-extrabold text-highlighted">
          <UIcon name="i-lucide-message-circle-more" class="mr-1 inline size-5 align-[-3px] text-[#0068ff] dark:text-[#4d9bff]" />{{ zalo.ten }}
        </p>
        <p v-if="zalo.moTa" class="mt-1 text-sm text-muted">
          {{ zalo.moTa }}
        </p>
        <p v-if="zalo.anhQr" class="mt-1 text-xs text-dimmed">
          Quét mã bằng ứng dụng Zalo hoặc bấm nút bên dưới.
        </p>
        <UButton
          :to="zalo.url"
          target="_blank"
          rel="noopener noreferrer"
          external
          class="mt-3 !bg-[#0068ff] !text-white hover:!bg-[#0054cc]"
          icon="i-lucide-message-circle-more"
          trailing-icon="i-lucide-arrow-up-right"
        >
          {{ nhan || 'Mở Zalo hỗ trợ' }}
        </UButton>
      </div>
    </div>
    <UButton
      v-else-if="kieu === 'chan'"
      :to="zalo.url"
      target="_blank"
      rel="noopener noreferrer"
      external
      size="sm"
      icon="i-lucide-message-circle-more"
      :block="block"
      class="!bg-[#0068ff] !text-white hover:!bg-[#0054cc]"
    >
      {{ nhan || zalo.ten }}
    </UButton>
    <UButton
      v-else
      :to="zalo.url"
      target="_blank"
      rel="noopener noreferrer"
      external
      color="neutral"
      variant="ghost"
      icon="i-lucide-message-circle-more"
      :block="block"
      :aria-label="zalo.ten"
      :title="zalo.ten"
      :ui="{ leadingIcon: 'text-[#0068ff] dark:text-[#4d9bff]' }"
    >
      {{ nhan || 'Zalo' }}
    </UButton>
  </template>
</template>
