<script setup lang="ts">
/**
 * Tự động đánh chính thức (máy chủ điều khiển) — docs/thiet-ke-game.md,
 * com.htbb.pokemon.TuDong / real.TuDongGame. Số mặc định theo tools/luu_tru_sinh_du_lieu/cauhinh/danh_muc.json (nhóm tu_dong);
 * admin đổi được ở trang Cấu hình hệ thống.
 */
</script>

<template>
  <WikiShell slug="tu-dong">
    <WikiSection id="la-gi" title="Tự động đánh là gì">
      <p class="van">
        <strong>Tự động đánh</strong> là tính năng chính thức của game: Pokémon của bạn tự tìm Pokémon hoang dã quanh chỗ bạn
        đứng, tự ra chiêu theo thứ tự bạn chọn, tự uống thuốc và nhặt đồ. Máy chủ quyết định mọi thứ — đếm giờ, kiểm tra nơi
        được dùng và tự dừng khi cần — nên mọi người chơi đều theo cùng một luật.
      </p>
      <WikiCallout kieu="vi-sao" title="Không còn mod tự động">
        Các bản mod cũ (tự luyện, tự PK, tự giao hàng, tự vận may, tự lên Gym…) đã bị bỏ. Chỉ có Tự động đánh chính thức.
      </WikiCallout>
    </WikiSection>

    <WikiSection id="cach-dung" title="Cách dùng">
      <ol class="cham">
        <li>Đi tới một bãi luyện tập có Pokémon hoang dã.</li>
        <li>Mở bảng <strong>Tự động đánh</strong>: chạm Poké Ball nhỏ ở mép trái → "Tự động đánh" (điện thoại cảm ứng), phím <code>#</code> → "Tự động đánh" (máy bấm phím), hoặc menu chính → hàng đầu. Trên máy tính: Menu nhanh → "Tự động đánh".</li>
        <li>Chọn thứ tự tối đa 4 chiêu (để trống = dùng mọi chiêu đánh đã học, chiêu mạnh trước), ngưỡng thuốc HP / MP, kiểu nhặt đồ, vùng đánh (số ô quanh chỗ bắt đầu) và có <em>về hồi phục</em> hay không.</li>
        <li>Bấm <strong>Bắt đầu</strong>. Thanh xanh dưới khung HP cho biết thời gian còn lại hôm nay và Pokémon đang làm gì.</li>
      </ol>
      <ul class="cham mt-4">
        <li><strong>Về hồi phục</strong>: hết thuốc mà HP thấp thì Pokémon quay về chỗ bắt đầu, đứng nghỉ tới 80% HP rồi đánh tiếp.</li>
        <li><strong>Tự hồi sinh</strong> (mặc định tắt): nếu gục, được dùng vật phẩm hồi sinh tại chỗ tối đa vài lần mỗi giờ; tắt thì Tự động đánh dừng và bạn về Trung tâm Pokémon như thường.</li>
        <li>Bạn bấm di chuyển hay chạm màn hình thì Tự động đánh nhường bạn vài giây; mở menu / hộp thoại thì tạm dừng.</li>
      </ul>
    </WikiSection>

    <WikiSection id="gioi-han" title="Giờ mỗi ngày & giới hạn">
      <div class="grid gap-3 sm:grid-cols-3">
        <div class="the-so">
          <b>5 giờ</b><span>mỗi ngày, miễn phí (đặt lại lúc 00:00)</span>
        </div>
        <div class="the-so">
          <b>+2 giờ</b><span>khi có thẻ tháng</span>
        </div>
        <div class="the-so">
          <b>15 ô</b><span>vùng đánh lớn nhất quanh chỗ bắt đầu</span>
        </div>
      </div>
      <ul class="cham mt-4">
        <li>Chỉ tính giờ khi bạn <strong>đang online và đang bật</strong>. Thoát game là dừng.</li>
        <li><strong>Không thưởng thêm</strong>: EXP, xu và đồ rơi y như tự đánh tay và vẫn theo <NuxtLink to="/cam-nang/cap-do#nang-luong" class="lien-ket">năng lượng huấn luyện</NuxtLink> (100% → 50% → 20% → 0%).</li>
        <li>Nhịp ra đòn, uống thuốc, nhặt đồ bị giới hạn như người thật; thao tác nhanh bất thường sẽ bị bỏ qua và làm Tự động đánh dừng.</li>
        <li>Số giờ và giới hạn có thể được điều chỉnh theo thông báo của máy chủ.</li>
      </ul>
    </WikiSection>

    <WikiSection id="tu-dung" title="Khi nào tự dừng">
      <ul class="cham">
        <li>Hết giờ của ngày.</li>
        <li>Rời bãi luyện tập, đổi bản đồ hoặc đi quá xa vùng đã chọn.</li>
        <li>Bị Pokémon khác tấn công, bạn tự đánh người, bật PK, vào sàn đấu / Gym / chiến trường, hoặc gặp boss — những lúc đó bạn tự điều khiển.</li>
        <li>Pokémon gục (trừ khi bật Tự hồi sinh và còn lượt).</li>
      </ul>
      <WikiCallout kieu="meo" title="Không bật được?">
        Bảng ghi lý do ngay dòng thứ hai: chưa ở bãi luyện tập, đang PK, hết giờ hôm nay, hoặc máy chủ đang tạm khoá tính năng.
      </WikiCallout>
    </WikiSection>

    <WikiSection id="treo-may" title="Khác vé treo máy">
      <p class="van">
        <strong>Phiếu ủy thác luyện tập</strong> (vé treo máy) vẫn giữ nguyên: nó cho EXP khi bạn <em>thoát game</em> (offline),
        tách riêng hoàn toàn với Tự động đánh. Tự động đánh không dùng và không tiêu vé treo máy.
      </p>
    </WikiSection>
  </WikiShell>
</template>
