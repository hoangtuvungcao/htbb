-- ==================================================================================
--  BANG BAT/TAT SU KIEN  (tob_su_kien)
-- ==================================================================================
--
--  KHONG BAT BUOC CHAY: may chu tu tao bang nay lan dau khoi dong
--  (data/DatabaseSuKien.taoBang). De o day de chay truoc cho yen tam va de xem duoc
--  cau truc ma khong phai doc Java.
--
--      mysql -u root -p <ten_db> < du_lieu/sql_nguon/them_bang_su_kien.sql
--
--  DUNG DE LAM GI. Truoc ngay 30/08/2026, moi su kien trong game duoc mo bang cach so
--  NGAY HE THONG voi mot moc GHI CUNG trong src/real/Char.java, vi du:
--
--      return getDayTime(0L).compareTo("2020-11-01 00:00:00") < 0;   // Halloween
--
--  Moc nao cung nam trong 2015..2024 nen den nay moi su kien deu tat vinh vien, khong
--  co cach nao mo lai ngoai sua ma nguon roi build lai jar. Gio moi su kien la mot dong
--  trong bang nay, bat luc nao cung duoc, dat o trang quan tri /admin/sukien (HTBB-Web).
--
--  AI GHI COT NAO - hai ben khong dam len nhau:
--
--      Trang quan tri ghi :  bat, sua_boi, sua_luc, mo_luc, dong_luc
--      May chu game ghi   :  ten, nhom, mo_ta, dang_bat, may_chu_luc
--
--  ghiAnhChup() KHONG BAO GIO ghi cot `bat`, nen khoi dong lai khong lam mat thiet lap.
--  Nguoc lai trang web khong can biet game co nhung su kien nao: moi lan khoi dong,
--  may chu tu dang danh sach len bang. Them su kien moi trong Java la no tu hien tren
--  web, khong phai sua PHP.
--
--  NGOAI LE: LichSuKien co ghi `bat` khi mot moc trong `mo_luc`/`dong_luc` toi gio - do
--  chinh la viec cua lich hen. Khi do no ghi luon sua_boi='lich hen' va sua_luc=NOW()
--  giong het trang web, roi XOA moc da chay de moi moc chi chay dung mot lan.
--
--  PHAI KHOI DONG LAI MAY CHU sau khi doi. Rat nhieu tac dung cua su kien duoc tinh
--  MOT LAN luc khoi dong chu khong hoi lai moi lan dung: bang shop (Map.loadShop dat
--  SELL/NOT_SELL theo Char.isSuKien*), Map.isSale, danh sach npc, bang vat pham...
--  So sanh `sua_luc` voi `may_chu_luc` la cach biet mot thiet lap da vao game chua.
--
--  TU KHOI DONG LAI: mac dinh may chu tu lo viec do - gom cac thay doi trong 2 phut,
--  bao trong game 60 giay, roi bat co 5h_systems.restart de BaoTriDaily luu du lieu va
--  thoat; systemd bat lai sau 10 giay (Restart=always). Tat bang sv.suKienTuKhoiDongLai=0
--  trong server.ini. Xem src/real/LichSuKien.java.
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
  `mo_luc`      DATETIME     NULL COMMENT 'WEB ghi: hen bat su kien luc nay, xong thi server xoa',
  `dong_luc`    DATETIME     NULL COMMENT 'WEB ghi: hen tat su kien luc nay, xong thi server xoa',
  PRIMARY KEY (`ma`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------------------
--  BANG DA CO TU TRUOC 30/08/2026 THI THIEU HAI COT LICH
-- ---------------------------------------------------------------------------------
--  CREATE TABLE IF NOT EXISTS o tren khong sua duoc bang da ton tai. May chu tu ALTER
--  luc khoi dong (DatabaseSuKien.themCotThieu), nhung neu muon lam truoc bang tay:
--
--      ALTER TABLE `tob_su_kien` ADD COLUMN `mo_luc`   DATETIME NULL;
--      ALTER TABLE `tob_su_kien` ADD COLUMN `dong_luc` DATETIME NULL;
--
--  Chay lai lan hai se bao "Duplicate column name" - do la binh thuong, bo qua.
--  (Khong viet san "ADD COLUMN IF NOT EXISTS" vi cu phap do chi MariaDB moi co.)

-- ---------------------------------------------------------------------------------
--  KIEM TRA
-- ---------------------------------------------------------------------------------
-- SELECT ma, ten, bat, dang_bat, mo_luc, dong_luc, sua_boi, sua_luc, may_chu_luc
--   FROM tob_su_kien ORDER BY nhom, ma;
--
-- Dong nao dang cho khoi dong lai:
-- SELECT ma, ten, bat, dang_bat FROM tob_su_kien
--  WHERE bat <> dang_bat AND sua_luc IS NOT NULL
--    AND (may_chu_luc IS NULL OR sua_luc > may_chu_luc);
--
-- Bat tay mot su kien ma khong qua web (van phai khoi dong lai may chu):
-- INSERT INTO tob_su_kien (ma, bat) VALUES ('halloween', 1)
--   ON DUPLICATE KEY UPDATE bat = 1, sua_boi = 'sql', sua_luc = NOW();
--
-- Hen mo Halloween dung 0h ngay 25/10 va tu tat cuoi ngay 02/11:
-- UPDATE tob_su_kien SET mo_luc = '2026-10-25 00:00:00', dong_luc = '2026-11-02 23:59:00'
--  WHERE ma = 'halloween';
