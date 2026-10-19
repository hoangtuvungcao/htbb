-- Thiet ke dai han - giai doan 2 (docs/thiet-ke-game.md): BANG MOI cho game
-- dang chay. Chay SAU du_lieu/sql_nguon/pokemon_lau_dai.sql. Server cung tu tao (CREATE TABLE IF NOT EXISTS)
-- luc khoi dong khi sv.lau_dai=1 (data.DatabaseLauDai.taoBang), file nay de dung CSDL moi va
-- de tools/pokemon/xuat_sql_day_du.sh gom vao sql/htbb_day_du.sql.
-- SQL portable (chay duoc tren MariaDB/MySQL va NovaDB): khong tao bang bang cach chep bang khac,
-- khong UPDATE co JOIN, khong ham hieu thoi gian rieng cua MySQL, khong ten cheo CSDL.
SET NAMES utf8;

-- Chi so kinh te hang dem (THIET_KE muc 2.5). Mot dong / ngay, ghi lai neu chay 2 lan.
CREATE TABLE IF NOT EXISTS `tob_kinh_te_ngay` (
  `ngay` date NOT NULL,
  `so_nguoi` int NOT NULL DEFAULT 0 COMMENT 'nguoi hoat dong 7 ngay',
  `cap_trung_vi` int NOT NULL DEFAULT 0,
  `chi_so_gia` bigint NOT NULL DEFAULT 0 COMMENT 'xuMoiGio(cap trung vi)',
  `tong_xu` bigint NOT NULL DEFAULT 0 COMMENT 'gold + kho_xu',
  `tong_luong` bigint NOT NULL DEFAULT 0,
  `tong_luong_khoa` bigint NOT NULL DEFAULT 0,
  `cung_tien_thuc_x100` bigint NOT NULL DEFAULT 0 COMMENT 'gio thu nhap / nguoi x100',
  `top1_phan_tram_x100` int NOT NULL DEFAULT 0,
  `xu_vao` bigint NOT NULL DEFAULT 0 COMMENT 'Char.addXu trong ngay (tu lan ghi truoc)',
  `xu_ra` bigint NOT NULL DEFAULT 0 COMMENT 'Char.subXu trong ngay',
  `muc` tinyint NOT NULL DEFAULT 0 COMMENT '0 xanh 1 vang 2 do',
  `ghi_luc` datetime DEFAULT NULL,
  PRIMARY KEY (`ngay`)
) DEFAULT CHARSET=utf8;

-- Trang thai dai han moi nhan vat (com.htbb.pokemon.HoSoLauDai.maHoa): nang luong, tinh
-- thong, pity nang cap, lich dang nhap, ve chuong, pity shiny, giftcode thang, ham nguc, Elo.
CREATE TABLE IF NOT EXISTS `tob_char_lau_dai` (
  `id_char` int NOT NULL,
  `du_lieu` varchar(2000) NOT NULL DEFAULT '',
  `cap_nhat` datetime DEFAULT NULL,
  PRIMARY KEY (`id_char`)
) DEFAULT CHARSET=utf8;

-- Tien do nhiem vu loai moi (tuan, Pokedex, su pham, ham nguc, thanh tuu, quay lai).
CREATE TABLE IF NOT EXISTS `tob_nhiem_vu_lau_dai` (
  `id_char` int NOT NULL,
  `ma` varchar(32) NOT NULL COMMENT 'ma trong data_quest_lau_dai (vd T-1.01)',
  `tien_do` int NOT NULL DEFAULT 0,
  `trang_thai` tinyint NOT NULL DEFAULT 0 COMMENT '0 dang lam 1 xong chua nhan 2 da nhan',
  `tuan` int NOT NULL DEFAULT 0 COMMENT 'chi so tuan (NhiemVuLauDai.tuan) khi nhan',
  PRIMARY KEY (`id_char`,`ma`)
) DEFAULT CHARSET=utf8;

