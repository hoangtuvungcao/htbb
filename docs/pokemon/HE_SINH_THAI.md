# Hệ sinh thái game: Huyền Thoại Bảo Bối

> Cờ `sv.he_sinh_thai` (mặc định `0` = game y như trước). Đọc qua `com.htbb.pokemon.HeSinhThai.bat()`.
> Cờ được đọc sớm từ `server.ini` khi lớp được nạp (giống `CheDoPokemon`, commit 915af4e), và `TeamServer` đọc lại.
>
> Số liệu gốc: `src/com/htbb/pokemon/HeSinhThai.java` (hằng số), `KyNangSong`, `VatLieuSinhThai`, `NhaTre`.
> Dữ liệu thế giới: `sql/pokemon_he_sinh_thai.sql`, sinh bởi `tools/pokemon/sinh_sql_he_sinh_thai.py`.
> Kiểm thử: `test/KiemHeSinhThai.java` (~380 kiểm tra). Mô phỏng: mô-đun `HeSinhThaiMoPhong` trong
> `tools/pokemon/MoPhongKinhTe.java`.
> Ngân sách kinh tế tuân theo [`THIET_KE_LAU_DAI.md`](THIET_KE_LAU_DAI.md) mục 2. Đơn vị tiền là **‰H**, tức phần nghìn
> "giờ thu nhập" `ThongSoLauDai.xuMoiGio(cấp)`.

## 0. Mục tiêu: mỗi kiểu người chơi có việc dài hạn

| Kiểu người chơi | Việc chính | Mục tiêu dài hạn |
|---|---|---|
| Chiến đấu | Đánh quái theo thời tiết (+10% hệ hợp trời), bùng phát, quái hiếm, đua, giải tuần | Shiny, album, quái hiếm |
| Sưu tầm | Nghiên cứu Pokédex (6 loại nhiệm vụ mỗi loài), album, hoàn thành vùng | 14 vùng, 4 mốc album, danh hiệu |
| Xã hội | Tưới vườn bạn, đố vui thế giới, đua, đơn đặt hàng | "Bạn của muôn loài", điểm thân thiện |
| Chế tác / thương nhân | Câu cá, khảo cổ, nấu ăn, chế thuốc, chợ sinh thái, đơn đặt hàng, nhân giống trứng IV cao | Kỹ năng cấp 50 (≈ 5 tháng một kỹ năng), trứng phẩm chất S, hoá thạch |
| Chơi nhẹ | Thể lực sinh hoạt 100/ngày, tích được 2 ngày; vườn berry | Không bị phạt khi nghỉ |

## 1. Vòng liên kết (mọi hoạt động nuôi hoạt động khác)

```
                 thời tiết / ngày-đêm / mùa (theo vùng, theo ngày)
                    │ đổi loài khi quái hồi sinh, +-10% sát thương hệ
                    ▼
 ĐÁNH QUÁI ──bước──► NHÀ TRẺ / TRỨNG ──nở──► PET (item thật, IV, shiny)
   │  ▲  └─nghiên cứu (đêm, thời tiết, hiếm)──► POKÉDEX ─► VÙNG ─► danh hiệu, +0,5%/vùng
   │  │                                           ▲
   │  └── thuốc HP/MP ◄── CHẾ THUỐC ◄── berry ◄── VƯỜN (trồng berry) ◄── bạn bè tưới (+5%)
   │                         ▲                                │
   │                         │ vảy cá, ngọc trai              ▼
   │                      CÂU CÁ ──gặp cá──► Pokédex/album    NẤU ĂN ─► Bánh Poké (+thể lực),
   │                         ▲                                │          mồi thơm (cá/đồ hiếm x2)
   │                         └───────── mồi thơm ◄────────────┘
   │
   └─ đá tiến hoá ◄── 12 mảnh ◄── KHẢO CỔ (hang/núi/sa mạc/tuyết) ──► hoá thạch ──► pet cổ đại
                                        │                          └──► trang trí VƯỜN
   CHỢ SINH THÁI + ĐƠN ĐẶT HÀNG: mọi nguyên liệu, trứng và trang trí đổi tay (thuế 6% + phí 1% bị đốt)
   NPC mua lại: sàn 40% giá, trần 0,1 giờ thu nhập mỗi ngày
```

