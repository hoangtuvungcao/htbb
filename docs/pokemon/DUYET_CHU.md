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
| Trấn yêu trận / Lệnh bài trấn yêu | Mê cung / Vé Mê cung (18/10/2026, THUAT_NGU.md; trước là Tháp Pokémon) | ? |
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
| Huyết linh thảo | Kẹo thú cưng | đổi từ "Kẹo hiếm" để không trùng Kẹo hiếm EXP (1214) - tiến hoá thú nuôi |
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

## 15. Đợt copy-edit chữ mới (18/10/2026) - đã duyệt theo uỷ quyền

Chủ dự án uỷ quyền duyệt mọi chữ người chơi thấy MỚI (server, nhiệm vụ, nội dung, client, web) cho đợt rà soát này.
Các chữ dưới đây đã copy-edit và sửa thẳng trong mã nguồn/generator (dữ liệu sinh lại), trừ dòng ghi "Cần chủ server/luật
sư xem".

### 15.1. Bảng thuật ngữ chuẩn

> Bảng cũ → mới đầy đủ (mọi thuật ngữ kiếm hiệp, ngoại lệ, công cụ, test): `docs/pokemon/THUAT_NGU.md` (18/10/2026).

Áp cho mọi chữ người chơi thấy (server, client J2ME/Android/PC, web). Cột "Không dùng" là các biến
thể cần sửa về cột "Chuẩn" khi gặp trong chữ mới.

| Chuẩn | Nghĩa / cách dùng | Không dùng |
|---|---|---|
| Pokémon | luôn có dấu é, viết hoa P; số nhiều cũng "Pokémon" | Pokemon, pokemon, POKEMON, các Pokémons |
| Pokédex | | Pokedex, pokedex |
| hệ (hệ Lửa, hệ Nước…) | loại của Pokémon/chiêu; tên hệ viết hoa chữ đầu: Thường, Lửa, Nước, Điện, Cỏ, Băng, Giác đấu, Độc, Đất, Bay, Siêu linh, Bọ, Đá, Ma, Rồng, Bóng tối, Thép, Tiên | hệ Hoả/Thuỷ/Mộc/Kim/Thổ, ngũ hành, thuộc tính (khi nói về hệ) |
| khắc hệ | hệ này mạnh hơn hệ kia | khắc chế ngũ hành |
| chiêu | đòn/kỹ năng chiến đấu của Pokémon; tên chiêu dùng tên tiếng Anh chính thức (Thunderbolt, Ember…) hoặc tên Việt đã có trong bảng chiêu | kỹ năng (khi nói về chiêu), skill, võ công |
| kỹ năng sống | câu cá, đào, nấu, ấp trứng… (Hệ sinh thái) | kĩ năng |
| thể lực | điểm tiêu cho kỹ năng sống mỗi ngày ("thể lực sinh hoạt" chỉ dùng ở menu Hệ sinh thái) | sức lực, stamina |
| năng lượng | chỉ số năng lượng/thanh năng lượng chiến đấu, "năng lượng 100%" (thời gian x2 EXP) | nội lực |
| tiến hoá | | tiến hóa, evolve |
| cấp | cấp độ của Pokémon người chơi ("cấp 30") | level, lv, Lv. (trừ ô quá hẹp: "Cấp") |
| EXP | điểm kinh nghiệm (viết hoa cả 3 chữ) | exp, Exp, kinh nghiệm (dùng được trong văn kể) |
| lượng | tiền nạp, giao dịch được | Lượng (giữa câu), lương |
| lượng khoá | lượng không giao dịch được | lượng khóa, lượng khoá (viết hoa giữa câu), LK |
| xu | tiền trong game | Xu (giữa câu), vàng, bạc |
| giftcode | mã quà tặng; đầu câu "Giftcode" | gift code, Gift Code, mã quà (dùng được trong văn kể) |
| đội | nhóm người chơi lâu dài (trước là bang) | bang, bang hội, clan |
| nhóm | tổ đội tạm thời đi cùng | party |
| Gym, trưởng Gym, huy hiệu | | gym, GYM, Huy Hiệu (giữa câu) |
| Sổ tay huấn luyện | tên tính năng (viết hoa chữ đầu) | Sổ Tay Huấn Luyện, sổ tay (khi nói tên tính năng) |
| Hầm ngục bí ẩn | tên tính năng (viết kiểu câu: font J2ME và kiểm tra server cấm chữ HOA có dấu như "Ẩ") | Hầm Ngục Bí Ẩn, hầm ngục bí ẩn (đầu câu) |
| Hệ sinh thái, Túi sinh thái | tên menu/tính năng | Sinh Thái, túi Sinh thái |
| Thang PvP | | PVP, pvp |
| boss thế giới | | Boss Thế Giới (giữa câu), world boss |
| nhiệm vụ chính / nhiệm vụ phụ / nhiệm vụ hằng ngày | | quest, nv |
| sự kiện | | event |
| hằng ngày, hằng tuần | | hàng ngày, hàng tuần |
| nhà huấn luyện | người/Pokémon luyện Pokémon | huấn luyện viên, trainer |
| trưởng Gym | NPC/boss đứng đầu Gym; tên boss: "Trưởng Gym Pewter: Golem" | thủ lĩnh, Thủ lĩnh Gym |
| Chiếm Gym, sàn đấu | tính năng chiếm thành cũ / lôi đài cũ | chiếm thành, lôi đài |
| thầy / trò (Sư phạm) | kèm cấp; điều kiện "cùng nhóm" | sư phụ, đệ tử |
| Thẻ tháng, Vé mùa (nhánh thưởng cao cấp / miễn phí), Cửa hàng lượng | tên tính năng nạp | track, battle pass |
| Tủ đồ trang trí, Chợ trang trí | | |
| quái tinh anh | | Alpha (trong chữ người chơi) |
| "đổi lối", "bố cục hôm nay" (Hầm ngục bí ẩn) | bản đồ hầm đổi mỗi ngày | hạt giống, seed |
| cậu / bạn | NPC gọi người chơi; NPC tự xưng "ta" (già) hoặc "tôi"/"mình" | ngươi, các hạ, thiếu hiệp |

