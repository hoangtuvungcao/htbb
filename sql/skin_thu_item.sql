-- =====================================================================
-- VAT PHAM SKIN THU CUOI  (5 skin: Phuc Thu, Gau Truc, Tien Loc, Xich Duong, Chien Lang)
--
-- Chay tren DB game (kpah_db), mot lan, roi BAT LAI SERVER (bang ten thuoc tinh va
-- bang item template chi doc luc khoi dong).
--
--   mysql -u root -p kpah_db < sql/skin_thu_item.sql
--
-- Gom hai phan:
--   1) data_attribute : 11 dong TEN chi so de client ghep mo ta vat pham
--   2) data_item      : 5 vat pham skin, type 19
--
-- ---------------------------------------------------------------------
-- VI SAO TYPE 19
-- Bang "Trang bi thu" ben client co 10 o, thu tu type do client quy dinh san:
--     idWearAnimal = {18, 19, 14, 20, 15, 21, 16, 22, 17, 23}
--     o chan = cot TRAI (yen 18, giap 14, ho uyen 15, non 16, ban dap 17)
--     o le   = cot PHAI
-- O DAU TIEN BEN PHAI ung voi type 19 -> vat pham skin dung type 19.
-- Type 19 cung la type cua "Phi phong" (do nhan vat) nhung khong lan nhau: server
-- phan biet bang ID TEMPLATE (real.SkinThuNuoi.ID_ITEM), phi phong that di duong cu.
--
-- ---------------------------------------------------------------------
-- CAC CON SO PHAI KHOP VOI CODE (src/real/SkinThuNuoi.java)
--   id vat pham 950..954  ->  ID_ITEM = {950, 951, 952, 953, 954}
--   cot xstart 815..819   ->  id icon, tuc map/icon/815..819.png
--   ten trong data_attribute -> TEN_DONG_MOTA (trung TUNG CHU, ke ca dau)
-- Doi id o day thi phai doi trong SkinThuNuoi cho khop, khong thi vat pham thanh do
-- vo dung (khong hien skin, mac vao o phi phong).
--
-- Kiem tra truoc khi chay (phai ra 0 dong, khong thi doi id khac):
--   SELECT id, name, type FROM data_item      WHERE id BETWEEN 950 AND 954;
--   SELECT id, name       FROM data_attribute WHERE id BETWEEN 200 AND 210;
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1) TEN CHI SO DE HIEN MO TA
--
-- Client ghep tung dong la  "<name>: <gia tri>" + ("%" neu ispercent = 1).
-- ispercent = 2 la kieu mot chu so thap phan (200 -> "20.0%") nen KHONG dung o day.
-- Server tra cuu cac dong nay THEO TEN (SkinThuNuoi.ganChiSoDongMoTa) chu khong theo
-- cot id, vi client danh so theo THU TU DONG khi doc "order by id" — them mot dong o
-- giua la moi thu lech. Vi vay id o day chi can lon hon moi id dang co.
-- ---------------------------------------------------------------------
INSERT INTO `data_attribute` (`id`, `name`, `idcolor`, `ispercent`, `decript`, `namein`) VALUES
(200, 'Sinh lực',                     0, 0, 'skin thu cuoi: Phuc Thu +HP',            ''),
(201, 'Nội lực',                      0, 0, 'skin thu cuoi: Phuc Thu +MP',            ''),
(202, 'Giảm sát thương khi bị đánh',  0, 1, 'skin thu cuoi: Phuc Thu buff bi dong',   ''),
(203, 'Chính xác cộng thêm',          0, 0, 'skin thu cuoi: Gau Truc +chinh xac',     ''),
(204, 'Phản sát thương',              0, 1, 'skin thu cuoi: Gau Truc buff bi dong',   ''),
(205, 'Tiềm năng cộng thêm',          0, 1, 'skin thu cuoi: Tien Loc +% tiem nang',   ''),
(206, 'Lá chắn chặn sát thương',      0, 1, 'skin thu cuoi: Tien Loc buff bi dong',   ''),
(207, 'Xuyên giáp cộng thêm',         0, 1, 'skin thu cuoi: Xich Duong +% xuyen giap',''),
(208, 'Nộ khí tăng tấn công',         0, 1, 'skin thu cuoi: Xich Duong buff HP thap', ''),
(209, 'Phòng thủ cộng thêm',          0, 1, 'skin thu cuoi: Chien Lang +% thu',       ''),
(210, 'Hấp thụ sát thương',           0, 1, 'skin thu cuoi: Chien Lang hap thu',      '');

