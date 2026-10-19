-- Migration (chay lai duoc): anh NPC = Pokemon (P2 tai nguyen). Chay SAU sql/pokemon_ten_hien.sql.
--   mysql htbb_db < sql/pokemon_npc_sprite.sql
-- data_npc.dex  = so Pokedex ve NPC tinh (client NPC.POKE_DEX_THEO_TYPE, Unity NPC.cs, du_lieu/bang_npc.csv).
-- data_npc.sprite = khoa tai nguyen client: 'pk:<dex 4 so>' (client_jar/res/pk, Unity Resources/poke).
--   Anh du phong npc.sh / npc.bytes cung la Pokemon do (tools/pokemon/sinh_npc_sh.py).
-- data_npc_su_kien_sprite = NPC su kien (lenh -50) theo idImg: dex Pokemon HOAC vat_the (pk/vt_npc<id>.png).
-- test/KiemNpcSprite doi chieu file nay voi jar, Unity va du_lieu/*.csv. Khong dung INSERT...SELECT...ON DUPLICATE.
SET NAMES utf8;

ALTER TABLE `data_npc` ADD COLUMN IF NOT EXISTS `dex` int NOT NULL DEFAULT 0;
ALTER TABLE `data_npc` ADD COLUMN IF NOT EXISTS `sprite` varchar(32) NOT NULL DEFAULT '';

UPDATE `data_npc` SET `dex`=113, `sprite`='pk:0113' WHERE `type`=0;
UPDATE `data_npc` SET `dex`=352, `sprite`='pk:0352' WHERE `type`=1;
UPDATE `data_npc` SET `dex`=68, `sprite`='pk:0068' WHERE `type`=2;
UPDATE `data_npc` SET `dex`=437, `sprite`='pk:0437' WHERE `type`=3;
UPDATE `data_npc` SET `dex`=59, `sprite`='pk:0059' WHERE `type`=4;
UPDATE `data_npc` SET `dex`=178, `sprite`='pk:0178' WHERE `type`=5;
UPDATE `data_npc` SET `dex`=53, `sprite`='pk:0053' WHERE `type`=6;
UPDATE `data_npc` SET `dex`=18, `sprite`='pk:0018' WHERE `type`=7;
UPDATE `data_npc` SET `dex`=143, `sprite`='pk:0143' WHERE `type`=8;
UPDATE `data_npc` SET `dex`=115, `sprite`='pk:0115' WHERE `type`=9;
UPDATE `data_npc` SET `dex`=131, `sprite`='pk:0131' WHERE `type`=10;
UPDATE `data_npc` SET `dex`=76, `sprite`='pk:0076' WHERE `type`=11;
UPDATE `data_npc` SET `dex`=76, `sprite`='pk:0076' WHERE `type`=12;
UPDATE `data_npc` SET `dex`=76, `sprite`='pk:0076' WHERE `type`=13;
UPDATE `data_npc` SET `dex`=76, `sprite`='pk:0076' WHERE `type`=14;
UPDATE `data_npc` SET `dex`=76, `sprite`='pk:0076' WHERE `type`=15;
UPDATE `data_npc` SET `dex`=128, `sprite`='pk:0128' WHERE `type`=16;
UPDATE `data_npc` SET `dex`=128, `sprite`='pk:0128' WHERE `type`=17;
UPDATE `data_npc` SET `dex`=128, `sprite`='pk:0128' WHERE `type`=18;
UPDATE `data_npc` SET `dex`=128, `sprite`='pk:0128' WHERE `type`=19;
UPDATE `data_npc` SET `dex`=128, `sprite`='pk:0128' WHERE `type`=20;
UPDATE `data_npc` SET `dex`=257, `sprite`='pk:0257' WHERE `type`=21;
UPDATE `data_npc` SET `dex`=52, `sprite`='pk:0052' WHERE `type`=22;
UPDATE `data_npc` SET `dex`=324, `sprite`='pk:0324' WHERE `type`=23;
UPDATE `data_npc` SET `dex`=302, `sprite`='pk:0302' WHERE `type`=24;
UPDATE `data_npc` SET `dex`=214, `sprite`='pk:0214' WHERE `type`=25;
UPDATE `data_npc` SET `dex`=352, `sprite`='pk:0352' WHERE `type`=26;
UPDATE `data_npc` SET `dex`=437, `sprite`='pk:0437' WHERE `type`=27;
UPDATE `data_npc` SET `dex`=83, `sprite`='pk:0083' WHERE `type`=28;
UPDATE `data_npc` SET `dex`=53, `sprite`='pk:0053' WHERE `type`=29;
UPDATE `data_npc` SET `dex`=115, `sprite`='pk:0115' WHERE `type`=30;
UPDATE `data_npc` SET `dex`=52, `sprite`='pk:0052' WHERE `type`=31;

CREATE TABLE IF NOT EXISTS `data_npc_su_kien_sprite` (
  `id_img` smallint NOT NULL,
  `dex` smallint NOT NULL DEFAULT 0,
  `vat_the` varchar(32) NOT NULL DEFAULT '',
  `ten` varchar(64) NOT NULL DEFAULT '',
  PRIMARY KEY (`id_img`)
) DEFAULT CHARSET=utf8;
REPLACE INTO `data_npc_su_kien_sprite` (`id_img`,`dex`,`vat_the`,`ten`) VALUES
(13,0,'vt_npc13','Cổng dịch chuyển'),
(14,52,'','Meowth'),
(15,0,'vt_npc15','Biển khu'),
(20,282,'','Gardevoir'),
(21,667,'','Litleo'),
(23,460,'','Abomasnow'),
(24,225,'','Delibird'),
(25,0,'vt_npc25','Nồi bánh chưng'),
(26,0,'vt_npc26','Nồi bánh tét'),
(27,448,'','Lucario'),
(28,214,'','Heracross'),
(29,185,'','Sudowoodo'),
(30,185,'','Sudowoodo');
