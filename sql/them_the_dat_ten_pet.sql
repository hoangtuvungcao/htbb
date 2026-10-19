-- THE DAT TEN POKEMON (0.9.6, nhanh ten-pet) - migration cho CSDL dang chay. Chay lai bao nhieu lan cung duoc
-- (CREATE TABLE IF NOT EXISTS, INSERT IGNORE giu gia tri admin da sua, UPDATE dat lai dung hinh/ten vat pham).
-- Khong dung "INSERT ... SELECT ... ON DUPLICATE KEY UPDATE col=col" (hong tren MariaDB 10.3).
-- Cung khoi nay nam trong sql/htbb_day_du.sql. Chay SAU sql/pokemon_vat_pham_lau_dai.sql neu chay lai tep do
-- (tep do DROP data_vat_pham_lau_dai / data_cua_hang_lau_dai). Thiet ke: docs/thiet-ke-game.md muc 4.8.
--   mysql htbb_db < sql/them_the_dat_ten_pet.sql
SET NAMES utf8;

-- Bang dong chi so cua the (chi so + trong so chon + he so % ap len muc gia tri trong cau hinh
-- dat_ten_pet.muc_phan_nghin / dat_ten_pet.trong_so_muc). Ma chi so chi gom: hp, tan_cong, phong_thu, chi_mang, ne_tranh
-- (com.htbb.pokemon.DatTenPet.MA_HOP_LE; dong ma khac bi may chu bo qua). bat = 0 tat dong.
CREATE TABLE IF NOT EXISTS `data_dong_the_ten` (
  `id` int NOT NULL,
  `chi_so` varchar(16) NOT NULL,
  `ten_hien` varchar(40) NOT NULL DEFAULT '',
  `trong_so` int NOT NULL DEFAULT 1,
  `he_so` int NOT NULL DEFAULT 100 COMMENT '% ap len muc (1..100): 50 = chi mang / ne bang nua muc',
  `bat` tinyint NOT NULL DEFAULT 1,
  `ghi_chu` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8;
INSERT IGNORE INTO `data_dong_the_ten` (`id`,`chi_so`,`ten_hien`,`trong_so`,`he_so`,`bat`,`ghi_chu`) VALUES
(1,'hp','HP',30,100,1,'+1..3% HP toi da, trong chi_so.tran_phan_tram_cong'),
(2,'tan_cong','Tấn công',25,100,1,'+1..3% tan cong, trong chi_so.tran_phan_tram_cong'),
(3,'phong_thu','Phòng thủ',25,100,1,'+1..3% phong thu, trong chi_so.tran_phan_tram_cong'),
(4,'chi_mang','Chí mạng',10,50,1,'+0,5..1,5% ti le chi mang, qua tran 40% giam dan'),
(5,'ne_tranh','Né tránh',10,50,1,'+0,5..1,5% ne, qua tran 30% / PvP 20%');

-- Tu cam trong ten pet. kieu 'tu' = ca tu (co dau, khong phan biet hoa thuong); 'chuoi' = chuoi con cua ban bo dau,
-- bo dau cach, doi so giong chu (DatTenPet.khung: "D.m.m", "đ m m", "dmm" deu bi chan).
CREATE TABLE IF NOT EXISTS `data_tu_cam_ten` (
  `tu` varchar(40) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'chu thuong; utf8_bin: đụ / đù khac nhau',
  `kieu` varchar(8) NOT NULL DEFAULT 'tu',
  `bat` tinyint NOT NULL DEFAULT 1,
  PRIMARY KEY (`tu`)
) DEFAULT CHARSET=utf8;
INSERT IGNORE INTO `data_tu_cam_ten` (`tu`,`kieu`,`bat`) VALUES
('lồn','tu',1),('cặc','tu',1),('buồi','tu',1),('địt','tu',1),('đụ','tu',1),('đéo','tu',1),('đĩ','tu',1),('cứt','tu',1),
('dái','tu',1),('nứng','tu',1),('đỉ','tu',1),('đù','tu',1),('chó đẻ','tu',1),('mẹ mày','tu',1),('bú cu','tu',1),
('admin','tu',1),('gm','tu',1),('quản trị','tu',1),
('dmm','chuoi',1),('dcm','chuoi',1),('dkm','chuoi',1),('vcl','chuoi',1),('vkl','chuoi',1),('cmm','chuoi',1),
('clgt','chuoi',1),('dmcs','chuoi',1),('ditme','chuoi',1),('dume','chuoi',1),('duma','chuoi',1),('dmeo','chuoi',1),
('fuck','chuoi',1),('shit','chuoi',1),('bitch','chuoi',1),('cunt','chuoi',1),('pussy','chuoi',1),('nigger','chuoi',1),
('nigga','chuoi',1),('dick','chuoi',1);

-- Ten + dong cua tung pet (khoa id_pet = tob_pet.id). May chu cung tu tao (real.DatTenPetGame.taoBang).
CREATE TABLE IF NOT EXISTS `tob_pet_dat_ten` (
  `id_pet` int NOT NULL,
  `id_char` int NOT NULL,
  `ten` varchar(40) NOT NULL,
  `chi_so` varchar(16) NOT NULL DEFAULT '',
  `gia_tri` int NOT NULL DEFAULT 0 COMMENT 'phan nghin',
  `so_lan` int NOT NULL DEFAULT 0,
  `cap_nhat` datetime DEFAULT NULL,
  PRIMARY KEY (`id_pet`),
  KEY `idx_pet_dat_ten_char` (`id_char`)
) DEFAULT CHARSET=utf8;

-- Nhat ky kinh te: moi lan the bi tru (ok) hoac hoan (hoan_the). /admin co the doc truc tiep.
CREATE TABLE IF NOT EXISTS `tob_log_dat_ten` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `luc` datetime NOT NULL,
  `id_char` int NOT NULL,
  `charname` varchar(30) NOT NULL DEFAULT '',
  `id_pet` int NOT NULL,
  `dex` int NOT NULL DEFAULT 0,
  `ten_cu` varchar(40) NOT NULL DEFAULT '',
  `dong_cu` varchar(32) NOT NULL DEFAULT '',
  `ten_moi` varchar(40) NOT NULL DEFAULT '',
  `dong_moi` varchar(32) NOT NULL DEFAULT '',
  `id_item` int NOT NULL DEFAULT 0,
  `ket_qua` varchar(32) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `idx_log_dat_ten_char` (`id_char`),
  KEY `idx_log_dat_ten_luc` (`luc`)
) DEFAULT CHARSET=utf8;

