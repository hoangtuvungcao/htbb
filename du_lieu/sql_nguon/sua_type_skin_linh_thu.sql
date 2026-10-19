-- =====================================================================================
--  SUA TYPE SKIN LINH THU: 19 -> 30
-- =====================================================================================
--
--  CHAY FILE NAY NEU BAN DA CHAY du_lieu/sql_nguon/skin_linh_thu.sql BAN CU (type 19).
--  Neu chua tung chay lan nao thi bo qua, cu chay du_lieu/sql_nguon/skin_linh_thu.sql la du.
--
--      mysql -u root -p <ten_db> < du_lieu/sql_nguon/sua_type_skin_linh_thu.sql
--
--  Roi khoi dong lai server (bang template item chi nap luc bat may).
--
-- -------------------------------------------------------------------------------------
--  VI SAO PHAI DOI
-- -------------------------------------------------------------------------------------
--  Bang "Trang bi thu" co 10 o, client tra thu tu o trong mang idWearAnimal. HAI BAN
--  CLIENT DANH SO COT PHAI KHAC NHAU:
--
--      ban JAR   : idWearAnimal = {18, 30, 14, 31, 15, 32, 16, 33, 17, 34}
--      ban Unity : idWearAnimal = {18, 19, 14, 20, 15, 21, 16, 22, 17, 23}   (truoc khi sua)
--
--  Ban Unity duoc port tu mot ban jar cu hon nen giu cach danh so cu. Dat type 19 thi
--  ban Unity ve dung, con ban JAR tra posPaintWearing[19] = (34, 92) — do la o PHI PHONG
--  cua bang NHAN VAT — nen icon skin roi xuong khoang trong giua hai cot o goc duoi.
--
--  Da thong nhat theo cach danh so cua ban JAR (30..34) vi:
--    1. ban JAR chay dung ngay, KHONG phai dong goi lai jar client cho nguoi choi;
--    2. type 19 von la type cua PHI PHONG nhan vat (real.Map.isPhiPhong) — dung chung mot
--       so cho hai thu khac nhau la nguon goc cua ca lo loi;
--    3. type 30 khong dung voi do mac nao: trong data_item chi co dong id=30 "ech quy",
--       do la ban ghi LOAI THU chu khong phai vat pham mac duoc.
--
--  Chi doi mot cot `type` cua 5 dong template. KHONG dong toi vat pham nguoi choi dang
--  giu: bang `item` tham chieu template qua id (905..909), id khong doi nen skin da phat
--  ra van con nguyen, van dang mac, khong ai mat gi.
-- =====================================================================================

UPDATE `data_item` SET `type` = 30 WHERE `id` BETWEEN 905 AND 909 AND `type` = 19;

-- Kiem tra: phai ra dung 5 dong, cot type deu bang 30.
SELECT `id`, `name`, `type`, `xstart` FROM `data_item` WHERE `id` BETWEEN 905 AND 909 ORDER BY `id`;

-- Kiem tra an toan: khong duoc dung phai template nao khac. Phai ra 0 dong.
SELECT `id`, `name`, `type` FROM `data_item`
 WHERE `type` = 30 AND `id` NOT BETWEEN 905 AND 909 AND `id` <> 30;
