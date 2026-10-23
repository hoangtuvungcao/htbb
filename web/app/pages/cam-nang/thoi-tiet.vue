<script setup lang="ts">
/**
 * Thời tiết & ngày cộng đồng (0.9.8). Số liệu: shared/utils/heThong098.ts (data_thoi_tiet, data_ngay_cong_dong, hằng số
 * com.htbb.pokemon.ThoiTietLich / NgayCongDong) — đối chiếu sql/htbb.sql ở tests/he-thong-098.test.ts.
 */
import { NGAY_CONG_DONG, THOI_TIET } from '~~/shared/utils/heThong098'

const khung = Array.from({ length: THOI_TIET.soKhung }, (_, i) => `${String(i * THOI_TIET.gioKhung).padStart(2, '0')}:00–${String((i + 1) * THOI_TIET.gioKhung).padStart(2, '0')}:00`)
</script>

<template>
  <WikiShell slug="thoi-tiet">
    <WikiSection id="thoi-tiet" title="Thời tiết" :lead="`Mỗi ngày ${THOI_TIET.soKhung} khung ${THOI_TIET.gioKhung} giờ (${khung.join(', ')}). Khung sau giữ thời tiết khung trước ${THOI_TIET.giuPhanTram}% số lần nên trời không đổi loạn; hang, trong nhà và đấu trường luôn quang đãng. Thời tiết hiện ở đĩa nhỏ trên HUD.`">
      <ul class="grid gap-3 sm:grid-cols-2">
        <li v-for="t in THOI_TIET.loai" :key="t.ten" class="rounded-2xl glass p-4">
          <strong class="font-display text-highlighted">{{ t.ten }}</strong>
          <p class="mt-1 text-sm text-muted">
            {{ t.goiY }}
          </p>
        </li>
      </ul>
      <WikiCallout kieu="vi-sao">
        Ảnh hưởng nhỏ và có trần: thời tiết chỉ đổi sát thương chiêu của vài hệ tối đa ±{{ THOI_TIET.tranSatThuong }}%, và làm Pokémon
        hoang dã hợp hệ xuất hiện nhiều hơn — đủ để đổi bãi săn, không đủ để quyết định trận đấu.
      </WikiCallout>
    </WikiSection>

    <WikiSection id="mua" title="Màu mùa" lead="Thế giới phủ một lớp màu nhẹ theo mùa thật (xuân hoa anh đào, hạ, thu lá phong, đông tuyết lạnh) hoặc theo lễ đang diễn ra (Tết, Trung Thu, Halloween, Noel). Tắt được trong Cài đặt.">
      <p class="van">
        Trong các lễ, thời tiết hợp lễ ra nhiều hơn (Halloween sương mù, Noel tuyết…). Lịch lễ: <NuxtLink to="/cam-nang/su-kien" class="lien-ket">Sự kiện năm</NuxtLink>.
      </p>
    </WikiSection>

    <WikiSection id="cong-dong" title="Ngày cộng đồng" lead="Mỗi tháng một Chủ nhật, cả máy chủ săn cùng một dòng Pokémon nổi bật. Lịch tự chạy theo năm, website tự đăng tin “Sắp / Đang / Đã kết thúc”.">
      <div class="grid gap-3 sm:grid-cols-4">
        <div class="the-so">
          <b>{{ NGAY_CONG_DONG.gioBatDau }}:00</b><span>{{ NGAY_CONG_DONG.soGio }} giờ vàng bắt đầu</span>
        </div>
        <div class="the-so">
          <b>{{ NGAY_CONG_DONG.tiLePhanTram }}%</b><span>lượt hồi sinh đổi thành loài nổi bật trên bản đồ của nó</span>
        </div>
        <div class="the-so">
          <b>+{{ NGAY_CONG_DONG.expPhanTram }}%</b><span>EXP khi hạ loài nổi bật</span>
        </div>
        <div class="the-so">
          <b>×{{ NGAY_CONG_DONG.nhanShiny }}</b><span>tỉ lệ shiny trong giờ vàng</span>
        </div>
      </div>
      <p class="mt-4 text-sm text-muted">
        Hạ đủ {{ NGAY_CONG_DONG.soHa }} con loài nổi bật trong giờ vàng nhận danh hiệu của dòng đó. Không thêm xu hay lượng: chỉ thời gian, hình và
        <NuxtLink to="/cam-nang/danh-hieu" class="lien-ket">danh hiệu</NuxtLink>.
      </p>
    </WikiSection>
  </WikiShell>
</template>
