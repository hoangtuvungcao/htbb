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
const KIEU_KHOI_DAU: Record<number, { mo: string, mau: string, vien: string, anh: string, mauHe: string }> = {
  0: { mo: 'Ngọn lửa trên đuôi không bao giờ tắt. Cận chiến cân bằng, càng đánh càng hăng.', mau: 'from-orange-400/30 to-orange-900/10', vien: 'hover:border-orange-400/60', anh: 'shadow-orange-500/30', mauHe: 'bg-orange-500/15 text-orange-300 ring-orange-400/40' },
  1: { mo: 'Hạt giống trên lưng lớn dần theo từng trận. Lì đòn, trụ vững tuyến đầu.', mau: 'from-lime-400/30 to-lime-900/10', vien: 'hover:border-lime-400/60', anh: 'shadow-lime-500/30', mauHe: 'bg-lime-500/15 text-lime-300 ring-lime-400/40' },
  3: { mo: 'Mai rùa cứng như đá, phun nước cực mạnh. Thủ chắc rồi phản công.', mau: 'from-sky-400/30 to-sky-900/10', vien: 'hover:border-sky-400/60', anh: 'shadow-sky-500/30', mauHe: 'bg-sky-500/15 text-sky-300 ring-sky-400/40' },
  4: { mo: 'Nhỏ mà có võ: tích điện ở má rồi bắn tia sét từ xa.', mau: 'from-yellow-400/30 to-yellow-900/10', vien: 'hover:border-yellow-400/60', anh: 'shadow-yellow-500/30', mauHe: 'bg-yellow-500/15 text-yellow-300 ring-yellow-400/40' },
  2: { mo: `Gen linh hoạt hiếm có. Đến cấp ${CAP_NHANH_EEVEE}, bạn tự chọn 1 trong 8 hướng tiến hoá.`, mau: 'from-violet-400/30 to-violet-900/10', vien: 'hover:border-violet-400/60', anh: 'shadow-violet-500/30', mauHe: 'bg-violet-500/15 text-violet-300 ring-violet-400/40' },
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
    he: loai.he,
    anhSrc: anhPokemon(dex),
    chuoi,
    nhanhEevee: laEevee ? NHANH_EEVEE.map(d => ({ dex: d, ten: tenLoai(d) })) : [],
    ...KIEU_KHOI_DAU[cls]!,
  }
})

const portals = [
  { so: '01 / KHỞI HÀNH', icon: 'i-lucide-download', tieuDe: 'Bước vào thế giới Pokémon', mo: 'Chọn phiên bản dành cho bạn và bắt đầu chuyến phiêu lưu.', link: 'Tải game', to: '/download' },
  { so: '02 / VINH DANH', icon: 'i-lucide-trophy', tieuDe: 'Lưu danh bảng vàng', mo: 'Theo dõi những Pokémon mạnh nhất và chủ nhân của chúng.', link: 'Xem xếp hạng', to: '/ranking' },
  { so: '03 / HỘI NGỘ', icon: 'i-lucide-scroll-text', tieuDe: 'Tin tức vùng đất', mo: 'Khám phá tin tức, sự kiện và những cuộc hẹn cùng bạn bè.', link: 'Đọc tin tức', to: '/news' },
]

const steps = computed(() => [
  { so: '01.', tieuDe: 'Tạo tài khoản', mo: 'Đăng ký tài khoản để bắt đầu hành trình của bạn.', link: 'Đăng ký ngay', to: '/register' },
  { so: '02.', tieuDe: 'Tải phiên bản phù hợp', mo: 'Chọn nền tảng và làm theo hướng dẫn cài đặt.', link: 'Chọn bản tải', to: '/download' },
  { so: '03.', tieuDe: 'Chọn Pokémon khởi đầu', mo: 'Đăng nhập game, chọn Pokémon của bạn và tìm những người bạn đồng hành.', link: 'Cộng đồng & hỗ trợ', to: hoTro.value },
])

