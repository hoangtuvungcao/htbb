<script setup lang="ts">
/** Điều khoản thanh toán — BẢN NHÁP (docs/van-hanh.md). Cần luật sư rà soát trước khi bật nạp. */
const tenSite = useAppConfig().htbb.tenSite
const { data: site } = await useSite()
useSeoMeta({ title: 'Điều khoản thanh toán', description: `Điều khoản thanh toán (bản nháp) của ${tenSite}.`, robots: 'noindex' })
const muc = [
  { id: 'pham-vi', ten: 'Phạm vi', noi: [
    'Điều khoản này áp dụng cho mọi giao dịch mua lượng, Thẻ tháng và Vé mùa trên website. Chỉ có hiệu lực khi máy chủ mở tính năng nạp tiền.',
    'Tính năng nạp tiền chỉ được mở sau khi ban quản trị có giấy phép sử dụng hình ảnh và tài sản trí tuệ liên quan bằng văn bản.',
  ] },
  { id: 'san-pham', ten: 'Sản phẩm bán', noi: [
    'Lượng: tỷ giá cố định 10 lượng / 1.000 đ, không có hệ số ngẫu nhiên hay khuyến mãi %. Lượng chỉ mua được đồ trang trí và tiện ích (mở rộng hành trang, đổi tên); không mua được trang bị, chỉ số, vé kinh nghiệm hay tỉ lệ nâng cấp.',
    'Thẻ tháng: 30 ngày, nhận 20 lượng mỗi ngày đăng nhập và bấm nhận (ngày bỏ lỡ không cộng bù), kèm tiện ích nhỏ. Không mua thêm khi còn hơn 30 ngày.',
    'Vé mùa: mở nhánh thưởng cao cấp của mùa đang diễn ra; tiến độ chỉ có được bằng cách chơi; không có chỉ số độc quyền.',
    'Không có quay thưởng / hộp may rủi trả bằng tiền thật.',
  ] },
  { id: 'thanh-toan', ten: 'Thanh toán và giao hàng', noi: [
    'Thanh toán bằng chuyển khoản VietQR, đúng số tiền và đúng nội dung (mã đơn). Chuyển thiếu hoặc sai nội dung sẽ được xử lý thủ công.',
    'Sau khi xác nhận, vật phẩm tự vào Pokémon đã chọn trong khoảng 15 giây khi Pokémon đó đang online. Đơn chưa thanh toán hết hạn sau 24 giờ; tiền đến muộn vẫn được ghi nhận.',
    'Lịch sử đơn xem tại trang Nạp tiền. Mọi giao dịch được ghi sổ và lưu nhật ký kiểm toán.',
  ] },
  { id: 'gioi-han', ten: 'Giới hạn chi tiêu', noi: [
    'Mỗi tài khoản có trần nạp mặc định 1.000.000 đ/tuần và 2.000.000 đ/tháng. Người chơi có thể tự đặt trần thấp hơn; hạ có hiệu lực ngay, nâng hoặc bỏ có hiệu lực sau 72 giờ.',
    'Người chưa đủ 18 tuổi cần có sự đồng ý của cha mẹ/người giám hộ trước khi thanh toán.',
  ] },
  { id: 'giao-dich', ten: 'Giao dịch giữa người chơi', noi: [
    'Lượng không chuyển trực tiếp giữa người chơi. Đồ trang trí giao dịch được chỉ được mua bán qua Chợ trang trí trong game (thuế 7%, có nhật ký). Mua bán tài khoản/vật phẩm bằng tiền thật ngoài game bị cấm.',
  ] },
  { id: 'hoan-tien', ten: 'Hoàn tiền', noi: ['Xem Chính sách hoàn tiền. Khoản đã hoàn sẽ được thu hồi trong game (lượng thành nợ, trừ dần).'] },
]
</script>

<template>
  <div>
    <SitePageBanner title="Điều khoản thanh toán" eyebrow="Pháp lý · Bản nháp" intro="Áp dụng khi máy chủ mở nạp tiền." />
    <UContainer class="max-w-4xl py-12 sm:py-16">
      <div class="wiki">
        <WikiCallout kieu="luu-y" title="Bản nháp">
          {{ site?.napBat ? 'Đang áp dụng.' : 'Máy chủ hiện KHÔNG thu tiền — điều khoản này chưa có hiệu lực.' }}
          Nội dung cần được rà soát pháp lý trước khi áp dụng chính thức.
        </WikiCallout>
        <WikiSection v-for="(m, i) in muc" :id="m.id" :key="m.id" :title="`${i + 1}. ${m.ten}`">
          <ul class="cham">
            <li v-for="(n, j) in m.noi" :key="j">
              {{ n }}
            </li>
          </ul>
        </WikiSection>
        <p class="mt-12 text-sm text-muted">
          Xem thêm: <NuxtLink to="/chinh-sach-hoan-tien" class="lien-ket">Chính sách hoàn tiền</NuxtLink> · <NuxtLink to="/dieu-khoan" class="lien-ket">Điều khoản sử dụng</NuxtLink>. Bản nháp 18/10/2026.
        </p>
      </div>
    </UContainer>
  </div>
</template>
