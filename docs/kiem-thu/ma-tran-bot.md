# Ma trận phủ tính năng của bot hồi quy

Tài liệu này trả lời câu hỏi: **bot hồi quy có kiểm TẤT CẢ tính năng người chơi chạm tới được không, kể cả thao tác bất thường?**

Lập ngày 25/10/2026. Nguồn dùng để lập:

- Phía máy chủ: bảng điều phối gói tin `server/src/real/MapXuLyLenh.java:461-930` và `server/src/real/RealController.java:975+`. Trong đó có danh sách lệnh chạy trên luồng phiên `privateCmd` (:76), bảng `commandHandler` (:939-953) và các switch riêng cho -45, 85, 22, -30, -31. Các bản ghi đè trong `OfflineMap.java` (cửa hàng trong nhà) và `NpcTinhBanDo.banDoDichVu` (NPC dịch vụ đặt ngoài trời) cũng được tính.
- Phía client: `client_jar/src/game/pokemon/ManMenu.java:24` (13 nút Menu), `client_jar/src/game/render/screen/MenuNhanh.java:42`, Sổ tay `server/src/real/SoTayLauDai.java:64` và `com/htbb/pokemon/DuongMenu.java`.
- Phía bot: `tools/pokemon/kiem_tai/hoi_quy.conf` (22 nhóm của cổng kiểm lúc lập bảng; nay 23 nhóm, thêm `bat_thuong`), `BotChucNang` cùng các `KichBan*`, `HoiQuy`, `BatBien` và `BotHonLoan`.

## Cách đọc

**Cột "Gói/cmd"** ghi mã lệnh client → máy chủ và nơi xử lý (`tệp:dòng`, đường dẫn tính từ `server/src/`).

- `-30 (npc,menu,opt)` là chọn mục menu NPC: `short idNpc, ubyte idMenu, byte idOption`.
- `-31` là trả lời ô nhập chữ: `short idActor, byte idMenuInput, UTF`.
- `-32` là trả lời hộp thoại.

**Cột "Nhóm bot / case"** ghi tên nhóm trong `hoi_quy.conf`, rồi tên case, rồi `tệp:dòng` (tệp nằm trong `tools/pokemon/kiem_tai/`).

- Tên nhóm có dấu `?` (`?cap1_40`) chỉ chạy ban đêm, **không** nằm trong cổng kiểm.
- "ngoài cổng" nghĩa là có lớp kịch bản nhưng `hoi_quy.conf` không gọi tới (ví dụ `KichBanNPC`, `KichBanHoiSinh`, `KichBanNhiemVu`, `KichBanDatTenPet`).

**Cột "Mức"** đánh giá theo những gì cổng kiểm thật sự chạy:

- **Kiểm**: có lời gọi `ket(...)` PASS/FAIL dựa trên phản hồi máy chủ hoặc kiểm CSDL. Chữ "(GM)" nghĩa là nhân vật làm thao tác là tài khoản GM, nằm trong khoảng `<tt>001..040`. GM được miễn khoá bản đồ và một số luật, nên lỗi mà người chơi thường gặp có thể bị che mất (xem ghi chú đầu `hoi_quy.conf`).
- **Chỉ chạy qua**: bot có gửi lệnh nhưng không kiểm kết quả. Trường hợp `ket` luôn trả true, hoặc chỉ kiểm "còn kết nối", cũng xếp vào mức này.
- **Thiếu**: cổng kiểm không gửi lệnh này. Chữ "Thiếu*" nghĩa là có kịch bản ngoài cổng hoặc chỉ chạy ban đêm.

**Cột "Thao tác bất thường"** ghi biến thể nào đã có kịch bản. Các biến thể xét tới là: bấm đúp hoặc spam, túi đầy, rớt mạng giữa chừng, id sai hoặc số lượng âm, đang chết hoặc đang đánh, số dư vừa đúng. Dòng có dấu **★** là việc giao cho nhóm mới `bat_thuong` (xem mục cuối).

Hầu hết số liệu `ket(...)` được trích bằng `grep -n`. Riêng các ghi chú rủi ro máy chủ thì rút từ việc đọc mã, chưa chạy thử. Những ghi chú này đánh dấu là CÓ THỂ.

## Tổng hợp

| Chỉ số | Số tính năng |
|---|---|
| **Tổng tính năng liệt kê** | **173** |
| Kiểm (assert) | 103 (trước `bat_thuong`: 101; khá nhiều dòng chỉ kiểm bằng GM, ghi "Kiểm (GM)") |
| Chỉ chạy qua | 13 (trước: 12) |
| Thiếu (cả Thiếu*) | 57 (trước: 60) |
|   trong đó Thiếu*: có kịch bản ngoài cổng hoặc chỉ chạy đêm | 9 |
| Dòng có ít nhất một biến thể bất thường đã được kiểm | ≈53 (trước `bat_thuong`: ≈38) |
| Dòng đánh ★ còn mở (biến thể bất thường chưa có bot) | ≈69 (lúc lập: 82; `bat_thuong` đóng 13 dòng, mở rộng thêm 2) |

Các con số trên được đếm tự động từ cột "Mức" của các bảng dưới đây.

Nhận xét chung:

1. **Gần như mọi case kinh tế trong `cn_vat_pham` / `cn_xa_hoi` chạy bằng GM.** Gồm `shop_npc`, `nang_cap`, `ngoc`, `giao_dich`, `cho`, `bang_hoi`. Những case này tự cấp tiền bằng `smn`.
   - Chỉ chế độ `HoiQuy` (nhóm `nguoi_choi_that`) dùng người chơi thật, không GM: `shop_xu`, `shop_dac_biet`, `giao_dich`, `thu_cuoi`, `giftcode`.
2. **Giao dịch và chợ chỉ được kiểm qua gói 16 phía client, không có kiểm SQL.**
   - Giao dịch chỉ chuyển thuốc. Việc rút thuốc ra sau khi đối phương đã khoá nay có `bat_thuong` `bt_gd` (đối chiếu CSDL) — và đó là lỗi thật, đã sửa.
   - Chợ chỉ kiểm "xu giảm", không kiểm đúng giá.
3. **Trước `bat_thuong`: không kịch bản nào gửi số lượng âm hoặc bằng 0**, không có phép thử "số dư vừa đúng bằng giá", không thao tác nào khi đang chết. Nay `bat_thuong` phủ phần cửa hàng NPC (24/28/29/22), giao dịch, nâng cấp, cổng, đăng nhập, khi gục và gói sai trên 34 lệnh (mục cuối). Còn thiếu: trong trận PvP, chợ, kho, Pokémon (-121), bang.
4. **Bất biến `BatBien` chỉ biến thành FAIL trong `HoiQuy.chay1`** (`HoiQuy.java:376`). Gồm: tiền âm, tiền mất ngoài cửa sổ 6 giây, lượng tự tăng, nạp lặp bản đồ, kéo lùi, kẹt bản đồ, đường menu sai. Ở `BotChucNang`, các vi phạm có được thu nhưng không được báo.
5. **Có các assert luôn đúng hoặc rất yếu.** Ví dụ: `di_chuyen.nhay_xa` `KichBanDS.java:284`, `di_chuyen.o_chan` `:295`, `phan_ra.roi_do` `KichBanDS3.java:452`, `ham_nguc.csdl` `KichBanDS4.java:444`, `thuoc.mp` `KichBanDS3.java:85`, `thu_mto` (không có ket). Bảng dưới xếp các dòng này vào mức "Chỉ chạy qua".

---

## 1. Tài khoản, nhân vật, phiên

