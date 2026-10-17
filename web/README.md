# HTBB-Web — Website Huyền Thoại Bảo Bối (trước đây Khí Phách Anh Hùng)

Website (trang công khai + khu quản trị) của máy chủ private HTBB, viết bằng **Vue 3**:

| Thành phần | Dùng gì |
|---|---|
| Framework | **Nuxt 4** (Vue 3.5, TypeScript) — trang + API server (Nitro) trong một dự án |
| Giao diện | **Nuxt UI v4** (Tailwind CSS v4, Reka UI) — phong cách game Pokémon: đỏ Poké Ball / vàng Pikachu / xanh dương, nền trời + cỏ, 18 màu hệ (token ở `app/assets/css/main.css`), font Nunito + Baloo 2 (có tiếng Việt) |
| Hero | CSS/SVG thuần (không WebGL): trời, mây, bãi cỏ, 5 Pokémon khởi đầu chạy sprite PMD thật (`PokeSprite`, dải khung sinh bằng `node scripts/sinh-anim-khoi-dau.mjs` vào `public/pokemon/anim/`) |
| Hiệu ứng | **motion-v** (hiện dần khi cuộn), thẻ nghiêng 3D theo chuột (`FxTiltCard`) |
| Database | `mysql2` — dùng **chung** MariaDB với game server (DB `account` + `kpah_db`) |

