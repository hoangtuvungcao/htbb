-- THÀNH VIÊN HUYỀN THOẠI BẢO BỐI (docs/van-hanh.md). Thành viên theo TÀI KHOẢN, chỉ để mở các
-- tính năng admin chọn khoá (chống tài khoản phụ / clone) — không cho sức mạnh, không cho tiền tệ.
-- Chạy SAU du_lieu/sql_nguon/pokemon_nap_tien.sql (dùng chung nap_goi / nap_don / sổ cái / SePay). Chạy lại an toàn.
-- MẶC ĐỊNH TẮT: thanh_vien.bat=0 (không khoá gì), thanh_vien.ban_bat=0 (không bán).
-- SQL portable (MariaDB + NovaDB): mốc thời gian do ứng dụng ghi, hạn lưu millis BIGINT.
SET NAMES utf8;

-- Trạng thái thành viên của tài khoản. het_han_ms: 0 = vĩnh viễn, < 0 = đã thu hồi, > 0 = hạn (millis).
CREATE TABLE IF NOT EXISTS `tv_thanh_vien` (
  `user_id` int NOT NULL COMMENT 'account.team_user.id',
  `username` varchar(50) NOT NULL DEFAULT '',
  `het_han_ms` bigint NOT NULL DEFAULT 0,
  `nguon` varchar(16) NOT NULL DEFAULT '' COMMENT 'mua | admin | hang_loat',
  `so_lan` int NOT NULL DEFAULT 0 COMMENT 'số lần được cấp / gia hạn',
  `cap_luc` datetime DEFAULT NULL,
  `cap_nhat_luc` datetime DEFAULT NULL,
  `ghi_chu` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`user_id`),
  KEY `idx_tv_username` (`username`)
) DEFAULT CHARSET=utf8;

-- Nhật ký kiểm toán: mọi lần cấp / gia hạn / thu hồi (mua, hoàn tiền, admin, hàng loạt).
CREATE TABLE IF NOT EXISTS `tv_nhat_ky` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `username` varchar(50) NOT NULL DEFAULT '',
  `hanh_dong` varchar(20) NOT NULL COMMENT 'cap | gia_han | thu_hoi | hoan_tien',
  `ngay` int NOT NULL DEFAULT 0 COMMENT 'số ngày cấp (0 = vĩnh viễn)',
  `het_han_cu` bigint NOT NULL DEFAULT 0,
  `het_han_moi` bigint NOT NULL DEFAULT 0,
  `ma_don` varchar(16) NOT NULL DEFAULT '',
  `tac_nhan` varchar(60) NOT NULL DEFAULT '' COMMENT 'web:mua | admin:<tên> | webhook:<ncc>',
  `ly_do` varchar(255) NOT NULL DEFAULT '',
  `ip` varchar(64) NOT NULL DEFAULT '',
  `tao_luc` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tv_nk_user` (`user_id`),
  KEY `idx_tv_nk_don` (`ma_don`)
) DEFAULT CHARSET=utf8;

-- Sản phẩm trong danh mục nạp (loại thanh_vien, không kèm lượng). Giá / tên / đang bán được trang
-- /admin/thanh-vien đồng bộ từ cau_hinh (thanh_vien.gia, thanh_vien.ban_bat).
INSERT IGNORE INTO `nap_goi` (`ma`,`ten`,`loai`,`gia_vnd`,`luong`,`mo_ta`,`dang_ban`,`thu_tu`) VALUES
('THANH_VIEN','Thành viên Huyền Thoại Bảo Bối','thanh_vien',20000,0,'Mở các tính năng cộng đồng (giao dịch, chat thế giới, chợ, đội...) cho MỌI Pokémon của tài khoản. Không tăng sức mạnh.',0,0);

-- Cấu hình (server game đọc lại mỗi 10 giây; trang /admin/thanh-vien sửa). MẶC ĐỊNH TẮT.
INSERT IGNORE INTO `cau_hinh` (`khoa`,`gia_tri`,`sua_boi`) VALUES
('thanh_vien.bat','0','sql'),
('thanh_vien.ban_bat','0','sql'),
('thanh_vien.gia','20000','sql'),
('thanh_vien.thoi_han_ngay','0','sql'),
('thanh_vien.chuc_nang','giao_dich,nhan_tang,chat_the_gioi,cho,cho_sinh_thai,chuyen_luong,bang,pvp_xep_hang,giftcode','sql'),
('thanh_vien.mien_cap','0','sql'),
('thanh_vien.thong_bao','','sql');
