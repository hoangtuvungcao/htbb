# `sql/` — bản seed và các file migration

## Dựng server mới (Huyền Thoại Bảo Bối): MỘT file

```
mysql -uroot < sql/htbb_day_du.sql        # tạo cả `account` lẫn `kpah_db`, đủ dữ liệu Pokémon
```

`htbb_day_du.sql` là **file sinh ra** (đừng sửa tay) bằng `tools/pokemon/xuat_sql_day_du.sh`. Script dựng
lại từ đầu trên một MariaDB trống theo đúng thứ tự: `loginServer/account.sql` → `gamedb7_clean.sql` →
`vps/db_new_server.sql` → các migration `them_*`/skin → các file `pokemon_*.sql` (`pokemon_doi_chu.sql`
cuối cùng). Đã kiểm: nạp lại file vào MariaDB trống cho checksum khớp cả 102 bảng. Chạy lại script
chỉ khác các mốc thời gian `NOW()` của tài khoản admin.

Các file lẻ bên dưới **vẫn giữ** vì test/tool/trang quản trị đọc trực tiếp (xem cột "Ai đọc" khi
`grep` tên file). Ngày 2026-10-16 đã xoá 3 file không ai đọc mà nội dung đã có sẵn trong seed hoặc trong
`htbb_day_du.sql`: `them_bang_cau_hinh.sql`, `them_cot_soluong_log_buy_shop.sql`,
`them_cot_userid_giftcode_log.sql`.

Đối chiếu với production ngày **02/09/2026** (bằng bản sao DB của VPS nạp vào máy local).

## Đừng xoá thư mục này khi migration "đã áp dụng xong"

"Đã áp dụng vào production" **không** phải tiêu chí để xoá. Các file ở đây còn là **nguồn
schema** cho 19 chỗ đọc:

- `tools/SinhDanhMucItem.java` sinh `docs/danh_muc_item.md` từ `gamedb7_clean.sql`
- `test/KiemDanhMucItem.java`, `test/KiemBangThuocTinh.java` đối chiếu lại chính file đó
- `test/KiemSkinLinhThu.java` đọc `skin_linh_thu.sql`, `ten_mo_ta_skin_linh_thu.sql`,
  `sua_type_skin_linh_thu.sql`
- `test/KiemBangMaNguon.java` quét cả `sql/` tìm lỗi bảng mã
- `web/admin/test/kiem_schema.php` đọc 6 file `them_bang_*.sql` ở đây + `vps/db_new_server.sql`

Xoá một file là hỏng những chỗ đó, hoàn toàn không liên quan tới việc migration đã chạy chưa.

## Bản seed

| File | Vai trò |
|---|---|
| `gamedb7_clean.sql` | **Bản seed dựng server mới**: 93 bảng + toàn bộ dữ liệu game, không có nhân vật/tài khoản nào. Dùng theo `docs/HUONG_DAN_BUILD_VPS.md`. |

Làm mới từ VPS ngày 02/09/2026: `data_item` 904 → **914** dòng, `data_attribute` 141 → **198**
dòng, `data_potion` khớp production **từng byte** (162 dòng, gồm cả 106-110 của hai sự kiện
Vu Lan / World Cup), và thêm 9 bảng (7 bảng nhóm `them_bang_*` + 2 bảng World Cup). Các bảng
khác là bản chụp cũ nhưng đã đối chiếu khớp production, **trừ hai chỗ**:

- `giftcode` vẫn 14 dòng (production có 32)
- `tob_tubinh` vẫn 0 dòng (production có 16; `vps/db_new_server.sql` PHẦN 2B nạp)

