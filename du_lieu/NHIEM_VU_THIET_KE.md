# Thiết kế nhiệm vụ: Huyền Thoại Bảo Bối

> Trả lời góp ý "nhiệm vụ hơi ít, không đủ để người chơi chơi". Bảng cũ có 721 dòng, khoảng 40% là "hạ N con".
> Bảng mới có khoảng 1.860 dòng, dùng 45 kiểu mục tiêu. Mọi dòng đều có tên, mô tả ngắn, lời thoại lúc nhận,
> lúc đang làm và lúc xong, văn bản mục tiêu, gợi ý và dòng thưởng.
>
> **Nguồn duy nhất** là các bảng chữ trong `tools/pokemon/nv_*.py`. Bộ sinh là `tools/luu_tru_sinh_du_lieu/pokemon/sinh_nhiem_vu.py`,
> được gọi từ `sinh_sql_lau_dai.py`, nên chỉ cần chạy:
>
> ```
> python3 -I tools/luu_tru_sinh_du_lieu/pokemon/sinh_nhiem_vu.py
> ```
>
> Lệnh này sinh lại `nhiem_vu_moi.csv`, `tools/luu_tru_sinh_du_lieu/sql_nguon/pokemon_lau_dai.sql`, `tools/luu_tru_sinh_du_lieu/sql_nguon/pokemon_nhiem_vu.sql`,
> `nhiem_vu_nhip_do.csv`, `web/nhiem_vu_tom_tat.json` và các bảng nằm giữa `<!-- SINH -->` trong file này.
> Kiểm thử: `test/KiemNhiemVu.java` (đã đăng ký trong `test/Kiem.java`) và `test/KiemLauDai.java`.
>
> Bộ sinh chỉ dùng thư viện chuẩn và tất định: cùng đầu vào cho ra cùng đầu ra.

## 1. Số lượng

<!-- SINH:DEM -->
| loai | nhom | so_dong |
|---|---|---|
| an | an | 32 |
| chinh | chinh | 582 |
| doi | doi | 12 |
| hang_bi_an | ham_nguc | 6 |
| huong_dan | huong_dan | 10 |
| huyen_thoai | huyen_thoai | 91 |
| ngay | bang_ngay | 216 |
| ngay | truy_na | 17 |
| phu | chuoi_dac_trung | 149 |
| phu | chuoi_npc | 264 |
| pokedex | pokedex | 154 |
| pokedex | sinh_thai | 40 |
| quay_lai | quay_lai | 6 |
| su_kien | chien_vung | 9 |
| su_kien | cong_dong | 21 |
| su_kien | dot_kich | 15 |
| su_kien | le_vn | 48 |
| su_kien | mua | 18 |
| su_kien | pokemon | 15 |
| su_kien | pvp | 12 |
| su_kien | tuan_he | 18 |
| su_pham | su_pham | 8 |
| thanh_tuu | thanh_tuu | 114 |
| tuan | chuoi_vung | 39 |
| tuan | to_doi | 16 |
| tuan | tuan | 48 |

**Tổng: 1960 dòng.**
<!-- /SINH:DEM -->

So với mục tiêu:

| Nhóm | Mục tiêu | Hiện có |
|---|---|---|
| Tổng | ≥ 1.800 | xem trên |
| Cốt truyện năm 1 | 52 chương × 6–10 bước | 52 chương, 6–7 bước, mỗi chương có câu treo |
| Năm 2 viết đầy đủ | ≥ 26 chương | 28 chương (H1–H7). 24 chương H8–H13 còn là dàn ý |
| Nhiệm vụ phụ | ≥ 400, theo chuỗi NPC | 413 dòng: 33 chuỗi đặc trưng của từng nơi + 66 chuỗi mẫu (29 truyện mẫu × 2 nhân vật) |
| Ngày / truy nã | ≥ 200 | 233 (5 bậc cấp; mỗi ngày giao 5, thưởng tối đa 3) |
| Tuần | ≥ 80 | 103: 48 `T-` (engine hiện có), 39 chuỗi vùng `TV-`, 16 tổ đội `TD-` |
| Pokédex | ≥ 150 | 194: gặp / hạ 50 / shiny, cộng 40 bài quan sát sinh thái |
| Thành tựu | ≥ 100 | 114, chia bậc trong 28 nhóm |
| Sự kiện | ≥ 52 × 3 | 156 (3 bước mỗi tuần trong `su_kien_nam.csv`) |
| Chuỗi huyền thoại | ≥ 10 | 13 chuỗi × 7 bước, mỗi bước mở sau bước trước 7 ngày |
| Ẩn | ≥ 30 | 32 |

## 2. Cốt truyện

