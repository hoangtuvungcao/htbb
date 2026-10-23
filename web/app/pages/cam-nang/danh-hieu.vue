<script setup lang="ts">
/**
 * Danh hiệu (0.9.7 / 0.9.8). Số liệu: shared/utils/heThong098.ts (data_danh_hieu theo bậc, data_danh_hieu_do_hiem,
 * danh_hieu.tran_phan_nghin) — đối chiếu sql/htbb.sql ở tests/he-thong-098.test.ts.
 */
import { DANH_HIEU, phanTram098 } from '~~/shared/utils/heThong098'

const tong = DANH_HIEU.bac.reduce((s, b) => s + b.so, 0)
</script>

<template>
  <WikiShell slug="danh-hieu">
    <WikiSection id="bac" title="Bậc danh hiệu" :lead="`${tong} danh hiệu, 4 bậc màu — hiện thành một dòng nhỏ ngay trên tên Pokémon của bạn.`">
      <ul class="grid gap-3 sm:grid-cols-4">
        <li v-for="b in DANH_HIEU.bac" :key="b.ten" class="the-so">
          <b :style="{ color: b.mau }">{{ b.so }}</b><span>{{ b.ten }}</span>
        </li>
      </ul>
    </WikiSection>

    <WikiSection id="deo" title="Đeo & chỉ số" :lead="`Mở Hồ sơ → Danh hiệu để chọn danh hiệu đang đeo. Chỉ MỘT danh hiệu được tính, tối đa +${phanTram098(DANH_HIEU.tranPhanNghin)}% một chỉ số (HP, chí mạng, né tránh hoặc kháng trạng thái).`">
      <WikiCallout kieu="vi-sao" title="Chung một trần trang trí">
        Danh hiệu, bốn ô Tủ đồ và dòng chỉ số của thẻ đặt tên Pokémon cộng lại tối đa 5% — xem
        <NuxtLink to="/cam-nang/trang-phuc#ngan-sach" class="lien-ket">Trang phục & trang trí</NuxtLink>. Có nhiều danh hiệu không cộng dồn.
      </WikiCallout>
    </WikiSection>

    <WikiSection id="kiem" title="Nhận danh hiệu ở đâu">
      <ul class="grid gap-3 sm:grid-cols-2">
        <li class="rounded-2xl glass p-4">
          <strong class="font-display text-highlighted">Nhiệm vụ & thành tựu</strong>
          <p class="mt-1 text-sm text-muted">
            Phần lớn danh hiệu: chính tuyến, nhiệm vụ phụ, thành tựu (cấp, nâng cấp, câu cá, chế tạo…), huy hiệu Gym, Pokédex.
          </p>
        </li>
        <li class="rounded-2xl glass p-4">
          <strong class="font-display text-highlighted">Phó bản & Cúp PvP</strong>
          <p class="mt-1 text-sm text-muted">
            Hạ boss Hầm ngục; vô địch / á quân <NuxtLink to="/cup" class="lien-ket">Cúp PvP tuần</NuxtLink> nhận danh hiệu có hạn.
          </p>
        </li>
        <li class="rounded-2xl glass p-4">
          <strong class="font-display text-highlighted">Ngày cộng đồng & sự kiện</strong>
          <p class="mt-1 text-sm text-muted">
            Mỗi dòng nổi bật của <NuxtLink to="/cam-nang/thoi-tiet#cong-dong" class="lien-ket">Ngày cộng đồng</NuxtLink> có danh hiệu riêng; lễ hội trong năm cũng vậy.
          </p>
        </li>
        <li class="rounded-2xl glass p-4">
          <strong class="font-display text-highlighted">PvP, boss & hệ sinh thái</strong>
          <p class="mt-1 text-sm text-muted">
            Bậc thang PvP mùa, boss thế giới, nghề sinh hoạt — và một số danh hiệu ẩn chỉ hiện khi bạn đạt.
          </p>
        </li>
      </ul>
    </WikiSection>
  </WikiShell>
</template>
