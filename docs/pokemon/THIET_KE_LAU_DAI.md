# Thiết kế dài hạn: Huyền Thoại Bảo Bối (2 năm, ổn định, không lạm phát)

> Dành cho đội phát triển. Mọi con số trong tài liệu này lấy từ **một nguồn**:
> `tools/pokemon/sinh_sql_lau_dai.py` (bảng thiết kế) → sinh `ThongSoLauDai.java`,
> `nhiem_vu_moi.csv`, `su_kien_nam.csv`, `bang_cap_do_lau_dai.csv`, `sql/pokemon_lau_dai.sql`.
> Muốn đổi số thì sửa script, chạy lại, rồi chạy mô phỏng (mục 10) và `test/KiemLauDai`.
>
> Hiện trạng và lý do thay đổi: `KIEM_TOAN_KINH_TE.md`. Thứ tự sửa code: `KE_HOACH_LAU_DAI_GD2.md`.
> Không có gì trong tài liệu này đang chạy trong game. Mới chỉ có dữ liệu và mô phỏng.

## 0. Nguyên tắc và mục tiêu đo được

| # | Nguyên tắc | Đo bằng (mô phỏng / vận hành) | Mục tiêu |
|---|---|---|---|
| N1 | **Đơn vị chung là "giờ chơi"**. Mọi thưởng, giá và chi phí tính theo "giờ thu nhập ở cấp X" chứ không theo số cố định | `xuMoiGio(L)`, `expMoiGio(L)` | Không còn quà kiểu "10 triệu xu" cho mọi cấp |
| N2 | **Mỗi nguồn tiền (faucet) có hố hút (sink) cùng tỉ lệ theo cấp** | Tổng xu ra/vào, từ tháng 3 | 80–105% (mô phỏng: **99,4%**) |
| N3 | **Tiền thực không phình** | Xu TB/người ÷ thu nhập 1 giờ ở cấp trung vị | Tăng TB ≤ 5%/tháng (mô phỏng: **2,7%**), ≤ 20 giờ (mô phỏng: **≤ 3,2 giờ**) |
| N4 | **Cày nhiều có lợi nhưng giảm dần**; treo máy 20 giờ không hơn người chơi 5–7 giờ | Giờ hiệu dụng/ngày | Bot ≤ 1,3 × nhóm cày (mô phỏng: **5,3 so với 4,36**) |
| N5 | **Cốt truyện kéo dài 12–18 tháng** với người chơi nhẹ | % xong năm 1 | Nhóm nhẹ: không ai xong trước tháng 12, trung vị **tháng 12,8**. Nhóm cày: không ai xong trước tháng 6 (sớm nhất **ngày 287**) |
| N6 | **Mở trần cấp theo mùa** để game sống 2 năm | Trần cấp theo ngày | 60 → 70 → 80 → 91 → 95 → 100 |
| N7 | **Không trả tiền để mạnh hơn** (Q10). Lượng chỉ đến từ chơi, chỉ mua đồ trang trí | Shop | Đồ có chỉ số ngừng bán bằng lượng |
| N8 | **Ngân sách sức mạnh theo mùa**: bậc đồ mới chỉ hơn bậc cũ +15 khoảng 10% | Chỉ số tấn công kỳ vọng | Mục 4.6 |
| N9 | **Cuốn hút**: luôn có mục tiêu phiên (15–30 phút), tuần và mùa | D1/D7/D30 (proxy) | ≥ 40/20/10% (mô phỏng: 68/49/29%) |

---

## 1. Tiến trình cấp

### 1.1 Bảng EXP mới (`data_cap_do_lau_dai`, `ThongSoLauDai.EXP_LEN_CAP`)

Cách dựng:
- **Giờ hiệu dụng để lên 1 cấp** `h(L)` lấy từ các mốc dưới đây, nội suy theo log nên đường cong trơn.
- **EXP lên cấp** = `h(L) × quái/giờ(L) × EXP 1 quái(L)`.
- Quái/giờ = `360 − 1,3·L`, tức 359 ở cấp 1 và 242 ở cấp 91 (thời gian hạ quái 4–6 giây cộng thời gian di chuyển).
- EXP 1 quái được nội suy theo log từ `rcvxp` của các quái đang đặt trên map: 20 (cấp 1), 300 (10), 2.000 (30), 5.940 (50), 9.000 (60), 14.000 (70), 24.000 (80), 34.000 (90), 45.000 (100).

| Cấp | Giờ/cấp | Giờ tích luỹ | EXP lên cấp (mới) | EXP lên cấp (cũ) | Xu/giờ |
|---|---|---|---|---|---|
| 1 | 0,03 | 0 | 215 | 200 | 3.590 |
| 10 | 0,05 | 0,3 | 5.210 | 36.000 | 5.552 |
| 16 | 0,10 | 0,8 | 19.700 | 248.935 | 7.458 |
| 20 | 0,25 | 1,3 | 75.200 | 574.256 | 9.018 |
| 30 | 0,90 | 6,1 | 578.000 | 2.567.962 | 13.803 |
| 40 | 2,0 | 19,3 | 2.530.000 | 15.900.142 | 21.560 |
| 50 | 3,6 | 45,8 | 6.310.000 | 98.449.487 | 33.925 |
| 60 | 6,2 | 92,3 | 15.700.000 | 609.573.273 | 52.734 |
| 70 | 11 | 173,6 | 41.400.000 | 3,77·10⁹ | 81.776 |
| 80 | 19 | 316 | 117.000.000 | 4,35·10¹¹ | 126.976 |
| 90 | 32 | 559 | 264.000.000 | 2,51·10¹³ | 196.101 |
| 91 → 100 | 33 → 46 | 591 → 938 | 281M → 548M | (không có) | 205k → 302k |

- **Tổng 1 → 91: 591 giờ hiệu dụng**. Bảng cũ cần 7,5·10¹³ EXP; bảng mới chỉ cần 3,1·10⁹.
- Cấp 91 → 100 (năm 2) thêm 347 giờ.
- Bảng mới **tăng đều và đơn điệu**, đã kiểm trong test.
- Mọi giá trị đều nhỏ hơn `Integer.MAX`, nên vẫn đọc được bằng `data_quest.exp` (`getInt`).

### 1.2 EXP khi hạ quái: bỏ 5 hệ số chồng nhau

Công thức mới, giống nhau ở **cả 7 bản sao** (`KIEM_TOAN_KINH_TE.md` mục 1.2):

```
exp = rcvxp(template) × heSoLechCap(Δ) × (1 + buff%) × nangLuong% × batKip%
  heSoLechCap: người cao hơn quái Δ = 0–3 → 100%, 4–6 → 80%, 7–9 → 50%, 10–14 → 20%, ≥ 15 → 5%
               quái cao hơn người: +5% mỗi cấp, tối đa +25% (Δ ≤ 5); quá 8 cấp → 0 (không đánh nổi)
  buff% (pet, cánh, sự kiện, đội…) CỘNG dồn, TRẦN +100%
  BỎ: ×3·TangEXP, giữ 10%/50%, chia 3 từ cấp 60, ×0,6/0,65 từ cấp 75, vé x2,5/x3, ngày x50
```

Tổ đội: sửa lỗi `nUser = 5`. Mỗi thành viên ở gần nhận `100% × (1 + 10% × (số người − 1))`, tối đa +40% khi đủ 5 người. Đây là lý do để chơi chung, không bị chia nhỏ.

### 1.3 Năng lượng huấn luyện (chống cày quá mức, chống bot)

| Thời gian đánh quái trong ngày | Tốc độ EXP và xu |
|---|---|
| 3 giờ đầu (180 phút), cộng phần tích luỹ từ hôm trước (tối đa thêm 180 phút, tổng 6 giờ) | **100%** |
| 3 giờ tiếp theo | **50%** |
| 4 giờ tiếp theo | **20%** |
| Sau 10 giờ | **0%** EXP, 0 xu, 0 đá (vẫn tốn thuốc và độ bền đồ) |

