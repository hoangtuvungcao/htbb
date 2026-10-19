# tools — công cụ sinh dữ liệu, tài nguyên và phát hành

Chạy **ở gốc repo** (đường dẫn trong công cụ viết từ gốc): Python `python3 -I tools/pokemon/<tên>.py`, Java
`javac -d /tmp/htbb-tools tools/<...>.java && java -cp /tmp/htbb-tools <Lớp>`. Đừng sửa tay tệp do công cụ sinh ra —
sửa nguồn (`du_lieu/*.csv`, `tools/cauhinh/danh_muc.json`…) rồi chạy lại. Luồng dữ liệu tổng thể:
[docs/kien-truc.md](../docs/kien-truc.md); lệnh build: [docs/build.md](../docs/build.md).

## `cauhinh/` — cấu hình trung tâm

`danh_muc.json` là nguồn duy nhất của mọi khoá cấu hình chỉnh được từ web. `sinh_cau_hinh.py` sinh
`sql/pokemon_cau_hinh_trung_tam.sql` (bảng `cau_hinh_danh_muc` + `cau_hinh_nhom`: máy chủ game và web đọc danh mục từ
hai bảng này lúc chạy, không còn bản Java), `web/shared/utils/cauHinhTrungTam.gen.ts` (bản dự phòng của web) và bảng
khoá trong [docs/cau-hinh.md](../docs/cau-hinh.md) (`--kiem` = chỉ kiểm lệch). Sửa danh mục: chạy script, nạp tệp SQL
vào CSDL, rồi `python3 -I tools/pokemon/sinh_them_p1.py` để làm mới migration. `vi_du_kiem.tsv`: bộ ví dụ kiểm giá trị dùng chung cho test Java và web.

## `pokemon/` — dữ liệu và tài nguyên Pokémon

| Nhóm | Công cụ | Làm gì |
|---|---|---|
| CSDL đầy đủ | `xuat_sql_day_du.sh` | dựng lại `sql/htbb_day_du.sql` (account + htbb_db) trên MariaDB trống theo đúng thứ tự migration |
| Migration P1 | `sinh_them_p1.py` (`--kiem` = chỉ kiểm lệch) | `sql/them_p1_du_lieu_sql.sql`: chép nguyên khối 8 bảng máy chủ nạp lúc khởi động (docs/kien-truc.md mục 4.4) cho CSDL đang chạy |
| Bảng nội dung | `sinh_bang_quai.py`, `sinh_bang_thu.py`, `sinh_noi_dung.py` | quái / thú / nội dung (trang bị, vật phẩm, Gym, cửa hàng…) → `du_lieu/*.csv` |
| SQL từ bảng | `sinh_sql_lau_dai.py` (+ `lau_dai_cot_truyen.py`), `sinh_vat_pham_lau_dai.py`, `sinh_sql_he_sinh_thai.py`, `sinh_sql_vat_pham.py`, `SinhSqlChiSo.java`, `sinh_giftcode_an_toan.py` | `du_lieu/*.csv` → `sql/pokemon_*.sql` |
| Nhiệm vụ | `sinh_nhiem_vu.py` (+ `nv_chung.py`, `nv_cot_truyen.py`, `nv_hoenn.py`, `nv_phu.py`, `nv_lap_lai.py`) | nhiệm vụ → `sql/pokemon_nhiem_vu.sql`, `du_lieu/nhiem_vu_moi.csv`, báo cáo `du_lieu/NHIEM_VU_THIET_KE.md` |
| Chữ và thuật ngữ | `sinh_sql_chu.py`, `sinh_sql_thuat_ngu.py`, `ap_thuat_ngu.py`, `ap_thuat_ngu_web.py`, `quet_tu_cam.py`, `chu_pokemon.py` | đổi chữ người chơi thấy từ kiếm hiệp sang Pokémon (CSDL, mã nguồn, web); quét từ cấm còn sót |
| Kiểm dữ liệu | `kiem_item_mo_coi.py` | mọi vật phẩm phải có chỗ dùng → `du_lieu/KIEM_ITEM.md`, `du_lieu/kiem_item.csv` |
| Mô phỏng | `MoPhongKinhTe.java`, `MoPhongChiSo.java` | mô phỏng kinh tế / tiến trình và chỉ số / TTK → `du_lieu/mo_phong_*` |
| Sprite và hiệu ứng | `tai_sprite_pmd.py`, `tai_hieu_ung_pmd.py`, `tai_vat_the.py`, `DongGoiSprite.java`, `TinhNeoPoke.java`, `SinhPhuKien.java`, `sinh_icon_pokemon.py` | tải tài nguyên PMDCollab (vào `pmd_raw*/`, không đưa vào git), đóng gói cho client, điểm neo phụ kiện, icon vật phẩm |
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
| `BotXuyenSuot` | chơi xuyên suốt từ tài khoản mới theo nhiệm vụ; bot điều phối tua đồng hồ ảo (chỉ máy thử `sv.cho_phep_thu_nghiem=1`) |
| `BotNguoiChoi` | bot theo kiểu người chơi (cày, cốt truyện, sưu tầm, buôn bán, PvP, xã hội…) |
| `BotVaCham`, `KichBanHoiSinh` | kiểm va chạm bản đồ, hồi sinh |
| `kiem_hoi_quy.sh` | chạy bộ hồi quy |

```sh
MYSQL=mysql sh tools/pokemon/chay_tren_novadb.sh                 # server thử
javac -encoding UTF-8 -d /tmp/bot tools/pokemon/kiem_tai/*.java
java -cp /tmp/bot -Dgm=10 BotTai 127.0.0.1 19129 50 600 tai      # 50 bot, 10 GM, 10 phút
```

Tài khoản bot tạo trước trong `account.team_user` (mật khẩu `PASSWORD(...)`) kèm dòng `htbb_db.5h_active`; bot GM cần
tên nhân vật trong `sv.adminAccounts` và `pInfo[16]=1` — chỉ làm trên server thử. Sau khi chạy: log server không có
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
| `SinhDanhMucItem.java` | `du_lieu/danh_muc_item.md` từ `data_item` trong `sql/gamedb7_clean.sql` |
| `SinhBangCapDo.java`, `SinhDanhMucDanhHieu.java`, `SinhDanhMucNgoc.java`, `SinhDanhMucThu.java` | `web/shared/data/*.json` từ chính mảng / hàm trong mã server |
| `SinhSqlTenSkinThu.java` | `sql/ten_mo_ta_skin_linh_thu.sql` từ `real.SkinThuCuoi` |
| `TaoDataSprite.java`, `CatSpriteSheet.java`, `DichSkinThu.java`, `CanNgangSkinThu.java`, `CanChoNgoiSkinThu.java`, `ThuChoNgoiSkinThu.java`, `XemSkinNhuTrongGame.java`, `TaoIconSkinThu.java`, `TaoPetLan*.java`, `TaoPetTrangThai1.java` | dữ liệu khung sprite trong `server/map/d_effect/`, căn chỉnh và xem trước thú cưỡi / skin |
| `DoiChuoiClass.java`, `va_mypet_client.py` | vá chuỗi trong tệp `.class` / jar client |
| `xem_sprite.html` | xem sprite trong trình duyệt |
