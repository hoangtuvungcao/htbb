<script setup lang="ts">
const { camNang: CAM_NANG } = await useCamNang() // số liệu CSDL game, dự phòng JSON
/**
 * Sổ tay huấn luyện — menu "Sổ tay huấn luyện" trong game (SoTayLauDai.java). Số liệu: năng lượng,
 * lịch đăng nhập, vé chương từ cam-nang.json (ThongSoLauDai.java); quà hoạt động, sư phạm, hầm ngục
 * từ cam-nang-noi-dung.json (QuaHoatDongLogic.java, CuonHut.java).
 */
const C = CAM_NANG.capDo
const N = CAM_NANG.nhiemVu
const K = CAM_NANG.kinhTe
const ST = CAM_NANG_ND.soTay
const Q = ST.quaHoatDong

/** Mỗi mục menu -> trang cẩm nang giải thích chi tiết. */
const LIEN_KET: Record<string, string> = {
  'Năng lượng và tinh thông': '/cam-nang/cap-do#nang-luong',
  'Nhiệm vụ tuần': '/cam-nang/nhiem-vu#ngay-tuan',
  'Hầm ngục bí ẩn': '/cam-nang/ham-nguc',
  'Thang PvP mùa': '/cam-nang/pvp',
  'Sư phạm (nhận trò)': '/cam-nang/doi-su-pham#su-pham',
  'Thách đấu Gym': '/cam-nang/gym',
  'Cửa hàng và đổi vật phẩm': '/cam-nang/cua-hang',
}
const mocQua = Q.phut.map((phut, i) => {
  const thuong: string[] = []
  if (Q.luongKhoa[i]) thuong.push(`${Q.luongKhoa[i]} lượng khoá`)
  if (Q.da[i]) thuong.push(`${Q.da[i]} đá nâng cấp`)
  if (Q.xuPhanTramGio[i]) thuong.push(`xu bằng ${Q.xuPhanTramGio[i]}% thu nhập một giờ`)
  if (Q.nguyenLieu[i]) thuong.push(`${Q.nguyenLieu[i]} nguyên liệu`)
  return { phut, thuong: thuong.join(' + ') || '—' }
})
const tongLichDangNhap = K.lichDangNhap.reduce((s, x) => s + x, 0)
</script>

