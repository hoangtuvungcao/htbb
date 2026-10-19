-- KINH TE: HE SO NAP = 1 VA BO QUA TAO NHAN VAT (19/10/2026, chu game duyet). Idempotent.
--   mysql htbb_db < du_lieu/sql_nguon/sua_he_so_nap_va_qua_tao_nv.sql
--
-- 1. 5h_systems.tangnap: may chu tu ban nay KHONG doc gia tri nay nua (Database.TILE_NAP_CO_DINH = 1),
--    sua dong cho khop de nguoi doc CSDL khong hieu nham (log cu: "TANG NAP TOAN SERVER = 2").
-- 2. board_created: hang doi qua tao nhan vat (40.000.000 xu + 20.000 luong + 30.000 luong khoa
--    + trung pet). Ma moi khong ghi vao nua; xoa cac dong CHUA NHAN (nhan vat da tao nhung chua vao
--    game lan nao). KHONG dong toi so du cua nhan vat nao - thu hoi phan da nhan la viec rieng:
--    vps/van_hanh/thu_hoi_qua_tao_nv.sql (chu game quyet).

SELECT command, status AS tangnap_hien_tai FROM `5h_systems` WHERE command = 'tangnap';
UPDATE `5h_systems` SET status = 1 WHERE command = 'tangnap' AND status <> 1;

SELECT COUNT(*) AS qua_tao_nv_chua_nhan, COALESCE(SUM(xu), 0) AS xu, COALESCE(SUM(luong), 0) AS luong,
       COALESCE(SUM(luongK), 0) AS luong_khoa FROM `board_created`;
DELETE FROM `board_created`;
