-- ============================================================================
-- Bang `web_admin_log` - nhat ky moi thao tac lam tren Trang quan tri web
-- (web/admin/). Chay MOT LAN vao database kpah_db truoc khi dung trang admin.
--
--     mysql -u root kpah_db < sql/them_bang_web_admin_log.sql
--   hoac tren Windows:
--     vps\win\vps-sql.bat < sql\them_bang_web_admin_log.sql
--
-- Bang nay dung cho HAI viec:
--   1. Ghi lai ai lam gi (khoa tai khoan, tao giftcode, doi mat khau...) - de
--      con soi lai khi co tranh cai, va de biet mot tai khoan admin co bi lo
--      hay khong.
--   2. Chan do mat khau: trang dang nhap dem so dong `dang_nhap_that_bai` cua
--      cung mot dia chi IP trong 15 phut gan nhat, qua 8 lan thi khoa 15 phut.
--      Khong co bang nay thi no lui ve dem trong session (yeu hon nhieu, vi ke
--      tan cong chi can xoa cookie).
--
-- Bang khong bao gio duoc xoa tu dong. Muon don bot dong cu:
--     DELETE FROM kpah_db.web_admin_log WHERE thoi_gian < DATE_SUB(NOW(), INTERVAL 180 DAY);
-- ============================================================================

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

-- Kiem tra lai: phai ra 1 dong.
SELECT COUNT(*) AS co_bang FROM information_schema.tables
 WHERE table_schema = 'kpah_db' AND table_name = 'web_admin_log';
