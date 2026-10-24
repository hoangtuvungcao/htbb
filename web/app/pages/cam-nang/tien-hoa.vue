<script setup lang="ts">
/** Tiến hoá & đá tiến hoá — số liệu từ pokedex.json (LoaiPokemon/DaTienHoa/Pokedex.java). */
const L = (dex: number) => loaiTheoDex(dex)
const dong = POKEDEX.khoiDau.map((k) => {
  const dau = L(k.dex)!
  return { ...k, dau, chuoi: (dau.tienHoa?.buoc ?? []).map(b => ({ ...b, loai: L(b.dex) })), laEevee: dau.tienHoa?.kieu === 'eevee' }
}).sort((a, b) => a.dex - b.dex)
const dongCap = dong.filter(d => !d.laEevee)
const daEevee = POKEDEX.daTienHoa.map(d => ({ ...d, loai: L(d.dex) }))
const tiLeRoi = (POKEDEX.tiLeRoiDaPhanNghin / 10).toLocaleString('vi-VN')
const pt = POKEDEX.phanTramGiaiDoan
const mocCoDa = POKEDEX.mocPokedex.filter(m => m.thuong.some(t => /đá/i.test(t.ten))).map(m => m.moc)
</script>

<template>
  <WikiShell slug="tien-hoa">
    <p class="van">
      Bạn <strong>chính là</strong> Pokémon của mình — lên cấp đủ mốc thì tự tiến hoá ngay tại chỗ, người chơi xung quanh cũng thấy.
      Mỗi dạng sau mạnh hơn: dạng 2 <strong>+{{ pt[2] }}%</strong>, dạng 3 <strong>+{{ pt[3] }}%</strong> HP và tấn công,
      nhánh Eevee <strong>+{{ pt.eevee }}%</strong>. Có thể đổi hệ (ví dụ Charizard thêm hệ Bay) — xem lại
      <NuxtLink to="/cam-nang/he-khac-che" class="lien-ket">bảng khắc hệ</NuxtLink> sau khi tiến hoá.
    </p>

    <WikiSection id="theo-cap" title="Tiến hoá theo cấp" lead="Tự động khi đạt cấp, không cần vật phẩm. Không thể huỷ hay trì hoãn.">
      <ul class="grid gap-4">
        <li v-for="d in dongCap" :key="d.dex" class="rounded-3xl glass p-4">
          <p class="mb-3 font-display text-sm font-extrabold uppercase tracking-wide text-dimmed">
            {{ d.dong }}
          </p>
          <ol class="flex flex-wrap items-center gap-2 sm:gap-3">
            <li v-for="(b, i) in d.chuoi" :key="b.dex" class="flex items-center gap-2 sm:gap-3">
              <span v-if="i > 0" class="flex flex-col items-center text-xs font-bold text-primary">
                <UIcon name="i-lucide-arrow-right" class="size-5" />{{ b.dieuKien }}
              </span>
              <NuxtLink :to="`/pokedex/${b.dex}`" class="group block w-28 overflow-hidden rounded-2xl border border-default text-center transition hover:-translate-y-0.5 sm:w-32">
                <PokeSan :dex="b.dex" :he="b.loai?.he ?? []" :size="104" class="w-full" />
                <span class="block py-1.5 font-display text-sm font-bold text-highlighted group-hover:text-primary">{{ b.loai?.ten }}</span>
              </NuxtLink>
            </li>
          </ol>
        </li>
      </ul>
    </WikiSection>

    <WikiSection id="eevee" title="Eevee & đá tiến hoá" :lead="`Eevee không tự tiến hoá. Từ cấp ${POKEDEX.capNhanhEevee}, mở Pokédex (Menu → Pokédex, hoặc Quầy đổi thưởng → Pokédex) và dùng 1 viên đá để chọn 1 trong ${daEevee.length} nhánh — lựa chọn là vĩnh viễn, đá bị trừ khi tiến hoá thành công.`">
      <ul class="grid grid-cols-2 gap-3 sm:grid-cols-4">
        <li v-for="d in daEevee" :key="d.idItem">
          <NuxtLink :to="`/pokedex/${d.dex}`" class="group block overflow-hidden rounded-2xl glass text-center transition hover:-translate-y-0.5">
            <PokeSan :dex="d.dex" :he="d.loai?.he ?? []" :size="110" class="w-full" />
            <span class="block px-2 pt-2 font-display font-bold text-highlighted group-hover:text-primary">{{ d.loai?.ten }}</span>
            <span class="mt-1 flex justify-center gap-1"><PokeHeBadge v-for="h in d.loai?.he ?? []" :key="h" :he="h" size="sm" /></span>
            <span class="block px-2 pb-3 pt-1.5 text-xs font-semibold text-muted">{{ d.ten }}</span>
          </NuxtLink>
        </li>
      </ul>
      <WikiCallout kieu="meo" title="Chọn nhánh nào?">
        Chọn theo lối chơi và theo hệ của đội: Vaporeon trâu bò, Jolteon nhanh, Flareon dồn sát thương, Espeon/Sylveon mạnh chiêu,
        Umbreon chịu đòn, Leafeon/Glaceon khắc nhiều quái hệ Đất/Rồng. Bảng chiêu của Eevee giữ nguyên, nhưng STAB đổi theo hệ mới.
      </WikiCallout>
    </WikiSection>

    <WikiSection id="kiem-da" title="Cách kiếm đá">
      <ul class="cham">
        <li>Là Eevee cấp {{ POKEDEX.capNhanhEevee }}+ và hạ Pokémon hoang dã: mỗi con có <strong>{{ tiLeRoi }}%</strong> rơi 1 viên đá ngẫu nhiên.</li>
        <li>Quái thấp hơn bạn quá <strong>{{ POKEDEX.lechCapRoiDa }} cấp</strong> không rơi đá — hãy đánh quái gần cấp mình.</li>
        <li v-if="mocCoDa.length">Đạt mốc Pokédex <strong>{{ mocCoDa.join(' và ') }} loài</strong> được tặng đá ngẫu nhiên (xem <NuxtLink to="/cam-nang/pokedex-shiny#moc" class="lien-ket">mốc Pokédex</NuxtLink>).</li>
        <li>Ngày cộng đồng Eevee trong <NuxtLink to="/cam-nang/su-kien" class="lien-ket">lịch sự kiện</NuxtLink> nhân đôi tỉ lệ rơi đá.</li>
      </ul>
    </WikiSection>
  </WikiShell>
</template>
