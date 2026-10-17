# Duyệt chữ: đợt rà soát "chất Pokémon" (2026-10-16)

Theo `docs/CODING_CONVENTION.md` §10, chữ người chơi thấy phải được chủ dự án duyệt. Dưới đây
là **các cách gọi mới** của đợt này, xếp theo nhóm (cũ → mới). Muốn đổi cách nào thì sửa đúng
một chỗ ghi ở cột "Sửa ở", chạy lại tool, không sửa tay file SQL/CSV/C# sinh ra.

- **Tự điển chung** (`src/com/htbb/pokemon/ChuPokemon.java`, mảng `DOI`): áp cho chữ server gửi
  và **mọi** chữ client vẽ. Sửa xong chạy `python3 -I tools/pokemon/sinh_sql_chu.py`.
- **Theo dòng CSDL** (`tools/pokemon/sinh_sql_chu.py`: `QUEST`, `DAU_ITEM`, `DAU_SHOP`;
  `tools/pokemon/sinh_sql_vat_pham.py`: `BAC`, `GIU_TEN`).
- Đối chiếu từng ô đã đổi: `docs/pokemon/bang_chu.csv` (ô CSDL + toàn bộ cặp tự điển).
- Mọi tên cũ dưới đây đã thêm vào `TU_CAM`, `quet_tu_cam.py --kiem` đang báo 0.

Ký hiệu: ✔ = đề xuất mạnh, ? = phân vân, chủ dự án nên xem kỹ.

## 1. Xưng hô, lời thoại

| Cũ | Mới | |
|---|---|---|
| ngươi | cậu | ✔ (NPC vẫn xưng "ta") |
| giết (quái) | hạ | ✔ |
| yêu ma / yêu ma quỷ quái / quái vật yêu ma | Pokémon hung dữ | ✔ |
| cương thi | Pokémon ma | |
| nội thương / nội kình / kình lực | vết thương / năng lượng / sức mạnh | |
| đấng anh hào, ẩn sỹ / anh em kết nghĩa / Hai Minh (lời ông nội lúc mở game) | nhà huấn luyện tài ba / bạn thân / Xatu | ? |
| anh hùng xuất thiếu niên | tuổi trẻ tài cao | |
| phu quân, phu nhân | bạn đời | ? |
| đồ sát | PK | ✔ |

## 2. Vận tiêu → giao hàng

vận tiêu → giao hàng; chuyến tiêu → chuyến giao hàng; nhận/trả/đổi tiêu → nhận/trả/đổi hàng;
màu tiêu → màu hàng; cướp tiêu → cướp hàng; Rương bảo tiêu → Rương giao hàng; Xa tiêu lệnh →
Vé xe giao hàng; Ngựa vận tiêu → Ponyta giao hàng. ✔

## 3. Chiếm thành, chức vụ vùng, chiến trường

| Cũ | Mới | |
|---|---|---|
| chiếm thành / vào thành / giữ thành / cổng thành | chiếm Gym / vào Gym / giữ Gym / cổng Gym | ? (cách gọi "Gym" cho thành) |
| thành Trấn Danh | Cao nguyên Indigo | ✔ (khớp tên map đã có) |
| Thành liên đấu N | Gym liên đấu N | ? |
| lôi đài | sàn đấu | ✔ |
| vua / đức vua | nhà vô địch vùng | ? |
| đại thần / bộ đầu (tướng quân đã là "chỉ huy") | cố vấn vùng / cảnh sát vùng | ? |
| phế chức, "Phế …" | bãi nhiệm | |
| quốc gia / quốc chiến | vùng / chiến vùng | ✔ |
| chiến trường chính tà / tống kim | chiến trường liên vùng | ✔ |
| cừu nhân / liên trảm | đối thủ / chuỗi thắng | ✔ |
| long trụ | trụ năng lượng | |
| lệnh bài, thẻ bài | thẻ | ✔ |
| Long/Lân/Quy/Phụng lệnh bài | Thẻ Rayquaza/Suicune/Kyogre/Ho-Oh | ? |
| Trấn yêu trận / Lệnh bài trấn yêu | Tháp Pokémon / Vé Tháp Pokémon | ? |
| hộ vệ / Hộ vệ lệnh | Pokémon hỗ trợ / Vé gọi Pokémon hỗ trợ | ? |

