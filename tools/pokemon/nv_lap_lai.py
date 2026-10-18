# -*- coding: utf-8 -*-
"""
NHIEM VU LAP LAI + DAC BIET (chi du lieu). Bo sinh: tools/pokemon/sinh_nhiem_vu.py.

  NGAY        : mau bang nhiem vu ngay / bang truy na (moi bac cap 1 ban, moi ngay giao 5,
                thuong toi da 3 - GIOI_HAN_NGAY).
  BAC_NGAY    : cap goc cac bac.
  TUAN_TEN    : ten 12 mau tuan (T-<bac>.xx, engine hien co: ha quai +-5 cap).
  CHUOI_VUNG  : chuoi tuan theo vung (3 buoc, tinh vao 2 nhiem vu tuan/tuan).
  TO_DOI      : nhiem vu tuan cho to doi (tinh vao 2 nhiem vu tuan/tuan).
  DOI         : nhiem vu Doi (clan, mainsub=2).
  SU_KIEN     : 3 buoc cho moi loai su kien + cau mo rieng tung tuan.
  HUYEN_THOAI : 12 chuoi huyen thoai nhieu tuan.
  AN          : nhiem vu an (mo khi dat dieu kien).
  THANH_TUU   : thanh tuu theo bac.
  QUAN_SAT_DK : dieu kien quan sat cho Pokedex sinh thai.
"""

BAC_NGAY = [1, 20, 40, 60, 80]

# (cap toi thieu cua mau, kieu, tham so, ten, loi mo, loi xong). NPC: 21 bang ngay, 4 bang truy na.
NGAY = [
 (1, "giet", "q=+0", "Săn {quai} buổi sáng", "Lửa lên nào! Bảng hôm nay: {so} {quai} đang phá ruộng.", "Gọn gàng! Mai ghé bảng tiếp nhé."),
 (1, "giet_lech_cap", "", "Tuần tra ngang sức", "Tuần tra quanh làng: hạ {so} quái ngang sức.", "Làng yên ổn. Cảm ơn nhóc."),
 (1, "thu_thap", "vat=Gói hàng", "Hàng thất lạc", "Lũ {quai} tha mất {so} {vat} của Kecleon. Lấy lại giúp.", "Kecleon gửi lời cảm ơn, hí hí."),
 (1, "giao_hang", "npc=0;vat=Lọ thuốc", "Chuyển thuốc cho Chansey", "Chansey cần {vat} gấp. Chạy một vòng nhé.", "Chansey: Chan-sey! Kịp lúc lắm."),
 (1, "giao_hang", "npc=8;vat=Giỏ quả Oran", "Bữa phụ của ông", "Ông Snorlax đói rồi. Mang {vat} tới cho ông.", "Snorlax: Oáp... ngon. Cảm ơn cháu."),
 (1, "noi_chuyen", "npc=5", "Hỏi thăm trưởng làng", "Xatu có lời nhắn cho nhóc. Ghé nghe đi.", "Xatu: ...Hôm nay gió tốt. Đi đường cẩn thận."),
 (1, "pokedex_gap", "q=+0", "Ghi một loài mới", "Mở Pokédex, ghi thêm {quai} hôm nay.", "Sổ dày thêm một trang!"),
 (1, "dung_toa_do", "map={noi};diem=0", "Điểm ngắm cảnh hôm nay", "Hôm nay Natu chọn điểm ngắm cảnh ở {noi}. Đứng đó một lát.", "Gió mát, nắng đẹp. Nhóc nghỉ ngơi đủ rồi."),
 (1, "den_noi", "map={noi}", "Ghé thăm {noi}", "Có thư mời từ {noi}. Ghé qua đó hôm nay nhé.", "Dân {noi} vẫy tay chào nhóc."),
 (1, "thu_thach_gio", "q=+0;phut=5", "Chạy đua với đồng hồ", "Hạ {so} {quai} trong {phut} phút. Bấm giờ!", "Kịp giờ! Phản xạ tốt đấy."),
 (1, "tim_vat_an", "map={noi};vat=Hạt giống Berry", "Hạt giống rơi", "Gió thổi rơi gói {vat} ở {noi}. Tìm giúp.", "Tìm thấy rồi! Mùa sau có quả."),
 (1, "lam_vuon", "vat=Quả Oran;so=2", "Chăm vườn nhà", "Vườn nhà cần chăm: thu {so} {vat}.", "Giỏ quả đầy ắp."),
 (1, "cau_ca", "vat=Vảy cá vàng;so=2", "Mẻ cá buổi chiều", "Câu {so} con có {vat} cho bữa tối.", "Cá tươi rói!"),
 (1, "dao_dat", "map=Khu mỏ;so=2", "Khảo cổ ở khu mỏ", "Khu mỏ vừa lở một vách đá. Đào {so} lần xem có gì.", "Bạn đào được vài viên sỏi đẹp và một chiếc nút áo cũ."),
 (5, "nau_an", "vat=Bánh quả mọng;so=1", "Bếp chung", "Chansey mời nấu {vat} cho bữa chung.", "Cả làng khen ngon."),
 (5, "ha_alpha", "q=+0", "Truy nã: Alpha {quai}", "BẢNG TRUY NÃ! Alpha {quai} quấy phá. Hạ nó!", "Gâu! Một tên truy nã đã gục."),
 (5, "giet_tinh_anh", "q=+3", "Truy nã: {quai} hung dữ", "Truy nã: {so} {quai} mạnh hơn bình thường đang lộng hành.", "Gâu! Tiền thưởng của cậu đây."),
 (5, "quan_sat", "q=+0;dk=trời mưa", "Quan sát trong mưa", "Natu muốn biết {quai} làm gì khi mưa. Quan sát giúp.", "Natu ghi chép say sưa."),
 (5, "quan_sat", "q=+0;dk=ban đêm", "Quan sát ban đêm", "Quan sát {quai} ban đêm cho sổ nghiên cứu.", "Ban đêm chúng hiền hơn ban ngày."),
 (5, "cau_do", "dap=Pikachu", "Câu đố buổi sáng: má đỏ", "Natu đố: 'Má đỏ, đuôi tia chớp, ai cũng yêu. Ta là ai?'", "Natu: Đúng! Dễ quá phải không?"),
 (5, "cau_do", "dap=Jigglypuff", "Câu đố buổi sáng: bài hát", "Natu đố: 'Ta hát cho ai cũng ngủ, rồi giận vì không ai nghe hết bài. Ta là ai?'", "Natu: Đúng rồi!"),
 (5, "cau_do", "dap=Ditto", "Câu đố buổi sáng: biến hình", "Natu đố: 'Ta biến thành bất cứ ai, mặt cười tí hin. Ta là ai?'", "Natu: Chính xác!"),
 (5, "cau_do", "dap=Slowpoke", "Câu đố buổi sáng: chậm chạp", "Natu đố: 'Ta chậm nhất Johto, đuôi ta câu cá, đau mất năm giây mới biết. Ta là ai?'", "Natu: Hay lắm!"),
 (5, "cau_do", "dap=Gyarados", "Câu đố buổi sáng: hoá rồng", "Natu đố: 'Từ con cá vô dụng nhất, ta thành con rồng biển giận dữ nhất. Ta là ai?'", "Natu: Đúng!"),
 (5, "cau_do", "dap=Eevee", "Câu đố buổi sáng: tám ngả", "Natu đố: 'Ta có tám con đường tiến hoá. Ta là ai?'", "Natu: Đúng! Tám ngả đường!"),
 (5, "cau_do", "dap=Charmander", "Câu đố buổi sáng: đuôi lửa", "Natu đố: 'Đuôi ta có ngọn lửa, lửa tắt là ta yếu đi. Ta là ai?'", "Natu: Đúng rồi!"),
 (5, "cau_do", "dap=Psyduck", "Câu đố buổi sáng: đau đầu", "Natu đố: 'Ta luôn ôm đầu, đau càng nhiều niệm lực càng mạnh. Ta là ai?'", "Natu: Giỏi quá!"),
 (8, "giup_tan_thu", "so=10", "Giúp người mới", "Có người mới ở làng. Tổ đội với một Pokémon dưới cấp 20, cùng hạ {so} quái.", "Người mới vẫy tay cảm ơn nhóc."),
 (10, "nang_cap_cong", "so=3", "Mài đồ mỗi ngày", "Machamp nhắn: thử nâng một món đồ lên +{so}.", "Khà khà! Sáng loáng!"),
 (10, "phan_ra", "so=1", "Dọn túi đồ", "Túi đầy rồi đấy. Phân rã {so} món đồ thừa lấy đá.", "Gọn túi, thêm đá!"),
 (10, "thang_pvp", "so=1", "Một trận đấu trường", "Đấu trường hôm nay đông. Thắng {so} trận!", "Lửa lên! Thắng đẹp!"),
 (10, "do_vui", "so=1", "Ai là Pokémon?", "Trò 'Ai là Pokémon?' mở lúc trưa và tối. Trả lời đúng {so} câu.", "Trí nhớ tốt đấy!"),
 (10, "cho_ban", "so=1", "Một phiên chợ", "Meowth nhắn: bán {so} món ở chợ hôm nay.", "Meo! Có lời rồi!"),
 (10, "tang_hang", "so=3", "Hầm ngục buổi sáng", "Hầm ngục bí ẩn có hạt giống mới. Vượt {so} tầng.", "Ra khỏi hầm an toàn!"),
 (12, "che_tao", "vat=Vòng cổ sơ cấp", "Đơn hàng của Machamp", "Machamp có đơn: chế tạo một món {vat}.", "Khà khà! Đơn xong!"),
 (12, "ho_tong", "vat=Gói hàng;npc=1", "Hộ tống hàng Kecleon", "Hộ tống {vat} tới tiệm Kecleon. Có kẻ rình cướp.", "Kecleon: Hí hí, nguyên vẹn!"),
 (15, "to_doi", "so=20;so2=2", "Săn theo tổ", "Lập tổ đội từ {so2} Pokémon, cùng hạ {so} quái.", "Đồng đội ăn ý!"),
 (15, "ap_trung", "so=1", "Trứng ở nhà trẻ", "Nhà trẻ báo có trứng. Ấp nở {so} quả.", "Một bé con chào đời!"),
 (15, "dua_pokemon", "", "Đua buổi tối", "Cuộc đua 19:00 thiếu người. Tham gia và về đích nhé.", "Về đích! Khán giả reo hò."),
 (20, "kham_ngoc", "so=1", "Khảm một viên ngọc", "Sableye khuyến mãi ngọc. Khảm {so} viên.", "Hì hì, lấp lánh!"),
 (20, "boss_the_gioi", "so=1", "Góp sức đánh boss", "Boss thế giới xuất hiện hôm nay? Góp sức, dù chỉ một chút.", "Mọi người cảm ơn nhóc đã có mặt."),
 (20, "tham_gia_gym", "", "Góp mặt chiếm Gym", "Đội của nhóc có trận chiếm Gym. Tham gia đi.", "Đội ghi nhận công nhóc."),
 (25, "quan_sat", "q=+1;dk=sương mù", "Bóng trong sương", "Sương mù hiếm hoi. Quan sát {quai} trong sương.", "Ảnh mờ ảo như tranh."),
 (25, "quan_sat", "q=+1;dk=nắng gắt", "Trưa nắng gắt", "Quan sát {quai} giữa trưa nắng gắt.", "Chúng lười biếng nằm dưới bóng cây."),
 (30, "ha_alpha", "q=+1", "Truy nã đặc biệt: Alpha {quai}", "TRUY NÃ ĐẶC BIỆT! Alpha {quai}, cực kỳ nguy hiểm.", "Gâu! Cả làng biết ơn cậu."),
 (30, "thu_thach_gio", "q=+1;phut=4", "Thử thách tốc độ", "Hạ {so} {quai} trong {phut} phút. Khó đấy!", "Nhanh như Pidgeot!"),
 (35, "pokedex_ha", "q=+0;so=20", "Nghiên cứu sâu {quai}", "Snorlax muốn biết thêm về {quai}. Hạ {so} con để ghi chép.", "Oáp... hay lắm. Ông ghi lại rồi."),
 (40, "giet_tinh_anh", "q=+4", "Truy nã: kẻ mạnh vượt cấp", "Truy nã: {quai} vượt cấp. Hạ {so} con.", "Gâu! Đáng nể!"),
 (40, "thang_pvp", "so=2", "Hai trận đấu trường", "Thắng {so} trận đấu trường hôm nay.", "Lửa lên! Hai trận, hai chiến thắng!"),
]

