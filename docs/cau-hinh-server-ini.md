# Cấu hình server — `server.ini`

Mọi thiết lập chạy máy chủ nằm trong `server.ini`: cổng, kết nối CSDL, tỉ lệ rơi đồ, giờ
chiếm thành, chống flood, quyền admin.

Tài liệu này là bản tra cứu. Phần dựng server lần đầu xem
[HUONG_DAN_BUILD_VPS.md](HUONG_DAN_BUILD_VPS.md).

---

## 1. Có **hai** file `server.ini` khác nhau

Đây là chỗ nhầm nhiều nhất.

| | Đường dẫn | Dùng cho |
|---|---|---|
| Máy nhà | `KPAH\server.ini` | server chạy thử ở nhà |
| **VPS** | **`/opt/kpah/server.ini`** | **server thật, người chơi đang vào** |
| VPS (đăng nhập) | `/opt/kpah/loginServer/server.ini` | server đăng nhập |

`server.ini` nằm trong `.gitignore` vì chứa mật khẩu CSDL. Nghĩa là **sửa file ở máy nhà
rồi `git push` cũng không tới VPS** — hai file hoàn toàn tách rời. Muốn đổi cho server thật
thì phải sửa **trên VPS**.

Bản mẫu không chứa mật khẩu thật: [`vps/server.ini.linux.example`](../vps/server.ini.linux.example).

## 2. Mở file

**Ở máy nhà** — VS Code: `Ctrl+P` rồi gõ `server.ini`.

**Trên VPS:**

```bash
ssh root@180.93.57.30
cp /opt/kpah/server.ini /root/server.ini.bak    # sao lưu trước khi sửa
nano /opt/kpah/server.ini
```

Lưu trong `nano`: `Ctrl+O` → Enter → `Ctrl+X`.

File phải cho user `kpah` ghi được, không thì bảng setting trong game không lưu được:

```bash
chown kpah:kpah /opt/kpah/server.ini
```

## 3. Ba cách đổi cấu hình

| Cách | Khi nào dùng |
|---|---|
| Sửa file rồi gõ `setting` → `8` trong game | đổi nhanh, **không cần khởi động lại** — nhưng chỉ với các khoá ở mục 4 |
| Sửa file rồi `systemctl restart kpah-game` | mọi khoá khác |
| Bảng `setting` trong game | tiện nhất cho tỉ lệ rơi và giờ chiếm thành |

Bảng `setting` **ghi thẳng vào `server.ini`**, nên đổi trong game vẫn còn sau khi khởi động
lại. Hai chiều đều đúng: sửa tay file rồi gõ `8` là áp dụng ngay. Bảng setting và file luôn
là **cùng một nguồn dữ liệu** (xem [`server/CauHinh.java`](../src/server/CauHinh.java)).

Lệnh trong ô nhập của `setting`:

```
1                                          chiếm thành sau 2 phút
2 <giờ> <phút> [ngày]                      đặt giờ chiếm thành
3                                          trả về lịch chuẩn 21h thứ 3 + thứ 7
5 <xu> <lượng> <sựkiện> <bình> <ngọc> <đồ> <chiaXu>   đặt tỉ lệ rơi
6                                          xem cấu hình hiện tại
8                                          nạp lại server.ini sau khi sửa tay
```

Gõ `6` trước khi sửa là cách nhanh nhất để biết đang đặt bao nhiêu.

## 4. Khoá nào nạp lại được, khoá nào phải khởi động lại

Chỉ **17 khoá** dưới đây được `CauHinh.napLai()` đọc lại. **Mọi khoá khác chỉ được đọc một
lần lúc bật máy** — sửa xong bắt buộc `systemctl restart kpah-game`.

```
sv.tileXu   sv.tileLuong   sv.tileItemSuKien   sv.tilePotion   sv.tileNgoc   sv.tileItem
sv.chiaXuRoi
sv.gioChiemThanh   sv.phutChiemThanh   sv.ngayChiemThanh
sv.adminAccounts   sv.maxCharPerAccount   sv.choPhepTaoTenGM
sv.website   sv.linkTaiGame   sv.testMode   sv.testCode
```

Cách tự kiểm chứng khi thêm khoá mới: khoá nào xuất hiện trong
[`src/server/CauHinh.java`](../src/server/CauHinh.java) thì nạp lại được; khoá chỉ có trong
[`src/server/TeamServer.java`](../src/server/TeamServer.java) thì phải khởi động lại.

## 5. Tỉ lệ rơi đồ (hay chỉnh nhất)

Các số là **phần trăm so với bản gốc**: `100` = như gốc, `50` = còn một nửa, `0` = tắt hẳn.

| Khoá | Chỉnh cái gì | Tỉ lệ gốc |
|---|---|---|
| `sv.tileXu` | quái rơi xu | 8–9% mỗi con |
| `sv.tileLuong` | ra lượng mở / lượng khoá | 25% mỗi con, mỗi loại |
| `sv.tileItemSuKien` | bánh, kẹo, bóng, kem, trứng pet… | |
| `sv.tilePotion` | bình HP/MP, bình tăng tốc, hp/mp chiến trường | |
| `sv.tileNgoc` | ngọc, ngọc tử binh | |
| `sv.tileItem` | trang bị rơi ra | |
| `sv.chiaXuRoi` | **số lượng** xu mỗi lần rơi | `10` = như gốc |

