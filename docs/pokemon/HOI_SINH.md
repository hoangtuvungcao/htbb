# Gục và hồi sinh (18/10/2026)

Trước đây hồi sinh gần như miễn phí: dưới cấp 50 tự hồi sinh tại chỗ sau vài giây ở mọi bãi luyện
(`Char.doSetTimeAutoHoiSinh`), "Auto về Map Up" đưa về làng rồi tự bay lại chỗ cũ, phí xu tại chỗ chỉ 5% giờ thu
nhập — treo máy đánh mãi không dừng. Nay khi Pokémon gục, người chơi **chọn** trong hộp thoại "Pokémon của bạn đã gục!".

Mã: `src/com/htbb/pokemon/HoiSinh.java` (thuần: phí, phạt, lựa chọn, vùng), `src/real/HoiSinhGame.java` (nối vào
game), client `htbb-client/j2me/src/game/pokemon/HopHoiSinh.java` (jar / APK / PC Java dùng chung). Test
`test/KiemHoiSinh.java`, bot `tools/pokemon/kiem_tai/KichBanHoiSinh.java`. Cấu hình: nhóm **Hồi sinh**
(`hoi_sinh.*`) trong `tools/cauhinh/danh_muc.json`, áp dụng ngay.

## 1. Lựa chọn

| Lựa chọn | Giá | Ghi chú |
|---|---|---|
| Hồi sinh tại chỗ (tân thủ) | miễn phí, 50% HP | chỉ dưới cấp `tan_thu_cap` (15), hồi chiêu `tan_thu_hoi_chieu_giay` (30 s) |
| Dùng Thuốc hồi sinh | 1 thuốc (potions 119), 30% HP | Sổ tay → Cửa hàng, dòng "Thuốc hồi sinh (Chansey) - N xu/viên" (giá tính cho chính người xem), hộp xác nhận "Mua 1 / 5 / 10: trả X xu" trừ đúng số đã hiện; giá theo cấp: `gia_thuoc_phan_tram_gio` (30%) giờ thu nhập, tối thiểu `gia_thuoc_toi_thieu` (407) |
| Dùng Thuốc hồi sinh tối đa | 1 vật phẩm 1207, 100% HP | Kecleon 5 lượng khoá, 3/tuần |
| Trả xu | `xu_phan_tram_gio`% (40%) giờ thu nhập ở cấp đó × hệ số gục lặp, tối thiểu `xu_toi_thieu`; 50% HP | hố hút xu |
| Trả lượng khoá | `luong_khoa` (2) × hệ số, trần `luong_khoa_toi_da` (10); 50% HP | 0 = tắt |
| Về Trung tâm Pokémon | miễn phí | phạt nhẹ (mục 2) |
| Chờ đồng đội hồi sinh | thuốc của đồng đội | đồng đội ở gần mang thuốc nhận menu cứu, `dong_doi_giay` (60 s) |

- **Hệ số gục lặp** `he_so_lap` = 100,150,200,300 (%): lần gục 1, 2, 3, 4+ trong `cua_so_phut` (30) phút.
  Lịch sử giữ theo nhân vật trên máy chủ (đăng nhập lại không xoá).
- **Chờ + hồi chiêu**: tại chỗ chỉ bấm được sau `cho_sau_guc_giay` (5 s) và cách lần tại chỗ trước
  `hoi_chieu_giay` (60 s). Hộp thoại đếm ngược; máy chủ kiểm lại mọi điều kiện.
- **Cấm tại chỗ** (chỉ còn Về Trung tâm / Chờ đồng đội): sàn đấu, giải Liên minh, chiến trường, lãnh địa đội, chiếm
  Gym đang đánh, đấu trường (`map_pvp`) — `cam_pvp`; đang thách đấu Gym — `cam_gym`; đã đánh boss thế giới / Gym
  còn sống cùng bản đồ — `cam_boss`; Mê cung — `cam_me_cung`; thêm `map_cam_them`.
- Bản đồ có luật riêng giữ nguyên: chiến trường liên vùng (tự hồi sinh theo đội), sàn đấu, Núi châu báu,
  đấu trường 2 (map 204), bản đồ sự kiện "hoá quái".
- Dùng Thuốc hồi sinh tối đa từ túi khi gục cũng đi qua cùng luật cấm / chờ / hồi chiêu.
- **Trần vật phẩm**: tối đa `thuoc_moi_gio` (6) lần hồi sinh tại chỗ bằng vật phẩm của chính mình / 60 phút (tay, Tự động
  đánh, dùng từ túi); hết lượt thì chỉ còn trả xu / lượng khoá / về Trung tâm. Đồng đội cứu không tính.

## 2. Về Trung tâm Pokémon

