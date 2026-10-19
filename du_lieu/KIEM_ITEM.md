# Kiểm vật phẩm mồ côi / treo / không lấy được
> **File sinh ra** bởi `tools/pokemon/kiem_item_mo_coi.py` - đừng sửa tay. Test: `test/KiemItemMoCoi.java`.
> Luật: mọi vật phẩm có thể tồn tại phải có ≥ 1 **nguồn** đang chạy và ≥ 1 **công dụng** thật (bán NPC chỉ là đường lui).

## 0. Tóm tắt
| Cấu hình | Đủ nguồn + công dụng | Mồ côi (có nguồn, không công dụng) | Chỉ bán NPC (không công dụng thật) | Không lấy được (có công dụng / thiết kế nhưng không nguồn nào chạy) | Không tồn tại (không nguồn, không cần) |
|---|---|---|---|---|---|
| A (`lau_dai=1 he_sinh_thai=0`) | 810 | 0 | 0 | 0 | 758 |
| B (`lau_dai=1 he_sinh_thai=1`) | 877 | 0 | 0 | 0 | 691 |
| C (`lau_dai=0 he_sinh_thai=0`) — chỉ báo cáo | 680 | 1 | 0 | 102 | 785 |

Vật phẩm theo loại: data_item 1026, data_potion 162, ngọc 270, vật phẩm nhiệm vụ 49, túi sinh thái 61.
Tham chiếu treo: **0**. Quy tắc lỗi thời: **0**. Trùng tên: **0**. Tên kiếm hiệp còn lấy được: **0**.

Lỗi bị test chặn (cấu hình A + B): **0**.

Dấu vân tay đầu vào (SHA-1 của `sql/htbb_day_du.sql`, `sql/pokemon_noi_dung.sql`, `sql/pokemon_vat_pham_lau_dai.sql`, `sql/pokemon_lau_dai.sql`, `sql/pokemon_doi_chu.sql`, `sql/pokemon_nhiem_vu.sql`, `du_lieu/noi_dung_roi.csv`, `du_lieu/nhiem_vu_moi.csv`, bỏ `\r`): `c5e953c392dc2fe35534383a36c4721e017ae9ad`

## 1. Cấu hình A: `sv.lau_dai=1`, `sv.he_sinh_thai=0`

### Mồ côi (có nguồn, không công dụng): 0

### Chỉ bán NPC (không công dụng thật): 0

### Không lấy được (có công dụng / thiết kế nhưng không nguồn nào chạy): 0

## 2. Cấu hình B: `sv.lau_dai=1`, `sv.he_sinh_thai=1`

### Mồ côi (có nguồn, không công dụng): 0

### Chỉ bán NPC (không công dụng thật): 0

### Không lấy được (có công dụng / thiết kế nhưng không nguồn nào chạy): 0

## 3. Cấu hình C: `sv.lau_dai=0`, `sv.he_sinh_thai=0` (game cũ, chỉ báo cáo)

### Mồ côi (có nguồn, không công dụng): 1

| Vật phẩm | Tên | Nguồn đang chạy | Công dụng | Nguồn bị tắt |
|---|---|---|---|---|
| `ngoc:246` | Bột trắng | doAddGemItem | — | — |

### Chỉ bán NPC (không công dụng thật): 0

### Không lấy được (có công dụng / thiết kế nhưng không nguồn nào chạy): 102

| Vật phẩm | Tên | Nguồn đang chạy | Công dụng | Nguồn bị tắt |
|---|---|---|---|---|
| `item:1001` | Ruy băng sơ cấp | — | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) | thưởng chuỗi chính (data_quest_moi.itemgift) |
| `item:1002` | Ruy băng đồng | — | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) | thưởng chuỗi chính (data_quest_moi.itemgift) |
| `item:1003` | Ruy băng bạc | — | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) | thưởng chuỗi chính (data_quest_moi.itemgift); thưởng nhiệm vụ (NhiemVuEngine.traThuong) |
| `item:1004` | Khăn quàng thần thoại | — | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Trang bị (Bronzong, xu)); rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi); thưởng chuỗi chính (data_quest_moi.itemgift) …(+1) |
| `item:1005` | Khăn quàng thần thoại | — | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Trang bị (Bronzong, xu)) |
| `item:1006` | Đai thần thoại | — | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Trang bị (Bronzong, xu)); rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi); thưởng nhiệm vụ (NhiemVuEngine.traThuong) |
| `item:1007` | Băng đô thần thoại | — | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Trang bị (Bronzong, xu)); rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi); thưởng nhiệm vụ (NhiemVuEngine.traThuong) |
| `item:1008` | Than lửa thần thoại | — | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) | rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi); rơi trang bị theo dải cấp (alpha, Hoàn mỹ) (NoiDungLauDai.phatTrangBi); thưởng chuỗi chính (data_quest_moi.itemgift) …(+1) |
| `item:1009` | Hạt giống thần thần thoại | — | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) | rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi); rơi trang bị theo dải cấp (alpha, Hoàn mỹ) (NoiDungLauDai.phatTrangBi); thưởng chuỗi chính (data_quest_moi.itemgift) …(+1) |
| `item:1010` | Khăn lụa thần thoại | — | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) | rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi); rơi trang bị theo dải cấp (alpha, Hoàn mỹ) (NoiDungLauDai.phatTrangBi); thưởng chuỗi chính (data_quest_moi.itemgift) …(+1) |
| `item:1011` | Nước thần thần thoại | — | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) | rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi); rơi trang bị theo dải cấp (alpha, Hoàn mỹ) (NoiDungLauDai.phatTrangBi); thưởng chuỗi chính (data_quest_moi.itemgift) …(+1) |
| `item:1012` | Nam châm thần thoại | — | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) | rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi); rơi trang bị theo dải cấp (alpha, Hoàn mỹ) (NoiDungLauDai.phatTrangBi); thưởng chuỗi chính (data_quest_moi.itemgift) …(+1) |
| `item:1013` | Huy hiệu thần thoại | — | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) | thưởng chuỗi chính (data_quest_moi.itemgift); thưởng nhiệm vụ (NhiemVuEngine.traThuong) |
| `item:1014` | Vòng cổ thần thoại | — | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)); rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi); thưởng nhiệm vụ (NhiemVuEngine.traThuong) |
| `item:1015` | Lông vũ tốc độ thần thoại | — | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Trang bị (Bronzong, xu)); rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi); thưởng nhiệm vụ (NhiemVuEngine.traThuong) |
| `item:1016` | Găng đấm thần thoại | — | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Trang bị (Bronzong, xu)); rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi); thưởng nhiệm vụ (NhiemVuEngine.traThuong) |
| `item:1017` | Phiến đá hệ thần thoại | — | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)); thưởng nhiệm vụ (NhiemVuEngine.traThuong) |
| `item:1018` | Ruy băng thần thoại | — | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)); thưởng nhiệm vụ (NhiemVuEngine.traThuong) |
| `item:1019` | Khăn quàng tối thượng | — | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Trang bị (Bronzong, xu)); thưởng nhiệm vụ (NhiemVuEngine.traThuong) |
| `item:1020` | Khăn quàng tối thượng | — | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Trang bị (Bronzong, xu)) |
| `item:1021` | Đai tối thượng | — | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Trang bị (Bronzong, xu)); thưởng nhiệm vụ (NhiemVuEngine.traThuong) |
| `item:1022` | Băng đô tối thượng | — | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Trang bị (Bronzong, xu)); thưởng nhiệm vụ (NhiemVuEngine.traThuong) |
| `item:1023` | Than lửa tối thượng | — | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)); thưởng nhiệm vụ (NhiemVuEngine.traThuong) |
| `item:1024` | Hạt giống thần tối thượng | — | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)); thưởng nhiệm vụ (NhiemVuEngine.traThuong) |
| `item:1025` | Khăn lụa tối thượng | — | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)); thưởng nhiệm vụ (NhiemVuEngine.traThuong) |
| `item:1026` | Nước thần tối thượng | — | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)); thưởng nhiệm vụ (NhiemVuEngine.traThuong) |
| `item:1027` | Nam châm tối thượng | — | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)); thưởng nhiệm vụ (NhiemVuEngine.traThuong) |
| `item:1028` | Huy hiệu tối thượng | — | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)); thưởng nhiệm vụ (NhiemVuEngine.traThuong) |
| `item:1029` | Vòng cổ tối thượng | — | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)); thưởng nhiệm vụ (NhiemVuEngine.traThuong) |
| `item:1030` | Lông vũ tốc độ tối thượng | — | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Trang bị (Bronzong, xu)); thưởng chuỗi chính (data_quest_moi.itemgift); thưởng nhiệm vụ (NhiemVuEngine.traThuong) |
| `item:1031` | Găng đấm tối thượng | — | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Trang bị (Bronzong, xu)); thưởng nhiệm vụ (NhiemVuEngine.traThuong) |
| `item:1032` | Phiến đá hệ tối thượng | — | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)); thưởng nhiệm vụ (NhiemVuEngine.traThuong) |
| `item:1033` | Ruy băng tối thượng | — | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)); thưởng nhiệm vụ (NhiemVuEngine.traThuong) |
| `item:1034` | Bụi sao | — | — | rơi theo dải cấp; thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) |
| `item:1035` | Mảnh sao | — | — | rơi theo dải cấp; thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) |
| `item:1036` | Mảnh thiên thạch | — | — | rơi theo dải cấp; thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) |
| `item:1037` | Vảy rồng | — | — | Sổ tay → Cửa hàng & đổi vật phẩm (Đổi tín vật huyền thoại); mở item:1051 (NoiDungLauDai.moHop); rơi theo dải cấp |
| `item:1038` | Mảnh tinh hoa | — | — | Sổ tay → Cửa hàng & đổi vật phẩm (Đổi tín vật huyền thoại); mở item:1051 (NoiDungLauDai.moHop); thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) |
| `item:1039` | Huy chương Gym | — | — | Gym đánh lại hằng tuần (GymLauDai.khiHa); thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) |
| `item:1040` | Lông vũ Zapdos | — | — | boss thế giới theo tuần (NoiDungLauDai.roiBossTheGioi); thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) |
| `item:1041` | Lông vũ Articuno | — | — | boss thế giới năm 2 (NoiDungLauDai.TIN_VAT_NAM2); thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) |
| `item:1042` | Nanh lửa Entei | — | — | boss thế giới theo tuần (NoiDungLauDai.roiBossTheGioi); thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) |
| `item:1043` | Lông tơ Raikou | — | — | boss thế giới năm 2 (NoiDungLauDai.TIN_VAT_NAM2); thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) |
| `item:1044` | Mảnh bóng tối | — | — | boss thế giới theo tuần (NoiDungLauDai.roiBossTheGioi); thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) |
| `item:1045` | Pha lê Suicune | — | — | boss thế giới năm 2 (NoiDungLauDai.TIN_VAT_NAM2); thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) |
| `item:1046` | Vảy Rayquaza | — | — | boss thế giới theo tuần (NoiDungLauDai.roiBossTheGioi); thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) |
| `item:1047` | Lông bạc Lugia | — | — | boss thế giới năm 2 (NoiDungLauDai.TIN_VAT_NAM2); thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) |
| `item:1048` | Ngọc đỏ Groudon | — | — | boss thế giới theo tuần (NoiDungLauDai.roiBossTheGioi); thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) |
| `item:1049` | Ngọc lam Kyogre | — | — | boss thế giới năm 2 (NoiDungLauDai.TIN_VAT_NAM2) |
| `item:1050` | Hộp tham gia đột kích | — | — | boss thế giới theo tuần (NoiDungLauDai.roiBossTheGioi) |
| `item:1051` | Hộp vàng đột kích | — | — | boss thế giới theo tuần (NoiDungLauDai.roiBossTheGioi) |
| `item:1052` | Tinh thể hệ | — | — | thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) |
| `item:1053` | Huy hiệu ngày cộng đồng | — | — | thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) |
| `item:1054` | Tem nghiên cứu | — | — | thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) |
| `item:1055` | Huy chương mùa giải | — | — | thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) |
| `item:1056` | Cờ vùng | — | — | thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) |
| `item:1057` | Quà lễ 30/4 - 1/5 | — | — | thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) |
| `item:1058` | Quà lễ Quốc khánh 2/9 | — | — | thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) |
| `item:1059` | Quà lễ Phụ nữ 20/10 | — | — | thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) |
| `item:1060` | Quà lễ Nhà giáo 20/11 | — | — | thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) |
| `item:1061` | Phiếu giảm giá trang trí | — | — | thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) |
| `item:1201` | Thuốc giải độc | — | bán cho NPC (OfflineMap.doSellItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)) |
| `item:1202` | Thuốc trị bỏng | — | bán cho NPC (OfflineMap.doSellItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)) |
| `item:1203` | Thuốc trị tê liệt | — | bán cho NPC (OfflineMap.doSellItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)) |
| `item:1204` | Thuốc đánh thức | — | bán cho NPC (OfflineMap.doSellItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)) |
| `item:1205` | Thuốc rã đông | — | bán cho NPC (OfflineMap.doSellItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)) |
| `item:1206` | Thuốc chữa toàn diện | — | bán cho NPC (OfflineMap.doSellItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi Huy chương Gym); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi vật phẩm sự kiện) |
| `item:1207` | Thuốc hồi sinh tối đa | — | — | Sổ tay → Cửa hàng & đổi vật phẩm (Lượng khoá (Kecleon)); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi vật phẩm sự kiện) |
| `item:1208` | X Tốc độ | — | bán cho NPC (OfflineMap.doSellItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi vật phẩm sự kiện) |
| `item:1209` | X Chính xác | — | bán cho NPC (OfflineMap.doSellItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi vật phẩm sự kiện) |
| `item:1210` | Tăng chí mạng | — | bán cho NPC (OfflineMap.doSellItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi Huy chương mùa giải) |
| `item:1211` | Thuốc xua quái | — | bán cho NPC (OfflineMap.doSellItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi vật phẩm sự kiện) |
| `item:1212` | Siêu thuốc xua quái | — | bán cho NPC (OfflineMap.doSellItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)) |
| `item:1213` | Thuốc xua quái tối đa | — | bán cho NPC (OfflineMap.doSellItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)) |
| `item:1214` | Kẹo hiếm | — | — | Sổ tay → Cửa hàng & đổi vật phẩm (Đổi Tem nghiên cứu); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi vật phẩm cũ); mốc Pokédex khi bật dài hạn (Pokedex.doiThuongLauDai) |
| `item:1215` | Quả Oran | — | bán cho NPC (OfflineMap.doSellItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi vật phẩm sự kiện) |
| `item:1216` | Quả Sitrus | — | bán cho NPC (OfflineMap.doSellItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi vật phẩm sự kiện) |
| `item:1217` | Quả Lum | — | bán cho NPC (OfflineMap.doSellItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi vật phẩm sự kiện) |
| `item:1218` | Quả Cheri | — | bán cho NPC (OfflineMap.doSellItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi vật phẩm sự kiện) |
| `item:1219` | Quả Chesto | — | bán cho NPC (OfflineMap.doSellItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi vật phẩm sự kiện) |
| `item:1220` | Quả Pecha | — | bán cho NPC (OfflineMap.doSellItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi vật phẩm sự kiện) |
| `item:1221` | Quả Rawst | — | bán cho NPC (OfflineMap.doSellItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi vật phẩm sự kiện) |
| `item:1222` | Quả Aspear | — | bán cho NPC (OfflineMap.doSellItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi vật phẩm sự kiện) |
| `item:1223` | Quả Leppa | — | bán cho NPC (OfflineMap.doSellItem) | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi vật phẩm sự kiện) |
| `item:1224` | Thức ăn thừa | — | — | Sổ tay → Cửa hàng & đổi vật phẩm (Đổi Huy chương Gym); chuỗi thưởng ITEM |
| `item:1225` | Chuông vỏ sò | — | — | Sổ tay → Cửa hàng & đổi vật phẩm (Đổi Huy chương Gym) |
| `item:1226` | Móng vuốt nhanh | — | — | Sổ tay → Cửa hàng & đổi vật phẩm (Đổi Huy chương mùa giải) |
| `item:1227` | Băng đô tập trung | — | — | Sổ tay → Cửa hàng & đổi vật phẩm (Đổi Huy chương Gym) |
| `item:1228` | Kính ngắm | — | — | Sổ tay → Cửa hàng & đổi vật phẩm (Đổi Huy chương Gym) |
| `item:1229` | Đai lực sĩ | — | — | Sổ tay → Cửa hàng & đổi vật phẩm (Đổi Huy chương mùa giải) |
| `item:1230` | Đá vĩnh cửu | — | — | Sổ tay → Cửa hàng & đổi vật phẩm (Đổi Huy chương Gym) |
| `item:1231` | Trứng may mắn | — | — | Sổ tay → Cửa hàng & đổi vật phẩm (Đổi Tem nghiên cứu); mốc Pokédex 50 khi bật dài hạn (Pokedex.doiThuongLauDai) |
| `item:1250` | Vé EXP x1,5 (30 phút) | — | — | Cửa hàng & Nạp (lượng, trao tang_toc; cần sv.nap_tien) |
| `item:1251` | Vé EXP x1,5 (30 phút) - khoá | — | — | Sổ tay → Cửa hàng & đổi vật phẩm (Lượng khoá (Kecleon)); bản khoá: nhiệm vụ tuần / lịch đăng nhập / vé mùa (TangTocGame.thuongKhoa) |
| `item:1252` | Vé EXP x2 (30 phút) | — | — | Cửa hàng & Nạp (lượng, trao tang_toc; cần sv.nap_tien) |
| `item:1253` | Vé EXP x2 (30 phút) - khoá | — | — | bản khoá: nhiệm vụ tuần / lịch đăng nhập / vé mùa (TangTocGame.thuongKhoa) |
| `item:1254` | Bùa thể lực (+60 phút) | — | — | Cửa hàng & Nạp (lượng, trao tang_toc; cần sv.nap_tien) |
| `item:1255` | Bùa thể lực (+60 phút) - khoá | — | — | bản khoá: nhiệm vụ tuần / lịch đăng nhập / vé mùa (TangTocGame.thuongKhoa) |
| `item:1256` | Phiếu ủy thác luyện tập (1 giờ) | — | — | Cửa hàng & Nạp (lượng, trao tang_toc; cần sv.nap_tien) |
| `item:1257` | Phiếu ủy thác luyện tập (1 giờ) - khoá | — | — | Sổ tay → Cửa hàng & đổi vật phẩm (Lượng khoá (Kecleon)); bản khoá: nhiệm vụ tuần / lịch đăng nhập / vé mùa (TangTocGame.thuongKhoa) |
| `item:1258` | Bùa bảo hộ | — | — | Sổ tay → Cửa hàng & đổi vật phẩm (Lượng khoá (Kecleon)) |
| `item:1259` | Bùa bảo hộ cao cấp | — | — | Sổ tay → Cửa hàng & đổi vật phẩm (Lượng khoá (Kecleon)) |