# 12 ten mau tuan (T-): engine hien co dem ha quai +-5 cap. Ten + loi giao khac nhau.
TUAN_TEN = [
 ("Đội tuần tra vùng", "Cả tuần, tuần tra các tuyến: hạ {so} quái ngang sức."),
 ("Cúp huấn luyện tuần", "Cúp huấn luyện mở! Ai hạ đủ {so} quái ngang sức sẽ có tên trên bảng."),
 ("Mùa thu hoạch", "Lũ quái phá mùa màng khắp vùng. Hạ {so} con trong tuần."),
 ("Bảo vệ ba vùng", "Kanto, Johto đều cần người. Hạ {so} quái ngang sức."),
 ("Đợt săn lớn", "Đợt săn lớn của tuần: {so} quái ngang sức."),
 ("Thám hiểm hang động", "Hang động đông quái. Dọn {so} con trong tuần."),
 ("Chuyến hàng lớn", "Đường hàng phải thông suốt cả tuần: hạ {so} quái."),
 ("Thử thách bền bỉ", "Không cần nhanh, cần bền: {so} quái ngang sức trong tuần."),
 ("Tuần lễ Blaziken", "Lửa lên nào! Tuần này tôi muốn thấy {so} trận thắng ngang sức."),
 ("Bảo vệ vườn Berry", "Mùa quả chín, lũ quái kéo về. Hạ {so} con."),
 ("Đêm dài", "Quái hoạt động mạnh về đêm tuần này. Hạ {so} con."),
 ("Lời hứa của Xatu", "Xatu thấy trước một tuần bận rộn: {so} quái ngang sức."),
]
# bac -> cap toi thieu (NhiemVuLauDai.bacCap: 1-29 / 30-54 / 55-79 / 80+)
BAC_TUAN = [(1, 10), (2, 30), (3, 55), (4, 80)]

