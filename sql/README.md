# sql — CSDL đầy đủ, bản seed và migration

## Dựng CSDL mới: một tệp

```sh
mysql -u root < sql/htbb_day_du.sql                         # tạo cả `account` lẫn `htbb_db`, đủ dữ liệu Pokémon
mysql -u root htbb_db < sql/them_chi_muc_production.sql     # chỉ mục cho bảng nóng (chạy lại được)
```

Cài cấu hình MariaDB (`vps/htbb-mariadb.cnf`: utf8, `lower_case_table_names=1`) **trước** khi nạp — xem
[docs/trien-khai.md](../docs/trien-khai.md). Có sẵn một tài khoản `admin`: đổi mật khẩu ngay sau khi nạp.

`htbb_day_du.sql` là **tệp sinh** (đừng sửa tay) bằng `sh tools/pokemon/xuat_sql_day_du.sh`: dựng lại trên MariaDB
trống theo đúng thứ tự `server/loginServer/account.sql` → `gamedb7_clean.sql` → `vps/db_new_server.sql` → migration
`them_*` / skin → các tệp `pokemon_*.sql` (`pokemon_thuat_ngu.sql` cuối cùng), chạy bằng `mysql -f` (lỗi "đã có" vô
hại). Sửa dữ liệu Pokémon thì sửa nguồn (`du_lieu/*.csv`, bộ sinh trong `tools/pokemon/`), sinh lại tệp `pokemon_*.sql`
rồi sinh lại `htbb_day_du.sql`. Server tự tạo thêm bảng theo kỳ và bảng phụ (`CREATE TABLE IF NOT EXISTS`) lúc chạy.

## Các tệp

| Nhóm | Tệp | Ghi chú |
|---|---|---|
| Seed gốc | `gamedb7_clean.sql` | bảng + dữ liệu game gốc, không có người chơi. Test đọc trực tiếp (`KiemBangThuocTinh`, `KiemDanhMucItem`) và `tools/SinhDanhMucItem.java` sinh `du_lieu/danh_muc_item.md` từ đây |
| Pokémon (sinh) | `pokemon_*.sql` | dữ liệu quái, chiêu, vật phẩm, chữ, thuật ngữ, nội dung, nhiệm vụ, dài hạn, hệ sinh thái, chỉ số, nạp tiền, thành viên; `pokemon_cau_hinh_trung_tam.sql` sinh từ `tools/cauhinh/danh_muc.json` |
| Thêm bảng | `them_bang_*.sql` | sự kiện (`tob_su_kien`), log online, nhật ký admin web, chặn spam web, cấm máy, 2FA, Pokédex, dài hạn, top, điều khiển server, `them_bang_web_lenh_nhan_vat.sql` (hàng đợi lệnh web → game; server và web cũng tự tạo bảng) |
| Thêm cột / khoá | `them_cot_*.sql`, `them_khoa_duy_nhat_giftcode_log.sql`, `them_chi_muc_production.sql`, `sua_cot_itemquest.sql` | chạy lại được; `sua_cot_itemquest.sql` cho CSDL dựng trước 19/10/2026 (cột `tob_char_quest.itemquest` 100 ký tự làm lưu nhiệm vụ thất bại trên MariaDB) |
| Sự kiện, vật phẩm | `them_su_kien_world_cup.sql`, `them_phi_phong_vo_hinh.sql`, `skin_linh_thu.sql`, `ten_mo_ta_skin_linh_thu.sql`, `sua_type_skin_linh_thu.sql`, `vo_hieu_giftcode_tt.sql`, `xoa_su_kien_trung_lap.sql` | đã nằm trong `htbb_day_du.sql`; giữ vì test và mã nguồn trỏ vào |
| Web CMS | `web_cms.sql` | bảng nội dung website (thiếu thì web dùng nội dung mặc định) |
| Thao tác tay | `wipe_player_data.sql`, `wipe_sau_open_test.sql` | xoá dữ liệu người chơi — **sao lưu trước** |

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
