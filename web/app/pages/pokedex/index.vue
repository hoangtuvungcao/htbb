<script setup lang="ts">
/**
 * Pokédex: toàn bộ 1.025 loài (bảng data_loai qua GET /api/pokedex), lọc theo tên/số, hệ, thế hệ, bậc, trạng thái
 * trong game và vai trò. Loài chưa mở trong game hiện "Chưa xuất hiện". CSDL chưa có dữ liệu đầy đủ thì rơi về
 * Pokédex tĩnh (shared/data/pokedex.json: các loài đang có trong game).
 * Bộ lọc ghi vào query (?q=&he=&gen=&bac=&tt=&vai=&xep=) để chia sẻ link được. Hiện theo trang 60 thẻ.
 */
import type { VaiTroLoai } from '~~/shared/utils/pokedex'
import type { BoLocLoai, LoaiTomTat } from '~~/shared/utils/loai'

const tenSite = useAppConfig().htbb.tenSite

const { data: api } = await useFetch('/api/pokedex', {
  key: 'pokedex-day-du',
  default: () => ({ coDuLieu: false, loai: [] as LoaiTomTat[] }),
})

interface Muc extends LoaiTomTat { vaiTro: VaiTroLoai[] }

const tinh = new Map(POKEDEX.loai.map(l => [l.dex, l]))
const ds = computed<Muc[]>(() => {
  if (!api.value?.coDuLieu) {
    return POKEDEX.loai.map(l => ({ dex: l.dex, ten: l.ten, he: l.he, bac: 0, theHe: 0, bat: true, nhom: 0, vaiTro: l.vaiTro }))
  }
  return api.value.loai.map(x => ({ ...x, vaiTro: tinh.get(x.dex)?.vaiTro ?? [] }))
})
const dayDu = computed(() => !!api.value?.coDuLieu)
const soMo = computed(() => ds.value.filter(l => l.bat).length)

useSeoMeta({
  title: 'Pokédex',
  description: () => `Pokédex ${tenSite}: ${ds.value.length} loài Pokémon — hệ, chỉ số nền, bậc hiếm, chuỗi tiến hoá, chiêu bạn đồng hành, nơi sống và khắc hệ.`,
})

const route = useRoute()
const router = useRouter()
const motQuery = (v: unknown) => (Array.isArray(v) ? v[0] : v) as string | undefined

const tim = ref(motQuery(route.query.q) ?? '')
const heChon = ref<string>(motQuery(route.query.he) ?? 'tat-ca')
const genChon = ref<string>(motQuery(route.query.gen) ?? 'tat-ca')
const bacChon = ref<string>(motQuery(route.query.bac) ?? 'tat-ca')
const ttChon = ref<string>(motQuery(route.query.tt) ?? 'tat-ca')
const xep = ref<string>(motQuery(route.query.xep) ?? 'dex')
const vaiChon = ref<string>(motQuery(route.query.vai) ?? 'tat-ca')

const heItems = [{ label: 'Mọi hệ', value: 'tat-ca' }, ...POKEDEX.he.map(h => ({ label: h.ten, value: String(h.id) }))]
const genItems = [{ label: 'Mọi thế hệ', value: 'tat-ca' }, ...Object.entries(TEN_THE_HE).map(([g, ten]) => ({ label: `Thế hệ ${g} · ${ten}`, value: g }))]
const bacItems = [{ label: 'Mọi bậc', value: 'tat-ca' }, ...Object.entries(BAC_LOAI).map(([b, v]) => ({ label: `${v.ma} · ${v.ten}`, value: b }))]
const ttItems = [
  { label: 'Tất cả', value: 'tat-ca' },
  { label: 'Đã xuất hiện', value: 'da-mo' },
  { label: 'Chưa xuất hiện', value: 'chua-mo' },
]
const xepItems = [
  { label: 'Số Pokédex', value: 'dex' },
  { label: 'Tên A → Z', value: 'ten' },
]
const vaiItems = [{ label: 'Tất cả', value: 'tat-ca' }, ...DS_VAI_TRO.map(v => ({ label: TEN_VAI_TRO[v], value: v as string }))]
const demVai = computed(() => Object.fromEntries(DS_VAI_TRO.map(v => [v, ds.value.filter(l => l.vaiTro.includes(v)).length])) as Record<VaiTroLoai, number>)

