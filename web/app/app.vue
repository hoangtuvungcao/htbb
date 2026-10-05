<script setup lang="ts">
useHead({
  titleTemplate: t => (t ? `${t} · KPAH - Khí Phách Anh Hùng` : 'KPAH - Khí Phách Anh Hùng'),
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
