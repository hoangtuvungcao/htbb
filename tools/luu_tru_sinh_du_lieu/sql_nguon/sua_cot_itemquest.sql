-- =====================================================================================
--  NOI COT tob_char_quest.itemquest: varchar(100) -> varchar(1000)
-- =====================================================================================
--
--  Char.getInfoQuestSave() ghi itemquest = so luong MOI vat pham nhiem vu, cach nhau dau phay
--  (do dai mang = so ten vat pham nhiem vu; che do lau dai co 51 ten -> it nhat 101 ky tu).
--  Cot cu 100 ky tu nen MariaDB (strict) bao "Data too long for column 'itemquest'" va
--  "LOI SAVE QUEST ROI" moi lan nhan vat moi vao game / thoat: tien do nhiem vu khong duoc luu.
--  NovaDB (may dev) khong kiem do dai nen khong thay loi nay.
--
--  du_lieu/sql_nguon/gamedb7_clean.sql va sql/htbb_day_du.sql da co cot moi. CSDL dung tu ban cu thi chay:
--
--      mysql -u root htbb_db < du_lieu/sql_nguon/sua_cot_itemquest.sql
--
--  Chay lai nhieu lan van an toan. Khong can khoi dong lai server.
-- -------------------------------------------------------------------------------------

ALTER TABLE `tob_char_quest` MODIFY `itemquest` varchar(1000) DEFAULT NULL;