## 4. Tham chiếu treo (trỏ vào vật phẩm không có)

Không có.

## 5. Nguồn khai báo nhưng không chạy (thiết kế / hệ thống đã tắt)

| Vật phẩm | Lý do | Số chỗ | Ví dụ |
|---|---|---|---|
| `item:1` Khăn quàng sơ cấp | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 1 (dai 1-5) |
| `item:7` Khăn quàng đồng | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 41 (dai 11-15) |
| `item:19` Khăn quàng bạch kim | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 161 (dai 41-45) |
| `item:23` Khăn quàng kim cương | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 201 (dai 51-55) |
| `item:27` Đai sơ cấp | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 2 (dai 1-5) |
| `item:33` Đai đồng | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 42 (dai 11-15) |
| `item:37` Đai bạc | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 82 (dai 21-25) |
| `item:41` Đai vàng | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 122 (dai 31-35) |
| `item:45` Đai bạch kim | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 162 (dai 41-45) |
| `item:49` Đai kim cương | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 202 (dai 51-55) |
| `item:53` Băng đô sơ cấp | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 3 (dai 1-5) |
| `item:59` Băng đô đồng | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 43 (dai 11-15) |
| `item:63` Băng đô bạc | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 83 (dai 21-25) |
| `item:67` Băng đô vàng | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 123 (dai 31-35) |
| `item:71` Băng đô bạch kim | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 163 (dai 41-45) |
| `item:75` Băng đô kim cương | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 203 (dai 51-55) |
| `item:79` Than lửa sơ cấp | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 4 (dai 1-5) |
| `item:81` Than lửa đồng | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 44 (dai 11-15) |
| `item:83` Than lửa bạc | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 84 (dai 21-25) |
| `item:85` Than lửa vàng | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 124 (dai 31-35) |
| `item:86` Hạt giống thần sơ cấp | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 5 (dai 1-5) |
| `item:88` Hạt giống thần đồng | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 45 (dai 11-15) |
| `item:90` Hạt giống thần bạc | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 85 (dai 21-25) |
| `item:92` Hạt giống thần vàng | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 125 (dai 31-35) |
| `item:93` Khăn lụa sơ cấp | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 6 (dai 1-5) |
| `item:95` Khăn lụa đồng | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 46 (dai 11-15) |
| `item:97` Khăn lụa bạc | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 86 (dai 21-25) |
| `item:99` Khăn lụa vàng | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 126 (dai 31-35) |
| `item:100` Nước thần sơ cấp | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 7 (dai 1-5) |
| `item:102` Nước thần đồng | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 47 (dai 11-15) |
| `item:104` Nước thần bạc | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 87 (dai 21-25) |
| `item:106` Nước thần vàng | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 127 (dai 31-35) |
| `item:107` Nam châm sơ cấp | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 8 (dai 1-5) |
| `item:109` Nam châm đồng | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 48 (dai 11-15) |
| `item:111` Nam châm bạc | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 88 (dai 21-25) |
| `item:113` Nam châm vàng | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 128 (dai 31-35) |
| `item:126` Vòng cổ sơ cấp | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 9 (dai 1-5) |
| `item:128` Vòng cổ đồng | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 49 (dai 11-15) |
| `item:130` Vòng cổ bạc | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 89 (dai 21-25) |
| `item:132` Vòng cổ vàng | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 129 (dai 31-35) |
| `item:134` Vòng cổ bạch kim | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 169 (dai 41-45) |
| `item:136` Vòng cổ kim cương | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 209 (dai 51-55) |
| `item:138` Lông vũ tốc độ sơ cấp | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 10 (dai 1-5) |
| `item:140` Lông vũ tốc độ đồng | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 50 (dai 11-15) |
| `item:142` Lông vũ tốc độ bạc | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 90 (dai 21-25) |
| `item:144` Lông vũ tốc độ vàng | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 130 (dai 31-35) |
| `item:146` Lông vũ tốc độ bạch kim | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 170 (dai 41-45) |
| `item:148` Lông vũ tốc độ kim cương | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 210 (dai 51-55) |
| `item:150` Găng đấm sơ cấp | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 11 (dai 1-5) |
| `item:152` Găng đấm đồng | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 51 (dai 11-15) |
| `item:154` Găng đấm bạc | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 91 (dai 21-25) |
| `item:156` Găng đấm vàng | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 131 (dai 31-35) |
| `item:158` Găng đấm bạch kim | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 171 (dai 41-45) |
| `item:160` Găng đấm kim cương | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 211 (dai 51-55) |
| `item:175` Than lửa bạch kim | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 164 (dai 41-45) |
| `item:177` Than lửa kim cương | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 204 (dai 51-55) |
| `item:179` Than lửa chuyên gia | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 244 (dai 61-65) |
| `item:181` Than lửa tinh anh | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 284 (dai 71-75) |
| `item:183` Hạt giống thần bạch kim | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 165 (dai 41-45) |
| `item:185` Hạt giống thần kim cương | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 205 (dai 51-55) |
| `item:187` Hạt giống thần chuyên gia | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 245 (dai 61-65) |
| `item:189` Hạt giống thần tinh anh | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 285 (dai 71-75) |
| `item:191` Khăn lụa bạch kim | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 166 (dai 41-45) |
| `item:193` Khăn lụa kim cương | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 206 (dai 51-55) |
| `item:195` Khăn lụa chuyên gia | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 246 (dai 61-65) |
| `item:197` Khăn lụa tinh anh | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 286 (dai 71-75) |
| `item:199` Nước thần bạch kim | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 167 (dai 41-45) |
| `item:201` Nước thần kim cương | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 207 (dai 51-55) |
| `item:203` Nước thần chuyên gia | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 247 (dai 61-65) |
| `item:205` Nước thần tinh anh | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 287 (dai 71-75) |
| `item:207` Nam châm bạch kim | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 168 (dai 41-45) |
| `item:209` Nam châm kim cương | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 208 (dai 51-55) |
| `item:211` Nam châm chuyên gia | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 248 (dai 61-65) |
| `item:213` Nam châm tinh anh | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 288 (dai 71-75) |
| `item:585` Ruy băng bạch kim | nguồn 'boss_the_gioi' không có code đọc | 10 | data_noi_dung_roi stt 499 (boss_the_gioi tuan_01:top10) |
| `item:587` Ruy băng kim cương | nguồn 'boss_the_gioi' không có code đọc | 10 | data_noi_dung_roi stt 530 (boss_the_gioi tuan_11:top10) |
| `item:589` Ruy băng chuyên gia | nguồn 'boss_the_gioi' không có code đọc | 11 | data_noi_dung_roi stt 561 (boss_the_gioi tuan_21:top10) |
| `item:591` Ruy băng tinh anh | nguồn 'boss_the_gioi' không có code đọc | 10 | data_noi_dung_roi stt 595 (boss_the_gioi tuan_32:top10) |
| `item:593` Ruy băng huyền thoại | nguồn 'boss_the_gioi' không có code đọc | 10 | data_noi_dung_roi stt 626 (boss_the_gioi tuan_42:top10) |
| `item:599` Than lửa huyền thoại | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 324 (dai 81-85) |
| `item:601` Hạt giống thần huyền thoại | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 325 (dai 81-85) |
| `item:603` Khăn lụa huyền thoại | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 326 (dai 81-85) |
| `item:605` Nước thần huyền thoại | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 327 (dai 81-85) |
| `item:607` Nam châm huyền thoại | trang bị Map.dropItem cũ đã rơi - bảng không phát lần hai (ngân sách) | 2 | data_noi_dung_roi stt 328 (dai 81-85) |
| `item:1018` Ruy băng thần thoại | nguồn 'boss_the_gioi' không có code đọc | 1 | data_noi_dung_roi stt 657 (boss_the_gioi tuan_52:top10) |
| `thuoc:1` Thuốc hồi máu | NoiDungLauDai.phat chỉ phát ngọc / item type 28 - dòng bị bỏ qua | 4 | data_noi_dung_roi stt 14 (dai 1-5) |
| `thuoc:2` Siêu thuốc hồi máu | NoiDungLauDai.phat chỉ phát ngọc / item type 28 - dòng bị bỏ qua | 4 | data_noi_dung_roi stt 94 (dai 21-25) |
| `thuoc:3` Thuốc hồi máu cực mạnh | NoiDungLauDai.phat chỉ phát ngọc / item type 28 - dòng bị bỏ qua | 4 | data_noi_dung_roi stt 174 (dai 41-45) |
| `thuoc:22` Thuốc hồi máu tối đa | NoiDungLauDai.phat chỉ phát ngọc / item type 28 - dòng bị bỏ qua | 4 | data_noi_dung_roi stt 254 (dai 61-65) |
| `thuoc:26` Thuốc hồi phục toàn diện | NoiDungLauDai.phat chỉ phát ngọc / item type 28 - dòng bị bỏ qua | 4 | data_noi_dung_roi stt 334 (dai 81-85) |

## 6. Trùng tên / gần trùng (khác loại hoặc khác chức năng)

Không có.

## 7. Tên kiếm hiệp còn lấy được (từ điển cấm `chu_pokemon.tu_dien_cam`)

Không có.

## 8. Quy tắc viết tay lỗi thời

Không có: mọi quy tắc trong `QUY_TAC` còn khớp mã nguồn.

## 9. Cách đọc, giới hạn

- **Nguồn** = rơi (bảng `data_noi_dung_roi` chỉ tính nguồn `dai`/`alpha`/`gym`/`gym_tuan` mà code đọc), cửa hàng (`data_shop` trừ dòng bị `data_shop_gia_lau_dai` chặn, NPC bán đồ, cửa hàng thuốc/ngọc, `data_cua_hang_lau_dai`), thưởng nhiệm vụ (engine + chuỗi chính `data_quest_moi`), Gym, Pokédex, hệ sinh thái, và số liệu cứng quét từ `src/` (`createItemSoLuong`, `doAddGemItem`, `potions[N]++`, `new Potion((short)N`, chuỗi `ITEM:/GEM:/PT:`). Giftcode không tính là nguồn (quản trị phát tay).
- **Công dụng** = mặc theo type, dùng (`doUsePotion`, `VatPhamDungLauDai`, đá tiến hoá, rương…), nguyên liệu nâng cấp / chế tạo / ngọc theo kiểu `GemTemplate.TYPE_*`, nộp nhiệm vụ, đổi ở `data_cua_hang_lau_dai`, công thức hệ sinh thái, trang trí. Bán NPC = “chỉ bán”.
- Cổng: `LD` = `sv.lau_dai`, `HST` = `sv.he_sinh_thai`, `CDP` = luôn 1 (game luôn là Pokémon), `SK` = sự kiện theo lịch (lịch 52 tuần của dài hạn bật lại các sự kiện cũ), `NAP` = `sv.nap_tien`. Hàm không ai gọi (`HAM_CHET`) không tính.
- Lớp cũ (thuốc / ngọc sự kiện kiếm hiệp) đọc qua mảng id nên phần quét là **xấp xỉ**: vật phẩm cũ không tìm thấy nguồn được xếp “không tồn tại”, không phải lỗi.

## 10. Ma trận (vật phẩm có nguồn ở cấu hình A hoặc B)

Đầy đủ: `du_lieu/kiem_item.csv`.