Bản seed có **93** bảng còn production có **99**. Trong 12 bảng production có mà seed không có,
8 bảng là bảng theo kỳ do server tự tạo khi chạy (`tob_log_use_luong_*`, `tob_nap_xai_luong_*`,
`tob_char_thach_dau_*`, `tob_qua_loi_dai_*` — xem `Database.createTable`); 4 bảng còn lại:
`tob_nap_moc` và `history_win_xoso` do `vps/db_new_server.sql` tạo, `tob_train_luong` do
`Database.java` tự `CREATE TABLE IF NOT EXISTS`, còn `tob_skin_thu` là bảng mồ côi trên
production — không một dòng code nào nhắc tới. Ngược lại seed còn giữ 6 bảng log
`tob_log_use_luong_*_2025` đã hết kỳ, vô hại. Nên **bản seed một mình chưa đủ** để dựng server
mới: vẫn phải chạy `vps/db_new_server.sql` sau khi nạp seed.

## Migration — thêm bảng (đã áp dụng, vẫn cần cho server mới)

| File | Trạng thái trên production |
|---|---|
| `them_bang_su_kien.sql` | ✅ đủ, gồm cả 2 cột lịch hẹn — 33 dòng |
| `them_bang_tob_log_online.sql` | ✅ đang ghi (973 dòng) |
| `them_bang_web_admin_log.sql` | ✅ đang ghi (318 dòng) |
| `them_bang_web_chan_spam.sql` | ✅ đang ghi (25 dòng) |
| `them_bang_cam_may.sql` | ✅ có bảng, 0 dòng (chưa ai bị cấm máy) |
| `them_bang_2buoc.sql` | ✅ có bảng, 0 dòng (chưa ai bật 2FA) |
| `them_bang_web_lenh_nhan_vat.sql` | 🆕 02/09/2026 — hàng đợi lệnh web → server game (đặt cấp độ cho nhân vật đang online). Server (`data/LenhWeb.java`) và web (`inc/lenh_nhan_vat.php`) đều tự `CREATE TABLE IF NOT EXISTS`, không cần chạy tay; **chưa có trong seed** |

Từ bản làm mới ngày 02/09/2026, 7 bảng đầu **đã có trong bản seed**, nên dựng server mới từ
seed không cần chạy lại chúng (bảng `web_lenh_nhan_vat` thêm sau, server tự tạo lúc khởi động). Vẫn giữ file vì `web/admin/test/kiem_schema.php` parse chúng để
kiểm schema của DB thật, và các trang admin (`cammay.php`, `sukien.php`, `nhatky.php`) trỏ người
vận hành tới đây khi thiếu bảng. Muốn bỏ hẳn thì phải đổi `kiem_schema.php` sang đọc seed trước.

## Migration — sự kiện & vật phẩm

| File | Trạng thái trên production |
|---|---|
| `them_su_kien_world_cup.sql` | ✅ đã áp dụng (potion 108 + 2 bảng). Bản seed đã hấp thụ cả ba. Giữ vì 4 file Java, `test/KiemSuKienWorldCup.java`, `web/admin/sukien.php` và docs trỏ vào. |
| `them_phi_phong_vo_hinh.sql` | ⚠️ **áp dụng một phần**: item 910 có, nhưng giftcode `canhvohinh` **chưa có**. |
| `skin_linh_thu.sql` | ✅ 5 dòng, type 30, xstart 860-864. Bản seed đã hấp thụ (id 905-909). |
| `ten_mo_ta_skin_linh_thu.sql` | ✅ tên đã kèm mô tả `[hệ …]`. Bản seed đã hấp thụ. |
| `sua_type_skin_linh_thu.sql` | ✅ đã áp dụng, **giờ là no-op**: điều kiện `AND type = 19` không còn khớp dòng nào (cả 5 đã là type 30). Giữ vì `SkinLinhThu.java` trỏ vào nó làm hướng dẫn khi đổi `TYPE_ITEM`. |

**`data_shop` id 180/181 (thời trang Bóng đá nam/nữ, ngày 02/09/2026):** hai dòng này đã có sẵn
trên production khi kiểm tra — không có migration nào trong repo tạo ra chúng, và lịch sử code
không nhắc tới id 180/181 trước ngày đó. Chưa rõ nguồn gốc (khả năng là dữ liệu cũ từ trước khi
repo này bắt đầu, hoặc thao tác tay qua trang quản trị). Đã nối `Map.doChangeInfoShopEvent` vào
hai id có sẵn đó để bật/tắt cờ `sell` theo `Char.isSuKienVuLan2015()` — xem
`Map.SHOP_BONG_DA_NAM/NU` — mà **không** ghi đè tên/giá/ảnh đang có. Bản seed cũng đã cập nhật
hai dòng này để khớp production.

