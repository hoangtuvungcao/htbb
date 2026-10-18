# Tự động đánh chính thức (máy chủ điều khiển)

Quyết định 18/10/2026: bỏ mod NQSH phía client (Auto up, Tự PK / Mã PK / Nhảy vùng, Vận may, Lên thành, Giao hàng
tự động, Chế đồ nhanh, Dòng ẩn, Xa phu từ xa, tự đăng nhập lại, tự mua, tầm chiêu 300, về làng không chờ). Thay bằng
MỘT tính năng **Tự động đánh** do máy chủ quyết định. Vé treo máy offline ("Phiếu ủy thác luyện tập",
`TangToc.UY_THAC`) giữ nguyên giá trị: Tự động đánh chỉ chạy khi **đang online** và chịu đúng luật năng lượng.

| Phần | Tệp |
|---|---|
| Logic thuần (giới hạn, điều kiện, nhịp, hồ sơ) | `src/com/htbb/pokemon/TuDong.java` |
| Nối vào game (gói -100, hook, kiểm toán, chỉ số) | `src/real/TuDongGame.java` |
| Kiểm thử | `test/KiemTuDong.java` (bộ `Kiem`) |
| Client jar / APK | `htbb-client/shared/mod/.../TuDong.java` (động cơ + mạng), `j2me/src/.../BangTuDong.java` (bảng) |
| Client PC | `htbb-client/pc/Assets/Scripts/Assembly-CSharp/Mod/TuDong.cs`, `BangTuDong.cs` |

## 1. Nguyên tắc

1. **Không thưởng thêm.** EXP / xu / đồ rơi khi tự động đi qua đúng đường hạ quái thường, nên chịu nguyên năng lượng
   huấn luyện (`NangLuong`: 100% → 50% → 20% → 0%). `TuDong` / `TuDongGame` không có hàm nào chạm EXP (test bắt).
2. **Trần giờ mỗi ngày**: `tu_dong.gio_moi_ngay` (mặc định 5 giờ) + `tu_dong.gio_them_the_thang` (mặc định 2 giờ
   khi thẻ tháng còn hạn). Chỉ tính lúc đang bật; khoảng không có nhịp > 90 giây chỉ tính 90 giây. Lưu trong hồ sơ dài
   hạn (`HoSoLauDai` khoá `a1` = ngày, `a2` = ms đã dùng); đăng nhập lại không reset, phiên mới luôn TẮT.
3. **Máy chủ kiểm khi bật**: chỉ bãi luyện tập có Pokémon hoang dã (`tu_dong.chi_map_da_mo`; không thành phố, nhà,
   sàn đấu, sân Liên minh, chiến trường, khu Chiếm Gym), không đang cờ PK, không gục, còn giờ.
4. **Máy chủ dừng khi**: hết giờ, đổi map, ra khỏi bán kính (điểm bật + `tu_dong.ban_kinh` + 64 px), gục (xem mục 4),
   và nếu `tu_dong.tat_khi_pvp=1`: bị Pokémon khác tấn công, tự đánh người, bật PK, vào sàn đấu / Gym / chiến
   trường, gây sát thương vào boss (gồm boss Gym, boss thế giới) - người chơi tự điều khiển phần đó.
5. **Chống bot**: lúc đang tự động, nhịp đánh (lệnh 9 / 106) `tu_dong.nhip_danh`, uống thuốc (22)
   `tu_dong.nhip_thuoc`, nhặt (18 / 19) `tu_dong.nhip_nhat` theo cửa sổ trượt; vượt thì BỎ lệnh và đếm vi phạm,
   đủ `tu_dong.vi_pham_tat` thì dừng + ghi `hack.txt` (`TU_DONG_NHIP`). Mọi người chơi (kể cả không tự động): nhịp
   đánh trên `tu_dong.nhip_nguoi` chỉ ghi nghi vấn `NHIP_DANH` (≤ 1 dòng / phút / Pokémon), KHÔNG chặn. Chống ngập gói
   tin (`io.BoDemGoiTin`) giữ nguyên.
6. **Kiểm toán + chỉ số**: `Nhat.kiemToan("TU_DONG", ...)` mỗi lần bật / tắt (lý do, phút, số đòn / thuốc / nhặt / bị
   chặn, phút đã dùng hôm nay) và mỗi lần tự hồi sinh. `trang_thai.json` có
   `"tuDong":{"dangChay","bat","tuChoi","chan","tatViPham","nghiVanNhip"}`.

## 2. Gói -100

Client → server (luồng bản đồ, `MapXuLyLenh`): `byte loai` = 0 xin trạng thái / 1 bật (`short` bán kính xin px,
`byte` tự hồi sinh 0/1) / 2 tắt / 3 nhịp (client gửi 20 giây một lần).