**Năm 1: "Mảnh Sao Băng".** Bạn tỉnh dậy ở đồng cỏ Pallet đúng đêm sao băng rơi và không nhớ gì.
- Xatu, trưởng làng, thấy trước tương lai và biết dịch chuyển; vì thế Xatu có mặt ở mọi làng. Ông kể rằng ngôi sao vỡ thành mười sáu mảnh, mỗi Thủ lĩnh Gym giữ một mảnh.
- Đoàn Mặt Nạ Tím săn các mảnh sao cho "Kẻ Vô Danh". Murkrow Mặt Nạ là tên trinh sát nói nhiều; Honchkrow là phó thủ lĩnh lạnh lùng.
- Lém là một Zorua hay hoá trang. Cậu là đối thủ của bạn, về sau thành bạn.

Các chặng chính:
- Kanto (chương 1–28): mảnh sao ở Núi Mặt Trăng, bí ẩn trên tàu S.S. Anne, ác mộng ở Lavender (Cubone và Marowak), Tòa nhà Silph, nhật ký Cinnabar ghi về "Đá Thức Tỉnh". Cuối Kanto, Honchkrow cướp hết mảnh sao.
- Johto (chương 29–52): tàn tích Unown, giếng Slowpoke, đền Celebi (ảo ảnh nghìn năm), Tháp Cháy và Entei, hải đăng Olivine, Hồ Phẫn Nộ, trạm phát sóng ác mộng ở Mahogany, Tổ Zapdos, lời thề ở đền rồng.
- Đường Victory và Cao nguyên Indigo đưa bạn lên Núi Silver. Ở đó Kẻ Vô Danh lộ mặt: Darkrai, kẻ muốn cả thế giới ngủ để không ai phải đau.
- Bạn chọn **tha thứ** hoặc **phong ấn**; mỗi lựa chọn cho một danh hiệu khác nhau.

Đủ 16 huy hiệu Gym, ở đúng chương đã khai trong `noi_dung_gym.csv`. Bước Gym nay nằm ở giữa hoặc cuối chương, không còn cố định là `.4`. Vì vậy `sinh_noi_dung.py` tìm bước Gym theo `huy_hieu`.

**Năm 2: Hoenn.** Bạn nhớ lại mình sinh ra ở Littleroot.
- Đoàn Dung Nham muốn mở rộng đất liền, Đoàn Thủy Triều muốn mở rộng biển; cả hai đều cần mảnh sao.
- Câu chuyện đi qua Groudon, Kyogre và Sootopolis rồi tới Tháp Sky Pillar. Mẹ bạn từng canh giữ tháp này; Rayquaza cho biết chính bạn là mảnh sao cuối cùng.
- Cả 52 chương (C53–C104) viết đầy đủ trên 29 bản đồ Hoenn mới (`src/com/htbb/pokemon/BanDoHoenn.java`; C53–C80 ở `nv_cot_truyen.NAM2`, C81–C104 ở `tools/luu_tru_sinh_du_lieu/pokemon/nv_hoenn.py`). Mỗi bước có `map_id` + toạ độ (điểm nhiệm vụ Hoenn trong `diem_co_dinh.csv`, câu cá lấy ô hồ trong `diem_cau_ca.csv`); quái mục tiêu là loài Hoenn mọc trên bản đồ đó (`nv_hoenn.chuan_buoc`).

**Giọng nhân vật.** Mỗi NPC có câu mở đầu và cách xưng hô riêng, khai báo trong `nv_chung.NPC`:
- Xatu "...", Snorlax "Oáp...", Blaziken "Lửa lên nào!"
- Machamp "Khà khà!", Kecleon "Hí hí,", Meowth "Meo!"
- Torkoal "Khụ khụ...", Bronzong "Boong...", Kangaskhan "Ôi con yêu,"

Lời "đang làm" của mọi nhiệm vụ đều tự sinh theo giọng của NPC giao nhiệm vụ.

## 3. Nhịp độ

<!-- SINH:NHIP -->
| dai_cap | gio_len_cap_mo_phong | gio_noi_dung_mot_lan | phan_tram_nhiem_vu | so_nhiem_vu |
|---|---|---|---|---|
| 1-15 | 0.8 | 27.9 | 100 | 395 |
| 16-30 | 6.2 | 24.7 | 100 | 240 |
| 31-45 | 25.2 | 26.4 | 100 | 235 |
| 46-60 | 61.3 | 25.3 | 41 | 239 |
| 61-75 | 141.6 | 32.9 | 23 | 200 |
| 76-90 | 321.3 | 19.5 | 6 | 191 |
| 91-100 | 326.0 | 53.2 | 16 | 346 |
<!-- /SINH:NHIP -->

Đọc bảng:
- Từ cấp 1 đến 45, lượng nhiệm vụ một lần nhiều hơn thời gian cần để lên cấp. Người chơi lên cấp chủ yếu nhờ nhiệm vụ và luôn có việc mới.
- Từ cấp 60 trở lên, trần cấp theo mùa và năng lượng là thứ giữ nhịp. Nhiệm vụ một lần chiếm 6–20% thời gian; phần còn lại là ngày, tuần, huyền thoại, sự kiện, Pokédex, Gym đánh lại và hầm ngục.

