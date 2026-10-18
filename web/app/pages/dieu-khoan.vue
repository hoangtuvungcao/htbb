<script setup lang="ts">
const tenSite = useAppConfig().htbb.tenSite
useSeoMeta({
  title: 'Điều khoản sử dụng',
  description: `Điều khoản sử dụng ${tenSite}: dự án fan phi thương mại, không liên kết với Nintendo/GAME FREAK/The Pokémon Company, quy tắc chơi và giới hạn trách nhiệm.`,
})
const muc = [
  { id: 'phi-thuong-mai', ten: 'Dự án fan phi thương mại', noi: [
    `${tenSite} là dự án do cộng đồng người hâm mộ thực hiện, hoàn toàn miễn phí. Không có nạp tiền, không bán vật phẩm, không quảng cáo.`,
    'Pokémon, tên các loài Pokémon và mọi hình ảnh, thương hiệu liên quan thuộc về Nintendo, Creatures Inc., GAME FREAK inc. và The Pokémon Company. Dự án không liên kết, không được tài trợ hay xác nhận bởi các công ty này.',
    'Sprite và chân dung Pokémon dùng theo giấy phép Creative Commons BY-NC 4.0 của PMDCollab (bắt buộc ghi công, cấm thương mại). Danh sách tác giả ở trang Giấy phép & nguồn tài nguyên (/giay-phep).',
    'Nếu bạn là chủ sở hữu quyền và muốn gỡ nội dung, hãy liên hệ ban quản trị qua kênh hỗ trợ — chúng tôi sẽ xử lý nhanh nhất có thể.',
  ] },
  { id: 'tai-khoan', ten: 'Tài khoản', noi: [
    'Bạn chịu trách nhiệm giữ bí mật mật khẩu. Không mua bán, cho thuê hay chia sẻ tài khoản.',
    'Tài khoản và mọi vật phẩm trong game không có giá trị tiền tệ thật và không thể quy đổi ra tiền.',
    'Ban quản trị có thể tạm khoá hoặc xoá tài khoản vi phạm quy tắc bên dưới.',
  ] },
  { id: 'quy-tac', ten: 'Quy tắc chơi', noi: [
    'Không dùng bot, phần mềm tự động, client sửa đổi hay lợi dụng lỗi. Phát hiện lỗi hãy báo cho ban quản trị.',
    'Không mua bán vật phẩm, xu, tài khoản bằng tiền thật (RMT).',
    'Tôn trọng người chơi khác: không lăng mạ, quấy rối, lừa đảo, phát tán thông tin cá nhân của người khác.',
    'Tên Pokémon/đội không được phản cảm, mạo danh ban quản trị hay người khác.',
  ] },
  { id: 'dich-vu', ten: 'Dịch vụ & thay đổi', noi: [
    'Máy chủ được vận hành "như hiện có", có thể bảo trì, thay đổi cân bằng, hoặc ngừng hoạt động mà không bồi hoàn — vì không có giao dịch tiền nào.',
    'Nội dung cẩm nang trên website có thể khác với game trong thời gian cập nhật; thông báo trong game là chuẩn.',
    'Điều khoản có thể được cập nhật; ngày cập nhật ghi ở cuối trang.',
  ] },
]
</script>

<template>
  <div>
    <SitePageBanner title="Điều khoản sử dụng" eyebrow="Pháp lý" :intro="`Ngắn gọn: ${tenSite} miễn phí, phi thương mại, chơi đẹp và tôn trọng nhau.`" />
    <UContainer class="max-w-4xl py-12 sm:py-16">
      <div class="wiki">
        <WikiCallout kieu="luu-y" title="Tuyên bố miễn trừ">
          Pokémon là thương hiệu của Nintendo / Creatures / GAME FREAK / The Pokémon Company. {{ tenSite }} là dự án fan phi thương mại, không chính thức.
        </WikiCallout>
        <WikiSection v-for="(m, i) in muc" :id="m.id" :key="m.id" :title="`${i + 1}. ${m.ten}`">
          <ul class="cham">
            <li v-for="(n, j) in m.noi" :key="j">
              {{ n }}
            </li>
          </ul>
        </WikiSection>
        <p class="mt-12 text-sm text-muted">
          Xem thêm: <NuxtLink to="/quyen-rieng-tu" class="lien-ket">Quyền riêng tư</NuxtLink> · <NuxtLink to="/giay-phep" class="lien-ket">Giấy phép & nguồn tài nguyên</NuxtLink>. Cập nhật lần cuối: 17/10/2026.
        </p>
      </div>
    </UContainer>
  </div>
</template>
