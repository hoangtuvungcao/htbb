<script setup lang="ts">
/** Danh sách tin tức & sự kiện (CSDL) + bài tĩnh cũ. */
import AdminTaikhoanXacNhan from '~/components/admin/taikhoan/XacNhan.vue'

definePageMeta({ layout: 'admin', middleware: 'admin' })
interface Dong { id: number, slug: string, tieu_de: string, loai: string, trang_thai: number, dang_luc: string | null, ghim: number, tac_gia: string, sua_luc: string }
const api = useAdminApi()
const toast = useToast()
const me = useAdminMe()
const xacNhan = useOverlay().create(AdminTaikhoanXacNhan)
const tim = ref('')
const { data, refresh, status } = await useAsyncData('admin-tin', () => api<{ coBang: boolean, ds: Dong[], tinh: { slug: string, tieuDe: string, loai: string, ngay: string }[] }>('/api/admin/cms/tin-tuc', { query: { q: tim.value || undefined } }), { server: false, watch: [tim] })

function trangThai(r: Dong) {
  if (Number(r.trang_thai) !== 1) return { t: 'Nháp', mau: 'neutral' as const }
  if (r.dang_luc && new Date(r.dang_luc.replace(' ', 'T')) > new Date()) return { t: `Hẹn ${r.dang_luc.slice(0, 16)}`, mau: 'info' as const }
  return { t: 'Đã đăng', mau: 'success' as const }
}
async function xoa(r: Dong) {
  if (!(await xacNhan.open({ title: `Xoá bài "${r.tieu_de}"?`, description: 'Không khôi phục được (nội dung cũ vẫn còn trong Lịch sử thay đổi).', nut: 'Xoá', mau: 'error' }).result)) return
  try { await api('/api/admin/cms/tin-tuc/xoa', { method: 'POST', body: { id: r.id } }); toast.add({ title: 'Đã xoá', color: 'success' }); refresh() }
  catch (e) { toast.add({ title: loiApi(e), color: 'error' }) }
}
</script>

<template>
  <AdminPage title="Tin tức & sự kiện">
    <template #right>
      <UButton v-if="me.duocGhiNoiDung.value" to="/admin/tin-tuc/moi" icon="i-lucide-plus" label="Viết bài mới" />
    </template>
    <AdminCmsChuaCoBang v-if="data && !data.coBang" bang="web_tin_tuc" />
    <UInput v-model="tim" icon="i-lucide-search" placeholder="Tìm theo tiêu đề / đường dẫn..." class="w-full max-w-md" />
    <UCard :ui="{ body: 'p-0 sm:p-0' }">
      <div class="overflow-x-auto">
        <table class="bang-wiki">
          <thead>
            <tr><th>Tiêu đề</th><th>Chuyên mục</th><th>Trạng thái</th><th>Sửa lúc</th><th /></tr>
          </thead>
          <tbody>
            <tr v-for="r in data?.ds ?? []" :key="r.id">
              <td>
                <NuxtLink :to="`/admin/tin-tuc/${r.id}`" class="font-semibold text-highlighted hover:text-primary">{{ r.tieu_de }}</NuxtLink>
                <UIcon v-if="Number(r.ghim) === 1" name="i-lucide-pin" class="ml-1 inline size-3.5 text-warning" />
                <span class="block text-xs text-dimmed">/news/{{ r.slug }} · {{ r.tac_gia }}</span>
              </td>
              <td>{{ r.loai }}</td>
              <td><UBadge :color="trangThai(r).mau" variant="subtle">{{ trangThai(r).t }}</UBadge></td>
              <td class="whitespace-nowrap text-xs">{{ r.sua_luc }}</td>
              <td class="whitespace-nowrap text-right">
                <UButton :to="`/news/${r.slug}`" target="_blank" size="xs" color="neutral" variant="ghost" icon="i-lucide-external-link" aria-label="Xem" />
                <UButton v-if="me.duocGhiNoiDung.value" size="xs" color="error" variant="ghost" icon="i-lucide-trash-2" aria-label="Xoá" @click="xoa(r)" />
              </td>
            </tr>
            <tr v-if="status !== 'pending' && !(data?.ds?.length)">
              <td colspan="5" class="text-center text-muted">Chưa có bài nào trong CSDL.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </UCard>
    <UCard v-if="data?.tinh?.length">
      <template #header>
        <p class="font-semibold">Bài tĩnh có sẵn (shared/utils/news.ts)</p>
        <p class="text-sm text-muted">Vẫn hiện trên trang. Tạo bài CSDL cùng đường dẫn để thay thế bài tĩnh.</p>
      </template>
      <ul class="space-y-1 text-sm">
        <li v-for="t in data.tinh" :key="t.slug" class="flex justify-between gap-3">
          <span><b>{{ t.tieuDe }}</b> <span class="text-dimmed">/news/{{ t.slug }}</span></span>
          <span class="text-muted">{{ t.loai }} · {{ t.ngay }}</span>
        </li>
      </ul>
    </UCard>
  </AdminPage>
</template>
