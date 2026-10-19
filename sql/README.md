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
| Tên NPC & bản đồ | `pokemon_ten_hien.sql` | bảng `data_npc` (tên NPC tĩnh theo type) + `data_map_ten` (tên bản đồ theo id) — **nguồn duy nhất** của tên người chơi thấy (P6, không còn bảng tên trong Java); cùng khối nằm trong `htbb_day_du.sql`. Sửa ở web `/admin/ten-hien` (có nhật ký), game tự nạp lại ≤ 30 giây. Chạy lại được: thêm dòng thiếu, giữ tên đã sửa; `test/KiemTenHien` kiểm dữ liệu (có dấu, không từ cũ, đủ NPC / bản đồ) |
| Chức năng NPC | `pokemon_npc_chuc_nang.sql` | `data_npc`: Heracross (type 25) đứng ở Cao nguyên Indigo (`ban_do` 201 — 13 nhiệm vụ giao/trả ở Heracross), `chuc_vu` đúng chức năng thật (Chansey = Trung tâm Pokémon, Persian = bán đá quý, Meowth = ngân hàng). Chỉ đổi dòng còn giá trị mặc định cũ; chạy lại được. CSDL mới đã có sẵn trong `htbb_day_du.sql`; `test/KiemNpc` kiểm `ban_do` = NPC trên cMap |
| Điểm móc nhiệm vụ | `them_moc_nhiem_vu.sql` | `data_su_kien_vat_pham` (vật phẩm thật của từng sự kiện, lấy từ code rơi / thưởng; kiểu mục tiêu `su_kien_vat_pham` chỉ đếm vật phẩm của sự kiện đang bật), `tob_nhiem_vu_cho` (bán chợ lúc offline, áp vào nhiệm vụ `cho_ban` khi vào game, mỗi dòng một lần) (0.9.7). Idempotent; server cũng tự tạo bảng |
| PvP xếp hạng | `sua_pvp_pk.sql` | `tob_pvp_tran` (mọi trận xếp hạng, hợp lệ / huỷ + lý do), `tob_pvp_thuong` (thưởng cuối mùa đã phát, khoá (mùa, nhân vật) - không phát hai lần) (0.9.7). Idempotent, tự chứa; server cũng tự tạo |
| Danh hiệu | `them_danh_hieu.sql` | danh mục `data_danh_hieu` (+ độ hiếm), bộ đếm `tob_danh_hieu_dem`, nhật ký `tob_danh_hieu_log`, khoá `danh_hieu.*` (0.9.7). Idempotent, tự chứa (nối được vào `nang_cap.sql`); chạy lại không ghi đè `chi_so` / `phan_nghin` / `bat` đã chỉnh. Chưa có trong `htbb_day_du.sql`: chạy sau tệp đầy đủ |
| Độ hiếm (sinh) | `them_do_hiem.sql` | **tệp sinh** từ `tools/cauhinh/do_hiem.json` (`python3 tools/cauhinh/sinh_cau_hinh.py`): `data_do_hiem` (6 bậc Thường / Hiếm / Quý / Sử thi / Huyền thoại / Trang trí, màu chữ / nền / sáng / phụ kiện), `data_do_hiem_vat_pham` (colorName → bậc), `data_bac_trang_bi` (11 bậc), màu `data_danh_hieu_do_hiem`. Game và web đọc bản sinh trong mã nguồn (cùng nguồn); bảng để tra cứu / admin. Idempotent, chạy sau `them_danh_hieu.sql` |
| Thời tiết, mùa, ngày cộng đồng | `them_thoi_tiet_mua.sql` | `data_thoi_tiet` (7 thời tiết: hệ +/−% chiêu, kẹp ≤ 10%; hệ sinh nhiều), `data_thoi_tiet_map` (bản đồ cố định thời tiết / tắt màu mùa), `data_thoi_tiet_su_kien` (sự kiện đang bật thêm trọng số + màu phủ), `data_ngay_cong_dong` (dòng nổi bật 12 tháng × 2 năm xoay, giờ vàng, tỉ lệ ≤ 50%, EXP ≤ 25%), 17 danh hiệu 9801..9817. Idempotent, chạy sau `them_danh_hieu.sql`; thiếu bảng thì máy chủ dùng mặc định (ngày cộng đồng không có dòng nổi bật) |
| Theo dõi client | `them_bang_tob_client_dang_nhap.sql` | bảng `tob_client_dang_nhap` + cột client của `tob_vi_tri_online` / `tob_log_online` cho trang /admin/online; server game cũng tự tạo lúc khởi động; chạy lại được |
| Cập nhật client | `them_cap_nhat_client.sql` | 0.9.7: danh mục 3 khoá `sv.banToiThieu` / `sv.banMoiNhat` / `sv.nhacCapNhat` + trường `ban` (số bản) cho từng bản tải trong `web_cai_dat` khoá `taiGame`; không đặt giá trị nào, chạy lại được |
| Chống clone | `them_bang_chong_clone.sql` | bảng `tob_log_chuyen_gia_tri` (mọi lần chuyển xu / giá trị giữa hai người chơi, lần bị chặn, cờ "nghi clone") cho /admin/kinh-te; server game cũng tự tạo; chạy lại được |
| Lịch sự kiện năm | `them_lich_su_kien_nam.sql` | bảng `tob_su_kien_lich` + cột `tob_su_kien.tu_dong`, xoá mốc hẹn cũ theo tuần ISO, xoá dòng sự kiện kiếm hiệp đã bỏ; server game cũng tự tạo; chạy lại được |
| Kinh tế | `sua_he_so_nap_va_qua_tao_nv.sql` | `5h_systems.tangnap` về 1 (server đã cố định hệ số nạp = 1) và xoá quà tạo nhân vật chưa nhận trong `board_created`; không đụng số dư. Thu hồi quà đã nhận: `vps/van_hanh/thu_hoi_qua_tao_nv.sql` (tuỳ chọn, chủ game quyết) |
| Sửa logic 0.9.6.1 | `sua_096_1_logic.sql` | nhiều khối riêng, mỗi khối một lỗi; khối `[giap-vo-han]`: sàn hồi chiêu / PP chiêu hỗ trợ trong `data_noi_dung_chieu` (hồi chiêu > thời lượng + biên; server `real/ChieuHoTro` cũng tự chặn). Chỉ nâng, không hạ: seed hiện tại không đổi; chạy lại được; `test/KiemGiapVoHan` kiểm; khối `[cau-ca-nhip]`: đăng ký 10 khoá `cau_ca.*` (nhịp câu cá: chờ cá cắn, khung kéo cần, tỉ lệ không được gì, nghỉ, giảm theo giờ) vào danh mục cấu hình, `test/KiemCauCaNhip` kiểm |
| Vòng quay | `sua_vong_quay.sql` | bảng `tob_vong_quay_*` (cược đang chờ, tiền chờ nhận khi offline, giới hạn ngày / thiết bị, lịch sử lượt) + danh mục cấu hình nhóm `vong_quay` (mức cược theo dải cấp, trần ngày, phí hũ là hố hút). Server game cũng tự tạo bảng và hoàn cược đang chờ của hệ cũ (`nin_xoso*`) lúc khởi động; chạy lại được |
| Thách đấu cược, lì xì | `sua_vong_quay_2.sql` (sau `sua_vong_quay.sql`) | bảng `tob_gioi_han_qua` (trần nhận / mở bao lì xì, mã lì xì theo tài khoản / thiết bị / IP, quỹ lượng khoá) và `tob_thach_dau_ky_quy` (ký quỹ trận cược đang đánh; máy tắt giữa trận thì hoàn đủ) + danh mục cấu hình nhóm `cuoc_xu`; server game cũng tự tạo bảng; chạy lại được |
| Dọn dữ liệu cũ | `vo_hieu_giftcode_cu.sql` | khoá mọi mã của bảng `giftcode` kiểu cũ (game chế độ dài hạn chỉ đọc `tob_giftcode`); in danh sách mã còn mở rồi khoá, chạy lại được |
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
