# Build và kiểm thử

Cách build từng thành phần từ mã nguồn. Mọi lệnh viết **từ gốc repo** trừ khi ghi khác. Triển khai lên VPS:
[trien-khai.md](trien-khai.md). Kiến trúc và luồng dữ liệu: [kien-truc.md](kien-truc.md).

| Thành phần | Công cụ cần | Lệnh chính | Ra |
|---|---|---|---|
| Game server | JDK 9+ (biên dịch `--release 8`) | `javac` + `jar` (mục 1) | `server/htbb-server.jar` |
| Client J2ME | JDK 9+, `rt.jar` của JRE 8, python3 | `sh client_jar/build.sh` | `client_jar/out/HTBB-v<N>*.jar` |
| Android | JDK 17/21, Android SDK, python3 | `sh client_apk/build-pokemon.sh` | `client_apk/out/HTBB-android-*.apk` |
| PC Java | JDK 9+, python3 | `sh client_pc/build.sh` | `client_pc/out/HTBB-PC.jar` |
| PC Windows / Linux (gói phát hành) | JDK 17+, python3, wine (bộ cài) | `KIEU=phat-hanh sh client_pc/dong_goi_windows.sh` | `client_pc/out/goi/` (Setup .exe, .AppImage) |
| PC Windows / iOS (Unity) | Unity 6000.5.9f1 | `Unity -batchmode … -executeMethod BuildCli.Win64` | thư mục `HTBB_OUT` |
| Website | Node 22 | `cd web && npm ci && npm run build` | `web/.output/` |
| Dữ liệu / SQL | MariaDB | `vps/xuat_csdl_day_du.sh` (xuất từ CSDL) | `sql/htbb.sql` (một tệp: CSDL mới + nâng cấp) |

## 1. Game server (`server/`)

Mã Java trong `server/src` (kể cả 169 lớp lõi cũ phục hồi từ `htbb-core.jar` ở P3, xem
[giay-phep.md](giay-phep.md)), thư viện bên thứ ba trong `server/lib` (`jxl`, Connector/J 5.1.49), bản đồ `server/cMap`,
tài nguyên `server/map`. Biên dịch ở **class version 52** (`--release 8`) để jar
chạy được trên mọi JVM ≥ 8; VPS chạy OpenJDK 17 (cờ `-Xlog:gc` của `htbb-game.service` cần Java ≥ 9).

Linux / macOS (chạy ở `server/`):

```sh
cd server
LIBCP="lib/jxl-2.6.jar:lib/mysql-connector-java-5.1.49.jar"
OUT=/tmp/htbb-classes; rm -rf "$OUT"; mkdir -p "$OUT"
find src -name '*.java' > /tmp/htbb-src.txt
javac --release 8 -encoding UTF-8 -nowarn -Xlint:none -cp "$LIBCP" -sourcepath src -d "$OUT" @/tmp/htbb-src.txt
printf 'Main-Class: server.TeamServer\nClass-Path: lib/jxl-2.6.jar lib/mysql-connector-java-5.1.49.jar\n' > /tmp/htbb-mf.txt
jar cfm HTBB_NEW.jar /tmp/htbb-mf.txt -C "$OUT" .
unzip -t HTBB_NEW.jar >/dev/null && mv HTBB_NEW.jar htbb-server.jar
```

Windows: `server\build.bat` (tự tìm JDK có cả `javac.exe` lẫn `jar.exe`, ra `HTBB_NEW.jar`) rồi `server\capnhat.bat`
để thay vào `htbb-server.jar`. Jar được đóng **sạch từ `src/`** (không chồng lên jar cũ). Dấu ngăn classpath trên
Linux là `:`, trên Windows là `;`.

`htbb-server.jar` nằm trong git: VPS chạy đúng tệp jar đã commit, sửa `.java` mà không build lại jar thì deploy không
có tác dụng. Chạy thử trên máy dev: tạo `server/server.ini` từ [`vps/server.ini.linux.example`](../vps/server.ini.linux.example)
(xem [cau-hinh.md](cau-hinh.md)), nạp CSDL theo [sql/README.md](../sql/README.md), chạy login server
(`cd server/loginServer && java -jar CheckLoginSocket.jar`) rồi `cd server && java -jar htbb-server.jar`
(hoặc `server\run.bat`). Thiếu màn hình thì thêm `-Djava.awt.headless=true` (AdminPanel Swing tự bỏ qua).

### Kiểm thử server

Bộ kiểm `server/test/Kiem.java` gọi lần lượt các lớp `Kiem*` (không cần CSDL, không cần mạng; đọc tệp bằng đường dẫn
tương đối từ `server/`, nên **phải chạy ở `server/`**). Giống `.github/workflows/server-test.yml`:

```sh
cd server
LIBCP="lib/jxl-2.6.jar:lib/mysql-connector-java-5.1.49.jar"
OUT=/tmp/htbb-test; rm -rf "$OUT"; mkdir -p "$OUT"
find src test -name '*.java' > /tmp/htbb-test-src.txt
javac --release 8 -encoding UTF-8 -nowarn -Xlint:none -cp "$LIBCP" -sourcepath "src:test" -d "$OUT" @/tmp/htbb-test-src.txt
java -Djava.awt.headless=true -cp "$OUT:$LIBCP" Kiem          # mã thoát 1 = có phép kiểm hỏng
```

Windows: `server\test.bat`. Một vài phép kiểm đọc thẳng mã và tài nguyên của `client_pc_ios/`, `client_jar/`, `sql/`,
`du_lieu/`, `vps/`, `web/` và tài liệu (`KiemLienKetTaiLieu` kiểm mọi liên kết trong tài liệu `.md` trỏ tới tệp có thật).
Chạy riêng một nhóm: viết lớp nhỏ gọi `KiemXxx.chay()` rồi `System.exit(Kiem.tongKet())`.

Bot tải / bot kiểm: `tools/pokemon/kiem_tai/` (xem [tools/README.md](../tools/README.md)).

## 2. Client J2ME (`client_jar/`)

Nguồn code game duy nhất cho J2ME, Android và PC Java. Cần JDK 9+ và `rt.jar` của JRE 8 cho ProGuard
(`JAVA8_HOME=/usr/lib/jvm/java-8-openjdk-amd64` hoặc `RT_JAR=…`). Các bước: `javac --release 8` → `Down45` (hạ về
class 45, kiểm API CLDC) → `jar` → ProGuard.

```sh
MAY_CHU=127.0.0.1:19129 HTBB_ASSETS=pc sh client_jar/build.sh 97   # bản DEV: mọi jar vào server MAY_CHU, có dấu HTBB-Ban-Thu
MAY_CHU_CHINH=host:cong GOI=nhe sh client_jar/build.sh             # bản phát hành + bản nhẹ
```

| Biến | Nghĩa |
|---|---|
| `MAY_CHU_CHINH=host:cong` | máy chủ của mọi bản (MANIFEST `HTBB-May-Chu`); thiếu thì đọc `MAY_CHU_CHINH=` trong `tools/phat-hanh/may_chu.conf`; thiếu cả hai và không có `MAY_CHU` là dừng |
| `MAY_CHU=host:cong` | bản thử `-thu.jar`; chỉ đặt `MAY_CHU` = bản DEV, không phát hành |
| tham số đầu / `BAN`, `PHIEN_BAN` | số bản và phiên bản; mặc định trong `tools/phat-hanh/phien_ban.conf` |
| `HTBB_ASSETS=pc` | đóng gói lại sprite Pokémon (`res/pk`, `res/pt`, `res_nhe/pk`) từ `client_pc_ios/Assets` và `du_lieu/*.csv` |
| `GOI=nhe` | thêm bản nhẹ cho máy yếu (ít khung, chân dung nhỏ, âm thanh rút gọn) |

Ra `client_jar/out/`: `HTBB-v<N>.jar`, `HTBB-v<N>-release.jar` (ProGuard; `mapping-v<N>.txt` giữ riêng),
`-thu.jar`, `-nhe.jar`. `res/GHI_CONG.txt` (ghi công PMDCollab) bắt buộc có — xem [giay-phep.md](giay-phep.md).
Kiểm vẽ mọi bản đồ: `sh client_jar/tools/kiem_ve_map/chay.sh /tmp/kiem_ve_map` (số `LOI` phải là 0). Chi tiết:
[client_jar/README.md](../client_jar/README.md).

## 3. Android (`client_apk/`)

Module Gradle `:pokemon`. Lúc build, `client_apk/port-j2me.py` chép code jar (`client_jar/src` + `client_jar/shared/mod`)
vào `pokemon/build/generated/htbb-j2me` và vá tối thiểu — mẫu vá lệch là dừng build. Cần JDK 17/21 (`JAVA_HOME`),
Android SDK (`ANDROID_HOME`, mặc định `~/Android/Sdk`), python3.

```sh
sh client_apk/build-pokemon.sh                                 # debug, server 10.0.2.2:19129 (máy dev nhìn từ máy ảo)
MAY_CHU=192.168.1.5:19129 sh client_apk/build-pokemon.sh       # debug nối server trong LAN
KIEU=release MAY_CHU=host:cong sh client_apk/build-pokemon.sh  # release -> client_apk/out/HTBB-android-<BAN>.apk
```

Release bắt buộc có máy chủ (`MAY_CHU`, `MAY_CHU_CHINH` hoặc `tools/phat-hanh/may_chu.conf`). `BAN` (versionCode) /
`PHIEN_BAN` (versionName) mặc định từ `tools/phat-hanh/phien_ban.conf`.

