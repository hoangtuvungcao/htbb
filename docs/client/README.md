# htbb-client — client Huyền Thoại Bảo Bối cho PC, Android, J2ME

Ba bản client của game Huyền Thoại Bảo Bối (HTBB) chạy server **NQSH-H2**.
Mod NQSH cũ (Auto up, PK, Giao hàng / Vận may / Lên thành tự động, chế đồ tự động, Xa phu từ xa...) đã BỎ; thay bằng
**Tự động đánh chính thức do máy chủ điều khiển** (gói -100, `client_jar/shared/mod/.../TuDong.java`, bảng `BangTuDong`; PC
`Mod/TuDong.cs`; thiết kế + hook: `HTBB/docs/pokemon/TU_DONG_DANH.md`) và Tiện ích (cửa hàng, đăng xuất, tắt popup). Cả ba bản đều là
**source đầy đủ** — không còn file game nhị phân nào (jar/APK/dex/DLL gốc) trong repo.

```
pc/          Unity 6000.5.9f1 — bản PC (Windows; build được cả iOS). C#, giải nén từ KPAH_276.exe
client_apk/     Gradle (AGP 9) — app/: bản Android NQSH (code 2.7.7 TeaMobi); pokemon/: APK HTBB = code bản jar
client_jar/        javac + ProGuard — bản jar J2ME (MIDP 2.0 / CLDC 1.0), code game 2.7.6 decompile ra Java
client_jar/shared/mod/  code dùng chung cho j2me + android (viết theo API J2ME): Tự động đánh chính thức, menu, Tiện ích, hiệu ứng
tools/phat-hanh/   phát hành + tự cập nhật (HTBB: repo phát hành cấu hình qua REPO; NQSH-H2 cũ: kpah-server/nqsh-h2-tai, NQSH=1)
.github/     workflow "Phát hành" (build cả 3 bản trên GitHub Actions) và "Build iOS"
```

## Build

| Bản | Lệnh | Ra |
|---|---|---|
| PC | Unity `-executeMethod BuildCli.Win64 -htbbBan N` trên `client_pc_ios/` (xem `pc/README.md`) | thư mục game Windows |
| Android | `BAN=1423 sh client_apk/build.sh` (hoặc `cd android && ./gradlew :app:assembleRelease -PhtbbBan=1423`) | `client_apk/out/NQSH-H2-1423.apk` |
| J2ME | `sh client_jar/build.sh 55` | `client_jar/out/HTBB-v55-release.jar` |

- Android ký bằng khóa trong thư mục `KEY_DIR` (`key.properties`: `keystore=`, `alias=`, `password=` + file keystore).
  Khóa **không bao giờ** đưa vào git; trên GitHub Actions lấy từ secrets.
- Máy Windows: build.sh tự dùng JDK 17 + Android SDK đi kèm Unity; J2ME cần JDK mới (javac) + JRE 8 (`rt.jar` cho ProGuard).
- `mapping-vNN.txt` của ProGuard (bản jar) giữ riêng, không đưa lên git.
- Phát hành: `sh tools/phat-hanh/phat-hanh.sh pc|android|jar|tat-ca` hoặc workflow "Phát hành" (xem `tools/phat-hanh/README.md`).

## Nhánh `pokemon/chuyen-doi`: bản jar Huyền Thoại Bảo Bối (Pokémon)

- **Client PC chính thức là `client_pc_ios/`** (Unity): đã gộp toàn bộ phần Pokémon từ `HTBB/ClientUnity` (ChuPokemon,
  PokeBody, PokeHieuUng, VatThePokemon, PokeTile, Tilemap/Res, `Resources/poke`, `poke_tile`, `poke_map`...) mà vẫn giữ
  mod + BuildCli. `HTBB/ClientUnity` là bản cũ, sẽ xoá (xem `pc/README.md`).
- Code: `client_jar/src/game/pokemon/` (bản J2ME của các lớp Pokémon trong `client_pc_ios/Assets/Scripts/Assembly-CSharp/`),
  hook có chú thích `// pokemon:` trong Char, Monster (+ boss), NPC, Npc_Server, AnimalMove, PokemonHuyenThoai,
  MessageHandler (-90..-94), Font (từ điển `ChuPokemon`), TField, Res (tile), Create/CharSelectScr, màn tiêu đề.
