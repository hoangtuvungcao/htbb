# Kiến trúc Huyền Thoại Bảo Bối (HTBB)

Tài liệu này dành cho lập trình viên mới vào dự án. Nó mô tả các thành phần chạy ra sao, nói chuyện với nhau qua
đâu, dữ liệu đi từ bảng thiết kế tới game theo đường nào, và các quy ước khi sửa mã.

HTBB là game nhập vai trực tuyến 2D, người chơi điều khiển **một con Pokémon**. Game được chuyển từ một game kiếm
hiệp cũ của TeaMobi (KPAH / NQSH). Lối chơi MMO thời gian thực của game gốc vẫn giữ nguyên: đi lại, đánh quái, lên
cấp, bang hội, sự kiện, PvP. Phần Pokémon gồm loài, 18 hệ khắc chế và tiến hoá. Hiện game chỉ còn chế độ Pokémon:
bản đồ kiếm hiệp cũ đã bỏ, mọi bản đồ nằm ở `server/cMap` (chỉ bản Pokémon).

Tài liệu liên quan:

| Tài liệu | Nội dung |
|---|---|
| [cau-hinh.md](cau-hinh.md) | Danh sách khoá `server.ini` và khoá cấu hình trung tâm |
| [build.md](build.md) | Build server, client, web; chạy kiểm thử |
| [trien-khai.md](trien-khai.md) | Dựng VPS, systemd, nginx, deploy tự động |
| [van-hanh.md](van-hanh.md) | Vận hành hằng ngày: bảo trì, sao lưu, nạp tiền, xử lý sự cố |
| [thiet-ke-game.md](thiet-ke-game.md) | Thiết kế lối chơi, nội dung, kinh tế |
| [giay-phep.md](giay-phep.md) | Giấy phép tài nguyên (PMDCollab, CC0) và ghi công |

Quy ước đường dẫn: mọi đường dẫn trong tài liệu viết **từ gốc monorepo** (`server/src/...`, `du_lieu/...`), trừ khi
ghi rõ khác. Server và bộ kiểm thử server chạy với thư mục làm việc là `server/`.

---

## 1. Tổng quan thành phần

### 1.1 Sơ đồ

```
 Người chơi
 ┌──────────────────────────────────────────────────────────────┐
 │ client_jar (J2ME)   client_apk/pokemon (Android)              │
 │ client_pc (Java SE) client_pc_ios (Unity: PC Windows, iOS)    │
 └───────────────┬──────────────────────────────────────────────┘
                 │ TCP 19129 (giao thức nhị phân, qua WAF / proxy TCP)
                 ▼
 ┌───────────────────────────────┐   TCP 8023 (chỉ nội bộ)   ┌───────────────────────┐
 │ Game server (Java)            │ ────────────────────────▶ │ Login server          │
 │ server/htbb-server.jar        │   kiểm tài khoản/mật khẩu │ CheckLoginSocket.jar  │
 │ systemd: htbb-game            │ ◀──────────────────────── │ systemd: htbb-login   │
 │ đọc server/server.ini         │       trả userId          └──────────┬────────────┘
 └──────────────┬────────────────┘                                     │
                │ JDBC (pool db.*)                                     │ JDBC
                ▼                                                      ▼
 ┌──────────────────────────────────────────────────────────────────────────────────┐
 │ MariaDB 127.0.0.1:3306      htbb_db (dữ liệu game, web, nạp, cấu hình)            │
 │                             account (bảng team_user: tài khoản đăng nhập)         │
 └──────────────────────────────────────────────────────────────────────────────────┘
                ▲  mysql2 (đọc/ghi cùng hai CSDL)
                │
 ┌──────────────┴────────────────┐      ┌─────────────────────────────────────────┐
 │ Website Nuxt 4 (web/)         │ ◀─── │ nginx :80/:443 (sau WAF)                 │ ◀── Trình duyệt
 │ 127.0.0.1:3000                │      │ /downloads/ phát bản tải game            │
 │ systemd: htbb-web             │      └─────────────────────────────────────────┘
 │ đọc server/server.ini +       │
 │ loginServer/server.ini để lấy │
 │ thông số CSDL; đọc/ghi        │
 │ bao_tri.txt, trang_thai.json  │
 └───────────────────────────────┘

 Ngoài JVM (cron root mỗi phút): vps/dieu-khien-server.sh (bật/tắt game theo bảng dieu_khien_server)
                                 vps/kiem-suc-khoe.sh (bắt tay -40, trang_thai.json, login, CSDL, web)
```

### 1.2 Thư mục monorepo

| Thư mục | Vai trò |
|---|---|
| `server/` | Game server Java (`src/`, `test/`, `lib/`), login server (`loginServer/`), bản đồ (`cMap/`), tài nguyên server gửi client (`map/`) |
| `client_jar/` | Client J2ME (MIDP 2.0). **Nguồn mã game duy nhất** cho cả bản Android và bản PC Java |
| `client_apk/` | Android. `client_apk/pokemon` biên dịch lại mã của `client_jar` qua [port-j2me.py](../client_apk/port-j2me.py) trên lớp tương thích MIDP |
| `client_pc/` | PC Java SE (AWT/Java2D), cũng chép mã `client_jar` qua [port-j2me.py](../client_pc/port-j2me.py) |
| `client_pc_ios/` | Client Unity (PC Windows, iOS). Mã C# ở `Assets/Scripts/Assembly-CSharp`, viết riêng (song song với bản jar) |
| `web/` | Website + trang quản trị Nuxt 4 (Node 22). Đây là website duy nhất; web PHP cũ đã bỏ hẳn |
| `sql/` | Seed, migration, file SQL sinh ra; `sql/htbb.sql` là CSDL đầy đủ ([sql/README.md](../sql/README.md)) |
| `du_lieu/` | Bảng dữ liệu thiết kế (CSV), ảnh xem trước bản đồ, báo cáo sinh ra |
| `tools/` | Công cụ sinh dữ liệu/tài nguyên (`tools/pokemon/`), cấu hình trung tâm (`tools/cauhinh/`), phát hành client (`tools/phat-hanh/`) |
| `vps/` | Unit systemd, nginx, MariaDB, script deploy/sao lưu/giám sát ([vps/README.md](../vps/README.md)) |

### 1.3 Ghi chú về mã nguồn gốc

- Server là mã dịch ngược từ game gốc rồi sửa tiếp. Tới P3 (10/2026) 169 lớp lõi (ví dụ `CreateCharHandler`, `Market`,
  `SessionManager`, `Actor`, nhiều lớp `real.*`, `data.*`) chỉ có dạng bytecode trong `lib/htbb-core.jar`; P3 đã dịch
  ngược chúng thành mã nguồn trong `src/` (đầu tệp `PHUC HOI TU LOI CU`, nguồn gốc ở [giay-phep.md](giay-phep.md)) và
  bỏ jar. Mọi lớp game giờ biên dịch từ `src/`; tên cũ vẫn giữ tới P4/P5 (xem mục 11).
- Server biên dịch `--release 8` (bytecode Java 8). Thư viện: Connector/J 5.1, `jxl`, `json`.
- Client J2ME là jar gốc dịch ngược bằng jadx rồi viết lại các hàm dựng sai. Phần Pokémon nằm trong
  `client_jar/src/game/pokemon/`. Chỗ móc vào mã gốc có chú thích `// pokemon:`.
- Client Unity là bản Unity gốc giải nén ra (AssetRipper/ILSpy), đã chuyển hẳn sang Pokémon.

---

## 2. Giao thức và cổng

### 2.1 Cổng

| Cổng | Ai nghe | Ai nối | Ghi chú |
|---|---|---|---|
| 19129/TCP | Game server (`sv.port`) | Client, `kiem-suc-khoe.sh`, bot kiểm tải | Công khai, qua WAF/proxy TCP |
| 8023/TCP | Login server (`port` trong `loginServer/server.ini`) | Chỉ game server (`sv.iplogin`/`sv.portlogin`) | Chỉ nội bộ (ufw chặn ngoài) |
| 3306/TCP | MariaDB | Game, login, web, script vps | Chỉ `127.0.0.1` |
| 127.0.0.1:3000 | Web Nuxt (`htbb-web`) | nginx | nginx nghe 80/443 sau WAF |

Proxy tin cậy (`sv.proxyTinCay`) được miễn giới hạn theo IP. Danh sách IP WAF khai ở cấu hình nginx/khoá cấu hình,
xem [trien-khai.md](trien-khai.md).

### 2.2 Khung gói tin

Mã: [Message.java](../server/src/io/Message.java), [Session.java](../server/src/io/Session.java).
Mọi số nguyên là **big-endian** (`DataInputStream`/`DataOutputStream`), chuỗi là `writeUTF` (2 byte độ dài + UTF-8
biến thể của Java).

| Chiều | Khung |
|---|---|
| Client → server | `i8 cmd`, `u16 size`, `size` byte dữ liệu |
| Server → client | `i8 cmd`, `u16 len`, dữ liệu; hoặc khi dữ liệu > 32767 byte: `i8 -128`, `i8 cmd`, `u32 len`, dữ liệu |

- **Mã hoá.** Ngay sau khi nối, client gửi gói `-40`. Server ([OtherHandle.java](../server/src/data/OtherHandle.java),
  `sendKey`) trả gói `-40` chứa khoá ngẫu nhiên rồi bật mã hoá cho phiên. Từ đó mọi byte hai chiều (cả `cmd` và `size`)
  được XOR với dòng khoá. Hai chiều có con trỏ khoá riêng (`curR`, `curW`). Gói `-40` luôn gửi **không mã hoá**.
- Gói vào có `size > 10000` thì cắt kết nối. Gói vào `size >= 1024` bị bỏ (không xử lý).
- `Session` gửi đi qua hàng đợi riêng của mỗi kết nối (tối đa 8192 gói, `-Dhtbb.tranHangDoiGui`). Hàng đợi đầy nghĩa là
  client không đọc nữa, server đóng socket. Luồng gửi gom nhiều gói đã sẵn thành một lần ghi + flush.
- **Gói kèm** (`Message.kemTheo`): một gói có thể mang theo chuỗi gói đi ngay sau, cùng hàng đợi. Chế độ Pokémon dùng
  cách này để gắn `-90` sau gói 3/5 và `-91` sau gói 26, nên không phải sửa hàng trăm chỗ tạo gói cũ.
- Client không biết một mã lệnh thì bỏ qua im lặng (switch không có nhánh `default`, bọc try/catch). Vì vậy gói mới
  thêm vào không làm client cũ hỏng.

### 2.3 Định tuyến lệnh phía server

[RealController.java](../server/src/real/RealController.java) `processMessage`:

1. Lệnh có handler đăng ký trong `commandHandler` (`initData`) thì gọi handler đó: `1` đăng nhập
   ([LoginHandler](../server/src/real/cmd/LoginHandler.java)), `39` đăng ký trong game
   ([DangKyTrongGame](../server/src/real/cmd/DangKyTrongGame.java)), `13` danh sách/chọn nhân vật, `14` tạo nhân vật,
   `5` thông tin nhân vật, `11` ping, `23` thông tin NPC, `47` lệnh admin, `-64` nhiệm vụ, `-58` bán đồ, `100` template.
2. Lệnh tài nguyên đi thẳng vào [DataGame.java](../server/src/data/DataGame.java): `-47` ảnh nhân vật, `-48` quần áo/vật
   trang trí, `-49` hiệu ứng, `-51` ảnh theo id (đọc tệp từ đĩa); `127` số người online.
3. Còn lại: lệnh của nhân vật đang ở trong bản đồ. Lệnh nằm trong `privateCmd` chạy ngay trên luồng đọc của phiên. Các
   lệnh khác xếp vào hàng đợi của bản đồ/khu (`Map`/`Region`) và chạy trên luồng gói tin chung (mục 9).

