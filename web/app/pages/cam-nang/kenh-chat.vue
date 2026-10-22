<script setup lang="ts">
/**
 * Kênh chat (0.9.8, client 908). Số liệu: shared/utils/heThong098.ts (sv.chat.* của cau_hinh_danh_muc; tên kênh
 * com.htbb.pokemon.KenhChatLogic.TEN_KENH) — đối chiếu sql/htbb_day_du.sql ở tests/he-thong-098.test.ts.
 */
import { CHAT } from '~~/shared/utils/heThong098'

const KENH = [
  { ten: 'Bản đồ', mo: `Người ở gần bạn trên cùng bản đồ. Từ cấp ${CHAT.capGan}.` },
  { ten: 'Thế giới', mo: `Cả máy chủ. Từ cấp ${CHAT.capTheGioi}, mỗi tin ${CHAT.xuTheGioi} xu, một tin mỗi ${CHAT.giayTheGioi} giây (có đếm ngược).` },
  { ten: 'Đội', mo: 'Thành viên Đội (hội) của bạn, có chức vụ cạnh tên. Đầu tab ghi tên Đội và số người đang trực tuyến; chưa vào Đội thì tab trống.' },
  { ten: 'Nhóm', mo: 'Nhóm đi săn đang lập (tối đa 5 người).' },
  { ten: 'Bạn bè', mo: 'Danh sách bạn (trực tuyến / ngoại tuyến) và hội thoại riêng hai phía: tin của bạn bên phải, của người kia bên trái.' },
  { ten: 'Hệ thống', mo: 'Thông báo của trò chơi (chỉ đọc).' },
]
</script>

<template>
  <WikiShell slug="kenh-chat">
    <WikiSection id="kenh" title="Các kênh" lead="Mở bảng chat bằng nút chat trên HUD, phím T hoặc “/” (bản PC). Mỗi kênh một tab, có chấm đỏ khi có tin chưa đọc.">
      <ul class="grid gap-3 sm:grid-cols-2">
        <li v-for="k in KENH" :key="k.ten" class="rounded-2xl glass p-4">
          <strong class="font-display text-highlighted">{{ k.ten }}</strong>
          <p class="mt-1 text-sm text-muted">
            {{ k.mo }}
          </p>
        </li>
      </ul>
    </WikiSection>

    <WikiSection id="tin-rieng" title="Tin riêng & chỉ trong phiên" :lead="`Chạm tên ai đó trong chat để mở menu: nhắn riêng, mời vào nhóm, kết bạn, xem thông tin, chặn. Nhắn riêng từ cấp ${CHAT.capRieng}.`">
      <figure class="my-4 overflow-hidden rounded-2xl ring-gilded">
        <img src="/anh-game/chat-1280.webp" srcset="/anh-game/chat-640.webp 640w, /anh-game/chat-1280.webp 1280w" sizes="(min-width: 768px) 720px, 100vw" alt="Hội thoại riêng hai phía trong bảng chat" loading="lazy" class="w-full">
      </figure>
      <ul class="cham">
        <li>Chat chỉ giữ trong lần chơi này (như Làng Lá): thoát game rồi vào lại thì mọi tab đều trống. Nút <strong>Xoá</strong> ở mỗi tab xoá ngay tin đang hiện.</li>
        <li>Người nhận không trực tuyến: tin <strong>không</strong> được gửi, bạn nhận thông báo “… không trực tuyến, tin không được gửi”.</li>
        <li>Danh sách bạn bè và danh sách chặn vẫn được lưu. Chặn tối đa {{ CHAT.chanToiDa }} người: không thấy tin và không nhận tin riêng của họ.</li>
        <li>Mỗi tin dài tối đa {{ CHAT.doDai }} ký tự.</li>
      </ul>
    </WikiSection>

    <WikiSection id="luat" title="Luật chat">
      <WikiCallout kieu="luu-y">
        Gửi dồn dập sẽ bị khoá chat một lúc (chống spam). Bị chặn vì cấp / giãn cách thì không trừ xu.
        Báo cáo lừa đảo cho ban quản trị qua kênh hỗ trợ.
      </WikiCallout>
    </WikiSection>
  </WikiShell>
</template>
