-- ============================================================================
-- UNIQUE KEY(giftcode,userId) tren `giftcode_log` - luoi an toan thu hai o tang DB
-- cho dieu kien "tai khoan nay da nhan ma nay chua".
--
-- KHONG BAT BUOC CHAY TAY: server game (src/data/DatabaseGiftCode.chuanBiKhoaDuyNhatGiftCodeLog(),
-- goi tu TeamServer luc khoi dong cung nhom voi chuanBiCotUserId()) tu ALTER TABLE them khoa
-- nay neu chua co. File nay chi can khi server tu them THAT BAI (log dong "[GIFTCODE] Chua
-- them duoc UNIQUE KEY...") vi du lieu cu con dong (giftcode,userId) trung nhau.
--
--     mysql -u root kpah_db < sql/them_khoa_duy_nhat_giftcode_log.sql
--
-- VI SAO CAN (15/09/2026): newDoGetGiftCode() kiem "da nhan chua" bang SELECT roi moi INSERT -
-- khong nguyen tu. Hai request REDEEM gan nhu dong thoi cho cung mot userId ve ly thuyet co
-- the cung SELECT thay "chua dung" truoc khi ben nao kip INSERT. Thuc te dang bi chan o tang
-- dang nhap (mot userId chi online duoc 1 nhan vat cung luc), nhung khoa DB giu an toan ke ca
-- sau nay logic do doi khac ma khong ai nho ra cho nay.
--
-- VI SAO CO THE CON DONG TRUNG: chuanBiCotUserId() doi chieu userId cho du lieu cu dua theo
-- ten nhan vat CON TON TAI trong tob_char; nhan vat DA BI XOA thi userId giu nguyen 0. Neu
-- nhieu nhan vat da-bi-xoa tung nhan cung mot ma giftcode, cac dong do gio deu co
-- (giftcode, userId=0) giong het nhau -> ALTER TABLE ADD UNIQUE se bao loi duplicate entry.
-- ============================================================================

-- 1) Liet ke cac cap (giftcode,userId) dang trung (chi de xem truoc, khong doi gi).
SELECT giftcode, userId, COUNT(*) AS soDongTrung
  FROM giftcode_log
 GROUP BY giftcode, userId
HAVING COUNT(*) > 1;

-- 2) Don trung: CHI xoa cac dong trung o userId=0 (nhan vat da bi xoa, khong con ai de nhan
--    lai nen khong mat lich su co y nghia), giu lai dong id NHO NHAT trong moi nhom trung.
--    KHONG dong nao co userId != 0 bi dong nay dung toi - neu van con trung o userId != 0
--    (tai khoan con song) thi phai tu tay xem lai truoc, KHONG chay xoa tu dong cho truong
--    hop do.
DELETE gl1 FROM giftcode_log gl1
JOIN giftcode_log gl2
  ON gl1.giftcode = gl2.giftcode
 AND gl1.userId = gl2.userId
 AND gl1.userId = 0
 AND gl1.id > gl2.id;

-- 3) Kiem lai: phai ra 0 dong (neu con dong nao o day, tuc van con trung o userId != 0 -
--    DUNG lai, khong chay buoc 4, bao nguoi quan tri xem tay).
SELECT giftcode, userId, COUNT(*) AS soDongTrung
  FROM giftcode_log
 GROUP BY giftcode, userId
HAVING COUNT(*) > 1;

-- 4) Them khoa (idempotent: bo qua neu da co).
ALTER TABLE giftcode_log
  ADD UNIQUE KEY uk_giftcode_userid (giftcode(191), userId);

-- 5) Kiem tra lai: phai ra 1.
SELECT COUNT(*) AS co_khoa FROM information_schema.statistics
 WHERE table_schema = 'kpah_db' AND table_name = 'giftcode_log' AND index_name = 'uk_giftcode_userid';
