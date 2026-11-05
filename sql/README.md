# sql — một tệp CSDL `htbb.sql`

Dữ liệu game nằm trong **CSDL** và được sửa trên trang quản trị. Thư mục này giữ **một** tệp, `htbb.sql`, dùng cho cả hai
trường hợp và chạy lại được bao nhiêu lần cũng được:

| Trường hợp | Lệnh | Kết quả |
|---|---|---|
| **CSDL mới** (MariaDB trống) | `mysql -u root < sql/htbb.sql` | `account` + `htbb_db`: mọi bảng, mọi dữ liệu thiết kế / cấu hình (vật phẩm, quái, nhiệm vụ, sự kiện, danh mục cấu hình kèm phân loại `lop`…), một tài khoản `admin`; bảng người chơi rỗng. Không cần tệp nào khác. |
| **CSDL đang chạy** (production) | `sudo bash /opt/htbb/vps/nang_cap_csdl.sh` | sao lưu → nạp `sql/htbb.sql` → kiểm. Thêm bảng / cột / chỉ mục / dòng mặc định, nạp lại bảng thiết kế máy chủ đọc lúc khởi động; **không** đụng dữ liệu người chơi và giá trị admin đã sửa. |

Không còn bộ sinh trong luồng làm việc: các bảng tính CSV, bộ sinh Python / Java và các tệp SQL lẻ trước đây đã dùng **một
lần cuối** để dựng bản đầy đủ, rồi chuyển vào [`tools/luu_tru_sinh_du_lieu/`](../tools/luu_tru_sinh_du_lieu/README.md)
(chỉ để tra lịch sử). Hai tệp cũ `htbb_day_du.sql` (bản đầy đủ) và `nang_cap.sql` (nâng cấp) đã gộp thành `htbb.sql`
(phần A = bản đầy đủ cũ, phần B = `nang_cap.sql` cũ). Tệp thao tác tay xoá dữ liệu người chơi ở `vps/van_hanh/`
(`wipe_*.sql`), không ở đây.

## Cấu trúc `htbb.sql`

**Phần A — CSDL đầy đủ** (dòng `-- ######## A. CSDL DAY DU`; sinh bằng `vps/xuat_csdl_day_du.sh`, không sửa tay):

1. Schema **mọi** bảng của `account` + `htbb_db`, `CREATE TABLE IF NOT EXISTS` (không `DROP`): bảng đã có giữ nguyên, cả
   dữ liệu; bảng thiếu được tạo đúng DDL hiện tại.
2. Dữ liệu 116 bảng thiết kế / cấu hình (mọi bảng `data_*` + danh sách `BANG_DU_LIEU` của `vps/xuat_csdl_day_du.sh`):
   nạp vào **bảng tạm** (`CREATE TEMPORARY TABLE` trong `account`, mất khi hết phiên), rồi chỉ **chép** sang `htbb_db` khi
   CSDL **mới** — `@htbb_csdl_moi` = `htbb_db.data_item` rỗng lúc bắt đầu — và bảng đích còn rỗng (`data_item` chép cuối:
   nạp đổ giữa chừng thì chạy lại vẫn là CSDL mới). Trên CSDL đang chạy không có dòng nào bị chép, sửa hay xoá ở phần này.
3. Tài khoản `admin` (chỉ CSDL mới, chưa có `admin`): mật khẩu là chuỗi ngẫu nhiên không ai dùng — **đổi ngay** (dưới).

**Phần B — nâng cấp CSDL đang chạy** (dòng `-- ######## B. NANG CAP`, từng là `nang_cap.sql`; viết tay + khối sinh
`-- >>> <khối>.sql`). Trên CSDL mới mọi lệnh ở đây không đổi gì (trừ bài tin 0.9.8 `INSERT IGNORE` vào `web_tin_tuc`):

1. cột / bảng mới qua các bản (`ADD COLUMN IF NOT EXISTS`, nới cột `itemquest`, 2FA, cấm máy, nhật ký, Pokédex, dài hạn,
   client đăng nhập, chống clone, CMS web…);