Điều này quan trọng khi sửa tiền/vật phẩm: cùng một `Char` có thể bị hai luồng sửa gần như cùng lúc (mục 11).

### 2.4 Một số mã lệnh hay gặp

| Mã | Chiều | Nội dung |
|---|---|---|
| `-40` | C↔S | Bắt tay, trao khoá mã hoá. `vps/kiem-suc-khoe.sh` dùng đúng gói này để kiểm cổng game còn sống |
| `1` | C→S | Đăng nhập: `username, pass, version, platform, clientProvider, provider, agent` (UTF), `firmware` (byte), `w` (short), `vsTile`, `idServer` (byte), rồi các trường mở rộng (mã máy…) |
| `39` | C→S | Đăng ký tài khoản trong game, cùng bộ quy tắc với web |
| `13` / `14` | C↔S | Danh sách nhân vật, chọn / tạo nhân vật. Cuối gói 13 có khối ngoại hình Pokémon (mục 2.6) |
| `3` / `5` | S→C | Thông tin nhân vật chính / nhân vật khác |
| `4` | C→S | Di chuyển |
| `7` | C→S | Hỏi máu/vị trí quái (lệnh tra cứu, đếm riêng) |
| `12` | S→C | Vào bản đồ: `short mapID, x, y`, `byte idXaphu`, `short mapLoad`, `UTF tên`, `bool có`, rồi **toàn bộ byte tệp bố cục** |
| `26` | S→C | Danh sách template quái. Số template đếm bằng 1 byte nên tối đa 255 |
| `105` | S→C | Bảng bản đồ và vị trí đích của cổng (`locationMap`) |
| `38` | S→C | Dòng thông báo chạy trên đỉnh màn hình |
| `-30` / `-31` | C↔S | Menu NPC / nhập chữ. NPC ảo của tính năng mới dùng id âm riêng (ví dụ dải `-540..-549` cho nạp tiền) |
| `47` | C→S | Lệnh quản trị trong game |

### 2.5 Gói Pokémon

Định nghĩa phía server: [GoiPokemon.java](../server/src/com/htbb/pokemon/GoiPokemon.java),
[GoiLauDai.java](../server/src/com/htbb/pokemon/GoiLauDai.java); phía client jar:
[PokeMang.java](../client_jar/src/game/pokemon/PokeMang.java),
[PokeMangChienDau.java](../client_jar/src/game/pokemon/PokeMangChienDau.java).

| Mã | Tên | Chiều | Nội dung | Gửi khi |
|---|---|---|---|---|
| `-90` | POKE_THAN | S→C | `i16 charId, i16 dex, u8 shiny` | Kèm sau mỗi gói 5 về một nhân vật (cả bot NPC), sau gói 3 về chính mình, khi tiến hoá |
| `-91` | POKE_QUAI | S→C | `u8 n`, n × (`u8 idTemplate, i16 dex, u8 shiny, u8 tiLe%`) | Kèm sau gói 26 |
| `-92` | POKE_TIEN_HOA | S→C | `i16 charId, i16 dexCu, i16 dexMoi` | Khi tiến hoá, gửi trước `-90` cho mình và người ở gần |
| `-93` | POKE_CUOI | S→C | `i16 charId, i16 dexThu (0 = không cưỡi), u8 shiny`; dex: hoá hình → Entei, skin linh thú đang hiện → `ThuPokemon.dexSkin`, còn lại theo `Animal.idImg` (`TienHoaPokemon.dexCuoi`, bảng SQL `data_thu_pokemon`) | Kèm sau mỗi `-90` |
| `-94` | POKE_PET | S→C | `i16 charId, i16 dexPet (0 = không có), u8 shiny`; pet hoá hình → `PetPokemon.dexHoaHinh(cap)`. Client vẽ cả pet-tool (lớp `monster/Pet`, kiểu 5) bằng PokeBody | Kèm sau mỗi `-93` (chuỗi `-90 → -93 → -94`) |
| `-95..-99` | Chiến đấu | S→C | Đơn sát thương, cảnh báo vùng boss, trạng thái, pha boss, đóng góp | Gói kèm sau gói cũ. Server chưa gửi thì client tự đoán hiệu ứng như cũ |
| `-100` | Tự động đánh | C↔S | Tự động đánh do máy chủ điều khiển ([TuDong.java](../server/src/com/htbb/pokemon/TuDong.java)) | |
| `-101` | Hồi sinh | C↔S | Hộp chọn cách hồi sinh ([HoiSinhGame.java](../server/src/real/HoiSinhGame.java)) | Client cũ dùng menu `-30` |
| `-102` / `-103` | Ngoại hình | S→C / C→S | Phụ kiện hiển thị trên thân Pokémon và cài đặt ẩn/hiện ([NgoaiHinh.java](../server/src/com/htbb/pokemon/NgoaiHinh.java)) | |
| `-104` | Kết quả dùng vật phẩm | S→C | `u8 loại (0 xong, 1 không dùng được, 2 tin), UTF nội dung` — thông báo nhanh sau lệnh `29` / `22` ([DungVatPham.java](../server/src/real/DungVatPham.java), client [DungDo.java](../client_jar/src/game/pokemon/DungDo.java)) | Chỉ gửi cho client bản ≥ 902; bản cũ / Unity nhận hộp thoại `37` |
| `-110` / `-111` | Danh hiệu | S→C / C→S | `-110`: `u8 0` bảng danh hiệu của mình, `u8 1` nhãn danh hiệu đang đeo của một nhân vật (`i16 charId, i16 id, UTF tên, u8 độ hiếm`, kèm cuối chuỗi `-90 → … → -102`), `u8 2` số danh hiệu mới; `-111`: xin bảng / đeo / tháo / đã xem ([DanhHieuGame.java](../server/src/real/DanhHieuGame.java), [DanhHieuLogic.java](../server/src/com/htbb/pokemon/DanhHieuLogic.java), client [DanhHieuKho.java](../client_jar/src/game/pokemon/DanhHieuKho.java)) | Chỉ gửi cho client bản ≥ 907 (bản 906 đã phát hành không có bộ xử lý) |
| `-118` | Thời tiết / mùa / ngày cộng đồng | S→C | trạng thái bản đồ đang đứng: thời tiết + gợi ý ảnh hưởng, giây tới khung sau, khung sau, màu phủ mùa, ngày cộng đồng (giờ vàng, dòng nổi bật) ([GoiThoiTiet.java](../server/src/com/htbb/pokemon/GoiThoiTiet.java), [ThoiTietGame.java](../server/src/com/htbb/pokemon/ThoiTietGame.java), client [ThoiTiet.java](../client_jar/src/game/pokemon/ThoiTiet.java)) | Chỉ gửi cho client bản ≥ 908 (0.9.8; client cũ, kể cả 0.9.7 = 907, nhận `-39`) |
| `-119` | Quản lý thú (ô thú, thả về tự nhiên, khoá, Vé mở ô thú) | C→S | byte kiểu: 0 trạng thái / 1 thả (byte n + n x (byte loại, short id)) / 2 khoá / 3 dùng vé / 4 mua vé (đường mua Cửa hàng lượng); trả -104 + gói 16 loại 2 có phần nối thêm (ô thú n/tối đa, số vé, giá vé, mỗi dòng: cờ khoá / không thả, loài, cấp, xu thả, lý do) ([QuanLyThuGame.java](../server/src/real/QuanLyThuGame.java), [OThu.java](../server/src/com/htbb/pokemon/OThu.java), client [QuanLyThu.java](../client_jar/src/game/pokemon/QuanLyThu.java)) | Phần nối thêm gói 16 chỉ gửi cho client bản ≥ 912 (0.9.12; bản 911 = 0.9.11 không có bộ đọc); client cũ không gửi -119 |
| `-124` | Kênh chat | C↔S | S→C: `u8 0` cấu hình kênh, `u8 1` tin (`u8 kênh, u8 cờ, i64 lúc, i16 id gửi, UTF tên, UTF đối phương, UTF nội dung, UTF nhãn`), `u8 2` bạn bè + online, `u8 3` danh sách chặn, `u8 4` kết quả gửi (`u8 kênh, u8 mã, i16 giây chờ, UTF lý do`), `u8 5` bạn online / offline, `u8 6` id theo tên; C→S: `u8 0` xin lại, `u8 1` gửi (`u8 kênh, UTF đích, UTF nội dung`), `u8 2` chặn, `u8 3` xin bạn bè, `u8 6` tra id ([KenhChatLogic.java](../server/src/com/htbb/pokemon/KenhChatLogic.java), [KenhChatGame.java](../server/src/real/KenhChatGame.java), client [KenhChat.java](../client_jar/src/game/pokemon/KenhChat.java), [ManChat.java](../client_jar/src/game/pokemon/ManChat.java)). Kênh: 0 Bản đồ (gửi vẫn bằng lệnh 27), 1 Thế giới, 2 Đội (bang), 3 Nhóm (tổ đội), 4 tin riêng, 5 Hệ thống (chỉ client) | Chỉ gửi cho client bản ≥ 908; bản cũ nhận gói cũ: thế giới `-60`, đội `-18`, tin riêng `-5`, chat nhóm `-5` từ thẻ "Nhóm" |
| `-125` | POKE_QUAI_CON | S→C | `u8 n`, n × (`i16 idQuai, i16 dex, u8 shiny, u8 bậc`); `dex = 0` = vẽ loài mặc định của mẫu; `shiny = 1` = con quái sắc màu (tung lúc hồi sinh, `LoaiQuaiGame.tungShiny`), client xin gói `<NNNN>s` | Ngay sau gói `4` thêm một quái đang mang loài riêng (hoặc vừa trả về loài mẫu). **Chỉ client ≥ `loai.ban_client` (909)**; client cũ thấy loài của mẫu ([LoaiQuaiGame.java](../server/src/real/LoaiQuaiGame.java), [GoiLoai.java](../server/src/com/htbb/pokemon/GoiLoai.java), client [LoaiMang.java](../client_jar/src/game/pokemon/LoaiMang.java)) |
| `-126` | POKE_LOAI | C↔S | byte đầu = kiểu: `0` sprite theo nhu cầu (C→S `i16 dex, u8 shiny`; S→C `i16 dex, u8 shiny, i32 tổng, i32 vị trí, u16 len, byte` theo khúc 4 KB, `tổng = 0` = máy chủ không có), `1` bảng 1.025 loài (C→S `i32 băm`; S→C `i32 băm, u16 n, n × (i16 dex, UTF tên, i8 hệ1, i8 hệ2, u8 bậc, u8 thế hệ, u8 cờ)`), `2` bảng không đổi (`i32 băm`), `3` Pokédex (S→C `u16 n, n × (i16 dex, u8 cờ gặp / hạ / bắt / bit3 bắt shiny)`); kiểu 0 với `shiny = 1` trả gói `<NNNN>s.goi` (`tổng = 0` = loài chưa có bản sắc màu, client vẽ bản thường) ([LoaiMayChu.java](../server/src/real/LoaiMayChu.java)) | Client hỏi khi cần; sprite tối đa `loai.sprite_kb_giay` (64 KB/s) mỗi client, gói nằm ở `server/map/loai/<NNNN>.goi` |
| `-116` | Tủ đồ trang trí (0.9.8) | C↔S | Trang phục / hào quang / skin thú cưỡi / thẻ bạn đồng hành: xin, mặc, chọn dòng chỉ số, nhận, chế, mua lượng ([TrangPhucGame.java](../server/src/real/TrangPhucGame.java), [TrangPhucLogic.java](../server/src/com/htbb/pokemon/TrangPhucLogic.java)); hình đi trong đuôi `-102` | Chỉ gửi cho client bản ≥ 908 |
| `-117` | Kho gói (0.9.15) | S→C | `u8 kiểu (0 lưu gói kế tiếp cùng lệnh / 1 dùng gói trong kho), i8 lệnh, i64 băm FNV-1a, i32 độ dài` - client khai gói đang giữ ở gói đăng nhập sau số khả năng (bit 4): `u8 n, n x (i8 lệnh, i64 băm, i32 độ dài)` ([KhoGoi.java](../server/src/io/KhoGoi.java), client [KhoGoi.java](../client_jar/src/network/KhoGoi.java)) | Chỉ client bản ≥ 915 có bit 4; lệnh 25 / 26 / 1 / -108 / 105 |

