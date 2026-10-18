# Kiểm thử tự động bằng bot (18/10/2026)

Bot nói đúng giao thức client (khóa -40, khung `cmd/size/data`) để chơi thử mọi tính năng, mỗi kịch bản in
`PASS / FAIL / BO QUA` kèm bằng chứng (thông báo server, số dư, dòng CSDL). Chạy trên **máy chủ thử riêng**
(cổng 19169, login 8044, CSDL `htbb_db_bot` trên NovaDB) — không chạy kịch bản GM trên server thật.

## 1. Công cụ

| Tệp | Việc |
|---|---|
| `tools/pokemon/kiem_tai/BotChucNang.java` | Khung bot: kết nối, giải mã gói (37/-26/27/-30/-31/-32/3/4/7/12/13/15/16/73/105…), chờ theo điều kiện, menu theo chữ, đọc bản đồ `cMap_pokemon` (ô đi được, cổng), tìm đường BFS, truy vấn CSDL qua `mysql`, tự giới hạn 200 gói/5 giây |
| `KichBanDS.java` | Đăng nhập, rời sảnh, di chuyển, đánh quái + EXP + Pokédex, tiến hóa giữa trận, kỹ năng (mọi ô, hồi chiêu), chết/hồi sinh, rớt kết nối |
| `KichBanDS2.java` | Đi **mọi cổng của mọi map** (đứng trước cổng như client, gửi lệnh 12 theo bảng vị trí 105), kiểm ô đến đi được / không kẹt / tới được cổng khác |
| `KichBanDS3.java` | Thuốc, shop NPC (trong nhà), shop lượng (85), trang bị mọi ô, nâng cấp +1..+15 (bùa, pity, nguyên liệu), ngọc (mua/bán/hợp thành/đục lỗ/khảm), nghiền, giao dịch, chợ gian hàng (thuế), thú cưỡi |
| `KichBanDS4.java` | Chat (bản đồ, riêng, loa, chống spam), bạn bè, tổ đội + chia EXP, bang hội (tạo/mời/chat/quỹ/rời/giải tán), Sổ tay (mọi mục), nhiệm vụ (3 khe phụ, ngày, tuần tối đa 2, câu đố, ẩn ban đêm, nhánh lựa chọn, chính), Hầm ngục, Gym, PvP xếp hạng (Elo), giftcode, NPC đổi thưởng/Pokédex |
| `KichBanDS5.java` | Nhân vật (tên sai, tên GM, tối đa 3, xóa + khôi phục), boss, hệ sinh thái (mọi mục), sự kiện, chiếm Gym (thành) |

```sh
# máy chủ thử riêng (server.ini sao chép, cổng/CSDL khác, sv.adminAccounts gồm kt001..kt100; kt101..kt120 là người thường)
javac -encoding UTF-8 -d /tmp/bot tools/pokemon/kiem_tai/BotChucNang.java tools/pokemon/kiem_tai/KichBanDS*.java
java -cp /tmp/bot -Ddb=htbb_db_bot -Dtu=21 -Dden=60 BotChucNang 127.0.0.1 19169 kt all
# server thật (không GM, tài khoản tai040..tai050, chỉ đọc CSDL):
java -cp /tmp/bot -Dgm=0 -Ddb=htbb_db -Dtu=40 -Dden=50 BotChucNang 127.0.0.1 19129 tai dang_nhap roi_sanh kham_pha su_kien
```

Tham số: `-Dtu/-Dden` dải tài khoản GM dùng vòng; `-DtuThuong/-DdenThuong` dải người thường (mặc định 101..120);
`-DbotCong=N` số bot song song cho `cong_tat_ca`; `-Dra=<tệp>` ghi kết quả.

## 2. Kết quả (máy chủ thử, mã nguồn 18/10 tối)

Chạy `all`: **144 PASS, 4 FAIL, 3 BỎ QUA**; 4 FAIL đều đã sửa (bug server hoặc bot) và chạy lại PASS:

