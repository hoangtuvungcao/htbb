"""DANH MỤC ĐỒ MỚI + TRANG PHỤC (nguồn sự thật duy nhất; sinh.py đọc file này ra SQL / CSV / PNG / JSON).

Luật cân bằng (thiết kế cân bằng 4.3-4.5 + quyết định chủ game 22/10/2026):
  - Đồ họ mới là BẢN SAO CHỈ SỐ của món gốc cùng ô + cùng bậc (data_item gốc trong noi_dung_trang_bi.csv): không thêm
    sức mạnh dọc. Khác ở tên, icon, và thưởng BỘ HỌ nằm TRONG trần chung (ST hệ <= 20%, kháng hệ <= 60%, thưởng bộ HP /
    tấn công chung trần 5%, chí mạng bộ <= 3%).
  - Không nguồn vào mới: đồ họ THAY món gốc khi rơi (quái / boss / chế tạo đã có), tỉ lệ ra 1 món giữ nguyên.
  - Mọi món trang trí có chỉ số nhỏ theo Ô (không theo món): trang phục 1,5%, danh hiệu 1%, hào quang 1%, skin thú cưỡi
    0,5%, thẻ bạn đồng hành 1% = tổng 5% (trang_tri.tran_tong_phan_nghin = 50). Lượng chỉ mua HÌNH, ô nào cũng có món
    miễn phí sớm (cấp 5 / 10 / 15 / 20).
"""

# ================================================================ BẬC / Ô
TEN_BAC = ["sơ cấp", "đồng", "bạc", "vàng", "bạch kim", "kim cương", "chuyên gia", "tinh anh", "huyền thoại",
           "thần thoại", "tối thượng"]
CAP_BAC = [1, 10, 20, 30, 40, 50, 60, 70, 80, 90, 98]

# Ô của BỘ HỆ (6 ô "vải / phụ kiện"): (khoá ô trong noi_dung_trang_bi.csv, type data_item, danh từ)
O_BO_HE = [
    ("khan_quang", 0, "Khăn quàng"),
    ("dai", 1, "Đai"),
    ("bang_do", 2, "Băng đô"),
    ("vong_co", 9, "Vòng cổ"),
    ("long_vu", 10, "Lông vũ"),
    ("ruy_bang", 19, "Ruy băng"),
]
BAC_BO_HE = [2, 4, 6, 8, 10]          # bộ hệ có ở bậc bạc, bạch kim, chuyên gia, huyền thoại, tối thượng

# Ô của BỘ VÙNG: huy hiệu, găng, phiến đá + đồ cầm tay 5 dòng (type 3..7)
O_BO_VUNG = [
    ("huy_hieu", 8, "Huy hiệu"),
    ("gang_dam", 11, "Găng"),
    ("phien_da", 12, "Phiến đá"),
]
DONG_TAY = [  # (type, dòng, danh từ, màu nhấn)
    (3, "Charmander", "Than lửa", 0xF08030),
    (4, "Bulbasaur", "Hạt giống thần", 0x78C850),
    (5, "Eevee", "Khăn lụa", 0xF4F0E8),
    (6, "Squirtle", "Nước thần", 0x6890F0),
    (7, "Pichu", "Nam châm", 0xD83838),
]

# ================================================================ 18 BỘ HỆ
# (hệ, mã, tên bộ, 5 Pokémon theo bậc 2/4/6/8/10, hệ khắc chính (đánh hệ này siêu hiệu quả) -> thưởng kháng)
TEN_HE = ["Thường", "Lửa", "Nước", "Điện", "Cỏ", "Băng", "Giác đấu", "Độc", "Đất", "Bay", "Siêu linh", "Bọ", "Đá", "Ma",
          "Rồng", "Bóng tối", "Thép", "Tiên"]
