# Android277 - APK NQSH-H2 từ KPAH_277.apk gốc của TeaMobi

Giữ nguyên giao diện / điều khiển của app Android gốc (`com.teamobi.kpah` 2.7.7), chỉ vá để vào NQSH-H2:

- `ServerListScr.loadSv` -> chỉ còn server **NQSH-H2** (`nqsh-h2.top:17949`)
- `ServerListScr.doUpdateServer` (nút "Cập nhật") -> đọc `https://nqsh-h2.top/robots.txt`, mỗi dòng `Tên|host|port`
- `GameService.login` -> xác thực `-88` trước (giống `KiemServer.java` của bản jar, xem KPAH `real/XacThucClient.java`), gói đăng nhập thêm mã máy (`andr...`, RMS `kpahmay`)
- `MessageHandler.onMessage` -> nhận gói `-88`
- Package `com.nqsh.h2`, tên app `NQSH-H2` (cài song song với bản TeaMobi)

Khi xác thực lỗi, hộp thoại hiện các bước đã qua trong `[...]` (cũng in ra logcat với tiền tố `NQSH:`).
Câu "Không thể kết nối. Vui lòng chọn server khác" KHÔNG có `[...]` = lỗi ở bước mở kết nối TCP.

## Thư mục

- `src/game/render/screen/NqshMod.java` - toàn bộ mod
- `stub/` - khai báo rỗng các class của APK gốc, chỉ để `javac` biên dịch NqshMod (không vào APK)
- `patch.py` - chèn lời gọi NqshMod vào smali, đổi package / tên app
- `build.sh` - build từ đầu: javac -> d8 -> smali -> apktool d APK gốc -> patch -> apktool b -> zipalign -> apksigner

## Build

Cần: JDK, apktool 3.x (jar), Android build-tools 36 + platform (có sẵn trong module Android của Unity), Python 3,
APK gốc `KPAH_277.apk`, thư mục khóa ký (`key.properties` gồm `keystore=`, `alias=`, `password=` + file keystore).
Đường dẫn chỉnh ở đầu `build.sh` hoặc qua biến môi trường:

```sh
APK_GOC=/duong/dan/KPAH_277.apk KEY_DIR=/duong/dan/android-key sh build.sh
```

Ra `out/KPAH277-NQSH-H2.apk`. Bản cập nhật sau phải ký bằng CÙNG khóa và tăng `versionCode`
(gốc 1418, sửa ở `work/smali-src/apktool.yml` hoặc thêm vào `patch.py`) thì mới cài đè được.
**Khóa ký và APK không đưa vào git.**
