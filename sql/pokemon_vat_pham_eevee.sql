-- ==================================================================================
--  CHE DO POKEMON - 8 DA TIEN HOA EEVEE  (viec I2, docs/pokemon/KE_HOACH_CHUYEN_DOI.md)
-- ==================================================================================
--
--      mysql -u root -p <ten_db> < sql/pokemon_vat_pham_eevee.sql
--
--  Id 960..967: da grep src/, sql/ va docs/danh_muc_item.md ngay 16/10/2026 - id lon nhat
--  dang dung la 954, khong cho nao nhac toi 955..967. PHAI khop
--  src/com/htbb/pokemon/DaTienHoa.ID_ITEM (test/KiemPokemonServer.java doi chieu).
--
--  type 28 = vat pham tieu hao (cung loai "Ruong may man" 735, "The doi ten" 836), dung
--  qua Map.doUseItem (goi 29). Khi sv.che_do_pokemon=1, real.TienHoaPokemon.dungDaTienHoa
--  bat lay truoc: chi Eevee (dex 133) tu cap 25 dung duoc, tru 1 da roi tien hoa. Tat co
--  thi da di duong cu nhu moi item type 28 khac (khong co tac dung).
--
--  Cot theo dung thu tu CREATE TABLE data_item trong sql/gamedb7_clean.sql. Mau lay tu
--  dong 836 "The doi ten": he=-1, level=1, price=0, clazz=-1, nloan=0 (khong het han).
--  icon (xstart) tam dung 747 (anh "Da tien giai"); viec I1 se doi icon theo RawAsset/Item.
--  KHONG chay lai hai lan (trung khoa chinh se bao loi, vo hai).
-- ==================================================================================

SET NAMES utf8;

INSERT INTO `data_item` (`id`, `name`, `type`, `style`, `he`, `gender`, `level`, `durable`, `atb0`, `atb1`, `atb2`, `atb3`, `atb4`, `atb5`, `atb6`, `atb7`, `atb8`, `atb9`, `price`, `clazz`, `xstart`, `ystart`, `colortype`, `nloan`, `idUpLevel`, `namein`, `ideff`, `pricecu`) VALUES
(960,'Đá Nước',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,747,0,-1,0,-1,'',-1,0),
(961,'Đá Sấm',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,747,0,-1,0,-1,'',-1,0),
(962,'Đá Lửa',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,747,0,-1,0,-1,'',-1,0),
(963,'Đá Mặt Trời',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,747,0,-1,0,-1,'',-1,0),
(964,'Đá Mặt Trăng',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,747,0,-1,0,-1,'',-1,0),
(965,'Đá Lá',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,747,0,-1,0,-1,'',-1,0),
(966,'Đá Băng',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,747,0,-1,0,-1,'',-1,0),
(967,'Ruy Băng Tiên',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,747,0,-1,0,-1,'',-1,0);
