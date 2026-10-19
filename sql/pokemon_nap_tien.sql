-- NAP TIEN khong lam phat, khong tra tien de manh hon (docs/van-hanh.md).
-- Bang dung CHUNG giua website (HTBB-Web: server/utils/napTien.ts) va server game
-- (com.htbb.pokemon.NapTienGame / NapTienDb). Chi co tac dung khi sv.nap_tien=1 + sv.lau_dai=1
-- (server) va HTBB_NAP_BAT=true (web). Tao bang khong bat gi ca.
--
-- SQL portable (MariaDB/MySQL va NovaDB): khong CREATE TABLE .. LIKE, khong UPDATE co JOIN,
-- khong ON DUPLICATE KEY, khong ham thoi gian rieng (moi moc thoi gian do UNG DUNG ghi, han
-- the thang luu bang millis BIGINT de NovaDB/MariaDB khong lech mui gio). Chay lai an toan
-- (CREATE TABLE IF NOT EXISTS + INSERT IGNORE: khong ghi de chinh sua cua admin).
--
--     mysql htbb_db < sql/pokemon_nap_tien.sql
--     NovaDB: mysql -h 127.0.0.1 -P 3318 -u htbb -p htbb_db < sql/pokemon_nap_tien.sql
SET NAMES utf8;

-- ---------------------------------------------------------------- danh muc goi (web ban)
-- Admin sua tren /admin/naptien (tab Goi). Luat (NapTienLogic.kiemGoi / napTienThuan.ts):
-- goi luong = DUNG 10 luong / 1.000 d (khong khuyen mai %), the thang / ve mua khong kem luong.
CREATE TABLE IF NOT EXISTS `nap_goi` (
  `ma` varchar(20) NOT NULL,
  `ten` varchar(100) NOT NULL,
  `loai` varchar(12) NOT NULL COMMENT 'luong | the_thang | ve_mua',
  `gia_vnd` int NOT NULL,
  `luong` int NOT NULL DEFAULT 0,
  `mo_ta` varchar(500) NOT NULL DEFAULT '',
  `dang_ban` tinyint NOT NULL DEFAULT 1,
  `thu_tu` int NOT NULL DEFAULT 0,
  `cap_nhat_luc` datetime DEFAULT NULL,
  `admin_user` varchar(50) NOT NULL DEFAULT '',
  PRIMARY KEY (`ma`)
) DEFAULT CHARSET=utf8;

-- ---------------------------------------------------------------- don nap (so cai thanh toan)
-- trang_thai: cho_thanh_toan -> da_thanh_toan (webhook/admin, WEB) -> da_cong (SERVER GAME)
--             cho_thanh_toan -> that_bai (het han 24 gio) -> da_thanh_toan (tien den muon)
--             da_thanh_toan -> hoan_tien (hoan truoc khi cong)
--             da_cong -> cho_thu_hoi (hoan/chargeback sau khi cong, WEB) -> da_thu_hoi (SERVER)
-- Moi buoc la "UPDATE .. WHERE id=? AND trang_thai=<cu>" -> goi lap / goi song song chi 1 lan.
CREATE TABLE IF NOT EXISTS `nap_don` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ma_don` varchar(16) NOT NULL COMMENT 'HTxxxxxxxx = noi dung chuyen khoan',
  `khoa_idem` varchar(64) DEFAULT NULL COMMENT 'trinh duyet sinh moi lan bam (chong tao trung)',
  `user_id` int NOT NULL COMMENT 'account.team_user.id',
  `username` varchar(50) NOT NULL DEFAULT '',
  `char_id` int NOT NULL DEFAULT 0 COMMENT 'tob_char.id nhan (0 = nhan vat dau tien vao game)',
  `charname` varchar(50) NOT NULL DEFAULT '',
  `ma_goi` varchar(20) NOT NULL,
  `loai` varchar(12) NOT NULL,
  `ten_goi` varchar(100) NOT NULL DEFAULT '',
  `so_tien` int NOT NULL COMMENT 'VND phai tra (chot luc tao don)',
  `luong` int NOT NULL DEFAULT 0 COMMENT 'chot luc tao don - admin doi gia sau khong anh huong',
  `trang_thai` varchar(16) NOT NULL DEFAULT 'cho_thanh_toan',
  `nguon` varchar(16) NOT NULL DEFAULT 'web' COMMENT 'web | admin (cong tay)',
  `nha_cung_cap` varchar(16) DEFAULT NULL COMMENT 'sepay | casso | mock | admin',
  `ma_gd_ncc` varchar(64) DEFAULT NULL COMMENT 'ma giao dich ben cong thanh toan',
  `so_tien_nhan` int NOT NULL DEFAULT 0,
  `tao_luc` datetime NOT NULL,
  `thanh_toan_luc` datetime DEFAULT NULL,
  `cong_luc` datetime DEFAULT NULL,
  `char_cong` int NOT NULL DEFAULT 0 COMMENT 'tob_char.id thuc nhan',
  `da_luu_nv` tinyint NOT NULL DEFAULT 0 COMMENT '1 = server da saveCharAuto sau khi cong',
  `hoan_luc` datetime DEFAULT NULL,
  `ly_do` varchar(255) NOT NULL DEFAULT '',
  `admin_user` varchar(50) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_nap_don_ma` (`ma_don`),
  UNIQUE KEY `uq_nap_don_idem` (`user_id`,`khoa_idem`),
  UNIQUE KEY `uq_nap_don_gd` (`nha_cung_cap`,`ma_gd_ncc`),
  KEY `idx_nap_don_tt` (`trang_thai`),
  KEY `idx_nap_don_user` (`user_id`,`tao_luc`)
) DEFAULT CHARSET=utf8;

