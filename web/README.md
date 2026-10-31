# web — Website Huyền Thoại Bảo Bối

Website + khu quản trị (CMS) của game, viết bằng **Nuxt 4** (Vue 3, Nuxt UI, Nitro), đọc/ghi thẳng CSDL game
(MariaDB / NovaDB) qua `mysql2`.

- Trang công khai: trang chủ, giới thiệu, hướng dẫn tân thủ, tải game (5 bản: Java J2ME, Windows, Linux AppImage, Android, iOS sắp ra mắt), cẩm nang, Pokédex, tin tức,
  bảng xếp hạng, tài khoản, giấy phép (`/giay-phep`).
- Khu quản trị `/admin`: người chơi, cấu hình hệ thống, sự kiện, giftcode, nội dung CMS, bản tải game, nhật ký.
  Bảng CMS tạo bằng `tools/luu_tru_sinh_du_lieu/sql_nguon/web_cms.sql` (chưa chạy thì web dùng nội dung mặc định).

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
| `HTBB_SERVER_LIST`, `HTBB_SERVER_LIST_CU` | Danh sách máy chủ cho client (`/server.txt`) |
| `HTBB_LINK_ANDROID`, `HTBB_LINK_PC`, `HTBB_LINK_JAR`, `HTBB_LINK_IOS`, `HTBB_LINK_GROUP` | Link dự phòng khi CMS để trống |
| `HTBB_NAP_BAT` + `HTBB_NAP_*`, `HTBB_SEPAY_*`, `HTBB_CASSO_*` | Nạp tiền — mặc định **tắt** |
| `HTBB_GMAIL_*`, `HTBB_MAIL_FROM_NAME` | Gửi email (Gmail API); lấy refresh token: `node scripts/lay-gmail-refresh-token.mjs` |
| `HTBB_UPLOAD_DIR` | Thư mục ảnh/tệp tải lên (mặc định `./uploads`) |
| `HTBB_DIR`, `HTBB_WEB_SRC_DIR` | Đường dẫn monorepo cho script tài nguyên (`npm run sinh-anim-khoi-dau`) |

Giải thích đầy đủ từng biến nằm trong `.env.example`. Các khoá cấu hình game (`cau_hinh`): `docs/cau-hinh.md`.

## Lệnh

Cần **Node 22** (≥ 22.12, xem `.nvmrc`).

```bash
npm ci                      # cài thư viện
npm run dev                 # chạy thử (http://localhost:3000)
npm run build               # bản chạy thật ở .output/  ->  npm start
npm test                    # kiểm thử đơn vị (node --test tests/*.test.ts)
npm run typecheck
HTBB_DIR=.. npm run sinh-anim-khoi-dau   # ảnh động màn khởi đầu từ tài nguyên client
```

`shared/data/*.json` và `shared/utils/*.gen.ts` là **bản dự phòng tĩnh** (bộ sinh cũ đã lưu trữ ở
`tools/luu_tru_sinh_du_lieu/`): dữ liệu game nằm trong CSDL (`sql/htbb.sql`), web đọc CSDL trước; test đối chiếu
các tệp tĩnh với `sql/htbb.sql`.
Kiểm mọi trang công khai bằng Chrome headless (lỗi console, cuộn ngang, ảnh hỏng, khung thả xuống đầu trang bị cắt chữ;
390 / 1280 / 1440 / 1920 / 2560 px, sáng + tối): chạy server thật rồi
`node scripts/kiem-trang-cong-khai.mjs --goc http://127.0.0.1:3000 --ra /tmp/kiem-trang [--anh] [--toi] [--menu-moi-trang] [--tk .. --mk ..]`.
Ảnh chụp trong game cho trang chủ / hướng dẫn: `public/anh-game/` (danh mục `shared/utils/anhGame.ts`).

`node scripts/dev-token.mjs admin admin 1` in token admin để thử API khi dev.

## Dữ liệu game: CSDL trước, JSON dự phòng

Dữ liệu đã có bảng SQL trong `htbb_db` được web đọc **lúc chạy** qua `server/utils/duLieuSql.ts` (danh sách trắng
tên bảng, cache 5 phút, bảng thiếu/trống thì một dòng `[du-lieu-sql]` cảnh báo rồi dùng JSON đóng gói lúc build):

| Dữ liệu web | Bảng SQL | Nơi đọc |
|---|---|---|
| Giới hạn cấp + bảng EXP cũ của trang quản trị (`bang-cap-do.json`) | `data_cap_do_lau_dai.exp_cu` | `server/utils/capDo.ts` (`await capDoToiDa()`) |
| Trang bị phát được bằng giftcode (`giftcode-an-toan.json` → `trangBi`) | `data_noi_dung_trang_bi.id_item` | `server/utils/giftcodeAnToan.ts` (`gcDuLieu()`) |
| Cẩm nang: bảng cấp, năng lượng, kinh tế, nâng cấp, lịch 52 tuần (`cam-nang.json`) | `data_cap_do_lau_dai`, `data_kinh_te_lau_dai`, `data_nang_cap_lau_dai`, `data_su_kien_nam` | `/api/cam-nang/du-lieu` → `useCamNang()` |
| Cẩm nang: cửa hàng, Gym, chế tạo, nhánh Eevee, trang trí, vật phẩm sự kiện, lịch boss (`cam-nang-noi-dung.json`) | `data_noi_dung_*` | như trên |
| Danh mục cấu hình trung tâm (`cauHinhTrungTam.gen.ts`) | `cau_hinh_danh_muc`, `cau_hinh_nhom` | `server/utils/cauHinhDanhMucSql.ts`, API `/api/admin/cau-hinh-he-thong` |

Biến đổi dòng SQL → hình dạng JSON nằm ở `shared/utils/duLieuSqlThuan.ts`; `tests/du-lieu-sql.test.ts` và
`tests/cau-hinh-danh-muc-sql.test.ts` đọc `../sql/htbb.sql` (bằng `shared/utils/sqlDump.ts`) và đòi kết quả
khớp từng phần tử với JSON đóng gói. Trang dùng `const { camNang: CAM_NANG } = await useCamNang()` thay cho `CAM_NANG`
tĩnh khi cần số liệu có bảng SQL. Vẫn chỉ là JSON (không có bảng SQL): `pokedex.json`, `sprite-anim.json`,
`quy-tac-dang-ky.json` (nguồn viết tay), `danh-muc-thu/ngoc/danh-hieu.json` (sinh từ mảng Java), và các mục cẩm nang
lấy từ hằng Java / CSV (nhiệm vụ, bản đồ, PvP, sổ tay, hầm ngục, hệ sinh thái, cốt truyện, vật phẩm tiêu hao).

## Triển khai

Xem `docs/trien-khai.md` (VPS, systemd `deploy/htbb-web.service`, nginx `../vps/htbb-nginx.conf`,
GitHub Actions `web-deploy.yml`). Vận hành, nạp tiền, thành viên, email: `docs/van-hanh.md`.
Giấy phép tài nguyên: `docs/giay-phep.md` và trang `/giay-phep`.
