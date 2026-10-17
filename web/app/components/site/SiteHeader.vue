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
    { label: 'Pokédex', to: '/pokedex', icon: 'i-lucide-book-open', active: route.path.startsWith('/pokedex') },
    { label: 'Hướng dẫn', to: '/huong-dan', icon: 'i-lucide-compass' },
    { label: 'Xếp hạng', to: '/ranking', icon: 'i-lucide-trophy' },
    { label: 'Tin tức', to: '/news', icon: 'i-lucide-newspaper', active: route.path.startsWith('/news') },
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
      root: 'bg-default/95 backdrop-blur border-b-4 border-pokered-500 shadow-[0_3px_0_var(--poke-outline)]',
      container: 'max-w-7xl',
    }"
  >
    <!-- UHeader đã tự bọc phần tiêu đề trong một liên kết về `to` (mặc định "/"):
         đặt thêm <NuxtLink> ở đây sẽ thành thẻ <a> lồng nhau -> lệch hydration. -->
    <template #title>
      <SiteLogo />
    </template>

    <UNavigationMenu
      :items="links"
      variant="pill"
      color="primary"
      highlight
      class="hidden lg:flex"
      :ui="{ link: 'font-display font-bold text-[15px]', linkLeadingIcon: 'size-4' }"
    />

    <template #right>
      <UColorModeButton color="neutral" variant="ghost" />
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
          <UButton to="/register" color="warning" variant="solid" trailing-icon="i-lucide-sparkles" class="btn-pikachu">
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
          <UButton to="/register" color="warning" class="btn-pikachu" block>
            Đăng ký
          </UButton>
        </div>
      </ClientOnly>
    </template>
  </UHeader>
</template>