- Tài nguyên `client_jar/res/pk` (sprite, chân dung, vật thể, từ điển) + `client_jar/res/pt` (tile PMD) sinh bằng
  `HTBB_ASSETS=pc sh client_jar/build.sh N` (= `../pc/Assets`; bản cũ `<HTBB>/ClientUnity/Assets` vẫn dùng được;
  tool `client_jar/tools/DongGoiPokeJ2me.java`, bảng loài bản nhẹ đọc từ `HTBB/docs/pokemon` hoặc biến `HTBB_DOCS`).
- Hiệu ứng chiêu (K3): `PokeHieuUng` (hook `Char.setCurrentSkillByID`, buff trong `GameScr`), dữ liệu
  `res/pk/hu.bin` + `hu_*.png` đóng gói từ `Resources/poke/hu_*.bytes`.
- Bản thử nối một server: `MAY_CHU=127.0.0.1:19129 sh client_jar/build.sh N` -> `...-vN-thu.jar`
  (MANIFEST `HTBB-May-Chu` + `HTBB-Ban-Thu`; không tải danh sách server).
- Server chính cho MỌI bản: `MAY_CHU_CHINH=host:cong sh client_jar/build.sh N` — **bắt buộc**: không đặt thì đọc
  `MAY_CHU_CHINH=` trong `tools/phat-hanh/may_chu.conf` (gitignore; mẫu `may_chu.conf.example`, đọc chung cho jar/APK/PC),
  thiếu cả hai là build DỪNG (trước đây rơi về danh sách server `nqsh-h2.top`). Thử trên máy:
  `MAY_CHU_CHINH=127.0.0.1:19129`. `NQSH=1` = kiểu cũ tải danh sách server (chỉ khi cố ý).
- Bản DEV: chỉ đặt `MAY_CHU` (không `MAY_CHU_CHINH`, không `may_chu.conf`), ví dụ
  `MAY_CHU=127.0.0.1:19129 GOI=nhe sh client_jar/build.sh N`, vẫn build đủ thường/thử/phát hành/nhẹ nhưng MỌI jar vào
  server `MAY_CHU` và mang `HTBB-Ban-Thu: 1` - chỉ để thử, KHÔNG phát hành.
- Phiên bản: `tools/phat-hanh/phien_ban.conf` (`PHIEN_BAN=0.9.0-opentest`, `BAN=900`) cho cả 3 client; `sh client_jar/build.sh`
  không số = `BAN`. MANIFEST: `MIDlet-Version: 0.9.0`, `HTBB-Phien-Ban`, `HTBB-Ban`. Nhật ký: `CHANGELOG.md`.
- Ghi công (PMDCollab CC BY-NC 4.0, bắt buộc): `client_jar/res/GHI_CONG.txt` (vào jar + APK; PC:
  `client_pc_ios/Assets/Resources/ghicong/GHI_CONG.txt` + chép cạnh .exe), sinh bằng `python3 -I client_jar/tools/sinh_ghi_cong.py
  ../HTBB/ClientUnity/Assets/Resources/poke client_jar/res/GHI_CONG.txt client_pc_ios/Assets/Resources/ghicong/GHI_CONG.txt`.
  Trong game: Menu màn đăng nhập -> "Ghi công" (jar/APK, `GhiCongScr`), nút "Ghi công" dưới khung đăng nhập (PC).
- Xác thực -88 (`KiemServer`) mặc định TẮT trên nhánh này (server Pokémon không có lệnh -88);
  `XAC_THUC=1` bật lại cho server NQSH cũ.
- Bản nhẹ cho máy thật: `GOI=nhe sh client_jar/build.sh N` -> `...-vN-nhe.jar` (ProGuard + `client_jar/res_nhe/pk`:
  ít loài, ít khung, chân dung 24px; loài thiếu vẽ bằng bóng đen `bong`). Xem đầu `DongGoiPokeJ2me.java`.