- Người chơi nhẹ (1–2 giờ) **không bao giờ chạm trần**. Nghỉ 1 ngày thì hôm sau có 6 giờ 100%, nên **không có áp lực đăng nhập mỗi ngày**.
- Người cày 6 giờ/ngày được 4,5 giờ hiệu dụng. Treo máy 20 giờ chỉ được 5,3 giờ, **thêm 18%** so với người cày 6 giờ.
- Nhiệm vụ, sự kiện và boss **không bị** giới hạn bởi năng lượng.

### 1.4 Trần cấp theo mùa (`TRAN_CAP_THEO_NGAY`)

| Mùa | Từ ngày | Trần | Nội dung mở | Bậc đồ mở |
|---|---|---|---|---|
| S1 | 0 | 60 | Kanto + Johto đầu, 36 chương | tới chuyên gia (60) |
| S2 | 60 | 70 | Johto giữa | tinh anh (70) |
| S3 | 150 | 80 | Johto cuối | huyền thoại (80) |
| S4 | 240 | **91** (trần của game cũ) | Đường Victory, Núi Silver | — |
| S5 (năm 2) | 365 | 95 | Hoenn 1 (cần map/quái mới) | thần thoại (90) |
| S6 | 545 | 100 | Hoenn 2 + Liên vùng | tối thượng (98) |

### 1.5 Nhịp độ (mô phỏng, nhóm chuẩn chơi liên tục từ khai trương)

| Tháng | Nhẹ (1–2 giờ/ngày) | Vừa (~3 giờ) | Cày (5–7 giờ) |
|---|---|---|---|
| 3 | cấp 65 | 70 (trần) | 70 (trần) |
| 6 | 74 | 77 | 80 (trần) |
| 9 | 82 | 84 | 86 |
| 12 | 88 | 91 (trần) | 91 |
| 15 | 93 | 95 (trần) | 95 |
| 24 | 100 | 100 | 100 |

Khi chạm trần, người cày chuyển sang **tinh thông, nâng cấp, Pokédex, PvP và Hầm ngục bí ẩn** (mục 7), chứ không cày cấp.

### 1.6 Sau trần: Tinh thông (thay cho chuyển sinh)

- EXP vượt trần chuyển thành **điểm Tinh thông**: 1 điểm = 50% EXP lên cấp (trần − 1).
- Mỗi mùa mở thêm tối đa 10 điểm, trần toàn bộ là 50 điểm.
- Mỗi điểm cho +0,2% tấn công **hoặc** HP, tối đa **+10%**. Mức này đã nằm trong ngân sách sức mạnh (mục 4.6).
- **Không làm chuyển sinh (reset cấp)**: client đọc cấp bằng 1 byte, tiến hoá gắn với cấp (`LoaiPokemon`), và reset cấp phá cốt truyện. Tinh thông cho cùng cảm giác "đã max vẫn còn tiến bộ" mà không tạo vòng lạm phát.

### 1.7 Bắt kịp

| Đối tượng | Điều kiện | Ưu đãi |
|---|---|---|
| Người vào muộn | Vào sau ngày 30 | EXP ×1,5 khi cấp < trung vị máy chủ − 5 |
| Người quay lại | Vắng ≥ 14 ngày | Cùng ưu đãi EXP ×1,5; thêm 3 nhiệm vụ "Quay lại" (`QL-01..03`, 0,8 giờ EXP mỗi cái) |

Cả hai ưu đãi đều không cộng thêm xu.

---

## 2. Kinh tế

### 2.1 Mô hình tiền tệ

| Đồng | Giao dịch | Nguồn duy nhất | Dùng cho | Trần |
|---|---|---|---|---|
| **Xu** | Được, qua chợ (thuế 6% + phí đăng 1%, đốt) và chuyển trực tiếp ≤ 5 giờ thu nhập/ngày | Quái (30% rơi), nhiệm vụ, 25% ngân sách sự kiện, phân rã đồ | Thuốc, sửa đồ, dịch chuyển, đồ theo bậc, **nâng cấp**, **ngọc hệ**, học chiêu, trang trí | Không cần: chi phí theo cấp hút hết |
| **Lượng khoá** | Không | Nhiệm vụ ngày/tuần/chính, đăng nhập 7 ngày (3–15), giftcode (≤ 2/tháng), thành tựu | Bùa bảo hộ, thuốc, vé EXP (1/tuần), đặt lại điểm, trang trí khoá | — |
| **Lượng** | Được (chuyển NPC, phí 5%) | **Chỉ** xếp hạng tuần (top 1: 50, top 10: 20, top 100: 8, top 1000: 3), thành tựu lớn, top sự kiện | **Chỉ đồ trang trí**, đổi tên, mở rộng túi | — |

**Bỏ hẳn:**
- lượng và lượng khoá rơi từ quái;
- quà 10 triệu xu ở cấp 35;
- giftcode cho lượng hoặc xu;
- mốc nạp;
- hoàn xu khi tiêu lượng;
- shop lượng bán đồ có chỉ số (`data_shop_gia_lau_dai`: 75 dòng đề xuất).

### 2.2 Bảng nguồn và hố hút (mô phỏng 730 ngày, toàn máy chủ)

| Nguồn xu | % | Hố hút xu | % |
|---|---|---|---|
| Quái (`xu_moi_quai(L) = 10·1,05^L`, rơi 30%) | 89,2 | Nâng cấp +1..+15 | 65,2 |
| Nhiệm vụ (chính, phụ, ngày, tuần, Pokédex) | 9,4 | Thuốc + sửa đồ (20% thu nhập mỗi giờ thật) | 23,3 |
| Sự kiện (25% ngân sách) | 0,9 | Mua đồ theo bậc (5 món × 1,5 giờ thu nhập) | 3,6 |
| Phân rã đồ | 0,5 | Ngọc hệ | 3,6 |
| | | Dịch chuyển (3 lần × 3% thu nhập 1 giờ) | 2,8 |
| | | Học chiêu (3 giờ thu nhập ở cấp 25/30/45/60/75/90) | 0,9 |
| | | Thuế chợ, mua đá trên chợ | 0,6 |

Tỉ lệ ra/vào theo đồng:
- Xu: **99,4%**.
- Lượng khoá: 95,7%.
- Lượng: 65%. Lượng tích luỹ chậm vì nguồn chỉ khoảng 2–12 mỗi tuần và chỉ mua được trang trí.

### 2.3 Tiền rơi theo lệch cấp, chống bot

- Xu, đá và đồ rơi theo cùng hệ số lệch cấp với EXP (mục 1.2). Đánh quái thấp hơn 15 cấp thì gần như không được gì.
- Năng lượng huấn luyện (mục 1.3) áp lên **cả xu**. Thuốc và sửa đồ tính theo **giờ thật**, nên treo máy sau giờ thứ 10 bị lỗ.
- Phát hiện bot:
  - Tín hiệu: log `nang_luong = 0` liên tục > 3 ngày, hoặc nhặt đồ đều hơn 20 giờ/ngày.
  - Kết hợp với cấm theo máy (`tob_cam_may`, đã có).
  - Mô phỏng giả định khoá 1% bot/ngày. Tài sản bị khoá được tính là hố hút.
- Chuyển xu trực tiếp có trần 5 giờ thu nhập/ngày. Muốn chuyển nhiều hơn phải qua chợ (thuế 7%), nên việc bán vàng cho người khác luôn bị đánh thuế.

### 2.4 Thuế và phí (`data_kinh_te_lau_dai`)

| Khoản | Cũ | Mới |
|---|---|---|
| Thuế chợ ký gửi | 10% (hoặc thuế đội) | 6% đốt + 1% phí đăng (không hoàn) |
| Chuyển lượng | 0% | 5% |
| Dịch chuyển | 500–30.000 xu | 3% thu nhập 1 giờ ở cấp người dùng |
| Sửa đồ | Σ price/10 | giữ công thức; `price` theo bậc (mục 3.1) |
| Hồi sinh | 1k → 4,5M theo cấp | 5% thu nhập 1 giờ |
| Nâng cấp | 5.000 xu cố định | `CHI_PHI_NC[bậc][k]` (mục 3.2) |
| Xổ số | thuế 10/15% | giữ |

