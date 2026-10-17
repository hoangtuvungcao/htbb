# QA giao diện client J2ME (Huyền Thoại Bảo Bối)

Đợt kiểm 17/10/2026, nhánh `pokemon/chuyen-doi`. Máy thử: MicroEmulator có skin 176x220 và `--resizableDevice 240 320`.
Server thử: 127.0.0.1:19129, tài khoản GM tai004–tai010 (tai001 và tai003 có người khác dùng).

Trạng thái: **OK** = đã mở, chụp màn hình, không lỗi hiển thị · **SỬA** = đã sửa trong đợt này ·
**SV** = cần server · **CHƯA** = chưa mở được trong đợt này (lý do ghi bên cạnh).

## Trước khi vào game

| Màn | Cách mở | Trạng thái |
|---|---|---|
| Tải (loading) | khởi động | SỬA: nền banner nhiều lớp + logo + Poké Ball chờ (`Banner`, `ThuongHieu.veLogo`) |
| Chọn máy chủ | sau màn tải | SỬA: banner thay bản đồ game, thẻ trắng mờ |
| Đăng nhập | Chọn máy chủ → Chọn | SỬA: banner, thẻ mờ, bỏ dòng "Hỗ trợ" đè lên Pokémon |
| Đăng ký | Đăng nhập → Menu → Đ.Ký | SV: server chặn đăng ký trong client (chuyển sang web) |
| Chọn nhân vật | sau đăng nhập | SỬA: banner + chiếu sáng Pokémon khởi đầu cùng dòng, bảng navy mờ |
| Tạo nhân vật | Chọn nhân vật → ô trống | SỬA: banner + cột sáng/hào quang màu hệ cho Pokémon đang chọn, huy hiệu tròn, tiêu đề không đè Poké Ball, dòng vai trò không tràn |
| Mất kết nối | server ngắt | SỬA: hộp thoại "Mất kết nối…" + **Kết nối lại** / Thoát (trước: NPE khi `Sound.me` null → treo) |

## Menu chính (phím mềm trái)

| Nhóm → mục | Màn | Trạng thái |
|---|---|---|
| Tên nhân vật → Xa phu, Chế đồ nhanh, Dòng ẩn, Auto up, Vận tiêu, Vận may, Tiện ích | menu mod | OK (Xa phu: "Map này không có Pidgeot" đúng) |
| Cài đặt → Cấu hình | menu Thấp/Vừa/Cao | OK |
| Cài đặt → Bản đồ lớn | PageScr | SỬA: bản đồ vùng vẽ bằng code (đảo, đường, chấm thị trấn, huy hiệu Pokémon người chơi) thay ảnh bản đồ kiếm hiệp; khung vừa màn 176 |
| Cài đặt → Bật/tắt giao diện, Âm thanh | bật/tắt | OK |
| Cài đặt → Hướng dẫn | HelpScr | SỬA: chữ tràn mép phải (ngắt dòng theo bề rộng khung) |
| Cài đặt → Lời mời / Auto đánh / Chế độ focus | hộp thoại / BangAuto | SỬA: viên tab + mũi tên vàng thay thanh xám và ảnh mũi tên cũ |
| Bản thân → Hành trang | WindowInfoScr tab 0 | OK |
| Bản thân → Kỹ năng | tab 3 | SỬA: "1/11" đè "Điểm kỹ năng" |
| Bản thân → Tiềm năng | tab 2 | SỬA: bỏ ảnh inv_1 kiếm hiệp, hàng chỉ số bo góc + biểu tượng màu hệ + nút "+" (`BangTiemNang`) |
| Bản thân → Trang bị / Thông tin / Trang bị thú | tab 1 / 4 / 31 | OK |
| Bản thân → Cây thần | Tree2014 | SỬA: tiêu đề trống, màu ô theo chủ đề |
| Bản thân → Cổ vật | TuBinhScr | SỬA: tab xám/vàng cũ → ô chọn xanh viền vàng |
| Bản thân → Khác | menu server (túi, kho, shop đặc biệt) | OK |
| Nạp Xu | hộp thoại | SV: "tạm khóa" |
| Nhiệm vụ | tab 6 | OK |
| Khác → Tin nhắn | MessageScr | OK |
| Khác → Nhóm | tab 5 | OK |
| Khác → Bạn bè | ListScr | OK ("Chưa có bạn") |
| Khác → Top cao thủ / đại gia / Bảng Top | ListScr | SỬA: hình nhân vật kiếm hiệp → huy hiệu Pokémon; tiêu đề chữ trắng (font cũ mất chữ hoa có dấu, đè Poké Ball) |
| Khác → Kênh thế giới | MsgWorld | SỬA: cuộn cộng dồn mỗi lần mở |
| Khác → Diễn đàn / Rời bang | trình duyệt / hộp thoại | không mở khi kiểm tự động |
| Bang hội (đội) → Top / Đăng ký | ListScr | SỬA: danh sách rỗng gây NullPointer mỗi khung hình (không đóng được cửa sổ) → "Chưa có dữ liệu" |
| Cửa hàng | WindowInfoScr tab 20 | OK |

