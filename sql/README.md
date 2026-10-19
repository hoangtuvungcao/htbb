# sql — một tệp CSDL đầy đủ

Dữ liệu game nằm trong **CSDL** và được sửa trên trang quản trị. Thư mục này giữ bản chụp đầy đủ của CSDL đó:

| Tệp | Là gì |
|---|---|
| `htbb_day_du.sql` | **Một tệp đầy đủ**: cả `account` lẫn `htbb_db` — mọi bảng (schema), mọi dữ liệu thiết kế / cấu hình (vật phẩm, quái, nhiệm vụ, sự kiện, danh mục cấu hình kèm phân loại `lop`…), một tài khoản `admin`. Bảng người chơi chỉ có schema (rỗng). Nạp một lần là có CSDL chạy được, không cần tệp nào khác. |
| `nang_cap.sql` | Nâng một CSDL **đang chạy** (dựng từ bản đầy đủ cũ) lên đúng schema + dữ liệu máy chủ đọc lúc khởi động như `htbb_day_du.sql`. Chạy lại được, không đụng dữ liệu người chơi. Viết tay. |

Không còn bộ sinh trong luồng làm việc: các bảng tính CSV, bộ sinh Python / Java và các tệp SQL lẻ trước đây đã dùng **một
lần cuối** để dựng `htbb_day_du.sql`, rồi chuyển vào [`tools/luu_tru_sinh_du_lieu/`](../tools/luu_tru_sinh_du_lieu/README.md)
(chỉ để tra lịch sử).

## Máy mới

Cài cấu hình MariaDB **trước** (`vps/htbb-mariadb.cnf`: utf8, `lower_case_table_names=1`; xem
[docs/trien-khai.md](../docs/trien-khai.md)), rồi:

```sh
mysql -u root < sql/htbb_day_du.sql      # tạo account + htbb_db, xong
mysql -u root -e "UPDATE account.team_user SET password = PASSWORD('mật-khẩu-mạnh') WHERE username = 'admin';"
```

Tài khoản `admin` trong tệp có mật khẩu ngẫu nhiên không ai biết: đổi ngay như trên. Server tự tạo thêm bảng theo kỳ và
bảng phụ (`CREATE TABLE IF NOT EXISTS`) lúc chạy lần đầu.

## CSDL đang chạy (production): nâng cấp, không nạp lại

**Không** nạp `htbb_day_du.sql` lên CSDL có người chơi (tệp `DROP TABLE` rồi tạo lại). Dùng một lệnh:

```sh
sudo bash /opt/htbb/vps/nang_cap_csdl.sh
```

Script sao lưu (`vps/backup_db.sh`, hỏng là dừng) → nạp `sql/nang_cap.sql` → kiểm 10 bảng máy chủ đọc lúc khởi động,
số khoá và cột phân loại `lop` của danh mục cấu hình. `nang_cap.sql` gồm:

0. `CREATE TABLE IF NOT EXISTS` cho **mọi** bảng của `htbb_day_du.sql` (bảng đã có giữ nguyên, cả dữ liệu);
1. cột / bảng mới qua các bản (`ADD COLUMN IF NOT EXISTS`, nới cột `itemquest`, 2FA, cấm máy, nhật ký, Pokédex, dài hạn,
   client đăng nhập, chống clone, CMS web…);
2. `DROP` + `CREATE` + `INSERT` lại 8 bảng thiết kế máy chủ nạp lúc khởi động (`data_cap_do_lau_dai`,
   `data_nang_cap_lau_dai`, `data_kinh_te_lau_dai`, `data_su_kien_nam`, `data_hst_ho_cau`, `data_noi_dung_trang_bi`,
   `cau_hinh_danh_muc`, `cau_hinh_nhom`) — từng dòng giống `htbb_day_du.sql` (`server/test/KiemDuLieuSql` kiểm);
3. chỉ mục bảng nóng (`CREATE INDEX IF NOT EXISTS`);
4. cấu hình hệ thống chạy lại được: `5h_systems.tangnap = 1`, công tắc sự kiện của lịch năm (`tob_su_kien`).
5. tên người chơi thấy: `data_npc` (tên NPC) + `data_map_ten` (tên bản đồ) — thêm dòng thiếu, dòng đã có chỉ cập nhật
   cột kỹ thuật, **giữ tên admin đã sửa** ở `/admin/ten-hien`.
6. 0.9.6: chức năng NPC đúng (Heracross đứng ở Cao nguyên Indigo 201, `chuc_vu` Chansey / Persian / Meowth) và tên
   chiêu kiểu Pokémon ("Múa kiếm" → "Vũ điệu kiếm") — chỉ đổi dòng còn giá trị mặc định cũ.
7. khoá bản đồ (kiểu NRO / NSO): `data_ban_do_mo_khoa` (mỗi bản đồ gốc: cấp tối thiểu, cửa ải cần xong, có cho Tự động
   đánh không + lý do; `INSERT IGNORE` giữ dòng admin đã sửa), 12 cửa ải `CA-01`…`CA-12` (29 bước, loại `cua_ai`, trong
   `data_quest_lau_dai` / `data_nhiem_vu_noi_dung`), khoá cấu hình `ban_do.khoa.bat`, bảng `tob_ban_do_mo` (mốc miễn trừ
   người chơi cũ: `INSERT IGNORE … SELECT` theo cấp / bản đồ đang lưu, không sửa dòng có sẵn; server cũng tự điền lúc
   đăng nhập đầu). `server/test/KiemKhoaBanDo` kiểm.