Nội dung lặp lại theo người chơi:
- **Người chơi nhẹ (1–2 giờ mỗi ngày):** 1 chuỗi phụ (khoảng 12 phút mỗi bước), 3 trong 5 nhiệm vụ ngày được giao, 2 vé chương mỗi tuần, 2 nhiệm vụ tuần.
- **Người chơi cày:** làm hết các nhiệm vụ trên, thêm chuỗi huyền thoại (một bước mỗi tuần), 3 bước sự kiện mỗi tuần, thành tựu, nhiệm vụ ẩn và hầm ngục.

Mô phỏng (`MoPhongKinhTe`) cho kết quả nhóm nhẹ xong năm 1 ở tháng **13,8** (khoảng cho phép 12–18) và vẫn đạt 12/12 mục tiêu.

Số lựa chọn có ý nghĩa tại mỗi cấp, đếm trong cửa sổ lọc của engine (lv ≤ cấp < lv + 10):

<!-- SINH:LUA_CHON -->
| cap | chinh | chuoi_phu | nhiem_vu_phu | ngay_giao | tuan | pokedex | huyen_thoai | su_kien | lua_chon_y_nghia | goi_danh_sach_phu |
|---|---|---|---|---|---|---|---|---|---|---|
| 1 | 8 | 1 | 1 | 5 | 0 | 18 | 0 | 0 | 12 | 13 |
| 5 | 28 | 4 | 10 | 5 | 0 | 31 | 0 | 3 | 18 | 22 |
| 10 | 41 | 9 | 32 | 5 | 14 | 53 | 0 | 3 | 29 | 44 |
| 15 | 25 | 13 | 42 | 5 | 16 | 41 | 0 | 3 | 33 | 54 |
| 20 | 31 | 12 | 38 | 5 | 17 | 31 | 0 | 3 | 33 | 50 |
| 30 | 42 | 10 | 31 | 5 | 33 | 27 | 0 | 3 | 31 | 43 |
| 40 | 38 | 11 | 36 | 5 | 22 | 29 | 1 | 3 | 33 | 48 |
| 50 | 37 | 14 | 43 | 5 | 22 | 19 | 1 | 3 | 36 | 55 |
| 60 | 37 | 18 | 60 | 5 | 22 | 11 | 3 | 3 | 42 | 72 |
| 70 | 37 | 20 | 59 | 5 | 20 | 12 | 4 | 3 | 45 | 71 |
| 80 | 30 | 15 | 46 | 5 | 33 | 9 | 4 | 3 | 40 | 58 |
| 85 | 30 | 10 | 32 | 5 | 23 | 5 | 4 | 3 | 35 | 44 |
| 90 | 39 | 11 | 32 | 5 | 21 | 3 | 3 | 3 | 33 | 44 |
| 95 | 121 | 11 | 34 | 5 | 22 | 0 | 6 | 3 | 33 | 46 |
| 100 | 260 | 10 | 36 | 5 | 21 | 0 | 8 | 3 | 34 | 48 |

Ít lựa chọn nhất: cấp 1 (12). Gói danh sách phụ lớn nhất (trước khi engine cắt 20): cấp 73 (76 ≤ 127).
<!-- /SINH:LUA_CHON -->

`goi_danh_sach_phu` là số nhiệm vụ phụ cộng nhiệm vụ Đội lọt cửa sổ trước khi engine cắt còn 20. Con số này luôn ≤ 127, tức trong giới hạn gói ghi số lượng 1 byte. Toàn bộ số liệu theo từng cấp nằm ở `tools/luu_tru_sinh_du_lieu/du_lieu/nhiem_vu_nhip_do.csv`.

## 4. Ngân sách thưởng

Tổng ngân sách **không đổi** so với bảng 721 dòng (`sinh_nhiem_vu.tong_cu()` tính lại tổng cũ theo đúng công thức cũ):

| Nhóm | Cách giữ tổng |
|---|---|
| Chính năm 1 | Tổng mỗi chương bằng 4 bước cũ, chia cho 6–7 bước mới theo trọng số: bước 1 là 0,8, bước Gym hoặc bước cuối là 1,6. Bước Gym giữ 10 lượng khoá |
| Chính năm 2 | Tổng mỗi chương bằng 1 dòng dàn ý cũ (×4) |
| Phụ | Tổng giờ EXP, giờ xu và lượng khoá của 100 dòng phụ cũ cộng 30 dòng tuyến cũ, chia cho 413 dòng; bước cuối mỗi chuỗi có trọng số ×2 |
| Pokédex | Tổng của 154 dòng cũ, chia cho 194 dòng (40 bài quan sát có trọng số 0,5) |
| Đội, sư phạm, hầm ngục, quay lại | Tổng cũ (8 / 5 / 5 / 3 dòng) chia cho 12 / 8 / 6 / 6 dòng |
| Ngày, tuần | Thưởng mỗi dòng giữ như cũ, vì số lần nhận thưởng vẫn giới hạn 3/ngày và 2/tuần. Chuỗi vùng 3 bước tính là 1 nhiệm vụ tuần, nên mỗi bước nhận 1/3 thưởng |
| Thành tựu, huyền thoại, ẩn | Tổng 600 lượng khoá và 180 lượng của 30 thành tựu cũ. Huyền thoại (5 lượng khoá mỗi chuỗi) và ẩn (2 lượng khoá mỗi nhiệm vụ) được trừ vào quỹ này |
| Sự kiện | EXP và xu bằng 0. Thưởng là vật phẩm sự kiện (khoá, hết hạn) của chính tuần đó và một món trang trí, tức đã nằm trong ngân sách sự kiện tuần |

