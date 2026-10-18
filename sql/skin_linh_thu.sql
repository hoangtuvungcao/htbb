-- =====================================================================================
--  SKIN LINH THU — 6 vat pham skin
-- =====================================================================================
--
--  Mac vat pham nay vao O DAU TIEN BEN PHAI bang "Trang bi thu" thi con linh thu dang
--  cuoi doi hinh, doi hoat anh va doi cach di chuyen. CHI SO cua con linh thu khong doi
--  mot chut nao — skin chi ghi de o tang dong goi tin, xem src/real/SkinThuCuoi.java.
--
--  Chay mot lan tren may chu, roi khoi dong lai server (bang template item duoc nap luc
--  bat may):
--
--      mysql -u root -p <ten_db> < sql/skin_linh_thu.sql
--      mysql -u root -p <ten_db> < sql/ten_mo_ta_skin_linh_thu.sql
--
--  PHAI CHAY CA TEP THU HAI. Tep nay chi dat TEN NGAN. Tep thu hai ghi de bang ten day
--  du kem MO TA (he ngu hanh + cac dong noi tai) — cot `name` cua game nay vua la ten
--  vua la mo ta, khong co cot mo ta rieng cho do mac duoc. Tep do DUOC SINH RA tu chinh
--  cac bang so trong SkinThuCuoi.java (xem tools/SinhSqlTenSkinThu.java) nen so nguoi
--  choi doc duoc luon dung bang so game dung.
--
-- -------------------------------------------------------------------------------------
--  CAC GIA TRI PHAI KHOP VOI CODE — doi o day thi phai doi ca ben kia
-- -------------------------------------------------------------------------------------
--   id      905..909, 911  <-> real.SkinThuCuoi.ID_ITEM (910 la Phi phong vo hinh, khac
--                         tinh nang - 911 duoc chon de bo qua id do)
--   type    30        <-> real.SkinThuCuoi.TYPE_ITEM
--                         Bang "Trang bi thu" ben client co bang thu tu o co dinh
--                         idWearAnimal = {18,30,14,31,15,32,16,33,17,34}; o dau tien ben
--                         phai ung voi type 30.
--                         DUNG dat 19: ban JAR danh so cot phai la 30..34 con ban Unity
--                         (port tu jar cu) danh 19..23. Gui 19 thi ban JAR tra ve o PHI
--                         PHONG cua bang nhan vat, icon roi xuong goc duoi. Da thong nhat
--                         theo ban JAR. Type 30 cung khong dung voi do mac nao: dong
--                         id=30 trong data_item la "ech quy", tuc ban ghi LOAI THU.
--   xstart  860..865  = id icon trong hanh trang -> map/icon/<xstart>.png
--                         DataGame.nIconItem da nang tu 860 len 866 de phuc vu duoc dai
--                         nay (865 = icon Kim Cang Long). KHONG dung lai dai 855..859 nhu
--                         ban skin dau tien: sau khi tinh nang bi go, dung nam id do da
--                         duoc giao cho CANH (commit 2c651ce) lam anh o xem truoc cua so
--                         Trang bi.
--
--  Kim Cang Long (911) KHONG cong chi so co dinh nhu 5 skin kia: chi so cua no NGAU
--  NHIEN THEO TUNG VAT PHAM (roll luc tao, xem Item.createAttributeSkinKimCangLong), nen
--  atb0..atb9 van la 0 (giong het cac skin khac) nhung "chi so" that nam trong otherAtt
--  cua CHINH VAT PHAM da tao ra, khong nam trong data_item nay.
--
--  Id 905..909, 911 la 6 id trong: data_item hien co dung 904 dong, id 1..904 lien tuc,
--  910 la Phi phong vo hinh (khac tinh nang). Neu may chu cua ban da co san cac id nay
--  thi doi sang id trong khac VA sua SkinThuCuoi.ID_ITEM cho khop, roi build lai.
--
-- -------------------------------------------------------------------------------------
--  Y NGHIA CAC COT DAT O DAY
-- -------------------------------------------------------------------------------------
--   type=30            o dau tien ben phai bang Trang bi thu (xem tren)
--   style=0 he=0       khong thuoc he nao, khong doi mau
--   gender=0           ca nam va nu deu mac duoc
--   level=1            khong yeu cau cap
--   durable=0          khong hao mon, khong can sua
--   atb0..atb9 = 0     KHONG CONG CHI SO NAO. Day la chu y quan trong nhat cua ca tinh
--                      nang: skin thuan tuy doi hinh, chi so van la cua con linh thu.
--   price=0 clazz=-1   khong ban o shop thuong, moi mon phai dung duoc
--   colortype=2        ten vat pham hien mau (giong may mon thoi trang 890..892)
--   nloan=0            khong phai do thue
--   idUpLevel=-1       khong nang cap duoc
--   ideff=-1           khong gan hieu ung roi
-- =====================================================================================

DELETE FROM `data_item` WHERE `id` BETWEEN 905 AND 909 OR `id` = 911;

INSERT INTO `data_item`
    (`id`, `name`, `type`, `style`, `he`, `gender`, `level`, `durable`,
     `atb0`, `atb1`, `atb2`, `atb3`, `atb4`, `atb5`, `atb6`, `atb7`, `atb8`, `atb9`,
     `price`, `clazz`, `xstart`, `ystart`, `colortype`, `nloan`, `idUpLevel`, `namein`,
     `ideff`, `pricecu`)
VALUES
    (905, 'Skin linh thú Phúc Thú',      30, 0, 0, 0, 1, 0, 0,0,0,0,0,0,0,0,0,0, 0, -1, 860, 860, 2, 0, -1, NULL, -1, 0),
    (906, 'Skin linh thú Gấu Trúc',      30, 0, 0, 0, 1, 0, 0,0,0,0,0,0,0,0,0,0, 0, -1, 861, 861, 2, 0, -1, NULL, -1, 0),
    (907, 'Skin linh thú Tiên Lộc',      30, 0, 0, 0, 1, 0, 0,0,0,0,0,0,0,0,0,0, 0, -1, 862, 862, 2, 0, -1, NULL, -1, 0),
    (908, 'Skin linh thú Xích Dương',    30, 0, 0, 0, 1, 0, 0,0,0,0,0,0,0,0,0,0, 0, -1, 863, 863, 2, 0, -1, NULL, -1, 0),
    (909, 'Skin linh thú Chiến Lang',    30, 0, 0, 0, 1, 0, 0,0,0,0,0,0,0,0,0,0, 0, -1, 864, 864, 2, 0, -1, NULL, -1, 0),
    (911, 'Skin linh thú Kim Cang Long', 30, 0, 0, 0, 1, 0, 0,0,0,0,0,0,0,0,0,0, 0, -1, 865, 865, 2, 0, -1, NULL, -1, 0);

-- Kiem tra: phai ra dung 6 dong, va id lon nhat cua data_item phai la 911.
SELECT `id`, `name`, `type`, `xstart` FROM `data_item` WHERE `id` BETWEEN 905 AND 909 OR `id` = 911 ORDER BY `id`;
SELECT MAX(`id`) AS `id_lon_nhat`, COUNT(*) AS `so_dong` FROM `data_item`;