2. `DROP` + `CREATE` + `INSERT` lại 8 bảng thiết kế máy chủ nạp lúc khởi động (`data_cap_do_lau_dai`,
   `data_nang_cap_lau_dai`, `data_kinh_te_lau_dai`, `data_su_kien_nam`, `data_hst_ho_cau`, `data_noi_dung_trang_bi`,
   `cau_hinh_danh_muc`, `cau_hinh_nhom`) — từng dòng giống phần A (`server/test/KiemDuLieuSql` kiểm);
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
10. 0.9.7 (main 1a705be6): sửa logic 0.9.6.1 (sàn hồi chiêu / PP / MP trong `data_noi_dung_chieu`, Đá phá khoá 1295,
    `data_pha_khoa_*`), PvP xếp hạng (`tob_pvp_tran`, `tob_pvp_thuong`), cập nhật client (trường `ban` của
    `web_cai_dat.taiGame`), danh hiệu (`data_danh_hieu*`, `tob_danh_hieu_*`), điểm móc nhiệm vụ
    (`data_su_kien_vat_pham`, `tob_nhiem_vu_cho`).
11. 0.9.8 (`BAN=908`; mỗi tệp lẻ cũ một khối `-- >>> <tên tệp>`, danh mục cấu hình của chúng ở mục 2):
    - `hoc_chieu_huan_luyen.sql`: học chiêu theo loài `data_learnset` (175 dòng, 21 loài), Điểm huấn luyện
      `data_huan_luyen`, `data_tang_truong_dong`, `tob_huan_luyen`, dấu chuyển đổi một lần `tob_chuyen_doi_can_bang`
      (nhân vật cũ chuyển đổi trong game lúc nạp, không sửa dữ liệu người chơi ở đây);
    - `them_bo_ngoc_che_tao_bat.sql`: bộ trang bị / ngọc hệ / chế tạo / mốc neo nâng cấp (`data_ngoc_he_*`,
      `data_che_tao_bac`, `data_nang_cap_moc`, cột `loi_che_tao` / `bat` của `data_noi_dung_che_tao`), thu phục
      (`data_bat_bong`, `data_bat_loai`, `tob_hop_pc`, `tob_ho_so_bo_ngoc`, cột `so_bat` / `bat_shiny` của `tob_pokedex`);
    - `them_pho_ban_cup.sql`: Phó bản Hầm ngục (`data_pho_ban_ngay`, `data_pho_ban_tang`, dòng rơi `pho_ban` 9001…9024 của
      `data_noi_dung_roi`, `tob_pho_ban_*`), Cúp PvP tuần (`data_cup_loai`, `data_cup_lich`, `tob_cup_*`), tên bản đồ phòng
      riêng 4381 / 4382 (`data_map_ten`, giữ tên admin đã sửa), danh hiệu 9850…9855 (cúp) + 9860…9862 (phó bản); mục 4
      thêm `moi:cuptuan` vào danh sách xoá mốc hẹn cũ của lịch năm;
    - `them_do_hiem.sql`: **khối sinh** từ `tools/cauhinh/do_hiem.json` (`python3 tools/cauhinh/sinh_do_hiem.py`, `--kiem`
      báo lệch; chỉ thay khối này trong `htbb.sql`): `data_do_hiem` (6 bậc), `data_do_hiem_vat_pham`,
      `data_bac_trang_bi`, màu `data_danh_hieu_do_hiem` (khối `them_danh_hieu.sql` ở mục 10 dùng cùng màu);
    - `them_thoi_tiet_mua.sql`: thời tiết theo khung giờ + phủ màu mùa (`data_thoi_tiet`, `data_thoi_tiet_map`,
      `data_thoi_tiet_su_kien`), ngày cộng đồng hằng tháng (`data_ngay_cong_dong`), danh hiệu dòng 9801…9817.
    - `them_trade_pokemon.sql` (bat-trade, sau khối bộ / ngọc / thu phục): bậc hiếm loài, `data_bat_chi_so_loai`,
      `data_bat_chuyen_doi`, cột `data_bat_loai.tien_hoa_trao_doi`, nhật ký `tob_log_trao_doi_pokemon`;
    - `them_kenh_chat.sql` (kenh-chat): hộp thư tin riêng offline `tob_chat_cho`, danh sách chặn `tob_chat_chan`;
    - `them_ngam_tay_pvp.sql` (pvp-keo): `data_chieu_ban` (kiểu bắn từng chiêu cho ngắm tay PvP, `INSERT IGNORE`);
    - `sua_ket_doi.sql` (ket-doi): Lễ kết đôi thay đám cưới - `data_nhom_trung_ten`, `data_nhom_trung` (nhóm trứng 21 loài),
      đổi tên vật phẩm 88–92, 98, 751 và bản đồ 225 / 226 (chỉ ô còn tên cũ);
    - `them_huong_dan_tan_thu.sql` (main 90658456 + bản sửa của main 4f89b3ad, đã chạy trên production): hướng dẫn tân thủ
      10 bước (`data_quest_moi` 901..910 viết lại tại chỗ), `data_huong_dan_tan_thu`, Chansey / Snorlax ở 3 làng, sửa chuỗi
      chính, `infogift` varchar(1000); chạy lại vô hại;
    - `sua_tuyen_nhiem_vu.sql` (main 4f89b3ad sua-tuyen, đã chạy trên production, SINH bởi `tools/pokemon/sinh_sua_tuyen.py`):
      `tob_lang_khoi_dau` (làng khởi đầu theo vùng), `data_nhiem_vu_khu_vuc` (vùng đích bước địa điểm), chữ phần thưởng
      `infogift`; chạy sau khối hướng dẫn tân thủ;
    - `mo_duong_dia_diem.sql` (ban-098, chủ game duyệt; chạy sau `sua_tuyen_nhiem_vu.sql`): Arcanine gác cổng dẫn tới khu xa
      (100 / 118 / 41 / 42, khoá 41 / 42 cấp 80), bật lại các bước địa điểm ở đó, đổi bản đồ các bước ở bản đồ khoá cao hơn cấp
      bước / Hang Darkrai theo mùa, chữ Gym "(đấu ở …)" + NPC xa "NPC:|Torkoal (Cerulean)|" của chuỗi chính (`KiemDiaDiemNpc`);
    - `them_the_dat_ten_pet.sql` (ten-pet): thẻ đặt tên Pokémon 1261 (`data_dong_the_ten`, `data_tu_cam_ten`, `tob_pet_dat_ten`,
      `tob_log_dat_ten`, công dụng / 2 nguồn kiếm được / món cửa hàng lượng);
    - `pokemon_npc_sprite.sql`, `pokemon_thu_pokemon.sql` (p2-tainguyen): ảnh NPC = Pokémon (`data_npc.dex` / `sprite`,
      `data_npc_su_kien_sprite`), ánh xạ thú cưỡi / pet / skin → Pokémon (`data_thu_pokemon`);
    - `them_loai_day_du.sql` (loai-day-du, tệp SINH bởi `tools/pokemon/loai/sinh_loai.py`: thay cả khối này khi sinh lại):
      1.025 loài `data_loai*`, `data_chieu`, nơi sống, sprite; thêm dòng `data_nhom_trung` / `data_bat_loai` (INSERT IGNORE, giữ
      dòng của khối kết đôi / thu phục);
    - `them_do_trang_phuc.sql` (do-trang-phuc, SINH bởi `tools/do_trang_phuc/sinh.py`: thay cả khối khi sinh lại): đồ họ mới
      `data_item` 1400..2027 (icon 15000..15688, `server/map/icon_do_moi/`), `data_trang_bi_ho*`, `data_trang_bi_moi`, tủ đồ
      `data_trang_phuc_o` / `data_trang_phuc` (69 món: 61 + 8 món cửa hàng lượng cũ cùng mã), `tob_trang_phuc`, 15 món `nap_cua_hang` (INSERT IGNORE);
    - `them_ve_mo_rong.sql` (giao-dien-nro): cột `tob_char.o_mo_rong` (ô hành trang lẻ, `ADD COLUMN IF NOT EXISTS`) + món
      `ve_mo_rong_tui` của `nap_cua_hang`.
    - `dong_bo_098.sql` (lượt đồng bộ trước open test, chạy CUỐI): một hệ trang trí (món Skin / Hào quang / Skin thú cũ của
      cửa hàng lượng = món tủ đồ cùng mã; tên chỉ đổi ở dòng còn tên mặc định cũ), chuỗi phụ "Nói chuyện với X ở Y" theo nơi
      NPC đứng thật (173 dòng → "ở làng" / nơi xa), chữ "X ở A ở B", bước chuỗi chính ghi bản đồ khoá cao hơn cấp bước, chữ
      Sổ tay (`data_nhiem_vu_noi_dung.muc_tieu_text`) = chữ bước thật của chuỗi chính (gồm Gym "(đấu ở X)").