### 2.5 Mục tiêu cung tiền và cách theo dõi (vận hành)

**Chỉ số giá** = `xuMoiGio(cấp trung vị người hoạt động 7 ngày)`.

**Cung tiền thực** = Σ xu (túi + kho) của người hoạt động ÷ số người ÷ chỉ số giá. Đơn vị là "giờ thu nhập".

| Chỉ số | Ngưỡng xanh | Vàng (xem lại) | Đỏ (hành động) |
|---|---|---|---|
| Cung tiền thực | ≤ 10 giờ | 10–20 | > 20 |
| Tăng cung tiền thực/tháng | ≤ 5% | 5–10% | > 10% |
| Ra/vào xu (30 ngày) | 90–105% | 80–90% | < 80% |
| Giá đá nâng cấp trên chợ ÷ chỉ số giá | 0,3–1 giờ | | > 2 giờ |
| % xu nằm ở top 1% người chơi | ≤ 25% | | > 40% |

Truy vấn (chạy hằng đêm, ghi vào bảng mới `tob_kinh_te_ngay`, giai đoạn 2):

```sql
-- người hoạt động 7 ngày; tob_char.gold = xu; kho_xu ở cột mới (sql/them_cot_kho_xu_luong.sql)
SELECT COUNT(*) n, SUM(gold + kho_xu) xu, SUM(luong + kho_luong) luong, SUM(luonglock + kho_luong_khoa) lk
FROM tob_char WHERE lastLog >= NOW() - INTERVAL 7 DAY;
-- nguồn/hố hút: tob_log_all_item / log "nhanxu"/"truxu" (Char.addXu/subXu đã ghi lý do) gom theo lý do
-- giá đá: tob_log_buy_store, tob_market (giá chào bán trung vị)
```

Hành động khi đỏ: tăng giá dịch chuyển hoặc sửa đồ (`cau_hinh`, không cần build); mở hố hút có thời hạn (shop trang trí bằng xu, sự kiện đổi xu lấy đồ trang trí); giảm `roi.xu.phan_tram`.

---

## 3. Trang bị

### 3.1 Bậc theo dải cấp (tên lấy từ bộ đồ Pokémon đã có trong game)

| Bậc | Tên | Cấp | Mở ngày | Giá 1 món (xu) = 1,5 giờ thu nhập | Ô |
|---|---|---|---|---|---|
| 0 | sơ cấp | 1 | 0 | 5.385 | 11 ô: Khăn quàng (áo), Đai (quần), Băng đô (nón), vũ khí theo dòng (Than lửa / Hạt giống thần / Khăn lụa / Nước thần / Nam châm), Huy hiệu ×2, Vòng cổ, Lông vũ tốc độ (giày), Găng đấm, Phiến đá hệ (ngọc bội), Ruy băng (phi phong) |
| 1 | đồng | 10 | 0 | 8.328 | |
| 2 | bạc | 20 | 0 | 13.527 | |
| 3 | vàng | 30 | 0 | 20.704 | |
| 4 | bạch kim | 40 | 0 | 32.340 | |
| 5 | kim cương | 50 | 0 | 50.888 | |
| 6 | chuyên gia | 60 | 0 | 79.101 | |
| 7 | tinh anh | 70 | 60 | 122.664 | |
| 8 | huyền thoại | 80 | 150 | 190.464 | |
| 9 | thần thoại | 90 | 365 | 294.152 | (năm 2, cần item mới) |
| 10 | tối thượng | 98 | 545 | 416.954 | (năm 2) |

- Mỗi bậc: mua 5 món ở NPC (Bronzong/Machamp); 6 món còn lại lấy từ thưởng nhiệm vụ chính/phụ (cột `vat_pham`) và đồ rơi.
- **Độ hiếm** (giữ `colorName` cũ): Trắng, Xanh (+10%), Đỏ (+20%), Hoàn mỹ (+30%), Vàng (sự kiện, chỉ trang trí). Đồ Hoàn mỹ chỉ có từ boss và Gym.
- Đổi bậc: đồ mới giữ **plus − 2**. Đồ cũ **phân rã** ra 2 đá nâng cấp và 10% giá xu, nên đồ trùng có chỗ đi.

### 3.2 Nâng cấp +1..+15 (`data_nang_cap_lau_dai`)

| +k | Tỉ lệ | Pity mỗi lần trượt | Kỳ vọng số lần | Xu/lần (× giá món) | Đá | Bùa bảo hộ (lượng khoá) | Trượt | Chỉ số món |
|---|---|---|---|---|---|---|---|---|
| 1–2 | 100% | — | 1,0 | 0,040–0,052 | 1 | — | — | +3%/cấp |
| 3 | 95% | +32% | 1,05 | 0,068 | 1 | — | giữ | |
| 4 | 90% | +30% | 1,10 | 0,088 | 1 | — | giữ | |
| 5 | 85% | +29% | 1,15 | 0,114 | 1 | — | giữ | |
| 6 | 75% | +25% | 1,25 | 0,149 | 2 | — | giữ | |
| 7 | 65% | +22% | 1,40 | 0,193 | 2 | 10 | **−1** (trừ khi có bùa) | |
| 8 | 55% | +19% | 1,58 | 0,251 | 2 | 10 | −1 | |
| 9 | 45% | +15% | 1,83 | 0,326 | 2 | 10 | −1 | |
| 10 | 38% | +13% | 2,06 | 0,424 | 2 | 10 | −1 | +30% |
| 11 | 30% | +10% | 2,44 | 0,551 | 4 | 25 | −1 | +5%/cấp |
| 12 | 24% | +8% | 2,85 | 0,717 | 4 | 25 | −1 | |
| 13 | 18% | +6% | 3,47 | 0,932 | 4 | 25 | −1 | |
| 14 | 13% | +5% | 4,14 | 1,212 | 4 | 25 | −1 | |
| 15 | 10% | +4% | 4,82 | 1,575 | 4 | 25 | −1 | **+55%** |

- **Pity**: mỗi lần trượt liên tiếp ở cùng một ô cộng `ceil(tỉ lệ gốc / 3)` điểm %. Ví dụ +15 sau 3 lần trượt là 22%. Bộ đếm về 0 khi thành công hoặc khi đổi bậc. Không có trường hợp "trượt 30 lần liền".
- **Không bao giờ vỡ đồ.**
- Kỳ vọng xu để đưa 1 món từ +0 lên +15 ≈ **22 × giá món** (chưa tính lần bị −1). Đủ 11 ô ≈ 360 giờ thu nhập, là hố hút dài hạn chính (65% tổng sink).
- **Sửa lỗi** cần làm: `Map.random()` reseed ⇒ dùng một `Random`/`ThreadLocalRandom` dùng chung; trừ tiền **sau** khi kiểm tra.

### 3.3 Ngọc hệ (khảm): hố hút thứ hai

- Mỗi ô có 1 ngọc hệ, cấp 1..10. Mỗi cấp +1% chỉ số món, tối đa +10%.
- Bước đầu giá 1 giờ thu nhập, mỗi cấp ×1,45. Lên cấp 10 trên đủ 11 ô ≈ 11 × 49 giờ.
- Dùng lại hệ khảm/đục lỗ đã có (`MapEpNgoc.doFinishKhamItem`, ngọc 12–59); đổi giá theo bảng.
- Mô phỏng cho thấy **thiếu hố hút thứ hai này** (kịch bản B, nâng cấp 5.000 xu như cũ) thì cung tiền thực tăng **12%/tháng**.

### 3.4 Bộ trang bị (set) và đồ theo hệ

| Số món cùng bậc | Thưởng |
|---|---|
| 4 | +5% HP |
| 7 | +5% tấn công |
| 11 | +3% chí mạng |

Thưởng bộ **giống nhau mọi bậc** nên không gây leo thang.

**Đồ theo hệ** (ô vũ khí = held item Pokémon):

| Đồ | Hệ |
|---|---|
| Than lửa | Lửa |
| Hạt giống thần | Cỏ |
| Nước thần | Nước |
| Nam châm | Điện |
| Khăn lụa | Thường |

