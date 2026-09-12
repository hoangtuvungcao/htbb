# Client J2ME 2.7.5 (`Downloads\kpah275555`) trỏ về server chạy trên máy nhà

File tạo ra: `C:\Users\ADMIN\Downloads\kpah275555_local.jar` — đóng từ **bản sao** của thư mục
`Downloads\kpah275555` (client 2.7.5 đã giải nén, class bị làm rối tên). **Thư mục gốc không bị
đụng vào.** Ngày 12/09/2026, cùng lúc với việc chạy server local (xem
`docs/HUONG_DAN_VPS_FRP.md` mục "Thứ tự khởi động ở máy nhà").

## Đã đổi gì

Chỉ một file: `yv.class` — lớp màn hình chọn máy chủ (bản 2.7.6 gọi là
`game/render/screen/ServerListScr`, xem [client-test-276.md](client-test-276.md)). Danh sách
server cứng trong `yv.<clinit>`: 9 tên, 4 IP dùng chung, cổng 19129 (Cổ Loa 19130).

| Mục trong game | Trước | Sau | Cổng | Dùng khi |
|---|---|---|---|---|
| **Vạn Xuân(mới)** | `112.213.84.163` | **`127.0.0.1`** | 19129 | **server chạy trên chính máy này** |
| Cổ Loa | `27.0.12.112` | *(giữ)* | 19130 | — |
| **Thăng Long**, **Hoa Lư** | `103.77.167.209` | **`180.93.57.30`** | 19129 | VPS thật (so sánh local ↔ live) |
| Kinh Môn → Tiên La | `112.213.94.124` | *(giữ)* | 19129 | — |

## Cách vá — có công cụ, không làm tay nữa

`tools/DoiChuoiClass.java`: đổi chuỗi hằng `CONSTANT_Utf8` trong một tệp `.class`, tự ghi lại
2 byte độ dài (constant pool đọc tuần tự, không gì tham chiếu theo offset tuyệt đối — xem giải
thích trong client-test-276.md). Không tìm thấy chuỗi cũ thì không ghi gì.

```bash
javac -encoding UTF-8 -d build/tools-classes tools/DoiChuoiClass.java
java -cp build/tools-classes DoiChuoiClass <ban-sao>/yv.class "112.213.84.163" "127.0.0.1" "103.77.167.209" "180.93.57.30"
# kiem: javap -v -p yv.class | grep -E '[0-9]+\.[0-9]+\.[0-9]+'   (va <clinit> van ldc dung #55/#59)
# dong jar: chuyen META-INF ra ngoai roi  jar cfm ra.jar META-INF/MANIFEST.MF -C <ban-sao> .
```

Tìm class chứa IP trong một client mới: `grep -rlaE '([0-9]{1,3}\.){3}[0-9]{1,3}' --include='*.class' .`
(grep thường bỏ qua file nhị phân — phải có `-a`), rồi `javap -v -p` xem constant pool.

## Đây là client MOD — và vì thế KEmulator KHÔNG chạy được nó

Soát 838 class: **827 class major 45** (J2ME thật) và **11 class major 52 (Java 8)**:
`pp`, `tbao`, `text`, `tienich`, `sleep`, `gianhang`, `gianhang2`, `tula5`, `tula8`, `tula9`,
`tula10` — là mod ("gian hàng", "đánh nhanh"…) do ai đó thêm vào, và được **móc vào lõi**:
`acv` (canvas chính) gọi `pp` và `tbao`, `abj` gọi `text`. Cả hai bản KEmulator trong Downloads
dùng JRE **1.4.2** kèm theo (chỉ đọc class ≤ 48) nên vừa "Launch MIDlet" là
`UnsupportedClassVersionError: pp (major.minor 52.0)` — không liên quan gì tới bản vá IP.
`menu.class` (chỉ `tula5` gọi, mà không ai gọi `tula5`) không có cả trong jar gốc
`Downloads\kpah275555.jar` — mã chết, vô hại.

Jar gốc và thư mục giải nén giống hệt nhau (1080 entry), nên `kpah275555_local.jar` = jar gốc
chỉ đổi `yv.class`.

## Cách chạy: MicroEmulator trên JDK hệ thống

```
C:\Users\ADMIN\Downloads\chay-kpah275555-local.bat
```
tức `"C:\Program Files\Java\jdk-26.0.1\bin\java.exe" -jar Downloads\microemulator.jar Downloads\kpah275555_local.jar`
(bản MicroEmulator "SesenBlog - ME" có sẵn trong Downloads; chạy trên JVM hiện đại nên nạp được
class 52). Đã thử 12/09/2026: mở được tới màn Launcher, không ngoại lệ; bấm **Start**.

**Bẫy RMS — đã dính ngay lần đầu**: lúc mở, `yv()` đọc RMS `ipnqsh` (danh sách server client
đã lưu) và **đè lên** danh sách vừa vá. MicroEmulator giữ RMS theo `MIDlet-Name` ở
`C:\Users\ADMIN\.microemulator\suite-Khi Phach Anh Hung\`, và ở đó đã có `ipnqsh.rs` chứa đúng
một mục "Server → 180.93.57.30" do lần chạy KPAH trước để lại — bấm Start là nối thẳng VPS.
Đã đổi tên thành `ipnqsh.rs.bak-truoc-local-20260912`. Client ghi lại `ipnqsh` khi bấm **Cập
nhật** hoặc khi server gửi danh sách xuống, nên nếu sau này jar mới mà vẫn nối IP lạ: xoá/đổi
tên `ipnqsh.rs` lần nữa (đừng xoá cả thư mục suite — `nqshlogin.rs` là tài khoản đã lưu). KEmulator
Pro cũng còn `rms\SonyEricssonK800_500x500\.aipnqsh` từ 2022, cùng bẫy.

Phương án B nếu vẫn muốn KEmulator: hạ major version 11 class mod từ 52 xuống 48 (2 byte ở
offset 6-7). Đã soát: chúng không dùng `invokedynamic`, `ldc class`, MethodHandle — nhiều khả
năng nạp được trên 1.4.2, nhưng **chưa thử**.

Trong game chọn **Vạn Xuân(mới)**. Server local phải đang chạy: MySQL 3306, login 8023, game
19129 (xem `HUONG_DAN_VPS_FRP.md`, mục "Thứ tự khởi động ở máy nhà").

## Nếu đăng nhập lỗi

Client bắt mọi ngoại lệ khi phân tích gói tin và in `<mã lệnh> tai cmd nay` ra console giả lập
— mã đó là lệnh server gửi mà client 2.7.5 không đọc được. Gói đăng nhập 2.7.5/2.7.6 giống
nhau (`version` = "2.7.5"), server không chặn theo phiên bản
([migration-client-276.md](migration-client-276.md)).
