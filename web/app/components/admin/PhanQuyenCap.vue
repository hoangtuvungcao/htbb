<script setup lang="ts">
/** Cấp / gỡ vai trò quản trị (siêu quản trị trong biến môi trường + vai trò trong CSDL). Một khối của trang /admin/vai-tro. */
import AdminTaikhoanXacNhan from '~/components/admin/taikhoan/XacNhan.vue'

const api = useAdminApi()
const toast = useToast()
const xacNhan = useOverlay().create(AdminTaikhoanXacNhan)
const { data, refresh } = await useAsyncData('admin-phan-quyen', () => api<{ coBang: boolean, env: string[], ds: { username: string, vai_tro: string, ghi_chu: string, tao_boi: string, tao_luc: string }[], vaiTro: Record<string, string> }>('/api/admin/phan-quyen'), { server: false })
const f = reactive({ username: '', vaiTro: 'bien_tap', ghiChu: '' })
const items = computed(() => Object.entries(data.value?.vaiTro ?? {}).map(([value, label]) => ({ value, label })))
async function luu() {
  try { await api('/api/admin/phan-quyen/luu', { method: 'POST', body: f }); toast.add({ title: 'Đã cấp quyền', color: 'success' }); f.username = ''; f.ghiChu = ''; refresh() }
  catch (e) { toast.add({ title: loiApi(e), color: 'error' }) }
}
async function go(u: string) {
  if (!(await xacNhan.open({ title: `Gỡ quyền của ${u}?`, nut: 'Gỡ', mau: 'error' }).result)) return
  try { await api('/api/admin/phan-quyen/xoa', { method: 'POST', body: { username: u } }); refresh() }
  catch (e) { toast.add({ title: loiApi(e), color: 'error' }) }
}
</script>

<template>
  <section class="space-y-4">
    <AdminCmsChuaCoBang v-if="data && !data.coBang" bang="web_admin_vai_tro" />
    <UCard>
      <template #header><b>Cấp quyền cho tài khoản game</b></template>
      <div class="grid gap-3 md:grid-cols-[1fr_14rem_1fr_auto] md:items-end">
        <UFormField label="Tên tài khoản"><UInput v-model="f.username" placeholder="ten_tai_khoan" class="w-full" /></UFormField>
        <UFormField label="Vai trò"><USelect v-model="f.vaiTro" :items="items" class="w-full" /></UFormField>
        <UFormField label="Ghi chú"><UInput v-model="f.ghiChu" class="w-full" /></UFormField>
        <UButton icon="i-lucide-user-plus" label="Cấp quyền" :disabled="!f.username" @click="luu" />
      </div>
    </UCard>
    <UCard :ui="{ body: 'p-0 sm:p-0' }">
      <table class="bang-wiki">
        <thead><tr><th>Tài khoản</th><th>Vai trò</th><th>Ghi chú</th><th>Cấp bởi</th><th /></tr></thead>
        <tbody>
          <tr v-for="u in data?.env ?? []" :key="`env-${u}`">
            <td class="font-semibold">{{ u }}</td><td><UBadge color="error" variant="subtle">Siêu quản trị</UBadge></td><td class="text-xs">HTBB_ADMIN_ACCOUNTS (sửa trên máy chủ)</td><td>—</td><td />
          </tr>
          <tr v-for="r in data?.ds ?? []" :key="r.username">
            <td class="font-semibold">{{ r.username }}</td>
            <td><UBadge color="primary" variant="subtle">{{ data?.vaiTro[r.vai_tro] ?? r.vai_tro }}</UBadge></td>
            <td>{{ r.ghi_chu }}</td>
            <td class="text-xs">{{ r.tao_boi }} · {{ r.tao_luc }}</td>
            <td><UButton size="xs" color="error" variant="ghost" icon="i-lucide-user-x" aria-label="Gỡ quyền" @click="go(r.username)" /></td>
          </tr>
        </tbody>
      </table>
    </UCard>
  </section>
</template>