| Tính năng | Gói/cmd (server file:line) | Đường client | Nhóm bot / case (file:line) | Mức | Thao tác bất thường | Ghi chú |
|---|---|---|---|---|---|---|
| Đăng nhập | -40 khoá, 1 đăng nhập — `real/cmd/LoginHandler.java:255` | Màn đăng nhập | cn_dang_nhap `dang_nhap` KichBanDS.java:200 (.sai_mat_khau :204, .ten_khong_hop_le :208, .dung :212); nguoi_choi_that `dk_dang_ky` KichBanHoiQuy.java:107 (.sai_mat_khau :138) | Kiểm | Sai mật khẩu, tên sai: có. Spam đăng nhập: **có** — `bat_thuong` `bt_dang_nhap.bao` KichBanBatThuong.java:521 (8 lần đăng nhập/đóng + 2 phiên, tiền/thuốc bảo toàn, CSDL) | |
| Hai phiên cùng tài khoản | 1 (lần hai) | — | `dang_nhap.lan_hai` KichBanDS.java:233; `hai_phien` KichBanHoiQuy.java:653 (.phien_cu_khong_tac_dung :669: phiên cũ gửi 24 sau khi bị đẩy); BotHonLoan nhánh 11 :170 | Kiểm | Có | |
| Đăng ký trong game | 39 "#dk2" — `real/cmd/DangKyTrongGame.java:129` | Màn đăng ký | qa_toan_game `qa_dang_ky_trong_game` KichBanQaToanGame.java:731; `dk_dang_ky` (.trung_ten :113); `dk_han_muc_ip` KichBanHoiQuy.java:144 | Kiểm | Trùng tên, hạn mức IP: có | |
| Tạo nhân vật | 14 — `real/cmd/CreateCharHandler.java:20` | Màn chọn nhân vật | cn_dang_nhap `nhan_vat` KichBanDS5.java:22 (.ten_sai :31, .ten_gm_cam :36, .toi_da_3 :47); `qa_ten_pho_bien` KichBanQaToanGame.java:764 (.ten_tuc :783) | Kiểm | Tên sai/tục/quá 3 nhân vật: có | |
| Chọn nhân vật | 13 kiểu 0/1 — `real/cmd/SelectCharHandler.java:68` | Màn chọn nhân vật | mọi bot (vaoGame BotChucNang.java:646); `chon_nv_da_xoa` KichBanHoiQuy.java:187 (.chon_kieu1/.chon_kieu0 :206) | Kiểm | Chọn nhân vật đã xoá: có | |
| Xoá nhân vật | 13 kiểu 2 | Màn chọn nhân vật | `nhan_vat.xoa` KichBanDS5.java:58; `chon_nv_da_xoa.xoa` KichBanHoiQuy.java:199 | Kiểm | — | |
| Khôi phục nhân vật | 13 kiểu 3 | Màn chọn nhân vật | `nhan_vat.khoi_phuc` KichBanDS5.java:63; `chon_nv_da_xoa.khoi_phuc` KichBanHoiQuy.java:213 | Kiểm | — | `nhan_vat.khoi_phuc` vẫn qua khi không có CSDL |
| Đổi tên nhân vật (lượng) | -30 → -31 — `real/RealController.java:1907`, `real/CharDoiTenNhanVat.java:68` | NPC dịch vụ | — | Thiếu | ★ thiếu lượng, tên trùng, bấm đúp | Có trừ lượng |
| Rời sảnh, vào bản đồ đầu | 12 (-500) | — | cn_dang_nhap `roi_sanh` KichBanDS.java:240 | Kiểm | — | |
| Rớt mạng / lưu khi đăng nhập lại | đóng socket | — | `mat_ket_noi` KichBanDS.java:492 (.luu_xu :510); `luu_dang_nhap_lai` KichBanHoiQuy.java:628 (.csdl_khop :647) | Kiểm | Rớt mạng: có | |
| Khởi động lại máy chủ giữ dữ liệu | — | — | `khoi_dong_lai` KichBanHoiQuy2.java:435 (BỎ QUA vì conf thiếu -Dkhoidonglai, :437) | Thiếu* | — | Có thêm `qa_khoi_dong_lai_ghi/kiem` KichBanQaToanGame.java:502/519 nhưng ngoài cổng |
| Hướng dẫn tân thủ: Làm / Bỏ qua | -30 (-527,40,0/1), -32 96 | Sổ tay | `hd_lam` KichBanHoiQuy.java:222; `hd_bo_qua` :266 (.thuong đúng +1450 xu :287) | Kiểm | Bỏ qua 2 lần: Thiếu ★ | |
| Cập nhật trong game | 1 (phiên bản) → -115 | Hộp cập nhật | `qa_cap_nhat` KichBanQaToanGame.java:114 | Kiểm | — | |
| Ô nhanh, trang chiêu (lưu máy chủ) | -103 kiểu 1 — `real/NgoaiHinhGame.java:261` | HUD | `qa_o_nhanh_dang_nhap_lai` KichBanQaToanGame.java:477 (:490) | Kiểm | Rớt mạng: có | |
| Mua trang ô nhanh 3 (xu) | -103 kiểu 2 — `real/NgoaiHinhGame.java:58` | Sổ tay ô nhanh | — | Thiếu | ★ thiếu xu, bấm đúp | |
| Chế độ huấn luyện (xem) | -122 kiểu 0 — `real/HocChieuGame.java:709` | — | `dk.che_do_huan_luyen` KichBanHoiQuy.java:134 | Kiểm | — | |
| Cài đặt | 104 — `real/Char.java:1935` | Menu → Cài đặt | — | Thiếu | — | Phần lớn lưu ở client (RMS) |
| Nạp tiền (demo) / mua gói | -30 → `com/htbb/pokemon/NapTienGame.java:637` (moGoc), :781 (xacNhanMua) | NPC dịch vụ (`real/NpcDichVu.java:412`) | — | Thiếu | ★ bấm đúp xác nhận | Tiền thật: kiểm sổ cái |
| Nạp SMS cũ | 107 — `real/MapXuLyLenh.java:1406` | — | — | Thiếu | ★ | Kiểm xem còn mở không |
| Thành viên (chặn quyền lợi) | `com/htbb/pokemon/ThanhVien.java` (chanVaBao, ví dụ `real/RealController.java:2139`) | — | — | Thiếu | — | |

## 2. Di chuyển, bản đồ, khu, cổng

| Tính năng | Gói/cmd (server file:line) | Đường client | Nhóm bot / case (file:line) | Mức | Thao tác bất thường | Ghi chú |
|---|---|---|---|---|---|---|
| Đi bộ | 4 — `real/CharCapNhat.java:1078`, `real/Map.java:5275` | Chạm / phím | cn_ban_do `di_chuyen` KichBanDS.java:251 (.hop_le :275); `di_bo` KichBanHoiQuy.java:347 (.hack_toc_do_bi_chan :367) | Kiểm | Hack tốc độ: có. Nhảy xa / ô chặn: chỉ chạy qua (ket luôn đúng :284/:295) | Bất biến KEO_LUI BatBien.java:131 |
| Qua cổng (đổi bản đồ) | 12: short toMap, short x, short y — `real/MapXuLyLenh.java:1933` | Đi vào cổng | cn_cong_tat_ca `cong_tat_ca` KichBanDS2.java:36 (.cong :62) (GM); người chơi thường: `di_bo`, `cua_ai` | Kiểm | Cổng sai / toạ độ sai, spam cổng, rớt mạng ngay sau gói cổng: **có** — `bt_dut.cong_gia` / `.spam_cong` / `.doi_map` KichBanBatThuong.java:479-497 | Bất biến NAP_LAP BatBien.java:112 |
| Cổng khoá theo cấp/cửa ải | 12 → popup 95, -32 | Hộp "Mở khoá" | `qa_khoa_popup` KichBanQaToanGame.java:293 (.hien_lai :346) | Kiểm | Bấm lại dưới 10 giây: có | |
| Vào bản đồ chưa mở | 27 mto | — | `qa_moba_chua_mo` KichBanQaToanGame.java:355 | Kiểm | — | Dùng lệnh GM |
| Đổi khu | 27 "khu" (`real/MapChat.java:338`) → -30 (-34, menu, khu) — `real/MapDoMenu.java:1413` | Chat "khu" / nút khu | Chỉ có `pb_khu` KichBanPhoBanCup.java:144: mở menu trong phó bản | Chỉ chạy qua | ★ khu sai, chưa hết 20 giây, khi đang đánh, đang giao dịch: Thiếu. Đổi khu khi đang gục: chỉ chạy qua (`bt_guc` KichBanBatThuong.java:863, kiểm bảo toàn tiền + log) | |
| Dịch chuyển tới boss | -33: byte x, byte y, short map — `real/Map.java:9015` | Menu boss | — | Thiếu | ★ toạ độ giả | |
| Bay Pidgeot (xa phu) | 79: byte xaphu, short map — `real/Map.java:14451` | NPC Pidgeot | `dich_vu.pidgeot.bay` KichBanNPC.java:501 (ngoài cổng) | Thiếu* | ★ thiếu xu, map không có trong danh sách | Kiểm tiền rồi mới trừ, không nguyên tử |
| Về làng khi chết | 31 — `real/Map.java:17256` | Hộp gục | `chet_hoi_sinh` KichBanDS.java:473 (:487) | Kiểm | Đang sống gửi 31 (không được dịch chuyển miễn phí), rớt mạng khi đang gục: **có** — `bt_guc.ve_lang_khi_song` / `.rut_mang_khi_guc` KichBanBatThuong.java:863 | |
| Về làng miễn phí (Sổ tay) | -30 (-527,0,…) — `real/VeLangMienPhiGame.java` | Sổ tay huấn luyện (mục Về làng) | `ve_lang` KichBanHoiQuy.java:376 (.mien_phi_ve_lang :386); BotHonLoan nhánh 12 bấm 2 lần :178 | Kiểm | Bấm 2 lần: chỉ chạy qua | |
| Vé dịch chuyển: mua | -30 (-591, cấp, gói) — `real/VeDichChuyenGame.java:91` (mua) | Kecleon | `ve_dich_chuyen` KichBanHoiQuy2.java:182 (.mua_1 :207) | Kiểm | Thiếu xu, túi đầy: Thiếu ★ | |
| Vé dịch chuyển: dùng | 22(162) → -30 (-590) — `real/VeDichChuyenGame.java:276/:321` | Túi | `ve_dc.ve_lang` :221, `.hoi_chieu` :228 | Kiểm | Bấm đúp: có | |
| Giấy thổ địa | 22(19) | Túi | `thuoc.ve_thanh` KichBanDS3.java:95 | Kiểm | — | |
| Lên tàu / mua vé tàu | 82 — `real/Map.java:14476`; 81 — `real/Map.java:14513` | NPC bến tàu | ket_tau `kt_len_tau` KichBanKetTau.java:79; `tau_new_bark` KichBanHoiQuy.java:556 | Kiểm | Từ chối khi chưa đủ điều kiện: có. Mua vé 81 chỉ có ở KichBanNPC.java:473 (ngoài cổng) | |
| Cứu kẹt bản đồ khi đăng nhập | 1/13 | — | `kt_cuu_107` / `kt_cuu_khoa` KichBanKetTau.java:120/124 | Kiểm | — | Bất biến KET BatBien.java:318 |
| Phòng riêng: Rời / Bỏ cuộc | -112 — `real/PhongRiengGame.java:224` | Thẻ trạng thái | (pb_roi dùng -527/61, không gửi -112) | Thiếu | ★ | |

