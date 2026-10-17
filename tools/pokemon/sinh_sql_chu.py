#!/usr/bin/env python3
"""
G7.C1/C4: doi chu nguoi choi thay trong CSDL tu kiem hiep sang the gioi Pokemon.

Ra (file sinh ra, KHONG sua tay - sua bang ben duoi hoac tu dien trong ChuPokemon.java roi
chay lai):
  sql/pokemon_doi_chu.sql                                 UPDATE chi doi chu (id/so/gia giu nguyen)
  docs/pokemon/bang_chu.csv                               moi o doi: bang,id,cot,cu,moi (+ moi cap
                                                          cua tu dien, bang=ChuPokemon cot=tu_dien)
  ClientUnity/Assets/Scripts/Game/ChuPokemonDuLieu.cs     tu dien cho client (ban sao bangDayDu)

Nguon chu:
  1. Bang rieng o duoi (TEN_THUOC, SHOP, QUEST, BO_CUOI, DAU_ITEM/DAU_SHOP...) - viet tay cho cho
     can hay. Cac lua chon moi can chu du an duyet: docs/pokemon/DUYET_CHU.md.
  2. Tu dien chung ChuPokemon.DOI (+ dia danh TenMapPokemon) cho moi o con lai.
  3. Ten quai trong nhiem vu ("20 con nhím" -> "20 con Sandshrew") lay tu
     sql/pokemon_du_lieu_quai.sql (chu thich "-- Cu -> Moi").
KHONG dong vao o da duoc file SQL Pokemon khac sua (I1 ten trang bi, T1 ten pet, quai...).

Chay (o thu muc HTBB/): python3 -I tools/pokemon/sinh_sql_chu.py
Chay SQL SAU cac file pokemon_* khac (muc 14 cua ke hoach).
"""
import csv
import importlib.util
import os
import re
import sys
import unicodedata

_P = os.path.join(os.path.dirname(os.path.abspath(__file__)), "chu_pokemon.py")
_spec = importlib.util.spec_from_file_location("chu_pokemon", _P)
cp = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(cp)

GOC = cp.GOC
RA_SQL = os.path.join(GOC, "sql", cp.SQL_CHU)
RA_CSV = os.path.join(GOC, "docs", "pokemon", "bang_chu.csv")
RA_CS = os.path.join(GOC, "ClientUnity", "Assets", "Scripts", "Game", "ChuPokemonDuLieu.cs")
F_QUAI = os.path.join(GOC, "sql", "pokemon_du_lieu_quai.sql")

# Do dai toi da cua cot (CREATE TABLE trong gamedb7_clean.sql).
DO_DAI = {
    ("data_quest", "name"): 45, ("data_quest", "content"): 1000, ("data_quest", "scontent"): 500,
    ("data_quest", "rescontent"): 500, ("data_quest", "infogift"): 200, ("data_quest", "infofinish"): 200,
    ("data_potion", "name"): 255, ("data_potion", "name2"): 255, ("data_shop", "name"): 255,
    ("data_shop", "decript"): 255, ("tob_fruit", "name"): 100, ("tob_fruit", "decript"): 200,
    ("data_attribute", "name"): 255,
}

