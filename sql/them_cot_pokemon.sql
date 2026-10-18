-- ==================================================================================
--  CHE DO POKEMON - cot loai/shiny/he2  (viec S1, docs/kien-truc.md)
-- ==================================================================================
--
--  Chay TRUOC sql/pokemon_du_lieu_quai.sql (file do UPDATE cac cot nay).
--
--      mysql -u root -p <ten_db> < sql/them_cot_pokemon.sql
--
--  Khi sv.che_do_pokemon=1, server cung TU THEM cac cot nay luc khoi dong neu thieu
--  (Database.themCotPokemon(), goi trong server/TeamServer.java truoc khi nap template
--  quai va nhan nguoi choi). Tat co thi server khong doc/ghi cot nao o day.
--
--  An toan voi ma nguon: moi cau SELECT * FROM tob_char / data_monster trong src/ doc
--  theo TEN cot (rs.getX("ten")), khong doc theo chi so - da grep 16/10/2026. Them cot
--  vao cuoi bang khong lam lech gi. Doc dex/shiny boc try rieng (Database.docCotPokemon),
--  ghi bang cau UPDATE rieng (Database.luuCotPokemon) - thieu cot chi mat dex, khong
--  khoa nhan vat.
--
--  tob_char.dex     : so Pokedex loai dang mang. 0 = suy tu class + cap luc nap.
--  tob_char.shiny   : 1 = ban shiny.
--  data_monster.dex : loai Pokemon cua template (0 = khong ve bang PokeBody, khong gui -91).
--  data_monster.he2 : he thu hai (0..17), -1 = chi mot he. Cot he co san la he 1.
--
--  Chay lai lan hai se bao "Duplicate column name" - vo hai (khong dung ADD COLUMN IF NOT
--  EXISTS vi chi MariaDB moi co).
--  Sao luu truoc: vps/backup_db.sh
-- ==================================================================================

ALTER TABLE `tob_char` ADD COLUMN `dex`   SMALLINT NOT NULL DEFAULT 0 COMMENT 'Pokemon: so Pokedex, 0 = suy tu class+cap',
                       ADD COLUMN `shiny` TINYINT  NOT NULL DEFAULT 0 COMMENT 'Pokemon: 1 = shiny';

ALTER TABLE `data_monster` ADD COLUMN `dex`   SMALLINT NOT NULL DEFAULT 0  COMMENT 'Pokemon: so Pokedex cua template',
                           ADD COLUMN `shiny` TINYINT  NOT NULL DEFAULT 0  COMMENT 'Pokemon: 1 = shiny',
                           ADD COLUMN `he2`   TINYINT  NOT NULL DEFAULT -1 COMMENT 'Pokemon: he thu hai, -1 = khong co';
