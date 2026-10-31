<script setup lang="ts">
/** Lịch sử thay đổi: ai sửa gì, lúc nào, trước/sau. */
definePageMeta({ layout: 'admin', middleware: 'admin' })
const api = useAdminApi()
const loc = ref('')
const { data } = await useAsyncData('admin-thay-doi', () => api<{ coBang: boolean, ds: { id: number, admin_user: string, hanh_dong: string, doi_tuong: string, ip: string, thoi_gian: string }[] }>('/api/admin/cms/thay-doi', { query: { doiTuong: loc.value || undefined } }), { server: false, watch: [loc] })
const chiTiet = ref<{ truoc: string, sau: string, doi_tuong: string, hanh_dong: string } | null>(null)
async function xem(id: number) {
  const r = await api<{ dong: { truoc: string, sau: string, doi_tuong: string, hanh_dong: string } }>('/api/admin/cms/thay-doi', { query: { id } })
  chiTiet.value = r.dong
}
const dep = (s: string) => { try { return JSON.stringify(JSON.parse(s), null, 2) } catch { return s } }

const trang = ref(1)
const moiTrang = ref(20)
const tongSo = computed(() => data.value?.ds?.length ?? 0)
const dsHienThi = computed(() => {
  const list = data.value?.ds ?? []
  if (moiTrang.value >= list.length) return list
  const start = (trang.value - 1) * moiTrang.value
  return list.slice(start, start + moiTrang.value)
})
watch(loc, () => { trang.value = 1 })
watch(tongSo, (m) => {
  const max = Math.max(1, Math.ceil(m / moiTrang.value))
  if (trang.value > max) trang.value = max
})
</script>

<template>
  <AdminPage title="Lịch sử thay đổi">
    <AdminCmsChuaCoBang v-if="data && !data.coBang" bang="web_admin_thay_doi" />
    <UInput v-model="loc" icon="i-lucide-filter" placeholder="Lọc đối tượng: tin:, trang:, cai_dat:, tk:, nv:..." class="w-full max-w-md" />
    <UCard :ui="{ body: 'p-0 sm:p-0' }">
      <div class="overflow-x-auto">
        <table class="bang-wiki">
          <thead><tr><th>Lúc</th><th>Người làm</th><th>Hành động</th><th>Đối tượng</th><th>IP</th><th /></tr></thead>
          <tbody>
            <tr v-for="r in dsHienThi" :key="r.id">
              <td class="whitespace-nowrap text-xs">{{ r.thoi_gian }}</td>
              <td class="font-semibold">{{ r.admin_user }}</td>
              <td><UBadge color="neutral" variant="subtle">{{ r.hanh_dong }}</UBadge></td>
              <td>{{ r.doi_tuong }}</td>
              <td class="text-xs">{{ r.ip }}</td>
              <td><UButton size="xs" color="neutral" variant="ghost" icon="i-lucide-git-compare" label="Trước/sau" @click="xem(r.id)" /></td>
            </tr>
            <tr v-if="!data?.ds?.length"><td colspan="6" class="text-center text-muted">Chưa có lịch sử thay đổi.</td></tr>
          </tbody>
        </table>
      </div>
      <template v-if="tongSo > moiTrang" #footer>
        <div class="flex justify-center">
          <UPagination v-model:page="trang" :total="tongSo" :items-per-page="moiTrang" />
        </div>
      </template>
    </UCard>
    <USlideover :open="!!chiTiet" :title="`${chiTiet?.hanh_dong} · ${chiTiet?.doi_tuong}`" :ui="{ content: 'sm:max-w-4xl' }" @update:open="(v: boolean) => { if (!v) chiTiet = null }">
      <template #body>
        <div v-if="chiTiet" class="grid gap-3 lg:grid-cols-2">
          <div><p class="mb-1 text-sm font-bold text-error">Trước</p><pre class="max-h-[70vh] overflow-auto rounded-lg bg-elevated p-3 text-xs whitespace-pre-wrap">{{ dep(chiTiet.truoc) || '(trống)' }}</pre></div>
          <div><p class="mb-1 text-sm font-bold text-success">Sau</p><pre class="max-h-[70vh] overflow-auto rounded-lg bg-elevated p-3 text-xs whitespace-pre-wrap">{{ dep(chiTiet.sau) || '(đã xoá)' }}</pre></div>
        </div>
      </template>
    </USlideover>
  </AdminPage>
</template>
