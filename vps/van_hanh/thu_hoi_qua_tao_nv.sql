-- =====================================================================================
--  TUY CHON - THU HOI QUA TAO NHAN VAT CU (40.000.000 xu + 20.000 luong + 30.000 luong khoa)
--
--  CHUA DUOC CHAY. Chu game quyet dinh rieng. Ma may chu tu ban 19/10/2026 khong phat qua nay nua
--  (data/DatabaseNhanVat.java, tools/luu_tru_sinh_du_lieu/sql_nguon/sua_he_so_nap_va_qua_tao_nv.sql); tep nay chi danh cho nhan vat
--  TAO TRUOC do va da NHAN qua.
--
--  NHAN DIEN: dong nhat ky tob_log_all_item aclog='nhanxu' co chu "tu vi tri board_created db"
--  (DatabaseXepHang.getBoardCreated ghi luc nhan). Moi nhan vat chi bi tinh MOT lan.
--  So thu hoi theo dot: 40.000.000 xu -> 20.000 luong + 30.000 luong khoa (12/09/2026 tro di);
--  10.000.000 xu -> 5.000 luong + 100.000 luong khoa (bo cu). Dot khac: bo qua (khong doan).
--
--  KHONG DONG TOI PHAN NGUOI CHOI KIEM DUOC: chi tru TOI DA bang so qua; tru tui truoc, thieu thi
--  tru kho (NPC Tong Quan); khong bao gio xuong duoi 0. Nguoi da tieu het qua thi chi mat phan con lai.
--  Vat pham kem qua (15 trung pet 684-688, 2 Huyet linh thao 720) KHONG thu hoi o day.
--
--  CACH CHAY (bao tri, may chu game DA TAT - Char dang online se ghi de so du khi luu):
--    1. sao luu:  mysqldump htbb_db tob_char > /opt/htbb/backups/tob_char-truoc-thu-hoi.sql
--    2. xem truoc: chay nguyen tep nay (chi SELECT, phan UPDATE dang bi chu thich)
--    3. dong y thi bo chu thich khoi BUOC 3 roi chay lai.
-- =====================================================================================

DROP TEMPORARY TABLE IF EXISTS tmp_thu_hoi_qua;
CREATE TEMPORARY TABLE tmp_thu_hoi_qua AS
SELECT c.id, c.charname,
       q.xu_qua,
       CASE q.xu_qua WHEN 40000000 THEN 20000 WHEN 10000000 THEN 5000 ELSE 0 END AS luong_qua,
       CASE q.xu_qua WHEN 40000000 THEN 30000 WHEN 10000000 THEN 100000 ELSE 0 END AS luong_khoa_qua,
       CAST(c.gold AS SIGNED) AS gold, CAST(c.kho_xu AS SIGNED) AS kho_xu,
       CAST(c.luong AS SIGNED) AS luong, CAST(c.kho_luong AS SIGNED) AS kho_luong,
       CAST(c.luonglock AS SIGNED) AS luonglock, CAST(c.kho_luong_khoa AS SIGNED) AS kho_luong_khoa
FROM tob_char c
JOIN (
  SELECT charname,
         MAX(CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(info, ' + ', -1), ' = ', 1) AS SIGNED)) AS xu_qua
  FROM tob_log_all_item
  WHERE aclog = 'nhanxu' AND info LIKE '%tu vi tri board_created db%'
  GROUP BY charname
) q ON q.charname = c.charname
WHERE q.xu_qua IN (40000000, 10000000);

-- BUOC 1: XEM TRUOC tung nhan vat (so du hien tai -> sau khi thu hoi)
SELECT id, charname,
       gold, kho_xu, LEAST(xu_qua, gold + kho_xu) AS thu_xu,
       GREATEST(gold - xu_qua, 0) AS gold_sau,
       kho_xu - LEAST(GREATEST(xu_qua - gold, 0), kho_xu) AS kho_xu_sau,
       luong, kho_luong, LEAST(luong_qua, luong + kho_luong) AS thu_luong,
       luonglock, kho_luong_khoa, LEAST(luong_khoa_qua, luonglock + kho_luong_khoa) AS thu_luong_khoa
FROM tmp_thu_hoi_qua ORDER BY id;

-- BUOC 2: TONG
SELECT COUNT(*) AS so_nhan_vat,
       SUM(LEAST(xu_qua, gold + kho_xu)) AS tong_thu_xu,
       SUM(LEAST(luong_qua, luong + kho_luong)) AS tong_thu_luong,
       SUM(LEAST(luong_khoa_qua, luonglock + kho_luong_khoa)) AS tong_thu_luong_khoa
FROM tmp_thu_hoi_qua;

-- BUOC 3: THU HOI (bo chu thich khi da dong y; chay trong mot giao dich)
-- START TRANSACTION;
-- (dung so du chup o tmp_thu_hoi_qua - may chu da tat nen khong doi giua chung)
-- UPDATE tob_char c JOIN tmp_thu_hoi_qua t ON t.id = c.id SET
--   c.gold           = GREATEST(t.gold - t.xu_qua, 0),
--   c.kho_xu         = t.kho_xu - LEAST(GREATEST(t.xu_qua - t.gold, 0), t.kho_xu),
--   c.luong          = GREATEST(t.luong - t.luong_qua, 0),
--   c.kho_luong      = t.kho_luong - LEAST(GREATEST(t.luong_qua - t.luong, 0), t.kho_luong),
--   c.luonglock      = GREATEST(t.luonglock - t.luong_khoa_qua, 0),
--   c.kho_luong_khoa = t.kho_luong_khoa - LEAST(GREATEST(t.luong_khoa_qua - t.luonglock, 0), t.kho_luong_khoa);
-- INSERT INTO tob_log_all_item (charname, aclog, info, timelog)
--   SELECT charname, 'thu_hoi_qua_tao_nv',
--          CONCAT('thu hoi qua tao nhan vat: xu ', xu_qua, ', luong ', luong_qua, ', luong khoa ', luong_khoa_qua), NOW()
--   FROM tmp_thu_hoi_qua;
-- COMMIT;

DROP TEMPORARY TABLE IF EXISTS tmp_thu_hoi_qua;
