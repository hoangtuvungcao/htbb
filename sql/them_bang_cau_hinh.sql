-- ============================================================================
-- Bang `cau_hinh` - 23 setting server.ini nap lai duoc luc dang chay (xem
-- src/server/CauHinh.java), nay chuyen tu file sang SQL de sua duoc qua trang
-- quan tri web (web/admin/cauhinh.php) thay vi phai vao game go lenh "setting".
--
-- KHONG BAT BUOC CHAY TAY: ca server game (CauHinh.dongBoLucKhoiDong(), goi tu
-- TeamServer luc khoi dong) lan trang web deu tu CREATE TABLE IF NOT EXISTS. File
-- nay co de:
--   1. bo kiem web/admin/test/kiem_schema.php doi chieu duoc ten cot,
--   2. ai muon co bang truoc khi khoi dong lai server thi chay tay duoc.
--
--     mysql -u root kpah_db < sql/them_bang_cau_hinh.sql
--
-- VI SAO CAN (09/09/2026): server.ini la file rieng tren tung may (khong nam trong
-- git), sua phai SSH vao VPS. 23 khoa nay von da nap lai duoc luc dang chay (CauHinh)
-- nhung truoc gio chi sua duoc qua bang "setting" TRONG GAME (real/AdminMenu.java),
-- khong sua tu xa qua web duoc. Chuyen sang SQL thi web va game cung doc/ghi chung
-- MOT nguon: web sua xong, server game tu ap dung trong vong toi da 10 giay (vong
-- lap CauHinh.batDau(), khong can restart, khong can bam nut "nap lai").
--
-- KHOA NAO DUOC CHUYEN: dung 23 khoa CauHinh.napLai() da nap lai duoc tu truoc -
-- CHI vay thoi, khong dong toi ~20 khoa bat buoc de ket noi CSDL/mo cong (con o
-- server.ini vi la van de "con ga qua trung": can chung de ket noi duoc CSDL truoc
-- da). vps/server.ini.tmpl VAN GIU nguyen 23 khoa nay lam GIA TRI KHOI TAO LAN DAU
-- (dung cung tinh than voi sv.isSuKien* cu) - bang cau_hinh rong thi server tu
-- chep gia tri tu file vao (INSERT IGNORE) o lan khoi dong dau tien, sau do SQL moi
-- la nguon that.
-- ============================================================================

CREATE TABLE IF NOT EXISTS `cau_hinh` (
  `khoa`    varchar(64) NOT NULL COMMENT 'ten setting, khop hang so K_* trong src/server/CauHinh.java',
  `gia_tri` varchar(255) NOT NULL DEFAULT '',
  `sua_boi` varchar(50) DEFAULT NULL COMMENT 'ten dang nhap admin (web) hoac "game" (bang setting trong game)',
  `sua_luc` datetime DEFAULT NULL,
  PRIMARY KEY (`khoa`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Kiem tra lai: phai ra 1.
SELECT COUNT(*) AS co_bang FROM information_schema.tables
 WHERE table_schema = 'kpah_db' AND table_name = 'cau_hinh';
