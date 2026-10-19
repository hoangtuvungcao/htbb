# tools/luu_tru_sinh_du_lieu — LƯU TRỮ, không thuộc build

Từ 19/10/2026 dữ liệu game nằm trong **CSDL** (sửa trên trang quản trị) và repo giữ **một tệp đầy đủ**
[`sql/htbb_day_du.sql`](../../sql/README.md). Thư mục này chứa bộ sinh và tệp nguồn **cũ** đã dùng để dựng tệp đó — chạy
**lần cuối** ngày 19/10/2026 (`pokemon/xuat_sql_day_du.sh`, có thêm `web_cms.sql` + `them_bang_top_boss.sql`), rồi xuất
lại bằng `vps/xuat_csdl_day_du.sh`. Không test, workflow, script triển khai hay mã máy chủ / web nào đọc thư mục này.

**Đừng sửa dữ liệu ở đây** — sửa trong CSDL rồi xuất lại `sql/htbb_day_du.sql` (xem `sql/README.md`); thêm bảng / cột thì
thêm lệnh chạy lại được vào `sql/nang_cap.sql`. Giữ lại chỉ để tra cứu: con số / công thức thiết kế ban đầu đến từ đâu.

| Thư mục | Nội dung cũ |
|---|---|
| `sql_nguon/` | seed `gamedb7_clean.sql`, `db_new_server.sql`, migration `them_*` / `sua_*` / skin, tệp sinh `pokemon_*.sql`, `web_cms.sql` (+ README cũ) |
| `du_lieu/` | bảng tính thiết kế đã thành bảng CSDL (`noi_dung_*.csv` → `data_noi_dung_*`, `nhiem_vu_moi.csv` → `data_quest_lau_dai` + `data_nhiem_vu_noi_dung`, `su_kien_nam.csv` → `data_su_kien_nam`…), bảng phụ của bộ sinh (`bang_chu.csv`, `bang_vat_pham.csv`, `chi_so_trang_bi.csv`, `bang_cap_do_lau_dai.csv`, `nhiem_vu_nhip_do.csv`), báo cáo sinh (`KIEM_ITEM.md`, `kiem_item.csv`, `danh_muc_item.md`, `mo_phong_*`, `tu_cam_con_lai.csv`), `web/` (bảng tóm tắt cho website) |
| `pokemon/` | bộ sinh Python (`sinh_*.py`, `nv_*.py`, `chu_pokemon.py`, `lau_dai_cot_truyen.py`, `quet_tu_cam.py`, `kiem_item_mo_coi.py`, `sinh_nang_cap_csdl.py`), `SinhSqlChiSo.java`, `MoPhongChiSo.java`, `xuat_sql_day_du.sh` |
| `cauhinh/` | `danh_muc.json` + `sinh_cau_hinh.py` (danh mục cấu hình nay là bảng `cau_hinh_danh_muc` / `cau_hinh_nhom`) |
| `java/` | `Sinh*.java` (sinh JSON tĩnh cho web, `danh_muc_item.md`, SQL tên skin) |
| `web_scripts/` | `sinh-du-lieu-pokemon.mjs`, `dong-bo-cam-nang.mjs` (sinh `web/shared/data/*.json` từ CSV cũ) |

Các tệp do bộ sinh cũ tạo mà vẫn nằm trong luồng chính (`web/shared/data/*.json`, `web/shared/utils/cauHinhTrungTam.gen.ts`,
`server/src/com/htbb/pokemon/GiftcodeAnToanDuLieu.java`, bảng cấu hình trong `docs/cau-hinh.md`) giờ là tệp **tĩnh**: web /
máy chủ đọc CSDL trước, các tệp đó chỉ là dự phòng lúc build và được test đối chiếu với `sql/htbb_day_du.sql`.
