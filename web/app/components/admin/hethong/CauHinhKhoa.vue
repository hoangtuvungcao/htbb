<script setup lang="ts">
/**
 * MỘT khoá của cấu hình trung tâm: nhãn + badge (áp dụng ngay / cần khởi động lại / thử nghiệm / env), ô nhập theo
 * kiểu (sinh từ danh mục), lỗi kiểm ngay khi gõ, giá trị máy chủ ĐANG CHẠY + nguồn, giá trị chờ khởi động lại,
 * người sửa cuối. Trạng thái nằm ở useCauHinhTT (truyền `ch` vào).
 */
const props = defineProps<{ k: KhoaTT, ch: ReturnType<typeof useCauHinhTT>, docOnly?: boolean }>()

const ten = computed(() => props.k.khoa)
const d = computed(() => props.ch.data.value)
const daSua = computed(() => Object.prototype.hasOwnProperty.call(props.ch.sua.value, ten.value))
const xoaDong = computed(() => daSua.value && props.ch.sua.value[ten.value] === null)
const v = computed(() => props.ch.hienTai(props.k))
const loi = computed(() => props.ch.loiNhap.value[ten.value] ?? '')
const quyenSua = computed(() => suaDuocTT(props.k, d.value?.quyen.choPhepThuNghiem ?? false))
const khoa = computed(() => props.docOnly || !d.value?.quyen.ghi || !quyenSua.value.duoc)
const hl = computed(() => d.value?.hieuLuc[ten.value] ?? null)
const coDong = computed(() => props.k.bang !== 'env' && props.ch.luu(props.k) !== null)
const nguonChu: Record<string, string> = { db: 'CSDL', ini: 'server.ini', mac_dinh: 'mặc định', giu_cu: 'giữ giá trị cũ (CSDL bị từ chối)' }
const sb = computed(() => d.value?.suaBoi[ten.value] ?? null)
const nguonChuThem: Record<string, string> = { co_dinh: 'cố định' }
// Giá trị lưu / đang sửa khác mặc định -> nút "Về mặc định" (xoá dòng: máy chủ về server.ini / mặc định).
const khacMacDinh = computed(() => daSua.value
  ? props.ch.sua.value[ten.value] !== null && v.value !== props.k.macDinh
  : coDong.value && props.ch.luu(props.k) !== props.k.macDinh)

function dat(x: string | number | boolean | null | undefined) {
  if (x === null || x === undefined) return
  props.ch.datGiaTri(props.k, typeof x === 'boolean' ? (x ? '1' : '0') : String(x))
}
// reka-ui Select không nhận value '' -> đổi qua lại một mã giữ chỗ.
const TRONG = '__trong__'
const dsChon = computed(() => (props.k.luaChon ?? []).map(([value, label]) => ({ value: value === '' ? TRONG : value, label: label || '(trống)' })))
const vChon = computed(() => (v.value === '' ? TRONG : v.value))
function datChon(x: string | number | boolean | null | undefined) {
  dat(x === TRONG ? '' : x)
}
const dsDaChon = computed(() => v.value.split(',').map(s => s.trim()).filter(Boolean))
function batTat(ma: string, co: boolean | 'indeterminate') {
  const s = new Set(dsDaChon.value)
  if (co === true) s.add(ma)
  else s.delete(ma)
  dat((props.k.luaChon ?? []).map(([m]) => m).filter(m => s.has(m)).join(','))
}
const goiY = computed(() => {
  const k = props.k
  if (k.kieu === 'ngay') return 'yyyy-MM-dd'
  if (k.kieu === 'ngay_gio') return 'yyyy-MM-dd HH:mm:ss'
  if (k.kieu === 'nhip') return 'số_lần/giây, vd 5/10'
  if (k.kieu === 'ds_so') return k.soPhanTu ? `${k.soPhanTu} số, cách nhau dấu phẩy` : 'các số, cách nhau dấu phẩy'
  if (k.kieu === 'ds_chuoi') return 'cách nhau dấu phẩy'
  if (k.kieu === 'so') return [k.min !== undefined ? `≥ ${k.min}` : '', k.max !== undefined ? `≤ ${k.max}` : ''].filter(Boolean).join(', ')
  return ''
})
</script>