BO_HE = [
    (0, "binh_nguyen", "Bộ Bình Nguyên", ["Meowth", "Eevee", "Kangaskhan", "Snorlax", "Regigigas"], 6),
    (1, "hoa_diem", "Bộ Hoả Diệm", ["Vulpix", "Ponyta", "Arcanine", "Moltres", "Ho-Oh"], 2),
    (2, "thuy_trieu", "Bộ Thuỷ Triều", ["Staryu", "Vaporeon", "Lapras", "Suicune", "Kyogre"], 3),
    (3, "loi_quang", "Bộ Lôi Quang", ["Pikachu", "Electabuzz", "Jolteon", "Zapdos", "Raikou"], 8),
    (4, "diep_luc", "Bộ Diệp Lục", ["Oddish", "Tangela", "Venusaur", "Celebi", "Shaymin"], 1),
    (5, "bang_tinh", "Bộ Băng Tinh", ["Snorunt", "Jynx", "Glaceon", "Articuno", "Regice"], 1),
    (6, "vo_dao", "Bộ Võ Đạo", ["Machop", "Hitmonlee", "Machamp", "Lucario", "Terrakion"], 10),
    (7, "doc_duoc", "Bộ Độc Dược", ["Ekans", "Grimer", "Arbok", "Nidoking", "Crobat"], 8),
    (8, "dia_chan", "Bộ Địa Chấn", ["Sandshrew", "Cubone", "Rhydon", "Donphan", "Groudon"], 2),
    (9, "phong_vu", "Bộ Phong Vũ", ["Pidgey", "Swablu", "Pidgeot", "Togekiss", "Lugia"], 3),
    (10, "tam_linh", "Bộ Tâm Linh", ["Abra", "Natu", "Espeon", "Alakazam", "Mewtwo"], 15),
    (11, "giap_trung", "Bộ Giáp Trùng", ["Caterpie", "Butterfree", "Pinsir", "Scyther", "Heracross"], 1),
    (12, "nham_thach", "Bộ Nham Thạch", ["Geodude", "Onix", "Golem", "Aerodactyl", "Regirock"], 2),
    (13, "u_linh", "Bộ U Linh", ["Gastly", "Misdreavus", "Haunter", "Gengar", "Giratina"], 15),
    (14, "long_vuong", "Bộ Long Vương", ["Dratini", "Bagon", "Dragonite", "Salamence", "Rayquaza"], 5),
    (15, "hac_am", "Bộ Hắc Ám", ["Murkrow", "Umbreon", "Houndoom", "Tyranitar", "Darkrai"], 6),
    (16, "cuong_thiet", "Bộ Cương Thiết", ["Magnemite", "Skarmory", "Steelix", "Metagross", "Dialga"], 1),
    (17, "tien_tu", "Bộ Tiên Tử", ["Cleffa", "Clefairy", "Togetic", "Gardevoir", "Xerneas"], 16),
]
# Thưởng bộ hệ (phần nghìn): 2 món ST hệ +2%, 4 món kháng hệ khắc +3%, 6 món ST hệ thêm +3% (tổng 5%).
THUONG_BO_HE = [(2, "st_he", 20), (4, "khang_he_khac", 30), (6, "st_he", 30)]

# ================================================================ 3 BỘ VÙNG (huy chương Gym)
VUNG = [  # (mã, tên bộ, bậc từ..đến)
    ("kanto", "Bộ Huy chương Kanto", 0, 3),
    ("johto", "Bộ Huy chương Johto", 4, 7),
    ("hoenn", "Bộ Huy chương Hoenn", 8, 10),
]
HUY_CHUONG = ["Đá Tảng", "Thác Nước", "Sấm Sét", "Cầu Vồng", "Gió Lành", "Tổ Ong", "Sương Mù", "Bão Tố", "Dung Nham",
              "Cánh Chim", "Mưa Rào"]
TRUONG_GYM = ["Brock", "Misty", "Surge", "Erika", "Falkner", "Bugsy", "Morty", "Chuck", "Flannery", "Winona", "Wallace"]
DIA_DANH = ["Pewter", "Cerulean", "Vermilion", "Celadon", "Violet", "Azalea", "Ecruteak", "Cianwood", "Lavaridge",
            "Fortree", "Sootopolis"]
# Thưởng bộ vùng (phần nghìn) - cộng CHUNG trần bộ 5% (HP / tấn công) và trần chí mạng bộ 3% với bộ bậc.
THUONG_BO_VUNG = [(2, "hp", 10), (3, "tan_cong", 10), (4, "chi_mang", 10)]

# ================================================================ RƠI / NGUỒN (thay món gốc, không thêm món)
TI_LE_HE_QUAI = 40      # % món ô bộ hệ rơi từ quái (đúng bậc có bộ) đổi thành món bộ hệ theo hệ của quái
TI_LE_VUNG_QUAI = 35    # % món ô bộ vùng rơi từ quái đổi thành món bộ vùng của bậc
TI_LE_HE_BOSS = 60      # boss: % đổi sang bộ hệ theo hệ boss (ô bộ hệ) / bộ vùng (ô bộ vùng)

