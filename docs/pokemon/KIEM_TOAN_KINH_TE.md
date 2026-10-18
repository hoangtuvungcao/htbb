# Kiểm toán kinh tế & tiến trình (trạng thái hiện tại, 2026-10-17)

> Nhánh `pokemon/chuyen-doi`. Tài liệu **chỉ đọc**: mô tả code/dữ liệu đang có, có số liệu và `file:dòng`.
> Thiết kế thay thế: `THIET_KE_LAU_DAI.md`. Mô phỏng: `tools/pokemon/MoPhongKinhTe.java`.
> Kế hoạch sửa code: `KE_HOACH_LAU_DAI_GD2.md`.
>
> Số liệu lấy từ `sql/htbb_day_du.sql` (seed), `server.ini` (giá trị khởi tạo) và mã nguồn.
> Lưu ý: tỉ lệ rơi/tỉ lệ EXP thật trên máy chủ đang chạy nằm ở bảng `cau_hinh`
> (`src/server/CauHinh.java:45-46, 283`), có thể khác `server.ini`. Muốn số thật phải
> `SELECT * FROM cau_hinh` trên máy chủ.

## 0. Tóm tắt 10 phát hiện lớn

| # | Phát hiện | Hệ quả | Mức |
|---|---|---|---|
| 1 | Bảng EXP `LevelDetail.expRequire` tăng ×1,2/cấp tới 70, **×3 ở cấp 71**, rồi ×1,5/cấp. Tổng tới 91 = **7,53·10¹³** EXP | Không ai lên 91 bằng đánh quái; cấp cao chỉ đạt bằng vé x2/x50, quà sự kiện, GM | Nghiêm trọng |
| 2 | 5 hệ số EXP chồng nhau: ×3 toàn cục (`TangEXP`), giữ 10% từ cấp 50 / 50% dưới 50, chia 3 từ cấp 60, ×0,6 từ cấp 80, giảm theo lệch cấp ÷1..÷20 | Số quái để lên 1 cấp: cấp 30 ≈ **765**, cấp 50 ≈ **41.000**, cấp 60 ≈ **500.000**, cấp 80 ≈ **134 triệu** | Nghiêm trọng |
| 3 | Lượng (tiền cao cấp) **rơi từ quái**: cấp ≥ 40, mỗi con 2,5% ra 1–4 lượng khoá **và** 2,5% ra 1–4 lượng thường | Bot/treo máy là nguồn tiền cao cấp chính; lượng mua được đồ tăng sức mạnh trong shop | Nghiêm trọng |
| 4 | Xu từ quái gần như bằng 0 (0,32% × `rcvgold/30`) ⇒ nguồn xu thật là **bán đồ cho NPC**, quà cấp 35 (10 triệu xu), giftcode, sự kiện | Tiền vào không gắn với thời gian chơi; người mới nghèo, người cũ giàu vô hạn (không có trần) | Cao |
| 5 | Giftcode `tt` = **2.000.000 lượng + 2.000.000 lượng khoá**, còn 998 lượt; mốc nạp = 100k lượng + 200k lượng khoá | Một mã phá cả nền kinh tế lượng | Nghiêm trọng |
| 6 | Shop lượng bán đồ **tăng sức mạnh** (áo +10% tấn công/phòng thủ +3000–5000 HP, vé x2 EXP, kẹo tiềm năng) | Trái nguyên tắc Q10 "không thu tiền / không pay-to-win"; lượng ⇒ sức mạnh | Cao |
| 7 | Nâng cấp +1..+15: **5.000 xu/lần cố định** mọi cấp đồ; tỉ lệ 100→0%; `Map.random()` tạo `Random(currentTimeMillis)` mỗi lần ⇒ cú roll thành công và roll bảo hiểm **trùng số** (bảo hiểm gần như vô dụng) | Hố tiền (sink) không lớn dần theo cấp ⇒ không hút được tiền người giàu; may rủi không công bằng | Cao |
| 8 | Không đồ nào/quái nào được đặt trên bản đồ trên cấp 86; Ursaluna (90) không xuất hiện; quái 80–86 HP 1–10 triệu, thủ 25.000–27.000 | Người chơi không đồ không gây nổi sát thương từ cấp ~60 | Cao |
| 9 | Nhiệm vụ: 18 nhiệm vụ chính (tới cấp 17) + 18 nhiệm vụ "Rèn luyện" lặp + 10 nhiệm vụ đội. Main story **hết ở cấp 17** | Không có mục tiêu dài hạn, chỉ còn cày lặp | Cao |
| 10 | Sự kiện: 28 mã trong `SuKien.DANH_MUC`, bật/tắt tay; phần thưởng là xu/lượng **không hết hạn** (vd. hộp World Cup 200k–10M xu, 500–2.000 lượng) | Mỗi sự kiện là một lần bơm tiền không có hố hút tương ứng | Cao |

