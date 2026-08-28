-- =====================================================================================
--  GO SKIN LINH THU — xoa 5 template vat pham 905..909
-- =====================================================================================
--
--  Toan bo code cua tinh nang skin linh thu da duoc go khoi server. File nay chi lo
--  phan CO SO DU LIEU, va PHAI chay dung thu tu duoi day.
--
-- -------------------------------------------------------------------------------------
--  VI SAO KHONG DUOC XOA NGAY
-- -------------------------------------------------------------------------------------
--  Item.getTemplate() tra ve NULL khi khong tim thay template:
--
--      return (ItemTemplates) Map.itemTemplates.get(...).get(new Integer(tempateID));
--
--  ma hau het cho goi thang `item.getTemplate().type`. Xoa template trong khi con nguoi
--  choi giu vat pham 905..909 la nem NullPointerException ngay luc nap nhan vat do —
--  ho khong vao duoc game.
--
--  Vat pham cua nguoi choi KHONG nam trong bang rieng ma duoc noi chuoi vao cot `equip`
--  cua bang nhan vat, theo dinh dang "<clazz>,<idTemplate>,<place>,..." (Item.getDBInfo).
--  Nen khong the xoa bang mot cau DELETE gon gang; phai doi den khi khong con ai giu.
--
-- -------------------------------------------------------------------------------------
--  BUOC 1 — dem xem con ai giu khong (chay TRUOC, khi server dang tat)
-- -------------------------------------------------------------------------------------
--  Truy van loc rong (co the dinh nham vi cac cot so khac cung mang gia tri 905..909),
--  nhung dung de SANG LOC: ra 0 dong la chac chan sach.

SELECT COUNT(*) AS `so_nhan_vat_co_the_dang_giu`
FROM `char`
WHERE `equip` REGEXP '(^|\|)[0-9-]+,(905|906|907|908|909),';

--  Ra khac 0 thi liet ke ra de xu ly tay:

SELECT `id`, `name`
FROM `char`
WHERE `equip` REGEXP '(^|\|)[0-9-]+,(905|906|907|908|909),'
LIMIT 50;

-- -------------------------------------------------------------------------------------
--  BUOC 2 — chi chay khi BUOC 1 ra 0 dong
-- -------------------------------------------------------------------------------------

-- DELETE FROM `data_item` WHERE `id` BETWEEN 905 AND 909;

-- -------------------------------------------------------------------------------------
--  BUOC 3 — kiem tra lai: phai ra 0 dong
-- -------------------------------------------------------------------------------------

-- SELECT `id`, `name` FROM `data_item` WHERE `id` BETWEEN 905 AND 909;

-- =====================================================================================
--  NEU VAN CON NGUOI GIU VAT PHAM
-- =====================================================================================
--  Hai lua chon, deu an toan hon la xoa template:
--
--  1. CU DE NGUYEN template. Khong xoa gi ca. Vat pham thanh mot mon type 19 binh
--     thuong (cung type voi Phi phong) khong cong chi so nao. Khong ai NPE, chi hoi
--     thua mot dong trong data_item.
--
--  2. Doi ten cho ro la do bo di, roi thu hoi dan bang tay:
--
--     UPDATE `data_item` SET `name` = CONCAT('[ngung dung] ', `name`)
--     WHERE `id` BETWEEN 905 AND 909;
--
--  Icon map/icon/855..859.png da bi xoa cung luc voi code, va DataGame.nIconItem da ha
--  tu 860 ve 855, nen server khong con doc toi cac icon do nua. Vat pham con sot lai se
--  hien khong co icon trong hanh trang — khong nem loi.
-- =====================================================================================
