# vps/ — tệp triển khai máy chủ HTBB

Trên VPS, `/opt/htbb` là bản checkout monorepo này (người dùng hệ thống `htbb`). Hướng dẫn đầy đủ:
[docs/trien-khai.md](../docs/trien-khai.md).

## Bố cục trên VPS

| Đường dẫn | Là gì |
|---|---|
| `/opt/htbb/server` | game server (`htbb-server.jar`, `server.ini` sinh từ `server.ini.tmpl`), `loginServer/` |
| `/opt/htbb/web` | website Nuxt; `.output` → `releases/<sha>/.output`, ảnh CMS ở `uploads/` |
| `/opt/htbb/downloads` | bản tải game, nginx phát ở `/downloads/` |
| `/opt/htbb/logs` | `game.out`, `login.out`, `gc.log`, log các script (`/opt/htbb/log` là symlink tương thích) |
| `/opt/htbb/backups` | bản sao lưu CSDL `*.sql.gz` (root, 0750), `server-ini/` (10 bản `server.ini` cũ) |
| `/opt/htbb-staging` | staging (`server/`, `sql/`, `logs/`) — chỉ chạy khi cổng kiểm chạy, cổng 29129 / 28023 chỉ localhost |
| `/var/lib/htbb/cong-kiem` | kết quả cổng kiểm đã ký + nhật ký 30 lần gần nhất (root 0700) |
| `/etc/htbb/staging.env`, `/etc/htbb/cong-kiem.key` | mật khẩu user MariaDB `htbb_staging`, khoá ký kết quả cổng kiểm (root 0600, `staging/cai-dat.sh` sinh) |
| `/etc/htbb/htbb.env` | bí mật + cấu hình (root, 0600), nạp bằng `EnvironmentFile` của `htbb-web` và `htbb-game` |

## Tệp trong thư mục này

| Tệp | Cài vào / dùng để |
|---|---|
| `htbb-game.service` | `/etc/systemd/system/` — game, TCP 19129, heap 1.5 GB |
| `htbb-login.service` | `/etc/systemd/system/` — login server, TCP 8023 chỉ nội bộ |
| `../web/deploy/htbb-web.service` | `/etc/systemd/system/` — website, 127.0.0.1:3000 |
| `htbb-nginx.conf` | `/etc/nginx/sites-available/htbb` — nginx sau WAF, dải IP WAF khai riêng trong `/etc/nginx/snippets/htbb-waf-ip.conf` |
| `htbb-mariadb.cnf` | `/etc/mysql/mariadb.conf.d/99-htbb.cnf` — MariaDB 127.0.0.1:3306, utf8, buffer pool 768M |
| `htbb-logrotate.conf` | `/etc/logrotate.d/htbb` — xoay vòng log |
| `backup_db.sh` | `/usr/local/sbin/htbb-backup-db.sh` + `/etc/cron.d/htbb-backup` (03:30) — dump `htbb_db` + `account` ra `backups/<csdl>_<YYYY-MM-DD_HHMM>.sql.gz` (một CSDL, không `USE` — staging nạp lại được), giữ 14 ngày |
| `kiem-khoi-phuc-csdl.sh` | cron hằng tuần — nạp thử bản sao lưu mới nhất vào CSDL tạm rồi so số dòng |
| `server.ini.tmpl` + `sinh-server-ini.sh` | nguồn DUY NHẤT của `server/server.ini`; deploy sinh lại, thử kết nối CSDL trước khi cài |
| `deploy-auto.sh` | GitHub Actions gọi sau mỗi push: kéo code, sinh cấu hình, áp dụng vào lần bảo trì kế tiếp |
| `update.sh` / `capnhat.sh` | cập nhật tay: kéo git rồi khởi động lại / thay một jar chép tay |
| `dieu-khien-server.sh` | cron root mỗi phút — bật/tắt game theo lệnh từ web admin |
| `kiem-suc-khoe.sh` | cron root mỗi phút — kiểm cổng game, `trang_thai.json`, login, CSDL, web |
| `don-log.sh` | cron 03:20 — dọn bảng nhật ký CSDL và tệp `.txt` lẻ |
| `server.ini.linux.example`, `loginServer.server.ini.example` | mẫu tạo tay (máy không dùng pipeline) |
| `doi_ten_nhan_vat.sql` | SQL đổi tên nhân vật |
| `nang_cap_csdl.sh` | nâng cấp CSDL đang chạy lên bản hiện tại: sao lưu → nạp `sql/htbb.sql` → kiểm, chạy lại được (`sudo bash /opt/htbb/vps/nang_cap_csdl.sh`); `HTBB_SQL` / `HTBB_CSDL_GAME` cho tệp ứng viên / CSDL staging |
| `xuat_csdl_day_du.sh` | sinh lại phần A (schema mọi bảng + dữ liệu thiết kế, bảng người chơi rỗng) của `sql/htbb.sql` từ CSDL đang chạy, giữ nguyên phần B, để chép về `sql/` (`sudo bash /opt/htbb/vps/xuat_csdl_day_du.sh /tmp/htbb.sql`) |
| `van_hanh/` | công cụ SQL thao tác tay (không thuộc `sql/htbb.sql`): xoá dữ liệu người chơi (`wipe_player_data.sql`, `wipe_sau_open_test.sql`), thu hồi quà (`thu_hoi_qua_tao_nv.sql`) — **tắt server và sao lưu trước**, không hoàn tác được |
| `len-production.sh` | **cách duy nhất** thay game production (jar / SQL / web): kiểm cổng kiểm (sha256 jar + sql, chữ ký, ≤ 24 giờ) → sao lưu → báo 62 giây → tắt → `nang_cap_csdl.sh` → jar → bật → sức khoẻ → tóm tắt; tự quay lại jar cũ nếu không lên; cấm thứ Hai 04:00–05:00 trừ `--force` |
| `staging/` | staging cùng VPS + cổng kiểm — [docs/trien-khai.md mục 4.4](../docs/trien-khai.md): `cai-dat.sh` (một lần: thư mục, bí mật, user MariaDB `htbb_staging`, unit, ufw), `lam-moi.sh` (CSDL từ bản sao lưu mới nhất + SQL + jar ứng viên), `sinh-server-ini.sh`, `kiem-truoc-khi-len.sh` (cổng kiểm: bot hồi quy + log sạch → kết quả ký ở `/var/lib/htbb/cong-kiem/`), `htbb-staging-{game,login}.service`, `loi-bo-qua.txt`, `chung.sh` |
| `theo-doi/bao-cao-ngay.sh` | cron 00:10 — báo cáo bất thường production theo ngày (`/opt/htbb/logs/bao_cao/<ngày>.txt`): Exception, nạp map lặp, kéo vị trí, chặn thao tác, đăng nhập lỗi, GC > 200 ms, nhịp > 100 ms |
| `win/` | tệp `.bat` xem log, chạy SQL, tải CSDL từ Windows qua SSH — [win/README.md](win/README.md) |

