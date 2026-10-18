<script setup lang="ts">
/** Câu hỏi thường gặp: thêm/sửa/xoá, nhóm, thứ tự, ẩn/hiện, Markdown. */
import AdminTaikhoanXacNhan from '~/components/admin/taikhoan/XacNhan.vue'

definePageMeta({ layout: 'admin', middleware: 'admin' })
interface Dong { id: number, nhom: string, cau_hoi: string, tra_loi: string, thu_tu: number, hien: number }
const api = useAdminApi()
const toast = useToast()
const me = useAdminMe()
const xacNhan = useOverlay().create(AdminTaikhoanXacNhan)
const { data, refresh } = await useAsyncData('admin-faq', () => api<{ coBang: boolean, ds: Dong[] }>('/api/admin/cms/faq'), { server: false })
const dangSua = ref<{ id: number, nhom: string, cauHoi: string, traLoi: string, thuTu: number, hien: boolean } | null>(null)
function sua(r?: Dong) {
  dangSua.value = r
    ? { id: r.id, nhom: r.nhom, cauHoi: r.cau_hoi, traLoi: r.tra_loi, thuTu: Number(r.thu_tu), hien: Number(r.hien) === 1 }
    : { id: 0, nhom: 'Chung', cauHoi: '', traLoi: '', thuTu: ((data.value?.ds.at(-1)?.thu_tu ?? 0) as number) + 10, hien: true }
}
async function luu() {
  try { await api('/api/admin/cms/faq/luu', { method: 'POST', body: dangSua.value }); toast.add({ title: 'Đã lưu', color: 'success' }); dangSua.value = null; refresh() }
  catch (e) { toast.add({ title: loiApi(e), color: 'error' }) }
}
async function xoa(r: Dong) {
  if (!(await xacNhan.open({ title: `Xoá câu "${r.cau_hoi}"?`, nut: 'Xoá', mau: 'error' }).result)) return
  try { await api('/api/admin/cms/faq/xoa', { method: 'POST', body: { id: r.id } }); refresh() }
  catch (e) { toast.add({ title: loiApi(e), color: 'error' }) }
}
async function chepMacDinh() {
  try { const r = await api<{ so: number }>('/api/admin/cms/faq/nhap-mac-dinh', { method: 'POST', body: {} }); toast.add({ title: `Đã chép ${r.so} câu`, color: 'success' }); refresh() }
  catch (e) { toast.add({ title: loiApi(e), color: 'error' }) }
}
const nhom = computed(() => [...new Set((data.value?.ds ?? []).map(r => r.nhom))])
</script>

<template>
  <AdminPage title="Câu hỏi thường gặp">
    <template #right>
      <UButton to="/cam-nang/faq" target="_blank" color="neutral" variant="ghost" icon="i-lucide-external-link" label="Xem" />
      <UButton v-if="me.duocGhiNoiDung.value" icon="i-lucide-plus" label="Thêm câu hỏi" @click="sua()" />
    </template>
    <AdminCmsChuaCoBang v-if="data && !data.coBang" bang="web_faq" />
    <UAlert v-else-if="data && !data.ds.length" color="info" variant="subtle" icon="i-lucide-info" title="Đang dùng bộ FAQ mặc định" description="Trang công khai đang hiện FAQ có sẵn. Chép vào CSDL để sửa từng câu." :actions="me.duocGhiNoiDung.value ? [{ label: 'Chép FAQ mặc định', onClick: chepMacDinh }] : []" />
    <UCard v-for="n in nhom" :key="n">
      <template #header><b>{{ n }}</b></template>
      <ul class="divide-y divide-default">
        <li v-for="r in data!.ds.filter(x => x.nhom === n)" :key="r.id" class="flex items-start gap-3 py-2">
          <span class="w-10 text-xs text-dimmed">#{{ r.thu_tu }}</span>
          <div class="min-w-0 flex-1">
            <p class="font-semibold" :class="Number(r.hien) ? '' : 'text-dimmed line-through'">{{ r.cau_hoi }}</p>
            <p class="line-clamp-2 text-sm text-muted">{{ markdownSangChu(r.tra_loi, 220) }}</p>
          </div>
          <UButton v-if="me.duocGhiNoiDung.value" size="xs" color="neutral" variant="ghost" icon="i-lucide-pencil" aria-label="Sửa" @click="sua(r)" />
          <UButton v-if="me.duocGhiNoiDung.value" size="xs" color="error" variant="ghost" icon="i-lucide-trash-2" aria-label="Xoá" @click="xoa(r)" />
        </li>
      </ul>
    </UCard>
    <USlideover :open="!!dangSua" :title="dangSua?.id ? 'Sửa câu hỏi' : 'Câu hỏi mới'" :ui="{ content: 'sm:max-w-2xl' }" @update:open="(v: boolean) => { if (!v) dangSua = null }">
      <template #body>
        <div v-if="dangSua" class="space-y-3">
          <div class="grid grid-cols-[1fr_8rem] gap-3">
            <UFormField label="Nhóm"><UInput v-model="dangSua.nhom" class="w-full" /></UFormField>
            <UFormField label="Thứ tự"><UInputNumber v-model="dangSua.thuTu" class="w-full" /></UFormField>
          </div>
          <UFormField label="Câu hỏi"><UInput v-model="dangSua.cauHoi" class="w-full" /></UFormField>
          <UFormField label="Trả lời (Markdown)"><AdminCmsMarkdown v-model="dangSua.traLoi" cao="14rem" /></UFormField>
          <USwitch v-model="dangSua.hien" label="Hiện trên website" />
        </div>
      </template>
      <template #footer>
        <UButton icon="i-lucide-save" label="Lưu" @click="luu" />
      </template>
    </USlideover>
  </AdminPage>
</template>
