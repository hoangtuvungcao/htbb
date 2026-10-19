-- NANG CAP CSDL dang chay (htbb_db) len schema + du lieu thiet ke hien tai. CHAY LAI DUOC.
-- SINH RA boi tools/pokemon/sinh_nang_cap_csdl.py - KHONG SUA TAY. Nguon: du_lieu/sql_nguon/.
-- Chay:  sudo bash /opt/htbb/vps/nang_cap_csdl.sh   (sao luu -> nap tep nay -> kiem)
--    hoac:  mysql -u root htbb_db < vps/nang_cap_csdl.sql
-- CSDL moi khong can tep nay: sql/htbb_day_du.sql da gom san.
-- Bang nguoi choi chi duoc THEM bang / THEM cot / NOI cot; khong DROP / DELETE / UPDATE du lieu nguoi choi.
SET NAMES utf8mb4;

-- ======== 1. Schema (khong pha, chay lai duoc)

-- du_lieu/sql_nguon/them_bang_2buoc.sql
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

-- du_lieu/sql_nguon/them_bang_cam_may.sql
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

-- du_lieu/sql_nguon/them_bang_dieu_khien_server.sql
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

-- du_lieu/sql_nguon/them_bang_su_kien.sql
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

-- du_lieu/sql_nguon/them_bang_tob_log_online.sql
CREATE TABLE IF NOT EXISTS `tob_log_online` (
  `id`        int(10) unsigned NOT NULL AUTO_INCREMENT,
  `thoi_gian` datetime         NOT NULL                COMMENT 'luc lay mau',
  `ket_noi`   int(11)          NOT NULL DEFAULT 0      COMMENT 'so socket dang mo',
  `nhan_vat`  int(11)          NOT NULL DEFAULT 0      COMMENT 'so nhan vat that trong game',
  PRIMARY KEY (`id`),
  KEY `idx_thoi_gian` (`thoi_gian`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- du_lieu/sql_nguon/them_bang_tob_top_thuong.sql
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

-- du_lieu/sql_nguon/them_bang_web_admin_log.sql
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

-- du_lieu/sql_nguon/them_bang_web_chan_spam.sql
CREATE TABLE IF NOT EXISTS `web_chan_spam` (
  `id`        bigint(20)  NOT NULL AUTO_INCREMENT,
  `ip`        varchar(45) NOT NULL DEFAULT ''  COMMENT 'IPv6 dai nhat 45 ky tu',
  `hanh_dong` varchar(30) NOT NULL DEFAULT ''  COMMENT 'dang_ky, tao_don_nap...',
  `thoi_gian` datetime    NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_dem` (`ip`, `hanh_dong`, `thoi_gian`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- du_lieu/sql_nguon/them_bang_web_lenh_nhan_vat.sql
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

-- du_lieu/sql_nguon/them_bang_tob_client_dang_nhap.sql
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

-- du_lieu/sql_nguon/them_bang_chong_clone.sql
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

-- du_lieu/sql_nguon/them_cot_kho_xu_luong.sql
ALTER TABLE `tob_char` ADD COLUMN IF NOT EXISTS `kho_xu`         BIGINT(20) NOT NULL DEFAULT 0 COMMENT 'Kho xu o NPC Tong Quan';
ALTER TABLE `tob_char` ADD COLUMN IF NOT EXISTS `kho_luong`      INT(11)    NOT NULL DEFAULT 0 COMMENT 'Kho luong mo o NPC Tong Quan';
ALTER TABLE `tob_char` ADD COLUMN IF NOT EXISTS `kho_luong_khoa` INT(11)    NOT NULL DEFAULT 0 COMMENT 'Kho luong khoa o NPC Tong Quan';

-- du_lieu/sql_nguon/them_cot_pokemon.sql
ALTER TABLE `tob_char` ADD COLUMN IF NOT EXISTS `dex`   SMALLINT NOT NULL DEFAULT 0 COMMENT 'Pokemon: so Pokedex, 0 = suy tu class+cap',
                       ADD COLUMN IF NOT EXISTS `shiny` TINYINT  NOT NULL DEFAULT 0 COMMENT 'Pokemon: 1 = shiny';
ALTER TABLE `data_monster` ADD COLUMN IF NOT EXISTS `dex`   SMALLINT NOT NULL DEFAULT 0  COMMENT 'Pokemon: so Pokedex cua template',
                           ADD COLUMN IF NOT EXISTS `shiny` TINYINT  NOT NULL DEFAULT 0  COMMENT 'Pokemon: 1 = shiny',
                           ADD COLUMN IF NOT EXISTS `he2`   TINYINT  NOT NULL DEFAULT -1 COMMENT 'Pokemon: he thu hai, -1 = khong co';

-- du_lieu/sql_nguon/them_bang_pokedex.sql
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

-- du_lieu/sql_nguon/them_bang_lau_dai.sql
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

-- du_lieu/sql_nguon/sua_cot_itemquest.sql
ALTER TABLE `tob_char_quest` MODIFY `itemquest` varchar(1000) DEFAULT NULL;

-- ======== 2. Du lieu thiet ke may chu doc luc khoi dong (P1): bang sinh, DROP + CREATE + INSERT

-- data_cap_do_lau_dai (du_lieu/sql_nguon/pokemon_lau_dai.sql, tools/pokemon/sinh_sql_lau_dai.py)
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

-- data_nang_cap_lau_dai (du_lieu/sql_nguon/pokemon_lau_dai.sql, tools/pokemon/sinh_sql_lau_dai.py)
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

-- data_kinh_te_lau_dai (du_lieu/sql_nguon/pokemon_lau_dai.sql, tools/pokemon/sinh_sql_lau_dai.py)
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

-- data_su_kien_nam (du_lieu/sql_nguon/pokemon_lau_dai.sql, tools/pokemon/sinh_sql_lau_dai.py)
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

-- data_hst_ho_cau (du_lieu/sql_nguon/pokemon_he_sinh_thai.sql, tools/pokemon/sinh_sql_he_sinh_thai.py)
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

-- data_noi_dung_trang_bi (du_lieu/sql_nguon/pokemon_noi_dung.sql, tools/pokemon/sinh_noi_dung.py)
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

-- cau_hinh_danh_muc (du_lieu/sql_nguon/pokemon_cau_hinh_trung_tam.sql, tools/cauhinh/sinh_cau_hinh.py)
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
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.dangKyTrongGame', 'bao_tri', 'Đăng ký trong game', '0 = nút Đăng ký trong game báo ''đăng ký trên web''.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.cmd.DangKyTrongGame', 72, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.dangKyTuKichHoat', 'bao_tri', 'Tự kích hoạt tài khoản đăng ký trong game', 'web = theo cấu hình web (web.autoActivate).', 'chon', 'web', '{"luaChon": ["web", "1", "0"], "luaChonTen": ["Theo web", "Lu\\u00f4n k\\u00edch ho\\u1ea1t", "Kh\\u00f4ng k\\u00edch ho\\u1ea1t"]}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.cmd.DangKyTrongGame', 73, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.dangKyTreMs', 'dang_ky', 'Trễ phản hồi đăng ký (ms)', 'Làm chậm dò tài khoản.', 'so', '800', '{"min": 0, "max": 5000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.cmd.DangKyTrongGame', 74, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.dangKyCsdlTaiKhoan', 'dang_ky', 'CSDL tài khoản', 'Tên database chứa team_user.', 'chuoi', 'account', '{"mau": "^[A-Za-z0-9_]{1,64}$"}', 'khoi_dong_lai', 0, 0, 0, 'cau_hinh', 'server', 'real.cmd.DangKyTrongGame', 75, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('dang_ky.web.bat', 'bao_tri', 'Đăng ký trên web', '0 = trang Đăng ký từ chối tài khoản mới.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'web', 'HTBB-Web api/auth/register.post.ts', 76, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.maxCharPerAccount', 'bao_tri', 'Số Pokémon tối đa / tài khoản', 'Client hỗ trợ tối đa 3.', 'so', '3', '{"min": 1, "max": 3}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'data.Database.MAX_CHAR_PER_ACCOUNT', 77, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.suKienTuKhoiDongLai', 'su_kien', 'Tự khởi động lại khi đổi sự kiện', '1 = lệnh bật/tắt sự kiện có hẹn sẽ tự khởi động lại máy chủ.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.LichSuKien', 78, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.suKienGomGiay', 'su_kien', 'Gom lệnh sự kiện (giây)', 'Gom nhiều lệnh thành một lần khởi động lại.', 'so', '120', '{"min": 30, "max": 3600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.LichSuKien', 79, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.suKienBaoTruocGiay', 'su_kien', 'Báo trước khi đổi sự kiện (giây)', '', 'so', '60', '{"min": 10, "max": 600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.LichSuKien', 80, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.worldCupBatDau', 'su_kien', 'World Cup bắt đầu', 'yyyy-MM-dd HH:mm:ss. Trống = tắt.', 'ngay_gio', '', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.SuKienWorldCup', 81, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.worldCupKetThuc', 'su_kien', 'World Cup kết thúc', 'Trống = tắt.', 'ngay_gio', '', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.SuKienWorldCup', 82, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.worldCupHanNhanQua', 'su_kien', 'World Cup hạn nhận quà', 'Trống = tắt.', 'ngay_gio', '', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.SuKienWorldCup', 83, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.gioChiemThanh', 'su_kien', 'Giờ chiếm Gym', '0–23, −1 = lịch chuẩn 21h thứ 3 + thứ 7.', 'so', '-1', '{"min": -1, "max": 23}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.MapTown', 84, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.phutChiemThanh', 'su_kien', 'Phút chiếm Gym', '0–59.', 'so', '-1', '{"min": -1, "max": 59}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.MapTown', 85, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.ngayChiemThanh', 'su_kien', 'Ngày chiếm Gym', 'Mon,Tue,... hoặc yyyy-MM-dd; trống = mỗi ngày.', 'chuoi', '', '{"mau": "^$|^((Mon|Tue|Wed|Thu|Fri|Sat|Sun)(,(Mon|Tue|Wed|Thu|Fri|Sat|Sun))*|\\\\d{4}-\\\\d{2}-\\\\d{2})$", "doDai": 64}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.MapTown', 86, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.isSuKienTrungThul2016', 'su_kien', 'Sự kiện cũ: Trung thu 2016', 'Cờ sự kiện gốc (đọc lúc khởi động).', 'co', '0', '{}', 'khoi_dong_lai', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.isSuKienTrungThul2016', 87, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.isSuKienTet2017', 'su_kien', 'Sự kiện cũ: Tết 2017', '', 'co', '0', '{}', 'khoi_dong_lai', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.isSuKienTet2017', 88, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.isSuKienGioTo2016', 'su_kien', 'Sự kiện cũ: Giỗ Tổ 2016', '', 'co', '0', '{}', 'khoi_dong_lai', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.isSuKienGioTo2016', 89, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.isSuKienNoel2023', 'su_kien', 'Sự kiện cũ: Noel 2023', '', 'co', '0', '{}', 'khoi_dong_lai', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.isSuKienNoel2023', 90, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.isSuKienTetduonglich2024', 'su_kien', 'Sự kiện cũ: Tết dương lịch 2024', '', 'co', '0', '{}', 'khoi_dong_lai', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.isSuKienTetduonglich2024', 91, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('bao_tri.bat', 'bao_tri', 'Chế độ bảo trì đăng nhập', '1 = từ chối đăng nhập mới (người đang chơi không bị đá). Tương đương tệp bao_tri.txt — bật một trong hai là bảo trì.', 'co', '0', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'server.ChiSoMayChu', 92, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('bao_tri.cau', 'bao_tri', 'Câu thông báo bảo trì', 'Trống = câu mặc định.', 'chuoi', '', '{"doDai": 200}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'server.ChiSoMayChu', 93, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('bao_tri.cho', 'bao_tri', 'Tài khoản được vào khi bảo trì', 'Tên tài khoản (chữ thường), cách nhau dấu phẩy.', 'ds_chuoi', '', '{"mau": "^[a-z0-9_]{3,20}$", "doDai": 255}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'server.ChiSoMayChu', 94, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.gioBaoTri', 'bao_tri', 'Giờ bảo trì hằng ngày', 'HH:MM cố định, ''tat'' = không tự bảo trì, trống = ngẫu nhiên 4:30–5:30.', 'chuoi', '', '{"mau": "^$|^(tat|off)$|^([01]?\\\\d|2[0-3]):[0-5]\\\\d$", "doDai": 5}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.BaoTriDaily', 95, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.chiSoMayChu', 'bao_tri', 'Ghi chỉ số máy chủ + đọc bao_tri.txt', '0 = tắt luồng chỉ số (tob_chi_so_may_chu).', 'co', '1', '{}', 'khoi_dong_lai', 0, 0, 0, 'cau_hinh', 'server', 'server.ChiSoMayChu', 96, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.spam.bat', 'chong_spam', 'Chống spam chat / thao tác', '0 = tắt toàn bộ.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.chongspam.ChongSpam', 97, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.spam.chat.gan', 'chong_spam', 'Nhịp chat gần', 'số tin / giây (vd 5/10).', 'nhip', '5/10', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongSpam', 98, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.spam.chat.rieng', 'chong_spam', 'Nhịp chat riêng', '', 'nhip', '6/10', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongSpam', 99, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.spam.chat.theGioi', 'chong_spam', 'Nhịp chat thế giới / người', '', 'nhip', '1/30', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongSpam', 100, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.spam.theGioiToanServer', 'chong_spam', 'Nhịp chat thế giới toàn máy chủ', '', 'nhip', '12/60', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongSpam', 101, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.spam.chat.doi', 'chong_spam', 'Nhịp chat đội', '', 'nhip', '8/10', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongSpam', 102, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.spam.chat.bang', 'chong_spam', 'Nhịp chat đội', '', 'nhip', '8/10', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongSpam', 103, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.spam.trungLapGiay', 'chong_spam', 'Chặn tin trùng lặp (giây)', '', 'so', '30', '{"min": 0, "max": 3600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongSpam', 104, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.spam.chanQuangCao', 'chong_spam', 'Chặn quảng cáo / link', '', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongSpam', 105, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.spam.viPhamCam', 'chong_spam', 'Số vi phạm thì cấm chat', '', 'so', '4', '{"min": 1, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongSpam', 106, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.spam.camGiay', 'chong_spam', 'Thang cấm chat (giây)', 'Các bậc cấm tăng dần, cách nhau dấu phẩy.', 'ds_so', '60,300,1800,7200', '{"min": 1, "max": 31536000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ChongSpam', 107, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.socketTimeout', 'chong_spam', 'Hết hạn socket (giây)', 'Áp dụng cho kết nối mới. 0 = tắt.', 'so', '180', '{"min": 0, "max": 3600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.SOCKET_TIMEOUT_MS', 108, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.maxConnPerIp', 'chong_spam', 'Kết nối tối đa / IP', '0 = tắt.', 'so', '50', '{"min": 0, "max": 100000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.MAX_CONN_PER_IP', 109, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.connRateMax', 'chong_spam', 'Số kết nối tối đa / cửa sổ', '0 = tắt.', 'so', '60', '{"min": 0, "max": 100000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.CONN_RATE_MAX', 110, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.connRateWindow', 'chong_spam', 'Cửa sổ đếm kết nối (giây)', '0 = tắt.', 'so', '30', '{"min": 0, "max": 3600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.CONN_RATE_WINDOW_MS', 111, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.dosBanSec', 'chong_spam', 'Thời gian chặn IP ngập (giây)', '', 'so', '300', '{"min": 0, "max": 86400}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.DOS_BAN_MS', 112, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.reloginWaitSec', 'chong_spam', 'Chờ đăng nhập lại (giây)', '', 'so', '0', '{"min": 0, "max": 600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.RELOGIN_WAIT_MS', 113, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.proxyTinCay', 'chong_spam', 'Proxy tin cậy (miễn giới hạn theo IP)', 'IP hoặc dải CIDR (IPv4/IPv6) của WAF / Nova agent, cách nhau dấu phẩy. Kết nối từ đây không bị tính sv.maxConnPerIp, sv.connRate*, chặn sv.dosBanSec, chờ đăng nhập theo IP, hạn mức đăng ký theo IP. Giới hạn theo tài khoản / kết nối / thiết bị vẫn giữ.', 'ds_chuoi', '127.0.0.1,::1', '{"mau": "^[0-9A-Fa-f:.]{2,45}(/[0-9]{1,3})?$", "doDai": 255}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.mang.ProxyTinCay', 114, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.dangNhapIpNguong', 'chong_spam', 'Ngưỡng đăng nhập dồn dập / IP', 'Số lần đăng nhập trong ~1 giây từ một IP (không phải proxy tin cậy) thì bắt đầu chờ + ghi nghi vấn. 0 = tắt.', 'so', '10', '{"min": 0, "max": 100000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.mang.GioiHanIp', 115, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.dangNhapIpChoMs', 'chong_spam', 'Chờ khi đăng nhập dồn dập (ms)', 'Thời gian chờ mỗi lần đăng nhập khi vượt sv.dangNhapIpNguong. 0 = không chờ.', 'so', '3000', '{"min": 0, "max": 10000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.mang.GioiHanIp', 116, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.tranGoiThuong', 'chong_spam', 'Trần gói tin thường / cửa sổ', '', 'so', '300', '{"min": 10, "max": 100000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'io.BoDemGoiTin', 117, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.tranGoiTaiNguyen', 'chong_spam', 'Trần gói tải tài nguyên / cửa sổ', '', 'so', '600', '{"min": 10, "max": 100000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'io.BoDemGoiTin', 118, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.tranGoiTraCuu', 'chong_spam', 'Trần gói tra cứu / cửa sổ', '', 'so', '600', '{"min": 10, "max": 100000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'io.BoDemGoiTin', 119, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.log.muc', 'chong_spam', 'Mức nhật ký (màn hình)', '', 'chon', 'THONG_TIN', '{"luaChon": ["LOI", "CANH_BAO", "THONG_TIN", "GO_LOI", "ERROR", "WARN", "INFO", "DEBUG"], "luaChonTen": ["L\\u1ed7i", "C\\u1ea3nh b\\u00e1o", "Th\\u00f4ng tin", "G\\u1ee1 l\\u1ed7i", "L\\u1ed7i (ERROR)", "C\\u1ea3nh b\\u00e1o (WARN)", "Th\\u00f4ng tin (INFO)", "G\\u1ee1 l\\u1ed7i (DEBUG)"], "hoa": true}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.log.Nhat', 120, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.log.mucTep', 'chong_spam', 'Mức nhật ký (tệp)', 'Trống = như màn hình.', 'chon', '', '{"luaChon": ["", "LOI", "CANH_BAO", "THONG_TIN", "GO_LOI", "ERROR", "WARN", "INFO", "DEBUG"], "luaChonTen": ["Nh\\u01b0 m\\u00e0n h\\u00ecnh", "L\\u1ed7i", "C\\u1ea3nh b\\u00e1o", "Th\\u00f4ng tin", "G\\u1ee1 l\\u1ed7i", "L\\u1ed7i (ERROR)", "C\\u1ea3nh b\\u00e1o (WARN)", "Th\\u00f4ng tin (INFO)", "G\\u1ee1 l\\u1ed7i (DEBUG)"], "hoa": true}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.log.Nhat', 121, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.log.tat', 'chong_spam', 'Loại nhật ký tắt', 'Mã loại, cách nhau dấu phẩy.', 'ds_chuoi', 'GOI3,GOI5,GOI26,VKTT,SQL,LUU_CHAR', '{"mau": "^[A-Z0-9_]{1,32}$", "doDai": 255}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.log.Nhat', 122, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.log.kiemToan', 'chong_spam', 'Loại nhật ký kiểm toán', 'Ghi riêng tệp kiem_toan (giữ lâu).', 'ds_chuoi', 'ADMIN,GM,NAP,NAP_TIEN,GIFTCODE,CAM,BAN,KINH_TE,KINHTE,CHONG_SPAM', '{"mau": "^[A-Z0-9_]{1,32}$", "doDai": 255}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.log.Nhat', 123, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chi_so.bat', 'chi_so', 'Bật giới hạn chỉ số', '0 = tắt mọi trần/giảm dần chỉ số (quay về công thức cũ).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChiSoTrangBiGame', 124, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chi_so.kep_trang_bi', 'chi_so', 'Kẹp chỉ số trang bị theo ngân sách', 'Mỗi món ≤ chuẩn bậc × 1,30 × nâng cấp; đồ bậc cũ ≤ 130% đồ bậc hiện tại.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChiSoTrangBiGame', 125, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chi_so.tran_chi_mang', 'chi_so', 'Trần tỉ lệ chí mạng (‰)', 'Phần nghìn, có giảm dần. PvP cố định 300‰.', 'so', '400', '{"min": 100, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChiSoTrangBiGame', 126, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chi_so.tran_st_chi_mang', 'chi_so', 'Trần ST chí mạng cộng thêm (‰)', 'Trên nền ×1,5. 500 = đòn chí mạng tối đa ×2,0.', 'so', '500', '{"min": 0, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChiSoTrangBiGame', 127, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chi_so.tran_ne', 'chi_so', 'Trần né tránh (‰)', 'Phần nghìn, có giảm dần. PvP cố định 200‰.', 'so', '300', '{"min": 0, "max": 900}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChiSoTrangBiGame', 128, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chi_so.tran_hut', 'chi_so', 'Trần hút HP PvE (‰ sát thương)', 'Gộp mọi nguồn hút HP.', 'so', '100', '{"min": 0, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChiSoTrangBiGame', 129, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chi_so.tran_hut_pvp', 'chi_so', 'Trần hút HP PvP (‰ sát thương thật)', 'Gộp mọi nguồn hút HP, tính trên sát thương sau ×0,30.', 'so', '50', '{"min": 0, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChiSoTrangBiGame', 130, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chi_so.tran_giam_st', 'chi_so', 'Trần giảm sát thương nhận (‰)', 'Gộp mọi nguồn thụ động (PvP cố định 400‰).', 'so', '500', '{"min": 0, "max": 900}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChiSoTrangBiGame', 131, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chi_so.tran_khang_he', 'chi_so', 'Trần kháng một hệ (‰)', 'Kháng hệ + kháng mọi hệ + phiến đá.', 'so', '600', '{"min": 0, "max": 900}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChiSoTrangBiGame', 132, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chi_so.san_sat_thuong', 'chi_so', 'Sàn sát thương mỗi đòn (‰)', 'Sau mọi lớp giảm, một đòn còn ít nhất chừng này phần nghìn (chống bất tử).', 'so', '400', '{"min": 0, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChiSoTrangBiGame', 133, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('chi_so.tran_phan_tram_cong', 'chi_so', 'Trần tổng % cộng thêm (‰)', 'Tổng % tấn công/phòng thủ/HP từ pet, thú cưỡi, thời trang, bộ, VIP… so với chỉ số gốc.', 'so', '500', '{"min": 0, "max": 5000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ChiSoTrangBiGame', 134, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('pvp.tinh_diem.bat', 'pvp', 'Tính điểm thang PvP', '0 = trận PvP không cộng/trừ điểm thang.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.ThangPvpGame', 135, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('gym.bat', 'pvp', 'Thách đấu Gym', '0 = tạm đóng Gym.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.GymLauDai', 136, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('ham_nguc.bat', 'pvp', 'Hầm Ngục bí ẩn', '0 = không vào tầng mới (vẫn nhận thưởng đã đạt).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.SoTayLauDai#hamNguc', 137, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.bat', 'tu_dong', 'Bật Tự động đánh', '0 = tạm khoá (người đang dùng bị dừng ở lần kiểm tiếp theo).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 138, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.gio_moi_ngay', 'tu_dong', 'Giờ Tự động đánh mỗi ngày', 'Miễn phí, chỉ tính lúc online và đang bật. Đặt lại lúc 00:00.', 'so', '5', '{"min": 0, "max": 24}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 139, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.gio_them_the_thang', 'tu_dong', 'Giờ cộng thêm khi có thẻ tháng', 'Cộng vào giờ mỗi ngày khi thẻ tháng còn hạn.', 'so', '2', '{"min": 0, "max": 24}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 140, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.chi_map_da_mo', 'tu_dong', 'Chỉ ở bãi luyện tập', '1 = chỉ bật ở bản đồ có Pokémon hoang dã đã mở (không ở thành phố, nhà, sàn đấu, sự kiện).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 141, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.tat_khi_pvp', 'tu_dong', 'Dừng khi PvP / sàn đấu / Gym / boss', '1 = dừng khi bị Pokémon khác tấn công, bật PK, vào sàn đấu / Gym / chiến trường hay đánh boss.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 142, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.tu_dung_thuoc', 'tu_dong', 'Cho tự uống thuốc', '0 = Tự động đánh không tự uống thuốc hồi phục (người chơi tự bấm).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 143, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.ban_kinh', 'tu_dong', 'Bán kính tối đa (px)', 'Pokémon chỉ đánh quanh điểm bật trong bán kính này; ra xa hơn (+64 px) thì dừng. 1 ô = 16 px.', 'so', '240', '{"min": 48, "max": 960, "buoc": 16}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 144, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.nhip_danh', 'tu_dong', 'Nhịp đánh tối đa khi tự động', 'số lệnh đánh / giây (vd 12/5). Vượt thì bỏ lệnh và tính vi phạm.', 'nhip', '12/5', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 145, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.nhip_thuoc', 'tu_dong', 'Nhịp uống thuốc tối đa khi tự động', '', 'nhip', '4/5', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 146, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.nhip_nhat', 'tu_dong', 'Nhịp nhặt đồ tối đa khi tự động', '', 'nhip', '8/5', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 147, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.nhip_nguoi', 'tu_dong', 'Nhịp đánh nghi bot (mọi người chơi)', 'Chỉ ghi nghi vấn (hack.txt), không chặn.', 'nhip', '30/5', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 148, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.vi_pham_tat', 'tu_dong', 'Số vi phạm nhịp thì dừng', 'Dừng Tự động đánh + ghi nghi vấn.', 'so', '20', '{"min": 1, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 149, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.hoi_sinh_moi_gio', 'tu_dong', 'Tự hồi sinh tối đa / giờ', 'Khi đang tự động mà Pokémon gục: chỉ dùng vật phẩm hồi sinh nếu người chơi bật "Tự hồi sinh", tối đa ngần này lần / 60 phút; còn lại dừng tự động và về Trung tâm Pokémon. 0 = không bao giờ.', 'so', '2', '{"min": 0, "max": 20}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 150, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.ke_hoach', 'tu_dong', 'Máy chủ điều khiển từng bước', '1 = máy chủ chọn mục tiêu, chiêu, thuốc, nhặt đồ, rút lui cho client 0.9.2+ (client chỉ thực hiện). 0 = client tự chọn như bản 0.9.1 (máy chủ vẫn giới hạn giờ / nhịp / vùng).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 151, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.cap_toi_thieu', 'tu_dong', 'Cấp tối thiểu để bật', '0 = mọi cấp. Dưới cấp này bấm Bắt đầu thì được báo rõ cấp cần.', 'so', '0', '{"min": 0, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 152, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.chenh_cap_tren', 'tu_dong', 'Bỏ Pokémon cao hơn quá N cấp', 'Không đánh Pokémon hoang dã cao hơn cấp mình quá ngần này. -1 = không giới hạn.', 'so', '5', '{"min": -1, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 153, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.chenh_cap_duoi', 'tu_dong', 'Bỏ Pokémon thấp hơn quá N cấp', '0 = không giới hạn dưới (vẫn đánh Pokémon yếu).', 'so', '0', '{"min": 0, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 154, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.danh_boss', 'tu_dong', 'Cho đánh boss', '0 = không chọn boss / Pokémon huyền thoại làm mục tiêu (đánh trúng boss vẫn dừng theo tu_dong.tat_khi_pvp).', 'co', '0', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 155, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.hp_quai_toi_da_pt', 'tu_dong', 'Bỏ Pokémon quá trâu (% HP mình)', 'Bỏ Pokémon có HP tối đa lớn hơn ngần này % HP tối đa của mình. 0 = tắt.', 'so', '500', '{"min": 0, "max": 100000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 156, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.chon_chieu_theo_he', 'tu_dong', 'Chọn chiêu theo khắc hệ', '1 = điểm chiêu = sát thương × khắc hệ × STAB, bỏ chiêu đang hồi / thiếu PP. 0 = chỉ theo sát thương.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 157, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.nguong_thuoc_hp', 'tu_dong', 'Ngưỡng thuốc HP mặc định (%)', 'Uống thuốc HP khi HP dưới ngưỡng người chơi chọn trong bảng; client cũ dùng số này. 0 = máy chủ không cho tự uống thuốc HP.', 'so', '50', '{"min": 0, "max": 90}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 158, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.nguong_thuoc_mp', 'tu_dong', 'Ngưỡng thuốc PP mặc định (%)', 'Như trên cho PP. 0 = tắt.', 'so', '30', '{"min": 0, "max": 90}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 159, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.nguong_hp_thap', 'tu_dong', 'HP thấp thì rút lui (%)', 'HP dưới ngưỡng mà hết thuốc: rút về điểm bắt đầu nghỉ (hoặc dừng nếu tu_dong.hp_thap_dung=1). 0 = tắt.', 'so', '25', '{"min": 0, "max": 90}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 160, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.hp_thap_dung', 'tu_dong', 'HP thấp thì dừng hẳn', '1 = dừng Tự động đánh thay vì rút lui nghỉ hồi phục.', 'co', '0', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 161, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.hoi_phuc_toi', 'tu_dong', 'Nghỉ hồi phục tới (%)', 'Rút lui nghỉ tới khi HP đạt ngần này % rồi đánh tiếp.', 'so', '80', '{"min": 10, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 162, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.nhat_ban_kinh', 'tu_dong', 'Bán kính nhặt đồ (px)', 'Chỉ đi nhặt đồ rơi của mình trong bán kính này quanh Pokémon (và trong vùng). 0 = không nhặt. 1 ô = 16 px.', 'so', '96', '{"min": 0, "max": 480, "buoc": 16}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 163, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.dung_khi_day_tui', 'tu_dong', 'Dừng khi hành trang đầy', '1 = hành trang đầy thì dừng và báo.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 164, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.dung_khi_tu_di', 'tu_dong', 'Dừng khi người chơi tự di chuyển', '1 = bấm phím di chuyển / chạm bản đồ thì dừng Tự động đánh. 0 = nhường 3 giây rồi chạy tiếp.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 165, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.tranh_pk_px', 'tu_dong', 'Tránh người bật PK (px)', 'Có người chơi khác bật PK trong bán kính này thì lùi về điểm bắt đầu chờ. 0 = tắt.', 'so', '160', '{"min": 0, "max": 960, "buoc": 16}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.pokemon.TuDongKeHoach', 166, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.map_cam', 'tu_dong', 'Bản đồ cấm Tự động đánh thêm (id)', 'Ngoài sàn đấu / Gym / chiến trường / mê cung / bản đồ boss. Cách nhau dấu phẩy; trống = không.', 'chuoi', '', '{"mau": "^[0-9, ]*$", "doDai": 255}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 167, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('tu_dong.hoi_sinh_tra_phi', 'tu_dong', 'Cho tự dùng vật phẩm hồi sinh trả phí', '1 = khi người chơi đồng ý riêng (bảng Tự động: Tự hồi sinh "cả vật phẩm lượng") mới được tự dùng Thuốc hồi sinh tối đa (lượng khoá). 0 = chỉ Thuốc hồi sinh thường; không bao giờ tự trả xu / lượng.', 'co', '0', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.TuDongGame', 168, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.bat', 'hoi_sinh', 'Bật cơ chế hồi sinh mới', '0 = luồng cũ (popup hồi sinh tại chỗ 5% giờ thu nhập, tự hồi sinh dưới cấp 50).', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 169, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.xu_phan_tram_gio', 'tu_dong', 'Phí xu tại chỗ (% thu nhập 1 giờ)', 'Theo cấp (ThongSoLauDai.xuMoiGio). 40 = 0,4 giờ cày. Nhân hệ số gục lặp.', 'so', '40', '{"min": 0, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 170, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.xu_toi_thieu', 'tu_dong', 'Phí xu tại chỗ tối thiểu', 'Xu.', 'so', '200', '{"min": 0, "max": 100000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 171, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.he_so_lap', 'hoi_sinh', 'Hệ số gục lặp (%)', 'Lần gục thứ 1, 2, 3... trong cửa sổ; lần sau dùng số cuối (trần). Áp cho phí xu và lượng khoá.', 'ds_so', '100,150,200,300', '{"min": 0, "max": 10000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 172, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.cua_so_phut', 'hoi_sinh', 'Cửa sổ gục lặp (phút)', 'Đếm số lần gục trong ngần này phút.', 'so', '30', '{"min": 1, "max": 1440}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 173, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.luong_khoa', 'tu_dong', 'Phí lượng khoá tại chỗ', '0 = tắt lựa chọn trả lượng khoá. Nhân hệ số gục lặp (làm tròn lên).', 'so', '2', '{"min": 0, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 174, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.luong_khoa_toi_da', 'tu_dong', 'Trần phí lượng khoá', 'Lượng khoá tối đa một lần.', 'so', '10', '{"min": 0, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 175, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.cho_sau_guc_giay', 'hoi_sinh', 'Chờ sau khi gục (giây)', 'Hồi sinh tại chỗ chỉ bấm được sau ngần này giây (đếm ngược trên hộp thoại).', 'so', '5', '{"min": 0, "max": 600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 176, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.hoi_chieu_giay', 'tu_dong', 'Hồi chiêu hồi sinh tại chỗ (giây)', 'Giữa hai lần hồi sinh tại chỗ. Về Trung tâm luôn được.', 'so', '60', '{"min": 0, "max": 3600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 177, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.hp_thuoc', 'hoi_sinh', 'HP khi dùng Thuốc hồi sinh (%)', 'Thuốc hồi sinh tối đa lấy % trong data_vat_pham_lau_dai (hoi_sinh:100).', 'so', '30', '{"min": 1, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 178, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.hp_tra_phi', 'hoi_sinh', 'HP khi trả xu / lượng khoá / tân thủ (%)', '', 'so', '50', '{"min": 1, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 179, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.exp_mat_phan_nghin', 'hoi_sinh', 'Mất EXP khi về Trung tâm (phần nghìn)', 'Phần nghìn EXP lên cấp hiện tại (20 = 2%). Không bao giờ tụt cấp: chỉ trừ trong tiến độ cấp.', 'so', '20', '{"min": 0, "max": 200}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 180, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.exp_mat_toi_da_phut', 'hoi_sinh', 'Trần mất EXP (phút cày)', 'Không mất quá ngần này phút EXP đánh quái ở cấp đó. 0 = không chặn.', 'so', '15', '{"min": 0, "max": 600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 181, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.do_ben_mat_phan_tram', 'hoi_sinh', 'Mất độ bền khi về Trung tâm (%)', '% độ bền tối đa của mỗi trang bị đang mặc (không làm vỡ đồ).', 'so', '5', '{"min": 0, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 182, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.met_moi_phut', 'hoi_sinh', 'Mệt mỏi (phút)', 'Sau khi về Trung tâm. 0 = tắt.', 'so', '5', '{"min": 0, "max": 120}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 183, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.met_moi_exp', 'hoi_sinh', 'Mệt mỏi: giảm EXP (%)', 'EXP hạ quái.', 'so', '20', '{"min": 0, "max": 90}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 184, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.met_moi_tan_cong', 'hoi_sinh', 'Mệt mỏi: giảm tấn công (%)', '', 'so', '10', '{"min": 0, "max": 90}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 185, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.tan_thu_cap', 'tu_dong', 'Bảo vệ tân thủ dưới cấp', 'Dưới cấp này: không mất EXP / độ bền / Mệt mỏi, hồi sinh tại chỗ miễn phí.', 'so', '15', '{"min": 0, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 186, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.tan_thu_hoi_chieu_giay', 'hoi_sinh', 'Hồi chiêu hồi sinh miễn phí tân thủ (giây)', '', 'so', '30', '{"min": 0, "max": 3600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 187, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.dong_doi_bat', 'hoi_sinh', 'Cho chờ đồng đội hồi sinh', 'Đồng đội ở gần mang Thuốc hồi sinh được mời cứu.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 188, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.dong_doi_giay', 'hoi_sinh', 'Thời gian chờ đồng đội (giây)', '', 'so', '60', '{"min": 10, "max": 600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 189, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.cam_pvp', 'hoi_sinh', 'Cấm tại chỗ ở PvP / sàn đấu', 'Sàn đấu, giải Liên minh, chiến trường, lãnh địa đội, chiếm Gym đang đánh, đấu trường + hoi_sinh.map_pvp.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 190, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.cam_gym', 'hoi_sinh', 'Cấm tại chỗ khi thách đấu Gym', '', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 191, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.cam_boss', 'hoi_sinh', 'Cấm tại chỗ giữa trận boss', 'Đã gây sát thương vào boss thế giới / Gym còn sống cùng bản đồ.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 192, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.cam_me_cung', 'hoi_sinh', 'Cấm tại chỗ trong Mê cung', '', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 193, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.map_pvp', 'hoi_sinh', 'Bản đồ PvP (id)', 'Thêm vào luật cấm PvP (so cả id bản đồ lẫn id nạp).', 'ds_so', '202,203,204,205,227,228', '{"min": 0, "max": 100000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 194, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.map_cam_them', 'hoi_sinh', 'Bản đồ cấm tại chỗ thêm (id)', 'Cách nhau dấu phẩy; trống = không.', 'chuoi', '', '{"mau": "^[0-9, ]*$", "doDai": 255}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 195, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.auto_ve_map_up', 'hoi_sinh', 'Cho "Auto về Map Up" sau khi gục', '0 = gục thì không tự bay lại bãi luyện (chống treo máy vô hạn).', 'co', '0', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 196, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.gia_thuoc_phan_tram_gio', 'tu_dong', 'Giá Thuốc hồi sinh (% thu nhập 1 giờ)', 'Giá ở cửa hàng NPC theo cấp người mua (cùng bậc phí xu tại chỗ). 0 = giá gốc data_potion.', 'so', '30', '{"min": 0, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 197, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.gia_thuoc_toi_thieu', 'tu_dong', 'Giá Thuốc hồi sinh tối thiểu', 'Xu / viên (không thấp hơn giá gốc).', 'so', '407', '{"min": 0, "max": 100000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 198, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('hoi_sinh.thuoc_moi_gio', 'hoi_sinh', 'Hồi sinh bằng vật phẩm tối đa / giờ', 'Thuốc hồi sinh / tối đa của chính mình (tay, Tự động đánh, dùng từ túi) trong 60 phút. Đồng đội cứu không tính. 0 = không giới hạn.', 'so', '6', '{"min": 0, "max": 100}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'real.HoiSinhGame', 199, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.thongBao.bat', 'thong_bao', 'Gộp thông báo toàn máy chủ', '0 = gửi thẳng như cũ.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'com.htbb.chongspam.ThongBaoGop', 200, 'luon_bat', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.thongBao.toiDa', 'thong_bao', 'Thông báo tối đa / cửa sổ', '', 'so', '6', '{"min": 1, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ThongBaoGop', 201, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.thongBao.cuaSoGiay', 'thong_bao', 'Cửa sổ đếm thông báo (giây)', '', 'so', '60', '{"min": 1, "max": 3600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ThongBaoGop', 202, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.thongBao.moiLoai', 'thong_bao', 'Tối đa mỗi loại / cửa sổ', '', 'so', '3', '{"min": 1, "max": 1000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ThongBaoGop', 203, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.thongBao.trungLapGiay', 'thong_bao', 'Bỏ thông báo trùng (giây)', '', 'so', '120', '{"min": 0, "max": 86400}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ThongBaoGop', 204, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.thongBao.gopGiay', 'thong_bao', 'Gộp mỗi (giây)', '', 'so', '15', '{"min": 1, "max": 3600}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'ThongBaoGop', 205, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.website', 'bao_tri', 'Địa chỉ website (hiện trong game)', '', 'chuoi', 'https://huyenthoaibaoboi.fun/', '{"doDai": 200}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.website', 206, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.linkTaiGame', 'bao_tri', 'Link tải game (hiện trong game)', '', 'chuoi', 'https://huyenthoaibaoboi.fun/', '{"doDai": 200}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.linkTaiGame', 207, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.adminAccounts', 'bao_tri', 'Danh sách GM trong game', 'Tên Pokémon GM, cách nhau dấu phẩy.', 'ds_chuoi', '', '{"mau": "^[A-Za-z0-9_]{1,30}$", "doDai": 255}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.adminAccounts', 208, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.gmCanCoPInfo', 'quan_tri', 'GM cần cả cờ pInfo', '1 = quyền GM cần tên trong danh sách VÀ pInfo[16]=1.', 'co', '0', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.gmCanCoPInfo', 209, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.banToiThieu', 'bao_tri', 'Bản client tối thiểu', '> 0 = từ chối client jar/Android cũ hơn.', 'so', '0', '{"min": 0, "max": 1000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.banToiThieu', 210, 'chinh', 0);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.choPhepTaoTenGM', 'quan_tri', 'Cho tạo Pokémon tên GM', 'Chỉ bật tạm thời.', 'co', '0', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'data.Database.CHO_PHEP_TAO_TEN_GM', 211, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.testMode', 'quan_tri', 'Chế độ thử (testMode)', 'Mã test cấp tài nguyên miễn phí. Khoá ở máy thật.', 'co', '0', '{}', 'ngay', 0, 1, 0, 'cau_hinh', 'server', 'TeamServer.testMode', 212, 'co_dinh', 1);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.testCode', 'quan_tri', 'Mã test', 'Chuỗi gõ trong game khi testMode=1.', 'chuoi', '', '{"mau": "^[ -~]*$", "doDai": 64}', 'ngay', 1, 1, 0, 'cau_hinh', 'server', 'TeamServer.testCode', 213, 'co_dinh', 1);

-- cau_hinh_nhom (du_lieu/sql_nguon/pokemon_cau_hinh_trung_tam.sql, tools/cauhinh/sinh_cau_hinh.py)
DROP TABLE IF EXISTS `cau_hinh_nhom`;
CREATE TABLE IF NOT EXISTS `cau_hinh_nhom` (`ma` VARCHAR(20) NOT NULL, `ten` VARCHAR(160) NOT NULL DEFAULT '', `mo_ta` TEXT NOT NULL, `thu_tu` INT NOT NULL DEFAULT 0, `an` TINYINT NOT NULL DEFAULT 0, PRIMARY KEY (`ma`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('kinh_te', 'Kinh tế: tỉ lệ rơi & EXP', 'Tỉ lệ rơi xu / lượng / đồ và EXP quái. Áp dụng ngay. Thuế, phí và công thức kinh tế cốt lõi đã cố định (không hiện ở đây).', 1, 0);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('su_kien', 'Sự kiện & giftcode', 'Giờ chiếm Gym, trần & ngân sách giftcode. Bật/tắt từng sự kiện ở trang Sự kiện (bảng tob_su_kien).', 2, 0);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('nap_tien', 'Nạp tiền', 'Công tắc bán từng loại sản phẩm, trần chi tiêu. Web vẫn KHOÁ (fail closed) khi thiếu HTBB_NAP_BAT, giấy phép hoặc bí mật cổng thanh toán — các giá trị đó nằm trong biến môi trường, không ở CSDL.', 3, 0);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('thanh_vien', 'Thành viên & kích hoạt', 'Thành viên HTBB theo tài khoản (trả phí) và Kích hoạt tài khoản (miễn phí): tính năng bị khoá, giá, thời hạn, chặn đăng nhập.', 4, 0);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('chong_clone', 'Chống clone & chuyển xu', 'Cổng cho mọi đường chuyển xu / giá trị giữa hai người chơi (giao dịch, ký gửi, thú cưng lấy xu, chợ trang trí, chợ sinh thái, thách đấu): cấp, tuổi tài khoản, giờ chơi, giới hạn cùng thiết bị, giá ký gửi. Nhật ký + báo cáo "Nghi clone" ở /admin/kinh-te.', 5, 0);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('tu_dong', 'Tự động đánh & hồi sinh', 'Giới hạn Tự động đánh (chỉ khi online) và giá hồi sinh tại chỗ. Luật an toàn (cấm ở PvP / Gym / boss, nhịp, bán kính…) đã cố định.', 6, 0);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('bao_tri', 'Máy chủ, đăng ký & bảo trì', 'Bảo trì, đăng ký, bản client tối thiểu, link website / tải game, GM, ngày khai trương.', 7, 0);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('che_do', 'Chế độ game (cố định)', 'Chế độ lớn luôn bật trên máy thật (dài hạn, hệ sinh thái). Đổi = sửa server.ini khi khẩn cấp.', 8, 1);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('dang_ky', 'Đăng ký (cố định)', 'Tham số nội bộ của đăng ký trong game.', 9, 1);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('chong_spam', 'Chống spam, DoS & nhật ký (cố định)', 'Nhịp chat/thao tác, chống ngập kết nối/gói tin, mức nhật ký.', 10, 1);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('pvp', 'PvP / Gym / Hầm Ngục (luôn bật)', 'Nội dung chiến đấu dài hạn.', 11, 1);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('chi_so', 'Chỉ số & chống lạm phát (cố định)', 'Trần + giảm dần cho chỉ số trang bị, pet, thú cưỡi, skin (docs/thiet-ke-game.md).', 12, 1);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('hoi_sinh', 'Hồi sinh — luật (cố định)', 'Phạt khi về Trung tâm, mệt mỏi, cấm hồi sinh tại chỗ ở PvP / Gym / boss / Mê cung.', 13, 1);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('thong_bao', 'Thông báo (cố định)', 'Gộp thông báo toàn máy chủ.', 14, 1);
INSERT INTO `cau_hinh_nhom` (`ma`, `ten`, `mo_ta`, `thu_tu`, `an`) VALUES ('quan_tri', 'Quản trị & thử nghiệm (cố định)', 'Cờ GM cũ và khoá thử nghiệm (máy thật luôn mặc định).', 15, 1);

-- ======== 3. Chi muc bang nong

-- du_lieu/sql_nguon/them_chi_muc_production.sql
CREATE INDEX IF NOT EXISTS idx_5h_active_username ON `5h_active` (username);
CREATE INDEX IF NOT EXISTS idx_5h_active_userid ON `5h_active` (userID);
CREATE INDEX IF NOT EXISTS idx_char_vip_charname ON tob_char_vip (charname);
CREATE INDEX IF NOT EXISTS idx_buy_ruong_ngay_ten ON tob_buy_ruong (dateLog, charname);
CREATE INDEX IF NOT EXISTS idx_qua_ct_top_charname ON tob_qua_ct_top (charname);

-- ======== 4. Cau hinh he thong / cong tac su kien (khong phai du lieu nguoi choi), chay lai duoc

-- du_lieu/sql_nguon/sua_he_so_nap_va_qua_tao_nv.sql (chi lenh nay)
UPDATE `5h_systems` SET status = 1 WHERE command = 'tangnap' AND status <> 1;

-- du_lieu/sql_nguon/them_lich_su_kien_nam.sql (chi lenh nay)
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

-- du_lieu/sql_nguon/them_lich_su_kien_nam.sql (chi lenh nay)
ALTER TABLE tob_su_kien ADD COLUMN IF NOT EXISTS tu_dong TINYINT NOT NULL DEFAULT 1;

-- du_lieu/sql_nguon/them_lich_su_kien_nam.sql (chi lenh nay)
UPDATE tob_su_kien SET mo_luc = NULL, dong_luc = NULL
 WHERE tu_dong = 1 AND (sua_boi IS NULL OR sua_boi = 'lich hen')
   AND ma IN ('tetduonglich','tet2017','tet123','moi:valentine','quocte83','gioto','moi:3004','moi:thieunhi','he2017',
              'moi:hamngucmua','vulan','moi:quockhanh','trungthu','moi:2010','halloween','demhalloween','sinhnhat2011',
              'blackfriday','blackfridaydaugia','blackfridayban','noel','demnoel','moi:tongket','moi:tuanhe',
              'moi:congdong','moi:shiny','moi:dotkich','moi:muapvp','moi:pokedex','moi:chienvung','moi:bungphat');

-- du_lieu/sql_nguon/them_lich_su_kien_nam.sql (chi lenh nay)
DELETE FROM tob_su_kien WHERE ma IN ('worldcup','worldcupnhanqua','worldcup2017','snngude','duatophoalu');