- Kiểm thử thuần Java: `client_jar/tools/KiemPokeDuLieu.java` (xem chú thích đầu tệp).
- Phản hồi chiến đấu: `HieuUngDanh` (hạt theo hệ, đạn quái theo hệ, chớp trắng + lắc, rung màn, vùng cảnh báo boss,
  gục mờ dần, lên cấp/tiến hoá, biểu tượng trạng thái), `SoBay` (số bay có viền, màu theo chí mạng/khắc hệ),
  `HePoke` (hệ theo dex + bảng khắc hệ khớp server), gói -95..-99 `PokeMangChienDau` (THIET_KE_LAU_DAI 4.6).
- Âm thanh: `game.model.Sound` (+ `SoundMm` MMAPI, an toàn khi máy không có) và `game.pokemon.AmThanh`; tệp
  `client_jar/res/am` (bản nhẹ `res_nhe/am`, chỉ tiếng hiệu ứng) sinh bằng `python3 -P client_jar/tools/lam_am_thanh.py`;
  bật/tắt nhạc, hiệu ứng, âm lượng trong Menu nhanh. Ghi công: `res/LICENSE-audio.txt`, HTBB `docs/pokemon/GHI_CONG_AM_THANH.md`.

## Nhánh `pokemon/chuyen-doi`: bản Android Huyền Thoại Bảo Bối (`client_apk/pokemon`)

- APK = **đúng code bản jar** (`client_jar/src` + `client_jar/shared/mod`) biên dịch cho Android trên lớp tương thích MIDP
  (`client_apk/pokemon/src/main/java/javax/microedition/**`: Graphics/Image trên `android.graphics`, Canvas/Display,
  RecordStore = tệp, Connector = Socket/HttpURLConnection, Player = MediaPlayer, TextBox = hộp nhập Android).
  Một nguồn duy nhất: Gradle chạy `client_apk/port-j2me.py` chép code jar vào `build/generated` mỗi lần build, chỉ vá
  `getResourceAsStream` -> assets và ô nhập TField -> bàn phím hệ thống. Tài nguyên = `client_jar/res` (bản đầy đủ).
  Module cũ `client_apk/app` (TeaMobi 2.7.7, NQSH) giữ nguyên.
- Màn hình: chỉ ngang (sensorLandscape); độ phân giải logic = màn thật chia hệ số nguyên (cạnh ngắn >= 320 px,
  vd 2400x1080 -> 800x360 x3, 1920x1080 -> 640x360 x3, 2048x1536 -> 512x384 x4), phóng láng giềng gần nhất.
  Nút Back = phím mềm phải (-7), Menu = phím mềm trái (-6).
- Build: `sh client_apk/build-pokemon.sh` -> `client_apk/out/HTBB-android-thu.apk` (debug, server thử `10.0.2.2:19129`
  = 127.0.0.1 của máy dev nhìn từ máy ảo Android). `MAY_CHU=host:cong` đổi server; `KIEU=release [BAN=N]
  [KEY_DIR=...]` bản phát hành (BẮT BUỘC có máy chủ: `MAY_CHU` hoặc `tools/phat-hanh/may_chu.conf`, thiếu là dừng);
  `XAC_THUC=1` bật lại -88.
- Giao diện HD (chỉ APK, bản jar không đổi): `client_apk/pokemon/src/main/java/com/htbb/pokemon/hd/` + cầu nối
  `game/render/screen/HdCau.java`; móc vào code jar bằng vá MỀM `VA_HD` trong `client_apk/port-j2me.py` (mẫu lệch chỉ
  cảnh báo). Game vẽ vào RenderNode theo toạ độ logic rồi phát lại ở độ phân giải thật (chữ vector Be Vietnam Pro
  SIL OFL - `assets/fonts`, hình khử răng cưa, pixel art phóng nguyên lần); `HdVe` thay khung `GiaoDien`/`KieuO`,
  `HdChu` thay font ảnh, `Hud` = HUD cảm ứng đa chạm (cần điều khiển nổi, cụm chiêu, thuốc nhanh, thẻ trạng thái,
  bản đồ nhỏ, chat, chỉnh nút + âm lượng lưu theo máy), `HdNut` = lệnh phím mềm thành nút theo ngữ cảnh,
  `HdPhong` = cửa sổ nhỏ của jar phóng thành thẻ lớn, `HdZoom` = phóng thế giới, `HdHieuUng` = hiệu ứng HD,
  `AmThanhHd` = âm thanh OGG 44,1 kHz (`assets/am_hd`, sinh bằng `client_apk/pokemon/tools/lam_am_thanh_hd.py`).
  Kiểm chồng lấn: `adb shell setprop debug.htbb.kiem 1` rồi `python3 -I client_apk/pokemon/tools/kiem_chong_lan.py`.
