<script setup lang="ts">
const tenSite = useAppConfig().htbb.tenSite
const cms = useCongKhai()
const { tenDangCo, tenSapRaMat } = useBanTai()
// Link giấy phép chỉ có MỘT chỗ (dòng chữ nhỏ cuối chân trang): bỏ khỏi cột menu kể cả khi menu đã lưu trong CSDL còn mục cũ.
const LINK_GIAY_PHEP = ['/ghi-cong', '/giay-phep']
const cot = computed(() => cms.value.menu.chanTrang.map(c => ({ ten: c.ten, links: c.links.filter(l => !LINK_GIAY_PHEP.includes(String(l.to ?? '').split('#')[0]!)).map(l => ({ ...l, to: lienKetAnToan(l.to) || '/' })) })).filter(c => c.links.length))
// Trang điều khoản thanh toán chỉ hiện khi máy chủ bật nạp (HTBB_NAP_BAT).
const { data: siteCfg } = useSite()
const cotDayDu = computed(() => cot.value.map(c => c.ten === 'Pháp lý' && siteCfg.value?.napBat
  ? { ...c, links: [...c.links, { label: 'Điều khoản thanh toán', to: '/dieu-khoan-thanh-toan' }, { label: 'Chính sách hoàn tiền', to: '/chinh-sach-hoan-tien' }] }
  : c))
// Chỉ link mạng xã hội thật (http(s) có tên miền) — ô để trống / '#' không thành link chết.
const mangXaHoi = computed(() => cms.value.site.mangXaHoi.filter(m => lienKetNgoaiThat(m.url)))
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
              {{ cms.site.khauHieu }}. {{ siteCfg?.napBat ? 'Máy chủ phục vụ cộng đồng.' : 'Máy chủ phi lợi nhuận, không nạp tiền, phục vụ cộng đồng.' }}
            </p>
            <div v-if="mangXaHoi.length" class="mt-4 flex flex-wrap gap-2">
              <UButton
                v-for="m in mangXaHoi"
                :key="m.url"
                :to="lienKetNgoaiThat(m.url)"
                target="_blank"
                rel="noopener noreferrer"
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
              <SiteHoTroZalo kieu="chan" nhan="Hỗ trợ qua Zalo" />
              <UButton to="/download" size="sm" class="btn-pikachu" trailing-icon="i-lucide-download">
                Tải game
              </UButton>
              <UButton to="/cam-nang" size="sm" color="neutral" variant="outline" icon="i-lucide-library" class="!bg-transparent !text-white ring-white/40 hover:!bg-white/10">
                Cẩm nang
              </UButton>
            </div>
            <p v-if="tenDangCo" class="mt-3 text-xs text-white/75">
              Có trên {{ tenDangCo }}<template v-if="tenSapRaMat"> · {{ tenSapRaMat }} sắp ra mắt</template>.
            </p>
          </div>
          <nav class="grid grid-cols-2 gap-8 sm:grid-cols-4" aria-label="Liên kết chân trang">
            <div v-for="c in cotDayDu" :key="c.ten">
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
        <SiteLuuYTuoi gon class="mb-4" />
        <p class="mx-auto mb-3 flex flex-wrap justify-center gap-x-4 gap-y-1 text-xs font-semibold">
          <NuxtLink to="/dieu-khoan" class="text-muted hover:text-primary">Điều khoản sử dụng</NuxtLink>
          <NuxtLink to="/quyen-rieng-tu" class="text-muted hover:text-primary">Quyền riêng tư</NuxtLink>
          <template v-if="siteCfg?.napBat">
            <NuxtLink to="/dieu-khoan-thanh-toan" class="text-muted hover:text-primary">Điều khoản thanh toán</NuxtLink>
            <NuxtLink to="/chinh-sach-hoan-tien" class="text-muted hover:text-primary">Chính sách hoàn tiền</NuxtLink>
          </template>
        </p>
        <p class="mx-auto max-w-4xl text-center text-xs leading-relaxed text-dimmed">
          {{ tenSite }} là dự án fan <strong class="text-toned">phi thương mại</strong>, không liên kết, không được tài trợ hay xác nhận bởi Nintendo, Creatures Inc., GAME FREAK inc. hay The Pokémon Company.
          Pokémon và các tên, hình ảnh liên quan là thương hiệu © Nintendo / Creatures / GAME FREAK / The Pokémon Company.
          Sprite & chân dung: PMDCollab (CC BY-NC 4.0); tile bản đồ: Kenney, Ninja Adventure (CC0) —
          <NuxtLink to="/giay-phep" class="underline hover:text-primary">Giấy phép & nguồn tài nguyên</NuxtLink>.
          Mã nguồn game gốc thuộc bản quyền TeaMobi; máy chủ này chỉ dùng cho mục đích học tập / cộng đồng.
        </p>
      </UContainer>
    </div>
  </footer>
</template>
