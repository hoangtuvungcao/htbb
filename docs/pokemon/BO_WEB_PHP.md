# Bỏ website PHP cũ (`web/`) — 19/10/2026

Website chính thức là **HTBB-Web** (Nuxt 4, repo `../HTBB-Web`; trong monorepo là `web/`). Thư mục `web/` PHP cũ của repo game đã bị xoá (`git rm -r web`) và không được dùng nữa. Trên VPS, Nuxt chạy sau reverse proxy; không còn vhost PHP hay DocumentRoot `/opt/htbb/web`. Runbook: [vps/CHUYEN_TEN_HTBB.md](../../vps/CHUYEN_TEN_HTBB.md), cài đặt: [docs/HUONG_DAN_BUILD_VPS.md](../HUONG_DAN_BUILD_VPS.md).

## Chức năng PHP → chỗ tương ứng trong HTBB-Web

| PHP cũ | HTBB-Web |
|---|---|
| `web/admin/*.php` (khu quản trị) | `/admin/*` (`app/pages/admin`, `server/api/admin`) |
| `web/admin/inc/lenh_nhan_vat.php` (hàng đợi `web_lenh_nhan_vat`, server game đọc qua `data.LenhWeb`) | `server/utils/lenhNhanVat.ts` — cùng bảng, cột, mã trạng thái, loại lệnh |
| `nhanvat.php` / `taikhoan.php` / `nang_level_toanbo.php` | `server/api/admin/nhanvat/[id]/*`, `server/api/admin/taikhoan/[id].post.ts`, `server/api/admin/nang-level-toanbo/*` |
| `sukien.php` (bảng `tob_su_kien`) | `/admin/sukien`, `server/api/admin/sukien/*`, `server/utils/suKien*.ts` |
| `cauhinh.php` (bảng `cau_hinh`) | `/admin/cauhinh`, `/admin/cau-hinh-he-thong` |
| `chandoan.php` | `server/utils/chanDoan.ts` |
| `inc/thuong.php` (cú pháp phần thưởng giftcode) | `shared/utils/thuong.ts`, `server/utils/giftcodeDanhMuc.ts` |
| `inc/danh_muc_thu/ngoc/danh_hieu.php`, `inc/capdo.php` (do `tools/Sinh*.java` sinh) | `shared/data/danh-muc-thu.json`, `danh-muc-ngoc.json`, `danh-muc-danh-hieu.json`, `bang-cap-do.json`. Bốn công cụ `tools/SinhDanhMucThu`, `SinhDanhMucNgoc`, `SinhDanhMucDanhHieu`, `SinhBangCapDo` giờ GHI THẲNG các tệp JSON này vào HTBB-Web; `server/utils/giftcode*.gen.ts` và `capDo.ts` đọc chúng |
| `naptien.php`, `webhook_sepay.php`, `admin_naptien.php` | `/naptien`, `server/api/nap/*` (webhook SePay), `/xac-nhan-nap` |
| `register.php` / `login.php` / `account.php` / `kichhoat.php` | `/register`, `/login`, `/account`, `/kichhoat` |
| `web/server/NQSH2.txt` + symlink `web/server.txt` (danh sách máy chủ: client Unity tải `http://HOST/server.txt`, client cũ tải `/NQSH2.txt` hoặc `/server/NQSH2.txt`, có thể kèm tiền tố `/kpah`) | `server/middleware/duong-dan-cu.ts` trả thẳng nội dung (không chuyển hướng), lấy từ biến `HTBB_SERVER_LIST` (dự phòng `HTBB_SERVER_LIST`). Riêng `NQSH2.txt` dùng `HTBB_NQSH_LIST` nếu có đặt. Chưa đặt biến thì trả 503 |
| Link cũ `/kpah/...` và `/*.php` | Cùng middleware đó (`server/utils/duongDanCu.ts`): 301 sang trang Nuxt tương ứng, giữ query string (vd `register.php` → `/register`, `admin/giam_level.php` → `/admin/giam-level`); không có trang tương ứng thì về trang chủ. Request không phải GET/HEAD (vd POST `webhook_sepay.php`) nhận 308 sang `/api/nap/webhook-sepay` để giữ method và body |

