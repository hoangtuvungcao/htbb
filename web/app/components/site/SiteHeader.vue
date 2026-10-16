<script setup lang="ts">
import type { NavigationMenuItem } from '@nuxt/ui'

const auth = useAuth()
onMounted(() => auth.hydrate())

const route = useRoute()
const tenSite = useAppConfig().kpah.tenSite
const { data: site } = useSite()

const links = computed<NavigationMenuItem[]>(() => {
  const ds: NavigationMenuItem[] = [
    { label: 'Trang chủ', to: '/', icon: 'i-lucide-house' },
    { label: 'Tải game', to: '/download', icon: 'i-lucide-download' },
    { label: 'Xếp hạng', to: '/ranking', icon: 'i-lucide-trophy' },
    { label: 'Tin tức', to: '/news', icon: 'i-lucide-scroll-text', active: route.path.startsWith('/news') },
  ]
  if (auth.loggedIn.value) {
    // Nạp tiền tắt mặc định (KPAH_NAP_BAT, quyết định Q10): chỉ hiện khi server bật.
    if (site.value?.napBat) ds.push({ label: 'Nạp mốc', to: '/naptien', icon: 'i-lucide-gem' })
    ds.push({ label: 'Kích hoạt', to: '/kichhoat', icon: 'i-lucide-badge-check' })
  }
  return ds
})
</script>

<template>
  <UHeader
    mode="slideover"
    to="/"
    :title="`${tenSite} — Trang chủ`"
    :ui="{
      root: 'bg-default/60 backdrop-blur-xl border-b border-default/60',
      container: 'max-w-7xl',
    }"
  >
    <!-- UHeader đã tự bọc phần tiêu đề trong một liên kết về `to` (mặc định "/"):
         đặt thêm <NuxtLink> ở đây sẽ thành thẻ <a> lồng nhau -> lệch hydration. -->
    <template #title>
      <img src="/logo.svg" width="180" height="50" :alt="`${tenSite} — Trang chủ`" class="h-11 w-auto drop-shadow-[0_0_18px_rgba(249,192,42,.25)]">
    </template>

    <UNavigationMenu :items="links" variant="link" color="primary" class="hidden lg:flex" />

    <template #right>
      <ClientOnly>
        <template v-if="auth.loggedIn.value">
          <UButton to="/account" color="neutral" variant="ghost" icon="i-lucide-user-round" class="hidden sm:inline-flex">
            {{ auth.user.value?.username }}
          </UButton>
          <UButton to="/logout" color="neutral" variant="outline" icon="i-lucide-log-out" class="hidden sm:inline-flex">
            Đăng xuất
          </UButton>
        </template>
        <template v-else>
          <UButton to="/login" color="neutral" variant="ghost" class="hidden sm:inline-flex">
            Đăng nhập
          </UButton>
          <UButton to="/register" color="primary" variant="solid" trailing-icon="i-lucide-sparkles" class="shadow-[0_0_24px_rgba(249,192,42,.35)]">
            Đăng ký
          </UButton>
        </template>
        <template #fallback>
          <USkeleton class="h-8 w-40" />
        </template>
      </ClientOnly>
    </template>

    <template #body>
      <UNavigationMenu :items="links" orientation="vertical" class="-mx-2.5" />
      <USeparator class="my-4" />
      <ClientOnly>
        <div v-if="auth.loggedIn.value" class="flex flex-col gap-2">
          <UButton to="/account" color="neutral" variant="soft" icon="i-lucide-user-round" block>
            Tài khoản ({{ auth.user.value?.username }})
          </UButton>
          <UButton to="/logout" color="neutral" variant="outline" icon="i-lucide-log-out" block>
            Đăng xuất
          </UButton>
        </div>
        <div v-else class="flex flex-col gap-2">
          <UButton to="/login" color="neutral" variant="outline" block>
            Đăng nhập
          </UButton>
          <UButton to="/register" color="primary" block>
            Đăng ký
          </UButton>
        </div>
      </ClientOnly>
    </template>
  </UHeader>
</template>
