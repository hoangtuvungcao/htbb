<script setup lang="ts">
const tenSite = useAppConfig().htbb.tenSite
useSeoMeta({
  title: 'Quyền riêng tư',
  description: `${tenSite} thu thập dữ liệu gì, dùng vào đâu: chỉ những gì cần để bạn đăng nhập và chơi. Không theo dõi, không quảng cáo.`,
})
const bang = [
  ['Tên tài khoản, mật khẩu (đã băm)', 'Đăng nhập game và website', 'Đến khi bạn yêu cầu xoá'],
  ['Email (nếu bạn nhập)', 'Lấy lại mật khẩu, thông báo bảo mật', 'Đến khi bạn đổi/xoá'],
  ['Dữ liệu nhân vật (cấp, vật phẩm, Pokédex…)', 'Vận hành game, bảng xếp hạng công khai', 'Theo tài khoản'],
  ['Địa chỉ IP, thời điểm đăng nhập', 'Chống spam đăng ký, chống gian lận, gỡ lỗi', 'Nhật ký máy chủ, xoá định kỳ'],
  ['Phiên đăng nhập website', 'Lưu trong localStorage của trình duyệt (không dùng cookie)', 'Tối đa 7 ngày hoặc khi đăng xuất'],
]
</script>

<template>
  <div>
    <SitePageBanner title="Quyền riêng tư" eyebrow="Pháp lý" intro="Chúng tôi chỉ giữ những gì cần để bạn chơi được — không hơn." />
    <UContainer class="max-w-4xl py-12 sm:py-16">
      <div class="wiki">
        <div class="grid gap-3 sm:grid-cols-3">
          <div class="the-so">
            <b>0</b><span>công cụ theo dõi / quảng cáo bên thứ ba</span>
          </div>
          <div class="the-so">
            <b>0</b><span>cookie theo dõi</span>
          </div>
          <div class="the-so">
            <b>Tự host</b><span>phông chữ & biểu tượng — không gọi dịch vụ ngoài</span>
          </div>
        </div>
        <WikiSection id="du-lieu" title="Dữ liệu chúng tôi lưu">
          <div class="bang-cuon">
            <table class="bang-wiki">
              <thead>
                <tr>
                  <th>Dữ liệu</th><th>Dùng để</th><th>Lưu bao lâu</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="r in bang" :key="r[0]">
                  <td class="font-semibold text-highlighted">
                    {{ r[0] }}
                  </td><td>{{ r[1] }}</td><td>{{ r[2] }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </WikiSection>
        <WikiSection id="chia-se" title="Chia sẻ & công khai">
          <ul class="cham">
            <li>Không bán, không chia sẻ dữ liệu cho bên thứ ba.</li>
            <li>Tên nhân vật, cấp, loài Pokémon có thể hiện công khai trên bảng xếp hạng.</li>
            <li>Email chỉ dùng để gửi thư khôi phục mật khẩu / thông báo tài khoản.</li>
          </ul>
        </WikiSection>
        <WikiSection id="quyen" title="Quyền của bạn">
          <ul class="cham">
            <li>Xem và đổi email, mật khẩu ở trang <NuxtLink to="/account" class="lien-ket">Tài khoản</NuxtLink>.</li>
            <li>Yêu cầu xoá tài khoản và dữ liệu qua <NuxtLink to="/download#support" class="lien-ket">kênh hỗ trợ</NuxtLink> (cần xác minh chủ tài khoản).</li>
            <li>Đăng xuất để xoá phiên khỏi trình duyệt, đặc biệt trên máy dùng chung.</li>
          </ul>
        </WikiSection>
        <p class="mt-12 text-sm text-muted">
          Xem thêm <NuxtLink to="/dieu-khoan" class="lien-ket">Điều khoản sử dụng</NuxtLink>. Cập nhật lần cuối: 17/10/2026.
        </p>
      </div>
    </UContainer>
  </div>
</template>
