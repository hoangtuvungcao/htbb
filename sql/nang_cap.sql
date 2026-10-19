-- NANG CAP CSDL DANG CHAY (htbb_db) len cung schema + du lieu thiet ke voi sql/htbb_day_du.sql. CHAY LAI DUOC.
-- Tep VIET TAY (khong con bo sinh). Chay:  sudo bash /opt/htbb/vps/nang_cap_csdl.sh   (sao luu -> nap tep nay -> kiem)
--                                   hoac:  mysql -u root htbb_db < sql/nang_cap.sql
-- CSDL moi khong can tep nay: sql/htbb_day_du.sql da gom san.
-- Luat (server/test/KiemDuLieuSql kiem): bang nguoi choi chi duoc THEM bang / THEM cot / NOI cot / THEM chi muc
-- (CREATE TABLE IF NOT EXISTS, ADD COLUMN IF NOT EXISTS, MODIFY noi cot, CREATE INDEX IF NOT EXISTS, INSERT IGNORE);
-- khong DROP / DELETE / UPDATE du lieu nguoi choi. Phan 2 (bang thiet ke may chu doc luc khoi dong) phai giong TUNG
-- DONG sql/htbb_day_du.sql. Them bang / cot moi vao htbb_day_du.sql thi them lenh tuong ung vao day (xem sql/README.md).
SET NAMES utf8mb4;

-- ======== 0. MOI bang cua sql/htbb_day_du.sql (htbb_db): CREATE TABLE IF NOT EXISTS - bang da co giu nguyen (ca du lieu).
--          Cung DDL voi ban day du; cot moi cua bang da co them o phan 1 (ADD COLUMN IF NOT EXISTS).

CREATE TABLE IF NOT EXISTS `5h_active` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `userID` int(11) NOT NULL,
  `username` varchar(255) NOT NULL,
  `time_end` varchar(50) DEFAULT '-1',
  PRIMARY KEY (`id`),
  KEY `idx_5h_active_username` (`username`),
  KEY `idx_5h_active_userid` (`userID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `5h_notify` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `owner` varchar(255) NOT NULL,
  `content` text NOT NULL,
  `time_end` int(11) NOT NULL DEFAULT 1702874482,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `5h_systems` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `command` varchar(255) DEFAULT NULL,
  `status` int(255) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `admin_msg` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `msg` varchar(1000) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `bad_word` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `word` varchar(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=170 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `board_created` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `xu` bigint(20) unsigned NOT NULL DEFAULT 0,
  `luong` bigint(20) unsigned NOT NULL DEFAULT 0,
  `luongK` bigint(20) unsigned NOT NULL DEFAULT 0,
  `svID` int(11) NOT NULL DEFAULT -1,
  `username` varchar(45) NOT NULL,
  `ve` int(10) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `Index_2` (`username`) USING BTREE,
  KEY `Index_3` (`svID`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci ROW_FORMAT=DYNAMIC;
CREATE TABLE IF NOT EXISTS `board_naptien` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `xu` bigint(20) unsigned NOT NULL DEFAULT 0,
  `luong` bigint(20) unsigned NOT NULL DEFAULT 0,
  `svID` int(11) NOT NULL DEFAULT -1,
  `username` varchar(45) NOT NULL,
  `ve` int(10) unsigned NOT NULL DEFAULT 0,
  `luongKhoa` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `Index_2` (`username`) USING BTREE,
  KEY `Index_3` (`svID`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci ROW_FORMAT=DYNAMIC;
CREATE TABLE IF NOT EXISTS `cau_hinh` (
  `khoa` varchar(64) NOT NULL COMMENT 'ten setting, khop hang so K_* trong src/server/CauHinh.java',
  `gia_tri` varchar(255) NOT NULL DEFAULT '',
  `sua_boi` varchar(50) DEFAULT NULL COMMENT 'ten dang nhap admin (web) hoac "game" (bang setting trong game)',
  `sua_luc` datetime DEFAULT NULL,
  PRIMARY KEY (`khoa`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `cau_hinh_ap_dung` (
  `may_chu` varchar(40) NOT NULL,
  `phien_ban` bigint(20) NOT NULL DEFAULT 0,
  `ap_dung_luc` datetime DEFAULT NULL,
  `khoi_dong_luc` datetime DEFAULT NULL,
  `nhip_luc` datetime DEFAULT NULL,
  `so_khoa` int(11) NOT NULL DEFAULT 0,
  `so_loi` int(11) NOT NULL DEFAULT 0,
  `loi` text DEFAULT NULL,
  `cho_khoi_dong_lai` text DEFAULT NULL,
  `thay_doi_cuoi` text DEFAULT NULL,
  PRIMARY KEY (`may_chu`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `cau_hinh_danh_muc` (
  `khoa` varchar(64) NOT NULL,
  `nhom` varchar(20) NOT NULL,
  `ten` varchar(160) NOT NULL DEFAULT '',
  `mo_ta` text NOT NULL,
  `kieu` varchar(12) NOT NULL,
  `mac_dinh` varchar(255) NOT NULL DEFAULT '',
  `gioi_han` text NOT NULL,
  `ap_dung` varchar(16) NOT NULL,
  `nhay` tinyint(4) NOT NULL DEFAULT 0,
  `khoa_prod` tinyint(4) NOT NULL DEFAULT 0,
  `chi_doc` tinyint(4) NOT NULL DEFAULT 0,
  `bang` varchar(16) NOT NULL DEFAULT 'cau_hinh',
  `dung_o` varchar(10) NOT NULL DEFAULT 'server',
  `nguon` varchar(255) NOT NULL DEFAULT '',
  `thu_tu` int(11) NOT NULL DEFAULT 0,
  `lop` varchar(10) NOT NULL DEFAULT 'chinh',
  `an` tinyint(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`khoa`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `cau_hinh_hieu_luc` (
  `may_chu` varchar(40) NOT NULL,
  `khoa` varchar(64) NOT NULL,
  `gia_tri` varchar(255) DEFAULT NULL,
  `nguon` varchar(12) NOT NULL DEFAULT '',
  `cho_khoi_dong_lai` varchar(255) DEFAULT NULL,
  `loi` varchar(255) DEFAULT NULL,
  `cap_nhat_luc` datetime DEFAULT NULL,
  PRIMARY KEY (`may_chu`,`khoa`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `cau_hinh_khoi_dong_lai` (
  `id` tinyint(4) NOT NULL,
  `trang_thai` varchar(12) NOT NULL DEFAULT '',
  `giay` int(11) NOT NULL DEFAULT 60,
  `ly_do` varchar(200) NOT NULL DEFAULT '',
  `yeu_cau_boi` varchar(50) DEFAULT NULL,
  `yeu_cau_luc` datetime DEFAULT NULL,
  `bat_dau_luc` datetime DEFAULT NULL,
  `thoat_luc` datetime DEFAULT NULL,
  `xong_luc` datetime DEFAULT NULL,
  `may_chu` varchar(40) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `cau_hinh_lich_su` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `khoa` varchar(64) NOT NULL,
  `bang` varchar(16) NOT NULL DEFAULT 'cau_hinh',
  `gia_tri_cu` varchar(255) DEFAULT NULL,
  `gia_tri_moi` varchar(255) DEFAULT NULL,
  `nguoi_sua` varchar(50) DEFAULT NULL,
  `nguon` varchar(20) NOT NULL DEFAULT '',
  `ly_do` varchar(255) NOT NULL DEFAULT '',
  `phien_ban` bigint(20) NOT NULL DEFAULT 0,
  `luc` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_chls_khoa` (`khoa`),
  KEY `idx_chls_luc` (`luc`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `cau_hinh_nhom` (
  `ma` varchar(20) NOT NULL,
  `ten` varchar(160) NOT NULL DEFAULT '',
  `mo_ta` text NOT NULL,
  `thu_tu` int(11) NOT NULL DEFAULT 0,
  `an` tinyint(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`ma`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `cau_hinh_phien_ban` (
  `id` tinyint(4) NOT NULL,
  `phien_ban` bigint(20) NOT NULL DEFAULT 0,
  `sua_luc` datetime DEFAULT NULL,
  `sua_boi` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `data_attribute` (
  `id` int(10) unsigned NOT NULL,
  `name` varchar(45) NOT NULL,
  `idcolor` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `ispercent` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `decript` varchar(100) NOT NULL,
  `namein` varchar(45) DEFAULT '""',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_cap_do_lau_dai` (
  `cap` int(11) NOT NULL,
  `exp_len_cap` bigint(20) NOT NULL,
  `gio_tich_luy_phan_nghin` int(11) NOT NULL,
  `mob_exp` int(11) NOT NULL,
  `quai_moi_gio` int(11) NOT NULL,
  `xu_moi_quai` int(11) NOT NULL,
  `exp_cu` bigint(20) NOT NULL,
  PRIMARY KEY (`cap`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_cua_hang_lau_dai` (
  `id` int(11) NOT NULL,
  `nhom` varchar(64) NOT NULL,
  `ten` varchar(255) NOT NULL,
  `chi_phi` varchar(255) NOT NULL,
  `thuong` varchar(255) NOT NULL,
  `cap` int(11) NOT NULL DEFAULT 1,
  `ngay_mo` int(11) NOT NULL DEFAULT 0,
  `lop` int(11) NOT NULL DEFAULT -1,
  `gioi_han_tuan` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_hst_bung_phat` (
  `map_id` int(11) NOT NULL,
  `id_quai` int(11) NOT NULL,
  PRIMARY KEY (`map_id`,`id_quai`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_hst_ho_cau` (
  `map_id` int(11) NOT NULL,
  `ho_id` int(11) NOT NULL,
  `so_o` int(11) NOT NULL,
  `x` int(11) NOT NULL,
  `y` int(11) NOT NULL,
  PRIMARY KEY (`map_id`,`ho_id`,`x`,`y`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_hst_thay_quai` (
  `id_goc` int(11) NOT NULL,
  `id_thay` int(11) NOT NULL,
  `trong_so` int(11) NOT NULL,
  `hiem` int(11) NOT NULL,
  `thoi_tiet` int(11) NOT NULL,
  `ngay_dem` int(11) NOT NULL,
  `mat_na_thang` int(11) NOT NULL,
  PRIMARY KEY (`id_goc`,`id_thay`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_hst_vung` (
  `map_id` int(11) NOT NULL,
  `vung` int(11) NOT NULL,
  `ten` varchar(64) NOT NULL,
  PRIMARY KEY (`map_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_hst_vung_loai` (
  `vung` int(11) NOT NULL,
  `dex` int(11) NOT NULL,
  PRIMARY KEY (`vung`,`dex`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_item` (
  `id` int(4) unsigned NOT NULL,
  `name` text NOT NULL,
  `type` int(1) unsigned NOT NULL,
  `style` int(1) unsigned NOT NULL DEFAULT 0,
  `he` int(1) NOT NULL DEFAULT 0,
  `gender` int(1) unsigned NOT NULL DEFAULT 0,
  `level` int(1) unsigned NOT NULL DEFAULT 0,
  `durable` int(2) unsigned NOT NULL DEFAULT 0,
  `atb0` int(2) NOT NULL DEFAULT 0,
  `atb1` int(2) NOT NULL DEFAULT 0,
  `atb2` int(2) NOT NULL DEFAULT 0,
  `atb3` int(2) NOT NULL DEFAULT 0,
  `atb4` int(2) NOT NULL DEFAULT 0,
  `atb5` int(2) NOT NULL DEFAULT 0,
  `atb6` int(2) NOT NULL DEFAULT 0,
  `atb7` int(2) NOT NULL DEFAULT 0,
  `atb8` int(2) NOT NULL DEFAULT 0,
  `atb9` int(2) NOT NULL DEFAULT 0,
  `price` int(4) NOT NULL DEFAULT 0,
  `clazz` int(1) NOT NULL DEFAULT -1,
  `xstart` int(10) unsigned NOT NULL DEFAULT 0,
  `ystart` int(10) unsigned NOT NULL DEFAULT 0,
  `colortype` int(10) NOT NULL DEFAULT -1,
  `nloan` int(10) unsigned NOT NULL DEFAULT 0,
  `idUpLevel` int(11) NOT NULL DEFAULT -1,
  `namein` varchar(45) DEFAULT NULL,
  `ideff` int(11) DEFAULT -1,
  `pricecu` int(10) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_kinh_te_lau_dai` (
  `khoa` varchar(64) NOT NULL,
  `gia_tri` varchar(64) NOT NULL,
  `gia_tri_cu` varchar(64) NOT NULL,
  `ghi_chu` varchar(255) NOT NULL,
  PRIMARY KEY (`khoa`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_monster` (
  `id` int(4) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `style` int(1) unsigned NOT NULL,
  `he` int(1) NOT NULL,
  `level` int(1) NOT NULL,
  `maxhp` int(2) NOT NULL,
  `attack` int(2) NOT NULL DEFAULT 0,
  `defent` int(2) NOT NULL DEFAULT 0,
  `accurate` int(1) NOT NULL DEFAULT 0,
  `dodge` int(2) NOT NULL DEFAULT 0,
  `rcvxp` int(4) unsigned NOT NULL DEFAULT 0,
  `rcvgold` int(2) unsigned NOT NULL DEFAULT 0,
  `active` int(2) unsigned NOT NULL DEFAULT 0,
  `palate` int(10) unsigned NOT NULL DEFAULT 0,
  `spalate` int(10) unsigned NOT NULL DEFAULT 0,
  `isuse` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `move` tinyint(3) unsigned NOT NULL DEFAULT 1,
  `speed` int(10) unsigned NOT NULL DEFAULT 1,
  `h` int(10) unsigned NOT NULL DEFAULT 20,
  `w` int(10) unsigned NOT NULL DEFAULT 20,
  `xcenter` int(10) unsigned NOT NULL DEFAULT 8,
  `ycenter` int(10) unsigned NOT NULL DEFAULT 8,
  `height` int(10) unsigned NOT NULL DEFAULT 20,
  `isnew` int(10) unsigned NOT NULL DEFAULT 0,
  `dex` smallint(6) NOT NULL DEFAULT 0 COMMENT 'Pokemon: so Pokedex cua template',
  `shiny` tinyint(4) NOT NULL DEFAULT 0 COMMENT 'Pokemon: 1 = shiny',
  `he2` tinyint(4) NOT NULL DEFAULT -1 COMMENT 'Pokemon: he thu hai, -1 = khong co',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=179 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_monster_lau_dai` (
  `id` int(11) NOT NULL,
  `ten` varchar(45) NOT NULL,
  `cap` int(11) NOT NULL,
  `maxhp_cu` int(11) NOT NULL,
  `maxhp` int(11) NOT NULL,
  `attack_cu` int(11) NOT NULL,
  `attack` int(11) NOT NULL,
  `defent_cu` int(11) NOT NULL,
  `defent` int(11) NOT NULL,
  `rcvxp_cu` int(11) NOT NULL,
  `rcvxp` int(11) NOT NULL,
  `rcvgold_cu` int(11) NOT NULL,
  `rcvgold` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_nang_cap_lau_dai` (
  `bac` int(11) NOT NULL,
  `ten_bac` varchar(20) NOT NULL,
  `cap_bac` int(11) NOT NULL,
  `cong` int(11) NOT NULL,
  `ti_le` int(11) NOT NULL,
  `pity_moi_lan` int(11) NOT NULL,
  `xu` bigint(20) NOT NULL,
  `da` int(11) NOT NULL,
  `bua_luong_khoa` int(11) NOT NULL,
  `mat_cap` tinyint(4) NOT NULL,
  `chi_so_phan_tram` int(11) NOT NULL,
  PRIMARY KEY (`bac`,`cong`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_nhiem_vu_kieu` (
  `ma` varchar(32) NOT NULL,
  `ten` varchar(64) NOT NULL,
  `co_che` varchar(255) NOT NULL,
  `du_phong` varchar(4) NOT NULL,
  `trang_thai` varchar(16) NOT NULL,
  `so_dong` int(11) NOT NULL,
  PRIMARY KEY (`ma`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_nhiem_vu_noi_dung` (
  `ma` varchar(32) NOT NULL,
  `loai` varchar(32) NOT NULL,
  `nhom` varchar(32) NOT NULL,
  `nhan_vat` text NOT NULL,
  `map_id` varchar(8) NOT NULL,
  `toa_do` varchar(16) NOT NULL,
  `dieu_kien` text NOT NULL,
  `du_phong` text NOT NULL,
  `mo_ta` text NOT NULL,
  `muc_tieu_text` text NOT NULL,
  `hoi_thoai_bat_dau` text NOT NULL,
  `hoi_thoai_dang_lam` text NOT NULL,
  `hoi_thoai_xong` text NOT NULL,
  `goi_y` text NOT NULL,
  `dong_thuong` text NOT NULL,
  `thuong_khac` text NOT NULL,
  `nhanh_lua_chon` text NOT NULL,
  `an` tinyint(4) NOT NULL DEFAULT 0,
  `dieu_kien_mo` text NOT NULL,
  `dap_an` text NOT NULL,
  `phut` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`ma`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_noi_dung_bo` (
  `bac` bigint(20) NOT NULL,
  `ten_bo` varchar(255) NOT NULL,
  `vung` varchar(255) NOT NULL,
  `so_mon` bigint(20) NOT NULL,
  `chi_so` varchar(255) NOT NULL,
  `phan_tram` bigint(20) NOT NULL,
  `mo_ta` varchar(255) NOT NULL,
  PRIMARY KEY (`bac`,`so_mon`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_noi_dung_boss_the_gioi` (
  `tuan` bigint(20) NOT NULL,
  `loai` varchar(255) NOT NULL,
  `ten_nam1` varchar(255) NOT NULL,
  `ten_nam2` varchar(255) NOT NULL,
  `template_quai` bigint(20) NOT NULL,
  `he` varchar(255) NOT NULL,
  `cap` bigint(20) NOT NULL,
  `hp` bigint(20) NOT NULL,
  `tan_cong` bigint(20) NOT NULL,
  `phong_thu` bigint(20) NOT NULL,
  `so_nguoi_chuan` bigint(20) NOT NULL,
  `phut_muc_tieu` bigint(20) NOT NULL,
  `no_sau_phut` bigint(20) NOT NULL,
  `lan_moi_ngay` bigint(20) NOT NULL,
  `vat_pham_dac_trung` varchar(255) NOT NULL,
  `vat_pham_dac_trung_nam2` varchar(255) NOT NULL,
  `template_nam2` bigint(20) NOT NULL,
  PRIMARY KEY (`tuan`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_noi_dung_che_tao` (
  `thanh_pham` varchar(255) NOT NULL,
  `ten` varchar(255) NOT NULL,
  `bac` bigint(20) NOT NULL,
  `cap` bigint(20) NOT NULL,
  `da` bigint(20) NOT NULL,
  `nguyen_lieu_phu` varchar(255) NOT NULL,
  `so_nl_phu` bigint(20) NOT NULL,
  `anh_xa_nl_phu` varchar(255) NOT NULL,
  `xu` bigint(20) NOT NULL,
  `npc` varchar(255) NOT NULL,
  PRIMARY KEY (`thanh_pham`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_noi_dung_chieu` (
  `dong` bigint(20) NOT NULL,
  `ten_dong` varchar(255) NOT NULL,
  `so_hieu` bigint(20) NOT NULL,
  `ten` varchar(255) NOT NULL,
  `he` varchar(255) NOT NULL,
  `kieu` varchar(255) NOT NULL,
  `vai_tro` varchar(255) NOT NULL,
  `giai_doan` bigint(20) NOT NULL,
  `cap_mo` bigint(20) NOT NULL,
  `cap_moi_cap_chieu` varchar(255) NOT NULL,
  `phan_tram_cap1` bigint(20) NOT NULL,
  `phan_tram_cap10` bigint(20) NOT NULL,
  `so_don` bigint(20) NOT NULL,
  `phan_tram_moi_don_cap10` bigint(20) NOT NULL,
  `hoi_chieu_giay` double NOT NULL,
  `pp` bigint(20) NOT NULL,
  `mp_phan_tram` bigint(20) NOT NULL,
  `trang_thai` varchar(255) NOT NULL,
  PRIMARY KEY (`dong`,`so_hieu`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_noi_dung_chieu_nhanh_eevee` (
  `dex` bigint(20) NOT NULL,
  `ten` varchar(255) NOT NULL,
  `he` varchar(255) NOT NULL,
  `vai_tro` varchar(255) NOT NULL,
  `chieu_o2` varchar(255) NOT NULL,
  `chieu_o8` varchar(255) NOT NULL,
  `chieu_o10` varchar(255) NOT NULL,
  `cap_mo` bigint(20) NOT NULL,
  `ghi_chu` varchar(255) NOT NULL,
  PRIMARY KEY (`dex`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_noi_dung_chieu_ttk` (
  `dong` bigint(20) NOT NULL,
  `ten_dong` varchar(255) NOT NULL,
  `giai_doan` bigint(20) NOT NULL,
  `cap` bigint(20) NOT NULL,
  `he_so_xoay_chieu` double NOT NULL,
  `ttk_giay` double NOT NULL,
  `ttk_muc_tieu` double NOT NULL,
  `ti_le` double NOT NULL,
  `chi_so_suc_manh` double NOT NULL,
  PRIMARY KEY (`dong`,`giai_doan`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_noi_dung_cua_hang` (
  `stt` bigint(20) NOT NULL,
  `cua_hang` varchar(255) NOT NULL,
  `vat_pham` varchar(255) NOT NULL,
  `ten` varchar(255) NOT NULL,
  `gia` bigint(20) NOT NULL,
  `tien` varchar(255) NOT NULL,
  `cap` bigint(20) NOT NULL,
  `ngay_mo` bigint(20) NOT NULL,
  `gioi_han` varchar(255) NOT NULL,
  `gio_thu_nhap` double NOT NULL,
  PRIMARY KEY (`stt`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_noi_dung_gym` (
  `stt` bigint(20) NOT NULL,
  `ma_nhiem_vu` varchar(255) NOT NULL,
  `huy_hieu` varchar(255) NOT NULL,
  `dia_danh` varchar(255) NOT NULL,
  `he` varchar(255) NOT NULL,
  `cap` bigint(20) NOT NULL,
  `template_quai` bigint(20) NOT NULL,
  `ten_boss` varchar(255) NOT NULL,
  `hp` bigint(20) NOT NULL,
  `tan_cong` bigint(20) NOT NULL,
  `phong_thu` bigint(20) NOT NULL,
  `ttk_giay` bigint(20) NOT NULL,
  `no_sau_phut` bigint(20) NOT NULL,
  `thuong_lan_dau` varchar(255) NOT NULL,
  `thuong_tuan` varchar(255) NOT NULL,
  `bac_thuong` bigint(20) NOT NULL,
  PRIMARY KEY (`stt`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_noi_dung_ngoc_he` (
  `he` bigint(20) NOT NULL,
  `ten` varchar(255) NOT NULL,
  `cap` bigint(20) NOT NULL,
  `chi_so_phan_tram` bigint(20) NOT NULL,
  `gio_thu_nhap` double NOT NULL,
  `xu_vi_du_cap60` bigint(20) NOT NULL,
  `phan_ra_da` bigint(20) NOT NULL,
  `anh_xa` varchar(255) NOT NULL,
  `trang_thai` varchar(255) NOT NULL,
  `icon` bigint(20) NOT NULL,
  PRIMARY KEY (`he`,`cap`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_noi_dung_nguyen_lieu` (
  `ma` varchar(255) NOT NULL,
  `ten` varchar(255) NOT NULL,
  `nhom` varchar(255) NOT NULL,
  `anh_xa` varchar(255) NOT NULL,
  `dung_cho` varchar(255) NOT NULL,
  `nguon` varchar(255) NOT NULL,
  `gia` bigint(20) NOT NULL,
  `tien` varchar(255) NOT NULL,
  PRIMARY KEY (`ma`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_noi_dung_phan_ra` (
  `nguon` varchar(255) NOT NULL,
  `bac` bigint(20) NOT NULL,
  `da` bigint(20) NOT NULL,
  `xu` bigint(20) NOT NULL,
  `them` varchar(255) NOT NULL,
  PRIMARY KEY (`nguon`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_noi_dung_quai_dai_cap` (
  `dai` varchar(255) NOT NULL,
  `cap_tu` bigint(20) NOT NULL,
  `cap_den` bigint(20) NOT NULL,
  `loai_co_san` varchar(255) NOT NULL,
  `so_loai` bigint(20) NOT NULL,
  `so_he` bigint(20) NOT NULL,
  `he` varchar(255) NOT NULL,
  `moi_truong` varchar(255) NOT NULL,
  `du` bigint(20) NOT NULL,
  `de_xuat_moi` varchar(1000) NOT NULL,
  `so_de_xuat` bigint(20) NOT NULL,
  `template_nam2` varchar(1000) NOT NULL,
  `so_template_nam2` bigint(20) NOT NULL,
  PRIMARY KEY (`dai`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_noi_dung_quai_moi` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) NOT NULL,
  `style` bigint(20) NOT NULL,
  `he` bigint(20) NOT NULL,
  `level` bigint(20) NOT NULL,
  `maxhp` bigint(20) NOT NULL,
  `attack` bigint(20) NOT NULL,
  `defent` bigint(20) NOT NULL,
  `accurate` bigint(20) NOT NULL,
  `dodge` bigint(20) NOT NULL,
  `rcvxp` bigint(20) NOT NULL,
  `rcvgold` bigint(20) NOT NULL,
  `active` bigint(20) NOT NULL,
  `palate` bigint(20) NOT NULL,
  `spalate` bigint(20) NOT NULL,
  `isuse` bigint(20) NOT NULL,
  `move` bigint(20) NOT NULL,
  `speed` bigint(20) NOT NULL,
  `h` bigint(20) NOT NULL,
  `w` bigint(20) NOT NULL,
  `xcenter` bigint(20) NOT NULL,
  `ycenter` bigint(20) NOT NULL,
  `height` bigint(20) NOT NULL,
  `isnew` bigint(20) NOT NULL,
  `dex` bigint(20) NOT NULL,
  `shiny` bigint(20) NOT NULL,
  `he2` bigint(20) NOT NULL,
  `phan_loai` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_noi_dung_quai_tinh_anh` (
  `id_goc` bigint(20) NOT NULL,
  `ten` varchar(255) NOT NULL,
  `cap` bigint(20) NOT NULL,
  `ti_le_xuat_hien` varchar(255) NOT NULL,
  `kich_thuoc_phan_tram` bigint(20) NOT NULL,
  `hp` bigint(20) NOT NULL,
  `tan_cong` bigint(20) NOT NULL,
  `phong_thu` bigint(20) NOT NULL,
  `exp` bigint(20) NOT NULL,
  `nhan_roi` bigint(20) NOT NULL,
  `ttk_giay` double NOT NULL,
  PRIMARY KEY (`id_goc`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_noi_dung_roi` (
  `stt` bigint(20) NOT NULL,
  `nguon` varchar(255) NOT NULL,
  `khoa_nguon` varchar(255) NOT NULL,
  `vat_pham` varchar(255) NOT NULL,
  `ten` varchar(255) NOT NULL,
  `ppm` bigint(20) NOT NULL,
  `sl_min` bigint(20) NOT NULL,
  `sl_max` bigint(20) NOT NULL,
  `do_hiem` varchar(255) NOT NULL,
  `khoa` bigint(20) NOT NULL,
  `het_han` bigint(20) NOT NULL,
  PRIMARY KEY (`stt`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_noi_dung_su_kien_vat_pham` (
  `stt` bigint(20) NOT NULL,
  `tuan` bigint(20) NOT NULL,
  `su_kien` varchar(255) NOT NULL,
  `su_kien_nam2` varchar(255) NOT NULL,
  `loai` varchar(255) NOT NULL,
  `vat_pham` varchar(255) NOT NULL,
  `anh_xa` varchar(255) NOT NULL,
  `so_luong_toi_da_tuan` bigint(20) NOT NULL,
  `gio_thu_nhap_moi_cai` double NOT NULL,
  `ngan_sach_gio` double NOT NULL,
  `phan_xu_phan_tram` bigint(20) NOT NULL,
  `khoa` bigint(20) NOT NULL,
  `het_han_khi_ket_thuc` bigint(20) NOT NULL,
  `doi_lay` varchar(255) NOT NULL,
  PRIMARY KEY (`stt`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_noi_dung_trang_bi` (
  `ma` varchar(255) NOT NULL,
  `bac` bigint(20) NOT NULL,
  `ten_bac` varchar(255) NOT NULL,
  `o` varchar(255) NOT NULL,
  `type_item` bigint(20) NOT NULL,
  `dong` varchar(255) NOT NULL,
  `ten` varchar(255) NOT NULL,
  `id_item` bigint(20) NOT NULL,
  `trang_thai` varchar(255) NOT NULL,
  `cap_yeu_cau` bigint(20) NOT NULL,
  `cap_item` bigint(20) NOT NULL,
  `ngay_mo` bigint(20) NOT NULL,
  `tran_cap_khi_mo` bigint(20) NOT NULL,
  `so_o` bigint(20) NOT NULL,
  `tan_cong` bigint(20) NOT NULL,
  `phong_thu` bigint(20) NOT NULL,
  `hp` bigint(20) NOT NULL,
  `chi_so_phu` varchar(255) NOT NULL,
  `he` varchar(255) NOT NULL,
  `gia_xu` bigint(20) NOT NULL,
  `ban_npc_xu` bigint(20) NOT NULL,
  `phan_ra` varchar(255) NOT NULL,
  `nguon` varchar(255) NOT NULL,
  `ghi_chu` varchar(255) NOT NULL,
  PRIMARY KEY (`ma`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_noi_dung_trang_tri` (
  `stt` bigint(20) NOT NULL,
  `mua` bigint(20) NOT NULL,
  `ten_mua` varchar(255) NOT NULL,
  `ngay_mo` bigint(20) NOT NULL,
  `tran_cap` bigint(20) NOT NULL,
  `kieu` varchar(255) NOT NULL,
  `ten` varchar(255) NOT NULL,
  `gia` bigint(20) NOT NULL,
  `tien` varchar(255) NOT NULL,
  `nguon` varchar(255) NOT NULL,
  `chi_so` bigint(20) NOT NULL,
  `trang_thai` varchar(255) NOT NULL,
  `icon` bigint(20) NOT NULL,
  PRIMARY KEY (`stt`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_noi_dung_vat_lieu_nang_cap` (
  `bac` bigint(20) NOT NULL,
  `ten_bac` varchar(255) NOT NULL,
  `cong` bigint(20) NOT NULL,
  `ti_le` bigint(20) NOT NULL,
  `da` bigint(20) NOT NULL,
  `nguyen_lieu_phu` varchar(255) NOT NULL,
  `so_nl_phu` bigint(20) NOT NULL,
  `xu` bigint(20) NOT NULL,
  `bua_luong_khoa` bigint(20) NOT NULL,
  `ky_vong_lan` double NOT NULL,
  PRIMARY KEY (`bac`,`cong`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_noi_dung_vat_pham` (
  `ma` varchar(255) NOT NULL,
  `ten` varchar(255) NOT NULL,
  `nhom` varchar(255) NOT NULL,
  `mo_ta` varchar(255) NOT NULL,
  `cap_tu` bigint(20) NOT NULL,
  `cap_den` bigint(20) NOT NULL,
  `hieu_ung` varchar(255) NOT NULL,
  `so_dung_moi_gio` double NOT NULL,
  `phan_ngan_sach` double NOT NULL,
  `gia` bigint(20) NOT NULL,
  `tien` varchar(255) NOT NULL,
  `gia_tren_thu_nhap_gio` double NOT NULL,
  `nguon` varchar(255) NOT NULL,
  `gioi_han` varchar(255) NOT NULL,
  `anh_xa` varchar(255) NOT NULL,
  `trang_thai` varchar(255) NOT NULL,
  `icon` bigint(20) NOT NULL,
  PRIMARY KEY (`ma`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_potion` (
  `id` smallint(6) NOT NULL,
  `idImage` smallint(6) DEFAULT NULL,
  `delay` smallint(6) DEFAULT NULL,
  `isTrade` tinyint(1) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `name2` varchar(255) DEFAULT NULL,
  `price` int(11) DEFAULT NULL,
  `recovered` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `data_quest` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `npcrcv` int(10) NOT NULL,
  `npcres` int(10) NOT NULL,
  `lv` int(10) unsigned NOT NULL DEFAULT 1,
  `mainsub` tinyint(3) unsigned NOT NULL DEFAULT 0 COMMENT '0-main-1sub',
  `content` varchar(1000) NOT NULL COMMENT 'noi dung oi chuyen nhan nhiem vu',
  `scontent` varchar(500) NOT NULL DEFAULT '32000' COMMENT 'mo ta ngan gon nhiem vu',
  `rescontent` varchar(500) NOT NULL DEFAULT '32000',
  `idItemget` varchar(45) NOT NULL DEFAULT '32000',
  `idMonskill` varchar(45) NOT NULL DEFAULT '32000',
  `type` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `nitemget` varchar(45) NOT NULL DEFAULT '32000' COMMENT 'so luong item can lay tuong ung',
  `nmonskill` varchar(45) NOT NULL DEFAULT '32000' COMMENT 'so luong quai can giet tuong ung',
  `gold` bigint(20) unsigned NOT NULL DEFAULT 0,
  `exp` bigint(20) unsigned NOT NULL DEFAULT 0,
  `potiongift` varchar(45) NOT NULL DEFAULT '32000',
  `npotion` varchar(45) NOT NULL DEFAULT '32000',
  `itemgift` varchar(45) NOT NULL DEFAULT '32000',
  `deltaLv` int(10) unsigned NOT NULL DEFAULT 0,
  `infogift` varchar(200) DEFAULT NULL,
  `infofinish` varchar(200) NOT NULL DEFAULT 'Nhiệm vụ hoàn thành',
  `pactive` int(10) unsigned NOT NULL DEFAULT 0,
  `luong` int(10) DEFAULT 0,
  `luongLock` int(10) DEFAULT 100,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=50 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_quest_lau_dai` (
  `ma` varchar(32) NOT NULL,
  `id_so` bigint(20) NOT NULL,
  `loai` varchar(32) NOT NULL,
  `nam` bigint(20) NOT NULL,
  `vung` varchar(255) NOT NULL,
  `cung` varchar(255) NOT NULL,
  `chuong` varchar(32) NOT NULL,
  `buoc` varchar(32) NOT NULL,
  `ten` varchar(255) NOT NULL,
  `npc_giao` varchar(32) NOT NULL,
  `ten_npc` varchar(255) NOT NULL,
  `dia_danh` varchar(255) NOT NULL,
  `cap_toi_thieu` bigint(20) NOT NULL,
  `ngay_mo` bigint(20) NOT NULL,
  `truoc` varchar(255) NOT NULL,
  `kieu_muc_tieu` varchar(32) NOT NULL,
  `muc_tieu_id` varchar(32) NOT NULL,
  `muc_tieu_ten` varchar(255) NOT NULL,
  `so_luong` bigint(20) NOT NULL,
  `quai_roi` varchar(32) NOT NULL,
  `lap_lai` varchar(32) NOT NULL,
  `gioi_han` varchar(32) NOT NULL,
  `exp` bigint(20) NOT NULL,
  `xu` bigint(20) NOT NULL,
  `luong_khoa` bigint(20) NOT NULL,
  `luong` bigint(20) NOT NULL,
  `vat_pham` varchar(255) NOT NULL,
  `huy_hieu` varchar(255) NOT NULL,
  `dong_co_tuong_thich` bigint(20) NOT NULL,
  `ghi_chu` varchar(255) NOT NULL,
  PRIMARY KEY (`ma`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_quest_moi` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `npcrcv` int(10) NOT NULL,
  `npcres` int(10) NOT NULL,
  `lv` int(10) unsigned NOT NULL DEFAULT 1,
  `mainsub` tinyint(3) unsigned NOT NULL DEFAULT 0 COMMENT '0-main-1sub',
  `content` varchar(1000) NOT NULL COMMENT 'noi dung oi chuyen nhan nhiem vu',
  `scontent` varchar(500) NOT NULL DEFAULT '32000' COMMENT 'mo ta ngan gon nhiem vu',
  `rescontent` varchar(500) NOT NULL DEFAULT '32000',
  `idItemget` varchar(45) NOT NULL DEFAULT '32000',
  `idMonskill` varchar(45) NOT NULL DEFAULT '32000',
  `type` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `nitemget` varchar(45) NOT NULL DEFAULT '32000' COMMENT 'so luong item can lay tuong ung',
  `nmonskill` varchar(45) NOT NULL DEFAULT '32000' COMMENT 'so luong quai can giet tuong ung',
  `gold` bigint(20) unsigned NOT NULL DEFAULT 0,
  `exp` bigint(20) unsigned NOT NULL DEFAULT 0,
  `potiongift` varchar(45) NOT NULL DEFAULT '32000',
  `npotion` varchar(45) NOT NULL DEFAULT '32000',
  `itemgift` varchar(45) NOT NULL DEFAULT '32000',
  `deltaLv` int(10) unsigned NOT NULL DEFAULT 0,
  `infogift` varchar(200) DEFAULT NULL,
  `infofinish` varchar(200) NOT NULL DEFAULT 'Nhiệm vụ hoàn thành',
  `pactive` int(10) unsigned NOT NULL DEFAULT 0,
  `luong` int(10) DEFAULT 0,
  `luongLock` int(10) DEFAULT 100,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5013 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_shop` (
  `id` int(11) NOT NULL,
  `idImg` int(11) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `decript` varchar(255) DEFAULT NULL,
  `value` int(11) DEFAULT NULL,
  `price` int(11) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `tab` int(11) DEFAULT NULL,
  `sell` int(11) DEFAULT NULL,
  `priceSale` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `data_shop_gia_lau_dai` (
  `id_shop` int(11) NOT NULL,
  `ten` varchar(100) NOT NULL,
  `gia_cu` bigint(20) NOT NULL,
  `tien_cu` tinyint(4) NOT NULL,
  `gia_moi` bigint(20) NOT NULL,
  `tien_moi` tinyint(4) NOT NULL COMMENT '0 luong,1 xu,2 luong khoa,9 ngung ban',
  `gioi_han_tuan` int(11) NOT NULL,
  `ly_do` varchar(255) NOT NULL,
  PRIMARY KEY (`id_shop`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_su_kien_nam` (
  `tuan` int(11) NOT NULL,
  `thang` int(11) NOT NULL,
  `ten_nam1` varchar(255) NOT NULL,
  `ten_nam2` varchar(255) NOT NULL,
  `loai` varchar(255) NOT NULL,
  `co_che` varchar(255) NOT NULL,
  `so_ngay` int(11) NOT NULL,
  `ma_su_kien` varchar(255) NOT NULL,
  `tai_dung` varchar(255) NOT NULL,
  `ngan_sach_gio` decimal(5,2) NOT NULL,
  `phan_xu_phan_tram` int(11) NOT NULL,
  `phan_thuong` varchar(255) NOT NULL,
  `moc_giu_chan` varchar(255) NOT NULL,
  PRIMARY KEY (`tuan`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_vat_pham_lau_dai` (
  `id_item` int(11) NOT NULL,
  `ma` varchar(64) NOT NULL,
  `ten` varchar(255) NOT NULL,
  `nhom` varchar(32) NOT NULL,
  `hieu_ung` varchar(255) NOT NULL,
  `gioi_han` varchar(32) NOT NULL DEFAULT '',
  `cap_tu` int(11) NOT NULL DEFAULT 1,
  `cap_den` int(11) NOT NULL DEFAULT 100,
  PRIMARY KEY (`id_item`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_vat_pham_nhiem_vu` (
  `id` int(11) NOT NULL,
  `ten` varchar(64) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `dieu_khien_server` (
  `id` tinyint(3) unsigned NOT NULL,
  `muon_chay` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1 = muon server dang chay, 0 = muon tat han',
  `gio_hen` datetime DEFAULT NULL COMMENT 'NULL = ap dung ngay; co gio = doi den gio nay watcher moi ap dung',
  `da_ap_dung` tinyint(1) NOT NULL DEFAULT 1 COMMENT '0 = dang cho watcher xu ly, 1 = da xong (khong con viec gi cho watcher)',
  `trang_thai_thuc_te` varchar(20) DEFAULT NULL COMMENT 'watcher tu ghi lai: active/inactive/failed/... (ket qua systemctl is-active)',
  `lan_kiem_cuoi` datetime DEFAULT NULL COMMENT 'nhip tim: lan watcher chay gan nhat - web dung de biet watcher con song khong',
  `yeu_cau_boi` varchar(50) DEFAULT NULL COMMENT 'ten admin vua bam nut',
  `yeu_cau_luc` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `even_online_game` (
  `playerId` int(11) NOT NULL,
  `lastLoginTime` datetime DEFAULT NULL,
  `totalOnlineTimeInSeconds` bigint(20) DEFAULT 0,
  `lastResetTime` datetime DEFAULT NULL,
  `nhanQuaOnline` text DEFAULT '0,0,0,0',
  PRIMARY KEY (`playerId`),
  UNIQUE KEY `unique_player` (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `giftcode` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `giftcode` varchar(255) NOT NULL,
  `xu` int(11) NOT NULL DEFAULT 0,
  `luong` int(11) NOT NULL DEFAULT 0,
  `luongLock` int(11) NOT NULL DEFAULT 0,
  `item` varchar(255) DEFAULT NULL,
  `expire` int(11) NOT NULL DEFAULT 0,
  `limit_use` int(11) NOT NULL DEFAULT 0,
  `type` int(11) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
CREATE TABLE IF NOT EXISTS `giftcode_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `giftcode` text DEFAULT NULL,
  `player` text DEFAULT NULL,
  `userId` int(11) NOT NULL DEFAULT 0,
  `item` text DEFAULT NULL,
  `xu` int(11) DEFAULT 0,
  `luong` int(11) DEFAULT 0,
  `luongK` int(11) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_giftcode_userid` (`giftcode`(191),`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `history_win_xoso` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_user` int(11) NOT NULL,
  `money_win` bigint(20) NOT NULL DEFAULT 0,
  `time_win` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_user` (`id_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `nap_cau_hinh` (
  `khoa` varchar(60) NOT NULL,
  `gia_tri` varchar(255) NOT NULL DEFAULT '',
  `cap_nhat_luc` datetime DEFAULT NULL,
  PRIMARY KEY (`khoa`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `nap_cho` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `so_huu_id` bigint(20) NOT NULL,
  `ma` varchar(40) NOT NULL,
  `nguoi_ban` int(11) NOT NULL,
  `ten_nguoi_ban` varchar(50) NOT NULL DEFAULT '',
  `gia_xu` bigint(20) NOT NULL,
  `phi_dang` bigint(20) NOT NULL DEFAULT 0 COMMENT '1% dot luc dang',
  `trang_thai` varchar(12) NOT NULL DEFAULT 'dang_ban' COMMENT 'dang_ban | da_ban | huy',
  `nguoi_mua` int(11) NOT NULL DEFAULT 0,
  `thue` bigint(20) NOT NULL DEFAULT 0 COMMENT '6% dot luc ban',
  `da_tra` tinyint(4) NOT NULL DEFAULT 0 COMMENT '1 = da cong xu cho nguoi ban',
  `tao_luc` datetime NOT NULL,
  `ban_luc` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_nap_cho_tt` (`trang_thai`,`ma`),
  KEY `idx_nap_cho_ban` (`nguoi_ban`,`da_tra`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `nap_cua_hang` (
  `ma` varchar(40) NOT NULL,
  `ten` varchar(100) NOT NULL,
  `nhom` varchar(20) NOT NULL,
  `trao` varchar(20) NOT NULL,
  `gia_luong` int(11) NOT NULL,
  `han_ngay` int(11) NOT NULL DEFAULT 0 COMMENT '0 = vinh vien',
  `gioi_han` int(11) NOT NULL DEFAULT 0 COMMENT 'so lan mua toi da / nhan vat (0 = khong gioi han); trao tang_toc: / TUAN',
  `nhom_xoay` int(11) NOT NULL DEFAULT 0,
  `giao_dich_duoc` tinyint(4) NOT NULL DEFAULT 0 COMMENT '1 = rao duoc tren Cho trang tri (co thue)',
  `co_chi_so` tinyint(4) NOT NULL DEFAULT 0,
  `dang_ban` tinyint(4) NOT NULL DEFAULT 1,
  `thu_tu` int(11) NOT NULL DEFAULT 0,
  `mo_ta` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`ma`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `nap_don` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ma_don` varchar(16) NOT NULL COMMENT 'HTxxxxxxxx = noi dung chuyen khoan',
  `khoa_idem` varchar(64) DEFAULT NULL COMMENT 'trinh duyet sinh moi lan bam (chong tao trung)',
  `user_id` int(11) NOT NULL COMMENT 'account.team_user.id',
  `username` varchar(50) NOT NULL DEFAULT '',
  `char_id` int(11) NOT NULL DEFAULT 0 COMMENT 'tob_char.id nhan (0 = nhan vat dau tien vao game)',
  `charname` varchar(50) NOT NULL DEFAULT '',
  `ma_goi` varchar(20) NOT NULL,
  `loai` varchar(12) NOT NULL,
  `ten_goi` varchar(100) NOT NULL DEFAULT '',
  `so_tien` int(11) NOT NULL COMMENT 'VND phai tra (chot luc tao don)',
  `luong` int(11) NOT NULL DEFAULT 0 COMMENT 'chot luc tao don - admin doi gia sau khong anh huong',
  `trang_thai` varchar(16) NOT NULL DEFAULT 'cho_thanh_toan',
  `nguon` varchar(16) NOT NULL DEFAULT 'web' COMMENT 'web | admin (cong tay)',
  `nha_cung_cap` varchar(16) DEFAULT NULL COMMENT 'sepay | casso | mock | admin',
  `ma_gd_ncc` varchar(64) DEFAULT NULL COMMENT 'ma giao dich ben cong thanh toan',
  `so_tien_nhan` int(11) NOT NULL DEFAULT 0,
  `tao_luc` datetime NOT NULL,
  `thanh_toan_luc` datetime DEFAULT NULL,
  `cong_luc` datetime DEFAULT NULL,
  `char_cong` int(11) NOT NULL DEFAULT 0 COMMENT 'tob_char.id thuc nhan',
  `da_luu_nv` tinyint(4) NOT NULL DEFAULT 0 COMMENT '1 = server da saveCharAuto sau khi cong',
  `hoan_luc` datetime DEFAULT NULL,
  `ly_do` varchar(255) NOT NULL DEFAULT '',
  `admin_user` varchar(50) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_nap_don_ma` (`ma_don`),
  UNIQUE KEY `uq_nap_don_idem` (`user_id`,`khoa_idem`),
  UNIQUE KEY `uq_nap_don_gd` (`nha_cung_cap`,`ma_gd_ncc`),
  KEY `idx_nap_don_tt` (`trang_thai`),
  KEY `idx_nap_don_user` (`user_id`,`tao_luc`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `nap_goi` (
  `ma` varchar(20) NOT NULL,
  `ten` varchar(100) NOT NULL,
  `loai` varchar(12) NOT NULL COMMENT 'luong | the_thang | ve_mua',
  `gia_vnd` int(11) NOT NULL,
  `luong` int(11) NOT NULL DEFAULT 0,
  `mo_ta` varchar(500) NOT NULL DEFAULT '',
  `dang_ban` tinyint(4) NOT NULL DEFAULT 1,
  `thu_tu` int(11) NOT NULL DEFAULT 0,
  `cap_nhat_luc` datetime DEFAULT NULL,
  `admin_user` varchar(50) NOT NULL DEFAULT '',
  PRIMARY KEY (`ma`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `nap_mua_dem` (
  `char_id` int(11) NOT NULL,
  `ma` varchar(40) NOT NULL,
  `so_lan` int(11) NOT NULL DEFAULT 0,
  `lan_cuoi_ms` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`char_id`,`ma`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `nap_nhat_ky` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tac_nhan` varchar(60) NOT NULL COMMENT 'web | webhook:<ncc> | admin:<ten> | server',
  `hanh_dong` varchar(40) NOT NULL,
  `ma_don` varchar(16) NOT NULL DEFAULT '',
  `user_id` int(11) NOT NULL DEFAULT 0,
  `chi_tiet` varchar(500) NOT NULL DEFAULT '',
  `ip` varchar(64) NOT NULL DEFAULT '',
  `tao_luc` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_nap_nk_don` (`ma_don`),
  KEY `idx_nap_nk_luc` (`tao_luc`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `nap_so_cai` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL DEFAULT 0,
  `char_id` int(11) NOT NULL DEFAULT 0,
  `loai` varchar(20) NOT NULL COMMENT 'thanh_toan | cong | the_thang_ngay | tieu | tra_no | thu_hoi | no | cho_ban | cho_mua',
  `so_luong` bigint(20) NOT NULL DEFAULT 0,
  `so_tien` int(11) NOT NULL DEFAULT 0,
  `ma_don` varchar(16) NOT NULL DEFAULT '',
  `ma_tham_chieu` varchar(80) NOT NULL,
  `ghi_chu` varchar(255) NOT NULL DEFAULT '',
  `tao_luc` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_nap_so_cai_tc` (`ma_tham_chieu`),
  KEY `idx_nap_so_cai_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `nap_so_huu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `char_id` int(11) NOT NULL,
  `ma` varchar(40) NOT NULL,
  `nguon` varchar(16) NOT NULL DEFAULT 'mua' COMMENT 'mua | ve_mua | lan_dau | cho | admin',
  `giao_dich_duoc` tinyint(4) NOT NULL DEFAULT 0,
  `dang_dung` tinyint(4) NOT NULL DEFAULT 0,
  `dang_rao` tinyint(4) NOT NULL DEFAULT 0,
  `het_han_ms` bigint(20) NOT NULL DEFAULT 0 COMMENT '0 = vinh vien',
  `tao_luc` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_nap_so_huu_char` (`char_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `nap_the_thang` (
  `char_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL DEFAULT 0,
  `het_han_ms` bigint(20) NOT NULL DEFAULT 0,
  `ngay_nhan_cuoi` varchar(10) NOT NULL DEFAULT '',
  `so_ngay_da_nhan` int(11) NOT NULL DEFAULT 0,
  `cap_nhat_luc` datetime DEFAULT NULL,
  PRIMARY KEY (`char_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `nap_ve_mua` (
  `char_id` int(11) NOT NULL,
  `mua` int(11) NOT NULL,
  `cao_cap` tinyint(4) NOT NULL DEFAULT 0,
  `diem` int(11) NOT NULL DEFAULT 0,
  `diem_ngay` int(11) NOT NULL DEFAULT 0,
  `ngay` varchar(10) NOT NULL DEFAULT '',
  `bac_free` int(11) NOT NULL DEFAULT 0 COMMENT 'bac free da nhan',
  `bac_cao_cap` int(11) NOT NULL DEFAULT 0 COMMENT 'bac cao cap da nhan',
  PRIMARY KEY (`char_id`,`mua`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `nap_vi` (
  `user_id` int(11) NOT NULL,
  `tong_nap_vnd` bigint(20) NOT NULL DEFAULT 0,
  `so_don` int(11) NOT NULL DEFAULT 0,
  `no_luong` bigint(20) NOT NULL DEFAULT 0 COMMENT 'thu hoi chua tru duoc (hoan tien sau khi da cong)',
  `khoa_nap` tinyint(4) NOT NULL DEFAULT 0 COMMENT '1 = chargeback: khong tao don moi',
  `thuong_dau` tinyint(4) NOT NULL DEFAULT 0 COMMENT '1 = da nhan thuong lan dau (trang tri)',
  `tran_tu_dat` bigint(20) NOT NULL DEFAULT 0 COMMENT 'tran thang nguoi choi tu dat (0 = khong)',
  `tran_cho` bigint(20) NOT NULL DEFAULT 0 COMMENT 'muc tran moi dang cho 72 gio',
  `tran_cho_luc` datetime DEFAULT NULL,
  `cap_nhat_luc` datetime DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `nap_webhook` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `nha_cung_cap` varchar(16) NOT NULL,
  `ma_gd` varchar(64) NOT NULL COMMENT 'ma giao dich ben cong (trung = goi lap, bo qua)',
  `chu_ky_hop_le` tinyint(4) NOT NULL DEFAULT 0,
  `so_tien` int(11) NOT NULL DEFAULT 0,
  `noi_dung` varchar(500) NOT NULL DEFAULT '',
  `ma_don` varchar(16) NOT NULL DEFAULT '',
  `ket_qua` varchar(120) NOT NULL DEFAULT '',
  `ip` varchar(64) NOT NULL DEFAULT '',
  `tao_luc` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_nap_webhook_gd` (`nha_cung_cap`,`ma_gd`),
  KEY `idx_nap_webhook_luc` (`tao_luc`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `ngude_giftcode` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL DEFAULT 'dangky',
  `xu` int(11) NOT NULL DEFAULT 0,
  `luong` int(11) NOT NULL DEFAULT 0,
  `luongLock` int(11) NOT NULL DEFAULT 0,
  `item` varchar(255) DEFAULT NULL,
  `charname` varchar(255) NOT NULL,
  `status` int(11) NOT NULL DEFAULT 0,
  `level` int(11) DEFAULT 40,
  `userId` int(11) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `nin_xoso` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(45) NOT NULL,
  `money` bigint(20) unsigned NOT NULL DEFAULT 0,
  `win` int(10) unsigned NOT NULL DEFAULT 0,
  `moneywin` bigint(20) unsigned NOT NULL DEFAULT 0,
  `moneyBid` bigint(20) unsigned NOT NULL DEFAULT 0,
  `moneyTax` bigint(20) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`charname`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `nin_xoso_low` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(45) NOT NULL,
  `money` bigint(20) unsigned NOT NULL DEFAULT 0,
  `win` int(10) unsigned NOT NULL DEFAULT 0,
  `moneywin` bigint(20) unsigned NOT NULL DEFAULT 0,
  `moneyBid` bigint(20) unsigned NOT NULL DEFAULT 0,
  `moneyTax` bigint(20) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`charname`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `team_user` (
  `id` int(4) NOT NULL AUTO_INCREMENT,
  `userid` int(10) unsigned NOT NULL,
  `provider` int(2) unsigned NOT NULL,
  `agent` int(2) NOT NULL DEFAULT -1,
  `newagent` varchar(45) NOT NULL DEFAULT '-1',
  `newprovider` varchar(45) NOT NULL DEFAULT '-1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`userid`) USING BTREE,
  KEY `Index_3` (`provider`),
  KEY `Index_4` (`agent`),
  KEY `newagent` (`newagent`),
  KEY `newprovider` (`newprovider`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_animal` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `owner` int(10) unsigned NOT NULL,
  `att` varchar(500) NOT NULL,
  `place` int(10) unsigned NOT NULL DEFAULT 0,
  `id_img` int(10) unsigned NOT NULL,
  `lv` int(2) unsigned NOT NULL DEFAULT 1,
  `name` varchar(45) NOT NULL,
  `texpire` bigint(20) unsigned NOT NULL DEFAULT 0,
  `del` int(10) unsigned NOT NULL DEFAULT 0,
  `isHoaHinh` int(11) DEFAULT 0,
  `timeHoaHinh` bigint(20) DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `Index_2` (`owner`),
  KEY `Index_4` (`place`),
  KEY `Index_5` (`id_img`),
  KEY `Index_6` (`lv`),
  KEY `Index_7` (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci PACK_KEYS=1;
CREATE TABLE IF NOT EXISTS `tob_boss_dong_gop` (
  `lan` bigint(20) NOT NULL COMMENT 'millis luc boss xuat hien',
  `id_char` int(11) NOT NULL,
  `phan_nghin` int(11) NOT NULL DEFAULT 0,
  `hang` int(11) NOT NULL DEFAULT 0,
  `bac_thuong` tinyint(4) NOT NULL DEFAULT 0 COMMENT '0 khong 1 hop tham gia 2 hop vang',
  `ghi_luc` datetime DEFAULT NULL,
  PRIMARY KEY (`lan`,`id_char`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_buy_ruong` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(100) NOT NULL,
  `dateLog` varchar(100) DEFAULT NULL,
  `timeBuy` datetime DEFAULT NULL,
  `soluong` int(10) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_buy_ruong_ngay_ten` (`dateLog`,`charname`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_cam_may` (
  `ma_may` varchar(64) NOT NULL COMMENT 'SystemInfo.deviceUniqueIdentifier do client gửi',
  `ly_do` varchar(255) NOT NULL DEFAULT '',
  `boi_ai` varchar(64) NOT NULL DEFAULT '' COMMENT 'admin nào cấm',
  `luc` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`ma_may`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Danh sách máy bị cấm đăng nhập';
CREATE TABLE IF NOT EXISTS `tob_char` (
  `id` int(4) NOT NULL AUTO_INCREMENT,
  `charname` varchar(20) NOT NULL,
  `userId` int(4) NOT NULL,
  `xp` bigint(20) unsigned NOT NULL DEFAULT 0,
  `lastLv` int(2) unsigned NOT NULL DEFAULT 0,
  `hp` int(4) NOT NULL DEFAULT 0,
  `mp` int(4) NOT NULL DEFAULT 0,
  `map` int(4) NOT NULL DEFAULT -1,
  `x` int(4) NOT NULL DEFAULT 0,
  `y` int(4) NOT NULL DEFAULT 0,
  `gold` bigint(20) unsigned NOT NULL DEFAULT 0,
  `headStyle` int(1) NOT NULL DEFAULT 0,
  `class` int(1) NOT NULL DEFAULT 0,
  `nHP1` int(2) NOT NULL DEFAULT 0,
  `nHP2` int(2) NOT NULL DEFAULT 0,
  `nHP3` int(2) NOT NULL DEFAULT 0,
  `nMP1` int(2) NOT NULL DEFAULT 0,
  `nMP2` int(2) NOT NULL DEFAULT 0,
  `nMP3` int(2) NOT NULL DEFAULT 0,
  `nAr1` int(2) NOT NULL DEFAULT 0,
  `nAr2` int(2) NOT NULL DEFAULT 0,
  `nAr3` int(2) NOT NULL DEFAULT 0,
  `skill1` int(1) NOT NULL DEFAULT 0,
  `skill2` int(1) NOT NULL DEFAULT 0,
  `skill3` int(1) NOT NULL DEFAULT 0,
  `skill4` int(1) NOT NULL DEFAULT 0,
  `skill5` int(11) NOT NULL DEFAULT 0,
  `strength` int(2) NOT NULL DEFAULT 0,
  `agitity` int(2) NOT NULL DEFAULT 0,
  `spirit` int(2) NOT NULL DEFAULT 0,
  `dexterity` int(2) NOT NULL DEFAULT 0,
  `luck` int(2) NOT NULL DEFAULT 0,
  `basepoint` int(2) NOT NULL DEFAULT 3,
  `skillpoint` int(2) NOT NULL DEFAULT 0,
  `itemq1` int(2) unsigned NOT NULL DEFAULT 0,
  `itemq2` int(2) unsigned NOT NULL DEFAULT 0,
  `itemq3` int(2) unsigned NOT NULL DEFAULT 0,
  `itemq4` int(2) unsigned NOT NULL DEFAULT 0,
  `skill6` int(2) unsigned NOT NULL DEFAULT 0,
  `skill7` int(2) unsigned NOT NULL DEFAULT 0,
  `skill8` int(2) unsigned NOT NULL DEFAULT 0,
  `skill9` int(2) unsigned NOT NULL DEFAULT 0,
  `skill10` int(2) unsigned NOT NULL DEFAULT 0,
  `skill11` int(2) unsigned NOT NULL DEFAULT 0,
  `skill12` int(2) unsigned NOT NULL DEFAULT 0,
  `skill13` int(2) unsigned NOT NULL DEFAULT 0,
  `skill14` int(2) unsigned NOT NULL DEFAULT 0,
  `skill15` int(2) unsigned NOT NULL DEFAULT 0,
  `killer` int(2) unsigned NOT NULL DEFAULT 0,
  `khan1` int(2) unsigned NOT NULL DEFAULT 0,
  `khan2` int(2) unsigned NOT NULL DEFAULT 0,
  `khan3` int(2) unsigned NOT NULL DEFAULT 0,
  `khan4` int(2) unsigned NOT NULL DEFAULT 0,
  `khan5` int(2) unsigned NOT NULL DEFAULT 0,
  `book` int(2) unsigned NOT NULL DEFAULT 0,
  `timeKiller` bigint(20) unsigned NOT NULL DEFAULT 0,
  `nPKill` int(2) unsigned NOT NULL DEFAULT 0,
  `gender` int(2) unsigned NOT NULL DEFAULT 0,
  `ticket` int(2) unsigned NOT NULL DEFAULT 0,
  `totalTimePlay` int(2) unsigned NOT NULL DEFAULT 0,
  `fullGoldTime` bigint(20) unsigned NOT NULL DEFAULT 0,
  `halfGoldTime` bigint(20) unsigned NOT NULL DEFAULT 0,
  `luong` int(10) unsigned NOT NULL DEFAULT 0,
  `isAdmin` int(2) unsigned NOT NULL DEFAULT 0,
  `nHP4` int(2) unsigned NOT NULL DEFAULT 0,
  `nHP5` int(2) unsigned NOT NULL DEFAULT 0,
  `nMP4` int(2) unsigned NOT NULL DEFAULT 0,
  `nMP5` int(2) unsigned NOT NULL DEFAULT 0,
  `potion` varchar(2000) DEFAULT NULL,
  `skill` varchar(2000) DEFAULT NULL,
  `basic` varchar(2000) DEFAULT NULL,
  `pInfo` varchar(2000) DEFAULT NULL,
  `del` int(1) unsigned NOT NULL DEFAULT 1,
  `tDelChar` bigint(20) unsigned NOT NULL DEFAULT 0,
  `blacklist` int(1) unsigned NOT NULL DEFAULT 0,
  `nhack` int(1) unsigned NOT NULL DEFAULT 0,
  `newiddb` int(10) unsigned NOT NULL DEFAULT 0,
  `newuserid` int(10) unsigned NOT NULL DEFAULT 0,
  `ban` int(10) unsigned NOT NULL DEFAULT 0,
  `idClan` int(10) NOT NULL DEFAULT -1,
  `reupclass` int(10) unsigned NOT NULL DEFAULT 0,
  `lastLog` datetime NOT NULL DEFAULT '2010-01-01 00:00:00',
  `infotop` varchar(2000) NOT NULL DEFAULT '-1',
  `itemquest` varchar(200) DEFAULT NULL,
  `allitem` text DEFAULT NULL,
  `equip` mediumtext DEFAULT NULL,
  `inven` mediumtext DEFAULT NULL,
  `bag` mediumtext DEFAULT NULL,
  `killme` varchar(600) DEFAULT NULL,
  `luonglock` int(10) unsigned DEFAULT 0,
  `idserver` int(10) unsigned NOT NULL DEFAULT 0,
  `tuido` mediumtext DEFAULT NULL,
  `tichluy` int(11) DEFAULT 0,
  `tichluy_bosung` int(11) NOT NULL DEFAULT 0,
  `tichluy_tuan` int(11) DEFAULT 0,
  `topNap` int(11) NOT NULL DEFAULT 0,
  `qua_npc` int(11) DEFAULT 0,
  `isPP` int(11) DEFAULT 0,
  `isMN` int(11) NOT NULL DEFAULT 0,
  `diemluong` int(11) DEFAULT 0,
  `kho_xu` bigint(20) NOT NULL DEFAULT 0 COMMENT 'Kho xu o NPC Tong Quan',
  `kho_luong` int(11) NOT NULL DEFAULT 0 COMMENT 'Kho luong mo o NPC Tong Quan',
  `kho_luong_khoa` int(11) NOT NULL DEFAULT 0 COMMENT 'Kho luong khoa o NPC Tong Quan',
  `dex` smallint(6) NOT NULL DEFAULT 0 COMMENT 'Pokemon: so Pokedex, 0 = suy tu class+cap',
  `shiny` tinyint(4) NOT NULL DEFAULT 0 COMMENT 'Pokemon: 1 = shiny',
  PRIMARY KEY (`id`),
  UNIQUE KEY `charname` (`charname`),
  KEY `Index_3` (`userId`),
  KEY `Index_6` (`idClan`),
  KEY `Index_5` (`pInfo`(333)),
  KEY `Index_7` (`gold`),
  KEY `Index_8` (`xp`),
  KEY `Index_9` (`ticket`),
  KEY `Index_10` (`basepoint`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_char_fruit` (
  `id_char` int(10) unsigned NOT NULL,
  `listidfruit` varchar(200) NOT NULL,
  `stFruit` varchar(100) NOT NULL,
  `tFruit` varchar(500) NOT NULL,
  `gift` varchar(500) NOT NULL COMMENT 'vi tri ve tren cay',
  `theo` varchar(500) NOT NULL,
  `tkho` varchar(500) NOT NULL,
  PRIMARY KEY (`id_char`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_char_lau_dai` (
  `id_char` int(11) NOT NULL,
  `du_lieu` varchar(2000) NOT NULL DEFAULT '',
  `cap_nhat` datetime DEFAULT NULL,
  PRIMARY KEY (`id_char`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_char_luong` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(45) NOT NULL,
  `country` tinyint(4) NOT NULL DEFAULT -1,
  `exp` bigint(20) unsigned NOT NULL DEFAULT 0,
  `idclan` int(11) NOT NULL DEFAULT -1,
  `honor` int(10) unsigned NOT NULL,
  `lv` int(10) unsigned NOT NULL DEFAULT 1,
  `nPK` bigint(20) unsigned NOT NULL DEFAULT 0,
  `wearing` varchar(500) NOT NULL DEFAULT '""',
  `luong` int(10) unsigned NOT NULL DEFAULT 0,
  `gold` bigint(20) unsigned NOT NULL DEFAULT 0,
  `headStyle` int(10) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `charname` (`charname`) USING BTREE,
  KEY `country` (`country`),
  KEY `exp` (`exp`),
  KEY `Index_5` (`honor`),
  KEY `Index_6` (`nPK`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_char_quest` (
  `id_char` int(10) unsigned NOT NULL,
  `itemget` varchar(200) NOT NULL COMMENT 'cac loai item cach nhau = dau , item cua cac nhiem vu khac nhau cach nhau bang dau |',
  `monskilled` varchar(200) NOT NULL COMMENT 'so luong quai da giet',
  `isfinish` varchar(200) NOT NULL COMMENT 'da hoan thanh nhiem vu hay chua',
  `idsubRcv` varchar(200) DEFAULT NULL,
  `itemquest` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id_char`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_char_thach_dau` (
  `id` int(10) unsigned NOT NULL,
  `charname` varchar(45) NOT NULL,
  `point` int(10) unsigned NOT NULL DEFAULT 0,
  `lv` int(10) unsigned NOT NULL DEFAULT 1,
  `timeupdate` datetime NOT NULL,
  `timeReg` datetime NOT NULL,
  `nhom` int(10) unsigned NOT NULL DEFAULT 0,
  `pointrank` int(10) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`charname`),
  KEY `Index_3` (`point`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci ROW_FORMAT=DYNAMIC;
CREATE TABLE IF NOT EXISTS `tob_char_tubinh` (
  `id_char` int(10) unsigned NOT NULL,
  `listid` varchar(200) NOT NULL,
  `listmanh` varchar(200) NOT NULL,
  PRIMARY KEY (`id_char`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_char_vip` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(100) NOT NULL,
  `vip` int(10) unsigned DEFAULT 0,
  `timeRcv` datetime NOT NULL,
  `reset` int(10) unsigned DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_char_vip_charname` (`charname`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_char_xp_cu` (
  `id` int(11) NOT NULL,
  `xp` bigint(20) NOT NULL DEFAULT 0,
  `lastLv` int(11) NOT NULL DEFAULT 0,
  `xp_moi` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_chiem_thanh_tuan` (
  `lan` bigint(20) NOT NULL,
  `id_char` int(11) NOT NULL,
  `id_clan` int(11) NOT NULL DEFAULT -1,
  `diem` bigint(20) NOT NULL DEFAULT 0,
  `thuong` varchar(255) NOT NULL DEFAULT '',
  `luc` datetime DEFAULT NULL,
  PRIMARY KEY (`lan`,`id_char`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_chiemthanh` (
  `clan` varchar(255) NOT NULL,
  `bangchu` varchar(255) NOT NULL,
  `win` int(11) NOT NULL DEFAULT 0,
  `xu` varchar(255) NOT NULL DEFAULT '0',
  PRIMARY KEY (`clan`,`bangchu`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `tob_chien_truong` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(45) NOT NULL,
  `sttru` bigint(20) unsigned NOT NULL DEFAULT 0,
  `lientram` int(10) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`charname`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_clan` (
  `id_clan` int(10) unsigned NOT NULL,
  `clanName` varchar(45) DEFAULT NULL,
  `charMaster` varchar(45) DEFAULT NULL,
  `lvClan` int(10) unsigned DEFAULT NULL,
  `member` varchar(8000) DEFAULT NULL,
  `money` bigint(20) unsigned DEFAULT NULL,
  `xp` bigint(20) unsigned DEFAULT NULL,
  `datecreate` datetime NOT NULL,
  `slogan` varchar(300) NOT NULL,
  `isdel` int(10) unsigned NOT NULL DEFAULT 0,
  `timedel` bigint(20) unsigned NOT NULL DEFAULT 0,
  `town` int(10) unsigned NOT NULL DEFAULT 0,
  `tax` int(10) unsigned NOT NULL DEFAULT 0,
  `devote` int(10) unsigned NOT NULL DEFAULT 0,
  `otherinfo` varchar(800) NOT NULL DEFAULT '""',
  `skillclan` varchar(800) NOT NULL DEFAULT '""',
  `country` int(11) NOT NULL DEFAULT -1,
  `tcheckdel` bigint(20) unsigned NOT NULL DEFAULT 0,
  `id_cu` int(10) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id_clan`) USING BTREE,
  KEY `Index_2` (`lvClan`),
  KEY `Index_3` (`clanName`),
  KEY `Index_4` (`charMaster`),
  KEY `Index_5` (`money`),
  KEY `Index_6` (`xp`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci ROW_FORMAT=DYNAMIC;
CREATE TABLE IF NOT EXISTS `tob_clan_msg` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(45) NOT NULL,
  `info` varchar(1000) NOT NULL,
  `datesend` datetime NOT NULL,
  `id_clan` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `Index_2` (`charname`),
  KEY `Index_3` (`id_clan`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci ROW_FORMAT=DYNAMIC;
CREATE TABLE IF NOT EXISTS `tob_client_dang_nhap` (
  `tai_khoan` varchar(45) NOT NULL,
  `loai` varchar(10) NOT NULL DEFAULT 'jar',
  `platf` varchar(100) NOT NULL DEFAULT '',
  `phien_ban` varchar(20) NOT NULL DEFAULT '',
  `ban_jar` int(11) NOT NULL DEFAULT 0,
  `chinh_chu` tinyint(4) NOT NULL DEFAULT 0,
  `so_lan` int(11) NOT NULL DEFAULT 0,
  `lan_dau` datetime NOT NULL,
  `lan_cuoi` datetime NOT NULL,
  PRIMARY KEY (`tai_khoan`,`loai`),
  KEY `idx_tcdn_lan_cuoi` (`lan_cuoi`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `tob_code_lixi` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `code` varchar(100) NOT NULL,
  `nhan` int(10) unsigned DEFAULT 0,
  `timeNhan` datetime DEFAULT NULL,
  `charnhan` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`code`) USING BTREE,
  KEY `Index_3` (`charnhan`),
  KEY `Index_4` (`nhan`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_danh_hieu` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(80) NOT NULL,
  `iddh` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`charname`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_dau_gia` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(100) NOT NULL,
  `luongbid` int(10) unsigned NOT NULL DEFAULT 0,
  `luongcoc` int(10) unsigned NOT NULL DEFAULT 0,
  `timebid` datetime DEFAULT NULL,
  `hang` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '0 la chua xep hang, 1 la dc quyen mua, 2 la nhan lai tien',
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`charname`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_dau_gia2` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(100) NOT NULL,
  `luongbid` int(10) unsigned NOT NULL DEFAULT 0,
  `luongcoc` int(10) unsigned NOT NULL DEFAULT 0,
  `timebid` datetime DEFAULT NULL,
  `hang` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '0 la chua xep hang, 1 la dc quyen mua, 2 la nhan lai tien',
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`charname`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_event` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `part1` varchar(450) NOT NULL,
  `part2` varchar(450) NOT NULL,
  `part3` varchar(850) NOT NULL,
  `part4` varchar(450) NOT NULL,
  `shield` varchar(45) NOT NULL DEFAULT '0,0,0,0,0,0,0,0,0,0,0,0',
  `gov1` varchar(200) NOT NULL DEFAULT '""',
  `gov2` varchar(200) NOT NULL DEFAULT '""',
  `gov3` varchar(200) NOT NULL DEFAULT '""',
  `quayso` varchar(200) NOT NULL,
  `topluong` varchar(500) NOT NULL DEFAULT '0',
  `topNapLuong` varchar(500) DEFAULT NULL,
  `phaohoa` varchar(100) NOT NULL DEFAULT '0,0',
  `iditemsell` int(10) unsigned NOT NULL DEFAULT 0,
  `openphuonghoang` varchar(100) NOT NULL DEFAULT '5000,5,0,0',
  `trung` varchar(45) NOT NULL DEFAULT '0|10000|0',
  `ruongmm` varchar(200) DEFAULT NULL,
  `lixi` varchar(100) DEFAULT '100,18,30',
  `vip` varchar(100) DEFAULT '500,100,5',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_event_top` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(100) NOT NULL,
  `luongnap` bigint(20) unsigned NOT NULL DEFAULT 0,
  `toppoint` bigint(20) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `Index_2` (`charname`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_event_world_cup_state` (
  `event_key` varchar(64) NOT NULL,
  `winner_side` tinyint(4) NOT NULL DEFAULT 0,
  `is_finalized` tinyint(4) NOT NULL DEFAULT 0,
  `finalized_by` varchar(100) DEFAULT NULL,
  `timeupdate` datetime DEFAULT NULL,
  PRIMARY KEY (`event_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `tob_farm` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `owner` int(11) NOT NULL,
  `farmId` tinyint(4) NOT NULL,
  `treeId` int(11) NOT NULL DEFAULT -1,
  `timeBegin` bigint(20) unsigned NOT NULL DEFAULT 0,
  `lv` tinyint(4) NOT NULL,
  `buy` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `lvPlot` tinyint(3) unsigned NOT NULL DEFAULT 1,
  `rank` tinyint(3) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `Index_2` (`treeId`),
  KEY `Index_3` (`lv`) USING BTREE,
  KEY `Index_4` (`lvPlot`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_friendlist` (
  `charId` int(11) NOT NULL,
  `friendId` varchar(200) NOT NULL,
  `name` varchar(1000) NOT NULL,
  `ischange` int(10) unsigned NOT NULL DEFAULT 1,
  PRIMARY KEY (`charId`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_fruit` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `id1` int(10) unsigned DEFAULT NULL,
  `id2` int(10) unsigned DEFAULT NULL,
  `id3` int(10) unsigned DEFAULT NULL,
  `id4` int(10) unsigned DEFAULT NULL,
  `id5` int(10) unsigned DEFAULT NULL,
  `decript` varchar(200) DEFAULT NULL,
  `time1` int(10) unsigned DEFAULT NULL,
  `time2` int(10) unsigned DEFAULT NULL,
  `time3` int(10) unsigned DEFAULT NULL,
  `time4` int(10) unsigned DEFAULT NULL,
  `time5` int(10) unsigned DEFAULT NULL,
  `tfruit` int(10) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=9 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_gem_new` (
  `owner` int(10) unsigned NOT NULL,
  `listtemplate` mediumtext NOT NULL,
  `soluong` mediumtext NOT NULL,
  `slock` mediumtext NOT NULL,
  PRIMARY KEY (`owner`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_get_board_nap` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(45) NOT NULL,
  `info` varchar(45) NOT NULL,
  `dateGet` varchar(100) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `Index_2` (`charname`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci ROW_FORMAT=DYNAMIC;
CREATE TABLE IF NOT EXISTS `tob_giftcode` (
  `ma` varchar(32) NOT NULL,
  `phan_thuong` varchar(500) NOT NULL,
  `han` datetime DEFAULT NULL,
  `tong_luot` int(11) NOT NULL DEFAULT 1,
  `da_dung` int(11) NOT NULL DEFAULT 0,
  `cap_toi_thieu` int(11) NOT NULL DEFAULT 1,
  `bat` tinyint(4) NOT NULL DEFAULT 1,
  `ghi_chu` varchar(255) NOT NULL DEFAULT '',
  `tao_boi` varchar(50) DEFAULT NULL,
  `tao_luc` datetime DEFAULT NULL,
  PRIMARY KEY (`ma`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_giftcode_log` (
  `ma` varchar(32) NOT NULL,
  `user_id` int(11) NOT NULL,
  `id_char` int(11) NOT NULL,
  `charname` varchar(30) NOT NULL DEFAULT '',
  `phan_thuong` varchar(500) NOT NULL DEFAULT '',
  `gia_tri` int(11) NOT NULL DEFAULT 0,
  `thang` int(11) NOT NULL DEFAULT 0,
  `luc` datetime DEFAULT NULL,
  PRIMARY KEY (`ma`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_ham_nguc` (
  `ngay` int(11) NOT NULL COMMENT 'yyyyMMdd',
  `id_char` int(11) NOT NULL,
  `tang` int(11) NOT NULL DEFAULT 0,
  `ghi_luc` datetime DEFAULT NULL,
  PRIMARY KEY (`ngay`,`id_char`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_hst_cho` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_ban` int(11) NOT NULL,
  `ten_ban` varchar(64) NOT NULL,
  `khoa_vat` int(11) NOT NULL,
  `so_luong` int(11) NOT NULL,
  `gia_moi` bigint(20) NOT NULL,
  `du_lieu` varchar(64) NOT NULL,
  `het_han` bigint(20) NOT NULL,
  `trang_thai` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_hst_don` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_mua` int(11) NOT NULL,
  `ten_mua` varchar(64) NOT NULL,
  `ma_vat` int(11) NOT NULL,
  `so_can` int(11) NOT NULL,
  `so_da_giao` int(11) NOT NULL,
  `gia_moi` bigint(20) NOT NULL,
  `het_han` bigint(20) NOT NULL,
  `trang_thai` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_hst_hop_thu` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_char` int(11) NOT NULL,
  `xu` bigint(20) NOT NULL,
  `khoa_vat` int(11) NOT NULL,
  `so_luong` int(11) NOT NULL,
  `du_lieu` varchar(64) NOT NULL,
  `ghi_chu` varchar(128) NOT NULL,
  `tao_luc` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_hst_nhan_vat` (
  `id_char` int(11) NOT NULL,
  `du_lieu` text NOT NULL,
  `cap_nhat` bigint(20) NOT NULL,
  PRIMARY KEY (`id_char`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_inbox` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(45) NOT NULL,
  `sender` varchar(45) NOT NULL,
  `info` varchar(800) NOT NULL,
  `dateSend` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `Index_2` (`charname`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci ROW_FORMAT=FIXED;
CREATE TABLE IF NOT EXISTS `tob_kinh_te_ngay` (
  `ngay` date NOT NULL,
  `so_nguoi` int(11) NOT NULL DEFAULT 0 COMMENT 'nguoi hoat dong 7 ngay',
  `cap_trung_vi` int(11) NOT NULL DEFAULT 0,
  `chi_so_gia` bigint(20) NOT NULL DEFAULT 0 COMMENT 'xuMoiGio(cap trung vi)',
  `tong_xu` bigint(20) NOT NULL DEFAULT 0 COMMENT 'gold + kho_xu',
  `tong_luong` bigint(20) NOT NULL DEFAULT 0,
  `tong_luong_khoa` bigint(20) NOT NULL DEFAULT 0,
  `cung_tien_thuc_x100` bigint(20) NOT NULL DEFAULT 0 COMMENT 'gio thu nhap / nguoi x100',
  `top1_phan_tram_x100` int(11) NOT NULL DEFAULT 0,
  `xu_vao` bigint(20) NOT NULL DEFAULT 0 COMMENT 'Char.addXu trong ngay (tu lan ghi truoc)',
  `xu_ra` bigint(20) NOT NULL DEFAULT 0 COMMENT 'Char.subXu trong ngay',
  `muc` tinyint(4) NOT NULL DEFAULT 0 COMMENT '0 xanh 1 vang 2 do',
  `ghi_luc` datetime DEFAULT NULL,
  PRIMARY KEY (`ngay`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_log_all_gem` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(45) NOT NULL,
  `aclog` varchar(45) NOT NULL DEFAULT '""',
  `info` varchar(8000) NOT NULL DEFAULT '""',
  `timelog` datetime NOT NULL,
  `charname2` varchar(45) NOT NULL DEFAULT '""',
  PRIMARY KEY (`id`),
  KEY `charname` (`charname`),
  KEY `aclog` (`aclog`),
  KEY `timelog` (`timelog`),
  KEY `charname2` (`charname2`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_log_all_item` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(45) NOT NULL,
  `aclog` varchar(45) NOT NULL DEFAULT '""',
  `info` varchar(8000) NOT NULL DEFAULT '""',
  `timelog` datetime NOT NULL,
  `charname2` varchar(45) NOT NULL DEFAULT '""',
  `idchar` int(10) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `charname` (`charname`),
  KEY `aclog` (`aclog`),
  KEY `timelog` (`timelog`),
  KEY `charname2` (`charname2`),
  KEY `idchar` (`idchar`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_log_buy_shop` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(45) NOT NULL,
  `info` varchar(450) NOT NULL,
  `timeBuy` datetime NOT NULL,
  `typeitem` int(10) unsigned NOT NULL DEFAULT 0,
  `price` int(10) unsigned NOT NULL,
  `money` varchar(450) NOT NULL DEFAULT '""',
  `soluong` int(10) unsigned NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  KEY `Index_2` (`charname`),
  KEY `Index_4` (`timeBuy`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci ROW_FORMAT=DYNAMIC;
CREATE TABLE IF NOT EXISTS `tob_log_buy_store` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(45) NOT NULL,
  `info` varchar(500) NOT NULL,
  `timeBuy` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_log_chuyen_gia_tri` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `luc` datetime NOT NULL,
  `kieu` varchar(20) NOT NULL,
  `gui_id` int(11) NOT NULL DEFAULT 0,
  `gui_ten` varchar(40) NOT NULL DEFAULT '',
  `gui_tk` int(11) NOT NULL DEFAULT 0,
  `gui_cap` int(11) NOT NULL DEFAULT 0,
  `gui_may` varchar(64) NOT NULL DEFAULT '',
  `gui_ip` varchar(64) NOT NULL DEFAULT '',
  `nhan_id` int(11) NOT NULL DEFAULT 0,
  `nhan_ten` varchar(40) NOT NULL DEFAULT '',
  `nhan_tk` int(11) NOT NULL DEFAULT 0,
  `nhan_cap` int(11) NOT NULL DEFAULT 0,
  `nhan_may` varchar(64) NOT NULL DEFAULT '',
  `nhan_ip` varchar(64) NOT NULL DEFAULT '',
  `xu` bigint(20) NOT NULL DEFAULT 0,
  `chi_tiet` varchar(255) NOT NULL DEFAULT '',
  `bi_chan` tinyint(4) NOT NULL DEFAULT 0,
  `nghi` tinyint(4) NOT NULL DEFAULT 0,
  `ly_do` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `k_nhan` (`nhan_id`,`luc`),
  KEY `k_luc` (`luc`),
  KEY `k_nghi` (`nghi`,`luc`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `tob_log_clan` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(45) NOT NULL,
  `actioninfo` varchar(45) NOT NULL,
  `timeLog` datetime NOT NULL,
  `info` varchar(8000) NOT NULL DEFAULT '""',
  PRIMARY KEY (`id`),
  KEY `Index_2` (`charname`),
  KEY `Index_3` (`actioninfo`),
  KEY `Index_4` (`timeLog`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_log_online` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `thoi_gian` datetime NOT NULL COMMENT 'luc lay mau',
  `ket_noi` int(11) NOT NULL DEFAULT 0 COMMENT 'so socket dang mo',
  `nhan_vat` int(11) NOT NULL DEFAULT 0 COMMENT 'so nhan vat that trong game',
  `pc` int(11) NOT NULL DEFAULT 0,
  `android` int(11) NOT NULL DEFAULT 0,
  `jar` int(11) NOT NULL DEFAULT 0,
  `khac` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_thoi_gian` (`thoi_gian`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_log_use_luong` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `idtemplate` int(10) unsigned NOT NULL DEFAULT 0,
  `soluong` bigint(20) unsigned NOT NULL DEFAULT 1,
  `solanmua` int(10) unsigned NOT NULL DEFAULT 1,
  `totalmoney` bigint(20) unsigned NOT NULL DEFAULT 0,
  `moneytype` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`name`) USING BTREE,
  KEY `Index_3` (`idtemplate`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_log_use_luong_1_7_2025` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `idtemplate` int(10) unsigned NOT NULL DEFAULT 0,
  `soluong` bigint(20) unsigned NOT NULL DEFAULT 1,
  `solanmua` int(10) unsigned NOT NULL DEFAULT 1,
  `totalmoney` bigint(20) unsigned NOT NULL DEFAULT 0,
  `moneytype` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`name`) USING BTREE,
  KEY `Index_3` (`idtemplate`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_log_use_luong_2_6_2025` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `idtemplate` int(10) unsigned NOT NULL DEFAULT 0,
  `soluong` bigint(20) unsigned NOT NULL DEFAULT 1,
  `solanmua` int(10) unsigned NOT NULL DEFAULT 1,
  `totalmoney` bigint(20) unsigned NOT NULL DEFAULT 0,
  `moneytype` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`name`) USING BTREE,
  KEY `Index_3` (`idtemplate`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_log_use_luong_2_7_2025` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `idtemplate` int(10) unsigned NOT NULL DEFAULT 0,
  `soluong` bigint(20) unsigned NOT NULL DEFAULT 1,
  `solanmua` int(10) unsigned NOT NULL DEFAULT 1,
  `totalmoney` bigint(20) unsigned NOT NULL DEFAULT 0,
  `moneytype` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`name`) USING BTREE,
  KEY `Index_3` (`idtemplate`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_log_use_luong_3_6_2025` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `idtemplate` int(10) unsigned NOT NULL DEFAULT 0,
  `soluong` bigint(20) unsigned NOT NULL DEFAULT 1,
  `solanmua` int(10) unsigned NOT NULL DEFAULT 1,
  `totalmoney` bigint(20) unsigned NOT NULL DEFAULT 0,
  `moneytype` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`name`) USING BTREE,
  KEY `Index_3` (`idtemplate`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_log_use_luong_4_6_2025` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `idtemplate` int(10) unsigned NOT NULL DEFAULT 0,
  `soluong` bigint(20) unsigned NOT NULL DEFAULT 1,
  `solanmua` int(10) unsigned NOT NULL DEFAULT 1,
  `totalmoney` bigint(20) unsigned NOT NULL DEFAULT 0,
  `moneytype` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`name`) USING BTREE,
  KEY `Index_3` (`idtemplate`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_log_use_luong_5_6_2025` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `idtemplate` int(10) unsigned NOT NULL DEFAULT 0,
  `soluong` bigint(20) unsigned NOT NULL DEFAULT 1,
  `solanmua` int(10) unsigned NOT NULL DEFAULT 1,
  `totalmoney` bigint(20) unsigned NOT NULL DEFAULT 0,
  `moneytype` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`name`) USING BTREE,
  KEY `Index_3` (`idtemplate`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_log_xoso` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `win` int(10) unsigned NOT NULL,
  `timelog` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `Index_2` (`win`),
  KEY `Index_3` (`timelog`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_market` (
  `charname` varchar(45) NOT NULL,
  `itemsell` text NOT NULL COMMENT 'danh sach item dang ban',
  `money` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT 'tien ban duoc',
  `items` text NOT NULL,
  `listBid` text NOT NULL,
  PRIMARY KEY (`charname`) USING BTREE,
  KEY `money` (`money`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_may_dang_nhap` (
  `ma_may` varchar(64) NOT NULL,
  `username` varchar(64) NOT NULL,
  `ip` varchar(45) NOT NULL DEFAULT '' COMMENT 'dài 45 để chứa được cả IPv6',
  `lan_dau` datetime NOT NULL DEFAULT current_timestamp(),
  `lan_cuoi` datetime NOT NULL DEFAULT current_timestamp(),
  `so_lan` int(11) NOT NULL DEFAULT 1,
  PRIMARY KEY (`ma_may`,`username`),
  KEY `k_username` (`username`),
  KEY `k_lan_cuoi` (`lan_cuoi`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Máy nào đã đăng nhập tài khoản nào';
CREATE TABLE IF NOT EXISTS `tob_mua_tuan` (
  `id_char` int(11) NOT NULL,
  `id_shop` int(11) NOT NULL,
  `tuan` int(11) NOT NULL,
  `so_luong` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id_char`,`id_shop`,`tuan`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_nap_moc` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `userID` int(11) NOT NULL COMMENT 'team_user.id / player.userID',
  `moc` tinyint(3) unsigned NOT NULL COMMENT '1 = 49k, 2 = 99k, 3 = 299k',
  `amount` int(10) unsigned NOT NULL COMMENT 'so tien VND',
  `ma_don` varchar(20) NOT NULL COMMENT 'noi dung chuyen khoan, dung de doi chieu voi SePay',
  `phuong_hoang` tinyint(4) NOT NULL DEFAULT -1 COMMENT 'moc 3 only: 0=Phuong Hoang, 1=Phuong Hoang Bang, -1=chua chon',
  `trang_thai` enum('cho_thanh_toan','da_thanh_toan','da_nhan') NOT NULL DEFAULT 'cho_thanh_toan',
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `paid_at` datetime DEFAULT NULL,
  `claimed_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_ma_don` (`ma_don`),
  KEY `idx_user_moc` (`userID`,`moc`,`trang_thai`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `tob_nap_xai_luong` (
  `int` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(100) NOT NULL,
  `luongxai` int(10) unsigned NOT NULL DEFAULT 0,
  `luongnap` int(10) unsigned NOT NULL DEFAULT 0,
  `tnap` datetime DEFAULT NULL,
  `txai` datetime DEFAULT NULL,
  PRIMARY KEY (`int`),
  UNIQUE KEY `Index_2` (`charname`),
  KEY `Index_3` (`luongxai`),
  KEY `Index_4` (`luongnap`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_nap_xai_luong_62025` (
  `int` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(100) NOT NULL,
  `luongxai` int(10) unsigned NOT NULL DEFAULT 0,
  `luongnap` int(10) unsigned NOT NULL DEFAULT 0,
  `tnap` datetime DEFAULT NULL,
  `txai` datetime DEFAULT NULL,
  PRIMARY KEY (`int`),
  UNIQUE KEY `Index_2` (`charname`),
  KEY `Index_3` (`luongxai`),
  KEY `Index_4` (`luongnap`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_nap_xai_luong_72025` (
  `int` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(100) NOT NULL,
  `luongxai` int(10) unsigned NOT NULL DEFAULT 0,
  `luongnap` int(10) unsigned NOT NULL DEFAULT 0,
  `tnap` datetime DEFAULT NULL,
  `txai` datetime DEFAULT NULL,
  PRIMARY KEY (`int`),
  UNIQUE KEY `Index_2` (`charname`),
  KEY `Index_3` (`luongxai`),
  KEY `Index_4` (`luongnap`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_nau_banh` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `listchar` text NOT NULL,
  `timecook` int(10) unsigned NOT NULL DEFAULT 60,
  `water` int(10) unsigned NOT NULL DEFAULT 0,
  `chamnc` int(10) unsigned NOT NULL DEFAULT 0,
  `listCharGift` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_nhiem_vu_lau_dai` (
  `id_char` int(11) NOT NULL,
  `ma` varchar(32) NOT NULL COMMENT 'ma trong data_quest_lau_dai (vd T-1.01)',
  `tien_do` int(11) NOT NULL DEFAULT 0,
  `trang_thai` tinyint(4) NOT NULL DEFAULT 0 COMMENT '0 dang lam 1 xong chua nhan 2 da nhan',
  `tuan` int(11) NOT NULL DEFAULT 0 COMMENT 'chi so tuan (NhiemVuLauDai.tuan) khi nhan',
  PRIMARY KEY (`id_char`,`ma`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_pet` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(300) NOT NULL,
  `idImg` tinyint(3) unsigned NOT NULL,
  `owner` int(10) unsigned NOT NULL,
  `info` varchar(200) NOT NULL COMMENT 'thong tin ve thoi gian mua, tong thoi gian ton tai',
  `place` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `itPet` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `name` (`name`),
  KEY `idimg` (`idImg`),
  KEY `owner` (`owner`),
  KEY `place` (`place`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_pokedex` (
  `id_char` int(11) NOT NULL,
  `dex` smallint(6) NOT NULL,
  `so_ha` int(11) NOT NULL DEFAULT 0,
  `lan_dau` datetime NOT NULL,
  PRIMARY KEY (`id_char`,`dex`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `tob_pokedex_shiny` (
  `id_char` int(11) NOT NULL,
  `dex` smallint(6) NOT NULL,
  `so_shiny` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id_char`,`dex`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_pokedex_thuong` (
  `id_char` int(11) NOT NULL,
  `moc` int(11) NOT NULL,
  `thoi_gian` datetime NOT NULL,
  PRIMARY KEY (`id_char`,`moc`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `tob_pvp_mua` (
  `mua` int(11) NOT NULL,
  `id_char` int(11) NOT NULL,
  `elo` int(11) NOT NULL DEFAULT 1000,
  `thang` int(11) NOT NULL DEFAULT 0,
  `thua` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`mua`,`id_char`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_qua` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(100) NOT NULL,
  `qua` bigint(20) unsigned NOT NULL DEFAULT 1,
  `typeQua` bigint(20) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`charname`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_qua_ct` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(45) NOT NULL,
  `soluong` int(10) unsigned NOT NULL DEFAULT 1,
  `nhan` int(10) unsigned NOT NULL DEFAULT 0,
  `tlog` datetime DEFAULT NULL,
  `tset` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`charname`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_qua_ct_bk` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(45) NOT NULL,
  `soluong` int(10) unsigned NOT NULL DEFAULT 1,
  `nhan` int(10) unsigned NOT NULL DEFAULT 0,
  `tlog` datetime DEFAULT NULL,
  `tset` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_qua_ct_top` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(45) NOT NULL,
  `soluong` int(10) unsigned NOT NULL DEFAULT 1 COMMENT '1-lien tram, 2 dam tru',
  `nhan` int(10) unsigned NOT NULL DEFAULT 0,
  `tlog` datetime DEFAULT NULL,
  `tset` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_qua_ct_top_charname` (`charname`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_qua_ct_top_bk` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(45) NOT NULL,
  `soluong` int(10) unsigned NOT NULL DEFAULT 1,
  `nhan` int(10) unsigned NOT NULL DEFAULT 0,
  `tlog` datetime DEFAULT NULL,
  `tset` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_qua_loi_dai` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(100) NOT NULL,
  `qua` int(10) unsigned NOT NULL DEFAULT 0,
  `typeQua` int(10) unsigned NOT NULL DEFAULT 0,
  `nhom` int(10) unsigned NOT NULL DEFAULT 0,
  `diem` int(10) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_qua_online` (
  `user_id` int(11) NOT NULL,
  `ngay` int(11) NOT NULL DEFAULT 0 COMMENT 'yyyyMMdd cua giay/moc',
  `giay` int(11) NOT NULL DEFAULT 0 COMMENT 'giay hoat dong (khong AFK) trong ngay',
  `moc` int(11) NOT NULL DEFAULT 0 COMMENT 'bitmask moc 15/30/60/120 phut da nhan',
  `chuoi_buoc` int(11) NOT NULL DEFAULT 0,
  `chuoi_ngay` int(11) NOT NULL DEFAULT 0,
  `thang` int(11) NOT NULL DEFAULT 0,
  `ngay_thang` int(11) NOT NULL DEFAULT 0,
  `thang_da_thuong` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_quest` (
  `idchar` int(10) unsigned NOT NULL,
  `idquest` int(10) unsigned NOT NULL,
  `state` tinyint(3) unsigned NOT NULL DEFAULT 0 COMMENT '0 chua nhan, 1 nhan, 2 hoan thanh',
  `totalmonskill` varchar(100) NOT NULL DEFAULT '0',
  `totalitemget` varchar(100) NOT NULL DEFAULT '0',
  PRIMARY KEY (`idchar`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_quest_clan` (
  `id_char` int(10) unsigned NOT NULL,
  `idquest` int(10) unsigned NOT NULL,
  `rcv` int(10) unsigned NOT NULL DEFAULT 0,
  `finish` int(10) unsigned NOT NULL DEFAULT 0,
  `nitem1` int(10) unsigned NOT NULL DEFAULT 0,
  `nitem2` int(10) unsigned NOT NULL DEFAULT 0,
  `nitem3` int(10) unsigned NOT NULL DEFAULT 0,
  `nmons1` int(10) unsigned NOT NULL DEFAULT 0,
  `nmons2` int(10) unsigned NOT NULL DEFAULT 0,
  `nmons3` int(10) unsigned NOT NULL DEFAULT 0,
  `id_clan` int(10) unsigned NOT NULL DEFAULT 0,
  `n` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `timeEnd` bigint(20) unsigned zerofill NOT NULL DEFAULT 00000000000000000000,
  PRIMARY KEY (`id_char`) USING BTREE,
  KEY `Index_2` (`idquest`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci ROW_FORMAT=DYNAMIC;
CREATE TABLE IF NOT EXISTS `tob_reg_chientruong` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(45) NOT NULL,
  `timeReg` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`charname`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_smsnap` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `text` varchar(50) NOT NULL,
  `syntax` varchar(100) NOT NULL,
  `port` varchar(10) NOT NULL,
  `provider` int(1) NOT NULL,
  `game` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `agent` int(10) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_sp_new` (
  `owner` int(10) unsigned NOT NULL,
  `listtemplate` varchar(300) NOT NULL,
  `soluong` varchar(500) NOT NULL,
  PRIMARY KEY (`owner`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_su_kien` (
  `ma` varchar(40) NOT NULL COMMENT 'khoa, khop hang so trong src/real/SuKien.java',
  `bat` tinyint(4) NOT NULL DEFAULT 0 COMMENT 'WEB ghi: 1 = bat su kien',
  `ten` varchar(100) DEFAULT NULL COMMENT 'SERVER ghi',
  `nhom` varchar(40) DEFAULT NULL COMMENT 'SERVER ghi',
  `mo_ta` varchar(500) DEFAULT NULL COMMENT 'SERVER ghi',
  `dang_bat` tinyint(4) NOT NULL DEFAULT 0 COMMENT 'SERVER ghi: trang thai that luc khoi dong',
  `sua_boi` varchar(50) DEFAULT NULL COMMENT 'WEB ghi',
  `sua_luc` datetime DEFAULT NULL COMMENT 'WEB ghi',
  `may_chu_luc` datetime DEFAULT NULL COMMENT 'SERVER ghi: lan khoi dong gan nhat',
  `mo_luc` datetime DEFAULT NULL,
  `dong_luc` datetime DEFAULT NULL,
  `tu_dong` tinyint(4) NOT NULL DEFAULT 1,
  PRIMARY KEY (`ma`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `tob_su_kien_lich` (
  `ma` varchar(40) NOT NULL,
  `nam` smallint(6) NOT NULL,
  `lan` tinyint(4) NOT NULL DEFAULT 0,
  `ten` varchar(100) NOT NULL DEFAULT '',
  `nhom` varchar(40) NOT NULL DEFAULT '',
  `ngay_le` varchar(60) NOT NULL DEFAULT '',
  `mo_ta` varchar(500) NOT NULL DEFAULT '',
  `phan_thuong` varchar(300) NOT NULL DEFAULT '',
  `lich_mo` datetime NOT NULL,
  `lich_dong` datetime NOT NULL,
  `mo_luc` datetime NOT NULL,
  `dong_luc` datetime NOT NULL,
  `ghi_de` tinyint(4) NOT NULL DEFAULT 0,
  `sua_boi` varchar(50) DEFAULT NULL,
  `sua_luc` datetime DEFAULT NULL,
  `cap_nhat` datetime DEFAULT NULL,
  PRIMARY KEY (`ma`,`nam`,`lan`),
  KEY `k_mo` (`mo_luc`),
  KEY `k_dong` (`dong_luc`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `tob_su_pham` (
  `id_tro` int(11) NOT NULL,
  `id_thay` int(11) NOT NULL,
  `bat_dau` datetime DEFAULT NULL,
  `moc` varchar(64) NOT NULL DEFAULT '' COMMENT 'moc da thuong, vd 20,40',
  PRIMARY KEY (`id_tro`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_top_event` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(50) NOT NULL,
  `point1` int(11) NOT NULL DEFAULT 0,
  `timeupdate` datetime DEFAULT current_timestamp(),
  `xu` bigint(20) DEFAULT 0,
  `luong` int(11) DEFAULT 0,
  `datexu` datetime DEFAULT current_timestamp(),
  `dateluong` datetime DEFAULT current_timestamp(),
  `crtime` bigint(20) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `charname` (`charname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `tob_top_event_wc` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(200) NOT NULL,
  `point1` int(10) unsigned NOT NULL DEFAULT 1,
  `timeupdate` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `xu` bigint(20) unsigned NOT NULL DEFAULT 0,
  `luong` bigint(20) unsigned NOT NULL DEFAULT 0,
  `datexu` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `dateluong` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `crtime` bigint(20) unsigned DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`charname`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_top_event_world_cup` (
  `charname` varchar(100) NOT NULL,
  `point_messi` int(11) NOT NULL DEFAULT 0,
  `point_ronaldo` int(11) NOT NULL DEFAULT 0,
  `claim_normal` tinyint(4) NOT NULL DEFAULT 0,
  `claim_top` tinyint(4) NOT NULL DEFAULT 0,
  `timeupdate` datetime DEFAULT NULL,
  PRIMARY KEY (`charname`),
  KEY `idx_messi` (`point_messi`),
  KEY `idx_ronaldo` (`point_ronaldo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `tob_top_thuong` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `vi_tri` tinyint(4) NOT NULL COMMENT 'hang 1..10',
  `charname` varchar(50) NOT NULL COMMENT 'nhan vat duoc thuong',
  `item` text DEFAULT NULL COMMENT 'chuoi phan thuong co dinh, cu phap giong giftcode.item',
  `item_chon` text DEFAULT NULL COMMENT 'phan thuong de nguoi choi tu chon: "nhan=chuoi|nhan=chuoi|..."',
  `da_nhan` tinyint(4) NOT NULL DEFAULT 0 COMMENT '0 cho nhan, 1 da nhan',
  `admin_user` varchar(50) NOT NULL DEFAULT '' COMMENT 'team_user.username nguoi tao',
  `tao_luc` datetime NOT NULL DEFAULT current_timestamp(),
  `nhan_luc` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_charname_da_nhan` (`charname`,`da_nhan`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `tob_tubinh` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `attack` int(10) unsigned NOT NULL,
  `def` int(10) unsigned NOT NULL,
  `dog` int(10) unsigned NOT NULL,
  `crit` int(10) unsigned NOT NULL,
  `hp` int(10) unsigned NOT NULL,
  `mp` int(10) unsigned NOT NULL,
  `level` int(10) unsigned NOT NULL,
  `nmanh` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=17 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_vantieu` (
  `idchar` int(10) unsigned NOT NULL,
  `namechar` varchar(45) NOT NULL,
  `typetieu` int(10) unsigned NOT NULL,
  `tReceive` bigint(20) unsigned NOT NULL,
  `lv` int(10) unsigned NOT NULL,
  `mapid` int(10) unsigned NOT NULL,
  `x` int(10) unsigned NOT NULL,
  `y` int(10) unsigned NOT NULL,
  `idcountry` tinyint(3) unsigned NOT NULL,
  `hp` bigint(20) unsigned NOT NULL DEFAULT 0,
  `maxhp` bigint(20) unsigned NOT NULL DEFAULT 0,
  `count` int(11) DEFAULT 0,
  PRIMARY KEY (`idchar`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_vi_tri_online` (
  `charname` varchar(45) NOT NULL,
  `map_id` int(11) NOT NULL DEFAULT 0,
  `map_ten` varchar(100) NOT NULL DEFAULT '',
  `khu` int(11) NOT NULL DEFAULT 0,
  `x` int(11) NOT NULL DEFAULT 0,
  `y` int(11) NOT NULL DEFAULT 0,
  `cap_nhat_luc` datetime NOT NULL,
  `tai_khoan` varchar(45) NOT NULL DEFAULT '',
  `client` varchar(10) NOT NULL DEFAULT '',
  `chinh_chu` tinyint(4) NOT NULL DEFAULT 0,
  `client_ban` varchar(20) NOT NULL DEFAULT '',
  `platf` varchar(100) NOT NULL DEFAULT '',
  PRIMARY KEY (`charname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `tob_web_info_char` (
  `charname` varchar(45) NOT NULL,
  `infoWearing` varchar(150) NOT NULL,
  `otherInfo` varchar(300) NOT NULL,
  `userid` int(10) unsigned NOT NULL,
  `lv` int(10) unsigned NOT NULL DEFAULT 0,
  `acountname` varchar(45) NOT NULL,
  PRIMARY KEY (`charname`) USING BTREE,
  KEY `userid` (`userid`) USING BTREE,
  KEY `lv` (`lv`),
  KEY `acountname` (`acountname`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci ROW_FORMAT=DYNAMIC;
CREATE TABLE IF NOT EXISTS `tob_wedding` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `idhusband` int(10) unsigned NOT NULL,
  `idwife` int(10) unsigned NOT NULL,
  `married` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `timeParty` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `dayparty` varchar(45) NOT NULL DEFAULT '""',
  `typeparty` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `idwedding` int(10) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `husband` (`idhusband`),
  KEY `idwife` (`idwife`),
  KEY `Index_4` (`married`),
  KEY `Index_5` (`typeparty`),
  KEY `Index_6` (`idwedding`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_x2exp` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `dayopenx2` varchar(500) DEFAULT NULL,
  `dayopenx3` varchar(500) DEFAULT NULL,
  `dayopenx50` varchar(500) DEFAULT NULL,
  `badwod` text NOT NULL,
  `tradepet` int(10) unsigned NOT NULL DEFAULT 0,
  `thamdinh` int(10) unsigned NOT NULL DEFAULT 0,
  `banchoi` int(11) NOT NULL DEFAULT 20,
  `itemevent` varchar(100) DEFAULT NULL,
  `pause` int(10) unsigned NOT NULL DEFAULT 1,
  `pauseT` int(10) unsigned NOT NULL DEFAULT 0,
  `vxmm` int(10) unsigned NOT NULL DEFAULT 1,
  `gopbang` int(10) unsigned NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_xoso` (
  `owner` int(10) unsigned NOT NULL,
  `vsxu` varchar(500) NOT NULL,
  `vsl` varchar(500) NOT NULL,
  `open` tinyint(3) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`owner`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `top_boss` (
  `charname` varchar(45) NOT NULL,
  `count` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`charname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `top_char_quest` (
  `id_char` int(10) unsigned NOT NULL,
  `id_quest` int(10) unsigned NOT NULL DEFAULT 1,
  `receive` tinyint(3) unsigned NOT NULL DEFAULT 0 COMMENT 'cho biet da nhan quest hay chua',
  `finish` tinyint(3) unsigned NOT NULL DEFAULT 0 COMMENT 'cho biet da hoan thanh quest hay chua, neu roi thi gui thong tin reponse ',
  `nmonster1` int(10) unsigned NOT NULL DEFAULT 0 COMMENT 'so luogn quai da giet',
  `nmonster2` int(10) unsigned NOT NULL DEFAULT 0,
  `nmonster3` int(10) unsigned NOT NULL DEFAULT 0,
  `nitem1` int(10) unsigned NOT NULL DEFAULT 0 COMMENT 'so luong item da lay',
  `nitem2` int(10) unsigned NOT NULL DEFAULT 0,
  `nitem3` int(10) unsigned NOT NULL DEFAULT 0,
  `currentnpc` int(10) unsigned NOT NULL DEFAULT 0,
  `ischeck` int(10) unsigned NOT NULL DEFAULT 0 COMMENT 'da kt roi thi ko kiem tra nua',
  PRIMARY KEY (`id_char`),
  KEY `Index_2` (`id_quest`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tv_nhat_ky` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL DEFAULT '',
  `hanh_dong` varchar(20) NOT NULL COMMENT 'cap | gia_han | thu_hoi | hoan_tien',
  `ngay` int(11) NOT NULL DEFAULT 0 COMMENT 'số ngày cấp (0 = vĩnh viễn)',
  `het_han_cu` bigint(20) NOT NULL DEFAULT 0,
  `het_han_moi` bigint(20) NOT NULL DEFAULT 0,
  `ma_don` varchar(16) NOT NULL DEFAULT '',
  `tac_nhan` varchar(60) NOT NULL DEFAULT '' COMMENT 'web:mua | admin:<tên> | webhook:<ncc>',
  `ly_do` varchar(255) NOT NULL DEFAULT '',
  `ip` varchar(64) NOT NULL DEFAULT '',
  `tao_luc` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tv_nk_user` (`user_id`),
  KEY `idx_tv_nk_don` (`ma_don`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tv_thanh_vien` (
  `user_id` int(11) NOT NULL COMMENT 'account.team_user.id',
  `username` varchar(50) NOT NULL DEFAULT '',
  `het_han_ms` bigint(20) NOT NULL DEFAULT 0,
  `nguon` varchar(16) NOT NULL DEFAULT '' COMMENT 'mua | admin | hang_loat',
  `so_lan` int(11) NOT NULL DEFAULT 0 COMMENT 'số lần được cấp / gia hạn',
  `cap_luc` datetime DEFAULT NULL,
  `cap_nhat_luc` datetime DEFAULT NULL,
  `ghi_chu` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`user_id`),
  KEY `idx_tv_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `web_admin_2fa` (
  `admin_user` varchar(50) NOT NULL COMMENT 'team_user.username, chu thuong. Khop ADMIN_ACCOUNTS trong config.local.php',
  `khoa_bimat` varchar(64) NOT NULL COMMENT 'Khoa TOTP dang Base32 (20 byte = 32 ky tu). CHI sinh boi tao_ma_2buoc.php',
  `kich_hoat` tinyint(4) NOT NULL DEFAULT 0 COMMENT '1 = tai khoan nay BUOC PHAI go ma hai buoc, khong co cach bo qua',
  `buoc_da_dung` bigint(20) NOT NULL DEFAULT 0 COMMENT 'Buoc thoi gian cao nhat da dung. Chong dung lai mot ma da bi doc len',
  `ma_du_phong` text DEFAULT NULL COMMENT '8 ma du phong DA BAM, ngan nhau bang \\n. Moi ma dung dung mot lan',
  `tao_luc` datetime DEFAULT NULL COMMENT 'Luc ghi danh',
  `dung_luc` datetime DEFAULT NULL COMMENT 'Lan cuoi go ma thanh cong',
  PRIMARY KEY (`admin_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `web_admin_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `admin_user` varchar(50) NOT NULL DEFAULT '' COMMENT 'team_user.username cua nguoi thao tac',
  `hanh_dong` varchar(50) NOT NULL DEFAULT '' COMMENT 'khoa_tk, mo_tk, tao_giftcode, dang_nhap_that_bai...',
  `doi_tuong` varchar(100) NOT NULL DEFAULT '' COMMENT 'ten tai khoan / ten nhan vat / ma giftcode bi tac dong',
  `chi_tiet` varchar(1000) NOT NULL DEFAULT '' COMMENT 'mo ta ro hon, vd gia tri cu -> gia tri moi',
  `ip` varchar(45) NOT NULL DEFAULT '' COMMENT 'IPv6 dai nhat 45 ky tu',
  `thoi_gian` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_thoi_gian` (`thoi_gian`),
  KEY `idx_admin_user` (`admin_user`),
  KEY `idx_hanh_dong` (`hanh_dong`),
  KEY `idx_doi_tuong` (`doi_tuong`),
  KEY `idx_chan_do_mk` (`ip`,`hanh_dong`,`thoi_gian`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `web_admin_thay_doi` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `admin_user` varchar(50) NOT NULL DEFAULT '',
  `hanh_dong` varchar(50) NOT NULL DEFAULT '',
  `doi_tuong` varchar(150) NOT NULL DEFAULT '',
  `truoc` mediumtext NOT NULL,
  `sau` mediumtext NOT NULL,
  `ip` varchar(45) NOT NULL DEFAULT '',
  `thoi_gian` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_thoi_gian` (`thoi_gian`),
  KEY `idx_doi_tuong` (`doi_tuong`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `web_admin_vai_tro` (
  `username` varchar(50) NOT NULL COMMENT 'account.team_user.username (chu thuong)',
  `vai_tro` varchar(20) NOT NULL DEFAULT 'xem',
  `ghi_chu` varchar(200) NOT NULL DEFAULT '',
  `tao_boi` varchar(50) NOT NULL DEFAULT '',
  `tao_luc` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `web_cai_dat` (
  `khoa` varchar(64) NOT NULL,
  `gia_tri` mediumtext NOT NULL,
  `cap_nhat_luc` datetime NOT NULL DEFAULT current_timestamp(),
  `cap_nhat_boi` varchar(50) NOT NULL DEFAULT '',
  PRIMARY KEY (`khoa`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `web_chan_spam` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ip` varchar(45) NOT NULL DEFAULT '' COMMENT 'IPv6 dai nhat 45 ky tu',
  `hanh_dong` varchar(30) NOT NULL DEFAULT '' COMMENT 'dang_ky, tao_don_nap...',
  `thoi_gian` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_dem` (`ip`,`hanh_dong`,`thoi_gian`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `web_faq` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `nhom` varchar(60) NOT NULL DEFAULT 'Chung',
  `cau_hoi` varchar(300) NOT NULL DEFAULT '',
  `tra_loi` mediumtext NOT NULL COMMENT 'Markdown',
  `thu_tu` int(11) NOT NULL DEFAULT 100,
  `hien` tinyint(4) NOT NULL DEFAULT 1,
  `sua_luc` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `web_khoa_tai_khoan` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `loai` varchar(20) NOT NULL DEFAULT 'khoa' COMMENT 'khoa | cam_chat',
  `ly_do` varchar(300) NOT NULL DEFAULT '',
  `het_han` datetime DEFAULT NULL,
  `admin_user` varchar(50) NOT NULL DEFAULT '',
  `tao_luc` datetime NOT NULL DEFAULT current_timestamp(),
  `mo_luc` datetime DEFAULT NULL COMMENT 'NULL = con hieu luc',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`username`),
  KEY `idx_het_han` (`mo_luc`,`het_han`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `web_lenh_nhan_vat` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `loai` varchar(30) NOT NULL COMMENT 'dat_cap_do | xoa_vat_pham',
  `char_id` int(11) NOT NULL COMMENT 'tob_char.id',
  `charname` varchar(50) NOT NULL DEFAULT '' COMMENT 'de doc log, khong dung de tim',
  `cap_do` int(11) NOT NULL DEFAULT 0,
  `phan_nghin` int(11) NOT NULL DEFAULT 0 COMMENT '% vao cap x10, 0..999',
  `tham_so` text DEFAULT NULL COMMENT 'xoa_vat_pham: "<cot>><mau>"',
  `admin_user` varchar(50) NOT NULL DEFAULT '' COMMENT 'team_user.username nguoi bam',
  `trang_thai` tinyint(4) NOT NULL DEFAULT 0 COMMENT '0 cho, 1 online da xong, 2 (cu) da ghi CSDL, 3 loi, 4 cho vao game, 5 huy',
  `ket_qua` varchar(255) NOT NULL DEFAULT '' COMMENT 'server ghi: viec da lam, hoac ly do loi',
  `tao_luc` datetime NOT NULL DEFAULT current_timestamp(),
  `xu_ly_luc` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_trang_thai` (`trang_thai`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `web_media` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ten_file` varchar(120) NOT NULL COMMENT 'ten luu tren dia (ngau nhien)',
  `ten_goc` varchar(200) NOT NULL DEFAULT '',
  `mime` varchar(40) NOT NULL DEFAULT '',
  `kich_thuoc` int(11) NOT NULL DEFAULT 0,
  `rong` int(11) NOT NULL DEFAULT 0,
  `cao` int(11) NOT NULL DEFAULT 0,
  `tai_boi` varchar(50) NOT NULL DEFAULT '',
  `tai_luc` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ten_file` (`ten_file`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `web_su_kien_lich` (
  `tuan` int(11) NOT NULL,
  `thang` int(11) NOT NULL DEFAULT 1,
  `ten1` varchar(200) NOT NULL DEFAULT '',
  `ten2` varchar(200) NOT NULL DEFAULT '',
  `phan` varchar(10) NOT NULL DEFAULT '',
  `loai` varchar(30) NOT NULL DEFAULT 'pokemon',
  `co_che` varchar(500) NOT NULL DEFAULT '',
  `so_ngay` int(11) NOT NULL DEFAULT 7,
  `phan_thuong` varchar(300) NOT NULL DEFAULT '',
  `moc` varchar(100) NOT NULL DEFAULT '',
  `sua_luc` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`tuan`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `web_tin_tuc` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `slug` varchar(120) NOT NULL,
  `tieu_de` varchar(200) NOT NULL DEFAULT '',
  `tom_tat` varchar(500) NOT NULL DEFAULT '',
  `anh_bia` varchar(300) NOT NULL DEFAULT '',
  `loai` varchar(40) NOT NULL DEFAULT 'Thông báo',
  `the` varchar(300) NOT NULL DEFAULT '' COMMENT 'tag, cach nhau dau phay',
  `noi_dung` mediumtext NOT NULL COMMENT 'Markdown',
  `trang_thai` tinyint(4) NOT NULL DEFAULT 0,
  `dang_luc` datetime DEFAULT NULL,
  `ghim` tinyint(4) NOT NULL DEFAULT 0 COMMENT '1 = ghim len trang chu',
  `tac_gia` varchar(50) NOT NULL DEFAULT '',
  `tao_luc` datetime NOT NULL DEFAULT current_timestamp(),
  `sua_luc` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_slug` (`slug`),
  KEY `idx_dang` (`trang_thai`,`dang_luc`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `web_trang` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `slug` varchar(120) NOT NULL,
  `tieu_de` varchar(200) NOT NULL DEFAULT '',
  `nhom` varchar(60) NOT NULL DEFAULT 'Thế giới',
  `icon` varchar(60) NOT NULL DEFAULT 'i-lucide-file-text',
  `mo_ta` varchar(500) NOT NULL DEFAULT '',
  `tu_khoa` varchar(300) NOT NULL DEFAULT '',
  `thu_tu` int(11) NOT NULL DEFAULT 100,
  `noi_dung` mediumtext NOT NULL COMMENT 'Markdown; ## tieu de muc -> muc luc + tim kiem',
  `trang_thai` tinyint(4) NOT NULL DEFAULT 1 COMMENT '0 nhap/an, 1 hien',
  `tac_gia` varchar(50) NOT NULL DEFAULT '',
  `sua_luc` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- ======== 1. Schema (khong pha, chay lai duoc)

-- them_bang_2buoc.sql
CREATE TABLE IF NOT EXISTS `web_admin_2fa` (
  `admin_user`   VARCHAR(50) NOT NULL
      COMMENT 'team_user.username, chu thuong. Khop ADMIN_ACCOUNTS trong config.local.php',
  `khoa_bimat`   VARCHAR(64) NOT NULL
      COMMENT 'Khoa TOTP dang Base32 (20 byte = 32 ky tu). CHI sinh boi tao_ma_2buoc.php',
  `kich_hoat`    TINYINT     NOT NULL DEFAULT 0
      COMMENT '1 = tai khoan nay BUOC PHAI go ma hai buoc, khong co cach bo qua',
  `buoc_da_dung` BIGINT      NOT NULL DEFAULT 0
      COMMENT 'Buoc thoi gian cao nhat da dung. Chong dung lai mot ma da bi doc len',
  `ma_du_phong`  TEXT        NULL
      COMMENT '8 ma du phong DA BAM, ngan nhau bang \\n. Moi ma dung dung mot lan',
  `tao_luc`      DATETIME    NULL COMMENT 'Luc ghi danh',
  `dung_luc`     DATETIME    NULL COMMENT 'Lan cuoi go ma thanh cong',
  PRIMARY KEY (`admin_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- them_bang_cam_may.sql
CREATE TABLE IF NOT EXISTS `tob_cam_may` (
    `ma_may`  VARCHAR(64)  NOT NULL COMMENT 'SystemInfo.deviceUniqueIdentifier do client gửi',
    `ly_do`   VARCHAR(255) NOT NULL DEFAULT '',
    `boi_ai`  VARCHAR(64)  NOT NULL DEFAULT '' COMMENT 'admin nào cấm',
    `luc`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`ma_may`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='Danh sách máy bị cấm đăng nhập';
CREATE TABLE IF NOT EXISTS `tob_may_dang_nhap` (
    `ma_may`   VARCHAR(64) NOT NULL,
    `username` VARCHAR(64) NOT NULL,
    `ip`       VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'dài 45 để chứa được cả IPv6',
    `lan_dau`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `lan_cuoi` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `so_lan`   INT         NOT NULL DEFAULT 1,
    PRIMARY KEY (`ma_may`, `username`),
    KEY `k_username` (`username`),
    KEY `k_lan_cuoi` (`lan_cuoi`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='Máy nào đã đăng nhập tài khoản nào';

-- them_bang_dieu_khien_server.sql
CREATE TABLE IF NOT EXISTS `dieu_khien_server` (
  `id` TINYINT UNSIGNED NOT NULL,
  `muon_chay` TINYINT(1) NOT NULL DEFAULT 1
      COMMENT '1 = muon server dang chay, 0 = muon tat han',
  `gio_hen` DATETIME NULL DEFAULT NULL
      COMMENT 'NULL = ap dung ngay; co gio = doi den gio nay watcher moi ap dung',
  `da_ap_dung` TINYINT(1) NOT NULL DEFAULT 1
      COMMENT '0 = dang cho watcher xu ly, 1 = da xong (khong con viec gi cho watcher)',
  `trang_thai_thuc_te` VARCHAR(20) NULL DEFAULT NULL
      COMMENT 'watcher tu ghi lai: active/inactive/failed/... (ket qua systemctl is-active)',
  `lan_kiem_cuoi` DATETIME NULL DEFAULT NULL
      COMMENT 'nhip tim: lan watcher chay gan nhat - web dung de biet watcher con song khong',
  `yeu_cau_boi` VARCHAR(50) NULL DEFAULT NULL COMMENT 'ten admin vua bam nut',
  `yeu_cau_luc` DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT IGNORE INTO `dieu_khien_server` (`id`, `muon_chay`, `da_ap_dung`) VALUES (1, 1, 1);

-- them_bang_su_kien.sql
CREATE TABLE IF NOT EXISTS `tob_su_kien` (
  `ma`          VARCHAR(40)  NOT NULL COMMENT 'khoa, khop hang so trong src/real/SuKien.java',
  `bat`         TINYINT      NOT NULL DEFAULT 0 COMMENT 'WEB ghi: 1 = bat su kien',
  `ten`         VARCHAR(100) NULL COMMENT 'SERVER ghi',
  `nhom`        VARCHAR(40)  NULL COMMENT 'SERVER ghi',
  `mo_ta`       VARCHAR(500) NULL COMMENT 'SERVER ghi',
  `dang_bat`    TINYINT      NOT NULL DEFAULT 0 COMMENT 'SERVER ghi: trang thai that luc khoi dong',
  `sua_boi`     VARCHAR(50)  NULL COMMENT 'WEB ghi',
  `sua_luc`     DATETIME     NULL COMMENT 'WEB ghi',
  `may_chu_luc` DATETIME     NULL COMMENT 'SERVER ghi: lan khoi dong gan nhat',
  `mo_luc`      DATETIME     NULL COMMENT 'WEB ghi: hen bat su kien luc nay, xong thi server xoa',
  `dong_luc`    DATETIME     NULL COMMENT 'WEB ghi: hen tat su kien luc nay, xong thi server xoa',
  PRIMARY KEY (`ma`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- them_bang_tob_log_online.sql
CREATE TABLE IF NOT EXISTS `tob_log_online` (
  `id`        int(10) unsigned NOT NULL AUTO_INCREMENT,
  `thoi_gian` datetime         NOT NULL                COMMENT 'luc lay mau',
  `ket_noi`   int(11)          NOT NULL DEFAULT 0      COMMENT 'so socket dang mo',
  `nhan_vat`  int(11)          NOT NULL DEFAULT 0      COMMENT 'so nhan vat that trong game',
  PRIMARY KEY (`id`),
  KEY `idx_thoi_gian` (`thoi_gian`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- them_bang_tob_top_thuong.sql
CREATE TABLE IF NOT EXISTS `tob_top_thuong` (
  `id`         bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `vi_tri`     tinyint(4)   NOT NULL                          COMMENT 'hang 1..10',
  `charname`   varchar(50)  NOT NULL                          COMMENT 'nhan vat duoc thuong',
  `item`       text         NULL                              COMMENT 'chuoi phan thuong co dinh, cu phap giong giftcode.item',
  `item_chon`  text         NULL                              COMMENT 'phan thuong de nguoi choi tu chon: "nhan=chuoi|nhan=chuoi|..."',
  `da_nhan`    tinyint(4)   NOT NULL DEFAULT 0                COMMENT '0 cho nhan, 1 da nhan',
  `admin_user` varchar(50)  NOT NULL DEFAULT ''               COMMENT 'team_user.username nguoi tao',
  `tao_luc`    datetime     NOT NULL DEFAULT current_timestamp(),
  `nhan_luc`   datetime     NULL,
  PRIMARY KEY (`id`),
  KEY `idx_charname_da_nhan` (`charname`, `da_nhan`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- them_bang_web_admin_log.sql
CREATE TABLE IF NOT EXISTS `web_admin_log` (
  `id`         bigint(20)    NOT NULL AUTO_INCREMENT,
  `admin_user` varchar(50)   NOT NULL DEFAULT ''   COMMENT 'team_user.username cua nguoi thao tac',
  `hanh_dong`  varchar(50)   NOT NULL DEFAULT ''   COMMENT 'khoa_tk, mo_tk, tao_giftcode, dang_nhap_that_bai...',
  `doi_tuong`  varchar(100)  NOT NULL DEFAULT ''   COMMENT 'ten tai khoan / ten nhan vat / ma giftcode bi tac dong',
  `chi_tiet`   varchar(1000) NOT NULL DEFAULT ''   COMMENT 'mo ta ro hon, vd gia tri cu -> gia tri moi',
  `ip`         varchar(45)   NOT NULL DEFAULT ''   COMMENT 'IPv6 dai nhat 45 ky tu',
  `thoi_gian`  datetime      NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_thoi_gian` (`thoi_gian`),
  KEY `idx_admin_user` (`admin_user`),
  KEY `idx_hanh_dong` (`hanh_dong`),
  KEY `idx_doi_tuong` (`doi_tuong`),
  KEY `idx_chan_do_mk` (`ip`, `hanh_dong`, `thoi_gian`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- them_bang_web_chan_spam.sql
CREATE TABLE IF NOT EXISTS `web_chan_spam` (
  `id`        bigint(20)  NOT NULL AUTO_INCREMENT,
  `ip`        varchar(45) NOT NULL DEFAULT ''  COMMENT 'IPv6 dai nhat 45 ky tu',
  `hanh_dong` varchar(30) NOT NULL DEFAULT ''  COMMENT 'dang_ky, tao_don_nap...',
  `thoi_gian` datetime    NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_dem` (`ip`, `hanh_dong`, `thoi_gian`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- them_bang_web_lenh_nhan_vat.sql
CREATE TABLE IF NOT EXISTS `web_lenh_nhan_vat` (
  `id`         bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `loai`       varchar(30)  NOT NULL                          COMMENT 'dat_cap_do | xoa_vat_pham',
  `char_id`    int(11)      NOT NULL                          COMMENT 'tob_char.id',
  `charname`   varchar(50)  NOT NULL DEFAULT ''               COMMENT 'de doc log, khong dung de tim',
  `cap_do`     int(11)      NOT NULL DEFAULT 0,
  `phan_nghin` int(11)      NOT NULL DEFAULT 0                COMMENT '% vao cap x10, 0..999',
  `tham_so`    text         NULL                              COMMENT 'xoa_vat_pham: "<cot>><mau>"',
  `admin_user` varchar(50)  NOT NULL DEFAULT ''               COMMENT 'team_user.username nguoi bam',
  `trang_thai` tinyint(4)   NOT NULL DEFAULT 0                COMMENT '0 cho, 1 online da xong, 2 (cu) da ghi CSDL, 3 loi, 4 cho vao game, 5 huy',
  `ket_qua`    varchar(255) NOT NULL DEFAULT ''               COMMENT 'server ghi: viec da lam, hoac ly do loi',
  `tao_luc`    datetime     NOT NULL DEFAULT current_timestamp(),
  `xu_ly_luc`  datetime     NULL,
  PRIMARY KEY (`id`),
  KEY `idx_trang_thai` (`trang_thai`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- them_bang_tob_client_dang_nhap.sql
CREATE TABLE IF NOT EXISTS `tob_client_dang_nhap` (
  `tai_khoan` VARCHAR(45) NOT NULL,
  `loai` VARCHAR(10) NOT NULL DEFAULT 'jar',
  `platf` VARCHAR(100) NOT NULL DEFAULT '',
  `phien_ban` VARCHAR(20) NOT NULL DEFAULT '',
  `ban_jar` INT NOT NULL DEFAULT 0,
  `chinh_chu` TINYINT NOT NULL DEFAULT 0,
  `so_lan` INT NOT NULL DEFAULT 0,
  `lan_dau` DATETIME NOT NULL,
  `lan_cuoi` DATETIME NOT NULL,
  PRIMARY KEY (`tai_khoan`, `loai`),
  KEY `idx_tcdn_lan_cuoi` (`lan_cuoi`)
) DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `tob_vi_tri_online` (
  `charname` VARCHAR(45) NOT NULL,
  `map_id` INT NOT NULL DEFAULT 0,
  `map_ten` VARCHAR(100) NOT NULL DEFAULT '',
  `khu` INT NOT NULL DEFAULT 0,
  `x` INT NOT NULL DEFAULT 0,
  `y` INT NOT NULL DEFAULT 0,
  `cap_nhat_luc` DATETIME NOT NULL,
  PRIMARY KEY (`charname`)
) DEFAULT CHARSET=utf8mb4;
ALTER TABLE `tob_vi_tri_online`
  ADD COLUMN IF NOT EXISTS `tai_khoan` VARCHAR(45) NOT NULL DEFAULT '',
  ADD COLUMN IF NOT EXISTS `client` VARCHAR(10) NOT NULL DEFAULT '',
  ADD COLUMN IF NOT EXISTS `chinh_chu` TINYINT NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS `client_ban` VARCHAR(20) NOT NULL DEFAULT '',
  ADD COLUMN IF NOT EXISTS `platf` VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE `tob_log_online`
  ADD COLUMN IF NOT EXISTS `pc` INT NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS `android` INT NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS `jar` INT NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS `khac` INT NOT NULL DEFAULT 0;

-- them_bang_chong_clone.sql
CREATE TABLE IF NOT EXISTS tob_log_chuyen_gia_tri (
  id BIGINT NOT NULL AUTO_INCREMENT,
  luc DATETIME NOT NULL,
  kieu VARCHAR(20) NOT NULL,
  gui_id INT NOT NULL DEFAULT 0, gui_ten VARCHAR(40) NOT NULL DEFAULT '', gui_tk INT NOT NULL DEFAULT 0,
  gui_cap INT NOT NULL DEFAULT 0, gui_may VARCHAR(64) NOT NULL DEFAULT '', gui_ip VARCHAR(64) NOT NULL DEFAULT '',
  nhan_id INT NOT NULL DEFAULT 0, nhan_ten VARCHAR(40) NOT NULL DEFAULT '', nhan_tk INT NOT NULL DEFAULT 0,
  nhan_cap INT NOT NULL DEFAULT 0, nhan_may VARCHAR(64) NOT NULL DEFAULT '', nhan_ip VARCHAR(64) NOT NULL DEFAULT '',
  xu BIGINT NOT NULL DEFAULT 0,
  chi_tiet VARCHAR(255) NOT NULL DEFAULT '',
  bi_chan TINYINT NOT NULL DEFAULT 0,
  nghi TINYINT NOT NULL DEFAULT 0,
  ly_do VARCHAR(255) NOT NULL DEFAULT '',
  PRIMARY KEY (id), KEY k_nhan (nhan_id, luc), KEY k_luc (luc), KEY k_nghi (nghi, luc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- them_cot_kho_xu_luong.sql
ALTER TABLE `tob_char` ADD COLUMN IF NOT EXISTS `kho_xu`         BIGINT(20) NOT NULL DEFAULT 0 COMMENT 'Kho xu o NPC Tong Quan';
ALTER TABLE `tob_char` ADD COLUMN IF NOT EXISTS `kho_luong`      INT(11)    NOT NULL DEFAULT 0 COMMENT 'Kho luong mo o NPC Tong Quan';
ALTER TABLE `tob_char` ADD COLUMN IF NOT EXISTS `kho_luong_khoa` INT(11)    NOT NULL DEFAULT 0 COMMENT 'Kho luong khoa o NPC Tong Quan';

-- them_cot_pokemon.sql
ALTER TABLE `tob_char` ADD COLUMN IF NOT EXISTS `dex`   SMALLINT NOT NULL DEFAULT 0 COMMENT 'Pokemon: so Pokedex, 0 = suy tu class+cap',
                       ADD COLUMN IF NOT EXISTS `shiny` TINYINT  NOT NULL DEFAULT 0 COMMENT 'Pokemon: 1 = shiny';
ALTER TABLE `data_monster` ADD COLUMN IF NOT EXISTS `dex`   SMALLINT NOT NULL DEFAULT 0  COMMENT 'Pokemon: so Pokedex cua template',
                           ADD COLUMN IF NOT EXISTS `shiny` TINYINT  NOT NULL DEFAULT 0  COMMENT 'Pokemon: 1 = shiny',
                           ADD COLUMN IF NOT EXISTS `he2`   TINYINT  NOT NULL DEFAULT -1 COMMENT 'Pokemon: he thu hai, -1 = khong co';

-- them_bang_pokedex.sql
CREATE TABLE IF NOT EXISTS `tob_pokedex` (
  `id_char` INT NOT NULL,
  `dex`     SMALLINT NOT NULL,
  `so_ha`   INT NOT NULL DEFAULT 0,
  `lan_dau` DATETIME NOT NULL,
  PRIMARY KEY (`id_char`, `dex`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `tob_pokedex_thuong` (
  `id_char`   INT NOT NULL,
  `moc`       INT NOT NULL,
  `thoi_gian` DATETIME NOT NULL,
  PRIMARY KEY (`id_char`, `moc`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- them_bang_lau_dai.sql
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
CREATE TABLE IF NOT EXISTS `tob_char_lau_dai` (
  `id_char` int NOT NULL,
  `du_lieu` varchar(2000) NOT NULL DEFAULT '',
  `cap_nhat` datetime DEFAULT NULL,
  PRIMARY KEY (`id_char`)
) DEFAULT CHARSET=utf8;
CREATE TABLE IF NOT EXISTS `tob_nhiem_vu_lau_dai` (
  `id_char` int NOT NULL,
  `ma` varchar(32) NOT NULL COMMENT 'ma trong data_quest_lau_dai (vd T-1.01)',
  `tien_do` int NOT NULL DEFAULT 0,
  `trang_thai` tinyint NOT NULL DEFAULT 0 COMMENT '0 dang lam 1 xong chua nhan 2 da nhan',
  `tuan` int NOT NULL DEFAULT 0 COMMENT 'chi so tuan (NhiemVuLauDai.tuan) khi nhan',
  PRIMARY KEY (`id_char`,`ma`)
) DEFAULT CHARSET=utf8;
CREATE TABLE IF NOT EXISTS `tob_mua_tuan` (
  `id_char` int NOT NULL,
  `id_shop` int NOT NULL,
  `tuan` int NOT NULL,
  `so_luong` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id_char`,`id_shop`,`tuan`)
) DEFAULT CHARSET=utf8;
CREATE TABLE IF NOT EXISTS `tob_boss_dong_gop` (
  `lan` bigint NOT NULL COMMENT 'millis luc boss xuat hien',
  `id_char` int NOT NULL,
  `phan_nghin` int NOT NULL DEFAULT 0,
  `hang` int NOT NULL DEFAULT 0,
  `bac_thuong` tinyint NOT NULL DEFAULT 0 COMMENT '0 khong 1 hop tham gia 2 hop vang',
  `ghi_luc` datetime DEFAULT NULL,
  PRIMARY KEY (`lan`,`id_char`)
) DEFAULT CHARSET=utf8;
CREATE TABLE IF NOT EXISTS `tob_ham_nguc` (
  `ngay` int NOT NULL COMMENT 'yyyyMMdd',
  `id_char` int NOT NULL,
  `tang` int NOT NULL DEFAULT 0,
  `ghi_luc` datetime DEFAULT NULL,
  PRIMARY KEY (`ngay`,`id_char`)
) DEFAULT CHARSET=utf8;
CREATE TABLE IF NOT EXISTS `tob_pvp_mua` (
  `mua` int NOT NULL,
  `id_char` int NOT NULL,
  `elo` int NOT NULL DEFAULT 1000,
  `thang` int NOT NULL DEFAULT 0,
  `thua` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`mua`,`id_char`)
) DEFAULT CHARSET=utf8;
CREATE TABLE IF NOT EXISTS `tob_su_pham` (
  `id_tro` int NOT NULL,
  `id_thay` int NOT NULL,
  `bat_dau` datetime DEFAULT NULL,
  `moc` varchar(64) NOT NULL DEFAULT '' COMMENT 'moc da thuong, vd 20,40',
  PRIMARY KEY (`id_tro`)
) DEFAULT CHARSET=utf8;
CREATE TABLE IF NOT EXISTS `tob_char_xp_cu` (
  `id` int NOT NULL,
  `xp` bigint NOT NULL DEFAULT 0,
  `lastLv` int NOT NULL DEFAULT 0,
  `xp_moi` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8;
CREATE TABLE IF NOT EXISTS `tob_pokedex_shiny` (
  `id_char` int NOT NULL,
  `dex` smallint NOT NULL,
  `so_shiny` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id_char`,`dex`)
) DEFAULT CHARSET=utf8;
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
CREATE TABLE IF NOT EXISTS `tob_chiem_thanh_tuan` (
  `lan` bigint NOT NULL,
  `id_char` int NOT NULL,
  `id_clan` int NOT NULL DEFAULT -1,
  `diem` bigint NOT NULL DEFAULT 0,
  `thuong` varchar(255) NOT NULL DEFAULT '',
  `luc` datetime DEFAULT NULL,
  PRIMARY KEY (`lan`,`id_char`)
) DEFAULT CHARSET=utf8;

-- them_bang_top_boss.sql
CREATE TABLE IF NOT EXISTS `top_boss` (
  `charname` varchar(45) NOT NULL,
  `count` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`charname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- web_cms.sql
CREATE TABLE IF NOT EXISTS `web_cai_dat` (
  `khoa`         varchar(64)  NOT NULL,
  `gia_tri`      mediumtext   NOT NULL,
  `cap_nhat_luc` datetime     NOT NULL DEFAULT current_timestamp(),
  `cap_nhat_boi` varchar(50)  NOT NULL DEFAULT '',
  PRIMARY KEY (`khoa`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `web_tin_tuc` (
  `id`        bigint(20)   NOT NULL AUTO_INCREMENT,
  `slug`      varchar(120) NOT NULL,
  `tieu_de`   varchar(200) NOT NULL DEFAULT '',
  `tom_tat`   varchar(500) NOT NULL DEFAULT '',
  `anh_bia`   varchar(300) NOT NULL DEFAULT '',
  `loai`      varchar(40)  NOT NULL DEFAULT 'Thông báo',
  `the`       varchar(300) NOT NULL DEFAULT '' COMMENT 'tag, cach nhau dau phay',
  `noi_dung`  mediumtext   NOT NULL COMMENT 'Markdown',
  `trang_thai` tinyint(4)  NOT NULL DEFAULT 0,
  `dang_luc`  datetime     DEFAULT NULL,
  `ghim`      tinyint(4)   NOT NULL DEFAULT 0 COMMENT '1 = ghim len trang chu',
  `tac_gia`   varchar(50)  NOT NULL DEFAULT '',
  `tao_luc`   datetime     NOT NULL DEFAULT current_timestamp(),
  `sua_luc`   datetime     NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_slug` (`slug`),
  KEY `idx_dang` (`trang_thai`, `dang_luc`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `web_trang` (
  `id`        bigint(20)   NOT NULL AUTO_INCREMENT,
  `slug`      varchar(120) NOT NULL,
  `tieu_de`   varchar(200) NOT NULL DEFAULT '',
  `nhom`      varchar(60)  NOT NULL DEFAULT 'Thế giới',
  `icon`      varchar(60)  NOT NULL DEFAULT 'i-lucide-file-text',
  `mo_ta`     varchar(500) NOT NULL DEFAULT '',
  `tu_khoa`   varchar(300) NOT NULL DEFAULT '',
  `thu_tu`    int(11)      NOT NULL DEFAULT 100,
  `noi_dung`  mediumtext   NOT NULL COMMENT 'Markdown; ## tieu de muc -> muc luc + tim kiem',
  `trang_thai` tinyint(4)  NOT NULL DEFAULT 1 COMMENT '0 nhap/an, 1 hien',
  `tac_gia`   varchar(50)  NOT NULL DEFAULT '',
  `sua_luc`   datetime     NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `web_faq` (
  `id`       bigint(20)    NOT NULL AUTO_INCREMENT,
  `nhom`     varchar(60)   NOT NULL DEFAULT 'Chung',
  `cau_hoi`  varchar(300)  NOT NULL DEFAULT '',
  `tra_loi`  mediumtext    NOT NULL COMMENT 'Markdown',
  `thu_tu`   int(11)       NOT NULL DEFAULT 100,
  `hien`     tinyint(4)    NOT NULL DEFAULT 1,
  `sua_luc`  datetime      NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `web_media` (
  `id`         bigint(20)   NOT NULL AUTO_INCREMENT,
  `ten_file`   varchar(120) NOT NULL COMMENT 'ten luu tren dia (ngau nhien)',
  `ten_goc`    varchar(200) NOT NULL DEFAULT '',
  `mime`       varchar(40)  NOT NULL DEFAULT '',
  `kich_thuoc` int(11)      NOT NULL DEFAULT 0,
  `rong`       int(11)      NOT NULL DEFAULT 0,
  `cao`        int(11)      NOT NULL DEFAULT 0,
  `tai_boi`    varchar(50)  NOT NULL DEFAULT '',
  `tai_luc`    datetime     NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ten_file` (`ten_file`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `web_su_kien_lich` (
  `tuan`        int(11)       NOT NULL,
  `thang`       int(11)       NOT NULL DEFAULT 1,
  `ten1`        varchar(200)  NOT NULL DEFAULT '',
  `ten2`        varchar(200)  NOT NULL DEFAULT '',
  `phan`        varchar(10)   NOT NULL DEFAULT '',
  `loai`        varchar(30)   NOT NULL DEFAULT 'pokemon',
  `co_che`      varchar(500)  NOT NULL DEFAULT '',
  `so_ngay`     int(11)       NOT NULL DEFAULT 7,
  `phan_thuong` varchar(300)  NOT NULL DEFAULT '',
  `moc`         varchar(100)  NOT NULL DEFAULT '',
  `sua_luc`     datetime      NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`tuan`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `web_admin_vai_tro` (
  `username`  varchar(50)  NOT NULL COMMENT 'account.team_user.username (chu thuong)',
  `vai_tro`   varchar(20)  NOT NULL DEFAULT 'xem',
  `ghi_chu`   varchar(200) NOT NULL DEFAULT '',
  `tao_boi`   varchar(50)  NOT NULL DEFAULT '',
  `tao_luc`   datetime     NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `web_khoa_tai_khoan` (
  `id`         bigint(20)   NOT NULL AUTO_INCREMENT,
  `username`   varchar(50)  NOT NULL,
  `loai`       varchar(20)  NOT NULL DEFAULT 'khoa' COMMENT 'khoa | cam_chat',
  `ly_do`      varchar(300) NOT NULL DEFAULT '',
  `het_han`    datetime     DEFAULT NULL,
  `admin_user` varchar(50)  NOT NULL DEFAULT '',
  `tao_luc`    datetime     NOT NULL DEFAULT current_timestamp(),
  `mo_luc`     datetime     DEFAULT NULL COMMENT 'NULL = con hieu luc',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`username`),
  KEY `idx_het_han` (`mo_luc`, `het_han`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `web_admin_thay_doi` (
  `id`         bigint(20)   NOT NULL AUTO_INCREMENT,
  `admin_user` varchar(50)  NOT NULL DEFAULT '',
  `hanh_dong`  varchar(50)  NOT NULL DEFAULT '',
  `doi_tuong`  varchar(150) NOT NULL DEFAULT '',
  `truoc`      mediumtext   NOT NULL,
  `sau`        mediumtext   NOT NULL,
  `ip`         varchar(45)  NOT NULL DEFAULT '',
  `thoi_gian`  datetime     NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_thoi_gian` (`thoi_gian`),
  KEY `idx_doi_tuong` (`doi_tuong`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- sua_cot_itemquest.sql
ALTER TABLE `tob_char_quest` MODIFY `itemquest` varchar(1000) DEFAULT NULL;

-- ======== 2. Du lieu thiet ke may chu doc luc khoi dong (P1): bang sinh, DROP + CREATE + INSERT

-- data_cap_do_lau_dai (tung la pokemon_lau_dai.sql, bo sinh tools/luu_tru_sinh_du_lieu/pokemon/sinh_sql_lau_dai.py - da luu tru)
DROP TABLE IF EXISTS `data_cap_do_lau_dai`;
CREATE TABLE `data_cap_do_lau_dai` (
  `cap` int NOT NULL,
  `exp_len_cap` bigint NOT NULL,
  `gio_tich_luy_phan_nghin` int NOT NULL,
  `mob_exp` int NOT NULL,
  `quai_moi_gio` int NOT NULL,
  `xu_moi_quai` int NOT NULL,
  `exp_cu` bigint NOT NULL,
  PRIMARY KEY (`cap`)
) DEFAULT CHARSET=utf8;
INSERT INTO `data_cap_do_lau_dai` VALUES
(1,215,0,20,359,10,200),
(2,306,30,27,357,11,600),
(3,443,62,37,356,12,1200),
(4,619,95,49,355,12,2000),
(5,893,131,67,354,13,4000),
(6,1260,169,90,352,13,8000),
(7,1810,208,122,351,14,16000),
(8,2560,251,164,350,15,16000),
(9,3650,295,222,348,16,24000),
(10,5210,342,300,347,16,36000),
(11,6510,392,335,346,17,54000),
(12,8100,449,374,344,18,64800),
(13,10100,512,417,343,19,90720),
(14,12600,582,466,342,20,127008),
(15,15800,662,520,340,21,177811),
(16,19700,751,580,339,22,248935),
(17,27500,851,647,338,23,261382),
(18,38500,976,722,337,24,339796),
(19,53700,1135,806,335,25,441735),
(20,75200,1333,900,334,27,574256),
(21,92300,1583,975,333,28,497688),
(22,113000,1868,1056,331,29,597226),
(23,139000,2191,1144,330,31,716671),
(24,170000,2558,1239,329,32,860005),
(25,209000,2975,1342,328,34,1032006),
(26,255000,3449,1453,326,36,1238407),
(27,314000,3989,1574,325,37,1486089),
(28,385000,4601,1705,324,39,1783307),
(29,471000,5298,1847,322,41,2139968),
(30,578000,6090,2000,321,43,2567962),
(31,667000,6990,2149,320,45,3081554),
(32,767000,7960,2309,318,48,3697865),
(33,886000,9005,2481,317,50,4437438),
(34,1020000,10131,2665,316,53,5324925),
(35,1180000,11344,2864,314,55,6389910),
(36,1360000,12652,3077,313,58,7667892),
(37,1570000,14061,3306,312,61,9201471),
(38,1810000,15580,3552,311,64,11041765),
(39,2080000,17216,3816,309,67,13250118),
(40,2400000,18979,4100,308,70,15900142),
(41,2630000,20879,4255,307,74,19080170),
(42,2870000,22893,4416,305,78,22896204),
(43,3150000,25027,4582,304,81,27475445),
(44,3450000,27290,4755,303,86,32970534),
(45,3790000,29688,4935,302,90,39564641),
(46,4140000,32229,5121,300,94,47477569),
(47,4540000,34923,5315,299,99,56973083),
(48,4970000,37779,5516,298,104,68367699),
(49,5430000,40805,5724,296,109,82041239),
(50,5960000,44013,5940,295,115,98449487),
(51,6530000,47413,6192,294,120,118139384),
(52,7130000,51000,6455,292,126,141767261),
(53,7810000,54783,6729,291,133,170120713),
(54,8560000,58774,7014,290,139,204144856),
(55,9350000,62983,7312,288,146,244973827),
(56,10200000,67424,7622,287,154,293968592),
(57,11200000,72108,7945,286,161,352762311),
(58,12300000,77050,8282,285,169,423314773),
(59,13400000,82262,8634,283,178,507977728),
(60,14700000,87761,9000,282,187,609573273),
(61,16200000,93561,9407,281,196,731487928),
(62,17800000,99703,9831,279,206,877785513),
(63,19700000,106209,10276,278,216,1053342616),
(64,21700000,113100,10740,277,227,1264011139),
(65,23900000,120398,11225,276,238,1516813367),
(66,26300000,128127,11732,274,250,1820176040),
(67,29000000,136313,12262,273,263,2184211248),
(68,32000000,144983,12816,272,276,2621053498),
(69,35200000,154165,13395,270,290,3145264198),
(70,38800000,163890,14000,269,304,3774317037),
(71,43100000,174190,14775,268,319,11322951112),
(72,47700000,185076,15594,266,335,16984426668),
(73,53000000,196579,16457,265,352,25476640002),
(74,58900000,208737,17368,264,370,38214960003),
(75,65200000,221585,18330,262,388,57322440005),
(76,72500000,235163,19345,261,408,85983660007),
(77,80500000,249513,20417,260,428,128975490011),
(78,89400000,264678,21547,259,450,193463235016),
(79,99000000,280705,22741,257,472,290194852524),
(80,110000000,297643,24000,256,496,435292278786),
(81,120000000,315543,24909,255,520,652938418179),
(82,130000000,334398,25853,253,546,979407627269),
(83,141000000,354258,26833,252,574,1469111440903),
(84,154000000,375179,27850,251,602,2203667161355),
(85,168000000,397215,28905,250,633,3305500742033),
(86,182000000,420427,30000,248,664,4958251113049),
(87,197000000,444877,30954,247,697,7437376669573),
(88,213000000,470631,31937,246,732,11156065004360),
(89,230000000,497760,32953,244,769,16734097506540),
(90,249000000,526335,34000,243,807,25101146259810),
(91,264000000,556435,34967,242,848,0),
(92,279000000,587643,35960,240,890,0),
(93,297000000,619998,36983,239,935,0),
(94,315000000,653545,38034,238,981,0),
(95,333000000,688325,39115,236,1030,0),
(96,353000000,724385,40227,235,1082,0),
(97,375000000,761771,41371,234,1136,0),
(98,398000000,800534,42547,233,1193,0),
(99,421000000,840722,43756,231,1252,0),
(100,0,882389,45000,230,1315,0);

-- data_nang_cap_lau_dai (tung la pokemon_lau_dai.sql, bo sinh tools/luu_tru_sinh_du_lieu/pokemon/sinh_sql_lau_dai.py - da luu tru)
DROP TABLE IF EXISTS `data_nang_cap_lau_dai`;
CREATE TABLE `data_nang_cap_lau_dai` (
  `bac` int NOT NULL,
  `ten_bac` varchar(20) NOT NULL,
  `cap_bac` int NOT NULL,
  `cong` int NOT NULL,
  `ti_le` int NOT NULL,
  `pity_moi_lan` int NOT NULL,
  `xu` bigint NOT NULL,
  `da` int NOT NULL,
  `bua_luong_khoa` int NOT NULL,
  `mat_cap` tinyint NOT NULL,
  `chi_so_phan_tram` int NOT NULL,
  PRIMARY KEY (`bac`,`cong`)
) DEFAULT CHARSET=utf8;
INSERT INTO `data_nang_cap_lau_dai` VALUES
(0,'sơ cấp',1,1,100,34,215,1,0,0,3),
(0,'sơ cấp',1,2,100,34,280,1,0,0,6),
(0,'sơ cấp',1,3,95,32,364,1,0,0,9),
(0,'sơ cấp',1,4,90,30,473,1,0,0,12),
(0,'sơ cấp',1,5,85,29,615,1,0,0,15),
(0,'sơ cấp',1,6,75,25,800,2,0,0,18),
(0,'sơ cấp',1,7,65,22,1040,2,10,1,21),
(0,'sơ cấp',1,8,55,19,1352,2,10,1,24),
(0,'sơ cấp',1,9,45,15,1757,2,10,1,27),
(0,'sơ cấp',1,10,38,13,2284,2,10,1,30),
(0,'sơ cấp',1,11,30,10,2969,4,25,1,35),
(0,'sơ cấp',1,12,24,8,3860,4,25,1,40),
(0,'sơ cấp',1,13,18,6,5018,4,25,1,45),
(0,'sơ cấp',1,14,13,5,6524,4,25,1,50),
(0,'sơ cấp',1,15,10,4,8481,4,25,1,55),
(1,'đồng',10,1,100,34,333,1,0,0,3),
(1,'đồng',10,2,100,34,433,1,0,0,6),
(1,'đồng',10,3,95,32,563,1,0,0,9),
(1,'đồng',10,4,90,30,732,1,0,0,12),
(1,'đồng',10,5,85,29,951,1,0,0,15),
(1,'đồng',10,6,75,25,1237,2,0,0,18),
(1,'đồng',10,7,65,22,1608,2,10,1,21),
(1,'đồng',10,8,55,19,2090,2,10,1,24),
(1,'đồng',10,9,45,15,2717,2,10,1,27),
(1,'đồng',10,10,38,13,3533,2,10,1,30),
(1,'đồng',10,11,30,10,4592,4,25,1,35),
(1,'đồng',10,12,24,8,5970,4,25,1,40),
(1,'đồng',10,13,18,6,7761,4,25,1,45),
(1,'đồng',10,14,13,5,10089,4,25,1,50),
(1,'đồng',10,15,10,4,13116,4,25,1,55),
(2,'bạc',20,1,100,34,541,1,0,0,3),
(2,'bạc',20,2,100,34,703,1,0,0,6),
(2,'bạc',20,3,95,32,914,1,0,0,9),
(2,'bạc',20,4,90,30,1189,1,0,0,12),
(2,'bạc',20,5,85,29,1545,1,0,0,15),
(2,'bạc',20,6,75,25,2009,2,0,0,18),
(2,'bạc',20,7,65,22,2612,2,10,1,21),
(2,'bạc',20,8,55,19,3395,2,10,1,24),
(2,'bạc',20,9,45,15,4414,2,10,1,27),
(2,'bạc',20,10,38,13,5738,2,10,1,30),
(2,'bạc',20,11,30,10,7459,4,25,1,35),
(2,'bạc',20,12,24,8,9697,4,25,1,40),
(2,'bạc',20,13,18,6,12606,4,25,1,45),
(2,'bạc',20,14,13,5,16388,4,25,1,50),
(2,'bạc',20,15,10,4,21304,4,25,1,55),
(3,'vàng',30,1,100,34,828,1,0,0,3),
(3,'vàng',30,2,100,34,1077,1,0,0,6),
(3,'vàng',30,3,95,32,1400,1,0,0,9),
(3,'vàng',30,4,90,30,1819,1,0,0,12),
(3,'vàng',30,5,85,29,2365,1,0,0,15),
(3,'vàng',30,6,75,25,3075,2,0,0,18),
(3,'vàng',30,7,65,22,3997,2,10,1,21),
(3,'vàng',30,8,55,19,5197,2,10,1,24),
(3,'vàng',30,9,45,15,6756,2,10,1,27),
(3,'vàng',30,10,38,13,8782,2,10,1,30),
(3,'vàng',30,11,30,10,11417,4,25,1,35),
(3,'vàng',30,12,24,8,14842,4,25,1,40),
(3,'vàng',30,13,18,6,19295,4,25,1,45),
(3,'vàng',30,14,13,5,25083,4,25,1,50),
(3,'vàng',30,15,10,4,32608,4,25,1,55),
(4,'bạch kim',40,1,100,34,1294,1,0,0,3),
(4,'bạch kim',40,2,100,34,1682,1,0,0,6),
(4,'bạch kim',40,3,95,32,2186,1,0,0,9),
(4,'bạch kim',40,4,90,30,2842,1,0,0,12),
(4,'bạch kim',40,5,85,29,3695,1,0,0,15),
(4,'bạch kim',40,6,75,25,4803,2,0,0,18),
(4,'bạch kim',40,7,65,22,6244,2,10,1,21),
(4,'bạch kim',40,8,55,19,8117,2,10,1,24),
(4,'bạch kim',40,9,45,15,10552,2,10,1,27),
(4,'bạch kim',40,10,38,13,13718,2,10,1,30),
(4,'bạch kim',40,11,30,10,17833,4,25,1,35),
(4,'bạch kim',40,12,24,8,23183,4,25,1,40),
(4,'bạch kim',40,13,18,6,30138,4,25,1,45),
(4,'bạch kim',40,14,13,5,39180,4,25,1,50),
(4,'bạch kim',40,15,10,4,50934,4,25,1,55),
(5,'kim cương',50,1,100,34,2036,1,0,0,3),
(5,'kim cương',50,2,100,34,2646,1,0,0,6),
(5,'kim cương',50,3,95,32,3440,1,0,0,9),
(5,'kim cương',50,4,90,30,4472,1,0,0,12),
(5,'kim cương',50,5,85,29,5814,1,0,0,15),
(5,'kim cương',50,6,75,25,7558,2,0,0,18),
(5,'kim cương',50,7,65,22,9825,2,10,1,21),
(5,'kim cương',50,8,55,19,12773,2,10,1,24),
(5,'kim cương',50,9,45,15,16604,2,10,1,27),
(5,'kim cương',50,10,38,13,21586,2,10,1,30),
(5,'kim cương',50,11,30,10,28061,4,25,1,35),
(5,'kim cương',50,12,24,8,36480,4,25,1,40),
(5,'kim cương',50,13,18,6,47424,4,25,1,45),
(5,'kim cương',50,14,13,5,61651,4,25,1,50),
(5,'kim cương',50,15,10,4,80146,4,25,1,55),
(6,'chuyên gia',60,1,100,34,3164,1,0,0,3),
(6,'chuyên gia',60,2,100,34,4113,1,0,0,6),
(6,'chuyên gia',60,3,95,32,5347,1,0,0,9),
(6,'chuyên gia',60,4,90,30,6951,1,0,0,12),
(6,'chuyên gia',60,5,85,29,9037,1,0,0,15),
(6,'chuyên gia',60,6,75,25,11748,2,0,0,18),
(6,'chuyên gia',60,7,65,22,15272,2,10,1,21),
(6,'chuyên gia',60,8,55,19,19854,2,10,1,24),
(6,'chuyên gia',60,9,45,15,25810,2,10,1,27),
(6,'chuyên gia',60,10,38,13,33553,2,10,1,30),
(6,'chuyên gia',60,11,30,10,43619,4,25,1,35),
(6,'chuyên gia',60,12,24,8,56705,4,25,1,40),
(6,'chuyên gia',60,13,18,6,73716,4,25,1,45),
(6,'chuyên gia',60,14,13,5,95831,4,25,1,50),
(6,'chuyên gia',60,15,10,4,124580,4,25,1,55),
(7,'tinh anh',70,1,100,34,4907,1,0,0,3),
(7,'tinh anh',70,2,100,34,6379,1,0,0,6),
(7,'tinh anh',70,3,95,32,8292,1,0,0,9),
(7,'tinh anh',70,4,90,30,10780,1,0,0,12),
(7,'tinh anh',70,5,85,29,14014,1,0,0,15),
(7,'tinh anh',70,6,75,25,18218,2,0,0,18),
(7,'tinh anh',70,7,65,22,23683,2,10,1,21),
(7,'tinh anh',70,8,55,19,30788,2,10,1,24),
(7,'tinh anh',70,9,45,15,40024,2,10,1,27),
(7,'tinh anh',70,10,38,13,52032,2,10,1,30),
(7,'tinh anh',70,11,30,10,67641,4,25,1,35),
(7,'tinh anh',70,12,24,8,87933,4,25,1,40),
(7,'tinh anh',70,13,18,6,114313,4,25,1,45),
(7,'tinh anh',70,14,13,5,148607,4,25,1,50),
(7,'tinh anh',70,15,10,4,193190,4,25,1,55),
(8,'huyền thoại',80,1,100,34,7619,1,0,0,3),
(8,'huyền thoại',80,2,100,34,9904,1,0,0,6),
(8,'huyền thoại',80,3,95,32,12875,1,0,0,9),
(8,'huyền thoại',80,4,90,30,16738,1,0,0,12),
(8,'huyền thoại',80,5,85,29,21759,1,0,0,15),
(8,'huyền thoại',80,6,75,25,28287,2,0,0,18),
(8,'huyền thoại',80,7,65,22,36773,2,10,1,21),
(8,'huyền thoại',80,8,55,19,47805,2,10,1,24),
(8,'huyền thoại',80,9,45,15,62147,2,10,1,27),
(8,'huyền thoại',80,10,38,13,80791,2,10,1,30),
(8,'huyền thoại',80,11,30,10,105028,4,25,1,35),
(8,'huyền thoại',80,12,24,8,136537,4,25,1,40),
(8,'huyền thoại',80,13,18,6,177498,4,25,1,45),
(8,'huyền thoại',80,14,13,5,230747,4,25,1,50),
(8,'huyền thoại',80,15,10,4,299971,4,25,1,55),
(9,'thần thoại',90,1,100,34,11766,1,0,0,3),
(9,'thần thoại',90,2,100,34,15296,1,0,0,6),
(9,'thần thoại',90,3,95,32,19885,1,0,0,9),
(9,'thần thoại',90,4,90,30,25850,1,0,0,12),
(9,'thần thoại',90,5,85,29,33605,1,0,0,15),
(9,'thần thoại',90,6,75,25,43687,2,0,0,18),
(9,'thần thoại',90,7,65,22,56793,2,10,1,21),
(9,'thần thoại',90,8,55,19,73830,2,10,1,24),
(9,'thần thoại',90,9,45,15,95980,2,10,1,27),
(9,'thần thoại',90,10,38,13,124773,2,10,1,30),
(9,'thần thoại',90,11,30,10,162205,4,25,1,35),
(9,'thần thoại',90,12,24,8,210867,4,25,1,40),
(9,'thần thoại',90,13,18,6,274127,4,25,1,45),
(9,'thần thoại',90,14,13,5,356365,4,25,1,50),
(9,'thần thoại',90,15,10,4,463275,4,25,1,55),
(10,'tối thượng',98,1,100,34,16678,1,0,0,3),
(10,'tối thượng',98,2,100,34,21682,1,0,0,6),
(10,'tối thượng',98,3,95,32,28186,1,0,0,9),
(10,'tối thượng',98,4,90,30,36642,1,0,0,12),
(10,'tối thượng',98,5,85,29,47634,1,0,0,15),
(10,'tối thượng',98,6,75,25,61925,2,0,0,18),
(10,'tối thượng',98,7,65,22,80502,2,10,1,21),
(10,'tối thượng',98,8,55,19,104653,2,10,1,24),
(10,'tối thượng',98,9,45,15,136049,2,10,1,27),
(10,'tối thượng',98,10,38,13,176864,2,10,1,30),
(10,'tối thượng',98,11,30,10,229923,4,25,1,35),
(10,'tối thượng',98,12,24,8,298899,4,25,1,40),
(10,'tối thượng',98,13,18,6,388569,4,25,1,45),
(10,'tối thượng',98,14,13,5,505140,4,25,1,50),
(10,'tối thượng',98,15,10,4,656682,4,25,1,55);

-- data_kinh_te_lau_dai (tung la pokemon_lau_dai.sql, bo sinh tools/luu_tru_sinh_du_lieu/pokemon/sinh_sql_lau_dai.py - da luu tru)
DROP TABLE IF EXISTS `data_kinh_te_lau_dai`;
CREATE TABLE `data_kinh_te_lau_dai` (
  `khoa` varchar(64) NOT NULL,
  `gia_tri` varchar(64) NOT NULL,
  `gia_tri_cu` varchar(64) NOT NULL,
  `ghi_chu` varchar(255) NOT NULL,
  PRIMARY KEY (`khoa`)
) DEFAULT CHARSET=utf8;
INSERT INTO `data_kinh_te_lau_dai` VALUES
('roi.xu.phan_tram','30','0.32','Xu rơi mỗi quái (%); lượng = xu_moi_quai/0.3'),
('roi.luong.phan_tram','0','2.5','Bỏ lượng rơi từ quái (Monster.java:1091-1108)'),
('roi.luong_khoa.phan_tram','0','2.5','Bỏ lượng khoá rơi từ quái'),
('roi.da_nang_cap.phan_nghin','15','0','Đá nâng cấp khi |lệch cấp| <= 5'),
('roi.trang_bi.ppm','150','150','Giữ; thêm phân rã đồ trùng'),
('exp.tang_exp','1','3','Bỏ x3 toàn cục (CharCapNhat.java:127)'),
('exp.tile_lv50','100','10','Bỏ giảm 90% từ cấp 50 (bảng EXP mới đã tính)'),
('exp.tile_duoi_lv50','100','50','Bỏ giảm 50%'),
('exp.chia3_lv60','0','1','Bỏ chia 3 từ cấp 60'),
('exp.lech_cap','100,100,100,100,80,80,80,50,50,50,20,20,20,20,20,5','1/5/10/20','% EXP theo lệch cấp người - quái = 0..15+'),
('exp.nang_luong_phut','90','0','Phút 100% mỗi ngày'),
('exp.nang_luong_tich_phut','180','0','Tích luỹ tối đa'),
('exp.bac_met','120:20,120:5','0','Sau giờ 100%: 120 phút 20%, 120 phút 5%, rồi 0'),
('thue.cho','6','10','Thuế chợ ký gửi (đốt)'),
('thue.dang_ban','1','0','Phí đăng bán (đốt, không hoàn)'),
('thue.chuyen_luong','5','0','Phí chuyển lượng NPC (%)'),
('tieu_hao.thuoc_phan_tram_thu_nhap','12','-','Giá thuốc chỉnh theo bậc'),
('tieu_hao.sua_do_phan_tram_thu_nhap','8','-','Σ price/10 giữ, price theo bậc'),
('nang_cap.xu_moi_lan_cu','0','5000','Thay bằng data_nang_cap_lau_dai.xu'),
('qua.cap35_xu','0','10000000','Bỏ (MapXuLyLenh.java:303); thay bằng trang bị bậc vàng'),
('giftcode.toi_da_thang','2','-','Chỉ vật phẩm khoá, ≤ 0,5 giờ thu nhập'),
('ve_chuong.tuan','2','-','Vé mở chương chính / tuần'),
('ve_chuong.toi_da','4','-','');

-- data_su_kien_nam (tung la pokemon_lau_dai.sql, bo sinh tools/luu_tru_sinh_du_lieu/pokemon/sinh_sql_lau_dai.py - da luu tru)
DROP TABLE IF EXISTS `data_su_kien_nam`;
CREATE TABLE `data_su_kien_nam` (
  `tuan` int NOT NULL,
  `thang` int NOT NULL,
  `ten_nam1` varchar(255) NOT NULL,
  `ten_nam2` varchar(255) NOT NULL,
  `loai` varchar(255) NOT NULL,
  `co_che` varchar(255) NOT NULL,
  `so_ngay` int NOT NULL,
  `ma_su_kien` varchar(255) NOT NULL,
  `tai_dung` varchar(255) NOT NULL,
  `ngan_sach_gio` decimal(5,2) NOT NULL,
  `phan_xu_phan_tram` int NOT NULL,
  `phan_thuong` varchar(255) NOT NULL,
  `moc_giu_chan` varchar(255) NOT NULL,
  PRIMARY KEY (`tuan`)
) DEFAULT CHARSET=utf8;
INSERT INTO `data_su_kien_nam` VALUES
(1,1,'Tết dương lịch','Tết dương lịch: Pokémon Tuyết','le_vn','Cây thông đổi 10 kẹo lấy hộp quà (khoá)',7,'tetduonglich','SuKien.TET_DUONG_LICH, npc -35',2.0,25,'Hộp quà khoá, khung avatar năm mới','Đăng nhập 7 ngày'),
(2,1,'Tuần hệ Băng','Tuần hệ Rồng','tuan_he','Quái hệ X xuất hiện ×2, chiêu hệ X +10% (thời tiết)',7,'moi:tuanhe','MapNapTaiNguyen spawn + ChieuPokemon',1.0,25,'Phiến đá hệ trang trí','Mục tiêu tuần'),
(3,1,'Ngày cộng đồng Charmander','Ngày cộng đồng Squirtle','cong_dong','3 giờ: dòng khởi đầu xuất hiện dày, shiny ×5',7,'moi:congdong','SuKien + spawn',1.0,25,'Trang phục khởi đầu (không chỉ số)','Giờ vàng chung'),
(4,1,'Chuẩn bị Tết: gói bánh','Chuẩn bị Tết: chợ hoa','le_vn','Nồi bánh chưng/bánh tét theo đội',7,'tet2017','MapSuKien nấu bánh, tob_nau_banh',1.5,25,'Bánh (thuốc khoá), lì xì khoá','Hoạt động đội'),
(5,1,'Tết Nguyên Đán (theo âm lịch: tuần 4-7) (tuần 1/2)','Tết Nguyên Đán: Lân Litleo','le_vn','Cây lì xì Sudowoodo, lì xì khoá, top Tết theo điểm (không theo nạp)',14,'tet2017,tet123','SuKien.TET_2017/TET_123',3.0,25,'Áo Tết (không chỉ số), danh hiệu năm','Đỉnh năm: quay lại'),
(6,2,'Tết Nguyên Đán (theo âm lịch: tuần 4-7) (tuần 2/2)','Tết Nguyên Đán: Lân Litleo','le_vn','Cây lì xì Sudowoodo, lì xì khoá, top Tết theo điểm (không theo nạp)',7,'tet2017,tet123','SuKien.TET_2017/TET_123',3.0,25,'Áo Tết (không chỉ số), danh hiệu năm','Đỉnh năm: quay lại'),
(7,2,'Tuần Shiny','Tuần Shiny: Johto','pokemon','Tỉ lệ shiny ×10, chắc chắn gặp shiny sau 2.000 lần hạ quái',7,'moi:shiny','tob_pokedex + cột shiny (GĐ2)',1.0,25,'Huy hiệu shiny','Sưu tầm'),
(8,2,'Valentine: Luvdisc','Valentine: Alomomola','le_vn','Tặng hoa cho bạn bè (Hoa hồng bán ở cửa hàng), cặp đôi cùng đội được thêm EXP',7,'moi:valentine','data_shop Hoa hồng + MapSuKien',1.0,25,'Khung đôi, hiệu ứng trái tim','Xã hội'),
(9,2,'Đột kích huyền thoại: Zapdos','Đột kích: Articuno','dot_kich','Boss thế giới 2 lần/ngày, đi nhóm từ 5 thành viên trở lên',7,'moi:dotkich','BossDotKich (Zapdos 82)',1.5,25,'Lông vũ Zapdos (nguyên liệu khoá)','Hẹn giờ chung'),
(10,3,'Quốc tế Phụ nữ 8/3','8/3: Gardevoir','le_vn','Quà 8/3, hoa hồng; nhiệm vụ tặng hoa',7,'quocte83','SuKien.QUOC_TE_8_3',1.0,25,'Ruy băng trang trí','Xã hội'),
(11,3,'Tuần hệ Cỏ','Tuần hệ Tiên','tuan_he','Như tuần 2',7,'moi:tuanhe','spawn',1.0,25,'Phiến đá trang trí','Mục tiêu tuần'),
(12,3,'Ngày cộng đồng Bulbasaur','Ngày cộng đồng Pichu','cong_dong','Như tuần 3',7,'moi:congdong','spawn',1.0,25,'Trang phục','Giờ vàng chung'),
(13,3,'Mùa giải PvP 1 kết thúc','Mùa giải PvP 5 kết thúc','pvp','Chốt bậc xếp hạng (từ Poké đến Master), phát thưởng mùa',7,'moi:muapvp','MapLienDau/ArenaData, tob_char_thach_dau',1.0,25,'Khung bậc mùa, lượng theo bậc','Cạnh tranh'),
(14,4,'Lễ hội Pokédex','Lễ hội Pokédex: Johto','pokemon','Mốc Pokédex ×2 điểm nghiên cứu',7,'moi:pokedex','tob_pokedex_thuong',1.0,25,'Danh hiệu nghiên cứu','Sưu tầm'),
(15,4,'Giỗ Tổ Hùng Vương','Giỗ Tổ: bánh giày','le_vn','Bánh giày, pháo hoa (EXP theo % cấp, có giới hạn)',7,'gioto','SuKien.GIO_TO',1.5,25,'Pháo hoa trang trí','Giờ vàng chung'),
(16,4,'Chiến vùng (Kanto/Johto/Hoenn)','Chiến vùng','chien_vung','Thứ Bảy 20h: 3 vùng giành cờ',7,'moi:chienvung','MapChienTruongMoba, Moba boss',1.0,25,'Cờ vùng (trang trí)','Đồng đội'),
(17,4,'30/4 - 1/5: Thống nhất','30/4: Diễu hành Pokémon','le_vn','Đường đua Ponyta, nhiệm vụ tuyến',7,'moi:3004','Thú cưỡi Ponyta/Rapidash',1.5,25,'Yên thú cưỡi trang trí','Nghỉ lễ'),
(18,4,'Tuần hệ Lửa','Tuần hệ Ma','tuan_he','Như tuần 2',7,'moi:tuanhe','spawn',1.0,25,'Phiến đá trang trí','Mục tiêu tuần'),
(19,5,'Ngày cộng đồng Squirtle','Ngày cộng đồng Eevee','cong_dong','Như tuần 3',7,'moi:congdong','spawn',1.0,25,'Trang phục','Giờ vàng chung'),
(20,5,'Đột kích: Entei','Đột kích: Raikou','dot_kich','Như tuần 9',7,'moi:dotkich','Entei 138',1.5,25,'Nanh lửa Entei (khoá)','Hẹn giờ chung'),
(21,5,'Bùng phát quần thể','Bùng phát: Johto','pokemon','Mỗi ngày 1 tuyến đường có 1 loài xuất hiện ×5',7,'moi:bungphat','spawn',1.0,25,'Điểm Pokédex','Bất ngờ'),
(22,5,'Quốc tế Thiếu nhi 1/6','1/6: Pichu & bạn','le_vn','Nhiệm vụ dẫn dắt người mới (thưởng Sư phạm ×2)',7,'moi:thieunhi','Sư phạm',1.0,25,'Mũ trẻ em','Người mới'),
(23,6,'Sự kiện Hè: biển Vermilion (tuần 1/2)','Hè: đảo Cinnabar','mua','Kem, dưa hấu, câu cá; thêm 50% thời gian năng lượng 100% (năng lượng hè)',14,'he2017','SuKien.HE_2017',1.5,25,'Đồ bơi trang trí','Mùa hè: nhiều giờ chơi'),
(24,6,'Sự kiện Hè: biển Vermilion (tuần 2/2)','Hè: đảo Cinnabar','mua','Kem, dưa hấu, câu cá; thêm 50% thời gian năng lượng 100% (năng lượng hè)',7,'he2017','SuKien.HE_2017',1.5,25,'Đồ bơi trang trí','Mùa hè: nhiều giờ chơi'),
(25,6,'Ngày cộng đồng Pichu','Ngày cộng đồng Bulbasaur','cong_dong','Như tuần 3',7,'moi:congdong','spawn',1.0,25,'Trang phục','Giờ vàng chung'),
(26,6,'Mùa giải PvP 2 kết thúc','Mùa giải PvP 6 kết thúc','pvp','Như tuần 13',7,'moi:muapvp','MapLienDau',1.0,25,'Khung bậc mùa','Cạnh tranh'),
(27,7,'Tuần hệ Nước','Tuần hệ Đất','tuan_he','Như tuần 2',7,'moi:tuanhe','spawn',1.0,25,'Phiến đá','Mục tiêu tuần'),
(28,7,'Đột kích: Darkrai','Đột kích: Suicune','dot_kich','Như tuần 9',7,'moi:dotkich','BossDracula (Darkrai 116)',1.5,25,'Mảnh bóng tối (khoá)','Hẹn giờ chung'),
(29,7,'Hè 2: Hầm ngục bí ẩn mùa hè','Hè 2: Hầm ngục sa mạc','mua','Hầm ngục bí ẩn ×2 điểm xếp hạng',7,'moi:hamngucmua','Hầm ngục (GĐ3)',1.5,25,'Khung hầm ngục','Endgame'),
(30,7,'Chiến vùng mùa hè','Chiến vùng','chien_vung','Như tuần 16',7,'moi:chienvung','Moba',1.0,25,'Cờ vùng','Đồng đội'),
(31,7,'Ngày cộng đồng Eevee','Ngày cộng đồng Charmander','cong_dong','Như tuần 3 + đá tiến hoá ×2',7,'moi:congdong','DaTienHoa',1.0,25,'Trang phục Eevee','Giờ vàng chung'),
(32,8,'Vu Lan','Vu Lan: hoa hồng vàng','le_vn','Nhánh hoa hồng vàng, đổi quà khoá',7,'vulan','SuKien.VU_LAN',1.0,25,'Hoa cài trang trí','Gia đình'),
(33,8,'Tuần hệ Điện','Tuần hệ Thép','tuan_he','Như tuần 2',7,'moi:tuanhe','spawn',1.0,25,'Phiến đá','Mục tiêu tuần'),
(34,8,'Tuần Shiny 2','Tuần Shiny: huyền thoại','pokemon','Như tuần 7',7,'moi:shiny','tob_pokedex',1.0,25,'Huy hiệu shiny','Sưu tầm'),
(35,8,'Quốc khánh 2/9','2/9: lễ hội đèn','le_vn','Pháo hoa, nhiệm vụ tuyến toàn quốc',7,'moi:quockhanh','MapSuKien pháo hoa',1.5,25,'Pháo hoa trang trí','Nghỉ lễ'),
(36,9,'Đột kích: Rayquaza','Đột kích: Lugia','dot_kich','Như tuần 9',7,'moi:dotkich','BossTruRong (120)',1.5,25,'Vảy Rayquaza (khoá)','Hẹn giờ chung'),
(37,9,'Trung Thu (tuần 1/2)','Trung Thu: Clefairy','le_vn','Bánh nướng/bánh dẻo, lồng đèn, thả đèn theo đội',14,'trungthu','SuKien.TRUNG_THU, Shop Trung Thu',1.5,25,'Lồng đèn trang trí','Gia đình'),
(38,9,'Trung Thu (tuần 2/2)','Trung Thu: Clefairy','le_vn','Bánh nướng/bánh dẻo, lồng đèn, thả đèn theo đội',7,'trungthu','SuKien.TRUNG_THU, Shop Trung Thu',1.5,25,'Lồng đèn trang trí','Gia đình'),
(39,9,'Mùa giải PvP 3 kết thúc','Mùa giải PvP 7 kết thúc','pvp','Như tuần 13',7,'moi:muapvp','MapLienDau',1.0,25,'Khung bậc mùa','Cạnh tranh'),
(40,10,'Ngày cộng đồng Charmander 2','Ngày cộng đồng Squirtle 2','cong_dong','Như tuần 3',7,'moi:congdong','spawn',1.0,25,'Trang phục','Giờ vàng chung'),
(41,10,'Phụ nữ Việt Nam 20/10','20/10: Sylveon','le_vn','Hoa, nhiệm vụ tặng quà',7,'moi:2010','data_shop hoa',1.0,25,'Ruy băng trang trí','Xã hội'),
(42,10,'Tuần hệ Siêu linh','Tuần hệ Giác đấu','tuan_he','Như tuần 2',7,'moi:tuanhe','spawn',1.0,25,'Phiến đá','Mục tiêu tuần'),
(43,10,'Halloween: Gengar (tuần 1/2)','Halloween: Mimikyu','mua','Bí ngô, mặt nạ, boss Darkrai đêm 31/10',14,'halloween,demhalloween','SuKien.HALLOWEEN',1.5,25,'Mặt nạ trang trí','Bất ngờ'),
(44,10,'Halloween: Gengar (tuần 2/2)','Halloween: Mimikyu','mua','Bí ngô, mặt nạ, boss Darkrai đêm 31/10',7,'halloween,demhalloween','SuKien.HALLOWEEN',1.5,25,'Mặt nạ trang trí','Bất ngờ'),
(45,11,'Lễ hội Pokédex 2','Lễ hội Pokédex: Hoenn','pokemon','Như tuần 14',7,'moi:pokedex','tob_pokedex_thuong',1.0,25,'Danh hiệu','Sưu tầm'),
(46,11,'Nhà giáo 20/11','20/11: thầy và trò','le_vn','Thưởng Sư phạm ×2, chổi băng/độc/điện',7,'sinhnhat2011','SuKien.SINH_NHAT_2011',1.0,25,'Danh hiệu người thầy','Người mới'),
(47,11,'Black Friday (giảm giá cửa hàng xu)','Black Friday','mua','Giảm 30% đồ trang trí mua bằng xu',7,'blackfriday','SuKien.BLACK_FRIDAY',0.5,25,'—','Hố hút xu'),
(48,11,'Đột kích: Groudon & Kyogre','Đột kích: Kyogre & Groudon','dot_kich','2 boss cùng lúc',7,'moi:dotkich','BossKyogreGroudon (129/130)',1.5,25,'Ngọc đỏ Groudon, Ngọc lam Kyogre (khoá)','Hẹn giờ chung'),
(49,12,'Chiến vùng mùa đông','Chiến vùng','chien_vung','Như tuần 16',7,'moi:chienvung','Moba',1.0,25,'Cờ vùng','Đồng đội'),
(50,12,'Ngày cộng đồng Bulbasaur 2','Ngày cộng đồng Pichu 2','cong_dong','Như tuần 3',7,'moi:congdong','spawn',1.0,25,'Trang phục','Giờ vàng chung'),
(51,12,'Giáng sinh: Delibird','Giáng sinh: Abomasnow','le_vn','Cây thông Abomasnow, quà Delibird, đêm 24/12',7,'noel,demnoel','SuKien.NOEL',2.0,25,'Mũ Noel trang trí','Đỉnh năm'),
(52,12,'Tổng kết năm + mùa giải PvP 4','Tổng kết năm 2','pvp','Bảng xếp hạng năm, kỷ niệm máy chủ',7,'moi:tongket','SuKienDuaTop, tob_top_thuong',2.0,25,'Danh hiệu năm','Cạnh tranh');

-- data_hst_ho_cau (tung la pokemon_he_sinh_thai.sql, bo sinh tools/luu_tru_sinh_du_lieu/pokemon/sinh_sql_he_sinh_thai.py - da luu tru)
DROP TABLE IF EXISTS `data_hst_ho_cau`;
CREATE TABLE `data_hst_ho_cau` (
  `map_id` int NOT NULL,
  `ho_id` int NOT NULL,
  `so_o` int NOT NULL,
  `x` int NOT NULL,
  `y` int NOT NULL,
  PRIMARY KEY (`map_id`,`ho_id`,`x`,`y`)
) DEFAULT CHARSET=utf8;
INSERT INTO `data_hst_ho_cau` VALUES
(0,0,48,43,45),
(0,0,48,42,46),
(0,0,48,40,49),
(0,0,48,43,52),
(1,0,148,44,64),
(1,0,148,51,66),
(1,0,148,39,71),
(1,0,148,42,76),
(1,1,13,66,65),
(1,1,13,64,67),
(1,1,13,69,68),
(1,1,13,65,70),
(2,0,27,31,5),
(2,0,27,30,6),
(2,0,27,34,8),
(2,0,27,28,10),
(3,0,2345,37,7),
(3,0,2345,24,16),
(3,0,2345,74,26),
(3,0,2345,40,39),
(3,1,813,40,54),
(3,1,813,42,59),
(3,1,813,62,71),
(3,1,813,80,74),
(3,2,12,21,56),
(3,2,12,20,57),
(3,2,12,25,58),
(3,2,12,21,60),
(3,3,13,37,73),
(3,3,13,35,75),
(3,3,13,40,76),
(3,3,13,36,78),
(4,0,23,17,33),
(4,0,23,19,35),
(4,0,23,20,37),
(4,0,23,19,39),
(5,0,146,17,6),
(5,0,146,13,10),
(5,0,146,10,15),
(5,0,146,11,20),
(5,1,12,29,35),
(5,1,12,28,36),
(5,1,12,33,37),
(5,1,12,29,39),
(6,0,21,17,2),
(6,0,21,16,3),
(6,0,21,15,5),
(6,0,21,20,7),
(7,0,2633,28,7),
(7,0,2633,43,35),
(7,0,2633,41,61),
(7,0,2633,52,77),
(7,1,13,13,8),
(7,1,13,11,10),
(7,1,13,16,11),
(7,1,13,12,13),
(8,0,39,7,5),
(8,0,39,5,7),
(8,0,39,14,9),
(8,0,39,9,11),
(9,0,13,44,14),
(9,0,13,42,16),
(9,0,13,47,17),
(9,0,13,43,19),
(9,1,93,12,42),
(9,1,93,16,44),
(9,1,93,17,48),
(9,1,93,8,53),
(10,0,13,62,67),
(10,0,13,63,68),
(10,0,13,59,70),
(10,0,13,64,71),
(10,1,20,63,108),
(10,1,20,62,111),
(10,1,20,63,112),
(10,1,20,65,114),
(12,0,52,27,13),
(12,0,52,31,15),
(12,0,52,33,18),
(12,0,52,31,21),
(14,0,121,20,24),
(14,0,121,25,27),
(14,0,121,13,32),
(14,0,121,24,36),
(15,0,38,50,27),
(15,0,38,50,29),
(15,0,38,57,31),
(15,0,38,53,34),
(17,0,13,72,8),
(17,0,13,70,10),
(17,0,13,75,11),
(17,0,13,71,13),
(17,1,13,78,57),
(17,1,13,76,59),
(17,1,13,81,60),
(17,1,13,77,62),
(17,2,13,98,59),
(17,2,13,96,61),
(17,2,13,101,62),
(17,2,13,97,64),
(19,0,20,9,34),
(19,0,20,8,35),
(19,0,20,7,37),
(19,0,20,9,39),
(20,0,52,59,31),
(20,0,52,57,33),
(20,0,52,55,36),
(20,0,52,58,39),
(21,0,13,36,34),
(21,0,13,34,36),
(21,0,13,39,37),
(21,0,13,35,39),
(21,1,101,39,41),
(21,1,101,36,44),
(21,1,101,47,48),
(21,1,101,46,52),
(22,0,6,42,45),
(22,0,6,41,46),
(22,0,6,41,47),
(22,0,6,44,48),
(23,0,53,19,4),
(23,0,53,23,6),
(23,0,53,24,9),
(23,0,53,23,12),
(28,0,135,14,33),
(28,0,135,20,36),
(28,0,135,22,41),
(28,0,135,20,46),
(70,0,51,48,46),
(70,0,51,52,47),
(70,0,51,55,50),
(70,0,51,48,53),
(80,0,35,39,12),
(80,0,35,41,14),
(80,0,35,42,17),
(80,0,35,37,20),
(106,0,52,18,15),
(106,0,52,14,17),
(106,0,52,23,19),
(106,0,52,21,22),
(114,0,17,68,69),
(114,0,17,67,70),
(114,0,17,66,72),
(114,0,17,71,73),
(201,0,72,61,66),
(201,0,72,66,67),
(201,0,72,57,71),
(201,0,72,59,74),
(45,0,59,42,4),
(45,0,59,50,6),
(45,0,59,41,9),
(45,0,59,47,11),
(46,0,39,11,5),
(46,0,39,9,7),
(46,0,39,18,9),
(46,0,39,13,11),
(49,0,49,10,8),
(49,0,49,7,10),
(49,0,49,6,13),
(49,0,49,8,16),
(50,0,13,50,22),
(50,0,13,48,24),
(50,0,13,53,25),
(50,0,13,49,27),
(51,0,128,25,18),
(51,0,128,33,19),
(51,0,128,21,24),
(51,0,128,34,28),
(53,0,39,51,45),
(53,0,39,55,46),
(53,0,39,56,49),
(53,0,39,50,52),
(54,0,58,5,20),
(54,0,58,9,21),
(54,0,58,12,25),
(54,0,58,4,29),
(55,0,158,24,4),
(55,0,158,33,7),
(55,0,158,36,12),
(55,0,158,23,17),
(55,1,93,71,7),
(55,1,93,70,8),
(55,1,93,68,12),
(55,1,93,71,16),
(56,0,48,5,39),
(56,0,48,4,40),
(56,0,48,11,43),
(56,0,48,4,47),
(58,0,13,79,11),
(58,0,13,77,13),
(58,0,13,82,14),
(58,0,13,78,16),
(59,0,64,50,15),
(59,0,64,48,17),
(59,0,64,47,21),
(59,0,64,49,25),
(60,0,52,51,7),
(60,0,52,54,9),
(60,0,52,56,12),
(60,0,52,54,15),
(60,1,184,30,19),
(60,1,184,27,23),
(60,1,184,41,28),
(60,1,184,38,33),
(61,0,39,7,5),
(61,0,39,5,7),
(61,0,39,14,9),
(61,0,39,9,11),
(62,0,54,48,9),
(62,0,54,45,12),
(62,0,54,44,15),
(62,0,54,46,18),
(161,0,20,18,4),
(161,0,20,20,5),
(161,0,20,17,8),
(161,0,20,20,10),
(161,1,18,7,6),
(161,1,18,9,7),
(161,1,18,9,10),
(161,1,18,6,12),
(161,2,18,50,5),
(161,2,18,52,6),
(161,2,18,47,8),
(161,2,18,52,9),
(161,3,58,38,7),
(161,3,58,37,10),
(161,3,58,39,13),
(161,3,58,39,16),
(161,4,8,27,8),
(161,4,8,26,9),
(161,4,8,30,10),
(161,4,8,29,11),
(161,5,6,25,13),
(161,5,6,24,14),
(161,5,6,27,15),
(161,5,6,28,16),
(161,6,176,52,20),
(161,6,176,61,21),
(161,6,176,63,26),
(161,6,176,51,33),
(161,7,120,16,21),
(161,7,120,18,26),
(161,7,120,18,31),
(161,7,120,7,36),
(161,8,30,44,30),
(161,8,30,48,31),
(161,8,30,46,34),
(161,8,30,46,37),
(161,9,86,17,40),
(161,9,86,28,42),
(161,9,86,31,45),
(161,9,86,22,48),
(161,10,51,47,40),
(161,10,51,46,43),
(161,10,51,46,46),
(161,10,51,46,49),
(161,11,8,60,47),
(161,11,8,61,48),
(161,11,8,58,50),
(161,11,8,59,51),
(162,0,9,43,10),
(162,0,9,41,11),
(162,0,9,44,12),
(162,0,9,43,13),
(162,1,12,15,15),
(162,1,12,14,17),
(162,1,12,18,18),
(162,1,12,15,20),
(162,2,35,44,20),
(162,2,35,42,23),
(162,2,35,46,26),
(162,2,35,44,28),
(162,3,15,29,26),
(162,3,15,31,27),
(162,3,15,28,30),
(162,3,15,33,31),
(162,4,28,20,31),
(162,4,28,22,32),
(162,4,28,19,37),
(162,4,28,20,38),
(162,5,23,37,32),
(162,5,23,35,34),
(162,5,23,35,37),
(162,5,23,38,39),
(162,6,75,50,32),
(162,6,75,53,36),
(162,6,75,46,40),
(162,6,75,46,43),
(163,0,34,11,4),
(163,0,34,8,6),
(163,0,34,11,8),
(163,0,34,16,8),
(163,1,38,21,7),
(163,1,38,19,9),
(163,1,38,21,15),
(163,1,38,21,18),
(163,2,85,43,11),
(163,2,85,38,13),
(163,2,85,51,16),
(163,2,85,41,20),
(163,3,7,12,18),
(163,3,7,13,19),
(163,3,7,11,21),
(163,3,7,12,22),
(163,4,12,33,22),
(163,4,12,32,23),
(163,4,12,36,24),
(163,4,12,36,25),
(163,5,30,9,25),
(163,5,30,12,27),
(163,5,30,8,30),
(163,5,30,13,32),
(163,6,12,17,29),
(163,6,12,19,30),
(163,6,12,19,32),
(163,6,12,16,34),
(163,7,26,29,39),
(163,7,26,32,40),
(163,7,26,27,43),
(163,7,26,27,45),
(163,8,32,18,44),
(163,8,32,21,46),
(163,8,32,15,49),
(163,8,32,20,51),
(163,9,28,41,46),
(163,9,28,39,48),
(163,9,28,45,50),
(163,9,28,42,53),
(164,0,11,33,19),
(164,0,11,31,21),
(164,0,11,35,22),
(164,0,11,32,24);

-- data_noi_dung_trang_bi (tung la pokemon_noi_dung.sql, bo sinh tools/luu_tru_sinh_du_lieu/pokemon/sinh_noi_dung.py - da luu tru)
DROP TABLE IF EXISTS `data_noi_dung_trang_bi`;
CREATE TABLE `data_noi_dung_trang_bi` (
  `ma` varchar(255) NOT NULL,
  `bac` bigint NOT NULL,
  `ten_bac` varchar(255) NOT NULL,
  `o` varchar(255) NOT NULL,
  `type_item` bigint NOT NULL,
  `dong` varchar(255) NOT NULL,
  `ten` varchar(255) NOT NULL,
  `id_item` bigint NOT NULL,
  `trang_thai` varchar(255) NOT NULL,
  `cap_yeu_cau` bigint NOT NULL,
  `cap_item` bigint NOT NULL,
  `ngay_mo` bigint NOT NULL,
  `tran_cap_khi_mo` bigint NOT NULL,
  `so_o` bigint NOT NULL,
  `tan_cong` bigint NOT NULL,
  `phong_thu` bigint NOT NULL,
  `hp` bigint NOT NULL,
  `chi_so_phu` varchar(255) NOT NULL,
  `he` varchar(255) NOT NULL,
  `gia_xu` bigint NOT NULL,
  `ban_npc_xu` bigint NOT NULL,
  `phan_ra` varchar(255) NOT NULL,
  `nguon` varchar(255) NOT NULL,
  `ghi_chu` varchar(255) NOT NULL,
  PRIMARY KEY (`ma`)
) DEFAULT CHARSET=utf8;
INSERT INTO `data_noi_dung_trang_bi` (`ma`,`bac`,`ten_bac`,`o`,`type_item`,`dong`,`ten`,`id_item`,`trang_thai`,`cap_yeu_cau`,`cap_item`,`ngay_mo`,`tran_cap_khi_mo`,`so_o`,`tan_cong`,`phong_thu`,`hp`,`chi_so_phu`,`he`,`gia_xu`,`ban_npc_xu`,`phan_ra`,`nguon`,`ghi_chu`) VALUES
('khan_quang_0',0,'sơ cấp','khan_quang',0,'','Khăn quàng sơ cấp',1,'co_san',1,1,0,60,1,0,2,273,'','',5385,269,'2 Đá nâng cấp + 538 xu','cua_hang,roi','data_item có sẵn'),
('dai_0',0,'sơ cấp','dai',1,'','Đai sơ cấp',27,'co_san',1,1,0,60,1,0,1,218,'','',5385,269,'2 Đá nâng cấp + 538 xu','cua_hang,roi','data_item có sẵn'),
('bang_do_0',0,'sơ cấp','bang_do',2,'','Băng đô sơ cấp',53,'co_san',1,1,0,60,1,0,1,163,'','',5385,269,'2 Đá nâng cấp + 538 xu','cua_hang,roi','data_item có sẵn'),
('vu_khi_0_c0',0,'sơ cấp','vu_khi',3,'Charmander','Than lửa sơ cấp',79,'co_san',1,1,0,60,1,10,0,0,'Chiêu cùng hệ +10%','Lửa',5385,269,'2 Đá nâng cấp + 538 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_0_c1',0,'sơ cấp','vu_khi',4,'Bulbasaur','Hạt giống thần sơ cấp',86,'co_san',1,1,0,60,1,10,0,0,'Chiêu cùng hệ +10%','Cỏ',5385,269,'2 Đá nâng cấp + 538 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_0_c2',0,'sơ cấp','vu_khi',5,'Eevee','Khăn lụa sơ cấp',93,'co_san',1,1,0,60,1,10,0,0,'Chiêu cùng hệ +10%','Thường',5385,269,'2 Đá nâng cấp + 538 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_0_c3',0,'sơ cấp','vu_khi',6,'Squirtle','Nước thần sơ cấp',100,'co_san',1,1,0,60,1,10,0,0,'Chiêu cùng hệ +10%','Nước',5385,269,'2 Đá nâng cấp + 538 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_0_c4',0,'sơ cấp','vu_khi',7,'Pichu','Nam châm sơ cấp',107,'co_san',1,1,0,60,1,10,0,0,'Chiêu cùng hệ +10%','Điện',5385,269,'2 Đá nâng cấp + 538 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('huy_hieu_0',0,'sơ cấp','huy_hieu',8,'','Huy hiệu sơ cấp',114,'co_san',1,4,0,60,2,2,0,0,'','',5385,269,'2 Đá nâng cấp + 538 xu','gym,nhiem_vu','data_item có sẵn'),
('vong_co_0',0,'sơ cấp','vong_co',9,'','Vòng cổ sơ cấp',126,'co_san',1,4,0,60,1,3,0,109,'','',5385,269,'2 Đá nâng cấp + 538 xu','che_tao,roi','data_item có sẵn'),
('long_vu_0',0,'sơ cấp','long_vu',10,'','Lông vũ tốc độ sơ cấp',138,'co_san',1,4,0,60,1,0,0,109,'Tốc chạy +1','',5385,269,'2 Đá nâng cấp + 538 xu','cua_hang,roi','data_item có sẵn'),
('gang_dam_0',0,'sơ cấp','gang_dam',11,'','Găng đấm sơ cấp',150,'co_san',1,4,0,60,1,3,0,0,'Chí mạng +0,5%','',5385,269,'2 Đá nâng cấp + 538 xu','cua_hang,roi','data_item có sẵn'),
('phien_da_0',0,'sơ cấp','phien_da',12,'','Phiến đá hệ sơ cấp',163,'co_san',1,9,0,60,1,2,0,0,'Chọn 1 trong 18 hệ: +5% sát thương gây ra hoặc -5% sát thương nhận vào','',5385,269,'2 Đá nâng cấp + 538 xu','che_tao,su_kien','data_item có sẵn'),
('ruy_bang_0',0,'sơ cấp','ruy_bang',19,'','Ruy băng sơ cấp',1001,'moi',1,1,0,60,1,0,0,218,'','',5385,269,'2 Đá nâng cấp + 538 xu','nhiem_vu,boss_the_gioi','sinh mới (id 1001)'),
('khan_quang_1',1,'đồng','khan_quang',0,'','Khăn quàng đồng',7,'co_san',10,14,0,60,1,0,3,367,'','',8328,416,'2 Đá nâng cấp + 832 xu','cua_hang,roi','data_item có sẵn'),
('dai_1',1,'đồng','dai',1,'','Đai đồng',33,'co_san',10,14,0,60,1,0,2,294,'','',8328,416,'2 Đá nâng cấp + 832 xu','cua_hang,roi','data_item có sẵn'),
('bang_do_1',1,'đồng','bang_do',2,'','Băng đô đồng',59,'co_san',10,14,0,60,1,0,1,220,'','',8328,416,'2 Đá nâng cấp + 832 xu','cua_hang,roi','data_item có sẵn'),
('vu_khi_1_c0',1,'đồng','vu_khi',3,'Charmander','Than lửa đồng',81,'co_san',10,11,0,60,1,20,0,0,'Chiêu cùng hệ +11%','Lửa',8328,416,'2 Đá nâng cấp + 832 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_1_c1',1,'đồng','vu_khi',4,'Bulbasaur','Hạt giống thần đồng',88,'co_san',10,11,0,60,1,20,0,0,'Chiêu cùng hệ +11%','Cỏ',8328,416,'2 Đá nâng cấp + 832 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_1_c2',1,'đồng','vu_khi',5,'Eevee','Khăn lụa đồng',95,'co_san',10,11,0,60,1,20,0,0,'Chiêu cùng hệ +11%','Thường',8328,416,'2 Đá nâng cấp + 832 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_1_c3',1,'đồng','vu_khi',6,'Squirtle','Nước thần đồng',102,'co_san',10,11,0,60,1,20,0,0,'Chiêu cùng hệ +11%','Nước',8328,416,'2 Đá nâng cấp + 832 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_1_c4',1,'đồng','vu_khi',7,'Pichu','Nam châm đồng',109,'co_san',10,11,0,60,1,20,0,0,'Chiêu cùng hệ +11%','Điện',8328,416,'2 Đá nâng cấp + 832 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('huy_hieu_1',1,'đồng','huy_hieu',8,'','Huy hiệu đồng',116,'co_san',10,14,0,60,2,5,0,0,'','',8328,416,'2 Đá nâng cấp + 832 xu','gym,nhiem_vu','data_item có sẵn'),
('vong_co_1',1,'đồng','vong_co',9,'','Vòng cổ đồng',128,'co_san',10,14,0,60,1,7,0,147,'','',8328,416,'2 Đá nâng cấp + 832 xu','che_tao,roi','data_item có sẵn'),
('long_vu_1',1,'đồng','long_vu',10,'','Lông vũ tốc độ đồng',140,'co_san',10,14,0,60,1,0,0,147,'Tốc chạy +1','',8328,416,'2 Đá nâng cấp + 832 xu','cua_hang,roi','data_item có sẵn'),
('gang_dam_1',1,'đồng','gang_dam',11,'','Găng đấm đồng',152,'co_san',10,14,0,60,1,7,0,0,'Chí mạng +0,6%','',8328,416,'2 Đá nâng cấp + 832 xu','cua_hang,roi','data_item có sẵn'),
('phien_da_1',1,'đồng','phien_da',12,'','Phiến đá hệ đồng',164,'co_san',10,14,0,60,1,5,0,0,'Chọn 1 trong 18 hệ: +5% sát thương gây ra hoặc -5% sát thương nhận vào','',8328,416,'2 Đá nâng cấp + 832 xu','che_tao,su_kien','data_item có sẵn'),
('ruy_bang_1',1,'đồng','ruy_bang',19,'','Ruy băng đồng',1002,'moi',10,10,0,60,1,0,0,294,'','',8328,416,'2 Đá nâng cấp + 832 xu','nhiem_vu,boss_the_gioi','sinh mới (id 1002)'),
('khan_quang_2',2,'bạc','khan_quang',0,'','Khăn quàng bạc',268,'co_san',20,20,0,60,1,0,5,472,'','',13527,676,'2 Đá nâng cấp + 1352 xu','cua_hang,roi','data_item có sẵn'),
('dai_2',2,'bạc','dai',1,'','Đai bạc',37,'co_san',20,24,0,60,1,0,3,378,'','',13527,676,'2 Đá nâng cấp + 1352 xu','cua_hang,roi','data_item có sẵn'),
('bang_do_2',2,'bạc','bang_do',2,'','Băng đô bạc',63,'co_san',20,24,0,60,1,0,3,283,'','',13527,676,'2 Đá nâng cấp + 1352 xu','cua_hang,roi','data_item có sẵn'),
('vu_khi_2_c0',2,'bạc','vu_khi',3,'Charmander','Than lửa bạc',83,'co_san',20,21,0,60,1,43,0,0,'Chiêu cùng hệ +12%','Lửa',13527,676,'2 Đá nâng cấp + 1352 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_2_c1',2,'bạc','vu_khi',4,'Bulbasaur','Hạt giống thần bạc',90,'co_san',20,21,0,60,1,43,0,0,'Chiêu cùng hệ +12%','Cỏ',13527,676,'2 Đá nâng cấp + 1352 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_2_c2',2,'bạc','vu_khi',5,'Eevee','Khăn lụa bạc',97,'co_san',20,21,0,60,1,43,0,0,'Chiêu cùng hệ +12%','Thường',13527,676,'2 Đá nâng cấp + 1352 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_2_c3',2,'bạc','vu_khi',6,'Squirtle','Nước thần bạc',104,'co_san',20,21,0,60,1,43,0,0,'Chiêu cùng hệ +12%','Nước',13527,676,'2 Đá nâng cấp + 1352 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_2_c4',2,'bạc','vu_khi',7,'Pichu','Nam châm bạc',111,'co_san',20,21,0,60,1,43,0,0,'Chiêu cùng hệ +12%','Điện',13527,676,'2 Đá nâng cấp + 1352 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('huy_hieu_2',2,'bạc','huy_hieu',8,'','Huy hiệu bạc',118,'co_san',20,24,0,60,2,10,0,0,'','',13527,676,'2 Đá nâng cấp + 1352 xu','gym,nhiem_vu','data_item có sẵn'),
('vong_co_2',2,'bạc','vong_co',9,'','Vòng cổ bạc',130,'co_san',20,24,0,60,1,16,0,189,'','',13527,676,'2 Đá nâng cấp + 1352 xu','che_tao,roi','data_item có sẵn'),
('long_vu_2',2,'bạc','long_vu',10,'','Lông vũ tốc độ bạc',142,'co_san',20,24,0,60,1,0,1,189,'Tốc chạy +2','',13527,676,'2 Đá nâng cấp + 1352 xu','cua_hang,roi','data_item có sẵn'),
('gang_dam_2',2,'bạc','gang_dam',11,'','Găng đấm bạc',154,'co_san',20,24,0,60,1,16,0,0,'Chí mạng +0,7%','',13527,676,'2 Đá nâng cấp + 1352 xu','cua_hang,roi','data_item có sẵn'),
('phien_da_2',2,'bạc','phien_da',12,'','Phiến đá hệ bạc',166,'co_san',20,24,0,60,1,10,0,0,'Chọn 1 trong 18 hệ: +5% sát thương gây ra hoặc -5% sát thương nhận vào','',13527,676,'2 Đá nâng cấp + 1352 xu','che_tao,su_kien','data_item có sẵn'),
('ruy_bang_2',2,'bạc','ruy_bang',19,'','Ruy băng bạc',1003,'moi',20,20,0,60,1,0,1,378,'','',13527,676,'2 Đá nâng cấp + 1352 xu','nhiem_vu,boss_the_gioi','sinh mới (id 1003)'),
('khan_quang_3',3,'vàng','khan_quang',0,'','Khăn quàng vàng',272,'co_san',30,30,0,60,1,0,10,577,'','',20704,1035,'2 Đá nâng cấp + 2070 xu','cua_hang,roi','data_item có sẵn'),
('dai_3',3,'vàng','dai',1,'','Đai vàng',41,'co_san',30,34,0,60,1,0,7,462,'','',20704,1035,'2 Đá nâng cấp + 2070 xu','cua_hang,roi','data_item có sẵn'),
('bang_do_3',3,'vàng','bang_do',2,'','Băng đô vàng',67,'co_san',30,34,0,60,1,0,6,346,'','',20704,1035,'2 Đá nâng cấp + 2070 xu','cua_hang,roi','data_item có sẵn'),
('vu_khi_3_c0',3,'vàng','vu_khi',3,'Charmander','Than lửa vàng',85,'co_san',30,31,0,60,1,86,0,0,'Chiêu cùng hệ +13%','Lửa',20704,1035,'2 Đá nâng cấp + 2070 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_3_c1',3,'vàng','vu_khi',4,'Bulbasaur','Hạt giống thần vàng',92,'co_san',30,31,0,60,1,86,0,0,'Chiêu cùng hệ +13%','Cỏ',20704,1035,'2 Đá nâng cấp + 2070 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_3_c2',3,'vàng','vu_khi',5,'Eevee','Khăn lụa vàng',99,'co_san',30,31,0,60,1,86,0,0,'Chiêu cùng hệ +13%','Thường',20704,1035,'2 Đá nâng cấp + 2070 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_3_c3',3,'vàng','vu_khi',6,'Squirtle','Nước thần vàng',106,'co_san',30,31,0,60,1,86,0,0,'Chiêu cùng hệ +13%','Nước',20704,1035,'2 Đá nâng cấp + 2070 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_3_c4',3,'vàng','vu_khi',7,'Pichu','Nam châm vàng',113,'co_san',30,31,0,60,1,86,0,0,'Chiêu cùng hệ +13%','Điện',20704,1035,'2 Đá nâng cấp + 2070 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('huy_hieu_3',3,'vàng','huy_hieu',8,'','Huy hiệu vàng',120,'co_san',30,34,0,60,2,21,0,0,'','',20704,1035,'2 Đá nâng cấp + 2070 xu','gym,nhiem_vu','data_item có sẵn'),
('vong_co_3',3,'vàng','vong_co',9,'','Vòng cổ vàng',132,'co_san',30,34,0,60,1,32,0,231,'','',20704,1035,'2 Đá nâng cấp + 2070 xu','che_tao,roi','data_item có sẵn'),
('long_vu_3',3,'vàng','long_vu',10,'','Lông vũ tốc độ vàng',144,'co_san',30,34,0,60,1,0,3,231,'Tốc chạy +2','',20704,1035,'2 Đá nâng cấp + 2070 xu','cua_hang,roi','data_item có sẵn'),
('gang_dam_3',3,'vàng','gang_dam',11,'','Găng đấm vàng',156,'co_san',30,34,0,60,1,32,0,0,'Chí mạng +0,8%','',20704,1035,'2 Đá nâng cấp + 2070 xu','cua_hang,roi','data_item có sẵn'),
('phien_da_3',3,'vàng','phien_da',12,'','Phiến đá hệ vàng',168,'co_san',30,34,0,60,1,21,0,0,'Chọn 1 trong 18 hệ: +5% sát thương gây ra hoặc -5% sát thương nhận vào','',20704,1035,'2 Đá nâng cấp + 2070 xu','che_tao,su_kien','data_item có sẵn'),
('ruy_bang_3',3,'vàng','ruy_bang',19,'','Ruy băng vàng',584,'co_san',30,35,0,60,1,0,3,462,'','',20704,1035,'2 Đá nâng cấp + 2070 xu','nhiem_vu,boss_the_gioi','data_item có sẵn'),
('khan_quang_4',4,'bạch kim','khan_quang',0,'','Khăn quàng bạch kim',19,'co_san',40,44,0,60,1,0,21,682,'','',32340,1617,'2 Đá nâng cấp + 3234 xu','cua_hang,roi','data_item có sẵn'),
('dai_4',4,'bạch kim','dai',1,'','Đai bạch kim',45,'co_san',40,44,0,60,1,0,15,546,'','',32340,1617,'2 Đá nâng cấp + 3234 xu','cua_hang,roi','data_item có sẵn'),
('bang_do_4',4,'bạch kim','bang_do',2,'','Băng đô bạch kim',71,'co_san',40,44,0,60,1,0,12,409,'','',32340,1617,'2 Đá nâng cấp + 3234 xu','cua_hang,roi','data_item có sẵn'),
('vu_khi_4_c0',4,'bạch kim','vu_khi',3,'Charmander','Than lửa bạch kim',175,'co_san',40,41,0,60,1,122,0,0,'Chiêu cùng hệ +14%','Lửa',32340,1617,'2 Đá nâng cấp + 3234 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_4_c1',4,'bạch kim','vu_khi',4,'Bulbasaur','Hạt giống thần bạch kim',183,'co_san',40,41,0,60,1,122,0,0,'Chiêu cùng hệ +14%','Cỏ',32340,1617,'2 Đá nâng cấp + 3234 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_4_c2',4,'bạch kim','vu_khi',5,'Eevee','Khăn lụa bạch kim',191,'co_san',40,41,0,60,1,122,0,0,'Chiêu cùng hệ +14%','Thường',32340,1617,'2 Đá nâng cấp + 3234 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_4_c3',4,'bạch kim','vu_khi',6,'Squirtle','Nước thần bạch kim',199,'co_san',40,41,0,60,1,122,0,0,'Chiêu cùng hệ +14%','Nước',32340,1617,'2 Đá nâng cấp + 3234 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_4_c4',4,'bạch kim','vu_khi',7,'Pichu','Nam châm bạch kim',207,'co_san',40,41,0,60,1,122,0,0,'Chiêu cùng hệ +14%','Điện',32340,1617,'2 Đá nâng cấp + 3234 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('huy_hieu_4',4,'bạch kim','huy_hieu',8,'','Huy hiệu bạch kim',122,'co_san',40,44,0,60,2,30,0,0,'','',32340,1617,'2 Đá nâng cấp + 3234 xu','gym,nhiem_vu','data_item có sẵn'),
('vong_co_4',4,'bạch kim','vong_co',9,'','Vòng cổ bạch kim',134,'co_san',40,44,0,60,1,45,0,273,'','',32340,1617,'2 Đá nâng cấp + 3234 xu','che_tao,roi','data_item có sẵn'),
('long_vu_4',4,'bạch kim','long_vu',10,'','Lông vũ tốc độ bạch kim',146,'co_san',40,44,0,60,1,0,6,273,'Tốc chạy +3','',32340,1617,'2 Đá nâng cấp + 3234 xu','cua_hang,roi','data_item có sẵn'),
('gang_dam_4',4,'bạch kim','gang_dam',11,'','Găng đấm bạch kim',158,'co_san',40,44,0,60,1,45,0,0,'Chí mạng +0,9%','',32340,1617,'2 Đá nâng cấp + 3234 xu','cua_hang,roi','data_item có sẵn'),
('phien_da_4',4,'bạch kim','phien_da',12,'','Phiến đá hệ bạch kim',170,'co_san',40,44,0,60,1,30,0,0,'Chọn 1 trong 18 hệ: +5% sát thương gây ra hoặc -5% sát thương nhận vào','',32340,1617,'2 Đá nâng cấp + 3234 xu','che_tao,su_kien','data_item có sẵn'),
('ruy_bang_4',4,'bạch kim','ruy_bang',19,'','Ruy băng bạch kim',585,'co_san',40,40,0,60,1,0,6,546,'','',32340,1617,'2 Đá nâng cấp + 3234 xu','nhiem_vu,boss_the_gioi','data_item có sẵn'),
('khan_quang_5',5,'kim cương','khan_quang',0,'','Khăn quàng kim cương',23,'co_san',50,54,0,60,1,0,33,787,'','',50888,2544,'2 Đá nâng cấp + 5088 xu','cua_hang,roi','data_item có sẵn'),
('dai_5',5,'kim cương','dai',1,'','Đai kim cương',49,'co_san',50,54,0,60,1,0,23,630,'','',50888,2544,'2 Đá nâng cấp + 5088 xu','cua_hang,roi','data_item có sẵn'),
('bang_do_5',5,'kim cương','bang_do',2,'','Băng đô kim cương',75,'co_san',50,54,0,60,1,0,18,472,'','',50888,2544,'2 Đá nâng cấp + 5088 xu','cua_hang,roi','data_item có sẵn'),
('vu_khi_5_c0',5,'kim cương','vu_khi',3,'Charmander','Than lửa kim cương',177,'co_san',50,51,0,60,1,151,0,0,'Chiêu cùng hệ +15%','Lửa',50888,2544,'2 Đá nâng cấp + 5088 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_5_c1',5,'kim cương','vu_khi',4,'Bulbasaur','Hạt giống thần kim cương',185,'co_san',50,51,0,60,1,151,0,0,'Chiêu cùng hệ +15%','Cỏ',50888,2544,'2 Đá nâng cấp + 5088 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_5_c2',5,'kim cương','vu_khi',5,'Eevee','Khăn lụa kim cương',193,'co_san',50,51,0,60,1,151,0,0,'Chiêu cùng hệ +15%','Thường',50888,2544,'2 Đá nâng cấp + 5088 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_5_c3',5,'kim cương','vu_khi',6,'Squirtle','Nước thần kim cương',201,'co_san',50,51,0,60,1,151,0,0,'Chiêu cùng hệ +15%','Nước',50888,2544,'2 Đá nâng cấp + 5088 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_5_c4',5,'kim cương','vu_khi',7,'Pichu','Nam châm kim cương',209,'co_san',50,51,0,60,1,151,0,0,'Chiêu cùng hệ +15%','Điện',50888,2544,'2 Đá nâng cấp + 5088 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('huy_hieu_5',5,'kim cương','huy_hieu',8,'','Huy hiệu kim cương',124,'co_san',50,54,0,60,2,37,0,0,'','',50888,2544,'2 Đá nâng cấp + 5088 xu','gym,nhiem_vu','data_item có sẵn'),
('vong_co_5',5,'kim cương','vong_co',9,'','Vòng cổ kim cương',136,'co_san',50,54,0,60,1,56,0,315,'','',50888,2544,'2 Đá nâng cấp + 5088 xu','che_tao,roi','data_item có sẵn'),
('long_vu_5',5,'kim cương','long_vu',10,'','Lông vũ tốc độ kim cương',148,'co_san',50,54,0,60,1,0,9,315,'Tốc chạy +3','',50888,2544,'2 Đá nâng cấp + 5088 xu','cua_hang,roi','data_item có sẵn'),
('gang_dam_5',5,'kim cương','gang_dam',11,'','Găng đấm kim cương',160,'co_san',50,54,0,60,1,56,0,0,'Chí mạng +1%','',50888,2544,'2 Đá nâng cấp + 5088 xu','cua_hang,roi','data_item có sẵn'),
('phien_da_5',5,'kim cương','phien_da',12,'','Phiến đá hệ kim cương',172,'co_san',50,54,0,60,1,37,0,0,'Chọn 1 trong 18 hệ: +5% sát thương gây ra hoặc -5% sát thương nhận vào','',50888,2544,'2 Đá nâng cấp + 5088 xu','che_tao,su_kien','data_item có sẵn'),
('ruy_bang_5',5,'kim cương','ruy_bang',19,'','Ruy băng kim cương',587,'co_san',50,50,0,60,1,0,9,630,'','',50888,2544,'2 Đá nâng cấp + 5088 xu','nhiem_vu,boss_the_gioi','data_item có sẵn'),
('khan_quang_6',6,'chuyên gia','khan_quang',0,'','Khăn quàng chuyên gia',467,'co_san',60,64,0,60,1,0,39,892,'','',79101,3955,'2 Đá nâng cấp + 7910 xu','cua_hang,roi','data_item có sẵn'),
('dai_6',6,'chuyên gia','dai',1,'','Đai chuyên gia',475,'co_san',60,64,0,60,1,0,28,714,'','',79101,3955,'2 Đá nâng cấp + 7910 xu','cua_hang,roi','data_item có sẵn'),
('bang_do_6',6,'chuyên gia','bang_do',2,'','Băng đô chuyên gia',483,'co_san',60,64,0,60,1,0,22,535,'','',79101,3955,'2 Đá nâng cấp + 7910 xu','cua_hang,roi','data_item có sẵn'),
('vu_khi_6_c0',6,'chuyên gia','vu_khi',3,'Charmander','Than lửa chuyên gia',179,'co_san',60,61,0,60,1,180,0,0,'Chiêu cùng hệ +16%','Lửa',79101,3955,'2 Đá nâng cấp + 7910 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_6_c1',6,'chuyên gia','vu_khi',4,'Bulbasaur','Hạt giống thần chuyên gia',187,'co_san',60,61,0,60,1,180,0,0,'Chiêu cùng hệ +16%','Cỏ',79101,3955,'2 Đá nâng cấp + 7910 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_6_c2',6,'chuyên gia','vu_khi',5,'Eevee','Khăn lụa chuyên gia',195,'co_san',60,61,0,60,1,180,0,0,'Chiêu cùng hệ +16%','Thường',79101,3955,'2 Đá nâng cấp + 7910 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_6_c3',6,'chuyên gia','vu_khi',6,'Squirtle','Nước thần chuyên gia',203,'co_san',60,61,0,60,1,180,0,0,'Chiêu cùng hệ +16%','Nước',79101,3955,'2 Đá nâng cấp + 7910 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_6_c4',6,'chuyên gia','vu_khi',7,'Pichu','Nam châm chuyên gia',211,'co_san',60,61,0,60,1,180,0,0,'Chiêu cùng hệ +16%','Điện',79101,3955,'2 Đá nâng cấp + 7910 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('huy_hieu_6',6,'chuyên gia','huy_hieu',8,'','Huy hiệu chuyên gia',499,'co_san',60,64,0,60,2,45,0,0,'','',79101,3955,'2 Đá nâng cấp + 7910 xu','gym,nhiem_vu','data_item có sẵn'),
('vong_co_6',6,'chuyên gia','vong_co',9,'','Vòng cổ chuyên gia',503,'co_san',60,64,0,60,1,67,0,357,'','',79101,3955,'2 Đá nâng cấp + 7910 xu','che_tao,roi','data_item có sẵn'),
('long_vu_6',6,'chuyên gia','long_vu',10,'','Lông vũ tốc độ chuyên gia',491,'co_san',60,64,0,60,1,0,11,357,'Tốc chạy +4','',79101,3955,'2 Đá nâng cấp + 7910 xu','cua_hang,roi','data_item có sẵn'),
('gang_dam_6',6,'chuyên gia','gang_dam',11,'','Găng đấm chuyên gia',495,'co_san',60,64,0,60,1,67,0,0,'Chí mạng +1,1%','',79101,3955,'2 Đá nâng cấp + 7910 xu','cua_hang,roi','data_item có sẵn'),
('phien_da_6',6,'chuyên gia','phien_da',12,'','Phiến đá hệ chuyên gia',569,'co_san',60,64,0,60,1,45,0,0,'Chọn 1 trong 18 hệ: +5% sát thương gây ra hoặc -5% sát thương nhận vào','',79101,3955,'2 Đá nâng cấp + 7910 xu','che_tao,su_kien','data_item có sẵn'),
('ruy_bang_6',6,'chuyên gia','ruy_bang',19,'','Ruy băng chuyên gia',589,'co_san',60,60,0,60,1,0,11,714,'','',79101,3955,'2 Đá nâng cấp + 7910 xu','nhiem_vu,boss_the_gioi','data_item có sẵn'),
('khan_quang_7',7,'tinh anh','khan_quang',0,'','Khăn quàng tinh anh',471,'co_san',70,74,60,70,1,0,45,997,'','',122664,6133,'2 Đá nâng cấp + 12266 xu','cua_hang,roi','data_item có sẵn'),
('dai_7',7,'tinh anh','dai',1,'','Đai tinh anh',479,'co_san',70,74,60,70,1,0,32,798,'','',122664,6133,'2 Đá nâng cấp + 12266 xu','cua_hang,roi','data_item có sẵn'),
('bang_do_7',7,'tinh anh','bang_do',2,'','Băng đô tinh anh',487,'co_san',70,74,60,70,1,0,26,598,'','',122664,6133,'2 Đá nâng cấp + 12266 xu','cua_hang,roi','data_item có sẵn'),
('vu_khi_7_c0',7,'tinh anh','vu_khi',3,'Charmander','Than lửa tinh anh',181,'co_san',70,71,60,70,1,208,0,0,'Chiêu cùng hệ +17%','Lửa',122664,6133,'2 Đá nâng cấp + 12266 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_7_c1',7,'tinh anh','vu_khi',4,'Bulbasaur','Hạt giống thần tinh anh',189,'co_san',70,71,60,70,1,208,0,0,'Chiêu cùng hệ +17%','Cỏ',122664,6133,'2 Đá nâng cấp + 12266 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_7_c2',7,'tinh anh','vu_khi',5,'Eevee','Khăn lụa tinh anh',197,'co_san',70,71,60,70,1,208,0,0,'Chiêu cùng hệ +17%','Thường',122664,6133,'2 Đá nâng cấp + 12266 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_7_c3',7,'tinh anh','vu_khi',6,'Squirtle','Nước thần tinh anh',205,'co_san',70,71,60,70,1,208,0,0,'Chiêu cùng hệ +17%','Nước',122664,6133,'2 Đá nâng cấp + 12266 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_7_c4',7,'tinh anh','vu_khi',7,'Pichu','Nam châm tinh anh',213,'co_san',70,71,60,70,1,208,0,0,'Chiêu cùng hệ +17%','Điện',122664,6133,'2 Đá nâng cấp + 12266 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('huy_hieu_7',7,'tinh anh','huy_hieu',8,'','Huy hiệu tinh anh',501,'co_san',70,74,60,70,2,52,0,0,'','',122664,6133,'2 Đá nâng cấp + 12266 xu','gym,nhiem_vu','data_item có sẵn'),
('vong_co_7',7,'tinh anh','vong_co',9,'','Vòng cổ tinh anh',505,'co_san',70,74,60,70,1,78,0,399,'','',122664,6133,'2 Đá nâng cấp + 12266 xu','che_tao,roi','data_item có sẵn'),
('long_vu_7',7,'tinh anh','long_vu',10,'','Lông vũ tốc độ tinh anh',493,'co_san',70,74,60,70,1,0,13,399,'Tốc chạy +4','',122664,6133,'2 Đá nâng cấp + 12266 xu','cua_hang,roi','data_item có sẵn'),
('gang_dam_7',7,'tinh anh','gang_dam',11,'','Găng đấm tinh anh',497,'co_san',70,74,60,70,1,78,0,0,'Chí mạng +1,2%','',122664,6133,'2 Đá nâng cấp + 12266 xu','cua_hang,roi','data_item có sẵn'),
('phien_da_7',7,'tinh anh','phien_da',12,'','Phiến đá hệ tinh anh',571,'co_san',70,74,60,70,1,52,0,0,'Chọn 1 trong 18 hệ: +5% sát thương gây ra hoặc -5% sát thương nhận vào','',122664,6133,'2 Đá nâng cấp + 12266 xu','che_tao,su_kien','data_item có sẵn'),
('ruy_bang_7',7,'tinh anh','ruy_bang',19,'','Ruy băng tinh anh',591,'co_san',70,70,60,70,1,0,13,798,'','',122664,6133,'2 Đá nâng cấp + 12266 xu','nhiem_vu,boss_the_gioi','data_item có sẵn'),
('khan_quang_8',8,'huyền thoại','khan_quang',0,'','Khăn quàng huyền thoại',641,'co_san',80,85,150,80,1,0,51,1102,'','',190464,9523,'2 Đá nâng cấp + 19046 xu','cua_hang,roi','data_item có sẵn'),
('dai_8',8,'huyền thoại','dai',1,'','Đai huyền thoại',646,'co_san',80,85,150,80,1,0,37,882,'','',190464,9523,'2 Đá nâng cấp + 19046 xu','cua_hang,roi','data_item có sẵn'),
('bang_do_8',8,'huyền thoại','bang_do',2,'','Băng đô huyền thoại',649,'co_san',80,85,150,80,1,0,29,661,'','',190464,9523,'2 Đá nâng cấp + 19046 xu','cua_hang,roi','data_item có sẵn'),
('vu_khi_8_c0',8,'huyền thoại','vu_khi',3,'Charmander','Than lửa huyền thoại',599,'co_san',80,81,150,80,1,237,0,0,'Chiêu cùng hệ +18%','Lửa',190464,9523,'2 Đá nâng cấp + 19046 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_8_c1',8,'huyền thoại','vu_khi',4,'Bulbasaur','Hạt giống thần huyền thoại',601,'co_san',80,81,150,80,1,237,0,0,'Chiêu cùng hệ +18%','Cỏ',190464,9523,'2 Đá nâng cấp + 19046 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_8_c2',8,'huyền thoại','vu_khi',5,'Eevee','Khăn lụa huyền thoại',603,'co_san',80,81,150,80,1,237,0,0,'Chiêu cùng hệ +18%','Thường',190464,9523,'2 Đá nâng cấp + 19046 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_8_c3',8,'huyền thoại','vu_khi',6,'Squirtle','Nước thần huyền thoại',605,'co_san',80,81,150,80,1,237,0,0,'Chiêu cùng hệ +18%','Nước',190464,9523,'2 Đá nâng cấp + 19046 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('vu_khi_8_c4',8,'huyền thoại','vu_khi',7,'Pichu','Nam châm huyền thoại',607,'co_san',80,81,150,80,1,237,0,0,'Chiêu cùng hệ +18%','Điện',190464,9523,'2 Đá nâng cấp + 19046 xu','nhiem_vu,roi,gym','data_item có sẵn'),
('huy_hieu_8',8,'huyền thoại','huy_hieu',8,'','Huy hiệu huyền thoại',652,'co_san',80,85,150,80,2,59,0,0,'','',190464,9523,'2 Đá nâng cấp + 19046 xu','gym,nhiem_vu','data_item có sẵn'),
('vong_co_8',8,'huyền thoại','vong_co',9,'','Vòng cổ huyền thoại',656,'co_san',80,85,150,80,1,89,0,441,'','',190464,9523,'2 Đá nâng cấp + 19046 xu','che_tao,roi','data_item có sẵn'),
('long_vu_8',8,'huyền thoại','long_vu',10,'','Lông vũ tốc độ huyền thoại',660,'co_san',80,85,150,80,1,0,14,441,'Tốc chạy +5','',190464,9523,'2 Đá nâng cấp + 19046 xu','cua_hang,roi','data_item có sẵn'),
('gang_dam_8',8,'huyền thoại','gang_dam',11,'','Găng đấm huyền thoại',664,'co_san',80,85,150,80,1,89,0,0,'Chí mạng +1,3%','',190464,9523,'2 Đá nâng cấp + 19046 xu','cua_hang,roi','data_item có sẵn'),
('phien_da_8',8,'huyền thoại','phien_da',12,'','Phiến đá hệ huyền thoại',668,'co_san',80,85,150,80,1,59,0,0,'Chọn 1 trong 18 hệ: +5% sát thương gây ra hoặc -5% sát thương nhận vào','',190464,9523,'2 Đá nâng cấp + 19046 xu','che_tao,su_kien','data_item có sẵn'),
('ruy_bang_8',8,'huyền thoại','ruy_bang',19,'','Ruy băng huyền thoại',593,'co_san',80,80,150,80,1,0,14,882,'','',190464,9523,'2 Đá nâng cấp + 19046 xu','nhiem_vu,boss_the_gioi','data_item có sẵn'),
('khan_quang_9',9,'thần thoại','khan_quang',0,'','Khăn quàng thần thoại',1004,'moi',90,90,365,95,1,0,58,1207,'','',294152,14707,'2 Đá nâng cấp + 29415 xu','cua_hang,roi','sinh mới (id 1004/1005)'),
('dai_9',9,'thần thoại','dai',1,'','Đai thần thoại',1006,'moi',90,90,365,95,1,0,41,966,'','',294152,14707,'2 Đá nâng cấp + 29415 xu','cua_hang,roi','sinh mới (id 1006)'),
('bang_do_9',9,'thần thoại','bang_do',2,'','Băng đô thần thoại',1007,'moi',90,90,365,95,1,0,33,724,'','',294152,14707,'2 Đá nâng cấp + 29415 xu','cua_hang,roi','sinh mới (id 1007)'),
('vu_khi_9_c0',9,'thần thoại','vu_khi',3,'Charmander','Than lửa thần thoại',1008,'moi',90,90,365,95,1,266,0,0,'Chiêu cùng hệ +19%','Lửa',294152,14707,'2 Đá nâng cấp + 29415 xu','nhiem_vu,roi,gym','sinh mới (id 1008)'),
('vu_khi_9_c1',9,'thần thoại','vu_khi',4,'Bulbasaur','Hạt giống thần thần thoại',1009,'moi',90,90,365,95,1,266,0,0,'Chiêu cùng hệ +19%','Cỏ',294152,14707,'2 Đá nâng cấp + 29415 xu','nhiem_vu,roi,gym','sinh mới (id 1009)'),
('vu_khi_9_c2',9,'thần thoại','vu_khi',5,'Eevee','Khăn lụa thần thoại',1010,'moi',90,90,365,95,1,266,0,0,'Chiêu cùng hệ +19%','Thường',294152,14707,'2 Đá nâng cấp + 29415 xu','nhiem_vu,roi,gym','sinh mới (id 1010)'),
('vu_khi_9_c3',9,'thần thoại','vu_khi',6,'Squirtle','Nước thần thần thoại',1011,'moi',90,90,365,95,1,266,0,0,'Chiêu cùng hệ +19%','Nước',294152,14707,'2 Đá nâng cấp + 29415 xu','nhiem_vu,roi,gym','sinh mới (id 1011)'),
('vu_khi_9_c4',9,'thần thoại','vu_khi',7,'Pichu','Nam châm thần thoại',1012,'moi',90,90,365,95,1,266,0,0,'Chiêu cùng hệ +19%','Điện',294152,14707,'2 Đá nâng cấp + 29415 xu','nhiem_vu,roi,gym','sinh mới (id 1012)'),
('huy_hieu_9',9,'thần thoại','huy_hieu',8,'','Huy hiệu thần thoại',1013,'moi',90,90,365,95,2,66,0,0,'','',294152,14707,'2 Đá nâng cấp + 29415 xu','gym,nhiem_vu','sinh mới (id 1013)'),
('vong_co_9',9,'thần thoại','vong_co',9,'','Vòng cổ thần thoại',1014,'moi',90,90,365,95,1,99,0,483,'','',294152,14707,'2 Đá nâng cấp + 29415 xu','che_tao,roi','sinh mới (id 1014)'),
('long_vu_9',9,'thần thoại','long_vu',10,'','Lông vũ tốc độ thần thoại',1015,'moi',90,90,365,95,1,0,16,483,'Tốc chạy +5','',294152,14707,'2 Đá nâng cấp + 29415 xu','cua_hang,roi','sinh mới (id 1015)'),
('gang_dam_9',9,'thần thoại','gang_dam',11,'','Găng đấm thần thoại',1016,'moi',90,90,365,95,1,99,0,0,'Chí mạng +1,4%','',294152,14707,'2 Đá nâng cấp + 29415 xu','cua_hang,roi','sinh mới (id 1016)'),
('phien_da_9',9,'thần thoại','phien_da',12,'','Phiến đá hệ thần thoại',1017,'moi',90,90,365,95,1,66,0,0,'Chọn 1 trong 18 hệ: +5% sát thương gây ra hoặc -5% sát thương nhận vào','',294152,14707,'2 Đá nâng cấp + 29415 xu','che_tao,su_kien','sinh mới (id 1017)'),
('ruy_bang_9',9,'thần thoại','ruy_bang',19,'','Ruy băng thần thoại',1018,'moi',90,90,365,95,1,0,16,966,'','',294152,14707,'2 Đá nâng cấp + 29415 xu','nhiem_vu,boss_the_gioi','sinh mới (id 1018)'),
('khan_quang_10',10,'tối thượng','khan_quang',0,'','Khăn quàng tối thượng',1019,'moi',98,98,545,100,1,0,63,1291,'','',416954,20847,'2 Đá nâng cấp + 41695 xu','cua_hang,roi','sinh mới (id 1019/1020)'),
('dai_10',10,'tối thượng','dai',1,'','Đai tối thượng',1021,'moi',98,98,545,100,1,0,45,1033,'','',416954,20847,'2 Đá nâng cấp + 41695 xu','cua_hang,roi','sinh mới (id 1021)'),
('bang_do_10',10,'tối thượng','bang_do',2,'','Băng đô tối thượng',1022,'moi',98,98,545,100,1,0,36,774,'','',416954,20847,'2 Đá nâng cấp + 41695 xu','cua_hang,roi','sinh mới (id 1022)'),
('vu_khi_10_c0',10,'tối thượng','vu_khi',3,'Charmander','Than lửa tối thượng',1023,'moi',98,98,545,100,1,289,0,0,'Chiêu cùng hệ +20%','Lửa',416954,20847,'2 Đá nâng cấp + 41695 xu','nhiem_vu,roi,gym','sinh mới (id 1023)'),
('vu_khi_10_c1',10,'tối thượng','vu_khi',4,'Bulbasaur','Hạt giống thần tối thượng',1024,'moi',98,98,545,100,1,289,0,0,'Chiêu cùng hệ +20%','Cỏ',416954,20847,'2 Đá nâng cấp + 41695 xu','nhiem_vu,roi,gym','sinh mới (id 1024)'),
('vu_khi_10_c2',10,'tối thượng','vu_khi',5,'Eevee','Khăn lụa tối thượng',1025,'moi',98,98,545,100,1,289,0,0,'Chiêu cùng hệ +20%','Thường',416954,20847,'2 Đá nâng cấp + 41695 xu','nhiem_vu,roi,gym','sinh mới (id 1025)'),
('vu_khi_10_c3',10,'tối thượng','vu_khi',6,'Squirtle','Nước thần tối thượng',1026,'moi',98,98,545,100,1,289,0,0,'Chiêu cùng hệ +20%','Nước',416954,20847,'2 Đá nâng cấp + 41695 xu','nhiem_vu,roi,gym','sinh mới (id 1026)'),
('vu_khi_10_c4',10,'tối thượng','vu_khi',7,'Pichu','Nam châm tối thượng',1027,'moi',98,98,545,100,1,289,0,0,'Chiêu cùng hệ +20%','Điện',416954,20847,'2 Đá nâng cấp + 41695 xu','nhiem_vu,roi,gym','sinh mới (id 1027)'),
('huy_hieu_10',10,'tối thượng','huy_hieu',8,'','Huy hiệu tối thượng',1028,'moi',98,98,545,100,2,72,0,0,'','',416954,20847,'2 Đá nâng cấp + 41695 xu','gym,nhiem_vu','sinh mới (id 1028)'),
('vong_co_10',10,'tối thượng','vong_co',9,'','Vòng cổ tối thượng',1029,'moi',98,98,545,100,1,108,0,516,'','',416954,20847,'2 Đá nâng cấp + 41695 xu','che_tao,roi','sinh mới (id 1029)'),
('long_vu_10',10,'tối thượng','long_vu',10,'','Lông vũ tốc độ tối thượng',1030,'moi',98,98,545,100,1,0,18,516,'Tốc chạy +6','',416954,20847,'2 Đá nâng cấp + 41695 xu','cua_hang,roi','sinh mới (id 1030)'),
('gang_dam_10',10,'tối thượng','gang_dam',11,'','Găng đấm tối thượng',1031,'moi',98,98,545,100,1,108,0,0,'Chí mạng +1,5%','',416954,20847,'2 Đá nâng cấp + 41695 xu','cua_hang,roi','sinh mới (id 1031)'),
('phien_da_10',10,'tối thượng','phien_da',12,'','Phiến đá hệ tối thượng',1032,'moi',98,98,545,100,1,72,0,0,'Chọn 1 trong 18 hệ: +5% sát thương gây ra hoặc -5% sát thương nhận vào','',416954,20847,'2 Đá nâng cấp + 41695 xu','che_tao,su_kien','sinh mới (id 1032)'),
('ruy_bang_10',10,'tối thượng','ruy_bang',19,'','Ruy băng tối thượng',1033,'moi',98,98,545,100,1,0,18,1033,'','',416954,20847,'2 Đá nâng cấp + 41695 xu','nhiem_vu,boss_the_gioi','sinh mới (id 1033)');

-- cau_hinh_danh_muc (tung la pokemon_cau_hinh_trung_tam.sql, bo sinh tools/luu_tru_sinh_du_lieu/cauhinh/sinh_cau_hinh.py - da luu tru)
DROP TABLE IF EXISTS `cau_hinh_danh_muc`;
CREATE TABLE IF NOT EXISTS `cau_hinh_danh_muc` (`khoa` VARCHAR(64) NOT NULL, `nhom` VARCHAR(20) NOT NULL, `ten` VARCHAR(160) NOT NULL DEFAULT '', `mo_ta` TEXT NOT NULL, `kieu` VARCHAR(12) NOT NULL, `mac_dinh` VARCHAR(255) NOT NULL DEFAULT '', `gioi_han` TEXT NOT NULL, `ap_dung` VARCHAR(16) NOT NULL, `nhay` TINYINT NOT NULL DEFAULT 0, `khoa_prod` TINYINT NOT NULL DEFAULT 0, `chi_doc` TINYINT NOT NULL DEFAULT 0, `bang` VARCHAR(16) NOT NULL DEFAULT 'cau_hinh', `dung_o` VARCHAR(10) NOT NULL DEFAULT 'server', `nguon` VARCHAR(255) NOT NULL DEFAULT '', `thu_tu` INT NOT NULL DEFAULT 0, `lop` VARCHAR(10) NOT NULL DEFAULT 'chinh', `an` TINYINT NOT NULL DEFAULT 0, PRIMARY KEY (`khoa`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.lau_dai', 'che_do', 'Thiết kế dài hạn', '1 = bật GD2 (bảng EXP mới, chuyển EXP Pokémon một lần có sao lưu). Bật/tắt chạy chuyển đổi EXP lúc khởi động.', 'co', '1', '{}', 'khoi_dong_lai', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.LauDai / real.ChuyenDoiLauDai', 1, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.he_sinh_thai', 'che_do', 'Hệ sinh thái', '1 = bật hệ sinh thái thế giới (data_hst_*, tob_hst_*).', 'co', '1', '{}', 'khoi_dong_lai', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.HeSinhThai', 2, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.tran_cap_theo_mua', 'che_do', 'Trần cấp theo mùa', '1 = bật lại trần cấp 60→100 theo ngày khai trương (thiết kế cũ). 0 = không trần, lên cấp chậm + vật phẩm tăng tốc.', 'co', '0', '{}', 'khoi_dong_lai', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.LauDai#tranTheoMua', 3, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.ngay_khai_truong', 'bao_tri', 'Ngày khai trương', 'yyyy-MM-dd — ngày 0 của lịch mùa. Để trống = theo lau_dai.ngay_khai_truong game tự ghi.', 'ngay', '', '{}', 'khoi_dong_lai', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.LauDai#datNgayKhaiTruong', 4, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.tua_nhanh_ngay', 'che_do', 'Tua nhanh thử nghiệm (ngày)', 'Cộng thêm N ngày vào ngày hiện tại của lịch dài hạn (thử mùa, vé mùa, lịch). chỉ máy thử — máy thật luôn 0.', 'so', '0', '{"min": 0, "max": 3650}', 'khoi_dong_lai', 0, 1, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.LauDai#ngayHienTai', 5, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.tileXu', 'kinh_te', 'Tỉ lệ rơi xu (%)', '% so với tỉ lệ gốc. 100 = như cũ, 0 = tắt.', 'so', '4', '{"min": 0, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.Monster.PC_TILE_XU', 6, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.tileLuong', 'kinh_te', 'Tỉ lệ rơi lượng (%)', '% so với tỉ lệ gốc.', 'so', '10', '{"min": 0, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.Monster.PC_TILE_LUONG', 7, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.tileItemSuKien', 'kinh_te', 'Tỉ lệ rơi đồ sự kiện (%)', '% so với tỉ lệ gốc.', 'so', '10', '{"min": 0, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.Monster.PC_TILE_ITEM_SUKIEN', 8, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.tilePotion', 'kinh_te', 'Tỉ lệ rơi bình thuốc (%)', '% so với tỉ lệ gốc.', 'so', '14', '{"min": 0, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.Monster.PC_TILE_POTION', 9, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.tileNgoc', 'kinh_te', 'Tỉ lệ rơi ngọc (%)', '% so với tỉ lệ gốc.', 'so', '14', '{"min": 0, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.Monster.PC_TILE_NGOC', 10, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.tileItem', 'kinh_te', 'Tỉ lệ rơi đồ thường (%)', '% so với tỉ lệ gốc.', 'so', '10', '{"min": 0, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.Monster.PC_TILE_ITEM', 11, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.chiaXuRoi', 'kinh_te', 'Chia số xu rơi', 'Xu rơi = xu gốc chia số này (tối thiểu 1).', 'so', '30', '{"min": 1, "max": 1000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.Map.DIV_DROP_XU', 12, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.tileExpLv50', 'kinh_te', 'EXP quái từ cấp 50 (%)', '% so với EXP gốc.', 'so', '10', '{"min": 0, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.Monster.PC_TILE_EXP_LV50', 13, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.tileExpDuoiLv50', 'kinh_te', 'EXP quái dưới cấp 50 (%)', '% so với EXP gốc.', 'so', '50', '{"min": 0, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.Monster.PC_TILE_EXP_DUOI_LV50', 14, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.expvantieu', 'kinh_te', 'Thưởng EXP giao hàng', '1 = bật.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.ExpVantieu', 15, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.Luckyexp', 'kinh_te', 'Ô EXP rương may mắn', '1 = bật.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.LuckyExp', 16, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.ExpQua', 'kinh_te', 'EXP cây ăn quả', '1 = bật (cũng khoá nút tăng tốc cây).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.ExpQua', 17, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.giaXuBoHoaDoVuLan', 'kinh_te', 'Giá xu bó hoa Vu Lan', 'Số xu.', 'so', '150000', '{"min": 0, "max": 2000000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.giaXuBoHoaDoVuLan', 18, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.tieuNguongMau', 'kinh_te', 'Giao hàng: ngưỡng màu khi nhận', '5 ngưỡng cộng dồn trên thang 100000 (Trắng..Vàng, còn lại Tím).', 'ds_so', '60189,89389,99689,99989,99999', '{"min": 0, "max": 100000, "soPhanTu": 5}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.Char.TIEU_NGUONG_MAU', 19, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.tieuHeSoExp', 'kinh_te', 'Giao hàng: hệ số EXP chung (%)', '200 = ×2.', 'so', '200', '{"min": 0, "max": 10000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.Char.TIEU_HE_SO_EXP', 20, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.tieuPcExpTheoMau', 'kinh_te', 'Giao hàng: % EXP theo màu', '7 số (Trắng..Tím, số cuối không dùng).', 'ds_so', '50,100,150,250,250,500,0', '{"min": 0, "max": 100000, "soPhanTu": 7}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.Char.TIEU_PC_EXP_THEO_MAU', 21, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.tieuPermilleCaoCap', 'kinh_te', 'Giao hàng: phần nghìn EXP cao cấp', '9 số (3 khung cấp × 3 màu).', 'ds_so', '50,70,90,35,55,75,5,25,45', '{"min": 0, "max": 1000, "soPhanTu": 9}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.Char.TIEU_PERMILLE_CAO_CAP', 22, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.doiTieuNguongMauXu', 'kinh_te', 'Đổi hàng (xu): ngưỡng màu', '5 ngưỡng cộng dồn.', 'ds_so', '50545,85545,98945,99945,99995', '{"min": 0, "max": 100000, "soPhanTu": 5}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.Char.DOI_TIEU_NGUONG_MAU_XU', 23, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.doiTieuNguongMauLuong', 'kinh_te', 'Đổi hàng (lượng): ngưỡng màu', '5 ngưỡng cộng dồn.', 'ds_so', '22890,52890,96890,99890,99990', '{"min": 0, "max": 100000, "soPhanTu": 5}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.Char.DOI_TIEU_NGUONG_MAU_LUONG', 24, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('roi.xu.phan_tram', 'kinh_te', 'Tỉ lệ một quái rơi xu (%) — dài hạn', 'Xác suất rơi xu; số xu mỗi lần tự chia ngược để kỳ vọng xu/quái không đổi.', 'so', '30', '{"min": 0, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'KinhTeLauDai', 25, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('roi.da_nang_cap.phan_nghin', 'kinh_te', 'Tỉ lệ rơi đá nâng cấp (‰)', 'Phần nghìn mỗi quái khi lệch cấp ≤ 5.', 'so', '15', '{"min": 0, "max": 200}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'KinhTeLauDai', 26, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('exp.nang_luong_phut', 'kinh_te', 'Phút năng lượng 100% / ngày', 'Số phút EXP/rơi đầy đủ mỗi ngày.', 'so', '90', '{"min": 0, "max": 1440}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'LauDaiNhanVat', 27, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('exp.nang_luong_tich_phut', 'kinh_te', 'Năng lượng tích luỹ tối đa (phút)', 'Phút năng lượng dồn tối đa khi không chơi.', 'so', '180', '{"min": 0, "max": 4320}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'LauDaiNhanVat', 28, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('thue.cho', 'kinh_te', 'Thuế chợ ký gửi (%)', 'Đốt khỏi nền kinh tế khi bán được.', 'so', '6', '{"min": 0, "max": 50}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'KinhTeLauDai', 29, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('thue.dang_ban', 'kinh_te', 'Phí đăng bán (%)', 'Đốt, không hoàn; tối thiểu 1 xu.', 'so', '1', '{"min": 0, "max": 20}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'KinhTeLauDai', 30, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('thue.chuyen_luong', 'kinh_te', 'Phí chuyển lượng (%)', 'Làm tròn lên, tối thiểu 1 khi > 0.', 'so', '5', '{"min": 0, "max": 50}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'KinhTeLauDai', 31, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('thue.tran_chuyen_xu_gio', 'kinh_te', 'Trần chuyển xu / ngày (giờ thu nhập)', 'Bội số thu nhập 1 giờ ở cấp người gửi.', 'so', '5', '{"min": 0, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'KinhTeLauDai', 32, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('phi.dich_chuyen_phan_tram_gio', 'kinh_te', 'Phí dịch chuyển (% thu nhập 1 giờ)', 'Tối thiểu 1 xu.', 'so', '3', '{"min": 0, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'KinhTeLauDai', 33, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('phi.hoi_sinh_phan_tram_gio', 'kinh_te', 'Phí hồi sinh (% thu nhập 1 giờ)', 'Tối thiểu 1 xu.', 'so', '5', '{"min": 0, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'KinhTeLauDai', 34, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('phi.ban_npc_phan_tram', 'kinh_te', 'Giá bán đồ cho NPC (% giá)', 'Không cộng theo cấp cường hoá.', 'so', '5', '{"min": 0, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'KinhTeLauDai', 35, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tieu_hao.sua_do_phan_tram_thu_nhap', 'kinh_te', 'Trần phí sửa đồ (% thu nhập 1 giờ / món)', 'Phí sửa = min(công thức cũ, trần × số món).', 'so', '8', '{"min": 0, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'KinhTeLauDai', 36, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('giftcode.toi_da_thang', 'su_kien', 'Giftcode dài hạn tối đa / tháng', 'Số giftcode một Pokémon nhập được mỗi tháng.', 'so', '2', '{"min": 0, "max": 20}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'GiftcodeLauDai', 37, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('giftcode.gia_tri_toi_da', 'su_kien', 'Trần giá trị một giftcode', 'Điểm giá trị tối đa của một mã (GiftcodeAnToanLogic).', 'so', '30', '{"min": 0, "max": 100000}', 'ngay', 0, 0, 0, 'cau_hinh', 'ca_hai', 'GiftcodeAnToanLogic', 38, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('giftcode.ngan_sach_thang', 'su_kien', 'Ngân sách giftcode / tháng', 'Tổng giá trị giftcode được phát mỗi tháng.', 'so', '20000', '{"min": 0, "max": 100000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'ca_hai', 'GiftcodeAnToanLogic', 39, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.nap_tien', 'nap_tien', 'Hệ thống nạp tiền (máy chủ game)', 'Công tắc tổng phía game: 1 = bật (chỉ chạy khi sv.lau_dai=1). Phía web còn cần HTBB_NAP_BAT.', 'co', '0', '{}', 'khoi_dong_lai', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.NapTien', 40, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('HTBB_NAP_BAT', 'nap_tien', 'Web: bật nạp tiền (biến môi trường)', 'Đặt trong .env của web, không sửa từ trang này. Tắt hoặc cấu hình cổng chưa an toàn = KHOÁ toàn bộ thu tiền.', 'co', '0', '{}', 'khoi_dong_lai', 0, 0, 1, 'env', 'web', 'HTBB-Web server/utils/config.ts', 41, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('NAP_DA_CO_GIAY_PHEP', 'nap_tien', 'Xác nhận giấy phép hình ảnh/IP (biến môi trường)', 'Chủ máy chủ tự xác nhận đã có giấy phép. Thiếu = không bán Thành viên, cảnh báo đỏ ở trang Nạp tiền.', 'co', '0', '{}', 'khoi_dong_lai', 0, 0, 1, 'env', 'web', 'HTBB-Web server/utils/config.ts', 42, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('nap.luong.bat', 'nap_tien', 'Bán gói lượng', '0 = web ngừng tạo đơn gói lượng mới (đơn đã thanh toán vẫn được cộng).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'web', 'HTBB-Web server/utils/napTien.ts#napTaoDon', 43, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('nap.the_thang.bat', 'nap_tien', 'Bán thẻ tháng', '0 = web ngừng tạo đơn thẻ tháng mới. Người đã mua vẫn nhận lượng hằng ngày.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'web', 'HTBB-Web server/utils/napTien.ts#napTaoDon', 44, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('nap.ve_mua.bat', 'nap_tien', 'Bán vé mùa cao cấp', '0 = web ngừng tạo đơn vé mùa mới.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'web', 'HTBB-Web server/utils/napTien.ts#napTaoDon', 45, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('thanh_vien.ban_bat', 'nap_tien', 'Bán Thành viên HTBB', '1 = mở bán (còn cần giấy phép + cổng an toàn). Cùng khoá với trang Thành viên.', 'co', '0', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'ca_hai', 'ThanhVienLogic / server/utils/thanhVien.ts', 46, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('nap.tang_toc.bat', 'nap_tien', 'Bán vật phẩm tăng tốc (bằng lượng, trong game)', '0 = cửa hàng lượng trong game ngừng bán vé EXP / bùa thể lực / ủy thác / kẹo hiếm bản cao cấp.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.NapTienGame#mua', 47, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('nap.hoan_tien.thu_cong', 'nap_tien', 'Cho phép hoàn tiền thủ công', '0 = khoá nút Hoàn tiền / Chargeback ở trang Nạp tiền.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'web', 'HTBB-Web api/admin/naptien/hoan-tien.post.ts', 48, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('nap.hoan_tien.tu_dong', 'nap_tien', 'Tự động thu hồi khi hoàn đơn đã cộng', '1 = máy chủ game tự chuyển lượng thành nợ / trừ thẻ tháng / gỡ vé mùa cho đơn ''chờ thu hồi''. 0 = đơn đứng chờ, xử lý tay.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.NapTienGame#thuHoiDon', 49, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tran_tuan_vnd', 'nap_tien', 'Trần nạp / tuần / tài khoản (VND)', '0 = mặc định 1.000.000 đ. Bị kẹp ≤ 5.000.000.', 'so', '1000000', '{"min": 0, "max": 5000000}', 'ngay', 0, 0, 0, 'nap_cau_hinh', 'web', 'HTBB-Web server/utils/napTien.ts#napTran', 50, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tran_thang_vnd', 'nap_tien', 'Trần nạp / tháng / tài khoản (VND)', '0 = mặc định 2.000.000 đ. Bị kẹp ≤ 5.000.000.', 'so', '2000000', '{"min": 0, "max": 5000000}', 'ngay', 0, 0, 0, 'nap_cau_hinh', 'web', 'HTBB-Web server/utils/napTien.ts#napTran', 51, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('thanh_vien.bat', 'thanh_vien', 'Bật yêu cầu Thành viên', '1 = các tính năng trong danh sách dưới chỉ dành cho Thành viên. 0 = không khoá gì.', 'co', '0', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'ca_hai', 'com.htbb.pokemon.ThanhVien', 52, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('thanh_vien.gia', 'thanh_vien', 'Giá Thành viên (VND)', 'Đồng bộ sang gói nạp THANH_VIEN khi lưu.', 'so', '20000', '{"min": 10000, "max": 2000000, "buoc": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'ca_hai', 'ThanhVienLogic', 53, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('thanh_vien.thoi_han_ngay', 'thanh_vien', 'Thời hạn (ngày)', '0 = vĩnh viễn.', 'so', '0', '{"min": 0, "max": 36500}', 'ngay', 0, 0, 0, 'cau_hinh', 'ca_hai', 'ThanhVienLogic', 54, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('thanh_vien.chuc_nang', 'thanh_vien', 'Tính năng yêu cầu Thành viên', 'Chọn các tính năng bị khoá với tài khoản thường.', 'ds_chuoi', 'giao_dich,nhan_tang,chat_the_gioi,cho,cho_sinh_thai,chuyen_luong,bang,pvp_xep_hang,giftcode', '{"luaChon": ["giao_dich", "nhan_tang", "chat_the_gioi", "cho", "cho_sinh_thai", "chuyen_luong", "bang", "pvp_xep_hang", "giftcode", "qua_online", "gui_thu"], "luaChonTen": ["Giao d\\u1ecbch tr\\u1ef1c ti\\u1ebfp", "Nh\\u1eadn giao d\\u1ecbch / qu\\u00e0", "Chat th\\u1ebf gi\\u1edbi (loa)", "Ch\\u1ee3 k\\u00fd g\\u1eedi", "Ch\\u1ee3 sinh th\\u00e1i", "Chuy\\u1ec3n l\\u01b0\\u1ee3ng", "\\u0110\\u1ed9i", "Gi\\u1ea3i Li\\u00ean minh x\\u1ebfp h\\u1ea1ng", "Giftcode", "Qu\\u00e0 online", "Nh\\u1eafn tin ri\\u00eang / t\\u1eb7ng qu\\u00e0"]}', 'ngay', 0, 0, 0, 'cau_hinh', 'ca_hai', 'ThanhVienLogic.CHUC_NANG', 55, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('thanh_vien.mien_cap', 'thanh_vien', 'Miễn yêu cầu dưới cấp', 'Người chơi dưới cấp này được dùng thử (0 = không miễn).', 'so', '0', '{"min": 0, "max": 200}', 'ngay', 0, 0, 0, 'cau_hinh', 'ca_hai', 'ThanhVienLogic', 56, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('thanh_vien.thong_bao', 'thanh_vien', 'Câu thông báo khi bị khoá', 'Trống = câu mặc định.', 'chuoi', '', '{"doDai": 250}', 'ngay', 0, 0, 0, 'cau_hinh', 'ca_hai', 'ThanhVienLogic', 57, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('kich_hoat.chan_dang_nhap', 'thanh_vien', 'Chặn đăng nhập tài khoản chưa kích hoạt', '0 (mặc định) = chưa kích hoạt vẫn vào chơi, chỉ bị khoá các tính năng dưới. 1 = hành vi cũ: không vào được game. Chỉ có tác dụng khi sv.EnableAvtiveCheck=1.', 'co', '0', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'ca_hai', 'com.htbb.pokemon.ThanhVien#chanDangNhap', 58, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('kich_hoat.chuc_nang', 'thanh_vien', 'Tính năng khoá khi chưa kích hoạt', 'Cùng cổng với Thành viên: bị chặn nếu (cần kích hoạt và chưa kích hoạt) hoặc (cần Thành viên và chưa là Thành viên).', 'ds_chuoi', 'giao_dich,chat_the_gioi,cho,chuyen_luong,nhan_tang,giftcode', '{"luaChon": ["giao_dich", "nhan_tang", "chat_the_gioi", "cho", "cho_sinh_thai", "chuyen_luong", "bang", "pvp_xep_hang", "giftcode", "qua_online", "gui_thu"], "luaChonTen": ["Giao d\\u1ecbch tr\\u1ef1c ti\\u1ebfp", "Nh\\u1eadn giao d\\u1ecbch / qu\\u00e0", "Chat th\\u1ebf gi\\u1edbi (loa)", "Ch\\u1ee3 k\\u00fd g\\u1eedi", "Ch\\u1ee3 sinh th\\u00e1i", "Chuy\\u1ec3n l\\u01b0\\u1ee3ng", "\\u0110\\u1ed9i", "Gi\\u1ea3i Li\\u00ean minh x\\u1ebfp h\\u1ea1ng", "Giftcode", "Qu\\u00e0 online", "Nh\\u1eafn tin ri\\u00eang / t\\u1eb7ng qu\\u00e0"]}', 'ngay', 0, 0, 0, 'cau_hinh', 'ca_hai', 'ThanhVienLogic.quyetDinh', 59, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('kich_hoat.thong_bao', 'thanh_vien', 'Câu thông báo khi bị khoá vì chưa kích hoạt', 'Trống = câu mặc định (chỉ tới /kichhoat). Thay được {ten} {web}.', 'chuoi', '', '{"doDai": 250}', 'ngay', 0, 0, 0, 'cau_hinh', 'ca_hai', 'ThanhVienLogic.thongBaoKichHoat', 60, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('kich_hoat.thanh_vien_la_kich_hoat', 'thanh_vien', 'Thành viên được coi như đã kích hoạt', '1 (mặc định) = tài khoản là Thành viên HTBB không bị khoá vì chưa kích hoạt (gộp hai bậc làm một nếu muốn).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'ca_hai', 'ThanhVienLogic.quyetDinh', 61, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chong_clone.bat', 'chong_clone', 'Bật chống clone', '1 = áp các cổng dưới cho mọi đường chuyển xu / giá trị giữa hai người chơi. 0 = tắt hết (chỉ để khẩn cấp).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.ChongClone', 62, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chong_clone.cap_toi_thieu', 'chong_clone', 'Cấp tối thiểu để chuyển / nhận xu', 'Cả hai bên của giao dịch, ký gửi, giao dịch thú cưng lấy xu, chợ trang trí / sinh thái, thách đấu cược xu.', 'so', '20', '{"min": 0, "max": 200}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongCloneLogic.lyDoChan', 63, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chong_clone.tuoi_tai_khoan_gio', 'chong_clone', 'Tuổi tài khoản tối thiểu (giờ)', 'Tính từ lúc đăng ký (team_user.regdate). 0 = không xét.', 'so', '24', '{"min": 0, "max": 2160}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongCloneLogic.lyDoChan', 64, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chong_clone.gio_choi_toi_thieu', 'chong_clone', 'Giờ chơi tối thiểu của Pokémon (giờ)', 'tob_char.totalTimePlay + phiên đang chơi. 0 = không xét.', 'so', '10', '{"min": 0, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongCloneLogic.lyDoChan', 65, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chong_clone.nv_cung_may_toi_da', 'chong_clone', 'Pokémon cùng thiết bị / IP gửi cho một người / ngày', 'Mỗi người nhận, mỗi ngày: tối đa N Pokémon (tài khoản phụ) dùng chung thiết bị hoặc IP được chuyển cho họ; Pokémon thứ N+1 bị chặn. IP proxy tin cậy chỉ tính theo thiết bị.', 'so', '2', '{"min": 1, "max": 50}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongCloneLogic.SoNgay', 66, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chong_clone.nghi_so_nguoi_gui', 'chong_clone', 'Ngưỡng "nghi clone" (số người gửi / ngày)', 'Một người nhận có từ N người gửi khác nhau trong ngày thì đánh dấu nghi (không chặn), hiện ở /admin/kinh-te.', 'so', '3', '{"min": 2, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongCloneLogic.SoNgay', 67, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chong_clone.ky_gui_gia_toi_da_lan', 'chong_clone', 'Giá ký gửi tối đa (lần giá gốc)', 'Giá rao ≤ max(giá gốc × N, thu nhập 1 giờ ở cấp người bán × số giờ bên dưới).', 'so', '10', '{"min": 1, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongCloneLogic.giaToiDa', 68, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chong_clone.ky_gui_gia_toi_da_gio', 'chong_clone', 'Giá ký gửi tối đa (giờ thu nhập)', 'Trần cho món không có giá gốc (và sàn của trần trên).', 'so', '200', '{"min": 1, "max": 100000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongCloneLogic.giaToiDa', 69, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chong_clone.ky_gui_gia_toi_thieu_phan_tram', 'chong_clone', 'Giá ký gửi tối thiểu (% giá gốc)', 'Chặn bán phá giá cho clone.', 'so', '5', '{"min": 0, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongCloneLogic.giaToiThieu', 70, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chong_clone.ban_npc_do_khoa', 'chong_clone', 'Bán đồ / nguyên liệu khoá cho NPC lấy xu', '0 = cấm (đồ khoá mua bằng lượng khoá không đổi ra xu được). 1 = cho như cũ.', 'co', '0', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.OfflineMap#doSellItem / real.Map#doSellGemItem', 71, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.giay_moi_vong', 'vong_quay', 'Thời gian một lượt (giây)', 'Đếm từ lúc có người đặt đầu tiên. Hết giờ: từ 2 người trở lên thì quay, chỉ 1 người thì hoàn lại đủ.', 'so', '120', '{"min": 30, "max": 3600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.VongQuayLogic', 72, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.giay_khoa', 'vong_quay', 'Khoá đặt cuối lượt (giây)', 'Mấy giây cuối không nhận thêm cược.', 'so', '10', '{"min": 0, "max": 120}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.VongQuayLogic', 73, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.thuong.bat', 'vong_quay', 'Vòng quay thường: bật', '0 = đóng (người đang chờ vẫn được quay / hoàn như thường).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 74, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.thuong.cap_toi_thieu', 'vong_quay', 'Vòng quay thường: cấp tối thiểu', 'Luôn cộng thêm cổng chống clone (chong_clone.cap_toi_thieu, tuổi tài khoản, giờ chơi): cấp hiệu lực = max(hai số).', 'so', '20', '{"min": 0, "max": 200}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 75, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.thuong.cuoc_toi_thieu', 'vong_quay', 'Vòng quay thường: đặt tối thiểu mỗi lượt (xu)', 'Mỗi người đặt 1 lần / lượt.', 'so', '1000', '{"min": 1, "max": 2000000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 76, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.thuong.cuoc_toi_da', 'vong_quay', 'Vòng quay thường: đặt tối đa mỗi lượt (xu)', 'Trần hũ = số này × số người tối đa.', 'so', '10000', '{"min": 1, "max": 2000000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 77, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.thuong.cuoc_ngay_toi_da', 'vong_quay', 'Vòng quay thường: tổng đặt tối đa mỗi ngày (xu)', 'Mỗi Pokémon của người chơi, tính theo ngày.', 'so', '30000', '{"min": 1, "max": 2000000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 78, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.thuong.luot_ngay_toi_da', 'vong_quay', 'Vòng quay thường: số lượt tối đa mỗi ngày', 'Mỗi Pokémon của người chơi.', 'so', '10', '{"min": 1, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 79, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.thuong.so_nguoi_toi_da', 'vong_quay', 'Vòng quay thường: số người tối đa mỗi lượt', '', 'so', '20', '{"min": 2, "max": 50}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 80, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.thuong.phi_phan_tram', 'vong_quay', 'Vòng quay thường: phí hũ (%)', 'Phần trăm hũ bị huỷ (hố hút xu) khi có người thắng. Tối thiểu 5% để vòng quay không thành đường chuyển xu rẻ hơn giao dịch.', 'so', '10', '{"min": 5, "max": 50}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 81, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.vip.bat', 'vong_quay', 'Vòng quay VIP: bật', '0 = đóng (người đang chờ vẫn được quay / hoàn như thường).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 82, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.vip.cap_toi_thieu', 'vong_quay', 'Vòng quay VIP: cấp tối thiểu', 'Luôn cộng thêm cổng chống clone (chong_clone.cap_toi_thieu, tuổi tài khoản, giờ chơi): cấp hiệu lực = max(hai số).', 'so', '50', '{"min": 0, "max": 200}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 83, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.vip.cuoc_toi_thieu', 'vong_quay', 'Vòng quay VIP: đặt tối thiểu mỗi lượt (xu)', 'Mỗi người đặt 1 lần / lượt.', 'so', '10000', '{"min": 1, "max": 2000000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 84, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.vip.cuoc_toi_da', 'vong_quay', 'Vòng quay VIP: đặt tối đa mỗi lượt (xu)', 'Trần hũ = số này × số người tối đa.', 'so', '50000', '{"min": 1, "max": 2000000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 85, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.vip.cuoc_ngay_toi_da', 'vong_quay', 'Vòng quay VIP: tổng đặt tối đa mỗi ngày (xu)', 'Mỗi Pokémon của người chơi, tính theo ngày.', 'so', '100000', '{"min": 1, "max": 2000000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 86, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.vip.luot_ngay_toi_da', 'vong_quay', 'Vòng quay VIP: số lượt tối đa mỗi ngày', 'Mỗi Pokémon của người chơi.', 'so', '10', '{"min": 1, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 87, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.vip.so_nguoi_toi_da', 'vong_quay', 'Vòng quay VIP: số người tối đa mỗi lượt', '', 'so', '20', '{"min": 2, "max": 50}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 88, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.vip.phi_phan_tram', 'vong_quay', 'Vòng quay VIP: phí hũ (%)', 'Phần trăm hũ bị huỷ (hố hút xu) khi có người thắng. Tối thiểu 5% để vòng quay không thành đường chuyển xu rẻ hơn giao dịch.', 'so', '10', '{"min": 5, "max": 50}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 89, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.luong.bat', 'vong_quay', 'Vòng quay lượng: bật', '0 = đóng (người đang chờ vẫn được quay / hoàn như thường).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 90, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.luong.cap_toi_thieu', 'vong_quay', 'Vòng quay lượng: cấp tối thiểu', 'Luôn cộng thêm cổng chống clone (chong_clone.cap_toi_thieu, tuổi tài khoản, giờ chơi): cấp hiệu lực = max(hai số).', 'so', '20', '{"min": 0, "max": 200}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 91, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.luong.cuoc_toi_thieu', 'vong_quay', 'Vòng quay lượng: đặt tối thiểu mỗi lượt (lượng)', 'Mỗi người đặt 1 lần / lượt.', 'so', '10', '{"min": 1, "max": 2000000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 92, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.luong.cuoc_toi_da', 'vong_quay', 'Vòng quay lượng: đặt tối đa mỗi lượt (lượng)', 'Trần hũ = số này × số người tối đa.', 'so', '100', '{"min": 1, "max": 2000000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 93, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.luong.cuoc_ngay_toi_da', 'vong_quay', 'Vòng quay lượng: tổng đặt tối đa mỗi ngày (lượng)', 'Mỗi Pokémon của người chơi, tính theo ngày.', 'so', '300', '{"min": 1, "max": 2000000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 94, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.luong.luot_ngay_toi_da', 'vong_quay', 'Vòng quay lượng: số lượt tối đa mỗi ngày', 'Mỗi Pokémon của người chơi.', 'so', '10', '{"min": 1, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 95, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.luong.so_nguoi_toi_da', 'vong_quay', 'Vòng quay lượng: số người tối đa mỗi lượt', '', 'so', '20', '{"min": 2, "max": 50}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 96, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.luong.phi_phan_tram', 'vong_quay', 'Vòng quay lượng: phí hũ (%)', 'Phần trăm hũ bị huỷ (hố hút lượng) khi có người thắng. Tối thiểu 5% để vòng quay không thành đường chuyển lượng rẻ hơn giao dịch.', 'so', '15', '{"min": 5, "max": 50}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 97, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('thach_dau.bat', 'cuoc_xu', 'Thách đấu cược xu: bật', '0 = không nhận trận cược mới (trận đang đánh vẫn chia như thường).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ThachDauCuoc / com.htbb.pokemon.ThachDauLogic', 98, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('thach_dau.cap_toi_thieu', 'cuoc_xu', 'Thách đấu cược xu: cấp tối thiểu', 'Mọi người trong trận (1-1 hoặc cả hai nhóm). Luôn cộng thêm cổng chống clone: cấp hiệu lực = max(hai số).', 'so', '40', '{"min": 0, "max": 200}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ThachDauCuoc / com.htbb.pokemon.ThachDauLogic', 99, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('thach_dau.cuoc_toi_thieu', 'cuoc_xu', 'Thách đấu cược xu: cược tối thiểu (xu)', 'Mỗi bên.', 'so', '1000', '{"min": 1, "max": 2000000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ThachDauCuoc / com.htbb.pokemon.ThachDauLogic', 100, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('thach_dau.cuoc_toi_da', 'cuoc_xu', 'Thách đấu cược xu: cược tối đa (xu)', 'Trần tuyệt đối mỗi bên; trần thật = min(số này, số giờ dưới × thu nhập 1 giờ ở cấp thấp nhất trong trận).', 'so', '100000', '{"min": 1, "max": 2000000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ThachDauCuoc / com.htbb.pokemon.ThachDauLogic', 101, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('thach_dau.cuoc_toi_da_gio', 'cuoc_xu', 'Thách đấu cược xu: cược tối đa (giờ thu nhập)', 'Theo cấp thấp nhất trong trận (LauDai.xuMoiGio).', 'so', '2', '{"min": 1, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ThachDauCuoc / com.htbb.pokemon.ThachDauLogic', 102, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('thach_dau.phi_phan_tram', 'cuoc_xu', 'Thách đấu cược xu: phí bên thắng (%)', 'Phần trăm của 2 × cược bị huỷ (hố hút) khi có bên thắng; hoà thì hoàn đủ. Tối thiểu 5% để thua cố ý không rẻ hơn giao dịch.', 'so', '10', '{"min": 5, "max": 50}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ThachDauCuoc / com.htbb.pokemon.ThachDauLogic', 103, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('li_xi.tui_nhan_ngay_toi_da', 'cuoc_xu', 'Bao lì xì: nhận tối đa / ngày', 'Mỗi Pokémon của người chơi.', 'so', '2', '{"min": 1, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.Char.doOpenLixi2021 / real.Map.doReceiveLixi / com.htbb.pokemon.LiXiLogic', 104, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('li_xi.tui_mo_ngay_toi_da', 'cuoc_xu', 'Bao lì xì: mở tối đa / ngày', 'Mỗi Pokémon của người chơi (bao nhận qua giao dịch cũng tính). Vượt thì trả lại bao.', 'so', '3', '{"min": 1, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.Char.doOpenLixi2021 / real.Map.doReceiveLixi / com.htbb.pokemon.LiXiLogic', 105, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('li_xi.tui_mo_dot_toi_da', 'cuoc_xu', 'Bao lì xì: mở tối đa cả đợt Tết', 'Mỗi Pokémon của người chơi, đợt = năm.', 'so', '15', '{"min": 1, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.Char.doOpenLixi2021 / real.Map.doReceiveLixi / com.htbb.pokemon.LiXiLogic', 106, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('li_xi.tui_xu_phan_tram_gio_min', 'cuoc_xu', 'Bao lì xì: xu thấp nhất (% giờ thu nhập)', '% thu nhập 1 giờ ở cấp người mở.', 'so', '5', '{"min": 0, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.Char.doOpenLixi2021 / real.Map.doReceiveLixi / com.htbb.pokemon.LiXiLogic', 107, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('li_xi.tui_xu_phan_tram_gio_max', 'cuoc_xu', 'Bao lì xì: xu cao nhất (% giờ thu nhập)', '', 'so', '10', '{"min": 0, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.Char.doOpenLixi2021 / real.Map.doReceiveLixi / com.htbb.pokemon.LiXiLogic', 108, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('li_xi.tui_giai_lon_phan_tram_gio', 'cuoc_xu', 'Bao lì xì: giải lớn (% giờ thu nhập)', 'Trước: 1.000.000 – 5.000.000 xu. Tối đa 300% (kẹp trong code); 5 giải / đợt toàn máy chủ.', 'so', '100', '{"min": 0, "max": 300}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.Char.doOpenLixi2021 / real.Map.doReceiveLixi / com.htbb.pokemon.LiXiLogic', 109, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('li_xi.ma_luong_khoa_min', 'cuoc_xu', 'Mã lì xì: lượng khoá thấp nhất', 'Trả lượng KHOÁ (không giao dịch).', 'so', '5', '{"min": 1, "max": 50}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.MapNhapText (-32051) / com.htbb.pokemon.LiXiLogic', 110, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('li_xi.ma_luong_khoa_max', 'cuoc_xu', 'Mã lì xì: lượng khoá cao nhất', '', 'so', '15', '{"min": 1, "max": 50}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.MapNhapText (-32051) / com.htbb.pokemon.LiXiLogic', 111, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('li_xi.ma_tai_khoan_moi_may', 'cuoc_xu', 'Mã lì xì: tài khoản / thiết bị / IP / đợt', 'Mỗi tài khoản 1 mã / đợt; mỗi thiết bị và mỗi IP tối đa N tài khoản / đợt.', 'so', '1', '{"min": 1, "max": 20}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.MapNhapText (-32051) / com.htbb.pokemon.LiXiLogic', 112, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('li_xi.ma_tong_luong_khoa_dot', 'cuoc_xu', 'Mã lì xì: tổng lượng khoá / đợt (cả máy chủ)', 'Hết quỹ thì mã còn lại không nhận được.', 'so', '20000', '{"min": 0, "max": 10000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.MapNhapText (-32051) / com.htbb.pokemon.LiXiLogic', 113, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.dangKyTrongGame', 'bao_tri', 'Đăng ký trong game', '0 = nút Đăng ký trong game báo ''đăng ký trên web''.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.cmd.DangKyTrongGame', 114, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.dangKyTuKichHoat', 'bao_tri', 'Tự kích hoạt tài khoản đăng ký trong game', 'web = theo cấu hình web (web.autoActivate).', 'chon', 'web', '{"luaChon": ["web", "1", "0"], "luaChonTen": ["Theo web", "Lu\\u00f4n k\\u00edch ho\\u1ea1t", "Kh\\u00f4ng k\\u00edch ho\\u1ea1t"]}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.cmd.DangKyTrongGame', 115, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.dangKyTreMs', 'dang_ky', 'Trễ phản hồi đăng ký (ms)', 'Làm chậm dò tài khoản.', 'so', '800', '{"min": 0, "max": 5000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.cmd.DangKyTrongGame', 116, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.dangKyCsdlTaiKhoan', 'dang_ky', 'CSDL tài khoản', 'Tên database chứa team_user.', 'chuoi', 'account', '{"mau": "^[A-Za-z0-9_]{1,64}$"}', 'khoi_dong_lai', 0, 0, 0, 'cau_hinh', 'server', 'real.cmd.DangKyTrongGame', 117, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('dang_ky.web.bat', 'bao_tri', 'Đăng ký trên web', '0 = trang Đăng ký từ chối tài khoản mới.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'web', 'HTBB-Web api/auth/register.post.ts', 118, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.maxCharPerAccount', 'bao_tri', 'Số Pokémon tối đa / tài khoản', 'Client hỗ trợ tối đa 3.', 'so', '3', '{"min": 1, "max": 3}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'data.Database.MAX_CHAR_PER_ACCOUNT', 119, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.suKienTuKhoiDongLai', 'su_kien', 'Tự khởi động lại khi đổi sự kiện', '1 = lệnh bật/tắt sự kiện có hẹn sẽ tự khởi động lại máy chủ.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.LichSuKien', 120, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.suKienGomGiay', 'su_kien', 'Gom lệnh sự kiện (giây)', 'Gom nhiều lệnh thành một lần khởi động lại.', 'so', '120', '{"min": 30, "max": 3600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.LichSuKien', 121, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.suKienBaoTruocGiay', 'su_kien', 'Báo trước khi đổi sự kiện (giây)', '', 'so', '60', '{"min": 10, "max": 600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.LichSuKien', 122, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.worldCupBatDau', 'su_kien', 'World Cup bắt đầu', 'yyyy-MM-dd HH:mm:ss. Trống = tắt.', 'ngay_gio', '', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.SuKienWorldCup', 123, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.worldCupKetThuc', 'su_kien', 'World Cup kết thúc', 'Trống = tắt.', 'ngay_gio', '', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.SuKienWorldCup', 124, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.worldCupHanNhanQua', 'su_kien', 'World Cup hạn nhận quà', 'Trống = tắt.', 'ngay_gio', '', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.SuKienWorldCup', 125, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.gioChiemThanh', 'su_kien', 'Giờ chiếm Gym', '0–23, −1 = lịch chuẩn 21h thứ 3 + thứ 7.', 'so', '-1', '{"min": -1, "max": 23}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.MapTown', 126, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.phutChiemThanh', 'su_kien', 'Phút chiếm Gym', '0–59.', 'so', '-1', '{"min": -1, "max": 59}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.MapTown', 127, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.ngayChiemThanh', 'su_kien', 'Ngày chiếm Gym', 'Mon,Tue,... hoặc yyyy-MM-dd; trống = mỗi ngày.', 'chuoi', '', '{"mau": "^$|^((Mon|Tue|Wed|Thu|Fri|Sat|Sun)(,(Mon|Tue|Wed|Thu|Fri|Sat|Sun))*|\\\\d{4}-\\\\d{2}-\\\\d{2})$", "doDai": 64}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.MapTown', 128, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.isSuKienTrungThul2016', 'su_kien', 'Sự kiện cũ: Trung thu 2016', 'Cờ sự kiện gốc (đọc lúc khởi động).', 'co', '0', '{}', 'khoi_dong_lai', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.isSuKienTrungThul2016', 129, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.isSuKienTet2017', 'su_kien', 'Sự kiện cũ: Tết 2017', '', 'co', '0', '{}', 'khoi_dong_lai', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.isSuKienTet2017', 130, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.isSuKienGioTo2016', 'su_kien', 'Sự kiện cũ: Giỗ Tổ 2016', '', 'co', '0', '{}', 'khoi_dong_lai', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.isSuKienGioTo2016', 131, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.isSuKienNoel2023', 'su_kien', 'Sự kiện cũ: Noel 2023', '', 'co', '0', '{}', 'khoi_dong_lai', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.isSuKienNoel2023', 132, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.isSuKienTetduonglich2024', 'su_kien', 'Sự kiện cũ: Tết dương lịch 2024', '', 'co', '0', '{}', 'khoi_dong_lai', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.isSuKienTetduonglich2024', 133, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('bao_tri.bat', 'bao_tri', 'Chế độ bảo trì đăng nhập', '1 = từ chối đăng nhập mới (người đang chơi không bị đá). Tương đương tệp bao_tri.txt — bật một trong hai là bảo trì.', 'co', '0', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'server.ChiSoMayChu', 134, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('bao_tri.cau', 'bao_tri', 'Câu thông báo bảo trì', 'Trống = câu mặc định.', 'chuoi', '', '{"doDai": 200}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'server.ChiSoMayChu', 135, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('bao_tri.cho', 'bao_tri', 'Tài khoản được vào khi bảo trì', 'Tên tài khoản (chữ thường), cách nhau dấu phẩy.', 'ds_chuoi', '', '{"mau": "^[a-z0-9_]{3,20}$", "doDai": 255}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'server.ChiSoMayChu', 136, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.gioBaoTri', 'bao_tri', 'Giờ bảo trì hằng ngày', 'HH:MM cố định, ''tat'' = không tự bảo trì, trống = ngẫu nhiên 4:30–5:30.', 'chuoi', '', '{"mau": "^$|^(tat|off)$|^([01]?\\\\d|2[0-3]):[0-5]\\\\d$", "doDai": 5}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.BaoTriDaily', 137, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.chiSoMayChu', 'bao_tri', 'Ghi chỉ số máy chủ + đọc bao_tri.txt', '0 = tắt luồng chỉ số (tob_chi_so_may_chu).', 'co', '1', '{}', 'khoi_dong_lai', 0, 0, 0, 'cau_hinh', 'server', 'server.ChiSoMayChu', 138, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.spam.bat', 'chong_spam', 'Chống spam chat / thao tác', '0 = tắt toàn bộ.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.chongspam.ChongSpam', 139, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.spam.chat.gan', 'chong_spam', 'Nhịp chat gần', 'số tin / giây (vd 5/10).', 'nhip', '5/10', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongSpam', 140, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.spam.chat.rieng', 'chong_spam', 'Nhịp chat riêng', '', 'nhip', '6/10', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongSpam', 141, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.spam.chat.theGioi', 'chong_spam', 'Nhịp chat thế giới / người', '', 'nhip', '1/30', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongSpam', 142, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.spam.theGioiToanServer', 'chong_spam', 'Nhịp chat thế giới toàn máy chủ', '', 'nhip', '12/60', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongSpam', 143, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.spam.chat.doi', 'chong_spam', 'Nhịp chat đội', '', 'nhip', '8/10', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongSpam', 144, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.spam.chat.bang', 'chong_spam', 'Nhịp chat đội', '', 'nhip', '8/10', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongSpam', 145, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.spam.trungLapGiay', 'chong_spam', 'Chặn tin trùng lặp (giây)', '', 'so', '30', '{"min": 0, "max": 3600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongSpam', 146, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.spam.chanQuangCao', 'chong_spam', 'Chặn quảng cáo / link', '', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongSpam', 147, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.spam.viPhamCam', 'chong_spam', 'Số vi phạm thì cấm chat', '', 'so', '4', '{"min": 1, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongSpam', 148, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.spam.camGiay', 'chong_spam', 'Thang cấm chat (giây)', 'Các bậc cấm tăng dần, cách nhau dấu phẩy.', 'ds_so', '60,300,1800,7200', '{"min": 1, "max": 31536000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongSpam', 149, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.socketTimeout', 'chong_spam', 'Hết hạn socket (giây)', 'Áp dụng cho kết nối mới. 0 = tắt.', 'so', '180', '{"min": 0, "max": 3600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.SOCKET_TIMEOUT_MS', 150, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.maxConnPerIp', 'chong_spam', 'Kết nối tối đa / IP', '0 = tắt.', 'so', '50', '{"min": 0, "max": 100000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.MAX_CONN_PER_IP', 151, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.connRateMax', 'chong_spam', 'Số kết nối tối đa / cửa sổ', '0 = tắt.', 'so', '60', '{"min": 0, "max": 100000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.CONN_RATE_MAX', 152, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.connRateWindow', 'chong_spam', 'Cửa sổ đếm kết nối (giây)', '0 = tắt.', 'so', '30', '{"min": 0, "max": 3600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.CONN_RATE_WINDOW_MS', 153, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.dosBanSec', 'chong_spam', 'Thời gian chặn IP ngập (giây)', '', 'so', '300', '{"min": 0, "max": 86400}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.DOS_BAN_MS', 154, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.reloginWaitSec', 'chong_spam', 'Chờ đăng nhập lại (giây)', '', 'so', '0', '{"min": 0, "max": 600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.RELOGIN_WAIT_MS', 155, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.proxyTinCay', 'chong_spam', 'Proxy tin cậy (miễn giới hạn theo IP)', 'IP hoặc dải CIDR (IPv4/IPv6) của WAF / Nova agent, cách nhau dấu phẩy. Kết nối từ đây không bị tính sv.maxConnPerIp, sv.connRate*, chặn sv.dosBanSec, chờ đăng nhập theo IP, hạn mức đăng ký theo IP. Giới hạn theo tài khoản / kết nối / thiết bị vẫn giữ.', 'ds_chuoi', '127.0.0.1,::1', '{"mau": "^[0-9A-Fa-f:.]{2,45}(/[0-9]{1,3})?$", "doDai": 255}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.mang.ProxyTinCay', 156, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.dangNhapIpNguong', 'chong_spam', 'Ngưỡng đăng nhập dồn dập / IP', 'Số lần đăng nhập trong ~1 giây từ một IP (không phải proxy tin cậy) thì bắt đầu chờ + ghi nghi vấn. 0 = tắt.', 'so', '10', '{"min": 0, "max": 100000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.mang.GioiHanIp', 157, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.dangNhapIpChoMs', 'chong_spam', 'Chờ khi đăng nhập dồn dập (ms)', 'Thời gian chờ mỗi lần đăng nhập khi vượt sv.dangNhapIpNguong. 0 = không chờ.', 'so', '3000', '{"min": 0, "max": 10000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.mang.GioiHanIp', 158, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.tranGoiThuong', 'chong_spam', 'Trần gói tin thường / cửa sổ', '', 'so', '300', '{"min": 10, "max": 100000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'io.BoDemGoiTin', 159, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.tranGoiTaiNguyen', 'chong_spam', 'Trần gói tải tài nguyên / cửa sổ', '', 'so', '600', '{"min": 10, "max": 100000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'io.BoDemGoiTin', 160, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.tranGoiTraCuu', 'chong_spam', 'Trần gói tra cứu / cửa sổ', '', 'so', '600', '{"min": 10, "max": 100000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'io.BoDemGoiTin', 161, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.log.muc', 'chong_spam', 'Mức nhật ký (màn hình)', '', 'chon', 'THONG_TIN', '{"luaChon": ["LOI", "CANH_BAO", "THONG_TIN", "GO_LOI", "ERROR", "WARN", "INFO", "DEBUG"], "luaChonTen": ["L\\u1ed7i", "C\\u1ea3nh b\\u00e1o", "Th\\u00f4ng tin", "G\\u1ee1 l\\u1ed7i", "L\\u1ed7i (ERROR)", "C\\u1ea3nh b\\u00e1o (WARN)", "Th\\u00f4ng tin (INFO)", "G\\u1ee1 l\\u1ed7i (DEBUG)"], "hoa": true}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.log.Nhat', 162, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.log.mucTep', 'chong_spam', 'Mức nhật ký (tệp)', 'Trống = như màn hình.', 'chon', '', '{"luaChon": ["", "LOI", "CANH_BAO", "THONG_TIN", "GO_LOI", "ERROR", "WARN", "INFO", "DEBUG"], "luaChonTen": ["Nh\\u01b0 m\\u00e0n h\\u00ecnh", "L\\u1ed7i", "C\\u1ea3nh b\\u00e1o", "Th\\u00f4ng tin", "G\\u1ee1 l\\u1ed7i", "L\\u1ed7i (ERROR)", "C\\u1ea3nh b\\u00e1o (WARN)", "Th\\u00f4ng tin (INFO)", "G\\u1ee1 l\\u1ed7i (DEBUG)"], "hoa": true}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.log.Nhat', 163, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.log.tat', 'chong_spam', 'Loại nhật ký tắt', 'Mã loại, cách nhau dấu phẩy.', 'ds_chuoi', 'GOI3,GOI5,GOI26,VKTT,SQL,LUU_CHAR', '{"mau": "^[A-Z0-9_]{1,32}$", "doDai": 255}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.log.Nhat', 164, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.log.kiemToan', 'chong_spam', 'Loại nhật ký kiểm toán', 'Ghi riêng tệp kiem_toan (giữ lâu).', 'ds_chuoi', 'ADMIN,GM,NAP,NAP_TIEN,GIFTCODE,CAM,BAN,KINH_TE,KINHTE,CHONG_SPAM', '{"mau": "^[A-Z0-9_]{1,32}$", "doDai": 255}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.log.Nhat', 165, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chi_so.bat', 'chi_so', 'Bật giới hạn chỉ số', '0 = tắt mọi trần/giảm dần chỉ số (quay về công thức cũ).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChiSoTrangBiGame', 166, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chi_so.kep_trang_bi', 'chi_so', 'Kẹp chỉ số trang bị theo ngân sách', 'Mỗi món ≤ chuẩn bậc × 1,30 × nâng cấp; đồ bậc cũ ≤ 130% đồ bậc hiện tại.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChiSoTrangBiGame', 167, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chi_so.tran_chi_mang', 'chi_so', 'Trần tỉ lệ chí mạng (‰)', 'Phần nghìn, có giảm dần. PvP cố định 300‰.', 'so', '400', '{"min": 100, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChiSoTrangBiGame', 168, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chi_so.tran_st_chi_mang', 'chi_so', 'Trần ST chí mạng cộng thêm (‰)', 'Trên nền ×1,5. 500 = đòn chí mạng tối đa ×2,0.', 'so', '500', '{"min": 0, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChiSoTrangBiGame', 169, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chi_so.tran_ne', 'chi_so', 'Trần né tránh (‰)', 'Phần nghìn, có giảm dần. PvP cố định 200‰.', 'so', '300', '{"min": 0, "max": 900}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChiSoTrangBiGame', 170, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chi_so.tran_hut', 'chi_so', 'Trần hút HP PvE (‰ sát thương)', 'Gộp mọi nguồn hút HP.', 'so', '100', '{"min": 0, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChiSoTrangBiGame', 171, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chi_so.tran_hut_pvp', 'chi_so', 'Trần hút HP PvP (‰ sát thương thật)', 'Gộp mọi nguồn hút HP, tính trên sát thương sau ×0,30.', 'so', '50', '{"min": 0, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChiSoTrangBiGame', 172, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chi_so.tran_giam_st', 'chi_so', 'Trần giảm sát thương nhận (‰)', 'Gộp mọi nguồn thụ động (PvP cố định 400‰).', 'so', '500', '{"min": 0, "max": 900}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChiSoTrangBiGame', 173, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chi_so.tran_khang_he', 'chi_so', 'Trần kháng một hệ (‰)', 'Kháng hệ + kháng mọi hệ + phiến đá.', 'so', '600', '{"min": 0, "max": 900}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChiSoTrangBiGame', 174, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chi_so.san_sat_thuong', 'chi_so', 'Sàn sát thương mỗi đòn (‰)', 'Sau mọi lớp giảm, một đòn còn ít nhất chừng này phần nghìn (chống bất tử).', 'so', '400', '{"min": 0, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChiSoTrangBiGame', 175, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chi_so.tran_phan_tram_cong', 'chi_so', 'Trần tổng % cộng thêm (‰)', 'Tổng % tấn công/phòng thủ/HP từ pet, thú cưỡi, thời trang, bộ, VIP… so với chỉ số gốc.', 'so', '500', '{"min": 0, "max": 5000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChiSoTrangBiGame', 176, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('pvp.tinh_diem.bat', 'pvp', 'Tính điểm thang PvP', '0 = trận PvP không cộng/trừ điểm thang.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ThangPvpGame', 177, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('gym.bat', 'pvp', 'Thách đấu Gym', '0 = tạm đóng Gym.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.GymLauDai', 178, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('ham_nguc.bat', 'pvp', 'Hầm Ngục bí ẩn', '0 = không vào tầng mới (vẫn nhận thưởng đã đạt).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.SoTayLauDai#hamNguc', 179, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.bat', 'tu_dong', 'Bật Tự động đánh', '0 = tạm khoá (người đang dùng bị dừng ở lần kiểm tiếp theo).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 180, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.gio_moi_ngay', 'tu_dong', 'Giờ Tự động đánh mỗi ngày', 'Miễn phí, chỉ tính lúc online và đang bật. Đặt lại lúc 00:00.', 'so', '5', '{"min": 0, "max": 24}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 181, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.gio_them_the_thang', 'tu_dong', 'Giờ cộng thêm khi có thẻ tháng', 'Cộng vào giờ mỗi ngày khi thẻ tháng còn hạn.', 'so', '2', '{"min": 0, "max": 24}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 182, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.chi_map_da_mo', 'tu_dong', 'Chỉ ở bãi luyện tập', '1 = chỉ bật ở bản đồ có Pokémon hoang dã đã mở (không ở thành phố, nhà, sàn đấu, sự kiện).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 183, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.tat_khi_pvp', 'tu_dong', 'Dừng khi PvP / sàn đấu / Gym / boss', '1 = dừng khi bị Pokémon khác tấn công, bật PK, vào sàn đấu / Gym / chiến trường hay đánh boss.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 184, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.tu_dung_thuoc', 'tu_dong', 'Cho tự uống thuốc', '0 = Tự động đánh không tự uống thuốc hồi phục (người chơi tự bấm).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 185, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.ban_kinh', 'tu_dong', 'Bán kính tối đa (px)', 'Pokémon chỉ đánh quanh điểm bật trong bán kính này; ra xa hơn (+64 px) thì dừng. 1 ô = 16 px.', 'so', '240', '{"min": 48, "max": 960, "buoc": 16}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 186, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.nhip_danh', 'tu_dong', 'Nhịp đánh tối đa khi tự động', 'số lệnh đánh / giây (vd 12/5). Vượt thì bỏ lệnh và tính vi phạm.', 'nhip', '12/5', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 187, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.nhip_thuoc', 'tu_dong', 'Nhịp uống thuốc tối đa khi tự động', '', 'nhip', '4/5', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 188, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.nhip_nhat', 'tu_dong', 'Nhịp nhặt đồ tối đa khi tự động', '', 'nhip', '8/5', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 189, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.nhip_nguoi', 'tu_dong', 'Nhịp đánh nghi bot (mọi người chơi)', 'Chỉ ghi nghi vấn (hack.txt), không chặn.', 'nhip', '30/5', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 190, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.vi_pham_tat', 'tu_dong', 'Số vi phạm nhịp thì dừng', 'Dừng Tự động đánh + ghi nghi vấn.', 'so', '20', '{"min": 1, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 191, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.hoi_sinh_moi_gio', 'tu_dong', 'Tự hồi sinh tối đa / giờ', 'Khi đang tự động mà Pokémon gục: chỉ dùng vật phẩm hồi sinh nếu người chơi bật "Tự hồi sinh", tối đa ngần này lần / 60 phút; còn lại dừng tự động và về Trung tâm Pokémon. 0 = không bao giờ.', 'so', '2', '{"min": 0, "max": 20}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 192, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.ke_hoach', 'tu_dong', 'Máy chủ điều khiển từng bước', '1 = máy chủ chọn mục tiêu, chiêu, thuốc, nhặt đồ, rút lui cho client 0.9.2+ (client chỉ thực hiện). 0 = client tự chọn như bản 0.9.1 (máy chủ vẫn giới hạn giờ / nhịp / vùng).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 193, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.cap_toi_thieu', 'tu_dong', 'Cấp tối thiểu để bật', '0 = mọi cấp. Dưới cấp này bấm Bắt đầu thì được báo rõ cấp cần.', 'so', '0', '{"min": 0, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 194, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.chenh_cap_tren', 'tu_dong', 'Bỏ Pokémon cao hơn quá N cấp', 'Không đánh Pokémon hoang dã cao hơn cấp mình quá ngần này. -1 = không giới hạn.', 'so', '5', '{"min": -1, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 195, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.chenh_cap_duoi', 'tu_dong', 'Bỏ Pokémon thấp hơn quá N cấp', '0 = không giới hạn dưới (vẫn đánh Pokémon yếu).', 'so', '0', '{"min": 0, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 196, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.danh_boss', 'tu_dong', 'Cho đánh boss', '0 = không chọn boss / Pokémon huyền thoại làm mục tiêu (đánh trúng boss vẫn dừng theo tu_dong.tat_khi_pvp).', 'co', '0', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 197, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.hp_quai_toi_da_pt', 'tu_dong', 'Bỏ Pokémon quá trâu (% HP mình)', 'Bỏ Pokémon có HP tối đa lớn hơn ngần này % HP tối đa của mình. 0 = tắt.', 'so', '500', '{"min": 0, "max": 100000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 198, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.chon_chieu_theo_he', 'tu_dong', 'Chọn chiêu theo khắc hệ', '1 = điểm chiêu = sát thương × khắc hệ × STAB, bỏ chiêu đang hồi / thiếu PP. 0 = chỉ theo sát thương.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 199, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.nguong_thuoc_hp', 'tu_dong', 'Ngưỡng thuốc HP mặc định (%)', 'Uống thuốc HP khi HP dưới ngưỡng người chơi chọn trong bảng; client cũ dùng số này. 0 = máy chủ không cho tự uống thuốc HP.', 'so', '50', '{"min": 0, "max": 90}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 200, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.nguong_thuoc_mp', 'tu_dong', 'Ngưỡng thuốc PP mặc định (%)', 'Như trên cho PP. 0 = tắt.', 'so', '30', '{"min": 0, "max": 90}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 201, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.nguong_hp_thap', 'tu_dong', 'HP thấp thì rút lui (%)', 'HP dưới ngưỡng mà hết thuốc: rút về điểm bắt đầu nghỉ (hoặc dừng nếu tu_dong.hp_thap_dung=1). 0 = tắt.', 'so', '25', '{"min": 0, "max": 90}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 202, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.hp_thap_dung', 'tu_dong', 'HP thấp thì dừng hẳn', '1 = dừng Tự động đánh thay vì rút lui nghỉ hồi phục.', 'co', '0', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 203, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.hoi_phuc_toi', 'tu_dong', 'Nghỉ hồi phục tới (%)', 'Rút lui nghỉ tới khi HP đạt ngần này % rồi đánh tiếp.', 'so', '80', '{"min": 10, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 204, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.nhat_ban_kinh', 'tu_dong', 'Bán kính nhặt đồ (px)', 'Chỉ đi nhặt đồ rơi của mình trong bán kính này quanh Pokémon (và trong vùng). 0 = không nhặt. 1 ô = 16 px.', 'so', '96', '{"min": 0, "max": 480, "buoc": 16}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 205, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.dung_khi_day_tui', 'tu_dong', 'Dừng khi hành trang đầy', '1 = hành trang đầy thì dừng và báo.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 206, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.dung_khi_tu_di', 'tu_dong', 'Dừng khi người chơi tự di chuyển', '1 = bấm phím di chuyển / chạm bản đồ thì dừng Tự động đánh. 0 = nhường 3 giây rồi chạy tiếp.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 207, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.tranh_pk_px', 'tu_dong', 'Tránh người bật PK (px)', 'Có người chơi khác bật PK trong bán kính này thì lùi về điểm bắt đầu chờ. 0 = tắt.', 'so', '160', '{"min": 0, "max": 960, "buoc": 16}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 208, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.map_cam', 'tu_dong', 'Bản đồ cấm Tự động đánh thêm (id)', 'Ngoài sàn đấu / Gym / chiến trường / mê cung / bản đồ boss. Cách nhau dấu phẩy; trống = không.', 'chuoi', '', '{"mau": "^[0-9, ]*$", "doDai": 255}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 209, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.hoi_sinh_tra_phi', 'tu_dong', 'Cho tự dùng vật phẩm hồi sinh trả phí', '1 = khi người chơi đồng ý riêng (bảng Tự động: Tự hồi sinh "cả vật phẩm lượng") mới được tự dùng Thuốc hồi sinh tối đa (lượng khoá). 0 = chỉ Thuốc hồi sinh thường; không bao giờ tự trả xu / lượng.', 'co', '0', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 210, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.bat', 'hoi_sinh', 'Bật cơ chế hồi sinh mới', '0 = luồng cũ (popup hồi sinh tại chỗ 5% giờ thu nhập, tự hồi sinh dưới cấp 50).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 211, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.xu_phan_tram_gio', 'tu_dong', 'Phí xu tại chỗ (% thu nhập 1 giờ)', 'Theo cấp (ThongSoLauDai.xuMoiGio). 40 = 0,4 giờ cày. Nhân hệ số gục lặp.', 'so', '40', '{"min": 0, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 212, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.xu_toi_thieu', 'tu_dong', 'Phí xu tại chỗ tối thiểu', 'Xu.', 'so', '200', '{"min": 0, "max": 100000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 213, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.he_so_lap', 'hoi_sinh', 'Hệ số gục lặp (%)', 'Lần gục thứ 1, 2, 3... trong cửa sổ; lần sau dùng số cuối (trần). Áp cho phí xu và lượng khoá.', 'ds_so', '100,150,200,300', '{"min": 0, "max": 10000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 214, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.cua_so_phut', 'hoi_sinh', 'Cửa sổ gục lặp (phút)', 'Đếm số lần gục trong ngần này phút.', 'so', '30', '{"min": 1, "max": 1440}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 215, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.luong_khoa', 'tu_dong', 'Phí lượng khoá tại chỗ', '0 = tắt lựa chọn trả lượng khoá. Nhân hệ số gục lặp (làm tròn lên).', 'so', '2', '{"min": 0, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 216, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.luong_khoa_toi_da', 'tu_dong', 'Trần phí lượng khoá', 'Lượng khoá tối đa một lần.', 'so', '10', '{"min": 0, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 217, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.cho_sau_guc_giay', 'hoi_sinh', 'Chờ sau khi gục (giây)', 'Hồi sinh tại chỗ chỉ bấm được sau ngần này giây (đếm ngược trên hộp thoại).', 'so', '5', '{"min": 0, "max": 600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 218, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.hoi_chieu_giay', 'tu_dong', 'Hồi chiêu hồi sinh tại chỗ (giây)', 'Giữa hai lần hồi sinh tại chỗ. Về Trung tâm luôn được.', 'so', '60', '{"min": 0, "max": 3600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 219, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.hp_thuoc', 'hoi_sinh', 'HP khi dùng Thuốc hồi sinh (%)', 'Thuốc hồi sinh tối đa lấy % trong data_vat_pham_lau_dai (hoi_sinh:100).', 'so', '30', '{"min": 1, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 220, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.hp_tra_phi', 'hoi_sinh', 'HP khi trả xu / lượng khoá / tân thủ (%)', '', 'so', '50', '{"min": 1, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 221, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.exp_mat_phan_nghin', 'hoi_sinh', 'Mất EXP khi về Trung tâm (phần nghìn)', 'Phần nghìn EXP lên cấp hiện tại (20 = 2%). Không bao giờ tụt cấp: chỉ trừ trong tiến độ cấp.', 'so', '20', '{"min": 0, "max": 200}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 222, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.exp_mat_toi_da_phut', 'hoi_sinh', 'Trần mất EXP (phút cày)', 'Không mất quá ngần này phút EXP đánh quái ở cấp đó. 0 = không chặn.', 'so', '15', '{"min": 0, "max": 600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 223, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.do_ben_mat_phan_tram', 'hoi_sinh', 'Mất độ bền khi về Trung tâm (%)', '% độ bền tối đa của mỗi trang bị đang mặc (không làm vỡ đồ).', 'so', '5', '{"min": 0, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 224, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.met_moi_phut', 'hoi_sinh', 'Mệt mỏi (phút)', 'Sau khi về Trung tâm. 0 = tắt.', 'so', '5', '{"min": 0, "max": 120}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 225, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.met_moi_exp', 'hoi_sinh', 'Mệt mỏi: giảm EXP (%)', 'EXP hạ quái.', 'so', '20', '{"min": 0, "max": 90}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 226, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.met_moi_tan_cong', 'hoi_sinh', 'Mệt mỏi: giảm tấn công (%)', '', 'so', '10', '{"min": 0, "max": 90}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 227, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.tan_thu_cap', 'tu_dong', 'Bảo vệ tân thủ dưới cấp', 'Dưới cấp này: không mất EXP / độ bền / Mệt mỏi, hồi sinh tại chỗ miễn phí.', 'so', '15', '{"min": 0, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 228, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.tan_thu_hoi_chieu_giay', 'hoi_sinh', 'Hồi chiêu hồi sinh miễn phí tân thủ (giây)', '', 'so', '30', '{"min": 0, "max": 3600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 229, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.dong_doi_bat', 'hoi_sinh', 'Cho chờ đồng đội hồi sinh', 'Đồng đội ở gần mang Thuốc hồi sinh được mời cứu.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 230, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.dong_doi_giay', 'hoi_sinh', 'Thời gian chờ đồng đội (giây)', '', 'so', '60', '{"min": 10, "max": 600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 231, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.cam_pvp', 'hoi_sinh', 'Cấm tại chỗ ở PvP / sàn đấu', 'Sàn đấu, giải Liên minh, chiến trường, lãnh địa đội, chiếm Gym đang đánh, đấu trường + hoi_sinh.map_pvp.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 232, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.cam_gym', 'hoi_sinh', 'Cấm tại chỗ khi thách đấu Gym', '', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 233, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.cam_boss', 'hoi_sinh', 'Cấm tại chỗ giữa trận boss', 'Đã gây sát thương vào boss thế giới / Gym còn sống cùng bản đồ.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 234, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.cam_me_cung', 'hoi_sinh', 'Cấm tại chỗ trong Mê cung', '', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 235, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.map_pvp', 'hoi_sinh', 'Bản đồ PvP (id)', 'Thêm vào luật cấm PvP (so cả id bản đồ lẫn id nạp).', 'ds_so', '202,203,204,205,227,228', '{"min": 0, "max": 100000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 236, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.map_cam_them', 'hoi_sinh', 'Bản đồ cấm tại chỗ thêm (id)', 'Cách nhau dấu phẩy; trống = không.', 'chuoi', '', '{"mau": "^[0-9, ]*$", "doDai": 255}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 237, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.auto_ve_map_up', 'hoi_sinh', 'Cho "Auto về Map Up" sau khi gục', '0 = gục thì không tự bay lại bãi luyện (chống treo máy vô hạn).', 'co', '0', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 238, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.gia_thuoc_phan_tram_gio', 'tu_dong', 'Giá Thuốc hồi sinh (% thu nhập 1 giờ)', 'Giá ở cửa hàng NPC theo cấp người mua (cùng bậc phí xu tại chỗ). 0 = giá gốc data_potion.', 'so', '30', '{"min": 0, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 239, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.gia_thuoc_toi_thieu', 'tu_dong', 'Giá Thuốc hồi sinh tối thiểu', 'Xu / viên (không thấp hơn giá gốc).', 'so', '407', '{"min": 0, "max": 100000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 240, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.thuoc_moi_gio', 'hoi_sinh', 'Hồi sinh bằng vật phẩm tối đa / giờ', 'Thuốc hồi sinh / tối đa của chính mình (tay, Tự động đánh, dùng từ túi) trong 60 phút. Đồng đội cứu không tính. 0 = không giới hạn.', 'so', '6', '{"min": 0, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 241, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.thongBao.bat', 'thong_bao', 'Gộp thông báo toàn máy chủ', '0 = gửi thẳng như cũ.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.chongspam.ThongBaoGop', 242, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.thongBao.toiDa', 'thong_bao', 'Thông báo tối đa / cửa sổ', '', 'so', '6', '{"min": 1, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ThongBaoGop', 243, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.thongBao.cuaSoGiay', 'thong_bao', 'Cửa sổ đếm thông báo (giây)', '', 'so', '60', '{"min": 1, "max": 3600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ThongBaoGop', 244, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.thongBao.moiLoai', 'thong_bao', 'Tối đa mỗi loại / cửa sổ', '', 'so', '3', '{"min": 1, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ThongBaoGop', 245, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.thongBao.trungLapGiay', 'thong_bao', 'Bỏ thông báo trùng (giây)', '', 'so', '120', '{"min": 0, "max": 86400}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ThongBaoGop', 246, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.thongBao.gopGiay', 'thong_bao', 'Gộp mỗi (giây)', '', 'so', '15', '{"min": 1, "max": 3600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ThongBaoGop', 247, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.website', 'bao_tri', 'Địa chỉ website (hiện trong game)', '', 'chuoi', 'https://huyenthoaibaoboi.fun/', '{"doDai": 200}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.website', 248, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.linkTaiGame', 'bao_tri', 'Link tải game (hiện trong game)', '', 'chuoi', 'https://huyenthoaibaoboi.fun/', '{"doDai": 200}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.linkTaiGame', 249, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.adminAccounts', 'bao_tri', 'Danh sách GM trong game', 'Tên Pokémon GM, cách nhau dấu phẩy.', 'ds_chuoi', '', '{"mau": "^[A-Za-z0-9_]{1,30}$", "doDai": 255}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.adminAccounts', 250, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.gmCanCoPInfo', 'quan_tri', 'GM cần cả cờ pInfo', '1 = quyền GM cần tên trong danh sách VÀ pInfo[16]=1.', 'co', '0', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.gmCanCoPInfo', 251, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.banToiThieu', 'bao_tri', 'Bản client tối thiểu', '> 0 = từ chối client jar/Android cũ hơn.', 'so', '0', '{"min": 0, "max": 1000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.banToiThieu', 252, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.choPhepTaoTenGM', 'quan_tri', 'Cho tạo Pokémon tên GM', 'Chỉ bật tạm thời.', 'co', '0', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'data.Database.CHO_PHEP_TAO_TEN_GM', 253, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.testMode', 'quan_tri', 'Chế độ thử (testMode)', 'Mã test cấp tài nguyên miễn phí. Khoá ở máy thật.', 'co', '0', '{}', 'ngay', 0, 1, 0, 'cau_hinh', 'server', 'TeamServer.testMode', 254, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.testCode', 'quan_tri', 'Mã test', 'Chuỗi gõ trong game khi testMode=1.', 'chuoi', '', '{"mau": "^[ -~]*$", "doDai": 64}', 'ngay', 1, 1, 0, 'cau_hinh', 'server', 'TeamServer.testCode', 255, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('ban_do.khoa.bat', 'che_do', 'Khoá bản đồ theo cấp / cửa ải', '1 = bản đồ cần đủ cấp và xong cửa ải (bảng data_ban_do_mo_khoa) mới vào được, kèm luật Tự động đánh theo bản đồ; 0 = mở mọi bản đồ (người chơi cũ luôn được miễn trừ theo cấp).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.KhoaBanDoGame', 256, 'chinh', 0);

-- cau_hinh_nhom (tung la pokemon_cau_hinh_trung_tam.sql, bo sinh tools/luu_tru_sinh_du_lieu/cauhinh/sinh_cau_hinh.py - da luu tru)
DROP TABLE IF EXISTS `cau_hinh_nhom`;
CREATE TABLE IF NOT EXISTS `cau_hinh_nhom` (`ma` VARCHAR(20) NOT NULL, `ten` VARCHAR(160) NOT NULL DEFAULT '', `mo_ta` TEXT NOT NULL, `thu_tu` INT NOT NULL DEFAULT 0, `an` TINYINT NOT NULL DEFAULT 0, PRIMARY KEY (`ma`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('kinh_te', 'Kinh tế: tỉ lệ rơi & EXP', 'Tỉ lệ rơi xu / lượng / đồ và EXP quái. Áp dụng ngay. Thuế, phí và công thức kinh tế cốt lõi đã cố định (không hiện ở đây).', 1, 0);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('su_kien', 'Sự kiện & giftcode', 'Giờ chiếm Gym, trần & ngân sách giftcode. Bật/tắt từng sự kiện ở trang Sự kiện (bảng tob_su_kien).', 2, 0);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('nap_tien', 'Nạp tiền', 'Công tắc bán từng loại sản phẩm, trần chi tiêu. Web vẫn KHOÁ (fail closed) khi thiếu HTBB_NAP_BAT, giấy phép hoặc bí mật cổng thanh toán — các giá trị đó nằm trong biến môi trường, không ở CSDL.', 3, 0);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('thanh_vien', 'Thành viên & kích hoạt', 'Thành viên HTBB theo tài khoản (trả phí) và Kích hoạt tài khoản (miễn phí): tính năng bị khoá, giá, thời hạn, chặn đăng nhập.', 4, 0);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('chong_clone', 'Chống clone & chuyển xu', 'Cổng cho mọi đường chuyển xu / giá trị giữa hai người chơi (giao dịch, ký gửi, thú cưng lấy xu, chợ trang trí, chợ sinh thái, thách đấu): cấp, tuổi tài khoản, giờ chơi, giới hạn cùng thiết bị, giá ký gửi. Nhật ký + báo cáo "Nghi clone" ở /admin/kinh-te.', 5, 0);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('vong_quay', 'Vòng quay may mắn (cố định)', 'Ba vòng quay góp hũ (thường / VIP bằng xu, lượng): người thắng nhận hũ trừ phí, phí là hố hút. Không in tiền: hũ = tổng cược; chỉ 1 người thì hoàn lại. Cổng chống clone áp như giao dịch xu (cấp, tuổi tài khoản, giờ chơi, tối đa N Pokémon cùng thiết bị / IP mỗi ngày). Mức cược, trần ngày, phí là công thức kinh tế cốt lõi: cố định (đổi khẩn cấp bằng server.ini).', 6, 1);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('cuoc_xu', 'Thách đấu cược xu & lì xì (cố định)', 'Thách đấu cược xu 1-1 và nhóm (ký quỹ hai bên, phí bên thắng là hố hút, hoà hoàn đủ, mức theo cấp) và lì xì Tết (bao: trần nhận / mở theo ngày và đợt, xu theo giờ thu nhập; mã: lượng khoá, mỗi tài khoản / thiết bị / IP một lần, trần tổng). Công thức kinh tế cốt lõi: cố định (đổi khẩn cấp bằng server.ini).', 7, 1);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('tu_dong', 'Tự động đánh & hồi sinh', 'Giới hạn Tự động đánh (chỉ khi online) và giá hồi sinh tại chỗ. Luật an toàn (cấm ở PvP / Gym / boss, nhịp, bán kính…) đã cố định.', 8, 0);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('bao_tri', 'Máy chủ, đăng ký & bảo trì', 'Bảo trì, đăng ký, bản client tối thiểu, link website / tải game, GM, ngày khai trương.', 9, 0);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('che_do', 'Chế độ game (cố định)', 'Chế độ lớn luôn bật trên máy thật (dài hạn, hệ sinh thái). Đổi = sửa server.ini khi khẩn cấp.', 10, 1);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('dang_ky', 'Đăng ký (cố định)', 'Tham số nội bộ của đăng ký trong game.', 11, 1);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('chong_spam', 'Chống spam, DoS & nhật ký (cố định)', 'Nhịp chat/thao tác, chống ngập kết nối/gói tin, mức nhật ký.', 12, 1);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('pvp', 'PvP / Gym / Hầm Ngục (luôn bật)', 'Nội dung chiến đấu dài hạn.', 13, 1);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('chi_so', 'Chỉ số & chống lạm phát (cố định)', 'Trần + giảm dần cho chỉ số trang bị, pet, thú cưỡi, skin (docs/thiet-ke-game.md).', 14, 1);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('hoi_sinh', 'Hồi sinh — luật (cố định)', 'Phạt khi về Trung tâm, mệt mỏi, cấm hồi sinh tại chỗ ở PvP / Gym / boss / Mê cung.', 15, 1);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('thong_bao', 'Thông báo (cố định)', 'Gộp thông báo toàn máy chủ.', 16, 1);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('quan_tri', 'Quản trị & thử nghiệm (cố định)', 'Cờ GM cũ và khoá thử nghiệm (máy thật luôn mặc định).', 17, 1);

-- ======== 3. Chi muc bang nong

-- them_chi_muc_production.sql
CREATE INDEX IF NOT EXISTS idx_5h_active_username ON `5h_active` (username);
CREATE INDEX IF NOT EXISTS idx_5h_active_userid ON `5h_active` (userID);
CREATE INDEX IF NOT EXISTS idx_char_vip_charname ON tob_char_vip (charname);
CREATE INDEX IF NOT EXISTS idx_buy_ruong_ngay_ten ON tob_buy_ruong (dateLog, charname);
CREATE INDEX IF NOT EXISTS idx_qua_ct_top_charname ON tob_qua_ct_top (charname);

-- ======== 4. Cau hinh he thong / cong tac su kien (khong phai du lieu nguoi choi), chay lai duoc

-- sua_he_so_nap_va_qua_tao_nv.sql (chi lenh nay)
UPDATE `5h_systems` SET status = 1 WHERE command = 'tangnap' AND status <> 1;

-- them_lich_su_kien_nam.sql (chi lenh nay)
CREATE TABLE IF NOT EXISTS tob_su_kien_lich (
  ma VARCHAR(40) NOT NULL,
  nam SMALLINT NOT NULL,
  lan TINYINT NOT NULL DEFAULT 0,
  ten VARCHAR(100) NOT NULL DEFAULT '',
  nhom VARCHAR(40) NOT NULL DEFAULT '',
  ngay_le VARCHAR(60) NOT NULL DEFAULT '',
  mo_ta VARCHAR(500) NOT NULL DEFAULT '',
  phan_thuong VARCHAR(300) NOT NULL DEFAULT '',
  lich_mo DATETIME NOT NULL,
  lich_dong DATETIME NOT NULL,
  mo_luc DATETIME NOT NULL,
  dong_luc DATETIME NOT NULL,
  ghi_de TINYINT NOT NULL DEFAULT 0,
  sua_boi VARCHAR(50) NULL,
  sua_luc DATETIME NULL,
  cap_nhat DATETIME NULL,
  PRIMARY KEY (ma, nam, lan), KEY k_mo (mo_luc), KEY k_dong (dong_luc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- them_lich_su_kien_nam.sql (chi lenh nay)
ALTER TABLE tob_su_kien ADD COLUMN IF NOT EXISTS tu_dong TINYINT NOT NULL DEFAULT 1;

-- them_lich_su_kien_nam.sql (chi lenh nay)
UPDATE tob_su_kien SET mo_luc = NULL, dong_luc = NULL
 WHERE tu_dong = 1 AND (sua_boi IS NULL OR sua_boi = 'lich hen')
   AND ma IN ('tetduonglich','tet2017','tet123','moi:valentine','quocte83','gioto','moi:3004','moi:thieunhi','he2017',
              'moi:hamngucmua','vulan','moi:quockhanh','trungthu','moi:2010','halloween','demhalloween','sinhnhat2011',
              'blackfriday','blackfridaydaugia','blackfridayban','noel','demnoel','moi:tongket','moi:tuanhe',
              'moi:congdong','moi:shiny','moi:dotkich','moi:muapvp','moi:pokedex','moi:chienvung','moi:bungphat');

-- them_lich_su_kien_nam.sql (chi lenh nay)
DELETE FROM tob_su_kien WHERE ma IN ('worldcup','worldcupnhanqua','worldcup2017','snngude','duatophoalu');

-- ======================================================================================================================
-- 5. Ten NGUOI CHOI THAY: ten NPC (data_npc) + ten ban do (data_map_ten) - P6, CSDL la nguon (sua o web /admin/ten-hien).
--    Server bat may doc hai bang (com.htbb.pokemon.TenHien, thieu la bao loi to) va tu nap lai <= 30 giay.
--    Tao bang neu thieu, them dong thieu; dong da co CHI cap nhat cot ky thuat (pokemon, dex, id_anh, ban_do, vung, loai,
--    mo_ta) - GIU ten / chuc_vu admin da sua. (tung la pokemon_ten_hien.sql)
-- ======================================================================================================================
CREATE TABLE IF NOT EXISTS `data_npc` (
  `type` int NOT NULL,
  `ten` varchar(64) NOT NULL,
  `chuc_vu` varchar(64) NOT NULL DEFAULT '',
  `pokemon` varchar(32) NOT NULL DEFAULT '',
  `dex` int NOT NULL DEFAULT 0,
  `id_anh` int NOT NULL DEFAULT 0,
  `ban_do` varchar(255) NOT NULL DEFAULT '',
  `sua_boi` varchar(50) NOT NULL DEFAULT '',
  `sua_luc` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`type`)
) DEFAULT CHARSET=utf8;
INSERT INTO `data_npc` (`type`,`ten`,`chuc_vu`,`pokemon`,`dex`,`id_anh`,`ban_do`) VALUES
(0,'Y tá Chansey','Trung tâm Pokémon: hồi phục, bán thuốc','Chansey',113,0,'43,45,47,49,51,54,56,59,60,62,100,281'),
(1,'Kecleon tạp hoá','Tạp hoá','Kecleon',352,1,'0,45,47,49,54,56,62,70,72,80,82,102'),
(2,'Machamp thợ rèn','Thợ rèn: chế tạo, sửa, khảm đồ','Machamp',68,2,'0,45,70,73,80,83,103'),
(3,'Bronzong thợ giáp','Bán trang bị','Bronzong',437,3,'0,47,55,59,70,74,80,84,104'),
(4,'Arcanine gác cổng','Gác cổng','Arcanine',59,4,'0,70,80,201,281'),
(5,'Trưởng làng Xatu','Trưởng làng, giao nhiệm vụ','Xatu',178,5,'0,43,45,50,56,59,60,70,80,101,163,164,280,282'),
(6,'Ngài Persian','Bán đá quý, nguyên liệu','Persian',53,6,'0,53,70,79,80,89,109'),
(7,'Pidgeot chở bay','Chở bay tới bản đồ khác','Pidgeot',18,7,'0,3,4,5,6,7,8,9,54,70,80'),
(8,'Ông Snorlax','Giao nhiệm vụ','Snorlax',143,8,'51,57,60,105,160'),
(9,'Kangaskhan thủ kho','Giữ đồ','Kangaskhan',115,9,'0,70,78,80,88,108'),
(10,'Lapras chở đò','Chở đò, bán vé tàu','Lapras',131,10,'43,58,60,106,162'),
(11,'Golem bán hàng 1','Bán hàng','Golem',76,11,'99,201,266'),
(12,'Golem bán hàng 2','Bán hàng','Golem',76,11,'98,201,267'),
(13,'Golem bán hàng 3','Bán hàng','Golem',76,11,'97,201,268'),
(14,'Golem bán hàng 4','Bán hàng','Golem',76,11,'96,201,269'),
(15,'Golem bán hàng 5','Bán hàng','Golem',76,11,'95,201,270'),
(16,'Tauros bán hàng 1','Bán hàng','Tauros',128,12,'94,201,271'),
(17,'Tauros bán hàng 2','Bán hàng','Tauros',128,12,'93'),
(18,'Tauros bán hàng 3','Bán hàng','Tauros',128,12,'92'),
(19,'Tauros bán hàng 4','Bán hàng','Tauros',128,12,'91'),
(20,'Tauros bán hàng 5','Bán hàng','Tauros',128,12,'90'),
(21,'Huấn luyện viên Blaziken','Huấn luyện viên','Blaziken',257,14,'0,44,50,51,52,62,70,80,163,282'),
(22,'Meowth thương nhân','Ngân hàng xu, lượng','Meowth',52,13,'0,47,49,56,62,70,80'),
(23,'Lão Torkoal','Giao nhiệm vụ','Torkoal',324,15,'4,51'),
(24,'Sableye buôn đá quý','Bán đá quý','Sableye',302,16,'201,265'),
(25,'Chỉ huy Heracross','Nhiệm vụ đội, Chiếm Gym','Heracross',214,17,'201'),
(26,'Kecleon bán lẻ','Tạp hoá','Kecleon',352,1,'201,263'),
(27,'Bronzong bán giáp','Bán trang bị','Bronzong',437,3,'201,260'),
(28,'Farfetch''d thợ cả','Bán vật phẩm, thợ rèn','Farfetch''d',83,2,'201,261'),
(29,'Persian quản gia','Bán đá quý, nguyên liệu','Persian',53,6,'201,264'),
(30,'Kangaskhan quản kho','Giữ đồ','Kangaskhan',115,9,'201,262'),
(31,'Meowth đổi xu','Ngân hàng xu, lượng','Meowth',52,13,'')
ON DUPLICATE KEY UPDATE `pokemon`=VALUES(`pokemon`), `dex`=VALUES(`dex`), `id_anh`=VALUES(`id_anh`), `ban_do`=VALUES(`ban_do`);

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
(0,'Pewter','Kanto','thi_tran','Thị trấn'),
(1,'Pallet','Kanto','dong_co','Đồng cỏ'),
(2,'Viridian','Kanto','bien','Biển'),
(3,'Vermilion','Kanto','ho','Hồ'),
(4,'Cerulean','Kanto','rung','Rừng'),
(5,'Lavender','Kanto','dong_co','Đồng cỏ'),
(6,'Celadon','Kanto','rung','Rừng'),
(7,'Saffron','Kanto','ho','Hồ'),
(8,'Fuchsia','Kanto','bien','Biển'),
(9,'Cinnabar','Kanto','dong_co','Đồng cỏ'),
(10,'New Bark','Johto','rung','Rừng'),
(11,'Rừng Viridian','Kanto','rung','Rừng'),
(12,'Khu mua bán','','thi_tran','Thị trấn'),
(13,'Núi Mặt Trăng','Kanto','nui','Núi'),
(14,'Vùng Safari','Kanto','dong_co','Đồng cỏ'),
(15,'Cherrygrove','Johto','ho','Hồ'),
(17,'Khu mỏ','','sa_mac','Sa mạc'),
(18,'Goldenrod','Johto','duong','Tuyến đường'),
(19,'Ecruteak','Johto','bien','Biển'),
(20,'Violet','Johto','rung','Rừng'),
(21,'Đồng cỏ phía tây','','dong_co','Đồng cỏ'),
(22,'Tàu S.S. Anne','Kanto','ho','Hồ'),
(23,'Azalea','Johto','duong','Tuyến đường'),
(24,'Olivine','Johto','nui','Núi'),
(25,'Tháp luyện tập','','dau_truong','Đấu trường'),
(26,'Rừng Cổ','','rung','Rừng'),
(27,'Chiến trường','','dau_truong','Đấu trường'),
(28,'Mahogany','Johto','dong_co','Đồng cỏ'),
(29,'Rừng Ilex','Johto','rung','Rừng'),
(30,'Sân Liên minh 1','Johto','rung','Rừng'),
(31,'Sân Liên minh 2','Johto','rung','Rừng'),
(32,'Sân Liên minh 3','','dau_truong','Đấu trường'),
(33,'Sân Liên minh 4','','dau_truong','Đấu trường'),
(34,'Sân Liên minh 5','','dau_truong','Đấu trường'),
(35,'Sân Liên minh 6','','dau_truong','Đấu trường'),
(36,'Sân Liên minh 7','','dau_truong','Đấu trường'),
(37,'Sân Liên minh 8','','dau_truong','Đấu trường'),
(38,'Sân Liên minh 9','','dau_truong','Đấu trường'),
(39,'Sân Liên minh 10','','dau_truong','Đấu trường'),
(40,'Chiến trường','','dong_co','Đồng cỏ'),
(41,'Chân núi Silver','','tuyet','Vùng tuyết'),
(42,'Núi Silver','','tuyet','Vùng tuyết'),
(43,'Littleroot','Hoenn','thi_tran','Thị trấn'),
(44,'Rừng Petalburg','Hoenn','rung','Rừng'),
(45,'Rustboro','Hoenn','thi_tran','Thị trấn'),
(46,'Bờ biển Dewford','Hoenn','bien','Biển'),
(47,'Slateport','Hoenn','thi_tran','Thị trấn'),
(48,'Route 110','Hoenn','duong','Tuyến đường'),
(49,'Mauville','Hoenn','thi_tran','Thị trấn'),
(50,'Sa mạc Route 111','Hoenn','sa_mac','Sa mạc'),
(51,'Lavaridge','Hoenn','thi_tran','Thị trấn'),
(52,'Núi Chimney','Hoenn','nui','Núi'),
(53,'Rừng mưa Route 119','Hoenn','rung','Rừng'),
(54,'Fortree','Hoenn','thi_tran','Thị trấn'),
(55,'Route 120 - Viện Thời tiết','Hoenn','dong_co','Đồng cỏ'),
(56,'Lilycove','Hoenn','thi_tran','Thị trấn'),
(57,'Núi Pyre','Hoenn','nui','Núi'),
(58,'Biển Route 124','Hoenn','bien','Biển'),
(59,'Mossdeep','Hoenn','thi_tran','Thị trấn'),
(60,'Sootopolis','Hoenn','thi_tran','Thị trấn'),
(61,'Biển Route 131','Hoenn','bien','Biển'),
(62,'Ever Grande','Hoenn','thi_tran','Thị trấn'),
(70,'Blackthorn','Johto','thi_tran','Thị trấn'),
(72,'Nhà (Blackthorn)','Johto','trong_nha','Trong nhà'),
(73,'Trung tâm Pokémon','Johto','trong_nha','Trong nhà'),
(74,'Poké Mart','Johto','trong_nha','Trong nhà'),
(78,'Nhà kho','Johto','trong_nha','Trong nhà'),
(79,'Xưởng chế tạo','Johto','trong_nha','Trong nhà'),
(80,'Cianwood','Johto','thi_tran','Thị trấn'),
(82,'Nhà (Cianwood)','Johto','trong_nha','Trong nhà'),
(83,'Trung tâm Pokémon','Johto','trong_nha','Trong nhà'),
(84,'Poké Mart','Johto','trong_nha','Trong nhà'),
(88,'Nhà kho','Johto','trong_nha','Trong nhà'),
(89,'Xưởng chế tạo','Johto','trong_nha','Trong nhà'),
(90,'Cửa hàng 1','','trong_nha','Trong nhà'),
(91,'Cửa hàng 2','','trong_nha','Trong nhà'),
(92,'Cửa hàng 3','','trong_nha','Trong nhà'),
(93,'Cửa hàng 4','','trong_nha','Trong nhà'),
(94,'Cửa hàng 5','','trong_nha','Trong nhà'),
(95,'Cửa hàng 6','','trong_nha','Trong nhà'),
(96,'Cửa hàng 7','','trong_nha','Trong nhà'),
(97,'Cửa hàng 8','','trong_nha','Trong nhà'),
(98,'Cửa hàng 9','','trong_nha','Trong nhà'),
(99,'Cửa hàng 10','','trong_nha','Trong nhà'),
(100,'Nhà (Pewter)','Kanto','trong_nha','Trong nhà'),
(101,'Khoảng rừng nhỏ','','rung','Rừng'),
(102,'Nhà (Pewter) 2','Kanto','trong_nha','Trong nhà'),
(103,'Trung tâm Pokémon','Kanto','trong_nha','Trong nhà'),
(104,'Poké Mart','Kanto','trong_nha','Trong nhà'),
(105,'Sân nhà khởi đầu','','dong_co','Đồng cỏ'),
(106,'Bến tàu','','thi_tran','Thị trấn'),
(107,'Thuyền','','tau','Tàu'),
(108,'Nhà kho','','trong_nha','Trong nhà'),
(109,'Xưởng chế tạo','','trong_nha','Trong nhà'),
(110,'Hang động','','hang','Hang động'),
(111,'Hang Darkrai','','hang','Hang động'),
(112,'Hang Salamence','','hang','Hang động'),
(113,'Hang Tối','Johto','hang','Hang động'),
(114,'Hồ Phẫn Nộ','Johto','hang','Hang động'),
(115,'Hang nhỏ','','hang','Hang động'),
(116,'Hang cổ','','hang','Hang động'),
(117,'Ngục tối','','hang','Hang động'),
(118,'Đường Victory','','hang','Hang động'),
(122,'Hang trống','','hang','Hang động'),
(160,'Hang Granite','Hoenn','hang','Hang động'),
(161,'Thác Meteor','Hoenn','hang','Hang động'),
(162,'Hang dưới biển','Hoenn','hang','Hang động'),
(163,'Hang Khởi Nguyên','Hoenn','hang','Hang động'),
(164,'Tháp Trời','Hoenn','hang','Hang động'),
(165,'Đường Victory Hoenn','Hoenn','hang','Hang động'),
(201,'Cao nguyên Indigo','Kanto','thanh','Lãnh địa'),
(202,'Đấu trường','','dau_truong','Đấu trường'),
(203,'Đấu trường La Mã','','dau_truong','Đấu trường'),
(204,'Đấu trường 2','','dau_truong','Đấu trường'),
(205,'Đấu trường 3','','dau_truong','Đấu trường'),
(206,'Lãnh địa đội','','thanh','Lãnh địa'),
(207,'Lãnh địa đội 2','','thanh','Lãnh địa'),
(208,'Tháp luyện tập 5x','','dau_truong','Đấu trường'),
(209,'Tháp luyện tập 6x','','dau_truong','Đấu trường'),
(210,'Tháp luyện tập 7x','','dau_truong','Đấu trường'),
(225,'Lễ cưới 1','','dau_truong','Đấu trường'),
(226,'Lễ cưới 2','','dau_truong','Đấu trường'),
(227,'Sàn đấu','','dau_truong','Đấu trường'),
(228,'Sảnh chờ','','dau_truong','Đấu trường'),
(260,'Nhà (Indigo) 1','Kanto','trong_nha','Trong nhà'),
(261,'Nhà (Indigo) 2','Kanto','trong_nha','Trong nhà'),
(262,'Nhà (Indigo) 3','Kanto','trong_nha','Trong nhà'),
(263,'Nhà (Indigo) 4','Kanto','trong_nha','Trong nhà'),
(264,'Nhà (Indigo) 5','Kanto','trong_nha','Trong nhà'),
(265,'Nhà (Indigo) 6','Kanto','trong_nha','Trong nhà'),
(266,'Nhà (Indigo) 7','Kanto','trong_nha','Trong nhà'),
(267,'Nhà (Indigo) 8','Kanto','trong_nha','Trong nhà'),
(268,'Nhà (Indigo) 9','Kanto','trong_nha','Trong nhà'),
(269,'Nhà (Indigo) 10','Kanto','trong_nha','Trong nhà'),
(270,'Nhà (Indigo) 11','Kanto','trong_nha','Trong nhà'),
(271,'Nhà (Indigo) 12','Kanto','trong_nha','Trong nhà'),
(280,'Đỉnh Tháp Trời','Hoenn','dau_truong','Đấu trường'),
(281,'Liên minh Hoenn','Hoenn','thanh','Lãnh địa'),
(282,'Sảnh Nhà vô địch','Hoenn','dau_truong','Đấu trường'),
(2541,'Tổ Zapdos','','hang','Hang động')
ON DUPLICATE KEY UPDATE `vung`=VALUES(`vung`), `loai`=VALUES(`loai`), `mo_ta`=VALUES(`mo_ta`);

-- ======================================================================================================================
-- 6. 0.9.6 (main da89d155): chuc nang NPC dung + ten chieu kieu Pokemon. (tung la pokemon_npc_chuc_nang.sql,
--    sua_ten_chieu_096.sql). Chi doi dong con gia tri mac dinh cu - GIU chuc_vu admin da sua o /admin/ten-hien.
-- ======================================================================================================================
UPDATE `data_npc` SET `ban_do` = '201', `sua_luc` = NOW() WHERE `type` = 25 AND `ban_do` = '';
UPDATE `data_npc` SET `chuc_vu` = 'Nhiệm vụ đội, Chiếm Gym', `sua_luc` = NOW()
 WHERE `type` = 25 AND `chuc_vu` = 'Chỉ huy giải Liên minh';
UPDATE `data_npc` SET `chuc_vu` = 'Trung tâm Pokémon: hồi phục, bán thuốc', `sua_luc` = NOW()
 WHERE `type` = 0 AND `chuc_vu` = 'Bán thuốc hồi phục';
UPDATE `data_npc` SET `chuc_vu` = 'Bán đá quý, nguyên liệu', `sua_luc` = NOW()
 WHERE `type` IN (6, 29) AND `chuc_vu` = 'Giữ đồ';
UPDATE `data_npc` SET `chuc_vu` = 'Ngân hàng xu, lượng', `sua_luc` = NOW()
 WHERE `type` IN (22, 31) AND `chuc_vu` = 'Nạp xu';
UPDATE `data_noi_dung_chieu` SET `ten` = 'Vũ điệu kiếm' WHERE `ten` = 'Múa kiếm';
UPDATE `data_noi_dung_chieu` SET `trang_thai` = REPLACE(`trang_thai`, 'Múa kiếm', 'Vũ điệu kiếm')
 WHERE `trang_thai` LIKE '%Múa kiếm%';

-- ======================================================================================================================
-- 7. KHOA BAN DO theo cap + cua ai (kieu NRO / NSO) va Tu dong danh theo ban do (nhanh khoa-map; thiet ke: sql/README.md).
--    data_ban_do_mo_khoa: mot dong / ban do goc (Map.mapIDLoadMap) - cap_toi_thieu, ma_nhiem_vu (da xong thi mo),
--    ten_nhiem_vu (chu bao), cho_phep_tu_dong + ly_do_tu_dong. Server com.htbb.pokemon.KhoaBanDo nap, tu nap lai <= 30 giay
--    theo (COUNT, MAX(sua_luc)). INSERT IGNORE: GIU dong admin da sua.
--    data_quest_lau_dai / data_nhiem_vu_noi_dung: 12 cua ai CA-01..CA-12 (29 buoc, loai cua_ai, engine NhiemVuEngine,
--    Sổ tay huấn luyện -> Thử thách cửa ải); thuong trong ngan sach cua_ai {250, 100, 2, 0} (ThongSoLauDai).
--    tob_ban_do_mo: moc mien tru nhan vat cu (ban do co cap_toi_thieu <= moc_mien mo san) - INSERT IGNORE theo cap /
--    ban do da luu, KHONG sua dong co san; server cung tu dien lan dau dang nhap (real.KhoaBanDoGame.khiNap).
-- ======================================================================================================================
CREATE TABLE IF NOT EXISTS `data_ban_do_mo_khoa` (
  `map_id` int NOT NULL,
  `cap_toi_thieu` int NOT NULL DEFAULT 0,
  `ma_nhiem_vu` varchar(32) NOT NULL DEFAULT '',
  `ten_nhiem_vu` varchar(96) NOT NULL DEFAULT '',
  `cho_phep_tu_dong` tinyint NOT NULL DEFAULT 1,
  `ly_do_tu_dong` varchar(128) NOT NULL DEFAULT '',
  `ghi_chu` varchar(128) NOT NULL DEFAULT '',
  `sua_boi` varchar(50) NOT NULL DEFAULT '',
  `sua_luc` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`map_id`)
) DEFAULT CHARSET=utf8;
INSERT IGNORE INTO `data_ban_do_mo_khoa` (`map_id`,`cap_toi_thieu`,`ma_nhiem_vu`,`ten_nhiem_vu`,`cho_phep_tu_dong`,`ly_do_tu_dong`,`ghi_chu`) VALUES
(0,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Pewter'),
(1,0,'','',1,'','Pallet'),
(2,0,'','',1,'','Viridian'),
(3,4,'CA-01.2','Cửa ải: Rời đồng cỏ Pallet',1,'','Vermilion'),
(4,3,'CA-01.2','Cửa ải: Rời đồng cỏ Pallet',1,'','Cerulean'),
(5,8,'CA-02.2','Cửa ải: Vượt hồ Vermilion',1,'','Lavender'),
(6,12,'CA-03.2','Cửa ải: Thử thách Pewter',1,'','Celadon'),
(7,12,'CA-03.2','Cửa ải: Thử thách Pewter',1,'','Saffron'),
(8,12,'CA-03.2','Cửa ải: Thử thách Pewter',1,'','Fuchsia'),
(9,19,'CA-04.2','Cửa ải: Vượt bờ biển Fuchsia',1,'','Cinnabar'),
(10,27,'CA-05.3','Cửa ải: Đường sang Johto',1,'','New Bark'),
(11,30,'CA-05.3','Cửa ải: Đường sang Johto',1,'','Rừng Viridian'),
(12,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Khu mua bán'),
(13,35,'CA-06.2','Cửa ải: Rừng Viridian sâu thẳm',1,'','Núi Mặt Trăng'),
(14,37,'CA-06.2','Cửa ải: Rừng Viridian sâu thẳm',1,'','Vùng Safari'),
(15,40,'CA-07.2','Cửa ải: Leo Núi Mặt Trăng',1,'','Cherrygrove'),
(17,20,'','',0,'Khu sự kiện / phó bản: không dùng Tự động đánh.','Khu mỏ'),
(18,49,'CA-08.3','Cửa ải: Bóng tối Cherrygrove',1,'','Goldenrod'),
(19,53,'CA-09.2','Cửa ải: Phố lớn Goldenrod',1,'','Ecruteak'),
(20,57,'CA-09.2','Cửa ải: Phố lớn Goldenrod',1,'','Violet'),
(21,61,'CA-10.3','Cửa ải: Rừng Violet',1,'','Đồng cỏ phía tây'),
(22,65,'CA-10.3','Cửa ải: Rừng Violet',1,'','Tàu S.S. Anne'),
(23,69,'CA-11.3','Cửa ải: Chuyến tàu S.S. Anne',1,'','Azalea'),
(24,73,'CA-11.3','Cửa ải: Chuyến tàu S.S. Anne',1,'','Olivine'),
(25,0,'','',0,'Hang boss: boss phải đánh tay.','Tháp luyện tập'),
(26,77,'CA-12.3','Cửa ải: Núi Olivine',1,'','Rừng Cổ'),
(27,0,'','',0,'Đấu trường / PvP: phải tự điều khiển.','Chiến trường'),
(28,79,'CA-12.3','Cửa ải: Núi Olivine',1,'','Mahogany'),
(29,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Rừng Ilex'),
(30,0,'','',0,'Đấu trường / PvP: phải tự điều khiển.','Sân Liên minh 1'),
(31,0,'','',0,'Đấu trường / PvP: phải tự điều khiển.','Sân Liên minh 2'),
(32,0,'','',0,'Đấu trường / PvP: phải tự điều khiển.','Sân Liên minh 3'),
(33,0,'','',0,'Đấu trường / PvP: phải tự điều khiển.','Sân Liên minh 4'),
(34,0,'','',0,'Đấu trường / PvP: phải tự điều khiển.','Sân Liên minh 5'),
(35,0,'','',0,'Đấu trường / PvP: phải tự điều khiển.','Sân Liên minh 6'),
(36,0,'','',0,'Đấu trường / PvP: phải tự điều khiển.','Sân Liên minh 7'),
(37,0,'','',0,'Đấu trường / PvP: phải tự điều khiển.','Sân Liên minh 8'),
(38,0,'','',0,'Đấu trường / PvP: phải tự điều khiển.','Sân Liên minh 9'),
(39,0,'','',0,'Đấu trường / PvP: phải tự điều khiển.','Sân Liên minh 10'),
(40,0,'','',0,'Khu sự kiện / phó bản: không dùng Tự động đánh.','Chiến trường'),
(41,0,'','',0,'Khu sự kiện / phó bản: không dùng Tự động đánh.','Chân núi Silver'),
(42,0,'','',0,'Khu sự kiện / phó bản: không dùng Tự động đánh.','Núi Silver'),
(43,90,'C52.7','Chính tuyến: Vùng đất phía nam',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Littleroot'),
(44,90,'C52.7','Chính tuyến: Vùng đất phía nam',1,'','Rừng Petalburg'),
(45,90,'C52.7','Chính tuyến: Vùng đất phía nam',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Rustboro'),
(46,90,'C52.7','Chính tuyến: Vùng đất phía nam',1,'','Bờ biển Dewford'),
(47,90,'C52.7','Chính tuyến: Vùng đất phía nam',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Slateport'),
(48,90,'C52.7','Chính tuyến: Vùng đất phía nam',1,'','Route 110'),
(49,90,'C52.7','Chính tuyến: Vùng đất phía nam',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Mauville'),
(50,90,'C52.7','Chính tuyến: Vùng đất phía nam',1,'','Sa mạc Route 111'),
(51,90,'C52.7','Chính tuyến: Vùng đất phía nam',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Lavaridge'),
(52,90,'C52.7','Chính tuyến: Vùng đất phía nam',1,'','Núi Chimney'),
(53,90,'C52.7','Chính tuyến: Vùng đất phía nam',1,'','Rừng mưa Route 119'),
(54,90,'C52.7','Chính tuyến: Vùng đất phía nam',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Fortree'),
(55,90,'C52.7','Chính tuyến: Vùng đất phía nam',1,'','Route 120 - Viện Thời tiết'),
(56,90,'C52.7','Chính tuyến: Vùng đất phía nam',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Lilycove'),
(57,90,'C52.7','Chính tuyến: Vùng đất phía nam',1,'','Núi Pyre'),
(58,90,'C52.7','Chính tuyến: Vùng đất phía nam',1,'','Biển Route 124'),
(59,90,'C52.7','Chính tuyến: Vùng đất phía nam',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Mossdeep'),
(60,90,'C52.7','Chính tuyến: Vùng đất phía nam',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Sootopolis'),
(61,91,'C52.7','Chính tuyến: Vùng đất phía nam',1,'','Biển Route 131'),
(62,90,'C52.7','Chính tuyến: Vùng đất phía nam',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Ever Grande'),
(70,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Blackthorn'),
(72,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Nhà (Blackthorn)'),
(73,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Trung tâm Pokémon'),
(74,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Poké Mart'),
(78,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Nhà kho'),
(79,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Xưởng chế tạo'),
(80,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Cianwood'),
(82,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Nhà (Cianwood)'),
(83,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Trung tâm Pokémon'),
(84,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Poké Mart'),
(88,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Nhà kho'),
(89,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Xưởng chế tạo'),
(90,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Cửa hàng 1'),
(91,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Cửa hàng 2'),
(92,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Cửa hàng 3'),
(93,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Cửa hàng 4'),
(94,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Cửa hàng 5'),
(95,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Cửa hàng 6'),
(96,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Cửa hàng 7'),
(97,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Cửa hàng 8'),
(98,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Cửa hàng 9'),
(99,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Cửa hàng 10'),
(100,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Nhà (Pewter)'),
(101,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Khoảng rừng nhỏ'),
(102,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Nhà (Pewter) 2'),
(103,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Trung tâm Pokémon'),
(104,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Poké Mart'),
(105,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Sân nhà khởi đầu'),
(106,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Bến tàu'),
(107,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Thuyền'),
(108,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Nhà kho'),
(109,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Xưởng chế tạo'),
(110,19,'CA-04.2','Cửa ải: Vượt bờ biển Fuchsia',1,'','Hang động'),
(111,0,'','',0,'Hang boss: boss phải đánh tay.','Hang Darkrai'),
(112,0,'','',0,'Hang boss: boss phải đánh tay.','Hang Salamence'),
(113,43,'CA-07.2','Cửa ải: Leo Núi Mặt Trăng',1,'','Hang Tối'),
(114,46,'CA-08.3','Cửa ải: Bóng tối Cherrygrove',1,'','Hồ Phẫn Nộ'),
(115,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Hang nhỏ'),
(116,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Hang cổ'),
(117,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Ngục tối'),
(118,69,'CA-11.3','Cửa ải: Chuyến tàu S.S. Anne',0,'Khu sự kiện / phó bản: không dùng Tự động đánh.','Đường Victory'),
(122,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Hang trống'),
(160,90,'C52.7','Chính tuyến: Vùng đất phía nam',1,'','Hang Granite'),
(161,90,'C52.7','Chính tuyến: Vùng đất phía nam',1,'','Thác Meteor'),
(162,90,'C52.7','Chính tuyến: Vùng đất phía nam',1,'','Hang dưới biển'),
(163,92,'C52.7','Chính tuyến: Vùng đất phía nam',1,'','Hang Khởi Nguyên'),
(164,93,'C52.7','Chính tuyến: Vùng đất phía nam',1,'','Tháp Trời'),
(165,94,'C52.7','Chính tuyến: Vùng đất phía nam',1,'','Đường Victory Hoenn'),
(201,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Cao nguyên Indigo'),
(202,0,'','',0,'Đấu trường / PvP: phải tự điều khiển.','Đấu trường'),
(203,0,'','',0,'Đấu trường / PvP: phải tự điều khiển.','Đấu trường La Mã'),
(204,0,'','',0,'Đấu trường / PvP: phải tự điều khiển.','Đấu trường 2'),
(205,0,'','',0,'Đấu trường / PvP: phải tự điều khiển.','Đấu trường 3'),
(206,0,'','',0,'Đấu trường / PvP: phải tự điều khiển.','Lãnh địa đội'),
(207,0,'','',0,'Đấu trường / PvP: phải tự điều khiển.','Lãnh địa đội 2'),
(208,0,'','',0,'Khu sự kiện / phó bản: không dùng Tự động đánh.','Tháp luyện tập 5x'),
(209,0,'','',0,'Khu sự kiện / phó bản: không dùng Tự động đánh.','Tháp luyện tập 6x'),
(210,0,'','',0,'Khu sự kiện / phó bản: không dùng Tự động đánh.','Tháp luyện tập 7x'),
(225,0,'','',0,'Lễ cưới: không có Pokémon hoang dã để tự đánh.','Lễ cưới 1'),
(226,0,'','',0,'Lễ cưới: không có Pokémon hoang dã để tự đánh.','Lễ cưới 2'),
(227,0,'','',0,'Đấu trường / PvP: phải tự điều khiển.','Sàn đấu'),
(228,0,'','',0,'Đấu trường / PvP: phải tự điều khiển.','Sảnh chờ'),
(260,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Nhà (Indigo) 1'),
(261,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Nhà (Indigo) 2'),
(262,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Nhà (Indigo) 3'),
(263,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Nhà (Indigo) 4'),
(264,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Nhà (Indigo) 5'),
(265,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Nhà (Indigo) 6'),
(266,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Nhà (Indigo) 7'),
(267,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Nhà (Indigo) 8'),
(268,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Nhà (Indigo) 9'),
(269,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Nhà (Indigo) 10'),
(270,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Nhà (Indigo) 11'),
(271,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Nhà (Indigo) 12'),
(280,96,'C52.7','Chính tuyến: Vùng đất phía nam',0,'Hang boss: boss phải đánh tay.','Đỉnh Tháp Trời'),
(281,90,'C52.7','Chính tuyến: Vùng đất phía nam',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Liên minh Hoenn'),
(282,90,'C52.7','Chính tuyến: Vùng đất phía nam',0,'Đấu trường / PvP: phải tự điều khiển.','Sảnh Nhà vô địch'),
(2541,0,'','',0,'Hang boss: boss phải đánh tay.','Tổ Zapdos');

INSERT INTO `data_quest_lau_dai` (`ma`,`id_so`,`loai`,`nam`,`vung`,`cung`,`chuong`,`buoc`,`ten`,`npc_giao`,`ten_npc`,`dia_danh`,`cap_toi_thieu`,`ngay_mo`,`truoc`,`kieu_muc_tieu`,`muc_tieu_id`,`muc_tieu_ten`,`so_luong`,`quai_roi`,`lap_lai`,`gioi_han`,`exp`,`xu`,`luong_khoa`,`luong`,`vat_pham`,`huy_hieu`,`dong_co_tuong_thich`,`ghi_chu`) VALUES
('CA-01.1',7001,'cua_ai',1,'','Rời đồng cỏ Pallet','CA-01','1','Cửa ải: 20 Torchic ở Pallet','','','Pallet',3,0,'','giet','4','Torchic',20,'','','',3161,384,0,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-01.2',7002,'cua_ai',1,'','Rời đồng cỏ Pallet','CA-01','2','Cửa ải: 20 Ekans ở Viridian','','','Viridian',3,0,'CA-01.1','giet','5','Ekans',20,'','','',3161,384,1,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-02.1',7003,'cua_ai',1,'','Vượt hồ Vermilion','CA-02','1','Cửa ải: 25 Oddish ở Vermilion','','','Vermilion',8,0,'','giet','10','Oddish',25,'','','',13776,472,0,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-02.2',7004,'cua_ai',1,'','Vượt hồ Vermilion','CA-02','2','Cửa ải: 25 Venonat ở Cerulean','','','Cerulean',8,0,'CA-02.1','giet','8','Venonat',25,'','','',13776,472,1,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-03.1',7005,'cua_ai',1,'','Thử thách Pewter','CA-03','1','Cửa ải: 30 Quagsire ở Lavender','','','Lavender',10,0,'','giet','13','Quagsire',30,'','','',24984,499,0,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-03.2',7006,'cua_ai',1,'','Thử thách Pewter','CA-03','2','Cửa ải: hạ Gym Pewter','','','',12,0,'CA-03.1','ha_gym','1','Trưởng Gym Pewter: Golem',1,'','','',30877,557,1,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-04.1',7007,'cua_ai',1,'','Vượt bờ biển Fuchsia','CA-04','1','Cửa ải: 40 Growlithe ở Fuchsia','','','Fuchsia',17,0,'','giet','28','Growlithe',40,'','','',52484,699,0,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-04.2',7008,'cua_ai',1,'','Vượt bờ biển Fuchsia','CA-04','2','Cửa ải: hạ Gym Cerulean','','','',19,0,'CA-04.1','ha_gym','2','Trưởng Gym Cerulean: Gyarados',1,'','','',64802,753,1,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-05.1',7009,'cua_ai',1,'','Đường sang Johto','CA-05','1','Cửa ải: 45 Croagunk ở Cinnabar','','','Cinnabar',24,0,'','giet','30','Croagunk',45,'','','',97831,947,0,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-05.2',7010,'cua_ai',1,'','Đường sang Johto','CA-05','2','Cửa ải: 30 Gastly ở Hang động','','','Hang động',24,0,'CA-05.1','giet','25','Gastly',30,'','','',97831,947,0,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-05.3',7011,'cua_ai',1,'','Đường sang Johto','CA-05','3','Cửa ải: hạ Gym Vermilion','','','',24,0,'CA-05.2','ha_gym','3','Trưởng Gym Vermilion: Luxray',1,'','','',97831,947,1,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-06.1',7012,'cua_ai',1,'','Rừng Viridian sâu thẳm','CA-06','1','Cửa ải: 50 Crawdaunt ở Rừng Viridian','','','Rừng Viridian',33,0,'','giet','35','Crawdaunt',50,'','','',188754,1426,0,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-06.2',7013,'cua_ai',1,'','Rừng Viridian sâu thẳm','CA-06','2','Cửa ải: hạ Gym Celadon','','','',35,0,'CA-06.1','ha_gym','4','Trưởng Gym Celadon: Vileplume',1,'','','',215831,1554,1,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-07.1',7014,'cua_ai',1,'','Leo Núi Mặt Trăng','CA-07','1','Cửa ải: 50 Farfetch''d ở Núi Mặt Trăng','','','Núi Mặt Trăng',37,0,'','giet','45','Farfetch''d',50,'','','',247553,1712,0,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-07.2',7015,'cua_ai',1,'','Leo Núi Mặt Trăng','CA-07','2','Cửa ải: hạ Gym Saffron','','','',38,0,'CA-07.1','ha_gym','5','Trưởng Gym Saffron: Seismitoad',1,'','','',265121,1791,1,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-08.1',7016,'cua_ai',1,'','Bóng tối Cherrygrove','CA-08','1','Cửa ải: 55 Dusclops ở Cherrygrove','','','Cherrygrove',43,0,'','giet','57','Dusclops',55,'','','',334302,2216,0,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-08.2',7017,'cua_ai',1,'','Bóng tối Cherrygrove','CA-08','2','Cửa ải: hạ Gym Fuchsia','','','',43,0,'CA-08.1','ha_gym','6','Trưởng Gym Fuchsia: Gengar',1,'','','',334302,2216,0,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-08.3',7018,'cua_ai',1,'','Bóng tối Cherrygrove','CA-08','3','Cửa ải: hạ Gym Cinnabar','','','',46,0,'CA-08.2','ha_gym','7','Trưởng Gym Cinnabar: Magcargo',1,'','','',368712,2537,1,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-09.1',7019,'cua_ai',1,'','Phố lớn Goldenrod','CA-09','1','Cửa ải: 60 Golem ở Goldenrod','','','Goldenrod',49,0,'','giet','74','Golem',60,'','','',406632,2903,0,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-09.2',7020,'cua_ai',1,'','Phố lớn Goldenrod','CA-09','2','Cửa ải: hạ Gym Viridian','','','',49,0,'CA-09.1','ha_gym','8','Trưởng Gym Viridian: Rhydon',1,'','','',406632,2903,1,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-10.1',7021,'cua_ai',1,'','Rừng Violet','CA-10','1','Cửa ải: 65 Rhydon ở Violet','','','Violet',57,0,'','giet','60','Rhydon',65,'','','',545344,4144,0,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-10.2',7022,'cua_ai',1,'','Rừng Violet','CA-10','2','Cửa ải: hạ Gym Violet','','','',57,0,'CA-10.1','ha_gym','9','Trưởng Gym Violet: Dodrio',1,'','','',545344,4144,0,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-10.3',7023,'cua_ai',1,'','Rừng Violet','CA-10','3','Cửa ải: hạ Gym Azalea','','','',57,0,'CA-10.2','ha_gym','10','Trưởng Gym Azalea: Heracross',1,'','','',545344,4144,1,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-11.1',7024,'cua_ai',1,'','Chuyến tàu S.S. Anne','CA-11','1','Cửa ải: 70 Kingler ở Tàu S.S. Anne','','','Tàu S.S. Anne',65,0,'','giet','54','Kingler',70,'','','',743544,5911,0,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-11.2',7025,'cua_ai',1,'','Chuyến tàu S.S. Anne','CA-11','2','Cửa ải: hạ Gym Goldenrod','','','',65,0,'CA-11.1','ha_gym','11','Trưởng Gym Goldenrod: Tauros',1,'','','',743544,5911,0,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-11.3',7026,'cua_ai',1,'','Chuyến tàu S.S. Anne','CA-11','3','Cửa ải: hạ Gym Ecruteak','','','',65,0,'CA-11.2','ha_gym','12','Trưởng Gym Ecruteak: Dusknoir',1,'','','',743544,5911,1,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-12.1',7027,'cua_ai',1,'','Núi Olivine','CA-12','1','Cửa ải: 75 Vileplume ở Olivine','','','Olivine',73,0,'','giet','58','Vileplume',75,'','','',1046665,8395,0,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-12.2',7028,'cua_ai',1,'','Núi Olivine','CA-12','2','Cửa ải: hạ Gym Olivine','','','',73,0,'CA-12.1','ha_gym','13','Trưởng Gym Olivine: Aegislash',1,'','','',1046665,8395,0,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-12.3',7029,'cua_ai',1,'','Núi Olivine','CA-12','3','Cửa ải: hạ Gym Cianwood','','','',73,0,'CA-12.2','ha_gym','14','Trưởng Gym Cianwood: Crabrawler',1,'','','',1046665,8395,1,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)')
ON DUPLICATE KEY UPDATE `id_so`=VALUES(`id_so`), `loai`=VALUES(`loai`), `cung`=VALUES(`cung`), `ten`=VALUES(`ten`),
 `dia_danh`=VALUES(`dia_danh`), `cap_toi_thieu`=VALUES(`cap_toi_thieu`), `truoc`=VALUES(`truoc`),
 `kieu_muc_tieu`=VALUES(`kieu_muc_tieu`), `muc_tieu_id`=VALUES(`muc_tieu_id`), `muc_tieu_ten`=VALUES(`muc_tieu_ten`),
 `so_luong`=VALUES(`so_luong`), `exp`=VALUES(`exp`), `xu`=VALUES(`xu`), `luong_khoa`=VALUES(`luong_khoa`), `luong`=VALUES(`luong`);

INSERT INTO `data_nhiem_vu_noi_dung` (`ma`,`loai`,`nhom`,`nhan_vat`,`map_id`,`toa_do`,`dieu_kien`,`du_phong`,`mo_ta`,`muc_tieu_text`,`hoi_thoai_bat_dau`,`hoi_thoai_dang_lam`,`hoi_thoai_xong`,`goi_y`,`dong_thuong`,`thuong_khac`,`nhanh_lua_chon`,`an`,`dieu_kien_mo`,`dap_an`,`phut`) VALUES
('CA-01.1','cua_ai','cua_ai','','1','','ban_do=1','','Cửa ải Rời đồng cỏ Pallet','Hạ 20 Torchic ở Pallet.','Nhiệm vụ cửa ải «Rời đồng cỏ Pallet» (1/2): Hạ 20 Torchic ở Pallet.','{da}/20','Tốt lắm! Sang bước tiếp theo trong Sổ tay.','Đến đúng bản đồ trong mục tiêu rồi hạ quái (Tự động đánh tính được).','3.161 EXP, 384 xu','','',0,'','',0),
('CA-01.2','cua_ai','cua_ai','','2','','ban_do=2','','Cửa ải Rời đồng cỏ Pallet','Hạ 20 Ekans ở Viridian.','Nhiệm vụ cửa ải «Rời đồng cỏ Pallet» (2/2): Hạ 20 Ekans ở Viridian.','{da}/20','Tốt lắm! Cửa ải «Rời đồng cỏ Pallet» đã mở.','Đến đúng bản đồ trong mục tiêu rồi hạ quái (Tự động đánh tính được).','3.161 EXP, 384 xu, 1 lượng khoá; mở bản đồ mới','','',0,'','',0),
('CA-02.1','cua_ai','cua_ai','','3','','ban_do=3','','Cửa ải Vượt hồ Vermilion','Hạ 25 Oddish ở Vermilion.','Nhiệm vụ cửa ải «Vượt hồ Vermilion» (1/2): Hạ 25 Oddish ở Vermilion.','{da}/25','Tốt lắm! Sang bước tiếp theo trong Sổ tay.','Đến đúng bản đồ trong mục tiêu rồi hạ quái (Tự động đánh tính được).','13.776 EXP, 472 xu','','',0,'','',0),
('CA-02.2','cua_ai','cua_ai','','4','','ban_do=4','','Cửa ải Vượt hồ Vermilion','Hạ 25 Venonat ở Cerulean.','Nhiệm vụ cửa ải «Vượt hồ Vermilion» (2/2): Hạ 25 Venonat ở Cerulean.','{da}/25','Tốt lắm! Cửa ải «Vượt hồ Vermilion» đã mở.','Đến đúng bản đồ trong mục tiêu rồi hạ quái (Tự động đánh tính được).','13.776 EXP, 472 xu, 1 lượng khoá; mở bản đồ mới','','',0,'','',0),
('CA-03.1','cua_ai','cua_ai','','5','','ban_do=5','','Cửa ải Thử thách Pewter','Hạ 30 Quagsire ở Lavender.','Nhiệm vụ cửa ải «Thử thách Pewter» (1/2): Hạ 30 Quagsire ở Lavender.','{da}/30','Tốt lắm! Sang bước tiếp theo trong Sổ tay.','Đến đúng bản đồ trong mục tiêu rồi hạ quái (Tự động đánh tính được).','24.984 EXP, 499 xu','','',0,'','',0),
('CA-03.2','cua_ai','cua_ai','','','','','','Cửa ải Thử thách Pewter','Hạ Trưởng Gym Pewter: Golem (Sổ tay huấn luyện → Gym).','Nhiệm vụ cửa ải «Thử thách Pewter» (2/2): Hạ Trưởng Gym Pewter: Golem (Sổ tay huấn luyện → Gym).','{da}/1','Tốt lắm! Cửa ải «Thử thách Pewter» đã mở.','Mở Sổ tay huấn luyện → Gym để triệu hồi trưởng Gym rồi đánh tay (boss không dùng Tự động đánh).','30.877 EXP, 557 xu, 1 lượng khoá; mở bản đồ mới','','',0,'','',0),
('CA-04.1','cua_ai','cua_ai','','8','','ban_do=8','','Cửa ải Vượt bờ biển Fuchsia','Hạ 40 Growlithe ở Fuchsia.','Nhiệm vụ cửa ải «Vượt bờ biển Fuchsia» (1/2): Hạ 40 Growlithe ở Fuchsia.','{da}/40','Tốt lắm! Sang bước tiếp theo trong Sổ tay.','Đến đúng bản đồ trong mục tiêu rồi hạ quái (Tự động đánh tính được).','52.484 EXP, 699 xu','','',0,'','',0),
('CA-04.2','cua_ai','cua_ai','','','','','','Cửa ải Vượt bờ biển Fuchsia','Hạ Trưởng Gym Cerulean: Gyarados (Sổ tay huấn luyện → Gym).','Nhiệm vụ cửa ải «Vượt bờ biển Fuchsia» (2/2): Hạ Trưởng Gym Cerulean: Gyarados (Sổ tay huấn luyện → Gym).','{da}/1','Tốt lắm! Cửa ải «Vượt bờ biển Fuchsia» đã mở.','Mở Sổ tay huấn luyện → Gym để triệu hồi trưởng Gym rồi đánh tay (boss không dùng Tự động đánh).','64.802 EXP, 753 xu, 1 lượng khoá; mở bản đồ mới','','',0,'','',0),
('CA-05.1','cua_ai','cua_ai','','9','','ban_do=9','','Cửa ải Đường sang Johto','Hạ 45 Croagunk ở Cinnabar.','Nhiệm vụ cửa ải «Đường sang Johto» (1/3): Hạ 45 Croagunk ở Cinnabar.','{da}/45','Tốt lắm! Sang bước tiếp theo trong Sổ tay.','Đến đúng bản đồ trong mục tiêu rồi hạ quái (Tự động đánh tính được).','97.831 EXP, 947 xu','','',0,'','',0),
('CA-05.2','cua_ai','cua_ai','','110','','ban_do=110','','Cửa ải Đường sang Johto','Hạ 30 Gastly ở Hang động.','Nhiệm vụ cửa ải «Đường sang Johto» (2/3): Hạ 30 Gastly ở Hang động.','{da}/30','Tốt lắm! Sang bước tiếp theo trong Sổ tay.','Đến đúng bản đồ trong mục tiêu rồi hạ quái (Tự động đánh tính được).','97.831 EXP, 947 xu','','',0,'','',0),
('CA-05.3','cua_ai','cua_ai','','','','','','Cửa ải Đường sang Johto','Hạ Trưởng Gym Vermilion: Luxray (Sổ tay huấn luyện → Gym).','Nhiệm vụ cửa ải «Đường sang Johto» (3/3): Hạ Trưởng Gym Vermilion: Luxray (Sổ tay huấn luyện → Gym).','{da}/1','Tốt lắm! Cửa ải «Đường sang Johto» đã mở.','Mở Sổ tay huấn luyện → Gym để triệu hồi trưởng Gym rồi đánh tay (boss không dùng Tự động đánh).','97.831 EXP, 947 xu, 1 lượng khoá; mở bản đồ mới','','',0,'','',0),
('CA-06.1','cua_ai','cua_ai','','11','','ban_do=11','','Cửa ải Rừng Viridian sâu thẳm','Hạ 50 Crawdaunt ở Rừng Viridian.','Nhiệm vụ cửa ải «Rừng Viridian sâu thẳm» (1/2): Hạ 50 Crawdaunt ở Rừng Viridian.','{da}/50','Tốt lắm! Sang bước tiếp theo trong Sổ tay.','Đến đúng bản đồ trong mục tiêu rồi hạ quái (Tự động đánh tính được).','188.754 EXP, 1.426 xu','','',0,'','',0),
('CA-06.2','cua_ai','cua_ai','','','','','','Cửa ải Rừng Viridian sâu thẳm','Hạ Trưởng Gym Celadon: Vileplume (Sổ tay huấn luyện → Gym).','Nhiệm vụ cửa ải «Rừng Viridian sâu thẳm» (2/2): Hạ Trưởng Gym Celadon: Vileplume (Sổ tay huấn luyện → Gym).','{da}/1','Tốt lắm! Cửa ải «Rừng Viridian sâu thẳm» đã mở.','Mở Sổ tay huấn luyện → Gym để triệu hồi trưởng Gym rồi đánh tay (boss không dùng Tự động đánh).','215.831 EXP, 1.554 xu, 1 lượng khoá; mở bản đồ mới','','',0,'','',0),
('CA-07.1','cua_ai','cua_ai','','13','','ban_do=13','','Cửa ải Leo Núi Mặt Trăng','Hạ 50 Farfetch''d ở Núi Mặt Trăng.','Nhiệm vụ cửa ải «Leo Núi Mặt Trăng» (1/2): Hạ 50 Farfetch''d ở Núi Mặt Trăng.','{da}/50','Tốt lắm! Sang bước tiếp theo trong Sổ tay.','Đến đúng bản đồ trong mục tiêu rồi hạ quái (Tự động đánh tính được).','247.553 EXP, 1.712 xu','','',0,'','',0),
('CA-07.2','cua_ai','cua_ai','','','','','','Cửa ải Leo Núi Mặt Trăng','Hạ Trưởng Gym Saffron: Seismitoad (Sổ tay huấn luyện → Gym).','Nhiệm vụ cửa ải «Leo Núi Mặt Trăng» (2/2): Hạ Trưởng Gym Saffron: Seismitoad (Sổ tay huấn luyện → Gym).','{da}/1','Tốt lắm! Cửa ải «Leo Núi Mặt Trăng» đã mở.','Mở Sổ tay huấn luyện → Gym để triệu hồi trưởng Gym rồi đánh tay (boss không dùng Tự động đánh).','265.121 EXP, 1.791 xu, 1 lượng khoá; mở bản đồ mới','','',0,'','',0),
('CA-08.1','cua_ai','cua_ai','','15','','ban_do=15','','Cửa ải Bóng tối Cherrygrove','Hạ 55 Dusclops ở Cherrygrove.','Nhiệm vụ cửa ải «Bóng tối Cherrygrove» (1/3): Hạ 55 Dusclops ở Cherrygrove.','{da}/55','Tốt lắm! Sang bước tiếp theo trong Sổ tay.','Đến đúng bản đồ trong mục tiêu rồi hạ quái (Tự động đánh tính được).','334.302 EXP, 2.216 xu','','',0,'','',0),
('CA-08.2','cua_ai','cua_ai','','','','','','Cửa ải Bóng tối Cherrygrove','Hạ Trưởng Gym Fuchsia: Gengar (Sổ tay huấn luyện → Gym).','Nhiệm vụ cửa ải «Bóng tối Cherrygrove» (2/3): Hạ Trưởng Gym Fuchsia: Gengar (Sổ tay huấn luyện → Gym).','{da}/1','Tốt lắm! Sang bước tiếp theo trong Sổ tay.','Mở Sổ tay huấn luyện → Gym để triệu hồi trưởng Gym rồi đánh tay (boss không dùng Tự động đánh).','334.302 EXP, 2.216 xu','','',0,'','',0),
('CA-08.3','cua_ai','cua_ai','','','','','','Cửa ải Bóng tối Cherrygrove','Hạ Trưởng Gym Cinnabar: Magcargo (Sổ tay huấn luyện → Gym).','Nhiệm vụ cửa ải «Bóng tối Cherrygrove» (3/3): Hạ Trưởng Gym Cinnabar: Magcargo (Sổ tay huấn luyện → Gym).','{da}/1','Tốt lắm! Cửa ải «Bóng tối Cherrygrove» đã mở.','Mở Sổ tay huấn luyện → Gym để triệu hồi trưởng Gym rồi đánh tay (boss không dùng Tự động đánh).','368.712 EXP, 2.537 xu, 1 lượng khoá; mở bản đồ mới','','',0,'','',0),
('CA-09.1','cua_ai','cua_ai','','18','','ban_do=18','','Cửa ải Phố lớn Goldenrod','Hạ 60 Golem ở Goldenrod.','Nhiệm vụ cửa ải «Phố lớn Goldenrod» (1/2): Hạ 60 Golem ở Goldenrod.','{da}/60','Tốt lắm! Sang bước tiếp theo trong Sổ tay.','Đến đúng bản đồ trong mục tiêu rồi hạ quái (Tự động đánh tính được).','406.632 EXP, 2.903 xu','','',0,'','',0),
('CA-09.2','cua_ai','cua_ai','','','','','','Cửa ải Phố lớn Goldenrod','Hạ Trưởng Gym Viridian: Rhydon (Sổ tay huấn luyện → Gym).','Nhiệm vụ cửa ải «Phố lớn Goldenrod» (2/2): Hạ Trưởng Gym Viridian: Rhydon (Sổ tay huấn luyện → Gym).','{da}/1','Tốt lắm! Cửa ải «Phố lớn Goldenrod» đã mở.','Mở Sổ tay huấn luyện → Gym để triệu hồi trưởng Gym rồi đánh tay (boss không dùng Tự động đánh).','406.632 EXP, 2.903 xu, 1 lượng khoá; mở bản đồ mới','','',0,'','',0),
('CA-10.1','cua_ai','cua_ai','','20','','ban_do=20','','Cửa ải Rừng Violet','Hạ 65 Rhydon ở Violet.','Nhiệm vụ cửa ải «Rừng Violet» (1/3): Hạ 65 Rhydon ở Violet.','{da}/65','Tốt lắm! Sang bước tiếp theo trong Sổ tay.','Đến đúng bản đồ trong mục tiêu rồi hạ quái (Tự động đánh tính được).','545.344 EXP, 4.144 xu','','',0,'','',0),
('CA-10.2','cua_ai','cua_ai','','','','','','Cửa ải Rừng Violet','Hạ Trưởng Gym Violet: Dodrio (Sổ tay huấn luyện → Gym).','Nhiệm vụ cửa ải «Rừng Violet» (2/3): Hạ Trưởng Gym Violet: Dodrio (Sổ tay huấn luyện → Gym).','{da}/1','Tốt lắm! Sang bước tiếp theo trong Sổ tay.','Mở Sổ tay huấn luyện → Gym để triệu hồi trưởng Gym rồi đánh tay (boss không dùng Tự động đánh).','545.344 EXP, 4.144 xu','','',0,'','',0),
('CA-10.3','cua_ai','cua_ai','','','','','','Cửa ải Rừng Violet','Hạ Trưởng Gym Azalea: Heracross (Sổ tay huấn luyện → Gym).','Nhiệm vụ cửa ải «Rừng Violet» (3/3): Hạ Trưởng Gym Azalea: Heracross (Sổ tay huấn luyện → Gym).','{da}/1','Tốt lắm! Cửa ải «Rừng Violet» đã mở.','Mở Sổ tay huấn luyện → Gym để triệu hồi trưởng Gym rồi đánh tay (boss không dùng Tự động đánh).','545.344 EXP, 4.144 xu, 1 lượng khoá; mở bản đồ mới','','',0,'','',0),
('CA-11.1','cua_ai','cua_ai','','22','','ban_do=22','','Cửa ải Chuyến tàu S.S. Anne','Hạ 70 Kingler ở Tàu S.S. Anne.','Nhiệm vụ cửa ải «Chuyến tàu S.S. Anne» (1/3): Hạ 70 Kingler ở Tàu S.S. Anne.','{da}/70','Tốt lắm! Sang bước tiếp theo trong Sổ tay.','Đến đúng bản đồ trong mục tiêu rồi hạ quái (Tự động đánh tính được).','743.544 EXP, 5.911 xu','','',0,'','',0),
('CA-11.2','cua_ai','cua_ai','','','','','','Cửa ải Chuyến tàu S.S. Anne','Hạ Trưởng Gym Goldenrod: Tauros (Sổ tay huấn luyện → Gym).','Nhiệm vụ cửa ải «Chuyến tàu S.S. Anne» (2/3): Hạ Trưởng Gym Goldenrod: Tauros (Sổ tay huấn luyện → Gym).','{da}/1','Tốt lắm! Sang bước tiếp theo trong Sổ tay.','Mở Sổ tay huấn luyện → Gym để triệu hồi trưởng Gym rồi đánh tay (boss không dùng Tự động đánh).','743.544 EXP, 5.911 xu','','',0,'','',0),
('CA-11.3','cua_ai','cua_ai','','','','','','Cửa ải Chuyến tàu S.S. Anne','Hạ Trưởng Gym Ecruteak: Dusknoir (Sổ tay huấn luyện → Gym).','Nhiệm vụ cửa ải «Chuyến tàu S.S. Anne» (3/3): Hạ Trưởng Gym Ecruteak: Dusknoir (Sổ tay huấn luyện → Gym).','{da}/1','Tốt lắm! Cửa ải «Chuyến tàu S.S. Anne» đã mở.','Mở Sổ tay huấn luyện → Gym để triệu hồi trưởng Gym rồi đánh tay (boss không dùng Tự động đánh).','743.544 EXP, 5.911 xu, 1 lượng khoá; mở bản đồ mới','','',0,'','',0),
('CA-12.1','cua_ai','cua_ai','','24','','ban_do=24','','Cửa ải Núi Olivine','Hạ 75 Vileplume ở Olivine.','Nhiệm vụ cửa ải «Núi Olivine» (1/3): Hạ 75 Vileplume ở Olivine.','{da}/75','Tốt lắm! Sang bước tiếp theo trong Sổ tay.','Đến đúng bản đồ trong mục tiêu rồi hạ quái (Tự động đánh tính được).','1.046.665 EXP, 8.395 xu','','',0,'','',0),
('CA-12.2','cua_ai','cua_ai','','','','','','Cửa ải Núi Olivine','Hạ Trưởng Gym Olivine: Aegislash (Sổ tay huấn luyện → Gym).','Nhiệm vụ cửa ải «Núi Olivine» (2/3): Hạ Trưởng Gym Olivine: Aegislash (Sổ tay huấn luyện → Gym).','{da}/1','Tốt lắm! Sang bước tiếp theo trong Sổ tay.','Mở Sổ tay huấn luyện → Gym để triệu hồi trưởng Gym rồi đánh tay (boss không dùng Tự động đánh).','1.046.665 EXP, 8.395 xu','','',0,'','',0),
('CA-12.3','cua_ai','cua_ai','','','','','','Cửa ải Núi Olivine','Hạ Trưởng Gym Cianwood: Crabrawler (Sổ tay huấn luyện → Gym).','Nhiệm vụ cửa ải «Núi Olivine» (3/3): Hạ Trưởng Gym Cianwood: Crabrawler (Sổ tay huấn luyện → Gym).','{da}/1','Tốt lắm! Cửa ải «Núi Olivine» đã mở.','Mở Sổ tay huấn luyện → Gym để triệu hồi trưởng Gym rồi đánh tay (boss không dùng Tự động đánh).','1.046.665 EXP, 8.395 xu, 1 lượng khoá; mở bản đồ mới','','',0,'','',0)
ON DUPLICATE KEY UPDATE `map_id`=VALUES(`map_id`), `dieu_kien`=VALUES(`dieu_kien`), `mo_ta`=VALUES(`mo_ta`),
 `goi_y`=VALUES(`goi_y`), `dong_thuong`=VALUES(`dong_thuong`),
 `muc_tieu_text`=VALUES(`muc_tieu_text`), `hoi_thoai_bat_dau`=VALUES(`hoi_thoai_bat_dau`),
 `hoi_thoai_dang_lam`=VALUES(`hoi_thoai_dang_lam`), `hoi_thoai_xong`=VALUES(`hoi_thoai_xong`);

CREATE TABLE IF NOT EXISTS `tob_ban_do_mo` (
  `id_char` int NOT NULL,
  `moc_mien` int NOT NULL DEFAULT 0,
  `nguon` varchar(16) NOT NULL DEFAULT '',
  `luc` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id_char`)
) DEFAULT CHARSET=utf8;
INSERT IGNORE INTO `tob_ban_do_mo` (`id_char`, `moc_mien`, `nguon`, `luc`)
 SELECT c.`id`, GREATEST(1, c.`lastLv`, COALESCE(k.`cap_toi_thieu`, 0)), 'nang_cap', NOW()
 FROM `tob_char` c LEFT JOIN `data_ban_do_mo_khoa` k ON k.`map_id` = c.`map`;

-- ======================================================================================================================
-- 8. main 0cbf73ae: vong quay gop hu (tung la sua_vong_quay.sql: 4 bang tob_vong_quay_*; danh muc nhom vong_quay nam
--    o phan 2) + noi dung CMS web 0.9.6 (tung la web_cap_nhat_noi_dung_096.sql: mo ta the Linux tai game). Cuoc cu
--    nin_xoso* duoc may chu hoan luc khoi dong (DatabaseVongQuay.chuyenCuocCu), khong o day.
-- ======================================================================================================================
CREATE TABLE IF NOT EXISTS `tob_vong_quay_cuoc` (
  `loai` TINYINT NOT NULL, `charname` VARCHAR(40) NOT NULL, `char_id` INT NOT NULL DEFAULT 0,
  `so` BIGINT NOT NULL DEFAULT 0, `may` VARCHAR(64) NOT NULL DEFAULT '', `ip` VARCHAR(64) NOT NULL DEFAULT '',
  `luc` DATETIME NOT NULL, PRIMARY KEY (`loai`, `charname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `tob_vong_quay_cho_nhan` (
  `charname` VARCHAR(40) NOT NULL, `xu` BIGINT NOT NULL DEFAULT 0, `luong` BIGINT NOT NULL DEFAULT 0,
  `ly_do` VARCHAR(255) NOT NULL DEFAULT '', `cap_nhat` DATETIME NOT NULL, PRIMARY KEY (`charname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `tob_vong_quay_ngay` (
  `ngay` DATE NOT NULL, `loai` TINYINT NOT NULL, `charname` VARCHAR(40) NOT NULL,
  `may` VARCHAR(64) NOT NULL DEFAULT '', `ip` VARCHAR(64) NOT NULL DEFAULT '',
  `luot` INT NOT NULL DEFAULT 0, `tong` BIGINT NOT NULL DEFAULT 0, PRIMARY KEY (`ngay`, `loai`, `charname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `tob_vong_quay_lich_su` (
  `id` BIGINT NOT NULL AUTO_INCREMENT, `luc` DATETIME NOT NULL, `loai` TINYINT NOT NULL,
  `so_nguoi` INT NOT NULL DEFAULT 0, `hu` BIGINT NOT NULL DEFAULT 0, `phi` BIGINT NOT NULL DEFAULT 0,
  `tra` BIGINT NOT NULL DEFAULT 0, `hoan` TINYINT NOT NULL DEFAULT 0,
  `nguoi_thang` VARCHAR(40) NOT NULL DEFAULT '', `chi_tiet` VARCHAR(1000) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`), KEY `k_luc` (`luc`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

UPDATE web_cai_dat
   SET gia_tri = JSON_SET(
         gia_tri,
         REPLACE(JSON_UNQUOTE(JSON_SEARCH(gia_tri, 'one', 'linux', NULL, '$.ban[*].ma')), '.ma', '.moTa'),
         'Bản Linux 64-bit một tệp .AppImage, có sẵn Java đi kèm'
       )
 WHERE khoa = 'taiGame'
   AND JSON_VALID(gia_tri)
   AND JSON_SEARCH(gia_tri, 'one', 'linux', NULL, '$.ban[*].ma') IS NOT NULL
   AND JSON_UNQUOTE(JSON_EXTRACT(gia_tri, REPLACE(JSON_UNQUOTE(JSON_SEARCH(gia_tri, 'one', 'linux', NULL, '$.ban[*].ma')), '.ma', '.moTa')))
       <> 'Bản Linux 64-bit một tệp .AppImage, có sẵn Java đi kèm';

-- ======================================================================================================================
-- 9. main bda0e901: thach dau cuoc xu ky quy + phi, li xi Tet co tran (tung la sua_vong_quay_2.sql; danh muc nhom
--    cuoc_xu o phan 2; tob_vong_quay_cho_nhan da o phan 8).
-- ======================================================================================================================
CREATE TABLE IF NOT EXISTS `tob_gioi_han_qua` (
  `ma` VARCHAR(40) NOT NULL, `khoa` VARCHAR(80) NOT NULL, `ngay` VARCHAR(8) NOT NULL DEFAULT '',
  `so_ngay` BIGINT NOT NULL DEFAULT 0, `tong` BIGINT NOT NULL DEFAULT 0, `cap_nhat` DATETIME NULL,
  PRIMARY KEY (`ma`, `khoa`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `tob_thach_dau_ky_quy` (
  `id_arena` INT NOT NULL, `loai` TINYINT NOT NULL DEFAULT 3,
  `ten0` VARCHAR(40) NOT NULL DEFAULT '', `id0` INT NOT NULL DEFAULT 0,
  `ten1` VARCHAR(40) NOT NULL DEFAULT '', `id1` INT NOT NULL DEFAULT 0,
  `cuoc` BIGINT NOT NULL DEFAULT 0, `luc` DATETIME NOT NULL, PRIMARY KEY (`id_arena`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
