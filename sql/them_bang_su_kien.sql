-- ==================================================================================
--  BANG BAT/TAT SU KIEN  (tob_su_kien)
-- ==================================================================================
--
--  KHONG BAT BUOC CHAY: may chu tu tao bang nay lan dau khoi dong
--  (data/DatabaseSuKien.taoBang). De o day de chay truoc cho yen tam va de xem duoc
--  cau truc ma khong phai doc Java.
--
--      mysql -u root -p <ten_db> < sql/them_bang_su_kien.sql
--
--  DUNG DE LAM GI. Truoc ngay 30/08/2026, moi su kien trong game duoc mo bang cach so
--  NGAY HE THONG voi mot moc GHI CUNG trong src/real/Char.java, vi du:
--
--      return getDayTime(0L).compareTo("2020-11-01 00:00:00") < 0;   // Halloween
--
--  Moc nao cung nam trong 2015..2024 nen den nay moi su kien deu tat vinh vien, khong
--  co cach nao mo lai ngoai sua ma nguon roi build lai jar. Gio moi su kien la mot dong
--  trong bang nay, bat luc nao cung duoc, dat o trang quan tri /admin/sukien.php.
--
--  AI GHI COT NAO - hai ben khong dam len nhau:
--
--      Trang quan tri ghi :  bat, sua_boi, sua_luc
--      May chu game ghi   :  ten, nhom, mo_ta, dang_bat, may_chu_luc
--
--  May chu KHONG BAO GIO ghi cot `bat`, nen khoi dong lai khong lam mat thiet lap.
--  Nguoc lai trang web khong can biet game co nhung su kien nao: moi lan khoi dong,
--  may chu tu dang danh sach len bang (DatabaseSuKien.ghiAnhChup). Them su kien moi
--  trong Java la no tu hien tren web, khong phai sua PHP.
--
--  PHAI KHOI DONG LAI MAY CHU sau khi doi. Rat nhieu tac dung cua su kien duoc tinh
--  MOT LAN luc khoi dong chu khong hoi lai moi lan dung: bang shop (Map.loadShop dat
--  SELL/NOT_SELL theo Char.isSuKien*), Map.isSale, danh sach npc, bang vat pham...
--  So sanh `sua_luc` voi `may_chu_luc` la cach trang web biet co can nhac hay khong.
-- ==================================================================================

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
  PRIMARY KEY (`ma`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------------------
--  KIEM TRA
-- ---------------------------------------------------------------------------------
-- SELECT ma, ten, bat, dang_bat, sua_boi, sua_luc, may_chu_luc FROM tob_su_kien ORDER BY nhom, ma;
--
-- Bat tay mot su kien ma khong qua web (van phai khoi dong lai may chu):
-- INSERT INTO tob_su_kien (ma, bat) VALUES ('halloween', 1)
--   ON DUPLICATE KEY UPDATE bat = 1, sua_boi = 'sql', sua_luc = NOW();
