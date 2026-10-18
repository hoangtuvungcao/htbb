# Cấu hình trung tâm (quản lý từ web, áp dụng ngay)

Mọi công tắc và tham số chỉnh được lúc chạy nằm ở **một** danh mục và **một** dịch vụ:

| Phần | Tệp |
|---|---|
| Danh mục khoá (nguồn duy nhất) | `tools/cauhinh/danh_muc.json` |
| Bộ sinh | `tools/cauhinh/sinh_cau_hinh.py` → `src/com/htbb/cauhinh/DanhMucSinh.java`, `sql/pokemon_cau_hinh_trung_tam.sql`, `../HTBB-Web/shared/utils/cauHinhTrungTam.gen.ts` |
| Dịch vụ máy chủ | `src/com/htbb/cauhinh/CauHinhTrungTam.java` (+ `TinhCauHinh`, `KiemGiaTri`, `ApDungMacDinh`, `CongTac`) |
| Trang web | `/admin/cau-hinh-he-thong` (nhóm *Hệ thống*), API `/api/admin/cau-hinh-he-thong/**` |
| Bộ ví dụ kiểm dùng chung Java ↔ web | `tools/cauhinh/vi_du_kiem.tsv` |
| Test | `test/KiemCauHinhTrungTam.java`, `../HTBB-Web/tests/cau-hinh-trung-tam.test.ts` |

Sửa danh mục: chỉnh `danh_muc.json` → `python3 tools/cauhinh/sinh_cau_hinh.py` (kiểm lệch: thêm `--kiem`).

## Thứ tự ưu tiên

**bảng `cau_hinh` (CSDL) > `server.ini` > mặc định trong danh mục.**

- Giá trị CSDL sai kiểu / khoảng / lựa chọn bị **từ chối**: máy chủ giữ giá trị tốt gần nhất, ghi lý do vào
  `cau_hinh_hieu_luc.loi` và `cau_hinh_ap_dung.loi` (trang hiện đỏ).
- Giá trị `server.ini` sai luật vẫn được dùng như cũ (mã đọc riêng của tính năng tự kẹp), chỉ cảnh báo.
- Cổng, CSDL, đường dẫn log… **không** nằm trong danh mục: chỉ đọc từ `server.ini` (con gà – quả trứng).
- Bí mật cổng thanh toán của web vẫn ở biến môi trường; khoá `bang: env` chỉ hiện trạng thái.

## Áp dụng ngay / cần khởi động lại

- `apDung: ngay` — luồng `CauHinhTrungTam` hỏi `cau_hinh_phien_ban` + `COUNT/MAX(sua_luc)` mỗi **5 giây**
  (đọc lại cả bảng mỗi 60 giây để bắt người ghi cũ), áp dụng qua người nghe, log `[CAU HINH] AP DUNG khoa: cũ -> mới`.
- `apDung: khoi_dong_lai` (`sv.che_do_pokemon`, `sv.lau_dai`, `sv.he_sinh_thai`, `sv.nap_tien`, `sv.tran_cap_theo_mua`,
  `sv.ngay_khai_truong`, cờ sự kiện cũ…) — đọc MỘT lần lúc khởi động (`CauHinhTrungTam.khoiTao(p)` ghi đè Properties
  ngay sau `Database.setLink1`, trước khi TeamServer đọc các cờ); sau đó đóng băng, giá trị mới hiện ở mục
  *chờ khởi động lại*.
- `khoaProd` (thử nghiệm: `sv.tua_nhanh_ngay`, `sv.testMode`, `sv.testCode`) — máy thật chỉ nhận mặc định từ CSDL.
  Máy thử thêm `sv.cho_phep_thu_nghiem=1` vào `server.ini` (web: chế độ dev hoặc `HTBB_CHO_PHEP_THU_NGHIEM=1`).

## Bảng

