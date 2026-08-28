-- ============================================================================
-- Sua icon hanh trang cua "ao brazin nam" (item 609) va "ao brazin nu" (610).
--
-- LOI: ca hai item deu ghi xstart = 519, ma map/icon/519.png la cai RUONG VANG
-- 16x16 - von la icon cua item 611 "Ruong bao tieu". Nen trong hanh trang hai
-- cai ao hien ra hinh cai ruong. Bo do khi MAC len nguoi van dung, vi phan do
-- lay tu cot atb7/atb8 = 52/53 (map/c/body/52-53 va map/c/leg/52-53) chu khong
-- lien quan gi den xstart.
--
-- Ca hai ban dump deu ghi 519 (kpah_db.sql va sql/gamedb7_clean.sql) nen day la
-- loi du lieu co tu lau, khong phai moi hong.
--
-- ANH MOI: map/icon/854.png (nam) va map/icon/826.png (nu), 18x18.
-- KHONG ve tay. Ghep tu chinh anh bo do co san trong repo:
--   than ao  <- map/c/body/52 va 53  (ao vang, co xanh la, tay ao do)
--   quan     <- map/c/leg/52 va 53   (quan xanh duong)
-- Hai file do co 2 byte dau la toa do ve, phan con lai moi la PNG.
-- Cach ghep: cat khung chinh dien, thu nho bang nearest-neighbour de giu net
-- pixel, xep than ao 11 hang tren + quan 7 hang duoi - dung bo cuc cua bon icon
-- ao bong da san co (map/icon/699, 700, 702, 704).
--
-- Vi sao chon o 854 va 826: ca hai deu CHUA co file trong map/icon va KHONG
-- item nao tro toi (ke ca cot ystart). Rieng 854 con trong o ca ban KPAH khac
-- trong Downloads. Luu y: 826 co file ben ban Downloads (mot anh nhan vat co
-- lon) - dung copy de len.
--
-- Chay tren VPS sau khi da day map/icon/854.png va 826.png len.
-- ============================================================================

-- Kiem tra truoc khi sua: phai thay ca 609, 610, 611 deu dang la 519.
SELECT id, name, xstart FROM data_item WHERE id IN (609, 610, 611);

-- Sua. KHONG dung toi 611 - no dung 519 la dung.
UPDATE data_item SET xstart = 854 WHERE id = 609;   -- ao brazin nam
UPDATE data_item SET xstart = 826 WHERE id = 610;   -- ao brazin nu

-- Kiem tra lai: 609 -> 854, 610 -> 826, 611 van 519.
SELECT id, name, xstart FROM data_item WHERE id IN (609, 610, 611);