## 3. Chiến đấu, kỹ năng, PvP

| Tính năng | Gói/cmd (server file:line) | Đường client | Nhóm bot / case (file:line) | Mức | Thao tác bất thường | Ghi chú |
|---|---|---|---|---|---|---|
| Đánh quái | 9 (id, ô chiêu) — `real/Map.java:4567` | Chạm quái | cn_chien_dau `danh_quai` KichBanDS.java:302 (.exp_csdl :332) | Kiểm | — | |
| Đánh nhiều mục tiêu | 106 — `real/Map.java:4501` | — | gym_lang `gl_gym_lang` .lenh106 KichBanGymLang.java:181 | Kiểm | — | |
| Ngắm tay | -113 — `real/BanHuongGame.java:502` | Nút ngắm | ngam_tay `nt_ban` KichBanNgamTay.java:158 (.hoi_chieu :185), `nt_khoang_cach` :239, `nt_pve` :409 | Kiểm | Hai phát liền: có | |
| Cộng điểm kỹ năng / dùng chiêu | 36 — `real/Map.java:15456` | Nhân vật → Kỹ năng | `ky_nang` KichBanDS.java:377 (.cong_diem :413, .hoi_chieu :460, .o_sai :466) | Kiểm | Spam, ô 99: có | |
| Cộng điểm tiềm năng | 34 — `real/Map.java:15495` | Nhân vật | `hd_bo_qua.diem_vao_nhan_vat` KichBanHoiQuy.java:303 | Kiểm | Cộng quá số điểm / số âm: Thiếu ★ | |
| Học chiêu | 109 — `real/Map.java:15415`; -122 học theo loài | — | — | Thiếu | ★ | |
| Buff | 51 — `real/Map.java:15052` | — | — | Thiếu | — | |
| Xem thông tin quái | 7 — `real/MapXuLyLenh.java:759` | — | mọi bot đánh quái | Chỉ chạy qua | — | |
| Tự động đánh (bật / nhặt / uống) | -100 — `real/TuDongGame.java:224` | Menu → Tự động đánh | Chỉ có `nt_tu_dong` (bị từ chối trong PvP) KichBanNgamTay.java:400; `KichBanHoiSinh` (ngoài cổng) | Thiếu* | ★ tự động khi túi đầy | |
| Tự động đánh bị chặn trong PvP | -100 | — | `nt.tu_dong.tu_choi` KichBanNgamTay.java:406 | Kiểm | — | |
| Tiến hoá | (máy chủ gửi -92) | — | `tien_hoa` KichBanDS.java:343 (.giua_tran :362) | Kiểm | Tiến hoá giữa trận: có | |
| Chết → về làng | 31 | Hộp gục | `chet_hoi_sinh` KichBanDS.java:473 | Kiểm | — | |
| Hồi sinh (các lựa chọn, phí xu ×1,5) | -101 — `real/HoiSinhGame.java:542` | Hộp gục | `KichBanHoiSinh` hs_* :369-378 (ngoài cổng) | Thiếu* | ★ thiếu xu, bấm đúp, đang trong đấu trường | |
| Boss thế giới | 9 | — | `boss` KichBanDS5.java:75 (.ha :107) (GM addboss) | Kiểm (GM) | — | Boss theo giờ chỉ có ở BotNguoiChoi (ngoài cổng) |
| Đánh người, bật PK | 6 — `real/Map.java:4341`; 67 — `real/Map.java:15019` | Tiện ích & PK | `pvp` KichBanDS4.java:480 (.bat_pk :500, .ha_doi_thu :523, .elo_khong_nuoi :534) | Kiểm | — | |
| An toàn trong làng | 6 | — | `pvp_an_toan` KichBanHoiQuy2.java:395; `gl_pvp_lang` KichBanGymLang.java:199; `nt_an_toan` KichBanNgamTay.java:324 | Kiểm | — | |
| Khăn / cờ PK | 22(14/15) | Túi | co_pk `cp_bat_tat` / `cp_danh_roi_tat` / `cp_vao_lai` / `cp_ten_do` KichBanCoPk.java:114-221 | Kiểm | Tháo khi đang đánh, rớt mạng: có | |
| Khoá mục tiêu từ client mới | 6 | — | `nt_khoa` KichBanNgamTay.java:367 | Kiểm | — | |
| Nhặt đồ / thuốc dưới đất | 18 — `real/Map.java:4883`; 19 — `real/Map.java:5204` | Chạm vật | cot_truyen (BotXuyenSuot.java:537-538) | Chỉ chạy qua | ★ túi đầy, nhặt từ xa, nhặt đồ người khác, đã chết | Máy chủ không kiểm khoảng cách (`real/Map.java:4893`) |
| Nhặt vật nhiệm vụ | -65 — `real/Map.java:5148` | — | `tdnv_weedle` (.xong) KichBanTienDoNhiemVu.java:259 | Kiểm | — | |
| Nhặt ngọc | -41 — `real/Map.java:4716` | — | BotXuyenSuot.java:539 (cot_truyen) | Chỉ chạy qua | — | |

## 4. Pokémon / Thú: bắt và quản lý

| Tính năng | Gói/cmd (server file:line) | Đường client | Nhóm bot / case (file:line) | Mức | Thao tác bất thường | Ghi chú |
|---|---|---|---|---|---|---|
| Mua bóng ở Chansey | -30 (-581) | NPC Chansey | thu_phuc `tp_bat` .mua_bong KichBanThuPhuc.java:143 | Kiểm (GM) | — | |
| Ném bóng bắt | -121 kiểu 1: short idQuai, byte thứ tự — `real/BatPokemonGame.java:408` | HUD bóng | `tp_bat` .bat :167, .quai_thuong_tu_choi :158 | Kiểm | Ném vào quái thường: có | |
| Mở Hộp PC | -121 kiểu 3 | Menu → Pokémon | `tp_bat` .hop_pc_mo :145 | Kiểm | — | |
| Hộp PC: theo / luyện / tiến hoá / thả / chuyển | -121 kiểu 4: byte hd, int id — `real/BatPokemonGame.java:1131` | Hộp PC | — | Thiếu | ★ id không có, thả khi đang trao đổi | |
| Trao đổi Pokémon | -121 kiểu 6: mời(short)/trả lời(byte)/đặt(byte n, n×int)/khoá/xác nhận/huỷ — `real/TraoDoiPokemonGame.java:115` | Hộp PC → Trao đổi | — | Thiếu | ★ đổi sau khi khoá, rớt mạng, xác nhận đúp | Rủi ro nhân bản |
| Chợ Pokémon | -121 kiểu 7: đăng(int id, int giá)/mua(long tin)/huỷ(long tin) — `real/ChoPokemonGame.java:115` | Hộp PC → Chợ | thu_phuc `tp_cho` KichBanThuPhuc.java:178 (.mua đúng giá :228, .thue :237) | Kiểm (GM) | Mua đúp, mua tin vừa huỷ, tự mua tin của mình: Thiếu ★ | Nhịp 800 ms :125 |
| Pokédex | -121 kiểu 2, -126 — `real/LoaiMayChu.java:43` | Menu → Pokédex | `pokedex_menu` KichBanDS4.java:573; `tp.pokedex_da_bat` KichBanThuPhuc.java:173 | Kiểm | — | |
| Thả thú | -119 kiểu 1: ubyte n, n×(byte loại, short id) — `real/QuanLyThuGame.java:360` | Pokémon → Quản lý | qa_toan_game `qa_quan_ly_thu` KichBanQaToanGame.java:642 (.tha_khoa_dup :671) | Kiểm (GM) | Bấm đúp: có | |
| Khoá thú | -119 kiểu 2 — `real/QuanLyThuGame.java:446` | — | `qa_quan_ly_thu` .dang_nhap_lai :684 | Kiểm | — | |
| Vé mở ô thú (mua / dùng) | -119 kiểu 3/4 — `real/QuanLyThuGame.java:509` | — | `qa_ve_o_thu` KichBanQaToanGame.java:571 (.dung :596) | Kiểm | Dùng đúp: có | |
| Mở rộng hành trang | -107: byte kiểu (1 dùng vé / 2 mua xu) — `real/MoRongTuiGame.java:234` | Túi | — | Thiếu | ★ bấm đúp, thiếu xu | |
| Cưỡi thú | -45 kiểu 0: byte, short id — `real/doCuoiThu.java:18` | Túi / Pokémon | `thu_cuoi` KichBanDS3.java:554 (.cuoi :582) (GM); `thu_cuoi` KichBanHoiQuy2.java:233 (.cuoi :276, .mua_sao đúng -300 lượng :248) | Kiểm | — | |
| Xuống thú | 108 kiểu 0 — `real/Map.java:5436` | — | `thu_cuoi.xuong` KichBanHoiQuy2.java:280 | Kiểm | — | |
| Tháo trang phục / rìu (xuống thú kiểu 1/2) | 108 kiểu 1/2 — `real/Map.java:5511/:5535` | — | — | Thiếu | ★ túi đầy (máy chủ không kiểm) | |
| Thú đi theo | -45 kiểu 1 | Pokémon | — | Thiếu | — | |
| Bán thú cho NPC | -30 (-800/-801, menu, 1) — `real/RealController.java:1074`, `real/MapDoMenu.java:673` (-801), `:689` (-800) | Thú → Bán (trong nhà) | — | Thiếu | ★ bán thú đã khoá, đang giao dịch, bán đúp | Bỏ qua khoá thú và kiểm giao dịch |
| Tặng thú | -30 (-800 "Tặng" / -514) | — | — | Thiếu | ★ | |
| Đặt tên Pokémon | -30 (-561) → -31 — `real/DatTenPetGame.java:397/:452` | NPC | `dat_ten_pet` KichBanDatTenPet.java:25 (ngoài cổng) | Thiếu* | ★ tên tục, bấm đúp thẻ | |
| Đổi màu đồ thú | -61 — `real/Map.java:8716` | — | — | Thiếu | — | Rỗng trong nhà (`real/OfflineMap.java:1964`) |
| Tháo trang bị thú | 29 → `real/Map.java:16352` | — | — | Thiếu | ★ túi đầy | |
| Hệ sinh thái (xem các mục) | -30 (-90 → "Hệ sinh thái") — `com/htbb/pokemon/HeSinhThaiMenu.java` | Menu → Hệ sinh thái | `he_sinh_thai` KichBanDS5.java:113 (:138 mỗi mục có phản hồi) | Chỉ chạy qua | — | Chỉ mở xem |
| Nhà trẻ và trứng / câu cá / khảo cổ / đua | `HeSinhThaiMenu.java:144` (G_NHA_TRE); chat "cauca"/"dao"/"dua" | Hệ sinh thái | — | Thiếu | ★ | |

