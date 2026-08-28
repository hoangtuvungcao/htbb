-- ============================================================================
-- Them vat pham "Phi phong vo hinh" (id 910) de CANH hien duoc o cua so Trang bi
-- ma khong phai mac mot cai phi phong that.
--
-- VI SAO CAN: o xem truoc trong cua so Trang bi ve bang Char.paintAvatar cua
-- client, va ca khoi ve phi phong / canh nam trong mot cai cong:
--
--     short coat = this.coatStyle;
--     ...
--     if (coat != -1) {
--         if (this.idImgPP == -1) { ...ve ao choang... }
--         else                    { ...ve anh phi phong / canh... }
--     }
--
-- coatStyle CHI duoc gan tu vat pham type 19 trong Char.setWearingInfo (client
-- 2.7.6, Char.java:2548) - khong con duong nao khac, idModel cung khong de duoc
-- vao no. Canh la type 29 va nam o danh sach THOI TRANG (wModel.CanhModel) chu
-- khong phai danh sach trang bi, nen tu no khong bao gio mo duoc cong nay.
--
-- Ket qua: ai chi deo canh ma khong mac phi phong thi tren ban do van thay canh
-- (duong khac, khong co cong nay) nhung trong Trang bi thi khong.
--
-- CACH LAM: cong do chi hoi "co dang mac MOT MON type 19 nao khong", no khong
-- quan tam mon do la gi. Va khi da co anh canh (idImgPP != -1) thi client KHONG
-- ve bo phan ao choang nua. Nen chi can mot mon type 19 tro tron.
--
-- Mon nay tro tron THAT SU, khong phai nho anh trong suot: bo phan ao choang
-- duoc client doc tu tai nguyen TRONG JAR (CharPartInfo.loadCoat ->
-- getResourceAsStream("/c/coat/" + id)), ma jar client khong he co thu muc do.
-- Da kiem bang cach giai nen build/emu/kpah.jar: khong co /c/coat/. Nghia la
-- bo phan ao choang chua bao gio duoc ve, style de bao nhieu cung the.
-- Chon 16 cho ro rang la nam ngoai day 0..15 dang dung.
--
-- KHONG CAN SUA JAVA. Nhanh canh trong Char.getIdBigPhiPhong() da tra san anh
-- canh (map/icon/855..859) tu ban truoc.
--
-- Chay tren VPS sau khi da day map/ len. Sau do phat vat pham 910 cho nguoi choi
-- (qua Admin Panel hoac cho vao shop).
-- ============================================================================

-- Kiem tra truoc: phai thay 910 CHUA co, va 829 chua vat pham nao dung lam icon.
SELECT id, name FROM data_item WHERE id = 910;
SELECT id, name, xstart FROM data_item WHERE xstart = 829;

-- Cot theo dung thu tu cua bang: id, name, type, style, he, gender, level,
-- durable, atb0..atb8, atb9, price, clazz, xstart, ystart, colortype, nloan,
-- idUpLevel, namein, ideff, pricecu
--   type   19 = phi phong (giong 584 'Phi phong Lang Van')
--   style  16 = ngoai day 0..15, khong tro vao bo phan ao choang nao
--   level   0 = ai cung mac duoc
--   atb0..atb9 deu 0 = KHONG cong chi so gi, thuan tuy mo cong hien canh
--   atb9    0 = do trang bi that (vao wItems), khong phai do thoi trang.
--             Phai la 0: do thoi trang di vao wModel va se khong bao gio
--             chay qua setWearingInfo de dat coatStyle.
--   xstart 829 = map/icon/829.png, anh cai ao choang mo di
--   nloan   0 = vinh vien, khong het han
INSERT INTO `data_item`
  (`id`, `name`, `type`, `style`, `he`, `gender`, `level`, `durable`,
   `atb0`, `atb1`, `atb2`, `atb3`, `atb4`, `atb5`, `atb6`, `atb7`, `atb8`, `atb9`,
   `price`, `clazz`, `xstart`, `ystart`, `colortype`, `nloan`, `idUpLevel`,
   `namein`, `ideff`, `pricecu`)
VALUES
  (910, 'Phi phong vô hình', 19, 16, 0, 0, 0, 480,
   0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
   0, -1, 829, 0, -1, 0, -1, '', -1, 0);

-- Kiem tra lai.
SELECT id, name, type, style, level, xstart, nloan FROM data_item WHERE id = 910;

-- Doi chieu voi mot phi phong that de thay khac nhau o dau.
SELECT id, name, type, style, level, xstart FROM data_item WHERE id IN (584, 910);

-- ============================================================================
-- PHAT VAT PHAM CHO NGUOI CHOI
--
-- Vat pham 910 la do TRANG BI THAT (atb9 = 0), khong phai do thoi trang, nen
-- nguoi choi mac no nhu mac mot cai phi phong binh thuong: mo Hanh trang, bam
-- vao mon do, chon "Mac". No vao dung o phi phong.
--
-- Cach phat: dung giftcode. Chuoi "ITEM:910:1" chay qua
-- Char.createItemSoLuong(910, 1, player) (DatabaseGiftCode.java:473), ham nay
-- tra template tu Map.itemTemplates.get(5) - la ro chua TAT CA vat pham
-- (DatabaseXepHang.java:577 itemBasic.put cho moi dong) nen id 910 tim thay duoc.
--
-- limit_use = so luot con dung duoc. expire = 0 la khong het han.
-- Doi ten ma 'canhvohinh' thanh gi tuy y truoc khi chay.
-- ============================================================================

-- Kiem tra truoc: ma nay chua ton tai, va lay id lon nhat dang co.
SELECT id, giftcode, item, limit_use FROM giftcode WHERE giftcode = 'canhvohinh';
SELECT MAX(id) AS id_lon_nhat FROM giftcode;

INSERT INTO `giftcode` (`id`, `giftcode`, `xu`, `luong`, `luongLock`, `item`, `expire`, `limit_use`, `type`)
VALUES (16, 'canhvohinh', 0, 0, 0, 'ITEM:910:1', 0, 99999, 0);

-- Kiem tra lai.
SELECT id, giftcode, item, limit_use FROM giftcode WHERE giftcode = 'canhvohinh';