- Đồ cùng hệ chiêu cho +10% sát thương chiêu hệ đó ở bậc 0, cộng thêm 1%/bậc, **tối đa +20%**.
- **Phiến đá hệ** (ô ngọc bội) chọn 1 trong 18 hệ: +5% sát thương hệ đó **hoặc** −5% sát thương nhận từ hệ đó.

### 3.5 Phân rã và tái chế

| Vật phẩm | Phân rã ra |
|---|---|
| Đồ trùng hoặc đồ bậc cũ | 2 đá + 10% giá |
| Đồ Hoàn mỹ | thêm 1 "Mảnh tinh hoa" (dùng nâng Hoàn mỹ bậc mới) |
| Ngọc hệ thừa | đá theo cấp ngọc |

Bán đồ cho NPC chỉ được **5%** giá (cũ là 20% + 1.000/plus), để phân rã luôn đáng hơn bán.

### 3.6 Ngân sách sức mạnh theo mùa (chống leo thang)

Chỉ số tấn công kỳ vọng ở cấp L:

```
TC(L) = (30 + 12·L) × (1 + 0,6 × (1 + nâng cấp%)) × (1 + ngọc% + bộ% + tinh thông%)
```

| Đồ ở cấp L | Hệ số so với không đồ |
|---|---|
| Trắng +0 | 1,60 |
| +6 | 1,71 |
| +10 | 1,78 |
| +15 | 1,93 |
| +15, ngọc 10, đủ bộ, tinh thông 50 | ×1,25 trên mức +15 ⇒ **trần ≈ 2,4** |

Quy tắc mùa:
- Bậc mới (+0) phải cho **≤ 1,10 ×** bậc cũ (+15) ở cùng cấp.
- Phần tăng của bậc mới đến từ cấp cao hơn (+12 tấn công/cấp).
- Người chơi đã đầu tư không bị lỗi thời ngay. Đồ mới chỉ vượt hẳn đồ cũ khi lên khoảng +6.

---

## 4. Chỉ số, chiến đấu, cân bằng

### 4.1 Công thức sát thương (sửa so với hiện tại)

```
cơ bản = max(5%·atk, atk − def)            (bỏ sàn cố định 5–14, sàn theo % để quái thủ cao vẫn trầy)
dam    = cơ bản × %chiêu × hệ × STAB × (1 + đồ hệ%) × chí mạng × (0,95..1,05)
hệ     = HePokemon.heSoTrongGame (×2 / ×1,5 / ×1 / ×0,7 / ×0,5 / ×0,3)   ÁP SAU khi trừ thủ (như quái)
STAB   = ×1,2 khi hệ chiêu trùng hệ 1 hoặc hệ 2
chí mạng = ×1,5 (cũ ×2); bạo kích ×4 BỎ (chỉ còn ở PvE boss như trạng thái "phá giáp")
PvP    = dam × 0,30, TRẦN = 8% HP tối đa mục tiêu mỗi đòn (thay trần 50k/75k cố định)
```

Lý do đưa hệ ra sau trừ thủ: hiện ×1,5 thành ×2,56 thực tế (`CharChiSo.java:753`), và người đánh với quái đang bất đối xứng.

### 4.2 Ngân sách chỉ số và quái đề xuất (`data_monster_lau_dai`, 68 quái đã đặt)

| Cấp | Tấn công người (đồ +6) | Quái HP | Quái tấn công | Quái thủ | Cũ: HP / thủ |
|---|---|---|---|---|---|
| 10 | 256 | 1.610 | 408 | 90 | 3.000 / 30 |
| 30 | 666 | 4.190 | 707 | 233 | 30.150 / 340 |
| 50 | 1.076 | 8.460 | 1.190 | 377 | 112.590 / 660 |
| 60 | 1.281 | 10.080 | 1.364 | 448 | 193.830 / 900 |
| 80 | 1.691 | 15.960 | 1.712 | 592 | 900.530 / 25.000 |
| 90 | 1.896 | 17.890 | 1.886 | 664 | 2.000.530 / 75.000 |

### 4.3 TTK mục tiêu

| Trường hợp | Cấp 1–30 | 31–60 | 61–100 |
|---|---|---|---|
| Người hạ quái thường | 4 giây | 5 giây | 6 giây |
| Quái thường hạ người (1 con) | 15 đòn ≈ 60 giây | 12 đòn ≈ 48 giây | 12 đòn |
| Quái tinh anh (alpha) | ×4 HP, ×1,3 tấn công ⇒ ~20 giây | | |
| Boss Gym | 90–120 giây đánh một mình đúng cấp, đồ +6 | | |
| Boss thế giới | 8–12 phút với 20 người | | |
| PvP cùng cấp, cùng đồ | 10–14 giây (không chí mạng) | ≥ 6 giây với đòn khắc hệ ×2 + chí mạng | |

### 4.4 5 dòng khởi đầu + Eevee: vai trò rõ ràng

| Dòng | Vai trò | HP | Tấn công | Thủ | Bản sắc |
|---|---|---|---|---|---|
| Charmander (c0) | Sát thương liên tục cận chiến | 100 | 105 | 90 | Bỏng (DoT), chí mạng cao |
| Bulbasaur (c1) | Chịu đòn + khống chế | 115 | 90 | 115 | Ngủ / độc, hồi máu nhẹ; **sửa lệch bảng chiêu 6/7/8** |
| Eevee (c2) → 8 nhánh | Linh hoạt theo nhánh | 90 | 100 | 95 | Vaporeon (hồi đội), Jolteon (tốc độ), Flareon (bùng nổ), Espeon / Umbreon (phép / chịu), Leafeon / Glaceon (khống chế), Sylveon (hỗ trợ) |
| Squirtle (c3) | Chịu đòn chính | 120 | 85 | 125 | Khiên, khiêu khích, giảm sát thương đội 10% |
| Pichu (c4) | Đánh xa, khống chế | 85 | 110 | 85 | Tê liệt, sát thương lan |

- Cột HP/Tấn công/Thủ là chỉ số tương đối, chuẩn = 100.
- **Ràng buộc cân bằng**: tích HP × Tấn công × Thủ (chuẩn hoá) của mọi dòng nằm trong **±5%**. Kiểm bằng test khi áp dụng.
- **Chiêu 3 nhiều đòn** (2.360%) giảm còn **5 đòn × 220% = 1.100%**, hồi chiêu 4 giây, để xoay chiêu có nghĩa.

### 4.5 Mở khoá và nâng chiêu theo giai đoạn tiến hoá

| Giai đoạn | Cấp | Chiêu mở | Điểm nâng chiêu | Chi phí sách chiêu |
|---|---|---|---|---|
| Dạng 1 | 1–15 | Chiêu 0–2 + bị động 1 | 1 điểm/cấp (giữ) | — |
| Dạng 2 | 16 (Pichu 10) | Chiêu 3 + 4, nhánh vai trò A/B | | 3 giờ thu nhập (cấp 25, 30) |
| Dạng 3 | 32/36 (Pichu 30, Eevee 25) | Chiêu 6–8 + tối thượng | | 3 giờ thu nhập (cấp 45, 60, 75, 90) |
| Tinh thông | trần | Cấp 10 chiêu (đang chỉ có qua item) | 1 điểm tinh thông = 1 cấp chiêu thứ 10 | — |

### 4.6 Quái, boss và chiến đấu (bổ sung theo yêu cầu 2)

**Họ quái theo vùng sinh thái (biome).** Mỗi tuyến có 3–4 loài thuộc ít nhất 2 hệ, dải cấp 3–4.

| Vùng | Ví dụ |
|---|---|
| Đồng cỏ | Thường / Cỏ / Bọ |
| Rừng | Bọ / Cỏ / Độc |
| Hang | Đá / Đất / Ma |
| Bờ biển | Nước / Bay |
| Núi lửa | Lửa / Đá |
| Hồ | Nước / Băng |
| Tháp | Ma / Siêu linh |

Bảng `thiet_ke_map.csv` (đội map) đã có cột `loai`. Đề xuất thêm cột `ho_quai` (giai đoạn 3) và dải cấp lấy từ cột Cấp trong mục 5.2.

