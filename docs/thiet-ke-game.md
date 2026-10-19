# Thiết kế game Huyền Thoại Bảo Bối

Tài liệu thiết kế và cơ chế cho người phát triển / thiết kế mới vào dự án. Mô tả **luật đang chạy** trong máy chủ
(`server/src`), số liệu lấy từ code (`com.htbb.pokemon.*`, `real.*`), bảng dữ liệu `du_lieu/*.csv` và danh mục cấu hình
[`tools/cauhinh/danh_muc.json`](../tools/cauhinh/danh_muc.json). Khi tài liệu và code lệch nhau, **code thắng**: sửa
tài liệu cho khớp.

Tài liệu liên quan: cấu hình từng khoá ở [cau-hinh.md](cau-hinh.md), vận hành (nạp tiền, giftcode, bảo trì, GM) ở
[van-hanh.md](van-hanh.md), kiến trúc mã nguồn ở [kien-truc.md](kien-truc.md).

Mục lục:

1. Tổng quan
2. Pokémon: hệ, chiêu, tiến hoá, chỉ số
3. Cấp, EXP, năng lượng
4. Trang bị và chỉ số
5. Gym, Chiếm Gym, PvP, Hầm ngục, boss
6. Nhiệm vụ, sự kiện, giữ chân
7. Hệ sinh thái
8. Hồi sinh và Tự động đánh
9. Kinh tế và chống lạm phát
10. Thuật ngữ và quy tắc chữ
11. Phụ lục: lệnh mạng riêng của HTBB

---

## 1. Tổng quan

### 1.1 Ý tưởng

- HTBB là MMORPG 2D đánh thời gian thực, chuyển từ game kiếm hiệp cũ (KPAH/NQSH). **Người chơi LÀ một Pokémon**:
  không có nhân vật người, quái, boss, NPC, thú cưỡi, pet đều là Pokémon.
- Giữ lối chơi MMO gốc: di chuyển, đánh quái, lên cấp, đội (bang cũ), sự kiện, PvP. **Không** có bắt Pokémon bằng
  bóng, không đội hình 6 con, không đánh theo lượt.
- "Chất Pokémon" đến từ **loài + 18 hệ khắc chế + tiến hoá theo cấp**, cộng hệ sinh thái (thời tiết, câu cá, nhà trẻ…).
- Hình theo phong cách Pokémon Mystery Dungeon (sprite PMDCollab, 4 hướng). Giấy phép asset là phi thương mại; vì vậy
  nạp tiền **tắt mặc định** và không bao giờ bán sức mạnh (mục 9.5).
- Game chỉ còn chế độ Pokémon: các bản đồ kiếm hiệp cũ đã gỡ, thế giới gồm Kanto, Johto và Hoenn.

### 1.2 Các hệ thống chính

| Hệ thống | Lõi (lớp thuần, có test) | Nối vào game | Dữ liệu |
|---|---|---|---|
| Loài, hệ, tiến hoá | `LoaiPokemon`, `HePokemon`, `ChieuPokemon` | `TienHoaPokemon`, `CharChiSo` | `bang_quai.csv`, `bang_chieu.csv` |
| Sát thương, trạng thái | `SatThuongLauDai`, `TrangThaiPokemon` | `CharChiSo.attackDam`, `TrangThaiGame` | — |
| Cấp, EXP, năng lượng, tăng tốc | `CapDoLauDai`, `NangLuong`, `TangToc`, `ThongSoLauDai` | `ExpLauDai`, `LauDaiNhanVat`, `TangTocGame` | `bang_cap_do_lau_dai.csv` |
| Trang bị, nâng cấp, chỉ số | `NangCapLauDai`, `GioiHanChiSo`, `NganSachTrangBi`, `ChiSoTrangBi`, `NgoaiHinh` | `NangCapLauDaiGame`, `ChiSoTrangBiGame`, `NgoaiHinhGame` | `noi_dung_trang_bi.csv`, `chi_so_trang_bi.csv` |
| Gym, boss, Chiếm Gym, PvP | `GiaiDoanBoss`, `ChiemGymLogic`, `ThangPvp` | `GymLauDai`, `BossLauDai`, `ChiemGymLauDai`, `HamNgucGame` | `noi_dung_gym.csv`, `noi_dung_boss_the_gioi.csv` |
| Nhiệm vụ | `NhiemVuMau`, `NhiemVuLauDai` | `NhiemVuEngine` | `nhiem_vu_moi.csv` |
| Sự kiện 52 tuần | `SuKienLauDai` | `SuKien`, `LichSuKien` | `su_kien_nam.csv` |
| Hệ sinh thái | `HeSinhThai`, `KyNangSong`, `NhaTre`, `SinhThaiTheGioi`, `ChoSinhThai` | `HeSinhThaiGame`, `HeSinhThaiMenu` | `diem_cau_ca.csv`, bảng `data_hst_*` |
| Hồi sinh, Tự động đánh | `HoiSinh`, `TuDong` | `HoiSinhGame`, `TuDongGame` | — |
| Kinh tế, nạp tiền | `KinhTeLauDai`, `ChiSoKinhTe`, `NapTienLogic`, `GiftcodeAnToanLogic` | `CuaHangLauDai`, `NapTienGame` | `mo_phong_kinh_te.csv` |
| Chữ, thuật ngữ | `ChuPokemon`, `TenMapPokemon` | (lúc chạy + sinh dữ liệu) | `bang_chu.csv` |

Lớp thuần nằm ở `server/src/com/htbb/pokemon/`, lớp nối vào game ở `server/src/real/`. Cửa vào chung cho người chơi là
**Sổ tay huấn luyện** (NPC đổi thưởng → mục Sổ tay, `real.SoTayLauDai`): nhiệm vụ, Gym, Hầm ngục, cửa hàng, năng lượng,
tinh thông, thang PvP. Hệ sinh thái có menu riêng (gõ `hst`).

#### NPC và vai trò

Mọi NPC có mặt trong game đều dùng được, ở **mọi bản đồ** (thị trấn ngoài trời lẫn trong nhà). NPC tĩnh (cMap, tên ở
`data_npc`) mở đúng chức năng khi bấm (client `GameScr.moChucNangNpc`); lệnh dịch vụ của người chơi đứng cạnh NPC (≤ 7 ô,
`real.NpcTinhBanDo`) chạy như trong nhà, đứng xa thì bị từ chối.

| NPC (type) | Vai trò |
|---|---|
| Y tá Chansey (0) | Trung tâm Pokémon: hồi phục HP / PP miễn phí, mua thuốc (`real.NpcDichVu`) |
| Kecleon (1, 26), Golem / Tauros (11–20), Sableye (24) | Bán thuốc, gian hàng, đá quý |
| Machamp (2), Farfetch'd (28) | Thợ rèn: mua đồ cầm tay / trang bị thú, luyện, khảm, sửa, khoá, nghiền bột |
| Bronzong (3, 27) | Bán trang bị theo dòng |
| Persian (6, 29) | Bán đá quý, nguyên liệu |
| Kangaskhan (9, 30) | Kho đồ |
| Pidgeot (7) | Bay: tuyến Kanto / Johto có sẵn; ở Hoenn bay tới các Trung tâm Pokémon (750 xu, giảm theo thu nhập) |
| Lapras (10) | Bán vé tàu; bến 106 ra đảo, các bến khác chở đò tới bến Lapras khác (1 vé / chuyến) |
| Blaziken (21) | Huấn luyện viên: học chiêu |
| Meowth (22, 31) | Ngân hàng: kho xu / lượng, chuyển lượng, cửa hàng và nạp (khi bật nạp) |
| Arcanine (4), Xatu (5), Snorlax (8), Torkoal (23), Heracross (25) | Giao / trả nhiệm vụ (Heracross: nhiệm vụ đội, ở Cao nguyên Indigo) |
| NPC server ở thị trấn (-8, -10, -11, -12, -15, -31, -50, -90, -24) | Thợ rèn bí ẩn, Tổng quản, hợp thành, đổi hệ, đấu trường, giao hàng, đổi thưởng + Sổ tay (Gym, nhiệm vụ, Hầm ngục), xổ số |
| NPC sự kiện (-49, -65, -66, -67, -35) | Theo sự kiện đang chạy, **một** thứ tự ưu tiên cho menu và xử lý (`real.SuKienNpc`) |

Kiểm: `server/test/KiemNpc` (dữ liệu, client, thứ tự sự kiện) và bot `tools/pokemon/kiem_tai/KichBanNPC` (`npc_tat_ca`,
`npc_dich_vu`, `npc_su_kien`: mở mọi NPC, chọn mọi mục, không mục nào im lặng).

### 1.3 Cờ lớn và cấu hình

Ba cờ chế độ, **chỉ có tác dụng sau khi khởi động lại máy chủ**. Bật / tắt ở web quản trị **/admin → Cấu hình hệ thống**
(hoặc `server.ini`); chi tiết từng khoá, thứ tự ưu tiên (bảng `cau_hinh` > `server.ini` > mặc định) ở
[cau-hinh.md](cau-hinh.md).

| Khoá | Mặc định | Bật thì |
|---|---|---|
| `sv.lau_dai` | 0 | Thiết kế dài hạn: bảng EXP 100 cấp, năng lượng, kinh tế mới, nâng cấp có pity, Gym, boss theo pha, thang PvP, Hầm ngục, nhiệm vụ mới, lịch 52 tuần, trần chỉ số. **Gần như mọi luật trong tài liệu này cần cờ này.** |
| `sv.he_sinh_thai` | 0 | Thời tiết theo vùng, thay loài khi hồi sinh, bùng phát, kỹ năng sống, nhà trẻ, chợ sinh thái, đố vui, đua (mục 7). |
| `sv.nap_tien` | 0 | Hệ thống nạp phía game (chỉ chạy khi `sv.lau_dai=1`; web còn cần cấu hình riêng, xem [van-hanh.md](van-hanh.md)). |
| `sv.tran_cap_theo_mua` | 0 | Bật lại trần cấp theo ngày khai trương (thiết kế cũ, mục 3.5). |
| `sv.ngay_khai_truong` | trống | `yyyy-MM-dd`, ngày 0 của lịch tuần / mùa. Trống thì lấy `cau_hinh.lau_dai.ngay_khai_truong`, chưa có thì game ghi **hôm nay** ở lần bật đầu. |

Mọi tham số kinh tế, chỉ số, hồi sinh, tự động đánh, PvP còn lại là khoá `cau_hinh` **áp dụng ngay** (≤ 10 giây), không cần
build hay khởi động lại.

**Bật / tắt `sv.lau_dai` là tự động** (`real.ChuyenDoiLauDai.khiKhoiDong`, chạy lúc khởi động ở cả hai trạng thái, trước
khi nhận người chơi):

- **EXP**: `cau_hinh.lau_dai.thang_exp` (`moi`/`cu`) cho biết `tob_char.xp` đang theo bảng nào. Bật mà đang `cu` thì đổi
  sang bảng mới; tắt mà đang `moi` thì đổi ngược. Luôn **giữ cấp và % trong cấp**, không bao giờ tụt cấp. Mỗi lần đổi
  có số lần (`lau_dai.lan_chuyen`) và nhật ký `tob_char_xp_chuyen`; ghi nhật ký trước rồi `UPDATE … WHERE xp = xp_truoc`.
  Bị ngắt giữa chừng thì lần khởi động sau làm tiếp đúng lần đó (`lau_dai.dang_chuyen`).
- **Nhiệm vụ chính**: khi bật, vị trí cũ được lưu (`tob_char_lau_dai`) rồi đưa vào đầu chương mới theo cấp; khi tắt, lần
  nạp nhân vật đầu tiên trả về chỗ cũ.
- **Lịch sự kiện**: khi tắt, các mốc do lịch 52 tuần hẹn được gỡ khỏi `tob_su_kien`.
- Các bảng mới khác chỉ thêm dữ liệu; số trong bộ nhớ (quái, shop) tự về bảng cũ khi tắt.
- Đổi `sv.tran_cap_theo_mua` **không** chuyển EXP (chỉ `sv.lau_dai` mới chuyển).

### 1.4 Nguyên tắc thiết kế

| # | Nguyên tắc | Hệ quả |
|---|---|---|
| N1 | Đơn vị chung là **"giờ thu nhập"** ở cấp người chơi (`ThongSoLauDai.xuMoiGio(cấp)`, `expMoiGio(cấp)`) | Thưởng, giá, phí đều tính theo giờ chơi, không có quà cố định kiểu "10 triệu xu" cho mọi cấp |
| N2 | Mỗi nguồn tiền có hố hút cùng tỉ lệ theo cấp | Xu ra / vào mục tiêu 90–105% |
| N3 | Tiền thực không phình | Cung tiền thực ≤ 10 giờ thu nhập / người, tăng ≤ 5%/tháng |
| N4 | Cày nhiều có lợi nhưng **giảm dần** | Năng lượng huấn luyện; treo máy 20 giờ ≈ người cày 5–7 giờ |
| N5 | Lên cấp **chậm**, không trần theo mùa | Tới cấp 100: người chơi nhẹ ~15–18 tháng, người cày ~9–12 tháng |
| N6 | **Không trả tiền để mạnh hơn** | Tiền chỉ mua trang trí, tiện ích, **thời gian** (tăng tốc chung trần với bản kiếm được) |
| N7 | Ngân sách sức mạnh theo bậc | Bậc đồ mới (+0) ≤ 1,10 × bậc cũ (+15) cùng cấp |
| N8 | Luôn có mục tiêu phiên (15–30 phút), tuần, mùa | Mục 6.3 |
| N9 | Không ép đăng nhập mỗi ngày | Năng lượng, vé chương, lịch đăng nhập đều tích / ân hạn |

Mô phỏng kiểm các mục tiêu trên: `tools/pokemon/MoPhongKinhTe.java` (tất định), kết quả ở
[mo_phong_kinh_te.csv](../du_lieu/mo_phong_kinh_te.csv) và `du_lieu/mo_phong_tom_tat.txt`. Đổi số thì sửa generator
(`tools/pokemon/sinh_sql_lau_dai.py` là nguồn duy nhất của bảng thiết kế), chạy lại, rồi chạy mô phỏng và bộ test.

### 1.5 Bảng dữ liệu (`du_lieu/`)

Phần lớn là **file sinh ra** từ `tools/pokemon/*.py`: không sửa tay, sửa generator rồi chạy lại.

| Bảng | Nội dung |
|---|---|
| [bang_quai.csv](../du_lieu/bang_quai.csv) | Mọi template quái → Pokémon (dex, shiny, hệ 1/hệ 2, phân loại quái / boss / thú cưỡi / vật thể) |
| [bang_chieu.csv](../du_lieu/bang_chieu.csv), [bang_hieu_ung.csv](../du_lieu/bang_hieu_ung.csv) | Tên và hệ chiêu theo dòng / ô; hiệu ứng hình |
| [bang_thu.csv](../du_lieu/bang_thu.csv), [bang_pet_cu.csv](../du_lieu/bang_pet_cu.csv) | Thú cưỡi, pet cũ → Pokémon |
| [bang_npc.csv](../du_lieu/bang_npc.csv), [bang_npc_su_kien.csv](../du_lieu/bang_npc_su_kien.csv) | NPC → Pokémon |
| [bang_vat_pham.csv](../du_lieu/bang_vat_pham.csv), [bang_chu.csv](../du_lieu/bang_chu.csv) | Tên vật phẩm, mọi ô chữ đã đổi |
| [bang_cap_do_lau_dai.csv](../du_lieu/bang_cap_do_lau_dai.csv) | EXP, giờ, xu, chỉ số quái đề xuất theo cấp 1..100 |
| [chi_so_trang_bi.csv](../du_lieu/chi_so_trang_bi.csv) | 198 dòng chỉ số trang bị: tên, đơn vị, trạng thái, nơi áp dụng |
| [noi_dung_trang_bi.csv](../du_lieu/noi_dung_trang_bi.csv), [noi_dung_bo.csv](../du_lieu/noi_dung_bo.csv) | Danh mục 11 bậc × ô, bộ trang bị |
| [noi_dung_vat_lieu_nang_cap.csv](../du_lieu/noi_dung_vat_lieu_nang_cap.csv), [noi_dung_ngoc_he.csv](../du_lieu/noi_dung_ngoc_he.csv), [noi_dung_phan_ra.csv](../du_lieu/noi_dung_phan_ra.csv), [noi_dung_che_tao.csv](../du_lieu/noi_dung_che_tao.csv), [noi_dung_nguyen_lieu.csv](../du_lieu/noi_dung_nguyen_lieu.csv) | Nâng cấp, ngọc hệ, phân rã, chế tạo, nguyên liệu phụ |
| [noi_dung_vat_pham.csv](../du_lieu/noi_dung_vat_pham.csv), [noi_dung_item_moi.csv](../du_lieu/noi_dung_item_moi.csv) | Vật phẩm tiêu hao (giá theo thu nhập), vật phẩm mới |
| [noi_dung_chieu.csv](../du_lieu/noi_dung_chieu.csv), [noi_dung_chieu_ttk.csv](../du_lieu/noi_dung_chieu_ttk.csv), [noi_dung_chieu_nhanh_eevee.csv](../du_lieu/noi_dung_chieu_nhanh_eevee.csv) | Cây chiêu, cân bằng TTK, nhánh Eevee |
| [noi_dung_quai_dai_cap.csv](../du_lieu/noi_dung_quai_dai_cap.csv), [noi_dung_quai_tinh_anh.csv](../du_lieu/noi_dung_quai_tinh_anh.csv), [noi_dung_quai_moi.csv](../du_lieu/noi_dung_quai_moi.csv) | Quái theo dải cấp, quái tinh anh, quái Hoenn |
| [noi_dung_gym.csv](../du_lieu/noi_dung_gym.csv), [noi_dung_boss_the_gioi.csv](../du_lieu/noi_dung_boss_the_gioi.csv) | 16 Gym, boss thế giới 52 tuần |
| [noi_dung_roi.csv](../du_lieu/noi_dung_roi.csv) | Bảng rơi (dải cấp, tinh anh, Gym, boss, mở hộp) |
| [noi_dung_cua_hang.csv](../du_lieu/noi_dung_cua_hang.csv), [noi_dung_trang_tri.csv](../du_lieu/noi_dung_trang_tri.csv) | Cửa hàng, trang trí theo mùa (0 chỉ số) |
| [nhiem_vu_moi.csv](../du_lieu/nhiem_vu_moi.csv), [nhiem_vu_nhip_do.csv](../du_lieu/nhiem_vu_nhip_do.csv) | Toàn bộ nhiệm vụ, nhịp độ theo cấp (báo cáo: [NHIEM_VU_THIET_KE.md](../du_lieu/NHIEM_VU_THIET_KE.md)) |
| [su_kien_nam.csv](../du_lieu/su_kien_nam.csv), [noi_dung_su_kien_vat_pham.csv](../du_lieu/noi_dung_su_kien_vat_pham.csv) | Lịch 52 tuần, vật phẩm sự kiện |
| [diem_cau_ca.csv](../du_lieu/diem_cau_ca.csv), [thiet_ke_map.csv](../du_lieu/thiet_ke_map.csv), [diem_co_dinh.csv](../du_lieu/diem_co_dinh.csv) | Điểm câu cá, thiết kế bản đồ (cột `loai` = vùng sinh thái), điểm cố định server |
| [kiem_item.csv](../du_lieu/kiem_item.csv) | Kiểm "không vật phẩm mồ côi": mọi vật phẩm có nguồn và công dụng |