Thưởng không phải tiền nằm ở cột `thuong_khac`:
- `danh_hieu:<tên>` cho 283 danh hiệu mới;
- `trang_tri:<tên>`;
- `truyen:<trang truyện>`, các trang sử thi mở dần;
- `item:<id>xN`: token huyền thoại 1040–1049, Huy chương Gym 1039, Tem nghiên cứu 1054, nguyên liệu 1034–1036;
- `diem_bang:N`, điểm bảng ngày để đổi đồ trang trí.

Vật phẩm trong cột `vat_pham` luôn là trang bị **có chỉ số**, đúng bậc của cấp nhiệm vụ, lấy từ `noi_dung_trang_bi.csv`. Bảng cũ có 43/73 dòng trỏ vào khung chế tạo không có chỉ số; lỗi này đã được sửa.

## 5. Yêu cầu engine

Engine nhiệm vụ do người làm GĐ2 viết trong `src`; tài liệu này chỉ mô tả dữ liệu.

### 5.1 Dữ liệu engine đọc

- `data_quest_lau_dai` giữ nguyên 30 cột cũ, nên `DatabaseLauDai.napMetaNhiemVu` và `napNhiemVuTuan` vẫn chạy được.
- `data_quest_moi` vẫn cùng schema `data_quest`, nhưng nay có 757 dòng:
  - hướng dẫn cộng toàn bộ chính năm 1 (`mainsub=0`);
  - nhiệm vụ phụ (`mainsub=1`);
  - nhiệm vụ Đội (`mainsub=2`).
- Mỗi dòng có **kiểu dự phòng** (cột `du_phong`, dạng `type:tham_số`). Nhờ vậy engine cũ type 0–4 vẫn chơi trọn chuỗi chính dù chưa có kiểu mới: một bước "đứng tại điểm" sẽ thành "gặp NPC", và một bước "hạ Alpha" sẽ thành "hạ quái mạnh gần cấp".
- `content` được dựng từ lời thoại thật. Câu của NPC có tên người nói ở đầu; câu của người chơi đi theo tiền tố `0`.
- Nhiệm vụ lặp lại (ngày, tuần, sự kiện) **không** vào `data_quest_moi`. Engine mới xoay vòng các nhiệm vụ này.
- `data_nhiem_vu_noi_dung` (trong `tools/luu_tru_sinh_du_lieu/sql_nguon/pokemon_nhiem_vu.sql`) lấy khoá theo `ma`. Bảng giữ hội thoại 3 giai đoạn, gợi ý, `muc_tieu_text`, `dong_thuong`, `thuong_khac`, `nhanh_lua_chon`, `an`, `dieu_kien_mo`, `dieu_kien`, `map_id`, `toa_do`, `dap_an`, `phut`.
- `data_nhiem_vu_kieu` là danh mục kiểu mục tiêu kèm số dòng của từng kiểu.
- `data_vat_pham_nhiem_vu` liệt kê 41 vật phẩm nhiệm vụ mới, id 10..50.

### 5.2 Việc engine cần làm (theo thứ tự ưu tiên)

1. **Gating theo `truoc`** cho cả nhiệm vụ phụ. Engine cũ không có điều kiện trước, nên nếu thiếu bước này thì cả 5 bước của một chuỗi hiện ra cùng lúc. Ngoài ra cần lọc `ngay_mo` và `an=1` (chỉ hiện khi đã đạt `dieu_kien_mo`).
2. **Nhiều nhiệm vụ phụ cùng lúc.** Mỗi chuỗi NPC nên là một "khe" riêng, tối đa 3 khe; tiến độ lưu trong `tob_nhiem_vu_lau_dai`. Gói danh sách vẫn ≤ 20 (đã kiểm ≤ 127).
3. **Bảng ngày:** mỗi ngày (05:00) chọn ngẫu nhiên 5 mẫu `N-<bậc>.*` theo bậc cấp. Hạt giống là `id_char + ngày`. Người chơi được thưởng tối đa 3 (`GIOI_HAN_NGAY`). Bậc theo cấp: 1–19, 20–39, 40–59, 60–79, 80+.
4. **Bảng tuần:** đếm chung giới hạn 2 mỗi tuần cho `T-`, `TV-` (cả chuỗi tính 1) và `TD-`. Hiện `daNhanTuanNay` chỉ đếm `T-`. Chuỗi `TV-` phải làm theo thứ tự `truoc`.
5. **Hội thoại nhiều người nói:** mỗi câu có dạng `Tên: câu`, câu của người chơi là `Bạn: câu`. Trong "đang làm", `{da}` phải được thay bằng tiến độ hiện tại.
6. **Kiểu mục tiêu mới:** xem bảng 5.3 (cột trạng thái `gd2` hoặc `he_sinh_thai`). Tham số nằm ở các cột `muc_tieu_id`, `so_luong`, `map_id`, `toa_do` (bán kính 3 ô), `dieu_kien`:
   - `dieu_kien_quan_sat=trời mưa|ban đêm|...`
   - `gioi_han_phut=N`
   - `to_doi_toi_thieu=N`
   - `thanh_vien_duoi_cap=20`
   - `cach_buoc_truoc_ngay=7`
