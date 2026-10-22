<script setup lang="ts">
/**
 * Trao đổi & chợ Pokémon (0.9.8). Số liệu: shared/utils/heThong098.ts (trao_doi.*, cho_pokemon.*, khung giá
 * com.htbb.pokemon.ChoPokemon) — đối chiếu sql/htbb_day_du.sql ở tests/he-thong-098.test.ts.
 */
import { BAC_LOAI, CHO_POKEMON, TRAO_DOI, phanTram098 } from '../../../shared/utils/heThong098'

const gio = (pn: number) => `${(pn / 1000).toLocaleString('vi-VN', { maximumFractionDigits: 2 })} giờ`
</script>

<template>
  <WikiShell slug="trao-doi-cho">
    <WikiSection id="trao-doi" title="Trao đổi Pokémon" :lead="`Hai người chơi từ cấp ${TRAO_DOI.cap} đổi Pokémon trong Hộp PC: mỗi bên chọn con, khoá, rồi xác nhận lần hai. Đổi một lần cho cả hai bên — rớt mạng giữa chừng thì không ai mất gì.`">
      <ul class="cham">
        <li>Người nhận trả phí mỗi con: <strong>{{ phanTram098(TRAO_DOI.phiPhanNghinGio) }}% giờ thu nhập</strong> ở cấp của mình × bậc hiếm (×1 / ×2 / ×4 / ×8 cho {{ BAC_LOAI.map(b => b.ten).join(' / ') }}), sắc màu ×4, 3 sao ×2; một con tối đa {{ gio(TRAO_DOI.phiTranPhanNghinGio) }} thu nhập. Phí bị đốt (hố hút xu).</li>
        <li>Nhận tối đa <strong>{{ TRAO_DOI.tranNgay }} Pokémon / ngày</strong> qua trao đổi, mỗi lần đổi cách nhau {{ TRAO_DOI.cachGiay }} giây.</li>
        <li>Con vừa nhận phải chờ <strong>{{ TRAO_DOI.khoaGio }} giờ</strong> mới đổi tiếp được (chống chuyền tay).</li>
        <li>Vài loài tiến hoá khi được trao đổi, như Pokémon thật — xem <NuxtLink to="/cam-nang/thu-phuc#hop-pc" class="lien-ket">Hộp PC & tiến hoá</NuxtLink>.</li>
      </ul>
    </WikiSection>

    <WikiSection id="cho" title="Chợ Pokémon" :lead="`Rao bán Pokémon trong Hộp PC lấy xu, từ cấp ${CHO_POKEMON.cap} (cả người bán lẫn người mua).`">
      <div class="grid gap-3 sm:grid-cols-3">
        <div class="the-so">
          <b>{{ CHO_POKEMON.thuePhanTram }}%</b><span>thuế khi bán được (đốt)</span>
        </div>
        <div class="the-so">
          <b>{{ CHO_POKEMON.phiDangPhanTram }}%</b><span>phí đăng, không hoàn kể cả khi huỷ / hết hạn</span>
        </div>
        <div class="the-so">
          <b>{{ CHO_POKEMON.hanGio }} giờ</b><span>hết hạn thì Pokémon về lại Hộp PC</span>
        </div>
      </div>
      <ul class="cham mt-4">
        <li>Đang rao cùng lúc tối đa {{ CHO_POKEMON.toiDaDang }} con; mỗi người mua tối đa {{ CHO_POKEMON.muaNgay }} con / ngày.</li>
        <li>Con đang rao nằm ở ký quỹ (không còn trong Hộp PC); hai người cùng bấm mua thì chỉ một người được.</li>
        <li>Pokémon nhận qua trao đổi / chợ phải chờ {{ CHO_POKEMON.khoaGio }} giờ mới rao bán được.</li>
      </ul>
    </WikiSection>

    <WikiSection id="khung-gia" title="Khung giá" lead="Máy chủ tính sàn và trần giá theo bậc hiếm, bằng giờ thu nhập ở CẤP CỦA CON POKÉMON (không theo cấp người bán). Ngoài khung thì không rao được.">
      <div class="bang-cuon">
        <table class="bang-wiki">
          <thead>
            <tr>
              <th>Bậc</th><th class="so">
                Sàn
              </th><th class="so">
                Trần
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(b, i) in BAC_LOAI" :key="b.bac">
              <td class="font-semibold text-highlighted">
                {{ b.ten }}
              </td>
              <td class="so">
                {{ gio(CHO_POKEMON.san[i]!) }}
              </td>
              <td class="so">
                {{ gio(CHO_POKEMON.tran[i]!) }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <p class="mt-3 text-sm text-muted">
        Sắc màu nhân cả sàn lẫn trần ×{{ CHO_POKEMON.heSoSacMau }}, cá thể 3 sao ×{{ CHO_POKEMON.heSo3Sao }}.
      </p>
    </WikiSection>

    <WikiSection id="chong-clone" title="Vì sao có giới hạn">
      <WikiCallout kieu="vi-sao">
        Mọi đường chuyển giá trị giữa hai người chơi (trao đổi, chợ, ký gửi) đi qua cùng cổng chống tài khoản phụ: tài khoản đủ
        tuổi và đủ giờ chơi, khung giá máy chủ, phí / thuế bị đốt. Nhờ vậy loài hiếm giữ giá và không ai “rửa” xu bằng nick phụ.
      </WikiCallout>
    </WikiSection>
  </WikiShell>
</template>