## 2. Thế giới (mục 1 yêu cầu): `SinhThaiTheGioi`, `HeSinhThaiGame.khiHoiSinh`

- **Ngày/đêm** theo giờ máy chủ: 19:00–05:59 là đêm.
- **Thời tiết theo ngày cho mỗi vùng sinh thái.**
  - Vùng là cột `loai` của `thiet_ke_map.csv`, nạp vào `data_hst_vung`.
  - Có 6 kiểu: quang, nắng, mưa, tuyết, bão cát, sương mù. Tính tất định bằng SplitMix64(ngày, vùng), nên không cần lưu.
  - Trọng số thay đổi theo vùng và mùa. Ví dụ: tuyết ≈ 70% ở vùng tuyết; hang và trong nhà luôn quang; mùa hạ tăng nắng; mùa đông ở hồ và núi thì mưa chuyển thành tuyết.
- **Ảnh hưởng của thời tiết:**
  - Hệ hợp trời sinh ra nhiều gấp 3 (mưa: Nước/Điện; nắng: Lửa/Cỏ; tuyết: Băng; bão cát: Đá/Đất/Thép; sương mù: Ma/Siêu linh). Ban đêm, hệ Ma/Bóng tối/Độc cũng gấp 3.
  - Sát thương chiêu: mưa thì Nước +10%, Lửa −10%; nắng thì Lửa +10%, Nước −10%; tuyết thì Băng +10%; bão cát thì Đá +10%; sương mù thì Ma +10%.
  - Mốc trong `CharChiSo.attackDam` chỉ áp cho đòn của người chơi.
- **Thay loài khi hồi sinh.** Mốc ở `Monster.update`, ngay trước khi đặt `hp = maxhp`.
  - Không đổi số lượng, vị trí hay id quái. Tải máy chủ giữ nguyên: mỗi lần hồi sinh chỉ một phép chọn.
  - 15% số lần hồi sinh sẽ đổi sang một ứng viên trong `data_hst_thay_quai`. Ứng viên có cùng dải cấp ±4, tối đa 6 loài.
  - Một phần ba số loài là **loài di cư**: chỉ có mặt 6 tháng liên tiếp mỗi năm (`mat_na_thang`).
  - **Quái hiếm**: cấp +3..+8, trọng số 2, chỉ ra trong một kiểu thời tiết và có thể chỉ ban đêm. Khi xuất hiện thì báo kênh thế giới, mỗi loài tối đa 1 lần mỗi 10 phút.
  - Client không cần sửa: quái chết thì client xoá actor; khi hồi sinh, client gửi lệnh 7 xin thông tin và nhận `monster_type` mới.
  - **Không đổi:** loài là mục tiêu nhiệm vụ **chính** (`Map.allMainQuest`), boss, quái vật thể (83, 85–89), quái vận tiêu, bản đồ không phải lớp `real.Map` (MOBA, liên đấu…), map 40 và 42.
- **Bùng phát:**
  - 4 khung mỗi ngày (11h, 17h, 20h, 22h), mỗi khung 1 giờ.
  - Mỗi khung chọn một cặp (tuyến, loài của chính tuyến đó) từ `data_hst_bung_phat`. Loài phải có ít nhất 3 con trên lưới; 71 cặp.
  - Trên tuyến đó, 40% số lần hồi sinh đổi thành loài bùng phát.
  - Có thông báo khi bắt đầu và khi kết thúc.
- **Hiệu ứng thời tiết trên client** dùng gói có sẵn `-39` (`AnimateEffect`: 0 mưa, 1 lá rơi, 2 tuyết).
  - Server gửi riêng cho từng người khi vùng của họ đổi thời tiết, kiểm tra mỗi 15 giây.
  - Không gửi khi đang có sự kiện thời tiết toàn máy chủ (`Map.weather`).

