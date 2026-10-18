<script setup lang="ts">
/**
 * Bảng MỌI khoá trong cau_hinh (kể cả khoá có trong danh mục nhưng chưa có dòng): mô tả tiếng Việt,
 * giá trị, ai sửa lúc nào. Khoá trong whitelist sửa lẻ (shared/utils/cauHinhDanhMuc.ts) có nút Sửa —
 * kiểm kiểu + khoảng ngay trên trang và lại lần nữa ở server; khoá khác chỉ xem (sửa qua thẻ phía trên
 * nếu có, hoặc không sửa từ web).
 */
interface Dong {
  khoa: string, coDong: boolean, giaTri: string | null, suaBoi: string | null, suaLuc: string | null
  ten: string, moTa: string, nhom: string, kieu: string, min: number | null, max: number | null, macDinh: string | null
  suaLe: boolean, the: string | null, nguon: string
}
const props = defineProps<{ dong: Dong[] | undefined, dangTai?: boolean, coTheSua: boolean }>()
const emit = defineEmits<{ daLuu: [] }>()
const api = useAdminApi()
const toast = useToast()

const nhom = ref('')
const dsNhom = computed(() => [...new Set((props.dong ?? []).map(d => d.nhom))])
const rows = computed(() => (props.dong ?? []).filter(d => !nhom.value || d.nhom === nhom.value))
const cot = [
  { key: 'khoa', label: 'Khoá', sort: true },
  { key: 'ten', label: 'Ý nghĩa', sort: true, giaTri: (r: Dong) => `${r.ten} ${r.moTa}` },
  { key: 'giaTri', label: 'Giá trị', giaTri: (r: Dong) => r.giaTri ?? '' },
  { key: 'suaLuc', label: 'Sửa lần cuối', sort: true, anMobile: true, giaTri: (r: Dong) => (r.suaLuc ? `${r.suaBoi ?? ''} ${r.suaLuc}` : '') },
  { key: 'thaoTac', label: '', giaTri: () => '' },
]

const dangSua = ref<Dong | null>(null)
const giaTriSo = ref('')
const giaTriCo = ref(false)
const dangLuu = ref(false)
function moSua(d: Dong) {
  dangSua.value = d
  const v = d.giaTri ?? d.macDinh ?? ''
  giaTriCo.value = v === '1'
  giaTriSo.value = v
}
const giaTriGui = () => (dangSua.value?.kieu === 'co' ? (giaTriCo.value ? '1' : '0') : giaTriSo.value)
const loi = computed(() => {
  const d = dangSua.value
  if (!d) return ''
  const k = kiemGiaTriKhoa(d.khoa, giaTriGui())
  return k.ok ? '' : k.loi
})
async function luu() {
  const d = dangSua.value
  if (!d || loi.value) return
  dangLuu.value = true
  try {
    const r = await api<{ message: string }>('/api/admin/cauhinh/khoa', { method: 'POST', body: { khoa: d.khoa, giaTri: giaTriGui() } })
    toast.add({ title: r.message, color: 'success', icon: 'i-lucide-check' })
    dangSua.value = null
    emit('daLuu')
  } catch (e) {
    toast.add({ title: 'Chưa lưu', description: loiApi(e), color: 'error', icon: 'i-lucide-circle-x' })
  } finally {
    dangLuu.value = false
  }
}
</script>

