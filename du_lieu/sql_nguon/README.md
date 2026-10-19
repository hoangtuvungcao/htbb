# du_lieu/sql_nguon — tệp SQL nguồn của CSDL đầy đủ

Nguồn để dựng [`sql/htbb_day_du.sql`](../../sql/README.md) — tệp duy nhất dùng để cài máy mới. Không nạp lẻ các tệp ở
đây lên CSDL đang chạy (dùng `vps/nang_cap_csdl.sh`). Tệp `pokemon_*.sql` là **tệp sinh** từ `du_lieu/*.csv` bằng bộ sinh
trong `tools/pokemon/` — sửa nguồn rồi sinh lại, đừng sửa tay. SQL thao tác tay xoá dữ liệu người chơi nằm ở
`vps/van_hanh/` (tắt server + sao lưu trước).

## Các tệp

| Nhóm | Tệp | Ghi chú |
|---|---|---|
| Seed gốc | `gamedb7_clean.sql` | bảng + dữ liệu game gốc, không có người chơi. Test đọc trực tiếp (`KiemBangThuocTinh`, `KiemDanhMucItem`) và `tools/SinhDanhMucItem.java` sinh `du_lieu/danh_muc_item.md` từ đây |
| Pokémon (sinh) | `pokemon_*.sql` | dữ liệu quái, chiêu, vật phẩm, chữ, thuật ngữ, nội dung, nhiệm vụ, dài hạn, hệ sinh thái, chỉ số, nạp tiền, thành viên; `pokemon_cau_hinh_trung_tam.sql` sinh từ `tools/cauhinh/danh_muc.json` |
| Thêm bảng | `them_bang_*.sql` | sự kiện (`tob_su_kien`), log online, nhật ký admin web, chặn spam web, cấm máy, 2FA, Pokédex, dài hạn, top, điều khiển server, `them_bang_web_lenh_nhan_vat.sql` (hàng đợi lệnh web → game; server và web cũng tự tạo bảng) |
| Thêm cột / khoá | `them_cot_*.sql`, `them_khoa_duy_nhat_giftcode_log.sql`, `them_chi_muc_production.sql`, `sua_cot_itemquest.sql` | chạy lại được (bản cột / chỉ mục / nới cột đã gộp vào `vps/nang_cap_csdl.sql`); `sua_cot_itemquest.sql` cho CSDL dựng trước 19/10/2026 (cột `tob_char_quest.itemquest` 100 ký tự làm lưu nhiệm vụ thất bại trên MariaDB) |
| Sự kiện, vật phẩm | `them_su_kien_world_cup.sql`, `them_phi_phong_vo_hinh.sql`, `skin_linh_thu.sql`, `ten_mo_ta_skin_linh_thu.sql`, `sua_type_skin_linh_thu.sql`, `vo_hieu_giftcode_tt.sql`, `xoa_su_kien_trung_lap.sql` | đã nằm trong `htbb_day_du.sql`; giữ vì test và mã nguồn trỏ vào |
| Web CMS | `web_cms.sql` | bảng nội dung website (thiếu thì web dùng nội dung mặc định) |

### Thứ tự nạp

Chỉ `tools/pokemon/xuat_sql_day_du.sh` nạp các tệp này, trên một MariaDB **trống**: `server/loginServer/account.sql` →
`gamedb7_clean.sql` → `vps/db_new_server.sql` → migration `them_*` / skin → `pokemon_*.sql` (`them_cot_pokemon` …
`pokemon_doi_chu` → `pokemon_lau_dai` → `pokemon_nhiem_vu` → … → `pokemon_noi_dung` → `pokemon_nap_tien` →
`pokemon_he_sinh_thai` → `pokemon_vat_pham_lau_dai` → `pokemon_cau_hinh_trung_tam` → `pokemon_chi_so` →
`pokemon_thuat_ngu`) → `them_chi_muc_production`, rồi mysqldump ra `sql/htbb_day_du.sql`. Migration chạy bằng `mysql -f`
(lỗi "đã có" vô hại).

CSDL đang chạy không nạp lẻ từng tệp: `tools/pokemon/sinh_nang_cap_csdl.py` gom phần chạy lại được (bảng / cột mới,
nới cột, 8 bảng thiết kế máy chủ đọc lúc khởi động, chỉ mục) thành `vps/nang_cap_csdl.sql`, nạp bằng
`sudo bash /opt/htbb/vps/nang_cap_csdl.sh`. Từ P1 các bảng thiết kế đó là **nguồn duy nhất** (không còn bản chép trong
Java): sửa số liệu là sửa bộ sinh, sinh lại `htbb_day_du.sql` + `nang_cap_csdl.sql`, nâng cấp rồi khởi động lại máy chủ
(danh mục cấu hình tự nạp lại sau ≤ 60 giây).

Đừng xoá tệp chỉ vì migration "đã áp dụng": nhiều tệp là nguồn schema cho test (`KiemSkinThuCuoi`, `KiemBangMaNguon`
quét cả thư mục này), công cụ và trang quản trị.

## Lưu ý dữ liệu

- **`data_attribute`** phải có id 0..186 liên tục (client tra theo vị trí; thiếu là đơ khi mở trang bị).
- **`data_item`**: không bao giờ `DELETE` dòng ở giữa (client ghi mảng theo id). Muốn bỏ vật phẩm thì đổi thành loại
  trơ, giữ dòng. Xoá template trong khi còn người giữ món đó là người đó không vào được game (`getTemplate()` trả
  `null`); vật phẩm người chơi nằm trong chuỗi cột `equip` / `inven` / `bag` của `tob_char`, dạng
  `<clazz>,<idTemplate>,<place>,...`.
- **`data_potion`** luôn đúng 162 dòng (`Char.SO_O_THUOC`, client cấp phát theo số này): thuốc mới `UPDATE` vào ô trống.
- Cột `xstart` của `data_item` là **icon trong hành trang**, độc lập với hình khi mặc (`atb7` / `atb8`). Dải icon
  800–806 đã bị chiếm (phục vụ từ `map/other/`), đừng gán `xstart` vào đó.
- Tệp ở đây là **LF** (`.gitattributes`). Dump trên Windows ra CRLF — đổi trước khi commit. Làm mới một bảng trong
  seed: dump bằng `--skip-extended-insert --complete-insert --no-create-info` rồi thay đúng khối `INSERT`.
