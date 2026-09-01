-- ==================================================================================
--  SU KIEN VU LAN (VIET LAI) - hoa hong vang, hoa hong do, bo hoa vang, bo hoa do
-- ==================================================================================
--
--  Chay MOT LAN tren may chu, roi khoi dong lai server (bang data_potion chi nap luc
--  bat may):
--
--      mysql -u root -p <ten_db> < sql/them_su_kien_vu_lan_2026.sql
--
--  Sau do mo su kien bang trang quan tri web/admin/sukien.php (ma su kien "vulan"),
--  hoac chen thang vao bang tob_su_kien.
--
--  Ma nguon: src/real/Potion.java (TYPE_NHANH_HOA_HONG_VANG/TYPE_HOA_HONG_DO/
--  TYPE_BO_HOA_VANG/TYPE_BO_HOA_DO), src/real/cmd/LoginHandler.java (POTION_SHOP,
--  PORTION_NAME), src/real/MapSuKien.java (doMenuDauTruongVuLan), src/real/doUsePotion.java.
--
--  VAT PHAM. data_potion la BANG CO KICH THUOC CO DINH (dung 162 dong, id 0..161 -
--  khop Char.SO_O_THUOC = 162 va POTION_SHOP client). KHONG duoc INSERT dong moi -
--  phai UPDATE vao cac o con trong san co. Dung 4 trong 5 o trong con lai:
--
--      106 Nhanh hoa hong vang  - roi khi thu hoach cay than / che do / tien giai /
--                                 san boss / giet quai lech cap trong su kien
--      107 Hoa hong do          - doi tu diem tieu xai (Char.subLuong), KHONG roi khi farm
--      108 (DA DUNG - "Hop qua may man" cua su kien World Cup, KHONG DUNG O DAY)
--      109 Bo hoa vang          - doi o NPC Dau Truong, CO THE GIAO DICH (isTrade=1)
--      110 Bo hoa do            - doi o NPC Dau Truong, khong giao dich
--
--  ICON TAM: dung lai idImage 45 (hoa hong, anh dang dung cho id 58-61) cho ca 4 dong -
--  CHUA CO ANH RIENG, can lam nghe thuat that roi doi idImage sau (xem ghi chu du an
--  ve dai icon 800-806 bi chan - dung dai do).
--
--  QUAN TRONG - da doi chieu voi LoginHandler.POTION_SHOP (real/cmd/LoginHandler.java):
--  ca 4 id (106,107,109,110) DA duoc them vao mang do, anh xa sang data_shop 87/88/90/91.
--  Bon dong data_shop nay co cot decript RONG (da kiem truoc) nen an toan tai su dung -
--  neu sau nay ai dien decript cho data_shop 87/88/90/91 thi chi tiet 4 thuoc nay se hien
--  nham mo ta do (dung cach 108 -> data_shop 89 dang lam).
-- ==================================================================================

UPDATE `data_potion`
   SET `idImage` = 45,
       `delay`   = 0,
       `isTrade` = 0,
       `name`    = 'Nhánh hoa hồng vàng',
       `name2`   = 'nhanhhoahongvang',
       `price`   = 0,
       `recovered` = 0
 WHERE `id` = 106;

UPDATE `data_potion`
   SET `idImage` = 45,
       `delay`   = 0,
       `isTrade` = 0,
       `name`    = 'Hoa hồng đỏ',
       `name2`   = 'hoahongdo',
       `price`   = 0,
       `recovered` = 0
 WHERE `id` = 107;

UPDATE `data_potion`
   SET `idImage` = 45,
       `delay`   = 0,
       `isTrade` = 1,
       `name`    = 'Bó hoa vàng',
       `name2`   = 'bohoavang',
       `price`   = 0,
       `recovered` = 0
 WHERE `id` = 109;

UPDATE `data_potion`
   SET `idImage` = 45,
       `delay`   = 0,
       `isTrade` = 0,
       `name`    = 'Bó hoa đỏ',
       `name2`   = 'bohoado',
       `price`   = 0,
       `recovered` = 0
 WHERE `id` = 110;

-- ---------------------------------------------------------------------------------
--  KIEM TRA
-- ---------------------------------------------------------------------------------
-- SELECT id, idImage, isTrade, name FROM data_potion WHERE id IN (106, 107, 108, 109, 110);
-- SELECT COUNT(*) FROM data_potion;   -- phai van la 162