Sprite Pokémon của các loài có sẵn **đóng gói trong client**; server chỉ gửi số `dex` và cờ `shiny`. Lý do: đường gửi ảnh
cũ của server dùng toạ độ u8 và kích thước sbyte, không chứa nổi khung hình PMD. Loài **không có trong jar** (Gen 1–2 đợt
A trở đi) tải theo nhu cầu qua `-126`: gói `HTPK` một loài (`server/map/loai/<NNNN>.goi`, cùng định dạng `res/pk`), client
J2ME lưu RMS mỗi loài một store, ngân sách 1 MB, xoá theo LRU ([KhoSprite.java](../client_jar/src/game/pokemon/KhoSprite.java));
đang tải thì vẽ bóng đen. Template quái vẫn ≤ 255: loài là **dex gắn từng con quái** (`-125`), không thêm template.

Giới hạn cứng phía client cần nhớ khi thêm dữ liệu:

| Thứ | Giới hạn | Nguồn |
|---|---|---|
| Số template quái | ≤ 255 | gói 26 đếm 1 byte |
| `w`/`h` quái, `palate` | ≤ 127 | sbyte |
| Toạ độ ảnh ghép hiệu ứng | ≤ 255 px | u8 |
| id quần áo/vật trang trí | ≤ 127 | gói `-48` |
| Ảnh tile gửi trong gói đăng nhập | ≤ 32767 byte | độ dài ghi bằng short |

### 2.6 Chống ngập gói tin

[BoDemGoiTin.java](../server/src/io/BoDemGoiTin.java) là cửa sổ trượt 5 giây cho từng kết nối, kiểm **trước** khi xử
lý gói. Ba ngân sách đếm riêng:

| Loại | Lệnh | Mặc định / 5 giây | Khoá |
|---|---|---|---|
| Thường | mọi lệnh khác | 300 | `sv.tranGoiThuong` |
| Tài nguyên | `-47`, `-48`, `-49`, `-51` | 600 | `sv.tranGoiTaiNguyen` |
| Tra cứu | `7` | 600 | `sv.tranGoiTraCuu` |

- Vượt trần thì **bỏ gói** nhưng giữ kết nối. Vượt xa trần (gấp 4 lần) thì cắt kết nối.
- Mỗi lần vượt ghi nhật ký `NGAP_GOI` và ghi một dòng nghi vấn gian lận.
- Giá trị `0` tắt hẳn loại đếm đó (chỉ dùng ở máy nhà). `sv.tranGoiMienIp` miễn đếm cho IP chạy bot tải.

Lớp bảo vệ ở mức kết nối nằm trong vòng `accept` của [TeamServer.java](../server/src/server/TeamServer.java): danh sách IP
bị chặn, giới hạn số kết nối mỗi IP và tốc độ mở kết nối (`sv.maxConnPerIp`, `sv.connRateMax`, `sv.connRateWindow`,
`sv.dosBanSec`), hết thời gian đọc socket (`sv.socketTimeout`). Giới hạn đăng nhập theo IP ở
[GioiHanIp.java](../server/src/com/htbb/mang/GioiHanIp.java). Chống spam chat/thao tác ở
[ChongSpam.java](../server/src/com/htbb/chongspam/ChongSpam.java).

---

## 3. Login server và game server

### 3.1 Login server

- [server/loginServer/CheckLoginSocket.jar](../server/loginServer/CheckLoginSocket.jar) là jar có sẵn từ game gốc,
  **không có mã nguồn**. Nó nghe TCP 8023 và đọc CSDL `account` qua JDBC (Connector/J rất cũ đóng gói bên trong).
- Cấu hình ở `server/loginServer/server.ini` (`db.url`, `db.user`, `db.password`, `port`). Tệp này không vào git, mẫu ở
  [loginServer.server.ini.example](../vps/loginServer.server.ini.example). URL JDBC phải có `characterEncoding=UTF-8`,
  thiếu thì driver cũ gặp collation `utf8mb4` sẽ báo lỗi và không ai đăng nhập được.
- Mật khẩu so bằng hàm `PASSWORD()` của MySQL/MariaDB trên cột `team_user.password`. Mọi chỗ tạo hoặc đổi mật khẩu
  (web đăng ký, đổi mật khẩu, quản trị, đăng ký trong game) đều phải ghi bằng `PASSWORD(?)` để khớp với login server.
  Khi chạy trên NovaDB (không có `PASSWORD()`), web tự tính giá trị tương đương.
- Schema: [server/loginServer/account.sql](../server/loginServer/account.sql) (bảng `team_user`).

### 3.2 Luồng đăng nhập

```
client ──(-40)──▶ game: nhận khoá, bật mã hoá
client ──(1: user, pass, version, ...)──▶ game LoginHandler
    ├─ kiểm bảo trì (bao_tri.txt), cấm máy, giới hạn IP, phiên bản
    └─ LogInController.OnMessageLogInComming: xếp gói (sessionId, user, pass) vào hàng đợi
          luồng gửi ──TCP 8023──▶ login server ──SELECT ... PASSWORD(?)──▶ account.team_user
          luồng nhận ◀── (sessionId, userId, cờ, region)
LoginHandler.onLoginOK ──▶ game nạp nhân vật từ htbb_db ──(13: danh sách nhân vật)──▶ client
```

- [LogInController.java](../server/src/io/LogInController.java) giữ **một** kết nối bền tới login server và tự nối lại mỗi
  giây khi rớt. Hàng đợi gửi quá 50 yêu cầu thì từ chối đăng nhập ngay.
- Game server đọc `sv.iplogin`/`sv.portlogin` sớm (`docDiaChiLoginSom`) trước khi mở kết nối tới login server.
- Game server chỉ nhận `userId` từ login server. Toàn bộ dữ liệu nhân vật nằm ở `htbb_db`.
- Login server không nối ra ngoài, nên nó chỉ cần mở cho `127.0.0.1`.

### 3.3 Game server

Điểm vào: [TeamServer.java](../server/src/server/TeamServer.java). Một tiến trình phục vụ một máy chủ game. Nó giữ toàn
bộ trạng thái chơi trong RAM (nhân vật online, bản đồ, quái, chợ) và lưu xuống CSDL định kỳ, khi thoát và khi bảo trì.
Luồng chạy chi tiết ở mục 9.

---

## 4. Cơ sở dữ liệu

### 4.1 Hai CSDL

| CSDL | Ai dùng | Nội dung |
|---|---|---|
| `htbb_db` | Game server, web, script vps | Gần như mọi thứ: template, nhân vật, bang, chợ, sự kiện, cấu hình, web, nạp tiền, nhật ký |
| `account` | Login server, web (đăng ký, đổi mật khẩu, quản trị tài khoản) | `team_user`: id, tên đăng nhập, mật khẩu (`PASSWORD()`), email, cờ cấm |

Pool kết nối của game server ([Database.java](../server/src/data/Database.java), lớp `ConnPool`):

| Pool | Khoá `server.ini` | Dùng cho |
|---|---|---|
| `connPool` | `db.host`, `db.name`, `db.user`, `db.password`, `db.maxco` | Pool chính |
| `connPoolNap`, `connGiftCode` | `db.nameNap`, `db.userNap`, `db.pass`, `db.maxNap` | Nạp tiền, giftcode |
| `connPool1` | `db.host1`, `db.name1`, `db.user1`, `db.password1`, `db.maxco1` | Một số thao tác nhân vật (`DatabaseNhanVat`) |

Trên máy thật ba nhóm đều trỏ cùng `htbb_db`. Tham số JDBC thêm ở `db.thamSoJdbc`. Lúc khởi động, `kiemTraCSDL` chạy
trước mọi thao tác CSDL và dừng với thông báo rõ ràng nếu không nối được. [LocBiMat.java](../server/src/server/LocBiMat.java)
che mật khẩu khỏi stdout. Web đọc thông số CSDL từ `server.ini` và `loginServer/server.ini` (biến `HTBB_DB_*` ghi đè), pool
`mysql2` riêng ([web/server/utils/db.ts](../web/server/utils/db.ts), [config.ts](../web/server/utils/config.ts)).

### 4.2 Nhóm bảng

| Nhóm | Ví dụ | Ai ghi | Ghi chú |
|---|---|---|---|
| `data_*` template gốc | `data_item`, `data_monster`, `data_potion`, `data_shop`, `data_quest`, `data_attribute` | Trang quản trị / SQL (`sql/htbb.sql`) | Server nạp **một lần lúc khởi động** (`Database.startSaveChar` → `Map.loadAllTemplate`). Sửa xong phải khởi động lại |
| `data_*` nội dung mới | `data_noi_dung_*`, `data_*_lau_dai`, `data_hst_*`, `data_nhiem_vu_*`, `data_su_kien_nam` | Trang quản trị / SQL | Bảng "staging" cho nội dung dài hạn, hệ sinh thái, nhiệm vụ. Một số là nguồn duy nhất máy chủ nạp lúc khởi động (mục 4.4) |
| `data_loai*` loài đầy đủ | `data_loai` (1.025 loài), `data_loai_tien_hoa`, `data_chieu`, `data_loai_chieu`, `data_loai_vung`, `data_loai_sprite`, `data_loai_khu_vuc_map` (+ `data_nhom_trung*` của ket-doi, `data_bat_loai` / `data_bat_chi_so_loai` của bat-trade) | `sql/htbb.sql` (khối `them_loai_day_du.sql`) sinh bởi `tools/pokemon/loai/sinh_loai.py` (PokeAPI ghim commit) | Server nạp lúc cần và nạp lại mỗi 5 phút ([LoaiQuaiGame.java](../server/src/real/LoaiQuaiGame.java)); web `/pokedex` đọc thẳng. Mở đợt loài = bật `data_loai.bat` + thêm `data_loai_vung` |
| `tob_*` dữ liệu người chơi | `tob_char` (cột `pInfo`, `equip`/`inven`/`bag` dạng chuỗi, `dex`, `shiny`), `tob_clan`, `tob_animal`, `tob_pet`, `tob_market`, `tob_friendlist`, `tob_pokedex*`, `tob_char_lau_dai`, `tob_hst_*` | Game server | Ghi đè bằng bản trong RAM khi lưu, nên **web không được ghi thẳng** vào nhân vật đang chơi (mục 5.1) |
| Bật/tắt sự kiện | `tob_su_kien` | Web `/admin/sukien` | Game đọc lúc khởi động (`real.SuKien`) và theo lịch (`real.LichSuKien`) |
| Cấu hình | `cau_hinh`, `cau_hinh_*`, `nap_cau_hinh` | Web, game | Mục 6 |
| Cờ hệ thống cũ | `5h_systems` (`restart`, `tangnap`…), `5h_active`, `5h_notify` | Game, web, SQL tay | Cờ `restart` là đường thoát có lưu dữ liệu của luồng bảo trì |
| Giao tiếp web → game | `web_lenh_nhan_vat`, `dieu_khien_server`, `tob_top_thuong` | Web ghi, game/watcher xử lý | Mục 5 |
| Web | `web_tin_tuc`, `web_trang`, `web_faq`, `web_media`, `web_cai_dat`, `web_su_kien_lich`, `web_admin_log`, `web_admin_2fa`, `web_admin_vai_tro`, `web_admin_thay_doi`, `web_chan_spam`, `web_khoa_tai_khoan` | Web | CMS, quản trị, chống spam đăng ký |
| Nạp tiền, thành viên | `nap_don`, `nap_so_cai`, `nap_vi`, `nap_goi`, `nap_cua_hang`, `nap_the_thang`, `nap_ve_mua`, `nap_webhook`…; `tv_thanh_vien`, `tv_nhat_ky` | Web (webhook, admin), game (cộng dồn) | Mặc định tắt, mục 5.5 |
| Giftcode | `giftcode`, `giftcode_log`, `tob_giftcode*` | Web, game | |
| Giám sát | `tob_chi_so_may_chu`, `tob_log_online`, `tob_vi_tri_online` | Game | Server tự tạo bảng, tự dọn dòng cũ |
| Nhật ký | `tob_log_*` (mua shop, dùng lượng, vật phẩm, ngọc, bang…), bảng theo kỳ `tob_log_use_luong_*` | Game | `vps/don-log.sh` dọn định kỳ |