<template>
  <WikiShell slug="so-tay">
    <WikiSection id="mo" title="Mở sổ tay" lead="Sổ tay huấn luyện gom mọi việc dài hạn vào một menu: năng lượng, nhiệm vụ tuần, Gym, Hầm ngục, PvP mùa, quà hằng ngày.">
      <ol class="cham">
        <li>Đến NPC <strong>Đổi thưởng</strong> trong làng và mở menu của NPC.</li>
        <li>Chọn mục <strong>Sổ tay huấn luyện</strong> ở cuối danh sách (sau "Xem đổi thưởng" và "Pokédex").</li>
        <li>Chọn một trong {{ ST.muc.length }} mục bên dưới.</li>
      </ol>
      <ul class="mt-5 grid gap-2 sm:grid-cols-2">
        <li v-for="(m, i) in ST.muc" :key="m" class="flex items-center gap-3 rounded-2xl glass px-4 py-3">
          <span class="grid size-8 shrink-0 place-items-center rounded-lg bg-navy-800 font-pixel text-[10px] text-pikachu-300" aria-hidden="true">{{ i + 1 }}</span>
          <NuxtLink v-if="LIEN_KET[m]" :to="LIEN_KET[m]" class="font-semibold text-highlighted hover:text-primary hover:underline">{{ m }}</NuxtLink>
          <span v-else class="font-semibold text-highlighted">{{ m }}</span>
        </li>
      </ul>
    </WikiSection>

    <WikiSection id="nang-luong" title="Năng lượng & tinh thông">
      <p class="van">
        Mục này cho biết hôm nay bạn đã đánh quái bao nhiêu phút, còn bao nhiêu phút ở tốc độ EXP/xu đầy đủ, điểm tăng tốc đã dùng hôm nay/tuần này (vé EXP, ủy thác đang chờ) và điểm tinh thông.
      </p>
      <ul class="cham mt-4">
        <li>Mỗi ngày có <strong>{{ C.nangLuong.phutNgay }} phút</strong> ở 100% tốc độ; phần chưa dùng cộng dồn tới tối đa {{ C.nangLuong.phutTichToiDa }} phút.</li>
        <li v-for="(b, i) in C.nangLuong.bacMet" :key="i">
          {{ i === 0 ? 'Hết phần 100%' : 'Sau đó' }}: {{ b.phut }} phút tiếp theo ở {{ b.tocDo }}%{{ i === C.nangLuong.bacMet.length - 1 ? ', rồi không còn EXP từ quái' : '' }}.
        </li>
        <li>Ở cấp tối đa, EXP dư đổi thành <strong>điểm tinh thông</strong>: mỗi điểm +{{ soVn(C.tinhThong.phanNghinMoiDiem / 10, 1) }}%, tối đa {{ C.tinhThong.toiDa }} điểm. Bạn chọn cộng vào tấn công hoặc HP.</li>
      </ul>
      <p class="mt-3 text-sm">
        Chi tiết: <NuxtLink to="/cam-nang/cap-do#nang-luong" class="lien-ket">Cấp độ & năng lượng</NuxtLink>.
      </p>
    </WikiSection>

    <WikiSection id="nhiem-vu" title="Nhiệm vụ của tôi & nhiệm vụ tuần">
      <ul class="cham">
        <li>Nhận tối đa <strong>{{ N.gioiHanNgay }} nhiệm vụ ngày</strong> và <strong>{{ N.gioiHanTuan }} nhiệm vụ tuần</strong> cùng lúc.</li>
        <li>Nhiệm vụ tuần làm mới mỗi thứ Hai lúc 05:00.</li>
        <li>"Nhiệm vụ của tôi" liệt kê mọi nhiệm vụ đang nhận và tiến độ.</li>
      </ul>
    </WikiSection>

    <WikiSection id="dang-nhap" title="Lịch đăng nhập & vé chương" :lead="`Đăng nhập mỗi ngày nhận lượng khoá theo vòng ${K.lichDangNhap.length} ngày (tổng ${tongLichDangNhap} lượng khoá mỗi vòng), rồi lặp lại.`">
      <ol class="grid grid-cols-4 gap-2 sm:grid-cols-7">
        <li v-for="(x, i) in K.lichDangNhap" :key="i" class="rounded-2xl glass p-3 text-center">
          <span class="block text-xs font-bold uppercase text-dimmed">Ngày {{ i + 1 }}</span>
          <span class="block font-display text-2xl font-black text-highlighted">{{ x }}</span>
          <span class="block text-[11px] text-muted">lượng khoá</span>
        </li>
      </ol>
      <ul class="cham mt-4">
        <li>Lỡ 1 ngày vẫn giữ chuỗi; nghỉ từ 2 ngày liền thì quay về ngày 1.</li>
        <li>Đăng nhập đủ {{ Q.ngayThang }} ngày trong tháng có thêm thưởng tháng.</li>
        <li><strong>Vé chương</strong> mở chương chính tuyến: nhận +{{ N.veChuongTuan }} vé mỗi thứ Hai 05:00, giữ tối đa {{ N.veChuongToiDa }} vé.</li>
      </ul>
    </WikiSection>

    <WikiSection id="qua-hoat-dong" title="Quà hoạt động hôm nay" :lead="`Tính theo thời gian chơi thật trong ngày; đứng yên quá ${Q.giayDungYen} giây thì không được tính.`">
      <div class="bang-cuon">
        <table class="bang-wiki">
          <thead>
            <tr>
              <th class="so">
                Mốc
              </th><th>Phần thưởng</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="m in mocQua" :key="m.phut">
              <td class="so font-semibold text-highlighted">
                {{ m.phut }} phút
              </td><td>{{ m.thuong }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </WikiSection>

    <WikiSection id="su-pham" title="Sư phạm (nhận trò)">
      <ul class="cham">
        <li>Làm thầy từ cấp <strong>{{ ST.suPham.capThay }}</strong>; trò phải thấp hơn thầy ít nhất {{ ST.suPham.cachCap }} cấp và đang chung nhóm.</li>
        <li>Trò được <strong>+{{ ST.suPham.expPhanTram }}% EXP</strong> khi đánh quái cùng nhóm với thầy.</li>
        <li>Khi trò đạt cấp {{ ST.suPham.moc.join(' và cấp ') }}, cả thầy và trò cùng nhận thưởng lượng khoá và đá nâng cấp.</li>
      </ul>
    </WikiSection>

    <WikiCallout kieu="meo">
      Gym, Hầm ngục bí ẩn và Thang PvP mùa đều mở từ sổ tay — xem
      <NuxtLink to="/cam-nang/gym" class="lien-ket">Gym & huy hiệu</NuxtLink>,
      <NuxtLink to="/cam-nang/ham-nguc" class="lien-ket">Hầm ngục bí ẩn</NuxtLink> và
      <NuxtLink to="/cam-nang/pvp" class="lien-ket">PvP xếp hạng mùa</NuxtLink>.
    </WikiCallout>
  </WikiShell>
</template>
