<script setup lang="ts">
import type { TabsItem } from '@nuxt/ui'
import type { RankRow } from '~/components/site/RankBoard.vue'

useSeoMeta({
  title: 'Bảng vàng Pokémon',
  description: 'Mỗi chiến tích, một dấu ấn. Cùng nhìn lại những nhà huấn luyện và Pokémon mạnh nhất vùng đất này.',
})

interface RankingData {
  topLevel: Array<{ charname: string, lastLv: number, class: number, dex: number }>
  topLevelError: boolean
  topNap: Array<{ name: string, luong: number }>
  topNapError: boolean
  cardRanks: Array<{ title: string, action: 'gcok' | 'pcok', rows: Array<{ player_name: string, total: number }>, error: boolean }>
  cardRankNote: string
}

const { data, error } = await useFetch<RankingData>('/api/ranking', { key: 'htbb-ranking' })

interface Board {
  value: string
  label: string
  icon: string
  note?: string
  nameLabel: string
  valueLabel: string
  rows: RankRow[]
  error: boolean
  errorText: string
  emptyText: string
}

const boards = computed<Board[]>(() => {
  const d = data.value
  const loiTai = !d || !!error.value
  const ds: Board[] = [
    {
      value: 'level',
      label: 'Top Cấp Độ',
      icon: 'i-lucide-trophy',
      nameLabel: 'Pokémon',
      valueLabel: 'Cấp',
      rows: (d?.topLevel ?? []).map(c => ({ name: c.charname, sub: speciesName(c.class, c.lastLv, c.dex), anh: anhPokemon(dexHienTai(c.class, c.lastLv, c.dex)), value: c.lastLv })),
      error: loiTai || !!d?.topLevelError,
      errorText: 'Tạm thời chưa tải được dữ liệu xếp hạng.',
      emptyText: 'Chưa có dữ liệu xếp hạng.',
    },
    {
      value: 'nap',
      label: 'Top Ủng Hộ',
      icon: 'i-lucide-gem',
      nameLabel: 'Người chơi',
      valueLabel: 'Lượng',
      rows: (d?.topNap ?? []).map(c => ({ name: c.name, value: c.luong })),
      error: loiTai || !!d?.topNapError,
      errorText: 'Tạm thời chưa tải được dữ liệu xếp hạng.',
      emptyText: 'Chưa có dữ liệu xếp hạng.',
    },
  ]
  const the = d?.cardRanks ?? [
    { title: 'Top Giao Thẻ', action: 'gcok' as const, rows: [], error: true },
    { title: 'Top Phá Thẻ', action: 'pcok' as const, rows: [], error: true },
  ]
  for (const r of the) {
    ds.push({
      value: r.action,
      label: r.title,
      icon: r.action === 'gcok' ? 'i-lucide-flag' : 'i-lucide-swords',
      note: d?.cardRankNote ?? 'Chiếm thành · Tính từ 20:12 ngày 12/09/2026.',
      nameLabel: 'Người chơi',
      valueLabel: 'Số lần',
      rows: r.rows.map(c => ({ name: c.player_name, value: c.total })),
      error: loiTai || r.error,
      errorText: 'Tạm thời chưa tải được bảng xếp hạng.',
      emptyText: `Chưa có lượt ${r.action === 'gcok' ? 'giao thẻ thành công' : 'phá thẻ'} được ghi nhận.`,
    })
  }
  return ds
})

const tabs = computed<TabsItem[]>(() => boards.value.map(b => ({ label: b.label, icon: b.icon, value: b.value })))
const tab = ref('level')
const board = computed(() => boards.value.find(b => b.value === tab.value) ?? boards.value[0]!)
</script>

<template>
  <div>
    <SitePageBanner
      title="Bảng vàng Pokémon"
      eyebrow="Xếp hạng"
      intro="Mỗi chiến tích, một dấu ấn. Cùng nhìn lại những nhà huấn luyện và Pokémon mạnh nhất vùng đất này."
    />

    <section id="rank" class="relative scroll-mt-20 py-20" aria-labelledby="rank-title">
      <FxGlowOrbs />
      <UContainer class="relative max-w-5xl">
        <FxReveal class="text-center">
          <p class="eyebrow justify-center">
            Dấu ấn của nhà vô địch
          </p>
          <h2 id="rank-title" class="section-title mt-3 text-4xl text-highlighted sm:text-5xl">
            Bảng vàng Pokémon
          </h2>
        </FxReveal>

        <FxReveal :delay="0.1">
          <UTabs
            v-model="tab"
            :items="tabs"
            :content="false"
            variant="pill"
            color="primary"
            class="mx-auto mt-10 w-full max-w-3xl"
            :ui="{ list: 'glass rounded-2xl p-1.5', trigger: 'min-w-0 flex-1 justify-center rounded-xl data-[state=inactive]:text-muted', label: 'sr-only sm:not-sr-only sm:truncate' }"
          />
        </FxReveal>

        <div class="mt-10">
          <p v-if="board.note" class="mb-6 flex items-center justify-center gap-2 text-sm text-muted">
            <UIcon name="i-lucide-castle" class="size-4 text-primary" /> {{ board.note }}
          </p>
          <SiteRankBoard
            :key="board.value"
            :rows="board.rows"
            :name-label="board.nameLabel"
            :value-label="board.valueLabel"
            :error="board.error"
            :error-text="board.errorText"
            :empty-text="board.emptyText"
          />
        </div>
      </UContainer>
    </section>
  </div>
</template>