# Chuoi tuan theo vung: (ten, vung, noi, cap, [3 buoc])
CHUOI_VUNG = [
 ("Đồng cỏ Kanto", "Kanto", "Pallet", 10, [
  ("pokedex_vung", "so=5;noi=Pallet", "Kiểm kê đồng cỏ", "Ghi {so} loài sống ở đồng cỏ Pallet.", "Đồng cỏ khoẻ mạnh."),
  ("ha_alpha", "q=+0", "Đầu đàn đồng cỏ", "Hạ Alpha {quai} đang chiếm đồng cỏ.", "Đồng cỏ yên bình."),
  ("noi_chuyen", "npc=5", "Báo cáo trưởng làng", "Báo cáo cho Xatu.", "Xatu: ...Tuần sau gió sẽ đổi. Cảm ơn cháu.")]),
 ("Rừng và núi Kanto", "Kanto", "Núi Mặt Trăng", 15, [
  ("den_noi", "map=Núi Mặt Trăng", "Lên núi", "Lên Núi Mặt Trăng kiểm tra.", "Hang yên tĩnh."),
  ("giet", "q=+1", "Dọn hang", "Hạ {so} {quai} trong hang.", "Bầy Clefairy vẫy tay."),
  ("thu_thap", "vat=Nấm phát quang", "Đèn cho hang", "Lấy {so} {vat} từ {quai} làm đèn.", "Hang sáng dịu.")]),
 ("Bến cảng Vermilion", "Kanto", "Vermilion", 22, [
  ("cau_ca", "vat=Vảy cá vàng;so=5", "Mẻ cá tuần", "Câu {so} con có {vat}.", "Chợ cá đầy ắp."),
  ("giao_hang", "npc=9;vat=Gói hàng", "Hàng về kho", "Giao {vat} cho Kangaskhan.", "Kangaskhan: Đủ hàng cho cả tuần."),
  ("giet", "q=+1", "Canh bến", "Hạ {so} {quai} quanh bến.", "Bến an toàn.")]),
 ("Sương mù Lavender", "Kanto", "Lavender", 30, [
  ("quan_sat", "q=+0;dk=sương mù", "Hồn ma trong sương", "Quan sát {quai} khi sương mù.", "Hồn ma bình yên."),
  ("giet", "q=+1", "Yên giấc", "Hạ {so} {quai} quấy nhiễu tháp.", "Tháp yên tĩnh."),
  ("dung_toa_do", "map=Lavender;diem=0", "Thắp nến", "Thắp nến ở đỉnh tháp.", "Ánh nến lung linh.")]),
 ("Thành phố lớn Kanto", "Kanto", "Celadon", 35, [
  ("cho_ban", "so=3", "Phiên chợ tuần", "Bán {so} món ở chợ.", "Chợ đông vui."),
  ("lam_vuon", "vat=Hoa sương;so=3", "Vườn hoa tuần", "Trồng {so} {vat}.", "Vườn rực rỡ."),
  ("thang_pvp", "so=2", "Đấu trường thành phố", "Thắng {so} trận.", "Khán giả reo hò.")]),
 ("Đảo lửa", "Kanto", "Cinnabar", 43, [
  ("thu_thap", "vat=Than hồng", "Than hồng", "Lấy {so} {vat} từ {quai}.", "Lò rèn đủ than."),
  ("ha_alpha", "q=+0", "Alpha núi lửa", "Hạ Alpha {quai}.", "Núi lửa yên."),
  ("noi_chuyen", "npc=3", "Báo Bronzong", "Báo cho Bronzong.", "Boong... núi ngủ ngon.")]),
 ("Gió Johto", "Johto", "New Bark", 49, [
  ("dung_toa_do", "map=New Bark;diem=0", "Đo gió", "Đứng trên đồi đo gió.", "Gió thổi đều."),
  ("pokedex_vung", "so=5;noi=New Bark", "Kiểm kê New Bark", "Ghi {so} loài.", "Sổ đầy thêm."),
  ("giet", "q=+1", "Bảo vệ cánh đồng", "Hạ {so} {quai}.", "Cánh đồng yên.")]),
 ("Rừng Ilex", "Johto", "Rừng Ilex", 58, [
  ("lam_vuon", "vat=Hạt giống Berry;so=3", "Gieo hạt rừng", "Gieo {so} {vat}.", "Rừng xanh thêm."),
  ("giet", "q=+1", "Giữ rừng", "Hạ {so} {quai}.", "Rừng yên."),
  ("quan_sat", "q=+0;dk=bình minh", "Bình minh trong rừng", "Quan sát {quai} lúc bình minh.", "Một bóng xanh thoáng qua...")]),
 ("Goldenrod sầm uất", "Johto", "Goldenrod", 60, [
  ("cho_ban", "so=4", "Chợ lớn", "Bán {so} món.", "Meo! Lời to."),
  ("dua_pokemon", "", "Đường đua", "Về đích trong cuộc đua.", "Về đích!"),
  ("do_vui", "so=2", "Đố vui", "Đúng {so} câu.", "Thông minh!")]),
 ("Biển Olivine", "Johto", "Olivine", 66, [
  ("cau_ca", "vat=Ngọc trai;so=3", "Ngọc biển", "Câu {so} {vat}.", "Ngọc lấp lánh."),
  ("giet", "q=+1", "Giữ hải đăng", "Hạ {so} {quai}.", "Hải đăng sáng."),
  ("ho_tong", "vat=Gói hàng;npc=2", "Hàng cho xưởng", "Hộ tống {vat} tới Machamp.", "Machamp: Khà khà, đủ hàng!")]),
 ("Hồ và núi Johto", "Johto", "Hồ Phẫn Nộ", 71, [
  ("ha_alpha", "q=+0", "Alpha bên hồ", "Hạ Alpha {quai}.", "Hồ yên."),
  ("cau_ca", "vat=Vảy cá vàng;so=4", "Cá hồ", "Câu {so} con.", "Cá béo."),
  ("giet", "q=+1", "Quanh hồ", "Hạ {so} {quai}.", "Bờ hồ an toàn.")]),
 ("Đất rồng", "Johto", "Blackthorn", 77, [
  ("thu_thap", "vat=Vảy rồng nhỏ", "Vảy rồng", "Lấy {so} {vat} từ {quai}.", "Già làng hài lòng."),
  ("giet_tinh_anh", "q=+3", "Thử thách rồng", "Hạ {so} {quai} mạnh.", "Rồng gật đầu."),
  ("noi_chuyen", "npc=5", "Báo già làng", "Báo cho Xatu.", "Xatu: ...Đất rồng yên ổn.")]),
 ("Đỉnh cao Johto", "Johto", "Núi Silver", 88, [
  ("den_noi", "map=Núi Silver", "Leo núi", "Lên Núi Silver.", "Tuyết rơi nhẹ."),
  ("quan_sat", "q=+0;dk=trời tuyết", "Tuyết rơi", "Quan sát {quai} khi tuyết rơi.", "Dấu chân trong tuyết."),
  ("giet_tinh_anh", "q=+3", "Kẻ mạnh trên núi", "Hạ {so} {quai} mạnh.", "Núi yên.")]),
]

TO_DOI = [
 ("Đội săn Gyarados", 20), ("Đội trinh sát Hang Tối", 30), ("Đội hái Berry", 10), ("Đội bảo vệ Pallet", 12),
 ("Đột kích Tổ Zapdos", 50), ("Đêm Hang Darkrai", 60), ("Bão Rayquaza", 75), ("Liên minh ba vùng", 85),
 ("Đội giải cứu Safari", 39), ("Đội hộ tống tàu", 27), ("Đội leo núi Silver", 88), ("Đội lặn Hồ Phẫn Nộ", 71),
 ("Đội đêm Lavender", 32), ("Đội rừng Ilex", 58), ("Đội đường Victory", 84), ("Đội Cao nguyên", 92),
]

DOI = [
 ("Quỹ đội tuần", "doi_dong_gop", 15), ("Chiếm Gym cùng nhau", "tham_gia_gym", 20),
 ("Đội săn cuối tuần", "doi_dong_gop", 25), ("Lãnh địa đội", "doi_dong_gop", 35),
 ("Đội trong bão", "doi_dong_gop", 45), ("Cờ của đội", "tham_gia_gym", 50),
 ("Đội mạnh nhất vùng", "doi_dong_gop", 60), ("Đội và rồng", "doi_dong_gop", 70),
 ("Bảo vệ đội", "tham_gia_gym", 75), ("Đội đỉnh núi", "doi_dong_gop", 85),
 ("Đội ba vùng", "doi_dong_gop", 90), ("Huyền thoại của đội", "tham_gia_gym", 95),
]

SU_PHAM = ["Dẫn đường người mới", "Kèm cặp tới cấp 20", "Kèm cặp tới cấp 40", "Cùng nhau tiến hoá",
           "Người thầy tận tâm", "Chia sẻ bí quyết", "Bài học đầu tiên", "Học trò thành tài"]

HAM_NGUC = ["Hầm ngục bí ẩn: tầng 1-5", "Hầm ngục bí ẩn: tầng 6-10", "Hầm ngục bí ẩn: tầng 11-20",
            "Hầm ngục bí ẩn: hạt giống ngày", "Hầm ngục bí ẩn: xếp hạng tuần", "Hầm ngục bí ẩn: tầng sâu"]

QUAY_LAI = [
 ("Chào mừng trở lại", "noi_chuyen", "...Ta biết cháu sẽ quay lại. Ta đã thấy trước.", "Nghỉ ngơi đủ rồi. Thế giới vẫn chờ cháu."),
 ("Ôn lại chiêu thức", "giet_lech_cap", "Lửa lên nào! Lâu không đánh, tay chân cứng hết rồi. Hạ {so} quái ngang sức.", "Lửa vẫn còn đó!"),
 ("Bắt kịp bạn bè", "giet_lech_cap", "Bạn bè nhóc đi xa rồi. Đuổi kịp đi: {so} quái.", "Gần kịp rồi!"),
 ("Kể chuyện đã qua", "noi_chuyen", "Oáp... cháu vắng mặt, ông kể cháu nghe chuyện làng.", "Thế đấy. Giờ cháu là người viết tiếp."),
 ("Túi đồ bám bụi", "nang_cap_cong", "Khà khà! Đồ của cháu bám bụi. Nâng một món lên +{so} cho sáng lại.", "Sáng như mới!"),
 ("Một ngày trọn vẹn", "dang_nhap", "Ôi con yêu, cô chỉ mong con ghé thăm {so} ngày liền.", "Con ở lại là cô vui rồi."),
]

