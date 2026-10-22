-- =====================================================================================================
-- KÊNH CHAT (0.9.8): bảng chat nhiều kênh (Bản đồ, Thế giới, Bang hội, Đội, Bạn bè / tin riêng, Hệ thống).
-- Nâng cấp IDEMPOTENT (MariaDB 10.3, utf8): chạy lại bao nhiêu lần cũng được; tự chứa. Máy chủ game cũng tự tạo hai
-- bảng này lúc khởi động (real.KenhChatGame.taoBang) - chạy tệp này chỉ để dựng sẵn trên CSDL mới / web đọc được.
--
--   tob_chat_cho    hộp thư tin riêng OFFLINE: tin gửi cho người đang offline, giao một lần khi họ vào game rồi xoá
--                   (trước 0.9.8 tin riêng cho người offline bị MẤT). id_nhan = tob_char.id (đổi tên không mất tin);
--                   luc = mili giây (System.currentTimeMillis). Tối đa sv.chat.thuChoToiDa tin / người, giữ
--                   sv.chat.thuChoNgay ngày (quá hạn xoá lúc khởi động).
--   tob_chat_chan   danh sách chặn: id_char chặn tên ten_chan (chữ thường). Người bị chặn không nhắn riêng được, tin
--                   thế giới / bang hội / đội của họ không tới người chặn.
--
-- Khoá cấu hình sv.chat.* (lớp co_dinh, không tăng số khoá trên web) nằm trong sql/pokemon_cau_hinh_trung_tam.sql
-- (sinh từ tools/cauhinh/danh_muc.json).
-- =====================================================================================================

CREATE TABLE IF NOT EXISTS `tob_chat_cho` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `id_nhan` INT NOT NULL,
  `ten_gui` VARCHAR(45) NOT NULL,
  `noi_dung` VARCHAR(400) NOT NULL,
  `luc` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_chat_cho_nhan` (`id_nhan`)
) DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `tob_chat_chan` (
  `id_char` INT NOT NULL,
  `ten_chan` VARCHAR(45) NOT NULL,
  `luc` DATETIME NOT NULL,
  PRIMARY KEY (`id_char`, `ten_chan`)
) DEFAULT CHARSET=utf8;
