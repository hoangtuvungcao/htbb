# Nội dung đầy đủ cho 1–2 năm chơi: kiểm kê, thiết kế, chỗ còn thiếu

> Dành cho đội phát triển. Mọi con số ở đây sinh từ **một nguồn**:
> `tools/pokemon/sinh_noi_dung.py`. Script đọc ngân sách kinh tế từ `tools/pokemon/sinh_sql_lau_dai.py`
> (giá bậc, xu/giờ, trần cấp theo mùa, chi phí nâng cấp, TTK quái) và các bảng đã có
> (`sql/htbb_day_du.sql`, `bang_chieu.csv`, `bang_quai.csv`, `nhiem_vu_moi.csv`, `su_kien_nam.csv`).
> Muốn đổi số thì sửa script, chạy lại, rồi chạy `test/KiemNoiDung`. **Không sửa tay** các file sinh ra.
>
> Thiết kế gốc: `THIET_KE_LAU_DAI.md`. Hiện trạng kinh tế: `KIEM_TOAN_KINH_TE.md`.

## 0. Chạy và file sinh ra

```bash
python3 -I tools/pokemon/sinh_noi_dung.py     # ở thư mục HTBB/, chỉ thư viện chuẩn, tất định
```

| File | Nội dung |
|---|---|
| `sql/pokemon_noi_dung.sql` | **Phần A**: 19 bảng staging `data_noi_dung_*` (DROP/CREATE/INSERT). **Phần B**: `data_item` id **1001..1061** (DELETE dải 1001..1199 rồi INSERT). Chạy lại nhiều lần vẫn cho cùng kết quả |
| `sql/pokemon_noi_dung.sql` **Phần C** | `data_monster` id **139..178** (DELETE 139..199 rồi INSERT): 36 loài Hoenn cho dải 86–100 + 4 huyền thoại đột kích năm 2 (Articuno, Raikou, Suicune, Lugia). Id phải liền sau id lớn nhất (138), vì client cấp mảng template theo **số lượng** (gói 26, 1 byte) rồi tra theo id |
| `docs/pokemon/bang_quai.csv` | Thêm 40 dòng (id 139–178), giữ nguyên các dòng cũ. Bản sao `noi_dung_quai_moi_bang.csv` được `sinh_bang_quai.py` nối lại khi chạy lại |
| `docs/pokemon/noi_dung_*.csv` | 21 bảng nội bộ (có id, ánh xạ, công thức, nguồn) |
| `docs/pokemon/web/*.csv` | 14 bảng cho website: chỉ cột người chơi cần, không id, không công thức |
| `test/KiemNoiDung.java` | 45 kiểm tra (đăng ký trong `test/Kiem.java`) |

**Cách nạp vào game**
- `tools/pokemon/xuat_sql_day_du.sh` chạy `pokemon_noi_dung.sql` **sau** nhóm `LAU_DAI`, nên `sql/htbb_day_du.sql` sinh lại sẽ có đủ nội dung. Đã chạy thử toàn bộ pipeline trên một MariaDB 10.4 riêng (ra 937.921 byte, 143 bảng).
- Đã chạy `pokemon_noi_dung.sql` hai lần liền trên NovaDB (CSDL nháp `kpah_db_noidung`, đã xoá bảng sau khi kiểm). Kết quả: data_item 924 → 985 dòng, cả hai lần như nhau.
- Phần B được server nạp lúc khởi động (`Map.loadAllTemplate` → `DatabaseXepHang.loadItemTemplate`) và gửi xuống client (MessageCreator gửi đủ id 0..max). Icon dùng lại icon đã có (`xstart`), nên client không cần thêm ảnh.
- Hiện **chưa có nguồn rơi, cửa hàng hay nhiệm vụ nào trỏ tới** id 1001+. Vì vậy khi `sv.lau_dai=0`, game giữ nguyên hành vi cũ.
- Phần A là số liệu thiết kế. Giai đoạn 2/3 đọc các bảng này để nối vào code (xem mục 9).

## 1. Kiểm kê: đã có, đã thêm, còn thiếu

