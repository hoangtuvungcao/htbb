<script setup lang="ts">
const tenSite = useAppConfig().htbb.tenSite
useSeoMeta({
  title: 'Giới thiệu game',
  description: `${tenSite}: MMORPG thời gian thực nơi bạn là một Pokémon. Thế giới Kanto, Johto, Hoenn; 18 hệ, tiến hoá, đội, PvP mùa, sự kiện mỗi tuần — miễn phí, không nạp tiền.`,
})
const N = CAM_NANG.nhiemVu
const cung1 = N.cung.filter(c => c.nam === 1)
const DIEM = [
  { icon: 'i-lucide-paw-print', t: 'Bạn chính là Pokémon', m: 'Thế giới không có con người. Bạn sinh ra là Charmander, Bulbasaur, Squirtle, Pichu hoặc Eevee — lên cấp và tiến hoá ngay trên bản đồ.' },
  { icon: 'i-lucide-swords', t: 'Đánh thời gian thực', m: 'MMO hành động: di chuyển, né, xoay chiêu. Không đánh theo lượt.' },
  { icon: 'i-lucide-flame', t: '18 hệ, khắc hệ dịu hơn', m: `Đủ 18 hệ như bản gốc, hệ số tối đa ×2 và không miễn nhiễm — luôn có cách đánh, luôn có lợi thế.` },
  { icon: 'i-lucide-heart-handshake', t: 'Công bằng tuyệt đối', m: 'Phi thương mại: không nạp tiền, không bán sức mạnh. Ai chăm chơi, chơi giỏi thì mạnh.' },
  { icon: 'i-lucide-calendar-heart', t: '52 tuần sự kiện', m: 'Tết, Trung Thu, Ngày cộng đồng, Tuần Shiny, đột kích huyền thoại — mỗi tuần một điều mới.' },
  { icon: 'i-lucide-hourglass', t: 'Trần cấp theo mùa', m: `Cấp tối đa ${CAM_NANG.capDo.capToiDa} mở dần; người chơi ít giờ không bị bỏ lại, cốt truyện trải đều cả năm.` },
  { icon: 'i-lucide-palette', t: 'Phong cách Mystery Dungeon', m: 'Sprite PMD động 4 hướng từ cộng đồng PMDCollab, bản đồ vẽ lại tông màu tươi sáng.' },
  { icon: 'i-lucide-users', t: 'Đội, vùng & thầy trò', m: 'Lập đội, tranh cờ giữa ba vùng, kèm người mới để cùng nhận thưởng.' },
]
const VUNG = [
  { ten: 'Kanto', dex: 4, mo: 'Nơi mọi hành trình bắt đầu. Đồng cỏ Pallet, Rừng Viridian rậm rạp, Núi Mặt Trăng đầy sao băng, con tàu S.S. Anne và đảo lửa Cinnabar.' },
  { ten: 'Johto', dex: 133, mo: 'Vùng đất cổ kính của những ngọn tháp: tháp chuông Ecruteak, Rừng Ilex linh thiêng, Hồ Phẫn Nộ đỏ rực và làng rồng Blackthorn.' },
  { ten: 'Hoenn', dex: 7, mo: 'Biển nhiệt đới và núi lửa, nơi Kyogre và Groudon đang ngủ say. Mở ra trong năm thứ hai.' },
]
</script>

