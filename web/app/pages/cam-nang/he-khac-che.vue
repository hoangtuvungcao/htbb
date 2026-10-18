<script setup lang="ts">
/** Hệ & khắc hệ: bảng 18×18 tương tác + máy tính hệ số. Số liệu: HePokemon.java qua pokedex.json. */
const dsHe = POKEDEX.he
const oKhac = (ck: number, pt: number) => heSoTrongGame(ck, [pt])

const lopO = (g: number) => g >= 150
  ? 'bg-grass-400 text-navy-950 font-bold'
  : g <= 30
    ? 'bg-navy-800 text-white font-bold dark:bg-black'
    : g < 100
      ? 'bg-pokered-200 text-pokered-900 dark:bg-pokered-900/70 dark:text-pokered-100'
      : 'bg-navy-50 text-dimmed dark:bg-navy-900'
const chuO = (g: number) => g === 100 ? '' : (g / 100).toLocaleString('vi-VN')

const bangLamDiu = Object.entries(POKEDEX.lamDiu)
  .map(([goc, game]) => ({ goc: Number(goc), game }))
  .sort((a, b) => b.goc - a.goc)

// Tô sáng hàng/cột khi rê chuột hoặc chọn
const hang = ref<number | null>(null)
const cot = ref<number | null>(null)
const chonHang = ref<number | null>(null)
const hangSang = computed(() => hang.value ?? chonHang.value)

// Máy tính
const heItems = dsHe.map(h => ({ label: h.ten, value: h.id }))
const heItems2 = [{ label: '— Không có hệ 2 —', value: -1 }, ...heItems]
const chieu = ref(1)
const thu1 = ref(4)
const thu2 = ref(-1)
const stab = ref(true)
const heThu = computed(() => (thu2.value >= 0 && thu2.value !== thu1.value ? [thu1.value, thu2.value] : [thu1.value]))
const heSoHe = computed(() => heSoTrongGame(chieu.value, heThu.value))
const heSoGoc = computed(() => heSoChinhXac(chieu.value, heThu.value))
const tong = computed(() => heSoHe.value * (stab.value ? POKEDEX.stabPhanTram : 100) / 100)

const phongThu = computed(() => bangPhongThu(heThu.value))
const tenHeSo = (g: number) => g >= 200 ? 'Rất hiệu quả' : g >= 150 ? 'Hiệu quả' : g === 100 ? 'Bình thường' : g >= 70 ? 'Bị kháng' : g >= 50 ? 'Kháng mạnh' : 'Gần như vô hiệu'

// Khởi đầu: hệ của 5 dòng
const khoiDau = POKEDEX.khoiDau.map(k => loaiTheoDex(k.dex)!).filter(Boolean)
</script>

