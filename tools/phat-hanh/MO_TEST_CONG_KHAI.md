# Open test công khai - build client trỏ đúng server HTBB

Kiểm tra ngày 2026-10-17 trên nhánh `pokemon/chuyen-doi`. Server game HTBB: cổng 19129.

## 1. Đặt máy chủ (host:cổng) cho bản phát hành

Không đặt thì client **rơi về danh sách server của game cũ** (`nqsh-h2.top:30774` /
`https://nqsh-h2.top/robots.txt`), không phải server HTBB.

| Client | Cách đặt | Ghi vào |
|---|---|---|
| J2ME jar | `MAY_CHU_CHINH=host:19129 sh j2me/build.sh N` (`GOI=nhe` thêm bản nhẹ) | MANIFEST `HTBB-May-Chu` (đọc ở `TaiDanhSach.mayChuThu`) |
| Android (`android/pokemon`) | `KIEU=release BAN=N MAY_CHU=host:19129 sh android/build-pokemon.sh` (= `-PmayChu=`) | `assets/htbb-app.properties` `HTBB-May-Chu` |
| PC (Unity) | sửa `ThuongHieu.MAY_CHU_CHINH` trong `pc/Assets/Scripts/Assembly-CSharp/ThuongHieu.cs` trước khi build, hoặc chạy game với `-htbbMayChu host:19129` | hằng số C# / tham số dòng lệnh |

Kiểm tra sau build:
`unzip -p j2me/out/HTBB-vN-release.jar META-INF/MANIFEST.MF | grep HTBB-May-Chu`,
`unzip -p android/out/HTBB-android-N.apk assets/htbb-app.properties | grep HTBB-May-Chu`.

## 2. Ký APK phát hành

Khóa luôn nằm NGOÀI repo (`.gitignore` chặn `*.jks`, `*.keystore`, `key.properties`).

Tạo khóa một lần (giữ bản sao lưu; mất khóa = người chơi phải gỡ app để lên bản mới):
```sh
keytool -genkeypair -v -keystore "$KEY_DIR/htbb.jks" -alias htbb -keyalg RSA -keysize 4096 -validity 10000 -dname "CN=HTBB"
```

Cách A, Gradle ký luôn: `$KEY_DIR/key.properties` có `keystore=htbb.jks`, `alias=htbb`, `password=...`, rồi chạy
`KIEU=release BAN=N MAY_CHU=host:19129 KEY_DIR=... sh android/build-pokemon.sh`.

Cách B, mật khẩu chỉ ở biến môi trường: build ra APK chưa ký (không đặt KEY_DIR), rồi chạy
```sh
KEY_DIR=... HTBB_KEYSTORE=htbb.jks HTBB_KEY_ALIAS=htbb HTBB_KS_PASS=... [HTBB_KEY_PASS=...] \
  sh android/ky-ban-phat-hanh.sh android/out/HTBB-android-N.apk
```
Script chạy zipalign rồi apksigner (v2+v3), sau đó in vân tay SHA-256. Nó từ chối chạy khi thiếu biến hoặc khi KEY_DIR nằm trong repo git.

## 3. CI (`.github/workflows/phat-hanh.yml`) chưa dùng được cho HTBB

Workflow hiện vẫn là của NQSH-H2. Nó build Android bằng `android/build.sh` (module `:app` cũ, không phải `:pokemon`), build jar mà không có `MAY_CHU_CHINH`, đặt tên file `NQSH-H2*` và đẩy lên `kpah-server/nqsh-h2-tai`.
Secrets cần có: `NQSH_TAI_TOKEN`, `ANDROID_KEYSTORE_B64`, `ANDROID_KEY_PASSWORD`, `UNITY_LICENSE`,
`UNITY_EMAIL`, `UNITY_PASSWORD` (game-ci/unity-builder@v5, Unity 6000.5.9f1; `UNITY_SERIAL` chỉ cần với license Pro).
Cách chạy: tab Actions → "Phát hành" → Run workflow. Chọn `chi_build=true` để chỉ build mà không phát hành.

## 4. Tự cập nhật: VIỆC PHẢI LÀM trước khi phát hành

`pc/.../CapNhat.cs` đọc `https://raw.githubusercontent.com/kpah-server/nqsh-h2-tai/main/phienban.json`,
tức kênh phát hành của NQSH-H2. Bản PC HTBB (`bundleVersion` 1) sẽ thấy số bản `pc` của NQSH-H2 lớn hơn và **tự tải
NQSH-H2 đè lên**. Cần tạo repo phát hành riêng cho HTBB, đổi `LINK` trong CapNhat.cs, rồi gọi
`REPO=<repo HTBB> sh phat-hanh/day-len.sh ...`. Hiện `day-len.sh` từ chối đẩy vào repo NQSH-H2 khi đang ở nhánh `pokemon/*`
(muốn bỏ qua thì đặt `CHO_PHEP_NQSH=1`).