## 3. Kỹ năng sống (mục 2): `KyNangSong`, `VatLieuSinhThai`

- 5 kỹ năng, mỗi kỹ năng cấp 1..50.
  - EXP lên cấp L là `40 × 1,105^L`. Tổng tới cấp 50 khoảng 55.000 EXP, tương đương khoảng 5 tháng dùng hết thể lực cho **một** kỹ năng.
- **Thể lực sinh hoạt**:
  - 100 mỗi ngày, phần chưa dùng tích tối đa 200; nghỉ từ 2 ngày trở lên thì đầy.
  - Riêng biệt với năng lượng huấn luyện, nên không lấy mất giờ EXP của người chơi.
  - Tốn 2 khi câu, 4 khi đào, 3 khi nấu/chế. Thu hoạch vườn không tốn.
- **Phí nguyên liệu bắt buộc** (hố hút xu): mồi câu 2‰H, xẻng 4‰H, bếp 5‰H.
- **Câu cá** (gõ `cauca` hoặc vào menu). Luật của người dùng: **chỉ câu được ở hồ**.
  - **Hồ** là vùng nước thật liên thông (4 hướng) nằm cách viền bản đồ hơn 2 ô (`HO_CACH_MEP`), có từ 6 ô đến 40% diện tích bản đồ.
    - Biển, sông chảy ra mép bản đồ, bờ biển và vũng nước nhỏ đều **không** phải hồ.
    - Ô nước thật là tile tên `W*`/`sen` có kiểu chặn (2) trong `bang_tile_moi.csv`.
    - Phân loại một lần khi nạp bản đồ: `BanDoSinhThai.nhanHo`.
  - Phải đứng cách ô hồ tối đa 2 ô. Nếu không thì trả lời đúng câu "Chỉ có thể câu cá ở hồ.", kèm "Điểm câu gần nhất: (x,y)" nếu bản đồ có hồ.
  - **Hồ lớn** (≥ 60 ô) có đủ bảng cá, kể cả cá hiếm (giá ≥ 100‰: Feebas, Dratini…). **Ao** nhỏ hơn thì chỉ có cá thường.
  - Hồ trên bản đồ biển/tàu được tính là đầm phá: có cả cá nước mặn. Hiện các bản đồ biển **không có hồ**, nên cá nước mặn (Tentacool, Krabby, Horsea, Shellder, Staryu…) tạm thời không câu được. Vỏ sò đã chuyển sang ra được cả ở nước ngọt (trai hồ) để nhiệm vụ cũ còn làm được.
  - **Điểm câu theo bản đồ** (xuất ra để client đánh dấu và để nhiệm vụ tham chiếu):
    - `docs/pokemon/diem_cau_ca.csv` và bảng `data_hst_ho_cau(map_id, ho_id, so_o, x, y)`, cùng sinh bởi `tools/pokemon/sinh_sql_he_sinh_thai.py`.
    - Hiện có 136 điểm trên 23 bản đồ; mỗi hồ tối đa 4 điểm là ô đi được sát hồ. Không tính map 40/42.
    - Test đối chiếu Java (lúc chạy) với CSV trên **mọi** bản đồ.
  - Có 21 kết quả câu được: rác, vỏ sò, rong, Oran, 15 loài cá (từ Magikarp đến Dratini/Lapras), ngọc trai.
  - Câu được cá thì ghi "đã gặp" vào Pokédex (với loài có template quái) và nghiên cứu "Câu được".
  - Cá hiếm (giá ≥ 100‰) vào album; loài ≥ 250‰ thì thông báo kênh thế giới.