| Hạng mục | Đã có trong game | Bản này thêm | Còn thiếu (cần code/client, mục 9) |
|---|---|---|---|
| Trang bị 11 bậc × 11 ô | Bậc 0–8 (sơ cấp → huyền thoại) cho 13 type. **252 dòng "khung chế tạo"** có chỉ số 0, do code gán chỉ số (`CharManager.ARMOR_POINT`, `CreateItemTemplate`) | Catalog 154 món (11 bậc × 14 loại; vũ khí theo 5 dòng). Ngân sách chỉ số theo bậc, cấp, ngày mở, giá, phân rã, nguồn. **33 món mới**: bậc **thần thoại (90)** và **tối thượng (98)** cho đủ ô, cộng Ruy băng sơ cấp/đồng/bạc | Đồ năm 2 chưa có nguồn thật: cần NPC/rơi khi mở ngày 365/545 |
| Bộ trang bị | Không có | 11 bộ theo vùng (Pallet … Rayquaza) × mốc 4/7/11 món. Thưởng giống nhau mọi bậc | Code tính thưởng bộ |
| Nâng cấp | Đá luyện kim (ngọc 8), bảo hiểm (ngọc 0–4). GĐ2 đang nối pity và giá | Bảng vật liệu 11 bậc × +1..+15: xu = `CHI_PHI_NC`, đá, bùa. Từ +11 cần thêm 1 **nguyên liệu phụ theo nhóm bậc** (Bụi sao / Mảnh sao / Mảnh thiên thạch / Vảy rồng; item mới) | Code trừ nguyên liệu phụ từ +11 |
| Ngọc hệ | Đá khảm 12–59 theo chỉ số, không theo hệ | 18 hệ × 10 cấp: +1%/cấp, giá 1,45^(n−1) giờ thu nhập, phân rã ra n đá | Item/ngọc mới + `MapEpNgoc` theo hệ |
| Phân rã | "Nghiền đồ" có sẵn | Bảng ra/vào theo bậc: 2 đá + 10% giá. Hoàn mỹ ra thêm Mảnh tinh hoa và 1 nguyên liệu phụ | Code GĐ2 (bước 2.9) |
| Chế tạo | Xưởng Machamp (vải/sắt/ngọc cấp 4–5) | 22 công thức Vòng cổ / Phiến đá hệ theo bậc (đá + nguyên liệu phụ + 50% giá xu) | Menu chế tạo theo bảng |
| Thuốc HP/PP | Potion 1/2/3/22/26, Ether 4/5/6, Elixir 23/24 | Giá theo **12% thu nhập/giờ** (mục 3), dải cấp dùng | Đổi giá `data_shop`/`data_potion` khi bật cờ |
| Chữa trạng thái, xua quái, X mới | Không có | 6 thuốc trạng thái, 3 thuốc xua quái, X Tốc độ / Chính xác / Chí mạng, Thuốc hồi sinh tối đa | `data_potion` phải đủ đúng 162 dòng (`Char.SO_O_THUOC`). Thêm thuốc cần tăng số ô và client |
| Kẹo EXP | Kẹo kinh nghiệm S/M/L (potion 10–12, cộng **100k / 500k / 1 tỷ** EXP cố định, gây lạm phát) | **Kẹo Hiếm**: EXP bằng 30 phút đánh quái ở cấp hiện tại và **trừ 30 phút năng lượng 100%**, nên không vượt trần ngày. Tối đa 2/tuần, không bán | Code. Ngừng phát kẹo S/M/L. Đổi tên item 720 "Kẹo hiếm" (dùng cho thú nuôi) để khỏi trùng tên |
| Đá tiến hoá | 960–967 (8 nhánh Eevee) | Nguồn: Ngày cộng đồng Eevee, chương, Pokédex. Không bán | — |
| Quả mọng, đồ giữ | Trang trại `tob_fruit` (trái cây cũ) | 9 quả tự dùng (Oran, Sitrus, Lum, Cheri…). 8 đồ giữ (Thức ăn thừa, Chuông vỏ sò…) thay chỉ số phụ của ô Phiến đá hệ, nên không vượt ngân sách | Code |
| Chiêu | 9 ô (Eevee 11) × 10 cấp chiêu, tên đã đổi sang Pokémon. **Lệch bảng Bulbasaur đã được GĐ2 sửa** (`SKILL_DAM_PERCENT[1]` còn 16 dòng) | Cây chiêu theo dạng 1/2/3: cấp mở, 10 cấp chiêu tới cấp 100, % sát thương, số đòn, hồi chiêu, PP, hiệu ứng. Cân theo TTK (mục 5). Thêm 8 nhánh Eevee | Nạp số mới vào `CharManager` khi bật cờ |
| Quái theo dải cấp | 68 quái đặt trên map, 1–90 | 20 dải × loài/hệ/môi trường, **cả 20 dải đều đủ**. Dải 86–100 dùng 36 template Hoenn mới (id 139–174), có sprite và chân dung PMD đã đóng gói cho client | Spawn trên map Hoenn (đội map) |
| Quái tinh anh | Không | 67 bản Alpha (×4 HP, ×1,3 tấn công, rơi ×3, chắc rơi đá, 2% Hoàn mỹ; TTK ~20 giây) | Code spawn (GĐ2 bước 6.7) |
| Gym | 16 bước Gym trong nhiệm vụ (`huy_hieu`) | 16 thủ lĩnh: template hình, cấp, HP cho 105 giây đánh một mình, thưởng lần đầu và hằng tuần | Code boss Gym (GĐ2 bước 6.6) |
| Boss thế giới | Boss sự kiện cũ | Lịch 52 tuần: 5 đột kích huyền thoại (năm 2 xoay sang Articuno, Raikou, Suicune, Lugia, Kyogre), 47 tuần boss xoay vòng. HP cho 10 phút với 20 người. Hộp thưởng theo đóng góp | Sprite huyền thoại năm 2 |
| Bảng rơi | Rơi cũ theo `Map.dropItem` | 662 dòng: dải, alpha, Gym, boss, mở hộp | Code đọc bảng (GĐ2 2.2) |
| Vật phẩm sự kiện 52 tuần | Nhiều vật phẩm lễ cũ (bánh chưng, lì xì, bí ngô…) | Mỗi tuần ≥ 1 vật phẩm: dùng lại đồ cũ, thêm token mới (Tinh thể hệ, Cờ vùng, Huy chương mùa giải…). Đều **khoá, hết hạn**, trong ngân sách tuần | Code đổi quà |
| Trang trí theo mùa | Áo/mặt nạ cũ **có chỉ số** (60 dòng) | 6 mùa × 7 món (danh hiệu, khung, trang phục, hiệu ứng), **0 chỉ số** | Ô trang trí riêng (0 chỉ số), `DanhHieu` |
| Cửa hàng | `data_shop` cũ | 106 dòng: trang bị (Bronzong), thuốc (Chansey), lượng khoá (Kecleon), trang trí bằng lượng (Delcatty), Huy chương Gym | Nạp khi bật cờ |

