<script setup lang="ts">
useSeoMeta({
  title: 'Trang chủ',
  description: 'MMORPG nơi chính bạn là một Pokémon: chọn 1 trong 5 Pokémon khởi đầu, lên cấp, tiến hoá và phiêu lưu cùng bạn bè. Tải game và đăng ký tài khoản.',
})

const auth = useAuth()
onMounted(() => auth.hydrate())
const { data: site } = await useSite()
const hoTro = computed(() => linkHoTro(site.value))
const tenSite = useAppConfig().kpah.tenSite

const stats = [
  { so: '05', tieuDe: 'Pokémon khởi đầu', mo: 'Chọn người bạn đồng hành của chính bạn' },
  { so: '18', tieuDe: 'Hệ khắc chế', mo: 'Lửa, Nước, Cỏ, Điện... biết hệ là thắng' },
  { so: '21', tieuDe: 'Dạng tiến hoá', mo: 'Lên cấp và lớn mạnh qua từng giai đoạn' },
]

/**
 * 5 Pokémon khởi đầu, theo cột tob_char.class 0..4 (shared/utils/pokemon.ts).
 * Thứ tự hiển thị: Charmander, Bulbasaur, Squirtle, Pichu, Eevee.
 */
const KIEU_KHOI_DAU: Record<number, { mo: string }> = {
  0: { mo: 'Ngọn lửa trên đuôi không bao giờ tắt. Cận chiến cân bằng, càng đánh càng hăng.' },
  1: { mo: 'Hạt giống trên lưng lớn dần theo từng trận. Lì đòn, trụ vững tuyến đầu.' },
  3: { mo: 'Mai rùa cứng như đá, phun nước cực mạnh. Thủ chắc rồi phản công.' },
  4: { mo: 'Nhỏ mà có võ: tích điện ở má rồi bắn tia sét từ xa.' },
  2: { mo: `Gen linh hoạt hiếm có. Đến cấp ${CAP_NHANH_EEVEE}, bạn tự chọn 1 trong 8 hướng tiến hoá.` },
}

const khoiDau = [0, 1, 3, 4, 2].map((cls) => {
  const dex = DEX_KHOI_DAU[cls as 0 | 1 | 2 | 3 | 4]
  const loai = loaiPokemon(dex)!
  const laEevee = cls === 2
  const chuoi = laEevee
    ? [{ dex, ten: loai.ten, cap: 0 }]
    : chuoiTienHoa(dex).map((d, i, ds) => ({ dex: d, ten: tenLoai(d), cap: i === 0 ? 0 : loaiPokemon(ds[i - 1]!)!.capTienHoa }))
  return {
    cls,
    dex,
    ten: loai.ten,
    he: loaiTheoDex(dex)?.he ?? [],
    anhSrc: anhPokemon(dex),
    chuoi,
    nhanhEevee: laEevee ? NHANH_EEVEE.map(d => ({ dex: d, ten: tenLoai(d) })) : [],
    ...KIEU_KHOI_DAU[cls]!,
  }
})

const portals = [
  { so: '01 · KHỞI HÀNH', icon: 'i-lucide-download', tieuDe: 'Bước vào thế giới Pokémon', mo: 'Chọn phiên bản dành cho bạn và bắt đầu chuyến phiêu lưu.', link: 'Tải game', to: '/download' },
  { so: '02 · VINH DANH', icon: 'i-lucide-trophy', tieuDe: 'Lưu danh bảng vàng', mo: 'Theo dõi những Pokémon mạnh nhất và chủ nhân của chúng.', link: 'Xem xếp hạng', to: '/ranking' },
  { so: '03 · HỘI NGỘ', icon: 'i-lucide-newspaper', tieuDe: 'Tin tức vùng đất', mo: 'Khám phá tin tức, sự kiện và những cuộc hẹn cùng bạn bè.', link: 'Đọc tin tức', to: '/news' },
]

