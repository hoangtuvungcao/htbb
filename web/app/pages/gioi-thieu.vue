<script setup lang="ts">
const tenSite = useAppConfig().htbb.tenSite
useSeoMeta({
  title: 'Giới thiệu game',
  description: `${tenSite}: MMORPG thời gian thực nơi bạn là một Pokémon. Thế giới Kanto, Johto, Hoenn; 18 hệ, tiến hoá, đội, PvP mùa, lễ hội theo lịch năm — miễn phí, không bán sức mạnh.`,
})
const N = CAM_NANG.nhiemVu
const cung1 = N.cung.filter(c => c.nam === 1)
/** Điểm độc đáo — mỗi thẻ một hình THẬT: chân dung PMD, ảnh chụp trong game hoặc icon tiền tệ / sự kiện của game. */
const DIEM: { t: string, m: string, dex?: number[], anh?: string, img?: string[], he?: number[] }[] = [
  { dex: [...DEX_KHOI_DAU], t: 'Bạn chính là Pokémon', m: 'Thế giới không có con người. Bạn sinh ra là Charmander, Bulbasaur, Squirtle, Pichu hoặc Eevee — lên cấp và tiến hoá ngay trên bản đồ.' },
  { anh: 'chien-dau', t: 'Đánh thời gian thực', m: 'Chạm để đi tới và chọn mục tiêu, nút chiêu to, thuốc HP / PP một chạm. Không đánh theo lượt.' },
  { he: [1, 2, 4, 3, 10, 14], t: '18 hệ, khắc hệ dịu hơn', m: `Đủ 18 hệ như bản gốc, hệ số tối đa ×2 và không miễn nhiễm — luôn có cách đánh, luôn có lợi thế.` },
  { img: ['/tien/xu_64.png', '/tien/luong_khoa_64.png', '/tien/luong_64.png'], t: 'Không bán sức mạnh', m: 'Không cửa hàng bán chỉ số. Lượng khoá không giao dịch được, trang bị khoá khi mặc, chuyển xu có chốt chống clone.' },
  { img: ['/tien/su_kien_64.png'], t: 'Lễ hội theo lịch năm', m: 'Tết, Giỗ Tổ, Vu Lan, Trung Thu, Halloween, Noel tự mở đúng ngày (cả âm lịch); xen giữa là tuần Pokémon, Ngày cộng đồng, đột kích.' },
  { anh: 'tien-hoa', t: 'Không trần cấp theo mùa', m: `Cấp ${CAM_NANG.capDo.capToiDa} mở cho mọi người ngay từ đầu; lên cấp chậm, năng lượng huấn luyện và bắt kịp giúp người chơi ít giờ không bị bỏ lại.` },
  { anh: 'the-gioi', t: 'Phong cách Mystery Dungeon', m: 'Sprite PMD động 4 hướng từ cộng đồng PMDCollab, bản đồ vẽ lại tông màu tươi sáng, camera cố định cho mọi người.' },
  { anh: 'doi', t: 'Đội, vùng & thầy trò', m: 'Lập đội, tranh cờ giữa ba vùng, kèm người mới để cùng nhận thưởng.' },
  // 0.9.8
  { anh: 'thu-phuc', t: 'Thu phục & trao đổi', m: 'Ném Poké / Great / Ultra Ball, sưu tầm vào Hộp PC, đổi Pokémon với bạn bè hoặc mua bán ở chợ Pokémon.' },
  { anh: 'pho-ban', t: 'Phó bản & Cúp PvP', m: 'Phó bản Hầm ngục 5 tầng mỗi ngày cho nhóm; Cúp PvP tuần Thuỵ Sĩ với chỉ số chuẩn hoá và ngắm tay kéo chiêu.' },
  { anh: 'ho-so-tu-do', t: 'Tủ đồ & danh hiệu', m: 'Trang phục hiện trên Pokémon, hào quang, skin thú và danh hiệu 4 bậc — chỉ là hình và chút chỉ số theo ô, tổng ≤ 5%.' },
  { anh: 'chat', t: 'Chat, nhóm & Lễ kết đôi', m: 'Bảng chat nhiều kênh, tin riêng tới cả khi bạn offline; hai Pokémon cùng nhóm trứng tổ chức Lễ kết đôi.' },
]
const VUNG = [
  { ten: 'Kanto', dex: 4, mo: 'Nơi mọi hành trình bắt đầu. Đồng cỏ Pallet, Rừng Viridian rậm rạp, Núi Mặt Trăng đầy sao băng, con tàu S.S. Anne và đảo lửa Cinnabar.' },
  { ten: 'Johto', dex: 133, mo: 'Vùng đất cổ kính của những ngọn tháp: tháp chuông Ecruteak, Rừng Ilex linh thiêng, Hồ Phẫn Nộ đỏ rực và làng rồng Blackthorn.' },
  { ten: 'Hoenn', dex: 7, mo: `Biển nhiệt đới và núi lửa, nơi Kyogre và Groudon đang ngủ say. Mở ra khi bạn hoàn thành cốt truyện năm 1 và đạt cấp ${POKEDEX.hoenn.capVao}.` },
]
</script>

