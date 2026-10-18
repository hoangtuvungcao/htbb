# client_pc_ios – Huyền Thoại Bảo Bối (Unity)

Client PC Windows và iOS của Huyền Thoại Bảo Bối, một game MMORPG 2D Pokémon (fan game phi thương mại).
Nền là client Unity gốc của TeaMobi, đã chuyển hẳn sang Pokémon: sprite PMDCollab, tile và bản đồ mới,
giao diện Pokémon. Không còn chế độ kiếm hiệp.

- **Unity:** 6000.5.9f1 (`ProjectSettings/ProjectVersion.txt`), chỉ dùng module có sẵn trong bản cài.
- **Mã game:** `Assets/Scripts/Assembly-CSharp/`; kịch bản build: `Assets/Editor/BuildCli.cs`.
- **Tài nguyên:** `Assets/Resources/poke` (sprite Pokémon), `poke_tile` (tile), `poke_map` (kiểu tile và bản đồ 0),
  `res` (giao diện, hiệu ứng gốc). Sinh lại bằng tool trong `tools/pokemon/`.
- **Ghi công:** `Assets/Resources/poke/GHI_CONG*.txt` và `Assets/Resources/ghicong/GHI_CONG.txt`
  (PMDCollab, CC BY-NC 4.0). Bắt buộc giữ lại khi phát hành.

## Mở dự án

Mở thư mục `client_pc_ios/` bằng Unity Hub (đúng bản 6000.5.9f1). Bản Development (Editor) tự vào máy chủ
`127.0.0.1:19129`; muốn chọn máy chủ khác thì chạy với tham số `-htbbMayChu host:cong`.

## Build

Chạy ở batchmode: `Unity -batchmode -quit -projectPath client_pc_ios -executeMethod <hàm>`

- **PC Windows:** `BuildCli.Win64`. Đường ra lấy từ biến `HTBB_OUT`. Bản phát hành bắt buộc phải có máy chủ
  (`-htbbMayChu`, biến `MAY_CHU_CHINH` hoặc `tools/phat-hanh/may_chu.conf`); CI chạy `.github/workflows/client-phat-hanh-htbb.yml`.
- **iOS:** `BuildCli.iOS` xuất project Xcode, rồi `xcodebuild` tạo IPA chưa ký. Chạy trên GitHub Actions bằng
  `.github/workflows/client-ios.yml`, cần các secret `UNITY_LICENSE`, `UNITY_EMAIL`, `UNITY_PASSWORD`.
- Bản Android phát hành nằm ở `client_apk/`, không build từ đây.

Trạng thái iOS: **Sắp ra mắt**.

## Kiểm tra

Test server (`server/test`, ví dụ `KiemClientUnity`, `KiemMapPokemon`, `KiemTilePokemon`, `KiemChuPokemon`) đọc thẳng
mã và tài nguyên trong thư mục này. Muốn kiểm biên dịch mà không cần Unity: tạo một project dotnet gom
`Assets/Scripts/**/*.cs`, tham chiếu `UnityEngine*.dll` lấy từ thư mục `Managed/` của bất kỳ bản Unity 6 nào.