## 2. Trang bị

### 2.1 Bậc, cấp, ngày mở, ngân sách

Ngân sách chuẩn của đồ (Trắng +0) bằng 60% chỉ số "trần" ở cấp của bậc:
- tấn công trần = 30 + 12·L;
- thủ = 25% tấn công;
- HP = 70·(25 + L).

Có thêm chặn trên: tổng chỉ số ở bậc mới phải ≤ **1,1 ×** tổng ở bậc cũ +15 (+55%) **cùng cấp** (THIẾT KẾ 3.6). Chặn này chỉ có tác dụng ở bậc đồng và bạc, vì khoảng cách cấp so với bậc sơ cấp lớn.

Bảng dưới là tổng 11 ô (đơn vị thiết kế).

| Bậc | Tên | Cấp | Ngày mở | Trần mùa lúc mở | Giá 1 món (xu) | Tấn công | Thủ | HP |
|---|---|---|---|---|---|---|---|---|
| 0 | sơ cấp | 1 | 0 | 60 | 5.385 | 22 | 4 | 1.090 |
| 1 | đồng | 10 | 0 | 60 | 8.328 | 49 | 6 | 1.469 |
| 2 | bạc | 20 | 0 | 60 | 13.527 | 105 | 13 | 1.889 |
| 3 | vàng | 30 | 0 | 60 | 20.704 | 213 | 29 | 2.309 |
| 4 | bạch kim | 40 | 0 | 60 | 32.340 | 302 | 60 | 2.729 |
| 5 | kim cương | 50 | 0 | 60 | 50.888 | 374 | 92 | 3.149 |
| 6 | chuyên gia | 60 | 0 | 60 | 79.101 | 449 | 111 | 3.569 |
| 7 | tinh anh | 70 | 60 | 70 | 122.664 | 520 | 129 | 3.989 |
| 8 | huyền thoại | 80 | 150 | 80 | 190.464 | 592 | 145 | 4.409 |
| 9 | thần thoại | 90 | 365 | 95 | 294.152 | 662 | 164 | 4.829 |
| 10 | tối thượng | 98 | 545 | 100 | 416.954 | 721 | 180 | 5.163 |

### 2.2 Ô và chia ngân sách

Dùng lại type cũ. Tên đã là đồ Pokémon.

| Ô | type | Số ô | Tấn công | Thủ | HP | Chỉ số phụ | Nguồn |
|---|---|---|---|---|---|---|---|
| Khăn quàng | 0 | 1 | | 35% | 25% | | cửa hàng, rơi |
| Đai | 1 | 1 | | 25% | 20% | | cửa hàng, rơi |
| Băng đô | 2 | 1 | | 20% | 15% | | cửa hàng, rơi |
| Vũ khí theo dòng (Than lửa / Hạt giống thần / Khăn lụa / Nước thần / Nam châm) | 3–7 | 1 | 40% | | | chiêu cùng hệ +10% + 1%/bậc (≤ 20%) | nhiệm vụ, rơi, Gym (Hoàn mỹ) |
| Huy hiệu | 8 | 2 | 10% mỗi cái | | | | Gym, nhiệm vụ |
| Vòng cổ | 9 | 1 | 15% | | 10% | | chế tạo, rơi |
| Lông vũ tốc độ | 10 | 1 | | 10% | 10% | tốc chạy +1/2 bậc | cửa hàng, rơi |
| Găng đấm | 11 | 1 | 15% | | | chí mạng +5‰ + 1‰/bậc (≤ 15‰) | cửa hàng, rơi |
| Phiến đá hệ | 12 | 1 | 10% | | | +5% sát thương 1 hệ **hoặc** −5% sát thương nhận từ 1 hệ; hoặc thay bằng 1 đồ giữ | chế tạo, sự kiện |
| Ruy băng | 19 | 1 | | 10% | 20% | | nhiệm vụ, boss thế giới |

