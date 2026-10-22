-- =====================================================================================================================
-- HTBB 0.9.8 - THU PHỤC (bậc hiếm) + CÁ THỂ + CHUYỂN ĐỔI + TRAO ĐỔI POKÉMON (docs/thiet-ke-game.md 7.7, 7.8)
--   Chạy SAU sql/them_bo_ngoc_che_tao_bat.sql. Idempotent (chạy lại bao nhiêu lần cũng được), MariaDB 10.3, utf8:
--      mysql -u root htbb_db < sql/them_trade_pokemon.sql
--   Máy chủ tự tạo bảng / cột thiếu (DatabaseBoNgoc.taoBang), thiếu dữ liệu thì dùng mặc định trong mã.
--
--   data_bat_loai           + cột tien_hoa_trao_doi (Haunter -> Gengar, Dusclops -> Dusknoir khi được trao đổi);
--                           bậc hiếm 4 mức: 0 Thường 55% / 1 Ít gặp 25% / 2 Hiếm 9% / 3 Rất hiếm 3% (tỉ lệ gốc,
--                           ti_le_phan_nghin = 0 = theo bậc). Huyền thoại / boss: không có dòng (không bắt được).
--   data_bat_chi_so_loai    chỉ số gốc loài (HP, Tấn công, Phòng thủ, Tấn công ĐB, Phòng thủ ĐB, Tốc độ) theo game
--                           chính bản - tính "Sức mạnh" con trong Hộp PC cùng chỉ số cá thể (IV 0..31) + tính cách.
--                           KHÔNG cộng vào chỉ số người chơi.
--   data_bat_chuyen_doi     thưởng Chuyển đổi con trùng loài theo bậc hiếm: Kẹo + xu nhỏ (phần nghìn giờ thu nhập)
--                           / lượng khoá; trần ngày ở bat.chuyen_doi.*; con nhận qua trao đổi chỉ ra Kẹo.
--   tob_log_trao_doi_pokemon nhật ký mọi lần trao đổi (ghi CÙNG giao dịch với hai Hộp PC).
--   Khoá cấu hình (tools/cauhinh/danh_muc.json): bat.giu_moi_loai, bat.chuyen_doi.*, trao_doi.*.
-- =====================================================================================================================

SET NAMES utf8;

ALTER TABLE `data_bat_loai` ADD COLUMN IF NOT EXISTS `tien_hoa_trao_doi` TINYINT NOT NULL DEFAULT 0;

-- Tiến hoá khi trao đổi như game chính bản (vẫn tiến hoá được bằng Kẹo ở Hộp PC).
UPDATE `data_bat_loai` SET `tien_hoa_trao_doi` = 1 WHERE `dex` IN (93, 356);

-- Bậc hiếm: loài hiếm giữ GIÁ TRỊ (tỉ lệ thấp). Rất hiếm = gần huyền thoại (Bagon, Beldum: Metang / Metagross, Ursaluna).
UPDATE `data_bat_loai` SET `do_hiem` = 3 WHERE `dex` IN (371, 372, 373, 375, 376, 901);
UPDATE `data_bat_loai` SET `do_hiem` = 2 WHERE `dex` IN (123, 214, 302, 306, 330, 335, 359, 637, 781);
UPDATE `data_bat_loai` SET `do_hiem` = 1 WHERE `dex` IN (127, 128, 334, 344, 346, 348, 357, 362, 365, 709);

