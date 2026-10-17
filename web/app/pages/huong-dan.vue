<script setup lang="ts">
/**
 * Hướng dẫn tân thủ. Mọi con số (cấp tiến hoá, bảng khắc chế, tỉ lệ rơi đá, mốc Pokédex...)
 * lấy từ shared/data/pokedex.json — sinh từ code game, nên trang luôn khớp với server.
 */
const tenSite = useAppConfig().htbb.tenSite
useSeoMeta({
  title: 'Hướng dẫn tân thủ',
  description: `Hướng dẫn ${tenSite}: chọn Pokémon khởi đầu, bảng khắc chế 18 hệ, tiến hoá và đá Eevee, mốc Pokédex, thú cưỡi, pet, đội và vùng.`,
})

const muc = [
  { id: 'khoi-dau', ten: 'Chọn Pokémon khởi đầu' },
  { id: 'khac-che', ten: 'Hệ & khắc chế' },
  { id: 'tien-hoa', ten: 'Tiến hoá' },
  { id: 'luyen-cap', ten: 'Khu vực luyện cấp' },
  { id: 'pokedex', ten: 'Pokédex & phần thưởng' },
  { id: 'thu-cuoi', ten: 'Thú cưỡi & pet' },
  { id: 'doi-vung', ten: 'Đội & vùng' },
  { id: 'dieu-khien', ten: 'Mẹo & điều khiển' },
]

const soDex = (d: number) => `#${String(d).padStart(3, '0')}`
const L = (dex: number) => loaiTheoDex(dex)

// ---- Khởi đầu: 5 dòng, theo thứ tự dex cho dễ nhìn
const dongKhoiDau = POKEDEX.khoiDau.map((k) => {
  const dau = L(k.dex)!
  const chuoi = (dau.tienHoa?.buoc ?? []).map(b => ({ ...b, loai: L(b.dex) }))
  const chieuNoiBat = (dau.chieu ?? []).filter(c => c.he !== 0).slice(-2)
  return { ...k, dau, chuoi, chieuNoiBat, laEevee: dau.tienHoa?.kieu === 'eevee' }
}).sort((a, b) => a.dex - b.dex)

// ---- Bảng khắc chế 18×18 (hệ số trong game khi đánh vào loài một hệ)
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

// ---- Tiến hoá
const dongCap = dongKhoiDau.filter(d => !d.laEevee)
const daEevee = POKEDEX.daTienHoa.map(d => ({ ...d, loai: L(d.dex) }))
const tiLeRoi = (POKEDEX.tiLeRoiDaPhanNghin / 10).toLocaleString('vi-VN')

// ---- Luyện cấp
const dsMap = [...POKEDEX.map].sort((a, b) => a.capThap - b.capThap || a.id - b.id)

// ---- Pokédex
const soLoaiHaDuoc = POKEDEX.loai.filter(l => l.quai.length).length

// ---- Thú cưỡi & pet
const thuCuoi = POKEDEX.loai.filter(l => l.vaiTro.includes('thu-cuoi'))
const chuoiPet = POKEDEX.loai
  .filter(l => l.tienHoa?.kieu === 'pet' && l.tienHoa.buoc[0]?.dex === l.dex)
  .map(l => l.tienHoa!.buoc.map(b => ({ ...b, loai: L(b.dex) })))
const petKhac = POKEDEX.loai.filter(l => l.vaiTro.includes('pet') && l.tienHoa?.kieu !== 'pet')
</script>

