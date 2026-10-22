<script setup lang="ts">
/**
 * Thu phục & Hộp PC (0.9.8). Số liệu: shared/utils/heThong098.ts (đối chiếu sql/htbb_day_du.sql ở tests/he-thong-098.test.ts):
 * data_bat_bong, data_bat_loai, data_bat_chuyen_doi, cấu hình bat.* và luật com.htbb.pokemon.BatPokemon.
 */
import { BAC_LOAI_BAT, BAT, BONG, CHUYEN_DOI, HOANG_DA, phanTram098 } from '~~/shared/utils/heThong098'

const giaBong = (pn: number) => `${(pn / 1000).toLocaleString('vi-VN', { maximumFractionDigits: 3 })} giờ thu nhập`
</script>

<template>
  <WikiShell slug="thu-phuc">
    <WikiSection id="hoang-da" title="Pokémon hoang dã: chỉ chúng mới bắt được" lead="Quái thường trên bản đồ (quái cày, mục tiêu nhiệm vụ) đã thuần hoá, không bắt được. Thỉnh thoảng một con hồi sinh thành Pokémon HOANG DÃ của vùng — chỉ những con này ném bóng được.">
      <ul class="cham">
        <li>Nhận ra: dấu <strong>Poké Ball</strong> cạnh tên và <strong>vòng màu bậc</strong> dưới chân (Phổ biến / Không phổ biến lấp lánh nhẹ, Hiếm trở lên vòng đậm); khung mục tiêu ghi bậc và “Bắt được”.</li>
        <li>Tần suất: Phổ biến / Không phổ biến khoảng {{ HOANG_DA.t12PhanTram }}% lần hồi sinh; Hiếm 1/{{ HOANG_DA.gapT3 }}, Rất hiếm 1/{{ HOANG_DA.gapT4 }}, Cực hiếm 1/{{ HOANG_DA.gapT5 }} (đúng giờ / thời tiết). Giờ ít người (0–5 giờ) loài hiếm gặp nhiều hơn.</li>
        <li>Mỗi bản đồ (mỗi khu) tối đa {{ HOANG_DA.toiDaMap }} Pokémon hoang dã cùng lúc; con nào không ai giao chiến sau {{ HOANG_DA.phutBienMat }} phút sẽ biến mất.</li>
        <li>Loài Hiếm trở lên được báo cho cả bản đồ; Cực hiếm báo toàn máy chủ.</li>
        <li>Ném vào quái thường: “Pokémon này đã thuần hoá, không thể bắt”.</li>
      </ul>
    </WikiSection>

    <WikiSection id="nem-bong" title="Ném bóng" :lead="`Đánh Pokémon hoang dã (dấu Poké Ball) xuống còn ≤ ${BAT.nguongHpPhanTram}% HP thì nút bóng tròn hiện cạnh cụm chiêu (PC: phím B) — hình trên nút là bóng sẽ dùng theo “Thứ tự dùng bóng” (mặc định Ultra → Great → Poké), nhãn dưới ghi tên và số còn, ví dụ “Ultra Ball ×3”; hết bóng thì nút xám. Bắt được: loài vào Pokédex “Đã bắt”, Hộp PC có thêm một con và bạn nhận Kẹo loài.`">
      <div class="bang-cuon">
        <table class="bang-wiki">
          <thead>
            <tr>
              <th>Bóng</th><th class="so">
                Hệ số
              </th><th class="so">
                Trần tỉ lệ
              </th><th class="so">
                Từ cấp
              </th><th>Giá</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="b in BONG" :key="b.ma">
              <td class="font-semibold text-highlighted">
                {{ b.ten }}
              </td>
              <td class="so">
                ×{{ (b.heSo / 100).toLocaleString('vi-VN') }}
              </td>
              <td class="so">
                {{ phanTram098(b.tran) }}%
              </td>
              <td class="so">
                {{ b.capMo }}
              </td>
              <td class="text-sm">
                {{ giaBong(b.giaPhanNghinGio) }} (xu, theo cấp của bạn)
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <ul class="cham mt-4">
        <li>Mỗi ngày bắt được tối đa <strong>{{ BAT.tranNgay }} Pokémon</strong> và ném tối đa <strong>{{ BAT.tranNemNgay }} lần</strong>; hai lần ném cách nhau ít nhất {{ (BAT.cachMs / 1000).toLocaleString('vi-VN') }} giây.</li>
        <li>Trượt thì lần sau trên cùng con dễ hơn; trượt tới lần thứ {{ BAT.soLanBoChay }} thì con đó bỏ chạy.</li>
        <li>Boss, boss thế giới, Pokémon của Trưởng Gym và Pokémon huyền thoại không thu phục được; một số loài chỉ bắt được trong sự kiện.</li>
      </ul>
    </WikiSection>

    <WikiSection id="ti-le" title="Tỉ lệ thu phục" lead="Tỉ lệ = tỉ lệ gốc của loài × độ yếu (HP càng thấp càng dễ) × chênh cấp × hệ số bóng, kẹp trong trần của bóng.">
      <ul class="cham">
        <li v-for="h in BAT.heSoCap" :key="h.chenh">
          Quái cao hơn bạn <strong>{{ h.chenh }} cấp</strong>: ×{{ h.pt }}%.
        </li>
        <li>Quái cao hơn bạn quá 10 cấp: không ném được.</li>
        <li>Quái tinh anh (Alpha): ×{{ BAT.alphaPhanTram }}%.</li>
      </ul>
      <WikiCallout kieu="vi-sao">
        Bóng bán bằng xu (theo giờ thu nhập ở cấp của bạn) là một hố hút xu nhỏ; Kẹo loài chỉ dùng cho Pokémon trong Hộp PC,
        không cộng chỉ số cho Pokémon bạn đang điều khiển — thu phục là sưu tầm, không phải đường mua sức mạnh.
      </WikiCallout>
    </WikiSection>

    <WikiSection id="bac-hiem" title="Bậc hiếm loài" lead="Mỗi loài thuộc một bậc; bậc càng hiếm tỉ lệ gốc càng thấp (con số phổ biến nhất của bậc, vài loài khác một chút).">
      <ul class="grid gap-3 sm:grid-cols-4">
        <li v-for="b in BAC_LOAI_BAT" :key="b.bac" class="the-so">
          <b>{{ phanTram098(b.tiLeGoc) }}%</b><span>{{ b.ten }}</span>
        </li>
      </ul>
    </WikiSection>

    <WikiSection id="hop-pc" title="Hộp PC & tiến hoá" lead="Mở Hộp PC từ Pokédex hoặc ở Y tá Chansey (Poké Ball & Hộp PC).">
      <ul class="cham">
        <li>Chọn một Pokémon trong Hộp PC làm <strong>bạn đồng hành</strong> đi theo bạn trên bản đồ (chỉ là hình, không cộng chỉ số).</li>
        <li>Dùng Kẹo loài để tiến hoá con trong Hộp PC; loài có nhiều nhánh (Eevee, Gloom, Poliwhirl…) cho chọn nhánh.</li>
        <li>Vài loài chỉ tiến hoá khi được <NuxtLink to="/cam-nang/trao-doi-cho" class="lien-ket">trao đổi</NuxtLink> như Pokémon thật.</li>
      </ul>
    </WikiSection>

    <WikiSection id="chuyen-doi" title="Chuyển đổi con trùng" :lead="`Hộp PC giữ tối đa ${BAT.giuMoiLoai} con thường mỗi loài; bắt thêm thì con kém nhất (IV thấp) tự chuyển đổi.`">
      <div class="bang-cuon">
        <table class="bang-wiki">
          <thead>
            <tr>
              <th>Bậc</th><th class="so">
                Kẹo loài
              </th><th>Thêm</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="c in CHUYEN_DOI" :key="c.bac">
              <td class="font-semibold text-highlighted">
                {{ BAC_LOAI_BAT[c.bac]?.ten }}
              </td>
              <td class="so">
                {{ c.keo }}
              </td>
              <td class="text-sm">
                {{ c.luongKhoa ? `${c.luongKhoa} lượng khoá` : `xu = ${phanTram098(c.xuPhanNghinGio)}% giờ thu nhập` }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <p class="mt-3 text-sm text-muted">
        Trần mỗi ngày: xu từ chuyển đổi tối đa {{ phanTram098(BAT.tranXuChuyenDoiPhanNghinGio) }}% giờ thu nhập, lượng khoá tối đa {{ BAT.tranLuongChuyenDoiNgay }}.
      </p>
    </WikiSection>
  </WikiShell>
</template>
