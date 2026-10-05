<script setup lang="ts">
/**
 * Hộp xác nhận cho các thao tác của khu Nhân vật (thay cho confirm() của trình duyệt).
 *
 *   <AdminNhanvatXacNhan v-model:open="mo" title="..." description="..." mau="error"
 *     :phai-go="'XOA TOAN BO'" nhan-nut="Reset" :dang-chay="dangGui" @xac-nhan="gui" />
 *
 * phaiGo: nếu có, nút xác nhận chỉ bật khi gõ đúng chuỗi đó (server vẫn kiểm lại).
 */
const props = withDefaults(defineProps<{
  title: string
  description?: string
  nhanNut?: string
  mau?: 'primary' | 'error' | 'warning'
  phaiGo?: string
  nhanGo?: string
  dangChay?: boolean
}>(), { description: '', nhanNut: 'Xác nhận', mau: 'primary', phaiGo: '', nhanGo: '', dangChay: false })

const emit = defineEmits<{ xacNhan: [go: string] }>()
const open = defineModel<boolean>('open', { default: false })
const go = ref('')

watch(open, (v) => {
  if (v) go.value = ''
})

const choPhep = computed(() => props.phaiGo === '' || go.value === props.phaiGo)
</script>

<template>
  <UModal v-model:open="open" :title="title" :dismissible="!dangChay">
    <template #body>
      <div class="space-y-4">
        <p v-if="description" class="whitespace-pre-line text-sm text-muted">
          {{ description }}
        </p>
        <slot />
        <UFormField v-if="phaiGo" :label="nhanGo || `Gõ đúng “${phaiGo}” để xác nhận`">
          <UInput v-model="go" autocomplete="off" class="w-full" :placeholder="phaiGo" @keydown.enter="choPhep && !dangChay && emit('xacNhan', go)" />
        </UFormField>
      </div>
    </template>
    <template #footer>
      <div class="flex w-full justify-end gap-2">
        <UButton color="neutral" variant="ghost" label="Thôi" :disabled="dangChay" @click="open = false" />
        <UButton :color="mau" :label="nhanNut" :loading="dangChay" :disabled="!choPhep" @click="emit('xacNhan', go)" />
      </div>
    </template>
  </UModal>
</template>