Server tự `CREATE TABLE IF NOT EXISTS` / thêm cột cho nhiều bảng mới lúc khởi động (`themCotPokemon`,
`themCotKhoXuLuong`, `DatabaseLauDai.taoBang`, bảng lệnh web, chỉ số máy chủ…). Các bước này chạy **trước** khi nhận người
chơi, vì câu lưu nhân vật ghi các cột mới.

### 4.3 Dựng CSDL

CSDL là **nguồn duy nhất** của dữ liệu game: sửa trên trang quản trị (hoặc SQL tay), không có bộ sinh. Repo giữ bản chụp
[`sql/htbb.sql`](../sql/README.md).

- Máy mới: `mysql < sql/htbb.sql` tạo cả `account` lẫn `htbb_db` (mọi bảng, dữ liệu thiết kế / cấu hình, tài
  khoản `admin`; bảng người chơi rỗng).
- CSDL đang chạy: `sudo bash /opt/htbb/vps/nang_cap_csdl.sh` — sao lưu rồi nạp [`sql/htbb.sql`](../sql/htbb.sql)
  (viết tay, chạy lại được, không đụng dữ liệu người chơi) trước khi deploy jar mới (mục 4.4).
- Đưa chỉnh sửa trên CSDL về repo: `sudo bash /opt/htbb/vps/xuat_csdl_day_du.sh /tmp/htbb.sql` (mysqldump schema
  mọi bảng + dữ liệu bảng thiết kế), chép thành `sql/htbb.sql`. Thêm bảng / cột thì thêm lệnh tương ứng vào
  `htbb.sql`.
- Bộ sinh và tệp SQL lẻ cũ đã dùng lần cuối rồi chuyển vào `tools/luu_tru_sinh_du_lieu/` (chỉ tra lịch sử).

### 4.4 Dữ liệu thiết kế đọc từ SQL (nguồn duy nhất)

Từ P1 (19/10/2026) các bảng dưới đây là **nguồn duy nhất**: máy chủ nạp chúng lúc khởi động, không còn bản chép trong
Java; dữ liệu của chúng nằm trong `sql/htbb.sql` và `sql/htbb.sql` (cùng từng dòng, `KiemDuLieuSql` kiểm).

| Bảng | Nạp vào (Java) | Thay bản chép cũ | Quan trọng |
|---|---|---|---|
| `data_cap_do_lau_dai` | `ThongSoLauDai.EXP_LEN_CAP`, `MOB_EXP`, `QUAI_MOI_GIO`, `XU_MOI_QUAI`, `GIO_TICH_LUY_PHAN_NGHIN` | mảng trong `ThongSoLauDai.java` | có |
| `data_nang_cap_lau_dai` | `ThongSoLauDai.TI_LE_NC`, `DA_NC`, `BUA_BAO_HO_LUONG_KHOA`, `CHI_SO_NC_PHAN_TRAM`, `CHI_PHI_NC`, `BAC_TB_TEN`, `BAC_TB_CAP`, `MAT_CAP_TU` | mảng trong `ThongSoLauDai.java` | có |
| `data_kinh_te_lau_dai` | `ThongSoLauDai.TI_LE_ROI_XU`, `THUE_CHO_PHAN_TRAM`, `PHI_DANG_BAN_PHAN_TRAM`, `NANG_LUONG_PHUT_*`, `BAC_MET`… (12 số) | hằng trong `ThongSoLauDai.java` | có |
| `data_su_kien_nam` | `SuKienLauDai.LICH` | mảng `LICH` | có |
| `cau_hinh_danh_muc` + `cau_hinh_nhom` | `CauHinhTrungTam` (qua `com.htbb.cauhinh.DanhMucSql`) | `DanhMucSinh.java` (đã xoá) | có |
| `data_hst_ho_cau` | `BanDoSinhThai.diemCauCua` (gợi ý điểm câu cá) | thuật toán `BanDoSinhThai.diemCau` chép từ bộ sinh | không |
| `data_noi_dung_trang_bi` | `GiftcodeAnToanDuLieu.TRANG_BI` (trang bị phát qua giftcode) | mảng `TRANG_BI` | không |

- Mã: [`com.htbb.dulieu.DuLieuSql`](../server/src/com/htbb/dulieu/DuLieuSql.java) (lớp thuần: đổi bảng → cấu trúc Java,
  kiểm hình dạng: đủ cấp 1..100, đủ 15 cấp nâng mỗi bậc, giá trị theo cấp nâng giống nhau ở mọi bậc, đủ 52 tuần…),
  [`data.DatabaseDuLieuSql`](../server/src/data/DatabaseDuLieuSql.java) (đọc qua pool chính `Database.connPool`). Tên bảng
  / cột chỉ lấy từ danh sách trắng trong mã.
- Thứ tự: danh mục cấu hình nạp trong `CauHinhTrungTam.khoiTao` (bước 5, mục 9.1), các bảng còn lại ngay sau
  `kiemTraCSDL` (bước 7), **trước** `SuKien.nap`, `ChuyenDoiLauDai` (đổi EXP nhân vật theo bảng cấp) và `startSaveChar`.
- Hỏng: bảng **quan trọng** thiếu / rỗng / thiếu cột / sai hình dạng → in `THIEU DU LIEU SQL QUAN TRONG - DUNG SERVER`
  kèm bảng, lý do và cách sửa (`mysql -u root < sql/htbb.sql`), thoát mã 1 (systemd báo `failed`). Chạy
  tiếp với bảng EXP = 0 sẽ đổi sai EXP nhân vật, tệ hơn không chạy. Bảng phụ thiếu chỉ in `[DU LIEU SQL] CANH BAO`
  (tắt gợi ý điểm câu, giftcode không phát được trang bị). Không bao giờ gán nửa vời: một bảng đọc và kiểm xong toàn bộ
  mới thay vào trường Java.
- Nạp lại: danh mục cấu hình tự nạp lại mỗi 60 giây (chu kỳ đầy đủ của `CauHinhTrungTam`, lỗi thì giữ danh mục đang
  chạy). Bảng số liệu (`data_*`) vẫn như mọi template `data_*` khác: đổi xong khởi động lại máy chủ (`napSoLieu` thay
  nguyên khối nên gọi lại được nếu sau này cần lệnh nạp nóng).
- Test và công cụ không có CSDL nạp cùng bảng từ `sql/htbb.sql` ([`DocDumpSql`](../server/src/com/htbb/dulieu/DocDumpSql.java):
  `Kiem.main` trước mọi nhóm, `tools/pokemon/MoPhongKinhTe.java`). [`KiemDuLieuSql`](../server/test/KiemDuLieuSql.java)
  ghim vân tay SHA-256 của bản chép Java cũ (tính ngay trước khi xoá), đòi tệp sinh, dump đầy đủ và migration cùng
  dòng, và kiểm các đường hỏng.
- Bảng `data_noi_dung_*` khác mà máy chủ không đọc (`bo`, `che_tao`, `chieu_nhanh_eevee`, `chieu_ttk`, `cua_hang`,
  `ngoc_he`, `nguyen_lieu`, `phan_ra`, `quai_moi`, `su_kien_vat_pham`, `trang_tri`, `vat_pham`) không có bản chép
  Java: chúng là bảng thiết kế cho web (Cẩm nang) và công cụ; vật phẩm / quái mới của chúng đã vào `data_item` /
  `data_monster` mà máy chủ đọc sẵn.

---

## 5. Web nói chuyện với game

Web và game **không gọi nhau trực tiếp** (không HTTP, không RPC). Chúng chỉ chia sẻ CSDL và vài tệp trong thư mục game.
Mọi kênh đều là "web ghi ý muốn, phía game tự đọc và áp dụng".

| Kênh | Web ghi | Bên đọc | Nhịp |
|---|---|---|---|
| `web_lenh_nhan_vat` | Lệnh với nhân vật | Luồng `LenhWeb` trong game | 1 giây |
| `cau_hinh` + `cau_hinh_*` | Giá trị cấu hình | Luồng `CauHinhTrungTam` | 5 giây (đọc cả bảng mỗi 60 giây) |
| `cau_hinh_khoi_dong_lai` | Yêu cầu khởi động lại an toàn | `CauHinhTrungTam` | 5 giây |
| `tob_su_kien` | Bật/tắt, lịch hẹn sự kiện | `real.LichSuKien` | 15 giây |
| `tob_top_thuong` | Phần thưởng đua top | Game, lúc người chơi mở NPC Tổng Quản | Theo yêu cầu |
| `dieu_khien_server` | Muốn game chạy hay tắt | `vps/dieu-khien-server.sh` (ngoài JVM) | Cron mỗi phút |
| `bao_tri.txt` | Bật bảo trì đăng nhập | `ChiSoMayChu` trong game | 5 giây |
| Bảng `nap_*` | Đơn đã thanh toán | `NapTienGame` | Vòng quét riêng |
| `trang_thai.json`, `tob_chi_so_may_chu` | (game ghi) | Web, `kiem-suc-khoe.sh` | 30 giây |

### 5.1 Hàng đợi lệnh web (`web_lenh_nhan_vat`)

Vấn đề: nhân vật đang online sống trong RAM của game. Web ghi thẳng `tob_char` thì lần tự lưu kế tiếp ghi đè mất.

Cách làm ([LenhWeb.java](../server/src/data/LenhWeb.java), web: [lenhNhanVat.ts](../web/server/utils/lenhNhanVat.ts)):

1. Web chèn một dòng `trang_thai = 0 (CHO)` với `loai`, `char_id`, tham số. Web không sửa `tob_char`.
2. Luồng `LenhWeb` đọc bảng mỗi giây (tối đa 50 dòng một vòng):
   - Nhân vật **online**: áp dụng vào RAM (cộng EXP qua đúng đường của AdminPanel, gỡ vật phẩm…), gửi lại gói cho client,
     lưu ngay bằng `saveCharAuto` → `1 XONG_ONLINE`.
   - Nhân vật **offline**: tuỳ loại lệnh, sửa cột CSDL hoặc để `4 CHO_DANG_NHAP`, áp dụng khi nhân vật vào game.
   - Lỗi → `3 LOI`, lý do ở `ket_qua`. Web huỷ dòng còn chờ → `5 HUY`.
3. Web chờ vài giây rồi đọc `trang_thai` để báo cho người bấm. Game đang tắt thì lệnh nằm chờ, không mất.

Các loại: `dat_cap_do` (chỉ nâng cấp, dùng cả cho "nâng cấp toàn server" với `tham_so = 'toanbo'`), `giam_cap_do`,
`xoa_vat_pham` (`tham_so = "<cột>><mẫu>"`, cột thuộc danh sách trắng `equip|inven|bag`), `xoa_nhan_vat` (xoá vĩnh viễn:
online thì kick, lưu rồi xoá). Lưu bằng `saveCharAuto`, **không** dùng `saveChar` (bản lưu lúc thoát rút thú cưỡi/pet
khỏi RAM). Cả web lẫn server tự tạo bảng; schema ở [them_bang_web_lenh_nhan_vat.sql](../tools/luu_tru_sinh_du_lieu/sql_nguon/them_bang_web_lenh_nhan_vat.sql).