# loai su kien -> 3 buoc (kieu, tham so, ten, mo, xong). Cho {ten} = ten su kien, {vat} = vat pham su kien,
# {thuong} = phan thuong doi duoc, {mo_rieng} = cau mo rieng cua tuan.
SU_KIEN = {
 "le_vn": [
  ("noi_chuyen", "npc=5", "{ten}: lời mời", "{mo_rieng}|Cả làng đang chuẩn bị {ten}. Cháu tham gia nhé.", "Vui lên nào!"),
  ("su_kien_vat_pham", "so=20", "{ten}: chuẩn bị", "Thu {so} {vat} từ quái ngang cấp trong tuần lễ.", "Đủ đồ cho lễ hội!"),
  ("noi_chuyen", "npc=22", "{ten}: đổi quà", "Mang {vat} tới Meowth đổi {thuong}.", "Meo! Quà lễ đây, chúc mừng!")],
 "tuan_he": [
  ("noi_chuyen", "npc=21", "{ten}: khai mạc", "{mo_rieng}|Tuần này một hệ áp đảo. Nhóc tận dụng đi.", "Lửa lên nào!"),
  ("giet_lech_cap", "", "{ten}: săn theo hệ", "Pokémon hệ của tuần xuất hiện dày. Hạ {so} quái ngang sức.", "Săn tốt!"),
  ("su_kien_vat_pham", "so=10", "{ten}: tinh thể", "Thu {so} {vat}, đổi {thuong}.", "Lấp lánh!")],
 "cong_dong": [
  ("noi_chuyen", "npc=8", "{ten}: tập hợp", "{mo_rieng}|Oáp... ngày cộng đồng! Ai cũng ra đồng.", "Đi cùng mọi người nhé."),
  ("quan_sat", "q=+0;dk=giờ cộng đồng", "{ten}: ba giờ vàng", "Trong 3 giờ cộng đồng, quan sát Pokémon nổi bật.", "Ảnh đẹp quá!"),
  ("su_kien_vat_pham", "so=5", "{ten}: huy hiệu", "Thu {so} {vat}.", "Kỷ niệm ngày cộng đồng!")],
 "pokemon": [
  ("noi_chuyen", "npc=8", "{ten}: mở màn", "{mo_rieng}|Oáp... tuần đặc biệt cho nhà nghiên cứu.", "Ghi chép thật nhiều nhé."),
  ("pokedex_vung", "so=5", "{ten}: ghi chép", "Ghi {so} loài mới vào Pokédex trong tuần.", "Sổ dày lên!"),
  ("su_kien_vat_pham", "so=10", "{ten}: tem nghiên cứu", "Thu {so} {vat}, đổi {thuong}.", "Nhà nghiên cứu chăm chỉ!")],
 "dot_kich": [
  ("noi_chuyen", "npc=21", "{ten}: báo động", "{mo_rieng}|Huyền thoại xuất hiện! Tập hợp!", "Lửa lên nào!"),
  ("boss_the_gioi", "so=1", "{ten}: đột kích", "Cùng mọi người đánh huyền thoại (2 lần/ngày).", "Huyền thoại đã lui!"),
  ("su_kien_vat_pham", "so=3", "{ten}: chiến lợi phẩm", "Thu {so} {vat}.", "Đáng nhớ!")],
 "pvp": [
  ("noi_chuyen", "npc=21", "{ten}: tổng kết mùa", "{mo_rieng}|Mùa giải sắp chốt. Dốc sức lần cuối!", "Lửa lên!"),
  ("thang_pvp", "so=3", "{ten}: nước rút", "Thắng {so} trận đấu trường trong tuần.", "Bậc mùa giải tăng!"),
  ("su_kien_vat_pham", "so=1", "{ten}: huy chương", "Nhận {so} {vat} khi chốt mùa.", "Huy chương mùa giải!")],
 "chien_vung": [
  ("noi_chuyen", "npc=25", "{ten}: tập hợp", "{mo_rieng}|Hự! Thứ Bảy 20h, ba vùng giành cờ. Chọn phe đi chiến hữu!", "Vì vùng của ta!"),
  ("tham_gia_gym", "", "{ten}: giành cờ", "Tham gia trận giành cờ cùng Đội.", "Cờ tung bay!"),
  ("su_kien_vat_pham", "so=5", "{ten}: cờ vùng", "Thu {so} {vat}.", "Chiến công ghi nhận!")],
 "mua": [
  ("noi_chuyen", "npc=22", "{ten}: khai hội", "{mo_rieng}|Meo! Mùa lễ hội! Bán, mua, chơi!", "Vui nào!"),
  ("su_kien_vat_pham", "so=15", "{ten}: sưu tầm", "Thu {so} {vat} trong tuần.", "Đủ rồi!"),
  ("noi_chuyen", "npc=22", "{ten}: đổi quà", "Mang {vat} đổi {thuong}.", "Meo! Quà đây!")],
}
# Cau mo rieng 52 tuan (theo thu tu su_kien_nam.csv).
SU_KIEN_MO = [
 "Tuyết đầu năm phủ trắng Pallet, cây thông treo đầy kẹo.",
 "Gió lạnh thổi về, Pokémon hệ Băng kéo nhau xuống núi.",
 "Đồng cỏ rực lửa: dòng Charmander tụ hội cả buổi chiều.",
 "Nồi bánh chưng bốc khói, cả làng ngồi canh lửa.",
 "Cây lì xì Sudowoodo đung đưa, Litleo múa lân khắp phố.",
 "Pháo hoa Tết rực trời, ai cũng diện áo mới.",
 "Tin đồn khắp nơi: Pokémon màu lạ xuất hiện nhiều hơn tuần này.",
 "Luvdisc bơi thành hình trái tim dưới hồ Cerulean.",
 "Mây sấm kéo về. Zapdos đang bay qua bầu trời Kanto!",
 "Hoa nở khắp làng: tuần lễ tri ân những người phụ nữ.",
 "Mùa xuân về, Pokémon hệ Cỏ trổ hoa khắp đồng.",
 "Bulbasaur kéo về đồng cỏ, nụ hoa trên lưng hé nở.",
 "Mùa giải PvP đầu tiên sắp khép lại. Ai sẽ lên bậc Master?",
 "Lễ hội Pokédex: nhà nghiên cứu khắp nơi đổ về Pewter.",
 "Ngày Giỗ Tổ, làng làm bánh giày dâng lễ.",
 "Ba vùng treo cờ. Thứ Bảy này, ai sẽ giữ cờ đến cuối?",
 "Đường đua Ponyta mở cửa, cả nước diễu hành.",
 "Nắng gắt đầu hè, Pokémon hệ Lửa hoạt động mạnh.",
 "Squirtle tụ về bờ suối, phun nước tung toé.",
 "Đất rung, lửa bùng: Entei đang chạy qua Johto!",
 "Bùng phát! Mỗi tuyến đường có một loài xuất hiện dày đặc.",
 "Ngày của các bé: người lớn dẫn người mới đi chơi.",
 "Biển Vermilion mở hội, kem dừa bán khắp bờ cát.",
 "Hè vẫn còn dài, lâu đài cát mọc khắp Vermilion.",
 "Pichu tí hon kéo nhau ra đồng tập phóng điện.",
 "Mùa giải PvP thứ hai đi vào hồi kết.",
 "Mưa mùa hạ, Pokémon hệ Nước vui đùa khắp nơi.",
 "Ác mộng trở lại trong đêm: Darkrai xuất hiện!",
 "Hầm ngục mùa hè: tầng sâu nhất mát lạnh như suối.",
 "Thế chiến vùng mùa hè, cờ ba vùng tung bay.",
 "Eevee kéo về đồng cỏ, đá tiến hoá rơi nhiều hơn.",
 "Mùa Vu Lan, hoa hồng vàng cài trên ngực áo.",
 "Sấm chớp mùa thu, Pokémon hệ Điện tích đầy năng lượng.",
 "Lời đồn lan nhanh: Pokémon màu lạ lại xuất hiện!",
 "Đèn lồng giăng khắp phố mừng Quốc khánh.",
 "Bầu trời xé toạc: Rayquaza lao xuống từ tầng mây!",
 "Trăng tròn Trung Thu, Clefairy múa quanh mâm bánh.",
 "Đêm hội trăng rằm, rước đèn quanh làng.",
 "Mùa giải PvP thứ ba khép lại dưới trăng thu.",
 "Dòng Charmander lại tụ hội, lửa đuôi sáng rực đồng cỏ.",
 "Tuần lễ của những người phụ nữ Việt Nam, hoa bày khắp chợ.",
 "Pokémon hệ Siêu linh khiến đồ vật bay lơ lửng khắp nơi.",
 "Bí ngô cười khúc khích, Gengar lẻn khắp phố đêm.",
 "Halloween vẫn chưa hết: ai dám vào Tháp Pokémon lúc nửa đêm?",
 "Lễ hội Pokédex lần hai, sổ ai dày nhất?",
 "Tuần tri ân thầy cô: Blaziken được học trò tặng hoa.",
 "Giảm giá đồ trang trí, Meowth vui nhất làng.",
 "Đất nứt, biển dâng: Groudon và Kyogre cùng thức giấc!",
 "Thế chiến vùng mùa đông, tuyết phủ chiến trường.",
 "Bulbasaur lại về, mầm xanh giữa trời đông.",
 "Delibird phát quà, cây thông Abomasnow lấp lánh đèn.",
 "Năm cũ khép lại. Ai là người đáng nhớ nhất năm?",
]