---

## 2. Pokémon: hệ, chiêu, tiến hoá, chỉ số

### 2.1 Năm dòng khởi đầu và tiến hoá

Cột `tob_char.class` giữ nguyên (0..4) và ánh xạ sang dòng Pokémon (`LoaiPokemon.KHOI_DAU_THEO_CLASS`). Loài hiện tại lưu
ở `tob_char.dex` (0 = suy ra từ class + cấp), `tob_char.shiny`. **Tiến hoá theo cấp**, xử lý ngay sau khi lên cấp
(`LoaiPokemon.tienHoaKeTiep`): server cập nhật `dex`, hệ, gửi -92 rồi -90 cho mình và người ở gần, kèm thông báo.

| `class` | Dòng | Hệ | Tiến hoá | Vai trò | HP / Tấn công / Thủ (tương đối) |
|---|---|---|---|---|---|
| 0 | Charmander | Lửa | Charmeleon 16 → Charizard 36 (Lửa/Bay) | Sát thương liên tục cận chiến, bỏng, chí mạng | 100 / 105 / 90 |
| 1 | Bulbasaur | Cỏ/Độc | Ivysaur 16 → Venusaur 32 | Chịu đòn + khống chế (ngủ, độc), hồi nhẹ | 115 / 90 / 115 |
| 2 | Eevee | Thường | cấp 25 chọn 1/8 nhánh bằng đá tiến hoá (item 960–967) | Linh hoạt theo nhánh | 90 / 100 / 95 |
| 3 | Squirtle | Nước | Wartortle 16 → Blastoise 36 | Chịu đòn chính, khiên, khiêu khích | 120 / 85 / 125 |
| 4 | Pichu | Điện | Pikachu 10 → Raichu 30 | Đánh xa, tê liệt, sát thương lan | 85 / 110 / 85 |

- Tám nhánh Eevee: Vaporeon (hồi đội), Jolteon (tốc độ), Flareon (bùng nổ), Espeon / Umbreon (đặc biệt / chịu đòn),
  Leafeon / Glaceon (khống chế), Sylveon (hỗ trợ). Chỉ chọn được khi `dex == 133` và cấp ≥ 25.
- Thưởng giai đoạn tiến hoá: **+8%** (dạng 2) / **+16%** (dạng 3) HP và tấn công (`LoaiPokemon.PT_GIAI_DOAN_*`).
- Ràng buộc cân bằng: tích HP × Tấn công × Thủ của mọi dòng nằm trong **±5%**; dòng chịu đòn hạ quái chậm hơn nhưng sống
  lâu hơn (test so TTK, mục 2.4).
- Không có chuyển sinh (reset cấp): client đọc cấp bằng 1 byte, tiến hoá gắn với cấp, và reset phá cốt truyện. Thay bằng
  **tinh thông** (mục 3.6).
- Mỗi tài khoản tối đa `sv.maxCharPerAccount` (3) Pokémon.

### 2.2 Mười tám hệ và bảng khắc hệ

`HePokemon` giữ 18 hệ theo số 0..17 (lưu xuống CSDL và gửi client, **không được đổi thứ tự**): 0 Thường, 1 Lửa, 2 Nước,
3 Điện, 4 Cỏ, 5 Băng, 6 Giác đấu, 7 Độc, 8 Đất, 9 Bay, 10 Siêu linh, 11 Bọ, 12 Đá, 13 Ma, 14 Rồng, 15 Bóng tối,
16 Thép, 17 Tiên. Bảng khắc là bảng chuẩn Gen 6+.

**Hệ số "làm dịu" cho MMO thời gian thực** (`HePokemon.heSoTrongGame`). Người chơi không đổi Pokémon giữa trận được, nên
×0 sẽ có quái không đánh nổi, còn ×4 sẽ hạ một đòn trong PvP. Hệ số chính xác (tính cả hệ 2 của mục tiêu) được ánh xạ:

| Chính xác | ×4 | ×2 | ×1 | ×0,5 | ×0,25 | ×0 |
|---|---|---|---|---|---|---|
| Trong game | **×2** | **×1,5** | ×1 | **×0,7** | **×0,5** | **×0,3** |

Bảng khắc hệ đơn (hệ chiêu đánh vào một hệ; trong game "mạnh" = ×1,5, "yếu" = ×0,7, "gần miễn" = ×0,3):

| Hệ chiêu | Mạnh với | Yếu với | Gần miễn |
|---|---|---|---|
| Thường | — | Đá, Thép | Ma |
| Lửa | Cỏ, Băng, Bọ, Thép | Lửa, Nước, Đá, Rồng | — |
| Nước | Lửa, Đất, Đá | Nước, Cỏ, Rồng | — |
| Điện | Nước, Bay | Điện, Cỏ, Rồng | Đất |
| Cỏ | Nước, Đất, Đá | Lửa, Cỏ, Độc, Bay, Bọ, Rồng, Thép | — |
| Băng | Cỏ, Đất, Bay, Rồng | Lửa, Nước, Băng, Thép | — |
| Giác đấu | Thường, Băng, Đá, Bóng tối, Thép | Độc, Bay, Siêu linh, Bọ, Tiên | Ma |
| Độc | Cỏ, Tiên | Độc, Đất, Đá, Ma | Thép |
| Đất | Lửa, Điện, Độc, Đá, Thép | Cỏ, Bọ | Bay |
| Bay | Cỏ, Giác đấu, Bọ | Điện, Đá, Thép | — |
| Siêu linh | Giác đấu, Độc | Siêu linh, Thép | Bóng tối |
| Bọ | Cỏ, Siêu linh, Bóng tối | Lửa, Giác đấu, Độc, Bay, Ma, Thép, Tiên | — |
| Đá | Lửa, Băng, Bay, Bọ | Giác đấu, Đất, Thép | — |
| Ma | Siêu linh, Ma | Bóng tối | Thường |
| Rồng | Rồng | Thép | Tiên |
| Bóng tối | Siêu linh, Ma | Giác đấu, Bóng tối, Tiên | — |
| Thép | Băng, Đá, Tiên | Lửa, Nước, Điện, Thép | — |
| Tiên | Giác đấu, Rồng, Bóng tối | Lửa, Độc, Thép | — |

- Mục tiêu hai hệ: nhân hệ số chính xác của hai hệ rồi mới ánh xạ (ví dụ ×2 × ×0,5 = ×1).
- **Hệ của quái lấy từ template** (`data_monster.he` / `he2`, -1 = không có hệ 2), không còn quay ngẫu nhiên lúc sinh
  quái (để Kyogre không ra hệ Lửa). Hệ của người chơi lấy từ loài (`LoaiPokemon.he1/he2(dex)`), cập nhật khi tiến hoá.
- Kháng hệ trên trang bị tính theo **hệ chiêu đang đánh** (PvP) hoặc hệ 1 của quái (quái đánh người), không theo dòng.

### 2.3 Chiêu

- Mỗi dòng có 9 ô chiêu (Eevee 11), mỗi chiêu 10 cấp; số hiệu và tên giữ khớp client (`bang_chieu.csv`,
  `SkillManager.SKILL_NAME`). Tên chiêu dùng tên Pokémon (Ember, Thunderbolt…), hệ chiêu ở `ChieuPokemon.HE`.
- **Mở theo giai đoạn tiến hoá**:

  | Giai đoạn | Cấp | Chiêu mở |
  |---|---|---|
  | Dạng 1 | 1 / 4 / 7 (bị động ô 5 ở cấp 6) | ô 0, 1, 2 |
  | Dạng 2 | 16 (Pichu 10) | ô 3 (chiêu nhiều đòn) và ô 4 |
  | Dạng 3 | 32 / 36 (Pichu 30, Eevee 25) | ô 6–8 (Eevee 6–10), có chiêu tối thượng |

- Cấp chiêu 1..9 rải từ cấp mở tới cấp 90; cấp 10 cần cấp 95, tối thượng cần cấp 100. Mỗi cấp chiêu +2% sát thương.
  Học chiêu tốn xu theo cấp (≈ 3 giờ thu nhập ở các mốc 25/30/45/60/75/90).
- Chiêu nhiều đòn ô 3 đã giảm từ 2.360% (game cũ) xuống ~5 đòn × 190–260%, để xoay chiêu có nghĩa. Tối thượng hồi
  15 giây, mạnh hơn mọi chiêu dạng 1.
- Nhánh Eevee đổi hệ ô 2/8/10 để có STAB (ví dụ Vaporeon dùng chiêu Nước, Sylveon dùng chiêu Tiên); số liệu như cây Eevee.
- Cân bằng theo TTK (thời gian hạ quái): xoay chiêu tham lam 60 giây ra hệ số sát thương mỗi giây; mục tiêu ~4 / 5 / 6
  giây cho quái thường ở cấp 1–30 / 31–60 / 61–100 (tính với đồ +6). Bảng: `noi_dung_chieu_ttk.csv`.

### 2.4 Công thức sát thương

`SatThuongLauDai.tinh` (áp ở `CharChiSo.attackDam` và mọi bản sao đường đánh):

```
cơ bản   = max(5% tấn công, tấn công − thủ)        (sàn theo %: quái thủ cao vẫn trầy)
sát thương = cơ bản × %chiêu × hệ × STAB × (1 + đồ hệ%) × chí mạng × (0,95..1,05)
hệ       = HePokemon.heSoTrongGame, ÁP SAU khi trừ thủ (như quái)
STAB     = ×1,2 khi hệ chiêu trùng hệ 1 hoặc hệ 2 của người đánh
chí mạng = ×1,5 (cộng thêm từ trang bị tối đa thành ×2,0; bỏ bạo kích ×4 cũ)
PvP      = sát thương × 0,30, TRẦN 8% HP tối đa của mục tiêu mỗi đòn
```

- Thời tiết (khi bật hệ sinh thái): chiêu hợp trời ±10% (mục 7.1), chỉ áp đòn của người chơi.
- Sau mọi lớp giảm phía bị đánh, đòn còn ít nhất 40% (sàn chống bất tử, mục 4.5).
- TTK mục tiêu: quái thường hạ người ~12–15 đòn; quái tinh anh ~20 giây; boss Gym 90–120 giây đánh một mình đúng cấp;
  boss thế giới 8–12 phút với 20 người; PvP cùng cấp cùng đồ 10–14 giây.

### 2.5 Trạng thái

Server tính, client chỉ vẽ (`TrangThaiPokemon`, gói -97):

| Trạng thái | Hiệu ứng | Thời gian | Miễn |
|---|---|---|---|
| 1 Bỏng | 3% HP tối đa mỗi 2 giây, −10% tấn công | 6 giây | hệ Lửa |
| 2 Độc | 2% → 4% → 6% HP mỗi 2 giây (chồng 3 lần) | 8 giây | hệ Độc, Thép |
| 3 Tê liệt | −30% tốc độ, 15% mất lượt | 5 giây | hệ Điện |
| 4 Ngủ | không hành động, tỉnh khi trúng đòn | 3 giây | sau đó miễn ngủ 10 giây |
| 5 Đóng băng | không hành động | 2 giây | hệ Băng; sau đó miễn 10 giây |

PvP: thời lượng ×0,5. Boss chỉ nhận 30% thời lượng, không ngủ và không đóng băng. Trạng thái từ trang bị: trần 15%
(PvP 7,5%), kháng ≤ 50%, mỗi đòn tối đa một trạng thái. Thuốc chữa trạng thái bán bằng xu ở cửa hàng.

### 2.6 Pokédex, shiny, sưu tầm

- **Pokédex nghiên cứu** theo loài (`tob_pokedex`, `tob_pokedex_thuong`): gặp → hạ 50 → thấy shiny; mốc 10 / 25 / 50 /
  75 / 100 loài cho danh hiệu. Hệ sinh thái thêm 6 kiểu nghiên cứu (mục 7.6).
- **Shiny** (quái): 1/4.096 lần hạ, pity 2.000 (lần thứ 2.000 không ra thì chắc chắn ra). Tuần Shiny ×10, Ngày cộng đồng
  ×5. Shiny chỉ đổi màu và cho danh hiệu, **không cộng chỉ số**. Shiny trứng nhà trẻ có tỉ lệ riêng (mục 7.4).
- 16 huy hiệu Gym là mốc cốt truyện, hiện trong hồ sơ.
- Trang phục, danh hiệu, khung theo mùa: **0 chỉ số**. Danh hiệu nói chung không có chỉ số.

### 2.7 Hiển thị Pokémon trên client

- Sprite đóng gói sẵn trong client; server chỉ gửi `dex` + `shiny` qua các gói -90 (thân), -91 (bảng quái, ngay sau lệnh
  26), -92 (tiến hoá), -93 (thú đang cưỡi), -94 (pet đi theo), -102 (ngoại hình trang bị). Chuỗi gửi kèm:
  -90 → -93 → -94 → -102 sau mỗi lần gửi thông tin một Pokémon. Chi tiết ở mục 11.
- Template có `maxhp` ≥ 10.000.000 vẽ cỡ boss (160%), quái thường 100%.
- Giới hạn kỹ thuật cần nhớ khi thêm nội dung: số template quái ≤ 255 (đếm 1 byte; id phải liền nhau), cấp đọc bằng
  1 byte (≤ 127), danh sách nhiệm vụ gửi client ≤ 127, `data_attribute` không được thêm / xoá / đổi thứ tự dòng (client tra
  tên chỉ số theo vị trí), `data_potion` đúng 162 dòng.

---

## 3. Cấp, EXP, năng lượng

### 3.1 Bảng EXP

Bảng `ThongSoLauDai.EXP_LEN_CAP` (bản đầy đủ: [bang_cap_do_lau_dai.csv](../du_lieu/bang_cap_do_lau_dai.csv)), cấp tối đa
**100**. Dựng từ số **giờ hiệu dụng** để lên một cấp `h(L)` (nội suy log): EXP lên cấp = `h(L) × quái/giờ(L) × EXP một
quái(L)`, quái/giờ = `360 − 1,3·L`.

| Cấp | Giờ / cấp | Giờ tích luỹ | EXP lên cấp | Xu / giờ |
|---|---|---|---|---|
| 1 | 0,03 | 0 | 215 | 3.590 |
| 10 | 0,05 | 0,3 | 5.210 | 5.552 |
| 20 | 0,25 | 1,3 | 75.200 | 9.018 |
| 30 | 0,90 | 6,1 | 578.000 | 13.803 |
| 40 | 1,9 | 19 | 2.400.000 | 21.560 |
| 50 | 3,4 | 44 | 5.960.000 | 33.925 |
| 60 | 5,8 | 88 | 14.700.000 | 52.734 |
| 70 | 10,3 | 164 | 38.800.000 | 81.776 |
| 80 | 17,9 | 298 | 110.000.000 | 126.976 |
| 90 | 30,1 | 526 | 249.000.000 | 196.101 |
| 91 → 100 | 31 → 42 | 556 → 882 | 264M → 421M | 205k → 302k |

Tổng 1 → 100 khoảng **882 giờ hiệu dụng**. Tiến hoá đầu (cấp 16) ~1,1 giờ (Pichu → Pikachu ~0,3 giờ). Mọi giá trị nhỏ hơn
`Integer.MAX`. Thời gian thật tới cấp 100 do **năng lượng** (3.3) và **tăng tốc** (3.4) quyết định.

