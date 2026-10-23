# du_lieu — bảng tài nguyên client / bản đồ

Dữ liệu game (vật phẩm, quái, nhiệm vụ, sự kiện, cấu hình) nằm trong **CSDL** — bản đầy đủ là
[`sql/htbb.sql`](../sql/README.md), sửa trên trang quản trị. Thư mục này chỉ còn các bảng (UTF-8, dòng đầu là tiêu
đề) mà mã máy chủ / client / công cụ đóng gói tài nguyên **đọc trực tiếp**: sprite, tile, bản đồ, NPC. Bảng thiết kế cũ đã
thành bảng CSDL và bộ sinh của chúng đã chuyển vào [`tools/luu_tru_sinh_du_lieu/`](../tools/luu_tru_sinh_du_lieu/README.md).
Luồng dữ liệu: [docs/kien-truc.md](../docs/kien-truc.md). Ý nghĩa các hệ thống: [docs/thiet-ke-game.md](../docs/thiet-ke-game.md).

| Tệp | Nội dung | Ai dùng |
|---|---|---|
| `bang_thu.csv`, `bang_pet_cu.csv` | Pokémon thay cho thú cưỡi / pet cũ | `PetPokemon`, `ChuPokemon`, `DongGoiPokeJ2me`, web |
| `bang_quai.csv` | mỗi dòng `data_monster` → Pokémon (hệ, dex) | hệ sinh thái, client |
| `bang_chieu.csv` | chiêu Pokémon | `ChieuPokemon`, `CharChiSo` |
| `bang_hieu_ung.csv` | hiệu ứng chiêu (PMDCollab RawAsset) | `tai_hieu_ung_pmd.py` |
| `bang_icon.csv` | icon vật phẩm (ảnh `server/map/icon_pokemon/`) | `KiemIconPokemon` |
| `bang_npc.csv`, `bang_npc_su_kien.csv` | NPC và hình NPC sự kiện | server, client, web |
| `bang_tile_moi.csv` | bộ tile bản đồ hiện tại, cột `nguon` ghi nguồn từng tile | `DungBoTileMoi.java`, `BanDoSinhThai`, test tile |
| `bang_vat_the.csv` | hình vật thể trên bản đồ | `tai_vat_the.py` |
| `thiet_ke_map.csv`, `vung_map.csv` | thiết kế từng bản đồ (vùng, loại map, cấp) | hệ sinh thái, nhiệm vụ, cẩm nang web |
| `diem_co_dinh.csv`, `diem_cau_ca.csv` | điểm cố định (NPC, cửa) và điểm câu cá | server, test bản đồ |
| `bang_den.json` | điểm đến qua cổng / bay của từng bản đồ (`GiaiMaMap` sinh) | `VeMapMoi`, `KiemMapPokemon` |
| `kiem_ke_sprite.csv` | kiểm kê động tác sprite (`tools/pokemon/tai_sprite_pmd.py`) | đóng gói sprite |
| `NHIEM_VU_THIET_KE.md` | thiết kế nhiệm vụ (test đọc mục "Yêu cầu engine") | tài liệu |
| `map_moi/` | ảnh xem trước bản đồ đã vẽ lại | tài liệu |

Mô phỏng kinh tế (`tools/pokemon/MoPhongKinhTe.java`) đọc thẳng `sql/htbb.sql`, ghi kết quả ra `mo_phong_ra/`
(không commit).

Đồ họ mới và trang phục (0.9.8):

| Tệp | Sinh bởi |
|---|---|
| `neo_trang_phuc.csv` | `client_pc/tools/XuatNeoTrangPhuc.java` — neo trang phục từng khung (đỉnh sọ, giữa sọ, mắt, cổ, lưng, eo) của 16 loài người chơi, cùng hàm client tính từ điểm ảnh (`game.pokemon.NeoTrangPhuc`) |
| `do_moi_trang_bi.csv`, `do_moi_bo.csv`, `trang_phuc.csv` | `tools/do_trang_phuc/sinh.py` — đồ họ mới (18 bộ hệ, 3 bộ vùng), thưởng bộ, trang trí (cùng `sql/htbb.sql` (khối `them_do_trang_phuc.sql`)) |
| `KIEM_KE_TRANG_BI.md` | `tools/do_trang_phuc/kiem_ke.py` — kiểm kê ô × dải cấp × họ, icon trùng / dùng chung, trước và sau đồ họ mới |