## 5. Vật phẩm, cửa hàng

| Tính năng | Gói/cmd (server file:line) | Đường client | Nhóm bot / case (file:line) | Mức | Thao tác bất thường | Ghi chú |
|---|---|---|---|---|---|---|
| Mua thuốc ở NPC (xu) | 24: byte n; mỗi món: byte loại(4), short id, short sl — `real/OfflineMap.java:1568` | NPC → Mua thuốc | cn_vat_pham `shop_npc` KichBanDS3.java:109 (.mua_thuoc :127, .thieu_xu :154) (GM); `shop_xu` KichBanHoiQuy.java:588 (.dung_gia đúng giá :604, .khong_du_xu_khong_tru :611) | Kiểm | Thiếu xu: có. **`bat_thuong` (người chơi thường):** bấm 14 lần với số dư vừa đủ 10 bình, thiếu 1 xu, SL 0/-5/-32768/32767, id/loại/kind lạ, gói cụt, túi đầy, rớt mạng ngay sau gói mua, khi đang gục — `bt_mua.*` KichBanBatThuong.java:247, `bt_tui.mua_khi_day` :375, `bt_dut.mua` :433, `bt_guc.bao_toan` :863 (đối chiếu CSDL) | Bot HonLoan nhánh 10: mua rồi rớt mạng (không assert riêng) |
| Mua trang bị ở NPC | 24 loại 3: short id, short sl, byte kind | NPC → vũ khí | `shop_npc` .mua_vu_khi :131, .mon_khong_ban :138 | Kiểm (GM) | Id không bán: có. Túi đầy (mua lô 20 tới khi đầy, rồi mua thêm): **có** — `bt_tui.lam_day` / `.mua_khi_day` KichBanBatThuong.java:375 | Nhánh lượng :1689 kiểm rồi mới trừ, không nguyên tử |
| Bán đồ cho NPC | 28: short id — `real/OfflineMap.java:1242` | NPC → Bán | `shop_npc.ban_do` KichBanDS3.java:145 | Kiểm (GM, chỉ client) | **có (người thường):** bán đúp ×8 cùng id, bán đồ đang mặc, id lạ / gói cụt, rớt mạng ngay sau bán — `bt_ban.*` KichBanBatThuong.java:319, `bt_dut.ban` :433. Bán đồ đặt chợ / đồ khoá: Thiếu ★ | Xoá CSDL trước, rồi addXu |
| Mua lại đồ đã bán | 95 ở NPC -67 — `real/Map.java:14232` | NPC | — | Thiếu | ★ | |
| Cửa hàng lượng / lượng khoá | 85: ubyte id — `real/doBuySpecialItem.java:59` | Menu → Cửa hàng | `shop_luong` KichBanDS3.java:159 (.gioi_han_tuan :181); `shop_dac_biet` KichBanHoiQuy2.java:76 (.tru_dung_tien :111, .het_tien_khong_tru_oan :136); BotHonLoan nhánh 9 bấm 2 lần :146 | Kiểm | Spam ×5, hết tiền: có. Số dư vừa đúng: Thiếu ★ | `.nhan_luong_khoa.id2` là lỗi ĐÃ BIẾT (HoiQuy.java:52) |
| Giftcode lượng khoá | -31 (…,7,mã) | Thư/Quà → Nhập giftcode | `shop_dac_biet.giftcode_luong_khoa` KichBanHoiQuy2.java:90 | Kiểm | — | |
| Cửa hàng VIP / Trung thu / túi đồ | -76: short loại, ubyte index — `real/Map.java:7719` | — | — | Thiếu | ★ index sai, túi đầy, thiếu tiền | Có thể mất tiền khi món không khớp nhánh nào (`real/Map.java:8218-8306`) |
| Kecleon (vé dịch chuyển) | -30 (-591) | NPC Kecleon | `ve_dich_chuyen` KichBanHoiQuy2.java:182 | Kiểm | — | |
| Nến (lượng) | -31 actor -65 — `real/RealController.java:1346` | NPC | — | Thiếu | ★ số âm / chữ / 100001 | |
| Uống thuốc | 22: ubyte loại — `real/DungVatPham.java:727` → `real/doUsePotion.java:58` | Túi / ô nhanh | `thuoc` KichBanDS3.java:69 (.hp yếu :84, .mp luôn đúng :85, .khong_co :104); `dung.binh_hp` KichBanDungVatPham.java:136 | Kiểm | Thuốc không có: có. Đang chết: **có** — `bt_guc.khong_hoi_mau` KichBanBatThuong.java:863 | BotHonLoan nhánh 6 uống đúp |
| Dùng vật phẩm (rương, vé, quà…) | 29: short id — `real/Map.java:15847` → `real/DungVatPham.java:237` | Túi | `dung_vat_pham` KichBanDungVatPham.java:92 (≈25 assert :114-:254, .id_khong_con :249) (GM) | Kiểm | Id sai: có. Mặc/tháo bấm liên tục ×9 (đồ không nhân đôi / mất, đối chiếu CSDL): **có** — `bt_ban.mac_thao_lien` KichBanBatThuong.java:319. Túi đầy khi mở rương: Thiếu ★ | |
| Vật phẩm đặc biệt (x2 EXP) | 86: short id — `real/MapMuaBan.java:521` | — | — | Thiếu | ★ | |
| Mặc trang bị | 29 — `real/Map.java:16230-16262` | Túi | `trang_bi` KichBanDS3.java:186 (.mac :195, .doi_mon :200, .sai_lop :212, .cap_thap :220, .moi_o :239) | Kiểm | Sai lớp, cấp thấp: có | |
| Tháo trang bị | 29 (đồ đang mặc) — `real/DungVatPham.java:416` | Nhân vật | `dung.thao_trang_bi` KichBanDungVatPham.java:222 | Kiểm | Túi đầy: **có** — `bt_tui.thao_khi_day` + `.vao_lai` KichBanBatThuong.java:375 (món vẫn mặc, CSDL đủ số món) | |
| Vứt đồ (huỷ) | 61: short id [, byte 1 xác nhận đồ khoá] — `real/Map.java:15585` | Túi | dùng để dọn túi ở KichBanDungVatPham.java:102, KichBanQaToanGame.java:424 | Chỉ chạy qua | ★ vứt đồ đang đặt chợ / trong ô nâng cấp | |
| Vứt thuốc | -21: short loại [, short sl] — `real/Map.java:15539` | Túi | `thuoc.bo_ra_dat` KichBanDS3.java:100 | Kiểm | SL âm / 0 / quá số có: Thiếu ★ | |
| Vứt nguyên liệu / ngọc | -23: byte kiểu, short id, [byte khoá], [short sl] — `real/MapEpNgoc.java:1134` | Túi | — | Thiếu | ★ vứt ngọc đã thêm vào ô nâng cấp | |
| Kho đồ (gửi / lấy / đóng) | 68 / 69: short id; 70 — `real/OfflineMap.java:599/:560` | NPC kho | — | Thiếu | ★ gửi và lấy dồn dập (68 chạy luồng phiên, 69 chạy luồng bản đồ), lấy khi túi đầy, đang giao dịch | 69 không có checkTrade |
| Sửa đồ | 72: byte 0/1/2 — `real/OfflineMap.java:1831` | NPC thợ | `KichBanNPC` :428 (ngoài cổng) | Thiếu* | ★ thiếu xu | |
| Đổi hệ vật lý / phép | -54: byte, short id — `real/Map.java:8792` | — | — | Thiếu | ★ bấm liên tục | CÓ THỂ: chỉ số bị chia 10 mỗi lần |
| Trang phục (xin / mặc / chế / mua) | -116 — `real/TrangPhucGame.java:739` | Pokémon → Trang phục | — | Thiếu | ★ mua đúp | |