12. `sua_buoc_hanh_dong.sql` (25/10/2026, bot chơi thật cấp 1-40 báo): 140 bước nói chuyện (-64 type 3) hứa hành động mà
    nghe NPC là xong. 47 bước có hệ thống thật và làm được ở cấp bước (mặc đồ, nâng +N ở chỗ Machamp, hạ 1 con loài X
    theo khung giờ, hạ N con trong X phút) được đánh dấu `kiem_hanh_dong=1` trong `data_nhiem_vu_noi_dung.dieu_kien`
    (`real.HanhDongNhiemVuGame` kiểm thật, chữ ghi đúng bản đồ quái sinh); 93 bước còn lại chỉ viết lại chữ thành
    "Nói chuyện với X ở Y" + lời thoại kể việc diễn ra trong hội thoại. Thưởng giữ nguyên. `server/test/KiemBuocHanhDong`.
13. `sua_tim_nhiem_vu.sql` (29/10/2026, người chơi báo): chữ chuỗi chính C01.5 / C02.4 ghi đúng hướng (đồng cỏ Pallet là
    cổng phía Đông làng, chỗ cỏ cháy góc trên bên phải; trước ghi "phía bắc"); C05.2 "Bảo tàng thiên thạch" tính cả ngôi nhà
    có cửa thật (102 / 72 / 82, Kecleon đứng trước cửa) thay vì chỉ nhà 100 không có cửa (chỉ Arcanine dẫn), chữ nói rõ
    đường đi; 3 mục Hướng dẫn tân thủ (`npc.18_diem_nhiem_vu`, `npc.19_trung`, `npc.20_vuon`) đúng nhãn menu Hệ sinh thái;
    Vé tàu (`data_potion` 20) 15.000 → 5.000 xu (`UPDATE … AND price = 15000`). Điểm đích bước địa điểm (dấu sao vàng,
    nút "Đi tới dấu sao") do máy chủ gửi ở đuôi thứ hai gói -64. `server/test/KiemTimDiemNhiemVu`.