# ==================== THUOC (data_potion) ====================
# id -> ten moi. HP -> Thuoc hoi mau, MP -> Ether, buff -> vat pham kieu Pokemon.
# Dong khong co o day: chi qua tu dien chung.
TEN_THUOC = {
    1: "Thuốc hồi máu",
    2: "Siêu thuốc hồi máu",
    3: "Thuốc hồi máu cực mạnh",
    4: "Ether",
    5: "Ether lớn",
    6: "Ether tối đa",
    9: "Rễ năng lượng",
    10: "Kẹo kinh nghiệm S: cộng 100k exp",
    11: "Kẹo kinh nghiệm M: cộng 500k exp",
    12: "Kẹo kinh nghiệm L: cộng 1t exp",
    19: "Dây thoát hiểm",
    21: "Siêu thuốc hồi máu đặc biệt",
    22: "Thuốc hồi máu tối đa",
    23: "Elixir",
    24: "Elixir tối đa",
    26: "Thuốc hồi phục toàn diện",
    27: "Vé bay Pidgeot",
    30: "Sáo gọi Ponyta",
    31: "Huy hiệu lập đội",
    34: "Sáo gọi Ponyta lửa",
    36: "Sáo gọi Stantler",
    64: "Sáo gọi Tauros",
    65: "Sáo gọi Arcanine",
    66: "Sáo gọi Mightyena",
    67: "Sáo gọi Pidgeot",
    68: "Sáo gọi Rapidash",
    70: "Trứng Articuno",
    71: "Trứng Ponyta",
    72: "Trứng Piloswine",
    74: "Trứng Spectrier",
    83: "Kẹo Poké. Tăng 1% kinh nghiệm cho thú nuôi khi cho ăn.",
    84: "Thuốc X",
    86: "Sáo gọi Moltres",
    93: "Thuốc hồi máu 7k",
    94: "Thuốc hồi máu 15k",
    95: "Ether 7k",
    96: "Ether 15k",
    104: "Poké Ball. Dùng để bắt thú nuôi",
    111: "Đĩa chiêu (TM) cho pet",
    115: "Trứng Suicune",
    118: "Vé Moltres.\nDùng quay số Moltres",
    119: "Thuốc hồi sinh\nHồi sinh bản thân.",
    123: "Thuốc X siêu cấp",
    126: "Thuốc hồi máu chiến trường.\nHồi 10.000 hp",
    127: "Ether chiến trường.\nHồi 10.000 mp",
    128: "Thuốc X chiến trường",
    129: "X Phòng thủ.\nTăng 20% giáp trong 90s",
    130: "X Tấn công.\nTăng 20% sát thương.",
    131: "Thuốc săn trùm.\nTăng 50% sát thương boss.",
    132: "Bom lửa. Gây sát thương theo x10% năng lượng bản thân lên toàn bộ mục tiêu trong bán kính 5m.",
}

# ==================== CUA HANG (data_shop) ====================
# id -> (ten, mo ta) ; None = giu cot do (van qua tu dien).
SHOP = {
    2: ("200 Siêu thuốc hồi máu", None),
    3: ("200 Thuốc hồi máu cực mạnh", None),
    4: ("200 Ether lớn", None),
    5: ("200 Ether tối đa", None),
    8: ("Vé bay Pidgeot", "Cho phép bay tức thời đến 1 vị trí giặc trong vùng của mình"),
    11: ("Sáo gọi Ponyta", None),
    12: ("Huy hiệu lập đội", "Huy hiệu dùng để tạo đội"),
    15: ("Sáo gọi Ponyta lửa", None),
    45: ("Sáo gọi Tauros", "Vật phẩm gọi thú cưỡi Tauros"),
    46: ("Sáo gọi Arcanine", "Vật phẩm gọi thú cưỡi Arcanine"),
    47: ("Sáo gọi Mightyena", "Vật phẩm gọi thú cưỡi Mightyena"),
    48: ("Sáo gọi Pidgeot", "Vật phẩm gọi thú cưỡi Pidgeot"),
    49: ("Sáo gọi Rapidash", "Vật phẩm gọi thú cưỡi Rapidash"),
    65: ("Thuốc X", None),
    74: ("200 Thuốc hồi máu 7000", None),
    75: ("200 Thuốc hồi máu 15000", None),
    76: ("200 Ether 7000", None),
    77: ("200 Ether 15000", None),
    104: ("Thuốc X siêu cấp", None),
    127: ("200 Thuốc hồi máu 15k \n - Hồi phục 15000 hp", None),
    128: ("200 Ether 15k \n - Hồi phục 15000 mp", None),
    146: ("Poké Ball \n - Dùng bắt thú nuôi", None),
    147: ("Poké Ball \n - Dùng bắt thú nuôi", None),
}