## Icon vật phẩm — kiến thức giữ lại từ 2 file đã xoá

Hai file `sua_icon_ao_brazin.sql` và `sua_icon_dai_800_806.sql` đã xoá ngày 02/09/2026: cả hai
đã áp dụng vào production, bản seed đã hấp thụ kết quả, và **không còn gì tham chiếu**. Phần
kiến thức của chúng giữ lại ở đây (phần cơ chế đã chuyển vào comment ngay tại
`src/data/DataGame.java`, chỗ kiểm dải 800-806).

**Cột `xstart` là icon trong hành trang, độc lập với hình khi mặc lên người.** Hình lúc mặc lấy
từ `atb7`/`atb8` (`map/c/body/<n>`, `map/c/leg/<n>`), không liên quan gì tới `xstart`. Nên một
món có thể mặc lên trông đúng mà trong hành trang lại hiện sai icon — đúng lỗi của item 609/610
"áo brazin": cả hai ghi `xstart = 519`, mà `map/icon/519.png` là **cái rương vàng 16x16** (icon
của item 611), nên hai cái áo hiện ra hình cái rương. Cả hai bản dump đều ghi 519, tức lỗi dữ
liệu có từ lâu. Đã đổi sang `map/icon/854.png` (nam) và `826.png` (nữ), 18x18, ghép từ chính ảnh
bộ đồ có sẵn trong repo — **không vẽ tay**.

**Dải icon 800-806 bị chiếm — đừng gán `xstart` vào đó.** Dải này không đọc
`map/icon/<id>.png` mà phục vụ từ `map/other/0..5.png`; id 806 thì server gửi gói **không có
byte ảnh nào**, khiến client NPE mọi lần vẽ icon cho tới khi tắt hẳn. Bảy vật phẩm 872-878 từng
trỏ vào dải này, đã đổi sang `map/icon/815-819, 827, 828`. Chuỗi lỗi đầy đủ nằm trong comment ở
`src/data/DataGame.java`.

**Lưu ý về `data_potion`:** `SELECT COUNT(*)` phải luôn là **162**. `Char.SO_O_THUOC` và mảng
`listPotion` của client đều cấp phát theo con số này — thêm/bớt dòng là lệch cả hai bên.
Vì vậy thuốc mới **không** `INSERT` mà `UPDATE` vào ô trống sẵn có (kiến thức giữ lại từ
`them_su_kien_vu_lan_2026.sql`, đã xoá): 106 nhánh hoa hồng vàng, 107 hoa hồng đỏ, 109 bó hoa
vàng (giao dịch được), 110 bó hoa đỏ là bốn thuốc sự kiện Vu Lan; 108 là hộp quà World Cup. Bốn
thuốc Vu Lan dùng tạm `idImage = 45` (ảnh hoa hồng của 58-61, chưa có ảnh riêng) và được
`LoginHandler.POTION_SHOP` ánh xạ sang `data_shop` 87/88/90/91 — bốn dòng shop đó có `decript`
rỗng nên mượn được; ai điền `decript` cho chúng là mô tả bốn thuốc này hiện nhầm. Hằng số ở
`src/real/Potion.java` (`TYPE_NHANH_HOA_HONG_VANG`…), menu ở `MapSuKien.doMenuDauTruongVuLan`.

## Chưa áp dụng / thao tác tay

| File | Trạng thái |
|---|---|
| `wipe_player_data.sql` | Thao tác tay (xoá dữ liệu người chơi). Đã bị `vps/db_new_server.sql` thay thế cho việc dựng server mới. **Giữ**: `web/admin/inc/vatpham.php` trỏ vào ghi chú trong file này để giải thích cách item được serialize vào 3 cột. |

## Đã xoá ngày 02/09/2026

