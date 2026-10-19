# client_pc — bản PC (Java SE) Huyền Thoại Bảo Bối

Bản PC chạy **đúng code game của bản jar** (`client_jar/src` + `client_jar/shared/mod`) trên lớp tương thích MIDP cho
máy tính (Java SE 8+, AWT/Java2D). Mỗi lần build, `port-j2me.py` chép code jar vào `out/build/gen` và vá tối thiểu
(mẫu vá lệch là dừng build). Lớp MIDP + cửa sổ / HD: `src/`. Tài nguyên: `client_jar/res` + `res/` (phông Noto Sans /
Baloo 2, tiếng 44.1 kHz `res/am_hq`, soundbank MIDI GPL-2 để riêng cạnh jar).

## Build

Cần JDK 9+ (`javac --release 8`; mặc định JDK 21/17 trong `/usr/lib/jvm`) và python3.

```sh
sh client_pc/build.sh                               # bản THỬ: server 127.0.0.1:19129 (HTBB-Ban-Thu)
MAY_CHU=192.168.1.5:19129 sh client_pc/build.sh     # bản thử nối server khác
KIEU=phat-hanh MAY_CHU_CHINH=host:cong sh client_pc/build.sh   # bản phát hành
KIEU=phat-hanh sh client_pc/dong_goi_windows.sh     # bộ cài Windows + AppImage Linux (ProGuard + jlink, tải công cụ)
java -jar client_pc/out/HTBB-PC.jar [--may-chu host:cong] [--cua-so 1920x1080] [--toan-man] [--khong-am] [--fps]
```

Ra `client_pc/out/`: `HTBB-PC.jar` (fat jar), `HTBB.sh` / `HTBB.bat` / `HTBB.command`, `am/` (soundbank),
`GHI_CONG.txt`, `app-image/HTBB/` (nếu có jpackage; `APP_IMAGE=0` để bỏ); `out/goi/` khi đóng gói Windows.

### Gói phát hành (0.9.5) - như một game PC bình thường, không có .jar rời

`dong_goi_windows.sh` ra `client_pc/out/goi/`:

| Tệp | Nội dung |
|---|---|
| `HTBB-PC-Setup-<ver>.exe` | bộ cài Inno Setup: `HTBB.exe` (Launch4j, jar game đã làm rối NHÚNG trong exe) + `runtime\` (jlink) + `am\` + giấy phép |
| `HTBB-PC-Windows-<ver>.zip` | cùng bố cục, chạy ngay |
| `HTBB-PC-Linux-<ver>.AppImage` | một tệp: `AppRun` + `usr/lib/htbb/{runtime, htbb.jar, am}` trong squashfs (`chmod +x` rồi chạy) |
| `HTBB-PC-Linux-<ver>.tar.gz` | `HTBB/HTBB` (trình chạy) + `runtime/` (jar game ở `runtime/lib/htbb/`) |
| `HTBB-PC-<ver>.jar` | jar PC đã làm rối (tệp riêng) |
| `mapping-pc.txt` | bản đồ tên ProGuard - GIỮ KÍN, không phát hành |

- Mã game (`game.**`, `network.**`, `effect.**`) làm rối bằng ProGuard (`release-pc.pro`); lớp nền `htbb.pc.**` /
  `javax.microedition.**` giữ tên (KiemPc chạy được trên bản làm rối).
- Java đi kèm dựng bằng `jlink` từ jmods Temurin 21 (module theo `jdeps`), bỏ debug / header / man, nén zip-9:
  Windows ~48 MB, Linux ~59 MB (JRE đầy đủ cũ ~190 MB giải nén). jlink luôn sinh `runtime/lib/jrt-fs.jar` (của JDK).
- Kiểm: `runtime/bin/java --list-modules`, `runtime/release` (MODULES=), chạy AppImage / HTBB.exe (wine) tới màn đăng
  nhập (`--chup <tệp.png> <giây>`).

## Cấu hình

- Máy chủ lúc chạy: `--may-chu` > biến `HTBB_MAY_CHU` > `may_chu=` trong `cai_dat.properties` > máy chủ gắn lúc build.
- Bản phát hành BẮT BUỘC `MAY_CHU_CHINH=host:cong` hoặc `MAY_CHU_CHINH=` trong `tools/phat-hanh/may_chu.conf`.
- `BAN` / `PHIEN_BAN`: mặc định từ `tools/phat-hanh/phien_ban.conf`.
- Dữ liệu người chơi (tài khoản nhớ, cài đặt, ảnh F12, `htbb-pc.log`): Windows `%APPDATA%\HTBB`,
  macOS `~/Library/Application Support/HTBB`, Linux `~/.local/share/htbb`.
- Phím: WASD / mũi tên đi, Space đánh, 1–5 chiêu, Enter = hành động ngữ cảnh (tiếp lời thoại / nói chuyện NPC /
  đánh / nhặt / câu cá), T hoặc / mở chat (Enter gửi), Esc menu, F1 trang Phím tắt, F10 cài đặt (đổi phím), F11 toàn màn,
  F12 chụp. Chuột trái làm mọi việc; chuột phải chỉ là menu ngữ cảnh tuỳ chọn. Danh sách đầy đủ: Cài đặt → Phím tắt.
- Vùng chơi CỐ ĐỊNH 640x360 logic, thế giới phóng cố định x2 (thấy 20 x 11,25 ô ở mọi cỡ cửa sổ - không thu phóng):
  cửa sổ to / phóng to / toàn màn chỉ làm hình to hơn (hệ số nguyên khi vừa, không thì thu vừa), phần dư là viền.
  Cỡ cửa sổ thường + trạng thái phóng to lưu ở `cua_so_rong` / `cua_so_cao` / `phong_to`.
- Trình bày (0.9.6.1): Linux / macOS dùng BufferStrategy kiểu CHÉP (Blt), tạo lại mỗi khi cỡ cửa sổ đổi hoặc vừa lỗi
  (lật trang XDBE trên Xwayland đen màn khi cửa sổ to ra); Windows giữ mặc định. `-Dhtbb.lat=true|false` để ép.

## Kiểm thử (không cần màn hình)

```sh
sh client_pc/build.sh && O=$(mktemp -d) && javac -cp client_pc/out/build/cls -d $O client_pc/test/KiemPc.java \
  && java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemPc
```
Khung cố định / phóng / viền / đổi toạ độ chuột ở nhiều cỡ cửa sổ, chữ bộ gõ (InputMethodEvent) và KEY_TYPED, lọc dán,
hàng tên trên đầu, hộp thoại đáy tự tắt, vùng bản đồ.

Khác trong `client_pc/test/` (biên dịch cùng `ChupTrongGame.java` và `server/src/com/htbb/pokemon/IconPokemon*.java` -
máy chủ ảnh giả nạp icon từ `server/map/icon_pokemon`):
- `ChupTrongGame` / `ChupTruocGame`: chụp mọi màn ở cỡ điện thoại / PC + kiểm cỡ chạm (`-Dchup.co=dt20x9,pc1080
  -Dchup.man=menu,the_gioi` để chạy một phần);
- `KiemTrungLap`: một lối vào mỗi việc (HUD x lưới Menu, vùng chạm trùng tên, phím trùng);
- `KiemChamMoi` (0.9.6): dấu dẫn đường chấm đỏ (game.pokemon.ChamMoi) - mỗi nguồn (thư, điểm tiềm năng / kỹ năng, chiêu
  mới, nhiệm vụ mới / đủ để trả) có chấm VẼ THẬT ở mọi cấp từ nút HUD xuống đúng hàng / nút; xoá nguồn thì cha tắt;
- `KiemThongBao` (0.9.6.1): thông báo (game.pokemon.ThongBao + DungDo) - phân loại câu máy chủ thật, thời gian hiện
  >= tối thiểu theo loại / độ dài, hàng đợi (tối đa 3 thẻ, không mất thẻ nào, mỗi thẻ hiện đủ thời gian đọc), gói -104 có
  hình, kết quả câu cá hiện lúc kéo cần (không hộp chặn), và vẽ mọi kiểu thẻ / biểu ngữ ở 11 cỡ (trong vùng an toàn, không
  chồng nhau / nút HUD);
- `KiemNhiemVu`: hội thoại / nhận nhiệm vụ bằng chạm / chuột / phím;
- `KiemHopChiTiet` (0.9.6.1): thẻ chi tiết vật phẩm chung (game.pokemon.HopChiTiet - cửa hàng / cửa hàng đá / hành trang)
  ở 11 cỡ + J2ME: trong vùng an toàn, giữa, <= 80 %, chữ + nút trong thẻ, nút không đè chữ, tên một đoạn, có giá;
- `KiemTruotDoiCo` (0.9.6.1): thanh trượt (Bang.thanhTruot - bảng Tự động đánh) theo ngón / chuột từng lần kéo, lưu khi
  thả (kể cả thả ngoài thanh); đổi cỡ cửa sổ nhỏ -> TO HƠN cỡ đầu -> phóng to / 21:9 / 4:3 / toàn màn: khung đúng cỡ mới,
  phủ kín, không đen (đường vẽ cửa sổ thật, bề mặt là ảnh) + luật tạo lại bề mặt trình bày (ManHinh.canTaoLai);
- `KiemPhongRieng` (0.9.7): thẻ phòng riêng (game.pokemon.PhongRieng - Phó bản Hầm ngục / trận Cúp PvP, gói -112): đọc gói,
  gói hỏng không vỡ, tự ẩn khi đổi bản đồ, vẽ ở 11 cỡ trong vùng an toàn, canh giữa, không chồng nút HUD, chạm >= cỡ tối thiểu;
- `KiemManTai`: màn tải duy nhất (đăng nhập → thế giới, đổi bản đồ, tự kết nối lại) - từng khung, luôn một lớp;
- `KiemIconChieu`: mọi chiêu có icon PNG (res/ui/chieu_*.png, `tools/pokemon/sinh_icon_chieu.py`);
- `ChupTrongGame` còn ghi `chuoi_da_ve.txt` (mọi chuỗi đã vẽ) cho `python3 -I tools/pokemon/kiem_chuoi_ui.py <tệp>`
  (chữ thô "ID:", "null", %s, toạ độ, từ cấm) và kiểm mọi hộp thoại canh giữa / trong giới hạn cỡ; 0.9.6: mọi TẤM
  (Bang.khungTam) canh giữa ±2 px, rộng <= 80 % vùng an toàn, trong vùng an toàn ở 11 cỡ; menu ngữ cảnh NPC trong vùng
  an toàn và không chồng phần HUD nào;
- `DoKhung`: thời gian vẽ một khung (p50 / p99 / max), KB cấp phát mỗi khung, GC - thế giới 24 người chơi, Menu,
  Nhân vật + Hành trang, Cửa hàng, chat, ở 1280x720 và 1920x1080 (`--j2me`: tắt chữ / khung HD);
- `DoCuaSo`: FPS trong CỬA SỔ THẬT (cần màn hình): khung game / khung trình bày mỗi giây + bộ đo khung.

Bộ đo khung trong game: Cài đặt -> Hiển thị -> "Đo khung (nhà phát triển)" (ms nhập / logic / vẽ thế giới / giao diện /
trình bày, p50 / p99, GC / giây, cấp phát / giây). Hiệu ứng "Cao" = vẽ 60 khung / giây (logic vẫn 25 nhịp, vị trí nội
suy giữa hai nhịp - game.pokemon.NhipVe); "Thấp" = 25.