### 5.2 Bật/tắt game từ web (`dieu_khien_server`)

- Khi game đang tắt thì không còn gì trong JVM để đọc CSDL. Vì vậy việc bật/tắt do một watcher chạy **ngoài JVM**:
  [vps/dieu-khien-server.sh](../vps/dieu-khien-server.sh), cron của root mỗi phút.
- Bảng chỉ có một dòng (`id = 1`): `muon_chay`, `gio_hen`, `da_ap_dung`, `trang_thai_thuc_te`, `lan_kiem_cuoi` (nhịp tim
  để web biết watcher còn sống), người yêu cầu ([them_bang_dieu_khien_server.sql](../tools/luu_tru_sinh_du_lieu/sql_nguon/them_bang_dieu_khien_server.sql)).
- Script chỉ gọi hai lệnh cố định `systemctl start|stop htbb-game`. Không chuỗi nào đọc từ CSDL được ghép vào lệnh.
  Thông số CSDL đọc từ chính `server.ini`.
- `systemctl stop` gửi SIGTERM; shutdown hook của game lưu dữ liệu (mục 9.4).

### 5.3 Bảo trì đăng nhập (`bao_tri.txt`) và chỉ số (`trang_thai.json`)

[ChiSoMayChu.java](../server/src/server/ChiSoMayChu.java), tắt cả hai bằng `sv.chiSoMayChu=0`.

- **`bao_tri.txt`** nằm cạnh `server.ini`. Có tệp thì chậm nhất 5 giây sau game **từ chối đăng nhập mới**; người đang
  chơi không bị đá. Dòng đầu là câu thông báo (trống thì dùng câu mặc định). Các dòng `cho: <tài khoản>` là tài khoản vẫn
  vào được (GM kiểm tra trước khi mở). Xoá tệp là mở lại. Web ghi tệp này nguyên tử (ghi tệp tạm rồi đổi tên,
  [baoTriTep.ts](../web/server/utils/baoTriTep.ts)). Trên VPS web thường không có quyền ghi thư mục game; khi đó trang
  quản trị chỉ hiện lệnh để làm tay.
- **`trang_thai.json`** (ghi đè nguyên tử mỗi 30 giây) và bảng `tob_chi_so_may_chu` (một dòng mỗi 30 giây, giữ 7 ngày):
  số online, heap, số luồng, CPU tiến trình, load máy, pool CSDL, và **số đếm tăng thêm trong chu kỳ** (gói vào/ra, gói
  bị bỏ, kết nối bị cắt vì ngập, lỗi xử lý gói, chat/thao tác bị chặn). Web chia cho 30 để ra tốc độ mỗi giây.
- `kiem-suc-khoe.sh` coi `trang_thai.json` cũ hơn 90 giây là JVM treo, dù cổng vẫn mở.

### 5.4 Khởi động lại có kiểm soát

Có ba đường dẫn tới cùng một việc: bật cờ `5h_systems.restart`. Luồng `BaoTriDaily` thấy cờ (đọc mỗi 5 giây) thì lưu toàn
bộ dữ liệu rồi `System.exit(0)`. systemd (`Restart=always`) bật lại.

1. Web yêu cầu qua `cau_hinh_khoi_dong_lai` (đếm ngược, báo người chơi, huỷ được), xem mục 6.
2. `real.LichSuKien` khi lịch sự kiện đổi cờ bật. Nó gom thay đổi 2 phút và im lặng 3 phút sau khởi động để không vượt
   `StartLimitBurst` của systemd.
3. Deploy tự động (`vps/deploy-auto.sh`): bật cờ thông báo trước 60 giây rồi `systemctl stop`.

### 5.5 Nạp tiền

Mặc định tắt. Phải bật đủ nhiều tầng: biến môi trường của web (`HTBB_NAP_BAT` và cấu hình cổng thanh toán), khoá
`sv.nap_tien` và `sv.lau_dai` phía game, các công tắc `nap.*` trong `cau_hinh`. Thiếu một tầng là không thu tiền.

- Web nhận webhook hoặc thao tác admin, chỉ đổi đơn sang `da_thanh_toan`.
- Game ([NapTienGame.java](../server/src/com/htbb/pokemon/NapTienGame.java)) là **đường duy nhất** cộng lượng: chuyển
  `da_thanh_toan → da_cong` bằng UPDATE có điều kiện, sổ cái `UNIQUE(ma_tham_chieu)`. Gọi lặp, hai luồng hay hai máy chủ
  cùng lúc đều chỉ cộng một lần.

Chi tiết vận hành ở [van-hanh.md](van-hanh.md).

### 5.6 Dữ liệu dùng chung web ↔ server

Một số quy tắc phải giống hệt ở hai bên. Nguồn là JSON trong `web/shared/data/`, sinh ra bản Java vì server không có thư
viện JSON lúc chạy. Hai bên so mã băm SHA-256 trong test, quên sinh lại là test báo hỏng. Dữ liệu đã có bảng SQL thì cả
hai bên đọc bảng (mục 4.4 và đoạn dưới bảng).

| Dữ liệu | Nguồn | Bản Java | Bộ sinh |
|---|---|---|---|
| Quy tắc đăng ký | `web/shared/data/quy-tac-dang-ky.json` | `server/src/data/QuyTacDangKyDuLieu.java` | `web/scripts/sinh-quy-tac-dang-ky-java.mjs` |
| Danh sách trắng giftcode | `web/shared/data/giftcode-an-toan.json` | `server/src/com/htbb/pokemon/GiftcodeAnToanDuLieu.java` (trang bị: bảng `data_noi_dung_trang_bi`) | tệp tĩnh (bộ sinh cũ đã lưu trữ) |
| Danh mục cấu hình | bảng `cau_hinh_danh_muc` + `cau_hinh_nhom` | không còn: máy chủ đọc bảng | không (CSDL) |

Web đọc dữ liệu game đã có bảng SQL lúc chạy qua `web/server/utils/duLieuSql.ts` (danh sách trắng, cache 5 phút; bảng
thiếu / trống → một dòng `[du-lieu-sql]` rồi dùng JSON đóng gói lúc build): `data_cap_do_lau_dai` (giới hạn cấp quản
trị, bảng cấp cẩm nang), `data_noi_dung_trang_bi` (trang bị giftcode), `data_kinh_te_lau_dai`, `data_nang_cap_lau_dai`,
`data_su_kien_nam`, `data_noi_dung_*` (cẩm nang, qua `/api/cam-nang/du-lieu` + `useCamNang()`), `cau_hinh_danh_muc` +
`cau_hinh_nhom` (danh mục cấu hình trung tâm). Biến đổi: `web/shared/utils/duLieuSqlThuan.ts`;
`web/tests/du-lieu-sql.test.ts` đòi SQL (`sql/htbb.sql`) == JSON.

---

## 6. Cấu hình trung tâm

Danh sách khoá và ý nghĩa: [cau-hinh.md](cau-hinh.md). Mục này chỉ nói kiến trúc.

### 6.1 Thành phần

| Phần | Tệp |
|---|---|
| Danh mục khoá (nguồn duy nhất) | bảng `cau_hinh_danh_muc` + `cau_hinh_nhom` trong CSDL (`sql/htbb.sql`) |
| Nạp danh mục | máy chủ: [DanhMucSql.java](../server/src/com/htbb/cauhinh/DanhMucSql.java) (mục 4.4); web: `danhMucTuSqlTT` trong [cauHinhTrungTam.ts](../web/shared/utils/cauHinhTrungTam.ts), dự phòng tĩnh [cauHinhTrungTam.gen.ts](../web/shared/utils/cauHinhTrungTam.gen.ts) |
| Dịch vụ máy chủ | [CauHinhTrungTam.java](../server/src/com/htbb/cauhinh/CauHinhTrungTam.java) (+ `TinhCauHinh`, `KiemGiaTri`, `ApDungMacDinh`, `CongTac`, `ThongBaoKhoiDong`) |
| Lớp cũ còn dùng | [server/CauHinh.java](../server/src/server/CauHinh.java): bảng setting trong game, hằng `K_*`; nạp lại uỷ quyền cho dịch vụ trung tâm |
| Trang web | `/admin/cau-hinh-he-thong`, API `/api/admin/cau-hinh-he-thong/**` ([cauHinhTrungTam.ts](../web/server/utils/cauHinhTrungTam.ts)) |
| Ví dụ kiểm dùng chung Java ↔ web | [tools/cauhinh/vi_du_kiem.tsv](../tools/cauhinh/vi_du_kiem.tsv) |

Sửa danh mục: sửa dòng trong `cau_hinh_danh_muc` / `cau_hinh_nhom` (SQL), khởi động lại máy chủ, xuất lại
`sql/htbb.sql` và chép các dòng mới vào khối tương ứng của `sql/htbb.sql`. Cột `lop` (`chinh` / `co_dinh` /
`luon_bat`) quyết định khoá hiện trên trang hay bị ẩn; máy chủ từ chối lưu khoá ẩn. Mỗi khoá có kiểu (`co`, `so`, `chon`, `chuoi`, `ds_*`, `ngay`,
`ngay_gio`, `nhip`), khoảng/lựa chọn, `apDung` (`ngay` hoặc `khoi_dong_lai`), bảng chứa (`cau_hinh`, `nap_cau_hinh`, `env`)
và nơi dùng (`server`, `web`, `ca_hai`).

### 6.2 Thứ tự ưu tiên

**Bảng `cau_hinh` (CSDL) > `server.ini` > mặc định trong danh mục.**

- Lúc khởi động, `CauHinhTrungTam.khoiTao(p)` chạy ngay sau khi tạo pool CSDL và **ghi đè vào `Properties`** của
  `server.ini`. Vì vậy mọi `p.getProperty(...)` phía sau trong `TeamServer` đọc giá trị đã áp thứ tự ưu tiên.
- Giá trị CSDL sai kiểu/khoảng/lựa chọn bị **từ chối**: máy chủ giữ giá trị tốt gần nhất, ghi lý do vào
  `cau_hinh_hieu_luc.loi` và `cau_hinh_ap_dung.loi`. Giá trị `server.ini` sai luật vẫn được dùng như cũ, chỉ cảnh báo.
- Cổng, thông số CSDL, đường dẫn log **không** nằm trong danh mục, chỉ đọc từ `server.ini` (phải có chúng mới đọc được
  CSDL).
- Bí mật cổng thanh toán của web vẫn ở biến môi trường. Khoá `bang: env` chỉ hiện trạng thái.
- Khoá thử nghiệm (`khoaProd`, ví dụ tua nhanh ngày, chế độ test) bị khoá về mặc định trên máy thật. Máy thử mở bằng
  `sv.cho_phep_thu_nghiem=1` trong `server.ini`.

### 6.3 Áp dụng ngay và khởi động lại

- `apDung: ngay`: luồng `CauHinhTrungTam` mỗi **5 giây** hỏi `cau_hinh_phien_ban` và `COUNT/MAX(sua_luc)` của `cau_hinh`.
  Có thay đổi thì nạp lại và gọi người nghe (`dangKyNghe`). Mỗi 60 giây đọc lại cả bảng để bắt người ghi thẳng mà không
  tăng phiên bản. Nhật ký: `[CAU HINH] AP DUNG khoa: cũ -> mới`.
- `apDung: khoi_dong_lai`: đọc một lần lúc khởi động rồi đóng băng. Giá trị mới hiện ở mục "chờ khởi động lại".

