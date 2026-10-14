# Phát hành bản mới (client tự cập nhật)

Bản mới được đặt ở `https://nqsh-h2.top/tai/` (thư mục `/var/www/tai` trên VPS 180.93.127.26),
kèm `phienban.json` ghi số bản mới nhất. Lúc mở game, client đọc file này:

- **PC** (`Decompiled/KPAH_276/.../CapNhat.cs`): có bản mới thì tự tải zip, kiểm sha256, tự giải nén đè
  và tự mở lại game. Người chơi không phải làm gì.
- **Android** (`Android277/.../NqshCapNhat.java`): hỏi "Có phiên bản mới", tải APK (kiểm sha256)
  rồi mở màn hình cài đặt, người chơi bấm **Cài đặt** một lần.
- **Jar J2ME** (`_mod276/jar`): KHÔNG tự cập nhật (máy J2ME không tự thay jar, không đọc được HTTPS
  đời mới). Lệnh phát hành chỉ đưa jar lên link cố định + ghi số bản vào `phienban.json`.

## Cài một lần

```sh
sh phat-hanh/cai-dat-vps.sh
```
Tạo `/var/www/tai`, thêm `kpah-tai.conf` vào Apache (`/tai/` phục vụ thẳng từ đĩa, không qua Node).

## Mỗi lần ra bản mới

```sh
sh phat-hanh/phat-hanh.sh android "Sửa lỗi ..., thêm ..."   # APK
sh phat-hanh/phat-hanh.sh pc      "Sửa lỗi ..., thêm ..."   # PC
sh phat-hanh/phat-hanh.sh jar     "Sửa lỗi ..., thêm ..."   # jar J2ME (bản -release, số vNN kế tiếp)
THU=1 sh phat-hanh/phat-hanh.sh jar                        # chạy thử: build + phat-hanh/tam/, không đẩy lên VPS
```
Script tự lấy số bản kế tiếp, build, đẩy file lên VPS rồi mới ghi `phienban.json`.
Ghi chú (tham số thứ 2) hiện cho người chơi lúc cập nhật.

Link cố định để gửi người chơi mới (luôn là bản mới nhất):
- PC: `https://nqsh-h2.top/tai/NQSH-H2-PC.zip`
- Android: `https://nqsh-h2.top/tai/NQSH-H2.apk`
- Jar: `https://nqsh-h2.top/tai/NQSH-H2.jar`

## `phienban.json`

```json
{
  "pc":      { "ban": 3,    "link": ".../NQSH-H2-PC-3.zip", "sha256": "...", "kichThuoc": 0, "ghiChu": "...", "ngay": "..." },
  "android": { "ban": 1420, "link": ".../NQSH-H2-1420.apk", "sha256": "...", "kichThuoc": 0, "ghiChu": "...", "ngay": "..." }
}
```
- PC: `ban` so với `Application.version` (đặt lúc build qua `KPAH_VERSION_CODE`).
- Android: `ban` là versionCode (APK gốc TeaMobi = 1418, bản NQSH-H2 bắt đầu từ 1419).
- Jar: `ban` là số vNN của `_mod276/jar/build.sh`; script lấy số lớn hơn cả bản đã phát hành và bản
  đã build trong `_mod276/jar/out`. File `mapping-vNN.txt` của ProGuard giữ kín, không đẩy lên.
- Muốn **thu hồi** bản lỗi: phát hành lại bản tốt với số bản lớn hơn (client không bao giờ hạ cấp).

## Lưu ý

- APK phải ký bằng **cùng khóa** (`_tmp_build/android-key`), nếu không Android từ chối cài đè.
- Bản PC/Android đã cài trước khi có tính năng này không tự cập nhật: người chơi cần cài lại một lần.
- Thử cập nhật PC trên máy: đặt biến môi trường `KPAH_CAPNHAT_URL` trỏ tới một `phienban.json` cục bộ.
