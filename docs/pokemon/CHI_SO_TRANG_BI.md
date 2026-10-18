# Chỉ số trang bị: kiểm kê, mô hình Pokémon, luật chống lạm phát

Tài liệu cho người làm game. Trang người chơi đọc nằm ở web (Cẩm nang → Chỉ số) và không nêu chi tiết nội bộ.

Mọi luật ở mục 3 chỉ chạy khi bật `sv.lau_dai=1` **và** khoá cấu hình `chi_so.bat=1` (mặc định 1). Tắt một trong hai thì
server tính đúng như trước. Các file chính:

| Việc | File |
|---|---|
| Trần, giảm dần, sàn sát thương, PvP (lớp thuần) | `src/com/htbb/pokemon/GioiHanChiSo.java` |
| Ngân sách chỉ số một món theo ô / cấp / nâng cấp (lớp thuần) | `src/com/htbb/pokemon/NganSachTrangBi.java` |
| Bảng 198 dòng chỉ số: tên, đơn vị, trạng thái, mô tả (nguồn duy nhất) | `src/com/htbb/pokemon/ChiSoTrangBi.java` |
| Nối vào server (`Char`, `CharChiSo`, PvP...) | `src/real/ChiSoTrangBiGame.java` |
| Sinh SQL + CSV | `tools/pokemon/SinhSqlChiSo.java` → `sql/pokemon_chi_so.sql`, `docs/pokemon/chi_so_trang_bi.csv` |
| Mô phỏng TTK / bất tử / bậc đồ / kiểm data_item | `tools/pokemon/MoPhongChiSo.java` → `docs/pokemon/mo_phong_chi_so.txt` |
| Kiểm thử | `test/KiemChiSo.java` (trong `test/Kiem.java`) |

## 0. Tóm tắt trước / sau

| Chỉ số | Trước (công thức dài hạn, không trần) | Sau |
|---|---|---|
| Tỉ lệ chí mạng | cộng thẳng mọi nguồn (điểm/300 + bạo kích + đồ giữ + buff), 80% trở lên được | giảm dần từ 25%, **trần 40%** (PvP 30%) |
| Đòn chí mạng | ×1,5 + bảo vật (tới +25%) + liên kích pet (+15%) cộng không trần | **tối đa ×2,0**; PvP ×1,5 phẳng |
| Hút HP | bảo vật 10% + mặt nạ + đồ giữ + hút phẳng; PvP hút trên sát thương **chưa** ×0,30 (gấp ~3,3 lần) | gộp một nguồn, **10% PvE / 5% PvP** (trên sát thương thật), boss/Gym/tinh anh ×10%, mỗi đòn ≤ 0,2% HP tối đa |
| Né tránh | pet + bảo vật tới 90% | **30%** (PvP 20%) |
| Giảm sát thương nhận | hấp thụ (thú cưỡi, skin, Lân...) không trần; bảo vật trần 80% | **50%** (PvP 40%) một lớp gộp |
| Kháng một hệ | không có (kháng theo "dòng" kiểu ngũ hành) | theo **hệ chiêu đang đánh**, **60%** |
| Tổng mọi lớp giảm của một đòn | chặn toàn phần (thú cưỡi `att[10]`, né theo hệ) → đòn còn 1 | **sàn 40%**: đòn luôn còn ≥ 40% |
| Phản đòn | bảo vật 20% + bộ hệ + skin tới 80% | **15%** (PvP 10%) |
| Tốc độ đánh / chạy | giảm hồi chiêu tới 50% (= +100% tốc đánh); tốc chạy +1 chồng lên thú cưỡi | **+50%** tốc đánh (hồi chiêu −33%), **+30%** tốc chạy |
| ST theo % HP mục tiêu | bảo vật cầm tay tới 24,1% HP mục tiêu mỗi đòn (29% tỉ lệ), bảo vật huyền thoại 6% | **1%** (boss 0,2%) và ≤ đòn gốc |
| ST lên boss / quái | 25% / 50% | **20% / 20%** |
| % cộng thêm tấn công / thủ / HP ngoài đồ | pet, thú cưỡi, thời trang, bộ +15, VIP, chức vụ, skin… nhân dồn (thường +100–200%) | **tổng +50%** trên chỉ số gốc |
| Thưởng bộ nâng cấp (+10/+13/+15 đủ bộ) | +1 / +4 / +8 / +12% tấn công, thủ, HP | **≤ 5%** (đúng THIẾT KẾ 3.4) |
| EXP từ trang bị / pet / thời trang / đồ giữ | cộng thẳng (tới +100% chung) | **+30%** (trần chung +100% giữ nguyên) |
| Hồi HP/PP mỗi 5 giây | pet "chữa trị" % HP + phẳng, không trần | **≤ 1% tối đa** mỗi nhịp |
| Gây trạng thái từ đồ (khảm cũ) | mỗi món cộng thẳng, 11 món có thể 100% đóng băng mỗi đòn | trạng thái Pokémon, **15%** (PvP 7,5%), kháng ≤ 50%, PvP thời lượng ×0,5 |
| Chỉ số một món | đồ tím 214–263 cao hơn đồ thường cùng cấp 70%, ruy băng sự kiện cấp 1 = thủ cấp 70, đồ chế tạo bạc/vàng 2× | **≤ chuẩn bậc × 1,30** (×nâng cấp); đồ bậc cũ ≤ 130% đồ bậc hiện tại |
| Nâng cấp chỉ số nhỏ | ép "+1 mỗi cấp": chí mạng 10 lên +15 thành ~25 (×2,5) | đúng % (10 → 16, ×1,55) |

