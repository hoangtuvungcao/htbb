<script setup lang="ts">
/** Kinh tế & tiền tệ — chỉ những gì người chơi cần biết (số liệu từ cam-nang.json). */
const K = CAM_NANG.kinhTe
const T = CAM_NANG.trangBi
const tenSite = useAppConfig().htbb.tenSite

const tongDangNhap = K.lichDangNhap.reduce((s, x) => s + x, 0)
</script>

<template>
  <WikiShell slug="kinh-te">
    <WikiCallout kieu="luu-y" title="Không bán sức mạnh">
      {{ tenSite }} là dự án fan phi thương mại: không có cửa hàng bán chỉ số, mọi thứ ảnh hưởng sức mạnh đều kiếm được bằng cách chơi.
      Nạp (nếu máy chủ bật) có tỷ giá cố định ×1 và chỉ mua đồ trang trí, tiện ích. Pokémon mới bắt đầu với
      <strong>5.000 xu, 0 lượng, 0 lượng khoá</strong> — không có quà khi tạo Pokémon mới.
    </WikiCallout>

    <WikiSection id="tien" title="Ba loại tiền">
      <div class="grid gap-4 md:grid-cols-3">
        <div class="rounded-3xl glass p-5">
          <p class="flex items-center gap-2 font-display text-xl font-extrabold text-highlighted">
            <TienTe loai="xu" :co="36" /> Xu
          </p>
          <p class="mt-1 text-xs font-bold uppercase tracking-wide text-grass-700 dark:text-grass-400">
            Giao dịch được
          </p>
          <p class="mt-3 text-sm text-muted">
            Tiền chính. Rơi từ quái (khoảng {{ K.tiLeRoiXu }}% số con), nhiệm vụ, phân rã đồ. Dùng để nâng cấp, mua thuốc, sửa đồ, bay, khảm ngọc, mua bán ở chợ.
          </p>
        </div>
        <div class="rounded-3xl glass p-5">
          <p class="flex items-center gap-2 font-display text-xl font-extrabold text-highlighted">
            <TienTe loai="luong_khoa" :co="36" /> Lượng khoá
          </p>
          <p class="mt-1 text-xs font-bold uppercase tracking-wide text-pokered-600 dark:text-pokered-300">
            Không giao dịch được
          </p>
          <p class="mt-3 text-sm text-muted">
            Nhận từ nhiệm vụ, đăng nhập {{ K.lichDangNhap.length }} ngày liên tiếp ({{ K.lichDangNhap.join(' · ') }} — tổng {{ tongDangNhap }} mỗi vòng), thành tựu, sự kiện. Dùng mua bùa bảo hộ, vé, đồ tiện ích.
          </p>
        </div>
        <div class="rounded-3xl glass p-5">
          <p class="flex items-center gap-2 font-display text-xl font-extrabold text-highlighted">
            <TienTe loai="luong" :co="36" /> Lượng
          </p>
          <p class="mt-1 text-xs font-bold uppercase tracking-wide text-grass-700 dark:text-grass-400">
            Giao dịch được · hiếm
          </p>
          <p class="mt-3 text-sm text-muted">
            Có từ đua top, thành tựu lớn và top sự kiện; khi máy chủ bật nạp thì mua được trên website với tỷ giá cố định ×1. Chỉ dùng cho trang trí và tiện ích.
          </p>
        </div>
      </div>
      <p class="mt-4 text-sm text-muted">
        Lượng đến từ các đợt <strong>đua top</strong> do ban quản trị công bố (nhận ở NPC Tổng quản → "Nhận thưởng đua top"), thành tựu lớn và top sự kiện.
        Lượng dùng cho đồ trang trí ở <NuxtLink to="/cam-nang/cua-hang#cua-hang-trang-tri-luong" class="lien-ket">Cửa hàng trang trí</NuxtLink> và tiện ích, không mua được sức mạnh.
      </p>
    </WikiSection>

    <WikiSection id="kiem" title="Cách kiếm tiền">
      <ul class="cham">
        <li><strong>Đánh quái gần cấp mình</strong> — nguồn xu chính, tỉ lệ thuận với cấp (xem cột "Xu / giờ" ở <NuxtLink to="/cam-nang/cap-do#bang-exp" class="lien-ket">bảng EXP</NuxtLink>). Chịu giới hạn <NuxtLink to="/cam-nang/cap-do#nang-luong" class="lien-ket">năng lượng huấn luyện</NuxtLink>.</li>
        <li><strong>Nhiệm vụ</strong> trả xu + EXP theo "giờ thu nhập" ở cấp của nhiệm vụ, nên luôn đáng làm dù bạn cấp nào.</li>
        <li><strong>Sự kiện</strong>: chỉ {{ K.suKienPhanXu }}% giá trị phần thưởng là xu; phần lớn là vật phẩm khoá, đồ trang trí, vật phẩm hết hạn.</li>
        <li><strong>Giftcode</strong>: tối đa {{ K.giftcodeToiDaThang }} mã/tháng, giá trị nhỏ — không có mã "phá game". Nhập ở Menu → Thư/Quà → Nhập giftcode (từ 0.9.6).</li>
      </ul>
    </WikiSection>

    <WikiSection id="tieu" title="Tiêu tiền" lead="Những 'hố hút' giữ cho xu không mất giá. Chi phí tính theo thu nhập ở cấp của bạn, nên người mới không bị đắt và người giàu vẫn có chỗ tiêu.">
      <ul class="cham">
        <li><strong>Nâng cấp trang bị</strong>: chi phí tăng theo bậc và mức (từ {{ soVn(T.chiPhiNangCap[0]?.[0] ?? 0) }} tới {{ soVn(T.chiPhiNangCap.at(-1)?.at(-1) ?? 0) }} xu/lần) — xem <NuxtLink to="/cam-nang/trang-bi#chi-phi" class="lien-ket">bảng chi phí</NuxtLink>.</li>
        <li><strong>Thuốc</strong> khoảng {{ K.tieuHaoPhanTram }}% và <strong>sửa đồ</strong> khoảng {{ K.suaDoPhanTram }}% thu nhập mỗi giờ chơi.</li>
        <li><strong>Bay (Pidgeot)</strong>: mỗi lần ~{{ K.dichChuyenPhanTramGio }}% thu nhập 1 giờ.</li>
        <li><strong>Ngọc hệ</strong>, học chiêu, đồ trang trí bằng xu (không chỉ số).</li>
      </ul>
    </WikiSection>

    <WikiSection id="cho" title="Chợ & giao dịch">
      <div class="grid gap-3 sm:grid-cols-3">
        <div class="the-so">
          <b>{{ K.thueCho }}%</b><span>thuế khi bán được hàng ở chợ</span>
        </div>
        <div class="the-so">
          <b>{{ K.phiDangBan }}%</b><span>phí đăng bán (mất kể cả khi không bán được)</span>
        </div>
        <div class="the-so">
          <b>{{ K.tranGiaoDichGio }} giờ</b><span>thu nhập: trần chuyển xu trực tiếp / ngày</span>
        </div>
      </div>
      <h3 class="mt-6 font-display text-lg font-bold text-highlighted">
        Chốt chống clone khi chuyển xu
      </h3>
      <p class="mt-1 text-sm text-muted">
        Chuyển xu cho người khác (giao dịch, ký gửi, thú cưng, thách đấu cược) chỉ mở khi Pokémon gửi đủ cả ba điều kiện
        (mặc định, ban quản trị chỉnh được):
      </p>
      <div class="mt-3 grid gap-3 sm:grid-cols-4">
        <div class="the-so">
          <b>Cấp 20</b><span>Pokémon gửi từ cấp 20 trở lên</span>
        </div>
        <div class="the-so">
          <b>24 giờ</b><span>tài khoản đã tạo đủ 24 giờ</span>
        </div>
        <div class="the-so">
          <b>10 giờ</b><span>Pokémon đã chơi ít nhất 10 giờ</span>
        </div>
        <div class="the-so">
          <b>2 / ngày</b><span>mỗi người nhận: tối đa 2 Pokémon cùng thiết bị / IP gửi cho</span>
        </div>
      </div>
      <ul class="cham mt-4">
        <li><strong>Lượng khoá</strong> không bao giờ giao dịch được.</li>
        <li><strong>Trang bị khoá khi mặc</strong>: món đã mặc vào không bán, không trao đổi được nữa.</li>
      </ul>
      <WikiCallout kieu="vi-sao">
        Giới hạn chuyển xu trực tiếp và chốt chống clone giúp chống mua bán xu bằng tiền thật, chống nuôi tài khoản phụ và chống lừa đảo.
      </WikiCallout>
    </WikiSection>

    <WikiSection id="lam-phat" title="Vì sao giá cả ổn định?" lead="Mục tiêu: đồng xu bạn kiếm hôm nay vẫn có giá trị sau nhiều tháng chơi.">
      <ul class="cham">
        <li>Chi phí (nâng cấp, thuốc, bay, ngọc) <strong>tăng theo cấp</strong> giống như thu nhập — người mới không bị đắt, người chơi lâu năm vẫn có chỗ tiêu xu.</li>
        <li>Không có xu hay lượng "từ trên trời": không quà khi tạo Pokémon mới, không giftcode khổng lồ, nạp chỉ mua trang trí. Sự kiện thưởng chủ yếu vật phẩm khoá và đồ trang trí.</li>
        <li>Năng lượng huấn luyện giới hạn số giờ đánh quái hiệu quả mỗi ngày, nên treo máy cả ngày không làm xu mất giá.</li>
        <li>Đồ trang trí bằng xu là nơi để người chơi giàu tiêu tiền mà không ảnh hưởng sức mạnh của người khác.</li>
      </ul>
    </WikiSection>
  </WikiShell>
</template>
