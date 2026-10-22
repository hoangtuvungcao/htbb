<script setup lang="ts">
/**
 * Lễ kết đôi (0.9.8, thay "đám cưới" của game gốc). Luật: com.htbb.pokemon.KetDoi + data_nhom_trung_ten (nhóm trứng);
 * số liệu shared/utils/heThong098.ts, đối chiếu sql/htbb_day_du.sql ở tests/he-thong-098.test.ts.
 */
import { KET_DOI } from '../../../shared/utils/heThong098'
</script>

<template>
  <WikiShell slug="ket-doi">
    <WikiSection id="dieu-kien" title="Điều kiện" lead="Hai Pokémon người chơi kết đôi như Pokémon thật: cùng ít nhất một nhóm trứng.">
      <ul class="cham">
        <li>Hai người lập <strong>nhóm đúng 2 người</strong> (cùng vào nhóm = đồng ý).</li>
        <li>Một Pokémon đực và một Pokémon cái, cả hai chưa kết đôi, cả hai từ <strong>cấp {{ KET_DOI.cap }}</strong>.</li>
        <li>Loài hiện tại của hai bên (lúc đăng ký) chung ít nhất một nhóm trứng. Tiến hoá về sau không làm mất kết đôi.</li>
      </ul>
    </WikiSection>

    <WikiSection id="nhom-trung" title="Nhóm trứng" :lead="`${KET_DOI.soNhomTrung} nhóm trứng theo Pokémon thật; nhóm “${KET_DOI.khongKetDoi}” (Pichu và Pokémon bé) không kết đôi với ai.`">
      <ul class="flex flex-wrap gap-2">
        <li v-for="n in KET_DOI.nhom" :key="n">
          <UBadge color="neutral" variant="subtle">
            {{ n }}
          </UBadge>
        </li>
        <li>
          <UBadge color="error" variant="subtle">
            {{ KET_DOI.khongKetDoi }}
          </UBadge>
        </li>
      </ul>
    </WikiSection>

    <WikiSection id="to-chuc" title="Tổ chức lễ" lead="Ở NPC Lễ kết đôi: Đăng ký Lễ kết đôi → mời khách bằng Thiệp mời kết đôi → Vào Lễ kết đôi → Khai lễ.">
      <ul class="cham">
        <li>Khách nhận lời mời sẽ được báo ngày diễn ra lễ; mỗi người chỉ nhận lời một Lễ kết đôi cùng lúc.</li>
        <li>Ở cùng NPC: <strong>Huỷ kết đôi</strong> {{ KET_DOI.huyLuong }} lượng, <strong>Đơn phương huỷ</strong> {{ KET_DOI.donPhuongLuong }} lượng.</li>
      </ul>
    </WikiSection>
  </WikiShell>
</template>