- **Trồng berry**: dùng lại vườn có sẵn của game gốc (`SeedItem`, `OfflineMap.doPlant`).
  - Khi bật cờ, tên hạt giống đổi thành 8 loại berry.
  - Mỗi lần thu hoạch (mốc `Char.doHarvest`) cho thêm `2 + hạng` berry vào túi sinh thái. Bạn bè tưới: +5% mỗi lần, tối đa +25%.
- **Khảo cổ** (gõ `dao`): ở hang, núi, sa mạc, tuyết.
  - Mỗi bản đồ có 6 điểm đào mỗi ngày, nằm trên ô đi được, tất định theo (ngày, map).
  - Đứng xa điểm thì "máy dò" chỉ hướng và số bước, kèm toạ độ chính xác dạng `Điểm đào: (x,y)`. Client đọc dạng này để đánh dấu ô, **không đổi định dạng**.
  - Đào ra: đá cuội, xương cổ, 7 loại mảnh đá tiến hoá, vảy tim, đá sao rơi, 5 loại hoá thạch (1–2%).
- **Công thức (30):**
  - Nấu ăn: Bánh Poké (+30 thể lực, tối đa 2 lần/ngày), mồi thơm, súp berry (+50% EXP kỹ năng trong 20 lần), cà ri cá.
  - Chế thuốc: tối đa **5 mẻ/ngày**. Thuốc nằm trong túi; khi "Dùng" thì đổi ra 10 bình HP/MP thật (`PT:1..5`).
  - Khảo cổ: ghép 12 mảnh thành **đá tiến hoá thật** (`ITEM:960..966`); phục hồi hoá thạch thành **pet** (`ITEM:694/704/689/699/709`); 5 món trang trí vườn.
  - Không vật liệu nào chỉ để bán NPC (`tools/pokemon/kiem_item_mo_coi.py`): 4 công thức 26–29 dùng rác trôi, ngọc trai lớn, cá biển (Tentacool, Horsea, Shellder, Krabby, Qwilfish, Corsola), cá hiếm (Feebas, Dratini, Lapras), đá sao rơi, vảy tim → Lẩu hải sản (ra Cà ri cá), Mồi thơm biển, trang trí Bể san hô (115), Tượng rồng Dratini (116). Giá trị đầu ra ≤ nguyên liệu.
- **Vật liệu nằm trong "Túi sinh thái"**, lưu trong `tob_hst_nhan_vat`, **không phải** `data_item`. Vì vậy:
  - không cần template hay gói tin client mới;
  - không đụng dải id 1001–1199 của nhóm nội dung.
  - Chỉ thành phẩm cuối (thuốc, đá, pet) mới ra vật phẩm thật.
  - Mỗi chồng có **bậc 1..5** theo cấp người lấy. Giá tham chiếu = giá ‰ × `xuMoiGio(CAP_BAC[bậc])`, tính theo giờ chơi.

## 4. Nhà trẻ và trứng (mục 3): `NhaTre`

- Gửi 1–2 pet **đang sở hữu**: pet đi theo hoặc item pet trong hành trang, nhận diện qua `PetPokemon`.
  - 12 dòng ra trứng: Hoothoot, Dratini, Cleffa, Zubat, Rufflet, Ponyta, Pachirisu, Delibird, Litleo, Growlithe, Deerling, Bidoof, kể cả dạng tiến hoá.
- Phí **50‰H mỗi ngày** (hố hút). Chủ hạ 1 quái = 1 bước; 300 bước được 1 trứng; tối đa 2 trứng/ngày; túi chứa 6 trứng.
  - Bước tính bằng quái hạ được, mà quái hạ đã bị năng lượng huấn luyện giới hạn, nên treo máy không nhanh hơn.
- **Trứng nở** sau 250–800 bước, tuỳ loài.
- **IV** (HP/công/thủ, mỗi chỉ số 0..31):
  - Mỗi chỉ số lấy của cha hoặc mẹ; 15% đột biến ±1..3.
  - Pet không nở từ trứng có IV "hoang dã" 0..15, tất định theo người chơi.
  - IV tốt nhất của mỗi dòng được giữ lại để nhân giống chọn lọc.
