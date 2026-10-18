-- ============================================================================
-- web_cms.sql — bang noi dung website (CMS) + phan quyen admin + khoa co thoi han
-- Chay MOT LAN vao database GAME (htbb_db), cung cho voi web_admin_log:
--
--     mysql -u htbb -p htbb_db < web_cms.sql
--     NovaDB: mysql -h 127.0.0.1 -P 3318 -u htbb -p htbb_db < web_cms.sql
--
-- Chay lai nhieu lan van an toan (CREATE TABLE IF NOT EXISTS, INSERT ... ON DUPLICATE KEY).
-- SQL viet de chay ca MariaDB lan NovaDB: khong CREATE TABLE LIKE, khong UPDATE...JOIN,
-- khong TIMESTAMPDIFF, khong ten "db.bang" cheo CSDL, khong ENUM/JSON.
-- Ban goc: HTBB-Web/server/sql/web_cms.sql (giu y het HTBB/sql/web_cms.sql).
--
-- Website van chay khi CHUA co cac bang nay: moi trang roi ve noi dung tinh / du lieu
-- sinh tu CSV. Trang admin bao "chua co bang" va huong dan chay file nay.
-- ============================================================================

-- Cai dat dang khoa -> gia tri JSON: 'site' (ten, SEO, mang xa hoi, bao tri, thanh thong bao),
-- 'trang_chu' (hero, the tinh nang, banner), 'menu' (menu dau trang + cot chan trang),
-- 'tai_game' (link tai, phien ban, dung luong, nhat ky thay doi).
CREATE TABLE IF NOT EXISTS `web_cai_dat` (
  `khoa`         varchar(64)  NOT NULL,
  `gia_tri`      mediumtext   NOT NULL,
  `cap_nhat_luc` datetime     NOT NULL DEFAULT current_timestamp(),
  `cap_nhat_boi` varchar(50)  NOT NULL DEFAULT '',
  PRIMARY KEY (`khoa`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Tin tuc & su kien. trang_thai: 0 nhap, 1 xuat ban (dang_luc <= bay gio thi hien; > bay gio = hen gio).
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

-- Trang cam nang / huong dan. slug trung trang co san (vd 'he-khac-che') = ghi de ten/mo ta/
-- thu tu/an hien + noi dung Markdown hien THEM o cuoi trang; slug moi = trang moi /cam-nang/<slug>.
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

-- Thu vien anh tai len (file nam o HTBB_UPLOAD_DIR, phuc vu qua /uploads/<ten_file>).
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

-- Lich su kien 52 tuan (hat giong: HTBB/du_lieu/su_kien_nam.csv, nut "Nhap tu CSV").
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

-- Vai tro admin ngoai HTBB_ADMIN_ACCOUNTS (nguoi trong bien moi truong luon la sieu_quan_tri).
-- vai_tro: sieu_quan_tri | bien_tap | ho_tro | xem
CREATE TABLE IF NOT EXISTS `web_admin_vai_tro` (
  `username`  varchar(50)  NOT NULL COMMENT 'account.team_user.username (chu thuong)',
  `vai_tro`   varchar(20)  NOT NULL DEFAULT 'xem',
  `ghi_chu`   varchar(200) NOT NULL DEFAULT '',
  `tao_boi`   varchar(50)  NOT NULL DEFAULT '',
  `tao_luc`   datetime     NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Khoa tai khoan co ly do + thoi han. team_user.ban (CSDL account) van la cong tac that;
-- website tu mo khoa khi het_han qua (kiem moi 5 phut). het_han NULL = vinh vien.
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

-- Lich su thay doi noi dung (truoc/sau) cho moi thao tac sua cua admin.
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

-- Kiem tra: phai ra 9 dong.
SELECT table_name FROM information_schema.tables
 WHERE table_schema = DATABASE() AND table_name IN ('web_cai_dat','web_tin_tuc','web_trang','web_faq','web_media',
   'web_su_kien_lich','web_admin_vai_tro','web_khoa_tai_khoan','web_admin_thay_doi');