### 3.2 EXP khi hạ quái

Một hàm duy nhất `ExpLauDai.traoHaQuai` cho mọi đường cộng EXP hạ quái (quy tắc: mọi bản sao gọi về một hàm):

```
exp = rcvxp(template) × lệch cấp × (1 + buff%) × năng lượng% × bắt kịp
  lệch cấp: người cao hơn quái Δ = 0–3 → 100%, 4–6 → 80%, 7–9 → 50%, 10–14 → 20%, ≥ 15 → 5%
            quái cao hơn người: +5% mỗi cấp, tối đa +25%; quá 8 cấp → 0
  buff% (pet, cánh, sự kiện, đội, bạn bè…) CỘNG dồn, TRẦN +100%
```

- Các hệ số chồng nhau của game cũ (×3 toàn cục, giữ 10%/50%, chia 3 từ cấp 60, ×0,6 từ cấp 80, vé x2,5/x3, ngày x50)
  đều đã bỏ. Kẹo EXP S/M/L cũ (cộng EXP cố định) bị từ chối.
- Tổ đội: mỗi thành viên ở gần nhận `100% × (1 + 10% × (số người − 1))`, tối đa +40% khi đủ 5 người.
- Xu, đá và đồ rơi theo **cùng hệ số lệch cấp** và cùng năng lượng: đánh quái thấp hơn 15 cấp gần như không được gì.
- EXP từ trang bị + pet + thời trang + đồ giữ cộng tối đa +30% (nằm trong trần chung +100%).
- Nhiệm vụ, sự kiện, boss **không** bị năng lượng giới hạn.

### 3.3 Năng lượng huấn luyện

"Thời gian đánh quái" = tổng khoảng giữa hai lần hạ quái liên tiếp, mỗi khoảng tối đa 60 giây (đứng yên, đi chợ, AFK không
tính). `NangLuong` + `ThongSoLauDai.NANG_LUONG_PHUT_NGAY` / `BAC_MET`:

| Thời gian đánh quái trong ngày | EXP, xu, đá từ quái |
|---|---|
| 90 phút đầu, cộng phần 100% chưa dùng hôm trước (tổng tối đa 180 phút) | **100%** |
| 120 phút tiếp | **20%** |
| 120 phút tiếp | **5%** |
| Sau đó | **0%** (vẫn tốn thuốc và độ bền đồ) |

- Nghỉ từ 2 ngày trở lên thì hôm sau có đủ 180 phút 100%: không có áp lực đăng nhập mỗi ngày.
- Người chơi nhẹ (60–90 phút đánh quái / ngày) gần như không chạm ngưỡng. Treo máy 20 giờ cũng chỉ ≈ người cày 6 giờ.
- Đổi ngày lúc 00:00 giờ máy chủ. Ghi đè được bằng khoá `exp.nang_luong_phut` / `exp.nang_luong_tich_phut` (bảng
  `cau_hinh`); thiếu dòng thì dùng 90 / 180 của code.
- Trạng thái hiện ở Sổ tay huấn luyện → Năng lượng.

### 3.4 Vật phẩm tăng tốc

Mua **thời gian**, không mua sức mạnh: chỉ cho EXP — không xu, không đá, không vật phẩm, không đổi tỉ lệ rơi, không chỉ
số (`com.htbb.pokemon.TangToc`, `real.TangTocGame`). Mỗi món có **bản cao cấp** (bán bằng lượng ở Cửa hàng lượng, lượng
bị đốt; tắt bằng `nap.tang_toc.bat`) và **bản khoá** (kiếm bằng chơi). Cả hai đều khoá (không giao dịch) và **dùng chung
một trần**.

| Món | Id cao cấp / khoá | Điểm | Tác dụng | Giới hạn |
|---|---|---|---|---|
| Vé EXP ×1,5 (30 phút) | 1250 / 1251 | 15 | EXP hạ quái ×1,5 trong 30 phút **đánh quái**, nhân sau năng lượng | 60 phút vé / ngày; không trộn ×1,5 với ×2 đang chạy |
| Vé EXP ×2 (30 phút) | 1252 / 1253 | 30 | như trên, ×2 | chung 60 phút vé |
| Bùa thể lực | 1254 / 1255 | 45 | +60 phút năng lượng 100% hôm nay, **chỉ EXP**, không tích sang hôm sau | 1 / ngày |
| Phiếu ủy thác luyện tập (1 giờ) | 1256 / 1257 | 12 | Treo offline: thoát game, đăng nhập lại nhận 20% EXP của 1 giờ đánh quái ở cấp lúc thoát | 3 giờ / ngày (+1 với Thẻ tháng), đặt trước ≤ 4 giờ |
| Kẹo hiếm | 1214 | 30 | EXP bằng 30 phút đánh quái, **trừ 30 phút năng lượng 100%** | 2 / tuần |

- **Trần chung**: tổng điểm ≤ **60 / ngày, 210 / tuần** (3,5 giờ EXP), bản cao cấp + bản khoá cộng chung, đếm khi
  **dùng**. Bot không vượt được trần.
- **Không dùng ở cấp 100** (EXP vượt cấp thành tinh thông = sức mạnh).
- Nguồn bản khoá: nhiệm vụ tuần (2/2 → 2 vé ×1,5 + 1 phiếu ủy thác), lịch đăng nhập (ngày 3 → vé ×1,5, ngày 7 → bùa),
  vé mùa (nhánh miễn phí có nhiều điểm tăng tốc hơn nhánh cao cấp), Kecleon (bằng lượng khoá, 2 / tuần mỗi món). Người không
  nạp dùng khoảng 40–60% trần mỗi tuần.
- Mục tiêu đã kiểm bằng mô phỏng: mua tối đa nhanh hơn người không dùng ≤ 40% cùng giờ chơi; sức mạnh cùng cấp chênh
  ≤ 1%.
- Phân biệt với **Tự động đánh** (mục 8.2): Tự động đánh chỉ chạy khi online và chịu nguyên luật năng lượng; phiếu ủy
  thác là treo offline có trần riêng.

### 3.5 Trần cấp theo mùa và ngày khai trương

Mặc định **tắt** (`sv.tran_cap_theo_mua=0`): trần = cấp 100, tinh thông mở đủ, mọi `ngay_mo` của nội dung bị bỏ qua
(`LauDai.ngayMoNoiDung()`). Nội dung vẫn khoá theo **cấp**, chuỗi **`truoc`** (cốt truyện) và **vé chương** 2/tuần.

Bật `=1` để dùng lại thiết kế cũ — trần theo số ngày kể từ `sv.ngay_khai_truong` (`ThongSoLauDai.TRAN_CAP_NGAY/GIA_TRI`):

| Từ ngày | 0 | 60 | 150 | 240 | 365 | 545 |
|---|---|---|---|---|---|---|
| Trần cấp | 60 | 70 | 80 | 91 | 95 | 100 |

Khi bật lại, ai đang trên trần giữ cấp; EXP vượt trần thành tinh thông. **Ngày khai trương vẫn dùng khi tắt trần**: là
ngày 0 của lịch sự kiện 52 tuần, mùa PvP, vé mùa, boss thế giới theo tuần, bắt kịp (sau ngày 30). Khoá thử nghiệm
`sv.tua_nhanh_ngay` cộng N ngày vào lịch (chỉ máy thử).

### 3.6 Tinh thông (sau cấp tối đa)

- EXP vượt cấp tối đa thành **điểm tinh thông**: 1 điểm = 50% EXP lên cấp 99 → 100.
- Tối đa 50 điểm (khi trần mùa bật thì mở dần 10 điểm / mùa). Mỗi điểm +0,2% tấn công **hoặc** HP (chọn ở Sổ tay),
  tối đa **+10%**, nằm trong ngân sách sức mạnh.
- Tiền không mua được tinh thông (tăng tốc không dùng ở cấp 100).

### 3.7 Bắt kịp

| Đối tượng | Điều kiện | Ưu đãi |
|---|---|---|
| Người vào muộn | vào sau ngày 30 | EXP ×1,5 khi cấp < trung vị máy chủ − 5 |
| Người quay lại | vắng ≥ 14 ngày | EXP ×1,5 như trên + 3 nhiệm vụ "Quay lại" |

Không cộng thêm xu. Trung vị cấp tính lại định kỳ.

### 3.8 Mở nội dung theo cấp và cốt truyện

| Nội dung | Điều kiện |
|---|---|
| Cổng Hoenn | cấp ≥ 84 **và** đã xong chương kết năm 1 (C52.7 "Vùng đất phía nam") — `BanDoHoenn.lyDoChan` |
| Chuỗi chính năm 2 (C53…) | cấp tối thiểu từng chương (91..100) + nhiệm vụ trước (`truoc`) + vé chương |
| Phụ / huyền thoại cấp cao | chỉ cấp |
| Chuỗi huyền thoại (HT-*) | mỗi bước cần cấp cao hơn bước trước 2 (ví dụ Zapdos 40, 42…, 52) + vật phẩm của bước |
| Bậc đồ 7–10, cửa hàng Bronzong | cấp của bậc |
| Quái Hoenn (template 139–178) | map cấp ≥ 84 |
| Boss thế giới, sự kiện tuần, mùa PvP, vé mùa | lịch tuần (không phải khoá nội dung) |

---

## 4. Trang bị và chỉ số

### 4.1 Ô và bậc

**11 ô** (type cũ giữ nguyên, tên đã là đồ Pokémon):

| Ô | type | Chỉ số chính | Nguồn |
|---|---|---|---|
| Khăn quàng (áo cũ) | 0 | thủ, HP | cửa hàng, rơi |
| Đai (quần cũ) | 1 | thủ, HP | cửa hàng, rơi |
| Băng đô (nón cũ) | 2 | thủ, HP | cửa hàng, rơi |
| Đồ cầm tay theo dòng: Than lửa / Hạt giống thần / Khăn lụa / Nước thần / Nam châm | 3–7 | tấn công | nhiệm vụ, rơi, Gym (Hoàn mỹ) |
| Huy hiệu ×2 (nhẫn cũ) | 8 | tấn công | Gym, nhiệm vụ |
| Vòng cổ (dây chuyền cũ) | 9 | tấn công, HP | chế tạo, rơi |
| Lông vũ tốc độ (giày cũ) | 10 | thủ, HP, tốc chạy | cửa hàng, rơi |
| Găng đấm | 11 | tấn công, chí mạng | cửa hàng, rơi |
| Phiến đá hệ (ngọc bội cũ) | 12 | tấn công + 1 hệ: +5% sát thương **hoặc** −5% sát thương nhận từ hệ đó | chế tạo, sự kiện |
| Ruy băng (phi phong cũ) | 19 | thủ, HP | nhiệm vụ, boss thế giới |

5 ô mua ở NPC Bronzong (Khăn quàng, Đai, Băng đô, Lông vũ, Găng) với giá **1,5 giờ thu nhập**; 6 ô còn lại lấy từ nhiệm
vụ, Gym, chế tạo, rơi, boss.

**11 bậc theo dải cấp** (giá 1 món = 1,5 giờ thu nhập ở cấp của bậc):

| Bậc | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 |
|---|---|---|---|---|---|---|---|---|---|---|---|
| Tên | sơ cấp | đồng | bạc | vàng | bạch kim | kim cương | chuyên gia | tinh anh | huyền thoại | thần thoại | tối thượng |
| Cấp | 1 | 10 | 20 | 30 | 40 | 50 | 60 | 70 | 80 | 90 | 98 |
| Giá (xu) | 5.385 | 8.328 | 13.527 | 20.704 | 32.340 | 50.888 | 79.101 | 122.664 | 190.464 | 294.152 | 416.954 |

- **Độ hiếm**: Trắng 100%, Xanh 110%, Đỏ 120%, **Hoàn mỹ 130%** (chỉ từ quái tinh anh, Gym, boss thế giới), Vàng (sự
  kiện, chỉ trang trí).
- **Bộ trang bị** (số món cùng bậc): 4 món +5% HP, 7 món +5% tấn công, 11 món +3% chí mạng. Thưởng giống nhau mọi bậc nên
  không gây leo thang ([noi_dung_bo.csv](../du_lieu/noi_dung_bo.csv)).
- **Đồ cầm tay cùng hệ chiêu**: thiết kế +10% (+1%/bậc, ≤ 20%) — **chưa nối vào sát thương** (nối vào sẽ làm TTK đã
  hiệu chỉnh ngắn đi; cần hiệu chỉnh lại HP quái cùng lúc).
- Đổi bậc: đồ cũ **phân rã** ra 2 đá + 10% giá; bán NPC chỉ được 5% giá, nên phân rã luôn đáng hơn bán.
- **Ngân sách sức mạnh**: bậc mới +0 ≤ **1,10 ×** bậc cũ +15 ở cùng cấp; phần tăng của bậc mới đến từ cấp cao hơn. Trần
  tổng (đồ +15, ngọc 10, đủ bộ, tinh thông 50) ≈ 2,4 lần không đồ.

### 4.2 Nâng cấp +1..+15

`NangCapLauDaiGame` (hai đường nâng cấp dùng chung luật), bảng [noi_dung_vat_lieu_nang_cap.csv](../du_lieu/noi_dung_vat_lieu_nang_cap.csv):

| +k | Tỉ lệ gốc | Đá | Bùa bảo hộ (lượng khoá) | Trượt | Chỉ số món |
|---|---|---|---|---|---|
| 1–2 | 100% | 1 | — | — | +3% / cấp |
| 3 / 4 / 5 | 95 / 90 / 85% | 1 | — | giữ cấp | |
| 6 | 75% | 2 | — | giữ cấp | |
| 7 / 8 / 9 / 10 | 65 / 55 / 45 / 38% | 2 | 10 | **−1** (trừ khi có bùa) | +30% ở +10 |
| 11 / 12 / 13 / 14 / 15 | 30 / 24 / 18 / 13 / 10% | 4 | 25 | −1 | +5% / cấp, **+55%** ở +15 |

- Xu mỗi lần = `ThongSoLauDai.CHI_PHI_NC[bậc][k]` (một phần giá món, tăng theo +k): nâng một món từ +0 lên +15 kỳ vọng
  ≈ 22 × giá món. Đây là **hố hút xu lớn nhất** của nền kinh tế.
- **Pity**: mỗi lần trượt liên tiếp ở cùng ô cộng `ceil(tỉ lệ gốc / 3)` điểm % (`ThongSoLauDai.tiLeNangCap`); về 0 khi
  thành công hoặc đổi bậc.
- **Không bao giờ vỡ đồ.** Bùa bảo hộ là item 1258 / 1259; thiếu bùa thì trả lượng khoá như cũ.
- Từ +11 cần thêm 1 **nguyên liệu phụ** theo nhóm bậc: Bụi sao (bậc 0–2), Mảnh sao (3–5), Mảnh thiên thạch (6–8), Vảy rồng
  (9–10). Nguyên liệu phụ rơi 0,4%/quái từ cấp 40, có trong phân rã và hộp boss.
- Trừ tiền **sau** khi kiểm đủ điều kiện (quy tắc chung của server).

### 4.3 Ngọc hệ, phân rã, chế tạo

- **Ngọc hệ** (khảm): mỗi ô 1 ngọc, cấp 1..10, +1% chỉ số món mỗi cấp (≤ 10%); giá bước n = 1,45^(n−1) giờ thu nhập.
  Đây là hố hút thứ hai (thiếu nó thì mô phỏng cho cung tiền thực tăng ~12%/tháng). Thiết kế 18 hệ × 10 cấp ở
  [noi_dung_ngoc_he.csv](../du_lieu/noi_dung_ngoc_he.csv); hiện giá khảm đã theo giờ thu nhập, ngọc theo hệ cần thêm code /
  client.
- **Phân rã**: đồ thường → 2 đá + 10% giá; đồ Hoàn mỹ thêm 1 Mảnh tinh hoa + 1 nguyên liệu phụ (3 Mảnh tinh hoa để chuyển
  đồ Hoàn mỹ sang bậc mới); ngọc thừa → đá theo cấp ngọc.
- **Chế tạo** ở Machamp (Xưởng Pewter): Vòng cổ và Phiến đá hệ mỗi bậc cần (2 + bậc) đá, (1 + bậc/3) nguyên liệu phụ và
  50% giá xu.
- Phiến đá hệ: dòng chỉ số 93 / 94 mang giá trị `hệ × 1000 + phần nghìn` (ví dụ Băng 5% = `5050`); tên món tự thêm
  " (hệ X)". Còn thiếu đường ghi giá trị lúc tạo món (NPC chọn hệ / ngẫu nhiên).

### 4.4 Mô hình chỉ số Pokémon

`data_attribute` có 198 dòng (id 0..186 và 200..210), nguồn duy nhất là `ChiSoTrangBi.BANG`, xuất ra
[chi_so_trang_bi.csv](../du_lieu/chi_so_trang_bi.csv). Giữ id, đổi nghĩa sang chỉ số Pokémon; mỗi dòng có trạng thái
**dùng / hiện / nội bộ / ngừng**. Nhóm chính:

