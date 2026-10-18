<script setup lang="ts">
/**
 * Tặng quà cho một nhân vật (chỉ siêu quản trị). Không ghi đồ thẳng vào nhân vật: tạo mã giftcode cá nhân
 * (1 lượt, có hạn) theo đúng luật giftcode an toàn rồi gửi mã vào hộp thư của nhân vật — xem
 * server/api/admin/nguoi-choi/tang-qua.post.ts. Xem trước dùng chính /api/admin/giftcode-moi/kiem-tra.
 */
const props = defineProps<{ charId: number, charname: string, cap: number }>()
const emit = defineEmits<{ close: [false | { ma: string, message: string }] }>()
const api = useAdminApi()
const toast = useToast()

const f = reactive({ phanThuong: 'LUONGKHOA:10', soNgay: 30, lyDo: '', loiNhan: '' })
const xem = ref<{ ok: boolean, giaTri?: number, toiDa?: number, moTa?: string[], loi?: string } | null>(null)
const buoc = ref<'nhap' | 'xac_nhan'>('nhap')
const dangGui = ref(false)
let hen: ReturnType<typeof setTimeout> | undefined
watch(() => f.phanThuong, (v) => {
  clearTimeout(hen)
  hen = setTimeout(async () => {
    try { xem.value = await api('/api/admin/giftcode-moi/kiem-tra', { method: 'POST', body: { phanThuong: v, capToiThieu: props.cap } }) }
    catch (e) { xem.value = { ok: false, loi: loiApi(e) } }
  }, 300)
}, { immediate: true })
const them = (s: string) => { f.phanThuong = f.phanThuong.trim() ? `${f.phanThuong.trim()},${s}` : s }
const duoc = computed(() => !!xem.value?.ok && f.lyDo.trim().length >= 3 && f.soNgay >= 1 && f.soNgay <= 90)

async function gui() {
  dangGui.value = true
  try {
    const r = await api<{ ma: string, message: string }>('/api/admin/nguoi-choi/tang-qua', {
      method: 'POST', body: { charId: props.charId, phanThuong: f.phanThuong, soNgay: f.soNgay, lyDo: f.lyDo, loiNhan: f.loiNhan },
    })
    emit('close', r)
  }
  catch (e) {
    toast.add({ title: 'Không tặng được', description: loiApi(e), color: 'error', icon: 'i-lucide-circle-x' })
    buoc.value = 'nhap'
  }
  finally { dangGui.value = false }
}
</script>

<template>
  <UModal :title="`Tặng quà cho ${charname}`" :close="false" :ui="{ content: 'sm:max-w-xl' }" @update:open="(v: boolean) => { if (!v) emit('close', false) }">
    <template #body>
      <template v-if="buoc === 'nhap'">
        <UAlert color="info" variant="subtle" icon="i-lucide-shield-check" class="mb-4"
          title="Quà đi qua giftcode an toàn"
          description="Web tạo một mã dùng 1 lần cho riêng người này và gửi vào hộp thư trong game; người chơi tự nhập mã. Chỉ vật phẩm khoá / lượng khoá trong danh sách trắng, có trần giá trị và ngân sách tháng. Cần máy chủ bật sv.lau_dai=1." />
        <div class="space-y-3">
          <UFormField label="Phần thưởng" help="LUONGKHOA:n · GEM:id:n · ITEM:id:n (n ≤ 10), cách nhau dấu phẩy" required>
            <UInput v-model="f.phanThuong" class="w-full font-mono" maxlength="500" />
            <div class="mt-1 flex flex-wrap gap-1">
              <UButton size="xs" color="neutral" variant="soft" label="+ 5 lượng khoá" @click="them('LUONGKHOA:5')" />
              <UButton size="xs" color="neutral" variant="soft" label="+ GEM:id:1" @click="them('GEM:0:1')" />
              <UButton size="xs" color="neutral" variant="soft" label="+ ITEM:id:1" @click="them('ITEM:0:1')" />
              <UButton size="xs" color="neutral" variant="link" to="/admin/giftcode" target="_blank" label="Tra mã vật phẩm" icon="i-lucide-external-link" />
            </div>
          </UFormField>
          <div class="rounded-lg p-3 text-sm" :class="xem?.ok ? 'bg-success/10' : 'bg-error/10'" aria-live="polite">
            <template v-if="xem?.moTa">
              <b>Giá trị {{ xem.giaTri }} / {{ xem.toiDa }}</b> — {{ xem.moTa.join(' · ') }}
            </template>
            <p v-if="xem?.loi" class="text-error">{{ xem.loi }}</p>
            <p v-if="!xem" class="text-muted">Đang kiểm tra…</p>
          </div>
          <div class="grid gap-3 sm:grid-cols-[8rem_1fr]">
            <UFormField label="Hạn dùng (ngày)">
              <UInputNumber v-model="f.soNgay" :min="1" :max="90" class="w-full" />
            </UFormField>
            <UFormField label="Lý do (nhật ký)" required help="Ít nhất 3 ký tự">
              <UInput v-model="f.lyDo" maxlength="200" placeholder="Đền bù lỗi sự kiện ngày…" class="w-full" />
            </UFormField>
          </div>
          <UFormField label="Lời nhắn trong thư (không bắt buộc)">
            <UInput v-model="f.loiNhan" maxlength="200" class="w-full" />
          </UFormField>
        </div>
      </template>
      <div v-else class="space-y-2 text-sm">
        <p>Tạo mã quà riêng cho <b>{{ charname }}</b>:</p>
        <ul class="list-disc pl-5">
          <li v-for="(m, i) in xem?.moTa ?? []" :key="i">{{ m }}</li>
        </ul>
        <p class="text-muted">Giá trị {{ xem?.giaTri }} · hạn {{ f.soNgay }} ngày · lý do: {{ f.lyDo }}</p>
        <p class="text-warning">Mã được gửi vào hộp thư trong game. Ai biết mã đều nhập được — không gửi mã qua kênh khác.</p>
      </div>
    </template>
    <template #footer>
      <div class="flex w-full justify-end gap-2">
        <UButton color="neutral" variant="ghost" :label="buoc === 'nhap' ? 'Huỷ' : 'Quay lại'" @click="buoc === 'nhap' ? emit('close', false) : (buoc = 'nhap')" />
        <UButton v-if="buoc === 'nhap'" color="primary" label="Tiếp tục" :disabled="!duoc" @click="buoc = 'xac_nhan'" />
        <UButton v-else color="warning" icon="i-lucide-gift" label="Tạo mã & gửi thư" :loading="dangGui" @click="gui" />
      </div>
    </template>
  </UModal>
</template>