| Vật phẩm | Tên | A | B | Nguồn | Công dụng |
|---|---|---|---|---|---|
| `item:1` | Khăn quàng sơ cấp | ok | ok | rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2); thưởng chuỗi chính (data_quest_moi.itemgift) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:3` | Khăn quàng sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:4` | Khăn quàng sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:5` | Khăn quàng sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:6` | Khăn quàng sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:7` | Khăn quàng đồng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) …(+2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:8` | Khăn quàng đồng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:9` | Khăn quàng đồng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:10` | Khăn quàng đồng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:11` | Khăn quàng bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:12` | Khăn quàng bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:13` | Khăn quàng bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:14` | Khăn quàng bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:15` | Khăn quàng vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:16` | Khăn quàng vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:17` | Khăn quàng vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:18` | Khăn quàng vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:19` | Khăn quàng bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (Pet) …(+1) |
| `item:20` | Khăn quàng bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (Pet) …(+1) |
| `item:21` | Khăn quàng bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (Pet) …(+1) |
| `item:22` | Khăn quàng bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (isPetKhi) …(+1) |
| `item:23` | Khăn quàng kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) …(+2) | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (isPetCu) …(+1) |
| `item:24` | Khăn quàng kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (isPetRong) …(+1) |
| `item:25` | Khăn quàng kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (isPetYeuTinh) …(+1) |
| `item:26` | Khăn quàng kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (isPetDoi) …(+1) |
| `item:27` | Đai sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) …(+2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:28` | Đai sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (isPetLongDenvinhVien) …(+1) |
| `item:29` | Đai sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (isPetBachThu) …(+1) |
| `item:30` | Đai sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (canTrade) …(+1) |
| `item:31` | Đai sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:32` | Đai sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:33` | Đai đồng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:34` | Đai đồng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:35` | Đai đồng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:36` | Đai đồng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (attack) …(+4) |
| `item:37` | Đai bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) …(+1) | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (attack) …(+4) |
| `item:38` | Đai bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:39` | Đai bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (actorDie) …(+4) |
| `item:40` | Đai bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:41` | Đai vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:42` | Đai vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:43` | Đai vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (isLienHoaTru) …(+1) |
| `item:44` | Đai vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:45` | Đai bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) …(+1) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:46` | Đai bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:47` | Đai bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:48` | Đai bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:49` | Đai kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) …(+2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:50` | Đai kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:51` | Đai kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:52` | Đai kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:53` | Băng đô sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:54` | Băng đô sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:55` | Băng đô sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:56` | Băng đô sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:57` | Băng đô sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:58` | Băng đô sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:59` | Băng đô đồng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:60` | Băng đô đồng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:61` | Băng đô đồng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:62` | Băng đô đồng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:63` | Băng đô bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) …(+1) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:64` | Băng đô bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:65` | Băng đô bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:66` | Băng đô bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:67` | Băng đô vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:68` | Băng đô vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:69` | Băng đô vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:70` | Băng đô vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:71` | Băng đô bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) …(+2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:72` | Băng đô bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:73` | Băng đô bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:74` | Băng đô bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:75` | Băng đô kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:76` | Băng đô kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:77` | Băng đô kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:78` | Băng đô kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:79` | Than lửa sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi trang bị theo dải cấp (alpha, Hoàn mỹ) (NoiDungLauDai.phatTrangBi) …(+1) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:80` | Than lửa sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:81` | Than lửa đồng | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) …(+3) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:82` | Than lửa đồng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (BossDotKich) …(+6) |
| `item:83` | Than lửa bạc | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) …(+3) | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (isCongThanh) …(+2) |
| `item:84` | Than lửa bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (update) …(+3) |
| `item:85` | Than lửa vàng | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) …(+4) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:86` | Hạt giống thần sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi trang bị theo dải cấp (alpha, Hoàn mỹ) (NoiDungLauDai.phatTrangBi) …(+1) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:87` | Hạt giống thần sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:88` | Hạt giống thần đồng | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) …(+3) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:89` | Hạt giống thần đồng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:90` | Hạt giống thần bạc | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) …(+3) | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (BossDotKich) …(+5) |
| `item:91` | Hạt giống thần bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (isBossLinhXa) …(+1) |
| `item:92` | Hạt giống thần vàng | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) …(+4) | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (isBossBachXa) …(+1) |
| `item:93` | Khăn lụa sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi trang bị theo dải cấp (alpha, Hoàn mỹ) (NoiDungLauDai.phatTrangBi) …(+1) | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (isBossMatQuy) …(+1) |
| `item:94` | Khăn lụa sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (isBossBachCot) …(+1) |
| `item:95` | Khăn lụa đồng | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) …(+3) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:96` | Khăn lụa đồng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:97` | Khăn lụa bạc | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) …(+3) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:98` | Khăn lụa bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:99` | Khăn lụa vàng | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) …(+3) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:100` | Nước thần sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi trang bị theo dải cấp (alpha, Hoàn mỹ) (NoiDungLauDai.phatTrangBi) …(+1) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:101` | Nước thần sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:102` | Nước thần đồng | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) …(+3) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:103` | Nước thần đồng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:104` | Nước thần bạc | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) …(+3) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:105` | Nước thần bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:106` | Nước thần vàng | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) …(+3) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:107` | Nam châm sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi trang bị theo dải cấp (alpha, Hoàn mỹ) (NoiDungLauDai.phatTrangBi) …(+1) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:108` | Nam châm sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:109` | Nam châm đồng | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) …(+3) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:110` | Nam châm đồng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:111` | Nam châm bạc | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) …(+3) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:112` | Nam châm bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:113` | Nam châm vàng | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) …(+3) | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (actorDie) …(+4) |
| `item:114` | Huy hiệu sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) …(+1) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:115` | Huy hiệu sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (actorDie) …(+6) |
| `item:116` | Huy hiệu đồng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) …(+2) | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (attack) …(+4) |
| `item:117` | Huy hiệu đồng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (attack) …(+3) |
| `item:118` | Huy hiệu bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:119` | Huy hiệu bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:120` | Huy hiệu vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) …(+1) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:121` | Huy hiệu vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:122` | Huy hiệu bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:123` | Huy hiệu bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:124` | Huy hiệu kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) …(+2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:125` | Huy hiệu kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:126` | Vòng cổ sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)) …(+1) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:127` | Vòng cổ sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:128` | Vòng cổ đồng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)) …(+2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:129` | Vòng cổ đồng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (attack) …(+2) |
| `item:130` | Vòng cổ bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)) …(+1) | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (attack) …(+2) |
| `item:131` | Vòng cổ bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:132` | Vòng cổ vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)) …(+3) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:133` | Vòng cổ vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:134` | Vòng cổ bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)) …(+1) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:135` | Vòng cổ bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:136` | Vòng cổ kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)) …(+1) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:137` | Vòng cổ kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:138` | Lông vũ tốc độ sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:139` | Lông vũ tốc độ sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:140` | Lông vũ tốc độ đồng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) …(+2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:141` | Lông vũ tốc độ đồng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:142` | Lông vũ tốc độ bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:143` | Lông vũ tốc độ bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:144` | Lông vũ tốc độ vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) …(+1) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:145` | Lông vũ tốc độ vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:146` | Lông vũ tốc độ bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:147` | Lông vũ tốc độ bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:148` | Lông vũ tốc độ kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) …(+2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:149` | Lông vũ tốc độ kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:150` | Găng đấm sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) …(+2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:151` | Găng đấm sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:152` | Găng đấm đồng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:153` | Găng đấm đồng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:154` | Găng đấm bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) …(+1) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:155` | Găng đấm bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:156` | Găng đấm vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:157` | Găng đấm vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:158` | Găng đấm bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:159` | Găng đấm bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:160` | Găng đấm kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) …(+2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:161` | Găng đấm kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:162` | Phiến đá hệ sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:163` | Phiến đá hệ sơ cấp | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)) …(+1) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:164` | Phiến đá hệ đồng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)) …(+2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:165` | Phiến đá hệ đồng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:166` | Phiến đá hệ bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)) …(+3) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:167` | Phiến đá hệ bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:168` | Phiến đá hệ vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)) …(+2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:169` | Phiến đá hệ vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:170` | Phiến đá hệ bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)) …(+1) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:171` | Phiến đá hệ bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:172` | Phiến đá hệ kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)) …(+2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:173` | Phiến đá hệ kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:174` | Than lửa vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:175` | Than lửa bạch kim | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) …(+3) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:176` | Than lửa bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:177` | Than lửa kim cương | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) …(+2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:178` | Than lửa kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:179` | Than lửa chuyên gia | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) …(+4) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:180` | Than lửa chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:181` | Than lửa tinh anh | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) …(+3) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:182` | Hạt giống thần vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:183` | Hạt giống thần bạch kim | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) …(+3) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:184` | Hạt giống thần bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:185` | Hạt giống thần kim cương | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) …(+2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:186` | Hạt giống thần kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:187` | Hạt giống thần chuyên gia | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) …(+4) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:188` | Hạt giống thần chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:189` | Hạt giống thần tinh anh | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) …(+3) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:190` | Khăn lụa vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:191` | Khăn lụa bạch kim | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) …(+4) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:192` | Khăn lụa bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:193` | Khăn lụa kim cương | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) …(+2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:194` | Khăn lụa kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:195` | Khăn lụa chuyên gia | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) …(+3) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:196` | Khăn lụa chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:197` | Khăn lụa tinh anh | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) …(+4) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:198` | Nước thần vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:199` | Nước thần bạch kim | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) …(+4) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:200` | Nước thần bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:201` | Nước thần kim cương | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) …(+2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:202` | Nước thần kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:203` | Nước thần chuyên gia | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) …(+3) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:204` | Nước thần chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:205` | Nước thần tinh anh | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) …(+4) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:206` | Nam châm vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:207` | Nam châm bạch kim | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) …(+4) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:208` | Nam châm bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:209` | Nam châm kim cương | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) …(+2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:210` | Nam châm kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:211` | Nam châm chuyên gia | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) …(+3) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:212` | Nam châm chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:213` | Nam châm tinh anh | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) …(+4) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:214` | Than lửa bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:215` | Than lửa bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:216` | Than lửa vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:217` | Than lửa vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:218` | Than lửa bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:219` | Than lửa bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:220` | Than lửa kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:221` | Than lửa kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:222` | Than lửa chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:223` | Than lửa chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:224` | Hạt giống thần bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:225` | Hạt giống thần bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:226` | Hạt giống thần vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:227` | Hạt giống thần vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:228` | Hạt giống thần bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:229` | Hạt giống thần bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:230` | Hạt giống thần kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:231` | Hạt giống thần kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:232` | Hạt giống thần chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:233` | Hạt giống thần chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:234` | Khăn lụa bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:235` | Khăn lụa bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:236` | Khăn lụa vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:237` | Khăn lụa vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:238` | Khăn lụa bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:239` | Khăn lụa bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:240` | Khăn lụa kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:241` | Khăn lụa kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:242` | Khăn lụa chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:243` | Khăn lụa chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:244` | Nước thần bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:245` | Nước thần bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:246` | Nước thần vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:247` | Nước thần vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:248` | Nước thần bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:249` | Nước thần bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:250` | Nước thần kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:251` | Nước thần kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:252` | Nước thần chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:253` | Nước thần chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:254` | Nam châm bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:255` | Nam châm bạc | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:256` | Nam châm vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:257` | Nam châm vàng | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:258` | Nam châm bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:259` | Nam châm bạch kim | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:260` | Nam châm kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:261` | Nam châm kim cương | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:262` | Nam châm chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:263` | Nam châm chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:268` | Khăn quàng bạc | ok | ok | rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:272` | Khăn quàng vàng | ok | ok | rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi); thưởng chuỗi chính (data_quest_moi.itemgift) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:465` | Cuốc thường | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:466` | Cuốc xịn | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:467` | Khăn quàng chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:468` | Khăn quàng chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:469` | Khăn quàng chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:470` | Khăn quàng chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:471` | Khăn quàng tinh anh | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi) …(+1) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:472` | Khăn quàng tinh anh | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:473` | Khăn quàng tinh anh | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:474` | Khăn quàng tinh anh | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:475` | Đai chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:476` | Đai chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:477` | Đai chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:478` | Đai chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:479` | Đai tinh anh | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi) …(+1) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:480` | Đai tinh anh | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:481` | Đai tinh anh | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:482` | Đai tinh anh | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:483` | Băng đô chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:484` | Băng đô chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:485` | Băng đô chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:486` | Băng đô chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:487` | Băng đô tinh anh | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi) …(+1) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:488` | Băng đô tinh anh | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:489` | Băng đô tinh anh | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:490` | Băng đô tinh anh | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:491` | Lông vũ tốc độ chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:492` | Lông vũ tốc độ chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:493` | Lông vũ tốc độ tinh anh | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:494` | Lông vũ tốc độ tinh anh | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:495` | Găng đấm chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:496` | Găng đấm chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:497` | Găng đấm tinh anh | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi) …(+1) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:498` | Găng đấm tinh anh | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:499` | Huy hiệu chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:500` | Huy hiệu chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:501` | Huy hiệu tinh anh | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); thưởng chuỗi chính (data_quest_moi.itemgift) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:502` | Huy hiệu tinh anh | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:503` | Vòng cổ chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)) …(+3) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:504` | Vòng cổ chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:505` | Vòng cổ tinh anh | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)) …(+1) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:506` | Vòng cổ tinh anh | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:509` | Mũ cưỡi Poké | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:510` | Mũ cưỡi Great | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:511` | Mũ cưỡi Ultra | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:512` | Mũ cưỡi Quick | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:513` | Mũ cưỡi Dusk | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:514` | Mũ cưỡi Timer | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:515` | Mũ cưỡi Luxury | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:516` | Mũ cưỡi Heavy | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:517` | Mũ cưỡi Level | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:518` | Mũ cưỡi Moon | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:519` | Mũ cưỡi Dream | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:520` | Mũ cưỡi Master | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:521` | Giáp cưỡi Poké | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:522` | Giáp cưỡi Great | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:523` | Giáp cưỡi Ultra | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:524` | Giáp cưỡi Quick | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:525` | Giáp cưỡi Dusk | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:526` | Giáp cưỡi Timer | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:527` | Giáp cưỡi Luxury | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:528` | Giáp cưỡi Heavy | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:529` | Giáp cưỡi Level | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:530` | Giáp cưỡi Moon | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:531` | Giáp cưỡi Dream | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:532` | Giáp cưỡi Master | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:533` | Yên cương Poké | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:534` | Yên cương Great | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:535` | Yên cương Ultra | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:536` | Yên cương Quick | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:537` | Yên cương Dusk | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:538` | Yên cương Timer | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:539` | Yên cương Luxury | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:540` | Yên cương Heavy | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:541` | Yên cương Level | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:542` | Yên cương Moon | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:543` | Yên cương Dream | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:544` | Yên cương Master | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:545` | Bàn đạp Poké | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:546` | Bàn đạp Great | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:547` | Bàn đạp Ultra | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:548` | Bàn đạp Quick | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:549` | Bàn đạp Dusk | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:550` | Bàn đạp Timer | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:551` | Bàn đạp Luxury | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:552` | Bàn đạp Heavy | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:553` | Bàn đạp Level | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:554` | Bàn đạp Moon | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:555` | Bàn đạp Dream | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:556` | Bàn đạp Master | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:557` | Vòng chân Poké | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:558` | Vòng chân Great | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:559` | Vòng chân Ultra | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:560` | Vòng chân Quick | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:561` | Vòng chân Dusk | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:562` | Vòng chân Timer | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:563` | Vòng chân Luxury | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:564` | Vòng chân Heavy | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:565` | Vòng chân Level | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:566` | Vòng chân Moon | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:567` | Vòng chân Dream | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:568` | Vòng chân Master | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); boss rơi đồ thú type 14..18 theo cấp (Monster.dropItemAnimal) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:569` | Phiến đá hệ chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)) …(+2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:570` | Phiến đá hệ chuyên gia | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:571` | Phiến đá hệ tinh anh | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:572` | Phiến đá hệ tinh anh | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:584` | Ruy băng vàng | ok | ok | hộp quà Halloween / World Cup (createItemCoat có hạn); thưởng chuỗi chính (data_quest_moi.itemgift) | mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:585` | Ruy băng bạch kim | ok | ok | hộp quà Halloween / World Cup (createItemCoat có hạn) | mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:586` | Ruy băng bạch kim | ok | ok | hộp quà Halloween / World Cup (createItemCoat có hạn) | mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:587` | Ruy băng kim cương | ok | ok | hộp quà Halloween / World Cup (createItemCoat có hạn); thưởng chuỗi chính (data_quest_moi.itemgift) | mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:588` | Ruy băng kim cương | ok | ok | hộp quà Halloween / World Cup (createItemCoat có hạn) | mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:589` | Ruy băng chuyên gia | ok | ok | hộp quà Halloween / World Cup (createItemCoat có hạn); thưởng chuỗi chính (data_quest_moi.itemgift) …(+1) | mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:590` | Ruy băng chuyên gia | ok | ok | hộp quà Halloween / World Cup (createItemCoat có hạn) | mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:591` | Ruy băng tinh anh | ok | ok | hộp quà Halloween / World Cup (createItemCoat có hạn) | mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:592` | Ruy băng tinh anh | ok | ok | hộp quà Halloween / World Cup (createItemCoat có hạn) | mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:593` | Ruy băng huyền thoại | ok | ok | hộp quà Halloween / World Cup (createItemCoat có hạn); thưởng chuỗi chính (data_quest_moi.itemgift) | mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:594` | Ruy băng huyền thoại | ok | ok | hộp quà Halloween / World Cup (createItemCoat có hạn) | mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:595` | Ruy băng huyền thoại | ok | ok | hộp quà Halloween / World Cup (createItemCoat có hạn) | mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:596` | Ruy băng huyền thoại | ok | ok | hộp quà Halloween / World Cup (createItemCoat có hạn) | mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:597` | Ruy băng huyền thoại | ok | ok | hộp quà Halloween / World Cup (createItemCoat có hạn) | mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:598` | Than lửa tinh anh | ok | ok | rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:599` | Than lửa huyền thoại | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); rơi trang bị theo dải cấp (alpha, Hoàn mỹ) (NoiDungLauDai.phatTrangBi) …(+2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:600` | Hạt giống thần tinh anh | ok | ok | rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:601` | Hạt giống thần huyền thoại | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); rơi trang bị theo dải cấp (alpha, Hoàn mỹ) (NoiDungLauDai.phatTrangBi) …(+2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:602` | Khăn lụa tinh anh | ok | ok | rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:603` | Khăn lụa huyền thoại | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); rơi trang bị theo dải cấp (alpha, Hoàn mỹ) (NoiDungLauDai.phatTrangBi) …(+2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:604` | Nước thần tinh anh | ok | ok | rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:605` | Nước thần huyền thoại | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); rơi trang bị theo dải cấp (alpha, Hoàn mỹ) (NoiDungLauDai.phatTrangBi) …(+2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:606` | Nam châm tinh anh | ok | ok | rơi từ quái theo cấp (Map.dropItem: id<214 hoặc 598..607, nloan=0, id≠2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:607` | Nam châm huyền thoại | ok | ok | Gym thắng lần đầu (GymLauDai.vuKhiHoanMy); rơi trang bị theo dải cấp (alpha, Hoàn mỹ) (NoiDungLauDai.phatTrangBi) …(+2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:611` | Rương giao hàng | ok | ok | vận tiêu (MonsterVantieu trong jar) | kiểm id vật phẩm (isRuongVanTieu); rương giao hàng (doResponseTieu) |
| `item:612` | Rương cướp hàng | ok | ok | vận tiêu (MonsterVantieu trong jar) | kiểm id vật phẩm (isRuongVanTieu); rương giao hàng (doResponseTieu) |
| `item:616` | Ruy băng lông thú | ok | ok | quay số / sự kiện 5 sao (chổi) | kiểm id vật phẩm (addGif); kiểm id vật phẩm (createAttributeItemCoat) …(+1) |
| `item:617` | Chổi băng | ok | ok | quay số / sự kiện 5 sao (chổi) | kiểm id vật phẩm (createAttChoi); kiểm id vật phẩm (createAttChoiNgauNhien) …(+2) |
| `item:618` | Chổi set | ok | ok | quay số / sự kiện 5 sao (chổi) | kiểm id vật phẩm (createAttChoi); kiểm id vật phẩm (createAttChoiNgauNhien) …(+3) |
| `item:619` | Chổi độc | ok | ok | quay số / sự kiện 5 sao (chổi) | kiểm id vật phẩm (createAttChoi); kiểm id vật phẩm (createAttChoiNgauNhien) …(+2) |
| `item:631` | Vé gọi Pokémon hỗ trợ 4x lv 2 | ok | ok | hộp/boss sự kiện (HoVeLenh) | bán cho NPC (OfflineMap.doSellItem); gọi Pokémon hỗ trợ (doUseItemLinhDanhThue) |
| `item:632` | Vé gọi Pokémon hỗ trợ 4x lv 3 | ok | ok | hộp/boss sự kiện (HoVeLenh) | bán cho NPC (OfflineMap.doSellItem); gọi Pokémon hỗ trợ (doUseItemLinhDanhThue) |
| `item:634` | Vé gọi Pokémon hỗ trợ 5x lv 2 | ok | ok | hộp/boss sự kiện (HoVeLenh) | bán cho NPC (OfflineMap.doSellItem); gọi Pokémon hỗ trợ (doUseItemLinhDanhThue) |
| `item:635` | Vé gọi Pokémon hỗ trợ 5x lv 3 | ok | ok | hộp/boss sự kiện (HoVeLenh) | bán cho NPC (OfflineMap.doSellItem); gọi Pokémon hỗ trợ (doUseItemLinhDanhThue) |
| `item:637` | Vé gọi Pokémon hỗ trợ 6x lv 2 | ok | ok | hộp/boss sự kiện (HoVeLenh) | bán cho NPC (OfflineMap.doSellItem); gọi Pokémon hỗ trợ (doUseItemLinhDanhThue) |
| `item:638` | Vé gọi Pokémon hỗ trợ 6x lv 3 | ok | ok | hộp/boss sự kiện (HoVeLenh) | bán cho NPC (OfflineMap.doSellItem); gọi Pokémon hỗ trợ (doUseItemLinhDanhThue) |
| `item:641` | Khăn quàng huyền thoại | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Trang bị (Bronzong, xu)); rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:646` | Đai huyền thoại | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Trang bị (Bronzong, xu)); rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi) …(+2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:649` | Băng đô huyền thoại | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Trang bị (Bronzong, xu)); rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi) …(+2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:656` | Vòng cổ huyền thoại | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)); rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi) …(+1) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:660` | Lông vũ tốc độ huyền thoại | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Trang bị (Bronzong, xu)); rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi) …(+1) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:664` | Găng đấm huyền thoại | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Trang bị (Bronzong, xu)); rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi) …(+2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:668` | Phiến đá hệ huyền thoại | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)); thưởng chuỗi chính (data_quest_moi.itemgift) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:675` | Thẻ mua bán | ok | ok | NPC bán trang bị (MessageCreator.ALL_ID_ITEM_NPC_SELL, OfflineMap.doBuyItem); thẻ mua bán: NPC bán + thành tựu VIP | bán cho NPC (OfflineMap.doSellItem); thẻ mua bán (dịch chuyển chợ) |
| `item:676` | Vé xe giao hàng 2x | ok | ok | boss Sơn Tinh / hộp quà sự kiện | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (doUseItemGoc) …(+1) |
| `item:677` | Vé xe giao hàng 3x | ok | ok | boss Sơn Tinh / hộp quà sự kiện | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (doUseItemGoc) …(+1) |
| `item:678` | Vé Mê cung 5x | ok | ok | quái vận tiêu / tiến giai 1% (MapCheTao.doAddNewAttBot) | bán cho NPC (OfflineMap.doSellItem); lệnh bài vào Tháp Pokémon (Char.getLenhBaiTranYeu) |
| `item:679` | Vé Mê cung 6x | ok | ok | quái vận tiêu / tiến giai 1% (MapCheTao.doAddNewAttBot) | bán cho NPC (OfflineMap.doSellItem); lệnh bài vào Tháp Pokémon (Char.getLenhBaiTranYeu) |
| `item:680` | Vé Mê cung 7x | ok | ok | quái vận tiêu / tiến giai 1% (MapCheTao.doAddNewAttBot) | bán cho NPC (OfflineMap.doSellItem); lệnh bài vào Tháp Pokémon (Char.getLenhBaiTranYeu) |
| `item:681` | Ponyta lửa | ok | ok | nở trứng Ponyta (doCreateHoaKyLan / NhaTre) | bán cho NPC (OfflineMap.doSellItem); thú cưỡi Hoả kỳ lân (menu -513 nâng cấp / ảo hoá) |
| `item:682` | Rapidash lửa | ok | ok | nâng cấp Hoả kỳ lân (doNangCapHoaKyLan) | thú cưỡi Hoả kỳ lân (menu -513 nâng cấp / ảo hoá) |
| `item:683` | Rapidash lửa Shiny | ok | ok | nâng cấp Hoả kỳ lân (doNangCapHoaKyLan) | thú cưỡi Hoả kỳ lân (menu -513 nâng cấp / ảo hoá) |
| `item:684` | Trứng Hoothoot | ok | ok | trứng pet rơi sự kiện hè / boss (Monster.droptrungPet) | ấp trứng ra pet (doCreatePet) |
| `item:685` | Trứng Dratini | ok | ok | trứng pet rơi sự kiện hè / boss (Monster.droptrungPet) | ấp trứng ra pet (doCreatePet) |
| `item:686` | Trứng Cleffa | ok | ok | trứng pet rơi sự kiện hè / boss (Monster.droptrungPet) | ấp trứng ra pet (doCreatePet) |
| `item:687` | Trứng Zubat | ok | ok | trứng pet rơi sự kiện hè / boss (Monster.droptrungPet) | ấp trứng ra pet (doCreatePet) |
| `item:688` | Trứng Rufflet | ok | ok | trứng pet rơi sự kiện hè / boss (Monster.droptrungPet) | ấp trứng ra pet (doCreatePet) |
| `item:689` | Hoothoot cấp 1 | ok | ok | nở trứng / nâng cấp pet (Char.doCreatePet, Pet.doUpdrage idItemUpLevel) | pet đi theo (trang bị thú) |
| `item:690` | Hoothoot cấp 2 | ok | ok | nở trứng / nâng cấp pet (Char.doCreatePet, Pet.doUpdrage idItemUpLevel) | pet đi theo (trang bị thú) |
| `item:691` | Noctowl cấp 3 | ok | ok | nở trứng / nâng cấp pet (Char.doCreatePet, Pet.doUpdrage idItemUpLevel) | pet đi theo (trang bị thú) |
| `item:692` | Noctowl cấp 4 | ok | ok | nở trứng / nâng cấp pet (Char.doCreatePet, Pet.doUpdrage idItemUpLevel) | pet đi theo (trang bị thú) |
| `item:693` | Noctowl Shiny cấp 5 | ok | ok | nở trứng / nâng cấp pet (Char.doCreatePet, Pet.doUpdrage idItemUpLevel) | pet đi theo (trang bị thú) |
| `item:694` | Dratini cấp 1 | ok | ok | nở trứng / nâng cấp pet (Char.doCreatePet, Pet.doUpdrage idItemUpLevel) | pet đi theo (trang bị thú) |
| `item:695` | Dratini cấp 2 | ok | ok | nở trứng / nâng cấp pet (Char.doCreatePet, Pet.doUpdrage idItemUpLevel) | pet đi theo (trang bị thú) |
| `item:696` | Dragonair cấp 3 | ok | ok | nở trứng / nâng cấp pet (Char.doCreatePet, Pet.doUpdrage idItemUpLevel) | pet đi theo (trang bị thú) |
| `item:697` | Dragonair cấp 4 | ok | ok | nở trứng / nâng cấp pet (Char.doCreatePet, Pet.doUpdrage idItemUpLevel) | pet đi theo (trang bị thú) |
| `item:698` | Dragonite cấp 5 | ok | ok | nở trứng / nâng cấp pet (Char.doCreatePet, Pet.doUpdrage idItemUpLevel) | pet đi theo (trang bị thú) |
| `item:699` | Cleffa cấp 1 | ok | ok | nở trứng / nâng cấp pet (Char.doCreatePet, Pet.doUpdrage idItemUpLevel) | pet đi theo (trang bị thú) |
| `item:700` | Clefairy cấp 2 | ok | ok | nở trứng / nâng cấp pet (Char.doCreatePet, Pet.doUpdrage idItemUpLevel) | pet đi theo (trang bị thú) |
| `item:701` | Clefairy cấp 3 | ok | ok | nở trứng / nâng cấp pet (Char.doCreatePet, Pet.doUpdrage idItemUpLevel) | pet đi theo (trang bị thú) |
| `item:702` | Clefable cấp 4 | ok | ok | nở trứng / nâng cấp pet (Char.doCreatePet, Pet.doUpdrage idItemUpLevel) | pet đi theo (trang bị thú) |
| `item:703` | Clefable Shiny cấp 5 | ok | ok | nở trứng / nâng cấp pet (Char.doCreatePet, Pet.doUpdrage idItemUpLevel) | pet đi theo (trang bị thú) |
| `item:704` | Zubat cấp 1 | ok | ok | nở trứng / nâng cấp pet (Char.doCreatePet, Pet.doUpdrage idItemUpLevel) | pet đi theo (trang bị thú) |
| `item:705` | Zubat cấp 2 | ok | ok | nở trứng / nâng cấp pet (Char.doCreatePet, Pet.doUpdrage idItemUpLevel) | pet đi theo (trang bị thú) |
| `item:706` | Golbat cấp 3 | ok | ok | nở trứng / nâng cấp pet (Char.doCreatePet, Pet.doUpdrage idItemUpLevel) | pet đi theo (trang bị thú) |
| `item:707` | Golbat cấp 4 | ok | ok | nở trứng / nâng cấp pet (Char.doCreatePet, Pet.doUpdrage idItemUpLevel) | pet đi theo (trang bị thú) |
| `item:708` | Crobat cấp 5 | ok | ok | nở trứng / nâng cấp pet (Char.doCreatePet, Pet.doUpdrage idItemUpLevel) | pet đi theo (trang bị thú) |
| `item:709` | Rufflet cấp 1 | ok | ok | nở trứng / nâng cấp pet (Char.doCreatePet, Pet.doUpdrage idItemUpLevel) | pet đi theo (trang bị thú) |
| `item:710` | Rufflet cấp 2 | ok | ok | nở trứng / nâng cấp pet (Char.doCreatePet, Pet.doUpdrage idItemUpLevel) | pet đi theo (trang bị thú) |
| `item:711` | Braviary cấp 3 | ok | ok | nở trứng / nâng cấp pet (Char.doCreatePet, Pet.doUpdrage idItemUpLevel) | pet đi theo (trang bị thú) |
| `item:712` | Braviary cấp 4 | ok | ok | nở trứng / nâng cấp pet (Char.doCreatePet, Pet.doUpdrage idItemUpLevel) | pet đi theo (trang bị thú) |
| `item:713` | Braviary Shiny cấp 5 | ok | ok | nở trứng / nâng cấp pet (Char.doCreatePet, Pet.doUpdrage idItemUpLevel) | pet đi theo (trang bị thú) |
| `item:714` | Kẹo Poké | ok | ok | Kẹo Poké: cửa hàng thú nuôi / hộp | cho thú nuôi ăn (Kẹo Poké, Item.getExpPetEatfromItem); kiểm id vật phẩm (getExpPetEatfromItem) …(+1) |
| `item:715` | Than lửa bạch kim | ok | ok | boss Người Tuyết / pháo hoa Tết (vũ khí thời trang có hạn) | mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:716` | Hạt giống thần bạch kim | ok | ok | boss Người Tuyết / pháo hoa Tết (vũ khí thời trang có hạn) | mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:717` | Khăn lụa bạch kim | ok | ok | boss Người Tuyết / pháo hoa Tết (vũ khí thời trang có hạn) | mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:718` | Nước thần bạch kim | ok | ok | boss Người Tuyết / pháo hoa Tết (vũ khí thời trang có hạn) | mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:719` | Nam châm bạch kim | ok | ok | boss Người Tuyết / pháo hoa Tết (vũ khí thời trang có hạn) | mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:720` | Kẹo thú cưng. | ok | ok | rương may mắn / boss (Huyết linh thảo 720, sách kỹ năng 721) | kiểm id vật phẩm (isHuyetLinhThao); tiến hoá trạng thái thú nuôi (isHuyetLinhThao → Pet.doTienHoa) |
| `item:721` | Bạc hà tính cách | ok | ok | rương may mắn / boss (Huyết linh thảo 720, sách kỹ năng 721) | kiểm id vật phẩm (isHuyetBoDe); đổi độ thông thạo thú nuôi (isHuyetBoDe) |
| `item:723` | Áo Zoroark | ok | ok | Shop Đặc Biệt (VIP, 3 giờ) | kiểm id vật phẩm (isCaiTaoSonTinh); kiểm id vật phẩm (isHacLang) …(+1) |
| `item:724` | Áo Gardevoir | ok | ok | Shop Đặc Biệt (VIP, 3 giờ) | kiểm id vật phẩm (isCaiTaoMiNuong); kiểm id vật phẩm (isThanNu) …(+1) |
| `item:727` | Pachirisu | ngu | ok | trứng nhà trẻ nở (NhaTre.LOAI_TRUNG) | pet đi theo (trang bị thú) |
| `item:728` | Delibird | ngu | ok | trứng nhà trẻ nở (NhaTre.LOAI_TRUNG) | kiểm id vật phẩm (isDuongQua); kiểm id vật phẩm (isGayGiangSinh) …(+1) |
| `item:736` | Áo Groudon | ok | ok | cửa hàng lượng (data_shop slot thời trang) | kiểm id vật phẩm (isSonTinh); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:737` | Áo Kyogre | ok | ok | cửa hàng lượng (data_shop slot thời trang) | kiểm id vật phẩm (isCaiTaoMiNuong); kiểm id vật phẩm (isThuyTinh) …(+1) |
| `item:740` | Áo Latias | ok | ok | cửa hàng lượng (data_shop slot thời trang) | kiểm id vật phẩm (isTieuLongNu); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:741` | Áo Latios | ok | ok | cửa hàng lượng (data_shop slot thời trang) | kiểm id vật phẩm (isDuongQua); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:742` | Ruy băng lông công | ok | ok | hộp quà Noel / Halloween / Vu Lan (mặt nạ, phi phong) | kiểm id vật phẩm (createAttributeItemCoat); kiểm id vật phẩm (isPhiPhongKhongTuoc) …(+1) |
| `item:743` | Áo Lunala | ok | ok | cửa hàng lượng (data_shop slot thời trang) | kiểm id vật phẩm (getInfoAtbItem); kiểm id vật phẩm (isTienNu) …(+1) |
| `item:744` | Áo Solgaleo | ok | ok | cửa hàng lượng (data_shop slot thời trang) | kiểm id vật phẩm (getInfoAtbItem); kiểm id vật phẩm (isTinhQuan) …(+1) |
| `item:745` | Mặt nạ Pumpkaboo | ok | ok | hộp quà Noel / Halloween / Vu Lan (mặt nạ, phi phong) | kiểm id vật phẩm (isMatNaBiNgo); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:746` | Mặt nạ Haunter | ok | ok | hộp quà Noel / Halloween / Vu Lan (mặt nạ, phi phong) | kiểm id vật phẩm (isMatNaQuy); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:747` | Mặt nạ Houndoom | ok | ok | hộp quà Noel / Halloween / Vu Lan (mặt nạ, phi phong) | kiểm id vật phẩm (isMatNaSoi); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:748` | Mặt nạ Yamask | ok | ok | hộp quà Noel / Halloween / Vu Lan (mặt nạ, phi phong) | kiểm id vật phẩm (isMatNaZoombi); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:749` | Mặt nạ Gengar | ok | ok | hộp quà Noel / Halloween / Vu Lan (mặt nạ, phi phong) | kiểm id vật phẩm (isMatNaPanda); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:750` | Ruy Băng Hoenn | ok | ok | hộp quà Noel / Halloween / Vu Lan (mặt nạ, phi phong) | kiểm id vật phẩm (createAttributeItemCoat); kiểm id vật phẩm (isPhiPhongHuyenVu) …(+1) |
| `item:752` | Rương nguyên liệu cao cấp | ok | ok | Shop Đặc Biệt (VIP) | kiểm id vật phẩm (doUseItemGoc); kiểm id vật phẩm (isRuongNguyenLieu) …(+1) |
| `item:753` | Rương nguyên liệu sơ cấp | ok | ok | Shop Đặc Biệt (VIP) | kiểm id vật phẩm (doUseItemGoc); kiểm id vật phẩm (isRuongNguyenLieu) …(+1) |
| `item:754` | Áo dài nam | ok | ok | cửa hàng lượng (data_shop slot thời trang) | kiểm id vật phẩm (isAoDai2021); kiểm id vật phẩm (isAoDaiNam) …(+1) |
| `item:755` | Áo dài nữ | ok | ok | cửa hàng lượng (data_shop slot thời trang) | kiểm id vật phẩm (isAoDai2021); kiểm id vật phẩm (isAoDaiNu) …(+2) |
| `item:791` | Bóng đá nam | ok | ok | cửa hàng lượng (data_shop slot thời trang) | kiểm id vật phẩm (isBongDaNam); kiểm id vật phẩm (isBongDaNamAoCam) …(+1) |
| `item:792` | Bóng đá nữ | ok | ok | cửa hàng lượng (data_shop slot thời trang) | kiểm id vật phẩm (isBongDaNu); kiểm id vật phẩm (isBongDaNuAoSoc) …(+1) |
| `item:803` | Litleo | ngu | ok | trứng nhà trẻ nở (NhaTre.LOAI_TRUNG) | pet đi theo (trang bị thú) |
| `item:804` | Growlithe | ngu | ok | trứng nhà trẻ nở (NhaTre.LOAI_TRUNG) | pet đi theo (trang bị thú) |
| `item:832` | Deerling | ngu | ok | trứng nhà trẻ nở (NhaTre.LOAI_TRUNG) | pet đi theo (trang bị thú) |
| `item:848` | Mảnh áo Emboar | ok | ok | quay số / giftcode / boss Trung thu (mảnh, cánh) | trang bị bộ sưu tập / cánh (ModelWearing, ghép mảnh) |
| `item:849` | Mảnh quần Emboar | ok | ok | quay số / giftcode / boss Trung thu (mảnh, cánh) | trang bị bộ sưu tập / cánh (ModelWearing, ghép mảnh) |
| `item:850` | Mảnh vòng tay Emboar | ok | ok | quay số / giftcode / boss Trung thu (mảnh, cánh) | trang bị bộ sưu tập / cánh (ModelWearing, ghép mảnh) |
| `item:851` | Mảnh vòng đầu Emboar | ok | ok | quay số / giftcode / boss Trung thu (mảnh, cánh) | trang bị bộ sưu tập / cánh (ModelWearing, ghép mảnh) |
| `item:852` | Mảnh giày Emboar | ok | ok | quay số / giftcode / boss Trung thu (mảnh, cánh) | trang bị bộ sưu tập / cánh (ModelWearing, ghép mảnh) |
| `item:853` | Mảnh găng Emboar | ok | ok | quay số / giftcode / boss Trung thu (mảnh, cánh) | trang bị bộ sưu tập / cánh (ModelWearing, ghép mảnh) |
| `item:854` | Mảnh áo Swampert | ok | ok | quay số / giftcode / boss Trung thu (mảnh, cánh) | trang bị bộ sưu tập / cánh (ModelWearing, ghép mảnh) |
| `item:855` | Mảnh quần Swampert | ok | ok | quay số / giftcode / boss Trung thu (mảnh, cánh) | trang bị bộ sưu tập / cánh (ModelWearing, ghép mảnh) |
| `item:856` | Mảnh vòng tay Swampert | ok | ok | quay số / giftcode / boss Trung thu (mảnh, cánh) | trang bị bộ sưu tập / cánh (ModelWearing, ghép mảnh) |
| `item:857` | Mảnh vòng đầu Swampert | ok | ok | quay số / giftcode / boss Trung thu (mảnh, cánh) | trang bị bộ sưu tập / cánh (ModelWearing, ghép mảnh) |
| `item:858` | Mảnh giày Swampert | ok | ok | quay số / giftcode / boss Trung thu (mảnh, cánh) | trang bị bộ sưu tập / cánh (ModelWearing, ghép mảnh) |
| `item:859` | Mảnh găng Swampert | ok | ok | quay số / giftcode / boss Trung thu (mảnh, cánh) | trang bị bộ sưu tập / cánh (ModelWearing, ghép mảnh) |
| `item:860` | Áo Emboar | ok | ok | ghép 200 mảnh → đồ bộ sưu tập; đủ bộ → cải trang (Map, Char.createItemSuuTap) | trang bị bộ sưu tập / cánh (ModelWearing, ghép mảnh) |
| `item:861` | Quần Emboar | ok | ok | ghép 200 mảnh → đồ bộ sưu tập; đủ bộ → cải trang (Map, Char.createItemSuuTap) | trang bị bộ sưu tập / cánh (ModelWearing, ghép mảnh) |
| `item:862` | Vòng tay Emboar | ok | ok | ghép 200 mảnh → đồ bộ sưu tập; đủ bộ → cải trang (Map, Char.createItemSuuTap) | trang bị bộ sưu tập / cánh (ModelWearing, ghép mảnh) |
| `item:863` | Vòng đầu Emboar | ok | ok | ghép 200 mảnh → đồ bộ sưu tập; đủ bộ → cải trang (Map, Char.createItemSuuTap) | trang bị bộ sưu tập / cánh (ModelWearing, ghép mảnh) |
| `item:864` | Giày Emboar | ok | ok | ghép 200 mảnh → đồ bộ sưu tập; đủ bộ → cải trang (Map, Char.createItemSuuTap) | trang bị bộ sưu tập / cánh (ModelWearing, ghép mảnh) |
| `item:865` | Găng Emboar | ok | ok | ghép 200 mảnh → đồ bộ sưu tập; đủ bộ → cải trang (Map, Char.createItemSuuTap) | trang bị bộ sưu tập / cánh (ModelWearing, ghép mảnh) |
| `item:866` | Áo Swampert | ok | ok | ghép 200 mảnh → đồ bộ sưu tập; đủ bộ → cải trang (Map, Char.createItemSuuTap) | trang bị bộ sưu tập / cánh (ModelWearing, ghép mảnh) |
| `item:867` | Quần Swampert | ok | ok | ghép 200 mảnh → đồ bộ sưu tập; đủ bộ → cải trang (Map, Char.createItemSuuTap) | trang bị bộ sưu tập / cánh (ModelWearing, ghép mảnh) |
| `item:868` | Vòng tay Swampert | ok | ok | ghép 200 mảnh → đồ bộ sưu tập; đủ bộ → cải trang (Map, Char.createItemSuuTap) | trang bị bộ sưu tập / cánh (ModelWearing, ghép mảnh) |
| `item:869` | Vòng đầu Swampert | ok | ok | ghép 200 mảnh → đồ bộ sưu tập; đủ bộ → cải trang (Map, Char.createItemSuuTap) | trang bị bộ sưu tập / cánh (ModelWearing, ghép mảnh) |
| `item:870` | Giày Swampert | ok | ok | ghép 200 mảnh → đồ bộ sưu tập; đủ bộ → cải trang (Map, Char.createItemSuuTap) | trang bị bộ sưu tập / cánh (ModelWearing, ghép mảnh) |
| `item:871` | Găng Swampert | ok | ok | ghép 200 mảnh → đồ bộ sưu tập; đủ bộ → cải trang (Map, Char.createItemSuuTap) | trang bị bộ sưu tập / cánh (ModelWearing, ghép mảnh) |
| `item:872` | Mặt nạ Snorlax | ok | ok | hộp quà Noel / Halloween / Vu Lan (mặt nạ, phi phong) | kiểm id vật phẩm (isMatNaOngDia); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:873` | Bidoof | ngu | ok | trứng nhà trẻ nở (NhaTre.LOAI_TRUNG) | pet đi theo (trang bị thú) |
| `item:890` | Quạt lá Shiftry | ok | ok | Thợ rèn bí ẩn chế thần khí (ThoRenBiAn.CreateThanKhi) | kiểm id vật phẩm (Option); kiểm id vật phẩm (RerollThanKhiAttribute) …(+4) |
| `item:891` | Gậy xương Marowak | ok | ok | Thợ rèn bí ẩn chế thần khí (ThoRenBiAn.CreateThanKhi) | kiểm id vật phẩm (Option); kiểm id vật phẩm (RerollThanKhiAttribute) …(+4) |
| `item:892` | Lưỡi liềm Scyther | ok | ok | Thợ rèn bí ẩn chế thần khí (ThoRenBiAn.CreateThanKhi) | kiểm id vật phẩm (Option); kiểm id vật phẩm (RerollThanKhiAttribute) …(+4) |
| `item:898` | Cánh Articuno | ok | ok | quay số / giftcode / boss Trung thu (mảnh, cánh) | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (addGif) …(+1) |
| `item:899` | Cánh Lugia | ok | ok | quay số / giftcode / boss Trung thu (mảnh, cánh) | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (addGif) …(+1) |
| `item:900` | Cánh Moltres | ok | ok | quay số / giftcode / boss Trung thu (mảnh, cánh) | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (addGif) …(+1) |
| `item:901` | Cánh Togekiss | ok | ok | quay số / giftcode / boss Trung thu (mảnh, cánh) | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (addGif) …(+1) |
| `item:902` | Cánh Crobat | ok | ok | quay số / giftcode / boss Trung thu (mảnh, cánh) | bán cho NPC (OfflineMap.doSellItem); kiểm id vật phẩm (addGif) …(+1) |
| `item:903` | Cải Trang Emboar | ok | ok | ghép 200 mảnh → đồ bộ sưu tập; đủ bộ → cải trang (Map, Char.createItemSuuTap) | kiểm id vật phẩm (doBuyItemShopNew); kiểm id vật phẩm (doPopup) …(+1) |
| `item:904` | Cải Trang Swampert | ok | ok | ghép 200 mảnh → đồ bộ sưu tập; đủ bộ → cải trang (Map, Char.createItemSuuTap) | kiểm id vật phẩm (doBuyItemShopNew); kiểm id vật phẩm (doPopup) …(+1) |
| `item:905` | Skin thú cưỡi Copperajah | ok | ok | đổi điểm thưởng (MapQuaTang case 15..19) / giftcode | skin linh thú (SkinThuCuoi) |
| `item:906` | Skin thú cưỡi Pangoro | ok | ok | đổi điểm thưởng (MapQuaTang case 15..19) / giftcode | skin linh thú (SkinThuCuoi) |
| `item:907` | Skin thú cưỡi Xerneas | ok | ok | đổi điểm thưởng (MapQuaTang case 15..19) / giftcode | skin linh thú (SkinThuCuoi) |
| `item:908` | Skin thú cưỡi Camerupt | ok | ok | đổi điểm thưởng (MapQuaTang case 15..19) / giftcode | skin linh thú (SkinThuCuoi) |
| `item:909` | Skin thú cưỡi Lycanroc | ok | ok | đổi điểm thưởng (MapQuaTang case 15..19) / giftcode | skin linh thú (SkinThuCuoi) |
| `item:911` | Skin thú cưỡi Dialga | ok | ok | đổi điểm thưởng (MapQuaTang case 15..19) / giftcode | skin linh thú (SkinThuCuoi) |
| `item:960` | Đá Nước | ok | ok | Pokédex mốc 50/100 (đá ngẫu nhiên); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi Huy hiệu ngày cộng đồng) …(+1) | đá tiến hoá Eevee (TienHoaPokemon.dungDaTienHoa) |
| `item:961` | Đá Sấm | ok | ok | Pokédex mốc 50/100 (đá ngẫu nhiên); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi Huy hiệu ngày cộng đồng) …(+1) | đá tiến hoá Eevee (TienHoaPokemon.dungDaTienHoa) |
| `item:962` | Đá Lửa | ok | ok | Pokédex mốc 50/100 (đá ngẫu nhiên); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi Huy hiệu ngày cộng đồng) …(+1) | đá tiến hoá Eevee (TienHoaPokemon.dungDaTienHoa) |
| `item:963` | Đá Mặt Trời | ok | ok | Pokédex mốc 50/100 (đá ngẫu nhiên); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi Huy hiệu ngày cộng đồng) …(+1) | đá tiến hoá Eevee (TienHoaPokemon.dungDaTienHoa) |
| `item:964` | Đá Mặt Trăng | ok | ok | Pokédex mốc 50/100 (đá ngẫu nhiên); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi Huy hiệu ngày cộng đồng) …(+1) | đá tiến hoá Eevee (TienHoaPokemon.dungDaTienHoa) |
| `item:965` | Đá Lá | ok | ok | Pokédex mốc 50/100 (đá ngẫu nhiên); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi Huy hiệu ngày cộng đồng) …(+1) | đá tiến hoá Eevee (TienHoaPokemon.dungDaTienHoa) |
| `item:966` | Đá Băng | ok | ok | Pokédex mốc 50/100 (đá ngẫu nhiên); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi Huy hiệu ngày cộng đồng) …(+1) | đá tiến hoá Eevee (TienHoaPokemon.dungDaTienHoa) |
| `item:967` | Ruy Băng Tiên | ok | ok | Pokédex mốc 50/100 (đá ngẫu nhiên); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi Huy hiệu ngày cộng đồng) …(+1) | đá tiến hoá Eevee (TienHoaPokemon.dungDaTienHoa) |
| `item:1001` | Ruy băng sơ cấp | ok | ok | thưởng chuỗi chính (data_quest_moi.itemgift) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:1002` | Ruy băng đồng | ok | ok | thưởng chuỗi chính (data_quest_moi.itemgift) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:1003` | Ruy băng bạc | ok | ok | thưởng chuỗi chính (data_quest_moi.itemgift); thưởng nhiệm vụ (NhiemVuEngine.traThuong) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:1004` | Khăn quàng thần thoại | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Trang bị (Bronzong, xu)); rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi) …(+2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:1005` | Khăn quàng thần thoại | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Trang bị (Bronzong, xu)) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:1006` | Đai thần thoại | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Trang bị (Bronzong, xu)); rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi) …(+1) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:1007` | Băng đô thần thoại | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Trang bị (Bronzong, xu)); rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi) …(+1) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:1008` | Than lửa thần thoại | ok | ok | rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi); rơi trang bị theo dải cấp (alpha, Hoàn mỹ) (NoiDungLauDai.phatTrangBi) …(+2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:1009` | Hạt giống thần thần thoại | ok | ok | rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi); rơi trang bị theo dải cấp (alpha, Hoàn mỹ) (NoiDungLauDai.phatTrangBi) …(+2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:1010` | Khăn lụa thần thoại | ok | ok | rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi); rơi trang bị theo dải cấp (alpha, Hoàn mỹ) (NoiDungLauDai.phatTrangBi) …(+2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:1011` | Nước thần thần thoại | ok | ok | rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi); rơi trang bị theo dải cấp (alpha, Hoàn mỹ) (NoiDungLauDai.phatTrangBi) …(+2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:1012` | Nam châm thần thoại | ok | ok | rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi); rơi trang bị theo dải cấp (alpha, Hoàn mỹ) (NoiDungLauDai.phatTrangBi) …(+2) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:1013` | Huy hiệu thần thoại | ok | ok | thưởng chuỗi chính (data_quest_moi.itemgift); thưởng nhiệm vụ (NhiemVuEngine.traThuong) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:1014` | Vòng cổ thần thoại | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)); rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi) …(+1) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:1015` | Lông vũ tốc độ thần thoại | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Trang bị (Bronzong, xu)); rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi) …(+1) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:1016` | Găng đấm thần thoại | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Trang bị (Bronzong, xu)); rơi trang bị theo dải cấp (NoiDungLauDai.phatTrangBi) …(+1) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:1017` | Phiến đá hệ thần thoại | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)); thưởng nhiệm vụ (NhiemVuEngine.traThuong) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:1018` | Ruy băng thần thoại | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)); thưởng nhiệm vụ (NhiemVuEngine.traThuong) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:1019` | Khăn quàng tối thượng | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Trang bị (Bronzong, xu)); thưởng nhiệm vụ (NhiemVuEngine.traThuong) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:1020` | Khăn quàng tối thượng | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Trang bị (Bronzong, xu)) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:1021` | Đai tối thượng | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Trang bị (Bronzong, xu)); thưởng nhiệm vụ (NhiemVuEngine.traThuong) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:1022` | Băng đô tối thượng | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Trang bị (Bronzong, xu)); thưởng nhiệm vụ (NhiemVuEngine.traThuong) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:1023` | Than lửa tối thượng | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)); thưởng nhiệm vụ (NhiemVuEngine.traThuong) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:1024` | Hạt giống thần tối thượng | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)); thưởng nhiệm vụ (NhiemVuEngine.traThuong) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:1025` | Khăn lụa tối thượng | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)); thưởng nhiệm vụ (NhiemVuEngine.traThuong) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:1026` | Nước thần tối thượng | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)); thưởng nhiệm vụ (NhiemVuEngine.traThuong) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:1027` | Nam châm tối thượng | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)); thưởng nhiệm vụ (NhiemVuEngine.traThuong) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:1028` | Huy hiệu tối thượng | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)); thưởng nhiệm vụ (NhiemVuEngine.traThuong) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:1029` | Vòng cổ tối thượng | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)); thưởng nhiệm vụ (NhiemVuEngine.traThuong) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:1030` | Lông vũ tốc độ tối thượng | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Trang bị (Bronzong, xu)); thưởng chuỗi chính (data_quest_moi.itemgift) …(+1) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:1031` | Găng đấm tối thượng | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Trang bị (Bronzong, xu)); thưởng nhiệm vụ (NhiemVuEngine.traThuong) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:1032` | Phiến đá hệ tối thượng | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)); thưởng nhiệm vụ (NhiemVuEngine.traThuong) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:1033` | Ruy băng tối thượng | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Chế tạo (Machamp)); thưởng nhiệm vụ (NhiemVuEngine.traThuong) | bán cho NPC (OfflineMap.doSellItem); mặc trang bị (doUseItem → MapMuaBan.exchangeItem) |
| `item:1034` | Bụi sao | ok | ok | rơi theo dải cấp; thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) | nguyên liệu chế tạo: Phiến đá hệ bạc; nguyên liệu chế tạo: Phiến đá hệ sơ cấp …(+5) |
| `item:1035` | Mảnh sao | ok | ok | rơi theo dải cấp; thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) | nguyên liệu chế tạo: Phiến đá hệ bạch kim; nguyên liệu chế tạo: Phiến đá hệ kim cương …(+5) |
| `item:1036` | Mảnh thiên thạch | ok | ok | rơi theo dải cấp; thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) | nguyên liệu chế tạo: Phiến đá hệ chuyên gia; nguyên liệu chế tạo: Phiến đá hệ huyền thoại …(+5) |
| `item:1037` | Vảy rồng | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Đổi tín vật huyền thoại); mở item:1051 (NoiDungLauDai.moHop) …(+1) | nguyên liệu chế tạo: Huy hiệu tối thượng; nguyên liệu chế tạo: Hạt giống thần tối thượng …(+11) |
| `item:1038` | Mảnh tinh hoa | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Đổi tín vật huyền thoại); mở item:1051 (NoiDungLauDai.moHop) …(+1) | Mảnh tinh hoa: nâng cấp +11..+15 đồ Hoàn mỹ (NangCapLauDaiGame); nguyên liệu chế tạo: Huy hiệu tối thượng …(+7) |
| `item:1039` | Huy chương Gym | ok | ok | Gym đánh lại hằng tuần (GymLauDai.khiHa); thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) | đổi tại Sổ tay → Cửa hàng: 2 Thuốc chữa toàn diện; đổi tại Sổ tay → Cửa hàng: Băng đô tập trung …(+4) |
| `item:1040` | Lông vũ Zapdos | ok | ok | boss thế giới theo tuần (NoiDungLauDai.roiBossTheGioi); thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) | đổi tại Sổ tay → Cửa hàng: Mảnh tinh hoa; đổi tại Sổ tay → Cửa hàng: Vảy rồng |
| `item:1041` | Lông vũ Articuno | ok | ok | boss thế giới năm 2 (NoiDungLauDai.TIN_VAT_NAM2); thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) | đổi tại Sổ tay → Cửa hàng: Mảnh tinh hoa; đổi tại Sổ tay → Cửa hàng: Vảy rồng |
| `item:1042` | Nanh lửa Entei | ok | ok | boss thế giới theo tuần (NoiDungLauDai.roiBossTheGioi); thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) | đổi tại Sổ tay → Cửa hàng: Mảnh tinh hoa; đổi tại Sổ tay → Cửa hàng: Vảy rồng |
| `item:1043` | Lông tơ Raikou | ok | ok | boss thế giới năm 2 (NoiDungLauDai.TIN_VAT_NAM2); thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) | đổi tại Sổ tay → Cửa hàng: Mảnh tinh hoa; đổi tại Sổ tay → Cửa hàng: Vảy rồng |
| `item:1044` | Mảnh bóng tối | ok | ok | boss thế giới theo tuần (NoiDungLauDai.roiBossTheGioi); thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) | đổi tại Sổ tay → Cửa hàng: Mảnh tinh hoa; đổi tại Sổ tay → Cửa hàng: Vảy rồng |
| `item:1045` | Pha lê Suicune | ok | ok | boss thế giới năm 2 (NoiDungLauDai.TIN_VAT_NAM2); thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) | đổi tại Sổ tay → Cửa hàng: Mảnh tinh hoa; đổi tại Sổ tay → Cửa hàng: Vảy rồng |
| `item:1046` | Vảy Rayquaza | ok | ok | boss thế giới theo tuần (NoiDungLauDai.roiBossTheGioi); thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) | đổi tại Sổ tay → Cửa hàng: Mảnh tinh hoa; đổi tại Sổ tay → Cửa hàng: Vảy rồng |
| `item:1047` | Lông bạc Lugia | ok | ok | boss thế giới năm 2 (NoiDungLauDai.TIN_VAT_NAM2); thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) | đổi tại Sổ tay → Cửa hàng: Mảnh tinh hoa; đổi tại Sổ tay → Cửa hàng: Vảy rồng |
| `item:1048` | Ngọc đỏ Groudon | ok | ok | boss thế giới theo tuần (NoiDungLauDai.roiBossTheGioi); thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) | đổi tại Sổ tay → Cửa hàng: Mảnh tinh hoa; đổi tại Sổ tay → Cửa hàng: Vảy rồng |
| `item:1049` | Ngọc lam Kyogre | ok | ok | boss thế giới năm 2 (NoiDungLauDai.TIN_VAT_NAM2) | đổi tại Sổ tay → Cửa hàng: Mảnh tinh hoa; đổi tại Sổ tay → Cửa hàng: Vảy rồng |
| `item:1050` | Hộp tham gia đột kích | ok | ok | boss thế giới theo tuần (NoiDungLauDai.roiBossTheGioi) | mở hộp (VatPhamDungLauDai.moHop) |
| `item:1051` | Hộp vàng đột kích | ok | ok | boss thế giới theo tuần (NoiDungLauDai.roiBossTheGioi) | mở hộp (VatPhamDungLauDai.moHop) |
| `item:1052` | Tinh thể hệ | ok | ok | thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) | đổi tại Sổ tay → Cửa hàng: Thuốc chữa toàn diện; đổi tại Sổ tay → Cửa hàng: X Tốc độ + X Chính xác |
| `item:1053` | Huy hiệu ngày cộng đồng | ok | ok | thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) | đổi tại Sổ tay → Cửa hàng: Ruy Băng Tiên; đổi tại Sổ tay → Cửa hàng: Đá Băng …(+6) |
| `item:1054` | Tem nghiên cứu | ok | ok | thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) | đổi tại Sổ tay → Cửa hàng: Kẹo hiếm; đổi tại Sổ tay → Cửa hàng: Trứng may mắn |
| `item:1055` | Huy chương mùa giải | ok | ok | thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) | đổi tại Sổ tay → Cửa hàng: 2 Tăng chí mạng; đổi tại Sổ tay → Cửa hàng: Móng vuốt nhanh …(+1) |
| `item:1056` | Cờ vùng | ok | ok | thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) | đổi tại Sổ tay → Cửa hàng: Thuốc hồi sinh tối đa |
| `item:1057` | Quà lễ 30/4 - 1/5 | ok | ok | thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) | đổi tại Sổ tay → Cửa hàng: Quà 30/4: 3 Quả Oran, 2 Quả Cheri |
| `item:1058` | Quà lễ Quốc khánh 2/9 | ok | ok | thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) | đổi tại Sổ tay → Cửa hàng: Quà 2/9: 2 Quả Sitrus, 2 Quả Leppa |
| `item:1059` | Quà lễ Phụ nữ 20/10 | ok | ok | thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) | đổi tại Sổ tay → Cửa hàng: Quà 20/10: 2 Quả Lum, 2 Quả Aspear |
| `item:1060` | Quà lễ Nhà giáo 20/11 | ok | ok | thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) | đổi tại Sổ tay → Cửa hàng: Quà 20/11: 6 quả mọng |
| `item:1061` | Phiếu giảm giá trang trí | ok | ok | thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) | đổi tại Sổ tay → Cửa hàng: 2 Thuốc xua quái |
| `item:1201` | Thuốc giải độc | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)) | bán cho NPC (OfflineMap.doSellItem); dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai) |
| `item:1202` | Thuốc trị bỏng | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)) | bán cho NPC (OfflineMap.doSellItem); dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai) |
| `item:1203` | Thuốc trị tê liệt | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)) | bán cho NPC (OfflineMap.doSellItem); dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai) |
| `item:1204` | Thuốc đánh thức | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)) | bán cho NPC (OfflineMap.doSellItem); dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai) |
| `item:1205` | Thuốc rã đông | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)) | bán cho NPC (OfflineMap.doSellItem); dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai) |
| `item:1206` | Thuốc chữa toàn diện | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi Huy chương Gym) …(+1) | bán cho NPC (OfflineMap.doSellItem); dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai) |
| `item:1207` | Thuốc hồi sinh tối đa | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Lượng khoá (Kecleon)); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi vật phẩm sự kiện) | dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai) |
| `item:1208` | X Tốc độ | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi vật phẩm sự kiện) | bán cho NPC (OfflineMap.doSellItem); dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai) |
| `item:1209` | X Chính xác | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi vật phẩm sự kiện) | bán cho NPC (OfflineMap.doSellItem); dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai) |
| `item:1210` | Tăng chí mạng | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi Huy chương mùa giải) | bán cho NPC (OfflineMap.doSellItem); dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai) |
| `item:1211` | Thuốc xua quái | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi vật phẩm sự kiện) | bán cho NPC (OfflineMap.doSellItem); dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai) |
| `item:1212` | Siêu thuốc xua quái | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)) | bán cho NPC (OfflineMap.doSellItem); dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai) |
| `item:1213` | Thuốc xua quái tối đa | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)) | bán cho NPC (OfflineMap.doSellItem); dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai) |
| `item:1214` | Kẹo hiếm | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Đổi Tem nghiên cứu); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi vật phẩm cũ) …(+1) | dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai) |
| `item:1215` | Quả Oran | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi vật phẩm sự kiện) | bán cho NPC (OfflineMap.doSellItem); dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai) |
| `item:1216` | Quả Sitrus | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi vật phẩm sự kiện) | bán cho NPC (OfflineMap.doSellItem); dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai) |
| `item:1217` | Quả Lum | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi vật phẩm sự kiện) | bán cho NPC (OfflineMap.doSellItem); dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai) |
| `item:1218` | Quả Cheri | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi vật phẩm sự kiện) | bán cho NPC (OfflineMap.doSellItem); dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai) |
| `item:1219` | Quả Chesto | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi vật phẩm sự kiện) | bán cho NPC (OfflineMap.doSellItem); dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai) |
| `item:1220` | Quả Pecha | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi vật phẩm sự kiện) | bán cho NPC (OfflineMap.doSellItem); dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai) |
| `item:1221` | Quả Rawst | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi vật phẩm sự kiện) | bán cho NPC (OfflineMap.doSellItem); dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai) |
| `item:1222` | Quả Aspear | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi vật phẩm sự kiện) | bán cho NPC (OfflineMap.doSellItem); dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai) |
| `item:1223` | Quả Leppa | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Thuốc và quả mọng (xu)); Sổ tay → Cửa hàng & đổi vật phẩm (Đổi vật phẩm sự kiện) | bán cho NPC (OfflineMap.doSellItem); dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai) |
| `item:1224` | Thức ăn thừa | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Đổi Huy chương Gym); chuỗi thưởng ITEM | dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai) |
| `item:1225` | Chuông vỏ sò | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Đổi Huy chương Gym) | dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai) |
| `item:1226` | Móng vuốt nhanh | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Đổi Huy chương mùa giải) | dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai) |
| `item:1227` | Băng đô tập trung | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Đổi Huy chương Gym) | dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai) |
| `item:1228` | Kính ngắm | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Đổi Huy chương Gym) | dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai) |
| `item:1229` | Đai lực sĩ | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Đổi Huy chương mùa giải) | dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai) |
| `item:1230` | Đá vĩnh cửu | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Đổi Huy chương Gym) | dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai) |
| `item:1231` | Trứng may mắn | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Đổi Tem nghiên cứu); mốc Pokédex 50 khi bật dài hạn (Pokedex.doiThuongLauDai) | dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai) |
| `item:1250` | Vé EXP x1,5 (30 phút) | ok | ok | Cửa hàng & Nạp (lượng, trao tang_toc; cần sv.nap_tien) | dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai); tăng tốc luyện cấp (TangTocGame.dung, chung trần điểm) |
| `item:1251` | Vé EXP x1,5 (30 phút) - khoá | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Lượng khoá (Kecleon)); bản khoá: nhiệm vụ tuần / lịch đăng nhập / vé mùa (TangTocGame.thuongKhoa) | dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai); tăng tốc luyện cấp (TangTocGame.dung, chung trần điểm) |
| `item:1252` | Vé EXP x2 (30 phút) | ok | ok | Cửa hàng & Nạp (lượng, trao tang_toc; cần sv.nap_tien) | dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai); tăng tốc luyện cấp (TangTocGame.dung, chung trần điểm) |
| `item:1253` | Vé EXP x2 (30 phút) - khoá | ok | ok | bản khoá: nhiệm vụ tuần / lịch đăng nhập / vé mùa (TangTocGame.thuongKhoa) | dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai); tăng tốc luyện cấp (TangTocGame.dung, chung trần điểm) |
| `item:1254` | Bùa thể lực (+60 phút) | ok | ok | Cửa hàng & Nạp (lượng, trao tang_toc; cần sv.nap_tien) | dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai); tăng tốc luyện cấp (TangTocGame.dung, chung trần điểm) |
| `item:1255` | Bùa thể lực (+60 phút) - khoá | ok | ok | bản khoá: nhiệm vụ tuần / lịch đăng nhập / vé mùa (TangTocGame.thuongKhoa) | dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai); tăng tốc luyện cấp (TangTocGame.dung, chung trần điểm) |
| `item:1256` | Phiếu ủy thác luyện tập (1 giờ) | ok | ok | Cửa hàng & Nạp (lượng, trao tang_toc; cần sv.nap_tien) | dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai); tăng tốc luyện cấp (TangTocGame.dung, chung trần điểm) |
| `item:1257` | Phiếu ủy thác luyện tập (1 giờ) - khoá | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Lượng khoá (Kecleon)); bản khoá: nhiệm vụ tuần / lịch đăng nhập / vé mùa (TangTocGame.thuongKhoa) | dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai); tăng tốc luyện cấp (TangTocGame.dung, chung trần điểm) |
| `item:1258` | Bùa bảo hộ | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Lượng khoá (Kecleon)) | bùa bảo hộ khi nâng cấp thất bại (NangCapLauDaiGame.buaChoMuc); dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai) |
| `item:1259` | Bùa bảo hộ cao cấp | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Lượng khoá (Kecleon)) | bùa bảo hộ khi nâng cấp thất bại (NangCapLauDaiGame.buaChoMuc); dùng theo bảng data_vat_pham_lau_dai (VatPhamDungLauDai) |
| `thuoc:0` | Xu | ok | ok | new Potion | xu (tiền) |
| `thuoc:1` | Thuốc hồi máu | ok | ok | chuỗi thưởng PT; cửa hàng thuốc NPC (OfflineMap.doBuyItem nhóm 4, price>0) | dùng thuốc (doUsePotion) |
| `thuoc:2` | Siêu thuốc hồi máu | ok | ok | cửa hàng thuốc NPC (OfflineMap.doBuyItem nhóm 4, price>0) | dùng thuốc (doUsePotion) |
| `thuoc:3` | Thuốc hồi máu cực mạnh | ok | ok | chuỗi thưởng PT; cửa hàng thuốc NPC (OfflineMap.doBuyItem nhóm 4, price>0) …(+1) | dùng thuốc (doUsePotion) |
| `thuoc:4` | Ether | ok | ok | cửa hàng thuốc NPC (OfflineMap.doBuyItem nhóm 4, price>0) | dùng thuốc (doUsePotion) |
| `thuoc:5` | Ether lớn | ok | ok | cửa hàng thuốc NPC (OfflineMap.doBuyItem nhóm 4, price>0) | dùng thuốc (doUsePotion) |
| `thuoc:6` | Ether tối đa | ok | ok | chuỗi thưởng PT; cửa hàng thuốc NPC (OfflineMap.doBuyItem nhóm 4, price>0) …(+1) | dùng thuốc (doUsePotion) |
| `thuoc:7` | Bình tăng tốc thường | ok | ok | cửa hàng thuốc NPC (OfflineMap.doBuyItem nhóm 4, price>0); new Potion | kiểm số thuốc (đổi / nộp) (doFruit); kiểm số thuốc (đổi / nộp) (initInfoFromDB) …(+2) |
| `thuoc:8` | Bình tăng tốc vàng. | ok | ok | cửa hàng thuốc NPC (OfflineMap.doBuyItem nhóm 4, price>0); new Potion | kiểm số thuốc (đổi / nộp) (doFruit); kiểm số thuốc (đổi / nộp) (doMenu) …(+1) |
| `thuoc:9` | Rễ năng lượng | ok | ok | cửa hàng thuốc NPC (OfflineMap.doBuyItem nhóm 4, price>0) | dùng thuốc (doUsePotion) |
| `thuoc:10` | Kẹo kinh nghiệm S: cộng 100k exp | ok | ok | chuỗi thưởng PT; cửa hàng thuốc NPC (OfflineMap.doBuyItem nhóm 4, price>0) …(+1) | đổi tại Sổ tay → Cửa hàng: 5 Kẹo kinh nghiệm S lấy 1 Kẹo hiếm |
| `thuoc:11` | Kẹo kinh nghiệm M: cộng 500k exp | ok | ok | chuỗi thưởng PT; cửa hàng thuốc NPC (OfflineMap.doBuyItem nhóm 4, price>0) …(+1) | đổi tại Sổ tay → Cửa hàng: 2 Kẹo kinh nghiệm M lấy 1 Kẹo hiếm |
| `thuoc:12` | Kẹo kinh nghiệm L: cộng 1t exp | ok | ok | chuỗi thưởng PT; cửa hàng thuốc NPC (OfflineMap.doBuyItem nhóm 4, price>0) | đổi tại Sổ tay → Cửa hàng: 1 Kẹo kinh nghiệm L lấy 1 Kẹo hiếm |
| `thuoc:13` | Bình tăng tốc bạc. | ok | ok | cửa hàng thuốc NPC (OfflineMap.doBuyItem nhóm 4, price>0); new Potion | kiểm số thuốc (đổi / nộp) (doFruit); potions[N]-= |
| `thuoc:14` | Khăn đỏ | ok | ok | cửa hàng thuốc NPC (OfflineMap.doBuyItem nhóm 4, price>0) | dùng thuốc (doUsePotion, trước switch) |
| `thuoc:15` | Khăn xanh | ok | ok | cửa hàng thuốc NPC (OfflineMap.doBuyItem nhóm 4, price>0) | dùng thuốc (doUsePotion, trước switch) |
| `thuoc:16` | Khăn vàng | ok | ok | cửa hàng thuốc NPC (OfflineMap.doBuyItem nhóm 4, price>0) | dùng thuốc (doUsePotion, trước switch) |
| `thuoc:17` | Khăn xanh lá | ok | ok | cửa hàng thuốc NPC (OfflineMap.doBuyItem nhóm 4, price>0) | dùng thuốc (doUsePotion, trước switch) |
| `thuoc:18` | Khăn tím | ok | ok | cửa hàng thuốc NPC (OfflineMap.doBuyItem nhóm 4, price>0) | dùng thuốc (doUsePotion, trước switch) |
| `thuoc:19` | Dây thoát hiểm | ok | ok | cửa hàng thuốc NPC (OfflineMap.doBuyItem nhóm 4, price>0) | dùng thuốc (doUsePotion, trước switch) |
| `thuoc:20` | Vé tàu | ok | ok | cửa hàng thuốc NPC (OfflineMap.doBuyItem nhóm 4, price>0); potions[N]+= | kiểm số thuốc (đổi / nộp) (doUp2Board); potions[N]-= |
| `thuoc:27` | Vé bay Pidgeot | ok | ok | cửa hàng data_shop slot 8 | dùng thuốc (doUsePotion) |
| `thuoc:29` | Phiếu công ích | ok | ok | cửa hàng data_shop slot 10 | dùng thuốc (doUsePotion) |
| `thuoc:30` | Sáo gọi Ponyta | ok | ok | cửa hàng data_shop slot 11 | dùng thuốc (doUsePotion) |
| `thuoc:31` | Huy hiệu lập đội | ok | ok | cửa hàng data_shop slot 12 | potions[N]-= |
| `thuoc:33` | Thẻ chiếm Gym | ok | ok | cửa hàng data_shop slot 14 | kiểm số thuốc (đổi / nộp) (playerGiveCard); potions[N]-= |
| `thuoc:34` | Sáo gọi Ponyta lửa | ok | ok | cửa hàng data_shop slot 15 | dùng thuốc (doUsePotion) |
| `thuoc:35` | Vé giờ vàng 100, x2 kinh nghiệm trong một giờ chơi. Tính the | ok | ok | new Potion; potions[N]+= | dùng thuốc (doUsePotion); potions[N] = potions[N] - |
| `thuoc:37` | Lông đinh | ok | ok | cửa hàng data_shop slot 18 | dùng thuốc (doUsePotion) |
| `thuoc:38` | Lông búi | ok | ok | cửa hàng data_shop slot 19 | dùng thuốc (doUsePotion) |
| `thuoc:39` | Lông cột cao | ok | ok | cửa hàng data_shop slot 20 | dùng thuốc (doUsePotion) |
| `thuoc:40` | Lông chéo | ok | ok | cửa hàng data_shop slot 21 | dùng thuốc (doUsePotion) |
| `thuoc:41` | Lông xù | ok | ok | cửa hàng data_shop slot 22 | dùng thuốc (doUsePotion) |
| `thuoc:42` | Lông ngang vai | ok | ok | cửa hàng data_shop slot 23 | dùng thuốc (doUsePotion) |
| `thuoc:43` | Lông đinh so le | ok | ok | cửa hàng data_shop slot 24 | dùng thuốc (doUsePotion) |
| `thuoc:44` | Lông so le | ok | ok | cửa hàng data_shop slot 25 | dùng thuốc (doUsePotion) |
| `thuoc:45` | Lông gọn gàng | ok | ok | cửa hàng data_shop slot 26 | dùng thuốc (doUsePotion) |
| `thuoc:46` | Lông cài hoa | ok | ok | cửa hàng data_shop slot 27 | dùng thuốc (doUsePotion) |
| `thuoc:47` | Bờm Ponyta | ok | ok | cửa hàng data_shop slot 28 | dùng thuốc (doUsePotion) |
| `thuoc:48` | Lông Clefairy | ok | ok | cửa hàng data_shop slot 29 | dùng thuốc (doUsePotion) |
| `thuoc:49` | Lông dựng đứng | ok | ok | cửa hàng data_shop slot 30 | dùng thuốc (doUsePotion) |
| `thuoc:58` | Hoa hồng | ok | ok | cửa hàng data_shop slot 39; thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) | dùng thuốc (doUsePotion) |
| `thuoc:59` | Hoa hồng | ok | ok | cửa hàng data_shop slot 40 | dùng thuốc (doUsePotion) |
| `thuoc:60` | Hoa hồng | ok | ok | cửa hàng data_shop slot 41 | dùng thuốc (doUsePotion) |
| `thuoc:61` | Hoa hồng | ok | ok | cửa hàng data_shop slot 42 | dùng thuốc (doUsePotion) |
| `thuoc:64` | Sáo gọi Tauros | ok | ok | cửa hàng data_shop slot 45 | kiểm số thuốc (đổi / nộp) (doMenu); potions[N]-= |
| `thuoc:65` | Sáo gọi Arcanine | ok | ok | cửa hàng data_shop slot 46 | kiểm số thuốc (đổi / nộp) (doMenu); potions[N]-= |
| `thuoc:66` | Sáo gọi Mightyena | ok | ok | cửa hàng data_shop slot 47 | kiểm số thuốc (đổi / nộp) (doMenu); potions[N]-= |
| `thuoc:67` | Sáo gọi Pidgeot | ok | ok | cửa hàng data_shop slot 48 | kiểm số thuốc (đổi / nộp) (doMenu); potions[N]-= |
| `thuoc:68` | Sáo gọi Rapidash | ok | ok | cửa hàng data_shop slot 49 | kiểm số thuốc (đổi / nộp) (doMenu); potions[N]-= |
| `thuoc:69` | Vé quay số | ok | ok | potions[N] = potions[N] + | kiểm số thuốc (đổi / nộp) (doStartLuckDraw) |
| `thuoc:71` | Trứng Ponyta | ok | ok | potions[N]+= | dùng thuốc (doUsePotion); kiểm số thuốc (đổi / nộp) (doMenuNpcDracula) …(+4) |
| `thuoc:75` | Vé giờ vàng 100, x2 kinh nghiệm trong 3 giờ chơi. Tính theo  | ok | ok | new Potion | dùng thuốc (doUsePotion) |
| `thuoc:76` | Vé quay số đặc biệt | ok | ok | potions[N] = potions[N] + | kiểm số thuốc (đổi / nộp) (doStartLuckDraw) |
| `thuoc:83` | Kẹo Poké. Tăng 1% kinh nghiệm cho thú nuôi khi cho ăn. | ok | ok | potions[N]+= | kiểm số thuốc (đổi / nộp) (doChoKeo); kiểm số thuốc (đổi / nộp) (doMenu) …(+4) |
| `thuoc:84` | Thuốc X | ok | ok | cửa hàng data_shop slot 65; potions[N] = potions[N] + | dùng thuốc (doUsePotion) |
| `thuoc:86` | Sáo gọi Moltres | ok | ok | potions[N]+= | kiểm số thuốc (đổi / nộp) (doMenu); kiểm số thuốc (đổi / nộp) (onDropItem) …(+1) |
| `thuoc:91` | Vé vào mỏ | ok | ok | cửa hàng data_shop slot 72; potions[N]+= | dùng thuốc (doUsePotion) |
| `thuoc:94` | Thuốc hồi máu 15k | ok | ok | new Potion; potions[N] = potions[N] + | dùng thuốc (doUsePotion); kiểm số thuốc (đổi / nộp) (doMenuNpcNoel) …(+2) |
| `thuoc:96` | Ether 15k | ok | ok | new Potion; potions[N] = potions[N] + | dùng thuốc (doUsePotion); kiểm số thuốc (đổi / nộp) (doMenuNpcNoel) …(+2) |
| `thuoc:97` | Thẻ đổi hàng | ok | ok | new Potion | dùng thuốc (doUsePotion) |
| `thuoc:98` | Vảy trái tim | ok | ok | potions[N] = potions[N] + | dùng thuốc (doUsePotion) |
| `thuoc:100` | loa | ok | ok | cửa hàng data_shop slot 81; potions[N] = potions[N] + | dùng thuốc (doUsePotion); kiểm số thuốc (đổi / nộp) (doProcessInputText) …(+1) |
| `thuoc:101` | Rương vàng | ok | ok | potions[N]+= | dùng thuốc (doUsePotion) |
| `thuoc:102` | Rương bạc | ok | ok | potions[N]+= | dùng thuốc (doUsePotion) |
| `thuoc:103` | Lá vàng | ok | ok | potions[N] = potions[N] + | kiểm số thuốc (đổi / nộp) (upLevelPlot); potions[N] = potions[N] - |
| `thuoc:104` | Poké Ball. Dùng để bắt thú nuôi | ok | ok | potions[N] = potions[N] + | kiểm số thuốc (đổi / nộp) (doGetItem); kiểm số thuốc (đổi / nộp) (upLevelPlot) …(+2) |
| `thuoc:105` | Mảnh bản đồ 2 | ok | ok | potions[N] = potions[N] +; potions[N]+= | kiểm số thuốc (đổi / nộp) (doMenuGioTo2016); kiểm số thuốc (đổi / nộp) (doMenuGioTo2020) …(+6) |
| `thuoc:106` | Nhánh hoa hồng vàng | ok | ok | potions[N]+=; thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) | kiểm số thuốc (đổi / nộp) (doVuLanDoiNguyenLieu); potions[N]-= …(+1) |
| `thuoc:107` | Hoa hồng đỏ | ok | ok | potions[N]+= | đổi bó hoa Vu Lan (MapSuKien.doVuLanDoiHoa) |
| `thuoc:111` | Đĩa chiêu (TM) cho pet | ok | ok | potions[N]+= | dùng thuốc (doUsePotion) |
| `thuoc:112` | Thức ăn tươi | ok | ok | new Potion | dùng thuốc (doUsePotion) |
| `thuoc:113` | Thức ăn khô | ok | ok | new Potion | dùng thuốc (doUsePotion) |
| `thuoc:114` | Thức ăn tổng hợp | ok | ok | new Potion; potions[N] = potions[N] + | dùng thuốc (doUsePotion) |
| `thuoc:116` | Thẻ đổi hàng cao cấp | ok | ok | new Potion | dùng thuốc (doUsePotion); kiểm số thuốc (đổi / nộp) (doMenuNpcVaoHa) …(+1) |
| `thuoc:117` | Mảnh bản đồ 3 | ok | ok | potions[N]+= | kiểm số thuốc (đổi / nộp) (doMenuGioTo2020); kiểm số thuốc (đổi / nộp) (doMenuNpcTet2015) …(+4) |
| `thuoc:118` | Vé Moltres. | ok | ok | new Potion; potions[N] = potions[N] + …(+1) | kiểm số thuốc (đổi / nộp) (doMenuNpcTet2015); kiểm số thuốc (đổi / nộp) (doMenuNpcVaoHa) …(+4) |
| `thuoc:119` | Thuốc hồi sinh | ok | ok | potions[N]+= | kiểm số thuốc (đổi / nộp) (doMenuNpcVaoHa); potions[N] = potions[N] - …(+1) |
| `thuoc:120` | Thịt | ok | ok | potions[N]+= | kiểm số thuốc (đổi / nộp) (doMenuNpcTet2015); kiểm số thuốc (đổi / nộp) (doMenuTet2016) …(+7) |
| `thuoc:122` | Đậu xanh | ok | ok | thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) | kiểm số thuốc (đổi / nộp) (doChamNuoc); kiểm số thuốc (đổi / nộp) (doMenuNpcTet2015) …(+6) |
| `thuoc:123` | Thuốc X siêu cấp | ok | ok | new Potion; potions[N] = potions[N] + | dùng thuốc (doUsePotion) |
| `thuoc:124` | Bánh dẻo | ok | ok | thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) | kiểm số thuốc (đổi / nộp) (doMenuTrungThu2016); kiểm số thuốc (đổi / nộp) (doProcessInputText) …(+3) |
| `thuoc:125` | Bánh nướng | ok | ok | potions[N]+= | kiểm số thuốc (đổi / nộp) (doDotDen); kiểm số thuốc (đổi / nộp) (doMenuNpcVaoHa) …(+4) |
| `thuoc:131` | Thuốc săn trùm. | ok | ok | mở item:1050 (NoiDungLauDai.moHop) | dùng thuốc (doUsePotion) |
| `thuoc:136` | Nến | ok | ok | potions[N] = potions[N] +; potions[N]+= | kiểm số thuốc (đổi / nộp) (doDotDen); kiểm số thuốc (đổi / nộp) (processMessage) …(+2) |
| `thuoc:137` | Kẹo | ok | ok | thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) | kiểm số thuốc (đổi / nộp) (doMenuHalloween2016); kiểm số thuốc (đổi / nộp) (doProcessInputTextHalloWeen2016) …(+3) |
| `thuoc:138` | Bí ngô | ok | ok | thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) | kiểm số thuốc (đổi / nộp) (doMenuHalloween2016); kiểm số thuốc (đổi / nộp) (doProcessInputTextHalloWeen2016) …(+1) |
| `thuoc:139` | Hộp quà | ok | ok | thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) | kiểm số thuốc (đổi / nộp) (doProcessInputTextHalloWeen2016) |
| `thuoc:142` | Bột mỳ | ok | ok | potions[N]+= | kiểm số thuốc (đổi / nộp) (doMenuGioTo2020); kiểm số thuốc (đổi / nộp) (doMenuSuKienHe2017) …(+6) |
| `thuoc:144` | Sao vàng | ok | ok | potions[N]+= | dùng thuốc (doUsePotion) |
| `thuoc:145` | Dưa Hấu | ok | ok | thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) | kiểm số thuốc (đổi / nộp) (doProcessInputTextNoel) |
| `thuoc:150` | Lá dong | ok | ok | thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) | kiểm số thuốc (đổi / nộp) (doChamNuoc); potions[N] = potions[N] - |
| `thuoc:151` | Bánh chưng | ok | ok | potions[N] = potions[N] + | kiểm số thuốc (đổi / nộp) (domenuTet2017); potions[N]-= |
| `thuoc:152` | Bánh tét | ok | ok | potions[N] = potions[N] + | kiểm số thuốc (đổi / nộp) (domenuTet2017); potions[N] = potions[N] - …(+1) |
| `thuoc:159` | Bao lì xì | ok | ok | potions[N]+=; thưởng khác nhiệm vụ (NhiemVuEngine.thuongKhac) | dùng thuốc (doUsePotion) |
| `thuoc:160` | Quả bóng bạc | ok | ok | potions[N]+= | dùng thuốc (doUsePotion); kiểm số thuốc (đổi / nộp) (domenuWorldcup2017) …(+1) |
| `thuoc:161` | Quả bóng vàng | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Đổi vật phẩm cũ) | dùng thuốc (doUsePotion) |
| `ngoc:0` | Bảo hiểm 20% | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | nâng cấp (bảo hiểm / may mắn / luyện kim) MapEpNgoc |
| `ngoc:1` | Bảo hiểm 30% | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | nâng cấp (bảo hiểm / may mắn / luyện kim) MapEpNgoc |
| `ngoc:2` | Bảo hiểm 40% | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | nâng cấp (bảo hiểm / may mắn / luyện kim) MapEpNgoc |
| `ngoc:3` | Bảo hiểm 50% | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | nâng cấp (bảo hiểm / may mắn / luyện kim) MapEpNgoc |
| `ngoc:4` | Bảo hiểm 90% | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | nâng cấp (bảo hiểm / may mắn / luyện kim) MapEpNgoc |
| `ngoc:5` | Đá may mắn cấp 1 | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | nâng cấp (bảo hiểm / may mắn / luyện kim) MapEpNgoc |
| `ngoc:6` | Đá may mắn cấp 2 | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | nâng cấp (bảo hiểm / may mắn / luyện kim) MapEpNgoc |
| `ngoc:7` | Đá may mắn cấp 3 | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | nâng cấp (bảo hiểm / may mắn / luyện kim) MapEpNgoc |
| `ngoc:8` | Luyện kim dược | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1); mở item:1050 (NoiDungLauDai.moHop) …(+3) | nguyên liệu chế tạo: Huy hiệu tối thượng; nguyên liệu chế tạo: Hạt giống thần tối thượng …(+29) |
| `ngoc:9` | Luyện kim dược | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | nâng cấp (bảo hiểm / may mắn / luyện kim) MapEpNgoc |
| `ngoc:10` | Luyện kim dược | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | nâng cấp (bảo hiểm / may mắn / luyện kim) MapEpNgoc |
| `ngoc:11` | Luyện kim dược | ok | ok | doAddGemItem | nâng cấp (bảo hiểm / may mắn / luyện kim) MapEpNgoc |
| `ngoc:66` | Luyện kim dược 35 | ok | ok | doAddGemItem | nâng cấp (bảo hiểm / may mắn / luyện kim) MapEpNgoc |
| `ngoc:106` | Tơ lụa cấp 4 | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | nguyên liệu chế tạo / ép / nâng pet (MapCheTao, MapEpNgoc.doFinishEpNgoc, Pet.doUpdrage) |
| `ngoc:107` | Tơ lụa cấp 5 | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | nguyên liệu chế tạo / ép / nâng pet (MapCheTao, MapEpNgoc.doFinishEpNgoc, Pet.doUpdrage) |
| `ngoc:113` | Bạc cấp 4 | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | nguyên liệu chế tạo / ép / nâng pet (MapCheTao, MapEpNgoc.doFinishEpNgoc, Pet.doUpdrage) |
| `ngoc:114` | Bạc cấp 5 | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | nguyên liệu chế tạo / ép / nâng pet (MapCheTao, MapEpNgoc.doFinishEpNgoc, Pet.doUpdrage) |
| `ngoc:120` | Thuỷ tinh cấp 4 | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | nguyên liệu chế tạo / ép / nâng pet (MapCheTao, MapEpNgoc.doFinishEpNgoc, Pet.doUpdrage) |
| `ngoc:121` | Thuỷ tinh cấp 5 | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | nguyên liệu chế tạo / ép / nâng pet (MapCheTao, MapEpNgoc.doFinishEpNgoc, Pet.doUpdrage) |
| `ngoc:127` | Gỗ sưa cấp 4 | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | nguyên liệu chế tạo / ép / nâng pet (MapCheTao, MapEpNgoc.doFinishEpNgoc, Pet.doUpdrage) |
| `ngoc:128` | Gỗ sưa cấp 5 | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | nguyên liệu chế tạo / ép / nâng pet (MapCheTao, MapEpNgoc.doFinishEpNgoc, Pet.doUpdrage) |
| `ngoc:134` | Da cứng cấp 4 | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | nguyên liệu chế tạo / ép / nâng pet (MapCheTao, MapEpNgoc.doFinishEpNgoc, Pet.doUpdrage) |
| `ngoc:135` | Da cứng cấp 5 | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | nguyên liệu chế tạo / ép / nâng pet (MapCheTao, MapEpNgoc.doFinishEpNgoc, Pet.doUpdrage) |
| `ngoc:137` | Đá cầu vồng thường lv 1 | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | đổi màu đồ thú (ItemChangeColor) / thần khí |
| `ngoc:138` | Đá cầu vồng thường lv 2 | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | đổi màu đồ thú (ItemChangeColor) / thần khí |
| `ngoc:139` | Đá cầu vồng thường lv 3 | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | đổi màu đồ thú (ItemChangeColor) / thần khí |
| `ngoc:140` | Đá cầu vồng thường lv 4 | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | đổi màu đồ thú (ItemChangeColor) / thần khí |
| `ngoc:141` | Đá cầu vồng thường lv 5 | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | đổi màu đồ thú (ItemChangeColor) / thần khí |
| `ngoc:142` | Đá cầu vồng thường lv 6 | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | đổi màu đồ thú (ItemChangeColor) / thần khí |
| `ngoc:143` | Đá cầu vồng cao cấp lv 1 | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | đổi màu đồ thú (ItemChangeColor) / thần khí |
| `ngoc:144` | Đá cầu vồng cao cấp lv 2 | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | đổi màu đồ thú (ItemChangeColor) / thần khí |
| `ngoc:145` | Đá cầu vồng cao cấp lv 3 | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | đổi màu đồ thú (ItemChangeColor) / thần khí |
| `ngoc:146` | Đá cầu vồng cao cấp lv 4 | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | đổi màu đồ thú (ItemChangeColor) / thần khí |
| `ngoc:147` | Đá cầu vồng cao cấp lv 5 | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | đổi màu đồ thú (ItemChangeColor) / thần khí |
| `ngoc:148` | Đá cầu vồng cao cấp lv 6 | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | đổi màu đồ thú (ItemChangeColor) / thần khí |
| `ngoc:149` | Đá cầu vồng tinh khiết lv 1 | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | đổi màu đồ thú (ItemChangeColor) / thần khí |
| `ngoc:150` | Đá cầu vồng tinh khiết lv 2 | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | đổi màu đồ thú (ItemChangeColor) / thần khí |
| `ngoc:151` | Đá cầu vồng tinh khiết lv 3 | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | đổi màu đồ thú (ItemChangeColor) / thần khí |
| `ngoc:152` | Đá cầu vồng tinh khiết lv 4 | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | đổi màu đồ thú (ItemChangeColor) / thần khí |
| `ngoc:153` | Đá cầu vồng tinh khiết lv 5 | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | đổi màu đồ thú (ItemChangeColor) / thần khí |
| `ngoc:154` | Đá cầu vồng tinh khiết lv 6 | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1); doAddGemItem | đổi màu đồ thú (ItemChangeColor) / thần khí |
| `ngoc:175` | Đá tách nguyên liệu 5x | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | tách nguyên liệu (MapEpNgoc.doTachNguyenLieu) |
| `ngoc:176` | Đá tách nguyên liệu 6x | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | tách nguyên liệu (MapEpNgoc.doTachNguyenLieu) |
| `ngoc:177` | Đá tách nguyên liệu 7x | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | tách nguyên liệu (MapEpNgoc.doTachNguyenLieu) |
| `ngoc:178` | Đá tách nguyên liệu đặc biệt | ok | ok | doAddGemItem | tách nguyên liệu (MapEpNgoc.doTachNguyenLieu) |
| `ngoc:227` | Đá thuộc tính thường | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | thêm thuộc tính (Map.doAddNewAttribute) |
| `ngoc:228` | Đá thuộc tính cao cấp | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | thêm thuộc tính (Map.doAddNewAttribute) |
| `ngoc:234` | Xương cấp 6 | ok | ok | cửa hàng ngọc NPC (OfflineMap.doBuyGemItem, sell=1) | khoá đồ thú (OfflineMap) |
| `ngoc:240` | Ngọc hoàng hôn cấp 6 | ok | ok | doAddGemItem | khoá trang bị (OfflineMap) |
| `ngoc:245` | Luyện kim dược 40 | ok | ok | doAddGemItem | nâng cấp (bảo hiểm / may mắn / luyện kim) MapEpNgoc |
| `ngoc:246` | Bột trắng | ok | ok | doAddGemItem | đổi tại Sổ tay → Cửa hàng: 3 Bột trắng lấy 1 Bột xanh lá |
| `ngoc:249` | Bột xanh lá | ok | ok | Sổ tay → Cửa hàng & đổi vật phẩm (Đổi vật phẩm cũ); chuỗi thưởng GEM …(+1) | tiến giai (MapCheTao.doAddNewAttBot) |
| `ngoc:250` | Đá thăng bậc | ok | ok | doAddGemItem | tiến giai (MapCheTao.doAddNewAttBot) |
| `ngoc:259` | Nham thạch cấp 2 | ok | ok | doAddGemItem | Hoả thạch: nâng cấp thần thú / thần khí; kiểm số ngọc (đổi / nộp) (doMenu) |
| `ngoc:267` | Bản đồ núi châu báu | ok | ok | doAddGemItem | Ngọc 1-7 sao / bản đồ kho báu (MapMenuNpc) |
| `ngoc:268` | Thuốc hóa hình | ok | ok | doAddGemItem | Thuốc hoá hình pet (RealController); kiểm số ngọc (đổi / nộp) (doCreateItem) …(+1) |
| `nv:0` | Nọc bọ cạp | ok | ok | nhận khi nhận nhiệm vụ giao hàng (Char.OnQuest) | nộp nhiệm vụ (Char.OnQuest xoá itemquest) |
| `nv:4` | Lá thư | ok | ok | nhận khi nhận nhiệm vụ giao hàng (Char.OnQuest) | nộp nhiệm vụ (Char.OnQuest xoá itemquest) |
| `nv:6` | Lông đuôi Hồng Kê | ok | ok | nhận khi nhận nhiệm vụ giao hàng (Char.OnQuest); rơi khi làm nhiệm vụ thu thập (Monster → checkDropItemQuest) | nộp nhiệm vụ (Char.OnQuest xoá itemquest) |
| `nv:8` | Da ếch | ok | ok | nhận khi nhận nhiệm vụ giao hàng (Char.OnQuest) | nộp nhiệm vụ (Char.OnQuest xoá itemquest) |
| `nv:9` | Cựa gà | ok | ok | nhận khi nhận nhiệm vụ giao hàng (Char.OnQuest) | nộp nhiệm vụ (Char.OnQuest xoá itemquest) |
| `nv:10` | Gói hàng | ok | ok | rơi khi làm nhiệm vụ thu thập (Monster → checkDropItemQuest) | nộp nhiệm vụ (Char.OnQuest xoá itemquest) |
| `nv:13` | Lông vũ tím | ok | ok | rơi khi làm nhiệm vụ thu thập (Monster → checkDropItemQuest) | nộp nhiệm vụ (Char.OnQuest xoá itemquest) |
| `nv:14` | Mảnh sao băng | ok | ok | rơi khi làm nhiệm vụ thu thập (Monster → checkDropItemQuest) | nộp nhiệm vụ (Char.OnQuest xoá itemquest) |
| `nv:17` | Mảnh bản đồ | ok | ok | rơi khi làm nhiệm vụ thu thập (Monster → checkDropItemQuest) | nộp nhiệm vụ (Char.OnQuest xoá itemquest) |
| `nv:18` | Quặng sắt | ok | ok | rơi khi làm nhiệm vụ thu thập (Monster → checkDropItemQuest) | nộp nhiệm vụ (Char.OnQuest xoá itemquest) |
| `nv:19` | Lọ thuốc | ok | ok | rơi khi làm nhiệm vụ thu thập (Monster → checkDropItemQuest) | nộp nhiệm vụ (Char.OnQuest xoá itemquest) |
| `nv:22` | Vỏ sò | ok | ok | nhận khi nhận nhiệm vụ giao hàng (Char.OnQuest); rơi khi làm nhiệm vụ thu thập (Monster → checkDropItemQuest) | nộp nhiệm vụ (Char.OnQuest xoá itemquest) |
| `nv:23` | Bản ghi chép | ok | ok | rơi khi làm nhiệm vụ thu thập (Monster → checkDropItemQuest) | nộp nhiệm vụ (Char.OnQuest xoá itemquest) |
| `nv:24` | Hoa sương | ok | ok | rơi khi làm nhiệm vụ thu thập (Monster → checkDropItemQuest) | nộp nhiệm vụ (Char.OnQuest xoá itemquest) |
| `nv:25` | Mật ong | ok | ok | rơi khi làm nhiệm vụ thu thập (Monster → checkDropItemQuest) | nộp nhiệm vụ (Char.OnQuest xoá itemquest) |
| `nv:26` | Đá Unown | ok | ok | rơi khi làm nhiệm vụ thu thập (Monster → checkDropItemQuest) | nộp nhiệm vụ (Char.OnQuest xoá itemquest) |
| `nv:27` | Ngọc trai | ok | ok | rơi khi làm nhiệm vụ thu thập (Monster → checkDropItemQuest) | nộp nhiệm vụ (Char.OnQuest xoá itemquest) |
| `nv:29` | Phong bì xanh | ok | ok | rơi khi làm nhiệm vụ thu thập (Monster → checkDropItemQuest) | nộp nhiệm vụ (Char.OnQuest xoá itemquest) |
| `nv:30` | Than hồng | ok | ok | rơi khi làm nhiệm vụ thu thập (Monster → checkDropItemQuest) | nộp nhiệm vụ (Char.OnQuest xoá itemquest) |
| `nv:32` | Nấm phát quang | ok | ok | rơi khi làm nhiệm vụ thu thập (Monster → checkDropItemQuest) | nộp nhiệm vụ (Char.OnQuest xoá itemquest) |
| `nv:34` | Lá trà Ecruteak | ok | ok | rơi khi làm nhiệm vụ thu thập (Monster → checkDropItemQuest) | nộp nhiệm vụ (Char.OnQuest xoá itemquest) |
| `nv:38` | Vảy rồng nhỏ | ok | ok | rơi khi làm nhiệm vụ thu thập (Monster → checkDropItemQuest) | nộp nhiệm vụ (Char.OnQuest xoá itemquest) |
| `nv:40` | Đèn lồng | ok | ok | rơi khi làm nhiệm vụ thu thập (Monster → checkDropItemQuest) | nộp nhiệm vụ (Char.OnQuest xoá itemquest) |
| `nv:43` | Hộp nhạc | ok | ok | rơi khi làm nhiệm vụ thu thập (Monster → checkDropItemQuest) | nộp nhiệm vụ (Char.OnQuest xoá itemquest) |
| `nv:50` | Chăn len | ok | ok | rơi khi làm nhiệm vụ thu thập (Monster → checkDropItemQuest) | nộp nhiệm vụ (Char.OnQuest xoá itemquest) |
| `hst:1` | Rác trôi | ngu | ok | câu cá (KyNangSong.BANG_CAU_CA) | bán NPC / chợ sinh thái; nguyên liệu công thức 27 Mồi thơm biển …(+1) |
| `hst:2` | Vỏ sò | ngu | ok | câu cá (KyNangSong.BANG_CAU_CA) | bán NPC / chợ sinh thái; nguyên liệu công thức 22 Hồ cá mini …(+1) |
| `hst:3` | Rong biển | ngu | ok | câu cá (KyNangSong.BANG_CAU_CA) | bán NPC / chợ sinh thái; nguyên liệu công thức 1 Mồi thơm (x3) …(+2) |
| `hst:4` | Ngọc trai nhỏ | ngu | ok | câu cá (KyNangSong.BANG_CAU_CA) | bán NPC / chợ sinh thái; nguyên liệu công thức 22 Hồ cá mini …(+2) |
| `hst:5` | Ngọc trai lớn | ngu | ok | câu cá (KyNangSong.BANG_CAU_CA) | bán NPC / chợ sinh thái; nguyên liệu công thức 28 Bể san hô |
| `hst:6` | Vảy Magikarp | ngu | ok | câu cá (KyNangSong.BANG_CAU_CA) | bán NPC / chợ sinh thái; nguyên liệu công thức 22 Hồ cá mini …(+1) |
| `hst:7` | Vảy Goldeen | ngu | ok | câu cá (KyNangSong.BANG_CAU_CA) | bán NPC / chợ sinh thái; nguyên liệu công thức 22 Hồ cá mini …(+1) |
| `hst:8` | Vảy Poliwag | ngu | ok | câu cá (KyNangSong.BANG_CAU_CA) | bán NPC / chợ sinh thái; nguyên liệu công thức 6 Siêu thuốc |
| `hst:9` | Xúc tu Tentacool | ngu | ok | câu cá (KyNangSong.BANG_CAU_CA) | bán NPC / chợ sinh thái; nguyên liệu công thức 26 Lẩu hải sản (Cà ri cá) |
| `hst:10` | Vảy Horsea | ngu | ok | câu cá (KyNangSong.BANG_CAU_CA) | bán NPC / chợ sinh thái; nguyên liệu công thức 26 Lẩu hải sản (Cà ri cá) |
| `hst:11` | Vỏ Shellder | ngu | ok | câu cá (KyNangSong.BANG_CAU_CA) | bán NPC / chợ sinh thái; nguyên liệu công thức 26 Lẩu hải sản (Cà ri cá) |
| `hst:12` | Lõi sao Staryu | ngu | ok | câu cá (KyNangSong.BANG_CAU_CA) | bán NPC / chợ sinh thái; nguyên liệu công thức 7 Nước khoáng |
| `hst:13` | Càng Krabby | ngu | ok | câu cá (KyNangSong.BANG_CAU_CA) | bán NPC / chợ sinh thái; nguyên liệu công thức 26 Lẩu hải sản (Cà ri cá) |
| `hst:14` | Đèn Chinchou | ngu | ok | câu cá (KyNangSong.BANG_CAU_CA) | bán NPC / chợ sinh thái; nguyên liệu công thức 24 Đèn lồng Chinchou |
| `hst:15` | Vảy Remoraid | ngu | ok | câu cá (KyNangSong.BANG_CAU_CA) | bán NPC / chợ sinh thái; nguyên liệu công thức 1 Mồi thơm (x3) |
| `hst:16` | Gai Qwilfish | ngu | ok | câu cá (KyNangSong.BANG_CAU_CA) | bán NPC / chợ sinh thái; nguyên liệu công thức 27 Mồi thơm biển |
| `hst:17` | San hô Corsola | ngu | ok | câu cá (KyNangSong.BANG_CAU_CA) | bán NPC / chợ sinh thái; nguyên liệu công thức 28 Bể san hô |
| `hst:18` | Vảy Feebas | ngu | ok | câu cá (KyNangSong.BANG_CAU_CA) | bán NPC / chợ sinh thái; nguyên liệu công thức 29 Tượng rồng Dratini |
| `hst:19` | Vảy rồng Dratini | ngu | ok | câu cá (KyNangSong.BANG_CAU_CA) | bán NPC / chợ sinh thái; nguyên liệu công thức 29 Tượng rồng Dratini |
| `hst:20` | Mai Lapras | ngu | ok | câu cá (KyNangSong.BANG_CAU_CA) | bán NPC / chợ sinh thái; nguyên liệu công thức 29 Tượng rồng Dratini |
| `hst:30` | Berry Oran | ngu | ok | câu cá (KyNangSong.BANG_CAU_CA); thu hoạch vườn berry (HeSinhThaiGame.khiThuHoach) | bán NPC / chợ sinh thái; nguyên liệu công thức 0 Bánh Poké …(+2) |
| `hst:31` | Berry Pecha | ngu | ok | thu hoạch vườn berry (HeSinhThaiGame.khiThuHoach) | bán NPC / chợ sinh thái; nguyên liệu công thức 0 Bánh Poké |
| `hst:32` | Berry Rawst | ngu | ok | thu hoạch vườn berry (HeSinhThaiGame.khiThuHoach) | bán NPC / chợ sinh thái; nguyên liệu công thức 2 Súp berry |
| `hst:33` | Berry Cheri | ngu | ok | thu hoạch vườn berry (HeSinhThaiGame.khiThuHoach) | bán NPC / chợ sinh thái; nguyên liệu công thức 21 Chậu hoa berry |
| `hst:34` | Berry Chesto | ngu | ok | thu hoạch vườn berry (HeSinhThaiGame.khiThuHoach) | bán NPC / chợ sinh thái; nguyên liệu công thức 5 Nước tinh khiết |
| `hst:35` | Berry Leppa | ngu | ok | thu hoạch vườn berry (HeSinhThaiGame.khiThuHoach) | bán NPC / chợ sinh thái; nguyên liệu công thức 7 Nước khoáng |
| `hst:36` | Berry Sitrus | ngu | ok | thu hoạch vườn berry (HeSinhThaiGame.khiThuHoach) | bán NPC / chợ sinh thái; nguyên liệu công thức 3 Cà ri cá …(+2) |
| `hst:37` | Berry Lum | ngu | ok | thu hoạch vườn berry (HeSinhThaiGame.khiThuHoach) | bán NPC / chợ sinh thái; nguyên liệu công thức 8 Thuốc cao cấp |
| `hst:50` | Đá cuội | ngu | ok | khảo cổ (KyNangSong.BANG_DAO) | bán NPC / chợ sinh thái; nguyên liệu công thức 21 Chậu hoa berry …(+1) |
| `hst:51` | Mảnh Đá Nước | ngu | ok | khảo cổ (KyNangSong.BANG_DAO) | bán NPC / chợ sinh thái; nguyên liệu công thức 9 Đá Nước |
| `hst:52` | Mảnh Đá Sấm | ngu | ok | khảo cổ (KyNangSong.BANG_DAO) | bán NPC / chợ sinh thái; nguyên liệu công thức 10 Đá Sấm |
| `hst:53` | Mảnh Đá Lửa | ngu | ok | khảo cổ (KyNangSong.BANG_DAO) | bán NPC / chợ sinh thái; nguyên liệu công thức 11 Đá Lửa |
| `hst:54` | Mảnh Đá Mặt Trời | ngu | ok | khảo cổ (KyNangSong.BANG_DAO) | bán NPC / chợ sinh thái; nguyên liệu công thức 12 Đá Mặt Trời |
| `hst:55` | Mảnh Đá Mặt Trăng | ngu | ok | khảo cổ (KyNangSong.BANG_DAO) | bán NPC / chợ sinh thái; nguyên liệu công thức 13 Đá Mặt Trăng |
| `hst:56` | Mảnh Đá Lá | ngu | ok | khảo cổ (KyNangSong.BANG_DAO) | bán NPC / chợ sinh thái; nguyên liệu công thức 14 Đá Lá |
| `hst:57` | Mảnh Đá Băng | ngu | ok | khảo cổ (KyNangSong.BANG_DAO) | bán NPC / chợ sinh thái; nguyên liệu công thức 15 Đá Băng |
| `hst:58` | Đá sao rơi | ngu | ok | khảo cổ (KyNangSong.BANG_DAO) | bán NPC / chợ sinh thái; nguyên liệu công thức 29 Tượng rồng Dratini |
| `hst:59` | Vảy tim | ngu | ok | khảo cổ (KyNangSong.BANG_DAO) | bán NPC / chợ sinh thái; nguyên liệu công thức 29 Tượng rồng Dratini |
| `hst:60` | Hoá thạch Vỏ ốc | ngu | ok | khảo cổ (KyNangSong.BANG_DAO) | bán NPC / chợ sinh thái; nguyên liệu công thức 16 Phục hồi Hoá thạch Vỏ ốc …(+1) |
| `hst:61` | Hoá thạch Mai | ngu | ok | khảo cổ (KyNangSong.BANG_DAO) | bán NPC / chợ sinh thái; nguyên liệu công thức 17 Phục hồi Hoá thạch Mai …(+1) |
| `hst:62` | Hổ phách cổ | ngu | ok | khảo cổ (KyNangSong.BANG_DAO) | bán NPC / chợ sinh thái; nguyên liệu công thức 20 Phục hồi Hổ phách cổ |
| `hst:63` | Hoá thạch Rễ | ngu | ok | khảo cổ (KyNangSong.BANG_DAO) | bán NPC / chợ sinh thái; nguyên liệu công thức 18 Phục hồi Hoá thạch Rễ …(+1) |
| `hst:64` | Hoá thạch Vuốt | ngu | ok | khảo cổ (KyNangSong.BANG_DAO) | bán NPC / chợ sinh thái; nguyên liệu công thức 19 Phục hồi Hoá thạch Vuốt …(+1) |
| `hst:65` | Xương cổ | ngu | ok | khảo cổ (KyNangSong.BANG_DAO) | bán NPC / chợ sinh thái; nguyên liệu công thức 16 Phục hồi Hoá thạch Vỏ ốc …(+5) |
| `hst:80` | Bánh Poké | ngu | ok | công thức 0 Bánh Poké | dùng món ăn (+thể lực / buff, HeSinhThaiMenu) |
| `hst:81` | Súp berry | ngu | ok | công thức 2 Súp berry | dùng món ăn (+thể lực / buff, HeSinhThaiMenu) |
| `hst:82` | Cà ri cá | ngu | ok | công thức 26 Lẩu hải sản (Cà ri cá); công thức 3 Cà ri cá | dùng món ăn (+thể lực / buff, HeSinhThaiMenu) |
| `hst:83` | Mồi thơm | ngu | ok | công thức 1 Mồi thơm (x3); công thức 27 Mồi thơm biển | dùng món ăn (+thể lực / buff, HeSinhThaiMenu) |
| `hst:90` | Thuốc xịt (10 bình HP nhỏ) | ngu | ok | công thức 4 Thuốc xịt | dùng → đổi bình thuốc thật (KyNangSong.thuongDungThuoc) |
| `hst:91` | Siêu thuốc (10 bình HP vừa) | ngu | ok | công thức 6 Siêu thuốc | dùng → đổi bình thuốc thật (KyNangSong.thuongDungThuoc) |
| `hst:92` | Thuốc cao cấp (10 bình HP lớn) | ngu | ok | công thức 8 Thuốc cao cấp | dùng → đổi bình thuốc thật (KyNangSong.thuongDungThuoc) |
| `hst:93` | Nước tinh khiết (10 bình MP nhỏ) | ngu | ok | công thức 5 Nước tinh khiết | dùng → đổi bình thuốc thật (KyNangSong.thuongDungThuoc) |
| `hst:94` | Nước khoáng (10 bình MP vừa) | ngu | ok | công thức 7 Nước khoáng | dùng → đổi bình thuốc thật (KyNangSong.thuongDungThuoc) |
| `hst:100` | Trứng Pokémon | ngu | ok | nhà trẻ đẻ trứng (NhaTre) | ấp trứng → pet (NhaTre) |
| `hst:110` | Chậu hoa berry | ngu | ok | công thức 21 Chậu hoa berry | trang trí vườn (điểm trang trí) |
| `hst:111` | Hồ cá mini | ngu | ok | công thức 22 Hồ cá mini | trang trí vườn (điểm trang trí) |
| `hst:112` | Tượng đá Onix | ngu | ok | công thức 23 Tượng đá Onix | trang trí vườn (điểm trang trí) |
| `hst:113` | Đèn lồng Chinchou | ngu | ok | công thức 24 Đèn lồng Chinchou | trang trí vườn (điểm trang trí) |
| `hst:114` | Bộ sưu tập hoá thạch | ngu | ok | công thức 25 Bộ sưu tập hoá thạch | trang trí vườn (điểm trang trí) |
| `hst:115` | Bể san hô | ngu | ok | công thức 28 Bể san hô | trang trí vườn (điểm trang trí) |
| `hst:116` | Tượng rồng Dratini | ngu | ok | công thức 29 Tượng rồng Dratini | trang trí vườn (điểm trang trí) |