# ==================== VAT PHAM (data_item) chua ai doi ====================
TEN_ITEM = {
    714: "Kẹo Poké",
    739: "Thuốc hồi sinh\r\nHồi sinh sau khi chết",
}
# Dau ten (truoc dong mo ta) doi rieng theo id: {id: (dau cu, dau moi)}. Phan con lai van qua tu
# dien chung. Dung cho do thoi trang ten ngan (vd "Freddy") de ro la "Áo ..." chu khong phai
# ten mot Pokemon. Danh sach cho chu du an duyet: docs/pokemon/DUYET_CHU.md.
DAU_ITEM = {
    622: ("Hồng hài nhi nam", "Áo Magby nam"), 623: ("Hồng hài nhi nữ", "Áo Magby nữ"),
    671: ("Freddy", "Áo Banette"), 672: ("Jason", "Áo Dusknoir"), 673: ("Harry potter", "Áo Alakazam"),
    674: ("Chúa tể Voldemort", "Áo Giratina"),
    723: ("Hắc Lang", "Áo Zoroark"), 724: ("Thần nữ", "Áo Gardevoir"), 725: ("Bạch Mi", "Áo Absol"),
    726: ("Thuý Vũ Hồng Sam", "Áo Oricorio"), 736: ("Sơn Tinh", "Áo Groudon"), 737: ("Thuỷ Tinh", "Áo Kyogre"),
    740: ("Tiểu long nữ", "Áo Latias"), 741: ("Dương Quá", "Áo Latios"),
    805: ("Bá Thiên Hổ Nam", "Áo Raikou nam"), 806: ("Bá Thiên Hổ Nữ", "Áo Raikou nữ"),
    833: ("Miêu mắt xanh", "Áo Glameow"), 834: ("Miêu quý tộc", "Áo Persian"),
    # Canh (type 29) - ten Han Viet -> Pokemon co canh cung mau
    898: ("Cánh Lam Băng", "Cánh Articuno"), 899: ("Cánh Bạch Linh", "Cánh Lugia"),
    900: ("Cánh Xích Vũ", "Cánh Moltres"), 901: ("Cánh Hồng Vân", "Cánh Togekiss"),
    902: ("Cánh Tử ảnh", "Cánh Crobat"),
}
DAU_SHOP = {
    51: ("Lạc long quân", "Áo Rayquaza"), 53: ("Thạch sanh", "Áo Lucario"), 54: ("Thầy đồ", "Áo giáo sư"),
    55: ("Tiên nữ", "Áo Lunala"), 56: ("Yếm đào", "Áo Bellossom"),
    139: ("Hồng hài nhi nam", "Áo Magby nam"), 140: ("Hồng hài nhi nữ", "Áo Magby nữ"),
    142: ("Freddy", "Áo Banette"), 143: ("Jason", "Áo Dusknoir"), 144: ("Harry potter", "Áo Alakazam"),
    145: ("Chúa tể voldermort", "Áo Giratina"),
    166: ("Sơn tinh", "Áo Groudon"), 167: ("Sơn tinh", "Áo Groudon"),
    168: ("Thuỷ tinh", "Áo Kyogre"), 169: ("Thuỷ tinh", "Áo Kyogre"),  # "thuỷ tinh" thuong = nguyen lieu thuy tinh
    170: ("Tiểu Long Nữ", "Áo Latias"), 171: ("Dương Quá", "Áo Latios"),
    174: ("Tinh Quân", "Áo Solgaleo"), 175: ("Tiên Nữ", "Áo Lunala"),
}


def thay_dau(s, cap):
    """Doi phan dau ten theo bang DAU_*; khong khop thi tra nguyen (bao loi o main)."""
    if not cap or not s or not s.startswith(cap[0]):
        return s
    return cap[1] + s[len(cap[0]):]


# Bo do thu cuoi (type 14..18, bang 6.4 "Yen, giap cho Pokemon cuoi"): ten bo kiem hiep ->
# bac theo ten Poke Ball (cung thu tu cap).
BO_CUOI = {
    "vủ dũng": "Poké", "vũ dũng": "Poké", "băng tâm": "Great", "linh động": "Ultra",
    "hóa thần": "Quick", "phá không": "Dusk", "quy hư": "Timer", "diệt tuyệt": "Luxury",
    "không lô": "Heavy", "khổng lô": "Heavy", "đại lực": "Level", "ngũ vân": "Moon",
    "hùng bá": "Dream", "vô song": "Master",
}
LOAI_CUOI = {16: ("Nón", "Mũ cưỡi"), 14: ("Giáp", "Giáp cưỡi"), 18: ("Yên cương", "Yên cương"),
             17: ("Bàn đạp", "Bàn đạp"), 15: ("Hộ uyển", "Vòng chân")}

