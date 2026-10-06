<script setup lang="ts">
import type { SelectMenuItem } from '@nuxt/ui'
/**
 * BỘ CHỌN VẬT PHẨM của trang Giftcode.
 *
 * Chỉ GHÉP CHUỖI (dmGhepChuoi — shared/utils/thuong.ts) rồi phát sự kiện `them` để
 * trang nối vào ô "Phần thưởng vật phẩm". Chuỗi đó vẫn đi qua kiemTraItem() phía máy
 * chủ y như khi gõ tay, nên bộ chọn không phải chốt an toàn và không mở ra đường dữ
 * liệu thứ hai vào cột giftcode.item.
 *
 * Trường ẩn (t.an): tham số server BẮT BUỘC có trong chuỗi nhưng KHÔNG đọc giá trị (vd
 * "_" của TK/TKMAX) — không hiện ô nhập, dmGhepChuoi tự gắn giá trị cố định.
 * minSo: mặc định sàn 1, nhưng trường "0 = vĩnh viễn" khai minSo='0' — ép min=1 cho mọi
 * ô số từng làm trình duyệt kẹp 0 thành 1, biến "vĩnh viễn" thành "1 ngày/phút" âm thầm.
 */
interface Muc { id: number, ten: string }
interface Nhom { ma: string, ten: string, loai: string, ghiChu: string, muc: Muc[] }

const props = defineProps<{ dmuc: Nhom[] }>()
const emit = defineEmits<{ them: [string] }>()

const nhomMa = ref<string>(props.dmuc[0]?.ma ?? '')
const nhom = computed(() => props.dmuc.find(n => n.ma === nhomMa.value) ?? null)

/** Ô "Nhóm": chia ba khúc cho dễ tìm, giữ đúng thứ tự của danh mục máy chủ. */
const nhomItems = computed(() => {
  const nguyenLieu: { label: string, value: string }[] = []
  const hayDung: { label: string, value: string }[] = []
  const dayDu: { label: string, value: string }[] = []
  for (const n of props.dmuc) {
    const it = { label: n.ten, value: n.ma }
    if (/^nl(sc|cc)\d$/.test(n.ma)) nguyenLieu.push(it)
    else if (n.ma === 'thuoc' || n.ma === 'vat_pham' || n.ma === 'ngoc') dayDu.push(it)
    else hayDung.push(it)
  }
  // Kiểu tường minh: dòng nhãn nhóm không có `value`, để TS khỏi suy ra value-key chỉ còn 'label'.
  const ra: SelectMenuItem[][] = []
  if (nguyenLieu.length) ra.push([{ type: 'label' as const, label: 'Ngọc nguyên liệu (NLSC / NLCC)' }, ...nguyenLieu])
  if (hayDung.length) ra.push([{ type: 'label' as const, label: 'Hay phát' }, ...hayDung])
  if (dayDu.length) ra.push([{ type: 'label' as const, label: 'Danh sách đầy đủ' }, ...dayDu])
  return ra
})

/** Ô "Vật phẩm": nhãn "id — tên" nên tìm được theo CẢ tên lẫn id (người quen id cũ vẫn gõ số). */
const mucItems = computed(() => (nhom.value?.muc ?? []).map(m => ({ value: String(m.id), label: `${m.id} — ${m.ten}` })))
const mucId = ref<string>('')

const truong = computed(() => (nhom.value ? dmTruongCuaLoai(nhom.value.loai) : []))
const giaTri = reactive<Record<string, string>>({})

function datMacDinh() {
  for (const k of Object.keys(giaTri)) delete giaTri[k]
  for (const t of truong.value) giaTri[t.ma] = t.macDinh
}

watch(nhomMa, () => {
  datMacDinh()
  mucId.value = mucItems.value[0]?.value ?? ''
}, { immediate: true })

watch(() => props.dmuc, () => {
  if (!props.dmuc.some(n => n.ma === nhomMa.value)) nhomMa.value = props.dmuc[0]?.ma ?? ''
})

