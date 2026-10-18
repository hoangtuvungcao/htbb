# Dọn log định kỳ

Giữ N ngày gần nhất, mọi thứ cũ hơn thì bỏ (`GIU_NGAY` ở đầu
[don-log.sh](don-log.sh)). Hai phần rời nhau vì hai loại log cần hai cách khác
nhau:

| Loại | Ai dọn | File |
|---|---|---|
| `game.out`, `login.out`, `*.log`, các file `.txt` server tự ghi | logrotate — `logrotate.timer` của hệ thống 00:00 mỗi ngày, **cộng thêm** một lần kiểm mỗi giờ | [htbb-logrotate.conf](htbb-logrotate.conf) |
| Bảng nhật ký trong database, file `.txt` lẻ đặt tên động, bản backup quá hạn | cron 03:20 mỗi đêm | [don-log.sh](don-log.sh) |

## Cài

```bash
sudo cp /opt/htbb/vps/htbb-logrotate.conf /etc/logrotate.d/htbb
sudo chown root:root /etc/logrotate.d/htbb && sudo chmod 644 /etc/logrotate.d/htbb
sudo logrotate -d /etc/logrotate.d/htbb          # chạy mô phỏng, không đổi gì

sudo tee /etc/cron.d/htbb-don-log >/dev/null <<'EOF'
SHELL=/bin/bash
PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
20 3 * * * root /opt/htbb/vps/don-log.sh >/dev/null 2>&1
17 * * * * root /usr/sbin/logrotate /etc/logrotate.d/htbb >/dev/null 2>&1
EOF
```

Dòng thứ hai **không** thừa — xem bẫy số 5 bên dưới. Nó cố ý không đặt `--state`
để dùng chung `/var/lib/logrotate/status` với lần chạy hằng ngày của hệ thống,
nhờ vậy hai bên không xoay vòng tranh phần nhau.

Chạy tay:

```bash
sudo /opt/htbb/vps/don-log.sh --thu    # chỉ báo sẽ xoá bao nhiêu, không xoá
sudo /opt/htbb/vps/don-log.sh          # xoá thật
```

Số ngày giữ lại là biến `GIU_NGAY` ở đầu [don-log.sh](don-log.sh). Đổi cho một
lần chạy: `sudo GIU_NGAY=7 /opt/htbb/vps/don-log.sh`.

Nhật ký của chính việc dọn: `/opt/htbb/log/don-log.log`.

## Vì sao 03:20

`src/real/BaoTriDaily.java` tự tắt server (`System.exit(0)`) ở một mốc **ngẫu
nhiên giữa 04:30 và 05:30** mỗi ngày, rồi systemd `Restart=always` bật lại. Dọn
lúc 03:20 là để không đâm vào lúc server đang lưu dữ liệu ra database.

## Ba cái bẫy mà cấu hình này né

**1. `game.out` bắt buộc phải `copytruncate`.** `htbb-game.service` dùng
`StandardOutput=append:/opt/htbb/log/game.out` — systemd **mở file một lần** lúc
khởi động rồi giữ nguyên fd đó. Nếu logrotate đổi tên file (cách mặc định),
systemd vẫn ghi tiếp vào file đã đổi tên: file mới mãi mãi rỗng, file cũ vẫn
phình to. `copytruncate` chép nội dung ra rồi cắt file gốc về 0; fd mở bằng
`O_APPEND` nên lần ghi kế tiếp tự quay về đầu file.

**2. Các file `.txt` thì ngược lại — đổi tên hoàn toàn an toàn.**
`util.Logger` (trong `lib/htbb-core.jar`) ghi bằng `new FileWriter(tên, true)`,
tức **mở lại file mỗi lần ghi** rồi đóng ngay. Đổi tên file đi thì lần ghi sau
tự tạo lại. Không cần `copytruncate`, và cũng không mất dòng nào.

**3. Ba file `.txt` KHÔNG được đụng vào** vì server đọc ngược lại chúng như dữ
liệu đầu vào, xoay vòng là mất dữ liệu:

