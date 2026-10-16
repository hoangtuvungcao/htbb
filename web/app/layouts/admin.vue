<script setup lang="ts">
/**
 * Khung khu quản trị: menu trái chia nhóm (thu gọn được), thanh trên chỉ đường.
 * Chưa đăng nhập -> chỉ hiện nội dung trang (form đăng nhập ở /admin).
 *
 * Mỗi trang admin dùng:
 *   definePageMeta({ layout: 'admin', middleware: 'admin' })
 *   và bọc nội dung trong <AdminPage title="..."> ... </AdminPage>
 */
import type { NavigationMenuItem } from '@nuxt/ui'

const auth = useAdminAuth()
auth.hydrate()

const route = useRoute()
const open = ref(false)

const groups = computed<NavigationMenuItem[][]>(() =>
  ADMIN_MENU.map(g => [
    { label: g.nhom, type: 'label' as const },
    ...g.items.map(i => ({
      label: i.label,
      icon: i.icon,
      to: i.to,
      active: i.to === '/admin' ? route.path === '/admin' : route.path.startsWith(i.to),
      onSelect: () => { open.value = false },
    })),
  ]),
)

// Tạo lúc setup: gọi composable (useRouter bên trong) trong click handler có thể lỗi "nuxt instance unavailable"
const apiAdmin = useAdminApi()

async function thoat() {
  try {
    await apiAdmin('/api/admin/auth/logout', { method: 'POST' })
  } catch { /* phiên đã hết hạn thì thôi */ }
  auth.clear()
  await navigateTo('/admin')
}

useHead({
  meta: [{ name: 'robots', content: 'noindex, nofollow' }],
  titleTemplate: t => (t ? `${t} · Quản trị KPAH` : 'Quản trị KPAH'),
})
</script>

<template>
  <UDashboardGroup v-if="auth.loggedIn.value" unit="rem" storage="local" storage-key="kpah-admin-sidebar">
    <UDashboardSidebar
      id="admin-sidebar"
      v-model:open="open"
      collapsible
      resizable
      class="bg-elevated/40"
      :ui="{ footer: 'lg:border-t lg:border-default' }"
    >
      <template #header="{ collapsed }">
        <NuxtLink to="/admin" class="flex items-center gap-2.5 px-1">
          <img src="/logo-mark.svg" alt="" class="size-8 shrink-0">
          <span v-if="!collapsed" class="min-w-0 leading-tight">
            <strong class="block text-highlighted">Quản trị</strong>
            <small class="block truncate text-xs text-muted">{{ useAppConfig().kpah.tenSite }}</small>
          </span>
        </NuxtLink>
      </template>

      <template #default="{ collapsed }">
        <UNavigationMenu
          v-for="(g, i) in groups"
          :key="i"
          :collapsed="collapsed"
          :items="g"
          orientation="vertical"
          tooltip
          popover
        />
      </template>

      <template #footer="{ collapsed }">
        <div class="flex w-full flex-col gap-1">
          <UButton to="/" icon="i-lucide-house" color="neutral" variant="ghost" :label="collapsed ? undefined : 'Ra trang chủ'" :square="collapsed" block class="justify-start" />
          <UButton icon="i-lucide-log-out" color="neutral" variant="ghost" :label="collapsed ? undefined : 'Thoát'" :square="collapsed" block class="justify-start" @click="thoat" />
          <div v-if="!collapsed" class="flex items-center gap-2 px-2.5 py-1.5 text-sm text-muted">
            <span class="size-2 rounded-full bg-success shadow-[0_0_8px] shadow-success" />
            {{ auth.admin.value?.username }}
          </div>
        </div>
      </template>
    </UDashboardSidebar>

    <slot />
  </UDashboardGroup>

  <div v-else class="relative grid min-h-dvh place-items-center overflow-hidden bg-pattern p-4">
    <FxGlowOrbs />
    <div class="relative w-full max-w-md">
      <slot />
    </div>
  </div>
</template>