<template>
  <div>
    <SitePageBanner title="Giới thiệu game" eyebrow="Thế giới" :intro="`${tenSite} — một buổi sáng ở làng khởi đầu, bạn thức dậy… trong hình hài một Pokémon.`" />

    <section class="py-14 sm:py-20" aria-labelledby="cot-truyen">
      <UContainer class="max-w-6xl">
        <div class="grid items-center gap-10 lg:grid-cols-2">
          <div>
            <p class="eyebrow">
              Cốt truyện
            </p>
            <h2 id="cot-truyen" class="section-title mt-4 text-4xl text-highlighted">
              Thức giấc ở làng khởi đầu
            </h2>
            <div class="mt-5 space-y-4 text-lg text-toned">
              <p>
                Trưởng làng Xatu đã chờ bạn từ lâu. Thế giới Pokémon đang đổi thay: Pokémon hoang dã kéo về các tuyến đường, những huyền thoại cổ xưa trở mình dưới lòng núi và đáy biển.
              </p>
              <p>
                Bạn — một Pokémon nhỏ bé — sẽ rời làng (Pewter ở Kanto hoặc Blackthorn ở Johto), đi qua {{ cung1.length }} chặng đường của Kanto và Johto, kết bạn, lập đội, tiến hoá,
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
          <figure class="the-go overflow-hidden p-1.5">
            <span class="block overflow-hidden rounded-[1.2rem]">
              <SiteAnhGame ma="khoi-dau" sizes="(min-width: 1024px) 560px, 100vw" alt="Ảnh trong game: màn chọn Pokémon khởi đầu bên hồ lúc hoàng hôn" />
            </span>
            <figcaption class="px-3 py-2 text-xs text-dimmed">Màn tạo Pokémon trong game: chạm vào Pokémon trên đường để chọn.</figcaption>
          </figure>
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
              <div class="the-go h-full p-2.5">
                <div class="grid h-28 place-items-center overflow-hidden rounded-2xl bg-screen" aria-hidden="true">
                  <SiteAnhGame v-if="d.anh" :ma="d.anh" sizes="(min-width: 1024px) 270px, (min-width: 640px) 50vw, 100vw" class="h-full" />
                  <span v-else-if="d.dex" class="flex -space-x-2">
                    <img v-for="x in d.dex" :key="x" :src="anhChanDung(x)" width="40" height="40" alt="" loading="lazy" class="pixelated size-12 rounded-full bg-white ring-2 ring-white">
                  </span>
                  <span v-else-if="d.he" class="flex flex-wrap justify-center gap-1 px-2">
                    <PokeHeBadge v-for="h in d.he" :key="h" :he="h" size="sm" />
                  </span>
                  <span v-else class="flex gap-3">
                    <img v-for="x in d.img" :key="x" :src="x" width="64" height="64" alt="" loading="lazy" class="size-12">
                  </span>
                </div>
                <h3 class="mt-3 px-2 font-display text-lg font-bold text-highlighted">
                  {{ d.t }}
                </h3>
                <p class="mt-1 px-2 pb-2 text-sm text-muted">
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
