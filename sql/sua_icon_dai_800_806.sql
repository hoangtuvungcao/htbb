-- ============================================================================
-- Dua 7 vat pham RA KHOI dai icon 800-806 (dai nay bi chiem, khong dung duoc).
--
-- TRIEU CHUNG: mo hanh trang thay mot phan UI bi ANH BAN DO THE GIOI de len,
-- hoac o vat pham trong tron va giao dien vo tu do tro di.
--
-- NGUYEN NHAN: DataGame.sendByteImg chan cung dai 800-806 va phuc vu tu mang
-- imgOther (= map/other/0..5.png) chu KHONG doc map/icon/<id>.png:
--
--     if (idImg < nIconItem && !(idImg >= 800 && idImg <= 806)) {
--         m.dos.write(allImg[idImg]);
--     } else if (idImg >= 800 && idImg <= 806) {
--         int k = idImg - 800;
--         if (k >= 0 && k < imgOther.length && imgOther[k] != null) {
--             m.dos.write(imgOther[k]);
--         }
--         // k = 6 (tuc idImg 806) roi vao day va KHONG ghi gi ca -> goi RONG
--     }
--
-- Hau qua theo tung id:
--   800       -> map/other/0.png la BAN DO THE GIOI co lon. Client ve icon bang
--                drawImage(img, x, y, HCENTER|VCENTER) nen anh to phu kin UI.
--   801..805  -> ra map/other/1..5.png, sai anh.
--   806       -> imgOther chi co 6 phan tu (0..5) nen k=6 truot dieu kien, server
--                gui goi KHONG CO BYTE ANH NAO. Ben client, MessageHandler case -51
--                da dat img.isLoad = false TRUOC khi goi Image.createImage; anh rong
--                lam createImage nem loi, img.img giu nguyen null. Tu do moi lan
--                paintIcon deu NullPointerException -> UI thung cho den khi tat han
--                client.
--
-- Bay vat pham 872-878 deu dang tro vao dai nay. Doi sang o trong an toan, va
-- da chep san anh dung sang o moi (map/icon/815-819, 827, 828).
--
-- LUU Y VE HAI BAN DUMP: sql/gamedb7_clean.sql khop voi DB dang chay
-- (872 -> 800). Con kpah_db.sql (dump 27/07/2026) da CU, no ghi 872 -> 807.
-- Khi tra cuu icon hay dung ban clean.
--
-- Chay tren VPS sau khi da day thu muc map/ len.
-- ============================================================================

-- Truoc khi sua: phai thay 872..878 dang nam trong khoang 800..806.
SELECT id, name, xstart FROM data_item WHERE id BETWEEN 872 AND 882 ORDER BY id;

UPDATE data_item SET xstart = 815 WHERE id = 872;   -- Mat na ong dia
UPDATE data_item SET xstart = 816 WHERE id = 873;   -- Capybara
UPDATE data_item SET xstart = 817 WHERE id = 874;   -- Ho ve lan su tu 4x lv 1
UPDATE data_item SET xstart = 818 WHERE id = 875;   -- Ho ve lan su tu 4x lv 2
UPDATE data_item SET xstart = 819 WHERE id = 876;   -- Ho ve lan su tu 4x lv 3
UPDATE data_item SET xstart = 827 WHERE id = 877;   -- Ho ve lan su tu 5x lv 1
UPDATE data_item SET xstart = 828 WHERE id = 878;   -- Ho ve lan su tu 5x lv 2

-- KHONG dung toi 879-882: chung dung icon 807-810, ngoai dai bi chan, van chay.

-- Kiem tra lai: khong con id nao trong 872..882 co xstart trong khoang 800..806.
SELECT id, name, xstart FROM data_item WHERE id BETWEEN 872 AND 882 ORDER BY id;

-- Kiem tra rong hon: bat ky vat pham nao con ket trong dai bi chan deu se pha UI.
SELECT id, name, xstart FROM data_item WHERE xstart BETWEEN 800 AND 806;
