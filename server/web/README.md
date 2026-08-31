# Website KPAH (đăng ký / đăng nhập / kích hoạt)

Trang chủ + hệ thống tài khoản cho máy chủ private KPAH, viết bằng **PHP + MySQL/MariaDB**,
dùng chung database với game server.

## Kết nối vào đâu?

- **Đăng ký** → ghi vào bảng `team_user` trong DB **`account`** (đúng DB mà login server `CheckLoginSocket.jar` kiểm tra).
- Mật khẩu lưu bằng hàm **`PASSWORD()`** của MySQL/MariaDB để KHỚP với login server
  (login server so khớp: `... and \`password\` = password('...')`).
- **Kích hoạt** → thêm vào bảng `5h_active` trong DB **`kpah_db`** (đúng bảng mà server kiểm tra ở `check_active`).
- **Bảng xếp hạng / nhân vật** → đọc từ `kpah_db` (`tob_char`, `board_created`).

## Cài đặt (Windows + XAMPP — khớp môi trường của bạn)

1. Cài **XAMPP** (hoặc dùng MySQL/MariaDB sẵn có). Bật **Apache** + **MySQL**.
2. Import 2 database (nếu chưa có) qua phpMyAdmin:
   - Tạo DB `account`, import `loginServer/account.sql`.
   - Tạo DB `kpah_db`, import `sql/gamedb7_clean.sql` (bản dump đã lọc sạch dữ liệu
     người chơi, giữ nguyên cấu trúc 84 bảng + toàn bộ dữ liệu game). Đọc phần
     đầu file đó: sau khi import có 3 việc bắt buộc phải làm.
3. Chép **cả thư mục `web/`** vào `C:\xampp\htdocs\` (ví dụ `htdocs/kpah`).
4. Tạo **`web/config.local.php`** từ mẫu rồi điền giá trị thật:

   ```bash
   cp web/config.local.php.example web/config.local.php
   ```

   **Không sửa `web/config.php`** — nó nằm trong git, sửa tay trên VPS sẽ chặn
   deploy lần sau (xem mục "Bí mật riêng của từng máy" bên dưới).

   Thông tin database **thường không phải khai**: `config.php` tự đọc từ
   `server.ini` của game server (xem mục "Thông tin database" bên dưới). Chỉ khai
   `DB_*` khi chép riêng `web/` sang `htdocs` — lúc đó thêm cả `SERVER_DIR` trỏ về
   thư mục dự án để trang Chẩn đoán đọc được file log.

   Còn lại sửa trong `config.php` như bình thường (chúng không phải bí mật):
   link tải game `LINK_ANDROID` / `LINK_PC` / `LINK_IOS` / `LINK_GROUP`, và
   `AUTO_ACTIVATE` (true = đăng ký xong vào game được luôn).
5. Truy cập: `http://localhost/kpah/` (hoặc IP máy bạn để người khác vào).

## Yêu cầu

- PHP 7.4+ (khuyến nghị 8.x) với extension **pdo_mysql**.
- **MariaDB** hoặc **MySQL 5.7** (vẫn còn hàm `PASSWORD()`).
  ⚠️ **MySQL 8.0 đã bỏ `PASSWORD()`** — nếu dùng MySQL 8 phải đổi cách băm cho khớp login server.
  Máy chủ của bạn đang dùng MariaDB 10.4 nên chạy bình thường.

## Chặn làm hàng loạt (chống spam)

Chạy một lần để bật:

```bash
mysql -u root kpah_db < sql/them_bang_web_chan_spam.sql
```

**Token CSRF không phải là giới hạn số lần.** Một script chỉ cần tải trang để lấy
token rồi gửi form, lặp bao nhiêu lần tuỳ thích. Bảng `web_chan_spam` mới là chỗ đếm.

| Việc | Hạn mức | Đếm cái gì |
|---|---|---|
| `register.php` | 5 tài khoản / IP / giờ | chỉ đếm lần tạo **thành công** — gõ trùng tên vài lần là chuyện bình thường |
| `login.php` | 15 lần **sai** / IP / 15 phút | chỉ đếm lần sai, nên người đăng nhập đúng không bao giờ chạm hạn mức |
| `naptien.php` | 10 đơn / IP / giờ | mỗi lần bấm là một dòng trong `tob_nap_moc` |
| `admin/index.php` | 8 lần sai / IP / 15 phút | dùng bảng `web_admin_log` riêng |

Thiếu bảng thì website **vẫn chạy bình thường**, chỉ lùi về đếm trong session —
yếu hơn nhiều (xoá cookie là đếm lại từ đầu). Trang Chẩn đoán báo rõ đang ở chế độ
nào. Đếm hỏng vì lỗi database thì **cho qua**: thà bỏ lọt còn hơn chặn oan người
chơi thật.

`biChanSpam()` chỉ đếm, không sửa gì. Việc dọn rác nằm ở `ghiLanLam()` theo một hạn
giữ cố định 24 giờ — dọn theo cửa sổ của phép kiểm tra sẽ xoá mất lịch sử mà một
phép kiểm tra khác (cửa sổ dài hơn) trên cùng hành động vẫn còn cần.

## Lưu ý bảo mật

- Đã dùng **PDO prepared statement** (chống SQL injection) và **CSRF token** cho form.
- Hàm `PASSWORD()` của MySQL là băm yếu, nhưng **bắt buộc dùng** để khớp login server hiện tại.
  Nếu sau này bạn nâng cấp cơ chế đăng nhập của server, hãy đổi cả hai nơi cho đồng bộ.
- Cookie phiên đặt `HttpOnly` + `SameSite=Lax`, và `Secure` **khi đang chạy HTTPS**
  (`web/inc/functions.php`). Không đặt cứng `Secure=true`: trên máy chủ còn HTTP thì
  trình duyệt sẽ không gửi cookie nữa và **không ai đăng nhập được**, kể cả bạn.
- Để người ngoài truy cập: mở cổng 80 (Apache) và trỏ tên miền / IP public vào máy.

### Bảo vệ khu quản trị

