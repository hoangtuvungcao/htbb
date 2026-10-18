<script setup lang="ts">
/**
 * Hộp xác nhận có ô LÝ DO (và tuỳ chọn THỜI HẠN) cho các thao tác của Trung tâm người chơi.
 * Dùng qua useOverlay():
 *   const hoi = useOverlay().create(AdminNguoichoiHoiLyDo)
 *   const kq = await hoi.open({ title, description, nut: 'Khoá', mau: 'error', han: HAN_KHOA, hanMacDinh: 24 }).result
 *   // kq === false (huỷ) hoặc { lyDo, soGio }
 * Lý do bắt buộc ≥ 3 ký tự khi `batBuoc` (mặc định có) — server vẫn kiểm lại.
 */
const props = withDefaults(defineProps<{
  title: string
  description?: string
  nut?: string
  mau?: 'error' | 'primary' | 'warning' | 'neutral'
  batBuoc?: boolean
  nhanLyDo?: string
  goiY?: string[]
  han?: { label: string, value: number }[]
  hanMacDinh?: number
  nhanHan?: string
  toiDa?: number
}>(), { description: '', nut: 'Đồng ý', mau: 'primary', batBuoc: true, nhanLyDo: 'Lý do', goiY: () => [], han: () => [], hanMacDinh: 24, nhanHan: 'Thời hạn', toiDa: 300 })

const emit = defineEmits<{ close: [false | { lyDo: string, soGio: number }] }>()
const lyDo = ref('')
const soGio = ref<number>(props.han.length ? (props.han.some(h => h.value === props.hanMacDinh) ? props.hanMacDinh : props.han[0]!.value) : 0)
const duoc = computed(() => !props.batBuoc || lyDo.value.trim().length >= 3)
const xong = () => { if (duoc.value) emit('close', { lyDo: lyDo.value.trim(), soGio: soGio.value }) }
</script>

<template>
  <UModal :title="title" :close="false" @update:open="(v: boolean) => { if (!v) emit('close', false) }">
    <template #body>
      <p v-if="description" class="mb-4 whitespace-pre-line text-sm text-muted">
        {{ description }}
      </p>
      <div class="space-y-3">
        <UFormField v-if="han.length" :label="nhanHan">
          <USelect v-model="soGio" :items="han" class="w-full" />
        </UFormField>
        <UFormField :label="nhanLyDo" :required="batBuoc" :help="batBuoc ? 'Bắt buộc, ít nhất 3 ký tự. Được lưu vào nhật ký.' : 'Không bắt buộc. Được lưu vào nhật ký.'">
          <UTextarea v-model="lyDo" :maxlength="toiDa" :rows="2" autoresize class="w-full" autofocus />
        </UFormField>
        <div v-if="goiY.length" class="flex flex-wrap gap-1">
          <UButton v-for="g in goiY" :key="g" size="xs" color="neutral" variant="soft" :label="g" @click="lyDo = g" />
        </div>
      </div>
    </template>
    <template #footer>
      <div class="flex w-full justify-end gap-2">
        <UButton color="neutral" variant="ghost" label="Huỷ" @click="emit('close', false)" />
        <UButton :color="mau" :label="nut" :disabled="!duoc" @click="xong" />
      </div>
    </template>
  </UModal>
</template>