- **5 ô mua ở NPC** (Bronzong), giá 1,5 giờ thu nhập: Khăn quàng, Đai, Băng đô, Lông vũ, Găng.
- **6 ô còn lại** lấy từ nhiệm vụ, Gym, chế tạo, rơi và boss. Khớp với THIẾT KẾ 3.1.
- **Độ hiếm**: Trắng 100%, Xanh 110%, Đỏ 120%, **Hoàn mỹ 130% (chỉ alpha, Gym, boss thế giới)**, Vàng (sự kiện, chỉ trang trí).
- **Chỉ số engine của 33 món mới** (`data_item.atb`): ngoại suy tuyến tính theo cấp từ các dòng thật cùng type. Như vậy thang chỉ số đang chạy giữ nguyên, kể cả ở chế độ cũ. Test chặn: ≤ 1,1 × (+15) của dòng thật mạnh nhất cùng ô.
- **Không sửa dòng cũ.** 252 dòng chỉ số 0 là khung chế tạo, chỉ số do code gán.

## 3. Nâng cấp, ngọc hệ, phân rã, bùa

- **Vật liệu nâng cấp** (`noi_dung_vat_lieu_nang_cap.csv`, 165 dòng):
  - xu theo `ThongSoLauDai.CHI_PHI_NC`;
  - đá (ngọc 8) 1/2/4 viên;
  - bùa 10/25 lượng khoá;
  - từ +11, thêm 1 nguyên liệu phụ: bậc 0–2 Bụi sao, 3–5 Mảnh sao, 6–8 Mảnh thiên thạch, 9–10 Vảy rồng.
  - Nguyên liệu phụ rơi 0,4%/quái từ cấp 40, có trong phân rã và hộp đột kích.
- **Bùa bảo hộ**: dùng lại ngọc bảo hiểm 0–2 cho +7..+10 và 3–4 cho +11..+15 (GĐ2 `NangCapLauDaiGame`).
- **Ngọc hệ**: 18 hệ × 10 cấp. Cấp n cho +n% chỉ số món, giá 1,45^(n−1) giờ thu nhập (ví dụ ở cấp 60 có trong CSV). Phân rã ra n đá.
- **Phân rã**: đồ thường ra 2 đá + 10% giá. Đồ Hoàn mỹ ra thêm 1 Mảnh tinh hoa và 1 nguyên liệu phụ. Cần 3 Mảnh tinh hoa để chuyển đồ Hoàn mỹ sang bậc mới.

## 4. Vật phẩm tiêu hao (`noi_dung_vat_pham.csv`, 53 dòng)

Giá xu tính theo công thức:

```
giá = xu_mỗi_giờ(cấp giữa dải) × 12% × phần_ngân_sách ÷ số_lần_dùng_mỗi_giờ
```

Test kiểm hai điều:
- mọi giá nằm trong 0,05–5% thu nhập 1 giờ;
- ở mọi cấp, tổng phần ngân sách của các thuốc đang dùng ≤ 100%. Tức tổng chi thuốc ≤ 12% thu nhập, đúng mục 2.2 của THIẾT KẾ.

| Nhóm | Món | Trạng thái |
|---|---|---|
| Hồi máu | Thuốc hồi máu (1–20), Siêu (21–40), Cực mạnh (41–60), Tối đa (61–80), Hồi phục toàn diện (81–100) | có sẵn (potion 1/2/3/22/26) |
| Hồi PP | Ether / Ether lớn / Ether tối đa, Elixir, Elixir tối đa | có sẵn (4/5/6/23/24) |
| Chữa trạng thái | Giải độc, Trị bỏng, Trị tê liệt, Đánh thức, Rã đông, Chữa toàn diện | cần code (`TrangThaiPokemon` 1..5) |
| Hồi sinh | Thuốc hồi sinh (119), Hồi sinh tối đa | 1 có sẵn, 1 cần code |
| Tăng cường X | X Tấn công (130), X Phòng thủ (129), Thuốc săn trùm (131), X Tốc độ, X Chính xác, Tăng chí mạng | 3 có sẵn, 3 cần code |
| Xua quái | Thường / Siêu / Tối đa (5/10/20 phút; quái thấp hơn 5 cấp không chủ động đánh) | cần code |
| Kẹo EXP | Kẹo Hiếm (trừ năng lượng, 2/tuần) | cần code |
| Đá tiến hoá | 8 đá 960–967 | có sẵn |
| Quả mọng | Oran, Sitrus, Lum, Cheri, Chesto, Pecha, Rawst, Aspear, Leppa | cần code |
| Đồ giữ | Thức ăn thừa, Chuông vỏ sò, Móng vuốt nhanh, Băng đô tập trung, Kính ngắm, Đai lực sĩ, Đá vĩnh cửu, Trứng may mắn | cần code (thay chỉ số phụ Phiến đá hệ) |