Ẩn đường dẫn `/admin` **không phải** một lớp bảo vệ — máy quét thư mục tìm ra trong
vài giây. Hai thứ thật sự chặn được:

**1. HTTPS.** Nếu máy chủ chỉ mở cổng 80, mỗi lần đăng nhập admin thì tên và mật khẩu
đi qua Internet dưới dạng chữ thường; ai trên đường truyền cũng đọc được. Đây là cách
thông tin đăng nhập admin bị lộ trong thực tế. Bật:

```bash
sudo /opt/kpah/vps/bat-https.sh                  # chứng chỉ tự ký (theo IP)
sudo /opt/kpah/vps/bat-https.sh admin.abc.com    # chứng chỉ thật (Let's Encrypt)
```

Script chỉ **thêm** cổng 443 và chỉ ép HTTPS cho `/kpah/admin` — trang công khai của
người chơi vẫn chạy cổng 80, nên không người chơi nào gặp cảnh báo chứng chỉ.

**2. Xác thực hai bước (TOTP).** Biết mật khẩu vẫn chưa vào được: phải có thêm mã 6 số
từ điện thoại, đổi 30 giây một lần, mỗi mã dùng **đúng một lần**.

```bash
cd /opt/kpah
sudo -u www-data php web/admin/tao_ma_2buoc.php <tên tài khoản>   # ghi danh
sudo -u www-data php web/admin/tao_ma_2buoc.php --danh-sach       # xem ai đã bật
```

**Ghi danh chỉ làm được qua SSH, cố ý như vậy.** Nếu để người gõ đúng mật khẩu tự ghi
danh trên web thì kẻ trộm được mật khẩu cũng tự ghi danh được bằng điện thoại của nó —
lúc đó 2FA không chặn ai cả, chỉ thêm một bước cho chủ nhà. Web **không có** đường nào
tạo hay đổi khoá bí mật, và `kiem_hai_buoc.php` có một phép kiểm giữ đúng điều đó.

Ghi danh xong cho tất cả người trong `ADMIN_ACCOUNTS` thì thêm vào `config.local.php`:

```php
define('ADMIN_2FA_BAT_BUOC', true);
```

Từ đó tài khoản nào chưa có khoá là **không vào được**, kể cả đúng mật khẩu. Chưa đặt
dòng này thì người chưa ghi danh vẫn vào được chỉ bằng mật khẩu — **đừng dừng ở bước
ghi danh**. Trang Tổng quan hiện cảnh báo đỏ cho tới khi cả hai việc trên xong.

**Mất điện thoại:** dùng một trong 8 mã dự phòng in ra lúc ghi danh (mỗi mã một lần).
Hết mã thì vào máy chủ bằng SSH, `DELETE FROM web_admin_2fa WHERE admin_user='tên';`
rồi ghi danh lại. Có SSH là luôn còn đường về, nên **không cần cửa hậu nào trên web**.

Mã sai bị tính vào bộ đếm khoá 8 lần / 15 phút y như mật khẩu sai. Bắt buộc phải vậy:
mã chỉ có sáu chữ số, không đếm thì một script dò được cả dải trong vài giờ.

## Bộ chọn vật phẩm của trang Giftcode

Ô "Phần thưởng vật phẩm" trước đây là một ô trống, phải tự nhớ id — không nhớ được là
không tạo được mã. Mà id nằm rải ở **năm** nguồn khác nhau:

| Nguồn | Số dòng | Loại thưởng |
|---|---|---|
| `data_item` | 914 (77 trong đó là "thời trang", lọc bằng cột `atb9`) | `ITEM`, `TT` |
| `data_potion` | 162 | `PT` |
| `Map.gemTemplate` — **ghi cứng trong Java, không có trong CSDL** | 270 | `GEM` |
| `Map.petTemplate` — **ghi cứng trong Java, id là CHỈ SỐ MẢNG** | 30 | `PET`, `PETV` |
| `DanhHieu.getName()` — **ghi cứng trong một switch Java** | 48 | `DH` |

Riêng "trứng" đã nằm ở cả ba nguồn đầu: trứng thú là `data_item` type 26, trứng linh thú
là type 21, còn trứng phượng hoàng / hoả kỳ lân / đương khang / bạch cốt / lân sư tử lại
là `data_potion`.

**Bộ chọn là bộ sinh chuỗi, không phải đường đi riêng.** Nó ghép chuỗi rồi chèn vào
chính ô văn bản cũ; chuỗi vẫn qua `kiemTraItem()` y như khi gõ tay. Nên nó không thể
tạo ra một loại dữ liệu sai mới, và ai quen gõ tay vẫn làm được như trước.

Danh mục **nhúng thẳng vào trang, không AJAX**: chỉ khoảng một nghìn lựa chọn sau khi
lọc, còn một endpoint AJAX là một đường vào mới phải tự lo `requireAdmin` và CSRF.

### Đã lọc bớt những gì

- **Bỏ đồ chế ở NPC thợ rèn thần bí** — vũ khí (type 3–7), trang bị (0,1,2 và 8–11),
  ngọc trang bị (12), trang bị thú (14–18). Đó là đồ người chơi tự làm bằng nguyên liệu,
  không phải thứ đem phát. Bỏ đi thì danh sách vật phẩm còn **183** món thay vì 914.
  Vẫn phát được nếu thật cần: gõ tay `ITEM:<id>:<số lượng>`.
- **Nhóm sẵn ngọc nguyên liệu theo tên gọi thật**: `NLSC1`–`NLSC6`, `NLCC1`–`NLCC6`,
  mỗi nhóm 5 hệ. Ví dụ **NLCC6** = Tơ lụa / Bạc / Thuỷ tinh / Gỗ sưa / Da cứng cấp 6,
  tức id 108, 115, 122, 129, 136 — không ai nhớ dãy số đó.

### Ba lỗi tìm ra khi rà lại theo yêu cầu người dùng

Cột `data_item.type` không phải lúc nào cũng khớp hành vi thật của id đó trong game —
ba trường hợp sau bị bắt và sửa:

- **950–954** mang `type=19` (Phi phong) nhưng tên "Skin Phúc Thú..." — style trùng hệt
  "Phi phong Lăng Vân" nên chạy được như phi phong thường, chỉ là tên hứa đổi hình thú
  cưỡi mà nó không làm. Đã loại khỏi bộ chọn (`dmIdRac()`).
- **728** "Gậy Giáng Sinh" mang `type=27` (Thú nuôi) nhưng `Item.java` có hằng số
  `GAY_GIANG_SINH=728` xử lý nó như vũ khí lễ hội — mọi thú nuôi thật đều có `atb0=5`,
  riêng 728 thì không. Đã loại khỏi bộ chọn.
- **898–902** "Cánh" mang `type=29` chung với 24 mảnh cải trang Bát Giới/Sa Tăng, nhưng
  là một hệ trang trí riêng (`Item.isCanh()`) — tách thành nhóm **"Cánh"** riêng, và ban
  đầu gán nhầm loại `ITEM` (Cánh không có chỉ số trong cột `atb`, chỉ số thật chỉ được
  gán khi tạo qua `TT`) — đã sửa lại thành loại `TT`.
- **`ITEM:id:soLuong`** chỉ thật sự cộng dồn số lượng với đúng 12 id mảnh sưu tập
  (848–859). Mọi id khác, server chỉ tạo **đúng 1 món** dù ghi số lượng bao nhiêu —
  nhưng thông báo vẫn in nguyên số đã gõ. Bộ chọn khoá ô "Số lượng" về 1 khi món đang
  chọn không nằm trong 12 id đó.

### Đợt mở rộng thứ hai: Thời trang, Thú nuôi, Thần khí, Danh hiệu

Một đợt rà soát 69 agent kiểm chứng chéo tìm thêm 22 vấn đề ở các loại thưởng còn lại.
Đưa thêm vào bộ chọn những phần xác minh đủ an toàn:

- **Thời trang (`TT`)** — không phải một `type`, mà là cột `atb9 IN (1,2)`, rải trên 4
  type (áo/mặt nạ/nón/chổi bay). Trước đợt này bộ chọn hoàn toàn không có nhóm này, dù
  có thể là loại được phát nhiều nhất. Cố ý bỏ 6 dòng type 22/23/24 dù cũng có `atb9=1`
  — đối chiếu dữ liệu thật thì đó không phải thời trang, chỉ là cách né bị nhân hệ số
  theo class.
- **Thú nuôi (`PET`/`PETV`)** — id ở đây là **chỉ số mảng** `Map.petTemplate` (0–29),
  hoàn toàn không phải id vật phẩm; server đọc thẳng không kiểm biên, id ngoài dải ném
  lỗi giữa lúc phát quà và làm mất luôn dòng log lẫn việc trừ lượt. Tên 30 con thú lấy
  từ tệp sinh `danh_muc_thu.php`, chỉ số 5 và 28 trùng tên "Lồng đèn hoa sen" — đúng vậy
  trong chính mã nguồn, không phải lỗi.
- **Thần khí (`TK`/`TKMAX`)** — chỉ đúng 3 id (890/891/892) được hệ thống nhận diện.
  **Lưu ý chính sách**: `Char.createThanKhi()` đặt cứng `newItem.lock = 0`, bỏ qua tham
  số khoá — thần khí phát qua giftcode **luôn giao dịch được**, không có cách nào khoá.
- **Danh hiệu (`DH`)** — chỉ 34/48 danh hiệu random được bằng cách gõ id `0`; 14 danh
  hiệu còn lại (id 8983 trở lên) phải chọn đúng tên, gõ `0` sẽ không bao giờ ra chúng.

Cố ý **chưa** đưa vào: `AOTT` (chỉ 12 id ghi cứng — dùng `TT` thay thế, an toàn hơn hẳn),
`TKF`/`TKMAXF` (quy tắc dòng ẩn theo màu quá rối để làm vội), `PPRK`/`PPVK` chọn kháng
(chỉ có tác dụng thật với đúng 1 id). Vẫn phải gõ tay cho các loại này.

### Quà giftcode là quà khoá

| Loại | Khoá được không |
|---|---|
| `ITEM` | **luôn khoá**, không có tham số để mở — `Char.createItemSoLuong` đặt `newItem.lock = 1` |
| `TT` | **có chọn, mặc định khoá** — gán thẳng vào `newItem.lock` (1 = khoá, khác chiều dấu của `GEM`) |
| `GEM` | **có chọn, mặc định khoá** |
| `PT` | không có khái niệm khoá — ô thuốc chỉ là một con số đếm trong mảng `potions` |
| `TK`/`TKMAX` | **không bao giờ khoá được** — `Char.createThanKhi()` đặt cứng `lock=0` |

Một cái bẫy đã đọc từ `DatabaseGiftCode.java`: viết `GEM:108:5` (ba phần) thì
`Integer.parseInt(tachTime[3])` ném lỗi và **khối `catch` đặt khoá = true**. Kết quả
tình cờ đúng chính sách, nhưng hành vi đến từ một khối bắt lỗi thì đổi nghĩa ngay lần ai
đó sửa chỗ đó — nên bộ chọn luôn ghi đủ bốn phần: `GEM:108:5:-1`.

### Ba tệp danh mục là SINH RA — đừng sửa tay

Tên 270 ngọc, 30 thú nuôi, 48 danh hiệu đều ghi cứng trong mã nguồn Java, không nằm
trong CSDL nào. Sinh lại sau khi đổi dữ liệu tương ứng:

```bash
java -cp "build/classes;lib/NQSH_5h.jar;lib/mysql-connector-java-5.1.49.jar" SinhDanhMucNgoc
java -cp "build/classes;lib/NQSH_5h.jar;lib/mysql-connector-java-5.1.49.jar" SinhDanhMucThu
java -cp "build/classes;lib/NQSH_5h.jar;lib/mysql-connector-java-5.1.49.jar" SinhDanhMucDanhHieu
```