CREATE TABLE IF NOT EXISTS `data_bat_chi_so_loai` (
  `dex` SMALLINT NOT NULL,
  `hp` SMALLINT NOT NULL DEFAULT 60,
  `tan_cong` SMALLINT NOT NULL DEFAULT 60,
  `phong_thu` SMALLINT NOT NULL DEFAULT 60,
  `tan_cong_db` SMALLINT NOT NULL DEFAULT 60,
  `phong_thu_db` SMALLINT NOT NULL DEFAULT 60,
  `toc_do` SMALLINT NOT NULL DEFAULT 60,
  PRIMARY KEY (`dex`)
) DEFAULT CHARSET=utf8;
INSERT INTO `data_bat_chi_so_loai` (`dex`, `hp`, `tan_cong`, `phong_thu`, `tan_cong_db`, `phong_thu_db`, `toc_do`) VALUES
(10,45,30,35,20,20,45),
(12,60,45,50,90,80,70),
(13,40,35,30,20,20,50),
(19,30,56,35,25,35,72),
(23,35,60,44,40,54,55),
(24,60,95,69,65,79,80),
(27,50,75,85,20,30,40),
(37,38,41,40,50,65,65),
(43,45,50,55,75,65,30),
(44,60,65,70,85,75,40),
(45,75,80,85,110,90,50),
(46,35,70,55,45,55,25),
(47,60,95,80,60,80,30),
(48,60,55,50,40,55,45),
(58,55,70,45,70,50,60),
(59,90,110,80,100,80,95),
(69,50,75,35,70,30,40),
(72,40,40,35,50,100,70),
(76,80,120,130,55,65,45),
(83,52,90,55,58,62,60),
(85,60,110,70,60,60,110),
(92,30,35,30,100,35,80),
(93,45,50,45,115,55,95),
(94,60,65,60,130,75,110),
(99,55,130,115,50,50,75),
(104,50,50,95,40,50,35),
(105,60,80,110,50,80,45),
(112,105,130,120,45,45,40),
(123,70,110,80,55,80,105),
(127,65,125,100,55,70,85),
(128,75,100,95,40,70,110),
(159,65,80,80,59,63,58),
(160,85,105,100,79,83,78),
(168,70,90,70,60,70,40),
(194,55,45,45,25,25,15),
(195,95,85,85,65,65,35),
(214,80,125,75,40,95,85),
(219,60,50,120,90,80,30),
(255,45,60,40,70,50,45),
(256,60,85,60,85,60,55),
(257,80,120,70,110,70,80),
(261,35,55,35,30,30,35),
(262,70,90,70,60,60,70),
(269,60,50,70,50,90,65),
(279,60,50,100,95,70,65),
(286,60,130,80,60,60,70),
(288,80,80,80,55,55,90),
(297,144,120,60,40,60,50),
(302,50,75,75,65,65,50),
(304,50,70,100,40,40,30),
(305,60,90,140,50,50,40),
(306,70,110,180,60,60,50),
(309,40,45,40,65,40,65),
(310,70,75,60,105,60,105),
(318,45,90,20,65,20,65),
(319,70,120,40,95,40,95),
(321,170,90,45,90,45,60),
(322,60,60,40,65,45,35),
(323,70,100,70,105,75,40),
(324,70,85,140,85,70,20),
(326,80,45,65,90,110,80),
(330,80,100,80,80,80,100),
(333,45,40,60,40,75,50),
(334,75,70,90,70,105,80),
(335,73,115,60,60,60,90),
(336,73,100,60,100,60,65),
(337,90,55,65,95,85,70),
(340,110,78,73,76,71,60),
(342,63,120,85,90,55,55),
(343,40,40,55,40,70,55),
(344,60,70,105,70,120,75),
(346,86,81,97,81,107,43),
(348,75,125,100,70,80,45),
(353,44,75,35,63,33,45),
(354,64,115,65,83,63,65),
(355,20,40,90,30,90,25),
(356,40,70,130,60,130,25),
(357,99,68,83,72,87,51),
(359,65,130,60,75,60,75),
(361,50,50,50,50,50,50),
(362,80,80,80,80,80,80),
(363,70,40,50,55,50,25),
(365,110,80,90,95,90,65),
(371,45,75,60,40,30,50),
(372,65,95,100,60,50,50),
(373,95,135,80,110,80,100),
(375,60,75,100,55,80,50),
(376,80,135,130,95,90,70),
(401,37,25,41,25,41,25),
(402,77,85,51,55,51,65),
(404,60,85,49,60,49,60),
(405,80,120,79,95,79,70),
(423,111,83,68,92,82,39),
(451,40,50,90,30,55,65),
(453,48,61,40,61,40,50),
(454,83,106,65,86,65,85),
(477,45,100,135,65,135,45),
(498,65,63,45,45,45,45),
(510,64,88,50,88,50,106),
(536,75,65,55,65,55,69),
(537,105,95,75,85,75,74),
(543,30,45,59,30,39,57),
(544,40,55,99,40,79,47),
(545,60,100,89,55,69,112),
(552,60,82,45,45,45,74),
(559,50,75,70,35,70,48),
(560,65,90,115,45,115,58),
(562,38,30,85,55,65,30),
(596,70,77,60,97,60,108),
(607,50,30,55,65,55,20),
(637,85,60,65,135,105,100),
(667,62,50,58,73,54,72),
(709,85,110,76,65,82,56),
(739,47,82,57,42,47,63),
(750,100,125,100,55,85,35),
(781,70,131,100,86,90,40),
(901,130,140,105,45,80,50)
ON DUPLICATE KEY UPDATE `hp` = VALUES(`hp`), `tan_cong` = VALUES(`tan_cong`), `phong_thu` = VALUES(`phong_thu`),
  `tan_cong_db` = VALUES(`tan_cong_db`), `phong_thu_db` = VALUES(`phong_thu_db`), `toc_do` = VALUES(`toc_do`);

