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
| Dữ liệu / SQL | python3, Java | `tools/pokemon/*`, `tools/cauhinh/sinh_cau_hinh.py` | `sql/*.sql`, tệp sinh |

## 1. Game server (`server/`)

Mã Java trong `server/src`, thư viện trong `server/lib` (`htbb-core.jar` là thư viện đóng của game gốc, không có mã
nguồn), bản đồ `server/cMap`, tài nguyên `server/map`. Biên dịch ở **class version 52** (`--release 8`) để jar
chạy được trên mọi JVM ≥ 8; VPS chạy OpenJDK 17 (cờ `-Xlog:gc` của `htbb-game.service` cần Java ≥ 9).

Linux / macOS (chạy ở `server/`):

```sh
cd server
LIBCP="lib/jxl-2.6.jar:lib/mysql-connector-java-5.1.49.jar:lib/htbb-core.jar"
OUT=/tmp/htbb-classes; rm -rf "$OUT"; mkdir -p "$OUT"
find src -name '*.java' > /tmp/htbb-src.txt
javac --release 8 -encoding UTF-8 -nowarn -Xlint:none -cp "$LIBCP" -sourcepath src -d "$OUT" @/tmp/htbb-src.txt
printf 'Main-Class: server.TeamServer\nClass-Path: lib/jxl-2.6.jar lib/mysql-connector-java-5.1.49.jar lib/htbb-core.jar\n' > /tmp/htbb-mf.txt
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
LIBCP="lib/jxl-2.6.jar:lib/mysql-connector-java-5.1.49.jar:lib/htbb-core.jar"
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
dừng: Launch4j 3.50, Temurin 21 JDK Windows + Linux để lấy jmods, appimagetool 1.9.1 + type2-runtime 20251108, Inno
Setup 6.7.3 qua wine) rồi:

- làm rối mã game bằng ProGuard (`client_pc/release-pc.pro`, bản đồ tên `out/goi/mapping-pc.txt` giữ riêng);
- dựng Java TỐI GIẢN bằng `jlink` (module theo `jdeps`: java.base, java.desktop, jdk.management + jdk.crypto.ec,
  jdk.localedata en/vi; `--strip-debug --no-header-files --no-man-pages --compress=zip-9`), ~50-60 MB thay JRE đầy đủ;
- Windows: `HTBB.exe` (Launch4j, jar game nhúng trong exe) + `runtime\` -> `HTBB-PC-Setup-<ver>.exe` (+ zip cùng bố cục);
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
HTBB_DIR=.. npm run dong-bo-du-lieu   # sinh lại shared/data/*.json + public/pokemon từ du_lieu/ và mã server
```

Không sửa tay `web/shared/data/*.json` và `web/shared/utils/*.gen.ts`. Trên CI, `.github/workflows/web-deploy.yml`
build rồi đẩy `.output` lên VPS. Chi tiết: [web/README.md](../web/README.md).

## 7. Dữ liệu, SQL và tệp sinh

- **Cấu hình trung tâm:** sửa `tools/cauhinh/danh_muc.json` → `python3 tools/cauhinh/sinh_cau_hinh.py` (sinh Java,
  SQL, TS và bảng trong [cau-hinh.md](cau-hinh.md); `--kiem` để kiểm lệch).
- **Dữ liệu game:** bảng nguồn `du_lieu/*.csv` → bộ sinh trong `tools/pokemon/` → `sql/pokemon_*.sql`;
  `sh tools/pokemon/xuat_sql_day_du.sh` gộp mọi thứ thành `sql/htbb_day_du.sql`. Xem
  [du_lieu/README.md](../du_lieu/README.md), [tools/README.md](../tools/README.md), [sql/README.md](../sql/README.md).
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

`day-len.sh` đẩy bản build lên GitHub Releases của repo phát hành và ghi `phienban.json`. Trước mỗi đợt: tăng `BAN`
+ `PHIEN_BAN`; muốn ép cập nhật thì đặt `sv.banToiThieu=<BAN>` ở server (jar/APK cũ hơn bị từ chối kèm link tải).
Biến và secret của workflow: [tools/phat-hanh/README.md](../tools/phat-hanh/README.md).

### Tài nguyên giao diện sinh tự động (0.9.5)

| Lệnh | Ra | Ghi chú |
|---|---|---|
| `python3 tools/pokemon/sinh_icon_chieu.py` (`--kiem` chỉ kiểm) | `client_jar/res/ui/chieu{,_tron}_{32,64,96}.png`, `web/public/chieu/*.png` | icon chiêu CHỈ cho giao diện (hiệu ứng trận không đổi); mỗi chiêu gán họ hình + hệ, thiếu là lỗi |
| `python3 tools/pokemon/sinh_icon_ui.py` | `client_jar/res/ui/ui_96.png`, `nut_96.png` | icon / da nút giao diện |
| `python3 tools/pokemon/sinh_icon_client.py` | `client_jar/res/pk_icon.png`, `IconBang.java` | icon vật phẩm |
| `python3 -I tools/pokemon/sinh_ten_ban_do_client.py` (`--kiem`) | `client_jar/src/game/pokemon/TenBanDoBang.java` | tên bản đồ client lấy từ SQL `data_map_ten` (sửa tên trên web rồi chạy lại trước khi build client) |
| `python3 -I tools/pokemon/kiem_chuoi_ui.py [chuoi_da_ve.txt]` | — | soát chữ giao diện còn sót (tĩnh + lúc chạy) |

`client_jar/res/ui/` chỉ đóng vào PC / APK; jar J2ME bỏ thư mục này (giữ jar thường <= 5,5 MB).