-- ---------------------------------------------------------------------
-- 2) NAM VAT PHAM SKIN
--
-- Y nghia cac cot dat o day:
--   type      = 19  o dau tien ben phai bang Trang bi thu (xem tren)
--   level     = 0   khong doi cap do (Map.doUseItem chan neu item.level > cap nhan vat)
--   clazz     = -1  moi class deu mac duoc
--   durable   = 0   khong co do ben; trang bi thu goc de 500, skin thi khong nen hong
--   atb0..9   = 0   chi so skin nam trong code (SkinThuNuoi), KHONG lay tu day. Cot atb
--                   cua type 19 chi hien duoc dong 1 va 6 (Item.attribShowedForType) nen
--                   khong du de mo ta skin; mo ta duoc gui rieng qua data_attribute o tren.
--   xstart    = id icon trong map/icon/<id>.png  (Database: item.idIcon = xstart)
--   ystart    = 0   khong co anh to
--   price     = 0   khong ban lai duoc gia cao; muon ban trong shop thi them vao data_shop
--   colortype = -1  mau ten mac dinh
--   idUpLevel = -1  khong nang cap
--   ideff     = -1  khong co hieu ung rieng
--
-- Ten vat pham hien ngay dong dau tooltip nen ghi luon he ngu hanh cho de nhan.
-- ---------------------------------------------------------------------
INSERT INTO `data_item`
(`id`, `name`, `type`, `style`, `he`, `gender`, `level`, `durable`,
 `atb0`,`atb1`,`atb2`,`atb3`,`atb4`,`atb5`,`atb6`,`atb7`,`atb8`,`atb9`,
 `price`, `clazz`, `xstart`, `ystart`, `colortype`, `nloan`, `idUpLevel`, `namein`, `ideff`, `pricecu`) VALUES
(950, 'Skin Phúc Thú (hệ Kim)',   19, 0, 0, 0, 0, 0, 0,0,0,0,0,0,0,0,0,0, 0, -1, 815, 0, -1, 0, -1, NULL, -1, 0),
(951, 'Skin Gấu Trúc (hệ Mộc)',   19, 0, 0, 0, 0, 0, 0,0,0,0,0,0,0,0,0,0, 0, -1, 816, 0, -1, 0, -1, NULL, -1, 0),
(952, 'Skin Tiên Lộc (hệ Thủy)',  19, 0, 0, 0, 0, 0, 0,0,0,0,0,0,0,0,0,0, 0, -1, 817, 0, -1, 0, -1, NULL, -1, 0),
(953, 'Skin Xích Dương (hệ Hỏa)', 19, 0, 0, 0, 0, 0, 0,0,0,0,0,0,0,0,0,0, 0, -1, 818, 0, -1, 0, -1, NULL, -1, 0),
(954, 'Skin Chiến Lang (hệ Thổ)', 19, 0, 0, 0, 0, 0, 0,0,0,0,0,0,0,0,0,0, 0, -1, 819, 0, -1, 0, -1, NULL, -1, 0);

-- =====================================================================
-- CACH PHAT CHO NGUOI CHOI
--   giftcode : them "DB:950:1:0" vao danh sach vat pham cua code (950..954)
--   admin    : cac lenh phat vat pham san co, dung id template 950..954
--
-- CACH DUNG (nguoi choi)
--   1. Cuoi thu (bat buoc — vat pham skin gan theo tung con thu cuoi)
--   2. Mo hanh trang, dung vat pham skin -> no vao o dau tien ben phai bang Trang bi thu
--   3. Thao ra bang cach mac vat pham skin khac; xuong thu la mat skin
-- =====================================================================
