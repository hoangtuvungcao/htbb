# Thuật ngữ chuẩn Huyền Thoại Bảo Bối (cũ → mới)

Bản chốt ngày 18/10/2026. Áp cho **mọi chữ người chơi và admin thấy**: server (chuỗi Java), CSDL (dữ liệu gốc +
`sql/pokemon_*.sql`), client J2ME / Android / PC, website HTBB-Web (trang công khai, trang admin, thông báo API),
web PHP cũ `HTBB/web`, cẩm nang, CHANGELOG. Bổ sung (không thay) bảng chuẩn ở `DUYET_CHU.md` mục 15.1; chỗ nào
hai bảng khác nhau thì theo file này.

Một nguồn duy nhất cho máy: `src/com/htbb/pokemon/ChuPokemon.java`

- `TU_CAM`: từ cũ bị cấm (test `KiemThuatNgu` và `tools/pokemon/quet_tu_cam.py` bắt);
- `DOI`: cặp {cũ, mới} (từ điển lúc chạy của server; sinh sang `ChuPokemonDuLieu.cs` cho client PC và `chu.bin`
  cho J2ME/Android);
- `CHO_PHEP`: cụm chứa từ cấm nhưng hợp lệ (giữ nguyên).

Từ 18/10/2026 **mã nguồn đã mang thuật ngữ mới** (không còn chỉ đổi lúc chạy). Từ điển lúc chạy vẫn giữ để đổi
phần động (tên từ CSDL cũ, chữ người chơi gửi lên...).

## 1. Bảng thuật ngữ chính