7. **Lựa chọn:** `nhanh_lua_chon` có dạng `A:<chữ>=>danh_hieu:<tên>|B:...`. Engine hiện menu 2 dòng, ghi nhánh đã chọn vào `tob_nhiem_vu_lau_dai` (`trang_thai` hoặc cột mới `nhanh`) và trao danh hiệu tương ứng.
8. **Câu đố:** so chat của người chơi (đã bỏ dấu, không phân biệt hoa thường) với `dap_an` ở đầu `MapXaHoi.doChatBinhThuong`, **trước** khi phát lên kênh. Đáp án không được gửi xuống client và không có trên web.
9. **Thưởng ngoài tiền:** mỗi mục trong `thuong_khac` (`;` ngăn cách) được trao như sau:
   - `danh_hieu`: `Char.addDanhHieu`;
   - `item:<id>xN`: vật phẩm khoá;
   - `trang_tri`: `TrangTriPokemon`;
   - `truyen`: sổ truyện, tức menu "Sổ tay huấn luyện";
   - `diem_bang`: cột điểm mới.
10. **Vật phẩm nhiệm vụ mới** (GĐ2 4.5): `data/ItemQuest.java` đè class trong jar để mở rộng tên từ id 10 (`data_vat_pham_nhiem_vu`). Dự phòng type 2 hiện tạm dùng id 6.
11. **Thành tựu:** bộ đếm theo `kieu_muc_tieu = thanh_tuu_<nhóm>`. Cột `ghi_chu` ghi nguồn đếm của từng nhóm (`nv_chung.THANH_TUU_CO_CHE`).
12. **Sự kiện:** chỉ mở `SK-<tuần>.*` khi `LichSuKien` đang chạy tuần đó (`tob_su_kien`). Bước 2 dùng vật phẩm sự kiện có sẵn của tuần.

### 5.3 Kiểu mục tiêu, cơ chế phát hiện và trạng thái

`co_san` nghĩa là engine cũ làm được ngay.
- `gd2`: cần engine mới; hook ở cột cơ chế được lấy từ khảo sát `src` ngày 17/10.
- `he_sinh_thai`: chờ `sv.he_sinh_thai` (`KyNangSong`, `NhaTre`, `SinhThaiTheGioi` đang là lớp thuần, chưa gọi từ `real/`).
- `gd3`: chờ bản đồ Hoenn.

