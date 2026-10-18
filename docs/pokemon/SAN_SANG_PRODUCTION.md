# Sẵn sàng production — Open test công khai Huyền Thoại Bảo Bối

Kiểm toán ngày 17/10/2026 trên nhánh `pokemon/chuyen-doi` của ba repo (`HTBB`, `htbb-client`, `HTBB-Web`).
**Không triển khai gì cả**: không deploy, không push, không phát hành, không đổi DNS. Mọi thứ dưới đây
đã chạy trên máy dev (một máy chủ thử thứ hai, cổng riêng, CSDL riêng) — chủ server làm các bước ở mục
"Chủ server phải làm".

Ký hiệu: ✅ sẵn sàng · ⚠️ chạy được nhưng có điều kiện / cần quyết định · ❌ chặn mở cửa (phải xử lý trước).

## 0. Tóm tắt — chặn mở cửa và quyết định

**Chặn (❌) — phải xử lý trước khi phát link tải:**

> Cập nhật 18/10/2026: mục 1–4 **đã sửa trong code** (`htbb-client`, chưa commit, chưa phát hành) — chi tiết
> `htbb-client/phat-hanh/MO_TEST_CONG_KHAI.md`. Còn việc của chủ server: tạo `phat-hanh/may_chu.conf`, repo phát hành
> HTBB, khoá ký APK, biến/secrets cho workflow "Phát hành HTBB". (1) PC tự cập nhật đọc `CAP_NHAT_URL`, rỗng = tắt,
> link NQSH-H2 luôn bị bỏ qua. (2) Thiếu máy chủ = build phát hành dừng (jar/APK/PC, `phat-hanh/may_chu.conf`).
> (3) `phat-hanh.sh` + `.github/workflows/phat-hanh-htbb.yml` build `HTBB-<ver>-*`; NQSH-H2 chỉ với `NQSH=1` /
> `xac_nhan_nqsh`, bị chặn trên `pokemon/*`. (4) `GHI_CONG.txt` trong jar + màn "Ghi công". Phiên bản
> `0.9.0-opentest` = số bản **900** (`phat-hanh/phien_ban.conf`, `CHANGELOG.md`) → `sv.banToiThieu=900` khi mở cửa.

1. **Client PC tự cập nhật thành game cũ.** `htbb-client/pc/Assets/Scripts/Assembly-CSharp/CapNhat.cs:27` đọc
   feed NQSH-H2; bản HTBB có `bundleVersion: 1` nên sẽ tải zip NQSH-H2 đè lên chính nó. Đổi `LINK` sang repo
   phát hành HTBB mới **hoặc** tắt tự cập nhật PC cho open test.
2. **Bản phát hành chưa gắn máy chủ.** Không đặt host:port thì jar/APK/PC mặc định về `nqsh-h2.top:30774`.
   Build bằng `MAY_CHU_CHINH=host:19129` (jar), `MAY_CHU=host:19129` (APK), `ThuongHieu.MAY_CHU_CHINH`/`-htbbMayChu` (PC).
3. **Pipeline phát hành vẫn là NQSH-H2** (`phat-hanh.sh`, `.github/workflows/phat-hanh.yml`): build Android
   bằng module cũ `:app`, tên tệp `NQSH-H2*`, đẩy lên `kpah-server/nqsh-h2-tai`. Đã chặn đẩy nhầm từ nhánh
   `pokemon/*` (`phat-hanh/day-len.sh`, cần `CHO_PHEP_NQSH=1` mới qua) — cần repo phát hành HTBB mới.
4. **Jar thiếu ghi công PMDCollab (CC BY-NC 4.0 bắt buộc ghi công).** APK có `assets/GHI_CONG.txt`, jar chỉ có
   `LICENSE-audio.txt`. Thêm `GHI_CONG.txt` vào `j2me/res` (thư mục của nhóm QA J2ME).
5. **Trang web thiếu thông báo độ tuổi + link Điều khoản/Quyền riêng tư ở chân trang** (mục 6).

**Quyết định của chủ server** (chi tiết ở từng mục):

| # | Quyết định | Khuyến nghị |
|---|---|---|
| Q1 | CSDL production | **MariaDB ≥ 10.4** (đã thử nạp đủ, có `PASSWORD()`); NovaDB chỉ cho máy dev |
| Q2 | Cỡ VPS | **4 vCPU / 8 GB** cho ≤ 300 CCU (mục 4); 2 vCPU / 4 GB nếu chắc < 100 |
| Q3 | Băm mật khẩu | Giữ `PASSWORD()` cho open test (login server là jar, không có nguồn); kế hoạch thay ở mục 1.2 |
| Q4 | `sv.socketTimeout` / `sv.connRateWindow` đang = 0 (tắt) trong `vps/server.ini.tmpl` | Bật 300 / 30 sau khi thử client thật đứng yên 5 phút không bị đá |
| Q5 | Đường vào mạng | IP công khai trực tiếp (không qua frp) — qua frp mọi người chơi là `127.0.0.1`, mất chống DoS theo IP |
| Q6 | `sv.gmCanCoPInfo=1` | Bật, sau khi đặt `pInfo[16]=1` cho nhân vật GM thật |
| Q7 | Thương hiệu / IP Pokémon | Đọc mục 6 — giữ tên thật (rủi ro gỡ cao) hay đổi tên loài/thương hiệu trước khi công khai |
| Q8 | Chính sách wipe | Wipe nhân vật sau open test, giữ tài khoản (mục 7) |
| Q9 | Kênh hỗ trợ (Discord/Zalo/Facebook) | Chọn một, ghi vào trang chủ + `sv.website` |
| Q10 | HSTS / CSP web | CSP bật mặc định; HSTS chỉ khi chắc chắn chạy HTTPS (`HTBB_HSTS=1`) |
| Q11 | Bỏ chỉ mục `tob_char.Index_5` (pInfo) | Nên bỏ (mục 2.3) |

## 1. Bảo mật

### 1.1 Máy chủ game

