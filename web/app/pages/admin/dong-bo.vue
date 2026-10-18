<script setup lang="ts">
/** Đồng bộ dữ liệu từ tài liệu game (chạy script sinh dữ liệu) + nhập lịch sự kiện. */
definePageMeta({ layout: 'admin', middleware: 'admin' })
const api = useAdminApi()
const toast = useToast()
const dangChay = ref(false)
const kq = ref<{ ok: boolean, ketQua: { buoc: string, ok: boolean, log: string }[] } | null>(null)
async function chay() {
  dangChay.value = true
  try {
    kq.value = await api('/api/admin/cms/dong-bo', { method: 'POST', body: {} })
    await api('/api/admin/cms/lich/nhap', { method: 'POST', body: { ghiDe: false } }).catch(() => {})
    toast.add({ title: kq.value?.ok ? 'Đồng bộ xong' : 'Có bước lỗi — xem nhật ký', color: kq.value?.ok ? 'success' : 'warning' })
  }
  catch (e) { toast.add({ title: loiApi(e), color: 'error' }) }
  finally { dangChay.value = false }
}
</script>

<template>
  <AdminPage title="Đồng bộ dữ liệu game">
    <UCard>
      <p>Chạy lại 3 bước sinh dữ liệu từ repo game (Pokédex + sprite, dữ liệu cẩm nang từ CSV thiết kế), rồi thêm các tuần sự kiện còn thiếu vào CSDL.</p>
      <ul class="mt-2 list-disc pl-5 text-sm text-muted">
        <li>Cần repo game trên cùng máy (biến <code>HTBB_DIR</code>, mặc định thư mục cạnh website) và mã nguồn website (<code>HTBB_WEB_SRC_DIR</code>).</li>
        <li>Số liệu Pokédex / cẩm nang được đóng gói lúc build: sau khi đồng bộ cần <b>build lại và khởi động lại website</b> (quy trình deploy thường lệ). Lịch sự kiện có hiệu lực ngay.</li>
      </ul>
      <UButton class="mt-4" icon="i-lucide-refresh-cw" label="Đồng bộ ngay" :loading="dangChay" @click="chay" />
    </UCard>
    <UCard v-for="b in kq?.ketQua ?? []" :key="b.buoc">
      <template #header>
        <span class="flex items-center gap-2"><UIcon :name="b.ok ? 'i-lucide-circle-check' : 'i-lucide-circle-x'" :class="b.ok ? 'text-success' : 'text-error'" /><b>{{ b.buoc }}</b></span>
      </template>
      <pre class="max-h-64 overflow-auto text-xs whitespace-pre-wrap">{{ b.log }}</pre>
    </UCard>
  </AdminPage>
</template>