8. vòng quay góp hũ (main 0cbf73ae): bảng `tob_vong_quay_*` (danh mục nhóm `vong_quay` ở mục 2) + nội dung CMS web
   0.9.6 (mô tả thẻ Linux tải game trong `web_cai_dat`).
9. thách đấu cược xu ký quỹ + phí, lì xì Tết có trần (main bda0e901): bảng `tob_gioi_han_qua`, `tob_thach_dau_ky_quy`
   (danh mục nhóm `cuoc_xu` ở mục 2).

Bảng người chơi chỉ được **thêm** bảng / cột / chỉ mục, không bị xoá hay sửa dòng. Không nằm trong script (quyết định
của chủ game, production đã chạy): xoá quà tạo nhân vật chưa nhận (`board_created`) và khoá giftcode kiểu cũ — bản gốc ở
`tools/luu_tru_sinh_du_lieu/sql_nguon/` (`sua_he_so_nap_va_qua_tao_nv.sql`, `vo_hieu_giftcode_cu.sql`), thu hồi quà đã
nhận ở `vps/van_hanh/thu_hoi_qua_tao_nv.sql`. Phải chạy **trước** khi jar mới khởi động, không thì máy chủ có thể dừng
với dòng `THIEU DU LIEU SQL QUAN TRONG - DUNG SERVER`.

Kiểm đã làm (19/10/2026): dump trước P1 + `nang_cap.sql` (chạy 2 lần) → xuất lại = `htbb_day_du.sql` (196 bảng cùng
schema, mọi bảng thiết kế cùng dữ liệu; bảng người chơi giữ nguyên số dòng + CHECKSUM).

## Xuất lại từ CSDL (đưa chỉnh sửa trên trang quản trị về repo)

```sh
sudo bash /opt/htbb/vps/xuat_csdl_day_du.sh /tmp/htbb_day_du.sql    # trên VPS (root, unix_socket)
# rồi chép về repo: sql/htbb_day_du.sql, xem git diff, commit
```

Script chạy `mysqldump` hai lượt: (1) `--no-data --databases account htbb_db` — schema mọi bảng; (2) `--no-create-info
--order-by-primary` — dữ liệu **chỉ** của bảng thiết kế / cấu hình: mọi bảng `data_*` cùng danh sách `BANG_DU_LIEU` ở đầu
script (`5h_systems`, `bad_word`, `cau_hinh`, `cau_hinh_danh_muc`, `cau_hinh_nhom`, `giftcode`, `nap_cau_hinh`,
`nap_cua_hang`, `nap_goi`, `tob_event`, `tob_fruit`, `tob_tubinh`…), rồi (3) thêm tài khoản `admin` mật khẩu ngẫu nhiên.
Bảng người chơi / nhật ký / đơn nạp chỉ có schema. Tương đương tay:

```sh
mysqldump -u root --skip-comments --skip-dump-date --no-tablespaces --single-transaction --no-data --databases account htbb_db > a.sql
mysqldump -u root --skip-comments --skip-dump-date --no-tablespaces --single-transaction --no-create-info --order-by-primary \
  htbb_db $(mysql -u root -N -e "SELECT table_name FROM information_schema.tables WHERE table_schema='htbb_db' AND table_name LIKE 'data\_%'") \
  5h_systems admin_msg bad_word cau_hinh cau_hinh_danh_muc cau_hinh_nhom cau_hinh_phien_ban cau_hinh_khoi_dong_lai \
  dieu_khien_server giftcode nap_cau_hinh nap_cua_hang nap_goi tob_event tob_event_world_cup_state tob_fruit tob_tubinh > b.sql
```

Thêm bảng / cột mới: tạo trong CSDL (máy chủ tự tạo hoặc qua trang quản trị), xuất lại `htbb_day_du.sql`, **và** thêm lệnh
chạy lại được tương ứng vào `nang_cap.sql` (`CREATE TABLE IF NOT EXISTS` / `ADD COLUMN IF NOT EXISTS`); bảng thiết kế mới
cần dữ liệu thì thêm tên vào `BANG_DU_LIEU` của `vps/xuat_csdl_day_du.sh`. `server/test/KiemDuLieuSql` chặn nếu
`nang_cap.sql` thiếu bảng của bản đầy đủ.

## Lưu ý dữ liệu

- **`data_attribute`** phải có id 0..186 liên tục (client tra theo vị trí; thiếu là đơ khi mở trang bị).
- **`data_item`**: không bao giờ `DELETE` dòng ở giữa (client ghi mảng theo id). Muốn bỏ vật phẩm thì đổi thành loại
  trơ, giữ dòng. Xoá template trong khi còn người giữ món đó là người đó không vào được game.
- **`data_potion`** luôn đúng 162 dòng (`Char.SO_O_THUOC`): thuốc mới `UPDATE` vào ô trống.
- Cột `xstart` của `data_item` là **icon trong hành trang** (icon Pokémon dải 14000+), độc lập với hình khi mặc.
- Tệp ở đây là **LF** (`.gitattributes`).