14. `bat_lai_vuon.sql` (29/10/2026, chủ game "Bật lại vườn"): vườn berry gieo / thu hoạch thật (`com.htbb.pokemon.VuonBerry`,
    ô đất trong `tob_hst_nhan_vat.du_lieu` - không bảng mới, `tob_farm` cũ không dùng). Chữ mục tiêu + gợi ý của 26 bước
    `lam_vuon` ("Trồng và thu hoạch …": N-x.12, TV-05.2, TV-08.1, AN-14, HT-CEL.3, HT-SUI.4, C54.4…C92.4, P-xx) ghi đúng
    loại berry + đường menu (`UPDATE … AND muc_tieu_text = <cũ>`), gợi ý C20.2 / C34.5 (chuỗi cũ "rơi từ") bỏ chữ vườn, cơ chế
    `data_nhiem_vu_kieu.lam_vuon`. Mục Hướng dẫn tân thủ `npc.20_vuon` (khối 13) nay chỉ cách trồng + thuốc từ vườn (hạt 50% giá
    tham chiếu, công thức thuốc chỉ cần berry ở `KyNangSong.CONG_THUC` 30–34 - mã, không phải SQL). `server/test/KiemVuonBerry`.
15. Cân bằng cấp 15-45 (26/10/2026, bot chơi thật cấp 1-40: từ cấp 20 gục liên tục, chủ game quyết "Tự cân chỉnh"; kiểm
    `server/test/KiemCanBangCap`, `KiemCanBangCapQuai`). Ba khối, mỗi lệnh `UPDATE` theo khoá + giá trị cũ:
    - `sua_chenh_cap_nhiem_vu.sql`: chữ nhiệm vụ "Hạ N Pokémon hoang dã chênh không quá 5 cấp" thêm "(hoặc ở bãi luyện cao
      nhất bạn đã mở)" và bỏ " ở <bản đồ>" (`data_quest_moi`, `data_nhiem_vu_noi_dung`). Luật đếm mới ở máy chủ
      (`ChenhCapNhiemVu`): quái chênh ≤ N cấp **hoặc** hạ ở bãi luyện cao nhất người chơi đang vào được;
    - `sua_gia_do_cua_hang.sql`: đồ cửa hàng NPC (`data_item` id 3..213) chỉ giảm giá: vũ khí ≤ 2 giờ thu nhập, giáp / phụ
      kiện ≤ 1,5 giờ thu nhập ở cấp món (vũ khí cấp 16 / 21 / 26 / 31: 30k / 45k / 60k / 80k → 14,9k / 18,6k / 23,5k / 28,8k);
    - `sua_can_bang_cap.sql`: `data_monster_lau_dai` quái thường cấp 13-59: thủ giảm dần tới 25% và tấn công tới 15% (cấp
      22-45) rồi trả dần về 0 ở cấp 60, HP / tấn công cấp 31-39 nội suy giữa cấp 30 và 40 (bỏ bậc thang cấp 31: HP ×1,29
      trong một cấp).
