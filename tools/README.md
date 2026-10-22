# tools — công cụ tài nguyên, kiểm thử và phát hành

Chạy **ở gốc repo** (đường dẫn trong công cụ viết từ gốc): Python `python3 -I tools/pokemon/<tên>.py`, Java
`javac -d /tmp/htbb-tools tools/<...>.java && java -cp /tmp/htbb-tools <Lớp>`. Dữ liệu game **không** còn sinh bằng công
cụ: nó nằm trong CSDL (sửa trên trang quản trị) và bản đầy đủ `sql/htbb_day_du.sql` — xem [sql/README.md](../sql/README.md).
Bộ sinh dữ liệu cũ (CSV → SQL, danh mục cấu hình, JSON tĩnh cho web) đã chạy lần cuối và nằm ở
[`luu_tru_sinh_du_lieu/`](luu_tru_sinh_du_lieu/README.md), không thuộc build. Luồng dữ liệu tổng thể:
[docs/kien-truc.md](../docs/kien-truc.md); lệnh build: [docs/build.md](../docs/build.md).

## `cauhinh/`

`vi_du_kiem.tsv`: bộ ví dụ kiểm giá trị cấu hình dùng chung cho test Java và web. Danh mục khoá cấu hình là bảng
`cau_hinh_danh_muc` + `cau_hinh_nhom` trong CSDL ([docs/cau-hinh.md](../docs/cau-hinh.md)).

## `pokemon/` — tài nguyên Pokémon

| Nhóm | Công cụ | Làm gì |
|---|---|---|
| Chữ và thuật ngữ | `ap_thuat_ngu.py`, `ap_thuat_ngu_web.py` | đổi chữ người chơi thấy trong mã nguồn / web từ kiếm hiệp sang Pokémon |
| Mô phỏng | `MoPhongKinhTe.java` | mô phỏng kinh tế / tiến trình, đọc bảng thiết kế trong `sql/htbb_day_du.sql` → `mo_phong_ra/` (không commit) |
| Sprite và hiệu ứng | `tai_sprite_pmd.py`, `tai_hieu_ung_pmd.py`, `tai_vat_the.py`, `DongGoiSprite.java`, `TinhNeoPoke.java`, `SinhPhuKien.java` | tải tài nguyên PMDCollab (vào `pmd_raw*/`, không đưa vào git), đóng gói cho client, điểm neo phụ kiện |
| Chuỗi chính | `sinh_sua_chuoi_chinh.py`, `ThemNpcLang.java` | in khối SQL sửa địa điểm / NPC giao của chuỗi chính (đọc CSDL + `server/cMap` + `server/map`) cho khối `them_huong_dan_tan_thu.sql` của `sql/nang_cap.sql` |
| Thay hình máy chủ cũ (P2) | `sinh_icon_thay_cu.py`, `ve_thu_pet_thay_cu.py`, `sinh_npc_sh.py`, `sinh_anh_quai_cu.py`, `ve_hieu_ung_thay_cu.py`, `ve_giao_dien_thay_cu.py`, `ve_vong_quay.py`, `xoa_hinh_nguoi_cu.py`, `p2_ban_do/*.py` | vẽ lại **tại chỗ** (giữ tên tệp, kích thước, bảng khung) mọi hình kiếm hiệp cũ còn có thể vẽ: icon dải cũ, ô dự phòng thú cưỡi / pet / skin, NPC, quái, hiệu ứng kiếm, danh hiệu, giao diện Unity, vòng quay; làm trong suốt bộ phận người / vũ khí paper-doll. Cần Pillow (chạy không kèm `-I`); chạy lại không đổi gì. Xem [docs/giay-phep.md](../docs/giay-phep.md) mục 7 |
| Kiểm tài nguyên (P2) | `kiem_tai_nguyen_cu.py` | không còn hình của máy chủ cũ (dấu vân tay `du_lieu/tai_nguyen_cu.txt`, giữ có lý do `du_lieu/tai_nguyen_cu_giu.csv`) và không thiếu tệp cho mọi dex / icon / mẫu pet / `d_effect` / đường dẫn jar được tham chiếu; `--ds tệp` ghi danh sách đầy đủ, `--sinh` dựng lại danh sách cũ từ lịch sử git. Test server `KiemTaiNguyenCu` chạy cùng luật |
| Bản đồ và tile | `DungBoTileMoi.java`, `VeMapMoi.java`, `GiaiMaMap.java`, `cai_map_j2me.sh` | dựng bộ tile mới, vẽ lại bản đồ `server/cMap`, giải mã bản đồ ra JSON/ảnh xem trước, cài bản đồ vào client J2ME. **Đừng chạy** `DoiTileMap.java` (công cụ cũ, ghi đè tile) |
| Đường dẫn client | `duong_client.py` | tìm thư mục `Assets` của client Unity (đặt `HTBB_CLIENT_ASSETS=client_pc_ios/Assets` nếu cần) |
| Chạy thử | `chay_tren_novadb.sh` | chạy toàn bộ server (NovaDB + login + game) trên máy dev; `dung` để tắt |