# Chuoi huyen thoai: (ma, ten huyen thoai, cap, npc, token (ten vat pham trong noi_dung_item_moi),
#   id quai boss (hoac 0), noi, danh hieu, [7 buoc]). Moi buoc mo sau buoc truoc 7 ngay.
HUYEN_THOAI = [
 ("ZAP", "Zapdos", 40, 8, "Lông vũ Zapdos", 82, "Tổ Zapdos", "Người nghe tiếng sấm", [
  ("noi_chuyen", "", "Tiếng sấm không mây", "Oáp... trời quang mà có sấm. Ông nghe kể Zapdos đang tìm tổ mới.", "Đi tìm hiểu đi cháu."),
  ("quan_sat", "q=+0;dk=trời mưa", "Mây sấm kéo về", "Quan sát {quai} khi mưa giông, chúng nhạy với sấm.", "Sét đánh về một hướng: phía Tổ Zapdos."),
  ("tim_vat_an", "map=Tổ Zapdos;vat=Lông vũ tím", "Chiếc lông cháy xém", "Tìm dấu vết ở Tổ Zapdos.", "Một chiếc lông vàng cháy xém. Zapdos bị thương?"),
  ("thu_thap", "vat=Lọ thuốc", "Thuốc cho chim sấm", "Thu {so} {vat} từ {quai} để làm thuốc.", "Thuốc đã sẵn sàng."),
  ("dung_toa_do", "map=Tổ Zapdos;diem=0", "Đặt thuốc ở tổ", "Đặt thuốc ở mỏm đá cao nhất.", "Đêm đó, sấm rền một tiếng như lời cảm ơn."),
  ("boss_the_gioi", "q=#82;so=1", "Zapdos giáng thế", "Zapdos xuất hiện trong tuần đột kích. Cùng mọi người thử sức với nó.", "Zapdos gật đầu, để lại một chiếc lông vàng."),
  ("noi_chuyen", "", "Người nghe tiếng sấm", "Kể cho ông nghe.", "Oáp... Zapdos tin cháu rồi. Từ nay sấm sẽ không làm cháu sợ.")]),
 ("ENT", "Entei", 60, 21, "Nanh lửa Entei", 138, "Ecruteak", "Người theo vệt lửa", [
  ("noi_chuyen", "", "Vệt lửa trên đường", "Lửa lên nào! Có vệt lửa chạy thẳng qua Ecruteak đêm qua.", "Theo dấu nó!"),
  ("tim_vat_an", "map=Ecruteak;vat=Than hồng", "Than còn ấm", "Tìm {vat} trên vệt lửa.", "Than nóng như mới."),
  ("quan_sat", "q=+0;dk=nắng gắt", "Bóng trong nắng", "Quan sát {quai} giữa trưa nắng gắt, Entei thích nắng.", "Một bóng lớn lướt qua đồi."),
  ("giet", "q=+1", "Vết cháy lan", "Lửa lan làm {quai} hoảng. Hạ {so} con.", "Lửa tắt dần."),
  ("cau_do", "dap=Ho-Oh", "Truyền thuyết Tháp Cháy", "Bronzong hỏi: 'Ai đã hồi sinh ba huyền thoại từ tro tàn Tháp Cháy?'", "Đúng. Ho-Oh, chim cầu vồng."),
  ("boss_the_gioi", "q=#138;so=1", "Entei hiện thân", "Đột kích Entei! Cùng mọi người đối mặt.", "Entei gầm vang rồi chạy về Núi Silver."),
  ("noi_chuyen", "", "Lửa trong tim", "Kể lại với Blaziken.", "Lửa lên nào... Entei công nhận nhóc. Hiếm lắm đấy.")]),
 ("DAR", "Darkrai", 70, 5, "Mảnh bóng tối", 116, "Hang Darkrai", "Người đi qua ác mộng", [
  ("noi_chuyen", "", "Giấc mơ lặp lại", "...Đêm nào ta cũng mơ một giấc. Mắt xanh trong bóng tối.", "Ta e là nó đang gọi."),
  ("quan_sat", "q=+0;dk=ban đêm", "Đêm trăng non", "Quan sát {quai} trong đêm tối nhất.", "Mọi Pokémon đều ngủ say. Trừ một bóng đen."),
  ("den_noi", "map=Hang Darkrai", "Hang của giấc mơ", "Đi vào Hang Darkrai.", "Hang lạnh và im lặng."),
  ("giet", "q=+1", "Ác mộng hiện hình", "Ác mộng hiện thành {quai}. Hạ {so} con.", "Bóng tối lùi lại."),
  ("tim_vat_an", "map=Hang Darkrai;vat=Lông vũ trăng", "Ánh trăng trong hang", "Tìm {vat}, thứ duy nhất xua được ác mộng.", "Ánh trăng toả ra, ấm áp."),
  ("boss_the_gioi", "q=#116;so=1", "Đối mặt Darkrai", "Đột kích Darkrai! Cùng mọi người.", "Darkrai tan vào bóng tối, để lại một mảnh đen."),
  ("noi_chuyen", "", "Sau cơn mơ", "Kể cho Xatu.", "...Đêm nay ta ngủ không mơ. Cảm ơn cháu.")]),
 ("RAY", "Rayquaza", 80, 21, "Vảy Rayquaza", 120, "Núi Silver", "Người chạm tới bầu trời", [
  ("noi_chuyen", "", "Vệt xanh trên trời", "Lửa lên nào! Có vệt xanh lục xé toạc mây. Không phải máy bay đâu!", "Đi xem!"),
  ("dung_toa_do", "map=Núi Silver;diem=4", "Đỉnh cao nhất", "Lên đỉnh Núi Silver, nơi gần trời nhất.", "Gió mạnh. Một tiếng gầm xa xăm."),
  ("quan_sat", "q=+0;dk=bão cát", "Gió xoáy", "Quan sát {quai} trong gió lớn.", "Gió xoáy theo hình con rồng."),
  ("thu_thap", "vat=Vảy rồng nhỏ", "Vảy rơi từ trời", "Thu {so} {vat} từ {quai}.", "Vảy lấp lánh xanh lục."),
  ("bo_trang_bi", "so=7;bac=tinh anh", "Sẵn sàng cho trời cao", "Mặc {so} món bậc {bac}.", "Bạn đã sẵn sàng."),
  ("boss_the_gioi", "q=#120;so=1", "Rayquaza giáng xuống", "Đột kích Rayquaza!", "Rayquaza vút lên mây."),
  ("noi_chuyen", "", "Bầu trời", "Kể với Blaziken.", "Nhóc chạm tới trời rồi đấy.")]),
 ("GRK", "Groudon và Kyogre", 85, 10, "Ngọc đỏ Groudon", 130, "Hồ Phẫn Nộ", "Người giữ cân bằng đất biển", [
  ("noi_chuyen", "", "Biển dâng, đất nứt", "La la~ biển dâng mà đất lại khô. Lạ lắm.", "Đi xem cùng mình."),
  ("quan_sat", "q=+0;dk=trời mưa", "Mưa không dứt", "Quan sát {quai} trong mưa kéo dài.", "Mưa nặng hạt bất thường."),
  ("quan_sat", "q=+0;dk=nắng gắt", "Nắng như đổ lửa", "Quan sát {quai} khi nắng gắt.", "Đất nứt nẻ."),
  ("cau_ca", "vat=Ngọc trai;so=3", "Ngọc lam dưới biển", "Câu {so} {vat}.", "Một viên sáng lam như mắt Kyogre."),
  ("dao_dat", "map=Khu mỏ;so=4", "Ngọc đỏ dưới đất", "Đào {so} lần ở khu mỏ.", "Một viên đỏ rực như mắt Groudon."),
  ("boss_the_gioi", "q=#130;so=1", "Hai gã khổng lồ", "Đột kích Groudon và Kyogre!", "Hai gã khổng lồ chìm vào giấc ngủ."),
  ("noi_chuyen", "", "Cân bằng", "Kể với Lapras.", "La la~ Biển và đất lại yên.")]),
 ("ART", "Articuno", 92, 23, "Lông vũ Articuno", 0, "Núi Silver", "Người giữ hơi thở băng", [
  ("noi_chuyen", "", "Tuyết giữa mùa hè", "Khụ khụ... tuyết rơi giữa hè ở Núi Silver. Hồi trẻ lão thấy một lần. Là Articuno.", "Đi đi, lão già rồi."),
  ("quan_sat", "q=+0;dk=trời tuyết", "Bông tuyết xanh", "Quan sát {quai} khi tuyết rơi.", "Bông tuyết xanh nhạt, lạnh buốt."),
  ("den_noi", "map=Núi Silver", "Đỉnh tuyết", "Lên Núi Silver.", "Một chiếc lông xanh băng nằm trên tuyết."),
  ("cau_do", "dap=Articuno", "Bài đồng dao", "'Chim băng ba anh em, anh cả mang tuyết, cánh xanh như hồ. Là ai?'", "Đúng. Articuno."),
  ("giet", "q=+1", "Bão tuyết", "Hạ {so} {quai} trong bão tuyết.", "Bão dịu dần."),
  ("tim_vat_an", "map=Núi Silver;vat=Mảnh băng vĩnh cửu", "Băng vĩnh cửu", "Tìm {vat}.", "Băng không tan trong tay bạn."),
  ("noi_chuyen", "", "Lời của băng", "Kể với lão Torkoal.", "Khụ khụ... cháu thấy nó rồi à. Lão mừng.")]),
 ("MOL", "Moltres", 93, 21, "Mảnh tinh hoa", 0, "Cinnabar", "Người mang ngọn lửa bất diệt", [
  ("noi_chuyen", "", "Lửa trên núi", "Lửa lên nào! Núi lửa Cinnabar lại sáng, nhưng không phun. Moltres đấy!", "Đi!"),
  ("den_noi", "map=Cinnabar", "Về đảo lửa", "Đến Cinnabar.", "Một vệt lửa vẽ vòng trên trời."),
  ("quan_sat", "q=+0;dk=hoàng hôn", "Hoàng hôn đỏ", "Quan sát {quai} lúc hoàng hôn.", "Trời đỏ như lửa."),
  ("thu_thap", "vat=Than hồng", "Lông lửa", "Thu {so} {vat} từ {quai}.", "Than vẫn cháy dù không có gió."),
  ("thu_thach_gio", "q=+1;phut=6", "Vũ điệu lửa", "Hạ {so} {quai} trong {phut} phút.", "Moltres bay vòng trên đầu bạn."),
  ("dung_toa_do", "map=Cinnabar;diem=0", "Miệng núi", "Đứng ở miệng núi lửa.", "Một chiếc lông lửa rơi xuống."),
  ("noi_chuyen", "", "Lửa bất diệt", "Kể với Blaziken.", "Lửa của nhóc... sẽ không bao giờ tắt.")]),
 ("RAI", "Raikou", 94, 3, "Lông tơ Raikou", 0, "Mahogany", "Người đuổi kịp tia chớp", [
  ("noi_chuyen", "", "Tia chớp chạy trên đất", "Boong... có tia chớp chạy trên mặt đất. Raikou.", "Đuổi theo."),
  ("quan_sat", "q=+0;dk=trời mưa", "Mưa giông Johto", "Quan sát {quai} khi mưa giông.", "Vệt chớp chạy về Mahogany."),
  ("den_noi", "map=Mahogany", "Tới Mahogany", "Đến Mahogany.", "Cây cháy sém hình móng vuốt."),
  ("thu_thach_gio", "q=+1;phut=5", "Đuổi tia chớp", "Hạ {so} {quai} trong {phut} phút.", "Bạn thấy bóng nó!"),
  ("dua_pokemon", "", "Chạy như sấm", "Về đích trong một cuộc đua.", "Đủ nhanh để Raikou để ý."),
  ("tim_vat_an", "map=Mahogany;vat=Lông vũ tím", "Lông tơ vàng", "Tìm dấu vết Raikou để lại.", "Một nhúm lông tơ vàng tích điện."),
  ("noi_chuyen", "", "Tốc độ", "Kể với Bronzong.", "Boong... cậu nhanh hơn ta. Nhiều.")]),
 ("SUI", "Suicune", 95, 10, "Pha lê Suicune", 0, "Hồ Phẫn Nộ", "Người làm trong dòng nước", [
  ("noi_chuyen", "", "Nước tự trong", "La la~ hồ đục bỗng trong veo trong một đêm. Suicune đã qua đây.", "Theo dấu nước trong."),
  ("cau_ca", "vat=Vảy cá vàng;so=3", "Cá hồ trong", "Câu {so} con có {vat}.", "Cá khỏe mạnh."),
  ("quan_sat", "q=+0;dk=sương mù", "Gió bắc", "Quan sát {quai} khi sương mù.", "Bóng xanh lướt trên mặt nước."),
  ("lam_vuon", "vat=Hoa sương;so=3", "Hoa bên hồ", "Trồng {so} {vat}.", "Hoa nở trắng bờ hồ."),
  ("dung_toa_do", "map=Hồ Phẫn Nộ;diem=0", "Mặt hồ", "Đứng bên mặt hồ lúc bình minh.", "Suicune nhìn bạn từ bờ bên kia."),
  ("tim_vat_an", "map=Hồ Phẫn Nộ;vat=Ngọc trai", "Giọt pha lê", "Tìm thứ Suicune để lại.", "Một giọt pha lê lạnh."),
  ("noi_chuyen", "", "Nước trong", "Kể với Lapras.", "La la~ hiếm ai được Suicune nhìn tận mắt.")]),
 ("LUG", "Lugia", 96, 10, "Lông bạc Lugia", 0, "Olivine", "Người nghe bài ca biển sâu", [
  ("noi_chuyen", "", "Bài ca dưới biển", "La la~ có tiếng hát từ đáy biển. Không phải mình.", "Lugia."),
  ("cau_ca", "vat=Ngọc trai;so=3", "Ngọc biển sâu", "Câu {so} {vat}.", "Ngọc ngân nga."),
  ("den_noi", "map=Olivine", "Hải đăng", "Đến Olivine.", "Ampharos chỉ ra biển."),
  ("quan_sat", "q=+0;dk=trời mưa", "Bão biển", "Quan sát {quai} trong bão.", "Một bóng bạc dưới sóng."),
  ("giet", "q=+1", "Biển động", "Hạ {so} {quai}.", "Sóng lặng."),
  ("tim_vat_an", "map=Olivine;vat=Vỏ sò", "Vỏ sò hát", "Tìm {vat} biết hát.", "Vỏ sò hát giai điệu của Lugia."),
  ("noi_chuyen", "", "Bài ca", "Kể với Lapras.", "La la~ mình sẽ học bài ca đó.")]),
 ("HOO", "Ho-Oh", 97, 5, "Mảnh tinh hoa", 0, "Ecruteak", "Người thấy cầu vồng bảy sắc", [
  ("noi_chuyen", "", "Cầu vồng không mưa", "...Cầu vồng hiện trên Tháp Chuông dù trời không mưa.", "Ho-Oh."),
  ("dung_toa_do", "map=Ecruteak;diem=0", "Bậc thang thứ chín", "Đứng trên bậc thang thứ chín Tháp Chuông.", "Chuông rung."),
  ("quan_sat", "q=+0;dk=bình minh", "Ánh cầu vồng", "Quan sát {quai} lúc bình minh.", "Ánh sáng bảy màu."),
  ("thu_thap", "vat=Lá trà Ecruteak", "Trà dâng tháp", "Thu {so} {vat} từ {quai}.", "Hương trà thơm."),
  ("cau_do", "dap=Ho-Oh", "Câu hỏi của tháp", "'Chim cầu vồng, hồi sinh ba huyền thoại. Là ai?'", "Đúng."),
  ("tim_vat_an", "map=Ecruteak;vat=Lông vũ tím", "Lông bảy sắc", "Tìm chiếc lông rơi từ trời.", "Lông vũ bảy sắc."),
  ("noi_chuyen", "", "Cầu vồng", "Kể với Xatu.", "...Ho-Oh chỉ hiện ra với người có trái tim trong sáng.")]),
 ("CEL", "Celebi", 55, 8, "Mảnh tinh hoa", 0, "Rừng Ilex", "Người du hành qua thời gian", [
  ("noi_chuyen", "", "Chuông gió trong rừng", "Oáp... chuông gió đền Ilex kêu suốt đêm.", "Celebi đang gọi."),
  ("den_noi", "map=Rừng Ilex", "Đền rừng", "Đến Rừng Ilex.", "Ánh xanh lấp lánh."),
  ("lam_vuon", "vat=Hạt giống Berry;so=3", "Cây non", "Trồng {so} {vat} quanh đền.", "Cây lớn nhanh lạ thường."),
  ("quan_sat", "q=+0;dk=bình minh", "Bình minh", "Quan sát {quai} lúc bình minh.", "Thời gian như chậm lại."),
  ("tim_vat_an", "map=Rừng Ilex;vat=Hộp nhạc", "Hộp nhạc từ tương lai", "Tìm {vat} trong đền.", "Hộp nhạc khắc năm sau."),
  ("ap_trung", "so=1", "Quả trứng thời gian", "Ấp nở quả trứng tìm thấy trong đền.", "Một Pokémon nhỏ nở ra, mắt xanh lá."),
  ("noi_chuyen", "", "Thời gian", "Kể với ông.", "Oáp... Celebi cho cháu thấy tương lai. Tốt đẹp chứ?")]),
 ("MEW", "Mew", 65, 3, "Mảnh tinh hoa", 0, "Cinnabar", "Người tìm thấy Mew", [
  ("noi_chuyen", "", "Bong bóng hồng", "Boong... có bong bóng hồng bay quanh phòng thí nghiệm Cinnabar.", "Lạ."),
  ("den_noi", "map=Cinnabar", "Phòng thí nghiệm", "Đến Cinnabar.", "Bong bóng dẫn ra biển."),
  ("cau_do", "dap=Mew", "Nhật ký cũ", "'Pokémon mang gen của mọi Pokémon. Là ai?'", "Đúng. Mew."),
  ("quan_sat", "q=+0;dk=buổi sáng", "Trò chơi", "Quan sát {quai} buổi sáng, Mew hay chơi cùng chúng.", "Một đuôi hồng thoáng qua."),
  ("ho_tong", "vat=Bong bóng hồng;npc=3", "Đuổi bong bóng", "Đưa bong bóng về cho Bronzong.", "Bong bóng vỡ, để lại tiếng cười."),
  ("tim_vat_an", "map=Cinnabar;vat=Ảnh chụp", "Bức ảnh mờ", "Tìm {vat} cũ trong phòng thí nghiệm.", "Ảnh một Pokémon hồng nhỏ."),
  ("noi_chuyen", "", "Lời chào của Mew", "Kể với Bronzong.", "Boong... Mew chọn cậu làm bạn chơi.")]),
]

