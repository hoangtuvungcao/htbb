<script setup lang="ts">
/**
 * Trình chiếu ảnh chụp THẬT trong game ở trang chủ (thay cảnh đồng cỏ vẽ bằng CSS trước 0.9.6):
 * khung "màn hình" bo tròn, chú thích, tự chuyển 5 giây/ảnh (dừng khi rê chuột / "giảm chuyển động"), dải ảnh nhỏ để chọn.
 * Chỉ ảnh đang hiện và ảnh kế tiếp được tải (loading lazy), mỗi ảnh ~20–50 KB.
 */
const DS = ['chien-dau', 'the-gioi', 'thu-cuoi', 'tu-dong', 'khoi-dau'] as const
const i = ref(0)
const dung = ref(false)
let t: ReturnType<typeof setInterval> | undefined
onMounted(() => {
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return
  t = setInterval(() => { if (!dung.value) i.value = (i.value + 1) % DS.length }, 5000)
})
onBeforeUnmount(() => clearInterval(t))
const hien = computed(() => ANH_GAME[DS[i.value]!]!)
</script>

<template>
  <figure
    class="anh-khung relative overflow-hidden rounded-[1.75rem] bg-navy-950"
    aria-roledescription="carousel"
    aria-label="Ảnh chụp trong game"
    @mouseenter="dung = true"
    @mouseleave="dung = false"
    @focusin="dung = true"
    @focusout="dung = false"
  >
    <div class="relative aspect-video">
      <SiteAnhGame
        v-for="(m, k) in DS"
        :key="m"
        :ma="m"
        sizes="(min-width: 1280px) 1216px, 100vw"
        class="absolute inset-0 size-full transition-opacity duration-700"
        :class="k === i ? 'opacity-100' : 'opacity-0'"
        :aria-hidden="k !== i"
      />
      <span class="pointer-events-none absolute inset-x-0 bottom-0 h-2/5 bg-gradient-to-t from-navy-950/85 to-transparent" aria-hidden="true" />
      <figcaption class="absolute inset-x-0 bottom-0 flex flex-wrap items-end justify-between gap-3 p-4 sm:p-6" aria-live="polite">
        <span class="max-w-xl">
          <span class="inline-flex items-center gap-1.5 rounded-full bg-white/90 px-2.5 py-1 text-[11px] font-extrabold uppercase tracking-wider text-[#127f77]">
            <UIcon name="i-lucide-camera" class="size-3.5" /> Ảnh thật trong game
          </span>
          <span class="mt-2 block font-display text-xl font-extrabold text-white sm:text-3xl">{{ hien.tieuDe }}</span>
          <span class="mt-1 hidden text-sm text-white/90 sm:block sm:text-base">{{ hien.moTa }}</span>
        </span>
      </figcaption>
    </div>
    <div class="flex gap-2 overflow-x-auto bg-[var(--poke-card)] p-2 sm:p-3" role="tablist" aria-label="Chọn ảnh">
      <button
        v-for="(m, k) in DS"
        :key="m"
        type="button"
        role="tab"
        :aria-selected="k === i"
        :aria-label="ANH_GAME[m]!.tieuDe"
        class="relative w-24 shrink-0 overflow-hidden rounded-xl ring-2 transition sm:w-36"
        :class="k === i ? 'ring-[#1fb5a8]' : 'opacity-70 ring-transparent hover:opacity-100'"
        @click="i = k"
      >
        <img :src="`/anh-game/${m}-640.webp`" width="640" height="360" alt="" loading="lazy" decoding="async" class="aspect-video w-full object-cover">
      </button>
    </div>
  </figure>
</template>

<style scoped>
.anh-khung {
  box-shadow: 0 0 0 3px var(--poke-card), 0 0 0 5px color-mix(in oklab, var(--poke-outline) 70%, transparent), 0 24px 48px -24px rgb(10 26 51 / .6);
}
</style>
