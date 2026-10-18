# Phát hành bản mới (client tự cập nhật)

Bản mới được đặt ở **GitHub Releases** của repo public [kpah-server/nqsh-h2-tai](https://github.com/kpah-server/nqsh-h2-tai)
(chỉ chứa bản build, không có mã nguồn), kèm `phienban.json` trên nhánh `main` ghi số bản mới nhất.
Không qua Nova WAF (WAF chặn `/tai/` trên nqsh-h2.top bằng trang thử thách JS) và không lộ IP VPS.
Lúc mở game, client đọc `https://raw.githubusercontent.com/kpah-server/nqsh-h2-tai/main/phienban.json`
(GitHub cache file này tối đa ~5 phút):

- **PC** (`pc/Assets/.../CapNhat.cs`): có bản mới thì tự tải zip, kiểm sha256, tự giải nén đè
  và tự mở lại game. Người chơi không phải làm gì.
- **Android** (`android/app/src/main/java/.../NqshCapNhat.java`): hỏi "Có phiên bản mới", tải APK (kiểm sha256)
  rồi mở màn hình cài đặt, người chơi bấm **Cài đặt** một lần.
- **Jar J2ME** (`j2me/`): KHÔNG tự cập nhật (máy J2ME không tự thay jar, không đọc được HTTPS
  đời mới). Lệnh phát hành chỉ đưa jar lên link cố định + ghi số bản vào `phienban.json`.

## Cài một lần

Đăng nhập GitHub CLI bằng tài khoản có quyền ghi repo `kpah-server/nqsh-h2-tai`:
```sh
"/c/Program Files/GitHub CLI/gh.exe" auth login
```
(`cai-dat-vps.sh` / `kpah-tai.conf` = cách cũ đặt file trên VPS ở `/tai/`; không dùng nữa vì WAF chặn.)

## Mỗi lần ra bản mới

```sh
sh phat-hanh/phat-hanh.sh android "Sửa lỗi ..., thêm ..."   # APK
sh phat-hanh/phat-hanh.sh pc      "Sửa lỗi ..., thêm ..."   # PC
sh phat-hanh/phat-hanh.sh jar     "Sửa lỗi ..., thêm ..."   # jar J2ME (bản -release, số vNN kế tiếp)
THU=1 sh phat-hanh/phat-hanh.sh jar                        # chạy thử: build + phat-hanh/tam/, không đẩy lên VPS
```
Script tự lấy số bản kế tiếp, build, đẩy file lên release `pc` / `android` / `jar` rồi mới ghi `phienban.json`.
Ghi chú (tham số thứ 2) hiện cho người chơi lúc cập nhật.

Link cố định để gửi người chơi mới (luôn là bản mới nhất):
- PC: `https://github.com/kpah-server/nqsh-h2-tai/releases/download/pc/NQSH-H2-PC.zip`
- Android: `https://github.com/kpah-server/nqsh-h2-tai/releases/download/android/NQSH-H2.apk`
- Jar: `https://github.com/kpah-server/nqsh-h2-tai/releases/download/jar/NQSH-H2.jar`

## `phienban.json`

```json
{
  "pc":      { "ban": 3,    "link": ".../NQSH-H2-PC-3.zip", "sha256": "...", "kichThuoc": 0, "ghiChu": "...", "ngay": "..." },
  "android": { "ban": 1420, "link": ".../NQSH-H2-1420.apk", "sha256": "...", "kichThuoc": 0, "ghiChu": "...", "ngay": "..." }
}
```
- PC: `ban` so với `Application.version` (đặt lúc build qua `HTBB_VERSION_CODE`).
- Android: `ban` là versionCode (APK gốc TeaMobi = 1418, bản NQSH-H2 bắt đầu từ 1419).
- Jar: `ban` là số vNN của `j2me/build.sh`; script lấy số lớn hơn cả bản đã phát hành và bản
  đã build trong `j2me/out`. File `mapping-vNN.txt` của ProGuard giữ kín, không đẩy lên.
- Muốn **thu hồi** bản lỗi: phát hành lại bản tốt với số bản lớn hơn (client không bao giờ hạ cấp).

## Lưu ý

- APK phải ký bằng **cùng khóa** (`_tmp_build/android-key`), nếu không Android từ chối cài đè.
- Bản PC/Android đã cài trước khi có tính năng này không tự cập nhật: người chơi cần cài lại một lần.
- Thử cập nhật PC trên máy: đặt biến môi trường `HTBB_CAPNHAT_URL` trỏ tới một `phienban.json` cục bộ.

## GitHub Actions (không cần máy tính)

Tab **Actions** của repo `kpah-mod-pb` → workflow **Phát hành** → **Run workflow** → chọn bản
(`tat-ca` / `android` / `pc` / `jar`) + ghi chú. Workflow `.github/workflows/phat-hanh.yml` build trên máy
Linux của GitHub (PC dùng GameCI build Windows từ Linux), rồi đẩy lần lượt từng bản bằng `day-len.sh`.
Tốn phút Actions của repo private (PC ~15-25 phút, Android / jar vài phút).

Secrets (repo `kpah-mod-pb` → Settings → Secrets and variables → Actions):

| Tên | Giá trị |
|---|---|
| `NQSH_TAI_TOKEN` | Token GitHub có quyền ghi repo `kpah-server/nqsh-h2-tai` (Contents: Read and write) |
| `ANDROID_KEYSTORE_B64` | Nội dung file `_tmp_build/android-key/ANDROID_KEYSTORE_B64.txt` (keystore dạng base64) |
| `ANDROID_KEY_PASSWORD` | Nội dung file `_tmp_build/android-key/ANDROID_KEY_PASSWORD.txt` |
| `UNITY_LICENSE` | Toàn bộ nội dung `C:\ProgramData\Unity\Unity_lic.ulf` (chỉ cần cho bản PC) |
| `UNITY_EMAIL` / `UNITY_PASSWORD` | Tài khoản Unity (chỉ cần cho bản PC) |

Cả 3 bản build từ source trong repo (xem `README.md` ở gốc): `pc/` (Unity), `android/` (Gradle), `j2me/` (javac + ProGuard).
