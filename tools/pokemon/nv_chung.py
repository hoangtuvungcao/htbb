# -*- coding: utf-8 -*-
"""
DU LIEU CHUNG cho bo sinh nhiem vu (tools/pokemon/sinh_nhiem_vu.py). Chi la bang, khong logic.

  NPC          : NPC tinh (type trong docs/pokemon/bang_npc.csv) -> ten Pokemon + giong noi.
  DIA_DANH_MAP : dia danh -> id ban do (docs/pokemon/thiet_ke_map.csv, docs/pokemon/map_cu/<id>.json).
  KIEU         : moi kieu muc tieu: ten, co che server phat hien, kieu du phong cho engine cu
                 (data_quest type 0..4), trang thai engine, mau chu (muc tieu / goi y / dang lam).
  VAT_NV       : vat pham nhiem vu MOI (ItemQuest hien chi co 10 ten, GD2 4.5 mo rong).

Moi chu o day la chu NGUOI CHOI THAY: khong dung tu cam (ChuPokemon.TU_CAM). Nguoi choi la
mot Pokemon; moi NPC deu la Pokemon. Xung ho: NPC goi nguoi choi theo "goi" cua NPC.
"""

# type -> (ten Pokemon, cau mo dau dac trung, cach goi nguoi choi, tu xung, tinh cach ngan)
NPC = {
    0: ("Chansey", "Chan-sey!", "em", "chị", "y tá dịu dàng, lo cho sức khoẻ mọi người"),
    1: ("Kecleon", "Hí hí,", "bạn nhỏ", "tôi", "chủ tạp hoá lém lỉnh, hay đổi màu khi nói dối"),
    2: ("Machamp", "Khà khà!", "cháu", "ta", "thợ chế tạo bốn tay, nói to, thương người"),
    3: ("Bronzong", "Boong... boong...", "cậu", "ta", "thợ giáp chậm rãi, thích triết lý"),
    4: ("Arcanine", "Gâu!", "cậu", "tôi", "lính gác trung thành, nghiêm túc"),
    5: ("Xatu", "...", "cháu", "ta", "trưởng làng thấy trước tương lai, nói như tiên tri, biết dịch chuyển"),
    6: ("Persian", "Hừm.", "cưng", "ta", "quý ông giàu có kiêu kỳ, mê đồ đẹp, tốt bụng ngầm"),
    7: ("Pidgeot", "Vút!", "bạn", "tôi", "người chở bay nhanh nhảu"),
    8: ("Snorlax", "Oáp...", "cháu", "ông", "ông nội ngủ nhiều, mê ăn, rất thông thái"),
    9: ("Kangaskhan", "Ôi con yêu,", "con", "cô", "thủ kho như người mẹ, hay lo"),
    10: ("Lapras", "La la~", "bạn nhỏ", "mình", "người chở đò hiền lành, hay hát"),
    11: ("Golem", "Ùng ục.", "cậu", "tôi", "người bán hàng ít nói"),
    16: ("Tauros", "Ùm bò!", "bạn", "tôi", "người bán hàng hăng hái"),
    21: ("Blaziken", "Lửa lên nào!", "nhóc", "tôi", "huấn luyện viên nóng tính, hết lòng vì học trò"),
    22: ("Meowth", "Meo!", "bạn", "tôi", "thương nhân mê xu, nói nhanh, hay mặc cả"),
    23: ("Torkoal", "Khụ khụ...", "cháu", "lão", "lão già ở Cerulean, khói than, cằn nhằn nhưng ấm áp"),
    24: ("Sableye", "Hì hì,", "cậu", "tớ", "người buôn đá quý tinh nghịch"),
    25: ("Heracross", "Hự!", "chiến hữu", "ta", "chỉ huy trọng danh dự"),
    28: ("Farfetch'd", "Hứ!", "cậu", "ta", "kiêu hãnh với cọng hành của mình"),
}

