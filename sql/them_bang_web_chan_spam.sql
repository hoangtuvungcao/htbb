-- ============================================================================
-- Bang `web_chan_spam` - dem so lan mot dia chi IP lam mot viec tren website,
-- de chan lam hang loat. Chay MOT LAN vao database htbb_db:
--
--     mysql -u root htbb_db < sql/them_bang_web_chan_spam.sql
--
-- VI SAO CAN: truoc day trang dang ky (web PHP cu) khong co gioi han nao. Token CSRF KHONG
-- phai gioi han - mot script chi can tai trang de lay token roi gui form, lap
-- lai bao nhieu lan tuy thich. Moi lan la mot dong moi trong account.team_user:
-- bang phinh ra, ten tai khoan dep bi chiem het, va thong ke nguoi choi thanh
-- vo nghia. Trang nap tien cung vay: moi lan bam la mot dong trong tob_nap_moc.
--
-- Thieu bang nay thi website VAN CHAY BINH THUONG, chi lui ve dem trong session
-- (yeu hon nhieu, vi xoa cookie la dem lai tu dau). Trang Chan doan cua khu
-- quan tri co mot muc bao ro dang o che do nao.
--
-- Don bot dong cu (khong bat buoc, bang tu nho):
--     DELETE FROM htbb_db.web_chan_spam WHERE thoi_gian < DATE_SUB(NOW(), INTERVAL 7 DAY);
-- ============================================================================

CREATE TABLE IF NOT EXISTS `web_chan_spam` (
  `id`        bigint(20)  NOT NULL AUTO_INCREMENT,
  `ip`        varchar(45) NOT NULL DEFAULT ''  COMMENT 'IPv6 dai nhat 45 ky tu',
  `hanh_dong` varchar(30) NOT NULL DEFAULT ''  COMMENT 'dang_ky, tao_don_nap...',
  `thoi_gian` datetime    NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_dem` (`ip`, `hanh_dong`, `thoi_gian`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Kiem tra lai: phai ra 1.
SELECT COUNT(*) AS co_bang FROM information_schema.tables
 WHERE table_schema = 'htbb_db' AND table_name = 'web_chan_spam';
