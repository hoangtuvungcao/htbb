<script setup lang="ts">
const tenSite = useAppConfig().htbb.tenSite
// Địa chỉ gốc: MỘT nguồn là HTBB_SITE_URL (qua /api/site), không theo Host của request.
const { data: siteCfg } = useSite()
const goc = useGocSite()
const cms = useCongKhai()
const route = useRoute()
// Chưa có tên miền thật (HTBB_SITE_URL trống / localhost / IP) hoặc chưa đọc được cấu hình -> noindex.
const choIndex = computed(() => siteCfg.value?.choIndex === true)
// Ảnh chia sẻ mặc định (sinh bởi scripts/sinh-anh-og.mjs); trang con có thể ghi đè ogImage.
useSeoMeta({
  ogType: 'website',
  ogImage: () => urlTuyetDoi(goc.value, lienKetAnToan(cms.value.site.anhOg) || '/og/htbb.png'),
  ogImageWidth: 1200,
  ogImageHeight: 630,
  ogImageAlt: `${tenSite} — năm Pokémon khởi đầu trên đồng cỏ`,
  ogUrl: () => urlTuyetDoi(goc.value, route.path),
  ogLocale: 'vi_VN',
  twitterCard: 'summary_large_image',
})
useHead({
  link: [{ rel: 'canonical', href: () => urlTuyetDoi(goc.value, route.path) }],
  titleTemplate: t => (t ? `${t} · ${tenSite}` : tenSite),
  meta: [
    { name: 'theme-color', content: '#e3350d' },
    { property: 'og:site_name', content: tenSite },
    { name: 'application-name', content: tenSite },
    { name: 'apple-mobile-web-app-title', content: tenSite },
  ],
  // Dữ liệu có cấu trúc: tên website (trang chủ thêm VideoGame, cẩm nang thêm BreadcrumbList).
  script: [{
    type: 'application/ld+json',
    key: 'ld-website',
    innerHTML: () => JSON.stringify({ '@context': 'https://schema.org', '@type': 'WebSite', 'name': tenSite, 'url': `${goc.value}/`, 'inLanguage': 'vi' }),
  }],
})
// Trang con có thể tự đặt robots: 'noindex' (useSeoMeta); ở đây chỉ chặn chung khi chưa có tên miền.
useHead(computed(() => ({
  meta: choIndex.value ? [] : [{ name: 'robots', content: 'noindex, nofollow' }],
})))
</script>

<template>
  <UApp :toaster="{ position: 'top-right' }" :tooltip="{ delayDuration: 200 }">
    <NuxtLoadingIndicator color="linear-gradient(90deg,#ffe452,#ff9f1c,#f26a1b)" />
    <NuxtLayout>
      <NuxtPage />
    </NuxtLayout>
  </UApp>
</template>
