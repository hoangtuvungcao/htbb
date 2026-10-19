-- ==================================================================================
--  VE MO RONG HANH TRANG  (tob_char.o_mo_rong + mon cua hang luong ve_mo_rong_tui)
-- ==================================================================================
--
--  Them 19/10/2026. Chay lai bao nhieu lan cung duoc (MariaDB 10.3 / MySQL 5.x, utf8):
--
--      mysql -u root -p <ten_db> < sql/them_ve_mo_rong.sql
--
--  KHONG BAT BUOC CHAY phan cot: may chu TU THEM cot o_mo_rong luc khoi dong neu chua co
--  (Database.themCotVeMoRong() trong server/TeamServer.java). Cot nay luu bang cau UPDATE
--  rieng (Database.luuCotMoRong) nen thieu cot KHONG khoa nhan vat, chi khong giu duoc o da mo.
--
--  Suc chua hanh trang = 42 x so trang (maxBag, mua bang luong nhu cu) + o_mo_rong.
--  o_mo_rong tang +10 moi lan dung "Ve mo rong hanh trang" (nguyen lieu id 270, hien trong
--  tab nguyen lieu, bam "Dung") hoac mua thang bang xu o Kangaskhan thu kho (gia tang dan:
--  20.000 / 30.000 / 45.000 / 68.000 / 100.000 / 150.000 / 230.000 / 340.000 / 510.000 xu).
--  Tran: 126 o (= 3 trang khoi dau). Hang so: server/src/com/htbb/pokemon/MoRongHanhTrang.java.
--
--  Mau nguyen lieu (Vé mở rộng hành trang, id 270) nam trong ma server
--  (real/MapNapTaiNguyen.loadGemTemplate) - khong co bang du lieu, file nay khong can them.
-- ==================================================================================

SET NAMES utf8;

-- ---------------------------------------------------------------- 1. cot tob_char.o_mo_rong (idempotent)
SET @co_cot := (SELECT COUNT(*) FROM information_schema.COLUMNS
                 WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tob_char' AND COLUMN_NAME = 'o_mo_rong');
SET @lenh := IF(@co_cot = 0,
    'ALTER TABLE `tob_char` ADD COLUMN `o_mo_rong` INT NOT NULL DEFAULT 0 COMMENT ''O hanh trang le: ve mo rong / mua xu (real.MoRongTuiGame)''',
    'SELECT ''tob_char.o_mo_rong da co'' AS ghi_chu');
PREPARE lenh_mo_rong FROM @lenh;
EXECUTE lenh_mo_rong;
DEALLOCATE PREPARE lenh_mo_rong;

-- ---------------------------------------------------------------- 2. cua hang luong (he nap tien)
-- Chi chay duoc khi da nap sql/pokemon_nap_tien.sql (bang nap_cua_hang). Chua co bang thi lenh duoi bao
-- loi "Table doesn't exist" - vo hai. Bat / tat ban: cot dang_ban (web /admin/naptien, giong moi mon khac).
-- trao = mo_rong_tui (danh sach trang cu); server phan biet theo ma ve_mo_rong_tui -> trao 1 ve vao tui.
INSERT IGNORE INTO `nap_cua_hang` (`ma`,`ten`,`nhom`,`trao`,`gia_luong`,`han_ngay`,`gioi_han`,`nhom_xoay`,`giao_dich_duoc`,`co_chi_so`,`dang_ban`,`thu_tu`) VALUES
('ve_mo_rong_tui','Vé mở rộng hành trang (+10 ô)','tien_ich','mo_rong_tui',80,0,9,0,0,0,1,23);

-- ---------------------------------------------------------------------------------
--  KIEM TRA
-- ---------------------------------------------------------------------------------
-- SELECT charname, o_mo_rong FROM tob_char WHERE o_mo_rong > 0;
-- SELECT * FROM nap_cua_hang WHERE ma = 've_mo_rong_tui';
