<script setup lang="ts">
/**
 * Trang chủ kiểu cổng game: key art (chung màn đăng nhập trong game) + nút Tải game / Đăng ký,
 * bảng Tin tức + Sự kiện (lịch năm từ CSDL), ảnh chụp thật trong game, chọn Pokémon khởi đầu, dải tải game.
 * Mọi hình là tài nguyên thật của game (key art, ảnh chụp, sprite / chân dung PMDCollab, icon vật phẩm).
 */
useSeoMeta({
  title: 'Trang chủ',
  description: 'MMORPG nơi chính bạn là một Pokémon: chọn 1 trong 5 Pokémon khởi đầu, lên cấp, tiến hoá và phiêu lưu cùng bạn bè. Tải game và đăng ký tài khoản.',
})

const gocSite = useGocSite()
const tenGame = useAppConfig().htbb.tenSite
const { ds: dsBanTai } = useBanTai()
useHead({
  script: [{
    type: 'application/ld+json',
    key: 'ld-game',
    innerHTML: () => JSON.stringify({
      '@context': 'https://schema.org',
      '@type': 'VideoGame',
      'name': tenGame,
      'url': `${gocSite.value}/`,
      'inLanguage': 'vi',
      'genre': ['MMORPG', 'Nhập vai'],
      'gamePlatform': dsBanTai.value.filter(b => !b.sapRaMat).map(b => b.ten),
      'playMode': 'MultiPlayer',
      'isAccessibleForFree': true,
      'description': 'MMORPG nơi chính bạn là một Pokémon: chọn 1 trong 5 Pokémon khởi đầu, lên cấp, tiến hoá và phiêu lưu cùng bạn bè.',
    }),
  }],
})

const auth = useAuth()
onMounted(() => auth.hydrate())
const tenSite = useAppConfig().htbb.tenSite
const cms = useCongKhai()
const tc = computed(() => cms.value.trangChu)

// Trạng thái máy chủ: chỉ gọi phía trình duyệt (không làm chậm SSR khi CSDL chậm). Chưa rõ thì không hiện gì.
const { data: trangThai } = useFetch<{ song: boolean | null, online: number | null }>('/api/trang-thai', { server: false, lazy: true, key: 'trang-thai', timeout: 5000 })

// ---- Tin tức ----
const { data: dsTin } = await useFetch<TinTomTatWeb[]>('/api/tin-tuc', { key: 'htbb-tin', default: () => [], timeout: 4000 })
const tinMoi = computed(() => (dsTin.value ?? []).slice(0, 5))

// ---- Sự kiện: lịch năm thật (đang diễn ra + 30 ngày tới); chưa có lịch năm thì lịch 52 tuần ----
const skNam = useSuKienNam(30)
const skHien = computed(() => [
  ...skNam.dang.value.map(l => ({ l, dang: true })),
  ...skNam.sap.value.map(l => ({ l, dang: false })),
].slice(0, 4))
const suKien = ref<SuKienTuan[]>([])
const lich = useLichSuKien()
onMounted(() => { suKien.value = suKienTuTuan(tuanTrongNam(new Date()), 2, lich.value) })
watch(lich, l => { suKien.value = suKienTuTuan(tuanTrongNam(new Date()), 2, l) })

// ---- Banner CMS (tuỳ chọn) ----
const bannerHien = computed(() => tc.value.banner.filter(b => b.hien && (b.anh || b.tieuDe)))
const iBanner = ref(0)
let hBanner: ReturnType<typeof setInterval> | undefined
onMounted(() => { hBanner = setInterval(() => { if (bannerHien.value.length > 1) iBanner.value = (iBanner.value + 1) % bannerHien.value.length }, 6000) })
onBeforeUnmount(() => clearInterval(hBanner))

