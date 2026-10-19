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

-- =====================================================================================================================
-- [khoa-khi-mac] Đồ đã mặc bị khoá (chủ game 0.9.6.1) + phá khoá ở Machamp thợ rèn (hố hút xu)
-- ---------------------------------------------------------------------------------------------------------------------
-- Luật (server, real.Item / real.PhaKhoaGame / com.htbb.pokemon.PhaKhoaLogic, test/KiemKhoaKhiMac):
--  * Mặc trang bị (nhân vật, trang bị thú, thần khí) lần đầu: lock = 1 vĩnh viễn + cờ "khoá do mặc" (daMac). Món đã
--    khoá vì lý do khác (mua bằng lượng khoá, quà nhiệm vụ / sự kiện, nguyên liệu khoá, tinh luyện) giữ daMac = 0.
--    Skin thú, trang phục (model) là trang trí: không khoá khi mặc.
--  * Món khoá không giao dịch / bày gian hàng / ký gửi / bỏ ra đất cho người khác nhặt (chặn sẵn theo lock = 1).
--    Món CHỈ khoá do mặc vẫn bán NPC được (trước khi mặc vốn bán được); món khoá vì lý do khác theo
--    chong_clone.ban_npc_do_khoa như cũ. Lượng khoá không có đường chuyển nào (test/KiemChongClone).
--  * Chuỗi lưu vật phẩm (getDBInfo, tob_char.equip / inven / bag / tuido): thêm 2 trường CUỐI - 34 = daMac,
--    35 = số lần phá khoá. Chuỗi cũ (34 trường) đọc ra 0 / 0.
--  * MIGRATION đồ đang mặc của nhân vật cũ: KHÔNG làm bằng SQL được an toàn - đồ mặc nằm trong cột chuỗi ghép
--    tob_char.equip ("dbInfo|thuộc tính>...", trường lock là trường thứ 15 của mỗi món). Server làm lúc NẠP nhân vật
--    (real.CharTrangBi.setItemWearing / CharNapDuLieu: Item.khoaKhiMac() cho món vào ô mặc), idempotent: món đã
--    khoá giữ nguyên, lần lưu kế tiếp ghi lock = 1 + daMac = 1 xuống CSDL.
--  * Thiết kế cân bằng 4.3 ghi "món đã mặc không bao giờ giao dịch lại"; chủ game ghi đè bằng PHÁ KHOÁ dưới đây.
--
-- Phá khoá (Machamp thợ rèn, mục "Phá khoá trang bị"; chọn món khoá do mặc trong hành trang, xác nhận hiện đúng giá):
--   xu = giờ thu nhập ở cấp món (ThongSoLauDai.xuMoiGio) × gio_phan_tram/100 × (100 + plus × cong_moi_cap_phan_tram)/100
--        × he_so_phan_tram(lần)/100, + so_da "Đá phá khoá" (item 1295). Xu bị đốt (không cộng cho ai), ghi
--   tob_log_use_luong "Pha khoa trang bi_xu". Tối đa = số dòng data_pha_khoa_lan (3); đếm theo món, hiện trong menu.
--   Ví dụ cấp 50 (33.925 xu/giờ), Quý +10: lần 1 = 135.700 xu + 1 đá, lần 2 = 271.400 + 2, lần 3 = 542.800 + 3.
--   Cấp 90 (196.101 xu/giờ), Sử thi +15: lần 1 = 1.470.757 xu + 1 đá (7,5 giờ thu nhập), lần 3 = 5.883.030 xu + 3 đá.
-- Đá phá khoá: chỉ từ hộp đột kích boss thế giới (data_noi_dung_roi nguon mo_hop: hộp vàng top 10 30%, hộp tham
-- gia 5%), vào túi ở dạng khoá; KHÔNG bán ở cửa hàng lượng / lượng khoá.
SET NAMES utf8;

INSERT INTO `data_item` (`id`, `name`, `type`, `style`, `he`, `gender`, `level`, `durable`, `atb0`, `atb1`, `atb2`, `atb3`, `atb4`, `atb5`, `atb6`, `atb7`, `atb8`, `atb9`, `price`, `clazz`, `xstart`, `ystart`, `colortype`, `nloan`, `idUpLevel`, `namein`, `ideff`, `pricecu`) VALUES
(1295,'Đá phá khoá',28,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,14135,0,-1,0,-1,'Đá phá khoá',-1,0)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `type` = VALUES(`type`), `namein` = VALUES(`namein`);

CREATE TABLE IF NOT EXISTS `data_pha_khoa_bac` (
  `color_name` TINYINT NOT NULL,
  `ten` VARCHAR(32) NOT NULL DEFAULT '',
  `gio_phan_tram` INT NOT NULL DEFAULT 100,
  `cong_moi_cap_phan_tram` INT NOT NULL DEFAULT 10,
  PRIMARY KEY (`color_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE IF NOT EXISTS `data_pha_khoa_lan` (
  `lan` TINYINT NOT NULL,
  `he_so_phan_tram` INT NOT NULL DEFAULT 100,
  `so_da` TINYINT NOT NULL DEFAULT 1,
  PRIMARY KEY (`lan`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
-- INSERT IGNORE: chạy lại không ghi đè số admin đã chỉnh. Khớp PhaKhoaLogic.BAC_MAC_DINH / LAN_MAC_DINH.
INSERT IGNORE INTO `data_pha_khoa_bac` (`color_name`, `ten`, `gio_phan_tram`, `cong_moi_cap_phan_tram`) VALUES
(0,'Thường',50,10),(1,'Hoàn mỹ (cũ, như Sử thi)',300,10),(2,'Đỏ (cũ, như Hiếm)',100,10),(3,'Quý',200,10),
(4,'Sử thi',300,10),(5,'Trang trí',50,10),(6,'Huyền thoại',500,10),(7,'Hiếm',100,10);
INSERT IGNORE INTO `data_pha_khoa_lan` (`lan`, `he_so_phan_tram`, `so_da`) VALUES (1,100,1),(2,200,2),(3,400,3);

-- Nguồn Đá phá khoá. stt 9501.. tránh dải seed. Chạy lại sql/pokemon_noi_dung.sql (DROP bảng) thì chạy lại khối này.
INSERT IGNORE INTO `data_noi_dung_roi` (`stt`,`nguon`,`khoa_nguon`,`vat_pham`,`ten`,`ppm`,`sl_min`,`sl_max`,`do_hiem`,`khoa`,`het_han`) VALUES
(9501,'mo_hop','item:1051','item:1295','Đá phá khoá',300000,1,1,'',1,0),
(9502,'mo_hop','item:1050','item:1295','Đá phá khoá',50000,1,1,'',1,0);
-- [/khoa-khi-mac] =====================================================================================================