<template>
  <div>
    <SitePageBanner
      title="Hướng dẫn tân thủ"
      eyebrow="Cẩm nang"
      :intro="`Mọi thứ cần biết để bắt đầu ${tenSite}: chọn bạn đồng hành, nắm hệ khắc chế, tiến hoá và sưu tầm Pokédex.`"
    />

    <UContainer class="max-w-7xl py-12 sm:py-16">
      <div class="grid gap-10 lg:grid-cols-[14rem_1fr]">
        <!-- Mục lục -->
        <nav aria-label="Mục lục" class="lg:sticky lg:top-24 lg:self-start">
          <p class="eyebrow">
            Mục lục
          </p>
          <ol class="mt-3 flex flex-wrap gap-2 lg:flex-col lg:gap-1">
            <li v-for="(m, i) in muc" :key="m.id">
              <a :href="`#${m.id}`" class="flex items-center gap-2 rounded-full px-3 py-1.5 font-display text-sm font-semibold text-muted ring-2 ring-default hover:bg-elevated hover:text-primary lg:ring-0">
                <span class="grid size-5 place-items-center rounded-full bg-pokered-500 font-pixel text-[8px] text-white">{{ i + 1 }}</span> {{ m.ten }}
              </a>
            </li>
          </ol>
          <UButton to="/pokedex" color="primary" variant="soft" icon="i-lucide-book-open" class="mt-6 hidden lg:inline-flex">
            Mở Pokédex
          </UButton>
        </nav>

        <div class="min-w-0 space-y-16">
          <!-- 1. Khởi đầu -->
          <section id="khoi-dau" class="scroll-mt-24" aria-labelledby="h-khoi-dau">
            <h2 id="h-khoi-dau" class="section-title text-3xl text-highlighted">
              1. Chọn Pokémon khởi đầu
            </h2>
            <p class="mt-3 max-w-3xl text-muted">
              Khi tạo nhân vật, bạn chọn một trong {{ dongKhoiDau.length }} dòng Pokémon. Chính bạn là Pokémon đó:
              lên cấp sẽ tự tiến hoá, dạng sau được cộng thêm HP và tấn công
              (dạng 2 +{{ POKEDEX.phanTramGiaiDoan[2] }}%, dạng 3 +{{ POKEDEX.phanTramGiaiDoan[3] }}%, nhánh Eevee +{{ POKEDEX.phanTramGiaiDoan.eevee }}%).
            </p>
            <ul class="mt-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
              <li v-for="d in dongKhoiDau" :key="d.dex" class="flex flex-col rounded-3xl glass p-5">
                <div class="flex items-center gap-4">
                  <img :src="anhLoaiDex(d.dau)" width="40" height="40" alt="" class="pixelated size-16 rounded-xl bg-screen">
                  <div>
                    <h3 class="text-lg font-bold text-highlighted">
                      <NuxtLink :to="`/pokedex/${d.dex}`" class="hover:text-primary">{{ d.dau.ten }}</NuxtLink>
                    </h3>
                    <p class="text-xs text-muted">
                      {{ d.dong }}
                    </p>
                    <div class="mt-1 flex gap-1">
                      <PokeHeBadge v-for="h in d.dau.he" :key="h" :he="h" size="sm" />
                    </div>
                  </div>
                </div>
                <p v-if="d.laEevee" class="mt-4 text-sm text-muted">
                  Linh hoạt nhất: tới cấp {{ POKEDEX.capNhanhEevee }} bạn tự chọn 1 trong {{ d.dau.tienHoa?.nhanh.length }} nhánh tiến hoá bằng đá.
                </p>
                <p v-else class="mt-4 text-sm text-muted">
                  Tiến hoá:
                  <template v-for="(b, i) in d.chuoi" :key="b.dex">
                    <span v-if="i > 0" aria-hidden="true"> → </span>
                    <NuxtLink :to="`/pokedex/${b.dex}`" class="text-highlighted hover:text-primary">{{ b.loai?.ten }}</NuxtLink>
                    <span v-if="i > 0" class="text-dimmed"> ({{ b.dieuKien.toLowerCase() }})</span>
                  </template>
                </p>
                <p v-if="d.chieuNoiBat.length" class="mt-2 text-sm text-muted">
                  Chiêu mạnh: {{ d.chieuNoiBat.map(c => c.ten).join(', ') }}
                </p>
              </li>
            </ul>
          </section>

          <!-- 2. Khắc chế -->
          <section id="khac-che" class="scroll-mt-24" aria-labelledby="h-khac-che">
            <h2 id="h-khac-che" class="section-title text-3xl text-highlighted">
              2. Hệ & khắc chế
            </h2>
            <p class="mt-3 max-w-3xl text-muted">
              Game có {{ dsHe.length }} hệ như bản gốc, nhưng hệ số được <strong class="text-highlighted">làm dịu</strong>
              vì bạn không đổi được Pokémon giữa trận: không có miễn nhiễm, khắc gấp đôi chỉ còn ×2.
              Chiêu cùng hệ với bạn được cộng ×{{ (POKEDEX.stabPhanTram / 100).toLocaleString('vi-VN') }} (STAB).
              Loài hai hệ: nhân hai hệ số gốc rồi mới quy đổi.
            </p>
            <div class="relative mt-5 overflow-x-auto rounded-2xl glass">
              <table class="w-full text-left text-sm">
                <caption class="sr-only">
                  Quy đổi hệ số khắc chế gốc sang hệ số trong game
                </caption>
                <thead>
                  <tr class="text-xs uppercase tracking-wide text-muted">
                    <th scope="row" class="px-4 py-2">
                      Bản gốc
                    </th>
                    <td v-for="r in bangLamDiu" :key="r.goc" class="num px-4 py-2 text-center">
                      {{ hienHeSo(r.goc) }}
                    </td>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <th scope="row" class="px-4 py-2 text-xs uppercase tracking-wide text-muted">
                      Trong game
                    </th>
                    <td v-for="r in bangLamDiu" :key="r.goc" class="num px-4 py-2 text-center font-bold text-highlighted">
                      {{ hienHeSo(r.game) }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div class="mt-6 overflow-x-auto rounded-2xl glass p-2 sm:p-4">
              <table class="mx-auto border-separate border-spacing-0.5 text-center text-xs">
                <caption class="mb-3 px-2 text-left text-sm text-muted">
                  Hàng = hệ của chiêu tấn công, cột = hệ của Pokémon bị đánh. Ô trống = ×1.
                </caption>
                <thead>
                  <tr>
                    <th scope="col" class="p-1 text-[10px] font-normal text-dimmed">
                      <span class="sr-only">Tấn công \ Phòng thủ</span>
                      <span aria-hidden="true">Đánh ↓ / Nhận →</span>
                    </th>
                    <th v-for="h in dsHe" :key="h.id" scope="col" class="p-0.5">
                      <PokeHeBadge :he="h.id" ngan size="sm" />
                    </th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="ck in dsHe" :key="ck.id">
                    <th scope="row" class="p-0.5 text-left">
                      <PokeHeBadge :he="ck.id" size="sm" />
                    </th>
                    <td
                      v-for="pt in dsHe"
                      :key="pt.id"
                      class="num size-8 min-w-8 rounded-md !text-center"
                      :class="lopO(oKhac(ck.id, pt.id))"
                      :title="`${ck.ten} → ${pt.ten}: ${hienHeSo(oKhac(ck.id, pt.id))}`"
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
              <span><span class="inline-block size-3 rounded-sm bg-navy-800 align-middle ring-1 ring-black/20 dark:bg-black" /> Gần như vô hiệu (×0,3, bản gốc ×0)</span>
            </p>
          </section>

          <!-- 3. Tiến hoá -->
          <section id="tien-hoa" class="scroll-mt-24" aria-labelledby="h-tien-hoa">
            <h2 id="h-tien-hoa" class="section-title text-3xl text-highlighted">
              3. Tiến hoá
            </h2>
            <div class="mt-5 grid grid-cols-1 gap-6 xl:grid-cols-2">
              <div class="rounded-3xl glass p-6">
                <h3 class="text-lg font-bold text-highlighted">
                  Tiến hoá theo cấp
                </h3>
                <p class="mt-1 text-sm text-muted">
                  Tự động khi đạt cấp, không cần vật phẩm.
                </p>
                <div class="relative mt-4 overflow-x-auto">
                  <table class="w-full min-w-[22rem] text-left text-sm">
                    <caption class="sr-only">
                      Cấp tiến hoá của các dòng khởi đầu
                    </caption>
                    <thead class="text-xs uppercase tracking-wide text-muted">
                      <tr>
                        <th scope="col" class="py-2 pr-3">
                          Dạng 1
                        </th>
                        <th scope="col" class="py-2 pr-3">
                          Dạng 2
                        </th>
                        <th scope="col" class="py-2">
                          Dạng 3
                        </th>
                      </tr>
                    </thead>
                    <tbody class="divide-y divide-default/60">
                      <tr v-for="d in dongCap" :key="d.dex">
                        <td v-for="(b, i) in d.chuoi" :key="b.dex" class="py-2 pr-3">
                          <NuxtLink :to="`/pokedex/${b.dex}`" class="flex items-center gap-2 hover:text-primary">
                            <img :src="b.loai ? anhLoaiDex(b.loai) : ''" width="40" height="40" alt="" class="pixelated size-8" loading="lazy">
                            <span>
                              <span class="font-medium text-highlighted">{{ b.loai?.ten }}</span>
                              <span v-if="i > 0" class="block text-xs text-muted">{{ b.dieuKien }}</span>
                            </span>
                          </NuxtLink>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>

              <div class="rounded-3xl glass p-6">
                <h3 class="text-lg font-bold text-highlighted">
                  Eevee & đá tiến hoá
                </h3>
                <p class="mt-1 text-sm text-muted">
                  Eevee từ cấp {{ POKEDEX.capNhanhEevee }} dùng một viên đá để chọn nhánh (đá bị trừ khi dùng thành công).
                </p>
                <ul class="mt-4 grid grid-cols-2 gap-2 sm:grid-cols-4">
                  <li v-for="d in daEevee" :key="d.idItem">
                    <NuxtLink :to="`/pokedex/${d.dex}`" class="flex h-full flex-col items-center rounded-xl border border-default/60 p-2 text-center hover:border-primary">
                      <img :src="d.loai ? anhLoaiDex(d.loai) : ''" width="40" height="40" alt="" class="pixelated size-10" loading="lazy">
                      <span class="text-sm font-medium text-highlighted">{{ d.loai?.ten }}</span>
                      <span class="text-xs text-muted">{{ d.ten }}</span>
                    </NuxtLink>
                  </li>
                </ul>
                <h4 class="mt-5 font-semibold text-highlighted">
                  Cách kiếm đá
                </h4>
                <ul class="mt-2 list-disc space-y-1 pl-5 text-sm text-muted">
                  <li>Là Eevee cấp {{ POKEDEX.capNhanhEevee }}+ và hạ Pokémon hoang dã: mỗi con có {{ tiLeRoi }}% rơi 1 viên đá ngẫu nhiên.</li>
                  <li>Quái thấp hơn bạn quá {{ POKEDEX.lechCapRoiDa }} cấp sẽ không rơi đá — hãy đánh quái gần cấp mình.</li>
                  <li>Đạt mốc Pokédex 50 và 100 loài cũng được tặng 1 viên đá ngẫu nhiên.</li>
                </ul>
              </div>
            </div>
          </section>

          <!-- 4. Luyện cấp -->
          <section id="luyen-cap" class="scroll-mt-24" aria-labelledby="h-luyen-cap">
            <h2 id="h-luyen-cap" class="section-title text-3xl text-highlighted">
              4. Khu vực luyện cấp
            </h2>
            <p class="mt-3 max-w-3xl text-muted">
              Pokémon hoang dã theo từng khu vực, xếp theo cấp. Chọn nơi có quái gần cấp bạn để lên cấp nhanh và an toàn.
            </p>
            <ul class="mt-5 grid gap-3 md:grid-cols-2">
              <li v-for="m in dsMap" :key="m.id" class="flex flex-wrap items-center gap-3 rounded-2xl glass p-4">
                <div class="min-w-32">
                  <p class="font-semibold text-highlighted">
                    {{ m.ten }}
                  </p>
                  <p class="num text-xs text-muted">
                    Cấp {{ m.capThap === m.capCao ? m.capThap : `${m.capThap}–${m.capCao}` }}
                  </p>
                </div>
                <ul class="flex flex-wrap gap-1" :aria-label="`Pokémon ở ${m.ten}`">
                  <li v-for="d in m.dex" :key="d">
                    <NuxtLink :to="`/pokedex/${d}`" class="flex items-center gap-1 rounded-lg bg-screen py-0.5 pl-0.5 pr-2 text-xs hover:text-primary">
                      <img :src="L(d) ? anhLoaiDex(L(d)!) : ''" width="40" height="40" alt="" class="pixelated size-6" loading="lazy">
                      {{ L(d)?.ten }}
                    </NuxtLink>
                  </li>
                </ul>
              </li>
            </ul>
          </section>

          <!-- 5. Pokédex -->
          <section id="pokedex" class="scroll-mt-24" aria-labelledby="h-pokedex">
            <h2 id="h-pokedex" class="section-title text-3xl text-highlighted">
              5. Pokédex & phần thưởng
            </h2>
            <p class="mt-3 max-w-3xl text-muted">
              Mỗi loài bạn đánh trúng được ghi là <em>đã gặp</em>, hạ được thì thành <em>đã hạ</em>.
              Có {{ soLoaiHaDuoc }} loài quái và boss để sưu tầm. Đạt đủ số loài <strong class="text-highlighted">đã hạ</strong>
              thì nhận thưởng mốc qua NPC Pokédex (cần một ô trống trong hành trang với mốc có vật phẩm).
            </p>
            <ol class="mt-5 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
              <li v-for="m in POKEDEX.mocPokedex" :key="m.moc" class="rounded-3xl glass ring-gilded p-5">
                <p class="num text-4xl font-black text-gilded">
                  {{ m.moc }}
                </p>
                <p class="text-sm text-muted">
                  loài đã hạ
                </p>
                <ul class="mt-3 space-y-1 text-sm">
                  <li v-for="t in m.thuong" :key="t.ten" class="flex justify-between gap-2">
                    <span>{{ t.ten }}</span>
                    <span class="num text-highlighted">×{{ t.soLuong }}</span>
                  </li>
                </ul>
              </li>
            </ol>
          </section>

          <!-- 6. Thú cưỡi & pet -->
          <section id="thu-cuoi" class="scroll-mt-24" aria-labelledby="h-thu-cuoi">
            <h2 id="h-thu-cuoi" class="section-title text-3xl text-highlighted">
              6. Thú cưỡi & pet
            </h2>
            <p class="mt-3 max-w-3xl text-muted">
              Thú cưỡi giúp di chuyển nhanh (có loại cá nhân, của đội và của vùng); pet là Pokémon nhỏ đi theo bạn.
              Pet nuôi từ trứng lên cấp 5, tiến hoá dần theo cấp pet.
            </p>
            <h3 class="mt-6 text-lg font-bold text-highlighted">
              Thú cưỡi ({{ thuCuoi.length }} loài)
            </h3>
            <ul class="mt-3 grid grid-cols-3 gap-2 sm:grid-cols-4 md:grid-cols-6 xl:grid-cols-8">
              <li v-for="t in thuCuoi" :key="t.dex">
                <NuxtLink :to="`/pokedex/${t.dex}`" class="flex flex-col items-center rounded-xl glass p-2 text-center text-xs hover:text-primary">
                  <img :src="anhLoaiDex(t)" width="40" height="40" alt="" class="pixelated size-10" loading="lazy">
                  {{ t.ten }}
                </NuxtLink>
              </li>
            </ul>
            <h3 class="mt-8 text-lg font-bold text-highlighted">
              Pet nuôi theo cấp
            </h3>
            <ul class="mt-3 grid gap-3 md:grid-cols-2">
              <li v-for="(c, i) in chuoiPet" :key="i" class="flex flex-wrap items-center gap-2 rounded-2xl glass p-3">
                <template v-for="(b, j) in c" :key="b.dex">
                  <UIcon v-if="j > 0" name="i-lucide-chevron-right" class="size-4 text-dimmed" aria-hidden="true" />
                  <NuxtLink :to="`/pokedex/${b.dex}`" class="flex items-center gap-1.5 text-sm hover:text-primary">
                    <img :src="b.loai ? anhLoaiDex(b.loai) : ''" width="40" height="40" alt="" class="pixelated size-8" loading="lazy">
                    <span>
                      <span class="font-medium text-highlighted">{{ b.loai?.ten }}</span>
                      <span class="block text-[11px] text-muted">{{ b.dieuKien }}</span>
                    </span>
                  </NuxtLink>
                </template>
              </li>
            </ul>
            <h3 class="mt-8 text-lg font-bold text-highlighted">
              Pet khác ({{ petKhac.length }} loài)
            </h3>
            <ul class="mt-3 flex flex-wrap gap-1.5">
              <li v-for="p in petKhac" :key="p.dex">
                <NuxtLink :to="`/pokedex/${p.dex}`" class="flex items-center gap-1 rounded-lg bg-screen py-0.5 pl-0.5 pr-2 text-xs hover:text-primary">
                  <img :src="anhLoaiDex(p)" width="40" height="40" alt="" class="pixelated size-6" loading="lazy">
                  {{ p.ten }}
                </NuxtLink>
              </li>
            </ul>
          </section>

          <!-- 7. Đội & vùng -->
          <section id="doi-vung" class="scroll-mt-24" aria-labelledby="h-doi-vung">
            <h2 id="h-doi-vung" class="section-title text-3xl text-highlighted">
              7. Đội & vùng
            </h2>
            <div class="mt-5 grid gap-4 md:grid-cols-2">
              <div class="rounded-3xl glass p-6">
                <h3 class="text-lg font-bold text-highlighted">
                  Vùng {{ POKEDEX.vung.join(' / ') }}
                </h3>
                <p class="mt-2 text-sm text-muted">
                  Mỗi người chơi thuộc một trong các vùng {{ POKEDEX.vung.join(', ') }}. Bản đồ có bản riêng cho từng vùng,
                  và các vùng tranh tài với nhau trong những sự kiện liên vùng.
                </p>
                <ul class="mt-4 flex flex-wrap gap-2">
                  <li v-for="v in POKEDEX.vung" :key="v">
                    <UBadge color="secondary" variant="subtle" size="lg">
                      {{ v }}
                    </UBadge>
                  </li>
                </ul>
              </div>
              <div class="rounded-3xl glass p-6">
                <h3 class="text-lg font-bold text-highlighted">
                  Đội
                </h3>
                <p class="mt-2 text-sm text-muted">
                  Lập hoặc gia nhập một Đội cùng bạn bè. Đội trưởng quản lý thành viên, và đội có thú cưỡi đội riêng.
                  Đi cùng đội để săn boss và tham gia sự kiện dễ hơn. Xem
                  <NuxtLink to="/ranking" class="text-primary hover:underline">bảng xếp hạng</NuxtLink>
                  để biết ai đang dẫn đầu.
                </p>
              </div>
            </div>
          </section>

          <!-- 8. Điều khiển -->
          <section id="dieu-khien" class="scroll-mt-24" aria-labelledby="h-dieu-khien">
            <h2 id="h-dieu-khien" class="section-title text-3xl text-highlighted">
              8. Mẹo & điều khiển
            </h2>
            <ul class="mt-5 grid gap-2 md:grid-cols-2">
              <li v-for="(d, i) in POKEDEX.huongDanGame" :key="i" class="flex gap-3 rounded-xl glass px-4 py-3 text-sm">
                <UIcon name="i-lucide-sparkle" class="mt-0.5 size-4 shrink-0 text-primary" aria-hidden="true" />
                <span>{{ d }}</span>
              </li>
            </ul>
            <div class="mt-8 flex flex-wrap gap-3">
              <UButton to="/download" color="primary" icon="i-lucide-download">
                Tải game
              </UButton>
              <UButton to="/pokedex" color="neutral" variant="outline" icon="i-lucide-book-open">
                Mở Pokédex
              </UButton>
            </div>
          </section>
        </div>
      </div>
    </UContainer>
  </div>
</template>