# ==================== NHIEM VU (data_quest) ====================
# Nhiem vu gio dau (tan thu) viet lai han. {id: {cot: chu}}. Dinh dang hoi thoai giu nhu cu:
# ">" tach cau, "1" = NPC noi, "0" = nguoi choi noi, cau cuoi khong so = cau hoi nhan.
QUEST = {
    1: {"content": "1Chào cháu, ta là Trưởng làng Xatu. Ta sẽ cho cháu ít tiền để bắt đầu cuộc phiêu lưu>0Chào Trưởng làng>1Cháu là Pokémon mới đến từ Rừng Viridian phải không>0Dạ phải ạ>1Như đã hứa, ta sẽ giới thiệu cháu với các nhà huấn luyện của làng Pewter>1Trong thế giới Pokémon, sức mạnh luôn đi cùng lòng tốt: hãy giúp kẻ yếu và sống vì lẽ phải>1Cháu hãy đi chào hỏi mọi người trong làng>1và giúp họ vài việc nhỏ để làm quen>1Đầu tiên, hãy gặp Machamp thợ rèn.>1Cậu ấy là chuyên gia vật phẩm của làng này đó.>0Dạ, cháu đi ngay>1Tốt lắm"},
    2: {"content": "1Khà khà!>1Ta đã nghe Trưởng làng kể về cháu>1Còn nhỏ mà đã biết sống vì lẽ phải>1Thật đáng quý!>1Ta là Machamp, thợ rèn của làng>1Niềm đam mê của ta là chế tạo và mua bán vật phẩm cho Pokémon>1Có gì thắc mắc về vật phẩm cứ đến tìm ta>1Giờ hãy đi gặp Bronzong thợ giáp nào."},
    3: {"content": "1Tuổi nhỏ chí lớn, tuổi nhỏ chí lớn!>1Ta là Bronzong, thợ làm đồ phòng thủ duy nhất của làng>1Khăn quàng, đai, băng đô... loại nào ta cũng rành>1Cần gì về đồ phòng thủ cứ đến tìm ta nhé!>1Cũng xế trưa rồi.>1Giờ này chắc Kecleon tạp hoá đang chờ cháu đấy>1Hãy qua chào hỏi đi."},
    4: {"content": "1Phù! Ta đợi cháu từ sáng sớm đến giờ>1Nghe chuyện của cháu mà ta thương quá>1Cửa hàng ta bán đủ thứ cho cả làng, từ Thuốc hồi máu đến Ether.>1Kangaskhan thủ kho đang háo hức được gặp cháu>1Ăn xong thì ghé chỗ cô ấy nhé."},
    5: {"content": "1Ha ha! Cháu đây rồi>1Lạ thật, chưa gặp lần nào mà ta thấy như quen cháu từ lâu>1Thật là vui>1Ta vốn ít nói nên cháu đừng ngại>1Sau này trên đường phiêu lưu>1nếu có món đồ quý mà không tiện mang theo>1cứ gửi ta giữ trong túi của ta>1Bảo đảm không ai lấy được.>1Giờ hãy đến gặp Persian>1cứ tìm ngôi nhà to và đẹp nhất làng là thấy."},
    6: {"content": "1Rất vui được gặp cháu>1Cháu tuy một mình nhưng ngoan và có chí lớn>1Thật tốt khi cháu trở thành nhà huấn luyện của làng>1Nếu cần nguyên liệu nâng cấp vật phẩm thì cứ đến tìm ta>1Ta đã nhắn Meowth thương nhân, bạn ta, về cháu rồi>1Hãy đến chào cậu ấy nhé."},
    7: {"content": "1Chào bạn trẻ!>1Ta hay đi buôn bán khắp các vùng>1Gặp được nhau thật là có duyên>1Meo meo ha ha>1Muốn phiêu lưu khắp thế giới Pokémon>1thì cần có chút bản lĩnh.>1Hãy đến gặp Blaziken để học hỏi nhé.>1Blaziken đang chờ cháu ở doanh trại>1Mau đến đó đi nào."},
    8: {"content": "1Hây dà! Cuối cùng cũng đến!>1Cháu thật sự muốn trở thành nhà huấn luyện của làng Pewter chứ?>1Nơi đây không có chỗ cho kẻ lười biếng đâu nhé>1Hãy chăm chỉ luyện tập>1ta thấy cháu có tiềm năng đấy>1Ta luôn có những nhiệm vụ cần người giải quyết>1khi nào đủ tự tin thì đến tìm ta.>1Nhân đây ta dạy cháu 1 chiêu>1Nó sẽ giúp cháu khi bắt đầu chuyến phiêu lưu.>1Giờ hãy quay lại gặp Trưởng làng>1Ông ấy có vài điều muốn dặn trước khi cháu lên đường."},
    9: {"content": "1Chúc mừng cháu>1Từ giờ cháu đã là nhà huấn luyện của làng Pewter>1Đừng phụ lòng mọi người nhé>0Dạ vâng. Cháu sẽ cố gắng."},
    10: {"name": "Tiệc chào mừng",
         "content": "1Cháu biết không, Pewter là ngôi làng có lịch sử lâu đời.>1Đây không chỉ là nơi ta sinh sống>1mà còn là nơi lưu giữ ý chí của những nhà huấn luyện đầu tiên khám phá thế giới Pokémon>1Nơi đây cũng là quê hương thứ hai của bao nhà vô địch khắp các vùng>1ai cũng tự hào là thành viên của làng>1Họ là tấm gương cho các Pokémon đi sau>1Cháu hãy cố gắng nối tiếp truyền thống của làng nhé>1Để ta làm bữa tiệc chào mừng cháu>1cháu hãy hạ 20 con Sandshrew ở Viridian giúp ta nhé>Cháu có muốn giúp ta việc này không ?",
         "scontent": "Sắp đến giờ tiệc rồi, nhanh lên cháu nhé.",
         "rescontent": "1Tốt lắm, cảm ơn cháu!"},
    12: {"name": "Thu nọc Ekans",
         "content": "1À, Ekans ở Viridian dạo này sinh sôi quá nhiều>1Chúng hay trườn vào ruộng làm mọi người sợ hãi>1Vùng Viridian cách làng ta chừng 5 dặm có rất nhiều Ekans>1Cháu hãy đến đó và mang về giúp ta 60 túi nọc Ekans để làm thuốc giải độc>Cháu có muốn giúp ta không ?",
         "scontent": "Cẩn thận kẻo bị Ekans cắn đấy!",
         "rescontent": "1Cảm ơn cháu!>1Cháu thật tốt bụng.",
         "infogift": "Nhiệm vụ:|thu 60 túi nọc Ekans|Địa điểm:|Viridian|NPC nv:|Persian|Phần thưởng:|300 xu và 300 exp"},
    16: {"name": "Làm bánh Poké",
         "content": "1Cả làng rất thích bánh Poké do ta làm nên hôm nay ta muốn đãi mọi người một bữa>1Sáng nay ta đã mua hết Berry ngoài chợ nhưng vẫn chưa đủ>1Đám Tepig ở Celadon hay giấu Berry trong hang>1cháu hãy hạ chúng và mang về đây cho ta 50 túi Berry nhé>Cháu có sẵn sàng giúp ta làm bánh không ?",
         "scontent": "Mọi người đang đợi bánh Poké của ta đấy, đi sớm về sớm.",
         "infogift": "Nhiệm vụ:|lấy 50 túi Berry|Địa điểm:|Celadon|NPC nv:|Kecleon|Phần thưởng:|800 xu và 150000 exp"},
    # Nhiem vu 11, 37-46: ban cu bat "lot da", "nau cao", "ngam ruou", "an thit" chinh Pokemon -
    # trai the gioi Pokemon. Viet lai thanh danh bai / thu vat pham roi ra; so luong giu nguyen.
    11: {"name": "Đuổi Caterpie phá lúa"},
    37: {"name": "Răng Croconaw",
         "content": "1Ta đang làm một chiếc vòng cổ may mắn từ những chiếc răng mà Croconaw thay ra>1nhưng hiện tại ta đang bận việc.>1Cậu hãy đánh bại 100 con Croconaw và nhặt giúp ta những chiếc răng chúng rơi lại nhé.>Cậu có đồng ý không?"},
    38: {"name": "Nọc Croagunk",
         "content": "1Croagunk có túi nọc độc rất quý, dùng để điều chế thuốc giải độc.>1Ta cần nọc của 100 con Croagunk.>Cậu có thể giúp ta không?"},
    39: {"name": "Đua sức với Dodrio",
         "content": "1Nghe nói Dodrio ở vùng đảo New Bark chạy nhanh như gió,>1rất hợp để các Pokémon trẻ luyện sức bền.>1Hãy đánh bại 100 con Dodrio để rèn luyện nhé.>Cậu có làm được không?"},
    40: {"content": "1Kricketune ở Rừng Viridian kêu rất hay,>1sắp tới có tiệc chiêu đãi các nhà huấn luyện của các đội.>1Ta muốn mời chúng đến biểu diễn, nhưng trước hết cậu phải thắng 100 con Kricketune đã.>Cậu có thể giúp ta không?"},
    44: {"content": "1Whirlipede ở Núi Mặt Trăng tiết ra một loại chất dịch>1kết hợp với thảo dược sẽ thành thuốc chữa thương rất hiệu quả.>1Cậu hãy giúp ta đánh bại 100 con Whirlipede để thu chất dịch đó nhé.>Liệu cậu có giúp ta không?"},
    45: {"content": "1Những chiếc lông vũ Farfetch'd thay ra rất bền và đẹp,>1dùng để trang trí đồ phòng thủ thì không gì bằng.>1Cậu hãy giúp ta mang về 100 chiếc lông vũ Farfetch'd nhé.>1Cẩn thận, Farfetch'd ở Núi Mặt Trăng rất hung dữ.>Cậu có muốn làm việc này không?"},
    46: {"name": "Hơi ấm của Litleo",
         "content": "1Khi trời trở lạnh ta thường đau nhức khắp người,>1thật sự ta đã già rồi.>1Nghe nói hơi ấm từ bờm lửa của Litleo giúp giảm đau rất tốt.>1Cậu hãy thắng 100 con Litleo để chúng chịu đến sưởi ấm cho ta nhé.>Cậu có muốn giúp ta không?"},
}

