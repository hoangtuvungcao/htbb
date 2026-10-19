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