# Nhiem vu an: (ten, dieu kien mo, kieu, tham so, cap, mo, xong, danh hieu)
AN = [
 ("Snorlax thức giấc", "nói chuyện với Snorlax 10 lần vào ban đêm", "noi_chuyen", "npc=8", 10, "...Khò... Ơ? Cháu lại tới à? Mười lần rồi đấy. Ông kể cháu nghe bí mật: ông không ngủ đâu, ông nghe hết.", "Đừng nói với ai nhé.", "Người giữ bí mật của Snorlax"),
 ("Bóng ma lúc nửa đêm", "đứng trên đỉnh Tháp Lavender lúc 0:00", "dung_toa_do", "map=Lavender;diem=0", 30, "Đúng nửa đêm. Một bóng ma nhỏ hiện lên, mời bạn chơi trốn tìm.", "Bóng ma cười khúc khích rồi biến mất.", "Bạn của bóng đêm"),
 ("Magikarp vàng", "câu cá khi mưa 50 lần", "cau_ca", "vat=Vảy cá vàng;so=1", 20, "Dưới làn mưa, phao câu chìm sâu bất thường...", "Một Magikarp vàng óng! Nó búng lên rồi bơi đi.", "Người thấy Magikarp vàng"),
 ("Lời thì thầm của Unown", "giải 10 câu đố Natu", "cau_do", "dap=Alph", 30, "Các Unown xếp thành câu: 'Tên tàn tích của chúng ta là gì?'", "Các Unown xoay tròn vui vẻ.", "Người đọc chữ Unown"),
 ("Ditto ở Khu mua bán", "nói chuyện với Kecleon 20 lần", "noi_chuyen", "npc=1", 15, "Kecleon: Hí hí... thật ra tôi không phải Kecleon. Tôi là Ditto! Đùa thôi. Hay không?", "Kecleon đổi sang màu hồng rồi lại xanh.", "Người nhìn thấu"),
 ("Tảng đá biết nói", "đào 100 lần", "dao_dat", "map=Khu mỏ;so=1", 25, "Xẻng chạm vào một tảng đá. Tảng đá... ho.", "Một Sudowoodo cũ ngái ngủ chui lên, cảm ơn vì được đào lên.", "Thợ đào may mắn"),
 ("Người ngủ trên cầu", "đứng trên cầu Cerulean 5 phút", "dung_toa_do", "map=Cerulean;diem=0", 18, "Bạn đứng lặng trên cầu. Lão Torkoal bước tới ngồi cạnh, không nói gì.", "Lão kể chuyện thời trẻ đến tận khuya.", "Người lắng nghe"),
 ("Chuông bạc rung", "có Chuông bạc trong túi khi trời mưa ở Ecruteak", "quan_sat", "q=+0;dk=trời mưa", 62, "Chiếc chuông bạc tự rung trong túi.", "Một bóng bạc bay qua Tháp Chuông.", "Người nghe chuông bạc"),
 ("Bánh cho Snorlax", "nấu 30 món", "nau_an", "vat=Bánh mật ong;so=1", 20, "Ông Snorlax ngửi thấy mùi từ ba bản đồ.", "Oáp... cháu là đầu bếp của ông từ hôm nay.", "Đầu bếp của Snorlax"),
 ("Mười sáu ngọn đèn", "có đủ 16 huy hiệu", "dung_toa_do", "map=Pallet;diem=0", 83, "Bạn trở lại nơi ngôi sao rơi. Mười sáu đốm sáng lơ lửng.", "Đồng cỏ sáng như ban ngày.", "Người mang mười sáu ngọn đèn"),
 ("Trứng lạ", "ấp nở 20 trứng", "ap_trung", "so=1", 30, "Quả trứng này có hoa văn chưa từng thấy.", "Một Togepi nở ra, cười khanh khách.", "Người ấp trứng lạ"),
 ("Chạy cùng gió", "về đích 10 cuộc đua", "dua_pokemon", "", 25, "Gió đẩy sau lưng bạn suốt cuộc đua.", "Pidgeot bay theo cổ vũ.", "Người chạy cùng gió"),
 ("Kẻ trộm lông vũ", "giữ 30 Lông vũ tím", "noi_chuyen", "npc=5", 40, "Murkrow cũ quay lại, cúi đầu xin lỗi vì những gì đã làm.", "Murkrow giờ làm người đưa thư.", "Người tha thứ"),
 ("Vườn đêm", "thu hoạch ban đêm 20 lần", "lam_vuon", "vat=Hoa sương;so=1", 30, "Hoa sương chỉ nở dưới trăng.", "Vườn nhà bạn toả hương.", "Người làm vườn đêm"),
 ("Đối thủ cũ", "thắng 50 trận đấu trường", "thang_pvp", "so=1", 50, "Lém xuất hiện ở đấu trường, không hoá trang: 'Đấu thật một trận nhé!'", "Lém: Hô hô... vẫn thua. Bạn tốt.", "Bạn của Lém"),
 ("Hồ sơ Mặt Nạ Tím", "xong chương 44", "tim_vat_an", "map=Mahogany;vat=Cuộn băng radio", 75, "Trong căn cứ cũ còn sót một cuộn băng.", "Giọng Darkrai: 'Ta chỉ muốn không ai phải đau.'", "Người hiểu Darkrai"),
 ("Clefairy và mặt trăng", "đứng ở Núi Mặt Trăng lúc trăng tròn", "dung_toa_do", "map=Núi Mặt Trăng;diem=2", 16, "Bầy Clefairy mời bạn múa cùng.", "Bạn được tặng một viên đá Mặt Trăng.", "Vũ công dưới trăng"),
 ("Người đưa thư bí ẩn", "giao 50 thư/hàng", "dua_thu", "npc=23", 30, "Một lá thư không người gửi, chỉ đề tên lão Torkoal.", "Khụ khụ... thư của Lapras. Lão sẽ trả lời ngay.", "Người đưa thư tận tâm"),
 ("Trận chiến trong mơ", "ngủ trong nhà 3 đêm liền (không đăng nhập ban đêm)", "giet_tinh_anh", "q=+3", 60, "Bạn mơ thấy mình đấu với {quai}.", "Tỉnh dậy, người mỏi nhừ nhưng mạnh hơn.", "Người chiến đấu trong mơ"),
 ("Tiếng hát Lapras", "đi đò 20 lần", "noi_chuyen", "npc=10", 40, "La la~ mình hát riêng cho bạn bài này.", "Bài hát về hai người bạn già và một lá thư.", "Người nghe Lapras hát"),
 ("Kho báu Persian", "tặng Persian 10 món quà", "tim_vat_an", "map=Celadon;vat=Vòng cổ ngọc", 35, "Persian thì thầm chỗ giấu kho báu thật.", "Hừm. Giờ cưng biết bí mật lớn nhất của ta.", "Bạn thân của Persian"),
 ("Một ngày không đánh", "đăng nhập mà không hạ quái nào cả ngày", "noi_chuyen", "npc=8", 20, "Oáp... hôm nay cháu không đánh nhau à? Ngồi đây với ông.", "Một ngày yên bình cũng là một ngày tốt.", "Người biết nghỉ ngơi"),
 ("Bóng mắt xanh", "xong chuỗi huyền thoại Darkrai", "quan_sat", "q=+0;dk=ban đêm", 75, "Trong đêm, một đôi mắt xanh nhìn bạn, không còn đáng sợ.", "Darkrai gật đầu rồi tan biến.", "Người được Darkrai tin"),
 ("Kẻ ăn đá", "thu 100 Quặng sắt", "giet", "q=+0", 70, "Larvitar ở Núi Silver đã lớn thành Pupitar, muốn đấu với bạn.", "Pupitar: Rộp! Mạnh lên rồi chứ?", "Người nuôi Larvitar"),
 ("Bức thư năm sau", "một năm sau khi chôn Hộp thư thời gian", "dao_dat", "map=Pallet;so=1", 30, "Bạn đào lại chiếc hộp đã chôn một năm trước.", "Thư của Xatu: '...Ta đã biết cháu sẽ quay lại.'", "Người giữ lời hẹn"),
 ("Ngôi sao thứ mười bảy", "xong chương 62", "tim_vat_an", "map=Núi Silver;vat=Mảnh sao băng", 93, "Một mảnh sao nữa nằm trên Núi Silver, chưa ai thấy.", "Mảnh sao ấm lên khi gặp bạn.", "Người tìm sao"),
 ("Bữa tiệc của Snorlax", "mời 5 bạn bè cùng ăn bánh ở Pallet", "nau_an", "vat=Bánh quả mọng;so=5", 40, "Ông Snorlax mở tiệc.", "Oáp... no quá. Năm nay vui nhất.", "Khách quý của Snorlax"),
 ("Người gác cầu", "thắng 5 trận thử thách giờ liên tiếp", "thu_thach_gio", "q=+1;phut=4", 45, "Lão Torkoal bấm giờ: 'Thử lại thời trẻ của lão xem!'", "Khụ khụ... nhanh hơn lão ngày xưa.", "Người gác cầu Cerulean"),
 ("Hoa anh đào cuối mùa", "ghé Cherrygrove 30 ngày khác nhau", "dung_toa_do", "map=Cherrygrove;diem=0", 51, "Cánh hoa anh đào cuối cùng rơi vào tay bạn.", "Ông lão Furret mỉm cười: 'Ta đi lạc cả đời để thấy cảnh này.'", "Người ngắm hoa cuối mùa"),
 ("Lửa của Blaziken", "đạt cấp 90", "noi_chuyen", "npc=21", 90, "Blaziken: Lửa lên nào... lần cuối tôi dạy nhóc. Giờ nhóc dạy lại tôi đi.", "Hai thầy trò đấu tập đến tối.", "Học trò của Blaziken"),
 ("Huyền thoại của làng", "xong 100 nhiệm vụ phụ", "noi_chuyen", "npc=5", 50, "...Cả làng muốn dựng tượng cháu. Ta đã cản. Cháu không thích thế đâu.", "Thay vào đó, họ đặt tên một con đường theo tên cháu.", "Huyền thoại của làng"),
 ("Hòn đá lạ ở Pewter", "Geodude đứng gác bảo tàng 30 ngày", "noi_chuyen", "npc=2", 30, "Khà khà! Geodude tiến hoá rồi! Nó muốn cảm ơn cháu.", "Graveler đứng gác bảo tàng, ngực ưỡn cao hơn.", "Người bạn của đá"),
]

