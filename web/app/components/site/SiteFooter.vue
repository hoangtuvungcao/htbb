<script setup lang="ts">
const tenSite = useAppConfig().htbb.tenSite
const cms = useCongKhai()
const cot = computed(() => cms.value.menu.chanTrang.map(c => ({ ten: c.ten, links: c.links.map(l => ({ ...l, to: lienKetAnToan(l.to) || '/' })) })))
const mangXaHoi = computed(() => cms.value.site.mangXaHoi.filter(m => lienKetAnToan(m.url)))
</script>

<template>
  <footer class="relative mt-16">
    <!-- Bãi cỏ cao nối chân trang -->
    <div class="relative h-6 bg-grass-500 dark:bg-grass-800" aria-hidden="true">
      <div class="grass-edge !-top-[33px] !bottom-auto !bg-grass-500 dark:!bg-grass-800" />
    </div>
    <div class="on-navy">
      <UContainer class="max-w-7xl py-12">
        <div class="grid gap-10 lg:grid-cols-[1.2fr_2fr]">
          <div>
            <SiteLogo />
            <p class="mt-4 max-w-sm text-sm text-muted">
              {{ cms.site.khauHieu }}. Máy chủ phi lợi nhuận, không nạp tiền, phục vụ cộng đồng.
            </p>
            <div v-if="mangXaHoi.length" class="mt-4 flex flex-wrap gap-2">
              <UButton
                v-for="m in mangXaHoi"
                :key="m.url"
                :to="lienKetAnToan(m.url)"
                target="_blank"
                rel="noopener"
                size="sm"
                color="neutral"
                variant="ghost"
                :icon="m.icon || 'i-lucide-link'"
                :aria-label="m.ten"
                class="!text-white hover:!bg-white/10"
              >
                {{ m.ten }}
              </UButton>
            </div>
            <div class="mt-5 flex flex-wrap gap-2">
              <UButton to="/download" size="sm" class="btn-pikachu" trailing-icon="i-lucide-download">
                Tải game
              </UButton>
              <UButton to="/cam-nang" size="sm" color="neutral" variant="outline" icon="i-lucide-library" class="!bg-transparent !text-white ring-white/40 hover:!bg-white/10">
                Cẩm nang
              </UButton>
            </div>
          </div>
          <nav class="grid grid-cols-2 gap-8 sm:grid-cols-4" aria-label="Liên kết chân trang">
            <div v-for="c in cot" :key="c.ten">
              <p class="font-display text-sm font-extrabold uppercase tracking-wider text-highlighted">
                {{ c.ten }}
              </p>
              <ul class="mt-3 space-y-2 text-sm">
                <li v-for="l in c.links" :key="l.to">
                  <NuxtLink :to="l.to" class="text-muted transition-colors hover:text-primary">
                    {{ l.label }}
                  </NuxtLink>
                </li>
              </ul>
            </div>
          </nav>
        </div>
        <div class="my-8 flex items-center gap-3" aria-hidden="true">
          <span class="h-0.5 flex-1 bg-[var(--ui-border)]" /><PokeBall class="size-5" /><span class="h-0.5 flex-1 bg-[var(--ui-border)]" />
        </div>
        <p class="mx-auto max-w-4xl text-center text-xs leading-relaxed text-dimmed">
          {{ tenSite }} là dự án fan <strong class="text-toned">phi thương mại</strong>, không liên kết, không được tài trợ hay xác nhận bởi Nintendo, Creatures Inc., GAME FREAK inc. hay The Pokémon Company.
          Pokémon và các tên, hình ảnh liên quan là thương hiệu © Nintendo / Creatures / GAME FREAK / The Pokémon Company.
          Sprite & chân dung: PMDCollab (CC BY-NC 4.0); tile bản đồ: Kenney, Ninja Adventure (CC0) —
          <NuxtLink to="/ghi-cong" class="underline hover:text-primary">xem ghi công</NuxtLink>.
          Mã nguồn game gốc thuộc bản quyền TeaMobi; máy chủ này chỉ dùng cho mục đích học tập / cộng đồng.
        </p>
      </UContainer>
    </div>
  </footer>
</template>
