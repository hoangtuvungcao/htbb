-- THEO DOI CLIENT DANG NHAP (web /admin/online). Idempotent, MariaDB >= 10.0.2.
-- May chu game (data/GhiClient, GhiViTriOnline, GhiSoOnline) cung tu tao bang / them cot luc
-- khoi dong; chay tep nay de trang /admin/online het bao "chua co bang" ngay, khong can doi.
--   mysql htbb_db < sql/them_bang_tob_client_dang_nhap.sql

-- Moi dong = (tai khoan, loai client): so lan, lan dau, lan cuoi. Cung DDL voi data.GhiClient.DDL.
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

-- Anh chup "ai dang online" (data.GhiViTriOnline): them tai khoan + client.
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

-- Mau so online 5 phut (data.GhiSoOnline): them so nhan vat theo loai client.
ALTER TABLE `tob_log_online`
  ADD COLUMN IF NOT EXISTS `pc` INT NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS `android` INT NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS `jar` INT NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS `khac` INT NOT NULL DEFAULT 0;