**Quái tinh anh (alpha):**
- 1/50 lần hồi sinh, to ×1,3, có tên ("Alpha Rattata").
- ×4 HP, ×1,3 tấn công.
- Rơi gấp 3, chắc chắn rơi 1 đá, 2% đồ Hoàn mỹ.
- Thông báo trong bản đồ.

**Boss: thiết kế lại theo giai đoạn.**

| Cơ chế | Chi tiết |
|---|---|
| Pha | 100–70% đánh thường + 1 chiêu lớn; 70–40% thêm chiêu vùng, gọi 2–4 đệ (adds); < 40% "nộ" (+30% tốc độ đánh) |
| Báo trước chiêu lớn | Server gửi **thời gian niệm** 1,5–3 giây + hình vùng (tròn / nón / chữ nhật / vành) ⇒ client vẽ vùng cảnh báo; đứng ngoài vùng thì không trúng |
| Nộ theo thời gian | Boss Gym 5 phút, boss thế giới 15 phút ⇒ sau đó sát thương ×3 (chống kéo dài bằng người đông) |
| Gọi đệ | 70% và 40% HP: mỗi lần gọi số đệ theo số người (tối đa 6), đệ đánh vào người gây thù ghét cao nhất |
| Boss thế giới hằng tuần | Huyền thoại theo lịch sự kiện (Zapdos, Entei, Darkrai, Rayquaza, Groudon/Kyogre…). **Thưởng theo % đóng góp sát thương/hồi máu/chịu đòn**, không theo đòn cuối. Bậc: ≥ 0,5% đóng góp ⇒ hộp tham gia; top 10 ⇒ hộp vàng; mọi hộp là vật phẩm khoá |
| Boss Gym (16) | Mỗi huy hiệu một boss PvE (chương có `huy_hieu`), sức mạnh theo cấp chương, đánh một mình hoặc đội 2. Đánh lại hằng tuần ⇒ "Huy chương Gym" (đổi đồ trang trí) |

Dùng lại các lớp `Boss*.java` đã có. Thêm `GiaiDoanBoss` (lớp thuần, có test) để mô tả pha, ngưỡng HP, chiêu, thời gian niệm, nộ và đệ.

**Trạng thái (status), server tính, client chỉ vẽ:**

| Trạng thái | Hiệu ứng | Thời gian | Kháng |
|---|---|---|---|
| Bỏng | 3% HP tối đa mỗi 2 giây, −10% tấn công | 6 giây | Hệ Lửa miễn |
| Độc | 2% → 4% → 6% HP mỗi 2 giây (cộng dồn 3 lần) | 8 giây | Hệ Độc/Thép miễn |
| Tê liệt | −30% tốc độ, 15% mất lượt | 5 giây | Hệ Điện miễn |
| Ngủ | Không hành động, tỉnh khi trúng đòn | 3 giây | Sau đó miễn ngủ 10 giây |
| Đóng băng | Không hành động | 2 giây | Hệ Băng miễn; sau đó miễn 10 giây |

- Trong PvP, mọi thời lượng ×0,5.
- Boss chỉ nhận 30% thời lượng, không ngủ và không đóng băng.

**Gói tin cho client** (để nhóm hiệu ứng J2ME/Android/PC móc vào). Số lệnh **phải grep lại trước khi chốt** (CODING_CONVENTION §1) và thống nhất với nhóm hiệu ứng/âm thanh đang làm. Hiện -90..-94 đã dùng; đề xuất -95..-99. Mọi gói đều là **gói kèm** (`Message.kemTheo`), đi ngay sau gói cũ, nên client cũ bỏ qua được.

| Số | Tên | Kèm sau | Nội dung |
|---|---|---|---|
| -95 | POKE_DON (đòn đánh) | lệnh 9 (người → quái), đòn quái → người, đòn PvP | `i16 nguon, u8 loaiNguon(0 người,1 quái), i16 dich, u8 loaiDich, i32 dam, u8 co` (bit0 chí mạng, bit1 né, bit2 khắc ≥×1,5, bit3 kém ≤×0,7, bit4 gần miễn ×0,3, bit5 STAB, bit6 trúng trạng thái), `u8 heChieu (0..17), u8 trangThai (0 không, 1 bỏng, 2 độc, 3 tê, 4 ngủ, 5 băng)` |
| -96 | POKE_CANH_BAO (niệm chiêu) | — | `i16 boss, u16 idChieu, u8 heChieu, u8 hinh (0 tròn,1 nón,2 chữ nhật,3 vành), i16 x, i16 y, i16 r_hoac_dai, i16 rong_hoac_goc, i16 huong (độ), u16 msNiem` |
| -97 | POKE_TRANG_THAI | — | `i16 actor, u8 loaiActor, u8 trangThai, u8 chong, u16 msConLai` (msConLai = 0 ⇒ gỡ) |
| -98 | POKE_BOSS | — | `i16 boss, u8 pha, u8 suKien (0 vào pha,1 nộ,2 gọi đệ,3 khiên), i32 hpConLai, u16 giayDenNo` |
| -99 | POKE_DONG_GOP | mỗi 5 giây khi đánh boss thế giới | `u8 n, n × (i16 charId, u16 phanNghin)` + `u16 phanNghinCuaToi` |

Client hiển thị:
- Số sát thương màu theo cờ: vàng = chí mạng, xanh = khắc, xám = kém.
- Biểu tượng trạng thái trên đầu.
- Vùng đỏ đếm ngược theo `msNiem`.
- Thanh pha boss.
- Bảng đóng góp.

---

## 5. Nhiệm vụ (`nhiem_vu_moi.csv`: 721 dòng)

### 5.1 Cấu trúc

| Loại | Số dòng | Ghi chú |
|---|---|---|
| Hướng dẫn (`HD.01..10`) | 10 | 30 phút đầu, xong ở cấp ~8 |
| Chính năm 1 (`C01.1..C52.4`) | 52 chương × 4 bước = 208 | Bước: lời nhờ / đưa thư → dọn đường (hạ quái) → thu thập → thử thách / Gym |
| Chính năm 2 (`C53..C104`) | 52 chương | Dàn ý, chưa sinh SQL |
| Phụ theo thị trấn (`P-xx.1..5`) | 20 thị trấn × 5 = 100 | Chuỗi, bước 5 cho danh hiệu thị trấn + 1 món đồ |
| Tuyến (`R-xx.1..3`) | 10 × 3 = 30 | |
| Ngày (`N-b.xx`) | 20 mẫu × 4 bậc cấp = 80 | Làm **3/ngày** |
| Tuần (`T-b.xx`) | 12 × 3 = 36 | Làm **2/tuần**, reset thứ Hai 05:00 |
| Pokédex (`D-xxx.1..3`) | 154 | Gặp / hạ 50 / thấy shiny |
| Đội | 8 | |
| Sư phạm | 5 | |
| Hầm ngục bí ẩn | 5 | |
| Quay lại | 3 | |
| Thành tựu / danh hiệu (`A-01..30`) | 30 | Không cộng chỉ số |

Ngân sách thưởng mỗi dòng (test kiểm cả 721 dòng). Đơn vị: giờ EXP / giờ xu ở cấp tối thiểu của nhiệm vụ.

| Loại | Giờ EXP | Giờ xu | Lượng khoá | Lượng |
|---|---|---|---|---|
| Chính | 0,30 (bước Gym 0,45) | 0,15 / 0,25 | 3 / 10 | 0 |
| Phụ | 0,40 | 0,25 | 3 | 0 |
| Tuyến | 0,50 | 0,30 | 4 | 0 |
| Ngày | 0,10 | 0,05 | 4 | 0 |
| Tuần | 1,0 | 0,5 | 20 | 0 |
| Pokédex | 0,25 (× 0,5 / 1 / 2 theo bậc) | | 5–15 | 0 |
| Đội | 0,5 | | 10 | |
| Sư phạm | 0,3 | | 10 | |
| Hầm ngục | 0,2 | | 5 | |
| Quay lại | 0,8 | | 15 | |
| Thành tựu | — | — | 20 | 10 (ngưỡng ≥ 20) |

