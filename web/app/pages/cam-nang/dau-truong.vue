<script setup lang="ts">
/**
 * Cẩm nang Đấu trường Bảo Bối 3v3 — số liệu từ shared/utils/dauTruong.ts (khớp com.htbb.pokemon.DauTruongLogic /
 * DauTruongTran / DauTruongDuLieu). Bảng xếp hạng: /dau-truong.
 */
import { DAU_TRUONG } from '../../../shared/utils/dauTruong'

const D = DAU_TRUONG
const BAC = D.tenBac.map((ten, i) => ({ ten, tu: i === 0 ? 0 : D.nguongBac[i - 1] }))
</script>

<template>
  <WikiShell slug="dau-truong">
    <WikiSection id="the-thuc" title="Thể thức" :lead="`Đội 3 Pokémon từ Hộp PC đấu đội phòng thủ của người khác theo lượt. Mọi con chuẩn hoá cấp ${D.capChuanHoa}; máy chủ tính trận, bạn chọn chiêu.`">
      <ul class="cham">
        <li>Vào ở NPC <strong>Đấu trường</strong> (các thị trấn) → "Đấu trường Bảo Bối". Cần cấp {{ D.capToiThieu }} và bản game mới.</li>
        <li><strong>Đội tấn công</strong> và <strong>đội phòng thủ</strong> xếp riêng ở tab Đội hình: 3 con khác loài, không Huyền thoại (T6). Chính bạn (Pokémon khởi đầu) không vào đội — chỉ Pokémon đã thu phục.</li>
        <li>Đội phòng thủ đấu thay bạn cả khi bạn offline (máy chọn chiêu và đổi Pokémon cho đội phòng thủ).</li>
        <li>Mỗi ngày {{ D.luotNgay }} trận tấn công (tính lúc vào trận, rời trận không hoàn lượt), làm mới danh sách đối thủ {{ D.lamMoiNgay }} lần.</li>
        <li>Mỗi lượt có {{ D.giayLuot }} giây chọn chiêu hoặc đổi Pokémon; hết giờ máy chọn giúp. Bật "Tự động" để máy đánh hết trận. Tối đa {{ D.luotToiDa }} lượt — hết lượt, bên còn nhiều % máu hơn thắng (bằng nhau: đội phòng thủ thắng).</li>
      </ul>
    </WikiSection>

    <WikiSection id="chi-so" title="Chỉ số & sát thương">
      <p class="van">
        Chỉ số theo công thức chính bản ở cấp 50 (không EV): HP = ⌊(2 × nền + IV) × 50 / 100⌋ + 60; chỉ số khác = (⌊(2 × nền + IV) × 50 / 100⌋ + 5) × tính cách (+10% / −10%).
        IV 0–31 và tính cách của từng con trong Hộp PC có tác dụng thật ở đây — con 31 IV mạnh hơn con 0 IV cùng loài khoảng 15 điểm mỗi chỉ số.
      </p>
      <p class="van mt-3">
        Sát thương = ⌊⌊22 × uy lực × Tấn công / Phòng thủ⌋ / 50⌋ + 2, rồi lần lượt: chí mạng ×1,5 (1/24), ngẫu nhiên 85–100%, cùng hệ (STAB) ×1,5,
        khắc hệ <strong>chính xác</strong> (×0 / ×0,25 / ×0,5 / ×1 / ×2 / ×4 — không làm dịu như ngoài thế giới), bỏng ×0,5 với đòn vật lý.
        Bậc chỉ số ±6 kiểu chính bản. Ai nhanh hơn đánh trước; đổi Pokémon luôn trước chiêu.
      </p>
    </WikiSection>

    <WikiSection id="chieu" title="Chiêu & trạng thái" lead="Mỗi loài dùng 4 chiêu của mình. Uy lực chuẩn hoá theo vai trò chiêu (không lấy số gốc chính bản).">
      <div class="overflow-x-auto">
        <table class="w-full text-left text-sm">
          <thead class="text-xs uppercase text-muted">
            <tr>
              <th class="py-2 pr-3">Vai trò</th>
              <th class="py-2 pr-3">Uy lực / tác dụng</th>
              <th class="py-2 pr-3 text-right">Chính xác</th>
              <th class="py-2 text-right">PP</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="v in D.vaiTro" :key="v.vai" class="border-t border-default">
              <td class="py-2 pr-3 font-semibold">{{ v.vai }}</td>
              <td class="py-2 pr-3">{{ v.uyLuc }}</td>
              <td class="py-2 pr-3 text-right">{{ v.chinhXac }}%</td>
              <td class="py-2 text-right">{{ v.pp }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <ul class="cham mt-4">
        <li><strong>Bỏng</strong>: mất 1/16 HP mỗi lượt, đòn vật lý ×0,5 (hệ Lửa miễn). <strong>Độc</strong>: mất 1/8 HP (Độc, Thép miễn).</li>
        <li><strong>Tê liệt</strong>: tốc độ ×0,5, 25% đứng im (Điện miễn). <strong>Ngủ</strong>: 1–3 lượt. <strong>Đóng băng</strong>: 20% tan mỗi lượt, tối đa 3 (Băng miễn).</li>
        <li><strong>Lú lẫn</strong> 2–5 lượt (33% tự đánh mình), <strong>choáng</strong> (chỉ khi đánh trước), giảm Tốc độ / Phòng thủ / Tấn công / Chính xác.</li>
        <li>Hết PP cả 4 chiêu: "Vùng vẫy" (uy lực 50, mất 1/4 HP tối đa).</li>
      </ul>
    </WikiSection>

    <WikiSection id="diem" title="Điểm mùa & ghép trận">
      <ol class="flex flex-wrap items-center gap-2">
        <li v-for="b in BAC" :key="b.ten" class="rounded-full bg-elevated px-3 py-1 text-sm font-semibold text-highlighted">
          {{ b.ten }} <span class="text-xs text-muted">từ {{ b.tu }}</span>
        </li>
      </ol>
      <ul class="cham mt-4">
        <li>Mọi người bắt đầu mùa với {{ D.diemBanDau }} điểm (mùa 13 tuần, cùng lịch mùa PvP nhưng điểm <strong>riêng</strong>). Kiểu Elo, tối đa ±{{ D.k }} mỗi trận; đội phòng thủ đổi {{ D.phanTramThu }}% mức đó. Sàn {{ D.diemSan }}.</li>
        <li>Máy chủ đề xuất 3 đối thủ: điểm lệch ≤ {{ D.cuaSoDiem[0] }} (nới tới {{ D.cuaSoDiem[1] }} khi làm mới), sức mạnh đội (tổng chỉ số cấp 50) lệch ≤ {{ D.daiSucManh[0] }}% (nới tới {{ D.daiSucManh[1] }}%).</li>
        <li>Không bao giờ ghép hai tài khoản cùng tài khoản / thiết bị / mạng. Gặp lại cùng đối thủ trong 24 giờ: điểm và thưởng ×{{ D.heSoLap.map(x => `${x}%`).join(' → ') }}; lần thứ tư không ghép nữa.</li>
      </ul>
    </WikiSection>

    <WikiSection id="thuong" title="Phần thưởng">
      <ul class="cham">
        <li>Mỗi trận thắng: +{{ D.luongThang }} lượng khoá (× hệ số gặp lại), tối đa <strong>{{ D.tranLuongTuan }} lượng khoá mỗi tuần</strong>. Thua: không mất gì ngoài điểm. Không có xu.</li>
        <li>Cuối mùa (đủ {{ D.tranToiThieuMua }} trận tấn công), nhận khi đăng nhập mùa sau, giữ một mùa:</li>
      </ul>
      <ul class="mt-3 grid gap-3 sm:grid-cols-3">
        <li v-for="d in D.danhHieu" :key="d.id" class="rounded-2xl glass p-4">
          <strong class="font-display text-highlighted">{{ d.ten }}</strong>
          <p class="mt-1 text-sm text-muted">
            {{ d.dieuKien }}
          </p>
        </li>
      </ul>
      <WikiCallout kieu="vi-sao">
        Đấu trường tách khỏi sức mạnh ngoài thế giới: thắng ở đây không làm bạn mạnh hơn khi đánh quái hay PK — nó cho IV, tính cách và loài Pokémon tốt một sân chơi riêng.
        Xem bảng tuần và mùa ở <NuxtLink to="/dau-truong" class="lien-ket">Đấu trường Bảo Bối</NuxtLink>.
      </WikiCallout>
    </WikiSection>
  </WikiShell>
</template>