| Hạng mục | TT | Ghi chú / việc đã làm |
|---|---|---|
| Xác thực -88 (RSA) tắt trên nhánh Pokémon | ⚠️ | Xem đánh giá dưới bảng. Đã thêm **cổng phiên bản** `sv.banToiThieu` (không vỡ client nào). |
| Cửa hậu tài khoản cứng `staynight` / `lamkckc` | ✅ **đã sửa** | `Map.isAcountAdmin` trả `true` cho hai tên tài khoản này → ai **đăng ký trùng tên trên web** là gửi được gói 47 `shutdown`. Đã bỏ. |
| Gói 47 (lệnh quản trị) nhận theo **tên tài khoản** | ✅ **đã sửa** | `AdminHandler.isAuthorizedAdmin` cho qua nếu tên tài khoản nằm trong `sv.adminAccounts` — mà tên tài khoản thì ai cũng đăng ký được (vd tài khoản `chienthan`). Nay chỉ nhận **nhân vật online có quyền GM**; mọi lệnh ghi `AdminChat_lenh47.txt`. |
| GM = tên trong `sv.adminAccounts` (+ `pInfo[16]`) | ⚠️ | Mã cũ cấp GM **chỉ theo tên** (README nói cần cả hai — sai). Thêm `sv.gmCanCoPInfo=1` để bắt buộc cả hai (mặc định 0 = như cũ). Tên trong danh sách bị chặn khi tạo nhân vật (`laTenDanhRieng`) nên rủi ro thấp; nhưng **đừng thêm vào danh sách tên một nhân vật người chơi đã có**. |
| Lệnh GM được ghi log | ✅ | Chat GM → `AdminChat.txt` (`MapChat:146`), gói 47 → `AdminChat_lenh47.txt` (mới, có trong logrotate). |
| Lệnh `ban` trong game | ⚠️ | Không có tác dụng (`Database.banChar(String,int)` trả về ngay). Khoá qua web `/admin/taikhoan`, `/admin/cammay`. Ghi trong `SO_TAY_GM.md`. |
| `mto`/`loadshop` không cần GM khi `sv.server=0` | ⚠️ | `TeamServer.isServerLocal()`. Production `sv.server=1` — **không bao giờ đặt 0**. |
| `camchat` cho 3 tên tài khoản cứng (`kongpeo`, `phieubatgiangho6789`, `staynight`) | ⚠️ | Chỉ khi `sv.me=1`. Production `sv.me=0` → vô hiệu. Không sửa (điều kiện rối, rủi ro sửa nhầm). |
| testMode / testCode / CodeTest / mã giftcode "cửa sau" | ✅ | `sv.testMode=0`, `sv.CodeTest=0`; mã test 100 triệu xu và các mã cửa sau đã bị nhóm GD2 xoá hẳn (`DatabaseGiftCode`). |
| SQL injection | ✅ **đã sửa 1** | Quét 137 chỗ ghép chuỗi SQL. Tên tài khoản/mật khẩu/tên nhân vật/tên bang đều qua bộ lọc chỉ chữ+số (`CreateCharHandler.checkInfo*`, đã thử với `' \ " ; %`), mã quà lọc `[a-zA-Z0-9_-]`. **Lỗ thật:** đuổi thành viên bang (`MapDoi.doKickMemberOutNewClan`) đưa tên đọc từ gói tin thẳng vào `Database.getIDClanChar(String)` → bang chủ tiêm SQL được. Đã đổi cả hai hàm `getIDClanChar` sang `PreparedStatement`. Còn `checkLoginIndo`/`addNewAcountAuto` ghép chuỗi nhưng chỉ chạy khi `sv.lang=1` (Indo) và đầu vào đã lọc chữ+số. |
| Kiểm tra gói tin (độ dài, số âm, chỉ số) | ✅ | Khung gói: `Session.readMessage` chặn > 10000 byte, bỏ gói ≥ 1024; số lượng mua/quyên góp đã kiểm `> 0`, nhân `long` (KiemTienAm…). Ngập gói: `BoDemGoiTin` 300/600 gói mỗi 5 s. Lỗi xử lý gói trước đây **bị nuốt im lặng** — nay được đếm (`loi_xu_ly`, mục 3). |
| Chống DoS (`sv.*`) | ⚠️ | `maxConnPerIp=50`, `connRateMax=60`, `dosBanSec=300`, ngập gói 300/600: hợp lý. **Nhưng** `vps/server.ini.tmpl` đặt `sv.socketTimeout=0` và `sv.connRateWindow=0` = **tắt** hai lớp (kết nối im lặng không bao giờ bị đóng, mỗi kết nối 2 thread). Và `isLocalIp()` miễn mọi IP `127.*`, `10.*`, `192.168.*` → qua frp/proxy là mất hết chống DoS theo IP. Quyết định Q4, Q5. |
| `sv.iplogin` / `sv.portlogin` bị bỏ qua | ✅ **đã sửa** | `LogInController` mở kết nối **trước** khi đọc `server.ini` → luôn nối `127.0.0.1:8023`. Phát hiện khi máy chủ thử (cổng 8123) đi đăng nhập qua login server thật. Nay đọc sớm hai khoá (`TeamServer.docDiaChiLoginSom`). |
| Bí mật trong git | ✅ | Quét cây làm việc ba repo: không có khoá riêng, keystore, `.env`, token. Mật khẩu trong `server.ini.tmpl` là `${...}` (GitHub Secrets); tệp test PHP (web PHP cũ, đã bỏ 19/10/2026) dùng mật khẩu giả. `.gitignore` chặn `server.ini`, dump, `config.local.php`, `.env`, keystore. Thêm `trang_thai.json`, `bao_tri.txt`. |

**Đánh giá -88 / giả mạo client.** Bước -88 cũ là *client kiểm server* (chữ ký RSA của server NQSH, khoá
riêng không có) cộng một "bằng chứng" SHA-256 tính từ hằng số trong jar. Nó **không bao giờ** chống được
client giả: mọi bí mật nằm trong jar/APK đều trích ra được (bot `tools/pokemon/kiem_tai/BotTai.java` nói
giao thức thật mà không cần -88). Rủi ro thật của client giả là gửi gói tin tuỳ ý — chỉ chặn được bằng
**kiểm tra phía server** (đã có: lọc tên, số lượng, ngập gói, tiền âm, nghi vấn gian lận). Do đó:

- **Đã làm:** `sv.banToiThieu=N` — jar/APK gửi sẵn số bản build (`writeInt(PhienBan.BAN)` sau mã máy,
  `shared/mod/.../AutoGuard.java:168-184`); server từ chối bản `< N` kèm link tải (`sv.linkTaiGame`). PC Unity
  không gửi số → luôn qua. Dùng để **ép cập nhật** khi bản cũ có lỗi nghiêm trọng, không phải chống giả mạo. Đã thử thật với `sv.banToiThieu=5`: bản 3 bị từ chối kèm link tải, bản 7 và client không gửi số (PC) vào bình thường; log `[CLIENT] … ban=<n>`.
