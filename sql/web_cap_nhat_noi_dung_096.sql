-- ============================================================================
-- web_cap_nhat_noi_dung_096.sql — sua noi dung website da luu trong CSDL (CMS) cho khop game 0.9.5.1 / 0.9.6.
-- Chay vao database GAME (htbb_db), chay lai bao nhieu lan cung duoc (idempotent):
--
--     mysql -u htbb -p htbb_db < sql/web_cap_nhat_noi_dung_096.sql
--
-- 1. Tai game (web_cai_dat khoa 'taiGame'): the Linux con ghi ".AppImage hoac .tar.gz" -> ban Linux tu 0.9.5 chi
--    la MOT tep .AppImage. Chi doi mo ta (moTa) cua the co ma 'linux', khong dong toi link / sha256 / phien ban.
-- 2. Cac khoa CMS khac (trangChu, menu...) tren production chua tung luu -> web dung noi dung mac dinh moi trong
--    ma nguon (shared/utils/cmsMacDinh.ts), khong can SQL.
--
-- Can MariaDB >= 10.2.3 (JSON_SEARCH / JSON_SET). Kiem tra truoc:
--     SELECT JSON_UNQUOTE(JSON_EXTRACT(gia_tri, REPLACE(JSON_UNQUOTE(JSON_SEARCH(gia_tri, 'one', 'linux', NULL, '$.ban[*].ma')), '.ma', '.moTa')))
--       FROM web_cai_dat WHERE khoa = 'taiGame';
-- ============================================================================

-- chu co dau: bat buoc, client mysql mac dinh co the la latin1
SET NAMES utf8mb4;

UPDATE web_cai_dat
   SET gia_tri = JSON_SET(
         gia_tri,
         REPLACE(JSON_UNQUOTE(JSON_SEARCH(gia_tri, 'one', 'linux', NULL, '$.ban[*].ma')), '.ma', '.moTa'),
         'Bản Linux 64-bit một tệp .AppImage, có sẵn Java đi kèm'
       )
 WHERE khoa = 'taiGame'
   AND JSON_VALID(gia_tri)
   AND JSON_SEARCH(gia_tri, 'one', 'linux', NULL, '$.ban[*].ma') IS NOT NULL
   AND JSON_UNQUOTE(JSON_EXTRACT(gia_tri, REPLACE(JSON_UNQUOTE(JSON_SEARCH(gia_tri, 'one', 'linux', NULL, '$.ban[*].ma')), '.ma', '.moTa')))
       <> 'Bản Linux 64-bit một tệp .AppImage, có sẵn Java đi kèm';

-- Web doc lai CMS sau toi da 15 giay (bo dem quaDem), khong can khoi dong lai.
