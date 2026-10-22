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
| Dữ liệu / SQL | MariaDB | `vps/xuat_csdl_day_du.sh` (xuất từ CSDL) | `sql/htbb_day_du.sql` |

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
HTBB_DIR=.. npm run sinh-anim-khoi-dau   # ảnh động màn khởi đầu từ sprite client
```

`web/shared/data/*.json` và `web/shared/utils/*.gen.ts` là bản dự phòng tĩnh (web đọc CSDL trước; test đối chiếu với
`sql/htbb_day_du.sql`). Trên CI, `.github/workflows/web-deploy.yml`
build rồi đẩy `.output` lên VPS. Chi tiết: [web/README.md](../web/README.md).

## 7. Dữ liệu, SQL và tệp sinh

- **Dữ liệu game và cấu hình trung tâm** nằm trong CSDL (sửa trên trang quản trị). Không có bước sinh: repo giữ bản chụp
  `sql/htbb_day_du.sql` (xuất bằng `vps/xuat_csdl_day_du.sh`) và `sql/nang_cap.sql` cho CSDL đang chạy — xem
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

### Danh sách kiểm triển khai 0.9.8 (`BAN=908`: học chiêu, bộ / ngọc / chế tạo / thu phục, phó bản + cúp, thời tiết, giao diện NRO, kênh chat, trao đổi + chợ Pokémon, ngắm tay PvP, Lễ kết đôi, thẻ đặt tên, đồ khoá, 1.025 loài, đồ họ mới + tủ đồ trang trí)

Như danh sách 0.9.7 bên dưới (cùng thứ tự: SQL trước, jar sau), khác:

1. `sudo bash /opt/htbb/vps/nang_cap_csdl.sh` — cuối log có `== XONG` và `danh muc cau hinh: 358 khoa`. `nang_cap.sql`
   phần 11 tạo bảng / dữ liệu 0.9.8 (`sql/README.md`); chỉ thêm bảng / cột (`tob_pokedex.so_bat`, `bat_shiny`, `tob_char.o_mo_rong`), thêm dòng cửa hàng
   `nap_cua_hang` bằng `INSERT IGNORE` (vé mở rộng túi, thẻ đặt tên, 15 món tủ đồ), `data_item` 1400..2027 (đồ họ), không sửa
   dòng người chơi. Kiểm (22/10/2026): dump `main` 1e27bf2a + `nang_cap.sql` hai lần → không lỗi, bảng người chơi giữ số
   dòng + CHECKSUM, xuất lại = `htbb_day_du.sql`.
2. Log máy chủ sau khi lên: `[CAU HINH] danh muc tu CSDL htbb_db: 358 khoa, 21 nhom`, `[KHOA BAN DO] da nap 142 luat`,
   `[HOC CHIEU] learnset 175 dong`, `[BO NGOC] 33 dòng bộ ... 330 loài thu phục được; [DO MOI] 21 bộ, 628/628 món họ; [TRANG PHUC] 69 món trang trí`,
   `ICON DO MOI -> 697 anh`, `[KET DOI] da nap 15 nhom trung, 1025 loai`, `[NGAM TAY] 50 dong`, `PHONG RIENG -> pho ban 4381, san
   cup 4382`, `[DANH HIEU] 420 mau danh hieu`, khi quái hồi sinh lần đầu `[LOAI] nap 1025 loai`; không có `THIEU DU LIEU` / `Exception`.
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
   (production đã chạy) nằm trong `nang_cap.sql`, chạy lại vô hại. Gói -123 (mảnh gói J2ME, main) giữ nguyên.

### Danh sách kiểm triển khai bản có khoá bản đồ (một tệp SQL, khoá bản đồ, vòng quay, thách đấu cược, 0.9.7)

Jar mới **dừng ngay** nếu CSDL chưa nâng cấp (`THIEU DU LIEU SQL QUAN TRONG`), và `vps/deploy-auto.sh` chỉ kéo code rồi
để lần bảo trì tự động kế tiếp nạp jar mới — nó **không** chạy SQL. Thứ tự:

1. **Trước khi gộp vào `main`** (hoặc ngay sau khi VPS kéo code, trước lần khởi động lại kế tiếp): trên VPS
   `sudo bash /opt/htbb/vps/nang_cap_csdl.sh`. Script sao lưu (hỏng là dừng), nạp `sql/nang_cap.sql`, kiểm bảng. Chạy lại
   được; jar đang chạy vẫn chạy được sau bước này. Kiểm cuối log script có `== XONG` và `danh muc cau hinh: 270 khoa`.
2. Gộp / đẩy `main` (jar đã build trong commit). Muốn lên ngay: chạy workflow với **deploy_now** hoặc "Bảo trì" trên web
   admin; không thì jar mới lên ở lần bảo trì hằng ngày.
3. Sau khi lên: log máy chủ có `[DU LIEU SQL] da nap 6 bang`, `[CAU HINH] danh muc tu CSDL htbb_db: 270 khoa`,
   `[KHOA BAN DO] da nap 140 luat ban do`, `[LAU DAI] engine nhiem vu: ... mau`; không có `THIEU DU LIEU`.
4. Một lần, tự động, không cần làm tay:
   - mốc miễn trừ khoá bản đồ (`tob_ban_do_mo`): `nang_cap.sql` điền theo cấp / bản đồ đang lưu, server điền nốt khi
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
