<script setup lang="ts">
/** Cài đặt website: tên, khẩu hiệu, SEO/OG, hỗ trợ Zalo, mạng xã hội, thanh thông báo, bảo trì, trạng thái máy chủ. */
import AdminCmsChonAnh from '~/components/admin/cms/ChonAnh.vue'

definePageMeta({ layout: 'admin', middleware: 'admin' })
const me = useAdminMe()
const { f: fRef, luu, dangLuu, coBang, daThayDoi } = await useCaiDatAdmin('site')
const f = fRef as Ref<CmsSite>
const chonAnh = useOverlay().create(AdminCmsChonAnh)
async function chonOg() {
  const u = await chonAnh.open().result
  if (u) f.value.anhOg = u
}
async function chonQrZalo() {
  const u = await chonAnh.open().result
  if (u) f.value.hoTroZalo.anhQr = u
}
// Kiểm nhanh phía trình duyệt (server kiểm lại khi lưu: https + host Zalo).
const zaloSai = computed(() => f.value.hoTroZalo.url.trim() !== '' && !lienKetZalo(f.value.hoTroZalo.url))
</script>

<template>
  <AdminPage title="Cài đặt website">
    <template #right>
      <UBadge v-if="daThayDoi" color="warning" variant="subtle">Chưa lưu</UBadge>
      <UButton icon="i-lucide-save" label="Lưu" :loading="dangLuu" :disabled="!me.duocGhiNoiDung.value" @click="luu" />
    </template>
    <AdminCmsChuaCoBang v-if="!coBang" bang="web_cai_dat" />
    <div class="grid gap-4 lg:grid-cols-2">
      <UCard :class="f.baoTri.bat ? 'ring-2 ring-error' : ''">
        <template #header>
          <div class="flex items-center justify-between">
            <b>Chế độ bảo trì</b>
            <USwitch v-model="f.baoTri.bat" :label="f.baoTri.bat ? 'ĐANG BẬT' : 'Tắt'" color="error" />
          </div>
        </template>
        <p class="mb-3 text-sm text-muted">Bật thì mọi trang công khai chỉ hiện thông báo bảo trì. Khu quản trị vẫn dùng được.</p>
        <UFormField label="Tiêu đề"><UInput v-model="f.baoTri.tieuDe" class="w-full" /></UFormField>
        <UFormField label="Nội dung" class="mt-3"><UTextarea v-model="f.baoTri.noiDung" :rows="3" class="w-full" /></UFormField>
      </UCard>
      <UCard>
        <template #header>
          <div class="flex items-center justify-between">
            <b>Thanh thông báo (đầu mọi trang)</b>
            <USwitch v-model="f.thongBao.bat" label="Bật" />
          </div>
        </template>
        <UFormField label="Nội dung"><UInput v-model="f.thongBao.noiDung" placeholder="Bảo trì 20h tối nay..." class="w-full" /></UFormField>
        <div class="mt-3 grid grid-cols-2 gap-3">
          <UFormField label="Liên kết (tuỳ chọn)"><UInput v-model="f.thongBao.to" placeholder="/news/..." class="w-full" /></UFormField>
          <UFormField label="Màu"><USelect v-model="f.thongBao.mau" :items="[{ label: 'Vàng', value: 'vang' }, { label: 'Đỏ', value: 'do' }, { label: 'Xanh', value: 'xanh' }]" class="w-full" /></UFormField>
        </div>
      </UCard>
      <UCard>
        <template #header><b>Nhận diện & SEO</b></template>
        <UFormField label="Khẩu hiệu"><UInput v-model="f.khauHieu" class="w-full" /></UFormField>
        <UFormField label="Mô tả SEO mặc định" class="mt-3"><UTextarea v-model="f.moTaSeo" :rows="3" :maxlength="300" class="w-full" /></UFormField>
        <UFormField label="Ảnh chia sẻ (Open Graph, 1200×630)" class="mt-3">
          <div class="flex items-center gap-3">
            <img :src="f.anhOg || '/og/htbb.png'" alt="" class="h-16 rounded border border-default">
            <UButton size="sm" color="neutral" variant="outline" icon="i-lucide-image" label="Chọn ảnh" @click="chonOg" />
            <UButton v-if="f.anhOg !== '/og/htbb.png'" size="sm" color="neutral" variant="ghost" label="Mặc định" @click="f.anhOg = '/og/htbb.png'" />
          </div>
        </UFormField>
        <p class="mt-3 text-xs text-dimmed">Tên game đặt ở một chỗ trong mã nguồn (đồng bộ với game) nên không sửa ở đây.</p>
      </UCard>
      <UCard>
        <template #header>
          <div class="flex items-center justify-between">
            <b>Hỗ trợ qua Zalo</b>
            <UBadge :color="f.hoTroZalo.url && !zaloSai ? 'success' : 'neutral'" variant="subtle" :label="f.hoTroZalo.url && !zaloSai ? 'Đang hiện' : 'Ẩn (chưa có link)'" />
          </div>
        </template>
        <p class="mb-3 text-sm text-muted">
          Kênh hỗ trợ chính thức. Có link thì hiện ở đầu trang, chân trang, trang Tải game, FAQ và khi người chơi bị khoá tài khoản lúc đăng nhập.
        </p>
        <UFormField label="Link nhóm / OA Zalo" :error="zaloSai ? 'Chỉ nhận https://zalo.me/..., https://chat.zalo.me/... hoặc https://oa.zalo.me/...' : undefined" help="Vd. https://zalo.me/g/abcxyz (nhóm) hoặc https://zalo.me/<id OA>. Để trống = ẩn.">
          <UInput v-model="f.hoTroZalo.url" placeholder="https://zalo.me/g/..." class="w-full" inputmode="url" autocomplete="off" />
        </UFormField>
        <div class="mt-3 grid gap-3 sm:grid-cols-2">
          <UFormField label="Tên hiển thị"><UInput v-model="f.hoTroZalo.ten" :maxlength="80" class="w-full" /></UFormField>
          <UFormField label="Mô tả ngắn"><UInput v-model="f.hoTroZalo.moTa" :maxlength="300" class="w-full" /></UFormField>
        </div>
        <UFormField label="Ảnh QR (tuỳ chọn, từ thư viện ảnh)" class="mt-3">
          <div class="flex items-center gap-3">
            <img v-if="f.hoTroZalo.anhQr" :src="f.hoTroZalo.anhQr" alt="" class="size-16 rounded border border-default bg-white object-contain">
            <span v-else class="grid size-16 place-items-center rounded border border-dashed border-default text-dimmed"><UIcon name="i-lucide-qr-code" class="size-6" /></span>
            <UButton size="sm" color="neutral" variant="outline" icon="i-lucide-image" label="Chọn ảnh" @click="chonQrZalo" />
            <UButton v-if="f.hoTroZalo.anhQr" size="sm" color="neutral" variant="ghost" label="Bỏ ảnh" @click="f.hoTroZalo.anhQr = ''" />
          </div>
        </UFormField>
      </UCard>
      <UCard>
        <template #header>
          <div class="flex items-center justify-between">
            <b>Mạng xã hội / cộng đồng</b>
            <UButton size="xs" icon="i-lucide-plus" label="Thêm" @click="f.mangXaHoi.push({ ten: '', url: '', icon: 'i-lucide-link' })" />
          </div>
        </template>
        <div v-for="(m, i) in f.mangXaHoi" :key="i" class="mt-2 grid grid-cols-[8rem_1fr_9rem_auto] gap-2">
          <UInput v-model="m.ten" placeholder="Tên" />
          <UInput v-model="m.url" placeholder="https://..." />
          <UInput v-model="m.icon" placeholder="i-lucide-..." />
          <UButton size="xs" color="error" variant="ghost" icon="i-lucide-x" aria-label="Xoá" @click="f.mangXaHoi.splice(i, 1)" />
        </div>
        <p v-if="!f.mangXaHoi.length" class="text-sm text-muted">Chưa có — hiện ở chân trang khi thêm.</p>
        <p v-else class="mt-2 text-xs text-dimmed">Chỉ dòng có link https:// thật mới hiện; dòng để trống bị bỏ khi lưu.</p>
      </UCard>
      <UCard>
        <template #header><b>Trạng thái máy chủ (trang chủ)</b></template>
        <UFormField label="Nguồn" help="Để trống: đọc bảng online của game trong CSDL. Hoặc URL JSON trả { song: boolean, online: number }.">
          <UInput v-model="f.trangThaiUrl" placeholder="https://status.example.com/htbb.json" class="w-full" />
        </UFormField>
      </UCard>
    </div>
  </AdminPage>
</template>