## Phím # và trong game

| Màn | Cách mở | Trạng thái |
|---|---|---|
| Menu # (Menu nhanh, Chat, Thay đồ, Tìm bạn) | phím # | OK |
| Menu nhanh (MenuNhanh) | # → Menu nhanh | OK; nhãn âm thanh hiển thị chữ "Â" (font ghép) |
| Chat / lệnh GM | # → Chat | OK (`mto 3 30 30` dịch chuyển) |
| HUD, thanh skill, bản đồ nhỏ | trong game | SỬA: chân dung HUD = huy hiệu tròn |
| Khung mục tiêu (quái / NPC / người) | phím mềm phải | SỬA: chân dung = huy hiệu tròn (cùng `PokeBody.paintChanDung`) |
| Chết / hồi sinh | hết HP | hộp thoại chung MsgDlg (không chụp được trong đợt này) |

## Cửa sổ chỉ mở qua NPC (quét bằng bản gỡ lỗi ở scratchpad, phím `d` mở lần lượt tab 0..32)

| Tab | Cửa sổ | Trạng thái |
|---|---|---|
| 7 | Học kỹ năng | OK |
| 8 | Trao đổi | OK bố cục; CHƯA thử với người chơi thật (cần 2 tài khoản) |
| 9, 10, 19, 20, 23, 24 | gian hàng / shop | OK với dữ liệu thật; mở rỗng (gỡ lỗi) thì lỗi dữ liệu chứ không phải lỗi vẽ |
| 21, 22, 25, 26, 28 | luyện đồ, kho, khảm, hợp thành, chế đồ | SỬA màu ô cũ (ScreenDapDo / ScreenItemTim); CHƯA thử luồng NPC đầy đủ |
| 31, 32 | trang bị thú, hợp đồ thú | OK bố cục |
| ShopOnline / MTickets / quay may mắn | NPC sự kiện | SỬA tab + mũi tên ShopOnline; còn lại CHƯA mở |
| Pokédex NPC -526 | NPC | OK (đợt trước) |

## Font tiếng Việt

Font ảnh của game chỉ có chữ thường có dấu + chữ hoa không dấu. `Font.ghep` ghép tại chỗ chữ hoa có dấu
(À…Ỵ, Ơ, Ư) từ chữ hoa gốc + dấu của chữ thường cùng font (khi cần, dấu trên chạm đỉnh chữ hoa hoặc chữ hoa
hạ 1 px), lưu đệm theo chữ. Chữ font vẫn thiếu thì thay chữ cùng chữ khác hoa/thường → bỏ dấu → "?" (không
còn mất chữ im lặng). Kiểm trên arialFontW, borderFont, normalFont, bigFont (không có chữ thường → dùng chữ hoa),
smallFont (chỉ chữ thường ASCII → bỏ dấu).
