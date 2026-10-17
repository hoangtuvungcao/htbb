<script lang="ts">
/**
 * Một bảng xếp hạng: bục vinh danh 3D cho top 3 + danh sách hạng 4–10.
 * Dùng cho trang /ranking.
 */
export interface RankRow {
  name: string
  /** Dòng phụ dưới tên (vd. loài Pokémon). */
  sub?: string
  /** Chân dung Pokémon 40×40 (vd. /pokemon/4.png), hiện cạnh tên. */
  anh?: string
  value: number
}
</script>

<script setup lang="ts">

const props = defineProps<{
  rows: RankRow[]
  /** Nhãn cột tên (Pokémon / Người chơi). */
  nameLabel: string
  /** Nhãn cột số (Cấp / Lượng / Số lần). */
  valueLabel: string
  error?: boolean
  errorText: string
  emptyText: string
}>()

const top3 = computed(() => props.rows.slice(0, 3))
const rest = computed(() => props.rows.slice(3).map((r, i) => ({ ...r, hang: i + 4 })))

/** Bục: hạng 2 bên trái, hạng 1 ở giữa (cao nhất), hạng 3 bên phải. */
const bucVinhDanh = computed(() => {
  const thuTu = [1, 0, 2]
  return thuTu
    .filter(i => top3.value[i])
    .map(i => ({ ...top3.value[i]!, hang: i + 1 }))
})

/** Màu bục theo hạng: vàng Pikachu, bạc (xanh nhạt), đồng (cam lửa). */
const kieuHang: Record<number, { nen: string, chu: string, cao: string, icon: string, huy: string }> = {
  1: { nen: 'bg-pikachu-400', chu: 'text-navy-950', cao: 'sm:h-40', icon: 'i-lucide-crown', huy: 'bg-pikachu-400 text-navy-950' },
  2: { nen: 'bg-pokeblue-200', chu: 'text-navy-950', cao: 'sm:h-32', icon: 'i-lucide-medal', huy: 'bg-pokeblue-200 text-navy-950' },
  3: { nen: 'bg-[#f6a26b]', chu: 'text-navy-950', cao: 'sm:h-24', icon: 'i-lucide-award', huy: 'bg-[#f6a26b] text-navy-950' },
}
</script>

<template>
  <div>
    <div v-if="!rows.length" class="flex flex-col items-center gap-3 rounded-2xl glass p-12 text-center">
      <PokeBall :wiggle="!error" class="size-12" :class="error ? 'opacity-50 grayscale' : ''" />
      <div class="dialog-box no-arrow mt-2 max-w-md">
        <p>{{ error ? errorText : emptyText }}</p>
      </div>
    </div>

    <template v-else>
      <!-- Bục vinh danh -->
      <div class="grid items-end gap-4 sm:grid-cols-3">
        <FxReveal
          v-for="(r, i) in bucVinhDanh"
          :key="r.hang"
          :delay="i * 0.12"
          :class="r.hang === 1 ? 'sm:order-none order-first' : ''"
        >
          <div class="flex flex-col items-center text-center">
            <span
              class="relative grid size-24 place-items-center rounded-full border-[3px] border-[var(--poke-outline)] bg-screen"
              :class="r.hang === 1 ? 'animate-float' : ''"
              aria-hidden="true"
            >
              <img v-if="r.anh" :src="r.anh" width="40" height="40" alt="" class="pixelated size-20 rounded-full">
              <PokeBall v-else class="size-12" />
              <span class="absolute -right-1 -top-1 grid size-9 place-items-center rounded-full border-[3px] border-[var(--poke-outline)]" :class="kieuHang[r.hang]!.huy">
                <UIcon :name="kieuHang[r.hang]!.icon" class="size-4" />
              </span>
            </span>
            <p class="mt-3 w-full truncate font-display text-2xl font-extrabold text-highlighted" :title="r.name">
              {{ r.name }}
            </p>
            <p v-if="r.sub" class="text-sm font-semibold text-muted">
              {{ r.sub }}
            </p>
            <!-- Thân bục: cao thấp theo hạng -->
            <div
              class="mt-3 flex w-full flex-col items-center justify-center rounded-t-2xl border-[3px] border-b-0 border-[var(--poke-outline)] py-4 sm:justify-start"
              :class="[kieuHang[r.hang]!.nen, kieuHang[r.hang]!.cao]"
            >
              <span class="font-pixel text-sm" :class="kieuHang[r.hang]!.chu">#{{ r.hang }}</span>
              <span class="num mt-2 font-display text-3xl font-extrabold" :class="kieuHang[r.hang]!.chu">{{ so(r.value) }}</span>
              <span class="text-xs font-bold uppercase tracking-[0.15em] text-navy-800">{{ valueLabel }}</span>
            </div>
          </div>
        </FxReveal>
      </div>
      <div class="h-3 rounded-b-xl border-[3px] border-[var(--poke-outline)] bg-grass-500" aria-hidden="true" />

      <!-- Hạng 4–10 -->
      <FxReveal v-if="rest.length" :delay="0.2">
        <div class="mt-8 overflow-hidden rounded-2xl glass">
          <table class="w-full text-sm">
            <thead>
              <tr class="border-b-2 border-default bg-elevated text-left font-display text-xs uppercase tracking-wider text-muted">
                <th scope="col" class="w-16 px-5 py-3">
                  #
                </th>
                <th scope="col" class="px-5 py-3">
                  {{ nameLabel }}
                </th>
                <th scope="col" class="px-5 py-3 text-right">
                  {{ valueLabel }}
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="r in rest" :key="r.hang" class="border-b border-default transition-colors last:border-0 hover:bg-pikachu-50 dark:hover:bg-navy-800">
                <td class="px-5 py-3 font-pixel text-xs text-dimmed">
                  {{ r.hang }}
                </td>
                <td class="px-5 py-3">
                  <span class="inline-flex items-center gap-3 align-middle">
                    <img v-if="r.anh" :src="r.anh" width="40" height="40" alt="" class="pixelated size-10 shrink-0 rounded-lg bg-screen">
                    <span>
                      <span class="font-bold text-highlighted">{{ r.name }}</span>
                      <span v-if="r.sub" class="ms-2 text-muted">· {{ r.sub }}</span>
                    </span>
                  </span>
                </td>
                <td class="num px-5 py-3 font-display text-base font-bold text-primary">
                  {{ so(r.value) }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </FxReveal>
    </template>
  </div>
</template>
