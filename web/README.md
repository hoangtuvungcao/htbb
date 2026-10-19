# web — Website Huyền Thoại Bảo Bối

Website + khu quản trị (CMS) của game, viết bằng **Nuxt 4** (Vue 3, Nuxt UI, Nitro), đọc/ghi thẳng CSDL game
(MariaDB / NovaDB) qua `mysql2`.

- Trang công khai: trang chủ, giới thiệu, hướng dẫn tân thủ, tải game (5 bản: Java J2ME, Windows, Linux AppImage, Android, iOS sắp ra mắt), cẩm nang, Pokédex, tin tức,
  bảng xếp hạng, tài khoản, giấy phép (`/giay-phep`).
- Khu quản trị `/admin`: người chơi, cấu hình hệ thống, sự kiện, giftcode, nội dung CMS, bản tải game, nhật ký.
  Bảng CMS tạo bằng `sql/web_cms.sql` (chưa chạy thì web dùng nội dung mặc định).

## Cấu hình

Chép `.env.example` thành `.env` (dev); trên VPS là `/etc/htbb/htbb.env` (dùng chung với game). Biến chính:

| Biến | Ý nghĩa |
|---|---|
| `HTBB_SERVER_DIR` | Thư mục game server (web tự đọc `server.ini` để lấy thông tin CSDL) |
| `HTBB_SECRET` | Khoá ký token đăng nhập — bắt buộc, chuỗi ngẫu nhiên dài (`openssl rand -hex 32`) |
| `HTBB_DB_*` | Ghi đè thông tin CSDL đọc từ `server.ini` (thường không cần) |
| `HTBB_SITE_URL` | Địa chỉ gốc website (canonical, sitemap); trống = chặn lập chỉ mục |
| `HTBB_ADMIN_ACCOUNTS` | Tài khoản luôn là Siêu quản trị (cách nhau dấu phẩy) |
| `HTBB_ADMIN_2FA_BAT_BUOC`, `HTBB_ADMIN_IP_CHO_PHEP`, `HTBB_ADMIN_*` | Bảo mật khu quản trị |
| `HTBB_SERVER_LIST`, `HTBB_NQSH_LIST` | Danh sách máy chủ cho client (`/server.txt`) |
| `HTBB_LINK_ANDROID`, `HTBB_LINK_PC`, `HTBB_LINK_JAR`, `HTBB_LINK_IOS`, `HTBB_LINK_GROUP` | Link dự phòng khi CMS để trống |
| `HTBB_NAP_BAT` + `HTBB_NAP_*`, `HTBB_SEPAY_*`, `HTBB_CASSO_*` | Nạp tiền — mặc định **tắt** |
| `HTBB_GMAIL_*`, `HTBB_MAIL_FROM_NAME` | Gửi email (Gmail API); lấy refresh token: `node scripts/lay-gmail-refresh-token.mjs` |
| `HTBB_UPLOAD_DIR` | Thư mục ảnh/tệp tải lên (mặc định `./uploads`) |
| `HTBB_DIR`, `HTBB_WEB_SRC_DIR` | Đường dẫn monorepo cho script đồng bộ dữ liệu (`npm run dong-bo-du-lieu`, chạy lúc build — trang `/admin/dong-bo` đã bỏ) |

Giải thích đầy đủ từng biến nằm trong `.env.example`. Các khoá cấu hình game (`cau_hinh`): `docs/cau-hinh.md`.

## Lệnh

Cần **Node 22** (≥ 22.12, xem `.nvmrc`).

```bash
npm ci                      # cài thư viện
npm run dev                 # chạy thử (http://localhost:3000)
npm run build               # bản chạy thật ở .output/  ->  npm start
npm test                    # kiểm thử đơn vị (node --test tests/*.test.ts)
npm run typecheck
HTBB_DIR=.. npm run dong-bo-du-lieu   # sinh lại shared/data/*.json + public/pokemon từ du_lieu/ và mã server
```

Kiểm mọi trang công khai bằng Chrome headless (lỗi console, cuộn ngang, ảnh hỏng, khung thả xuống đầu trang bị cắt chữ;
390 / 1280 / 1440 / 1920 / 2560 px, sáng + tối): chạy server thật rồi
`node scripts/kiem-trang-cong-khai.mjs --goc http://127.0.0.1:3000 --ra /tmp/kiem-trang [--anh] [--toi] [--menu-moi-trang] [--tk .. --mk ..]`.
Ảnh chụp trong game cho trang chủ / hướng dẫn: `public/anh-game/` (danh mục `shared/utils/anhGame.ts`).

Không sửa tay `shared/data/*.json` và `shared/utils/*.gen.ts` — đổi nguồn rồi chạy lại script sinh.
`node scripts/dev-token.mjs admin admin 1` in token admin để thử API khi dev.

## Triển khai

Xem `docs/trien-khai.md` (VPS, systemd `deploy/htbb-web.service`, nginx `../vps/htbb-nginx.conf`,
GitHub Actions `web-deploy.yml`). Vận hành, nạp tiền, thành viên, email: `docs/van-hanh.md`.
Giấy phép tài nguyên: `docs/giay-phep.md` và trang `/giay-phep`.
