-- ==================================================================================
--  SU KIEN WORLD CUP - binh chon Messi / Ronaldo
-- ==================================================================================
--
--  Chay MOT LAN tren may chu, roi khoi dong lai server (bang data_potion chi nap luc
--  bat may):
--
--      mysql -u root -p <ten_db> < du_lieu/sql_nguon/them_su_kien_world_cup.sql
--
--  Sau do mo su kien bang cach dien ba dong nay trong server.ini (hoac trong
--  vps/server.ini.tmpl neu dung pipeline GitHub), roi restart / go lenh nap lai:
--
--      sv.worldCupBatDau=2026-09-05 00:00:00
--      sv.worldCupKetThuc=2026-10-05 23:59:59
--      sv.worldCupHanNhanQua=2026-10-12 23:59:59
--
--  Ma nguon: src/real/SuKienWorldCup.java, src/data/DatabaseSuKienWorldCup.java
--
--  VAT PHAM. Su kien dung ba dong thuoc, hai dong dau da co san:
--      161 Qua bong vang   - nguyen lieu binh chon, quai roi ra
--      143 Hop qua bi mat  - qua kem khi nhan thuong cuoi su kien
--      108 Hop qua may man - MOI, tep nay dat ten cho no
--
--  VI SAO LA 108 MA KHONG PHAI 162: data_potion dang co dung 162 dong (id 0..161) va
--  Char.SO_O_THUOC = 162 phai khop voi con so do. Client cap phat mang listPotion
--  theo so dong server gui trong goi dang nhap, gui id vuot tran la nguoi choi thay
--  CA HANH TRANG RONG (chu thich day du o Char.getAllPotion). 108 la mot trong nam
--  dong trong san co (106, 107, 108, 109, 110) nen dung lai duoc ma khong doi so dong.
--
--  108 cung da duoc them vao LoginHandler.POTION_SHOP - thieu buoc do thi nguoi choi
--  KHONG MO DUOC chi tiet hop qua (client tra cuu Res.potionTemplates[108-19] vuot bien).
-- ==================================================================================

-- ---------------------------------------------------------------------------------
--  1. Dat ten / icon cho dong thuoc 108
-- ---------------------------------------------------------------------------------
--  idImage 220 = tui qua (dung chung voi 147 "Tui qua may man"), map/icon/220.png
--  isTrade 0   = khong trao doi duoc, giong 143 va 161
--  delay 0     = khong co thoi gian hoi
UPDATE `data_potion`
   SET `idImage` = 220,
       `delay`   = 0,
       `isTrade` = 0,
       `name`    = 'Hộp quà may mắn
Quà bình chọn World Cup.
Bấm hai lần để mở, nhận ngẫu nhiên phi phong, nguyên liệu, xu/lượng hoặc pet.',
       `name2`   = 'hopquamayman',
       `price`   = 0,
       `recovered` = 0
 WHERE `id` = 108;

-- ---------------------------------------------------------------------------------
--  2. Bang diem binh chon
-- ---------------------------------------------------------------------------------
--  Server cung tu tao hai bang nay lan dau chay (DatabaseSuKienWorldCup.taoBang),
--  de o day de chay truoc va de xem duoc cau truc.
CREATE TABLE IF NOT EXISTS `tob_top_event_world_cup` (
  `charname`      VARCHAR(100) NOT NULL,
  `point_messi`   INT NOT NULL DEFAULT 0,
  `point_ronaldo` INT NOT NULL DEFAULT 0,
  `claim_normal`  TINYINT NOT NULL DEFAULT 0,
  `claim_top`     TINYINT NOT NULL DEFAULT 0,
  `timeupdate`    DATETIME NULL,
  PRIMARY KEY (`charname`),
  KEY `idx_messi` (`point_messi`),
  KEY `idx_ronaldo` (`point_ronaldo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------------------
--  3. Trang thai su kien (ben thang, da chot chua)
-- ---------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tob_event_world_cup_state` (
  `event_key`    VARCHAR(64) NOT NULL,
  `winner_side`  TINYINT NOT NULL DEFAULT 0,
  `is_finalized` TINYINT NOT NULL DEFAULT 0,
  `finalized_by` VARCHAR(100) NULL,
  `timeupdate`   DATETIME NULL,
  PRIMARY KEY (`event_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `tob_event_world_cup_state` (`event_key`, `winner_side`, `is_finalized`, `timeupdate`)
VALUES ('WORLD_CUP', 0, 0, NOW())
ON DUPLICATE KEY UPDATE `event_key` = `event_key`;

-- ---------------------------------------------------------------------------------
--  KIEM TRA
-- ---------------------------------------------------------------------------------
-- SELECT id, idImage, name FROM data_potion WHERE id IN (108, 143, 161);
-- SELECT COUNT(*) FROM data_potion;   -- phai van la 162
