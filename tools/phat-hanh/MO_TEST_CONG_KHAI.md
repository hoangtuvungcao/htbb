# Open test công khai - build client trỏ đúng server HTBB

Kiểm tra ngày 2026-10-17 trên nhánh `pokemon/chuyen-doi`. Server game HTBB: cổng 19129.

## 1. Đặt máy chủ (host:cổng) cho bản phát hành

**Bắt buộc** (cập nhật 2026-10-18): thiếu máy chủ là build phát hành **dừng** với thông báo lỗi — không còn rơi về
danh sách server của game cũ (`nqsh-h2.top:30774`). Một tệp cho cả ba client:

```sh
cp tools/phat-hanh/may_chu.conf.example tools/phat-hanh/may_chu.conf   # gitignore; điền MAY_CHU_CHINH=host:19129
```

| Client | Thứ tự ưu tiên | Ghi vào |
|---|---|---|
| J2ME jar | biến `MAY_CHU_CHINH` > `may_chu.conf` (`NQSH=1` = kiểu cũ, cố ý) | MANIFEST `HTBB-May-Chu` mọi bản (thường/phát hành/nhẹ) |
| Android (`client_apk/pokemon`) | `-PmayChu` / `MAY_CHU` > `MAY_CHU_CHINH` > `may_chu.conf` (chỉ release; debug vẫn `10.0.2.2:19129`) | `assets/htbb-app.properties` `HTBB-May-Chu` |
| PC (Unity) | lúc chạy: `-htbbMayChu` > `ThuongHieu.MAY_CHU_CHINH` > giá trị đóng lúc build. Lúc build (`BuildCli.Win64`): `-htbbMayChu` > `MAY_CHU_CHINH` > `may_chu.conf` | `Resources/htbb_phat_hanh.txt` (BuildCli sinh rồi xoá sau build) |

Phiên bản: `tools/phat-hanh/phien_ban.conf` (`PHIEN_BAN=0.9.0-opentest`, `BAN=900`) cho cả ba.

Kiểm tra sau build:
`unzip -p client_jar/out/HTBB-v900-release.jar META-INF/MANIFEST.MF | grep HTBB-`,
`unzip -p client_apk/out/HTBB-android-900.apk assets/htbb-app.properties | grep HTBB-`.

## 2. Ký APK phát hành

Khóa luôn nằm NGOÀI repo (`.gitignore` chặn `*.jks`, `*.keystore`, `key.properties`).

Tạo khóa một lần (giữ bản sao lưu; mất khóa = người chơi phải gỡ app để lên bản mới):
```sh
keytool -genkeypair -v -keystore "$KEY_DIR/htbb.jks" -alias htbb -keyalg RSA -keysize 4096 -validity 10000 -dname "CN=HTBB"
```

Cách A, Gradle ký luôn: `$KEY_DIR/key.properties` có `keystore=htbb.jks`, `alias=htbb`, `password=...`, rồi chạy
`KIEU=release BAN=N MAY_CHU=host:19129 KEY_DIR=... sh client_apk/build-pokemon.sh`.

Cách B, mật khẩu chỉ ở biến môi trường: build ra APK chưa ký (không đặt KEY_DIR), rồi chạy
```sh
KEY_DIR=... HTBB_KEYSTORE=htbb.jks HTBB_KEY_ALIAS=htbb HTBB_KS_PASS=... [HTBB_KEY_PASS=...] \
  sh client_apk/ky-ban-phat-hanh.sh client_apk/out/HTBB-android-N.apk
```
Script chạy zipalign rồi apksigner (v2+v3), sau đó in vân tay SHA-256. Nó từ chối chạy khi thiếu biến hoặc khi KEY_DIR nằm trong repo git.

## 3. CI — workflow "Phát hành HTBB" (`.github/workflows/phat-hanh-htbb.yml`)

Build jar (thường + nhẹ), APK `:pokemon` (ký bằng `client_apk/ky-ban-phat-hanh.sh`, khoá giải từ secret vào thư mục tạm
ngoài repo rồi xoá), PC qua GameCI (tham số `-htbbBan/-htbbMayChu/-htbbCapNhat`), tên `HTBB-<phiên bản>-thuong.jar`,
`-nhe.jar`, `-android.apk`, `-PC.zip`, rồi `day-len.sh` lên repo phát hành HTBB (biến `HTBB_REPO_PHAT_HANH`,
**chủ server tạo**). Mặc định `chi_build=true`. Biến / secrets: `tools/phat-hanh/README.md`.
Workflow NQSH-H2 cũ (`phat-hanh.yml`) chỉ chạy khi tích `xac_nhan_nqsh` và **không** chạy từ nhánh `pokemon/*`.
Chạy tay: `tools/phat-hanh/phat-hanh.sh` (mặc định HTBB; kiểu cũ cần `NQSH=1`).

## 4. Tự cập nhật PC

`pc/.../CapNhat.cs` đọc link `ThuongHieu.capNhatUrl()` = `CAP_NHAT_URL` (hằng số `ThuongHieu.CAP_NHAT_URL` hoặc giá trị
đóng lúc build từ `may_chu.conf` / `-htbbCapNhat`). **Rỗng = tắt hẳn** (không gửi request nào) — mặc định cho open
test. Link chứa `nqsh-h2` (kênh `kpah-server/nqsh-h2-tai`) luôn bị bỏ qua, kể cả khi `phienban.json` ghi nhầm link tải.
Bật lại sau khi có repo phát hành HTBB: `CAP_NHAT_URL=https://raw.githubusercontent.com/<chu>/<repo>/main/phienban.json`.
`bundleVersion` = `BAN` (900).

## 5. Ghi công (CC BY-NC 4.0)

`client_jar/res/GHI_CONG.txt` (sinh bằng `client_jar/tools/sinh_ghi_cong.py` từ ghi công PMDCollab của HTBB + tên họa sĩ
`client_jar/tools/pmdcollab_credit_names.txt`) nằm trong jar và APK; PC có `Resources/ghicong/GHI_CONG.txt` và
`GHI_CONG.txt` cạnh `.exe`. Trong game: Menu màn đăng nhập → **Ghi công** (jar/APK), nút **Ghi công** dưới khung
đăng nhập (PC); cuộn được, dòng đầu là phiên bản.
