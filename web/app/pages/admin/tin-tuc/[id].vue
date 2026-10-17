<script setup lang="ts">
/** Soạn / sửa một bài tin tức: Markdown + xem trước, ảnh bìa, nháp / đăng / hẹn giờ, ghim trang chủ. */
import AdminCmsChonAnh from '~/components/admin/cms/ChonAnh.vue'

definePageMeta({ layout: 'admin', middleware: 'admin' })
const route = useRoute()
const router = useRouter()
const api = useAdminApi()
const toast = useToast()
const me = useAdminMe()
const chonAnh = useOverlay().create(AdminCmsChonAnh)
const id = computed(() => (route.params.id === 'moi' ? 0 : Number(route.params.id)))

const f = reactive({ slug: '', tieuDe: '', tomTat: '', anhBia: '', loai: 'Thông báo', the: '', noiDung: '', trangThai: 0, dangLuc: '', ghim: false })
const daSuaSlug = ref(false)
const md = ref<{ xoaNhap: () => void } | null>(null)
if (id.value > 0) {
  const r = await api<Record<string, any>>(`/api/admin/cms/tin-tuc/${id.value}`).catch((e) => { toast.add({ title: loiApi(e), color: 'error' }); return null })
  if (r) {
    Object.assign(f, { slug: r.slug, tieuDe: r.tieu_de, tomTat: r.tom_tat, anhBia: r.anh_bia, loai: r.loai, the: r.the, noiDung: r.noi_dung, trangThai: Number(r.trang_thai), dangLuc: (r.dang_luc ?? '').slice(0, 16).replace(' ', 'T'), ghim: Number(r.ghim) === 1 })
    daSuaSlug.value = true
  }
}
watch(() => f.tieuDe, (v) => { if (!daSuaSlug.value) f.slug = slugTieuDe(v) })
const LOAI = ['Thông báo', 'Sự kiện', 'Cập nhật', 'Hướng dẫn', 'Cộng đồng', 'Bảo trì']
const cheDo = computed({
  get: () => (f.trangThai !== 1 ? 'nhap' : f.dangLuc && new Date(f.dangLuc) > new Date() ? 'hen' : 'dang'),
  set: (v: string) => {
    if (v === 'nhap') f.trangThai = 0
    else { f.trangThai = 1; if (v === 'dang') f.dangLuc = '' }
  },
})
const dangLuu = ref(false)
async function luu() {
  dangLuu.value = true
  try {
    const r = await api<{ id: number }>('/api/admin/cms/tin-tuc/luu', { method: 'POST', body: { id: id.value, ...f, dangLuc: f.dangLuc ? f.dangLuc.replace('T', ' ') : '' } })
    md.value?.xoaNhap()
    toast.add({ title: f.trangThai === 1 ? 'Đã lưu & đăng' : 'Đã lưu nháp', color: 'success' })
    if (id.value === 0) router.replace(`/admin/tin-tuc/${r.id}`)
  }
  catch (e) { toast.add({ title: loiApi(e), color: 'error' }) }
  finally { dangLuu.value = false }
}
async function chonBia() {
  const u = await chonAnh.open().result
  if (u) f.anhBia = u
}
defineShortcuts({ meta_s: { usingInput: true, handler: () => { if (!dangLuu.value) luu() } } })
</script>

<template>
  <AdminPage :title="id ? 'Sửa bài viết' : 'Viết bài mới'">
    <template #right>
      <UButton to="/admin/tin-tuc" color="neutral" variant="ghost" icon="i-lucide-arrow-left" label="Danh sách" />
      <UButton v-if="id && f.trangThai === 1" :to="`/news/${f.slug}`" target="_blank" color="neutral" variant="outline" icon="i-lucide-external-link" label="Xem trên web" />
      <UButton :loading="dangLuu" :disabled="!me.duocGhiNoiDung.value" icon="i-lucide-save" :label="f.trangThai === 1 ? 'Lưu & đăng' : 'Lưu nháp'" @click="luu" />
    </template>
    <div class="grid gap-6 xl:grid-cols-[1fr_20rem]">
      <div class="space-y-4">
        <UFormField label="Tiêu đề" required>
          <UInput v-model="f.tieuDe" size="xl" class="w-full" placeholder="Ví dụ: Sự kiện Tuần Shiny bắt đầu!" />
        </UFormField>
        <UFormField label="Đường dẫn" :help="`/news/${f.slug || '...'}`">
          <UInput v-model="f.slug" class="w-full" @input="daSuaSlug = true" />
        </UFormField>
        <UFormField label="Tóm tắt (hiện ở danh sách & chia sẻ)">
          <UTextarea v-model="f.tomTat" :rows="2" :maxlength="500" class="w-full" />
        </UFormField>
        <UFormField label="Nội dung">
          <AdminCmsMarkdown ref="md" v-model="f.noiDung" :khoa-nhap="`tin-${id || 'moi'}`" />
        </UFormField>
      </div>
      <div class="space-y-4">
        <UCard>
          <UFormField label="Trạng thái">
            <URadioGroup v-model="cheDo" :items="[{ label: 'Nháp (chưa hiện)', value: 'nhap' }, { label: 'Đăng ngay', value: 'dang' }, { label: 'Hẹn giờ đăng', value: 'hen' }]" />
          </UFormField>
          <UFormField v-if="cheDo !== 'nhap'" label="Thời điểm đăng" class="mt-3" help="Để trống = ngay bây giờ. Đặt tương lai = hẹn giờ.">
            <UInput v-model="f.dangLuc" type="datetime-local" class="w-full" />
          </UFormField>
          <UCheckbox v-model="f.ghim" label="Ghim lên trang chủ" class="mt-4" />
        </UCard>
        <UCard>
          <UFormField label="Chuyên mục">
            <USelect v-model="f.loai" :items="LOAI" class="w-full" />
          </UFormField>
          <UFormField label="Thẻ (cách nhau dấu phẩy)" class="mt-3">
            <UInput v-model="f.the" placeholder="shiny, sự kiện" class="w-full" />
          </UFormField>
        </UCard>
        <UCard>
          <p class="mb-2 text-sm font-medium">Ảnh bìa</p>
          <img v-if="f.anhBia" :src="f.anhBia" alt="" class="mb-2 aspect-video w-full rounded-lg object-cover">
          <div class="flex gap-2">
            <UButton icon="i-lucide-image" size="sm" color="neutral" variant="outline" label="Chọn ảnh" @click="chonBia" />
            <UButton v-if="f.anhBia" icon="i-lucide-x" size="sm" color="neutral" variant="ghost" label="Bỏ" @click="f.anhBia = ''" />
          </div>
        </UCard>
        <p class="text-xs text-dimmed">Ctrl/⌘ + S để lưu nhanh. Nháp tự lưu trong trình duyệt khi gõ.</p>
      </div>
    </div>
  </AdminPage>
</template>
