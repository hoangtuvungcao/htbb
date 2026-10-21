-- =====================================================================================================================
--  BỘ TRANG BỊ, NGỌC HỆ, CHẾ TẠO, MỐC NEO NÂNG CẤP, THU PHỤC POKÉMON (0.9.8, nhánh bo-ngoc-bat)
-- =====================================================================================================================
--
--      mysql -u root htbb_db < sql/them_bo_ngoc_che_tao_bat.sql
--
--  Chạy lại được (CREATE TABLE IF NOT EXISTS, ADD COLUMN IF NOT EXISTS, INSERT ... ON DUPLICATE KEY UPDATE
--  cột = VALUES(cột)). MariaDB 10.3+, utf8. Server game cũng tự tạo các bảng này (rỗng) lúc khởi động
--  (data.DatabaseBoNgoc.taoBang) và dùng số mặc định trong mã khi bảng rỗng; tệp này điền dữ liệu chuẩn.
--
--  Bảng có sẵn từ pokemon_noi_dung.sql, giờ server ĐỌC (real.BoNgocGame, nạp lại bằng lệnh admin "9" hoặc tự động khi
--  CHECKSUM TABLE đổi, ≤ 60 giây):
--    data_noi_dung_bo        bộ cùng bậc 4 / 7 / 11 món: +5% HP / +5% tấn công / +3% chí mạng (trần chung 5% / 3%)
--    data_noi_dung_ngoc_he   giá khảm / nâng ngọc theo giờ thu nhập (1,45^(n-1)), % chỉ số món, đá khi phân rã
--    data_noi_dung_che_tao   công thức Machamp (+ cột loi_che_tao, bat)
--  Bảng mới:
--    data_ngoc_he_lo         số lỗ ngọc theo colorName (bậc hiếm) của món
--    data_ngoc_he_khac       ngọc hệ A trừ kháng hệ nào (phần "hại" của ngọc)
--    data_che_tao_bac        tỉ lệ bậc hiếm thành phẩm chế tạo (Hiếm 60 / Quý 30 / Sử thi 9 / Huyền thoại 1%)
--    data_nang_cap_moc       mốc neo nâng cấp +7 / +10 / +13
--    data_bat_bong           Poké Ball / Great Ball / Ultra Ball (hệ số, trần, giá theo giờ thu nhập, cấp mở)
--    data_bat_loai           loài thu phục được: độ hiếm, tỉ lệ gốc, tiến hoá trong Hộp PC (cấp, kẹo)
--    tob_ho_so_bo_ngoc       người chơi: túi ngọc hệ + pity chế tạo (chuỗi k=v)
--    tob_hop_pc              người chơi: Hộp PC, Kẹo loài, bóng, bộ đếm ngày (chuỗi k=v)
--    tob_pokedex             + cột so_bat, bat_shiny ("Đã bắt")
--  Khoá cau_hinh (danh mục: tools/cauhinh/danh_muc.json): bo.*, ngoc_he.*, che_tao.*, nang_cap.moc_neo,
--  nang_cap.he_so_xu_bac_cao, bat.*. Thiết kế: docs/thiet-ke-game.md 4.2, 4.3, 7.7.
-- =====================================================================================================================

SET NAMES utf8;

-- ---------------------------------------------------------------------------------------------------------- ngọc hệ
CREATE TABLE IF NOT EXISTS `data_ngoc_he_lo` (
  `color_name` TINYINT NOT NULL,
  `ten_bac` VARCHAR(32) NOT NULL DEFAULT '',
  `so_lo` TINYINT NOT NULL DEFAULT 1,
  PRIMARY KEY (`color_name`)
) DEFAULT CHARSET=utf8;
-- colorName của món: 0 trắng = Thường, 3 xanh = Hiếm, 2 đỏ = Quý, 1 Hoàn mỹ = Sử thi, 4 tím = Sử thi, 6 = Huyền thoại,
-- 7 = Hiếm (mã mới của thiết kế cân bằng 4.3); 5 vàng = đồ trang trí (-1: không khảm).
INSERT INTO `data_ngoc_he_lo` (`color_name`, `ten_bac`, `so_lo`) VALUES
(0, 'Thường', 1), (3, 'Hiếm', 1), (7, 'Hiếm', 1), (2, 'Quý', 2), (1, 'Sử thi', 2), (4, 'Sử thi', 2),
(6, 'Huyền thoại', 3), (5, 'Trang trí', -1)
ON DUPLICATE KEY UPDATE `ten_bac` = VALUES(`ten_bac`), `so_lo` = VALUES(`so_lo`);

