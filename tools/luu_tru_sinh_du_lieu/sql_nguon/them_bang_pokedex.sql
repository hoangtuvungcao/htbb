-- ==================================================================================
--  CHE DO POKEMON - Pokedex (viec G7.D2/D3, docs/kien-truc.md)
-- ==================================================================================
--
--      mysql -u root -p <ten_db> < du_lieu/sql_nguon/them_bang_pokedex.sql
--
--  Khi sv.che_do_pokemon=1 server cung TU TAO hai bang nay luc khoi dong neu thieu
--  (data.DatabasePokedex.taoBang(), goi trong Database.themCotPokemon()). Chay lai an toan.
--
--  tob_pokedex        : moi dong = mot loai (dex cua template quai) nhan vat da gap.
--                       so_ha = so con da ha (0 = moi gap/danh trung, chua ha).
--                       lan_dau = lan dau gap/ha. Ghi theo lo luc saveChar/saveCharAuto.
--  tob_pokedex_thuong : moc thuong Pokedex (10/25/50/100 loai da ha) da nhan - khoa chinh
--                       (id_char, moc) chan nhan hai lan (INSERT IGNORE truoc khi phat).
-- ==================================================================================

CREATE TABLE IF NOT EXISTS `tob_pokedex` (
  `id_char` INT NOT NULL,
  `dex`     SMALLINT NOT NULL,
  `so_ha`   INT NOT NULL DEFAULT 0,
  `lan_dau` DATETIME NOT NULL,
  PRIMARY KEY (`id_char`, `dex`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `tob_pokedex_thuong` (
  `id_char`   INT NOT NULL,
  `moc`       INT NOT NULL,
  `thoi_gian` DATETIME NOT NULL,
  PRIMARY KEY (`id_char`, `moc`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
