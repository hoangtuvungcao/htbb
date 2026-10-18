# -*- coding: utf-8 -*-
"""
Du lieu COT TRUYEN cho tools/pokemon/sinh_sql_lau_dai.py (chi la bang, khong co logic).

Tach rieng de nguoi viet kich ban sua chu ma khong dung vao cong thuc kinh te.
Moi chu o day la chu NGUOI CHOI THAY: khong dung tu cam (ChuPokemon.TU_CAM) - test
KiemLauDai quet lai toan bo ten nhiem vu sinh ra.

Quy uoc:
  CUNG_NAM1: 13 cung x 4 chuong = 52 chuong (nam 1, Kanto -> Johto).
  CUNG_NAM2: dan y nam 2 (Hoenn + mo rong), chi ten chuong - CHUA sinh SQL.
  Moi chuong: (ten chuong, dia danh, npc type giao, cap toi thieu).
"""

# NPC tinh (type trong bang_npc.csv) -> ten hien thi Pokemon. Chi dung cac type co san o
# moi thi tran (nhiem vu cu da dung 1,2,3,5,6,9,21,22,23).
NPC = {
    1: "Kecleon",      # tap hoa
    2: "Machamp",      # xuong che tao
    3: "Bronzong",     # ban trang bi
    5: "Xatu",         # truong lang
    6: "Persian",      # phu ong
    8: "Snorlax",      # ong noi
    9: "Kangaskhan",   # kho do
    21: "Blaziken",    # huan luyen vien
    22: "Meowth",      # thuong nhan
    23: "Torkoal",     # lao nhan Cerulean
}

# (ma cung, ten cung, vung, [(ten chuong, dia danh, npc, cap toi thieu) x4])
CUNG_NAM1 = [
    ("K1", "Khởi hành từ Pallet", "Kanto", [
        ("Buổi sáng ở Pallet", "Pallet", 5, 1),
        ("Đồng cỏ phía bắc", "Pallet", 6, 3),
        ("Lối mòn Viridian", "Viridian", 1, 5),
        ("Bóng cây Rừng Viridian", "Rừng Viridian", 21, 8),
    ]),
    ("K2", "Đá xám Pewter", "Kanto", [
        ("Thị trấn đá xám", "Pewter", 5, 10),
        ("Xưởng chế tạo Pewter", "Pewter", 2, 12),
        ("Đường lên Núi Mặt Trăng", "Núi Mặt Trăng", 8, 14),
        ("Mảnh sao băng", "Núi Mặt Trăng", 21, 16),
    ]),
    ("K3", "Dòng nước Cerulean", "Kanto", [
        ("Cây cầu Cerulean", "Cerulean", 23, 18),
        ("Lá thư của Torkoal", "Cerulean", 23, 19),
        ("Bến cảng Vermilion", "Vermilion", 22, 21),
        ("Tiếng sấm bên bờ biển", "Vermilion", 21, 23),
    ]),
    ("K4", "Chuyến tàu S.S. Anne", "Kanto", [
        ("Tấm vé lên tàu", "Vermilion", 22, 25),
        ("Hành lang tàu S.S. Anne", "Tàu S.S. Anne", 6, 26),
        ("Kho hàng dưới boong", "Tàu S.S. Anne", 9, 28),
        ("Cơn bão ngoài khơi", "Tàu S.S. Anne", 21, 29),
    ]),
    ("K5", "Sương mù Lavender", "Kanto", [
        ("Tháp Pokémon", "Lavender", 8, 30),
        ("Tiếng khóc trong sương", "Lavender", 5, 32),
        ("Khu thương mại Celadon", "Celadon", 1, 33),
        ("Vườn hoa Celadon", "Celadon", 6, 35),
    ]),
    ("K6", "Saffron và Vùng Safari", "Kanto", [
        ("Thành phố Saffron", "Saffron", 5, 36),
        ("Toà nhà Silph", "Saffron", 2, 38),
        ("Cổng Vùng Safari", "Vùng Safari", 22, 39),
        ("Bờ biển Fuchsia", "Fuchsia", 21, 41),
    ]),
    ("K7", "Đảo lửa Cinnabar", "Kanto", [
        ("Chuyến đò ra đảo", "Cinnabar", 9, 42),
        ("Phòng thí nghiệm bỏ hoang", "Cinnabar", 3, 44),
        ("Núi lửa thức giấc", "Cinnabar", 21, 46),
        ("Trở về Viridian", "Viridian", 5, 48),
    ]),
    ("J1", "Gió mới New Bark", "Johto", [
        ("Thị trấn New Bark", "New Bark", 5, 49),
        ("Hoa anh đào Cherrygrove", "Cherrygrove", 6, 51),
        ("Tháp Sprout ở Violet", "Violet", 8, 53),
        ("Cánh đồng phía tây", "Violet", 21, 54),
    ]),
    ("J2", "Rừng Ilex và Azalea", "Johto", [
        ("Giếng Slowpoke", "Azalea", 5, 55),
        ("Lạc trong Rừng Ilex", "Rừng Ilex", 23, 57),
        ("Đền thờ trong rừng", "Rừng Ilex", 8, 59),
        ("Phố lớn Goldenrod", "Goldenrod", 22, 61),
    ]),
    ("J3", "Tháp chuông Ecruteak", "Johto", [
        ("Tháp Chuông", "Ecruteak", 5, 62),
        ("Tháp Cháy", "Ecruteak", 8, 64),
        ("Ngọn hải đăng Olivine", "Olivine", 2, 66),
        ("Thuốc từ Cianwood", "Cianwood", 1, 68),
    ]),
    ("J4", "Hồ Phẫn Nộ", "Johto", [
        ("Tin đồn ở Mahogany", "Mahogany", 6, 69),
        ("Mặt hồ đỏ rực", "Hồ Phẫn Nộ", 21, 71),
        ("Hang Tối", "Hang Tối", 9, 73),
        ("Sóng radio lạ", "Mahogany", 3, 75),
    ]),
    ("J5", "Rồng Blackthorn", "Johto", [
        ("Ngôi làng của rồng", "Blackthorn", 5, 76),
        ("Hang Salamence", "Hang Salamence", 21, 78),
        ("Tổ Zapdos", "Tổ Zapdos", 8, 81),
        ("Lời thề ở đền rồng", "Blackthorn", 5, 83),
    ]),
    ("J6", "Đường Victory", "Johto", [
        ("Cổng Đường Victory", "Đường Victory", 21, 84),
        ("Cao nguyên Indigo", "Cao nguyên Indigo", 5, 86),
        ("Đỉnh Núi Silver", "Núi Silver", 8, 88),
        ("Huyền thoại thức tỉnh", "Núi Silver", 21, 90),
    ]),
]

