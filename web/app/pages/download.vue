<script setup lang="ts">
useSeoMeta({
  title: 'Tải game',
  description: 'Một lần tải game. Mở ra cả một thế giới để khám phá, kết giao và viết nên câu chuyện của bạn.',
})

const { data: site } = await useSite()
const hoTro = computed(() => linkHoTro(site.value))

/** Link được cấu hình hay còn để trống / '#'. */
function coLink(url: string | null | undefined): boolean {
  const u = (url ?? '').trim()
  return u !== '' && u !== '#'
}

const platforms = computed(() => [
  { name: 'Android', hint: 'Bản cài đặt APK', icon: 'i-lucide-smartphone', url: site.value?.linkAndroid ?? '', mau: 'from-grass-400/25 to-grass-900/5', anh: 'shadow-grass-500/30' },
  { name: 'PC / Windows', hint: 'Chơi trên máy tính', icon: 'i-lucide-monitor', url: site.value?.linkPc ?? '', mau: 'from-pikachu-400/25 to-pikachu-900/5', anh: 'shadow-pikachu-500/30' },
  { name: 'Java (.jar)', hint: 'Điện thoại Java / giả lập J2ME', icon: 'i-lucide-cpu', url: site.value?.linkJar ?? '', mau: 'from-sky-400/25 to-sky-900/5', anh: 'shadow-sky-500/30' },
  // iOS chưa có bản riêng: chỉ hiện khi đã cấu hình link
  ...(coLink(site.value?.linkIos) ? [{ name: 'iOS', hint: 'Hướng dẫn cài đặt', icon: 'i-lucide-tablet-smartphone', url: site.value!.linkIos, mau: 'from-rose-400/25 to-rose-900/5', anh: 'shadow-rose-500/30' }] : []),
])

const steps = computed(() => [
  { so: '01.', tieuDe: 'Tạo tài khoản', mo: 'Đăng ký tài khoản để bắt đầu hành trình của bạn.', link: 'Đăng ký ngay', to: '/register' },
  { so: '02.', tieuDe: 'Tải phiên bản phù hợp', mo: 'Chọn nền tảng và làm theo hướng dẫn cài đặt.', link: 'Chọn bản tải', to: '/download#download' },
  { so: '03.', tieuDe: 'Chọn Pokémon khởi đầu', mo: 'Đăng nhập game, chọn Pokémon của bạn và tìm những người bạn đồng hành.', link: 'Cộng đồng & hỗ trợ', to: hoTro.value },
])

const faq = [
  { label: 'Tôi cần làm gì sau khi đăng ký?', slot: 'faq1' as const },
  { label: 'Tôi gặp khó khăn khi cài đặt hoặc đăng nhập?', slot: 'faq2' as const },
]
</script>

