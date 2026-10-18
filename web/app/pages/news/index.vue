<script setup lang="ts">
const tenSite = useAppConfig().htbb.tenSite
useSeoMeta({
  title: 'Tin tức & sự kiện',
  description: `Những câu chuyện mới, những cuộc hội ngộ và thông tin từ thế giới ${tenSite}.`,
})

const { data: dsTin } = await useFetch<TinTomTatWeb[]>('/api/tin-tuc', { key: 'htbb-tin', default: () => [] })
const news = computed(() => (dsTin.value ?? []).map(t => [t.slug, { category: t.loai, date: t.ngay, title: t.tieuDe, body: t.tomTat, anh: t.anhBia, ghim: t.ghim }] as const))
const lich = useLichSuKien()
const suKien = ref<SuKienTuan[]>([])
onMounted(() => { suKien.value = suKienTuTuan(tuanTrongNam(new Date()), 3, lich.value) })
watch(lich, l => { suKien.value = suKienTuTuan(tuanTrongNam(new Date()), 3, l) })

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

        <ClientOnly>
          <div class="mt-10 rounded-3xl glass p-5 sm:p-6">
            <div class="flex flex-wrap items-center justify-between gap-3">
              <h3 class="font-display text-xl font-extrabold text-highlighted">
                <UIcon name="i-lucide-calendar-days" class="mr-1 inline size-5 align-[-3px] text-primary" /> Sự kiện trong game
              </h3>
              <UButton to="/cam-nang/su-kien" size="sm" color="neutral" variant="outline" trailing-icon="i-lucide-arrow-right">
                Lịch cả năm
              </UButton>
            </div>
            <ol class="mt-4 grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
              <li v-for="(e, i) in suKien" :key="e.tuan" class="relative overflow-hidden rounded-2xl bg-elevated/50 p-3 pl-4">
                <span class="absolute inset-y-0 left-0 w-1" :style="{ background: LOAI_SU_KIEN[e.loai]?.mau }" />
                <p class="text-[11px] font-bold uppercase tracking-wide" :class="i === 0 ? 'text-primary' : 'text-dimmed'">
                  {{ i === 0 ? 'Tuần này' : `Tuần ${e.tuan}` }} · {{ LOAI_SU_KIEN[e.loai]?.ten }}
                </p>
                <p class="mt-0.5 font-display font-bold text-highlighted">
                  {{ e.ten1 }}
                </p>
                <p class="mt-0.5 line-clamp-2 text-xs text-muted">
                  {{ e.coChe }}
                </p>
              </li>
            </ol>
          </div>
        </ClientOnly>

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
                <img v-if="n.anh" :src="n.anh" alt="" loading="lazy" class="-mx-8 -mt-8 mb-6 aspect-[21/9] w-[calc(100%+4rem)] max-w-none object-cover">
                <div class="flex items-center justify-between gap-4 [transform:translateZ(30px)]">
                  <span class="flex items-center gap-2">
                    <UBadge color="primary" variant="subtle" :icon="iconTheoLoai[n.category] ?? 'i-lucide-newspaper'">
                      {{ n.category }}
                    </UBadge>
                    <UBadge v-if="n.ghim" color="warning" variant="subtle" icon="i-lucide-pin">Ghim</UBadge>
                  </span>
                  <span class="font-display text-lg font-bold text-dimmed">{{ n.date }}</span>
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
          <span class="relative font-display text-2xl font-bold text-highlighted">Chưa có Pokémon của riêng mình?</span>
          <UButton to="/register" size="xl" trailing-icon="i-lucide-arrow-up-right" class="relative shadow-[0_10px_40px_rgba(10,26,51,.14)]">
            Bắt đầu phiêu lưu
          </UButton>
        </div>
      </FxReveal>
    </UContainer>
  </div>
</template>
