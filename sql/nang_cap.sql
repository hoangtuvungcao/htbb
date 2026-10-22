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
CREATE TABLE IF NOT EXISTS `data_huan_luyen` (
  `id` tinyint(4) NOT NULL,
  `ma` varchar(16) NOT NULL,
  `ten` varchar(32) NOT NULL,
  `mo_ta` varchar(120) NOT NULL DEFAULT '',
  `tran_diem` smallint(6) NOT NULL,
  `nguong_giam` smallint(6) NOT NULL,
  `phan_nghin_truoc` smallint(6) NOT NULL,
  `phan_nghin_sau` smallint(6) NOT NULL,
  `thu_tu` tinyint(4) NOT NULL DEFAULT 0,
  `sua_boi` varchar(50) DEFAULT NULL,
  `sua_luc` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_learnset` (
  `loai` smallint(6) NOT NULL,
  `o` tinyint(4) NOT NULL,
  `dong` tinyint(4) NOT NULL,
  `ten` varchar(48) NOT NULL,
  `he` tinyint(4) NOT NULL,
  `kieu` varchar(16) NOT NULL,
  `nguon` varchar(8) NOT NULL DEFAULT 'cap',
  `cap_hoc` tinyint(4) NOT NULL,
  `gia_xu` int(11) NOT NULL DEFAULT 0,
  `cap_cap_chieu` varchar(64) NOT NULL,
  `sua_boi` varchar(50) DEFAULT NULL,
  `sua_luc` datetime DEFAULT NULL,
  PRIMARY KEY (`loai`,`o`),
  KEY `k_dong` (`dong`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `data_tang_truong_dong` (
  `dong` tinyint(4) NOT NULL,
  `ten` varchar(24) NOT NULL,
  `suc_manh` tinyint(4) NOT NULL,
  `kheo_leo` tinyint(4) NOT NULL,
  `tinh_than` tinyint(4) NOT NULL,
  `suc_khoe` tinyint(4) NOT NULL,
  `may_man` tinyint(4) NOT NULL DEFAULT 0,
  `ghi_chu` varchar(160) NOT NULL DEFAULT '',
  `sua_boi` varchar(50) DEFAULT NULL,
  `sua_luc` datetime DEFAULT NULL,
  PRIMARY KEY (`dong`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_chuyen_doi_can_bang` (
  `char_id` int(11) NOT NULL,
  `buoc` varchar(24) NOT NULL,
  `luc` datetime NOT NULL,
  `chi_tiet` text DEFAULT NULL,
  `xu_hoan` bigint(20) NOT NULL DEFAULT 0,
  `da_hoan` tinyint(4) NOT NULL DEFAULT 0,
  `da_bao` tinyint(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`char_id`,`buoc`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
CREATE TABLE IF NOT EXISTS `tob_huan_luyen` (
  `char_id` int(11) NOT NULL,
  `phan_bo` varchar(48) NOT NULL DEFAULT '',
  `lan_tay` int(11) NOT NULL DEFAULT 0,
  `tay_luc` bigint(20) NOT NULL DEFAULT 0,
  `tong_xu_tay` bigint(20) NOT NULL DEFAULT 0,
  `cap_nhat` datetime DEFAULT NULL,
  PRIMARY KEY (`char_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

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
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chong_clone.ban_npc_do_khoa', 'chong_clone', 'Bán đồ / nguyên liệu khoá cho NPC lấy xu', '0 = đồ khoá chỉ bán ở giá thấp (chong_clone.ban_npc_khoa_phan_tram). 1 = bán giá NPC thường như cũ.', 'co', '0', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.OfflineMap#doSellItem / real.Map#doSellGemItem', 71, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chong_clone.ban_npc_khoa_phan_tram', 'chong_clone', 'Giá bán đồ / nguyên liệu khoá cho NPC (% giá NPC thường)', '0.9.8: đồ khoá (phần thưởng khoá, mua bằng lượng khoá) bán NPC ở giá thấp = số này % giá NPC thường (mặc định 25% = 1/4, khớp thẻ vật phẩm client; trần cứng 25%; giá NPC thường đã là 5% giá mẫu). 0 = không bán. Thuốc không bán. Vẫn không giao dịch / vứt cho người khác.', 'so', '25', '{"min": 0, "max": 25}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.BanDoKhoa', 72, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.giay_moi_vong', 'vong_quay', 'Thời gian một lượt (giây)', 'Đếm từ lúc có người đặt đầu tiên. Hết giờ: từ 2 người trở lên thì quay, chỉ 1 người thì hoàn lại đủ.', 'so', '120', '{"min": 30, "max": 3600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.VongQuayLogic', 73, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.giay_khoa', 'vong_quay', 'Khoá đặt cuối lượt (giây)', 'Mấy giây cuối không nhận thêm cược.', 'so', '10', '{"min": 0, "max": 120}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.VongQuayLogic', 74, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.thuong.bat', 'vong_quay', 'Vòng quay thường: bật', '0 = đóng (người đang chờ vẫn được quay / hoàn như thường).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 75, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.thuong.cap_toi_thieu', 'vong_quay', 'Vòng quay thường: cấp tối thiểu', 'Luôn cộng thêm cổng chống clone (chong_clone.cap_toi_thieu, tuổi tài khoản, giờ chơi): cấp hiệu lực = max(hai số).', 'so', '20', '{"min": 0, "max": 200}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 76, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.thuong.cuoc_toi_thieu', 'vong_quay', 'Vòng quay thường: đặt tối thiểu mỗi lượt (xu)', 'Mỗi người đặt 1 lần / lượt.', 'so', '1000', '{"min": 1, "max": 2000000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 77, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.thuong.cuoc_toi_da', 'vong_quay', 'Vòng quay thường: đặt tối đa mỗi lượt (xu)', 'Trần hũ = số này × số người tối đa.', 'so', '10000', '{"min": 1, "max": 2000000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 78, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.thuong.cuoc_ngay_toi_da', 'vong_quay', 'Vòng quay thường: tổng đặt tối đa mỗi ngày (xu)', 'Mỗi Pokémon của người chơi, tính theo ngày.', 'so', '30000', '{"min": 1, "max": 2000000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 79, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.thuong.luot_ngay_toi_da', 'vong_quay', 'Vòng quay thường: số lượt tối đa mỗi ngày', 'Mỗi Pokémon của người chơi.', 'so', '10', '{"min": 1, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 80, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.thuong.so_nguoi_toi_da', 'vong_quay', 'Vòng quay thường: số người tối đa mỗi lượt', '', 'so', '20', '{"min": 2, "max": 50}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 81, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.thuong.phi_phan_tram', 'vong_quay', 'Vòng quay thường: phí hũ (%)', 'Phần trăm hũ bị huỷ (hố hút xu) khi có người thắng. Tối thiểu 5% để vòng quay không thành đường chuyển xu rẻ hơn giao dịch.', 'so', '10', '{"min": 5, "max": 50}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 82, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.vip.bat', 'vong_quay', 'Vòng quay VIP: bật', '0 = đóng (người đang chờ vẫn được quay / hoàn như thường).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 83, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.vip.cap_toi_thieu', 'vong_quay', 'Vòng quay VIP: cấp tối thiểu', 'Luôn cộng thêm cổng chống clone (chong_clone.cap_toi_thieu, tuổi tài khoản, giờ chơi): cấp hiệu lực = max(hai số).', 'so', '50', '{"min": 0, "max": 200}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 84, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.vip.cuoc_toi_thieu', 'vong_quay', 'Vòng quay VIP: đặt tối thiểu mỗi lượt (xu)', 'Mỗi người đặt 1 lần / lượt.', 'so', '10000', '{"min": 1, "max": 2000000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 85, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.vip.cuoc_toi_da', 'vong_quay', 'Vòng quay VIP: đặt tối đa mỗi lượt (xu)', 'Trần hũ = số này × số người tối đa.', 'so', '50000', '{"min": 1, "max": 2000000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 86, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.vip.cuoc_ngay_toi_da', 'vong_quay', 'Vòng quay VIP: tổng đặt tối đa mỗi ngày (xu)', 'Mỗi Pokémon của người chơi, tính theo ngày.', 'so', '100000', '{"min": 1, "max": 2000000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 87, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.vip.luot_ngay_toi_da', 'vong_quay', 'Vòng quay VIP: số lượt tối đa mỗi ngày', 'Mỗi Pokémon của người chơi.', 'so', '10', '{"min": 1, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 88, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.vip.so_nguoi_toi_da', 'vong_quay', 'Vòng quay VIP: số người tối đa mỗi lượt', '', 'so', '20', '{"min": 2, "max": 50}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 89, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.vip.phi_phan_tram', 'vong_quay', 'Vòng quay VIP: phí hũ (%)', 'Phần trăm hũ bị huỷ (hố hút xu) khi có người thắng. Tối thiểu 5% để vòng quay không thành đường chuyển xu rẻ hơn giao dịch.', 'so', '10', '{"min": 5, "max": 50}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 90, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.luong.bat', 'vong_quay', 'Vòng quay lượng: bật', '0 = đóng (người đang chờ vẫn được quay / hoàn như thường).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 91, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.luong.cap_toi_thieu', 'vong_quay', 'Vòng quay lượng: cấp tối thiểu', 'Luôn cộng thêm cổng chống clone (chong_clone.cap_toi_thieu, tuổi tài khoản, giờ chơi): cấp hiệu lực = max(hai số).', 'so', '20', '{"min": 0, "max": 200}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 92, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.luong.cuoc_toi_thieu', 'vong_quay', 'Vòng quay lượng: đặt tối thiểu mỗi lượt (lượng)', 'Mỗi người đặt 1 lần / lượt.', 'so', '10', '{"min": 1, "max": 2000000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 93, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.luong.cuoc_toi_da', 'vong_quay', 'Vòng quay lượng: đặt tối đa mỗi lượt (lượng)', 'Trần hũ = số này × số người tối đa.', 'so', '100', '{"min": 1, "max": 2000000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 94, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.luong.cuoc_ngay_toi_da', 'vong_quay', 'Vòng quay lượng: tổng đặt tối đa mỗi ngày (lượng)', 'Mỗi Pokémon của người chơi, tính theo ngày.', 'so', '300', '{"min": 1, "max": 2000000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 95, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.luong.luot_ngay_toi_da', 'vong_quay', 'Vòng quay lượng: số lượt tối đa mỗi ngày', 'Mỗi Pokémon của người chơi.', 'so', '10', '{"min": 1, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 96, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.luong.so_nguoi_toi_da', 'vong_quay', 'Vòng quay lượng: số người tối đa mỗi lượt', '', 'so', '20', '{"min": 2, "max": 50}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 97, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('vong_quay.luong.phi_phan_tram', 'vong_quay', 'Vòng quay lượng: phí hũ (%)', 'Phần trăm hũ bị huỷ (hố hút lượng) khi có người thắng. Tối thiểu 5% để vòng quay không thành đường chuyển lượng rẻ hơn giao dịch.', 'so', '15', '{"min": 5, "max": 50}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.VongQuay', 98, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('thach_dau.bat', 'cuoc_xu', 'Thách đấu cược xu: bật', '0 = không nhận trận cược mới (trận đang đánh vẫn chia như thường).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ThachDauCuoc / com.htbb.pokemon.ThachDauLogic', 99, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('thach_dau.cap_toi_thieu', 'cuoc_xu', 'Thách đấu cược xu: cấp tối thiểu', 'Mọi người trong trận (1-1 hoặc cả hai nhóm). Luôn cộng thêm cổng chống clone: cấp hiệu lực = max(hai số).', 'so', '40', '{"min": 0, "max": 200}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ThachDauCuoc / com.htbb.pokemon.ThachDauLogic', 100, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('thach_dau.cuoc_toi_thieu', 'cuoc_xu', 'Thách đấu cược xu: cược tối thiểu (xu)', 'Mỗi bên.', 'so', '1000', '{"min": 1, "max": 2000000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ThachDauCuoc / com.htbb.pokemon.ThachDauLogic', 101, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('thach_dau.cuoc_toi_da', 'cuoc_xu', 'Thách đấu cược xu: cược tối đa (xu)', 'Trần tuyệt đối mỗi bên; trần thật = min(số này, số giờ dưới × thu nhập 1 giờ ở cấp thấp nhất trong trận).', 'so', '100000', '{"min": 1, "max": 2000000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ThachDauCuoc / com.htbb.pokemon.ThachDauLogic', 102, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('thach_dau.cuoc_toi_da_gio', 'cuoc_xu', 'Thách đấu cược xu: cược tối đa (giờ thu nhập)', 'Theo cấp thấp nhất trong trận (LauDai.xuMoiGio).', 'so', '2', '{"min": 1, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ThachDauCuoc / com.htbb.pokemon.ThachDauLogic', 103, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('thach_dau.phi_phan_tram', 'cuoc_xu', 'Thách đấu cược xu: phí bên thắng (%)', 'Phần trăm của 2 × cược bị huỷ (hố hút) khi có bên thắng; hoà thì hoàn đủ. Tối thiểu 5% để thua cố ý không rẻ hơn giao dịch.', 'so', '10', '{"min": 5, "max": 50}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ThachDauCuoc / com.htbb.pokemon.ThachDauLogic', 104, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('li_xi.tui_nhan_ngay_toi_da', 'cuoc_xu', 'Bao lì xì: nhận tối đa / ngày', 'Mỗi Pokémon của người chơi.', 'so', '2', '{"min": 1, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.Char.doOpenLixi2021 / real.Map.doReceiveLixi / com.htbb.pokemon.LiXiLogic', 105, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('li_xi.tui_mo_ngay_toi_da', 'cuoc_xu', 'Bao lì xì: mở tối đa / ngày', 'Mỗi Pokémon của người chơi (bao nhận qua giao dịch cũng tính). Vượt thì trả lại bao.', 'so', '3', '{"min": 1, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.Char.doOpenLixi2021 / real.Map.doReceiveLixi / com.htbb.pokemon.LiXiLogic', 106, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('li_xi.tui_mo_dot_toi_da', 'cuoc_xu', 'Bao lì xì: mở tối đa cả đợt Tết', 'Mỗi Pokémon của người chơi, đợt = năm.', 'so', '15', '{"min": 1, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.Char.doOpenLixi2021 / real.Map.doReceiveLixi / com.htbb.pokemon.LiXiLogic', 107, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('li_xi.tui_xu_phan_tram_gio_min', 'cuoc_xu', 'Bao lì xì: xu thấp nhất (% giờ thu nhập)', '% thu nhập 1 giờ ở cấp người mở.', 'so', '5', '{"min": 0, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.Char.doOpenLixi2021 / real.Map.doReceiveLixi / com.htbb.pokemon.LiXiLogic', 108, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('li_xi.tui_xu_phan_tram_gio_max', 'cuoc_xu', 'Bao lì xì: xu cao nhất (% giờ thu nhập)', '', 'so', '10', '{"min": 0, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.Char.doOpenLixi2021 / real.Map.doReceiveLixi / com.htbb.pokemon.LiXiLogic', 109, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('li_xi.tui_giai_lon_phan_tram_gio', 'cuoc_xu', 'Bao lì xì: giải lớn (% giờ thu nhập)', 'Trước: 1.000.000 – 5.000.000 xu. Tối đa 300% (kẹp trong code); 5 giải / đợt toàn máy chủ.', 'so', '100', '{"min": 0, "max": 300}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.Char.doOpenLixi2021 / real.Map.doReceiveLixi / com.htbb.pokemon.LiXiLogic', 110, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('li_xi.ma_luong_khoa_min', 'cuoc_xu', 'Mã lì xì: lượng khoá thấp nhất', 'Trả lượng KHOÁ (không giao dịch).', 'so', '5', '{"min": 1, "max": 50}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.MapNhapText (-32051) / com.htbb.pokemon.LiXiLogic', 111, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('li_xi.ma_luong_khoa_max', 'cuoc_xu', 'Mã lì xì: lượng khoá cao nhất', '', 'so', '15', '{"min": 1, "max": 50}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.MapNhapText (-32051) / com.htbb.pokemon.LiXiLogic', 112, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('li_xi.ma_tai_khoan_moi_may', 'cuoc_xu', 'Mã lì xì: tài khoản / thiết bị / IP / đợt', 'Mỗi tài khoản 1 mã / đợt; mỗi thiết bị và mỗi IP tối đa N tài khoản / đợt.', 'so', '1', '{"min": 1, "max": 20}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.MapNhapText (-32051) / com.htbb.pokemon.LiXiLogic', 113, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('li_xi.ma_tong_luong_khoa_dot', 'cuoc_xu', 'Mã lì xì: tổng lượng khoá / đợt (cả máy chủ)', 'Hết quỹ thì mã còn lại không nhận được.', 'so', '20000', '{"min": 0, "max": 10000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.MapNhapText (-32051) / com.htbb.pokemon.LiXiLogic', 114, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.dangKyTrongGame', 'bao_tri', 'Đăng ký trong game', '0 = nút Đăng ký trong game báo ''đăng ký trên web''.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.cmd.DangKyTrongGame', 115, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.dangKyTuKichHoat', 'bao_tri', 'Tự kích hoạt tài khoản đăng ký trong game', 'web = theo cấu hình web (web.autoActivate).', 'chon', 'web', '{"luaChon": ["web", "1", "0"], "luaChonTen": ["Theo web", "Lu\\u00f4n k\\u00edch ho\\u1ea1t", "Kh\\u00f4ng k\\u00edch ho\\u1ea1t"]}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.cmd.DangKyTrongGame', 116, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.dangKyTreMs', 'dang_ky', 'Trễ phản hồi đăng ký (ms)', 'Làm chậm dò tài khoản.', 'so', '800', '{"min": 0, "max": 5000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.cmd.DangKyTrongGame', 117, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.dangKyCsdlTaiKhoan', 'dang_ky', 'CSDL tài khoản', 'Tên database chứa team_user.', 'chuoi', 'account', '{"mau": "^[A-Za-z0-9_]{1,64}$"}', 'khoi_dong_lai', 0, 0, 0, 'cau_hinh', 'server', 'real.cmd.DangKyTrongGame', 118, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('dang_ky.web.bat', 'bao_tri', 'Đăng ký trên web', '0 = trang Đăng ký từ chối tài khoản mới.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'web', 'HTBB-Web api/auth/register.post.ts', 119, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.maxCharPerAccount', 'bao_tri', 'Số Pokémon tối đa / tài khoản', 'Client hỗ trợ tối đa 3.', 'so', '3', '{"min": 1, "max": 3}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'data.Database.MAX_CHAR_PER_ACCOUNT', 120, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.suKienTuKhoiDongLai', 'su_kien', 'Tự khởi động lại khi đổi sự kiện', '1 = lệnh bật/tắt sự kiện có hẹn sẽ tự khởi động lại máy chủ.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.LichSuKien', 121, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.suKienGomGiay', 'su_kien', 'Gom lệnh sự kiện (giây)', 'Gom nhiều lệnh thành một lần khởi động lại.', 'so', '120', '{"min": 30, "max": 3600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.LichSuKien', 122, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.suKienBaoTruocGiay', 'su_kien', 'Báo trước khi đổi sự kiện (giây)', '', 'so', '60', '{"min": 10, "max": 600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.LichSuKien', 123, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.worldCupBatDau', 'su_kien', 'World Cup bắt đầu', 'yyyy-MM-dd HH:mm:ss. Trống = tắt.', 'ngay_gio', '', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.SuKienWorldCup', 124, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.worldCupKetThuc', 'su_kien', 'World Cup kết thúc', 'Trống = tắt.', 'ngay_gio', '', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.SuKienWorldCup', 125, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.worldCupHanNhanQua', 'su_kien', 'World Cup hạn nhận quà', 'Trống = tắt.', 'ngay_gio', '', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.SuKienWorldCup', 126, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.gioChiemThanh', 'su_kien', 'Giờ chiếm Gym', '0–23, −1 = lịch chuẩn 21h thứ 3 + thứ 7.', 'so', '-1', '{"min": -1, "max": 23}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.MapTown', 127, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.phutChiemThanh', 'su_kien', 'Phút chiếm Gym', '0–59.', 'so', '-1', '{"min": -1, "max": 59}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.MapTown', 128, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.ngayChiemThanh', 'su_kien', 'Ngày chiếm Gym', 'Mon,Tue,... hoặc yyyy-MM-dd; trống = mỗi ngày.', 'chuoi', '', '{"mau": "^$|^((Mon|Tue|Wed|Thu|Fri|Sat|Sun)(,(Mon|Tue|Wed|Thu|Fri|Sat|Sun))*|\\\\d{4}-\\\\d{2}-\\\\d{2})$", "doDai": 64}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.MapTown', 129, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.isSuKienTrungThul2016', 'su_kien', 'Sự kiện cũ: Trung thu 2016', 'Cờ sự kiện gốc (đọc lúc khởi động).', 'co', '0', '{}', 'khoi_dong_lai', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.isSuKienTrungThul2016', 130, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.isSuKienTet2017', 'su_kien', 'Sự kiện cũ: Tết 2017', '', 'co', '0', '{}', 'khoi_dong_lai', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.isSuKienTet2017', 131, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.isSuKienGioTo2016', 'su_kien', 'Sự kiện cũ: Giỗ Tổ 2016', '', 'co', '0', '{}', 'khoi_dong_lai', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.isSuKienGioTo2016', 132, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.isSuKienNoel2023', 'su_kien', 'Sự kiện cũ: Noel 2023', '', 'co', '0', '{}', 'khoi_dong_lai', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.isSuKienNoel2023', 133, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.isSuKienTetduonglich2024', 'su_kien', 'Sự kiện cũ: Tết dương lịch 2024', '', 'co', '0', '{}', 'khoi_dong_lai', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.isSuKienTetduonglich2024', 134, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('bao_tri.bat', 'bao_tri', 'Chế độ bảo trì đăng nhập', '1 = từ chối đăng nhập mới (người đang chơi không bị đá). Tương đương tệp bao_tri.txt — bật một trong hai là bảo trì.', 'co', '0', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'server.ChiSoMayChu', 135, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('bao_tri.cau', 'bao_tri', 'Câu thông báo bảo trì', 'Trống = câu mặc định.', 'chuoi', '', '{"doDai": 200}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'server.ChiSoMayChu', 136, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('bao_tri.cho', 'bao_tri', 'Tài khoản được vào khi bảo trì', 'Tên tài khoản (chữ thường), cách nhau dấu phẩy.', 'ds_chuoi', '', '{"mau": "^[a-z0-9_]{3,20}$", "doDai": 255}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'server.ChiSoMayChu', 137, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.gioBaoTri', 'bao_tri', 'Giờ bảo trì hằng ngày', 'HH:MM cố định, ''tat'' = không tự bảo trì, trống = ngẫu nhiên 4:30–5:30.', 'chuoi', '', '{"mau": "^$|^(tat|off)$|^([01]?\\\\d|2[0-3]):[0-5]\\\\d$", "doDai": 5}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.BaoTriDaily', 138, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.chiSoMayChu', 'bao_tri', 'Ghi chỉ số máy chủ + đọc bao_tri.txt', '0 = tắt luồng chỉ số (tob_chi_so_may_chu).', 'co', '1', '{}', 'khoi_dong_lai', 0, 0, 0, 'cau_hinh', 'server', 'server.ChiSoMayChu', 139, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.spam.bat', 'chong_spam', 'Chống spam chat / thao tác', '0 = tắt toàn bộ.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.chongspam.ChongSpam', 140, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.spam.chat.gan', 'chong_spam', 'Nhịp chat gần', 'số tin / giây (vd 5/10).', 'nhip', '5/10', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongSpam', 141, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.spam.chat.rieng', 'chong_spam', 'Nhịp chat riêng', '', 'nhip', '6/10', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongSpam', 142, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.spam.chat.theGioi', 'chong_spam', 'Nhịp chat thế giới / người', '', 'nhip', '1/30', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongSpam', 143, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.spam.theGioiToanServer', 'chong_spam', 'Nhịp chat thế giới toàn máy chủ', '', 'nhip', '12/60', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongSpam', 144, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.spam.chat.doi', 'chong_spam', 'Nhịp chat đội', '', 'nhip', '8/10', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongSpam', 145, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.spam.chat.bang', 'chong_spam', 'Nhịp chat đội', '', 'nhip', '8/10', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongSpam', 146, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.spam.trungLapGiay', 'chong_spam', 'Chặn tin trùng lặp (giây)', '', 'so', '30', '{"min": 0, "max": 3600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongSpam', 147, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.spam.chanQuangCao', 'chong_spam', 'Chặn quảng cáo / link', '', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongSpam', 148, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.spam.viPhamCam', 'chong_spam', 'Số vi phạm thì cấm chat', '', 'so', '4', '{"min": 1, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongSpam', 149, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.spam.camGiay', 'chong_spam', 'Thang cấm chat (giây)', 'Các bậc cấm tăng dần, cách nhau dấu phẩy.', 'ds_so', '60,300,1800,7200', '{"min": 1, "max": 31536000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongSpam', 150, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.chat.capGan', 'chong_spam', 'Chat bản đồ: cấp tối thiểu', 'Dưới cấp này tin bản đồ không phát (người gửi được báo lý do). Trước 0.9.8 cố định 20 và bỏ im lặng.', 'so', '5', '{"min": 0, "max": 200}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'KenhChatLogic', 151, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.chat.capTheGioi', 'chong_spam', 'Chat thế giới: cấp tối thiểu', 'Kênh Thế giới trong bảng chat (0.9.8). Loa vẫn theo luật loa cũ.', 'so', '10', '{"min": 0, "max": 200}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'KenhChatLogic', 152, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.chat.xuTheGioi', 'chong_spam', 'Chat thế giới: phí xu mỗi tin', 'Hố hút xu nhỏ; 0 = miễn phí. Bị chặn (spam, cấp, giãn cách) thì không trừ.', 'so', '100', '{"min": 0, "max": 100000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'KenhChatLogic', 153, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.chat.giayTheGioi', 'chong_spam', 'Chat thế giới: giãn cách (giây)', 'Mỗi người một tin / N giây; client hiện đếm ngược. sv.spam.chat.theGioi vẫn áp.', 'so', '30', '{"min": 0, "max": 3600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'KenhChatLogic', 154, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.chat.capRieng', 'chong_spam', 'Nhắn riêng: cấp tối thiểu', 'Chặn tài khoản rác mới tạo nhắn riêng hàng loạt.', 'so', '3', '{"min": 0, "max": 200}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'KenhChatLogic', 155, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.chat.doDai', 'chong_spam', 'Độ dài tối đa một tin (ký tự)', 'Kênh thế giới / đội / nhóm / riêng của bảng chat 0.9.8.', 'so', '80', '{"min": 10, "max": 200}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'KenhChatLogic', 156, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.chat.thuChoToiDa', 'chong_spam', 'Hộp thư offline: tối đa tin chờ / người', 'Tin riêng gửi cho người offline (tob_chat_cho), giao khi họ vào game. 0 = tắt hộp thư.', 'so', '30', '{"min": 0, "max": 500}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'KenhChatLogic', 157, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.chat.thuChoNgay', 'chong_spam', 'Hộp thư offline: giữ tối đa (ngày)', 'Tin chờ quá hạn bị bỏ (xoá lúc khởi động).', 'so', '7', '{"min": 1, "max": 90}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'KenhChatLogic', 158, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.chat.lichSu', 'chong_spam', 'Lịch sử kênh gửi khi vào game (dòng)', 'Số dòng thế giới / đội gần nhất (trong bộ nhớ) gửi cho người vừa vào.', 'so', '50', '{"min": 0, "max": 200}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'KenhChatLogic', 159, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.chat.chanToiDa', 'chong_spam', 'Danh sách chặn: tối đa tên / người', 'tob_chat_chan.', 'so', '100', '{"min": 0, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'KenhChatLogic', 160, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.socketTimeout', 'chong_spam', 'Hết hạn socket (giây)', 'Áp dụng cho kết nối mới. 0 = tắt.', 'so', '180', '{"min": 0, "max": 3600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.SOCKET_TIMEOUT_MS', 161, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.maxConnPerIp', 'chong_spam', 'Kết nối tối đa / IP', '0 = tắt.', 'so', '50', '{"min": 0, "max": 100000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.MAX_CONN_PER_IP', 162, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.connRateMax', 'chong_spam', 'Số kết nối tối đa / cửa sổ', '0 = tắt.', 'so', '60', '{"min": 0, "max": 100000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.CONN_RATE_MAX', 163, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.connRateWindow', 'chong_spam', 'Cửa sổ đếm kết nối (giây)', '0 = tắt.', 'so', '30', '{"min": 0, "max": 3600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.CONN_RATE_WINDOW_MS', 164, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.dosBanSec', 'chong_spam', 'Thời gian chặn IP ngập (giây)', '', 'so', '300', '{"min": 0, "max": 86400}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.DOS_BAN_MS', 165, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.reloginWaitSec', 'chong_spam', 'Chờ đăng nhập lại (giây)', '', 'so', '0', '{"min": 0, "max": 600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.RELOGIN_WAIT_MS', 166, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.proxyTinCay', 'chong_spam', 'Proxy tin cậy (miễn giới hạn theo IP)', 'IP hoặc dải CIDR (IPv4/IPv6) của WAF / Nova agent, cách nhau dấu phẩy. Kết nối từ đây không bị tính sv.maxConnPerIp, sv.connRate*, chặn sv.dosBanSec, chờ đăng nhập theo IP, hạn mức đăng ký theo IP. Giới hạn theo tài khoản / kết nối / thiết bị vẫn giữ.', 'ds_chuoi', '127.0.0.1,::1', '{"mau": "^[0-9A-Fa-f:.]{2,45}(/[0-9]{1,3})?$", "doDai": 255}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.mang.ProxyTinCay', 167, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.dangNhapIpNguong', 'chong_spam', 'Ngưỡng đăng nhập dồn dập / IP', 'Số lần đăng nhập trong ~1 giây từ một IP (không phải proxy tin cậy) thì bắt đầu chờ + ghi nghi vấn. 0 = tắt.', 'so', '10', '{"min": 0, "max": 100000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.mang.GioiHanIp', 168, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.dangNhapIpChoMs', 'chong_spam', 'Chờ khi đăng nhập dồn dập (ms)', 'Thời gian chờ mỗi lần đăng nhập khi vượt sv.dangNhapIpNguong. 0 = không chờ.', 'so', '3000', '{"min": 0, "max": 10000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.mang.GioiHanIp', 169, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.tranGoiThuong', 'chong_spam', 'Trần gói tin thường / cửa sổ', '', 'so', '300', '{"min": 10, "max": 100000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'io.BoDemGoiTin', 170, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.tranGoiTaiNguyen', 'chong_spam', 'Trần gói tải tài nguyên / cửa sổ', '', 'so', '600', '{"min": 10, "max": 100000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'io.BoDemGoiTin', 171, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.tranGoiTraCuu', 'chong_spam', 'Trần gói tra cứu / cửa sổ', '', 'so', '600', '{"min": 10, "max": 100000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'io.BoDemGoiTin', 172, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.log.muc', 'chong_spam', 'Mức nhật ký (màn hình)', '', 'chon', 'THONG_TIN', '{"luaChon": ["LOI", "CANH_BAO", "THONG_TIN", "GO_LOI", "ERROR", "WARN", "INFO", "DEBUG"], "luaChonTen": ["L\\u1ed7i", "C\\u1ea3nh b\\u00e1o", "Th\\u00f4ng tin", "G\\u1ee1 l\\u1ed7i", "L\\u1ed7i (ERROR)", "C\\u1ea3nh b\\u00e1o (WARN)", "Th\\u00f4ng tin (INFO)", "G\\u1ee1 l\\u1ed7i (DEBUG)"], "hoa": true}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.log.Nhat', 173, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.log.mucTep', 'chong_spam', 'Mức nhật ký (tệp)', 'Trống = như màn hình.', 'chon', '', '{"luaChon": ["", "LOI", "CANH_BAO", "THONG_TIN", "GO_LOI", "ERROR", "WARN", "INFO", "DEBUG"], "luaChonTen": ["Nh\\u01b0 m\\u00e0n h\\u00ecnh", "L\\u1ed7i", "C\\u1ea3nh b\\u00e1o", "Th\\u00f4ng tin", "G\\u1ee1 l\\u1ed7i", "L\\u1ed7i (ERROR)", "C\\u1ea3nh b\\u00e1o (WARN)", "Th\\u00f4ng tin (INFO)", "G\\u1ee1 l\\u1ed7i (DEBUG)"], "hoa": true}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.log.Nhat', 174, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.log.tat', 'chong_spam', 'Loại nhật ký tắt', 'Mã loại, cách nhau dấu phẩy.', 'ds_chuoi', 'GOI3,GOI5,GOI26,VKTT,SQL,LUU_CHAR', '{"mau": "^[A-Z0-9_]{1,32}$", "doDai": 255}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.log.Nhat', 175, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.log.kiemToan', 'chong_spam', 'Loại nhật ký kiểm toán', 'Ghi riêng tệp kiem_toan (giữ lâu).', 'ds_chuoi', 'ADMIN,GM,NAP,NAP_TIEN,GIFTCODE,CAM,BAN,KINH_TE,KINHTE,CHONG_SPAM', '{"mau": "^[A-Z0-9_]{1,32}$", "doDai": 255}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.log.Nhat', 176, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chieu.learnset.bat', 'hoc_chieu', 'Học chiêu theo loài', '1 = chiêu tự học theo cấp của loài, NPC Blaziken dạy đúng loài (data_learnset). 0 = luật cũ (1 điểm kỹ năng để học, mua ô ở NPC).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HocChieuGame', 177, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('huan_luyen.bat', 'hoc_chieu', 'Điểm huấn luyện', '1 = chỉ số gốc tự tăng theo cấp + Điểm huấn luyện có trần thay 5 điểm tiềm năng tự do mỗi cấp. Đổi có hiệu lực từ lần vào game kế tiếp của mỗi Pokémon.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HocChieuGame', 178, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('huan_luyen.diem_moi_cap', 'hoc_chieu', 'Điểm huấn luyện mỗi cấp', 'Tổng = số này × (cấp − 1) + 1 / huy hiệu Gym (trần riêng). Cấp 100: 198 + 16.', 'so', '2', '{"min": 1, "max": 5}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HocChieuGame', 179, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('huan_luyen.tran_diem_thuong', 'hoc_chieu', 'Trần điểm thưởng (huy hiệu Gym)', 'Mỗi huy hiệu Gym +1 Điểm huấn luyện, tối đa số này.', 'so', '16', '{"min": 0, "max": 30}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HocChieuGame', 180, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('huan_luyen.gia_tay_phan_nghin_gio', 'hoc_chieu', 'Giá tẩy điểm (‰ giờ thu nhập)', 'Lần tẩy đầu miễn phí; sau đó = giờ thu nhập ở cấp hiện tại × số này / 1000 (hố hút). 1000 = 1 giờ.', 'so', '1000', '{"min": 0, "max": 20000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HocChieuGame', 181, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('huan_luyen.hoi_tay_gio', 'hoc_chieu', 'Chờ giữa hai lần tẩy (giờ)', 'Không áp cho lần đầu và cho Kẹo tiềm năng.', 'so', '24', '{"min": 0, "max": 720}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HocChieuGame', 182, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chi_so.bat', 'chi_so', 'Bật giới hạn chỉ số', '0 = tắt mọi trần/giảm dần chỉ số (quay về công thức cũ).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChiSoTrangBiGame', 183, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chi_so.kep_trang_bi', 'chi_so', 'Kẹp chỉ số trang bị theo ngân sách', 'Mỗi món ≤ chuẩn bậc × 1,30 × nâng cấp; đồ bậc cũ ≤ 130% đồ bậc hiện tại.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChiSoTrangBiGame', 184, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chi_so.tran_chi_mang', 'chi_so', 'Trần tỉ lệ chí mạng (‰)', 'Phần nghìn, có giảm dần. PvP cố định 300‰.', 'so', '400', '{"min": 100, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChiSoTrangBiGame', 185, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chi_so.tran_st_chi_mang', 'chi_so', 'Trần ST chí mạng cộng thêm (‰)', 'Trên nền ×1,5. 500 = đòn chí mạng tối đa ×2,0.', 'so', '500', '{"min": 0, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChiSoTrangBiGame', 186, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chi_so.tran_ne', 'chi_so', 'Trần né tránh (‰)', 'Phần nghìn, có giảm dần. PvP cố định 200‰.', 'so', '300', '{"min": 0, "max": 900}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChiSoTrangBiGame', 187, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chi_so.tran_hut', 'chi_so', 'Trần hút HP PvE (‰ sát thương)', 'Gộp mọi nguồn hút HP.', 'so', '100', '{"min": 0, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChiSoTrangBiGame', 188, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chi_so.tran_hut_pvp', 'chi_so', 'Trần hút HP PvP (‰ sát thương thật)', 'Gộp mọi nguồn hút HP, tính trên sát thương sau ×0,30.', 'so', '50', '{"min": 0, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChiSoTrangBiGame', 189, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chi_so.tran_giam_st', 'chi_so', 'Trần giảm sát thương nhận (‰)', 'Gộp mọi nguồn thụ động (PvP cố định 400‰).', 'so', '500', '{"min": 0, "max": 900}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChiSoTrangBiGame', 190, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chi_so.tran_khang_he', 'chi_so', 'Trần kháng một hệ (‰)', 'Kháng hệ + kháng mọi hệ + phiến đá.', 'so', '600', '{"min": 0, "max": 900}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChiSoTrangBiGame', 191, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chi_so.san_sat_thuong', 'chi_so', 'Sàn sát thương mỗi đòn (‰)', 'Sau mọi lớp giảm, một đòn còn ít nhất chừng này phần nghìn (chống bất tử).', 'so', '400', '{"min": 0, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChiSoTrangBiGame', 192, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chi_so.tran_phan_tram_cong', 'chi_so', 'Trần tổng % cộng thêm (‰)', 'Tổng % tấn công/phòng thủ/HP từ pet, thú cưỡi, thời trang, bộ, VIP… so với chỉ số gốc.', 'so', '500', '{"min": 0, "max": 5000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChiSoTrangBiGame', 193, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('danh_hieu.bat', 'chi_so', 'Bật danh hiệu thành tựu', '0 = ngừng tự mở khoá danh hiệu và tắt dòng chỉ số của danh hiệu đang đeo (danh hiệu đã có vẫn giữ, vẫn hiện trên đầu). Danh mục + điều kiện ở bảng data_danh_hieu.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.DanhHieuGame.bat', 194, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('danh_hieu.tran_phan_nghin', 'chi_so', 'Trần chỉ số ô danh hiệu (‰)', 'Ngân sách ô danh hiệu (thiết kế cân bằng 4.5, đề xuất 1%): chỉ một danh hiệu đang đeo được tính, giá trị từng danh hiệu (data_danh_hieu.phan_nghin) bị kẹp về trần này và vào trần tổng trang trí. 10 = +1%.', 'so', '10', '{"min": 0, "max": 30}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.DanhHieuGame.tranO', 195, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('bo.bat', 'trang_bi', 'Bật bộ trang bị cùng bậc', '4 / 7 / 11 món cùng bậc (data_noi_dung_bo): +5% HP / +5% tấn công / +3% chí mạng. 0 = tắt thưởng bộ cùng bậc (thưởng bộ nâng cấp cũ vẫn giữ).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.BoNgocGame', 196, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('bo.tran_phan_tram', 'trang_bi', 'Trần thưởng bộ HP / tấn công (%)', 'Chung cho bộ cùng bậc + bộ nâng cấp cũ (+10/+13/+15). Thiết kế: ≤ 5%.', 'so', '5', '{"min": 0, "max": 5}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.BoNgocGame', 197, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('bo.tran_chi_mang_phan_nghin', 'trang_bi', 'Trần chí mạng từ bộ (‰)', 'Bộ 11 món: +3% chí mạng, vẫn qua trần chí mạng chung 40%.', 'so', '30', '{"min": 0, "max": 30}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.BoNgocGame', 198, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('ngoc_he.bat', 'trang_bi', 'Bật ngọc hệ', 'Khảm ngọc 18 hệ vào lỗ trang bị ở Machamp: +% chỉ số món, + sát thương hệ, − kháng hệ khắc. 0 = ngọc đã khảm không còn tác dụng (vẫn giữ trên món).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.BoNgocGame', 199, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('ngoc_he.tran_phan_tram_mon', 'trang_bi', 'Trần % chỉ số món từ ngọc', 'Tổng % chỉ số chính của một món từ mọi ngọc trên món. Thiết kế: ≤ 10%.', 'so', '10', '{"min": 0, "max": 10}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.BoNgocGame', 200, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('ngoc_he.st_he_phan_van_moi_cap', 'trang_bi', 'Sát thương hệ mỗi cấp ngọc (phần vạn)', '15 = 1,5‰ mỗi cấp; cộng vào ST hệ, trần chung 20%.', 'so', '15', '{"min": 0, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.BoNgocGame', 201, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('ngoc_he.khang_tru_phan_nghin_moi_cap', 'trang_bi', 'Kháng hệ khắc bị trừ mỗi cấp ngọc (‰)', 'Phần “hại” của ngọc (Ngọc Lửa → −kháng Nước). Mài ngọc giảm một nửa.', 'so', '1', '{"min": 0, "max": 10}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.BoNgocGame', 202, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('ngoc_he.gia_thao_phan_nghin_gio', 'trang_bi', 'Giá tháo ngọc (‰ giờ thu nhập)', '500 = 0,5 giờ thu nhập ở cấp người chơi. Ngọc giữ cấp, vào túi ngọc.', 'so', '500', '{"min": 0, "max": 5000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.BoNgocGame', 203, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('ngoc_he.gia_mai_phan_nghin_gio', 'trang_bi', 'Giá mài ngọc (‰ giờ thu nhập)', '300 = 0,3 giờ thu nhập.', 'so', '300', '{"min": 0, "max": 5000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.BoNgocGame', 204, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('che_tao.bat', 'trang_bi', 'Bật chế tạo', 'Machamp thợ rèn: công thức data_noi_dung_che_tao, bậc hiếm data_che_tao_bac.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.CheTaoGame', 205, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('che_tao.pity', 'trang_bi', 'Pity chế tạo Sử thi (lần)', 'Lần thứ N liên tiếp không ra Sử thi trở lên thì chắc Sử thi. 0 = tắt.', 'so', '20', '{"min": 0, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.CheTaoGame', 206, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('nang_cap.moc_neo', 'trang_bi', 'Mốc neo nâng cấp', 'Đã đạt +7 / +10 / +13 (data_nang_cap_moc) thì nâng hỏng không rơi dưới mốc. 0 = luật cũ (−1 từ +7).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.BoNgocGame', 207, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('nang_cap.he_so_xu_bac_cao', 'trang_bi', 'Hệ số xu nâng +11..+15 (‰)', 'Bù phần kỳ vọng giảm do mốc neo (thiết kế: +6%). Chỉ áp khi bật mốc neo.', 'so', '1060', '{"min": 1000, "max": 2000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.BoNgocGame', 208, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('bat.bat', 'trang_bi', 'Bật thu phục Pokémon', 'Làm Pokémon hoang dã yếu đi rồi ném bóng (Hộp PC, Kẹo loài, Pokédex “Đã bắt”). 0 = tạm khoá ném bóng / mua bóng.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.BatPokemonGame', 209, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('bat.nguong_hp_phan_tram', 'trang_bi', 'Ngưỡng HP để ném bóng (%)', 'Pokémon phải còn ≤ chừng này % HP.', 'so', '35', '{"min": 1, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.BatPokemonGame', 210, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('bat.tran_ngay', 'trang_bi', 'Trần thu phục / ngày', 'Số Pokémon bắt được tối đa mỗi người chơi mỗi ngày (chống bot).', 'so', '30', '{"min": 0, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.BatPokemonGame', 211, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('bat.tran_nem_ngay', 'trang_bi', 'Trần ném bóng / ngày', 'Số lần ném tối đa mỗi ngày.', 'so', '150', '{"min": 0, "max": 5000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.BatPokemonGame', 212, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('bat.cach_ms', 'trang_bi', 'Nhịp ném tối thiểu (ms)', 'Hai lần ném cách nhau ít nhất chừng này (gói đến sớm hơn bị bỏ qua).', 'so', '1500', '{"min": 300, "max": 60000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.BatPokemonGame', 213, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('bat.so_lan_bo_chay', 'trang_bi', 'Số lần trượt thì Pokémon bỏ chạy', 'Mỗi lần trượt trên cùng một con: lần sau cao hơn (+25% tỉ lệ, tối đa +5%); đến lần này thì nó bỏ chạy.', 'so', '4', '{"min": 1, "max": 10}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.BatPokemonGame', 214, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('bat.su_kien', 'trang_bi', 'Mở loài chỉ bắt trong sự kiện', '1 = cho bắt các loài data_bat_loai.chi_su_kien = 1 (bật trong thời gian sự kiện).', 'co', '0', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.BatPokemonGame', 215, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('bat.dong_hanh', 'trang_bi', 'Bạn đồng hành đi theo (chỉ hình)', 'Một Pokémon trong Hộp PC đi theo người chơi (0 chỉ số). 0 = ẩn.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.BatPokemonGame', 216, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('bat.giu_moi_loai', 'trang_bi', 'Số con thường giữ mỗi loài', 'Bắt thêm con thường khi Hộp PC đã có chừng này con cùng loài: con kém nhất (IV thấp) tự chuyển đổi thành Kẹo (+ xu nhỏ). Sắc màu không bao giờ tự chuyển. 0 = tắt.', 'so', '2', '{"min": 0, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.BatPokemonGame', 217, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('bat.chuyen_doi.tran_xu_phan_nghin_gio', 'trang_bi', 'Trần xu chuyển đổi / ngày (‰ giờ thu nhập)', 'Xu nhận từ Chuyển đổi con trùng loài mỗi ngày tối đa chừng này phần nghìn giờ thu nhập (vượt: chỉ ra Kẹo).', 'so', '300', '{"min": 0, "max": 5000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.BatPokemonGame', 218, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('bat.chuyen_doi.tran_luong_ngay', 'trang_bi', 'Trần lượng khoá chuyển đổi / ngày', 'Lượng khoá tối đa mỗi ngày từ Chuyển đổi loài Rất hiếm.', 'so', '1', '{"min": 0, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.BatPokemonGame', 219, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('trao_doi.bat', 'trang_bi', 'Bật trao đổi Pokémon', 'Hai người chơi trao đổi Pokémon trong Hộp PC (khoá + xác nhận hai lần, đổi nguyên tử). 0 = tạm khoá.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TraoDoiPokemonGame', 220, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('trao_doi.cap_toi_thieu', 'trang_bi', 'Cấp tối thiểu trao đổi Pokémon', 'Cả hai bên phải đạt cấp này (ngoài cổng chống clone chung).', 'so', '10', '{"min": 1, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TraoDoiPokemonGame', 221, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('trao_doi.tran_ngay', 'trang_bi', 'Trần Pokémon nhận qua trao đổi / ngày', 'Số Pokémon mỗi người chơi nhận tối đa mỗi ngày qua trao đổi.', 'so', '10', '{"min": 0, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TraoDoiPokemonGame', 222, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('trao_doi.khoa_gio', 'trang_bi', 'Khoá trao đổi lại (giờ)', 'Pokémon vừa nhận qua trao đổi phải chờ chừng này giờ mới trao đổi tiếp (chống chuyền tay).', 'so', '24', '{"min": 0, "max": 720}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TraoDoiPokemonGame', 223, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('trao_doi.cach_giay', 'trang_bi', 'Nhịp giữa hai lần trao đổi (giây)', 'Mỗi người chơi chỉ trao đổi xong một lần mỗi chừng này giây.', 'so', '60', '{"min": 0, "max": 86400}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TraoDoiPokemonGame', 224, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('trao_doi.phi_phan_nghin_gio', 'trang_bi', 'Phí trao đổi mỗi Pokémon (‰ giờ thu nhập)', 'Người nhận trả cho mỗi con: chừng này phần nghìn giờ thu nhập ở cấp mình × bậc hiếm (1/2/4/8) × sắc màu 4 × 3 sao 2 (hố hút xu).', 'so', '50', '{"min": 0, "max": 5000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TraoDoiPokemonGame', 225, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('trao_doi.phi_tran_phan_nghin_gio', 'trang_bi', 'Trần phí trao đổi mỗi Pokémon (‰ giờ thu nhập)', 'Phí một con không vượt chừng này phần nghìn giờ thu nhập.', 'so', '2000', '{"min": 0, "max": 20000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TraoDoiPokemonGame', 226, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('cho_pokemon.bat', 'trang_bi', 'Bật chợ Pokémon', 'Bán / mua Pokémon trong Hộp PC giữa người chơi (khung giá máy chủ, ký quỹ, mua nguyên tử). 0 = tạm khoá.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChoPokemonGame', 227, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('cho_pokemon.thue_phan_tram', 'trang_bi', 'Thuế chợ Pokémon (%)', 'Đốt khi bán được; luôn ≥ thuế chợ ký gửi (thue.cho).', 'so', '8', '{"min": 0, "max": 50}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChoPokemonGame', 228, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('cho_pokemon.phi_dang_phan_tram', 'trang_bi', 'Phí đăng bán Pokémon (%)', 'Đốt, không hoàn (huỷ / hết hạn cũng mất); tối thiểu 1 xu.', 'so', '1', '{"min": 0, "max": 20}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChoPokemonGame', 229, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('cho_pokemon.cap_toi_thieu', 'trang_bi', 'Cấp tối thiểu chợ Pokémon', 'Người bán và người mua phải đạt cấp này (ngoài cổng chống clone chung).', 'so', '20', '{"min": 1, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChoPokemonGame', 230, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('cho_pokemon.toi_da_dang', 'trang_bi', 'Số tin bán Pokémon tối đa / người bán', 'Số Pokémon đang bán cùng lúc tối đa.', 'so', '3', '{"min": 0, "max": 50}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChoPokemonGame', 231, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('cho_pokemon.mua_ngay', 'trang_bi', 'Số Pokémon mua tối đa / ngày', 'Mỗi người mua tối đa chừng này Pokémon ở chợ mỗi ngày.', 'so', '5', '{"min": 0, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChoPokemonGame', 232, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('cho_pokemon.khoa_gio', 'trang_bi', 'Khoá bán lại (giờ)', 'Pokémon nhận qua trao đổi / chợ phải chờ chừng này giờ mới bán được.', 'so', '72', '{"min": 0, "max": 720}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChoPokemonGame', 233, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('cho_pokemon.han_gio', 'trang_bi', 'Hạn tin bán Pokémon (giờ)', 'Hết hạn: Pokémon về Hộp PC người bán, phí đăng không hoàn.', 'so', '48', '{"min": 1, "max": 720}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChoPokemonGame', 234, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('cho_pokemon.he_so_sac_mau', 'trang_bi', 'Hệ số khung giá sắc màu', 'Nhân cả sàn lẫn trần khung giá cho Pokémon sắc màu.', 'so', '3', '{"min": 1, "max": 20}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChoPokemonGame', 235, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('cho_pokemon.he_so_3_sao', 'trang_bi', 'Hệ số khung giá 3 sao', 'Nhân cả sàn lẫn trần khung giá cho Pokémon cá thể 3 sao.', 'so', '2', '{"min": 1, "max": 20}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChoPokemonGame', 236, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('pvp.tinh_diem.bat', 'pvp', 'Tính điểm thang PvP', '0 = trận PvP không cộng/trừ điểm thang.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ThangPvpGame', 237, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('gym.bat', 'pvp', 'Thách đấu Gym', '0 = tạm đóng Gym.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.GymLauDai', 238, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('ham_nguc.bat', 'pvp', 'Hầm Ngục bí ẩn', '0 = không vào tầng mới (vẫn nhận thưởng đã đạt).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.SoTayLauDai#hamNguc', 239, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('pho_ban.bat', 'pvp', 'Phó bản Hầm ngục (tổ đội 5 tầng)', '0 = không mở phòng mới (phòng đang chạy vẫn chạy hết).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.PhoBanGame', 240, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('pho_ban.luot_ngay', 'pvp', 'Phó bản: lượt vào mỗi ngày', 'Mỗi Pokémon, đếm cả lần vào lại sau khi thua. Thưởng ngày (0,2 giờ EXP + 5 lượng khoá + rương boss) vẫn chỉ 1 lần, chung với Hầm ngục bí ẩn.', 'so', '3', '{"min": 1, "max": 10}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.PhoBanLogic', 241, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('pho_ban.phut_toi_da', 'pvp', 'Phó bản: thời gian tối đa (phút)', 'Hết giờ chưa hạ boss tầng 5 thì phòng đóng, không có thưởng ngày.', 'so', '20', '{"min": 5, "max": 60}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.PhoBanLogic', 242, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('pho_ban.cap_toi_thieu', 'pvp', 'Phó bản: cấp tối thiểu', 'Mọi thành viên phải đạt cấp này; chênh cấp trong đội tối đa 10.', 'so', '20', '{"min": 1, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.PhoBanLogic', 243, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('cup.bat', 'pvp', 'Cúp PvP tuần', '0 = tạm đóng cúp (không nhận đăng ký, không ghép vòng). Tắt riêng một tuần: /admin/sukien (mã moi:cuptuan, tắt năm nay).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.CupGame', 244, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('cup.gio_vong_dau', 'pvp', 'Cúp: giờ vòng 1 (Chủ nhật)', 'Giờ Việt Nam. Mỗi 10 phút một vòng; đóng đăng ký trước vòng 1 năm phút.', 'so', '20', '{"min": 8, "max": 22}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.CupLogic', 245, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('cup.so_vong', 'pvp', 'Cúp: số vòng Thuỵ Sĩ', 'Vòng ghép tự động theo điểm; hai bên cùng thiết bị / IP / tài khoản không bao giờ gặp nhau.', 'so', '5', '{"min": 1, "max": 8}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.CupLogic', 246, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('cup.so_doi_toi_thieu', 'pvp', 'Cúp: số đội tối thiểu', 'Ít hơn số này lúc đóng đăng ký thì cúp tuần đó hoãn (không phát thưởng).', 'so', '4', '{"min": 2, "max": 64}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.CupLogic', 247, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.bat', 'tu_dong', 'Bật Tự động đánh', '0 = tạm khoá (người đang dùng bị dừng ở lần kiểm tiếp theo).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 248, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.gio_moi_ngay', 'tu_dong', 'Giờ Tự động đánh mỗi ngày', 'Miễn phí, chỉ tính lúc online và đang bật. Đặt lại lúc 00:00.', 'so', '5', '{"min": 0, "max": 24}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 249, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.gio_them_the_thang', 'tu_dong', 'Giờ cộng thêm khi có thẻ tháng', 'Cộng vào giờ mỗi ngày khi thẻ tháng còn hạn.', 'so', '2', '{"min": 0, "max": 24}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 250, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.chi_map_da_mo', 'tu_dong', 'Chỉ ở bãi luyện tập', '1 = chỉ bật ở bản đồ có Pokémon hoang dã đã mở (không ở thành phố, nhà, sàn đấu, sự kiện).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 251, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.tat_khi_pvp', 'tu_dong', 'Dừng khi PvP / sàn đấu / Gym / boss', '1 = dừng khi bị Pokémon khác tấn công, bật PK, vào sàn đấu / Gym / chiến trường hay đánh boss.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 252, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.tu_dung_thuoc', 'tu_dong', 'Cho tự uống thuốc', '0 = Tự động đánh không tự uống thuốc hồi phục (người chơi tự bấm).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 253, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.ban_kinh', 'tu_dong', 'Bán kính tối đa (px)', 'Pokémon chỉ đánh quanh điểm bật trong bán kính này; ra xa hơn (+64 px) thì dừng. 1 ô = 16 px.', 'so', '240', '{"min": 48, "max": 960, "buoc": 16}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 254, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.nhip_danh', 'tu_dong', 'Nhịp đánh tối đa khi tự động', 'số lệnh đánh / giây (vd 12/5). Vượt thì bỏ lệnh và tính vi phạm.', 'nhip', '12/5', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 255, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.nhip_thuoc', 'tu_dong', 'Nhịp uống thuốc tối đa khi tự động', '', 'nhip', '4/5', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 256, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.nhip_nhat', 'tu_dong', 'Nhịp nhặt đồ tối đa khi tự động', '', 'nhip', '8/5', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 257, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.nhip_nguoi', 'tu_dong', 'Nhịp đánh nghi bot (mọi người chơi)', 'Chỉ ghi nghi vấn (hack.txt), không chặn.', 'nhip', '30/5', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 258, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.vi_pham_tat', 'tu_dong', 'Số vi phạm nhịp thì dừng', 'Dừng Tự động đánh + ghi nghi vấn.', 'so', '20', '{"min": 1, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 259, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.hoi_sinh_moi_gio', 'tu_dong', 'Tự hồi sinh tối đa / giờ', 'Khi đang tự động mà Pokémon gục: chỉ dùng vật phẩm hồi sinh nếu người chơi bật "Tự hồi sinh", tối đa ngần này lần / 60 phút; còn lại dừng tự động và về Trung tâm Pokémon. 0 = không bao giờ.', 'so', '2', '{"min": 0, "max": 20}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 260, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.ke_hoach', 'tu_dong', 'Máy chủ điều khiển từng bước', '1 = máy chủ chọn mục tiêu, chiêu, thuốc, nhặt đồ, rút lui cho client 0.9.2+ (client chỉ thực hiện). 0 = client tự chọn như bản 0.9.1 (máy chủ vẫn giới hạn giờ / nhịp / vùng).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 261, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.cap_toi_thieu', 'tu_dong', 'Cấp tối thiểu để bật', '0 = mọi cấp. Dưới cấp này bấm Bắt đầu thì được báo rõ cấp cần.', 'so', '0', '{"min": 0, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 262, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.chenh_cap_tren', 'tu_dong', 'Bỏ Pokémon cao hơn quá N cấp', 'Không đánh Pokémon hoang dã cao hơn cấp mình quá ngần này. -1 = không giới hạn.', 'so', '5', '{"min": -1, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 263, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.chenh_cap_duoi', 'tu_dong', 'Bỏ Pokémon thấp hơn quá N cấp', '0 = không giới hạn dưới (vẫn đánh Pokémon yếu).', 'so', '0', '{"min": 0, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 264, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.danh_boss', 'tu_dong', 'Cho đánh boss', '0 = không chọn boss / Pokémon huyền thoại làm mục tiêu (đánh trúng boss vẫn dừng theo tu_dong.tat_khi_pvp).', 'co', '0', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 265, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.hp_quai_toi_da_pt', 'tu_dong', 'Bỏ Pokémon quá trâu (% HP mình)', 'Bỏ Pokémon có HP tối đa lớn hơn ngần này % HP tối đa của mình. 0 = tắt.', 'so', '500', '{"min": 0, "max": 100000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 266, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.chon_chieu_theo_he', 'tu_dong', 'Chọn chiêu theo khắc hệ', '1 = điểm chiêu = sát thương × khắc hệ × STAB, bỏ chiêu đang hồi / thiếu PP. 0 = chỉ theo sát thương.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 267, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.nguong_thuoc_hp', 'tu_dong', 'Ngưỡng thuốc HP mặc định (%)', 'Uống thuốc HP khi HP dưới ngưỡng người chơi chọn trong bảng; client cũ dùng số này. 0 = máy chủ không cho tự uống thuốc HP.', 'so', '50', '{"min": 0, "max": 90}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 268, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.nguong_thuoc_mp', 'tu_dong', 'Ngưỡng thuốc PP mặc định (%)', 'Như trên cho PP. 0 = tắt.', 'so', '30', '{"min": 0, "max": 90}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 269, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.nguong_hp_thap', 'tu_dong', 'HP thấp thì rút lui (%)', 'HP dưới ngưỡng mà hết thuốc: rút về điểm bắt đầu nghỉ (hoặc dừng nếu tu_dong.hp_thap_dung=1). 0 = tắt.', 'so', '25', '{"min": 0, "max": 90}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 270, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.hp_thap_dung', 'tu_dong', 'HP thấp thì dừng hẳn', '1 = dừng Tự động đánh thay vì rút lui nghỉ hồi phục.', 'co', '0', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 271, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.hoi_phuc_toi', 'tu_dong', 'Nghỉ hồi phục tới (%)', 'Rút lui nghỉ tới khi HP đạt ngần này % rồi đánh tiếp.', 'so', '80', '{"min": 10, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 272, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.nhat_ban_kinh', 'tu_dong', 'Bán kính nhặt đồ (px)', 'Chỉ đi nhặt đồ rơi của mình trong bán kính này quanh Pokémon (và trong vùng). 0 = không nhặt. 1 ô = 16 px.', 'so', '96', '{"min": 0, "max": 480, "buoc": 16}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 273, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.dung_khi_day_tui', 'tu_dong', 'Dừng khi hành trang đầy', '1 = hành trang đầy thì dừng và báo.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 274, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.dung_khi_tu_di', 'tu_dong', 'Dừng khi người chơi tự di chuyển', '1 = bấm phím di chuyển / chạm bản đồ thì dừng Tự động đánh. 0 = nhường 3 giây rồi chạy tiếp.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 275, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.tranh_pk_px', 'tu_dong', 'Tránh người bật PK (px)', 'Có người chơi khác bật PK trong bán kính này thì lùi về điểm bắt đầu chờ. 0 = tắt.', 'so', '160', '{"min": 0, "max": 960, "buoc": 16}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 276, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.map_cam', 'tu_dong', 'Bản đồ cấm Tự động đánh thêm (id)', 'Ngoài sàn đấu / Gym / chiến trường / mê cung / bản đồ boss. Cách nhau dấu phẩy; trống = không.', 'chuoi', '', '{"mau": "^[0-9, ]*$", "doDai": 255}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 277, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.hoi_sinh_tra_phi', 'tu_dong', 'Cho tự dùng vật phẩm hồi sinh trả phí', '1 = khi người chơi đồng ý riêng (bảng Tự động: Tự hồi sinh "cả vật phẩm lượng") mới được tự dùng Thuốc hồi sinh tối đa (lượng khoá). 0 = chỉ Thuốc hồi sinh thường; không bao giờ tự trả xu / lượng.', 'co', '0', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 278, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.bat', 'hoi_sinh', 'Bật cơ chế hồi sinh mới', '0 = luồng cũ (popup hồi sinh tại chỗ 5% giờ thu nhập, tự hồi sinh dưới cấp 50).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 279, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.xu_phan_tram_gio', 'tu_dong', 'Phí xu tại chỗ (% thu nhập 1 giờ)', 'Theo cấp (ThongSoLauDai.xuMoiGio). 40 = 0,4 giờ cày. Nhân hệ số gục lặp.', 'so', '40', '{"min": 0, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 280, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.xu_toi_thieu', 'tu_dong', 'Phí xu tại chỗ tối thiểu', 'Xu.', 'so', '200', '{"min": 0, "max": 100000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 281, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.he_so_lap', 'hoi_sinh', 'Hệ số gục lặp (%)', 'Lần gục thứ 1, 2, 3... trong cửa sổ; lần sau dùng số cuối (trần). Áp cho phí xu và lượng khoá.', 'ds_so', '100,150,200,300', '{"min": 0, "max": 10000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 282, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.cua_so_phut', 'hoi_sinh', 'Cửa sổ gục lặp (phút)', 'Đếm số lần gục trong ngần này phút.', 'so', '30', '{"min": 1, "max": 1440}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 283, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.luong_khoa', 'tu_dong', 'Phí lượng khoá tại chỗ', '0 = tắt lựa chọn trả lượng khoá. Nhân hệ số gục lặp (làm tròn lên).', 'so', '2', '{"min": 0, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 284, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.luong_khoa_toi_da', 'tu_dong', 'Trần phí lượng khoá', 'Lượng khoá tối đa một lần.', 'so', '10', '{"min": 0, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 285, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.cho_sau_guc_giay', 'hoi_sinh', 'Chờ sau khi gục (giây)', 'Hồi sinh tại chỗ chỉ bấm được sau ngần này giây (đếm ngược trên hộp thoại).', 'so', '5', '{"min": 0, "max": 600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 286, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.hoi_chieu_giay', 'tu_dong', 'Hồi chiêu hồi sinh tại chỗ (giây)', 'Giữa hai lần hồi sinh tại chỗ. Về Trung tâm luôn được.', 'so', '60', '{"min": 0, "max": 3600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 287, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.hp_thuoc', 'hoi_sinh', 'HP khi dùng Thuốc hồi sinh (%)', 'Thuốc hồi sinh tối đa lấy % trong data_vat_pham_lau_dai (hoi_sinh:100).', 'so', '30', '{"min": 1, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 288, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.hp_tra_phi', 'hoi_sinh', 'HP khi trả xu / lượng khoá / tân thủ (%)', '', 'so', '50', '{"min": 1, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 289, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.exp_mat_phan_nghin', 'hoi_sinh', 'Mất EXP khi về Trung tâm (phần nghìn)', 'Phần nghìn EXP lên cấp hiện tại (20 = 2%). Không bao giờ tụt cấp: chỉ trừ trong tiến độ cấp.', 'so', '20', '{"min": 0, "max": 200}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 290, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.exp_mat_toi_da_phut', 'hoi_sinh', 'Trần mất EXP (phút cày)', 'Không mất quá ngần này phút EXP đánh quái ở cấp đó. 0 = không chặn.', 'so', '15', '{"min": 0, "max": 600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 291, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.do_ben_mat_phan_tram', 'hoi_sinh', 'Mất độ bền khi về Trung tâm (%)', '% độ bền tối đa của mỗi trang bị đang mặc (không làm vỡ đồ).', 'so', '5', '{"min": 0, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 292, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.met_moi_phut', 'hoi_sinh', 'Mệt mỏi (phút)', 'Sau khi về Trung tâm. 0 = tắt.', 'so', '5', '{"min": 0, "max": 120}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 293, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.met_moi_exp', 'hoi_sinh', 'Mệt mỏi: giảm EXP (%)', 'EXP hạ quái.', 'so', '20', '{"min": 0, "max": 90}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 294, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.met_moi_tan_cong', 'hoi_sinh', 'Mệt mỏi: giảm tấn công (%)', '', 'so', '10', '{"min": 0, "max": 90}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 295, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.tan_thu_cap', 'tu_dong', 'Bảo vệ tân thủ dưới cấp', 'Dưới cấp này: không mất EXP / độ bền / Mệt mỏi, hồi sinh tại chỗ miễn phí.', 'so', '15', '{"min": 0, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 296, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.tan_thu_hoi_chieu_giay', 'hoi_sinh', 'Hồi chiêu hồi sinh miễn phí tân thủ (giây)', '', 'so', '30', '{"min": 0, "max": 3600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 297, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.dong_doi_bat', 'hoi_sinh', 'Cho chờ đồng đội hồi sinh', 'Đồng đội ở gần mang Thuốc hồi sinh được mời cứu.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 298, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.dong_doi_giay', 'hoi_sinh', 'Thời gian chờ đồng đội (giây)', '', 'so', '60', '{"min": 10, "max": 600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 299, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.cam_pvp', 'hoi_sinh', 'Cấm tại chỗ ở PvP / sàn đấu', 'Sàn đấu, giải Liên minh, chiến trường, lãnh địa đội, chiếm Gym đang đánh, đấu trường + hoi_sinh.map_pvp.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 300, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.cam_gym', 'hoi_sinh', 'Cấm tại chỗ khi thách đấu Gym', '', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 301, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.cam_boss', 'hoi_sinh', 'Cấm tại chỗ giữa trận boss', 'Đã gây sát thương vào boss thế giới / Gym còn sống cùng bản đồ.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 302, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.cam_me_cung', 'hoi_sinh', 'Cấm tại chỗ trong Mê cung', '', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 303, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.map_pvp', 'hoi_sinh', 'Bản đồ PvP (id)', 'Thêm vào luật cấm PvP (so cả id bản đồ lẫn id nạp).', 'ds_so', '202,203,204,205,227,228', '{"min": 0, "max": 100000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 304, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.map_cam_them', 'hoi_sinh', 'Bản đồ cấm tại chỗ thêm (id)', 'Cách nhau dấu phẩy; trống = không.', 'chuoi', '', '{"mau": "^[0-9, ]*$", "doDai": 255}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 305, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.auto_ve_map_up', 'hoi_sinh', 'Cho "Auto về Map Up" sau khi gục', '0 = gục thì không tự bay lại bãi luyện (chống treo máy vô hạn).', 'co', '0', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 306, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.gia_thuoc_phan_tram_gio', 'tu_dong', 'Giá Thuốc hồi sinh (% thu nhập 1 giờ)', 'Giá ở cửa hàng NPC theo cấp người mua (cùng bậc phí xu tại chỗ). 0 = giá gốc data_potion.', 'so', '30', '{"min": 0, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 307, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.gia_thuoc_toi_thieu', 'tu_dong', 'Giá Thuốc hồi sinh tối thiểu', 'Xu / viên (không thấp hơn giá gốc).', 'so', '407', '{"min": 0, "max": 100000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 308, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.thuoc_moi_gio', 'hoi_sinh', 'Hồi sinh bằng vật phẩm tối đa / giờ', 'Thuốc hồi sinh / tối đa của chính mình (tay, Tự động đánh, dùng từ túi) trong 60 phút. Đồng đội cứu không tính. 0 = không giới hạn.', 'so', '6', '{"min": 0, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 309, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.thongBao.bat', 'thong_bao', 'Gộp thông báo toàn máy chủ', '0 = gửi thẳng như cũ.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.chongspam.ThongBaoGop', 310, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.thongBao.toiDa', 'thong_bao', 'Thông báo tối đa / cửa sổ', '', 'so', '6', '{"min": 1, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ThongBaoGop', 311, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.thongBao.cuaSoGiay', 'thong_bao', 'Cửa sổ đếm thông báo (giây)', '', 'so', '60', '{"min": 1, "max": 3600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ThongBaoGop', 312, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.thongBao.moiLoai', 'thong_bao', 'Tối đa mỗi loại / cửa sổ', '', 'so', '3', '{"min": 1, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ThongBaoGop', 313, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.thongBao.trungLapGiay', 'thong_bao', 'Bỏ thông báo trùng (giây)', '', 'so', '120', '{"min": 0, "max": 86400}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ThongBaoGop', 314, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.thongBao.gopGiay', 'thong_bao', 'Gộp mỗi (giây)', '', 'so', '15', '{"min": 1, "max": 3600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ThongBaoGop', 315, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.website', 'bao_tri', 'Địa chỉ website (hiện trong game)', '', 'chuoi', 'https://huyenthoaibaoboi.fun/', '{"doDai": 200}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.website', 316, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.linkTaiGame', 'bao_tri', 'Link tải game (hiện trong game)', '', 'chuoi', 'https://huyenthoaibaoboi.fun/', '{"doDai": 200}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.linkTaiGame', 317, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.adminAccounts', 'bao_tri', 'Danh sách GM trong game', 'Tên Pokémon GM, cách nhau dấu phẩy.', 'ds_chuoi', '', '{"mau": "^[A-Za-z0-9_]{1,30}$", "doDai": 255}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.adminAccounts', 318, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.gmCanCoPInfo', 'quan_tri', 'GM cần cả cờ pInfo', '1 = quyền GM cần tên trong danh sách VÀ pInfo[16]=1.', 'co', '0', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.gmCanCoPInfo', 319, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.banToiThieu', 'bao_tri', 'Bản tối thiểu (bắt buộc cập nhật)', 'Số bản (BAN, vd 907). Client gửi số bản nhỏ hơn bị chặn đăng nhập, hiện hộp Cần cập nhật + nút Tải bản mới. Chỉ chặn nền tảng đã có bản tải đạt mức này ở trang Bản tải game. 0 = tắt.', 'so', '0', '{"min": 0, "max": 1000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.banToiThieu', 320, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.banMoiNhat', 'bao_tri', 'Bản mới nhất (dự phòng)', 'Bản mới nhất lấy theo số bản từng nền tảng ở trang Bản tải game (/admin/tai-game). Ô này chỉ dùng khi bản tải chưa ghi số bản. 0 = chỉ theo trang Bản tải game.', 'so', '0', '{"min": 0, "max": 1000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.banMoiNhat', 321, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.nhacCapNhat', 'bao_tri', 'Nhắc khi có bản mới', 'Client cũ hơn bản mới nhất thấy thẻ Đã có bản mới (Cập nhật ngay / Để sau, nhắc một lần mỗi ngày). Không chặn chơi. Cố định bật; tắt khẩn cấp: sv.nhacCapNhat=0 trong server.ini.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.nhacCapNhat', 322, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.choPhepTaoTenGM', 'quan_tri', 'Cho tạo Pokémon tên GM', 'Chỉ bật tạm thời.', 'co', '0', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'data.Database.CHO_PHEP_TAO_TEN_GM', 323, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.testMode', 'quan_tri', 'Chế độ thử (testMode)', 'Mã test cấp tài nguyên miễn phí. Khoá ở máy thật.', 'co', '0', '{}', 'ngay', 0, 1, 0, 'cau_hinh', 'server', 'TeamServer.testMode', 324, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.testCode', 'quan_tri', 'Mã test', 'Chuỗi gõ trong game khi testMode=1.', 'chuoi', '', '{"mau": "^[ -~]*$", "doDai": 64}', 'ngay', 1, 1, 0, 'cau_hinh', 'server', 'TeamServer.testCode', 325, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('cau_ca.cho_min_giay', 'kinh_te', 'Câu cá: chờ cá cắn tối thiểu (giây)', 'Sau khi thả câu, cá cắn câu sau ngẫu nhiên [tối thiểu, tối đa] giây (server quyết định).', 'so', '6', '{"min": 2, "max": 120}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.CauCaPhien', 326, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('cau_ca.cho_max_giay', 'kinh_te', 'Câu cá: chờ cá cắn tối đa (giây)', 'Nhỏ hơn tối thiểu thì dùng tối thiểu.', 'so', '15', '{"min": 2, "max": 180}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.CauCaPhien', 327, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('cau_ca.cho_them_ho_lon_giay', 'kinh_te', 'Câu cá: chờ thêm ở hồ lớn (giây)', 'Hồ lớn (có cá hiếm) chờ lâu hơn ngần này giây (cả hai đầu khoảng).', 'so', '3', '{"min": 0, "max": 60}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.CauCaPhien', 328, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('cau_ca.cua_so_ms', 'kinh_te', 'Câu cá: khung kéo cần (ms)', 'Sau dấu "Cá cắn câu!", phải kéo cần trong ngần này mili giây; sớm hoặc muộn thì cá thoát.', 'so', '1500', '{"min": 300, "max": 5000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.CauCaPhien', 329, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('cau_ca.tre_mang_ms', 'kinh_te', 'Câu cá: bù trễ mạng (ms)', 'Lệnh kéo cần tới server muộn thêm tối đa ngần này ms vẫn tính đúng lúc (bù đường truyền hai chiều).', 'so', '700', '{"min": 0, "max": 3000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.CauCaPhien', 330, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('cau_ca.trong_phan_tram', 'kinh_te', 'Câu cá: tỉ lệ không câu được gì (%)', 'Kéo đúng lúc vẫn có ngần này % không được gì (mồi và thể lực vẫn mất).', 'so', '30', '{"min": 0, "max": 90}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.CauCaPhien', 331, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('cau_ca.hoi_giay', 'kinh_te', 'Câu cá: nghỉ giữa hai lần thả (giây)', 'Sau khi một lần câu kết thúc, chờ ngần này giây mới thả câu tiếp.', 'so', '3', '{"min": 0, "max": 120}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.CauCaPhien', 332, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('cau_ca.giam_tu_lan_gio', 'kinh_te', 'Câu cá: số lần câu được mỗi giờ trước khi giảm', 'Trong một giờ đồng hồ, câu được quá ngần này lần thì tỉ lệ không được gì tăng dần (chống cày).', 'so', '30', '{"min": 1, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.CauCaPhien', 333, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('cau_ca.giam_moi_lan_phan_tram', 'kinh_te', 'Câu cá: tăng tỉ lệ trượt mỗi lần vượt (%)', 'Mỗi lần câu được vượt mốc trong giờ cộng ngần này % vào tỉ lệ không được gì.', 'so', '2', '{"min": 0, "max": 50}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.CauCaPhien', 334, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('cau_ca.trong_toi_da_phan_tram', 'kinh_te', 'Câu cá: tỉ lệ không được gì tối đa (%)', 'Trần của tỉ lệ không được gì sau khi cộng giảm theo giờ.', 'so', '75', '{"min": 0, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.CauCaPhien', 335, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('ban_do.khoa.bat', 'che_do', 'Khoá bản đồ theo cấp / cửa ải', '1 = bản đồ cần đủ cấp và xong cửa ải (bảng data_ban_do_mo_khoa) mới vào được, kèm luật Tự động đánh theo bản đồ; 0 = mở mọi bản đồ (người chơi cũ luôn được miễn trừ theo cấp).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.KhoaBanDoGame', 336, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('pvp.ngam_tay', 'pvp', 'Ngắm tay trong PvP', '1 = trận giữa người chơi (PK, thách đấu, xếp hạng, sàn đấu, chiến trường, chiếm Gym) không tự động đánh, không tự nhắm: kéo chiêu để ngắm, chiêu bay thẳng theo hướng kéo (client 0.9.8; bản cũ bắn theo hướng Pokémon đang quay). 0 = như 0.9.7.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.BanHuongGame', 337, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('pvp.ngam_tay.giay_giao_chien', 'pvp', 'Giây coi là đang giao chiến PvP', 'Đánh trúng / bị người chơi đánh trúng trong ngần này giây thì vẫn tính là trận giữa người chơi (tắt tự động, bắt buộc ngắm tay).', 'so', '10', '{"min": 0, "max": 600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.BanHuongGame', 338, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('pvp.ngam_tay.bu_tre_ms', 'pvp', 'Bù trễ vị trí (ms)', 'Vị trí mục tiêu trong ngần này ms trước lúc đạn tới đều tính trúng (ưu tiên người bắn khi mạng trễ). Trần 300.', 'so', '150', '{"min": 0, "max": 300}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.BanHuongGame', 339, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('pvp.ngam_tay.map', 'pvp', 'Bản đồ PvP bắt buộc ngắm tay (id)', 'Ngoài sàn đấu / chiến trường / giải Liên minh / thách đấu / chiếm Gym đang đánh (tự nhận theo loại bản đồ). So cả id bản đồ lẫn id nạp.', 'ds_so', '202,204,205,227', '{"min": 0, "max": 100000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.BanHuongGame', 340, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('dat_ten_pet.bat', 'dat_ten_pet', 'Bật thẻ đặt tên Pokémon', 'Khẩn cấp (server.ini): 0 = không dùng / không bán thẻ (dòng đã có vẫn tính, vẫn trong trần).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.DatTenPetGame', 341, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('dat_ten_pet.gia_luong', 'dat_ten_pet', 'Giá thẻ (lượng)', 'Cửa hàng lượng (mục Tiện ích). Bản kiếm được: đổi Tem nghiên cứu / lượng khoá ở Sổ tay (data_cua_hang_lau_dai).', 'so', '100', '{"min": 10, "max": 5000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.DatTenPetGame.giaLuong', 342, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('dat_ten_pet.mua_tuan_toi_da', 'dat_ten_pet', 'Số thẻ mua bằng lượng / tuần', 'Mỗi Pokémon, mở lại thứ Hai. 0 = tạm ngừng bán. Giới hạn số lần quay lại dòng bằng tiền.', 'so', '3', '{"min": 0, "max": 20}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.NapTienGame.mua', 343, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('dat_ten_pet.muc_phan_nghin', 'dat_ten_pet', 'Các mức của dòng (‰)', 'Tăng dần, mỗi mức ≤ trần một dòng. 10 = +1%. Chí mạng / né nhân thêm hệ số của bảng data_dong_the_ten.', 'ds_so', '10,15,20,30', '{"min": 1, "max": 30}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.DatTenPet.quay', 344, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('dat_ten_pet.trong_so_muc', 'dat_ten_pet', 'Trọng số từng mức', 'Cùng số phần tử với các mức, KHÔNG tăng (mức thấp luôn ra nhiều hơn). Sai luật thì máy chủ dùng mặc định 55,30,12,3.', 'ds_so', '55,30,12,3', '{"min": 1, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.DatTenPet.mucHopLe', 345, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('dat_ten_pet.tran_dong_phan_nghin', 'dat_ten_pet', 'Trần một dòng thẻ tên (‰)', 'Ngân sách ô thẻ tên pet. 30 = +3%.', 'so', '30', '{"min": 0, "max": 60}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.DatTenPetGame.tranDong', 346, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('trang_tri.tran_tong_phan_nghin', 'dat_ten_pet', 'Trần tổng chỉ số trang trí (‰)', 'Mọi ô trang trí cộng lại (thiết kế cân bằng 4.5), nằm trong chi_so.tran_phan_tram_cong. 50 = 5%.', 'so', '50', '{"min": 0, "max": 50}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.DatTenPetGame.tranTrangTri', 347, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('dat_ten_pet.do_dai_toi_thieu', 'dat_ten_pet', 'Tên pet: ít nhất (ký tự)', '', 'so', '2', '{"min": 1, "max": 12}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.DatTenPet.kiemTen', 348, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('dat_ten_pet.do_dai_toi_da', 'dat_ten_pet', 'Tên pet: nhiều nhất (ký tự)', 'Cột tob_pet_dat_ten.ten dài 40.', 'so', '12', '{"min": 2, "max": 20}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.DatTenPet.kiemTen', 349, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('dat_ten_pet.ban_client_popup', 'dat_ten_pet', 'Bản client có hộp kết quả thẻ tên', 'Client có số bản ≥ giá trị này nhận gói -106 (thẻ trắng kiểu Pokémon GO); cũ hơn / Unity nhận hộp thông báo 37.', 'so', '908', '{"min": 0, "max": 100000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.DatTenPetGame.baoKetQua', 350, 'co_dinh', 1);

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
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('hoc_chieu', 'Học chiêu & Điểm huấn luyện (cố định)', 'Chiêu học theo loài (bảng data_learnset), Điểm huấn luyện thay điểm tiềm năng tự do (data_huan_luyen, data_tang_truong_dong). Số liệu trong bảng dữ liệu; ở đây chỉ có công tắc và giá tẩy điểm.', 14, 1);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('chi_so', 'Chỉ số & chống lạm phát (cố định)', 'Trần + giảm dần cho chỉ số trang bị, pet, thú cưỡi, skin (docs/thiet-ke-game.md).', 15, 1);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('trang_bi', 'Trang bị, ngọc hệ, chế tạo & thu phục (cố định)', 'Bộ cùng bậc, ngọc hệ, chế tạo, mốc neo nâng cấp, thu phục Pokémon (docs/thiet-ke-game.md 4.2, 4.3, 7.7).', 16, 1);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('hoi_sinh', 'Hồi sinh — luật (cố định)', 'Phạt khi về Trung tâm, mệt mỏi, cấm hồi sinh tại chỗ ở PvP / Gym / boss / Mê cung.', 17, 1);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('thong_bao', 'Thông báo (cố định)', 'Gộp thông báo toàn máy chủ.', 18, 1);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('quan_tri', 'Quản trị & thử nghiệm (cố định)', 'Cờ GM cũ và khoá thử nghiệm (máy thật luôn mặc định).', 19, 1);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('dat_ten_pet', 'Thẻ đặt tên Pokémon', 'Giá bán (lượng), số lần mua / tuần và tỉ lệ các mức của dòng chỉ số ngẫu nhiên. Trần một dòng và trần tổng trang trí 5% là cố định. Bảng chỉ số trong data_dong_the_ten, từ cấm trong data_tu_cam_ten.', 20, 1);

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
              'moi:congdong','moi:shiny','moi:dotkich','moi:muapvp','moi:pokedex','moi:chienvung','moi:bungphat',
              'moi:cuptuan');

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
(0,'Y tá Chansey','Trung tâm Pokémon: hồi phục, bán thuốc','Chansey',113,0,'0,43,45,47,49,51,54,56,59,60,62,70,80,100,281'),
(1,'Kecleon tạp hoá','Tạp hoá','Kecleon',352,1,'0,45,47,49,54,56,62,70,72,80,82,102'),
(2,'Machamp thợ rèn','Thợ rèn: chế tạo, sửa, khảm đồ','Machamp',68,2,'0,45,70,73,80,83,103'),
(3,'Bronzong thợ giáp','Bán trang bị','Bronzong',437,3,'0,47,55,59,70,74,80,84,104'),
(4,'Arcanine gác cổng','Gác cổng','Arcanine',59,4,'0,70,80,201,281'),
(5,'Trưởng làng Xatu','Trưởng làng, giao nhiệm vụ','Xatu',178,5,'0,43,45,50,56,59,60,70,80,101,163,164,280,282'),
(6,'Ngài Persian','Bán đá quý, nguyên liệu','Persian',53,6,'0,53,70,79,80,89,109'),
(7,'Pidgeot chở bay','Chở bay tới bản đồ khác','Pidgeot',18,7,'0,3,4,5,6,7,8,9,54,70,80'),
(8,'Ông Snorlax','Giao nhiệm vụ','Snorlax',143,8,'0,51,57,60,70,80,105,160'),
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
(225,'Lễ kết đôi 1','','dau_truong','Đấu trường'),
(226,'Lễ kết đôi 2','','dau_truong','Đấu trường'),
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
(2541,'Tổ Zapdos','','hang','Hang động'),
(4381,'Phó bản Hầm ngục','','hang','Phòng riêng của tổ đội (5 tầng)'),
(4382,'Sàn Cúp PvP','','dau_truong','Sàn đấu riêng của trận cúp tuần')
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
UPDATE `data_noi_dung_chieu` SET `vai_tro` = REPLACE(`vai_tro`, 'Múa kiếm', 'Vũ điệu kiếm')
 WHERE `vai_tro` LIKE '%Múa kiếm%';

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
(9,18,'CA-04.2','Cửa ải: Vượt bờ biển Fuchsia',1,'','Cinnabar'),
(10,27,'CA-05.3','Cửa ải: Đường sang Johto',1,'','New Bark'),
(11,29,'CA-05.3','Cửa ải: Đường sang Johto',1,'','Rừng Viridian'),
(12,0,'','',0,'Khu an toàn, không có Pokémon hoang dã để tự đánh.','Khu mua bán'),
(13,33,'CA-06.2','Cửa ải: Rừng Viridian sâu thẳm',1,'','Núi Mặt Trăng'),
(14,37,'CA-06.2','Cửa ải: Rừng Viridian sâu thẳm',1,'','Vùng Safari'),
(15,40,'CA-07.2','Cửa ải: Leo Núi Mặt Trăng',1,'','Cherrygrove'),
(17,20,'','',0,'Khu sự kiện / phó bản: không dùng Tự động đánh.','Khu mỏ'),
(18,49,'CA-08.3','Cửa ải: Bóng tối Cherrygrove',1,'','Goldenrod'),
(19,53,'CA-09.2','Cửa ải: Phố lớn Goldenrod',1,'','Ecruteak'),
(20,53,'CA-09.2','Cửa ải: Phố lớn Goldenrod',1,'','Violet'),
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
(225,0,'','',0,'Lễ kết đôi: không có Pokémon hoang dã để tự đánh.','Lễ kết đôi 1'),
(226,0,'','',0,'Lễ kết đôi: không có Pokémon hoang dã để tự đánh.','Lễ kết đôi 2'),
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
(2541,0,'','',0,'Hang boss: boss phải đánh tay.','Tổ Zapdos'),
(4381,0,'','',0,'Phó bản: tổ đội phải tự điều khiển.','Phó bản Hầm ngục'),
(4382,0,'','',0,'Đấu trường / PvP: phải tự điều khiển.','Sàn Cúp PvP');
-- 0.9.8 (ban-098, ra soat huong dan / chuoi chinh): buoc giet quai chuoi chinh C09 / C16 / C19 / C31 mo truoc ban do
-- cua quai 1..4 cap -> ha cap_toi_thieu cho khop (chi dong con gia tri cu, giu dong admin da sua). Test KiemKhoaBanDo.
UPDATE `data_ban_do_mo_khoa` SET `cap_toi_thieu` = 18 WHERE `map_id` = 9 AND `cap_toi_thieu` = 19;
UPDATE `data_ban_do_mo_khoa` SET `cap_toi_thieu` = 29 WHERE `map_id` = 11 AND `cap_toi_thieu` = 30;
UPDATE `data_ban_do_mo_khoa` SET `cap_toi_thieu` = 33 WHERE `map_id` = 13 AND `cap_toi_thieu` = 35;
UPDATE `data_ban_do_mo_khoa` SET `cap_toi_thieu` = 53 WHERE `map_id` = 20 AND `cap_toi_thieu` = 57;
-- cua ai mo hai ban do do (buoc Gym) cung ha theo: cap cua ai <= cap ban do (KiemKhoaBanDo)
UPDATE `data_quest_lau_dai` SET `cap_toi_thieu` = 18, `exp` = 60890, `xu` = 707 WHERE `ma` = 'CA-04.2' AND `cap_toi_thieu` = 19; -- thuong theo ngan sach cap 18
UPDATE `data_quest_lau_dai` SET `cap_toi_thieu` = 33, `exp` = 196816, `xu` = 1417 WHERE `ma` = 'CA-06.2' AND `cap_toi_thieu` = 35; -- thuong theo ngan sach cap 33

INSERT INTO `data_quest_lau_dai` (`ma`,`id_so`,`loai`,`nam`,`vung`,`cung`,`chuong`,`buoc`,`ten`,`npc_giao`,`ten_npc`,`dia_danh`,`cap_toi_thieu`,`ngay_mo`,`truoc`,`kieu_muc_tieu`,`muc_tieu_id`,`muc_tieu_ten`,`so_luong`,`quai_roi`,`lap_lai`,`gioi_han`,`exp`,`xu`,`luong_khoa`,`luong`,`vat_pham`,`huy_hieu`,`dong_co_tuong_thich`,`ghi_chu`) VALUES
('CA-01.1',7001,'cua_ai',1,'','Rời đồng cỏ Pallet','CA-01','1','Cửa ải: 20 Torchic ở Pallet','','','Pallet',3,0,'','giet','4','Torchic',20,'','','',3161,384,0,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-01.2',7002,'cua_ai',1,'','Rời đồng cỏ Pallet','CA-01','2','Cửa ải: 20 Ekans ở Viridian','','','Viridian',3,0,'CA-01.1','giet','5','Ekans',20,'','','',3161,384,1,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-02.1',7003,'cua_ai',1,'','Vượt hồ Vermilion','CA-02','1','Cửa ải: 25 Oddish ở Vermilion','','','Vermilion',8,0,'','giet','10','Oddish',25,'','','',13776,472,0,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-02.2',7004,'cua_ai',1,'','Vượt hồ Vermilion','CA-02','2','Cửa ải: 25 Venonat ở Cerulean','','','Cerulean',8,0,'CA-02.1','giet','8','Venonat',25,'','','',13776,472,1,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-03.1',7005,'cua_ai',1,'','Thử thách Pewter','CA-03','1','Cửa ải: 30 Quagsire ở Lavender','','','Lavender',10,0,'','giet','13','Quagsire',30,'','','',24984,499,0,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-03.2',7006,'cua_ai',1,'','Thử thách Pewter','CA-03','2','Cửa ải: hạ Gym Pewter','','','',12,0,'CA-03.1','ha_gym','1','Trưởng Gym Pewter: Golem',1,'','','',30877,557,1,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-04.1',7007,'cua_ai',1,'','Vượt bờ biển Fuchsia','CA-04','1','Cửa ải: 40 Growlithe ở Fuchsia','','','Fuchsia',17,0,'','giet','28','Growlithe',40,'','','',52484,699,0,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-04.2',7008,'cua_ai',1,'','Vượt bờ biển Fuchsia','CA-04','2','Cửa ải: hạ Gym Cerulean','','','',18,0,'CA-04.1','ha_gym','2','Trưởng Gym Cerulean: Gyarados',1,'','','',60890,707,1,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-05.1',7009,'cua_ai',1,'','Đường sang Johto','CA-05','1','Cửa ải: 45 Croagunk ở Cinnabar','','','Cinnabar',24,0,'','giet','30','Croagunk',45,'','','',97831,947,0,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-05.2',7010,'cua_ai',1,'','Đường sang Johto','CA-05','2','Cửa ải: 30 Gastly ở Hang động','','','Hang động',24,0,'CA-05.1','giet','25','Gastly',30,'','','',97831,947,0,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-05.3',7011,'cua_ai',1,'','Đường sang Johto','CA-05','3','Cửa ải: hạ Gym Vermilion','','','',24,0,'CA-05.2','ha_gym','3','Trưởng Gym Vermilion: Luxray',1,'','','',97831,947,1,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-06.1',7012,'cua_ai',1,'','Rừng Viridian sâu thẳm','CA-06','1','Cửa ải: 50 Crawdaunt ở Rừng Viridian','','','Rừng Viridian',33,0,'','giet','35','Crawdaunt',50,'','','',188754,1426,0,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
('CA-06.2',7013,'cua_ai',1,'','Rừng Viridian sâu thẳm','CA-06','2','Cửa ải: hạ Gym Celadon','','','',33,0,'CA-06.1','ha_gym','4','Trưởng Gym Celadon: Vileplume',1,'','','',196816,1417,1,0,'','',0,'Cửa ải mở bản đồ (khoá bản đồ)'),
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

-- ======================================================================================================================
-- 10. main 1a705be6 (0.9.7): giu nguyen van ban cac tep cu (kem chu thich / khoi [..]) tru lenh danh muc cau hinh
--     (cau_hinh_danh_muc / cau_hinh_nhom nam o phan 2) va SET NAMES. Moi tep mo dau bang dong "-- >>> <ten tep>"
--     (test doc tung phan: test/DayDu.phanNangCap).
-- ======================================================================================================================
-- >>> sua_096_1_logic.sql
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

-- =====================================================================================================================
-- [cau-ca-nhip] Nhịp câu cá (chủ game 0.9.6.1: câu quá nhanh, phải có lúc không được gì)
-- ---------------------------------------------------------------------------------------------------------------------
-- Server quyết định: thả câu ("cauca2") -> chờ ngẫu nhiên 6..15 s (hồ lớn +3 s) -> "Cá cắn câu!" -> kéo cần ("keocan")
-- trong 1,5 s (+0,7 s bù trễ mạng) -> 30% không được gì (tăng 2%/lần khi câu được quá 30 lần trong một giờ, trần 75%)
-- -> nghỉ 3 s mới thả tiếp. Kéo sớm / muộn / rời chỗ = cá thoát; mồi (xu) + thể lực (2/lần, 100/ngày) mất ngay lúc thả.
-- Client cũ ("cauca") server tự kéo khi cá cắn, cùng thời gian chờ, thêm 15% cá thoát. Số liệu = khoá cau_ca.* trong
-- tools/cauhinh/danh_muc.json (nguồn duy nhất, sinh sql/pokemon_cau_hinh_trung_tam.sql); khối này đăng ký danh mục cho
-- web quản trị /admin/cau-hinh-he-thong (máy chủ có mặc định sẵn, thiếu dòng vẫn chạy). Không ghi giá trị vào cau_hinh.
-- Cần bảng cau_hinh_danh_muc dạng 0.9.4+ (pokemon_cau_hinh_trung_tam.sql / máy chủ tự tạo). test/KiemCauCaNhip kiểm.
-- [/cau-ca-nhip]
-- >>> sua_pvp_pk.sql
-- =====================================================================================================
-- PvP XẾP HẠNG + LUẬT PK (0.9.7): nâng cấp IDEMPOTENT (MariaDB 10.3, utf8), tự chứa, nối được vào sql/nang_cap.sql.
--
--   tob_pvp_tran    mỗi trận đấu xếp hạng (real.XepHangPvpGame): hợp lệ (cộng/trừ Elo) hay huỷ + lý do
--                   (qua_ngan, khong_danh_tra, cung_tai_khoan_thiet_bi_ip, roi_tran, het_gio) - đối chiếu nghi vấn cày Elo.
--   tob_pvp_thuong  thưởng cuối mùa đã phát, khoá (mua, id_char): INSERT IGNORE trước rồi mới phát -> không phát hai lần
--                   kể cả khi máy chủ khởi động lại giữa chừng.
-- Elo mùa vẫn ở tob_pvp_mua (data.DatabaseLauDai tự tạo). Máy chủ cũng tự tạo hai bảng này lúc khởi động.
-- Danh hiệu thưởng mùa (thuong_mua_great / _ultra / _master) nằm trong sql/them_danh_hieu.sql.
-- =====================================================================================================

CREATE TABLE IF NOT EXISTS `tob_pvp_tran` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `mua` INT NOT NULL,
  `thang` INT NOT NULL,
  `thua` INT NOT NULL,
  `hop_le` TINYINT NOT NULL DEFAULT 0,
  `ly_do` VARCHAR(40) NOT NULL DEFAULT '',
  `giay` INT NOT NULL DEFAULT 0,
  `elo_thang` INT NOT NULL DEFAULT 0,
  `elo_thua` INT NOT NULL DEFAULT 0,
  `luc` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_pvpt_luc` (`luc`),
  KEY `idx_pvpt_thang` (`thang`),
  KEY `idx_pvpt_thua` (`thua`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `tob_pvp_thuong` (
  `mua` INT NOT NULL,
  `id_char` INT NOT NULL,
  `bac` TINYINT NOT NULL DEFAULT 0,
  `elo` INT NOT NULL DEFAULT 0,
  `so_tran` INT NOT NULL DEFAULT 0,
  `luong_khoa` INT NOT NULL DEFAULT 0,
  `danh_hieu` INT NOT NULL DEFAULT 0,
  `luc` DATETIME NOT NULL,
  PRIMARY KEY (`mua`, `id_char`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
-- >>> them_cap_nhat_client.sql
-- ============================================================================
-- CAP NHAT CLIENT (0.9.7, ban 907) - chay lai bao nhieu lan cung duoc (MariaDB 10.3+, utf8mb4).
--
--   mysql -u root -p gamedb7 < sql/them_cap_nhat_client.sql
--
-- 1. Danh muc cau hinh trung tam: 3 khoa cua com.htbb.capnhat.CapNhatClient (ban sinh tu tools/cauhinh/danh_muc.json,
--    giong sql/pokemon_cau_hinh_trung_tam.sql - chay tep do cung duoc):
--      sv.banToiThieu  ban toi thieu: client gui so ban nho hon bi CHAN dang nhap (hop "Can cap nhat"), 0 = tat
--      sv.banMoiNhat   ban moi nhat DU PHONG khi ban tai chua ghi so ban, 0 = chi theo trang Ban tai game
--      sv.nhacCapNhat  nhac "Da co ban moi" (co dinh 1; tat khan cap trong server.ini)
--    Gia tri dat o web /admin/cau-hinh-he-thong (bang cau_hinh) - tep nay KHONG dat gia tri nao.
-- 2. Danh muc tai game (web_cai_dat khoa 'taiGame'): them truong "ban" = 0 cho moi ban tai chua co (so ban cua tep dang
--    phat; admin dien o /admin/tai-game khi phat hanh). Khong dong cham gia tri da co. Chua co bang / khoa thi khong lam gi.
-- ============================================================================


CREATE TABLE IF NOT EXISTS `web_cai_dat` (
  `khoa`         varchar(64)  NOT NULL,
  `gia_tri`      mediumtext   NOT NULL,
  `cap_nhat_luc` datetime     NOT NULL DEFAULT current_timestamp(),
  `cap_nhat_boi` varchar(50)  NOT NULL DEFAULT '',
  PRIMARY KEY (`khoa`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

UPDATE `web_cai_dat` SET `gia_tri` = JSON_SET(`gia_tri`, '$.ban[0].ban', 0) WHERE `khoa` = 'taiGame' AND JSON_VALID(`gia_tri`) AND JSON_EXTRACT(`gia_tri`, '$.ban[0].ma') IS NOT NULL AND JSON_EXTRACT(`gia_tri`, '$.ban[0].ban') IS NULL;
UPDATE `web_cai_dat` SET `gia_tri` = JSON_SET(`gia_tri`, '$.ban[1].ban', 0) WHERE `khoa` = 'taiGame' AND JSON_VALID(`gia_tri`) AND JSON_EXTRACT(`gia_tri`, '$.ban[1].ma') IS NOT NULL AND JSON_EXTRACT(`gia_tri`, '$.ban[1].ban') IS NULL;
UPDATE `web_cai_dat` SET `gia_tri` = JSON_SET(`gia_tri`, '$.ban[2].ban', 0) WHERE `khoa` = 'taiGame' AND JSON_VALID(`gia_tri`) AND JSON_EXTRACT(`gia_tri`, '$.ban[2].ma') IS NOT NULL AND JSON_EXTRACT(`gia_tri`, '$.ban[2].ban') IS NULL;
UPDATE `web_cai_dat` SET `gia_tri` = JSON_SET(`gia_tri`, '$.ban[3].ban', 0) WHERE `khoa` = 'taiGame' AND JSON_VALID(`gia_tri`) AND JSON_EXTRACT(`gia_tri`, '$.ban[3].ma') IS NOT NULL AND JSON_EXTRACT(`gia_tri`, '$.ban[3].ban') IS NULL;
UPDATE `web_cai_dat` SET `gia_tri` = JSON_SET(`gia_tri`, '$.ban[4].ban', 0) WHERE `khoa` = 'taiGame' AND JSON_VALID(`gia_tri`) AND JSON_EXTRACT(`gia_tri`, '$.ban[4].ma') IS NOT NULL AND JSON_EXTRACT(`gia_tri`, '$.ban[4].ban') IS NULL;
UPDATE `web_cai_dat` SET `gia_tri` = JSON_SET(`gia_tri`, '$.ban[5].ban', 0) WHERE `khoa` = 'taiGame' AND JSON_VALID(`gia_tri`) AND JSON_EXTRACT(`gia_tri`, '$.ban[5].ma') IS NOT NULL AND JSON_EXTRACT(`gia_tri`, '$.ban[5].ban') IS NULL;
UPDATE `web_cai_dat` SET `gia_tri` = JSON_SET(`gia_tri`, '$.ban[6].ban', 0) WHERE `khoa` = 'taiGame' AND JSON_VALID(`gia_tri`) AND JSON_EXTRACT(`gia_tri`, '$.ban[6].ma') IS NOT NULL AND JSON_EXTRACT(`gia_tri`, '$.ban[6].ban') IS NULL;
UPDATE `web_cai_dat` SET `gia_tri` = JSON_SET(`gia_tri`, '$.ban[7].ban', 0) WHERE `khoa` = 'taiGame' AND JSON_VALID(`gia_tri`) AND JSON_EXTRACT(`gia_tri`, '$.ban[7].ma') IS NOT NULL AND JSON_EXTRACT(`gia_tri`, '$.ban[7].ban') IS NULL;
-- >>> them_danh_hieu.sql
-- =====================================================================================================
-- DANH HIỆU (0.9.7): danh mục danh hiệu + điều kiện mở khoá + bộ đếm theo nhân vật + nhật ký.
-- Nâng cấp IDEMPOTENT (MariaDB 10.3, utf8): chạy lại bao nhiêu lần cũng được; tự chứa, nối được vào sql/nang_cap.sql.
--
--   data_danh_hieu_do_hiem  4 độ hiếm (Thường / Hiếm / Sử thi / Huyền thoại); màu = bậc chung MỘT nguồn
--                           tools/cauhinh/do_hiem.json (game: ChuDe.mauDanhHieu; web; phần them_do_hiem.sql ghi lại).
--   data_danh_hieu          mọi danh hiệu: id = id hiệu ứng (Char.idEffDanhHieu, tob_danh_hieu.iddh), tên, mô tả
--                           (điều kiện viết cho người chơi), gợi ý (danh hiệu ẩn), độ hiếm, hình, một dòng chỉ số nhỏ
--                           (chi_so + phan_nghin, kẹp về trần danh_hieu.tran_phan_nghin = 10‰ = 1%), điều kiện
--                           (dieu_kien + tham_so + so_luong, xem com.htbb.pokemon.DanhHieuLogic), an (0 hiện,
--                           1 ẩn "???" tới khi mở, 2 chỉ hiện khi đang có), thoi_han_phut (0 = vĩnh viễn), nguồn.
--   tob_danh_hieu_dem       bộ đếm theo nhân vật (câu cá, gục, bản đồ đã đến, danh hiệu mới chưa xem...).
--   tob_danh_hieu_log       nhật ký mở khoá.
--
-- Sở hữu + hạn dùng + danh hiệu đang đeo vẫn ở chỗ cũ (tob_danh_hieu.iddh "id,hết hạn,..." + tob_char dữ liệu 103).
-- Dải id: 8767..8999 danh hiệu cũ (giftcode / sự kiện, an = 2), 9600..9615 danh hiệu sinh thái, 9700.. danh hiệu
-- thành tựu, 10000.. danh hiệu phần thưởng nhiệm vụ (284 tên trong data_nhiem_vu_noi_dung, trước đây chỉ là chữ).
--
-- Chạy lại: tên / mô tả / điều kiện được cập nhật theo tệp này; chi_so, phan_nghin và bat KHÔNG bị ghi đè (chủ game
-- chỉnh cân bằng trực tiếp trong CSDL). Máy chủ đọc lại khi nạp nội dung (khởi động).
-- =====================================================================================================

CREATE TABLE IF NOT EXISTS `data_danh_hieu_do_hiem` (
  `id` TINYINT NOT NULL,
  `ten` VARCHAR(32) NOT NULL DEFAULT '',
  `mau` VARCHAR(7) NOT NULL DEFAULT '',
  `thu_tu` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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

CREATE TABLE IF NOT EXISTS `tob_danh_hieu_dem` (
  `id_char` INT NOT NULL,
  `khoa` VARCHAR(80) NOT NULL,
  `gia_tri` BIGINT NOT NULL DEFAULT 0,
  `tap` TEXT NULL,
  PRIMARY KEY (`id_char`, `khoa`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `tob_danh_hieu_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `id_char` INT NOT NULL,
  `id_dh` INT NOT NULL,
  `hanh_dong` VARCHAR(16) NOT NULL DEFAULT '',
  `luc` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_dhl_char` (`id_char`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `data_danh_hieu_do_hiem` (`id`,`ten`,`mau`,`thu_tu`) VALUES
(0,'Thường','#5F6B7A',0),(1,'Hiếm','#23803A',1),(2,'Sử thi','#7B45D6',2),(3,'Huyền thoại','#B95A0E',3)
ON DUPLICATE KEY UPDATE `ten`=VALUES(`ten`), `mau`=VALUES(`mau`), `thu_tu`=VALUES(`thu_tu`);

-- 46 danh hiệu thành tựu / xếp hạng / sự kiện / ẩn (Thường 5‰, Hiếm 7‰, Sử thi 9‰, Huyền thoại 10‰ = 1%).
INSERT INTO `data_danh_hieu` (`id`,`ma`,`ten`,`mo_ta`,`goi_y`,`do_hiem`,`hinh`,`chi_so`,`phan_nghin`,`dieu_kien`,`tham_so`,`so_luong`,`an`,`thoi_han_phut`,`nguon`,`thu_tu`,`bat`) VALUES
(9700,'cap_10','Huấn luyện viên tập sự','Đạt cấp 10.','',0,'sao','hp',5,'cap','',10,0,0,'thanh_tuu',100,1),
(9701,'cap_30','Huấn luyện viên lành nghề','Đạt cấp 30.','',0,'tang','ne_tranh',5,'cap','',30,0,0,'thanh_tuu',101,1),
(9702,'cap_50','Huấn luyện viên tinh anh','Đạt cấp 50.','',1,'nhanh','chi_mang',7,'cap','',50,0,0,'thanh_tuu',102,1),
(9703,'cap_70','Bậc thầy huấn luyện','Đạt cấp 70.','',2,'khien','khang_trang_thai',9,'cap','',70,0,0,'thanh_tuu',103,1),
(9704,'cap_91','Đỉnh cao huấn luyện','Đạt cấp tối đa của năm đầu (91).','',3,'mat_troi','hp',10,'cap','',91,0,0,'thanh_tuu',104,1),
(9705,'dex_10','Nhà nghiên cứu tập sự','Nghiên cứu (hạ) 10% số loài trong Pokédex.','',0,'la','ne_tranh',5,'pokedex_phan_tram','',10,0,0,'pokedex',105,1),
(9706,'dex_25','Trợ lý Giáo sư','Nghiên cứu 25% số loài trong Pokédex.','',1,'la','khang_trang_thai',7,'pokedex_phan_tram','',25,0,0,'pokedex',106,1),
(9707,'dex_50','Nhà Pokédex học','Nghiên cứu 50% số loài trong Pokédex.','',2,'sao','chi_mang',9,'pokedex_phan_tram','',50,0,0,'pokedex',107,1),
(9708,'dex_90','Người hoàn thiện Pokédex','Nghiên cứu 90% số loài trong Pokédex.','',3,'sao','hp',10,'pokedex_phan_tram','',90,0,0,'pokedex',108,1),
(9709,'boss_1','Kẻ thách thức trùm','Lần đầu góp sức hạ một trùm thế giới.','',0,'no','chi_mang',5,'boss_the_gioi','',1,0,0,'boss',109,1),
(9710,'boss_25','Thợ săn trùm','Góp sức hạ 25 trùm thế giới.','',1,'nanh','chi_mang',7,'boss_the_gioi','',25,0,0,'boss',110,1),
(9711,'boss_100','Khắc tinh huyền thoại','Góp sức hạ 100 trùm thế giới.','',2,'no','hp',9,'boss_the_gioi','',100,0,0,'boss',111,1),
(9712,'ca_50','Cần thủ nghiệp dư','Câu cá 50 lần ở hồ.','',0,'giot','khang_trang_thai',5,'cau_ca','',50,0,0,'thanh_tuu',112,1),
(9713,'ca_300','Ngư phủ Magikarp','Câu cá 300 lần ở hồ.','',1,'song','ne_tranh',7,'cau_ca','',300,0,0,'thanh_tuu',113,1),
(9714,'ca_1000','Vua Gyarados','Câu cá 1.000 lần ở hồ.','Mặt hồ chỉ trả lời người đủ kiên nhẫn...',2,'song','hp',9,'cau_ca','',1000,1,0,'an',114,1),
(9715,'pvp_10','Huấn luyện viên tân binh','Thắng 10 trận đấu xếp hạng (NPC Đấu trường).','',0,'vuot','chi_mang',5,'thang_pvp','',10,0,0,'pvp',115,1),
(9716,'pvp_100','Huấn luyện viên dày dạn','Thắng 100 trận đấu xếp hạng.','',1,'vuot','ne_tranh',7,'thang_pvp','',100,0,0,'pvp',116,1),
(9717,'pvp_500','Chiến thần đấu trường','Thắng 500 trận đấu xếp hạng.','',2,'set','chi_mang',9,'thang_pvp','',500,0,0,'pvp',117,1),
(9718,'mua_great','Great Ball mùa này','Đạt bậc Great Ball ở thang PvP mùa hiện tại. Giữ bậc để gia hạn.','',1,'khien','ne_tranh',7,'pvp_bac','',1,0,43200,'xep_hang',118,1),
(9719,'mua_ultra','Ultra Ball mùa này','Đạt bậc Ultra Ball ở thang PvP mùa hiện tại. Giữ bậc để gia hạn.','',2,'khien','khang_trang_thai',9,'pvp_bac','',2,0,43200,'xep_hang',119,1),
(9720,'mua_master','Master Ball mùa này','Đạt bậc Master Ball ở thang PvP mùa hiện tại. Giữ bậc để gia hạn.','',3,'khien','chi_mang',10,'pvp_bac','',3,0,43200,'xep_hang',120,1),
(9721,'top_pvp_10','Top 10 Nhà vô địch','Nằm trong top 10 thang PvP mùa hiện tại. Rời top thì danh hiệu hết hạn sau 7 ngày.','',3,'mat_troi','ne_tranh',10,'top_pvp','',10,0,10080,'xep_hang',121,1),
(9722,'gym_1','Huy hiệu đầu tiên','Có huy hiệu Gym đầu tiên.','',0,'khien','khang_trang_thai',5,'gym_huy_hieu','',1,0,0,'nhiem_vu',122,1),
(9723,'gym_8','Người sưu tầm huy hiệu','Có 8 huy hiệu Gym.','',2,'khien','ne_tranh',9,'gym_huy_hieu','',8,0,0,'nhiem_vu',123,1),
(9724,'gym_16','Nhà vô địch hai vùng','Có đủ 16 huy hiệu Gym (Kanto + Hoenn).','',3,'mat_troi','khang_trang_thai',10,'gym_huy_hieu','',16,0,0,'nhiem_vu',124,1),
(9725,'nc_7','Thợ rèn +7','Nâng một món trang bị lên +7.','',1,'thep','hp',7,'thanh_tuu_nang_cap','',7,0,0,'thanh_tuu',125,1),
(9726,'nc_10','Thợ rèn +10','Nâng một món trang bị lên +10.','',2,'thep','chi_mang',9,'thanh_tuu_nang_cap','',10,0,0,'thanh_tuu',126,1),
(9727,'nc_15','Bàn tay vàng +15','Nâng một món trang bị lên +15.','',3,'thep','hp',10,'thanh_tuu_nang_cap','',15,0,0,'thanh_tuu',127,1),
(9728,'ham_nguc_5','Kẻ chinh phục Hầm ngục','Vượt tầng 5 Hầm ngục bí ẩn.','',1,'ma','khang_trang_thai',7,'tang_hang','',5,0,0,'thanh_tuu',128,1),
(9729,'che_tao_50','Nhà giả kim','Chế tạo 50 lần.','',1,'cong','khang_trang_thai',7,'che_tao','',50,0,0,'thanh_tuu',129,1),
(9730,'kham_100','Thợ khảm ngọc','Khảm ngọc 100 lần.','',1,'bong','chi_mang',7,'kham_ngoc','',100,0,0,'thanh_tuu',130,1),
(9731,'giup_100','Đồng đội tận tâm','Cùng tổ đội hạ 100 quái với người chơi dưới cấp 20.','',1,'tim','hp',7,'giup_tan_thu','',100,0,0,'thanh_tuu',131,1),
(9732,'dang_nhap_30','Người bạn trung thành','Đăng nhập 30 ngày.','',0,'tim','hp',5,'dang_nhap','',30,0,0,'thanh_tuu',132,1),
(9733,'gym_chiem','Trụ cột chiếm Gym','Tham gia chiếm Gym 20 lần.','',1,'khien','hp',7,'tham_gia_gym','',20,0,0,'su_kien',133,1),
(9734,'halloween','Bí ngô đêm hội','Có mặt trong lễ Halloween. Hết hạn sau 30 ngày.','',1,'ma','ne_tranh',7,'su_kien','halloween',1,0,43200,'su_kien',134,1),
(9735,'noel','Tuần lộc tuyết trắng','Có mặt trong mùa Giáng sinh. Hết hạn sau 30 ngày.','',1,'sao','khang_trang_thai',7,'su_kien','noel',1,0,43200,'su_kien',135,1),
(9736,'trung_thu','Clefable đêm rằm','Có mặt trong lễ Trung thu. Hết hạn sau 30 ngày.','',1,'mat_troi','hp',7,'su_kien','trungthu',1,0,43200,'su_kien',136,1),
(9737,'shiny_1','Ánh sáng lấp lánh','Gặp một Pokémon shiny.','Có những Pokémon mang màu khác thường...',1,'sao','chi_mang',7,'pokedex_shiny','',1,1,0,'an',137,1),
(9738,'guc_100','Kẻ không bỏ cuộc','Gục ngã 100 lần mà vẫn đứng dậy.','Ngã nhiều lần cũng là một kiểu bản lĩnh.',0,'tim','hp',5,'guc','',100,1,0,'an',138,1),
(9739,'map_50','Kẻ lang thang','Đặt chân tới 50 bản đồ khác nhau.','Cứ đi, đường sẽ tự mở.',1,'loc','ne_tranh',7,'tap:den_ban_do','',50,1,0,'an',139,1),
(9740,'map_120','Người đi khắp thế gian','Đặt chân tới 120 bản đồ khác nhau.','Đã đặt chân tới mọi miền chưa?',3,'loc','ne_tranh',10,'tap:den_ban_do','',120,1,0,'an',140,1),
(9741,'giet_10000','Cơn bão đồng cỏ','Hạ 10.000 Pokémon hoang dã.','Đồng cỏ nào cũng nhớ tên bạn.',2,'loc','chi_mang',9,'giet','',10000,1,0,'an',141,1),
(9742,'alpha_50','Thợ săn Alpha','Hạ 50 Pokémon Alpha.','Con đầu đàn to hơn thường lệ...',2,'nanh','hp',9,'ha_alpha','',50,1,0,'an',142,1),
(9743,'thuong_mua_great','Great Ball mùa trước','Thưởng cuối mùa: kết thúc mùa PvP ở bậc Great Ball (ít nhất 10 trận xếp hạng). Giữ một mùa.','',1,'khien','ne_tranh',7,'trao','',1,0,131040,'xep_hang',143,1),
(9744,'thuong_mua_ultra','Ultra Ball mùa trước','Thưởng cuối mùa: kết thúc mùa PvP ở bậc Ultra Ball (ít nhất 10 trận xếp hạng). Giữ một mùa.','',2,'khien','khang_trang_thai',9,'trao','',1,0,131040,'xep_hang',144,1),
(9745,'thuong_mua_master','Master Ball mùa trước','Thưởng cuối mùa: kết thúc mùa PvP ở bậc Master Ball (ít nhất 10 trận xếp hạng). Giữ một mùa.','',3,'mat_troi','chi_mang',10,'trao','',1,0,131040,'xep_hang',145,1)
ON DUPLICATE KEY UPDATE `ma`=VALUES(`ma`), `ten`=VALUES(`ten`), `mo_ta`=VALUES(`mo_ta`), `goi_y`=VALUES(`goi_y`), `do_hiem`=VALUES(`do_hiem`), `hinh`=VALUES(`hinh`), `dieu_kien`=VALUES(`dieu_kien`), `tham_so`=VALUES(`tham_so`), `so_luong`=VALUES(`so_luong`), `an`=VALUES(`an`), `thoi_han_phut`=VALUES(`thoi_han_phut`), `nguon`=VALUES(`nguon`), `thu_tu`=VALUES(`thu_tu`);

-- 16 danh hiệu hệ sinh thái (HoSoSinhThai.DANH_HIEU, đã có từ trước; nay đeo qua bảng chung).
INSERT INTO `data_danh_hieu` (`id`,`ma`,`ten`,`mo_ta`,`goi_y`,`do_hiem`,`hinh`,`chi_so`,`phan_nghin`,`dieu_kien`,`tham_so`,`so_luong`,`an`,`thoi_han_phut`,`nguon`,`thu_tu`,`bat`) VALUES
(9600,'sinh_thai_00','Nhiếp ảnh tập sự','Danh hiệu hệ sinh thái: Nhiếp ảnh tập sự (Sưu tầm sinh thái).','',0,'sao','ne_tranh',5,'sinh_thai','Nhiếp ảnh tập sự',1,0,0,'sinh_thai',300,1),
(9601,'sinh_thai_01','Thợ săn khoảnh khắc','Danh hiệu hệ sinh thái: Thợ săn khoảnh khắc (Sưu tầm sinh thái).','',1,'sao','chi_mang',7,'sinh_thai','Thợ săn khoảnh khắc',1,0,0,'sinh_thai',301,1),
(9602,'sinh_thai_02','Nhà sinh thái học','Danh hiệu hệ sinh thái: Nhà sinh thái học (Sưu tầm sinh thái).','',1,'la','khang_trang_thai',7,'sinh_thai','Nhà sinh thái học',1,0,0,'sinh_thai',302,1),
(9603,'sinh_thai_03','Huyền thoại ống kính','Danh hiệu hệ sinh thái: Huyền thoại ống kính (Sưu tầm sinh thái).','',3,'sao','hp',10,'sinh_thai','Huyền thoại ống kính',1,0,0,'sinh_thai',303,1),
(9604,'sinh_thai_04','Ngư phủ bậc thầy','Danh hiệu hệ sinh thái: Ngư phủ bậc thầy (Sưu tầm sinh thái).','',2,'giot','ne_tranh',9,'sinh_thai','Ngư phủ bậc thầy',1,0,0,'sinh_thai',304,1),
(9605,'sinh_thai_05','Nhà khảo cổ','Danh hiệu hệ sinh thái: Nhà khảo cổ (Sưu tầm sinh thái).','',1,'thep','chi_mang',7,'sinh_thai','Nhà khảo cổ',1,0,0,'sinh_thai',305,1),
(9606,'sinh_thai_06','Đầu bếp Poké','Danh hiệu hệ sinh thái: Đầu bếp Poké (Sưu tầm sinh thái).','',0,'lua','khang_trang_thai',5,'sinh_thai','Đầu bếp Poké',1,0,0,'sinh_thai',306,1),
(9607,'sinh_thai_07','Dược sư','Danh hiệu hệ sinh thái: Dược sư (Sưu tầm sinh thái).','',0,'cong','hp',5,'sinh_thai','Dược sư',1,0,0,'sinh_thai',307,1),
(9608,'sinh_thai_08','Nhà vườn xanh','Danh hiệu hệ sinh thái: Nhà vườn xanh (Sưu tầm sinh thái).','',0,'la','ne_tranh',5,'sinh_thai','Nhà vườn xanh',1,0,0,'sinh_thai',308,1),
(9609,'sinh_thai_09','Nhà nhân giống','Danh hiệu hệ sinh thái: Nhà nhân giống (Sưu tầm sinh thái).','',1,'tim','chi_mang',7,'sinh_thai','Nhà nhân giống',1,0,0,'sinh_thai',309,1),
(9610,'sinh_thai_10','Thợ săn shiny','Danh hiệu hệ sinh thái: Thợ săn shiny (Sưu tầm sinh thái).','',2,'sao','khang_trang_thai',9,'sinh_thai','Thợ săn shiny',1,0,0,'sinh_thai',310,1),
(9611,'sinh_thai_11','Vua tốc độ','Danh hiệu hệ sinh thái: Vua tốc độ (Sưu tầm sinh thái).','',1,'nhanh','hp',7,'sinh_thai','Vua tốc độ',1,0,0,'sinh_thai',311,1),
(9612,'sinh_thai_12','Thánh đố Pokémon','Danh hiệu hệ sinh thái: Thánh đố Pokémon (Sưu tầm sinh thái).','',1,'set','ne_tranh',7,'sinh_thai','Thánh đố Pokémon',1,0,0,'sinh_thai',312,1),
(9613,'sinh_thai_13','Nhà vô địch giải tuần','Danh hiệu hệ sinh thái: Nhà vô địch giải tuần (Sưu tầm sinh thái).','',2,'khien','chi_mang',9,'sinh_thai','Nhà vô địch giải tuần',1,0,0,'sinh_thai',313,1),
(9614,'sinh_thai_14','Nhà thám hiểm vùng','Danh hiệu hệ sinh thái: Nhà thám hiểm vùng (Sưu tầm sinh thái).','',1,'loc','khang_trang_thai',7,'sinh_thai','Nhà thám hiểm vùng',1,0,0,'sinh_thai',314,1),
(9615,'sinh_thai_15','Bạn của muôn loài','Danh hiệu hệ sinh thái: Bạn của muôn loài (Sưu tầm sinh thái).','',2,'tim','hp',9,'sinh_thai','Bạn của muôn loài',1,0,0,'sinh_thai',315,1)
ON DUPLICATE KEY UPDATE `ma`=VALUES(`ma`), `ten`=VALUES(`ten`), `mo_ta`=VALUES(`mo_ta`), `goi_y`=VALUES(`goi_y`), `do_hiem`=VALUES(`do_hiem`), `hinh`=VALUES(`hinh`), `dieu_kien`=VALUES(`dieu_kien`), `tham_so`=VALUES(`tham_so`), `so_luong`=VALUES(`so_luong`), `an`=VALUES(`an`), `thoi_han_phut`=VALUES(`thoi_han_phut`), `nguon`=VALUES(`nguon`), `thu_tu`=VALUES(`thu_tu`);

-- 48 danh hiệu cũ (data.DanhHieu, giftcode / sự kiện / chiếm Gym): chỉ hiện khi đang có (an = 2), trao tay.
-- Chỉ số 5‰ = mức Thường của danh hiệu kiếm được: món trao qua giftcode / gói nạp không bao giờ vượt món tự cày.
INSERT INTO `data_danh_hieu` (`id`,`ma`,`ten`,`mo_ta`,`goi_y`,`do_hiem`,`hinh`,`chi_so`,`phan_nghin`,`dieu_kien`,`tham_so`,`so_luong`,`an`,`thoi_han_phut`,`nguon`,`thu_tu`,`bat`) VALUES
(8767,'cu_8767','Anh hùng','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','ne_tranh',5,'trao','',1,2,0,'cu',500,1),
(8768,'cu_8768','Bá chủ','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','chi_mang',5,'trao','',1,2,0,'cu',501,1),
(8769,'cu_8769','Tối thượng','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','khang_trang_thai',5,'trao','',1,2,0,'cu',502,1),
(8770,'cu_8770','Đại gia','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','hp',5,'trao','',1,2,0,'cu',503,1),
(8771,'cu_8771','Đệ nhất','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','ne_tranh',5,'trao','',1,2,0,'cu',504,1),
(8772,'cu_8772','Tứ Thiên Vương','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','chi_mang',5,'trao','',1,2,0,'cu',505,1),
(8773,'cu_8773','Nhà thám hiểm','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','khang_trang_thai',5,'trao','',1,2,0,'cu',506,1),
(8774,'cu_8774','Người dũng cảm','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','hp',5,'trao','',1,2,0,'cu',507,1),
(8775,'cu_8775','Huyền thoại','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','ne_tranh',5,'trao','',1,2,0,'cu',508,1),
(8776,'cu_8776','Bậc thầy Pokémon','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','chi_mang',5,'trao','',1,2,0,'cu',509,1),
(8777,'cu_8777','Uy phong','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','khang_trang_thai',5,'trao','',1,2,0,'cu',510,1),
(8778,'cu_8778','Vô địch','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','hp',5,'trao','',1,2,0,'cu',511,1),
(8779,'cu_8779','Nhà vô địch Pokémon','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','ne_tranh',5,'trao','',1,2,0,'cu',512,1),
(8785,'cu_8785','Tia Mặt Trời','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','chi_mang',5,'trao','',1,2,0,'cu',513,1),
(8786,'cu_8786','Có một không hai','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','khang_trang_thai',5,'trao','',1,2,0,'cu',514,1),
(8787,'cu_8787','ACE','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','hp',5,'trao','',1,2,0,'cu',515,1),
(8788,'cu_8788','Fan cứng','Trao ngẫu nhiên cho người chơi online lâu (1 ngày).','',1,'sao','ne_tranh',5,'trao','',1,2,0,'cu',516,1),
(8900,'cu_8900','Angle Of Death','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','chi_mang',5,'trao','',1,2,0,'cu',517,1),
(8901,'cu_8901','Bất Chấp Cuộc Chơi','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','khang_trang_thai',5,'trao','',1,2,0,'cu',518,1),
(8902,'cu_8902','Huyền Thoại Bất Diệt','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','hp',5,'trao','',1,2,0,'cu',519,1),
(8903,'cu_8903','Rayquaza Chấn Thiên','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','ne_tranh',5,'trao','',1,2,0,'cu',520,1),
(8904,'cu_8904','Kẻ Thách Thức Định Mệnh','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','chi_mang',5,'trao','',1,2,0,'cu',521,1),
(8905,'cu_8905','Gươm Aegislash','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','khang_trang_thai',5,'trao','',1,2,0,'cu',522,1),
(8906,'cu_8906','Nhà Vô Địch Mọi Vùng','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','hp',5,'trao','',1,2,0,'cu',523,1),
(8907,'cu_8907','Vô Địch Mọi Vùng','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','ne_tranh',5,'trao','',1,2,0,'cu',524,1),
(8908,'cu_8908','Bảo Vật Bất Bại','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','chi_mang',5,'trao','',1,2,0,'cu',525,1),
(8909,'cu_8909','Bá Chủ Một Vùng','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','khang_trang_thai',5,'trao','',1,2,0,'cu',526,1),
(8910,'cu_8910','Tung Hoành Mọi Vùng','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','hp',5,'trao','',1,2,0,'cu',527,1),
(8911,'cu_8911','Tinh Anh Tuyệt Đỉnh','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','ne_tranh',5,'trao','',1,2,0,'cu',528,1),
(8924,'cu_8924','Độc Nhất Vô Nhị','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','chi_mang',5,'trao','',1,2,0,'cu',529,1),
(8925,'cu_8925','Tia Chớp Phá Trời','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','khang_trang_thai',5,'trao','',1,2,0,'cu',530,1),
(8935,'cu_8935','Vinh Danh Đội','Trao cho Đội chiếm Gym (có hạn).','',1,'sao','hp',5,'trao','',1,2,0,'cu',531,1),
(8956,'cu_8956','Người Được Chọn','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','ne_tranh',5,'trao','',1,2,0,'cu',532,1),
(8957,'cu_8957','Nhà Vô Địch Pokémon 2','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','chi_mang',5,'trao','',1,2,0,'cu',533,1),
(8983,'cu_8983','Huyền Thoại Bất Diệt 2','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','khang_trang_thai',5,'trao','',1,2,0,'cu',534,1),
(8984,'cu_8984','Truyền Nhân Huyền Thoại','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','hp',5,'trao','',1,2,0,'cu',535,1),
(8985,'cu_8985','Sức Mạnh Hủy Diệt','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','ne_tranh',5,'trao','',1,2,0,'cu',536,1),
(8986,'cu_8986','Kinh Tài Tuyệt Diễm','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','chi_mang',5,'trao','',1,2,0,'cu',537,1),
(8987,'cu_8987','Nữ Thần Tuyệt Sắc','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','khang_trang_thai',5,'trao','',1,2,0,'cu',538,1),
(8988,'cu_8988','Phong Vân Tùy Bút','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','hp',5,'trao','',1,2,0,'cu',539,1),
(8989,'cu_8989','Lão Làng Pokémon','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','ne_tranh',5,'trao','',1,2,0,'cu',540,1),
(8990,'cu_8990','Số Một Mọi Vùng','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','chi_mang',5,'trao','',1,2,0,'cu',541,1),
(8991,'cu_8991','Vô Song Mọi Vùng','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','khang_trang_thai',5,'trao','',1,2,0,'cu',542,1),
(8992,'cu_8992','Tung Hoành Mọi Vùng','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','hp',5,'trao','',1,2,0,'cu',543,1),
(8993,'cu_8993','Có Một Không Hai','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','ne_tranh',5,'trao','',1,2,0,'cu',544,1),
(8994,'cu_8994','Vinh Hoa Phú Qúy','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','chi_mang',5,'trao','',1,2,0,'cu',545,1),
(8995,'cu_8995','Chúa Tể Mọi Vùng','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','khang_trang_thai',5,'trao','',1,2,0,'cu',546,1),
(8996,'cu_8996','Tuyệt Thế Giai Nhân','Danh hiệu được trao (giftcode, sự kiện, chiếm Gym).','',1,'sao','hp',5,'trao','',1,2,0,'cu',547,1)
ON DUPLICATE KEY UPDATE `ma`=VALUES(`ma`), `ten`=VALUES(`ten`), `mo_ta`=VALUES(`mo_ta`), `goi_y`=VALUES(`goi_y`), `do_hiem`=VALUES(`do_hiem`), `hinh`=VALUES(`hinh`), `dieu_kien`=VALUES(`dieu_kien`), `tham_so`=VALUES(`tham_so`), `so_luong`=VALUES(`so_luong`), `an`=VALUES(`an`), `thoi_han_phut`=VALUES(`thoi_han_phut`), `nguon`=VALUES(`nguon`), `thu_tu`=VALUES(`thu_tu`);

-- 284 danh hiệu phần thưởng nhiệm vụ (thuong_khac / nhanh_lua_chon "danh_hieu:<tên>" của data_nhiem_vu_noi_dung):
-- trước đây chỉ ghi chữ vào tiến độ nhiệm vụ, nay đeo được. Nhiệm vụ ẩn -> danh hiệu ẩn. 3‰ / 5‰ / 7‰.
INSERT INTO `data_danh_hieu` (`id`,`ma`,`ten`,`mo_ta`,`goi_y`,`do_hiem`,`hinh`,`chi_so`,`phan_nghin`,`dieu_kien`,`tham_so`,`so_luong`,`an`,`thoi_han_phut`,`nguon`,`thu_tu`,`bat`) VALUES
(10000,'nv_000','Người ngay thẳng','Phần thưởng nhiệm vụ C02.6: Chọn một con đường.','',1,'sao','ne_tranh',5,'nhiem_vu','Người ngay thẳng',1,0,0,'nhiem_vu',1000,1),
(10001,'nv_001','Người rộng lượng','Phần thưởng nhiệm vụ C02.6: Chọn một con đường.','',1,'la','chi_mang',5,'nhiem_vu','Người rộng lượng',1,0,0,'nhiem_vu',1001,1),
(10002,'nv_002','Bạn của Clefairy','Phần thưởng nhiệm vụ C08.6: Chọn một con đường.','',1,'tim','khang_trang_thai',5,'nhiem_vu','Bạn của Clefairy',1,0,0,'nhiem_vu',1002,1),
(10003,'nv_003','Người giữ sao','Phần thưởng nhiệm vụ C08.6: Chọn một con đường.','',1,'khien','hp',5,'nhiem_vu','Người giữ sao',1,0,0,'nhiem_vu',1003,1),
(10004,'nv_004','Người bạn dịu dàng','Phần thưởng nhiệm vụ C18.5: Chọn một con đường.','',1,'giot','ne_tranh',5,'nhiem_vu','Người bạn dịu dàng',1,0,0,'nhiem_vu',1004,1),
(10005,'nv_005','Người giữ lời hứa','Phần thưởng nhiệm vụ C18.5: Chọn một con đường.','',1,'lua','chi_mang',5,'nhiem_vu','Người giữ lời hứa',1,0,0,'nhiem_vu',1005,1),
(10006,'nv_006','Người cho cơ hội thứ hai','Phần thưởng nhiệm vụ C33.5: Chọn một con đường.','',1,'set','khang_trang_thai',5,'nhiem_vu','Người cho cơ hội thứ hai',1,0,0,'nhiem_vu',1006,1),
(10007,'nv_007','Người giữ công lý','Phần thưởng nhiệm vụ C33.5: Chọn một con đường.','',1,'nhanh','hp',5,'nhiem_vu','Người giữ công lý',1,0,0,'nhiem_vu',1007,1),
(10008,'nv_008','Người giữ lời thề rồng','Phần thưởng nhiệm vụ C48.3: Chọn một con đường.','',1,'cong','ne_tranh',5,'nhiem_vu','Người giữ lời thề rồng',1,0,0,'nhiem_vu',1008,1),
(10009,'nv_009','Người tìm sự thật','Phần thưởng nhiệm vụ C48.3: Chọn một con đường.','',1,'loc','chi_mang',5,'nhiem_vu','Người tìm sự thật',1,0,0,'nhiem_vu',1009,1),
(10010,'nv_010','Người mang giấc mơ đẹp','Phần thưởng nhiệm vụ C52.6: Chọn một con đường.','',1,'sao','khang_trang_thai',5,'nhiem_vu','Người mang giấc mơ đẹp',1,0,0,'nhiem_vu',1010,1),
(10011,'nv_011','Người phong ấn ác mộng','Phần thưởng nhiệm vụ C52.6: Chọn một con đường.','',1,'la','hp',5,'nhiem_vu','Người phong ấn ác mộng',1,0,0,'nhiem_vu',1011,1),
(10012,'nv_012','Ngôi sao của Hoenn','Phần thưởng nhiệm vụ C78.4: Chọn một con đường.','',1,'tim','ne_tranh',5,'nhiem_vu','Ngôi sao của Hoenn',1,0,0,'nhiem_vu',1012,1),
(10013,'nv_013','Người cưỡi gió trời','Phần thưởng nhiệm vụ C78.4: Chọn một con đường.','',1,'khien','chi_mang',5,'nhiem_vu','Người cưỡi gió trời',1,0,0,'nhiem_vu',1013,1),
(10014,'nv_014','Vô địch Hoenn','Phần thưởng nhiệm vụ C100.4: Chọn một con đường.','',1,'giot','khang_trang_thai',5,'nhiem_vu','Vô địch Hoenn',1,0,0,'nhiem_vu',1014,1),
(10015,'nv_015','Sứ giả ba vùng','Phần thưởng nhiệm vụ C100.4: Chọn một con đường.','',1,'lua','hp',5,'nhiem_vu','Sứ giả ba vùng',1,0,0,'nhiem_vu',1015,1),
(10016,'nv_016','Người hùng Pallet','Phần thưởng nhiệm vụ P-01.0.5: Nói chuyện với Xatu ở Pallet.','',0,'set','ne_tranh',3,'nhiem_vu','Người hùng Pallet',1,0,0,'nhiem_vu',1016,1),
(10017,'nv_017','Người bạn của Luvdisc','Phần thưởng nhiệm vụ P-01.1.4: Chọn một con đường.','',0,'nhanh','chi_mang',3,'nhiem_vu','Người bạn của Luvdisc',1,0,0,'nhiem_vu',1017,1),
(10018,'nv_018','Người đưa tin vội vã','Phần thưởng nhiệm vụ P-01.1.4: Chọn một con đường.','',0,'cong','khang_trang_thai',3,'nhiem_vu','Người đưa tin vội vã',1,0,0,'nhiem_vu',1018,1),
(10019,'nv_019','Ông tơ bà nguyệt Pallet','Phần thưởng nhiệm vụ P-01.1.4: Chọn một con đường.','',0,'loc','hp',3,'nhiem_vu','Ông tơ bà nguyệt Pallet',1,0,0,'nhiem_vu',1019,1),
(10020,'nv_020','Người bạn của Munchlax','Phần thưởng nhiệm vụ P-01.2.4: Nấu 2 Bánh quả mọng.','',0,'sao','ne_tranh',3,'nhiem_vu','Người bạn của Munchlax',1,0,0,'nhiem_vu',1020,1),
(10021,'nv_021','Người bạn của Togepi','Phần thưởng nhiệm vụ P-01.3.4: Nói chuyện với Kangaskhan ở Pallet.','',0,'la','chi_mang',3,'nhiem_vu','Người bạn của Togepi',1,0,0,'nhiem_vu',1021,1),
(10022,'nv_022','Người hùng Viridian','Phần thưởng nhiệm vụ P-02.0.5: Nói chuyện với Kecleon ở Viridian.','',0,'tim','khang_trang_thai',3,'nhiem_vu','Người hùng Viridian',1,0,0,'nhiem_vu',1022,1),
(10023,'nv_023','Người bạn của Remoraid','Phần thưởng nhiệm vụ P-02.1.4: Nói chuyện với Lapras ở Viridian.','',0,'khien','hp',3,'nhiem_vu','Người bạn của Remoraid',1,0,0,'nhiem_vu',1023,1),
(10024,'nv_024','Người bạn của Sandshrew','Phần thưởng nhiệm vụ P-02.2.4: Nói chuyện với Persian ở Viridian.','',0,'giot','ne_tranh',3,'nhiem_vu','Người bạn của Sandshrew',1,0,0,'nhiem_vu',1024,1),
(10025,'nv_025','Người hùng Rừng Viridian','Phần thưởng nhiệm vụ P-03.0.4: Quan sát Oddish lúc bình minh.','',0,'lua','chi_mang',3,'nhiem_vu','Người hùng Rừng Viridian',1,0,0,'nhiem_vu',1025,1),
(10026,'nv_026','Người bạn của Smeargle','Phần thưởng nhiệm vụ P-03.1.4: Giao Tấm ảnh cho Meowth.','',0,'set','khang_trang_thai',3,'nhiem_vu','Người bạn của Smeargle',1,0,0,'nhiem_vu',1026,1),
(10027,'nv_027','Người bạn của Hoothoot','Phần thưởng nhiệm vụ P-03.2.4: Nói chuyện với Arcanine ở Rừng Viridian.','',0,'nhanh','hp',3,'nhiem_vu','Người bạn của Hoothoot',1,0,0,'nhiem_vu',1027,1),
(10028,'nv_028','Người hùng Pewter','Phần thưởng nhiệm vụ P-04.0.5: Nói chuyện với Machamp ở Pewter.','',0,'cong','ne_tranh',3,'nhiem_vu','Người hùng Pewter',1,0,0,'nhiem_vu',1028,1),
(10029,'nv_029','Người bạn của Machoke','Phần thưởng nhiệm vụ P-04.1.4: Phân rã 2 món đồ thừa.','',0,'loc','chi_mang',3,'nhiem_vu','Người bạn của Machoke',1,0,0,'nhiem_vu',1029,1),
(10030,'nv_030','Người bạn của Jigglypuff','Phần thưởng nhiệm vụ P-04.2.4: Đứng tại điểm đánh dấu trên Pewter (ô 13,10).','',0,'sao','khang_trang_thai',3,'nhiem_vu','Người bạn của Jigglypuff',1,0,0,'nhiem_vu',1030,1),
(10031,'nv_031','Người hùng Núi Mặt Trăng','Phần thưởng nhiệm vụ P-05.0.5: Nói chuyện với Snorlax ở Núi Mặt Trăng.','',0,'la','hp',3,'nhiem_vu','Người hùng Núi Mặt Trăng',1,0,0,'nhiem_vu',1031,1),
(10032,'nv_032','Người bạn của Pelipper','Phần thưởng nhiệm vụ P-05.1.4: Mang lá thư tới Bronzong.','',0,'tim','ne_tranh',3,'nhiem_vu','Người bạn của Pelipper',1,0,0,'nhiem_vu',1032,1),
(10033,'nv_033','Người bạn của Bellossom','Phần thưởng nhiệm vụ P-05.2.4: Nói chuyện với Persian ở Núi Mặt Trăng.','',0,'khien','chi_mang',3,'nhiem_vu','Người bạn của Bellossom',1,0,0,'nhiem_vu',1033,1),
(10034,'nv_034','Người hùng Cerulean','Phần thưởng nhiệm vụ P-06.0.5: Nói chuyện với Torkoal ở Cerulean.','',0,'giot','khang_trang_thai',3,'nhiem_vu','Người hùng Cerulean',1,0,0,'nhiem_vu',1034,1),
(10035,'nv_035','Người bạn của Slowking','Phần thưởng nhiệm vụ P-06.1.4: Nói chuyện với Snorlax ở Cerulean.','',0,'lua','hp',3,'nhiem_vu','Người bạn của Slowking',1,0,0,'nhiem_vu',1035,1),
(10036,'nv_036','Người bạn của Pinsir','Phần thưởng nhiệm vụ P-06.2.4: Thắng 1 trận đấu trường.','',0,'set','ne_tranh',3,'nhiem_vu','Người bạn của Pinsir',1,0,0,'nhiem_vu',1036,1),
(10037,'nv_037','Người hùng Vermilion','Phần thưởng nhiệm vụ P-07.0.5: Nói chuyện với Meowth ở Vermilion.','',0,'nhanh','chi_mang',3,'nhiem_vu','Người hùng Vermilion',1,0,0,'nhiem_vu',1037,1),
(10038,'nv_038','Người bạn của Riolu','Phần thưởng nhiệm vụ P-07.1.4: Nói chuyện với Xatu ở Vermilion.','',0,'cong','khang_trang_thai',3,'nhiem_vu','Người bạn của Riolu',1,0,0,'nhiem_vu',1038,1),
(10039,'nv_039','Người bạn của Bronzor','Phần thưởng nhiệm vụ P-07.2.4: Đào 2 lần ở Vermilion.','',0,'loc','hp',3,'nhiem_vu','Người bạn của Bronzor',1,0,0,'nhiem_vu',1039,1),
(10040,'nv_040','Người hùng Tàu S.S. Anne','Phần thưởng nhiệm vụ P-08.0.4: Nói chuyện với Persian ở Tàu S.S. Anne.','',0,'sao','ne_tranh',3,'nhiem_vu','Người hùng Tàu S.S. Anne',1,0,0,'nhiem_vu',1040,1),
(10041,'nv_041','Người bạn của Slurpuff','Phần thưởng nhiệm vụ P-08.1.4: Giao Bánh quả mọng cho Kecleon.','',0,'la','chi_mang',3,'nhiem_vu','Người bạn của Slurpuff',1,0,0,'nhiem_vu',1041,1),
(10042,'nv_042','Người hùng Lavender','Phần thưởng nhiệm vụ P-09.0.5: Nói chuyện với Snorlax ở Lavender.','',0,'tim','khang_trang_thai',3,'nhiem_vu','Người hùng Lavender',1,0,0,'nhiem_vu',1042,1),
(10043,'nv_043','Người bạn của Chansey','Phần thưởng nhiệm vụ P-09.1.4: Hộ tống Pokémon bị thương từ Lavender tới Chansey an toàn.','',0,'khien','hp',3,'nhiem_vu','Người bạn của Chansey',1,0,0,'nhiem_vu',1043,1),
(10044,'nv_044','Người bạn của Shellder','Phần thưởng nhiệm vụ P-09.2.4: Nói chuyện với Meowth ở Lavender.','',0,'giot','ne_tranh',3,'nhiem_vu','Người bạn của Shellder',1,0,0,'nhiem_vu',1044,1),
(10045,'nv_045','Người hùng Celadon','Phần thưởng nhiệm vụ P-10.0.5: Nói chuyện với Kecleon ở Celadon.','',0,'lua','chi_mang',3,'nhiem_vu','Người hùng Celadon',1,0,0,'nhiem_vu',1045,1),
(10046,'nv_046','Người bạn của Loudred','Phần thưởng nhiệm vụ P-10.1.4: Chọn một con đường.','',0,'set','khang_trang_thai',3,'nhiem_vu','Người bạn của Loudred',1,0,0,'nhiem_vu',1046,1),
(10047,'nv_047','Người giữ yên giấc','Phần thưởng nhiệm vụ P-10.1.4: Chọn một con đường.','',0,'nhanh','hp',3,'nhiem_vu','Người giữ yên giấc',1,0,0,'nhiem_vu',1047,1),
(10048,'nv_048','Người dạy hát ru','Phần thưởng nhiệm vụ P-10.1.4: Chọn một con đường.','',0,'cong','ne_tranh',3,'nhiem_vu','Người dạy hát ru',1,0,0,'nhiem_vu',1048,1),
(10049,'nv_049','Người bạn của Hoppip','Phần thưởng nhiệm vụ P-10.2.4: Nói chuyện với Pidgeot ở Celadon.','',0,'loc','chi_mang',3,'nhiem_vu','Người bạn của Hoppip',1,0,0,'nhiem_vu',1049,1),
(10050,'nv_050','Người hùng Saffron','Phần thưởng nhiệm vụ P-11.0.5: Nói chuyện với Xatu ở Saffron.','',0,'sao','khang_trang_thai',3,'nhiem_vu','Người hùng Saffron',1,0,0,'nhiem_vu',1050,1),
(10051,'nv_051','Người bạn của Pikachu','Phần thưởng nhiệm vụ P-11.1.4: Nói chuyện với Blaziken ở Saffron.','',0,'la','hp',3,'nhiem_vu','Người bạn của Pikachu',1,0,0,'nhiem_vu',1051,1),
(10052,'nv_052','Người bạn của Foongus','Phần thưởng nhiệm vụ P-11.2.4: Quan sát Whirlipede Shiny vào ban đêm.','',0,'tim','ne_tranh',3,'nhiem_vu','Người bạn của Foongus',1,0,0,'nhiem_vu',1052,1),
(10053,'nv_053','Người hùng Vùng Safari','Phần thưởng nhiệm vụ P-12.0.4: Hộ tống Kangaskhan con từ Vùng Safari tới Kangaskhan an toàn.','',0,'khien','chi_mang',3,'nhiem_vu','Người hùng Vùng Safari',1,0,0,'nhiem_vu',1053,1),
(10054,'nv_054','Người bạn của Ponyta','Phần thưởng nhiệm vụ P-12.1.4: Nói chuyện với Tauros ở Vùng Safari.','',0,'giot','khang_trang_thai',3,'nhiem_vu','Người bạn của Ponyta',1,0,0,'nhiem_vu',1054,1),
(10055,'nv_055','Người bạn của Unown','Phần thưởng nhiệm vụ P-12.2.4: Nói chuyện với Xatu ở Vùng Safari.','',0,'lua','hp',3,'nhiem_vu','Người bạn của Unown',1,0,0,'nhiem_vu',1055,1),
(10056,'nv_056','Người hùng Fuchsia','Phần thưởng nhiệm vụ P-13.0.4: Nói chuyện với Persian ở Fuchsia.','',0,'set','ne_tranh',3,'nhiem_vu','Người hùng Fuchsia',1,0,0,'nhiem_vu',1056,1),
(10057,'nv_057','Người bạn của Lotad','Phần thưởng nhiệm vụ P-13.1.4: Nói chuyện với Snorlax ở Fuchsia.','',0,'nhanh','chi_mang',3,'nhiem_vu','Người bạn của Lotad',1,0,0,'nhiem_vu',1057,1),
(10058,'nv_058','Người bạn của Mareep','Phần thưởng nhiệm vụ P-13.2.4: Giao Chăn len cho Kangaskhan.','',0,'cong','khang_trang_thai',3,'nhiem_vu','Người bạn của Mareep',1,0,0,'nhiem_vu',1058,1),
(10059,'nv_059','Người hùng Cinnabar','Phần thưởng nhiệm vụ P-14.0.4: Nói chuyện với Bronzong ở Cinnabar.','',0,'loc','hp',3,'nhiem_vu','Người hùng Cinnabar',1,0,0,'nhiem_vu',1059,1),
(10060,'nv_060','Người bạn của Wigglytuff','Phần thưởng nhiệm vụ P-14.1.4: Nói chuyện với Snorlax ở Cinnabar.','',0,'sao','ne_tranh',3,'nhiem_vu','Người bạn của Wigglytuff',1,0,0,'nhiem_vu',1060,1),
(10061,'nv_061','Người bạn của Sableye','Phần thưởng nhiệm vụ P-14.2.4: Nói chuyện với Sableye ở Cinnabar.','',0,'la','chi_mang',3,'nhiem_vu','Người bạn của Sableye',1,0,0,'nhiem_vu',1061,1),
(10062,'nv_062','Người hùng New Bark','Phần thưởng nhiệm vụ P-15.0.5: Nói chuyện với Xatu ở New Bark.','',0,'tim','khang_trang_thai',3,'nhiem_vu','Người hùng New Bark',1,0,0,'nhiem_vu',1062,1),
(10063,'nv_063','Người bạn của Bulbasaur','Phần thưởng nhiệm vụ P-15.1.4: Nói chuyện với Snorlax ở New Bark.','',0,'khien','hp',3,'nhiem_vu','Người bạn của Bulbasaur',1,0,0,'nhiem_vu',1063,1),
(10064,'nv_064','Người bạn của Skitty','Phần thưởng nhiệm vụ P-15.2.4: Chọn một con đường.','',0,'giot','ne_tranh',3,'nhiem_vu','Người bạn của Skitty',1,0,0,'nhiem_vu',1064,1),
(10065,'nv_065','Ông tơ bà nguyệt New Bark','Phần thưởng nhiệm vụ P-15.2.4: Chọn một con đường.','',0,'lua','chi_mang',3,'nhiem_vu','Ông tơ bà nguyệt New Bark',1,0,0,'nhiem_vu',1065,1),
(10066,'nv_066','Người hùng Hang Tối','Phần thưởng nhiệm vụ P-16.0.4: Nói chuyện với Kangaskhan ở Hang Tối.','',0,'set','khang_trang_thai',3,'nhiem_vu','Người hùng Hang Tối',1,0,0,'nhiem_vu',1066,1),
(10067,'nv_067','Người bạn của Swirlix','Phần thưởng nhiệm vụ P-16.1.4: Nấu 2 Bánh quả mọng.','',0,'nhanh','hp',3,'nhiem_vu','Người bạn của Swirlix',1,0,0,'nhiem_vu',1067,1),
(10068,'nv_068','Người bạn của Happiny','Phần thưởng nhiệm vụ P-16.2.4: Nói chuyện với Kangaskhan ở Hang Tối.','',0,'cong','ne_tranh',3,'nhiem_vu','Người bạn của Happiny',1,0,0,'nhiem_vu',1068,1),
(10069,'nv_069','Người hùng Cherrygrove','Phần thưởng nhiệm vụ P-17.0.5: Nói chuyện với Persian ở Cherrygrove.','',0,'loc','chi_mang',3,'nhiem_vu','Người hùng Cherrygrove',1,0,0,'nhiem_vu',1069,1),
(10070,'nv_070','Người bạn của Corphish','Phần thưởng nhiệm vụ P-17.1.4: Nói chuyện với Lapras ở Cherrygrove.','',0,'sao','khang_trang_thai',3,'nhiem_vu','Người bạn của Corphish',1,0,0,'nhiem_vu',1070,1),
(10071,'nv_071','Người bạn của Diglett','Phần thưởng nhiệm vụ P-17.2.4: Nói chuyện với Persian ở Cherrygrove.','',0,'la','hp',3,'nhiem_vu','Người bạn của Diglett',1,0,0,'nhiem_vu',1071,1),
(10072,'nv_072','Người hùng Violet','Phần thưởng nhiệm vụ P-18.0.4: Nói chuyện với Snorlax ở Violet.','',0,'tim','ne_tranh',3,'nhiem_vu','Người hùng Violet',1,0,0,'nhiem_vu',1072,1),
(10073,'nv_073','Người bạn của Kecleon','Phần thưởng nhiệm vụ P-18.1.4: Giao Tấm ảnh cho Meowth.','',0,'khien','chi_mang',3,'nhiem_vu','Người bạn của Kecleon',1,0,0,'nhiem_vu',1073,1),
(10074,'nv_074','Người bạn của Noctowl','Phần thưởng nhiệm vụ P-18.2.4: Nói chuyện với Arcanine ở Violet.','',0,'giot','khang_trang_thai',3,'nhiem_vu','Người bạn của Noctowl',1,0,0,'nhiem_vu',1074,1),
(10075,'nv_075','Người hùng Azalea','Phần thưởng nhiệm vụ P-19.0.4: Nói chuyện với Xatu ở Azalea.','',0,'lua','hp',3,'nhiem_vu','Người hùng Azalea',1,0,0,'nhiem_vu',1075,1),
(10076,'nv_076','Người bạn của Timburr','Phần thưởng nhiệm vụ P-19.1.4: Phân rã 2 món đồ thừa.','',0,'set','ne_tranh',3,'nhiem_vu','Người bạn của Timburr',1,0,0,'nhiem_vu',1076,1),
(10077,'nv_077','Người bạn của Chatot','Phần thưởng nhiệm vụ P-19.2.4: Đứng tại điểm đánh dấu trên Azalea (ô 47,5).','',0,'nhanh','chi_mang',3,'nhiem_vu','Người bạn của Chatot',1,0,0,'nhiem_vu',1077,1),
(10078,'nv_078','Người hùng Rừng Ilex','Phần thưởng nhiệm vụ P-20.0.4: Nói chuyện với Torkoal ở Rừng Ilex.','',0,'cong','khang_trang_thai',3,'nhiem_vu','Người hùng Rừng Ilex',1,0,0,'nhiem_vu',1078,1),
(10079,'nv_079','Người bạn của Delibird','Phần thưởng nhiệm vụ P-20.1.4: Mang lá thư tới Bronzong.','',0,'loc','hp',3,'nhiem_vu','Người bạn của Delibird',1,0,0,'nhiem_vu',1079,1),
(10080,'nv_080','Người bạn của Sunkern','Phần thưởng nhiệm vụ P-20.2.4: Nói chuyện với Persian ở Rừng Ilex.','',0,'sao','ne_tranh',3,'nhiem_vu','Người bạn của Sunkern',1,0,0,'nhiem_vu',1080,1),
(10081,'nv_081','Người hùng Goldenrod','Phần thưởng nhiệm vụ P-21.0.5: Nói chuyện với Meowth ở Goldenrod.','',0,'la','chi_mang',3,'nhiem_vu','Người hùng Goldenrod',1,0,0,'nhiem_vu',1081,1),
(10082,'nv_082','Người bạn của Primeape','Phần thưởng nhiệm vụ P-21.2.4: Thắng 1 trận đấu trường.','',0,'tim','khang_trang_thai',3,'nhiem_vu','Người bạn của Primeape',1,0,0,'nhiem_vu',1082,1),
(10083,'nv_083','Người hùng Ecruteak','Phần thưởng nhiệm vụ P-22.0.4: Chọn một con đường.','',0,'khien','hp',3,'nhiem_vu','Người hùng Ecruteak',1,0,0,'nhiem_vu',1083,1),
(10084,'nv_084','Bạn của ánh trăng','Phần thưởng nhiệm vụ P-22.0.4: Chọn một con đường.','',0,'giot','ne_tranh',3,'nhiem_vu','Bạn của ánh trăng',1,0,0,'nhiem_vu',1084,1),
(10085,'nv_085','Bạn của ánh nắng','Phần thưởng nhiệm vụ P-22.0.4: Chọn một con đường.','',0,'lua','chi_mang',3,'nhiem_vu','Bạn của ánh nắng',1,0,0,'nhiem_vu',1085,1),
(10086,'nv_086','Người bạn của Bunnelby','Phần thưởng nhiệm vụ P-22.1.4: Nói chuyện với Xatu ở Ecruteak.','',0,'set','khang_trang_thai',3,'nhiem_vu','Người bạn của Bunnelby',1,0,0,'nhiem_vu',1086,1),
(10087,'nv_087','Người bạn của Natu','Phần thưởng nhiệm vụ P-22.2.4: Đào 2 lần ở Ecruteak.','',0,'nhanh','hp',3,'nhiem_vu','Người bạn của Natu',1,0,0,'nhiem_vu',1087,1),
(10088,'nv_088','Người hùng Rừng Cổ','Phần thưởng nhiệm vụ P-23.0.4: Nói chuyện với Bronzong ở Rừng Cổ.','',0,'cong','ne_tranh',3,'nhiem_vu','Người hùng Rừng Cổ',1,0,0,'nhiem_vu',1088,1),
(10089,'nv_089','Người bạn của Alcremie','Phần thưởng nhiệm vụ P-23.1.4: Giao Bánh quả mọng cho Kecleon.','',0,'loc','chi_mang',3,'nhiem_vu','Người bạn của Alcremie',1,0,0,'nhiem_vu',1089,1),
(10090,'nv_090','Người bạn của Audino','Phần thưởng nhiệm vụ P-23.2.4: Hộ tống Pokémon bị thương từ Rừng Cổ tới Chansey an toàn.','',0,'sao','khang_trang_thai',3,'nhiem_vu','Người bạn của Audino',1,0,0,'nhiem_vu',1090,1),
(10091,'nv_091','Người hùng Olivine','Phần thưởng nhiệm vụ P-24.0.4: Nói chuyện với Machamp ở Olivine.','',0,'la','hp',3,'nhiem_vu','Người hùng Olivine',1,0,0,'nhiem_vu',1091,1),
(10092,'nv_092','Người bạn của Exploud','Phần thưởng nhiệm vụ P-24.1.4: Chọn một con đường.','',0,'tim','ne_tranh',3,'nhiem_vu','Người bạn của Exploud',1,0,0,'nhiem_vu',1092,1),
(10093,'nv_093','Người bạn của Swablu','Phần thưởng nhiệm vụ P-24.2.4: Nói chuyện với Pidgeot ở Olivine.','',0,'khien','chi_mang',3,'nhiem_vu','Người bạn của Swablu',1,0,0,'nhiem_vu',1093,1),
(10094,'nv_094','Người hùng Cianwood','Phần thưởng nhiệm vụ P-25.0.4: Nói chuyện với Kecleon ở Cianwood.','',0,'giot','khang_trang_thai',3,'nhiem_vu','Người hùng Cianwood',1,0,0,'nhiem_vu',1094,1),
(10095,'nv_095','Người bạn của Eevee','Phần thưởng nhiệm vụ P-25.1.4: Nói chuyện với Blaziken ở Cianwood.','',0,'lua','hp',3,'nhiem_vu','Người bạn của Eevee',1,0,0,'nhiem_vu',1095,1),
(10096,'nv_096','Người bạn của Morelull','Phần thưởng nhiệm vụ P-25.2.4: Quan sát Gastrodon vào ban đêm.','',0,'set','ne_tranh',3,'nhiem_vu','Người bạn của Morelull',1,0,0,'nhiem_vu',1096,1),
(10097,'nv_097','Người hùng Mahogany','Phần thưởng nhiệm vụ P-26.0.4: Nói chuyện với Persian ở Mahogany.','',0,'nhanh','chi_mang',3,'nhiem_vu','Người hùng Mahogany',1,0,0,'nhiem_vu',1097,1),
(10098,'nv_098','Người bạn của Dodrio','Phần thưởng nhiệm vụ P-26.1.4: Nói chuyện với Tauros ở Mahogany.','',0,'cong','khang_trang_thai',3,'nhiem_vu','Người bạn của Dodrio',1,0,0,'nhiem_vu',1098,1),
(10099,'nv_099','Người hùng Hồ Phẫn Nộ','Phần thưởng nhiệm vụ P-27.0.5: Nói chuyện với Blaziken ở Hồ Phẫn Nộ.','',0,'loc','hp',3,'nhiem_vu','Người hùng Hồ Phẫn Nộ',1,0,0,'nhiem_vu',1099,1)
ON DUPLICATE KEY UPDATE `ma`=VALUES(`ma`), `ten`=VALUES(`ten`), `mo_ta`=VALUES(`mo_ta`), `goi_y`=VALUES(`goi_y`), `do_hiem`=VALUES(`do_hiem`), `hinh`=VALUES(`hinh`), `dieu_kien`=VALUES(`dieu_kien`), `tham_so`=VALUES(`tham_so`), `so_luong`=VALUES(`so_luong`), `an`=VALUES(`an`), `thoi_han_phut`=VALUES(`thoi_han_phut`), `nguon`=VALUES(`nguon`), `thu_tu`=VALUES(`thu_tu`);

INSERT INTO `data_danh_hieu` (`id`,`ma`,`ten`,`mo_ta`,`goi_y`,`do_hiem`,`hinh`,`chi_so`,`phan_nghin`,`dieu_kien`,`tham_so`,`so_luong`,`an`,`thoi_han_phut`,`nguon`,`thu_tu`,`bat`) VALUES
(10100,'nv_100','Người bạn của Wooper','Phần thưởng nhiệm vụ P-27.1.4: Nói chuyện với Snorlax ở Hồ Phẫn Nộ.','',0,'sao','ne_tranh',3,'nhiem_vu','Người bạn của Wooper',1,0,0,'nhiem_vu',1100,1),
(10101,'nv_101','Người bạn của Wooloo','Phần thưởng nhiệm vụ P-27.2.4: Giao Chăn len cho Kangaskhan.','',0,'la','chi_mang',3,'nhiem_vu','Người bạn của Wooloo',1,0,0,'nhiem_vu',1101,1),
(10102,'nv_102','Người hùng Blackthorn','Phần thưởng nhiệm vụ P-28.0.5: Nói chuyện với Xatu ở Blackthorn.','',0,'tim','khang_trang_thai',3,'nhiem_vu','Người hùng Blackthorn',1,0,0,'nhiem_vu',1102,1),
(10103,'nv_103','Người bạn của Linoone','Phần thưởng nhiệm vụ P-28.1.4: Nói chuyện với Snorlax ở Blackthorn.','',0,'khien','hp',3,'nhiem_vu','Người bạn của Linoone',1,0,0,'nhiem_vu',1103,1),
(10104,'nv_104','Người bạn của Carbink','Phần thưởng nhiệm vụ P-28.2.4: Nói chuyện với Sableye ở Blackthorn.','',0,'giot','ne_tranh',3,'nhiem_vu','Người bạn của Carbink',1,0,0,'nhiem_vu',1104,1),
(10105,'nv_105','Người hùng Hang Salamence','Phần thưởng nhiệm vụ P-29.0.4: Nói chuyện với Blaziken ở Hang Salamence.','',0,'lua','chi_mang',3,'nhiem_vu','Người hùng Hang Salamence',1,0,0,'nhiem_vu',1105,1),
(10106,'nv_106','Người bạn của Oddish','Phần thưởng nhiệm vụ P-29.1.4: Nói chuyện với Snorlax ở Hang Salamence.','',0,'set','khang_trang_thai',3,'nhiem_vu','Người bạn của Oddish',1,0,0,'nhiem_vu',1106,1),
(10107,'nv_107','Ông tơ bà nguyệt Hang Salamence','Phần thưởng nhiệm vụ P-29.2.4: Chọn một con đường.','',0,'nhanh','hp',3,'nhiem_vu','Ông tơ bà nguyệt Hang Salamence',1,0,0,'nhiem_vu',1107,1),
(10108,'nv_108','Người hùng Đường Victory','Phần thưởng nhiệm vụ P-30.0.4: Nói chuyện với Blaziken ở Đường Victory.','',0,'cong','ne_tranh',3,'nhiem_vu','Người hùng Đường Victory',1,0,0,'nhiem_vu',1108,1),
(10109,'nv_109','Người hùng Núi Silver','Phần thưởng nhiệm vụ P-31.0.5: Nói chuyện với Snorlax ở Núi Silver.','',0,'loc','chi_mang',3,'nhiem_vu','Người hùng Núi Silver',1,0,0,'nhiem_vu',1109,1),
(10110,'nv_110','Người hùng Cao nguyên Indigo','Phần thưởng nhiệm vụ P-32.0.5: Nói chuyện với Xatu ở Cao nguyên Indigo.','',0,'sao','khang_trang_thai',3,'nhiem_vu','Người hùng Cao nguyên Indigo',1,0,0,'nhiem_vu',1110,1),
(10111,'nv_111','Người hùng Hang Darkrai','Phần thưởng nhiệm vụ P-33.0.5: Nói chuyện với Blaziken ở Hang Darkrai.','',0,'la','hp',3,'nhiem_vu','Người hùng Hang Darkrai',1,0,0,'nhiem_vu',1111,1),
(10112,'nv_112','Người gác Pallet','Phần thưởng nhiệm vụ TV-01.3: Nói chuyện với Xatu ở Pallet.','',0,'tim','ne_tranh',3,'nhiem_vu','Người gác Pallet',1,0,0,'nhiem_vu',1112,1),
(10113,'nv_113','Người gác Núi Mặt Trăng','Phần thưởng nhiệm vụ TV-02.3: Thu 34 Nấm phát quang (rơi từ Tepig).','',0,'khien','chi_mang',3,'nhiem_vu','Người gác Núi Mặt Trăng',1,0,0,'nhiem_vu',1113,1),
(10114,'nv_114','Người gác Vermilion','Phần thưởng nhiệm vụ TV-03.3: Hạ 330 Ariados.','',0,'giot','khang_trang_thai',3,'nhiem_vu','Người gác Vermilion',1,0,0,'nhiem_vu',1114,1),
(10115,'nv_115','Người gác Lavender','Phần thưởng nhiệm vụ TV-04.3: Đứng tại điểm đánh dấu trên Lavender (ô 44,41).','',0,'lua','hp',3,'nhiem_vu','Người gác Lavender',1,0,0,'nhiem_vu',1115,1),
(10116,'nv_116','Người gác Celadon','Phần thưởng nhiệm vụ TV-05.3: Thắng 2 trận đấu trường.','',0,'set','ne_tranh',3,'nhiem_vu','Người gác Celadon',1,0,0,'nhiem_vu',1116,1),
(10117,'nv_117','Người gác Cinnabar','Phần thưởng nhiệm vụ TV-06.3: Nói chuyện với Bronzong ở Cinnabar.','',0,'nhanh','chi_mang',3,'nhiem_vu','Người gác Cinnabar',1,0,0,'nhiem_vu',1117,1),
(10118,'nv_118','Người gác New Bark','Phần thưởng nhiệm vụ TV-07.3: Hạ 295 Scolipede.','',0,'cong','khang_trang_thai',3,'nhiem_vu','Người gác New Bark',1,0,0,'nhiem_vu',1118,1),
(10119,'nv_119','Người gác Rừng Ilex','Phần thưởng nhiệm vụ TV-08.3: Quan sát Magcargo lúc bình minh.','',0,'loc','hp',3,'nhiem_vu','Người gác Rừng Ilex',1,0,0,'nhiem_vu',1119,1),
(10120,'nv_120','Người gác Goldenrod','Phần thưởng nhiệm vụ TV-09.3: Trả lời đúng 2 câu đố Ai là Pokémon?.','',0,'sao','ne_tranh',3,'nhiem_vu','Người gác Goldenrod',1,0,0,'nhiem_vu',1120,1),
(10121,'nv_121','Người gác Olivine','Phần thưởng nhiệm vụ TV-10.3: Hộ tống Gói hàng từ Olivine tới Machamp an toàn.','',0,'la','chi_mang',3,'nhiem_vu','Người gác Olivine',1,0,0,'nhiem_vu',1121,1),
(10122,'nv_122','Người gác Hồ Phẫn Nộ','Phần thưởng nhiệm vụ TV-11.3: Hạ 270 Kingler.','',0,'tim','khang_trang_thai',3,'nhiem_vu','Người gác Hồ Phẫn Nộ',1,0,0,'nhiem_vu',1122,1),
(10123,'nv_123','Người gác Blackthorn','Phần thưởng nhiệm vụ TV-12.3: Nói chuyện với Xatu ở Blackthorn.','',0,'khien','hp',3,'nhiem_vu','Người gác Blackthorn',1,0,0,'nhiem_vu',1123,1),
(10124,'nv_124','Người gác Núi Silver','Phần thưởng nhiệm vụ TV-13.3: Đánh bại 61 Luxray mạnh.','',0,'giot','ne_tranh',3,'nhiem_vu','Người gác Núi Silver',1,0,0,'nhiem_vu',1124,1),
(10125,'nv_125','Người nghe tiếng sấm','Phần thưởng nhiệm vụ HT-ZAP.7: Nói chuyện với Snorlax ở Tổ Zapdos.','',2,'lua','chi_mang',7,'nhiem_vu','Người nghe tiếng sấm',1,0,0,'nhiem_vu',1125,1),
(10126,'nv_126','Người theo vệt lửa','Phần thưởng nhiệm vụ HT-ENT.7: Nói chuyện với Blaziken ở Ecruteak.','',2,'set','khang_trang_thai',7,'nhiem_vu','Người theo vệt lửa',1,0,0,'nhiem_vu',1126,1),
(10127,'nv_127','Người đi qua ác mộng','Phần thưởng nhiệm vụ HT-DAR.7: Nói chuyện với Xatu ở Hang Darkrai.','',2,'nhanh','hp',7,'nhiem_vu','Người đi qua ác mộng',1,0,0,'nhiem_vu',1127,1),
(10128,'nv_128','Người chạm tới bầu trời','Phần thưởng nhiệm vụ HT-RAY.7: Nói chuyện với Blaziken ở Núi Silver.','',2,'cong','ne_tranh',7,'nhiem_vu','Người chạm tới bầu trời',1,0,0,'nhiem_vu',1128,1),
(10129,'nv_129','Người giữ cân bằng đất biển','Phần thưởng nhiệm vụ HT-GRK.7: Nói chuyện với Lapras ở Hồ Phẫn Nộ.','',2,'loc','chi_mang',7,'nhiem_vu','Người giữ cân bằng đất biển',1,0,0,'nhiem_vu',1129,1),
(10130,'nv_130','Người giữ hơi thở băng','Phần thưởng nhiệm vụ HT-ART.7: Nói chuyện với Torkoal ở Núi Silver.','',2,'sao','khang_trang_thai',7,'nhiem_vu','Người giữ hơi thở băng',1,0,0,'nhiem_vu',1130,1),
(10131,'nv_131','Người mang ngọn lửa bất diệt','Phần thưởng nhiệm vụ HT-MOL.7: Nói chuyện với Blaziken ở Cinnabar.','',2,'la','hp',7,'nhiem_vu','Người mang ngọn lửa bất diệt',1,0,0,'nhiem_vu',1131,1),
(10132,'nv_132','Người đuổi kịp tia chớp','Phần thưởng nhiệm vụ HT-RAI.7: Nói chuyện với Bronzong ở Mahogany.','',2,'tim','ne_tranh',7,'nhiem_vu','Người đuổi kịp tia chớp',1,0,0,'nhiem_vu',1132,1),
(10133,'nv_133','Người làm trong dòng nước','Phần thưởng nhiệm vụ HT-SUI.7: Nói chuyện với Lapras ở Hồ Phẫn Nộ.','',2,'khien','chi_mang',7,'nhiem_vu','Người làm trong dòng nước',1,0,0,'nhiem_vu',1133,1),
(10134,'nv_134','Người nghe bài ca biển sâu','Phần thưởng nhiệm vụ HT-LUG.7: Nói chuyện với Lapras ở Olivine.','',2,'giot','khang_trang_thai',7,'nhiem_vu','Người nghe bài ca biển sâu',1,0,0,'nhiem_vu',1134,1),
(10135,'nv_135','Người thấy cầu vồng bảy sắc','Phần thưởng nhiệm vụ HT-HOO.7: Nói chuyện với Xatu ở Ecruteak.','',2,'lua','hp',7,'nhiem_vu','Người thấy cầu vồng bảy sắc',1,0,0,'nhiem_vu',1135,1),
(10136,'nv_136','Người du hành qua thời gian','Phần thưởng nhiệm vụ HT-CEL.7: Nói chuyện với Snorlax ở Rừng Ilex.','',2,'set','ne_tranh',7,'nhiem_vu','Người du hành qua thời gian',1,0,0,'nhiem_vu',1136,1),
(10137,'nv_137','Người tìm thấy Mew','Phần thưởng nhiệm vụ HT-MEW.7: Nói chuyện với Bronzong ở Cinnabar.','',2,'nhanh','chi_mang',7,'nhiem_vu','Người tìm thấy Mew',1,0,0,'nhiem_vu',1137,1),
(10138,'nv_138','Người giữ bí mật của Snorlax','Phần thưởng nhiệm vụ AN-01: Nói chuyện với Snorlax ở Pallet.','Một nhiệm vụ ẩn đang chờ được tìm ra.',1,'cong','khang_trang_thai',5,'nhiem_vu','Người giữ bí mật của Snorlax',1,1,0,'an',1138,1),
(10139,'nv_139','Bạn của bóng đêm','Phần thưởng nhiệm vụ AN-02: Đứng tại điểm đánh dấu trên Lavender (ô 44,41).','Một nhiệm vụ ẩn đang chờ được tìm ra.',1,'loc','hp',5,'nhiem_vu','Bạn của bóng đêm',1,1,0,'an',1139,1),
(10140,'nv_140','Người thấy Magikarp vàng','Phần thưởng nhiệm vụ AN-03: Câu được 1 Vảy cá vàng.','Một nhiệm vụ ẩn đang chờ được tìm ra.',1,'sao','ne_tranh',5,'nhiem_vu','Người thấy Magikarp vàng',1,1,0,'an',1140,1),
(10141,'nv_141','Người đọc chữ Unown','Phần thưởng nhiệm vụ AN-04: Giải câu đố của Xatu: gõ đáp án vào kênh trò chuyện.','Một nhiệm vụ ẩn đang chờ được tìm ra.',1,'la','chi_mang',5,'nhiem_vu','Người đọc chữ Unown',1,1,0,'an',1141,1),
(10142,'nv_142','Người nhìn thấu','Phần thưởng nhiệm vụ AN-05: Nói chuyện với Kecleon ở Pallet.','Một nhiệm vụ ẩn đang chờ được tìm ra.',1,'tim','khang_trang_thai',5,'nhiem_vu','Người nhìn thấu',1,1,0,'an',1142,1),
(10143,'nv_143','Thợ đào may mắn','Phần thưởng nhiệm vụ AN-06: Đào 1 lần ở Khu mỏ.','Một nhiệm vụ ẩn đang chờ được tìm ra.',1,'khien','hp',5,'nhiem_vu','Thợ đào may mắn',1,1,0,'an',1143,1),
(10144,'nv_144','Người lắng nghe','Phần thưởng nhiệm vụ AN-07: Đứng tại điểm đánh dấu trên Cerulean (ô 50,46).','Một nhiệm vụ ẩn đang chờ được tìm ra.',1,'giot','ne_tranh',5,'nhiem_vu','Người lắng nghe',1,1,0,'an',1144,1),
(10145,'nv_145','Người nghe chuông bạc','Phần thưởng nhiệm vụ AN-08: Quan sát Rhydon khi trời mưa.','Một nhiệm vụ ẩn đang chờ được tìm ra.',1,'lua','chi_mang',5,'nhiem_vu','Người nghe chuông bạc',1,1,0,'an',1145,1),
(10146,'nv_146','Đầu bếp của Snorlax','Phần thưởng nhiệm vụ AN-09: Nấu 1 Bánh mật ong.','Một nhiệm vụ ẩn đang chờ được tìm ra.',1,'set','khang_trang_thai',5,'nhiem_vu','Đầu bếp của Snorlax',1,1,0,'an',1146,1),
(10147,'nv_147','Người mang mười sáu ngọn đèn','Phần thưởng nhiệm vụ AN-10: Đứng tại điểm đánh dấu trên Pallet (ô 53,19).','Một nhiệm vụ ẩn đang chờ được tìm ra.',1,'nhanh','hp',5,'nhiem_vu','Người mang mười sáu ngọn đèn',1,1,0,'an',1147,1),
(10148,'nv_148','Người ấp trứng lạ','Phần thưởng nhiệm vụ AN-11: Cho 1 quả trứng nở.','Một nhiệm vụ ẩn đang chờ được tìm ra.',1,'cong','ne_tranh',5,'nhiem_vu','Người ấp trứng lạ',1,1,0,'an',1148,1),
(10149,'nv_149','Người chạy cùng gió','Phần thưởng nhiệm vụ AN-12: Về đích trong cuộc Đua Pokémon.','Một nhiệm vụ ẩn đang chờ được tìm ra.',1,'loc','chi_mang',5,'nhiem_vu','Người chạy cùng gió',1,1,0,'an',1149,1),
(10150,'nv_150','Người tha thứ','Phần thưởng nhiệm vụ AN-13: Nói chuyện với Xatu ở Pallet.','Một nhiệm vụ ẩn đang chờ được tìm ra.',1,'sao','khang_trang_thai',5,'nhiem_vu','Người tha thứ',1,1,0,'an',1150,1),
(10151,'nv_151','Người làm vườn đêm','Phần thưởng nhiệm vụ AN-14: Trồng và thu hoạch 1 Hoa sương.','Một nhiệm vụ ẩn đang chờ được tìm ra.',1,'la','hp',5,'nhiem_vu','Người làm vườn đêm',1,1,0,'an',1151,1),
(10152,'nv_152','Bạn của Lém','Phần thưởng nhiệm vụ AN-15: Thắng 1 trận đấu trường.','Một nhiệm vụ ẩn đang chờ được tìm ra.',1,'tim','ne_tranh',5,'nhiem_vu','Bạn của Lém',1,1,0,'an',1152,1),
(10153,'nv_153','Người hiểu Darkrai','Phần thưởng nhiệm vụ AN-16: Tìm Cuộn băng radio giấu ở Mahogany.','Một nhiệm vụ ẩn đang chờ được tìm ra.',1,'khien','chi_mang',5,'nhiem_vu','Người hiểu Darkrai',1,1,0,'an',1153,1),
(10154,'nv_154','Vũ công dưới trăng','Phần thưởng nhiệm vụ AN-17: Đứng tại điểm đánh dấu trên Núi Mặt Trăng (ô 30,30).','Một nhiệm vụ ẩn đang chờ được tìm ra.',1,'giot','khang_trang_thai',5,'nhiem_vu','Vũ công dưới trăng',1,1,0,'an',1154,1),
(10155,'nv_155','Người đưa thư tận tâm','Phần thưởng nhiệm vụ AN-18: Mang lá thư tới Torkoal.','Một nhiệm vụ ẩn đang chờ được tìm ra.',1,'lua','hp',5,'nhiem_vu','Người đưa thư tận tâm',1,1,0,'an',1155,1),
(10156,'nv_156','Người chiến đấu trong mơ','Phần thưởng nhiệm vụ AN-19: Đánh bại 7 Pinsir mạnh.','Một nhiệm vụ ẩn đang chờ được tìm ra.',1,'set','ne_tranh',5,'nhiem_vu','Người chiến đấu trong mơ',1,1,0,'an',1156,1),
(10157,'nv_157','Người nghe Lapras hát','Phần thưởng nhiệm vụ AN-20: Nói chuyện với Lapras ở Pallet.','Một nhiệm vụ ẩn đang chờ được tìm ra.',1,'nhanh','chi_mang',5,'nhiem_vu','Người nghe Lapras hát',1,1,0,'an',1157,1),
(10158,'nv_158','Bạn thân của Persian','Phần thưởng nhiệm vụ AN-21: Tìm Vòng cổ ngọc giấu ở Celadon.','Một nhiệm vụ ẩn đang chờ được tìm ra.',1,'cong','khang_trang_thai',5,'nhiem_vu','Bạn thân của Persian',1,1,0,'an',1158,1),
(10159,'nv_159','Người biết nghỉ ngơi','Phần thưởng nhiệm vụ AN-22: Nói chuyện với Snorlax ở Pallet.','Một nhiệm vụ ẩn đang chờ được tìm ra.',1,'loc','hp',5,'nhiem_vu','Người biết nghỉ ngơi',1,1,0,'an',1159,1),
(10160,'nv_160','Người được Darkrai tin','Phần thưởng nhiệm vụ AN-23: Quan sát Gastrodon vào ban đêm.','Một nhiệm vụ ẩn đang chờ được tìm ra.',1,'sao','ne_tranh',5,'nhiem_vu','Người được Darkrai tin',1,1,0,'an',1160,1),
(10161,'nv_161','Người nuôi Larvitar','Phần thưởng nhiệm vụ AN-24: Hạ 25 Kingler.','Một nhiệm vụ ẩn đang chờ được tìm ra.',1,'la','chi_mang',5,'nhiem_vu','Người nuôi Larvitar',1,1,0,'an',1161,1),
(10162,'nv_162','Người giữ lời hẹn','Phần thưởng nhiệm vụ AN-25: Đào 1 lần ở Pallet.','Một nhiệm vụ ẩn đang chờ được tìm ra.',1,'tim','khang_trang_thai',5,'nhiem_vu','Người giữ lời hẹn',1,1,0,'an',1162,1),
(10163,'nv_163','Người tìm sao','Phần thưởng nhiệm vụ AN-26: Tìm Mảnh sao băng giấu ở Núi Silver.','Một nhiệm vụ ẩn đang chờ được tìm ra.',1,'khien','hp',5,'nhiem_vu','Người tìm sao',1,1,0,'an',1163,1),
(10164,'nv_164','Khách quý của Snorlax','Phần thưởng nhiệm vụ AN-27: Nấu 5 Bánh quả mọng.','Một nhiệm vụ ẩn đang chờ được tìm ra.',1,'giot','ne_tranh',5,'nhiem_vu','Khách quý của Snorlax',1,1,0,'an',1164,1),
(10165,'nv_165','Người gác cầu Cerulean','Phần thưởng nhiệm vụ AN-28: Hạ 12 Dusclops trong 4 phút.','Một nhiệm vụ ẩn đang chờ được tìm ra.',1,'lua','chi_mang',5,'nhiem_vu','Người gác cầu Cerulean',1,1,0,'an',1165,1),
(10166,'nv_166','Người ngắm hoa cuối mùa','Phần thưởng nhiệm vụ AN-29: Đứng tại điểm đánh dấu trên Cherrygrove (ô 9,19).','Một nhiệm vụ ẩn đang chờ được tìm ra.',1,'set','khang_trang_thai',5,'nhiem_vu','Người ngắm hoa cuối mùa',1,1,0,'an',1166,1),
(10167,'nv_167','Học trò của Blaziken','Phần thưởng nhiệm vụ AN-30: Nói chuyện với Blaziken ở Pallet.','Một nhiệm vụ ẩn đang chờ được tìm ra.',1,'nhanh','hp',5,'nhiem_vu','Học trò của Blaziken',1,1,0,'an',1167,1),
(10168,'nv_168','Huyền thoại của làng','Phần thưởng nhiệm vụ AN-31: Nói chuyện với Xatu ở Pallet.','Một nhiệm vụ ẩn đang chờ được tìm ra.',1,'cong','ne_tranh',5,'nhiem_vu','Huyền thoại của làng',1,1,0,'an',1168,1),
(10169,'nv_169','Người bạn của đá','Phần thưởng nhiệm vụ AN-32: Nói chuyện với Machamp ở Pallet.','Một nhiệm vụ ẩn đang chờ được tìm ra.',1,'loc','chi_mang',5,'nhiem_vu','Người bạn của đá',1,1,0,'an',1169,1),
(10170,'nv_170','Bước chân đầu tiên','Phần thưởng nhiệm vụ A-001: Cấp độ đạt 10.','',0,'sao','khang_trang_thai',3,'nhiem_vu','Bước chân đầu tiên',1,0,0,'thanh_tuu',1170,1),
(10171,'nv_171','Nhà huấn luyện đồng','Phần thưởng nhiệm vụ A-002: Cấp độ đạt 20.','',0,'la','hp',3,'nhiem_vu','Nhà huấn luyện đồng',1,0,0,'thanh_tuu',1171,1),
(10172,'nv_172','Vững vàng','Phần thưởng nhiệm vụ A-003: Cấp độ đạt 30.','',0,'tim','ne_tranh',3,'nhiem_vu','Vững vàng',1,0,0,'thanh_tuu',1172,1),
(10173,'nv_173','Nhà huấn luyện bạc','Phần thưởng nhiệm vụ A-004: Cấp độ đạt 40.','',0,'khien','chi_mang',3,'nhiem_vu','Nhà huấn luyện bạc',1,0,0,'thanh_tuu',1173,1),
(10174,'nv_174','Nửa chặng đường','Phần thưởng nhiệm vụ A-005: Cấp độ đạt 50.','',0,'giot','khang_trang_thai',3,'nhiem_vu','Nửa chặng đường',1,0,0,'thanh_tuu',1174,1),
(10175,'nv_175','Nhà huấn luyện vàng','Phần thưởng nhiệm vụ A-006: Cấp độ đạt 60.','',0,'lua','hp',3,'nhiem_vu','Nhà huấn luyện vàng',1,0,0,'thanh_tuu',1175,1),
(10176,'nv_176','Vượt mùa','Phần thưởng nhiệm vụ A-007: Cấp độ đạt 70.','',0,'set','ne_tranh',3,'nhiem_vu','Vượt mùa',1,0,0,'thanh_tuu',1176,1),
(10177,'nv_177','Nhà huấn luyện bạch kim','Phần thưởng nhiệm vụ A-008: Cấp độ đạt 80.','',0,'nhanh','chi_mang',3,'nhiem_vu','Nhà huấn luyện bạch kim',1,0,0,'thanh_tuu',1177,1),
(10178,'nv_178','Đỉnh cao ba vùng','Phần thưởng nhiệm vụ A-009: Cấp độ đạt 91.','',0,'cong','khang_trang_thai',3,'nhiem_vu','Đỉnh cao ba vùng',1,0,0,'thanh_tuu',1178,1),
(10179,'nv_179','Huyền thoại sống','Phần thưởng nhiệm vụ A-010: Cấp độ đạt 100.','',0,'loc','hp',3,'nhiem_vu','Huyền thoại sống',1,0,0,'thanh_tuu',1179,1),
(10180,'nv_180','Nhà sưu tầm 10','Phần thưởng nhiệm vụ A-011: Pokédex đạt 10.','',0,'sao','ne_tranh',3,'nhiem_vu','Nhà sưu tầm 10',1,0,0,'thanh_tuu',1180,1),
(10181,'nv_181','Nhà sưu tầm 25','Phần thưởng nhiệm vụ A-012: Pokédex đạt 25.','',0,'la','chi_mang',3,'nhiem_vu','Nhà sưu tầm 25',1,0,0,'thanh_tuu',1181,1),
(10182,'nv_182','Nhà sưu tầm 40','Phần thưởng nhiệm vụ A-013: Pokédex đạt 40.','',0,'tim','khang_trang_thai',3,'nhiem_vu','Nhà sưu tầm 40',1,0,0,'thanh_tuu',1182,1),
(10183,'nv_183','Nhà sưu tầm 50','Phần thưởng nhiệm vụ A-014: Pokédex đạt 50.','',0,'khien','hp',3,'nhiem_vu','Nhà sưu tầm 50',1,0,0,'thanh_tuu',1183,1),
(10184,'nv_184','Nhà sưu tầm 60','Phần thưởng nhiệm vụ A-015: Pokédex đạt 60.','',0,'giot','ne_tranh',3,'nhiem_vu','Nhà sưu tầm 60',1,0,0,'thanh_tuu',1184,1),
(10185,'nv_185','Nhà sưu tầm 75','Phần thưởng nhiệm vụ A-016: Pokédex đạt 75.','',0,'lua','chi_mang',3,'nhiem_vu','Nhà sưu tầm 75',1,0,0,'thanh_tuu',1185,1),
(10186,'nv_186','Nhà sưu tầm 100','Phần thưởng nhiệm vụ A-017: Pokédex đạt 100.','',0,'set','khang_trang_thai',3,'nhiem_vu','Nhà sưu tầm 100',1,0,0,'thanh_tuu',1186,1),
(10187,'nv_187','Khởi hành','Phần thưởng nhiệm vụ A-018: Cốt truyện đạt 4.','',0,'nhanh','hp',3,'nhiem_vu','Khởi hành',1,0,0,'thanh_tuu',1187,1),
(10188,'nv_188','Người kể chuyện','Phần thưởng nhiệm vụ A-019: Cốt truyện đạt 13.','',0,'cong','ne_tranh',3,'nhiem_vu','Người kể chuyện',1,0,0,'thanh_tuu',1188,1),
(10189,'nv_189','Kanto toàn tập','Phần thưởng nhiệm vụ A-020: Cốt truyện đạt 28.','',0,'loc','chi_mang',3,'nhiem_vu','Kanto toàn tập',1,0,0,'thanh_tuu',1189,1),
(10190,'nv_190','Gió Johto','Phần thưởng nhiệm vụ A-021: Cốt truyện đạt 40.','',0,'sao','khang_trang_thai',3,'nhiem_vu','Gió Johto',1,0,0,'thanh_tuu',1190,1),
(10191,'nv_191','Johto toàn tập','Phần thưởng nhiệm vụ A-022: Cốt truyện đạt 52.','',0,'la','hp',3,'nhiem_vu','Johto toàn tập',1,0,0,'thanh_tuu',1191,1),
(10192,'nv_192','Đất liền Hoenn','Phần thưởng nhiệm vụ A-023: Cốt truyện đạt 66.','',0,'tim','ne_tranh',3,'nhiem_vu','Đất liền Hoenn',1,0,0,'thanh_tuu',1192,1),
(10193,'nv_193','Bình yên Hoenn','Phần thưởng nhiệm vụ A-024: Cốt truyện đạt 80.','',0,'khien','chi_mang',3,'nhiem_vu','Bình yên Hoenn',1,0,0,'thanh_tuu',1193,1),
(10194,'nv_194','Huy hiệu đầu tiên','Phần thưởng nhiệm vụ A-025: Huy hiệu Gym đạt 1.','',0,'giot','khang_trang_thai',3,'nhiem_vu','Huy hiệu đầu tiên',1,0,0,'thanh_tuu',1194,1),
(10195,'nv_195','Bốn ngọn đèn','Phần thưởng nhiệm vụ A-026: Huy hiệu Gym đạt 4.','',0,'lua','hp',3,'nhiem_vu','Bốn ngọn đèn',1,0,0,'thanh_tuu',1195,1),
(10196,'nv_196','Tám huy hiệu Kanto','Phần thưởng nhiệm vụ A-027: Huy hiệu Gym đạt 8.','',0,'set','ne_tranh',3,'nhiem_vu','Tám huy hiệu Kanto',1,0,0,'thanh_tuu',1196,1),
(10197,'nv_197','Mười hai','Phần thưởng nhiệm vụ A-028: Huy hiệu Gym đạt 12.','',0,'nhanh','chi_mang',3,'nhiem_vu','Mười hai',1,0,0,'thanh_tuu',1197,1),
(10198,'nv_198','Mười sáu ngọn đèn','Phần thưởng nhiệm vụ A-029: Huy hiệu Gym đạt 16.','',0,'cong','khang_trang_thai',3,'nhiem_vu','Mười sáu ngọn đèn',1,0,0,'thanh_tuu',1198,1),
(10199,'nv_199','Thợ rèn tập sự','Phần thưởng nhiệm vụ A-030: Nâng cấp đạt 5.','',0,'loc','hp',3,'nhiem_vu','Thợ rèn tập sự',1,0,0,'thanh_tuu',1199,1)
ON DUPLICATE KEY UPDATE `ma`=VALUES(`ma`), `ten`=VALUES(`ten`), `mo_ta`=VALUES(`mo_ta`), `goi_y`=VALUES(`goi_y`), `do_hiem`=VALUES(`do_hiem`), `hinh`=VALUES(`hinh`), `dieu_kien`=VALUES(`dieu_kien`), `tham_so`=VALUES(`tham_so`), `so_luong`=VALUES(`so_luong`), `an`=VALUES(`an`), `thoi_han_phut`=VALUES(`thoi_han_phut`), `nguon`=VALUES(`nguon`), `thu_tu`=VALUES(`thu_tu`);

INSERT INTO `data_danh_hieu` (`id`,`ma`,`ten`,`mo_ta`,`goi_y`,`do_hiem`,`hinh`,`chi_so`,`phan_nghin`,`dieu_kien`,`tham_so`,`so_luong`,`an`,`thoi_han_phut`,`nguon`,`thu_tu`,`bat`) VALUES
(10200,'nv_200','Thợ rèn khéo tay','Phần thưởng nhiệm vụ A-031: Nâng cấp đạt 8.','',0,'sao','ne_tranh',3,'nhiem_vu','Thợ rèn khéo tay',1,0,0,'thanh_tuu',1200,1),
(10201,'nv_201','Thợ rèn lành nghề','Phần thưởng nhiệm vụ A-032: Nâng cấp đạt 10.','',0,'la','chi_mang',3,'nhiem_vu','Thợ rèn lành nghề',1,0,0,'thanh_tuu',1201,1),
(10202,'nv_202','Thợ cả','Phần thưởng nhiệm vụ A-033: Nâng cấp đạt 12.','',0,'tim','khang_trang_thai',3,'nhiem_vu','Thợ cả',1,0,0,'thanh_tuu',1202,1),
(10203,'nv_203','Thợ rèn bậc thầy','Phần thưởng nhiệm vụ A-034: Nâng cấp đạt 15.','',0,'khien','hp',3,'nhiem_vu','Thợ rèn bậc thầy',1,0,0,'thanh_tuu',1203,1),
(10204,'nv_204','Người hàng xóm tốt','Phần thưởng nhiệm vụ A-035: Nhiệm vụ phụ đạt 10.','',0,'giot','ne_tranh',3,'nhiem_vu','Người hàng xóm tốt',1,0,0,'thanh_tuu',1204,1),
(10205,'nv_205','Người bạn của mọi nhà','Phần thưởng nhiệm vụ A-036: Nhiệm vụ phụ đạt 50.','',0,'lua','chi_mang',3,'nhiem_vu','Người bạn của mọi nhà',1,0,0,'thanh_tuu',1205,1),
(10206,'nv_206','Huyền thoại thị trấn','Phần thưởng nhiệm vụ A-037: Nhiệm vụ phụ đạt 100.','',0,'set','khang_trang_thai',3,'nhiem_vu','Huyền thoại thị trấn',1,0,0,'thanh_tuu',1206,1),
(10207,'nv_207','Người không thể thiếu','Phần thưởng nhiệm vụ A-038: Nhiệm vụ phụ đạt 200.','',0,'nhanh','hp',3,'nhiem_vu','Người không thể thiếu',1,0,0,'thanh_tuu',1207,1),
(10208,'nv_208','Linh hồn của ba vùng','Phần thưởng nhiệm vụ A-039: Nhiệm vụ phụ đạt 400.','',0,'cong','ne_tranh',3,'nhiem_vu','Linh hồn của ba vùng',1,0,0,'thanh_tuu',1208,1),
(10209,'nv_209','Một tuần trọn vẹn','Phần thưởng nhiệm vụ A-040: Chăm chỉ đạt 7.','',0,'loc','chi_mang',3,'nhiem_vu','Một tuần trọn vẹn',1,0,0,'thanh_tuu',1209,1),
(10210,'nv_210','Chăm chỉ 30 ngày','Phần thưởng nhiệm vụ A-041: Chăm chỉ đạt 30.','',0,'sao','khang_trang_thai',3,'nhiem_vu','Chăm chỉ 30 ngày',1,0,0,'thanh_tuu',1210,1),
(10211,'nv_211','Chăm chỉ 100 ngày','Phần thưởng nhiệm vụ A-042: Chăm chỉ đạt 100.','',0,'la','hp',3,'nhiem_vu','Chăm chỉ 100 ngày',1,0,0,'thanh_tuu',1211,1),
(10212,'nv_212','Chăm chỉ 200 ngày','Phần thưởng nhiệm vụ A-043: Chăm chỉ đạt 200.','',0,'tim','ne_tranh',3,'nhiem_vu','Chăm chỉ 200 ngày',1,0,0,'thanh_tuu',1212,1),
(10213,'nv_213','Chăm chỉ 365 ngày','Phần thưởng nhiệm vụ A-044: Chăm chỉ đạt 365.','',0,'khien','chi_mang',3,'nhiem_vu','Chăm chỉ 365 ngày',1,0,0,'thanh_tuu',1213,1),
(10214,'nv_214','Trận thắng đầu tiên','Phần thưởng nhiệm vụ A-045: Đấu trường đạt 1.','',0,'giot','khang_trang_thai',3,'nhiem_vu','Trận thắng đầu tiên',1,0,0,'thanh_tuu',1214,1),
(10215,'nv_215','Đấu trường: 10 trận thắng','Phần thưởng nhiệm vụ A-046: Đấu trường đạt 10.','',0,'lua','hp',3,'nhiem_vu','Đấu trường: 10 trận thắng',1,0,0,'thanh_tuu',1215,1),
(10216,'nv_216','Đấu trường: 50 trận thắng','Phần thưởng nhiệm vụ A-047: Đấu trường đạt 50.','',0,'set','ne_tranh',3,'nhiem_vu','Đấu trường: 50 trận thắng',1,0,0,'thanh_tuu',1216,1),
(10217,'nv_217','Đấu trường: 100 trận thắng','Phần thưởng nhiệm vụ A-048: Đấu trường đạt 100.','',0,'nhanh','chi_mang',3,'nhiem_vu','Đấu trường: 100 trận thắng',1,0,0,'thanh_tuu',1217,1),
(10218,'nv_218','Đấu trường: 500 trận thắng','Phần thưởng nhiệm vụ A-049: Đấu trường đạt 500.','',0,'cong','khang_trang_thai',3,'nhiem_vu','Đấu trường: 500 trận thắng',1,0,0,'thanh_tuu',1218,1),
(10219,'nv_219','Lần đầu đột kích','Phần thưởng nhiệm vụ A-050: Boss thế giới đạt 1.','',0,'loc','hp',3,'nhiem_vu','Lần đầu đột kích',1,0,0,'thanh_tuu',1219,1),
(10220,'nv_220','Săn huyền thoại','Phần thưởng nhiệm vụ A-051: Boss thế giới đạt 10.','',0,'sao','ne_tranh',3,'nhiem_vu','Săn huyền thoại',1,0,0,'thanh_tuu',1220,1),
(10221,'nv_221','Người săn boss','Phần thưởng nhiệm vụ A-052: Boss thế giới đạt 50.','',0,'la','chi_mang',3,'nhiem_vu','Người săn boss',1,0,0,'thanh_tuu',1221,1),
(10222,'nv_222','Khắc tinh của boss','Phần thưởng nhiệm vụ A-053: Boss thế giới đạt 100.','',0,'tim','khang_trang_thai',3,'nhiem_vu','Khắc tinh của boss',1,0,0,'thanh_tuu',1222,1),
(10223,'nv_223','Thành viên đội','Phần thưởng nhiệm vụ A-054: Đội đạt 1.','',0,'khien','hp',3,'nhiem_vu','Thành viên đội',1,0,0,'thanh_tuu',1223,1),
(10224,'nv_224','Đội mạnh nhất tuần','Phần thưởng nhiệm vụ A-055: Đội đạt 10.','',0,'giot','ne_tranh',3,'nhiem_vu','Đội mạnh nhất tuần',1,0,0,'thanh_tuu',1224,1),
(10225,'nv_225','Trụ cột của đội','Phần thưởng nhiệm vụ A-056: Đội đạt 50.','',0,'lua','chi_mang',3,'nhiem_vu','Trụ cột của đội',1,0,0,'thanh_tuu',1225,1),
(10226,'nv_226','Đủ bộ trang bị','Phần thưởng nhiệm vụ A-057: Bộ trang bị đạt 1.','',0,'set','khang_trang_thai',3,'nhiem_vu','Đủ bộ trang bị',1,0,0,'thanh_tuu',1226,1),
(10227,'nv_227','Bộ trang bị tinh anh','Phần thưởng nhiệm vụ A-058: Bộ trang bị đạt 3.','',0,'nhanh','hp',3,'nhiem_vu','Bộ trang bị tinh anh',1,0,0,'thanh_tuu',1227,1),
(10228,'nv_228','Tủ đồ ba vùng','Phần thưởng nhiệm vụ A-059: Bộ trang bị đạt 6.','',0,'cong','ne_tranh',3,'nhiem_vu','Tủ đồ ba vùng',1,0,0,'thanh_tuu',1228,1),
(10229,'nv_229','Người vui hội','Phần thưởng nhiệm vụ A-060: Sự kiện đạt 5.','',0,'loc','chi_mang',3,'nhiem_vu','Người vui hội',1,0,0,'thanh_tuu',1229,1),
(10230,'nv_230','Thánh sự kiện','Phần thưởng nhiệm vụ A-061: Sự kiện đạt 20.','',0,'sao','khang_trang_thai',3,'nhiem_vu','Thánh sự kiện',1,0,0,'thanh_tuu',1230,1),
(10231,'nv_231','Không bỏ lỡ tuần nào','Phần thưởng nhiệm vụ A-062: Sự kiện đạt 52.','',0,'la','hp',3,'nhiem_vu','Không bỏ lỡ tuần nào',1,0,0,'thanh_tuu',1231,1),
(10232,'nv_232','Cần câu mới','Phần thưởng nhiệm vụ A-063: Câu cá đạt 10.','',0,'tim','ne_tranh',3,'nhiem_vu','Cần câu mới',1,0,0,'thanh_tuu',1232,1),
(10233,'nv_233','Ngư ông','Phần thưởng nhiệm vụ A-064: Câu cá đạt 100.','',0,'khien','chi_mang',3,'nhiem_vu','Ngư ông',1,0,0,'thanh_tuu',1233,1),
(10234,'nv_234','Vua câu cá','Phần thưởng nhiệm vụ A-065: Câu cá đạt 500.','',0,'giot','khang_trang_thai',3,'nhiem_vu','Vua câu cá',1,0,0,'thanh_tuu',1234,1),
(10235,'nv_235','Huyền thoại sông hồ','Phần thưởng nhiệm vụ A-066: Câu cá đạt 1.000.','',0,'lua','hp',3,'nhiem_vu','Huyền thoại sông hồ',1,0,0,'thanh_tuu',1235,1),
(10236,'nv_236','Xẻng mới','Phần thưởng nhiệm vụ A-067: Đào đất đạt 10.','',0,'set','ne_tranh',3,'nhiem_vu','Xẻng mới',1,0,0,'thanh_tuu',1236,1),
(10237,'nv_237','Thợ đào','Phần thưởng nhiệm vụ A-068: Đào đất đạt 100.','',0,'nhanh','chi_mang',3,'nhiem_vu','Thợ đào',1,0,0,'thanh_tuu',1237,1),
(10238,'nv_238','Thợ đào huyền thoại','Phần thưởng nhiệm vụ A-069: Đào đất đạt 500.','',0,'cong','khang_trang_thai',3,'nhiem_vu','Thợ đào huyền thoại',1,0,0,'thanh_tuu',1238,1),
(10239,'nv_239','Bếp nhỏ','Phần thưởng nhiệm vụ A-070: Nấu ăn đạt 10.','',0,'loc','hp',3,'nhiem_vu','Bếp nhỏ',1,0,0,'thanh_tuu',1239,1),
(10240,'nv_240','Đầu bếp','Phần thưởng nhiệm vụ A-071: Nấu ăn đạt 100.','',0,'sao','ne_tranh',3,'nhiem_vu','Đầu bếp',1,0,0,'thanh_tuu',1240,1),
(10241,'nv_241','Siêu đầu bếp','Phần thưởng nhiệm vụ A-072: Nấu ăn đạt 300.','',0,'la','chi_mang',3,'nhiem_vu','Siêu đầu bếp',1,0,0,'thanh_tuu',1241,1),
(10242,'nv_242','Trứng đầu tiên','Phần thưởng nhiệm vụ A-073: Trứng nở đạt 1.','',0,'tim','khang_trang_thai',3,'nhiem_vu','Trứng đầu tiên',1,0,0,'thanh_tuu',1242,1),
(10243,'nv_243','Nhà trẻ đông vui','Phần thưởng nhiệm vụ A-074: Trứng nở đạt 20.','',0,'khien','hp',3,'nhiem_vu','Nhà trẻ đông vui',1,0,0,'thanh_tuu',1243,1),
(10244,'nv_244','Bảo mẫu ba vùng','Phần thưởng nhiệm vụ A-075: Trứng nở đạt 100.','',0,'giot','ne_tranh',3,'nhiem_vu','Bảo mẫu ba vùng',1,0,0,'thanh_tuu',1244,1),
(10245,'nv_245','Cha mẹ của trăm Pokémon','Phần thưởng nhiệm vụ A-076: Trứng nở đạt 300.','',0,'lua','chi_mang',3,'nhiem_vu','Cha mẹ của trăm Pokémon',1,0,0,'thanh_tuu',1245,1),
(10246,'nv_246','Hạ Alpha đầu tiên','Phần thưởng nhiệm vụ A-077: Alpha đạt 1.','',0,'set','khang_trang_thai',3,'nhiem_vu','Hạ Alpha đầu tiên',1,0,0,'thanh_tuu',1246,1),
(10247,'nv_247','Thợ săn Alpha','Phần thưởng nhiệm vụ A-078: Alpha đạt 10.','',0,'nhanh','hp',3,'nhiem_vu','Thợ săn Alpha',1,0,0,'thanh_tuu',1247,1),
(10248,'nv_248','Khắc tinh Alpha','Phần thưởng nhiệm vụ A-079: Alpha đạt 50.','',0,'cong','ne_tranh',3,'nhiem_vu','Khắc tinh Alpha',1,0,0,'thanh_tuu',1248,1),
(10249,'nv_249','Vua Alpha','Phần thưởng nhiệm vụ A-080: Alpha đạt 200.','',0,'loc','chi_mang',3,'nhiem_vu','Vua Alpha',1,0,0,'thanh_tuu',1249,1),
(10250,'nv_250','Người đi đường','Phần thưởng nhiệm vụ A-081: Khám phá đạt 10.','',0,'sao','khang_trang_thai',3,'nhiem_vu','Người đi đường',1,0,0,'thanh_tuu',1250,1),
(10251,'nv_251','Nhà thám hiểm','Phần thưởng nhiệm vụ A-082: Khám phá đạt 30.','',0,'la','hp',3,'nhiem_vu','Nhà thám hiểm',1,0,0,'thanh_tuu',1251,1),
(10252,'nv_252','Người vẽ bản đồ','Phần thưởng nhiệm vụ A-083: Khám phá đạt 60.','',0,'tim','ne_tranh',3,'nhiem_vu','Người vẽ bản đồ',1,0,0,'thanh_tuu',1252,1),
(10253,'nv_253','Đặt chân mọi nơi','Phần thưởng nhiệm vụ A-084: Khám phá đạt 90.','',0,'khien','chi_mang',3,'nhiem_vu','Đặt chân mọi nơi',1,0,0,'thanh_tuu',1253,1),
(10254,'nv_254','Người thầy đầu tiên','Phần thưởng nhiệm vụ A-085: Sư phạm đạt 1.','',0,'giot','khang_trang_thai',3,'nhiem_vu','Người thầy đầu tiên',1,0,0,'thanh_tuu',1254,1),
(10255,'nv_255','Người thầy tận tâm','Phần thưởng nhiệm vụ A-086: Sư phạm đạt 5.','',0,'lua','hp',3,'nhiem_vu','Người thầy tận tâm',1,0,0,'thanh_tuu',1255,1),
(10256,'nv_256','Bậc thầy dạy dỗ','Phần thưởng nhiệm vụ A-087: Sư phạm đạt 20.','',0,'set','ne_tranh',3,'nhiem_vu','Bậc thầy dạy dỗ',1,0,0,'thanh_tuu',1256,1),
(10257,'nv_257','Món đồ đầu tay','Phần thưởng nhiệm vụ A-088: Chế tạo đạt 1.','',0,'nhanh','chi_mang',3,'nhiem_vu','Món đồ đầu tay',1,0,0,'thanh_tuu',1257,1),
(10258,'nv_258','Thợ chế tạo','Phần thưởng nhiệm vụ A-089: Chế tạo đạt 20.','',0,'cong','khang_trang_thai',3,'nhiem_vu','Thợ chế tạo',1,0,0,'thanh_tuu',1258,1),
(10259,'nv_259','Nghệ nhân','Phần thưởng nhiệm vụ A-090: Chế tạo đạt 100.','',0,'loc','hp',3,'nhiem_vu','Nghệ nhân',1,0,0,'thanh_tuu',1259,1),
(10260,'nv_260','Màu sắc lạ','Phần thưởng nhiệm vụ A-091: Shiny đạt 1.','',0,'sao','ne_tranh',3,'nhiem_vu','Màu sắc lạ',1,0,0,'thanh_tuu',1260,1),
(10261,'nv_261','Thợ săn shiny','Phần thưởng nhiệm vụ A-092: Shiny đạt 5.','',0,'la','chi_mang',3,'nhiem_vu','Thợ săn shiny',1,0,0,'thanh_tuu',1261,1),
(10262,'nv_262','Bậc thầy shiny','Phần thưởng nhiệm vụ A-093: Shiny đạt 20.','',0,'tim','khang_trang_thai',3,'nhiem_vu','Bậc thầy shiny',1,0,0,'thanh_tuu',1262,1),
(10263,'nv_263','Hầm ngục tầng 10','Phần thưởng nhiệm vụ A-094: Hầm ngục đạt 10.','',0,'khien','hp',3,'nhiem_vu','Hầm ngục tầng 10',1,0,0,'thanh_tuu',1263,1),
(10264,'nv_264','Hầm ngục tầng 30','Phần thưởng nhiệm vụ A-095: Hầm ngục đạt 30.','',0,'giot','ne_tranh',3,'nhiem_vu','Hầm ngục tầng 30',1,0,0,'thanh_tuu',1264,1),
(10265,'nv_265','Đáy hầm ngục','Phần thưởng nhiệm vụ A-096: Hầm ngục đạt 50.','',0,'lua','chi_mang',3,'nhiem_vu','Đáy hầm ngục',1,0,0,'thanh_tuu',1265,1),
(10266,'nv_266','Đầu óc nhanh nhạy','Phần thưởng nhiệm vụ A-097: Câu đố đạt 5.','',0,'set','khang_trang_thai',3,'nhiem_vu','Đầu óc nhanh nhạy',1,0,0,'thanh_tuu',1266,1),
(10267,'nv_267','Nhà thông thái','Phần thưởng nhiệm vụ A-098: Câu đố đạt 25.','',0,'nhanh','hp',3,'nhiem_vu','Nhà thông thái',1,0,0,'thanh_tuu',1267,1),
(10268,'nv_268','Bậc thầy câu đố','Phần thưởng nhiệm vụ A-099: Câu đố đạt 60.','',0,'cong','ne_tranh',3,'nhiem_vu','Bậc thầy câu đố',1,0,0,'thanh_tuu',1268,1),
(10269,'nv_269','Người được huyền thoại chọn','Phần thưởng nhiệm vụ A-100: Huyền thoại đạt 1.','',0,'loc','chi_mang',3,'nhiem_vu','Người được huyền thoại chọn',1,0,0,'thanh_tuu',1269,1),
(10270,'nv_270','Bạn của huyền thoại','Phần thưởng nhiệm vụ A-101: Huyền thoại đạt 5.','',0,'sao','khang_trang_thai',3,'nhiem_vu','Bạn của huyền thoại',1,0,0,'thanh_tuu',1270,1),
(10271,'nv_271','Sử thi ba vùng','Phần thưởng nhiệm vụ A-102: Huyền thoại đạt 12.','',0,'la','hp',3,'nhiem_vu','Sử thi ba vùng',1,0,0,'thanh_tuu',1271,1),
(10272,'nv_272','Kẻ tò mò','Phần thưởng nhiệm vụ A-103: Bí ẩn đạt 1.','',0,'tim','ne_tranh',3,'nhiem_vu','Kẻ tò mò',1,0,0,'thanh_tuu',1272,1),
(10273,'nv_273','Người tìm bí mật','Phần thưởng nhiệm vụ A-104: Bí ẩn đạt 10.','',0,'khien','chi_mang',3,'nhiem_vu','Người tìm bí mật',1,0,0,'thanh_tuu',1273,1),
(10274,'nv_274','Không còn bí mật','Phần thưởng nhiệm vụ A-105: Bí ẩn đạt 32.','',0,'giot','khang_trang_thai',3,'nhiem_vu','Không còn bí mật',1,0,0,'thanh_tuu',1274,1),
(10275,'nv_275','Bảng ngày quen thuộc','Phần thưởng nhiệm vụ A-106: Nhiệm vụ ngày đạt 10.','',0,'lua','hp',3,'nhiem_vu','Bảng ngày quen thuộc',1,0,0,'thanh_tuu',1275,1),
(10276,'nv_276','Người của bảng ngày','Phần thưởng nhiệm vụ A-107: Nhiệm vụ ngày đạt 100.','',0,'set','ne_tranh',3,'nhiem_vu','Người của bảng ngày',1,0,0,'thanh_tuu',1276,1),
(10277,'nv_277','Trụ cột bảng ngày','Phần thưởng nhiệm vụ A-108: Nhiệm vụ ngày đạt 500.','',0,'nhanh','chi_mang',3,'nhiem_vu','Trụ cột bảng ngày',1,0,0,'thanh_tuu',1277,1),
(10278,'nv_278','Bảng ngày là nhà','Phần thưởng nhiệm vụ A-109: Nhiệm vụ ngày đạt 1.000.','',0,'cong','khang_trang_thai',3,'nhiem_vu','Bảng ngày là nhà',1,0,0,'thanh_tuu',1278,1),
(10279,'nv_279','Tuần nào cũng có mặt','Phần thưởng nhiệm vụ A-110: Nhiệm vụ tuần đạt 10.','',0,'loc','hp',3,'nhiem_vu','Tuần nào cũng có mặt',1,0,0,'thanh_tuu',1279,1),
(10280,'nv_280','Người của bảng tuần','Phần thưởng nhiệm vụ A-111: Nhiệm vụ tuần đạt 50.','',0,'sao','ne_tranh',3,'nhiem_vu','Người của bảng tuần',1,0,0,'thanh_tuu',1280,1),
(10281,'nv_281','Trăm tuần','Phần thưởng nhiệm vụ A-112: Nhiệm vụ tuần đạt 100.','',0,'la','chi_mang',3,'nhiem_vu','Trăm tuần',1,0,0,'thanh_tuu',1281,1),
(10282,'nv_282','Người quyết đoán','Phần thưởng nhiệm vụ A-113: Lựa chọn đạt 5.','',0,'tim','khang_trang_thai',3,'nhiem_vu','Người quyết đoán',1,0,0,'thanh_tuu',1282,1),
(10283,'nv_283','Người chọn đường','Phần thưởng nhiệm vụ A-114: Lựa chọn đạt 15.','',0,'khien','hp',3,'nhiem_vu','Người chọn đường',1,0,0,'thanh_tuu',1283,1)
ON DUPLICATE KEY UPDATE `ma`=VALUES(`ma`), `ten`=VALUES(`ten`), `mo_ta`=VALUES(`mo_ta`), `goi_y`=VALUES(`goi_y`), `do_hiem`=VALUES(`do_hiem`), `hinh`=VALUES(`hinh`), `dieu_kien`=VALUES(`dieu_kien`), `tham_so`=VALUES(`tham_so`), `so_luong`=VALUES(`so_luong`), `an`=VALUES(`an`), `thoi_han_phut`=VALUES(`thoi_han_phut`), `nguon`=VALUES(`nguon`), `thu_tu`=VALUES(`thu_tu`);

-- Cấu hình (khoá cố định trong tools/cauhinh/danh_muc.json; dòng CSDL chỉ để đọc/ghi chú): bật + trần ô danh hiệu.
INSERT IGNORE INTO `cau_hinh` (`khoa`, `gia_tri`) VALUES ('danh_hieu.bat', '1'), ('danh_hieu.tran_phan_nghin', '10');
-- >>> them_moc_nhiem_vu.sql
-- =====================================================================================================
-- ĐIỂM MÓC NHIỆM VỤ (0.9.7): vật phẩm thật của từng sự kiện + tiến độ chợ cho người bán offline.
-- Nâng cấp IDEMPOTENT (MariaDB 10.3, utf8), tự chứa, nối được vào sql/nang_cap.sql.
--
--   data_su_kien_vat_pham  vật phẩm (id trong potions[] / data_potion) mà CODE của từng sự kiện thả / thưởng vào túi:
--                          quái rơi (real.Monster.onDropItem), NPC sự kiện (MapMenuNpc), giao hàng (TraHang), chế đồ /
--                          tiến giải Vu Lan (MapCheTao, Map). Kiểu mục tiêu "su_kien_vat_pham" (real.DiemMocNhiemVu)
--                          chỉ đếm vật phẩm thuộc một sự kiện ĐANG BẬT (SuKien.bat(ma_su_kien)). Nhiệm vụ đặt
--                          muc_tieu_id = item_id để nhắm một vật phẩm, để trống = bất kỳ vật phẩm của sự kiện đang chạy.
--                          Sự kiện trong lịch năm KHÔNG có vật phẩm rơi trong code (tet123, quocte83, gioto,
--                          demhalloween, sinhnhat2011, blackfriday*, noel, demnoel) không có dòng.
--   tob_nhiem_vu_cho       người bán OFFLINE bán được hàng trên Chợ sinh thái: một dòng chờ, áp vào nhiệm vụ "cho_ban"
--                          lần đầu vào bản đồ (da_ap 0 -> 1, mỗi dòng áp đúng một lần).
-- Kiểm: server/test/KiemPvpPk (mọi id vật phẩm trong các khối rơi sự kiện của Monster.java đều có dòng ở đây).
-- =====================================================================================================

CREATE TABLE IF NOT EXISTS `data_su_kien_vat_pham` (
  `ma_su_kien` VARCHAR(32) NOT NULL,
  `item_id` INT NOT NULL,
  `ten` VARCHAR(64) NOT NULL DEFAULT '',
  `nguon` VARCHAR(128) NOT NULL DEFAULT '',
  PRIMARY KEY (`ma_su_kien`, `item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `data_su_kien_vat_pham` (`ma_su_kien`, `item_id`, `ten`, `nguon`) VALUES
('trungthu', 120, 'Thịt', 'Monster: quái rơi nguyên liệu Trung Thu'),
('trungthu', 121, 'Trứng', 'Monster: quái rơi nguyên liệu Trung Thu'),
('trungthu', 142, 'Bột mỳ', 'Monster: quái rơi nguyên liệu Trung Thu; MapMenuNpc: hộp quà NPC'),
('trungthu', 122, 'Đậu xanh', 'Monster: quái rơi khi có buff sôcôla'),
('tet2017', 148, 'Gạo nếp', 'Monster: quái rơi nguyên liệu bánh chưng'),
('tet2017', 122, 'Đậu xanh', 'Monster: quái rơi nguyên liệu bánh chưng'),
('tet2017', 149, 'Thịt mỡ', 'Monster: quái rơi nguyên liệu bánh chưng'),
('tet2017', 150, 'Lá dong', 'Monster: quái rơi nguyên liệu bánh chưng'),
('tet2017', 153, 'Giấy', 'Monster: quái rơi nguyên liệu pháo'),
('tet2017', 154, 'Thuốc pháo', 'Monster: quái rơi nguyên liệu pháo'),
('halloween', 137, 'Kẹo', 'Monster: quái rơi'),
('halloween', 138, 'Bí ngô', 'Monster: quái rơi'),
('tetduonglich', 137, 'Kẹo', 'Monster: quái rơi (mặc gậy Giáng sinh)'),
('noel2023', 145, 'Dưa Hấu', 'Monster: quái rơi'),
('he2017', 146, 'Kem', 'Monster: quái rơi'),
('vulan', 106, 'Nhánh hoa hồng vàng', 'Monster: quái rơi; MapCheTao / Map: chế đồ, tiến giải'),
('worldcup2017', 160, 'Quả bóng bạc', 'Monster: quái rơi; TraHang: giao hàng'),
('worldcup', 161, 'Quả bóng vàng', 'SuKienWorldCup: quái / boss rơi'),
('mininuichaubau', 77, 'Mảnh bản đồ 1', 'Monster: quái rơi')
ON DUPLICATE KEY UPDATE `ten` = VALUES(`ten`), `nguon` = VALUES(`nguon`);

CREATE TABLE IF NOT EXISTS `tob_nhiem_vu_cho` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `id_char` INT NOT NULL,
  `so` INT NOT NULL DEFAULT 1,
  `ghi_chu` VARCHAR(128) NOT NULL DEFAULT '',
  `da_ap` TINYINT NOT NULL DEFAULT 0,
  `luc` DATETIME NOT NULL,
  `ap_luc` DATETIME NULL,
  PRIMARY KEY (`id`),
  KEY `idx_nvc_char` (`id_char`, `da_ap`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ======================================================================================================================
-- 11. 0.9.8 (ban-098: hoc-chieu, bo-ngoc-bat, pho-ban-cup, mua-thoi-tiet): giu nguyen van ban cac tep le cua tung nhanh
--     tru lenh danh muc cau hinh (cau_hinh_danh_muc / cau_hinh_nhom nam o phan 2) va SET NAMES. Moi tep mo dau bang dong
--     "-- >>> <ten tep>" (test doc tung phan: test/DayDu.phanNangCap).
-- ======================================================================================================================
-- >>> hoc_chieu_huan_luyen.sql
-- =====================================================================================================================
-- HỌC CHIÊU THEO LOÀI + ĐIỂM HUẤN LUYỆN (thay điểm tiềm năng tự do) — bản nâng cấp idempotent (MariaDB 10.3, utf8).
-- Chạy lại bao nhiêu lần cũng được. Game server cũng tự tạo bảng khi thiếu (data.DatabaseHocChieu) nhưng DỮ LIỆU
-- learnset chỉ có ở đây / trong CSDL: thiếu dữ liệu thì máy chủ giữ luật cũ (không học chiêu theo loài).
-- Thiết kế: docs/thiet-ke-game.md §2.3 (chiêu) và §4.4b (Điểm huấn luyện). Kiểm thử: server/test/KiemHocChieu.java.
-- Luật P4: tệp này KHÔNG sửa dữ liệu người chơi; chuyển đổi nhân vật cũ chạy trong game lúc nạp nhân vật
-- (real.HocChieuGame, đánh dấu tob_chuyen_doi_can_bang), mỗi nhân vật đúng một lần.
-- =====================================================================================================================

-- ---------------------------------------------------------------------------------------------------------- learnset
-- Mỗi loài (số Pokédex) một dòng cho mỗi ô chiêu nó học được. Ô = số hiệu chiêu của dòng (client, icon, SKILL_*).
-- nguon: 'cap' = tự học khi đủ cấp (cấp chiêu 1, không tốn điểm); 'npc' = học ở NPC Blaziken bằng gia_xu.
-- cap_cap_chieu: 10 mốc "a/b/...": mốc 0 = cấp học, mốc k = cấp Pokémon cần để chiêu lên cấp k+1 (điểm kỹ năng).
-- Loài chưa có ô nào thì ô đó học khi tiến hoá (ví dụ Xoáy lửa: Charmeleon cấp 16; Súng phun lửa: Charizard cấp 36).
CREATE TABLE IF NOT EXISTS data_learnset (
  loai SMALLINT NOT NULL,
  o TINYINT NOT NULL,
  dong TINYINT NOT NULL,
  ten VARCHAR(48) NOT NULL,
  he TINYINT NOT NULL,
  kieu VARCHAR(16) NOT NULL,
  nguon VARCHAR(8) NOT NULL DEFAULT 'cap',
  cap_hoc TINYINT NOT NULL,
  gia_xu INT NOT NULL DEFAULT 0,
  cap_cap_chieu VARCHAR(64) NOT NULL,
  sua_boi VARCHAR(50) NULL,
  sua_luc DATETIME NULL,
  PRIMARY KEY (loai, o),
  KEY k_dong (dong)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO data_learnset (loai, o, dong, ten, he, kieu, nguon, cap_hoc, gia_xu, cap_cap_chieu) VALUES
(4,0,0,'Cào',0,'thuong','cap',1,0,'1/2/3/4/5/6/7/8/9/10'),
(4,1,0,'Than hồng',1,'tan_cong','cap',4,0,'4/7/8/9/10/11/12/13/14/15'),
(4,2,0,'Nanh lửa',1,'tan_cong','cap',7,0,'7/12/13/14/15/16/17/18/19/20'),
(4,5,0,'Vũ điệu kiếm',0,'bi_dong','cap',6,0,'6/9/12/14/16/18/20/22/24/26'),
(5,0,0,'Cào',0,'thuong','cap',1,0,'1/2/3/4/5/6/7/8/9/10'),
(5,1,0,'Than hồng',1,'tan_cong','cap',4,0,'4/7/8/9/10/11/12/13/14/15'),
(5,2,0,'Nanh lửa',1,'tan_cong','cap',7,0,'7/12/13/14/15/16/17/18/19/20'),
(5,3,0,'Xoáy lửa',1,'nhieu_don','cap',16,0,'16/20/22/24/26/30/33/36/39/42'),
(5,4,0,'Màn khói',0,'buff','cap',16,0,'16/17/18/19/20/24/26/28/30/32'),
(5,5,0,'Vũ điệu kiếm',0,'bi_dong','cap',6,0,'6/9/12/14/16/18/20/22/24/26'),
(6,0,0,'Cào',0,'thuong','cap',1,0,'1/2/3/4/5/6/7/8/9/10'),
(6,1,0,'Than hồng',1,'tan_cong','cap',4,0,'4/7/8/9/10/11/12/13/14/15'),
(6,2,0,'Nanh lửa',1,'tan_cong','cap',7,0,'7/12/13/14/15/16/17/18/19/20'),
(6,3,0,'Xoáy lửa',1,'nhieu_don','cap',16,0,'16/20/22/24/26/30/33/36/39/42'),
(6,4,0,'Màn khói',0,'buff','cap',16,0,'16/17/18/19/20/24/26/28/30/32'),
(6,5,0,'Vũ điệu kiếm',0,'bi_dong','cap',6,0,'6/9/12/14/16/18/20/22/24/26'),
(6,6,0,'Súng phun lửa',1,'tan_cong','npc',36,60000,'36/37/38/39/40/41/42/43/44/45'),
(6,7,0,'Vuốt rồng',14,'tan_cong','npc',36,120000,'36/37/38/39/40/41/42/44/46/48'),
(6,8,0,'Đại hỏa',1,'toi_thuong','npc',36,300000,'36/45/47/49/51/53/55/57/59/61'),
(1,0,1,'Húc',0,'thuong','cap',1,0,'1/2/3/4/5/6/7/8/9/10'),
(1,1,1,'Roi dây leo',4,'tan_cong','cap',4,0,'4/7/8/9/10/11/12/13/14/15'),
(1,2,1,'Lá dao',4,'tan_cong','cap',7,0,'7/12/13/14/15/16/17/18/19/20'),
(1,5,1,'Sinh trưởng',0,'bi_dong','cap',6,0,'6/9/12/14/16/18/20/22/24/26'),
(2,0,1,'Húc',0,'thuong','cap',1,0,'1/2/3/4/5/6/7/8/9/10'),
(2,1,1,'Roi dây leo',4,'tan_cong','cap',4,0,'4/7/8/9/10/11/12/13/14/15'),
(2,2,1,'Lá dao',4,'tan_cong','cap',7,0,'7/12/13/14/15/16/17/18/19/20'),
(2,3,1,'Bom bùn',7,'nhieu_don','cap',16,0,'16/20/22/24/26/30/33/36/39/42'),
(2,4,1,'Tự vệ',0,'khien','cap',16,0,'16/17/18/19/20/24/26/28/30/32'),
(2,5,1,'Sinh trưởng',0,'bi_dong','cap',6,0,'6/9/12/14/16/18/20/22/24/26'),
(3,0,1,'Húc',0,'thuong','cap',1,0,'1/2/3/4/5/6/7/8/9/10'),
(3,1,1,'Roi dây leo',4,'tan_cong','cap',4,0,'4/7/8/9/10/11/12/13/14/15'),
(3,2,1,'Lá dao',4,'tan_cong','cap',7,0,'7/12/13/14/15/16/17/18/19/20'),
(3,3,1,'Bom bùn',7,'nhieu_don','cap',16,0,'16/20/22/24/26/30/33/36/39/42'),
(3,4,1,'Tự vệ',0,'khien','cap',16,0,'16/17/18/19/20/24/26/28/30/32'),
(3,5,1,'Sinh trưởng',0,'bi_dong','cap',6,0,'6/9/12/14/16/18/20/22/24/26'),
(3,6,1,'Bom hạt',4,'khong_che','npc',32,60000,'32/33/34/35/36/37/38/39/41/43'),
(3,7,1,'Bão lá',4,'tan_cong','npc',32,120000,'32/33/34/36/38/40/42/44/46/48'),
(3,8,1,'Tia mặt trời',4,'toi_thuong','npc',32,300000,'32/45/47/49/51/53/55/57/59/61'),
(133,0,2,'Húc',0,'thuong','cap',1,0,'1/2/3/4/5/6/7/8/9/10'),
(133,1,2,'Tấn công nhanh',0,'tan_cong','cap',4,0,'4/7/8/9/10/11/12/13/14/15'),
(133,2,2,'Cắn',15,'tan_cong','cap',7,0,'7/12/13/14/15/16/17/18/19/20'),
(133,3,2,'Sao băng',0,'nhieu_don','cap',16,0,'16/20/22/24/26/30/33/36/39/42'),
(133,4,2,'Mong ước',0,'hoi','cap',16,0,'16/17/18/19/20/24/26/28/30/32'),
(133,5,2,'Nghỉ ngơi',10,'bi_dong','cap',6,0,'6/7/9/12/18/24/26/28/30/32'),
(133,6,2,'Phước lành hồi sinh',0,'hoi_sinh','cap',25,0,'25/26/27/28/29/30/31/32/33/34'),
(133,7,2,'Chia sẻ nỗi đau',13,'buff','cap',25,0,'25/26/27/28/29/30/31/32/33/34'),
(133,8,2,'Đuôi sắt',16,'tan_cong','npc',25,60000,'25/27/29/31/33/35/37/39/41/43'),
(133,9,2,'Bứt phá cuối cùng',0,'tan_cong','npc',25,120000,'25/32/34/36/38/40/42/44/46/48'),
(133,10,2,'Tia phá hủy',0,'toi_thuong','npc',25,300000,'25/45/47/49/51/53/55/57/59/61'),
(134,0,2,'Húc',0,'thuong','cap',1,0,'1/2/3/4/5/6/7/8/9/10'),
(134,1,2,'Tấn công nhanh',0,'tan_cong','cap',4,0,'4/7/8/9/10/11/12/13/14/15'),
(134,2,2,'Súng nước',2,'tan_cong','cap',7,0,'7/12/13/14/15/16/17/18/19/20'),
(134,3,2,'Sao băng',0,'nhieu_don','cap',16,0,'16/20/22/24/26/30/33/36/39/42'),
(134,4,2,'Mong ước',0,'hoi','cap',16,0,'16/17/18/19/20/24/26/28/30/32'),
(134,5,2,'Nghỉ ngơi',10,'bi_dong','cap',6,0,'6/7/9/12/18/24/26/28/30/32'),
(134,6,2,'Phước lành hồi sinh',0,'hoi_sinh','cap',25,0,'25/26/27/28/29/30/31/32/33/34'),
(134,7,2,'Chia sẻ nỗi đau',13,'buff','cap',25,0,'25/26/27/28/29/30/31/32/33/34'),
(134,8,2,'Vòng nước',2,'tan_cong','npc',25,60000,'25/27/29/31/33/35/37/39/41/43'),
(134,9,2,'Bứt phá cuối cùng',0,'tan_cong','npc',25,120000,'25/32/34/36/38/40/42/44/46/48'),
(134,10,2,'Thủy triều',2,'toi_thuong','npc',25,300000,'25/45/47/49/51/53/55/57/59/61'),
(135,0,2,'Húc',0,'thuong','cap',1,0,'1/2/3/4/5/6/7/8/9/10'),
(135,1,2,'Tấn công nhanh',0,'tan_cong','cap',4,0,'4/7/8/9/10/11/12/13/14/15'),
(135,2,2,'Phóng điện',3,'tan_cong','cap',7,0,'7/12/13/14/15/16/17/18/19/20'),
(135,3,2,'Sao băng',0,'nhieu_don','cap',16,0,'16/20/22/24/26/30/33/36/39/42'),
(135,4,2,'Mong ước',0,'hoi','cap',16,0,'16/17/18/19/20/24/26/28/30/32'),
(135,5,2,'Nghỉ ngơi',10,'bi_dong','cap',6,0,'6/7/9/12/18/24/26/28/30/32'),
(135,6,2,'Phước lành hồi sinh',0,'hoi_sinh','cap',25,0,'25/26/27/28/29/30/31/32/33/34'),
(135,7,2,'Chia sẻ nỗi đau',13,'buff','cap',25,0,'25/26/27/28/29/30/31/32/33/34'),
(135,8,2,'Gai sấm',3,'tan_cong','npc',25,60000,'25/27/29/31/33/35/37/39/41/43'),
(135,9,2,'Bứt phá cuối cùng',0,'tan_cong','npc',25,120000,'25/32/34/36/38/40/42/44/46/48'),
(135,10,2,'Sấm sét',3,'toi_thuong','npc',25,300000,'25/45/47/49/51/53/55/57/59/61'),
(136,0,2,'Húc',0,'thuong','cap',1,0,'1/2/3/4/5/6/7/8/9/10'),
(136,1,2,'Tấn công nhanh',0,'tan_cong','cap',4,0,'4/7/8/9/10/11/12/13/14/15'),
(136,2,2,'Nanh lửa',1,'tan_cong','cap',7,0,'7/12/13/14/15/16/17/18/19/20'),
(136,3,2,'Sao băng',0,'nhieu_don','cap',16,0,'16/20/22/24/26/30/33/36/39/42'),
(136,4,2,'Mong ước',0,'hoi','cap',16,0,'16/17/18/19/20/24/26/28/30/32'),
(136,5,2,'Nghỉ ngơi',10,'bi_dong','cap',6,0,'6/7/9/12/18/24/26/28/30/32'),
(136,6,2,'Phước lành hồi sinh',0,'hoi_sinh','cap',25,0,'25/26/27/28/29/30/31/32/33/34'),
(136,7,2,'Chia sẻ nỗi đau',13,'buff','cap',25,0,'25/26/27/28/29/30/31/32/33/34'),
(136,8,2,'Hỏa diệm',1,'tan_cong','npc',25,60000,'25/27/29/31/33/35/37/39/41/43'),
(136,9,2,'Bứt phá cuối cùng',0,'tan_cong','npc',25,120000,'25/32/34/36/38/40/42/44/46/48'),
(136,10,2,'Đại hỏa',1,'toi_thuong','npc',25,300000,'25/45/47/49/51/53/55/57/59/61'),
(196,0,2,'Húc',0,'thuong','cap',1,0,'1/2/3/4/5/6/7/8/9/10'),
(196,1,2,'Tấn công nhanh',0,'tan_cong','cap',4,0,'4/7/8/9/10/11/12/13/14/15'),
(196,2,2,'Tâm niệm',10,'tan_cong','cap',7,0,'7/12/13/14/15/16/17/18/19/20'),
(196,3,2,'Sao băng',0,'nhieu_don','cap',16,0,'16/20/22/24/26/30/33/36/39/42'),
(196,4,2,'Mong ước',0,'hoi','cap',16,0,'16/17/18/19/20/24/26/28/30/32'),
(196,5,2,'Nghỉ ngơi',10,'bi_dong','cap',6,0,'6/7/9/12/18/24/26/28/30/32'),
(196,6,2,'Phước lành hồi sinh',0,'hoi_sinh','cap',25,0,'25/26/27/28/29/30/31/32/33/34'),
(196,7,2,'Chia sẻ nỗi đau',13,'buff','cap',25,0,'25/26/27/28/29/30/31/32/33/34'),
(196,8,2,'Ảo ảnh tâm linh',10,'tan_cong','npc',25,60000,'25/27/29/31/33/35/37/39/41/43'),
(196,9,2,'Bứt phá cuối cùng',0,'tan_cong','npc',25,120000,'25/32/34/36/38/40/42/44/46/48'),
(196,10,2,'Siêu năng',10,'toi_thuong','npc',25,300000,'25/45/47/49/51/53/55/57/59/61'),
(197,0,2,'Húc',0,'thuong','cap',1,0,'1/2/3/4/5/6/7/8/9/10'),
(197,1,2,'Tấn công nhanh',0,'tan_cong','cap',4,0,'4/7/8/9/10/11/12/13/14/15'),
(197,2,2,'Đòn hiểm',15,'tan_cong','cap',7,0,'7/12/13/14/15/16/17/18/19/20'),
(197,3,2,'Sao băng',0,'nhieu_don','cap',16,0,'16/20/22/24/26/30/33/36/39/42'),
(197,4,2,'Mong ước',0,'hoi','cap',16,0,'16/17/18/19/20/24/26/28/30/32'),
(197,5,2,'Nghỉ ngơi',10,'bi_dong','cap',6,0,'6/7/9/12/18/24/26/28/30/32'),
(197,6,2,'Phước lành hồi sinh',0,'hoi_sinh','cap',25,0,'25/26/27/28/29/30/31/32/33/34'),
(197,7,2,'Chia sẻ nỗi đau',13,'buff','cap',25,0,'25/26/27/28/29/30/31/32/33/34'),
(197,8,2,'Ánh trăng',15,'tan_cong','npc',25,60000,'25/27/29/31/33/35/37/39/41/43'),
(197,9,2,'Bứt phá cuối cùng',0,'tan_cong','npc',25,120000,'25/32/34/36/38/40/42/44/46/48'),
(197,10,2,'Hồn tối',15,'toi_thuong','npc',25,300000,'25/45/47/49/51/53/55/57/59/61'),
(470,0,2,'Húc',0,'thuong','cap',1,0,'1/2/3/4/5/6/7/8/9/10'),
(470,1,2,'Tấn công nhanh',0,'tan_cong','cap',4,0,'4/7/8/9/10/11/12/13/14/15'),
(470,2,2,'Lưỡi lá',4,'tan_cong','cap',7,0,'7/12/13/14/15/16/17/18/19/20'),
(470,3,2,'Sao băng',0,'nhieu_don','cap',16,0,'16/20/22/24/26/30/33/36/39/42'),
(470,4,2,'Mong ước',0,'hoi','cap',16,0,'16/17/18/19/20/24/26/28/30/32'),
(470,5,2,'Nghỉ ngơi',10,'bi_dong','cap',6,0,'6/7/9/12/18/24/26/28/30/32'),
(470,6,2,'Phước lành hồi sinh',0,'hoi_sinh','cap',25,0,'25/26/27/28/29/30/31/32/33/34'),
(470,7,2,'Chia sẻ nỗi đau',13,'buff','cap',25,0,'25/26/27/28/29/30/31/32/33/34'),
(470,8,2,'Lá dao',4,'tan_cong','npc',25,60000,'25/27/29/31/33/35/37/39/41/43'),
(470,9,2,'Bứt phá cuối cùng',0,'tan_cong','npc',25,120000,'25/32/34/36/38/40/42/44/46/48'),
(470,10,2,'Tia mặt trời',4,'toi_thuong','npc',25,300000,'25/45/47/49/51/53/55/57/59/61'),
(471,0,2,'Húc',0,'thuong','cap',1,0,'1/2/3/4/5/6/7/8/9/10'),
(471,1,2,'Tấn công nhanh',0,'tan_cong','cap',4,0,'4/7/8/9/10/11/12/13/14/15'),
(471,2,2,'Gió băng',5,'tan_cong','cap',7,0,'7/12/13/14/15/16/17/18/19/20'),
(471,3,2,'Sao băng',0,'nhieu_don','cap',16,0,'16/20/22/24/26/30/33/36/39/42'),
(471,4,2,'Mong ước',0,'hoi','cap',16,0,'16/17/18/19/20/24/26/28/30/32'),
(471,5,2,'Nghỉ ngơi',10,'bi_dong','cap',6,0,'6/7/9/12/18/24/26/28/30/32'),
(471,6,2,'Phước lành hồi sinh',0,'hoi_sinh','cap',25,0,'25/26/27/28/29/30/31/32/33/34'),
(471,7,2,'Chia sẻ nỗi đau',13,'buff','cap',25,0,'25/26/27/28/29/30/31/32/33/34'),
(471,8,2,'Tia băng',5,'tan_cong','npc',25,60000,'25/27/29/31/33/35/37/39/41/43'),
(471,9,2,'Bứt phá cuối cùng',0,'tan_cong','npc',25,120000,'25/32/34/36/38/40/42/44/46/48'),
(471,10,2,'Bão tuyết',5,'toi_thuong','npc',25,300000,'25/45/47/49/51/53/55/57/59/61'),
(700,0,2,'Húc',0,'thuong','cap',1,0,'1/2/3/4/5/6/7/8/9/10'),
(700,1,2,'Tấn công nhanh',0,'tan_cong','cap',4,0,'4/7/8/9/10/11/12/13/14/15'),
(700,2,2,'Hôn tiên',17,'tan_cong','cap',7,0,'7/12/13/14/15/16/17/18/19/20'),
(700,3,2,'Sao băng',0,'nhieu_don','cap',16,0,'16/20/22/24/26/30/33/36/39/42'),
(700,4,2,'Mong ước',0,'hoi','cap',16,0,'16/17/18/19/20/24/26/28/30/32'),
(700,5,2,'Nghỉ ngơi',10,'bi_dong','cap',6,0,'6/7/9/12/18/24/26/28/30/32'),
(700,6,2,'Phước lành hồi sinh',0,'hoi_sinh','cap',25,0,'25/26/27/28/29/30/31/32/33/34'),
(700,7,2,'Chia sẻ nỗi đau',13,'buff','cap',25,0,'25/26/27/28/29/30/31/32/33/34'),
(700,8,2,'Khí tiên',17,'tan_cong','npc',25,60000,'25/27/29/31/33/35/37/39/41/43'),
(700,9,2,'Bứt phá cuối cùng',0,'tan_cong','npc',25,120000,'25/32/34/36/38/40/42/44/46/48'),
(700,10,2,'Ánh trăng tiên',17,'toi_thuong','npc',25,300000,'25/45/47/49/51/53/55/57/59/61'),
(7,0,3,'Húc',0,'thuong','cap',1,0,'1/2/3/4/5/6/7/8/9/10'),
(7,1,3,'Súng nước',2,'tan_cong','cap',4,0,'4/7/8/9/10/11/12/13/14/15'),
(7,2,3,'Bong bóng',2,'tan_cong','cap',7,0,'7/12/13/14/15/16/17/18/19/20'),
(7,5,3,'Rút vào mai',2,'khien','cap',6,0,'6/7/9/12/18/24/26/28/30/32'),
(8,0,3,'Húc',0,'thuong','cap',1,0,'1/2/3/4/5/6/7/8/9/10'),
(8,1,3,'Súng nước',2,'tan_cong','cap',4,0,'4/7/8/9/10/11/12/13/14/15'),
(8,2,3,'Bong bóng',2,'tan_cong','cap',7,0,'7/12/13/14/15/16/17/18/19/20'),
(8,3,3,'Xoáy nước',2,'nhieu_don','cap',16,0,'16/20/22/24/26/30/33/36/39/42'),
(8,4,3,'Phá mai',0,'buff','cap',16,0,'16/17/18/19/20/24/26/28/30/32'),
(8,5,3,'Rút vào mai',2,'khien','cap',6,0,'6/7/9/12/18/24/26/28/30/32'),
(9,0,3,'Húc',0,'thuong','cap',1,0,'1/2/3/4/5/6/7/8/9/10'),
(9,1,3,'Súng nước',2,'tan_cong','cap',4,0,'4/7/8/9/10/11/12/13/14/15'),
(9,2,3,'Bong bóng',2,'tan_cong','cap',7,0,'7/12/13/14/15/16/17/18/19/20'),
(9,3,3,'Xoáy nước',2,'nhieu_don','cap',16,0,'16/20/22/24/26/30/33/36/39/42'),
(9,4,3,'Phá mai',0,'buff','cap',16,0,'16/17/18/19/20/24/26/28/30/32'),
(9,5,3,'Rút vào mai',2,'khien','cap',6,0,'6/7/9/12/18/24/26/28/30/32'),
(9,6,3,'Đuôi nước',2,'tan_cong','npc',36,60000,'36/37/38/39/40/41/42/43/44/45'),
(9,7,3,'Bơm nước',2,'tan_cong','npc',36,120000,'36/37/38/39/40/41/42/44/46/48'),
(9,8,3,'Đại pháo nước',2,'toi_thuong','npc',36,300000,'36/45/47/49/51/53/55/57/59/61'),
(172,0,4,'Tia lửa điện',3,'thuong','cap',1,0,'1/2/3/4/5/6/7/8/9/10'),
(172,1,4,'Sóng điện',3,'tan_cong','cap',4,0,'4/7/8/9/10/11/12/13/14/15'),
(172,2,4,'Cầu điện',3,'tan_cong','cap',7,0,'7/12/13/14/15/16/17/18/19/20'),
(172,5,4,'Nạp điện',3,'bi_dong','cap',6,0,'6/7/9/12/18/24/26/28/30/32'),
(25,0,4,'Tia lửa điện',3,'thuong','cap',1,0,'1/2/3/4/5/6/7/8/9/10'),
(25,1,4,'Sóng điện',3,'tan_cong','cap',4,0,'4/7/8/9/10/11/12/13/14/15'),
(25,2,4,'Cầu điện',3,'tan_cong','cap',7,0,'7/12/13/14/15/16/17/18/19/20'),
(25,3,4,'Tia chớp',3,'nhieu_don','cap',10,0,'10/20/22/24/26/30/33/36/39/42'),
(25,4,4,'Nhanh nhẹn',10,'buff','cap',10,0,'10/11/12/13/18/24/26/28/30/32'),
(25,5,4,'Nạp điện',3,'bi_dong','cap',6,0,'6/7/9/12/18/24/26/28/30/32'),
(26,0,4,'Tia lửa điện',3,'thuong','cap',1,0,'1/2/3/4/5/6/7/8/9/10'),
(26,1,4,'Sóng điện',3,'tan_cong','cap',4,0,'4/7/8/9/10/11/12/13/14/15'),
(26,2,4,'Cầu điện',3,'tan_cong','cap',7,0,'7/12/13/14/15/16/17/18/19/20'),
(26,3,4,'Tia chớp',3,'nhieu_don','cap',10,0,'10/20/22/24/26/30/33/36/39/42'),
(26,4,4,'Nhanh nhẹn',10,'buff','cap',10,0,'10/11/12/13/18/24/26/28/30/32'),
(26,5,4,'Nạp điện',3,'bi_dong','cap',6,0,'6/7/9/12/18/24/26/28/30/32'),
(26,6,4,'Lưới điện',3,'khong_che','npc',30,60000,'30/31/32/33/34/35/37/39/41/43'),
(26,7,4,'Sấm sét',3,'tan_cong','npc',30,120000,'30/32/34/36/38/40/42/44/46/48'),
(26,8,4,'Đâm sầm điện',3,'toi_thuong','npc',30,300000,'30/45/47/49/51/53/55/57/59/61')
ON DUPLICATE KEY UPDATE dong=VALUES(dong), ten=VALUES(ten), he=VALUES(he), kieu=VALUES(kieu), nguon=VALUES(nguon),
  cap_hoc=VALUES(cap_hoc), gia_xu=VALUES(gia_xu), cap_cap_chieu=VALUES(cap_cap_chieu);

-- ------------------------------------------------------------------------------------------------- Điểm huấn luyện
-- Dòng chỉ số được phân bổ (có trần mỗi dòng, giảm dần): 'nguong_giam' điểm đầu mỗi điểm +phan_nghin_truoc ‰, các
-- điểm sau +phan_nghin_sau ‰, tối đa tran_diem điểm. Mặc định 40 × 3‰ + 40 × 2‰ = +20% ở 80 điểm (spec 4.2: trung
-- bình +0,25%/điểm, trần +20% một chỉ số). Tổng điểm = huan_luyen.diem_moi_cap × (cấp − 1) + 1 / huy hiệu Gym (trần
-- huan_luyen.tran_diem_thuong) — cấp 100 đủ 16 huy hiệu: 214 điểm, không thể dồn quá 80 vào một dòng.
CREATE TABLE IF NOT EXISTS data_huan_luyen (
  id TINYINT NOT NULL,
  ma VARCHAR(16) NOT NULL,
  ten VARCHAR(32) NOT NULL,
  mo_ta VARCHAR(120) NOT NULL DEFAULT '',
  tran_diem SMALLINT NOT NULL,
  nguong_giam SMALLINT NOT NULL,
  phan_nghin_truoc SMALLINT NOT NULL,
  phan_nghin_sau SMALLINT NOT NULL,
  thu_tu TINYINT NOT NULL DEFAULT 0,
  sua_boi VARCHAR(50) NULL,
  sua_luc DATETIME NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO data_huan_luyen (id, ma, ten, mo_ta, tran_diem, nguong_giam, phan_nghin_truoc, phan_nghin_sau, thu_tu) VALUES
(0,'hp','HP','Tăng % HP tối đa',80,40,3,2,0),
(1,'tan_cong','Tấn công','Tăng % tấn công (cả PvP)',80,40,3,2,1),
(2,'phong_thu','Phòng thủ','Tăng % phòng thủ và phòng thủ đặc biệt',80,40,3,2,2),
(3,'pp','PP','Tăng % PP tối đa',80,40,3,2,3)
ON DUPLICATE KEY UPDATE ma=VALUES(ma), ten=VALUES(ten), mo_ta=VALUES(mo_ta), tran_diem=VALUES(tran_diem),
  nguong_giam=VALUES(nguong_giam), phan_nghin_truoc=VALUES(phan_nghin_truoc), phan_nghin_sau=VALUES(phan_nghin_sau),
  thu_tu=VALUES(thu_tu);

-- Chỉ số gốc tăng theo cấp của từng dòng (thay 5 điểm tiềm năng tự do mỗi cấp): ngoài +1 mọi chỉ số mỗi cấp như cũ,
-- mỗi cấp cộng thêm số điểm dưới đây (tổng 5, May mắn 0 — May mắn không còn cộng được). Nền theo vai trò docs §2.1.
CREATE TABLE IF NOT EXISTS data_tang_truong_dong (
  dong TINYINT NOT NULL,
  ten VARCHAR(24) NOT NULL,
  suc_manh TINYINT NOT NULL,
  kheo_leo TINYINT NOT NULL,
  tinh_than TINYINT NOT NULL,
  suc_khoe TINYINT NOT NULL,
  may_man TINYINT NOT NULL DEFAULT 0,
  ghi_chu VARCHAR(160) NOT NULL DEFAULT '',
  sua_boi VARCHAR(50) NULL,
  sua_luc DATETIME NULL,
  PRIMARY KEY (dong)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO data_tang_truong_dong (dong, ten, suc_manh, kheo_leo, tinh_than, suc_khoe, may_man, ghi_chu) VALUES
(0,'Charmander',4,0,0,1,0,'Sát thương cận chiến: Sức mạnh = tấn công'),
(1,'Bulbasaur',3,1,0,1,0,'Chịu đòn + khống chế'),
(2,'Eevee',0,0,4,1,0,'Tinh thần = tấn công và PP'),
(3,'Squirtle',2,2,0,1,0,'Chịu đòn chính: Khéo léo >= Sức mạnh giữ tấn công đủ'),
(4,'Pichu',0,4,0,1,0,'Đánh xa: Khéo léo = tấn công và phòng thủ')
ON DUPLICATE KEY UPDATE ten=VALUES(ten), suc_manh=VALUES(suc_manh), kheo_leo=VALUES(kheo_leo), tinh_than=VALUES(tinh_than),
  suc_khoe=VALUES(suc_khoe), may_man=VALUES(may_man), ghi_chu=VALUES(ghi_chu);

-- Trạng thái Điểm huấn luyện của từng Pokémon (điểm đã phân bổ theo id dòng; điểm còn lại = tổng − đã phân bổ).
CREATE TABLE IF NOT EXISTS tob_huan_luyen (
  char_id INT NOT NULL,
  phan_bo VARCHAR(48) NOT NULL DEFAULT '',
  lan_tay INT NOT NULL DEFAULT 0,
  tay_luc BIGINT NOT NULL DEFAULT 0,
  tong_xu_tay BIGINT NOT NULL DEFAULT 0,
  cap_nhat DATETIME NULL,
  PRIMARY KEY (char_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dấu chuyển đổi một lần mỗi nhân vật (spec 7): buoc 'huan_luyen' (điểm tiềm năng -> Điểm huấn luyện),
-- 'hoan_chieu_<ô>' (hoàn xu chiêu NPC loài chưa học được), chi_tiet = trước / sau; da_bao = đã hiện hộp thoại.
CREATE TABLE IF NOT EXISTS tob_chuyen_doi_can_bang (
  char_id INT NOT NULL,
  buoc VARCHAR(24) NOT NULL,
  luc DATETIME NOT NULL,
  chi_tiet TEXT NULL,
  xu_hoan BIGINT NOT NULL DEFAULT 0,
  da_hoan TINYINT NOT NULL DEFAULT 0,
  da_bao TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (char_id, buoc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
ALTER TABLE tob_chuyen_doi_can_bang ADD COLUMN IF NOT EXISTS xu_hoan BIGINT NOT NULL DEFAULT 0;
ALTER TABLE tob_chuyen_doi_can_bang ADD COLUMN IF NOT EXISTS da_hoan TINYINT NOT NULL DEFAULT 0;
ALTER TABLE tob_chuyen_doi_can_bang ADD COLUMN IF NOT EXISTS da_bao TINYINT NOT NULL DEFAULT 0;
-- >>> them_bo_ngoc_che_tao_bat.sql
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
-- >>> them_pho_ban_cup.sql
-- ======================================================================================================================
-- PHO BAN HAM NGUC (to doi 5 tang) + CUP PVP TUAN XOAY VONG (0.9.7, de xuat 7 + 10 chu game da duyet 19/10/2026).
-- Chay lai bao nhieu lan cung duoc (MariaDB 10.3, utf8): CREATE TABLE IF NOT EXISTS + INSERT ... ON DUPLICATE KEY
-- UPDATE cot = VALUES(cot). May chu game tu tao cac bang tob_* neu thieu (data.DatabasePhoBanCup); bang data_* thieu
-- thi may chu dung ban mac dinh trong com.htbb.pokemon.PhoBanLogic / CupLogic (cung so lieu voi tep nay).
--
--   (nay la mot phan cua sql/nang_cap.sql; danh muc khoa pho_ban.* / cup.* o phan 2)
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
-- (ban-098) Dong 4381 / 4382 nam o phan 5 (data_map_ten, giu ten admin da sua) + luat khoa ban do o phan 7
-- (data_ban_do_mo_khoa: khong khoa cap, cam Tu dong danh - giong PhongRieng.khongTuDong).

-- ---------------------------------------------------------------------------------------------------- 4. Danh hieu
-- Danh muc danh hieu (he danh hieu 0.9.7, nhanh danh-hieu: cung cau truc bang data_danh_hieu). Kho so huu van la
-- tob_danh_hieu (Char.allDanhHieu) - may chu trao THANG id hieu ung + han (real.PhongRiengGame.traoDanhHieu), khong
-- tu deo. Chi so nho cua o danh hieu (ngan sach o 1%, chi danh hieu dang deo duoc tinh). Dai id 9850..9869 (9800.. thuoc mua-thoi-tiet).
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
(9850,'cup_vo_dich_nhi','Vô địch Cúp Nhí','Vô địch Cúp Nhí của một tuần (Pokémon cấp 20–30). Hiệu lực 28 ngày.','Đứng hạng 1 Cúp Nhí.',2,'vuot','chi_mang',9,'trao','',1,0,40320,'cup',300,1),
(9851,'cup_vo_dich_he','Vua hệ tuần','Vô địch một Cúp hệ (Lửa / Nước / Cỏ / Điện). Hiệu lực 28 ngày.','Đứng hạng 1 một Cúp hệ.',2,'vuot','ne_tranh',9,'trao','',1,0,40320,'cup',301,1),
(9852,'cup_vo_dich_doi','Song đấu vô địch','Vô địch Cúp Đôi 2 đấu 2. Hiệu lực 28 ngày.','Đứng hạng 1 Cúp Đôi cùng đồng đội.',2,'vuot','khang_trang_thai',9,'trao','',1,0,40320,'cup',302,1),
(9853,'cup_vo_dich_thuan','Bậc thầy tay không','Vô địch Cúp Không trang bị. Hiệu lực 28 ngày.','Đứng hạng 1 Cúp Không trang bị.',2,'vuot','hp',9,'trao','',1,0,40320,'cup',303,1),
(9854,'cup_a_quan','Á quân cúp tuần','Hạng 2–3 của một cúp tuần. Hiệu lực 14 ngày.','Vào top 3 cúp tuần.',1,'khien','ne_tranh',7,'trao','',1,0,20160,'cup',304,1),
(9855,'cup_dau_si','Huấn luyện viên cúp tuần','Hạng 4–8, hoặc dự đủ 3 trận và thắng ít nhất 1 trận cúp tuần. Hiệu lực 7 ngày.','Dự cúp tuần đủ 3 trận.',0,'khien','chi_mang',5,'trao','',1,0,10080,'cup',305,1),
(9860,'pho_ban_chinh_phuc','Chinh phục Phó bản','Hạ boss tầng 5 Phó bản Hầm ngục lần đầu.','Hạ boss tầng 5 Phó bản Hầm ngục.',1,'sao','khang_trang_thai',7,'trao','',1,0,0,'thanh_tuu',310,1),
(9861,'pho_ban_10','Thám hiểm Hầm ngục','Hạ boss Phó bản Hầm ngục 10 lần.','Hạ boss phó bản nhiều lần.',1,'la','hp',7,'pho_ban','',10,0,0,'thanh_tuu',311,1),
(9862,'pho_ban_50','Chúa tể Hầm ngục','Hạ boss Phó bản Hầm ngục 50 lần.','Hạ boss phó bản thật nhiều lần.',2,'mat_troi','chi_mang',9,'pho_ban','',50,1,0,'thanh_tuu',312,1)
ON DUPLICATE KEY UPDATE `ma`=VALUES(`ma`), `ten`=VALUES(`ten`), `mo_ta`=VALUES(`mo_ta`), `goi_y`=VALUES(`goi_y`),
  `do_hiem`=VALUES(`do_hiem`), `hinh`=VALUES(`hinh`), `chi_so`=VALUES(`chi_so`), `phan_nghin`=VALUES(`phan_nghin`),
  `dieu_kien`=VALUES(`dieu_kien`), `tham_so`=VALUES(`tham_so`), `so_luong`=VALUES(`so_luong`), `an`=VALUES(`an`),
  `thoi_han_phut`=VALUES(`thoi_han_phut`), `nguon`=VALUES(`nguon`), `thu_tu`=VALUES(`thu_tu`), `bat`=VALUES(`bat`);
-- >>> them_do_hiem.sql
-- DO HIEM CHUNG (de xuat 4, chu game duyet 19/10/2026) - SINH TU tools/cauhinh/do_hiem.json boi
-- tools/cauhinh/sinh_do_hiem.py, KHONG SUA TAY. Idempotent (MariaDB 10.3, utf8): chay lai bao nhieu lan cung duoc.
-- Mau trong game / web doc ban sinh trong ma nguon (cung nguon); bang nay de tra cuu, admin va cong cu ngoai.
--   (khoi nay nam trong sql/nang_cap.sql phan 11)

CREATE TABLE IF NOT EXISTS `data_do_hiem` (
  `id` TINYINT NOT NULL,
  `ma` VARCHAR(16) NOT NULL DEFAULT '',
  `ten` VARCHAR(32) NOT NULL DEFAULT '',
  `mau` VARCHAR(7) NOT NULL DEFAULT '' COMMENT 'chu / vien tren nen sang',
  `nhat` VARCHAR(7) NOT NULL DEFAULT '' COMMENT 'nen nhat cung tong',
  `sang` VARCHAR(7) NOT NULL DEFAULT '' COMMENT 'chu tren nen toi / the gioi',
  `phu_kien` VARCHAR(40) NOT NULL DEFAULT '' COMMENT '5 tong phu kien: vien,toi,goc,sang,diem',
  `vien_dong` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `data_do_hiem` (`id`,`ma`,`ten`,`mau`,`nhat`,`sang`,`phu_kien`,`vien_dong`) VALUES
(0,'thuong','Thường','#5F6B7A','#EEF1F5','#F2F4F7','#3A4250,#9AA2B0,#D8DCE4,#FFFFFF,#8FA8C8',0),
(1,'hiem','Hiếm','#23803A','#E2F5E5','#7EE08A','#123A1C,#2A8A3E,#52C866,#C2F2C8,#FFFFFF',0),
(2,'quy','Quý','#2366C0','#E3EEFC','#8CC8FF','#123060,#2A60B8,#48A0F8,#B8E0FF,#FFFFFF',0),
(3,'su_thi','Sử thi','#7B45D6','#EEE8FC','#CDB8FF','#30104A,#6A2CA0,#A45CE8,#E0C0FF,#FFE070',0),
(4,'huyen_thoai','Huyền thoại','#B95A0E','#FFEEDB','#FFC46B','#4A2006,#B45A10,#F08A28,#FFD8A0,#FFFFFF',1),
(5,'trang_tri','Trang trí','#8A6A00','#FFF5D0','#FFE070','#4A3208,#C89018,#FFD040,#FFF8C8,#FFFFFF',0)
ON DUPLICATE KEY UPDATE `ma`=VALUES(`ma`), `ten`=VALUES(`ten`), `mau`=VALUES(`mau`), `nhat`=VALUES(`nhat`), `sang`=VALUES(`sang`), `phu_kien`=VALUES(`phu_kien`), `vien_dong`=VALUES(`vien_dong`);

-- Item.colorName (data_item.colortype) -> bac; ma khong co dong = Thuong.
CREATE TABLE IF NOT EXISTS `data_do_hiem_vat_pham` (
  `color_name` TINYINT NOT NULL,
  `bac` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`color_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `data_do_hiem_vat_pham` (`color_name`,`bac`) VALUES
(0,0),(1,3),(2,2),(3,1),(4,3),(5,5),(6,4),(7,1)
ON DUPLICATE KEY UPDATE `bac`=VALUES(`bac`);

-- 11 bac trang bi theo cap (vong sang / vien Pokemon).
CREATE TABLE IF NOT EXISTS `data_bac_trang_bi` (
  `bac` TINYINT NOT NULL,
  `ten` VARCHAR(32) NOT NULL DEFAULT '',
  `mau` VARCHAR(7) NOT NULL DEFAULT '',
  PRIMARY KEY (`bac`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `data_bac_trang_bi` (`bac`,`ten`,`mau`) VALUES
(0,'Sơ cấp','#9AA0A8'),
(1,'Đồng','#C88048'),
(2,'Bạc','#D0D8E0'),
(3,'Vàng','#F8D030'),
(4,'Bạch kim','#E4F4FF'),
(5,'Kim cương','#78D8F8'),
(6,'Chuyên gia','#52C866'),
(7,'Tinh anh','#A45CE8'),
(8,'Huyền thoại','#F08A28'),
(9,'Thần thoại','#F84868'),
(10,'Tối thượng','#FFFFFF')
ON DUPLICATE KEY UPDATE `ten`=VALUES(`ten`), `mau`=VALUES(`mau`);

-- Mau danh hieu (them_danh_hieu.sql tao bang; cot mau chi cho web) = mau bac chung.
CREATE TABLE IF NOT EXISTS `data_danh_hieu_do_hiem` (
  `id` TINYINT NOT NULL,
  `ten` VARCHAR(32) NOT NULL DEFAULT '',
  `mau` VARCHAR(7) NOT NULL DEFAULT '',
  `thu_tu` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `data_danh_hieu_do_hiem` (`id`,`ten`,`mau`,`thu_tu`) VALUES
(0,'Thường','#5F6B7A',0),(1,'Hiếm','#23803A',1),(2,'Sử thi','#7B45D6',2),(3,'Huyền thoại','#B95A0E',3)
ON DUPLICATE KEY UPDATE `ten`=VALUES(`ten`), `mau`=VALUES(`mau`), `thu_tu`=VALUES(`thu_tu`);
-- >>> them_thoi_tiet_mua.sql
-- =====================================================================================================
-- THỜI TIẾT THEO KHUNG GIỜ, PHỦ MÀU MÙA, NGÀY CỘNG ĐỒNG HẰNG THÁNG (đề xuất 8, chủ game duyệt 19/10/2026).
-- Nâng cấp IDEMPOTENT (MariaDB 10.3, utf8): chạy lại bao nhiêu lần cũng được; tự chứa, nối được vào sql/nang_cap.sql.
--   (nay la mot phan cua sql/nang_cap.sql phan 11)
--
--   data_thoi_tiet           7 thời tiết: hệ được +% / -% sát thương chiêu, hệ sinh nhiều, gợi ý HUD. Máy chủ KẸP
--                            |%| <= 10 (com.htbb.pokemon.ThoiTietLich.TRAN_SAT_THUONG_PHAN_TRAM) dù bảng ghi gì.
--   data_thoi_tiet_map       bản đồ cố định một thời tiết (thoi_tiet_co_dinh >= 0) / tắt phủ màu mùa (tat_mau_mua = 1).
--                            Rỗng = mọi bản đồ theo vùng sinh thái (data_hst_vung).
--   data_thoi_tiet_su_kien   sự kiện đang bật (tob_su_kien.bat theo lịch năm) thêm trọng số một thời tiết (<= 60) và
--                            đặt màu phủ (uu_tien cao nhất thắng).
--   data_ngay_cong_dong      dòng Pokémon nổi bật mỗi tháng (nam_xoay 0 = năm chẵn kể từ 2026, 1 = năm lẻ): id quái
--                            của dòng (mỗi loài trên bản đồ tự nhiên của nó, data_hst_bung_phat), giờ vàng, % hồi sinh
--                            (<= 50), % EXP khi hạ (<= 25), số hạ để mở danh hiệu, danh hiệu.
--   data_danh_hieu 9801..9817 danh hiệu dòng (điều kiện cong_dong / tap:cong_dong, đếm trong giờ vàng).
-- Ngày cộng đồng bật / tắt theo lịch năm (moi:congdong, com.htbb.pokemon.NgayCongDong): một Chủ nhật mỗi tháng
-- 04:00 -> 04:00, web tự đăng tin. Không xu, không lượng: chỉ thời gian + hình + danh hiệu (ngân sách ô 1%).
-- =====================================================================================================

CREATE TABLE IF NOT EXISTS `data_thoi_tiet` (
  `id` TINYINT NOT NULL,
  `ma` VARCHAR(16) NOT NULL DEFAULT '',
  `ten` VARCHAR(32) NOT NULL DEFAULT '',
  `he_tang` VARCHAR(40) NOT NULL DEFAULT '' COMMENT 'hệ chiêu +phan_tram (HePokemon, phẩy)',
  `he_giam` VARCHAR(40) NOT NULL DEFAULT '' COMMENT 'hệ chiêu -phan_tram',
  `he_sinh` VARCHAR(40) NOT NULL DEFAULT '' COMMENT 'hệ sinh nhiều (x3)',
  `phan_tram` TINYINT NOT NULL DEFAULT 10 COMMENT 'kẹp <= 10',
  `goi_y` VARCHAR(160) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `data_thoi_tiet` (`id`,`ma`,`ten`,`he_tang`,`he_giam`,`he_sinh`,`phan_tram`,`goi_y`) VALUES
(0,'quang','Quang đãng','','','',0,'Trời đẹp, không ảnh hưởng chiêu.'),
(1,'nang','Nắng gắt','1','2','1,4',10,'Pokémon hệ Lửa, Cỏ xuất hiện nhiều.'),
(2,'mua','Mưa','2','1','2,3',10,'Pokémon hệ Nước, Điện xuất hiện nhiều.'),
(3,'tuyet','Tuyết','5','','5',10,'Pokémon hệ Băng xuất hiện nhiều.'),
(4,'bao_cat','Bão cát','12','','12,8,16',10,'Pokémon hệ Đá, Đất, Thép xuất hiện nhiều.'),
(5,'suong_mu','Sương mù','13','','13,10',10,'Pokémon hệ Ma, Siêu linh xuất hiện nhiều.'),
(6,'gio','Gió mạnh','9','','9,14',10,'Pokémon hệ Bay, Rồng xuất hiện nhiều.')
ON DUPLICATE KEY UPDATE `ma`=VALUES(`ma`), `ten`=VALUES(`ten`), `he_tang`=VALUES(`he_tang`), `he_giam`=VALUES(`he_giam`),
  `he_sinh`=VALUES(`he_sinh`), `phan_tram`=VALUES(`phan_tram`), `goi_y`=VALUES(`goi_y`);

CREATE TABLE IF NOT EXISTS `data_thoi_tiet_map` (
  `map_id` INT NOT NULL,
  `thoi_tiet_co_dinh` TINYINT NOT NULL DEFAULT -1 COMMENT '-1 = theo vùng',
  `tat_mau_mua` TINYINT NOT NULL DEFAULT 0,
  `ghi_chu` VARCHAR(120) NOT NULL DEFAULT '',
  PRIMARY KEY (`map_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `data_thoi_tiet_su_kien` (
  `ma_su_kien` VARCHAR(40) NOT NULL,
  `vung` TINYINT NOT NULL DEFAULT -1 COMMENT '-1 = mọi vùng ngoài trời',
  `thoi_tiet` TINYINT NOT NULL,
  `trong_so` TINYINT NOT NULL DEFAULT 0 COMMENT 'kẹp <= 60',
  `mau_mua` TINYINT NOT NULL DEFAULT 0 COMMENT 'ThoiTietLich.MAU_*: 5 Halloween, 6 Tết, 7 Noel, 8 Trung Thu',
  `uu_tien` INT NOT NULL DEFAULT 0,
  `bat` TINYINT NOT NULL DEFAULT 1,
  PRIMARY KEY (`ma_su_kien`, `vung`, `thoi_tiet`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `data_thoi_tiet_su_kien` (`ma_su_kien`,`vung`,`thoi_tiet`,`trong_so`,`mau_mua`,`uu_tien`) VALUES
('halloween',-1,5,30,5,50),
('noel',-1,3,25,7,50),
('tet2017',-1,0,20,6,60),
('trungthu',-1,0,20,8,40),
('he2017',-1,1,15,0,10)
ON DUPLICATE KEY UPDATE `trong_so`=VALUES(`trong_so`), `mau_mua`=VALUES(`mau_mua`), `uu_tien`=VALUES(`uu_tien`);

CREATE TABLE IF NOT EXISTS `data_ngay_cong_dong` (
  `thang` TINYINT NOT NULL,
  `nam_xoay` TINYINT NOT NULL DEFAULT 0 COMMENT '0 năm chẵn (2026, 2028...), 1 năm lẻ',
  `dong` TINYINT NOT NULL COMMENT 'số dòng (bộ đếm danh hiệu)',
  `ten` VARCHAR(32) NOT NULL DEFAULT '',
  `id_quai` VARCHAR(60) NOT NULL DEFAULT '' COMMENT 'id template các loài của dòng (phẩy)',
  `map_ids` VARCHAR(120) NOT NULL DEFAULT '' COMMENT 'bản đồ thêm (rỗng = chỉ bản đồ tự nhiên)',
  `gio_bat_dau` TINYINT NOT NULL DEFAULT 14,
  `so_gio` TINYINT NOT NULL DEFAULT 3 COMMENT 'kẹp <= 4',
  `ti_le` TINYINT NOT NULL DEFAULT 40 COMMENT '% lần hồi sinh, kẹp <= 50',
  `exp_phan_tram` TINYINT NOT NULL DEFAULT 20 COMMENT 'kẹp <= 25, chung trần +100%',
  `so_ha` INT NOT NULL DEFAULT 30,
  `id_danh_hieu` INT NOT NULL DEFAULT 0,
  `chieu` VARCHAR(60) NOT NULL DEFAULT '' COMMENT 'chiêu nổi bật (chờ learnset hoc-chieu)',
  `bat` TINYINT NOT NULL DEFAULT 1,
  PRIMARY KEY (`thang`, `nam_xoay`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `data_ngay_cong_dong` (`thang`,`nam_xoay`,`dong`,`ten`,`id_quai`,`map_ids`,`gio_bat_dau`,`so_gio`,`ti_le`,`exp_phan_tram`,`so_ha`,`id_danh_hieu`,`chieu`) VALUES
(1,0,1,'Torchic','4,14,44','',14,3,40,20,30,9801,''),
(2,0,11,'Caterpie','2,24','',14,3,40,20,30,9811,''),
(3,0,2,'Oddish','10,20,58','',14,3,40,20,30,9802,''),
(4,0,8,'Paras','19,32','',14,3,40,20,30,9808,''),
(5,0,12,'Wooper','3,13','',14,3,40,20,30,9812,''),
(6,0,7,'Growlithe','28,48','',14,3,40,20,30,9807,''),
(7,0,13,'Luxio','62,137','',14,3,40,20,30,9813,''),
(8,0,5,'Ekans','5,23','',14,3,40,20,30,9805,''),
(9,0,14,'Swablu','143,164','',14,3,40,20,30,9814,''),
(10,0,4,'Gastly','25,52','',14,3,40,20,30,9804,''),
(11,0,3,'Venipede','21,40,42,41','',14,3,40,20,30,9803,''),
(12,0,10,'Aron','146,156,169','',14,3,40,20,30,9810,''),
(1,1,2,'Oddish','10,20,58','',14,3,40,20,30,9802,''),
(2,1,1,'Torchic','4,14,44','',14,3,40,20,30,9801,''),
(3,1,8,'Paras','19,32','',14,3,40,20,30,9808,''),
(4,1,11,'Caterpie','2,24','',14,3,40,20,30,9811,''),
(5,1,7,'Growlithe','28,48','',14,3,40,20,30,9807,''),
(6,1,12,'Wooper','3,13','',14,3,40,20,30,9812,''),
(7,1,5,'Ekans','5,23','',14,3,40,20,30,9805,''),
(8,1,13,'Luxio','62,137','',14,3,40,20,30,9813,''),
(9,1,16,'Baltoy','148,163','',14,3,40,20,30,9816,''),
(10,1,9,'Duskull','55,57','',14,3,40,20,30,9809,''),
(11,1,6,'Cubone','27,59','',14,3,40,20,30,9806,''),
(12,1,15,'Bagon','157,167','',14,3,40,20,30,9815,'')
ON DUPLICATE KEY UPDATE `dong`=VALUES(`dong`), `ten`=VALUES(`ten`), `id_quai`=VALUES(`id_quai`), `id_danh_hieu`=VALUES(`id_danh_hieu`);

-- Danh hiệu dòng ngày cộng đồng (Thường, +0,5% HP - một danh hiệu đeo, ngân sách ô 1%) + "Trụ cột cộng đồng" (6 dòng).
-- Bảng do sql/them_danh_hieu.sql tạo; ON DUPLICATE không ghi đè chi_so / phan_nghin / bat (chủ game chỉnh).
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
(9801,'cong_dong_1','Bạn đồng hành Torchic','Hạ 30 Pokémon dòng Torchic trong giờ vàng ngày cộng đồng.','',0,'tim','hp',5,'cong_dong','1',30,0,0,'su_kien',301,1),
(9802,'cong_dong_2','Bạn đồng hành Oddish','Hạ 30 Pokémon dòng Oddish trong giờ vàng ngày cộng đồng.','',0,'tim','hp',5,'cong_dong','2',30,0,0,'su_kien',302,1),
(9803,'cong_dong_3','Bạn đồng hành Venipede','Hạ 30 Pokémon dòng Venipede trong giờ vàng ngày cộng đồng.','',0,'tim','hp',5,'cong_dong','3',30,0,0,'su_kien',303,1),
(9804,'cong_dong_4','Bạn đồng hành Gastly','Hạ 30 Pokémon dòng Gastly trong giờ vàng ngày cộng đồng.','',0,'tim','hp',5,'cong_dong','4',30,0,0,'su_kien',304,1),
(9805,'cong_dong_5','Bạn đồng hành Ekans','Hạ 30 Pokémon dòng Ekans trong giờ vàng ngày cộng đồng.','',0,'tim','hp',5,'cong_dong','5',30,0,0,'su_kien',305,1),
(9806,'cong_dong_6','Bạn đồng hành Cubone','Hạ 30 Pokémon dòng Cubone trong giờ vàng ngày cộng đồng.','',0,'tim','hp',5,'cong_dong','6',30,0,0,'su_kien',306,1),
(9807,'cong_dong_7','Bạn đồng hành Growlithe','Hạ 30 Pokémon dòng Growlithe trong giờ vàng ngày cộng đồng.','',0,'tim','hp',5,'cong_dong','7',30,0,0,'su_kien',307,1),
(9808,'cong_dong_8','Bạn đồng hành Paras','Hạ 30 Pokémon dòng Paras trong giờ vàng ngày cộng đồng.','',0,'tim','hp',5,'cong_dong','8',30,0,0,'su_kien',308,1),
(9809,'cong_dong_9','Bạn đồng hành Duskull','Hạ 30 Pokémon dòng Duskull trong giờ vàng ngày cộng đồng.','',0,'tim','hp',5,'cong_dong','9',30,0,0,'su_kien',309,1),
(9810,'cong_dong_10','Bạn đồng hành Aron','Hạ 30 Pokémon dòng Aron trong giờ vàng ngày cộng đồng.','',0,'tim','hp',5,'cong_dong','10',30,0,0,'su_kien',310,1),
(9811,'cong_dong_11','Bạn đồng hành Caterpie','Hạ 30 Pokémon dòng Caterpie trong giờ vàng ngày cộng đồng.','',0,'tim','hp',5,'cong_dong','11',30,0,0,'su_kien',311,1),
(9812,'cong_dong_12','Bạn đồng hành Wooper','Hạ 30 Pokémon dòng Wooper trong giờ vàng ngày cộng đồng.','',0,'tim','hp',5,'cong_dong','12',30,0,0,'su_kien',312,1),
(9813,'cong_dong_13','Bạn đồng hành Luxio','Hạ 30 Pokémon dòng Luxio trong giờ vàng ngày cộng đồng.','',0,'tim','hp',5,'cong_dong','13',30,0,0,'su_kien',313,1),
(9814,'cong_dong_14','Bạn đồng hành Swablu','Hạ 30 Pokémon dòng Swablu trong giờ vàng ngày cộng đồng.','',0,'tim','hp',5,'cong_dong','14',30,0,0,'su_kien',314,1),
(9815,'cong_dong_15','Bạn đồng hành Bagon','Hạ 30 Pokémon dòng Bagon trong giờ vàng ngày cộng đồng.','',0,'tim','hp',5,'cong_dong','15',30,0,0,'su_kien',315,1),
(9816,'cong_dong_16','Bạn đồng hành Baltoy','Hạ 30 Pokémon dòng Baltoy trong giờ vàng ngày cộng đồng.','',0,'tim','hp',5,'cong_dong','16',30,0,0,'su_kien',316,1),
(9817,'cong_dong_6_dong','Trụ cột cộng đồng','Tham gia giờ vàng ngày cộng đồng của 6 dòng khác nhau.','',1,'sao','ne_tranh',7,'tap:cong_dong','',6,0,0,'su_kien',317,1)
ON DUPLICATE KEY UPDATE `ma`=VALUES(`ma`), `ten`=VALUES(`ten`), `mo_ta`=VALUES(`mo_ta`), `goi_y`=VALUES(`goi_y`), `do_hiem`=VALUES(`do_hiem`), `hinh`=VALUES(`hinh`), `dieu_kien`=VALUES(`dieu_kien`), `tham_so`=VALUES(`tham_so`), `so_luong`=VALUES(`so_luong`), `an`=VALUES(`an`), `thoi_han_phut`=VALUES(`thoi_han_phut`), `nguon`=VALUES(`nguon`), `thu_tu`=VALUES(`thu_tu`);
-- >>> them_ve_mo_rong.sql
-- ==================================================================================
--  VE MO RONG HANH TRANG  (tob_char.o_mo_rong + mon cua hang luong ve_mo_rong_tui)
-- ==================================================================================
--
--  Them 19/10/2026. Chay lai bao nhieu lan cung duoc (MariaDB 10.3 / MySQL 5.x, utf8):
--
--      (nay la mot phan cua sql/nang_cap.sql phan 11)
--
--  KHONG BAT BUOC CHAY phan cot: may chu TU THEM cot o_mo_rong luc khoi dong neu chua co
--  (Database.themCotVeMoRong() trong server/TeamServer.java). Cot nay luu bang cau UPDATE
--  rieng (Database.luuCotMoRong) nen thieu cot KHONG khoa nhan vat, chi khong giu duoc o da mo.
--
--  Suc chua hanh trang = 42 x so trang (maxBag, mua bang luong nhu cu) + o_mo_rong.
--  o_mo_rong tang +10 moi lan dung "Ve mo rong hanh trang" (nguyen lieu id 270, hien trong
--  tab nguyen lieu, bam "Dung") hoac mua thang bang xu o Kangaskhan thu kho (gia tang dan:
--  20.000 / 30.000 / 45.000 / 68.000 / 100.000 / 150.000 / 230.000 / 340.000 / 510.000 xu).
--  Tran: 126 o (= 3 trang khoi dau). Hang so: server/src/com/htbb/pokemon/MoRongHanhTrang.java.
--
--  Mau nguyen lieu (Vé mở rộng hành trang, id 270) nam trong ma server
--  (real/MapNapTaiNguyen.loadGemTemplate) - khong co bang du lieu, file nay khong can them.
-- ==================================================================================


-- ---------------------------------------------------------------- 1. cot tob_char.o_mo_rong (idempotent)
ALTER TABLE `tob_char` ADD COLUMN IF NOT EXISTS `o_mo_rong` INT NOT NULL DEFAULT 0 COMMENT 'O hanh trang le: ve mo rong / mua xu (real.MoRongTuiGame)';

-- ---------------------------------------------------------------- 2. cua hang luong (he nap tien)
-- trao = mo_rong_tui (danh sach trang cu); server phan biet theo ma ve_mo_rong_tui -> trao 1 ve vao tui.
INSERT IGNORE INTO `nap_cua_hang` (`ma`,`ten`,`nhom`,`trao`,`gia_luong`,`han_ngay`,`gioi_han`,`nhom_xoay`,`giao_dich_duoc`,`co_chi_so`,`dang_ban`,`thu_tu`) VALUES
('ve_mo_rong_tui','Vé mở rộng hành trang (+10 ô)','tien_ich','mo_rong_tui',80,0,9,0,0,0,1,23);
-- >>> them_trade_pokemon.sql
-- =====================================================================================================================
-- HTBB 0.9.8 - THU PHỤC (bậc hiếm) + CÁ THỂ + CHUYỂN ĐỔI + TRAO ĐỔI POKÉMON (docs/thiet-ke-game.md 7.7, 7.8)
--   Chạy SAU sql/them_bo_ngoc_che_tao_bat.sql. Idempotent (chạy lại bao nhiêu lần cũng được), MariaDB 10.3, utf8:
--      (nay la mot phan cua sql/nang_cap.sql phan 11, sau khoi them_bo_ngoc_che_tao_bat.sql)
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

-- >>> them_kenh_chat.sql
-- =====================================================================================================
-- KÊNH CHAT (0.9.8): bảng chat nhiều kênh (Bản đồ, Thế giới, Bang hội, Đội, Bạn bè / tin riêng, Hệ thống).
-- Nâng cấp IDEMPOTENT (MariaDB 10.3, utf8): chạy lại bao nhiêu lần cũng được; tự chứa. Máy chủ game cũng tự tạo hai
-- bảng này lúc khởi động (real.KenhChatGame.taoBang) - chạy tệp này chỉ để dựng sẵn trên CSDL mới / web đọc được.
--
--   tob_chat_cho    hộp thư tin riêng OFFLINE: tin gửi cho người đang offline, giao một lần khi họ vào game rồi xoá
--                   (trước 0.9.8 tin riêng cho người offline bị MẤT). id_nhan = tob_char.id (đổi tên không mất tin);
--                   luc = mili giây (System.currentTimeMillis). Tối đa sv.chat.thuChoToiDa tin / người, giữ
--                   sv.chat.thuChoNgay ngày (quá hạn xoá lúc khởi động).
--   tob_chat_chan   danh sách chặn: id_char chặn tên ten_chan (chữ thường). Người bị chặn không nhắn riêng được, tin
--                   thế giới / bang hội / đội của họ không tới người chặn.
--
-- Khoá cấu hình sv.chat.* (lớp co_dinh, không tăng số khoá trên web) nằm ở phần 2 của sql/nang_cap.sql.
-- =====================================================================================================

CREATE TABLE IF NOT EXISTS `tob_chat_cho` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `id_nhan` INT NOT NULL,
  `ten_gui` VARCHAR(45) NOT NULL,
  `noi_dung` VARCHAR(400) NOT NULL,
  `luc` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_chat_cho_nhan` (`id_nhan`)
) DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `tob_chat_chan` (
  `id_char` INT NOT NULL,
  `ten_chan` VARCHAR(45) NOT NULL,
  `luc` DATETIME NOT NULL,
  PRIMARY KEY (`id_char`, `ten_chan`)
) DEFAULT CHARSET=utf8;
-- >>> them_ngam_tay_pvp.sql
-- =====================================================================================================
-- NGẮM TAY TRONG PvP (0.9.8): thông số bắn theo hướng của từng chiêu. IDEMPOTENT (MariaDB 10.3, utf8), tự chứa.
--
--   data_chieu_ban   (lop, chieu) -> kieu: dan (đạn bay thẳng, trúng người đầu tiên) / xuyen (tia xuyên, trúng mọi
--                    người trên đường tới toi_da) / non (hình nón góc `goc` độ) / vung (vùng tròn đường kính be_rong ở
--                    cuối tầm) / tu_than (buff, tức thì - không ngắm);
--                    tam (px, 0 = tầm gốc CharManager.SKILL_RANGE của chiêu; lớn hơn tầm gốc cũng bị kẹp về tầm gốc),
--                    be_rong (px; đạn / tia = bề rộng cả đường, cộng thêm bán kính thân 10 px khi xét trúng),
--                    toc_do (px/giây; 0 = tức thì), goc (độ, chỉ nón), toi_da (số mục tiêu tối đa; đạn luôn 1).
-- Máy chủ (real.BanHuongGame.nap) tự tạo bảng nếu thiếu; chiêu không có dòng thì dùng đúng mặc định dưới đây
-- (com.htbb.pokemon.BanHuong.macDinh). INSERT IGNORE: chạy lại không ghi đè dòng đã chỉnh.
-- Khoá cấu hình: pvp.ngam_tay (bật/tắt), pvp.ngam_tay.giay_giao_chien, pvp.ngam_tay.bu_tre_ms, pvp.ngam_tay.map.
-- =====================================================================================================

CREATE TABLE IF NOT EXISTS `data_chieu_ban` (
  `lop` TINYINT NOT NULL,
  `chieu` TINYINT NOT NULL,
  `kieu` VARCHAR(10) NOT NULL DEFAULT 'dan',
  `tam` SMALLINT NOT NULL DEFAULT 0,
  `be_rong` SMALLINT NOT NULL DEFAULT 20,
  `toc_do` SMALLINT NOT NULL DEFAULT 0,
  `goc` SMALLINT NOT NULL DEFAULT 60,
  `toi_da` TINYINT NOT NULL DEFAULT 1,
  `ghi_chu` VARCHAR(120) NOT NULL DEFAULT '',
  PRIMARY KEY (`lop`, `chieu`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT IGNORE INTO `data_chieu_ban` (`lop`, `chieu`, `kieu`, `tam`, `be_rong`, `toc_do`, `goc`, `toi_da`, `ghi_chu`) VALUES
  (0, 0, 'non', 0, 24, 0, 100, 2, 'đánh thường cận chiến: nón 100°'),
  (0, 1, 'dan', 0, 20, 480, 30, 1, 'đạn bay thẳng, trúng người đầu tiên'),
  (0, 2, 'xuyen', 0, 24, 0, 30, 3, 'tia xuyên tức thì, tối đa 3'),
  (0, 3, 'non', 0, 32, 0, 70, 4, 'nón 70°, tối đa 4'),
  (0, 4, 'tu_than', 0, 32, 0, 360, 1, 'buff / bản thân: tức thì (lệnh 51)'),
  (0, 5, 'tu_than', 0, 32, 0, 360, 1, 'buff / bản thân: tức thì (lệnh 51)'),
  (0, 6, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5'),
  (0, 7, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5'),
  (0, 8, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5'),
  (0, 9, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5'),
  (1, 0, 'non', 0, 24, 0, 100, 2, 'đánh thường cận chiến: nón 100°'),
  (1, 1, 'dan', 0, 20, 480, 30, 1, 'đạn bay thẳng, trúng người đầu tiên'),
  (1, 2, 'xuyen', 0, 24, 0, 30, 3, 'tia xuyên tức thì, tối đa 3'),
  (1, 3, 'non', 0, 32, 0, 70, 4, 'nón 70°, tối đa 4'),
  (1, 4, 'tu_than', 0, 32, 0, 360, 1, 'buff / bản thân: tức thì (lệnh 51)'),
  (1, 5, 'tu_than', 0, 32, 0, 360, 1, 'buff / bản thân: tức thì (lệnh 51)'),
  (1, 6, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5'),
  (1, 7, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5'),
  (1, 8, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5'),
  (1, 9, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5'),
  (2, 0, 'dan', 0, 16, 420, 30, 1, 'đánh thường: đạn bay thẳng'),
  (2, 1, 'dan', 0, 20, 480, 30, 1, 'đạn bay thẳng, trúng người đầu tiên'),
  (2, 2, 'xuyen', 0, 24, 0, 30, 3, 'tia xuyên tức thì, tối đa 3'),
  (2, 3, 'non', 0, 32, 0, 70, 4, 'nón 70°, tối đa 4'),
  (2, 4, 'tu_than', 0, 32, 0, 360, 1, 'buff / bản thân: tức thì (lệnh 51)'),
  (2, 5, 'tu_than', 0, 32, 0, 360, 1, 'buff / bản thân: tức thì (lệnh 51)'),
  (2, 6, 'tu_than', 0, 32, 0, 360, 1, 'buff / bản thân: tức thì (lệnh 51)'),
  (2, 7, 'tu_than', 0, 32, 0, 360, 1, 'buff / bản thân: tức thì (lệnh 51)'),
  (2, 8, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5'),
  (2, 9, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5'),
  (3, 0, 'non', 0, 24, 0, 100, 2, 'đánh thường cận chiến: nón 100°'),
  (3, 1, 'dan', 0, 20, 480, 30, 1, 'đạn bay thẳng, trúng người đầu tiên'),
  (3, 2, 'xuyen', 0, 24, 0, 30, 3, 'tia xuyên tức thì, tối đa 3'),
  (3, 3, 'non', 0, 32, 0, 70, 4, 'nón 70°, tối đa 4'),
  (3, 4, 'tu_than', 0, 32, 0, 360, 1, 'buff / bản thân: tức thì (lệnh 51)'),
  (3, 5, 'tu_than', 0, 32, 0, 360, 1, 'buff / bản thân: tức thì (lệnh 51)'),
  (3, 6, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5'),
  (3, 7, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5'),
  (3, 8, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5'),
  (3, 9, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5'),
  (4, 0, 'dan', 0, 16, 420, 30, 1, 'đánh thường: đạn bay thẳng'),
  (4, 1, 'dan', 0, 20, 480, 30, 1, 'đạn bay thẳng, trúng người đầu tiên'),
  (4, 2, 'xuyen', 0, 24, 0, 30, 3, 'tia xuyên tức thì, tối đa 3'),
  (4, 3, 'non', 0, 32, 0, 70, 4, 'nón 70°, tối đa 4'),
  (4, 4, 'tu_than', 0, 32, 0, 360, 1, 'buff / bản thân: tức thì (lệnh 51)'),
  (4, 5, 'tu_than', 0, 32, 0, 360, 1, 'buff / bản thân: tức thì (lệnh 51)'),
  (4, 6, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5'),
  (4, 7, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5'),
  (4, 8, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5'),
  (4, 9, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5');
-- >>> sua_ket_doi.sql
-- =====================================================================================================
-- Lễ kết đôi (thay hệ thống đám cưới cũ) + nhóm trứng (Egg Group) theo Pokémon gốc.
--
-- Chạy lại được (MariaDB 10.3, utf8): bảng tạo IF NOT EXISTS; dữ liệu nhóm trứng INSERT IGNORE (không ghi đè dòng
-- admin đã sửa); đổi tên chỉ đổi ô còn giữ tên CŨ (ô đã sửa tay giữ nguyên).
--
--   data_nhom_trung_ten  15 nhóm trứng (id theo thứ tự trong game gốc), tên hiện, ket_doi_duoc = 0 cho nhóm
--                        "Chưa khám phá" (Pokémon bé như Pichu: không kết đôi với ai, kể cả cùng nhóm).
--   data_nhom_trung      nhóm trứng của MỌI loài Pokémon người chơi điều khiển (5 dòng khởi đầu + tiến hoá +
--                        8 nhánh Eevee = 21 loài, com.htbb.pokemon.LoaiPokemon). Không có luật Ditto.
--
-- Điều kiện kết đôi (server real.KetDoiGame / com.htbb.pokemon.KetDoi): hai loài HIỆN TẠI chung ít nhất một nhóm
-- trứng, xét LÚC ĐĂNG KÝ Lễ kết đôi; cặp đã kết đôi từ trước giữ nguyên (tiến hoá không xét lại).
-- Server nạp hai bảng lúc khởi động / lần đăng ký đầu; thiếu bảng thì từ chối đăng ký và báo trong log.
--
-- Đổi tên hiển thị: Thiệp cưới -> Thiệp mời kết đôi (92), Nhẫn Trái tim -> Nhẫn kết đôi (88..90), vật phẩm dịch
-- chuyển tới bạn kết đôi Vảy trái tim -> Vảy trái tim kết đôi (thuốc 98, data_item 751), bản đồ 225 / 226
-- Lễ cưới 1 / 2 -> Lễ kết đôi 1 / 2. Test: server/test/KiemKetDoi.java đọc khối này của sql/nang_cap.sql.
-- =====================================================================================================


CREATE TABLE IF NOT EXISTS `data_nhom_trung_ten` (
  `id` TINYINT UNSIGNED NOT NULL,
  `ma` VARCHAR(20) NOT NULL,
  `ten` VARCHAR(50) NOT NULL,
  `ket_doi_duoc` TINYINT NOT NULL DEFAULT 1,
  `sua_luc` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `data_nhom_trung` (
  `dex` SMALLINT UNSIGNED NOT NULL,
  `ten_loai` VARCHAR(30) NOT NULL,
  `nhom1` TINYINT UNSIGNED NOT NULL,
  `nhom2` TINYINT UNSIGNED DEFAULT NULL,
  `sua_luc` DATETIME DEFAULT NULL,
  PRIMARY KEY (`dex`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT IGNORE INTO `data_nhom_trung_ten` (`id`, `ma`, `ten`, `ket_doi_duoc`) VALUES
(1,'monster','Quái vật',1),
(2,'water1','Nước 1',1),
(3,'bug','Côn trùng',1),
(4,'flying','Bay',1),
(5,'field','Đồng cỏ',1),
(6,'fairy','Tiên',1),
(7,'grass','Thực vật',1),
(8,'humanlike','Dạng người',1),
(9,'water3','Nước 3',1),
(10,'mineral','Khoáng vật',1),
(11,'amorphous','Vô định hình',1),
(12,'water2','Nước 2',1),
(13,'ditto','Ditto',1),
(14,'dragon','Rồng',1),
(15,'undiscovered','Chưa khám phá',0);

INSERT IGNORE INTO `data_nhom_trung` (`dex`, `ten_loai`, `nhom1`, `nhom2`) VALUES
(4,'Charmander',1,14),
(5,'Charmeleon',1,14),
(6,'Charizard',1,14),
(1,'Bulbasaur',1,7),
(2,'Ivysaur',1,7),
(3,'Venusaur',1,7),
(7,'Squirtle',1,2),
(8,'Wartortle',1,2),
(9,'Blastoise',1,2),
(172,'Pichu',15,NULL),
(25,'Pikachu',5,6),
(26,'Raichu',5,6),
(133,'Eevee',5,NULL),
(134,'Vaporeon',5,NULL),
(135,'Jolteon',5,NULL),
(136,'Flareon',5,NULL),
(196,'Espeon',5,NULL),
(197,'Umbreon',5,NULL),
(470,'Leafeon',5,NULL),
(471,'Glaceon',5,NULL),
(700,'Sylveon',5,NULL);

-- Tên vật phẩm (chỉ đổi ô còn tên cũ: tên kiếm hiệp gốc hoặc tên sau pokemon_doi_chu.sql trước đợt này).
UPDATE `data_potion` SET `name`='Nhẫn kết đôi đồng 2%pt', `name2`='nhanketdoidongpt'
  WHERE `id`=88 AND `name` IN ('Nhẫn Trái tim đồng 2%pt','Nhẫn Bách niên giai lão 2%pt');
UPDATE `data_potion` SET `name`='Nhẫn kết đôi bạc 4%pt', `name2`='nhanketdoibacpt'
  WHERE `id`=89 AND `name` IN ('Nhẫn Trái tim bạc 4%pt','Nhẫn Vĩnh kết đồng tâm 4%pt');
UPDATE `data_potion` SET `name`='Nhẫn kết đôi vàng 7%pt', `name2`='nhanketdoivangpt'
  WHERE `id`=90 AND `name` IN ('Nhẫn Trái tim vàng 7%pt','Nhẫn Loan phụng hòa minh 7%pt');
UPDATE `data_potion` SET `name`='Thiệp mời kết đôi', `name2`='thiepmoiketdoi'
  WHERE `id`=92 AND `name`='Thiệp cưới';
UPDATE `data_potion` SET `name`='Vảy trái tim kết đôi', `name2`='vaytraitimketdoi'
  WHERE `id`=98 AND `name` IN ('Vảy trái tim','Phù phu thê');
UPDATE `data_item` SET `name`='Vảy trái tim kết đôi'
  WHERE `id`=751 AND `name` IN ('Vảy trái tim','Phù phu thê');

-- Phần thưởng sự kiện Valentine (tuần 8) là chính vật phẩm thuốc 98.
UPDATE `data_noi_dung_su_kien_vat_pham` SET `vat_pham`='Vảy trái tim kết đôi'
  WHERE `anh_xa`='thuoc:98' AND `vat_pham`='Vảy trái tim';

-- Tên bản đồ (game tự nạp lại data_map_ten trong 30 giây khi sua_luc đổi).
-- Luat khoa ban do (khoa-map, INSERT IGNORE nen CSDL da co giu dong cu): ly do / ghi chu con chu cu thi doi.
UPDATE `data_ban_do_mo_khoa` SET `ly_do_tu_dong`='Lễ kết đôi: không có Pokémon hoang dã để tự đánh.', `ghi_chu`=CONCAT('Lễ kết đôi ', `map_id` - 224)
  WHERE `map_id` IN (225, 226) AND `ly_do_tu_dong`='Lễ cưới: không có Pokémon hoang dã để tự đánh.';

UPDATE `data_map_ten` SET `ten`='Lễ kết đôi 1', `sua_boi`='sua_ket_doi.sql', `sua_luc`=NOW()
  WHERE `map_id`=225 AND `ten`='Lễ cưới 1';
UPDATE `data_map_ten` SET `ten`='Lễ kết đôi 2', `sua_boi`='sua_ket_doi.sql', `sua_luc`=NOW()
  WHERE `map_id`=226 AND `ten`='Lễ cưới 2';
-- >>> them_huong_dan_tan_thu.sql
-- =====================================================================================================================
--  HUONG DAN TAN THU + SUA CHUOI CHINH (nhanh huong-dan, client 0.9.7 tro len)
-- =====================================================================================================================
--
--  Chay lai nhieu lan van an toan (MariaDB 10.3, utf8). Khong can khoi dong lai de doi CHU (game nap lai bang
--  data_huong_dan_tan_thu moi 60 giay); doi buoc nhiem vu (data_quest_moi / data_quest_lau_dai) can khoi dong lai.
--
--  1. data_huong_dan_tan_thu: moi chu cua he huong dan (menu moi khi vao lang, Bo qua + xac nhan, So tay -> cong dung
--     tung NPC). Dong co sua_boi khac rong (sua tay / web) thi giu noi dung da sua.
--  2. data_vat_pham_nhiem_vu 51..58: thu / dau tap cua buoc huong dan (client hien tien do x/y nhu nhiem vu nhat do).
--  3. 10 buoc huong dan = 10 dong DAU chuoi chinh (data_quest_moi 901..910, ma HD.01..HD.10) - VIET LAI TAI CHO, cung id,
--     cung so dong: chi so chuoi chinh cua moi nhan vat cu khong doi. Moi buoc mot NPC trong lang (co o ca ba lang
--     Pewter / Blackthorn / Cianwood), muc tieu ro, loi thoai ngan, thuong nho: xu it, thuoc, mot Ruy bang; KHONG luong /
--     luong khoa. Buoc hanh dong (kieu 2) dem bang su kien that (dung_thuoc / hoc_chieu / nang_cap / tha_cau).
--  4. data_nhiem_vu_kieu: ba kieu muc tieu moi (diem moc o real.DiemMocNhiemVu).
--  5. data_npc.ban_do: Chansey + Snorlax nay dung o ca ba lang (server/cMap 0 / 70 / 80, tools/pokemon/ThemNpcLang.java).
--  6. Chuoi chinh sau huong dan: chuong 1-2 sua tay (loi dau lap lai "Tinh giac", NPC giao khac nguoi noi, muc tieu
--     khong kiem duoc); khoi SUA CHUOI CHINH (sinh boi tools/pokemon/sinh_sua_chuoi_chinh.py) sua "Dia diem" tro ban do
--     khong co quai / NPC va buoc noi chuyen giao nham NPC.
--
--  Kiem: server/test/KiemHuongDanTanThu (doc tep nay), bot tools/pokemon/kiem_tai/KichBanHuongDan.java (hd_*).
-- ---------------------------------------------------------------------------------------------------------------------

-- 1. Chu he huong dan ---------------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `data_huong_dan_tan_thu` (
  `ma` varchar(48) NOT NULL,
  `thu_tu` int(11) NOT NULL DEFAULT 0,
  `tieu_de` varchar(128) NOT NULL DEFAULT '',
  `noi_dung` text NOT NULL,
  `sua_boi` varchar(50) NOT NULL DEFAULT '',
  `sua_luc` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`ma`)
) DEFAULT CHARSET=utf8;

INSERT INTO `data_huong_dan_tan_thu` (`ma`, `thu_tu`, `tieu_de`, `noi_dung`) VALUES
('moi.chao', 1, '', 'Chào mừng tới làng! Đi theo dấu nhiệm vụ trên đầu NPC và chấm trên bản đồ nhỏ.'),
('moi.chao_phu', 2, '', 'Mở Nhiệm vụ, chọn nhiệm vụ rồi bấm Đi tới để tự tìm đường.'),
('moi.lam', 3, 'Làm hướng dẫn tân thủ (khuyên dùng)', 'Làm hướng dẫn tân thủ (khuyên dùng)'),
('moi.bo_qua', 4, 'Bỏ qua hướng dẫn (đã quen chơi)', 'Bỏ qua hướng dẫn (đã quen chơi)'),
('moi.bat_dau', 5, '', 'Hãy gặp Trưởng làng Xatu, NPC có dấu nhiệm vụ trên đầu.'),
('so_tay.bo_qua', 10, 'Bỏ qua hướng dẫn tân thủ', 'Bỏ qua hướng dẫn tân thủ'),
('so_tay.huong_dan', 11, 'Hướng dẫn tân thủ: công dụng các NPC', 'Hướng dẫn tân thủ: công dụng các NPC'),
('bo_qua.hoi', 20, '', 'Bỏ qua hướng dẫn tân thủ? Bạn vẫn nhận đủ thưởng của các bước còn lại: {thuong}. Không làm lại được.'),
('bo_qua.xong', 21, '', 'Đã bỏ qua hướng dẫn. Đã nhận: {thuong}. Gặp Trưởng làng Xatu để bắt đầu cốt truyện.'),
('bo_qua.da_xong', 22, '', 'Bạn đã xong hướng dẫn tân thủ.'),
('bo_qua.tui_day', 23, '', 'Hành trang không đủ chỗ cho phần thưởng. Hãy dọn bớt vài ô rồi thử lại.'),
('npc.01_xatu', 30, 'Xatu · trưởng làng', 'Trưởng làng Xatu: giao nhiệm vụ cốt truyện chính. Có mặt ở cả ba làng Pewter, Blackthorn, Cianwood.'),
('npc.02_chansey', 31, 'Chansey · hồi phục', 'Y tá Chansey đứng trước cửa Trung tâm Pokémon: hồi đầy HP và PP miễn phí, bán thuốc hồi phục.'),
('npc.03_kecleon', 32, 'Kecleon · tạp hoá', 'Kecleon tạp hoá: bán Thuốc hồi máu, Ether và đồ dùng hằng ngày. Chạm vào rồi chọn Mua.'),
('npc.04_blaziken', 33, 'Blaziken · huấn luyện', 'Huấn luyện viên Blaziken: dạy chiêu mới (trả xu). Điểm kỹ năng nhận khi lên cấp: Menu → Pokémon → Kỹ năng → Học.'),
('npc.05_machamp', 34, 'Machamp · thợ rèn', 'Machamp thợ rèn: nâng cấp, khảm ngọc, sửa và chế tạo trang bị.'),
('npc.06_bronzong', 35, 'Bronzong · bán giáp', 'Bronzong thợ giáp: bán trang bị phòng thủ theo hệ và cấp.'),
('npc.07_persian', 36, 'Persian · đá quý', 'Ngài Persian: bán đá quý và nguyên liệu nâng cấp.'),
('npc.08_meowth', 37, 'Meowth · ngân hàng', 'Meowth thương nhân: ngân hàng, gửi và rút xu, lượng; chuyển lượng cho Pokémon khác.'),
('npc.09_kangaskhan', 38, 'Kangaskhan · giữ đồ', 'Kangaskhan thủ kho: giữ bớt đồ khi hành trang đầy.'),
('npc.10_pidgeot', 39, 'Pidgeot · chở bay', 'Pidgeot: chở bay tới các vùng xa, trả bằng xu. Vùng mới mở dần theo cấp.'),
('npc.11_snorlax', 40, 'Snorlax · cốt truyện', 'Ông Snorlax: giao nhiệm vụ cốt truyện chương đầu, ngay trong làng.'),
('npc.12_doi_thuong', 41, 'Đổi thưởng · Sổ tay', 'NPC Đổi thưởng: đổi điểm lấy vật phẩm; mở Pokédex, Sổ tay huấn luyện (nhiệm vụ ngày, tuần, thách đấu Gym) và Hệ sinh thái.'),
('npc.13_tong_quan', 42, 'Tổng quản · quà tặng', 'Tổng quản: nhận quà Giftcode, mở rộng hành trang, chuyển vùng. Mã quà cũng nhập được ở Menu → Thư/Quà.'),
('npc.14_cau_ca', 43, 'Câu cá ở ao làng', 'Đứng sát mép ao trong làng, mở Menu → Hệ sinh thái → Câu cá. Mỗi lần câu tốn thể lực sinh hoạt và ít xu mua mồi.'),
('npc.15_tu_dong', 44, 'Tự động đánh', 'Nút Tự động trên màn hình: Pokémon tự đánh quái gần nhất và tự dùng thuốc theo ngưỡng. Chỉ dùng ở bản đồ có Pokémon hoang dã.'),
('npc.16_pokedex', 45, 'Pokédex', 'Menu → Pokédex: ghi mọi loài đã gặp và đã hạ; xong một vùng có thưởng.'),
('npc.17_thu', 46, 'Thư và quà', 'Menu → Thư/Quà: hộp thư và ô nhập mã quà tặng.')
ON DUPLICATE KEY UPDATE
  `thu_tu` = VALUES(`thu_tu`),
  `tieu_de` = IF(`sua_boi` = '', VALUES(`tieu_de`), `tieu_de`),
  `noi_dung` = IF(`sua_boi` = '', VALUES(`noi_dung`), `noi_dung`);

-- 2. Thu / dau tap cua buoc huong dan ----------------------------------------------------------------------------------
INSERT INTO `data_vat_pham_nhiem_vu` (`id`, `ten`) VALUES
(51, 'Thư giới thiệu của Xatu'),
(52, 'Điểm kỹ năng đã cộng'),
(53, 'Bình thuốc đã dùng'),
(54, 'Đơn thuốc của Chansey'),
(55, 'Ruy băng đã mặc'),
(56, 'Phiếu gửi của Machamp'),
(57, 'Lần thả câu'),
(58, 'Lông vũ Pidgeot')
ON DUPLICATE KEY UPDATE `ten` = VALUES(`ten`);

-- 3. Muoi buoc huong dan (cung id 901..910) ---------------------------------------------------------------------------
--    type: 3 noi chuyen (nghe het loi = xong), 1 mang do (nhan o npcrcv, tra o npcres), 0 ha quai, 2 hanh dong / nhat do.
--    content: dong "1<NPC>: ..." (NPC noi) / "0..." (nguoi choi); buoc khong phai noi chuyen: dong CUOI la cau hoi nhan.
INSERT INTO `data_quest_moi` (`id`, `name`, `npcrcv`, `npcres`, `lv`, `mainsub`, `content`, `scontent`, `rescontent`,
  `idItemget`, `idMonskill`, `type`, `nitemget`, `nmonskill`, `gold`, `exp`, `potiongift`, `npotion`, `itemgift`, `deltaLv`,
  `infogift`, `infofinish`, `pactive`, `luong`, `luongLock`) VALUES
(901, 'Chào trưởng làng', 5, 32000, 1, 0,
 '1Xatu: ...Cháu tới rồi. Ông nội cháu nhờ ta trông nom cháu. Ta là Xatu, trưởng làng.>1Xatu: Chạm xuống đất để đi. Chạm vào một Pokémon trong làng để nói chuyện.>1Xatu: Ai có dấu nhiệm vụ trên đầu là người cháu cần gặp. Bản đồ nhỏ cũng chấm chỗ họ đứng.>1Xatu: Mở Nhiệm vụ, chọn nhiệm vụ rồi bấm Đi tới, cháu sẽ tự tìm đường.>1Xatu: Đã quen chơi thì vào Sổ tay huấn luyện (NPC Đổi thưởng) mà bỏ qua hướng dẫn. Còn không, ta dẫn cháu đi một vòng làng.',
 'Nói chuyện với Xatu ở làng', '32000', '32000', '32000', 3, '32000', '32000', 100, 1292, '32000', '32000', '32000', 0,
 'Nhiệm vụ:|Nói chuyện với Xatu|Địa điểm:|Làng khởi đầu|NPC:|Xatu|Phần thưởng:|1.292 EXP, 100 xu',
 'Nhiệm vụ hoàn thành|Xatu: Việc đầu tiên: mang thư của ta cho Blaziken.', 0, 0, 0),
(902, 'Thư gửi Blaziken', 5, 21, 1, 0,
 '1Xatu: Blaziken là huấn luyện viên của làng. Cậu ấy dạy cách đánh và cách dùng chiêu thức.>1Xatu: Cháu mang thư giới thiệu này cho Blaziken nhé?',
 'Mang thư cho Blaziken, người có dấu nhiệm vụ trên đầu',
 '1Blaziken: Lửa lên nào! Thư của trưởng làng à? Tốt.>1Blaziken: Muốn mạnh lên thì đánh quái, cộng điểm kỹ năng và học chiêu mới ở chỗ ta. Cầm mấy bình thuốc này.',
 '51', '32000', 1, '1', '32000', 100, 1292, '1', '3', '32000', 0,
 'Nhiệm vụ:|Mang Thư giới thiệu cho Blaziken|Địa điểm:|Làng khởi đầu|NPC:|Xatu → Blaziken|Phần thưởng:|1.292 EXP, 100 xu, 3 Thuốc hồi máu',
 'Nhiệm vụ hoàn thành|Blaziken: Giờ ra đồng tập đánh thôi.', 0, 0, 0),
(903, 'Bình thuốc khi máu thấp', 21, 21, 1, 0,
 '1Blaziken: Ra cổng phía bắc của làng là đồng cỏ Pallet. Chạm vào quái để đánh, bấm nút chiêu để đánh mạnh hơn.>1Blaziken: Bị đánh sẽ mất máu. Máu vơi thì mở Hành trang dùng Thuốc hồi máu, hoặc đặt bình thuốc vào ô phím tắt.>1Blaziken: Ra đồng đánh thử, máu vơi thì uống 1 bình rồi về đây. Đi chứ?',
 'Đánh quái ở Pallet, máu vơi thì dùng 1 bình thuốc',
 '1Blaziken: Biết giữ máu là sống lâu. Cầm mấy bình Ether này, hết PP thì uống.>1Blaziken: Giờ là bài thật: hạ 8 Caterpie.',
 '53', '32000', 2, '1', '32000', 150, 1735, '4', '2', '32000', 0,
 'Nhiệm vụ:|Dùng 1 bình thuốc khi máu vơi|Địa điểm:|Pallet (cổng phía bắc làng)|NPC:|Blaziken|Phần thưởng:|1.735 EXP, 150 xu, 2 Ether',
 'Nhiệm vụ hoàn thành|Blaziken: Giờ là bài thật: hạ 8 Caterpie.', 0, 0, 0),
(904, 'Bài học chiến đấu', 21, 21, 1, 0,
 '1Blaziken: Caterpie sống ở đồng cỏ Pallet. Hạ 8 con rồi quay về đây.>1Blaziken: Mỏi tay thì bật Tự động: Pokémon tự đánh quái gần nhất, tự uống thuốc khi máu thấp. Đi chứ?',
 'Hạ 8 Caterpie ở Pallet',
 '1Blaziken: Khá lắm! Mỗi lần lên cấp cháu có thêm 1 điểm kỹ năng.>1Blaziken: Ta chỉ cháu cách dùng điểm đó.',
 '32000', '2', 0, '32000', '8', 100, 1735, '32000', '32000', '32000', 0,
 'Nhiệm vụ:|Hạ 8 Caterpie|Địa điểm:|Pallet (cổng phía bắc làng)|NPC:|Blaziken|Phần thưởng:|1.735 EXP, 100 xu',
 'Nhiệm vụ hoàn thành|Blaziken: Khá lắm! Mỗi lần lên cấp cháu có thêm 1 điểm kỹ năng.', 0, 0, 0),
(905, 'Chiêu thức đầu tiên', 21, 0, 1, 0,
 '1Blaziken: Lên cấp là được điểm kỹ năng. Mở Menu, chọn Pokémon, sang thẻ Kỹ năng, chọn chiêu rồi bấm Học để cộng 1 điểm.>1Blaziken: Chiêu mới thì học ở chỗ ta khi đủ xu. Cộng 1 điểm xong thì tới Chansey trước cửa Trung tâm Pokémon hồi sức nhé?',
 'Cộng 1 điểm kỹ năng, rồi gặp Chansey ở Trung tâm Pokémon',
 '1Chansey: Chan-sey! Chị là y tá của làng. Chạm vào chị để hồi đầy HP và PP, miễn phí.>1Chansey: Chị cũng bán thuốc. Cầm thêm mấy bình này nhé.',
 '52', '32000', 2, '1', '32000', 100, 2370, '1', '3', '32000', 0,
 'Nhiệm vụ:|Cộng 1 điểm kỹ năng|Địa điểm:|Menu → Pokémon → Kỹ năng, rồi Chansey|NPC:|Blaziken → Chansey|Phần thưởng:|2.370 EXP, 100 xu, 3 Thuốc hồi máu',
 'Nhiệm vụ hoàn thành|Chansey: Máu thấp thì về đây với chị.', 0, 0, 0),
(906, 'Đơn thuốc cho Kecleon', 0, 1, 1, 0,
 '1Chansey: Thuốc của chị nhập từ Kecleon, tiệm tạp hoá của làng.>1Chansey: Em mang đơn thuốc này cho Kecleon giúp chị nhé?',
 'Mang đơn thuốc cho Kecleon ở tiệm tạp hoá',
 '1Kecleon: Hí hí, khách mới! Tiệm tôi bán Thuốc hồi máu, Ether và đồ dùng hằng ngày.>1Kecleon: Chạm vào tôi, chọn Mua là xem hàng. Quà làm quen: một chiếc Ruy băng!',
 '54', '32000', 1, '1', '32000', 100, 2370, '32000', '32000', '1001,1001,1001,1001,1001,0', 0,
 'Nhiệm vụ:|Mang Đơn thuốc cho Kecleon|Địa điểm:|Làng khởi đầu|NPC:|Chansey → Kecleon|Phần thưởng:|2.370 EXP, 100 xu, Ruy băng sơ cấp',
 'Nhiệm vụ hoàn thành|Kecleon: Thử mặc Ruy băng xem nào.', 0, 0, 0),
(907, 'Mặc đồ mới', 1, 2, 1, 0,
 '1Kecleon: Mở Hành trang, chọn Ruy băng rồi chọn Mặc. Đồ mặc vào là tăng chỉ số ngay.>1Kecleon: Mặc xong thì ghé Machamp thợ rèn, cậu ấy lo nâng cấp đồ. Đi nhé?',
 'Mặc Ruy băng, rồi gặp Machamp thợ rèn',
 '1Machamp: Khà khà! Ruy băng hợp với cháu đấy.>1Machamp: Ta là thợ rèn: nâng cấp, khảm ngọc, sửa đồ. Bronzong bán giáp, Persian bán đá quý và nguyên liệu.',
 '55', '32000', 2, '1', '32000', 200, 3131, '32000', '32000', '32000', 0,
 'Nhiệm vụ:|Mặc Ruy băng sơ cấp|Địa điểm:|Hành trang, rồi Machamp|NPC:|Kecleon → Machamp|Phần thưởng:|3.131 EXP, 200 xu',
 'Nhiệm vụ hoàn thành|Machamp: Đồ tốt thì phải giữ cho kỹ.', 0, 0, 0),
(908, 'Ghé ngân hàng Meowth', 2, 22, 1, 0,
 '1Machamp: Xu kiếm được nên gửi bớt ở Meowth, ngân hàng của làng.>1Machamp: Cháu đưa phiếu này cho Meowth giúp ta nhé?',
 'Mang phiếu cho Meowth ở ngân hàng',
 '1Meowth: Meo! Ta giữ xu và lượng giúp cháu, chạm vào ta rồi chọn Kho xu, lượng.>1Meowth: Đồ nhiều thì gửi Kangaskhan thủ kho. Thư và mã quà tặng xem ở Menu → Thư/Quà.',
 '56', '32000', 1, '1', '32000', 200, 3131, '32000', '32000', '32000', 0,
 'Nhiệm vụ:|Mang Phiếu gửi cho Meowth|Địa điểm:|Làng khởi đầu|NPC:|Machamp → Meowth|Phần thưởng:|3.131 EXP, 200 xu',
 'Nhiệm vụ hoàn thành|Meowth: Ao trong làng có cá đấy, thử câu xem.', 0, 0, 0),
(909, 'Thả câu ở ao làng', 22, 7, 1, 0,
 '1Meowth: Ao trong làng có cá. Đứng sát mép ao, mở Menu → Hệ sinh thái → Câu cá để thả câu.>1Meowth: Mỗi lần câu tốn ít thể lực và xu mồi. Câu thử một lần rồi tới chỗ Pidgeot nhé?',
 'Thả câu 1 lần ở ao trong làng, rồi gặp Pidgeot',
 '1Pidgeot: Gió lên rồi! Ta chở cháu bay tới các vùng xa, trả bằng xu.>1Pidgeot: Vùng mới mở dần theo cấp. Cầm thêm mấy bình Ether cho chuyến đi.',
 '57', '32000', 2, '1', '32000', 150, 4269, '4', '2', '32000', 0,
 'Nhiệm vụ:|Thả câu 1 lần ở ao làng|Địa điểm:|Ao trong làng, rồi Pidgeot|NPC:|Meowth → Pidgeot|Phần thưởng:|4.269 EXP, 150 xu, 2 Ether',
 'Nhiệm vụ hoàn thành|Pidgeot: Về báo trưởng làng đi.', 0, 0, 0),
(910, 'Về báo trưởng làng', 7, 5, 1, 0,
 '1Pidgeot: Cầm chiếc lông vũ này về cho Xatu, ông ấy sẽ biết cháu đã đi hết làng.>1Pidgeot: Nhận lông vũ chứ?',
 'Mang Lông vũ Pidgeot về cho Xatu',
 '1Xatu: ...Cháu đã đi hết một vòng làng. Giỏi lắm.>1Xatu: Pokédex ở Menu ghi mọi loài cháu gặp. Sổ tay huấn luyện ở NPC Đổi thưởng có nhiệm vụ ngày, tuần và thách đấu Gym.>1Xatu: Từ giờ là hành trình thật sự. Đêm cháu tới có mưa sao băng... ta có chuyện muốn kể.',
 '58', '32000', 1, '1', '32000', 250, 4269, '32000', '32000', '32000', 0,
 'Nhiệm vụ:|Mang Lông vũ Pidgeot cho Xatu|Địa điểm:|Làng khởi đầu|NPC:|Pidgeot → Xatu|Phần thưởng:|4.269 EXP, 250 xu',
 'Nhiệm vụ hoàn thành|Xatu: Hết hướng dẫn. Nói chuyện với ta để bắt đầu cốt truyện.', 0, 0, 0)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`), `npcrcv` = VALUES(`npcrcv`), `npcres` = VALUES(`npcres`), `lv` = VALUES(`lv`),
  `mainsub` = VALUES(`mainsub`), `content` = VALUES(`content`), `scontent` = VALUES(`scontent`),
  `rescontent` = VALUES(`rescontent`), `idItemget` = VALUES(`idItemget`), `idMonskill` = VALUES(`idMonskill`),
  `type` = VALUES(`type`), `nitemget` = VALUES(`nitemget`), `nmonskill` = VALUES(`nmonskill`), `gold` = VALUES(`gold`),
  `exp` = VALUES(`exp`), `potiongift` = VALUES(`potiongift`), `npotion` = VALUES(`npotion`), `itemgift` = VALUES(`itemgift`),
  `deltaLv` = VALUES(`deltaLv`), `infogift` = VALUES(`infogift`), `infofinish` = VALUES(`infofinish`),
  `pactive` = VALUES(`pactive`), `luong` = VALUES(`luong`), `luongLock` = VALUES(`luongLock`);

-- meta (engine doc kieu_muc_tieu / muc_tieu_id cua buoc hanh dong; cap 1 het: cap chuoi chinh khong giam sau huong dan)
UPDATE `data_quest_lau_dai` SET `ten` = 'Chào trưởng làng', `npc_giao` = 5, `ten_npc` = 'Xatu', `dia_danh` = 'Làng', `cap_toi_thieu` = 1, `kieu_muc_tieu` = 'noi_chuyen', `muc_tieu_id` = '', `muc_tieu_ten` = '', `so_luong` = 1, `exp` = 1292, `xu` = 100, `luong_khoa` = 0, `luong` = 0, `vat_pham` = '', `ghi_chu` = 'Hướng dẫn tân thủ: 10 bước trong làng ~20 phút' WHERE `ma` = 'HD.01';
UPDATE `data_quest_lau_dai` SET `ten` = 'Thư gửi Blaziken', `npc_giao` = 5, `ten_npc` = 'Xatu', `dia_danh` = 'Làng', `cap_toi_thieu` = 1, `kieu_muc_tieu` = 'dua_thu', `muc_tieu_id` = '51', `muc_tieu_ten` = 'Thư giới thiệu của Xatu', `so_luong` = 1, `exp` = 1292, `xu` = 100, `luong_khoa` = 0, `luong` = 0, `vat_pham` = '', `ghi_chu` = 'Hướng dẫn tân thủ: 10 bước trong làng ~20 phút' WHERE `ma` = 'HD.02';
UPDATE `data_quest_lau_dai` SET `ten` = 'Bình thuốc khi máu thấp', `npc_giao` = 21, `ten_npc` = 'Blaziken', `dia_danh` = 'Pallet', `cap_toi_thieu` = 1, `kieu_muc_tieu` = 'dung_thuoc', `muc_tieu_id` = '', `muc_tieu_ten` = 'Bình thuốc', `so_luong` = 1, `exp` = 1735, `xu` = 150, `luong_khoa` = 0, `luong` = 0, `vat_pham` = '', `ghi_chu` = 'Hướng dẫn tân thủ: 10 bước trong làng ~20 phút' WHERE `ma` = 'HD.03';
UPDATE `data_quest_lau_dai` SET `ten` = 'Bài học chiến đấu', `npc_giao` = 21, `ten_npc` = 'Blaziken', `dia_danh` = 'Pallet', `cap_toi_thieu` = 1, `kieu_muc_tieu` = 'giet', `muc_tieu_id` = '2', `muc_tieu_ten` = 'Caterpie', `so_luong` = 8, `exp` = 1735, `xu` = 100, `luong_khoa` = 0, `luong` = 0, `vat_pham` = '', `ghi_chu` = 'Hướng dẫn tân thủ: 10 bước trong làng ~20 phút' WHERE `ma` = 'HD.04';
UPDATE `data_quest_lau_dai` SET `ten` = 'Chiêu thức đầu tiên', `npc_giao` = 21, `ten_npc` = 'Blaziken', `dia_danh` = 'Làng', `cap_toi_thieu` = 1, `kieu_muc_tieu` = 'hoc_chieu', `muc_tieu_id` = '', `muc_tieu_ten` = 'Điểm kỹ năng', `so_luong` = 1, `exp` = 2370, `xu` = 100, `luong_khoa` = 0, `luong` = 0, `vat_pham` = '', `ghi_chu` = 'Hướng dẫn tân thủ: 10 bước trong làng ~20 phút' WHERE `ma` = 'HD.05';
UPDATE `data_quest_lau_dai` SET `ten` = 'Đơn thuốc cho Kecleon', `npc_giao` = 0, `ten_npc` = 'Chansey', `dia_danh` = 'Làng', `cap_toi_thieu` = 1, `kieu_muc_tieu` = 'dua_thu', `muc_tieu_id` = '54', `muc_tieu_ten` = 'Đơn thuốc của Chansey', `so_luong` = 1, `exp` = 2370, `xu` = 100, `luong_khoa` = 0, `luong` = 0, `vat_pham` = '1001', `ghi_chu` = 'Hướng dẫn tân thủ: 10 bước trong làng ~20 phút' WHERE `ma` = 'HD.06';
UPDATE `data_quest_lau_dai` SET `ten` = 'Mặc đồ mới', `npc_giao` = 1, `ten_npc` = 'Kecleon', `dia_danh` = 'Làng', `cap_toi_thieu` = 1, `kieu_muc_tieu` = 'nang_cap', `muc_tieu_id` = '1001', `muc_tieu_ten` = 'Ruy băng sơ cấp', `so_luong` = 1, `exp` = 3131, `xu` = 200, `luong_khoa` = 0, `luong` = 0, `vat_pham` = '', `ghi_chu` = 'Hướng dẫn tân thủ: 10 bước trong làng ~20 phút' WHERE `ma` = 'HD.07';
UPDATE `data_quest_lau_dai` SET `ten` = 'Ghé ngân hàng Meowth', `npc_giao` = 2, `ten_npc` = 'Machamp', `dia_danh` = 'Làng', `cap_toi_thieu` = 1, `kieu_muc_tieu` = 'dua_thu', `muc_tieu_id` = '56', `muc_tieu_ten` = 'Phiếu gửi của Machamp', `so_luong` = 1, `exp` = 3131, `xu` = 200, `luong_khoa` = 0, `luong` = 0, `vat_pham` = '', `ghi_chu` = 'Hướng dẫn tân thủ: 10 bước trong làng ~20 phút' WHERE `ma` = 'HD.08';
UPDATE `data_quest_lau_dai` SET `ten` = 'Thả câu ở ao làng', `npc_giao` = 22, `ten_npc` = 'Meowth', `dia_danh` = 'Làng', `cap_toi_thieu` = 1, `kieu_muc_tieu` = 'tha_cau', `muc_tieu_id` = '', `muc_tieu_ten` = 'Lần thả câu', `so_luong` = 1, `exp` = 4269, `xu` = 150, `luong_khoa` = 0, `luong` = 0, `vat_pham` = '', `ghi_chu` = 'Hướng dẫn tân thủ: 10 bước trong làng ~20 phút' WHERE `ma` = 'HD.09';
UPDATE `data_quest_lau_dai` SET `ten` = 'Về báo trưởng làng', `npc_giao` = 7, `ten_npc` = 'Pidgeot', `dia_danh` = 'Làng', `cap_toi_thieu` = 1, `kieu_muc_tieu` = 'dua_thu', `muc_tieu_id` = '58', `muc_tieu_ten` = 'Lông vũ Pidgeot', `so_luong` = 1, `exp` = 4269, `xu` = 250, `luong_khoa` = 0, `luong` = 0, `vat_pham` = '', `ghi_chu` = 'Hướng dẫn tân thủ: 10 bước trong làng ~20 phút' WHERE `ma` = 'HD.10';

-- noi dung (web / So tay doc; client doc data_quest_moi)
UPDATE `data_nhiem_vu_noi_dung` d JOIN `data_quest_moi` q ON q.`id` = CAST(SUBSTRING(d.`ma`, 4) AS UNSIGNED) + 900
  SET d.`mo_ta` = q.`scontent`, d.`muc_tieu_text` = q.`scontent`, d.`goi_y` = q.`scontent`,
      d.`hoi_thoai_bat_dau` = REPLACE(REPLACE(SUBSTRING(q.`content`, 2), '>1', ' | '), '>0', ' | Bạn: '),
      d.`hoi_thoai_xong` = SUBSTRING_INDEX(q.`infofinish`, '|', -1),
      d.`dong_thuong` = CONCAT('Thưởng: ', SUBSTRING_INDEX(q.`infogift`, '|', -1)), d.`thuong_khac` = ''
  WHERE d.`ma` LIKE 'HD.%' AND q.`mainsub` = 0;

-- 4. Kieu muc tieu moi -------------------------------------------------------------------------------------------------
INSERT INTO `data_nhiem_vu_kieu` (`ma`, `ten`, `co_che`, `du_phong`, `trang_thai`, `so_dong`) VALUES
('dung_thuoc', 'Dùng thuốc', 'DungVatPham.dungThuoc (bình đã trừ khỏi túi) → DiemMocNhiemVu.khiDungThuoc', '2', 'co_san', 1),
('hoc_chieu', 'Học chiêu', 'Map.doAddSkillPoint / doLearnSkil → DiemMocNhiemVu.khiHocChieu', '2', 'co_san', 1),
('tha_cau', 'Thả câu', 'HeSinhThaiMenu.thaCau (đã trừ thể lực + mồi) → DiemMocNhiemVu.khiThaCau', '2', 'co_san', 1)
ON DUPLICATE KEY UPDATE `ten` = VALUES(`ten`), `co_che` = VALUES(`co_che`), `du_phong` = VALUES(`du_phong`),
  `trang_thai` = VALUES(`trang_thai`), `so_dong` = VALUES(`so_dong`);

-- 5. Chansey + Snorlax o ca ba lang (cMap 0 / 70 / 80) ---------------------------------------------------------------
UPDATE `data_npc` SET `ban_do` = '0,43,45,47,49,51,54,56,59,60,62,70,80,100,281' WHERE `type` = 0;
UPDATE `data_npc` SET `ban_do` = '0,51,57,60,70,80,105,160' WHERE `type` = 8;

-- 6. Chuong 1-2: noi tiep huong dan, NPC giao = nguoi noi, muc tieu dung voi cach game tinh (noi chuyen) ---------------
UPDATE `data_quest_moi` SET `name` = 'Đêm mưa sao băng',
  `content` = '1Xatu: Đêm cháu tới, cả làng thấy một trận mưa sao băng.>1Xatu: Ta mơ thấy một ngôi sao rơi xuống đồng cỏ, và cháu đứng ngay chỗ nó rơi.>0Cháu... không nhớ gì về đêm đó cả.>1Xatu: Không sao. Ký ức sẽ về khi cháu bước đi.',
  `scontent` = 'Nói chuyện với Xatu ở làng',
  `infogift` = REPLACE(REPLACE(`infogift`, 'Nói chuyện với Xatu ở Pallet|', 'Nói chuyện với Xatu|'), 'Địa điểm:|Pallet|', 'Địa điểm:|Làng khởi đầu|'),
  `infofinish` = 'Nhiệm vụ hoàn thành|Xatu: Ông Snorlax ở làng sẽ cho cháu ăn sáng, rồi ông kể tiếp.'
  WHERE `id` = 1001 AND `mainsub` = 0;
UPDATE `data_quest_lau_dai` SET `ten` = 'Đêm mưa sao băng', `dia_danh` = 'Làng' WHERE `ma` = 'C01.1';
UPDATE `data_quest_moi` SET `npcrcv` = 0,
  `scontent` = 'Nghe Chansey kể về nơi ngôi sao rơi',
  `infogift` = REPLACE(REPLACE(`infogift`, 'Đứng tại điểm đánh dấu trên Pallet (ô 53,19)|', 'Nghe Chansey kể về nơi ngôi sao rơi|'), 'Địa điểm:|Pallet|', 'Địa điểm:|Làng khởi đầu|')
  WHERE `id` = 1005 AND `mainsub` = 0;
UPDATE `data_quest_lau_dai` SET `npc_giao` = 0, `ten_npc` = 'Chansey' WHERE `ma` = 'C01.5';
UPDATE `data_quest_moi` SET
  `scontent` = 'Nghe Kecleon bày cách phục kích ở bụi cỏ cao',
  `infogift` = REPLACE(REPLACE(`infogift`, 'Đứng tại điểm đánh dấu trên Pallet (ô 53,19)|', 'Nghe Kecleon bày cách phục kích|'), 'Địa điểm:|Pallet|', 'Địa điểm:|Làng khởi đầu|')
  WHERE `id` = 1010 AND `mainsub` = 0;

-- SUA CHUOI CHINH (sinh: tools/pokemon/sinh_sua_chuoi_chinh.py) ------------------------------------------------------
-- 1002 C01.2: NPC 8 chi o [0, 51, 57, 60, 70, 80, 105, 160]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Pallet', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Pallet|', 'Địa điểm:|Làng khởi đầu|'), ' ở Pallet|', ' ở làng|'), 200) WHERE `id` = 1002 AND `mainsub` = 0;
-- 1004 C01.4: NPC 0 chi o [0, 43, 45, 47, 49, 51, 54, 56, 59, 60, 62, 70, 80, 100, 281]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Pallet', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Pallet|', 'Địa điểm:|Làng khởi đầu|'), ' ở Pallet|', ' ở làng|'), 200) WHERE `id` = 1004 AND `mainsub` = 0;
-- 1007 C02.1: NPC 6 chi o [0, 53, 70, 79, 80, 89, 109]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Pallet', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Pallet|', 'Địa điểm:|Làng khởi đầu|'), ' ở Pallet|', ' ở làng|'), 200) WHERE `id` = 1007 AND `mainsub` = 0;
-- 1009 C02.3: NPC 1 chi o [0, 45, 47, 49, 54, 56, 62, 70, 72, 80, 82, 102]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Pallet', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Pallet|', 'Địa điểm:|Làng khởi đầu|'), ' ở Pallet|', ' ở làng|'), 200) WHERE `id` = 1009 AND `mainsub` = 0;
-- 1011 C02.5: quai 5 chi moc o [2]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Pallet', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở Viridian'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Pallet|', 'Địa điểm:|Viridian|'), 200) WHERE `id` = 1011 AND `mainsub` = 0;
-- 1013 C03.1: NPC 1 chi o [0, 45, 47, 49, 54, 56, 62, 70, 72, 80, 82, 102]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Viridian', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Viridian|', 'Địa điểm:|Làng khởi đầu|'), ' ở Viridian|', ' ở làng|'), 200) WHERE `id` = 1013 AND `mainsub` = 0;
-- 1014 C03.2: quai 6 chi moc o [4]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Viridian', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Cerulean'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Viridian|', 'Địa điểm:|Cerulean|'), 200) WHERE `id` = 1014 AND `mainsub` = 0;
-- 1017 C03.5: quai 9 chi moc o [3]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Viridian', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Vermilion'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Viridian|', 'Địa điểm:|Vermilion|'), 200) WHERE `id` = 1017 AND `mainsub` = 0;
-- 1018 C03.6: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Viridian', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Viridian|', 'Địa điểm:|Làng khởi đầu|'), ' ở Viridian|', ' ở làng|'), 200) WHERE `id` = 1018 AND `mainsub` = 0;
-- 1019 C04.1: NPC 21 chi o [0, 44, 50, 51, 52, 62, 70, 80, 163, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Rừng Viridian', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Rừng Viridian|', 'Địa điểm:|Làng khởi đầu|'), ' ở Rừng Viridian|', ' ở làng|'), 200) WHERE `id` = 1019 AND `mainsub` = 0;
-- 1023 C04.5: quai 11 chi moc o [5]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Rừng Viridian', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở Lavender'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Rừng Viridian|', 'Địa điểm:|Lavender|'), 200) WHERE `id` = 1023 AND `mainsub` = 0;
-- 1024 C04.6: quai 7 chi moc o [3, 4]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Rừng Viridian', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở Vermilion'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Rừng Viridian|', 'Địa điểm:|Vermilion|'), 200) WHERE `id` = 1024 AND `mainsub` = 0;
-- 1025 C04.7: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Rừng Viridian', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Rừng Viridian|', 'Địa điểm:|Làng khởi đầu|'), ' ở Rừng Viridian|', ' ở làng|'), 200) WHERE `id` = 1025 AND `mainsub` = 0;
-- 1029 C05.4: quai 10 chi moc o [3]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Pewter', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở Vermilion'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Pewter|', 'Địa điểm:|Vermilion|'), 200) WHERE `id` = 1029 AND `mainsub` = 0;
-- 1030 C05.5: quai 9 chi moc o [3]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Pewter', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở Vermilion'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Pewter|', 'Địa điểm:|Vermilion|'), 200) WHERE `id` = 1030 AND `mainsub` = 0;
-- 1035 C06.4: quai 17 chi moc o [6]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Pewter', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở Celadon'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Pewter|', 'Địa điểm:|Celadon|'), 200) WHERE `id` = 1035 AND `mainsub` = 0;
-- 1037 C06.6: quai 16 chi moc o [7]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Pewter|', 'Địa điểm:|Saffron|'), 200) WHERE `id` = 1037 AND `mainsub` = 0;
-- 1038 C07.1: NPC 8 chi o [0, 51, 57, 60, 70, 80, 105, 160]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Núi Mặt Trăng', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Núi Mặt Trăng|', 'Địa điểm:|Làng khởi đầu|'), ' ở Núi Mặt Trăng|', ' ở làng|'), 200) WHERE `id` = 1038 AND `mainsub` = 0;
-- 1039 C07.2: quai 15 chi moc o [6]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Núi Mặt Trăng', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở Celadon'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Núi Mặt Trăng|', 'Địa điểm:|Celadon|'), 200) WHERE `id` = 1039 AND `mainsub` = 0;
UPDATE `data_quest_lau_dai` SET `npc_giao` = 0, `ten_npc` = 'Chansey' WHERE `id_so` = 1040;
-- 1040 C07.3: loi dau cua Chansey, giao boi Snorlax
UPDATE `data_quest_moi` SET `npcrcv` = 0, `infogift` = LEFT(REPLACE(`infogift`, 'NPC:|Snorlax|', 'NPC:|Chansey|'), 200) WHERE `id` = 1040 AND `mainsub` = 0;
-- 1041 C07.4: NPC 8 chi o [0, 51, 57, 60, 70, 80, 105, 160]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Núi Mặt Trăng', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Núi Mặt Trăng|', 'Địa điểm:|Làng khởi đầu|'), ' ở Núi Mặt Trăng|', ' ở làng|'), 200) WHERE `id` = 1041 AND `mainsub` = 0;
-- 1042 C07.5: quai 16 chi moc o [7]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Núi Mặt Trăng', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở Saffron'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Núi Mặt Trăng|', 'Địa điểm:|Saffron|'), 200) WHERE `id` = 1042 AND `mainsub` = 0;
-- 1044 C08.1: NPC 21 chi o [0, 44, 50, 51, 52, 62, 70, 80, 163, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Núi Mặt Trăng', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Núi Mặt Trăng|', 'Địa điểm:|Làng khởi đầu|'), ' ở Núi Mặt Trăng|', ' ở làng|'), 200) WHERE `id` = 1044 AND `mainsub` = 0;
-- 1046 C08.3: quai 19 chi moc o [7]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Núi Mặt Trăng', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở Saffron'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Núi Mặt Trăng|', 'Địa điểm:|Saffron|'), 200) WHERE `id` = 1046 AND `mainsub` = 0;
-- 1047 C08.4: quai 14 chi moc o [6]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Núi Mặt Trăng', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở Celadon'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Núi Mặt Trăng|', 'Địa điểm:|Celadon|'), 200) WHERE `id` = 1047 AND `mainsub` = 0;
-- 1050 C08.7: quai 28 chi moc o [8]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Núi Mặt Trăng', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở Fuchsia'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Núi Mặt Trăng|', 'Địa điểm:|Fuchsia|'), 200) WHERE `id` = 1050 AND `mainsub` = 0;
-- 1053 C09.3: quai 26 chi moc o [9]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cerulean', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Cinnabar'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cerulean|', 'Địa điểm:|Cinnabar|'), 200) WHERE `id` = 1053 AND `mainsub` = 0;
-- 1058 C10.2: NPC 10 chi o [43, 58, 60, 106, 162]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cerulean', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Bến tàu'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Cerulean|', 'Địa điểm:|Bến tàu|'), ' ở Cerulean|', ' ở Bến tàu|'), 200) WHERE `id` = 1058 AND `mainsub` = 0;
-- 1060 C10.4: quai 28 chi moc o [8]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cerulean', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Fuchsia'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cerulean|', 'Địa điểm:|Fuchsia|'), 200) WHERE `id` = 1060 AND `mainsub` = 0;
-- 1061 C10.5: quai 28 chi moc o [8]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cerulean', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Fuchsia'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cerulean|', 'Địa điểm:|Fuchsia|'), 200) WHERE `id` = 1061 AND `mainsub` = 0;
-- 1062 C10.6: quai 26 chi moc o [9]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cerulean|', 'Địa điểm:|Cinnabar|'), 200) WHERE `id` = 1062 AND `mainsub` = 0;
-- 1063 C11.1: NPC 22 chi o [0, 47, 49, 56, 62, 70, 80]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Vermilion', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Vermilion|', 'Địa điểm:|Làng khởi đầu|'), ' ở Vermilion|', ' ở làng|'), 200) WHERE `id` = 1063 AND `mainsub` = 0;
-- 1065 C11.3: NPC 9 chi o [0, 70, 78, 80, 88, 108]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Vermilion', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Vermilion|', 'Địa điểm:|Làng khởi đầu|'), ' ở Vermilion|', ' ở làng|'), 200) WHERE `id` = 1065 AND `mainsub` = 0;
-- 1066 C11.4: quai 26 chi moc o [9]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Vermilion', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở Cinnabar'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Vermilion|', 'Địa điểm:|Cinnabar|'), 200) WHERE `id` = 1066 AND `mainsub` = 0;
-- 1067 C11.5: quai 26 chi moc o [9]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Vermilion', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở Cinnabar'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Vermilion|', 'Địa điểm:|Cinnabar|'), 200) WHERE `id` = 1067 AND `mainsub` = 0;
-- 1068 C11.6: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Vermilion', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Vermilion|', 'Địa điểm:|Làng khởi đầu|'), ' ở Vermilion|', ' ở làng|'), 200) WHERE `id` = 1068 AND `mainsub` = 0;
-- 1069 C12.1: NPC 21 chi o [0, 44, 50, 51, 52, 62, 70, 80, 163, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Vermilion', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Vermilion|', 'Địa điểm:|Làng khởi đầu|'), ' ở Vermilion|', ' ở làng|'), 200) WHERE `id` = 1069 AND `mainsub` = 0;
-- 1072 C12.4: quai 27 chi moc o [110]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Vermilion', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở Hang động'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Vermilion|', 'Địa điểm:|Hang động|'), 200) WHERE `id` = 1072 AND `mainsub` = 0;
-- 1073 C12.5: NPC 21 chi o [0, 44, 50, 51, 52, 62, 70, 80, 163, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Vermilion', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Vermilion|', 'Địa điểm:|Làng khởi đầu|'), ' ở Vermilion|', ' ở làng|'), 200) WHERE `id` = 1073 AND `mainsub` = 0;
-- 1074 C12.6: quai 29 chi moc o [9]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Vermilion|', 'Địa điểm:|Cinnabar|'), 200) WHERE `id` = 1074 AND `mainsub` = 0;
-- 1075 C13.1: NPC 22 chi o [0, 47, 49, 56, 62, 70, 80]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Vermilion', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Vermilion|', 'Địa điểm:|Làng khởi đầu|'), ' ở Vermilion|', ' ở làng|'), 200) WHERE `id` = 1075 AND `mainsub` = 0;
-- 1077 C13.3: quai 27 chi moc o [110]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Vermilion', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở Hang động'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Vermilion|', 'Địa điểm:|Hang động|'), 200) WHERE `id` = 1077 AND `mainsub` = 0;
-- 1078 C13.4: NPC 6 chi o [0, 53, 70, 79, 80, 89, 109]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Vermilion', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Vermilion|', 'Địa điểm:|Làng khởi đầu|'), ' ở Vermilion|', ' ở làng|'), 200) WHERE `id` = 1078 AND `mainsub` = 0;
-- 1079 C13.5: NPC 22 chi o [0, 47, 49, 56, 62, 70, 80]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Vermilion', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Vermilion|', 'Địa điểm:|Làng khởi đầu|'), ' ở Vermilion|', ' ở làng|'), 200) WHERE `id` = 1079 AND `mainsub` = 0;
-- 1081 C14.1: NPC 6 chi o [0, 53, 70, 79, 80, 89, 109]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Tàu S.S. Anne', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Tàu S.S. Anne|', 'Địa điểm:|Làng khởi đầu|'), ' ở Tàu S.S. Anne|', ' ở làng|'), 200) WHERE `id` = 1081 AND `mainsub` = 0;
-- 1082 C14.2: NPC 9 chi o [0, 70, 78, 80, 88, 108]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Tàu S.S. Anne', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Tàu S.S. Anne|', 'Địa điểm:|Làng khởi đầu|'), ' ở Tàu S.S. Anne|', ' ở làng|'), 200) WHERE `id` = 1082 AND `mainsub` = 0;
-- 1083 C14.3: NPC 1 chi o [0, 45, 47, 49, 54, 56, 62, 70, 72, 80, 82, 102]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Tàu S.S. Anne', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Tàu S.S. Anne|', 'Địa điểm:|Làng khởi đầu|'), ' ở Tàu S.S. Anne|', ' ở làng|'), 200) WHERE `id` = 1083 AND `mainsub` = 0;
-- 1084 C14.4: quai 29 chi moc o [9]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Tàu S.S. Anne', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở Cinnabar'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Tàu S.S. Anne|', 'Địa điểm:|Cinnabar|'), 200) WHERE `id` = 1084 AND `mainsub` = 0;
-- 1086 C14.6: quai 18 chi moc o [110]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Tàu S.S. Anne', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở Hang động'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Tàu S.S. Anne|', 'Địa điểm:|Hang động|'), 200) WHERE `id` = 1086 AND `mainsub` = 0;
-- 1087 C15.1: NPC 9 chi o [0, 70, 78, 80, 88, 108]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Tàu S.S. Anne', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Tàu S.S. Anne|', 'Địa điểm:|Làng khởi đầu|'), ' ở Tàu S.S. Anne|', ' ở làng|'), 200) WHERE `id` = 1087 AND `mainsub` = 0;
-- 1088 C15.2: quai 29 chi moc o [9]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Tàu S.S. Anne', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở Cinnabar'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Tàu S.S. Anne|', 'Địa điểm:|Cinnabar|'), 200) WHERE `id` = 1088 AND `mainsub` = 0;
-- 1090 C15.4: quai 22 chi moc o [10]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Tàu S.S. Anne', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở New Bark'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Tàu S.S. Anne|', 'Địa điểm:|New Bark|'), 200) WHERE `id` = 1090 AND `mainsub` = 0;
-- 1091 C15.5: quai 30 chi moc o [9]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Tàu S.S. Anne', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở Cinnabar'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Tàu S.S. Anne|', 'Địa điểm:|Cinnabar|'), 200) WHERE `id` = 1091 AND `mainsub` = 0;
-- 1092 C15.6: NPC 6 chi o [0, 53, 70, 79, 80, 89, 109]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Tàu S.S. Anne', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Tàu S.S. Anne|', 'Địa điểm:|Làng khởi đầu|'), ' ở Tàu S.S. Anne|', ' ở làng|'), 200) WHERE `id` = 1092 AND `mainsub` = 0;
-- 1093 C16.1: NPC 21 chi o [0, 44, 50, 51, 52, 62, 70, 80, 163, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Tàu S.S. Anne', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Tàu S.S. Anne|', 'Địa điểm:|Làng khởi đầu|'), ' ở Tàu S.S. Anne|', ' ở làng|'), 200) WHERE `id` = 1093 AND `mainsub` = 0;
-- 1095 C16.3: NPC 10 chi o [43, 58, 60, 106, 162]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Tàu S.S. Anne', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở Bến tàu'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Tàu S.S. Anne|', 'Địa điểm:|Bến tàu|'), ' ở Tàu S.S. Anne|', ' ở Bến tàu|'), 200) WHERE `id` = 1095 AND `mainsub` = 0;
-- 1096 C16.4: NPC 0 chi o [0, 43, 45, 47, 49, 51, 54, 56, 59, 60, 62, 70, 80, 100, 281]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Tàu S.S. Anne', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Tàu S.S. Anne|', 'Địa điểm:|Làng khởi đầu|'), ' ở Tàu S.S. Anne|', ' ở làng|'), 200) WHERE `id` = 1096 AND `mainsub` = 0;
-- 1097 C16.5: quai 31 chi moc o [11]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Tàu S.S. Anne', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở Rừng Viridian'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Tàu S.S. Anne|', 'Địa điểm:|Rừng Viridian|'), 200) WHERE `id` = 1097 AND `mainsub` = 0;
-- 1099 C17.1: NPC 8 chi o [0, 51, 57, 60, 70, 80, 105, 160]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Lavender', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Lavender|', 'Địa điểm:|Làng khởi đầu|'), ' ở Lavender|', ' ở làng|'), 200) WHERE `id` = 1099 AND `mainsub` = 0;
-- 1101 C17.3: quai 32 chi moc o [10]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Lavender', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở New Bark'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Lavender|', 'Địa điểm:|New Bark|'), 200) WHERE `id` = 1101 AND `mainsub` = 0;
-- 1103 C17.5: quai 23 chi moc o [10]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Lavender', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở New Bark'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Lavender|', 'Địa điểm:|New Bark|'), 200) WHERE `id` = 1103 AND `mainsub` = 0;
-- 1104 C17.6: NPC 8 chi o [0, 51, 57, 60, 70, 80, 105, 160]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Lavender', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Lavender|', 'Địa điểm:|Làng khởi đầu|'), ' ở Lavender|', ' ở làng|'), 200) WHERE `id` = 1104 AND `mainsub` = 0;
-- 1105 C18.1: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Lavender', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Lavender|', 'Địa điểm:|Làng khởi đầu|'), ' ở Lavender|', ' ở làng|'), 200) WHERE `id` = 1105 AND `mainsub` = 0;
-- 1107 C18.3: quai 31 chi moc o [11]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Lavender', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Rừng Viridian'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Lavender|', 'Địa điểm:|Rừng Viridian|'), 200) WHERE `id` = 1107 AND `mainsub` = 0;
-- 1108 C18.4: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Lavender', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Lavender|', 'Địa điểm:|Làng khởi đầu|'), ' ở Lavender|', ' ở làng|'), 200) WHERE `id` = 1108 AND `mainsub` = 0;
-- 1110 C18.6: NPC 8 chi o [0, 51, 57, 60, 70, 80, 105, 160]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Lavender', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Lavender|', 'Địa điểm:|Làng khởi đầu|'), ' ở Lavender|', ' ở làng|'), 200) WHERE `id` = 1110 AND `mainsub` = 0;
-- 1111 C19.1: NPC 1 chi o [0, 45, 47, 49, 54, 56, 62, 70, 72, 80, 82, 102]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Celadon', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Celadon|', 'Địa điểm:|Làng khởi đầu|'), ' ở Celadon|', ' ở làng|'), 200) WHERE `id` = 1111 AND `mainsub` = 0;
-- 1114 C19.4: quai 55 chi moc o [13]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Celadon', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở Núi Mặt Trăng'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Celadon|', 'Địa điểm:|Núi Mặt Trăng|'), 200) WHERE `id` = 1114 AND `mainsub` = 0;
-- 1115 C19.5: quai 31 chi moc o [11]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Celadon', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở Rừng Viridian'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Celadon|', 'Địa điểm:|Rừng Viridian|'), 200) WHERE `id` = 1115 AND `mainsub` = 0;
-- 1116 C19.6: NPC 1 chi o [0, 45, 47, 49, 54, 56, 62, 70, 72, 80, 82, 102]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Celadon', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Celadon|', 'Địa điểm:|Làng khởi đầu|'), ' ở Celadon|', ' ở làng|'), 200) WHERE `id` = 1116 AND `mainsub` = 0;
-- 1117 C20.1: NPC 6 chi o [0, 53, 70, 79, 80, 89, 109]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Celadon', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Celadon|', 'Địa điểm:|Làng khởi đầu|'), ' ở Celadon|', ' ở làng|'), 200) WHERE `id` = 1117 AND `mainsub` = 0;
-- 1118 C20.2: quai 33 chi moc o [11]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Celadon', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở Rừng Viridian'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Celadon|', 'Địa điểm:|Rừng Viridian|'), 200) WHERE `id` = 1118 AND `mainsub` = 0;
-- 1119 C20.3: quai 35 chi moc o [11]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Celadon', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở Rừng Viridian'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Celadon|', 'Địa điểm:|Rừng Viridian|'), 200) WHERE `id` = 1119 AND `mainsub` = 0;
-- 1120 C20.4: quai 35 chi moc o [11]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Celadon', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở Rừng Viridian'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Celadon|', 'Địa điểm:|Rừng Viridian|'), 200) WHERE `id` = 1120 AND `mainsub` = 0;
-- 1121 C20.5: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Celadon', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Celadon|', 'Địa điểm:|Làng khởi đầu|'), ' ở Celadon|', ' ở làng|'), 200) WHERE `id` = 1121 AND `mainsub` = 0;
-- 1122 C20.6: quai 35 chi moc o [11]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Celadon', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở Rừng Viridian'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Celadon|', 'Địa điểm:|Rừng Viridian|'), 200) WHERE `id` = 1122 AND `mainsub` = 0;
-- 1123 C20.7: quai 40 chi moc o [13]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Celadon|', 'Địa điểm:|Núi Mặt Trăng|'), 200) WHERE `id` = 1123 AND `mainsub` = 0;
-- 1124 C21.1: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Saffron', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Saffron|', 'Địa điểm:|Làng khởi đầu|'), ' ở Saffron|', ' ở làng|'), 200) WHERE `id` = 1124 AND `mainsub` = 0;
UPDATE `data_quest_lau_dai` SET `npc_giao` = 3, `ten_npc` = 'Bronzong' WHERE `id_so` = 1125;
-- 1125 C21.2: loi dau cua Bronzong, giao boi Xatu
UPDATE `data_quest_moi` SET `npcrcv` = 3, `infogift` = LEFT(REPLACE(`infogift`, 'NPC:|Xatu|', 'NPC:|Bronzong|'), 200) WHERE `id` = 1125 AND `mainsub` = 0;
UPDATE `data_quest_lau_dai` SET `npc_giao` = 0, `ten_npc` = 'Chansey' WHERE `id_so` = 1128;
-- 1128 C21.5: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]; loi dau cua Chansey, giao boi Xatu
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Saffron', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở làng'), `scontent`), `npcrcv` = 0, `infogift` = LEFT(REPLACE(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Saffron|', 'Địa điểm:|Làng khởi đầu|'), ' ở Saffron|', ' ở làng|'), 'NPC:|Xatu|', 'NPC:|Chansey|'), 200) WHERE `id` = 1128 AND `mainsub` = 0;
-- 1129 C21.6: quai 55 chi moc o [13]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Saffron', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở Núi Mặt Trăng'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Saffron|', 'Địa điểm:|Núi Mặt Trăng|'), 200) WHERE `id` = 1129 AND `mainsub` = 0;
-- 1130 C22.1: NPC 2 chi o [0, 45, 70, 73, 80, 83, 103]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Saffron', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Saffron|', 'Địa điểm:|Làng khởi đầu|'), ' ở Saffron|', ' ở làng|'), 200) WHERE `id` = 1130 AND `mainsub` = 0;
-- 1132 C22.3: quai 40 chi moc o [13]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Saffron', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở Núi Mặt Trăng'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Saffron|', 'Địa điểm:|Núi Mặt Trăng|'), 200) WHERE `id` = 1132 AND `mainsub` = 0;
-- 1133 C22.4: quai 71 chi moc o [14]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Saffron', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở Vùng Safari'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Saffron|', 'Địa điểm:|Vùng Safari|'), 200) WHERE `id` = 1133 AND `mainsub` = 0;
-- 1134 C22.5: quai 40 chi moc o [13]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Saffron', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở Núi Mặt Trăng'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Saffron|', 'Địa điểm:|Núi Mặt Trăng|'), 200) WHERE `id` = 1134 AND `mainsub` = 0;
-- 1135 C22.6: NPC 2 chi o [0, 45, 70, 73, 80, 83, 103]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Saffron', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Saffron|', 'Địa điểm:|Làng khởi đầu|'), ' ở Saffron|', ' ở làng|'), 200) WHERE `id` = 1135 AND `mainsub` = 0;
-- 1136 C22.7: quai 68 chi moc o [14]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Saffron|', 'Địa điểm:|Vùng Safari|'), 200) WHERE `id` = 1136 AND `mainsub` = 0;
-- 1137 C23.1: NPC 22 chi o [0, 47, 49, 56, 62, 70, 80]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Vùng Safari', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 14), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Vùng Safari|', 'Địa điểm:|Làng khởi đầu|'), ' ở Vùng Safari|', ' ở làng|'), 200) WHERE `id` = 1137 AND `mainsub` = 0;
-- 1141 C23.5: quai 45 chi moc o [13]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Vùng Safari', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 14), ' ở Núi Mặt Trăng'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Vùng Safari|', 'Địa điểm:|Núi Mặt Trăng|'), 200) WHERE `id` = 1141 AND `mainsub` = 0;
-- 1142 C23.6: NPC 22 chi o [0, 47, 49, 56, 62, 70, 80]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Vùng Safari', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 14), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Vùng Safari|', 'Địa điểm:|Làng khởi đầu|'), ' ở Vùng Safari|', ' ở làng|'), 200) WHERE `id` = 1142 AND `mainsub` = 0;
-- 1143 C24.1: NPC 21 chi o [0, 44, 50, 51, 52, 62, 70, 80, 163, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Fuchsia', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Fuchsia|', 'Địa điểm:|Làng khởi đầu|'), ' ở Fuchsia|', ' ở làng|'), 200) WHERE `id` = 1143 AND `mainsub` = 0;
-- 1144 C24.2: quai 71 chi moc o [14]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Fuchsia', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở Vùng Safari'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Fuchsia|', 'Địa điểm:|Vùng Safari|'), 200) WHERE `id` = 1144 AND `mainsub` = 0;
-- 1145 C24.3: quai 71 chi moc o [14]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Fuchsia', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở Vùng Safari'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Fuchsia|', 'Địa điểm:|Vùng Safari|'), 200) WHERE `id` = 1145 AND `mainsub` = 0;
-- 1148 C24.6: quai 50 chi moc o [15]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Fuchsia|', 'Địa điểm:|Cherrygrove|'), 200) WHERE `id` = 1148 AND `mainsub` = 0;
-- 1149 C25.1: NPC 9 chi o [0, 70, 78, 80, 88, 108]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cinnabar', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Cinnabar|', 'Địa điểm:|Làng khởi đầu|'), ' ở Cinnabar|', ' ở làng|'), 200) WHERE `id` = 1149 AND `mainsub` = 0;
-- 1150 C25.2: NPC 10 chi o [43, 58, 60, 106, 162]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cinnabar', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Bến tàu'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Cinnabar|', 'Địa điểm:|Bến tàu|'), ' ở Cinnabar|', ' ở Bến tàu|'), 200) WHERE `id` = 1150 AND `mainsub` = 0;
-- 1151 C25.3: quai 42 chi moc o [15]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Bến tàu', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở Cherrygrove'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Bến tàu|', 'Địa điểm:|Cherrygrove|'), 200) WHERE `id` = 1151 AND `mainsub` = 0;
-- 1152 C25.4: quai 68 chi moc o [14]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cinnabar', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Vùng Safari'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cinnabar|', 'Địa điểm:|Vùng Safari|'), 200) WHERE `id` = 1152 AND `mainsub` = 0;
-- 1155 C26.1: NPC 3 chi o [0, 47, 55, 59, 70, 74, 80, 84, 104]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cinnabar', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Cinnabar|', 'Địa điểm:|Làng khởi đầu|'), ' ở Cinnabar|', ' ở làng|'), 200) WHERE `id` = 1155 AND `mainsub` = 0;
-- 1156 C26.2: quai 42 chi moc o [15]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cinnabar', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Cherrygrove'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cinnabar|', 'Địa điểm:|Cherrygrove|'), 200) WHERE `id` = 1156 AND `mainsub` = 0;
-- 1157 C26.3: quai 57 chi moc o [15]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cinnabar', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Cherrygrove'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cinnabar|', 'Địa điểm:|Cherrygrove|'), 200) WHERE `id` = 1157 AND `mainsub` = 0;
-- 1158 C26.4: quai 57 chi moc o [15]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cinnabar', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Cherrygrove'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cinnabar|', 'Địa điểm:|Cherrygrove|'), 200) WHERE `id` = 1158 AND `mainsub` = 0;
-- 1160 C26.6: NPC 3 chi o [0, 47, 55, 59, 70, 74, 80, 84, 104]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cinnabar', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Cinnabar|', 'Địa điểm:|Làng khởi đầu|'), ' ở Cinnabar|', ' ở làng|'), 200) WHERE `id` = 1160 AND `mainsub` = 0;
-- 1161 C27.1: NPC 21 chi o [0, 44, 50, 51, 52, 62, 70, 80, 163, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cinnabar', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Cinnabar|', 'Địa điểm:|Làng khởi đầu|'), ' ở Cinnabar|', ' ở làng|'), 200) WHERE `id` = 1161 AND `mainsub` = 0;
-- 1165 C27.5: quai 59 chi moc o [113]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cinnabar', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Hang Tối'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cinnabar|', 'Địa điểm:|Hang Tối|'), 200) WHERE `id` = 1165 AND `mainsub` = 0;
-- 1166 C27.6: NPC 21 chi o [0, 44, 50, 51, 52, 62, 70, 80, 163, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cinnabar', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Cinnabar|', 'Địa điểm:|Làng khởi đầu|'), ' ở Cinnabar|', ' ở làng|'), 200) WHERE `id` = 1166 AND `mainsub` = 0;
-- 1167 C27.7: quai 47 chi moc o [113]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cinnabar|', 'Địa điểm:|Hang Tối|'), 200) WHERE `id` = 1167 AND `mainsub` = 0;
-- 1168 C28.1: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Viridian', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Viridian|', 'Địa điểm:|Làng khởi đầu|'), ' ở Viridian|', ' ở làng|'), 200) WHERE `id` = 1168 AND `mainsub` = 0;
-- 1169 C28.2: quai 41 chi moc o [114]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Viridian', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Hồ Phẫn Nộ'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Viridian|', 'Địa điểm:|Hồ Phẫn Nộ|'), 200) WHERE `id` = 1169 AND `mainsub` = 0;
-- 1170 C28.3: NPC 4 chi o [0, 70, 80, 201, 281]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Viridian', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Viridian|', 'Địa điểm:|Làng khởi đầu|'), ' ở Viridian|', ' ở làng|'), 200) WHERE `id` = 1170 AND `mainsub` = 0;
-- 1172 C28.5: quai 44 chi moc o [114]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Viridian|', 'Địa điểm:|Hồ Phẫn Nộ|'), 200) WHERE `id` = 1172 AND `mainsub` = 0;
-- 1173 C28.6: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Viridian', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Viridian|', 'Địa điểm:|Làng khởi đầu|'), ' ở Viridian|', ' ở làng|'), 200) WHERE `id` = 1173 AND `mainsub` = 0;
-- 1174 C29.1: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở New Bark', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|New Bark|', 'Địa điểm:|Làng khởi đầu|'), ' ở New Bark|', ' ở làng|'), 200) WHERE `id` = 1174 AND `mainsub` = 0;
-- 1175 C29.2: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở New Bark', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|New Bark|', 'Địa điểm:|Làng khởi đầu|'), ' ở New Bark|', ' ở làng|'), 200) WHERE `id` = 1175 AND `mainsub` = 0;
-- 1179 C29.6: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở New Bark', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|New Bark|', 'Địa điểm:|Làng khởi đầu|'), ' ở New Bark|', ' ở làng|'), 200) WHERE `id` = 1179 AND `mainsub` = 0;
-- 1180 C30.1: NPC 6 chi o [0, 53, 70, 79, 80, 89, 109]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cherrygrove', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 14), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Cherrygrove|', 'Địa điểm:|Làng khởi đầu|'), ' ở Cherrygrove|', ' ở làng|'), 200) WHERE `id` = 1180 AND `mainsub` = 0;
-- 1183 C30.4: quai 62 chi moc o [114]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cherrygrove', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 14), ' ở Hồ Phẫn Nộ'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cherrygrove|', 'Địa điểm:|Hồ Phẫn Nộ|'), 200) WHERE `id` = 1183 AND `mainsub` = 0;
-- 1184 C30.5: NPC 6 chi o [0, 53, 70, 79, 80, 89, 109]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cherrygrove', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 14), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Cherrygrove|', 'Địa điểm:|Làng khởi đầu|'), ' ở Cherrygrove|', ' ở làng|'), 200) WHERE `id` = 1184 AND `mainsub` = 0;
-- 1185 C30.6: quai 44 chi moc o [114]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cherrygrove', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 14), ' ở Hồ Phẫn Nộ'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cherrygrove|', 'Địa điểm:|Hồ Phẫn Nộ|'), 200) WHERE `id` = 1185 AND `mainsub` = 0;
-- 1186 C31.1: NPC 8 chi o [0, 51, 57, 60, 70, 80, 105, 160]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Violet', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Violet|', 'Địa điểm:|Làng khởi đầu|'), ' ở Violet|', ' ở làng|'), 200) WHERE `id` = 1186 AND `mainsub` = 0;
-- 1188 C31.3: quai 76 chi moc o [19]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Violet', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở Ecruteak'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Violet|', 'Địa điểm:|Ecruteak|'), 200) WHERE `id` = 1188 AND `mainsub` = 0;
-- 1190 C31.5: NPC 21 chi o [0, 44, 50, 51, 52, 62, 70, 80, 163, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Violet', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Violet|', 'Địa điểm:|Làng khởi đầu|'), ' ở Violet|', ' ở làng|'), 200) WHERE `id` = 1190 AND `mainsub` = 0;
-- 1191 C31.6: quai 65 chi moc o [19]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Violet|', 'Địa điểm:|Ecruteak|'), 200) WHERE `id` = 1191 AND `mainsub` = 0;
-- 1192 C32.1: NPC 21 chi o [0, 44, 50, 51, 52, 62, 70, 80, 163, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Violet', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Violet|', 'Địa điểm:|Làng khởi đầu|'), ' ở Violet|', ' ở làng|'), 200) WHERE `id` = 1192 AND `mainsub` = 0;
-- 1194 C32.3: quai 44 chi moc o [114]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Violet', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở Hồ Phẫn Nộ'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Violet|', 'Địa điểm:|Hồ Phẫn Nộ|'), 200) WHERE `id` = 1194 AND `mainsub` = 0;
-- 1197 C32.6: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Violet', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Violet|', 'Địa điểm:|Làng khởi đầu|'), ' ở Violet|', ' ở làng|'), 200) WHERE `id` = 1197 AND `mainsub` = 0;
-- 1198 C33.1: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Azalea', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Azalea|', 'Địa điểm:|Làng khởi đầu|'), ' ở Azalea|', ' ở làng|'), 200) WHERE `id` = 1198 AND `mainsub` = 0;
-- 1200 C33.3: quai 76 chi moc o [19]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Azalea', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở Ecruteak'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Azalea|', 'Địa điểm:|Ecruteak|'), 200) WHERE `id` = 1200 AND `mainsub` = 0;
-- 1201 C33.4: quai 65 chi moc o [19]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Azalea', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở Ecruteak'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Azalea|', 'Địa điểm:|Ecruteak|'), 200) WHERE `id` = 1201 AND `mainsub` = 0;
-- 1203 C33.6: NPC 25 chi o [201]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Azalea', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở Cao nguyên Indigo'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Azalea|', 'Địa điểm:|Cao nguyên Indigo|'), ' ở Azalea|', ' ở Cao nguyên Indigo|'), 200) WHERE `id` = 1203 AND `mainsub` = 0;
-- 1204 C33.7: quai 48 chi moc o [20]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Azalea|', 'Địa điểm:|Violet|'), 200) WHERE `id` = 1204 AND `mainsub` = 0;
-- 1205 C34.1: NPC 23 chi o [4, 51]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Rừng Ilex', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở Cerulean'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Rừng Ilex|', 'Địa điểm:|Cerulean|'), ' ở Rừng Ilex|', ' ở Cerulean|'), 200) WHERE `id` = 1205 AND `mainsub` = 0;
-- 1206 C34.2: quai 65 chi moc o [19]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Rừng Ilex', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở Ecruteak'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Rừng Ilex|', 'Địa điểm:|Ecruteak|'), 200) WHERE `id` = 1206 AND `mainsub` = 0;
-- 1207 C34.3: quai 48 chi moc o [20]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Rừng Ilex', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở Violet'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Rừng Ilex|', 'Địa điểm:|Violet|'), 200) WHERE `id` = 1207 AND `mainsub` = 0;
-- 1209 C34.5: quai 65 chi moc o [19]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Rừng Ilex', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở Ecruteak'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Rừng Ilex|', 'Địa điểm:|Ecruteak|'), 200) WHERE `id` = 1209 AND `mainsub` = 0;
-- 1210 C34.6: NPC 23 chi o [4, 51]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Rừng Ilex', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở Cerulean'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Rừng Ilex|', 'Địa điểm:|Cerulean|'), ' ở Rừng Ilex|', ' ở Cerulean|'), 200) WHERE `id` = 1210 AND `mainsub` = 0;
-- 1211 C35.1: NPC 8 chi o [0, 51, 57, 60, 70, 80, 105, 160]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Rừng Ilex', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Rừng Ilex|', 'Địa điểm:|Làng khởi đầu|'), ' ở Rừng Ilex|', ' ở làng|'), 200) WHERE `id` = 1211 AND `mainsub` = 0;
-- 1212 C35.2: quai 65 chi moc o [19]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Rừng Ilex', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở Ecruteak'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Rừng Ilex|', 'Địa điểm:|Ecruteak|'), 200) WHERE `id` = 1212 AND `mainsub` = 0;
-- 1215 C35.5: NPC 8 chi o [0, 51, 57, 60, 70, 80, 105, 160]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Rừng Ilex', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Rừng Ilex|', 'Địa điểm:|Làng khởi đầu|'), ' ở Rừng Ilex|', ' ở làng|'), 200) WHERE `id` = 1215 AND `mainsub` = 0;
-- 1216 C35.6: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Rừng Ilex', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Rừng Ilex|', 'Địa điểm:|Làng khởi đầu|'), ' ở Rừng Ilex|', ' ở làng|'), 200) WHERE `id` = 1216 AND `mainsub` = 0;
-- 1217 C36.1: NPC 22 chi o [0, 47, 49, 56, 62, 70, 80]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Goldenrod', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Goldenrod|', 'Địa điểm:|Làng khởi đầu|'), ' ở Goldenrod|', ' ở làng|'), 200) WHERE `id` = 1217 AND `mainsub` = 0;
-- 1220 C36.4: quai 48 chi moc o [20]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Goldenrod', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở Violet'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Goldenrod|', 'Địa điểm:|Violet|'), 200) WHERE `id` = 1220 AND `mainsub` = 0;
-- 1221 C36.5: NPC 16 chi o [94, 201, 271]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Goldenrod', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở Cửa hàng 5'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Goldenrod|', 'Địa điểm:|Cửa hàng 5|'), ' ở Goldenrod|', ' ở Cửa hàng 5|'), 200) WHERE `id` = 1221 AND `mainsub` = 0;
-- 1222 C36.6: quai 77 chi moc o [21]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Goldenrod|', 'Địa điểm:|Đồng cỏ phía tây|'), 200) WHERE `id` = 1222 AND `mainsub` = 0;
-- 1223 C36.7: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Goldenrod', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Goldenrod|', 'Địa điểm:|Làng khởi đầu|'), ' ở Goldenrod|', ' ở làng|'), 200) WHERE `id` = 1223 AND `mainsub` = 0;
-- 1224 C37.1: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Ecruteak', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Ecruteak|', 'Địa điểm:|Làng khởi đầu|'), ' ở Ecruteak|', ' ở làng|'), 200) WHERE `id` = 1224 AND `mainsub` = 0;
-- 1225 C37.2: quai 48 chi moc o [20]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Ecruteak|', 'Địa điểm:|Violet|'), 200) WHERE `id` = 1225 AND `mainsub` = 0;
-- 1228 C37.5: quai 60 chi moc o [20]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Ecruteak', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Violet'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Ecruteak|', 'Địa điểm:|Violet|'), 200) WHERE `id` = 1228 AND `mainsub` = 0;
-- 1229 C37.6: quai 81 chi moc o [21]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Ecruteak|', 'Địa điểm:|Đồng cỏ phía tây|'), 200) WHERE `id` = 1229 AND `mainsub` = 0;
-- 1230 C38.1: NPC 8 chi o [0, 51, 57, 60, 70, 80, 105, 160]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Ecruteak', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Ecruteak|', 'Địa điểm:|Làng khởi đầu|'), ' ở Ecruteak|', ' ở làng|'), 200) WHERE `id` = 1230 AND `mainsub` = 0;
-- 1232 C38.3: quai 60 chi moc o [20]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Ecruteak', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Violet'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Ecruteak|', 'Địa điểm:|Violet|'), 200) WHERE `id` = 1232 AND `mainsub` = 0;
-- 1233 C38.4: quai 77 chi moc o [21]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Ecruteak', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Đồng cỏ phía tây'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Ecruteak|', 'Địa điểm:|Đồng cỏ phía tây|'), 200) WHERE `id` = 1233 AND `mainsub` = 0;
-- 1235 C38.6: NPC 8 chi o [0, 51, 57, 60, 70, 80, 105, 160]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Ecruteak', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Ecruteak|', 'Địa điểm:|Làng khởi đầu|'), ' ở Ecruteak|', ' ở làng|'), 200) WHERE `id` = 1235 AND `mainsub` = 0;
-- 1236 C39.1: NPC 2 chi o [0, 45, 70, 73, 80, 83, 103]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Olivine', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Olivine|', 'Địa điểm:|Làng khởi đầu|'), ' ở Olivine|', ' ở làng|'), 200) WHERE `id` = 1236 AND `mainsub` = 0;
-- 1239 C39.4: quai 81 chi moc o [21]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Olivine', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở Đồng cỏ phía tây'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Olivine|', 'Địa điểm:|Đồng cỏ phía tây|'), 200) WHERE `id` = 1239 AND `mainsub` = 0;
-- 1241 C39.6: quai 53 chi moc o [22]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Olivine|', 'Địa điểm:|Tàu S.S. Anne|'), 200) WHERE `id` = 1241 AND `mainsub` = 0;
-- 1244 C40.3: quai 81 chi moc o [21]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cianwood', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Đồng cỏ phía tây'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cianwood|', 'Địa điểm:|Đồng cỏ phía tây|'), 200) WHERE `id` = 1244 AND `mainsub` = 0;
-- 1245 C40.4: quai 51 chi moc o [23]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cianwood|', 'Địa điểm:|Azalea|'), 200) WHERE `id` = 1245 AND `mainsub` = 0;
-- 1248 C41.1: NPC 6 chi o [0, 53, 70, 79, 80, 89, 109]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Mahogany', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Mahogany|', 'Địa điểm:|Làng khởi đầu|'), ' ở Mahogany|', ' ở làng|'), 200) WHERE `id` = 1248 AND `mainsub` = 0;
-- 1249 C41.2: NPC 1 chi o [0, 45, 47, 49, 54, 56, 62, 70, 72, 80, 82, 102]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Mahogany', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Mahogany|', 'Địa điểm:|Làng khởi đầu|'), ' ở Mahogany|', ' ở làng|'), 200) WHERE `id` = 1249 AND `mainsub` = 0;
-- 1251 C41.4: quai 54 chi moc o [22]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Mahogany', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Tàu S.S. Anne'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Mahogany|', 'Địa điểm:|Tàu S.S. Anne|'), 200) WHERE `id` = 1251 AND `mainsub` = 0;
-- 1252 C41.5: quai 54 chi moc o [22]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Mahogany', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Tàu S.S. Anne'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Mahogany|', 'Địa điểm:|Tàu S.S. Anne|'), 200) WHERE `id` = 1252 AND `mainsub` = 0;
-- 1253 C41.6: NPC 6 chi o [0, 53, 70, 79, 80, 89, 109]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Mahogany', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Mahogany|', 'Địa điểm:|Làng khởi đầu|'), ' ở Mahogany|', ' ở làng|'), 200) WHERE `id` = 1253 AND `mainsub` = 0;
-- 1254 C42.1: NPC 21 chi o [0, 44, 50, 51, 52, 62, 70, 80, 163, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Hồ Phẫn Nộ', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 13), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Hồ Phẫn Nộ|', 'Địa điểm:|Làng khởi đầu|'), ' ở Hồ Phẫn Nộ|', ' ở làng|'), 200) WHERE `id` = 1254 AND `mainsub` = 0;
-- 1256 C42.3: quai 49 chi moc o [23]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Hồ Phẫn Nộ', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 13), ' ở Azalea'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Hồ Phẫn Nộ|', 'Địa điểm:|Azalea|'), 200) WHERE `id` = 1256 AND `mainsub` = 0;
-- 1257 C42.4: quai 49 chi moc o [23]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Hồ Phẫn Nộ', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 13), ' ở Azalea'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Hồ Phẫn Nộ|', 'Địa điểm:|Azalea|'), 200) WHERE `id` = 1257 AND `mainsub` = 0;
-- 1258 C42.5: quai 53 chi moc o [22]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Hồ Phẫn Nộ', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 13), ' ở Tàu S.S. Anne'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Hồ Phẫn Nộ|', 'Địa điểm:|Tàu S.S. Anne|'), 200) WHERE `id` = 1258 AND `mainsub` = 0;
-- 1259 C42.6: NPC 21 chi o [0, 44, 50, 51, 52, 62, 70, 80, 163, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Hồ Phẫn Nộ', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 13), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Hồ Phẫn Nộ|', 'Địa điểm:|Làng khởi đầu|'), ' ở Hồ Phẫn Nộ|', ' ở làng|'), 200) WHERE `id` = 1259 AND `mainsub` = 0;
-- 1260 C43.1: NPC 9 chi o [0, 70, 78, 80, 88, 108]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Hang Tối', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Hang Tối|', 'Địa điểm:|Làng khởi đầu|'), ' ở Hang Tối|', ' ở làng|'), 200) WHERE `id` = 1260 AND `mainsub` = 0;
-- 1261 C43.2: quai 49 chi moc o [23]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Hang Tối', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Azalea'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Hang Tối|', 'Địa điểm:|Azalea|'), 200) WHERE `id` = 1261 AND `mainsub` = 0;
-- 1263 C43.4: quai 56 chi moc o [24]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Hang Tối', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Olivine'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Hang Tối|', 'Địa điểm:|Olivine|'), 200) WHERE `id` = 1263 AND `mainsub` = 0;
-- 1265 C43.6: quai 51 chi moc o [23]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Hang Tối', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Azalea'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Hang Tối|', 'Địa điểm:|Azalea|'), 200) WHERE `id` = 1265 AND `mainsub` = 0;
-- 1266 C44.1: NPC 3 chi o [0, 47, 55, 59, 70, 74, 80, 84, 104]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Mahogany', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Mahogany|', 'Địa điểm:|Làng khởi đầu|'), ' ở Mahogany|', ' ở làng|'), 200) WHERE `id` = 1266 AND `mainsub` = 0;
-- 1267 C44.2: quai 51 chi moc o [23]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Mahogany', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Azalea'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Mahogany|', 'Địa điểm:|Azalea|'), 200) WHERE `id` = 1267 AND `mainsub` = 0;
-- 1269 C44.4: quai 58 chi moc o [24]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Mahogany', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Olivine'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Mahogany|', 'Địa điểm:|Olivine|'), 200) WHERE `id` = 1269 AND `mainsub` = 0;
-- 1270 C44.5: quai 56 chi moc o [24]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Mahogany', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Olivine'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Mahogany|', 'Địa điểm:|Olivine|'), 200) WHERE `id` = 1270 AND `mainsub` = 0;
-- 1271 C44.6: quai 118 chi moc o [26]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Mahogany|', 'Địa điểm:|Rừng Cổ|'), 200) WHERE `id` = 1271 AND `mainsub` = 0;
-- 1275 C45.4: quai 51 chi moc o [23]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Blackthorn', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 13), ' ở Azalea'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Blackthorn|', 'Địa điểm:|Azalea|'), 200) WHERE `id` = 1275 AND `mainsub` = 0;
-- 1278 C46.1: NPC 21 chi o [0, 44, 50, 51, 52, 62, 70, 80, 163, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Hang Salamence', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 17), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Hang Salamence|', 'Địa điểm:|Làng khởi đầu|'), ' ở Hang Salamence|', ' ở làng|'), 200) WHERE `id` = 1278 AND `mainsub` = 0;
-- 1280 C46.3: quai 118 chi moc o [26]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Hang Salamence', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 17), ' ở Rừng Cổ'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Hang Salamence|', 'Địa điểm:|Rừng Cổ|'), 200) WHERE `id` = 1280 AND `mainsub` = 0;
-- 1282 C46.5: quai 118 chi moc o [26]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Hang Salamence', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 17), ' ở Rừng Cổ'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Hang Salamence|', 'Địa điểm:|Rừng Cổ|'), 200) WHERE `id` = 1282 AND `mainsub` = 0;
-- 1283 C46.6: NPC 21 chi o [0, 44, 50, 51, 52, 62, 70, 80, 163, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Hang Salamence', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 17), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Hang Salamence|', 'Địa điểm:|Làng khởi đầu|'), ' ở Hang Salamence|', ' ở làng|'), 200) WHERE `id` = 1283 AND `mainsub` = 0;
-- 1284 C47.1: NPC 8 chi o [0, 51, 57, 60, 70, 80, 105, 160]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Tổ Zapdos', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Tổ Zapdos|', 'Địa điểm:|Làng khởi đầu|'), ' ở Tổ Zapdos|', ' ở làng|'), 200) WHERE `id` = 1284 AND `mainsub` = 0;
-- 1287 C47.4: quai 118 chi moc o [26]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Tổ Zapdos', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở Rừng Cổ'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Tổ Zapdos|', 'Địa điểm:|Rừng Cổ|'), 200) WHERE `id` = 1287 AND `mainsub` = 0;
-- 1289 C47.6: NPC 8 chi o [0, 51, 57, 60, 70, 80, 105, 160]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Tổ Zapdos', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Tổ Zapdos|', 'Địa điểm:|Làng khởi đầu|'), ' ở Tổ Zapdos|', ' ở làng|'), 200) WHERE `id` = 1289 AND `mainsub` = 0;
-- 1293 C48.4: quai 136 chi moc o [28]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Blackthorn', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 13), ' ở Mahogany'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Blackthorn|', 'Địa điểm:|Mahogany|'), 200) WHERE `id` = 1293 AND `mainsub` = 0;
-- 1295 C48.6: quai 138 chi moc o [28]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Blackthorn|', 'Địa điểm:|Mahogany|'), 200) WHERE `id` = 1295 AND `mainsub` = 0;
-- 1296 C49.1: NPC 21 chi o [0, 44, 50, 51, 52, 62, 70, 80, 163, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Đường Victory', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Đường Victory|', 'Địa điểm:|Làng khởi đầu|'), ' ở Đường Victory|', ' ở làng|'), 200) WHERE `id` = 1296 AND `mainsub` = 0;
UPDATE `data_quest_lau_dai` SET `npc_giao` = 3, `ten_npc` = 'Bronzong' WHERE `id_so` = 1297;
-- 1297 C49.2: loi dau cua Bronzong, giao boi Blaziken
UPDATE `data_quest_moi` SET `npcrcv` = 3, `infogift` = LEFT(REPLACE(`infogift`, 'NPC:|Blaziken|', 'NPC:|Bronzong|'), 200) WHERE `id` = 1297 AND `mainsub` = 0;
-- 1300 C49.5: quai 138 chi moc o [28]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Đường Victory', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở Mahogany'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Đường Victory|', 'Địa điểm:|Mahogany|'), 200) WHERE `id` = 1300 AND `mainsub` = 0;
-- 1302 C50.1: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cao nguyên Indigo', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 20), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Cao nguyên Indigo|', 'Địa điểm:|Làng khởi đầu|'), ' ở Cao nguyên Indigo|', ' ở làng|'), 200) WHERE `id` = 1302 AND `mainsub` = 0;
-- 1304 C50.3: quai 138 chi moc o [28]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cao nguyên Indigo', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 20), ' ở Mahogany'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cao nguyên Indigo|', 'Địa điểm:|Mahogany|'), 200) WHERE `id` = 1304 AND `mainsub` = 0;
-- 1305 C50.4: quai 138 chi moc o [28]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cao nguyên Indigo', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 20), ' ở Mahogany'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cao nguyên Indigo|', 'Địa điểm:|Mahogany|'), 200) WHERE `id` = 1305 AND `mainsub` = 0;
-- 1306 C50.5: quai 137 chi moc o [28]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cao nguyên Indigo', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 20), ' ở Mahogany'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cao nguyên Indigo|', 'Địa điểm:|Mahogany|'), 200) WHERE `id` = 1306 AND `mainsub` = 0;
-- 1307 C50.6: quai 136 chi moc o [28]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cao nguyên Indigo', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 20), ' ở Mahogany'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cao nguyên Indigo|', 'Địa điểm:|Mahogany|'), 200) WHERE `id` = 1307 AND `mainsub` = 0;
-- 1309 C51.1: NPC 8 chi o [0, 51, 57, 60, 70, 80, 105, 160]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Núi Silver', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 13), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Núi Silver|', 'Địa điểm:|Làng khởi đầu|'), ' ở Núi Silver|', ' ở làng|'), 200) WHERE `id` = 1309 AND `mainsub` = 0;
-- 1311 C51.3: quai 138 chi moc o [28]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Núi Silver', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 13), ' ở Mahogany'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Núi Silver|', 'Địa điểm:|Mahogany|'), 200) WHERE `id` = 1311 AND `mainsub` = 0;
-- 1314 C51.6: quai 138 chi moc o [28]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Núi Silver', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 13), ' ở Mahogany'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Núi Silver|', 'Địa điểm:|Mahogany|'), 200) WHERE `id` = 1314 AND `mainsub` = 0;
-- 1316 C52.1: NPC 21 chi o [0, 44, 50, 51, 52, 62, 70, 80, 163, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Núi Silver', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 13), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Núi Silver|', 'Địa điểm:|Làng khởi đầu|'), ' ở Núi Silver|', ' ở làng|'), 200) WHERE `id` = 1316 AND `mainsub` = 0;
-- 1317 C52.2: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Núi Silver', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 13), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Núi Silver|', 'Địa điểm:|Làng khởi đầu|'), ' ở Núi Silver|', ' ở làng|'), 200) WHERE `id` = 1317 AND `mainsub` = 0;
-- 1319 C52.4: quai 136 chi moc o [28]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Núi Silver', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 13), ' ở Mahogany'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Núi Silver|', 'Địa điểm:|Mahogany|'), 200) WHERE `id` = 1319 AND `mainsub` = 0;
-- 1320 C52.5: quai 138 chi moc o [28]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Núi Silver|', 'Địa điểm:|Mahogany|'), 200) WHERE `id` = 1320 AND `mainsub` = 0;
-- 1322 C52.7: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Núi Silver', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 13), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Núi Silver|', 'Địa điểm:|Làng khởi đầu|'), ' ở Núi Silver|', ' ở làng|'), 200) WHERE `id` = 1322 AND `mainsub` = 0;
-- /SUA CHUOI CHINH ---------------------------------------------------------------------------------------------------
-- >>> them_the_dat_ten_pet.sql
-- THE DAT TEN POKEMON (0.9.6, nhanh ten-pet) - migration cho CSDL dang chay. Chay lai bao nhieu lan cung duoc
-- (CREATE TABLE IF NOT EXISTS, INSERT IGNORE giu gia tri admin da sua, UPDATE dat lai dung hinh/ten vat pham).
-- Khong dung "INSERT ... SELECT ... ON DUPLICATE KEY UPDATE col=col" (hong tren MariaDB 10.3).
-- Cung khoi nay nam trong sql/htbb_day_du.sql. Chay SAU sql/pokemon_vat_pham_lau_dai.sql neu chay lai tep do
-- (tep do DROP data_vat_pham_lau_dai / data_cua_hang_lau_dai). Thiet ke: docs/thiet-ke-game.md muc 4.8.
--   (nay la mot phan cua sql/nang_cap.sql phan 11)

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
('the_dat_ten_pet','Thẻ đặt tên Pokémon','tien_ich','dat_ten_pet',100,0,0,0,0,0,1,24);
-- >>> pokemon_npc_sprite.sql
-- Migration (chay lai duoc): anh NPC = Pokemon (P2 tai nguyen). Chay SAU sql/pokemon_ten_hien.sql.
--   mysql htbb_db < sql/pokemon_npc_sprite.sql
-- data_npc.dex  = so Pokedex ve NPC tinh (client NPC.POKE_DEX_THEO_TYPE, Unity NPC.cs, du_lieu/bang_npc.csv).
-- data_npc.sprite = khoa tai nguyen client: 'pk:<dex 4 so>' (client_jar/res/pk, Unity Resources/poke).
--   Anh du phong npc.sh / npc.bytes cung la Pokemon do (tools/pokemon/sinh_npc_sh.py).
-- data_npc_su_kien_sprite = NPC su kien (lenh -50) theo idImg: dex Pokemon HOAC vat_the (pk/vt_npc<id>.png).
-- test/KiemNpcSprite doi chieu file nay voi jar, Unity va du_lieu/*.csv. Khong dung INSERT...SELECT...ON DUPLICATE.

ALTER TABLE `data_npc` ADD COLUMN IF NOT EXISTS `dex` int NOT NULL DEFAULT 0;
ALTER TABLE `data_npc` ADD COLUMN IF NOT EXISTS `sprite` varchar(32) NOT NULL DEFAULT '';

UPDATE `data_npc` SET `dex`=113, `sprite`='pk:0113' WHERE `type`=0;
UPDATE `data_npc` SET `dex`=352, `sprite`='pk:0352' WHERE `type`=1;
UPDATE `data_npc` SET `dex`=68, `sprite`='pk:0068' WHERE `type`=2;
UPDATE `data_npc` SET `dex`=437, `sprite`='pk:0437' WHERE `type`=3;
UPDATE `data_npc` SET `dex`=59, `sprite`='pk:0059' WHERE `type`=4;
UPDATE `data_npc` SET `dex`=178, `sprite`='pk:0178' WHERE `type`=5;
UPDATE `data_npc` SET `dex`=53, `sprite`='pk:0053' WHERE `type`=6;
UPDATE `data_npc` SET `dex`=18, `sprite`='pk:0018' WHERE `type`=7;
UPDATE `data_npc` SET `dex`=143, `sprite`='pk:0143' WHERE `type`=8;
UPDATE `data_npc` SET `dex`=115, `sprite`='pk:0115' WHERE `type`=9;
UPDATE `data_npc` SET `dex`=131, `sprite`='pk:0131' WHERE `type`=10;
UPDATE `data_npc` SET `dex`=76, `sprite`='pk:0076' WHERE `type`=11;
UPDATE `data_npc` SET `dex`=76, `sprite`='pk:0076' WHERE `type`=12;
UPDATE `data_npc` SET `dex`=76, `sprite`='pk:0076' WHERE `type`=13;
UPDATE `data_npc` SET `dex`=76, `sprite`='pk:0076' WHERE `type`=14;
UPDATE `data_npc` SET `dex`=76, `sprite`='pk:0076' WHERE `type`=15;
UPDATE `data_npc` SET `dex`=128, `sprite`='pk:0128' WHERE `type`=16;
UPDATE `data_npc` SET `dex`=128, `sprite`='pk:0128' WHERE `type`=17;
UPDATE `data_npc` SET `dex`=128, `sprite`='pk:0128' WHERE `type`=18;
UPDATE `data_npc` SET `dex`=128, `sprite`='pk:0128' WHERE `type`=19;
UPDATE `data_npc` SET `dex`=128, `sprite`='pk:0128' WHERE `type`=20;
UPDATE `data_npc` SET `dex`=257, `sprite`='pk:0257' WHERE `type`=21;
UPDATE `data_npc` SET `dex`=52, `sprite`='pk:0052' WHERE `type`=22;
UPDATE `data_npc` SET `dex`=324, `sprite`='pk:0324' WHERE `type`=23;
UPDATE `data_npc` SET `dex`=302, `sprite`='pk:0302' WHERE `type`=24;
UPDATE `data_npc` SET `dex`=214, `sprite`='pk:0214' WHERE `type`=25;
UPDATE `data_npc` SET `dex`=352, `sprite`='pk:0352' WHERE `type`=26;
UPDATE `data_npc` SET `dex`=437, `sprite`='pk:0437' WHERE `type`=27;
UPDATE `data_npc` SET `dex`=83, `sprite`='pk:0083' WHERE `type`=28;
UPDATE `data_npc` SET `dex`=53, `sprite`='pk:0053' WHERE `type`=29;
UPDATE `data_npc` SET `dex`=115, `sprite`='pk:0115' WHERE `type`=30;
UPDATE `data_npc` SET `dex`=52, `sprite`='pk:0052' WHERE `type`=31;

CREATE TABLE IF NOT EXISTS `data_npc_su_kien_sprite` (
  `id_img` smallint NOT NULL,
  `dex` smallint NOT NULL DEFAULT 0,
  `vat_the` varchar(32) NOT NULL DEFAULT '',
  `ten` varchar(64) NOT NULL DEFAULT '',
  PRIMARY KEY (`id_img`)
) DEFAULT CHARSET=utf8;
REPLACE INTO `data_npc_su_kien_sprite` (`id_img`,`dex`,`vat_the`,`ten`) VALUES
(13,0,'vt_npc13','Cổng dịch chuyển'),
(14,52,'','Meowth'),
(15,0,'vt_npc15','Biển khu'),
(20,282,'','Gardevoir'),
(21,667,'','Litleo'),
(23,460,'','Abomasnow'),
(24,225,'','Delibird'),
(25,0,'vt_npc25','Nồi bánh chưng'),
(26,0,'vt_npc26','Nồi bánh tét'),
(27,448,'','Lucario'),
(28,214,'','Heracross'),
(29,185,'','Sudowoodo'),
(30,185,'','Sudowoodo');
-- >>> pokemon_thu_pokemon.sql
-- SINH TU tools/pokemon/sinh_bang_thu.py - KHONG SUA TAY (sua bang trong tool roi chay lai).
-- P2: anh xa thu cuoi / pet / skin linh thu / hoa hinh -> Pokemon (dex PMDCollab, shiny).
-- loai: thu_cuoi (id = data.Animal.idImg), pet_item (id = data_item.id), pet_cu (id = PetTemplate.idImg),
--       skin (id = real.SkinThuCuoi 0..5), hoa_hinh_pet (id = cap 1..5), hoa_hinh_thu (id 0).
-- Chay lai bao nhieu lan cung duoc (CREATE IF NOT EXISTS + REPLACE INTO, khong INSERT...SELECT).
-- SO_DONG: 95 (test KiemThuPetSql dem lai)
CREATE TABLE IF NOT EXISTS `data_thu_pokemon` (
  `loai` varchar(16) NOT NULL,
  `id` int(11) NOT NULL,
  `dex` smallint(6) NOT NULL,
  `shiny` tinyint(1) NOT NULL DEFAULT 0,
  `ten` varchar(64) NOT NULL,
  PRIMARY KEY (`loai`, `id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('hoa_hinh_pet', 1, 58, 0, 'Growlithe');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('hoa_hinh_pet', 2, 58, 0, 'Growlithe');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('hoa_hinh_pet', 3, 58, 0, 'Growlithe');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('hoa_hinh_pet', 4, 59, 0, 'Arcanine');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('hoa_hinh_pet', 5, 244, 0, 'Entei');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('hoa_hinh_thu', 0, 244, 0, 'Entei');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 0, 607, 0, 'Litwick');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 1, 276, 0, 'Taillow');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 2, 165, 0, 'Ledyba');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 3, 41, 0, 'Zubat');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 4, 370, 0, 'Luvdisc');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 5, 270, 0, 'Lotad');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 6, 120, 0, 'Staryu');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 7, 615, 0, 'Cryogonal');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 8, 77, 0, 'Ponyta');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 9, 27, 0, 'Sandshrew');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 10, 417, 0, 'Pachirisu');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 11, 29, 0, 'Nidoran♀');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 12, 129, 0, 'Magikarp');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 13, 393, 0, 'Piplup');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 14, 179, 0, 'Mareep');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 15, 385, 0, 'Jirachi');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 16, 146, 0, 'Moltres');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 17, 144, 0, 'Articuno');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 18, 121, 0, 'Starmie');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 19, 337, 0, 'Lunatone');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 20, 190, 0, 'Aipom');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 681, 77, 0, 'Ponyta lửa');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 682, 78, 0, 'Rapidash lửa');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 683, 78, 1, 'Rapidash lửa Shiny');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 684, 163, 0, 'Trứng Hoothoot');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 685, 147, 0, 'Trứng Dratini');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 686, 173, 0, 'Trứng Cleffa');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 687, 41, 0, 'Trứng Zubat');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 688, 627, 0, 'Trứng Rufflet');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 689, 163, 0, 'Hoothoot cấp 1');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 690, 163, 0, 'Hoothoot cấp 2');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 691, 164, 0, 'Noctowl cấp 3');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 692, 164, 0, 'Noctowl cấp 4');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 693, 164, 1, 'Noctowl Shiny cấp 5');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 694, 147, 0, 'Dratini cấp 1');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 695, 147, 0, 'Dratini cấp 2');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 696, 148, 0, 'Dragonair cấp 3');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 697, 148, 0, 'Dragonair cấp 4');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 698, 149, 0, 'Dragonite cấp 5');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 699, 173, 0, 'Cleffa cấp 1');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 700, 35, 0, 'Clefairy cấp 2');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 701, 35, 0, 'Clefairy cấp 3');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 702, 36, 0, 'Clefable cấp 4');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 703, 36, 1, 'Clefable Shiny cấp 5');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 704, 41, 0, 'Zubat cấp 1');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 705, 41, 0, 'Zubat cấp 2');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 706, 42, 0, 'Golbat cấp 3');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 707, 42, 0, 'Golbat cấp 4');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 708, 169, 0, 'Crobat cấp 5');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 709, 627, 0, 'Rufflet cấp 1');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 710, 627, 0, 'Rufflet cấp 2');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 711, 628, 0, 'Braviary cấp 3');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 712, 628, 0, 'Braviary cấp 4');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 713, 628, 1, 'Braviary Shiny cấp 5');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 727, 417, 0, 'Pachirisu');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 728, 225, 0, 'Delibird');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 790, 100, 1, 'Voltorb vàng');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 802, 403, 1, 'Shinx vàng');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 803, 667, 0, 'Litleo');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 804, 58, 0, 'Growlithe');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 832, 585, 0, 'Deerling');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 873, 399, 0, 'Bidoof');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('skin', 0, 879, 0, 'Copperajah');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('skin', 1, 675, 0, 'Pangoro');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('skin', 2, 716, 0, 'Xerneas');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('skin', 3, 323, 0, 'Camerupt');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('skin', 4, 745, 0, 'Lycanroc');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('skin', 5, 483, 0, 'Dialga');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 0, 78, 0, 'Rapidash');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 1, 59, 0, 'Arcanine');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 2, 128, 0, 'Tauros');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 3, 262, 0, 'Mightyena');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 4, 18, 0, 'Pidgeot');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 5, 146, 0, 'Moltres');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 6, 428, 1, 'Lopunny Shiny');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 7, 234, 0, 'Stantler');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 8, 144, 0, 'Articuno');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 9, 831, 0, 'Wooloo');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 10, 245, 0, 'Suicune');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 11, 221, 0, 'Piloswine');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 12, 897, 0, 'Spectrier');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 13, 149, 0, 'Dragonite');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 14, 250, 0, 'Ho-Oh');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 15, 357, 0, 'Tropius');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 16, 227, 0, 'Skarmory');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 17, 330, 0, 'Flygon');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 18, 473, 0, 'Mamoswine');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 19, 244, 0, 'Entei');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 20, 896, 0, 'Glastrier');