| Chỉ số | Ghi chú |
|---|---|
| HP, PP (MP) | id 33 / 34 mỗi điểm +1.000 |
| Tấn công / Tấn công đặc biệt / Phòng thủ / Phòng thủ đặc biệt (điểm và %) | "ma pháp" cũ = đặc biệt |
| Tốc độ đánh (id 160, % giảm hồi chiêu), tốc độ chạy | trần 33% hồi chiêu (= +50% tốc đánh), +30% tốc chạy |
| Chí mạng, sát thương chí mạng | gộp một lần quay; trên nền ×1,5 |
| Chính xác / né tránh, hút HP / PP, hồi HP / PP mỗi 5 giây | xem trần 4.5 |
| Xuyên giáp, giảm sát thương nhận, phản sát thương, sát thương lên boss / quái, sát thương theo % HP mục tiêu | xem trần 4.5 |
| Kháng hệ / sát thương hệ | 5 hệ cũ ánh xạ thẳng (Lửa, Nước, Cỏ, Thép, Đất); 13 hệ còn lại qua phiến đá (id 93 / 94); 186 = kháng mọi hệ |
| Gây / kháng trạng thái (id 14..25) | ngủ, đóng băng, độc, tê liệt, bỏng (khảm cũ đổi nghĩa) |
| EXP (id 111) | trang bị + pet + thời trang ≤ 30% |

Ngừng khi bật `sv.lau_dai`: hiệu ứng "lan" kiếm hiệp (độc / sét / băng / lửa lan), biến người tuyết, các dòng không nơi
nào đọc; chúng bị lọc khỏi mô tả món (`ChiSoTrangBiGame.locHienThi`). Client lấy tên chỉ số từ server (gói 25).

### 4.5 `GioiHanChiSo`: trần, giảm dần, sàn sát thương

Lớp thuần [`GioiHanChiSo`](../server/src/com/htbb/pokemon/GioiHanChiSo.java) chặn lạm phát chỉ số từ mọi nguồn (trang bị,
pet, thú cưỡi, skin, thời trang, cải trang, đồ giữ). Chạy khi `sv.lau_dai=1` **và** `chi_so.bat=1`; nối vào game ở
`real.ChiSoTrangBiGame`. Nguyên tắc: **trần đặt ở điểm cuối** (lần quay chí mạng, né, hút, từng lớp giảm, tổng % cộng
thêm), không đặt ở từng nguồn, nên pet / thú cưỡi / skin không lách được.

**Giảm dần** `giamDan(x, gối, trần)`: x ≤ gối giữ nguyên; trên gối thì `gối + dư × (trần − gối) / (dư + trần − gối)` —
đơn điệu, liên tục, không bao giờ chạm trần. Cộng thêm chỉ số luôn có ích một chút, chồng nhiều nguồn thì lợi ích giảm dần.

| Chỉ số | Gối → trần PvE | PvP | Khoá cấu hình (‰) |
|---|---|---|---|
| Tỉ lệ chí mạng | 25% → 40% | 20% → 30% (trừ kháng chí mạng) | `chi_so.tran_chi_mang` (400) |
| Sát thương chí mạng cộng thêm | +30% → +50% (đòn ≤ ×2,0) | ×1,5 phẳng | `chi_so.tran_st_chi_mang` (500) |
| Né tránh | 20% → 30% | 15% → 20% | `chi_so.tran_ne` (300) |
| Hút HP gộp mọi nguồn | 6% → 10% | 3% → 5% trên sát thương thật (sau ×0,30) | `chi_so.tran_hut` (100), `chi_so.tran_hut_pvp` (50) |
| Giảm sát thương thụ động (một lớp gộp) | 35% → 50% | 30% → 40% | `chi_so.tran_giam_st` (500) |
| Kháng một hệ (kháng hệ + kháng mọi hệ + phiến đá) | 40% → 60% | | `chi_so.tran_khang_he` (600) |
| Gây trạng thái / kháng | 10% → 15% / 30% → 50% | ×0,5 | — |
| Xuyên giáp (tỉ lệ bỏ qua thủ) | 20% → 30% | | — |

**Trần cứng**: phản đòn 15% (PvP 10%); sát thương lên boss / quái +20% / +20%; sát thương theo % HP mục tiêu 1% (boss
0,2%) và không quá đòn gốc; hồi HP / PP mỗi nhịp 5 giây ≤ 1% tối đa; hút HP lên boss / Gym / tinh anh ×10% (mỗi đòn ≤ 0,2%
HP tối đa); EXP từ đồ ≤ +30%; thưởng bộ ≤ 5%; **tổng % cộng thêm** tấn công / thủ / HP ngoài đồ ≤ +50% trên chỉ số gốc
(`chi_so.tran_phan_tram_cong`). Giai đoạn tiến hoá, tinh thông, hệ sinh thái tính ngoài (đã có trần riêng).

**Sàn sát thương (chống bất tử)**: mọi lớp giảm phía bị đánh đi qua một hàm; sau mọi lớp, đòn còn **ít nhất 40%**
(`chi_so.san_sat_thuong` = 400‰). Tổng giảm vì thế ≤ 60%.

**Ngân sách một món** (`NganSachTrangBi`, bật bằng `chi_so.kep_trang_bi`): trần một món = `ceil(chuẩn bậc × 1,30 × hệ số
nâng cấp) + 2` (1,30 phủ biến động ngẫu nhiên +0..25% lúc tạo đồ). Đồ **bậc cũ** thêm trần 1,30 × 1,25 × chuẩn bậc hiện
tại, tức ngang đồ mới khoảng +10. Kẹp **lúc tính** cho đồ đang mặc, nên đồ cũ trong túi người chơi cũng được kẹp mà không
sửa dữ liệu; mô tả món hiện đúng số đã kẹp. Bỏ qua thời trang và đồ thú cưỡi (thang riêng).

Mọi khoá `chi_so.*` áp dụng ngay (đòn kế tiếp; kẹp đồ: lần mặc / tháo kế tiếp), nhóm "Chỉ số & chống lạm phát" trên trang
cấu hình. `chi_so.bat=0` quay về công thức không trần.

### 4.6 Ngoại hình trang bị

Trang bị đang mặc được **vẽ thành phụ kiện trên thân Pokémon** (`com.htbb.pokemon.NgoaiHinh`, `real.NgoaiHinhGame`). Chỉ là
hình, **không đổi chỉ số**.

- 7 ô hình: mũ / nơ (đỉnh đầu), khăn quàng, vòng cổ, đồ cầm tay, ruy băng / áo choàng (sau thân), phiến đá hệ (bay quanh
  thân), kính. Kèm vầng sáng theo bậc, hạt lấp lánh theo cấp cường hoá, vòng bộ trang bị (4 / 7 / 11 món), bảng màu trang
  phục.
- Gói **-102** (S→C) đi kèm chuỗi -90 → -93 → -94 → -102, nên mọi chỗ đã gửi thông tin Pokémon tự mang theo ngoại hình.
  Màn chọn Pokémon nhận khối ngoại hình ở cuối gói 13. Client cũ không biết -102 thì bỏ qua.
- Gói **-103** (C→S): người chơi ẩn từng ô / trang phục trong bảng Cài đặt; server lưu vào hồ sơ và gửi lại -102.
- Phiên bản sau chỉ được **thêm trường ở cuối** gói.

### 4.7 Thú cưỡi, pet, skin thú cưỡi

- **Thú cưỡi** là Pokémon cưỡi được (Ponyta, Rapidash, Arcanine, Lapras, Gyarados, Dragonite…), bảng
  [bang_thu.csv](../du_lieu/bang_thu.csv); gói -93 báo thú đang cưỡi. Chỉ số thú cưỡi đi qua cùng trần của 4.5 (tốc chạy
  thú cưỡi đã chạm trần +30%).
- **Pet** là Pokémon nhỏ đi theo (`PetPokemon`), gói -94. Item 720 là "Kẹo thú cưng" (tiến hoá pet), khác Kẹo hiếm EXP 1214.
- **Skin thú cưỡi** (`real.SkinThuCuoi`, item 905–909 và 911, type 30): mặc vào ô đầu tiên của bảng Trang bị thú →
  **thú đang cưỡi đổi hình**, và **Pokémon chủ** được thêm chỉ số + một đặc tính. Chỉ số của bản thân con thú không bị
  đụng (không ghi gì vào `Animal` / `tob_animal`).

  | # | Tên | Item | Hệ | Chỉ số thường trực | Đặc tính (tỉ lệ, hồi chiêu 40 giây) |
  |---|---|---|---|---|---|
  | 0 | Copperajah | 905 | Thép | +2.000 HP, +3.000 PP | bị đòn vật lý: chặn 30% sát thương 3 giây (15%) |
  | 1 | Gấu Trúc | 906 | Cỏ | +200 chính xác | bị đòn đặc biệt: phản +50 điểm 3 giây (15%) |
  | 2 | Xerneas | 907 | Nước | +20% điểm tiềm năng | nhận sát thương: khiên chặn 50% trong 5 giây (10%) |
  | 3 | Camerupt | 908 | Lửa | 5% đòn xuyên toàn bộ giáp | HP dưới 30%: +50% tấn công 3 giây |
  | 4 | Lycanroc | 909 | Đất | +20% thủ, +15% hấp thụ sát thương | (hấp thụ luôn bật) |
  | 5 | Dialga (Kim Cang Long cũ) | 911 | — | 2–3 dòng ngẫu nhiên theo vật phẩm | — |

  - Skin hợp hệ với dòng Pokémon thì +5% sát thương (vòng tương sinh 5 hệ cũ: mỗi dòng đúng một skin hợp).
  - Skin gắn theo Pokémon, không theo con thú: đổi con thú vẫn giữ skin. **Xuống thú là gỡ hẳn** (vật phẩm về hành
    trang); gỡ thêm bằng menu NPC hoặc gõ `boskin`.
  - Mọi chỉ số đi qua một cổng `SkinThuCuoi.dangMang()`: xuống thú, máy chủ thiếu ảnh… thì không có chỉ số. Đặc tính
    móc ở một chỗ mọi đường sát thương đều qua (`Char.checkHapthuSatThuong`). Khi bật `sv.lau_dai`, phản đòn / hấp thụ /
    xuyên giáp của skin chịu trần `GioiHanChiSo`.
  - Nguồn: đổi điểm ở NPC đổi thưởng (5 skin đầu), giftcode / quà `DB:<id>:1:<phút>` (0 = vĩnh viễn). Tên kèm mô tả
    sinh từ chính bảng số (`tools/SinhSqlTenSkinThu.java`), không sửa tay SQL.

---

## 5. Gym, Chiếm Gym, PvP, Hầm ngục, boss

### 5.1 Gym (16 trưởng Gym)

`real.GymLauDai`, dữ liệu [noi_dung_gym.csv](../du_lieu/noi_dung_gym.csv). Vào từ Sổ tay huấn luyện → "Thách đấu Gym":
triệu hồi boss tại chỗ, **1 đấu 1** (chỉ người thách đấu gây được sát thương), nộ sau 5 phút, quá 10 phút chưa hạ thì thua.
Bật / tắt: `gym.bat`.

| Gym | Huy hiệu | Trưởng Gym | Cấp | Gym | Huy hiệu | Trưởng Gym | Cấp |
|---|---|---|---|---|---|---|---|
| Pewter | Đá | Golem | 12 | Violet | Gió | Dodrio | 53 |
| Cerulean | Thác | Gyarados | 19 | Azalea | Tổ Ong | Heracross | 55 |
| Vermilion | Sấm | Luxray | 23 | Goldenrod | Đồng Bằng | Tauros | 61 |
| Celadon | Cầu Vồng | Vileplume | 35 | Ecruteak | Sương | Dusknoir | 62 |
| Saffron | Đầm Lầy | Seismitoad | 38 | Olivine | Khoáng | Aegislash | 66 |
| Fuchsia | Linh Hồn | Gengar | 41 | Cianwood | Bão | Crabrawler | 68 |
| Cinnabar | Núi Lửa | Magcargo | 46 | Mahogany | Băng | Abomasnow | 75 |
| Viridian | Đất | Rhydon | 48 | Blackthorn | Rồng | Salamence | 83 |

- HP boss đủ cho ~105 giây đánh một mình đúng cấp với đồ +6.
- Lần đầu: huy hiệu (bit trong hồ sơ) + 1 đồ cầm tay Hoàn mỹ đúng dòng. Đánh lại **1 lần / tuần**: 3 Huy chương Gym
  (item 1039, khoá) để đổi trang trí (Áo choàng Gym, hào quang khi đủ 8 Gym của vùng).
- Không hồi sinh tại chỗ khi đang thách đấu Gym (`hoi_sinh.cam_gym`).

### 5.2 Chiếm Gym (chiếm thành cũ)

Tính năng **Chiếm Gym** thay "chiếm thành" của game gốc: các **đội** đánh PvP giành quyền giữ Gym trong khung giờ cố định.

- Lịch: khoá `sv.gioChiemThanh` / `sv.phutChiemThanh` / `sv.ngayChiemThanh`; mặc định (`-1`) là **21:00 thứ Ba và thứ Bảy**.
- Đóng góp = sát thương PvP trong bản đồ Gym lúc đang chiến (`real.ChiemGymLauDai`, số ở `ChiemGymLogic`). Thưởng tuần
  khi bật `sv.lau_dai` (thay thưởng cũ 10.000 lượng + 50 triệu xu, đã tắt):
  - đội thắng: danh hiệu trang trí 7 ngày cho thành viên lúc chiếm, +10% EXP 7 ngày (trong trần buff +100%), chia 20%
    **quỹ đội** (tiền đã thu từ thuế chợ, chia lại chứ không in thêm) cho thành viên có đóng góp, mỗi người ≤ 1 giờ thu nhập;
  - lượng khoá theo đóng góp (chống AFK: phải ≥ 1% đóng góp cao nhất và ≥ mức tối thiểu): đội thắng 20, đội khác 5, top 10
    toàn trận +5; trần 25 / người / tuần.
- Mỗi khoản phát ghi `tob_chiem_thanh_tuan` + nhật ký. Không hồi sinh tại chỗ trong Gym đang chiến.
- Cao nguyên Indigo là Gym chiếm chung của cả máy chủ ("thành trấn danh" cũ).

### 5.3 PvP

- **Thang PvP mùa** (`ThangPvp`): Elo, bậc Poké Ball → Great → Ultra → Master; ghép trận theo dải 10 cấp; mùa 13 tuần,
  **reset mềm** giữ 50% phần trên mức gốc; chống cày điểm (cùng cặp đấu lặp lại trong 10 phút không tính). Thưởng mùa là
  khung, danh hiệu và lượng theo bậc (Master 100, Ultra 40, Great 15). Tắt tính điểm: `pvp.tinh_diem.bat=0`.
- **Sàn đấu** (lôi đài cũ, `MapLoiDai`), **giải Liên minh** (liên đấu cũ, `MapLienDau`, sân Liên minh N), **chiến
  trường liên vùng / chiến vùng** (tống kim cũ) giữa Kanto, Johto, Hoenn.
- Công thức PvP: sát thương ×0,30, trần 8% HP tối đa mỗi đòn; chí mạng PvP ×1,5 phẳng, trần chí mạng 30%, né 20%, giảm
  sát thương 40%, hút 5%; thời lượng trạng thái ×0,5.
- Không hồi sinh tại chỗ ở bản đồ PvP (`hoi_sinh.cam_pvp`, `hoi_sinh.map_pvp`).

### 5.4 Hầm ngục bí ẩn

`real.HamNgucGame` (bật / tắt: `ham_nguc.bat`). Bản hiện tại chưa có bản đồ / tầng riêng: mỗi "tầng" là một thử thách hạ quái
chênh không quá 5 cấp, số lượng từ bố cục của ngày (mọi người cùng đề trong ngày), xong tầng thì tự sang tầng sau. Thưởng
**1 lần / ngày** theo tầng cao nhất: 0,2 giờ EXP + 5 lượng khoá từ tầng 5; chơi thêm không thưởng. Có bảng xếp hạng tầng.
Chữ người chơi thấy không nói "hạt giống" — dùng "đổi lối", "bố cục hôm nay".

### 5.5 Mê cung

Sự kiện vượt ải theo nhóm của game gốc (Vạn Tiên Trận / trấn yêu cũ, `RegionVantienTran`, `MapVanTienTran`, `BossMeCung`):
vào bằng **vé Mê cung**, quái ra theo đợt kèm boss Mê cung, giới hạn 1 giờ. Khác "Tháp Pokémon" (địa danh Lavender) và
"Hầm ngục bí ẩn". Không hồi sinh tại chỗ trong Mê cung (`hoi_sinh.cam_me_cung`).

### 5.6 Boss theo pha, quái tinh anh

`GiaiDoanBoss` (lớp thuần) + `real.BossLauDai`, áp cho mọi quái boss:

| Cơ chế | Chi tiết |
|---|---|
| Pha | 100–70% HP: đánh thường + 1 chiêu lớn; 70–40%: thêm chiêu vùng, gọi đệ; < 40%: "nộ" +30% tốc độ đánh, gọi đệ lần 2 |
| Báo trước chiêu lớn | gói -96: thời gian niệm 1,5–3 giây + hình vùng (tròn / nón / chữ nhật / vành); đứng ngoài vùng thì không trúng |
| Nộ theo thời gian | boss Gym sau 5 phút, boss thế giới sau 15 phút: sát thương ×3 (chống kéo dài bằng người đông) |
| Gọi đệ | ở 70% và 40% HP, 2..6 con theo số người đang đánh |
| Thông báo pha | gói -98 (vào pha, nộ, gọi đệ, khiên) |

