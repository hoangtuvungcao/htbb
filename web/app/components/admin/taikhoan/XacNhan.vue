<script setup lang="ts">
/**
 * Hộp xác nhận cho khu Tài khoản / Cấm theo máy (thay cho confirm() của trình duyệt).
 * Dùng qua useOverlay():
 *   const xacNhan = useOverlay().create(AdminTaikhoanXacNhan)
 *   const ok = await xacNhan.open({ title, description, nut: 'Khoá', mau: 'error' }).result
 * `goDe`: bắt gõ đúng chuỗi này mới bật nút (chỉ là tiện cho giao diện — server vẫn
 * kiểm lại thật).
 */
const props = withDefaults(defineProps<{
  title: string
  description?: string
  nut?: string
  mau?: 'error' | 'primary' | 'warning' | 'neutral'
  goDe?: string
}>(), { description: '', nut: 'Đồng ý', mau: 'primary', goDe: '' })

const emit = defineEmits<{ close: [boolean] }>()
const go = ref('')
const nhanGo = computed(() => `Gõ đúng "${props.goDe}" để xác nhận`)
const duoc = computed(() => props.goDe === '' || go.value === props.goDe)
</script>

<template>
  <UModal :title="title" :close="false" :dismissible="true" @update:open="(v: boolean) => { if (!v) emit('close', false) }">
    <template #body>
      <p v-if="description" class="whitespace-pre-line text-sm text-muted">
        {{ description }}
      </p>
      <UFormField v-if="goDe" :label="nhanGo" class="mt-4">
        <UInput v-model="go" autocomplete="off" class="w-full" autofocus @keydown.enter="duoc && emit('close', true)" />
      </UFormField>
    </template>
    <template #footer>
      <div class="flex w-full justify-end gap-2">
        <UButton color="neutral" variant="ghost" label="Huỷ" @click="emit('close', false)" />
        <UButton :color="mau" :label="nut" :disabled="!duoc" @click="emit('close', true)" />
      </div>
    </template>
  </UModal>
</template>
