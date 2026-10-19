-- LICH SU KIEN NAM (chu game duyet 19/10/2026). Idempotent, MariaDB >= 10.0.2.
-- May chu game tu tao bang / cot luc khoi dong (data/DatabaseLichNam, DatabaseSuKien.themCotThieu) va tu ghi lich
-- cac nam (nam truoc .. nam sau). Chay tep nay truoc khi deploy web de /admin/sukien va /api/su-kien khong bao loi
-- trong vai giay dau.
--   mysql htbb_db < sql/them_lich_su_kien_nam.sql

-- 1. Moi lan dien ra cua moi su kien (com.htbb.pokemon.LichNam). ghi_de: 0 theo lich, 1 tat nam nay, 2 doi gio.
--    Cung DDL voi DatabaseLichNam.SQL_TAO_BANG.
CREATE TABLE IF NOT EXISTS tob_su_kien_lich (
  ma VARCHAR(40) NOT NULL,
  nam SMALLINT NOT NULL,
  lan TINYINT NOT NULL DEFAULT 0,
  ten VARCHAR(100) NOT NULL DEFAULT '',
  nhom VARCHAR(40) NOT NULL DEFAULT '',
  ngay_le VARCHAR(60) NOT NULL DEFAULT '',
  mo_ta VARCHAR(500) NOT NULL DEFAULT '',
  phan_thuong VARCHAR(300) NOT NULL DEFAULT '',
  lich_mo DATETIME NOT NULL,
  lich_dong DATETIME NOT NULL,
  mo_luc DATETIME NOT NULL,
  dong_luc DATETIME NOT NULL,
  ghi_de TINYINT NOT NULL DEFAULT 0,
  sua_boi VARCHAR(50) NULL,
  sua_luc DATETIME NULL,
  cap_nhat DATETIME NULL,
  PRIMARY KEY (ma, nam, lan), KEY k_mo (mo_luc), KEY k_dong (dong_luc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. tob_su_kien.tu_dong: 1 = theo lich nam (mac dinh), 0 = admin chuyen sang thu cong.
ALTER TABLE tob_su_kien ADD COLUMN IF NOT EXISTS tu_dong TINYINT NOT NULL DEFAULT 1;

-- 3. Moc hen cu cua lich 52 tuan ISO (vd blackfriday 16/11, noel 14/12) khong con dung: lich nam tu lo cac ma nay.
--    Chi xoa moc do LICH CU dat (sua_boi rong hoac 'lich hen'), khong dong moc admin hen tay.
UPDATE tob_su_kien SET mo_luc = NULL, dong_luc = NULL
 WHERE tu_dong = 1 AND (sua_boi IS NULL OR sua_boi = 'lich hen')
   AND ma IN ('tetduonglich','tet2017','tet123','moi:valentine','quocte83','gioto','moi:3004','moi:thieunhi','he2017',
              'moi:hamngucmua','vulan','moi:quockhanh','trungthu','moi:2010','halloween','demhalloween','sinhnhat2011',
              'blackfriday','blackfridaydaugia','blackfridayban','noel','demnoel','moi:tongket','moi:tuanhe',
              'moi:congdong','moi:shiny','moi:dotkich','moi:muapvp','moi:pokedex','moi:chienvung','moi:bungphat');

-- 4. Su kien rieng cua game kiem hiep cu da bo khoi danh muc (World Cup binh chon Messi/Ronaldo, Sinh nhat Ngude,
--    Dua top Hoa Lu): may chu bo qua ma la, xoa dong cho trang /admin/sukien gon.
DELETE FROM tob_su_kien WHERE ma IN ('worldcup','worldcupnhanqua','worldcup2017','snngude','duatophoalu');
