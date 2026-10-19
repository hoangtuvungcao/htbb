-- ======================================================================================================================
-- PHO BAN HAM NGUC (to doi 5 tang) + CUP PVP TUAN XOAY VONG (0.9.7, de xuat 7 + 10 chu game da duyet 19/10/2026).
-- Chay lai bao nhieu lan cung duoc (MariaDB 10.3, utf8): CREATE TABLE IF NOT EXISTS + INSERT ... ON DUPLICATE KEY
-- UPDATE cot = VALUES(cot). May chu game tu tao cac bang tob_* neu thieu (data.DatabasePhoBanCup); bang data_* thieu
-- thi may chu dung ban mac dinh trong com.htbb.pokemon.PhoBanLogic / CupLogic (cung so lieu voi tep nay).
--
--   mysql -u root htbb_db < sql/them_pho_ban_cup.sql
--
-- Thiet ke + so lieu: docs/thiet-ke-game.md muc 5.3 (Cup PvP tuan) va 5.4 (Ham nguc / Pho ban). Test:
-- server/test/KiemPhoBanCup.java doi chieu tep nay voi ban mac dinh trong ma.
-- ======================================================================================================================

-- ---------------------------------------------------------------------------------------------------- 1. Pho ban
-- Chu de theo thu (1 = thu Hai .. 7 = Chu nhat): he + template quai (du_lieu/bang_quai.csv) + boss tang 5.
CREATE TABLE IF NOT EXISTS `data_pho_ban_ngay` (
  `thu` tinyint NOT NULL,
  `ma` varchar(32) NOT NULL,
  `ten` varchar(64) NOT NULL DEFAULT '',
  `he` tinyint NOT NULL DEFAULT -1,
  `mau_quai` varchar(64) NOT NULL DEFAULT '',
  `mau_boss` int NOT NULL DEFAULT 0,
  `mo_ta` varchar(255) NOT NULL DEFAULT '',
  `bat` tinyint NOT NULL DEFAULT 1,
  `sua_boi` varchar(50) NOT NULL DEFAULT '',
  `sua_luc` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`thu`)
) DEFAULT CHARSET=utf8;
INSERT INTO `data_pho_ban_ngay` (`thu`,`ma`,`ten`,`he`,`mau_quai`,`mau_boss`,`mo_ta`,`bat`) VALUES
(1,'hang_bang','Hang Băng',5,'150,155,165',172,'Snorunt, Spheal, Glalie canh giữ; boss Walrein.',1),
(2,'nui_lua','Núi Lửa',1,'17,28,141,76',154,'Torkoal, Growlithe, Numel, Magcargo; boss Camerupt.',1),
(3,'ho_sau','Hồ Sâu',2,'26,29,35,144',56,'Tentacool, Croconaw, Crawdaunt, Carvanha; boss Sharpedo.',1),
(4,'rung_ram','Rừng Rậm',4,'10,19,80,20',58,'Oddish, Paras, Breloom, Gloom; boss Vileplume.',1),
(5,'nha_may_dien','Nhà máy Điện',3,'62,140,118',152,'Luxio, Electrike, Galvantula; boss Manectric.',1),
(6,'thap_ma','Tháp Ma',13,'25,55,52,114',161,'Gastly, Duskull, Haunter, Shuppet; boss Banette.',1),
(7,'hang_rong','Hang Rồng',14,'157,167,164',39,'Bagon, Shelgon, Altaria; boss Salamence.',1)
ON DUPLICATE KEY UPDATE `ma`=VALUES(`ma`), `ten`=VALUES(`ten`), `he`=VALUES(`he`), `mau_quai`=VALUES(`mau_quai`),
  `mau_boss`=VALUES(`mau_boss`), `mo_ta`=VALUES(`mo_ta`), `bat`=VALUES(`bat`);