const ketQua = computed(() => {
  const ra = locLoai(ds.value, {
    tim: tim.value,
    he: heChon.value === 'tat-ca' ? -1 : Number(heChon.value),
    theHe: genChon.value === 'tat-ca' ? 0 : Number(genChon.value),
    bac: bacChon.value === 'tat-ca' ? 0 : Number(bacChon.value),
    trangThai: ttChon.value as BoLocLoai['trangThai'],
  }, boDau).filter(l => vaiChon.value === 'tat-ca' || l.vaiTro.includes(vaiChon.value as VaiTroLoai))
  return xep.value === 'ten' ? [...ra].sort((a, b) => a.ten.localeCompare(b.ten)) : ra
})

const MOI_TRANG = 60
const hienSo = ref(MOI_TRANG)
const dangHien = computed(() => ketQua.value.slice(0, hienSo.value))

// Đồng bộ bộ lọc lên URL (không thêm lịch sử mới mỗi lần gõ); đổi lọc thì về trang đầu.
watch([tim, heChon, genChon, bacChon, ttChon, vaiChon, xep], ([q, he, gen, bac, tt, vai, x]) => {
  hienSo.value = MOI_TRANG
  const query: Record<string, string> = {}
  if (q.trim()) query.q = q.trim()
  if (he !== 'tat-ca') query.he = he
  if (gen !== 'tat-ca') query.gen = gen
  if (bac !== 'tat-ca') query.bac = bac
  if (tt !== 'tat-ca') query.tt = tt
  if (vai !== 'tat-ca') query.vai = vai
  if (x !== 'dex') query.xep = x
  router.replace({ query })
})

function xoaLoc() {
  tim.value = ''
  heChon.value = 'tat-ca'
  genChon.value = 'tat-ca'
  bacChon.value = 'tat-ca'
  ttChon.value = 'tat-ca'
  vaiChon.value = 'tat-ca'
  xep.value = 'dex'
}
const dangLoc = computed(() => !!tim.value || [heChon, genChon, bacChon, ttChon, vaiChon].some(r => r.value !== 'tat-ca'))
</script>