16. `sua_nha_tre_pet.sql` (30/10/2026, người chơi J2ME bấm "Trả phí hôm nay" chỉ thấy "Chưa gửi pet nào."): mục Hướng dẫn
    tân thủ `npc.19_trung` nói rõ nhà trẻ chỉ nhận **pet đồng hành** (không phải Pokémon chiến đấu) và cách có pet (trứng pet
    rơi khi có Poké Ball, trứng Chợ sinh thái); `UPDATE … AND sua_boi = '' AND noi_dung = <cũ>`. Chữ menu / thông báo Nhà
    trẻ ở mã (`HeSinhThaiMenu.CHUA_CO_PET`). Cùng đợt (mã, không SQL): bước địa điểm trong nhà (C05.2 "Bảo tàng thiên thạch")
    xong ngay lúc vào nhà - client không gửi gói di chuyển trong nhà. `server/test/KiemVaoNhaNhiemVu`.
17. `them_chi_so_vps.sql` (31/10/2026, trang `/admin/he-thong` thẻ VPS): bảng `tob_chi_so_vps` — web lấy mẫu CPU / RAM / swap /
    đĩa / mạng toàn máy mỗi 2 giây, ghi một dòng mỗi phút mỗi máy (`may` = hostname), giữ 7 ngày. Web tự tạo bảng nếu thiếu
    (`SQL_TAO_BANG_VPS` ở `web/server/utils/vpsThuan.ts`, `web/tests/chi-so-vps.test.ts` so khớp với phần A + B). Phần A cũng có.
18. `them_doi_luong.sql` (31/10/2026, chủ game, `docs/thiet-ke-game.md` 9.1): bảng đổi lượng → lượng khoá / xu và sàn
    lượng ↔ xu (`tob_doi_tien_ngay`, `tob_doi_luong_log`, `tob_ti_gia_luong_xu`, `tob_san_luong_*`); máy chủ tự tạo nếu thiếu
    (`DoiLuongDb.DDL`). Khoá danh mục `doi_luong.*` / `doi_luong_xu.*` / `san_luong.*` ở mục 2. `server/test/KiemDoiLuong`.
19. `them_nang_luong_thanh_vien.sql` (31/10/2026, chủ game): Bùa thể lực không giới hạn (cửa hàng lượng 30 / Kecleon 25
    lượng khoá), mô tả gói nạp +30 phút năng lượng (chỉ đổi dòng còn mô tả mặc định cũ). Khoá `exp.nang_luong_*_phut` /
    `tang_toc.bua_*` ở mục 2. `server/test/KiemNangLuongThanhVien`.
20. `sua_tien_te_cua_hang.sql` (31/10/2026, kiểm loại tiền mọi vật phẩm đang bán): `data_shop_gia_lau_dai` dòng 116..125
    (nguyên liệu cấp 4-5 khoá) ghi đúng `tien_moi = 2` (lượng khoá) như máy chủ đang trừ; giá không đổi. Phần A cũng có.
    `server/test/KiemLoaiTienCuaHang`.
21. `sua_gia_thuoc.sql` (01/11/2026): giá cửa sổ thuốc Chansey / Kecleon (`data_potion` 1–6) 100 / 500 / 800 xu (80 / 300 /
    1.000 HP) và 50 / 150 / 500 xu (Ether 160 / 600 / 2.000 PP) → 2 / 4 / 7 xu, nối chuỗi với thuốc cấp cao bằng xu
    (`com.htbb.pokemon.ThuocCapCao`, thiết kế 9.3). `UPDATE … AND price = <cũ>` (giá admin đã sửa giữ nguyên). Phần A cũng có.