| File | Đọc ở |
|---|---|
| `trangan.txt` | `Database.java:5466` |
| `offline.txt` | `Database.java:11715` |
| `top.txt` | `MapChat.java:868` |

Vì vậy `htbb-logrotate.conf` **liệt kê từng file một**, cố ý không dùng
`/opt/htbb/server/*.txt`. Thêm file mới vào danh sách thì kiểm lại: nó chỉ được **ghi**,
không được **đọc** ở bất kỳ đâu trong `src/`.

**4. Log không được nằm trong git.** `AdminChat.txt` và `updateleaf.txt` trước
đây bị theo dõi (`.gitignore` còn có hẳn dòng miễn trừ `!AdminChat.txt`). Xoay
vòng một file đang được theo dõi là làm `git status` trên VPS **dirty**, mà
`deploy-auto.sh` từ chối `git pull` khi cây làm việc bẩn — lần deploy kế tiếp
sẽ hỏng. Đã gỡ cả hai khỏi git và cho vào `.gitignore`; bản nội dung cũ nằm ở
`/root/htbb-backup/*.truoc-khi-go-git` trên VPS.

Thêm file nào vào `htbb-logrotate.conf` thì kiểm luôn `git ls-files <tên>` —
phải **rỗng**.

**5. `maxsize` chỉ có tác dụng lúc logrotate CHẠY.** `Region.java:186-188` nằm
trong vòng lặp chạy của **mỗi map** và in cả stack trace ra stdout, còn
`Map.DELAY_UPDATE_MAP = 300ms`. Một map dính lỗi lặp lại (ví dụ NPE cố định) bơm
vài chục dòng mỗi giây — hàng triệu dòng một ngày. `CharVongLap.java:622` và
`MapVongLap.java:458` cũng cùng dạng.

`maxsize 20M` không chặn được trong ngày, mà `logrotate.timer` của hệ thống chỉ
chạy 00:00, nên giữa hai lần chạy `game.out` có thể phình lên hàng GB — đĩa chỉ
còn 6 GB. Vì vậy có thêm dòng cron mỗi giờ ở phần Cài: nó giới hạn thiệt hại
xuống một giờ thay vì một ngày.

## Vì sao xoá theo lô chứ không một lệnh DELETE

`tob_log_all_item` là **MyISAM**, mà MyISAM **khoá cả bảng** khi xoá. Một lệnh
`DELETE` quét 4 triệu dòng giữ khoá hàng phút; trong lúc đó mọi `INSERT` log của
server xếp hàng chờ, hàng đợi `Logdata.query` phình to. Script chia 20.000 dòng
một lô, nghỉ 0,4 giây giữa các lô, nên mỗi lô chỉ khoá dưới một giây.

MyISAM xoá dòng xong cũng **không trả lại chỗ ổ đĩa** — nó để lại lỗ hổng trong
`.MYD` cho lần insert sau lấp vào. Bảng vẫn insert đều thì lỗ hổng được tái sử
dụng, nên script chỉ chạy `OPTIMIZE TABLE` khi chỗ trống vượt 200 MB (vì
`OPTIMIZE` cũng khoá bảng, với bảng vài trăm MB có thể mất vài chục giây).

## Bảng nào được dọn

Chỉ dọn được bảng **có cột thời gian**. `giftcode_log` không có cột nào ghi thời
điểm nên không lọc theo tuổi được (mà cũng chỉ vài trăm dòng).

```
tob_log_all_item   timelog        tob_log_clan      timeLog
tob_log_all_gem    timelog        web_admin_log     thoi_gian
tob_log_buy_shop   timeBuy        web_chan_spam     thoi_gian
tob_log_buy_store  timeBuy        history_win_xoso  time_win
tob_log_xoso       timelog
```