-- ---------------------------------------------------------------- vi theo tai khoan
CREATE TABLE IF NOT EXISTS `nap_vi` (
  `user_id` int NOT NULL,
  `tong_nap_vnd` bigint NOT NULL DEFAULT 0,
  `so_don` int NOT NULL DEFAULT 0,
  `no_luong` bigint NOT NULL DEFAULT 0 COMMENT 'thu hoi chua tru duoc (hoan tien sau khi da cong)',
  `khoa_nap` tinyint NOT NULL DEFAULT 0 COMMENT '1 = chargeback: khong tao don moi',
  `thuong_dau` tinyint NOT NULL DEFAULT 0 COMMENT '1 = da nhan thuong lan dau (trang tri)',
  `tran_tu_dat` bigint NOT NULL DEFAULT 0 COMMENT 'tran thang nguoi choi tu dat (0 = khong)',
  `tran_cho` bigint NOT NULL DEFAULT 0 COMMENT 'muc tran moi dang cho 72 gio',
  `tran_cho_luc` datetime DEFAULT NULL,
  `cap_nhat_luc` datetime DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) DEFAULT CHARSET=utf8;

-- ---------------------------------------------------------------- so cai luong (bat bien)
-- So luong duong = vao vi nhan vat, am = ra. ma_tham_chieu UNIQUE = chot idempotent cuoi cung.
CREATE TABLE IF NOT EXISTS `nap_so_cai` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL DEFAULT 0,
  `char_id` int NOT NULL DEFAULT 0,
  `loai` varchar(20) NOT NULL COMMENT 'thanh_toan | cong | the_thang_ngay | tieu | tra_no | thu_hoi | no | cho_ban | cho_mua',
  `so_luong` bigint NOT NULL DEFAULT 0,
  `so_tien` int NOT NULL DEFAULT 0,
  `ma_don` varchar(16) NOT NULL DEFAULT '',
  `ma_tham_chieu` varchar(80) NOT NULL,
  `ghi_chu` varchar(255) NOT NULL DEFAULT '',
  `tao_luc` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_nap_so_cai_tc` (`ma_tham_chieu`),
  KEY `idx_nap_so_cai_user` (`user_id`)
) DEFAULT CHARSET=utf8;

-- ---------------------------------------------------------------- nhat ky kiem toan
CREATE TABLE IF NOT EXISTS `nap_nhat_ky` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tac_nhan` varchar(60) NOT NULL COMMENT 'web | webhook:<ncc> | admin:<ten> | server',
  `hanh_dong` varchar(40) NOT NULL,
  `ma_don` varchar(16) NOT NULL DEFAULT '',
  `user_id` int NOT NULL DEFAULT 0,
  `chi_tiet` varchar(500) NOT NULL DEFAULT '',
  `ip` varchar(64) NOT NULL DEFAULT '',
  `tao_luc` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_nap_nk_don` (`ma_don`),
  KEY `idx_nap_nk_luc` (`tao_luc`)
) DEFAULT CHARSET=utf8;

-- ---------------------------------------------------------------- webhook tho (doi soat)
CREATE TABLE IF NOT EXISTS `nap_webhook` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nha_cung_cap` varchar(16) NOT NULL,
  `ma_gd` varchar(64) NOT NULL COMMENT 'ma giao dich ben cong (trung = goi lap, bo qua)',
  `chu_ky_hop_le` tinyint NOT NULL DEFAULT 0,
  `so_tien` int NOT NULL DEFAULT 0,
  `noi_dung` varchar(500) NOT NULL DEFAULT '',
  `ma_don` varchar(16) NOT NULL DEFAULT '',
  `ket_qua` varchar(120) NOT NULL DEFAULT '',
  `ip` varchar(64) NOT NULL DEFAULT '',
  `tao_luc` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_nap_webhook_gd` (`nha_cung_cap`,`ma_gd`),
  KEY `idx_nap_webhook_luc` (`tao_luc`)
) DEFAULT CHARSET=utf8;

-- ---------------------------------------------------------------- cau hinh + trang thai server
-- tran_*_vnd: tran nap / tai khoan (kep <= 5.000.000 d/thang trong code).
-- trang_thai.*: server game ghi moi vong (web doc de hien "server cong luong con song", mua ve).
CREATE TABLE IF NOT EXISTS `nap_cau_hinh` (
  `khoa` varchar(60) NOT NULL,
  `gia_tri` varchar(255) NOT NULL DEFAULT '',
  `cap_nhat_luc` datetime DEFAULT NULL,
  PRIMARY KEY (`khoa`)
) DEFAULT CHARSET=utf8;

-- ---------------------------------------------------------------- cua hang luong (trong game)
-- trao: CHI trang_tri | mo_rong_tui | doi_ten | tang_toc | dat_ten_pet (whitelist trong NapTienLogic.TRAO_HOP_LE).
-- co_chi_so PHAI = 0: server bo qua dong co co_chi_so <> 0 hoac sai luat (NapTienLogic.kiemMon).
-- nhom_xoay: 0 = luon ban; 1..4 = ban vao tuan (tuan % 4 == nhom_xoay - 1).
CREATE TABLE IF NOT EXISTS `nap_cua_hang` (
  `ma` varchar(40) NOT NULL,
  `ten` varchar(100) NOT NULL,
  `nhom` varchar(20) NOT NULL,
  `trao` varchar(20) NOT NULL,
  `gia_luong` int NOT NULL,
  `han_ngay` int NOT NULL DEFAULT 0 COMMENT '0 = vinh vien',
  `gioi_han` int NOT NULL DEFAULT 0 COMMENT 'so lan mua toi da / nhan vat (0 = khong gioi han); trao tang_toc: / TUAN',
  `nhom_xoay` int NOT NULL DEFAULT 0,
  `giao_dich_duoc` tinyint NOT NULL DEFAULT 0 COMMENT '1 = rao duoc tren Cho trang tri (co thue)',
  `co_chi_so` tinyint NOT NULL DEFAULT 0,
  `dang_ban` tinyint NOT NULL DEFAULT 1,
  `thu_tu` int NOT NULL DEFAULT 0,
  `mo_ta` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`ma`)
) DEFAULT CHARSET=utf8;

-- ---------------------------------------------------------------- so huu trang tri
CREATE TABLE IF NOT EXISTS `nap_so_huu` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `char_id` int NOT NULL,
  `ma` varchar(40) NOT NULL,
  `nguon` varchar(16) NOT NULL DEFAULT 'mua' COMMENT 'mua | ve_mua | lan_dau | cho | admin',
  `giao_dich_duoc` tinyint NOT NULL DEFAULT 0,
  `dang_dung` tinyint NOT NULL DEFAULT 0,
  `dang_rao` tinyint NOT NULL DEFAULT 0,
  `het_han_ms` bigint NOT NULL DEFAULT 0 COMMENT '0 = vinh vien',
  `tao_luc` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_nap_so_huu_char` (`char_id`)
) DEFAULT CHARSET=utf8;