22. `them_pk3v3.sql` (05/11/2026, chủ game, `docs/thiet-ke-game.md` 5.3): PK 3 vs 3 buổi tối — bảng `tob_pk3v3_luot` /
    `_diem` / `_gap` / `_tran` / `_chot` / `_thuong` (máy chủ cũng tự tạo, `real.Pk3v3Game.SQL_TAO_BANG`) + danh hiệu tuần
    9880 / 9881 (`data_danh_hieu`, phần A cũng có). Khoá `pk3v3.*` ở mục 2 (nhóm `su_kien`). `server/test/KiemPk3v3`.
23. `them_chuyen_luyen.sql` (05/11/2026, chủ game, `docs/thiet-ke-game.md` "Chuyển luyện"): Bùa chuyển luyện (`data_item` /
    `data_vat_pham_lau_dai` 1262), Kecleon dòng 131 (20 lượng khoá), cửa hàng lượng `nap_cua_hang` `bua_chuyen_luyen` (20
    lượng, bùa không khoá) — `INSERT IGNORE`, phần A cũng có. Khoá `chuyen_luyen.*` ở mục 2 (nhóm `kinh_te`).
    `server/test/KiemChuyenLuyen`.

Bảng người chơi chỉ được **thêm** bảng / cột / chỉ mục / dòng (`INSERT IGNORE`), không bị xoá hay sửa dòng; giá trị admin
đã sửa (`cau_hinh`, `web_cai_dat`, `web_tin_tuc`, `giftcode`, `nap_*`, tên NPC / bản đồ…) giữ nguyên. Không nằm trong
tệp (quyết định của chủ game, production đã chạy): xoá quà tạo nhân vật chưa nhận (`board_created`) và khoá giftcode kiểu
cũ — bản gốc ở `tools/luu_tru_sinh_du_lieu/sql_nguon/` (`sua_he_so_nap_va_qua_tao_nv.sql`, `vo_hieu_giftcode_cu.sql`),
thu hồi quà đã nhận ở `vps/van_hanh/thu_hoi_qua_tao_nv.sql`. Phải chạy **trước** khi jar mới khởi động, không thì máy chủ có
thể dừng với dòng `THIEU DU LIEU SQL QUAN TRONG - DUNG SERVER`.

## Máy mới

Cài cấu hình MariaDB **trước** (`vps/htbb-mariadb.cnf`: utf8, `lower_case_table_names=1`; xem
[docs/trien-khai.md](../docs/trien-khai.md)), rồi:

```sh
mysql -u root < sql/htbb.sql      # tạo account + htbb_db, xong
mysql -u root -e "UPDATE account.team_user SET password = PASSWORD('mật-khẩu-mạnh') WHERE username = 'admin';"
```

Tài khoản `admin` trong tệp có mật khẩu ngẫu nhiên không ai dùng: đổi ngay như trên. Server tự tạo thêm bảng theo kỳ và
bảng phụ (`CREATE TABLE IF NOT EXISTS`) lúc chạy lần đầu.

## CSDL đang chạy (production): cùng tệp, một lệnh

```sh
sudo bash /opt/htbb/vps/nang_cap_csdl.sh
```

Script kiểm máy là MariaDB và có `htbb_db.tob_char`, sao lưu (`vps/backup_db.sh`, hỏng là dừng) → nạp `sql/htbb.sql`
(tệp tự `USE account` / `htbb_db`) → kiểm 10 bảng máy chủ đọc lúc khởi động, số khoá (`INSERT INTO cau_hinh_danh_muc` của
phần B) và cột phân loại `lop` của danh mục cấu hình. Tài khoản đăng nhập: root qua unix_socket, không thì
`HTBB_DB_USER` / `HTBB_DB_PASS` của `/etc/htbb/htbb.env` (cần `ALL` trên `htbb_db.*` + `account.*` như
docs/trien-khai.md — bảng tạm của phần A nằm trong `account`).

