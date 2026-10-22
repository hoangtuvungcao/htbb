<script setup lang="ts">
/**
 * "Sân" hiển thị một Pokémon: sprite PMD động (nền trong suốt, phóng NGUYÊN lần, giữ nét pixel)
 * đứng đúng chân trên bóng elip, nền nhạt pha màu hệ chính + Poké Ball mờ làm hình chìm.
 * Dùng thay cho chân dung 40×40 (có nền vẽ sẵn) ở Pokédex, thẻ, trang chi tiết.
 *
 *   <PokeSan :dex="25" :he="[3]" :size="150" class="w-full" />
 *
 * `size` = chiều cao khung (px); chiều rộng do class quyết định (mặc định bằng chiều cao).
 */
const props = withDefaults(defineProps<{ dex: number, he?: readonly number[], size?: number, shiny?: boolean, label?: string, delay?: number }>(), { size: 150, delay: 0 })

/** Mặt đất (tâm bóng) nằm ở 84% chiều cao khung. */
const MAT_DAT = 0.84
const mau = computed(() => mauHe(props.he?.[0] ?? 0).backgroundColor)
const khoa = computed(() => khoaSprite(props.dex, props.shiny))
const d = computed(() => (khoa.value ? ANIM[khoa.value]?.idle : undefined))

const viTri = computed(() => {
  const x = d.value
  if (!x) return null
  const tren = x.h / 2 + x.chanY // phần khung nằm trên điểm chân
  const s = Math.max(1, Math.floor(Math.min((props.size * 0.7) / tren, (props.size * 0.95) / x.w)))
  const matDat = props.size * MAT_DAT
  return {
    scale: s,
    style: {
      left: `calc(50% - ${(x.w / 2 + x.chanX) * s}px)`,
      top: `${Math.round(matDat - tren * s)}px`,
    },
    bong: { width: `${Math.max(18, Math.round(x.w * s * 0.5))}px`, top: `${Math.round(matDat - 4)}px` },
  }
})
const anh = computed(() => {
  const l = loaiTheoDex(props.dex)
  return l ? anhLoaiDex(l) : ''
})
</script>

<template>
  <span
    class="poke-san relative isolate block overflow-hidden"
    :style="{ '--mau': mau, 'height': `${size}px` }"
  >
    <template v-if="viTri">
      <span class="absolute left-1/2 h-2 -translate-x-1/2 rounded-[50%] bg-[rgb(10_26_51/.2)] dark:bg-black/45" :style="viTri.bong" aria-hidden="true" />
      <PokeSprite :dex="dex" :shiny="shiny" :scale="viTri.scale" :label="label" :delay="delay" class="!absolute" :style="viTri.style" />
    </template>
    <span v-else class="absolute inset-0 grid place-items-center">
      <PokeAvatar :src="anh" :alt="label" class="size-[45%]" />
    </span>
  </span>
</template>

<style scoped>
.poke-san {
  background:
    radial-gradient(90% 60% at 50% 100%, color-mix(in oklab, var(--mau) 22%, transparent), transparent 75%),
    linear-gradient(180deg, color-mix(in oklab, var(--mau) 7%, #fff), color-mix(in oklab, var(--mau) 15%, #fff));
}
.dark .poke-san {
  background:
    radial-gradient(90% 60% at 50% 100%, color-mix(in oklab, var(--mau) 28%, transparent), transparent 75%),
    linear-gradient(180deg, color-mix(in oklab, var(--mau) 8%, #0f2240), color-mix(in oklab, var(--mau) 16%, #0f2240));
}
</style>
