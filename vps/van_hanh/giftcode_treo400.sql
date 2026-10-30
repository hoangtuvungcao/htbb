-- =====================================================================================
--  GIFTCODE TREO400 - "400 giờ treo game" + 1.000 xu cho nguoi choi hien tai (chu game 31/10/2026)
--
--  Phan thuong: XU:1000,ITEM:1260:40  = 1.000 xu + 40 "Vé treo 10 giờ" (40 x 10 = 400 gio Tu dong danh).
--    - Ve vao tui KHOA (khong giao dich / ky gui), xep chong MOT o; dung trong hanh trang: +10 gio vao kho gio treo,
--      Tu dong danh tru kho sau khi het 5 gio mien phi moi ngay. Kho toi da 500 gio (dung tu tu, ve con giu trong tui).
--    - Gia tri ma (GiftcodeAnToanLogic.giaTri) = ceil(1000/1000) + 40 x 1 = 41 <= giftcode.gia_tri_toi_da (150 tren may
--      chu that; mac dinh 30 thi phai nang truoc). Ngan sach thang: 41 x 1000 luot = 41.000 <= 300.000.
--  Luat giu nguyen (real.GiftcodeAnToan): moi TAI KHOAN 1 lan (khoa (ma, user_id)), toi da giftcode.toi_da_thang ma /
--  thang / nhan vat, ngan sach thang, sai 5 lan khoa 10 phut, tui phai con 1 o (neu chua co chong ve).
--  Nguoi choi nhap: Menu -> Thu/Qua -> "Nhap giftcode" (hoac NPC qua tang) -> TREO400 (khong phan biet hoa thuong).
--
--  tong_luot 1000: 453 tai khoan luc chuan bi (31/10/2026). han: 7 ngay ke tu luc chay tep, het 23:59:59.
--  CAN CO TRUOC: may chu ban co Ve treo (vat pham 1260 trong data_item + data_vat_pham_lau_dai, khoi
--  '-- >>> them_ve_treo.sql' cua sql/htbb.sql - nang_cap_csdl.sh). Thieu thi tep nay khong chen gi.
--  Chay lai duoc (INSERT IGNORE theo khoa ma: lan sau khong doi han / luot). Tat ma: UPDATE ... SET bat = 0.
--  CACH CHAY (VPS): sudo mysql htbb_db < /opt/htbb/vps/van_hanh/giftcode_treo400.sql
-- =====================================================================================

INSERT IGNORE INTO `tob_giftcode` (`ma`, `phan_thuong`, `han`, `tong_luot`, `da_dung`, `cap_toi_thieu`, `bat`, `ghi_chu`, `tao_boi`, `tao_luc`)
SELECT 'treo400', 'XU:1000,ITEM:1260:40', TIMESTAMP(DATE(NOW()) + INTERVAL 7 DAY, '23:59:59'), 1000, 0, 1, 1,
       'Quà người chơi hiện tại: 400 giờ treo (40 Vé treo 10 giờ) + 1.000 xu, mỗi tài khoản 1 lần, 7 ngày', 'sql-van-hanh', NOW()
FROM DUAL
WHERE EXISTS (SELECT 1 FROM `data_item` WHERE `id` = 1260)
  AND EXISTS (SELECT 1 FROM `data_vat_pham_lau_dai` WHERE `id_item` = 1260 AND `hieu_ung` LIKE 'gio_treo:%');

SELECT `ma`, `phan_thuong`, `han`, `tong_luot`, `da_dung`, `cap_toi_thieu`, `bat` FROM `tob_giftcode` WHERE `ma` = 'treo400';