- Dưới cấp 16, thưởng hướng dẫn, chính và phụ ×3 để **tiến hoá đầu trong ~1 giờ** (mô phỏng: trung vị 1,1 giờ).
- Thời gian chơi để làm: chương ~0,6 giờ (dưới cấp 16: 0,3 giờ); phụ ~0,25 giờ.

**Khoá theo tuần:**
- Mỗi nhân vật nhận **2 vé chương/tuần** (tích tối đa 4, bắt đầu với 4 vé).
- Mỗi chương còn cần **cấp tối thiểu** và **ngày mở** của máy chủ.
- 52 chương cần ≥ 24 tuần. Thực tế bị trần cấp chặn: chương 52 cần cấp 90, mà trần 91 mở ở ngày 240.

### 5.2 Danh sách chương năm 1 (13 cung, 16 huy hiệu Gym)

| # | Cung | Chương | Địa danh | Cấp | Mục tiêu hạ | Huy hiệu |
|---|---|---|---|---|---|---|
| 1–4 | K1 Khởi hành từ Pallet | Buổi sáng ở Pallet; Đồng cỏ phía bắc; Lối mòn Viridian; Bóng cây Rừng Viridian | Pallet, Viridian, Rừng Viridian | 1–8 | Caterpie, Ekans, Litwick, Bellsprout, Venonat, Vulpix | |
| 5–8 | K2 Đá xám Pewter | Thị trấn đá xám; **Xưởng chế tạo Pewter**; Đường lên Núi Mặt Trăng; Mảnh sao băng | Pewter, Núi Mặt Trăng | 10–16 | Rattata, Quagsire, Venipede, Tepig, Paras, Skorupi, Combusken | Đá (ch.6) |
| 9–12 | K3 Dòng nước Cerulean | Cây cầu Cerulean; **Lá thư của Torkoal**; Bến cảng Vermilion; **Tiếng sấm bên bờ biển** | Cerulean, Vermilion | 18–23 | Gloom, Gastly, Combusken, Ariados, Cubone, Croagunk | Thác (10), Sấm (12) |
| 13–16 | K4 Chuyến tàu S.S. Anne | Tấm vé lên tàu; Hành lang tàu; Kho hàng dưới boong; Cơn bão ngoài khơi | Vermilion, Tàu S.S. Anne | 25–29 | Ariados, Croconaw, Croagunk, Dodrio, Kricketune | |
| 17–20 | K5 Sương mù Lavender | Tháp Pokémon; Tiếng khóc trong sương; Khu thương mại Celadon; **Vườn hoa Celadon** | Lavender, Celadon | 30–35 | Arbok, Kricketune, Tauros, Duskull, Whirlipede | Cầu Vồng (20) |
| 21–24 | K6 Saffron và Vùng Safari | Thành phố Saffron; **Tòa nhà Silph**; Cổng Vùng Safari; **Bờ biển Fuchsia** | Saffron, Vùng Safari, Fuchsia | 36–41 | Duskull, Whirlipede, Palpitoad, Whirlipede Shiny | Đầm Lầy (22), Linh Hồn (24) |
| 25–28 | K7 Đảo lửa Cinnabar | Chuyến đò ra đảo; Phòng thí nghiệm bỏ hoang; **Núi lửa thức giấc**; **Trở về Viridian** | Cinnabar, Viridian | 42–48 | Palpitoad, Haunter, Marowak, Seviper, Breloom | Núi Lửa (27), Đất (28) |
| 29–32 | J1 Gió mới New Bark | Thị trấn New Bark; Hoa anh đào Cherrygrove; **Tháp Sprout ở Violet**; Cánh đồng phía tây | New Bark, Cherrygrove, Violet | 49–54 | Seviper, Breloom, Magcargo, Golem, Crabrawler | Gió (31) |
| 33–36 | J2 Rừng Ilex và Azalea | **Giếng Slowpoke**; Lạc trong Rừng Ilex; Đền thờ trong rừng; **Phố lớn Goldenrod** | Azalea, Rừng Ilex, Goldenrod | 55–61 | Breloom, Arcanine, Crabrawler, Rhydon | Tổ Ong (33), Đồng Bằng (36) |
| 37–40 | J3 Tháp chuông Ecruteak | **Tháp Chuông**; Tháp Cháy; **Ngọn hải đăng Olivine**; **Thuốc từ Cianwood** | Ecruteak, Olivine, Cianwood | 62–68 | Arcanine, Whiscash, Pinsir, Kingler, Trevenant | Sương (37), Khoáng (39), Bão (40) |
| 41–44 | J4 Hồ Phẫn Nộ | Tin đồn ở Mahogany; Mặt hồ đỏ rực; Hang Tối; **Sóng radio lạ** | Mahogany, Hồ Phẫn Nộ, Hang Tối | 69–75 | Kingler, Gastrodon, Whiscash, Vileplume, Sharpedo | Băng (44) |
| 45–48 | J5 Rồng Blackthorn | Ngôi làng của rồng; Hang Salamence; Tổ Zapdos; **Lời thề ở đền rồng** | Blackthorn, Hang Salamence, Tổ Zapdos | 76–83 | Trevenant, Galvantula, Vileplume, Liepard Shiny, Luxray, Entei | Rồng (48) |
| 49–52 | J6 Đường Victory | Cổng Đường Victory; Cao nguyên Indigo; Đỉnh Núi Silver; Huyền thoại thức tỉnh | Đường Victory, Cao nguyên Indigo, Núi Silver | 84–90 | Liepard Shiny, Luxray, Entei | (danh hiệu Nhà vô địch) |

Chi tiết từng bước (NPC giao, id quái, số lượng, vật phẩm, thưởng) nằm trong CSV.

### 5.3 Dàn ý năm 2 (cấp 91–100, mở ngày 365 / 545)

H1 Đất liền Hoenn → H2 Biển nóng Slateport → H3 Núi lửa Chimney → H4 Bầu trời Fortree →
H5 Biển sâu Lilycove → H6 Thức tỉnh Kyogre và Groudon → H7 Tháp trời (Rayquaza) → H8 Liên
minh Hoenn → H9 Vùng chiến đấu → H10 Đảo Navel và Faraway → H11 Kết nối ba vùng → H12 Mùa giải
huyền thoại → H13 Khúc vĩ thanh (mở đường Sinnoh).

Tổng cộng 52 chương. Năm 2 cần map và quái Hoenn mới, cùng bảng cấp tới 100 (`LevelDetail`, giai đoạn 3).

### 5.4 Khả năng của engine hiện tại và chỗ còn thiếu

Biểu diễn được bằng `data_quest` type 0–4: **410/721 dòng** (cột `dong_co_tuong_thich = 1`), đã nằm trong bảng tạm `data_quest_moi`.

Chỗ còn thiếu (chi tiết trong GĐ2):
1. `lv` của nhiệm vụ chính chưa được kiểm tra.
2. Chưa có vé tuần.
3. Danh sách nhiệm vụ phụ ghi số lượng bằng **1 byte** (tối đa 127), nên phải lọc theo cấp trước khi gửi.
4. Chỉ có 10 vật phẩm nhiệm vụ (`ItemQuest` nằm trong jar). Tên "Thịt heo" / "Rắn" không hợp, nên nhiệm vụ mới chỉ dùng id 0, 2, 3, 6, 7, 8, 9.
5. Nhiệm vụ tuần, Pokédex, sư phạm, Hầm ngục và thành tựu cần bảng tiến độ mới (`tob_nhiem_vu_lau_dai`).
6. Chỉ giữ được 1 nhiệm vụ phụ cùng lúc.

---

## 6. Sự kiện: lịch 52 tuần (`su_kien_nam.csv`)

