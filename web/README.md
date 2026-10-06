# KPAH-Web — Website Khí Phách Anh Hùng

Website (trang công khai + khu quản trị) của máy chủ private KPAH, viết bằng **Vue 3**:

| Thành phần | Dùng gì |
|---|---|
| Framework | **Nuxt 4** (Vue 3.5, TypeScript) — trang + API server (Nitro) trong một dự án |
| Giao diện | **Nuxt UI v4** (Tailwind CSS v4, Reka UI) — bảng màu vàng cổ / ngọc bích / mực đêm |
| 3D | **TresJS** (Three.js cho Vue): Linh Kiếm lơ lửng, vòng thiên văn, tinh thể, đom đóm ở trang chủ |
| Hiệu ứng | **motion-v** (hiện dần khi cuộn), thẻ nghiêng 3D theo chuột (`FxTiltCard`) |
| Database | `mysql2` — dùng **chung** MariaDB với game server (DB `account` + `kpah_db`) |

Website này thay cho website cũ trong `KPAH/web/`; website cũ vẫn chạy cho tới khi
bạn bật proxy Apache sang đây (xem [Bật website trên VPS](#bật-website-trên-vps)).

## Chạy thử trên máy

Cần **Node ≥ 20** và MariaDB (XAMPP được).

```bash
npm install
cp .env.example .env      # rồi sửa .env (xem bên dưới)
npm run dev               # http://localhost:3000
```

`.env` tối thiểu khi dev:

```bash
KPAH_SERVER_DIR=C:/Users/ADMIN/Desktop/KPAH-All/KPAH   # để tự đọc server.ini
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
   lệnh**: [deploy/deploy-web.sh](deploy/deploy-web.sh) — giống cách repo KPAH đang làm.
3. **`nhan-env`**: gửi các biến `KPAH_*` qua **stdin** (không qua biến môi trường của
   ssh, không in ra log). VPS gộp vào `/etc/kpah-web.env` (`chmod 640`). **Secret nào
   để trống thì VPS giữ nguyên giá trị đang có** — quên đặt một secret không làm sập web.
4. **`nhan-build <sha>`**: gửi bản build qua stdin → giải nén vào
   `/opt/kpah-web/releases/<sha>` → trỏ `current` sang đó → restart `kpah-web` → gọi thử
   `/api/site`. Hỏng thì **tự quay lại bản trước**. Giữ 5 bản gần nhất.

### Khai ở đâu trên GitHub

`KPAH-Web` > Settings > Secrets and variables > Actions:

| Tab **Secrets** (bí mật) | Tab **Variables** (không nhạy cảm) |
|---|---|
| `VPS_SSH_KEY_WEB` — **bắt buộc** | `KPAH_LINK_ANDROID`, `KPAH_LINK_PC`, `KPAH_LINK_IOS`, `KPAH_LINK_GROUP` |
| `KPAH_SECRET` — **bắt buộc**, `openssl rand -hex 32` | `KPAH_AUTO_ACTIVATE` (`true`/`false`) |
| `KPAH_ADMIN_ACCOUNTS` — vd `hoahv,admin` | `KPAH_ADMIN_2FA_BAT_BUOC` |
| `KPAH_TPBANK_ACCOUNT_NO`, `KPAH_TPBANK_ACCOUNT_NAME` | `KPAH_ADMIN_IDLE_TIMEOUT`, `KPAH_ADMIN_MAX_FAIL`, `KPAH_ADMIN_FAIL_WINDOW` |
| `KPAH_SEPAY_WEBHOOK_APIKEY` | `KPAH_SERVER_DIR` (mặc định `/opt/kpah`) |
| `KPAH_ADMIN_NAPTIEN_PASSWORD` | `KPAH_TRUSTED_PROXIES` |
| `KPAH_DB_*` — chỉ khi web không đọc được `server.ini` | |

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

## Bật website trên VPS

Apache **vẫn đứng trước** (HTTPS, mod_remoteip sau Nova WAF, whitelist IP, Basic
Auth cho `/admin`) và chuyển tiếp mọi request về Node ở `127.0.0.1:3000`.
Làm theo chú thích trong [deploy/apache-kpah-web.conf](deploy/apache-kpah-web.conf).
Lưu ý quan trọng ở đó: các luật `<Directory /opt/kpah/web>` không áp dụng cho
request đã proxy — phải chuyển sang `<Location />` trước.

Không phải đổi gì ở phía game:

- `/server.txt` và `/server/NQSH2.txt` (client game đọc) nằm ở `public/`.
- **Webhook SePay đổi địa chỉ**: vào SePay > Webhooks, đổi URL thành
  `https://<tên-miền>/api/nap/webhook-sepay` (API Key giữ nguyên). Chưa đổi thì
  tiền vẫn vào tài khoản nhưng đơn không tự xác nhận — phải bấm tay ở `/admin/naptien`.
- Đường dẫn của website cũ không còn chạy; các trang mới: `/`, `/download`,
  `/ranking`, `/news`, `/register`, `/login`, `/account`, `/kichhoat`, `/naptien`, `/admin`.
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
  header `X-Kpah-Token` (gia hạn trượt). Tên bị gỡ khỏi `KPAH_ADMIN_ACCOUNTS` thì
  mất quyền ngay ở request kế tiếp; danh sách rỗng = không ai vào được.
- **IP khách.** Node chỉ tin `X-Forwarded-For` khi kết nối đến từ proxy cùng máy
  (`KPAH_TRUSTED_PROXIES`, mặc định chỉ loopback), và lấy phần tử **cuối** — địa
  chỉ do chính Apache ghi sau khi mod_remoteip đã khôi phục IP thật. Kết nối thẳng
  từ Internet thì bỏ qua mọi header tự khai.
- **Giới hạn số lần** theo IP: đăng ký, gõ sai mật khẩu người chơi, tạo đơn nạp, gõ
  sai mật khẩu trang xác nhận nạp (bảng `web_chan_spam`), gõ sai mật khẩu admin
  (bảng `web_admin_log`). Thiếu bảng thì lùi về đếm trong bộ nhớ của tiến trình
  (mất khi khởi động lại). Lỗi database khi đếm thì cho qua — thà bỏ lọt còn hơn
  chặn oan người chơi thật.
- **Không lộ dữ liệu.** Mọi câu SQL dùng tham số `?`; mật khẩu so bằng `PASSWORD()`
  ngay trong SQL, không bao giờ đọc cột mật khẩu ra. API admin bọc bằng
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
public/               # server.txt, logo, ảnh
deploy/               # deploy-web.sh, systemd, Apache, cài đặt VPS
```