## 6. Nâng cấp, ngọc, chế tạo, phân rã

| Tính năng | Gói/cmd (server file:line) | Đường client | Nhóm bot / case (file:line) | Mức | Thao tác bất thường | Ghi chú |
|---|---|---|---|---|---|---|
| Nâng cấp trang bị | 71 byte; 75 short id; 76 short ngọc [byte 1=gỡ]; 77 byte (-1 huỷ / 1 làm) — `real/MapEpNgoc.java:1108/:379/:419/:599` | NPC thợ rèn | `nang_cap` KichBanDS3.java:275 (.len_15 :321, .bua_bao_ho :322, .pity :323, .rot_khong_bua :347, .thieu_xu :355) | Kiểm (GM) | Thiếu xu: có. **`bat_thuong`:** bán ngọc sau khi thêm (76 → 78 → 77), 77 gửi ×6, rớt mạng ngay sau 77 — `bt_nc.*` KichBanBatThuong.java:784. Vứt ngọc (-23) giữa chừng: Thiếu ★ | Chế độ dài hạn (`NangCapLauDaiGame.thu`) đếm lại đá trong `truNeuDu` nên an toàn — bot xác nhận ("Lên +1 cần … đá", không trừ xu). Nhánh cũ (LauDai tắt) chưa kiểm |
| Tự động nâng cấp | -69: short id, byte cấp, byte n, n×(short ngọc, byte khoá) — `real/MapEpNgoc.java:545` | — | — | Thiếu | ★ | |
| Mua ngọc / đá | 74: short n, n×short id — `real/OfflineMap.java:1433` | NPC | `ngoc.mua` KichBanDS3.java:369 | Kiểm (GM) | n âm, id ngoài bảng: Thiếu ★ | |
| Bán ngọc | 78: short id, byte khoá, [short sl] — `real/OfflineMap.java:1341` | NPC | `ngoc.ban` KichBanDS3.java:378 | Kiểm (GM) | Bán ngọc đang nằm trong ô nâng cấp: **có** (`bt_nc.ban_da_giua_chung`). SL 0/âm, bán vé ô: Thiếu ★ | |
| Ghép ngọc | -36: byte 1, byte len(3..5), len×short — `real/MapEpNgoc.java:848` | NPC | `ngoc.ghep` KichBanDS3.java:391 | Kiểm (GM) | Trùng id trong danh sách, thiếu xu đúng lúc: Thiếu ★ | Trừ xu sau khi trao kết quả (:979) |
| Đục lỗ / khảm | -34 byte; -35 byte — `real/MapEpNgoc.java:822/:987` | NPC | `ngoc.duc_lo` :408, `ngoc.kham` :427 | Kiểm (GM) | — | |
| Ngọc hệ: đặt / nâng / tháo / mài / phân rã | -120 kiểu 3..7 — `real/BoNgocGame.java:653` | NPC Machamp | — | Thiếu | ★ | Có synchronized và truNeuDu |
| Chế tạo mới | -120 kiểu 1: short idMẫu [, byte hệ, byte đá] — `real/CheTaoGame.java:203` | NPC | — | Thiếu | ★ túi đầy, bấm đúp | Trừ trước, nếu taoMon null thì không hoàn |
| Chế tạo / nâng cấp cũ | -52: byte option … — `real/MapCheTao.java:428` | — | — | Thiếu | ★ thiếu xu đúng lúc | Trừ xu sau khi trao (:434 / :446-527) |
| Phân rã / nghiền bột | -68 kiểu 4: short id — `real/OfflineMap.java:1967` | NPC | `phan_ra` KichBanDS3.java:433 (.do_shop :442; .roi_do luôn đúng :452) | Chỉ chạy qua | ★ phân rã đồ đang mặc / đặt chợ, bấm đúp | Trừ 2000 xu SAU khi xoá đồ |
| Tách nguyên liệu | -68 kiểu 0: short id — `real/MapEpNgoc.java:227` | — | — | Thiếu | ★ | Không có isItemSelled, checkTrade, Database.deleteItem |
| Thêm thuộc tính / khoá đồ người / khoá đồ thú | -68 kiểu 1/2/3/5: short id, short ngọc, byte khoá … — `real/Map.java:8469`, `real/OfflineMap.java:1891/:1926`, `real/MapCheTao.java:707` | — | — | Thiếu | ★ id ngọc ngoài bảng | |
| Tẩy bình / trái cây / trồng cây | -67 `real/Map.java:8556`; -66 `real/Map.java:8622`; -59 `real/OfflineMap.java:1841` | — | — | Thiếu | — | |

## 7. Giao dịch giữa người chơi (cmd 66 → `real/MapMuaBan.java:100`, khoá `Char.KHOA_GIAO_DICH`)

| Tính năng | Gói/cmd (server file:line) | Đường client | Nhóm bot / case (file:line) | Mức | Thao tác bất thường | Ghi chú |
|---|---|---|---|---|---|---|
| Mời | 66/0: short id — :148 | Chạm người → Giao dịch | cn_xa_hoi `giao_dich` KichBanDS3.java:458 (.loi_moi :487) (GM); `giao_dich` KichBanHoiQuy2.java:290 (.moi :344) | Kiểm | Mời lại trong 60 giây: Thiếu ★ (bot `bt_gd` chờ đúng 60 giây) | |
| Từ chối | 66/-1: short id — :129 | — | — | Thiếu | — | |
| Chấp nhận | 66/1: short id — :201 | — | như trên | Kiểm | Chấp nhận khi không có lời mời: Thiếu ★ | |
| Đặt thuốc | 66/2/1: byte thuốc, short sl — :246+ | Cửa sổ giao dịch | `.thuoc` KichBanDS3.java:488; `.chuyen_dung` / `.bao_toan_thuoc` KichBanHoiQuy2.java:345-346 | Kiểm (chỉ client, không SQL) | SL âm / 0 / quá số có, thuốc khoá, túi bên nhận đầy: Thiếu ★ | |
| Rút thuốc | 66/2/2: byte thuốc — :283-299 | `bat_thuong` `bt_gd.doi_sau_khi_khoa` KichBanBatThuong.java:640 | Kiểm | **có** | **LỖI THẬT, ĐÃ SỬA 25/10/2026:** C khoá + xác nhận, A rút 10 bình còn 1 rồi khoá → C mất 10 thuốc 4 chỉ nhận 1 thuốc 1. Nay `GiaoDichThuoc.rutDoDuoc` chặn rút khi đối phương đã khoá (`real/MapMuaBan.java`, test `server/test/KiemGiaoDichRutDo.java`) |
| Đặt đồ (bị từ chối) | 66/2/0 | — | — | Thiếu | ★ | |
| Huỷ | 66/3 — :390 | — | `giao_dich.huy` KichBanDS3.java:502 | Kiểm | — | |
| Khoá / xác nhận | 66/4, 66/5 — :394/:420 | — | cả hai bản giao_dich; `bt_gd.xac_nhan_dup` KichBanBatThuong.java:640 | Kiểm | Xác nhận đúp ×5 hai bên (chuyển đúng 1 lần): **có**. Xác nhận khi đang di chuyển xa: Thiếu ★ | |
| Rớt mạng giữa giao dịch | — | — | `giao_dich.rot_mang_bao_toan` KichBanHoiQuy2.java:364; `bt_gd.rut_mang` / `.khong_ket` / `.csdl` KichBanBatThuong.java:640 | Kiểm | Có | `bat_thuong` ngắt SAU khi A đã khoá + xác nhận, C đang khoá; đối chiếu CSDL |

## 8. Chợ người chơi