Mô phỏng (`docs/pokemon/mo_phong_chi_so.txt`, 12/12 mục tiêu đạt):

- TTK quái thường của dòng chuẩn ở +0..+15 lệch tối đa 18,7% so với mục tiêu 4 / 5 / 6 giây. Mọi dòng nằm trong 0,71..1,42 lần
  mục tiêu (Squirtle chịu đòn chậm nhất, Pichu nhanh nhất).
- Dồn mọi chỉ số phụ tối đa: sau trần TTK thấp nhất bằng 48% mục tiêu (trước trần 27%).
- Gym 114–115 giây ở +6 (+0: 129 giây, +15: 94 giây). Boss thế giới 9,7–9,8 phút với 20 người, tinh anh 15,6–23,4 giây.
- Bất tử: trước trần có trường hợp hồi ≥ sát thương nhận (giảm 85%, né 60%, hút 35%). Sau trần, trường hợp xấu nhất là
  Squirtle cấp 30, +15 đánh tinh anh: hồi bằng 88% sát thương nhận.
- PvP cùng cấp, cùng đồ, dòng chuẩn, cấp ≥ 40: 11,6–14,5 giây. Khắc hệ ×2 kèm chí mạng tối đa vẫn ≥ 16 giây.
- Đồ bậc cũ +15 có ngọc 10% so với đồ bậc mới +0: trước kẹp tới 1,64 lần (Phiến đá 1,97), sau kẹp tối đa 1,29.
- `data_item`: 729 dòng trang bị nhân vật, **70 dòng vượt ngân sách trước migration, 0 sau**.
- Kinh tế: `MoPhongKinhTe` không đọc chỉ số trang bị nên 17/17 mục tiêu giữ nguyên (chạy trong bộ test).

## 1. Kiểm kê

### 1.1 Lưu trữ và ánh xạ id

`data_attribute` có 198 dòng: id 0..186 và 200..210. Server nạp **theo thứ tự dòng** (`Database.loadNameAttributeItem`, biến `byte i`)
và gửi cho client trong gói 25. Client (J2ME/Android/PC) tra tên theo **vị trí**, id thuộc tính của món đọc bằng `readUnsignedByte`.
Vì vậy **không được thêm, xoá hay đổi thứ tự dòng**. Test `KiemBangThuocTinh` giữ điều này. Id > 186 trên món không lưu được:
`otherAtt` dài 126 và nhiều vòng lặp dùng biến `byte`, nên lớn hơn 127 phần tử là lặp vô hạn. Cơ chế 18 hệ ở mục 2.3 vì thế
dùng lại id đã có.

| Id | Mảng trên `Item` | Ghi chú |
|---|---|---|
| 0..32 | `atb[i]` | `data_item.atb0..9` = id 0..9. 10..27 là chỉ số khảm (`Char.abilityKham`). 28..32 chỉ có tác dụng khi kích bộ hệ (`checkKickAtt5`) |
| 33..42 | `newAtb[i-33]` | kiểu `byte`. `Char.newAtb` là tổng của mọi món, cũng `byte`, nên **có thể tràn số âm** |
| 43..57 | `addMoreLevelSkill` | client tự thay tên bằng tên chiêu |
| 58..60 | `lockAtb` | % tấn công / thủ đặc biệt / thủ |
| 61..186 | `otherAtt[i-61]` | dòng thêm, dòng ẩn bảo vật huyền thoại (`ThoRenBiAn.HIDDEN_STAT_IDS`), mặt nạ, cánh, thú cưng |
| 200..210 | — | chỉ để hiện đặc tính skin thú cưỡi (`SkinThuCuoi`), không lưu trên món |

Đơn vị (`ispercent`) chỉ là cách client in số: 0 in số nguyên, 1 in "x%", 2 in "x/10 %" (giá trị lưu phần nghìn). Code mỗi nơi tự
chia /100 hoặc /1000 nên **nhiều dòng in sai đơn vị** (mục 1.5).

### 1.2 Id → nơi dùng

Bảng đầy đủ 198 dòng (tên cũ, tên mới, đơn vị, trạng thái, nhóm, mô tả, đường code) ở `docs/pokemon/chi_so_trang_bi.csv`
(sinh từ `ChiSoTrangBi`). Tóm tắt theo nhóm:

