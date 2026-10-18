# pc/ - bản PC Huyền Thoại Bảo Bối (Unity, gốc client TeaMobi 2.7.6)

Client `KPAH_276.exe` gốc (Unity 5.1.2f1) giải nén bằng AssetRipper / ILSpy, đã nâng lên
Unity **6000.5.9f1** và sửa để build PC / Android / iOS vào server NQSH-H2.
Cũng build được Android / iOS từ Unity, nhưng bản Android phát hành là `android/` (Gradle, bản TeaMobi 2.7.7).

Đã sửa so với bản giải nén:
- Server `nqsh-h2.top:17949`, nút "Cập nhật" đọc `https://nqsh-h2.top/robots.txt` (dòng `Tên|host|port`)
- `KiemServer.cs`: xác thực -88 trước khi đăng nhập (giống KiemServer.java của bản jar) + mã máy
- `ScaleGUI.cs`: phóng khung 800x600 theo cửa sổ (PC), lấp màn hình ngang (điện thoại)
- `TouchScreenKeyboard.cs`: bàn phím ảo thật trên điện thoại
- Bỏ `PlayerPrefs.DeleteAll()` lúc khởi động (mất mật khẩu đã lưu)
- Font `normalFont0/1/2/4` dựng lại từ `normalFont3.ttf`; lỗi giải nén ở `MenuWindows.cs`
- `iOSPlugins.cs`: hàm native iOS thay bằng bản C# rỗng (không còn plugin `.mm`)

Build (Unity 6000.5.9f1, batchmode `-executeMethod`):
- PC: `BuildCli.Win64` (đường ra ở biến `HTBB_OUT`)
- Android: `BuildCli.Android` (`HTBB_OUT`, `HTBB_KEY_DIR` chứa `key.properties` + keystore - KHÔNG đưa vào git)
- iOS: `BuildCli.iOS` - chạy trên GitHub Actions, xem `.github/workflows/ios.yml`

## Pokémon (nhánh `pokemon/chuyen-doi`)

Đây là client PC **chính thức** của Huyền Thoại Bảo Bối: phần Pokémon của `HTBB/ClientUnity` (bản cũ) đã gộp vào
đây, giữ nguyên mod (`Mod/`), `CapNhat`, `KiemServer`, `BuildCli`.
- Mã Pokémon: `ChuPokemon(DuLieu)`, `LoaiPokemonClient`, `PokeBody`, `PokeDuLieu`, `PokeHieuUng`, `PokeTile`,
  `TenMapPokemon`, `ThuongHieu`, `VatThePokemon` + hook trong Char, Monster/boss, NPC, Npc_Server, MessageHandler
  (-90..-94), MFont (`ChuPokemon.D`), TField, Res, Tilemap, Create/CharSelectScr...
- Tài nguyên: `Resources/poke` (sprite PMDCollab, CC BY-NC), `Resources/poke_tile` (tile), `Resources/poke_map`
  (bản đồ vẽ lại). Sinh bằng tool trong `HTBB/tools/pokemon/` (ghi thẳng vào đây; biến `HTBB_CLIENT_ASSETS`).
- Phản hồi chiến đấu (`PhanHoiChienDau.cs`, hook duy nhất ở `GameScr.startFlyText`): số sát thương viền tối,
  chí mạng vàng chữ to, "Hiệu quả cao!" cam (server gửi effect 5 = `AttackResult.EFF_HIEU_QUA_CAO`), "Trượt" xám,
  hồi máu "+N" xanh, "Lên cấp!"; Pokémon trúng đòn nháy trắng (`LiveActor.pokeNhayDen`).
- Âm thanh (`AmThanh.cs`): `Resources/am_thanh/<tên>.ogg|.wav`, thiếu tệp thì im lặng. Tên trùng bản jar
  (`j2me/res/am`, sinh bởi `j2me/tools/lam_am_thanh.py` từ gói Kenney CC0): SFX `s_danh`, `s_bidanh`, `s_chimang`,
  `s_hieuqua`, `s_truot`, `s_hoimau`, `s_lencap`, `s_tienhoa`, `s_click` (chép thẳng `.wav`); nhạc nền `n_tieude`,
  `n_thitran` (map < 110), `n_hang` (110..200), `n_dauchien` (> 200) - bản jar là MIDI, Unity không đọc MIDI nên
  cần xuất ra `.ogg` cùng tên. Âm lượng PlayerPrefs `htbb_am_sfx` / `htbb_am_nhac`; tắt bằng tham số `-htbbIm`.
- Tên hiển thị: `ThuongHieu.cs` (BuildCli đặt companyName/productName từ đây); tiêu đề cửa sổ có dấu do
  `TieuDeCuaSo.cs` đặt trên Windows. Tên file build (`NQSH-H2.exe`), applicationIdentifier giữ nguyên (tự cập nhật).
- Kiểm biên dịch không cần Unity: project dotnet `netstandard2.1` gom `Assets/Scripts/**/*.cs` + tham chiếu
  `UnityEngine*.dll` (bất kỳ bản Unity/game Unity nào có thư mục `Managed/`), `LangVersion 9`.
- Giao diện Pokémon (bản C# của `j2me/src/game/pokemon/*` + `MenuNhanh`): `BucVe` (lệnh vẽ kiểu J2ME vào ảnh,
  lọc Point - sắc nét khi phóng), `GiaoDien`, `KieuO`, `PokeChieu`, `HePoke`, `BangChieu` (tab Kỹ năng),
  `BangTiemNang` (tab Tiềm năng), `BangTrangBi` (tab Trang bị, bố cục PC), lưới đồ (`WindowInfoScr.paintGrid`),
  `BanDoVung` (Bản đồ lớn), `HudPokemon` (khung HP nhân vật / đối thủ, thanh 5 chiêu: phím 1-5, chuột rê = thẻ
  chiêu, bấm = dùng chiêu), `MenuNhanh` (một Poké Ball mép trái thay hai cột nút mod; F3 / Esc / mũi tên + Enter),
  `ChanDungPoke` (huy hiệu tròn màu hệ + hình cắt sát; chân dung lớn che tròn sẵn cho hộp thoại).
- Màn ngoài game (chọn máy chủ / đăng nhập / tạo - chọn Pokémon): `BannerParallax` - lớp ảnh `Resources/banner/`
  (BuildCli tự chép từ `../shared/banner/` nếu có; `lop.txt`: `ten,toc_do,do_sau`), chưa có ảnh thì tự vẽ trời/đồi.
- Màn hình: luôn nằm ngang; cửa sổ PC mặc định 1280x720, đổi cỡ được; màn logic cao 600, rộng 800..1400 theo tỉ lệ
  (4:3 .. 21:9), HUD neo theo mép.
- Máy chủ cố định: `-htbbMayChu host:cong` hoặc `ThuongHieu.MAY_CHU_CHINH`; bản Development dùng `127.0.0.1:19129`;
  khi có máy chủ cố định thì không tải danh sách robots.txt.
