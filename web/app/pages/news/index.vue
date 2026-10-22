<script setup lang="ts">
/** Tin tức (CSDL web_tin_tuc, có tin mặc định) + sự kiện đang / sắp diễn ra theo lịch năm của máy chủ game. */
import { cacTuanToi, homNayMayChu, ngayTuEpoch, type LichVungHoangDa } from '~~/shared/utils/vungHoangDa'
const tenSite = useAppConfig().htbb.tenSite
useSeoMeta({
  title: 'Tin tức & sự kiện',
  description: `Thông báo, sự kiện và cập nhật mới nhất của ${tenSite}.`,
})

const { data: dsTin } = await useFetch<TinTomTatWeb[]>('/api/tin-tuc', { key: 'htbb-tin', default: () => [], timeout: 4000 })
const ds = computed(() => dsTin.value ?? [])
const dau = computed(() => ds.value[0] ?? null)
const conLai = computed(() => ds.value.slice(1))

const skNam = useSuKienNam(45)
const skHien = computed(() => [
  ...skNam.dang.value.map(l => ({ l, dang: true })),
  ...skNam.sap.value.map(l => ({ l, dang: false })),
].slice(0, 6))
// Vùng hoang dã xoay vòng (loai-bo-sung): tuần này + 2 tuần tới, cùng công thức với máy chủ
const { data: vhd } = await useFetch<LichVungHoangDa | null>('/api/vung-hoang-da', { key: 'htbb-vung-hoang-da', default: () => null, timeout: 4000 })
const vhdTuan = computed(() => vhd.value ? cacTuanToi(vhd.value, homNayMayChu(), 3) : [])
const lich = useLichSuKien()
const suKien = ref<SuKienTuan[]>([])
onMounted(() => { suKien.value = suKienTuTuan(tuanTrongNam(new Date()), 3, lich.value) })
watch(lich, l => { suKien.value = suKienTuTuan(tuanTrongNam(new Date()), 3, l) })
</script>

