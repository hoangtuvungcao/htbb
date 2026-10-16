<script setup lang="ts">
const tenSite = useAppConfig().kpah.tenSite
useHead({
  titleTemplate: t => (t ? `${t} · ${tenSite}` : tenSite),
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
