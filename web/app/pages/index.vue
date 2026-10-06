<script setup lang="ts">
useSeoMeta({
  title: 'Trang chủ',
  description: 'Khám phá Khí Phách Anh Hùng: năm môn phái, bang hội và hành trình kiếm hiệp cùng bằng hữu. Tải game và đăng ký tài khoản.',
})

const auth = useAuth()
onMounted(() => auth.hydrate())
const { data: site } = await useSite()
const hoTro = computed(() => linkHoTro(site.value))

const stats = [
  { so: '05', tieuDe: 'Môn phái, một giang hồ', mo: 'Tìm phong cách chiến đấu của bạn' },
  { so: '01', tieuDe: 'Lời hẹn cùng bằng hữu', mo: 'Sát cánh bên nhau, chinh phục thử thách' },
  { so: '∞', tieuDe: 'Hành trình khám phá', mo: 'Mỗi ngày một câu chuyện mới' },
]

const classes = [
  { ten: 'Kiếm khách', ky: '⚔', mo: 'Kiếm trong tay, ngạo nghễ bước giữa giang hồ.', mau: 'from-jade-400/30 to-jade-900/10', vien: 'hover:border-jade-400/60', anh: 'shadow-jade-500/30' },
  { ten: 'Chiến binh', ky: '♜', mo: 'Ý chí kiên cường, sát cánh cùng bằng hữu.', mau: 'from-orange-400/30 to-orange-900/10', vien: 'hover:border-orange-400/60', anh: 'shadow-orange-500/30' },
  { ten: 'Đấu sĩ', ky: '◇', mo: 'Bản lĩnh tiên phong, không lùi trước thử thách.', mau: 'from-violet-400/30 to-violet-900/10', vien: 'hover:border-violet-400/60', anh: 'shadow-violet-500/30' },
  { ten: 'Pháp sư', ky: '✧', mo: 'Khơi nguồn pháp thuật, khám phá sức mạnh huyền bí.', mau: 'from-sky-400/30 to-sky-900/10', vien: 'hover:border-sky-400/60', anh: 'shadow-sky-500/30' },
  { ten: 'Cung thủ', ky: '➶', mo: 'Giương cung đón gió, vững tâm hướng tới mục tiêu.', mau: 'from-lime-400/30 to-lime-900/10', vien: 'hover:border-lime-400/60', anh: 'shadow-lime-500/30' },
]

const portals = [
  { so: '01 / KHỞI HÀNH', icon: 'i-lucide-download', tieuDe: 'Mở lối giang hồ', mo: 'Chọn phiên bản dành cho bạn và bắt đầu hành trình.', link: 'Tải game', to: '/download' },
  { so: '02 / VINH DANH', icon: 'i-lucide-trophy', tieuDe: 'Lưu danh bảng vàng', mo: 'Theo dõi những cái tên ghi dấu ấn trong thế giới anh hùng.', link: 'Xem xếp hạng', to: '/ranking' },
  { so: '03 / HỘI NGỘ', icon: 'i-lucide-scroll-text', tieuDe: 'Chuyện chốn giang hồ', mo: 'Khám phá tin tức, hoạt động và những lời hẹn cùng bằng hữu.', link: 'Đọc tin tức', to: '/news' },
]

const steps = computed(() => [
  { so: '01.', tieuDe: 'Tạo tài khoản', mo: 'Đăng ký tài khoản để bắt đầu hành trình của bạn.', link: 'Đăng ký ngay', to: '/register' },
  { so: '02.', tieuDe: 'Tải phiên bản phù hợp', mo: 'Chọn nền tảng và làm theo hướng dẫn cài đặt.', link: 'Chọn bản tải', to: '/download' },
  { so: '03.', tieuDe: 'Gia nhập giang hồ', mo: 'Đăng nhập game, tạo nhân vật và tìm những người bạn đồng hành.', link: 'Cộng đồng & hỗ trợ', to: hoTro.value },
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
              Khí Phách Anh Hùng · MMORPG
            </p>
          </FxReveal>
          <FxReveal :delay="0.1">
            <h1 id="hero-heading" class="section-title mt-5 text-5xl text-highlighted drop-shadow-[0_4px_30px_rgba(0,0,0,.6)] sm:text-6xl lg:text-7xl">
              Một thời <span class="text-jade-300">khí phách.</span><br>
              <em class="text-gilded">Một đời anh hùng.</em>
            </h1>
          </FxReveal>
          <FxReveal :delay="0.2">
            <p class="mt-6 max-w-xl text-lg text-toned">
              Trở về miền ký ức, viết tiếp chuyện giang hồ. Cùng bằng hữu săn boss, lập bang hội và chinh phục thế giới Khí Phách Anh Hùng.
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
              <span class="h-px w-12 bg-gradient-to-r from-primary to-transparent" /> ✦ NHẬP VAI KIẾM HIỆP / KẾT NỐI BẰNG HỮU
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

    <!-- ============ NGŨ ĐẠI MÔN PHÁI ============ -->
    <section id="classes" class="scroll-mt-20 py-24" aria-labelledby="classes-title">
      <UContainer class="max-w-7xl">
        <FxReveal class="text-center">
          <p class="eyebrow justify-center">
            Chọn con đường của bạn
          </p>
          <h2 id="classes-title" class="section-title mt-3 text-4xl text-highlighted sm:text-5xl">
            Ngũ đại môn phái
          </h2>
          <p class="mx-auto mt-4 max-w-2xl text-muted">
            Một mình làm nên tên tuổi. Cùng nhau làm nên huyền thoại. Bạn sẽ bước vào giang hồ với vai trò nào?
          </p>
        </FxReveal>
        <div class="mt-14 grid gap-5 sm:grid-cols-2 lg:grid-cols-5">
          <FxReveal v-for="(c, i) in classes" :key="c.ten" :delay="i * 0.08">
            <FxTiltCard :max="14" as="article" class="group h-full rounded-2xl">
              <div
                class="relative h-full overflow-hidden rounded-2xl border border-default/70 bg-gradient-to-br p-6 transition-colors duration-300"
                :class="[c.mau, c.vien]"
              >
                <small class="text-[0.7rem] tracking-[0.2em] text-dimmed">0{{ i + 1 }} / MÔN PHÁI</small>
                <span
                  class="mt-6 grid size-16 place-items-center rounded-full border border-white/10 bg-white/5 text-3xl text-highlighted shadow-2xl transition-transform duration-500 [transform:translateZ(60px)] group-hover:scale-110"
                  :class="c.anh"
                  aria-hidden="true"
                >{{ c.ky }}</span>
                <h3 class="mt-6 font-serif text-2xl font-bold text-highlighted [transform:translateZ(40px)]">
                  {{ c.ten }}
                </h3>
                <p class="mt-2 text-sm text-muted [transform:translateZ(25px)]">
                  {{ c.mo }}
                </p>
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
              Giang hồ không chỉ có chiến trận.
            </h2>
          </div>
          <span class="hidden rounded-xl border border-primary/30 px-3 py-2 text-center font-serif text-2xl leading-tight text-primary/70 sm:block" aria-hidden="true">江<br>湖</span>
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
              Hẹn gặp bạn tại giang hồ
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