Kiểm đã làm (23/10/2026, MariaDB 10.4 cục bộ): (1) CSDL trống + `htbb.sql` = nạp `htbb_day_du.sql` cũ (282 bảng cùng
schema, cùng số dòng + CHECKSUM; khác duy nhất `regdate` của `admin` và bài tin 0.9.8); (2) `htbb_day_du.sql` cũ + người
chơi / sửa admin giả + `htbb.sql` hai lần = luồng cũ (`nang_cap.sql` hai lần), dòng người chơi và giá trị admin giữ nguyên,
lần 2 và 3 không đổi gì; (3) xuất lại từ CSDL (1) = `htbb.sql` từng byte.

## Xuất lại từ CSDL (đưa chỉnh sửa trên trang quản trị về repo)

```sh
sudo bash /opt/htbb/vps/xuat_csdl_day_du.sh /tmp/htbb.sql    # trên VPS (root, unix_socket)
# rồi chép về repo: sql/htbb.sql, xem git diff, commit
```

Script sinh lại **phần A** bằng `mysqldump` hai lượt: (1) `--no-data --databases account htbb_db` — schema mọi bảng, đổi
thành `CREATE TABLE IF NOT EXISTS`; (2) `--order-by-primary` — DDL (thành `CREATE TEMPORARY TABLE`) + dữ liệu **chỉ** của
bảng thiết kế / cấu hình: mọi bảng `data_*` cùng danh sách `BANG_DU_LIEU` ở đầu script (`5h_systems`, `bad_word`,
`cau_hinh`, `cau_hinh_danh_muc`, `cau_hinh_nhom`, `giftcode`, `nap_cau_hinh`, `nap_cua_hang`, `nap_goi`, `tob_event`,
`tob_fruit`, `tob_tubinh`…), rồi lệnh chép có điều kiện và tài khoản `admin` (giữ chuỗi mật khẩu ngẫu nhiên của tệp cũ).
**Phần B chép nguyên văn** từ `sql/htbb.sql` của repo (biến `HTBB_SQL_NGUON`). Bảng người chơi / nhật ký / đơn nạp chỉ có
schema. Xuất → nạp vào CSDL trống → xuất lại ra đúng cùng tệp.

Thêm bảng / cột mới: tạo trong CSDL (máy chủ tự tạo hoặc qua trang quản trị) rồi xuất lại phần A (bảng mới có `CREATE TABLE
IF NOT EXISTS`); cột mới của bảng **đã có** và dòng mặc định thêm lệnh chạy lại được vào phần B (`ADD COLUMN IF NOT EXISTS`
/ `INSERT IGNORE`); bảng thiết kế mới cần dữ liệu thì thêm tên vào `BANG_DU_LIEU` của `vps/xuat_csdl_day_du.sh`.
Khối sinh của phần B (`-- >>> them_loai_day_du.sql`, `them_do_trang_phuc.sql`, `them_do_hiem.sql`…) do bộ sinh trong
`tools/` ghi thẳng vào `htbb.sql` (thay cả khối). `server/test/KiemDuLieuSql` kiểm phần A không phá gì (chỉ `IF NOT EXISTS`,
dữ liệu chỉ qua bảng tạm) và phần B giống phần A từng dòng cho bảng máy chủ nạp lúc khởi động.

## Lưu ý dữ liệu

- **`data_attribute`** phải có id 0..186 liên tục (client tra theo vị trí; thiếu là đơ khi mở trang bị).
- **`data_item`**: không bao giờ `DELETE` dòng ở giữa (client ghi mảng theo id). Muốn bỏ vật phẩm thì đổi thành loại
  trơ, giữ dòng. Xoá template trong khi còn người giữ món đó là người đó không vào được game.
- **`data_potion`** luôn đúng `Char.SO_O_THUOC` dòng (163 từ 24/10/2026, id 162 = Vé dịch chuyển, khối `them_ve_dich_chuyen.sql`): thuốc mới ưu tiên `UPDATE` vào ô trống; thêm id mới thì tăng `Char.SO_O_THUOC` cùng lúc (tối đa 255 — client đọc số ô bằng một byte) và chạy SQL trước khi thay jar.
- Cột `xstart` của `data_item` là **icon trong hành trang** (icon Pokémon dải 14000+), độc lập với hình khi mặc.
- Tệp ở đây là **LF** (`.gitattributes`).
