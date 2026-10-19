<script setup lang="ts">
/**
 * Khung chung cho mọi trang Cẩm nang (/cam-nang/<slug>): đầu trang, thanh bên (nhóm trang,
 * lọc nhanh), mục lục "Trên trang này", nút trang trước/sau, SEO.
 *
 *   <WikiShell slug="he-khac-che"> ...nội dung (WikiSection)... </WikiShell>
 */
const props = defineProps<{ slug: string }>()

// Danh mục đã trộn CSDL (đổi tên / ẩn / xếp lại / trang mới ở /admin/trang).
const danhMuc = useDanhMucCamNang()
const trang = computed(() => danhMuc.value.find(p => p.slug === props.slug) ?? trangCamNang(props.slug))
// Nội dung Markdown thêm cho trang này (nếu admin đã viết) — hiện sau phần có sẵn.
const { data: them } = await useFetch<{ html: string, muc: { id: string, ten: string }[] } | null>(() => `/api/cam-nang-db/${props.slug}`, { key: `trang-db-${props.slug}`, default: () => null })
const mucDayDu = computed(() => {
  const goc = trangCamNang(props.slug)?.muc ?? []
  return [...goc, ...(them.value?.muc ?? []).filter(m => !goc.some(g => g.id === m.id))]
})
const tenSite = useAppConfig().htbb.tenSite

useSeoMeta({
  title: () => `${trang.value?.ten ?? 'Cẩm nang'} — Cẩm nang`,
  description: () => trang.value?.moTa ?? `Cẩm nang cơ chế ${tenSite}.`,
  ogTitle: () => `${trang.value?.ten ?? 'Cẩm nang'} · ${tenSite}`,
  ogDescription: () => trang.value?.moTa ?? '',
})

const goc = useGocSite()
useHead({
  script: [{
    type: 'application/ld+json',
    key: 'ld-breadcrumb',
    innerHTML: () => JSON.stringify({
      '@context': 'https://schema.org',
      '@type': 'BreadcrumbList',
      'itemListElement': [
        { '@type': 'ListItem', 'position': 1, 'name': 'Trang chủ', 'item': `${goc.value}/` },
        { '@type': 'ListItem', 'position': 2, 'name': 'Cẩm nang', 'item': `${goc.value}/cam-nang` },
        { '@type': 'ListItem', 'position': 3, 'name': trang.value?.ten ?? 'Cẩm nang', 'item': `${goc.value}/cam-nang/${props.slug}` },
      ],
    }),
  }],
})

const loc = ref('')
const nhom = computed(() => {
  const t = boDau(loc.value.trim())
  return NHOM_CAM_NANG.map(n => ({
    ten: n,
    trang: danhMuc.value.filter(p => p.nhom === n && (!t || boDau(`${p.ten} ${p.moTa} ${p.tuKhoa ?? ''}`).includes(t))),
  })).filter(n => n.trang.length)
})

const chiSo = computed(() => danhMuc.value.findIndex(p => p.slug === props.slug))
const truoc = computed(() => (chiSo.value > 0 ? danhMuc.value[chiSo.value - 1] : undefined))
const sau = computed(() => (chiSo.value >= 0 ? danhMuc.value[chiSo.value + 1] : undefined))
const moMenu = ref(false)
</script>

