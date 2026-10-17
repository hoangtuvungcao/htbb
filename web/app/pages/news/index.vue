<script setup lang="ts">
const tenSite = useAppConfig().htbb.tenSite
useSeoMeta({
  title: 'Tin tức & sự kiện',
  description: `Những câu chuyện mới, những cuộc hội ngộ và thông tin từ thế giới ${tenSite}.`,
})

const news = newsList()

const iconTheoLoai: Record<string, string> = {
  'Thông báo': 'i-lucide-megaphone',
  'Sự kiện': 'i-lucide-flame',
  'Hướng dẫn': 'i-lucide-book-open',
}
</script>

<template>
  <div>
    <SitePageBanner
      title="Tin tức & sự kiện"
      eyebrow="Tin tức"
      :intro="`Những câu chuyện mới, những cuộc hội ngộ và thông tin từ thế giới ${tenSite}.`"
    />

    <section id="news" class="scroll-mt-20 py-20" aria-labelledby="news-title">
      <UContainer class="max-w-6xl">
        <FxReveal>
          <h2 id="news-title" class="section-title text-4xl text-highlighted">
            Nhịp sống vùng đất Pokémon
          </h2>
        </FxReveal>

        <div class="mt-12 grid gap-6 md:grid-cols-2">
          <FxReveal
            v-for="([slug, n], i) in news"
            :key="slug"
            :delay="i * 0.08"
            :class="i === 0 ? 'md:col-span-2' : ''"
          >
            <FxTiltCard :max="i === 0 ? 6 : 10" as="article" class="group h-full rounded-3xl">
              <div class="relative flex h-full flex-col overflow-hidden rounded-3xl glass ring-gilded p-8 transition-shadow group-hover:shadow-[0_20px_60px_rgba(10,26,51,.14)]">
                <div class="pointer-events-none absolute -right-12 -top-12 size-48 rounded-full bg-pikachu-400/10 blur-3xl transition-opacity group-hover:opacity-100" :class="i === 0 ? 'opacity-100' : 'opacity-0'" aria-hidden="true" />
                <div class="flex items-center justify-between gap-4 [transform:translateZ(30px)]">
                  <UBadge color="primary" variant="subtle" :icon="iconTheoLoai[n.category] ?? 'i-lucide-newspaper'">
                    {{ n.category }}
                  </UBadge>
                  <span class="font-display text-3xl font-bold text-primary/40">{{ n.date }}</span>
                </div>
                <h3 class="mt-6 font-display font-bold text-highlighted [transform:translateZ(45px)]" :class="i === 0 ? 'text-3xl sm:text-4xl' : 'text-2xl'">
                  <NuxtLink :to="`/news/${slug}`" class="after:absolute after:inset-0 hover:text-primary">
                    {{ n.title }}
                  </NuxtLink>
                </h3>
                <p class="mt-3 flex-1 text-muted [transform:translateZ(20px)]">
                  {{ n.body }}
                </p>
                <span class="mt-6 inline-flex items-center gap-2 border-t border-default/60 pt-4 font-semibold text-primary" aria-hidden="true">
                  Đọc tiếp <UIcon name="i-lucide-arrow-up-right" class="size-4 transition-transform group-hover:-translate-y-1 group-hover:translate-x-1" />
                </span>
                <span class="sr-only">Đọc tiếp: {{ n.title }}</span>
              </div>
            </FxTiltCard>
          </FxReveal>
        </div>
      </UContainer>
    </section>

    <UContainer class="max-w-6xl pb-24">
      <FxReveal>
        <div class="relative flex flex-col items-center justify-between gap-6 overflow-hidden rounded-3xl glass ring-gilded p-8 text-center sm:flex-row sm:text-left">
          <div class="pointer-events-none absolute -bottom-20 -left-10 size-64 rounded-full bg-grass-500/15 blur-3xl" aria-hidden="true" />
          <span class="relative font-display text-2xl font-bold text-highlighted">Chưa có nhân vật của riêng mình?</span>
          <UButton to="/register" size="xl" trailing-icon="i-lucide-arrow-up-right" class="relative shadow-[0_10px_40px_rgba(10,26,51,.14)]">
            Bắt đầu phiêu lưu
          </UButton>
        </div>
      </FxReveal>
    </UContainer>
  </div>
</template>
