<script setup lang="ts">
/** Lịch sự kiện 52 tuần: nhập từ dữ liệu game, sửa từng tuần. */
import AdminTaikhoanXacNhan from '~/components/admin/taikhoan/XacNhan.vue'

definePageMeta({ layout: 'admin', middleware: 'admin' })
interface Dong { tuan: number, thang: number, ten1: string, ten2: string, phan: string, loai: string, co_che: string, so_ngay: number, phan_thuong: string, moc: string }
const api = useAdminApi()
const toast = useToast()
const me = useAdminMe()
const xacNhan = useOverlay().create(AdminTaikhoanXacNhan)
const { data, refresh } = await useAsyncData('admin-lich', () => api<{ coBang: boolean, ds: Dong[], macDinh: SuKienTuan[] }>('/api/admin/cms/lich'), { server: false })
const tuanNay = tuanTrongNam(new Date())
const loaiItems = Object.entries(LOAI_SU_KIEN).map(([value, v]) => ({ label: v.ten, value }))
const dangSua = ref<Record<string, any> | null>(null)
function sua(r: Dong) {
  dangSua.value = { tuan: r.tuan, thang: Number(r.thang), ten1: r.ten1, ten2: r.ten2, phan: r.phan, loai: r.loai, coChe: r.co_che, soNgay: Number(r.so_ngay), phanThuong: r.phan_thuong, mocGiuChan: r.moc }
}
async function luu() {
  try { await api('/api/admin/cms/lich/luu', { method: 'POST', body: dangSua.value }); toast.add({ title: 'Đã lưu tuần', color: 'success' }); dangSua.value = null; refresh() }
  catch (e) { toast.add({ title: loiApi(e), color: 'error' }) }
}
async function nhap(ghiDe: boolean) {
  if (ghiDe && !(await xacNhan.open({ title: 'Ghi đè toàn bộ lịch bằng dữ liệu game?', description: 'Mọi tuần đã sửa tay sẽ bị thay.', nut: 'Ghi đè', mau: 'warning' }).result)) return
  try { const r = await api<{ them: number }>('/api/admin/cms/lich/nhap', { method: 'POST', body: { ghiDe } }); toast.add({ title: `Đã nhập ${r.them} tuần`, color: 'success' }); refresh() }
  catch (e) { toast.add({ title: loiApi(e), color: 'error' }) }
}
</script>

<template>
  <AdminPage title="Lịch sự kiện năm">
    <template #right>
      <UButton to="/cam-nang/su-kien" target="_blank" color="neutral" variant="ghost" icon="i-lucide-external-link" label="Xem" />
      <UButton v-if="me.duocGhiNoiDung.value" color="neutral" variant="outline" icon="i-lucide-download" label="Nhập tuần còn thiếu" @click="nhap(false)" />
      <UButton v-if="me.duocGhiNoiDung.value && data?.ds.length" color="warning" variant="soft" icon="i-lucide-refresh-ccw" label="Ghi đè từ dữ liệu game" @click="nhap(true)" />
    </template>
    <AdminCmsChuaCoBang v-if="data && !data.coBang" bang="web_su_kien_lich" />
    <UAlert v-else-if="data && !data.ds.length" color="info" variant="subtle" icon="i-lucide-info" title="Website đang dùng lịch sinh từ dữ liệu game" description="Bấm 'Nhập tuần còn thiếu' để chép 52 tuần vào CSDL, rồi sửa từng tuần." />
    <UCard :ui="{ body: 'p-0 sm:p-0' }">
      <div class="overflow-x-auto">
        <table class="bang-wiki">
          <thead><tr><th class="so">Tuần</th><th>Tháng</th><th>Năm 1</th><th>Năm 2</th><th>Loại</th><th>Cơ chế</th><th /></tr></thead>
          <tbody>
            <tr v-for="r in data?.ds ?? []" :key="r.tuan" :class="r.tuan === tuanNay ? 'noi-bat' : ''">
              <td class="so font-bold">{{ r.tuan }}</td>
              <td>{{ r.thang }}</td>
              <td class="font-semibold text-highlighted">{{ r.ten1 }} <span v-if="r.phan" class="text-dimmed">({{ r.phan }})</span></td>
              <td>{{ r.ten2 }}</td>
              <td><span class="inline-flex items-center gap-1"><span class="size-2 rounded-full" :style="{ background: LOAI_SU_KIEN[r.loai]?.mau }" />{{ LOAI_SU_KIEN[r.loai]?.ten ?? r.loai }}</span></td>
              <td class="max-w-sm text-xs">{{ r.co_che }}</td>
              <td><UButton v-if="me.duocGhiNoiDung.value" size="xs" color="neutral" variant="ghost" icon="i-lucide-pencil" aria-label="Sửa" @click="sua(r)" /></td>
            </tr>
          </tbody>
        </table>
      </div>
    </UCard>
    <USlideover :open="!!dangSua" :title="`Sửa tuần ${dangSua?.tuan}`" @update:open="(v: boolean) => { if (!v) dangSua = null }">
      <template #body>
        <div v-if="dangSua" class="space-y-3">
          <div class="grid grid-cols-3 gap-3">
            <UFormField label="Tháng"><UInputNumber v-model="dangSua.thang" :min="1" :max="12" class="w-full" /></UFormField>
            <UFormField label="Số ngày"><UInputNumber v-model="dangSua.soNgay" :min="1" :max="60" class="w-full" /></UFormField>
            <UFormField label="Phần"><UInput v-model="dangSua.phan" placeholder="1/2" class="w-full" /></UFormField>
          </div>
          <UFormField label="Tên (năm 1)"><UInput v-model="dangSua.ten1" class="w-full" /></UFormField>
          <UFormField label="Tên (năm 2)"><UInput v-model="dangSua.ten2" class="w-full" /></UFormField>
          <UFormField label="Loại"><USelect v-model="dangSua.loai" :items="loaiItems" class="w-full" /></UFormField>
          <UFormField label="Cơ chế"><UTextarea v-model="dangSua.coChe" :rows="3" class="w-full" /></UFormField>
          <UFormField label="Phần thưởng"><UInput v-model="dangSua.phanThuong" class="w-full" /></UFormField>
          <UFormField label="Ghi chú giữ chân"><UInput v-model="dangSua.mocGiuChan" class="w-full" /></UFormField>
        </div>
      </template>
      <template #footer><UButton icon="i-lucide-save" label="Lưu" @click="luu" /></template>
    </USlideover>
  </AdminPage>
</template>
