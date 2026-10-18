<script setup lang="ts">
/**
 * Vật phẩm — nhóm theo docs/kien-truc.md §6.4; đá Eevee + mốc Pokédex từ pokedex.json;
 * bảng chi tiết (công dụng, cấp, giá, nguồn, giới hạn) từ du_lieu/web/vat_pham.csv (cam-nang-noi-dung.json).
 */
const T = CAM_NANG.trangBi
const NHOM = [
  { icon: 'i-lucide-heart-pulse', ten: 'Thuốc hồi máu & Ether', mo: 'Hồi HP (phím 7) và PP chiêu (phím 9). Mua ở Chansey (Trung tâm Pokémon); loại mạnh hơn mở theo cấp.', to: '#bang' },
  { icon: 'i-lucide-cherry', ten: 'Berry & thuốc đặc biệt', mo: 'Vật phẩm tiêu hao tăng tạm thời EXP, tấn công, phòng thủ; nhiều loại là quà sự kiện (khoá).' },
  { icon: 'i-lucide-gem', ten: 'Đá tiến hoá', mo: `${POKEDEX.daTienHoa.length} loại (${POKEDEX.daTienHoa.map(d => d.ten).join(', ')}) — dùng cho Eevee từ cấp ${POKEDEX.capNhanhEevee}.`, to: '/cam-nang/tien-hoa#kiem-da' },
  { icon: 'i-lucide-hammer', ten: 'Đá nâng cấp', mo: `Nguyên liệu nâng cấp trang bị: rơi ${(T.daRoiPhanNghin / 10).toLocaleString('vi-VN')}% từ quái gần cấp, hoặc phân rã đồ trùng.`, to: '/cam-nang/trang-bi#nang-cap' },
  { icon: 'i-lucide-shield-check', ten: 'Bùa bảo hộ', mo: `Giữ cấp nâng khi trượt từ +${T.matCapTu}. Mua bằng lượng khoá.`, to: '/cam-nang/trang-bi#nang-cap' },
  { icon: 'i-lucide-diamond', ten: 'Ngọc hệ', mo: `Khảm vào trang bị, cấp 1–${T.ngocHe.capToiDa}.`, to: '/cam-nang/trang-bi#ngoc-he' },
  { icon: 'i-lucide-egg', ten: 'Trứng Pokémon', mo: 'Ấp ra pet — Pokémon nhỏ đi theo bạn.', to: '/cam-nang/thu-cuoi-pet#pet' },
  { icon: 'i-lucide-rabbit', ten: 'Thú cưỡi & đồ thú cưỡi', mo: 'Thú cưỡi (Ponyta, Rapidash, Lapras…), yên và giáp cộng chỉ số cho thú.', to: '/cam-nang/thu-cuoi-pet' },
  { icon: 'i-lucide-puzzle', ten: 'Mảnh & nguyên liệu', mo: 'Gom đủ để ghép, chế tạo ở Machamp; nguyên liệu đột kích là đồ khoá.' },
  { icon: 'i-lucide-ticket', ten: 'Vé chương', mo: 'Mở một chương chính tuyến. Nhận tự động mỗi tuần.', to: '/cam-nang/nhiem-vu#chinh-tuyen' },
  { icon: 'i-lucide-sparkles', ten: 'Đồ trang trí', mo: 'Trang phục, khung avatar, danh hiệu, hiệu ứng — chỉ để đẹp, không chỉ số.' },
  { icon: 'i-lucide-package', ten: 'Vật phẩm nhiệm vụ', mo: 'Thư, nguyên liệu thu thập cho nhiệm vụ; tự mất khi trả nhiệm vụ.' },
]
const tongTrangBi = T.o.reduce((s, o) => s + o.soMon, 0)
const VP = CAM_NANG_ND.vatPham
const nhomVp = [...new Set(VP.map(v => v.nhom))].map(nhom => ({ nhom, ds: VP.filter(v => v.nhom === nhom) }))
const coSapCo = VP.some(v => v.sapCo)
</script>