# Nhan vat cot truyen (khong phai NPC tinh: noi qua hoi thoai, xuat hien bang quai/boss khi danh).
NHAN_VAT = {
    "Lém": "Zorua tinh quái, đối thủ của bạn, hay hoá thành Pokémon khác",
    "Murkrow Mặt Nạ": "trinh sát của Đoàn Mặt Nạ Tím, nói nhiều, hay khoe",
    "Honchkrow": "phó thủ lĩnh Đoàn Mặt Nạ Tím, lạnh lùng",
    "Kẻ Vô Danh": "thủ lĩnh giấu mặt của Đoàn Mặt Nạ Tím (về sau lộ là Darkrai)",
    "Darkrai": "chúa tể giấc mơ, muốn cả thế giới ngủ yên để không ai phải đau",
    "Natu": "học trò nhỏ của Xatu, tò mò, hay hỏi",
}

# Dia danh -> id ban do (thiet_ke_map.csv). Noi khong co ban do rieng dung ban do gan nhat.
DIA_DANH_MAP = {
    "Pewter": 0, "Pallet": 1, "Viridian": 2, "Vermilion": 3, "Cerulean": 4, "Lavender": 5,
    "Celadon": 6, "Saffron": 7, "Fuchsia": 8, "Cinnabar": 9, "New Bark": 10, "Rừng Viridian": 11,
    "Khu mua bán": 12, "Núi Mặt Trăng": 13, "Vùng Safari": 14, "Cherrygrove": 15, "Khu mỏ": 17,
    "Goldenrod": 18, "Ecruteak": 19, "Violet": 20, "Đồng cỏ phía tây": 21, "Tàu S.S. Anne": 22,
    "Azalea": 23, "Olivine": 24, "Tháp luyện tập": 25, "Rừng Cổ": 26, "Mahogany": 28, "Rừng Ilex": 29,
    "Chiến trường": 40, "Chân núi Silver": 41, "Núi Silver": 42, "Blackthorn": 70,
    "Trung tâm Pokémon": 103, "Poké Mart": 104, "Cianwood": 80, "Nhà Pewter": 100, "Bến tàu": 106,
    "Thuyền": 107, "Nhà kho": 108, "Xưởng chế tạo": 109, "Hang động": 110, "Hang Darkrai": 111,
    "Hang Salamence": 112, "Hang Tối": 113, "Hồ Phẫn Nộ": 114, "Hang nhỏ": 115, "Hang cổ": 116,
    "Ngục tối": 117, "Đường Victory": 118, "Cao nguyên Indigo": 201, "Đấu trường": 202,
    "Lãnh địa đội": 206, "Sảnh chờ": 228, "Sân nhà khởi đầu": 105, "Tổ Zapdos": 110,
    "Phòng nghiên cứu": 100, "Trụ sở đội": 206, "Hang Serperior": 116,
}
# Vung Hoenn (nam 2, ban do moi BanDoHoenn) - tools/pokemon/nv_hoenn.py
from nv_hoenn import DIA_DANH as _DIA_DANH_HOENN  # noqa: E402
DIA_DANH_MAP.update(_DIA_DANH_HOENN)

# Vat pham nhiem vu MOI (ten -> ma). Engine cu chi co data.ItemQuest 0..9; GD2 4.5 them lop
# data/ItemQuest.java de mo rong. Ma 10.. danh cho bang nay (giu id cu 0..9 nguyen nghia).
VAT_NV = [
    "Gói hàng", "Giỏ quả Oran", "Mảnh đá phát sáng", "Lông vũ tím", "Mảnh sao băng", "Vé tàu",
    "Chuông bạc", "Mảnh bản đồ", "Quặng sắt", "Lọ thuốc", "Trứng thất lạc", "Hạt giống Berry",
    "Vỏ sò", "Bản ghi chép", "Hoa sương", "Mật ong", "Đá Unown", "Ngọc trai", "Bức thư cũ",
    "Phong bì xanh", "Than hồng", "Vảy cá vàng", "Nấm phát quang", "Cành hành thơm",
    "Lá trà Ecruteak", "Vòng cổ ngọc", "Sổ tay thuyền trưởng", "Cuộn băng radio", "Vảy rồng nhỏ",
    "Mảnh băng vĩnh cửu", "Đèn lồng", "Bánh Lava", "Kẹo bông", "Hộp nhạc", "Tấm ảnh",
    "Bình sữa", "Bức tranh", "Lông vũ trăng", "Hạt giống hoa", "Bánh quả mọng", "Chăn len",
]
VAT_NV_ID = {t: 10 + i for i, t in enumerate(VAT_NV)}