| Nhóm | Tuần (năm 1) | Cơ chế | Dùng lại | Ngân sách (giờ thu nhập/tuần) |
|---|---|---|---|---|
| Lễ Việt Nam | 1 Tết dương lịch, 4 gói bánh, 5–6 **Tết Nguyên Đán** (âm lịch: dời tuần 4–7), 8 Valentine, 10 8/3, 15 Giỗ Tổ, 17 30/4–1/5, 22 1/6, 23–24 Hè, 32 Vu Lan, 35 2/9, 37–38 Trung Thu, 41 20/10, 43–44 Halloween, 46 20/11, 47 Black Friday, 51 Giáng sinh | Đổi vật phẩm sự kiện, NPC, nấu bánh, thả đèn | `SuKien.TET_2017/TET_123/TET_DUONG_LICH/QUOC_TE_8_3/GIO_TO/HE_2017/VU_LAN/TRUNG_THU/HALLOWEEN/SINH_NHAT_2011/BLACK_FRIDAY/NOEL` | 1–3 (Tết 3, Noel 2) |
| Ngày cộng đồng (mỗi dòng 2 lần/năm) | 3, 12, 19, 25, 31, 40, 50 | 3 giờ: dòng khởi đầu xuất hiện dày, shiny ×5 | spawn + `tob_pokedex` | 1 |
| Tuần Shiny | 7, 34 | Shiny ×10, pity 2.000 lần hạ | cột shiny mới | 1 |
| Đột kích huyền thoại | 9 Zapdos, 20 Entei, 28 Darkrai, 36 Rayquaza, 48 Groudon & Kyogre | Boss thế giới 2 lần/ngày, thưởng theo đóng góp | `BossThanLan/BossDracula/BossTruRong/BossSonTinhThuyTinh` | 1,5 |
| Tuần hệ | 2 Băng, 11 Cỏ, 18 Lửa, 27 Nước, 33 Điện, 42 Siêu linh | Quái hệ X ×2, chiêu hệ X +10% | spawn | 1 |
| Lễ hội Pokédex / bùng phát | 14, 45 / 21 | Điểm nghiên cứu ×2 / 1 loài ×5 trên 1 tuyến mỗi ngày | `tob_pokedex_thuong` | 1 |
| Thế chiến vùng | 16, 30, 49 | Thứ Bảy 20h, Kanto/Johto/Hoenn giành cờ | `MapChienTruongMoba` | 1 |
| Mùa giải PvP | 13, 26, 39, 52 | Chốt bậc mùa | `MapLienDau`, `tob_char_thach_dau` | 1–2 |
| Tuần thường | các tuần còn lại | Chỉ có bảng tuần + xếp hạng | — | 0,5 |

- Tổng ngân sách năm = **66,5 giờ thu nhập** (test chặn ≤ 70). Chỉ **25%** trả bằng xu; phần còn lại là vật phẩm khoá **hết hạn khi sự kiện kết thúc** hoặc đồ trang trí. Vì vậy sự kiện không tích tiền.
- **Năm 2 xoay vòng** (cột `ten_nam2`): đổi huyền thoại (Articuno, Raikou, Suicune, Lugia…), đổi hệ của tuần hệ, đổi dòng của ngày cộng đồng, đổi linh vật ngày lễ.
- **Vận hành**: mỗi dòng có `ma_su_kien` để đặt vào `tob_su_kien` (cột `mo_luc/dong_luc`), dùng lịch hẹn của `LichSuKien`. Mã `moi:*` cần thêm `Muc` mới trong `SuKien.DANH_MUC` (GĐ2). Lưu ý: bật hoặc tắt sự kiện **cần khởi động lại** (đã có chốt an toàn). Lịch gom các thay đổi vào 05:00 thứ Hai.

---

## 7. Cuốn hút và giữ chân người chơi

### 7.1 Vòng chơi và mục tiêu 3 tầng

| Tầng | Thời lượng | Mục tiêu luôn có sẵn | Hệ thống |
|---|---|---|---|
| Phiên | 15–30 phút | 3 nhiệm vụ ngày (~5 phút mỗi cái), 1 bước chương, 1 lượt Hầm ngục, 1 lần nâng cấp, 1 loài Pokédex mới | `data_quest` type 4, NPC Blaziken |
| Tuần | 7 ngày | 2 nhiệm vụ tuần, 2 vé chương, boss Gym đánh lại, đột kích, thế chiến vùng (thứ Bảy), xếp hạng tuần (lượng) | bảng tuần mới, `SuKien` |
| Mùa | 2–6 tháng | Trần cấp mới, bậc đồ mới, mùa PvP (13 tuần), 8 huy hiệu Gym của vùng, săn huyền thoại, danh hiệu mùa | `TRAN_CAP_THEO_NGAY`, `DanhHieu` |

Vòng lõi: **đánh quái theo tuyến → nhiệm vụ / Pokédex → đồ + đá → nâng cấp → mở chương / Gym → tuyến mới**.

### 7.2 Sưu tầm (không tăng sức mạnh)

- **Pokédex nghiên cứu 3 bậc mỗi loài**: gặp → hạ 50 → thấy shiny. 154 nhiệm vụ. Mốc 10 / 25 / 50 / 75 / 100 loài cho danh hiệu (đã có `tob_pokedex_thuong`).
- **Shiny**: tỉ lệ 1/4.096 lần hạ. Pity: lần thứ 2.000 không ra thì chắc chắn ra (ngoài Tuần Shiny). Shiny chỉ đổi màu và cho danh hiệu.
- Trang phục và skin theo mùa, lấy từ sự kiện, Gym đánh lại, xếp hạng. **Không chỉ số.** Shop lượng chỉ bán loại này.
- **16 huy hiệu Gym** là mốc cốt truyện. Hiển thị trong hồ sơ, xem được qua `tob_web_info_char`.

### 7.3 Xã hội

| Cơ chế | Số liệu | Dùng lại |
|---|---|---|
| Đội (bang cũ) | Nhiệm vụ đội tuần (8 mẫu, `mainsub=2`), boss đội, quỹ đội | `tob_quest_clan`, `MapBangHoi` |
| Bạn bè | +5% EXP khi cùng đội tổ với bạn ≥ 7 ngày | `tob_friendlist` |
| Tổ đội | +10%/người, tối đa +40% | sửa `calculatorXpParty` |
| Sư phạm | Thầy (cấp ≥ 30) kèm trò thấp hơn ≥ 20 cấp. Trò +20% EXP khi chung đội. Cả hai nhận thưởng khi trò lên 20/40 hoặc tiến hoá | nhiệm vụ `SP-01..05` |
| Giao dịch | Chợ thuế 7%; chuyển xu trực tiếp ≤ 5 giờ thu nhập/ngày | `OfflineMap` |
| Sự kiện chat thế giới | Đố Pokémon (đoán loài từ bóng), 2 lần/ngày, thưởng lượng khoá | `MapChat` |

### 7.4 Cạnh tranh

- **Thang PvP mùa**: các bậc Poké Ball → Great → Ultra → Master, theo điểm Elo.
- **Ghép trận theo dải cấp 10.**
- **Reset mềm** mỗi 13 tuần, giữ 50% điểm.
- Thưởng mùa chỉ là khung và danh hiệu, cộng lượng theo bậc (Master 100, Ultra 40, Great 15).
- **Thế chiến vùng** hằng tuần giữa Kanto, Johto và Hoenn (lãnh thổ cũ).
- **Thách đấu Gym**: boss PvE mạnh dần theo cấp, có bảng xếp hạng thời gian hạ.
- **Bảng xếp hạng reset theo mùa**: cấp, Pokédex, Hầm ngục, PvP. Ẩn xu/lượng (`AN_XU_LUONG_TOP`).

### 7.5 Bất ngờ và đa dạng

| Sự kiện | Cơ chế |
|---|---|
| Huyền thoại lang thang | Mỗi ngày ngẫu nhiên 1 lần, báo trước 5 phút |
| Bùng phát quần thể | 1 tuyến × 1 loài ×5, đổi mỗi ngày |
| Thời tiết theo ngày | Mưa: Nước +10%. Nắng: Lửa +10%. Tuyết: Băng +10%. Bão cát: Đá +10%. Quay vòng theo hạt giống ngày |
| Lịch đăng nhập 7 ngày tăng dần | 3, 4, 5, 6, 8, 10, 15 lượng khoá, lặp lại. **Không mất chuỗi khi nghỉ**, chỉ dừng lại |
| Trứng bí ẩn | Nở sau 5 km đi bộ hoặc 200 quái. Ra pet trang trí hoặc 1–3 đá |
| **Hầm ngục bí ẩn** (hợp phong cách PMD) | Tầng ngẫu nhiên. Hạt giống theo ngày, mọi người cùng bản đồ. Bảng xếp hạng tầng. 1 lượt thưởng/ngày, chơi thêm không thưởng. Endgame lặp lại được |

