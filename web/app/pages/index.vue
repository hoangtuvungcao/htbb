<script setup lang="ts">
useSeoMeta({
  title: 'Trang chủ',
  description: 'MMORPG nơi chính bạn là một Pokémon: chọn 1 trong 5 Pokémon khởi đầu, lên cấp, tiến hoá và phiêu lưu cùng bạn bè. Tải game và đăng ký tài khoản.',
})

const auth = useAuth()
onMounted(() => auth.hydrate())
const { data: site } = await useSite()
const hoTro = computed(() => linkHoTro(site.value))
const tenSite = useAppConfig().htbb.tenSite

const stats = [
  { so: '05', tieuDe: 'Pokémon khởi đầu', mo: 'Chính bạn là Pokémon đó', to: '/huong-dan#khoi-dau' },
  { so: String(POKEDEX.he.length), tieuDe: 'Hệ khắc chế', mo: 'Biết hệ là thắng', to: '/cam-nang/he-khac-che' },
  { so: String(POKEDEX.loai.length), tieuDe: 'Loài trong Pokédex', mo: 'Săn, sưu tầm, săn Shiny', to: '/pokedex' },
  { so: String(CAM_NANG.suKien.length), tieuDe: 'Tuần sự kiện / năm', mo: 'Tuần nào cũng có chuyện vui', to: '/cam-nang/su-kien' },
]

// Trạng thái máy chủ: chỉ gọi phía trình duyệt (không làm chậm SSR khi CSDL chậm).
const { data: trangThai, status: ttStatus } = useFetch<{ song: boolean | null, online: number | null }>('/api/trang-thai', { server: false, lazy: true, key: 'trang-thai' })

// Sự kiện tuần này + 2 tuần tới (tính theo ngày trên máy người xem)
const suKien = ref<SuKienTuan[]>([])
const lich = useLichSuKien()
onMounted(() => { suKien.value = suKienTuTuan(tuanTrongNam(new Date()), 2, lich.value) })
watch(lich, l => { suKien.value = suKienTuTuan(tuanTrongNam(new Date()), 2, l) })

// Nội dung sửa được trong khu quản trị (CSDL), mặc định = bản cũ.
const cms = useCongKhai()
const tc = computed(() => cms.value.trangChu)
const { data: dsTin } = await useFetch<TinTomTatWeb[]>('/api/tin-tuc', { key: 'htbb-tin', default: () => [] })
const tinMoi = computed(() => (dsTin.value ?? []).slice(0, 3))
const bannerHien = computed(() => tc.value.banner.filter(b => b.hien && (b.anh || b.tieuDe)))
const iBanner = ref(0)
let hBanner: ReturnType<typeof setInterval> | undefined
onMounted(() => { hBanner = setInterval(() => { if (bannerHien.value.length > 1) iBanner.value = (iBanner.value + 1) % bannerHien.value.length }, 6000) })
onBeforeUnmount(() => clearInterval(hBanner))


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