# Vat pham nhiem vu CU dung duoc (data.ItemQuest.NAME_ITEM_QUEST trong lib/htbb-core.jar).
VAT_NV_CU = {0: "Nọc bọ cạp", 2: "Khúc gỗ", 3: "Rết đỏ", 4: "Lá thư", 6: "Lông đuôi Hồng Kê",
             7: "Móng cá sấu", 8: "Da ếch", 9: "Cựa gà"}

# --------------------------------------------------------------------------------------------
# KIEU MUC TIEU
# ma -> (ten, co che server (noi phat hien), du phong engine cu (type data_quest hoac ""),
#        trang thai: co_san | gd2 (engine moi) | he_sinh_thai (sv.he_sinh_thai) | gd3,
#        mau muc tieu, mau goi y, mau "dang lam")
# Cho trong mau: {so} {quai} {noi} {map} {npc} {npc_nhan} {vat} {x} {y} {thoi_tiet} {gio} {phut}
#                {bac} {so2}
# --------------------------------------------------------------------------------------------
KIEU = {
    "noi_chuyen": ("Nói chuyện", "Char.OnQuest type 3 (hoàn thành ngay tại NPC)", 3, "co_san",
                   "Nói chuyện với {npc} ở {noi}", "{npc} đang ở {noi}.",
                   "{Goi} cứ từ từ, xong việc thì ghé {xung} nhé."),
    "dua_thu": ("Đưa thư", "Char.OnQuest type 1 (vật phẩm nhiệm vụ 4, npcres)", 1, "co_san",
                "Mang lá thư tới {npc_nhan}", "Lá thư nằm trong túi nhiệm vụ. {npc_nhan} đang ở {noi}.",
                "Lá thư vẫn còn trong túi đấy, đừng làm nhàu nhé."),
    "giao_hang": ("Giao hàng", "Char.OnQuest type 1 + vật phẩm nhiệm vụ mới (GĐ2 4.5)", 1, "gd2",
                  "Giao {vat} cho {npc_nhan}", "{npc_nhan} đang chờ ở {noi}. Hàng nằm trong túi nhiệm vụ.",
                  "{vat} vẫn trong túi đấy. Giao sớm kẻo {npc_nhan} chờ."),
    "giet": ("Hạ quái", "Monster.onDropItem → Char.checkFinishQuest (type 0)", 0, "co_san",
             "Hạ {so} {quai}", "{quai} hay xuất hiện ở {noi}.",
             "Mới được {da}/{so} {quai} thôi. Cố lên!"),
    "giet_lech_cap": ("Hạ quái cùng tầm", "Char.checkFinishQuest type 4 (deltaLv 5)", 4, "co_san",
                      "Hạ {so} Pokémon hoang dã chênh không quá 5 cấp",
                      "Chọn bãi quái ngang cấp bạn. Quái thấp hơn bạn quá 5 cấp không được tính.",
                      "Mới {da}/{so} trận. {Goi} tìm bãi quái ngang sức nhé."),
    "thu_thap": ("Thu thập", "Map.doGetItemQuest → checkFinishQuest (type 2)", 2, "co_san",
                 "Thu {so} {vat} (rơi từ {quai})", "Hạ {quai} ở {noi} rồi nhặt {vat} rơi ra.",
                 "Có {da}/{so} {vat} rồi. Còn thiếu một chút."),
    "giet_tinh_anh": ("Thách đấu", "Char.checkFinishQuest type 0 trên quái cấp cao hơn", 0, "co_san",
                      "Đánh bại {so} {quai} mạnh", "{quai} cấp cao hơn bạn vài cấp: mang thuốc theo.",
                      "Đối thủ mạnh đấy. Hồi máu rồi đánh tiếp."),
    "ha_alpha": ("Hạ Alpha", "Cờ alpha trên Monster (GĐ2 6.7, MapNapTaiNguyen.loadMap) → Monster.onDropItem", 0, "gd2",
                 "Đánh bại Alpha {quai}", "Alpha to hơn, có viền đỏ, khoảng 1/50 cơ hội xuất hiện ở {noi}. Khi Alpha hiện ra, cả bản đồ đều được báo.",
                 "Alpha {quai} chưa ngã. Kiên nhẫn chờ nó xuất hiện."),
    "ha_gym": ("Thắng Gym", "Boss Gym (noi_dung_gym, GĐ2 4.6 + BossLauDai.khiChet)", 0, "gd2",
               "Thắng trưởng Gym {noi}: {quai}", "Trưởng Gym hệ {he}: mang chiêu khắc hệ và đồ +6 trở lên.",
               "Trưởng Gym {quai} vẫn chờ {goi} ở Gym {noi}."),
    "ho_tong": ("Hộ tống", "Giao hàng (CharSuKien.doResponseTieu) với hàng nhiệm vụ", 3, "gd2",
                "Hộ tống {vat} từ {noi} tới {npc_nhan} an toàn", "Đi đường lớn, quái có thể chặn đường cướp hàng.",
                "Hàng vẫn đang trên đường. Đừng để bị cướp!"),
    "den_noi": ("Khám phá", "Map.playerJoin / MapXuLyLenh.doChangeMap (hook vào bản đồ)", 3, "gd2",
                "Đến {map}", "Hỏi Pidgeot ở làng để bay nhanh, hoặc đi bộ theo cổng.",
                "{Goi} vẫn chưa đặt chân tới {map}."),
    "dung_toa_do": ("Đứng tại điểm", "CharCapNhat.doPlayerMove (vị trí, bán kính 3 ô)", 3, "gd2",
                    "Đứng tại {diem} trên {map} (ô {x},{y})", "Mở bản đồ nhỏ, tìm ô {x},{y} rồi đứng yên vài giây.",
                    "Chưa đúng chỗ. Ô {x},{y} trên {map} cơ."),
    "tim_vat_an": ("Tìm vật ẩn", "Điểm tương tác ẩn trên bản đồ (MapDoMenu, NPC ẩn) hoặc điểm đào", 2, "gd2",
                   "Tìm {vat} giấu ở {map}", "Vật ẩn lấp lánh khi bạn đứng gần. Thử các góc khuất của {map}.",
                   "Vẫn chưa thấy {vat}. Để ý chỗ có ánh sáng lạ."),
    "dao_dat": ("Khảo cổ", "HeSinhThaiMenu.dao (lệnh chat \"dao\" qua HeSinhThaiGame.khiChat); chỉ hang/núi/sa mạc/tuyết, 6 điểm/ngày", 2, "he_sinh_thai",
                "Đào {so} lần ở {map}", "Gõ lệnh dao vào kênh trò chuyện (hoặc mở menu Hệ sinh thái). Máy dò chỉ hướng điểm đào. Mỗi lần đào tốn 4 thể lực và tiền xẻng.",
                "Mới đào {da}/{so} lần."),
    "cau_ca": ("Câu cá", "HeSinhThaiMenu.cauCa (lệnh chat \"cauca\"): CHỈ ở hồ, đứng cách hồ ≤ 2 ô (docs/pokemon/diem_cau_ca.csv)", 2, "he_sinh_thai",
               "Câu được {so} {vat}", "Chỉ câu được ở hồ: đứng cách bờ hồ ở {noi} tối đa 2 ô rồi gõ lệnh cauca. Hồ lớn mới có cá hiếm. Mỗi lần câu tốn 2 thể lực và mồi câu.",
               "Có {da}/{so} rồi. Kiên nhẫn nhé, {goi}."),
    "lam_vuon": ("Làm vườn", "Char.doHarvest → HeSinhThaiGame.khiThuHoach (vườn berry trên bản đồ cá nhân)", 2, "he_sinh_thai",
                 "Trồng và thu hoạch {so} {vat}", "Gieo hạt ở vườn nhà (bản đồ cá nhân), chờ cây lớn rồi thu hoạch. Bạn bè tưới giúp được thêm quả.",
                 "Vườn mới cho {da}/{so} {vat}."),
    "nau_an": ("Nấu ăn", "HeSinhThaiMenu.lamCongThuc (menu Hệ sinh thái → Công thức)", "", "he_sinh_thai",
               "Nấu {so} {vat}", "Mở menu Hệ sinh thái, chọn Công thức. Cần berry hoặc cá trong Túi sinh thái. Mỗi mẻ tốn 3 thể lực và tiền bếp.",
               "Món ăn chưa xong. Nhóm lửa lại đi."),
    "ap_trung": ("Trứng nở", "NhaTre + HeSinhThaiMenu.hanhDongTrung (trứng nở sau 250–800 quái hạ)", "", "he_sinh_thai",
                 "Cho {so} quả trứng nở", "Gửi 1-2 pet ở Nhà trẻ: cứ hạ 300 quái là được 1 trứng. Mang trứng theo người, trứng nở sau 250-800 trận.",
                 "Trứng vẫn ấm, chưa nở. Đi thêm vài bước."),
    "che_tao": ("Chế tạo", "MapCheTao.doUserCreateItem", "", "gd2",
                "Chế tạo {vat} ở Xưởng", "Machamp ở Xưởng chế tạo có công thức. Cần đá nâng cấp và nguyên liệu.",
                "Chưa thấy món {vat} nào ra lò."),
    "nang_cap_cong": ("Nâng cấp", "NangCapLauDaiGame.thu (MapEpNgoc.doFinishImbue, CharCapNhat.doAutoImbue)", "", "gd2",
                      "Nâng 1 món trang bị lên +{so}", "Mang đá nâng cấp tới Machamp. Từ +7 nên dùng Bùa bảo hộ.",
                      "Món đồ chưa đạt +{so}. Thử lại nhé, mỗi lần hỏng đều cộng dồn may mắn."),
    "nang_cap": ("Mặc trang bị", "Map.doUseItem (mặc đồ)", 3, "co_san",
                 "Mặc 1 món trang bị", "Mở túi đồ, chọn món đồ rồi chọn Mặc.", "{Goi} vẫn chưa mặc đồ mới."),
    "bo_trang_bi": ("Bộ trang bị", "Char.calculateAttrib / checkKickAtt5 (đếm món cùng bậc)", "", "gd2",
                    "Mặc {so} món cùng bậc {bac}", "Mua ở Bronzong, chế tạo hoặc nhặt từ quái cùng dải cấp.",
                    "Mới có {da}/{so} món bậc {bac}."),
    "kham_ngoc": ("Khảm ngọc", "MapEpNgoc.doFinishKhamItem", "", "gd2",
                  "Khảm {so} ngọc hệ vào trang bị", "Sableye bán ngọc hệ, còn Machamp là thợ khảm.",
                  "Mới khảm {da}/{so} viên."),
    "phan_ra": ("Phân rã", "RoiLauDai.phanRa (MapXuLyLenh.doGetItem)", "", "gd2",
                "Phân rã {so} món đồ thừa", "Đồ trùng phân rã ra đá nâng cấp ở Machamp.", "Mới phân rã {da}/{so}."),
    "thang_pvp": ("Đấu trường", "ThangPvpGame.ketQua(thắng, thua)", "", "gd2",
                  "Thắng {so} trận đấu trường", "Vào Đấu trường ở làng. Trận được ghép theo dải 10 cấp.",
                  "Thắng {da}/{so} trận rồi."),
    "to_doi": ("Tổ đội", "Region.doAttackMonster (vòng party.userParty) — cộng cho cả tổ", 4, "gd2",
               "Lập nhóm từ {so2} Pokémon, cùng hạ {so} quái", "Mời bạn bè vào nhóm (menu Xã hội).",
               "Cả nhóm mới hạ {da}/{so} quái."),
    "doi_dong_gop": ("Đóng góp Đội", "MapDoi (nhiệm vụ Đội, mainsub=2) + ChiemGymLauDai.dongGop", 4, "gd2",
                     "Đóng góp {so} điểm cho đội", "Hạ quái, làm nhiệm vụ đội và tham gia chiếm Gym đều được cộng điểm.",
                     "Đội đã có {da}/{so} điểm."),
    "boss_the_gioi": ("Boss thế giới", "BossLauDai.satThuongVaoBoss + khiChet (bảng đóng góp)", "", "gd2",
                      "Góp sức đánh {quai} (từ {so} phần nghìn sát thương)",
                      "Boss xuất hiện theo lịch (thông báo toàn máy chủ). Đánh từ đầu tới cuối để có điểm.",
                      "Boss chưa ngã, hoặc {goi} góp sức chưa đủ."),
    "quan_sat": ("Quan sát", "PokedexPokemon.khiDanh + HeSinhThaiGame.khiHaQuai (thoiTietMap, laDem 19:00–05:59)", "", "gd2",
                 "Quan sát {quai} {khi_dk}", "Hãy chờ đúng thời điểm ({dk}). Chỉ cần chạm trán (đánh 1 đòn) là được ghi vào sổ. Trong hang và trong nhà trời luôn quang đãng.",
                 "{Goi} chưa gặp {quai} {khi_dk}."),
    "pokedex_gap": ("Pokédex: gặp", "PokedexPokemon.khiDanh (tob_pokedex)", "", "gd2",
                    "Ghi {quai} vào Pokédex", "Chạm trán là ghi được.", "Chưa gặp {quai}."),
    "pokedex_ha": ("Pokédex: nghiên cứu", "PokedexPokemon.khiHaQuai (tob_pokedex.so_ha)", "", "gd2",
                   "Hạ {so} {quai} để nghiên cứu", "{quai} sống ở dải cấp {cap}.", "Đã ghi {da}/{so} lần."),
    "pokedex_shiny": ("Pokédex: shiny", "RoiLauDai.shiny → tob_pokedex_shiny", "", "gd2",
                      "Thấy {quai} màu sắc khác thường (shiny)", "Tuần Shiny và ngày cộng đồng tỉ lệ cao hơn.",
                      "Chưa gặp {quai} shiny."),
    "pokedex_vung": ("Pokédex vùng", "tob_pokedex (đếm loài theo vùng)", "", "gd2",
                     "Ghi {so} loài sống ở {noi}", "Mỗi bản đồ có loài riêng. Xem bảng quái trên website.",
                     "Mới {da}/{so} loài."),
    "thu_thach_gio": ("Thử thách giờ", "Bộ đếm giờ trong tob_nhiem_vu_lau_dai + checkFinishQuest", "", "gd2",
                      "Hạ {so} {quai} trong {phut} phút", "Đồng hồ bắt đầu chạy khi nhận nhiệm vụ. Hết giờ thì nhận lại.",
                      "Đồng hồ đang chạy!"),
    "cau_do": ("Câu đố", "MapChat.doChat (cạnh HeSinhThaiGame.khiChat; so đáp án trước khi phát kênh)", "", "gd2",
               "Giải câu đố của {npc}: gõ đáp án vào kênh trò chuyện", "Gợi ý nằm trong lời {npc}. Không phân biệt chữ hoa, chữ thường.",
               "Chưa đúng. {Goi} nghĩ thêm chút nữa nhé."),
    "lua_chon": ("Lựa chọn", "Menu NPC (MapDoMenu) ghi nhánh vào tob_nhiem_vu_lau_dai", 3, "gd2",
                 "Chọn một con đường", "Mỗi lựa chọn cho một danh hiệu khác nhau. Không có lựa chọn nào sai.",
                 "{Xung} vẫn chờ câu trả lời của {goi}."),
    "kem_cap": ("Sư phạm", "SoTayLauDai.suPham / khiLenCap (tob_su_pham)", "", "gd2",
                "Kèm {muc} lên cấp", "Nhận học trò kém mình từ 20 cấp trở lên và đi chung nhóm.", "Học trò chưa đạt mốc."),
    "giup_tan_thu": ("Giúp người mới", "Region party + cấp thành viên < 20", 4, "gd2",
                     "Lập nhóm với 1 Pokémon dưới cấp 20, cùng hạ {so} quái", "Người mới thường ở Pallet và Pewter.",
                     "Mới {da}/{so}."),
    "tang_hang": ("Hầm ngục", "HamNgucGame.khiHaQuai / nhanThuong (tob_ham_nguc)", "", "gd2",
                  "Vượt {so} tầng Hầm ngục bí ẩn", "Mỗi ngày hầm ngục đổi lối một lần. Càng xuống sâu quái càng mạnh.",
                  "Mới tới tầng {da}."),
    "dang_nhap": ("Đăng nhập", "LauDaiNhanVat.khiDoiNgay (QuaHoatDong)", "", "gd2",
                  "Đăng nhập {so} ngày", "Chỉ cần vào game mỗi ngày.", "Mới {da}/{so} ngày."),
    "su_kien_vat_pham": ("Vật phẩm sự kiện", "Monster.onDropItem (khối rơi sự kiện) + MapSuKien", "", "gd2",
                         "Thu {so} {vat} trong sự kiện", "Vật phẩm sự kiện rơi từ quái ngang cấp trong tuần sự kiện.",
                         "Có {da}/{so} {vat}."),
    "do_vui": ("Đố vui", "HeSinhThaiGame.DoVui.traLoi (qua khiChat, 12:30 và 21:30)", "", "he_sinh_thai",
               "Trả lời đúng {so} câu đố Ai là Pokémon?", "Trò chơi chạy trên kênh thế giới lúc 12:30 và 21:30. Gợi ý gồm hệ, số chữ cái và chữ cái đầu. Trả lời bằng kênh trò chuyện.",
               "Mới đúng {da}/{so} câu."),
    "dua_pokemon": ("Đua Pokémon", "HeSinhThaiGame.Dua.dangKy / ketThuc (19:00, map 18)", "", "he_sinh_thai",
                    "Về đích trong cuộc Đua Pokémon", "Gõ lệnh dua để đăng ký từ 18:50. Xuất phát lúc 19:00 ở tuyến Goldenrod, chạy qua 5 mốc từ trái sang phải.",
                    "Chưa về đích."),
    "cho_ban": ("Chợ", "HeSinhThaiMenu.rao + mua/traTienNguoiBan (Chợ sinh thái) hoặc banNpc", "", "he_sinh_thai",
                "Bán {so} món ở chợ", "Mở menu Hệ sinh thái, chọn Chợ: rao bán với giá gấp 1-3 lần giá tham chiếu. Thuế 6% và phí đăng 1%.",
                "Mới bán {da}/{so}."),
    "tham_gia_gym": ("Chiếm Gym", "ChiemGymLauDai.batDau / dongGop", "", "gd2",
                     "Tham gia một trận chiếm Gym cùng đội", "Trận mở theo lịch của đội. Chỉ cần gây sát thương là được tính.",
                     "Chưa tham gia trận nào."),
    "dan_y": ("Dàn ý", "Chương chưa viết (không còn dùng: 52 chương năm 2 đã có bản đồ Hoenn)", "", "gd3",
              "Chương dàn ý năm 2", "Mở ở ngày {ngay}.", ""),
}
# Bien the cau "dang lam" (chon tat dinh theo vi tri dong) de loi nhac khong lap mot mau.
# Cho them: {goi}/{Goi} = cach NPC goi nguoi choi, {xung}/{Xung} = NPC tu xung.
DANG_LAM_THEM = {
    "noi_chuyen": ["Khi nào rảnh thì qua gặp {xung}, {goi} nhé.", "{Xung} vẫn ở đây chờ {goi}."],
    "giet": ["{Goi} mới hạ {da}/{so} {quai}. Cố thêm chút nữa!", "Được {da}/{so} {quai} rồi. Đừng bỏ dở nhé!"],
    "giet_lech_cap": ["Quái thấp hơn quá 5 cấp không tính đâu. Mới {da}/{so} trận."],
    "thu_thap": ["{vat} rơi từ {quai} đấy. Mới có {da}/{so}."],
    "quan_sat": ["Chờ {dk} rồi tìm {quai} nhé, {goi}."],
    "pokedex_ha": ["Sổ mới ghi {da}/{so} lần về {quai}.", "Cần thêm vài trận với {quai} nữa: {da}/{so}."],
    "pokedex_gap": ["Chỉ cần chạm trán {quai} một lần là được, {goi}."],
    "cau_do": ["Chưa phải đâu. Đọc lại lời đố thử xem, {goi}."],
    "den_noi": ["{map} vẫn đang chờ {goi} ghé đấy."],
    "dung_toa_do": ["Gần tới rồi. Tìm ô {x},{y} trên {map} nhé."],
    "tim_vat_an": ["{vat} vẫn nằm đâu đó ở {map}. Tìm kỹ các góc khuất nhé."],
    "giao_hang": ["{npc_nhan} vẫn đang chờ {vat} đấy, {goi}."],
    "su_kien_vat_pham": ["Mới gom được {da}/{so} {vat}. Tuần lễ vẫn còn dài!"],
}