Phía server game không có hành vi nào chỉ dựa vào PHP: mọi bảng mà PHP từng ghi (`web_lenh_nhan_vat`, `tob_su_kien`, `cau_hinh`, `5h_systems`…) đều được HTBB-Web ghi với cùng định dạng. Chỉ có danh sách máy chủ trước đây là tệp tĩnh; nay middleware Nuxt ở trên phục vụ nó. `vps/chi-cho-server-txt.sh` cho khách lạ vào cả ba đường dẫn danh sách. Lưu ý thứ tự: phải có middleware này và đặt biến trên VPS **trước** khi nhánh xoá `web/` lên `main`, vì `deploy-auto.sh` sẽ xoá tệp PHP đang chạy.

## Bộ kiểm (test/) — từng phép kiểm PHP

Các phép kiểm hợp đồng Java ↔ web đọc HTBB-Web qua `test/WebHtbb.java`. Thứ tự tìm thư mục: `-Dhtbb.web`, `HTBB_WEB_DIR`, `../HTBB-Web`, rồi `../web`. Không thấy thì phép kiểm in dòng "bo qua" (CI của repo game chỉ checkout repo này), giống `KiemNapTien` đối chiếu `napTienThuan.ts`.

| Test | Hành động | Lý do |
|---|---|---|
| KiemDanhMucThu | Đổi đích: `shared/data/danh-muc-thu.json` | Web vẫn dùng danh mục (bộ chọn giftcode); vẫn kiểm 31 thú, từng tên, chỉ số 5/28 |
| KiemDanhMucNgoc | Đổi đích: `danh-muc-ngoc.json` | Như trên; vẫn kiểm tên, bán/không bán, nhóm NLSC/NLCC |
| KiemDanhMucDanhHieu | Đổi đích: `danh-muc-danh-hieu.json` | Như trên; vẫn kiểm tên với `getName()` và cờ ngẫu nhiên với `availableTitles` |
| KiemBangCapDo | Đổi đích: `bang-cap-do.json` | Web dùng `CAPDO_TOI_DA`/expMain. Bỏ riêng phần so nhãn `// cap M` vì JSON không có chú thích; cấp suy từ vị trí, và số mục cùng từng số vẫn được đối chiếu |
| KiemLenhWeb | Đổi đích: `lenhNhanVat.ts`, API `nhanvat/[id]/cap-do`, `xoa-vat-pham`, `nang-level-toanbo`, `adminMenu.ts` | Giao thức hàng đợi lệnh vẫn quan trọng. Bỏ kiểm `web/admin/test/kiem_schema.php` (bộ kiểm PHP không còn); thay bằng kiểm `CREATE TABLE` của web khớp file SQL |
| KiemXoaVinhVien | Đổi đích: API `nhanvat/[id]/xoa`, `taikhoan/[id].post.ts`, `taiKhoanAdmin.ts` | Chặn tài khoản quản trị, gõ đúng tên, xoá qua hàng đợi, không `DELETE FROM tob_char` |
| KiemSuKien (mục 7) | Đổi đích: `server/api/admin/sukien/*`, `suKien.ts`, `adminMenu.ts` | Web không được ghi đè cột của máy chủ |
| KiemLichSuKien | Đổi đích: `sukien/lich.post.ts`, `xoa-lich.post.ts`, `suKien.ts`, `suKienDb.ts`. Bỏ kiểm CSS `.lich-form`/`.lich-o` | CSS của `admin.css` PHP; trang Nuxt dùng Nuxt UI, không có tệp CSS tương đương |
| KiemGiftcodeRuyBangHoenn, KiemGiftcodeDoCamTayBaoVat | Đổi đích: `shared/utils/thuong.ts` | Hướng dẫn cú pháp cho admin vẫn phải đúng với code Java |
| KiemLocLogVatPham (mục 4) | Đổi đích: `server/utils/chanDoan.ts` (`CHAN_DOAN_LOAI_LOI`, cộng các `aclog = '…'`) | Loại nào trang chẩn đoán đếm thì phải được giữ trong log |
| KiemBangMaNguon | Bỏ `web` khỏi thư mục quét, bỏ 5 tệp PHP khỏi danh sách bỏ qua | Thư mục không còn; HTBB-Web có `npm test` riêng |
| KiemLienKetTaiLieu | Bỏ `web/README.md` | Tệp không còn |
| KiemThuatNgu | Bỏ quét `web` (.php) | Thư mục không còn; HTBB-Web vẫn được quét (`../HTBB-Web/app,server,shared,scripts`) |

HTBB-Web có thêm `tests/danh-sach-may-chu.test.ts`. Tệp này kiểm danh sách máy chủ, ánh xạ link cũ `/kpah` / `.php` và hình dạng bốn tệp JSON danh mục.
