# kpah-mod-pb — client NQSH-H2 (KPAH) cho PC, Android, J2ME

Ba bản client của game KPAH (Khí Phách Anh Hùng, TeaMobi) chạy server **NQSH-H2**, kèm các tính năng mod
(menu Auto, Auto up, Vận tiêu, Vận may, Tiện ích, Lên thành, Xa phu, chế đồ tự động...). Cả ba bản đều là
**source đầy đủ** — không còn file game nhị phân nào (jar/APK/dex/DLL gốc) trong repo.

```
pc/          Unity 6000.5.9f1 — bản PC (Windows; build được cả iOS). C#, giải nén từ KPAH_276.exe
android/     Gradle (AGP 9) — bản Android, code game 2.7.7 của TeaMobi decompile ra Java
j2me/        javac + ProGuard — bản jar J2ME (MIDP 2.0 / CLDC 1.0), code game 2.7.6 decompile ra Java
shared/mod/  code mod dùng chung cho j2me + android (viết theo API J2ME)
phat-hanh/   phát hành + tự cập nhật (GitHub Releases kpah-server/nqsh-h2-tai, phienban.json)
.github/     workflow "Phát hành" (build cả 3 bản trên GitHub Actions) và "Build iOS"
```

## Build

| Bản | Lệnh | Ra |
|---|---|---|
| PC | Unity `-executeMethod BuildCli.Win64 -kpahBan N` trên `pc/` (xem `pc/README.md`) | thư mục game Windows |
| Android | `BAN=1423 sh android/build.sh` (hoặc `cd android && ./gradlew :app:assembleRelease -PkpahBan=1423`) | `android/out/NQSH-H2-1423.apk` |
| J2ME | `sh j2me/build.sh 55` | `j2me/out/KPAH_276 (1)-automenu-v55-release.jar` |

- Android ký bằng khóa trong thư mục `KEY_DIR` (`key.properties`: `keystore=`, `alias=`, `password=` + file keystore).
  Khóa **không bao giờ** đưa vào git; trên GitHub Actions lấy từ secrets.
- Máy Windows: build.sh tự dùng JDK 17 + Android SDK đi kèm Unity; J2ME cần JDK mới (javac) + JRE 8 (`rt.jar` cho ProGuard).
- `mapping-vNN.txt` của ProGuard (bản jar) giữ riêng, không đưa lên git.
- Phát hành: `sh phat-hanh/phat-hanh.sh pc|android|jar|tat-ca` hoặc workflow "Phát hành" (xem `phat-hanh/README.md`).

## Nhánh `pokemon/chuyen-doi`: bản jar Huyền Thoại Bảo Bối (Pokémon)

- Code: `j2me/src/game/pokemon/` (bản J2ME của các lớp Pokémon trong `KPAH/ClientUnity/Assets/Scripts/Game/`),
  hook có chú thích `// pokemon:` trong Char, Monster (+ boss), NPC, Npc_Server, AnimalMove, ThanThu,
  MessageHandler (-90..-94), Font (từ điển `ChuPokemon`), TField, Res (tile), Create/CharSelectScr, màn tiêu đề.
- Tài nguyên `j2me/res/pk` (sprite, chân dung, vật thể, từ điển) + `j2me/res/pt` (tile PMD) sinh bằng
  `KPAH_ASSETS=<KPAH>/ClientUnity/Assets sh j2me/build.sh N` (tool `j2me/tools/DongGoiPokeJ2me.java`).
- Hiệu ứng chiêu (K3): `PokeHieuUng` (hook `Char.setCurrentSkillByID`, buff trong `GameScr`), dữ liệu
  `res/pk/hu.bin` + `hu_*.png` đóng gói từ `Resources/poke/hu_*.bytes`.
- Bản thử nối một server: `MAY_CHU=127.0.0.1:19129 sh j2me/build.sh N` -> `...-vN-thu.jar`
  (MANIFEST `HTBB-May-Chu` + `HTBB-Ban-Thu`; không tải danh sách server).
- Server chính cho MỌI bản: `MAY_CHU_CHINH=host:cong sh j2me/build.sh N`.
- Xác thực -88 (`KiemServer`) mặc định TẮT trên nhánh này (server Pokémon không có lệnh -88);
  `XAC_THUC=1` bật lại cho server NQSH cũ.
- Bản nhẹ cho máy thật: `GOI=nhe sh j2me/build.sh N` -> `...-vN-nhe.jar` (ProGuard + `j2me/res_nhe/pk`:
  ít loài, ít khung, chân dung 24px; loài thiếu vẽ bằng bóng đen `bong`). Xem đầu `DongGoiPokeJ2me.java`.
- Kiểm thử thuần Java: `j2me/tools/KiemPokeDuLieu.java` (xem chú thích đầu tệp).

## Code mod nằm ở đâu

- **Hook vào game** viết thẳng trong source game, có chú thích `// mod:`:
  - J2ME: `j2me/src/...` (GameService.login, MessageHandler.onMessage, GameScr.onMenuOption/showWindow/onActorMove/
    onAdd2Party/onMap, ScreenDapDo nút "Đập", ServerListScr, Canvas.paint/startOKDlg, GameScr.update).
  - Android: cùng các chỗ đó trong `android/app/src/main/java/...` + tab "Auto" của `MenuWindows`,
    `GCanvas.paint/startOKDlg`, `GameMidlet.platformRequest`, `MessageRecieve0` (link server).
  - PC: các file C# trong `pc/Assets/Scripts/Assembly-CSharp/` (thư mục `Mod/` + hook trong GameScr, Canvas, MenuWindows...).
- **Tính năng mod dùng chung** (`shared/mod/game/render/screen/`): `AutoGuard` (cổng hook), `PauseMenu` (menu mod +
  việc chế đồ), `AutoUp`, `AutoVanTieu`, `AutoVanMay`, `TienIch`, `LenThanh`. Viết theo API J2ME; bản Android
  dùng lại qua `android/port-android.py` (đổi `Canvas`→`GCanvas`, `Graphics`, `GameMidlet`...) lúc build.
  Chỗ hai nền tảng khác nhau thật đi qua lớp `Nen` (`j2me/src/.../Nen.java` và `android/.../Nen.java`).
- **Riêng từng bản**: J2ME `KiemServer`, `TaiDanhSach`; Android `NqshMod` (server, xác thực -88, tab Auto),
  `NqshCapNhat` (tự cập nhật APK); PC `Mod/*.cs` (bản C# của shared/mod), `CapNhat.cs`, `KiemServer.cs`.
  Sửa tính năng ở `shared/mod` thì sửa tương ứng ở `pc/.../Mod/` (C#).

## Nguồn gốc source game

- `pc/`: `KPAH_276.exe` (Unity 5.1) giải nén bằng AssetRipper/ILSpy.
- `j2me/src`: jar gốc 2.7.6 decompile bằng jadx (class nâng lên v49 để nhận lớp lồng), các method jadx dựng sai
  được viết lại theo bytecode; đã so bytecode chuẩn hoá từng method với bản gốc.
- `android/app/src/main/java`: APK TeaMobi 2.7.7 decompile bằng jadx; method jadx bỏ trống/dựng sai viết lại theo
  smali gốc; đã so smali build lại với APK gốc từng method. Thư viện Google (Billing, Play Services, Firebase)
  game không dùng nên đã bỏ; chỉ còn `androidx.core` (FileProvider cho tự cập nhật).
- `j2me/api/`: khai báo API SMS (WMA) chỉ để biên dịch; `j2me/tools/`: công cụ build (asm, ProGuard, midp).
