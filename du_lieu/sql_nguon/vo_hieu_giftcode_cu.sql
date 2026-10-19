-- Vo hieu TOAN BO giftcode KIEU CU (bang `giftcode`) - kiem toan web 19/10/2026.
--
-- May chu chay che do dai han (sv.lau_dai = 1, luon bat): DatabaseGiftCode chuyen moi ma nguoi choi go sang
-- GiftcodeAnToan (bang tob_giftcode), bang `giftcode` cu KHONG con duoc doc. Trang /admin/giftcode da bo phan
-- "ma kieu cu". Bang cu tren may that van con cac ma thu nghiem thoi KPAH (vd `123` = DN:500000:500000,
-- `oke212` = 1000 luong khoa + vat pham, `vailua` = 2 x 10.000 ngoc, `tt` = 2.000.000 luong) - neu ai do tat
-- sv.lau_dai bang server.ini (khan cap) thi cac ma nay song lai. Script nay khoa han chung:
--   type = 1      -> DatabaseGiftCode chi tim ma type = 0, ma tro nen vo hinh;
--   limit_use = 0 -> het luot (khong phai "vo han").
-- Khong xoa dong: giftcode_log van tham chieu ten ma. Chay lai nhieu lan khong sao.
SET NAMES utf8;
SELECT id, giftcode, xu, luong, luongLock, limit_use, type FROM giftcode WHERE type = 0 AND limit_use <> 0;
UPDATE giftcode SET type = 1, limit_use = 0 WHERE type = 0 OR limit_use <> 0;