| Tính năng | Gói/cmd (server file:line) | Đường client | Nhóm bot / case (file:line) | Mức | Thao tác bất thường | Ghi chú |
|---|---|---|---|---|---|---|
| Rao bán đồ | 92: bool true, byte npc, byte shop, short id, int giá, [byte 0] → -31 — `real/OfflineMap.java:1075` | NPC chợ | cn_xa_hoi `cho` KichBanDS3.java:508 (.dang_ban :528) | Kiểm (GM) | Giá 0/âm, đồ khoá, rao 2 lần: Thiếu ★ | Chỉ số npc không kiểm biên |
| Rao bán ngọc theo số lượng | 92 type 1 → `real/OfflineMap.java:975` | NPC chợ | — | Thiếu | ★ SL vượt số có | |
| Huỷ rao | 92: bool false … | NPC chợ | — | Thiếu | ★ huỷ đúng lúc có người mua | |
| Xem hàng | 94: byte kiểu, byte npc, byte shop [, short id người bán] — `real/OfflineMap.java:1776` | NPC chợ | `cho` (:532-534) | Chỉ chạy qua | — | |
| Mua hàng | 95: byte npc, byte shop, short người bán, short id, [byte] → -32 popup 2 → `real/OfflineMap.java:318` | NPC chợ | `cho.mua` :548 (chỉ "xu giảm"), `cho.thue` :549 | Kiểm (GM, không SQL) | Hai người mua cùng lúc, số dư vừa đúng, tiêu xu ở luồng khác giữa lúc kiểm và lúc trừ, người bán offline: Thiếu ★ | CÓ THỂ: cộng tiền người bán trước khi trừ người mua, subXu kẹp về 0 (:340/:408-426) |
| Lệnh 62 (rao bán kiểu cũ) | 62 — `real/Map.java:8380` (rỗng) | — | — | Thiếu | — | Không làm gì |

## 9. Xã hội: chat, bạn bè, tổ đội, bang hội, thư

| Tính năng | Gói/cmd (server file:line) | Đường client | Nhóm bot / case (file:line) | Mức | Thao tác bất thường | Ghi chú |
|---|---|---|---|---|---|---|
| Chat bản đồ | 27 — `real/Map.java:16745` | Ô chat | `chat.ban_do` KichBanDS4.java:106 | Kiểm | — | HoiQuy `chat.ban_do` vẫn qua nếu hai người ở khác bản đồ (KichBanHoiQuy2.java:383) |
| Tin riêng | -124 (1,4,tên,nội dung) — `real/KenhChatGame.java:255`; -5 | Đội/Bạn bè | `chat.tin_rieng` KichBanDS4.java:112; KichBanHoiQuy2.java:387 | Kiểm | — | |
| Loa | -31 (actor,1,nội dung) | Ô chat | `chat.loa` :118, `.loa_khong_co` :124 | Kiểm | Không có loa: có | |
| Chống spam chat | 27 ×8 | — | `chat.chong_spam` KichBanDS4.java:143 | Kiểm | Spam: có | |
| Kênh chat: chặn / kênh khác | -124 kiểu khác | — | — | Thiếu | — | |
| Kết bạn | 101: short id, byte (0 mời / 1 nhận) — `real/MapXaHoi.java:194` | Chạm người | `ban_be.ket_ban` KichBanDS4.java:161 (GM) | Kiểm | Gửi 101 ok=1 khi không có lời mời, đồng ý lặp: **có** — `bt_ket_ban.*` KichBanBatThuong.java:757 | **LỖI THẬT, ĐÃ SỬA 25/10/2026:** ok=1 không cần lời mời = ép kết bạn, lặp = danh sách trùng. Nay `com.htbb.pokemon.LoiMoiKetBan` (lời mời một lần, hạn 2 phút, không thêm trùng); test `server/test/KiemKetBan.java` |
| Xoá bạn | -6: UTF tên — `real/Map.java:15381` | — | `ban_be.xoa` :165 (chỉ kiểm còn kết nối) | Chỉ chạy qua | ★ tên không tồn tại | |
| Tổ đội: tạo / mời / nhận / đuổi | 48; 49 (0 mời / 1 nhận); 50 (0 đuổi / 1 giải tán / 2 rời) — `real/Char.java:7822`, `real/MapXaHoi.java:270`, `real/Map.java:15067` | Đội/Bạn bè | `to_doi` KichBanDS4.java:170 (.tao :177, .moi_vao :184, .chia_exp :198, .duoi :202) | Kiểm (GM) | — | `.giai_tan` chỉ kiểm còn kết nối |
| Bang: tạo | -9; -10: short icon, UTF tên — `real/MapDoi.java:221/:1203` | — | `bang_hoi` KichBanDS4.java:211 (.tao :231, .csdl :233) | Kiểm (GM) | Tên trùng, thiếu xu: Thiếu ★ | |
| Bang: mời / nhận | -11 — `real/MapDoi.java:1054` | — | `bang_hoi.moi` :244 | Kiểm | — | |
| Bang: chat | -18 — `real/MapDoi.java:433` | — | `bang_hoi.chat` :249 | Kiểm | — | |
| Bang: góp quỹ | -20: int tiền → -32 popup 114 — `real/MapDoi.java:394`, `real/Map.java:9834` | — | `bang_hoi.quy` :259 | Kiểm (GM) | Góp đúp, số dư vừa đúng, tiêu xu giữa hai bước: Thiếu ★ | CÓ THỂ: quỹ được cộng nhiều hơn số đã trừ (`real/Map.java:9884-9913`) |
| Bang: rời / giải tán | -15 / -14 — `real/MapDoi.java:641/:500` | — | `.roi` :264, `.giai_tan` :268 (chỉ kiểm còn kết nối) | Chỉ chạy qua | — | |
| Bang: đuổi / khẩu hiệu / thông báo / xem / top / nhiệm vụ bang | -13, -16, -17, -12, -7, -8, -53, -37, -70 | — | — | Thiếu | — | |
| Hộp thư: đọc | 110: byte index — `real/Map.java:15404` | Thư/Quà → Hộp thư | `bat_thuong` `bt_thu.doc_thu` KichBanBatThuong.java:979 | Kiểm | Có (thư người khác ở id nhỏ phải còn) | **LỖI THẬT, ĐÃ SỬA 25/10/2026:** `delInboxMsg(index)` xoá dòng có id = chỉ số → thư người khác. Nay xoá theo `idMsg` + tên người nhận (`CharInboxMessage.idCanXoa`, `Database.delInboxMsg(id, charname)`); test `server/test/KiemDocThu.java` |
| Giftcode | -31 (actor,7,mã) — `real/RealController.java:1383` | Thư/Quà → Nhập giftcode | cn_dai_han `giftcode` KichBanGiftcode.java:31 (:60-:96); `qa_giftcode_tui_day` KichBanQaToanGame.java:392; HoiQuy `giftcode` KichBanHoiQuy2.java:141 | Kiểm | Túi đầy, mã sai / hết hạn / tắt / hết lượt, bấm nhanh: có | |
| Hộp may mắn | -62: byte kiểu, [byte hộp] — `real/MapQuaTang.java:591` | — | — | Thiếu | ★ mở đúp, túi đầy | |
| Xổ số | -63: byte số — `real/Map.java:8686` | — | — | Thiếu | — | |
| Vòng quay may mắn | 87: byte index — `real/Map.java:8678` | — | — | Thiếu | ★ quay đúp, túi đầy | |
| Quà hoạt động / online | -30 menu 100 — `real/RealController.java:2137`; Sổ tay | Sổ tay huấn luyện (mục Quà hoạt động) | `so_tay.nhan_qua_hoat_dong` KichBanDS4.java:298 (chỉ cần có phản hồi) | Chỉ chạy qua | ★ nhận 2 lần | |

## 10. Nhiệm vụ, Sổ tay, phó bản, Gym, PvP xếp hạng, danh hiệu, sự kiện

