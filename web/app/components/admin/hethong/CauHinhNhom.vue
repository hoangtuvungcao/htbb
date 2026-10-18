<script setup lang="ts">
/**
 * Khối nhúng MỘT nhóm của "Cấu hình hệ thống" vào trang khác (Nạp tiền, Giftcode…): cùng khoá, cùng API, cùng
 * lịch sử với trang /admin/cau-hinh-he-thong — không có công tắc thứ hai.
 *   <AdminHethongCauHinhNhom nhom="nap_tien" />
 *   <AdminHethongCauHinhNhom :khoa="['giftcode.gia_tri_toi_da', 'giftcode.ngan_sach_thang']" tieu-de="Ngân sách giftcode" />
 */
const props = defineProps<{ nhom?: string, khoa?: string[], tieuDe?: string }>()
const ch = useCauHinhTT(`admin-cau-hinh-tt-nhung-${props.nhom ?? (props.khoa ?? []).join(',')}`)
const ds = computed(() => {
  void ch.phienDanhMuc.value
  return danhMucTT().filter(k => (props.nhom ? k.nhom === props.nhom : true) && (props.khoa ? props.khoa.includes(k.khoa) : true))
})
const ten = computed(() => {
  void ch.phienDanhMuc.value
  return props.tieuDe ?? dsNhomTT().find(n => n.ma === props.nhom)?.ten ?? 'Cấu hình'
})
const lyDo = ref('')
const cuaToi = computed(() => ch.soSanh.value.thayDoi.filter(d => ds.value.some(k => k.khoa === d.khoa)))
const coLoi = computed(() => ds.value.some(k => ch.loiNhap.value[k.khoa]))
async function luu() {
  if (await ch.luuThayDoi(lyDo.value.trim(), cuaToi.value.map(d => d.khoa))) lyDo.value = ''
}
</script>

<template>
  <UCard>
    <template #header>
      <div class="flex flex-wrap items-center justify-between gap-2">
        <div>
          <h2 class="font-semibold text-highlighted">{{ ten }}</h2>
          <p class="text-xs text-muted">
            Cùng khoá với trang <NuxtLink to="/admin/cau-hinh-he-thong" class="text-primary hover:underline">Cấu hình hệ thống</NuxtLink> — lưu ở đây là lưu ở đó (có lịch sử, hoàn tác).
          </p>
        </div>
        <UBadge :color="ch.trangThaiApDung.value.mau" variant="subtle" :label="ch.trangThaiApDung.value.chu" class="max-w-full truncate" />
      </div>
    </template>
    <UAlert v-if="ch.error.value" color="error" variant="subtle" title="Không tải được cấu hình" :description="loiApi(ch.error.value)" />
    <div v-else-if="ch.data.value">
      <AdminHethongCauHinhKhoa v-for="k in ds" :key="k.khoa" :k="k" :ch="ch" />
      <div v-if="cuaToi.length" class="mt-3 flex flex-wrap items-center gap-2 border-t border-default pt-3">
        <span class="text-sm text-muted">{{ cuaToi.length }} thay đổi:</span>
        <UBadge v-for="d in cuaToi" :key="d.khoa" color="primary" variant="subtle" size="sm" :label="`${d.ten}: ${hienThiTT(khoaTT(d.khoa), d.cu)} → ${hienThiTT(khoaTT(d.khoa), d.moi)}`" />
        <UInput v-model="lyDo" placeholder="Lý do (bắt buộc)" class="min-w-56 flex-1" />
        <UButton label="Lưu" icon="i-lucide-save" :loading="ch.dangLuu.value" :disabled="coLoi || lyDo.trim().length < 3" @click="luu" />
      </div>
    </div>
    <USkeleton v-else class="h-24 w-full" />
  </UCard>
</template>