| Nhóm | Id | Nguồn ghi giá trị | Code áp dụng |
|---|---|---|---|
| Cơ bản | 0 tấn công, 1 thủ, 2 né, 3 chính xác, 4 chí mạng, 5 HP, 6 thủ đặc biệt | `data_item.atb0..6` × `Map.rand10` (+0..25%) × nâng cấp | `CharChiSo.calculateAttack / calculateDefend / calculateMaxHPMP`, `Char.calculateCritical / Dodge / Accurate` |
| Khảm (ngọc cũ) | 10..13 điểm, 14..19 gây trạng thái, 20..25 kháng, 26 đánh kép, 27 rơi bảo vật | `atb[10..27]` của món | `Char.calculateAttrib` → `abilityKham` → `buffSkillKham`, boss cũ (`khamKhangBang`...). 26, 27: không nơi nào gọi |
| Bộ hệ | 28..32 | đá thuộc tính, `doAddNewAttributeUseBot` | `attNguHanh[0..4]` → `subDam`, `calculateAttack`, `getBuffEffAttack` (bỏ qua thủ), phản sát thương |
| Dòng thêm (`newAtb`) | 33 HP ×1000, 34 PP ×1000, 35..38 điểm, 39 kỹ năng (thú cưng), 40 chí mạng, 41 ST chí mạng, 42 trống | đá thuộc tính, thú cưng | `Char.newAtb` → HP/MP, điểm. **41 trước không nơi nào đọc** |
| % (`lockAtb`) | 58..60 | khoá đồ, thú cưng | `calculateAttack / calculateDefend` |
| Mặt nạ / cánh / thời trang | 61, 62 bỏ qua thủ, 65 bạo kích, 76 kháng bạo kích, 77 hút máu, 111 EXP, 120..124 kháng 5 hệ, 129 chuyển ST thành HP, 132 hoá giải kỹ năng pet | `Item.createOptionMatNa / createOptionCanh / createAttributeItemCoat` | `Char.getPcBoQuaThuMatNa`, `haveBaoKich`, `hutmau`, `ExpLauDai.buffPhanTram`. **120..124 không nơi nào đọc** |
| Lan (kiếm hiệp) | 66..75, 85..92, 127 | đá thuộc tính (`doAddNewAttribute`), áo thời trang | `CharTrangBi.checkNewEffectItem`: độc lan trừ **½ HP** 8 mục tiêu xung quanh, sét lan 15 mục tiêu |
| Thời trang cầm tay | 115..117 sát thương chuẩn / tăng chỉ số đồ cầm tay | `createAttributeVuKhiThoiTrang`, tinh luyện | `CharChiSo.attackDam` (ST theo % HP mục tiêu, tới 24,1% mỗi đòn) |
| Thú cưng | 99..114 | `Item.doAddOptionPet*` | `data.Pet` (getter theo loại thú cưng). 99..101 **không nơi nào đọc** |
| Cải trang | 134..140 | `setAttributeByBatGioi / SaTang` | chỉ để hiện; hiệu ứng code cứng theo cấp cải trang trong `CharChiSo` |
| Bảo vật huyền thoại (dòng ẩn) | 141..168, 180..186 | `ThoRenBiAn` (10 bậc, `HIDDEN_STAT_TABLE`) | `Char.getThanKhiRaw` → `CharChiSo.applyThanKhiOnHit`, `subDamThanKhi`, `calculateAttrib` (hồi chiêu), `getThanKhiSpeedBonus` |
| Nguyên liệu bảo vật | 169..179 | `ThoRenBiAn` | không phải chỉ số |
| Nội bộ | 7..9 (mã hình thời trang), 42, 125 giá lượng, 126 số lượng | — | — |
| Không nơi nào đọc | 81, 83, 84 (`tyleRotItem`/`tylerotXu` được cộng nhưng không ai đọc), 93..98, 128, 130, 131, 133 | — | — |

Các nguồn chỉ số **ngoài** dòng thuộc tính (đều đi qua điểm kẹp ở mục 3):

- **Thú cưng**: điểm `newAtb` trên món thú cưng (byte), `percent[idTemplate]` (ST %, thủ %, EXP, bạo kích, né), kỹ năng nội tại
  (cường tráng +HP%, đoạt mệnh +ST%, liên kích +ST chí mạng, chữa trị % HP mỗi nhịp, thiên nhãn né...).
- **Thú cưỡi** (`Animal.att[0..11]`, `attUp`): % thủ, % ST, HP/MP phẳng, điểm, hấp thụ `att[9]`, chặn toàn phần `att[10]`
  (đòn còn 1), chí mạng `att[11]`, kháng ngũ hành theo dòng kẻ đánh, đủ bộ thú cưỡi +8/+12%.
- **Skin thú cưỡi** (`SkinThuCuoi`): HP/MP phẳng, % thủ, chặn / hấp thụ / phản sát thương (tới 80%), xuyên giáp, nộ.
- **Thời trang / cải trang / cánh / mặt nạ** (`ModelWearing`): % tấn công / thủ, HP/MP phẳng, bạo kích, ánh sáng / bóng tối
  (+5.000..10.000 ST phẳng, 20%), hút máu +100/đòn, biến người tuyết.
- **Bộ**: kích bộ hệ (28..32), đủ bộ +10/+13/+15 (`fullSet10/13/15`), vợ chồng +2..7% thủ, VIP, chức vụ, Lân (+5 lửa lan, +2% ST,
  hấp thụ 3%), tứ bình (`tubinh`) cộng phẳng.
- **Đồ giữ / vật phẩm dùng** (`VatPhamDungLauDai`): chí mạng, tấn công %, X Phòng thủ (giảm tới 80%), hút %, sống sót.
- **Danh hiệu**: chỉ hiệu ứng hình, không có chỉ số (đã soát `currentDanhHieu`).

### 1.3 Nâng cấp, ngọc, tinh luyện, dòng ẩn

