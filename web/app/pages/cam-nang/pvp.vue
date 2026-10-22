<script setup lang="ts">
import { CUP, NGAM_TAY, XEP_HANG } from '~~/shared/utils/heThong098'
const { camNang: CAM_NANG } = await useCamNang() // số liệu CSDL game, dự phòng JSON
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
        <li>Điểm chỉ đổi trong <strong>trận xếp hạng</strong>: nói chuyện với NPC Đấu trường → <strong>Đấu xếp hạng</strong> (từ cấp {{ XEP_HANG.cap }}). Máy chủ ghép đối thủ lệch tối đa {{ XEP_HANG.lechCap }} cấp và gần Elo, đếm ngược {{ XEP_HANG.demNguoc }} giây rồi đấu ở Đấu trường (tối đa {{ XEP_HANG.giayToiDa }} giây). Hạ người ngoài thế giới không cộng Elo.</li>
        <li>Trận ngắn hơn {{ XEP_HANG.giayToiThieu }} giây hoặc người thua không gây được {{ XEP_HANG.stToiThieuPhanTram }}% HP đối thủ thì bị huỷ (không Elo). Gặp lại cùng đối thủ trong 24 giờ: lần 2 nửa điểm, lần 3 một phần tư, từ lần 4 không tính; không bao giờ ghép hai tài khoản cùng thiết bị / IP.</li>
        <li>Cần ít nhất {{ XEP_HANG.tranToiThieuMua }} trận trong mùa để nhận thưởng mùa (lượng khoá theo bậc + danh hiệu mùa có hạn).</li>
        <li>Sang mùa mới, điểm được đặt lại mềm: giữ một nửa phần điểm trên {{ P.eloBanDau }}; số trận thắng/thua về 0.</li>
      </ul>
      <p class="mt-4 text-sm text-muted">
        Lịch chốt mùa trong năm: {{ muaPvp.map(m => `tuần ${m.tuan}`).join(', ') }} — xem <NuxtLink to="/cam-nang/su-kien" class="lien-ket">Sự kiện năm</NuxtLink>.
      </p>
    </WikiSection>

    <WikiSection id="che-do" title="Các chế độ PvP">
      <ul class="grid gap-3 sm:grid-cols-2">
        <li class="rounded-2xl glass p-4">
          <strong class="font-display text-highlighted">PK trên bản đồ</strong>
          <p class="mt-1 text-sm text-muted">
            Đeo khăn PK để bật chiến đấu với người chơi khác. Không tính điểm thang mùa (chỉ trận xếp hạng tính).
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

    <WikiSection id="ngam-tay" title="Ngắm tay (0.9.8)" lead="Mọi trận giữa người chơi — PK, thách đấu, xếp hạng, sàn đấu, chiến trường, Cúp PvP — không tự động đánh, không tự nhắm: kéo chiêu để ngắm rồi thả để bắn (bản PC: chuột).">
      <ul class="cham">
        <li>{{ NGAM_TAY.soChieu }} chiêu có kiểu bắn riêng: đạn bay thẳng, nón, vùng, xuyên, quanh thân — tầm và độ rộng hiện khi kéo.</li>
        <li>Đánh trúng / bị người chơi đánh trúng trong {{ NGAM_TAY.giayGiaoChien }} giây thì vẫn tính là đang giao chiến (tắt tự động).</li>
        <li>Mạng trễ: vị trí đối thủ trong {{ NGAM_TAY.buTreMs }} ms trước khi đạn tới vẫn tính trúng.</li>
        <li>Bản cũ (trước 0.9.8) chỉ bắn thẳng theo hướng đang quay — hãy cập nhật để kéo ngắm.</li>
      </ul>
    </WikiSection>

    <WikiSection id="cup" title="Cúp PvP tuần" :lead="`Mỗi tuần một thể thức (${CUP.loai.join(', ')}). Đăng ký trong game từ cấp ${CUP.cap}; Chủ nhật ${CUP.gioVongDau}:00 thi đấu ${CUP.soVong} vòng Thuỵ Sĩ, mỗi vòng ${CUP.phutMoiVong} phút.`">
      <ul class="cham">
        <li>Thắng {{ CUP.diem.thang }} điểm, hoà {{ CUP.diem.hoa }}, thua {{ CUP.diem.thua }}; vòng sau ghép người cùng điểm. Dưới {{ CUP.soDoiToiThieu }} đội thì cúp tuần đó hoãn.</li>
        <li>Chỉ số chuẩn hoá theo cấp — thắng nhờ chiêu, hệ và ngắm, không nhờ đồ.</li>
        <li>Thưởng: danh hiệu có hạn + lượng khoá nhỏ (vô địch {{ CUP.luongKhoa.voDich }}, hạng 2–3 {{ CUP.luongKhoa.hang23 }}, hạng 4–8 {{ CUP.luongKhoa.hang48 }}, tham gia {{ CUP.luongKhoa.thamGia }}), không xu.</li>
      </ul>
      <p class="mt-3 text-sm">
        <NuxtLink to="/cup" class="lien-ket">Xem cúp tuần này, bảng điểm và nhà vô địch →</NuxtLink>
      </p>
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