- **Shiny** 1/512; pity: trứng thứ 200 không shiny thì chắc chắn shiny. Shiny được album, danh hiệu "Thợ săn shiny" và thông báo thế giới.
- **Phẩm chất S** (tổng IV ≥ 84) nở ra **pet cấp 2** nếu loài có cấp 2. Đây là lợi ích kinh tế (bỏ qua 1 lần nâng pet), không cộng chỉ số chiến đấu.
- Trứng là **hàng hoá**: bán trên chợ, giá = tham chiếu × (1 + IV/93), shiny ×5. NPC không mua trứng.

## 5. Chợ, đơn đặt hàng, hộp thư (mục 4): `ChoSinhThai`, `DuLieuHeSinhThai`

| Khoản | Giá trị | Ghi chú |
|---|---|---|
| Thuế bán / phí đăng | 6% (đốt) / 1% (không hoàn) | Khớp `THIET_KE_LAU_DAI` 2.4 |
| Giá rao | chọn mức x1 / x1,5 / x2 / x3 giá tham chiếu, trần x5 | Không cần ô nhập chữ; chặn rửa tiền bằng giá ảo |
| NPC mua lại | 40% giá tham chiếu, **trần 0,1 giờ thu nhập/ngày** | Không mua trang trí, trứng |
| Tin bán / đơn | tối đa 8 / 4 mỗi người, hết hạn sau 3 ngày | Hết hạn: hàng hoặc ký quỹ về hộp thư |
| Đơn đặt hàng | Ký quỹ giá × số + 1%; người bán giao dần, nhận tiền trừ thuế | Huỷ: hoàn phần chưa giao |

Danh mục chợ: cá, berry, khoáng, hoá thạch, món ăn, thuốc, trứng, trang trí.

Mọi thao tác tranh chấp dùng `UPDATE … WHERE trang_thai = 0` rồi kiểm số dòng, nên chỉ một bên thắng:
- Thứ tự mua: trừ xu → chốt tin → nếu thua thì hoàn xu.
- Người bán offline: tiền vào `tob_hst_hop_thu`.

Đội và người hướng dẫn (sư phạm): đơn đặt hàng thay cho "quỹ vườn đội". Chưa nối riêng với `tob_clan` (mục 10).

## 6. Giải trí (mục 5): `MiniGameSinhThai`, `HeSinhThaiGame.DoVui/Dua`

- **"Ai là Pokémon?"**:
  - Chạy lúc 12:30 và 21:30 trên kênh thế giới.
  - Gợi ý: hệ, số chữ cái, chữ đầu.
  - Trả lời bằng chat. Câu trả lời đúng **không** phát cho người khác. Người đúng đầu tiên nhận 3 lượng khoá; đúng 20 lần được danh hiệu.
- **Đua Pokémon** (gõ `dua`):
  - Đăng ký từ 18:50; xuất phát 19:00 tại map 18 (tuyến Goldenrod).
  - 5 mốc trên ô đi được, trái → phải, tính từ bố cục bản đồ. Server đọc vị trí người chơi mỗi giây.
  - Thưởng top 3: 5/3/2 lượng khoá. Thắng 5 lần được "Vua tốc độ".
  - Tận dụng di chuyển và thú cưỡi có sẵn.
- **Giải đấu tuần**:
  - Đăng ký qua menu. Cây đấu loại trực tiếp (có miễn đấu, hạt giống theo cấp) công bố thứ Bảy 20:00, hoặc bằng lệnh GM `hst giaidau`.
  - **Kết quả trận**: mốc `MapLienDau` (khi một người hạ người khác) gọi `HeSinhThaiGame.ketQuaTranPvp`. Chỉ ghi khi hai người đang gặp nhau ở vòng hiện tại; xong vòng thì tự sang vòng sau.
  - Nhà vô địch nhận 20 lượng khoá, danh hiệu và thông báo thế giới.