### 15.2. Quy tắc trình bày

- **Chính tả**: dấu thanh kiểu cũ thống nhất cho "oa/oe/uy": hoá, khoá, hoà, thuỷ, tuỳ, khoẻ (đa số chữ mới đang
  dùng). Chữ cũ đã có không sửa hàng loạt.
- **Tên vật phẩm mới**: kiểu câu ("Kẹo hiếm", "Quả Oran"); ngoại lệ đá tiến hoá giữ kiểu cũ ("Đá Nước", "Ruy
  Băng Tiên") vì lấy từ dump và `DaTienHoa.java`.
- **Viết hoa**: kiểu câu (chỉ hoa chữ đầu) cho tên menu, tên tính năng, nút; tên riêng (Pokémon, địa danh, NPC,
  tên vật phẩm trong bảng nội dung) giữ như bảng nội dung. Không viết HOA toàn bộ để nhấn mạnh trong game.
- **Chữ HOA có dấu** (Ấ, Ẩ, Ị…): không dùng trong chữ server/J2ME (test `KiemChuHoaCoDau`; J2ME phải ghép
  dấu tại chỗ, "Ỵ" và vài chữ hiếm có thể vẽ lệch). Đặt lại câu cho chữ đầu không có dấu, hoặc viết thường.
- **Ký tự font J2ME/Android không có** (hiện thành "?"): `" ' & ; < > = @ [ ] _ … – — → × ≥ ≤ “ ” ·`. Trong chữ
  game dùng ":" , dấu phẩy hoặc lời ("chọn", "nở ra") thay "→" (KHÔNG dùng "->": font cũng thiếu ">"); "..." thay
  "…"; "-" thay "–/—"; "x" thay "×"; "từ ... trở lên" thay "≥"; "chênh không quá N cấp" thay "±N cấp"; "và" thay
  "&"; ngoặc tròn thay ngoặc vuông; bỏ dấu nháy (vd: gõ lệnh dao vào kênh trò chuyện; nhận danh hiệu X). Web và PC
  vẽ được mọi ký tự.
- **Số**: hàng nghìn dùng dấu chấm (1.000, 25.000); phần trăm viết liền (10%); thời gian 20:00, 20h-22h; ngày
  dd/mm/yyyy; tiền VNĐ "49.000 đ" (có khoảng trắng; hàm `vnd()` của web đã theo); số thập phân trong mô tả dùng
  dấu phẩy (0,5%).
- **Dấu câu**: không có khoảng trắng trước : ! ? ; câu thông báo kết thúc bằng dấu chấm hoặc "!"; sau dấu phẩy
  viết thường; "..." dính liền chữ trước.
- **Không lộ thông tin nội bộ**: không id, tên bảng (tob_*, data_*), cờ cấu hình (sv.xxx, HTBB_*), mã lỗi kỹ thuật,
  "hạt giống" (seed) trong chữ người chơi thấy. Chữ cho admin/GM (Sổ tay GM, trang admin) được phép.
- **Độ rộng J2ME**: màn 176 px; tiêu đề/ô menu một dòng nên ≤ ~150 px theo `normalFont` (đo bằng
  `do_rong_j2me.py`). Hộp thoại tự ngắt dòng nên chỉ cần câu gọn.

### 15.3. Các nhóm chữ đã rà

"Đã duyệt" = "Đã duyệt (ủy quyền 18/10/2026)": chủ dự án uỷ quyền duyệt mọi chữ MỚI cho đợt rà soát này (ngoại lệ §10
CODING_CONVENTION). Số chuỗi đã rà là ước lượng.

| Nhóm / tệp | Đã rà | Đã sửa | Trạng thái |
|---|---|---|---|
| **Server `src/com/htbb/pokemon/`** | | | |
| HeSinhThaiGame.java (đua, giải tuần, bùng phát, đố vui) | ~70 | 19 | Đã duyệt (ủy quyền 18/10/2026) |
| HeSinhThaiMenu.java (menu Hệ sinh thái, kỹ năng sống, nhà trẻ, chợ/tủ trang trí) | ~240 | 28 | Đã duyệt (ủy quyền 18/10/2026) |
| NapTienGame.java, NapTienLogic.java (chữ nạp tiền trong game) | ~150 | 20 | Cần chủ server/luật sư xem: câu Hướng dẫn nạp (hạn mức mặc định 1.000.000 đ/tuần, 2.000.000 đ/tháng; "khoảng 15 giây") |
| SuuTamSinhThai, MiniGameSinhThai, NhiemVuMau, SuKienLauDai | ~30 | 6 | Đã duyệt (ủy quyền 18/10/2026) |
| KyNangSong, VatLieuSinhThai, HoSoSinhThai, SinhThaiTheGioi, BanDoSinhThai, NhaTre, CuonHut, DaTienHoa, HePokemon, ThangPvp, NhiemVuLauDai | ~180 | 0 | Đã duyệt (ủy quyền 18/10/2026) |
| Tệp LauDai/KinhTe/ChiSo/GiaiDoanBoss/ChiemGymLogic/QuaHoatDongLogic/DuLieuHeSinhThai…, NapTien.java, NapTienDb.java | chỉ khoá/SQL/log | 0 | Không có chữ người chơi thấy |
| GiftcodeAnToanLogic (chữ cho admin web) | 2 | 0 | Đã duyệt (ủy quyền 18/10/2026) |
| **Server `src/real/`, `src/data/`, `src/server/`** | | | |
| SoTayLauDai (Sổ tay huấn luyện) | ~30 | 9 | Đã duyệt (ủy quyền 18/10/2026) |
| NhiemVuEngine, NhiemVuLauDaiGame | ~37 | 9 | Đã duyệt (ủy quyền 18/10/2026) |
| GymLauDai, ChiemGymLauDai (chiếm Gym), HamNgucGame | ~18 | 7 | Đã duyệt (ủy quyền 18/10/2026) |
| BossLauDai, RoiLauDai, NoiDungLauDai, VatPhamDungLauDai | ~10 | 3 | Đã duyệt (ủy quyền 18/10/2026) |
| GiftcodeAnToan, GiftcodeLauDai, DatabaseGiftCode (thông báo giftcode) | ~13 | 0 | Đã duyệt (ủy quyền 18/10/2026); giữ "Mã quà tặng…" như thông báo cũ |
| QuaHoatDong (quà online), NangCapLauDaiGame, LauDaiNhanVat, DatabaseLauDai | ~35 | 0 | Đã duyệt (ủy quyền 18/10/2026) |
| Dòng mới ở Char*, Map*, OfflineMap, doUsePotion, BaiLuyenTap, LoginHandler, TeamServer, ChiSoMayChu, BaoTriDaily | ~24 | 6 | Đã duyệt (ủy quyền 18/10/2026) |
| SuKien.java nhóm "moi:*" (tên/mô tả sự kiện; chỉ trang admin thấy) | 15 | 1 | Đã duyệt (ủy quyền 18/10/2026); giữ "(cần sv.lau_dai=1)" vì chỉ admin thấy |
| **Nhiệm vụ** (`docs/pokemon/nhiem_vu_moi.csv`, 1.864 dòng; sửa ở sinh_nhiem_vu.py, nv_chung.py, nv_cot_truyen.py, nv_phu.py, nv_lap_lai.py, lau_dai_cot_truyen.py) | | | |
| Mẫu chung (kiểu mục tiêu, lời NPC, "đang làm", dòng thưởng, data_quest_moi) | ~250 mẫu | ~60 mẫu (~1.300 dòng) | Đã duyệt (ủy quyền 18/10/2026) |
| Hướng dẫn HD.01-10 | 68 | 4 | Đã duyệt (ủy quyền 18/10/2026) |
| Cốt truyện năm 1 (Kanto/Johto) | ~1.700 câu | ~70 | Đã duyệt (ủy quyền 18/10/2026) |
| Cốt truyện năm 2 (Hoenn, dàn ý) | ~600 | ~25 | Đã duyệt (ủy quyền 18/10/2026) |
| Nhiệm vụ phụ | ~1.000 | ~25 | Đã duyệt (ủy quyền 18/10/2026) |
| Hằng ngày, truy nã, tuần, nhóm, đội | ~400 | ~35 | Đã duyệt (ủy quyền 18/10/2026) |
| Sự kiện 52 tuần, huyền thoại, ẩn, thành tựu, Pokédex, sư phạm, hầm ngục, quay lại | ~900 | ~40 | Đã duyệt (ủy quyền 18/10/2026), trừ các việc ở 15.5 |
| **Nội dung** (sinh_noi_dung.py, sinh_vat_pham_lau_dai.py, sinh_sql_lau_dai.py, sinh_sql_he_sinh_thai.py) | | | |
| Tên data_item 1001..1061 | 59 | 1 | Đã duyệt (ủy quyền 18/10/2026) |
| Vật phẩm tiêu hao, đồ giữ (tên + mô tả) | 66 | 18 | Đã duyệt (ủy quyền 18/10/2026) |
| Nguyên liệu, bùa, ngọc hệ, phân rã | ~40 | 7 | Đã duyệt (ủy quyền 18/10/2026) |
| Chiêu (vai trò), nhánh Eevee | ~55 | 3 | Đã duyệt (ủy quyền 18/10/2026) |
| Gym, boss thế giới, token đột kích | ~45 | 2 | Đã duyệt (ủy quyền 18/10/2026) |
| Bộ trang bị, trang trí theo mùa, cửa hàng, chế tạo, chỉ số phụ, tiêu đề cột web | ~120 | 5 mẫu | Đã duyệt (ủy quyền 18/10/2026) |
| Vật phẩm GD2b (31 + 2 bùa) | 33 | 2 | Đã duyệt (ủy quyền 18/10/2026) |
| Lịch sự kiện 52 tuần (`LICH` trong sinh_sql_lau_dai.py) | ~230 | 15 | Đã duyệt (ủy quyền 18/10/2026) |
| sinh_sql_he_sinh_thai.py | 0 | 0 | Không có chữ người chơi thấy |
| **Client** (htbb-client) | | | |
| J2ME game/pokemon/* (SinhThai, BangChieu, BangTiemNang, BangTrangBi, ThuongHieu, PokeMang, AmThanh, Banner…) | ~150 | 11 | Đã duyệt (ủy quyền 18/10/2026) |
| J2ME màn hình (MenuNhanh, TuiSinhThaiScr, GhiCongScr, LoginScr, CharSelectScr, GameScr…) | ~60 | 6 | Đã duyệt (ủy quyền 18/10/2026) |
| TenMapPokemon J2ME (BANG 38 + BANG_HIEN 20, qua `doiHien()`) đối chiếu server | 58 cặp | 1 sửa, 1 thêm | Đã duyệt (ủy quyền 18/10/2026) |
| Android (strings.xml, NenAndroid, Nen) | ~8 | 2 | Đã duyệt (ủy quyền 18/10/2026) |
| PC C# (BangChieu, BangTiemNang, HudPokemon, PhanHoiChienDau, MessageHandler, ThuongHieu, MenuNhanh…) | ~120 | 8 | Đã duyệt (ủy quyền 18/10/2026) |
| CHANGELOG.md | ~40 dòng | 2 | Đã duyệt (ủy quyền 18/10/2026) |
| GHI_CONG.txt (sinh bởi j2me/tools/sinh_ghi_cong.py) | 262 dòng | 2 | Cần chủ server/luật sư xem (câu giấy phép CC BY-NC) |
| **Web** (HTBB-Web) | | | |
| Trang công khai, layouts, components, CMS/FAQ/tin tức mặc định | ~420 | 27 | Đã duyệt (ủy quyền 18/10/2026) |
| Cẩm nang (20 trang + camNang.ts) | ~520 | 19 | Đã duyệt (ủy quyền 18/10/2026) |
| Tài khoản (đăng nhập, đăng ký, kích hoạt, quên mật khẩu) | ~150 | 5 | Đã duyệt (ủy quyền 18/10/2026) |
| Nạp tiền (naptien, xac-nhan-nap, napTienHienThi.ts, MOC_NAP) | ~110 | 16 | Cần chủ server/luật sư xem |
| Pháp lý (dieu-khoan, quyen-rieng-tu, dieu-khoan-thanh-toan, chinh-sach-hoan-tien) | ~70 | 8 | Cần chủ server/luật sư xem (chỉ sửa ngôn ngữ, giữ nghĩa và ghi chú "cần rà soát pháp lý") |
| Admin (pages/components/api admin) | ~1.560 | 0 | Đã duyệt (ủy quyền 18/10/2026); giữ thuật ngữ kỹ thuật |
| API cho người chơi (auth, nap, tin-tuc, middleware) | ~70 | 8 | Đã duyệt (ủy quyền 18/10/2026); riêng thông báo nạp: Cần chủ server/luật sư xem |

### 15.4. Thay đổi đáng chú ý

- **Ký tự font J2ME/Android không có** (hiện thành "?"). Trước đây có trong chữ mới: "→" (~30 dòng nhiệm vụ), "≥"
  (~92), "–/—", dấu nháy quanh lệnh (Gõ "dao"), "&" trong menu ("Kỹ năng & thể lực"), ">" ("NPC Tổng Quản > Cửa hàng &
  Nạp"), "±5 cấp", "[x]", ";", "->" giữa các mốc đua. Nay: "gõ lệnh dao vào kênh trò chuyện", "Kỹ năng và thể lực",
  "NPC Tổng Quản, mục Cửa hàng và nạp", "chênh không quá 5 cấp", "(x)", mốc đua cách nhau bằng dấu phẩy.
- **Chữ HOA có dấu / viết HOA cả câu**: "XUẤT PHÁT! Mốc:" -> "Xuất phát! Các mốc:", "NHÀ VÔ ĐỊCH GIẢI TUẦN" -> "Nhà vô
  địch giải tuần", "Ấp nở" -> "Cho trứng nở", "ĐANG BẬT/TẮT" -> "đang bật/tắt", "Ẩn xu/lượng trên bảng top" -> "Bảng
  top: ẩn xu/lượng", ~65 câu nhiệm vụ mở bằng Ả/Ở/Ấ/Ồ/Ừ… và "BẢNG TRUY NÃ!", "GẦM!"… Test "khong chuoi nao con chu HOA
  co dau" nay đạt (nền 65 hỏng -> 64). Client J2ME `SinhThai.docMoc` nhận cả "Xuất phát"/"mốc:" mới lẫn chữ cũ.
- **Lộ thông tin nội bộ**: "Hầm ngục bí ẩn hôm nay (hạt giống N)" -> bỏ seed (nhiệm vụ: "vừa đổi lối mới"; web: "bố cục");
  "Cần xong <mã nhiệm vụ>" -> "Cần hoàn thành nhiệm vụ trước đó"; người vắng trong kết quả đua "#<id>" -> "(vắng mặt)";
  web: lỗi "Chưa có bảng nạp tiền (nap_*) — chạy …sql" và "Gói đang cấu hình sai: …" chỉ còn cho admin, người chơi thấy
  "Tính năng nạp tiền chưa sẵn sàng…"; dàn ý năm 2 "Chương này chưa được viết" -> câu trong truyện; GhiCongScr "tệp
  GHI_CONG.txt" -> "tệp ghi công".
- **Lỗi ghép mẫu nhiệm vụ**: "Kecleon: Hí hí, Cứ từ từ, nhưng nhớ ghé Kecleon nhé." (viết hoa sau phẩy, NPC tự gọi tên mình)
  -> lời "đang làm" 2-3 biến thể theo xưng hô từng NPC; "Đứng tại Pallet trên Pallet (ô ?,?)" -> "Đến Pallet và đứng yên
  vài giây"; tên loài/boss bị trống ("Nghe nói có  màu sắc…"); "Nói chuyện với Xatu ở Pallet ở Pallet".
- **Thuật ngữ**: thủ lĩnh -> trưởng Gym; bang/quỹ bang -> đội/quỹ đội; tổ đội tạm -> nhóm; "Bảng ngày/tuần" -> "Nhiệm vụ
  hằng ngày/tuần"; "Hầm ngục"/"Hầm Ngục Bí Ẩn" -> "Hầm ngục bí ẩn"; "Kẹo Hiếm" -> "Kẹo hiếm"; client "Điểm kỹ năng",
  "ST", "Lv" -> "Điểm chiêu", "Uy lực", "Cấp"; web "khắc chế" -> "khắc hệ", "Chiếm thành" -> "Chiếm Gym", "Lôi đài" ->
  "Sàn đấu", "nhân vật" -> "Pokémon", "track" -> "nhánh thưởng"; dấu kiểu cũ (hoá, khoá, khoẻ, thuỷ, hoạ).
- **Nạp tiền trong game**: bỏ giá cứng "50.000 đ / 30 ngày" (admin đổi được giá) -> "Thẻ tháng (30 ngày) được bán trên
  website"; "Trần nạp: 1000k/tuần" -> "Hạn mức nạp mặc định: 1.000.000 đ/tuần, 2.000.000 đ/tháng"; lỗi rao giá chợ không
  dấu -> có dấu.
- **Số**: thưởng nhiệm vụ "Nhận thưởng: 15479 EXP" -> "15.479 EXP" (NhiemVuEngine); số ≥ 1.000 trong lời thoại có dấu
  chấm; web `vnd()` "49.000đ" -> "49.000 đ"; "+5‰" -> "+0,5%".
- **Độ rộng J2ME (176 px)**: 139 tên nhiệm vụ quá ~150 px còn 5 (tối đa 161 px); "Quà lễ Phụ nữ Việt Nam 20/10" 161 ->
  "Quà lễ Phụ nữ 20/10" 110 px; mục menu "Nhận thưởng vùng sinh thái" (trước 224 px); lời giới thiệu và "Pokémon (c)
  Nintendo / Game Freak" (173/178 px, tràn) có bản ngắn cho màn hẹp; "Sư phạm (nhận trò)".
- **Tên bản đồ**: bảng chính TenMapPokemon J2ME/PC/ClientUnity/server khớp hoàn toàn (38 cặp, KiemPokeDuLieu: 304 chuỗi
  khớp). Bảng hiện riêng client (`doiHien`): "Đấu trường liên minh" -> "Gym liên đấu" cho khớp tự điển server; thêm
  {"Làng Sơn Nam", "Pewter"}; nhãn menu chọn khu/bản đồ lớn nay cũng qua `doiHien`.
- **Khoảng trống glyph J2ME**: font ghép chữ HOA có dấu tại chỗ; mô phỏng trên `normalFont`: Ỳ, Ý, Ỷ, Ỹ, Â, Ô ghép đúng,
  **Ỵ ghép được nhưng dấu nặng lệch** (dưới-phải, cách chân chữ 2 hàng). bigFont có sẵn Ỵ (không có chữ thường);
  smallFont/smallFontColor không có chữ hoa và dấu. CHANGELOG ghi ở "Lỗi còn biết". Logic font giữ nguyên.

### 15.5. Còn treo, cần chủ dự án / luật sư quyết

| Việc | Ghi chú |
|---|---|
| Web khẳng định "không nạp tiền" (FAQ, SEO, huong-dan, ghi-cong, dieu-khoan, kinh-te, download) trong khi đã có `/naptien` (tắt mặc định) và "Top ủng hộ" | Bật nạp thì các câu đó sai và `dieu-khoan` phải sửa nội dung; rủi ro giấy phép CC BY-NC. Cần chủ server/luật sư xem |
| `dieu-khoan.vue`, `quyen-rieng-tu.vue` chưa có ghi chú "cần luật sư duyệt" (hai trang thanh toán đã có) | Không tự thêm vào trang hiển thị |
| "Farfetch'd": font J2ME không có dấu ' -> hiện "Farfetch?d" (~94 chỗ) | Thêm ký tự vào font hoặc viết "Farfetchd" |
| Pokédex shiny (D-xxx.3): chữ ghi tên loài nhưng engine vẫn nhận shiny bất kỳ | Có buộc đúng loài không |
| Lỗi logic thấy khi rà (không phải chữ, chưa sửa) | `toa_do` sinh "x;y" nhưng DatabaseLauDai đọc theo ","; huyền thoại ZAP/RAI/HOO dùng "Lông vũ tím" nhưng lời tả lông vàng/bảy sắc; `dia_danh` chương năm 2 là tên chương |
| Tên sự kiện "Thế chiến vùng" (su_kien_nam / LICH) | Đã đổi thành "Chiến vùng" ở nguồn (sinh_sql_lau_dai.py LICH, SuKien.DANH_MUC) |
| Item 720 (thú nuôi) cũng tên "Kẹo hiếm" | Chọn tên mới cho 720 (NOI_DUNG_DAY_DU.md đã ghi) |
| "Hạt giống thần thần thoại", "Nước thần thần thoại" (lặp chữ) | Đề xuất cả họ: "Hạt kỳ diệu", "Nước huyền bí" (tên các bậc cũ nằm trong dump) |
| "Dưa Hấu" (potion 145), "Hộp quà halloween" (từ dump) | Đề xuất "Dưa hấu", "Hộp quà Halloween" qua tự điển/dump |
| Tên bản đồ client đổi mà tự điển server chưa có: "TYT 5x/6x/7x", "Chiến trường", "Đấu trường la mã", "Ngục tối", "Khu mỏ", "Cân núi" | Thêm vào `ChuPokemon.DOI` để thông báo server khớp; PC chưa có bảng `BANG_HIEN` |
| "Cao nguyên Indigo" viết hoa lệch "Núi Mặt Trăng", "Hồ Phẫn Nộ" | Đổi thì phải đồng bộ server/ClientUnity/PC/J2ME |
| Server "điểm kỹ năng" (ChuPokemon.java, Text.help) vs client "Điểm chiêu" | Đề xuất thêm vào tự điển |
| "Đổi Pet" (Menu nhanh), "Bạo kích!" (PC), "Đại hỏa"/"Tia phá hủy" (SKILL_NAME) | Đề xuất "Đổi Pokémon" (71 px vừa ô 76), "Chí mạng!", tên chiêu chuẩn - đổi đồng bộ từ server |
| "Hết tiền" (MapEpNgoc, chữ cũ) | Đề xuất tự điển: "Không đủ xu." |
| Thông báo giftcode mới dùng "Mã quà tặng…" như cũ | Muốn "Giftcode …" thì đổi cả chữ cũ qua tự điển |
| Phím chiêu web (`chieu-thuc.vue`, `HudMinhHoa.vue`: 1,3,5,7,9) lệch bảng điều khiển `huong-dan.vue` (5 đánh thường, 7/9 thuốc) | Đối chiếu với game |
| Vài mục menu nạp/giải tuần vẫn 155-180 px ("Vé mùa N (cao cấp): bậc X/30"…) | Chưa xác minh J2ME cắt mục menu dài thế nào |
| `sql/htbb_day_du.sql` chưa có chữ mới | Chạy lại `tools/pokemon/xuat_sql_day_du.sh` trên MariaDB trống (các tệp `sql/pokemon_*.sql` đã sinh lại) |
| Chữ admin web (`thuong.ts`, `giftcodeDanhMuc.ts`, `duaTop.ts`, `docLog.ts`) còn tên kiếm hiệp gốc của mã phần thưởng | Chỉ GM thấy, giữ |

Kiểm tra sau đợt này: `quet_tu_cam.py --kiem` 0; test server 64/7.739 hỏng (nền 65, hết mục chữ HOA có dấu, không hỏng
mới); J2ME biên dịch được, `KiemPokeDuLieu` OK (ChuPokemon khác server 0, TenMapPokemon 304 chuỗi khớp); web `nuxt build`
(Node 22) và `npm test` 6/6 đạt.
