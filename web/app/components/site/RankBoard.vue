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
  /** Nhãn cột tên (Nhân vật / Tài khoản). */
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

const kieuHang: Record<number, { vien: string, nen: string, chu: string, cao: string, icon: string, anh: string }> = {
  1: { vien: 'border-gold-300/60', nen: 'from-gold-400/35 via-gold-700/10 to-transparent', chu: 'text-gold-300', cao: 'sm:h-40', icon: 'i-lucide-crown', anh: 'shadow-[0_0_60px_rgba(249,192,42,.35)]' },
  2: { vien: 'border-ink-200/40', nen: 'from-ink-200/25 via-ink-500/10 to-transparent', chu: 'text-ink-100', cao: 'sm:h-32', icon: 'i-lucide-medal', anh: 'shadow-[0_0_40px_rgba(203,211,209,.18)]' },
  3: { vien: 'border-orange-400/50', nen: 'from-orange-400/25 via-orange-900/10 to-transparent', chu: 'text-orange-300', cao: 'sm:h-28', icon: 'i-lucide-award', anh: 'shadow-[0_0_40px_rgba(251,146,60,.2)]' },
}
</script>

<template>
  <div>
    <div v-if="!rows.length" class="flex flex-col items-center gap-3 rounded-2xl border border-dashed border-default p-12 text-center">
      <UIcon :name="error ? 'i-lucide-cloud-off' : 'i-lucide-trophy'" class="size-10 text-primary/50" />
      <p class="text-muted">
        {{ error ? errorText : emptyText }}
      </p>
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
          <FxTiltCard :max="14" class="rounded-3xl">
            <div
              class="relative flex flex-col items-center overflow-hidden rounded-3xl border bg-gradient-to-b p-6 text-center glass"
              :class="[kieuHang[r.hang]!.vien, kieuHang[r.hang]!.nen]"
            >
              <span
                class="relative grid size-20 place-items-center rounded-full border bg-ink-950/60 [transform:translateZ(70px)]"
                :class="[kieuHang[r.hang]!.vien, kieuHang[r.hang]!.chu, kieuHang[r.hang]!.anh, r.hang === 1 ? 'animate-float' : '']"
                aria-hidden="true"
              >
                <img v-if="r.anh" :src="r.anh" width="40" height="40" alt="" class="pixelated size-16 rounded-full">
                <UIcon v-else :name="kieuHang[r.hang]!.icon" class="size-8" />
                <span v-if="r.anh" class="absolute -top-2 -right-2 grid size-8 place-items-center rounded-full border bg-ink-950" :class="kieuHang[r.hang]!.vien">
                  <UIcon :name="kieuHang[r.hang]!.icon" class="size-4" />
                </span>
              </span>
              <p class="mt-4 text-xs tracking-[0.25em] text-dimmed [transform:translateZ(30px)]">
                HẠNG {{ r.hang }}
              </p>
              <p class="mt-1 w-full truncate font-serif text-2xl font-bold text-highlighted [transform:translateZ(45px)]" :title="r.name">
                {{ r.name }}
              </p>
              <p v-if="r.sub" class="text-sm text-muted [transform:translateZ(25px)]">
                {{ r.sub }}
              </p>
              <!-- Thân bục: cao thấp theo hạng -->
              <div
                class="mt-5 flex w-full flex-col items-center justify-center rounded-2xl border border-white/5 bg-white/[.03] py-5 sm:justify-end"
                :class="kieuHang[r.hang]!.cao"
              >
                <span class="num font-serif text-4xl font-extrabold" :class="r.hang === 1 ? 'text-gilded' : kieuHang[r.hang]!.chu">{{ so(r.value) }}</span>
                <span class="mt-1 text-xs uppercase tracking-[0.2em] text-dimmed">{{ valueLabel }}</span>
              </div>
            </div>
          </FxTiltCard>
        </FxReveal>
      </div>

      <!-- Hạng 4–10 -->
      <FxReveal v-if="rest.length" :delay="0.2">
        <div class="mt-8 overflow-hidden rounded-2xl glass">
          <table class="w-full text-sm">
            <thead>
              <tr class="border-b border-default text-left text-xs uppercase tracking-wider text-dimmed">
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
              <tr v-for="r in rest" :key="r.hang" class="border-b border-default/50 transition-colors last:border-0 hover:bg-primary/5">
                <td class="px-5 py-3 font-serif text-lg font-bold text-dimmed">
                  {{ r.hang }}
                </td>
                <td class="px-5 py-3">
                  <span class="inline-flex items-center gap-3 align-middle">
                    <img v-if="r.anh" :src="r.anh" width="40" height="40" alt="" class="pixelated size-10 shrink-0 rounded-lg bg-ink-950/60">
                    <span>
                      <span class="font-semibold text-highlighted">{{ r.name }}</span>
                      <span v-if="r.sub" class="ms-2 text-muted">· {{ r.sub }}</span>
                    </span>
                  </span>
                </td>
                <td class="num px-5 py-3 font-semibold text-primary">
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