CREATE TABLE IF NOT EXISTS `data_bat_chuyen_doi` (
  `do_hiem` TINYINT NOT NULL,
  `ten` VARCHAR(32) NOT NULL DEFAULT '',
  `keo` SMALLINT NOT NULL DEFAULT 1,
  `xu_phan_nghin_gio` SMALLINT NOT NULL DEFAULT 0,
  `luong_khoa` SMALLINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`do_hiem`)
) DEFAULT CHARSET=utf8;
-- Ngân sách: mỗi con bắt được tốn >= 1 bóng (2..12% giờ thu nhập) nên xu chuyển đổi (1 / 3 / 10%) luôn nhỏ hơn xu bỏ ra;
-- trần ngày 30% giờ thu nhập + 1 lượng khoá (bat.chuyen_doi.*). Sắc màu x2 Kẹo, không bao giờ tự chuyển đổi.
INSERT INTO `data_bat_chuyen_doi` (`do_hiem`, `ten`, `keo`, `xu_phan_nghin_gio`, `luong_khoa`) VALUES
(0, 'Thường', 2, 10, 0),
(1, 'Ít gặp', 3, 30, 0),
(2, 'Hiếm', 5, 100, 0),
(3, 'Rất hiếm', 8, 0, 1)
ON DUPLICATE KEY UPDATE `ten` = VALUES(`ten`), `keo` = VALUES(`keo`), `xu_phan_nghin_gio` = VALUES(`xu_phan_nghin_gio`),
  `luong_khoa` = VALUES(`luong_khoa`);

CREATE TABLE IF NOT EXISTS `tob_log_trao_doi_pokemon` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `luc` DATETIME NOT NULL,
  `id_a` INT NOT NULL,
  `ten_a` VARCHAR(64) NOT NULL DEFAULT '',
  `id_b` INT NOT NULL,
  `ten_b` VARCHAR(64) NOT NULL DEFAULT '',
  `a_dua` VARCHAR(512) NOT NULL DEFAULT '',
  `b_dua` VARCHAR(512) NOT NULL DEFAULT '',
  `phi_a` BIGINT NOT NULL DEFAULT 0,
  `phi_b` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `id_a` (`id_a`, `luc`),
  KEY `id_b` (`id_b`, `luc`)
) DEFAULT CHARSET=utf8;