## Quy trình triển khai

**Từ 24/10/2026 game server chỉ lên production qua cổng kiểm** (docs/trien-khai.md mục 4.4):
`staging/kiem-truoc-khi-len.sh --jar … --sql …` (bot hồi quy trên staging) rồi `len-production.sh --chi-jar | --co-sql`.
Khi đã cài cổng kiểm, `deploy-auto.sh` / `update.sh` / `capnhat.sh` dừng ngay (`HTBB_BO_CONG_KIEM=1` để bỏ qua).
Đoạn dưới mô tả đường cũ (vẫn dùng cho VPS chưa cài cổng kiểm và cho website).


Dựng VPS một lần (Ubuntu 20.04, OpenJDK 17, Node 22, MariaDB, nginx, ufw mở 22/80/443/19129 và 19130 cho Nova agent):
tạo user `htbb` và các thư mục ở trên, cài `htbb-mariadb.cnf` **trước** khi nạp schema, sinh `/etc/htbb/htbb.env`
(mật khẩu CSDL ngẫu nhiên, user `htbb` có toàn quyền trên `htbb_db` và `account`), checkout repo vào `/opt/htbb`,
cài ba unit systemd, nginx, logrotate và cron sao lưu, rồi `web/deploy/cai-dat-vps.sh` cho khoá deploy web.
Sau đó mỗi push lên `main`: GitHub Actions gọi `deploy-auto.sh` (game: kéo code + sinh `server.ini`, khởi động lại
vào giờ bảo trì hoặc ngay khi `DEPLOY_NOW=1`) và `htbb-deploy-web.sh` (web: nhận bản build `.output`, đổi symlink,
khởi động lại, tự quay lui nếu `/api/site` không trả 200).

Người chơi vào game qua WAF / Nova agent. Danh sách proxy tin cậy (`sv.proxyTinCay`, mặc định `127.0.0.1,::1`)
được miễn giới hạn theo IP; nếu máy chủ chỉ thấy IP của WAF thì thêm dải WAF ở `/admin` → Cấu hình hệ thống
(áp dụng ngay), không ghi IP WAF vào Git.