- **Vùng**: đang ở Hoenn → Trung tâm (Chansey) của thị trấn Hoenn gần nhất theo số lần qua cổng, đứng trước NPC
  trên ô đi được (trước đây gục ở Hoenn bị đưa về làng nhà ở Kanto). Kanto / Johto (thế giới cũ) → làng nhà của
  phe như cũ (mỗi làng có Trung tâm Pokémon).
- **Phạt** (không áp tân thủ): mất `exp_mat_phan_nghin` (20 = 2%) EXP lên cấp hiện tại, trần
  `exp_mat_toi_da_phut` (15) phút cày ở cấp đó, và **không bao giờ tụt cấp** (chỉ trừ trong tiến độ cấp); mất
  `do_ben_mat_phan_tram` (5%) độ bền tối đa của mỗi trang bị đang mặc (không làm vỡ đồ); **Mệt mỏi**
  `met_moi_phut` (5) phút: −`met_moi_exp` (20)% EXP hạ quái, −`met_moi_tan_cong` (10)% tấn công (đếm ngược -75
  id 47). Không rơi đồ.

## 3. Tự động đánh

Gục khi đang Tự động đánh (`real.TuDongGame`): nếu người chơi bật "Tự hồi sinh", có Thuốc hồi sinh / tối đa, bản
đồ không cấm tại chỗ và còn lượt `tu_dong.hoi_sinh_moi_gio` (2/giờ) → máy chủ tự dùng thuốc sau thời gian chờ,
tự động chạy tiếp. Còn lại → dừng Tự động đánh (`TuDongGame.dungKhiChet`) và sau 3 giây tự về Trung tâm (có phạt)
rồi **dừng** — không treo máy vô hạn. "Auto về Map Up" không còn bay lại bãi luyện sau khi gục
(`hoi_sinh.auto_ve_map_up` = 0).

## 4. Giao thức

Lệnh -101 (hai chiều). Server → client: byte 0 MỞ {UTF tiêu đề, UTF mô tả, byte n, n × {byte mã, UTF tên, UTF chi
tiết, byte bấm được, UTF lý do khoá, short giây chờ}, short giây chờ đồng đội}; byte 1 ĐÓNG. Client → server:
byte 0 XIN, byte 1 CHỌN + byte mã. Mã: 0 tân thủ, 1 Thuốc hồi sinh, 2 tối đa, 3 xu, 4 lượng khoá, 5 Trung tâm,
6 chờ đồng đội. Client mới gửi XIN khi thấy mình gục; client cũ (không gửi -101) nhận menu -30 id -803 với cùng
lựa chọn (chữ kèm phí / lý do khoá); nút "Hồi sinh" và popup cũ cũng mở menu này. Đồng đội cứu: menu -804.

## 5. Chỉ số

`trang_thai.json` → `hoiSinh` (cộng dồn từ lúc chạy): `guc`, nguồn `tanThu / thuoc / thuocToiDa / xu / luongKhoa /
trungTam / dongDoi / tuDongThuoc / tuDongVe / khac`, `xuDot`, `luongKhoaDot`, `expMat`. Xu đốt vào `xu_ra` của
`tob_kinh_te_ngay` (`LauDai.ghiXuRa`) và `tob_log_use_luong*` ("hoi sinh tai cho"). Nhật ký kiểm toán loại
`HOI_SINH`.

## 6. Mô phỏng kinh tế

Mô-đun `HoiSinhMoPhong` cuối `tools/pokemon/MoPhongKinhTe.java` (giá trị kỳ vọng, không rút RNG): 0,15–0,25 lần
gục / giờ cày theo nhóm; 45% Thuốc hồi sinh, 15% xu, 5% lượng khoá, 35% về Trung tâm. Giá thuốc theo `HoiSinh.giaThuoc` (theo cấp). Kết quả 730 ngày: hố hút `hoi_sinh` 6,2% tổng xu ra (trước khi tăng giá thuốc: 2,2%); mục tiêu vẫn 17/17 + 6/6 (nhẹ tới cấp tối đa tháng 17,3, trước 17,1).

## 7. Còn lại

- Giá thuốc do MÁY CHỦ ghi vào menu (mọi client jar / Android / PC / Unity hiện đúng, không cần sửa client). Cửa sổ
  thuốc NPC kiểu cũ (giá lấy từ bảng client) KHÔNG bán Thuốc hồi sinh (gói mua 119 bị từ chối kèm hướng dẫn). Nếu
  đổi cấp giữa lúc xem và lúc bấm, máy chủ hiện lại hộp với giá mới thay vì trừ khác giá đã hiện.
  Thuốc hồi sinh tối đa giữ 5 lượng khoá (3/tuần) và chịu trần / giờ.
- Client PC Unity (`htbb-client/pc`) chưa có hộp thoại -101: dùng menu -803.