| Tính năng | Gói/cmd (server file:line) | Đường client | Nhóm bot / case (file:line) | Mức | Thao tác bất thường | Ghi chú |
|---|---|---|---|---|---|---|
| Nhiệm vụ chính | -64 (0 nhận / 1 trả / 2 huỷ) — `real/cmd/QuestHandler.java:16` | Nhiệm vụ của tôi | `nhiem_vu_chinh` KichBanDS4.java:374; cot_truyen KichBanHoiQuy.java:677 (.tien_trien :742); `tdnv_all` KichBanTienDoNhiemVu.java:183 | Kiểm | Trả đúp: chỉ chạy qua (BotHonLoan nhánh 7 :131) ★ | |
| Nhiệm vụ phụ | Sổ tay -527/10,12 | Sổ tay huấn luyện (mục Nhiệm vụ) | `nhiem_vu` KichBanDS4.java:323 (.3_khe_phu :334) | Kiểm | Nhận cái thứ 4: có | |
| Nhiệm vụ tuần | -527 | Sổ tay huấn luyện (mục Nhiệm vụ tuần) | `nhiem_vu.tuan_toi_da_2` :360 | Kiểm | Quá giới hạn: có | |
| Câu đố | 27 | — | `nhiem_vu.cau_do` :352 | Kiểm | — | |
| Cửa ải | -64, -32 95 | Nhiệm vụ → Thử thách cửa ải | `cua_ai` KichBanHoiQuy.java:398 (.chua_nhan_khong_tinh :415); `tdnv_sai_quai` KichBanTienDoNhiemVu.java:219-247 | Kiểm | Huỷ nhiệm vụ cửa ải: có (.khong_huy :247) | |
| Huỷ nhiệm vụ | -64 kiểu 2; 52 (client cũ) — `real/Char.java:8195` | — | `KichBanNhiemVu` nv_huy (ngoài cổng) | Thiếu* | ★ | |
| Nói chuyện NPC kiểu cũ | 53 / 55 — `real/Char.java:8644/:8501` | — | — | Thiếu | — | |
| Lưu tiến độ khi đăng nhập lại | -64 (1,-1,0) | — | `cap1_40_luu` KichBanCap140.java:50 | Kiểm | Rớt mạng: có | |
| Bất biến nhiệm vụ | — | — | BatBien NV_LAM_DUOC/NV_SO_LUONG/NV_BAN_DO `BatBien.java:283-303`; `nv_muc_tieu` KichBanHoiQuy.java:455 | Kiểm (chỉ HoiQuy) | — | |
| Sổ tay (mở các mục) | 23(-90) → -527/0 | Menu → Sổ tay | `so_tay` KichBanDS4.java:275 (.mo :278, mục :285, .tinh_thong :292) | Kiểm | Mở 2 lần liên tiếp: chỉ chạy qua (BotHonLoan nhánh 4) | |
| Sư phạm / lịch đăng nhập / vé chương | -527 | Sổ tay | `so_tay.{su_pham,dang_nhap}` :285 (chỉ kiểm có chữ) | Chỉ chạy qua | ★ nhận đúp | |
| Hầm ngục | -527/3 | Sổ tay huấn luyện (mục Hầm ngục) | `ham_nguc` KichBanDS4.java:418 (.vao :426; .thuong chỉ cần có phản hồi :442; .csdl luôn đúng :444) | Kiểm (yếu) | Nhận thưởng 2 lần: Thiếu ★ | |
| Phó bản | -527/60,61 | Sổ tay | pho_ban_cup `pb_vao`/`pb_ket_noi`/`pb_roi`/`pb_luot`/`pb_hai_phong` KichBanPhoBanCup.java:107-209 | Kiểm (GM) | Rớt mạng, vào lượt thứ 4: có | |
| Cúp PvP tuần | -527/70 | Sổ tay huấn luyện (mục Cúp PvP) | `cup_vong` KichBanPhoBanCup.java:277 | Kiểm | Đánh trong 5 giây chuẩn bị: chỉ chạy qua (:400) | |
| Gym | -527/5 | Quầy → Thách đấu Gym | cn_dai_han `gym` KichBanDS4.java:449; gym_lang KichBanGymLang.java:170/189; HoiQuy `gym` KichBanHoiQuy.java:502 (.mot_boss :532) | Kiểm | Bấm 3 lần: có. Thoát khi boss còn: ngoài cổng (gl_mot_tran :308) | |
| Chiếm thành / chiếm Gym | -31 "setting", GM tgct — `real/ChiemGymLauDai.java` | — | `chiem_thanh` KichBanDS5.java:152 (.bat_dau :187; .ket_thuc chỉ kiểm còn kết nối) | Kiểm (GM) | Người chơi thường đăng ký / tham gia / nhận thưởng: Thiếu ★ | |
| Thuế thành / thẻ thành | -71 — `real/Map.java:15088`; -25 — `real/Map.java:15175` | — | — | Thiếu | ★ thuế âm | |
| Đấu trường 3v3 | -109 — `real/DauTruongGame.java:338` | NPC Đấu trường | dau_truong `dt_tran` KichBanDauTruong.java:153 (.luong_khoa_vao_vi đúng :262, .het_luot_ngay :283) | Kiểm (GM) | Hết lượt: có | |
| Xếp hạng PvP mùa | -30 "Đấu xếp hạng" | NPC | xep_hang `xh_ghep`/`xh_roi`/`xh_mua` KichBanXepHang.java:59/102/129 | Kiểm | Không phát thưởng lại: có | |
| Bảng xếp hạng (xem top) | -19 — `real/Map.java:15243` | Menu → Xếp hạng | — | Thiếu | — | |
| Danh hiệu | -111 (0 bảng / 1 đeo / 2 tháo / 3 đã xem) — `real/DanhHieuGame.java:888` | Nhân vật → Danh hiệu | danh_hieu `dh_cap`/`dh_bang`/`dh_deo`/`dh_nguoi`/`dh_luu` KichBanDanhHieu.java:145-234 | Kiểm (GM) | Đeo danh hiệu chưa có: có | `dh_cho` :252 không chạy |
| Sự kiện (NPC sự kiện) | -30 với -49 — `real/SuKienNpc.java:115`, `real/RealController.java:1297` | Menu → Sự kiện | `su_kien` KichBanDS5.java:144 (chỉ đọc bảng SQL); `npc_su_kien` KichBanNPC (ngoài cổng) | Thiếu* | ★ đổi quà đúp | |
| Năng lượng / tinh thông | -527 | Sổ tay | `so_tay.tinh_thong` :292 | Kiểm | — | |
| Quầy đổi thưởng / Pokédex đổi thưởng | 23(-90) → -30 | Quầy đổi thưởng | `pokedex_menu` KichBanDS4.java:573 (:579-:587) | Kiểm | — | |
| Mở mọi NPC / mọi mục menu | 23, -30 | — | `KichBanNPC` npc_tat_ca (ngoài cổng); `?cap1_40` quetNpc/quetMenu KichBanCap140TinhNang.java:60/104 (chỉ đêm) | Thiếu* | — | Lượt quét bỏ qua mọi mục tiêu tiền (NGUY_HIEM :23) |
| Tải nhẹ (10 bot, 30 giây) | 4, 27 | — | bang_thong BotBangThong | Chỉ chạy qua | — | Chỉ kiểm "vào game N/N, loi=0" |

---

## Nhóm `bat_thuong` (mới, 25/10/2026)

Lớp `tools/pokemon/kiem_tai/KichBanBatThuong.java`, dòng `bat_thuong` trong `hoi_quy.conf` (kiểu `cn`, giới hạn 1200 giây,
chạy thật trên stack cục bộ: **≈12 phút**, 53 PASS / 0 FAIL / 2 BỎ QUA).

- **Người chơi thường** `kt101..` (không GM, nhân vật mới mỗi lần). GM `kt0xx` CHỈ chuẩn bị cảnh bằng lệnh nhắm tên (`smn` đặt xu chính xác, `spt` thuốc, `gem` đá, `slv` cấp cho chống clone, `die`).
- **Bất biến, không so chữ thông báo:** tiền / thuốc / số món bảo toàn (đối chiếu `tob_char.gold`, `tob_char.potion`, số bản ghi vũ khí trong `inven` + `equip` sau khi thoát), không âm, không kẹt trạng thái (đăng nhập lại, vào map hợp lệ, hp > 0), máy chủ còn trả lời (một người chơi khác đăng nhập + làm mới), **không Exception mới trong log** (`-Dlog`, mỗi case một `.log_sach`).
- **Chờ trạng thái, không chờ giờ:** `onDinh` gửi 88 rồi đợi gói 16 (88 chạy cùng luồng phiên với 24/28/29/22) tới khi xu + thuốc + túi đứng yên hai lần; mọi chờ có hạn 5-15 giây.
- Exception do **gói sai cố ý** (`bt_mua`, `bt_goi_sai`) ghi `.log_goi_sai` = BỎ QUA kèm dòng log (máy chủ đã bắt, trạng thái kiểm riêng) — xem "Còn mở" dưới.

