-- 0.9.6: tên chiêu kiểu Pokémon (không dịch kiểu kiếm hiệp): "Múa kiếm" -> "Vũ điệu kiếm" (Swords Dance).
-- Chạy được nhiều lần (idempotent). Seed đầy đủ htbb_day_du.sql đã có tên mới.
UPDATE `data_noi_dung_chieu` SET `ten` = 'Vũ điệu kiếm' WHERE `ten` = 'Múa kiếm';
UPDATE `data_noi_dung_chieu` SET `trang_thai` = REPLACE(`trang_thai`, 'Múa kiếm', 'Vũ điệu kiếm') WHERE `trang_thai` LIKE '%Múa kiếm%';