Server → client: `byte 0`, `byte bật`, `int giây còn lại hôm nay`, `int giây tối đa hôm nay`, `short bán kính`
(đang chạy = bán kính phiên, không thì tối đa), `byte cờ` (1 tính năng bật, 2 cho tự uống thuốc, 4 dừng khi PvP,
8 chỉ bãi luyện tập), `UTF thông báo` (lý do dừng / từ chối, rỗng = không). Server cũ không trả lời → client không
bao giờ chạy.

## 3. Cấu hình trung tâm (nhóm `tu_dong`, áp dụng ngay)

| Khoá | Mặc định | Ý nghĩa |
|---|---|---|
| `tu_dong.bat` | 1 | 0 = tạm khoá (người đang dùng dừng ở lần kiểm tiếp) |
| `tu_dong.gio_moi_ngay` | 5 | giờ / ngày, 0..24 |
| `tu_dong.gio_them_the_thang` | 2 | cộng thêm khi có thẻ tháng |
| `tu_dong.chi_map_da_mo` | 1 | chỉ bãi luyện tập |
| `tu_dong.tat_khi_pvp` | 1 | dừng khi PvP / sàn đấu / Gym / boss |
| `tu_dong.tu_dung_thuoc` | 1 | 0 = client không tự uống thuốc |
| `tu_dong.ban_kinh` | 240 | px, 48..960 (1 ô = 16 px) |
| `tu_dong.nhip_danh` / `nhip_thuoc` / `nhip_nhat` | 12/5, 4/5, 8/5 | số lệnh / giây khi tự động |
| `tu_dong.nhip_nguoi` | 30/5 | ngưỡng ghi nghi vấn cho mọi người chơi |
| `tu_dong.vi_pham_tat` | 20 | số lần vượt nhịp thì dừng |
| `tu_dong.hoi_sinh_moi_gio` | 2 | tối đa số lần tự dùng vật phẩm hồi sinh / 60 phút (0 = không) |

Sửa `tools/cauhinh/danh_muc.json` rồi chạy `python3 tools/cauhinh/sinh_cau_hinh.py`.

## 4. Gục và hồi sinh (hook cho hệ thống chết / hồi sinh)

Gục khi đang tự động thì **tự động dừng**, trừ khi người chơi bật "Tự hồi sinh" trong bảng (mặc định tắt):

- Hệ thống gục / hồi sinh gọi `real.TuDongGame.choHoiSinhTuDong(Char p)` lúc Pokémon gục:
  - `true` = đang tự động, người chơi đã bật "Tự hồi sinh", còn lượt (`tu_dong.hoi_sinh_moi_gio` lần / 60 phút, cửa
    sổ trượt, đã ghi nhận một lượt): dùng NGAY một vật phẩm hồi sinh tại chỗ theo luật thường của hệ thống hồi sinh
    (không có / không đủ vật phẩm thì coi như `false`), tự động chạy tiếp.
  - `false` = gọi `real.TuDongGame.dungKhiChet(Char p)` (dừng, lý do `TAT_CHET`, báo client) rồi về Trung tâm Pokémon
    như thường.
- `real.TuDongGame.dangTuDong(Char p)`: tra cứu (vd không bật hộp thoại chọn cách hồi sinh khi đang tự động).
- Chưa có hệ thống nào gọi hook trong 30 giây sau khi gục (`CHO_HOI_SINH_MS`) thì máy chủ tự dừng ở nhịp kế; người
  chơi không bật "Tự hồi sinh" thì dừng ngay ở nhịp kế. Client cũng tự dừng (chờ tối đa 35 giây nếu bật tự hồi sinh).

## 5. Client

Bảng **Tự động đánh** (kiểu Pokémon): Bắt đầu / Dừng; thứ tự 4 chiêu (trống = mọi chiêu đánh đã học, mạnh trước);
thuốc HP < 30/50/70% và MP < 20/30/50% (hoặc tắt); nhặt đồ (không / thuốc + xu / + đá / tất cả); về hồi phục (hết
thuốc mà HP thấp: về điểm bắt đầu đứng hồi tới 80%); vùng đánh 4..20 ô (không quá `tu_dong.ban_kinh`); tự hồi sinh.
Dòng trên cùng là giờ còn lại hôm nay do máy chủ báo. Mọi thao tác đi qua đúng hàm của game như bấm tay (tự đi tới
tầm, kiểm MP / hồi chiêu, có hoạt ảnh); không lướt, không bỏ hoạt ảnh. Người chơi bấm phím di chuyển / chạm màn thì
tự động nhường 3 giây; hộp thoại / menu đang mở thì tạm dừng.

Mở từ: Menu nhanh (Poké Ball) → "Tự động đánh", phím # → "Tự động đánh", menu chính → hàng đầu, hoặc bảng Auto gốc
của game (giờ cũng mở bảng này). PC: tương tự (`BangTuDong.cs`).