const steps = computed(() => [
  { so: '1', tieuDe: 'Tạo tài khoản', mo: 'Đăng ký tài khoản để bắt đầu hành trình của bạn.', link: 'Đăng ký ngay', to: '/register' },
  { so: '2', tieuDe: 'Tải phiên bản phù hợp', mo: 'Chọn nền tảng và làm theo hướng dẫn cài đặt.', link: 'Chọn bản tải', to: '/download' },
  { so: '3', tieuDe: 'Chọn Pokémon khởi đầu', mo: 'Đăng nhập game, chọn Pokémon của bạn và tìm những người bạn đồng hành.', link: 'Cộng đồng & hỗ trợ', to: hoTro.value },
])

const faq = computed(() => [
  { label: 'Tôi cần làm gì sau khi đăng ký?', slot: 'faq1' as const },
  { label: 'Tôi gặp khó khăn khi cài đặt hoặc đăng nhập?', slot: 'faq2' as const },
])
</script>

<template>
  <div>
    <!-- ============ HERO: trời, cỏ, 5 Pokémon khởi đầu ============ -->
    <section class="relative isolate overflow-hidden" aria-labelledby="hero-heading">
      <SiteHeroSky class="-z-10" />

      <UContainer class="max-w-7xl">
        <div class="grid items-end gap-6 pb-10 pt-12 sm:pt-16 lg:min-h-[640px] lg:grid-cols-[1.1fr_1fr] lg:items-center lg:gap-10 lg:pb-16">
          <div class="lg:pb-24">
            <FxReveal>
              <p class="eyebrow">
                {{ tenSite }} · MMORPG
              </p>
            </FxReveal>
            <FxReveal :delay="0.1">
              <h1 id="hero-heading" class="section-title mt-5 text-[2.6rem] leading-[1.08] text-highlighted sm:text-6xl lg:text-[4.2rem]">
                Bạn không huấn luyện Pokémon.<br>
                <span class="text-gilded">Bạn chính là Pokémon!</span>
              </h1>
            </FxReveal>
            <FxReveal :delay="0.2">
              <div class="dialog-box mt-7 max-w-xl">
                <p class="text-base sm:text-lg">
                  Chọn một Pokémon khởi đầu, lên cấp, tiến hoá và phiêu lưu thời gian thực cùng bạn bè: săn Pokémon hoang dã, hạ boss huyền thoại, lập đội và tranh tài PvP.
                </p>
              </div>
            </FxReveal>
            <FxReveal :delay="0.3">
              <div class="mt-8 flex flex-wrap gap-3">
                <UButton to="/download" size="xl" trailing-icon="i-lucide-download" class="btn-game px-6">
                  Tải game ngay
                </UButton>
                <ClientOnly>
                  <UButton v-if="auth.loggedIn.value" to="/account" size="xl" color="warning" class="btn-pikachu px-6">
                    Tài khoản của bạn
                  </UButton>
                  <UButton v-else to="/register" size="xl" color="warning" class="btn-pikachu px-6">
                    Tạo tài khoản
                  </UButton>
                  <template #fallback>
                    <UButton to="/register" size="xl" color="warning" class="btn-pikachu px-6">
                      Tạo tài khoản
                    </UButton>
                  </template>
                </ClientOnly>
              </div>
            </FxReveal>
          </div>

          <FxReveal :delay="0.15" class="relative">
            <SiteStarterStage />
          </FxReveal>
        </div>
      </UContainer>
    </section>

    <!-- ============ DẢI SỐ LIỆU ============ -->
    <div class="bg-grass-400 pb-2 dark:bg-grass-800">
      <UContainer class="max-w-6xl">
        <FxReveal>
          <div class="grid divide-y-2 divide-[var(--ui-border)] overflow-hidden rounded-2xl glass ring-gilded sm:grid-cols-3 sm:divide-x-2 sm:divide-y-0">
            <div v-for="s in stats" :key="s.so" class="flex items-center gap-4 p-5">
              <b class="grid size-14 shrink-0 place-items-center rounded-xl bg-screen font-pixel text-lg text-pokered-600 dark:text-pikachu-300">{{ s.so }}</b>
              <span>
                <strong class="block font-display text-lg text-highlighted">{{ s.tieuDe }}</strong>
                <span class="text-sm text-muted">{{ s.mo }}</span>
              </span>
            </div>
          </div>
        </FxReveal>
      </UContainer>
      <div class="h-10" />
    </div>

    <!-- ============ 5 POKÉMON KHỞI ĐẦU ============ -->
    <section id="classes" class="scroll-mt-20 py-20" aria-labelledby="classes-title">
      <UContainer class="max-w-7xl">
        <FxReveal class="text-center">
          <p class="eyebrow">
            Chọn người bạn của chính bạn
          </p>
          <h2 id="classes-title" class="section-title mt-4 text-4xl text-highlighted sm:text-5xl">
            Năm Pokémon khởi đầu
          </h2>
          <p class="mx-auto mt-4 max-w-2xl text-muted">
            Mỗi Pokémon một hệ, một lối đánh và một con đường tiến hoá. Bạn sẽ bắt đầu chuyến phiêu lưu trong hình hài nào?
          </p>
        </FxReveal>
        <div class="mt-12 grid gap-5 sm:grid-cols-2 lg:grid-cols-5">
          <FxReveal v-for="(c, i) in khoiDau" :key="c.dex" :delay="i * 0.08">
            <article class="dex-card group flex h-full flex-col transition-transform duration-200 hover:-translate-y-1">
              <!-- Màn hình Pokédex -->
              <div class="dex-screen mt-5 overflow-hidden">
                <div class="relative grid h-36 place-items-center bg-screen [--s:3]">
                  <span class="absolute left-2 top-2 font-pixel text-[10px] text-navy-700 dark:text-grass-200">#{{ String(c.dex).padStart(4, '0') }}</span>
                  <PokeSprite :dex="c.dex" :delay="i * 0.3" />
                </div>
              </div>
              <div class="dex-screen mt-2 flex flex-1 flex-col p-4">
                <h3 class="font-display text-2xl font-extrabold text-highlighted">
                  {{ c.ten }}
                </h3>
                <div class="mt-1 flex flex-wrap gap-1.5">
                  <PokeHeBadge v-for="h in c.he" :key="h" :he="h" />
                </div>
                <p class="mt-3 flex-1 text-sm text-muted">
                  {{ c.mo }}
                </p>
                <!-- Chuỗi tiến hoá -->
                <div class="mt-4 border-t-2 border-dashed border-[var(--ui-border)] pt-3">
                  <p class="font-display text-xs font-bold uppercase tracking-wider text-dimmed">
                    Tiến hoá
                  </p>
                  <ol v-if="!c.nhanhEevee.length" class="mt-2 flex flex-wrap items-center gap-x-1 gap-y-2 text-xs">
                    <li v-for="(t, j) in c.chuoi" :key="t.dex" class="inline-flex items-center gap-1">
                      <UIcon v-if="j > 0" name="i-lucide-chevron-right" class="size-3 text-dimmed" aria-hidden="true" />
                      <img :src="anhPokemon(t.dex)" width="40" height="40" alt="" class="pixelated size-6 rounded">
                      <span class="font-semibold text-toned">{{ t.ten }}</span>
                      <span v-if="t.cap" class="text-dimmed">(Lv {{ t.cap }})</span>
                    </li>
                  </ol>
                  <template v-else>
                    <p class="mt-2 text-xs font-semibold text-toned">
                      Cấp {{ CAP_NHANH_EEVEE }}: chọn 1 trong 8
                    </p>
                    <ul class="mt-2 grid grid-cols-4 gap-1">
                      <li v-for="t in c.nhanhEevee" :key="t.dex">
                        <img :src="anhPokemon(t.dex)" width="40" height="40" :alt="t.ten" :title="t.ten" class="pixelated size-8 rounded">
                      </li>
                    </ul>
                  </template>
                </div>
              </div>
            </article>
          </FxReveal>
        </div>
      </UContainer>
    </section>

    <!-- ============ THẾ GIỚI ============ -->
    <section class="relative isolate py-20" aria-labelledby="explore-title">
      <div class="absolute inset-0 -z-10 bg-pokeblue-50 dark:bg-navy-900/60" aria-hidden="true" />
      <FxGlowOrbs />
      <UContainer class="relative max-w-7xl">
        <FxReveal>
          <p class="eyebrow">
            Thế giới của bạn
          </p>
          <h2 id="explore-title" class="section-title mt-4 text-4xl text-highlighted sm:text-5xl">
            Thế giới Pokémon không chỉ có chiến đấu.
          </h2>
        </FxReveal>
        <div class="mt-12 grid gap-6 md:grid-cols-3">
          <FxReveal v-for="(p, i) in portals" :key="p.to" :delay="i * 0.1">
            <NuxtLink :to="p.to" class="group flex h-full flex-col rounded-2xl glass ring-gilded p-7 transition-transform hover:-translate-y-1">
              <span class="font-display text-xs font-bold tracking-[0.18em] text-dimmed">{{ p.so }}</span>
              <span class="mt-5 grid size-14 place-items-center rounded-2xl border-2 border-[var(--poke-outline)] bg-pikachu-400 text-navy-950 shadow-[0_3px_0_var(--poke-outline)]">
                <UIcon :name="p.icon" class="size-7" />
              </span>
              <h3 class="mt-5 font-display text-2xl font-bold text-highlighted">{{ p.tieuDe }}</h3>
              <p class="mt-2 flex-1 text-muted">{{ p.mo }}</p>
              <span class="mt-6 inline-flex items-center gap-2 border-t-2 border-dashed border-[var(--ui-border)] pt-4 font-display font-bold text-primary">
                {{ p.link }} <UIcon name="i-lucide-arrow-right" class="size-4 transition-transform group-hover:translate-x-1" />
              </span>
            </NuxtLink>
          </FxReveal>
        </div>
      </UContainer>
    </section>

    <!-- ============ HÀNH TRÌNH + FAQ ============ -->
    <section id="guide" class="scroll-mt-20 pt-20" aria-labelledby="guide-title">
      <UContainer class="max-w-6xl">
        <FxReveal>
          <div class="relative overflow-hidden rounded-3xl glass ring-gilded p-7 sm:p-12">
            <PokeBall class="pointer-events-none absolute -right-14 -top-14 size-48 rotate-12 opacity-10" />
            <p class="eyebrow">
              Hẹn gặp bạn trong thế giới Pokémon
            </p>
            <h2 id="guide-title" class="section-title mt-4 text-4xl text-highlighted">
              Hành trình bắt đầu từ đây.
            </h2>
            <ol class="mt-10 grid gap-8 md:grid-cols-3">
              <li v-for="s in steps" :key="s.so" class="relative">
                <span class="grid size-12 place-items-center rounded-full border-[3px] border-[var(--poke-outline)] bg-pokered-500 font-pixel text-sm text-white shadow-[0_3px_0_var(--poke-outline)]">{{ s.so }}</span>
                <h3 class="mt-4 text-xl font-bold text-highlighted">{{ s.tieuDe }}</h3>
                <p class="mt-1 text-muted">{{ s.mo }}</p>
                <ULink :to="s.to" class="mt-3 inline-flex items-center gap-1 font-display font-bold text-primary hover:underline">
                  {{ s.link }} <UIcon name="i-lucide-arrow-right" class="size-4" />
                </ULink>
              </li>
            </ol>
            <UAccordion :items="faq" class="mt-12" :ui="{ trigger: 'font-display text-lg font-bold' }">
              <template #faq1-body>
                <p class="text-muted">
                  Tải và cài đặt game, sau đó đăng nhập bằng tài khoản vừa tạo. Nếu tài khoản yêu cầu kích hoạt, hãy truy cập
                  <NuxtLink to="/kichhoat" class="text-primary hover:underline">trang kích hoạt</NuxtLink> và làm theo hướng dẫn.
                </p>
              </template>
              <template #faq2-body>
                <p class="text-muted">
                  Tham gia <ULink :to="hoTro" class="text-primary hover:underline">nhóm hỗ trợ</ULink> và mô tả lỗi để được hướng dẫn. Không chia sẻ mật khẩu tài khoản trong nhóm.
                </p>
              </template>
            </UAccordion>
          </div>
        </FxReveal>
      </UContainer>
    </section>
  </div>
</template>