const khoiDauTatCa = [0, 1, 2, 3, 4].map((cls) => {
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
const khoiDau = computed(() => {
  const thuTu = tc.value.khoiDauThuTu.filter(c => c >= 0 && c <= 4)
  return (thuTu.length ? thuTu : [0, 1, 3, 4, 2]).map(c => khoiDauTatCa[c]!).filter(Boolean)
})


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
                {{ tc.hero.nhan || `${tenSite} · MMORPG` }}
              </p>
            </FxReveal>
            <FxReveal :delay="0.1">
              <h1 id="hero-heading" class="section-title mt-5 text-[2.6rem] leading-[1.08] text-highlighted sm:text-6xl lg:text-[4.2rem]">
                {{ tc.hero.dong1 }}<br>
                <span class="text-gilded">{{ tc.hero.dong2 }}</span>
              </h1>
            </FxReveal>
            <FxReveal :delay="0.2">
              <div class="dialog-box mt-7 max-w-xl">
                <p class="text-base sm:text-lg">
                  {{ tc.hero.moTa }}
                </p>
              </div>
            </FxReveal>
            <FxReveal :delay="0.3">
              <div class="mt-8 flex flex-wrap gap-3">
                <UButton
                  v-for="(n, k) in tc.hero.nut"
                  :key="k"
                  :to="lienKetAnToan(n.to) || '/'"
                  size="xl"
                  :color="n.kieu === 'chinh' ? 'primary' : 'warning'"
                  :class="n.kieu === 'chinh' ? 'btn-game px-6' : 'btn-pikachu px-6'"
                  :trailing-icon="n.kieu === 'chinh' ? 'i-lucide-arrow-right' : undefined"
                >
                  {{ n.label }}
                </UButton>
                <ClientOnly v-if="!tc.hero.nut.length">
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
            <FxReveal :delay="0.35">
              <ClientOnly>
                <p class="mt-6 inline-flex items-center gap-2 rounded-full bg-white/80 px-3 py-1.5 text-sm font-semibold text-navy-800 shadow-sm dark:bg-navy-950/70 dark:text-navy-100">
                  <span class="relative flex size-2.5">
                    <span v-if="trangThai?.song" class="absolute inline-flex size-full animate-ping rounded-full bg-grass-400 opacity-75" />
                    <span class="relative inline-flex size-2.5 rounded-full" :class="trangThai?.song ? 'bg-grass-500' : trangThai?.song === false ? 'bg-pokered-500' : 'bg-navy-400'" />
                  </span>
                  <template v-if="trangThai?.song">
                    Máy chủ đang mở · {{ trangThai.online }} Pokémon online
                  </template>
                  <template v-else-if="trangThai?.song === false">
                    Máy chủ đang bảo trì
                  </template>
                  <template v-else-if="ttStatus === 'pending' || ttStatus === 'idle'">
                    Đang kiểm tra máy chủ…
                  </template>
                  <template v-else>
                    Chưa rõ trạng thái máy chủ — xem kênh hỗ trợ
                  </template>
                </p>
              </ClientOnly>
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
          <div class="grid grid-cols-2 overflow-hidden rounded-2xl glass ring-gilded lg:grid-cols-4">
            <NuxtLink v-for="s in stats" :key="s.tieuDe" :to="s.to" class="flex items-center gap-3 border-[var(--ui-border)] p-4 transition-colors hover:bg-elevated/60 sm:gap-4 sm:p-5 [&:nth-child(-n+2)]:border-b-2 lg:[&:nth-child(-n+2)]:border-b-0 [&:not(:last-child)]:border-r-2 max-lg:[&:nth-child(2)]:border-r-0">
              <b class="grid size-12 shrink-0 place-items-center rounded-xl bg-screen font-pixel text-sm text-pokered-600 sm:size-14 sm:text-base dark:text-pikachu-300">{{ s.so }}</b>
              <span class="min-w-0">
                <strong class="block font-display text-base leading-tight text-highlighted sm:text-lg">{{ s.tieuDe }}</strong>
                <span class="text-xs text-muted sm:text-sm">{{ s.mo }}</span>
              </span>
            </NuxtLink>
          </div>
        </FxReveal>
      </UContainer>
      <div class="h-10" />
    </div>

    <!-- ============ CẢNH PHIM + TÍNH NĂNG ============ -->
    <section class="pt-16 sm:pt-20" aria-labelledby="tinh-nang-title">
      <UContainer class="max-w-7xl">
        <div v-if="bannerHien.length" class="relative mb-10 overflow-hidden rounded-3xl ring-gilded" aria-roledescription="carousel">
          <NuxtLink
            v-for="(b, k) in bannerHien"
            v-show="k === iBanner"
            :key="k"
            :to="lienKetAnToan(b.to) || '/'"
            class="group relative block aspect-[21/7] min-h-44 bg-navy-900"
            :aria-label="b.tieuDe"
          >
            <img v-if="b.anh" :src="lienKetAnToan(b.anh)" alt="" class="absolute inset-0 size-full object-cover transition-transform duration-700 group-hover:scale-105">
            <span class="absolute inset-0 bg-gradient-to-r from-navy-950/80 via-navy-950/30 to-transparent" />
            <span class="absolute inset-y-0 left-0 flex max-w-xl flex-col justify-center p-6 sm:p-10">
              <span class="font-display text-2xl font-extrabold text-white sm:text-4xl">{{ b.tieuDe }}</span>
              <span v-if="b.moTa" class="mt-2 text-sm text-white/85 sm:text-base">{{ b.moTa }}</span>
            </span>
          </NuxtLink>
          <div v-if="bannerHien.length > 1" class="absolute bottom-3 right-4 flex gap-1.5">
            <button v-for="(b, k) in bannerHien" :key="k" type="button" class="size-2.5 rounded-full" :class="k === iBanner ? 'bg-white' : 'bg-white/45'" :aria-label="`Banner ${k + 1}`" @click="iBanner = k" />
          </div>
        </div>
        <FxReveal v-if="tc.hienCanhPhim">
          <SiteCanhPhim />
        </FxReveal>
        <FxReveal class="mt-14 text-center">
          <p class="eyebrow">
            Có gì trong game
          </p>
          <h2 id="tinh-nang-title" class="section-title mt-4 text-4xl text-highlighted sm:text-5xl">
            {{ tc.tinhNangTieuDe }}
          </h2>
        </FxReveal>
        <ul class="mt-10 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <li v-for="(f, i) in tc.tinhNang" :key="f.t">
            <FxReveal :delay="(i % 3) * 0.08" class="h-full">
              <NuxtLink :to="lienKetAnToan(f.to) || '/'" class="group flex h-full gap-4 rounded-3xl glass p-5 transition hover:-translate-y-1">
                <span class="grid size-12 shrink-0 place-items-center rounded-2xl bg-pikachu-400 text-navy-950 ring-2 ring-[color-mix(in_oklab,var(--poke-outline)_70%,transparent)]"><UIcon :name="f.icon" class="size-6" /></span>
                <span>
                  <span class="block font-display text-xl font-bold text-highlighted group-hover:text-primary">{{ f.t }}</span>
                  <span class="mt-1 block text-sm text-muted">{{ f.m }}</span>
                </span>
              </NuxtLink>
            </FxReveal>
          </li>
        </ul>
      </UContainer>
    </section>

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
                <div class="relative">
                  <PokeSan :dex="c.dex" :he="c.he" :size="150" :delay="i * 0.3" :label="c.ten" class="w-full" />
                  <span class="absolute left-2 top-2 rounded-full bg-white/80 px-1.5 py-1 font-pixel text-[9px] leading-none text-navy-800 dark:bg-navy-950/70 dark:text-navy-100">#{{ String(c.dex).padStart(3, '0') }}</span>
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
                      <PokeAvatar :dex="t.dex" class="size-6" />
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
                        <PokeAvatar :dex="t.dex" :alt="t.ten" :title="t.ten" class="size-8" />
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

    <!-- ============ SỰ KIỆN + TIN MỚI ============ -->
    <section class="relative isolate py-20" aria-labelledby="su-kien-title">
      <div class="absolute inset-0 -z-10 bg-pokeblue-50 dark:bg-navy-900/60" aria-hidden="true" />
      <FxGlowOrbs />
      <UContainer class="relative max-w-7xl">
        <div class="grid gap-10 lg:grid-cols-[1.1fr_1fr]">
          <div>
            <FxReveal>
              <p class="eyebrow">
                Lịch tuần này
              </p>
              <h2 id="su-kien-title" class="section-title mt-4 text-4xl text-highlighted">
                Sự kiện đang diễn ra
              </h2>
            </FxReveal>
            <ClientOnly>
              <ol class="mt-8 space-y-3">
                <li v-for="(e, i) in suKien" :key="e.tuan">
                  <NuxtLink to="/cam-nang/su-kien" class="group relative flex gap-4 overflow-hidden rounded-2xl glass p-4 pl-5 transition hover:-translate-y-0.5" :class="i === 0 ? 'ring-gilded' : ''">
                    <span class="absolute inset-y-0 left-0 w-1.5" :style="{ background: LOAI_SU_KIEN[e.loai]?.mau }" />
                    <span class="grid size-11 shrink-0 place-items-center rounded-xl text-white" :style="{ background: LOAI_SU_KIEN[e.loai]?.mau }"><UIcon :name="LOAI_SU_KIEN[e.loai]?.icon ?? 'i-lucide-calendar'" class="size-5" /></span>
                    <span class="min-w-0">
                      <span class="text-xs font-bold uppercase tracking-wide" :class="i === 0 ? 'text-primary' : 'text-dimmed'">{{ i === 0 ? 'Tuần này' : i === 1 ? 'Tuần sau' : 'Sắp tới' }} · {{ LOAI_SU_KIEN[e.loai]?.ten }}</span>
                      <span class="block font-display text-lg font-bold text-highlighted group-hover:text-primary">{{ e.ten1 }}</span>
                      <span class="block text-sm text-muted">{{ e.coChe }}</span>
                    </span>
                  </NuxtLink>
                </li>
              </ol>
              <template #fallback>
                <USkeleton class="mt-8 h-64 w-full rounded-2xl" />
              </template>
            </ClientOnly>
            <UButton to="/cam-nang/su-kien" color="neutral" variant="ghost" trailing-icon="i-lucide-arrow-right" class="mt-4">
              Lịch sự kiện cả năm
            </UButton>
          </div>
          <div>
            <FxReveal>
              <p class="eyebrow">
                Tin mới
              </p>
              <h2 class="section-title mt-4 text-4xl text-highlighted">
                Bảng tin vùng đất
              </h2>
            </FxReveal>
            <ul class="mt-8 space-y-3">
              <li v-for="a in tinMoi" :key="a.slug">
                <NuxtLink :to="`/news/${a.slug}`" class="group flex gap-4 rounded-2xl glass p-4 transition hover:-translate-y-0.5">
                  <img v-if="a.anhBia" :src="a.anhBia" alt="" loading="lazy" class="hidden size-20 shrink-0 rounded-xl object-cover sm:block">
                  <span class="min-w-0">
                    <span class="flex items-center gap-2 text-xs font-bold text-dimmed">
                      <UBadge color="primary" variant="subtle" size="sm">{{ a.loai }}</UBadge>
                      <UBadge v-if="a.ghim" color="warning" variant="subtle" size="sm" icon="i-lucide-pin">Ghim</UBadge> {{ a.ngay }}
                    </span>
                    <span class="mt-1.5 block font-display text-lg font-bold text-highlighted group-hover:text-primary">{{ a.tieuDe }}</span>
                    <span class="mt-1 line-clamp-2 block text-sm text-muted">{{ a.tomTat }}</span>
                  </span>
                </NuxtLink>
              </li>
            </ul>
            <UButton to="/news" color="neutral" variant="ghost" trailing-icon="i-lucide-arrow-right" class="mt-4">
              Tất cả tin tức
            </UButton>
          </div>
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
