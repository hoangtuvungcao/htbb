<script setup lang="ts">
const tenSite = useAppConfig().htbb.tenSite
const url = useRequestURL()
const cms = useCongKhai()
const route = useRoute()
// Ảnh chia sẻ mặc định (sinh bởi scripts/sinh-anh-og.mjs); trang con có thể ghi đè ogImage.
useSeoMeta({
  ogType: 'website',
  ogImage: () => new URL(lienKetAnToan(cms.value.site.anhOg) || '/og/htbb.png', url.origin).href,
  ogImageWidth: 1200,
  ogImageHeight: 630,
  ogImageAlt: `${tenSite} — năm Pokémon khởi đầu trên đồng cỏ`,
  ogUrl: () => `${url.origin}${route.path}`,
  ogLocale: 'vi_VN',
  twitterCard: 'summary_large_image',
})
useHead({
  link: [{ rel: 'canonical', href: () => `${url.origin}${route.path}` }],
  titleTemplate: t => (t ? `${t} · ${tenSite}` : tenSite),
  meta: [
    { name: 'theme-color', content: '#e3350d' },
    { property: 'og:site_name', content: tenSite },
    { name: 'application-name', content: tenSite },
    { name: 'apple-mobile-web-app-title', content: tenSite },
  ],
})
</script>

<template>
  <UApp :toaster="{ position: 'top-right' }" :tooltip="{ delayDuration: 200 }">
    <NuxtLoadingIndicator color="linear-gradient(90deg,#e3350d,#ffcb05,#3d7dca)" />
    <NuxtLayout>
      <NuxtPage />
    </NuxtLayout>
  </UApp>
</template>