## 4. Vật phẩm, nguyên liệu

| Cũ | Mới | |
|---|---|---|
| Thần Khí | Bảo vật huyền thoại | ✔ |
| Quạt ba tiêu / Gậy như ý / Lưỡng nhận đao (3 thần khí, item 890–892) | Quạt lá Shiftry / Gậy xương Marowak / Lưỡi liềm Scyther | ? |
| Hỏa thạch | Nham thạch | |
| Ngọc huyền minh | Ngọc hoàng hôn | |
| Đá ngũ hợp, quả ngũ hợp | Đá cầu vồng, quả cầu vồng | |
| tiến giai | thăng bậc | |
| Tụ hồn đan | Kẹo Poké | ✔ |
| Tiên đan / Tiên dược | Cỏ hồi sinh / Kẹo kinh nghiệm | ✔ |
| Tiềm năng đan / Kỹ năng đan | Kẹo tiềm năng / Kẹo kỹ năng | |
| Huyết linh thảo | Kẹo hiếm | ? (công dụng: tiến hoá thú nuôi) |
| Huyết bồ đề | Bạc hà tính cách | ? (công dụng: đổi phẩm chất) |
| nhân sâm | Berry Razz vàng | ? |
| Hỗn độn / Hỗn nguyên / Hoá công / Đoạn cốt / Hoả dược | X Phòng thủ / X Tấn công / Thuốc săn trùm / Ether chiến trường / Bom lửa | ✔ (khớp tên thuốc đã có) |
| Thổ địa phù / Thiên lý mã / Xích thố | Vé bay Pidgeot / Ponyta / Ponyta lửa | ✔ |
| Rương bát bảo | Rương kho báu | |
| Nhẫn Bách niên giai lão / Vĩnh kết đồng tâm / Loan phụng hòa minh | Nhẫn Trái tim đồng / bạc / vàng | ? |
| Phù phu thê | Vảy trái tim | ? |
| Nguyệt Linh Trượng / Nguyệt ảnh | Gậy Ánh Trăng / Ánh Trăng | |
| Ám Khí | Đòn đánh lén | |
| Nhất…Ngũ phẩm | Hạng 1…5 | |
| Ngọc bội / Dây chuyền / Hộ uyển (tên ô trang bị) | Phiến đá hệ / Vòng cổ / Vòng chân | ✔ (khớp bảng 6.4) |
| Bút/Búa/Cung cấp độ, "giảm ST từ Bút/Búa/Cung", Đũa thần | Khăn lụa / Nước thần / Nam châm, Khăn lụa | ✔ |
| Bậc trang bị cấp 70 "đại sư" (`BAC`) | "tinh anh" | ? |

## 5. Bộ mảnh sưu tập

| Bộ cũ | Bộ mới | 4 mảnh |
|---|---|---|
| Tứ bình (mai, lan, cúc, trúc) | Bộ Berry | Oran, Pecha, Cheri, Rawst |
| Tứ nhạc (sáo, quạt, sênh, đàn) | Bộ Flute | Blue, Red, Yellow, White Flute |
| Tứ khoái (ngư, tiều, canh, độc) | Bộ huy hiệu | huy hiệu Cascade, Rainbow, Boulder, Marsh |
| Tứ linh (long, lân, quy, phụng) | Bộ huyền thoại | Rayquaza, Suicune, Kyogre, Ho-Oh |

Tất cả là ?: có thể đổi sang bộ khác (ví dụ 4 loại Poké Ball).

## 6. Thời trang, cải trang, skin, cánh

**Thay đổi cách làm (cần duyệt):** 95 đồ thời trang/cải trang/skin/thần khí trước đây bị
`sinh_sql_vat_pham.py` đổi thành tên chung ("Khăn quàng sơ cấp"…), mất cả tên lẫn dòng mô tả
công dụng. Nay chúng giữ tên gốc (vẫn `gender=0`) rồi đổi chữ qua tự điển. Danh sách id:
`GIU_TEN` trong `sinh_sql_vat_pham.py`.

