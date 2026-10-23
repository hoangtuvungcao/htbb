<script setup lang="ts">
/**
 * Câu cá — luật "chỉ câu ở hồ" (HeSinhThaiMenu / BanDoSinhThai.nhanHo), bảng câu từ
 * KyNangSong.BANG_CAU_CA, điểm câu theo bản đồ từ du_lieu/diem_cau_ca.csv.
 */
const H = CAM_NANG_ND.heSinhThai
const C = H.cauCa
const VUNG = ['Kanto', 'Johto', 'Hoenn', '']
const theoVung = VUNG.map(v => ({ vung: v || 'Khu khác', ds: C.ban.filter(b => b.vung === v).sort((a, b) => b.hoLon - a.hoLon || a.map - b.map) })).filter(g => g.ds.length)
const bangSapXep = [...C.bang].sort((a, b) => a.capKyNang - b.capKyNang)
const tongTrongSo = C.bang.reduce((s, x) => s + x.trongSo, 0)
const NUOC: Record<string, string> = { 'ngọt': 'Nước ngọt', 'mặn': 'Nước mặn', 'cả hai': 'Cả hai' }
const soMapHoLon = C.ban.filter(b => b.hoLon > 0).length
</script>

<template>
  <WikiShell slug="cau-ca">
    <WikiCallout kieu="sap-ra-mat" title="Thuộc hệ sinh thái — sắp mở">
      Câu cá là một kỹ năng sống của <NuxtLink to="/cam-nang/he-sinh-thai" class="lien-ket">hệ sinh thái</NuxtLink>, mở theo thông báo trong game.
    </WikiCallout>

    <WikiSection id="luat" title="Chỉ câu được ở hồ">
      <p class="van">
        Bạn chỉ câu được khi đứng cách mép <strong>hồ</strong> tối đa 2 ô. Đứng ở chỗ khác, game trả lời
        <q class="font-semibold text-highlighted">Chỉ có thể câu cá ở hồ.</q> và chỉ cho bạn toạ độ <em>điểm câu gần nhất</em> nếu bản đồ đó có hồ.
      </p>
      <ul class="cham mt-4">
        <li><strong>Hồ</strong> là một vùng nước liền nhau nằm hẳn bên trong bản đồ (không chạm mép bản đồ) và đủ rộng.</li>
        <li><strong>Không phải hồ:</strong> biển, sông chảy ra mép bản đồ, bờ biển và vũng nước nhỏ.</li>
        <li><strong>Hồ lớn</strong> có đủ bảng cá, kể cả cá hiếm. Hồ nhỏ (ao) chỉ có cá thường.</li>
        <li>Hồ trên bản đồ biển hoặc tàu có cả cá nước mặn; ở nơi khác chỉ có cá nước ngọt.</li>
      </ul>
      <div class="mt-5 grid gap-3 sm:grid-cols-3">
        <div class="the-so">
          <b>{{ C.tongDiem }}</b><span>điểm câu trên {{ C.ban.length }} bản đồ</span>
        </div>
        <div class="the-so">
          <b>{{ soMapHoLon }}</b><span>bản đồ có hồ lớn (câu được cá hiếm)</span>
        </div>
        <div class="the-so">
          <b>{{ H.tlCauCa }}</b><span>thể lực mỗi lần câu (kèm một ít xu tiền mồi)</span>
        </div>
      </div>
    </WikiSection>

    <WikiSection id="cach-cau" title="Cách câu">
      <ol class="cham">
        <li>Đi tới cạnh một hồ (xem danh sách bên dưới).</li>
        <li>Gõ <code class="font-bold text-highlighted">cauca</code> trong ô chat, hoặc mở menu Hệ sinh thái → Câu cá.</li>
        <li>Mỗi lần câu cho một kết quả; cấp kỹ năng càng cao càng mở thêm loài và có lúc câu được 2 con.</li>
        <li>Dùng <strong>mồi thơm</strong> (nấu từ rong biển + vảy cá) để tăng tỉ lệ ra đồ giá trị.</li>
      </ol>
      <p class="van mt-3">
        Câu được cá thì loài đó được ghi "đã gặp" trong Pokédex; cá hiếm vào album, cá rất hiếm còn được báo cho cả máy chủ.
      </p>
    </WikiSection>

    <WikiSection id="bang-cau" title="Bảng câu được" :lead="`${C.bang.length} kết quả. Tỉ lệ là tỉ trọng gốc trước khi lọc theo cấp kỹ năng, loại nước và cỡ hồ.`">
      <div class="bang-cuon">
        <table class="bang-wiki">
          <thead>
            <tr>
              <th>Kết quả</th><th class="so">
                Tỉ trọng
              </th><th class="so">
                Cấp kỹ năng
              </th><th>Nước</th><th>Cần hồ lớn</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="x in bangSapXep" :key="x.ten">
              <td class="font-semibold text-highlighted">
                {{ x.ten }}
              </td>
              <td class="so">
                {{ soVn(x.trongSo / tongTrongSo * 100, 1) }}%
              </td>
              <td class="so">
                {{ x.capKyNang }}
              </td>
              <td>{{ NUOC[x.nuoc] ?? x.nuoc }}</td>
              <td>
                <UBadge v-if="x.hoLon" color="warning" variant="subtle" size="sm">
                  Hồ lớn
                </UBadge>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </WikiSection>

    <WikiSection id="ban-do" title="Bản đồ có hồ">
      <div v-for="g in theoVung" :key="g.vung" class="mt-6 first:mt-0">
        <h3 class="font-display text-xl font-bold text-highlighted">
          {{ g.vung }}
        </h3>
        <ul class="mt-2 grid gap-2 sm:grid-cols-2 lg:grid-cols-3">
          <li v-for="b in g.ds" :key="b.map" class="flex items-center justify-between gap-3 rounded-2xl glass px-4 py-3">
            <span class="min-w-0">
              <span class="line-clamp-2 block font-semibold text-highlighted" :title="b.ten">{{ b.ten }}</span>
              <span class="block text-xs text-muted">{{ b.soHo }} hồ · {{ b.diem }} điểm câu</span>
            </span>
            <UBadge v-if="b.hoLon" color="warning" variant="subtle" size="sm" class="shrink-0">
              {{ b.hoLon }} hồ lớn
            </UBadge>
            <UBadge v-else color="neutral" variant="soft" size="sm" class="shrink-0">
              Ao
            </UBadge>
          </li>
        </ul>
      </div>
      <p class="mt-4 text-sm text-muted">
        Vùng Hoenn mở khi xong cốt truyện năm 1 (chương 52) và đạt cấp {{ CAP_VAO_HOENN }} trở lên.
      </p>
    </WikiSection>
  </WikiShell>
</template>