| Hệ thống | Tác động | Giới hạn trước |
|---|---|---|
| Nâng cấp dài hạn +1..+15 (`NangCapLauDaiGame.doiChiSo`) | `atb[0..9]` × (1 + 3%/cấp tới +10, 5%/cấp tới +15) = **×1,55** ở +15 | không; ép "+1 mỗi cấp" làm chỉ số nhỏ trôi (mục 1.5) |
| Đủ bộ nâng cấp | +8% (6 món +15), +12% (10 món +15) tấn công/thủ/HP | không, cộng trên ×1,55 |
| Khảm cũ (`abilityKham`) | điểm + trạng thái | tổng `byte` (tràn) |
| Ngọc hệ (thiết kế 3.3) | +1%/cấp, ≤ 10% | **chưa có code** (bảng `data_noi_dung_ngoc_he` trạng thái `can_code`) |
| Tinh luyện | chỉ bảo vật cầm tay thời trang (`TinhLuyen`, `otherAtt[55]` ST đồ cầm tay ‰) | trần cũ theo bảng |
| Dòng ẩn bảo vật huyền thoại | 29 loại dòng, bậc 10: ST chí mạng +25%, ST theo HP 6%, ST boss +25%, ST quái +50%, giảm ST boss / người chơi 20%, kháng 1 hệ 20%, kháng tất cả 10%, hút HP / PP 10%, né +20%, phản +20%, giảm hồi chiêu 20% | `Char.TK_TRAN_GIAM_ST` 80% (chỉ dòng giảm ST) |

### 1.4 Giá trị lớn nhất trong dữ liệu

`data_item` (mẫu, live 18/10/2026):

| Ô | Chỉ số chính, cấp 1 → 98 | Vượt ngân sách |
|---|---|---|
| Khăn quàng (0) | thủ 10 → 105 | 268..275 (khung chế tạo bạc/vàng, bán ở danh mục): thủ 75–90 ở cấp 20–35 (đồ thường 31–50), cao hơn cả bạch kim 55. 841..847 (Áo Swampert/Emboar): né / chính xác **5.000** |
| Đồ cầm tay (3..7) | tấn công 20–90 → 429–511 | **214..263** (colortype 4, giá 10–80): tấn công +55..75% so với đồ thường cùng cấp + thủ/né/chí mạng/HP 10 mỗi dòng |
| Ruy băng (19) | thủ 32 → 226 | 608, 616, 742, 750, 795 (sự kiện): thủ **170** ở cấp 1 (= ruy băng cấp 70) |
| Phụ kiện (8..12) | đúng chuẩn | danh mục Phiến đá chuyên gia HP 15 > tinh anh 13 (bậc cũ mạnh hơn bậc mới) |

Đồ thời trang (`atb9=1`): `atb4..8` là **số hiệu hình**, không phải chỉ số. Hiệu ứng thật đọc qua `getInfoAtbItem`: `atb0` → +% tấn công,
`atb1` → +% thủ, `atb2/atb3` ÷1000 → HP/MP (thường +10%, +3.000–5.000).

Đồ trong túi người chơi (`tob_char` equip/inven/bag/tuido, 1.003 món, máy thử): chỉ có đồ cơ bản (tấn công ≤ 90, thủ ≤ 27), không có
dòng thêm nào.

Giá trị code sinh ra (lớn nhất): sát thương chuẩn bảo vật cầm tay 241‰ HP mục tiêu, tỉ lệ 290‰. Dòng ẩn: xem 1.3. Phản sát thương
skin 80%. Bạo kích VIP 3 +30‰. Liên kích thú cưng +15%. Chữa trị thú cưng 1% HP + phẳng mỗi nhịp.

### 1.5 Vấn đề phát hiện

1. **Trùng nghĩa**: Kháng Lửa/Nước/Cỏ/Thép/Đất ở cả 120..124 (phần nghìn, mặt nạ, không ai đọc) và 152..156 (phần trăm, bảo vật).
   "Né tránh" 2 và 131, "Sức khoẻ" 5 và 100, "Trí mạng" 140 và 4 / 40, "Hấp thụ sát thương" 81 / 136 / 210, "Tiềm năng" 138 / 205.
   Đã gom: cùng tên, ghi rõ nơi lưu, dòng không ai đọc thì ngừng.
2. **Không dùng**: 26, 27, 81, 83, 84, 93..101, 128, 130, 131, 133 không có code đọc. `tyleRotItem`/`tylerotXu` được cộng nhưng không
   ai đọc. 120..124 và 41 cũng vậy, nay đã nối vào (mục 2).
3. **Di sản kiếm hiệp**: lan sét/độc/băng/lửa (độc lan trừ ½ HP 8 mục tiêu), hoá người tuyết, hắc ám/ánh sáng, hoảng sợ, ngũ hành
   theo **dòng** (Kim = dòng 0...). Kháng theo dòng nghĩa là kháng Charmander chứ không phải kháng hệ Lửa.