<!-- SINH:KIEU -->
| kiểu | tên | cơ chế phát hiện (server) | dự phòng engine cũ | trạng thái | số dòng |
|---|---|---|---|---|---|
| `ap_trung` | Trứng nở | NhaTre + HeSinhThaiMenu.hanhDongTrung (trứng nở sau 250–800 quái hạ) | — | he_sinh_thai | 12 |
| `bo_trang_bi` | Bộ trang bị | Char.calculateAttrib / checkKickAtt5 (đếm món cùng bậc) | — | gd2 | 5 |
| `boss_the_gioi` | Boss thế giới | BossLauDai.satThuongVaoBoss + khiChet (bảng đóng góp) | — | gd2 | 20 |
| `cau_ca` | Câu cá | HeSinhThaiMenu.cauCa (lệnh chat "cauca"): CHỈ ở hồ, đứng cách hồ ≤ 2 ô (du_lieu/diem_cau_ca.csv) | 2 | he_sinh_thai | 29 |
| `cau_do` | Câu đố | MapChat.doChat (cạnh HeSinhThaiGame.khiChat; so đáp án trước khi phát kênh) | — | gd2 | 65 |
| `che_tao` | Chế tạo | MapCheTao.doUserCreateItem | — | gd2 | 13 |
| `cho_ban` | Chợ | HeSinhThaiMenu.rao + mua/traTienNguoiBan (Chợ sinh thái) hoặc banNpc | — | he_sinh_thai | 12 |
| `dan_y` | Dàn ý | Chương chưa viết (không còn dùng: 52 chương năm 2 đã có bản đồ Hoenn) | — | gd3 | 0 |
| `dang_nhap` | Đăng nhập | LauDaiNhanVat.khiDoiNgay (QuaHoatDong) | — | gd2 | 1 |
| `dao_dat` | Khảo cổ | HeSinhThaiMenu.dao (lệnh chat "dao" qua HeSinhThaiGame.khiChat); chỉ hang/núi/sa mạc/tuyết, 6 điểm/ngày | 2 | he_sinh_thai | 20 |
| `den_noi` | Khám phá | Map.playerJoin / MapXuLyLenh.doChangeMap (hook vào bản đồ) | 3 | gd2 | 58 |
| `do_vui` | Đố vui | HeSinhThaiGame.DoVui.traLoi (qua khiChat, 12:30 và 21:30) | — | he_sinh_thai | 9 |
| `doi_dong_gop` | Đóng góp Đội | MapDoi (nhiệm vụ Đội, mainsub=2) + ChiemGymLauDai.dongGop | 4 | gd2 | 8 |
| `dua_pokemon` | Đua Pokémon | HeSinhThaiGame.Dua.dangKy / ketThuc (19:00, map 18) | — | he_sinh_thai | 13 |
| `dua_thu` | Đưa thư | Char.OnQuest type 1 (vật phẩm nhiệm vụ 4, npcres) | 1 | co_san | 18 |
| `dung_toa_do` | Đứng tại điểm | CharCapNhat.doPlayerMove (vị trí, bán kính 3 ô) | 3 | gd2 | 68 |
| `giao_hang` | Giao hàng | Char.OnQuest type 1 + vật phẩm nhiệm vụ mới (GĐ2 4.5) | 1 | gd2 | 31 |
| `giet` | Hạ quái | Monster.onDropItem → Char.checkFinishQuest (type 0) | 0 | co_san | 120 |
| `giet_lech_cap` | Hạ quái cùng tầm | Char.checkFinishQuest type 4 (deltaLv 5) | 4 | co_san | 77 |
| `giet_tinh_anh` | Thách đấu | Char.checkFinishQuest type 0 trên quái cấp cao hơn | 0 | co_san | 41 |
| `giup_tan_thu` | Giúp người mới | Region party + cấp thành viên < 20 | 4 | gd2 | 10 |
| `ha_alpha` | Hạ Alpha | Cờ alpha trên Monster (GĐ2 6.7, MapNapTaiNguyen.loadMap) → Monster.onDropItem | 0 | gd2 | 20 |
| `ha_gym` | Thắng Gym | Boss Gym (noi_dung_gym, GĐ2 4.6 + BossLauDai.khiChet) | 0 | gd2 | 19 |
| `ho_tong` | Hộ tống | Giao hàng (CharSuKien.doResponseTieu) với hàng nhiệm vụ | 3 | gd2 | 19 |
| `kem_cap` | Sư phạm | SoTayLauDai.suPham / khiLenCap (tob_su_pham) | — | gd2 | 8 |
| `kham_ngoc` | Khảm ngọc | MapEpNgoc.doFinishKhamItem | — | gd2 | 7 |
| `lam_vuon` | Làm vườn | Char.doHarvest → HeSinhThaiGame.khiThuHoach (vườn berry trên bản đồ cá nhân) | 2 | he_sinh_thai | 28 |
| `lua_chon` | Lựa chọn | Menu NPC (MapDoMenu) ghi nhánh vào tob_nhiem_vu_lau_dai | 3 | gd2 | 14 |
| `nang_cap` | Mặc trang bị | Map.doUseItem (mặc đồ) | 3 | co_san | 2 |
| `nang_cap_cong` | Nâng cấp | NangCapLauDaiGame.thu (MapEpNgoc.doFinishImbue, CharCapNhat.doAutoImbue) | — | gd2 | 13 |
| `nau_an` | Nấu ăn | HeSinhThaiMenu.lamCongThuc (menu Hệ sinh thái → Công thức) | — | he_sinh_thai | 22 |
| `noi_chuyen` | Nói chuyện | Char.OnQuest type 3 (hoàn thành ngay tại NPC) | 3 | co_san | 499 |
| `phan_ra` | Phân rã | RoiLauDai.phanRa (MapXuLyLenh.doGetItem) | — | gd2 | 8 |
| `pokedex_gap` | Pokédex: gặp | PokedexPokemon.khiDanh (tob_pokedex) | — | gd2 | 72 |
| `pokedex_ha` | Pokédex: nghiên cứu | PokedexPokemon.khiHaQuai (tob_pokedex.so_ha) | — | gd2 | 70 |
| `pokedex_shiny` | Pokédex: shiny | RoiLauDai.shiny → tob_pokedex_shiny | — | gd2 | 22 |
| `pokedex_vung` | Pokédex vùng | tob_pokedex (đếm loài theo vùng) | — | gd2 | 14 |
| `quan_sat` | Quan sát | PokedexPokemon.khiDanh + HeSinhThaiGame.khiHaQuai (thoiTietMap, laDem 19:00–05:59) | — | gd2 | 123 |
| `su_kien_vat_pham` | Vật phẩm sự kiện | Monster.onDropItem (khối rơi sự kiện) + MapSuKien | — | gd2 | 52 |
| `tang_hang` | Hầm ngục | HamNgucGame.khiHaQuai / nhanThuong (tob_ham_nguc) | — | gd2 | 11 |
| `tham_gia_gym` | Chiếm Gym | ChiemGymLauDai.batDau / dongGop | — | gd2 | 11 |
| `thang_pvp` | Đấu trường | ThangPvpGame.ketQua(thắng, thua) | — | gd2 | 27 |
| `thu_thach_gio` | Thử thách giờ | Bộ đếm giờ trong tob_nhiem_vu_lau_dai + checkFinishQuest | — | gd2 | 27 |
| `thu_thap` | Thu thập | Map.doGetItemQuest → checkFinishQuest (type 2) | 2 | co_san | 52 |
| `tim_vat_an` | Tìm vật ẩn | Điểm tương tác ẩn trên bản đồ (MapDoMenu, NPC ẩn) hoặc điểm đào | 2 | gd2 | 49 |
| `to_doi` | Tổ đội | Region.doAttackMonster (vòng party.userParty) — cộng cho cả tổ | 4 | gd2 | 27 |
| `thanh_tuu_*` | Thành tựu | bảng tiến độ + bộ đếm (xem cột ghi_chu) | — | gd2 | 114 |
<!-- /SINH:KIEU -->

