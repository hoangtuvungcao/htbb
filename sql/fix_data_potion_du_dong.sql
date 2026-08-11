-- =====================================================================
-- BU DU DONG CHO BANG data_potion (can du id 0..161)
--
--   mysql -u root -p kpah_db < sql/fix_data_potion_du_dong.sql
--   ... roi BAT LAI SERVER (bang nay chi doc luc khoi dong).
--
-- ---------------------------------------------------------------------
-- VI SAO PHAI DU DONG
-- Nhan vat co dung 162 o thuoc (Char.SO_O_THUOC, cot tob_char.potion luu 162 truong).
-- Nhung client lai cap phat o thuoc theo SO DONG cua bang data_potion: luc dang nhap
-- server gui potionTemplates2.size() -> Char.CHAR_POTION -> MainChar.listPotion, roi
-- khi doc goi hanh trang client gan thang listPotion[id] KHONG kiem tra bien.
--
-- Thieu dong -> nguoi choi giu thuoc id cao (vi du SACH KY NANG PET la id 111) se lam
-- client nem loi giua luc doc goi hanh trang. Hau qua khong chi mat mon thuoc do:
-- setInventory() khong chay, mang potions ben client giu nguyen do dai khoi tao 25,
-- roi vong dung danh sach tui do cham ra ngoai mang va nem tiep — ma ca vong do nam
-- trong MOT try duy nhat, dung TRUOC phan nguyen lieu / thu / vat pham. Ket qua:
-- NGUOI CHOI THAY CA HANH TRANG RONG.
--
-- Ban server tu 2026-08 da chan o Char.getAllPotion() (bo qua id vuot tran + ghi log)
-- nen khong con rong hanh trang nua, nhung mon thuoc do van khong hien duoc cho tan
-- khi bang co du dong.
--
-- ---------------------------------------------------------------------
-- KIEM TRA TRUOC / SAU
--   SELECT COUNT(*) FROM data_potion;                        -- phai = 162
--   SELECT MAX(id)  FROM data_potion;                        -- phai = 161
--   SELECT id FROM data_potion WHERE id=111;                 -- phai co dong nay
-- =====================================================================

-- Bang tam chua du 162 so 0..161 (khong dung thu tuc, chay duoc tren MySQL/MariaDB cu)
DROP TEMPORARY TABLE IF EXISTS tmp_so;
CREATE TEMPORARY TABLE tmp_so (n INT PRIMARY KEY);
INSERT INTO tmp_so (n)
SELECT a.i + b.i * 10 + c.i * 100 AS n
FROM      (SELECT 0 i UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
           UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) a
CROSS JOIN (SELECT 0 i UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
           UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) b
CROSS JOIN (SELECT 0 i UNION SELECT 1) c
WHERE a.i + b.i * 10 + c.i * 100 <= 161;

-- Chi them dong con THIEU, khong dung tay vao dong da co (giu nguyen ten, icon, gia).
-- Dong bu la o thuoc trong: khong ten, khong icon, khong ban, khong trao doi.
INSERT INTO data_potion (id, idImage, delay, isTrade, name, name2, price, recovered)
SELECT s.n, 0, 0, 0, '', '', 0, 0
FROM tmp_so s
LEFT JOIN data_potion p ON p.id = s.n
WHERE p.id IS NULL;

DROP TEMPORARY TABLE IF EXISTS tmp_so;

-- ---------------------------------------------------------------------
-- Mo ta cho sach ky nang pet (potion 111). Chay lai duoc nhieu lan.
-- idImage 95 -> map/images_potion/95.png (anh nay cung chi doc luc khoi dong server).
-- ---------------------------------------------------------------------
UPDATE data_potion
   SET idImage = 95, delay = 0, isTrade = 1,
       name = 'Sách kỹ năng pet', name2 = 'sachkynangpet',
       price = 0, recovered = 0
 WHERE id = 111;

SELECT COUNT(*) AS so_dong_hien_co, MAX(id) AS id_lon_nhat FROM data_potion;
SELECT id, idImage, delay, isTrade, name, name2 FROM data_potion WHERE id = 111;
