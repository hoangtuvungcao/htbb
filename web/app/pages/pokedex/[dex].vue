<script setup lang="ts">
/**
 * Chi tiết một loài. Hai nguồn:
 *  - CSDL (GET /api/pokedex/[dex], bảng data_loai* — đủ 1.025 loài): mô tả Pokédex (data_loai.mo_ta, tự viết),
 *    chỉ số nền, bậc, cả họ tiến hoá + điều kiện,
 *    4 chiêu bạn đồng hành, nơi sống, nhóm trứng, trạng thái sprite + ghi công;
 *  - Pokédex tĩnh (shared/data/pokedex.json, loài đang có trong game): vai trò, nơi gặp quái / boss, chiêu dòng người
 *    chơi, thú cưỡi / pet / NPC.
 * Có ít nhất một nguồn thì dựng trang; không có cả hai -> 404.
 */
import type { ChiTietLoai, LoaiTomTat } from '~~/shared/utils/loai'

definePageMeta({ key: r => r.path })

const route = useRoute()
const dex = docSoDex(route.params.dex) ?? Number.NaN
const l = Number.isInteger(dex) ? loaiTheoDex(dex) : undefined

const { data: ct } = await useFetch<ChiTietLoai | null>(`/api/pokedex/${dex}`, {
  key: `pokedex-loai-${dex}`,
  default: () => null,
  // 404 / CSDL chưa có dữ liệu: dùng Pokédex tĩnh
  onResponseError: () => {},
})
const { data: dsApi } = await useFetch('/api/pokedex', {
  key: 'pokedex-day-du',
  default: () => ({ coDuLieu: false, loai: [] as LoaiTomTat[] }),
})
if (!l && !ct.value) {
  throw createError({ statusCode: 404, statusMessage: 'Không có Pokémon này trong Pokédex', fatal: true })
}
const d = computed(() => ct.value?.loai ?? null)
const ten = computed(() => d.value?.ten ?? l?.ten ?? soDex(dex))
const he = computed<number[]>(() => d.value?.he ?? l?.he ?? [0])

const tenSite = useAppConfig().htbb.tenSite
useSeoMeta({
  title: () => `${ten.value} ${soDex(dex)} — Pokédex`,
  description: () => `${ten.value} (${he.value.map(tenHe).join(' / ')}) trong ${tenSite}: `
    + (d.value ? `bậc ${bacLoai(d.value.bac).ma} ${bacLoai(d.value.bac).ten.toLowerCase()}, thế hệ ${d.value.theHe}, tổng chỉ số ${d.value.tong}${d.value.bat ? '' : ', chưa xuất hiện trong game'}. ` : '')
    + (d.value?.moTa ? `${d.value.moTa} ` : '')
    + 'Khắc hệ, tiến hoá, chiêu thức, nơi sống.',
})

const phongThu = computed(() => bangPhongThu(he.value))
const moTaHeSo = (g: number) => g > 100 ? 'Bị khắc (nhận thêm sát thương)' : g < 100 ? 'Kháng (nhận ít sát thương)' : 'Bình thường'
const mauHeSo = (g: number) => g > 100 ? 'text-error' : g < 100 ? 'text-success' : 'text-muted'

// loài trước / sau: theo danh sách đầy đủ nếu có, không thì theo Pokédex tĩnh
const lanCan = computed(() => {
  const ds = dsApi.value?.coDuLieu ? dsApi.value.loai : POKEDEX.loai
  const i = ds.findIndex(x => x.dex === dex)
  return { truoc: i > 0 ? ds[i - 1] : undefined, sau: i >= 0 ? ds[i + 1] : undefined }
})

// Tiến hoá: chuỗi đầy đủ từ CSDL (theo tầng, có nhánh); không có thì dùng bản tĩnh
const chuoi = computed(() => ct.value ? dungChuoiTienHoa(ct.value.ho.thanhVien, ct.value.ho.canh, ct.value.loai.dexGoc) : [])
const coChuoi = computed(() => chuoi.value.length > 1)
const buocTienHoa = (l?.tienHoa?.buoc ?? []).map(b => ({ ...b, loai: loaiTheoDex(b.dex) }))
const nhanhTienHoa = (l?.tienHoa?.nhanh ?? []).map(b => ({ ...b, loai: loaiTheoDex(b.dex) }))
const tieuDeTienHoa: Record<string, string> = {
  cap: 'Tiến hoá theo cấp (tự động khi đạt cấp)',
  eevee: `Eevee chọn nhánh bằng đá tiến hoá từ cấp ${POKEDEX.capNhanhEevee}`,
  pet: 'Pet tiến hoá theo cấp pet (nâng cấp pet)',
  ho: 'Họ tiến hoá — các dạng có trong game',
}