# Thanh tuu: (nhom, kieu, [(nguong, ten)])
THANH_TUU = [
 ("Cấp độ", "cap", [(10, "Bước chân đầu tiên"), (20, "Nhà huấn luyện đồng"), (30, "Vững vàng"), (40, "Nhà huấn luyện bạc"),
                    (50, "Nửa chặng đường"), (60, "Nhà huấn luyện vàng"), (70, "Vượt mùa"), (80, "Nhà huấn luyện bạch kim"),
                    (91, "Đỉnh cao ba vùng"), (100, "Huyền thoại sống")]),
 ("Pokédex", "pokedex", [(10, "Nhà sưu tầm 10"), (25, "Nhà sưu tầm 25"), (40, "Nhà sưu tầm 40"), (50, "Nhà sưu tầm 50"),
                         (60, "Nhà sưu tầm 60"), (75, "Nhà sưu tầm 75"), (100, "Nhà sưu tầm 100")]),
 ("Cốt truyện", "chuong", [(4, "Khởi hành"), (13, "Người kể chuyện"), (28, "Kanto toàn tập"), (40, "Gió Johto"),
                           (52, "Johto toàn tập"), (66, "Đất liền Hoenn"), (80, "Bình yên Hoenn")]),
 ("Huy hiệu Gym", "gym", [(1, "Huy hiệu đầu tiên"), (4, "Bốn ngọn đèn"), (8, "Tám huy hiệu Kanto"), (12, "Mười hai"),
                          (16, "Mười sáu ngọn đèn")]),
 ("Nâng cấp", "nang_cap", [(5, "Thợ rèn tập sự"), (8, "Thợ rèn khéo tay"), (10, "Thợ rèn lành nghề"), (12, "Thợ cả"),
                           (15, "Thợ rèn bậc thầy")]),
 ("Nhiệm vụ phụ", "phu", [(10, "Người hàng xóm tốt"), (50, "Người bạn của mọi nhà"), (100, "Huyền thoại thị trấn"),
                          (200, "Người không thể thiếu"), (400, "Linh hồn của ba vùng")]),
 ("Chăm chỉ", "ngay", [(7, "Một tuần trọn vẹn"), (30, "Chăm chỉ 30 ngày"), (100, "Chăm chỉ 100 ngày"),
                       (200, "Chăm chỉ 200 ngày"), (365, "Chăm chỉ 365 ngày")]),
 ("Đấu trường", "pvp", [(1, "Trận thắng đầu tiên"), (10, "Đấu trường: 10 trận thắng"), (50, "Đấu trường: 50 trận thắng"),
                        (100, "Đấu trường: 100 trận thắng"), (500, "Đấu trường: 500 trận thắng")]),
 ("Boss thế giới", "boss", [(1, "Lần đầu đột kích"), (10, "Săn huyền thoại"), (50, "Người săn boss"), (100, "Khắc tinh của boss")]),
 ("Đội", "doi", [(1, "Thành viên đội"), (10, "Đội mạnh nhất tuần"), (50, "Trụ cột của đội")]),
 ("Bộ trang bị", "bo", [(1, "Đủ bộ trang bị"), (3, "Bộ trang bị tinh anh"), (6, "Tủ đồ ba vùng")]),
 ("Sự kiện", "su_kien", [(5, "Người vui hội"), (20, "Thánh sự kiện"), (52, "Không bỏ lỡ tuần nào")]),
 ("Câu cá", "cau_ca", [(10, "Cần câu mới"), (100, "Ngư ông"), (500, "Vua câu cá"), (1000, "Huyền thoại sông hồ")]),
 ("Đào đất", "dao_dat", [(10, "Xẻng mới"), (100, "Thợ đào"), (500, "Thợ đào huyền thoại")]),
 ("Nấu ăn", "nau_an", [(10, "Bếp nhỏ"), (100, "Đầu bếp"), (300, "Siêu đầu bếp")]),
 ("Ấp trứng", "ap_trung", [(1, "Trứng đầu tiên"), (20, "Nhà trẻ đông vui"), (100, "Bảo mẫu ba vùng"), (300, "Cha mẹ của trăm Pokémon")]),
 ("Alpha", "alpha", [(1, "Hạ Alpha đầu tiên"), (10, "Thợ săn Alpha"), (50, "Khắc tinh Alpha"), (200, "Vua Alpha")]),
 ("Khám phá", "kham_pha", [(10, "Người đi đường"), (30, "Nhà thám hiểm"), (60, "Người vẽ bản đồ"), (90, "Đặt chân mọi nơi")]),
 ("Sư phạm", "su_pham", [(1, "Người thầy đầu tiên"), (5, "Người thầy tận tâm"), (20, "Bậc thầy dạy dỗ")]),
 ("Chế tạo", "che_tao", [(1, "Món đồ đầu tay"), (20, "Thợ chế tạo"), (100, "Nghệ nhân")]),
 ("Shiny", "shiny", [(1, "Màu sắc lạ"), (5, "Thợ săn shiny"), (20, "Bậc thầy shiny")]),
 ("Hầm ngục", "ham_nguc", [(10, "Hầm ngục tầng 10"), (30, "Hầm ngục tầng 30"), (50, "Đáy hầm ngục")]),
 ("Câu đố", "cau_do", [(5, "Đầu óc nhanh nhạy"), (25, "Nhà thông thái"), (60, "Bậc thầy câu đố")]),
 ("Huyền thoại", "huyen_thoai", [(1, "Người được huyền thoại chọn"), (5, "Bạn của huyền thoại"), (12, "Sử thi ba vùng")]),
 ("Bí ẩn", "an", [(1, "Kẻ tò mò"), (10, "Người tìm bí mật"), (32, "Không còn bí mật")]),
 ("Nhiệm vụ ngày", "ngay_xong", [(10, "Bảng ngày quen thuộc"), (100, "Người của bảng ngày"), (500, "Trụ cột bảng ngày"), (1000, "Bảng ngày là nhà")]),
 ("Nhiệm vụ tuần", "tuan_xong", [(10, "Tuần nào cũng có mặt"), (50, "Người của bảng tuần"), (100, "Trăm tuần")]),
 ("Lựa chọn", "lua_chon", [(5, "Người quyết đoán"), (15, "Người chọn đường")]),
]

# Dieu kien quan sat cho Pokedex sinh thai (luan phien theo loai).
QUAN_SAT_DK = ["trời mưa", "ban đêm", "buổi sáng", "sương mù", "nắng gắt", "trời tuyết", "hoàng hôn", "bình minh"]