<template>
  <div>
    <SitePageBanner
      :title="trang?.ten ?? 'Cẩm nang'"
      :eyebrow="trang?.nhom ?? 'Cẩm nang'"
      :intro="trang?.moTa"
      :trail="[{ label: 'Cẩm nang', to: '/cam-nang' }]"
    />

    <UContainer class="max-w-7xl py-10 sm:py-14">
      <div class="grid gap-8 lg:grid-cols-[16rem_minmax(0,1fr)] xl:grid-cols-[16rem_minmax(0,1fr)_13rem]">
        <!-- Thanh bên: các trang cẩm nang -->
        <aside class="lg:sticky lg:top-24 lg:max-h-[calc(100dvh-7rem)] lg:self-start lg:overflow-y-auto lg:pr-1" aria-label="Các trang cẩm nang">
          <UButton
            class="w-full justify-between lg:hidden"
            color="neutral"
            variant="outline"
            :trailing-icon="moMenu ? 'i-lucide-chevron-up' : 'i-lucide-chevron-down'"
            :aria-expanded="moMenu"
            aria-controls="wiki-nav"
            @click="moMenu = !moMenu"
          >
            <span class="flex items-center gap-2"><UIcon name="i-lucide-library" class="size-4" /> Mục lục cẩm nang</span>
          </UButton>
          <div id="wiki-nav" :class="moMenu ? 'mt-3 block' : 'hidden lg:block'">
            <UInput v-model="loc" icon="i-lucide-filter" placeholder="Lọc trang..." size="sm" class="mb-4 w-full" aria-label="Lọc trang cẩm nang" />
            <nav class="space-y-5">
              <div v-for="n in nhom" :key="n.ten">
                <p class="px-2 font-display text-xs font-extrabold uppercase tracking-wider text-dimmed">
                  {{ n.ten }}
                </p>
                <ul class="mt-1.5 space-y-0.5">
                  <li v-for="p in n.trang" :key="p.slug">
                    <NuxtLink
                      :to="`/cam-nang/${p.slug}`"
                      class="flex items-center gap-2 rounded-xl px-2 py-1.5 text-sm font-semibold transition-colors"
                      :class="p.slug === slug ? 'bg-pokered-600 text-white shadow-[0_2px_0_var(--poke-outline)]' : 'text-toned hover:bg-elevated hover:text-highlighted'"
                      :aria-current="p.slug === slug ? 'page' : undefined"
                      @click="moMenu = false"
                    >
                      <UIcon :name="p.icon" class="size-4 shrink-0" aria-hidden="true" />
                      {{ p.ten }}
                    </NuxtLink>
                  </li>
                </ul>
              </div>
              <p v-if="!nhom.length" class="px-2 text-sm text-muted">
                Không có trang nào khớp.
              </p>
            </nav>
            <div class="mt-6 space-y-2 border-t-2 border-dashed border-[var(--ui-border)] pt-4">
              <NuxtLink to="/huong-dan" class="flex items-center gap-2 px-2 text-sm font-semibold text-primary hover:underline">
                <UIcon name="i-lucide-compass" class="size-4" /> Hướng dẫn tân thủ
              </NuxtLink>
              <NuxtLink to="/pokedex" class="flex items-center gap-2 px-2 text-sm font-semibold text-primary hover:underline">
                <UIcon name="i-lucide-book-open" class="size-4" /> Pokédex
              </NuxtLink>
            </div>
          </div>
        </aside>

        <!-- Nội dung -->
        <article class="wiki min-w-0">
          <slot />
          <div v-if="them?.html" class="noi-dung-md mt-12" v-html="them.html" />

          <div class="mt-14 rounded-2xl bg-elevated/60 p-4 text-xs text-muted">
            <UIcon name="i-lucide-database" class="mr-1 inline size-3.5 align-[-2px]" aria-hidden="true" />
            Số liệu có thể được điều chỉnh trong các bản cập nhật cân bằng. Mục ghi "từ 0.9.6" có ở bản cập nhật kế tiếp;
            mục "Sắp có" chưa có trong game. Khi có khác biệt, thông báo trong game là chuẩn.
          </div>

          <nav class="mt-8 grid gap-3 sm:grid-cols-2" aria-label="Trang trước / sau">
            <NuxtLink v-if="truoc" :to="`/cam-nang/${truoc.slug}`" class="group rounded-2xl glass p-4 transition hover:-translate-y-0.5">
              <span class="text-xs font-bold uppercase tracking-wide text-dimmed">← Trang trước</span>
              <span class="mt-1 block font-display text-lg font-bold text-highlighted group-hover:text-primary">{{ truoc.ten }}</span>
            </NuxtLink>
            <span v-else />
            <NuxtLink v-if="sau" :to="`/cam-nang/${sau.slug}`" class="group rounded-2xl glass p-4 text-right transition hover:-translate-y-0.5">
              <span class="text-xs font-bold uppercase tracking-wide text-dimmed">Trang sau →</span>
              <span class="mt-1 block font-display text-lg font-bold text-highlighted group-hover:text-primary">{{ sau.ten }}</span>
            </NuxtLink>
          </nav>
        </article>

        <!-- Trên trang này -->
        <nav v-if="mucDayDu.length" class="hidden xl:sticky xl:top-24 xl:block xl:self-start" aria-label="Trên trang này">
          <p class="font-display text-xs font-extrabold uppercase tracking-wider text-dimmed">
            Trên trang này
          </p>
          <ul class="mt-2 space-y-1 border-l-2 border-[var(--ui-border)]">
            <li v-for="m in mucDayDu" :key="m.id">
              <a :href="`#${m.id}`" class="-ml-0.5 block border-l-2 border-transparent py-1 pl-3 text-sm text-muted hover:border-primary hover:text-highlighted">{{ m.ten }}</a>
            </li>
          </ul>
        </nav>
      </div>
    </UContainer>
  </div>
</template>
