# `hack.txt` và `hackdo.txt`

Hai file này nằm ở thư mục gốc của server (`/opt/kpah` trên VPS) và đọc được ngay
trên trang **Chẩn đoán** của web quản trị.

```
hack.txt     nghi vấn chung    — ngập gói tin, đổi map trái phép, chọn nhân vật của người khác
hackdo.txt   nghi vấn vật phẩm — mua bán, hành trang, trang bị
```

## Trước đây chúng luôn rỗng

Không phải do phân quyền hay do đường dẫn. Chỗ ghi `hack.txt` duy nhất nằm trong
`Database.banchar()`, và **không nơi nào gọi hàm đó**. `hackdo.txt` thì còn không
được ghi ở đâu cả — nó chỉ được *đọc* (`Database.java:3703`), như một file đầu vào
admin gõ tay tên nhân vật vào.

Cả `banchar()` lẫn `bakupChar()` (hàm duy nhất đọc `hack.txt`) đều là tàn dư của
một công cụ chống gian lận thủ công đời trước. **Không bật lại chúng**: chúng dựng
SQL bằng cách nối chuỗi thẳng tên nhân vật, và `banchar()` đặt `ban=0` — tức là
*mở khoá*, không phải khoá.

Nay hai file được ghi bởi [`data.NghiVanGianLan`](../src/data/NghiVanGianLan.java),
nối vào các phép phát hiện **đang chạy thật** trong server.

## Định dạng

Một sự kiện là **một dòng**, năm cột ngăn bằng ` | `:

```
2026-08-29 14:03:11 | AnhHung | 1.2.3.4 | hackbitem | doi mua vat pham id 9999 khong co trong danh sach ban
```

Tên nhân vật và phần chi tiết là dữ liệu do người chơi kiểm soát, nên ký tự xuống
dòng bị lọc và dấu `|` bị đổi thành `/`. Nhờ vậy không ai tự chế được dòng log của
chính mình, và `grep` theo tên luôn đếm đúng số sự kiện:

```bash
grep ' | AnhHung | ' /opt/kpah/hack.txt | wc -l
awk -F' \\| ' '{print $4}' /opt/kpah/hackdo.txt | sort | uniq -c | sort -rn
```

## Hạn mức ghi — đọc phần này trước khi thêm điểm mới

Mỗi nhân vật tối đa **20 dòng/giờ**, cả server tối đa **500 dòng/giờ**. Chạm trần
thì ghi một dòng báo rồi im đến giờ sau.

Không phải cẩn thận thừa. Ngày 29/08/2026 `log/game.out` trên VPS phình lên
**146 MB**; đọc ra thì **326.826 trên 2.437.569 dòng** là *cùng một* vết gọi lặp
lại — một `ArrayIndexOutOfBoundsException` bị in bởi khối `catch` nằm trong vòng
lặp chạy của map (`Region.java:186`, mỗi 300 ms).

Các điểm phát hiện gian lận cũng nằm trong đường xử lý gói tin. Nếu một phép kiểm
báo động giả liên tục — độ trễ mạng, dữ liệu cũ — nó sẽ ghi không ngừng. Bộ đếm
được tách thành hàm thuần để kiểm thử được, xem
[`test/KiemNghiVanGianLan.java`](../test/KiemNghiVanGianLan.java).

## Các điểm đang ghi

| Loại | Ở đâu | Khi nào |
|---|---|---|
| `hackchonchar` | `cmd/SelectCharHandler.java` | Chọn nhân vật thuộc tài khoản khác |
| `ngapgoi` | `io/Session.java` | Vượt 300 gói thường / 600 gói tài nguyên trong 5 giây |
| `hackdoimap` | `real/MapXuLyLenh.java` | Đổi map trái phép từ trong map offline |
| `hackc` | `real/MapTown.java`, `real/MapLienDau.java` | Giao thẻ khi không ở đúng map / đúng nước |
| `loiinvent` | `data/DatabaseNhanVat.java` | Lưu hành trang thất bại → server tự khoá nhân vật |
| `hackbitem` | `real/OfflineMap.java` | Mua món không có trong danh sách bán của gian hàng |
| `hackR` | `real/Map.java` | Gọi lệnh sửa đồ khi đang ở map online |
| `ffbug` | `real/CharTrangBi.java` (4 chỗ) | Phi phong lỗi cũ / hết hạn còn trong túi hoặc rương |
| `hackgd` | `real/MapMuaBan.java` | Giao dịch thuốc: nhận lời mời không tồn tại; hoặc lúc hoàn tất số thuốc hai bên không khớp, bên chuyển không còn đủ, thuốc không được phép giao dịch, vượt 999 (luật ở `real/GiaoDichThuoc.java`) |

Việc ghi database (`saveOrtherLog(..., "hackc")`...) **giữ nguyên**. Database để
thống kê và lọc theo nhân vật, file để đọc nhanh theo dòng thời gian.

## Những chỗ CỐ Ý không ghi

Không phải phép kiểm nào cũng đáng đưa vào file. Ba chỗ dưới đây đuổi người chơi
nhưng **tỉ lệ báo động giả quá cao**, ghi vào là biến file thành rác:

| Ở đâu | Vì sao không ghi |
|---|---|
| `Map.java:4628`, `MapClan.java:288` | Nhặt đồ khi `hp <= 0` → kick. Đây là tranh chấp kinh điển: người chơi bấm nhặt đúng lúc bị đánh chết, gói tin đã rời khỏi máy trước khi máu về 0. Hàng chục lần/ngày với người chơi hoàn toàn bình thường. |
| `CharCapNhat.java:966` | Chặn dịch chuyển bất thường. Với mạng kém thì hàng nghìn lần/ngày. Server chỉ kéo người chơi về chỗ cũ và **không đếm**, nên không tách được người lag với người hack tốc độ. Muốn ghi thì phải có bộ đếm trước. |
| `CharManager.java:262` | `kickPlayer(p, pos)` — tham số `pos` (`"map 3"`, `"char 2"`...) là thứ duy nhất phân biệt kick vì nghi hack với thoát game bình thường, nhưng nó **không hề được đọc** trong thân hàm. Ghi hết ở đây thì mỗi lượt đăng xuất đều thành một dòng. |

## Một lỗ hổng phát hiện chưa vá

Cơ chế chống auto đánh nhanh **đã chết hoàn toàn**. `Char.timeStartHack` được kiểm
ở `CharVongLap.java:461` (quá 30 giây không giải captcha thì kick), nhưng biến đó
**không nơi nào gán giá trị khác 0**. Tương tự `nHit` / `nhit4` / `timeHit` chỉ
được khai báo và đặt lại, không chỗ nào tăng lên; `Char.maxHit = 150` không được
so sánh với gì cả.

Nghĩa là: **auto đánh nhanh hiện không bị chặn gì**. Captcha chỉ còn phát ra được
bằng lệnh chat admin `capcha` (`MapChat.java:217`). Đây là lỗ hổng phát hiện chứ
không phải báo động giả — vá nó là việc riêng, không thuộc phạm vi hai file log này.
