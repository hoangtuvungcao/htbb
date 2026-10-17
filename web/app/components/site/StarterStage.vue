<script setup lang="ts">
import { ANIM_KHOI_DAU } from '~/components/poke/Sprite.vue'

/**
 * Sân đấu nhỏ ở hero: 5 Pokémon khởi đầu (sprite PMD động) đứng trên bệ cỏ hình
 * bầu dục như màn đấu trong game, Poké Ball lơ lửng phía trên. Rê chuột / chạm vào
 * một Pokémon thì nó chạy tại chỗ.
 */
const props = defineProps<{ nho?: boolean }>()
const ds = [
  { dex: 4, ten: 'Charmander', delay: 0.2 },
  { dex: 1, ten: 'Bulbasaur', delay: 0.7 },
  { dex: 172, ten: 'Pichu', delay: 0 },
  { dex: 7, ten: 'Squirtle', delay: 0.45 },
  { dex: 133, ten: 'Eevee', delay: 0.9 },
]
const dangChay = ref<number | null>(null)

/** Hộp chứa cố định theo khung lớn nhất (idle/walk) để đổi động tác không làm xô lệch hàng. */
function hop(dex: number) {
  const a = ANIM_KHOI_DAU[dex]!
  return {
    width: `calc(${Math.max(a.idle.w, a.walk.w)}px * var(--s))`,
    height: `calc(${Math.max(a.idle.h, a.walk.h)}px * var(--s))`,
  }
}
</script>

<template>
  <div class="relative mx-auto w-full max-w-[36rem] [--s:2]" :class="props.nho ? '' : 'sm:[--s:3]'">
    <!-- Poké Ball lơ lửng -->
    <div class="pointer-events-none absolute left-1/2 top-0 -translate-x-1/2 animate-bob" aria-hidden="true">
      <PokeBall class="size-14 drop-shadow-[0_8px_0_rgb(10_26_51/.18)] sm:size-20" />
    </div>
    <div class="relative pt-20 sm:pt-28">
      <!-- Bệ đấu -->
      <div class="absolute inset-x-0 bottom-0 h-20 rounded-[50%] border-4 border-grass-700/60 bg-grass-200 shadow-[inset_0_-10px_0_rgb(0_0_0/.08)] sm:h-28 dark:border-grass-950 dark:bg-grass-700" aria-hidden="true" />
      <ul class="relative flex items-end justify-center gap-1 pb-6 sm:gap-3 sm:pb-10" aria-label="Năm Pokémon khởi đầu">
        <li
          v-for="p in ds"
          :key="p.dex"
          class="group relative flex flex-col items-center"
          tabindex="0"
          @mouseenter="dangChay = p.dex"
          @mouseleave="dangChay = null"
          @focus="dangChay = p.dex"
          @blur="dangChay = null"
          @touchstart.passive="dangChay = dangChay === p.dex ? null : p.dex"
        >
          <span class="relative grid place-items-center" :style="hop(p.dex)">
            <span class="absolute bottom-[8%] h-[14%] w-[70%] rounded-[50%] bg-black/20" aria-hidden="true" />
            <PokeSprite :dex="p.dex" :dong="dangChay === p.dex ? 'walk' : 'idle'" :scale="3" :delay="p.delay" :label="p.ten" class="relative" />
          </span>
          <span class="pointer-events-none mt-1 hidden rounded-full border-2 border-[var(--poke-outline)] bg-white px-2 font-display text-xs font-bold text-navy-900 opacity-0 transition-opacity group-hover:opacity-100 group-focus:opacity-100 sm:block">
            {{ p.ten }}
          </span>
        </li>
      </ul>
    </div>
  </div>
</template>
