-- ============================================================================
-- Cot `soluong` trong `tob_log_buy_shop` - gop nhieu lan mua CUNG mon lien tiep
-- (trong vong vai giay) thanh MOT dong log, thay vi moi lan bam mua ghi rieng
-- 1 dong giong het nhau.
--
-- KHONG BAT BUOC CHAY TAY: server game (src/data/Database.chuanBiCotSoLuongLogBuyShop(),
-- goi tu TeamServer luc khoi dong, TRUOC khi nhan nguoi choi) tu ALTER TABLE neu
-- thieu cot. File nay de ai muon co cot truoc khi khoi dong lai server thi chay
-- tay duoc:
--
--     mysql -u root kpah_db < sql/them_cot_soluong_log_buy_shop.sql
--
-- VI SAO CAN (14/09/2026): goi tin mua o shop NPC (lenh 85, MapMuaBan.doBuySpecialItem)
-- KHONG mang truong so luong - moi lan bam mua la mot goi tin rieng. Mua vd 500
-- nguyen lieu mot lan la client gui 500 goi tin, truoc day moi goi ghi THANG 1
-- dong vao tob_log_buy_shop -> 500 dong lien tiep giong het nhau, log kho doc.
-- Sua: gop trong bo nho server (Database.saveLogBuyItemShop/xaLogMuaShopHetHan),
-- nhieu lan mua cung mon trong vong 5 giay duoc cong don vao cot soluong nay,
-- chi ghi xuong CSDL dung 1 dong khi doi mon khac hoac het cua so thoi gian.
-- ============================================================================

ALTER TABLE tob_log_buy_shop
  ADD COLUMN soluong INT(10) UNSIGNED NOT NULL DEFAULT 1 AFTER money;

-- Kiem tra lai: phai ra 1.
SELECT COUNT(*) AS co_cot FROM information_schema.columns
 WHERE table_schema = 'kpah_db' AND table_name = 'tob_log_buy_shop' AND column_name = 'soluong';