| Bảng | Ai ghi | Nội dung |
|---|---|---|
| `cau_hinh` | web, game, SQL tay | giá trị (đã có từ trước) |
| `cau_hinh_phien_ban` | web / `CauHinhTrungTam.ghi` / `CauHinh.luuNhieu` | số phiên bản tăng mỗi lần lưu |
| `cau_hinh_lich_su` | web (`nguon=web`), `ghi()`, máy chủ (`phat_hien` — ai đó ghi thẳng bảng) | ai / khi nào / cũ / mới / lý do; trang có nút hoàn tác |
| `cau_hinh_ap_dung` | máy chủ (mỗi lần áp dụng + nhịp 30 giây) | phiên bản đã áp dụng, giờ, lỗi, khoá chờ khởi động lại |
| `cau_hinh_hieu_luc` | máy chủ | giá trị đang chạy từng khoá + nguồn (db/ini/mac_dinh/giu_cu) |
| `cau_hinh_khoi_dong_lai` | web đặt `cho`/`huy`, máy chủ chuyển trạng thái | khởi động lại an toàn |
| `cau_hinh_danh_muc` | `sql/pokemon_cau_hinh_trung_tam.sql` | bản tra cứu của danh mục |

## Khởi động lại an toàn

Web ghi `trang_thai='cho'` (giây 10–1800, lý do) → máy chủ (≤ 5 giây) chuyển `dang_dem`, báo người chơi, đếm ngược
(huỷ được: `huy` → `da_huy`) → hết giờ `dang_thoat`, bật cờ `5h_systems restart` (luồng BaoTriDaily lưu toàn bộ dữ
liệu rồi thoát) → systemd `Restart=always` / `chay_novadb/chay_lap.sh` bật lại → lần khởi động mới ghi `da_xong`.
Yêu cầu `cho` cũ hơn 10 phút (gửi lúc máy chủ tắt) bị huỷ khi khởi động, không tự khởi động lại bất ngờ.

## API cho nơi khác (bảng Swing AdminPanel, lệnh trong game)

```java
CauHinhTrungTam.danhMuc();                       // DinhNghiaKhoa[]: khoa, nhom, ten(), moTa(), kieu, min(), max(), luaChon(), apDungNgay...
CauHinhTrungTam.anhChup();                       // giá trị đang chạy, nguồn, lỗi, chờ khởi động lại, phiên bản
CauHinhTrungTam.co("gym.bat"); CauHinhTrungTam.so("sv.tileXu", 10); CauHinhTrungTam.chuoi(k);
CauHinhTrungTam.ghi(map, "ten_admin", "swing", "lý do");   // kiểm hết rồi mới ghi, lịch sử + phiên bản + nạp lại ngay
CauHinhTrungTam.dangKyNghe(new String[]{"sv.spam."}, nghe); // gọi khi khoá đổi
CauHinhTrungTam.yeuCauKhoiDongLai(60, "lý do", "ten_admin"); CauHinhTrungTam.huyKhoiDongLai("ten_admin");
```

`server.CauHinh` cũ vẫn còn (bảng setting trong game, các hằng `K_*`): `napLai()` nay uỷ quyền cho dịch vụ trung tâm,
phần áp dụng thành `CauHinh.apDung(Properties)` (một người nghe), `luuNhieu()` tăng phiên bản sau khi ghi.

## Nạp tiền (fail closed)

Công tắc sống trong `cau_hinh`: `nap.luong.bat`, `nap.the_thang.bat`, `nap.ve_mua.bat` (web chặn tạo đơn mới + ẩn
gói; đọc lỗi = tắt), `thanh_vien.ban_bat` (cùng khoá trang Thành viên), `nap.tang_toc.bat` (cửa hàng lượng trong game),
`nap.hoan_tien.thu_cong` (nút hoàn tiền admin), `nap.hoan_tien.tu_dong` (máy chủ tự thu hồi đơn `cho_thu_hoi`),
`tran_tuan_vnd` / `tran_thang_vnd` (bảng `nap_cau_hinh`). Tầng trên vẫn là `HTBB_NAP_BAT` + giấy phép + kiểm cấu hình
cổng (biến môi trường) và `sv.nap_tien` + `sv.lau_dai` phía game — thiếu một cái là không thu tiền.
