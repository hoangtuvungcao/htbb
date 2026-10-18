<script setup lang="ts">
/** Chi tiết một loài: hệ, khắc chế (hệ số trong game), tiến hoá, nơi xuất hiện, chiêu, vai trò. */
// Dữ liệu tính một lần trong setup -> mỗi số dex là một lần dựng trang mới.
definePageMeta({ key: r => r.path })

const route = useRoute()
const dex = Number(route.params.dex)
const loai = Number.isInteger(dex) ? loaiTheoDex(dex) : undefined
if (!loai) {
  throw createError({ statusCode: 404, statusMessage: 'Không có Pokémon này trong game', fatal: true })
}
const l = loai

const soDex = (d: number) => `#${String(d).padStart(3, '0')}`
const tenSite = useAppConfig().htbb.tenSite
const heChu = l.he.map(tenHe).join(' / ')

useSeoMeta({
  title: `${l.ten} ${soDex(l.dex)} — Pokédex`,
  description: `${l.ten} (${heChu}) trong ${tenSite}: ${l.vaiTro.map(v => TEN_VAI_TRO[v].toLowerCase()).join(', ')}. Khắc hệ, tiến hoá, nơi xuất hiện${l.chieu ? ', chiêu thức' : ''}.`,
})

const phongThu = bangPhongThu(l.he)
const moTaHeSo = (g: number) => g > 100 ? 'Bị khắc (nhận thêm sát thương)' : g < 100 ? 'Kháng (nhận ít sát thương)' : 'Bình thường'
const mauHeSo = (g: number) => g > 100 ? 'text-error' : g < 100 ? 'text-success' : 'text-muted'

const idx = POKEDEX.loai.findIndex(x => x.dex === l.dex)
const truoc = POKEDEX.loai[idx - 1]
const sau = POKEDEX.loai[idx + 1]

const buocTienHoa = (l.tienHoa?.buoc ?? []).map(b => ({ ...b, loai: loaiTheoDex(b.dex) }))
const nhanhTienHoa = (l.tienHoa?.nhanh ?? []).map(b => ({ ...b, loai: loaiTheoDex(b.dex) }))
const tieuDeTienHoa: Record<string, string> = {
  cap: 'Tiến hoá theo cấp (tự động khi đạt cấp)',
  eevee: `Eevee chọn nhánh bằng đá tiến hoá từ cấp ${POKEDEX.capNhanhEevee}`,
  pet: 'Pet tiến hoá theo cấp pet (nâng cấp pet)',
  ho: 'Họ tiến hoá — các dạng có trong game',
}

/** LoaiPokemon.phanTramGiaiDoan: dạng 2 / dạng 3 / nhánh Eevee được cộng HP + tấn công. */
const thuongGiaiDoan = l.tienHoa?.kieu === 'eevee' && l.dex !== 133
  ? POKEDEX.phanTramGiaiDoan.eevee
  : l.giaiDoan === 2 ? POKEDEX.phanTramGiaiDoan[2] : l.giaiDoan === 3 ? POKEDEX.phanTramGiaiDoan[3] : 0

const daCuaLoai = POKEDEX.daTienHoa.find(d => d.dex === l.dex)
const fmt = (n: number) => n.toLocaleString('vi-VN')
</script>