CREATE TABLE IF NOT EXISTS `data_ngoc_he_khac` (
  `he` TINYINT NOT NULL,
  `he_khac` TINYINT NOT NULL,
  PRIMARY KEY (`he`)
) DEFAULT CHARSET=utf8;
-- Ngọc hệ A cấp n: −n‰ kháng hệ he_khac (hệ đánh A siêu hiệu quả). Thường→Giác đấu, Lửa→Nước, Nước→Điện, Điện→Đất,
-- Cỏ→Lửa, Băng→Lửa, Giác đấu→Siêu linh, Độc→Đất, Đất→Nước, Bay→Điện, Siêu linh→Bóng tối, Bọ→Lửa, Đá→Nước,
-- Ma→Bóng tối, Rồng→Băng, Bóng tối→Giác đấu, Thép→Lửa, Tiên→Thép.
INSERT INTO `data_ngoc_he_khac` (`he`, `he_khac`) VALUES
(0, 6), (1, 2), (2, 3), (3, 8), (4, 1), (5, 1), (6, 10), (7, 8), (8, 2), (9, 3), (10, 15), (11, 1), (12, 2), (13, 15),
(14, 5), (15, 6), (16, 1), (17, 16)
ON DUPLICATE KEY UPDATE `he_khac` = VALUES(`he_khac`);

-- Ngọc hệ đã có code: đổi trạng thái thiết kế (cột chỉ để tra cứu).
UPDATE `data_noi_dung_ngoc_he` SET `trang_thai` = 'da_noi' WHERE `trang_thai` = 'can_code';

-- ---------------------------------------------------------------------------------------------------------- chế tạo
ALTER TABLE `data_noi_dung_che_tao`
  ADD COLUMN IF NOT EXISTS `loi_che_tao` TINYINT NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS `bat` TINYINT NOT NULL DEFAULT 1;

CREATE TABLE IF NOT EXISTS `data_che_tao_bac` (
  `ma` TINYINT NOT NULL,
  `ten` VARCHAR(32) NOT NULL DEFAULT '',
  `color_name` TINYINT NOT NULL DEFAULT 0,
  `phan_nghin` INT NOT NULL DEFAULT 0,
  `he_so_phan_tram` SMALLINT NOT NULL DEFAULT 100,
  `so_lo` TINYINT NOT NULL DEFAULT 1,
  PRIMARY KEY (`ma`)
) DEFAULT CHARSET=utf8;
-- ma 3 = Sử thi là mốc pity (che_tao.pity = 20 lần). Huyền thoại chưa có bậc màu riêng: Hoàn mỹ (colorName 1) + 3 lỗ.
INSERT INTO `data_che_tao_bac` (`ma`, `ten`, `color_name`, `phan_nghin`, `he_so_phan_tram`, `so_lo`) VALUES
(1, 'Hiếm', 3, 600, 110, 1),
(2, 'Quý', 2, 300, 120, 2),
(3, 'Sử thi', 1, 90, 130, 2),
(4, 'Huyền thoại', 1, 10, 130, 3)
ON DUPLICATE KEY UPDATE `ten` = VALUES(`ten`), `color_name` = VALUES(`color_name`), `phan_nghin` = VALUES(`phan_nghin`),
  `he_so_phan_tram` = VALUES(`he_so_phan_tram`), `so_lo` = VALUES(`so_lo`);

-- ---------------------------------------------------------------------------------------------------------- nâng cấp
CREATE TABLE IF NOT EXISTS `data_nang_cap_moc` (
  `moc` TINYINT NOT NULL,
  PRIMARY KEY (`moc`)
) DEFAULT CHARSET=utf8;
INSERT INTO `data_nang_cap_moc` (`moc`) VALUES (7), (10), (13)
ON DUPLICATE KEY UPDATE `moc` = VALUES(`moc`);