-- Tang: so quai + he so (phan nghin) HP / tan cong / phong thu so voi chi so goc o cap do (noi suy bang Gym:
-- HP ~105 giay danh mot minh dung cap, do +6). HP nhan them 60% / nguoi them (boss 70%). Tong ~11 phut mot minh.
CREATE TABLE IF NOT EXISTS `data_pho_ban_tang` (
  `tang` tinyint NOT NULL,
  `so_quai` int NOT NULL DEFAULT 1,
  `hp_phan_nghin` int NOT NULL DEFAULT 1000,
  `tc_phan_nghin` int NOT NULL DEFAULT 1000,
  `thu_phan_nghin` int NOT NULL DEFAULT 1000,
  `la_boss` tinyint NOT NULL DEFAULT 0,
  `sua_boi` varchar(50) NOT NULL DEFAULT '',
  `sua_luc` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`tang`)
) DEFAULT CHARSET=utf8;
INSERT INTO `data_pho_ban_tang` (`tang`,`so_quai`,`hp_phan_nghin`,`tc_phan_nghin`,`thu_phan_nghin`,`la_boss`) VALUES
(1,6,120,550,900,0),
(2,8,120,600,900,0),
(3,7,150,650,950,0),
(4,10,120,700,950,0),
(5,1,1800,1000,1000,1)
ON DUPLICATE KEY UPDATE `so_quai`=VALUES(`so_quai`), `hp_phan_nghin`=VALUES(`hp_phan_nghin`),
  `tc_phan_nghin`=VALUES(`tc_phan_nghin`), `thu_phan_nghin`=VALUES(`thu_phan_nghin`), `la_boss`=VALUES(`la_boss`);

-- Ruong boss (moi nguoi, 1 lan / ngay khi ha boss tang 5): them dong nguon 'pho_ban' vao bang roi CO SAN
-- data_noi_dung_roi (khoa_nguon = nhom cap cao nhat cua doi: < 30 / 30-59 / >= 60). Tham chieu id CO SAN, khong dinh
-- nghia vat pham moi: ngoc:8 Da nang cap (RoiLauDai.ID_DA), item:1034 Bui sao, 1035 Manh sao, 1036 Manh thien thach,
-- 1038 Manh tinh hoa; trang_bi:0 = 1 mon trang bi ngau nhien cua dai cap (cac dong 'dai' cua bang roi). Moi thu KHOA.
-- Ky vong / lan: 2,5-3 da, 0,5 nguyen lieu nang cap, 0,2 trang bi (tran kiem o KiemPhoBanCup: 4 / 1 / 0,35).
INSERT INTO `data_noi_dung_roi` (`stt`,`nguon`,`khoa_nguon`,`vat_pham`,`ten`,`ppm`,`sl_min`,`sl_max`,`do_hiem`,`khoa`,`het_han`) VALUES
(9001,'pho_ban','boss:thap','ngoc:8','Đá nâng cấp',1000000,2,3,'',1,0),
(9002,'pho_ban','boss:thap','item:1034','Bụi sao',500000,1,1,'',1,0),
(9003,'pho_ban','boss:thap','trang_bi:0','Trang bị đúng dải cấp',200000,1,1,'trang',1,0),
(9011,'pho_ban','boss:trung','ngoc:8','Đá nâng cấp',1000000,2,3,'',1,0),
(9012,'pho_ban','boss:trung','item:1035','Mảnh sao',500000,1,1,'',1,0),
(9013,'pho_ban','boss:trung','trang_bi:0','Trang bị đúng dải cấp',200000,1,1,'trang',1,0),
(9021,'pho_ban','boss:cao','ngoc:8','Đá nâng cấp',1000000,2,4,'',1,0),
(9022,'pho_ban','boss:cao','item:1036','Mảnh thiên thạch',450000,1,1,'',1,0),
(9023,'pho_ban','boss:cao','item:1038','Mảnh tinh hoa',50000,1,1,'',1,0),
(9024,'pho_ban','boss:cao','trang_bi:0','Trang bị đúng dải cấp',200000,1,1,'trang',1,0)
ON DUPLICATE KEY UPDATE `nguon`=VALUES(`nguon`), `khoa_nguon`=VALUES(`khoa_nguon`), `vat_pham`=VALUES(`vat_pham`),
  `ten`=VALUES(`ten`), `ppm`=VALUES(`ppm`), `sl_min`=VALUES(`sl_min`), `sl_max`=VALUES(`sl_max`),
  `do_hiem`=VALUES(`do_hiem`), `khoa`=VALUES(`khoa`), `het_han`=VALUES(`het_han`);

