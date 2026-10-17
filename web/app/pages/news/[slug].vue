<script setup lang="ts">
/** Chi tiết tin tức /news/<slug>. Slug lạ -> trạng thái 404. */
const route = useRoute()
const slug = computed(() => String(route.params.slug ?? ''))
const article = computed(() => newsBySlug(slug.value))

// Trả đúng mã 404 khi render phía server (để máy tìm kiếm không lập chỉ mục link
// hỏng), nhưng vẫn hiện trang "Không tìm thấy bài viết" trong khung site thay vì trang lỗi.
if (!article.value && import.meta.server) {
  setResponseStatus(useRequestEvent()!, 404)
}

const pageIntro = computed(() => article.value ? article.value.body : 'Bài viết này không tồn tại hoặc đường dẫn chưa chính xác.')
useSeoMeta({
  title: () => article.value ? article.value.title : 'Không tìm thấy bài viết',
  description: () => pageIntro.value,
  robots: () => (article.value ? undefined : 'noindex'),
})

const related = computed(() => newsList().filter(([s]) => s !== slug.value))
</script>

<template>
  <div>
    <!-- ============ 404 ============ -->
    <section v-if="!article" class="relative overflow-hidden py-28" aria-labelledby="not-found-title">
      <FxGlowOrbs />
      <UContainer class="relative max-w-2xl text-center">
        <FxReveal>
          <span class="font-display text-[8rem] leading-none text-primary/15" aria-hidden="true">404</span>
          <p class="eyebrow justify-center">
            Tin tức · 404
          </p>
          <h1 id="not-found-title" class="section-title mt-3 text-4xl text-highlighted">
            Không tìm thấy bài viết
          </h1>
          <p class="mt-4 text-muted">
            {{ pageIntro }}
          </p>
          <UButton to="/news" size="lg" icon="i-lucide-arrow-left" class="mt-8">
            Về danh sách tin tức
          </UButton>
        </FxReveal>
      </UContainer>
    </section>

    <template v-else>
      <!-- ============ ĐẦU BÀI ============ -->
      <header class="relative overflow-hidden border-b border-default/60">
        <FxGlowOrbs />
        <UContainer class="relative max-w-5xl py-16 sm:py-20">
          <nav aria-label="Đường dẫn bài viết" class="mb-6 flex flex-wrap items-center gap-2 text-sm text-muted">
            <NuxtLink to="/" class="text-primary hover:underline">Trang chủ</NuxtLink>
            <UIcon name="i-lucide-chevron-right" class="size-4" aria-hidden="true" />
            <NuxtLink to="/news" class="text-primary hover:underline">Tin tức</NuxtLink>
            <UIcon name="i-lucide-chevron-right" class="size-4" aria-hidden="true" />
            <span aria-current="page">{{ article.title }}</span>
          </nav>
          <FxReveal>
            <div class="flex flex-wrap items-center gap-3 text-sm text-muted">
              <UBadge color="primary" variant="subtle">
                {{ article.category }}
              </UBadge>
              <span class="inline-flex items-center gap-1"><UIcon name="i-lucide-calendar" class="size-4" /> {{ article.date }}</span>
              <span class="inline-flex items-center gap-1"><UIcon name="i-lucide-feather" class="size-4" /> {{ useAppConfig().kpah.tenSite }}</span>
            </div>
            <h1 class="section-title mt-5 text-4xl text-highlighted sm:text-5xl">
              {{ article.title }}
            </h1>
            <p class="mt-5 max-w-3xl text-lg text-toned">
              {{ article.body }}
            </p>
            <div class="mt-8 flex items-center gap-4 text-primary/70" aria-hidden="true">
              <span class="h-0.5 flex-1 bg-[var(--ui-border)]" /><PokeBall class="size-5" /><span class="h-0.5 flex-1 bg-[var(--ui-border)]" />
            </div>
          </FxReveal>
        </UContainer>
      </header>

      <!-- ============ NỘI DUNG ============ -->
      <UContainer class="max-w-5xl py-16">
        <div class="grid gap-10 lg:grid-cols-[1fr_16rem]">
          <article class="min-w-0 space-y-10">
            <FxReveal v-for="(section, i) in article.sections" :id="`section-${i + 1}`" :key="i" as="section" class="scroll-mt-24">
              <h2 class="section-title text-2xl text-highlighted">
                {{ section.title }}
              </h2>
              <p v-for="(paragraph, j) in section.paragraphs ?? []" :key="j" class="mt-4 leading-relaxed text-toned">
                {{ paragraph }}
              </p>
              <ol v-if="section.steps?.length" class="mt-5 space-y-3">
                <li v-for="(step, j) in section.steps" :key="j" class="flex gap-4 rounded-2xl glass p-4">
                  <span class="grid size-8 shrink-0 place-items-center rounded-full bg-primary/15 font-display font-bold text-primary">{{ j + 1 }}</span>
                  <span class="text-toned">{{ step }}</span>
                </li>
              </ol>
            </FxReveal>

            <FxReveal>
              <UAlert color="warning" variant="subtle" icon="i-lucide-info" title="Lưu ý" :description="article.notice" />
            </FxReveal>

            <div class="flex flex-wrap gap-3">
              <UButton to="/download" size="lg" trailing-icon="i-lucide-arrow-up-right">
                Tải game
              </UButton>
              <UButton to="/news" color="neutral" variant="outline" size="lg" icon="i-lucide-arrow-left">
                Tất cả tin tức
              </UButton>
            </div>
          </article>

          <aside class="hidden lg:block" aria-label="Mục lục">
            <div class="sticky top-24 rounded-2xl glass ring-gilded p-6">
              <p class="eyebrow">
                Trong bài viết
              </p>
              <nav aria-label="Mục lục bài viết" class="mt-4 flex flex-col gap-2 text-sm">
                <a v-for="(section, i) in article.sections" :key="i" :href="`#section-${i + 1}`" class="text-muted transition-colors hover:text-primary">
                  {{ section.title }}
                </a>
              </nav>
              <USeparator class="my-5" />
              <div class="flex items-center gap-3 text-sm text-dimmed">
                <UIcon name="i-lucide-sparkles" class="size-6 text-primary/70" aria-hidden="true" />
                <p>Từng cấp một lần tiến hoá.<br>Viết nên huyền thoại của riêng bạn.</p>
              </div>
            </div>
          </aside>
        </div>
      </UContainer>

      <!-- ============ TIN LIÊN QUAN ============ -->
      <section class="pb-24" aria-labelledby="related-heading">
        <UContainer class="max-w-5xl">
          <FxReveal>
            <p class="eyebrow">
              Khám phá thêm
            </p>
            <h2 id="related-heading" class="section-title mt-3 text-3xl text-highlighted">
              Tin tức liên quan
            </h2>
          </FxReveal>
          <div class="mt-8 grid gap-6 md:grid-cols-2">
            <FxReveal v-for="([s, r], i) in related" :key="s" :delay="i * 0.08">
              <FxTiltCard :max="10" class="group h-full rounded-3xl">
                <NuxtLink :to="`/news/${s}`" class="flex h-full flex-col rounded-3xl glass ring-gilded p-6 transition-shadow hover:shadow-[0_20px_60px_rgba(10,26,51,.14)]">
                  <span class="text-xs tracking-[0.2em] text-dimmed">{{ r.category }} · {{ r.date }}</span>
                  <h3 class="mt-3 font-display text-xl font-bold text-highlighted [transform:translateZ(35px)]">
                    {{ r.title }}
                  </h3>
                  <p class="mt-2 flex-1 text-muted">
                    {{ r.body }}
                  </p>
                  <span class="mt-4 inline-flex items-center gap-2 font-semibold text-primary">
                    Đọc bài viết <UIcon name="i-lucide-arrow-up-right" class="size-4 transition-transform group-hover:-translate-y-1 group-hover:translate-x-1" />
                  </span>
                </NuxtLink>
              </FxTiltCard>
            </FxReveal>
          </div>
        </UContainer>
      </section>
    </template>
  </div>
</template>
