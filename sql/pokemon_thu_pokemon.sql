-- SINH TU tools/pokemon/sinh_bang_thu.py - KHONG SUA TAY (sua bang trong tool roi chay lai).
-- P2: anh xa thu cuoi / pet / skin linh thu / hoa hinh -> Pokemon (dex PMDCollab, shiny).
-- loai: thu_cuoi (id = data.Animal.idImg), pet_item (id = data_item.id), pet_cu (id = PetTemplate.idImg),
--       skin (id = real.SkinThuCuoi 0..5), hoa_hinh_pet (id = cap 1..5), hoa_hinh_thu (id 0).
-- Chay lai bao nhieu lan cung duoc (CREATE IF NOT EXISTS + REPLACE INTO, khong INSERT...SELECT).
-- SO_DONG: 95 (test KiemThuPetSql dem lai)
CREATE TABLE IF NOT EXISTS `data_thu_pokemon` (
  `loai` varchar(16) NOT NULL,
  `id` int(11) NOT NULL,
  `dex` smallint(6) NOT NULL,
  `shiny` tinyint(1) NOT NULL DEFAULT 0,
  `ten` varchar(64) NOT NULL,
  PRIMARY KEY (`loai`, `id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('hoa_hinh_pet', 1, 58, 0, 'Growlithe');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('hoa_hinh_pet', 2, 58, 0, 'Growlithe');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('hoa_hinh_pet', 3, 58, 0, 'Growlithe');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('hoa_hinh_pet', 4, 59, 0, 'Arcanine');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('hoa_hinh_pet', 5, 244, 0, 'Entei');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('hoa_hinh_thu', 0, 244, 0, 'Entei');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 0, 607, 0, 'Litwick');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 1, 276, 0, 'Taillow');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 2, 165, 0, 'Ledyba');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 3, 41, 0, 'Zubat');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 4, 370, 0, 'Luvdisc');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 5, 270, 0, 'Lotad');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 6, 120, 0, 'Staryu');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 7, 615, 0, 'Cryogonal');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 8, 77, 0, 'Ponyta');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 9, 27, 0, 'Sandshrew');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 10, 417, 0, 'Pachirisu');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 11, 29, 0, 'Nidoran♀');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 12, 129, 0, 'Magikarp');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 13, 393, 0, 'Piplup');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 14, 179, 0, 'Mareep');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 15, 385, 0, 'Jirachi');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 16, 146, 0, 'Moltres');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 17, 144, 0, 'Articuno');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 18, 121, 0, 'Starmie');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 19, 337, 0, 'Lunatone');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_cu', 20, 190, 0, 'Aipom');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 681, 77, 0, 'Ponyta lửa');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 682, 78, 0, 'Rapidash lửa');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 683, 78, 1, 'Rapidash lửa Shiny');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 684, 163, 0, 'Trứng Hoothoot');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 685, 147, 0, 'Trứng Dratini');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 686, 173, 0, 'Trứng Cleffa');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 687, 41, 0, 'Trứng Zubat');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 688, 627, 0, 'Trứng Rufflet');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 689, 163, 0, 'Hoothoot cấp 1');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 690, 163, 0, 'Hoothoot cấp 2');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 691, 164, 0, 'Noctowl cấp 3');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 692, 164, 0, 'Noctowl cấp 4');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 693, 164, 1, 'Noctowl Shiny cấp 5');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 694, 147, 0, 'Dratini cấp 1');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 695, 147, 0, 'Dratini cấp 2');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 696, 148, 0, 'Dragonair cấp 3');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 697, 148, 0, 'Dragonair cấp 4');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 698, 149, 0, 'Dragonite cấp 5');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 699, 173, 0, 'Cleffa cấp 1');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 700, 35, 0, 'Clefairy cấp 2');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 701, 35, 0, 'Clefairy cấp 3');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 702, 36, 0, 'Clefable cấp 4');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 703, 36, 1, 'Clefable Shiny cấp 5');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 704, 41, 0, 'Zubat cấp 1');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 705, 41, 0, 'Zubat cấp 2');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 706, 42, 0, 'Golbat cấp 3');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 707, 42, 0, 'Golbat cấp 4');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 708, 169, 0, 'Crobat cấp 5');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 709, 627, 0, 'Rufflet cấp 1');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 710, 627, 0, 'Rufflet cấp 2');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 711, 628, 0, 'Braviary cấp 3');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 712, 628, 0, 'Braviary cấp 4');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 713, 628, 1, 'Braviary Shiny cấp 5');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 727, 417, 0, 'Pachirisu');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 728, 225, 0, 'Delibird');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 790, 100, 1, 'Voltorb vàng');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 802, 403, 1, 'Shinx vàng');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 803, 667, 0, 'Litleo');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 804, 58, 0, 'Growlithe');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 832, 585, 0, 'Deerling');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('pet_item', 873, 399, 0, 'Bidoof');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('skin', 0, 879, 0, 'Copperajah');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('skin', 1, 675, 0, 'Pangoro');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('skin', 2, 716, 0, 'Xerneas');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('skin', 3, 323, 0, 'Camerupt');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('skin', 4, 745, 0, 'Lycanroc');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('skin', 5, 483, 0, 'Dialga');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 0, 78, 0, 'Rapidash');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 1, 59, 0, 'Arcanine');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 2, 128, 0, 'Tauros');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 3, 262, 0, 'Mightyena');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 4, 18, 0, 'Pidgeot');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 5, 146, 0, 'Moltres');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 6, 428, 1, 'Lopunny Shiny');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 7, 234, 0, 'Stantler');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 8, 144, 0, 'Articuno');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 9, 831, 0, 'Wooloo');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 10, 245, 0, 'Suicune');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 11, 221, 0, 'Piloswine');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 12, 897, 0, 'Spectrier');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 13, 149, 0, 'Dragonite');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 14, 250, 0, 'Ho-Oh');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 15, 357, 0, 'Tropius');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 16, 227, 0, 'Skarmory');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 17, 330, 0, 'Flygon');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 18, 473, 0, 'Mamoswine');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 19, 244, 0, 'Entei');
REPLACE INTO `data_thu_pokemon` (`loai`, `id`, `dex`, `shiny`, `ten`) VALUES ('thu_cuoi', 20, 896, 0, 'Glastrier');