const faq = computed(() => [
  { label: 'Tôi cần làm gì sau khi đăng ký?', slot: 'faq1' as const },
  { label: 'Tôi gặp khó khăn khi cài đặt hoặc đăng nhập?', slot: 'faq2' as const },
])
</script>

<template>
  <div>
    <!-- ============ HERO 3D ============ -->
    <section class="relative isolate flex min-h-[calc(100dvh-4rem)] items-center overflow-hidden" aria-labelledby="hero-heading">
      <div class="absolute inset-0 -z-10 bg-[radial-gradient(ellipse_at_70%_40%,#0b645a55,transparent_60%),radial-gradient(ellipse_at_20%_80%,#6c3b1455,transparent_55%)]" />
      <ThreeHeroScene class="-z-10" />
      <!-- Lớp tối mờ sau phần chữ: cảnh 3D sáng màu không làm chìm tiêu đề và đoạn mô tả -->
      <div class="pointer-events-none absolute inset-0 -z-10 bg-[linear-gradient(90deg,var(--ui-bg)_0%,color-mix(in_oklab,var(--ui-bg)_55%,transparent)_40%,transparent_70%)] max-lg:bg-[linear-gradient(180deg,transparent_0%,color-mix(in_oklab,var(--ui-bg)_70%,transparent)_35%,color-mix(in_oklab,var(--ui-bg)_70%,transparent)_80%,transparent_100%)] opacity-80" />
      <div class="pointer-events-none absolute inset-x-0 bottom-0 -z-10 h-40 bg-gradient-to-t from-[var(--ui-bg)] to-transparent" />

      <UContainer class="max-w-7xl">
        <div class="max-w-2xl py-20 xl:max-w-3xl">
          <FxReveal>
            <p class="eyebrow">
              {{ tenSite }} · MMORPG
            </p>
          </FxReveal>
          <FxReveal :delay="0.1">
            <h1 id="hero-heading" class="section-title mt-5 text-5xl text-highlighted drop-shadow-[0_4px_30px_rgba(0,0,0,.6)] sm:text-6xl lg:text-7xl">
              Bạn không huấn luyện <span class="text-jade-300">Pokémon.</span><br>
              <em class="text-gilded">Bạn chính là Pokémon.</em>
            </h1>
          </FxReveal>
          <FxReveal :delay="0.2">
            <p class="mt-6 max-w-xl text-lg text-toned">
              Chọn một Pokémon khởi đầu, lên cấp, tiến hoá và phiêu lưu thời gian thực cùng bạn bè: săn Pokémon hoang dã, hạ boss huyền thoại, lập bang hội và tranh tài PvP.
            </p>
          </FxReveal>
          <FxReveal :delay="0.3">
            <div class="mt-9 flex flex-wrap gap-4">
              <UButton to="/download" size="xl" trailing-icon="i-lucide-arrow-up-right" class="shadow-[0_10px_40px_rgba(249,192,42,.35)]">
                Tải game ngay
              </UButton>
              <ClientOnly>
                <UButton v-if="auth.loggedIn.value" to="/account" size="xl" color="neutral" variant="outline" class="backdrop-blur">
                  Tài khoản của bạn
                </UButton>
                <UButton v-else to="/register" size="xl" color="neutral" variant="outline" class="backdrop-blur">
                  Tạo tài khoản
                </UButton>
                <template #fallback>
                  <UButton to="/register" size="xl" color="neutral" variant="outline" class="backdrop-blur">
                    Tạo tài khoản
                  </UButton>
                </template>
              </ClientOnly>
            </div>
          </FxReveal>
          <FxReveal :delay="0.4">
            <div class="mt-10 flex items-center gap-4 text-xs tracking-[0.3em] text-muted">
              <span class="h-px w-12 bg-gradient-to-r from-primary to-transparent" /> ✦ NHẬP VAI POKÉMON / KẾT NỐI BẠN BÈ
            </div>
          </FxReveal>
        </div>
      </UContainer>

      <a href="#classes" class="absolute bottom-8 left-1/2 -translate-x-1/2 text-muted hover:text-primary" aria-label="Cuộn xuống">
        <UIcon name="i-lucide-chevrons-down" class="size-7 animate-bounce" />
      </a>
    </section>

    <!-- ============ DẢI SỐ LIỆU ============ -->
    <UContainer class="relative z-10 -mt-10 max-w-6xl">
      <FxReveal>
        <div class="grid divide-y divide-default/60 overflow-hidden rounded-2xl glass ring-gilded sm:grid-cols-3 sm:divide-x sm:divide-y-0">
          <div v-for="s in stats" :key="s.so" class="flex items-center gap-5 p-6">
            <b class="font-serif text-4xl text-gilded">{{ s.so }}</b>
            <span>
              <strong class="block text-highlighted">{{ s.tieuDe }}</strong>
              <span class="text-sm text-muted">{{ s.mo }}</span>
            </span>
          </div>
        </div>
      </FxReveal>
    </UContainer>

    <!-- ============ 5 POKÉMON KHỞI ĐẦU ============ -->
    <section id="classes" class="scroll-mt-20 py-24" aria-labelledby="classes-title">
      <UContainer class="max-w-7xl">
        <FxReveal class="text-center">
          <p class="eyebrow justify-center">
            Chọn người bạn của chính bạn
          </p>
          <h2 id="classes-title" class="section-title mt-3 text-4xl text-highlighted sm:text-5xl">
            Năm Pokémon khởi đầu
          </h2>
          <p class="mx-auto mt-4 max-w-2xl text-muted">
            Mỗi Pokémon một hệ, một lối đánh và một con đường tiến hoá. Bạn sẽ bắt đầu chuyến phiêu lưu trong hình hài nào?
          </p>
        </FxReveal>
        <div class="mt-14 grid gap-5 sm:grid-cols-2 lg:grid-cols-5">
          <FxReveal v-for="(c, i) in khoiDau" :key="c.dex" :delay="i * 0.08">
            <FxTiltCard :max="14" as="article" class="group h-full rounded-2xl">
              <div
                class="relative flex h-full flex-col overflow-hidden rounded-2xl border border-default/70 bg-gradient-to-br p-6 transition-colors duration-300"
                :class="[c.mau, c.vien]"
              >
                <small class="text-[0.7rem] tracking-[0.2em] text-dimmed">#{{ String(c.dex).padStart(4, '0') }} / KHỞI ĐẦU</small>
                <span
                  class="mt-6 grid size-24 place-items-center rounded-full border border-white/10 bg-white/5 shadow-2xl transition-transform duration-500 [transform:translateZ(60px)] group-hover:scale-110"
                  :class="c.anh"
                >
                  <img :src="c.anhSrc" width="40" height="40" :alt="`Chân dung ${c.ten}`" class="pixelated size-20 rounded-full">
                </span>
                <h3 class="mt-6 font-serif text-2xl font-bold text-highlighted [transform:translateZ(40px)]">
                  {{ c.ten }}
                </h3>
                <div class="mt-2 flex flex-wrap gap-1.5 [transform:translateZ(30px)]">
                  <span v-for="h in c.he" :key="h" class="rounded-full px-2 py-0.5 text-xs font-semibold ring-1" :class="c.mauHe">{{ h }}</span>
                </div>
                <p class="mt-3 flex-1 text-sm text-muted [transform:translateZ(25px)]">
                  {{ c.mo }}
                </p>
                <!-- Chuỗi tiến hoá -->
                <div class="mt-5 border-t border-white/10 pt-4">
                  <p class="text-[0.65rem] tracking-[0.2em] text-dimmed">
                    TIẾN HOÁ
                  </p>
                  <ol v-if="!c.nhanhEevee.length" class="mt-2 flex flex-wrap items-center gap-x-1 gap-y-2 text-xs">
                    <li v-for="(t, j) in c.chuoi" :key="t.dex" class="inline-flex items-center gap-1">
                      <UIcon v-if="j > 0" name="i-lucide-chevron-right" class="size-3 text-dimmed" aria-hidden="true" />
                      <img :src="anhPokemon(t.dex)" width="40" height="40" alt="" class="pixelated size-6 rounded">
                      <span class="text-toned">{{ t.ten }}</span>
                      <span v-if="t.cap" class="text-dimmed">({{ t.cap }})</span>
                    </li>
                  </ol>
                  <template v-else>
                    <p class="mt-2 text-xs text-toned">
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
            </FxTiltCard>
          </FxReveal>
        </div>
      </UContainer>
    </section>

    <!-- ============ THẾ GIỚI ============ -->
    <section class="relative py-24" aria-labelledby="explore-title">
      <FxGlowOrbs />
      <UContainer class="relative max-w-7xl">
        <FxReveal class="flex items-end justify-between gap-6">
          <div>
            <p class="eyebrow">
              Thế giới của bạn
            </p>
            <h2 id="explore-title" class="section-title mt-3 text-4xl text-highlighted sm:text-5xl">
              Thế giới Pokémon không chỉ có chiến đấu.
            </h2>
          </div>
        </FxReveal>
        <div class="mt-12 grid gap-6 md:grid-cols-3">
          <FxReveal v-for="(p, i) in portals" :key="p.to" :delay="i * 0.1">
            <FxTiltCard :max="8" class="h-full rounded-2xl">
              <NuxtLink :to="p.to" class="group flex h-full flex-col rounded-2xl glass ring-gilded p-8 transition-shadow hover:shadow-[0_20px_60px_rgba(249,192,42,.15)]">
                <span class="text-xs tracking-[0.2em] text-dimmed">{{ p.so }}</span>
                <UIcon :name="p.icon" class="mt-6 size-10 text-primary [transform:translateZ(50px)]" />
                <h3 class="mt-6 font-serif text-2xl font-bold text-highlighted">{{ p.tieuDe }}</h3>
                <p class="mt-2 flex-1 text-muted">{{ p.mo }}</p>
                <span class="mt-6 inline-flex items-center gap-2 border-t border-default/60 pt-4 font-semibold text-primary">
                  {{ p.link }} <UIcon name="i-lucide-arrow-up-right" class="size-4 transition-transform group-hover:translate-x-1 group-hover:-translate-y-1" />
                </span>
              </NuxtLink>
            </FxTiltCard>
          </FxReveal>
        </div>
      </UContainer>
    </section>

    <!-- ============ HÀNH TRÌNH + FAQ ============ -->
    <section id="guide" class="scroll-mt-20 pb-28" aria-labelledby="guide-title">
      <UContainer class="max-w-6xl">
        <FxReveal>
          <div class="relative overflow-hidden rounded-3xl glass ring-gilded p-8 sm:p-12">
            <div class="pointer-events-none absolute -right-24 -top-24 size-80 rounded-full bg-gold-400/10 blur-3xl" aria-hidden="true" />
            <p class="eyebrow">
              Hẹn gặp bạn trong thế giới Pokémon
            </p>
            <h2 id="guide-title" class="section-title mt-3 text-4xl text-highlighted">
              Hành trình bắt đầu từ đây.
            </h2>
            <div class="mt-10 grid gap-8 md:grid-cols-3">
              <div v-for="s in steps" :key="s.so" class="relative">
                <b class="font-serif text-5xl text-primary/30">{{ s.so }}</b>
                <h3 class="mt-2 text-lg font-bold text-highlighted">{{ s.tieuDe }}</h3>
                <p class="mt-1 text-muted">{{ s.mo }}</p>
                <ULink :to="s.to" class="mt-3 inline-flex items-center gap-1 font-semibold text-primary hover:underline">
                  {{ s.link }} <UIcon name="i-lucide-arrow-up-right" class="size-4" />
                </ULink>
              </div>
            </div>
            <UAccordion :items="faq" class="mt-12" :ui="{ trigger: 'text-base font-semibold' }">
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