**Quái tinh anh** (chữ người chơi thấy là "quái tinh anh", không dùng "Alpha"): 1/50 lần hồi sinh, to ×1,3, HP ×4, tấn
công ×1,3, rơi ×3, chắc chắn rơi 1 đá, 2% đồ Hoàn mỹ, TTK ~20 giây, có thông báo bản đồ
([noi_dung_quai_tinh_anh.csv](../du_lieu/noi_dung_quai_tinh_anh.csv)).

**Họ quái theo vùng**: mỗi tuyến 3–4 loài thuộc ít nhất 2 hệ, theo môi trường (Đồng cỏ: Thường / Cỏ / Bọ; Rừng: Bọ / Cỏ / Độc;
Hang: Đá / Đất / Ma; Bờ biển: Nước / Bay; Núi lửa: Lửa / Đá; Hồ: Nước / Băng; Tháp: Ma / Siêu linh). 20 dải 5 cấp ở
[noi_dung_quai_dai_cap.csv](../du_lieu/noi_dung_quai_dai_cap.csv); dải 86–100 dùng 36 loài Hoenn (template 139–174).

### 5.7 Boss thế giới

- Triệu hồi **thứ Bảy 20:00**, tuần đột kích thêm 12:30. Vị trí / mẫu cấu hình bằng khoá `boss.the_gioi.*`.
- Lịch 52 tuần ([noi_dung_boss_the_gioi.csv](../du_lieu/noi_dung_boss_the_gioi.csv)): 5 đột kích huyền thoại ở tuần 9 / 20 /
  28 / 36 / 48 (Zapdos, Entei, Darkrai, Rayquaza, Groudon & Kyogre; năm 2 xoay sang Articuno, Raikou, Suicune, Lugia,
  Kyogre & Groudon), các tuần khác 11 boss xoay vòng. HP cho ~10 phút với 20 người; nộ sau 15 phút.
- **Thưởng theo % đóng góp** (sát thương), không theo đòn cuối: ≥ 0,5% → Hộp tham gia (2–4 đá) + 1–3 vật phẩm đặc trưng
  tuần đột kích; top 10 → Hộp vàng (4–8 đá, 25% Mảnh tinh hoa, 20% Vảy rồng) và 10% Ruy băng Hoàn mỹ. Mọi thứ đều **khoá**;
  lượng khoá giữ mức nhỏ (10 / 3). Ghi `tob_boss_dong_gop`.
- Bảng đóng góp gửi mỗi 5 giây bằng gói -99 `POKE_DONG_GOP`.
- Không hồi sinh tại chỗ khi đã đánh boss còn sống cùng bản đồ (`hoi_sinh.cam_boss`).

### 5.8 Giao thức chiến đấu -95..-99 (đã chốt)

Năm gói -95..-99 là **bản chốt, không đổi trường nào** (`com.htbb.pokemon.GoiLauDai`, gửi từ `real.GoiLauDaiGame`; client
J2ME đọc ở `game.pokemon.PokeMangChienDau`). Mọi gói là gói **kèm** (`Message.kemTheo`) hoặc gói độc lập mới, big-endian;
client cũ bỏ qua được. Muốn thêm thông tin thì dùng số lệnh **mới** (grep trước khi gán), không sửa định dạng các gói này.

| Số | Hằng | Gửi khi | Nội dung |
|---|---|---|---|
| -95 | `POKE_DON` (đòn đánh, 13 byte) | kèm sau lệnh 9 ở mọi đường đánh quái, đòn quái → người, đòn PvP | `i16 nguon, u8 loaiNguon (0 người, 1 quái), i16 dich, u8 loaiDich, i32 dam, u8 co, u8 heChieu (0..17), u8 trangThai (0..5)` |
| -96 | `POKE_CANH_BAO` (niệm chiêu, 18 byte) | boss niệm chiêu lớn; gửi người đánh boss và người ở gần | `i16 boss, u16 idChieu, u8 heChieu, u8 hinh (0 tròn, 1 nón, 2 chữ nhật, 3 vành), i16 x, i16 y, i16 r_hoac_dai, i16 rong_hoac_goc, i16 huong (độ), u16 msNiem` |
| -97 | `POKE_TRANG_THAI` (7 byte) | kèm sau -95 khi gây trạng thái; khi gỡ | `i16 actor, u8 loaiActor, u8 trangThai, u8 chong, u16 msConLai` (0 = gỡ) |
| -98 | `POKE_BOSS` (10 byte) | boss vào pha / nộ / gọi đệ / khiên | `i16 boss, u8 pha, u8 suKien (0 vào pha, 1 nộ, 2 gọi đệ, 3 khiên), i32 hpConLai, u16 giayDenNo` |
| -99 | `POKE_DONG_GOP` | mỗi 5 giây cho người đóng góp boss thế giới | `u8 n, n × (i16 charId, u16 phanNghin)` + `u16 phanNghinCuaToi` (tối đa 10 dòng) |

Cờ `co` của -95 (bit): 0 chí mạng, 1 né, 2 khắc (≥ ×1,5), 3 kém (≤ ×0,7), 4 gần miễn (×0,3), 5 STAB, 6 trúng trạng thái.
Client hiển thị số sát thương màu theo cờ (vàng = chí mạng, xanh = khắc, xám = kém), biểu tượng trạng thái trên đầu, vùng
cảnh báo đếm ngược theo `msNiem`, thanh pha boss và bảng đóng góp.

Lưu ý: "-99" trong `RequestNPCInfoHandler` và -90..-92 trong `MapDoMenu` là **id NPC**, khác không gian số với
`Message.command`.

---

## 6. Nhiệm vụ, sự kiện, giữ chân

### 6.1 Nhiệm vụ

Toàn bộ nhiệm vụ nằm ở [nhiem_vu_moi.csv](../du_lieu/nhiem_vu_moi.csv) (khoảng 1.900 dòng, hơn 40 kiểu mục tiêu, đủ lời
thoại lúc nhận / đang làm / xong, gợi ý, dòng thưởng). Nguồn chữ là `tools/pokemon/nv_*.py`, bộ sinh
`tools/pokemon/sinh_nhiem_vu.py`. Báo cáo sinh tự động (số lượng, nhịp độ, yêu cầu engine):
[NHIEM_VU_THIET_KE.md](../du_lieu/NHIEM_VU_THIET_KE.md).

| Loại | Cấu trúc |
|---|---|
| Hướng dẫn `HD.01..10` | 30 phút đầu: chiêu, thuốc, trang bị, Pokédex; xong ở cấp ~8 |
| Chính năm 1 | 52 chương (13 cung, Kanto + Johto, cấp 1–90, 16 huy hiệu Gym), mỗi chương nhiều bước: lời nhờ / đưa thư → dọn đường → thu thập → thử thách / Gym; kết ở C52.7 |
| Chính năm 2 | 52 chương Hoenn (H1 Đất liền Hoenn … H13 Khúc vĩ thanh), cấp 91–100 |
| Phụ theo thị trấn, tuyến | chuỗi 3–5 bước, bước cuối cho danh hiệu thị trấn + 1 món đồ |
| Hằng ngày | rút 5 việc, nhận thưởng 3 / ngày |
| Hằng tuần | 2 / tuần, đặt lại thứ Hai 05:00 |
| Pokédex, sư phạm, Hầm ngục, đội, quay lại | theo hệ thống tương ứng |
| Huyền thoại (HT-*), ẩn, thành tựu | khoá theo cấp / điều kiện ẩn; thành tựu không cộng chỉ số |

- **Vé chương**: mỗi Pokémon nhận 2 vé / tuần (tích tối đa 4, bắt đầu với 4). Mỗi chương cần cấp tối thiểu + nhiệm vụ trước.
  52 chương cần ít nhất ~24 tuần: người cày không xong năm 1 quá sớm.
- **Ngân sách thưởng mỗi dòng** tính bằng giờ EXP / giờ xu ở cấp tối thiểu của nhiệm vụ (ví dụ chương 0,30 giờ EXP, bước
  Gym 0,45; ngày 0,10; tuần 1,0 + 20 lượng khoá). Dưới cấp 16 thưởng hướng dẫn / chính / phụ ×3 để tiến hoá đầu trong
  ~1 giờ. Nhiệm vụ không bao giờ thưởng lượng (trừ thành tựu lớn).
- Engine: chuỗi chính / hướng dẫn chạy trên `data_quest` cũ (bản `data_quest_moi`, `ORDER BY id`); phần còn lại chạy trên
  `real.NhiemVuEngine` + `NhiemVuMau` (3 khe phụ cùng lúc, nhánh lựa chọn, câu đố kiểm trong chat, khoá `truoc` / ẩn, vật
  phẩm nhiệm vụ 10–50). Mở từ Sổ tay → "Nhiệm vụ của tôi". Danh sách gửi client được lọc theo cấp (≤ 127 mục).

### 6.2 Sự kiện: lịch năm theo ngày thật + tuần Pokémon

Chủ game duyệt 19/10/2026: **mọi sự kiện tự bật / tắt đúng ngày thật** (giờ Việt Nam, `Asia/Ho_Chi_Minh`). Lịch ở
`com.htbb.pokemon.LichNam` (lễ âm lịch tính bằng `AmLich` — thuật toán Hồ Ngọc Đức, múi giờ +7; test đối chiếu Tết /
Giỗ Tổ / Vu Lan / Trung Thu 2026–2035). Máy chủ ghi mọi lần diễn ra (năm trước .. năm sau) vào `tob_su_kien_lich`, và
mỗi 15 giây (`real.LichSuKien.dongBoLichNam`) đặt `tob_su_kien.bat` theo lịch cho mọi mã `tu_dong = 1`. Admin ghi đè
**một năm** ở `/admin/sukien` (tắt năm nay / đổi giờ / mở ngay / về theo lịch) hoặc chuyển cả sự kiện sang thủ công.

**Mốc mở / đóng luôn 04:00**: bật / tắt sự kiện cần khởi động lại, và đổi do lịch khi bảo trì hằng ngày (04:30–05:30)
còn ≤ 3 giờ thì `LichSuKien` **không** khởi động lại riêng mà chờ bảo trì — cả năm không thêm lần khởi động lại nào.
Thay đổi tay của admin (hay lịch lúc bảo trì còn xa / đã tắt) vẫn khởi động lại như cũ (gom + báo trong game).

| Mã | Sự kiện | Cửa sổ (mở 04:00 → đóng 04:00) | 2026 | 2027 |
|---|---|---|---|---|
| `tetduonglich` | Tết Dương lịch | 30/12 → 04/01 | 30/12/2025 → 04/01/2026 | 30/12/2026 → 04/01/2027 |
| `tet2017` | Tết Nguyên Đán | mùng 1 âm − 7 ngày → mùng 10 | 10/02 → 27/02 | 30/01 → 16/02 |
| `tet123` | Ba ngày Tết | mùng 1 → mùng 4 | 17/02 → 20/02 | 06/02 → 09/02 |
| `moi:valentine` | Valentine | 11/02 → 18/02 | 11/02 → 18/02 | 11/02 → 18/02 |
| `quocte83` | Quốc tế Phụ nữ 8/3 | 05/03 → 12/03 | 05/03 → 12/03 | 05/03 → 12/03 |
| `gioto` | Giỗ Tổ Hùng Vương | 10/3 âm − 3 → + 4 | 23/04 → 30/04 | 13/04 → 20/04 |
| `moi:3004` | Lễ 30/4 – 1/5 | 28/04 → 04/05 | 28/04 → 04/05 | 28/04 → 04/05 |
| `moi:thieunhi` | Quốc tế Thiếu nhi 1/6 | 30/05 → 06/06 | 30/05 → 06/06 | 30/05 → 06/06 |
| `he2017` | Sự kiện Hè | 15/06 → 15/07 | 15/06 → 15/07 | 15/06 → 15/07 |
| `moi:hamngucmua` | Hầm ngục bí ẩn mùa hè | 15/07 → 29/07 | 15/07 → 29/07 | 15/07 → 29/07 |
| `vulan` | Vu Lan | 15/7 âm − 4 → + 3 | 23/08 → 30/08 | 12/08 → 19/08 |
| `moi:quockhanh` | Quốc khánh 2/9 | 30/08 → 05/09 | 30/08 → 05/09 | 30/08 → 05/09 |
| `trungthu` | Trung Thu | 15/8 âm − 10 → + 4 | 15/09 → 29/09 | 05/09 → 19/09 |
| `moi:2010` | Phụ nữ Việt Nam 20/10 | 17/10 → 23/10 | 17/10 → 23/10 | 17/10 → 23/10 |
| `halloween` | Halloween | 18/10 → 03/11 | 18/10 → 03/11 | 18/10 → 03/11 |
| `demhalloween` | Đêm Halloween | 31/10 → 01/11 | 31/10 → 01/11 | 31/10 → 01/11 |
| `sinhnhat2011` | Ngày Nhà giáo Việt Nam 20/11 | 17/11 → 24/11 | 17/11 → 24/11 | 17/11 → 24/11 |
| `blackfriday` (+ `…daugia`, `…ban`) | Black Friday | thứ Sáu thứ 4 tháng 11 − 1 → + 4 | 26/11 → 01/12 | 25/11 → 30/11 |
| `noel` | Giáng sinh | 18/12 → 28/12 | 18/12 → 28/12 | 18/12 → 28/12 |
| `demnoel` | Đêm Noel | 24/12 → 26/12 | 24/12 → 26/12 | 24/12 → 26/12 |
| `moi:tongket` | Tổng kết năm | 28/12 → 04/01 | 28/12/2026 → 04/01/2027 | 28/12/2027 → 04/01/2028 |

Tuần Pokémon (bảng dưới) vẫn theo tuần ISO (`SuKienLauDai.LICH`, [su_kien_nam.csv](../du_lieu/su_kien_nam.csv)), thứ Hai
04:00 → thứ Hai tuần sau 04:00, vì hệ của tuần hệ và huyền thoại đột kích gắn với số tuần. Bật tay (không có lịch):
`noel2023`, `halloween2015`, `he2015` (bản cũ), `mini*`, `anxuluongtop`. **Đã bỏ** (sự kiện riêng của game kiếm hiệp cũ):
World Cup bình chọn Messi/Ronaldo (`worldcup*`), Sinh nhật Ngudẻ, Đua top Hoa Lư.

**Website tự đăng tin** (`web/server/plugins/tin-su-kien.ts`, chuyên mục "Sự kiện"): "Sắp diễn ra" 3 ngày trước
(tuần Pokémon: 1 ngày), "Đang diễn ra" lúc mở, "Đã kết thúc" lúc đóng; nội dung viết từ mô tả, phần thưởng và cửa sổ
của sự kiện; sự kiện phụ (Ba ngày Tết, Đêm Halloween, Đêm Noel, Black Friday đấu giá / mở bán) nhắc trong tin của sự kiện
chính. Trang chủ có mục "Sự kiện" (đang diễn ra + 30 ngày tới, đếm ngược), Cẩm nang › Sự kiện có lịch lễ hội 12 tháng.

| Nhóm | Tuần (năm 1) | Cơ chế |
|---|---|---|
| Lễ Việt Nam | theo ngày thật (bảng trên) | Đổi vật phẩm sự kiện, NPC, nấu bánh, thả đèn |
| Ngày cộng đồng | 3, 12, 19, 25, 31, 40, 50 | 3 giờ: dòng khởi đầu xuất hiện dày, shiny ×5 |
| Tuần Shiny | 7, 34 | shiny ×10 |
| Đột kích huyền thoại | 9, 20, 28, 36, 48 | boss thế giới thêm khung, thưởng theo đóng góp |
| Tuần hệ | 2 Băng, 11 Cỏ, 18 Lửa, 27 Nước, 33 Điện, 42 Siêu linh | quái hệ X ×2, chiêu hệ X +10% |
| Lễ hội Pokédex / bùng phát | 14, 45 / 21 | điểm nghiên cứu ×2 / một loài ×5 trên một tuyến |
| Chiến vùng | 16, 30, 49 | Kanto / Johto / Hoenn giành cờ |
| Mùa giải PvP | 13, 26, 39, 52 | chốt bậc mùa |

- **Ngân sách**: cả năm 66,5 giờ thu nhập (test chặn ≤ 70). Chỉ **25%** trả bằng xu; còn lại là vật phẩm **khoá, hết hạn
  khi sự kiện kết thúc** hoặc đồ trang trí, nên sự kiện không tích tiền. Mỗi tuần 1–3 vật phẩm, tối đa 20 cái / tuần
  ([noi_dung_su_kien_vat_pham.csv](../du_lieu/noi_dung_su_kien_vat_pham.csv)).
- **Năm 2 xoay vòng** (cột `ten_nam2`): đổi huyền thoại, hệ của tuần hệ, dòng của ngày cộng đồng, linh vật ngày lễ.
- Mã sự kiện mới có tiền tố `moi:*` trong `SuKien.DANH_MUC`; các cờ sự kiện kiếm hiệp cũ (`sv.isSuKien*`) chỉ còn để tương
  thích (mặc định khi bảng chưa có dòng).

### 6.3 Giữ chân người chơi

| Tầng | Mục tiêu luôn có sẵn |
|---|---|
| Phiên (15–30 phút) | 3 nhiệm vụ hằng ngày, 1 bước chương, 1 lượt Hầm ngục, 1 lần nâng cấp, 1 loài Pokédex mới |
| Tuần | 2 nhiệm vụ tuần, 2 vé chương, Gym đánh lại, boss thế giới / đột kích, Chiếm Gym, xếp hạng tuần |
| Mùa (2–6 tháng) | mốc cấp / bậc đồ mới, mùa PvP 13 tuần, 8 huy hiệu Gym của vùng, săn huyền thoại, danh hiệu mùa, vé mùa |