Giấy phép tài nguyên tải về: [docs/giay-phep.md](../docs/giay-phep.md).

### `pokemon/kiem_tai/` — bot kiểm tải và kiểm chức năng

Bot nói giao thức thật của server (khoá `-40`, đăng nhập, tạo / chọn nhân vật, đi lại, đánh quái, chat).

| Lớp | Việc |
|---|---|
| `BotTai` | kiểm tải: N bot (một phần là GM dùng `slv` / `mto`), mỗi 10 giây in số bot, rớt kết nối, gói tin, đòn đánh, quái hạ, RAM/CPU |
| `BotChucNang` + `KichBanDS*.java` | kiểm PASS/FAIL theo tính năng (đăng nhập → cổng mọi map → chiến đấu → vật phẩm → kinh tế → xã hội → hệ thống dài hạn) |
| `KichBanDungVatPham.java` | kịch bản `dung_vat_pham`: mua / nhận rồi dùng một vật phẩm mỗi loại, mọi lần bấm phải có phản hồi (`-Dban=902` = client gửi số bản, nhận thông báo nhanh `-104`) |
| `KichBanNPC.java` | NPC: `npc_tat_ca` mở mọi NPC (bot server, hoa tiêu, NPC tĩnh) và chọn mọi mục (không im lặng, không Exception trong log `-Dlog=`, không chữ cũ / mục đã gỡ); `npc_dich_vu` dịch vụ NPC tĩnh ở thị trấn ngoài trời (Chansey, Kecleon, Machamp, Bronzong, Meowth, Persian, Kangaskhan, Lapras, Pidgeot); `npc_su_kien` NPC sự kiện đang hiện; `npc_do` chỉ in bảng NPC → mục → trả lời (kiểm toán). Tài khoản GM: `sv.adminAccounts` |
| `KichBanNhiemVu.java` | nhiệm vụ chính / phụ qua gói `-64` đúng như client thật: `nv_noi_chuyen` (nhiệm vụ nói chuyện client gửi thẳng "trả" → sang bước sau), `nv_nhan_kep` (chạm đúp, danh sách cũ), `nv_danh_quai` (nhận, trả sớm có lý do), `nv_huy` (nút Huỷ client 0.9.7 gói 52 và -64), `nv_phu` (phụ nói chuyện, không nhận lại, một phụ một lúc), `nv_dang_nhap_lai` |
| `KichBanHuongDan.java` | hướng dẫn tân thủ (`sql/them_huong_dan_tan_thu.sql`): `hd_toan_bo` (tài khoản mới tinh: sân 105 → làng → menu Làm / Bỏ qua → đi bộ tới từng NPC theo cMap, 10 bước nhận / trả `-64`, ra đồng Pallet qua cổng, hạ Caterpie, uống thuốc, cộng điểm kỹ năng, mặc Ruy băng, thả câu ở ao làng → bàn giao chương 1), `hd_bo_qua` (xác nhận `-32` id 96, đủ thưởng, không bỏ qua lần hai), `hd_so_tay`, `hd_tien_do_cu`; cần `-Ddb`, `-Dnoi` |
| `BotXuyenSuot` | chơi xuyên suốt từ tài khoản mới theo nhiệm vụ; bot điều phối tua đồng hồ ảo (chỉ máy thử `sv.cho_phep_thu_nghiem=1`) |
| `BotNguoiChoi` | bot theo kiểu người chơi (cày, cốt truyện, sưu tầm, buôn bán, PvP, xã hội…) |
| `BotVaCham`, `KichBanHoiSinh` | kiểm va chạm bản đồ, hồi sinh |
| `KichBanPhoBanCup` | Phó bản Hầm ngục (vào phòng riêng, gói -112, lượt ngày, mất kết nối về đúng phòng, rời, hai phòng tách nhau) + Cúp PvP tuần (đăng ký, tua đồng hồ tới Chủ nhật 20:00, ghép Thuỵ Sĩ, trận, chốt hạng, thưởng một lần) — máy chủ thử có `sv.cho_phep_thu_nghiem=1`, chạy `-Dban=908` |
| `kiem_hoi_quy.sh` | chạy bộ hồi quy |