- **Đề xuất (chưa làm, cần sửa cả 3 client trước):** trường cuối mới `writeUTF("HTBB1") + writeInt(ban) +
  write(HMAC-SHA256(k, khoá -40 ‖ ban))`, server cờ `sv.xacThucClient` 0=tắt / 1=chỉ ghi log / 2=bắt buộc. Chỉ
  cản bot "lười"; không thay được kiểm tra phía server. Không đáng làm trước open test.

### 1.2 Mật khẩu

| Hạng mục | TT | Ghi chú |
|---|---|---|
| Băm `PASSWORD()` (SHA1 hai lần, không muối) | ⚠️ | Login server `CheckLoginSocket.jar` **chỉ có jar** (không nguồn) và so `password=PASSWORD(?)`; web đăng ký/đổi mật khẩu cũng dùng `PASSWORD()` cho khớp. MySQL 8 đã bỏ hàm này → phải MariaDB. |
| Kế hoạch bcrypt/scrypt | ⚠️ | Web đã có `server/utils/matKhauV2.ts` (scrypt, **tắt**, chưa nối vào đâu) + kế hoạch 3 pha cột kép trong đầu tệp. Thêm cột băm mạnh **không làm CSDL an toàn hơn** chừng nào cột `PASSWORD()` còn phải giữ cho jar. Cách duy nhất: viết lại login server (giao thức đơn giản: `long sessionId, UTF user, UTF pass, byte` → `long, int userId, boolean, byte`, xem `io/LogInController.java`) dùng bcrypt, chuyển dần khi người chơi đăng nhập. Làm sau open test. |
| Lộ mật khẩu qua mạng | ⚠️ | Giao thức game chỉ XOR với khoá -40 — coi như **rõ**. Dặn người chơi không dùng lại mật khẩu (ghi ở trang đăng ký/điều khoản). |

### 1.3 Website (HTBB-Web)

| Hạng mục | TT | Ghi chú |
|---|---|---|
| Đăng nhập | ✅ | Token HMAC-SHA256 qua `Authorization: Bearer` (không cookie — Nova WAF lọc cookie). Người chơi 7 ngày; admin 60 phút rảnh / 12 giờ tối đa; đổi mật khẩu/bị khoá là token hết hiệu lực. Khoá ký: `HTBB_SECRET` (production không có thì sinh ngẫu nhiên + cảnh báo). |
| Phân quyền admin | ✅ | Mọi `server/api/admin/**` qua `defineAdminHandler` + vai trò; chỉ `auth/status` và `verify-2fa` công khai (đúng thiết kế). 2FA tuỳ chọn (`HTBB_ADMIN_2FA_BAT_BUOC=1` để bắt buộc — **nên bật**). |
| CSRF | ✅ | Bearer token, không cookie → CSRF cổ điển không áp dụng; thêm `server/middleware/admin-csrf.ts` kiểm Origin. |
| Giới hạn tần suất | ✅ | `web_chan_spam`: sai đăng nhập 15/15 phút, đăng ký 5/giờ, quên mật khẩu 5/giờ/IP + 3/ngày/tài khoản, admin 8/15 phút. Giftcode chỉ nhập trong game (có giãn cách `GIFTCODE_COOLDOWN_MS`). |
| Header bảo mật | ✅ **đã thêm** | `server/middleware/bao-mat-header.ts`: CSP (`default-src 'self'`, script/style `'unsafe-inline'` vì Nuxt), Permissions-Policy, COOP, nosniff; HSTS 180 ngày chỉ khi production + HTTPS qua proxy tin cậy (`HTBB_HSTS`). `HTBB_CSP=enforce|report-only|off`. Đã thử bản build: header có, trang chủ/điều khoản/`/api/trang-thai` 200. Còn lộ `x-powered-by: Nuxt` (nhỏ). |
| SQL injection web | ✅ | Mọi truy vấn có đầu vào dùng `?`. Sửa thêm: `/api/admin/nhanvat?trang=` không chặn trên (SQL hỏng, không tiêm được) → chặn 1.000.000. |
| Nạp tiền | ✅ | Khoá bởi `server/middleware/tat-nap.ts` khi `HTBB_NAP_BAT` tắt (mặc định nhánh Pokémon). **Giữ tắt** (mục 6). |
| Upload CMS trên VPS | ⚠️ | `htbb-web.service` có `ProtectSystem=strict`, không `ReadWritePaths`; upload ghi vào `/opt/htbb-web/current/uploads` (bị thay mỗi lần deploy). Đặt `HTBB_UPLOAD_DIR=/var/lib/htbb-web/uploads` + `ReadWritePaths=` cho nó. |
| Node | ⚠️ | Build **chỉ qua với Node 22** (Nuxt 4.5/Vite 8); Node 20.20 lỗi `trustedFunctions.difference`. `deploy/cai-dat-vps.sh` còn chấp nhận Node 20 → ghim 22. |

## 2. Cơ sở dữ liệu

### 2.1 Chọn CSDL — đã thử nạp `sql/htbb_day_du.sql`

| CSDL (tự dựng, không container) | Kết quả |
|---|---|
| **MariaDB 10.4.32** (`/opt/lampp`, datadir riêng, cổng 3420, `STRICT_TRANS_TABLES`) | ✅ nạp sạch: `account` 1 bảng + `htbb_db` 101 bảng, 0 lỗi, < 1 s |
| **NovaDB 0.3.0** (instance riêng, cổng 3419) | ✅ nạp sạch, 0 lỗi (~4 s) |
| MySQL 8.0.46 (datadir riêng, cổng 3421) | ❌ dừng ở dòng 392: `BLOB/TEXT column 'nhanQuaOnline' can't have a default value`; và **không có `PASSWORD()`** → login server không chạy. Loại. |

**Khuyến nghị Q1: MariaDB ≥ 10.4** cho production: có `PASSWORD()`, chạy được ở chế độ strict, công cụ sao lưu
chuẩn (`mysqldump --single-transaction`), toàn bộ tài liệu VPS/backup viết cho nó. NovaDB cần hai bản vá
**chưa commit bên repo NovaDB** (`PASSWORD()`, `NOVADB_MYSQL_TIME_ZONE`), so sánh chuỗi phân biệt hoa/thường,
không truy vấn chéo CSDL — giữ cho máy dev.