Vòng lõi: **đánh quái theo tuyến → nhiệm vụ / Pokédex → đồ + đá → nâng cấp → mở chương / Gym → tuyến mới.**

| Cơ chế | Luật |
|---|---|
| Lịch đăng nhập 7 ngày | 3, 4, 5, 6, 8, 10, 15 lượng khoá rồi lặp; theo **tài khoản** (chặn acc phụ); nghỉ 1 ngày vẫn giữ chuỗi, nghỉ từ 2 ngày liền về ngày 1. Đủ 20 ngày / tháng: danh hiệu trang trí 30 ngày |
| Quà hoạt động | mốc 15 / 30 / 60 / 120 phút **hoạt động thật** mỗi ngày / tài khoản (đứng yên quá 60 giây không tính): đá khoá, 5% giờ xu, lượng khoá, nguyên liệu khoá |
| Sư phạm | thầy (cấp ≥ 30) kèm trò thấp hơn ≥ 20 cấp; trò +20% EXP khi cùng nhóm; cả hai nhận thưởng khi trò lên 20 / 40 hoặc tiến hoá |
| Tổ đội, bạn bè | +10% / người (tối đa +40%); bạn bè cùng nhóm +5% (thiết kế) |
| Đội | nhiệm vụ đội tuần, quỹ đội, Chiếm Gym |
| Xếp hạng | cấp, Pokédex, Hầm ngục, PvP; ẩn xu / lượng trên bảng top; lượng chỉ phát theo top tuần |
| Chống kiệt sức | không có việc bắt buộc nhiều giờ; năng lượng tích 2 ngày, vé chương tích 2 tuần, lịch đăng nhập có ân hạn |

---

## 7. Hệ sinh thái

Bật bằng `sv.he_sinh_thai=1` (cần chạy `sql/pokemon_he_sinh_thai.sql`, khởi động lại). Lối vào: mục "Hệ sinh thái" trong
menu NPC đổi thưởng, hoặc gõ `hst`; phím tắt `cauca`, `dao`, `dua`. Đơn vị tiền là **‰H** = phần nghìn giờ thu nhập ở cấp
người chơi. Mục tiêu: mỗi kiểu người chơi (chiến đấu, sưu tầm, xã hội, chế tác / thương nhân, chơi nhẹ) có việc dài hạn,
và hệ sinh thái **hút ròng** xu (hố hút ≈ 118% nguồn của chính nó).

### 7.1 Thế giới: ngày đêm, thời tiết, mùa, bùng phát

- **Ngày / đêm** theo giờ máy chủ: 19:00–05:59 là đêm.
- **Thời tiết theo ngày cho từng vùng sinh thái** (vùng = cột `loai` của `thiet_ke_map.csv`): quang, nắng, mưa, tuyết, bão
  cát, sương mù; tính tất định từ (ngày, vùng), trọng số theo vùng và mùa (vùng tuyết ~70% tuyết; hang và trong nhà luôn
  quang; mùa hạ tăng nắng; mùa đông ở hồ / núi mưa thành tuyết).
- Ảnh hưởng: hệ hợp trời sinh nhiều gấp 3 (mưa: Nước / Điện; nắng: Lửa / Cỏ; tuyết: Băng; bão cát: Đá / Đất / Thép; sương
  mù: Ma / Siêu linh; ban đêm thêm Ma / Bóng tối / Độc). Sát thương chiêu: mưa Nước +10% / Lửa −10%; nắng Lửa +10% / Nước
  −10%; tuyết Băng +10%; bão cát Đá +10%; sương mù Ma +10% (chỉ đòn của người chơi).
- **Thay loài khi hồi sinh**: 15% số lần hồi sinh đổi sang ứng viên cùng dải cấp ±4 (tối đa 6 loài / điểm); một phần ba là
  **loài di cư** (chỉ có 6 tháng liền mỗi năm); **quái hiếm** cấp +3..+8 chỉ ra trong một kiểu thời tiết (có thể chỉ ban
  đêm), báo kênh thế giới tối đa 1 lần / 10 phút mỗi loài. Không đổi: mục tiêu nhiệm vụ chính, boss, vật thể, bản đồ đặc
  biệt. Không đổi số lượng, vị trí, id quái.
- **Bùng phát**: 4 khung / ngày (11h, 17h, 20h, 22h), mỗi khung 1 giờ, một cặp (tuyến, loài của tuyến); 40% số lần hồi sinh
  trên tuyến đó thành loài bùng phát; có thông báo đầu / cuối.
- Hiệu ứng client qua gói có sẵn `-39` (0 mưa, 1 lá rơi, 2 tuyết, 6 sương mù, 7 nắng gắt), gửi theo vùng khi đổi.

### 7.2 Kỹ năng sống

- 5 kỹ năng (câu cá, khảo cổ, trồng berry, nấu ăn, chế thuốc), mỗi kỹ năng cấp 1..50; EXP lên cấp L là `40 × 1,105^L`
  (~5 tháng dùng hết thể lực cho **một** kỹ năng).
- **Thể lực**: 100 / ngày, tích tối đa 200, nghỉ từ 2 ngày thì đầy; **riêng biệt** với năng lượng huấn luyện. Tốn 2 khi
  câu, 4 khi đào, 3 khi nấu / chế; thu hoạch vườn không tốn.
- **Phí nguyên liệu bắt buộc** (hố hút): mồi câu 2‰H, xẻng 4‰H, bếp 5‰H.
- Vật liệu nằm trong **Túi sinh thái** (`tob_hst_nhan_vat`, không phải `data_item`), mỗi chồng có bậc 1..5 theo cấp người
  lấy; chỉ thành phẩm cuối (thuốc, đá, pet) mới ra vật phẩm thật.

### 7.3 Câu cá, khảo cổ, vườn, nấu ăn

- **Câu cá** (`cauca`): **chỉ câu được ở hồ** — vùng nước thật liên thông cách viền bản đồ hơn 2 ô, từ 6 ô tới 40% diện
  tích bản đồ (biển, sông ra mép, vũng nhỏ không phải hồ). Đứng cách hồ ≤ 2 ô, nếu không thì báo "Chỉ có thể câu cá ở hồ."
  kèm điểm câu gần nhất. Hồ lớn (≥ 60 ô) có cá hiếm (Feebas, Dratini…), ao chỉ có cá thường. 21 kết quả (rác, vỏ sò, rong,
  Oran, 15 loài cá, ngọc trai). Điểm câu: [diem_cau_ca.csv](../du_lieu/diem_cau_ca.csv) / `data_hst_ho_cau`.
- **Khảo cổ** (`dao`) ở hang, núi, sa mạc, tuyết: 6 điểm đào / bản đồ / ngày (tất định); đứng xa thì máy dò chỉ hướng và in
  `Điểm đào: (x,y)` (client đọc định dạng này, **không đổi**). Ra đá cuội, xương cổ, 7 loại mảnh đá tiến hoá, vảy tim, đá
  sao rơi, hoá thạch (1–2%).
- **Vườn berry**: dùng vườn cá nhân có sẵn; hạt giống thành 8 loại berry; mỗi lần thu hoạch thêm `2 + hạng` berry. Bạn bè
  tưới +5% / lần (tối đa +25%; 5 lần / ngày mỗi người cho và mỗi vườn nhận). Trang trí vườn chế từ khảo cổ, 0 chỉ số; điểm
  thân thiện ≥ 50 cho danh hiệu.
- **30 công thức**: nấu Bánh Poké (+30 thể lực, tối đa 2 / ngày), mồi thơm, súp berry; chế thuốc tối đa 5 mẻ / ngày (dùng
  thì ra 10 bình HP / MP thật); ghép 12 mảnh thành **đá tiến hoá thật** (item 960–966); phục hồi hoá thạch thành pet. Giá
  trị đầu ra ≤ nguyên liệu; không vật liệu nào chỉ để bán NPC.

### 7.4 Nhà trẻ và trứng

- Gửi 1–2 pet đang sở hữu; phí **50‰H / ngày**. Chủ hạ 1 quái = 1 bước (nên vẫn bị năng lượng giới hạn); 300 bước được 1
  trứng; tối đa 2 trứng / ngày; túi chứa 6 trứng. Trứng nở sau 250–800 bước.
- **IV** HP / công / thủ 0..31: mỗi chỉ số lấy của cha hoặc mẹ, 15% đột biến ±1..3; pet không nở từ trứng có IV hoang dã
  0..15.
- **Shiny trứng** 1/512, pity 200. **Phẩm chất S** (tổng IV ≥ 84) nở pet cấp 2 nếu loài có cấp 2 (lợi ích kinh tế, không
  cộng chỉ số chiến đấu). IV hiện chỉ quyết định phẩm chất, cấp nở và giá trứng.
- Trứng là hàng hoá trên chợ (giá theo IV, shiny ×5); NPC không mua trứng.

### 7.5 Chợ sinh thái, đơn đặt hàng

| Khoản | Giá trị |
|---|---|
| Thuế bán / phí đăng | 6% (đốt) / 1% (không hoàn) |
| Giá rao | chọn ×1 / ×1,5 / ×2 / ×3 giá tham chiếu, trần ×5 (chặn rửa tiền bằng giá ảo) |
| NPC mua lại | 40% giá tham chiếu, **trần 0,1 giờ thu nhập / ngày**; không mua trang trí, trứng |
| Tin bán / đơn | tối đa 8 / 4 mỗi người, hết hạn sau 3 ngày (hàng / ký quỹ về hộp thư) |
| Đơn đặt hàng | ký quỹ giá × số + 1%; người bán giao dần, nhận tiền trừ thuế |

Mọi thao tác tranh chấp dùng `UPDATE … WHERE trang_thai = 0` rồi kiểm số dòng: chỉ một bên thắng; người bán offline nhận tiền
vào `tob_hst_hop_thu`.

### 7.6 Giải trí và sưu tầm

- **"Ai là Pokémon?"** 12:30 và 21:30 trên kênh thế giới (gợi ý: hệ, số chữ, chữ đầu); người đúng đầu tiên 3 lượng khoá.
- **Đua Pokémon** (`dua`): đăng ký từ 18:50, xuất phát 19:00 tại tuyến Goldenrod; 5 mốc; top 3: 5 / 3 / 2 lượng khoá.
- **Giải đấu tuần**: loại trực tiếp, công bố thứ Bảy 20:00; kết quả lấy từ trận ở giải Liên minh; vô địch 20 lượng khoá.
- **Album**: quái hiếm, bùng phát, cá hiếm, trứng shiny, hoá thạch; mốc 10 / 25 / 50 / 100 ảnh.
- **Nghiên cứu Pokédex**: 6 kiểu mỗi loài (câu được, ấp nở, hạ trong thời tiết ưa thích, hạ ban đêm, gặp khi hiếm / bùng
  phát, nở shiny), 2 lượng khoá mỗi việc; loài "xong" khi làm đủ 2 việc.
- **Hoàn thành vùng**: 20 lượng khoá, danh hiệu, thiết kế +0,5% chỉ số vĩnh viễn mỗi vùng (trần 3%, hiện mới ghi nhận, chưa
  cộng vào chỉ số).
- Danh hiệu sinh thái: `idEffDanhHieu` 9600..9615.

---

## 8. Hồi sinh và Tự động đánh

### 8.1 Gục và hồi sinh

Khi Pokémon gục, người chơi **chọn** trong hộp thoại "Pokémon của bạn đã gục!" (`com.htbb.pokemon.HoiSinh`,
`real.HoiSinhGame`, gói -101). Bật luồng mới: `hoi_sinh.bat`; mọi khoá `hoi_sinh.*` áp dụng ngay.

| Lựa chọn (mã) | Giá | HP |
|---|---|---|
| 0 Tân thủ tại chỗ | miễn phí, chỉ dưới cấp `tan_thu_cap` (15), hồi chiêu 30 giây | 50% |
| 1 Thuốc hồi sinh (potion 119) | giá cửa hàng theo cấp: 30% giờ thu nhập, tối thiểu 407 xu | 30% |
| 2 Thuốc hồi sinh tối đa (item 1207) | Kecleon 5 lượng khoá, 3 / tuần | 100% |
| 3 Trả xu | 40% giờ thu nhập × hệ số gục lặp, tối thiểu 200 xu (hố hút) | 50% |
| 4 Trả lượng khoá | 2 × hệ số, trần 10 (0 = tắt) | 50% |
| 5 Về Trung tâm Pokémon | miễn phí, có phạt nhẹ | đầy |
| 6 Chờ đồng đội | thuốc của đồng đội ở gần (chờ 60 giây) | theo thuốc |

- **Hệ số gục lặp** 100 / 150 / 200 / 300% cho lần gục 1 / 2 / 3 / 4+ trong 30 phút (lịch sử giữ trên máy chủ).
- Tại chỗ chỉ bấm được sau 5 giây và cách lần tại chỗ trước 60 giây; tối đa 6 lần hồi sinh tại chỗ bằng vật phẩm của chính
  mình / 60 phút. Máy chủ kiểm lại mọi điều kiện.
- **Cấm tại chỗ** (chỉ còn Về Trung tâm / Chờ đồng đội): sàn đấu, giải Liên minh, chiến trường, lãnh địa đội, Chiếm Gym đang
  đánh, đấu trường; đang thách đấu Gym; đã đánh boss còn sống cùng bản đồ; Mê cung; bản đồ thêm (`hoi_sinh.map_cam_them`).
- **Về Trung tâm Pokémon**: ở Hoenn về Trung tâm của thị trấn Hoenn gần nhất; Kanto / Johto về làng nhà. Phạt (không áp tân
  thủ): mất 2% EXP lên cấp hiện tại (trần 15 phút cày, **không bao giờ tụt cấp**), mất 5% độ bền tối đa mỗi trang bị đang mặc
  (không vỡ đồ), **Mệt mỏi** 5 phút (−20% EXP hạ quái, −10% tấn công). Không rơi đồ.
- "Auto về Map Up" không còn bay lại bãi luyện sau khi gục (`hoi_sinh.auto_ve_map_up=0`), để không treo máy vô hạn.
- Client cũ không có hộp -101 nhận menu thường (id -803) với cùng lựa chọn; đồng đội cứu qua menu -804.

### 8.2 Tự động đánh

Một tính năng **Tự động đánh** do **máy chủ** điều khiển thay cho các mod auto phía client cũ (`com.htbb.pokemon.TuDong`,
`real.TuDongGame`, gói -100).

1. **Không thưởng thêm**: EXP / xu / đồ rơi đi qua đúng đường hạ quái thường, nên chịu nguyên năng lượng huấn luyện.
2. **Trần giờ / ngày**: `tu_dong.gio_moi_ngay` (5) + `tu_dong.gio_them_the_thang` (2 khi Thẻ tháng còn hạn); chỉ tính lúc đang
   bật, chỉ khi **online**; đặt lại 00:00; phiên mới luôn tắt.
3. **Điều kiện bật**: chỉ ở bãi luyện tập có Pokémon hoang dã (không thành phố, nhà, sàn đấu, chiến trường, khu Chiếm Gym),
   không đang PK, không gục, còn giờ.
4. **Tự dừng khi**: hết giờ, đổi bản đồ, ra khỏi bán kính (`tu_dong.ban_kinh` 240 px + 64 px), gục, và nếu
   `tu_dong.tat_khi_pvp=1`: bị Pokémon khác tấn công, tự đánh người, bật PK, vào sàn đấu / Gym / chiến trường, đánh boss.
5. **Chống bot**: nhịp đánh / thuốc / nhặt theo cửa sổ trượt (`tu_dong.nhip_*`); vượt thì bỏ lệnh và đếm vi phạm, đủ
   `tu_dong.vi_pham_tat` (20) thì dừng + ghi nghi vấn. Với mọi người chơi, nhịp đánh trên `tu_dong.nhip_nguoi` chỉ ghi nghi
   vấn, không chặn.
6. **Gục khi đang tự động**: chỉ tự dùng vật phẩm hồi sinh nếu người chơi bật "Tự hồi sinh", có thuốc, bản đồ không cấm và
   còn lượt `tu_dong.hoi_sinh_moi_gio` (2 / giờ); còn lại thì dừng và về Trung tâm (có phạt).

