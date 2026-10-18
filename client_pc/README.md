# pc-java — bản PC (Java SE) Huyền Thoại Bảo Bối

Bản PC **chạy đúng code game của bản jar J2ME** (`client_jar/src` + `client_jar/shared/mod`) trên một lớp tương thích MIDP cho
máy tính (Java SE 8+, AWT/Java2D) — cùng cách làm với bản APK (`client_apk/pokemon`). Một nguồn duy nhất: mỗi lần
build, `port-j2me.py` chép code jar vào `out/build/gen` và vá tối thiểu (mẫu vá lệch là **dừng build**). Bản Unity
`client_pc_ios/` giữ nguyên. Khi gộp monorepo, đề xuất đặt thư mục này ở **`client_pc/`** (Unity ở `client_pc_ios/`).

```
client_pc/
  build.sh               build -> out/HTBB-PC.jar + HTBB.sh / HTBB.bat / HTBB.command (+ app-image jpackage)
  dong_goi_windows.sh    đóng gói: HTBB.exe (Launch4j) + JRE Temurin đi kèm -> zip portable, bộ cài Inno Setup, tar.gz Linux
  port-j2me.py           chép + vá code jar (TField, Font -> chữ HD, GiaoDien -> khung HD, thu phóng thế giới, nút lệnh...)
  src/javax/microedition/**  lớp MIDP: lcdui (Graphics/Image/Font/Canvas/Display/TextBox), rms (tệp), io (java.net),
                         media (WAV = javax.sound.sampled, MIDI = javax.sound.midi), midlet
  src/htbb/pc/**         cửa sổ, bàn phím/chuột, lớp HD (ChuHD, GiaoDienHD, HieuUngHD, Zoom, LopPhu, ThanhLenh, Kinh),
                         cài đặt, hộp nhập, kiểm chồng chữ
  res/phong/             Noto Sans + Baloo 2 (SIL OFL 1.1, giấy phép kèm theo)
  res/am_hq/             tiếng hiệu ứng 44.1 kHz / 16 bit (sinh bởi tools/am_thanh_hq.py từ nguồn Kenney CC0)
  res/soundbank/         TimGM6mb.sf2 (GPL-2, phát hành TÁCH RIÊNG cạnh jar - xem GIAY_PHEP.txt)
  tools/am_thanh_hq.py   hồ sơ HQ của client_jar/tools/lam_am_thanh.py (cùng bảng tiếng)
```

## Build

```sh
sh client_pc/build.sh                         # bản THỬ: server 127.0.0.1:19129 (HTBB-Ban-Thu)
MAY_CHU=192.168.1.5:19129 sh client_pc/build.sh
KIEU=phat-hanh sh client_pc/build.sh          # BẮT BUỘC MAY_CHU_CHINH=host:cong hoặc MAY_CHU_CHINH= trong tools/phat-hanh/may_chu.conf
java -jar client_pc/out/HTBB-PC.jar [--may-chu host:cong] [--cua-so 1920x1080] [--toan-man] [--du-lieu <thư mục>] [--chu-anh] [--khong-am] [--fps]
```

Cần JDK 9+ (`javac --release 8`, mặc định JDK 21/17 trong `/usr/lib/jvm`) và python3. Phiên bản / số bản đọc
`tools/phat-hanh/phien_ban.conf`. Máy chủ lúc chạy: `--may-chu` > biến `HTBB_MAY_CHU` > `may_chu=` trong
`cai_dat.properties` > máy chủ gắn lúc build.

Dữ liệu người chơi (RMS = tài khoản nhớ, cài đặt, ảnh chụp F12, nhật ký `htbb-pc.log`):
Windows `%APPDATA%\HTBB`, macOS `~/Library/Application Support/HTBB`, Linux `~/.local/share/htbb`.

## Đóng gói (Windows .exe như game PC bình thường)

```sh
sh client_pc/dong_goi_windows.sh              # chạy trên Linux; KIEU=phat-hanh cho bản phát hành; CHEP_VAO=<thư mục> để chép ra
```