-- ------------------------------------------------------------------------------------------- chợ Pokémon (mục 7.9)
-- Khung giá theo bậc hiếm (phần nghìn giờ thu nhập Ở CẤP CỦA CON POKÉMON); sắc màu x3, 3 sao x2 ở cả sàn lẫn trần
-- (cho_pokemon.he_so_sac_mau / he_so_3_sao). Ngoài khung: máy chủ từ chối.
CREATE TABLE IF NOT EXISTS `data_cho_pokemon_gia` (
  `do_hiem` TINYINT NOT NULL,
  `ten` VARCHAR(32) NOT NULL DEFAULT '',
  `san_phan_nghin_gio` INT NOT NULL DEFAULT 100,
  `tran_phan_nghin_gio` INT NOT NULL DEFAULT 1000,
  PRIMARY KEY (`do_hiem`)
) DEFAULT CHARSET=utf8;
INSERT INTO `data_cho_pokemon_gia` (`do_hiem`, `ten`, `san_phan_nghin_gio`, `tran_phan_nghin_gio`) VALUES
(0, 'Thường', 100, 1000),
(1, 'Ít gặp', 300, 3000),
(2, 'Hiếm', 1000, 10000),
(3, 'Rất hiếm', 3000, 30000)
ON DUPLICATE KEY UPDATE `ten` = VALUES(`ten`), `san_phan_nghin_gio` = VALUES(`san_phan_nghin_gio`),
  `tran_phan_nghin_gio` = VALUES(`tran_phan_nghin_gio`);

-- Tin bán + KÝ QUỸ: con Pokémon nằm ở du_lieu (không còn trong Hộp PC) tới khi bán / huỷ / hết hạn.
-- trang_thai: 0 đang bán, 1 đã bán (chờ người bán nhận tiền), 2 đã trả tiền, 3 đã thu hồi (huỷ / hết hạn).
CREATE TABLE IF NOT EXISTS `tob_cho_pokemon` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `id_ban` INT NOT NULL,
  `ten_ban` VARCHAR(64) NOT NULL DEFAULT '',
  `user_ban` INT NOT NULL DEFAULT 0,
  `may_ban` VARCHAR(64) NOT NULL DEFAULT '',
  `ip_ban` VARCHAR(64) NOT NULL DEFAULT '',
  `du_lieu` VARCHAR(200) NOT NULL DEFAULT '',
  `dex` SMALLINT NOT NULL DEFAULT 0,
  `cap` TINYINT NOT NULL DEFAULT 1,
  `shiny` TINYINT NOT NULL DEFAULT 0,
  `do_hiem` TINYINT NOT NULL DEFAULT 0,
  `gia` BIGINT NOT NULL DEFAULT 0,
  `phi_dang` BIGINT NOT NULL DEFAULT 0,
  `dang_luc` BIGINT NOT NULL DEFAULT 0,
  `het_han` BIGINT NOT NULL DEFAULT 0,
  `trang_thai` TINYINT NOT NULL DEFAULT 0,
  `id_mua` INT NOT NULL DEFAULT 0,
  `ten_mua` VARCHAR(64) NOT NULL DEFAULT '',
  `mua_luc` BIGINT NOT NULL DEFAULT 0,
  `thue` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `trang_thai` (`trang_thai`, `het_han`),
  KEY `id_ban` (`id_ban`, `trang_thai`)
) DEFAULT CHARSET=utf8;

-- Nhật ký mỗi lần bán (ghi CÙNG giao dịch với Hộp PC người mua): giá, thuế, giá sát trần, nghi vấn.
CREATE TABLE IF NOT EXISTS `tob_log_cho_pokemon` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `luc` DATETIME NOT NULL,
  `id_tin` BIGINT NOT NULL,
  `id_ban` INT NOT NULL,
  `ten_ban` VARCHAR(64) NOT NULL DEFAULT '',
  `id_mua` INT NOT NULL,
  `ten_mua` VARCHAR(64) NOT NULL DEFAULT '',
  `con` VARCHAR(200) NOT NULL DEFAULT '',
  `gia` BIGINT NOT NULL DEFAULT 0,
  `thue` BIGINT NOT NULL DEFAULT 0,
  `sat_tran` TINYINT NOT NULL DEFAULT 0,
  `nghi` VARCHAR(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `id_ban` (`id_ban`, `luc`),
  KEY `id_mua` (`id_mua`, `luc`)
) DEFAULT CHARSET=utf8;