**Tiền:**
- **Lượng** chỉ mua trang trí (test chặn).
- **Lượng khoá** ≤ 25, luôn có giới hạn tuần.
- Đồ có chỉ số chỉ bán bằng xu.

## 5. Chiêu (`noi_dung_chieu.csv`, 47 dòng; `noi_dung_chieu_ttk.csv`)

- **Giữ đúng số hiệu và tên** của `bang_chieu.csv` (client lấy tên theo ô: `ap_ten_chieu.py`, `SkillManager.SKILL_NAME`). Test so từng dòng.
- **Mở theo giai đoạn tiến hoá** (`LoaiPokemon`):

  | Giai đoạn | Cấp | Chiêu mở |
  |---|---|---|
  | Dạng 1 | 1 / 4 / 7 | Chiêu 0, 1, 2, cộng bị động ô 5 ở cấp 6 |
  | Dạng 2 | 16 (Pichu 10) | Ô 3 (5 đòn) và ô 4 |
  | Dạng 3 | 36 / 32 / 25 / 36 / 30 | Ô 6–8 (Eevee 6–10), có tối thượng |

- **10 cấp chiêu**: cấp 1..9 trải từ cấp mở tới cấp 90. Cấp 10 cần cấp 95, tối thượng cần cấp 100 (mở ở năm 2). Mỗi cấp chiêu +2% sát thương.
- **Cân bằng theo TTK** (THIẾT KẾ 4.3):
  - Xoay chiêu tham lam 60 giây (GCD 0,7 giây) tính ra hệ số sát thương mỗi giây.
  - TTK = HP quái ÷ ((tấn công đồ +6 − thủ quái) × 1,1 × hệ số).
  - Hệ số mỗi giai đoạn được chọn (chia đôi) để TTK ở mốc giai đoạn = mục tiêu × chỉ số dòng × (1 / 0,92 / 0,80). Vì vậy tiến hoá cho cảm giác mạnh hơn.
  - Chỉ số dòng = HP × Tấn công × Thủ (THIẾT KẾ 4.4): dòng chịu đòn hạ quái chậm hơn nhưng sống lâu hơn. **Sức mạnh** (HP × Thủ × sát thương) của 5 dòng bằng nhau **±5%** ở mọi mốc (test).

  | Dòng | Dạng 1 (cấp 10) | Dạng 2 (cấp 30) | Dạng 3 (cấp 60) | Năm 2 (cấp 95) | Mục tiêu |
  |---|---|---|---|---|---|
  | Charmander (tấn công cận chiến) | 3,8 giây | 3,5 | 3,8 | 4,2 | 4 / 4 / 5 / 6 |
  | Bulbasaur (chịu đòn + khống chế) | 4,8 | 4,4 | 4,8 | 5,2 | |
  | Eevee (linh hoạt) | 3,4 | 3,1 (cấp 24) | 3,4 | 3,8 | |
  | Squirtle (chịu đòn chính) | 5,1 | 4,7 | 5,1 | 5,8 | |
  | Pichu (đánh xa) | 3,2 (cấp 9) | 2,9 (cấp 29) | 3,2 | 3,5 | |

- **Chiêu 5 đòn ô 3**: ~190–260% mỗi đòn ở cấp 10, hồi 6 giây. Đây là bản đã giảm so với 2.360% của game cũ.
- **Tối thượng**: hồi 15 giây, mạnh hơn mọi chiêu dạng 1.
- **8 nhánh Eevee** (`noi_dung_chieu_nhanh_eevee.csv`): đổi hệ ô 2/8/10 để có STAB. Ví dụ Vaporeon: Súng nước, Vòng nước, Thủy triều; Sylveon: Hôn tiên, Khí tiên, Ánh trăng tiên. Số liệu giữ như cây Eevee. Tên nhánh chỉ là thiết kế; client vẫn hiện tên theo ô.

## 6. Quái, alpha, Gym, boss thế giới

