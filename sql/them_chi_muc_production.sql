-- ============================================================================
-- Chi muc cho bang NONG truoc open test (san sang production, 17/10/2026).
-- Xem docs/pokemon/SAN_SANG_PRODUCTION.md muc 2.
--
-- Chay NHIEU LAN DUOC (IF NOT EXISTS, MariaDB >= 10.1.4). Chay khi server dang tat hoac luc it nguoi:
--     mysql htbb_db < sql/them_chi_muc_production.sql
--
-- Moi chi muc duoi day ung voi mot truy van chay o DUONG NONG ma truoc day phai quet ca bang:
-- ============================================================================

-- Database.check_active(uname) / check_active_putcode(userId): MOI LAN DANG NHAP va moi lan nhap
-- giftcode. 5h_active chi co PRIMARY(id) -> quet ca bang, mot dong / tai khoan tung dang ky.
CREATE INDEX IF NOT EXISTS idx_5h_active_username ON `5h_active` (username);
CREATE INDEX IF NOT EXISTS idx_5h_active_userid ON `5h_active` (userID);

-- tob_char_vip: "update ... where charname=" moi lan nhan qua VIP.
CREATE INDEX IF NOT EXISTS idx_char_vip_charname ON tob_char_vip (charname);

-- tob_buy_ruong: "where charname=.. and dateLog=.." + "where dateLog=.." moi lan mua ruong.
CREATE INDEX IF NOT EXISTS idx_buy_ruong_ngay_ten ON tob_buy_ruong (dateLog, charname);

-- tob_qua_ct_top: "WHERE charname=.. and nhan=0 and soluong=..".
CREATE INDEX IF NOT EXISTS idx_qua_ct_top_charname ON tob_qua_ct_top (charname);

-- KHONG tu dong lam (QUYET DINH CUA CHU SERVER): tob_char co chi muc Index_5 tren pInfo
-- (varchar(2000), prefix 333) - KHONG truy van nao loc theo pInfo, nhung MOI lan luu nhan vat
-- (UPDATE tob_char SET pInfo=...) deu phai cap nhat chi muc nay. Bo di giam ghi dia khi dong nguoi:
--     ALTER TABLE tob_char DROP INDEX Index_5;
-- Cac chi muc gold/xp/ticket/basepoint dung cho bang xep hang (ORDER BY) - giu.
