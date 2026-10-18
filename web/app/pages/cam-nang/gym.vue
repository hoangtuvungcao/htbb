<script setup lang="ts">
/**
 * Gym & huy hiệu — 16 Gym từ du_lieu/web/gym.csv + noi_dung_gym.csv, luật trận từ
 * GymLauDai.java / GiaiDoanBoss.java (qua cam-nang-noi-dung.json).
 */
const G = CAM_NANG_ND.gym
const L = CAM_NANG_ND.gymLuat
const heTheoTen = new Map(POKEDEX.he.map(h => [h.ten.toLocaleLowerCase('vi'), h.id]))
const idHe = (ten: string) => heTheoTen.get(ten.toLocaleLowerCase('vi'))
const loaiTheoTen = new Map(POKEDEX.loai.map(l => [l.ten.toLocaleLowerCase('vi'), l]))
const truong = (ten: string) => loaiTheoTen.get(ten.toLocaleLowerCase('vi'))
const kanto = G.filter(g => g.so <= 8)
const johto = G.filter(g => g.so > 8)
</script>

<template>
  <WikiShell slug="gym">
    <WikiSection id="thach-dau" title="Thách đấu Gym" :lead="`${G.length} Gym trải dài Kanto và Johto. Hạ Trưởng Gym lần đầu nhận huy hiệu và một đồ cầm tay Hoàn mỹ đúng dòng của bạn.`">
      <ol class="cham">
        <li>Mở <NuxtLink to="/cam-nang/so-tay" class="lien-ket">Sổ tay huấn luyện</NuxtLink> → <strong>Thách đấu Gym</strong> → chọn Gym.</li>
        <li>Chỉ cần đủ cấp của Gym — không bắt buộc đánh theo thứ tự.</li>
        <li>Trưởng Gym xuất hiện ngay cạnh bạn, trên bản đồ bạn đang đứng. Mỗi lúc chỉ thách đấu được một Gym.</li>
      </ol>
      <div class="mt-5 grid gap-3 sm:grid-cols-3">
        <div class="the-so">
          <b>1 đấu 1</b><span>người khác đánh vào Trưởng Gym không gây sát thương</span>
        </div>
        <div class="the-so">
          <b>{{ L.phutGioiHan }} phút</b><span>hết giờ chưa hạ được thì Trưởng Gym rời đi (tính là thua)</span>
        </div>
        <div class="the-so">
          <b>{{ L.phutNoiGian }} phút</b><span>sau thời gian này Trưởng Gym nổi giận, đánh nhanh và đau hơn</span>
        </div>
      </div>
      <p class="van mt-4">
        Thua không bị phạt thời gian chờ — hồi máu, đổi chiêu rồi thử lại.
      </p>
    </WikiSection>

    <WikiSection v-for="nhom in [{ id: 'kanto', ten: 'Gym Kanto', ds: kanto }, { id: 'johto', ten: 'Gym Johto', ds: johto }]" :id="nhom.id" :key="nhom.id" :title="nhom.ten">
      <div class="bang-cuon">
        <table class="bang-wiki">
          <thead>
            <tr>
              <th class="so">
                #
              </th><th>Huy hiệu</th><th>Hệ</th><th class="so">
                Cấp
              </th><th>Trưởng Gym</th><th class="so">
                HP
              </th><th>Đồ cầm tay thưởng</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="g in nhom.ds" :key="g.so">
              <td class="so">
                {{ g.so }}
              </td>
              <td class="font-semibold text-highlighted">
                {{ g.huyHieu }}
              </td>
              <td>
                <PokeHeBadge v-if="idHe(g.he) != null" :he="idHe(g.he)!" size="sm" />
                <span v-else>{{ g.he }}</span>
              </td>
              <td class="so">
                {{ g.cap }}
              </td>
              <td>
                <NuxtLink v-if="truong(g.truong)" :to="`/pokedex/${truong(g.truong)!.dex}`" class="flex items-center gap-2 font-semibold hover:text-primary">
                  <PokeAvatar :dex="truong(g.truong)!.dex" class="size-6" /> {{ g.truong }}
                </NuxtLink>
                <span v-else>{{ g.truong }}</span>
              </td>
              <td class="so whitespace-nowrap">
                {{ g.hp ? soVn(g.hp) : '—' }}
              </td>
              <td>
                Hoàn mỹ {{ g.bacThuong }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </WikiSection>

    <WikiSection id="danh-lai" title="Đánh lại hằng tuần">
      <ul class="cham">
        <li>Mỗi Gym đã có huy hiệu đánh lại được <strong>1 lần mỗi tuần</strong> (tuần mới bắt đầu thứ Hai 05:00), thưởng <strong>{{ L.huyChuongTuan }} Huy chương Gym</strong>.</li>
        <li v-if="L.aoChoang">
          Đổi {{ L.aoChoang.gia }} Huy chương Gym lấy <strong>{{ L.aoChoang.ten }}</strong> — xem <NuxtLink to="/cam-nang/cua-hang" class="lien-ket">Cửa hàng</NuxtLink>.
        </li>
        <li>Đồ cầm tay Hoàn mỹ từ Gym là đồ <strong>khoá</strong> (không bán, không trao đổi), đúng dòng: Than lửa (Charmander), Hạt giống thần (Bulbasaur), Khăn lụa (Eevee), Nước thần (Squirtle), Nam châm (Pichu).</li>
      </ul>
      <WikiCallout kieu="meo">
        Xem hệ của Trưởng Gym rồi chuẩn bị chiêu <NuxtLink to="/cam-nang/he-khac-che" class="lien-ket">khắc hệ</NuxtLink>; mang theo thuốc hồi máu và Ether vì trận 1 đấu 1 không ai hồi giúp bạn.
      </WikiCallout>
    </WikiSection>
  </WikiShell>
</template>