- **Album**: chụp quái hiếm, bùng phát (1 ảnh/khung/người), cá hiếm, trứng shiny, hoá thạch.
  - Mốc 10/25/50/100 ảnh: 10/20/30/50 lượng khoá và danh hiệu.

## 7. Vườn nhà, housing-lite (mục 6)

- **Vườn berry**: dùng vườn sẵn có trên bản đồ cá nhân (`OfflineMap`) cho phần trồng.
- **Trang trí**:
  - 5 món chế từ khảo cổ, chỉ để trang trí (không cộng chỉ số).
  - Đặt hoặc cất qua menu. Mỗi món có điểm trang trí.
- **Bạn bè**: chọn trong danh sách bạn đang online.
  - Có thể tưới (5 lần/ngày mỗi người cho, 5 lần/ngày mỗi vườn nhận) hoặc **thăm** (xem trang trí, danh hiệu, điểm thân thiện).
  - Điểm thân thiện ≥ 50 cho danh hiệu "Bạn của muôn loài".

## 8. Sưu tầm (mục 7): `SuuTamSinhThai`

- **6 nhiệm vụ nghiên cứu mỗi loài**: câu được, ấp nở, hạ trong thời tiết ưa thích, hạ ban đêm, gặp khi hiếm/bùng phát, nở shiny.
  - Chỉ áp những nhiệm vụ hợp với loài.
  - Mỗi nhiệm vụ cho 2 lượng khoá. Loài "nghiên cứu xong" khi làm đủ 2 nhiệm vụ.
- **Vùng**:
  - `data_hst_vung_loai` gồm 71 cặp (vùng, loài quái); thêm cá cho hồ (nước ngọt) và biển (nước mặn), lấy lúc chạy từ `KyNangSong`.
  - Hoàn thành vùng: 20 lượng khoá, danh hiệu, và **+0,5% chỉ số vĩnh viễn mỗi vùng, trần 3%**. Mức này nằm trong ngân sách sức mạnh (tinh thông +10%).
  - Chỉ số hiện **mới ghi nhận**, chưa cộng vào `CharChiSo` (mục 10).

## 9. Ngân sách và mô phỏng

Mô-đun `HeSinhThaiMoPhong` có RNG riêng. Cách mô hình:
- Kỹ năng mở từ cấp 10.
- Tỉ lệ tham gia: nhẹ 60%, vừa 50%, cày 35%, bot 100% (bot macro câu cá).
- Thể lực dùng mỗi ngày: 60 / 80 / 100.
- Thời gian sinh hoạt **trừ vào** thời gian đánh quái.
- Giá trị hàng lấy từ `KyNangSong.kyVongCauCa`.
- Giả định **xấu nhất cho lạm phát**: mọi người bán hết hàng dư cho NPC tới trần.
- 30% hàng giao dịch giữa người chơi (thuế 7%); 30% người tham gia gửi nhà trẻ.

Kết quả (730 ngày, dân số đầy đủ):

| Chỉ số | Kết quả |
|---|---|
| 12 mục tiêu `THIET_KE_LAU_DAI` | **12/12** (xu ra/vào 99,3%, xu thực +3,3%/tháng, D1/D7/D30 68/49/29%) |
| 6 mục tiêu nạp tiền | 6/6 |
| NPC mua lại / tổng nguồn xu | **1,53%** (mục tiêu ≤ 3%) |
| Hố hút của hệ sinh thái / nguồn của hệ sinh thái | **118%** (nguyên liệu, thuế, nhà trẻ). Hệ sinh thái hút ròng, không làm phát |
| Giờ sinh hoạt trung bình | 0,21 giờ mỗi lượt |

Test chặn ngân sách:
- Phí nguyên liệu cả ngày ≥ trần NPC (câu: 100‰ ≥ 100‰; đào: 100‰).
- Giá trị hàng tối đa mỗi ngày ≤ 4 giờ (giá trị hàng, không phải xu).
- Sát thương thời tiết ±10%.
- Chỉ số vùng ≤ 3%.