| Cũ (kiếm hiệp) | Mới (Pokémon) | Ghi chú |
|---|---|---|
| bang, bang hội, bang phái | **đội** | nhóm người chơi lâu dài; "nhóm" = tổ đội tạm thời |
| bang chủ, chủ bang / phó bang / bang chúng | đội trưởng / đội phó / thành viên đội | |
| trưởng lão (chức vụ bang) | cố vấn | |
| chiếm thành, công thành, chiến thành | **Chiếm Gym** (tính năng), chiếm Gym, chiến Gym | |
| thành chủ, giữ thành, sở hữu thành, cổng thành | trưởng Gym, giữ Gym, sở hữu Gym, cổng Gym | không dùng "thủ lĩnh" |
| thành trấn danh | Cao nguyên Indigo | thành chiếm của cả server |
| vận tiêu, bảo tiêu, chuyến tiêu | **giao hàng**, chuyến giao hàng | |
| nhận / trả / đổi / cướp / thế tiêu, màu tiêu, con tiêu | nhận / trả / đổi / cướp / thế chỗ hàng, màu hàng, kiện hàng | |
| tiêu cục, xa tiêu lệnh, ngựa vận tiêu | trạm giao hàng, vé xe giao hàng, Ponyta giao hàng | |
| liên đấu (giải liên máy chủ) | **giải Liên minh** (Pokémon League) | "Liên minh" viết hoa như tên riêng |
| thành liên đấu N | sân Liên minh N | |
| lôi đài, võ đài | **sàn đấu** | |
| tỷ võ / tỉ võ / luận kiếm | đấu tập | |
| tống kim, chính tà, quốc chiến | chiến trường liên vùng, chiến vùng | |
| lãnh thổ, quốc gia | vùng (Kanto / Johto / Hoenn) | thanh long / hắc hổ / huyền vũ → Kanto / Johto / Hoenn |
| đức vua, đại thần, bộ đầu, tướng quân, thống lĩnh, nguyên soái | nhà vô địch vùng, cố vấn vùng, cảnh sát vùng, chỉ huy | chức vụ vùng |
| phế chức | bãi nhiệm | |
| linh thú (thú cưỡi), ngựa, chiến mã | **thú cưỡi** | tên con cụ thể theo `bang_thu.csv` (Ponyta, Rapidash...) |
| thần thú | Pokémon huyền thoại | |
| cưỡi ngựa / xuống ngựa / cưỡi linh thú | cưỡi thú / xuống thú cưỡi / cưỡi thú | |
| xe ngựa, đi xe ngựa, xa phu, thần hành phù | chuyến bay Pidgeot, bay Pidgeot, Pidgeot (Bay), vé bay Pidgeot | |
| khinh công | chạy nhanh | |
| phi phong | **ruy băng** (ô trang bị 19, `BangTrangBi` "Ruy băng") | "phi phong ma/vật" = ruy băng đặc biệt / vật lý |
| vũ khí | **đồ cầm tay** (theo dòng: Than lửa, Hạt giống thần, Khăn lụa, Nước thần, Nam châm) | "Kiếm, Đao, Bút, Búa, Cung" (tên vũ khí) → tên đồ cầm tay đó |
| thần binh, vũ khí thần binh | bảo vật, bảo vật cầm tay | |
| thần khí | bảo vật huyền thoại | |
| giáp trụ, ngọc bội, dây chuyền, hộ uyển | lớp giáp, phiến đá hệ, vòng cổ, vòng chân | |
| bí kíp | đĩa chiêu | |
| tụ hồn đan, tiên đan, tiên dược, tiềm năng đan, kỹ năng đan, linh đan | Kẹo Poké, cỏ hồi sinh, kẹo kinh nghiệm, kẹo tiềm năng, kẹo kỹ năng, thuốc hồi phục | |
| huyết linh thảo, huyết bồ đề, nhân sâm | Kẹo thú cưng, Bạc hà tính cách, Berry Razz vàng | |
| hỏa thạch, ngũ hợp, huyền minh, tiến giai | nham thạch, cầu vồng, hoàng hôn, thăng bậc | tên nguyên liệu |
| lệnh bài, thẻ bài, kim bài, hộ vệ lệnh; lệnh bài trấn yêu | thẻ, thẻ, huy hiệu lập đội, vé gọi Pokémon hỗ trợ; vé Mê cung | |
| trấn yêu (trận), Vạn Tiên Trận (sự kiện vượt ải theo nhóm) | **Mê cung** (vé Mê cung, "Mê cung kết thúc", boss Mê cung) | khác "Tháp Pokémon" ở Lavender (địa danh trong cốt truyện) và "Hầm ngục bí ẩn" (tính năng khác) |
| hộ vệ | Pokémon hỗ trợ | |
| nội công, nội lực, nội kình, chân khí, công lực | **năng lượng**, sức mạnh | |
| võ công | chiêu thức (trong câu thường: "chiêu") | tên chiêu: `docs/pokemon/bang_chieu.csv` |
| tâm pháp, kinh mạch | bí quyết, thể trạng | |
| ma pháp / vật lý (chỉ số), thủ ma / thủ vật | đặc biệt / vật lý, phòng thủ đặc biệt / phòng thủ | |
| ngũ hành; hệ Kim / Mộc / Thủy / Hỏa / Thổ; kháng kim... | **hệ**; hệ Thép / Cỏ / Nước / Lửa / Đất; kháng Thép... | mảng tên ngũ hành trong mã cũng đổi (SkinThuCuoi, ItemInInventory, CreateCharScr) |
| nhất / nhị / tam / tứ / ngũ phẩm | hạng 1..5 | |
| nội tại | đặc tính | |
| nhân vật | **Pokémon** (người chơi LÀ một Pokémon) | áp cả trang admin ("Khoá Pokémon", "Tên Pokémon") |
| môn phái | dòng Pokémon | |
| kiếm khách / kiếm sĩ, chiến binh, pháp sư, đấu sĩ, cung thủ | Dòng Charmander, Dòng Bulbasaur, Dòng Eevee, Dòng Squirtle, Dòng Pichu | thứ tự class 0..4 (`ChuPokemon.TEN_DONG`) |
| đại hiệp, nghĩa sĩ, thiếu hiệp, hiệp khách | nhà huấn luyện, nhà huấn luyện trẻ, nhà thám hiểm | |
| cao thủ; top cao thủ | nhà vô địch; top sức mạnh | |
| sư phụ / đệ tử (Sư phạm) | thầy / học trò | kèm cấp, giữ nghĩa dẫn dắt |
| sư huynh / sư đệ / sư muội / sư tỷ | bạn học | |
| chưởng môn | người đứng đầu | |
| tiền bối, huynh, tiểu đệ, tại hạ, các hạ, ngươi | bậc đàn anh, anh, em, tôi, cậu, cậu | NPC xưng "ta"/"tôi", gọi người chơi "cậu"/"bạn" |
| giang hồ, võ lâm, kiếm hiệp, thiên hạ | thế giới Pokémon, mọi vùng | |
| bôn tẩu, hành tẩu | phiêu lưu | |
| giết, đồ sát, liên trảm, cừu nhân | hạ, PK, chuỗi thắng, đối thủ | |
| yêu ma, yêu quái, cương thi, ma đầu | Pokémon hung dữ, Pokémon ma, kẻ xấu | |
| sơn tặc, thổ phỉ, đạo tặc | kẻ trộm | |
| khách điếm, tửu lâu | nhà trọ, quán ăn | |
| ngân lượng, nguyên bảo | xu, lượng | |
| luyện công, bãi luyện công | luyện tập, bãi luyện tập | |
| phu quân / phu nhân, phu thê | bạn đời | |
| chết, tử vong (nhân vật) | **gục** ("Pokémon của bạn đã gục!") | quái thì "hạ" |
| về làng (sau khi chết), hồi thành | **về Trung tâm Pokémon** | Hoenn: Trung tâm của thị trấn Hoenn gần nhất; Kanto/Johto: làng nhà (`docs/pokemon/HOI_SINH.md`) |
| tiên đan / cỏ hồi sinh (dùng khi gục) | **Thuốc hồi sinh** (30% HP), **Thuốc hồi sinh tối đa** (100% HP) | potions[119] / item 1207 |
| suy yếu sau khi chết | **Mệt mỏi** (−EXP, −tấn công vài phút) | |