Cấu hình MariaDB mới: **`vps/htbb-mariadb.cnf`** — lý do chính:
- Game server **mở sẵn ~137 kết nối** khi khởi động (đo được: `db.maxco=100` + 2×`db.maxNap=30` + …); mặc định
  `max_connections=151` → thêm web/login/backup là "Too many connections". Đặt 300.
- **Login server không đăng nhập được** nếu máy chủ mặc định `utf8mb4_unicode_ci`: Connector/J trong jar báo
  `Unknown initial character set index '224'` (đo được). Đặt `character-set-server=utf8` **hoặc** thêm
  `characterEncoding=UTF-8` vào `db.url` (đã sửa `vps/loginServer.server.ini.example`).

### 2.2 Migration — chạy lại được không (đã chạy mỗi tệp 2 lần trên bản sao)

| Tệp | Kết quả |
|---|---|
| `web_cms.sql`, `them_bang_*` (trừ dưới), `pokemon_doi_*`, `pokemon_du_lieu_quai`, `pokemon_icon_vat_pham`, `them_bang_pokedex`, `them_bang_lau_dai`, `them_chi_muc_production` (mới) | ✅ chạy lại không lỗi |
| `them_cot_kho_xu_luong`, `them_cot_pokemon` (1060 trùng cột), `them_khoa_duy_nhat_giftcode_log` (1061), `them_phi_phong_vo_hinh`, `pokemon_vat_pham_eevee` (1062 trùng khoá) | ⚠️ lỗi "đã có" vô hại — chạy bằng `mysql -f` như `tools/pokemon/xuat_sql_day_du.sh` |
| `pokemon_lau_dai.sql` (đang được nhóm GD2 sửa) | ❌ **trên MariaDB strict**: `ERROR 1366 Incorrect integer value: '' for column data_quest_lau_dai.so_luong` (dòng 747) và `ngay_mo` (dòng 949, 1050) → cả lệnh INSERT nhiều dòng bị bỏ. Báo nhóm GD2 sửa `tools/pokemon/sinh_sql_lau_dai.py` (ô trống phải ra `0`/`NULL`), rồi sinh lại `htbb_day_du.sql`. **Không** tắt strict để chữa. |

Lưu ý: máy chủ tự tạo thêm **27 bảng** khi chạy lần đầu (101 → 128: bảng theo kỳ, `tob_vi_tri_online`, …) — bình thường.
Lỗi lúc khởi động trên bản sao: `[TOP EVENT] loi migrate id_char ... Duplicate entry '51' for key 'idx_id_char'`
— `tob_top_event` có dòng trùng (400 dòng / 300 nhân vật sau test): các câu `INSERT ... ON DUPLICATE KEY UPDATE`
dựa trên khoá mà bảng không có (chỉ có PRIMARY id). Không làm sập; báo nhóm sự kiện.

### 2.3 Chỉ mục bảng nóng — `sql/them_chi_muc_production.sql` (mới, chạy lại được, đã thử MariaDB + NovaDB)

| Bảng | Truy vấn nóng | Trước | Thêm |
|---|---|---|---|
| `5h_active` | `check_active` **mỗi lần đăng nhập** + nhập giftcode | chỉ PRIMARY → quét cả bảng | `username`, `userID` |
| `tob_char_vip` | cập nhật VIP theo `charname` | chỉ PRIMARY | `charname` |
| `tob_buy_ruong` | `dateLog` + `charname` | chỉ PRIMARY | `(dateLog, charname)` |
| `tob_qua_ct_top` | `charname` + `nhan` | chỉ PRIMARY | `charname` |
| `tob_char`, `team_user`, `tob_pet`, `tob_inbox`, `tob_log_all_item`, `tob_clan`, `tob_pokedex` | — | ✅ đã có chỉ mục đúng cột tra | — |

**Q11:** `tob_char.Index_5` là chỉ mục trên `pInfo varchar(2000)` (prefix 333) — không truy vấn nào lọc theo
nó, nhưng **mỗi lần lưu nhân vật** đều cập nhật. Nên `ALTER TABLE tob_char DROP INDEX Index_5;` (ghi trong tệp,
không tự chạy).

### 2.4 Sao lưu / khôi phục

| Hạng mục | TT | Ghi chú |
|---|---|---|
| Sao lưu hằng ngày | ✅ | `vps/backup_db.sh` (đã có): `--single-transaction`, `pipefail`, kiểm `gzip -t` + "Dump completed", giữ 7 ngày. **Thêm:** bản Chủ nhật giữ 5 tuần (`backup/tuan/`), chép ra máy khác nếu đặt `HTBB_BACKUP_DICH` trong `/etc/default/htbb-backup` (rsync). |
| Thử khôi phục | ✅ **mới** | `vps/kiem-khoi-phuc-csdl.sh`: nạp bản mới nhất vào CSDL tạm `<db>_kiem_khoi_phuc`, so số bảng + số dòng `tob_char/tob_pet/tob_clan/tob_pokedex/data_item`, xoá CSDL tạm; báo lỗi nếu bản lưu cũ > 48 giờ. **Đã chạy thật** trên bản sao: 128/128 bảng, 300/300 nhân vật, nạp 1 s → OK. Cron tuần (trong đầu tệp). Đầu tệp có quy trình khôi phục thật. |
| Bản sao ngoài máy | ⚠️ | Chủ server chọn đích (VPS thứ hai / máy nhà qua ssh key). Không có thì mất VPS = mất hết. |

## 3. Vận hành