Lượng khoá thêm khoảng 1/ngày cho người tham gia (≤ 200 ngày đầu), sau đó 0,3/ngày.

## 10. Còn thiếu / một phần

| Hạng mục | Trạng thái |
|---|---|
| Chỉ số vĩnh viễn theo vùng (+0,5%/vùng) | Đã ghi nhận trong hồ sơ; **cần mốc** `CharChiSo` (vùng của nhóm GĐ2) đọc `SuuTamSinhThai.chiSoVinhVien(bitCount(vungXong))` |
| IV ảnh hưởng chỉ số pet | IV chỉ quyết định phẩm chất, nở cấp 2 và giá trứng; chưa cộng vào chỉ số `Pet` |
| Giải đấu tuần | Đã nối với PvP `MapLienDau`. PvP ở `Region`/`MapDanhNhau` (thang Elo của GĐ2) **không** tính cho giải |
| Vườn đội (clan) | Chưa làm; đơn đặt hàng thay cho nhu cầu cung ứng tập thể |
| Thăm nhà bằng bản đồ riêng (instanced) | Chưa. Hiện "thăm" bằng menu; vườn berry vẫn là bản đồ cá nhân của game gốc |
| Hoá thạch → pet Omanyte/Kabuto/Aerodactyl… | Đang tạm ra pet sẵn có (Dratini, Zubat…). Nhóm nội dung thêm item pet thì sửa chuỗi trong `KyNangSong.CONG_THUC` 16–20 |
| Chạy server thật + bot | Đã chạy: instance riêng (cổng 19229, CSDL `htbb_db_hst`), 45 bot, kịch bản GM. Xem mục 13 |
| Sát thương thời tiết cho đòn quái → người | Chưa (chỉ áp đòn của người) |

## 11. Client (cho nhóm client; không sửa htbb-client)

Mọi thứ trên **chạy với client J2ME/Unity hiện tại**: menu NPC (`createMsgMenu`), hộp thông báo (lệnh 37), chat (lệnh 27), gói thời tiết `-39`, gói thông tin quái `7`. Các móc dưới đây chỉ để đẹp hơn:

| Móc | Đề xuất |
|---|---|
| Ngày/đêm | Lớp phủ tối dần theo giờ máy chủ (19h–6h), client tự tính theo giờ |
| Sương mù, nắng gắt | **Đã làm.** Server gửi `-39` id **6** (sương mù) và **7** (nắng gắt). Client J2ME/Android đã có móc; client cũ bỏ qua id lạ |
| Phao câu | Hiệu ứng cần câu và phao khi gõ `cauca` |
| Điểm câu | Đánh dấu từ `diem_cau_ca.csv` / `data_hst_ho_cau` |
| Điểm đào / mốc đua | Định dạng chữ cố định cho client đọc: `Điểm đào: (x,y)`; đua: `XUẤT PHÁT! Mốc: (x,y) → (x,y) …`, `Qua mốc k/n. Mốc tiếp: (x,y)`, `Về đích! Thời gian t giây.` |
| Tab "Túi sinh thái" | UI lưới thay cho menu danh sách (đang tối đa 40 dòng) |
| Danh hiệu sinh thái | **Đã làm.** Đeo/tháo ở Sưu tầm > Danh hiệu. `Char.idEffDanhHieu = 9600 + chỉ số trong HoSoSinhThai.DANH_HIEU` (9600..9615). Đã kiểm không trùng: danh hiệu cũ 8767..8957, hiệu ứng 8700..8999, skin 13000.. |

## 12. Vận hành

- **Bật**:
  1. Chạy `sql/pokemon_he_sinh_thai.sql` (đã nằm trong `tools/pokemon/xuat_sql_day_du.sh`).
  2. Đặt `sv.he_sinh_thai=1`.
  3. Khởi động lại.