**Ký bản phát hành.** Khoá luôn nằm **ngoài repo** (`.gitignore` chặn `*.jks`, `*.keystore`, `key.properties`). Tạo
một lần và sao lưu hai nơi — mất khoá là người chơi phải gỡ app mới lên được bản mới:

```sh
keytool -genkeypair -v -keystore "$KEY_DIR/htbb.jks" -alias htbb -keyalg RSA -keysize 4096 -validity 10000 -dname "CN=HTBB"
```

- Cách A: `$KEY_DIR/key.properties` có `keystore=`, `alias=`, `password=` → Gradle tự ký khi build release với `KEY_DIR`.
- Cách B (mật khẩu chỉ ở biến môi trường): build APK chưa ký rồi
  `KEY_DIR=… HTBB_KEYSTORE=htbb.jks HTBB_KEY_ALIAS=htbb HTBB_KS_PASS=… sh client_apk/ky-ban-phat-hanh.sh client_apk/out/HTBB-android-<BAN>.apk`
  (zipalign + apksigner v2/v3, in vân tay SHA-256; từ chối nếu `KEY_DIR` nằm trong repo git).

Chi tiết: [client_apk/README.md](../client_apk/README.md).

## 4. PC Java (`client_pc/`)

Chạy đúng code jar trên lớp MIDP cho Java SE (AWT/Java2D); `port-j2me.py` chép code vào `client_pc/out/build/gen` mỗi
lần build.

```sh
sh client_pc/build.sh                                          # bản thử: server 127.0.0.1:19129
KIEU=phat-hanh MAY_CHU_CHINH=host:cong sh client_pc/build.sh   # bản phát hành
KIEU=phat-hanh sh client_pc/dong_goi_windows.sh                # bộ cài Windows + AppImage Linux (+ zip, tar.gz, jar)
java -jar client_pc/out/HTBB-PC.jar --may-chu host:cong        # chạy thử
```

`dong_goi_windows.sh` (0.9.5) tải công cụ một lần vào `client_pc/out/cong_cu/` (ghim phiên bản, kiểm SHA-256, lệch là
dừng: Temurin 21 JDK Windows + Linux để lấy jmods, appimagetool 1.9.1 + type2-runtime 20251108, Inno
Setup 6.7.3 qua wine) rồi:

- làm rối mã game bằng ProGuard (`client_pc/release-pc.pro`, bản đồ tên `out/goi/mapping-pc.txt` giữ riêng);
- dựng Java TỐI GIẢN bằng `jlink` (module theo `jdeps`: java.base, java.desktop, jdk.management + jdk.crypto.ec,
  jdk.localedata en/vi; `--strip-debug --no-header-files --no-man-pages --compress=zip-9`), ~50-60 MB thay JRE đầy đủ;