-- ---------------------------------------------------------------------------------------------------------- thu phục
CREATE TABLE IF NOT EXISTS `data_bat_bong` (
  `ma` VARCHAR(16) NOT NULL,
  `ten` VARCHAR(32) NOT NULL DEFAULT '',
  `thu_tu` TINYINT NOT NULL DEFAULT 0,
  `he_so_phan_tram` SMALLINT NOT NULL DEFAULT 100,
  `tran_phan_nghin` SMALLINT NOT NULL DEFAULT 600,
  `gia_phan_nghin_gio` INT NOT NULL DEFAULT 20,
  `cap_mo` TINYINT NOT NULL DEFAULT 1,
  `bat` TINYINT NOT NULL DEFAULT 1,
  PRIMARY KEY (`ma`)
) DEFAULT CHARSET=utf8;
-- Giá = phần nghìn giờ thu nhập ở cấp người mua (hố hút xu nhỏ). Trần tỉ lệ mỗi bóng: 60 / 75 / 90%.
INSERT INTO `data_bat_bong` (`ma`, `ten`, `thu_tu`, `he_so_phan_tram`, `tran_phan_nghin`, `gia_phan_nghin_gio`, `cap_mo`, `bat`) VALUES
('poke', 'Poké Ball', 1, 100, 600, 20, 1, 1),
('great', 'Great Ball', 2, 150, 750, 50, 20, 1),
('ultra', 'Ultra Ball', 3, 200, 900, 120, 40, 1)
ON DUPLICATE KEY UPDATE `ten` = VALUES(`ten`), `thu_tu` = VALUES(`thu_tu`), `he_so_phan_tram` = VALUES(`he_so_phan_tram`),
  `tran_phan_nghin` = VALUES(`tran_phan_nghin`), `gia_phan_nghin_gio` = VALUES(`gia_phan_nghin_gio`),
  `cap_mo` = VALUES(`cap_mo`), `bat` = VALUES(`bat`);

