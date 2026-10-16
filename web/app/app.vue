<script setup lang="ts">
const tenSite = useAppConfig().kpah.tenSite
useHead({
  titleTemplate: t => (t ? `${t} · ${tenSite}` : tenSite),
  meta: [
    { property: 'og:site_name', content: tenSite },
    { name: 'application-name', content: tenSite },
    { name: 'apple-mobile-web-app-title', content: tenSite },
  ],
})
</script>

<template>
  <UApp :toaster="{ position: 'top-right' }" :tooltip="{ delayDuration: 200 }">
    <NuxtLoadingIndicator color="linear-gradient(90deg,#20e0bf,#f9c02a)" />
    <NuxtLayout>
      <NuxtPage />
    </NuxtLayout>
  </UApp>
</template>