<template>
  <div>
    <SitePageBanner
      :title="`${l.ten} ${soDex(l.dex)}`"
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
              <PokeSan :dex="l.dex" :he="l.he" :size="232" :label="l.ten" class="w-full rounded-xl" />
              <span class="absolute left-3 top-3 rounded-full bg-white/80 px-2 py-1 font-pixel text-[10px] leading-none text-navy-800 dark:bg-navy-950/70 dark:text-navy-100">{{ soDex(l.dex) }}</span>
            </div>
            <p class="mt-3 text-center font-display text-2xl font-extrabold text-highlighted">
              {{ l.ten }}
            </p>
            <div class="mt-1 flex justify-center gap-1.5">
              <PokeHeBadge v-for="h in l.he" :key="h" :he="h" />
            </div>
            <div v-if="l.anh && l.anhShiny" class="mt-4 flex items-center justify-center gap-3 rounded-xl bg-pikachu-50 p-2 text-sm font-semibold text-pikachu-900 dark:bg-pikachu-950/40 dark:text-pikachu-200">
              <PokeSan :dex="l.dex" shiny :he="l.he" :size="56" :label="`${l.ten} shiny`" class="w-14 rounded-lg" />
              ✨ Có bản shiny trong game
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
          <dl class="mt-5 grid gap-4 sm:grid-cols-2">
            <div>
              <dt class="text-sm text-muted">
                Số Pokédex
              </dt>
              <dd class="text-lg font-semibold tabular-nums text-highlighted">
                {{ soDex(l.dex) }}
              </dd>
            </div>
            <div>
              <dt class="text-sm text-muted">
                Hệ
              </dt>
              <dd class="mt-1 flex flex-wrap gap-1.5">
                <PokeHeBadge v-for="h in l.he" :key="h" :he="h" />
              </dd>
            </div>
            <div>
              <dt class="text-sm text-muted">
                Vai trò trong game
              </dt>
              <dd class="mt-1 flex flex-wrap gap-1.5">
                <UBadge v-for="v in l.vaiTro" :key="v" :color="v === 'boss' ? 'error' : v === 'khoi-dau' ? 'primary' : 'neutral'" variant="subtle">
                  {{ TEN_VAI_TRO[v] }}
                </UBadge>
              </dd>
            </div>
            <div v-if="l.dong">
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

      <!-- Tiến hoá -->
      <section v-if="l.tienHoa" class="mt-10 rounded-3xl glass p-6" aria-labelledby="tien-hoa">
        <h2 id="tien-hoa" class="section-title text-2xl text-highlighted">
          Tiến hoá
        </h2>
        <p class="mt-2 text-sm text-muted">
          {{ tieuDeTienHoa[l.tienHoa.kieu] }}
        </p>
        <ol class="mt-5 flex flex-wrap items-stretch gap-3">
          <li v-for="(b, i) in buocTienHoa" :key="b.dex" class="flex items-center gap-3">
            <UIcon v-if="i > 0" name="i-lucide-chevron-right" class="size-5 text-dimmed" aria-hidden="true" />
            <NuxtLink
              :to="`/pokedex/${b.dex}`"
              class="flex flex-col items-center rounded-2xl border p-3 text-center transition hover:border-primary"
              :class="b.dex === l.dex ? 'border-primary bg-primary/10' : 'border-default/60'"
              :aria-current="b.dex === l.dex ? 'page' : undefined"
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
              :class="b.dex === l.dex ? 'border-primary bg-primary/10' : 'border-default/60'"
              :aria-current="b.dex === l.dex ? 'page' : undefined"
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
      <section v-if="l.quai.length" class="mt-10 rounded-3xl glass p-6" aria-labelledby="noi-gap">
        <h2 id="noi-gap" class="section-title text-2xl text-highlighted">
          Gặp ở đâu
        </h2>
        <p class="mt-2 text-sm text-muted">
          Hạ loài này để ghi vào Pokédex của bạn. Số lượng là số con xuất hiện cùng lúc trên bản đồ.
        </p>
        <div class="relative mt-5 overflow-x-auto">
          <table class="w-full min-w-[34rem] text-left text-sm">
            <caption class="sr-only">
              Các mẫu {{ l.ten }} trong game và nơi xuất hiện
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
              <tr v-for="q in l.quai" :key="q.id">
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

      <!-- Chiêu -->
      <section v-if="l.chieu?.length" class="mt-10 rounded-3xl glass p-6" aria-labelledby="chieu">
        <h2 id="chieu" class="section-title text-2xl text-highlighted">
          Chiêu thức của {{ l.dong?.ten }}
        </h2>
        <p class="mt-2 text-sm text-muted">
          Cả dòng dùng chung bộ chiêu. Chiêu cùng hệ với Pokémon được cộng ×{{ (POKEDEX.stabPhanTram / 100).toLocaleString('vi-VN') }} sát thương.
        </p>
        <ol class="mt-5 grid gap-2 sm:grid-cols-2 lg:grid-cols-3">
          <li v-for="c in l.chieu" :key="c.so" class="flex items-center justify-between gap-3 rounded-xl border border-default/60 px-3 py-2">
            <span class="flex items-center gap-2">
              <span class="num w-5 text-xs text-dimmed">{{ c.so + 1 }}</span>
              <span class="font-medium text-highlighted">{{ c.ten }}</span>
              <UBadge v-if="l.he.includes(c.he)" color="primary" variant="subtle" size="sm">STAB</UBadge>
            </span>
            <PokeHeBadge :he="c.he" size="sm" />
          </li>
        </ol>
      </section>

      <!-- Thú cưỡi / pet / NPC -->
      <section v-if="l.thuCuoi.length || l.pet.length || l.npc.length" class="mt-10 grid gap-6 md:grid-cols-3" aria-label="Vai trò khác">
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

      <nav class="mt-12 flex flex-wrap items-center justify-between gap-3" aria-label="Loài trước và sau">
        <UButton v-if="truoc" :to="`/pokedex/${truoc.dex}`" color="neutral" variant="outline" icon="i-lucide-arrow-left">
          {{ soDex(truoc.dex) }} {{ truoc.ten }}
        </UButton>
        <span v-else />
        <UButton to="/pokedex" color="neutral" variant="ghost" icon="i-lucide-layout-grid">
          Pokédex
        </UButton>
        <UButton v-if="sau" :to="`/pokedex/${sau.dex}`" color="neutral" variant="outline" trailing-icon="i-lucide-arrow-right">
          {{ soDex(sau.dex) }} {{ sau.ten }}
        </UButton>
        <span v-else />
      </nav>
    </UContainer>
  </div>
</template>
