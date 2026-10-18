<script setup lang="ts">
/** Chính sách hoàn tiền — BẢN NHÁP (HTBB/docs/pokemon/NAP_TIEN.md mục 8). */
const tenSite = useAppConfig().htbb.tenSite
const { data: site } = await useSite()
useSeoMeta({ title: 'Chính sách hoàn tiền', description: `Chính sách hoàn tiền (bản nháp) của ${tenSite}.`, robots: 'noindex' })
const muc = [
  { id: 'duoc-hoan', ten: 'Trường hợp được hoàn', noi: [
    'Đã chuyển tiền nhưng đơn không được xác nhận / vật phẩm không vào game sau 24 giờ và ban quản trị không khắc phục được.',
    'Chuyển trùng (hai lần cho một đơn) hoặc chuyển dư: hoàn phần dư.',
    'Trong 7 ngày kể từ khi thanh toán, nếu lượng / Thẻ tháng / Vé mùa của đơn đó CHƯA được sử dụng.',
    'Máy chủ ngừng hoạt động vĩnh viễn: hoàn theo tỷ lệ thời gian còn lại của Thẻ tháng / Vé mùa mua trong 30 ngày cuối.',
  ] },
  { id: 'khong-hoan', ten: 'Trường hợp không hoàn', noi: [
    'Lượng đã tiêu, đồ trang trí đã dùng, tiện ích đã kích hoạt (trừ lỗi của máy chủ).',
    'Tài khoản bị khoá do vi phạm quy tắc (gian lận, mua bán bằng tiền thật ngoài game...).',
  ] },
  { id: 'quy-trinh', ten: 'Quy trình', noi: [
    'Gửi yêu cầu qua kênh hỗ trợ kèm mã đơn (HTxxxxxxxx) và ảnh sao kê. Ban quản trị phản hồi trong 3 ngày làm việc và chuyển khoản trả lại trong 7 ngày làm việc kể từ khi chấp thuận.',
    'Khi hoàn tiền cho đơn đã vào game: lượng của đơn được thu hồi (trừ vào lượng đang có; phần thiếu thành nợ, trừ dần vào lượng nhận sau và tạm khoá cửa hàng tới khi trả hết), Thẻ tháng bị trừ 30 ngày, Vé mùa mất track cao cấp.',
  ] },
  { id: 'khieu-nai', ten: 'Khiếu nại qua ngân hàng (chargeback)', noi: [
    'Nếu bạn yêu cầu ngân hàng huỷ giao dịch, đơn sẽ bị thu hồi như trên và tài khoản tạm khoá tạo đơn nạp mới cho tới khi giải quyết xong. Hãy liên hệ ban quản trị trước — đa số trường hợp được xử lý nhanh hơn.',
  ] },
]
</script>

<template>
  <div>
    <SitePageBanner title="Chính sách hoàn tiền" eyebrow="Pháp lý · Bản nháp" intro="Khi nào được hoàn, hoàn thế nào, và điều gì xảy ra trong game." />
    <UContainer class="max-w-4xl py-12 sm:py-16">
      <div class="wiki">
        <WikiCallout kieu="luu-y" title="Bản nháp">
          {{ site?.napBat ? 'Đang áp dụng.' : 'Máy chủ hiện KHÔNG thu tiền — chính sách này chưa có hiệu lực.' }}
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
          Xem thêm: <NuxtLink to="/dieu-khoan-thanh-toan" class="lien-ket">Điều khoản thanh toán</NuxtLink>. Bản nháp 18/10/2026.
        </p>
      </div>
    </UContainer>
  </div>
</template>