# Nam 2: dan y. Cap > 91 can mo rong LevelDetail + quai moi (giai doan 3, docs/thiet-ke-game.md).
CUNG_NAM2 = [
    ("H1", "Đất liền Hoenn", "Hoenn", ["Bến Littleroot", "Rừng Petalburg", "Đá Rustboro", "Hang Granite"], 91),
    ("H2", "Biển nóng Slateport", "Hoenn", ["Chợ cảng Slateport", "Bảo tàng biển", "Đường cát Route 111", "Thị trấn Mauville"], 92),
    ("H3", "Núi lửa Chimney", "Hoenn", ["Suối nóng Lavaridge", "Miệng núi Chimney", "Sa mạc gió", "Huy hiệu Nhiệt"], 93),
    ("H4", "Bầu trời Fortree", "Hoenn", ["Nhà trên cây Fortree", "Viện Thời tiết", "Cánh rừng mưa", "Huy hiệu Lông Vũ"], 94),
    ("H5", "Biển sâu Lilycove", "Hoenn", ["Cửa hàng Lilycove", "Núi Pyre", "Đảo Mossdeep", "Hang dưới biển"], 95),
    ("H6", "Thức tỉnh Kyogre và Groudon", "Hoenn", ["Thành phố Sootopolis", "Cave of Origin", "Cơn mưa vô tận", "Hạn hán vô tận"], 96),
    ("H7", "Tháp trời", "Hoenn", ["Tháp Sky Pillar", "Rayquaza giáng thế", "Vùng trời yên bình", "Ngày hội Hoenn"], 97),
    ("H8", "Liên minh Hoenn", "Hoenn", ["Đường Victory Hoenn", "Hội đồng Tứ Kỳ 1", "Hội đồng Tứ Kỳ 2", "Nhà vô địch"], 98),
    ("H9", "Vùng chiến đấu", "Hoenn", ["Tháp Battle", "Cung điện Battle", "Mái vòm Battle", "Kim tự tháp Battle"], 99),
    ("H10", "Đảo Navel và Faraway", "Hoenn", ["Đảo Navel", "Đảo Faraway", "Đảo Birth", "Đảo Southern"], 100),
    ("H11", "Kết nối ba vùng", "Liên vùng", ["Cầu nối Kanto", "Cầu nối Johto", "Cầu nối Hoenn", "Đại hội ba vùng"], 100),
    ("H12", "Mùa giải huyền thoại", "Liên vùng", ["Mùa giải 1", "Mùa giải 2", "Mùa giải 3", "Chung kết"], 100),
    ("H13", "Khúc vĩ thanh", "Liên vùng", ["Lời mời mới", "Tấm bản đồ cổ", "Bờ biển lạ", "Chân trời Sinnoh"], 100),
]