```sh
MYSQL=mysql sh tools/pokemon/chay_tren_novadb.sh                 # server thử
javac -encoding UTF-8 -d /tmp/bot tools/pokemon/kiem_tai/*.java
java -cp /tmp/bot -Dgm=10 BotTai 127.0.0.1 19129 50 600 tai      # 50 bot, 10 GM, 10 phút
```

Tài khoản bot tạo trước trong `account.team_user` (mật khẩu `PASSWORD(...)`) kèm dòng `htbb_db.5h_active`; bot GM cần
tên nhân vật trong `sv.adminAccounts` và `pInfo[16]=1` — chỉ làm trên server thử. `sv.adminAccounts` tối đa 255 ký tự
(cột `cau_hinh.gia_tri`): `BotChucNang` dùng GM `kt001..kt040` (`-Dden`, mặc định 40) và tài khoản thường `kt101..kt120`;
tài khoản thử nên có `regdate` lùi ≥ 24 giờ (chống clone) — bot tự cộng giờ chơi `tob_char.totalTimePlay` khi có `-Ddb`. Sau khi chạy: log server không có
`Exception`, dữ liệu đã lưu (`tob_char`, `tob_pokedex`). Máy dùng chung: ≤ 100 bot, server headless.

## `phat-hanh/` — phát hành client

`phat-hanh.sh` (build jar / Android / PC và đẩy lên GitHub Releases), `day-len.sh`, `phienban.py`
(`phienban.json`), `nen-zip.py`; cấu hình `phien_ban.conf` (trong git) và `may_chu.conf` (không trong git, mẫu
`may_chu.conf.example`). Chi tiết: [phat-hanh/README.md](phat-hanh/README.md), [docs/build.md](../docs/build.md).

## `doi_ten/` — đổi tên mã nguồn

Công cụ đổi tên lớp / định danh kiếm hiệp sang tên Pokémon (`doi_ten_lop.tsv` là bảng ánh xạ) và gộp monorepo.
Thuật ngữ chuẩn: [docs/thiet-ke-game.md](../docs/thiet-ke-game.md).

## Công cụ lẻ ở `tools/`

| Công cụ | Làm gì |
|---|---|
| `TaoDataSprite.java`, `CatSpriteSheet.java`, `DichSkinThu.java`, `CanNgangSkinThu.java`, `CanChoNgoiSkinThu.java`, `ThuChoNgoiSkinThu.java`, `XemSkinNhuTrongGame.java`, `TaoIconSkinThu.java`, `TaoPetLan*.java`, `TaoPetTrangThai1.java` | dữ liệu khung sprite trong `server/map/d_effect/`, căn chỉnh và xem trước thú cưỡi / skin |
| `DoiChuoiClass.java`, `va_mypet_client.py` | vá chuỗi trong tệp `.class` / jar client |
| `xem_sprite.html` | xem sprite trong trình duyệt |
