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
  `MO_TEST_CONG_KHAI.md` mục 2. Mọi bản sau phải ký cùng khoá.
- Ghi công PMDCollab (CC BY-NC 4.0): `client_jar/res/GHI_CONG.txt` có trong jar/APK; PC có `GHI_CONG.txt` cạnh `.exe`.
  Script và workflow kiểm có tệp này trước khi phát hành.
- Trước mỗi bản: tăng `BAN` + `PHIEN_BAN`, thêm mục vào `CHANGELOG.md`; bắt buộc cập nhật thì đặt
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

## NQSH-H2 cũ — chỉ khi đặt cờ rõ ràng

Mọi lệnh dưới đây cần `NQSH=1` (chạy tay) hoặc tích `xac_nhan_nqsh` (workflow "Phát hành NQSH-H2 (cũ)",
`.github/workflows/phat-hanh.yml`), và **bị từ chối trên nhánh `pokemon/*`** (nhánh đó build ra HTBB — client NQSH-H2
của người chơi cũ sẽ tự tải game Pokémon). Bỏ chặn nhánh: `CHO_PHEP_NQSH=1` (chạy tay).


Bản mới được đặt ở **GitHub Releases** của repo public [kpah-server/nqsh-h2-tai](https://github.com/kpah-server/nqsh-h2-tai)
(chỉ chứa bản build, không có mã nguồn), kèm `phienban.json` trên nhánh `main` ghi số bản mới nhất.
Không qua Nova WAF (WAF chặn `/tai/` trên nqsh-h2.top bằng trang thử thách JS) và không lộ IP VPS.
Lúc mở game, client đọc `https://raw.githubusercontent.com/kpah-server/nqsh-h2-tai/main/phienban.json`
(GitHub cache file này tối đa ~5 phút):

- **PC** (`client_pc_ios/Assets/.../CapNhat.cs`): có bản mới thì tự tải zip, kiểm sha256, tự giải nén đè
  và tự mở lại game. Người chơi không phải làm gì.
- **Android** (`client_apk/app/src/main/java/.../NqshCapNhat.java`): hỏi "Có phiên bản mới", tải APK (kiểm sha256)
  rồi mở màn hình cài đặt, người chơi bấm **Cài đặt** một lần.
- **Jar J2ME** (`client_jar/`): KHÔNG tự cập nhật (máy J2ME không tự thay jar, không đọc được HTTPS
  đời mới). Lệnh phát hành chỉ đưa jar lên link cố định + ghi số bản vào `phienban.json`.

### Cài một lần

Đăng nhập GitHub CLI bằng tài khoản có quyền ghi repo `kpah-server/nqsh-h2-tai`:
```sh
"/c/Program Files/GitHub CLI/gh.exe" auth login
```
(`cai-dat-vps.sh` / `htbb-tai.conf` = cách cũ đặt file trên VPS ở `/tai/`; không dùng nữa vì WAF chặn.)

### Mỗi lần ra bản mới

```sh
NQSH=1 sh tools/phat-hanh/phat-hanh.sh android "Sửa lỗi ..., thêm ..."   # APK
NQSH=1 sh tools/phat-hanh/phat-hanh.sh pc      "Sửa lỗi ..., thêm ..."   # PC
NQSH=1 sh tools/phat-hanh/phat-hanh.sh jar     "Sửa lỗi ..., thêm ..."   # jar J2ME (bản -release, số vNN kế tiếp)
NQSH=1 THU=1 sh tools/phat-hanh/phat-hanh.sh jar                        # chạy thử: build + tools/phat-hanh/tam/, không đẩy lên VPS
```
Script tự lấy số bản kế tiếp, build, đẩy file lên release `pc` / `android` / `jar` rồi mới ghi `phienban.json`.
Ghi chú (tham số thứ 2) hiện cho người chơi lúc cập nhật.

Link cố định để gửi người chơi mới (luôn là bản mới nhất):
- PC: `https://github.com/kpah-server/nqsh-h2-tai/releases/download/pc/NQSH-H2-PC.zip`
- Android: `https://github.com/kpah-server/nqsh-h2-tai/releases/download/android/NQSH-H2.apk`
- Jar: `https://github.com/kpah-server/nqsh-h2-tai/releases/download/jar/NQSH-H2.jar`

### `phienban.json`

```json
{
  "pc":      { "ban": 3,    "link": ".../NQSH-H2-PC-3.zip", "sha256": "...", "kichThuoc": 0, "ghiChu": "...", "ngay": "..." },
  "android": { "ban": 1420, "link": ".../NQSH-H2-1420.apk", "sha256": "...", "kichThuoc": 0, "ghiChu": "...", "ngay": "..." }
}
```
- PC: `ban` so với `Application.version` (đặt lúc build qua `HTBB_VERSION_CODE`).
- Android: `ban` là versionCode (APK gốc TeaMobi = 1418, bản NQSH-H2 bắt đầu từ 1419).
- Jar: `ban` là số vNN của `client_jar/build.sh`; script lấy số lớn hơn cả bản đã phát hành và bản
  đã build trong `client_jar/out`. File `mapping-vNN.txt` của ProGuard giữ kín, không đẩy lên.
- Muốn **thu hồi** bản lỗi: phát hành lại bản tốt với số bản lớn hơn (client không bao giờ hạ cấp).

### Lưu ý

- APK phải ký bằng **cùng khóa** (`_tmp_build/android-key`), nếu không Android từ chối cài đè.
- Bản PC/Android đã cài trước khi có tính năng này không tự cập nhật: người chơi cần cài lại một lần.
- Thử cập nhật PC trên máy: đặt biến môi trường `HTBB_CAPNHAT_URL` trỏ tới một `phienban.json` cục bộ.

### GitHub Actions — NQSH-H2 cũ (không cần máy tính)

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

Cả 3 bản build từ source trong repo (xem `README.md` ở gốc): `client_pc_ios/` (Unity), `client_apk/` (Gradle), `client_jar/` (javac + ProGuard).
