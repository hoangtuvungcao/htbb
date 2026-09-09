-- ============================================================================
-- Cot `userId` trong `giftcode_log` - chan nhan mot ma giftcode nhieu lan bang nhieu
-- nhan vat cung mot tai khoan.
--
-- KHONG BAT BUOC CHAY TAY: server game (src/data/DatabaseGiftCode.chuanBiCotUserId(),
-- goi tu TeamServer luc khoi dong, TRUOC khi nhan nguoi choi) tu ALTER TABLE neu thieu
-- cot, roi tu doi chieu userId cho cac dong cu dua theo ten nhan vat con ton tai trong
-- tob_char. File nay co de:
--   1. bo kiem web/admin/test/kiem_schema.php doi chieu duoc ten cot,
--   2. ai muon co cot truoc khi khoi dong lai server thi chay tay duoc.
--
--     mysql -u root kpah_db < sql/them_cot_userid_giftcode_log.sql
--
-- VI SAO CAN (09/09/2026): truoc day server chan nhan lai giftcode bang dieu kien
-- "player = <ten nhan vat>" trong giftcode_log (src/data/DatabaseGiftCode.java,
-- newDoGetGiftCode) - mot tai khoan nhieu nhan vat thi nhan duoc nhieu lan cung mot ma.
-- Sua lai chan theo `userId` (tai khoan), nen bang cu can them cot nay va doi chieu lai
-- du lieu da co de khong mo lai ke ho cho nhung tai khoan da tung nhan truoc do.
-- ============================================================================

ALTER TABLE giftcode_log
  ADD COLUMN userId INT(11) NOT NULL DEFAULT 0 AFTER player;

-- Doi chieu userId cho cac dong da nhan tu truoc, dua theo ten nhan vat con ton tai
-- trong tob_char. Nhan vat da bi xoa thi userId giu nguyen 0 - khong con hai vi nhan
-- vat do khong con de nhan lai. Chi sua dong con userId = 0 nen chay lai nhieu lan
-- khong hai gi (idempotent).
UPDATE giftcode_log gl
JOIN tob_char tc ON tc.charname = gl.player
SET gl.userId = tc.userId
WHERE gl.userId = 0;

-- Kiem tra lai: phai ra 1.
SELECT COUNT(*) AS co_cot FROM information_schema.columns
 WHERE table_schema = 'kpah_db' AND table_name = 'giftcode_log' AND column_name = 'userId';
