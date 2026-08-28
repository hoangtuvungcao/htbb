# Client thử nghiệm trỏ về server của bạn

File: `C:\Users\ADMIN\Downloads\KPAH_276_TEST.jar` (1.6 MB, 1106 entry)

Tạo từ `Downloads\KPAH_276-1` — **thư mục gốc của bạn không bị đụng vào**.

## Đã đổi gì

Chỉ một file: `game/render/screen/ServerListScr.class`. Danh sách server:

| Mục trong game | Trước | Sau | Cổng |
|---|---|---|---|
| **Vạn Xuân(mới)** | `112.213.84.163` | **`180.93.57.30`** | 19129 |
| Cổ Loa | `27.0.12.112` | *(giữ nguyên)* | 19130 |
| **Thăng Long** | `103.77.167.209` | **`127.0.0.1`** | 19129 |
| **Hoa Lư** | `103.77.167.209` | **`127.0.0.1`** | 19129 |
| **Kinh Môn → Tiên La** | `112.213.94.124` | **`180.93.37.32`** | 19129 |

→ Chọn **Vạn Xuân(mới)** để nối vào VPS thật, **Thăng Long** để thử server chạy
trên chính máy bạn.

## Cách vá (để bạn tự làm lại được)

Không dịch ngược rồi biên dịch lại — chuỗi IP là hằng `CONSTANT_Utf8` trong
constant pool, dạng `[01][u2 độ dài][bytes]`. Constant pool được đọc **tuần tự**,
không chỗ nào tham chiếu theo offset tuyệt đối, nên đổi độ dài chuỗi là an toàn.
Đã sửa cả 2 byte độ dài cho khớp. File: 8436 → 8429 byte.

Kiểm chứng ba lớp: `javap` đọc được cấu trúc → `unzip -t` báo jar toàn vẹn →
dịch ngược **từ trong jar** ra thấy đúng mảng `address`.

## Cách chạy

`.jar` này là **MIDlet J2ME**, không chạy bằng `java -jar` được. Cần giả lập:

- **KEmulator** (Windows, nhẹ nhất cho việc này) — mở jar rồi Run
- **MicroEmulator** (đa nền tảng)
- **J2ME Loader** (Android) — chép jar vào máy rồi mở

## Một cái bẫy phải biết trước khi thử

`ServerListScr.loadIP()` đọc danh sách server từ **RMS** (bộ nhớ lưu của MIDlet),
và nếu có thì **đè lên** danh sách mặc định vừa vá:

```java
byte[] bData = RMS.loadRMS("ipnqsh");
if (bData == null) { return; }   // chưa lưu -> dùng mặc định (đã vá)
... // đã lưu -> ghi đè, IP vừa vá bị bỏ qua
```

Nên nếu giả lập của bạn **đã từng chạy client này**, nó có thể còn danh sách cũ
lưu sẵn và jar mới sẽ nối vào IP cũ. Cách xử lý: xoá dữ liệu RMS của MIDlet
trong giả lập (KEmulator: xoá thư mục `rms`), hoặc dùng một hồ sơ giả lập mới.

## Nếu đăng nhập lỗi

Client bắt mọi ngoại lệ khi phân tích gói tin và in ra:

```
<mã lệnh> tai cmd nay
```

Bật console của giả lập lên xem — con số đó chính là mã lệnh mà server gửi nhưng
client 2.7.6 phân tích không nổi, và là chỗ bắt đầu tìm.

Xem thêm [migration-client-276.md](migration-client-276.md) để biết đã kiểm
chứng được gì và còn gì chưa chắc.

---

## Cập nhật: có HAI server, đó là nguyên nhân "tài khoản bị khoá"

Bản `KPAH_276_TEST.jar` đầu tiên chỉ trỏ về `180.93.57.30` (lấy từ
`.github/workflows/deploy.yml`), và đăng nhập báo **"Tài khoản của bạn hiện đang
bị khoá"**.

Truy ra: client **PK Ngũ Đế** đang chơi được nối vào một server **khác** —
`180.93.37.32`, tên hiển thị "Hùng Vương" (tìm thấy trong `qF.java` của
`KPAH_PK-ngude.jar`, lớp danh sách server đã bị làm rối tên).

Hai client gửi gói đăng nhập **giống hệt nhau** — đã đối chiếu từng trường:

```
username, pass, version, platform, client_Provider="0", provider="0",
agent="0", byte 0, short w, byte vsTile, byte serverIndex
```

Cả hai đều có `agent.txt` = `0` và `provider.txt` = `0`. Khác biệt duy nhất là
chuỗi phiên bản: `2.0.9` (Ngũ Đế) vs `2.7.6` — mà không nhánh nào trong
`LoginHandler` phân biệt hai giá trị đó (cả hai đều vượt mốc `1.8.8` và `2.0.0`).

Nên **không phải lỗi giao thức**: tài khoản có `ban = 1` trên `180.93.57.30`
nhưng `ban = 0` trên `180.93.37.32`. Hai server, hai CSDL `account` riêng.

### File mới: `KPAH_276_TEST_v2.jar`

| Mục trong game | Trỏ tới | Dùng khi |
|---|---|---|
| **Vạn Xuân(mới)** | `180.93.57.30:19129` | server trong `deploy.yml` |
| **Thăng Long** / **Hoa Lư** | `127.0.0.1:19129` | chạy thử cục bộ |
| **Kinh Môn → Tiên La** | `180.93.37.32:19129` | **server bạn đang chơi** |

Chọn **Kinh Môn** để thử client 2.7.6 trên đúng server mà tài khoản của bạn
đang hoạt động.

### Câu hỏi còn bỏ ngỏ

`deploy.yml` đẩy bản build lên `180.93.57.30`, nhưng bạn chơi ở `180.93.37.32`.
Nếu `180.93.37.32` mới là server thật thì **mọi bản build từ repo này đang được
đẩy lên sai máy** — cần kiểm tra lại.