# ================================================================ TRANG TRÍ: ngân sách theo ô
# (mã ô, tên, phần nghìn, các dòng chỉ số chọn được (mã, tên))
O_TRANG_TRI = [
    ("trang_phuc", "Trang phục", 15, [("hp", "HP"), ("tan_cong", "Tấn công"), ("phong_thu", "Phòng thủ"),
                                      ("thu_dac_biet", "Phòng thủ đặc biệt")]),
    ("hao_quang", "Hào quang", 10, [("hp", "HP"), ("tan_cong", "Tấn công")]),
    ("skin_thu", "Skin thú cưỡi", 5, [("hp", "HP"), ("phong_thu", "Phòng thủ")]),
    ("the_dong_hanh", "Thẻ bạn đồng hành", 10, [("hp", "HP"), ("tan_cong", "Tấn công"), ("phong_thu", "Phòng thủ")]),
]
NGAN_SACH_DANH_HIEU = 10
TRAN_TONG_TRANG_TRI = 50

# Nguyên liệu chế tạo trang trí (data_item 1034..1037 - nguyên liệu phụ có sẵn) + xu theo giờ thu nhập (hố hút)
BUI_SAO, MANH_SAO, MANH_THIEN_THACH, VAY_RONG = 1034, 1035, 1036, 1037

