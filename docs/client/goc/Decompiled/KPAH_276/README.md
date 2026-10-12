# KPAH_276 (giải nén, đã sửa để build)

Client `KPAH_276.exe` gốc (Unity 5.1.2f1) giải nén bằng AssetRipper / ILSpy, đã nâng lên
Unity **6000.5.9f1** và sửa để build PC / Android / iOS vào server NQSH-H2.
Nằm ngoài `Assets/` của kpah-mod-pb nên project chính không biên dịch thư mục này.

Đã sửa so với bản giải nén:
- Server `nqsh-h2.top:17949`, nút "Cập nhật" đọc `https://nqsh-h2.top/robots.txt` (dòng `Tên|host|port`)
- `KiemServer.cs`: xác thực -88 trước khi đăng nhập (giống KiemServer.java của bản jar) + mã máy
- `ScaleGUI.cs`: phóng khung 800x600 theo cửa sổ (PC), lấp màn hình ngang (điện thoại)
- `TouchScreenKeyboard.cs`: bàn phím ảo thật trên điện thoại
- Bỏ `PlayerPrefs.DeleteAll()` lúc khởi động (mất mật khẩu đã lưu)
- Font `normalFont0/1/2/4` dựng lại từ `normalFont3.ttf`; lỗi giải nén ở `MenuWindows.cs`
- `Assets/Plugins/iOS/KpahIosStubs.mm`: hàm native rỗng cho `iOSPlugins.cs`

Build (Unity 6000.5.9f1, batchmode `-executeMethod`):
- PC: `BuildCli.Win64` (đường ra ở biến `KPAH_OUT`)
- Android: `BuildCli.Android` (`KPAH_OUT`, `KPAH_KEY_DIR` chứa `key.properties` + keystore - KHÔNG đưa vào git)
- iOS: `BuildCli.iOS` - chạy trên GitHub Actions, xem `.github/workflows/ios.yml`