Website này thay cho website cũ trong `HTBB/web/`; website cũ vẫn chạy cho tới khi
bạn bật proxy Apache sang đây (xem [Bật website trên VPS](#bật-website-trên-vps)).

## Nhánh Pokémon (`pokemon/chuyen-doi`)

Trên nhánh này mọi nhân vật người chơi là một Pokémon (kế hoạch:
`HTBB/docs/pokemon/KE_HOACH_CHUYEN_DOI.md`, việc W1 + W3). Phía web:

- **Tên loài thay tên nghề.** `tob_char.class` (0..4) giữ nguyên, nay là dòng khởi đầu:
  0 Charmander, 1 Bulbasaur, 2 Eevee, 3 Squirtle, 4 Pichu. Loài hiện tại tính bằng
  `speciesName(class, cấp, dex)` / `dexHienTai()` trong
  [shared/utils/pokemon.ts](shared/utils/pokemon.ts) — bản TS của `LoaiPokemon.java` ở repo
  game, sửa bên kia thì sửa cả ở đây.
- **Cần migration trước khi deploy.** Xếp hạng, Tài khoản và các trang admin (Nhân vật, chi
  tiết Tài khoản, Tổng quan) `SELECT` cột mới `tob_char.dex` (smallint, 0 = suy ra từ class +
  cấp). Chạy `HTBB/sql/them_cot_pokemon.sql` trên CSDL game **trước**; chưa có cột thì các
  bảng đó báo "chưa tải được dữ liệu" / lỗi.
- **Chân dung** 40×40 của 21 loài người chơi ở `public/pokemon/<dex>.png`, chép từ
  `HTBB/ClientUnity/Assets/Resources/poke/<dddd>_cd.bytes` (thực chất là PNG). Hiển thị với
  class `.pixelated` (`image-rendering: pixelated`).
- **Trang ghi công `/ghi-cong`** (bắt buộc theo CC BY-NC 4.0) đọc
  `public/pokemon/GHI_CONG.txt` lúc build. Mỗi lần client đóng gói lại sprite thì chép lại
  file `HTBB/ClientUnity/Assets/Resources/poke/GHI_CONG.txt` sang rồi build.
- **Tắt nạp tiền: `KPAH_NAP_BAT`** (mặc định `false`). Quyết định Q10: asset PMDCollab là
  CC BY-NC 4.0 và Pokémon là IP của Nintendo, nên không được thu tiền. Khi tắt: menu
  "Nạp mốc" ẩn, trang `/naptien` và `/xac-nhan-nap` báo 403, mọi API `/api/nap/**` và
  `/api/xac-nhan-nap/**` (kể cả webhook SePay) trả **403** với thông báo tiếng Việt
  ([server/middleware/tat-nap.ts](server/middleware/tat-nap.ts)). Code nạp không bị xoá;
  `/admin/naptien` vẫn mở để xem đơn cũ. Đặt `KPAH_NAP_BAT=true` để bật lại — lưu ý deploy
  bỏ qua biến rỗng, nên muốn tắt lại phải đặt hẳn `false`.
- **Tên game/website: "Huyền Thoại Bảo Bối"** (viết tắt **HTBB**, không dấu "Huyen Thoai Bao Boi"),
  thay cho "Khí Phách Anh Hùng" / tên tạm "KPAH Pokémon" trước đây. **Nguồn duy nhất:**
  [shared/utils/tenSite.ts](shared/utils/tenSite.ts) (`TEN_SITE`, `TEN_SITE_VIET_TAT`, `TEN_SITE_ASCII`).
  Giao diện đọc qua `useAppConfig().htbb.tenSite` / `.tenVietTat` ([app/app.config.ts](app/app.config.ts)):
  tiêu đề tab, meta description, `og:site_name`, đầu/chân trang, tin tức. Server đọc qua
  `useHtbbConfig().site.name` ([server/utils/config.ts](server/utils/config.ts)): API `/api/site`, tiêu đề
  và nội dung email (`server/api/auth/*.post.ts`), tên người gửi mặc định (`KPAH_MAIL_FROM_NAME`).
  Nhãn 2FA admin là "HTBB Admin". Chữ trong [public/logo.svg](public/logo.svg) là ảnh tĩnh, đổi tên
  thì sửa tay. Khoá trong app config là `htbb`. Hạ tầng giữ tên cũ: biến môi trường `KPAH_*` (GitHub Secrets/VPS), CSDL `kpah_db`, service `kpah-web`, thư mục `/opt/kpah-web`.
- **Thuật ngữ** trên giao diện: nhân vật → Pokémon, bang hội → Đội, linh thú → thú cưỡi.

## Trang công khai & cẩm nang (dữ liệu sinh tự động)

| Đường dẫn | Nội dung | Dữ liệu |
|---|---|---|
| `/` | Hero, cảnh "trailer" sprite (`SiteCanhPhim`), tính năng, 5 khởi đầu, sự kiện tuần này, tin mới, trạng thái máy chủ (`/api/trang-thai`) | `pokedex.json`, `cam-nang.json`, `shared/utils/news.ts` |
| `/gioi-thieu` | Thế giới, vùng, điểm độc đáo, cung truyện năm 1 | `cam-nang.json` |
| `/huong-dan` | Tân thủ: tải/cài, tài khoản, chọn khởi đầu, điều khiển, HUD minh hoạ (`SiteHudMinhHoa`), 30 phút đầu | cả hai JSON |
| `/cam-nang`, `/cam-nang/<slug>` | Wiki 18 trang có thanh bên, tìm kiếm, trang trước/sau (`WikiShell`). Danh mục trang: `DS_CAM_NANG` trong [shared/utils/camNang.ts](shared/utils/camNang.ts) | cả hai JSON |
| `/pokedex` | Lọc theo tên/số, hệ, giai đoạn tiến hoá, vai trò, sắp xếp; sprite động (`PokeSan`) | `pokedex.json`, `sprite-anim.json` |
| `/dieu-khoan`, `/quyen-rieng-tu`, `/ghi-cong` | Pháp lý, tuyên bố fan phi thương mại, ghi công PMDCollab / Kenney / Ninja Adventure | — |
| `/sitemap.xml`, `/robots.txt` | Sinh động trong `server/routes/` | danh mục trang |

Tìm kiếm toàn site (Ctrl/⌘+K hoặc `/`) chạy hoàn toàn trên trình duyệt (`SiteTimKiem`), không gọi server, không theo dõi.

**Đồng bộ lại dữ liệu từ repo game** (chỉ đọc repo HTBB, ghi vào repo web):

```bash
HTBB_DIR=../HTBB npm run dong-bo-du-lieu
#  = sinh-du-lieu-pokemon.mjs  -> shared/data/pokedex.json + public/pokemon/<dex>.png (chân dung)
#  + sinh-anim-khoi-dau.mjs    -> public/pokemon/anim/<dex>[s]-idle.png (mọi loài), walk/walkR/danhR (5 khởi đầu)
#                                 + shared/data/sprite-anim.json (khung, thời lượng, điểm chân)
#  + dong-bo-cam-nang.mjs      -> shared/data/cam-nang.json (bang_cap_do_lau_dai, nhiem_vu_moi, su_kien_nam,
#                                 thiet_ke_map, bang_vat_pham .csv + ThongSoLauDai.java + mo_phong_tom_tat.txt)
node scripts/dong-bo-cam-nang.mjs --anh-map   # thêm: chép ảnh xem trước docs/pokemon/map_moi/<id>.png -> public/ban-do/
npm run sinh-anh-og                           # ảnh chia sẻ public/og/htbb.png (cần google-chrome)
```

Không sửa tay các file JSON trên — đổi số bên game rồi chạy lại. Hình Pokémon trên thẻ/Pokédex dùng
**sprite trong suốt** (`PokeSan`/`PokeSprite`, phóng nguyên lần, đặt chân lên bóng theo điểm chân của PMD);
chân dung 40×40 (có nền vẽ sẵn) chỉ dùng làm ảnh đại diện tròn nhỏ (`PokeAvatar`).

## Khu quản trị nội dung (CMS) & phân quyền

**Cài một lần:** chạy `server/sql/web_cms.sql` (bản sao `HTBB/sql/web_cms.sql`) vào CSDL game `kpah_db`
(MariaDB hoặc NovaDB — SQL viết để chạy cả hai). Chưa chạy thì website vẫn hiện nội dung mặc định;
trang admin báo "Chưa có bảng". Bảng: `web_cai_dat` (khối JSON: site/trangChu/menu/taiGame),
`web_tin_tuc`, `web_trang`, `web_faq`, `web_media`, `web_su_kien_lich`, `web_admin_vai_tro`,
`web_khoa_tai_khoan`, `web_admin_thay_doi`.

**Admin đầu tiên:** tài khoản game bình thường (đăng ký trên web hoặc trong game), rồi đặt
`KPAH_ADMIN_ACCOUNTS=ten_tai_khoan` (nhiều tên cách dấu phẩy) và khởi động lại web — người trong
danh sách này luôn là **Siêu quản trị**. Từ đó cấp vai trò cho người khác ở `/admin/phan-quyen`:

| Vai trò | Được làm |
|---|---|
| Siêu quản trị | Mọi thứ (server, cấu hình, giftcode, reset, phân quyền, đồng bộ dữ liệu) |
| Biên tập | Tin tức/sự kiện, trang cẩm nang, trang chủ, FAQ, menu, ảnh, lịch sự kiện, cài đặt website |
| Hỗ trợ | Trung tâm người chơi: khoá có hạn/mở, cấm chat (khi offline), gửi thư, đổi mật khẩu |
| Chỉ xem | Xem mọi trang, không sửa |

Quyền kiểm ở server cho MỌI `/api/admin/**` (`server/utils/vaiTro.ts`, gọi trong `defineAdminHandler`);
menu chỉ ẩn bớt cho gọn. Token admin đi qua header (không cookie) + chặn khác nguồn + chỉ nhận
JSON/multipart (`server/middleware/admin-csrf.ts`); đăng nhập có giới hạn gõ sai; Markdown được lọc
(HTML thô bị escape, link/ảnh chỉ http(s)/đường dẫn nội bộ — `shared/utils/markdown.ts`); ảnh tải
lên kiểm chữ ký byte (PNG/JPEG/GIF/WebP ≤ 5 MB, không SVG), lưu ở `KPAH_UPLOAD_DIR`
(mặc định `./uploads`), phục vụ qua `/uploads/<tên>`. Mọi thao tác ghi `web_admin_log`; sửa nội dung
ghi thêm bản trước/sau ở `web_admin_thay_doi` (xem `/admin/lich-su-thay-doi`).

Trang: `/admin` (tổng quan + nội dung), `/admin/tin-tuc` (soạn Markdown + xem trước, nháp/đăng/hẹn giờ,
ghim), `/admin/trang` (trang cẩm nang mới hoặc tuỳ chỉnh trang có sẵn), `/admin/trang-chu`,
`/admin/lich-su-kien`, `/admin/faq`, `/admin/tai-game`, `/admin/menu`, `/admin/media`, `/admin/cai-dat`
(bảo trì, thanh thông báo, SEO/OG, mạng xã hội, nguồn trạng thái máy chủ), `/admin/nguoi-choi`,
`/admin/bang-game` (kết quả trong game, chỉ đọc), `/admin/dong-bo` (chạy script đồng bộ dữ liệu game —
cần `HTBB_DIR` + `KPAH_WEB_SRC_DIR`; số liệu đóng gói cần build lại).

## Chạy thử trên máy

Cần **Node ≥ 20** và MariaDB (XAMPP được).

```bash
npm install
cp .env.example .env      # rồi sửa .env (xem bên dưới)
npm run dev               # http://localhost:3000
```

`.env` tối thiểu khi dev:

```bash
KPAH_SERVER_DIR=C:/Users/ADMIN/Desktop/KPAH-All/HTBB   # để tự đọc server.ini
KPAH_SECRET=chuoi-ngau-nhien-bat-ky
KPAH_ADMIN_ACCOUNTS=admin
```

Lệnh khác: `npm run build` (bản chạy thật ở `.output/`), `npm run typecheck`.

`node scripts/dev-token.mjs admin admin 1` in ra một token admin để thử API bằng
`curl -H "Authorization: Bearer <token>"` — chỉ chạy được khi có `KPAH_SECRET` trong `.env`.

## Cấu hình — biến môi trường `KPAH_*`

Danh sách đầy đủ và giải thích ở [.env.example](.env.example). Thứ tự ưu tiên:

1. biến môi trường `KPAH_*` — khai thì luôn thắng
2. `server.ini` + `loginServer/server.ini` trong `KPAH_SERVER_DIR` — nguồn thật, tự dò
3. mặc định cho máy dev (`root`, không mật khẩu)

Nên thông tin database **thường không phải khai**: đổi mật khẩu DB ở `server.ini`
là web theo ngay.

**Trên VPS, không ai sửa tay biến môi trường.** GitHub Actions ghi chúng vào
`/etc/kpah-web.env` ở mỗi lần deploy (xem mục dưới).

## Deploy lên VPS (GitHub Actions)

Mỗi lần push lên `main`, [.github/workflows/deploy.yml](.github/workflows/deploy.yml):

1. `npm ci` → `nuxt build` → đóng gói `.output`.
2. SSH vào VPS bằng khoá riêng `VPS_SSH_KEY_WEB`. Khoá này **bị ép chạy đúng một
   lệnh**: [deploy/deploy-web.sh](deploy/deploy-web.sh) — giống cách repo HTBB đang làm.
3. **`nhan-env`**: gửi các biến `KPAH_*` qua **stdin** (không qua biến môi trường của
   ssh, không in ra log). VPS gộp vào `/etc/kpah-web.env` (`chmod 640`). **Secret nào
   để trống thì VPS giữ nguyên giá trị đang có** — quên đặt một secret không làm sập web.
4. **`nhan-build <sha>`**: gửi bản build qua stdin → giải nén vào
   `/opt/kpah-web/releases/<sha>` → trỏ `current` sang đó → restart `kpah-web` → gọi thử
   `/api/site`. Hỏng thì **tự quay lại bản trước**. Giữ 5 bản gần nhất.

### Khai ở đâu trên GitHub

`KPAH-Web` (tên repo GitHub, giữ nguyên) > Settings > Secrets and variables > Actions:

| Tab **Secrets** (bí mật) | Tab **Variables** (không nhạy cảm) |
|---|---|
| `VPS_SSH_KEY_WEB` — **bắt buộc** (VPS cũ 180.93.57.30) | `KPAH_LINK_ANDROID`, `KPAH_LINK_PC`, `KPAH_LINK_IOS`, `KPAH_LINK_JAR`, `KPAH_LINK_GROUP` |
| `VPS_SSH_KEY_WEB_2` — VPS mới 180.93.127.26 (để trống = bỏ qua VPS mới) | |
| `KPAH_SECRET` — **bắt buộc**, `openssl rand -hex 32` | `KPAH_AUTO_ACTIVATE` (`true`/`false`), `KPAH_NAP_BAT` (`true`/`false`, xem [Nhánh Pokémon](#nhánh-pokémon-pokemonchuyen-doi)) |
| `KPAH_ADMIN_ACCOUNTS` — vd `hoahv,admin` | `KPAH_ADMIN_2FA_BAT_BUOC` |
| `KPAH_TPBANK_ACCOUNT_NO`, `KPAH_TPBANK_ACCOUNT_NAME` | `KPAH_ADMIN_IDLE_TIMEOUT`, `KPAH_ADMIN_MAX_FAIL`, `KPAH_ADMIN_FAIL_WINDOW` |
| `KPAH_SEPAY_WEBHOOK_APIKEY` | `KPAH_SERVER_DIR` (mặc định `/opt/kpah`) |
| `KPAH_ADMIN_NAPTIEN_PASSWORD` | `KPAH_TRUSTED_PROXIES` |
| `KPAH_DB_*` — chỉ khi web không đọc được `server.ini` | |
| `KPAH_GMAIL_CLIENT_ID`, `KPAH_GMAIL_CLIENT_SECRET`, `KPAH_GMAIL_REFRESH_TOKEN`, `KPAH_GMAIL_SENDER` — gửi email, xem [Gửi email (Gmail API)](#gửi-email-gmail-api) | `KPAH_MAIL_FROM_NAME` (mặc định `Huyền Thoại Bảo Bối`) |

**Hai VPS.** Push lên `main` deploy song song lên cả hai (job `deploy (vps-cu)` và
`deploy (vps-moi)`, một bên lỗi không chặn bên kia). VPS mới **chỉ nhận bản build**: các
secret `KPAH_*` ở trên là của VPS cũ (mật khẩu CSDL, `KPAH_SECRET`...), đẩy sang VPS mới
là web ở đó dùng sai CSDL - nên VPS mới giữ `/etc/kpah-web.env` riêng, sửa tay trên VPS.
Lấy giá trị cho `VPS_SSH_KEY_WEB_2`: `ssh root@180.93.127.26 cat /root/.ssh/deploy_github_web`
(khoá bị ép chạy đúng `deploy-web.sh`, tạo bởi `cai-dat-vps.sh`) - dán TOÀN BỘ, giữ xuống dòng.

Thêm biến mới thì phải thêm ở **ba** chỗ: `deploy.yml` (env + vòng `for`),
`KHOA_HOP_LE` trong `deploy/deploy-web.sh`, và `server/utils/config.ts`.

Đổi `KPAH_SECRET` = mọi người (kể cả admin) bị đăng xuất.

### Cài đặt một lần trên VPS

```bash
scp -r deploy root@180.93.57.30:/root/kpah-web-deploy
ssh root@180.93.57.30 'bash /root/kpah-web-deploy/cai-dat-vps.sh'
```

Script kiểm Node ≥ 20, tạo user `kpah-web` (thêm vào nhóm `kpah` để đọc
`server.ini` và log game), cài `kpah-web.service`, tạo khoá SSH bị ép lệnh và in
private key ra để dán vào secret `VPS_SSH_KEY_WEB`. Rồi đặt các secret và push.

## Gửi email (Gmail API)

Website gửi thư bằng **Gmail API** (OAuth2 + refresh token, không dùng SMTP/mật khẩu
Gmail) — [server/utils/guiMail.ts](server/utils/guiMail.ts). Dùng cho:

- **Quên mật khẩu** (`/quen-mat-khau`): nhập tên tài khoản + email đã đăng ký → web
  đặt mật khẩu mới ngẫu nhiên (8 ký tự chữ thường + số) và gửi về email. Gửi thư hỏng
  thì trả lại mật khẩu cũ. Chưa cấu hình mail thì trang báo "chưa được bật".
- Thư báo khi người chơi **đổi mật khẩu** / **đổi email** ở trang Tài khoản (mục Bảo mật).

Thiếu một trong 4 biến `KPAH_GMAIL_CLIENT_ID`, `KPAH_GMAIL_CLIENT_SECRET`,
`KPAH_GMAIL_REFRESH_TOKEN`, `KPAH_GMAIL_SENDER` = tắt gửi mail. `KPAH_MAIL_FROM_NAME` là
tên người gửi (mặc định `Huyền Thoại Bảo Bối`).

### Lấy thông tin (làm một lần)

1. Vào [Google Cloud Console](https://console.cloud.google.com/), đăng nhập **đúng tài
   khoản Gmail sẽ dùng để gửi thư**, tạo project mới (vd. `htbb-mail`).
2. **APIs & Services → Library** → tìm **Gmail API** → **Enable**.
3. **APIs & Services → OAuth consent screen** (Google Auth Platform):
   - User type: **External**. Điền tên ứng dụng (vd. `HTBB Web`), email hỗ trợ, email liên hệ.
   - **Data access / Scopes** → Add scope `https://www.googleapis.com/auth/gmail.send`
     (chỉ quyền gửi thư, không đọc được hộp thư).
   - **Audience → Publish app** → chuyển sang **In production**. Dùng cho chính mình thì
     **không cần gửi Google xác minh**. Nếu để ở "Testing", refresh token **hết hạn sau
     7 ngày** và web sẽ ngừng gửi được mail. Khi cấp quyền sẽ gặp cảnh báo "Google chưa
     xác minh ứng dụng này" một lần: bấm **Nâng cao → Đi tới ... (không an toàn)**.
4. **APIs & Services → Credentials → Create credentials → OAuth client ID**:
   - Loại **Desktop app** (khuyên dùng — Google nhận mọi cổng `127.0.0.1`), hoặc
   - Loại **Web application** với Authorized redirect URI đúng `http://127.0.0.1:8765/`
     (đổi cổng thì đổi cả ở đây lẫn tham số cổng của script).
   - Lưu lại **Client ID** và **Client secret**.
5. Trên máy có trình duyệt, trong thư mục HTBB-Web:

   ```bash
   node scripts/lay-gmail-refresh-token.mjs <client_id> <client_secret> [cổng, mặc định 8765]
   ```

   Mở đường link script in ra, đăng nhập đúng tài khoản Gmail gửi thư, bấm Cho phép.
   Script in ra `KPAH_GMAIL_REFRESH_TOKEN=...`.
6. Đặt giá trị:
   - **VPS cũ 180.93.57.30**: GitHub → Settings → Secrets and variables → Actions →
     **Secrets**: `KPAH_GMAIL_CLIENT_ID`, `KPAH_GMAIL_CLIENT_SECRET`,
     `KPAH_GMAIL_REFRESH_TOKEN`, `KPAH_GMAIL_SENDER` (địa chỉ Gmail ở bước 1). Tab
     **Variables** (tuỳ chọn): `KPAH_MAIL_FROM_NAME`. Push / chạy lại workflow để deploy.
   - **VPS mới 180.93.127.26**: CI **không** đẩy biến môi trường sang VPS này — thêm tay
     các dòng trên vào `/etc/kpah-web.env` rồi `systemctl restart kpah-web`.

Refresh token + client secret = gửi được thư dưới tên bạn: **không** dán vào chat, không
commit. Bị lộ / web báo `invalid_grant` trong log (`journalctl -u kpah-web | grep '\[mail\]'`)
thì vào [quyền của tài khoản Google](https://myaccount.google.com/permissions) gỡ quyền
ứng dụng rồi chạy lại bước 5. Gmail thường giới hạn khoảng 500 thư/ngày cho tài khoản
cá nhân — dư cho quên mật khẩu.

## Bật website trên VPS

Apache **vẫn đứng trước** (HTTPS, mod_remoteip sau Nova WAF, whitelist IP, Basic
Auth cho `/admin`) và chuyển tiếp mọi request về Node ở `127.0.0.1:3000`.
Làm theo chú thích trong [deploy/apache-kpah-web.conf](deploy/apache-kpah-web.conf).
Lưu ý quan trọng ở đó: các luật `<Directory /opt/kpah/web>` không áp dụng cho
request đã proxy — phải chuyển sang `<Location />` trước.

Không phải đổi gì ở phía game:

- **Webhook SePay đổi địa chỉ**: vào SePay > Webhooks, đổi URL thành
  `https://<tên-miền>/api/nap/webhook-sepay` (API Key giữ nguyên). Chưa đổi thì
  tiền vẫn vào tài khoản nhưng đơn không tự xác nhận — phải bấm tay ở `/admin/naptien`.
- Đường dẫn của website cũ không còn chạy; các trang mới: `/`, `/download`,
  `/ranking`, `/news`, `/register`, `/login`, `/account`, `/kichhoat`, `/naptien`, `/ghi-cong`, `/admin`.
- Database, bảng, mật khẩu `PASSWORD()` y hệt — login server không biết gì về thay đổi.

Tắt proxy (trả Apache về website cũ): `a2disconf kpah-web-proxy && systemctl reload apache2`.

## Thiết kế bảo mật

- **Đăng nhập bằng token, không dùng cookie.** Nova WAF nuốt `Cookie`/`Set-Cookie`
  qua domain, nên mọi thứ dựa vào cookie phiên đều hỏng. Website đăng nhập bằng
  **token ký HMAC-SHA256** gửi trong header `Authorization: Bearer ...`
  ([server/utils/token.ts](server/utils/token.ts)); chữ ký so bằng
  `timingSafeEqual`, loại token (`user` / `admin` / chờ mã hai bước / xác nhận nạp)
  nằm trong phần được ký nên token loại này không dùng thay được loại khác. Trình
  duyệt không tự gửi kèm token như cookie, nên không có lỗ CSRF và không cần token CSRF.
- **`KPAH_SECRET` là khoá ký mọi token, kể cả admin.** Bắt buộc đặt khi chạy thật
  (`openssl rand -hex 32`). Thiếu thì server tự sinh khoá ngẫu nhiên cho tiến trình
  (an toàn, nhưng mọi người bị đăng xuất mỗi lần khởi động lại) và ghi cảnh báo vào log.
- **Phiên.** Người chơi: 7 ngày kể từ lúc đăng nhập (localStorage). Admin:
  sessionStorage (đóng tab là mất), hết hạn sau `KPAH_ADMIN_IDLE_TIMEOUT` giây
  (mặc định 60 phút) không thao tác — mỗi request hợp lệ server cấp token mới qua
  header `X-Htbb-Token` (gia hạn trượt). Tên bị gỡ khỏi `KPAH_ADMIN_ACCOUNTS` thì
  mất quyền ngay ở request kế tiếp; danh sách rỗng = không ai vào được.
- **IP khách.** Node chỉ tin `X-Forwarded-For` khi kết nối đến từ proxy cùng máy
  (`KPAH_TRUSTED_PROXIES`, mặc định chỉ loopback), và lấy phần tử **cuối** — địa
  chỉ do chính Apache ghi sau khi mod_remoteip đã khôi phục IP thật. Kết nối thẳng
  từ Internet thì bỏ qua mọi header tự khai.
- **Giới hạn số lần** theo IP: đăng ký, gõ sai mật khẩu người chơi, quên mật khẩu
  (đếm mọi lần gửi), gõ sai mật khẩu hiện tại khi đổi mật khẩu/email, tạo đơn nạp, gõ
  sai mật khẩu trang xác nhận nạp (bảng `web_chan_spam`), gõ sai mật khẩu admin
  (bảng `web_admin_log`). Thiếu bảng thì lùi về đếm trong bộ nhớ của tiến trình
  (mất khi khởi động lại). Lỗi database khi đếm thì cho qua — thà bỏ lọt còn hơn
  chặn oan người chơi thật.
- **Quên mật khẩu** trả cùng một câu cho mọi trường hợp (sai tên, sai email, tài khoản
  bị khoá, đã cấp lại quá 3 lần/24 giờ cho tài khoản đó) — không dò được tài khoản nào
  có thật hay email của ai. Hạn mức 3 lần/24 giờ theo tài khoản đếm trong bộ nhớ (mất khi
  khởi động lại). Mật khẩu mới chỉ nằm trong thư, không hiện trên web.
- **Không lộ dữ liệu.** Mọi câu SQL dùng tham số `?`; mật khẩu so bằng `PASSWORD()`
  ngay trong SQL, không bao giờ trả cột mật khẩu ra ngoài (Quên mật khẩu đọc bản băm
  cũ chỉ để ghi trả lại khi gửi thư hỏng). API admin bọc bằng
  `defineAdminHandler` (bắt buộc token admin); chỉ admin đã đăng nhập mới thấy chi
  tiết lỗi, người khác chỉ nhận thông báo chung — thông báo lỗi của MySQL có thể
  chứa host, tên database, tên người dùng.

## Cấu trúc

```
app/                  # giao diện (Vue)
  pages/              # mỗi file = một đường dẫn; pages/admin/* = khu quản trị
  layouts/            # default (trang công khai), admin (dashboard)
  components/three/   # cảnh 3D TresJS của trang chủ
  components/fx/      # FxTiltCard (nghiêng 3D), FxReveal, FxGlowOrbs
  components/site/    # header, footer, banner, khung đăng nhập
  components/admin/   # AdminPage, thẻ số liệu, biểu đồ thuần CSS
  composables/        # useAuth, useApi, useSite
server/
  api/                # API: auth, nap, ranking, site, admin/*
  utils/              # config, db, token, chặn spam, admin, ...
shared/utils/         # dùng chung client + server: quy tắc tài khoản, mốc nạp, ...
public/               # logo, ảnh
deploy/               # deploy-web.sh, systemd, Apache, cài đặt VPS
```