<template>
  <div>
    <SitePageBanner
      title="Pokédex"
      eyebrow="Cẩm nang"
      :intro="dayDu
        ? `Toàn bộ ${ds.length} loài Pokémon: ${soMo} loài đã xuất hiện trong ${tenSite}, các thế hệ khác sẽ mở dần theo đợt.`
        : `Toàn bộ ${ds.length} loài Pokémon xuất hiện trong ${tenSite}: Pokémon khởi đầu, Pokémon hoang dã, boss, thú cưỡi, pet và NPC.`"
    />

    <section class="py-12 sm:py-16" aria-labelledby="loc-title">
      <UContainer class="max-w-7xl">
        <h2 id="loc-title" class="sr-only">
          Bộ lọc
        </h2>
        <div class="rounded-3xl glass ring-gilded p-4 sm:p-6">
          <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-[1fr_11rem_13rem_13rem_11rem]">
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
            <UFormField v-if="dayDu" label="Thế hệ" name="gen">
              <USelect v-model="genChon" :items="genItems" class="w-full" />
            </UFormField>
            <UFormField v-if="dayDu" label="Bậc hiếm" name="bac">
              <USelect v-model="bacChon" :items="bacItems" class="w-full" />
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
          <div class="mt-4 grid gap-4 md:grid-cols-2">
            <fieldset v-if="dayDu">
              <legend class="mb-2 font-display text-sm font-bold text-default">
                Trong game
              </legend>
              <div class="flex flex-wrap gap-2">
                <UButton
                  v-for="t in ttItems"
                  :key="t.value"
                  size="sm"
                  :color="ttChon === t.value ? 'primary' : 'neutral'"
                  :variant="ttChon === t.value ? 'solid' : 'outline'"
                  :aria-pressed="ttChon === t.value"
                  @click="ttChon = t.value"
                >
                  {{ t.label }}
                </UButton>
              </div>
            </fieldset>
            <fieldset>
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
        </div>

        <div class="mt-8 flex flex-wrap items-center justify-between gap-3">
          <p class="text-sm text-muted" role="status" aria-live="polite">
            Hiển thị <strong class="text-highlighted" data-test="so-loai">{{ ketQua.length }}</strong> / {{ ds.length }} loài
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
          <li v-for="(l, i) in dangHien" :key="l.dex">
            <NuxtLink
              :to="`/pokedex/${l.dex}`"
              class="group flex h-full flex-col overflow-hidden rounded-2xl glass text-center transition hover:-translate-y-1 hover:border-[color-mix(in_oklab,var(--ui-primary)_55%,transparent)] focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary"
            >
              <span class="relative block" :class="l.bat ? '' : 'grayscale-[.7] opacity-75'">
                <PokeSan :dex="l.dex" :he="l.he" :size="150" :delay="(i % 7) * 0.13" class="w-full" />
                <span class="absolute left-2 top-2 rounded-full bg-white/85 px-1.5 py-1 font-pixel text-[9px] leading-none text-navy-800 shadow-sm dark:bg-navy-950/75 dark:text-navy-100">{{ soDex(l.dex) }}</span>
                <span
                  v-if="l.bac"
                  class="absolute right-2 top-2 rounded-full px-1.5 py-1 font-pixel text-[9px] leading-none text-navy-950 shadow-sm"
                  :style="{ backgroundColor: bacLoai(l.bac).mau }"
                  :title="bacLoai(l.bac).ten"
                >{{ bacLoai(l.bac).ma }}</span>
              </span>
              <span class="mt-2.5 px-2 font-display text-lg font-bold leading-tight text-highlighted group-hover:text-primary">{{ l.ten }}</span>
              <span class="mt-1.5 flex flex-wrap justify-center gap-1 px-2">
                <PokeHeBadge v-for="h in l.he" :key="h" :he="h" size="sm" />
              </span>
              <span class="mt-auto px-2 pb-3 pt-2 text-[11px] font-semibold text-muted">
                <template v-if="!l.bat">Chưa xuất hiện</template>
                <template v-else-if="l.vaiTro.length">{{ l.vaiTro.map(v => TEN_VAI_TRO[v]).join(' · ') }}</template>
                <template v-else>Hoang dã · {{ TEN_THE_HE[l.theHe] }}</template>
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
        <div v-if="ketQua.length > dangHien.length" class="mt-8 flex justify-center">
          <UButton color="neutral" variant="outline" icon="i-lucide-chevrons-down" @click="hienSo += MOI_TRANG">
            Xem thêm ({{ ketQua.length - dangHien.length }} loài nữa)
          </UButton>
        </div>

        <p class="mt-10 text-center text-sm text-muted">
          Mới chơi? Xem
          <NuxtLink to="/huong-dan" class="text-primary hover:underline">hướng dẫn tân thủ</NuxtLink>,
          <NuxtLink to="/cam-nang/he-khac-che" class="text-primary hover:underline">bảng khắc hệ</NuxtLink>,
          <NuxtLink to="/cam-nang/tien-hoa" class="text-primary hover:underline">tiến hoá</NuxtLink> và
          <NuxtLink to="/cam-nang/pokedex-shiny" class="text-primary hover:underline">săn Shiny</NuxtLink>.
          Hình Pokémon: PMDCollab (CC BY-NC 4.0) — xem <NuxtLink to="/giay-phep" class="text-primary hover:underline">giấy phép</NuxtLink>.
        </p>
      </UContainer>
    </section>
  </div>
</template>