/** LoaiPokemon.phanTramGiaiDoan: dạng 2 / dạng 3 / nhánh Eevee được cộng HP + tấn công. */
const thuongGiaiDoan = !l ? 0 : l.tienHoa?.kieu === 'eevee' && l.dex !== 133
  ? POKEDEX.phanTramGiaiDoan.eevee
  : l.giaiDoan === 2 ? POKEDEX.phanTramGiaiDoan[2] : l.giaiDoan === 3 ? POKEDEX.phanTramGiaiDoan[3] : 0

const daCuaLoai = POKEDEX.daTienHoa.find(x => x.dex === dex)
const fmt = (n: number) => n.toLocaleString('vi-VN')
const gioiTinh = (r: number) => r < 0 ? 'Vô tính' : r === 0 ? '100% đực' : r === 8 ? '100% cái' : `${(r * 12.5).toLocaleString('vi-VN')}% cái`
const chanDung = computed(() => (l ? anhLoaiDex(l) : '') || chanDungLoai(dex))

// Học chiêu theo loài: bảng data_learnset của game (/api/learnset/<dex>); CSDL chưa có bảng / không phải Pokémon người
// chơi -> giữ danh sách chiêu tĩnh của dòng bên dưới.
const { data: learnset } = await useFetch<DongLearnset[] | null>(`/api/learnset/${dex}`, { key: `htbb-learnset-${dex}`, default: () => null })
const chieuLoai = computed(() => learnset.value?.length ? chieuCuaLoai(learnset.value, dex) : [])
const moTaCach = (c: ChieuCuaLoai) => c.cach === 'cap'
  ? `Tự học ở cấp ${c.capHoc}`
  : c.cach === 'npc'
    ? `NPC Blaziken · cấp ${c.capHoc} · ${fmt(c.giaXu)} xu`
    : `Khi tiến hoá thành ${loaiTheoDex(c.loaiHoc)?.ten ?? `#${c.loaiHoc}`} (cấp ${c.capHoc})`
</script>