- **Dải cấp** (`noi_dung_quai_dai_cap.csv`): 20 dải 5 cấp, có loài, hệ và môi trường (Đồng cỏ, Rừng, Hang, Bờ biển, Núi lửa, Hồ, Tháp).
  - Dải 1–85 đều có ≥ 3 loài và ≥ 2 hệ.
  - **86–100 thiếu**. Đề xuất 36 loài Hoenn: Mightyena, Manectric, Camerupt, Altaria, Aggron, Sharpedo, Claydol, Banette, Absol, Glalie, Walrein, Shelgon, Metagross, Flygon… Mỗi loài có dex, hệ và cấp, dùng cho mùa S5/S6.
- **Alpha**: 67 bản. Xuất hiện 1/50 lần hồi sinh, to ×1,3, HP ×4, tấn công ×1,3, rơi ×3, chắc chắn rơi đá, 2% vũ khí Hoàn mỹ. TTK 16–24 giây.
- **16 Gym** (`noi_dung_gym.csv`):

  | Gym | Huy hiệu | Thủ lĩnh | Cấp |
  |---|---|---|---|
  | Pewter | Đá | Golem | 12 |
  | Cerulean | Thác | Gyarados | 19 |
  | Vermilion | Sấm | Luxray | 23 |
  | Celadon | Cầu Vồng | Vileplume | 35 |
  | Saffron | Đầm Lầy | Seismitoad | 38 |
  | Fuchsia | Linh Hồn | Gengar | 41 |
  | Cinnabar | Núi Lửa | Magcargo | 46 |
  | Viridian | Đất | Rhydon | 48 |
  | Violet | Gió | Dodrio | 53 |
  | Azalea | Tổ Ong | Heracross | 55 |
  | Goldenrod | Đồng Bằng | Tauros | 61 |
  | Ecruteak | Sương | Dusknoir | 62 |
  | Olivine | Khoáng | Aegislash | 66 |
  | Cianwood | Bão | Crabrawler | 68 |
  | Mahogany | Băng | Abomasnow | 75 |
  | Blackthorn | Rồng | Salamence | 83 |

  - HP đủ cho 105 giây đánh một mình đúng cấp với đồ +6; nộ sau 5 phút.
  - Lần đầu: huy hiệu + 1 vũ khí Hoàn mỹ theo dòng.
  - Đánh lại hằng tuần: 3 Huy chương Gym (đổi trang trí).
- **Boss thế giới 52 tuần** (`noi_dung_boss_the_gioi.csv`):
  - Đột kích tuần 9 / 20 / 28 / 36 / 48: Zapdos, Entei, Darkrai, Rayquaza, Groudon & Kyogre. Năm 2: Articuno, Raikou, Suicune, Lugia, Kyogre & Groudon.
  - Các tuần khác: 11 boss xoay vòng (Gyarados, Salamence, Sudowoodo, Abomasnow, Gengar…).
  - HP cho 10 phút với 20 người; nộ sau 15 phút.
  - Thưởng: đóng góp ≥ 0,5% nhận Hộp tham gia và vật phẩm đặc trưng (1–3); top 10 nhận Hộp vàng và 10% Ruy băng Hoàn mỹ. Mọi thứ đều khoá.

## 7. Bảng rơi (`noi_dung_roi.csv`, 662 dòng)

| Nguồn | Quy tắc (test kiểm) |
|---|---|
| Dải cấp | Trang bị **150 ppm/quái** (`roi.trang_bi.ppm`) chia đều các món "rơi" của bậc. Đá nâng cấp **15.000 ppm** (1,5%). Nguyên liệu phụ 4.000 ppm từ cấp 40. Thuốc HP của dải 2% |
| Alpha | Đá 100% (1–2 viên). Vũ khí Hoàn mỹ tổng 2% |
| Gym | Vũ khí Hoàn mỹ theo dòng (lần đầu). 3 Huy chương Gym/tuần |
| Boss thế giới | Hộp tham gia, hộp vàng, vật phẩm đặc trưng, Ruy băng Hoàn mỹ 10% (top 10). Khoá, hết hạn |
| Mở hộp | Hộp tham gia: 2–4 đá, 30% Thuốc săn trùm. Hộp vàng: 4–8 đá, 25% Mảnh tinh hoa, 20% Vảy rồng |

**Hoàn mỹ chỉ đến từ alpha, Gym và boss.** Thưởng boss và Gym luôn khoá.

## 8. Sự kiện, trang trí, cửa hàng, chế tạo

