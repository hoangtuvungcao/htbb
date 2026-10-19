# du_lieu — bảng dữ liệu thiết kế game

Bảng CSV (UTF-8, dòng đầu là tiêu đề) mô tả nội dung Pokémon: loài, chiêu, quái, vật phẩm, nhiệm vụ, bản đồ, sự
kiện. Đây là **nguồn** cho bộ sinh trong `tools/pokemon/` (ra `du_lieu/sql_nguon/pokemon_*.sql`), cho website (`npm run
dong-bo-du-lieu`), cho client (đóng gói sprite) và là dữ liệu đối chiếu của rất nhiều test `server/test/Kiem*`.
Luồng dữ liệu: [docs/kien-truc.md](../docs/kien-truc.md). Ý nghĩa các hệ thống: [docs/thiet-ke-game.md](../docs/thiet-ke-game.md).

`sql_nguon/`: tệp SQL nguồn để dựng `sql/htbb_day_du.sql` (seed `gamedb7_clean.sql`, migration `them_*` / `sua_*`, tệp sinh
`pokemon_*.sql`, `web_cms.sql`) — xem [sql/README.md](../sql/README.md).

Sửa bảng → chạy lại bộ sinh tương ứng → chạy test. Bảng có cột ghi "sinh bởi" thì sửa ở công cụ, không sửa tay.

## Bảng gốc (`bang_*`)

| Tệp | Nội dung | Ai dùng |
|---|---|---|
| `bang_thu.csv`, `bang_pet_cu.csv` | Pokémon thay cho thú cưỡi / pet cũ | `PetPokemon`, `ChuPokemon`, `DongGoiPokeJ2me`, web |
| `bang_quai.csv` | mỗi dòng `data_monster` → Pokémon (hệ, dex) | `sinh_bang_quai.py` (sinh), hệ sinh thái, nhiệm vụ, client |
| `bang_chieu.csv` | chiêu Pokémon | `ChieuPokemon`, `CharChiSo` |
| `bang_hieu_ung.csv` | hiệu ứng chiêu (PMDCollab RawAsset) | `tai_hieu_ung_pmd.py` |
| `bang_vat_pham.csv`, `bang_icon.csv` | tên và icon vật phẩm | `sinh_sql_vat_pham.py`, `sinh_icon_pokemon.py`, `sinh_sql_lau_dai.py` |
| `bang_npc.csv`, `bang_npc_su_kien.csv` | NPC và hình NPC sự kiện | server, client, web |
| `bang_chu.csv` | thay chữ người chơi thấy trong CSDL | `sinh_sql_chu.py` |
| `bang_cap_do_lau_dai.csv` | bảng cấp / EXP dài hạn | `sinh_sql_lau_dai.py`, web |
| `bang_tile_moi.csv` | bộ tile bản đồ hiện tại, cột `nguon` ghi nguồn từng tile | `DungBoTileMoi.java`, `BanDoSinhThai`, test tile |
| `bang_vat_the.csv` | hình vật thể trên bản đồ | `tai_vat_the.py` |

## Nội dung (`noi_dung_*`)

Sinh bởi `tools/pokemon/sinh_noi_dung.py` (đừng sửa tay), kiểm bởi `KiemNoiDung`: trang bị (`noi_dung_trang_bi`, bộ
`noi_dung_bo`), vật phẩm (`noi_dung_vat_pham`, `noi_dung_item_moi`, `noi_dung_su_kien_vat_pham`), nguyên liệu / chế tạo /
phân rã / vật liệu nâng cấp, ngọc hệ, chiêu (`noi_dung_chieu`, `noi_dung_chieu_ttk`, `noi_dung_chieu_nhanh_eevee`),
quái mới / đại cấp / tinh anh / boss thế giới, Gym, cửa hàng, rơi đồ (`noi_dung_roi`), trang trí / ngoại hình
(`noi_dung_trang_tri`). Cùng lúc ra `du_lieu/sql_nguon/pokemon_noi_dung.sql` và `du_lieu/web/*.csv`; `sinh_vat_pham_lau_dai.py` đọc các bảng này ra
`du_lieu/sql_nguon/pokemon_vat_pham_lau_dai.sql`.

## Bản đồ, nhiệm vụ, sự kiện

| Tệp | Nội dung |
|---|---|
| `thiet_ke_map.csv` | thiết kế từng bản đồ (vùng, loại map, cấp) — hệ sinh thái, nhiệm vụ, cẩm nang web |
| `diem_co_dinh.csv`, `diem_cau_ca.csv` | điểm cố định (NPC, cửa) và điểm câu cá trên bản đồ |
| `bang_den.json` | điểm đến qua cổng / bay của từng bản đồ (`GiaiMaMap` sinh) — `VeMapMoi`, `KiemMapPokemon` |
| `nhiem_vu_moi.csv`, `nhiem_vu_nhip_do.csv` | nhiệm vụ và nhịp độ (sinh bởi `sinh_nhiem_vu.py`) |
| `su_kien_nam.csv` | lịch sự kiện theo tuần trong năm |
| `map_moi/` | ảnh xem trước bản đồ đã vẽ lại |

## Báo cáo và kết quả sinh

| Tệp | Sinh bởi |
|---|---|
| `NHIEM_VU_THIET_KE.md` | `tools/pokemon/sinh_nhiem_vu.py` — thiết kế nhiệm vụ + bảng đã sinh (test đọc mục "Yêu cầu engine") |
| `KIEM_ITEM.md`, `kiem_item.csv` | `tools/pokemon/kiem_item_mo_coi.py` — mọi vật phẩm phải có chỗ dùng |
| `danh_muc_item.md` | `tools/SinhDanhMucItem.java` — danh mục `data_item` từ `du_lieu/sql_nguon/gamedb7_clean.sql` |
| `chi_so_trang_bi.csv` | `tools/pokemon/SinhSqlChiSo.java` (cùng `du_lieu/sql_nguon/pokemon_chi_so.sql`) |
| `kiem_ke_sprite.csv` | `tools/pokemon/tai_sprite_pmd.py` — kiểm kê động tác sprite |
| `tu_cam_con_lai.csv` | `tools/pokemon/quet_tu_cam.py` — từ kiếm hiệp còn sót |
| `mo_phong_kinh_te.csv`, `mo_phong_tom_tat.txt` (biểu đồ `mo_phong_*.png` sinh ra, không commit) | `tools/pokemon/MoPhongKinhTe.java` |
| `mo_phong_chi_so.txt` | `tools/pokemon/MoPhongChiSo.java` |
| `xem_truoc_*.png` | ảnh xem trước tile / icon (sinh ra, không commit) |
| `web/*.csv` | `tools/pokemon/sinh_noi_dung.py` — bản rút gọn cho website (chỉ cột người chơi cần); `web/nhiem_vu_tom_tat.json` do `sinh_nhiem_vu.py` |