## 1. Lên cấp (EXP)

### 1.1 Bảng EXP (`src/real/LevelDetail.java:11-14`)

- `expMain[i]` = EXP tích luỹ để **đạt** cấp i+1 (91 phần tử, `expMain[90] = 75.303.438.779.430`).
- `expRequire[L]` = EXP từ L → L+1. Không đơn điệu: cấp 8 (16.000) = cấp 7; cấp 17 < cấp 16 về tỉ lệ; cấp 21 (497.688) **nhỏ hơn** cấp 20 (574.256).
- Hình dạng: ×1,2/cấp ở 20–70; **×3 tại 71** (3,77·10⁹ → 1,13·10¹⁰); ×1,5/cấp từ 72.
- Trần cứng 91 (`setExpNew` L87-105). `xpUyThac` (48 giá trị) **không được dùng ở đâu** (code chết).
- Mất EXP khi chết: `expRequire[lv]/1000`, PK: `/100` (`getXPLost` L165-176).
- Mỗi cấp: +5 điểm tự do, +1 mỗi chỉ số, +1 điểm kỹ năng (`MapXuLyLenh.java:117-124` và bản sao `:248-254`).

| Cấp | EXP lên cấp (cũ) | EXP/quái cùng cấp (sau mọi hệ số) | Số quái cần |
|---|---|---|---|
| 10 | 24.000 | 300 × 1,125 × 0,5 × 3 ≈ 506 | ≈ 47 |
| 30 | 2.567.962 | ≈ 3.358 | ≈ 765 |
| 50 | 82.041.239 | ≈ 2.003 | ≈ 41.000 |
| 60 | 507.977.728 | ≈ 1.015 | ≈ 500.000 |
| 70 | 3.774.317.037 | ≈ 1.580 | ≈ 2,4 triệu |
| 80 | 290.194.852.524 | ≈ 2.160 | ≈ 134 triệu |

Với ~300 quái/giờ: cấp 50→51 mất ~140 giờ, cấp 60→61 ~1.700 giờ. Tiến trình bình thường
**dừng hẳn ở khoảng cấp 50–55**; phần trên chỉ đạt bằng nhiệm vụ "Rèn luyện" (cấp 80: 250 triệu
EXP/7.000 quái), vé x2/x2,5/x3, ngày `dayopenx50`, kẹo EXP, quà sự kiện.

### 1.2 Đường đi của EXP khi hạ quái (7 bản sao song song)

1. Mỗi đòn: `Monster.getXpReceive(dmg)` (`Monster.java:497-515`) ⇒ tổng ≈ `rcvxp` của template.
2. Lệch cấp (chỉ khi người cao hơn quái, `Region.java:1163-1175`): `a=min(3,Δ/4)`, chia `{1,5,10,20}`; dưới cấp 40 dùng `Δ/6` và `{1,2,3,4}`.
3. `Map.rand10`: +0..25% (`Map.java:4915`).
4. Tổ đội (`Map.calculatorXpParty` `Map.java:4883-4900`): **lỗi** `nUser` luôn = 5 khi >1 người ⇒ người đánh 1,0×, thành viên 0,8×.
5. `Monster.apDungGiamExpTheoCapNV` (`Monster.java:807-818`): từ cấp 50 giữ **10%**, dưới 50 giữ **50%** (`cau_hinh.tileExpLv50/DuoiLv50`).
6. `CharCapNhat.expReceive` (`CharCapNhat.java:29-138`): cộng % pet/cánh/mặt nạ/pháo hoa/vợ chồng…; vé `x2Player` 1,5/2/2,5/3; **×3·TangEXP toàn cục** (L127); cấp 75–79 ×0,65, ≥ 80 ×0,6.
7. `MapXuLyLenh.addXPForChar` (L168-203): **cấp ≥ 60 chia 3** (trừ Rễ năng lượng).