Ghi chú về hook (từ khảo sát `src`):
- **Hạ quái:** đi qua `Monster.onDropItem`, rồi tới `Char.checkFinishQuest` (type 0 và 4) và `RoiLauDai.roi`, nơi gọi `NhiemVuLauDaiGame.khiHaQuai`. Hiện chỉ người kết liễu được tính; muốn có kiểu `to_doi` thì phải cộng tiến độ cho cả `party.userParty`.
- **Alpha:** chưa có cờ alpha nào. Phải thêm ở `MapNapTaiNguyen.loadMap` hoặc `MonsterTemplate` (GĐ2 6.7).
- **Gym:** chưa có trận 1 đấu 1 với Thủ lĩnh; "Gym" mới chỉ là tên hiển thị của chiếm thành. Cần boss Gym theo `noi_dung_gym.csv`, chết thì gọi `BossLauDai.khiChet`.
- **Vào bản đồ:** `Map.playerJoin` / `MapXuLyLenh.doChangeMap`. Cần chú ý các lớp con ghi đè (MapLoiDai, MapLienDau, MapClan...).
- **Vị trí:** `CharCapNhat.doPlayerMove`, nơi đã gọi `QuaHoatDong.hanhDong`. Chỉ cần kiểm khi người chơi đổi ô.
- **PvP:** dùng một hook duy nhất là `ThangPvpGame.ketQua(thắng, thua)`.
- **Hộ tống:** `CharSuKien.doResponseTieu`. Trường hợp bị cướp đi qua `Map.doUseItem` (`isThief`).
- **Nâng cấp:** `NangCapLauDaiGame.thu`. Chế tạo: `MapCheTao.doUserCreateItem`. Khảm ngọc: `MapEpNgoc.doFinishKhamItem`. Phân rã: `RoiLauDai.phanRa`.
- **Pokédex:** `PokedexPokemon.khiDanh` / `khiHaQuai`. Shiny: `RoiLauDai.shiny`.
- **Boss thế giới:** `BossLauDai.satThuongVaoBoss` (đóng góp) và `khiChet`.
- **Lên cấp:** `SoTayLauDai.khiLenCap`. Đổi ngày: `LauDaiNhanVat.khiDoiNgay`.

### 5.4 Phối hợp hệ sinh thái

Các mục tiêu đã đối chiếu với `docs/thiet-ke-game.md` và các lớp `VatLieuSinhThai`, `KyNangSong`, `NhaTre`, `SinhThaiTheGioi`, `HeSinhThaiGame`, `HeSinhThaiMenu`, `MiniGameSinhThai` (18/10). `test/KiemNhiemVu` kiểm lại mỗi lần chạy.