Tên NPC, pet, thú cưỡi, thời trang, danh hiệu, bộ sưu tập kiểu cũ (Dì út, Bà tám, Hắc ngưu, Phượng hoàng, Kỳ lân,
Tứ bình...) đổi sang tên Pokémon theo `ChuPokemon.DOI` (xem phụ lục) và các bảng `docs/pokemon/bang_npc.csv`,
`bang_thu.csv`, `bang_pet_cu.csv`.

## 2. Ngoại lệ (không phải thuật ngữ cũ, giữ nguyên)

| Chữ | Vì sao giữ |
|---|---|
| "tìm kiếm", "kiếm được", "kiếm xu" | động từ "kiếm"; chỉ "Kiếm" viết hoa (tên vũ khí) mới bị cấm (`=Kiếm`) |
| "Múa kiếm" | tên chiêu Swords Dance (`CHO_PHEP`) |
| "thành phố", "thành công", "hoàn thành", "thành viên", "vào thành phố", "giữ thành phố" | "thành" chỉ bị cấm trong cụm Chiếm Gym; hai cụm cuối nằm trong `CHO_PHEP` |
| "hệ kim cương" | kim cương (đá quý), không phải hệ Kim (`CHO_PHEP`) |
| "kim đan" (nhiệm vụ đan len) | kim đan len, không phải đan dược |
| "võ sĩ" | lớp nhà huấn luyện Black Belt (Poliwrath tập dưới thác) |
| "lính gác" | lính gác thường (Arcanine gác cổng) |
| "xác minh chủ tài khoản" | "minh chủ" ở đây là "xác minh" + "chủ" |
| "bang" trong chữ KHÔNG dấu ("BANG DIEU KHIEN", "chua doc duoc bang", khoá `'bang'`) | là "bảng"/"băng" không dấu hoặc khoá kỹ thuật |
| "Nhân vật dựa trên Pokémon Mystery Dungeon" (GHI_CONG.txt) | câu giấy phép: nhân vật = nhân vật bản quyền |
| địa danh cũ trong bảng bản đồ của mã (Dao Châu, Sơn lâm...) | client tra bản đồ theo tên cũ; tên hiện ra đổi lúc chạy bằng `TenMapPokemon` (server + client), không phải thuật ngữ |
| chú thích, tên biến, tên tệp, khoá cấu hình (`sv.expvantieu`, `tob_chiemthanh`, `MapDoi`...) | việc của đợt đổi tên định danh (xem mục 4) |
| tài liệu thiết kế/lịch sử trong `docs/` (bảng "Không dùng", nhật ký đổi tên) | chữ cho người làm game, phải nêu từ cũ |

