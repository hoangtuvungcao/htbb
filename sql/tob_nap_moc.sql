-- Bảng theo dõi "mốc nạp tân thủ" (49k / 99k / 299k).
-- Import bảng này vào database kpah_db (cùng DB với tob_char, board_created...).
--
-- Luồng hoạt động:
--   1. Web (naptien.php) tạo 1 dòng trạng thái 'cho_thanh_toan' kèm mã đơn (ma_don) duy nhất,
--      hiển thị mã QR VietQR với nội dung chuyển khoản = ma_don.
--   2. Khi SePay xác nhận có tiền vào khớp ma_don (webhook_sepay.php), hoặc admin xác nhận
--      thủ công (admin_naptien.php) -> chuyển trạng thái 'da_thanh_toan'.
--   3. Người chơi vào game, gặp NPC Tổng Quản, chọn "Nhận mốc nạp" -> server Java kiểm tra
--      có dòng 'da_thanh_toan' cho đúng userID + mốc đó không, nếu có thì trao quà và
--      chuyển trạng thái 'da_nhan' (không thể nhận lại).

CREATE TABLE IF NOT EXISTS `tob_nap_moc` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `userID` int(11) NOT NULL COMMENT 'team_user.id / player.userID',
  `moc` tinyint(3) unsigned NOT NULL COMMENT '1 = 49k, 2 = 99k, 3 = 299k',
  `amount` int(10) unsigned NOT NULL COMMENT 'so tien VND',
  `ma_don` varchar(20) NOT NULL COMMENT 'noi dung chuyen khoan, dung de doi chieu voi SePay',
  `phuong_hoang` tinyint(4) NOT NULL DEFAULT -1 COMMENT 'moc 3 only: 0=Phuong Hoang, 1=Phuong Hoang Bang, -1=chua chon',
  `trang_thai` enum('cho_thanh_toan','da_thanh_toan','da_nhan') NOT NULL DEFAULT 'cho_thanh_toan',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `paid_at` datetime NULL DEFAULT NULL,
  `claimed_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_ma_don` (`ma_don`),
  KEY `idx_user_moc` (`userID`, `moc`, `trang_thai`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
