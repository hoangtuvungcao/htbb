-- ==================================================================================
--  KHO XU/LUONG O NPC TONG QUAN  (tob_char.kho_xu, kho_luong, kho_luong_khoa)
-- ==================================================================================
--
--  KHONG BAT BUOC CHAY: may chu TU THEM 3 cot nay luc khoi dong neu chua co
--  (Database.themCotKhoXuLuong(), goi ngay canh themCotCongTacHienThi() trong
--  server/TeamServer.java - CUNG LY DO: DatabaseNhanVat.saveChar/saveCharAuto ghi
--  thang cac cot nay, thieu cot se lam UPDATE nem loi va saveChar KHOA NHAN VAT ngay
--  lan luu dau tien). De o day de chay truoc cho yen tam, hoac xem cau truc ma khong
--  phai doc Java.
--
--      mysql -u root -p <ten_db> < sql/them_cot_kho_xu_luong.sql
--
--  DUNG DE LAM GI. Them 21/09/2026 theo yeu cau nguoi dung: cho gui xu/luong "dang giu"
--  vao mot kho rieng cua tung nhan vat (qua NPC Tong Quan, npc -10, muc "Kho xu/luong"),
--  rut ra lai bat cu luc nao. Ly do chinh: luong nhat duoc tu train quai bi chan boi
--  Char.MAX_LUONG_MO_DANG_GIU / MAX_LUONG_KHOA_DANG_GIU (50k dang giu trong nguoi) - xem
--  Char.addLuongTrainQuai. Gui bot vao kho de "dang giu" tut xuong duoi tran thi train
--  tiep se lai nhan duoc luong. Khong phi, khong han muc gui/rut. Xem real/KhoXuLuong.java.
--
--  AN TOAN CHAY LAI NHIEU LAN: ALTER TABLE ADD COLUMN tren cot da co se bao loi
--  "Duplicate column name" - vo hai, bo qua (khong dung "ADD COLUMN IF NOT EXISTS" vi cu
--  phap do chi MariaDB moi co, khong biet may chu nao chay ban nao).
-- ==================================================================================

ALTER TABLE `tob_char` ADD COLUMN `kho_xu`         BIGINT(20) NOT NULL DEFAULT 0 COMMENT 'Kho xu o NPC Tong Quan';
ALTER TABLE `tob_char` ADD COLUMN `kho_luong`      INT(11)    NOT NULL DEFAULT 0 COMMENT 'Kho luong mo o NPC Tong Quan';
ALTER TABLE `tob_char` ADD COLUMN `kho_luong_khoa` INT(11)    NOT NULL DEFAULT 0 COMMENT 'Kho luong khoa o NPC Tong Quan';

-- ---------------------------------------------------------------------------------
--  KIEM TRA
-- ---------------------------------------------------------------------------------
-- SELECT charname, gold, luong, luonglock, kho_xu, kho_luong, kho_luong_khoa
--   FROM tob_char WHERE kho_xu > 0 OR kho_luong > 0 OR kho_luong_khoa > 0;