## 3. Công cụ và kiểm tra

- `python3 -I tools/pokemon/ap_thuat_ngu.py [--ghi]`: đổi chuỗi người chơi thấy trong Java/C# (server, J2ME,
  Android, PC, ClientUnity) bằng `ChuPokemon.doi`. Chuỗi dùng để so sánh in ra "XEM TAY" (hai đầu server/client
  phải đổi cùng nhau).
- `python3 -I tools/pokemon/ap_thuat_ngu_web.py [--ghi]`: như trên cho HTBB-Web (.vue/.ts/.js/.mjs/.sql) và web PHP
  cũ `HTBB/web`.
- `tools/pokemon/sinh_sql_thuat_ngu.py` → `sql/pokemon_thuat_ngu.sql`: UPDATE dữ liệu gốc còn sót (chạy cuối trong
  `xuat_sql_day_du.sh`, idempotent: `WHERE <khoá chính> AND cột=<chữ cũ>`).
- Dữ liệu sinh: sửa generator rồi chạy lại (`sinh_noi_dung.py`, `sinh_nhiem_vu.py`, `sinh_sql_chu.py`,
  `tools/cauhinh/sinh_cau_hinh.py`, `tools/SinhSqlTenSkinThu.java`, `tools/SinhDanhMuc*.java`), từ điển client
  `chu.bin` bằng `DongGoiPokeJ2me.ghiTuDien`.
- Test `test/KiemThuatNgu.java` (bộ `Kiem`): quét chuỗi người chơi/admin thấy ở server, client J2ME/Android/PC,
  web, PHP, `sql/pokemon_*.sql` và `sql/htbb_day_du.sql`; phải **0** từ cấm. `tools/pokemon/quet_tu_cam.py`
  (báo cáo chi tiết, cả "trước/sau đổi").

Thêm một từ cũ: thêm vào `TU_CAM` + cặp `DOI` (giá trị mới không chứa khoá nào, không có chữ HOA có dấu khi viết hoa
đầu từ), chạy `ap_thuat_ngu.py --ghi`, `ap_thuat_ngu_web.py --ghi`, `sinh_sql_chu.py`, `GhiChuBin`/`build.sh`, sinh lại
dump, rồi bộ test.

## 4. Gợi ý tên định danh (cho đợt đổi tên mã nguồn)

| Định danh cũ | Gợi ý mới |
|---|---|
| `BangHoi`, `Clan` (chữ), `MapDoi` | `Doi`, `MapDoi` |
| `VanTieu`, `Vantieu`, `TraHang`, `MonsterVantieu` | `GiaoHang`, `TraHang`, `QuaiGiaoHang` |
| `RegionVantienTran`, `MapVanTienTran`, `BossMeCung`, `TranYeu`, `LenhBaiTranYeu` (sự kiện Vạn Tiên Trận, KHÔNG phải giao hàng) | `VungMeCung`, `MapMeCung`, `BossMeCung` (đã có), `MeCung`, `VeMeCung` |
| `ChiemThanh`, `CongGym`, `MapTown` (phần chiếm) | `ChiemGym` |
| `LienDau`, `MapLienDau`, `NpcReceiveCardLienDau` | `GiaiLienMinh`, `MapGiaiLienMinh` |
| `LoiDai` | `SanDau` |
| `TongKim` | `ChienTruongLienVung` |
| `LinhThu`, `SkinThuCuoi`, `PokemonHuyenThoai` | `ThuCuoi`, `SkinThuCuoi`, `PokemonHuyenThoai` |
| `PhiPhong` | `RuyBang` |
| `VuKhi`, `itemVukhiThoiTrang` | `DoCamTay`, `itemDoCamTayThoiTrang` |
| `ThanBinh`, `TinhLuyenThanBinh` | `BaoVat`, `TinhLuyenBaoVat` |
| `BaoVatHuyenThoai` (gói), `ThoRenBiAn` | `BaoVatHuyenThoai`, `ThoRenBiAn` |
| `HoaKyLan`, `TuHonDan`, `HuyetLinhThao` | `Ponyta`, `KeoPoke`, `KeoThuCung` |
| `MonPhai`, `NAME_CLASS` | `DongPokemon` |
| `NoiLuc`, `NoiCong` | `NangLuong` |
| `SuPhu`, `DeTu` | `Thay`, `HocTro` |
| `NhanVat` | giữ hoặc `Pokemon` (tuỳ đợt đổi tên; chữ hiện ra đã là "Pokémon") |
| `LanhTho`, `QuocGia` | `Vung` |
| `XaPhu` | `PidgeotBay` |

