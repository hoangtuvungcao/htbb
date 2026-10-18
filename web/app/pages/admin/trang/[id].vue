<script setup lang="ts">
/** Soạn trang cẩm nang (mới hoặc tuỳ chỉnh trang có sẵn). ## tiêu đề -> mục lục + chỉ mục tìm kiếm. */
definePageMeta({ layout: 'admin', middleware: 'admin' })
const route = useRoute()
const router = useRouter()
const api = useAdminApi()
const toast = useToast()
const me = useAdminMe()
const id = computed(() => (route.params.id === 'moi' ? 0 : Number(route.params.id)))
const coSan = computed(() => trangCamNang(f.slug))
const f = reactive({ slug: String(route.query.slug ?? ''), tieuDe: '', nhom: 'Thế giới', icon: 'i-lucide-file-text', moTa: '', tuKhoa: '', thuTu: 100, noiDung: '', trangThai: 1 })
const md = ref<{ xoaNhap: () => void } | null>(null)
if (id.value > 0) {
  const r = await api<Record<string, any>>(`/api/admin/cms/trang/${id.value}`).catch((e) => { toast.add({ title: loiApi(e), color: 'error' }); return null })
  if (r) Object.assign(f, { slug: r.slug, tieuDe: r.tieu_de, nhom: r.nhom, icon: r.icon, moTa: r.mo_ta, tuKhoa: r.tu_khoa, thuTu: Number(r.thu_tu), noiDung: r.noi_dung, trangThai: Number(r.trang_thai) })
}
else if (coSan.value) {
  const c = coSan.value
  Object.assign(f, { tieuDe: c.ten, nhom: c.nhom, icon: c.icon, moTa: c.moTa, thuTu: DS_CAM_NANG.findIndex(p => p.slug === c.slug) * 10 })
}
const ICON = ['i-lucide-file-text', 'i-lucide-book-open', 'i-lucide-flame', 'i-lucide-swords', 'i-lucide-shield', 'i-lucide-sparkles', 'i-lucide-map', 'i-lucide-users', 'i-lucide-trophy', 'i-lucide-skull', 'i-lucide-gift', 'i-lucide-calendar-days', 'i-lucide-scroll-text', 'i-lucide-coins', 'i-lucide-circle-help', 'i-lucide-lightbulb']
const dangLuu = ref(false)
async function luu() {
  dangLuu.value = true
  try {
    const r = await api<{ id: number }>('/api/admin/cms/trang/luu', { method: 'POST', body: { id: id.value, ...f } })
    md.value?.xoaNhap()
    toast.add({ title: 'Đã lưu trang', color: 'success' })
    if (id.value === 0) router.replace(`/admin/trang/${r.id}`)
  }
  catch (e) { toast.add({ title: loiApi(e), color: 'error' }) }
  finally { dangLuu.value = false }
}
defineShortcuts({ meta_s: { usingInput: true, handler: () => { if (!dangLuu.value) luu() } } })
</script>

<template>
  <AdminPage :title="id ? 'Sửa trang' : 'Trang mới'">
    <template #right>
      <UButton to="/admin/trang" color="neutral" variant="ghost" icon="i-lucide-arrow-left" label="Danh sách" />
      <UButton v-if="f.slug" :to="`/cam-nang/${f.slug}`" target="_blank" color="neutral" variant="outline" icon="i-lucide-external-link" label="Xem" />
      <UButton :loading="dangLuu" :disabled="!me.duocGhiNoiDung.value" icon="i-lucide-save" label="Lưu" @click="luu" />
    </template>
    <UAlert v-if="coSan" color="info" variant="subtle" icon="i-lucide-info" :title="`Đang tuỳ chỉnh trang có sẵn: ${coSan.ten}`" description="Phần số liệu tự động của trang giữ nguyên. Tên, mô tả, nhóm, thứ tự, ẩn/hiện lấy từ đây; nội dung Markdown bên dưới hiện THÊM ở cuối trang." />
    <div class="grid gap-6 xl:grid-cols-[1fr_20rem]">
      <div class="space-y-4">
        <UFormField label="Tiêu đề" required>
          <UInput v-model="f.tieuDe" size="xl" class="w-full" />
        </UFormField>
        <UFormField label="Mô tả ngắn">
          <UTextarea v-model="f.moTa" :rows="2" class="w-full" />
        </UFormField>
        <UFormField :label="coSan ? 'Nội dung thêm (cuối trang)' : 'Nội dung'" help="Dùng ## Tiêu đề cho từng mục: tự thành mục lục 'Trên trang này' và vào chỉ mục tìm kiếm.">
          <AdminCmsMarkdown ref="md" v-model="f.noiDung" :khoa-nhap="`trang-${id || f.slug || 'moi'}`" />
        </UFormField>
      </div>
      <UCard class="self-start">
        <div class="space-y-3">
          <UFormField label="Đường dẫn" :help="`/cam-nang/${f.slug}`">
            <UInput v-model="f.slug" :disabled="!!coSan && id === 0" class="w-full" />
          </UFormField>
          <UFormField label="Nhóm">
            <USelect v-model="f.nhom" :items="[...NHOM_CAM_NANG] as string[]" class="w-full" />
          </UFormField>
          <UFormField label="Biểu tượng">
            <USelect v-model="f.icon" :items="ICON.map(i => ({ label: i.replace('i-lucide-', ''), value: i, icon: i }))" class="w-full" />
          </UFormField>
          <UFormField label="Thứ tự trong thanh bên" help="Trang có sẵn đánh số 0, 10, 20…">
            <UInputNumber v-model="f.thuTu" class="w-full" />
          </UFormField>
          <UFormField label="Từ khoá tìm kiếm thêm">
            <UInput v-model="f.tuKhoa" class="w-full" />
          </UFormField>
          <USwitch :model-value="f.trangThai === 1" label="Hiện trên website" @update:model-value="(v: boolean) => f.trangThai = v ? 1 : 0" />
        </div>
      </UCard>
    </div>
  </AdminPage>
</template>