| Hạng mục | TT | Ghi chú / việc đã làm |
|---|---|---|
| systemd game/login | ✅ | `vps/htbb-game.service` (`Restart=always`, `StartLimitBurst=5/300s`, `TasksMax=infinity`, `LimitNOFILE=65535`, `-Xss256k`, `TimeoutStopSec=180`), `vps/htbb-login.service`. **Đã cập nhật cờ JVM** (mục 4). |
| systemd web | ✅ | `HTBB-Web/deploy/htbb-web.service` (xem lưu ý upload mục 1.3). |
| systemd CSDL | ✅ | Unit `mariadb.service` của distro + `vps/htbb-mariadb.cnf`. `htbb-game` đã `Wants=mariadb.service`. |
| Xoay vòng log | ✅ | `vps/htbb-logrotate.conf` (game.out/login.out copytruncate 30 ngày, các `.txt` gốc); thêm `AdminChat_lenh47.txt`. |
| Tắt có trật tự (lưu người chơi) | ✅ **đã thử** | `SIGTERM` → shutdown hook `TeamServer` lưu bang, nhân vật, chợ, vận tiêu, log. Kết quả thử thật ở mục 3.1. |
| Tự khởi động lại khi chết | ✅ | `Restart=always`. Khi **treo** (cổng mở nhưng không xử lý): mới `vps/kiem-suc-khoe.sh` (cron mỗi phút, `TU_KHOI_DONG_LAI=1`). |
| Kiểm tra sức khoẻ | ✅ **mới** | `vps/kiem-suc-khoe.sh`: bắt tay -40 thật tới cổng game (đo ms), nhịp tim `trang_thai.json` (< 120 s), cổng login, `SELECT 1`, web `/api/trang-thai`. Đã thử: khoẻ → `game=ok(12ms) … login=ok csdl=ok`, mã 0; sai cổng → `game=HONG`, mã 1. |
| Trang trạng thái công khai | ✅ | Web `GET /api/trang-thai` → `{song, online, capNhat}` (đọc `tob_vi_tri_online`, game ghi mỗi 30 s). |
| Chế độ bảo trì | ✅ **mới** | Trước đây chỉ có nút "Dừng đăng nhập" trong AdminPanel (Swing — VPS không có màn hình). Nay: tạo `/opt/htbb/server/bao_tri.txt` → ≤ 5 s sau từ chối đăng nhập mới (người đang chơi không bị đá); dòng đầu = câu báo; `cho: gm1 gm2` = tài khoản vẫn vào được; xoá tệp = mở lại. Tắt hẳn bằng `sv.chiSoMayChu=0`. |
| Chỉ số cho trang quản trị | ✅ **mới** | Lớp `server/ChiSoMayChu.java`: mỗi 30 s ghi một dòng `tob_chi_so_may_chu` (giữ 7 ngày): `ket_noi, nhan_vat, heap_mb, heap_toi_da_mb, so_thread, cpu_phan_tram, tai_may, goi_vao, goi_bo, cat_ngap, loi_xu_ly, bao_tri` (bốn bộ đếm là **số tăng trong 30 s**, chia 30 ra /giây) + `trang_thai.json` cùng nội dung. Bộ đếm gắn ở `io/Session` (gói vào, gói bị bỏ vì ngập, kết nối bị cắt vì ngập, lỗi xử lý gói — trước đây bị nuốt). **API đã thêm:** `HTBB-Web/server/api/admin/chi-so-may-chu.get.ts` (`GET /api/admin/chi-so-may-chu?gio=6`, mọi vai trò admin; gộp điểm, đổi bộ đếm ra /giây, trả `songGanNhat` = mẫu mới nhất cách bao nhiêu giây; SQL đã thử MariaDB + NovaDB; build Node 22 OK). **Việc cho nhóm web/CMS:** vẽ biểu đồ ở `/admin` (tổng quan / chẩn đoán). Đã thử thật: bảo trì chặn/cho đúng tài khoản `cho:`, `trang_thai.json` + bảng ghi đều 30 s. Đã có sẵn: `tob_log_online` (5 phút), `/api/admin/online`. |

### 3.1 Thử tắt có trật tự (máy chủ thử, MariaDB `htbb_db_prod_thu`)

Kịch bản `san_sang/thu_tat_em.sh` (scratch): 30 bot đang chơi (10 GM tự `slv` lên cấp), sau 75 s gửi
`kill -TERM` vào máy chủ **khi 29 nhân vật đang online** (đọc từ `trang_thai.json`).

| Kiểm | Kết quả |
|---|---|
| Shutdown hook chạy | ✅ `[SHUTDOWN] Dang luu du lieu…` → `savechar` → `SAVE XONG` → `[SHUTDOWN] Da luu xong.` |
| Thời gian thoát | ✅ **1 s** với 29 người (còn rất xa `TimeoutStopSec=180`) |
| Dữ liệu đã ghi | ✅ trước TERM đặt `xp=0, lastLog=2000-01-01` cho 5 nhân vật; sau TERM: 4 nhân vật đã vào game có `lastLog` = giây TERM, 2 GM có `xp` mới (5.160.202 / 5.160.033) |
| Bot không vào được | ⚠️ 1/30 (`tai001`) nhận "Có lỗi xảy ra khi đăng nhập" (login server trả -3) ngay sau lần chạy trước — nghi phiên cũ chưa kịp nhả ở login server; không lặp lại ở các lượt khác |

Ngoài ra **mỗi lượt test tải** (50/150/300) đều kết thúc bằng `kill -TERM` → cả ba lần `Da luu xong`, 0 lỗi SQL.
Chú ý: `-XX:+ExitOnOutOfMemoryError` (cờ mới) thoát **không** qua shutdown hook — mất tối đa một chu kỳ lưu tự động.

## 4. Tải và cỡ máy

Máy chủ thử: bản build hiện tại (có mọi sửa đổi ở trên), `-Xms512m -Xmx1g -Xss256k -XX:+UseG1GC`, MariaDB 10.4
riêng (`htbb_db_prod_thu`, buffer pool 512 MB), cùng một máy dev 12 luồng / 23 GB (bot chạy cùng máy, qua
loopback). Bot: `tools/pokemon/kiem_tai/BotTai.java` + bản đo độ trễ (scratch); 10 bot GM đi đánh quái
(`mto` + lệnh 9 + hỏi máu lệnh 7), còn lại đi lại + chat 15–25 s/lần. Mỗi mức **600 s**, khởi động lại máy chủ giữa các mức.
DoS bật như production nên cân nhắc (`socketTimeout=180`, `connRateWindow=30`).