<template>
  <div>
    <SitePageBanner title="Giới thiệu game" eyebrow="Thế giới" :intro="`${tenSite} — một buổi sáng ở Pallet, bạn thức dậy… trong hình hài một Pokémon.`" />

    <section class="py-14 sm:py-20" aria-labelledby="cot-truyen">
      <UContainer class="max-w-6xl">
        <div class="grid items-center gap-10 lg:grid-cols-2">
          <div>
            <p class="eyebrow">
              Cốt truyện
            </p>
            <h2 id="cot-truyen" class="section-title mt-4 text-4xl text-highlighted">
              Thức giấc ở Pallet
            </h2>
            <div class="mt-5 space-y-4 text-lg text-toned">
              <p>
                Trưởng làng Xatu đã chờ bạn từ lâu. Thế giới Pokémon đang đổi thay: Pokémon hoang dã kéo về các tuyến đường, những huyền thoại cổ xưa trở mình dưới lòng núi và đáy biển.
              </p>
              <p>
                Bạn — một Pokémon nhỏ bé — sẽ rời Pallet, đi qua {{ cung1.length }} chặng đường của Kanto và Johto, kết bạn, lập đội, tiến hoá,
                và cuối cùng đứng trên đỉnh Núi Silver khi huyền thoại thức tỉnh.
              </p>
            </div>
            <div class="mt-8 flex flex-wrap gap-3">
              <UButton to="/huong-dan" class="btn-game" size="lg" trailing-icon="i-lucide-arrow-right">
                Bắt đầu hành trình
              </UButton>
              <UButton to="/cam-nang/nhiem-vu#chinh-tuyen" color="neutral" variant="outline" size="lg" icon="i-lucide-scroll-text">
                Các chương truyện
              </UButton>
            </div>
          </div>
          <div class="relative isolate overflow-hidden rounded-3xl glass ring-gilded">
            <SiteHeroSky class="-z-10" />
            <div class="relative pt-6">
              <SiteStarterStage nho />
            </div>
          </div>
        </div>
      </UContainer>
    </section>

    <section class="bg-pokeblue-50 py-14 dark:bg-navy-900/60 sm:py-20" aria-labelledby="vung-title">
      <UContainer class="max-w-6xl">
        <p class="eyebrow">
          Các vùng đất
        </p>
        <h2 id="vung-title" class="section-title mt-4 text-4xl text-highlighted">
          Ba vùng, một thế giới
        </h2>
        <div class="mt-10 grid gap-5 md:grid-cols-3">
          <article v-for="v in VUNG" :key="v.ten" class="overflow-hidden rounded-3xl glass">
            <PokeSan :dex="v.dex" :he="loaiTheoDex(v.dex)?.he" :size="150" class="w-full" />
            <div class="p-5">
              <span class="route-sign">{{ v.ten }}</span>
              <p class="mt-3 text-muted">
                {{ v.mo }}
              </p>
            </div>
          </article>
        </div>
        <p class="mt-6 text-center">
          <UButton to="/cam-nang/ban-do" color="neutral" variant="ghost" trailing-icon="i-lucide-map">
            Xem bản đồ thế giới
          </UButton>
        </p>
      </UContainer>
    </section>

    <section class="py-14 sm:py-20" aria-labelledby="doc-dao">
      <UContainer class="max-w-6xl">
        <p class="eyebrow">
          Điểm độc đáo
        </p>
        <h2 id="doc-dao" class="section-title mt-4 text-4xl text-highlighted">
          Không giống game Pokémon nào bạn từng chơi
        </h2>
        <ul class="mt-10 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <li v-for="(d, i) in DIEM" :key="d.t">
            <FxReveal :delay="(i % 4) * 0.06" class="h-full">
              <div class="h-full rounded-3xl glass p-5">
                <span class="grid size-11 place-items-center rounded-2xl bg-pikachu-400 text-navy-950 ring-2 ring-[color-mix(in_oklab,var(--poke-outline)_70%,transparent)]"><UIcon :name="d.icon" class="size-5" /></span>
                <h3 class="mt-4 font-display text-lg font-bold text-highlighted">
                  {{ d.t }}
                </h3>
                <p class="mt-1 text-sm text-muted">
                  {{ d.m }}
                </p>
              </div>
            </FxReveal>
          </li>
        </ul>
      </UContainer>
    </section>

    <section aria-labelledby="hanh-trinh">
      <UContainer class="max-w-6xl">
        <div class="rounded-3xl glass ring-gilded p-6 sm:p-10">
          <h2 id="hanh-trinh" class="section-title text-3xl text-highlighted">
            Hành trình năm đầu
          </h2>
          <p class="mt-2 text-muted">
            {{ cung1.length }} cung truyện, mỗi cung 4 chương, gắn với từng vùng đất (không tiết lộ nội dung).
          </p>
          <ol class="mt-6 grid gap-2 sm:grid-cols-2 lg:grid-cols-3">
            <li v-for="c in cung1" :key="c.ma" class="flex items-center gap-3 rounded-xl bg-elevated/60 px-3 py-2">
              <span class="grid size-9 shrink-0 place-items-center rounded-lg bg-navy-800 font-pixel text-[10px] text-pikachu-300">{{ c.ma }}</span>
              <span class="min-w-0">
                <span class="block truncate font-semibold text-highlighted">{{ c.ten }}</span>
                <span class="text-xs text-muted">{{ c.vung }} · cấp {{ c.capTu }}–{{ c.capDen }}</span>
              </span>
            </li>
          </ol>
        </div>
      </UContainer>
    </section>
  </div>
</template>