- Icon: `client_apk/pokemon/tools/tao_icon.py` (Poké Ball vẽ bằng code + chân dung Pikachu PMDCollab CC BY-NC 4.0,
  ghi công trong `client_jar/res/GHI_CONG.txt` = assets `GHI_CONG.txt`, màn "Ghi công" ở Menu màn đăng nhập).

## Code mod nằm ở đâu

- **Hook vào game** viết thẳng trong source game, có chú thích `// mod:`:
  - J2ME: `client_jar/src/...` (GameService.login, MessageHandler.onMessage, GameScr.onMenuOption/showWindow/onActorMove/
    onAdd2Party/onMap, ScreenDapDo nút "Đập", ServerListScr, Canvas.paint/startOKDlg, GameScr.update).
  - Android: cùng các chỗ đó trong `client_apk/app/src/main/java/...` + tab "Auto" của `MenuWindows`,
    `GCanvas.paint/startOKDlg`, `GameMidlet.platformRequest`, `MessageRecieve0` (link server).
  - PC: các file C# trong `client_pc_ios/Assets/Scripts/Assembly-CSharp/` (thư mục `Mod/` + hook trong GameScr, Canvas, MenuWindows...).
- **Tính năng mod dùng chung** (`client_jar/shared/mod/game/render/screen/`): `AutoGuard` (cổng hook), `PauseMenu` (menu mod +
  việc chế đồ), `AutoUp`, `AutoGiaoHang`, `AutoVanMay`, `TienIch`, `LenGym`. Viết theo API J2ME; bản Android
  dùng lại qua `client_apk/port-android.py` (đổi `Canvas`→`GCanvas`, `Graphics`, `GameMidlet`...) lúc build.
  Chỗ hai nền tảng khác nhau thật đi qua lớp `Nen` (`client_jar/src/.../Nen.java` và `client_apk/.../Nen.java`).
- **Riêng từng bản**: J2ME `KiemServer`, `TaiDanhSach`; Android `NqshMod` (server, xác thực -88, tab Auto),
  `NqshCapNhat` (tự cập nhật APK); PC `Mod/*.cs` (bản C# của client_jar/shared/mod), `CapNhat.cs`, `KiemServer.cs`.
  Sửa tính năng ở `client_jar/shared/mod` thì sửa tương ứng ở `pc/.../Mod/` (C#).

## Nguồn gốc source game

- `client_pc_ios/`: `KPAH_276.exe` (Unity 5.1) giải nén bằng AssetRipper/ILSpy.
- `client_jar/src`: jar gốc 2.7.6 decompile bằng jadx (class nâng lên v49 để nhận lớp lồng), các method jadx dựng sai
  được viết lại theo bytecode; đã so bytecode chuẩn hoá từng method với bản gốc.
- `client_apk/app/src/main/java`: APK TeaMobi 2.7.7 decompile bằng jadx; method jadx bỏ trống/dựng sai viết lại theo
  smali gốc; đã so smali build lại với APK gốc từng method. Thư viện Google (Billing, Play Services, Firebase)
  game không dùng nên đã bỏ; chỉ còn `androidx.core` (FileProvider cho tự cập nhật).
- `client_jar/api/`: khai báo API SMS (WMA) chỉ để biên dịch; `client_jar/tools/`: công cụ build (asm, ProGuard, midp).
