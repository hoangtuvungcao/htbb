# tools — công cụ tài nguyên, kiểm thử và phát hành

Chạy **ở gốc repo** (đường dẫn trong công cụ viết từ gốc): Python `python3 -I tools/pokemon/<tên>.py`, Java
`javac -d /tmp/htbb-tools tools/<...>.java && java -cp /tmp/htbb-tools <Lớp>`. Dữ liệu game **không** còn sinh bằng công
cụ: nó nằm trong CSDL (sửa trên trang quản trị) và bản đầy đủ `sql/htbb.sql` — xem [sql/README.md](../sql/README.md).
Bộ sinh dữ liệu cũ (CSV → SQL, danh mục cấu hình, JSON tĩnh cho web) đã chạy lần cuối và nằm ở
[`luu_tru_sinh_du_lieu/`](luu_tru_sinh_du_lieu/README.md), không thuộc build. Luồng dữ liệu tổng thể:
[docs/kien-truc.md](../docs/kien-truc.md); lệnh build: [docs/build.md](../docs/build.md).

## `cauhinh/`

`vi_du_kiem.tsv`: bộ ví dụ kiểm giá trị cấu hình dùng chung cho test Java và web. Danh mục khoá cấu hình là bảng
`cau_hinh_danh_muc` + `cau_hinh_nhom` trong CSDL ([docs/cau-hinh.md](../docs/cau-hinh.md)).

## `do_trang_phuc/` — đồ họ mới và trang phục (0.9.8)

`danh_muc.py` là nguồn duy nhất (18 bộ hệ, 3 bộ huy chương vùng, thưởng bộ, ngân sách ô trang trí, 61 món trang trí và
nguồn của chúng); `hinh_trang_bi.py` / `hinh_trang_phuc.py` là hình vẽ tay bằng lưới ký tự (CC0). `sinh.py` ra
khối `-- >>> them_do_trang_phuc.sql` của `sql/htbb.sql`, icon `server/map/icon_do_moi/` + `web/public/icon-do/`, atlas lớp phủ
`client_jar/res/pk/trang_phuc.{png,bin}`, `khung_hiem.png`, `du_lieu/do_moi_*.csv`, `du_lieu/trang_phuc.csv`,
`web/shared/data/danh-muc-do-trang-phuc.json` (`--xem <thư mục>` = bộ sưu tập ảnh phóng to). `kiem_ke.py` ra
`du_lieu/KIEM_KE_TRANG_BI.md`.

## `pokemon/` — tài nguyên Pokémon

