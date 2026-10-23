<script setup lang="ts">
/**
 * Tài khoản & đăng ký — đăng ký trên web và trong game dùng CHUNG một bộ quy tắc
 * (shared/data/quy-tac-dang-ky.json -> QuyTacDangKy.java), nên trang này đọc số từ QUY_TAC_DANG_KY.
 */
const Q = QUY_TAC_DANG_KY
const viDuCam = [...new Set([...Q.tenQuanTri.batDau.slice(0, 5), ...Q.tenQuanTri.bang.slice(0, 2)])]
const thaySo = Object.entries(Q.thayTheSo).map(([so, chu]) => `${so}→${chu}`).join(', ')
</script>

<template>
  <WikiShell slug="tai-khoan">
    <WikiSection id="hai-cach" title="Hai cách đăng ký" lead="Một tài khoản dùng chung cho mọi bản game (Android, PC, Java) và cho website. Đăng ký ở đâu cũng theo cùng một bộ quy tắc.">
      <div class="grid gap-4 md:grid-cols-2">
        <div class="rounded-3xl glass p-5">
          <p class="flex items-center gap-2 font-display text-lg font-extrabold text-highlighted">
            <UIcon name="i-lucide-globe" class="size-5 text-primary" aria-hidden="true" /> Trên website
          </p>
          <ol class="cham mt-3">
            <li>Mở trang <NuxtLink to="/register" class="lien-ket">Đăng ký</NuxtLink>.</li>
            <li>Nhập tên tài khoản, mật khẩu (2 lần) và <strong>email</strong> — email là bắt buộc, dùng để lấy lại mật khẩu.</li>
            <li>Đăng nhập vào game bằng tài khoản vừa tạo.</li>
          </ol>
        </div>
        <div class="rounded-3xl glass p-5">
          <p class="flex items-center gap-2 font-display text-lg font-extrabold text-highlighted">
            <UIcon name="i-lucide-gamepad-2" class="size-5 text-primary" aria-hidden="true" /> Trong game
          </p>
          <ol class="cham mt-3">
            <li>Ở màn hình đăng nhập, chọn <strong>Đăng ký</strong>.</li>
            <li>Nhập tên tài khoản và mật khẩu; email {{ Q.email.batBuocGame ? 'bắt buộc' : 'có thể bỏ trống' }}.</li>
            <li>Trả lời câu hỏi tính nhẩm (ví dụ "ba cộng bốn bằng mấy?") để chứng minh bạn không phải máy.</li>
          </ol>
          <p class="mt-3 text-sm text-muted">
            Bản game quá cũ chưa đăng ký được trong game — cập nhật ở trang <NuxtLink to="/download" class="lien-ket">Tải game</NuxtLink> hoặc đăng ký trên web.
          </p>
        </div>
      </div>
      <WikiCallout v-if="!Q.email.batBuocGame" kieu="meo">
        Đăng ký trong game mà chưa nhập email? Hãy đăng nhập website và thêm email ở trang <NuxtLink to="/account" class="lien-ket">Tài khoản</NuxtLink> để lấy lại mật khẩu khi quên.
      </WikiCallout>
    </WikiSection>

    <WikiSection id="quy-tac" title="Quy tắc tên & mật khẩu">
      <div class="bang-cuon">
        <table class="bang-wiki">
          <tbody>
            <tr>
              <th scope="row">
                Tên tài khoản
              </th>
              <td>{{ Q.tenTaiKhoan.min }}–{{ Q.tenTaiKhoan.max }} ký tự, chỉ chữ thường a–z và số 0–9 (không dấu, không khoảng trắng).</td>
            </tr>
            <tr>
              <th scope="row">
                Mật khẩu
              </th>
              <td>{{ Q.matKhau.min }}–{{ Q.matKhau.max }} ký tự, chỉ chữ cái không dấu và số; không được là {{ Q.matKhau.cam.join(', ') }}.</td>
            </tr>
            <tr>
              <th scope="row">
                Email
              </th>
              <td>Tối đa {{ Q.email.toiDa }} ký tự. {{ Q.email.batBuocWeb ? 'Bắt buộc khi đăng ký trên web' : 'Không bắt buộc trên web' }}{{ Q.email.batBuocGame ? ' và trong game' : ', không bắt buộc trong game' }}.</td>
            </tr>
            <tr>
              <th scope="row">
                Tên bị chặn
              </th>
              <td>Tên giống quản trị viên (ví dụ bắt đầu bằng {{ viDuCam.join(', ') }}…) và từ ngữ thô tục — kể cả khi viết lách bằng số ({{ thaySo }}).</td>
            </tr>
            <tr>
              <th scope="row">
                Giới hạn
              </th>
              <td>Mỗi địa chỉ mạng chỉ tạo được một số tài khoản mỗi giờ và mỗi ngày (tính chung web và game, chỉ tính lần tạo thành công). Dùng chung mạng di động mà bị báo giới hạn: thử lại sau ít phút bằng Wi-Fi khác hoặc liên hệ hỗ trợ.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </WikiSection>

    <WikiSection id="kich-hoat" title="Kích hoạt & bảo mật">
      <ul class="cham">
        <li>Sau khi đăng ký, trang web (hoặc game) cho biết tài khoản đã sẵn sàng hay cần quản trị viên kích hoạt. Xem trạng thái ở trang <NuxtLink to="/kichhoat" class="lien-ket">Kích hoạt</NuxtLink> sau khi đăng nhập.</li>
        <li>Quên mật khẩu: dùng trang <NuxtLink to="/quen-mat-khau" class="lien-ket">Quên mật khẩu</NuxtLink> (cần email đã đăng ký).</li>
        <li>Không chia sẻ mật khẩu. Quản trị viên không bao giờ hỏi mật khẩu của bạn.</li>
      </ul>
    </WikiSection>
  </WikiShell>
</template>