| | 50 bot | 150 bot | 300 bot |
|---|---|---|---|
| Vào game / rớt kết nối | 50 / **0** | 150 / **0** | 300 / **0** |
| Gói server gửi (10 phút) | 165 nghìn (25 MB) | 1,25 triệu (140 MB) | **3,68 triệu** (228 MB ≈ 6.100 gói/s, ~380 KB/s) |
| Đòn đánh / quái hạ | 9.300 / 0 ¹ | 5.360 / 3.357 | 5.374 / 3.334 |
| Exception trong log | 1 ² | 2 ² ³ | 1 ² |
| Lỗi SQL | 0 | 0 | 0 |
| Ngập gói (bỏ bớt, không cắt) | 1 | 67 | 71 — đều là bot GM gửi 400 lệnh 7 liền: bộ chặn hoạt động đúng |
| RSS máy chủ (min–max) | 730–937 MB | 745–1.011 MB | 766–**1.228** MB |
| Thread (max) | 3.118 | 3.393 | 3.813 |
| Kết nối CSDL (max) | 145 | 144 | 155 |
| Heap old cuối mức (Xmx 1 GB) | 80 % | 92 % | **92 %**, GC tổng 0,55 s / 10 phút, 1 full GC (lúc khởi động) |
| CPU máy chủ (trung bình cả đời tiến trình) | ~47 % một lõi | ~57 % | **~82–100 %** một lõi |
| Trễ bắt tay -40 (p50/p95/max) | 1 / 2 / 2 ms | 1 / 2 / 4 ms | 1 / 3 / 11 ms |
| Trễ đăng nhập → danh sách nhân vật | 15 / 16 / 19 ms | 15 / 16 / 19 ms | 15 / **93** / 304 ms |
| Trễ hỏi quái (lệnh 7 → 7) | 15 / 16 / 24 ms | 15 / 16 / 19 ms | 15 / 16 / **395** ms |

¹ Mức 50: GM tự `slv` từ cấp 1 rồi tiến hoá giữa trận và không hạ được quái nào; mức 150/300 (GM đã cấp 25) hạ bình thường. Không có exception — có thể do luật đánh sau tiến hoá/giai đoạn 2; **cần một người thật thử đánh quái bằng client jar/APK trước khi mở** (nhóm QA).
² `[TOP EVENT] loi migrate id_char … Duplicate entry` lúc khởi động (mục 2.2). ³ NPE sự kiện Vạn Tiên Trận (mục 8.2).
Độ phân giải đo của bot là ~15 ms (vòng đọc ngủ 15 ms) — p50 15 ms nghĩa là "dưới một nhịp".

**Nút cổ chai thấy được:**
1. **Thread:** máy chủ tạo **một thread cho mỗi khu của mỗi map** (~2.950 thread khi rỗng) + 2 thread/kết nối. Chi phí
   CPU nền khi **không có ai** ≈ 0,5–0,7 lõi (các vòng lặp khu tự chạy). Đây là giới hạn chính, không phải CSDL.
2. **Heap:** 1 GB đủ cho 300 nhưng old gen ~92 % → nâng `-Xmx2g` (đã sửa `vps/htbb-game.service`).
3. **Đăng nhập dồn:** p95 đăng nhập tăng 15 → 93 ms ở 300 (300 bot vào trong 45 s) — vẫn tốt.
4. CSDL: 0 lỗi, ~140–155 kết nối (chủ yếu pool mở sẵn) → `max_connections=300` (`vps/htbb-mariadb.cnf`).

**Khuyến nghị VPS (Q2):** **4 vCPU / 8 GB RAM / SSD 40 GB+** cho open test tới ~300 CCU (game ~1,2–2 GB, MariaDB ~1 GB,
web Node ~200 MB, dư cho đỉnh và heap dump). Băng thông ~0,4 MB/s (~3 Mbps) ra ở 300 người (bot ít hoạt động hơn người thật — tính
gấp 3: ~10 Mbps). Chắc chắn < 100 CCU: 2 vCPU / 4 GB với `-Xmx1536m`. Trên 300: đo lại; hướng tối ưu là gộp thread khu
(thay đổi lớn, sau open test).

## 5. Bản phát hành client

| Hạng mục | TT | Ghi chú |
|---|---|---|
| Jar + `MAY_CHU_CHINH` | ⚠️ | `MAY_CHU_CHINH=host:19129 sh j2me/build.sh <ban>` ghi `HTBB-May-Chu` vào MANIFEST mọi biến thể (đã kiểm). Build thử OK: `HTBB-v900.jar` 5,3 MB, `-release` 4,6 MB, `-nhe` 2,0 MB. MANIFEST còn `MIDlet-Vendor: TeaM` → đổi. |
| `KiemPokeDuLieu` (jar ↔ server) | ✅ | Exit 0: 18.648 khung khớp từng pixel (4.692 bản nhẹ), hiệu ứng chiêu 0 pixel lệch, bảng hệ 6.156 cặp khớp, ChuPokemon 0/3.336 lệch, tên map 304/304. |
| Android | ✅ / ⚠️ | `KIEU=release BAN=N MAY_CHU=host:19129 sh android/build-pokemon.sh` → APK release (đã thử: debug 5,0 MB, release chưa ký 4,76 MB). **Ký**: script mới `htbb-client/android/ky-ban-phat-hanh.sh` (zipalign + apksigner v2+v3; keystore `$KEY_DIR/$HTBB_KEYSTORE`, mật khẩu qua `HTBB_KS_PASS`/`HTBB_KEY_PASS`; từ chối nếu `KEY_DIR` nằm trong repo git). Lệnh `keytool` tạo khoá ở đầu script. **Chưa tạo khoá thật** — chủ server tạo offline, sao lưu 2 nơi (mất khoá = không cập nhật được APK). |
| PC (Unity) qua CI | ⚠️ | `htbb-client/.github/workflows/phat-hanh.yml` (game-ci/unity-builder@v5, Unity 6000.5.9f1). Secrets: `UNITY_LICENSE`, `UNITY_EMAIL`, `UNITY_PASSWORD` (`UNITY_SERIAL` chỉ khi Pro), `ANDROID_KEYSTORE_B64`, `ANDROID_KEY_PASSWORD`, `NQSH_TAI_TOKEN`. Chạy: Actions → "Phát hành" → Run workflow. ❌ Workflow vẫn là NQSH-H2 (mục 0.3). |
| Tự cập nhật / `phienban.json` | ❌ | Mục 0.1. Manifest `{ban, link, sha256, kichThuoc, ghiChu, ngay}` do `phat-hanh/phienban.py` sinh; tên tệp còn `NQSH-H2-PC.zip/.apk/.jar`. Jar và APK `:pokemon` **không** tự cập nhật (người chơi tải lại từ web). |
| Trang tải web | ⚠️ | `app/pages/download.vue` lấy link từ CMS `/admin/tai-game` (dự phòng `HTBB_LINK_*`, mặc định `#`). Điền link bản HTBB trước khi mở. |
| Đánh số bản + nhật ký thay đổi | ⚠️ | Số bản = số nguyên tăng dần (`build.sh <n>`, `BAN=n`) — dùng **cùng một số** cho jar/APK của một đợt để `sv.banToiThieu` có nghĩa. Chưa có CHANGELOG: dùng mẫu mục 8.3, đăng ở web tin tức + `ghiChu` của `phienban.json`. |