## 5. Phụ lục: mọi từ cấm (`ChuPokemon.TU_CAM`) và chữ thay (`ChuPokemon.doi`), 18/10/2026

| Từ cũ (bị cấm) | Đổi thành (ChuPokemon.doi) |
|---|---|
| giang hồ | thế giới Pokémon |
| võ lâm | thế giới Pokémon |
| môn phái | dòng Pokémon |
| bang hội | đội |
| bang chủ | đội trưởng |
| phó bang | đội phó |
| bang | đội |
| lãnh thổ | vùng |
| thanh long | Kanto |
| hắc hổ | Johto |
| huyền vũ | Hoenn |
| linh thú | thú cưỡi |
| xa phu | Pidgeot (Bay) |
| đại hiệp | nhà huấn luyện |
| hiệp khách | nhà thám hiểm |
| thiếu hiệp | nhà huấn luyện trẻ |
| tiền bối | bậc đàn anh |
| nội công | năng lượng |
| nội lực | năng lượng |
| võ công | chiêu thức |
| thần binh | bảo vật |
| bí kíp | đĩa chiêu |
| cao thủ | nhà vô địch |
| kiếm khách | dòng Charmander |
| kiếm hiệp | Pokémon |
| kiếm sư | Farfetch'd |
| kiếm sĩ | dòng Charmander |
| kiếm sỹ | dòng Charmander |
| thanh kiếm | lưỡi năng lượng |
| Kiếm | Than lửa |
| đao | hạt giống thần |
| nhân vật | Pokémon |
| ngũ hành | hệ |
| huynh | anh |
| đệ tử | học trò |
| sư phụ | thầy |
| tại hạ | tôi |
| nghĩa sỹ | nhà huấn luyện |
| nghĩa sĩ | nhà huấn luyện |
| tướng quân | chỉ huy |
| chiến binh | dòng Bulbasaur |
| pháp sư | dòng Eevee |
| đấu sĩ | dòng Squirtle |
| cung thủ | dòng Pichu |
| bôn tẩu | phiêu lưu |
| hành tẩu | phiêu lưu |
| ngươi | cậu |
| giết | hạ |
| yêu ma | Pokémon hung dữ |
| cương thi | Pokémon ma |
| nội thương | vết thương |
| nội kình | năng lượng |
| kình lực | sức mạnh |
| anh hào | nhà huấn luyện tài ba |
| ẩn sỹ | bậc thầy |
| ẩn sĩ | bậc thầy |
| kết nghĩa | (chỉ có trong cụm dài ở DOI, vd "anh em kết nghĩa"; gặp riêng lẻ thì viết lại câu) |
| phu quân | bạn đời |
| phu nhân | bạn đời |
| nghĩa hiệp | (chỉ có trong cụm dài ở DOI, vd "anh em kết nghĩa"; gặp riêng lẻ thì viết lại câu) |
| vận tiêu | giao hàng |
| cướp tiêu | cướp hàng |
| bảo tiêu | giao hàng |
| xa tiêu | (chỉ có trong cụm dài ở DOI, vd "anh em kết nghĩa"; gặp riêng lẻ thì viết lại câu) |
| trấn yêu | Mê cung |
| lệnh bài | thẻ |
| thẻ bài | thẻ |
| chiếm thành | chiếm Gym |
| lôi đài | sàn đấu |
| đức vua | nhà vô địch vùng |
| đại thần | cố vấn vùng |
| bộ đầu | cảnh sát vùng |
| phế chức | bãi nhiệm |
| quốc gia | vùng |
| chính tà | liên vùng |
| tống kim | liên vùng |
| cừu nhân | đối thủ |
| liên trảm | chuỗi thắng |
| đồ sát | PK |
| long trụ | trụ năng lượng |
| thống lĩnh | chỉ huy |
| thần khí | bảo vật huyền thoại |
| hỏa thạch | nham thạch |
| hoả thạch | nham thạch |
| huyền minh | hoàng hôn |
| ngũ hợp | cầu vồng |
| tiến giai | thăng bậc |
| tụ hồn đan | Kẹo Poké |
| tiên đan | cỏ hồi sinh |
| tiên dược | kẹo kinh nghiệm |
| huyết linh thảo | Kẹo thú cưng |
| huyết bồ đề | Bạc hà tính cách |
| huyết bổ đề | Bạc hà tính cách |
| nhân sâm | Berry Razz vàng |
| bát bảo | (chỉ có trong cụm dài ở DOI, vd "anh em kết nghĩa"; gặp riêng lẻ thì viết lại câu) |
| bách niên giai lão | trái tim đồng |
| phu thê | (chỉ có trong cụm dài ở DOI, vd "anh em kết nghĩa"; gặp riêng lẻ thì viết lại câu) |
| nguyệt linh | (chỉ có trong cụm dài ở DOI, vd "anh em kết nghĩa"; gặp riêng lẻ thì viết lại câu) |
| ám khí | đòn đánh lén |
| kim cang | Thu mình |
| kim cô | vòng đầu |
| bát giới | Emboar |
| sa tăng | Swampert |
| tề thiên | Infernape |
| tứ bình | bộ Berry |
| tứ nhạc | bộ Flute |
| tứ khoái | bộ huy hiệu |
| tứ linh | bộ huyền thoại |
| tóc | lông |
| ma pháp | đặc biệt |
| thủ ma | phòng thủ đặc biệt |
| thủ vật | phòng thủ |
| nhất phẩm | hạng 1 |
| ngũ phẩm | hạng 5 |
| nội tại | đặc tính |
| thiên nhãn | Phân thân |
| càn khôn | Màn ánh sáng |
| phí huyết | Rễ bám |
| yểm bùa | nguyền rủa |
| bạch cốt | Spectrier |
| đương khang | Piloswine |
| mãnh hổ | Arcanine |
| tiên hạc | Pidgeot |
| bạch mã | Rapidash |
| phượng hoàng | Moltres |
| kỳ lân | (chỉ có trong cụm dài ở DOI, vd "anh em kết nghĩa"; gặp riêng lẻ thì viết lại câu) |
| kì lân | (chỉ có trong cụm dài ở DOI, vd "anh em kết nghĩa"; gặp riêng lẻ thì viết lại câu) |
| lân sư | Suicune |
| xích thố | Ponyta lửa |
| thiên lý mã | Ponyta |
| lạc long quân | Rayquaza |
| âu cơ | Ho-Oh |
| thạch sanh | Lucario |
| hằng nga | Clefairy |
| chú cuội | Clefable |
| hồng hài nhi | Magby |
| voldemort | Giratina |
| voldermort | Giratina |
| harry potter | Alakazam |
| dracula | Darkrai |
| tiểu long nữ | Latias |
| dương quá | Latios |
| ngưu lang | Volbeat |
| chức nữ | Illumise |
| sơn tinh | Groudon |
| mị nương | (chỉ có trong cụm dài ở DOI, vd "anh em kết nghĩa"; gặp riêng lẻ thì viết lại câu) |
| ông địa | Snorlax |
| ngạ quỷ | Haunter |
| dạ xoa | Gengar |
| phúc thú | Copperajah |
| thần tài | Meowth |
| chí tôn | tối thượng |
| tông sư | bậc thầy Pokémon |
| hảo hán | người dũng cảm |
| thiên hạ | mọi vùng |
| trừ gian | (chỉ có trong cụm dài ở DOI, vd "anh em kết nghĩa"; gặp riêng lẻ thì viết lại câu) |
| trượng nghĩa | (chỉ có trong cụm dài ở DOI, vd "anh em kết nghĩa"; gặp riêng lẻ thì viết lại câu) |
| cường thân | Tĩnh tâm |
| phi phong | ruy băng |
| liên đấu | giải Liên minh |
| công thành | chiếm Gym |
| thành chủ | trưởng Gym |
| thần thú | Pokémon huyền thoại |
| ngựa | thú cưỡi |
| vũ khí | đồ cầm tay |
| giáp trụ | lớp giáp |
| trưởng lão | cố vấn |
| thần hành phù | vé bay Pidgeot |
| tiềm năng đan | kẹo tiềm năng |
| kỹ năng đan | kẹo kỹ năng |
| linh đan | thuốc hồi phục |
| nội đan | lõi năng lượng |
| công lực | sức mạnh |
| luyện công | luyện tập |
| chân khí | năng lượng |
| tâm pháp | bí quyết |
| kinh mạch | thể trạng |
| khinh công | chạy nhanh |
| chưởng môn | người đứng đầu |
| bang phái | đội |
| sư huynh | bạn học |
| sư đệ | bạn học |
| sư muội | bạn học |
| sư tỷ | bạn học |
| sư tỉ | bạn học |
| nguyên soái | chỉ huy |
| các hạ | cậu |
| tiêu cục | trạm giao hàng |
| khách điếm | nhà trọ |
| tửu lâu | quán ăn |
| ma đầu | kẻ xấu |
| yêu quái | Pokémon hung dữ |
| thổ phỉ | kẻ trộm |
| đạo tặc | kẻ trộm |
| sơn tặc | kẻ trộm |
| ngân lượng | xu |
| nguyên bảo | lượng |
| võ đài | sàn đấu |
| tỷ võ | đấu tập |
| tỉ võ | đấu tập |
| luận kiếm | đấu tập |
| đại đao | hạt giống thần |
| lưỡng nhận đao | Lưỡi liềm Scyther |
| kiếm phi kinh thiên | Đại hỏa |
| xuống ngựa | xuống thú cưỡi |
| hộ vệ | Pokémon hỗ trợ |
| tiểu đệ | em |
| thần kiếm | thần hỏa |
| kim bài | huy hiệu lập đội |
| chuyến tiêu | chuyến giao hàng |
| trả tiêu | trả hàng |
| nhận tiêu | nhận hàng |
| đổi tiêu | đổi hàng |
| màu tiêu | màu hàng |
| thế tiêu | thế chỗ hàng |
| con tiêu | kiện hàng |
| trứng ph băng | trứng Articuno |
| chiếm được thành | chiếm được Gym |
| chiến thành | chiến Gym |
| giữ thành | giữ Gym |
| sở hữu thành | sở hữu Gym |
| cổng thành | cổng Gym |
| thành trấn danh | Cao nguyên Indigo |
| quốc chiến | chiến vùng |
| nhà vua | Meowth |
| thổ địa phù | vé bay Pidgeot |
| nhị phẩm | hạng 2 |
| tam phẩm | hạng 3 |
| tứ phẩm | hạng 4 |
| ngọc bội | phiến đá hệ |
| hộ uyển | vòng chân |
| đấu sỹ | dòng Squirtle |
| hệ mộc | hệ Cỏ |
| hệ hỏa | hệ Lửa |
| hệ hoả | hệ Lửa |
| hệ thổ | hệ Đất |
| hệ kim | hệ Thép |
| hệ thủy | hệ Nước |
| hệ thuỷ | hệ Nước |
| kháng hỏa | kháng Lửa |
| kháng hoả | kháng Lửa |
| kháng thủy | kháng Nước |
| kháng thuỷ | kháng Nước |
| kháng mộc | kháng Cỏ |
| kháng kim | kháng Thép |
| kháng thổ | kháng Đất |
| duy ngã độc tôn | Tứ Thiên Vương |
| tuyệt thế vô song | có một không hai |
| nghịch thiên nhi hành | Kẻ Thách Thức Định Mệnh |
| độc bá nhất phương | Bá Chủ Một Vùng |
| chân mệnh thiên tử | Người Được Chọn |
| chân mên thiên tử | Người Được Chọn |
| cửu huyền thất tổ | Truyền Nhân Huyền Thoại |
| hủy thiên diệt địa | Sức Mạnh Hủy Diệt |
| khai thiên thần kiếm | Gươm Aegislash |
| chiến thần bất diệt | Huyền Thoại Bất Diệt |
| long tiễn chấn thiên | Rayquaza Chấn Thiên |
| nhất tiễn phá thiên | Tia Chớp Phá Trời |
| nhất dương tiễn | Tia Mặt Trời |
| dì út | Y tá Chansey |
| bà tám | Kecleon |
| anh bảy | Kangaskhan |
| hắc ngưu | Machamp |
| thiết bì | Bronzong |
| phú ông | Persian |
| phú ồng | Persian |
| nhật thương nhân | Meowth thương nhân |
| hỏa xích | Torkoal |
| hoả xích | Torkoal |
| hai minh | Xatu |
| địa chủ | Persian |
| giáp sư | Bronzong |
| bội châu | Persian |
| lộc phát | Meowth |
| nhất giáp | Golem 1 |
| nhị giáp | Golem 2 |
| tam giáp | Golem 3 |
| tứ giáp | Golem 4 |
| ngũ giáp | Golem 5 |
| nhất ngưu | Tauros 1 |
| nhị ngưu | Tauros 2 |
| tam ngưu | Tauros 3 |
| tứ ngưu | Tauros 4 |
| ngũ ngưu | Tauros 5 |
| quạt ba tiêu | Quạt lá Shiftry |
| gậy như ý | Gậy xương Marowak |
| hỗn nguyên | X Tấn công |
| hoá công | Thuốc săn trùm |
| hóa công | Thuốc săn trùm |
| đoạn cốt | Ether chiến trường |
| hoả dược | Bom lửa |
| hỏa dược | Bom lửa |
| vĩnh kết đồng tâm | trái tim bạc |
| loan phụng hòa minh | trái tim vàng |
| loan phụng hoà minh | trái tim vàng |
| nguyệt ảnh | Ánh Trăng |
| mảnh lan | mảnh Berry Pecha |
| mảnh cúc | mảnh Berry Cheri |
| mảnh trúc | mảnh Berry Rawst |
| mảnh sáo | mảnh Blue Flute |
| mảnh quạt | mảnh Red Flute |
| mảnh sênh | mảnh Yellow Flute |
| mảnh đàn | mảnh White Flute |
| mảnh ngư | mảnh huy hiệu Cascade |
| mảnh tiều | mảnh huy hiệu Rainbow |
| mảnh canh | mảnh huy hiệu Boulder |
| mảnh độc | mảnh huy hiệu Marsh |
| mảnh long | mảnh Rayquaza |
| mảnh lân | mảnh Suicune |
| mảnh quy | mảnh Kyogre |
| mảnh phụng | mảnh Ho-Oh |
| đại bạch thử | Wooloo |
| bạch thử | Sandshrew |
| rồng băng | Dragonite |
| sư tử băng | Glastrier |
| hắc thỏ | Lopunny |
| hắc thố | Lopunny |
| hắc lang | Zoroark |
| bạch mi | Absol |
| thanh tước | Taillow |
| xích cước | Blaziken |
| lôi công | Zapdos |
| bá thiên hổ | Raikou |
| ngọc nữ | Minun |
| kim đồng | Plusle |
| nghê thường | Milotic |
| thần nữ | Gardevoir |
| tiên nữ | Lunala |
| tinh quân | Solgaleo |
| thầy đồ | giáo sư |
| yếm đào | Bellossom |
| áo yếm | áo Bellossom |
| tiên lộc | Xerneas |
| xích dương | Camerupt |
| chiến lang | Lycanroc |
| thuồng luồng | Gyarados |
| miêu quý tộc | Persian |
| miêu mắt xanh | Glameow |
| ngạ khuyển | Houndoom |
| thi khiển | Yamask |
| anh hùng xuất thiếu niên | tuổi trẻ tài cao |
