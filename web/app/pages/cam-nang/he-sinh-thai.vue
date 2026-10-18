<script setup lang="ts">
/**
 * Hệ sinh thái — viết cho người chơi từ HTBB/docs/pokemon/HE_SINH_THAI.md, số liệu đối chiếu với
 * HeSinhThai.java / KyNangSong.java / SuuTamSinhThai.java (cam-nang-noi-dung.json).
 * Cách các kiểu thời tiết ưu ái hệ (SinhThaiTheGioi.java) là mô tả, không phải số nên viết ở đây.
 */
const H = CAM_NANG_ND.heSinhThai
const THOI_TIET = [
  { ten: 'Quang đãng', icon: 'i-lucide-cloud-sun', he: '—', satThuong: 'Không đổi' },
  { ten: 'Nắng gắt', icon: 'i-lucide-sun', he: 'Lửa, Cỏ', satThuong: `Lửa +${H.thoiTietPhanTram}%, Nước −${H.thoiTietPhanTram}%` },
  { ten: 'Mưa', icon: 'i-lucide-cloud-rain', he: 'Nước, Điện', satThuong: `Nước +${H.thoiTietPhanTram}%, Lửa −${H.thoiTietPhanTram}%` },
  { ten: 'Tuyết', icon: 'i-lucide-snowflake', he: 'Băng', satThuong: `Băng +${H.thoiTietPhanTram}%` },
  { ten: 'Bão cát', icon: 'i-lucide-wind', he: 'Đá, Đất, Thép', satThuong: `Đá +${H.thoiTietPhanTram}%` },
  { ten: 'Sương mù', icon: 'i-lucide-cloud-fog', he: 'Ma, Siêu linh', satThuong: `Ma +${H.thoiTietPhanTram}%` },
]
const KY_NANG_MO: Record<string, string> = {
  'Câu cá': 'Câu ở hồ: vảy cá, vỏ sò, ngọc trai, cá hiếm cho album.',
  'Trồng trọt': 'Vườn berry trên bản đồ nhà riêng; bạn bè tưới giúp được thêm sản lượng.',
  'Khảo cổ': 'Đào ở hang, núi, sa mạc, vùng tuyết: mảnh đá tiến hoá, hoá thạch, đá sao rơi.',
  'Nấu ăn': 'Bánh Poké hồi thể lực, mồi thơm cho câu cá, súp berry tăng EXP kỹ năng.',
  'Chế thuốc': `Thuốc hồi HP/MP từ berry, tối đa ${H.meThuocNgay} mẻ mỗi ngày.`,
}
const nhomCongThuc = computed(() => H.kyNang.map(k => ({ kyNang: k, ds: H.congThuc.filter(c => c.kyNang === k).sort((a, b) => a.cap - b.cap) })).filter(g => g.ds.length))
const gioKhung = (g: number) => `${String(g).padStart(2, '0')}:00–${String(g + 1).padStart(2, '0')}:00`
</script>

