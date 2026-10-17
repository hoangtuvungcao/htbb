<script setup lang="ts">
/**
 * Trình soạn Markdown + xem trước trực tiếp (cùng bộ chuyển đổi an toàn với trang công khai:
 * shared/utils/markdown.ts). Tự lưu nháp vào localStorage theo `khoaNhap` (mỗi 2 giây khi gõ);
 * mở lại trang có nháp chưa lưu thì hỏi khôi phục.
 */
import AdminCmsChonAnh from './ChonAnh.vue'

const model = defineModel<string>({ default: '' })
const props = withDefaults(defineProps<{ khoaNhap?: string, cao?: string }>(), { khoaNhap: '', cao: '28rem' })
const che = ref<'viet' | 'xem' | 'doi'>('doi')
const ta = ref<HTMLTextAreaElement | null>(null)
const chonAnh = useOverlay().create(AdminCmsChonAnh)
const html = computed(() => markdownSangHtml(model.value))

function chen(truoc: string, sau = '', mau = '') {
  const el = ta.value
  if (!el) { model.value += truoc + mau + sau; return }
  const a = el.selectionStart
  const b = el.selectionEnd
  const chon = model.value.slice(a, b) || mau
  model.value = model.value.slice(0, a) + truoc + chon + sau + model.value.slice(b)
  nextTick(() => { el.focus(); el.setSelectionRange(a + truoc.length, a + truoc.length + chon.length) })
}
async function anh() {
  const url = await chonAnh.open().result
  if (url) chen('![', `](${url})`, 'mô tả ảnh')
}
const NUT = [
  { icon: 'i-lucide-heading-2', t: 'Tiêu đề mục', f: () => chen('\n## ', '', 'Tiêu đề') },
  { icon: 'i-lucide-bold', t: 'Đậm', f: () => chen('**', '**', 'chữ đậm') },
  { icon: 'i-lucide-italic', t: 'Nghiêng', f: () => chen('_', '_', 'chữ nghiêng') },
  { icon: 'i-lucide-list', t: 'Danh sách', f: () => chen('\n- ', '', 'mục') },
  { icon: 'i-lucide-list-ordered', t: 'Danh sách số', f: () => chen('\n1. ', '', 'bước') },
  { icon: 'i-lucide-quote', t: 'Trích dẫn', f: () => chen('\n> ', '', 'ghi chú') },
  { icon: 'i-lucide-link', t: 'Liên kết', f: () => chen('[', '](/duong-dan)', 'chữ hiện') },
  { icon: 'i-lucide-table', t: 'Bảng', f: () => chen('\n| Cột 1 | Cột 2 |\n|---|---|\n| a | b |\n') },
]

// ---- tự lưu nháp
const nhapCu = ref<string | null>(null)
let hen: ReturnType<typeof setTimeout> | undefined
const k = computed(() => (props.khoaNhap ? `htbb-nhap:${props.khoaNhap}` : ''))
onMounted(() => {
  if (!k.value) return
  try {
    const v = localStorage.getItem(k.value)
    if (v !== null && v !== model.value) nhapCu.value = v
  }
  catch { /* bỏ qua */ }
})
watch(model, (v) => {
  if (!k.value) return
  clearTimeout(hen)
  hen = setTimeout(() => { try { localStorage.setItem(k.value, v) } catch { /* đầy bộ nhớ */ } }, 2000)
})
function khoiPhuc() { if (nhapCu.value !== null) model.value = nhapCu.value; nhapCu.value = null }
/** Gọi sau khi lưu thành công lên server. */
function xoaNhap() { clearTimeout(hen); try { if (k.value) localStorage.removeItem(k.value) } catch { /* bỏ qua */ } }
defineExpose({ xoaNhap })
</script>

<template>
  <div class="rounded-xl border border-default">
    <UAlert v-if="nhapCu !== null" color="info" variant="subtle" icon="i-lucide-save" title="Có bản nháp tự lưu chưa gửi lên" class="rounded-b-none" :actions="[{ label: 'Khôi phục', onClick: khoiPhuc }, { label: 'Bỏ', color: 'neutral', variant: 'ghost', onClick: () => { nhapCu = null; xoaNhap() } }]" />
    <div class="flex flex-wrap items-center gap-1 border-b border-default p-1.5">
      <UButton v-for="n in NUT" :key="n.t" :icon="n.icon" size="xs" color="neutral" variant="ghost" :aria-label="n.t" :title="n.t" @click="n.f" />
      <UButton icon="i-lucide-image-plus" size="xs" color="neutral" variant="ghost" aria-label="Chèn ảnh" title="Chèn ảnh từ thư viện" @click="anh" />
      <span class="ml-auto flex gap-0.5">
        <UButton v-for="c in ([['viet', 'Viết'], ['doi', 'Song song'], ['xem', 'Xem trước']] as const)" :key="c[0]" size="xs" :color="che === c[0] ? 'primary' : 'neutral'" :variant="che === c[0] ? 'soft' : 'ghost'" :label="c[1]" :class="c[0] === 'doi' ? 'hidden lg:inline-flex' : ''" @click="che = c[0]" />
      </span>
    </div>
    <div class="grid" :class="che === 'doi' ? 'lg:grid-cols-2' : ''">
      <textarea
        v-show="che !== 'xem'"
        ref="ta"
        v-model="model"
        class="w-full resize-y bg-transparent p-3 font-mono text-sm outline-none"
        :style="{ minHeight: cao }"
        spellcheck="false"
        placeholder="Viết bằng Markdown: ## tiêu đề mục, **đậm**, - danh sách, [liên kết](/duong-dan), ![ảnh](/uploads/...)"
      />
      <div v-show="che !== 'viet'" class="overflow-y-auto border-default p-4" :class="che === 'doi' ? 'lg:border-l' : ''" :style="{ maxHeight: che === 'doi' ? cao : undefined, minHeight: cao }">
        <div class="noi-dung-md" v-html="html" />
      </div>
    </div>
    <p class="border-t border-default px-3 py-1.5 text-xs text-dimmed">
      HTML thô bị vô hiệu (an toàn). Ảnh/liên kết chỉ nhận https://, /đường-dẫn hoặc #mục. {{ model.length.toLocaleString('vi-VN') }} ký tự.
    </p>
  </div>
</template>
