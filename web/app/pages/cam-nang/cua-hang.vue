<script setup lang="ts">
const { noiDung: CAM_NANG_ND } = await useCamNang() // số liệu CSDL game, dự phòng JSON
/** Cửa hàng & chế tạo — tools/luu_tru_sinh_du_lieu/du_lieu/web/cua_hang.csv, che_tao.csv (cam-nang-noi-dung.json). */
const CH = CAM_NANG_ND.cuaHang
const MO_TA: Record<string, string> = {
  'Chansey (thuốc)': 'Ở Trung tâm Pokémon mỗi thị trấn. Bán thuốc hồi máu, Ether, thuốc chữa trạng thái, thuốc tăng cường X.',
  'Kecleon (lượng khoá)': 'Quầy lượng khoá: bùa bảo hộ và thuốc mạnh có giới hạn mỗi tuần.',
  'Delcatty (trang trí)': 'Đồ trang trí (khung ảnh, trang phục) — chỉ để đẹp, không cộng chỉ số.',
  'Bronzong (trang bị)': 'Bán trang bị theo bậc. Đồ bán ở đây là bản thường; bản tốt hơn đến từ quái rơi, chế tạo và Gym.',
  'Huy chương Gym': 'Đổi Huy chương Gym (đánh lại Gym hằng tuần) lấy phần thưởng riêng.',
}
const ICON: Record<string, string> = {
  'Chansey (thuốc)': 'i-lucide-heart-pulse',
  'Kecleon (lượng khoá)': 'i-lucide-lock',
  'Delcatty (trang trí)': 'i-lucide-sparkles',
  'Bronzong (trang bị)': 'i-lucide-shield',
  'Huy chương Gym': 'i-lucide-medal',
}
const idCh = (ten: string) => boDau(ten).replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '')
const thuTu = ['Chansey (thuốc)', 'Kecleon (lượng khoá)', 'Huy chương Gym', 'Delcatty (trang trí)', 'Bronzong (trang bị)']
const viTri = (ten: string) => (thuTu.includes(ten) ? thuTu.indexOf(ten) : thuTu.length)
const ds = [...CH].sort((a, b) => viTri(a.ten) - viTri(b.ten))
const CT = CAM_NANG_ND.cheTao
</script>

<template>
  <WikiShell slug="cua-hang">
    <WikiSection id="tien" title="Trả bằng gì?">
      <ul class="cham">
        <li><strong>Xu</strong>: tiền thường, kiếm từ quái và nhiệm vụ. Mua thuốc, trang bị, chế tạo, nâng cấp.</li>
        <li><strong>Lượng khoá</strong>: nhận từ đăng nhập, nhiệm vụ, sự kiện; không trao đổi được. Mua bùa bảo hộ và thuốc mạnh.</li>
        <li><strong>Lượng</strong>: loại tiền hiếm, chỉ dùng cho đồ trang trí và tiện ích (không cộng sức mạnh).</li>
      </ul>
      <p class="mt-3 text-sm">
        Chi tiết từng loại tiền: <NuxtLink to="/cam-nang/kinh-te#tien" class="lien-ket">Kinh tế & tiền tệ</NuxtLink>.
      </p>
    </WikiSection>

    <WikiSection v-for="c in ds" :id="idCh(c.ten)" :key="c.ten" :title="c.ten" :lead="MO_TA[c.ten]">
      <details class="group rounded-2xl glass" :open="c.hang.length <= 40">
        <summary class="flex cursor-pointer items-center gap-2 px-4 py-3 font-semibold text-highlighted">
          <UIcon :name="ICON[c.ten] ?? 'i-lucide-store'" class="size-4 text-primary" aria-hidden="true" />
          {{ c.hang.length }} món
          <UIcon name="i-lucide-chevron-down" class="ml-auto size-4 transition group-open:rotate-180" aria-hidden="true" />
        </summary>
        <div class="bang-cuon px-2 pb-2">
          <table class="bang-wiki">
            <thead>
              <tr>
                <th>Vật phẩm</th><th class="so">
                  Giá
                </th><th class="so">
                  Cấp
                </th><th>Giới hạn</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="h in c.hang" :key="h.vatPham">
                <td class="font-semibold text-highlighted">
                  {{ h.vatPham }}
                </td>
                <td class="so whitespace-nowrap">
                  {{ giaHienThi(h.gia, h.tien) }}
                </td>
                <td class="so">
                  {{ h.cap ?? '—' }}
                </td>
                <td>{{ h.gioiHan || '—' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </details>
    </WikiSection>

    <WikiSection id="che-tao" title="Chế tạo ở Machamp" :lead="`${CT.length} món chế được: vòng cổ và phiến đá hệ theo bậc. Cần đá nâng cấp, nguyên liệu phụ và xu.`">
      <div class="bang-cuon">
        <table class="bang-wiki">
          <thead>
            <tr>
              <th>Thành phẩm</th><th class="so">
                Cấp
              </th><th class="so">
                Đá nâng cấp
              </th><th>Nguyên liệu phụ</th><th class="so">
                Xu
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="t in CT" :key="t.ten">
              <td class="font-semibold text-highlighted">
                {{ t.ten }}
              </td><td class="so">
                {{ t.cap }}
              </td><td class="so">
                {{ t.da }}
              </td><td>{{ t.phu || '—' }}</td><td class="so">
                {{ soVn(t.xu) }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <p v-if="CT[0]" class="mt-3 text-sm text-muted">
        Nơi chế tạo: {{ CT[0].noi }}.
      </p>
    </WikiSection>
  </WikiShell>
</template>