Riêng trong `tob_log_all_item`, mọi dòng **không thuộc danh sách giữ** thì xoá hết không cần
đợi qua hạn (mục 1b của script). Từ 02/09/2026 server chỉ còn ghi 54 loại phục vụ ba việc: truy
vật phẩm hiếm bị mất hay đổi chủ, lỗi và nghi vấn gian lận, tài sản tăng bất thường. Khoảng 250
loại còn lại trong mã nguồn không ghi nữa — riêng `luckGOLD` từng chiếm 3,2 triệu dòng trong một
ngày, hơn nửa bảng. Danh sách gốc nằm ở `src/data/LocLogVatPham.java` (`GIU`); bản chép trong
script là biến `LOAI_GIU`, và `test/KiemLocLogVatPham.java` bắt hai bản phải khớp nhau, nên
thêm loại thì sửa cả hai chỗ rồi chạy `test.bat`.

Riêng `tob_log_use_luong_<tuần>_<tháng>_<năm>`: `Database.saveLogThongKeChiTieu`
tạo một **bảng mới mỗi tuần**, không ai xoá — sau một năm là 52 bảng rác. Chúng
không có cột thời gian nên script dọn theo `UPDATE_TIME` mà MyISAM tự ghi lại, và
chỉ đụng vào bảng **có hậu tố**, giữ nguyên bảng gốc `tob_log_use_luong`.

## Cách tìm ra một nơi sinh log mới

Sáu cách, làm hết chứ đừng dùng mỗi một cách:

1. `grep -rn "logString\|logError\|logStringWithDate" src/` — đối số **thứ hai**
   là tên file.
2. Đọc `build/core-src/util/Logger.java` (giải mã từ `htbb-core.jar` bằng
   `tools/giai-ma-core.bat`) để biết nó ghi kiểu gì.
3. `grep -rn "new FileWriter\|new PrintWriter\|new FileOutputStream" src/`
4. `grep -rn "insert into tob_log\|INSERT INTO tob_log" src/` cho phía database.
5. Mục *"File runtime server tự ghi ra thư mục gốc khi chạy"* trong `.gitignore` —
   danh sách đó là manh mối tốt, vì file nào làm `git status` bẩn thì đã bị chặn
   ở đó rồi.
6. Đối chiếu với thực tế trên VPS: `ls -la /opt/htbb/server/*.txt` và
   `SELECT TABLE_NAME, TABLE_ROWS FROM information_schema.TABLES WHERE TABLE_NAME LIKE '%log%'`.

Cách 1 tìm ra tên file, nhưng **không** chứng minh file đó bao giờ được ghi. Phải
kiểm thêm: hàm chứa lệnh ghi có ai gọi không.

Và cách 1 **cũng không đủ**: có những sink mà tên file nằm *trong jar*, không có
trong `src/`. Phải đọc cả `build/core-src/data/Logger.java` (khác
`util/Logger.java`) — nó hard-code bốn tên:

| Hàm | File | Có ai gọi trong `src/`? |
|---|---|---|
| `logError` | `errorlog.txt` | có |
| `logErrorPet` | `errorlogPet.txt` | có — `Pet.java:1208` |
| `logHack` | `hacklog.txt` | **không** |
| `writeToFile` | `serverlog/<yyyyMMdd>.txt` | **không** |

## `hack.txt` và `hackdo.txt` là code chết

Trang Chẩn đoán báo *"chưa có"* và sẽ mãi như vậy:

- `hack.txt` chỉ được ghi ở `Database.banchar()` — **không nơi nào gọi hàm này**.
- `hackdo.txt` không được ghi ở đâu cả, nó chỉ được **đọc** (`Database.java:3703`).

Cả `banchar()` lẫn `bakupChar()` (hàm duy nhất đọc `hack.txt`) đều không có người
gọi. Đây là tàn dư của một công cụ chống gian lận thủ công đời trước: admin tự
liệt kê tên nhân vật vào `hackdo.txt`, chạy `banchar()` để mở khoá hàng loạt rồi
`bakupChar()` để đặt lại chỉ số.

Muốn thật sự có "nghi vấn gian lận" thì phải viết mới, không phải bật lại hai hàm
này — chúng dựng SQL bằng cách nối chuỗi thẳng tên nhân vật, và `banchar()` đặt
`ban=0` (mở khoá) chứ không phải khoá.