## 6. Pháp lý / tuân thủ

| Hạng mục | TT | Ghi chú |
|---|---|---|
| IP Pokémon (tên loài, "Pokémon", hình ảnh phái sinh) | ⚠️ **rủi ro cao** | The Pokémon Company/Nintendo **chủ động gỡ** fan game công khai (DMCA tới host, GitHub, Discord, cửa hàng ứng dụng), kể cả phi thương mại — đặc biệt khi có tên thật + sprite + có thể tải về + quảng bá rộng. "Phi thương mại" **không** là miễn trừ pháp lý, chỉ giảm động cơ. |
| Giảm thiểu đã có | ✅ | Phi thương mại; nạp tiền tắt (`tat-nap.ts`); trang `dieu-khoan.vue` có tuyên bố không liên kết + "Pokémon © Nintendo/Creatures/GAME FREAK"; `ghi-cong.vue`; client hiện "Game Pokémon fan phi thương mại / Pokémon © Nintendo / Game Freak". |
| Giảm thiểu thêm (Q7) | ⚠️ | (a) Không dùng chữ "Pokémon" trong tên game/tên miền/biểu tượng ứng dụng (đã là "Huyền Thoại Bảo Bối" — giữ); (b) **không** đưa APK lên CH Play/App Store (bị gỡ + khoá tài khoản nhà phát triển); (c) tải từ web riêng, không gắn quảng cáo, không donate; (d) mở test giới hạn (đăng ký/mời) thay vì quảng bá đại trà; (e) phương án an toàn nhất: đổi tên loài + bộ sprite gốc trước khi công khai rộng — tốn công, quyết định của chủ server; (f) chuẩn bị quy trình nhận thư gỡ: một email liên hệ trên web, cam kết gỡ trong 24 giờ. |
| PMDCollab sprite (CC BY-NC 4.0) | ⚠️ | Phải ghi công **trong mọi bản phân phối**: APK ✅ (`GHI_CONG.txt`), web ✅ (`ghi-cong.vue`), **jar ❌**, PC ⚠️ (chỉ nằm trong asset Unity, không có tệp nhìn thấy trong zip). NC = **cấm mọi thu tiền** (kể cả donate đổi quyền lợi) chừng nào còn dùng. |
| Ninja Adventure (CC0), Kenney (CC0), nhạc tự sinh (CC0) | ✅ | Không bắt buộc ghi công; đã ghi ở `GHI_CONG_MAP.txt`, `LICENSE-audio.txt`, `docs/pokemon/GHI_CONG_AM_THANH.md`, `docs/pokemon/NGUON_ASSET_MAP.md`. |
| Chính sách quyền riêng tư / điều khoản | ✅ / ⚠️ | `app/pages/quyen-rieng-tu.vue`, `app/pages/dieu-khoan.vue` có (của nhóm CMS, chưa commit). Thiếu link ở `SiteFooter.vue` (cạnh link `/ghi-cong`, dòng ~70); trang quyền riêng tư ghi "Email (nếu bạn nhập)" trong khi đăng ký **bắt buộc** email → sửa câu. Nên thêm ô "Tôi đồng ý Điều khoản" ở `/register`. |
| Dữ liệu cá nhân tối thiểu | ✅ | Tên tài khoản, băm mật khẩu, email, IP + mã máy (chống gian lận, `tob_may_dang_nhap`), log hành động. Không thu số điện thoại bắt buộc, không thanh toán. Nêu thời gian giữ log (`vps/don-log.sh` mặc định `GIU_NGAY=15` ngày) trong trang quyền riêng tư. |
| Xoá tài khoản | ⚠️ | Trang quyền riêng tư: yêu cầu qua kênh hỗ trợ. Quy trình cho GM: khoá `team_user.ban=1` ngay, xoá nhân vật (`tob_char` theo `userId`) + dòng `team_user` trong 30 ngày, ghi `/admin/nhatky`. Chưa có nút tự xoá trên web. |
| Thông báo độ tuổi | ❌ | Chưa có ở đâu. Thêm vào điều khoản + trang đăng ký: "Dành cho người từ 13 tuổi; dưới 16 tuổi cần cha mẹ đồng ý" (Nghị định 13/2023 về dữ liệu cá nhân trẻ em). |

## 7. Hỗ trợ người chơi

| Hạng mục | TT | Ghi chú |
|---|---|---|
| Kênh báo lỗi trong game | ⚠️ | Client **không có** nút báo lỗi. Dùng kênh cộng đồng (Q9) + mẫu báo lỗi trong `SO_TAY_GM.md` mục 6. Server đã log `[CLIENT] <tk> ip platf version ban=<số bản>` mỗi lần đăng nhập → đủ tra theo giờ + tên. |
| Kháng cáo khoá | ✅ | Quy trình ở `SO_TAY_GM.md` mục 5. |
| Sổ tay GM | ✅ **mới** | `docs/pokemon/SO_TAY_GM.md`: quyền GM hoạt động thế nào, lệnh được/cấm dùng trong open test, mức xử lý, kháng cáo, bảo trì. |
| Thông báo mở test | ✅ | Mẫu mục 8.1. |
| Danh sách lỗi đã biết | ✅ | Mục 8.2 — cập nhật hằng ngày. |
| Chính sách wipe | ✅ **mới** | Wipe nhân vật sau open test, **giữ tài khoản**. Script mới `sql/wipe_sau_open_test.sql` (chỉ **in** lệnh TRUNCATE để đọc lại; giữ `data_%`, `web_%`, `cau_hinh`, `tob_su_kien`, giftcode, `5h_active`, khoá máy). Đã chạy thử khô trên bản sao: 86 bảng người chơi. ⚠️ **Đừng dùng `sql/wipe_player_data.sql` cũ**: danh sách giữ lại cố định 16 bảng nên sẽ xoá sạch `cau_hinh`, `tob_su_kien`, toàn bộ CMS `web_*` và bảng nội dung giai đoạn 2 không bắt đầu bằng tên trong danh sách. |

## 8. Mẫu

### 8.1 Thông báo mở test

