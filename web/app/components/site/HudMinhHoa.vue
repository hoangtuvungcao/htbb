<script setup lang="ts">
/**
 * Giao diện trong game (HUD) — ẢNH CHỤP THẬT bản PC 1920×1080 (public/anh-game/chien-dau-*.webp) có chấm số.
 * Toạ độ chấm tính theo % của ảnh gốc; chụp lại ảnh thì chỉnh CHAM cho khớp.
 */
interface ChamHud { so: number, ten: string, mo: string, x: number, y: number }
const CHAM: ChamHud[] = [
  { so: 1, ten: 'Thẻ trạng thái', mo: 'Chân dung, cấp, hệ; thanh HP (xanh lá), PP để dùng chiêu (xanh dương) và EXP (vàng). Hết HP là gục ngã.', x: 13, y: 6 },
  { so: 2, ten: 'Trạng thái tự động', mo: 'Đang bật Tự động đánh: còn bao nhiêu phút trong ngày và đang làm gì.', x: 8, y: 16.5 },
  { so: 3, ten: 'Mục tiêu', mo: 'Tên, cấp và HP của Pokémon đang nhắm. Chạm / bấm chuột vào quái hoặc người chơi khác để chọn.', x: 50, y: 5 },
  { so: 4, ten: 'Số sát thương & khắc hệ', mo: '"Siêu hiệu quả!" = đánh đúng hệ; "Không hiệu quả lắm" = nên đổi chiêu.', x: 47, y: 31 },
  { so: 5, ten: 'Nút nhanh', mo: 'Hành trang, Nhiệm vụ và Bản đồ thế giới.', x: 92.7, y: 4 },
  { so: 6, ten: 'Bản đồ nhỏ', mo: 'Vị trí của bạn, NPC và cổng sang bản đồ khác. Camera cố định: ai cũng thấy cùng một khung cảnh.', x: 94.5, y: 21 },
  { so: 7, ten: 'AUTO & cài đặt', mo: 'Viên AUTO bật / tắt Tự động đánh; bánh răng bên cạnh mở bảng cài đặt (chiêu, ngưỡng thuốc HP / PP, nhặt đồ, vùng).', x: 95.8, y: 63 },
  { so: 8, ten: 'Cụm chiêu', mo: 'Nút lớn là đánh thường, các nút màu hệ là chiêu (icon đúng chiêu). Giữ để xem tên, hệ, PP, thời gian hồi.', x: 88, y: 84 },
  { so: 9, ten: 'Thuốc HP / PP', mo: 'Hai nút uống nhanh thuốc hồi máu và Ether (PP); số nhỏ là số lượng còn lại.', x: 78, y: 88 },
  { so: 10, ten: 'Menu (Poké Ball)', mo: 'Mở vòng menu: Pokémon, Pokédex, Cửa hàng, Đội/Bạn bè, Sự kiện, Thư/Quà (nhập giftcode), Tự động đánh, Cài đặt…', x: 50, y: 94 },
  { so: 11, ten: 'Chat', mo: 'Kênh khu vực, đội, thế giới. PC: phím T; Java: phím # → Chat; Android: chạm bong bóng chat.', x: 2.5, y: 95.5 },
]
</script>

<template>
  <figure>
    <div class="relative overflow-hidden rounded-3xl shadow-[0_0_0_3px_var(--poke-card),0_0_0_5px_color-mix(in_oklab,var(--poke-outline)_60%,transparent),0_18px_36px_-20px_rgb(10_26_51/.5)]">
      <SiteAnhGame ma="chien-dau" sizes="(min-width: 1280px) 960px, 100vw" alt="Ảnh chụp giao diện trong game khi đang đánh Pokémon hoang dã" />
      <span
        v-for="c in CHAM"
        :key="c.so"
        class="absolute z-10 grid size-5 -translate-x-1/2 -translate-y-1/2 place-items-center rounded-full border-2 border-white bg-pokered-600 font-display text-[10px] font-extrabold text-white shadow-md sm:size-7 sm:text-xs"
        :style="{ left: `${c.x}%`, top: `${c.y}%` }"
        aria-hidden="true"
      >{{ c.so }}</span>
    </div>
    <figcaption class="mt-2 text-xs text-dimmed">
      Ảnh chụp trong game, bản PC. Bản Android cùng bố cục; bản Java (J2ME) gọn hơn, dùng phím số.
    </figcaption>
    <ol class="mt-4 grid gap-2 sm:grid-cols-2">
      <li v-for="c in CHAM" :key="c.so" class="flex gap-3 rounded-2xl bg-elevated/60 p-3 text-sm">
        <span class="grid size-6 shrink-0 place-items-center rounded-full bg-pokered-600 font-display text-xs font-extrabold text-white">{{ c.so }}</span>
        <span><strong class="text-highlighted">{{ c.ten }}</strong><span class="block text-muted">{{ c.mo }}</span></span>
      </li>
    </ol>
  </figure>
</template>