Phải sinh bằng Java chứ không đọc bằng regex: 160/270 tên ngọc lấy từ hằng số
`data.Text.*` (đọc tệp `.java` sẽ ra `Text.BAO_HIEM_20` thay vì `Bảo hiểm 20%`), còn tên
danh hiệu nằm trong một khối `switch` với các nhánh case-fallthrough dễ đọc nhầm. Ba bộ
`test/KiemDanhMuc*.java` đối chiếu tệp sinh với mã nguồn Java, nên quên chạy lại là bộ
kiểm đỏ chứ không âm thầm lệch tên.

## Thông tin database: đọc thẳng từ `server.ini`

Website **không** giữ bản sao thông tin database. `config.php` đọc thẳng từ hai file
cấu hình mà game server dùng hằng ngày:

| File | Lấy ra |
|---|---|
| `<SERVER_DIR>/server.ini` | `db.host`, `db.name`, `db.user`, `db.password` → DB game |
| `<SERVER_DIR>/loginServer/server.ini` | `db.url` (chứa tên DB tài khoản), `db.user`, `db.password` → DB `account` |

`SERVER_DIR` mặc định là thư mục cha của `web/`, đúng sẵn cho VPS: server chạy với
`WorkingDirectory=/opt/kpah` còn web ở `/opt/kpah/web`.

Nhờ vậy đổi mật khẩu database chỉ phải sửa **một chỗ**. Trước đây chép tay sang
`config.local.php` là tạo bản sao thứ hai để lệch nhau — đổi mật khẩu cho server
là website chết mà không ai nhớ tại sao.

Thứ tự ưu tiên, cao xuống thấp:

1. `config.local.php` — khai tay thì luôn thắng
2. `server.ini` — nguồn thật, tự dò
3. mặc định trong `config.php` — `root` / không mật khẩu, cho máy dev XAMPP

Login server có thể dùng **người dùng database khác** game server; khi hai file ini
khai khác nhau, `inc/db.php` tự dùng đúng cặp cho mỗi database.

Đọc không được thì im lặng rơi xuống mặc định — hai file ini đó thường là
`chmod 640 chown kpah:kpah`, mà PHP chạy dưới `www-data`. Cấp quyền đọc:

```bash
usermod -a -G kpah www-data && systemctl restart php8.2-fpm
```

Trang **Chẩn đoán** có mục *"Nguồn thông tin database"* nói rõ đang đọc được từ
`server.ini` hay đang dùng giá trị khai tay, kèm đúng câu lệnh để sửa.

Giữ nguyên chữ `localhost` chứ không đổi thành `127.0.0.1`: MySQL phân quyền theo
máy khách, và `'kpah'@'localhost'` với `'kpah'@'127.0.0.1'` là **hai quyền khác
nhau** — đổi đi là nhận `Access denied` dù mật khẩu hoàn toàn đúng.

## Bí mật riêng của từng máy — `config.local.php`

`web/config.php` nằm trong git, còn DB password / số TK ngân hàng / API key / mật khẩu
admin thì **KHÁC NHAU giữa máy dev và VPS thật** → không được để trong file đã commit.
Chúng nằm ở `web/config.local.php` (đã thêm vào `.gitignore`, không commit):

```bash
cp web/config.local.php.example web/config.local.php
# rồi sửa config.local.php với giá trị THẬT
```

`config.php` tự `require` file này nếu nó tồn tại. **Không bao giờ sửa tay
`web/config.php` trực tiếp trên VPS** — `deploy-auto.sh` chạy `git status --porcelain`
trước mỗi lần deploy và **từ chối `git pull` nếu repo bị sửa tay**, làm job
"Goi deploy tren VPS" trên GitHub Actions báo lỗi đỏ ở mọi lần push sau đó.

**Nếu VPS đang bị dính lỗi này** (đã lỡ sửa tay `config.php` trên VPS): SSH vào VPS,
copy các giá trị thật đang có trong `config.php` sang `config.local.php` (theo mẫu
`config.local.php.example`), rồi trả `config.php` về đúng bản gốc trong git:

```bash
sudo -u kpah git -C /opt/kpah checkout -- web/config.php
sudo -u kpah git -C /opt/kpah status --porcelain   # phải ra rỗng
```

## Nạp tiền theo mốc (49k / 99k / 299k) + NPC Tổng Quản

Tính năng "3 mốc tân thủ": người chơi vào `naptien.php` chọn mốc, quét mã QR VietQR
chuyển khoản TPBank, sau khi thanh toán vào game gặp **NPC Tổng Quản** chọn
**"Nhận mốc nạp"** để nhận quà (mốc 3 được chọn Phượng Hoàng hoặc Phượng Hoàng Băng).

Cài đặt:

1. Bảng `tob_nap_moc` (theo dõi trạng thái mốc nạp) đã được tạo sẵn trong PHẦN 2I của
   [vps/db_new_server.sql](../vps/db_new_server.sql) khi dựng database `kpah_db`. Nếu server
   đã chạy từ trước lúc chưa có bảng này, chạy riêng khối `CREATE TABLE IF NOT EXISTS
   tob_nap_moc` ở đó vào `kpah_db`.
2. Mở `web/config.local.php` (xem mục "Bí mật riêng của từng máy" phía trên), điền đúng:
   - `TPBANK_ACCOUNT_NO`, `TPBANK_ACCOUNT_NAME` (số tài khoản + tên chủ TK TPBank nhận tiền).
   - `ADMIN_NAPTIEN_PASSWORD` (đổi mật khẩu mặc định trước khi public).