// ---- 5 Pokémon khởi đầu (cột tob_char.class 0..4, shared/utils/pokemon.ts) ----
const KIEU_KHOI_DAU: Record<number, { mo: string, loi: string }> = {
  0: { loi: 'Cận chiến cân bằng', mo: 'Ngọn lửa trên đuôi không bao giờ tắt. Càng đánh càng hăng.' },
  1: { loi: 'Lì đòn, trụ tuyến đầu', mo: 'Hạt giống trên lưng lớn dần theo từng trận.' },
  3: { loi: 'Thủ chắc, phản công', mo: 'Mai rùa cứng như đá, phun nước cực mạnh.' },
  4: { loi: 'Bắn xa', mo: 'Nhỏ mà có võ: tích điện ở má rồi phóng tia sét.' },
  2: { loi: 'Linh hoạt', mo: `Đến cấp ${CAP_NHANH_EEVEE}, bạn tự chọn 1 trong 8 hướng tiến hoá.` },
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
    chuoi,
    nhanhEevee: laEevee ? NHANH_EEVEE.map(d => ({ dex: d, ten: tenLoai(d) })) : [],
    ...KIEU_KHOI_DAU[cls]!,
  }
})
const khoiDau = computed(() => {
  const thuTu = tc.value.khoiDauThuTu.filter(c => c >= 0 && c <= 4)
  return (thuTu.length ? thuTu : [0, 1, 3, 4, 2]).map(c => khoiDauTatCa[c]!).filter(Boolean)
})
const iChon = ref(0)
const chon = computed(() => khoiDau.value[iChon.value] ?? khoiDau.value[0]!)

// ---- Ảnh trong game ----
const BO_SUU_TAP = ['the-gioi', 'chien-dau', 'thu-cuoi', 'tu-dong', 'tien-hoa', 'doi'] as const
const LINK_ANH: Record<string, string> = {
  'the-gioi': '/cam-nang/ban-do', 'chien-dau': '/cam-nang/he-khac-che', 'thu-cuoi': '/cam-nang/thu-cuoi-pet',
  'tu-dong': '/cam-nang/tu-dong', 'tien-hoa': '/cam-nang/tien-hoa', 'doi': '/cam-nang/boss-dot-kich',
}
</script>

