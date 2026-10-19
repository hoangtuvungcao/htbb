-- SINH TU tools/pokemon/sinh_icon_pokemon.py - KHONG SUA TAY.
-- Icon PMD o dai anh 14000+ (map/icon_pokemon/). Chay SAU pokemon_vat_pham_eevee.sql.
-- Thu tu: trang bi theo type, vat pham theo id, roi theo icon cu (xstart < 866),
-- cuoi cung tach o anh thuoc dung chung (data_potion.idImage / data_shop.idImg).
-- SO_DONG: 192 (test KiemIconPokemon dem lai)
UPDATE data_item SET xstart=14000 WHERE id IN (1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,267,264,265,266,268,269,270,271,272,273,274,275,276,277,278,279,280,281,282,283,284,285,286,287,288,289,290,291,467,468,469,470,471,472,473,474,507,508,573,574,575,576,579,580,582,583,609,610,620,621,622,623,624,625,626,627,628,629,639,640,641,642,671,672,673,674,723,724,725,726,736,737,740,741,743,744,754,755,791,789,788,757,792,797,798,799,800,801,805,806,833,834,839,756,841,842,843,844,845,846,847,840,888,889,903,904); -- Scarf_Red
UPDATE data_item SET xstart=14001 WHERE id IN (27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,292,293,294,295,296,297,298,299,300,301,302,303,304,305,306,307,308,309,310,311,312,313,314,315,475,476,477,478,479,480,481,482,577,643,644,645,646,745,746,747,748,749,837,838,872); -- Belt_Black
UPDATE data_item SET xstart=14002 WHERE id IN (53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,316,317,318,319,320,321,322,323,324,325,326,327,328,329,330,331,332,333,334,335,336,337,338,339,483,484,485,486,487,488,489,490,578,581,647,648,649,650); -- Band_Brown
UPDATE data_item SET xstart=14003 WHERE id IN (79,80,81,82,83,84,85,174,175,176,177,178,179,180,181,214,215,216,217,218,219,220,221,222,223,400,401,402,403,404,405,406,407,408,409,411,412,410,598,599,715,783,778,773,768,763,758,893); -- Stone_DarkBrown
UPDATE data_item SET xstart=14004 WHERE id IN (86,87,88,89,90,91,92,182,183,184,185,186,187,188,189,224,225,226,227,228,229,230,231,232,233,413,414,415,416,417,418,419,420,421,422,423,424,425,600,601,716,784,779,774,769,764,759,890,891,892,894); -- Seed_Green
UPDATE data_item SET xstart=14005 WHERE id IN (93,94,95,96,97,98,99,190,191,192,193,194,195,196,197,234,235,236,237,238,239,240,241,242,243,426,427,428,429,430,431,432,433,434,435,436,437,438,602,603,717,785,780,775,770,765,760,796,895); -- Silk_White
UPDATE data_item SET xstart=14006 WHERE id IN (100,101,102,103,104,105,106,198,199,200,201,202,203,204,205,244,245,246,247,248,249,250,251,252,253,439,440,441,442,443,444,445,446,447,448,449,450,451,604,605,718,786,781,776,771,766,761,896); -- Bottle_LightBlue
UPDATE data_item SET xstart=14007 WHERE id IN (107,108,109,110,111,112,113,206,207,208,209,210,211,212,213,254,255,256,257,258,259,260,261,262,263,452,453,454,455,456,457,458,459,460,461,462,463,464,606,607,719,787,782,777,772,767,762,897); -- Iron_Ball
UPDATE data_item SET xstart=14008 WHERE id IN (115,116,117,118,119,120,121,122,123,124,125,114,364,365,366,367,368,369,370,371,372,373,374,375,499,500,501,502,651,652,653,654); -- Ring_Target
UPDATE data_item SET xstart=14009 WHERE id IN (126,127,128,129,130,131,132,133,134,135,136,137,376,377,378,379,380,381,382,383,384,385,386,387,503,504,505,506,655,656,657,658); -- Pearl
UPDATE data_item SET xstart=14010 WHERE id IN (138,139,140,141,142,143,144,145,146,147,148,149,340,341,342,343,344,345,346,347,348,349,350,351,491,492,493,494,659,660,661,662); -- Scale_LightBlue
UPDATE data_item SET xstart=14011 WHERE id IN (150,151,152,153,154,155,156,157,158,159,160,161,352,357,356,355,354,353,358,359,360,361,362,363,495,496,497,498,663,664,665,666); -- Claw_Gray
UPDATE data_item SET xstart=14012 WHERE id IN (162,163,164,165,166,167,168,169,170,171,172,173,388,389,390,391,392,393,394,395,396,397,398,399,571,572,569,570,667,668,669,670); -- Slab_Gray
UPDATE data_item SET xstart=14013 WHERE id IN (584,585,586,587,588,589,590,591,592,593,594,595,596,597,608,616,950,742,750,795,951,952,953,954,910); -- Bow_Pink
UPDATE data_item SET xstart=14014 WHERE id =960; -- Stone_Blue
UPDATE data_item SET xstart=14015 WHERE id =961; -- Stone_Yellow
UPDATE data_item SET xstart=14016 WHERE id =962; -- Stone_Red
UPDATE data_item SET xstart=14017 WHERE id =963; -- Stone_Orange
UPDATE data_item SET xstart=14018 WHERE id =964; -- Stone_Black
UPDATE data_item SET xstart=14019 WHERE id =965; -- Stone_Green
UPDATE data_item SET xstart=14020 WHERE id =966; -- Stone_LightBlue
UPDATE data_item SET xstart=14021 WHERE id =967; -- Bow_Fuschia
UPDATE data_item SET xstart=14022 WHERE id =465; -- Stick_Brown
UPDATE data_item SET xstart=14023 WHERE id =466; -- Stick_Yellow
UPDATE data_item SET xstart=14065 WHERE id IN (689,690); -- dex:0163
UPDATE data_item SET xstart=14066 WHERE id IN (691,692); -- dex:0164
UPDATE data_item SET xstart=14067 WHERE id =693; -- dex:0164s
UPDATE data_item SET xstart=14068 WHERE id IN (694,695); -- dex:0147
UPDATE data_item SET xstart=14069 WHERE id IN (696,697); -- dex:0148
UPDATE data_item SET xstart=14070 WHERE id =698; -- dex:0149
UPDATE data_item SET xstart=14071 WHERE id =699; -- dex:0173
UPDATE data_item SET xstart=14072 WHERE id IN (700,701); -- dex:0035
UPDATE data_item SET xstart=14073 WHERE id =702; -- dex:0036
UPDATE data_item SET xstart=14074 WHERE id =703; -- dex:0036s
UPDATE data_item SET xstart=14075 WHERE id IN (704,705); -- dex:0041
UPDATE data_item SET xstart=14076 WHERE id IN (706,707); -- dex:0042
UPDATE data_item SET xstart=14077 WHERE id =708; -- dex:0169
UPDATE data_item SET xstart=14078 WHERE id IN (709,710); -- dex:0627
UPDATE data_item SET xstart=14079 WHERE id IN (711,712); -- dex:0628
UPDATE data_item SET xstart=14080 WHERE id =713; -- dex:0628s
UPDATE data_item SET xstart=14117 WHERE id =735; -- Box_Pink
UPDATE data_item SET xstart=14118 WHERE id =752; -- Chest_Red
UPDATE data_item SET xstart=14119 WHERE id =753; -- Chest_Blue
UPDATE data_item SET xstart=14142 WHERE id =860; -- Cape_Gray+mau:F06030
UPDATE data_item SET xstart=14143 WHERE id =848; -- Cape_Gray+mau:F06030+toi
UPDATE data_item SET xstart=14144 WHERE id =861; -- Belt_White+mau:F06030
UPDATE data_item SET xstart=14145 WHERE id =849; -- Belt_White+mau:F06030+toi
UPDATE data_item SET xstart=14146 WHERE id =862; -- Band_Gray+mau:F06030
UPDATE data_item SET xstart=14147 WHERE id =850; -- Band_Gray+mau:F06030+toi
UPDATE data_item SET xstart=14148 WHERE id =863; -- Crown_Gray+mau:F06030
UPDATE data_item SET xstart=14149 WHERE id =851; -- Crown_Gray+mau:F06030+toi
UPDATE data_item SET xstart=14150 WHERE id =864; -- Scarf_2_White+mau:F06030
UPDATE data_item SET xstart=14151 WHERE id =852; -- Scarf_2_White+mau:F06030+toi
UPDATE data_item SET xstart=14152 WHERE id =865; -- Claw_White+mau:F06030
UPDATE data_item SET xstart=14153 WHERE id =853; -- Claw_White+mau:F06030+toi
UPDATE data_item SET xstart=14154 WHERE id =866; -- Cape_Gray+mau:3C8CFF
UPDATE data_item SET xstart=14155 WHERE id =854; -- Cape_Gray+mau:3C8CFF+toi
UPDATE data_item SET xstart=14156 WHERE id =867; -- Belt_White+mau:3C8CFF
UPDATE data_item SET xstart=14157 WHERE id =855; -- Belt_White+mau:3C8CFF+toi
UPDATE data_item SET xstart=14158 WHERE id =868; -- Band_Gray+mau:3C8CFF
UPDATE data_item SET xstart=14159 WHERE id =856; -- Band_Gray+mau:3C8CFF+toi
UPDATE data_item SET xstart=14160 WHERE id =869; -- Crown_Gray+mau:3C8CFF
UPDATE data_item SET xstart=14161 WHERE id =857; -- Crown_Gray+mau:3C8CFF+toi
UPDATE data_item SET xstart=14162 WHERE id =870; -- Scarf_2_White+mau:3C8CFF
UPDATE data_item SET xstart=14163 WHERE id =858; -- Scarf_2_White+mau:3C8CFF+toi
UPDATE data_item SET xstart=14164 WHERE id =871; -- Claw_White+mau:3C8CFF
UPDATE data_item SET xstart=14165 WHERE id =859; -- Claw_White+mau:3C8CFF+toi
UPDATE data_item SET xstart=14042 WHERE xstart=473; -- Disc_Black+so:1
UPDATE data_item SET xstart=14043 WHERE xstart=474; -- Disc_Blue+so:2
UPDATE data_item SET xstart=14044 WHERE xstart=475; -- Disc_Yellow+so:3
UPDATE data_item SET xstart=14045 WHERE xstart=476; -- Disc_Yellow+hue:-45+so:4
UPDATE data_item SET xstart=14030 WHERE xstart=477; -- Cape_Brown+so:1
UPDATE data_item SET xstart=14031 WHERE xstart=478; -- Cape_Gray+so:2
UPDATE data_item SET xstart=14032 WHERE xstart=479; -- Cape_Orange+so:3
UPDATE data_item SET xstart=14033 WHERE xstart=480; -- Cape_Red+so:4
UPDATE data_item SET xstart=14034 WHERE xstart=481; -- Band_Tan+so:1
UPDATE data_item SET xstart=14035 WHERE xstart=482; -- Band_Gray+so:2
UPDATE data_item SET xstart=14036 WHERE xstart=483; -- Band_Green+so:3
UPDATE data_item SET xstart=14037 WHERE xstart=484; -- Band_Pink+so:4
UPDATE data_item SET xstart=14038 WHERE xstart=485; -- Crown_Gray+so:1
UPDATE data_item SET xstart=14039 WHERE xstart=486; -- Crown_Gray+mau:5AA0FF+so:2
UPDATE data_item SET xstart=14040 WHERE xstart=487; -- Crown_Yellow+so:3
UPDATE data_item SET xstart=14041 WHERE xstart=488; -- Crown_Yellow+hue:-50+so:4
UPDATE data_item SET xstart=14046 WHERE xstart=489; -- Belt_Brown+so:1
UPDATE data_item SET xstart=14047 WHERE xstart=490; -- Belt_LightBlue+so:2
UPDATE data_item SET xstart=14048 WHERE xstart=491; -- Belt_White+so:3
UPDATE data_item SET xstart=14049 WHERE xstart=492; -- Belt_LightBlue+hue:70+so:4
UPDATE data_item SET xstart=14050 WHERE xstart=519; -- Box_Brown
UPDATE data_item SET xstart=14051 WHERE xstart=520; -- Box_Red
UPDATE data_item SET xstart=14024 WHERE xstart=521; -- Wand_Orange+sang
UPDATE data_item SET xstart=14025 WHERE xstart=522; -- Wand_Blue+sang
UPDATE data_item SET xstart=14026 WHERE xstart=523; -- Wand_Purple+sang
UPDATE data_item SET xstart=14027 WHERE xstart=526; -- Wand_Blue
UPDATE data_item SET xstart=14028 WHERE xstart=527; -- Wand_Orange
UPDATE data_item SET xstart=14029 WHERE xstart=528; -- Wand_Purple
UPDATE data_item SET xstart=14052 WHERE xstart=532; -- Tag_Purple+hue:-90+so:4
UPDATE data_item SET xstart=14052 WHERE xstart=533; -- Tag_Purple+hue:-90+so:4
UPDATE data_item SET xstart=14052 WHERE xstart=534; -- Tag_Purple+hue:-90+so:4
UPDATE data_item SET xstart=14053 WHERE xstart=535; -- Tag_Purple+hue:-170+so:5
UPDATE data_item SET xstart=14053 WHERE xstart=536; -- Tag_Purple+hue:-170+so:5
UPDATE data_item SET xstart=14053 WHERE xstart=537; -- Tag_Purple+hue:-170+so:5
UPDATE data_item SET xstart=14054 WHERE xstart=538; -- Tag_Purple+hue:70+so:6
UPDATE data_item SET xstart=14054 WHERE xstart=539; -- Tag_Purple+hue:70+so:6
UPDATE data_item SET xstart=14054 WHERE xstart=540; -- Tag_Purple+hue:70+so:6
UPDATE data_item SET xstart=14055 WHERE xstart=558; -- Tag_White
UPDATE data_item SET xstart=14058 WHERE xstart=559; -- Key_Brown+so:5
UPDATE data_item SET xstart=14059 WHERE xstart=560; -- Key_White+so:6
UPDATE data_item SET xstart=14060 WHERE xstart=561; -- Key_Brown+mau:FFD040+so:7
UPDATE data_item SET xstart=14056 WHERE xstart=562; -- Tag_Purple+hue:30+so:2
UPDATE data_item SET xstart=14057 WHERE xstart=563; -- Tag_Purple+hue:30+so:3
UPDATE data_item SET xstart=14062 WHERE xstart=564; -- dex:0077
UPDATE data_item SET xstart=14063 WHERE xstart=565; -- dex:0078
UPDATE data_item SET xstart=14064 WHERE xstart=566; -- dex:0078s
UPDATE data_item SET xstart=14096 WHERE xstart=572; -- Egg+hue:-150
UPDATE data_item SET xstart=14097 WHERE xstart=573; -- Egg+hue:110
UPDATE data_item SET xstart=14098 WHERE xstart=574; -- Egg+hue:-110
UPDATE data_item SET xstart=14099 WHERE xstart=575; -- Egg+hue:160
UPDATE data_item SET xstart=14100 WHERE xstart=576; -- Egg+hue:-60
UPDATE data_item SET xstart=14108 WHERE xstart=577; -- Gummi_Pink
UPDATE data_item SET xstart=14109 WHERE xstart=578; -- Gummi_Royal
UPDATE data_item SET xstart=14110 WHERE xstart=579; -- Herb_Green
UPDATE data_item SET xstart=14061 WHERE xstart=580; -- Machine_Green
UPDATE data_item SET xstart=14081 WHERE xstart=585; -- dex:0417
UPDATE data_item SET xstart=14082 WHERE xstart=586; -- dex:0225
UPDATE data_item SET xstart=14111 WHERE xstart=589; -- Wand_Red
UPDATE data_item SET xstart=14112 WHERE xstart=590; -- Money_Yellow
UPDATE data_item SET xstart=14113 WHERE xstart=591; -- Money_Gray
UPDATE data_item SET xstart=14114 WHERE xstart=592; -- Money_Gray+mau:50A0FF
UPDATE data_item SET xstart=14115 WHERE xstart=594; -- Money_Gray+mau:FF5050
UPDATE data_item SET xstart=14116 WHERE xstart=595; -- Money_Gray+mau:60D060
UPDATE data_item SET xstart=14120 WHERE xstart=601; -- Chest_Gold
UPDATE data_item SET xstart=14121 WHERE xstart=604; -- Medicine_Yellow
UPDATE data_item SET xstart=14122 WHERE xstart=627; -- Scale_Pink
UPDATE data_item SET xstart=14083 WHERE xstart=698; -- dex:0100s
UPDATE data_item SET xstart=14123 WHERE xstart=705; -- Tag_White+mau:FF4040
UPDATE data_item SET xstart=14124 WHERE xstart=706; -- Tag_White+mau:FFD040
UPDATE data_item SET xstart=14084 WHERE xstart=731; -- dex:0667
UPDATE data_item SET xstart=14085 WHERE xstart=732; -- dex:0058
UPDATE data_item SET xstart=14086 WHERE xstart=733; -- dex:0403s
UPDATE data_item SET xstart=14125 WHERE xstart=734; -- Crystal_Yellow
UPDATE data_item SET xstart=14126 WHERE xstart=735; -- Silk_Pink
UPDATE data_item SET xstart=14127 WHERE xstart=736; -- Ore_White
UPDATE data_item SET xstart=14128 WHERE xstart=737; -- Ore_Blue
UPDATE data_item SET xstart=14129 WHERE xstart=738; -- Stick_Pink
UPDATE data_item SET xstart=14130 WHERE xstart=739; -- Scale_Black
UPDATE data_item SET xstart=14131 WHERE xstart=740; -- Rock_Gray
UPDATE data_item SET xstart=14132 WHERE xstart=741; -- Rock_Blue
UPDATE data_item SET xstart=14133 WHERE xstart=742; -- Rock_Purple
UPDATE data_item SET xstart=14134 WHERE xstart=743; -- Stone_White
UPDATE data_item SET xstart=14135 WHERE xstart=744; -- Stone_Gold
UPDATE data_item SET xstart=14136 WHERE xstart=745; -- Stick_White
UPDATE data_item SET xstart=14137 WHERE xstart=746; -- Orb_Purple
UPDATE data_item SET xstart=14138 WHERE xstart=747; -- Stone_Purple
UPDATE data_item SET xstart=14139 WHERE xstart=748; -- Rock_Red
UPDATE data_item SET xstart=14087 WHERE xstart=750; -- dex:0585
UPDATE data_item SET xstart=14140 WHERE xstart=766; -- Flower_Pink
UPDATE data_item SET xstart=14141 WHERE xstart=767; -- Tag_Purple
UPDATE data_item SET xstart=14089 WHERE xstart=807; -- dex:0245
UPDATE data_item SET xstart=14089 WHERE xstart=808; -- dex:0245
UPDATE data_item SET xstart=14089 WHERE xstart=809; -- dex:0245
UPDATE data_item SET xstart=14089 WHERE xstart=810; -- dex:0245
UPDATE data_item SET xstart=14088 WHERE xstart=816; -- dex:0399
UPDATE data_item SET xstart=14089 WHERE xstart=817; -- dex:0245
UPDATE data_item SET xstart=14089 WHERE xstart=818; -- dex:0245
UPDATE data_item SET xstart=14089 WHERE xstart=819; -- dex:0245
UPDATE data_item SET xstart=14089 WHERE xstart=827; -- dex:0245
UPDATE data_item SET xstart=14089 WHERE xstart=828; -- dex:0245
UPDATE data_item SET xstart=14166 WHERE xstart=834; -- Herb_Blue
UPDATE data_item SET xstart=14167 WHERE xstart=835; -- Herb_White
UPDATE data_item SET xstart=14168 WHERE xstart=836; -- Herb_Red
UPDATE data_item SET xstart=14169 WHERE xstart=837; -- Herb_Yellow
UPDATE data_item SET xstart=14170 WHERE xstart=838; -- Herb_Blue+hue:60
UPDATE data_item SET xstart=14101 WHERE xstart=839; -- Egg+hue:-90
UPDATE data_item SET xstart=14102 WHERE xstart=840; -- Egg+hue:-75+sang
UPDATE data_item SET xstart=14103 WHERE xstart=841; -- Egg+hue:90
UPDATE data_item SET xstart=14104 WHERE xstart=842; -- Egg+xam
UPDATE data_item SET xstart=14105 WHERE xstart=843; -- Egg+hue:140+toi
UPDATE data_item SET xstart=14090 WHERE xstart=860; -- dex:0879
UPDATE data_item SET xstart=14091 WHERE xstart=861; -- dex:0675
UPDATE data_item SET xstart=14092 WHERE xstart=862; -- dex:0716
UPDATE data_item SET xstart=14093 WHERE xstart=863; -- dex:0323
UPDATE data_item SET xstart=14094 WHERE xstart=864; -- dex:0745
UPDATE data_item SET xstart=14095 WHERE xstart=865; -- dex:0483
UPDATE data_potion SET idImage=237 WHERE id=0; -- tach o anh thuoc
UPDATE data_potion SET idImage=236 WHERE id=36; -- tach o anh thuoc
UPDATE data_potion SET idImage=240 WHERE id=62; -- tach o anh thuoc
UPDATE data_potion SET idImage=238 WHERE id=95; -- tach o anh thuoc
UPDATE data_potion SET idImage=238 WHERE id=96; -- tach o anh thuoc
UPDATE data_potion SET idImage=239 WHERE id=127; -- tach o anh thuoc
UPDATE data_shop SET idImg=241 WHERE id=110; -- tach o anh thuoc
UPDATE data_shop SET idImg=242 WHERE id=180; -- tach o anh thuoc
UPDATE data_shop SET idImg=243 WHERE id=181; -- tach o anh thuoc
