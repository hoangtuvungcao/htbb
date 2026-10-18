<script setup lang="ts">
/**
 * Pokédex: mọi loài có trong game, lọc theo tên/số, hệ và vai trò.
 * Dữ liệu tĩnh (shared/data/pokedex.json) -> SSR đủ danh sách, lọc ngay trên trình duyệt.
 * Bộ lọc ghi vào query (?q=&he=&vai=) để chia sẻ link được.
 */
const tenSite = useAppConfig().htbb.tenSite
useSeoMeta({
  title: 'Pokédex',
  description: `Pokédex ${tenSite}: ${POKEDEX.loai.length} loài Pokémon trong game — hệ, vai trò, tiến hoá, nơi xuất hiện và khắc chế.`,
})

const route = useRoute()
const router = useRouter()

const motQuery = (v: unknown) => (Array.isArray(v) ? v[0] : v) as string | undefined

const tim = ref(motQuery(route.query.q) ?? '')
const heChon = ref<string>(motQuery(route.query.he) ?? 'tat-ca')
const gdChon = ref<string>(motQuery(route.query.gd) ?? 'tat-ca')
const xep = ref<string>(motQuery(route.query.xep) ?? 'dex')
const vaiChon = ref<string>(motQuery(route.query.vai) ?? 'tat-ca')

const heItems = [{ label: 'Mọi hệ', value: 'tat-ca' }, ...POKEDEX.he.map(h => ({ label: h.ten, value: String(h.id) }))]
const vaiItems = [{ label: 'Tất cả', value: 'tat-ca' }, ...DS_VAI_TRO.map(v => ({ label: TEN_VAI_TRO[v], value: v as string }))]

/** Giai đoạn tiến hoá của một loài: 1, 2, 3 (nhánh Eevee = 2), 0 = không tiến hoá. */
function giaiDoanCua(l: LoaiDex): number {
  const th = l.tienHoa
  if (!th) return 0
  const i = th.buoc.findIndex(b => b.dex === l.dex)
  if (i >= 0) return th.buoc.length > 1 ? Math.min(3, i + 1) : 0
  if (th.nhanh.some(b => b.dex === l.dex)) return 2
  return 1
}
const gdItems = [
  { label: 'Mọi giai đoạn', value: 'tat-ca' },
  { label: 'Dạng 1 (cơ bản)', value: '1' },
  { label: 'Dạng 2', value: '2' },
  { label: 'Dạng 3 (cuối)', value: '3' },
  { label: 'Không tiến hoá', value: '0' },
]
const xepItems = [
  { label: 'Số Pokédex', value: 'dex' },
  { label: 'Tên A → Z', value: 'ten' },
]

const demVai = Object.fromEntries(DS_VAI_TRO.map(v => [v, POKEDEX.loai.filter(l => l.vaiTro.includes(v)).length])) as Record<VaiTroLoai, number>

const dsTim = POKEDEX.loai.map(l => ({ l, gd: giaiDoanCua(l), khoa: `${l.dex} ${String(l.dex).padStart(3, '0')} ${boDau(l.ten)} ${l.he.map(h => boDau(tenHe(h))).join(' ')}` }))