- Windows: `HTBB.exe` (0.9.11: trình mở tự viết `client_pc/khoi_chay/` - nạp jvm.dll trong tiến trình, bảng mã ANSI
  UTF-8, biên dịch bằng mingw-w64 `x86_64-w64-mingw32-gcc` + `windres`; chạy được từ thư mục có dấu, thay Launch4j 3.50
  đầu ANSI) + `HTBB-PC.jar` (đã làm rối) + `runtime\` -> `HTBB-PC-Setup-<ver>.exe` (+ zip cùng bố cục);
- Linux: `HTBB-PC-Linux-<ver>.AppImage` một tệp (AppRun + runtime + jar trong squashfs), thêm tar.gz (`HTBB/HTBB` +
  `runtime/`, jar game ở `runtime/lib/htbb/`).

Không còn tệp `.jar` rời của game trong gói phát hành (jlink luôn để lại `runtime/lib/jrt-fs.jar` - tệp của JDK).
Dữ liệu người chơi: `%APPDATA%\HTBB` (Windows), `~/.local/share/htbb` (Linux). Chi tiết:
[client_pc/README.md](../client_pc/README.md).

## 5. Unity: PC Windows và iOS (`client_pc_ios/`)

Unity **6000.5.9f1**. Mã game `client_pc_ios/Assets/Scripts/Assembly-CSharp/`, kịch bản build
`client_pc_ios/Assets/Editor/BuildCli.cs`.

```sh
Unity -batchmode -quit -projectPath client_pc_ios -executeMethod BuildCli.Win64 -htbbMayChu host:cong   # đường ra: biến HTBB_OUT
Unity -batchmode -quit -projectPath client_pc_ios -executeMethod BuildCli.iOS                           # xuất project Xcode
```

Bản phát hành bắt buộc có máy chủ (`-htbbMayChu`, `MAY_CHU_CHINH` hoặc `tools/phat-hanh/may_chu.conf`). CI:
`.github/workflows/client-phat-hanh-htbb.yml` (Windows qua GameCI) và `.github/workflows/client-ios.yml` (IPA chưa ký);
cần secret `UNITY_LICENSE`, `UNITY_EMAIL`, `UNITY_PASSWORD`. Chi tiết: [client_pc_ios/README.md](../client_pc_ios/README.md).

## 6. Website (`web/`)

Nuxt 4, cần **Node 22** (≥ 22.12, xem `web/.nvmrc`).

```sh
cd web
npm ci
npm run dev          # http://localhost:3000, đọc .env (chép từ .env.example)
npm run build        # -> .output/ ; chạy: node .output/server/index.mjs
npm test             # node --test tests/*.test.ts
npm run typecheck
HTBB_DIR=.. npm run sinh-anim-khoi-dau   # ảnh động màn khởi đầu từ sprite client
```

`web/shared/data/*.json` và `web/shared/utils/*.gen.ts` là bản dự phòng tĩnh (web đọc CSDL trước; test đối chiếu với
`sql/htbb.sql`). Trên CI, `.github/workflows/web-deploy.yml`
build rồi đẩy `.output` lên VPS. Chi tiết: [web/README.md](../web/README.md).

## 7. Dữ liệu, SQL và tệp sinh

- **Dữ liệu game và cấu hình trung tâm** nằm trong CSDL (sửa trên trang quản trị). Không có bước sinh: repo giữ MỘT tệp
  `sql/htbb.sql`: phần A = bản chụp đầy đủ (xuất bằng `vps/xuat_csdl_day_du.sh`, chỉ nạp vào CSDL mới), phần B = nâng cấp
  CSDL đang chạy (viết tay + khối sinh) — cùng tệp cho máy mới lẫn production, chạy lại được, xem
  [sql/README.md](../sql/README.md). Bộ sinh cũ: `tools/luu_tru_sinh_du_lieu/` (lưu trữ).
- `du_lieu/*.csv` chỉ còn bảng tài nguyên (sprite, tile, bản đồ) — [du_lieu/README.md](../du_lieu/README.md),
  [tools/README.md](../tools/README.md).
- Chạy công cụ Python **ở gốc repo**: `python3 -I tools/pokemon/<tên>.py`.

## 8. Phát hành client (`tools/phat-hanh/`)

Ba bản dùng chung **một phiên bản + một số bản** (`tools/phat-hanh/phien_ban.conf`: `PHIEN_BAN`, `BAN` chỉ tăng, quy
ước `major*10000 + minor*100 + patch`) và một máy chủ production (`tools/phat-hanh/may_chu.conf`, không nằm trong
git, chép từ `may_chu.conf.example`).

```sh
THU=1 sh tools/phat-hanh/phat-hanh.sh jar                        # chỉ build vào tools/phat-hanh/tam/, không đẩy
REPO=<chủ>/<repo-phát-hành> sh tools/phat-hanh/phat-hanh.sh jar     "ghi chú"
REPO=<chủ>/<repo-phát-hành> sh tools/phat-hanh/phat-hanh.sh android "ghi chú"   # cần KEY_DIR + HTBB_KEYSTORE…
REPO=<chủ>/<repo-phát-hành> sh tools/phat-hanh/phat-hanh.sh pc      "ghi chú"   # máy Windows có Unity
```

`day-len.sh` đẩy bản build lên GitHub Releases của repo phát hành và ghi `phienban.json`. Biến và secret của workflow:
[tools/phat-hanh/README.md](../tools/phat-hanh/README.md).

**Một thư mục bản phát hành (0.9.8, luật chủ game)**: build xong APK (`KIEU=release`), jar (`client_jar/build.sh`, có
`GOI=nhe`) và gói PC (`KIEU=phat-hanh sh client_pc/dong_goi_windows.sh`) thì gom vào **một** thư mục cạnh repo, chỉ giữ
bản mới nhất (mỗi lần gom xoá bản cũ), kèm `SHA256SUMS.txt` — không còn thư mục theo phiên bản (`phat_hanh_0.9.8`...):

```sh
sh tools/phat-hanh/gom-ban.sh            # -> <thư mục chứa htbb>/phat_hanh/ (vd ~/Downloads/pokemon/phat_hanh)
RA=/đường/khác sh tools/phat-hanh/gom-ban.sh
```

Tên tệp: `HTBB-<PHIEN_BAN>-android.apk`, `HTBB-<PHIEN_BAN>.jar`, `HTBB-<PHIEN_BAN>-nhe.jar`, `HTBB-PC-Setup-<x.y.z>.exe`,
`HTBB-PC-Linux-<x.y.z>.AppImage` (+ `.tar.gz`), `HTBB-PC-Windows-<x.y.z>.zip`.

### Số bản và cập nhật client (0.9.7)

- **Mọi bản phát hành công khai, kể cả bản vá nóng, có `BAN` mới lớn hơn bản trước** (máy chủ chỉ phân biệt client bằng
  số này: 0.9.5.1 và 0.9.6.1 từng giữ nguyên `BAN` nên máy chủ không tách được). Quy ước `BAN = major*10000 + minor*100
  + patch` và `PHIEN_BAN` chỉ còn **3 phần số** khớp `BAN` (+ hậu tố tuỳ ý): bản vá của 0.9.7 là 0.9.8 (`BAN=908`), không
  dùng 0.9.7.1. Nhờ vậy `MIDlet-Version` của jar (MIDP chỉ nhận a.b.c, lấy phần số của `PHIEN_BAN`) luôn tăng theo.
  Sau 0.9.6.1 (`BAN=906`) là 0.9.7 (`BAN=907`) - bản đầu tiên có hộp cập nhật trong game.
- Máy chủ (`com.htbb.capnhat.CapNhatClient`) so số bản client gửi lúc đăng nhập với hai mức đặt trên web:
  **bản mới nhất** = ô *Số bản* từng nền tảng ở `/admin/tai-game` (dự phòng `sv.banMoiNhat`) -> thẻ "Đã có bản mới" (Cập
  nhật ngay / Để sau, nhắc một lần mỗi ngày); **bản tối thiểu** = `sv.banToiThieu` ở `/admin/cau-hinh-he-thong` -> hộp
  "Cần cập nhật để tiếp tục chơi", không vào được game. Gói -115 (client >= 907); client cũ nhận hộp chữ kèm link tải.
- "Cập nhật ngay": APK tải trong game (tiến độ, kiểm SHA-256) rồi mở trình cài Android (cùng khoá ký = giữ dữ liệu); PC
  Windows tải bộ cài rồi chạy `/SILENT` (nâng cấp tại chỗ, tự mở lại); PC Linux tải AppImage vào cạnh `$APPIMAGE`, thay
  nguyên tử rồi mở lại; J2ME (và khi thiếu SHA-256 / không cài được tại chỗ) mở link bằng trình duyệt.

**Ra một bản mới:**

1. Tăng `BAN` + `PHIEN_BAN` trong `tools/phat-hanh/phien_ban.conf` (và `ThuongHieu.PHIEN_BAN` của Unity), build các gói.
2. `/admin/tai-game`: với từng nền tảng bấm *Tải tệp lên* (tự điền link, dung lượng, **SHA-256**), sửa *Phiên bản*,
   *Số bản* (= `BAN`), *Có gì mới* rồi **Lưu**. Trong ≤ 60 giây người chơi bản cũ thấy "Đã có bản mới".
3. Cần ép (bản cũ lỗi nặng / đổi giao thức): đặt *Bản tối thiểu* = `BAN` ở `/admin/cau-hinh-he-thong` sau khi mọi nền tảng
   đã có tệp mới (nền tảng chưa có tệp đạt mức thì máy chủ chỉ nhắc, không khoá người chơi).
4. Muốn tắt nhắc tạm thời: đặt *Số bản* của nền tảng về 0 (hoặc `sv.nhacCapNhat=0` trong server.ini).

Kiểm: `server/test/KiemCapNhatClient` (hai mức, gói -115, hộp client cũ); `client_pc/test/KiemCapNhat` (máy chủ HTTP
cục bộ: tải + SHA-256 + tiến độ, SHA lệch / 404 / tải thiếu / huỷ bị từ chối, AppImage thay nguyên tử, bộ cài Windows,
mở link khi không tự cài được; vẽ hộp ở mọi cỡ chuẩn) - lệnh chạy như `KiemThongBao` (mục 4 / client_pc/README.md).

### Bản 0.9.18 (`BAN=918`: sửa "game chậm như slow motion" 0.9.15-0.9.17 - vòng game bước cố định)

Client (không gói mới, không đổi máy chủ) `PHIEN_BAN=0.9.18-opentest` (`BAN = 0*10000 + 9*100 + 18`; `MIDlet-Version`
0.9.18), chạy với mọi máy chủ 0.9.15+.

1. Lỗi (chủ dự án / người chơi sau 0.9.15-0.9.17: "game giờ chậm quá, kiểu như slow motion"): `Canvas.run` chạy MỘT nhịp
   logic mỗi vòng rồi vẽ các khung của nhịp đó và ngủ tới "đầu nhịp + 40 ms" - vẽ quá giờ là nhịp sau trễ, cả game (đi,
   hoạt hình, đánh, hồi chiêu theo nhịp) chậm theo khung. 0.9.15 bật APK 60 khung (mặc định) và dồn MỌI khung vào
   `NhipVe.veThem` (vòng vẽ theo `keTiep` chứ không theo giờ thật: vẽ chậm vẫn vẽ đủ 2-3 khung mỗi nhịp) nên trên Android
   (trình bày chờ vsync 60 Hz) game chạy 60-80 % ngay cả máy nhanh, 26 % khi vẽ 20 ms. Đo `KiemTocDoGame` (vòng game thật,
   máy chậm giả lập; tốc độ game = nhịp logic / 25, đi px / 125):

   | Đường | vẽ 0 ms | 10 ms | 20 ms | 30 ms | 45 ms | 70 ms |
   |---|---|---|---|---|---|---|
   | 0.9.14 J2ME / APK (vẽ mỗi nhịp) | 99 % | 100 % | 100 % | 80 % | 79 % | 48 % |
   | 0.9.14 PC 60 | 98 % | 80 % | 53 % | 51 % | 56 % | 37 % |
   | 0.9.17 PC 60 | 98 % | 80 % | 43 % | 27 % | 27 % | 19 % |
   | 0.9.17 APK 60 (vsync) | 60-80 % | 60-80 % | 27 % | 26 % | 26 % | 16 % |
   | 0.9.17 APK 30 (vsync) | 90 % | 82 % | 60 % | 50 % | 26 % | 15 % |
   | 0.9.17 J2ME | 100 % | 100 % | 100 % | 80 % | 75 % | 48 % |
   | **0.9.18 mọi đường** | 99-100 % | 100-101 % | 99-101 % | 100-101 % | 99-101 % | 100-101 % |

2. Sửa: vòng game **bước cố định** - logic theo lịch 25 nhịp / giây thời gian thật (`nhipKe += 40`), trễ thì chạy bù tối
   đa `NhipVe.BU_TOI_DA` = 5 nhịp trước một khung, trễ hơn thì bỏ phần thời gian (`NhipVe.boMs`, không xoáy chết); vẽ
   bao nhiêu khung tuỳ máy (`NhipVe.veKhung`: lưới đều 1000/fps, MỘT khung mỗi lần, lỡ hơn một bước thì bỏ ô; fps <= 25 /
   J2ME: một khung sau mỗi lượt nhịp), nội suy theo giờ LỊCH của nhịp, ngủ đúng tới việc kế tiếp (tối thiểu 1 ms - J2ME một
   lõi). Chỉ CLDC 1.1 (không java.util.concurrent).
3. `serviceRepaints` (PC `NenPc`, APK `NenAndroid`): khung đã được luồng vẽ nhận nhưng chưa vẽ xong cũng phải chờ - trước
   đây trả về ngay, nhịp logic chạy SONG SONG với `Canvas.paint` đang giữ vị trí NỘI SUY (đọc / ghi vị trí lùi một phần
   nhịp: `moveTo(x, y)` khi bấm đánh lấy đích lùi, gói vị trí dừng lùi).
4. APK: chọn 60 khung mà máy không theo kịp 3 giây liền (< 75 % số khung) -> tự hạ 30 (`NhipVe.tuHa`), lưu lựa chọn
   (`Hud.kiemTuHa`, `khung60=false`) và báo "Máy vẽ chậm: tự chuyển 30 khung/giây"; chọn lại 60 ở Chỉnh nút.
5. Kiểm mới: `KiemTocDoGame` (PC 60 / APK 60 tự hạ / APK 30 / J2ME x vẽ 0..70 ms: 25 nhịp / giây và 125 px / giây ±3 %,
   máy nhanh >= 50 khung p90 <= 25 ms), `KiemKeoLui` (đi rồi chạm nút đánh thường, quái gần 16 px / xa 120 px, máy chủ giả
   kiểm gói đi bằng `real.TocDoDiChuyen`: không gói nào bị từ chối, không lùi, đứng yên sau khi đánh, vị trí máy chủ =
   client). `NenPc.giaLapVeMs` / `giaLapVsync` (mặc định tắt) giả lập máy chậm cho hai kiểm này. Ngưỡng tốc độ máy chủ
   (`TocDoDiChuyen`: quỹ 1,5 x tốc độ / 40 ms, trần 2 x 50 px) hợp với bước cố định (trung bình đúng 1 x, chạy bù tối đa
   5 nhịp = 25 px) - không đổi máy chủ.

### Bản 0.9.17 (`BAN=917`: Menu › Sự kiện mở thẻ Sự kiện, thẻ menu máy chủ không lấy tên quái đang nhắm, kiểm đích mọi nút HUD)

Client (không gói mới) `PHIEN_BAN=0.9.17-opentest` (`BAN = 0*10000 + 9*100 + 17`; `MIDlet-Version` 0.9.17), chạy với mọi
máy chủ 0.9.15+ (chỉ dùng -527 / -76 / -30 đã có).

1. Menu › Sự kiện (chủ dự án, PC 0.9.16: "bấm vào sự kiện sao lại ra cái này vậy" - nhắm Ekans, bấm Sự kiện ra thẻ tiêu
   đề "Ekans" với Mở Túi Đồ / Bỏ đồ vào túi / Mở Kho / Shop Đặc Biệt / Tắt lời mời / Thêm). Không phải lỗi mới của 0.9.16:
   từ 0.9.3 ô này gửi -76 / -10000 = menu "Khác" của game gốc (`ManMenu.lam` I_SU_KIEN), và thẻ lệnh tiếp quản menu máy
   chủ (`MenuNguCanh.tiepQuan` -> `mo(v, true)`) luôn lấy `focusedActor.getDisplayName()` làm tiêu đề. Nay ô mở thẻ
   **"Sự kiện"** ở client (`GameScr.moSuKien`): dòng đầu = mùa / ngày cộng đồng đang diễn ra từ gói -118 ("Mùa: Hoàng hôn
   Halloween", chạm = thẻ thời tiết & mùa), rồi Quà hoạt động hôm nay / Lịch đăng nhập, vé chương / Nhiệm vụ tuần / Cúp PvP
   tuần / Hầm ngục bí ẩn (mục gốc Sổ tay `-527/0/i`, chỉ số tra theo tên trong `SO_TAY_MUC`; ChupTrongGame so với
   `real.SoTayLauDai.MUC_GOC`). Menu "Khác" của máy chủ giữ một lối vào: Tiện ích & PK › Bản thân › **Kho & tiện ích**
   (trước là "Khác", ẩn ở menu chữ gọn vì "= Menu › Sự kiện").
2. Tiêu đề thẻ menu máy chủ (`MenuNguCanh`): yêu cầu gửi từ Menu / Sổ tay / Kho & tiện ích / lệnh của thẻ có tiêu đề riêng
   đặt tiêu đề chờ (`choTieuDe`, 6 s) -> menu máy chủ trả về mang tiêu đề nơi gửi ("Sự kiện", "Sổ tay huấn luyện", "Nhiệm vụ
   của tôi", "Kho & tiện ích"), không neo cạnh mục tiêu; chạm NPC / người chơi (`HanhDong.chamThe`, chuột phải,
   `GameService.requestNPCInfo`) xoá chờ -> thẻ của NPC vẫn mang tên NPC + chân dung. Quái hoang (`Monster`) không bao giờ
   là tiêu đề thẻ lệnh (trước: Nhiệm vụ của tôi / mục Sổ tay khi đang nhắm quái cũng ra thẻ "Ekans").
3. Kiểm `KiemChamHud` (thêm `kiemDichNut`, chạy ở 11 cỡ, cả `-Dchup.j2me`): 4 cảnh (không mục tiêu / nhắm Ekans + thẻ thời
   tiết mở như ảnh / nhắm Ekans / nhắm NPC) x mọi nút HUD (thẻ người chơi, Hành trang, Nhiệm vụ, Bản đồ, Menu, Cài đặt, thời
   tiết, chat, AUTO, bánh răng AUTO, thuốc HP / PP, đánh, 4 chiêu, 1/2, câu cá / ném bóng) -> đúng màn / bảng / gói; mọi ô
   Menu cá nhân (3 cảnh) -> đúng màn / thẻ / gói, kể cả menu máy chủ giả trả về (tiêu đề nơi gửi); tâm mỗi ô trúng đúng vùng
   (thẻ thời tiết không cướp chạm); Kho & tiện ích -> -76 + thẻ "Kho & tiện ích"; menu máy chủ lạc khi nhắm quái không
   mang tên quái; chạm NPC -> thẻ tên NPC. Bảng đích ghi ra `dich_nut.txt`. Trên mã 0.9.16 kiểm này hỏng 27 chỗ ở pc720
   (đúng thẻ "Ekans" [Mở Túi Đồ, Bỏ đồ vào túi, Mở Kho, Shop Đặc Biệt, Tắt lời mời...] của ảnh). `ChupTrongGame` thêm màn
   `menu_su_kien` (thẻ canh giữa, tiêu đề + 6 hàng ở 11 cỡ). Tắt phần mới khi cần chạy nhanh: `-Dkiem.dich=0`.
4. Menu cá nhân › Sổ tay huấn luyện thêm dòng cuối **"Về làng miễn phí"** (`-527/0/11` = `real.SoTayLauDai.MUC_VE_LANG` của
   main 618e75da; máy chủ báo số lượt khi chọn). `SO_TAY_MUC` vẫn đúng 11 mục cố định (`KiemVeLangMienPhi` kiểm mảng,
   ChupTrongGame kiểm chỉ số + tên `VeLangMienPhi.TEN`). Máy chủ cũ hơn chọn chỉ số 11 thì rơi vào mục hướng dẫn tân thủ
   (chỉ hỏi lại / mở danh sách NPC, không mất gì) - phát hành cùng máy chủ main.
5. `du_lieu/parity_097.txt` (KiemParity): đoạn mã giữ viên "Tự động đánh" ở Menu nay là
   `static final int[] PHU = {I_NV_CUA_TOI, I_SO_TAY, I_TU_DONG,` (0.9.16 thêm hai viên trước nó) - sửa ở
   `tools/pokemon/sinh_parity_097.py` rồi sinh lại (chỉ dòng này đổi); bộ kiểm máy chủ 17735/17735.

### Bản 0.9.16 (`BAN=916`: tab Thú cưỡi / Đi theo, danh sách thú gọn trên điện thoại, hào quang khi cưỡi, Nhiệm vụ của tôi / Sổ tay ở Menu, thẻ menu canh giữa)

Client (không gói mới) trên main 35416507 (máy chủ Vé dịch chuyển / thuốc 162 / Gym một trận - jar máy chủ của main):
`PHIEN_BAN=0.9.16-opentest` (3 phần số khớp `BAN = 0*10000 + 9*100 +
16`; `MIDlet-Version` 0.9.16). Client 0.9.16 với máy chủ 0.9.15 / cũ hơn chạy đủ (loại thú lấy từ `typeAnimal` của gói 16
loại 2 và khối đang dùng 0.9.13 - mọi máy chủ đều gửi, không chặn theo `BAN`).

1. Danh sách thú (chủ dự án: "chia rõ ra thú cưỡi và đi theo chứ nhỉ"): Trang bị › Thú cưỡi và tab Thú có hai **tab con**
   "Thú cưỡi (n)" / "Đi theo (m)" (`ManNhanVat.veDanhSachThu`, `QuanLyThu.dsLoai`): mỗi tab chỉ một loại, con đang cưỡi /
   đang đi theo đứng đầu tab của nó. Chạm ô "Thú cưỡi" mở tab Thú cưỡi, ô "Đi theo" mở tab Đi theo, tab Thú mở lại tab con
   dùng lần cuối; phím trái / phải đổi tab con (ở mép mới đổi tab màn). "Ô thú: n/max" vẫn là tổng chung; **Thả trùng**
   chỉ thả bản trùng của tab đang xem (`QuanLyThu.banTrung(loai)`, hộp hỏi "Thả thú cưỡi trùng" / "Thả Pokémon đi theo
   trùng" + "… không bị thả"). Tab trống: "Chưa có thú cưỡi · Mua Sáo gọi ở Cửa hàng đặc biệt" / "Chưa có Pokémon đi theo ·
   Bắt hoặc ấp trứng". J2ME hẹp: nhãn "Cưỡi (n)", tiêu đề "Hành trang thú" (không cắt chữ).
2. Hào quang khi cưỡi (chủ dự án: "ở đây hào quang thì ở trên có nhân vật, còn ở ngoài nó dưới chân con thú cưỡi?"): lớp
   dưới chân (`NgoaiHinhVe.veDuoiChan`: hào quang / vòng sáng theo bậc) vẽ ở mặt đất dưới chân con CHẠM ĐẤT (thú cưỡi khi
   cưỡi, cỡ theo thú), TRƯỚC thú + người (nằm sau; trước đây bản đồ vẽ sau thú -> đè chân thú). Sân khấu Ngoại hình /
   Trang bị đang cưỡi vẽ cả thú + người + hào quang một lượt bằng `Char.paintPokemonTinh(..., voiThu)` cùng thứ tự / neo
   với `Char.paintPokemon` (người nâng `NgoaiHinhVe.nangNguoi`); trước đây hào quang ở chân người trên lưng thú.
   Kiểm: `ChupTrongGame` màn `nv_ngoai_hinh_hq[_cuoi]`, `nv_trang_bi_hq_cuoi`, `the_gioi_hq[_cuoi]` (neo hào quang lệch chân
   con chạm đất 0,0 px, lượt vẽ ngay sau là thú, người nâng 17/17) và 8 màn tab con thú (`nv_thu_dang_dung_tb_*`,
   `_tab_di_theo`, `_tab_nho`, `_tha_trung_*`, `nv_thu_trong_tab0/1`).
3. Danh sách thú trên điện thoại (chủ dự án: "menu pet / thú cưỡi trên Android hơi to, chỉ thấy được có 2"): khi bố cục
   thường thấy < 5 hàng (`ManNhanVat.gonThu`) - sân khấu hẹp 36 % (người cưỡi thú + pet / thú đang chọn, hình thu nhỏ dưới
   hai dòng chữ) + hai ô Thú cưỡi / Đi theo xếp dọc; hàng đầu [lùi][tab con] (+ Ô thú nếu đủ chỗ), hàng hai Ô thú hoặc
   thanh thao tác con đang chọn (không thêm hàng dưới); lưới 2-3 cột (ô >= 128 px logic), hàng = cỡ chạm tối thiểu. Mọi cỡ
   điện thoại (16:9 / 19.5:9 / 20:9 / 21:9, cả J2ME) thấy >= 6 thú. Máy tính bảng / PC giữ bố cục cũ.
4. Menu cá nhân (chủ dự án: "mỗi lần về làng có về trực tiếp được đâu"): Menu kiểu GO có "Nhiệm vụ của tôi" (`-527/0/8`
   như nút trong sổ Nhiệm vụ) và "Sổ tay huấn luyện" (mục gốc dựng ở client theo `real.SoTayLauDai.MUC_GOC` - thứ tự cố
   định, mỗi mục `-527/0/i`; ChupTrongGame so với mảng máy chủ) đầu hàng viên phụ; menu chữ J2ME (phím mềm) hàng "Nhiệm
   vụ" thành menu con Sổ nhiệm vụ / Nhiệm vụ của tôi / Sổ tay huấn luyện. Tiêu đề menu chữ không còn cắt ("Nhiệm...").
5. Thẻ menu NPC / máy chủ (`MenuNguCanh`, chủ dự án: "nó bị lệch nè"): luôn canh giữa ngang màn (kẹp vùng an toàn), dọc ưu
   tiên giữa, chỉ dời dọc khi đè HUD / che mục tiêu. ChupTrongGame `kiemMenuGiua`: mọi thẻ menu (1 / 2 cột, nhiều trang)
   lệch trái - phải <= 2 px. (`MenuNguCanh` chỉ có bố cục 1 hoặc 2 cột.)

### Bản 0.9.15 (`BAN=915`: sổ Nhiệm vụ ghi rõ mục tiêu, Huấn luyện, tên trang bị "· Cấp N", chống lag, kho gói, APK 60 khung)

Client + máy chủ + SQL: `PHIEN_BAN=0.9.15-opentest` (3 phần số khớp `BAN = 0*10000 + 9*100 + 15`; `MIDlet-Version`
0.9.15). Thứ tự: `nang_cap_csdl.sh` (khối `sua_muc_tieu_nhiem_vu.sql`, `loc_tu_cam_ca_tu.sql`, `doi_ten_trang_bi_cap.sql`,
`doi_icon_trang_bi.sql` - đã chạy cùng các jar máy chủ trước) -> `htbb-server.jar` (kho gói `-117`) -> client. Client 0.9.15
với máy chủ cũ vẫn chạy (không có kho gói); client cũ với máy chủ mới nhận đúng từng byte như trước.

1. Sổ Nhiệm vụ (main 2d557fa7): mỗi hàng 2-3 dòng **mục tiêu** (đếm từ dữ liệu + bảng sinh quái thật, "Mục tiêu: … ·
   Trả: …"), xong tô vàng, khoá đỏ, "của ai" ở khe đợi; nút **"Nhiệm vụ của tôi"** ở NPC 5 / 8 / 23 và trong sổ. Máy chủ
   gửi lại `-64` MỖI lần tiến độ tăng và đuôi `-64` (mục / trả / khoá / cờ) chỉ cho client `BAN >= 915` (client cũ nhận tên
   kèm mục tiêu). Trang bị: nút **"Huấn luyện"** sáng theo Điểm huấn luyện + hộp giải thích; lên cấp báo "+N Điểm huấn
   luyện" (main 74def795: một hàm thưởng chung mọi đường EXP, bù điểm lên cấp còn thiếu lúc nạp).
2. Tên trang bị trùng -> "<Tên> · Cấp N" (`doi_ten_trang_bi_cap.sql`), icon gốc đúng ô (`doi_icon_trang_bi.sql`); client
   (`ChuDe.cat` / `boCap`) giữ cấp khi cắt tên ("<Tên…> C24"), bỏ "· Cấp N" ở hàng đã hiện cấp, tên dài xuống dòng (túi, Kho
   đồ, cửa hàng, chợ, thẻ vật phẩm). Tạo Pokémon: tên 4-10 ký tự (chữ hướng dẫn), lọc từ cấm theo cả từ (`LocTuCam`).
3. Chống lag (máy chủ, main d754da33): gom ghi socket khi kết nối đang đông (CPU 300 bot 484% -> 148%, `-Dhtbb.gomGuiMs`),
   bỏ chờ 2 giây lần chọn Pokémon đầu của một IP (vào game 2,1 s -> 0,08 s). Client (d781ed06): bỏ cấp phát mỗi khung (chữ
   hoa có dấu ghép lại mỗi lần vẽ, cắt tên HUD, tên ảnh Pokémon, PP ném lỗi mỗi khung): Hành trang J2ME 50,9 -> 17,1 KB /
   khung; gửi một khung / gói (trước 4 lần ghi TCP) + đánh thức luồng gửi ngay; PC / APK đọc socket có đệm.
4. **Kho gói** (`io.KhoGoi`, client `network.KhoGoi`): client `BAN >= 915` giữ gói dữ liệu lớn 25 / 26 / 1 / -108 / 105 trong
   RMS (mỗi mục một kho `gc<i>_<o>`, khúc 8 KB, băm FNV-1a 64) và khai (lệnh, băm, độ dài) ở gói đăng nhập (bit khả năng 4);
   gói đã có -> máy chủ gửi `-117` kiểu 1 (21 byte) thay gói gốc, gói mới / đã đổi -> `-117` kiểu 0 + gói gốc (client lưu).
   Chỉ khai mục đọc lại đủ và đúng băm; đọc kho hỏng lúc dùng -> xoá kho, tắt kho cả phiên, ngắt, tự nối lại nhận đủ (không
   vòng lặp); RMS không mở được / đầy -> không lưu, nhận đủ như cũ. **J2ME không dùng kho** (không báo bit 4): giả lập heap
   7 MB giữ RMS trong heap, lưu gói 25 / 26 làm 2/3 lần vào game hết bộ nhớ. Đo (PC thật, trễ 150 ms, 40 KB/s): vào thế giới
   4,88 s / 141 KB (lần đầu) -> 1,86 s / 7-15 KB (đã có kho). Tắt: `sv.khoGoi=0`. Kiểm: `server/test/KiemKhoGoi`,
   `client_pc/test/KiemKhoGoi.java` (`network.KiemKhoGoi`).
5. **APK 60 khung/giây** (nội suy vị trí như PC, `game.pokemon.NhipVe`): mặc định 60, bảng Chỉnh nút có "Khung hình: 60
   khung/giây / 30 (tiết kiệm pin)" (trước 0.9.15: 25 khung, lệch nhịp màn 60 Hz). J2ME giữ 25. Mọi khung > 25/giây vẽ theo
   lưới đều (bỏ khung lệch ngay sau logic): 60 khung khoảng p90 29 -> 18 ms. Kiểm: `client_pc/test/KiemNhipKhung`.
6. Máy chủ khác (không chặn theo `BAN`): đổi tuần chung thứ Hai 05:00 (cúp, tuần Pokémon, Đấu trường, vùng hoang dã), ngắm
   tay PvP sát mép tầm, MOBA chưa mở trận từ chối `mto`.

### Bản 0.9.14 (`BAN=914`: thẻ vật phẩm đúng loại / mô tả, chữ client, thẻ lý do 2 dòng, chợ đấu giá)

Client + máy chủ + SQL: `PHIEN_BAN=0.9.14-opentest` (3 phần số khớp `BAN = 0*10000 + 9*100 + 14`; `MIDlet-Version`
0.9.14). Thứ tự: `nang_cap_csdl.sh` (khối `sua_mo_ta_vat_pham.sql`, `sua_ten_qa_toan_game.sql`) -> jar máy chủ -> client.

1. Thẻ vật phẩm dạng thuốc (rà soát vật phẩm, main 5302cebb): máy chủ gửi **bảng mô tả** gói `-104` kiểu 9
   (`com.htbb.pokemon.MoTaVatPham`, `real.MoTaVatPhamGame`, nhiều gói <= 64 KB) cho client `BAN >= 914`: mỗi id thuốc có
   loại thật ("Vật phẩm gọi thú cưỡi", "Nguyên liệu sự kiện"...), mô tả, dùng được từ túi hay không + lý do. Client
   (`game.pokemon.MoTaVatPham`, `DungDo`) dùng bảng này cho thẻ (`TheVatPham`): không còn "Bình thuốc / Hồi HP" cho Sáo gọi
   thú cưỡi, nút "Sử dụng" chỉ hiện khi dùng được, còn lại nút xám + lý do. Client cũ không nhận kiểu 9 (giữ như 0.9.13).
   Máy chủ chặn (không trừ) vật phẩm không dùng từ túi ở mọi bản client; Sáo gọi / trứng thú cưỡi dùng được từ túi
   (`real.ThuCuoiTuTui`); ô nhanh chỉ nhận bình HP / PP.
2. Lý do "không dùng từ túi" đưa **bước tiếp lên trước** ("Đổi ở NPC sự kiện khi sự kiện mở (không dùng trực tiếp từ
   túi).") và `TheVatPham` ngắt lý do / gợi ý tối đa **2 dòng** (chiều cao thẻ tính trước bằng lượt chạy khô `veNut`):
   phông J2ME từng cắt mất phần bước tiếp ở 7 cỡ (`ChupTrongGame -Dchup.j2me` 7 lỗi -> 0).
3. Chữ client (rà soát hiển thị, main 9ddb1e5f + QA toàn game): chợ "Bid / Giá bid / Đặt bid" -> "Đấu giá / Giá đặt / Đặt
   giá", "Level: / Theo level" -> "Cấp: / Theo cấp", "áo" -> "Áo"; "Không thể cưỡi." -> nói phải xuống thú đang cưỡi; Dây
   thoát hiểm trong làng nói bước tiếp; bỏ tên chiêu cũ "Bua 8x". Kiểm: `python3 -I client_jar/tools/kiem_chu_hien_thi.py`
   (0 lỗi, `CHO_SUA` rỗng).
4. Máy chủ (không cần chặn theo `BAN`): giftcode / thưởng nhiệm vụ kiểm đủ ô trống trước khi trao (không mất quà), đồ thú
   cưỡi "… Level" -> "… Level Ball" (SQL), chữ quà giftcode cũ. Kiểm: `server/test/KiemQaToanGame`, `KiemMoTaVatPham`.

### Bản 0.9.13 (`BAN=913`: Trang bị › Thú cưỡi hiện thú đang cưỡi + pet đang đi theo)

Client + máy chủ (SQL KHÔNG đổi): `PHIEN_BAN=0.9.13-opentest` (3 phần số khớp `BAN = 0*10000 + 9*100 + 13`;
`MIDlet-Version` 0.9.13). Thứ tự: jar trước, client sau.

1. Lỗi (chủ dự án, PC 0.9.12): đang cưỡi thú mà ô Thú cưỡi trống, sân khấu "Chưa cưỡi thú", danh sách chỉ có pet đi theo,
   "Ô thú: 5/20" nhưng 4 hàng. Gói 16 loại 2 KHÔNG gửi con đang cưỡi (`place == 1`) và pet đang đi theo (`petUsing`)
   trong khi "Ô thú" (`QuanLyThuGame.soThu`) đếm cả hai; client chỉ ĐOÁN "đang cưỡi" từ gói 15 / gói 3 / -93
   (`ManNhanVat.dangCuoi`, có từ 0.9.5) và chưa từng hiện pet đang đi theo ở Trang bị › Thú cưỡi. Không phải lỗi mã
   0.9.12 - 0.9.12 làm lộ ra vì thanh "Ô thú" đếm cả con bị giấu.
2. Thay `htbb-server.jar`, khởi động lại: client `BAN >= 913` nhận thêm KHỐI ĐANG DÙNG sau phần nối thêm gói 16 loại 2
   (`QuanLyThuGame.ghiDangDung`: thú đang cưỡi + con `place == 1`, pet đang đi theo, mỗi con kèm dòng khoá / xu / lý do;
   cờ thú cưỡi cũ bằng thuốc - không chiếm ô). Client 912 trở về trước nhận gói y như cũ.
3. Client: trạng thái cưỡi / đi theo theo máy chủ (không đoán); ô Thú cưỡi + sân khấu hiện thú đang cưỡi ("Đang cưỡi
   Rapidash", cấp, pet đi theo kèm); danh sách: con đang dùng đứng đầu, nhãn "Đang cưỡi" / "Đang đi theo" / "Thú cưỡi" /
   "Đi theo", số hàng = "Ô thú"; chọn con đang cưỡi: Xuống thú / Khoá (không nút Thả), pet đang đi theo: Khoá; Thả trùng chỉ
   xét con trong túi. Trang bị › Thú cưỡi KHÔNG vẽ lưới đồ nhân vật / Tiềm năng / Danh hiệu nữa mà sân khấu + hai ô
   "Thú cưỡi" / "Đi theo" (Trang bị thường giữ nguyên). Hàng thú: tên ưu tiên (chật thì bỏ "#id", cấp xuống dòng phụ,
   dòng phụ bỏ đoạn cuối) - không còn "Rapida..", "Hoo..". Kiểm: `server/test/KiemQuanLyThu` (khối đang dùng 912 / 913),
   `ChupTrongGame` màn `nv_thu_dang_dung`, `nv_thu_dang_dung_chon`, `nv_thu_di_theo_dang_dung`, `nv_thu_dang_dung_tab`
   (không ô đồ nhân vật ở chế độ Thú cưỡi, tên thú không bị cắt, cả `-Dchup.j2me`).
4. Tải gói 913 lên `/admin/tai-game` (Số bản = 913) cho từng nền tảng sau khi máy chủ chạy jar mới.

### Sửa nhanh 0.9.12: hạn mức đăng ký theo mạng (không đổi client)

Người chơi mạng di động (CGNAT, chung IP) bị báo "đã tạo quá nhiều tài khoản". Thứ tự: SQL → jar → web.

1. `sudo bash /opt/htbb/vps/nang_cap_csdl.sh` — danh mục thêm `dang_ky.toi_da_gio` / `dang_ky.toi_da_ngay` (nhóm Bảo trì,
   chỉnh trên web; log máy chủ `danh muc cau hinh: 366 khoa`), khối `-- >>> mo_han_muc_dang_ky.sql` đặt giá trị open test
   6 / giờ, 30 / 24 giờ (INSERT IGNORE). Web + jar cũ bỏ qua hai khoá (vẫn 3 / 10).
2. Thay `htbb-server.jar`, khởi động lại: `DangKyTrongGame` đọc hai khoá (≤ 5 giây sau mỗi lần sửa).
3. Deploy web: `register.post.ts` đọc hai khoá; kiểm nhập liệu trước hạn mức và trả chỗ giữ trong bộ nhớ khi lượt đăng ký
   lỗi (trước đây bấm lại trong 5 giây sau một lần lỗi bị tính như đã tạo thêm một tài khoản); câu báo mới chỉ cách đổi
   Wi-Fi / liên hệ hỗ trợ. Xem lại hai khoá lúc khai trương chính thức ([cau-hinh.md](cau-hinh.md)).

### Bản 0.9.12 (`BAN=912`: quản lý thú - ô thú, thả về tự nhiên, khoá, Vé mở ô thú)

Client + máy chủ + SQL: `PHIEN_BAN=0.9.12-opentest` (3 phần số khớp `BAN = 0*10000 + 9*100 + 12`; `MIDlet-Version` 0.9.12).
Thứ tự: SQL trước, jar sau, client cuối.

1. `sudo bash /opt/htbb/vps/nang_cap_csdl.sh` — `sql/htbb.sql` phần B khối `-- >>> them_quan_ly_thu.sql`: cột
   `tob_char.o_thu_mo` + `tob_char.thu_khoa` (`ADD COLUMN IF NOT EXISTS`; máy chủ cũng tự thêm lúc khởi động), món cửa hàng
   `tob_char.thu_xu_ngay` + `tob_char.thu_ngay` (đếm xu thả trong ngày), món cửa hàng lượng `ve_mo_o_thu` (`INSERT IGNORE`,
   10 lượng, giới hạn 40) và 6 khoá cấu hình `thu.*` (nhóm Kinh tế, chỉnh trên web). Log máy chủ: `danh muc cau hinh: 364 khoa`.
2. Thay `htbb-server.jar` rồi khởi động lại: `real.QuanLyThuGame` + `com.htbb.pokemon.OThu`. Thú cưỡi + Pokémon đi theo
   dùng CHUNG một sức chứa "Ô thú" (`thu.o_co_ban` 20 + ô đã mở bằng vé, tối đa `thu.o_toi_da` 60). Đầy ô: mọi đường nhận
   thú (mua, nở trứng, sự kiện, giftcode, được tặng / giao dịch) từ chối kèm thông báo (đường trả tiền kiểm TRƯỚC khi trừ);
   nhân vật cũ đang vượt giữ nguyên, chỉ không nhận thêm. Thả về tự nhiên: `(20 + 2 x cấp) x bậc hiếm` xu, trần 300 / con,
   thú có hạn 0 xu (`thu.tha_xu_*`), trần mỗi người chơi mỗi ngày `thu.tha_xu_ngay` = 2.000 xu (đếm lưu CSDL, đăng nhập lại
   / khởi động lại không đặt lại; quá trần vẫn thả được nhưng 0 xu), log `THATHU`; không thả con đang cưỡi / đi theo / đã
   khoá / đang giao dịch / chờ tặng. Pokémon đi theo có tên trả phí (thẻ đặt tên) tự khoá (mở khoá tay được rồi thả).
   Đường cũ trả tiền (sự kiện, cửa hàng đặc biệt, NPC đổi, đốt đèn, hộp quà) kiểm ô TRƯỚC khi trừ tiền / vật phẩm (hộp đã
   trừ thì hoàn); quà miễn phí (giftcode, quà top, rơi boss) khi đầy ô: bỏ phần thú, báo rõ + log `THUDAY`.
   Vé mở ô thú = nguyên liệu 271 (+1 ô, đã tối đa thì không mất vé). Gói mới `-119` (C->S) + phần nối thêm gói 16 loại 2
   chỉ cho client `BAN >= 912` (client 911 trở về trước không đổi).
3. Client: tab Thú và Trang bị › Thú cưỡi có "Ô thú: n/max", nút Thả trùng (giữ con cấp cao nhất mỗi loài, hỏi kèm tổng
   xu) và Thêm ô (dùng vé / mua ở Cửa hàng lượng); chạm thú = chọn -> Cưỡi / Gọi, Khoá / Mở khoá, Thả (+xu). Sân khấu
   Trang bị › Thú cưỡi hiện thú đang chọn / đang cưỡi / gợi ý (trước trống). Kiểm: `server/test/KiemQuanLyThu`,
   `ChupTrongGame` màn `nv_ds_thu`, `nv_ds_thu_quan_ly`, `nv_thu_quan_ly`, `nv_cuoi_ds_thu_quan_ly`.
4. Tải gói 912 lên `/admin/tai-game` (Số bản = 912) cho từng nền tảng sau khi máy chủ chạy jar mới.

### Bản vá 0.9.11 (`BAN=911`: kéo núm thanh cuộn đúng chiều)

Chỉ client (`client_jar/src` + `client_jar/shared/mod`, vào cả J2ME / APK / PC); SQL và `htbb-server.jar` KHÔNG đổi.
`PHIEN_BAN=0.9.11-opentest` (3 phần số khớp `BAN = 0*10000 + 9*100 + 11`; `MIDlet-Version` 0.9.11).

1. Thanh cuộn danh sách (lỗi 0.9.10, ví dụ Nhân vật -> Ngoại hình -> Hào quang): kéo núm LÊN thì danh sách cuộn XUỐNG và
   ngược lại - `game.pokemon.Cuon` coi cú nhấn trên thanh như kéo nội dung (vuốt ngón). Nay nhấn lên thanh = nắm NÚM: núm
   theo con trỏ, nội dung theo tỉ lệ cùng chiều (`game.pokemon.ThanhCuon`), chạm rãnh = nhảy tới chỗ chạm rồi kéo tiếp;
   kéo trên nội dung giữ chiều theo ngón, lăn chuột không đổi. Áp cho mọi danh sách dùng `Cuon` (`Bang.danhSach`: hành
   trang, cửa hàng, nhiệm vụ, kho đồ, Pokédex, xếp hạng, danh hiệu, tủ đồ Ngoại hình, luyện / hợp / khảm, giao dịch,
   xem trang bị; hộp thông báo dài; chat) và các thanh cũ: menu chữ (`Menu`), menu tạm dừng (`PauseMenu`), bảng gán chiêu
   (`ChonChieuNhanh`), thẻ chi tiết vật phẩm (`HopChiTiet` trong `ShopNew` / `WindowInfoScr`), lưới cũ `WindowInfoScr`,
   màn ghi công (`GhiCongScr`).
2. Bản PC Windows: `HTBB.exe` báo "This application requires a Java Runtime Environment" khi thư mục game có dấu (cài
   riêng người dùng `C:\Users\Nguyễn ...`, zip giải nén ra Desktop) - Launch4j 3.50 đầu ANSI. Thay bằng trình mở tự viết
   `client_pc/khoi_chay/` (jvm.dll trong tiến trình, bảng mã ANSI UTF-8, tên ngắn 8.3); jar game (đã làm rối) nằm cạnh:
   `HTBB-PC.jar`. Bộ cài mặc định cài cho mọi người dùng (vẫn cho chọn riêng người dùng); lối tắt + mở lại sau cập nhật
   trong game trỏ `HTBB.exe`. Đóng gói cần thêm `gcc-mingw-w64-x86-64` + `binutils-mingw-w64-x86-64`.
3. Kiểm: `client_pc/test/KiemThanhCuon` (PC 1280x720, điện thoại 20:9, đường vẽ J2ME; chạm và chuột) - lệnh chạy như
   `KiemTruotDoiCo` (client_pc/README.md).
4. Tải gói 911 lên `/admin/tai-game` (Số bản = 911) cho từng nền tảng.

### Bản vá 0.9.10 (`BAN=910`: giao diện J2ME - `ChupTrongGame -Dchup.j2me` 35 lỗi -> 0)

Chỉ client (`client_jar/src`, vào cả J2ME / APK / PC); SQL và `htbb-server.jar` KHÔNG đổi. `PHIEN_BAN=0.9.10-opentest`
(3 phần số khớp `BAN = 0*10000 + 9*100 + 10`; `MIDlet-Version` 0.9.10).

1. Màn Nhân vật (tab Trang bị, kiểu gọn 2 cột): viên Danh hiệu giữa sân khấu cách vùng chạm ô trang bị >= `Bang.cach()`
   (trước hở 6 < 7 px với ô "Huy hiệu" ở J2ME 16:9); vùng chạm viên canh giữa viên.
2. Chọn chiêu nhanh: khe các nút chân ("Bỏ trống / Dời sang trái / Dời sang phải") = `Bang.cach()` ở mọi bản (trước J2ME
   cố định 4 px).
3. Menu ngữ cảnh (menu máy chủ / NPC): màn thấp (J2ME 19.5:9 / 20:9 / 21:9, PC 21:9) không đủ chỗ cho 6 nút mà không đè
   HUD (nút Menu, dải chat, nhãn nhặt bóng) -> ít nút mỗi trang hơn (+ "Thêm"), thẻ đặt chỗ không chồng HUD.
4. Hàng ví (dưới lưới hành trang): font ảnh J2ME rộng hơn chữ HD - "1,25 Tr" đè biểu tượng lượng ở 4:3. Ô theo bề ngang
   thật từng mục (khe chữ >= 4 px), biểu tượng 12 px khi chật, vẫn không vừa thì xuống dòng; vùng chạm mỗi mục >= cỡ chạm.
5. Tải gói 910 lên `/admin/tai-game` (Số bản = 910) cho từng nền tảng.

### Bản vá 0.9.9 (`BAN=909`: menu AUTO, trang chiêu tự động, lỗi J2ME đổi bản đồ, giftcode, nút Cài đặt HUD)

SQL không đổi (không chạy `nang_cap_csdl.sh`); `htbb-server.jar` ĐỔI (chỉ câu trả lời giftcode) - thay jar rồi khởi động lại.

1. Menu AUTO: viên "Tự động đánh" luôn có ở Menu (0.9.8 bỏ khi HUD màn rộng có bánh răng - PC / J2ME màn rộng mất lối
   vào ở Menu). Lối vào: nhấn giữ viên AUTO, bánh răng cạnh AUTO, Menu, phím #, menu chữ, APK nhấn giữ + bánh răng mới -
   tất cả trong `du_lieu/parity_097.txt` mục `[ma]` (KiemParity) + `client_pc/test/KiemChamHud` (mở thật bằng chạm, cả bản
   đồ khoá Tự động). Bảng có thêm "Trang chiêu dùng" (mặc định trang đang dùng; Tự chọn ở Thứ tự chiêu = chiêu đánh của
   trang đó).
2. Nút Cài đặt (bánh răng PNG) ở hàng nút phải trên của HUD (PC / J2ME màn rộng: `hud-cai-dat`; APK: Cài đặt + bánh
   răng Tự động ở hàng trên).
3. Giftcode: MỘT lối vào Menu -> Thư/Quà -> "Nhập giftcode" (+ NPC quà tặng); bỏ khỏi Cài đặt PC / Menu nhanh. Thành
   công = "Đổi giftcode thành công: +5 lượng khoá, ..." (thẻ phần thưởng); bấm lại < 2 giây được báo (trước im lặng).
   Tài khoản CHƯA kích hoạt nhận "Tính năng Giftcode cần tài khoản đã kích hoạt" khi `kich_hoat.chuc_nang` có `giftcode`
   (cấu hình web). Kiểm: `client_pc/test/KiemGiftcode`, bot `tools/pokemon/kiem_tai/KichBanGiftcode` (tob_giftcode thật).
4. Lỗi J2ME "nhip man: ArrayIndexOutOfBoundsException(17 >= 0)": đổi bản đồ do máy chủ (dịch chuyển / hồi sinh / về hồi
   phục) xoá danh sách cổng (`Tilemap.reset`) trong lúc `Tilemap.type` còn mã cổng map cũ -> `toOtherMapAt` kiểm cỡ, ô cổng
   ghi sau khi cổng vào danh sách (KiemPc `kiemCongDoiMap`).
5. Tải gói 909 lên `/admin/tai-game` (Số bản = 909) cho từng nền tảng sau khi máy chủ chạy jar mới.

### Danh sách kiểm triển khai 0.9.8 (`BAN=908`: học chiêu, bộ / ngọc / chế tạo / thu phục, phó bản + cúp, thời tiết, giao diện NRO, kênh chat, trao đổi + chợ Pokémon, ngắm tay PvP, Lễ kết đôi, thẻ đặt tên, đồ khoá, 1.025 loài, đồ họ mới + tủ đồ trang trí)

Như danh sách 0.9.7 bên dưới (cùng thứ tự: SQL trước, jar sau), khác:

1. `sudo bash /opt/htbb/vps/nang_cap_csdl.sh` — nạp `sql/htbb.sql` (từ bản gộp `mot-sql`: MỘT tệp thay
   `htbb_day_du.sql` + `nang_cap.sql`; phần A chỉ `CREATE TABLE IF NOT EXISTS`, dữ liệu đầy đủ chỉ chép vào CSDL mới — trên
   production phần A không đổi dòng nào, phần B = `nang_cap.sql` cũ). Cuối log có `== XONG` và `danh muc cau hinh: 358 khoa`.
   Phần B mục 11 tạo bảng / dữ liệu 0.9.8 (`sql/README.md`); chỉ thêm bảng / cột (`tob_pokedex.so_bat`, `bat_shiny`, `tob_char.o_mo_rong`), thêm dòng cửa hàng
   `nap_cua_hang` bằng `INSERT IGNORE` (vé mở rộng túi, thẻ đặt tên, 15 món tủ đồ), `data_item` 1400..2027 (đồ họ), không sửa
   dòng người chơi. Kiểm (22/10/2026): dump `main` 1e27bf2a + `nang_cap.sql` hai lần → không lỗi, bảng người chơi giữ số
   dòng + CHECKSUM, xuất lại = `htbb_day_du.sql`. Kiểm bản gộp (23/10/2026): dump cũ + người chơi / sửa admin giả +
   `htbb.sql` ba lần = luồng `nang_cap.sql` cũ, không lỗi, dòng người chơi / giá trị admin giữ nguyên; CSDL trống +
   `htbb.sql` = dump cũ (sql/README.md).
2. Log máy chủ sau khi lên: `[CAU HINH] danh muc tu CSDL htbb_db: 358 khoa, 21 nhom`, `[KHOA BAN DO] da nap 142 luat`,
   `[HOC CHIEU] learnset 175 dong`, `[BO NGOC] 33 dòng bộ ... 916 loài thu phục được; [DO MOI] 21 bộ, 628/628 món họ; [TRANG PHUC] 69 món trang trí`,
   `ICON DO MOI -> 697 anh`, `[KET DOI] da nap 15 nhom trung, 1025 loai`, `[NGAM TAY] 50 dong`, `PHONG RIENG -> pho ban 4381, san
   cup 4382`, `[DANH HIEU] 429 mau danh hieu`, khi quái hồi sinh lần đầu `[LOAI] nap 1025 loai (916 da mo)`; không có `THIEU DU LIEU` / `Exception`.
3. Nhân vật cũ: điểm tiềm năng → Điểm huấn luyện chuyển **trong game** lúc nạp nhân vật (một lần, dấu
   `tob_chuyen_doi_can_bang`). Gói mới -106 / -107 / -108 / -112 / -113 / -116 / -118 / -120 / -121 / -122 / -124 / -125 / -126 (và phần nối thêm sức chứa túi của -105) chỉ gửi cho client `BAN >= 908`
   (`loai.ban_client` = 908); đuôi 6 byte trang trí của -102 gửi mọi client (client cũ đọc trường cố định, bỏ qua đuôi); client
   906 / 907 vẫn chơi được (không có màn mới).
4. Đồ / nguyên liệu khoá bán NPC ở giá thấp (`chong_clone.ban_npc_khoa_phan_tram` = 25% = 1/4 giá NPC thường, trần 25%; tắt
   khẩn cấp: `chong_clone.ban_npc_khoa_phan_tram=0` trong `server.ini`), đồ khoá Huỷ được; nhật ký `KHOA_BAN_NPC` / `KHOA_HUY`.
   Danh hiệu: 9700…9745 (thành tựu), 9801…9817 (ngày cộng đồng), 9850…9855 (cúp tuần), 9860…9862 (phó bản).
5. Client: tải gói 908 lên `/admin/tai-game` (Số bản = 908) sau khi máy chủ đã chạy jar mới.
   Trong lần bảo trì này `systemctl restart htbb-login` (CheckLoginSocket.jar rò một luồng mỗi kết nối - bỏ các luồng đã rò;
   `vps/kiem-suc-khoe.sh` của main 2ed95e58 chỉ xem cổng LISTEN nên không rò thêm).
6. Bước địa điểm: Arcanine gác cổng ở làng dẫn tới Bảo tàng Pewter / Đường Victory / Núi Silver (khoá 41 / 42 cấp 80,
   118 cấp 69 + CA-11.3); mọi dòng `data_nhiem_vu_khu_vuc` bật. Khối `sua_tuyen_nhiem_vu.sql` / `them_huong_dan_tan_thu.sql`
   (production đã chạy) nằm trong `sql/htbb.sql` (phần B), chạy lại vô hại. Gói -123 (mảnh gói J2ME, main) giữ nguyên.

### Danh sách kiểm triển khai bản có khoá bản đồ (một tệp SQL, khoá bản đồ, vòng quay, thách đấu cược, 0.9.7)

Jar mới **dừng ngay** nếu CSDL chưa nâng cấp (`THIEU DU LIEU SQL QUAN TRONG`), và `vps/deploy-auto.sh` chỉ kéo code rồi
để lần bảo trì tự động kế tiếp nạp jar mới — nó **không** chạy SQL. Thứ tự:

1. **Trước khi gộp vào `main`** (hoặc ngay sau khi VPS kéo code, trước lần khởi động lại kế tiếp): trên VPS
   `sudo bash /opt/htbb/vps/nang_cap_csdl.sh`. Script sao lưu (hỏng là dừng), nạp `sql/nang_cap.sql` (nay `sql/htbb.sql`), kiểm bảng. Chạy lại
   được; jar đang chạy vẫn chạy được sau bước này. Kiểm cuối log script có `== XONG` và `danh muc cau hinh: 270 khoa`.
2. Gộp / đẩy `main` (jar đã build trong commit). Muốn lên ngay: chạy workflow với **deploy_now** hoặc "Bảo trì" trên web
   admin; không thì jar mới lên ở lần bảo trì hằng ngày.
3. Sau khi lên: log máy chủ có `[DU LIEU SQL] da nap 6 bang`, `[CAU HINH] danh muc tu CSDL htbb_db: 270 khoa`,
   `[KHOA BAN DO] da nap 140 luat ban do`, `[LAU DAI] engine nhiem vu: ... mau`; không có `THIEU DU LIEU`.
4. Một lần, tự động, không cần làm tay:
   - mốc miễn trừ khoá bản đồ (`tob_ban_do_mo`): phần B của `sql/htbb.sql` điền theo cấp / bản đồ đang lưu, server điền nốt khi
     nhân vật đăng nhập đầu tiên — không ai bị dịch chuyển hay mất quyền vào bản đồ đang đứng;
   - cược đang chờ của xổ số cũ (`nin_xoso*`) được hoàn qua `tob_vong_quay_cho_nhan` lúc khởi động; ký quỹ thách đấu còn
     treo được hoàn lúc khởi động.
5. Client: 0.9.7 (`BAN=907`) đã phát hành **không** có bộ đọc gói -108; ổ khoá / nhãn AUTO hiện từ client 0.9.8
   (`BAN=908`). Client 906 / 907 vẫn chơi được: bị chặn bản đồ thì nhận thông báo nhanh -104 (cũ hơn 902: chữ -26).
6. Tắt khẩn cấp khoá bản đồ: thêm `ban_do.khoa.bat=0` vào `server.ini` rồi khởi động lại (khoá lớp *luôn bật*, ẩn trên
   web như `gym.bat`). Sửa luật một bản đồ: bảng
   `data_ban_do_mo_khoa` (server nạp lại ≤ 30 giây).
7. Quay lại bản trước: như [trien-khai.md mục 4.1](trien-khai.md) — CSDL đã nâng cấp để nguyên, jar cũ vẫn chạy.

### Tài nguyên giao diện sinh tự động (0.9.5)

| Lệnh | Ra | Ghi chú |
|---|---|---|
| `python3 tools/pokemon/sinh_icon_chieu.py` (`--kiem` chỉ kiểm) | `client_jar/res/ui/chieu{,_tron}_{32,64,96}.png`, `web/public/chieu/*.png` | icon chiêu CHỈ cho giao diện (hiệu ứng trận không đổi); mỗi chiêu gán họ hình + hệ, thiếu là lỗi |
| `python3 tools/pokemon/sinh_icon_ui.py` | `client_jar/res/ui/ui_96.png`, `nut_96.png` | icon / da nút giao diện |
| `python3 tools/giao_dien_nro/sinh_khung_nro.py` (`--kiem` chỉ kiểm) | `client_jar/res/nro/{khung,icon}.png`, `client_jar/src/game/pokemon/KhungNroBang.java`, `docs/anh/giao_dien_nro.png` | 0.9.7 giao diện kiểu NRO: mảnh 9-slice + icon pixel (vẽ bằng `game.pokemon.KhungNro`, vào cả jar J2ME) |
| `python3 tools/giao_dien_nro/kiem_kieu_cu.py <thư mục ảnh ChupTrongGame> [<ChupTruocGame>]` | — | gắn cờ màn còn kiểu GO 0.9.6 (mảng trắng / teal / xám xanh) + mã vẽ kiểu cũ; thoát 1 = còn sót |
| `python3 tools/pokemon/sinh_icon_client.py` | `client_jar/res/pk_icon.png`, `IconBang.java` | icon vật phẩm |
| `python3 -I tools/pokemon/sinh_ten_ban_do_client.py` (`--kiem`) | `client_jar/src/game/pokemon/TenBanDoBang.java` | tên bản đồ client lấy từ SQL `data_map_ten` (sửa tên trên web rồi chạy lại trước khi build client) |
| `python3 -I tools/pokemon/kiem_chuoi_ui.py [chuoi_da_ve.txt]` | — | soát chữ giao diện còn sót (tĩnh + lúc chạy) |

`client_jar/res/ui/` chỉ đóng vào PC / APK; jar J2ME bỏ thư mục này (giữ jar thường <= 5,5 MB).
