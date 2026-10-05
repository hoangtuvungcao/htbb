<script setup lang="ts">
/**
 * Hộp xác nhận của trang Giftcode. Dùng qua useOverlay():
 *   const xacNhan = useOverlay().create(AdminGiftcodeXacNhan)
 *   const ok = await xacNhan.open({ title, description, nut: 'Xoá', mau: 'error' }).result
 * Chỉ là tiện cho giao diện — server vẫn kiểm lại thật.
 */
withDefaults(defineProps<{
  title: string
  description?: string
  nut?: string
  mau?: 'error' | 'primary' | 'warning' | 'neutral'
}>(), { description: '', nut: 'Đồng ý', mau: 'primary' })

const emit = defineEmits<{ close: [boolean] }>()
</script>

<template>
  <UModal :title="title" :close="false" @update:open="(v: boolean) => { if (!v) emit('close', false) }">
    <template v-if="description" #body>
      <p class="whitespace-pre-line text-sm text-muted">
        {{ description }}
      </p>
    </template>
    <template #footer>
      <div class="flex w-full justify-end gap-2">
        <UButton color="neutral" variant="ghost" label="Huỷ" @click="emit('close', false)" />
        <UButton :color="mau" :label="nut" autofocus @click="emit('close', true)" />
      </div>
    </template>
  </UModal>
</template>