> **Huyền Thoại Bảo Bối — Open Test từ <ngày giờ>**
>
> Huyền Thoại Bảo Bối là game fan **phi thương mại**, không liên kết và không được bảo trợ bởi Nintendo,
> Creatures, GAME FREAK hay The Pokémon Company. Không có nạp tiền, không quảng cáo.
>
> - Tải game: <link trang tải> (jar cho điện thoại Java, APK Android, bản PC Windows). Chỉ tải từ trang này.
> - Đăng ký: <link> — từ 13 tuổi; dưới 16 tuổi cần cha mẹ đồng ý. Không dùng lại mật khẩu ở nơi khác.
> - Thời gian test: <từ> đến <đến>. **Sau open test toàn bộ nhân vật sẽ bị xoá (wipe)**, tài khoản được giữ.
> - Báo lỗi / hỗ trợ: <kênh> — gửi tên nhân vật, giờ, bản đồ, nền tảng + số bản, ảnh/clip.
> - Lỗi đã biết: <link mục tin tức>. Bảo trì được báo trước ≥ 30 phút.
> - Dùng tool/hack, lợi dụng lỗi, mạo danh GM: khoá tài khoản (xem Điều khoản).

### 8.2 Lỗi đã biết (bắt đầu — cập nhật trong open test)

| Lỗi | Nơi | Tình trạng |
|---|---|---|
| `NullPointerException` ở `BossMeCung.sendMove → MapVanTienTran.getAllPlayer` khi sự kiện Vạn Tiên Trận bắt đầu (log máy chủ thử 22:11) | server, sự kiện | Bị bắt, không sập; cần sửa (nhóm sự kiện) |
| `tob_top_event` có dòng trùng / migrate `idx_id_char` lỗi lúc khởi động | server, bảng xếp hạng sự kiện | Mục 2.2 |
| `pokemon_lau_dai.sql` lỗi strict trên MariaDB | SQL | Mục 2.2 — chặn nếu bật `sv.lau_dai=1` |
| Còn 2 chuỗi chữ HOA có dấu — font jar không vẽ được | nội dung | Test "Chu HOA co dau" đang hỏng |
| Client PC không có số bản trong gói đăng nhập | client | `sv.banToiThieu` không áp cho PC |
| Lệnh GM `ban` không có tác dụng | server | Khoá qua web |

### 8.3 Nhật ký phiên bản (mẫu)

```
## Bản <N> — <yyyy-mm-dd>   (jar/APK cùng số N; PC <x.y>)
Máy chủ: <host:cổng>   Bắt buộc cập nhật: có/không (sv.banToiThieu=<N>)
### Mới
### Sửa lỗi
### Cân bằng
### Lỗi còn biết
```

## 9. Kiểm thử hồi quy

| Kiểm | Kết quả |
|---|---|
| Bộ test server (`test/`, Linux JDK 21 `--release 8`) | ✅ **65/7.601 hỏng, 14 nhóm = đúng mức nền 65/14, +0 mới** (sau khi thêm 3 khoá mới vào `vps/server.ini.tmpl` + `docs/cau-hinh-server-ini.md`; trước đó test pipeline bắt được thiếu khoá → +1, đã sửa) |
| `KiemPokeDuLieu` (jar) | ✅ exit 0 (mục 5) |
| Web `npm run build` | ✅ với Node 22.22.2 (ra thư mục tạm, không đè `.output`); ❌ với Node 20.20.0 (mục 1.3) |
| Android `sh android/build-pokemon.sh` | ✅ debug 5,0 MB, release chưa ký 4,76 MB |
| j2me `build.sh` | ✅ (bản chụp có cả thay đổi chưa commit của nhóm QA J2ME) |

## 10. Chủ server phải làm (theo thứ tự)

1. Quyết định Q1–Q11 (mục 0).
2. VPS: MariaDB + `sudo cp vps/htbb-mariadb.cnf /etc/mysql/mariadb.conf.d/90-htbb.cnf`; nạp `sql/htbb_day_du.sql`
   (sau khi nhóm GD2 sửa `pokemon_lau_dai.sql` và sinh lại); `mysql htbb_db < sql/them_chi_muc_production.sql`.
3. `loginServer/server.ini` từ `vps/loginServer.server.ini.example` (có `characterEncoding=UTF-8`).
4. GitHub Secrets cho `server.ini.tmpl` (đã có trong pipeline); đặt `sv.website`, `sv.linkTaiGame` thật; xem lại
   `sv.socketTimeout`/`sv.connRateWindow` (Q4).
5. systemd: `htbb-game`, `htbb-login`, `htbb-web`; logrotate; cron root:
   ```
   0 4 * * *  /opt/htbb/vps/backup_db.sh >> /opt/htbb/log/backup.log 2>&1
   30 4 * * 1 /opt/htbb/vps/kiem-khoi-phuc-csdl.sh >> /opt/htbb/log/backup.log 2>&1
   * * * * *  TU_KHOI_DONG_LAI=1 WEB_URL=http://127.0.0.1:3000 /opt/htbb/vps/kiem-suc-khoe.sh >> /opt/htbb/log/suc-khoe.log 2>&1
   ```
   và `/etc/default/htbb-backup` với `HTBB_BACKUP_DICH=` (bản sao ngoài máy).
6. Web: Node 22, `HTBB_SECRET` (64 hex), `HTBB_ADMIN_2FA_BAT_BUOC=1`, `HTBB_UPLOAD_DIR` + `ReadWritePaths`,
   `HTBB_NAP_BAT` để tắt; điền link tải ở `/admin/tai-game`; thêm link Điều khoản/Quyền riêng tư + thông báo tuổi.
7. Client: tạo repo phát hành HTBB, sửa `CapNhat.cs` (hoặc tắt tự cập nhật PC), `phienban.py`/`day-len.sh`/
   `phat-hanh.yml` (tên `HTBB*`, Android qua `build-pokemon.sh`, `MAY_CHU_CHINH`); tạo keystore Android offline;
   thêm `GHI_CONG.txt` vào jar; đổi `MIDlet-Vendor`.
8. Nhân vật GM: tạo (bật tạm `sv.choPhepTaoTenGM=1`), đặt `pInfo[16]=1`, bật `sv.gmCanCoPInfo=1`; phát `SO_TAY_GM.md`.
9. Chạy thử 1 giờ với người thật (5–10 người, cả jar/APK/PC) trước khi công bố; theo dõi `tob_chi_so_may_chu`.
10. Đăng thông báo (8.1), ghim lỗi đã biết (8.2).