| Bảng | Ai ghi | Nội dung |
|---|---|---|
| `cau_hinh` | Web, game, SQL tay | Giá trị |
| `cau_hinh_phien_ban` | Web, `CauHinhTrungTam.ghi`, `CauHinh.luuNhieu` | Số phiên bản, tăng mỗi lần lưu |
| `cau_hinh_lich_su` | Web, `ghi()`, máy chủ (khi phát hiện ai đó ghi thẳng bảng) | Ai, khi nào, cũ, mới, lý do. Trang có nút hoàn tác |
| `cau_hinh_ap_dung` | Máy chủ (mỗi lần áp dụng + nhịp 30 giây) | Phiên bản đã áp dụng, giờ, lỗi, khoá chờ khởi động lại |
| `cau_hinh_hieu_luc` | Máy chủ | Giá trị đang chạy từng khoá và nguồn (`db`/`ini`/`mac_dinh`/`giu_cu`) |
| `cau_hinh_khoi_dong_lai` | Web đặt `cho`/`huy`, máy chủ chuyển trạng thái | Khởi động lại an toàn |
| `cau_hinh_danh_muc`, `cau_hinh_nhom` | SQL (`sql/htbb.sql`, `sql/htbb.sql`) | Danh mục khoá — máy chủ nạp lúc khởi động |

### 6.4 Khởi động lại an toàn

Web ghi `trang_thai = 'cho'` (10–1800 giây, kèm lý do) → máy chủ (≤ 5 giây) chuyển `dang_dem`, báo người chơi, đếm
ngược (huỷ được: `huy` → `da_huy`) → hết giờ chuyển `dang_thoat` và bật cờ `5h_systems restart` → luồng `BaoTriDaily`
lưu toàn bộ rồi thoát → systemd bật lại → lần khởi động mới ghi `da_xong`. Yêu cầu `cho` cũ hơn 10 phút (gửi lúc máy chủ
đang tắt) bị huỷ khi khởi động, để máy chủ không tự khởi động lại bất ngờ.

---

## 7. Luồng dữ liệu

```
trang quản trị web / SQL tay ──▶ MariaDB (account + htbb_db) ──▶ game server (nạp lúc khởi động, mục 4.4) + web (duLieuSql.ts)
                                    │  ▲
     vps/xuat_csdl_day_du.sh ◀──────┘  └────── sql/htbb.sql (máy mới) / sql/htbb.sql (CSDL đang chạy)
            │
            ▼
     sql/htbb.sql (commit) ──▶ test server/test/Kiem* + web/tests đọc thẳng tệp này

du_lieu/*.csv (sprite, tile, bản đồ, NPC) ──▶ server/map/*, server/cMap/*, tài nguyên client ──▶ client_jar/res/* ──▶ APK, PC
```

### 7.1 Dữ liệu game và bảng tài nguyên

- Dữ liệu thiết kế (vật phẩm, quái, nhiệm vụ, sự kiện, nội dung dài hạn, cấu hình) là bảng `data_*` / `cau_hinh_*` /
  `nap_*` / `tob_event*`… trong CSDL. Không có bảng tính hay bộ sinh nào ghi đè chúng.
- `du_lieu/` chỉ còn bảng tài nguyên mà mã máy chủ / client / công cụ đóng gói đọc trực tiếp (`bang_thu.csv`,
  `bang_chieu.csv`, `bang_tile_moi.csv`, `thiet_ke_map.csv`, `diem_co_dinh.csv`…) — [du_lieu/README.md](../du_lieu/README.md).
- Bộ sinh cũ (CSV → SQL, `danh_muc.json`, JSON tĩnh cho web) và bảng tính thiết kế ban đầu: `tools/luu_tru_sinh_du_lieu/`
  ([README](../tools/luu_tru_sinh_du_lieu/README.md)), không thuộc build, không test nào đọc.

### 7.2 Dữ liệu cho web

`web/shared/data/*.json` và `web/shared/utils/*.gen.ts` là bản **dự phòng tĩnh** đóng gói lúc build (bộ sinh đã lưu
trữ). Lúc chạy, các mục có bảng SQL lấy từ CSDL (mục 5.6); test web đối chiếu các tệp tĩnh với `sql/htbb.sql`.
`npm run sinh-anim-khoi-dau` (`scripts/sinh-anim-khoi-dau.mjs`) vẫn dựng `shared/data/sprite-anim.json` từ sprite client.

---

## 8. Bản đồ, tile và tài nguyên

### 8.1 Tệp bản đồ phía server

| Tệp | Nội dung | Ai đọc |
|---|---|---|
| `server/cMap/<id>` | Bố cục: tile nền, tileTop, vật trang trí, cổng, NPC phía client | Server (`Map*`, `MapNapTaiNguyen`), gửi nguyên byte cho client trong gói 12 |
| `server/cMap/{t,t_hang,t_thanh}.type` | 1 byte mỗi tile của bộ: bit `& 2` = chặn, còn lại đi được | Server và client |
| `server/map/map<N>` | Lưới sinh quái | Server (`MapNapTaiNguyen.loadMap`) |
| `server/map/pokemon_tile/*.png` | Ảnh bộ tile (dải 16 × 16n) | Server gửi `t_hang`, `t_thanh` trong gói đăng nhập; client đóng gói sẵn |
| `server/map/tree/<i>`, `server/map/cay/<j>.png`, `server/map/*.png` | Vật trang trí (`tree/<i>` lệnh `-48`, `cay/`, ảnh dịp lễ) | Server gửi theo yêu cầu |

Bản đồ, lưới quái và vật trang trí kiếm hiệp cũ đã xoá: `cMap/`, `map/map<N>`, `map/tree` (vẽ lại từ Ninja Adventure, P2), `map/cay` giờ chỉ chứa bản Pokémon; `map/farm` cố ý để trống (dự phòng NPC sự kiện, server trả ảnh trong suốt),
nên cả mã nguồn lẫn các lớp trong `các lớp lõi cũ phục hồi ở P3` (đọc thẳng `cMap/<id>`) đều dùng đúng bản Pokémon.
[BanDoPokemon.java](../server/src/com/htbb/pokemon/BanDoPokemon.java) chỉ còn danh sách map gửi thêm cho client.
Không nhầm: `map/pokemon_tile` (ảnh tile nền) và `cMap` (bố cục).

**Bộ tile theo id nạp** (`mapLoad`): id < 110 dùng `t`, 110–200 dùng `t_hang` (hang), > 200 dùng `t_thanh` (thành).
Quy tắc này có ở cả server, Unity và J2ME.

### 8.2 Định dạng `cMap/<id>`

Đọc/ghi đúng từng byte bằng [DinhDangBanDo.java](../server/src/com/htbb/pokemon/DinhDangBanDo.java): đọc rồi ghi lại cho
ra đúng tệp cũ. Ba nơi đọc định dạng này phải khớp lớp đó: server `MapNapTaiNguyen.loadTileMap` (+ đoạn đọc tile trong các
lớp `Map*`), Unity `Tilemap.loadMap`, J2ME `game/model/Tilemap.loadMap`.

```
u8 w, u8 h                                       ≤ 255 (ô 16 px)
w*h × u8 tile                                    chỉ số trong bộ tile; 254 và 255 = ô trống (kiểu 0 = ĐI ĐƯỢC)
{ u8 x,u8 y,u8 ww,u8 hh, hh*ww × u8 } … u8 255   khối tileTop (vẽ đè lên người)
{ u8 x,u8 y,u8 tile } … u8 255                   tileTop lẻ
{ u8 x,u8 y,u8 idCay } … u8 255 (hoặc 254)       vật trang trí tree/<idCay> (lệnh -48), không chặn
u8 offline                                       1 = bản đồ offline / trong nhà
u8 n, n × { u8 x,u8 y, u16 LE toM, u8 xOut, u8 yOut }   ô cổng (toM = chỉ số hướng trong locationMap)
u8 nNpc, nNpc × { u8 x, u8 y, u8 loai }          NPC làng chỉ client đọc
[byte còn lại, thường một byte 0: giữ nguyên]
```

Lưới quái `map<N>`: `u8 w, u8 h, h × w × u8 id mẫu quái` (0 và 255 = không có). Không nhầm với tệp bố cục.

Cổng hoạt động thế nào:

- Ô cổng có kiểu `2000000000 + i`. Server cho đổi bản đồ khi người chơi đứng gần ô cổng; điểm đến lấy từ
  `locationMap[nhóm][toM]` (5 bản sao `{map, x, y}`), bảng này cũng gửi cho client qua gói 105.
- Bản đồ vẽ lại có ô cổng có thể nằm ngoài vùng chữ nhật kiểm cổng của bản gốc (`ALL_LOCALTION_MAP`), nên server chấp
  nhận cả khi người chơi đứng trong 2 ô quanh một ô cổng và đích khớp một bản sao trong `locationMap`
  (`Map.dungCanhCong`).

Client lấy bố cục: gói 12 mang toàn bộ byte bố cục cho mọi bản đồ trong danh sách gửi (`Map.idMapSend2Client`, cộng các
bản đồ gửi thêm như 0 và 122). Client dùng byte server gửi khi có, chỉ đọc bản đóng gói sẵn khi không có (lúc khởi động
client gọi `Tilemap.loadMap(0, null)`). Không có kiểm tra phiên bản bản đồ: byte server gửi luôn thắng. Vì vậy sửa bản đồ
chỉ cần deploy server, trừ bản đồ 0 lúc khởi động client.

Bảng SQL không lưu toạ độ bản đồ, trừ vị trí đã lưu của nhân vật (`tob_char.x/y`). Nhân vật đứng ở chỗ nay thành vật cản
có thể kẹt sau khi đổi bản đồ.

### 8.3 Ràng buộc khi vẽ hoặc sửa bản đồ

- Giữ nguyên: kích thước, danh sách cổng (x, y, toM, xOut, yOut), cờ offline, danh sách NPC client, đuôi tệp.
- Chỉ số tile < số tile của bộ (≤ 253). Số tile mỗi bộ không vượt mảng màu bản đồ nhỏ của Unity: `t` 232, `t_hang` 116,
  `t_thanh` 216. Ảnh `t_hang`/`t_thanh` ≤ 32767 byte.
- Mọi ô cổng, NPC, điểm đến, điểm cố định, ô quái phải đi được và tới được từ cổng (loang 4 hướng trên kiểu tile thật).
  Không ô trống (kiểu 0) nào được kề ô người chơi tới được.
- Mọi vật thể trên bản đồ mới là tile của lớp nền, mọi ô có hình vật thể đều chặn (trừ cửa, cầu, cỏ cao, hoa, sỏi). Nhân
  vật không bao giờ đứng "sau" vật thể, nên vẽ nhân vật sau lớp tile là đúng chiều sâu.
- Bản đồ và `.type` phải đi cùng nhau, vì chỉ số tile bộ mới khác hẳn bộ gốc.
- Bản đồ 40 (MOBA) và 42: vị trí quái gắn với logic tháp/lane, tool không đổi chỗ quái.
- Điểm cố định phía server (NPC làng, điểm hồi sinh, boss, tháp, sảnh…) liệt kê ở `du_lieu/diem_co_dinh.csv`.

### 8.4 Công cụ bản đồ

| Tool | Việc |
|---|---|
| `tools/pokemon/GiaiMaMap.java` | Giải mã bản đồ ra JSON/ảnh và bảng điểm đến (đọc bằng phản chiếu từ lớp server đã build) |
| `tools/pokemon/DungBoTileMoi.java` | Dựng bộ tile mới hoàn toàn từ tài nguyên CC0, ra `pokemon_tile` + `.type` |
| `tools/pokemon/VeMapMoi.java` | Vẽ lại bản đồ theo `du_lieu/thiet_ke_map.csv` (đường nối cổng, hồ, rừng, vách, cỏ cao, làng), autotile, kiểm rồi sửa lặp; ra `cMap/`, `pokemon_spawn/`, ảnh `du_lieu/map_moi/` |
| `tools/pokemon/cai_map_j2me.sh` | Chép bản đồ 0 và `.type` mới vào tài nguyên J2ME (không build jar) |
| `client_jar/tools/kiem_ve_map/chay.sh` | Vẽ mọi bản đồ bằng chính mã vẽ J2ME, so pixel với ảnh xem trước, báo tile ngoài bộ, `.type` lệch, vật thể thiếu mảnh, tường vô hình, NPC đứng ô chặn. Phải ra `LOI 0` |

