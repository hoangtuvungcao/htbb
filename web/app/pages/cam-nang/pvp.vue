<script setup lang="ts">
/** PvP xếp hạng mùa — lịch mùa từ su_kien_nam.csv, lượng tuần từ ThongSoLauDai.java, luật PK từ pokedex.json (Text.help). */
const muaPvp = CAM_NANG.suKien.filter(s => s.loai === 'pvp')
const K = CAM_NANG.kinhTe
const BAC = [
  { ten: 'Poké Ball', mau: 'bg-pokered-500' },
  { ten: 'Great Ball', mau: 'bg-pokeblue-500' },
  { ten: 'Ultra Ball', mau: 'bg-pikachu-400' },
  { ten: 'Master Ball', mau: 'bg-[#7038f8]' },
]
const thanhTuuPvp = CAM_NANG.nhiemVu.thanhTuu.filter(t => t.kieu === 'pvp')
const luatPk = POKEDEX.huongDanGame.filter(t => /PK/.test(t))
</script>

<template>
  <WikiShell slug="pvp">
    <WikiSection id="mua" title="Mùa giải" :lead="`Mỗi năm có ${muaPvp.length} mùa PvP, mỗi mùa khoảng 3 tháng. Cuối mùa bậc được chốt và phát thưởng theo bậc.`">
      <ol class="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
        <li v-for="(m, i) in muaPvp" :key="m.tuan" class="rounded-2xl glass p-4">
          <p class="font-display text-lg font-extrabold text-highlighted">
            Mùa {{ i + 1 }}
          </p>
          <p class="text-sm text-muted">
            Kết thúc tuần {{ m.tuan }} (tháng {{ m.thang }})
          </p>
          <p class="mt-2 text-xs text-dimmed">
            {{ m.phanThuong }}
          </p>
        </li>
      </ol>
      <h3 class="mt-8 font-display text-xl font-bold text-highlighted">
        Bậc xếp hạng
      </h3>
      <ol class="mt-3 flex flex-wrap items-center gap-2">
        <li v-for="(b, i) in BAC" :key="b.ten" class="flex items-center gap-2">
          <UIcon v-if="i > 0" name="i-lucide-chevron-right" class="size-4 text-dimmed" />
          <span class="flex items-center gap-2 rounded-full bg-elevated py-1 pl-1 pr-3 font-display font-bold text-highlighted">
            <span class="size-6 rounded-full ring-2 ring-white" :class="b.mau" /> {{ b.ten }}
          </span>
        </li>
      </ol>
      <p class="van mt-3">
        Thắng trận đấu xếp hạng để tích điểm và lên bậc. Thưởng cuối mùa: khung avatar theo bậc và lượng (không thể mua bằng tiền).
      </p>
    </WikiSection>

    <WikiSection id="che-do" title="Các chế độ PvP">
      <ul class="grid gap-3 sm:grid-cols-2">
        <li class="rounded-2xl glass p-4">
          <strong class="font-display text-highlighted">Đấu trường xếp hạng</strong>
          <p class="mt-1 text-sm text-muted">
            Đấu 1–1 tính điểm mùa. Vào qua Cao nguyên Indigo.
          </p>
        </li>
        <li class="rounded-2xl glass p-4">
          <strong class="font-display text-highlighted">Lôi đài</strong>
          <p class="mt-1 text-sm text-muted">
            Thách đấu trực tiếp bạn bè hoặc đối thủ trên lôi đài.
          </p>
        </li>
        <li class="rounded-2xl glass p-4">
          <strong class="font-display text-highlighted">Liên đấu tuần</strong>
          <p class="mt-1 text-sm text-muted">
            Giải theo tuần, thắng nhận lượng khoá.
          </p>
        </li>
        <li class="rounded-2xl glass p-4">
          <strong class="font-display text-highlighted">Thế chiến vùng</strong>
          <p class="mt-1 text-sm text-muted">
            Ba vùng giành cờ trên chiến trường ba làn — xem <NuxtLink to="/cam-nang/doi-su-pham#vung" class="lien-ket">Vùng</NuxtLink>.
          </p>
        </li>
      </ul>
      <WikiCallout kieu="vi-sao">
        Sát thương giữa người chơi được giảm mạnh và có trần mỗi đòn, hệ số khắc hệ tối đa ×2 — trận PvP kéo dài đủ để xoay chiêu, dùng thuốc và phản công.
      </WikiCallout>
    </WikiSection>

    <WikiSection id="pk" title="PK ngoài bản đồ">
      <ul class="cham">
        <li v-for="t in luatPk" :key="t">
          {{ t }}
        </li>
      </ul>
    </WikiSection>

    <WikiSection id="xep-hang-tuan" title="Xếp hạng tuần">
      <p class="van">
        Bảng xếp hạng tuần (cấp, PvP, đội) phát <strong>lượng</strong> — loại tiền giao dịch được duy nhất không đến từ nạp:
        hạng 1 nhận {{ K.luongXepHangTuan[0]?.luong }}, top {{ K.luongXepHangTuan.at(-1)?.top }} nhận {{ K.luongXepHangTuan.at(-1)?.luong }}.
        Xem <NuxtLink to="/ranking" class="lien-ket">bảng xếp hạng</NuxtLink>.
      </p>
      <ul v-if="thanhTuuPvp.length" class="mt-4 flex flex-wrap gap-2">
        <li v-for="t in thanhTuuPvp" :key="t.ten">
          <UBadge color="warning" variant="subtle" icon="i-lucide-award">
            {{ t.ten }}
          </UBadge>
        </li>
      </ul>
    </WikiSection>
  </WikiShell>
</template>