-- Vat pham 1261 "The dat ten Pokemon" (type 28, icon map/icon_pokemon/502.png = 14502). Moi the vao tui deu KHOA
-- (Char.createItemSoLuong lock = 1): khong ky gui, khong ban NPC. price = 0 (khong phai nguon xu).
INSERT IGNORE INTO `data_item` (`id`,`name`,`type`,`style`,`he`,`gender`,`level`,`durable`,`atb0`,`atb1`,`atb2`,`atb3`,`atb4`,`atb5`,`atb6`,`atb7`,`atb8`,`atb9`,`price`,`clazz`,`xstart`,`ystart`,`colortype`,`nloan`,`idUpLevel`,`namein`,`ideff`,`pricecu`) VALUES
(1261,'Thẻ đặt tên Pokémon',28,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,14502,0,-1,0,-1,'Thẻ đặt tên Pokémon',-1,0);
UPDATE `data_item` SET `name`='Thẻ đặt tên Pokémon', `namein`='Thẻ đặt tên Pokémon', `type`=28, `xstart`=14502, `price`=0
WHERE `id`=1261;

-- Cong dung (real.VatPhamDungLauDai: hieu_ung "dat_ten" -> real.DatTenPetGame).
INSERT IGNORE INTO `data_vat_pham_lau_dai` (`id_item`,`ma`,`ten`,`nhom`,`hieu_ung`,`gioi_han`,`cap_tu`,`cap_den`) VALUES
(1261,'the_dat_ten_pet','Thẻ đặt tên Pokémon','tien_ich','dat_ten:1','1 thẻ / 1 lần đặt tên',1,100);

-- Nguon KIEM DUOC (cung vat pham, cung ngan sach voi ban luong): doi Tem nghien cuu (nhiem vu nghien cuu / su kien)
-- hoac luong khoa (Kecleon), moi dong 1 lan / tuan.
INSERT IGNORE INTO `data_cua_hang_lau_dai` (`id`,`nhom`,`ten`,`chi_phi`,`thuong`,`cap`,`ngay_mo`,`lop`,`gioi_han_tuan`) VALUES
(128,'Đổi Tem nghiên cứu','Thẻ đặt tên Pokémon','item:1054x8','ITEM:1261:1',10,0,-1,1),
(129,'Lượng khoá (Kecleon)','Thẻ đặt tên Pokémon','luong_khoa:40','ITEM:1261:1',10,0,-1,1);

-- Nguon TRA PHI: Cua hang luong, nhom Tien ich. Gia that = cau hinh dat_ten_pet.gia_luong (mac dinh 100 luong),
-- so lan / tuan = dat_ten_pet.mua_tuan_toi_da (mac dinh 3); gioi_han cua dong PHAI = 0 (NapTienLogic.kiemDatTen).
INSERT IGNORE INTO `nap_cua_hang` (`ma`,`ten`,`nhom`,`trao`,`gia_luong`,`han_ngay`,`gioi_han`,`nhom_xoay`,`giao_dich_duoc`,`co_chi_so`,`dang_ban`,`thu_tu`) VALUES
('the_dat_ten_pet','Thẻ đặt tên Pokémon','tien_ich','dat_ten_pet',100,0,0,0,0,0,1,23);
