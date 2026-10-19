<script setup lang="ts">
/**
 * Đầu trang công khai — MỘT hàng, không bao giờ chồng chữ ở 1024–2560 px.
 *
 *   [logo] [mục chính… | Thêm ▾] [Tải game]      [🔍] [☀] [Zalo] [ảnh đại diện ▾ | Đăng nhập · Đăng ký]
 *
 * - Mục menu sửa ở /admin/menu (vị trí từng mục: thanh / "Thêm ▾" / nút nổi bật — shared/utils/menuDauTrang.ts).
 * - Mục 'chinh' không đủ chỗ thì TỰ DỒN vào "Thêm ▾": đo bề rộng từng mục (hàng đo ẩn) bằng ResizeObserver.
 * - Mục tài khoản (Nạp, Thành viên, Kích hoạt, Quản trị, Đăng xuất) nằm trong menu ảnh đại diện.
 * - Dưới xl (< 1280 px): menu chính ẩn, dùng nút ☰ (slideover) của UHeader.
 */
import type { DropdownMenuItem, NavigationMenuItem } from '@nuxt/ui'
import { chiaDauTrang, chuanHoaDauTrang, soMucVua, type MucMenu } from '~~/shared/utils/menuDauTrang'

const auth = useAuth()
const adminAuth = useAdminAuth()
// Bản DEMO nạp tiền (HTBB_NAP_DEMO=1): server quyết định ai thấy menu theo token người chơi.
const napDemo = useNapDemo()
onMounted(() => { auth.hydrate(); adminAuth.hydrate(); napDemo.tai() })
watch(() => auth.user.value?.username, () => { napDemo.tai() })

const route = useRoute()
const tenSite = useAppConfig().htbb.tenSite
const { data: site } = useSite()

const cms = useCongKhai()
const danhMuc = useDanhMucCamNang()

function thanhMuc(l: MucMenu): NavigationMenuItem {
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
}

const chia = computed(() => chiaDauTrang(chuanHoaDauTrang(cms.value.menu.dauTrang)))
const nut = computed(() => (chia.value.nut ? { label: chia.value.nut.label, to: lienKetAnToan(chia.value.nut.to) || '/', icon: chia.value.nut.icon || 'i-lucide-download' } : null))

// ---- Tự dồn mục vào "Thêm ▾" theo chỗ trống ----
const khung = ref<HTMLElement | null>(null)
const hangDo = ref<HTMLElement | null>(null)
const soHien = ref(Number.POSITIVE_INFINITY)
function doLai() {
  const k = khung.value
  const h = hangDo.value
  if (!k || !h) return
  const muc = [...h.querySelectorAll<HTMLElement>('[data-do="muc"]')].map(e => Math.ceil(e.getBoundingClientRect().width) + 4)
  const them = Math.ceil(h.querySelector<HTMLElement>('[data-do="them"]')?.getBoundingClientRect().width ?? 90) + 4
  soHien.value = soMucVua(muc, Math.floor(k.getBoundingClientRect().width), them, chia.value.them.length > 0)
}
let ro: ResizeObserver | null = null
onMounted(() => {
  ro = new ResizeObserver(() => doLai())
  if (khung.value) ro.observe(khung.value)
  nextTick(doLai)
  // phông chữ hiển thị tải xong làm chữ rộng ra
  document.fonts?.ready.then(doLai).catch(() => {})
})
onBeforeUnmount(() => ro?.disconnect())
watch(chia, () => nextTick(doLai))

const links = computed<NavigationMenuItem[]>(() => {
  const n = Math.min(soHien.value, chia.value.chinh.length)
  const hien = chia.value.chinh.slice(0, n).map(thanhMuc)
  const don = [...chia.value.chinh.slice(n), ...chia.value.them]
  if (don.length) {
    hien.push({
      label: 'Thêm',
      icon: 'i-lucide-ellipsis',
      active: don.some(m => m.to !== '/' && route.path.startsWith(m.to)),
      children: don.map(m => ({ label: m.label, icon: m.icon || undefined, to: lienKetAnToan(m.to) || '/' })),
    })
  }
  return hien
})

// Menu điện thoại: đủ mọi mục (không dồn), nút nổi bật đứng đầu.
const linksDoc = computed<NavigationMenuItem[]>(() => [
  ...(nut.value ? [{ label: nut.value.label, to: nut.value.to, icon: nut.value.icon }] : []),
  ...[...chia.value.chinh, ...chia.value.them].map(thanhMuc),
])

// ---- Mục tài khoản ----
const mucTaiKhoan = computed<{ label: string, to: string, icon: string }[]>(() => {
  const ds = [{ label: 'Tài khoản của tôi', to: '/account', icon: 'i-lucide-user-round' }]
  // Nạp tiền tắt mặc định (HTBB_NAP_BAT, quyết định Q10): chỉ hiện khi server bật.
  if (site.value?.napBat) ds.push({ label: 'Nạp tiền', to: '/naptien', icon: 'i-lucide-gem' })
  else if (napDemo.xem.value) {
    ds.push({ label: 'Nạp (demo)', to: '/naptien', icon: 'i-lucide-gem' })
    ds.push({ label: 'Thành viên (demo)', to: '/thanh-vien', icon: 'i-lucide-badge-plus' })
  }
  ds.push({ label: 'Kích hoạt', to: '/kichhoat', icon: 'i-lucide-badge-check' })
  return ds
})
const menuNguoiDung = computed<DropdownMenuItem[][]>(() => {
  const nhom: DropdownMenuItem[][] = [
    [{ label: auth.user.value?.username ?? '', type: 'label', icon: 'i-lucide-circle-user-round' }],
    mucTaiKhoan.value.map(m => ({ ...m })),
  ]
  if (adminAuth.loggedIn.value) nhom.push([{ label: 'Quản trị', to: '/admin', icon: 'i-lucide-shield-check', color: 'primary' }])
  nhom.push([{ label: 'Đăng xuất', to: '/logout', icon: 'i-lucide-log-out' }])
  return nhom
})
</script>

