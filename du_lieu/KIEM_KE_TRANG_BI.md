# Kiểm kê trang bị, bộ và icon (đồ họ mới + trang phục, 0.9.8)

> **Tệp sinh** bởi `tools/do_trang_phuc/kiem_ke.py` - đừng sửa tay. Danh mục đồ mới: `tools/do_trang_phuc/danh_muc.py`.

## 1. Hiện trạng trước khi thêm (data_item trong `sql/htbb_day_du.sql`)

- 729 mẫu trang bị nhân vật (type 0..12, 19) trong `data_item`; 154 dòng "chuẩn" ô × bậc ở `noi_dung_trang_bi.csv` (14 ô × 11 bậc, đồ cầm tay tách 5 dòng Pokémon). Độ hiếm là thuộc tính của **từng món** (`colorName`), không phải của mẫu: mọi ô × bậc đều ra được Thường / Hiếm / Quý / Sử thi (Huyền thoại = Sử thi + 3 lỗ từ chế tạo).
- **Trùng tên**: 124 tên dùng cho nhiều mẫu (di sản kiếm hiệp chia mẫu theo giới tính / style / cấp 1-4-9), tổng 604 mẫu. Ví dụ: "Khăn quàng bạc" ×8, "Khăn quàng vàng" ×8, "Khăn quàng bạch kim" ×8, "Khăn quàng kim cương" ×8, "Đai bạc" ×8, "Đai vàng" ×8.
- **Icon dùng chung giữa các bậc** (xstart giống nhau cho cả 11 bậc của một ô):

| Ô | Số mẫu | Số icon khác nhau | Icon (xstart) |
|---|---|---|---|
| Khăn quàng | 136 | 1 | 14000×136 |
| Đai | 73 | 1 | 14001×73 |
| Băng đô | 66 | 1 | 14002×66 |
| Đồ cầm tay Charmander | 50 | 1 | 14003×50 |
| Đồ cầm tay Bulbasaur | 53 | 1 | 14004×53 |
| Đồ cầm tay Eevee | 51 | 1 | 14005×51 |
| Đồ cầm tay Squirtle | 50 | 1 | 14006×50 |
| Đồ cầm tay Pichu | 50 | 1 | 14007×50 |
| Huy hiệu | 34 | 1 | 14008×34 |
| Vòng cổ | 34 | 1 | 14009×34 |
| Lông vũ | 34 | 1 | 14010×34 |
| Găng | 34 | 1 | 14011×34 |
| Phiến đá hệ | 34 | 1 | 14012×34 |
| Ruy băng | 30 | 1 | 14013×30 |

- **Icon kiếm hiệp còn sót** (xstart < 14000, dải icon gốc game cũ): 0 mẫu trang bị.
- **Tên kiếm hiệp còn sót** trong mẫu trang bị: 58 (Áo dài, Áo Lunala, Áo Rayquaza, Áo giáo sư, Áo Bellossom, Áo Lucario).
- **Ảnh icon trùng hệt nhau** trong `map/icon_pokemon` (cùng nội dung, khác số): 47 nhóm (260,415; 131,458; 173,429; 261,416; 171,430). Icon PMDCollab (CC BY-NC) - không dùng cho đồ mới.
- **Bộ**: chỉ có 33 dòng bộ bậc (11 bộ × 4/7/11 món, cùng thưởng +5% HP / +5% tấn công / +3% chí mạng mọi bậc). Chưa có bộ theo hệ (đặc tả 4.4 "Bộ hệ") hay theo vùng; không món nào mang tên / hình Pokémon.
- **Trang trí**: 8 bảng màu trang phục + 7 kiểu phụ kiện vẽ theo trang bị (`NgoaiHinh`); cửa hàng lượng (`nap_cua_hang`) có skin / hào quang nhưng **không hình nào hiện trên Pokémon** và không có ô ngân sách chỉ số.

### 1.1 Khoảng trống theo dải cấp × ô (số mẫu có tên/hình riêng ngoài món gốc)

| Ô | cấp 1–19 | cấp 20–39 | cấp 40–59 | cấp 60–79 | cấp 80–98 |
|---|---|---|---|---|---|
| Khăn quàng | 71 tên, 0 họ | 2 tên, 0 họ | 2 tên, 0 họ | 2 tên, 0 họ | 3 tên, 0 họ |
| Đai | 11 tên, 0 họ | 2 tên, 0 họ | 2 tên, 0 họ | 2 tên, 0 họ | 3 tên, 0 họ |
| Băng đô | 4 tên, 0 họ | 2 tên, 0 họ | 2 tên, 0 họ | 2 tên, 0 họ | 3 tên, 0 họ |
| Đồ cầm tay Charmander | 2 tên, 0 họ | 2 tên, 0 họ | 2 tên, 0 họ | 2 tên, 0 họ | 3 tên, 0 họ |
| Đồ cầm tay Bulbasaur | 5 tên, 0 họ | 2 tên, 0 họ | 2 tên, 0 họ | 2 tên, 0 họ | 3 tên, 0 họ |
| Đồ cầm tay Eevee | 3 tên, 0 họ | 2 tên, 0 họ | 2 tên, 0 họ | 2 tên, 0 họ | 3 tên, 0 họ |
| Đồ cầm tay Squirtle | 2 tên, 0 họ | 2 tên, 0 họ | 2 tên, 0 họ | 2 tên, 0 họ | 3 tên, 0 họ |
| Đồ cầm tay Pichu | 2 tên, 0 họ | 2 tên, 0 họ | 2 tên, 0 họ | 2 tên, 0 họ | 3 tên, 0 họ |
| Huy hiệu | 2 tên, 0 họ | 2 tên, 0 họ | 2 tên, 0 họ | 2 tên, 0 họ | 3 tên, 0 họ |
| Vòng cổ | 2 tên, 0 họ | 2 tên, 0 họ | 2 tên, 0 họ | 2 tên, 0 họ | 3 tên, 0 họ |
| Lông vũ | 2 tên, 0 họ | 2 tên, 0 họ | 2 tên, 0 họ | 2 tên, 0 họ | 3 tên, 0 họ |
| Găng | 2 tên, 0 họ | 2 tên, 0 họ | 2 tên, 0 họ | 2 tên, 0 họ | 3 tên, 0 họ |
| Phiến đá hệ | 2 tên, 0 họ | 2 tên, 0 họ | 2 tên, 0 họ | 2 tên, 0 họ | 3 tên, 0 họ |
| Ruy băng | 13 tên, 0 họ | 2 tên, 0 họ | 2 tên, 0 họ | 2 tên, 0 họ | 3 tên, 0 họ |