CREATE TABLE IF NOT EXISTS `tob_pho_ban_luot` (
  `ngay` int NOT NULL,
  `char_id` int NOT NULL,
  `luot` int NOT NULL DEFAULT 0,
  `cap_nhat` datetime NULL,
  PRIMARY KEY (`ngay`, `char_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE IF NOT EXISTS `tob_pho_ban_lich_su` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `luc` datetime NOT NULL,
  `ngay` int NOT NULL,
  `chu_de` varchar(32) NOT NULL DEFAULT '',
  `cap` int NOT NULL DEFAULT 0,
  `so_nguoi` tinyint NOT NULL DEFAULT 0,
  `thanh_vien` varchar(255) NOT NULL DEFAULT '',
  `tang` tinyint NOT NULL DEFAULT 0,
  `ket_qua` tinyint NOT NULL DEFAULT 0,
  `giay` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `k_ngay` (`ngay`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ---------------------------------------------------------------------------------------------------- 2. Cup PvP
-- Loai cup. cap_toi_da 0 = khong gioi han; he -1 = moi he (0..17 theo com.htbb.pokemon.HePokemon); so_nguoi 1 / 2;
-- phan_nghin_min / max = kep moi don (phan nghin HP toi da muc tieu) sau chuan hoa - Cup Khong trang bi kep hep 30..50.
CREATE TABLE IF NOT EXISTS `data_cup_loai` (
  `ma` varchar(16) NOT NULL,
  `ten` varchar(64) NOT NULL DEFAULT '',
  `mo_ta` varchar(255) NOT NULL DEFAULT '',
  `cap_toi_da` int NOT NULL DEFAULT 0,
  `he` tinyint NOT NULL DEFAULT -1,
  `so_nguoi` tinyint NOT NULL DEFAULT 1,
  `khong_trang_bi` tinyint NOT NULL DEFAULT 0,
  `phan_nghin_min` int NOT NULL DEFAULT 10,
  `phan_nghin_max` int NOT NULL DEFAULT 80,
  `thu_tu` int NOT NULL DEFAULT 0,
  `bat` tinyint NOT NULL DEFAULT 1,
  `sua_boi` varchar(50) NOT NULL DEFAULT '',
  `sua_luc` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`ma`)
) DEFAULT CHARSET=utf8;
INSERT INTO `data_cup_loai` (`ma`,`ten`,`mo_ta`,`cap_toi_da`,`he`,`so_nguoi`,`khong_trang_bi`,`phan_nghin_min`,`phan_nghin_max`,`thu_tu`,`bat`) VALUES
('nhi','Cúp Nhí','Cúp cho Pokémon cấp 20–30: chỉ số chuẩn hoá, ai cũng có cơ hội.',30,-1,1,0,10,80,1,1),
('he_lua','Cúp hệ Lửa','Chỉ Pokémon mang hệ Lửa (Charmander, Flareon...).',0,1,1,0,10,80,2,1),
('he_nuoc','Cúp hệ Nước','Chỉ Pokémon mang hệ Nước (Squirtle, Vaporeon...).',0,2,1,0,10,80,3,1),
('he_co','Cúp hệ Cỏ','Chỉ Pokémon mang hệ Cỏ (Bulbasaur, Leafeon...).',0,4,1,0,10,80,4,1),
('he_dien','Cúp hệ Điện','Chỉ Pokémon mang hệ Điện (Pichu, Jolteon...).',0,3,1,0,10,80,5,1),
('doi','Cúp Đôi','Đấu 2 đấu 2: đăng ký cùng một bạn trong tổ đội.',0,-1,2,0,10,80,6,1),
('thuan','Cúp Không trang bị','Chỉ kỹ năng và hệ quyết định: trang bị gần như không tính.',0,-1,1,1,30,50,7,1)
ON DUPLICATE KEY UPDATE `ten`=VALUES(`ten`), `mo_ta`=VALUES(`mo_ta`), `cap_toi_da`=VALUES(`cap_toi_da`), `he`=VALUES(`he`),
  `so_nguoi`=VALUES(`so_nguoi`), `khong_trang_bi`=VALUES(`khong_trang_bi`), `phan_nghin_min`=VALUES(`phan_nghin_min`),
  `phan_nghin_max`=VALUES(`phan_nghin_max`), `thu_tu`=VALUES(`thu_tu`), `bat`=VALUES(`bat`);

-- Lich xoay 16 tuan: o = chi so tuan lien tuc (CupLogic.tuan, thu Hai 04:00 gio Viet Nam) mod so dong.
CREATE TABLE IF NOT EXISTS `data_cup_lich` (
  `vi_tri` int NOT NULL,
  `ma_loai` varchar(16) NOT NULL,
  PRIMARY KEY (`vi_tri`)
) DEFAULT CHARSET=utf8;
INSERT INTO `data_cup_lich` (`vi_tri`,`ma_loai`) VALUES
(0,'nhi'),(1,'he_lua'),(2,'doi'),(3,'thuan'),
(4,'nhi'),(5,'he_nuoc'),(6,'doi'),(7,'thuan'),
(8,'nhi'),(9,'he_co'),(10,'doi'),(11,'thuan'),
(12,'nhi'),(13,'he_dien'),(14,'doi'),(15,'thuan')
ON DUPLICATE KEY UPDATE `ma_loai`=VALUES(`ma_loai`);

CREATE TABLE IF NOT EXISTS `tob_cup_dang_ky` (
  `tuan` int NOT NULL,
  `char_id` int NOT NULL,
  `doi_id` int NOT NULL,
  `ma_loai` varchar(16) NOT NULL DEFAULT '',
  `ten` varchar(40) NOT NULL DEFAULT '',
  `cap` int NOT NULL DEFAULT 0,
  `may` varchar(64) NOT NULL DEFAULT '',
  `ip` varchar(64) NOT NULL DEFAULT '',
  `user_id` int NOT NULL DEFAULT 0,
  `thu_tu` int NOT NULL DEFAULT 0,
  `diem` int NOT NULL DEFAULT 0,
  `thang` int NOT NULL DEFAULT 0,
  `hoa` int NOT NULL DEFAULT 0,
  `thua` int NOT NULL DEFAULT 0,
  `mien` tinyint NOT NULL DEFAULT 0,
  `hang` int NOT NULL DEFAULT 0,
  `luc` datetime NULL,
  PRIMARY KEY (`tuan`, `char_id`),
  KEY `k_doi` (`tuan`, `doi_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE IF NOT EXISTS `tob_cup_tran` (
  `tuan` int NOT NULL,
  `vong` tinyint NOT NULL,
  `doi_a` int NOT NULL,
  `doi_b` int NOT NULL,
  `ket_qua` tinyint NOT NULL DEFAULT 0,
  `giay` int NOT NULL DEFAULT 0,
  `luc` datetime NULL,
  PRIMARY KEY (`tuan`, `vong`, `doi_a`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
-- Thuong: mot dong / (tuan, nhan vat), ghi TRUOC khi phat; da_nhan doi 0 -> 1 nguyen tu luc phat (offline nhan khi
-- dang nhap). Khong bao gio phat hai lan.
CREATE TABLE IF NOT EXISTS `tob_cup_thuong` (
  `tuan` int NOT NULL,
  `char_id` int NOT NULL,
  `hang` int NOT NULL DEFAULT 0,
  `luong_khoa` int NOT NULL DEFAULT 0,
  `danh_hieu` int NOT NULL DEFAULT -1,
  `phut` int NOT NULL DEFAULT 0,
  `da_nhan` tinyint NOT NULL DEFAULT 0,
  `luc` datetime NOT NULL,
  `nhan_luc` datetime NULL,
  PRIMARY KEY (`tuan`, `char_id`),
  KEY `k_cho` (`char_id`, `da_nhan`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ---------------------------------------------------------------------------------------------------- 3. Ban do
-- Ten ban do phong rieng (P6: data_map_ten la nguon duy nhat cua ten nguoi choi thay; client them "_<khu>").
CREATE TABLE IF NOT EXISTS `data_map_ten` (
  `map_id` int NOT NULL,
  `ten` varchar(64) NOT NULL,
  `vung` varchar(16) NOT NULL DEFAULT '',
  `loai` varchar(16) NOT NULL DEFAULT '',
  `mo_ta` varchar(128) NOT NULL DEFAULT '',
  `sua_boi` varchar(50) NOT NULL DEFAULT '',
  `sua_luc` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`map_id`)
) DEFAULT CHARSET=utf8;
INSERT INTO `data_map_ten` (`map_id`,`ten`,`vung`,`loai`,`mo_ta`) VALUES
(4381,'Phó bản Hầm ngục','','hang','Phòng riêng của tổ đội (5 tầng)'),
(4382,'Sàn Cúp PvP','','dau_truong','Sàn đấu riêng của trận cúp tuần')
ON DUPLICATE KEY UPDATE `ten`=VALUES(`ten`), `vung`=VALUES(`vung`), `loai`=VALUES(`loai`), `mo_ta`=VALUES(`mo_ta`);

-- ---------------------------------------------------------------------------------------------------- 4. Danh hieu
-- Danh muc danh hieu (he danh hieu 0.9.7, nhanh danh-hieu: cung cau truc bang data_danh_hieu). Kho so huu van la
-- tob_danh_hieu (Char.allDanhHieu) - may chu trao THANG id hieu ung + han (real.PhongRiengGame.traoDanhHieu), khong
-- tu deo. Chi so nho cua o danh hieu (ngan sach o 1%, chi danh hieu dang deo duoc tinh). Dai id 9800..9819.
CREATE TABLE IF NOT EXISTS `data_danh_hieu` (
  `id` INT NOT NULL,
  `ma` VARCHAR(48) NOT NULL,
  `ten` VARCHAR(64) NOT NULL DEFAULT '',
  `mo_ta` VARCHAR(255) NOT NULL DEFAULT '',
  `goi_y` VARCHAR(255) NOT NULL DEFAULT '',
  `do_hiem` TINYINT NOT NULL DEFAULT 0,
  `hinh` VARCHAR(16) NOT NULL DEFAULT '',
  `chi_so` VARCHAR(24) NOT NULL DEFAULT '',
  `phan_nghin` INT NOT NULL DEFAULT 0,
  `dieu_kien` VARCHAR(40) NOT NULL DEFAULT 'trao',
  `tham_so` VARCHAR(64) NOT NULL DEFAULT '',
  `so_luong` INT NOT NULL DEFAULT 1,
  `an` TINYINT NOT NULL DEFAULT 0,
  `thoi_han_phut` INT NOT NULL DEFAULT 0,
  `nguon` VARCHAR(16) NOT NULL DEFAULT '',
  `thu_tu` INT NOT NULL DEFAULT 0,
  `bat` TINYINT NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_ddh_ma` (`ma`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
INSERT INTO `data_danh_hieu` (`id`,`ma`,`ten`,`mo_ta`,`goi_y`,`do_hiem`,`hinh`,`chi_so`,`phan_nghin`,`dieu_kien`,`tham_so`,`so_luong`,`an`,`thoi_han_phut`,`nguon`,`thu_tu`,`bat`) VALUES
(9800,'cup_vo_dich_nhi','Vô địch Cúp Nhí','Vô địch Cúp Nhí của một tuần (Pokémon cấp 20–30). Hiệu lực 28 ngày.','Đứng hạng 1 Cúp Nhí.',2,'vuot','chi_mang',9,'trao','',1,0,40320,'cup',300,1),
(9801,'cup_vo_dich_he','Vua hệ tuần','Vô địch một Cúp hệ (Lửa / Nước / Cỏ / Điện). Hiệu lực 28 ngày.','Đứng hạng 1 một Cúp hệ.',2,'vuot','ne_tranh',9,'trao','',1,0,40320,'cup',301,1),
(9802,'cup_vo_dich_doi','Song đấu vô địch','Vô địch Cúp Đôi 2 đấu 2. Hiệu lực 28 ngày.','Đứng hạng 1 Cúp Đôi cùng đồng đội.',2,'vuot','khang_trang_thai',9,'trao','',1,0,40320,'cup',302,1),
(9803,'cup_vo_dich_thuan','Bậc thầy tay không','Vô địch Cúp Không trang bị. Hiệu lực 28 ngày.','Đứng hạng 1 Cúp Không trang bị.',2,'vuot','hp',9,'trao','',1,0,40320,'cup',303,1),
(9804,'cup_a_quan','Á quân cúp tuần','Hạng 2–3 của một cúp tuần. Hiệu lực 14 ngày.','Vào top 3 cúp tuần.',1,'khien','ne_tranh',7,'trao','',1,0,20160,'cup',304,1),
(9805,'cup_dau_si','Đấu sĩ cúp tuần','Hạng 4–8, hoặc dự đủ 3 trận và thắng ít nhất 1 trận cúp tuần. Hiệu lực 7 ngày.','Dự cúp tuần đủ 3 trận.',0,'khien','chi_mang',5,'trao','',1,0,10080,'cup',305,1),
(9810,'pho_ban_chinh_phuc','Chinh phục Phó bản','Hạ boss tầng 5 Phó bản Hầm ngục lần đầu.','Hạ boss tầng 5 Phó bản Hầm ngục.',1,'sao','khang_trang_thai',7,'trao','',1,0,0,'thanh_tuu',310,1),
(9811,'pho_ban_10','Thám hiểm Hầm ngục','Hạ boss Phó bản Hầm ngục 10 lần.','Hạ boss phó bản nhiều lần.',1,'la','hp',7,'pho_ban','',10,0,0,'thanh_tuu',311,1),
(9812,'pho_ban_50','Chúa tể Hầm ngục','Hạ boss Phó bản Hầm ngục 50 lần.','Hạ boss phó bản thật nhiều lần.',2,'mat_troi','chi_mang',9,'pho_ban','',50,1,0,'thanh_tuu',312,1)
ON DUPLICATE KEY UPDATE `ma`=VALUES(`ma`), `ten`=VALUES(`ten`), `mo_ta`=VALUES(`mo_ta`), `goi_y`=VALUES(`goi_y`),
  `do_hiem`=VALUES(`do_hiem`), `hinh`=VALUES(`hinh`), `chi_so`=VALUES(`chi_so`), `phan_nghin`=VALUES(`phan_nghin`),
  `dieu_kien`=VALUES(`dieu_kien`), `tham_so`=VALUES(`tham_so`), `so_luong`=VALUES(`so_luong`), `an`=VALUES(`an`),
  `thoi_han_phut`=VALUES(`thoi_han_phut`), `nguon`=VALUES(`nguon`), `thu_tu`=VALUES(`thu_tu`), `bat`=VALUES(`bat`);

-- ---------------------------------------------------------------------------------------------------- 5. Cau hinh
-- Danh muc khoa (nguon: tools/cauhinh/danh_muc.json, sinh lai o sql/pokemon_cau_hinh_trung_tam.sql). Lap lai o day de
-- nang cap CSDL cu chi can tep nay; KHONG ghi gia tri vao cau_hinh (thieu = mac dinh).
CREATE TABLE IF NOT EXISTS `cau_hinh_danh_muc` (`khoa` VARCHAR(64) NOT NULL, `nhom` VARCHAR(20) NOT NULL, `ten` VARCHAR(160) NOT NULL DEFAULT '', `mo_ta` TEXT NOT NULL, `kieu` VARCHAR(12) NOT NULL, `mac_dinh` VARCHAR(255) NOT NULL DEFAULT '', `gioi_han` TEXT NOT NULL, `ap_dung` VARCHAR(16) NOT NULL, `nhay` TINYINT NOT NULL DEFAULT 0, `khoa_prod` TINYINT NOT NULL DEFAULT 0, `chi_doc` TINYINT NOT NULL DEFAULT 0, `bang` VARCHAR(16) NOT NULL DEFAULT 'cau_hinh', `dung_o` VARCHAR(10) NOT NULL DEFAULT 'server', `nguon` VARCHAR(255) NOT NULL DEFAULT '', `thu_tu` INT NOT NULL DEFAULT 0, `lop` VARCHAR(10) NOT NULL DEFAULT 'chinh', `an` TINYINT NOT NULL DEFAULT 0, PRIMARY KEY (`khoa`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT INTO `cau_hinh_danh_muc` (`khoa`,`nhom`,`ten`,`mo_ta`,`kieu`,`mac_dinh`,`gioi_han`,`ap_dung`,`nguon`,`lop`,`an`) VALUES
('pho_ban.bat','pvp','Phó bản Hầm ngục (tổ đội 5 tầng)','0 = không mở phòng mới (phòng đang chạy vẫn chạy hết).','co','1','{}','ngay','real.PhoBanGame','luon_bat',1),
('pho_ban.luot_ngay','pvp','Phó bản: lượt vào mỗi ngày','Mỗi Pokémon, đếm cả lần vào lại sau khi thua. Thưởng ngày (0,2 giờ EXP + 5 lượng khoá + rương boss) vẫn chỉ 1 lần, chung với Hầm ngục bí ẩn.','so','3','{"min": 1, "max": 10}','ngay','com.htbb.pokemon.PhoBanLogic','co_dinh',1),
('pho_ban.phut_toi_da','pvp','Phó bản: thời gian tối đa (phút)','Hết giờ chưa hạ boss tầng 5 thì phòng đóng, không có thưởng ngày.','so','20','{"min": 5, "max": 60}','ngay','com.htbb.pokemon.PhoBanLogic','co_dinh',1),
('pho_ban.cap_toi_thieu','pvp','Phó bản: cấp tối thiểu','Mọi thành viên phải đạt cấp này; chênh cấp trong đội tối đa 10.','so','20','{"min": 1, "max": 100}','ngay','com.htbb.pokemon.PhoBanLogic','co_dinh',1),
('cup.bat','pvp','Cúp PvP tuần','0 = tạm đóng cúp (không nhận đăng ký, không ghép vòng). Tắt riêng một tuần: /admin/sukien (mã moi:cuptuan, tắt năm nay).','co','1','{}','ngay','real.CupGame','luon_bat',1),
('cup.gio_vong_dau','pvp','Cúp: giờ vòng 1 (Chủ nhật)','Giờ Việt Nam. Mỗi 10 phút một vòng; đóng đăng ký trước vòng 1 năm phút.','so','20','{"min": 8, "max": 22}','ngay','com.htbb.pokemon.CupLogic','co_dinh',1),
('cup.so_vong','pvp','Cúp: số vòng Thuỵ Sĩ','Vòng ghép tự động theo điểm; hai bên cùng thiết bị / IP / tài khoản không bao giờ gặp nhau.','so','5','{"min": 1, "max": 8}','ngay','com.htbb.pokemon.CupLogic','co_dinh',1),
('cup.so_doi_toi_thieu','pvp','Cúp: số đội tối thiểu','Ít hơn số này lúc đóng đăng ký thì cúp tuần đó hoãn (không phát thưởng).','so','4','{"min": 2, "max": 64}','ngay','com.htbb.pokemon.CupLogic','co_dinh',1)
ON DUPLICATE KEY UPDATE `nhom`=VALUES(`nhom`), `ten`=VALUES(`ten`), `mo_ta`=VALUES(`mo_ta`), `kieu`=VALUES(`kieu`),
  `mac_dinh`=VALUES(`mac_dinh`), `gioi_han`=VALUES(`gioi_han`), `ap_dung`=VALUES(`ap_dung`), `nguon`=VALUES(`nguon`),
  `lop`=VALUES(`lop`), `an`=VALUES(`an`);