| Nhóm | Kịch bản (số kiểm) | Kết quả |
|---|---|---|
| Tài khoản | dang_nhap (5), nhan_vat (5), mat_ket_noi (2) | PASS |
| Bản đồ | roi_sanh, di_chuyen (4), **cong_tat_ca: 891 cổng / 88 map** | PASS (881/881 cổng sau sửa; cổng map 227 không có trong bảng 105 → client bỏ qua sau sửa) |
| Chiến đấu | danh_quai (5), ky_nang (4), tien_hoa (2), chet_hoi_sinh, boss, gym (3), pvp (3) | PASS (boss GM `addboss` không phát vị trí → BỎ QUA) |
| Vật phẩm | thuoc (5), shop_npc (6), shop_luong (3), trang_bi (6), nang_cap (5), ngoc (5), phan_ra (3), thu_cuoi (3) | PASS |
| Kinh tế | giao_dich (4), cho (3: thuế 6% + phí 1% → nhận 9300/10000) | PASS |
| Xã hội | chat (5), ban_be (2), to_doi (5), bang_hoi (8) | PASS |
| Hệ thống dài hạn | so_tay (12), nhiem_vu (6), nhiem_vu_chinh (2), ham_nguc (5), giftcode (4), pokedex/đổi thưởng (3), su_kien, chiem_thanh (4), he_sinh_thai (12) | PASS (câu đố: bảng ngày của nhân vật không rút câu đố → BỎ QUA hôm đó) |
| Server thật | 8 kiểm nhẹ (tai040–tai042) | PASS — nhưng nhân vật mới **vẫn không đánh được** (chiêu 0 = 0) cho tới khi deploy bản sửa #1 |

**Tải (máy chủ thử, 100 bot, 20 GM đánh quái + 80 đi lại/chat):** chạy 10,5 phút (máy bị dừng do tải chung
cả máy lên 255, không phải do server): 100/100 vào game, 1 rớt kết nối, 1,12 triệu gói nhận, 7.511 quái hạ,
54 lần tiến hóa; RSS server 410–630 MB, CPU ~50%, **~3.300 luồng** (nhiều — đã báo nhóm tối ưu hiệu năng).
Log lỗi trong lúc tải: 0. Lỗi trong cả ngày chạy kịch bản: 3 NPE chỉ khi GM `mto` vào map đặc biệt (Moba 40,
Vạn tiên trận 208–210, map bang 206) — đường GM, ghi ở mục 4.

**Giao diện (MicroEmulator trên Xvfb :95, `ui/tour2.sh`):** 240×320: đăng nhập → chọn nhân vật → vào game →
các nhóm menu chính (Cài đặt, Bản thân: hành trang/kỹ năng/tiềm năng, bản đồ lớn…), 0 exception trong log
emulator. 176×220 và 320×240 chưa chạy xong (máy bị dừng giữa chừng).

**Test server (`test/`):** các kiểm liên quan thay đổi của đợt này (cửa hàng sự kiện, `doBuySpecialItem`…) đều
đạt; 33/8975 kiểm hỏng lúc chạy thuộc phần các nhóm khác đang sửa dở (vật phẩm mồ côi, nạp tiền, tăng tốc,
chữ cấm) — không do các sửa đổi dưới đây.

## 3. Lỗi đã sửa

