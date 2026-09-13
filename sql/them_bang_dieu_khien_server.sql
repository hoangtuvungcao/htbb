-- Bang dieu khien bat/tat server tu web admin (web/admin/server.php).
--
-- Web KHONG SSH/exec vao VPS duoc - no chi ghi "trang thai mong muon" vao day.
-- Mot tien trinh canh gac rieng chay ngoai JVM game (vps/dieu-khien-server.sh,
-- cai qua cron cua ROOT) doc bang nay moi phut roi tu goi systemctl thay ban.
--
-- Chi MOT dong duy nhat (id=1) - dieu khien MOT service kpah-game. Chay mot
-- lan:  mysql -u root kpah_db < sql/them_bang_dieu_khien_server.sql
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
