-- SINH TU tools/pokemon/sinh_bang_quai.py - KHONG SUA TAY.
-- Chay SAU sql/them_cot_pokemon.sql. he = he 1 Pokemon (0..17, com.kpah.pokemon.HePokemon).
-- Chi dung cho server che_do_pokemon=1: cot he khong con la ngu hanh 0..4.
UPDATE data_monster SET name='Sandshrew', dex=27, shiny=0, he=8, he2=-1 WHERE id=1; -- Nhím -> Sandshrew
UPDATE data_monster SET name='Caterpie', dex=10, shiny=0, he=11, he2=-1 WHERE id=2; -- Sâu -> Caterpie
UPDATE data_monster SET name='Wooper', dex=194, shiny=0, he=2, he2=8 WHERE id=3; -- Giọt nước -> Wooper
UPDATE data_monster SET name='Torchic', dex=255, shiny=0, he=1, he2=-1 WHERE id=4; -- Gà điên -> Torchic
UPDATE data_monster SET name='Ekans', dex=23, shiny=0, he=7, he2=-1 WHERE id=5; -- Rắn lục -> Ekans
UPDATE data_monster SET name='Litwick', dex=607, shiny=0, he=13, he2=1 WHERE id=6; -- Ma trơi -> Litwick
UPDATE data_monster SET name='Bellsprout', dex=69, shiny=0, he=4, he2=7 WHERE id=7; -- Nắp ấm -> Bellsprout
UPDATE data_monster SET name='Venonat', dex=48, shiny=0, he=11, he2=7 WHERE id=8; -- Rệp quỷ -> Venonat
UPDATE data_monster SET name='Rattata', dex=19, shiny=0, he=0, he2=-1 WHERE id=9; -- Chuột cống -> Rattata
UPDATE data_monster SET name='Oddish', dex=43, shiny=0, he=4, he2=7 WHERE id=10; -- Quỷ hoa -> Oddish
UPDATE data_monster SET name='Weedle', dex=13, shiny=0, he=11, he2=7 WHERE id=11; -- Sâu róm -> Weedle
UPDATE data_monster SET name='Vulpix', dex=37, shiny=0, he=1, he2=-1 WHERE id=12; -- Lửa ma -> Vulpix
UPDATE data_monster SET name='Quagsire', dex=195, shiny=0, he=2, he2=8 WHERE id=13; -- Giọt nước lớn -> Quagsire
UPDATE data_monster SET name='Combusken', dex=256, shiny=0, he=1, he2=6 WHERE id=14; -- Gà con -> Combusken
UPDATE data_monster SET name='Tepig', dex=498, shiny=0, he=1, he2=-1 WHERE id=15; -- Heo mọi -> Tepig
UPDATE data_monster SET name='Skorupi', dex=451, shiny=0, he=7, he2=11 WHERE id=16; -- Bọ cạp -> Skorupi
UPDATE data_monster SET name='Torkoal', dex=324, shiny=0, he=1, he2=-1 WHERE id=17; -- Rùa đỏ -> Torkoal
UPDATE data_monster SET name='Ariados', dex=168, shiny=0, he=11, he2=7 WHERE id=18; -- Nhện tím -> Ariados
UPDATE data_monster SET name='Paras', dex=46, shiny=0, he=11, he2=4 WHERE id=19; -- Nấm ma -> Paras
UPDATE data_monster SET name='Gloom', dex=44, shiny=0, he=4, he2=7 WHERE id=20; -- Quỷ sinh hoa -> Gloom
UPDATE data_monster SET name='Venipede', dex=543, shiny=0, he=11, he2=7 WHERE id=21; -- Rết -> Venipede
UPDATE data_monster SET name='Dodrio', dex=85, shiny=0, he=0, he2=9 WHERE id=22; -- Gà khổng lồ -> Dodrio
UPDATE data_monster SET name='Arbok', dex=24, shiny=0, he=7, he2=-1 WHERE id=23; -- Rắn khổng lồ -> Arbok
UPDATE data_monster SET name='Butterfree', dex=12, shiny=0, he=11, he2=9 WHERE id=24; -- Bướm khổng lồ -> Butterfree
UPDATE data_monster SET name='Gastly', dex=92, shiny=0, he=13, he2=7 WHERE id=25; -- Bóng ma -> Gastly
UPDATE data_monster SET name='Tentacool', dex=72, shiny=0, he=2, he2=7 WHERE id=26; -- Thủy nhãn -> Tentacool
UPDATE data_monster SET name='Cubone', dex=104, shiny=0, he=8, he2=-1 WHERE id=27; -- Skeleton -> Cubone
UPDATE data_monster SET name='Growlithe', dex=58, shiny=0, he=1, he2=-1 WHERE id=28; -- Cọp khổng lồ -> Growlithe
UPDATE data_monster SET name='Croconaw', dex=159, shiny=0, he=2, he2=-1 WHERE id=29; -- Cá sấu -> Croconaw
UPDATE data_monster SET name='Croagunk', dex=453, shiny=0, he=7, he2=6 WHERE id=30; -- Ếch quỷ -> Croagunk
UPDATE data_monster SET name='Kricketune', dex=402, shiny=0, he=11, he2=-1 WHERE id=31; -- Dế khổng lồ -> Kricketune
UPDATE data_monster SET name='Parasect', dex=47, shiny=0, he=11, he2=4 WHERE id=32; -- Nấm tinh -> Parasect
UPDATE data_monster SET name='Tauros', dex=128, shiny=0, he=0, he2=-1 WHERE id=33; -- Trâu núi -> Tauros
UPDATE data_monster SET name='Scraggy', dex=559, shiny=0, he=15, he2=6 WHERE id=34; -- Sơn tặc -> Scraggy
UPDATE data_monster SET name='Crawdaunt', dex=342, shiny=0, he=2, he2=15 WHERE id=35; -- Hải tặc -> Crawdaunt
UPDATE data_monster SET name='Bronzong', dex=437, shiny=0, he=16, he2=10 WHERE id=36; -- Long Trụ (sự kiện) -> Bronzong
UPDATE data_monster SET name='Bronzor', dex=436, shiny=0, he=16, he2=10 WHERE id=37; -- Long trụ phụ -> Bronzor
UPDATE data_monster SET name='Gyarados', dex=130, shiny=0, he=2, he2=9 WHERE id=38; -- Thuồng luồng -> Gyarados (BossThuongLuong)
UPDATE data_monster SET name='Salamence', dex=373, shiny=0, he=14, he2=9 WHERE id=39; -- Thằn lằn -> Salamence (BossThanLan)
UPDATE data_monster SET name='Whirlipede', dex=544, shiny=0, he=11, he2=7 WHERE id=40; -- Rết đỏ -> Whirlipede
UPDATE data_monster SET name='Scolipede', dex=545, shiny=0, he=11, he2=7 WHERE id=41; -- Rết tía -> Scolipede
UPDATE data_monster SET name='Whirlipede Shiny', dex=544, shiny=1, he=11, he2=7 WHERE id=42; -- Rết xanh -> Whirlipede shiny
UPDATE data_monster SET name='Sudowoodo', dex=185, shiny=0, he=12, he2=-1 WHERE id=43; -- Liên hoa trụ -> Sudowoodo
UPDATE data_monster SET name='Blaziken', dex=257, shiny=0, he=1, he2=6 WHERE id=44; -- Tử kê -> Blaziken
UPDATE data_monster SET name='Farfetch''d', dex=83, shiny=0, he=0, he2=9 WHERE id=45; -- Hồng kê -> Farfetch'd
UPDATE data_monster SET name='Aegislash', dex=681, shiny=0, he=16, he2=13 WHERE id=46; -- Tướng thủ thành -> Aegislash
UPDATE data_monster SET name='Seviper', dex=336, shiny=0, he=7, he2=-1 WHERE id=47; -- Rắn mang bành -> Seviper
UPDATE data_monster SET name='Arcanine', dex=59, shiny=0, he=1, he2=-1 WHERE id=48; -- Sư tử -> Arcanine
UPDATE data_monster SET name='Gastrodon', dex=423, shiny=0, he=2, he2=8 WHERE id=49; -- Ốc ma -> Gastrodon
UPDATE data_monster SET name='Dustox', dex=269, shiny=0, he=11, he2=7 WHERE id=50; -- Bướm nâu -> Dustox
UPDATE data_monster SET name='Trevenant', dex=709, shiny=0, he=13, he2=4 WHERE id=51; -- Ma cây -> Trevenant
UPDATE data_monster SET name='Haunter', dex=93, shiny=0, he=13, he2=7 WHERE id=52; -- Huyết ma -> Haunter
UPDATE data_monster SET name='Whiscash', dex=340, shiny=0, he=2, he2=8 WHERE id=53; -- Cá thòi lòi -> Whiscash
UPDATE data_monster SET name='Kingler', dex=99, shiny=0, he=2, he2=-1 WHERE id=54; -- Cua càng to -> Kingler
UPDATE data_monster SET name='Duskull', dex=355, shiny=0, he=13, he2=-1 WHERE id=55; -- Độc nhãn -> Duskull
UPDATE data_monster SET name='Sharpedo', dex=319, shiny=0, he=2, he2=15 WHERE id=56; -- Cá ma -> Sharpedo
UPDATE data_monster SET name='Dusclops', dex=356, shiny=0, he=13, he2=-1 WHERE id=57; -- Huyết nhãn -> Dusclops
UPDATE data_monster SET name='Vileplume', dex=45, shiny=0, he=4, he2=7 WHERE id=58; -- Quỷ hoa (lv78) -> Vileplume
UPDATE data_monster SET name='Marowak', dex=105, shiny=0, he=8, he2=-1 WHERE id=59; -- Skeleton lam -> Marowak
UPDATE data_monster SET name='Rhydon', dex=112, shiny=0, he=8, he2=12 WHERE id=60; -- Tê giác -> Rhydon
UPDATE data_monster SET name='Liepard', dex=510, shiny=0, he=15, he2=-1 WHERE id=61; -- Cọp tím -> Liepard
UPDATE data_monster SET name='Luxio', dex=404, shiny=0, he=3, he2=-1 WHERE id=62; -- Cọp xanh -> Luxio
UPDATE data_monster SET name='Litleo', dex=667, shiny=0, he=1, he2=0 WHERE id=63; -- Cọp đỏ -> Litleo
UPDATE data_monster SET name='Krokorok', dex=552, shiny=0, he=8, he2=15 WHERE id=64; -- Cá sấu đỏ -> Krokorok
UPDATE data_monster SET name='Crabrawler', dex=739, shiny=0, he=6, he2=-1 WHERE id=65; -- Cua đinh -> Crabrawler
UPDATE data_monster SET name='Feraligatr', dex=160, shiny=0, he=2, he2=-1 WHERE id=66; -- Cá sấu xanh -> Feraligatr
UPDATE data_monster SET name='Seismitoad', dex=537, shiny=0, he=2, he2=8 WHERE id=67; -- Cóc lam -> Seismitoad
UPDATE data_monster SET name='Palpitoad', dex=536, shiny=0, he=2, he2=8 WHERE id=68; -- Cóc lửa -> Palpitoad
UPDATE data_monster SET name='Toxicroak', dex=454, shiny=0, he=7, he2=6 WHERE id=69; -- Cóc xanh -> Toxicroak
UPDATE data_monster SET name='Volcarona', dex=637, shiny=0, he=11, he2=1 WHERE id=70; -- Dế lửa -> Volcarona
UPDATE data_monster SET name='Kricketot', dex=401, shiny=0, he=11, he2=-1 WHERE id=71; -- Dế cam -> Kricketot
UPDATE data_monster SET name='Heracross', dex=214, shiny=0, he=11, he2=6 WHERE id=72; -- Dế lam -> Heracross
UPDATE data_monster SET name='Tauros Shiny', dex=128, shiny=1, he=0, he2=-1 WHERE id=73; -- Trâu đỏ -> Tauros shiny
UPDATE data_monster SET name='Golem', dex=76, shiny=0, he=12, he2=8 WHERE id=74; -- Người đá -> Golem
UPDATE data_monster SET name='Mudsdale', dex=750, shiny=0, he=8, he2=-1 WHERE id=75; -- Trâu xanh -> Mudsdale
UPDATE data_monster SET name='Magcargo', dex=219, shiny=0, he=1, he2=12 WHERE id=76; -- Ốc sên -> Magcargo
UPDATE data_monster SET name='Pinsir', dex=127, shiny=0, he=11, he2=-1 WHERE id=77; -- Bọ hung -> Pinsir
UPDATE data_monster SET name='Scrafty', dex=560, shiny=0, he=15, he2=6 WHERE id=78; -- Sơn tặc 3 -> Scrafty
UPDATE data_monster SET name='Dhelmise', dex=781, shiny=0, he=13, he2=4 WHERE id=79; -- Hải tặc 1 -> Dhelmise
UPDATE data_monster SET name='Breloom', dex=286, shiny=0, he=4, he2=6 WHERE id=80; -- Nấm độc -> Breloom
UPDATE data_monster SET name='Scyther', dex=123, shiny=0, he=11, he2=9 WHERE id=81; -- Cánh cam -> Scyther
UPDATE data_monster SET name='Zapdos', dex=145, shiny=0, he=3, he2=9 WHERE id=82; -- Đại bàng -> Zapdos (huyền thoại)
UPDATE data_monster SET dex=0, shiny=0, he2=-1 WHERE id=83; -- Cổng thành -> Object PMD (cổng/khối)
UPDATE data_monster SET name='Yamask', dex=562, shiny=0, he=13, he2=-1 WHERE id=84; -- Cương thi -> Yamask
UPDATE data_monster SET dex=0, shiny=0, he2=-1 WHERE id=85; -- Đá (nguyên liệu khu mỏ) -> Object PMD
UPDATE data_monster SET dex=0, shiny=0, he2=-1 WHERE id=86; -- Bông (nguyên liệu) -> Object PMD
UPDATE data_monster SET dex=0, shiny=0, he2=-1 WHERE id=87; -- Gỗ (nguyên liệu) -> Object PMD
UPDATE data_monster SET dex=0, shiny=0, he2=-1 WHERE id=88; -- Da (nguyên liệu) -> Object PMD
UPDATE data_monster SET dex=0, shiny=0, he2=-1 WHERE id=89; -- Sắt (nguyên liệu) -> Object PMD
UPDATE data_monster SET name='Abomasnow', dex=460, shiny=0, he=4, he2=5 WHERE id=90; -- Người tuyết -> Abomasnow (BossNguoiTuyet)
UPDATE data_monster SET name='Serperior', dex=497, shiny=0, he=4, he2=-1 WHERE id=91; -- Rắn chị -> Serperior (BossLinhXa)
UPDATE data_monster SET name='Servine', dex=496, shiny=0, he=4, he2=-1 WHERE id=92; -- Rắn em -> Servine (BossBachXa)
UPDATE data_monster SET name='Gengar', dex=94, shiny=0, he=13, he2=7 WHERE id=93; -- Mắt quỷ -> Gengar
UPDATE data_monster SET name='Dusknoir', dex=477, shiny=0, he=13, he2=-1 WHERE id=94; -- Bạch cốt tướng quân -> Dusknoir (BossSkelonton)
UPDATE data_monster SET name='Ponyta', dex=77, shiny=0, he=1, he2=-1 WHERE id=95; -- Ngựa cá nhân trắng -> Ponyta
UPDATE data_monster SET name='Ponyta', dex=77, shiny=0, he=1, he2=-1 WHERE id=96; -- Ngựa cá nhân xanh -> Ponyta (đổi màu)
UPDATE data_monster SET name='Ponyta', dex=77, shiny=0, he=1, he2=-1 WHERE id=97; -- Ngựa cá nhân đỏ -> Ponyta (đổi màu)
UPDATE data_monster SET name='Ponyta', dex=77, shiny=0, he=1, he2=-1 WHERE id=98; -- Ngựa cá nhân xanh lá -> Ponyta (đổi màu)
UPDATE data_monster SET name='Ponyta', dex=77, shiny=0, he=1, he2=-1 WHERE id=99; -- Ngựa cá nhân vàng -> Ponyta (đổi màu)
UPDATE data_monster SET name='Ponyta Shiny', dex=77, shiny=1, he=1, he2=-1 WHERE id=100; -- Ngựa cá nhân tím -> Ponyta shiny
UPDATE data_monster SET name='Rapidash', dex=78, shiny=0, he=1, he2=-1 WHERE id=101; -- Ngựa bang trắng -> Rapidash
UPDATE data_monster SET name='Rapidash', dex=78, shiny=0, he=1, he2=-1 WHERE id=102; -- Ngựa bang xanh -> Rapidash (đổi màu)
UPDATE data_monster SET name='Rapidash', dex=78, shiny=0, he=1, he2=-1 WHERE id=103; -- Ngựa bang đỏ -> Rapidash (đổi màu)
UPDATE data_monster SET name='Rapidash', dex=78, shiny=0, he=1, he2=-1 WHERE id=104; -- Ngựa bang xanh lá -> Rapidash (đổi màu)
UPDATE data_monster SET name='Rapidash', dex=78, shiny=0, he=1, he2=-1 WHERE id=105; -- Ngựa bang vàng -> Rapidash (đổi màu)
UPDATE data_monster SET name='Rapidash Shiny', dex=78, shiny=1, he=1, he2=-1 WHERE id=106; -- Ngựa bang tím -> Rapidash shiny
UPDATE data_monster SET name='Mudsdale', dex=750, shiny=0, he=8, he2=-1 WHERE id=107; -- Ngựa quốc gia trắng -> Mudsdale
UPDATE data_monster SET name='Mudsdale', dex=750, shiny=0, he=8, he2=-1 WHERE id=108; -- Ngựa quốc gia xanh -> Mudsdale (đổi màu)
UPDATE data_monster SET name='Mudsdale', dex=750, shiny=0, he=8, he2=-1 WHERE id=109; -- Ngựa quốc gia đỏ -> Mudsdale (đổi màu)
UPDATE data_monster SET name='Mudsdale', dex=750, shiny=0, he=8, he2=-1 WHERE id=110; -- Ngựa quốc gia xanh lá -> Mudsdale (đổi màu)
UPDATE data_monster SET name='Mudsdale', dex=750, shiny=0, he=8, he2=-1 WHERE id=111; -- Ngựa quốc gia vàng -> Mudsdale (đổi màu)
UPDATE data_monster SET name='Mudsdale Shiny', dex=750, shiny=1, he=8, he2=-1 WHERE id=112; -- Ngựa quốc gia tím -> Mudsdale shiny
UPDATE data_monster SET name='Lopunny', dex=428, shiny=0, he=0, he2=-1 WHERE id=113; -- Boss thỏ điên -> Lopunny
UPDATE data_monster SET name='Shuppet', dex=353, shiny=0, he=13, he2=-1 WHERE id=114; -- Khô lâu -> Shuppet
UPDATE data_monster SET name='Ursaring', dex=217, shiny=0, he=0, he2=-1 WHERE id=115; -- Boss Gấu xám -> Ursaring
UPDATE data_monster SET name='Darkrai', dex=491, shiny=0, he=15, he2=-1 WHERE id=116; -- Dracula -> Darkrai (BossDracula)
UPDATE data_monster SET name='Gourgeist', dex=711, shiny=0, he=13, he2=4 WHERE id=117; -- Bí ngô -> Gourgeist (BossDracula/Halloween)
UPDATE data_monster SET name='Galvantula', dex=596, shiny=0, he=11, he2=3 WHERE id=118; -- Nhện ma -> Galvantula
UPDATE data_monster SET dex=0, shiny=0, he2=-1 WHERE id=119; -- cong thanh -> Object PMD
UPDATE data_monster SET name='Rayquaza', dex=384, shiny=0, he=14, he2=9 WHERE id=120; -- Trụ rồng -> Rayquaza (BossTruRong)
UPDATE data_monster SET dex=0, shiny=0, he2=-1 WHERE id=121; -- Rương ma quái -> Object PMD Chest
UPDATE data_monster SET dex=0, shiny=0, he2=-1 WHERE id=122; -- Ngọc 1 sao -> Object/Item PMD
UPDATE data_monster SET dex=0, shiny=0, he2=-1 WHERE id=123; -- Ngọc 2 sao -> Object/Item PMD
UPDATE data_monster SET dex=0, shiny=0, he2=-1 WHERE id=124; -- Ngọc 3 sao -> Object/Item PMD
UPDATE data_monster SET dex=0, shiny=0, he2=-1 WHERE id=125; -- Ngọc 4 sao -> Object/Item PMD
UPDATE data_monster SET dex=0, shiny=0, he2=-1 WHERE id=126; -- Ngọc 5 sao -> Object/Item PMD
UPDATE data_monster SET dex=0, shiny=0, he2=-1 WHERE id=127; -- Ngọc 6 sao -> Object/Item PMD
UPDATE data_monster SET dex=0, shiny=0, he2=-1 WHERE id=128; -- Ngọc 7 sao -> Object/Item PMD
UPDATE data_monster SET name='Kyogre', dex=382, shiny=0, he=2, he2=-1 WHERE id=129; -- Boss Thủy Tinh -> Kyogre (BossSonTinhThuyTinh)
UPDATE data_monster SET name='Groudon', dex=383, shiny=0, he=8, he2=-1 WHERE id=130; -- Boss Sơn Tinh -> Groudon (BossSonTinhThuyTinh)
UPDATE data_monster SET dex=0, shiny=0, he2=-1 WHERE id=131; -- Thùng gỗ -> Object PMD
UPDATE data_monster SET name='Marowak Shiny', dex=105, shiny=1, he=8, he2=-1 WHERE id=132; -- Skeleton lam (bản 2) -> Marowak shiny
UPDATE data_monster SET name='Haunter Shiny', dex=93, shiny=1, he=13, he2=7 WHERE id=133; -- Huyết ma (bản 2) -> Haunter shiny
UPDATE data_monster SET name='Seviper Shiny', dex=336, shiny=1, he=7, he2=-1 WHERE id=134; -- Rắn mang bành (bản 2) -> Seviper shiny
UPDATE data_monster SET name='Ursaluna', dex=901, shiny=0, he=8, he2=0 WHERE id=135; -- Gấu xám lv90 -> Ursaluna
UPDATE data_monster SET name='Liepard Shiny', dex=510, shiny=1, he=15, he2=-1 WHERE id=136; -- Cọp tím lv82 -> Liepard shiny
UPDATE data_monster SET name='Luxray', dex=405, shiny=0, he=3, he2=-1 WHERE id=137; -- Cọp xanh lv84 -> Luxray
UPDATE data_monster SET name='Entei', dex=244, shiny=0, he=1, he2=-1 WHERE id=138; -- Cọp đỏ lv86 -> Entei (huyền thoại)