Mỗi ô × dải chỉ có 1-2 tên chung ("Khăn quàng bạc"), không có lựa chọn ngang nào (hệ, vùng), nên người chơi không có lý do đổi đồ trong cùng bậc ngoài độ hiếm.

## 2. Sau khi thêm

- **628 món mới** (`data_item` 1400..2027, `sql/nang_cap.sql` (khối `them_do_trang_phuc.sql`)): 540 món 18 bộ hệ (6 ô × 5 bậc) + 88 món 3 bộ huy chương vùng (8 ô × 11 bậc). Mỗi món là bản sao chỉ số của món gốc cùng ô + bậc (không sức mạnh dọc mới).
- **689 icon mới** 16x16 vẽ tay (`server/map/icon_do_moi`, id ảnh 15000+n), không trùng nhau: 0 nhóm trùng nội dung.

| Ô | cấp 1–19 | cấp 20–39 | cấp 40–59 | cấp 60–79 | cấp 80–98 |
|---|---|---|---|---|---|
| Khăn quàng | 0 món, 0 họ | 18 món, 18 họ | 18 món, 18 họ | 18 món, 18 họ | 36 món, 18 họ |
| Đai | 0 món, 0 họ | 18 món, 18 họ | 18 món, 18 họ | 18 món, 18 họ | 36 món, 18 họ |
| Băng đô | 0 món, 0 họ | 18 món, 18 họ | 18 món, 18 họ | 18 món, 18 họ | 36 món, 18 họ |
| Đồ cầm tay Charmander | 2 món, 1 họ | 2 món, 1 họ | 2 món, 1 họ | 2 món, 1 họ | 3 món, 1 họ |
| Đồ cầm tay Bulbasaur | 2 món, 1 họ | 2 món, 1 họ | 2 món, 1 họ | 2 món, 1 họ | 3 món, 1 họ |
| Đồ cầm tay Eevee | 2 món, 1 họ | 2 món, 1 họ | 2 món, 1 họ | 2 món, 1 họ | 3 món, 1 họ |
| Đồ cầm tay Squirtle | 2 món, 1 họ | 2 món, 1 họ | 2 món, 1 họ | 2 món, 1 họ | 3 món, 1 họ |
| Đồ cầm tay Pichu | 2 món, 1 họ | 2 món, 1 họ | 2 món, 1 họ | 2 món, 1 họ | 3 món, 1 họ |
| Huy hiệu | 2 món, 1 họ | 2 món, 1 họ | 2 món, 1 họ | 2 món, 1 họ | 3 món, 1 họ |
| Vòng cổ | 0 món, 0 họ | 18 món, 18 họ | 18 món, 18 họ | 18 món, 18 họ | 36 món, 18 họ |
| Lông vũ | 0 món, 0 họ | 18 món, 18 họ | 18 món, 18 họ | 18 món, 18 họ | 36 món, 18 họ |
| Găng | 2 món, 1 họ | 2 món, 1 họ | 2 món, 1 họ | 2 món, 1 họ | 3 món, 1 họ |
| Phiến đá hệ | 2 món, 1 họ | 2 món, 1 họ | 2 món, 1 họ | 2 món, 1 họ | 3 món, 1 họ |
| Ruy băng | 0 món, 0 họ | 18 món, 18 họ | 18 món, 18 họ | 18 món, 18 họ | 36 món, 18 họ |

- Dải cấp 1–19: bộ hệ bắt đầu ở bậc bạc (cấp 20) theo đặc tả (người mới dùng món gốc + bộ vùng Kanto); mọi ô khác có ít nhất một họ mới ở mọi dải.
- Độ hiếm: không đổi cơ chế - món họ mới rơi với đúng độ hiếm của món gốc mà nó thay (Thường..Sử thi, Huyền thoại qua chế tạo), khung độ hiếm PNG mới `client_jar/res/pk/khung_hiem.png` (6 bậc, Sử thi+ lấp lánh).
- Trang trí: 32 trang phục, 13 hào quang, 8 skin thú cưỡi, 8 thẻ bạn đồng hành (`data_trang_phuc`).

