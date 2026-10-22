<script setup lang="ts">
/**
 * Chân trang gọn: logo + nút Tải game / Zalo, các cột liên kết (sửa ở /admin/menu), dòng tuổi 12+, pháp lý,
 * ghi công tài nguyên ngắn. Dòng bản quyền mã nguồn game gốc chỉ nằm ở /giay-phep.
 */
const tenSite = useAppConfig().htbb.tenSite
const cms = useCongKhai()
// Link giấy phép chỉ ở dòng pháp lý cuối chân trang: bỏ khỏi cột menu kể cả khi menu đã lưu trong CSDL còn mục cũ.
const LINK_GIAY_PHEP = ['/ghi-cong', '/giay-phep']
const cot = computed(() => cms.value.menu.chanTrang.map(c => ({ ten: c.ten, links: c.links.filter(l => !LINK_GIAY_PHEP.includes(String(l.to ?? '').split('#')[0]!)).map(l => ({ ...l, to: lienKetAnToan(l.to) || '/' })) })).filter(c => c.links.length && c.ten !== 'Pháp lý'))
const { data: siteCfg } = useSite()
// Chỉ link mạng xã hội thật (http(s) có tên miền) — ô để trống / '#' không thành link chết.
const mangXaHoi = computed(() => cms.value.site.mangXaHoi.filter(m => lienKetNgoaiThat(m.url)))
const nam = new Date().getFullYear()
</script>

<template>
  <footer class="vung-toi mt-20 border-t-[3px] border-[#ffcb05]">
    <UContainer class="max-w-7xl py-10">
      <div class="grid gap-10 lg:grid-cols-[1fr_2fr]">
        <div>
          <SiteLogo />
          <p class="mt-3 max-w-sm text-sm text-muted">
            {{ cms.site.khauHieu }}.
          </p>
          <div class="mt-5 flex flex-wrap gap-2">
            <UButton to="/download" class="btn-pikachu" icon="i-lucide-download">
              Tải game
            </UButton>
            <SiteHoTroZalo kieu="chan" nhan="Hỗ trợ qua Zalo" />
          </div>
          <div v-if="mangXaHoi.length" class="mt-4 flex flex-wrap gap-1">
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
            >
              {{ m.ten }}
            </UButton>
          </div>
        </div>
        <nav class="grid grid-cols-2 gap-8 sm:grid-cols-3" aria-label="Liên kết chân trang">
          <div v-for="c in cot" :key="c.ten">
            <p class="font-display text-sm font-extrabold uppercase tracking-wider text-[#ffcb05]">
              {{ c.ten }}
            </p>
            <ul class="mt-3 space-y-2 text-sm">
              <li v-for="l in c.links" :key="l.to">
                <NuxtLink :to="l.to" class="text-muted transition-colors hover:text-white">
                  {{ l.label }}
                </NuxtLink>
              </li>
            </ul>
          </div>
        </nav>
      </div>

      <div class="mt-10 border-t border-[#3b3366] pt-6">
        <SiteLuuYTuoi gon class="mb-4" />
        <p class="flex flex-wrap justify-center gap-x-5 gap-y-1 text-xs font-semibold">
          <NuxtLink to="/dieu-khoan" class="text-muted hover:text-white">Điều khoản sử dụng</NuxtLink>
          <NuxtLink to="/quyen-rieng-tu" class="text-muted hover:text-white">Quyền riêng tư</NuxtLink>
          <template v-if="siteCfg?.napBat">
            <NuxtLink to="/dieu-khoan-thanh-toan" class="text-muted hover:text-white">Điều khoản thanh toán</NuxtLink>
            <NuxtLink to="/chinh-sach-hoan-tien" class="text-muted hover:text-white">Chính sách hoàn tiền</NuxtLink>
          </template>
          <NuxtLink to="/giay-phep" class="text-muted hover:text-white">Giấy phép & nguồn tài nguyên</NuxtLink>
        </p>
        <p class="mx-auto mt-4 max-w-3xl text-center text-xs leading-relaxed text-dimmed">
          © {{ nam }} {{ tenSite }} — dự án fan phi thương mại, không liên kết với Nintendo, Creatures Inc., GAME FREAK inc.
          hay The Pokémon Company. Pokémon là thương hiệu của các công ty trên. Chân dung Pokémon: PMDCollab (CC BY-NC 4.0).
        </p>
      </div>
    </UContainer>
  </footer>
</template>