Vùng Hoenn là các bản đồ chỉ có ở bản Pokémon (id 43–62, 160–165, 280–282). Nguồn duy nhất của khung (kích thước, cổng,
NPC, mẫu quái) là [BanDoHoenn.java](../server/src/com/htbb/pokemon/BanDoHoenn.java). `real.VungHoenn` đăng ký các bản đồ
này vào bảng của server; lối vào duy nhất qua cổng bắc bản đồ 9. Tên bản đồ có bản giống hệt nhau ở server và các client
(`TenMapPokemon`).

### 8.5 Tài nguyên client

Nguồn hình là PMDCollab (sprite SpriteCollab, RawAsset cho item/hiệu ứng/vật thể) và tài nguyên CC0 cho tile. Giấy phép
và nghĩa vụ ghi công: [giay-phep.md](giay-phep.md). Ghi công phải đi kèm mọi bản phát hành (`GHI_CONG.txt` trong client).

```
PMDCollab ──tai_sprite_pmd.py / tai_hieu_ung_pmd.py / tai_vat_the.py──▶ tools/pokemon/pmd_raw*/ (không commit)
    │
    ├─ DongGoiSprite.java ──▶ client_pc_ios/Assets/Resources/poke/<khoa>_{anh,dl,cd}.bytes  (+ GHI_CONG)
    ├─ DungBoTileMoi / VeMapMoi ──▶ Resources/poke_tile, Resources/poke_map (kiểu tile, bản đồ 0)
    │                              server/map/pokemon_tile, server/cMap
    ▼
HTBB_ASSETS=pc sh client_jar/build.sh N
    └─ client_jar/tools/DongGoiPokeJ2me.java ──▶ client_jar/res/pk (sprite, chân dung, vật thể, từ điển),
                                                client_jar/res/pt (tile), client_jar/res_nhe/pk (bản nhẹ)
client_jar/res ──▶ assets của APK (client_apk/pokemon) và tài nguyên của client_pc
```

- Định dạng sprite: atlas PNG (`_anh`), dữ liệu khung (`_dl`, big-endian: số động tác, kích thước khung, thời lượng từng
  khung theo 1/60 giây, vị trí trên atlas, điểm chân theo 4 hướng) và chân dung 40×40 (`_cd`). Đuôi `.bytes` để Unity nạp
  thành `TextAsset`. Sáu động tác: đứng, chạy, đánh gần, đánh xa, trúng đòn, gục. Bốn hướng client (xuống, lên, trái,
  phải) lấy từ hàng 0, 4, 6, 2 của sprite PMD 8 hướng.
- Vẽ: đặt khung sao cho điểm chân trùng toạ độ `(x, y)` của actor (`PokeBody` ở mọi client).
- Icon vật phẩm Pokémon do server phục vụ ở dải ảnh 14000+ (`server/map/icon_pokemon`), không ghi đè icon gốc.
- `client_jar/tools/KiemPokeDuLieu.java` kiểm sprite/hiệu ứng/âm thanh của jar khớp bản Unity và từ điển chữ khớp server.
- Không còn hình của máy chủ cũ (P2): `tools/pokemon/kiem_tai_nguyen_cu.py` + test `KiemTaiNguyenCu` so dấu vân tay mọi tệp /
  ảnh trong gói phát hành với `du_lieu/tai_nguyen_cu.txt`; chỉ giữ hình cũ có lý do trong `du_lieu/tai_nguyen_cu_giu.csv`.
  Công cụ vẽ lại: [giay-phep.md](giay-phep.md) mục 7.

Lưu ý đường dẫn: một số tool sinh tài nguyên (`DongGoiSprite.java`, `tools/pokemon/duong_client.py`) mặc định còn trỏ vị
trí client từ trước khi gộp monorepo. Đặt `HTBB_CLIENT_ASSETS=client_pc_ios/Assets` khi chạy chúng.

### 8.6 Client

| Client | Mã | Ghi chú |
|---|---|---|
| J2ME `client_jar` | `client_jar/src` + `client_jar/shared/mod` | Nguồn duy nhất của mã game cho jar, APK, PC Java. `game/pokemon/*` là phần Pokémon |
| Android `client_apk/pokemon` | Sinh từ mã jar mỗi lần build | `port-j2me.py` vá cứng vài chỗ (đọc tài nguyên → assets, ô nhập → bàn phím hệ thống; mẫu lệch là dừng build) và vá mềm lớp giao diện HD (lệch chỉ cảnh báo) |
| PC Java `client_pc` | Sinh từ mã jar mỗi lần build | Lớp MIDP trên AWT/Java2D trong `client_pc/src` |
| Unity `client_pc_ios` | `Assets/Scripts/Assembly-CSharp` | Mã C# riêng. Sửa tính năng ở jar thì phải sửa tương ứng ở đây |

Máy chủ gắn vào client lúc build (`MAY_CHU_CHINH` / `MAY_CHU`, `tools/phat-hanh/may_chu.conf`), ghi vào MANIFEST. Bản phát
hành bắt buộc có máy chủ, thiếu là build dừng. Chi tiết build ở [build.md](build.md).

---

## 9. Luồng chạy server

### 9.1 Trình tự khởi động (`TeamServer.run`)

1. Đọc sớm `sv.iplogin`/`sv.portlogin`, mở kết nối tới login server (`LogInController`).
2. Mở bảng quản trị Swing (`AdminPanel`) chỉ khi có màn hình. Trên VPS (headless) thì bỏ qua.
3. Đăng ký shutdown hook (mục 9.4).
4. Đọc `server.ini`; bật `LocBiMat` (che bí mật trong stdout) và cấu hình nhật ký `com.htbb.log.Nhat` (`sv.log.*`).
5. Đọc cổng, giới hạn, tham số; tạo các pool CSDL; `CauHinhTrungTam.khoiTao(p)` nạp danh mục khoá từ
   `cau_hinh_danh_muc` + `cau_hinh_nhom` rồi ghi đè giá trị từ CSDL.
6. Đọc các cờ (`sv.lau_dai`, `sv.nap_tien`, `sv.he_sinh_thai`, giờ bảo trì, chống DoS, ngưỡng gói…).
7. `kiemTraCSDL`: dừng sớm nếu không nối được CSDL. Ngay sau đó `DatabaseDuLieuSql.napLucKhoiDong()` nạp dữ liệu thiết
   kế từ SQL (mục 4.4); thiếu bảng quan trọng thì dừng.
8. `SuKien.nap()` (phải trước khi nạp shop, vì shop chốt trạng thái bán theo cờ sự kiện lúc nạp).
9. Tự thêm bảng/cột mới (Pokémon, kho xu/lượng, dài hạn, giftcode…). Tất cả **trước khi nhận người chơi**.
10. `CauHinh.dongBoLucKhoiDong` + `CauHinh.batDau`; `Database.startSaveChar()` nạp toàn bộ template; nội dung dài hạn;
    `ChuPokemon.ap()` đổi chữ kiếm hiệp → Pokémon trong dữ liệu đã nạp.
11. `RealController`: dựng bản đồ, đăng ký handler; NPC, bang, top, boss; `DataGame.load()`, chợ.
12. Bật các luồng nền (mục 9.2).
13. `ServerSocket(sv.port, 200)`, vòng `accept`: chặn IP, chống DoS, tạo `Session` cho mỗi kết nối.

Thứ tự trong `run()` có nhiều ràng buộc được ghi chú ngay tại chỗ ("PHAI chay TRUOC…"). Đọc chú thích trước khi chuyển
một bước.

### 9.2 Luồng

| Luồng | Việc | Nhịp |
|---|---|---|
| `READ` + `SEND` mỗi kết nối | Đọc gói, kiểm ngập, gọi `processMessage`; gửi từ hàng đợi | **2 luồng / kết nối** |
| `HTBB-NHIP-n` ([VongLapChung](../server/src/com/htbb/runtime/VongLapChung.java)) | "Tick" của mọi bản đồ/khu | Pool chung, số lõi × 2 (`-Dhtbb.nhip.luong`) |
| `HTBB-GOITIN-n` (VongLapChung) | Xử lý hàng đợi gói tin của bản đồ/khu, nối tiếp từng khu | Pool chung, max(16, số lõi × 4) (`-Dhtbb.goitin.luong`) |
| Login (2 luồng) | Gửi/nhận với login server | Theo hàng đợi |
| `BaoTriDaily` | Bảo trì hằng ngày, đọc cờ `5h_systems` mỗi 5 giây, job đêm của hệ dài hạn | 1 giây |
| `CauHinhTrungTam` | Nạp lại cấu hình, xử lý khởi động lại an toàn | 5 giây |
| `LenhWeb` | Hàng đợi lệnh web | 1 giây |
| `ChiSoMayChu` | `trang_thai.json`, `tob_chi_so_may_chu`; kiểm `bao_tri.txt` | 30 giây / 5 giây |
| `LichSuKien` | Lịch sự kiện, gom khởi động lại | 15 giây |
| `GhiSoOnline` / `GhiViTriOnline` | Số online / vị trí người chơi cho trang quản trị | 5 phút / 30 giây |
| `NapTienGame` | Cộng dồn, thu hồi đơn nạp | Vòng riêng (chỉ khi bật) |
| `LAM MOI TOP`, `XA LOG MUA SHOP`, `LOG CCU`, `HUY SESSION WA LAU`, boss, xổ số… | Việc lặt vặt của game gốc | Vài trăm ms tới 60 giây |

`HUY SESSION WA LAU` ngắt phiên chưa đăng nhập sau 60 giây và phiên không hoạt động quá 10 phút.

**VongLapChung.** Game gốc tạo 4 luồng cho mỗi khu/bản đồ (tick hai phe, hai luồng gói tin), tức hàng nghìn luồng khi
server rỗng và hàng chục nghìn lần đánh thức mỗi giây. Nay:

- `Nhip`: thân hàm `update` cũ chạy trên pool hẹn giờ chung, chu kỳ giữ nguyên như vòng cũ (fixed delay). Khu không có
  người chơi thật chạy chu kỳ rảnh 2 giây (`-Dhtbb.nhip.ranhMs`); có người vào khu (`danhThuc()`) thì chạy lại ngay.
- `Lan`: vòng xử lý gói tin nối tiếp (không bao giờ chạy song song với chính nó), chỉ chạy khi có gói mới.
- Mỗi vòng bắt `Throwable`, một lỗi không giết được vòng.

Kết quả: cỡ 170–190 luồng khi server rỗng, cộng 2 luồng cho mỗi kết nối. Bất biến được kiểm ở `test/KiemVongLapChung.java`.

### 9.3 Bảo trì hằng ngày (`BaoTriDaily`)

[BaoTriDaily.java](../server/src/real/BaoTriDaily.java):

- Giờ bảo trì: `sv.gioBaoTri=HH:MM` cố định, `tat` thì không tự tắt, trống/sai thì ngẫu nhiên 05:01–05:30 (sau mốc lễ 04:00 và mốc tuần thứ Hai 05:00).
- Báo trước 60 giây bằng **một** dòng thông báo (gói 38). Gửi nhiều mốc thì client giữ tin trong hàng đợi và hiện lại
  sau khi đăng nhập lại.
- Tới giờ, hoặc cờ `5h_systems.restart` bật: lưu bang, nhân vật (`saveAllChar`), chợ, xe tiêu, sự kiện, nhật ký, top rồi
  `System.exit(0)`. systemd (`Restart=always`) bật lại.
- Mỗi giờ nạp lại hệ số tăng EXP/nạp/tỉ lệ rơi từ CSDL.

### 9.4 Tắt máy