<template>
  <UHeader
    mode="slideover"
    to="/"
    :title="`${tenSite} — Trang chủ`"
    :ui="{
      root: 'bg-default/95 backdrop-blur border-b-4 border-pokered-500 shadow-[0_3px_0_var(--poke-outline)]',
      container: 'max-w-[96rem] gap-2 xl:gap-3',
      // Logo + cụm phải giữ nguyên cỡ; phần giữa co lại, mục thừa tự dồn vào Thêm (không đè logo).
      left: 'shrink-0 lg:flex-none',
      center: 'min-w-0 flex-1 xl:flex items-center gap-2 overflow-hidden',
      right: 'shrink-0 lg:flex-none gap-1',
      // Menu ☰ (slideover) dùng tới hết lg: thanh menu chỉ hiện từ xl (1280 px).
      toggle: 'lg:inline-flex xl:hidden',
      content: 'lg:flex xl:hidden',
      overlay: 'lg:block xl:hidden',
    }"
  >
    <!-- UHeader đã tự bọc phần tiêu đề trong một liên kết về `to` (mặc định "/"):
         đặt thêm <NuxtLink> ở đây sẽ thành thẻ <a> lồng nhau -> lệch hydration. -->
    <template #title>
      <SiteLogo />
    </template>

    <div class="hidden min-w-0 flex-1 items-center justify-center gap-2 xl:flex">
      <div ref="khung" class="relative flex min-w-0 flex-1 justify-center">
        <!-- Hàng đo (ẩn): bề rộng thật của từng mục để biết bao nhiêu mục vừa chỗ. -->
        <div ref="hangDo" aria-hidden="true" class="pointer-events-none invisible absolute left-0 top-0 flex whitespace-nowrap">
          <span v-for="m in chia.chinh" :key="m.to" data-do="muc" class="inline-flex items-center gap-1.5 px-2 font-display text-[15px] font-bold">
            <UIcon v-if="m.icon" :name="m.icon" class="size-4" />{{ m.label }}<UIcon v-if="m.to === '/cam-nang'" name="i-lucide-chevron-down" class="size-5" />
          </span>
          <span data-do="them" class="inline-flex items-center gap-1.5 px-2 font-display text-[15px] font-bold">
            <UIcon name="i-lucide-ellipsis" class="size-4" />Thêm<UIcon name="i-lucide-chevron-down" class="size-5" />
          </span>
        </div>
        <UNavigationMenu
          :items="links"
          variant="pill"
          color="primary"
          highlight
          class="min-w-0"
          :ui="{ link: 'font-display font-bold text-[15px] px-2 whitespace-nowrap', linkLeadingIcon: 'size-4', childLinkDescription: 'line-clamp-1', content: 'sm:w-[34rem]' }"
        />
      </div>
      <UButton
        v-if="nut"
        :to="nut.to"
        :icon="nut.icon"
        color="primary"
        variant="solid"
        class="shrink-0 whitespace-nowrap font-display font-bold"
      >
        {{ nut.label }}
      </UButton>
    </div>

    <template #right>
      <SiteTimKiem />
      <UColorModeButton color="neutral" variant="ghost" />
      <SiteHoTroZalo class="hidden 2xl:inline-flex" />
      <ClientOnly>
        <UDropdownMenu v-if="auth.loggedIn.value" :items="menuNguoiDung" :content="{ align: 'end' }" :ui="{ content: 'min-w-52' }">
          <UButton color="neutral" variant="ghost" class="hidden max-w-40 whitespace-nowrap sm:inline-flex" trailing-icon="i-lucide-chevron-down" :aria-label="`Tài khoản ${auth.user.value?.username ?? ''}`" :ui="{ label: 'truncate' }">
            <template #leading>
              <UAvatar :alt="auth.user.value?.username" size="xs" :ui="{ root: 'bg-primary/15', fallback: 'text-primary font-bold' }" />
            </template>
            <span class="hidden 2xl:inline">{{ auth.user.value?.username }}</span>
          </UButton>
        </UDropdownMenu>
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
          <USkeleton class="h-8 w-24" />
        </template>
      </ClientOnly>
    </template>

    <template #body>
      <UNavigationMenu :items="linksDoc" orientation="vertical" class="-mx-2.5" />
      <USeparator class="my-4" />
      <SiteHoTroZalo kieu="chan" nhan="Hỗ trợ qua Zalo" block class="mb-4" />
      <ClientOnly>
        <div v-if="auth.loggedIn.value" class="flex flex-col gap-2">
          <p class="px-1 text-sm font-bold text-muted">
            <UIcon name="i-lucide-circle-user-round" class="mr-1 inline size-4 align-[-3px]" />{{ auth.user.value?.username }}
          </p>
          <UButton v-for="m in mucTaiKhoan" :key="m.to" :to="m.to" :icon="m.icon" color="neutral" variant="soft" block>
            {{ m.label }}
          </UButton>
          <UButton v-if="adminAuth.loggedIn.value" to="/admin" color="primary" variant="soft" icon="i-lucide-shield-check" block>
            Quản trị
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