# Thanh tuu: dem tu bang tien do (tob_nhiem_vu_lau_dai + bang co san).
THANH_TUU_CO_CHE = {
    "cap": "khiLenCap (SoTayLauDai)", "pokedex": "tob_pokedex (đếm loài)", "chuong": "idMainQuest",
    "nang_cap": "NangCapLauDaiGame.thu", "phu": "đếm nhiệm vụ phụ xong", "ngay": "QuaHoatDong (ngày đăng nhập)",
    "pvp": "ThangPvpGame.ketQua", "boss": "BossLauDai.khiChet", "doi": "MapDoi", "bo": "checkKickAtt5",
    "su_kien": "MapSuKien", "cau_ca": "KyNangSong.cauCa", "dao_dat": "KyNangSong.dao", "nau_an": "bếp",
    "ap_trung": "NhaTre", "gym": "huy hiệu Gym", "alpha": "cờ alpha", "kham_pha": "Map.playerJoin (bản đồ khác nhau)",
    "su_pham": "tob_su_pham", "che_tao": "MapCheTao", "shiny": "tob_pokedex_shiny", "ham_nguc": "tob_ham_nguc",
    "cau_do": "câu đố đã giải", "huyen_thoai": "chuỗi huyền thoại xong", "an": "nhiệm vụ ẩn xong",
    "ngay_xong": "nhiệm vụ ngày xong", "tuan_xong": "nhiệm vụ tuần xong", "lua_chon": "nhánh đã chọn",
}