- **Lệnh GM** (chat):
  - `hst dovui`, `hst dua`, `hst bungphat`, `hst theluc`, `hst napthegioi`
  - `hst thoitiet <vùng> <tt>`: ghi đè đến hết ngày.
  - Chỉ dùng khi thử nghiệm: `hst giaidau`, `hst vatlieu <mã> <số> [bậc]`, `hst xu <số>`, `hst danhhieu <i>`.
- **Lối vào menu**:
  - Mục "Hệ sinh thái" ở cuối menu NPC đổi thưởng (-90).
  - Hoặc gõ `hst` trong chat. Phím tắt: `cauca`, `dao`, `dua`.
  - NPC ảo: `-528`.
- **Mốc trong `src/real`** (đánh dấu `// he sinh thai:`):
  - `Monster.update` (hồi sinh) và `Monster` (sau `PokedexPokemon.khiHaQuai`)
  - `MapLienDau` (kết quả trận giải đấu tuần)
  - `CharChiSo.attackDam`
  - `MapChat.doChat`
  - `Char.doHarvest`
  - `MapDoMenu`
  - `RequestNPCInfoHandler` (-90)
  - `TeamServer` (đọc cờ)

## 13. Chạy thật (2026-10-18)

Instance riêng: cổng 19229, thư mục chạy riêng, CSDL `htbb_db_hst` (bản sao `htbb_day_du.sql` + `pokemon_he_sinh_thai.sql`). Cờ `sv.che_do_pokemon=1`, `sv.he_sinh_thai=1`.

Đã chạy 3 lượt 8–10 phút với `sv.lau_dai=0` và 1 lượt 5,5 phút với `sv.lau_dai=1`. Mỗi lượt có 45 bot BotTai, cộng một bot kịch bản (lệnh GM qua chat, đi menu bằng gói `-30`).

| Kiểm tra | Kết quả |
|---|---|
| Ổn định | 0 exception, 0 lỗi SQL, 0 rơi kết nối trong cả 4 lượt. RSS 650–930 MB, CPU 40–57% (máy dev) |
| Thay loài khi hồi sinh | 484 lần hồi sinh: 9 lần đổi loài, 4 quái hiếm, 41 lần bùng phát. Có thông báo "Một Litwick hiếm vừa xuất hiện ở Viridian (mưa)!" và ảnh album. Tỉ lệ đổi chung thấp vì 8 loài là mục tiêu nhiệm vụ chính và quái map 1/2 ít ứng viên |
| Thời tiết | `hst thoitiet` → gói `-39` id 0/2/6/7 gửi theo vùng khi người chơi đổi map |
| Câu cá | Ở biển (map 2): "Chỉ có thể câu cá ở hồ." Ở hồ map 3 (điểm 37,7 trong CSV): Magikarp/Poliwag/Goldeen, vỏ sò, rong; lên cấp kỹ năng; nghiên cứu Pokédex |
| Khảo cổ | Đào được mảnh đá; đứng xa thì nhận "Máy dò rung: … Điểm đào: (22,34)" |
| Nấu / chế | Qua menu: Bánh Poké, Thuốc xịt |
| Chợ, tranh mua | Hai bot bấm mua cùng lúc một tin: đúng 1 người "Đã mua", người kia "Người khác vừa mua mất tin này". Người bán nhận 334 xu = 355 − 6% |
| Đố vui | Câu hỏi đúng định dạng; người trả lời đúng đầu tiên được thưởng và thông báo thế giới |
| Đua | Hiện đúng định dạng "XUẤT PHÁT! Mốc: (x,y) → …", "Qua mốc k/n. Mốc tiếp: (x,y)", "Về đích! Thời gian 14.7 giây.", và bảng kết quả |
| Giải tuần | Đăng ký 2 người + `hst giaidau` → "vòng 1: tai006 vs tai007". Trận PvP thật ở Liên đấu **chưa** chạy bằng bot |
| Danh hiệu | Đeo qua menu, `idEffDanhHieu` = 9605 ("Nhà khảo cổ") |
