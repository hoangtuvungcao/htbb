<script setup lang="ts">
/** Bản tải game: link, phiên bản, dung lượng, ngày cập nhật, nhật ký thay đổi cho từng nền tảng. */
definePageMeta({ layout: 'admin', middleware: 'admin' })
const me = useAdminMe()
const { f: fRef, luu, dangLuu, coBang, daThayDoi } = await useCaiDatAdmin('taiGame')
const f = fRef as Ref<CmsTaiGame>
</script>

<template>
  <AdminPage title="Bản tải game">
    <template #right>
      <UBadge v-if="daThayDoi" color="warning" variant="subtle">Chưa lưu</UBadge>
      <UButton to="/download" target="_blank" color="neutral" variant="ghost" icon="i-lucide-external-link" label="Xem trang Tải game" />
      <UButton icon="i-lucide-save" label="Lưu" :loading="dangLuu" :disabled="!me.duocGhiNoiDung.value" @click="luu" />
    </template>
    <AdminCmsChuaCoBang v-if="!coBang" bang="web_cai_dat" />
    <UAlert color="neutral" variant="subtle" icon="i-lucide-info" description="Để trống link thì trang dùng link trong biến môi trường KPAH_LINK_* (nếu có); không có link sẽ hiện 'Đang cập nhật bản tải'." />
    <div class="grid gap-4 lg:grid-cols-2">
      <UCard v-for="b in f.ban" :key="b.ma">
        <template #header>
          <div class="flex items-center justify-between">
            <b>{{ b.ten }}</b>
            <USwitch v-model="b.hien" label="Hiện" />
          </div>
        </template>
        <div class="grid gap-3 sm:grid-cols-2">
          <UFormField label="Tên hiển thị"><UInput v-model="b.ten" class="w-full" /></UFormField>
          <UFormField label="Phiên bản"><UInput v-model="b.phienBan" placeholder="1.0.3" class="w-full" /></UFormField>
          <UFormField label="Link tải" class="sm:col-span-2"><UInput v-model="b.url" placeholder="https://..." class="w-full" /></UFormField>
          <UFormField label="Dung lượng"><UInput v-model="b.dungLuong" placeholder="85 MB" class="w-full" /></UFormField>
          <UFormField label="Ngày cập nhật"><UInput v-model="b.capNhat" placeholder="17/10/2026" class="w-full" /></UFormField>
          <UFormField label="Mô tả ngắn" class="sm:col-span-2"><UInput v-model="b.moTa" class="w-full" /></UFormField>
          <UFormField label="Có gì mới (mỗi dòng một ý)" class="sm:col-span-2"><UTextarea v-model="b.nhatKy" :rows="3" class="w-full" /></UFormField>
        </div>
      </UCard>
    </div>
  </AdminPage>
</template>