<template>
  <WikiShell slug="he-sinh-thai">
    <WikiCallout kieu="sap-ra-mat" title="Đang thử nghiệm">
      Hệ sinh thái đang được thử nghiệm và sẽ mở theo thông báo trong game. Số liệu dưới đây lấy từ bản đang thử, có thể còn điều chỉnh.
    </WikiCallout>

    <WikiSection id="the-gioi" title="Thế giới sống: ngày đêm & thời tiết" lead="Mỗi vùng có thời tiết riêng, đổi theo ngày và theo mùa. Thời tiết quyết định Pokémon hoang dã nào hay xuất hiện và chiêu hệ nào mạnh hơn.">
      <ul class="cham">
        <li><strong>Ban đêm</strong> từ {{ H.gioDemTu }}:00 đến {{ String(H.gioDemDen).padStart(2, '0') }}:00 (giờ máy chủ): Pokémon hệ Ma, Bóng tối, Độc xuất hiện nhiều gấp {{ H.heSoSinh }}.</li>
        <li>Hang động và trong nhà luôn quang đãng; vùng tuyết hay có tuyết; mùa hè nắng nhiều hơn.</li>
        <li>Mỗi lần quái hồi sinh có {{ H.thayLoaiPhanTram }}% khả năng đổi sang loài hợp thời tiết, ban đêm hoặc mùa. Một số loài chỉ có mặt nửa năm (loài di cư).</li>
        <li><strong>Quái hiếm</strong> cấp cao hơn bình thường chỉ ra trong một kiểu thời tiết (có loài chỉ ra ban đêm) — cả máy chủ được báo khi chúng xuất hiện.</li>
      </ul>
      <div class="bang-cuon mt-5">
        <table class="bang-wiki">
          <thead>
            <tr>
              <th>Thời tiết</th><th>Hệ xuất hiện nhiều gấp {{ H.heSoSinh }}</th><th>Sát thương chiêu của bạn</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="t in THOI_TIET" :key="t.ten">
              <td class="whitespace-nowrap font-semibold text-highlighted">
                <UIcon :name="t.icon" class="mr-1.5 inline size-4 align-[-3px]" aria-hidden="true" />{{ t.ten }}
              </td>
              <td>{{ t.he }}</td>
              <td>{{ t.satThuong }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </WikiSection>

    <WikiSection id="bung-phat" title="Bùng phát" :lead="`Mỗi ngày ${H.gioBungPhat.length} khung giờ, mỗi khung 1 giờ: một tuyến đường được chọn và ${H.bungPhatPhanTram}% số lần quái hồi sinh ở đó đổi thành loài bùng phát.`">
      <ul class="flex flex-wrap gap-2">
        <li v-for="g in H.gioBungPhat" :key="g">
          <UBadge color="warning" variant="subtle" size="lg" icon="i-lucide-alarm-clock">
            {{ gioKhung(g) }}
          </UBadge>
        </li>
      </ul>
      <p class="van mt-3">
        Có thông báo khi khung bắt đầu và kết thúc. Bùng phát là lúc tốt để làm nhiệm vụ Pokédex và chụp ảnh cho album.
      </p>
    </WikiSection>

    <WikiSection id="ky-nang" title="Kỹ năng sống & thể lực" :lead="`${H.kyNang.length} kỹ năng, mỗi kỹ năng lên tối đa cấp ${H.capKyNangToiDa}. Dùng thể lực sinh hoạt — tách riêng khỏi năng lượng huấn luyện, nên không lấy mất giờ EXP của bạn.`">
      <div class="grid gap-3 sm:grid-cols-3">
        <div class="the-so">
          <b>{{ H.theLucNgay }}</b><span>thể lực mỗi ngày (tích tối đa {{ H.theLucToiDa }})</span>
        </div>
        <div class="the-so">
          <b>{{ H.tlCauCa }} / {{ H.tlDao }} / {{ H.tlNau }}</b><span>thể lực mỗi lần câu / đào / nấu hoặc chế</span>
        </div>
        <div class="the-so">
          <b>0</b><span>thu hoạch vườn không tốn thể lực</span>
        </div>
      </div>
      <ul class="mt-5 grid gap-3 sm:grid-cols-2">
        <li v-for="k in H.kyNang" :key="k" class="rounded-2xl glass p-4">
          <strong class="font-display text-highlighted">{{ k }}</strong>
          <p class="mt-1 text-sm text-muted">
            {{ KY_NANG_MO[k] ?? '' }}
          </p>
          <NuxtLink v-if="k === 'Câu cá'" to="/cam-nang/cau-ca" class="mt-1 inline-flex items-center gap-1 text-xs font-bold text-primary hover:underline">Hướng dẫn câu cá <UIcon name="i-lucide-arrow-right" class="size-3" /></NuxtLink>
        </li>
      </ul>
      <ul class="cham mt-4">
        <li>Mỗi lần câu, đào, nấu tốn một ít xu tiền nguyên liệu (mồi câu, xẻng, củi bếp).</li>
        <li>Nghỉ chơi từ 2 ngày trở lên thì thể lực đầy khi quay lại — chơi nhẹ không bị thiệt.</li>
        <li>Vật liệu nằm trong <strong>Túi sinh thái</strong> riêng, không chiếm ô hành trang. Chỉ thành phẩm (thuốc, đá tiến hoá, pet) mới ra vật phẩm thật.</li>
      </ul>
    </WikiSection>

    <WikiSection id="cong-thuc" title="Công thức" :lead="`${H.congThuc.length} công thức nấu ăn, chế thuốc và khảo cổ. Cấp ghi trong bảng là cấp kỹ năng cần có.`">
      <div v-for="g in nhomCongThuc" :key="g.kyNang" class="mt-6 first:mt-0">
        <h3 class="font-display text-xl font-bold text-highlighted">
          {{ g.kyNang }}
        </h3>
        <div class="bang-cuon mt-2">
          <table class="bang-wiki">
            <thead>
              <tr>
                <th>Công thức</th><th class="so">
                  Cấp
                </th><th>Nguyên liệu</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="c in g.ds" :key="c.ten">
                <td class="font-semibold text-highlighted">
                  {{ c.ten }}
                </td><td class="so">
                  {{ c.cap }}
                </td><td>{{ c.nguyenLieu.join(', ') }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <p class="mt-3 text-sm text-muted">
        Khảo cổ: ghép 12 mảnh thành một viên đá tiến hoá thật (dùng cho <NuxtLink to="/cam-nang/tien-hoa#eevee" class="lien-ket">Eevee</NuxtLink>), phục hồi hoá thạch thành pet, chế đồ trang trí vườn.
      </p>
    </WikiSection>

    <WikiSection id="nha-tre" title="Nhà trẻ & trứng">
      <ul class="cham">
        <li>Gửi 1–2 pet bạn đang sở hữu vào nhà trẻ (tốn một ít xu mỗi ngày). Mỗi quái bạn hạ tính là 1 bước.</li>
        <li>Đủ <strong>{{ H.nhaTre.buocTrung }} bước</strong> nhà trẻ đẻ 1 trứng; tối đa {{ H.nhaTre.trungNgay }} trứng/ngày, túi chứa {{ H.nhaTre.trungTui }} trứng.</li>
        <li>Trứng có chỉ số tiềm năng (IV) 0–{{ H.nhaTre.ivToiDa }} cho HP, công, thủ — thừa hưởng từ cha hoặc mẹ, đôi khi đột biến. Trứng phẩm chất S nở ra pet cấp 2.</li>
        <li>Trứng shiny: 1/{{ H.nhaTre.shinyMau }}; trứng thứ {{ H.nhaTre.shinyPity }} chưa shiny thì chắc chắn shiny.</li>
        <li>Trứng là hàng hoá: bán được trên chợ sinh thái (IV cao, shiny giá cao hơn). NPC không mua trứng.</li>
      </ul>
    </WikiSection>

    <WikiSection id="cho" title="Chợ sinh thái & đơn đặt hàng">
      <ul class="cham">
        <li>Bán cá, berry, khoáng, hoá thạch, món ăn, thuốc, trứng, đồ trang trí cho người chơi khác. Giá rao chọn ×1 / ×1,5 / ×2 / ×3 giá tham chiếu (trần ×{{ H.cho.giaToiDaLan }}).</li>
        <li>Thuế bán {{ H.cho.thue }}% và phí đăng {{ H.cho.phiDang }}% (không hoàn) — tiền này bị xoá khỏi game để giữ giá ổn định.</li>
        <li>Tối đa {{ H.cho.tinBan }} tin bán và {{ H.cho.don }} đơn đặt hàng cùng lúc; hết hạn sau {{ H.cho.hanNgay }} ngày thì hàng/tiền ký quỹ về hộp thư.</li>
        <li><strong>Đơn đặt hàng</strong>: ký quỹ trước, người bán giao dần, nhận tiền sau thuế. Huỷ đơn được hoàn phần chưa giao.</li>
        <li>NPC mua lại với {{ H.cho.npcPhanTram }}% giá tham chiếu, có trần mỗi ngày (không mua trứng và đồ trang trí).</li>
      </ul>
    </WikiSection>

    <WikiSection id="giai-tri" title="Đố vui, đua & giải tuần">
      <div class="grid gap-3 md:grid-cols-3">
        <div class="rounded-2xl glass p-4">
          <strong class="font-display text-highlighted">Ai là Pokémon?</strong>
          <p class="mt-1 text-sm text-muted">
            {{ H.doVui.gio.join(' và ') }} trên kênh thế giới. Có {{ H.doVui.giay }} giây, gợi ý hệ, số chữ cái, chữ đầu — trả lời bằng chat.
            Người đúng đầu tiên nhận {{ H.doVui.luongKhoa }} lượng khoá.
          </p>
        </div>
        <div class="rounded-2xl glass p-4">
          <strong class="font-display text-highlighted">Đua Pokémon</strong>
          <p class="mt-1 text-sm text-muted">
            Đăng ký trước {{ H.dua.phutDangKy }} phút, xuất phát {{ H.dua.gio }} hằng ngày (gõ <code>dua</code>). Chạy qua 5 mốc trong tối đa {{ H.dua.giayToiDa / 60 }} phút.
            Top 3 nhận {{ H.dua.thuong.join(' / ') }} lượng khoá. Thú cưỡi giúp bạn nhanh hơn.
          </p>
        </div>
        <div class="rounded-2xl glass p-4">
          <strong class="font-display text-highlighted">Giải đấu tuần</strong>
          <p class="mt-1 text-sm text-muted">
            Đăng ký qua menu Hệ sinh thái. Cây đấu loại trực tiếp công bố thứ Bảy 20:00, trận đánh ở Giải Liên minh. Nhà vô địch nhận lượng khoá và danh hiệu.
          </p>
        </div>
      </div>
    </WikiSection>

    <WikiSection id="suu-tam" title="Album, nghiên cứu & vùng">
      <ul class="cham">
        <li><strong>Album</strong> (tối đa {{ H.album.toiDa }} ảnh): {{ H.album.loai.join(', ').toLocaleLowerCase('vi') }}.</li>
        <li><strong>Nghiên cứu loài</strong>: mỗi loài có tới 6 nhiệm vụ (câu được, ấp nở, hạ trong thời tiết ưa thích, hạ ban đêm, gặp khi hiếm/bùng phát, nở shiny). Mỗi nhiệm vụ {{ H.nghienCuuLuongKhoa }} lượng khoá.</li>
        <li><strong>Hoàn thành vùng</strong>: {{ H.vung.luongKhoa }} lượng khoá, danh hiệu và +{{ soVn(H.vung.phanNghin / 10, 1) }}% chỉ số vĩnh viễn mỗi vùng (tối đa +{{ H.vung.tranPhanNghin / 10 }}%).</li>
      </ul>
      <div class="bang-cuon mt-4">
        <table class="bang-wiki">
          <caption>Mốc album</caption>
          <thead>
            <tr>
              <th class="so">
                Số ảnh
              </th><th class="so">
                Lượng khoá
              </th><th>Danh hiệu</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="m in H.album.moc" :key="m.anh">
              <td class="so">
                {{ m.anh }}
              </td><td class="so">
                {{ m.luongKhoa }}
              </td><td class="font-semibold text-highlighted">
                {{ m.danhHieu }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </WikiSection>

    <WikiSection id="lenh" title="Lệnh nhanh">
      <ul class="grid gap-2 sm:grid-cols-2">
        <li class="rounded-2xl glass px-4 py-3">
          <code class="font-bold text-highlighted">hst</code> — mở menu Hệ sinh thái (hoặc chọn "Hệ sinh thái" ở NPC Đổi thưởng)
        </li>
        <li class="rounded-2xl glass px-4 py-3">
          <code class="font-bold text-highlighted">cauca</code> — câu cá khi đứng cạnh hồ
        </li>
        <li class="rounded-2xl glass px-4 py-3">
          <code class="font-bold text-highlighted">dao</code> — khảo cổ; máy dò chỉ hướng tới điểm đào gần nhất
        </li>
        <li class="rounded-2xl glass px-4 py-3">
          <code class="font-bold text-highlighted">dua</code> — đăng ký Đua Pokémon
        </li>
      </ul>
      <p class="mt-3 text-sm text-muted">
        Gõ lệnh vào ô chat như một tin nhắn bình thường.
      </p>
    </WikiSection>
  </WikiShell>
</template>