| Case | Thao tác bất thường | Bất biến | file:line | Mức |
|---|---|---|---|---|
| `bt_mua` | Giá 1 bình; 14 gói mua liền với số dư vừa đủ 10 bình; thiếu 1 xu; 11 gói 24 sai (SL 0/-5/-32768, id -1/30000, loại 7, kind 99, món không bán, n=-1, cụt); SL 32767 | Đúng 10 bình + xu = 0; không cấp/không trừ; tối đa 999 và trừ đúng giá × số bình; CSDL = trong game | KichBanBatThuong.java:247 | Kiểm |
| `bt_ban` | Bán đúp ×8 cùng id; bán đồ đang mặc ×3; mặc/tháo liên tục ×9; bán/dùng id lạ, gói cụt | Cộng tiền đúng 1 lần; không bán được đồ đang mặc; số vũ khí (túi + mặc, CSDL) = khởi đầu + 1 | KichBanBatThuong.java:319 | Kiểm |
| `bt_tui` | Mua lô 20 vũ khí tới khi túi đầy; mua thêm đồ + thuốc loại mới; tháo đồ đang mặc khi đầy | Không trừ xu, không thêm món; món đang mặc không biến mất; CSDL đủ số món | KichBanBatThuong.java:375 | Kiểm |
| `bt_dut` | Rớt mạng ngay sau gói mua (×4, có lần gửi 2 gói), sau gói bán, sau gói qua cổng; spam 8 gói cổng; cổng giả tới map 50 | Xu trừ = 100 × số bình nhận; món mất ⇔ xu cộng; vào lại map hợp lệ; không tới map 50 | KichBanBatThuong.java:433 | Kiểm |
| `bt_dang_nhap` | 8 lần đăng nhập rồi đóng (có lần chọn nhân vật dở), hai phiên cùng tài khoản | Xu/thuốc giữ nguyên (CSDL); chỉ một phiên sống; chơi tiếp được | KichBanBatThuong.java:521 | Kiểm |
| `bt_ket_ban` | 101 ok=1 không có lời mời ×3; đồng ý lặp | Không bị ép kết bạn; không thêm trùng (**lỗi thật, đã sửa**) | KichBanBatThuong.java:757 | Kiểm |
| `bt_gd` | Rút thuốc sau khi đối phương đã khoá + xác nhận (**lỗi thật, đã sửa**); xác nhận đúp ×5 hai bên; rớt mạng khi đã khoá + xác nhận | Bên khoá không bị lừa; chuyển đúng 1 lần; tổng thuốc bảo toàn; không kẹt trạng thái giao dịch; CSDL | KichBanBatThuong.java:640 | Kiểm |
| `bt_nc` | Thêm đá luyện kim rồi BÁN đá, bấm Đập; Đập ×6 liền; rớt mạng ngay sau Đập | Không lên cấp, không trừ xu khi thiếu đá; trừ xu ⇔ mất đá; CSDL | KichBanBatThuong.java:784 | Kiểm |
| `bt_guc` | Khi gục: mua, uống thuốc, dùng id lạ, đổi khu; gửi 31 khi đang sống; rớt mạng khi gục | Không hồi máu, không lợi tiền; 31 khi sống không dịch chuyển; vào lại hồi sinh được | KichBanBatThuong.java:863 | Kiểm |
| `bt_goi_sai` | 136 gói sai / cụt / ngẫu nhiên (hạt giống cố định) trên 34 lệnh (22, 24, 28, 29, 61, 66, 68, 69, 71, 74-78, 85, 92, 94, 95, 108, 110, -21, -23, -30, -34..-36, -45, -52, -62, -68, -69, -107, -119, -120) | Xu / lượng / lượng khoá / món / ngọc / thuốc không tăng, xu không âm (CSDL), máy chủ còn trả lời | KichBanBatThuong.java:918 | Kiểm |
| `bt_thu` | Đọc 4 thư của mình (110 chỉ số 0..3) khi người khác có thư chờ ở id nhỏ | Thư người khác còn nguyên (**lỗi thật, đã sửa**) | KichBanBatThuong.java:979 | Kiểm |

Ba lỗi thật bot tìm ra đều có kiểm hồi quy `server/test` (đăng ký trong `Kiem.java`): `KiemDocThu`, `KiemGiaoDichRutDo`,
`KiemKetBan`. Chạy nhóm trên `htbb-server.jar` CŨ: `bt_gd` + `bt_thu` cho 4 FAIL đúng ba lỗi; trên bản đã sửa: 0 FAIL.

**Còn mở (chưa sửa, ghi nhận):** gói sai làm máy chủ in Exception (đã bắt, không đổi trạng thái): `OfflineMap.doBuyItem`
với `kind` ngoài bảng (`ArrayIndexOutOfBoundsException: 99`) và gói cụt (`EOFException`); `doBuySpecialItem` id lạ
(`NullPointerException ... "sell"`); EOF ở nhiều lệnh khác khi gói cụt. Không phải lỗi kinh tế; nên kiểm biên trước khi
đọc / tra bảng để log không bị gói rác làm đầy.

## Lỗ hổng ưu tiên

Xếp theo rủi ro kinh tế: nhân bản, mất tiền, tiền được cộng sai. Mọi mục dưới đây là **Thiếu** hoặc **chỉ kiểm bằng GM / chỉ kiểm phía client**.

1. ~~**Giao dịch rút thuốc sau khi đối phương khoá**~~ — **ĐÃ XÁC NHẬN + SỬA** (bot `bt_gd.doi_sau_khi_khoa`, `GiaoDichThuoc.rutDoDuoc`).
2. **Chợ: mua trong lúc người mua đang tiêu xu ở luồng khác.** Mã ở `real/OfflineMap.java:318-426`. Người bán được cộng trước, người mua bị trừ sau, mà subXu kẹp về 0. Kiểm hiện có ở `cho` chỉ là "xu giảm" bằng GM.
3. **Góp quỹ bang khi số dư đổi giữa hai bước.** Mã ở `real/MapDoi.java:394` và `real/Map.java:9834-9913`. Quỹ có thể được cộng nhiều hơn số đã trừ.
4. **Nâng cấp: ngọc đã thêm (76) rồi bán (78) hoặc vứt (-23) trước khi gửi 77.** Nhánh dài hạn: bot `bt_nc.ban_da_giua_chung` xác nhận an toàn (bán). Còn: vứt (-23), và nhánh cũ khi LauDai tắt (`real/MapEpNgoc.java:726/:752` không đếm lại đá).
5. **Ghép ngọc (-36) và chế tạo cũ (-52) trừ xu SAU khi đã trao kết quả.** Mã ở `real/MapEpNgoc.java:979` và `real/MapCheTao.java:434`. Gửi song song với một lệnh tiêu xu khác thì nhận được đồ miễn phí.
6. **Bán thú cho NPC (-800/-801) bỏ qua khoá thú và trạng thái giao dịch.** Mã ở `real/RealController.java:1074` và `real/MapDoMenu.java:673,689`.
7. **Trao đổi Pokémon (-121/6)** chưa có kịch bản nào. Cần thử: đặt, khoá, đổi lại; xác nhận đúp; rớt mạng.
8. **Chợ Pokémon (-121/7):** mua đúp, mua tin vừa huỷ, hai người mua cùng tin.
9. **Kho đồ 68/69 chạy trên hai luồng khác nhau, và 69 không có checkTrade.** Mã ở `real/OfflineMap.java:560/599`. Cần thử gửi và lấy dồn dập, lấy khi túi đầy.
10. **Bán đồ (28):** bán đúp cùng id + đồ đang mặc đã có bot (`bt_ban`). Còn: bán đồ đang đặt chợ hoặc đang trong ô nâng cấp, bán đồ khoá. Mã ở `real/OfflineMap.java:1242`.
11. **Phân rã (-68/4) và tách nguyên liệu (-68/0)** trên đồ đang đặt chợ hoặc đang giao dịch. Phân rã trừ xu sau khi xoá đồ. Mã ở `real/OfflineMap.java:1967` và `real/MapEpNgoc.java:227`.
12. **Cửa hàng VIP / Trung thu (-76):** trừ tiền rồi không trao đồ, hoặc chặn hạn mức sau khi đã trừ. Mã ở `real/Map.java:8218-8306`.
13. **Số lượng âm / 0 / tràn:** 24 đã có bot (`bt_mua.goi_sai`, `.so_luong_lon`); các lệnh khác chỉ qua gói ngẫu nhiên `bt_goi_sai` (bất biến không tăng tài sản). Còn thiếu phép thử có chủ đích cho -21, 78, -23, 66/2/1, 74 (n âm), -31 nến.
14. **Số dư vừa đúng bằng giá:** 24 đã có (`bt_mua.bam_lien_vua_du`, `.thieu_mot_xu`). Còn 85, 95, -20.
15. ~~**Hộp thư (110) xoá nhầm thư người khác.**~~ — **ĐÃ XÁC NHẬN + SỬA** (bot `bt_thu.doc_thu`, `CharInboxMessage.idCanXoa`).
16. ~~**Kết bạn ép** (101 ok=1 khi không có lời mời)~~ — **ĐÃ XÁC NHẬN + SỬA** (bot `bt_ket_ban`, `LoiMoiKetBan`).
17. **Đổi khu (-30/-34):** khu sai, gửi trong 20 giây hồi, khi đang đánh, chết hoặc giao dịch. Mã ở `real/MapDoMenu.java:1413`.
18. **Mở rộng túi (-107) và vé ô thú** khi bấm đúp hoặc thiếu xu. Hộp may mắn (-62) và vòng quay (87) khi túi đầy hoặc bấm đúp.
19. **Thao tác khi đang chết hoặc đang trong trận PvP / đấu trường:** khi gục đã có bot (`bt_guc`: mua, uống, đổi khu, 31). Còn: trong trận PvP / đấu trường; 85, 66, vé 22/162 khi gục.
20. **Rớt mạng giữa:** 77 (`bt_nc.rut_mang`), 66/5 (`bt_gd.rut_mang`), 24/28/12 (`bt_dut`) đã có bot. Còn: 95 + -32 (mua chợ), -119/1 (thả thú).
