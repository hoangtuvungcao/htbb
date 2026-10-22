<script setup lang="ts">
/**
 * PvP xếp hạng mùa — luật thang từ ThangPvp.java / ThangPvpGame.java (cam-nang-noi-dung.json),
 * lịch chốt mùa từ su_kien_nam.csv, luật PK từ ChuPokemon.HUONG_DAN (pokedex.json).
 * Thưởng cuối mùa và thưởng top tuần CHƯA được game tự phát -> ghi "sẽ công bố", không hứa số.
 */
const P = CAM_NANG_ND.pvp
const muaPvp = CAM_NANG.suKien.filter(s => s.loai === 'pvp')
const MAU = ['bg-pokered-500', 'bg-pokeblue-500', 'bg-pikachu-400', 'bg-[#7038f8]']
const thanhTuuPvp = CAM_NANG.nhiemVu.thanhTuu.filter(t => t.kieu === 'pvp')
const luatPk = POKEDEX.huongDanGame.filter(t => /PK/.test(t))
const soMuaNam = Math.round(52 / P.tuanMoiMua)
</script>

<template>
  <WikiShell slug="pvp">
    <WikiSection id="mua" title="Thang PvP mùa" :lead="`Mỗi mùa dài ${P.tuanMoiMua} tuần (${soMuaNam} mùa mỗi năm), tính từ ngày khai trương máy chủ. Xem điểm và bậc của bạn trong Sổ tay huấn luyện → Thang PvP mùa.`">
      <ol class="flex flex-wrap items-center gap-2">
        <li v-for="(b, i) in P.bac" :key="b.ten" class="flex items-center gap-2">
          <UIcon v-if="i > 0" name="i-lucide-chevron-right" class="size-4 text-dimmed" aria-hidden="true" />
          <span class="flex items-center gap-2 rounded-full bg-elevated py-1 pl-1 pr-3 font-display font-bold text-highlighted">
            <span class="size-6 rounded-full ring-2 ring-white" :class="MAU[i]" aria-hidden="true" /> {{ b.ten }}
            <span class="text-xs font-semibold text-muted">{{ i === 0 ? `dưới ${P.bac[1]?.tuElo}` : `từ ${b.tuElo}` }}</span>
          </span>
        </li>
      </ol>
      <ul class="cham mt-5">
        <li>Mọi người bắt đầu với <strong>{{ P.eloBanDau }} điểm</strong>. Thắng đối thủ mạnh hơn được nhiều điểm hơn, thua đối thủ yếu hơn mất nhiều điểm hơn (tối đa ±{{ P.k }} điểm mỗi trận).</li>
        <li>Trận được tính khi bạn hạ một người chơi <strong>cùng dải {{ P.daiCap }} cấp</strong> (1–10, 11–20, …) trong chế độ PK.</li>
        <li>Cùng một cặp người chơi chỉ được tính 1 trận mỗi {{ P.phutCungCap }} phút — không cày điểm bằng cách đánh nhau liên tục.</li>
        <li>Sang mùa mới, điểm được đặt lại mềm: giữ một nửa phần điểm trên {{ P.eloBanDau }}; số trận thắng/thua về 0.</li>
      </ul>
      <WikiCallout kieu="sap-ra-mat" title="Thưởng cuối mùa">
        Phần thưởng chốt mùa (khung theo bậc, danh hiệu) sẽ được công bố trong tin tức trước khi mùa đầu tiên kết thúc.
        Lịch chốt mùa trong năm: {{ muaPvp.map(m => `tuần ${m.tuan}`).join(', ') }} — xem <NuxtLink to="/cam-nang/su-kien" class="lien-ket">Sự kiện năm</NuxtLink>.
      </WikiCallout>
    </WikiSection>

    <WikiSection id="che-do" title="Các chế độ PvP">
      <ul class="grid gap-3 sm:grid-cols-2">
        <li class="rounded-2xl glass p-4">
          <strong class="font-display text-highlighted">PK trên bản đồ</strong>
          <p class="mt-1 text-sm text-muted">
            Đeo khăn PK để bật chiến đấu với người chơi khác. Đây là nơi tính điểm thang PvP mùa.
          </p>
        </li>
        <li class="rounded-2xl glass p-4">
          <strong class="font-display text-highlighted">Giải Liên minh</strong>
          <p class="mt-1 text-sm text-muted">
            Các Sân Liên minh, từ cấp 50. Đăng ký ở NPC Tổng quản → "Đăng ký giải Liên minh".
          </p>
        </li>
        <li class="rounded-2xl glass p-4">
          <strong class="font-display text-highlighted">Giải đấu tuần</strong>
          <p class="mt-1 text-sm text-muted">
            Cây đấu loại trực tiếp công bố thứ Bảy 20:00, trận đánh ở Giải Liên minh — thuộc <NuxtLink to="/cam-nang/he-sinh-thai#giai-tri" class="lien-ket">hệ sinh thái</NuxtLink> (sắp mở).
          </p>
        </li>
        <li class="rounded-2xl glass p-4">
          <strong class="font-display text-highlighted">Thế chiến vùng</strong>
          <p class="mt-1 text-sm text-muted">
            Ba vùng giành cờ vào tối thứ Bảy trong các tuần chiến vùng — xem <NuxtLink to="/cam-nang/doi-su-pham#vung" class="lien-ket">Vùng</NuxtLink>.
          </p>
        </li>
      </ul>
      <WikiCallout kieu="vi-sao">
        Sát thương giữa người chơi được giảm mạnh và có trần mỗi đòn, hệ số khắc hệ tối đa ×2 — trận PvP kéo dài đủ để xoay chiêu, dùng thuốc và phản công.
      </WikiCallout>
    </WikiSection>

    <WikiSection id="pk" title="Luật PK">
      <ul class="cham">
        <li v-for="t in luatPk" :key="t">
          {{ t }}
        </li>
      </ul>
    </WikiSection>

    <WikiSection id="xep-hang-tuan" title="Xếp hạng & đua top">
      <p class="van">
        <NuxtLink to="/ranking" class="lien-ket">Bảng xếp hạng</NuxtLink> trên website cập nhật theo dữ liệu game. Các đợt <strong>đua top</strong> được ban quản trị công bố kèm phần thưởng;
        người đạt giải nhận thưởng ở NPC Tổng quản → "Nhận thưởng đua top".
      </p>
      <ul v-if="thanhTuuPvp.length" class="mt-4 flex flex-wrap gap-2" aria-label="Thành tựu PvP">
        <li v-for="t in thanhTuuPvp" :key="t.ten">
          <UBadge color="warning" variant="subtle" icon="i-lucide-award">
            {{ t.ten }}
          </UBadge>
        </li>
      </ul>
    </WikiSection>
  </WikiShell>
</template>