# Ten quai them (chu trong nhiem vu khong khop dung ten data_monster).
QUAI_THEM = {
    "chuột": "Rattata", "heo": "Tepig", "bò cạp": "Skorupi", "rết": "Venipede", "cá sấu": "Croconaw",
    "cá sâu": "Croconaw", "ếch quỷ": "Croagunk", "gà khổng lồ": "Dodrio", "dế khổng lồ": "Kricketune",
    "trâu núi": "Tauros", "sơn tặc": "Scraggy", "hải tặc": "Crawdaunt", "rết đỏ": "Whirlipede",
    "hồng kê": "Farfetch'd", "hổ đỏ": "Litleo", "nhím": "Sandshrew", "sâu": "Caterpie",
    "rắn lục": "Ekans", "quỷ sinh hoa": "Gloom",
}


def ten_quai():
    """{ten cu viet thuong: ten Pokemon} tu chu thich cua pokemon_du_lieu_quai.sql."""
    kq = {}
    for m in re.finditer(r"SET name='((?:[^'\\]|\\.)*)'.*?-- (.+?) -> ", open(F_QUAI, encoding="utf8").read()):
        cu = re.sub(r"\s*\(.*?\)\s*", "", m.group(2)).strip().lower()
        if cu and cu not in kq:
            kq[cu] = m.group(1)
    kq.update(QUAI_THEM)
    return kq