| Cũ | Mới | Cũ | Mới |
|---|---|---|---|
| Lạc long quân / Âu cơ | Rayquaza / Ho-Oh | Sơn Tinh / Thuỷ Tinh | Groudon / Kyogre |
| Tiên nữ / Tinh Quân | Lunala / Solgaleo | Tiểu long nữ / Dương Quá | Latias / Latios |
| Thạch sanh | Lucario | Thầy đồ | Giáo sư |
| Yếm đào | Bellossom | Hằng Nga / Chú cuội | Clefairy / Clefable |
| Dracula | Darkrai | Hồng hài nhi | Magby |
| Freddy / Jason / Harry potter / Voldemort | Banette / Dusknoir / Alakazam / Giratina | Đầu bí ngô / Nón phù thủy | Đầu Pumpkaboo / Nón Mismagius |
| Hắc Lang / Bạch Mi / Thuý Vũ Hồng Sam / Thần nữ | Zoroark / Absol / Oricorio / Gardevoir | Mị Nương Công Chúa / Sơn Tinh Vương | Diancie / Groudon |
| Nghê Thường / Ngọc Nữ / Kim Đồng / Xích Cước / Lôi Công | Milotic / Minun / Plusle / Blaziken / Zapdos | Bá Thiên Hổ | Raikou |
| Miêu mắt xanh / Miêu quý tộc | Glameow / Persian | Mặt nạ ông địa / ngạ quỷ / ngạ khuyển / thi khiển / dạ xoa | Mặt nạ Snorlax / Haunter / Houndoom / Yamask / Gengar |
| Bát Giới / Sa Tăng (cải trang, áo, mảnh; kim cang/kim cô → vòng tay/vòng đầu) | Emboar / Swampert | Phi phong Khổng tước | Phi phong lông công |
| Skin Phúc Thú / Gấu Trúc / Tiên Lộc / Xích Dương / Chiến Lang / Kim Cang Long | Copperajah / Pangoro / Xerneas / Camerupt / Lycanroc / Dialga | Cánh Lam Băng / Bạch Linh / Xích Vũ / Hồng Vân / Tử ảnh | Cánh Articuno / Lugia / Moltres / Togekiss / Crobat |
| "Nội tại" (mô tả skin) | Đặc tính | Ngưu Lang / Chức Nữ (sự kiện) | Volbeat / Illumise |

Tên đồ ngắn trong CSDL được thêm chữ "Áo …" (vd shop "Freddy" → "Áo Banette") để khỏi nhầm
với tên một Pokémon: `DAU_ITEM`, `DAU_SHOP`.

## 7. Kiểu tóc → kiểu lông (Pokémon không có tóc)

tóc → lông; Đầu đinh → Lông đinh; Tóc bờm ngựa → Bờm Ponyta; Tóc tiên đồng → Lông Clefairy;
Tóc thư sinh → Lông gọn gàng; xuống/cưỡi ngựa → xuống thú cưỡi/cưỡi thú. ?

## 8. Thú cưỡi, pet gọi một mình

Tên thú cưỡi đứng một mình (trước chỉ đổi khi có "linh thú …" đi trước) theo `bang_thu.csv`:
Bạch cốt mã → Spectrier, Đương khang (băng giá) → Piloswine (Mamoswine), Đại Bạch Thử → Wooloo,
Mãnh hổ → Arcanine, Sói xám → Mightyena, Tiên hạc → Pidgeot, Bạch mã → Rapidash, Tuần lộc →
Stantler, Rồng băng → Dragonite, Phượng hoàng (các hệ) → Moltres/Articuno/Ho-Oh/Tropius/
Skarmory/Flygon, Hoả kỳ lân → Ponyta, Lân sư tử → Suicune. ✔