<template>
  <div>
    <SitePageBanner
      title="Tin tức & sự kiện"
      eyebrow="Bảng tin"
      intro="Thông báo, sự kiện và cập nhật mới nhất trong game."
    />

    <UContainer class="max-w-7xl py-10 sm:py-14">
      <div class="grid gap-8 lg:grid-cols-[minmax(0,1fr)_22rem]">
        <section aria-labelledby="news-title">
          <h2 id="news-title" class="sr-only">
            Tin tức
          </h2>
          <article v-if="dau" class="khung group relative overflow-hidden">
            <img v-if="dau.anhBia" :src="dau.anhBia" alt="" width="1200" height="514" class="aspect-[21/9] w-full rounded-t-[.45rem] border-b-2 border-[var(--khung-vien)] object-cover">
            <div class="p-5 sm:p-7">
              <p class="flex flex-wrap items-center gap-2 text-xs font-bold text-dimmed">
                <span class="rounded bg-pokered-600 px-1.5 py-0.5 font-extrabold uppercase text-white">{{ dau.loai }}</span>
                <span v-if="dau.ghim" class="rounded bg-pikachu-400 px-1.5 py-0.5 font-extrabold uppercase text-[#261a00]">Ghim</span>
                <time>{{ dau.ngay }}</time>
              </p>
              <h3 class="tieu-de-muc mt-3 text-2xl sm:text-3xl">
                <NuxtLink :to="`/news/${dau.slug}`" class="after:absolute after:inset-0 group-hover:text-primary">
                  {{ dau.tieuDe }}
                </NuxtLink>
              </h3>
              <p class="mt-2 text-muted">
                {{ dau.tomTat }}
              </p>
            </div>
          </article>

          <ul v-if="conLai.length" class="khung mt-6 divide-y divide-[var(--ui-border-muted)] overflow-hidden">
            <li v-for="a in conLai" :key="a.slug">
              <NuxtLink :to="`/news/${a.slug}`" class="group flex gap-4 p-4 hover:bg-elevated/60 sm:p-5">
                <img v-if="a.anhBia" :src="a.anhBia" alt="" width="160" height="90" loading="lazy" class="hidden aspect-video w-36 shrink-0 rounded border-2 border-[var(--khung-vien)] object-cover sm:block">
                <span class="min-w-0">
                  <span class="flex flex-wrap items-center gap-2 text-xs font-bold text-dimmed">
                    <span class="rounded bg-elevated px-1.5 py-0.5 font-extrabold uppercase text-toned">{{ a.loai }}</span>
                    <span v-if="a.ghim" class="rounded bg-pikachu-400 px-1.5 py-0.5 font-extrabold uppercase text-[#261a00]">Ghim</span>
                    <time>{{ a.ngay }}</time>
                  </span>
                  <span class="mt-1 block font-display text-lg font-bold text-highlighted group-hover:text-primary">{{ a.tieuDe }}</span>
                  <span class="mt-0.5 line-clamp-2 block text-sm text-muted">{{ a.tomTat }}</span>
                </span>
              </NuxtLink>
            </li>
          </ul>
          <p v-if="!ds.length" class="khung p-8 text-center text-muted">
            Chưa có tin nào. Quay lại sau nhé!
          </p>
        </section>

        <aside aria-labelledby="su-kien-title">
          <div class="khung overflow-hidden lg:sticky lg:top-24">
            <h2 id="su-kien-title" class="khung-dau">
              <img src="/tien/su_kien_32.png" width="32" height="32" alt="" class="pixelated size-5">Sự kiện trong game
            </h2>
            <ClientOnly>
              <ul v-if="skHien.length" class="divide-y divide-[var(--ui-border-muted)]">
                <li v-for="e in skHien" :key="`${e.l.ma}-${e.l.nam}-${e.l.lan}`" class="px-4 py-3">
                  <p class="text-[11px] font-extrabold uppercase" :class="e.dang ? 'text-success' : 'text-dimmed'">
                    {{ e.dang ? `Đang diễn ra · còn ${demNguoc(msTuChuoi(e.l.dong) - skNam.bay.value)}` : `Sắp tới · sau ${demNguoc(msTuChuoi(e.l.mo) - skNam.bay.value)}` }}
                  </p>
                  <p class="font-display font-bold text-highlighted">
                    {{ e.l.ten }}
                  </p>
                  <p class="text-xs text-muted">
                    {{ ngayNgan(msTuChuoi(e.l.mo)) }} – {{ ngayNgan(msTuChuoi(e.l.dong) - 60_000) }}
                  </p>
                </li>
              </ul>
              <ul v-else-if="suKien.length" class="divide-y divide-[var(--ui-border-muted)]">
                <li v-for="(e, i) in suKien" :key="e.tuan" class="px-4 py-3">
                  <p class="text-[11px] font-extrabold uppercase" :class="i === 0 ? 'text-success' : 'text-dimmed'">
                    {{ i === 0 ? 'Tuần này' : i === 1 ? 'Tuần sau' : 'Sắp tới' }} · {{ LOAI_SU_KIEN[e.loai]?.ten }}
                  </p>
                  <p class="font-display font-bold text-highlighted">
                    {{ e.ten1 }}
                  </p>
                  <p class="line-clamp-2 text-xs text-muted">
                    {{ e.coChe }}
                  </p>
                </li>
              </ul>
              <p v-else class="px-4 py-6 text-center text-sm text-muted">
                Lịch sự kiện đang cập nhật.
              </p>
              <template #fallback>
                <div class="space-y-3 p-4">
                  <USkeleton v-for="k in 3" :key="k" class="h-10 w-full" />
                </div>
              </template>
            </ClientOnly>
            <div v-if="vhd && vhdTuan.length" class="border-t-2 border-[var(--ui-border-muted)]" data-vung-hoang-da>
              <h3 class="px-4 pt-3 font-display text-sm font-extrabold uppercase text-highlighted">
                Vùng hoang dã
              </h3>
              <ul class="divide-y divide-[var(--ui-border-muted)]">
                <li v-for="(t, i) in vhdTuan" :key="t.batDau" class="px-4 py-3">
                  <p class="text-[11px] font-extrabold uppercase" :class="i === 0 ? 'text-success' : 'text-dimmed'">
                    {{ i === 0 ? 'Tuần này' : i === 1 ? 'Tuần sau' : 'Sắp tới' }} · từ {{ ngayTuEpoch(t.batDau) }}
                  </p>
                  <p class="font-display font-bold text-highlighted">
                    {{ vhd.tuan[t.chuKy]?.ten }} · {{ vhd.soLoai[t.chuKy] ?? 0 }} loài
                  </p>
                  <p class="line-clamp-2 text-xs text-muted">
                    {{ vhd.tuan[t.chuKy]?.moTa }}
                  </p>
                </li>
              </ul>
              <p class="px-4 pb-3 text-xs text-muted">
                Bản đồ: {{ vhd.banDo.filter(b => b.nhom === 0).map(b => b.ten).join(', ') }}; cho người mới (chỉ dạng đầu):
                {{ vhd.banDo.filter(b => b.nhom === 1).map(b => b.ten).join(', ') }}. Độ hiếm và số quái không đổi; huyền thoại không xuất hiện.
              </p>
            </div>
            <div class="border-t-2 border-[var(--ui-border-muted)] p-3">
              <UButton to="/cam-nang/su-kien" block color="neutral" variant="outline" trailing-icon="i-lucide-arrow-right">
                Lịch sự kiện cả năm
              </UButton>
            </div>
          </div>
        </aside>
      </div>
    </UContainer>
  </div>
</template>