- **Launch4j 3.50** (BSD-3, bản Linux, md5/SHA-256 kiểm) bọc `HTBB-PC.jar` thành `HTBB.exe` (biểu tượng
  `client_jar/shared/brand/htbb.ico`, thông tin phiên bản, `-Xmx512m -Dfile.encoding=UTF-8 -Dsun.java2d.uiScale=1`), JVM =
  `jre\` cạnh exe (**người chơi không cần cài Java**).
- **Eclipse Temurin 21 JRE** x64 Windows + Linux (adoptium.net), SHA-256 kiểm theo API Adoptium.
- Ra `out/goi/`: `HTBB-PC-Windows-<ver>.zip` (portable: `HTBB/HTBB.exe`, `jre/`, `am/`, `GHI_CONG.txt`,
  `DOC_TRUOC.txt`, `giay_phep/`), `HTBB-PC-Setup-<ver>.exe` (Inno Setup 6.7.3 chạy qua wine nếu máy có wine;
  shortcut Start Menu + màn hình nền, gỡ cài đặt), `HTBB-PC-Linux-<ver>.tar.gz` (`HTBB.sh` + JRE Linux + `.desktop`).
- `sun.java2d.uiScale=1`: vẽ đúng điểm ảnh thật (không bị Windows phóng mờ); lần chạy đầu tự chọn cửa sổ 16:9
  lớn nhất vừa ~85% màn hình (màn 4K -> 2560x1440).

**Trên Windows (không có Linux)**: cài JDK 21 + WiX 3, rồi
`jpackage --type exe --input out --main-jar HTBB-PC.jar --main-class htbb.pc.Chay --name HTBB --icon ..\shared\brand\htbb.ico --app-version 0.9.0 --java-options "-Xmx512m -Dfile.encoding=UTF-8 -Dsun.java2d.uiScale=1" --win-menu --win-shortcut --win-dir-chooser`
(`--type msi` cho MSI). jpackage tự jlink JRE riêng vào bộ cài. Hoặc Launch4j bản Windows với cùng
`out/goi/launch4j.xml`, và `ISCC.exe out\goi\htbb.iss`.

**macOS**: `HTBB.command` (cạnh `HTBB-PC.jar`, cần Java 17+), hoặc trên máy Mac:
`jpackage --type dmg --input out --main-jar HTBB-PC.jar --main-class htbb.pc.Chay --name HTBB --icon htbb.icns`
(chuyển `client_jar/shared/brand/icon_1024.png` -> `.icns` bằng `iconutil`). Chưa ký / notarize.

## Điều khiển

WASD / mũi tên đi · Space đánh (ô giữa) · 1–5 ô chiêu (nhãn ô = phím PC) · Q / E bình HP / MP · Tab đổi mục tiêu
(Shift+Tab lùi) · Enter chat · Esc menu / quay lại · M menu phím # · R đổi bộ chiêu · P tổ đội · F1 bật/tắt gợi ý
phím · F3 / F2 phím mềm trái / phải · F10 cài đặt · F11 hoặc Alt+Enter toàn màn · F12 chụp màn · con lăn / + −
thu phóng bản đồ (trong màn chơi), cuộn danh sách (ngoài màn chơi) · chuột trái chạm / chọn (chạm bản đồ nhỏ = tìm
đường) · chuột phải quay lại. Đổi phím: F10 -> Phím (lưu `cai_dat.properties`). Gõ tiếng Việt bằng bộ gõ của máy.

## Trình bày HD

- Giao diện (HUD, menu, hộp thoại) ở độ phân giải logic = cửa sổ chia hệ số nguyên k (cạnh ngắn ≥ 320: 1280x720 ->
  640x360 x2, 1920x1080 -> x3, 2560x1440 -> x4, 21:9 -> 853x360 x3, 4:3 -> 512x384 x2); game vẽ thẳng vào bộ đệm cỡ
  thật (ảnh pixel phóng láng giềng gần nhất).
- **Thế giới phóng riêng** (`Zoom`): m điểm ảnh thật / px thế giới, nguyên, ~20 x 11 ô (720p ×4, 1080p ×6, 1440p ×8);
  camera của game theo nhân vật, kẹp mép bản đồ; bản đồ nhỏ hơn vùng nhìn: căn giữa + lề "bản đồ mờ" (không đen).
  Hạt / thời tiết / đêm nằm trong lớp thế giới (cắt theo khung bản đồ). Con lăn / + − chỉnh, lưu `zoom_bu`.
- **Chữ HD** (`ChuHD`): font ảnh của game -> Noto Sans (màu / viền / cỡ tự suy từ ảnh font). **Khung HD**
  (`GiaoDienHD`): cùng hệ thiết kế với bản APK (`client_apk/.../hd/HdVe.java` — cùng hàm, token màu, bo góc, bóng, kính).
- **Nút trong ngữ cảnh** (`ThanhLenh`): thanh phím mềm J2ME bị tắt; lệnh trái/giữa/phải thành nút kính gắn dưới
  khung đang mở (nút chính đỏ "Enter", "Esc", "F3"), di chuột sáng lên, bấm = đúng phím mềm.
- **Hiệu ứng** (`HieuUngHD`, Cài đặt -> Hiệu ứng Cao / Thấp): luồng trình bày 60 khung/giây (khung game vẫn 25/giây
  như J2ME), quầng sáng + vòng sóng + tia theo màu hệ khi trúng đòn, cột sáng / vòng đôi / sao khi lên cấp / tiến hoá,
  khói tan khi Pokémon hoang gục, chuyển màn mờ dần, viền tối nhẹ, sương mù / nắng gắt HD.
- **Âm thanh**: WAV 44.1 kHz / 16 bit (`res/am_hq`) thay tiếng 8 bit của jar; nhạc MIDI qua Gervill + soundbank
  TimGM6mb, 2 làn tổng hợp -> **chuyển bài mượt** (nhỏ dần ra / vào); thanh âm lượng nhạc / hiệu ứng trong F10.
  Máy không có thiết bị âm thanh: game chạy không tiếng (không lỗi).

## Kiểm thử

- Tự động trên Xvfb: tham số JVM `-Dhtbb.lenh=<tệp>` -> tệp lệnh `chup <png>` (chụp khung đã hợp thành) và
  `kiem <txt>` (kiểm chồng chữ: hình chữ nhật mọi chữ HD của một khung, báo chữ đè nhau / ra ngoài cửa sổ, bỏ qua
  chữ bị khung vẽ sau che và chữ thế giới nằm dưới giao diện). `-Dhtbb.ghiNhap=true`: ghi phím / chuột, F9 = trạng thái.
- Bản jar không đổi: `JAVA8_HOME=/usr/lib/jvm/java-8-openjdk-amd64 MAY_CHU=127.0.0.1:19129 sh client_jar/build.sh 99`.