<template>
  <div class="grid gap-3 border-b border-default py-3 last:border-b-0 lg:grid-cols-[minmax(0,1.1fr)_minmax(0,1fr)]" :class="daSua ? 'bg-primary/5 -mx-3 px-3 rounded' : ''">
    <div class="min-w-0">
      <div class="flex flex-wrap items-center gap-1.5">
        <span class="font-medium text-highlighted">{{ k.ten }}</span>
        <UBadge v-if="k.apDung === 'ngay'" color="success" variant="subtle" size="sm" label="Áp dụng ngay" />
        <UBadge v-else color="warning" variant="subtle" size="sm" icon="i-lucide-rotate-ccw" label="Cần khởi động lại" />
        <UBadge v-if="k.khoaProd" color="error" variant="subtle" size="sm" icon="i-lucide-flask-conical" label="Thử nghiệm — khoá ở máy thật" />
        <UBadge v-if="k.lop" color="neutral" variant="subtle" size="sm" icon="i-lucide-lock" :label="TEN_LOP_TT[k.lop]" />
        <UBadge v-if="k.bang === 'env'" color="neutral" variant="subtle" size="sm" icon="i-lucide-lock" label="Biến môi trường" />
        <UBadge v-if="k.bang === 'nap_cau_hinh'" color="neutral" variant="outline" size="sm" label="nap_cau_hinh" />
        <UBadge v-if="k.dungO === 'web'" color="info" variant="outline" size="sm" label="web" />
        <UBadge v-else-if="k.dungO === 'ca_hai'" color="info" variant="outline" size="sm" label="game + web" />
      </div>
      <p class="mt-0.5 font-mono text-xs text-dimmed">{{ k.khoa }}</p>
      <p v-if="k.moTa" class="mt-1 text-sm text-muted">{{ k.moTa }}</p>
      <p v-if="!quyenSua.duoc" class="mt-1 text-xs text-warning">{{ quyenSua.lyDo }}</p>
    </div>

    <div class="min-w-0 space-y-1.5">
      <div class="flex items-start gap-2">
        <div class="min-w-0 flex-1">
          <USwitch v-if="k.kieu === 'co'" :model-value="v === '1'" :disabled="khoa" :label="v === '1' ? 'Bật' : 'Tắt'" @update:model-value="dat" />
          <USelect v-else-if="k.kieu === 'chon'" :model-value="vChon" :items="dsChon" :disabled="khoa" class="w-full sm:w-72" @update:model-value="datChon" />
          <div v-else-if="k.kieu === 'ds_chuoi' && k.luaChon" class="grid gap-1 sm:grid-cols-2">
            <UCheckbox v-for="[ma, nhan] in k.luaChon" :key="ma" :label="nhan" :model-value="dsDaChon.includes(ma)" :disabled="khoa"
              @update:model-value="(c) => batTat(ma, c)" />
          </div>
          <UInput v-else :model-value="v" :type="k.kieu === 'so' ? 'number' : k.kieu === 'ngay' ? 'date' : 'text'" :disabled="khoa" :placeholder="goiY"
            :min="k.kieu === 'so' ? k.min : undefined" :max="k.kieu === 'so' ? k.max : undefined" :step="k.kieu === 'so' ? (k.buoc || 1) : undefined"
            :color="loi ? 'error' : undefined" :highlight="!!loi" class="w-full" :class="k.kieu === 'so' ? 'sm:w-48' : ''" @update:model-value="dat" />
        </div>
        <UTooltip v-if="!khoa && daSua" text="Bỏ sửa">
          <UButton icon="i-lucide-undo-2" size="xs" color="neutral" variant="ghost" aria-label="Bỏ sửa" @click="ch.boSua(k.khoa)" />
        </UTooltip>
        <UTooltip v-if="!khoa && !xoaDong && khacMacDinh" :text="`Về mặc định: ${hienThiTT(k, k.macDinh)} (xoá dòng CSDL)`">
          <UButton icon="i-lucide-rotate-ccw" size="xs" color="neutral" variant="outline" label="Mặc định" @click="ch.datGiaTri(k, null)" />
        </UTooltip>
      </div>
      <p v-if="loi" class="text-xs text-error">{{ loi }}</p>
      <p v-if="xoaDong" class="text-xs text-warning">Sẽ xoá dòng CSDL → máy chủ dùng server.ini hoặc mặc định "{{ hienThiTT(k, k.macDinh) }}".</p>
      <div class="flex flex-wrap gap-x-3 gap-y-0.5 text-xs text-dimmed">
        <span>Mặc định: <b class="text-default">{{ hienThiTT(k, k.macDinh) }}</b></span>
        <span v-if="k.bang === 'cau_hinh' && k.dungO !== 'web' && hl">
          Máy chủ đang chạy: <b class="text-default">{{ hienThiTT(k, hl.giaTri) }}</b> ({{ nguonChu[hl.nguon] ?? nguonChuThem[hl.nguon] ?? hl.nguon }})
        </span>
        <span v-if="hl?.cho" class="text-warning">Chờ khởi động lại: <b>{{ hienThiTT(k, hl.cho) }}</b></span>
        <span v-if="sb?.boi">Sửa: {{ sb.boi }} · {{ sb.luc }}</span>
      </div>
      <p v-if="hl?.loi" class="text-xs text-error">{{ hl.loi }}</p>
    </div>
  </div>
</template>
