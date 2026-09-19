-- ============================================================================
-- Bang `tob_top_thuong` - phat qua đua top tu trang quan tri web.
--
-- KHONG BAT BUOC CHAY TAY: ca server game (src/real/SuKienDuaTop.java) lan trang
-- web (web/admin/dua_top.php) deu tu CREATE TABLE IF NOT EXISTS. File nay co de
-- chay tay truoc khi khoi dong lai server, hoac doi chieu ten cot.
--
--     mysql -u root kpah_db < sql/them_bang_tob_top_thuong.sql
--
-- VI SAO CAN (19/09/2026): truoc day "dua top" chi co mot co bat/tat rieng
-- (SuKien.DUA_TOP_HOA_LU), khong gan voi phan thuong nao. Bang nay ghi tung dong
-- "hang X -> nhan vat Y -> phan thuong Z"; server doc MOI LAN nguoi choi mo NPC
-- Tong Quan (npcType -10) - CHI nhan vat co ten trong bang VA CHUA nhan moi thay
-- muc menu "Nhan thuong đua top".
--
-- Khong UNIQUE theo vi_tri: dua top lap lai nhieu dot, dong cua dot truoc (da
-- nhan hay chua) van giu lai de tra cuu lich su.
--
-- item: cung cu phap chuoi phan thuong voi giftcode (xem web/admin/inc/thuong.php
-- va data/DatabaseGiftCode.applyItemString()), vd "ITEM:848:1000,GEM:73:100:-1".
--
-- item_chon (them 19/09/2026): phan thuong DE NGUOI CHOI TU CHON, cu phap
-- "nhan1=chuoi1|nhan2=chuoi2|..." - moi doan la MOT lua chon hien trong menu NPC
-- Tong Quan luc nhan thuong (xem real/SuKienDuaTop.doChonThuong). Co the co CA
-- item (cong ngay) LAN item_chon (cho chon them) tren cung mot dong.
-- ============================================================================

CREATE TABLE IF NOT EXISTS `tob_top_thuong` (
  `id`         bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `vi_tri`     tinyint(4)   NOT NULL                          COMMENT 'hang 1..10',
  `charname`   varchar(50)  NOT NULL                          COMMENT 'nhan vat duoc thuong',
  `item`       text         NULL                              COMMENT 'chuoi phan thuong co dinh, cu phap giong giftcode.item',
  `item_chon`  text         NULL                              COMMENT 'phan thuong de nguoi choi tu chon: "nhan=chuoi|nhan=chuoi|..."',
  `da_nhan`    tinyint(4)   NOT NULL DEFAULT 0                COMMENT '0 cho nhan, 1 da nhan',
  `admin_user` varchar(50)  NOT NULL DEFAULT ''               COMMENT 'team_user.username nguoi tao',
  `tao_luc`    datetime     NOT NULL DEFAULT current_timestamp(),
  `nhan_luc`   datetime     NULL,
  PRIMARY KEY (`id`),
  KEY `idx_charname_da_nhan` (`charname`, `da_nhan`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Bang tao truoc khi co item_chon thi them cot:
--   ALTER TABLE `tob_top_thuong` ADD COLUMN `item_chon` text NULL AFTER `item`;
-- (server va web tu lam viec nay, chi ghi ra day de biet.)

-- Kiem tra lai: phai ra 1.
SELECT COUNT(*) AS co_bang FROM information_schema.tables
 WHERE table_schema = 'kpah_db' AND table_name = 'tob_top_thuong';