# ================================================================ TRANG PHỤC (bộ mảnh vẽ trên thân Pokémon)
# (id, mã, tên, [id mảnh hinh_trang_phuc.MANH], độ hiếm hình 0..4, nguồn, tham số, giá lượng, mô tả)
# nguồn: mien_phi (cap:N) | su_kien (lich:<mã LichNam>,..;quai:N) | thanh_tuu (danh_hieu:<mã>) | cua_hang | che_tao
#        (nl:<id>xN,..;gio:N = xu bằng N giờ thu nhập ở cấp người chơi)
TRANG_PHUC = [
    (1, "tp_tan_thu", "Áo huấn luyện viên tân thủ", [1, 30], 0, "mien_phi", "cap:5", 0,
     "Mũ lưỡi trai đỏ và khăn trắng của mọi huấn luyện viên mới."),
    (2, "tp_tuyet", "Trang phục Pokémon Tuyết", [5, 31], 1, "su_kien", "lich:tetduonglich;quai:300", 0,
     "Mũ len và khăn sọc cho mùa đông đầu năm."),
    (3, "tp_tet", "Trang phục Tết Nguyên Đán", [2, 32, 56], 2, "su_kien", "lich:tet2017;quai:400", 0,
     "Nón lá, khăn đỏ viền vàng và bao lì xì may mắn."),
    (4, "tp_valentine", "Trang phục Valentine", [22, 42, 54], 2, "su_kien", "lich:moi:valentine;quai:300", 0,
     "Nơ tim, cánh thiên thần và đoá hồng."),
    (5, "tp_hoa_thang_ba", "Trang phục Hoa tháng Ba", [12, 37], 1, "su_kien", "lich:quocte83,moi:2010;quai:300", 0,
     "Vòng hoa xuân và dây chuyền ngọc trai."),
    (6, "tp_tham_tu", "Trang phục Thám tử", [7, 52], 2, "su_kien", "lich:moi:congdong;quai:200", 0,
     "Mũ thám tử và kính lúp - quà Ngày cộng đồng."),
    (7, "tp_thieu_nhi", "Trang phục Thiếu nhi", [13, 33, 51], 1, "su_kien", "lich:moi:thieunhi;quai:200", 0,
     "Mũ tiệc, nơ cổ và bóng bay 1/6."),
    (8, "tp_bien_he", "Trang phục Biển hè", [8, 35, 57], 2, "su_kien", "lich:he2017;quai:500", 0,
     "Mũ rơm, vòng hoa Hawaii và que kem mát lạnh."),
    (9, "tp_vu_lan", "Trang phục Vu Lan", [21, 37, 54], 1, "su_kien", "lich:vulan;quai:300", 0,
     "Hoa cài, dây chuyền ngọc và bông hồng tặng mẹ."),
    (10, "tp_trung_thu", "Trang phục Trung Thu", [15, 50], 2, "su_kien", "lich:trungthu;quai:400", 0,
     "Tai thỏ ngọc và đèn ông sao đêm rằm."),
    (11, "tp_halloween", "Trang phục Halloween", [4, 41, 53], 3, "su_kien", "lich:halloween;quai:500", 0,
     "Mũ phù thuỷ, áo choàng tím và đũa phép."),
    (12, "tp_giang_sinh", "Trang phục Giáng sinh", [3, 31, 58], 3, "su_kien", "lich:noel;quai:400", 0,
     "Mũ Noel, khăn len sọc và kẹo gậy."),
    (13, "tp_tuan_loc", "Trang phục Tuần lộc", [16, 34], 2, "thanh_tuu", "danh_hieu:noel", 0,
     "Sừng tuần lộc và lục lạc vàng - danh hiệu Tuần lộc tuyết trắng."),
    (14, "tp_lap_lanh", "Trang phục Lấp lánh", [6, 37], 3, "thanh_tuu", "danh_hieu:shiny_1", 0,
     "Vương miện và chuỗi ngọc cho người gặp Pokémon shiny đầu tiên."),
    (15, "tp_vo_dich_gym", "Trang phục Nhà vô địch Gym", [1, 46], 3, "thanh_tuu", "danh_hieu:gym_8", 0,
     "Áo choàng Gym có huy hiệu - thắng 8 Gym."),
    (16, "tp_nha_nghien_cuu", "Trang phục Nhà nghiên cứu", [24, 52], 2, "thanh_tuu", "danh_hieu:dex_50", 0,
     "Mũ thám hiểm và kính lúp - Pokédex 50 loài."),
    (17, "tp_ngu_dan", "Trang phục Ngư dân", [23, 55], 2, "thanh_tuu", "danh_hieu:ca_300", 0,
     "Mũ ngư dân và cần câu - câu 300 lần."),
    (18, "tp_nha_vo_dich", "Trang phục Nhà vô địch", [6, 40], 4, "thanh_tuu", "danh_hieu:top_pvp_10", 0,
     "Vương miện và áo choàng đỏ của top 10 Nhà vô địch PvP."),
    (19, "tp_dau_bep", "Trang phục Đầu bếp Poké", [10, 33], 1, "thanh_tuu", "danh_hieu:sinh_thai_06", 0,
     "Mũ đầu bếp và nơ cổ - danh hiệu Đầu bếp Poké."),
    (20, "tp_tot_nghiep", "Trang phục Tốt nghiệp", [11, 33], 2, "thanh_tuu", "danh_hieu:cap_50", 0,
     "Mũ tốt nghiệp cho huấn luyện viên cấp 50."),
    (21, "tp_hai_tac", "Trang phục Hải tặc", [9, 36], 2, "cua_hang", "", 200,
     "Mũ tam giác và khăn rằn của hải tặc Cinnabar."),
    (22, "tp_ninja", "Trang phục Ninja", [25, 36], 2, "cua_hang", "", 200, "Băng đô ninja và khăn rằn."),
    (23, "tp_hiep_si", "Trang phục Hiệp sĩ", [18, 40], 3, "cua_hang", "", 250, "Mũ giáp lông đỏ và áo choàng."),
    (24, "tp_phi_cong", "Trang phục Phi công", [20, 30], 2, "cua_hang", "", 200, "Mũ phi công có kính và khăn trắng."),
    (25, "tp_ba_tuoc", "Trang phục Bá tước bóng đêm", [43, 33], 3, "cua_hang", "", 250, "Cánh dơi và nơ cổ đỏ."),
    (26, "tp_thien_than", "Trang phục Thiên thần", [12, 42], 3, "cua_hang", "", 250, "Vòng hoa và đôi cánh trắng."),
    (27, "tp_cao_boi", "Trang phục Cao bồi", [17, 36], 2, "cua_hang", "", 200, "Mũ cao bồi và khăn rằn."),
    (28, "tp_canh_buom", "Trang phục Cánh bướm", [21, 44], 2, "cua_hang", "", 200, "Hoa cài và cánh bướm hồng."),
    (29, "tp_tho_mo", "Trang phục Thợ mỏ", [14, 36], 1, "che_tao", "nl:1034x10,1035x4;gio:2", 0,
     "Mũ thợ mỏ có đèn - chế ở Machamp."),
    (30, "tp_tham_hiem", "Trang phục Thám hiểm", [19, 45], 2, "che_tao", "nl:1035x6,1036x2;gio:3", 0,
     "Kính bơi trên trán và ba lô thám hiểm."),
    (31, "tp_tai_tho", "Trang phục Tai thỏ tuyết", [15, 31], 1, "che_tao", "nl:1034x12;gio:1", 0,
     "Tai thỏ và khăn len - món chế đầu tiên cho người mới."),
    (32, "tp_hiep_khach", "Trang phục Huấn luyện viên Gym", [25, 46], 3, "che_tao", "nl:1036x4,1037x2;gio:5", 0,
     "Băng đô và áo choàng Gym - món chế cao cấp."),
]