# --------------------------------------------------------------------------------------------
# Khop HE_SINH_THAI.md / VatLieuSinhThai: ten hien thi trong truyen -> vat lieu that ma engine dem.
# --------------------------------------------------------------------------------------------
VAT_SINH_THAI = {
    # cau ca
    "Vảy cá vàng": "Vảy Goldeen", "Ngọc trai": "Ngọc trai nhỏ", "Vỏ sò": "Vỏ sò",
    # vuon berry (8 loai berry cua vuon)
    "Quả Oran": "Berry Oran", "Hạt giống Berry": "Berry Pecha", "Hoa sương": "Berry Chesto",
    "Cành hành thơm": "Berry Leppa", "Nấm phát quang": "Berry Lum",
    # nau an (cong thuc KyNangSong)
    "Bánh mật ong": "Bánh Poké", "Bánh quả mọng": "Bánh Poké", "Bánh Lava": "Bánh Poké", "Kem sữa": "Bánh Poké",
    "Trà hoa sương": "Súp berry", "Trà mật ong": "Súp berry",
}
# Vat pham nhiem vu "cau" duoc o ban do nhiem vu (khong phai vat lieu): engine cho roi them khi cau o dung noi.
CAU_VAT_NHIEM_VU = {"Phong bì xanh", "Mảnh sao băng"}
# Loai ban do (thiet_ke_map.csv) cho phep khao co (HeSinhThaiMenu.dao).
LOAI_MAP_KHAO_CO = {"hang", "nui", "sa_mac", "tuyet"}
# Dieu kien quan sat -> ma may doc (SinhThaiTheGioi.TEN_THOI_TIET, dem 19:00-05:59, khung gio may chu).
# Cum chu dat sau ten loai: "Quan sat Wooper vao buoi sang", "... khi troi mua".
KHI_DK = {"ban đêm": "vào ban đêm", "buổi sáng": "vào buổi sáng", "bình minh": "lúc bình minh",
          "hoàng hôn": "lúc hoàng hôn", "giờ cộng đồng": "trong giờ cộng đồng"}
DK_QUAN_SAT = {
    "trời mưa": "thoi_tiet=Mưa", "sương mù": "thoi_tiet=Sương mù", "trời sương mù": "thoi_tiet=Sương mù",
    "nắng gắt": "thoi_tiet=Nắng gắt", "trời tuyết": "thoi_tiet=Tuyết", "bão cát": "thoi_tiet=Bão cát",
    "ban đêm": "dem", "buổi sáng": "gio=6-10", "bình minh": "gio=5-7", "hoàng hôn": "gio=17-19",
    "giờ cộng đồng": "su_kien=congdong",
}
