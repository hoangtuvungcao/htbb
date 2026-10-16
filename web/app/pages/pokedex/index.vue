<script setup lang="ts">
/**
 * Pokédex: mọi loài có trong game, lọc theo tên/số, hệ và vai trò.
 * Dữ liệu tĩnh (shared/data/pokedex.json) -> SSR đủ danh sách, lọc ngay trên trình duyệt.
 * Bộ lọc ghi vào query (?q=&he=&vai=) để chia sẻ link được.
 */
const tenSite = useAppConfig().kpah.tenSite
useSeoMeta({
  title: 'Pokédex',
  description: `Pokédex ${tenSite}: ${POKEDEX.loai.length} loài Pokémon trong game — hệ, vai trò, tiến hoá, nơi xuất hiện và khắc chế.`,
})

const route = useRoute()
const router = useRouter()

const motQuery = (v: unknown) => (Array.isArray(v) ? v[0] : v) as string | undefined

const tim = ref(motQuery(route.query.q) ?? '')
const heChon = ref<string>(motQuery(route.query.he) ?? 'tat-ca')
const vaiChon = ref<string>(motQuery(route.query.vai) ?? 'tat-ca')

const heItems = [{ label: 'Mọi hệ', value: 'tat-ca' }, ...POKEDEX.he.map(h => ({ label: h.ten, value: String(h.id) }))]
const vaiItems = [{ label: 'Tất cả', value: 'tat-ca' }, ...DS_VAI_TRO.map(v => ({ label: TEN_VAI_TRO[v], value: v as string }))]

const demVai = Object.fromEntries(DS_VAI_TRO.map(v => [v, POKEDEX.loai.filter(l => l.vaiTro.includes(v)).length])) as Record<VaiTroLoai, number>

const dsTim = POKEDEX.loai.map(l => ({ l, khoa: `${l.dex} ${String(l.dex).padStart(3, '0')} ${boDau(l.ten)}` }))

const ketQua = computed(() => {
  const t = boDau(tim.value.trim()).replace(/^#/, '')
  const he = heChon.value === 'tat-ca' ? -1 : Number(heChon.value)
  const vai = vaiChon.value
  return dsTim
    .filter(x => (!t || x.khoa.includes(t))
      && (he < 0 || x.l.he.includes(he))
      && (vai === 'tat-ca' || x.l.vaiTro.includes(vai as VaiTroLoai)))
    .map(x => x.l)
})

// Đồng bộ bộ lọc lên URL (không thêm lịch sử mới mỗi lần gõ).
watch([tim, heChon, vaiChon], ([q, he, vai]) => {
  const query: Record<string, string> = {}
  if (q.trim()) query.q = q.trim()
  if (he !== 'tat-ca') query.he = he
  if (vai !== 'tat-ca') query.vai = vai
  router.replace({ query })
})

function xoaLoc() {
  tim.value = ''
  heChon.value = 'tat-ca'
  vaiChon.value = 'tat-ca'
}

const soDex = (d: number) => `#${String(d).padStart(3, '0')}`
</script>

<template>
  <div>
    <SitePageBanner
      title="Pokédex"
      eyebrow="Cẩm nang"
      :intro="`Toàn bộ ${POKEDEX.loai.length} loài Pokémon xuất hiện trong ${tenSite}: Pokémon khởi đầu, quái hoang dã, boss, thú cưỡi, pet và NPC.`"
    />

    <section class="py-12 sm:py-16" aria-labelledby="loc-title">
      <UContainer class="max-w-7xl">
        <h2 id="loc-title" class="sr-only">
          Bộ lọc
        </h2>
        <div class="rounded-3xl glass p-4 sm:p-6">
          <div class="grid gap-4 md:grid-cols-[1fr_14rem]">
            <UFormField label="Tìm theo tên hoặc số Pokédex" name="tim">
              <UInput
                v-model="tim"
                icon="i-lucide-search"
                placeholder="Vd. Pikachu, 25, eevee..."
                class="w-full"
                type="search"
                autocomplete="off"
              />
            </UFormField>
            <UFormField label="Hệ" name="he">
              <USelect v-model="heChon" :items="heItems" class="w-full" />
            </UFormField>
          </div>
          <fieldset class="mt-4">
            <legend class="mb-2 text-sm font-medium text-default">
              Vai trò trong game
            </legend>
            <div class="flex flex-wrap gap-2">
              <UButton
                v-for="v in vaiItems"
                :key="v.value"
                size="sm"
                :color="vaiChon === v.value ? 'primary' : 'neutral'"
                :variant="vaiChon === v.value ? 'solid' : 'outline'"
                :aria-pressed="vaiChon === v.value"
                @click="vaiChon = v.value"
              >
                {{ v.label }}
                <span v-if="v.value !== 'tat-ca'" class="opacity-70">{{ demVai[v.value as VaiTroLoai] }}</span>
              </UButton>
            </div>
          </fieldset>
        </div>

        <div class="mt-8 flex flex-wrap items-center justify-between gap-3">
          <p class="text-sm text-muted" role="status" aria-live="polite">
            Hiển thị <strong class="text-highlighted" data-test="so-loai">{{ ketQua.length }}</strong> / {{ POKEDEX.loai.length }} loài
          </p>
          <UButton
            v-if="tim || heChon !== 'tat-ca' || vaiChon !== 'tat-ca'"
            size="sm"
            color="neutral"
            variant="ghost"
            icon="i-lucide-x"
            @click="xoaLoc"
          >
            Xoá bộ lọc
          </UButton>
        </div>

        <ul v-if="ketQua.length" class="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6" aria-label="Danh sách Pokémon">
          <li v-for="l in ketQua" :key="l.dex">
            <NuxtLink
              :to="`/pokedex/${l.dex}`"
              class="group flex h-full flex-col items-center rounded-2xl glass p-3 text-center transition hover:-translate-y-0.5 hover:ring-1 hover:ring-primary/60 focus-visible:outline-2 focus-visible:outline-primary"
            >
              <span class="grid size-20 place-items-center rounded-xl bg-ink-950/60">
                <img
                  v-if="anhLoaiDex(l)"
                  :src="anhLoaiDex(l)"
                  width="40"
                  height="40"
                  alt=""
                  loading="lazy"
                  decoding="async"
                  class="pixelated size-16"
                >
              </span>
              <span class="num mt-2 text-xs text-dimmed">{{ soDex(l.dex) }}</span>
              <span class="font-semibold text-highlighted group-hover:text-primary">{{ l.ten }}</span>
              <span class="mt-2 flex flex-wrap justify-center gap-1">
                <PokeHeBadge v-for="h in l.he" :key="h" :he="h" size="sm" />
              </span>
              <span class="mt-2 text-[11px] text-muted">
                {{ l.vaiTro.map(v => TEN_VAI_TRO[v]).join(' · ') }}
              </span>
            </NuxtLink>
          </li>
        </ul>
        <div v-else class="mt-10 rounded-2xl glass p-10 text-center text-muted">
          Không có Pokémon nào khớp bộ lọc.
        </div>

        <p class="mt-10 text-center text-sm text-muted">
          Mới chơi? Xem
          <NuxtLink to="/huong-dan" class="text-primary hover:underline">hướng dẫn tân thủ</NuxtLink>
          để chọn Pokémon khởi đầu và học bảng khắc chế hệ.
        </p>
      </UContainer>
    </section>
  </div>
</template>
