-- ============================================================================
-- Bang `tob_log_online` - luu so nguoi online theo thoi gian, 5 phut mot mau.
-- Trang Tong quan cua khu quan tri doc bang nay de ve bieu do.
--
-- KHONG BAT BUOC CHAY TAY: server game tu tao bang nay luc khoi dong
-- (src/data/GhiSoOnline.java, cau CREATE TABLE IF NOT EXISTS). File nay co de:
--   1. xem duoc ten cot/cau truc ma khong phai doc ma nguon,
--   2. ai muon co bang truoc khi khoi dong lai server thi chay tay duoc.
--
--     mysql -u root htbb_db < sql/them_bang_tob_log_online.sql
--
-- VI SAO CAN: truoc day server chi biet so online TUC THOI
-- (SessionManager.instance.size(), dung o AdminPanel va o chan dang nhap), khong
-- luu lai gi ca. Nen khong tra loi duoc nhung cau rat co ban: "toi qua dong nhat
-- bao nhieu nguoi", "sau ban cap nhat hom truoc nguoi choi co bo di khong",
-- "gio nao dong nhat de dat su kien".
--
-- HAI COT SO, CO Y GIU CA HAI:
--   ket_noi  = so socket dang mo. Tinh ca nguoi con o man hinh dang nhap /
--              chon nhan vat, chua vao game.
--   nhan_vat = so nhan vat THAT dang trong game.
-- Chenh lech giua hai cot chinh la so nguoi ket noi duoc nhung chua vao duoc
-- game - rat huu ich khi chan doan (vi du khi dang nhap bi cham 5 giay vi
-- isFullConnection, hoac khi loi o buoc chon nhan vat).
--
-- KHOI LUONG: 288 dong/ngay, giu 30 ngay -> khoang 8.600 dong. Khong dang ke.
-- Server tu don dong cu hon 30 ngay moi 6 tieng, khong can cron rieng.
-- ============================================================================

CREATE TABLE IF NOT EXISTS `tob_log_online` (
  `id`        int(10) unsigned NOT NULL AUTO_INCREMENT,
  `thoi_gian` datetime         NOT NULL                COMMENT 'luc lay mau',
  `ket_noi`   int(11)          NOT NULL DEFAULT 0      COMMENT 'so socket dang mo',
  `nhan_vat`  int(11)          NOT NULL DEFAULT 0      COMMENT 'so nhan vat that trong game',
  PRIMARY KEY (`id`),
  KEY `idx_thoi_gian` (`thoi_gian`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Kiem tra lai: phai ra 1.
SELECT COUNT(*) AS co_bang FROM information_schema.tables
 WHERE table_schema = 'htbb_db' AND table_name = 'tob_log_online';