-- ---------------------------------------------------------------- cho trang tri (co thue)
CREATE TABLE IF NOT EXISTS `nap_cho` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `so_huu_id` bigint NOT NULL,
  `ma` varchar(40) NOT NULL,
  `nguoi_ban` int NOT NULL,
  `ten_nguoi_ban` varchar(50) NOT NULL DEFAULT '',
  `gia_xu` bigint NOT NULL,
  `phi_dang` bigint NOT NULL DEFAULT 0 COMMENT '1% dot luc dang',
  `trang_thai` varchar(12) NOT NULL DEFAULT 'dang_ban' COMMENT 'dang_ban | da_ban | huy',
  `nguoi_mua` int NOT NULL DEFAULT 0,
  `thue` bigint NOT NULL DEFAULT 0 COMMENT '6% dot luc ban',
  `da_tra` tinyint NOT NULL DEFAULT 0 COMMENT '1 = da cong xu cho nguoi ban',
  `tao_luc` datetime NOT NULL,
  `ban_luc` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_nap_cho_tt` (`trang_thai`,`ma`),
  KEY `idx_nap_cho_ban` (`nguoi_ban`,`da_tra`)
) DEFAULT CHARSET=utf8;

-- ---------------------------------------------------------------- the thang / ve mua / dem mua
CREATE TABLE IF NOT EXISTS `nap_the_thang` (
  `char_id` int NOT NULL,
  `user_id` int NOT NULL DEFAULT 0,
  `het_han_ms` bigint NOT NULL DEFAULT 0,
  `ngay_nhan_cuoi` varchar(10) NOT NULL DEFAULT '',
  `so_ngay_da_nhan` int NOT NULL DEFAULT 0,
  `cap_nhat_luc` datetime DEFAULT NULL,
  PRIMARY KEY (`char_id`)
) DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `nap_ve_mua` (
  `char_id` int NOT NULL,
  `mua` int NOT NULL,
  `cao_cap` tinyint NOT NULL DEFAULT 0,
  `diem` int NOT NULL DEFAULT 0,
  `diem_ngay` int NOT NULL DEFAULT 0,
  `ngay` varchar(10) NOT NULL DEFAULT '',
  `bac_free` int NOT NULL DEFAULT 0 COMMENT 'bac free da nhan',
  `bac_cao_cap` int NOT NULL DEFAULT 0 COMMENT 'bac cao cap da nhan',
  PRIMARY KEY (`char_id`,`mua`)
) DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `nap_mua_dem` (
  `char_id` int NOT NULL,
  `ma` varchar(40) NOT NULL,
  `so_lan` int NOT NULL DEFAULT 0,
  `lan_cuoi_ms` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`char_id`,`ma`)
) DEFAULT CHARSET=utf8;

-- ---------------------------------------------------------------- du lieu mac dinh
-- Trung voi NapTienLogic.GOI_MAC_DINH / MON_MAC_DINH (test/KiemNapTien doi chieu).
INSERT IGNORE INTO `nap_goi` (`ma`,`ten`,`loai`,`gia_vnd`,`luong`,`mo_ta`,`dang_ban`,`thu_tu`) VALUES
('L20','200 lượng','luong',20000,200,'Tỷ giá cố định 10 lượng / 1.000 đ.',1,1),
('L50','500 lượng','luong',50000,500,'Tỷ giá cố định 10 lượng / 1.000 đ.',1,2),
('L100','1.000 lượng','luong',100000,1000,'Tỷ giá cố định 10 lượng / 1.000 đ.',1,3),
('L200','2.000 lượng','luong',200000,2000,'Tỷ giá cố định 10 lượng / 1.000 đ.',1,4),
('L500','5.000 lượng','luong',500000,5000,'Tỷ giá cố định 10 lượng / 1.000 đ.',1,5),
('THE_THANG','Thẻ tháng (30 ngày)','the_thang',50000,0,'20 lượng mỗi ngày đăng nhập (30 ngày, bỏ lỡ không cộng bù), +1 lượt Hầm ngục (chỉ điểm vé mùa), +2 ô tự nhặt, +1 trang tủ đồ trang trí, +1 giờ ủy thác luyện tập/ngày (trong giới hạn tăng tốc).',1,6),
('VE_MUA','Vé mùa cao cấp','ve_mua',99000,0,'Mở track cao cấp của mùa hiện tại: trang phục, biểu cảm, hào quang; vật liệu khoá đúng bằng track miễn phí. Tiến độ chỉ bằng chơi.',1,7);

INSERT IGNORE INTO `nap_cua_hang` (`ma`,`ten`,`nhom`,`trao`,`gia_luong`,`han_ngay`,`gioi_han`,`nhom_xoay`,`giao_dich_duoc`,`co_chi_so`,`dang_ban`,`thu_tu`) VALUES
('mo_rong_tui','Mở rộng hành trang (+42 ô)','tien_ich','mo_rong_tui',300,0,4,0,0,0,1,1),
('doi_ten','Đổi tên Pokémon','tien_ich','doi_ten',500,0,0,0,0,0,1,2),
('mau_ten_vang','Màu tên Vàng (30 ngày)','mau_ten','trang_tri',150,30,0,0,0,0,1,3),
('mau_ten_xanh','Màu tên Xanh ngọc (30 ngày)','mau_ten','trang_tri',150,30,0,0,0,0,1,4),
('khung_ten_bac','Khung tên Bạc','khung','trang_tri',200,0,1,0,1,0,1,5),
('skin_mu_rom','Skin: Mũ rơm','skin','trang_tri',600,0,1,1,1,0,1,6),
('hao_quang_lua','Hào quang Lửa','hao_quang','trang_tri',400,0,1,1,1,0,1,7),
('bieu_cam_vui','Biểu cảm: Vui vẻ','bieu_cam','trang_tri',80,0,1,1,1,0,1,8),
('skin_khan_quang','Skin: Khăn quàng mùa đông','skin','trang_tri',600,0,1,2,1,0,1,9),
('thu_cuoi_van_may','Skin thú cưỡi: Vân mây','thu_cuoi','trang_tri',500,0,1,2,1,0,1,10),
('nha_den_long','Trang trí nhà: Đèn lồng','nha','trang_tri',120,0,5,2,1,0,1,11),
('skin_kinh_ram','Skin: Kính râm','skin','trang_tri',600,0,1,3,1,0,1,12),
('hao_quang_nuoc','Hào quang Nước','hao_quang','trang_tri',400,0,1,3,1,0,1,13),
('bieu_cam_ngu','Biểu cảm: Buồn ngủ','bieu_cam','trang_tri',80,0,1,3,1,0,1,14),
('skin_no_xinh','Skin: Nơ xinh','skin','trang_tri',600,0,1,4,1,0,1,15),
('thu_cuoi_cau_vong','Skin thú cưỡi: Cầu vồng','thu_cuoi','trang_tri',500,0,1,4,1,0,1,16),
('nha_cay_canh','Trang trí nhà: Cây cảnh','nha','trang_tri',120,0,5,4,1,0,1,17),
('ve_exp_15','Vé EXP x1,5 (30 phút)','tang_toc','tang_toc',20,0,14,0,0,0,1,18),
('ve_exp_2','Vé EXP x2 (30 phút)','tang_toc','tang_toc',40,0,7,0,0,0,1,19),
('bua_the_luc','Bùa thể lực (+60 phút)','tang_toc','tang_toc',60,0,4,0,0,0,1,20),
('uy_thac','Phiếu ủy thác luyện tập (1 giờ)','tang_toc','tang_toc',15,0,17,0,0,0,1,21),
('keo_hiem','Kẹo hiếm','tang_toc','tang_toc',40,0,2,0,0,0,1,22),
('the_dat_ten_pet','Thẻ đặt tên Pokémon','tien_ich','dat_ten_pet',100,0,0,0,0,0,1,23);

INSERT IGNORE INTO `nap_cau_hinh` (`khoa`,`gia_tri`) VALUES
('tran_tuan_vnd','1000000'),
('tran_thang_vnd','2000000');
