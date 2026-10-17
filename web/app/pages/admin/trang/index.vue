<script setup lang="ts">
/** Trang cẩm nang: trang mới + ghi đè trang có sẵn (tên, mô tả, thứ tự, ẩn, nội dung thêm). */
import AdminTaikhoanXacNhan from '~/components/admin/taikhoan/XacNhan.vue'

definePageMeta({ layout: 'admin', middleware: 'admin' })
interface Dong { id: number, slug: string, tieu_de: string, nhom: string, icon: string, thu_tu: number, trang_thai: number, tac_gia: string, sua_luc: string }
const api = useAdminApi()
const toast = useToast()
const me = useAdminMe()
const xacNhan = useOverlay().create(AdminTaikhoanXacNhan)
const { data, refresh } = await useAsyncData('admin-trang', () => api<{ coBang: boolean, ds: Dong[], coSan: { slug: string, ten: string, nhom: string, icon: string, moTa: string }[] }>('/api/admin/cms/trang'), { server: false })
const daGhiDe = computed(() => new Map((data.value?.ds ?? []).map(r => [r.slug, r])))
const trangMoi = computed(() => (data.value?.ds ?? []).filter(r => !data.value?.coSan.some(c => c.slug === r.slug)))
async function xoa(r: Dong) {
  if (!(await xacNhan.open({ title: `Xoá "${r.tieu_de}"?`, description: 'Trang có sẵn sẽ quay về như cũ; trang mới sẽ biến mất.', nut: 'Xoá', mau: 'error' }).result)) return
  try { await api('/api/admin/cms/trang/xoa', { method: 'POST', body: { id: r.id } }); toast.add({ title: 'Đã xoá', color: 'success' }); refresh() }
  catch (e) { toast.add({ title: loiApi(e), color: 'error' }) }
}
</script>

<template>
  <AdminPage title="Trang cẩm nang">
    <template #right>
      <UButton v-if="me.duocGhiNoiDung.value" to="/admin/trang/moi" icon="i-lucide-plus" label="Tạo trang mới" />
    </template>
    <AdminCmsChuaCoBang v-if="data && !data.coBang" bang="web_trang" />
    <UCard>
      <template #header>
        <p class="font-semibold">Trang mới tạo</p>
        <p class="text-sm text-muted">Hiện ở /cam-nang/&lt;đường dẫn&gt;, trong thanh bên theo nhóm và thứ tự.</p>
      </template>
      <ul class="divide-y divide-default">
        <li v-for="r in trangMoi" :key="r.id" class="flex items-center gap-3 py-2">
          <UIcon :name="r.icon" class="size-5 text-primary" />
          <NuxtLink :to="`/admin/trang/${r.id}`" class="font-semibold hover:text-primary">{{ r.tieu_de }}</NuxtLink>
          <span class="text-xs text-dimmed">/cam-nang/{{ r.slug }} · {{ r.nhom }} · thứ tự {{ r.thu_tu }}</span>
          <UBadge v-if="Number(r.trang_thai) !== 1" color="neutral" variant="subtle" size="sm">Ẩn</UBadge>
          <span class="ml-auto">
            <UButton v-if="me.duocGhiNoiDung.value" size="xs" color="error" variant="ghost" icon="i-lucide-trash-2" aria-label="Xoá" @click="xoa(r)" />
          </span>
        </li>
        <li v-if="!trangMoi.length" class="py-2 text-sm text-muted">Chưa có.</li>
      </ul>
    </UCard>
    <UCard>
      <template #header>
        <p class="font-semibold">Trang có sẵn ({{ data?.coSan.length ?? 0 }})</p>
        <p class="text-sm text-muted">Số liệu sinh tự động từ dữ liệu game. Bấm "Tuỳ chỉnh" để đổi tên/mô tả/thứ tự, ẩn trang, hoặc viết thêm nội dung hiện ở cuối trang.</p>
      </template>
      <ul class="grid gap-2 md:grid-cols-2">
        <li v-for="c in data?.coSan ?? []" :key="c.slug" class="flex items-center gap-3 rounded-lg border border-default p-2">
          <UIcon :name="c.icon" class="size-5 text-primary" />
          <span class="min-w-0">
            <b class="block truncate">{{ daGhiDe.get(c.slug)?.tieu_de || c.ten }}</b>
            <span class="text-xs text-dimmed">{{ c.nhom }}</span>
          </span>
          <UBadge v-if="daGhiDe.get(c.slug)" color="primary" variant="subtle" size="sm">Đã tuỳ chỉnh</UBadge>
          <UButton
            class="ml-auto"
            size="xs"
            color="neutral"
            variant="outline"
            :to="daGhiDe.get(c.slug) ? `/admin/trang/${daGhiDe.get(c.slug)!.id}` : `/admin/trang/moi?slug=${c.slug}`"
            label="Tuỳ chỉnh"
          />
          <UButton :to="`/cam-nang/${c.slug}`" target="_blank" size="xs" color="neutral" variant="ghost" icon="i-lucide-external-link" aria-label="Xem" />
        </li>
      </ul>
    </UCard>
  </AdminPage>
</template>