7. **Máy chủ điều khiển từng bước** (`tu_dong.ke_hoach=1`, client 0.9.2+, `com.htbb.pokemon.TuDongKeHoach`): client xin
   kế hoạch ~0,6 giây / lần (gói -100 loại 4), máy chủ trả một việc (đánh / uống thuốc / nhặt / về điểm bắt đầu / chờ /
   dừng) kèm lý do hiện trên thanh trạng thái. Thứ tự: đầy túi → dừng (`dung_khi_day_tui`); HP / PP dưới ngưỡng người
   chơi chọn (mặc định `nguong_thuoc_hp` 50 / `nguong_thuoc_mp` 30) → bình nhỏ nhất đủ bù; HP dưới `nguong_hp_thap` (25)
   mà hết thuốc → rút về điểm bắt đầu nghỉ tới `hoi_phuc_toi` (80) % (hoặc dừng, `hp_thap_dung`); người khác bật PK
   trong `tranh_pk_px` → lùi về chờ; đồ rơi của mình trong `nhat_ban_kinh` → nhặt; mục tiêu = Pokémon hoang dã **gần
   nhất theo đường đi** (BFS trên ô đi được, không xuyên tường) trong vùng, không cao hơn quá `chenh_cap_tren` (5) cấp /
   thấp hơn quá `chenh_cap_duoi`, không boss (`danh_boss=0`), không quá trâu (`hp_quai_toi_da_pt` 500 %), không phải xe
   giao hàng của người khác; giữ mục tiêu đang đánh. Chiêu = sát thương × khắc hệ × STAB (`chon_chieu_theo_he`), bỏ chiêu
   đang hồi / thiếu PP. Không có mục tiêu thì báo rõ vì sao (mạnh hơn quá nhiều / chỉ có boss / bị chắn / chờ). Client
   cũ (0.9.1) vẫn tự chọn như trước; máy chủ vẫn giới hạn giờ, nhịp, vùng.
8. **Không im lặng**: mọi lần từ chối bật / dừng đều có câu trong gói -100 và chữ trên đầu nhân vật (cả client cũ, cả
   khi Tiện ích "Thông báo: tắt"); `tu_dong.cap_toi_thieu` (0) và `tu_dong.map_cam` báo rõ cấp / bản đồ. Không có khoá
   Thành viên / kích hoạt nào chặn Tự động đánh (`thanh_vien.chuc_nang`, `kich_hoat.chuc_nang` không có mục này).
9. **Tự di chuyển**: bấm phím di chuyển khi đang tự động thì dừng hẳn (`dung_khi_tu_di=1`, lý do gửi máy chủ); 0 = nhường
   3 giây như cũ. Đổi bản đồ / ra khỏi vùng vẫn dừng.
10. **Hồi sinh**: chỉ Thuốc hồi sinh thường (mua bằng xu) khi bật "Tự hồi sinh"; Thuốc hồi sinh tối đa (lượng khoá) chỉ
    khi `tu_dong.hoi_sinh_tra_phi=1` **và** người chơi chọn "Tự hồi sinh: cả đồ lượng". Không bao giờ tự trả xu / lượng.

Bảng Tự động đánh trên client: thứ tự 4 chiêu (khi máy chủ không điều khiển), ngưỡng thuốc HP / PP, kiểu nhặt đồ, về
hồi phục, vùng đánh 4..20 ô, tự hồi sinh (tắt / thuốc thường / cả đồ lượng); mọi thao tác đi qua đúng hàm như bấm tay
(có hoạt ảnh, không lướt).

---

## 9. Kinh tế và chống lạm phát

### 9.1 Ba đồng tiền

| Đồng | Giao dịch | Nguồn | Dùng cho |
|---|---|---|---|
| **Xu** | được, qua chợ (thuế 6% + phí đăng 1%, đốt) | quái (rơi 30% số lần, theo lệch cấp × năng lượng), nhiệm vụ, ≤ 25% ngân sách sự kiện, phân rã | thuốc, sửa đồ, dịch chuyển, đồ theo bậc, **nâng cấp**, **ngọc hệ**, học chiêu, hồi sinh |
| **Lượng khoá** | không | nhiệm vụ ngày / tuần / chính, lịch đăng nhập, quà hoạt động, giftcode, thành tựu, Gym, boss, Chiếm Gym, Hầm ngục | bùa bảo hộ, thuốc hiếm (Kecleon, có giới hạn tuần), bản khoá tăng tốc, trang trí khoá |
| **Lượng** | được (chuyển, phí 5%) | xếp hạng tuần (top 1: 50, top 10: 20, top 100: 8, top 1000: 3), thành tựu lớn, top sự kiện, mùa PvP; nạp tiền nếu bật | **chỉ** trang trí, tiện ích (đổi tên, mở rộng túi), bản cao cấp tăng tốc |

**Đã bỏ hẳn** khi bật `sv.lau_dai`: lượng / lượng khoá rơi từ quái; quà 10 triệu xu ở cấp 35 (nay 5.000 xu + 6 lượng khoá + 10 nguyên liệu khoá mỗi loại cấp 6 cho mọi chế độ, xem 9.6);
giftcode ra lượng hoặc xu; mốc nạp; hoàn xu khi tiêu lượng; VIP theo nạp; shop lượng bán đồ có chỉ số; kẹo EXP cố định;
cộng lượng khoá hằng ngày cho mọi nhân vật cấp ≥ 40.

### 9.2 Nguồn vào và hố hút

| Nguồn xu | Tỉ trọng (mô phỏng) | Hố hút xu | Tỉ trọng |
|---|---|---|---|
| Quái (`xu_moi_quai(L) = 10·1,05^L`, rơi 30%) | ~89% | Nâng cấp +1..+15 | ~65% |
| Nhiệm vụ | ~9% | Thuốc + sửa đồ | ~23% |
| Sự kiện (25% ngân sách) | ~1% | Mua đồ theo bậc | ~4% |
| Phân rã | ~0,5% | Ngọc hệ | ~4% |
| | | Dịch chuyển | ~3% |
| | | Học chiêu, thuế chợ, hồi sinh, phí hệ sinh thái | phần còn lại |

Mục tiêu: xu ra / vào 90–105%, lượng khoá ~95%; lượng tích chậm vì nguồn nhỏ và chỉ mua trang trí.

### 9.3 Thuế, phí, giá theo thu nhập

| Khoản | Luật | Khoá `cau_hinh` |
|---|---|---|
| Thuế chợ ký gửi | 6% đốt khi bán được | `thue.cho` |
| Phí đăng bán | 1% đốt, không hoàn, tối thiểu 1 xu | `thue.dang_ban` |
| Chuyển lượng | 5% (làm tròn lên) | `thue.chuyen_luong` |
| Trần chuyển xu trực tiếp | 5 giờ thu nhập / ngày (muốn nhiều hơn phải qua chợ, chịu thuế) | `thue.tran_chuyen_xu_gio` |
| Dịch chuyển | 3% thu nhập 1 giờ | `phi.dich_chuyen_phan_tram_gio` |
| Hồi sinh trả xu | 40% thu nhập 1 giờ × hệ số gục lặp (luồng mới) | `hoi_sinh.xu_phan_tram_gio` |
| Sửa đồ | công thức cũ, trần 8% thu nhập 1 giờ mỗi món | `tieu_hao.sua_do_phan_tram_thu_nhap` |
| Bán đồ cho NPC | 5% giá, không cộng theo cấp cường hoá | `phi.ban_npc_phan_tram` |
| Rơi xu / đá | 30% số quái / 15‰ (khi lệch cấp ≤ 5) | `roi.xu.phan_tram`, `roi.da_nang_cap.phan_nghin` |
| Thuốc | tổng chi thuốc ≤ 12% thu nhập; mỗi món 0,05–5% thu nhập 1 giờ | (dữ liệu) |
| Xổ số | thuế 10% xu / 15% lượng | — |

Bot: xu, đá, đồ rơi đều theo năng lượng; thuốc và sửa đồ tính theo giờ thật, nên treo máy lâu bị lỗ. Tín hiệu nghi bot: năng
lượng = 0 liên tục nhiều ngày, nhặt đồ đều hơn 20 giờ / ngày, nhịp đánh bất thường (kết hợp cấm theo máy).

### 9.4 Theo dõi cung tiền

Mỗi ngày (04:00) máy chủ ghi `tob_kinh_te_ngay` (xu vào / ra qua `LauDai.ghiXuVao/ghiXuRa`, `Char.addXu/subXu` có lý do).

| Chỉ số | Xanh | Vàng | Đỏ (hành động) |
|---|---|---|---|
| Cung tiền thực (xu TB / người ÷ thu nhập 1 giờ ở cấp trung vị) | ≤ 10 giờ | 10–20 | > 20 |
| Tăng cung tiền thực / tháng | ≤ 5% | 5–10% | > 10% |
| Xu ra / vào (30 ngày) | 90–105% | 80–90% | < 80% |
| Giá đá nâng cấp trên chợ ÷ chỉ số giá | 0,3–1 giờ | | > 2 giờ |
| % xu ở top 1% người chơi | ≤ 25% | | > 40% |

Khi đỏ: tăng phí dịch chuyển / sửa đồ qua `cau_hinh` (không cần build), mở hố hút có thời hạn (trang trí bằng xu), giảm
`roi.xu.phan_tram`. Vài lớp chỉ có trong jar gốc (MOBA, đấu trường) còn tự cộng tiền: chỉ bù được bằng đo chỉ số này và tắt
bản đồ / sự kiện đó nếu đỏ.

### 9.5 Giftcode, cửa hàng, nạp tiền

- **Giftcode an toàn** (`GiftcodeAnToanLogic`, bảng `tob_giftcode` / `tob_giftcode_log`): phần thưởng **chỉ** lượng khoá, đá
  (luôn khoá) và vật phẩm khoá (≤ 10 mỗi loại). Giá trị một mã (lượng khoá + số đá + 5 × số vật phẩm) ≤
  `giftcode.gia_tri_toi_da` (30); tổng giá trị phát / tháng toàn máy chủ ≤ `giftcode.ngan_sach_thang` (20.000); mỗi tài khoản 1
  lần / mã, mỗi Pokémon ≤ `giftcode.toi_da_thang` (2) mã / tháng; nhập sai 5 lần khoá 10 phút. Server kiểm lại khi phát.
- **Cửa hàng** (Sổ tay → Cửa hàng, `real.CuaHangLauDai`): luật "không vật phẩm mồ côi" — mọi dòng **chỉ ra vật phẩm, không bao
  giờ ra xu / lượng / lượng khoá**. Trang bị bằng xu (Bronzong, 1,5 giờ), thuốc bằng xu (Chansey), bùa / thuốc hiếm bằng lượng
  khoá có giới hạn tuần (Kecleon), trang trí bằng lượng (Delcatty), đổi Huy chương Gym / tín vật đột kích / vật phẩm sự kiện
  lấy đồ giữ, đá tiến hoá, Kẹo hiếm, Mảnh tinh hoa (giá trị ra ≤ giá trị tín vật).
- **Nạp tiền không bán sức mạnh** (tắt mặc định, `sv.nap_tien` + cấu hình web): bán gói lượng, Thẻ tháng, Vé mùa cao cấp,
  Thành viên, và bằng lượng trong game là bản cao cấp vật phẩm tăng tốc (chung trần với bản khoá). **Không bán** chỉ số, đồ,
  đá, tỉ lệ nâng cấp. Lượng tiêu cho tăng tốc / trang trí bị đốt. Mục tiêu mô phỏng: người nạp chênh sức mạnh cùng giờ chơi
  ≤ 5%. Trần nạp, hoàn tiền, giấy phép, vận hành cổng thanh toán: [van-hanh.md](van-hanh.md).

---

### 9.6 Chống clone chuyển xu, quà mốc cấp

Chủ game hỏi 19/10/2026: "tạo nhiều clone để giao dịch xu qua thì sao? lượng khoá thì không giao dịch được đúng không?"

**Lượng khoá không chuyển được bằng đường nào** (test `KiemChongClone`): giao dịch trực tiếp chỉ chở thuốc (không có ô
xu / lượng / lượng khoá); ký gửi, chợ trang trí, chợ sinh thái, thú cưng lấy xu đều trả bằng **xu**; chuyển lượng chỉ trừ /
cộng lượng **mở**; quỹ đội / lương đội là xu; giftcode an toàn chỉ phát (không chuyển); không có thư / quà giữa người chơi
hay thất lạc. Các đường *đổi* lượng khoá ra giá trị chuyển được đã bịt: thuốc mua bằng lượng khoá thành **thuốc khoá**
(`real.ThuocKhoa`, đếm `x<id>` trong hồ sơ dài hạn — không giao dịch được), vật phẩm mới mua bằng lượng khoá có `lock = 1`
(ký gửi từ chối), và đồ / nguyên liệu khoá **không bán cho NPC lấy xu** (`chong_clone.ban_npc_do_khoa` = 0). Không có khái
niệm "xu khoá", nên xu từ nhiệm vụ tân thủ được chặn bằng cổng cấp bên dưới.

**Cổng cho mọi đường chuyển xu / giá trị giữa hai người** (`com.htbb.pokemon.ChongClone`, nhóm cấu hình *Chống clone &
chuyển xu*): giao dịch, ký gửi (rao + mua), thú cưng lấy xu, chợ trang trí, chợ sinh thái (rao, mua, đơn đặt hàng), thách
đấu cược xu (1-1 và nhóm).

| Cổng | Khoá | Mặc định |
|---|---|---|
| Cấp tối thiểu **cả hai bên** | `chong_clone.cap_toi_thieu` | 20 |
| Tuổi tài khoản (từ `team_user.regdate`) | `chong_clone.tuoi_tai_khoan_gio` | 24 giờ |
| Giờ chơi của Pokémon (`tob_char.totalTimePlay`) | `chong_clone.gio_choi_toi_thieu` | 10 giờ |
| Mỗi người nhận / ngày: tối đa N Pokémon **cùng thiết bị hoặc cùng IP** gửi cho họ | `chong_clone.nv_cung_may_toi_da` | 2 |
| Giá ký gửi ≤ max(giá gốc × N, thu nhập 1 giờ × M) và ≥ 5% giá gốc | `chong_clone.ky_gui_gia_toi_da_lan` / `…_gio` / `…_toi_thieu_phan_tram` | 10 / 200 / 5 |

Cổng có sẵn từ trước: chưa kích hoạt tài khoản thì khoá giao dịch, chat thế giới, chợ, chuyển lượng, nhận tặng, giftcode
(`kich_hoat.chuc_nang`); Thành viên (`thanh_vien.*`, đang tắt); đăng ký tối đa 3 / giờ, 10 / ngày mỗi IP và mỗi thiết bị;
cấm theo thiết bị (`tob_cam_may`) + nhật ký thiết bị đăng nhập; góp quỹ đội cần cấp 50. Mọi lần chuyển (kể cả lần bị chặn)
ghi `tob_log_chuyen_gia_tri`; người nhận có ≥ 3 người gửi trong ngày (`chong_clone.nghi_so_nguoi_gui`), người gửi chung
thiết bị / IP, hoặc gửi từ chính thiết bị người nhận thì gắn cờ **nghi clone** — báo cáo ở `/admin/kinh-te`.

**Quà cấp 35** (`QuaMocCap`, NPC, một lần mỗi Pokémon): 5.000 xu + 6 lượng khoá + 10 nguyên liệu khoá mỗi loại cấp 6
(trước: 10.000.000 xu thời thử nghiệm ≈ 580 giờ thu nhập ở cấp 35, ≈ 1.300 lần số xu trung vị người chơi cấp 35 đang giữ
trong mô phỏng — vừa lạm phát vừa là mồi nuôi clone). 5.000 xu = vốn khởi đầu ≈ 0,3 giờ thu nhập cấp 35 (17.270 xu / giờ);
6 lượng khoá nằm giữa thành tựu cấp 30 (5) và cấp 40 (6). Các mốc cấp khác (thành tựu cấp 10..100) đã đúng thang: 0 xu,
1–15 lượng khoá.

## 10. Thuật ngữ và quy tắc chữ

Áp cho **mọi chữ người chơi và admin thấy**: chuỗi server, CSDL, client J2ME / Android / PC, web. Nguồn máy duy nhất:
`server/src/com/htbb/pokemon/ChuPokemon.java` (`TU_CAM` từ cấm, `DOI` cặp cũ → mới, `CHO_PHEP` cụm hợp lệ chứa từ cấm). Mã
nguồn đã mang thuật ngữ mới; từ điển lúc chạy vẫn giữ để đổi phần động (tên từ CSDL cũ, chữ người chơi gửi lên). Test
`KiemThuatNgu` và `tools/pokemon/quet_tu_cam.py` phải báo **0** từ cấm.

### 10.1 Bảng thuật ngữ chính (kiếm hiệp → Pokémon)