| File | Vì sao xoá được |
|---|---|
| `go_skin_linh_thu.sql` | Header khẳng định sai rằng tính năng skin linh thú "đã gỡ" — thực tế đã làm lại trên đúng id 905-909. Câu lệnh trong đó dùng bảng `char` (schema thật là `tob_char`) nên sẽ lỗi. Kiến thức giữ ở mục "Cách xoá một template vật phẩm" bên dưới. |
| `sua_icon_ao_brazin.sql` | Đã áp dụng, bản seed đã hấp thụ, không còn gì tham chiếu. Kiến thức giữ ở mục "Icon vật phẩm" bên trên. |
| `sua_icon_dai_800_806.sql` | Như trên. Phần cơ chế đã chuyển vào comment ở `src/data/DataGame.java`. |
| `buff_ddsa.sql` | **Chưa từng áp dụng**: không một giftcode nào trong ~40 cái của nó tồn tại trên production. Là file buff cho một nhân vật cụ thể, không phải migration. Nội dung còn bị lỗi bảng mã (từng nằm trong danh sách bỏ qua của `test/KiemBangMaNguon.java`). |
| `them_su_kien_vu_lan_2026.sql` | Đã áp dụng; bản seed làm mới ngày 02/09/2026 có khối `data_potion` khớp production từng byte (đã `diff` bản dump), nên 4 dòng 106/107/109/110 đã nằm trong seed. Không còn file nào tham chiếu. Kiến thức giữ ở mục "Lưu ý về `data_potion`" bên trên. |

## Cách xoá một template vật phẩm cho an toàn

Kiến thức này giữ lại từ `go_skin_linh_thu.sql` (đã xoá file đó ngày 02/09/2026 vì header của
nó khẳng định sai rằng tính năng skin linh thú "đã được gỡ" — thực tế đã **làm lại** trên đúng
id 905-909, xem `SkinLinhThu.java:79`; và câu truy vấn trong đó dùng bảng `char` trong khi
schema thật là `tob_char`).

**Xoá template trong khi còn người chơi giữ món đó = họ không vào được game.**
`Item.getTemplate()` trả `null` khi không thấy template, mà hầu hết chỗ gọi thẳng
`item.getTemplate().type` → ném NullPointerException ngay lúc nạp nhân vật.

Vật phẩm của người chơi **không** nằm ở bảng riêng — nó được nối chuỗi vào cột `equip` /
`inven` / `bag` của `tob_char` theo dạng `<clazz>,<idTemplate>,<place>,...` (xem
`Item.getDBInfo`). Nên không thể xoá bằng một câu `DELETE` gọn gàng.

Đếm xem còn ai giữ không (lọc rộng, ra 0 dòng là chắc chắn sạch):

```sql
SELECT COUNT(*) FROM tob_char
WHERE equip REGEXP '(^|\\|)[0-9-]+,(905|906|907|908|909),';
```

*Tại thời điểm 02/09/2026 câu này trả về **5** — tức vẫn còn người giữ.*

Nếu vẫn còn người giữ, hai lựa chọn đều an toàn hơn xoá:

1. **Cứ để nguyên template.** Vật phẩm thành một món type thường không cộng chỉ số nào. Không
   ai NPE, chỉ hơi thừa một dòng trong `data_item`.
2. Đổi tên cho rõ là đã bỏ, rồi thu hồi dần bằng tay:
   ```sql
   UPDATE data_item SET name = CONCAT('[ngung dung] ', name) WHERE id BETWEEN 905 AND 909;
   ```

## Quy ước

- Tất cả file ở đây là **LF** (`.gitattributes`: `*.sql text eol=lf`). `mysqldump.exe` trên
  Windows xuất CRLF — phải chuyển về LF trước khi commit.
- `gamedb7_clean.sql` là file được **đọc bởi test**; sửa nó thì chạy lại `test.bat`.
- Làm mới một bảng trong bản seed: dump bảng đó từ production bằng
  `--skip-extended-insert --complete-insert --no-create-info`, tách phần tuple, rồi thay đúng
  khối `INSERT` tương ứng. Đừng để `mysqldump` tự escape lại — nó làm đúng, đừng viết tay.