3. **Xác nhận chuyển khoản** — 2 cách, có thể dùng song song:
   - **Tự động (khuyến nghị)**: đăng ký [SePay](https://sepay.vn), liên kết tài khoản
     TPBank, thêm Webhook trỏ tới `https://ten-mien/webhook_sepay.php`, đặt API Key
     trên SePay và dán y hệt vào `SEPAY_WEBHOOK_APIKEY` trong `config.php`.
   - **Thủ công**: để `SEPAY_WEBHOOK_APIKEY` trống, vào `admin_naptien.php`
     (nhập `ADMIN_NAPTIEN_PASSWORD`) để tự kiểm tra TPBank rồi bấm xác nhận từng đơn.
4. Phía server game (Java) đã có sẵn: NPC Tổng Quản (npcType `-10`) tự thêm mục
   "Nhận mốc nạp" — không cần chỉnh gì thêm ở server, chỉ cần build lại và chạy.

## Trang quản trị — `web/admin/`

Khu riêng để **xem thống kê người chơi, khoá tài khoản, tạo giftcode và đọc log**.
Địa chỉ: `https://ten-mien/admin/` (không có liên kết nào từ trang công khai trỏ tới).

### Bật lên (2 bước)

1. **Tạo bảng nhật ký** — chạy một lần:

   ```bash
   mysql -u root kpah_db < sql/them_bang_web_admin_log.sql
   ```

   Bảng `web_admin_log` ghi lại mọi thao tác (ai, làm gì, lúc nào, từ IP nào) và
   là chỗ đếm số lần gõ sai mật khẩu để chặn dò. Thiếu bảng thì trang vẫn chạy
   nhưng không để lại dấu vết, và việc chặn dò chỉ đếm trong session — xoá cookie
   là hết.

2. **Cấp quyền** — mở `web/config.local.php` trên máy chủ và thêm:

   ```php
   define('ADMIN_ACCOUNTS', 'hoahv,admin');
   ```

   Đó là **tên tài khoản đăng nhập** (`account.team_user.username`), *không phải*
   tên nhân vật. Người vào vẫn phải gõ đúng mật khẩu tài khoản game của mình —
   danh sách chỉ quyết định ai được phép.

   Để trống = **không ai vào được**. Đây là mặc định có chủ ý: quên cấu hình thì
   trang tự khoá, chứ không mở toang cho mọi người có tài khoản.

### Có gì

| Trang | Làm được gì |
|---|---|
| **Tổng quan** | Tổng tài khoản / đăng ký hôm nay–7–30 ngày, số bị khoá, số chưa kích hoạt; nhân vật vào game 24h/7/30 ngày; biểu đồ 14 ngày (đăng ký + hoạt động); phân bố nghề và cấp độ; top cấp / top lượng / top nạp; tài khoản mới nhất; thao tác admin gần nhất |
| **Tài khoản** | Tìm theo tên / email / SĐT / ID · **khoá – mở khoá đăng nhập** · kích hoạt – huỷ kích hoạt · đổi mật khẩu · xem mọi nhân vật của tài khoản |
| **Nhân vật** | Tìm theo tên hoặc ID tài khoản · **khoá – mở khoá từng nhân vật** · **đặt cấp độ** (chỉ tác dụng khi nhân vật offline) · xem cấp, lượng, xu, lần vào cuối, bang hội, cờ nghi vấn · **danh sách vật phẩm**: đồ đang mặc, thời trang, hành trang, rương |
| **Cấm theo máy** | Cấm đăng nhập theo **mã máy** thay vì theo tài khoản · xem một máy đã dùng những tài khoản nào (và ngược lại) · lọc ra máy dùng nhiều tài khoản bất thường |
| **Sự kiện** | Bật / tắt **mọi sự kiện trong game** bất kỳ lúc nào (Tết, Noel, Trung Thu, Halloween, Giỗ Tổ, Hè, Vu Lan, World Cup…) · **hẹn giờ mở và đóng** từng sự kiện · so trạng thái đang chạy trên server với thiết lập vừa đặt · **server tự khởi động lại** sau khi gom các thay đổi (không cần sudo/ssh, trang chỉ ghi CSDL) · trước đây mốc thời gian ghi cứng trong `Char.java` nên không sự kiện nào mở lại được |
| **Giftcode** | Tạo / sửa / tắt / xoá mã · **bộ chọn vật phẩm theo tên, không cần nhớ id** · **kiểm tra cú pháp phần thưởng trước khi lưu** · xem ai đã nhận mã nào · cho một nhân vật nhận lại |
| **Log game** | 8 bảng log server tự ghi (vật phẩm, ngọc, mua shop, mua kho, bang hội, tiêu lượng, nạp/xài lượng, xổ số), lọc theo nhân vật |
| **Chẩn đoán & Lỗi** | 13 phép kiểm tra sức khoẻ, thống kê lỗi server ghi vào DB, xem cuối các file log của server, lỗi của chính trang web — xem mục dưới |
| **Nhật ký admin** | Mọi thao tác trên trang quản trị. Không xoá được từ giao diện |

### Giao diện

Khu quản trị **không dùng `assets/style.css` của trang giới thiệu**. Bảng màu vàng–nâu
bên đó sinh ra để quảng bá game (nền đỏ loang, nút gradient, chữ nâu nhạt trên nền nâu);
đọc bảng số liệu thì tương phản kém và mọi thứ cùng một sức nặng. Toàn bộ giao diện nằm
gọn trong `web/admin/assets/admin.css`, tự đủ.

Nguyên tắc: **màu chỉ dùng khi nó mang nghĩa** — xanh lam = bấm được / đang chọn,
xanh lá = bình thường, hổ phách = nên xem, đỏ = cần xử lý. Ngoài bốn thứ đó là thang xám.

Bố cục: menu dọc bên trái chia nhóm, thanh trên mỏng chỉ đường, khung nội dung rộng
1360px (khung cũ 1080px làm bảng bảy cột phải cuộn ngang ngay trên màn hình lớn), tiêu đề
bảng dính lại khi cuộn, số căn phải bằng chữ số đều nhau. **Không một dòng JavaScript và
không tải gì từ Internet** — biểu tượng là SVG nhúng thẳng, biểu đồ thuần CSS — vì máy chủ
game có thể không ra được mạng.

Đổi giao diện thì chạy `php web/admin/test/kiem_giao_dien.php`: nó quét mọi `class="..."`
trong các trang rồi đối chiếu với admin.css. Một lớp bị bỏ sót **không gây lỗi nào cả**,
chỗ đó chỉ lặng lẽ mất định dạng — nên không có bộ kiểm này thì không ai biết.

### Cấm theo máy

Cần chạy một lần: `mysql -u root kpah_db < sql/them_bang_cam_may.sql`, và máy chủ phải
chạy bản jar có `data/CamTheoMay`.

Mã máy do **client Unity** gửi (`SystemInfo.deviceUniqueIdentifier`, `Client/Canvas.cs:157`).
**Không phải địa chỉ MAC** — từ Android 10 / iOS 7 ứng dụng không đọc được MAC thật nữa, và
máy chủ thì không bao giờ nhìn thấy MAC của người kết nối: MAC bị ghi đè ở mỗi chặng router,
nên máy chủ chỉ thấy MAC của cổng ra của chính nó.

Server đọc mã máy ở **trường cuối cùng** của gói đăng nhập, **bên trong khối `try` sẵn có** —
client cũ không gửi thì `readUTF()` ném EOF, bị nuốt, và bỏ qua. Nhờ vậy đẩy server trước rồi
build client sau đều được.

Lệnh cấm có hiệu lực **chậm nhất 60 giây**: server giữ danh sách trong RAM và làm mới mỗi
phút. Cố ý — tra database mỗi lần ai đó đăng nhập thì một trận dò mật khẩu sẽ thành một trận
dội database.

Bảng `tob_may_dang_nhap` mới là thứ đáng giá: cấm tài khoản thì kẻ phá lập cái mới trong 30
giây, còn bảng này cho thấy *một máy đã dùng những tài khoản nào*.

**Giới hạn:** giá trị do client tự khai nên nằm trong tay người dùng — chặn được người phá
lười, không chặn được người biết sửa client. Cài lại ứng dụng có thể sinh mã mới. Máy dùng
chung thì cấm một người là chặn cả nhóm. Và hoàn toàn vô dụng với DDoS: kẻ tấn công không
đăng nhập, chỉ mở socket.

Máy nào từng đăng nhập tài khoản quản trị thì trang từ chối cấm — để bạn không tự khoá mình.

### Vật phẩm của nhân vật nằm ở đâu

Ở **trang chi tiết một nhân vật** (`admin/nhanvat.php?id=…`), không phải ở bảng liệt kê.
Bấm tên nhân vật để vào; tìm ra đúng một nhân vật thì trang tự mở thẳng chi tiết.

Máy chủ này **không có bảng `tob_item`** — trang bị, hành trang và rương được serialize
vào ba cột `equip`, `inven`, `bag` của `tob_char`, mỗi món một chuỗi 33 trường ngăn bằng
dấu phẩy, các món nối nhau bằng `>`. Bộ đọc nằm ở `web/admin/inc/vatpham.php`; vị trí
từng trường lấy theo `Item.getDBInfo()` trong `src/real/Item.java`, nên **đổi thứ tự
trường bên Java là phải sửa cả hằng `VP_*` bên PHP** — `php web/admin/test/kiem_vatpham.php`
canh đúng chỗ đó.

Đây là **ảnh chụp lần lưu gần nhất**: người chơi đang online thì đồ thật nằm trong RAM
của server, chỉ khớp sau khi họ thoát game hoặc server tự lưu. Đá quý và nguyên liệu ở
bảng riêng nên không hiện ở đây.

### Trang Chẩn đoán gom bốn nguồn lỗi

| Nguồn | Ở đâu |
|---|---|
| Lỗi Java | `errorlog.txt` (`util.Logger.logError`) và `log/game.out` (systemd hứng stdout/stderr) |
| Lỗi server ghi vào DB | `tob_log_all_item` / `tob_log_all_gem`, cột `aclog`: `loiinvent`, `hackc`, `hackR`, `hackbitem`, `hackchonchar`, `black`, `ffbug`, `timeout` |
| Mất vật phẩm | `tob_log_all_item`, `aclog = 'matdo'` — nghiền đồ, bán tự động, admin xoá tay, server xoá lúc nạp nhân vật. Các đường **chuyển nhượng** có mã riêng: `sitems` (bán cho NPC), và bảng `tob_log_buy_store` (bán ở gian hàng người chơi) |
| Dữ liệu lệch | không ai báo — trang tự đi soi (xem danh sách dưới) |
| Lỗi trang web | `web_admin_log` với `hanh_dong = 'loi_web'` |

13 phép kiểm tra, xếp mức nặng lên trước, mỗi phép kèm câu SQL sửa:

1. **Sức chứa cột hành trang** — `inven`/`bag`/`tuido`/`equip` còn là `TEXT` (trần 65.535 byte ≈ 140 món) hay đã nới `MEDIUMTEXT`. Đây chính là cái bẫy đã nổ ngày 28/07/2026: lưu hỏng → server *tự* đặt `ban = 1` → nhân vật biến mất khỏi màn chọn.
2. **Nhân vật sắp đầy hành trang** — ai đã dùng quá 80% sức chứa, tức lần lưu kế tiếp có thể hỏng.
3. **Lỗi lưu hành trang** — nhân vật nào từng có dòng `loiinvent`.
4. **Nhân vật đang bị khoá** — nhắc rằng không phải cái nào cũng do admin khoá.
5. **Giftcode trùng tên** — server dùng `LIMIT 1` nên dòng sau vĩnh viễn chết.
6. **Cú pháp phần thưởng của các mã đang có** — chạy bộ kiểm tra lên *dữ liệu thật*.
7. **`5h_active` trùng dòng** · 8. **kích hoạt mồ côi** · 9. **nhân vật mồ côi tài khoản**.
10. **Giá trị `ban` lạ** — giá trị ngoài 0/1/3 rơi vào nhánh cuối của login server và được coi như *bình thường*: đặt nhầm là tưởng đã khoá mà thật ra chưa.
11. **Bảng phụ có đủ chưa** · 12. **cờ nghi vấn `nhack`/`blacklist`** · 13. **đọc được file log của server không**.

Đọc file log lấy khoảng **64 KB cuối** bằng `fseek` chứ không nạp cả file — `game.out` có thể vài trăm MB. Thư mục lấy từ hằng `SERVER_DIR`, mặc định là thư mục cha của `web/` (đúng cho VPS vì server chạy với `WorkingDirectory=/opt/kpah` còn web ở `/opt/kpah/web`). Chạy thử trên máy khác cây thư mục thì đặt lại trong `config.local.php`:

```php
define('SERVER_DIR', 'C:/Users/ADMIN/Desktop/KPAH');
```

Trang **chỉ đọc**, và chỉ đọc đúng 10 tên file viết sẵn trong mã nguồn — tên file không bao giờ lấy từ đường dẫn, nên `?file=../config.local.php` không lần ra mật khẩu database được.

### Khoá tài khoản khác gì khoá nhân vật

| | Khoá **tài khoản** | Khoá **nhân vật** |
|---|---|---|
| Cột | `account.team_user.ban = 1` | `kpah_db.tob_char.ban = 1` |
| Người chơi thấy | “Tài khoản của bạn hiện đang bị khoá” khi đăng nhập | Nhân vật **biến mất** khỏi màn chọn nhân vật |
| Còn đăng nhập được? | Không | Có — chỉ mất nhân vật đó |
| Xếp hạng | — | Bị loại khỏi mọi bảng xếp hạng |

**Cả hai đều chỉ ăn từ lần vào sau.** Người đang online vẫn chơi tiếp cho tới khi
họ tự thoát — trang web không nói chuyện được với tiến trình game đang chạy. Muốn
đá ra ngay thì dùng bảng điều khiển trong game (`AdminPanel`).

### Đặt cấp độ — rủi ro KHÁC HẲN khoá nhân vật, không phải "chỉ chậm có hiệu lực"

Khoá nhân vật an toàn khi online vì cột `ban` không nằm trong câu `UPDATE` lưu
nhân vật định kỳ — server không bao giờ ghi đè lại nó. **`lastLv`/`xp` thì có.**
Nhân vật đang online giữ một bản cấp độ cũ trong RAM của tiến trình game; lần
server tự lưu tiếp theo (định kỳ, hoặc lúc thoát) sẽ ghi đè NGUYÊN bản cũ đó lên
đúng dòng vừa sửa — không phải "chậm có hiệu lực" mà là **mất hẳn thay đổi mà
không có cảnh báo nào**. Trang web không có cách nào biết một nhân vật có đang
online hay không (`CharManager` chỉ giữ trong RAM của Java, không ghi ra CSDL),
nên không tự chặn được — chỉ cảnh báo rõ trên trang, người vận hành tự xác nhận
nhân vật đang offline trước khi bấm.

Chỉ cần đặt `lastLv`; **không đụng đến chỉ số nào khác**. `SelectCharHandler.java`
tự tính lại sức mạnh/nhanh nhẹn/HP/điểm kỹ năng từ `lastLv` ngay lúc chọn nhân
vật, giống hệt lên cấp tự nhiên. Cột `xp` chỉ được đặt kèm cho khớp mốc "vừa lên
cấp" (0% vào cấp tiếp theo) để thanh kinh nghiệm không hiện số âm — tra từ
`web/admin/inc/capdo.php`, tệp **sinh ra** từ `real.LevelDetail.expMain`
(`tools/SinhBangCapDo.java`, đối chiếu lại bằng `test/KiemBangCapDo.java`), không
gõ tay: mảng có 91 số dài tới 14 chữ số, gõ nhầm một chữ số không ai nhìn ra được
bằng mắt.

### Vài chốt an toàn đã cài sẵn

- Không khoá được tài khoản nằm trong `ADMIN_ACCOUNTS` — kể cả của chính mình.
  Đây là thứ duy nhất ngăn việc tự nhốt mình ra ngoài (mở lại phải sửa thẳng DB).
- Không đổi được mật khẩu của một admin khác.
- Gõ sai mật khẩu 8 lần trong 15 phút từ cùng một IP thì bị chặn tạm.
- Phiên tự hết hạn sau 60 phút không thao tác; gỡ tên khỏi `ADMIN_ACCOUNTS` là
  phiên đang mở mất quyền ngay lần bấm kế tiếp.
- Mọi form đều có CSRF token; mọi truy vấn đều dùng prepared statement.
- Tên mã giftcode và chuỗi phần thưởng bị lọc ký tự chặt, vì **server ghép thẳng
  hai thứ đó vào câu SQL** khi ghi `giftcode_log` — một dấu nháy đơn lọt vào là
  hỏng câu lệnh của server.

## Danh sách file

```
web/
├── config.php          # SỬA Ở ĐÂY: DB + thiết lập site
├── index.php           # Trang chủ (hero, tải game, BXH, tin tức)
├── register.php        # Đăng ký (ghi team_user + PASSWORD())
├── login.php           # Đăng nhập website
├── account.php         # Trang tài khoản + nút Kích hoạt
├── logout.php
├── naptien.php          # Nạp tiền theo mốc + hiển thị QR VietQR
├── order_status.php     # API polling trạng thái 1 đơn nạp mốc (JSON)
├── webhook_sepay.php    # Webhook SePay xác nhận chuyển khoản tự động
├── admin_naptien.php    # Trang admin xác nhận chuyển khoản thủ công
├── assets/style.css    # Giao diện tối võ hiệp
├── inc/
│   ├── db.php          # Kết nối PDO
│   ├── functions.php   # Helper (validate, csrf, kích hoạt)
│   ├── header.php      # Header + menu
│   └── footer.php
└── admin/              # KHU QUẢN TRỊ (xem mục ở trên)
    ├── index.php       # Đăng nhập + bảng tổng quan / thống kê
    ├── taikhoan.php    # Khoá, kích hoạt, đổi mật khẩu tài khoản
    ├── nhanvat.php     # Khoá / mở khoá nhân vật · đặt cấp độ
    ├── cammay.php      # Cấm theo mã máy + tra cứu máy ↔ tài khoản
    ├── sukien.php      # Bật / tắt mọi sự kiện trong game (bảng tob_su_kien)
    ├── giftcode.php    # Tạo & quản lý mã quà, xem ai đã nhận
    ├── log.php         # Xem 8 bảng log của game server
    ├── chandoan.php    # Chẩn đoán & lỗi: 13 phép kiểm tra + đọc file log server
    ├── nhatky.php      # Nhật ký thao tác của admin
    ├── dangxuat.php
    ├── assets/admin.css  # Toàn bộ giao diện khu quản trị — tự đủ, không dùng style.css
    ├── inc/
    │   ├── boot.php    # Đăng nhập, phân quyền, ghi nhật ký, chặn dò mật khẩu
    │   ├── hai_buoc.php # Xác thực hai bước TOTP (RFC 6238), mã dự phòng
    │   ├── danh_muc_thuong.php # Danh mục + giải thích chuỗi cho bộ chọn Giftcode
    │   ├── danh_muc_ngoc.php   # 270 tên ngọc — SINH RA từ Java, đừng sửa tay
    │   ├── danh_muc_thu.php    # 30 tên thú nuôi — SINH RA từ Java, đừng sửa tay
    │   ├── danh_muc_danh_hieu.php # 48 tên danh hiệu — SINH RA từ Java, đừng sửa tay
    │   ├── capdo.php   # Bảng cấp độ → kinh nghiệm — SINH RA từ Java, đừng sửa tay
    │   ├── layout.php  # Khung trang (menu trái + thanh trên) + biểu đồ thuần CSS
    │   └── thuong.php  # Bảng 61 loại phần thưởng + kiểm tra cú pháp giftcode
    └── test/           # Kiểm thử, không cần MySQL
        ├── kiem_giftcode.php
        ├── kiem_schema.php
        ├── kiem_duong_dan.php
        ├── kiem_doc_ini.php
        ├── kiem_chan_spam.php
        ├── kiem_log_online.php
        ├── kiem_vatpham.php
        ├── kiem_giao_dien.php
        ├── kiem_hai_buoc.php
        ├── kiem_danh_muc_thuong.php
        ├── kiem_capdo.php
        └── chan_doan_ket_noi.php
```

### Kiểm thử (không cần MySQL chạy)

> **Các bài kiểm chỉ chạy được bằng dòng lệnh.** `/var/www/html/kpah` là symlink tới
> `/opt/kpah/web`, nên `web/admin/test/` mở được từ Internet — gọi qua HTTP thì bài kiểm
> chạy thật và in ra kết quả, lộ đường dẫn tệp, tên bảng, tên cột. Mỗi tệp vì vậy tự chặn
> bằng `PHP_SAPI !== 'cli'`, và các thư mục không có trang chỉ mục đều có `index.php` trả
> 403 để Apache khỏi liệt kê. **Không dùng `.htaccess` được**: Apache trên VPS đặt
> `AllowOverride None` nên nó bị bỏ qua hoàn toàn — đặt vào chỉ tạo cảm giác an toàn giả.


```bash
php web/admin/test/kiem_giftcode.php   # bộ kiểm tra cú pháp phần thưởng
php web/admin/test/kiem_schema.php     # đối chiếu mọi câu SQL với cấu trúc bảng thật
php web/admin/test/kiem_duong_dan.php  # chốt chặn đọc file của trang Chẩn đoán
php web/admin/test/kiem_doc_ini.php    # đọc server.ini ra thông tin database
php web/admin/test/kiem_chan_spam.php  # bộ đếm chặn làm hàng loạt
php web/admin/test/kiem_doc_log.php    # hàm đọc phần cuối file log
php web/admin/test/kiem_log_online.php # log số người online: đối chiếu cột Java <-> PHP
php web/admin/test/kiem_vatpham.php    # bộ đọc vật phẩm: đối chiếu vị trí trường với Item.java
php web/admin/test/kiem_giao_dien.php  # mọi lớp CSS các trang dùng đều phải có định nghĩa
php web/admin/test/kiem_hai_buoc.php   # TOTP hai bước: đối chiếu vector chính thức RFC 6238
php web/admin/test/kiem_danh_muc_thuong.php # bộ chọn vật phẩm: mọi chuỗi sinh ra phải qua kiemTraItem()
php web/admin/test/kiem_capdo.php      # bảng cấp độ → kinh nghiệm dùng cho "Đặt cấp độ"

# Chẩn đoán lỗi kết nối database — chạy TRÊN MÁY CHỦ, chỉ bằng dòng lệnh
sudo -u www-data php web/admin/test/chan_doan_ket_noi.php
```

- `kiem_giftcode.php` chạy lại **14 mã giftcode thật đang có trong bản dump** để
  chắc chúng vẫn hợp lệ, và **đối chiếu bảng 60 loại phần thưởng với chính
  `src/data/DatabaseGiftCode.java`**. Thêm một loại thưởng mới bên Java mà quên
  cập nhật `inc/thuong.php` là test này báo đỏ ngay.
- `kiem_schema.php` đọc `sql/gamedb7_clean.sql` + `loginServer/account.sql`, rồi
  soi từng câu SQL trong `web/admin/` xem có bảng/cột nào gõ nhầm không. Lưu ý
  `kpah_db` **cũng** có một bảng tên `team_user` nhưng khác hẳn bảng
  `account.team_user` — test này biết chuyện đó và luôn dùng bảng ở DB `account`.
- `kiem_duong_dan.php` lấy **chính** danh sách file cho phép trong `chandoan.php`
  (không chép lại) rồi ném vào 24 đường dẫn độc — `../config.local.php`,
  `php://filter/...`, byte null, `%2F`… — khẳng định không cái nào lọt, và mọi
  file hợp lệ đều nằm trong `SERVER_DIR`.

### Một lỗi sửa ở phía server Java

`src/data/Database.java` có 4 chỗ bắt lỗi gọi `Logger.logError(e, "")` — tên file
**rỗng**. `new FileWriter("")` ném `IOException` ngay, nên vết gọi không bao giờ
tới `errorlog.txt`; nó rơi ra stdout và chỉ còn thấy được trong `log/game.out`.
Đã đổi thành `Logger.logError(e, "errorlog.txt")` để trang Chẩn đoán nhìn thấy.
**Cần build lại jar và cập nhật server thì thay đổi này mới có tác dụng.**