<template>
  <UCard>
    <template #header>
      <h3 class="font-semibold text-highlighted">Tất cả khoá trong bảng cau_hinh</h3>
      <p class="text-sm text-muted">
        Server đọc lại toàn bộ bảng mỗi 10 giây (<code>CauHinh.napLai</code>). Khoá nhóm "Kinh tế dài hạn" đè số mặc định của
        <code>ThongSoLauDai.java</code> — chưa có dòng = game dùng mặc định. Chỉ khoá có nút <b>Sửa</b> mới sửa lẻ được (kiểu và khoảng kiểm chặt);
        khoá khác sửa ở thẻ phía trên (cột "Sửa ở") hoặc chỉ xem.
      </p>
    </template>
    <AdminBang :rows="rows" :cot="cot" :dang-tai="dangTai" ten-csv="cau-hinh" tim-placeholder="Tìm khoá, ý nghĩa…" :moi-trang="20" row-key="khoa">
      <template #loc>
        <USelect v-model="nhom" :items="[{ label: 'Mọi nhóm', value: '' }, ...dsNhom.map(n => ({ label: n, value: n }))]" size="md" class="w-full sm:w-48" aria-label="Lọc theo nhóm" />
      </template>
      <template #cell-khoa="{ row }">
        <code class="text-xs">{{ row.khoa }}</code>
        <p class="text-xs text-dimmed">{{ row.nhom }}</p>
      </template>
      <template #cell-ten="{ row }">
        <p class="font-medium text-highlighted">{{ row.ten || '—' }}</p>
        <p class="text-xs text-muted">{{ row.moTa }}<template v-if="row.min !== null"> Khoảng {{ row.min }}–{{ row.max }}.</template><template v-if="row.macDinh !== null"> Mặc định {{ row.macDinh }}.</template></p>
      </template>
      <template #cell-giaTri="{ row }">
        <span v-if="row.coDong" class="break-all font-mono text-xs">{{ row.giaTri === '' ? '(trống)' : row.giaTri }}</span>
        <span v-else class="text-xs text-dimmed">chưa có dòng{{ row.macDinh !== null ? ` → ${row.macDinh}` : '' }}</span>
      </template>
      <template #cell-suaLuc="{ row }">
        <span v-if="row.suaLuc" class="text-xs"><b class="text-toned">{{ row.suaBoi }}</b><br><span class="text-dimmed tabular-nums">{{ row.suaLuc }}</span></span>
        <span v-else class="text-xs text-dimmed">—</span>
      </template>
      <template #cell-thaoTac="{ row }">
        <UButton v-if="row.suaLe" size="xs" color="neutral" variant="outline" icon="i-lucide-pencil" label="Sửa" :disabled="!coTheSua" @click="moSua(row)" />
        <span v-else-if="row.the" class="text-xs text-muted whitespace-nowrap">Sửa ở: {{ row.the }}</span>
        <span v-else class="text-xs text-dimmed whitespace-nowrap">Chỉ xem</span>
      </template>
    </AdminBang>

    <UModal :open="!!dangSua" :title="dangSua ? `Sửa ${dangSua.ten}` : ''" @update:open="(v: boolean) => { if (!v) dangSua = null }">
      <template #body>
        <form v-if="dangSua" class="space-y-3" @submit.prevent="luu">
          <p class="text-sm text-muted"><code>{{ dangSua.khoa }}</code> — {{ dangSua.moTa }}</p>
          <USwitch v-if="dangSua.kieu === 'co'" v-model="giaTriCo" label="Bật" />
          <UFormField v-else :label="`Giá trị (số nguyên ${dangSua.min}–${dangSua.max})`" :error="loi || undefined">
            <UInput v-model="giaTriSo" inputmode="numeric" class="w-full" autofocus />
          </UFormField>
          <p class="text-xs text-dimmed">Hiện tại: {{ dangSua.coDong ? (dangSua.giaTri || '(trống)') : `chưa có dòng (game dùng ${dangSua.macDinh ?? 'mặc định'})` }}. Lưu xong, server áp dụng trong tối đa 10 giây; ghi vào Lịch sử thay đổi.</p>
          <div class="flex justify-end gap-2">
            <UButton color="neutral" variant="ghost" label="Huỷ" @click="dangSua = null" />
            <UButton type="submit" icon="i-lucide-save" label="Lưu" :loading="dangLuu" :disabled="!!loi" />
          </div>
        </form>
      </template>
    </UModal>
  </UCard>
</template>
