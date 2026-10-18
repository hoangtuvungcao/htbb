<script setup lang="ts">
import type { NavigationMenuItem } from '@nuxt/ui'

const auth = useAuth()
const adminAuth = useAdminAuth()
onMounted(() => { auth.hydrate(); adminAuth.hydrate() })

const route = useRoute()
const tenSite = useAppConfig().htbb.tenSite
const { data: site } = useSite()

const cms = useCongKhai()
const danhMuc = useDanhMucCamNang()
const links = computed<NavigationMenuItem[]>(() => {
  // Menu đầu trang sửa được ở /admin/menu; mục "Cẩm nang" tự kèm danh sách con.
  const ds: NavigationMenuItem[] = cms.value.menu.dauTrang.map((l) => {
    const to = lienKetAnToan(l.to) || '/'
    const item: NavigationMenuItem = { label: l.label, to, icon: l.icon || undefined, active: to !== '/' && route.path.startsWith(to) }
    if (to === '/cam-nang') {
      item.children = [
        ...danhMuc.value.filter(p => ['he-khac-che', 'cap-do', 'tien-hoa', 'trang-bi', 'kinh-te', 'su-kien', 'nhiem-vu', 'ban-do'].includes(p.slug))
          .map(p => ({ label: p.ten, description: p.moTa, icon: p.icon, to: `/cam-nang/${p.slug}` })),
        { label: 'Tất cả cẩm nang', description: `${danhMuc.value.length} trang: PvP, boss, Pokédex, vật phẩm, FAQ...`, icon: 'i-lucide-library', to: '/cam-nang' },
      ]
    }
    return item
  })
  if (auth.loggedIn.value) {
    // Nạp tiền tắt mặc định (HTBB_NAP_BAT, quyết định Q10): chỉ hiện khi server bật.
    if (site.value?.napBat) ds.push({ label: 'Nạp tiền', to: '/naptien', icon: 'i-lucide-gem' })
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
      container: 'max-w-[96rem]',
      // Logo + nút bên phải giữ nguyên kích thước; menu giữa co lại trong phần còn lại (không đè logo).
      left: 'shrink-0 lg:flex-none',
      center: 'min-w-0 flex-1 justify-center',
      right: 'shrink-0 lg:flex-none',
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
      class="hidden xl:flex"
      :ui="{ link: 'font-display font-bold text-[15px] px-2 whitespace-nowrap', linkLeadingIcon: 'size-4', childLinkDescription: 'line-clamp-1', content: 'sm:w-[34rem]' }"
    />

    <template #right>
      <SiteTimKiem />
      <SiteHoTroZalo class="hidden lg:inline-flex" />
      <UColorModeButton color="neutral" variant="ghost" />
      <ClientOnly>
        <template v-if="auth.loggedIn.value">
          <!-- Thanh trên chật (9 mục menu): Quản trị / Đăng xuất chỉ là icon, chữ nằm ở tooltip;
               menu điện thoại (#body) vẫn hiện đủ chữ. -->
          <UTooltip v-if="adminAuth.loggedIn.value" text="Quản trị">
            <UButton to="/admin" color="primary" variant="soft" icon="i-lucide-shield-check" square aria-label="Quản trị" class="hidden sm:inline-flex" />
          </UTooltip>
          <UButton to="/account" color="neutral" variant="ghost" icon="i-lucide-user-round" class="hidden max-w-36 whitespace-nowrap sm:inline-flex" :ui="{ label: 'truncate' }">
            {{ auth.user.value?.username }}
          </UButton>
          <UTooltip text="Đăng xuất">
            <UButton to="/logout" color="neutral" variant="outline" icon="i-lucide-log-out" square aria-label="Đăng xuất" class="hidden sm:inline-flex" />
          </UTooltip>
        </template>
        <template v-else>
          <UButton to="/login" color="neutral" variant="ghost" class="hidden 2xl:inline-flex">
            Đăng nhập
          </UButton>
          <UButton to="/register" color="warning" variant="solid" class="btn-pikachu whitespace-nowrap">
            <UIcon name="i-lucide-sparkles" class="hidden size-4 sm:inline" />
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
      <SiteHoTroZalo kieu="chan" nhan="Hỗ trợ qua Zalo" block class="mb-4" />
      <ClientOnly>
        <div v-if="auth.loggedIn.value" class="flex flex-col gap-2">
          <UButton v-if="adminAuth.loggedIn.value" to="/admin" color="primary" variant="soft" icon="i-lucide-shield-check" block>
            Quản trị
          </UButton>
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
