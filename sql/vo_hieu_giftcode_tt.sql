-- GD2 buoc 1.10 (docs/pokemon/KE_HOACH_LAU_DAI_GD2.md, KIEM_TOAN_KINH_TE.md phat hien #5):
-- giftcode `tt` cho 2.000.000 luong + 2.000.000 luong khoa moi luot (con 998 luot) - mot ma
-- pha ca nen kinh te luong. limit_use = 0 nghia la "het luot" (DatabaseGiftCode bao "Ma Qua
-- Tang da het luot su dung."), KHONG phai "vo han". Khong xoa dong: giftcode_log van tham
-- chieu ten ma. Chay lai nhieu lan khong sao.
SET NAMES utf8;
UPDATE giftcode SET limit_use = 0 WHERE giftcode = 'tt';