/** Chuỗi sẽ được chèn, theo đúng thứ tự tham số của máy chủ. */
const chuoi = computed(() => (nhom.value ? dmGhepChuoi(nhom.value.loai, mucId.value, giaTri) : ''))
const loiChuoi = computed(() => (chuoi.value ? kiemTraItem(chuoi.value)[1] : []))

function them() {
  if (chuoi.value === '' || loiChuoi.value.length) return
  emit('them', chuoi.value)
}

function soCuaO(v: string | number | null | undefined, ma: string) {
  giaTri[ma] = v === null || v === undefined ? '' : String(v)
}
</script>

<template>
  <div class="rounded-xl border border-default bg-elevated/40 p-4">
    <div class="mb-3 flex flex-wrap items-baseline gap-x-3 gap-y-1">
      <strong class="flex items-center gap-2 text-highlighted">
        <UIcon name="i-lucide-wand-sparkles" class="size-4 text-primary" />
        Chọn vật phẩm
      </strong>
      <span class="text-xs text-muted">không cần nhớ id — chọn rồi bấm Thêm</span>
    </div>

    <div class="grid gap-3 lg:grid-cols-[minmax(0,18rem)_minmax(0,1fr)]">
      <UFormField label="Nhóm">
        <USelectMenu
          v-model="nhomMa"
          :items="nhomItems"
          value-key="value"
          :search-input="{ placeholder: 'Tìm nhóm…', icon: 'i-lucide-search' }"
          class="w-full"
        />
      </UFormField>

      <UFormField :label="`Vật phẩm (${mucItems.length})`" :hint="nhom ? `loại ${nhom.loai}` : ''">
        <USelectMenu
          v-model="mucId"
          :items="mucItems"
          value-key="value"
          :virtualize="mucItems.length > 60"
          :search-input="{ placeholder: 'ví dụ: trứng, cấp 6, 848', icon: 'i-lucide-search' }"
          :ui="{ content: 'min-w-[min(36rem,90vw)]' }"
          class="w-full"
        >
          <template #empty>
            không có món nào khớp
          </template>
        </USelectMenu>
      </UFormField>
    </div>

    <div v-if="truong.some(t => !t.an)" class="mt-3 grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
      <template v-for="t in truong" :key="t.ma">
        <UFormField v-if="!t.an" :label="t.nhan">
          <USelect
            v-if="t.chon"
            :model-value="giaTri[t.ma]"
            :items="t.chon"
            value-key="value"
            class="w-full"
            @update:model-value="(v: any) => soCuaO(v, t.ma)"
          />
          <UInput
            v-else
            type="number"
            :min="t.minSo ?? '1'"
            :model-value="giaTri[t.ma]"
            class="w-full"
            @update:model-value="(v: any) => soCuaO(v, t.ma)"
          />
        </UFormField>
      </template>
    </div>

    <div class="mt-4 flex flex-wrap items-center gap-3">
      <UButton
        icon="i-lucide-plus"
        label="Thêm vào phần thưởng"
        size="sm"
        :disabled="chuoi === '' || loiChuoi.length > 0"
        @click="them"
      />
      <code
        class="rounded-md border border-default bg-default px-2 py-1 text-xs"
        :class="chuoi === '' ? 'text-dimmed' : (loiChuoi.length ? 'text-error' : 'text-highlighted')"
      >{{ chuoi === '' ? '—' : chuoi }}</code>
    </div>
    <p v-if="loiChuoi.length" class="mt-2 text-xs text-error">
      {{ loiChuoi.join(' ') }}
    </p>
    <p v-if="nhom?.ghiChu" class="mt-3 flex gap-2 text-xs text-muted">
      <UIcon name="i-lucide-info" class="mt-0.5 size-3.5 shrink-0" />
      <span>{{ nhom.ghiChu }}</span>
    </p>
  </div>
</template>
