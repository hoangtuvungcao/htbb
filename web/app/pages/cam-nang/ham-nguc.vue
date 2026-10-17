<script setup lang="ts">
/** Hầm Ngục Bí Ẩn — các chặng từ nhiem_vu_moi.csv (loại hang_bi_an). */
const HM = CAM_NANG.nhiemVu.hamNguc
const suKienHn = CAM_NANG.suKien.filter(s => /hầm ngục/i.test(`${s.ten1} ${s.coChe}`))
</script>

<template>
  <WikiShell slug="ham-nguc">
    <WikiCallout kieu="sap-ra-mat">
      Hầm Ngục Bí Ẩn là chế độ mới đang được phát triển. Nội dung dưới đây là thiết kế hiện tại và có thể thay đổi khi ra mắt.
    </WikiCallout>

    <WikiSection id="gioi-thieu" title="Hầm ngục là gì">
      <p class="van">
        Lấy cảm hứng từ dòng game Pokémon Mystery Dungeon: mỗi tầng là một bản đồ <strong>sinh ngẫu nhiên</strong>, có quái, bẫy và lối xuống tầng dưới.
        Mỗi ngày dùng chung một <strong>hạt giống</strong> cho cả máy chủ — mọi người gặp cùng một bố cục, nên có thể so tài trên bảng điểm ngày.
      </p>
      <ul class="cham mt-4">
        <li>Vào qua NPC Snorlax ở các hang động.</li>
        <li>Mỗi chặng tính là một nhiệm vụ ngày (làm 1 lần/ngày).</li>
        <li>Luật chi tiết (gục ngã, bảng điểm ngày) sẽ công bố khi chế độ ra mắt.</li>
      </ul>
    </WikiSection>

    <WikiSection id="tang" title="Các chặng">
      <ol class="grid gap-3 sm:grid-cols-2">
        <li v-for="(q, i) in HM" :key="i" class="flex items-center gap-4 rounded-2xl glass p-4">
          <span class="grid size-12 shrink-0 place-items-center rounded-xl bg-navy-800 font-pixel text-xs text-pikachu-300">{{ i + 1 }}</span>
          <span>
            <strong class="block font-display text-highlighted">{{ q.ten.replace(/^Hầm ngục bí ẩn:\s*/i, '') }}</strong>
            <span class="text-sm text-muted">Từ cấp {{ q.cap }} · vượt {{ q.soLuong }} {{ q.mucTieu }}</span>
          </span>
        </li>
      </ol>
      <p v-for="s in suKienHn" :key="s.tuan" class="mt-4 text-sm text-muted">
        <UIcon name="i-lucide-calendar" class="mr-1 inline size-4 align-[-3px]" /> Tuần {{ s.tuan }}: <strong class="text-highlighted">{{ s.ten1 }}</strong> — {{ s.coChe }}.
      </p>
    </WikiSection>

    <WikiSection id="meo" title="Mẹo">
      <ul class="cham">
        <li>Mang đủ thuốc hồi máu và Ether trước khi xuống tầng.</li>
        <li>Đi đội để chia nhau dọn quái — tầng sâu có quái tinh anh.</li>
        <li>Hạt giống giống nhau cả ngày: hỏi bạn bè đã qua về lối xuống tầng.</li>
      </ul>
    </WikiSection>
  </WikiShell>
</template>