`sv.chiaXuRoi` khác `sv.tileXu`: một cái là **bao nhiêu lần** rơi, một cái là **mỗi lần bao
nhiêu**. Thấy "rơi ít lần nhưng mỗi lần quá nhiều xu" thì **tăng** `sv.chiaXuRoi`
(`30` = mỗi lần chỉ còn 1/3).

## 6. Chiếm thành

```ini
sv.gioChiemThanh = -1      # -1 = lịch chuẩn 21h thứ 3 + thứ 7
sv.phutChiemThanh = 0
sv.ngayChiemThanh =        # trống = mọi ngày; "2026-07-28" = đúng ngày đó; "Tue"/"Sat" = thứ
```

Phiên luôn kéo dài 1 tiếng. Lệnh chat tương đương: `stown 1 <giờ> <phút> [ngày]`,
`stown 0` (hoặc `ktct`) trả về lịch chuẩn, `tgct` xem giờ đang đặt.

## 7. Quyền admin

```ini
sv.adminAccounts = ten1,ten2
```

**So với TÊN NHÂN VẬT, không phải tên tài khoản.** Không phân biệt hoa/thường, ngăn bằng dấu
phẩy. Để trống = không cấp cho ai.

## 8. Chống flood

Đều **phải khởi động lại**. Số trong ngoặc là mặc định khi thiếu dòng.

| Khoá | Nghĩa |
|---|---|
| `sv.socketTimeout` | giây, ngắt kết nối im lặng quá lâu (180) |
| `sv.maxConnPerIp` | số kết nối cùng lúc tối đa từ một IP (50) |
| `sv.connRateMax` | số kết nối mới tối đa từ một IP trong một cửa sổ (60) |
| `sv.connRateWindow` | độ dài cửa sổ đếm, giây (30) |
| `sv.dosBanSec` | cấm tạm một IP flood bao lâu, giây (300) |
| `sv.reloginWaitSec` | phải chờ bao lâu mới vào lại sau khi thoát (0 = vào ngay) |
| `limit.ccu` | số người online tối đa — **bắt buộc có**, không có mặc định |

## 9. Kết nối CSDL

Đều phải khởi động lại. `db.maxco` + `db.maxNap` + `db.maxco1` + login server (8) + web
**phải nhỏ hơn** `max_connections` của MariaDB, thiếu là giờ đông người ăn lỗi
*Too many connections*.

## 10. Cạm bẫy

### Sai một dòng là **bỏ qua mọi dòng bên dưới**

Toàn bộ phần đọc cấu hình nằm trong **một khối `try`**
([TeamServer.java:404](../src/server/TeamServer.java#L404)). Một khoá thiếu hoặc sai kiểu là
ném lỗi ngay tại dòng đó, và **mọi dòng phía sau không được đọc** — nhưng server **vẫn khởi
động**, chạy với cấu hình dở dang.

Ví dụ thật: xoá `limit.ccu` thì `sv.port` (đọc trước) vẫn đúng, còn **toàn bộ kết nối CSDL
(đọc sau) không được đặt** — server bật lên nhưng không vào được.

Dấu hiệu duy nhất là dòng này trong log:

```
Load configuration file error!
```

Nên **sau mỗi lần sửa, phải xem log**:

```bash
journalctl -u kpah-game -n 50 | grep -i "configuration file error"
```

Không thấy dòng nào là đã đọc trọn file.

### Các khoá **bắt buộc phải có**

Những khoá này đọc thẳng không có giá trị mặc định, thiếu là kích hoạt cạm bẫy ở trên:

```
api.sv  api.url  sv.port  limit.ccu  sv.server  sv.portnap  sv.LG  sv.range
sv.pathLog  sv.iplogin  sv.portlogin  sv.EnableAvtiveCheck
db.host  db.name  db.user  db.password  db.maxco
db.nameNap  db.userNap  db.pass  db.maxNap
db.host1  db.name1  db.user1  db.password1  db.maxco1
sv.isSuKien*  (mọi khoá sự kiện)
```

Các khoá ở mục 8 (trừ `limit.ccu`) và ở mục 4 thì **có mặc định**, xoá đi vẫn chạy.

### `sv.isSuKien83` là khoá chết

Nó có trong file nhưng **không được đọc ở đâu trong mã nguồn**. Đổi giá trị không có tác
dụng gì. Các khoá `sv.isSuKien*` còn lại thì có thật.

### Đừng làm ba việc này

1. **Đừng `git add server.ini`.** Nó bị ignore có lý do — trong đó có mật khẩu CSDL.
2. **Đừng gửi nội dung file cho ai**, kể cả dán vào chat hỏi bài. Cần hỏi thì nói **tên
   khoá**, đừng kèm giá trị.
3. **Đừng sửa file ở máy nhà rồi tưởng VPS đổi theo.** Hai file riêng biệt.

## 11. Sau khi sửa — kiểm lại

```bash
# 1. Đọc trọn file chưa (không được có dòng nào)
journalctl -u kpah-game -n 80 | grep -i "configuration file error"

# 2. Server còn sống không
systemctl status kpah-game --no-pager

# 3. Cổng còn nghe không
ss -lntp | grep 19129
```

Trong game gõ `setting` → `6` để xem giá trị server **đang thực sự dùng** — đây là cách chắc
chắn nhất, vì nó đọc từ bộ nhớ chứ không đọc lại file.
