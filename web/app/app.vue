<script setup lang="ts">
const tenSite = useAppConfig().htbb.tenSite
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
    <NuxtLoadingIndicator color="linear-gradient(90deg,#e3350d,#ffcb05,#3d7dca)" />
    <NuxtLayout>
      <NuxtPage />
    </NuxtLayout>
  </UApp>
</template>