<template>
  <div>
    <SitePageBanner
      :title="`${ten} ${soDex(dex)}`"
      eyebrow="Pokédex"
      :trail="[{ label: 'Pokédex', to: '/pokedex' }]"
    />

    <UContainer class="max-w-6xl py-12 sm:py-16">
      <!-- Tổng quan -->
      <section class="grid gap-6 lg:grid-cols-[18rem_1fr] lg:items-start" aria-labelledby="tong-quan">
        <div class="dex-card self-start">
          <div class="mb-2 mt-1 flex items-center justify-end gap-1.5 pr-1" aria-hidden="true">
            <span class="size-2.5 rounded-full border-2 border-[var(--poke-outline)] bg-pokered-300" />
            <span class="size-2.5 rounded-full border-2 border-[var(--poke-outline)] bg-pikachu-400" />
            <span class="size-2.5 rounded-full border-2 border-[var(--poke-outline)] bg-grass-400" />
          </div>
          <div class="dex-screen p-3">
            <div class="relative overflow-hidden rounded-xl">
              <PokeSan :dex="dex" :he="he" :size="232" :label="ten" class="w-full rounded-xl" :class="d && !d.bat ? 'grayscale-[.6]' : ''" />
              <span class="absolute left-3 top-3 rounded-full bg-white/80 px-2 py-1 font-pixel text-[10px] leading-none text-navy-800 dark:bg-navy-950/70 dark:text-navy-100">{{ soDex(dex) }}</span>
              <span
                v-if="d"
                class="absolute right-3 top-3 rounded-full px-2 py-1 font-pixel text-[10px] leading-none text-navy-950"
                :style="{ backgroundColor: bacLoai(d.bac).mau }"
              >{{ bacLoai(d.bac).ma }}</span>
            </div>
            <p class="mt-3 text-center font-display text-2xl font-extrabold text-highlighted">
              {{ ten }}
            </p>
            <div class="mt-1 flex justify-center gap-1.5">
              <PokeHeBadge v-for="h in he" :key="h" :he="h" />
            </div>
            <p v-if="d && !d.bat" class="mt-3 rounded-xl bg-elevated p-2 text-center text-sm font-semibold text-muted">
              Chưa xuất hiện trong game — sẽ mở theo đợt.
            </p>
            <div v-if="l && l.anh && l.anhShiny" class="mt-4 flex items-center justify-center gap-3 rounded-xl bg-pikachu-50 p-2 text-sm font-semibold text-pikachu-900 dark:bg-pikachu-950/40 dark:text-pikachu-200">
              <PokeSan :dex="dex" shiny :he="he" :size="56" :label="`${ten} shiny`" class="w-14 rounded-lg" />
              Có bản shiny trong game
            </div>
          </div>
          <div class="mt-2 flex items-center justify-between px-2 pb-0.5" aria-hidden="true">
            <span class="h-2 w-16 rounded-full bg-pokered-800/60" />
            <span class="grid size-7 place-items-center rounded-full border-2 border-[var(--poke-outline)] bg-navy-800">
              <span class="h-1 w-3 rounded bg-navy-400" />
            </span>
          </div>
        </div>

        <div class="rounded-3xl glass ring-gilded p-6">
          <h2 id="tong-quan" class="section-title text-2xl text-highlighted">
            Tổng quan
          </h2>
          <p v-if="d?.moTa" class="mt-3 text-pretty leading-relaxed text-default" data-mo-ta>
            {{ d.moTa }}
          </p>
          <dl class="mt-5 grid gap-4 sm:grid-cols-2">
            <div>
              <dt class="text-sm text-muted">
                Số Pokédex
              </dt>
              <dd class="text-lg font-semibold tabular-nums text-highlighted">
                {{ soDex(dex) }}
              </dd>
            </div>
            <div>
              <dt class="text-sm text-muted">
                Hệ
              </dt>
              <dd class="mt-1 flex flex-wrap gap-1.5">
                <PokeHeBadge v-for="h in he" :key="h" :he="h" />
              </dd>
            </div>
            <template v-if="d">
              <div>
                <dt class="text-sm text-muted">
                  Bậc hiếm
                </dt>
                <dd class="font-semibold text-highlighted">
                  <span class="mr-1 inline-block size-2.5 rounded-full align-middle" :style="{ backgroundColor: bacLoai(d.bac).mau }" aria-hidden="true" />
                  {{ bacLoai(d.bac).ma }} · {{ bacLoai(d.bac).ten }}
                  <span v-if="d.nhom" class="block text-sm font-normal text-muted">{{ TEN_NHOM_LOAI[d.nhom] }}</span>
                </dd>
              </div>
              <div>
                <dt class="text-sm text-muted">
                  Thế hệ
                </dt>
                <dd class="font-semibold text-highlighted">
                  {{ d.theHe }} · {{ TEN_THE_HE[d.theHe] }}
                  <span class="block text-sm font-normal text-muted">{{ d.bat ? 'Đã xuất hiện trong game' : 'Chưa xuất hiện' }}</span>
                </dd>
              </div>
              <div>
                <dt class="text-sm text-muted">
                  Thu phục
                </dt>
                <dd class="text-highlighted">
                  <template v-if="d.batDuoc">
                    Tỉ lệ gốc {{ (d.gocBat / 10).toLocaleString('vi-VN') }}%<span v-if="d.chiSuKien" class="text-muted"> · chỉ ở sự kiện / đúng giờ, thời tiết</span>
                  </template>
                  <template v-else>
                    Chỉ sau trận đột kích huyền thoại
                  </template>
                </dd>
              </div>
              <div>
                <dt class="text-sm text-muted">
                  Nhóm trứng
                </dt>
                <dd class="text-highlighted">
                  {{ d.nhomTrung.join(' · ') || '—' }}
                </dd>
              </div>
              <div>
                <dt class="text-sm text-muted">
                  Môi trường sống
                </dt>
                <dd class="text-highlighted">
                  {{ TEN_MOI_TRUONG[d.moiTruong] ?? '—' }}
                </dd>
              </div>
              <div>
                <dt class="text-sm text-muted">
                  Kích thước · giới tính
                </dt>
                <dd class="text-highlighted">
                  {{ (d.chieuCao / 10).toLocaleString('vi-VN') }} m · {{ (d.canNang / 10).toLocaleString('vi-VN') }} kg · {{ gioiTinh(d.tiLeCai) }}
                </dd>
              </div>
            </template>
            <div v-if="l">
              <dt class="text-sm text-muted">
                Vai trò trong game
              </dt>
              <dd class="mt-1 flex flex-wrap gap-1.5">
                <UBadge v-for="v in l.vaiTro" :key="v" :color="v === 'boss' ? 'error' : v === 'khoi-dau' ? 'primary' : 'neutral'" variant="subtle">
                  {{ TEN_VAI_TRO[v] }}
                </UBadge>
              </dd>
            </div>
            <div v-if="l?.dong">
              <dt class="text-sm text-muted">
                Dòng người chơi
              </dt>
              <dd class="font-semibold text-highlighted">
                {{ l.dong.ten }}
                <span v-if="thuongGiaiDoan" class="block text-sm font-normal text-muted">
                  +{{ thuongGiaiDoan }}% HP và tấn công nhờ tiến hoá
                </span>
              </dd>
            </div>
            <div v-if="daCuaLoai">
              <dt class="text-sm text-muted">
                Cách có được
              </dt>
              <dd class="text-highlighted">
                Eevee cấp {{ POKEDEX.capNhanhEevee }}+ dùng <strong>{{ daCuaLoai.ten }}</strong>
              </dd>
            </div>
          </dl>
          <p class="mt-6 text-sm text-muted">
            <NuxtLink to="/cam-nang/he-khac-che" class="text-primary hover:underline">Xem bảng khắc hệ đầy đủ</NuxtLink>
            · Chiêu cùng hệ với người dùng được cộng ×{{ (POKEDEX.stabPhanTram / 100).toLocaleString('vi-VN') }} (STAB).
          </p>
        </div>
      </section>

      <!-- Khắc chế -->
      <section class="mt-10 rounded-3xl glass p-6" aria-labelledby="khac-che">
        <h2 id="khac-che" class="section-title text-2xl text-highlighted">
          Sát thương nhận vào theo hệ
        </h2>
        <p class="mt-2 text-sm text-muted">
          Hệ số trong game (đã làm dịu so với bản gốc: không có miễn nhiễm, tối đa ×2).
          Hệ số gốc ghi trong ngoặc.
        </p>
        <div class="mt-5 divide-y divide-default/60">
          <div v-for="n in phongThu" :key="n.heSo" class="grid gap-2 py-3 sm:grid-cols-[12rem_1fr] sm:items-center">
            <p>
              <span class="num text-lg font-bold" :class="mauHeSo(n.heSo)">{{ hienHeSo(n.heSo) }}</span>
              <span class="ml-1 text-xs text-dimmed">(gốc {{ hienHeSo(n.chinhXac) }})</span>
              <span class="block text-xs text-muted">{{ moTaHeSo(n.heSo) }}</span>
            </p>
            <ul class="flex flex-wrap gap-1.5" :aria-label="`Hệ gây ${hienHeSo(n.heSo)}`">
              <li v-for="h in n.he" :key="h">
                <PokeHeBadge :he="h" />
              </li>
            </ul>
          </div>
        </div>
      </section>

      <!-- Chỉ số nền (CSDL) -->
      <section v-if="d" class="mt-10 rounded-3xl glass p-6" aria-labelledby="chi-so">
        <h2 id="chi-so" class="section-title text-2xl text-highlighted">
          Chỉ số nền
        </h2>
        <p class="mt-2 text-sm text-muted">
          Chỉ số nền theo loài (dữ liệu chuẩn). Dùng cho Pokémon trong Hộp PC, bạn đồng hành và Đấu trường Bảo Bối —
          không cộng vào chỉ số của người chơi.
        </p>
        <dl class="mt-5 space-y-2">
          <div v-for="c in TEN_CHI_SO" :key="c.ma" class="grid grid-cols-[7rem_3rem_1fr] items-center gap-3">
            <dt class="text-sm text-muted">
              {{ c.ten }}
            </dt>
            <dd class="num text-right font-semibold text-highlighted">
              {{ d.cs[c.ma] }}
            </dd>
            <dd class="h-2.5 overflow-hidden rounded-full bg-elevated" aria-hidden="true">
              <span class="block h-full rounded-full" :style="{ width: `${phanTramChiSo(d.cs[c.ma])}%`, backgroundColor: mauHe(he[0] ?? 0).backgroundColor }" />
            </dd>
          </div>
          <div class="grid grid-cols-[7rem_3rem_1fr] items-center gap-3 border-t border-default/60 pt-2">
            <dt class="text-sm font-semibold text-default">
              Tổng
            </dt>
            <dd class="num text-right font-bold text-highlighted">
              {{ d.tong }}
            </dd>
          </div>
        </dl>
      </section>

      <!-- Tiến hoá đầy đủ (CSDL) -->
      <section v-if="coChuoi" class="mt-10 rounded-3xl glass p-6" aria-labelledby="tien-hoa-du">
        <h2 id="tien-hoa-du" class="section-title text-2xl text-highlighted">
          Chuỗi tiến hoá
        </h2>
        <p class="mt-2 text-sm text-muted">
          Cả họ theo chuỗi thật; điều kiện gốc kèm cách tiến hoá trong game (Hộp PC: cấp, vật phẩm, trao đổi, thân thiết hoặc Kẹo).
        </p>
        <ol class="mt-5 flex flex-wrap items-start gap-3">
          <li v-for="(tang, i) in chuoi" :key="i" class="flex items-center gap-3">
            <UIcon v-if="i > 0" name="i-lucide-chevron-right" class="size-5 shrink-0 text-dimmed" aria-hidden="true" />
            <ul class="flex flex-col gap-2">
              <li v-for="n in tang" :key="n.dex">
                <NuxtLink
                  :to="`/pokedex/${n.dex}`"
                  class="flex w-40 flex-col items-center rounded-2xl border p-3 text-center transition hover:border-primary"
                  :class="n.dex === dex ? 'border-primary bg-primary/10' : 'border-default/60'"
                  :aria-current="n.dex === dex ? 'page' : undefined"
                >
                  <PokeAvatar :dex="n.dex" :alt="n.loai?.ten" class="size-14" />
                  <span class="mt-1 font-semibold text-highlighted">{{ n.loai?.ten ?? soDex(n.dex) }}</span>
                  <span class="mt-1 flex gap-1"><PokeHeBadge v-for="h in n.loai?.he ?? []" :key="h" :he="h" size="sm" /></span>
                  <span v-if="n.vao" class="mt-1 text-xs text-muted">{{ moTaTienHoa(n.vao) }}</span>
                  <span v-if="n.loai && !n.loai.bat" class="mt-1 text-[11px] font-semibold text-dimmed">Chưa xuất hiện</span>
                </NuxtLink>
              </li>
            </ul>
          </li>
        </ol>
      </section>

      <!-- Tiến hoá (Pokédex tĩnh, khi CSDL chưa có chuỗi) -->
      <section v-if="!coChuoi && l?.tienHoa" class="mt-10 rounded-3xl glass p-6" aria-labelledby="tien-hoa">
        <h2 id="tien-hoa" class="section-title text-2xl text-highlighted">
          Tiến hoá
        </h2>
        <p class="mt-2 text-sm text-muted">
          {{ tieuDeTienHoa[l!.tienHoa!.kieu] }}
        </p>
        <ol class="mt-5 flex flex-wrap items-stretch gap-3">
          <li v-for="(b, i) in buocTienHoa" :key="b.dex" class="flex items-center gap-3">
            <UIcon v-if="i > 0" name="i-lucide-chevron-right" class="size-5 text-dimmed" aria-hidden="true" />
            <NuxtLink
              :to="`/pokedex/${b.dex}`"
              class="flex flex-col items-center rounded-2xl border p-3 text-center transition hover:border-primary"
              :class="b.dex === dex ? 'border-primary bg-primary/10' : 'border-default/60'"
              :aria-current="b.dex === dex ? 'page' : undefined"
            >
              <PokeSan :dex="b.dex" :he="b.loai?.he ?? []" :size="96" class="w-28 rounded-xl" />
              <span class="mt-1 font-semibold text-highlighted">{{ b.loai?.ten ?? soDex(b.dex) }}</span>
              <span class="text-xs text-muted">{{ b.dieuKien }}</span>
            </NuxtLink>
          </li>
        </ol>
        <ul v-if="nhanhTienHoa.length" class="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-4" aria-label="Các nhánh Eevee">
          <li v-for="b in nhanhTienHoa" :key="b.dex">
            <NuxtLink
              :to="`/pokedex/${b.dex}`"
              class="flex h-full flex-col items-center rounded-2xl border p-3 text-center transition hover:border-primary"
              :class="b.dex === dex ? 'border-primary bg-primary/10' : 'border-default/60'"
              :aria-current="b.dex === dex ? 'page' : undefined"
            >
              <PokeSan :dex="b.dex" :he="b.loai?.he ?? []" :size="96" class="w-28 rounded-xl" />
              <span class="mt-1 font-semibold text-highlighted">{{ b.loai?.ten }}</span>
              <span class="mt-1 flex gap-1"><PokeHeBadge v-for="h in b.loai?.he ?? []" :key="h" :he="h" size="sm" /></span>
              <span class="mt-1 text-xs text-muted">{{ b.dieuKien }}</span>
            </NuxtLink>
          </li>
        </ul>
      </section>

      <!-- Nơi xuất hiện (quái / boss) -->
      <section v-if="l?.quai.length" class="mt-10 rounded-3xl glass p-6" aria-labelledby="noi-gap">
        <h2 id="noi-gap" class="section-title text-2xl text-highlighted">
          Gặp ở đâu
        </h2>
        <p class="mt-2 text-sm text-muted">
          Hạ loài này để ghi vào Pokédex của bạn. Số lượng là số con xuất hiện cùng lúc trên bản đồ.
        </p>
        <div class="relative mt-5 overflow-x-auto">
          <table class="w-full min-w-[34rem] text-left text-sm">
            <caption class="sr-only">
              Các mẫu {{ ten }} trong game và nơi xuất hiện
            </caption>
            <thead class="text-xs uppercase tracking-wide text-muted">
              <tr>
                <th scope="col" class="py-2 pr-4">
                  Loại
                </th>
                <th scope="col" class="py-2 pr-4">
                  Cấp
                </th>
                <th scope="col" class="py-2 pr-4">
                  HP
                </th>
                <th scope="col" class="py-2 pr-4">
                  EXP
                </th>
                <th scope="col" class="py-2">
                  Khu vực
                </th>
              </tr>
            </thead>
            <tbody class="divide-y divide-default/60">
              <tr v-for="q in l!.quai" :key="q.id">
                <td class="py-3 pr-4">
                  <UBadge :color="q.boss ? 'error' : 'neutral'" variant="subtle" size="sm">
                    {{ q.boss ? 'Boss' : 'Hoang dã' }}
                  </UBadge>
                  <UBadge v-if="q.shiny" color="primary" variant="subtle" size="sm" class="ml-1">
                    Shiny
                  </UBadge>
                </td>
                <td class="num py-3 pr-4 font-semibold text-highlighted">
                  {{ q.cap }}
                </td>
                <td class="num py-3 pr-4">
                  {{ fmt(q.hp) }}
                </td>
                <td class="num py-3 pr-4">
                  {{ fmt(q.exp) }}
                </td>
                <td class="py-3">
                  <ul v-if="q.noi.length" class="flex flex-wrap gap-1.5">
                    <li v-for="n in q.noi" :key="n.map">
                      <UBadge color="secondary" variant="outline" size="sm">
                        {{ n.ten }} <span class="text-dimmed">×{{ n.soLuong }}</span>
                      </UBadge>
                    </li>
                  </ul>
                  <span v-else class="text-muted">{{ q.ghiChuNoi || 'Không cố định' }}</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <!-- Chiêu theo loài (data_learnset) -->
      <section v-if="chieuLoai.length" class="mt-10 rounded-3xl glass p-6" aria-labelledby="chieu">
        <h2 id="chieu" class="section-title text-2xl text-highlighted">
          Chiêu {{ ten }} học được
        </h2>
        <p class="mt-2 text-sm text-muted">
          Chiêu tự học khi đủ cấp (không tốn điểm); chiêu mạnh của dạng cuối học ở NPC Blaziken. Điểm kỹ năng (1 mỗi cấp) dùng để nâng chiêu đã học.
          Chiêu cùng hệ với Pokémon được cộng ×{{ (POKEDEX.stabPhanTram / 100).toLocaleString('vi-VN') }} sát thương.
        </p>
        <ol class="mt-5 grid gap-2 sm:grid-cols-2 lg:grid-cols-3">
          <li v-for="c in chieuLoai" :key="c.o" class="flex items-center justify-between gap-3 rounded-xl border border-default/60 px-3 py-2" :class="c.cach === 'tien-hoa' ? 'opacity-70' : ''">
            <span class="flex min-w-0 items-center gap-2">
              <img v-if="l?.dong" :src="`/chieu/${l.dong.class}-${c.o}.png`" width="28" height="28" alt="" loading="lazy" class="size-7 shrink-0">
              <span class="min-w-0">
                <span class="flex items-center gap-1 font-medium text-highlighted">
                  {{ c.ten }}
                  <UBadge v-if="he.includes(c.he)" color="primary" variant="subtle" size="sm">STAB</UBadge>
                </span>
                <span class="block text-xs text-muted">{{ moTaCach(c) }}</span>
              </span>
            </span>
            <PokeHeBadge :he="c.he" size="sm" />
          </li>
        </ol>
      </section>

      <!-- Chiêu bạn đồng hành (CSDL) -->
      <section v-if="ct?.chieu.length" class="mt-10 rounded-3xl glass p-6" aria-labelledby="chieu-dong-hanh">
        <h2 id="chieu-dong-hanh" class="section-title text-2xl text-highlighted">
          Chiêu khi là bạn đồng hành
        </h2>
        <p class="mt-2 text-sm text-muted">
          4 ô chiêu của {{ ten }} trong Hộp PC / bạn đồng hành: 2 chiêu cùng hệ, 1 chiêu phủ hệ, 1 chiêu hỗ trợ
          (họ hiếm và dòng khởi đầu có chiêu đặc trưng).
        </p>
        <ol class="mt-5 grid gap-2 sm:grid-cols-2">
          <li v-for="c in ct.chieu" :key="c.o" class="flex items-center justify-between gap-3 rounded-xl border border-default/60 px-3 py-2">
            <span class="flex min-w-0 flex-col">
              <span class="font-medium text-highlighted">
                {{ c.ten }}
                <UBadge v-if="c.vai === 'dac_trung'" color="primary" variant="subtle" size="sm">Đặc trưng</UBadge>
              </span>
              <span class="text-xs text-muted">{{ TEN_VAI_CHIEU[c.vai] ?? c.vai }} · {{ TEN_KIEU_CHIEU[c.kieu] ?? c.kieu }} · học từ cấp {{ c.capHoc }}</span>
            </span>
            <PokeHeBadge :he="c.he" size="sm" />
          </li>
        </ol>
      </section>

      <!-- Nơi sống (CSDL) -->
      <section v-if="d" class="mt-10 rounded-3xl glass p-6" aria-labelledby="noi-song">
        <h2 id="noi-song" class="section-title text-2xl text-highlighted">
          Nơi sống
        </h2>
        <p v-if="!ct?.vung.length" class="mt-2 text-sm text-muted">
          {{ !d.bat ? 'Loài này chưa xuất hiện ngoài trời trong game.' : d.bac >= 6 ? 'Huyền thoại chỉ gặp ở đột kích.' : d.giaiDoan === 0 ? 'Pokémon con chỉ nở từ trứng.' : 'Không gặp ngoài trời: tiến hoá trong Hộp PC hoặc ở sự kiện.' }}
        </p>
        <template v-else>
          <p class="mt-2 text-sm text-muted">
            Quái hồi sinh ở vùng phù hợp có thể đổi thành loài này (cấp theo bản đồ). Trọng số càng cao càng hay gặp.
          </p>
          <div class="relative mt-5 overflow-x-auto">
            <table class="w-full min-w-[34rem] text-left text-sm">
              <caption class="sr-only">
                Nơi sống của {{ ten }}
              </caption>
              <thead class="text-xs uppercase tracking-wide text-muted">
                <tr>
                  <th scope="col" class="py-2 pr-4">
                    Khu vực
                  </th>
                  <th scope="col" class="py-2 pr-4">
                    Vùng
                  </th>
                  <th scope="col" class="py-2 pr-4">
                    Cấp quái
                  </th>
                  <th scope="col" class="py-2 pr-4">
                    Trọng số
                  </th>
                  <th scope="col" class="py-2">
                    Điều kiện
                  </th>
                </tr>
              </thead>
              <tbody class="divide-y divide-default/60">
                <tr v-for="v in ct.vung" :key="`${v.khuVuc}-${v.vung}`">
                  <td class="py-3 pr-4 font-semibold text-highlighted">
                    {{ v.khuVuc }}
                  </td>
                  <td class="py-3 pr-4">
                    {{ TEN_VUNG_SINH_THAI[v.vung] ?? v.vung }}
                  </td>
                  <td class="num py-3 pr-4">
                    {{ v.capMin }}–{{ v.capMax }}
                  </td>
                  <td class="num py-3 pr-4">
                    {{ v.trongSo }}
                  </td>
                  <td class="py-3 text-muted">
                    {{ TEN_NGAY_DEM[v.ngayDem] }}<template v-if="v.thoiTiet >= 0">
                      {{ v.bacGap >= 5 ? ' hoặc ' : ' · ' }}{{ TEN_THOI_TIET[v.thoiTiet] }}
                    </template>
                    <span v-if="v.bacGap >= 5" class="block text-xs">Lượt gặp cực hiếm</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </template>
      </section>

      <!-- Chiêu (danh sách tĩnh của dòng khi chưa đọc được bảng chiêu theo loài) -->
      <section v-if="!chieuLoai.length && l?.chieu?.length" class="mt-10 rounded-3xl glass p-6" aria-labelledby="chieu">
        <h2 id="chieu" class="section-title text-2xl text-highlighted">
          Chiêu thức của {{ l!.dong?.ten }}
        </h2>
        <p class="mt-2 text-sm text-muted">
          Cả dòng dùng chung bộ chiêu. Chiêu cùng hệ với Pokémon được cộng ×{{ (POKEDEX.stabPhanTram / 100).toLocaleString('vi-VN') }} sát thương.
        </p>
        <ol class="mt-5 grid gap-2 sm:grid-cols-2 lg:grid-cols-3">
          <li v-for="c in l!.chieu!" :key="c.so" class="flex items-center justify-between gap-3 rounded-xl border border-default/60 px-3 py-2">
            <span class="flex min-w-0 items-center gap-2">
              <span class="num w-5 text-xs text-dimmed">{{ c.so + 1 }}</span>
              <!-- icon chiêu thật trong game (public/chieu/<class>-<số>.png) -->
              <img v-if="l!.dong" :src="`/chieu/${l!.dong.class}-${c.so}.png`" width="28" height="28" alt="" loading="lazy" class="size-7 shrink-0">
              <span class="font-medium text-highlighted">{{ c.ten }}</span>
              <UBadge v-if="he.includes(c.he)" color="primary" variant="subtle" size="sm">STAB</UBadge>
            </span>
            <PokeHeBadge :he="c.he" size="sm" />
          </li>
        </ol>
      </section>

      <!-- Thú cưỡi / pet / NPC -->
      <section v-if="l && (l.thuCuoi.length || l.pet.length || l.npc.length)" class="mt-10 grid gap-6 md:grid-cols-3" aria-label="Vai trò khác">
        <div v-if="l.thuCuoi.length" class="rounded-3xl glass p-6">
          <h2 class="section-title text-xl text-highlighted">
            Thú cưỡi
          </h2>
          <ul class="mt-3 space-y-1 text-sm">
            <li v-for="t in l.thuCuoi" :key="`${t.nguon}-${t.id}`">
              {{ t.ten }} <UBadge v-if="t.shiny" color="primary" variant="subtle" size="sm">Shiny</UBadge>
            </li>
          </ul>
        </div>
        <div v-if="l.pet.length" class="rounded-3xl glass p-6">
          <h2 class="section-title text-xl text-highlighted">
            Pet đi theo
          </h2>
          <ul class="mt-3 space-y-1 text-sm">
            <li v-for="(p, i) in l.pet" :key="i">
              {{ p.ten }} <UBadge v-if="p.shiny" color="primary" variant="subtle" size="sm">Shiny</UBadge>
            </li>
          </ul>
        </div>
        <div v-if="l.npc.length" class="rounded-3xl glass p-6">
          <h2 class="section-title text-xl text-highlighted">
            NPC
          </h2>
          <ul class="mt-3 space-y-1 text-sm">
            <li v-for="n in l.npc" :key="n.ten">
              {{ n.ten }}
            </li>
          </ul>
        </div>
      </section>

      <p v-if="d" class="mt-10 text-center text-xs text-muted">
        <template v-if="ct?.sprite?.ghiCong">
          Sprite: PMDCollab — {{ ct.sprite.ghiCong }} (CC BY-NC 4.0).
        </template>
        <template v-else-if="chanDung">
          Sprite / chân dung: PMDCollab (CC BY-NC 4.0).
        </template>
        <template v-else>
          Chưa có hình cho loài này.
        </template>
        Dữ kiện loài: PokéAPI (BSD-3-Clause).
        <NuxtLink to="/giay-phep" class="text-primary hover:underline">Giấy phép và ghi công</NuxtLink>
      </p>

      <nav class="mt-12 flex flex-wrap items-center justify-between gap-3" aria-label="Loài trước và sau">
        <UButton v-if="lanCan.truoc" :to="`/pokedex/${lanCan.truoc.dex}`" color="neutral" variant="outline" icon="i-lucide-arrow-left">
          {{ soDex(lanCan.truoc.dex) }} {{ lanCan.truoc.ten }}
        </UButton>
        <span v-else />
        <UButton to="/pokedex" color="neutral" variant="ghost" icon="i-lucide-layout-grid">
          Pokédex
        </UButton>
        <UButton v-if="lanCan.sau" :to="`/pokedex/${lanCan.sau.dex}`" color="neutral" variant="outline" trailing-icon="i-lucide-arrow-right">
          {{ soDex(lanCan.sau.dex) }} {{ lanCan.sau.ten }}
        </UButton>
        <span v-else />
      </nav>
    </UContainer>
  </div>
</template>