const ketQua = computed(() => {
  const t = boDau(tim.value.trim()).replace(/^#/, '')
  const he = heChon.value === 'tat-ca' ? -1 : Number(heChon.value)
  const vai = vaiChon.value
  const gd = gdChon.value === 'tat-ca' ? -1 : Number(gdChon.value)
  const ra = dsTim
    .filter(x => (!t || x.khoa.includes(t))
      && (he < 0 || x.l.he.includes(he))
      && (gd < 0 || x.gd === gd)
      && (vai === 'tat-ca' || x.l.vaiTro.includes(vai as VaiTroLoai)))
    .map(x => x.l)
  return xep.value === 'ten' ? [...ra].sort((a, b) => a.ten.localeCompare(b.ten)) : ra
})

// Đồng bộ bộ lọc lên URL (không thêm lịch sử mới mỗi lần gõ).
watch([tim, heChon, vaiChon, gdChon, xep], ([q, he, vai, gd, x]) => {
  const query: Record<string, string> = {}
  if (q.trim()) query.q = q.trim()
  if (he !== 'tat-ca') query.he = he
  if (vai !== 'tat-ca') query.vai = vai
  if (gd !== 'tat-ca') query.gd = gd
  if (x !== 'dex') query.xep = x
  router.replace({ query })
})

function xoaLoc() {
  tim.value = ''
  heChon.value = 'tat-ca'
  vaiChon.value = 'tat-ca'
  gdChon.value = 'tat-ca'
  xep.value = 'dex'
}
const dangLoc = computed(() => !!tim.value || heChon.value !== 'tat-ca' || vaiChon.value !== 'tat-ca' || gdChon.value !== 'tat-ca')

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
        <div class="rounded-3xl glass ring-gilded p-4 sm:p-6">
          <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-[1fr_12rem_12rem_10rem]">
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
            <UFormField label="Giai đoạn tiến hoá" name="gd">
              <USelect v-model="gdChon" :items="gdItems" class="w-full" />
            </UFormField>
            <UFormField label="Sắp xếp" name="xep">
              <USelect v-model="xep" :items="xepItems" class="w-full" />
            </UFormField>
          </div>
          <div class="mt-4 flex flex-wrap gap-1.5" role="group" aria-label="Lọc nhanh theo hệ">
            <button
              v-for="h in POKEDEX.he"
              :key="h.id"
              type="button"
              class="rounded-full transition"
              :class="heChon === String(h.id) ? 'scale-110 ring-2 ring-[var(--poke-outline)] ring-offset-1' : heChon !== 'tat-ca' ? 'opacity-45 hover:opacity-100' : 'hover:-translate-y-0.5'"
              :aria-pressed="heChon === String(h.id)"
              @click="heChon = heChon === String(h.id) ? 'tat-ca' : String(h.id)"
            >
              <PokeHeBadge :he="h.id" size="sm" />
            </button>
          </div>
          <fieldset class="mt-4">
            <legend class="mb-2 font-display text-sm font-bold text-default">
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
            v-if="dangLoc"
            size="sm"
            color="neutral"
            variant="ghost"
            icon="i-lucide-x"
            @click="xoaLoc"
          >
            Xoá bộ lọc
          </UButton>
        </div>

        <ul v-if="ketQua.length" class="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-3 sm:gap-4 md:grid-cols-4 lg:grid-cols-6" aria-label="Danh sách Pokémon">
          <li v-for="(l, i) in ketQua" :key="l.dex">
            <NuxtLink
              :to="`/pokedex/${l.dex}`"
              class="group flex h-full flex-col overflow-hidden rounded-2xl glass text-center transition hover:-translate-y-1 hover:border-[color-mix(in_oklab,var(--ui-primary)_55%,transparent)] focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary"
            >
              <span class="relative block">
                <PokeSan :dex="l.dex" :he="l.he" :size="150" :delay="(i % 7) * 0.13" class="w-full" />
                <span class="absolute left-2 top-2 rounded-full bg-white/85 px-1.5 py-1 font-pixel text-[9px] leading-none text-navy-800 shadow-sm dark:bg-navy-950/75 dark:text-navy-100">{{ soDex(l.dex) }}</span>
              </span>
              <span class="mt-2.5 px-2 font-display text-lg font-bold leading-tight text-highlighted group-hover:text-primary">{{ l.ten }}</span>
              <span class="mt-1.5 flex flex-wrap justify-center gap-1 px-2">
                <PokeHeBadge v-for="h in l.he" :key="h" :he="h" size="sm" />
              </span>
              <span class="mt-auto px-2 pb-3 pt-2 text-[11px] font-semibold text-muted">
                {{ l.vaiTro.map(v => TEN_VAI_TRO[v]).join(' · ') }}
              </span>
            </NuxtLink>
          </li>
        </ul>
        <div v-else class="mt-10 flex flex-col items-center gap-4 p-6 text-center">
          <PokeBall wiggle class="size-14" />
          <div class="dialog-box max-w-md">
            <p>Không có Pokémon nào khớp bộ lọc. Có lẽ chúng đang trốn trong bụi cỏ cao...</p>
          </div>
        </div>

        <p class="mt-10 text-center text-sm text-muted">
          Mới chơi? Xem
          <NuxtLink to="/huong-dan" class="text-primary hover:underline">hướng dẫn tân thủ</NuxtLink>,
          <NuxtLink to="/cam-nang/he-khac-che" class="text-primary hover:underline">bảng khắc hệ</NuxtLink>,
          <NuxtLink to="/cam-nang/tien-hoa" class="text-primary hover:underline">tiến hoá</NuxtLink> và
          <NuxtLink to="/cam-nang/pokedex-shiny" class="text-primary hover:underline">săn Shiny</NuxtLink>.
        </p>
      </UContainer>
    </section>
  </div>
</template>