- **Sự kiện** (`noi_dung_su_kien_vat_pham.csv`, 72 dòng):
  - Mỗi tuần 1–3 vật phẩm, tối đa 20 cái/tuần.
  - Giá trị = ngân sách tuần của `su_kien_nam.csv`. Cả năm 66,5 giờ, test chặn ≤ 70.
  - Đều khoá và hết hạn khi sự kiện kết thúc.
  - Dùng lại đồ lễ cũ: Bánh chưng, Lì xì, Bánh tét, Hoa hồng, Bánh dẻo/nướng, Bí ngô, Dưa hấu, Kem…
  - Thêm mới: Tinh thể hệ, Huy hiệu ngày cộng đồng, Tem nghiên cứu, Huy chương mùa giải, Cờ vùng, Quà lễ 30/4, 2/9, 20/10, 20/11, Phiếu giảm giá trang trí, cùng 10 vật phẩm huyền thoại.
- **Trang trí** (`noi_dung_trang_tri.csv`): 6 mùa (S1 Kanto → S6 Hoenn 2) × 7 món, **0 chỉ số**.
  - Danh hiệu "Nhà vô địch / Nhà nghiên cứu mùa".
  - Khung ảnh linh vật (60 lượng), Khung bậc Master.
  - Trang phục linh vật (150 lượng), Áo choàng Gym (40 Huy chương).
  - Hào quang (đủ 8 Gym của vùng).
- **Cửa hàng** (`noi_dung_cua_hang.csv`):
  - Trang bị bằng xu (1,5 giờ thu nhập).
  - Thuốc bằng xu (≤ 5% giờ).
  - Bùa và thuốc hiếm bằng lượng khoá (≤ 25, có giới hạn tuần).
  - Trang trí bằng lượng.
- **Chế tạo** (`noi_dung_che_tao.csv`): Vòng cổ và Phiến đá hệ mỗi bậc cần (2 + bậc) đá, (1 + bậc/3) nguyên liệu phụ và 50% giá xu, làm ở Machamp (Xưởng Pewter).

## 9. Còn thiếu: cần code hoặc client (giao GĐ2/GĐ3)

1. **Thuốc mới** (trạng thái, xua quái, X mới, Kẹo Hiếm, quả mọng):
   - `data_potion` bắt buộc đúng 162 dòng (`Char.SO_O_THUOC`, client cấp phát ô theo số dòng). Muốn thêm phải tăng `SO_O_THUOC`, cập nhật `Map.kiemTraSoDongPotion`/`SuKienWorldCup` và icon client.
   - Hoặc làm thành `data_item` type 28 có `doUseItem` riêng.
2. **Nguyên liệu phụ từ +11 và Mảnh tinh hoa**: `NangCapLauDaiGame` chưa trừ. Item đã có id (1034–1038).
3. **Ngọc hệ 18 hệ**: ngọc là mảng cứng (`MapNapTaiNguyen.loadGemTemplate`, id 0..66). Cần ngọc mới hoặc item + `MapEpNgoc.doFinishKhamItem` theo hệ.
4. **Chiêu**: nạp `phan_tram_*`, `hoi_chieu_giay`, `cap_moi_cap_chieu` vào `CharManager.SKILL_DAM_PERCENT / SKILL_COOLDOWN / LEVEL_ADD_SKILL` khi `sv.lau_dai=1` (GĐ2 bước 6.3). Nhánh Eevee đổi hệ ở `ChieuPokemon.HE`.
5. **Quái năm 2 (dải 86–100)**: template + sprite + chân dung đã xong; còn thiếu **spawn** trên map Hoenn và gọi boss đột kích năm 2 (template 175–178).
   - Sprite: `tai_sprite_pmd.py` (227 loài, 0 lỗi) → `DongGoiSprite` → 33 loài mới (99 tệp `.bytes`) chép vào `htbb-client/pc/Assets` và `ClientUnity/Assets`. Sprite cũ trùng byte; `GHI_CONG.txt` thêm ghi công.
   - Gói J2ME: `cd htbb-client/j2me && HTBB_ASSETS=pc sh build.sh 54` (hoặc chỉ bước `DongGoiPokeJ2me ../pc/Assets res res_nhe`).
6. **Alpha, Gym, boss, bảng rơi**: GĐ2 bước 6.6–6.7 và 2.2 đọc `data_noi_dung_quai_tinh_anh / _gym / _boss_the_gioi / _roi`.
7. **Trang trí 0 chỉ số**: cần ô trang trí riêng, vì áo cũ nằm ở ô type 0 nên mặc vào là mất giáp. Danh hiệu mùa nằm trong `DanhHieu`.
8. **Phát hiện khi kiểm**: 43/73 thưởng vật phẩm trong `nhiem_vu_moi.csv` (do `sinh_sql_lau_dai.py: SinhNV.chon_do` chọn) trỏ vào **dòng khung chế tạo chỉ số 0**. Ví dụ 276/277 Khăn quàng bạch kim, 294/295 Đai bạc, 639/640 Khăn quàng huyền thoại.
   - Nếu thưởng tạo item thẳng từ template, người chơi nhận đồ 0 chỉ số.
   - Đề xuất: `chon_do` bỏ các id có `noi_dung_trang_bi.csv.trang_thai ≠ co_san`, hoặc chọn `id_item` của catalog theo bậc.
   - Việc này thuộc GĐ2, bản này **không sửa**.