<template>
  <div>
    <!-- ============ HERO: key art của game ============ -->
    <section class="relative isolate flex min-h-[max(38rem,calc(100svh-4rem))] overflow-hidden" aria-labelledby="hero-heading">
      <SiteKeyArt class="-z-10" />
      <div class="pointer-events-none absolute inset-x-0 top-0 -z-10 h-2/3 bg-[linear-gradient(180deg,rgb(13_10_31/.7),transparent)]" aria-hidden="true" />
      <UContainer class="w-full max-w-4xl">
        <div class="flex flex-col items-center pb-[clamp(11rem,30svh,21rem)] pt-6 text-center sm:pt-10">
          <img src="/logo.svg" :alt="tenSite" width="836" height="506" class="hero-logo w-[min(28rem,80vw,42svh)]" fetchpriority="high">
          <h1 id="hero-heading" class="mt-3 font-display text-2xl font-extrabold leading-tight text-white [text-shadow:0_3px_0_#06040e] sm:text-4xl">
            {{ tc.hero.dong1 }}
            <span class="block text-[#ffcb05]">{{ tc.hero.dong2 }}</span>
          </h1>
          <div class="mt-6 flex flex-wrap justify-center gap-3">
            <template v-if="tc.hero.nut.length">
              <UButton
                v-for="(n, k) in tc.hero.nut"
                :key="k"
                :to="lienKetAnToan(n.to) || '/'"
                size="xl"
                :class="n.kieu === 'chinh' ? 'btn-pikachu px-7 text-lg uppercase' : 'btn-game px-7 text-lg uppercase'"
              >
                {{ n.label }}
              </UButton>
            </template>
            <template v-else>
              <UButton to="/download" size="xl" class="btn-pikachu px-7 text-lg uppercase">
                Tải game
              </UButton>
              <UButton to="/register" size="xl" class="btn-game px-7 text-lg uppercase">
                Đăng ký
              </UButton>
            </template>
          </div>
          <ClientOnly>
            <p v-if="trangThai && trangThai.song !== null" class="mt-5 inline-flex items-center gap-2 rounded-md border-2 border-[#06040e] bg-[#1b1540]/90 px-3 py-1.5 text-sm font-bold text-white">
              <span class="size-2.5 rounded-full" :class="trangThai.song ? 'bg-grass-400' : 'bg-pokered-400'" aria-hidden="true" />
              <template v-if="trangThai.song">
                Máy chủ Online · {{ (trangThai.online ?? 0).toLocaleString('vi-VN') }} người đang chơi
              </template>
              <template v-else>
                Máy chủ đang bảo trì
              </template>
            </p>
          </ClientOnly>
        </div>
      </UContainer>
    </section>

    <!-- ============ TIN TỨC + SỰ KIỆN ============ -->
    <section class="relative z-10 -mt-10 sm:-mt-14" aria-label="Tin tức và sự kiện">
      <UContainer class="max-w-7xl">
        <div class="grid gap-5 lg:grid-cols-[1.25fr_1fr]">
          <div class="khung overflow-hidden">
            <h2 class="khung-dau justify-between">
              <span class="inline-flex items-center gap-2"><img src="/vat-pham/123.png" width="16" height="16" alt="" class="pixelated size-5">Tin tức</span>
              <NuxtLink to="/news" class="text-xs font-bold normal-case tracking-normal text-white/90 hover:text-white hover:underline">Xem tất cả</NuxtLink>
            </h2>
            <ul v-if="tinMoi.length" class="divide-y divide-[var(--ui-border-muted)]">
              <li v-for="a in tinMoi" :key="a.slug">
                <NuxtLink :to="`/news/${a.slug}`" class="group flex items-center gap-3 px-4 py-3 hover:bg-elevated/60">
                  <span class="w-[5.75rem] shrink-0 whitespace-nowrap rounded bg-pokered-600 px-1 py-0.5 text-center text-[10px] font-extrabold uppercase text-white">{{ a.loai }}</span>
                  <span class="min-w-0 flex-1 truncate font-semibold text-highlighted group-hover:text-primary">
                    <UIcon v-if="a.ghim" name="i-lucide-pin" class="mr-1 inline size-3.5 align-[-2px] text-primary" aria-label="Ghim" />{{ a.tieuDe }}
                  </span>
                  <time class="hidden shrink-0 text-xs font-semibold tabular-nums text-dimmed sm:block">{{ a.ngay }}</time>
                </NuxtLink>
              </li>
            </ul>
            <p v-else class="px-4 py-8 text-center text-sm text-muted">
              Chưa có tin mới. Theo dõi kênh hỗ trợ để nhận thông báo.
            </p>
          </div>

          <div class="khung overflow-hidden">
            <h2 class="khung-dau justify-between">
              <span class="inline-flex items-center gap-2"><img src="/tien/su_kien_32.png" width="32" height="32" alt="" class="pixelated size-5">Sự kiện</span>
              <NuxtLink to="/cam-nang/su-kien" class="text-xs font-bold normal-case tracking-normal text-white/90 hover:text-white hover:underline">Lịch cả năm</NuxtLink>
            </h2>
            <ClientOnly>
              <ul v-if="skHien.length" class="divide-y divide-[var(--ui-border-muted)]">
                <li v-for="e in skHien" :key="`${e.l.ma}-${e.l.nam}-${e.l.lan}`">
                  <NuxtLink to="/cam-nang/su-kien" class="group block px-4 py-3 hover:bg-elevated/60">
                    <span class="flex items-center justify-between gap-2">
                      <span class="truncate font-semibold text-highlighted group-hover:text-primary">{{ e.l.ten }}</span>
                      <span class="shrink-0 rounded px-1.5 py-0.5 text-[11px] font-extrabold uppercase" :class="e.dang ? 'bg-grass-600 text-white' : 'bg-elevated text-toned'">
                        {{ e.dang ? 'Đang diễn ra' : 'Sắp tới' }}
                      </span>
                    </span>
                    <span class="mt-0.5 block text-xs text-muted">
                      {{ ngayNgan(msTuChuoi(e.l.mo)) }} – {{ ngayNgan(msTuChuoi(e.l.dong) - 60_000) }}
                      · {{ e.dang ? `còn ${demNguoc(msTuChuoi(e.l.dong) - skNam.bay.value)}` : `sau ${demNguoc(msTuChuoi(e.l.mo) - skNam.bay.value)}` }}
                    </span>
                  </NuxtLink>
                </li>
              </ul>
              <ul v-else-if="suKien.length" class="divide-y divide-[var(--ui-border-muted)]">
                <li v-for="(e, i) in suKien" :key="e.tuan">
                  <NuxtLink to="/cam-nang/su-kien" class="group block px-4 py-3 hover:bg-elevated/60">
                    <span class="flex items-center justify-between gap-2">
                      <span class="truncate font-semibold text-highlighted group-hover:text-primary">{{ e.ten1 }}<template v-if="e.phan"> ({{ e.phan }})</template></span>
                      <span class="shrink-0 rounded px-1.5 py-0.5 text-[11px] font-extrabold uppercase" :class="i === 0 ? 'bg-grass-600 text-white' : 'bg-elevated text-toned'">
                        {{ i === 0 ? 'Tuần này' : i === 1 ? 'Tuần sau' : 'Sắp tới' }}
                      </span>
                    </span>
                    <span class="mt-0.5 block truncate text-xs text-muted">{{ e.coChe }}</span>
                  </NuxtLink>
                </li>
              </ul>
              <p v-else class="px-4 py-8 text-center text-sm text-muted">
                Lịch sự kiện đang cập nhật.
              </p>
              <template #fallback>
                <div class="space-y-3 p-4">
                  <USkeleton v-for="k in 3" :key="k" class="h-10 w-full" />
                </div>
              </template>
            </ClientOnly>
          </div>
        </div>
      </UContainer>
    </section>

    <!-- ============ BANNER CMS (nếu có) ============ -->
    <UContainer v-if="bannerHien.length" class="mt-10 max-w-7xl">
      <div class="khung relative overflow-hidden" aria-roledescription="carousel">
        <NuxtLink
          v-for="(b, k) in bannerHien"
          v-show="k === iBanner"
          :key="k"
          :to="lienKetAnToan(b.to) || '/'"
          class="group relative block aspect-[21/7] min-h-44 bg-[#1b1540]"
          :aria-label="b.tieuDe"
        >
          <img v-if="b.anh" :src="lienKetAnToan(b.anh)" alt="" loading="lazy" class="absolute inset-0 size-full object-cover">
          <span class="absolute inset-0 bg-[linear-gradient(90deg,rgb(13_10_31/.85),rgb(13_10_31/.2)_60%,transparent)]" />
          <span class="absolute inset-y-0 left-0 flex max-w-xl flex-col justify-center p-6 sm:p-10">
            <span class="font-display text-2xl font-extrabold text-white sm:text-4xl">{{ b.tieuDe }}</span>
            <span v-if="b.moTa" class="mt-2 text-sm text-white/85 sm:text-base">{{ b.moTa }}</span>
          </span>
        </NuxtLink>
        <div v-if="bannerHien.length > 1" class="absolute bottom-3 right-4 flex gap-1.5">
          <button v-for="(b, k) in bannerHien" :key="k" type="button" class="size-2.5 rounded-sm" :class="k === iBanner ? 'bg-white' : 'bg-white/45'" :aria-label="`Banner ${k + 1}`" @click="iBanner = k" />
        </div>
      </div>
    </UContainer>

    <!-- ============ 5 POKÉMON KHỞI ĐẦU ============ -->
    <section id="classes" class="scroll-mt-20 pt-16 sm:pt-20" aria-labelledby="classes-title">
      <UContainer class="max-w-6xl">
        <div class="text-center">
          <p class="eyebrow">
            Chính bạn là Pokémon
          </p>
          <h2 id="classes-title" class="tieu-de-muc mt-2 text-3xl sm:text-5xl">
            Chọn Pokémon khởi đầu
          </h2>
        </div>
        <div class="khung mt-8 overflow-hidden">
          <div class="grid md:grid-cols-[minmax(0,1fr)_minmax(0,1.1fr)]">
            <div class="relative border-b-2 border-[var(--khung-vien)] md:border-b-0 md:border-r-2">
              <PokeSan :key="chon.dex" :dex="chon.dex" :he="chon.he" :size="260" :label="chon.ten" class="w-full" />
              <span class="absolute left-3 top-3 rounded bg-[#1b1540] px-2 py-1 font-pixel text-[10px] text-white">#{{ String(chon.dex).padStart(3, '0') }}</span>
            </div>
            <div class="flex flex-col p-5 sm:p-7">
              <p class="text-xs font-extrabold uppercase tracking-wider text-primary">
                {{ chon.loi }}
              </p>
              <h3 class="tieu-de-muc mt-1 text-3xl sm:text-4xl">
                {{ chon.ten }}
              </h3>
              <div class="mt-2 flex flex-wrap gap-1.5">
                <PokeHeBadge v-for="h in chon.he" :key="h" :he="h" />
              </div>
              <p class="mt-4 text-toned">
                {{ chon.mo }}
              </p>
              <div class="mt-5">
                <p class="text-xs font-extrabold uppercase tracking-wider text-dimmed">
                  Tiến hoá
                </p>
                <ol v-if="!chon.nhanhEevee.length" class="mt-2 flex flex-wrap items-center gap-x-1 gap-y-2 text-sm">
                  <li v-for="(t, j) in chon.chuoi" :key="t.dex" class="inline-flex items-center gap-1.5">
                    <UIcon v-if="j > 0" name="i-lucide-chevron-right" class="size-4 text-dimmed" aria-hidden="true" />
                    <img :src="anhChanDung(t.dex)" width="40" height="40" alt="" loading="lazy" class="pixelated size-9 rounded border-2 border-[var(--khung-vien)]">
                    <span class="font-semibold text-highlighted">{{ t.ten }}</span>
                    <span v-if="t.cap" class="text-xs text-dimmed">cấp {{ t.cap }}</span>
                  </li>
                </ol>
                <ul v-else class="mt-2 flex flex-wrap gap-1.5">
                  <li v-for="t in chon.nhanhEevee" :key="t.dex">
                    <img :src="anhChanDung(t.dex)" width="40" height="40" :alt="t.ten" :title="t.ten" loading="lazy" class="pixelated size-10 rounded border-2 border-[var(--khung-vien)]">
                  </li>
                </ul>
              </div>
              <div class="mt-auto pt-6">
                <div class="grid grid-cols-5 gap-2" role="group" aria-label="Chọn Pokémon khởi đầu">
                  <button
                    v-for="(c, i) in khoiDau"
                    :key="c.dex"
                    type="button"
                    class="o-slot aspect-square p-1"
                    :aria-pressed="i === iChon"
                    :aria-label="c.ten"
                    @click="iChon = i"
                  >
                    <img :src="anhChanDung(c.dex)" width="40" height="40" alt="" class="pixelated size-full max-w-16 rounded">
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </UContainer>
    </section>

    <!-- ============ ẢNH TRONG GAME ============ -->
    <section class="pt-16 sm:pt-20" aria-labelledby="anh-game-title">
      <UContainer class="max-w-7xl">
        <div class="text-center">
          <p class="eyebrow">
            Ảnh chụp trong game
          </p>
          <h2 id="anh-game-title" class="tieu-de-muc mt-2 text-3xl sm:text-5xl">
            {{ tc.tinhNangTieuDe }}
          </h2>
        </div>
        <SiteTrinhChieu v-if="tc.hienCanhPhim" class="mt-8" />
        <ul class="mt-6 grid grid-cols-2 gap-3 sm:gap-4 lg:grid-cols-3">
          <li v-for="m in BO_SUU_TAP" :key="m">
            <NuxtLink :to="LINK_ANH[m] ?? '/cam-nang'" class="khung group block overflow-hidden">
              <SiteAnhGame :ma="m" sizes="(min-width: 1024px) 400px, 50vw" class="rounded-t-[.45rem]" />
              <span class="block border-t-2 border-[var(--khung-vien)] px-3 py-2.5">
                <span class="block font-display text-base font-bold text-highlighted group-hover:text-primary sm:text-lg">{{ ANH_GAME[m]!.tieuDe }}</span>
                <span class="mt-0.5 line-clamp-2 hidden text-sm text-muted sm:block">{{ ANH_GAME[m]!.moTa }}</span>
              </span>
            </NuxtLink>
          </li>
        </ul>
        <!-- Điểm nổi bật (sửa ở /admin/trang-chu) -->
        <ul class="mt-10 grid gap-x-8 gap-y-4 sm:grid-cols-2 lg:grid-cols-3">
          <li v-for="f in tc.tinhNang" :key="f.t">
            <NuxtLink :to="lienKetAnToan(f.to) || '/'" class="group flex gap-3">
              <img src="/vat-pham/286.png" width="16" height="16" alt="" class="pixelated mt-1 size-4 shrink-0">
              <span>
                <span class="block font-display text-lg font-bold text-highlighted group-hover:text-primary">{{ f.t }}</span>
                <span class="block text-sm text-muted">{{ f.m }}</span>
              </span>
            </NuxtLink>
          </li>
        </ul>
      </UContainer>
    </section>

    <!-- ============ DẢI TẢI GAME ============ -->
    <section class="pt-16 sm:pt-20" aria-labelledby="tai-title">
      <UContainer class="max-w-7xl">
        <div class="vung-toi khung relative isolate overflow-hidden">
          <img src="/keyart/nen_ngang.jpg" alt="" width="1760" height="800" loading="lazy" decoding="async" class="pixelated absolute inset-0 -z-10 size-full object-cover object-bottom opacity-40">
          <div class="absolute inset-0 -z-10 bg-[linear-gradient(90deg,#120f24,rgb(18_15_36/.75)_55%,rgb(18_15_36/.3))]" aria-hidden="true" />
          <div class="flex flex-col gap-6 p-6 sm:p-10 lg:flex-row lg:items-center lg:justify-between">
            <div class="max-w-xl">
              <h2 id="tai-title" class="tieu-de-muc text-3xl !text-white sm:text-4xl">
                Tải game miễn phí
              </h2>
              <p class="mt-2 text-toned">
                Tạo tài khoản trên website, tải bản cho thiết bị của bạn và đăng nhập để chọn Pokémon.
              </p>
              <ul class="mt-4 flex flex-wrap gap-2">
                <li v-for="b in dsBanTai" :key="b.ma" class="inline-flex items-center gap-1.5 rounded border-2 border-[#3b3366] bg-[#1b1540]/80 px-2.5 py-1 text-sm font-bold text-white">
                  <UIcon :name="b.icon" class="size-4" aria-hidden="true" />{{ b.ten }}<span v-if="b.sapRaMat" class="text-xs font-semibold text-[#ffcb05]">· sắp ra mắt</span>
                </li>
              </ul>
            </div>
            <div class="flex flex-wrap gap-3">
              <UButton to="/download" size="xl" class="btn-pikachu px-7 uppercase" icon="i-lucide-download">
                Tải game
              </UButton>
              <ClientOnly>
                <UButton v-if="auth.loggedIn.value" to="/account" size="xl" class="btn-game px-7 uppercase">
                  Tài khoản
                </UButton>
                <UButton v-else to="/register" size="xl" class="btn-game px-7 uppercase">
                  Đăng ký
                </UButton>
                <template #fallback>
                  <UButton to="/register" size="xl" class="btn-game px-7 uppercase">
                    Đăng ký
                  </UButton>
                </template>
              </ClientOnly>
            </div>
          </div>
        </div>
        <SiteLuuYTuoi class="mt-6" />
      </UContainer>
    </section>
  </div>
</template>

<style scoped>
.hero-logo {
  display: block;
  height: auto;
  animation: hero-logo-hien 0.4s cubic-bezier(0.2, 0.8, 0.2, 1) both;
}
@keyframes hero-logo-hien {
  from { transform: translateY(10px); }
  to { transform: none; }
}
@media (prefers-reduced-motion: reduce) {
  .hero-logo { animation: none; }
}
</style>