# ================================================================ HÀO QUANG / SKIN THÚ / THẺ BẠN ĐỒNG HÀNH (nguồn)
# (mã trong hinh_trang_phuc, độ hiếm, nguồn, tham số, giá lượng, mô tả)
NGUON_HAO_QUANG = {
    "tap_su": (0, "mien_phi", "cap:10", 0, "Vòng sáng đầu tiên của mọi huấn luyện viên."),
    "lua": (2, "che_tao", "nl:1035x5;gio:2", 0, "Vòng lửa chế từ Mảnh sao."),
    "nuoc": (2, "che_tao", "nl:1035x5;gio:2", 0, "Vòng nước chế từ Mảnh sao."),
    "co": (2, "che_tao", "nl:1035x5;gio:2", 0, "Vòng lá chế từ Mảnh sao."),
    "dien": (2, "che_tao", "nl:1035x5;gio:2", 0, "Vòng điện chế từ Mảnh sao."),
    "bang": (2, "cua_hang", "", 200, "Vòng băng lấp lánh."),
    "tam_linh": (3, "thanh_tuu", "danh_hieu:pvp_100", 0, "Huấn luyện viên dày dạn: thắng 100 trận PvP."),
    "rong": (4, "thanh_tuu", "danh_hieu:boss_100", 0, "Khắc tinh huyền thoại: hạ 100 boss."),
    "tien": (2, "cua_hang", "", 200, "Vòng hồng của các tiên tử."),
    "bong_toi": (2, "cua_hang", "", 200, "Vòng bóng đêm."),
    "cau_vong": (4, "thanh_tuu", "danh_hieu:dex_90", 0, "Người hoàn thiện Pokédex."),
    "anh_dao": (3, "su_kien", "lich:tet2017;quai:600", 0, "Hoa anh đào ngày Tết."),
    "sao_bang": (3, "su_kien", "lich:moi:shiny;quai:500", 0, "Sao băng Tuần Shiny."),
}
NGUON_SKIN_THU = {
    "yen_co_ban": (0, "mien_phi", "cap:15", 0, "Yên cưỡi đầu tiên khi có thú cưỡi."),
    "yen_da": (1, "che_tao", "nl:1034x8;gio:1", 0, "Yên da chế ở Machamp."),
    "yen_hoa": (2, "su_kien", "lich:gioto,moi:3004;quai:300", 0, "Yên hoa mùa xuân."),
    "yen_vang": (3, "cua_hang", "", 150, "Yên vàng hoàng gia."),
    "yen_bang": (2, "su_kien", "lich:noel;quai:300", 0, "Yên băng Giáng sinh."),
    "yen_lua": (3, "thanh_tuu", "danh_hieu:gym_16", 0, "Nhà vô địch hai vùng."),
    "yen_dem": (2, "cua_hang", "", 150, "Yên bóng đêm."),
    "yen_cau_vong": (4, "thanh_tuu", "danh_hieu:map_120", 0, "Người đi khắp thế gian."),
}
NGUON_THE = {
    "the_co_ban": (0, "mien_phi", "cap:20", 0, "Thẻ tên cho bạn đồng hành đầu tiên."),
    "no_do": (1, "cua_hang", "", 80, "Nơ đỏ trên đầu bạn đồng hành."),
    "chuong_vang": (1, "che_tao", "nl:1034x6;gio:1", 0, "Chuông vàng leng keng."),
    "khan_xanh": (1, "che_tao", "nl:1034x6;gio:1", 0, "Khăn xanh nhỏ."),
    "vuong_mien_nho": (3, "thanh_tuu", "danh_hieu:dang_nhap_30", 0, "Người bạn trung thành: 30 ngày đăng nhập."),
    "hoa_cai_nho": (2, "su_kien", "lich:quocte83,moi:2010;quai:200", 0, "Hoa cài ngày phụ nữ."),
    "ngoi_sao_nho": (2, "su_kien", "lich:moi:shiny;quai:300", 0, "Ngôi sao Tuần Shiny."),
    "tim_hong_nho": (2, "cua_hang", "", 80, "Tim hồng."),
}