<template>
  <WikiShell slug="he-khac-che">
    <p class="van">
      Mỗi Pokémon có 1 hoặc 2 hệ, mỗi chiêu có 1 hệ. Khi chiêu đánh trúng, game nhân sát thương với
      <strong>hệ số khắc hệ</strong> (hệ của chiêu so với hệ của mục tiêu). Chiêu cùng hệ với chính bạn được thêm
      <strong>STAB ×{{ (POKEDEX.stabPhanTram / 100).toLocaleString('vi-VN') }}</strong>.
    </p>
    <ul class="mt-4 flex flex-wrap gap-2">
      <li v-for="l in khoiDau" :key="l.dex" class="flex items-center gap-2 rounded-full bg-elevated py-1 pl-1 pr-3 text-sm">
        <PokeAvatar :dex="l.dex" class="size-7" />
        <span class="font-semibold text-highlighted">{{ l.ten }}</span>
        <PokeHeBadge v-for="h in l.he" :key="h" :he="h" size="sm" />
      </li>
    </ul>

    <WikiSection id="bang" title="Bảng khắc hệ 18×18" lead="Hàng = hệ của chiêu tấn công, cột = hệ của Pokémon bị đánh. Rê chuột để dò hàng/cột, bấm tên hệ bên trái để giữ tô sáng một hàng.">
      <div class="overflow-x-auto rounded-2xl glass p-2 sm:p-4" @mouseleave="hang = null; cot = null">
        <table class="mx-auto border-separate border-spacing-0.5 text-center text-xs">
          <caption class="sr-only">
            Hệ số khắc hệ trong game giữa 18 hệ
          </caption>
          <thead>
            <tr>
              <th scope="col" class="p-1 text-[10px] font-normal text-dimmed">
                <span aria-hidden="true">Đánh ↓ / Nhận →</span><span class="sr-only">Tấn công \ Phòng thủ</span>
              </th>
              <th v-for="h in dsHe" :key="h.id" scope="col" class="p-0 transition-opacity" :class="cot !== null && cot !== h.id ? 'opacity-40' : ''">
                <abbr :title="h.ten" class="grid h-7 w-[30px] place-items-center rounded-md font-display text-[10px] font-extrabold leading-none no-underline" :style="mauHe(h.id)">{{ tenHeNgan(h.id) }}</abbr>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="ck in dsHe" :key="ck.id" :class="hangSang !== null && hangSang !== ck.id ? 'opacity-40' : ''" class="transition-opacity">
              <th scope="row" class="p-0.5 text-left">
                <button type="button" class="rounded-full focus-visible:outline-2 focus-visible:outline-primary" :aria-pressed="chonHang === ck.id" @click="chonHang = chonHang === ck.id ? null : ck.id">
                  <PokeHeBadge :he="ck.id" size="sm" />
                </button>
              </th>
              <td
                v-for="pt in dsHe"
                :key="pt.id"
                class="num h-[30px] w-[30px] min-w-[30px] cursor-default rounded-md !text-center text-[11px] transition"
                :class="[lopO(oKhac(ck.id, pt.id)), hang === ck.id && cot === pt.id ? 'ring-2 ring-[var(--poke-outline)] scale-110' : '']"
                :title="`${ck.ten} → ${pt.ten}: ${hienHeSo(oKhac(ck.id, pt.id))}`"
                @mouseenter="hang = ck.id; cot = pt.id"
              >
                {{ chuO(oKhac(ck.id, pt.id)) }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <p class="mt-3 flex flex-wrap gap-4 text-xs text-muted">
        <span><span class="inline-block size-3 rounded-sm bg-grass-400 align-middle ring-1 ring-black/20" /> Khắc (×1,5)</span>
        <span><span class="inline-block size-3 rounded-sm bg-pokered-200 align-middle ring-1 ring-black/20 dark:bg-pokered-900" /> Bị kháng (×0,7)</span>
        <span><span class="inline-block size-3 rounded-sm bg-navy-800 align-middle ring-1 ring-black/20 dark:bg-black" /> Gần như vô hiệu (×0,3)</span>
        <span>Ô trống = ×1</span>
      </p>
      <p class="mt-2 text-sm text-muted" aria-live="polite">
        <template v-if="hang !== null && cot !== null">
          <strong class="text-highlighted">{{ tenHe(hang) }}</strong> đánh <strong class="text-highlighted">{{ tenHe(cot) }}</strong>:
          {{ hienHeSo(oKhac(hang, cot)) }} — {{ tenHeSo(oKhac(hang, cot)).toLowerCase() }}.
        </template>
        <template v-else>
          Rê chuột lên một ô để xem chi tiết.
        </template>
      </p>
    </WikiSection>

    <WikiSection id="tinh-thu" title="Tính thử hệ số" lead="Chọn hệ của chiêu và hệ của mục tiêu (có thể 2 hệ) để xem hệ số cuối cùng.">
      <div class="grid gap-6 rounded-3xl glass ring-gilded p-5 sm:p-6 lg:grid-cols-2">
        <div class="grid gap-4 sm:grid-cols-2">
          <UFormField label="Hệ của chiêu" class="sm:col-span-2">
            <USelect v-model="chieu" :items="heItems" class="w-full" />
          </UFormField>
          <UFormField label="Mục tiêu — hệ 1">
            <USelect v-model="thu1" :items="heItems" class="w-full" />
          </UFormField>
          <UFormField label="Mục tiêu — hệ 2">
            <USelect v-model="thu2" :items="heItems2" class="w-full" />
          </UFormField>
          <UCheckbox v-model="stab" label="Chiêu cùng hệ với Pokémon của tôi (STAB)" class="sm:col-span-2" />
        </div>
        <div class="flex flex-col justify-center rounded-2xl bg-screen p-5">
          <p class="flex flex-wrap items-center gap-1.5 text-sm text-toned">
            <PokeHeBadge :he="chieu" /> <span>→</span>
            <PokeHeBadge v-for="h in heThu" :key="h" :he="h" />
          </p>
          <p class="mt-3 font-display text-5xl font-extrabold text-highlighted">
            ×{{ (tong / 100).toLocaleString('vi-VN', { maximumFractionDigits: 2 }) }}
          </p>
          <p class="mt-1 text-sm text-muted">
            {{ tenHeSo(heSoHe) }} · khắc hệ {{ hienHeSo(heSoHe) }}<template v-if="stab">
              × STAB {{ hienHeSo(POKEDEX.stabPhanTram) }}
            </template>
          </p>
          <p class="mt-2 text-xs text-dimmed">
            Bản gốc dòng game: {{ hienHeSo(heSoGoc) }} → trong {{ useAppConfig().htbb.tenVietTat }}: {{ hienHeSo(heSoHe) }}
          </p>
        </div>
      </div>

      <h3 class="mt-8 font-display text-xl font-bold text-highlighted">
        Mục tiêu này sợ / chịu được hệ nào?
      </h3>
      <ul class="mt-3 grid gap-2 sm:grid-cols-2">
        <li v-for="g in phongThu" :key="g.heSo" class="rounded-2xl glass p-3">
          <p class="text-sm font-bold text-highlighted">
            {{ hienHeSo(g.heSo) }} <span class="font-normal text-muted">— {{ tenHeSo(g.heSo) }}</span>
          </p>
          <p class="mt-2 flex flex-wrap gap-1">
            <PokeHeBadge v-for="h in g.he" :key="h" :he="h" size="sm" />
          </p>
        </li>
      </ul>
    </WikiSection>

    <WikiSection id="lam-diu" title="Vì sao hệ số được làm dịu">
      <p class="van">
        Trong {{ useAppConfig().htbb.tenSite }} bạn <strong>chính là</strong> một Pokémon và không đổi được Pokémon giữa trận như bản gốc.
        Nếu giữ hệ số gốc, có những con quái bạn không thể gây sát thương (×0), còn PvP sẽ kết thúc trong một đòn (×4).
        Vì vậy hệ số được quy đổi như sau:
      </p>
      <div class="bang-cuon mt-4 max-w-2xl">
        <table class="bang-wiki">
          <thead>
            <tr>
              <th>Bản gốc</th><th v-for="r in bangLamDiu" :key="r.goc" class="so">
                {{ hienHeSo(r.goc) }}
              </th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td class="font-bold">
                Trong game
              </td><td v-for="r in bangLamDiu" :key="r.goc" class="so font-bold text-highlighted">
                {{ hienHeSo(r.game) }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <WikiCallout kieu="meo">
        Loài hai hệ (ví dụ Bulbasaur Cỏ/Độc): game nhân hai hệ số <em>gốc</em> trước rồi mới quy đổi — nên khắc kép ×4 thành ×2,
        còn một hệ khắc một hệ kháng thì triệt tiêu về ×1.
      </WikiCallout>
      <WikiCallout kieu="luu-y">
        Hệ số khắc hệ áp lên sát thương <strong>trước</strong> khi trừ phòng thủ, nên đánh đúng hệ thường hiệu quả hơn con số ×1,5 trông thấy.
        Xem <NuxtLink to="/cam-nang/chi-so-sat-thuong" class="lien-ket">Chỉ số & sát thương</NuxtLink>.
      </WikiCallout>
    </WikiSection>
  </WikiShell>
</template>
