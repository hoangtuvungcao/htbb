# Chuyển sang client KPAH_276 (J2ME 2.7.6)

Khảo sát khả năng cho server chạy với client `KPAH_276-1`.

## Ba bên đang có

| | Phiên bản | Dạng |
|---|---|---|
| Client đang dùng | **2.3.2** | C# (`Client/`), port từ J2ME |
| Client mới | **2.7.6** | J2ME đã giải nén (`MIDlet-Version: 1.5.9`, `GameMidlet.version = "2.7.6"`) |
| Server mặc định | `1.0.3` | `MessageCreator.version` |

## Đã kiểm chứng chắc chắn

**1. Gói đăng nhập khớp chính xác.** Client gửi 11 trường, server đọc đúng 11
trường đó, đúng thứ tự và kiểu:

```
username(UTF) pass(UTF) version(UTF) platform(UTF) clientProvider(UTF)
provider(UTF) agent(UTF) firmware(byte) w(short) vsTile(byte) idServer(byte)
```

`GameService.login()` (client) ↔ `LoginHandler.process()` dòng 206-233 (server).

**2. Server KHÔNG từ chối phiên bản.** Nó chỉ nhận lấy:
`MessageCreator.version = version;` — không có kiểm tra nào chặn 2.7.6.

**3. Phủ mã lệnh: 127/132 (96%).** Server gửi 132 mã lệnh khác nhau, client
2.7.6 xử lý 127. Năm mã còn lại đều là phụ: `-44` (thông tin thú), `-40`,
`47` (lệnh admin), `123` (cờ thẩm định), `127` (số người online).

**4. Lệnh lạ KHÔNG làm client crash.** `MessageHandler.onMessage` bọc toàn bộ
`switch` trong `try/catch` và **không có nhánh `default`** → mã lệnh không biết
bị bỏ qua im lặng.

**5. Cùng họ giao thức.** Mọi mã lệnh client 2.7.6 xử lý thì client 2.3.2 cũng
xử lý (142 mã chung, 2.7.6 không có mã nào lạ).

## CHƯA kiểm chứng được — và tại sao

**Bố cục byte bên trong từng lệnh.** Tôi đã thử so tự động chuỗi `read*()` của
hai client cho từng lệnh, nhưng cách đó **không đáng tin** và tôi đã bỏ kết quả:
cả hai bên đều có vòng lặp (`while (i < CHAR_POTION)`) và `switch` lồng nhau,
nên việc trích đoạn hay lấy nhầm phạm vi. Ví dụ `case 1` trong `MessageHandler.cs`
xuất hiện 5 lần, lần đầu là switch lồng bên trong `case 86`.

Kết luận trung thực: **phải thử thật mới biết**. Chạy server cục bộ, trỏ client
vào, xem đăng nhập và vào map có trót lọt không. Đó là cách duy nhất cho câu
trả lời chắc chắn.

## Đã sửa: phiên bản phải theo từng phiên

`MessageCreator.version` là biến **static toàn cục**, bị `LoginHandler` ghi đè
**mỗi lần có người đăng nhập**. Trong khi `Session.version` (theo từng phiên) đã
tồn tại sẵn và vài chỗ đã dùng đúng (`Session.java:373`,
`MessageCreator.java` chỗ so `2.2.5`).

Chỗ dùng sai đáng kể: `CharVongLap` (vòng lặp `update`, chạy cho **mọi** người
chơi) gọi `isVersion162()` đọc biến toàn cục → nó lấy phiên bản của **người đăng
nhập gần nhất**. Hàm này quyết định mức phạt khi chết (`timedie`, bảng `cost`).

Chạy lẫn 2.3.2 và 2.7.6 thì một người 2.7.6 đăng nhập sẽ đổi hành vi chết của
**tất cả** người 2.3.2, và ngược lại.

Đã thêm `isVersion162(Char)` đọc từ `p.getSession().version` (an toàn null vì
`getSession()` có thể null và `Session.version` mặc định `"0"`), và
`isVersionAtLeast162(String)` để so chuỗi. Hàm cũ giữ nguyên, đánh dấu
`@Deprecated` — `LoginHandler` gọi nó ngay sau khi gán nên vẫn đúng.

11 kiểm thử mới trong `test/KiemPhienBan.java` chốt logic này.

## Việc còn lại nếu muốn chuyển thật

1. **Thử thật trước** — server cục bộ + client 2.7.6, xem đăng nhập/vào map.
   Client in lỗi ra `Cout.println("<cmd> tai cmd nay")` khi parse hỏng, đó là
   chỗ bắt đầu tìm.
2. Rà nốt các chỗ còn đọc `MessageCreator.version` toàn cục
   (`LoginHandler` dòng 402 so `1.8.6`, dòng 469 so `2.0.0`).
3. Năm mã lệnh client không xử lý: quyết định bỏ hay thêm handler phía client.
4. `server.ini` chưa có mục cấu hình phiên bản client tối thiểu — nếu muốn ép
   người chơi lên 2.7.6 thì cần thêm.