4. **5 hệ cũ / 18 hệ**: chỉ có dòng cho Lửa/Nước/Cỏ/Thép/Đất. 13 hệ còn lại không có cách kháng hay tăng. Phiến đá hệ ("chọn 1 trong
   18 hệ") chỉ có trong danh mục, chưa có code.
5. **Cộng dồn không trần**: hấp thụ (`checkHapthuSatThuong` cộng thú cưỡi + skin + Lân, không trần), chặn toàn phần của thú cưỡi,
   khảm trạng thái (11 món cộng thẳng), % tấn công nhân dồn hơn 10 nguồn, chí mạng PvE không trần, liên kích cộng sau chí mạng, ST
   theo % HP mục tiêu với boss.
6. **% / phẳng lẫn lộn**: 14..25 in "x%" nhưng code so với 1000 (phần nghìn). 33/34 in "Tăng HP: 2" nhưng thực là +2.000.
   77 "hút máu" phẳng 20–55 HP (nhỏ dần vô nghĩa khi HP lớn). 160 "tăng tốc độ đánh" thực là % giảm hồi chiêu. Tên dòng `newAtb`
   37..40 trên món **thú cưng** nghĩa khác hẳn (37 = sức mạnh thú cưng), client hiện sai tên. Chưa sửa được vì một id chỉ có một tên.
7. **PvP hút HP** tính trên sát thương trước khi ×0,30: hút thật gấp ~3,3 lần con số ghi.
8. **Nâng cấp trôi**: `doiChiSo` ép tăng ít nhất 1 mỗi cấp rồi đoán lại "gốc" từ số đã ép. Chỉ số nhỏ (chí mạng 10, thủ 7) lên +15
   thành 25 / 22 thay vì 16 / 11.
9. **Danh mục trỏ nhầm**: khăn quàng bạc/vàng trỏ vào khung chế tạo 268/272 (thủ 75/85 > bạch kim 55). Phiến đá chuyên gia
   HP 15 > tinh anh 13.
10. **Tên / mô tả**: "chỉ số 7/8/9", "chưa dùng", mô tả có ghi chú nội bộ ("max 3%, tong vuot 10 se =10", "skin thu cuoi: Phuc Thu
    +HP", "otheratt"), `namein` tiếng Indonesia.
11. **Tên đồ thời trang** chứa ghi chú hiệu ứng lan ("Áo Banette\nTăng100% băng, sét, độc, lửa lan", "Giáp rồng-...-Tỷ lệ xh:15%").
    Khi bật `sv.lau_dai` hiệu ứng lan đã ngừng nên ghi chú sai. **Chưa sửa** (tên vật phẩm do `pokemon_doi_ten_vat_pham.sql` quản,
    cần chủ game chốt tên). Danh sách id: 609, 610, 620..623, 671..674, 723..726, 736, 737, 740, 741, 743, 744, 754..757, 788, 789,
    791, 792, 797..801, 833, 834, 839, 888, 889.
12. `Char.newAtb` / `abilityKham` là tổng kiểu `byte`: hơn 127 thì tràn âm. Khi bật, `ChiSoTrangBiGame.capNhat` cộng bằng `int`
    cho các dòng nó dùng. Hai biến cũ vẫn giữ nguyên cho đường cũ.

## 2. Mô hình chỉ số Pokémon (giữ id, đổi nghĩa)

Nguồn: `ChiSoTrangBi.BANG`. Trạng thái: **dùng** (có tác dụng), **hiện** (dòng mô tả hiệu ứng cố định của đồ đặc biệt), **nội bộ**
(không phải chỉ số, không bao giờ hiện), **ngừng** (không tác dụng khi bật, bị ẩn khỏi mô tả món).

### 2.1 Nhóm chính

| Chỉ số | Id | Đơn vị | Ghi chú |
|---|---|---|---|
| HP | 5, 33 (mỗi điểm +1.000), 139/200 (hiện) | điểm | 5 trên Phiến đá nhân theo dòng Pokémon |
| PP (MP) | 34 (mỗi điểm +1.000), 201 | điểm | |
| Tấn công / Tấn công % | 0, 158 / 58, 30, 144 | điểm / % | |
| Tấn công đặc biệt | 159 (cộng thẳng), 145 (%) | | đường "ma pháp" cũ = đặc biệt |
| Phòng thủ / % | 1, 151, 157 / 60, 64, 88 | | |
| Phòng thủ đặc biệt / % | 6 / 59, 63 | | |
| Tốc độ đánh | 160 | % giảm hồi chiêu | trần 33% = +50% tốc đánh |
| Tốc độ chạy | 161, 137 (hiện) | +1 | trần +30% tốc cơ bản |
| Chí mạng | 4, 40 (điểm, 3 = 1%), 65 "Chí mạng thêm" (phần nghìn, bạo kích cũ) | | gộp một lần quay |
| ST chí mạng % | 41 (%), 141 (phần nghìn) | | trên nền ×1,5, tối đa ×2,0 |
| Chính xác / Né tránh | 3 / 2 (điểm), 183 (%) | | |
| Hút HP % | 77 (phần nghìn, trước là HP phẳng), 129, 182 | | gộp, trần 10% / 5% |
| Hút PP % | 181 | | |
| Hồi HP / PP mỗi 5 giây | 78 / 79 | điểm | trần 1% tối đa mỗi nhịp |
| Xuyên giáp | 31 (tỉ lệ bỏ qua thủ, bộ hệ), 146/147 (xuyên thủ phần nghìn), 61/62 (bỏ qua thủ %, mặt nạ) | | trần 30% |
| Giảm ST nhận % | 28/29 (vật lý/đặc biệt), 150 (từ boss), 180 (từ người chơi) | | gộp một lớp, trần 50% / 40% |
| Phản sát thương | 32, 185 | % | trần 15% / 10% |
| ST lên boss / quái | 148 / 149 | phần nghìn | trần 20% |
| ST theo % HP mục tiêu | 115 + 117 (tỉ lệ), 142, 143 (PP) | phần nghìn | trần 1% (boss 0,2%), ≤ đòn gốc |
| EXP | 111 | % | trang bị + pet + thời trang ≤ 30% |

### 2.2 Trạng thái (bỏng / độc / tê liệt / ngủ / đóng băng)

| Cũ (khảm) | Mới | Id gây / kháng |
|---|---|---|
| Gây mù / Kháng gây mù | Gây ngủ / Kháng ngủ | 14 / 22 |
| Đóng băng / Kháng đóng băng | Gây đóng băng / Kháng đóng băng | 15 / 23 |
| Trúng độc / Kháng trúng độc | Gây trúng độc / Kháng trúng độc | 16 / 21 |
| Gây choáng / Kháng gây choáng | Gây tê liệt / Kháng tê liệt | 17 / 24 |
| Hoá thạch / Kháng hoá thạch | Gây bỏng / Kháng bỏng | 18 / 25 |
| Giảm tốc / Kháng giảm tốc | ngừng | 19 / 20 |

Khi bật, `Char.buffSkillKham` chuyển sang `ChiSoTrangBiGame.khamTrangThai`: tỉ lệ mỗi trạng thái giảm dần, trần 15% (PvP 7,5%), trừ
kháng của mục tiêu (trần 50%), mỗi đòn tối đa một trạng thái, rồi áp qua `TrangThaiGame.gayTuTrangBi`. Hàm này dùng chung luật
`TrangThaiPokemon`: miễn theo hệ, PvP ×0,5 thời lượng, boss 30% và không ngủ/đóng băng. Đơn vị đổi sang phần nghìn (ispercent 2),
khớp code. Kỹ năng thú cưng 102..114 giữ cơ chế cũ, chỉ đổi tên ("... (thú cưng)").

### 2.3 Hệ: 5 hệ cũ + cơ chế gọn cho 13 hệ còn lại

- 5 hệ cũ ánh xạ thẳng: Lửa 120 / 155 / 166, Nước 121 / 154 / 165, Cỏ 122 / 153 / 164, Thép 123 / 152 / 163, Đất 124 / 156 / 167
  (kháng phần nghìn / kháng % / ST %). 186 = kháng mọi hệ.
- **13 hệ còn lại**: hai id không ai dùng được đổi nghĩa thành dòng **theo hệ của phiến đá**: 93 "ST theo hệ phiến đá", 94 "Kháng theo
  hệ phiến đá". Giá trị lưu `hệ × 1000 + phần nghìn` (hệ 0..17 theo `HePokemon`, ví dụ Băng 5%: `5050`). Chỉ phiến đá hệ (ô 12)
  được tính. Khi bật, tên món tự thêm " (hệ X)" và mô tả hiện đúng phần trăm (`ChiSoTrangBiGame.locHienThi` / `tenHePhienDa`).
- Kháng tính theo **hệ chiêu đang đánh** (`Char.lauDaiHe`, PvP) hoặc hệ 1 của quái (quái đánh người), không còn theo dòng. Trần 60%.
- ST hệ (163..167 + 93) được đưa vào tham số `doHePhanTram` của `SatThuongLauDai.tinh`, trước đây luôn truyền 0. Trần 20% theo
  THIẾT KẾ 3.4. Phần "đồ cầm tay cùng hệ +10..20%" của danh mục **chưa nối**: nối vào sẽ làm TTK đã hiệu chỉnh ngắn đi 10–20%. Việc
  này cần chủ game quyết cùng lúc hiệu chỉnh lại HP quái.

### 2.4 Ngừng / ẩn

Lan 66..74, 85..92, 127. Biến người tuyết 75. 26, 27, 81, 83, 84, 95..101, 128, 130, 131, 133. Khi bật, các dòng này không có tác dụng
và bị lọc khỏi mô tả món (`ChiSoTrangBiGame.locHienThi`, gắn vào `Item.getInfoAtbItem` / `getInfoAtbItemWearing`). Lan thì dừng hẳn ở
`CharTrangBi.checkNewEffectItem`. Biến người tuyết dừng ở `CharChiSo.attackDam`. Nội bộ 7..9, 42, 125, 126 cũng bị lọc.

Rơi đồ / rơi xu từ trang bị (27, 83, 84) chưa bao giờ có tác dụng nên **ngừng** chứ không nối vào. Rơi xu trong thiết kế dài hạn chỉ
đến từ năng lượng và sự kiện, nối thêm sẽ làm tăng cung tiền. Trần sẵn có nếu sau này cần: `GioiHanChiSo.roiDo` 20%, `roiXu` 10%.

### 2.5 Tên / mô tả

198 tên tiếng Việt, người chơi đọc được. Mô tả không còn ghi chú nội bộ. `namein` bị xoá; server Indo thiếu `namein` sẽ dùng tên
tiếng Việt (`Database.loadNameAttributeItem`). Kiểm bằng test: không còn "chưa dùng", "chỉ số 7..9", mô tả không chứa "max N",
"otheratt", "skin thu cuoi".

## 3. Luật chống lạm phát (server, khi bật)

### 3.1 Giảm dần

`giamDan(x, gối, trần)`: x ≤ gối thì giữ nguyên. Trên gối: `gối + dư × (trần − gối) / (dư + trần − gối)`. Hàm đơn điệu tăng, liên tục,
độ dốc bằng 1 tại gối và **không bao giờ chạm trần**. Cộng thêm chỉ số luôn có ích một chút, nhưng chồng nhiều nguồn thì lợi ích giảm dần.

| Chỉ số | Gối → trần PvE | PvP | Điểm áp |
|---|---|---|---|
| Tỉ lệ chí mạng (điểm/300 + bạo kích + đồ giữ + buff) | 25% → 40% | 20% → 30%, trừ "Kháng chí mạng" 76 | `Char.havecrit` (một lần quay), 4 khối PvP (`giuChiMangPvp`) |
| ST chí mạng cộng thêm (141 + 41 + liên kích) | +30% → +50% (đòn ≤ ×2,0) | ×1,5 phẳng | `Char.tkNhanChiMang` |
| Né tránh | 20% → 30% | 15% → 20% | `CharChiSo.attackDam` |
| Hút HP gộp (77, 129, 182, đồ giữ, thời trang, thú cưng) | 6% → 10% | 3% → 5% trên ST thật | `ChiSoTrangBiGame.hutSauDon` |
| Giảm ST thụ động (một lớp) | 35% → 50% | 30% → 40% | `checkHapthuSatThuong`, `subDam`, `trenDon` |
| Kháng một hệ | 40% → 60% | | `trenDon`, `subDam(Monster)` |
| Gây trạng thái / kháng | 10% → 15% / 30% → 50% | ×0,5 | `khamTrangThai` |
| Xuyên giáp (tỉ lệ bỏ qua thủ) | 20% → 30% | | `Char.getBuffEffAttack` |

### 3.2 Trần cứng

| Chỉ số | Trần |
|---|---|
| Phản đòn | 15%, PvP 10% (tính trên ST thật) |
| Tốc độ đánh | hồi chiêu −33% (= +50%); tắt cờ thì trần cũ 50% |
| Tốc độ chạy | +30% tốc cơ bản (5 → 6). Thú cưỡi 6 đã chạm trần, bảo vật +1 không cộng thêm |
| ST lên boss / quái | 20% / 20% |
| ST theo % HP mục tiêu | 1% (boss 0,2%), không quá đòn gốc. Áp cho bảo vật cầm tay, skin Kim Cang Long, dòng ẩn 142/143 |
| Ánh sáng / bóng tối (thời trang) | ≤ 30% đòn |
| Hồi HP/PP mỗi nhịp 5 giây | 1% tối đa |
| Gây chậm / cấm chiêu (80, 82, 162) | 5% (162 PvP 3%) |
| EXP từ trang bị + thú cưng + thời trang + cải trang + đồ giữ | +30%. Trần chung +100% (`CapDoLauDai`) giữ nguyên, không cộng dồn quá trần |
| Hút HP lên boss / Gym / boss thế giới / tinh anh | ×10% (tức ≤ 1%). Mỗi đòn ≤ 0,2% HP tối đa |
| Tổng % cộng thêm tấn công / thủ / HP ngoài đồ | +50% trên gốc (nhân vật + đồ đã kẹp + đồ thú cưỡi + tứ bình). Giai đoạn tiến hoá, tinh thông, hệ sinh thái tính ngoài (đã có trần riêng) |
| Thưởng bộ (+10/+13/+15 đủ bộ, đủ bộ thú cưỡi) | 5% |
| Liên kích thú cưng | không cộng riêng nữa, gộp vào ST chí mạng |

### 3.3 Sàn sát thương (chống bất tử)

Mọi lớp giảm ở phía bị đánh đi qua `ChiSoTrangBiGame.giam(c, dam, phầnNghìn)`: khác hệ, giảm ST người chơi / boss, bộ hệ, hấp thụ,
chặn toàn phần thú cưỡi, giảm theo hệ thú cưỡi, Pha triển, X Phòng thủ. Hàm này giữ phần còn lại của đòn đang xử lý
(`Char.ldConLai`, đặt lại ở đầu đòn hoặc sau 40 ms). Sau mọi lớp, đòn còn **ít nhất 40%** (`chi_so.san_sat_thuong`). Tổng giảm vì
thế ≤ 60%, bằng trần kháng một hệ. Hai đòn đến cùng lúc chỉ làm sàn chặt hơn, không bao giờ lỏng hơn.

### 3.4 Ngân sách một món

`NganSachTrangBi`: chuẩn = chỉ số món Trắng +0 trong danh mục 11 bậc (đơn vị engine), nội suy theo cấp. Hai dòng danh mục sai
(khăn quàng bạc/vàng, Phiến đá HP) dùng giá trị đã sửa.

- Trần một món = `ceil(chuẩn × 1,30 × hệ số nâng cấp) + 2`. 1,30 phủ `Map.rand10` (+0..25%). Dòng ngoài ô:
  `(3 + cấp/10) × nâng cấp`.
- Đồ **bậc cũ** (bậc của món < bậc theo cấp nhân vật): thêm trần `1,30 × 1,25 × chuẩn bậc hiện tại + 2`. Đồ bậc cũ nâng tối đa
  không bao giờ hơn đồ bậc hiện tại +0 cùng độ ngẫu nhiên quá 30%, tức ngang đồ mới khoảng +10.
- Áp **lúc tính** cho đồ đang mặc (`ChiSoTrangBiGame.atb` trong 6 vòng tính chỉ số) nên đồ cũ trong túi người chơi cũng được kẹp mà
  không sửa dữ liệu của họ. Mô tả món hiện đúng số đã kẹp. Bỏ qua: thời trang (`atb9≠0`), đồ thú cưỡi 14..18 (thang riêng, nhỏ).
- Mẫu `data_item` được kẹp bằng `LEAST` trong `sql/pokemon_chi_so.sql` (chỉ giảm, theo quy tắc (ô, cấp) nên idempotent).
- Nâng cấp: `doiChiSo` không ép +1 nữa, chỉ giữ không giảm khi lên cấp.

### 3.5 Pet, thú cưỡi, skin, danh hiệu không lách trần

Trần đặt ở **điểm cuối** (lần quay chí mạng, né, hút, từng lớp giảm, tổng % cộng thêm), không đặt ở từng nguồn. Vì vậy chí mạng thú
cưỡi `att[11]`, bạo kích thú cưng / thời trang / skin, né thiên nhãn, hấp thụ thú cưỡi / skin / Lân, phản sát thương skin, % thủ / ST
của thú cưng, thú cưỡi, thời trang đều chịu cùng trần. Danh hiệu không có chỉ số. Kiểm từng điểm gọi bằng `KiemChiSo` (chuỗi trong code).

### 3.6 Cấu hình (áp dụng ngay, nhóm "Chỉ số & chống lạm phát" trên web quản trị)

`chi_so.bat`, `chi_so.kep_trang_bi`, `chi_so.tran_chi_mang` (400), `chi_so.tran_st_chi_mang` (500), `chi_so.tran_ne` (300),
`chi_so.tran_hut` (100), `chi_so.tran_hut_pvp` (50), `chi_so.tran_giam_st` (500), `chi_so.tran_khang_he` (600),
`chi_so.san_sat_thuong` (400), `chi_so.tran_phan_tram_cong` (500). Đơn vị phần nghìn. Gối giảm dần co giãn theo trần mới. Đăng ký ở
`tools/cauhinh/danh_muc.json`.

## 4. Kiểm chứng

- `tools/pokemon/MoPhongChiSo.java` dùng mô hình của bộ sinh nội dung (`quai_de_xuat`, Gym 105 giây, boss 10 phút / 20 người). Nó
  tính TTK theo cấp × dòng × +0/+6/+10/+15 với ba kịch bản: thường, tối đa trước trần, tối đa sau trần. Ngoài ra: tinh anh / Gym / boss,
  hồi phục so với sát thương nhận (tinh anh, Gym, boss, 3 quái cùng lúc), PvP, tỉ lệ bậc đồ, quy tắc THIẾT KẾ 3.6 trên danh mục, và
  kiểm toàn bộ `data_item` của `sql/htbb_day_du.sql` trước / sau migration. Kết quả ở mục 0 và `docs/pokemon/mo_phong_chi_so.txt`.
- Quy tắc "bậc mới +0 ≤ 1,10 × bậc cũ +15" (tổng chỉ số gốc + 11 ô): lớn nhất 1,091. Tính một món ở đơn vị engine thì bậc sơ cấp →
  đồng chênh tới 2,58 lần (chênh 13 cấp), nằm trong thiết kế vì quy tắc tính trên tổng.
- `test/KiemChiSo.java`: trần, giảm dần, sàn, PvP, ngân sách, ánh xạ 5 hệ + phiến đá, bảng 198 dòng, SQL khớp nguồn, điểm gọi
  trong code, mô phỏng 12 mục tiêu.

## 5. Triển khai

- `sql/pokemon_chi_so.sql`: chạy sau nội dung + vật phẩm, trước thuật ngữ, trong `tools/pokemon/xuat_sql_day_du.sh`. Sinh lại:
  `javac -cp <lớp src> -d <ra> tools/pokemon/SinhSqlChiSo.java && java -cp <ra>:<lớp src> SinhSqlChiSo`.
  **Chưa chạy lên CSDL NovaDB** đang chạy vì file sửa dữ liệu, không chỉ thêm. Áp tay: `mysql htbb_db < sql/pokemon_chi_so.sql`
  (idempotent), rồi khởi động lại server để nạp lại tên chỉ số (gói 25). `sql/htbb_day_du.sql` cần sinh lại bằng script.
- Client J2ME / Android / PC lấy tên chỉ số từ server (gói 25), nên đổi bảng là đổi tên ở mọi client. Chuỗi cứng còn lại trong
  client (`ItemInInventory.attrName`, `Res.newNameAtbItem`, bảng thông tin nhân vật) đã đổi cho khớp.
- Web: Cẩm nang → "Chỉ số" (HTBB-Web `app/pages/cam-nang`), chỉ nội dung người chơi cần.

## 6. Còn lại

- Đổi tên đồ thời trang còn ghi chú lan (1.5 mục 11), cần chủ game duyệt.
- Nối "đồ cầm tay cùng hệ +10..20%" và ngọc hệ (thiết kế 3.3) cùng lúc hiệu chỉnh lại HP quái (2.3).
- Phiến đá hệ: cơ chế 93/94 đã chạy (tính + hiện tên hệ), nhưng **chưa có đường ghi** giá trị (NPC chọn hệ / ngẫu nhiên khi
  tạo món). Cần thêm khi làm danh mục Phiến đá (gợi ý: lúc tạo món ghi `ChiSoTrangBi.maDongPhienDa(he, 50)` vào `otherAtt[32]`
  hoặc `otherAtt[33]`).
- Tên dòng `newAtb` trên món thú cưng (1.5 mục 6) cần client tách tên theo loại món.
- Các nhánh cũ chỉ chạy khi tắt cờ (lan, ngũ hành theo dòng, chí mạng ×2/×4) giữ nguyên để so sánh / quay lui.