| # | Lỗi (bot bắt được) | Sửa |
|---|---|---|
| 1 | **Nhân vật mới không đánh được quái**: chiêu 0 cấp 0 (game gốc mở qua nhiệm vụ cũ số 8 / vào lại từ cấp 3), chuỗi nhiệm vụ mới 901+ không có nhiệm vụ 8 nhưng nhiệm vụ 903 bắt hạ 20 quái ở cấp 2 → `doAttackMonster` bỏ qua mọi đòn | `CharNapDuLieu`: khi `sv.lau_dai` bật, mở chiêu 0 ngay khi nạp nhân vật. **Cần deploy server thật** (đã xác nhận tai042 trên server thật: chiêu toàn 0) |
| 2 | **Shop lượng (85) chặn hẳn vé EXP, thuốc, kẹo** ("chỉ đổi bằng lượng khoá" nhưng không có chỗ đổi; cả 126–128 vốn trả lượng khoá cũng bị chặn) | `DatabaseLauDai.chanMuaShop` cho qua mục `tien_moi=2`; `doBuySpecialItem` đánh dấu luồng xử lý (ThreadLocal) để `Char.truLuongNeuDu` trừ **lượng khoá**; giá lấy `gia_moi` (`Database.loadShopTemplate`); giới hạn tuần giữ nguyên |
| 3 | **Bị kick "hack chuyển map" khi bước vào cổng**: vùng chữ nhật `ALL_LOCALTION_MAP` là toạ độ bản đồ GỐC, bản đồ Pokémon vẽ lại có ô cổng nằm ngoài vùng (map 1, 4, 7, 9, 70, 201… — 10 cổng) | `Map.dungCanhCong` + `MapXuLyLenh.doChangeMap`: ở chế độ Pokémon, chấp nhận nếu người chơi đứng trong 2 ô quanh một ô cổng của map và đích (map, x, y) khớp đúng một bản sao trong `locationMap` của hướng cổng đó |
| 4 | Thông báo xoá nhân vật "không thể khôi phục" trong khi khôi phục được trong 7 ngày | `SelectCharHandler`: "Nhân vật đã xoá. Có thể khôi phục trong 7 ngày." |
| 5 | Thông báo boss "Salamence , Lopunny , đã xuất hiện" | `Map.getNameBossApear`: nối ", " và bỏ đúng 2 ký tự cuối |
| 6 | Client: đứng cạnh cổng của map không có trong bảng vị trí (đấu trường 227) → `NullPointerException` mỗi khung hình | `GameScr.checkCanChangeMap`: bỏ qua cổng khi `Res.getNameMap/getLocation` null |
| 7 | Client: bình về thành (19) dùng trong làng 301/70/1701/80/1901 vẫn mất bình (chỉ chặn map 0) | `GameScr`: chặn cả 6 bản làng |

## 4. Còn lại / ghi nhận (không sửa trong đợt này)

- **Cân bằng Gym 1**: nhân vật cấp 15, đủ điểm tiềm năng + kỹ năng, vũ khí đồng +0, xoay chiêu 0..3: hạ Golem
  (49.100 HP) mất **226 giây / 776 đòn** — thiết kế `ttk_giay`=105. Cần xem lại HP Gym hoặc giả định trang bị.
- **Quà nhân vật đầu tiên** 40.000.000 xu + 20.000 lượng + 30.000 lượng khoá (`board_created`, yêu cầu 12/09)
  vẫn phát khi `sv.lau_dai=1` — lệch với ngân sách kinh tế dài hạn (giftcode dài hạn cấm xu/lượng). Cần chủ dự án quyết.
- Chống spam chat chỉ áp người thường (GM bỏ qua) — đúng thiết kế; bot GM không kiểm được.
- GM `mto` vào map đặc biệt (40, 206, 208–210) gây NPE trong `playerJoin` của map đó (đường GM, không phải người chơi).
- Map Liên đấu 32–39, 27 có trong bảng 105 nhưng không đăng ký trên server thường (không có tệp bố cục) — GM `mto` rơi vào map rỗng.
- GM `slv` không cộng điểm tiềm năng/kỹ năng (bot cấp điểm qua CSDL để mô phỏng người chơi thật).
- Giới hạn tuần shop lượng tăng đếm TRƯỚC khi trừ tiền (thiếu lượng khoá vẫn mất 1 lượt tuần).
- Danh sách mặt hàng shop lượng (gói 76) vẫn hiện loại tiền theo `data_shop.type` (lượng) cho mục đã chuyển sang lượng khoá.
- Chữ còn sót kiếm hiệp trong thông báo chợ: "tụ hồn đan, xa tiêu lệnh, lệnh bài trấn yêu" (`SellerTemplate.infoItemSell`) — chuyển nhóm rà chữ.
- Lời mời giao dịch: "Không thể mời quá 2 lần trong 1 phút" nhưng thực tế chỉ 1 lần/phút.
- Map 105 (sảnh nhân vật mới) gửi tên map rỗng.