| Nhóm | Công cụ | Làm gì |
|---|---|---|
| Chữ và thuật ngữ | `ap_thuat_ngu.py`, `ap_thuat_ngu_web.py` | đổi chữ người chơi thấy trong mã nguồn / web từ kiếm hiệp sang Pokémon |
| Mô phỏng | `MoPhongKinhTe.java` | mô phỏng kinh tế / tiến trình, đọc bảng thiết kế trong `sql/htbb.sql` → `mo_phong_ra/` (không commit) |
| Sprite và hiệu ứng | `tai_sprite_pmd.py`, `tai_hieu_ung_pmd.py`, `tai_vat_the.py`, `DongGoiSprite.java`, `TinhNeoPoke.java`, `SinhPhuKien.java` | tải tài nguyên PMDCollab (vào `pmd_raw*/`, không đưa vào git), đóng gói cho client, điểm neo phụ kiện |
| Chuỗi chính | `sinh_sua_chuoi_chinh.py`, `ThemNpcLang.java` | in khối SQL sửa địa điểm / NPC giao của chuỗi chính (đọc CSDL + `server/cMap` + `server/map`) cho khối `them_huong_dan_tan_thu.sql` của `sql/htbb.sql` |
| Thay hình máy chủ cũ (P2) | `sinh_icon_thay_cu.py`, `ve_thu_pet_thay_cu.py`, `sinh_npc_sh.py`, `sinh_anh_quai_cu.py`, `ve_hieu_ung_thay_cu.py`, `ve_giao_dien_thay_cu.py`, `ve_vong_quay.py`, `xoa_hinh_nguoi_cu.py`, `p2_ban_do/*.py` | vẽ lại **tại chỗ** (giữ tên tệp, kích thước, bảng khung) mọi hình kiếm hiệp cũ còn có thể vẽ: icon dải cũ, ô dự phòng thú cưỡi / pet / skin, NPC, quái, hiệu ứng kiếm, danh hiệu, giao diện Unity, vòng quay; làm trong suốt bộ phận người / vũ khí paper-doll. Cần Pillow (chạy không kèm `-I`); chạy lại không đổi gì. Xem [docs/giay-phep.md](../docs/giay-phep.md) mục 7 |
| Icon trang phục trong ô trang bị | `them_icon_trang_phuc.py` | thêm icon PMD `server/map/icon_pokemon/503..526` (chân dung SpriteCollab / icon RawAsset, cùng phép thu nhỏ / đổi màu của bộ sinh cũ `luu_tru_sinh_du_lieu/pokemon/sinh_icon_pokemon.py`), dòng `du_lieu/bang_icon.csv`, `GHI_CONG.txt`; khối SQL `doi_icon_trang_phuc.sql` (test `KiemIconTrangBi`) |
| Kiểm tài nguyên (P2) | `kiem_tai_nguyen_cu.py` | không còn hình của máy chủ cũ (dấu vân tay `du_lieu/tai_nguyen_cu.txt`, giữ có lý do `du_lieu/tai_nguyen_cu_giu.csv`) và không thiếu tệp cho mọi dex / icon / mẫu pet / `d_effect` / đường dẫn jar được tham chiếu; `--ds tệp` ghi danh sách đầy đủ, `--sinh` dựng lại danh sách cũ từ lịch sử git. Test server `KiemTaiNguyenCu` chạy cùng luật |
| Sửa tuyến nhiệm vụ | `sinh_sua_tuyen.py` | in thân khối `-- >>> sua_tuyen_nhiem_vu.sql` của `sql/htbb.sql` từ CSDL đang chạy + `server/cMap` + bảng cổng `RealController` / `BanDoHoenn`: vùng đích bước địa điểm (ô đi được, liên thông với cổng, ba làng tương đương, bản đồ không tới được thì tắt) và chữ "Phần thưởng" sinh từ dòng thưởng (cột 1000) |
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
| `KichBanDauTruong.java` | Đấu trường Bảo Bối 3v3 (gói `-109`, `-Dban=908`): `dt_tran` hai bot có Hộp PC (nạp qua SQL) mở NPC Đấu trường → lưu đội tấn công / phòng thủ → tìm đối thủ → thách đấu → chọn chiêu, bỏ trống một lượt (máy chọn sau 20 giây), Tự động → kết quả; kiểm `tob_dau_truong_*`, lượng khoá, báo bên phòng thủ, hết 5 lượt / ngày; cần `-Ddb`, `-Ddbport` |
| `KichBanNhiemVu.java` | nhiệm vụ chính / phụ qua gói `-64` đúng như client thật: `nv_noi_chuyen` (nhiệm vụ nói chuyện client gửi thẳng "trả" → sang bước sau), `nv_nhan_kep` (chạm đúp, danh sách cũ), `nv_danh_quai` (nhận, trả sớm có lý do), `nv_huy` (nút Huỷ client 0.9.7 gói 52 và -64), `nv_phu` (phụ nói chuyện, không nhận lại, một phụ một lúc), `nv_dang_nhap_lai` |
| `KichBanHuongDan.java` | hướng dẫn tân thủ (khối `them_huong_dan_tan_thu.sql` của `sql/htbb.sql`): `hd_toan_bo` (tài khoản mới tinh: sân 105 → làng → menu Làm / Bỏ qua → đi bộ tới từng NPC theo cMap, 10 bước nhận / trả `-64`, ra đồng Pallet qua cổng, hạ Caterpie, uống thuốc, cộng điểm kỹ năng, mặc Ruy băng, thả câu ở ao làng → bàn giao chương 1), `hd_bo_qua` (xác nhận `-32` id 96, đủ thưởng, không bỏ qua lần hai), `hd_so_tay`, `hd_tien_do_cu`; cần `-Ddb`, `-Dnoi` |
| `KichBanSuaTuyen.java` | sửa tuyến (khối `sua_tuyen_nhiem_vu.sql` của `sql/htbb.sql`): `st_lang_vung` (tài khoản mới Kanto → Pewter, Johto → Blackthorn, NPC hướng dẫn đủ), `st_lang_cu` (nhân vật cũ giữ làng), `st_dia_diem` (GM thử cấp 20: C01.5 nghe Chansey = đang làm, chưa tới không trả được, đi bộ tới ô 53,19 ở Pallet = xong + thưởng); `hd_toan_bo_johto` = 10 bước hướng dẫn với nhân vật Johto; cần `-Ddb`, `-Ddbport`, `-Dnoi`, `sv.choPhepTaoTenGM=1` (máy thử) |
| `KichBanVuon.java` | vườn berry (khối `bat_lai_vuon.sql`, `VuonBerry`): `vuon_berry` — tài khoản mới tinh, không GM (GM chỉ `slv` cấp 7 + `tua 21m`, máy thử): gõ `hst` → Vườn nhà và bạn bè → Ô đất của tôi → gieo 2 Berry Oran (trừ xu hạt), bấm ô đang lớn không thu, đăng nhập lại cây còn, tua → thu hoạch tất cả (+6 Berry Oran), bước Sổ tay `P-01.2.3` xong (`tob_nhiem_vu_lau_dai`), đăng nhập lại ô trống / túi giữ 6 (`tob_hst_nhan_vat`), chế "Thuốc xịt từ vườn" (3 Oran → 1 Thuốc xịt), `tua 0` cuối; ~2 phút; cần `-Ddb`, `-Ddbport` |
| `KichBanCoPk.java` | khăn PK (`real.CoPkGame`): `cp_bat_tat` (đeo / tháo ngay, không tên đỏ), `cp_danh_roi_tat` (đánh người → chờ 10 giây có số giây, người bị đánh không đổi), `cp_vao_lai` (vào lại = đã tháo), `cp_ten_do` (nghiệp 300 trong `pInfo` → không đeo, có báo); cần `-Ddb`, `-Ddbport` |
| `KichBanBatThuong.java` | nhóm cổng kiểm `bat_thuong`: thao tác BẤT THƯỜNG của người chơi thường (không GM; GM chỉ `smn`/`spt`/`gem`/`slv`/`die` nhắm tên để dựng cảnh) - bấm đúp / bấm liền mua-bán-mặc-đập, số dư vừa đủ / thiếu 1 xu, túi đầy, rớt mạng giữa mua-bán-đổi map-giao dịch-nâng cấp, đăng nhập lại liên tục + hai phiên, rút đồ sau khi đối phương khoá, kết bạn không lời mời, khi gục, 136 gói sai trên 34 lệnh, đọc thư. Bất biến: tiền / thuốc / món bảo toàn (đối chiếu CSDL), không kẹt, máy chủ còn trả lời, không Exception mới (`-Dlog`). Ma trận phủ: `docs/kiem-thu/ma-tran-bot.md` |
| `BotXuyenSuot` | chơi xuyên suốt từ tài khoản mới theo nhiệm vụ; bot điều phối tua đồng hồ ảo (chỉ máy thử `sv.cho_phep_thu_nghiem=1`) |
| `BotNguoiChoi` | bot theo kiểu người chơi (cày, cốt truyện, sưu tầm, buôn bán, PvP, xã hội…) |
| `BotVaCham`, `KichBanHoiSinh` | kiểm va chạm bản đồ, hồi sinh |
| `KichBanPhoBanCup` | Phó bản Hầm ngục (vào phòng riêng, gói -112, lượt ngày, mất kết nối về đúng phòng, rời, hai phòng tách nhau) + Cúp PvP tuần (đăng ký, tua đồng hồ tới Chủ nhật 20:00, ghép Thuỵ Sĩ, trận, chốt hạng, thưởng một lần) — máy chủ thử có `sv.cho_phep_thu_nghiem=1`, chạy `-Dban=908` |
| `kiem_hoi_quy.sh` | chạy bộ hồi quy |
| `chay_hoi_quy.sh` + `hoi_quy.conf` | bộ hồi quy của **cổng kiểm** (`vps/staging/kiem-truoc-khi-len.sh`): `chay_hoi_quy.sh <host> <cổng game> <cổng login> <dbConn>` (`dbConn` = `cnf=<tệp mysql>,db=…,dbtk=…` hoặc `port=3399,db=htbb_db`) chạy từng nhóm trong `hoi_quy.conf` (một dòng một nhóm: lớp, giới hạn giây, `-D…`, kịch bản), mã thoát 0 = mọi nhóm DAT, kết quả ở `tong_ket.txt`. `BotChucNang` nhận thêm `-Ddbcnf=<tệp>` (`mysql --defaults-file`) và `-Ddbtk=<CSDL tài khoản>` (đổi mọi `account.` trong truy vấn kịch bản — staging dùng `htbb_staging_account`) |
| `HoiQuy` + `KichBanHoiQuy*.java` + `BatBien` + `BotHonLoan` | **người chơi thật** (nhóm `nguoi_choi_that` / `cot_truyen` / `hon_loan` của `hoi_quy.conf`): mỗi kịch bản một tài khoản **đăng ký mới qua đường trong game** (gói 39 `#dk2`, câu đố), không GM; pha chuẩn bị tách riêng (cấp qua GM `kt040` `slv`, xu / lượng / lượng khoá / kích hoạt / tuổi tài khoản trên CSDL khi đã thoát, giftcode cho người chơi tự nhập) ghi từng mục trong báo cáo. Mỗi lỗi người chơi báo 24/10 là một kịch bản (hướng dẫn mời lặp, mục tiêu nhiệm vụ sai bản đồ / số, cửa ải chưa nhận không đếm, Gym trong làng 0 sát thương / hiện lại / hai con, tàu New Bark kẹt 107, shop đặc biệt ô ẩn / nhãn lượng khoá, đăng ký chung IP, điểm lên cấp qua EXP nhiệm vụ, thú cưỡi, chọn Pokémon đã xoá). `BatBien` chạy kèm mọi bot: nạp lại map lặp, giật lùi, tiêu đề menu = tên quái, đường "A → B" khớp menu thật (`DuongMenu.kiem`), tiền không mất / lượng không tự tăng, nhiệm vụ làm được, không kẹt bản đồ, Exception trong log. `-Dkhoidonglai="<lệnh>"` kiểm lưu qua khởi động lại. `kiem_client_hoi_quy.sh [báo cáo]` = phần client headless (ChupTrongGame + KiemChamHud + KiemTocDoGame, hai đường vẽ) |

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
