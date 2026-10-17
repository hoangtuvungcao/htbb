<script setup lang="ts">
/** Thư viện ảnh: tải lên (kéo thả), xem, sao chép URL / Markdown, xoá. */
import AdminTaikhoanXacNhan from '~/components/admin/taikhoan/XacNhan.vue'

definePageMeta({ layout: 'admin', middleware: 'admin' })
interface Anh { id: number, url: string, ten_goc: string, mime: string, kich_thuoc: number, rong: number, cao: number, tai_boi: string, tai_luc: string }
const api = useAdminApi()
const toast = useToast()
const me = useAdminMe()
const xacNhan = useOverlay().create(AdminTaikhoanXacNhan)
const { data, refresh } = await useAsyncData('admin-media', () => api<{ coBang: boolean, ds: Anh[], toiDa: number }>('/api/admin/cms/media'), { server: false })
const dangTai = ref(false)
const keo = ref(false)
async function guiFile(files: FileList | File[]) {
  const ds = [...files].filter(f => /^image\/(png|jpeg|gif|webp)$/.test(f.type))
  if (!ds.length) { toast.add({ title: 'Chỉ nhận ảnh PNG, JPEG, GIF, WebP', color: 'warning' }); return }
  const fd = new FormData()
  for (const f of ds) fd.append('file', f)
  dangTai.value = true
  try { const r = await api<{ ds: unknown[] }>('/api/admin/cms/media/tai-len', { method: 'POST', body: fd }); toast.add({ title: `Đã tải ${r.ds.length} ảnh`, color: 'success' }); refresh() }
  catch (e) { toast.add({ title: loiApi(e), color: 'error' }) }
  finally { dangTai.value = false }
}
function tha(e: DragEvent) { keo.value = false; if (e.dataTransfer?.files) guiFile(e.dataTransfer.files) }
async function chep(s: string) {
  try { await navigator.clipboard.writeText(s); toast.add({ title: 'Đã sao chép', color: 'success' }) }
  catch { toast.add({ title: s, color: 'neutral' }) }
}
async function xoa(a: Anh) {
  if (!(await xacNhan.open({ title: 'Xoá ảnh này?', description: 'Bài viết đang dùng ảnh sẽ bị mất ảnh.', nut: 'Xoá', mau: 'error' }).result)) return
  try { await api('/api/admin/cms/media/xoa', { method: 'POST', body: { id: a.id } }); refresh() }
  catch (e) { toast.add({ title: loiApi(e), color: 'error' }) }
}
const kb = (n: number) => n > 1048576 ? `${(n / 1048576).toFixed(1)} MB` : `${Math.ceil(n / 1024)} KB`
</script>

<template>
  <AdminPage title="Thư viện ảnh">
    <AdminCmsChuaCoBang v-if="data && !data.coBang" bang="web_media" />
    <label
      v-else-if="me.duocGhiNoiDung.value"
      class="flex cursor-pointer flex-col items-center justify-center gap-2 rounded-2xl border-2 border-dashed p-8 text-center transition"
      :class="keo ? 'border-primary bg-primary/5' : 'border-default hover:border-primary'"
      @dragover.prevent="keo = true"
      @dragleave="keo = false"
      @drop.prevent="tha"
    >
      <UIcon :name="dangTai ? 'i-lucide-loader-circle' : 'i-lucide-image-up'" class="size-8 text-primary" :class="dangTai ? 'animate-spin' : ''" />
      <b>Kéo thả ảnh vào đây hoặc bấm để chọn</b>
      <span class="text-sm text-muted">PNG, JPEG, GIF, WebP · tối đa 5 MB mỗi ảnh · kiểm tra nội dung file (không nhận SVG)</span>
      <input type="file" accept="image/png,image/jpeg,image/gif,image/webp" multiple class="sr-only" @change="(e) => guiFile((e.target as HTMLInputElement).files ?? [])">
    </label>
    <ul class="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-5">
      <li v-for="a in data?.ds ?? []" :key="a.id" class="overflow-hidden rounded-xl border border-default bg-elevated/40">
        <a :href="a.url" target="_blank"><img :src="a.url" :alt="a.ten_goc" loading="lazy" class="aspect-video w-full object-cover"></a>
        <div class="p-2 text-xs">
          <p class="truncate font-semibold" :title="a.ten_goc">{{ a.ten_goc || a.url }}</p>
          <p class="text-dimmed">{{ a.rong }}×{{ a.cao }} · {{ kb(a.kich_thuoc) }} · {{ a.tai_boi }}</p>
          <div class="mt-1 flex gap-1">
            <UButton size="xs" color="neutral" variant="soft" icon="i-lucide-link" label="URL" @click="chep(a.url)" />
            <UButton size="xs" color="neutral" variant="soft" icon="i-lucide-code" label="MD" @click="chep(`![${a.ten_goc}](${a.url})`)" />
            <UButton v-if="me.duocGhiNoiDung.value" size="xs" color="error" variant="ghost" icon="i-lucide-trash-2" aria-label="Xoá" class="ml-auto" @click="xoa(a)" />
          </div>
        </div>
      </li>
    </ul>
  </AdminPage>
</template>
