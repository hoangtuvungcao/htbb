<script setup lang="ts">
/**
 * Khung trang công khai. Thanh thông báo + chế độ bảo trì lấy từ cài đặt trong khu quản trị
 * (/admin/cai-dat). Bảo trì chỉ che trang công khai — /admin vẫn vào được để tắt.
 */
const cms = useCongKhai()
const site = computed(() => cms.value.site)
const dongThongBao = ref(false)
const khoaTb = computed(() => `htbb-tb:${site.value.thongBao.noiDung}`)
onMounted(() => {
  try { dongThongBao.value = sessionStorage.getItem(khoaTb.value) === '1' }
  catch { /* trình duyệt chặn bộ nhớ */ }
})
function dong() {
  dongThongBao.value = true
  try { sessionStorage.setItem(khoaTb.value, '1') }
  catch { /* bỏ qua */ }
}
const mauTb = computed(() => ({
  vang: 'bg-pikachu-300 text-navy-950',
  do: 'bg-pokered-600 text-white',
  xanh: 'bg-pokeblue-600 text-white',
})[site.value.thongBao.mau] ?? 'bg-pikachu-300 text-navy-950')
</script>

<template>
  <div class="relative min-h-dvh bg-pattern text-default">
    <a
      href="#main-content"
      class="sr-only focus:not-sr-only focus:fixed focus:left-4 focus:top-4 focus:z-[100] focus:rounded-md focus:bg-primary focus:px-4 focus:py-2 focus:text-inverted"
    >Đến nội dung chính</a>
    <div v-if="site.thongBao.bat && site.thongBao.noiDung && !dongThongBao" class="relative z-50 px-10 py-2 text-center text-sm font-semibold" :class="mauTb" role="status">
      <NuxtLink v-if="lienKetAnToan(site.thongBao.to)" :to="lienKetAnToan(site.thongBao.to)" class="underline-offset-2 hover:underline">
        {{ site.thongBao.noiDung }} <UIcon name="i-lucide-arrow-right" class="inline size-4 align-[-3px]" />
      </NuxtLink>
      <span v-else>{{ site.thongBao.noiDung }}</span>
      <button type="button" class="absolute right-3 top-1/2 -translate-y-1/2 rounded-full p-1 opacity-70 hover:opacity-100" aria-label="Đóng thông báo" @click="dong">
        <UIcon name="i-lucide-x" class="size-4" />
      </button>
    </div>
    <SiteHeader />
    <main id="main-content" tabindex="-1" class="relative outline-none">
      <section v-if="site.baoTri.bat" class="py-24" aria-labelledby="bao-tri-title">
        <UContainer class="max-w-2xl text-center">
          <PokeBall wiggle class="mx-auto size-20" />
          <h1 id="bao-tri-title" class="section-title mt-6 text-4xl text-highlighted">
            {{ site.baoTri.tieuDe }}
          </h1>
          <p class="dialog-box no-arrow mx-auto mt-6 max-w-xl text-lg">
            {{ site.baoTri.noiDung }}
          </p>
        </UContainer>
      </section>
      <slot v-else />
    </main>
    <SiteFooter />
  </div>
</template>