| Kiểu | Hook của hệ sinh thái | Điều khiển trong `dieu_kien` |
|---|---|---|
| `cau_ca` | `HeSinhThaiMenu.cauCa` (lệnh chat `cauca` qua `HeSinhThaiGame.khiChat`). Phải đứng cách ô nước thật tối đa 2 ô; tốn 2 thể lực và mồi câu | `vat_sinh_thai=<tên trong VatLieuSinhThai>`: Vảy Goldeen, Ngọc trai nhỏ, Vỏ sò. Riêng 2 bước truyện câu vật phẩm nhiệm vụ dùng `vat_nhiem_vu_khi_cau=<id>`; engine cho rơi thêm vật đó khi câu ở đúng bản đồ |
| `dao_dat` | `HeSinhThaiMenu.dao` (lệnh `dao`/`khaoco`). Chỉ đào được ở bản đồ loại hang, núi, sa mạc, tuyết; mỗi bản đồ có 6 điểm mỗi ngày | `khao_co=1` khi bản đồ thuộc 4 loại trên. Ngược lại là `diem_dao_nhiem_vu=1`: **engine nhiệm vụ cần đặt 1 điểm đào riêng** trên bản đồ đó và cho `dao` chấp nhận điểm này (10 dòng, như kho báu, hộp thư thời gian, gốc Berry ở Celadon) |
| `lam_vuon` | `Char.doHarvest` → `HeSinhThaiGame.khiThuHoach` (vườn berry trên bản đồ cá nhân) | `vat_sinh_thai=Berry Oran/Pecha/Chesto/Leppa/Lum`. Tên hiển thị trong truyện ("Hoa sương", "Nấm phát quang"...) được ánh xạ sang berry thật ở `nv_chung.VAT_SINH_THAI` |
| `nau_an` | `HeSinhThaiMenu.lamCongThuc` (menu Hệ sinh thái → Công thức) | `vat_sinh_thai=Bánh Poké` hoặc `Súp berry`. Các món trong truyện (bánh mật ong, bánh Lava, trà...) tính theo 2 công thức này |
| `ap_trung` | `NhaTre` + `HeSinhThaiMenu.hanhDongTrung`. Cứ 300 quái hạ được 1 trứng; trứng nở sau 250–800 quái | `trung=bat_ky`: trứng nào nở cũng tính |
| `do_vui` | `HeSinhThaiGame.DoVui.traLoi`, lúc **12:30 và 21:30** | — |
| `dua_pokemon` | `HeSinhThaiGame.Dua.dangKy`/`ketThuc`. Đăng ký từ 18:50, xuất phát 19:00 ở map 18 (tuyến Goldenrod), 5 mốc | — |
| `cho_ban` | `HeSinhThaiMenu.rao` cùng mua và `traTienNguoiBan`, hoặc `banNpc` | — |
| `quan_sat` | `PokedexPokemon.khiDanh` + `HeSinhThaiGame.khiHaQuai` (`thoiTietMap`, `laDem`) | `dieu_kien_quan_sat=`, nhận một trong các giá trị: `thoi_tiet=<Quang đãng/Nắng gắt/Mưa/Tuyết/Bão cát/Sương mù>`, `dem` (19:00–05:59), `gio=a-b` (khung giờ máy chủ cho buổi sáng 6–10, bình minh 5–7, hoàng hôn 17–19) hoặc `su_kien=congdong`. Hang và trong nhà luôn quang đãng |
| `cau_do` | Đặt cạnh `HeSinhThaiGame.khiChat` trong `MapChat.doChat`, và so đáp án **trước** khi phát lên kênh | `dap_an` |

Các phần không trùng nhau:
- 6 nhiệm vụ nghiên cứu mỗi loài của `SuuTamSinhThai` (2 lượng khoá mỗi nhiệm vụ) là hệ riêng. Pokédex sinh thái `DQ-*` ở đây chỉ dùng điều kiện quan sát, không trao thưởng nghiên cứu lần hai.
- Thể lực sinh hoạt (100 mỗi ngày) giới hạn tự nhiên số nhiệm vụ câu, đào và nấu làm được trong ngày.

## 6. File

| File | Nội dung |
|---|---|
| `tools/luu_tru_sinh_du_lieu/pokemon/nv_chung.py` | NPC và giọng nói, địa danh → bản đồ, kiểu mục tiêu (cơ chế, dự phòng, mẫu chữ), vật phẩm nhiệm vụ |
| `tools/luu_tru_sinh_du_lieu/pokemon/nv_cot_truyen.py` | 52 chương năm 1 và 28 chương năm 2 (dàn ý H8–H13 lấy từ `lau_dai_cot_truyen.py`) |
| `tools/luu_tru_sinh_du_lieu/pokemon/nv_phu.py` | 33 nơi; chuỗi đặc trưng mỗi nơi; 29 chuỗi mẫu |
| `tools/luu_tru_sinh_du_lieu/pokemon/nv_lap_lai.py` | Ngày/truy nã, tuần, chuỗi vùng, tổ đội, Đội, sư phạm, hầm ngục, quay lại, sự kiện, huyền thoại, ẩn, thành tựu |
| `tools/luu_tru_sinh_du_lieu/pokemon/sinh_nhiem_vu.py` | Bộ sinh: ngân sách, dự phòng, hội thoại, kiểm từ cấm, ghi file phụ |
| `tools/luu_tru_sinh_du_lieu/du_lieu/nhiem_vu_moi.csv` | 30 cột cũ cộng 19 cột mới |
| `tools/luu_tru_sinh_du_lieu/sql_nguon/pokemon_nhiem_vu.sql` | Bảng staging nội dung, đã thêm vào `xuat_sql_day_du.sh` sau `pokemon_lau_dai` |
| `tools/luu_tru_sinh_du_lieu/du_lieu/web/nhiem_vu_tom_tat.json` | Bản cho website: không có id, ngân sách hay đáp án |