# Thi tran/tuyen duong cho chuoi nhiem vu phu: (dia danh, vung, cap goc, npc, chu de).
THI_TRAN = [
    ("Pallet", "Kanto", 2, 5, "đồng cỏ"),
    ("Viridian", "Kanto", 6, 1, "lối mòn"),
    ("Pewter", "Kanto", 11, 2, "đá"),
    ("Cerulean", "Kanto", 18, 23, "dòng nước"),
    ("Vermilion", "Kanto", 22, 22, "bến cảng"),
    ("Lavender", "Kanto", 30, 8, "sương mù"),
    ("Celadon", "Kanto", 33, 1, "vườn hoa"),
    ("Saffron", "Kanto", 37, 5, "thành phố"),
    ("Fuchsia", "Kanto", 40, 6, "bờ biển"),
    ("Cinnabar", "Kanto", 43, 3, "núi lửa"),
    ("New Bark", "Johto", 49, 5, "gió"),
    ("Cherrygrove", "Johto", 51, 6, "hoa"),
    ("Violet", "Johto", 53, 8, "tháp"),
    ("Azalea", "Johto", 56, 5, "giếng"),
    ("Goldenrod", "Johto", 60, 22, "phố"),
    ("Ecruteak", "Johto", 63, 8, "chuông"),
    ("Olivine", "Johto", 66, 2, "hải đăng"),
    ("Cianwood", "Johto", 68, 1, "thuốc"),
    ("Mahogany", "Johto", 70, 6, "hồ"),
    ("Blackthorn", "Johto", 77, 5, "rồng"),
]

TUYEN = [
    ("Rừng Viridian", 8), ("Núi Mặt Trăng", 15), ("Tàu S.S. Anne", 27), ("Vùng Safari", 39),
    ("Hang Tối", 50), ("Rừng Ilex", 58), ("Rừng Cổ", 65), ("Hồ Phẫn Nộ", 71),
    ("Hang Salamence", 79), ("Đường Victory", 85),
]

# Mau ten nhiem vu phu theo buoc trong chuoi (5 buoc / thi tran).
MAU_PHU = [
    "Lời chào {noi}",
    "Rắc rối ở {noi}",
    "Món quà từ {noi}",
    "Bí mật {chu_de} {noi}",
    "Người bảo vệ {noi}",
]

MAU_TUYEN = ["Trinh sát {noi}", "Dọn đường {noi}", "Kho báu {noi}"]

NGAY = [
    "Săn bắt buổi sáng", "Giao hàng cho Kecleon", "Tuần tra đồng cỏ", "Thu thập Berry",
    "Huấn luyện cùng Blaziken", "Dọn tổ Rattata", "Gom gỗ sửa nhà", "Đưa thư liên vùng",
    "Bảo vệ ruộng lúa", "Thăm Trung tâm Pokémon", "Kiểm tra bẫy", "Đánh dấu bản đồ",
    "Thử sức lệch cấp", "Chợ phiên Goldenrod", "Hộ tống Chansey", "Quan sát Pokédex",
    "Tìm trứng thất lạc", "Luyện tập né đòn", "Săn quái tinh anh", "Giúp đỡ người mới",
]

TUAN = [
    "Thử thách tuần: Kanto", "Thử thách tuần: Johto", "Đội tuần tra", "Cúp huấn luyện",
    "Đợt săn huyền thoại", "Nghiên cứu Pokédex tuần", "Đấu trường tuần", "Chuyến hàng lớn",
    "Bảo vệ ba vùng", "Mùa thu hoạch", "Thám hiểm hang động", "Trận đấu đội",
]

THANH_TUU = [
    ("Bước chân đầu tiên", "cap", 10), ("Nhà huấn luyện đồng", "cap", 20),
    ("Nhà huấn luyện bạc", "cap", 40), ("Nhà huấn luyện vàng", "cap", 60),
    ("Nhà huấn luyện bạch kim", "cap", 80), ("Đỉnh cao ba vùng", "cap", 91),
    ("Nhà sưu tầm 10", "pokedex", 10), ("Nhà sưu tầm 25", "pokedex", 25),
    ("Nhà sưu tầm 50", "pokedex", 50), ("Nhà sưu tầm 75", "pokedex", 75),
    ("Nhà sưu tầm 100", "pokedex", 100), ("Kanto toàn tập", "chuong", 28),
    ("Johto toàn tập", "chuong", 52), ("Thợ rèn tập sự", "nang_cap", 5),
    ("Thợ rèn lành nghề", "nang_cap", 10), ("Thợ rèn bậc thầy", "nang_cap", 15),
    ("Người bạn của mọi nhà", "phu", 50), ("Huyền thoại thị trấn", "phu", 100),
    ("Chăm chỉ 30 ngày", "ngay", 30), ("Chăm chỉ 100 ngày", "ngay", 100),
    ("Chăm chỉ 365 ngày", "ngay", 365), ("Đấu trường: 10 trận thắng", "pvp", 10),
    ("Đấu trường: 100 trận thắng", "pvp", 100), ("Săn huyền thoại", "boss", 10),
    ("Thành viên đội", "doi", 1), ("Đội mạnh nhất tuần", "doi", 10),
    ("Đủ bộ trang bị", "bo", 1), ("Bộ trang bị tinh anh", "bo", 3),
    ("Thánh sự kiện", "su_kien", 20), ("Người kể chuyện", "chuong", 13),
]