9. **Kẹo kinh nghiệm S/M/L** (potion 10–12) phát EXP cố định (tới 1 tỷ): nên ngừng phát khi bật cờ. **Item 720 "Kẹo hiếm"** (thú nuôi) trùng tên với Kẹo Hiếm EXP mới, nên đổi tên.
10. **60 áo/mặt nạ cũ có chỉ số** (+tấn công/tiềm năng) đang được coi là "trang trí": `data_shop_gia_lau_dai` (GĐ2) đã ngừng bán bằng lượng. Bản 0 chỉ số theo mục 8.

## 9b. Icon

- 101 icon PMD mới (n = 401..501, id ảnh 14401..14501, `map/icon_pokemon/`), khai báo ở cuối bảng `ICON` của `tools/pokemon/sinh_icon_pokemon.py` với kiểu dùng mới `nd:<khoá>`.
- `sinh_noi_dung.py` đọc `bang_icon.csv`:
  - 28 vật phẩm type 28 mới (1034–1061) mỗi món một icon riêng: nguyên liệu, Huy chương Gym, chân dung huyền thoại cho token, hộp, token sự kiện;
  - 31 tiêu hao `can_code`, 18 ngọc hệ (Gummi), 24 icon trang trí (6 mùa × 4 loại, có số mùa) ghi vào cột `icon` của bảng staging.
- Icon cũ, `pokemon_icon_vat_pham.sql` và `IconPokemonBang.java` không đổi.

## 10. Kiểm thử

`test/KiemNoiDung.java` có 45 kiểm tra, chỉ đọc file:

- **Id**: không trùng, nằm trong dải 1001..1199, không đè `data_item` cũ; khoá chính mọi bảng không trùng; `data_potion` vẫn 162 dòng.
- **Tham chiếu**: mọi `item:` / `thuoc:` / `ngoc:` / `moi:` trong rơi, cửa hàng, chế tạo, sự kiện, boss, Gym và thưởng nhiệm vụ đều tồn tại.
- **Trang bị**:
  - đủ 11 ô mỗi bậc;
  - cấp, ngày mở, giá đúng `ThongSoLauDai`, giá = 1,5 giờ thu nhập;
  - chỉ số không giảm;
  - bậc mới ≤ 1,1 × bậc cũ +15;
  - bộ đồ giống nhau mọi bậc;
  - chỉ số engine món mới trong trần;
  - vật liệu khớp `CHI_PHI_NC/DA_NC/TI_LE_NC`.
- **Tiêu hao**: đủ nhóm; giá trong tầm thu nhập; tổng ngân sách thuốc ≤ 12%; đủ thuốc cho 5 trạng thái; 8 đá tiến hoá; Kẹo EXP trừ năng lượng.
- **Chiêu**:
  - đủ số chiêu và tên khớp client;
  - cấp mở theo tiến hoá, 10 cấp ≤ 100;
  - tối thượng mạnh hơn chiêu dạng 1;
  - **tính lại TTK** bằng thuật toán xoay chiêu (bản Java), lệch ≤ 5% so với CSV;
  - TTK trong 0,65–1,05 × mục tiêu × chỉ số dòng; các dòng cân bằng ±5%.
- **Quái**: dải năm 1 đủ loài và hệ; dải năm 2 có ≥ 3 đề xuất, ≥ 2 hệ; alpha ~20 giây; 16 Gym khớp huy hiệu nhiệm vụ, cấp tăng dần; 52 tuần boss, 5 đột kích.
- **Rơi**: 150 ppm trang bị mỗi dải; đá 1,5%; Hoàn mỹ chỉ từ alpha, Gym, boss; thưởng boss khoá.
- **Sự kiện, trang trí, cửa hàng**: mỗi tuần có vật phẩm, trong ngân sách, cả năm ≤ 70 giờ; trang trí 0 chỉ số; lượng chỉ cho trang trí.
- **Tên**: không có từ cấm kiếm hiệp (`ChuPokemon.tuCamTrong`), là tiếng Việt có dấu.
- **Web**: không lộ id hay công thức.
- **SQL**: idempotent, portable, đã gom vào `xuat_sql_day_du.sh`.

Script còn tự kiểm từ cấm bằng cùng từ điển với `tools/pokemon/quet_tu_cam.py` (`chu_pokemon.tu_dien_cam`) và dừng nếu có.

`test/KiemIconPokemon` đã nới cho dải nội dung: dump phải có 924 dòng gốc, cộng 0 hoặc đủ 61 dòng 1001+, tuỳ dump đã được sinh lại hay chưa.
