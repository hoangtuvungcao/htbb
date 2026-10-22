<script setup lang="ts">
/** Pokédex & Shiny — pokedex.json (mốc thưởng, loài shiny) + cam-nang.json (nhiệm vụ nghiên cứu, sự kiện). */
const soLoaiHaDuoc = POKEDEX.loai.filter(l => l.quai.length).length
const loaiShiny = POKEDEX.loai.filter(l => l.anhShiny && l.quai.some(q => q.shiny))
const N = CAM_NANG.nhiemVu
const SH = CAM_NANG_ND.shiny
const suKienShiny = CAM_NANG.suKien.filter(s => /shiny/i.test(`${s.ten1} ${s.coChe}`))
</script>

<template>
  <WikiShell slug="pokedex-shiny">
    <WikiSection id="ghi-nhan" title="Pokédex ghi nhận gì">
      <div class="grid gap-3 sm:grid-cols-3">
        <div class="the-so">
          <b>{{ soLoaiHaDuoc }}</b><span>loài quái & boss có thể hạ</span>
        </div>
        <div class="the-so">
          <b>Đã gặp</b><span>khi bạn đánh trúng loài đó lần đầu</span>
        </div>
        <div class="the-so">
          <b>Đã hạ</b><span>khi bạn hạ gục được ít nhất một con</span>
        </div>
      </div>
      <p class="van mt-4">
        Mở Pokédex trong game ở Menu → Pokédex (hoặc NPC Đổi thưởng → Pokédex). Số loài <strong>đã hạ</strong> quyết định mốc thưởng.
        Website có <NuxtLink to="/pokedex" class="lien-ket">Pokédex đầy đủ</NuxtLink> để tra hệ, nơi xuất hiện và tiến hoá.
      </p>
    </WikiSection>

    <WikiSection id="moc" title="Mốc thưởng" lead="Nhận trong mục Pokédex (NPC Đổi thưởng) khi đủ số loài đã hạ (cần một ô trống trong hành trang nếu mốc có vật phẩm).">
      <ol class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <li v-for="m in POKEDEX.mocPokedex" :key="m.moc" class="rounded-3xl glass ring-gilded p-5">
          <p class="font-display text-4xl font-black text-gilded">
            {{ m.moc }}
          </p>
          <p class="text-sm text-muted">
            loài đã hạ
          </p>
          <ul class="mt-3 space-y-1 text-sm">
            <li v-for="t in m.thuong" :key="t.ten" class="flex justify-between gap-2">
              <span>{{ t.ten }}</span><span class="num text-highlighted">×{{ t.soLuong }}</span>
            </li>
          </ul>
        </li>
      </ol>
    </WikiSection>

    <WikiSection id="nghien-cuu" title="Nhiệm vụ nghiên cứu" :lead="`Ngoài mốc, mỗi loài có nhiệm vụ nghiên cứu riêng (${N.pokedexSoLoai} loài × 2 bước): gặp lần đầu, rồi hạ đủ số lượng. Phần thưởng gồm EXP, xu và lượng khoá.`">
      <ul class="cham">
        <li><strong>Gặp:</strong> đánh trúng loài đó 1 lần.</li>
        <li><strong>Hạ:</strong> hạ 50 con cùng loài — dễ làm cùng lúc với nhiệm vụ ngày.</li>
        <li>Lễ hội Pokédex trong lịch sự kiện cho <strong>×2 điểm nghiên cứu</strong>.</li>
      </ul>
    </WikiSection>

    <WikiSection id="shiny" title="Pokémon Shiny" :lead="`Mỗi lần hạ quái có 1/${soVn(SH.mauSo)} cơ hội gặp bản Shiny (màu khác hẳn); lần hạ thứ ${soVn(SH.pity)} kể từ lần Shiny gần nhất chắc chắn ra Shiny. Shiny được ghi vào Pokédex và tính danh hiệu — không cộng sức mạnh.`">
      <ul class="grid grid-cols-2 gap-3 sm:grid-cols-4 lg:grid-cols-6">
        <li v-for="l in loaiShiny" :key="l.dex">
          <NuxtLink :to="`/pokedex/${l.dex}`" class="group block overflow-hidden rounded-2xl glass text-center transition hover:-translate-y-0.5">
            <PokeSan :dex="l.dex" shiny :he="l.he" :size="96" class="w-full" />
            <span class="block py-2 text-sm font-bold text-highlighted group-hover:text-primary">{{ l.ten }}</span>
          </NuxtLink>
        </li>
      </ul>
      <ul class="cham mt-5">
        <li v-for="s in suKienShiny" :key="s.tuan">
          <strong>{{ s.ten1 }}</strong> (tuần {{ s.tuan }}): {{ s.coChe }}
        </li>
        <li>Ngày cộng đồng: trong 3 giờ vàng, dòng khởi đầu được chọn xuất hiện dày và tỉ lệ shiny ×5.</li>
      </ul>
    </WikiSection>
  </WikiShell>
</template>
