-- =====================================================================
-- BU LO ID TRONG BANG data_item  (lo id = nhan vat mat than, hanh trang rong)
--
--   mysql -u root -p kpah_db < sql/fix_data_item_lo_id.sql
--   ... roi BAT LAI SERVER (bang item template chi doc luc khoi dong).
--
-- ---------------------------------------------------------------------
-- CHUYEN GI DA XAY RA
-- Script doi sach ky nang pet sang potion (sql/migrate_sach_pet_to_potion.php buoc 5)
-- co lenh:  DELETE FROM data_item WHERE id=722
-- Bang dang co id 1..904 (thieu id 0) = 904 dong. Xoa mot dong con 903 dong,
-- nhung id lon nhat VAN la 904.
--
-- Client cap phat mang template theo SO LUONG nhung ghi vao mang theo ID
-- (Client/MessageHandler.cs:1647):
--     ItemTemplate[] array = new ItemTemplate[soLuong + 1];   // 903 + 1 = 904 o
--     array[idDocDuoc] = new ItemTemplate();                  // ghi vao o 904 -> TRAN
-- Client bat loi do roi bo qua am tham, nen mat dung template id 904
-- ("Cai Trang Sa Tang", type 0 = ao). Hau qua:
--   * Res.getItem(904) tra null
--   * Char.setWearingInfo() nem NPE giua vong lap — ma no da dat bodyStyle,
--     legStyle, hatStyle, weaponStyle = -1 TRUOC vong lap -> nhan vat mat than,
--     chan, non, vu khi, CHI CON CAI DAU
--   * paintItemIcon / ItemInInventory.paint nem NPE luc ve -> o hanh trang va o
--     trang bi nhin nhu RONG
--
-- Ban server tu 2026-08 da gui du id 0..max (id trong thi gui o rong) nen lo id
-- khong con lam hong client. File nay de sua duoc NGAY ma khong can build lai jar,
-- va de bang du lieu sach dung nghia.
--
-- ---------------------------------------------------------------------
-- KIEM TRA (truoc va sau)
--   SELECT COUNT(*) AS so_dong, MAX(id) AS id_lon_nhat FROM data_item;
--   -- so_dong PHAI >= id_lon_nhat, khong thi client mat template o cuoi bang
-- =====================================================================

-- Tra lai dong 722 duoi dang VAT PHAM DA NGHI HUU: type 24 la loai tro
-- (Map.doUseItem gap type 24 la return ngay), khong icon, khong ban, khong roi ra.
-- Giu dong nay de so dong khong bi hut mot, va de item cu con sot trong hanh trang
-- nguoi choi (neu co) van tra cuu duoc template thay vi lam vo hanh trang.
INSERT INTO data_item
(id, name, type, style, he, gender, level, durable,
 atb0, atb1, atb2, atb3, atb4, atb5, atb6, atb7, atb8, atb9,
 price, clazz, xstart, ystart, colortype, nloan, idUpLevel, namein, ideff, pricecu)
SELECT 722, 'Sách kỹ năng pet (cũ)', 24, 0, 0, 0, 1, 0,
       0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
       0, -1, 580, 0, -1, 0, -1, NULL, -1, 0
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM data_item WHERE id = 722) t);

SELECT COUNT(*) AS so_dong, MAX(id) AS id_lon_nhat,
       CASE WHEN COUNT(*) >= MAX(id) THEN 'OK'
            ELSE 'CON THIEU DONG - client se mat template o cuoi bang' END AS trang_thai
  FROM data_item;