Pet kiểu cũ theo `bang_pet_cu.csv`: Lồng đèn xếp → Litwick, Thanh tước → Taillow, Cánh cam →
Ledyba, Dơi quỷ → Zubat, Tim bay → Luvdisc, Lồng đèn hoa sen → Lotad, Lồng đèn ngôi sao →
Staryu/Starmie, Pet Hoa tuyết → Cryogonal, Pet Giáp Ngọ → Ponyta, Pet Armadillo băng/sét/độc →
Sandshrew/Pachirisu/Nidoran, Lồng đèn cá chép → Magikarp, Chim cánh cụt → Piplup, Pet ất mùi /
Thỏ bay → Mareep, Ngôi sao băng → Jirachi, Lồng đèn trăng khuyết → Lunatone, Lồng đèn rồng xanh
→ Articuno, Khỉ vàng → Aipom, pet Bạch Thử → Sandshrew; Trứng cú/rồng/yêu tinh/dơi/đại bàng →
Trứng Hoothoot/Dratini/Cleffa/Zubat/Rufflet. ✔ ("Capybara" giữ nguyên vì là pet chủ dự án yêu cầu.)

## 9. Kỹ năng pet → chiêu Pokémon

Phá triền → Bảo vệ, Đoạt mệnh → Dồn sức, Thiên nhãn → Phân thân, Tiên khí → Màn bảo hộ,
Kim cang → Thu mình, Càn khôn → Màn ánh sáng, Cường thân → Tĩnh tâm, Hộ thể → Giáp sắt,
Thuấn ảnh → Hồi năng lượng, Phí huyết → Rễ bám, Yểm bùa → Nguyền rủa. (Giữ: Chữa trị, Cường
tráng, Kiên cố, Liên kích, Tăng kích.) ?

## 10. Chỉ số vật lý / ma pháp

Thủ vật (lý) → Phòng thủ; Thủ ma (pháp) → Phòng thủ đặc biệt; ma pháp → đặc biệt (st/tc/PT ma →
… đặc biệt); Kháng hoả/thuỷ/mộc/kim/thổ → Kháng Lửa/Nước/Cỏ/Thép/Đất. ✔

## 11. Danh hiệu

| Cũ | Mới | Cũ | Mới |
|---|---|---|---|
| Chí tôn | Tối thượng | Duy ngã độc tôn | Tứ Thiên Vương |
| Hảo hán | Người dũng cảm | Tông sư | Bậc thầy Pokémon |
| Đệ nhất | Số một | Nhất dương tiễn | Tia Mặt Trời |
| Tuyệt thế vô song | Có một không hai | Chiến Thần Bất Diệt | Huyền Thoại Bất Diệt |
| Long Tiễn Chấn Thiên | Rayquaza Chấn Thiên | Nghịch Thiên Nhi Hành | Kẻ Thách Thức Định Mệnh |
| Khai Thiên Thần Kiếm | Gươm Aegislash | Thiên Hạ Chí Tôn | Nhà Vô Địch Mọi Vùng |
| Vô Địch Thiên Hạ | Vô Địch Mọi Vùng | Độc Bá Nhất Phương | Bá Chủ Một Vùng |
| Tung Hoành Thiên Hạ | Tung Hoành Mọi Vùng | Tuyệt Đỉnh Cao Thủ | Tinh Anh Tuyệt Đỉnh |
| Nhất Tiễn Phá Thiên | Tia Chớp Phá Trời | Chân Mên Thiên Tử | Người Được Chọn |
| Cửu Huyền Thất Tổ | Truyền Nhân Huyền Thoại | Hủy Thiên Diệt Địa | Sức Mạnh Hủy Diệt |
| Quy Ân Giang Hồ | Lão Làng Pokémon | Thiên Hạ Đệ Nhất / Vô Song | Số Một / Vô Song Mọi Vùng |
| Quân Lâm Thiên Hạ | Chúa Tể Mọi Vùng | | |

Giữ nguyên: Anh hùng, Bá chủ, Đại gia, Huyền thoại, Uy phong, Vô địch, Kinh Tài Tuyệt Diễm, Nữ
Thần Tuyệt Sắc, Phong Vân Tùy Bút, Vinh Hoa Phú Qúy, Tuyệt Thế Giai Nhân, Độc Nhất Vô Nhị.

