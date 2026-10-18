# client_apk — bản Android Huyền Thoại Bảo Bối

Module Gradle `:pokemon` = **code bản jar** (`client_jar/src` + `client_jar/shared/mod`) chạy trên lớp tương thích
MIDP cho Android (`pokemon/src/main/java/javax/microedition/**`) + lớp HD (`com/htbb/pokemon/hd`). Lúc build,
`port-j2me.py` chép code jar vào `pokemon/build/generated/htbb-j2me` và vá tối thiểu (mẫu vá lệch là dừng build).
Tài nguyên: `client_jar/res` (bản đầy đủ) + `client_jar/shared/banner/res` + `pokemon/src/main/assets`.

## Build

Cần JDK 17/21 (`JAVA_HOME`), Android SDK (`ANDROID_HOME`, mặc định `~/Android/Sdk`), python3.

```sh
sh client_apk/build-pokemon.sh                                  # debug -> out/HTBB-android-thu.apk (server 10.0.2.2:19129)
MAY_CHU=192.168.1.5:19129 sh client_apk/build-pokemon.sh        # debug nối server khác (máy thật trong LAN)
KIEU=release MAY_CHU=host:cong sh client_apk/build-pokemon.sh   # release -> out/HTBB-android-<BAN>.apk
sh client_apk/ky-ban-phat-hanh.sh client_apk/out/HTBB-android-<BAN>.apk   # ký (khóa ngoài repo, xem đầu script)
```

Hoặc trực tiếp: `cd client_apk && ./gradlew :pokemon:assembleDebug -PmayChu=host:cong -PhtbbBan=N`.

## Cấu hình

- Máy chủ: debug mặc định `10.0.2.2:19129` (= 127.0.0.1 của máy dev trong máy ảo); `MAY_CHU` / `-PmayChu`.
  Release BẮT BUỘC có máy chủ: `MAY_CHU`, `MAY_CHU_CHINH` hoặc `MAY_CHU_CHINH=` trong `tools/phat-hanh/may_chu.conf`.
- `BAN` (versionCode) / `PHIEN_BAN` (versionName): mặc định từ `tools/phat-hanh/phien_ban.conf`.
- `KEY_DIR` chứa `key.properties` (keystore=, alias=, password=) để Gradle tự ký; không có thì APK release chưa ký.
- Công cụ: `pokemon/tools/tao_icon.py` (icon), `lam_am_thanh_hd.py` (âm thanh HD), `kiem_chong_lan.py` (kiểm chồng
  lấn giao diện qua logcat).