<template>
  <WikiShell slug="vat-pham">
    <p class="van">
      Hành trang có số ô giới hạn — gửi bớt đồ ở Kangaskhan (kho) và bán đồ thừa. Trang bị ({{ tongTrangBi }} món, {{ T.bac.length }} bậc) có trang riêng:
      <NuxtLink to="/cam-nang/trang-bi" class="lien-ket">Trang bị & nâng cấp</NuxtLink>.
    </p>
    <WikiSection id="nhom" title="Các nhóm vật phẩm">
      <ul class="grid gap-3 sm:grid-cols-2">
        <li v-for="n in NHOM" :key="n.ten" class="flex gap-3 rounded-2xl glass p-4">
          <span class="grid size-10 shrink-0 place-items-center rounded-xl bg-pikachu-400 text-navy-950"><UIcon :name="n.icon" class="size-5" /></span>
          <span class="min-w-0">
            <strong class="font-display text-highlighted">{{ n.ten }}</strong>
            <span class="block text-sm text-muted">{{ n.mo }}</span>
            <NuxtLink v-if="n.to" :to="n.to" class="mt-1 inline-flex items-center gap-1 text-xs font-bold text-primary hover:underline">Chi tiết <UIcon name="i-lucide-arrow-right" class="size-3" /></NuxtLink>
          </span>
        </li>
      </ul>
    </WikiSection>
    <WikiSection id="bang" title="Bảng vật phẩm" :lead="`${VP.length} vật phẩm tiêu hao và nguyên liệu: công dụng, cấp dùng, giá và nơi kiếm.`">
      <p v-if="coSapCo" class="mb-4 text-sm text-muted">
        <UBadge color="info" variant="subtle" size="sm">
          Sắp có
        </UBadge> = đã có trong thiết kế, sẽ xuất hiện trong game ở các bản cập nhật tới.
      </p>
      <div v-for="g in nhomVp" :key="g.nhom" class="mt-6 first:mt-0">
        <h3 class="font-display text-xl font-bold text-highlighted">
          {{ g.nhom }}
        </h3>
        <div class="bang-cuon mt-2">
          <table class="bang-wiki">
            <thead>
              <tr>
                <th>Vật phẩm</th><th>Công dụng</th><th class="so">
                  Cấp
                </th><th class="so">
                  Giá
                </th><th>Nguồn</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="v in g.ds" :key="v.ten">
                <td class="font-semibold text-highlighted">
                  {{ v.ten }}
                  <UBadge v-if="v.sapCo" color="info" variant="subtle" size="sm" class="ml-1">
                    Sắp có
                  </UBadge>
                </td>
                <td>{{ v.congDung }}{{ v.gioiHan ? ' ' : '' }}<span v-if="v.gioiHan" class="text-muted">(tối đa {{ v.gioiHan }})</span></td>
                <td class="so whitespace-nowrap">
                  {{ v.cap || '—' }}
                </td>
                <td class="so whitespace-nowrap">
                  {{ giaHienThi(v.gia, v.tien) }}
                </td>
                <td>{{ v.nguon }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <p class="mt-3 text-sm">
        Giá theo từng cửa hàng: <NuxtLink to="/cam-nang/cua-hang" class="lien-ket">Cửa hàng & chế tạo</NuxtLink>.
      </p>
    </WikiSection>

    <WikiSection id="khoa" title="Vật phẩm khoá & hết hạn">
      <ul class="cham">
        <li><strong>Khoá</strong>: không bán, không trao đổi được (biểu tượng ổ khoá). Phần lớn quà sự kiện, nhiệm vụ là đồ khoá để giữ giá chợ ổn định.</li>
        <li><strong>Hết hạn</strong>: một số vật phẩm sự kiện tự biến mất sau thời gian ghi trên mô tả — dùng trước khi hết hạn.</li>
        <li>Đồ <strong>giao dịch được</strong> bán qua chợ với thuế {{ CAM_NANG.kinhTe.thueCho }}% — xem <NuxtLink to="/cam-nang/kinh-te#cho" class="lien-ket">Chợ & giao dịch</NuxtLink>.</li>
      </ul>
    </WikiSection>
  </WikiShell>
</template>