| Cũ (kiếm hiệp) | Mới (Pokémon) | Ghi chú |
|---|---|---|
| nhân vật | **Pokémon** | người chơi LÀ một Pokémon; cả trang admin ("Tên Pokémon") |
| môn phái; kiếm khách, chiến binh, pháp sư, đấu sĩ, cung thủ | dòng Pokémon; dòng Charmander, Bulbasaur, Eevee, Squirtle, Pichu | thứ tự class 0..4 |
| bang, bang hội, bang phái / bang chủ, phó bang, bang chúng | **đội** / đội trưởng, đội phó, thành viên đội | "nhóm" = tổ đội tạm thời |
| trưởng lão | cố vấn | |
| chiếm thành, công thành, chiến thành | **Chiếm Gym**, chiếm Gym, chiến Gym | |
| thành chủ, giữ / sở hữu / cổng thành | trưởng Gym, giữ / sở hữu / cổng Gym | không dùng "thủ lĩnh" |
| thành trấn danh | Cao nguyên Indigo | |
| vận tiêu, bảo tiêu, chuyến tiêu | **giao hàng**, chuyến giao hàng | nhận / trả / đổi / cướp hàng, màu hàng, kiện hàng |
| tiêu cục, xa tiêu lệnh, ngựa vận tiêu | trạm giao hàng, vé xe giao hàng, Ponyta giao hàng | |
| liên đấu / thành liên đấu N | **giải Liên minh** / sân Liên minh N | "Liên minh" viết hoa |
| lôi đài, võ đài / tỷ võ, luận kiếm | **sàn đấu** / đấu tập | |
| tống kim, chính tà, quốc chiến | chiến trường liên vùng, chiến vùng | |
| lãnh thổ, quốc gia; thanh long / hắc hổ / huyền vũ | vùng; Kanto / Johto / Hoenn | |
| đức vua, đại thần, bộ đầu, tướng quân, thống lĩnh, nguyên soái | nhà vô địch vùng, cố vấn vùng, cảnh sát vùng, chỉ huy | |
| trấn yêu, Vạn Tiên Trận; lệnh bài trấn yêu | **Mê cung**; vé Mê cung | khác Tháp Pokémon và Hầm ngục bí ẩn |
| linh thú, ngựa, chiến mã / cưỡi, xuống ngựa | **thú cưỡi** / cưỡi thú, xuống thú cưỡi | tên con theo `bang_thu.csv` |
| thần thú | Pokémon huyền thoại | |
| xe ngựa, xa phu, thần hành phù | chuyến bay Pidgeot, Pidgeot (Bay), vé bay Pidgeot | |
| khinh công | chạy nhanh | |
| phi phong | **ruy băng** (ô 19) | |
| vũ khí; Kiếm / Đao / Bút / Búa / Cung | **đồ cầm tay**; Than lửa / Hạt giống thần / Khăn lụa / Nước thần / Nam châm | |
| thần binh / thần khí | bảo vật / bảo vật huyền thoại | |
| giáp trụ, ngọc bội, dây chuyền, hộ uyển | lớp giáp, phiến đá hệ, vòng cổ, vòng chân | |
| bí kíp | đĩa chiêu | |
| tụ hồn đan, tiên đan, tiên dược, tiềm năng đan, kỹ năng đan, linh đan | Kẹo Poké, cỏ hồi sinh, kẹo kinh nghiệm, kẹo tiềm năng, kẹo kỹ năng, thuốc hồi phục | |
| huyết linh thảo, huyết bồ đề, nhân sâm | Kẹo thú cưng, Bạc hà tính cách, Berry Razz vàng | |
| lệnh bài, thẻ bài, kim bài, hộ vệ lệnh | thẻ, thẻ, huy hiệu lập đội, vé gọi Pokémon hỗ trợ | |
| hộ vệ | Pokémon hỗ trợ | |
| nội công, nội lực, chân khí, công lực | **năng lượng**, sức mạnh | |
| võ công / tâm pháp, kinh mạch | chiêu thức ("chiêu") / bí quyết, thể trạng | |
| ma pháp / vật lý, thủ ma / thủ vật | đặc biệt / vật lý, phòng thủ đặc biệt / phòng thủ | |
| ngũ hành; hệ Kim / Mộc / Thủy / Hỏa / Thổ | **hệ**; hệ Thép / Cỏ / Nước / Lửa / Đất | |
| nhất … ngũ phẩm / nội tại | hạng 1..5 / đặc tính | |
| đại hiệp, thiếu hiệp, hiệp khách / cao thủ | nhà huấn luyện, nhà huấn luyện trẻ, nhà thám hiểm / nhà vô địch | |
| sư phụ / đệ tử; sư huynh, sư đệ… | thầy / học trò; bạn học | |
| tiền bối, huynh, tiểu đệ, tại hạ, các hạ, ngươi | bậc đàn anh, anh, em, tôi, cậu, cậu | NPC xưng "ta" / "tôi", gọi "cậu" / "bạn" |
| giang hồ, võ lâm, thiên hạ / bôn tẩu | thế giới Pokémon, mọi vùng / phiêu lưu | |
| giết, đồ sát, liên trảm, cừu nhân | hạ, PK, chuỗi thắng, đối thủ | |
| yêu ma, yêu quái, cương thi, ma đầu / sơn tặc, đạo tặc | Pokémon hung dữ, Pokémon ma, kẻ xấu / kẻ trộm | |
| khách điếm, tửu lâu / ngân lượng, nguyên bảo | nhà trọ, quán ăn / xu, lượng | |
| luyện công, bãi luyện công | luyện tập, bãi luyện tập | |
| phu quân, phu nhân, phu thê | bạn đời | |
| chết (nhân vật) / về làng, hồi thành | **gục** / **về Trung tâm Pokémon** | quái thì "hạ" |
| tiên đan / cỏ hồi sinh (khi gục); suy yếu sau khi chết | Thuốc hồi sinh, Thuốc hồi sinh tối đa; **Mệt mỏi** | |

Tên NPC tĩnh và tên bản đồ nằm ở CSDL (`data_npc`, `data_map_ten` — [sql/pokemon_ten_hien.sql](../sql/pokemon_ten_hien.sql), sửa ở
/admin/ten-hien). Tên pet, thú cưỡi, thời trang, danh hiệu, bộ sưu tập kiểu cũ đổi theo `ChuPokemon.DOI` và các bảng
[bang_npc.csv](../du_lieu/bang_npc.csv), [bang_thu.csv](../du_lieu/bang_thu.csv), [bang_pet_cu.csv](../du_lieu/bang_pet_cu.csv).
Đối chiếu từng ô CSDL đã đổi: [bang_chu.csv](../du_lieu/bang_chu.csv).

**Ngoại lệ giữ nguyên**: động từ "kiếm" ("kiếm được", "tìm kiếm"; chỉ "Kiếm" viết hoa là tên vũ khí cũ); tên chiêu "Múa
kiếm"; "thành phố", "thành công", "hoàn thành", "thành viên"; "hệ kim cương"; "võ sĩ" (lớp nhà huấn luyện); lễ hội Việt Nam
(Tết, Trung thu, Vu Lan, bánh chưng, lì xì); "Luyện kim dược", "Liên kích", "quái"; chú thích, tên biến, tên tệp, khoá cấu
hình; tài liệu thiết kế trong `docs/` (được nêu từ cũ).

### 10.2 Tên định danh trong mã (cũ → mới)

Đích đổi tên cho mã nguồn (đổi theo đợt; tên class cũ còn gặp trong code cho tới khi đổi xong):

| Định danh cũ | Tên mới |
|---|---|
| `BangHoi`, `Clan` (chữ) | `Doi` (`MapDoi` giữ) |
| `VanTieu`, `Vantieu`, `MonsterVantieu` (giao hàng) | `GiaoHang`, `QuaiGiaoHang` (`TraHang` giữ) |
| `RegionVantienTran`, `MapVanTienTran`, `TranYeu`, `LenhBaiTranYeu` (sự kiện Vạn Tiên Trận — Mê cung, **không** phải giao hàng) | `VungMeCung`, `MapMeCung`, `MeCung`, `VeMeCung` (`BossMeCung` đã có) |
| `ChiemThanh`, `CongGym`, phần chiếm của `MapTown` | `ChiemGym` |
| `LienDau`, `MapLienDau`, `NpcReceiveCardLienDau` | `GiaiLienMinh`, `MapGiaiLienMinh` |
| `LoiDai`, `MapLoiDai` | `SanDau`, `MapSanDau` |
| `TongKim` | `ChienTruongLienVung` |
| `LinhThu` | `ThuCuoi` (`SkinThuCuoi`, `PokemonHuyenThoai` giữ) |
| `PhiPhong` | `RuyBang` |
| `VuKhi`, `itemVukhiThoiTrang` | `DoCamTay`, `itemDoCamTayThoiTrang` |
| `ThanBinh`, `TinhLuyenThanBinh` | `BaoVat`, `TinhLuyenBaoVat` (`BaoVatHuyenThoai`, `ThoRenBiAn` giữ) |
| `HoaKyLan`, `TuHonDan`, `HuyetLinhThao` | `Ponyta`, `KeoPoke`, `KeoThuCung` |
| `MonPhai`, `NAME_CLASS` | `DongPokemon` |
| `NoiLuc`, `NoiCong` | `NangLuong` |
| `SuPhu`, `DeTu` | `Thay`, `HocTro` |
| `LanhTho`, `QuocGia` | `Vung` |
| `XaPhu` | `PidgeotBay` |
| `NhanVat` | giữ hoặc `Pokemon` (chữ hiện ra đã là "Pokémon") |

Không đổi tên class nằm trong jar gốc (`lib/htbb-core.jar`) — không có mã nguồn.

### 10.3 Quy tắc chữ (rút gọn)

| Chuẩn | Không dùng |
|---|---|
| Pokémon, Pokédex | Pokemon, pokemon, Pokedex |
| hệ Lửa, hệ Nước… (tên hệ hoa chữ đầu); khắc hệ | hệ Hoả / Thuỷ / Mộc / Kim / Thổ, ngũ hành, khắc chế ngũ hành |
| chiêu (tên chiêu Pokémon); kỹ năng sống (câu cá, đào, nấu…) | skill, võ công, kĩ năng |
| năng lượng (chiến đấu) / thể lực (kỹ năng sống) | nội lực, stamina |
| tiến hoá, cấp, EXP | tiến hóa, level, Lv, exp |
| xu, lượng, lượng khoá, giftcode | vàng, lương, LK, gift code |
| đội (lâu dài) / nhóm (tạm thời) | bang, clan, party |
| Gym, trưởng Gym, huy hiệu, Chiếm Gym, sàn đấu | gym, thủ lĩnh, chiếm thành, lôi đài |
| Sổ tay huấn luyện, Hầm ngục bí ẩn, Hệ sinh thái, Túi sinh thái, Thang PvP, boss thế giới | viết HOA mọi chữ đầu, world boss |
| quái tinh anh; "đổi lối", "bố cục hôm nay" | Alpha, hạt giống, seed |
| nhiệm vụ chính / phụ / hằng ngày; hằng tuần; sự kiện | quest, nv, hàng ngày, event |
| thầy / trò; Thẻ tháng, Vé mùa (nhánh thưởng cao cấp / miễn phí) | sư phụ, đệ tử, track, battle pass |

- **Chính tả**: dấu thanh kiểu cũ cho "oa / oe / uy": hoá, khoá, hoà, thuỷ, tuỳ, khoẻ. Tên vật phẩm mới viết kiểu câu
  ("Kẹo hiếm"). Tên menu, nút viết kiểu câu; không viết HOA cả câu.
- **Không dùng chữ HOA có dấu** (Ấ, Ẩ, Ở…) trong chữ server / J2ME: đặt lại câu cho chữ đầu không dấu hoặc viết thường.
- **Ký tự font J2ME / Android không có** (hiện thành "?"): `" ' & ; < > = @ [ ] _ … – — → × ≥ ≤ “ ” ·`. Trong chữ game dùng
  dấu phẩy, ":", "...", "-", "x", "từ ... trở lên", "chênh không quá N cấp", "và", ngoặc tròn; không dùng "->". Web và PC vẽ
  được mọi ký tự.
- **Số**: hàng nghìn dấu chấm (25.000), phần trăm viết liền (10%), giờ 20:00, ngày dd/mm/yyyy, tiền "49.000 đ", thập phân dấu
  phẩy (0,5%).
- **Không lộ thông tin nội bộ** trong chữ người chơi: id, tên bảng (`tob_*`, `data_*`), khoá cấu hình (`sv.*`), mã lỗi kỹ
  thuật, "hạt giống". Chữ cho GM / admin thì được.
- **Độ rộng J2ME**: màn 176 px; tiêu đề / mục menu một dòng nên ≤ ~150 px (đo bằng `do_rong_j2me.py`).
- Chữ người chơi thấy **mới** phải được duyệt (CODING_CONVENTION §10). Thêm từ cũ: thêm vào `TU_CAM` + cặp `DOI`, chạy
  `tools/pokemon/ap_thuat_ngu.py --ghi`, `ap_thuat_ngu_web.py --ghi`, `sinh_sql_chu.py`, sinh lại từ điển client và dump, rồi
  chạy bộ test.

---

## 11. Phụ lục: lệnh mạng riêng của HTBB

Số lệnh âm do HTBB thêm (`Message.command`; grep cả server và client trước khi gán số mới). Lệnh cũ của game gốc giữ nguyên
định dạng.

| Số | Tên | Hướng | Nội dung / khi gửi |
|---|---|---|---|
| -90 | `POKE_THAN` | S→C | `i16 charId, i16 dex, u8 shiny` — sau mỗi lệnh 5 về một Pokémon, về chính mình sau lệnh 3, khi tiến hoá |
| -91 | `POKE_QUAI` | S→C | `u8 n, n × (u8 idTemplate, i16 dex, u8 shiny, u8 tiLe%)` — ngay sau lệnh 26 |
| -92 | `POKE_TIEN_HOA` | S→C | `i16 charId, i16 dexCu, i16 dexMoi` — trước -90 khi tiến hoá |
| -93 | `POKE_CUOI` | S→C | `i16 charId, i16 dexThu (0 = không cưỡi), u8 shiny` — sau mỗi -90 |
| -94 | `POKE_PET` | S→C | `i16 charId, i16 dexPet (0 = không có), u8 shiny` — sau mỗi -93 |
| -95..-99 | `POKE_DON`, `POKE_CANH_BAO`, `POKE_TRANG_THAI`, `POKE_BOSS`, `POKE_DONG_GOP` | S→C | chiến đấu, **đã chốt** (mục 5.8) |
| -100 | Tự động đánh | hai chiều | xin trạng thái / bật / tắt / nhịp / xin kế hoạch (4); server trả byte 0 trạng thái (giờ còn lại, bán kính, cờ, thông báo) hoặc byte 1 kế hoạch (hành động, id, số, x, y, lý do) (mục 8.2) |
| -101 | Hồi sinh | hai chiều | mở / đóng hộp chọn; client xin / chọn mã 0..6 (mục 8.1) |
| -102 | `POKE_NGOAI_HINH` | S→C | ngoại hình trang bị, sau -94 (mục 4.6) |
| -103 | Cài đặt ngoại hình | C→S | ẩn ô / trang phục (mục 4.6) |
| -110 / -111 | Danh hiệu | hai chiều | bảng danh hiệu, nhãn trên đầu, số mới / xin, đeo, tháo, đã xem (mục "Danh hiệu" dưới) |

Hiệu ứng thời tiết dùng lại gói cũ `-39` (mục 7.1).

## Danh hiệu (0.9.7)

- **Dữ liệu:** `sql/them_danh_hieu.sql` (idempotent). `data_danh_hieu` là danh mục duy nhất: tên, mô tả điều kiện, gợi ý
  (danh hiệu ẩn), độ hiếm (`data_danh_hieu_do_hiem`: Thường / Hiếm / Sử thi / Huyền thoại), hình, **một** dòng chỉ số
  nhỏ (`chi_so` = `ne_tranh` / `chi_mang` / `khang_trang_thai` / `hp`, `phan_nghin`), điều kiện (`dieu_kien`,
  `tham_so`, `so_luong`), `an` (0 hiện, 1 ẩn "???" tới khi mở, 2 chỉ hiện khi đang có), `thoi_han_phut` (0 vĩnh viễn).
  391 dòng: 43 danh hiệu thành tựu / xếp hạng mùa / sự kiện / ẩn (id 9700..), 16 danh hiệu sinh thái (9600..), 48 danh
  hiệu cũ giftcode / sự kiện (8767..), 284 danh hiệu thưởng nhiệm vụ (10000.., trước đây chỉ là chữ trong tiến độ).
- **Sở hữu:** kho cũ `tob_danh_hieu` (id, hết hạn) + đang đeo `Char.idEffDanhHieu`; bộ đếm `tob_danh_hieu_dem`, nhật ký
  `tob_danh_hieu_log`.
- **Mở khoá** phía máy chủ: mọi điểm móc `NhiemVuEngine.suKien` / `datMuc` (hạ trùm, câu cá, thắng PvP, nâng cấp +N,
  Hầm ngục, chế tạo, khảm, gục, đến bản đồ...) cộng bộ đếm; trạng thái sống (cấp, Pokédex %, huy hiệu Gym, bậc / hạng
  PvP mùa, sự kiện đang bật, danh hiệu sinh thái / nhiệm vụ) đánh giá mỗi phút, khi lên cấp và khi mở bảng. Danh hiệu có
  hạn (bậc mùa, top 10, sự kiện) tự gia hạn khi còn giữ điều kiện, hết hạn thì mất chỉ số và có thể mở lại.
- **Chỉ số:** chỉ danh hiệu **đang đeo** được tính, kẹp về trần ô `danh_hieu.tran_phan_nghin` (10‰ = 1%, trần cứng 3%)
  và trần tổng trang trí `trang_tri.tran_tong_phan_nghin` (5%), rồi đi qua `GioiHanChiSo` như mọi nguồn. Danh hiệu trao
  qua giftcode / gói nạp chỉ có mức Thường (5‰), không vượt danh hiệu tự cày.
- **Giao diện:** Hồ sơ (bảng Pokémon) → viên "Danh hiệu" cạnh huy hiệu hệ (lối vào duy nhất; menu Sưu tầm sinh thái
  → Danh hiệu mở cùng bảng) → tấm Danh hiệu: Đã có / Chưa có / Bí ẩn, thanh tiến độ, đếm ngược hạn, nút Đeo / Tháo,
  chấm đỏ `ChamMoi.DANH_HIEU` khi có danh hiệu mới. Nhãn danh hiệu vẽ trên tên mọi người chơi, màu theo độ hiếm
  (token `ChuDe.mauDanhHieu` / `nhanDanhHieu`). Web: bảng xếp hạng hiện danh hiệu đang đeo.