- **Shutdown hook** (SIGTERM từ `systemctl stop`, Ctrl+C, đóng cửa sổ): nếu chưa tắt có trật tự thì lưu bang, nhân vật,
  chợ, xe tiêu, nấu bánh, sự kiện, nhật ký mua shop, top. Hook **không** gọi `AdminHandler.stopServer()` (hàm đó có
  `System.exit()`, gọi trong hook sẽ treo).
- `htbb-game.service` cho `TimeoutStopSec=180` để hook kịp lưu.
- Admin tắt có trật tự (`AdminHandler.isStopServer`) thì hook bỏ qua vì dữ liệu đã lưu.
- Cổng game bị chiếm (`BindException`) thì lưu rồi thoát.

### 9.5 Nhật ký

- Stdout/stderr của game vào `logs/game.out` (systemd), xoay vòng bằng logrotate.
- [com.htbb.log.Nhat](../server/src/com/htbb/log/Nhat.java): mức, loại tắt, gộp dòng lặp; lỗi xử lý gói chỉ in stack lần
  đầu mỗi loại trong 10 phút, các lần sau gộp thành số đếm.
- Nghi vấn gian lận (ngập gói, thao tác lạ) ghi vào CSDL qua `data.NghiVanGianLan`.

---

## 10. Kiểm thử

### 10.1 Server

- Bộ kiểm tối giản tự viết, **không JUnit**: [server/test/Kiem.java](../server/test/Kiem.java) là runner và các hàm
  `bang(moTa, mongDoi, thucTe)`, `dung`, `sai`, `tongKet()` (mã thoát 0/1). Mỗi nhóm là một lớp `Kiem*.java` trong
  `server/test/`.
- Test nằm ngoài `src/` có chủ đích: `build.bat` gom toàn bộ `src/` và `lib/` nằm trên Class-Path của jar deploy. Test
  không làm đổi một byte nào của `htbb-server.jar`.
- Phần lớn là **characterization test**: ghi lại hành vi hiện tại để bắt thay đổi ngoài ý muốn khi refactor, không khẳng
  định hành vi "đúng".
- Không cần CSDL, không cần mạng. Chạy với thư mục làm việc `server/`, test đọc tệp trong repo qua `../docs`, `../sql`,
  `../vps`, `../tools`, `../web/shared/data`… Dữ liệu thiết kế máy chủ đọc từ SQL (mục 4.4) được `Kiem.main` nạp từ
  `../sql/htbb.sql` trước mọi nhóm, nên test chạy trên đúng số liệu máy chủ nạp từ CSDL.
- Ngoài logic thuần, nhiều test kiểm **tính nhất quán của repo**: tệp sinh ra khớp bộ sinh (cấu hình trung tâm, quy tắc
  đăng ký, giftcode), bản đồ Pokémon đủ và đi được (`KiemMapPokemon`, `KiemVungHoenn`), mojibake trong mã nguồn
  (`KiemBangMaNguon`), mọi liên kết trong tài liệu `.md` trỏ tới tệp có thật (`KiemLienKetTaiLieu`), mẫu cấu hình VPS
  (`KiemBanMauVps`), chặn lộ bí mật (`KiemLocBiMat`).
- CI: [.github/workflows/server-test.yml](../.github/workflows/server-test.yml), biên dịch `src` + `test` với
  `--release 8` rồi chạy `Kiem`. Lệnh chạy tay ở [build.md](build.md).

Viết test mới: tạo `server/test/KiemTinhNang.java` theo mẫu các lớp sẵn có, gọi `Kiem.nhom("...")` rồi các phép
`Kiem.bang`, và đăng ký lớp vào `Kiem.main`. Logic mới nên tách thành lớp thuần (không socket, không CSDL) để test được,
như `BoDemGoiTin` tách khỏi `Session`, `GoiPokemon` tách khỏi `TienHoaPokemon`, `NapTienLogic` tách khỏi `NapTienDb`.

### 10.2 Web

`cd web && npm test` chạy `node --test tests/*.test.ts`. Logic thuần đặt ở `web/shared/utils/*` và các tệp `*Thuan.ts`
trong `web/server/utils/` để test không cần CSDL.

### 10.3 Bot kiểm chức năng và tải

`tools/pokemon/kiem_tai/` có bot nói đúng giao thức client (khoá `-40`, khung gói, đọc `cMap`, tìm đường) và các
kịch bản chơi thử mọi tính năng, in `PASS / FAIL / BO QUA` kèm bằng chứng. Chỉ chạy kịch bản GM trên **máy chủ thử
riêng** (cổng và CSDL khác), không chạy trên server thật.

### 10.4 Client

- `client_jar/tools/KiemPokeDuLieu.java`: dữ liệu Pokémon của jar khớp Unity và server.
- `client_jar/tools/kiem_ve_map/chay.sh`: kiểm cách client vẽ bản đồ (mục 8.4).

---

## 11. Quy ước mã nguồn

### 11.1 Ngôn ngữ, đặt tên, mã hoá ký tự

- **Định danh** (lớp, hàm, biến, cột/bảng CSDL mới, tệp tool) viết **tiếng Việt không dấu**, kiểu camelCase cho Java/TS
  (`CauHinhTrungTam`, `datMienIp`, `vuotNguong`), snake_case cho bảng/cột SQL và tool Python (`web_lenh_nhan_vat`,
  `trang_thai`, `sinh_sql_lau_dai.py`), kebab-case cho script/API web (`dong-bo-cam-nang.mjs`,
  `/api/admin/cau-hinh-he-thong`). Mã gốc dịch ngược còn tên tiếng Anh (`Char`, `Map`, `doPlayerMove`): giữ nguyên.
- **Chú thích**: trong Java server và tool Python phần lớn viết tiếng Việt **không dấu** (theo phong cách sẵn có). Mã web,
  README và tài liệu viết tiếng Việt có dấu. Chú thích giải thích **vì sao** (bug cũ, ràng buộc thứ tự, giới hạn client),
  không chỉ nói mã làm gì.
- **Chuỗi người chơi thấy** viết tiếng Việt có dấu, đúng thuật ngữ Pokémon chuẩn. Không tự đổi văn bản hiển thị khi
  không được yêu cầu: đề xuất nguyên văn trước, tách khỏi thay đổi logic.
- Mọi tệp nguồn **UTF-8**. Biên dịch Java với `-encoding UTF-8`; JVM chạy với `-Dfile.encoding=UTF-8`.
  `KiemBangMaNguon` bắt chữ bị mã hoá hai lần (mojibake), lỗi này không gây cảnh báo khi build.
- Không ghi tiếng Việt vào CSDL bằng `mysql -e "..."` qua shell. Viết tệp `.sql` bắt đầu bằng `SET NAMES utf8;`, chạy
  `mysql ... < tep.sql`, rồi `SELECT` lại để xác nhận.

### 11.2 Tệp sinh ra

- Tệp sinh ra ghi rõ ở đầu "KHÔNG sửa tay" và tên bộ sinh. Sửa nguồn rồi chạy lại bộ sinh.
- Bộ sinh phải tất định (không dấu thời gian, thứ tự cố định) để diff sạch và test so được.
- Mỗi số liệu có **một nguồn**. Ví dụ: danh mục cấu hình và số liệu dài hạn ở bảng CSDL (`cau_hinh_danh_muc`,
  `data_*_lau_dai`), khung vùng Hoenn ở `BanDoHoenn.java`, từ điển đổi chữ ở `ChuPokemon.java`.
- Số liệu đã có bảng SQL thì máy chủ và web **đọc bảng** lúc chạy, không sinh thêm bản chép Java/TS (mục 4.4, 5.6). Có
  dữ liệu mới thì thêm bảng SQL; nếu máy chủ cần thì thêm vào danh sách trắng của `DuLieuSql` kèm phép kiểm hình dạng.

### 11.3 Quy tắc rút từ bug thật

1. **Số hiệu dùng chung một không gian toàn cục, luôn `grep` trước khi gán**: id lệnh mạng, id NPC (kể cả NPC ảo âm),
   id ảnh, id vật phẩm, ô `otherAtt[N]`, `type` trong các hàm dispatch lớn, id input (`-32xxx`). Đếm so với giới hạn cứng
   phía client (mục 2.5). Hai ô cạnh nhau có thể khác đơn vị (%, ‰, 0,1 %): đọc cả chỗ ghi lẫn chỗ đọc.
2. **Hai thứ giống tên chưa chắc là một**: xác nhận bằng mã định nghĩa hoặc `SELECT` thật. Gặp cặp dễ nhầm thì ghi ngay
   `// KHONG nham voi X, xem Y` ở đầu tệp/hàm.
3. **Nghiệp vụ có nhiều bản sao** (họ `Map*.java`, cặp `saveChar`/`saveCharAuto`, các hàm cộng EXP song song): `grep`
   liệt kê hết bản sao trước khi coi là xong, đếm lại số chỗ đã sửa.
4. **Tiền/vật phẩm chạm từ nhiều luồng**: lệnh trong `privateCmd` chạy trên luồng phiên, phần lớn lệnh khác chạy trên luồng
   bản đồ. Khoá đúng phạm vi (`Char.KHOA_TIEN`, `Char.KHOA_GIAO_DICH`…) bọc **cả chuỗi** "kiểm đủ → trừ → cộng". Tham chiếu
   `Item` giữ giữa hai gói tin phải kiểm lại còn trong túi ngay trước khi dùng.
5. **Đánh dấu đã dùng trước, phát thưởng sau**: (1) trừ/đánh dấu bằng câu có điều kiện và kiểm đã áp dụng; (2) chỉ phát
   thưởng khi (1) thành công.
6. **Thêm loại thực thể mới** (thú, pet, skin…): rà mọi mảng song song cùng trục chỉ số. Các mảng này không kiểm độ dài lẫn
   nhau.
7. **Đổi tần suất gọi** (một lần → định kỳ) hoặc thêm dòng vào danh sách có giả định ngầm: rà lại nơi dựa vào giả định
   cũ, test bất biến khi gọi lặp.
8. **Ẩn ở giao diện không phải là khoá ở server**: điều kiện bật/tắt (sự kiện, VIP, cấp, cờ tính năng) phải kiểm lại ở
   chính nơi xử lý lệnh cuối cùng. Client luôn gửi thẳng gói tin được.
9. **Đổi tên package/class có sẵn** trong `real`, `data`, `server`, `io`, `real/cmd`… chỉ sau khi tìm hết chỗ nạp lớp
   bằng chuỗi (`Class.forName`, tên lớp trong cấu hình/log). Từ P3 không còn jar lõi đóng gọi ngược theo tên, nên
   `javac` báo mọi chỗ gọi trực tiếp; đổi tên hàng loạt để dành P4/P5.
10. **SQL mới luôn dùng PreparedStatement**. Tên cột/bảng ghép vào SQL phải lấy từ danh sách trắng (như `COT_VAT_PHAM` của
    `LenhWeb`). Không chép các đoạn SQL ghép chuỗi cũ làm mẫu.
11. **Sửa cùng một chỗ lần thứ hai trong thời gian ngắn**: dừng lại xem có phải sai loại nguyên nhân (cấu trúc dữ liệu
    dùng chung không đủ chỗ) thay vì vá thêm số liệu.
12. **Tính năng mới có cờ** đọc qua một hàm duy nhất của lớp tính năng (`LauDai.bat()`, `NapTien.bat()`,
    `HeSinhThai.bat()`…), không đọc lại `server.ini` rải rác. Cờ đổi schema hoặc dữ liệu đã nạp thì đặt
    `apDung: khoi_dong_lai`.
13. **Logic mới thì kèm test** trong `server/test/` (hoặc `web/tests/`), tách phần thuần ra lớp riêng.
14. **Không lộ bí mật**: không ghi mật khẩu, khoá, địa chỉ máy thật vào mã, tài liệu, log hay commit. Bí mật ở
    `/etc/htbb/htbb.env` và các `server.ini` không vào git; mẫu dùng biến `${...}`.
