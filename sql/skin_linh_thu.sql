-- =====================================================================================
--  SKIN LINH THU — 5 vat pham skin
-- =====================================================================================
--
--  Mac vat pham nay vao O DAU TIEN BEN PHAI bang "Trang bi thu" thi con linh thu dang
--  cuoi doi hinh, doi hoat anh va doi cach di chuyen. CHI SO cua con linh thu khong doi
--  mot chut nao — skin chi ghi de o tang dong goi tin, xem src/real/SkinLinhThu.java.
--
--  Chay mot lan tren may chu, roi khoi dong lai server (bang template item duoc nap luc
--  bat may):
--
--      mysql -u root -p <ten_db> < sql/skin_linh_thu.sql
--
-- -------------------------------------------------------------------------------------
--  CAC GIA TRI PHAI KHOP VOI CODE — doi o day thi phai doi ca ben kia
-- -------------------------------------------------------------------------------------
--   id      905..909  <-> real.SkinLinhThu.ID_ITEM
--   type    19        <-> real.SkinLinhThu.TYPE_ITEM
--                         Bang "Trang bi thu" ben client co bang thu tu o co dinh
--                         idWearAnimal = {18,19,14,20,15,21,16,22,17,23}; o dau tien ben
--                         phai ung voi type 19. Type 19 cung la type cua Phi phong nhung
--                         khong lan nhau: moi cho trong server phan biet bang ID TEMPLATE
--                         (SkinLinhThu.laItemSkin), khong bang type.
--   xstart  855..859  = id icon trong hanh trang -> map/icon/<xstart>.png
--                         DataGame.nIconItem da nang tu 855 len 864 de phuc vu duoc dai
--                         nay. 855..859 truoc day chua co file nao, khong ghi de icon cu.
--
--  Id 905..909 la 5 id trong ke tiep: data_item hien co dung 904 dong, id 1..904 lien tuc.
--  Neu may chu cua ban da co san id 905..909 thi doi sang 5 id trong khac VA sua
--  SkinLinhThu.ID_ITEM cho khop, roi build lai.
--
-- -------------------------------------------------------------------------------------
--  Y NGHIA CAC COT DAT O DAY
-- -------------------------------------------------------------------------------------
--   type=19            o dau tien ben phai bang Trang bi thu (xem tren)
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

DELETE FROM `data_item` WHERE `id` BETWEEN 905 AND 909;

INSERT INTO `data_item`
    (`id`, `name`, `type`, `style`, `he`, `gender`, `level`, `durable`,
     `atb0`, `atb1`, `atb2`, `atb3`, `atb4`, `atb5`, `atb6`, `atb7`, `atb8`, `atb9`,
     `price`, `clazz`, `xstart`, `ystart`, `colortype`, `nloan`, `idUpLevel`, `namein`,
     `ideff`, `pricecu`)
VALUES
    (905, 'Skin linh thú Phúc Thú',   19, 0, 0, 0, 1, 0, 0,0,0,0,0,0,0,0,0,0, 0, -1, 855, 855, 2, 0, -1, NULL, -1, 0),
    (906, 'Skin linh thú Gấu Trúc',   19, 0, 0, 0, 1, 0, 0,0,0,0,0,0,0,0,0,0, 0, -1, 856, 856, 2, 0, -1, NULL, -1, 0),
    (907, 'Skin linh thú Tiên Lộc',   19, 0, 0, 0, 1, 0, 0,0,0,0,0,0,0,0,0,0, 0, -1, 857, 857, 2, 0, -1, NULL, -1, 0),
    (908, 'Skin linh thú Xích Dương', 19, 0, 0, 0, 1, 0, 0,0,0,0,0,0,0,0,0,0, 0, -1, 858, 858, 2, 0, -1, NULL, -1, 0),
    (909, 'Skin linh thú Chiến Lang', 19, 0, 0, 0, 1, 0, 0,0,0,0,0,0,0,0,0,0, 0, -1, 859, 859, 2, 0, -1, NULL, -1, 0);

-- Kiem tra: phai ra dung 5 dong, va id lon nhat cua data_item phai la 909.
SELECT `id`, `name`, `type`, `xstart` FROM `data_item` WHERE `id` BETWEEN 905 AND 909 ORDER BY `id`;
SELECT MAX(`id`) AS `id_lon_nhat`, COUNT(*) AS `so_dong` FROM `data_item`;
