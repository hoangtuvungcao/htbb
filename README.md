# Huyền Thoại Bảo Bối (HTBB)

Game nhập vai trực tuyến 2D, người chơi **là một Pokémon**: đi khắp thế giới, đánh quái, tiến hoá, thách đấu Gym,
chiếm Gym, làm nhiệm vụ cốt truyện, tham gia sự kiện và hệ sinh thái (câu cá, vườn, nấu ăn, trứng…). Game được chuyển
đổi từ một game nhập vai kiếm hiệp cũ (máy chủ Java + client J2ME / Unity) sang thế giới Pokémon hoàn toàn. Đây là
**game fan phi thương mại** — xem [docs/giay-phep.md](docs/giay-phep.md).

Monorepo gồm máy chủ, bốn client, website và công cụ. Website: `https://huyenthoaibaoboi.fun/`.

## Thư mục

| Thư mục | Nội dung | Tài liệu |
|---|---|---|
| `server/` | game server + login server (Java, cổng 19129 / 8023): `src/`, `test/`, `lib/`, `map/`, `cMap/`, `htbb-server.jar` | [server/README.md](server/README.md) |
| `client_jar/` | client J2ME (MIDP 2.0) — **nguồn code game duy nhất** cho jar, Android và PC Java | [client_jar/README.md](client_jar/README.md) |
| `client_apk/` | Android: module `pokemon/` sinh từ code jar qua `port-j2me.py` | [client_apk/README.md](client_apk/README.md) |
| `client_pc/` | bản PC Java SE chạy code jar (Windows / macOS / Linux) | [client_pc/README.md](client_pc/README.md) |
| `client_pc_ios/` | client Unity 6000.5.9f1 (PC Windows, iOS) | [client_pc_ios/README.md](client_pc_ios/README.md) |
| `web/` | website + khu quản trị Nuxt 4 (Node 22) | [web/README.md](web/README.md) |
| `sql/` | MỘT tệp CSDL `htbb.sql` (bản chụp CSDL — dữ liệu game sửa trong CSDL — cho CSDL mới, kèm phần nâng cấp CSDL đang chạy; chạy lại được) | [sql/README.md](sql/README.md) |
| `du_lieu/` | bảng tài nguyên client / bản đồ (sprite, tile, NPC) mà mã đọc trực tiếp | [du_lieu/README.md](du_lieu/README.md) |
| `tools/` | công cụ tài nguyên, bot kiểm thử, phát hành client; `luu_tru_sinh_du_lieu/`: bộ sinh dữ liệu cũ (lưu trữ) | [tools/README.md](tools/README.md) |
| `vps/` | tệp triển khai VPS: systemd, nginx, MariaDB, deploy, sao lưu | [vps/README.md](vps/README.md) |
| `docs/` | tài liệu dự án (bên dưới) | |

## Tài liệu

| Tệp | Nội dung |
|---|---|
| [docs/kien-truc.md](docs/kien-truc.md) | kiến trúc: thành phần, giao thức, CSDL, web ↔ game, cấu hình trung tâm, luồng dữ liệu, bản đồ, quy ước mã |
| [docs/build.md](docs/build.md) | build và kiểm thử từng thành phần, phát hành client |
| [docs/trien-khai.md](docs/trien-khai.md) | dựng VPS và deploy (MariaDB, systemd, nginx, WAF, sao lưu) |
| [docs/van-hanh.md](docs/van-hanh.md) | vận hành: dịch vụ, log, sao lưu, bảo trì, sổ tay GM, sự kiện, giftcode, nạp tiền, email |
| [docs/dang-nhap.md](docs/dang-nhap.md) | login server: giao thức game ↔ login, mã kết quả, cấu hình, số đo cũ/mới, lên bản và quay lui |
| [docs/cau-hinh.md](docs/cau-hinh.md) | mọi khoá cấu hình (`server.ini` + bảng `cau_hinh`), một phần sinh tự động |
| [docs/thiet-ke-game.md](docs/thiet-ke-game.md) | thiết kế game: Pokémon, hệ, chiêu, Gym, kinh tế, hệ sinh thái, thuật ngữ |
| [docs/giay-phep.md](docs/giay-phep.md) | giấy phép và ghi công tài nguyên |

## Bắt đầu nhanh (máy dev)

Cần: JDK 17+ (biên dịch `--release 8`), MariaDB 10.4+ (không dùng MySQL 8), Node 22, python3.

```sh
# 1. CSDL (đặt lower_case_table_names=1 và utf8 trước, xem vps/htbb-mariadb.cnf)
mysql -u root < sql/htbb.sql

# 2. Cấu hình server: chép mẫu rồi sửa db.* (máy nhà có thể đặt db.choPhepMatKhauTrong=1)
cp vps/server.ini.linux.example server/server.ini
cp vps/loginServer.server.ini.example server/loginServer/server.ini

# 3. Login server rồi game server (chạy ở server/)
(cd server/loginServer && java -Djava.awt.headless=true -jar htbb-login.jar) &   # jar cũ: CheckLoginSocket.jar
(cd server && java -Djava.awt.headless=true -jar htbb-server.jar)

# 4. Website
cd web && cp .env.example .env && npm ci && npm run dev        # http://localhost:3000

# 5. Một client nối vào máy dev
MAY_CHU=127.0.0.1:19129 sh client_jar/build.sh                  # jar (chạy bằng MicroEmulator)
sh client_pc/build.sh && java -jar client_pc/out/HTBB-PC.jar    # PC Java, mặc định 127.0.0.1:19129
```

Không có MariaDB: `sh tools/pokemon/chay_tren_novadb.sh` chạy toàn bộ server trên NovaDB. Kiểm thử server:
[docs/build.md](docs/build.md).

## Quy ước

- Đường dẫn trong tài liệu và công cụ viết **từ gốc repo** (`server/src/...`, `du_lieu/...`); công cụ chạy ở gốc repo,
  test server chạy ở `server/` (đọc `../docs`, `../sql`, `../vps`…).
- Tài liệu và chữ người chơi thấy: tiếng Việt có dấu; tên định danh trong mã: tiếng Việt không dấu.
- Không đưa mật khẩu, khoá, `server.ini`, `.env` hay địa chỉ IP máy chủ vào git.
- Workflow GitHub Actions ở `.github/workflows/` có tiền tố thành phần (`server-`, `web-`, `client-`) và lọc theo đường
  dẫn; `server-deploy.yml` chỉ chạy khi biến repo `HTBB_VPS_MONOREPO=1`.