## 12. NPC, boss

Thần tài / Nhà Vua → Meowth; Mẹ hiền, Phù Thủy Dễ Thương → Gardevoir; Cây thông Noel →
Abomasnow; ông Già Noel → Delibird; Cây lì xì → Sudowoodo; Tả/Hữu thống lĩnh, Tả Phó Thống →
Lucario (cả ba cùng tên, ?); thống lĩnh → chỉ huy; NPC client Nhất…Ngũ giáp → Golem 1…5, Nhất…Ngũ
ngưu → Tauros 1…5, Bảo ngọc → Sableye, Giáp Sư → Bronzong, Bội Châu → Persian, Lộc Phát →
Meowth, Kim hoa → Kecleon, Lính gác → Arcanine gác cổng, ông nội → ông Snorlax, lão Địa chủ →
Persian; Thuồng luồng → Gyarados; "Đại bàng đã xuất hiện" → Zapdos; boss Người tuyết →
Abomasnow (theo bảng 6.2). Tên địa danh viết thường ("Đông sơn", "Sơn lâm", "Tiên du"…) nay
cũng đổi như bản viết hoa của `TenMapPokemon`.

## 13. Nhiệm vụ viết lại (`QUEST` trong `sinh_sql_chu.py`)

Tên: Giết sâu bọ → Đuổi Caterpie phá lúa; Cường thân kiện thể → Rèn luyện thể lực; Trừ gian
diệt ác → Dẹp loạn khu hoang dã; Hành hiệp trượng nghĩa → Giúp đỡ mọi người; Trừ hại cho dân →
Bảo vệ dân làng; Trừ khử yêu ma → Đẩy lùi Pokémon hung dữ; Móng Croconaw → Răng Croconaw; Tìm da
ếch → Nọc Croagunk; Tìm cựa gà → Đua sức với Dodrio; Tìm Litleo nấu cao → Hơi ấm của Litleo.

Lời thoại nhiệm vụ 37–46 viết lại (bản cũ lột da, nấu cao, ngâm rượu, ăn thịt chính Pokémon):
nay là đánh bại / nhặt răng, nọc, lông vũ rơi ra, mời Kricketune biểu diễn, Litleo sưởi ấm. Số
lượng và phần thưởng giữ nguyên. Ba câu cũ có chữ HOA có dấu ("Ở…", "Ừm") ở nhiệm vụ 1, 8 đã
viết lại ("Trong…", "Nơi…", "Tốt lắm").

## 14. Chưa làm, cần chủ dự án quyết

| Việc | Ghi chú |
|---|---|
| Menu chế đồ `MapCheTao.java` hiện "Bút", "Búa", "Cung" đứng một mình | "Cung" trùng chữ "cung cấp" nên tự điển không đổi được; cần sửa chuỗi trong file (đang do việc khác giữ) |
| Menu trứng thú cưỡi `MapDoMenu.java:4533` hiện "Machamp" | Chuỗi "Hắc ngưu" ở đó là thú cưỡi (Tauros) nhưng tự điển coi "Hắc ngưu" một mình là NPC thợ rèn |
| Tên ô trang bị client "Áo, Quần, Nón, Giày, Găng, Nhẫn" | Bảng 6.4 đã đổi tên đồ (Khăn quàng, Đai…) nhưng nhãn ô vẫn cũ; đổi bằng tự điển sẽ phá "Quần áo", "áo dài" |
| "Phi phong" (áo choàng thời trang) | Bảng 6.4 gọi loại này là "Ruy băng"; đồ thời trang vẫn để "Phi phong" |
| Sự kiện Việt (Tết, Trung thu, Giỗ Tổ, Vu Lan, bánh chưng, lì xì, câu đối) | Giữ nguyên: là lễ hội hiện đại, không phải kiếm hiệp |
| "Luyện kim dược", "Kiên cố", "Liên kích", "quái" | Giữ nguyên |