def doi_quai(s, quai):
    """'20 con nhím' -> '20 con Sandshrew'; ten nhieu tu (Dế khổng lồ) doi ca khi khong co 'con'."""
    if not s:
        return s
    ten = sorted(quai, key=len, reverse=True)
    mau = re.compile(r"(?<!\w)((?:con|tên)\s+)?(%s)(?!\w)" % "|".join(re.escape(t) for t in ten), re.I)

    def thay(m):
        tien, t = m.group(1), m.group(2)
        if not tien and " " not in t:
            return m.group(0)
        return (tien or "") + quai[t.lower()]

    return mau.sub(thay, s)


def slug(s):
    """name2 cua data_potion: viet thuong, bo dau, chi chu va so (giong du lieu cu)."""
    s = s.replace("đ", "d").replace("Đ", "D")
    s = unicodedata.normalize("NFD", s)
    s = "".join(c for c in s if unicodedata.category(c) != "Mn")
    return re.sub(r"[^a-z]", "", s.lower())


def doi_bo_cuoi(ten, typ):
    if typ not in LOAI_CUOI:
        return None
    cu, moi = LOAI_CUOI[typ]
    if not ten.lower().startswith(cu.lower() + " "):
        return None
    bo = ten[len(cu) + 1:].strip().lower()
    return "%s %s" % (moi, BO_CUOI[bo]) if bo in BO_CUOI else None