<template>
  <div>
    <SitePageBanner
      title="Hẹn gặp trong thế giới Pokémon"
      eyebrow="Tải game"
      intro="Một lần tải game. Mở ra cả một thế giới để khám phá, kết giao và viết nên câu chuyện của bạn."
    />

    <!-- ============ CHỌN NỀN TẢNG ============ -->
    <section id="download" class="scroll-mt-20 py-20" aria-labelledby="download-title">
      <UContainer class="max-w-6xl">
        <FxReveal class="text-center">
          <p class="eyebrow justify-center">
            Chọn nền tảng của bạn
          </p>
          <h2 id="download-title" class="section-title mt-3 text-4xl text-highlighted sm:text-5xl">
            Sẵn sàng cho cuộc phiêu lưu?
          </h2>
          <p class="mx-auto mt-4 max-w-2xl text-muted">
            Chọn phiên bản phù hợp với thiết bị. Bạn có thể tạo tài khoản trước khi cài đặt game.
          </p>
        </FxReveal>

        <div class="mt-14 grid gap-6 md:grid-cols-3">
          <FxReveal v-for="(p, i) in platforms" :key="p.name" :delay="i * 0.1">
            <FxTiltCard :max="12" as="article" class="group h-full rounded-3xl">
              <div class="relative flex h-full flex-col items-center overflow-hidden rounded-3xl glass ring-gilded bg-gradient-to-b p-8 text-center" :class="p.mau">
                <small class="text-[0.7rem] tracking-[0.25em] text-dimmed">0{{ i + 1 }} / NỀN TẢNG</small>
                <span
                  class="mt-8 grid size-20 place-items-center rounded-2xl border border-white/10 bg-white/5 text-highlighted shadow-2xl transition-transform duration-500 [transform:translateZ(70px)] group-hover:scale-110"
                  :class="p.anh"
                  aria-hidden="true"
                >
                  <UIcon :name="p.icon" class="size-10" />
                </span>
                <h3 class="mt-8 font-display text-2xl font-bold text-highlighted [transform:translateZ(40px)]">
                  {{ p.name }}
                </h3>
                <p class="mt-2 flex-1 text-muted [transform:translateZ(25px)]">
                  {{ p.hint }}
                </p>
                <div class="mt-8 w-full [transform:translateZ(30px)]">
                  <UButton
                    v-if="coLink(p.url)"
                    :to="p.url"
                    external
                    block
                    size="lg"
                    trailing-icon="i-lucide-arrow-up-right"
                    class="shadow-[0_10px_30px_rgba(10,26,51,.14)]"
                  >
                    Tải / cài đặt
                  </UButton>
                  <UBadge v-else color="neutral" variant="outline" size="lg" icon="i-lucide-hourglass" class="w-full justify-center py-2">
                    Đang cập nhật bản tải
                  </UBadge>
                </div>
              </div>
            </FxTiltCard>
          </FxReveal>
        </div>
      </UContainer>
    </section>

    <!-- ============ HỖ TRỢ ============ -->
    <section id="support" class="scroll-mt-20 pb-20" aria-labelledby="support-title">
      <UContainer class="max-w-6xl">
        <FxReveal>
          <div class="relative flex flex-col items-start justify-between gap-6 overflow-hidden rounded-3xl glass ring-gilded p-8 sm:flex-row sm:items-center sm:p-10">
            <div class="pointer-events-none absolute -left-16 -bottom-24 size-72 rounded-full bg-grass-500/15 blur-3xl" aria-hidden="true" />
            <div class="relative flex items-start gap-5">
              <span class="hidden size-14 shrink-0 place-items-center rounded-2xl border border-grass-400/30 bg-grass-500/10 text-grass-300 sm:grid" aria-hidden="true">
                <UIcon name="i-lucide-life-buoy" class="size-7" />
              </span>
              <div>
                <p class="eyebrow">
                  Luôn có bạn bè đồng hành
                </p>
                <h2 id="support-title" class="section-title mt-2 text-3xl text-highlighted">
                  Cần hỗ trợ cài đặt?
                </h2>
                <p class="mt-2 text-muted">
                  Kết nối cộng đồng để nhận hướng dẫn và tìm những người bạn cùng chơi.
                </p>
              </div>
            </div>
            <UButton
              v-if="coLink(site?.linkGroup)"
              :to="site!.linkGroup"
              external
              color="neutral"
              variant="outline"
              size="lg"
              trailing-icon="i-lucide-arrow-up-right"
              class="relative"
            >
              Vào nhóm hỗ trợ
            </UButton>
            <UBadge v-else color="neutral" variant="outline" size="lg" icon="i-lucide-hourglass" class="relative">
              Kênh hỗ trợ đang cập nhật
            </UBadge>
          </div>
        </FxReveal>
      </UContainer>
    </section>

    <!-- ============ HÀNH TRÌNH + FAQ ============ -->
    <section id="guide" class="scroll-mt-20 pb-28" aria-labelledby="guide-title">
      <UContainer class="max-w-6xl">
        <FxReveal>
          <div class="relative overflow-hidden rounded-3xl glass ring-gilded p-8 sm:p-12">
            <div class="pointer-events-none absolute -right-24 -top-24 size-80 rounded-full bg-pikachu-400/10 blur-3xl" aria-hidden="true" />
            <p class="eyebrow">
              Hẹn gặp bạn trong thế giới Pokémon
            </p>
            <h2 id="guide-title" class="section-title mt-3 text-4xl text-highlighted">
              Hành trình bắt đầu từ đây.
            </h2>
            <div class="mt-10 grid gap-8 md:grid-cols-3">
              <div v-for="s in steps" :key="s.so">
                <b class="font-display text-5xl text-primary/30">{{ s.so }}</b>
                <h3 class="mt-2 text-lg font-bold text-highlighted">
                  {{ s.tieuDe }}
                </h3>
                <p class="mt-1 text-muted">
                  {{ s.mo }}
                </p>
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