CREATE TABLE IF NOT EXISTS `data_bat_loai` (
  `dex` SMALLINT NOT NULL,
  `ten` VARCHAR(32) NOT NULL DEFAULT '',
  `do_hiem` TINYINT NOT NULL DEFAULT 0,
  `ti_le_phan_nghin` SMALLINT NOT NULL DEFAULT 0,
  `tien_hoa_thanh` SMALLINT NOT NULL DEFAULT 0,
  `cap_tien_hoa` TINYINT NOT NULL DEFAULT 0,
  `keo_tien_hoa` SMALLINT NOT NULL DEFAULT 0,
  `bat_duoc` TINYINT NOT NULL DEFAULT 1,
  `chi_su_kien` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`dex`)
) DEFAULT CHARSET=utf8;
-- Mọi loài quái thường (du_lieu/bang_quai.csv phan_loai = quai) + dạng tiến hoá có hình. do_hiem 0 / 1 / 2 = tỉ lệ gốc
-- 45 / 20 / 8% (ti_le_phan_nghin 0 = theo độ hiếm). Boss, Gym, boss thế giới, huyền thoại KHÔNG có dòng (không bắt được);
-- Gengar / Dusknoir chỉ đạt được bằng tiến hoá trong Hộp PC (bat_duoc = 0). keo_tien_hoa: 25 (bậc 1) / 50 (bậc 2), x2 loài hiếm.
INSERT INTO `data_bat_loai` (`dex`, `ten`, `do_hiem`, `ti_le_phan_nghin`, `tien_hoa_thanh`, `cap_tien_hoa`, `keo_tien_hoa`, `bat_duoc`, `chi_su_kien`) VALUES
(10,'Caterpie',0,0,0,0,0,1,0),
(12,'Butterfree',0,0,0,0,0,1,0),
(13,'Weedle',0,0,0,0,0,1,0),
(19,'Rattata',0,0,0,0,0,1,0),
(23,'Ekans',0,0,24,22,25,1,0),
(24,'Arbok',0,0,0,0,0,1,0),
(27,'Sandshrew',0,0,0,0,0,1,0),
(37,'Vulpix',0,0,0,0,0,1,0),
(43,'Oddish',0,0,44,21,25,1,0),
(44,'Gloom',0,0,45,36,50,1,0),
(45,'Vileplume',0,0,0,0,0,1,0),
(46,'Paras',0,0,47,24,25,1,0),
(47,'Parasect',0,0,0,0,0,1,0),
(48,'Venonat',0,0,0,0,0,1,0),
(58,'Growlithe',0,0,59,30,25,1,0),
(59,'Arcanine',0,0,0,0,0,1,0),
(69,'Bellsprout',0,0,0,0,0,1,0),
(72,'Tentacool',0,0,0,0,0,1,0),
(76,'Golem',0,0,0,0,0,1,0),
(83,'Farfetch''d',0,0,0,0,0,1,0),
(85,'Dodrio',0,0,0,0,0,1,0),
(92,'Gastly',0,0,93,25,25,1,0),
(93,'Haunter',0,0,94,38,50,1,0),
(94,'Gengar',0,0,0,0,0,0,0),
(99,'Kingler',0,0,0,0,0,1,0),
(104,'Cubone',0,0,105,28,25,1,0),
(105,'Marowak',0,0,0,0,0,1,0),
(112,'Rhydon',0,0,0,0,0,1,0),
(123,'Scyther',1,0,0,0,0,1,0),
(127,'Pinsir',1,0,0,0,0,1,0),
(128,'Tauros',1,0,0,0,0,1,0),
(159,'Croconaw',0,0,160,30,25,1,0),
(160,'Feraligatr',0,0,0,0,0,1,0),
(168,'Ariados',0,0,0,0,0,1,0),
(194,'Wooper',0,0,195,20,25,1,0),
(195,'Quagsire',0,0,0,0,0,1,0),
(214,'Heracross',1,0,0,0,0,1,0),
(219,'Magcargo',0,0,0,0,0,1,0),
(255,'Torchic',0,0,256,16,25,1,0),
(256,'Combusken',0,0,257,36,50,1,0),
(257,'Blaziken',0,0,0,0,0,1,0),
(261,'Poochyena',0,0,262,18,25,1,0),
(262,'Mightyena',0,0,0,0,0,1,0),
(269,'Dustox',0,0,0,0,0,1,0),
(279,'Pelipper',0,0,0,0,0,1,0),
(286,'Breloom',0,0,0,0,0,1,0),
(288,'Vigoroth',0,0,0,0,0,1,0),
(297,'Hariyama',0,0,0,0,0,1,0),
(302,'Sableye',1,0,0,0,0,1,0),
(304,'Aron',0,0,305,32,25,1,0),
(305,'Lairon',0,0,306,42,50,1,0),
(306,'Aggron',2,0,0,0,0,1,0),
(309,'Electrike',0,0,310,26,25,1,0),
(310,'Manectric',0,0,0,0,0,1,0),
(318,'Carvanha',0,0,319,30,25,1,0),
(319,'Sharpedo',0,0,0,0,0,1,0),
(321,'Wailord',0,0,0,0,0,1,0),
(322,'Numel',0,0,323,33,25,1,0),
(323,'Camerupt',0,0,0,0,0,1,0),
(324,'Torkoal',0,0,0,0,0,1,0),
(326,'Grumpig',0,0,0,0,0,1,0),
(330,'Flygon',2,0,0,0,0,1,0),
(333,'Swablu',0,0,334,35,25,1,0),
(334,'Altaria',1,0,0,0,0,1,0),
(335,'Zangoose',1,0,0,0,0,1,0),
(336,'Seviper',0,0,0,0,0,1,0),
(337,'Lunatone',0,0,0,0,0,1,0),
(340,'Whiscash',0,0,0,0,0,1,0),
(342,'Crawdaunt',0,0,0,0,0,1,0),
(343,'Baltoy',0,0,344,36,25,1,0),
(344,'Claydol',1,0,0,0,0,1,0),
(346,'Cradily',1,0,0,0,0,1,0),
(348,'Armaldo',1,0,0,0,0,1,0),
(353,'Shuppet',0,0,354,37,25,1,0),
(354,'Banette',0,0,0,0,0,1,0),
(355,'Duskull',0,0,356,37,25,1,0),
(356,'Dusclops',0,0,477,50,50,1,0),
(357,'Tropius',1,0,0,0,0,1,0),
(359,'Absol',1,0,0,0,0,1,0),
(361,'Snorunt',0,0,362,42,25,1,0),
(362,'Glalie',1,0,0,0,0,1,0),
(363,'Spheal',0,0,0,0,0,1,0),
(365,'Walrein',1,0,0,0,0,1,0),
(371,'Bagon',1,0,372,30,50,1,0),
(372,'Shelgon',1,0,373,50,100,1,0),
(373,'Salamence',2,0,0,0,0,0,0),
(375,'Metang',1,0,376,45,50,1,0),
(376,'Metagross',2,0,0,0,0,1,0),
(401,'Kricketot',0,0,402,10,25,1,0),
(402,'Kricketune',0,0,0,0,0,1,0),
(404,'Luxio',0,0,405,30,25,1,0),
(405,'Luxray',0,0,0,0,0,1,0),
(423,'Gastrodon',0,0,0,0,0,1,0),
(451,'Skorupi',0,0,0,0,0,1,0),
(453,'Croagunk',0,0,454,37,25,1,0),
(454,'Toxicroak',0,0,0,0,0,1,0),
(477,'Dusknoir',0,0,0,0,0,0,0),
(498,'Tepig',0,0,0,0,0,1,0),
(510,'Liepard',0,0,0,0,0,1,0),
(536,'Palpitoad',0,0,537,36,25,1,0),
(537,'Seismitoad',0,0,0,0,0,1,0),
(543,'Venipede',0,0,544,22,25,1,0),
(544,'Whirlipede',0,0,545,30,50,1,0),
(545,'Scolipede',0,0,0,0,0,1,0),
(552,'Krokorok',0,0,0,0,0,1,0),
(559,'Scraggy',0,0,560,39,25,1,0),
(560,'Scrafty',0,0,0,0,0,1,0),
(562,'Yamask',0,0,0,0,0,1,0),
(596,'Galvantula',0,0,0,0,0,1,0),
(607,'Litwick',0,0,0,0,0,1,0),
(637,'Volcarona',2,0,0,0,0,1,0),
(667,'Litleo',0,0,0,0,0,1,0),
(709,'Trevenant',1,0,0,0,0,1,0),
(739,'Crabrawler',0,0,0,0,0,1,0),
(750,'Mudsdale',0,0,0,0,0,1,0),
(781,'Dhelmise',1,0,0,0,0,1,0),
(901,'Ursaluna',2,0,0,0,0,1,0)
ON DUPLICATE KEY UPDATE `ten` = VALUES(`ten`), `do_hiem` = VALUES(`do_hiem`), `ti_le_phan_nghin` = VALUES(`ti_le_phan_nghin`),
  `tien_hoa_thanh` = VALUES(`tien_hoa_thanh`), `cap_tien_hoa` = VALUES(`cap_tien_hoa`), `keo_tien_hoa` = VALUES(`keo_tien_hoa`),
  `bat_duoc` = VALUES(`bat_duoc`), `chi_su_kien` = VALUES(`chi_su_kien`);

-- ---------------------------------------------------------------------------------------------------------- người chơi
CREATE TABLE IF NOT EXISTS `tob_ho_so_bo_ngoc` (
  `id_char` INT NOT NULL,
  `du_lieu` MEDIUMTEXT NULL,
  `sua_luc` DATETIME NULL,
  PRIMARY KEY (`id_char`)
) DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `tob_hop_pc` (
  `id_char` INT NOT NULL,
  `du_lieu` MEDIUMTEXT NULL,
  `sua_luc` DATETIME NULL,
  PRIMARY KEY (`id_char`)
) DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `tob_pokedex` (
  `id_char` INT NOT NULL,
  `dex`     SMALLINT NOT NULL,
  `so_ha`   INT NOT NULL DEFAULT 0,
  `lan_dau` DATETIME NOT NULL,
  PRIMARY KEY (`id_char`, `dex`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
ALTER TABLE `tob_pokedex`
  ADD COLUMN IF NOT EXISTS `so_bat` INT NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS `bat_shiny` TINYINT NOT NULL DEFAULT 0;
