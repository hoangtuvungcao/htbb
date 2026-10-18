<script setup lang="ts">
/**
 * Hầm Ngục Bí Ẩn — luật từ HamNgucGame.java / CuonHut.java (cam-nang-noi-dung.json); các chặng
 * nhiệm vụ "hang_bi_an" từ nhiem_vu_moi.csv (cam-nang.json).
 */
const HN = CAM_NANG_ND.hamNguc
const CHANG = CAM_NANG.nhiemVu.hamNguc
const vietHoaDau = (t: string) => t.charAt(0).toLocaleUpperCase('vi') + t.slice(1)
const suKienHn = CAM_NANG.suKien.filter(s => /hầm ngục/i.test(`${s.ten1} ${s.coChe}`))
</script>

<template>
  <WikiShell slug="ham-nguc">
    <WikiSection id="gioi-thieu" title="Hầm ngục là gì" lead="Thử thách mỗi ngày lấy cảm hứng từ Pokémon Mystery Dungeon: leo càng nhiều tầng, thưởng càng lớn. Mọi người chơi cùng một đề trong ngày.">
      <ol class="cham">
        <li>Mở <NuxtLink to="/cam-nang/so-tay" class="lien-ket">Sổ tay huấn luyện</NuxtLink> → <strong>Hầm ngục bí ẩn</strong> → <strong>Vào tầng</strong>.</li>
        <li>Mỗi tầng yêu cầu hạ một số quái <strong>chênh không quá {{ HN.lechCap }} cấp</strong> so với bạn, ở bất kỳ bản đồ nào. Đủ số quái thì tự sang tầng sau.</li>
        <li>Số quái mỗi tầng tăng dần theo tầng; đề sinh theo ngày nên cả máy chủ gặp cùng một chuỗi tầng.</li>
        <li>Tối đa <strong>{{ HN.tangToiDa }} tầng</strong>. Tiến độ về tầng 0 khi sang ngày mới.</li>
      </ol>
      <WikiCallout kieu="luu-y">
        Bấm "Vào tầng" lần nữa sẽ bắt đầu lại từ đầu — chỉ bấm một lần mỗi ngày rồi cứ đánh quái.
      </WikiCallout>
    </WikiSection>

    <WikiSection id="thuong" title="Phần thưởng" lead="Nhận 1 lần mỗi ngày trong cùng menu, tính theo tầng cao nhất bạn đã qua.">
      <div class="grid gap-3 sm:grid-cols-2">
        <div class="the-so">
          <b>{{ HN.expPhanTramGio }}%</b><span>EXP của một giờ đánh quái ở tầng {{ HN.tangDuExp }} trở lên (thấp hơn thì nhận theo tỉ lệ tầng)</span>
        </div>
        <div class="the-so">
          <b>{{ HN.luongKhoa }} lượng khoá</b><span>khi qua từ tầng {{ HN.tangLuongKhoa }} trở lên</span>
        </div>
      </div>
    </WikiSection>

    <WikiSection v-if="CHANG.length" id="tang" title="Nhiệm vụ Hầm ngục">
      <ol class="grid gap-3 sm:grid-cols-2">
        <li v-for="(q, i) in CHANG" :key="i" class="flex items-center gap-4 rounded-2xl glass p-4">
          <span class="grid size-12 shrink-0 place-items-center rounded-xl bg-navy-800 font-pixel text-xs text-pikachu-300" aria-hidden="true">{{ i + 1 }}</span>
          <span>
            <strong class="block font-display text-highlighted">{{ vietHoaDau(q.ten.replace(/^Hầm ngục bí ẩn:\s*/i, '')) }}</strong>
            <span class="text-sm text-muted">Từ cấp {{ q.cap }}<template v-if="q.yeuCau"> · {{ q.yeuCau }}</template></span>
          </span>
        </li>
      </ol>
      <p v-for="s in suKienHn" :key="s.tuan" class="mt-4 text-sm text-muted">
        <UIcon name="i-lucide-calendar" class="mr-1 inline size-4 align-[-3px]" aria-hidden="true" /> Tuần {{ s.tuan }}: <strong class="text-highlighted">{{ s.ten1 }}</strong> — {{ s.coChe }}.
      </p>
    </WikiSection>

    <WikiSection id="meo" title="Mẹo">
      <ul class="cham">
        <li>Chọn bãi quái gần cấp bạn (xem <NuxtLink to="/cam-nang/ban-do#luyen-cap" class="lien-ket">Khu luyện cấp</NuxtLink>) — quái chênh quá {{ HN.lechCap }} cấp không được tính.</li>
        <li>Mang đủ thuốc hồi máu và Ether để không phải về thị trấn giữa chừng.</li>
        <li>Đánh Hầm ngục song song với nhiệm vụ ngày để tận dụng năng lượng huấn luyện.</li>
      </ul>
    </WikiSection>
  </WikiShell>
</template>
