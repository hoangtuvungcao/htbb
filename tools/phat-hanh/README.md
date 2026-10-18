# Phát hành bản mới

## Huyền Thoại Bảo Bối (HTBB) — mặc định

Ba bản cùng **một phiên bản + một số bản** (`tools/phat-hanh/phien_ban.conf`: `PHIEN_BAN=0.9.0-opentest`, `BAN=900`),
cùng **một máy chủ production** (`tools/phat-hanh/may_chu.conf`, gitignore — chép từ `may_chu.conf.example`):

| Tệp cấu hình | Khoá | Ai đọc |
|---|---|---|
| `phien_ban.conf` (trong git) | `PHIEN_BAN`, `BAN` (chỉ tăng; `major*10000+minor*100+patch`) | `client_jar/build.sh` (MANIFEST `MIDlet-Version`/`HTBB-Phien-Ban`/`HTBB-Ban`, `PhienBan.BAN` gửi khi đăng nhập → server `sv.banToiThieu`), `client_apk/pokemon/build.gradle` (versionCode/versionName), `client_pc_ios/Assets/Editor/BuildCli.cs` (bundleVersion), workflow |
| `may_chu.conf` (KHÔNG trong git) | `MAY_CHU_CHINH=host:cổng` (**bắt buộc**), `CAP_NHAT_URL=` (rỗng = PC không tự cập nhật) | cả ba build; biến môi trường cùng tên thắng tệp |

Thiếu máy chủ là build phát hành **dừng** với thông báo (trước đây jar/APK/PC rơi về danh sách server `nqsh-h2.top`).

```sh
cp tools/phat-hanh/may_chu.conf.example tools/phat-hanh/may_chu.conf      # điền MAY_CHU_CHINH=host:19129
THU=1 sh tools/phat-hanh/phat-hanh.sh jar                           # chỉ build vào tools/phat-hanh/tam/, không đẩy
REPO=<chu>/<repo-phat-hanh-htbb> sh tools/phat-hanh/phat-hanh.sh jar     "ghi chú"   # jar thường + nhẹ
KEY_DIR=~/htbb-khoa HTBB_KEYSTORE=htbb.jks HTBB_KEY_ALIAS=htbb HTBB_KS_PASS=... \
REPO=<chu>/<repo-phat-hanh-htbb> sh tools/phat-hanh/phat-hanh.sh android "ghi chú"   # APK :pokemon, ký bằng ky-ban-phat-hanh.sh
REPO=<chu>/<repo-phat-hanh-htbb> sh tools/phat-hanh/phat-hanh.sh pc      "ghi chú"   # máy Windows có Unity 6000.5.9f1
```

- Tên tệp: `HTBB-<PHIEN_BAN>-thuong.jar`, `-nhe.jar`, `-android.apk`, `-PC.zip`; release cố định trên repo phát hành:
  tag `jar` (`HTBB.jar`), `jar-nhe` (`HTBB-nhe.jar`), `android` (`HTBB.apk`), `pc` (`HTBB-PC.zip`).
- `phienban.json` (nhánh `main` của repo phát hành): mục `jar`, `jar-nhe`, `android`, `pc` =
  `{ban, phienBan, link, sha256, kichThuoc, ghiChu, ngay}`. Bản PC đọc mục `pc` **chỉ khi** build có `CAP_NHAT_URL`
  (link raw của `phienban.json` này); open test mặc định để trống = tắt. Link kênh NQSH-H2 luôn bị bỏ qua.
- **Repo phát hành HTBB chưa có**: chủ server tạo repo public chỉ chứa bản build (vd `<chu>/htbb-tai`), đặt
  `REPO=` khi chạy tay hoặc biến `HTBB_REPO_PHAT_HANH` cho workflow. Script từ chối `REPO` là repo NQSH-H2.
- Khoá ký APK luôn **ngoài repo** (`KEY_DIR`; `ky-ban-phat-hanh.sh` từ chối nếu nằm trong repo git). Tạo khoá: xem
  [docs/build.md](../../docs/build.md) mục Android. Mọi bản sau phải ký cùng khoá.
- Ghi công PMDCollab (CC BY-NC 4.0): `client_jar/res/GHI_CONG.txt` có trong jar/APK; PC có `GHI_CONG.txt` cạnh `.exe`.
  Script và workflow kiểm có tệp này trước khi phát hành.
- Trước mỗi bản: tăng `BAN` + `PHIEN_BAN`, ghi thay đổi vào tin tức trên web và `ghiChu` của `phienban.json`; bắt buộc cập nhật thì đặt
  `sv.banToiThieu=<BAN>` ở server.

### GitHub Actions — workflow "Phát hành HTBB" (`.github/workflows/phat-hanh-htbb.yml`)

Tab Actions → **Phát hành HTBB** → Run workflow (mặc định `chi_build=true`: chỉ build, tải file ở Artifacts).

| Loại | Tên | Giá trị |
|---|---|---|
| Variable | `HTBB_MAY_CHU_CHINH` | `host:cổng` production (bắt buộc) |
| Variable | `HTBB_REPO_PHAT_HANH` | `<chu>/<repo-phat-hanh-htbb>` (cần khi phát hành thật) |
| Variable | `HTBB_CAP_NHAT_URL` | link raw `phienban.json` cho PC tự cập nhật (trống = tắt) |
| Variable | `HTBB_KEY_ALIAS` | alias khoá APK (mặc định `htbb`) |
| Secret | `HTBB_TAI_TOKEN` | token ghi repo phát hành HTBB (Contents + Releases: write) |
| Secret | `HTBB_KEYSTORE_B64` | `base64 -w0 htbb.jks` |
| Secret | `HTBB_KS_PASS` (+ `HTBB_KEY_PASS` nếu khác) | mật khẩu keystore / khoá |
| Secret | `UNITY_LICENSE`, `UNITY_EMAIL`, `UNITY_PASSWORD` | bản PC (GameCI, Unity 6000.5.9f1) |

## `phienban.json`

```json
{
  "pc":      { "ban": 3, "phienBan": "0.9.0-opentest", "link": ".../HTBB-PC.zip", "sha256": "...", "kichThuoc": 0, "ghiChu": "...", "ngay": "..." },
  "android": { "ban": 3, "phienBan": "0.9.0-opentest", "link": ".../HTBB.apk",    "sha256": "...", "kichThuoc": 0, "ghiChu": "...", "ngay": "..." }
}
```

- PC: `ban` so với `Application.version` (đặt lúc build qua `HTBB_VERSION_CODE`).
- Android: `ban` là versionCode.
- Jar: `ban` là số vNN của `client_jar/build.sh`. File `mapping-vNN.txt` của ProGuard giữ kín, không đẩy lên.
- Muốn **thu hồi** bản lỗi: phát hành lại bản tốt với số bản lớn hơn (client không bao giờ hạ cấp).

## Lưu ý

- APK phải ký bằng **cùng khóa** (`KEY_DIR`, ngoài repo), nếu không Android từ chối cài đè.
- Thử cập nhật PC trên máy: đặt biến môi trường `HTBB_CAPNHAT_URL` trỏ tới một `phienban.json` cục bộ.