### 7.6 Người mới và người quay lại

- **30 phút đầu**: 10 bước hướng dẫn (`HD.01..10`). Chiêu, thuốc, trang bị, Pokédex. Kết thúc ở cấp ~8.
- **Tiến hoá đầu**: trung vị 1,1 giờ (Pichu → Pikachu: 0,3 giờ).
- Người quay lại: xem mục 1.7.

### 7.7 Chống kiệt sức

- Không có việc bắt buộc 5 giờ/ngày.
- Thưởng ngày có trần (3 nhiệm vụ). Năng lượng tích được 2 ngày.
- Vé chương tích được 2 tuần.
- Lịch đăng nhập không phạt khi nghỉ.

### 7.8 Không thu tiền (Q10)

- Không có nạp, lượng không bán, không có vé EXP mua bằng tiền thật.
- Mọi đồ trang trí đều lấy từ việc chơi.
- Bỏ mốc nạp, bỏ VIP theo nạp (`up_px_xu_vip`).

### 7.9 Giữ chân trong mô phỏng (proxy)

Mô hình nguy cơ bỏ chơi:
- Theo ngày tuổi của nhân vật: ngày 1 là 38%, hoặc 55% nếu chưa tiến hoá trong 1,5 giờ. Ngày 2–7 là 7%/ngày. Ngày 8–30 là 2,2% + gốc.
- Theo nhóm (gốc): nhẹ 1%, vừa 0,6%, cày 0,4%/ngày.
- ×1,8 nếu 10 ngày không có mốc tiến bộ nào (cấp, chương, nâng cấp ≥ +7, đổi bậc, thành tựu, tinh thông).
- ×0,85 trong tuần sự kiện; ×0,85 khi có đội từ cấp 20.
- Người đã bỏ chơi quay lại với xác suất 0,3%/ngày.

Kết quả: **D1 68%, D7 49%, D30 29%**. Đây là con số **tương đối** để so sánh phương án, không phải dự báo.

---

## 8. Ánh xạ hệ thống cũ được tái dùng

| Cần | Dùng lại |
|---|---|
| Lịch sự kiện | `tob_su_kien` + `LichSuKien` (mở/đóng theo giờ) |
| Nhiệm vụ chính/phụ/đội | `data_quest`, `NewCharQuest`, `tob_char_quest` |
| Pokédex | `tob_pokedex`, `tob_pokedex_thuong`, NPC -90 |
| PvP | `MapLienDau`, `ArenaData`, `tob_char_thach_dau` |
| Đội | `tob_clan`, `tob_quest_clan`, `MapBangHoi` |
| Boss | `Boss*.java`, `MapNapTaiNguyen` |
| Tham số không cần build | `cau_hinh` / `CauHinh.java` |
| Danh hiệu | `DanhHieu`, `tob_danh_hieu` |

---

## 9. File

| File | Vai trò |
|---|---|
| `tools/pokemon/sinh_sql_lau_dai.py` + `lau_dai_cot_truyen.py` | **Nguồn duy nhất** của bảng thiết kế (chỉ thư viện chuẩn, tất định) |
| `docs/pokemon/nhiem_vu_moi.csv` | 721 nhiệm vụ |
| `docs/pokemon/su_kien_nam.csv` | 52 tuần |
| `docs/pokemon/bang_cap_do_lau_dai.csv` | EXP, giờ, xu, quái đề xuất theo cấp 1..100 |
| `src/com/htbb/pokemon/ThongSoLauDai.java` | Hằng số. **Chưa nối vào game** |
| `sql/pokemon_lau_dai.sql` | 8 bảng tạm. **Không** nằm trong `xuat_sql_day_du.sh`. Đã chạy thử trên NovaDB |
| `tools/pokemon/MoPhongKinhTe.java` | Mô phỏng (mục 10) |
| `docs/pokemon/mo_phong_kinh_te.csv`, `mo_phong_tom_tat.txt`, `mo_phong_*.png` | Kết quả mô phỏng |
| `test/KiemLauDai.java` | 65 kiểm tra |

---

## 10. Mô phỏng (`tools/pokemon/MoPhongKinhTe.java`)

Chạy:

```bash
javac -encoding UTF-8 -cp build/src -d build/tools tools/pokemon/MoPhongKinhTe.java
java -cp build/tools:build/src MoPhongKinhTe [số_ngày=730] [%dân_số=100]
```

- Tất định: RNG SplitMix64, hạt giống 20261017. Chạy khoảng 4–15 giây.
- **Hai dân số**:
  - **Dân chuẩn**: 300 người/nhóm từ khai trương, không bỏ chơi. Dùng để đo tiến trình.
  - **Dân số máy chủ**: 2.000 người khai trương (50% nhẹ, 30% vừa, 15% cày, 5% bot), thêm `40·e^(−ngày/150) + 12` người/ngày (×1,3 trong tuần lễ), có bỏ chơi và quay lại. Tổng khoảng 21.000 người. Dùng để đo kinh tế và giữ chân.
- Mỗi ngày mỗi người: đăng nhập (85 / 90 / 97 / 100%) → hướng dẫn → vé và chương chính → phụ (≤ 25% thời gian) → Pokédex → 3 nhiệm vụ ngày (+2 tuần) → sự kiện → đánh quái qua năng lượng → thuốc, sửa đồ, dịch chuyển → học chiêu → đổi bậc → nâng cấp (mục tiêu +12 / +14 / +15) → ngọc hệ → trang trí → lượng khoá / lượng → thành tựu. Bot bán 90% xu mỗi ngày qua chợ (bị đánh thuế).

**Kết quả: 12/12 mục tiêu đạt** (`mo_phong_tom_tat.txt`).

| Mục tiêu | Kết quả |
|---|---|
| Nhẹ: ≤ 5% xong năm 1 trước tháng 12 | 0% |
| Nhẹ: trung vị ≤ tháng 18 | tháng 12,8 |
| Cày: ≤ 5% xong năm 1 trước tháng 6 | 0% (sớm nhất ngày 287) |
| Tiến hoá đầu ≤ 1,2 giờ | 1,10 giờ |
| Bot ≤ 1,3 × cày | 5,30 so với 4,36 giờ hiệu dụng |
| Xu thực tăng ≤ 5%/tháng | 2,7% (danh nghĩa 8,0%, do cấp tăng) |
| Xu TB ≤ 20 giờ thu nhập | ≤ 3,2 giờ |
| Ra/vào xu 80–105% | 99,4% |
| D1 / D7 / D30 ≥ 40 / 20 / 10% | 68 / 49 / 29% |

**Kịch bản so sánh** (để thấy cơ chế nào giữ cân bằng):

| Kịch bản | Kết quả |
|---|---|
| A. Không có năng lượng huấn luyện | Bot đạt 20 giờ hiệu dụng/ngày, gấp 3,4 lần người cày |
| B. Nâng cấp giá cố định 5.000 xu và không có ngọc hệ (như game cũ) | Cung tiền thực **tăng 12%/tháng** (danh nghĩa 19%) |

Hình:
- `mo_phong_cap.png`: cấp theo nhóm và trần.
- `mo_phong_xu.png`: xu mỗi người đang chơi.
- `mo_phong_cot_truyen.png`: % đã xong năm 1.
- `mo_phong_nang_cap.png`: mức +k trung bình.

**Giới hạn của mô hình:**
- Không có chợ đấu giá thật; giá đá cố định bằng 0,5 × giá tự kiếm.
- Không mô hình PvP, boss, đội (chỉ là hệ số giữ chân).
- Cầu đồ trang trí là giả định hành vi (2%/ngày phần vượt 20 giờ).
- Vì vậy phải **theo dõi thật** bằng các chỉ số ở mục 2.5 ngay từ ngày đầu.
