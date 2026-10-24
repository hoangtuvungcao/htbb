<script setup lang="ts">
const { camNang: CAM_NANG } = await useCamNang() // số liệu CSDL game, dự phòng JSON
/** Đội, vùng & sư phạm — nhiệm vụ đội/sư phạm từ cam-nang.json (nhiem_vu_moi.csv). */
const N = CAM_NANG.nhiemVu
const chienVung = CAM_NANG.suKien.filter(s => s.loai === 'chien_vung')
const SP = CAM_NANG_ND.soTay.suPham
</script>

<template>
  <WikiShell slug="doi-su-pham">
    <WikiSection id="to-doi" title="Tổ đội">
      <ul class="cham">
        <li>Bấm <strong>phím *</strong> lên người chơi khác → <em>Mời nhóm</em>. Tổ đội tối đa 5 Pokémon.</li>
        <li>Cùng đánh quái thì cả nhóm đều nhận EXP; người ra đòn cuối nhận phần lớn nhất.</li>
        <li>Tổ đội mạnh hơn hẳn khi phối hợp hệ: một Pokémon chịu đòn (Bulbasaur, Squirtle), một gây sát thương (Charmander, Pichu), một hỗ trợ (Eevee).</li>
        <li>Boss thế giới và đột kích huyền thoại dễ hơn nhiều khi đi cả đội — xem <NuxtLink to="/cam-nang/boss-dot-kich" class="lien-ket">Boss & đột kích</NuxtLink>.</li>
      </ul>
    </WikiSection>

    <WikiSection id="doi" title="Đội" lead="Đội là hội nhóm lâu dài của người chơi. Đội trưởng và đội phó quản lý thành viên; đội có quỹ chung, thú cưỡi đội và lãnh địa riêng.">
      <div class="bang-cuon">
        <table class="bang-wiki">
          <caption>Nhiệm vụ đội hằng tuần — cả đội cùng tiến độ</caption>
          <thead>
            <tr>
              <th>Nhiệm vụ</th><th class="so">
                Cấp
              </th><th>Mục tiêu</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(q, i) in N.doi" :key="i">
              <td class="font-semibold text-highlighted">
                {{ q.ten }}
              </td><td class="so">
                {{ q.cap }}
              </td><td>{{ q.yeuCau || `${q.soLuong ? `${soVn(q.soLuong)} ` : ''}${q.mucTieu}` }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <p class="van mt-4">
        Đội cũng xếp hạng theo tuần ("Đội mạnh nhất tuần" là một thành tựu). Thuế chợ của thành viên có thể chảy về quỹ đội.
      </p>
    </WikiSection>

    <WikiSection id="vung" title="Vùng" :lead="`Mỗi người chơi thuộc một trong ba vùng: ${POKEDEX.vung.join(', ')}. Bản đồ có bản riêng cho từng vùng và các vùng tranh tài trong sự kiện liên vùng.`">
      <ul class="grid gap-3 sm:grid-cols-3">
        <li v-for="v in POKEDEX.vung" :key="v" class="rounded-2xl glass p-4 text-center">
          <span class="route-sign">{{ v }}</span>
        </li>
      </ul>
      <WikiCallout kieu="meo" title="Thế chiến vùng">
        {{ chienVung[0]?.coChe ?? 'Ba vùng giành cờ' }} — diễn ra {{ chienVung.length }} lần mỗi năm
        (tuần {{ chienVung.map(s => s.tuan).join(', ') }}). Phần thưởng là cờ vùng trang trí và danh hiệu.
      </WikiCallout>
    </WikiSection>

    <WikiSection id="su-pham" title="Sư phạm (thầy – trò)" :lead="`Người chơi cấp cao kèm người mới: thầy từ cấp ${SP.capThay}, trò thấp hơn ít nhất ${SP.cachCap} cấp; trò được +${SP.expPhanTram}% EXP khi chung nhóm với thầy.`">
      <div class="bang-cuon">
        <table class="bang-wiki">
          <thead>
            <tr>
              <th>Nhiệm vụ</th><th class="so">
                Cấp thầy tối thiểu
              </th><th>Điều kiện</th><th>Lặp lại</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(q, i) in N.suPham" :key="i">
              <td class="font-semibold text-highlighted">
                {{ q.ten }}
              </td><td class="so">
                {{ q.cap }}
              </td><td>{{ q.yeuCau || q.mucTieu }}</td><td>{{ q.lapLai === 'tuan' ? 'Mỗi tuần' : q.lapLai === 'ngay' ? 'Mỗi ngày' : 'Một lần' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <p class="van mt-4">
        Nhận trò ở Quầy đổi thưởng → <NuxtLink to="/cam-nang/so-tay#su-pham" class="lien-ket">Sổ tay huấn luyện</NuxtLink> → Sư phạm (nhận trò). Khi trò đạt cấp {{ SP.moc.join(' và ') }}, cả hai cùng nhận thưởng.
      </p>
    </WikiSection>
  </WikiShell>
</template>