-- Dem mua theo tuan (ve EXP bang luong khoa 1/tuan, data_shop_gia_lau_dai.gioi_han_tuan).
CREATE TABLE IF NOT EXISTS `tob_mua_tuan` (
  `id_char` int NOT NULL,
  `id_shop` int NOT NULL,
  `tuan` int NOT NULL,
  `so_luong` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id_char`,`id_shop`,`tuan`)
) DEFAULT CHARSET=utf8;

-- Boss the gioi: dong gop tung nguoi moi tran (thuong theo % dong gop, khong theo don cuoi).
CREATE TABLE IF NOT EXISTS `tob_boss_dong_gop` (
  `lan` bigint NOT NULL COMMENT 'millis luc boss xuat hien',
  `id_char` int NOT NULL,
  `phan_nghin` int NOT NULL DEFAULT 0,
  `hang` int NOT NULL DEFAULT 0,
  `bac_thuong` tinyint NOT NULL DEFAULT 0 COMMENT '0 khong 1 hop tham gia 2 hop vang',
  `ghi_luc` datetime DEFAULT NULL,
  PRIMARY KEY (`lan`,`id_char`)
) DEFAULT CHARSET=utf8;

-- Ham nguc bi an: tang cao nhat moi ngay (bang xep hang tang).
CREATE TABLE IF NOT EXISTS `tob_ham_nguc` (
  `ngay` int NOT NULL COMMENT 'yyyyMMdd',
  `id_char` int NOT NULL,
  `tang` int NOT NULL DEFAULT 0,
  `ghi_luc` datetime DEFAULT NULL,
  PRIMARY KEY (`ngay`,`id_char`)
) DEFAULT CHARSET=utf8;

-- Thang PvP theo mua (Elo, reset mem moi 13 tuan).
CREATE TABLE IF NOT EXISTS `tob_pvp_mua` (
  `mua` int NOT NULL,
  `id_char` int NOT NULL,
  `elo` int NOT NULL DEFAULT 1000,
  `thang` int NOT NULL DEFAULT 0,
  `thua` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`mua`,`id_char`)
) DEFAULT CHARSET=utf8;

-- Su pham: moi tro mot thay.
CREATE TABLE IF NOT EXISTS `tob_su_pham` (
  `id_tro` int NOT NULL,
  `id_thay` int NOT NULL,
  `bat_dau` datetime DEFAULT NULL,
  `moc` varchar(64) NOT NULL DEFAULT '' COMMENT 'moc da thuong, vd 20,40',
  PRIMARY KEY (`id_tro`)
) DEFAULT CHARSET=utf8;

-- Sao luu EXP cu truoc khi doi sang bang EXP moi (mot lan, xem docs/kien-truc.md
-- muc "Giai doan 2"). id = tob_char.id.
CREATE TABLE IF NOT EXISTS `tob_char_xp_cu` (
  `id` int NOT NULL,
  `xp` bigint NOT NULL DEFAULT 0,
  `lastLv` int NOT NULL DEFAULT 0,
  `xp_moi` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8;

-- So shiny da thay moi loai (Pokedex bac 3) - khong ALTER tob_pokedex.
CREATE TABLE IF NOT EXISTS `tob_pokedex_shiny` (
  `id_char` int NOT NULL,
  `dex` smallint NOT NULL,
  `so_shiny` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id_char`,`dex`)
) DEFAULT CHARSET=utf8;

-- Giftcode AN TOAN (yeu cau 17/10/2026, bat khi sv.lau_dai=1; real.GiftcodeAnToan). Web admin tao ma
-- bang cach INSERT vao day. ma: chu thuong a-z0-9_- 3..32 ky tu. phan_thuong: CHI "LUONGKHOA:n",
-- "GEM:id:n" (luon khoa), "ITEM:id:n" (n <= 10, vat pham khoa), ngan bang dau phay; gia tri (luong
-- khoa = 1/don vi, GEM = 1/vien, ITEM = 5/mon) <= cau_hinh giftcode.gia_tri_toi_da (30). han NULL =
-- khong han. bat = 0 de tat ma.
CREATE TABLE IF NOT EXISTS `tob_giftcode` (
  `ma` varchar(32) NOT NULL,
  `phan_thuong` varchar(500) NOT NULL,
  `han` datetime DEFAULT NULL,
  `tong_luot` int NOT NULL DEFAULT 1,
  `da_dung` int NOT NULL DEFAULT 0,
  `cap_toi_thieu` int NOT NULL DEFAULT 1,
  `bat` tinyint NOT NULL DEFAULT 1,
  `ghi_chu` varchar(255) NOT NULL DEFAULT '',
  `tao_boi` varchar(50) DEFAULT NULL,
  `tao_luc` datetime DEFAULT NULL,
  PRIMARY KEY (`ma`)
) DEFAULT CHARSET=utf8;

-- Nhat ky giftcode an toan: moi TAI KHOAN 1 lan / ma (khoa chinh), thang = yyyyMM (ngan sach thang).
CREATE TABLE IF NOT EXISTS `tob_giftcode_log` (
  `ma` varchar(32) NOT NULL,
  `user_id` int NOT NULL,
  `id_char` int NOT NULL,
  `charname` varchar(30) NOT NULL DEFAULT '',
  `phan_thuong` varchar(500) NOT NULL DEFAULT '',
  `gia_tri` int NOT NULL DEFAULT 0,
  `thang` int NOT NULL DEFAULT 0,
  `luc` datetime DEFAULT NULL,
  PRIMARY KEY (`ma`,`user_id`)
) DEFAULT CHARSET=utf8;

-- Qua hoat dong + lich dang nhap 7 ngay + lich thang, theo TAI KHOAN (real.QuaHoatDong).
CREATE TABLE IF NOT EXISTS `tob_qua_online` (
  `user_id` int NOT NULL,
  `ngay` int NOT NULL DEFAULT 0 COMMENT 'yyyyMMdd cua giay/moc',
  `giay` int NOT NULL DEFAULT 0 COMMENT 'giay hoat dong (khong AFK) trong ngay',
  `moc` int NOT NULL DEFAULT 0 COMMENT 'bitmask moc 15/30/60/120 phut da nhan',
  `chuoi_buoc` int NOT NULL DEFAULT 0,
  `chuoi_ngay` int NOT NULL DEFAULT 0,
  `thang` int NOT NULL DEFAULT 0,
  `ngay_thang` int NOT NULL DEFAULT 0,
  `thang_da_thuong` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`user_id`)
) DEFAULT CHARSET=utf8;

-- Chiem thanh tuan (real.ChiemGymLauDai): moi khoan phat theo dong gop.
CREATE TABLE IF NOT EXISTS `tob_chiem_thanh_tuan` (
  `lan` bigint NOT NULL,
  `id_char` int NOT NULL,
  `id_clan` int NOT NULL DEFAULT -1,
  `diem` bigint NOT NULL DEFAULT 0,
  `thuong` varchar(255) NOT NULL DEFAULT '',
  `luc` datetime DEFAULT NULL,
  PRIMARY KEY (`lan`,`id_char`)
) DEFAULT CHARSET=utf8;