def thoat_cs(s):
    return s.replace("\\", "\\\\").replace('"', '\\"').replace("\n", "\\n").replace("\r", "\\r")


def ghi_cs(b):
    dong = cp.bang_day_du(b)
    with open(RA_CS, "w", encoding="utf8", newline="\n") as f:
        f.write("// SINH TU tools/pokemon/sinh_sql_chu.py - KHONG SUA TAY.\n")
        f.write("// Ban sao cua com.htbb.pokemon.ChuPokemon.bangDayDu() (server). test/KiemChuPokemon doi chieu.\n")
        f.write("// Khoa bat dau bang \"=\" la so khop phan biet hoa thuong.\n")
        f.write("public static class ChuPokemonDuLieu\n{\n")
        f.write("\tpublic static readonly string[][] BANG = new string[][]\n\t{\n")
        for k, v in dong:
            f.write('\t\tnew string[] { "%s", "%s" },\n' % (thoat_cs(k), thoat_cs(v)))
        f.write("\t};\n}\n")
    return len(dong)


def main():
    b = cp.doc_bang()
    td = cp.tu_dien_doi(b)
    cam = cp.tu_dien_cam(b)
    quai = ten_quai()
    db, da_sua = cp.nap_csdl(cp.SQL_POKEMON)
    if len(db["data_potion"]) < 150 or len(db["data_quest"]) < 40 or len(db["data_item"]) < 900:
        print("LOI: doc dump thieu dong (potion %d, quest %d, item %d) - dinh dang SQL da doi?"
              % (len(db["data_potion"]), len(db["data_quest"]), len(db["data_item"])))
        return 1
    loi = []
    doi = []  # (bang, id, cot, cu, moi)

    def kiem_dau(bang, id_, s, bang_dau):
        cap = bang_dau.get(id_)
        if cap and not (s or "").startswith(cap[0]):
            loi.append("%s id=%d: ten khong bat dau bang '%s' (DAU_* cu?)" % (bang, id_, cap[0]))
        return thay_dau(s, cap)

    def dat(bang, id_, cot, moi):
        cu = db[bang][id_][cot]
        if (bang, id_, cot) in da_sua:
            return  # o cua viec khac (I1/T1/quai...) - khong dong vao
        if moi is None or moi == cu:
            return
        gh = DO_DAI.get((bang, cot))
        if gh and len(moi) > gh:
            loi.append("%s.%s id=%d dai %d > %d" % (bang, cot, id_, len(moi), gh))
        doi.append((bang, id_, cot, cu, moi))

    for i, r in sorted(db["data_potion"].items()):
        moi = TEN_THUOC.get(i, td.doi(r["name"]))
        dat("data_potion", i, "name", moi)
        if moi != r["name"]:
            dat("data_potion", i, "name2", slug(moi))
    for i, r in sorted(db["data_shop"].items()):
        ten, mo = SHOP.get(i, (None, None))
        dat("data_shop", i, "name", ten if ten is not None else td.doi(kiem_dau("data_shop", i, r["name"], DAU_SHOP)))
        dat("data_shop", i, "decript", mo if mo is not None else td.doi(r["decript"]))
    for i, r in sorted(db["data_quest"].items()):
        rieng = QUEST.get(i, {})
        for c in cp.COT_CHU["data_quest"]:
            if r.get(c) in (None, "32000", "NULL"):
                continue
            moi = rieng.get(c)
            if moi is None:
                moi = td.doi(doi_quai(r[c], quai))
            dat("data_quest", i, c, moi)
    for i, r in sorted(db["data_item"].items()):
        moi = (TEN_ITEM.get(i) or doi_bo_cuoi(r["name"], int(r["type"]))
               or td.doi(kiem_dau("data_item", i, r["name"], DAU_ITEM)))
        dat("data_item", i, "name", moi)
    for i, r in sorted(db["data_attribute"].items()):
        dat("data_attribute", i, "name", td.doi(r["name"]))
    for i, r in sorted(db["tob_fruit"].items()):
        dat("tob_fruit", i, "name", td.doi(r["name"]))
        dat("tob_fruit", i, "decript", td.doi(r["decript"]))

    # Sau khi doi: khong con tu cam trong o minh da ghi; khong chu HOA co dau U+1EA0..U+1EF8
    # (font client khong ve duoc - test nhom "Chu HOA co dau").
    for bang, id_, cot, cu, moi in doi:
        if cot != "name2" and cam.tim(moi):
            loi.append("%s.%s id=%d van con tu cam %s: %s" % (bang, cot, id_, cam.tim(moi), moi[:80]))
        hoa = [c for c in moi if 0x1EA0 <= ord(c) <= 0x1EF8 and c.isupper()]
        if hoa:
            loi.append("%s.%s id=%d co chu HOA co dau %s: %s" % (bang, cot, id_, "".join(hoa), moi[:80]))

    with open(RA_SQL, "w", encoding="utf8", newline="\n") as f:
        f.write("-- SINH TU tools/pokemon/sinh_sql_chu.py - KHONG SUA TAY.\n")
        f.write("-- G7.C: chi doi CHU (ten/mo ta/hoi thoai). id, so, gia giu nguyen. Chay SAU cac file\n")
        f.write("-- pokemon_* khac (muc 14 ke hoach). Doi chieu tung o: docs/pokemon/bang_chu.csv\n")
        f.write("SET NAMES utf8;\n")
        for bang, id_, cot, cu, moi in doi:
            f.write("UPDATE %s SET %s='%s' WHERE id=%d;\n" % (bang, cot, cp.thoat_sql(moi), id_))
    with open(RA_CSV, "w", encoding="utf8", newline="") as f:
        w = csv.writer(f, lineterminator="\n")
        w.writerow(["bang", "id", "cot", "cu", "moi"])
        for bang, id_, cot, cu, moi in doi:
            w.writerow([bang, id_, cot, cu.replace("\r", "").replace("\n", "\\n"),
                        moi.replace("\r", "").replace("\n", "\\n")])
        # Tu dien chung (ap luc chay cho chu server gui + moi chu client ve): ghi luon de mot
        # bang nay du moi cap doi. bang=ChuPokemon, id = so thu tu trong bangDayDu().
        for n, (k, v) in enumerate(cp.bang_day_du(b)):
            if k != v:
                w.writerow(["ChuPokemon", n, "tu_dien", k, v])
    n_cs = ghi_cs(b)
    dem = {}
    for d in doi:
        dem[d[0]] = dem.get(d[0], 0) + 1
    print("Da ghi %s (%d o: %s), %s, %s (%d cap)" % (
        os.path.relpath(RA_SQL, GOC), len(doi), ", ".join("%s %d" % kv for kv in sorted(dem.items())),
        os.path.relpath(RA_CSV, GOC), os.path.relpath(RA_CS, GOC), n_cs))
    for l in loi:
        print("LOI:", l)
    return 1 if loi else 0


if __name__ == "__main__":
    sys.exit(main())
