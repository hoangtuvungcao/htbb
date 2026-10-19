-- 0.9.6.1: sửa logic server bằng dữ liệu. Chạy được nhiều lần (idempotent). Mỗi phần một khối riêng, nối thêm ở cuối.
-- mysql -u root htbb_db < sql/sua_096_1_logic.sql

-- =====================================================================================================================
-- [giap-vo-han] Chiêu hỗ trợ / phòng thủ: hồi chiêu luôn dài hơn thời lượng hiệu ứng (chủ game 0.9.6.1)
-- ---------------------------------------------------------------------------------------------------------------------
-- Lỗi gốc nằm ở server (real/CharCapNhat.setBuff2Char chia đôi hồi chiêu, thời lượng lấy bảng cũ 50..210 giây) và đã
-- sửa ở real/ChieuHoTro: hồi chiêu hiệu quả = max(dữ liệu, thời lượng + max(5 giây, 25% thời lượng)). Khối này là SÀN dữ
-- liệu cho CSDL đã sửa tay (web quản trị / bản cũ): chỉ NÂNG giá trị thấp hơn sàn, không hạ giá trị nào, nên chạy trên
-- htbb_day_du.sql hiện tại không đổi gì (số liệu seed đã đúng thiết kế). test/KiemGiapVoHan kiểm mỗi sàn
-- >= thời lượng thiết kế + biên và kiểm cả bảng chiêu trong htbb_day_du.sql / pokemon_noi_dung.sql / du_lieu.
--
-- dòng, ô  chiêu                 thời lượng   hồi chiêu   PP   PP%
-- 0, 4     Màn khói              10 s         30 s        10   8
-- 0, 5     Vũ điệu kiếm          30 s         60 s        10   8
-- 1, 4     Tự vệ (giáp)           8 s         40 s        10   8
-- 1, 5     Sinh trưởng           30 s         60 s        10   8
-- 2, 4     Mong ước              10 s         30 s        10   8
-- 2, 5     Nghỉ ngơi              2 s         90 s         5   12
-- 2, 6     Phước lành hồi sinh    -          120 s         3   15
-- 2, 7     Chia sẻ nỗi đau       10 s         60 s         5   12   (khiên PP: hút sát thương bằng PP)
-- 3, 4     Phá mai               20 s         45 s        10   8
-- 3, 5     Rút vào mai (khiên)   10 s         40 s        10   8
-- 4, 4     Nhanh nhẹn            10 s         40 s        10   8
-- 4, 5     Nạp điện              10 s         30 s        10   8
UPDATE `data_noi_dung_chieu` SET `hoi_chieu_giay` = GREATEST(`hoi_chieu_giay`, 30),  `pp` = GREATEST(`pp`, 10), `mp_phan_tram` = GREATEST(`mp_phan_tram`, 8)  WHERE `dong` = 0 AND `so_hieu` = 4;
UPDATE `data_noi_dung_chieu` SET `hoi_chieu_giay` = GREATEST(`hoi_chieu_giay`, 60),  `pp` = GREATEST(`pp`, 10), `mp_phan_tram` = GREATEST(`mp_phan_tram`, 8)  WHERE `dong` = 0 AND `so_hieu` = 5;
UPDATE `data_noi_dung_chieu` SET `hoi_chieu_giay` = GREATEST(`hoi_chieu_giay`, 40),  `pp` = GREATEST(`pp`, 10), `mp_phan_tram` = GREATEST(`mp_phan_tram`, 8)  WHERE `dong` = 1 AND `so_hieu` = 4;
UPDATE `data_noi_dung_chieu` SET `hoi_chieu_giay` = GREATEST(`hoi_chieu_giay`, 60),  `pp` = GREATEST(`pp`, 10), `mp_phan_tram` = GREATEST(`mp_phan_tram`, 8)  WHERE `dong` = 1 AND `so_hieu` = 5;
UPDATE `data_noi_dung_chieu` SET `hoi_chieu_giay` = GREATEST(`hoi_chieu_giay`, 30),  `pp` = GREATEST(`pp`, 10), `mp_phan_tram` = GREATEST(`mp_phan_tram`, 8)  WHERE `dong` = 2 AND `so_hieu` = 4;
UPDATE `data_noi_dung_chieu` SET `hoi_chieu_giay` = GREATEST(`hoi_chieu_giay`, 90),  `pp` = GREATEST(`pp`, 5),  `mp_phan_tram` = GREATEST(`mp_phan_tram`, 12) WHERE `dong` = 2 AND `so_hieu` = 5;
UPDATE `data_noi_dung_chieu` SET `hoi_chieu_giay` = GREATEST(`hoi_chieu_giay`, 120), `pp` = GREATEST(`pp`, 3),  `mp_phan_tram` = GREATEST(`mp_phan_tram`, 15) WHERE `dong` = 2 AND `so_hieu` = 6;
UPDATE `data_noi_dung_chieu` SET `hoi_chieu_giay` = GREATEST(`hoi_chieu_giay`, 60),  `pp` = GREATEST(`pp`, 5),  `mp_phan_tram` = GREATEST(`mp_phan_tram`, 12) WHERE `dong` = 2 AND `so_hieu` = 7;
UPDATE `data_noi_dung_chieu` SET `hoi_chieu_giay` = GREATEST(`hoi_chieu_giay`, 45),  `pp` = GREATEST(`pp`, 10), `mp_phan_tram` = GREATEST(`mp_phan_tram`, 8)  WHERE `dong` = 3 AND `so_hieu` = 4;
UPDATE `data_noi_dung_chieu` SET `hoi_chieu_giay` = GREATEST(`hoi_chieu_giay`, 40),  `pp` = GREATEST(`pp`, 10), `mp_phan_tram` = GREATEST(`mp_phan_tram`, 8)  WHERE `dong` = 3 AND `so_hieu` = 5;
UPDATE `data_noi_dung_chieu` SET `hoi_chieu_giay` = GREATEST(`hoi_chieu_giay`, 40),  `pp` = GREATEST(`pp`, 10), `mp_phan_tram` = GREATEST(`mp_phan_tram`, 8)  WHERE `dong` = 4 AND `so_hieu` = 4;
UPDATE `data_noi_dung_chieu` SET `hoi_chieu_giay` = GREATEST(`hoi_chieu_giay`, 30),  `pp` = GREATEST(`pp`, 10), `mp_phan_tram` = GREATEST(`mp_phan_tram`, 8)  WHERE `dong` = 4 AND `so_hieu` = 5;
-- Sàn chung: không chiêu nào hồi dưới 0,5 giây (đánh thường 0,7); chiêu không phải đánh thường phải tốn PP.
UPDATE `data_noi_dung_chieu` SET `hoi_chieu_giay` = 0.5 WHERE `hoi_chieu_giay` < 0.5;
UPDATE `data_noi_dung_chieu` SET `pp` = 1 WHERE `kieu` <> 'thuong' AND `pp` < 1;
UPDATE `data_noi_dung_chieu` SET `mp_phan_tram` = 1 WHERE `kieu` <> 'thuong' AND `mp_phan_tram` < 1;
-- [/giap-vo-han] =====================================================================================================
