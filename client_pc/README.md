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
sh client_pc/dong_goi_windows.sh                    # HTBB.exe (Launch4j) + JRE Temurin -> zip / bộ cài / tar.gz (tải công cụ)
java -jar client_pc/out/HTBB-PC.jar [--may-chu host:cong] [--cua-so 1920x1080] [--toan-man] [--khong-am] [--fps]
```

Ra `client_pc/out/`: `HTBB-PC.jar` (fat jar), `HTBB.sh` / `HTBB.bat` / `HTBB.command`, `am/` (soundbank),
`GHI_CONG.txt`, `app-image/HTBB/` (nếu có jpackage; `APP_IMAGE=0` để bỏ); `out/goi/` khi đóng gói Windows.

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

## Kiểm thử (không cần màn hình)

```sh
sh client_pc/build.sh && O=$(mktemp -d) && javac -cp client_pc/out/build/cls -d $O client_pc/test/KiemPc.java \
  && java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemPc
```
Khung cố định / phóng / viền / đổi toạ độ chuột ở nhiều cỡ cửa sổ, chữ bộ gõ (InputMethodEvent) và KEY_TYPED, lọc dán,
hàng tên trên đầu, hộp thoại đáy tự tắt, vùng bản đồ.