Bản sao cần sửa đồng loạt (CODING_CONVENTION §3): `Region.java` 1321/1582/1690,
`RegionVantienTran.java` 1003/1279/1405, `MapTown.java` 817/1067/1197, `MapLienDau.java`
1692/1952/2083, `MapDanhNhau.java` 840/979/1566, `MapClan.java` 726/862, `CharCopy.java`
383/496/620, `CharTrangBi.java` 402.

### 1.3 Nguồn EXP khác

| Nguồn | Ở đâu | Số |
|---|---|---|
| Nhiệm vụ chính 1–18 | `data_quest`, `InfoQuestTemplate.java:72` | 100 → 900.000 (hết ở cấp 17) |
| "Rèn luyện" lặp (18 dòng) | `data_quest` type 4 | cấp 20: 0,5–2 tr; cấp 60: 24–80 tr; cấp 80: 150–250 tr; reset mỗi ngày, tốn điểm hoạt động 30/60/100 (100/ngày, trần 300) |
| Vận tiêu | `CharSuKien.java:60-90`, `Char.EXP_NV_HANG_NGAY` (`Char.java:1164`) | từ cấp 50: `expRequire × 5..90‰` |
| Rễ năng lượng (#9) | `doUsePotion.java:222-236` | +1% cấp, bỏ qua chia 3 |
| Kẹo EXP #10/11/12 | `doUsePotion.java:248` | 100k/500k/1M |
| Vé x2 (#35, #75), x2,5 (#81) | `doUsePotion.java:397-480` | 60–180 phút |
| Sự kiện | `MapSuKien.java`, `MapMenuNpc.java:435-467`… | 20k–3M cố định (vô nghĩa ở cấp cao, quá lớn ở cấp thấp) |
| Treo máy / offline | — | **Không có** |
| Ngày x2/x3/x50 | `DatabaseXepHang.java:40-70`, `tob_x2exp` | toàn máy chủ |

### 1.4 Quái và bản đồ

- 138 template; quái thường đặt trên map từ cấp 1 tới 86 (map 28: Liepard shiny 82, Luxray 84, Entei 86). **Không có quái thường nào trên 86**; 11 template không được đặt (61, 64, 66, 67, 69, 70, 72, 73, 75, 78, 79, 135).
- `rcvxp` ≈ HP/10 tới cấp 20, sau đó HP/13–/30; từ cấp 82 HP nhảy 1M → 10M nhưng EXP đứng 53–55k.
- Hồi sinh quái thường 6 giây (`Monster.java:63`); boss 1–24 giờ.
- Trứng/đồ đặc biệt: Gyarados (38) rơi 100.000 xu `rcvgold`, Zapdos 100.000, Darkrai 10 triệu.

## 2. Chiến đấu và chỉ số

| Mục | Hiện trạng (file:dòng) | Vấn đề |
|---|---|---|
| Chỉ số gốc | `Database.java:83`; mỗi cấp +1 tất cả, +5 tự do | Ổn |
| HP | `(health+…) × {80,70,60,70,60}` theo class (`CharChiSo.java:49-76`) | Eevee (c2) HP thấp nhất, bù bằng MP×50 |
| Tấn công | c0 str×4/3; c1 str×1,1×1,5; c2 spirit×5/3; c3 2×str; c4 agi×5/3 (`CharChiSo.java:1229-1270`) | c1 (Bulbasaur) cao nhất khi dồn điểm |
| Hệ/STAB | `ChieuPokemon.apDung` áp **trước khi trừ thủ** (`CharChiSo.java:753-758`) | Hệ khắc chế ×1,5 thành ×2,56 thực tế ⇒ khuếch đại; quái thì áp **sau** thủ (`LiveActor.java:395-467`) ⇒ bất đối xứng |
| Giai đoạn | +8% / +16% / +12% HP & tấn công (`LoaiPokemon.java:171-216`) | Ổn, nhỏ |
| Chiêu | `CharManager.java:59-70`; chiêu 3 nhiều đòn: 295% × 8 = **2.360%**/lần (cấp 9) | Một chiêu áp đảo mọi chiêu khác (135–450%) ⇒ xoay chiêu vô nghĩa |
| Bảng c1 | thêm dòng ở chỉ số 6 ⇒ chiêu học 6/7/8 của Bulbasaur thấp một bậc | Lỗi dữ liệu |
| Học chiêu | 60k/120k/300k xu (`Map.java:114-118`) | Hố tiền một lần, quá nhỏ ở cấp cao |
| Lửa ×2 | `getValueLua(2)` cộng hai lần (`Char.java:3758`, `CharChiSo.java:775`) | Lỗi |
| PvP | sát thương /5 rồi −1/3 (×0,267), trần 50k/75k (`Region.java:748-764`, `MapDanhNhau.java:263-288`) | Trần cố định không theo cấp |

**TTK hiện tại (không trang bị, dồn điểm, cùng cấp):**

| Cấp | Người hạ quái | Quái hạ người |
|---|---|---|
| 10 | 12–31 đòn thường | 20–44 đòn (c0–c2) |
| 30 | **39–207 lần chiêu 3** (Dodrio thủ 340 ≈ tấn công người) | **4–6 đòn** |
| 60 | ~750 lần chiêu 3 (thủ Arcanine 900 > tấn công người 580–740) | 1–2 đòn |
| 90 | miễn nhiễm | 1 đòn |

⇒ Nội dung giả định người chơi có đồ +10..+15. Không có "ngân sách chỉ số" nào; quái 70+
có thủ 7.100–75.000 trong khi vũ khí lv81 chỉ +360 tấn công.

## 3. Trang bị và nâng cấp

### 3.1 Danh mục (924 dòng `data_item`; `docs/danh_muc_item.md` đã cũ: 915 dòng)

| type | Ô | Tên Pokémon | Số dòng | Cấp |
|---|---|---|---|---|
| 0 | áo | Khăn quàng | 132 | 0–85 |
| 1 | quần | Đai | 71 | 0–85 |
| 2 | nón | Băng đô | 64 | 0–85 |
| 3–7 | vũ khí | Than lửa / Hạt giống thần / Khăn lụa / Nước thần / Nam châm | 48–51 mỗi loại | 1–86 |
| 8 | nhẫn ×2 | Huy hiệu | 32 | 4–95 |
| 9 | dây chuyền | Vòng cổ | 32 | 4–95 |
| 10 | giày | Lông vũ tốc độ | 32 | 4–95 |
| 11 | găng | Găng đấm | 32 | 4–95 |
| 12 | ngọc bội | Phiến đá hệ | 32 | 4–95 |
| 14–18 | đồ thú cưỡi | tên Poké Ball theo cấp | 12 mỗi loại | 30–85 |
| 19 | phi phong | Ruy băng | 25 | 0–100 |

Bậc theo cấp: sơ cấp / đồng / bạc / vàng / bạch kim / kim cương / chuyên gia / tinh anh / huyền thoại
(`tools/pokemon/sinh_sql_vat_pham.py`). Ví dụ Than lửa: tấn công 30 (lv1) → 180 (lv36) → 360 (lv81).

### 3.2 Hệ nâng cấp

| Hệ | Chi phí | Tỉ lệ | Thất bại | Ghi chú |
|---|---|---|---|---|
| Cường hoá +1..+15 (`MapEpNgoc.java:598-769`, `CharCapNhat.java:866-1036`) | **5.000 xu/lần, mọi cấp đồ** + đá Luyện kim dược bắt buộc | 100,90,85,80,70,30,15,10,7,4,3,2,1,1,0 (+1% do `<=`) | −1 cấp, mất chỉ số | **Trừ tiền trước khi kiểm tra có đá** (`:639` vs `:641-648`) |
| Bảo hiểm (ngọc 0–4: 20–90) | mua ở Phú ông 100–500 xu | — | — | **Vô dụng** khi < ngưỡng thành công: `Map.random()` reseed theo ms ⇒ 2 cú roll trùng số (`Map.java:3320-3323`, `MapEpNgoc.java:651-653`) |
| Ép ngọc 3/4/5 viên | 5.000 xu, mất hết khi trượt | 11% / 21% / 100% (`<=` ⇒ lệch 1%) | mất ngọc | `MapEpNgoc.java:862` |
| Đục lỗ / khảm | 20.000 xu | 100% | — | tối đa 6 lỗ |
| Thăng cấp đồ (+5 cấp) | 5.000 xu + nguyên liệu | — | — | giữ plus−1 |
| Tinh luyện | 3.000.000 xu + 1 lượng + 1 vũ khí | 3% → 60% (`Map.java:9725-9737`) | mất nguyên liệu | không bảo hộ |
| Thần Khí +1..+10 | nguyên liệu | 72 → 12% | mất nguyên liệu, giữ cấp | `ThoRenBiAn.java:581` |
| Pet | nguyên liệu cấp 6 | 90/45/20/15/7 | mất nguyên liệu | **AIOOBE ở bậc 5** (`Pet.java:69-70,703`) |

Kết luận: hố tiền của nâng cấp **không tăng theo cấp/bậc đồ** (luôn 5.000 xu) nên không hút
được tiền khi người chơi giàu dần; độ khó nằm ở tỉ lệ 0–4% không có cơ chế bù (pity) ⇒ ức chế,
và người có lượng (từ giftcode/nạp) mua đá may mắn bỏ xa người chơi thường.

## 4. Tiền tệ: nguồn vào (faucet) và hố hút (sink)

### 4.1 Ba đồng tiền (`Char.java:94-106`)

| Đồng | Trường | Trao đổi | Trần |
|---|---|---|---|
| Xu | `xu` (long), DB `gold` | **Không** qua gói 66; chỉ qua chợ ký gửi (thuế 10%), bán pet (10%), cá cược đấu trường (0%) | Không (Long.MAX) |
| Lượng | `luong` | Chuyển NPC (không phí, nhận ≤ 5.000/ngày), đấu giá | Không |
| Lượng khoá | `lockLuong` | Không | Không |

### 4.2 Faucet

| Nguồn | file:dòng | Lượng | Tần suất | Đánh giá |
|---|---|---|---|---|
| Xu rơi từ quái | `Monster.java:919-945` | 0,32% × (`rcvgold/30`±4) ≈ 0,5 xu/quái | mọi quái | quá nhỏ, x2 gold bị ghi đè (L931) |
| **Lượng rơi từ quái (cấp ≥ 40)** | `Monster.java:1091-1108` | 2,5% × 1–4 lượng khoá **+** 2,5% × 1–4 lượng; trần 50.000/ngày/loại | mọi quái | ~14 lượng + 14 lượng khoá/giờ; bot 20 giờ ≈ 560/ngày |
| Bán đồ cho NPC | `OfflineMap.java:1169-1171` | `price/5 + plus×1000` (+10/30% VIP) | cao | nguồn xu chính |
| Quà cấp 35 | `MapXuLyLenh.java:303` | 10.000.000 xu | 1 lần | = ~vài tháng thu nhập cấp 35 |
| Boss | `Monster.java:1694-1707` | 4 × 25.000 xu, ai cũng nhặt được | vài/giờ | ổn |
| Nhiệm vụ | `InfoQuestTemplate.java:78-80` | tổng 9.300 xu, 0 lượng | 1 lần | gần 0 |
| **Giftcode** | `DatabaseGiftCode.java:430-495` | `tt`: 2M lượng + 2M lượng khoá × 998 lượt | theo mã | phá giá |
| Mốc nạp | `MapQuaTang.java:832-834` | 100k lượng + 200k lượng khoá | mỗi mốc | trái Q10 |
| Sự kiện | `SuKienWorldCup.java:407-495`, `BossTrungThu.java:226`… | 200k–10M xu, 500–50.000 lượng | theo sự kiện | không hết hạn |
| Hoàn xu khi tiêu lượng | `Char.java:14789-14797` | 300k xu / 100 lượng | Giỗ Tổ/Mini | chuyển lượng → xu |
| Liên đấu | `MapDoMenu.java:1786-1845` | 100 lượng; 10 × thắng lượng khoá | tuần | ổn |

### 4.3 Sink

| Hố hút | file:dòng | Chi phí | Đánh giá |
|---|---|---|---|
| Shop đồ NPC | `OfflineMap.java:1495-1507` | `data_item.price` (lv80 ≈ 380k) | tốt, nhưng mua một lần |
| Thuốc (Phú ông) | `OfflineMap.java:1441-1464` | 50–800 xu/bình | quá rẻ ở cấp cao |
| Shop đặc biệt | `doBuySpecialItem.java:40+` | 10–1.000 lượng; 50k–5M xu | bán sức mạnh |
| Sửa đồ | `Char.java:4197-4253` | Σ price/10 | tốt, tỉ lệ theo cấp |
| Hồi sinh | `Map.java:10416-10471` | 1k → 4,5M theo cấp | tốt |
| Dịch chuyển | `Map.java:16909`, `BaiLuyenTap.java:55-77` | 500–30.000 xu | quá rẻ ở cấp cao |
| Học chiêu | `Map.java:15528` | 60k/120k/300k | một lần |
| Cường hoá/khảm/ép | `MapEpNgoc.java` | 5k/20k/5k | **không theo cấp** |
| Tinh luyện | `Map.java:9692` | 3M xu + 1 lượng | tốt |
| Lập đội | `MapDoi.java:1069` | 100M xu | một lần |
| Thuế chợ | `OfflineMap.java:376-397` | 10% (hoặc thuế đội 80% về quỹ) | tốt |
| Xổ số | `Char.java:944/958` | thuế 10% xu / 15% lượng | tốt |
| Đổi tên | `CharDoiTenNhanVat.java:29` | 100.000 lượng | quá đắt, ít dùng |
| Kho | `Char.java:14356-14472` | **miễn phí** | — |

### 4.4 Lạm phát đến từ đâu (xếp theo mức độ)

1. **Giftcode và mốc nạp** tạo lượng không giới hạn (2M/mã). Một giftcode = thu nhập lượng của ~70.000 giờ cày.
2. **Lượng rơi theo số quái**, không có trần theo thời gian chơi hợp lý (50.000/ngày ≫ người thật) ⇒ bot treo 24/7 là máy in tiền cao cấp; lượng lại mua được đồ tăng sức mạnh.
3. **Quà cố định không theo cấp** (cấp 35: 10M xu; sự kiện: 200k–10M xu) ⇒ giá trị so với thu nhập lệch hàng trăm lần giữa cấp thấp và cao.
4. **Không có hố hút tăng dần**: nâng cấp 5.000 xu/lần cố định; thuốc 50–800 xu; dịch chuyển 500 xu ⇒ người giàu tích luỹ mãi. Kho không phí, số dư không trần.
5. **Sự kiện trả tiền tệ vĩnh viễn** thay vì vật phẩm hết hạn/đồ trang trí.
6. **Bán đồ cho NPC** là nguồn xu chính và **tăng theo số đồ rơi** (bot nhặt hết).

### 4.5 Tiến trình quá nhanh/quá chậm ở đâu

| Giai đoạn | Hiện trạng | Đánh giá |
|---|---|---|
| Cấp 1–20 | vài giờ, có 18 nhiệm vụ chính dẫn dắt | Hơi nhanh nhưng ổn; nhiệm vụ 1 ghi "999999999 xu" trong `infogift` (chữ sai, thực nhận 1.000) |
| Cấp 20–50 | không còn nhiệm vụ chính; quái cùng cấp mất 39–200 chiêu mới chết nếu không có đồ | **Chậm và trống** |
| Cấp 50–70 | 41k → 2,4 triệu quái/cấp | **Không thể** nếu không có vé/sự kiện |
| Cấp 70–91 | 10⁸ quái/cấp | Chỉ GM/x50 |
| Trang bị | tỉ lệ +10..+15 = 4..0% | Ức chế, phụ thuộc lượng |

## 5. Sự kiện (28 mã, `src/real/SuKien.java:142-205`)

- Bật/tắt qua `tob_su_kien` + lịch `mo_luc/dong_luc` (`LichSuKien.java`), **phải khởi động lại** máy chủ (shop/NPC tính một lần khi bật). Hạ tầng này dùng lại được cho lịch 52 tuần.
- Nhóm: Tết (3), Cuối năm (6), Mùa (8: Trung Thu, Halloween ×3, Hè ×2, Vu Lan, Giỗ Tổ), Mini (3), Kỷ niệm (4), World Cup (3), Bảng xếp hạng (1).
- Không có: Valentine, 8/3 riêng ngoài "Quốc tế phụ nữ", 30/4, 20/10, sự kiện chủ đề Pokémon (Community Day, Shiny, Raid huyền thoại, tuần hệ).
- Phần thưởng hiện tại tính bằng xu/lượng/EXP cố định, không theo cấp, không có ngân sách.

## 6. Nhiệm vụ (`data_quest`, 49 dòng; nạp ở `DatabaseXepHang.java:471-560`)

| Cột | Giới hạn kỹ thuật |
|---|---|
| `id` | **Không dùng**: server đánh số lại 0,1,2… theo thứ tự đọc (`info.id = id++`), không `ORDER BY` ⇒ thứ tự = thứ tự chèn (MyISAM) |
| `mainsub` | 0 chính (chuỗi tuyến tính `idMainQuest++`), 1 phụ (chỉ nhận **1** cái, reset mỗi ngày), 2 đội |
| `type` | 0 hạ quái theo id; 1 đưa thư (vật phẩm nhiệm vụ id 4); 2 thu thập vật phẩm nhiệm vụ rơi từ quái; 3 nói chuyện; 4 hạ quái lệch ±`deltaLv` |
| `npcrcv/npcres` | gửi client bằng **1 byte** (`MessageCreator.java:2909`) ⇒ −128..127 |
| `lv` | **Không được kiểm tra** với nhiệm vụ chính (chỉ lọc type 4 phụ: `lv ≤ cấp < lv+10`) |
| Vật phẩm nhiệm vụ | chỉ **10 loại** cố định trong `data.ItemQuest.NAME_ITEM_QUEST` (lớp trong `lib/htbb-core.jar`, không có nguồn) |
| Danh sách gửi client | `writeByte(allQuest.size())` ⇒ tối đa 127 nhiệm vụ nhận được cùng lúc; hiện liệt kê **mọi** nhiệm vụ phụ |
| `exp` | đọc bằng `getInt` ⇒ ≤ 2,147·10⁹ |
| `infogift` | varchar(200), `name` varchar(45) |
| Điểm hoạt động | `pointActiveQuest` +100/ngày, trần 300 (`CharCapNhat.java:565-578`) |

Không có: nhiệm vụ tuần, chuỗi điều kiện tiên quyết, khoá theo tuần, nhiệm vụ Pokédex, thành tựu.

## 7. Những gì giữ lại được

- Hạ tầng `SuKien`/`LichSuKien`/`tob_su_kien` (lịch hẹn, trang admin) ⇒ đủ cho lịch 52 tuần.
- `data_quest` + `NewCharQuest` cho các kiểu 0–4 ⇒ biểu diễn được ~80% nhiệm vụ mới (xem `nhiem_vu_moi.csv`, cột `dong_co_tuong_thich`).
- Pokédex (`tob_pokedex`, `tob_pokedex_thuong`) ⇒ nền cho nhiệm vụ nghiên cứu.
- Thuế chợ, thuế xổ số, sửa đồ, hồi sinh theo cấp ⇒ hố hút đúng hướng, chỉ cần chỉnh số.
- `cau_hinh` (tỉ lệ rơi/EXP chỉnh không cần build) ⇒ nơi đặt tham số mới.

## Bổ sung (kiểm toán tiền tệ, 17/10/2026)

Nguồn/hố bị sót ở bản đầu. "Đã sửa" = trong GĐ2 (`KE_HOACH_LAU_DAI_GD2.md` mục "Trạng thái thực hiện").

| # | Phát hiện | file | Xử lý |
|---|---|---|---|
| A1 | **Nghiêm trọng**: khối đổi ngày cộng thẳng `lockLuong += 1000` cho mọi nhân vật cấp ≥ 40 mỗi ngày (bỏ qua `KHOA_TIEN`, trần 50k, không log). 3 nhân vật/tài khoản ⇒ 3 000 lượng khoá/ngày | `CharCapNhat.java` (khối đổi ngày) | Đã sửa: tắt cờ đi qua `addLuongLock` + log; bật cờ thì bỏ, thay bằng lịch đăng nhập 7 ngày theo **tài khoản** (3–15) |
| A2 | Vận tiêu trả 100–200 lượng (0,5% lượng mở) | `CharSuKien.java` | Đã sửa (cờ): chỉ lượng khoá, ≤ 10/lần |
| A3 | Quà sự kiện offline 5 000/2 000/1 000 lượng | `Map.java` (quà top offline) | Đã sửa (cờ): 50/20/8 lượng theo ngân sách xếp hạng |
| A4 | Hệ số `TileNap` admin không kiểm trên | `DatabaseXepHang.java` | Đã sửa: chặn ≤ 3 (đội nạp tiền tiếp quản phần còn lại) |
| A5 | Đổi điểm xài lượng → xu ×10 000 | `MapSuKien.java` | Đã sửa (cờ): tắt |
| A6 | Code lì xì (lượng mở), đổi kẹo `lockLuong +=` thẳng, BossTrungThu | `MapNhapText`, `RequestNPCInfoHandler`, `BossTrungThu` | Đổi kẹo: đã đi qua `addLuongLock` + log; lì xì (cờ): lượng khoá ≤ 15; BossTrungThu đã dùng `addLuongLock` |
| D1 | Lượng train quái dừng khi đang giữ ≥ 50k (`Char.java` ~14506) - bản đầu ghi "không trần" | `Char.java` | Ghi chú: **có** trần giữ 50k; bật cờ thì nguồn này bỏ hẳn (GĐ2 2.1) |
| D2 | Khoảng thuế chợ 1–20% (nhập chữ) khác 0–30% (gói tin) | `MapNhapText`, `Map.java` | Đã sửa (mọi chế độ): chung `Map.THUE_DOI_TOI_THIEU/TOI_DA` = 1..20 |

**Hố hút còn thiếu trong mô hình** (giữ nguyên, đưa vào chỉ số `tob_kinh_te_ngay` qua `Char.subXu`): đặt lại lỗ khảm,
nâng cấp thời trang, mở rộng túi, nâng cấp/đổi màu/xoá thú cưỡi, ly hôn/cưới, rời bang, chế tạo, quy đổi, thả đèn,
xổ số, hộp may mắn, mua EXP Dracula, chuộc lại đồ đã bán, đặt cọc đấu giá.

**Lớp chỉ có trong `lib/htbb-core.jar` (không sửa được, CODING_CONVENTION §12)**: `RegionMapMoba` (addLuong 100 × 2),
`PrivateDataMap` (addXu 40k/50k), `TongKimData`, `ArenaData`. Không bọc được từ ngoài mà không đổi tên lớp; theo dõi bằng
`tob_kinh_te_ngay` (xu vào/ra) và tắt các bản đồ/sự kiện đó khi chạy thiết kế dài hạn nếu chỉ số đỏ.

**Thưởng mới GĐ2 đã đưa vào mô phỏng** (`MoPhongKinhTe`, module "GD2 bổ sung"): quà hoạt động (≤ 5 lượng khoá + 5% giờ xu/ngày/tài khoản),
Hầm ngục (5 lượng khoá/ngày), boss thế giới (3/tuần), chiếm thành (5–20/tuần, chia quỹ bang là chuyển tiền). Kết quả vẫn **12/12**,
xu ra/vào 99,3%, lượng khoá ra/vào 95,0%.

**Cửa hàng & đổi vật phẩm (18/10/2026, luật "không item mồ côi", `docs/pokemon/KIEM_ITEM.md`)**: Sổ tay huấn luyện → mục 9
(`real.CuaHangLauDai`, bảng `data_cua_hang_lau_dai` sinh bởi `tools/pokemon/sinh_vat_pham_lau_dai.py`, chỉ khi `sv.lau_dai=1`).
Ngân sách không đổi vì mọi dòng **chỉ ra vật phẩm, không bao giờ ra xu / lượng / lượng khoá** (generator và
`test/KiemItemMoCoi.java` chặn):
- Thuốc trạng thái / X / xua quái / quả mọng bán bằng xu đúng giá thiết kế (`noi_dung_vat_pham.csv`, gói 5) = một phần của hố
  hút "thuốc + sửa đồ" đã có trong mô phỏng (`TIEU_HAO_PHAN_TRAM`), không phải hố hút mới.
- Kecleon: Thuốc hồi sinh tối đa 5 lượng khoá (3/tuần), bùa bảo hộ 10/25 lượng khoá = đúng giá lượng khoá cũ khi nâng cấp
  thiếu bùa; nằm trong "lượng khoá thuốc 4/ngày" của mô phỏng.
- Trang bị năm 2 (Bronzong, xu, 1,5 giờ thu nhập) và chế tạo Machamp (đá + nguyên liệu phụ + 50% giá xu) đúng
  `noi_dung_cua_hang.csv` / `noi_dung_che_tao.csv` — vốn đã nằm trong ngân sách trang bị của `THIET_KE_LAU_DAI`.
- Đổi huy chương Gym / mùa giải / tín vật đột kích / vật phẩm sự kiện lấy đồ giữ, đá tiến hoá, Kẹo hiếm (vẫn 2/tuần khi dùng,
  trừ năng lượng), Mảnh tinh hoa / Vảy rồng, thuốc: giá trị ra ≤ giá trị tín vật trong ngân sách sự kiện tuần (≤ 70 giờ/năm).
- Boss thế giới: đá nâng cấp nay nằm trong Hộp vàng / Hộp tham gia (4–8 / 2–4 đá theo `data_noi_dung_roi` nguồn `mo_hop`,
  trước là 5 / 2 thẳng) + 1–3 tín vật đột kích tuần đột kích: +≈1 đá / người / tuần, không đáng kể so với rơi 1,5%/quái;
  lượng khoá giữ nguyên 10 / 3. Mô phỏng chạy lại vẫn **12/12** + **6/6**.
