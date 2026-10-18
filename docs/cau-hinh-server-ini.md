# Cấu hình server — `server.ini`

Mọi thiết lập chạy máy chủ nằm trong `server.ini`: cổng, kết nối CSDL, tỉ lệ rơi đồ, giờ
chiếm thành, chống flood, quyền admin.

Tài liệu này là bản tra cứu. Phần dựng server lần đầu xem
[HUONG_DAN_BUILD_VPS.md](HUONG_DAN_BUILD_VPS.md).

> **Từ 18/10/2026:** mọi khoá chỉnh được lúc chạy (và các cờ chế độ) quản lý ở trang web *Cấu hình hệ thống* —
> thứ tự ưu tiên CSDL > `server.ini` > mặc định, áp dụng trong ≤ 10 giây hoặc khi khởi động lại.
> Xem [cau-hinh-trung-tam.md](cau-hinh-trung-tam.md). `server.ini` vẫn là giá trị khởi tạo / dự phòng.

---

## 1. Cấu hình giờ **sinh ra từ pipeline** — sửa trên GitHub

Nguồn duy nhất là [`vps/server.ini.tmpl`](../vps/server.ini.tmpl), **nằm trong git**. Mỗi
lần deploy, [`vps/sinh-server-ini.sh`](../vps/sinh-server-ini.sh) dựng lại
`/opt/htbb/server.ini` từ bản mẫu đó cộng với mật khẩu lấy từ GitHub Secrets.

```
vps/server.ini.tmpl  ─┐
                      ├─→ sinh-server-ini.sh ─→ /opt/htbb/server.ini ─→ khởi động lại
GitHub Secrets  ──────┘        (kiểm tra rồi mới cài)
```

Nghĩa là:

- **Sửa cấu hình = sửa `vps/server.ini.tmpl` rồi commit.** Sửa ngay trên web GitHub cũng
  được. Deploy tự chạy, tự khởi động lại nếu cấu hình thật sự đổi.
- **Đừng sửa tay `/opt/htbb/server.ini` nữa** — lần deploy sau ghi đè.
- Mọi thay đổi cấu hình từ giờ đều có lịch sử git, xem được ai đổi gì, và `git revert` được.

`/opt/htbb/server.ini` vẫn nằm trong `.gitignore` (nó chứa mật khẩu thật sau khi sinh ra).

Hai file khác **không** đi qua pipeline này, vẫn phải sửa tay trên VPS:

| Đường dẫn | Dùng cho |
|---|---|
| `/opt/htbb/server/loginServer/server.ini` | server đăng nhập |
| `HTBB\server.ini` (máy nhà) | server chạy thử ở nhà |

## 2. Đổi một thiết lập

1. Mở [`vps/server.ini.tmpl`](../vps/server.ini.tmpl) trên GitHub, bấm sửa, đổi giá trị, commit.
2. Xem tab **Actions** — deploy chạy ngay.
3. Nếu cấu hình đổi thật thì server báo trước 60 giây trong game rồi khởi động lại.

Xem trước khi commit, chạy trên VPS (không cài gì, chỉ in ra khác biệt):

```bash
sudo /opt/htbb/vps/sinh-server-ini.sh --thu
```

**Bộ sinh không bao giờ cài một cấu hình hỏng.** Trước khi ghi đè nó kiểm: còn chỗ trống
chưa thay không, đủ 20 khoá bắt buộc không, mật khẩu có còn là chuỗi mẫu không, và **thử kết
nối CSDL thật**. Sai bất kỳ điều nào thì báo lỗi, giữ nguyên file đang chạy, không khởi động
lại. Bản cũ được lưu vào `/opt/htbb/backup/server.ini.<ngày giờ>` (giữ 10 bản gần nhất).

### GitHub quản **toàn bộ** — không còn phải mở `server.ini`

Bản mẫu chứa **đủ 69 khoá** mà mã nguồn thực sự đọc, không thiếu không thừa (có phép kiểm
đối chiếu hai chiều với `TeamServer.java` và `CauHinh.java`). Thiếu một khoá là lại phải vào
VPS sửa tay — đúng việc đã làm sập server; thừa một khoá là nút bấm giả, xoay hết cỡ cũng
không có tác dụng.

Vì GitHub quản hết nên: sửa trong game bằng bảng `setting` hay lệnh `stown` **vẫn ăn ngay**,
nhưng lần deploy kế tiếp đặt lại theo bản mẫu. Muốn đổi lâu dài thì sửa bản mẫu.

### Lối thoát `@GIU_NGUYEN@`

Muốn một khoá nào đó để **trong game thắng**, đổi giá trị của nó trong bản mẫu thành
`@GIU_NGUYEN@`. Bộ sinh sẽ không đụng tới khoá đó, giữ nguyên giá trị đang chạy trên VPS.

Hữu ích nhất cho ba khoá giờ chiếm thành nếu bạn hay đổi giờ giữa sự kiện bằng `stown`.
Mặc định đang **không** bật, vì bạn muốn chỉ sửa trên GitHub.

## 3. Mật khẩu CSDL — GitHub Secrets

Mật khẩu **không** nằm trong bản mẫu (bản mẫu ở trong git, ai xem repo cũng đọc được). Chúng
đi riêng qua GitHub Secrets, xuống VPS bằng **stdin của ssh**.

Đặt ở: **Settings → Secrets and variables → Actions → New repository secret**

| Secret | Điền vào khoá |
|---|---|
| `DB_USER` / `DB_PASSWORD` | `db.user` / `db.password` |
| `DB_USER_NAP` / `DB_PASS_NAP` | `db.userNap` / `db.pass` |
| `DB_USER1` / `DB_PASSWORD1` | `db.user1` / `db.password1` |

**Chưa đặt secret nào cũng không sao**: bộ sinh giữ nguyên giá trị đang chạy trên VPS. Nên
bật pipeline này lên không làm sập server, và bạn đặt secret lúc nào cũng được.

Vì sao phải qua stdin chứ không phải biến môi trường: khoá SSH của Actions bị ép chạy đúng
một lệnh (`command="..."` trong `authorized_keys`), và sshd chỉ nhận `AcceptEnv LANG LC_*`.

## 3b. Đổi khi đang chạy, không qua git

Vẫn dùng được cho việc gấp, nhưng **lần deploy sau sẽ đặt lại theo bản mẫu**:

| Cách | Khi nào dùng |
|---|---|
| Bảng `setting` trong game | đổi gấp tỉ lệ rơi / giờ chiếm thành giữa sự kiện |
| Trang quản trị `/admin/cauhinh` | sửa từ xa, không cần SSH — xem bên dưới |
| Sửa `/opt/htbb/server.ini` rồi gõ `setting` → `8` | chỉ với 26 khoá ở mục 4 |

**Từ 09/09/2026, cả ba cách trên đọc/ghi CHUNG một bảng SQL `htbb_db.cau_hinh`** (khoá,
giá_trị, sửa_bởi, sửa_lúc) — không còn ghi thẳng vào `server.ini` nữa (xem
[`server/CauHinh.java`](../server/src/server/CauHinh.java)). `server.ini` vẫn giữ 26 khoá này làm
**giá trị khởi tạo lần đầu** (chép một lần vào bảng lúc server khởi động nếu bảng còn thiếu
dòng, `INSERT IGNORE` nên không ghi đè giá trị đã có); từ đó về sau bảng SQL mới là nguồn
thật. Server tự đọc lại bảng này mỗi 10 giây, nên sửa trên trang web thì trong game áp dụng
sau tối đa 10 giây — không cần khởi động lại, không bắt buộc gõ `setting` → `8` (vẫn dùng
được để nạp lại ngay lập tức thay vì chờ).

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

Chỉ **32 khoá** dưới đây được `CauHinh.napLai()` đọc lại. **Mọi khoá khác chỉ được đọc một
lần lúc bật máy** — sửa xong bắt buộc `systemctl restart htbb-game`.

```
sv.tileXu   sv.tileLuong   sv.tileItemSuKien   sv.tilePotion   sv.tileNgoc   sv.tileItem
sv.chiaXuRoi
sv.tileExpLv50   sv.tileExpDuoiLv50
sv.tieuNguongMau   sv.tieuHeSoExp   sv.tieuPcExpTheoMau   sv.tieuPermilleCaoCap
sv.doiTieuNguongMauXu   sv.doiTieuNguongMauLuong
sv.gioChiemThanh   sv.phutChiemThanh   sv.ngayChiemThanh
sv.adminAccounts   sv.maxCharPerAccount   sv.choPhepTaoTenGM
sv.website   sv.linkTaiGame   sv.testMode   sv.testCode
sv.worldCupBatDau   sv.worldCupKetThuc   sv.worldCupHanNhanQua
sv.suKienTuKhoiDongLai   sv.suKienGomGiay   sv.suKienBaoTruocGiay
sv.expvantieu   sv.Luckyexp   sv.ExpQua
```

### Thưởng EXP phụ (vận tiêu / rương may mắn / cây ăn quả)

Từ 11/09/2026, ba khoá dưới đây đổi ở **trang quản trị `/admin/cauhinh`** thay vì sửa
tay `server.ini` + restart. Trước đó chúng đi qua `Byte.parseByte` bắt buộc trong
`TeamServer.java` (thiếu dòng là ném lỗi khi khởi động); nay đọc qua `CauHinh.napLai()`
với giá trị mặc định an toàn, xoá dòng trong `server.ini` cũng không sao.

| Khoá | Mặc định | Ý nghĩa |
|---|---|---|
| `sv.expvantieu` | `0` | EXP thưởng khi giao tiêu **cá nhân** (`CharSuKien.java`). Tắt thì vẫn nhận vật phẩm thưởng bình thường, chỉ mất phần EXP. |
| `sv.Luckyexp` | `0` | Ô thưởng loại EXP trong **Rương May Mắn** Bạc &amp; Vàng (`InfoGifLucky.java`). Tắt thì ô đó ra 0, hiện "Không có EXP"; các loại thưởng khác (xu, đá quý, thuốc) không đổi. |
| `sv.ExpQua` | `0` | EXP từ **cây ăn quả** loại EXP (`FruitTemplate.java`, `type == 0`). **Lưu ý tác dụng phụ**: `canTangToc()` (nút "Tăng tốc" dùng bình tăng tốc thường/bạc/vàng) cũng đọc đúng cờ này nhưng áp dụng cho **MỌI loại cây** — tắt cờ này khoá luôn nút Tăng tốc cả với cây ra đá quý/thuốc, không riêng cây EXP. Đây là hành vi có sẵn trong mã nguồn, không phải phát sinh từ việc chuyển sang CSDL. |

### Lịch hẹn sự kiện và tự khởi động lại

Đổi trạng thái một sự kiện chỉ có hiệu lực **sau khi khởi động lại máy chủ** — bảng shop
(`Map.loadShop` đặt SELL/NOT_SELL theo `Char.isSuKien*`), `Map.isSale` và danh sách NPC
đều được dựng *một lần* lúc bật máy. Mặc định máy chủ tự lo việc đó, xem
[`src/real/LichSuKien.java`](../server/src/real/LichSuKien.java).

| Khoá | Mặc định | Ý nghĩa |
|---|---|---|
| `sv.suKienTuKhoiDongLai` | `1` | `0` = tắt hẳn. Lịch hẹn **vẫn chạy** (vẫn đổi cột `bat`), chỉ là không ai tự bật lại máy — trang quản trị sẽ hiện "chờ khởi động lại". |
| `sv.suKienGomGiay` | `120` | Đợi bao lâu không còn thay đổi nào nữa mới khởi động lại. Kẹp trong `30..3600`. |
| `sv.suKienBaoTruocGiay` | `60` | Báo trong game bao lâu trước khi tắt. Kẹp trong `10..600`. |

**Đừng hạ `sv.suKienGomGiay` xuống thấp.** `htbb-game.service` đặt `StartLimitBurst=5` /
`StartLimitIntervalSec=300`: khởi động quá 5 lần trong 300 giây thì systemd cho unit vào
trạng thái `failed` và **không bật lại nữa** — server nằm hẳn cho tới khi có người ssh vào.
Việc gom chính là thứ giữ cho "bật liền 5 sự kiện" chỉ tốn một lần khởi động lại. Cùng lý
do đó, máy chủ không làm gì trong **3 phút đầu** sau khi bật máy, nên hai lần khởi động lại
liên tiếp luôn cách nhau trên 6 phút.

Đừng hạ `sv.suKienBaoTruocGiay` xuống dưới 10 giây: client nhét tin báo vào một hàng đợi
tĩnh và mỗi tin mất 15–20 giây mới trôi hết, nên người chơi sẽ bị đá mà chưa kịp thấy chữ
nào — còn tin thì hiện lại ngay sau khi họ đăng nhập lại, lúc bảo trì đã xong.

Máy chủ **không tự tắt mình**: nó bật cờ `5h_systems.restart`, `BaoTriDaily` đọc cờ đó mỗi
5 giây rồi chạy cả chuỗi lưu dữ liệu trước khi `System.exit(0)`, và systemd bật lại sau 10
giây (`Restart=always`). Nhờ vậy trang quản trị **không cần quyền sudo hay ssh** — nó chỉ
ghi vào CSDL.

### Sự kiện World Cup

Cách bật chính thức [sự kiện bình chọn Messi – Ronaldo](../server/src/real/SuKienWorldCup.java)
là hai dòng *World Cup* trên **trang quản trị `/admin/sukien`**. Ba khoá
`sv.worldCup*` chỉ còn là **giá trị mặc định** cho hai cổng đó khi bảng `tob_su_kien` chưa
có dòng tương ứng.

Định dạng `yyyy-MM-dd HH:mm:ss`, phải theo thứ tự `batDau < ketThuc <= hanNhanQua`.
**Để trống bất kỳ khoá nào là mặc định tắt** — quái không rơi Quả bóng vàng và NPC Tổng
Quản không hiện mục sự kiện. Bản mẫu để trống, nên deploy không tự mở sự kiện.

| Giai đoạn | Người chơi làm gì |
|---|---|
| `batDau` → `ketThuc` | train quái nhặt Quả bóng vàng, đổi 100 bóng + 5000 lượng lấy 1 phiếu + 1 Hộp quà may mắn |
| `ketThuc` → `hanNhanQua` | bên nhiều phiếu hơn thắng; ai đã bỏ phiếu cho bên đó vào NPC nhận quà |

Chạy `sql/them_su_kien_world_cup.sql` **một lần** trước khi mở sự kiện lần đầu — nó đặt tên
cho dòng thuốc 108 (Hộp quà may mắn) và tạo hai bảng điểm.

Cách tự kiểm chứng khi thêm khoá mới: khoá nào xuất hiện trong
[`src/server/CauHinh.java`](../server/src/server/CauHinh.java) thì nạp lại được; khoá chỉ có trong
[`src/server/TeamServer.java`](../server/src/server/TeamServer.java) thì phải khởi động lại.

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

## 5b. Tỉ lệ EXP quái thường (không dùng Boss)

Từ 14/09/2026, chuyển từ hằng số hardcode (`Monster.PC_TILE_EXP_LV50` /
`PC_TILE_EXP_DUOI_LV50`) sang bảng `cau_hinh`, sửa qua `/admin/cauhinh` không cần
restart. Cũng là **phần trăm so với bản gốc**, giống mục 5. Xem
[`Monster.rcvXpSauGiam()`](../server/src/real/Monster.java).

| Khoá | Mặc định | Áp dụng cho |
|---|---|---|
| `sv.tileExpLv50` | `10` | quái từ cấp 50 trở lên |
| `sv.tileExpDuoiLv50` | `50` | quái dưới cấp 50 |

Hai khoá **độc lập nhau** — đổi một cái không đụng tới cái kia. Không áp dụng cho Boss.

## 5c. Tỉ lệ màu + EXP Vận Tiêu

Từ 14/09/2026, chuyển từ mảng hardcode trong `Char.doInitMonter()` /
`CharSuKien.doResponseTieu()` sang bảng `cau_hinh`, sửa qua `/admin/cauhinh`. Xem
[`Char.java`](../server/src/real/Char.java) (các trường `TIEU_*`) và
[`docs/skin-thu-cuoi.md`](skin-thu-cuoi.md) là ví dụ khác của cùng cơ chế "quản lý qua SQL".

| Khoá | Mặc định | Ý nghĩa |
|---|---|---|
| `sv.tieuNguongMau` | `60189,89389,99689,99989,99999` | 5 ngưỡng **cộng dồn** (Trắng/Xanh/Đỏ/Hoàn Mỹ/Vàng) trên thang 100000, roll khi nhận tiêu mới. Phần còn lại (roll ≥ ngưỡng cuối) là Tím. **Phải tăng dần**, server không tự kiểm tra thứ tự — nhập sai thứ tự thì một vài màu bị "nuốt" mất, không nhận được nữa. |
| `sv.tieuHeSoExp` | `200` | % nhân trước khi áp tỉ lệ theo màu (mục dưới), `200` = x2 (giá trị cũ "TangExpVanTieu"). |
| `sv.tieuPcExpTheoMau` | `50,100,150,250,250,500,0` | 7 % EXP theo màu (Trắng/Xanh/Đỏ/Hoàn Mỹ/Vàng/Tím/_ — phần tử cuối không dùng tới), áp dụng khi cấp mở tiêu dưới 50 hoặc màu Trắng/Xanh/Đỏ. |
| `sv.tieuPermilleCaoCap` | `50,70,90,35,55,75,5,25,45` | 9 phần nghìn EXP cần để lên cấp, riêng Hoàn Mỹ/Vàng/Tím từ cấp 50 — gộp phẳng 3 khung cấp (50-59 / 60-69 / 70+) × 3 màu, chỉ số = khung×3 + (màu-3). |
| `sv.doiTieuNguongMauXu` | `50545,85545,98945,99945,99995` | 5 ngưỡng **cộng dồn** (Trắng/Xanh/Đỏ/Hoàn Mỹ/Vàng) trên thang 100000, roll khi **đổi** tiêu (reroll trả phí) bằng xu — độc lập với `sv.tieuNguongMau` (đó là lúc nhận tiêu mới). Xem `Char.doChangeTieu()`. |
| `sv.doiTieuNguongMauLuong` | `22890,52890,96890,99890,99990` | Như trên, dùng khi đổi tiêu trả phí bằng lượng (đắt hơn, tỉ lệ màu hiếm cao hơn hẳn so với đổi bằng xu). |

Sáu khoá đều là chuỗi **CSV** (phân cách bằng dấu phẩy, không có khoảng trắng thừa). Sai số
lượng phần tử (khác đúng 5 / 7 / 9 như bảng trên) hoặc không phải số thì server **âm thầm bỏ
qua**, giữ nguyên giá trị đang chạy — không có dòng log báo lỗi. Lệnh chat `setting` → `6`
(mục 3b) **không** hiện được các khoá này (chỉ hiện giờ chiếm thành + tỉ lệ rơi đồ) — sửa
xong thì mở lại trang `/admin/cauhinh`, giá trị đọc thẳng từ CSDL sẽ phản ánh đúng những
gì server đang dùng.

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

Từ 17/10/2026 (sẵn sàng production, xem `docs/pokemon/SAN_SANG_PRODUCTION.md`):

| Khoá | Ý nghĩa |
|---|---|
| `sv.gmCanCoPInfo` | `1` = quyền GM cần **cả** tên trong `sv.adminAccounts` **lẫn** `pInfo[16]=1` trong `tob_char`. `0` (mặc định) = chỉ cần tên, như cũ. |
| `sv.banToiThieu` | `N>0` = từ chối jar/APK có số bản build `< N` (trường int cuối gói đăng nhập). PC Unity không gửi số bản nên luôn qua. `0` = tắt. Số bản = `BAN=` trong `htbb-client/phat-hanh/phien_ban.conf` (quy ước `major*10000 + minor*100 + patch`; jar MANIFEST `HTBB-Ban`, Android versionCode, PC bundleVersion). Open test **0.9.0-opentest = 900** → đặt `sv.banToiThieu=900` khi mở cửa để chặn jar/APK thử cũ; bản vá 0.9.1 = 901 → nâng lên 901 nếu bắt buộc cập nhật. |
| `sv.chiSoMayChu` | `1` (mặc định) = ghi `tob_chi_so_may_chu` + `trang_thai.json` mỗi 30 s và bật chế độ bảo trì bằng tệp `bao_tri.txt`. `0` = tắt cả hai. |

Lệnh quản trị gói 47 (`AdminHandler`, gồm `shutdown`) giờ chỉ nhận từ **nhân vật đang online có
quyền GM** (trước đây còn nhận theo tên tài khoản và hai tài khoản cứng `staynight`/`lamkckc`), và
mọi lệnh được ghi vào `AdminChat_lenh47.txt`.

## 7b. Chế độ Pokémon

```ini
sv.che_do_pokemon = 1
```

Cờ của nhánh `pokemon/chuyen-doi` (quyết định Q8 trong
[`pokemon/KE_HOACH_CHUYEN_DOI.md`](pokemon/KE_HOACH_CHUYEN_DOI.md)). **Phải khởi động lại**
mới có tác dụng: chỉ đọc một lần lúc bật máy, mọi chỗ trong mã nguồn đọc qua
`com.htbb.pokemon.CheDoPokemon.bat()`.

| Giá trị | Nghĩa |
|---|---|
| `1` | Bật. Sát thương dùng 18 hệ khắc chế thay ngũ hành, nhân vật tiến hoá theo cấp, quái lấy hệ từ `data_monster.he/he2` (không random), server gửi thêm lệnh `-90/-91/-92` cho ClientUnity. Bản mẫu đặt `1`. |
| `0` hoặc thiếu dòng | game gốc, y như trước — không thêm câu SQL, không thêm gói tin nào. |

Trước khi bật trên một CSDL mới: chạy `sql/them_cot_pokemon.sql` (thêm cột `dex`, `shiny`,
`he2`; server cũng tự thêm nếu thiếu, nhưng chỉ khi cờ đang bật) rồi
`sql/pokemon_du_lieu_quai.sql`. Đá tiến hoá Eevee: `sql/pokemon_vat_pham_eevee.sql`.

## 7d. Nạp tiền

```ini
sv.nap_tien = 0
```

Nạp tiền không lạm phát, không bán sức mạnh ([`pokemon/NAP_TIEN.md`](pokemon/NAP_TIEN.md)). **Phải
khởi động lại**; mọi chỗ đọc qua `com.htbb.pokemon.NapTien.bat()`. Chỉ thật sự bật khi **cả**
`sv.lau_dai=1` (thiếu thì server in cảnh báo và giữ tắt). Trước khi bật: chạy
`sql/pokemon_nap_tien.sql`, web đặt `HTBB_NAP_BAT=true`, và **phải có giấy phép hình ảnh/IP**
(`NAP_DA_CO_GIAY_PHEP=1` trên web để tắt cảnh báo). `0` hoặc thiếu dòng = y như trước.

## 7c. Thiết kế dài hạn (giai đoạn 2)

```ini
sv.lau_dai = 0
sv.ngay_khai_truong =
sv.tran_cap_theo_mua = 0
```

Cờ của [`pokemon/KE_HOACH_LAU_DAI_GD2.md`](pokemon/KE_HOACH_LAU_DAI_GD2.md). **Phải khởi động lại**, mọi chỗ đọc
qua `com.htbb.pokemon.LauDai.bat()`. `0` hoặc thiếu dòng = y như trước. `1` = kinh tế/EXP/nhiệm vụ/chiến đấu mới.
**Bật/tắt hoàn toàn tự động**: lúc khởi động server tự chuyển EXP nhân vật sang bảng đúng với cờ (giữ cấp, giữ %),
cả chiều tắt; tự trả nhiệm vụ chính và gỡ lịch sự kiện khi tắt (chi tiết: tài liệu kế hoạch, mục "Bật / tắt cờ").
`sv.tran_cap_theo_mua` (18/10/2026): `0` hoặc thiếu dòng = **không** trần cấp theo mùa — lên cấp chậm (năng lượng 90 phút/ngày)
+ vật phẩm tăng tốc chung trần, nội dung (Hoenn, chuỗi năm 2, huyền thoại, bậc đồ) mở theo cấp + cốt truyện; `1` = bật lại
trần 60 → 70 → 80 → 91 → 95 → 100 theo ngày khai trương. Đổi cờ không chuyển EXP, nhân vật giữ cấp
([`pokemon/THIET_KE_LAU_DAI.md`](pokemon/THIET_KE_LAU_DAI.md) mục 1.4, 1.8). Phải khởi động lại.
`sv.ngay_khai_truong` (yyyy-MM-dd, ngày 0 của lịch sự kiện / mùa PvP / vé mùa, và của trần cấp mùa khi bật) **không bắt buộc**: thiếu thì lần bật đầu
tiên tự ghi hôm nay vào `cau_hinh.lau_dai.ngay_khai_truong` và dùng lại về sau; đặt trong `server.ini` thì giá trị đó thắng.
Số liệu chỉnh không cần build qua bảng `cau_hinh` bằng khoá của `data_kinh_te_lau_dai` (vd `thue.cho`).

## 7d. Giờ bảo trì hằng ngày

```ini
sv.gioBaoTri =
```

`BaoTriDaily`: server tự thoát lúc bảo trì (systemd `Restart=always` trong `vps/htbb-game.service` bật lại).
Trống/thiếu = giờ ngẫu nhiên như cũ; `HH:MM` (vd `04:30`) = giờ cố định; `tat` = không tự bảo trì (cả cảnh báo).
**Phải khởi động lại.**

## 7e. Hệ sinh thái game

```ini
sv.he_sinh_thai = 0
```

Cờ của [`pokemon/HE_SINH_THAI.md`](pokemon/HE_SINH_THAI.md): thời tiết/ngày đêm/mùa đổi loài quái khi hồi sinh,
bùng phát, câu cá, khảo cổ, vườn berry, nấu ăn/chế thuốc, nhà trẻ và trứng, chợ sinh thái, đố vui, đua, album.
**Phải khởi động lại**; mọi chỗ đọc qua `com.htbb.pokemon.HeSinhThai.bat()` (đọc sớm từ `server.ini` khi lớp
được nạp, như `sv.che_do_pokemon`). `0` hoặc thiếu dòng = y như trước. Trước khi bật: chạy
`sql/pokemon_he_sinh_thai.sql` (bảng `tob_hst_*` server cũng tự tạo nếu thiếu).

## 7f. Đăng ký tài khoản trong game

```ini
sv.dangKyTrongGame=1          # 0 = chỉ đăng ký trên web (client nhận hộp thông báo chỉ sang web)
sv.dangKyTuKichHoat=web       # web = theo HTBB_AUTO_ACTIVATE của web | 1 = luôn kích hoạt 30 ngày | 0 = không
sv.dangKyCsdlTaiKhoan=account # CSDL chứa team_user (giống HTBB_DB_ACCOUNT của web)
sv.dangKyTreMs=800            # trả lời chậm (ms) chống bot
```

**Đường đi:** màn "Đăng ký" ở Menu màn đăng nhập (jar/APK: `DangKyScr.java`, PC: `DangKyScr.cs`) → lệnh 39
dạng mới `"#dk2"` qua chính kết nối game → `real.cmd.DangKyTrongGame` trên server game. Không đi qua
`CheckLoginSocket.jar` (jar đóng, chỉ kiểm mật khẩu) và không cần HTTP từ máy J2ME.

**Một bộ quy tắc cho cả web lẫn game:** `HTBB-Web/shared/data/quy-tac-dang-ky.json` (độ dài, ký tự, tên quản
trị cấm, từ ngữ cấm, hạn mức 3/giờ + 10/ngày mỗi IP, câu báo lỗi, bộ ca kiểm thử). Sửa tệp đó rồi chạy
`cd HTBB-Web && node scripts/sinh-quy-tac-dang-ky-java.mjs` (sinh lại `src/data/QuyTacDangKyDuLieu.java`);
`npm test` bên web và `test/KiemDangKyTrongGame.java` bên này so mã băm + chạy chung bộ ca nên quên là báo đỏ.

**Chống spam:** đếm GỘP với web trong `htbb_db.web_chan_spam` (web `dang_ky`, game `dang_ky_game`, thêm bộ đếm
theo mã máy `dang_ky_game_may`), câu hỏi tính nhẩm phải trả lời sau ≥ 2 giây và trong 5 phút, dùng một lần,
tối đa 5 lần gửi mỗi phiên; máy bị cấm (`tob_cam_may`) không đăng ký được. Nhật ký: `sv.pathLog/dang_ky_game.log`.

**Dòng CSDL y hệt web:** `account.team_user` với `PASSWORD()`, **`fromgame=0`** — `CheckLoginSocket.jar` trả cột
`fromgame` về làm "region" và server chỉ cho vào khi region là 0/9, nên `fromgame` khác 0 là tài khoản không
bao giờ đăng nhập được. Kích hoạt `5h_active` 30 ngày như `activateAccount()` của web; với `web` thì đọc khoá
`cau_hinh.web.autoActivate` mà web tự ghi lúc khởi động (`server/plugins/cong-bo-tu-kich-hoat.ts`).
Email không bắt buộc trong game (bắt buộc trên web) — người chơi thêm sau ở trang tài khoản.

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
| `sv.tranGoiThuong` | số gói tin **thường** tối đa của một kết nối trong 5 giây (300) |
| `sv.tranGoiTaiNguyen` | số gói **tải tài nguyên** tối đa của một kết nối trong 5 giây (600) |
| `sv.tranGoiTraCuu` | số gói **tra cứu** (lệnh `7` hỏi máu/vị trí quái) tối đa trong 5 giây, đếm riêng (600) — xem mục 12 |
| `sv.tranGoiMienIp` | IP **không** bị đếm ngập gói, phân cách dấu phẩy (trống). **Chỉ** cho máy chạy bot tải; VPS để trống |
| `limit.ccu` | số người online tối đa — **bắt buộc có**, không có mặc định |

### Hai ngưỡng `sv.tranGoi*` — vì sao máy nhà phải nâng lên

Lệnh tải tài nguyên (`-47` ảnh nhân vật, `-48` quần áo, `-49` hiệu ứng, `-51` ảnh) được đếm
**riêng** với hạn mức rộng hơn, vì lúc vào bản đồ mới chúng trả về một chùm hoàn toàn bình
thường. Vượt ngưỡng là **cắt kết nối** ngay ([io/BoDemGoiTin.java](../server/src/io/BoDemGoiTin.java)).

Hai số này được canh theo **độ trễ Internet**. Qua loopback (máy nhà tự chạy server để thử)
không có độ trễ, nên một client vừa xoá cache tải ảnh nhanh hơn hẳn và chạm trần một cách hoàn
toàn lương thiện — đã xảy ra thật: `NGAP GOI TIN: cat ket noi admin ip=127.0.0.1 (601 goi tai
nguyen trong 5000ms (tran 600))` ba lần liền trong một phiên thử, và sau khi **nâng trần lên
6000 thì vẫn bị cắt ở gói 6001**. Tức qua loopback không có con số nào là "đủ cao" chắc chắn.

**`0` (hoặc số âm) = tắt hẳn phép đếm đó**, cùng quy ước với `sv.socketTimeout` và
`sv.connRateWindow`. Máy nhà nên đặt `sv.tranGoiTaiNguyen=0` trong `server.ini` **của máy đó**,
thay vì đoán một con số. Bản mẫu VPS ghim `300`/`600` là giá trị đã chạy từ trước — **đừng hạ
xuống và đừng đặt 0 ở đó**: tắt trên VPS là mở cửa cho spam lệnh `-51` (đọc ảnh từ đĩa, vừa tốn
đĩa vừa tốn băng thông).

Đọc kiểu có mặc định (`parseIntDefault`), **không** phải `Integer.parseInt` — thiếu dòng thì giữ
số cũ chứ không ném lỗi làm bỏ qua mọi dòng bên dưới (xem mục 10).

## 9. Kết nối CSDL

Đều phải khởi động lại. `db.maxco` + `db.maxNap` + `db.maxco1` + login server (8) + web
**phải nhỏ hơn** `max_connections` của MariaDB, thiếu là giờ đông người ăn lỗi
*Too many connections*.

### `db.choPhepMatKhauTrong` — lối thoát **chỉ dành cho máy nhà**

Server từ chối khởi động khi `db.password` để trống (xem mục 10). Máy nhà chạy XAMPP thường
để `root` không mật khẩu, nên khoá này mở riêng phép kiểm đó:

| Giá trị | Ý nghĩa |
|---|---|
| `0` | Nghiêm ngặt. **Bản mẫu VPS ghim giá trị này.** |
| `1` | Cho `db.password` để trống — **và chỉ khi** `db.host` trỏ vào máy nội bộ (`localhost`, `127.0.0.1`, `[::1]`). |
| thiếu khoá, `""`, `true`, sai chính tả | Nghiêm ngặt (mặc định fail-closed). |

Đặt `1` trong `server.ini` **của máy mình** (file này bị `.gitignore`), **tuyệt đối không**
thêm vào `vps/server.ini.tmpl`. Phải khởi động lại, không nạp lại được.

Khoá này **chỉ** miễn phép kiểm "mật khẩu để trống". Nó không miễn phép thử kết nối thật,
cũng không miễn phép kiểm chuỗi mẫu `DOI_MAT_KHAU_DB` — máy đặt `1` mà chưa bật MySQL thì
vẫn chết ngay với lý do rõ ràng. VPS được bảo vệ ở ba lớp độc lập: bản mẫu ghim `0` và
deploy **sinh lại** `/opt/htbb/server.ini` từ bản mẫu mỗi lần deploy;
[`vps/sinh-server-ini.sh`](../vps/sinh-server-ini.sh) từ chối cài nếu giá trị khác `0`; và
thiếu khoá thì mặc định là nghiêm ngặt. Xem `TeamServer.choPhepMatKhauTrong()` và
[test/KiemMayDevCSDL.java](../server/test/KiemMayDevCSDL.java).

### `db.thamSoJdbc` — tham số thêm vào mọi URL JDBC

Chuỗi dạng `a=1&b=2` (không có `?` ở đầu) được nối vào **cuối mọi URL JDBC** của game
server: bốn pool `db.*` (`Database.setLink*`) và phép thử kết nối lúc khởi động
(`TeamServer.thuKetNoiCSDL`). Để trống hoặc thiếu khoá = URL y như trước (MariaDB trên VPS,
bản mẫu để trống). Phải khởi động lại.

### Chạy trên NovaDB (cổng MySQL của NovaDB, lưu trên SQLite)

```ini
db.host=127.0.0.1:3318
db.thamSoJdbc=useSSL=false
```

`useSSL=false` là **bắt buộc nên có**: cổng MySQL của NovaDB không có TLS, thiếu dòng này
Connector/J 5.1 in cảnh báo *Establishing SSL connection without server's identity
verification…* mỗi lần mở kết nối (ngập log). Mọi câu SQL trong `src/` đã được viết lại để
chạy **y hệt** trên cả MariaDB 10.4 lẫn NovaDB (ngày 16/10/2026,
[test/KiemNovaDB.java](../server/test/KiemNovaDB.java) chặn các cú pháp sau quay lại):

| Cú pháp MySQL NovaDB không chạy | Viết lại thành |
|---|---|
| `CREATE TABLE moi LIKE cu` (bảng theo tháng/tuần: `tob_char_thach_dau_*`, `tob_qua_loi_dai_*`, `tob_log_use_luong_*`, `tob_nap_xai_luong_*`) | `Database.createTable` đọc `SHOW CREATE TABLE cu`, đổi tên, bỏ `AUTO_INCREMENT=N`, chạy lại. Bảng đã có thì trả `false`, không in `SQL ERROR` mỗi lần khởi động nữa |
| `UPDATE a x JOIN b y … SET …` (giftcode_log.userId, id_char của tob_danh_hieu / tob_top_event) | subquery tương quan + `EXISTS` (giữ nghĩa INNER JOIN) |
| `TIMESTAMPDIFF(SECOND, a, NOW())` (even_online_game) | `UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(a)` |
| `UPDATE account.team_user …` (AdminPanel đổi mật khẩu) | `Connection.setCatalog("account")` rồi trả catalog cũ |

**Khác biệt còn lại — do NovaDB, mã nguồn không bù được:**

* `NOW()`/`CURDATE()`/`DEFAULT current_timestamp()` của NovaDB trả **giờ UTC** (MariaDB trên
  VPS trả +07). Mã nguồn **cố ý** giữ `NOW()` của CSDL (lịch hẹn sự kiện so với giờ trang
  quản trị ghi, xem `test/KiemLichSuKien.java`), nên trên NovaDB mọi mốc thời gian SQL ghi
  (log, `lastLog`, lịch hẹn `tob_su_kien`, reset tích luỹ online theo `CURDATE()`) lệch 7
  tiếng so với giờ Java. Cần NovaDB hỗ trợ múi giờ phiên (khi có: thêm
  `sessionVariables=time_zone='+07:00'` vào `db.thamSoJdbc`, không phải sửa mã).
* So sánh chuỗi: `=` của NovaDB **phân biệt hoa/thường** (MariaDB `utf8_general_ci` thì
  không, còn coi `a` = `á`). Server luôn hạ chữ `charname` khi nạp nhân vật và `username`
  khi đăng nhập, nên: tạo nhân vật / đổi tên giờ **lưu tên chữ thường** và kiểm trùng bằng
  `LOWER(charname)=LOWER(?)` (chỉ chạy khi tạo/đổi tên, mất chỉ mục trên MariaDB là chấp
  nhận được). Dữ liệu cũ có tên/username viết hoa chuyển sang NovaDB phải hạ chữ một lần
  (`UPDATE tob_char SET charname=LOWER(charname)` và các bảng giữ tên), không thì các câu
  `WHERE charname='…'` trượt. Gộp dấu (`á`=`a`) của MariaDB không giả lập được. Lệnh GM gõ
  tên người chơi khác (vd `slv Ten 30 0`) trên NovaDB phải gõ đúng chữ thường.
* `UNIQUE KEY` khai trong `CREATE TABLE` thành ràng buộc SQLite, **không xoá được** bằng
  `ALTER TABLE … DROP INDEX` (NovaDB im lặng bỏ qua) và mang tên cột thay cho tên khoá
  (`Index_2` của tob_danh_hieu hiện là `charname`). Hậu quả: trên NovaDB tob_danh_hieu /
  tob_top_event giữ thêm UNIQUE(charname) bên cạnh khoá mới `idx_id_char` — vô hại vì
  tên nhân vật vốn duy nhất.
* Chia số nguyên `7/2` = `3` (MariaDB `3.5000`) — hiện không câu SQL nào chia.
* Login server `CheckLoginSocket.jar` và lớp chỉ có trong `lib/htbb-core.jar` không sửa
  được (xem báo cáo chuyển đổi).

## 10. Cạm bẫy

### Sai một dòng là **bỏ qua mọi dòng bên dưới**

Toàn bộ phần đọc cấu hình nằm trong **một khối `try`**
([TeamServer.java:404](../server/src/server/TeamServer.java#L404)). Một khoá thiếu hoặc sai kiểu là
ném lỗi ngay tại dòng đó, và **mọi dòng phía sau không được đọc** — nhưng server **vẫn khởi
động**, chạy với cấu hình dở dang.

Ví dụ: xoá `limit.ccu` thì `sv.port` (đọc trước, dòng 409) vẫn đúng, còn **toàn bộ kết nối
CSDL (đọc sau, dòng 418–421) không được đặt** — server bật lên nhưng không vào được. Thứ tự
các dòng trong hàm quyết định cái gì sống sót, nên cùng một lỗi gõ nhầm có thể ra triệu
chứng hoàn toàn khác nhau tuỳ khoá nào sai.

Dấu hiệu duy nhất là dòng này trong log:

```
Load configuration file error!
```

Nên **sau mỗi lần sửa, phải xem log**:

```bash
journalctl -u htbb-game -n 50 | grep -i "configuration file error"
```

Không thấy dòng nào là đã đọc trọn file.

### Các khoá **bắt buộc phải có**

Chỉ những khoá đi qua `parseInt` / `parseByte` / `parseShort` mới ném lỗi khi thiếu — và
chính chúng kích hoạt cạm bẫy ở trên. Đúng 20 khoá:

```
api.sv        sv.port      sv.server    sv.portnap   sv.portlogin
limit.ccu     sv.LG        sv.range     sv.lang      sv.me
sv.createRace sv.CodeTest  sv.EnableAvtiveCheck      db.hour      db.minute
sv.isSuKienTrungThul2016    sv.isSuKienTet2017       sv.isSuKienGioTo2016
sv.isSuKienNoel2023         sv.isSuKienTetduonglich2024
```

`sv.expvantieu`, `sv.Luckyexp`, `sv.ExpQua` đã dời khỏi nhóm này từ 11/09/2026 — xem mục 4,
"Thưởng EXP phụ".

**Nhóm `db.*` thì khác**: chúng được đọc thô rồi truyền thẳng vào `Database.setLink(...)`,
nên thiếu **không** ném lỗi ở đây — server vẫn bật, chỉ là không kết nối được CSDL. Triệu
chứng khác hẳn (log lỗi kết nối chứ không phải *Load configuration file error*).

Các khoá đọc thô khác, thiếu cũng không ném lỗi: `api.url`, `db.run`, `sv.dayrace`,
`sv.iplogin`, `sv.pathLog`, `sv.tablerace`, `sv.thuNghiemThuCuoi`.

Các khoá ở mục 4 và mục 8 (trừ `limit.ccu`) đều **có giá trị mặc định**, xoá đi vẫn chạy.

### `sv.isSuKien*` giờ chỉ còn là giá trị mặc định

Từ 30/08/2026, chỗ bật/tắt sự kiện là **trang quản trị `/admin/sukien`** (bảng
`tob_su_kien` trong CSDL), không phải file này. Mọi sự kiện đều bật được bất kỳ lúc nào —
không còn mốc ngày ghi cứng nào trong mã nguồn nữa, xem
[`src/real/SuKien.java`](../server/src/real/SuKien.java).

Năm khoá `sv.isSuKien*` vẫn **bắt buộc phải có** (chúng đi qua `parseByte`, thiếu là ném
lỗi và nuốt luôn mọi dòng cấu hình bên dưới — xem cạm bẫy ở đầu mục này), nhưng giá trị
của chúng chỉ quyết định trạng thái của năm sự kiện đó **khi bảng `tob_su_kien` chưa có
dòng tương ứng**, tức lần khởi động đầu tiên sau khi cập nhật. Sau đó bảng CSDL là nguồn
duy nhất.

Sửa xong trên trang quản trị thì **phải khởi động lại server**: bảng shop
(`Map.loadShop` đặt SELL/NOT_SELL theo `Char.isSuKien*`), `Map.isSale` và danh sách NPC
được dựng *một lần* lúc bật máy theo trạng thái sự kiện.

### `sv.isSuKien83` là khoá chết — đã bỏ khỏi bản mẫu

Nó **không được đọc ở đâu trong mã nguồn**. Đổi giá trị không có tác dụng gì, nên đã bỏ hẳn
khỏi `vps/server.ini.tmpl` — để lại chỉ khiến người ta tưởng chỉnh được cái gì đó. Sự kiện
8/3 giờ bật ở trang quản trị như mọi sự kiện khác.

### Khoá chẩn đoán thú cưỡi

```ini
sv.thuNghiemThuCuoi = tenNhanVat
```

Bật log chẩn đoán thú cưỡi cho **đúng một nhân vật** (so theo tên, không phân biệt
hoa/thường). Để trống hoặc bỏ hẳn dòng = tắt. Dùng khi soi lỗi hình thú cưỡi / skin linh
thú mà không muốn ngập log cả server. Phải khởi động lại mới có tác dụng.

### Chép đè bằng file mẫu — chuyện đã xảy ra 30/08/2026

`/opt/htbb/server.ini` bị chép đè bằng `vps/server.ini.linux.example`, nên `db.password`
thành chuỗi mẫu `DOI_MAT_KHAU_DB`. Triệu chứng **không hề giống nguyên nhân**:

```
mật khẩu sai → ConnPool rỗng → mọi getConnection() ném NoSuchElementException
→ server VẪN CHẠY TIẾP → Map.loadAllTemplate đọc Vector rỗng
→ ArrayIndexOutOfBoundsException trong luồng "main"
→ main chết TRƯỚC KHI mở socket, nhưng luồng non-daemon khác giữ JVM sống
→ systemd báo "active (running)" mà không cổng nào lắng nghe
```

Nhìn `systemctl status` thấy mọi thứ bình thường, nên rất lâu mới lần ra.

Giờ đã có hai lớp chặn:

- `TeamServer.kiemTraCSDL()` thử kết nối **trước mọi lệnh chạm CSDL**; hỏng thì in một dòng
  nói rõ lý do rồi `System.exit(1)`, để systemd báo `failed` đúng sự thật.
- `StartLimitBurst` trong [`vps/htbb-game.service`](../vps/htbb-game.service) để systemd
  thôi bật lại vô hạn và để unit vào trạng thái `failed`.

Và pipeline ở mục 1 làm việc chép đè kia thành vô nghĩa: file luôn được sinh lại từ bản mẫu.

Lối thoát `db.choPhepMatKhauTrong` (mục 9) **không** hé lại cạm bẫy này: nó chỉ nhận đúng giá
trị `1`, chỉ khi CSDL nằm ở máy nội bộ, và chuỗi mẫu `DOI_MAT_KHAU_DB` không bao giờ đi qua nó
(nhánh nới lỏng so bằng chuỗi rỗng). Bản mẫu ghim `0`, và `vps/sinh-server-ini.sh` từ chối cài
nếu khác `0` — nên bản sinh ra cho VPS luôn nghiêm ngặt.

### Đừng làm ba việc này

1. **Đừng `git add server.ini`.** Nó bị ignore có lý do — trong đó có mật khẩu CSDL.
   Bản mẫu `vps/server.ini.tmpl` thì ngược lại: **phải** ở trong git, và không được chứa
   mật khẩu thật (có phép kiểm chặn).
2. **Đừng gửi nội dung file cho ai**, kể cả dán vào chat hỏi bài. Cần hỏi thì nói **tên
   khoá**, đừng kèm giá trị.
3. **Đừng sửa tay `/opt/htbb/server.ini`.** Lần deploy sau ghi đè. Sửa `vps/server.ini.tmpl`.

## 11. Sau khi sửa — kiểm lại

```bash
# 1. Đọc trọn file chưa (không được có dòng nào)
journalctl -u htbb-game -n 80 | grep -i "configuration file error"

# 2. Server còn sống không
systemctl status htbb-game --no-pager

# 3. Cổng còn nghe không
ss -lntp | grep 19129
```

Trong game gõ `setting` → `6` để xem giá trị server **đang thực sự dùng** — đây là cách chắc
chắn nhất, vì nó đọc từ bộ nhớ chứ không đọc lại file.

## 12. Nhật ký (log) và chống spam khi đông người

Tất cả các khoá dưới đây đều **có mặc định trong mã nguồn** (thiếu dòng = mặc định, không ném lỗi) và
chỉ được đọc **lúc khởi động**. Trong `vps/server.ini.tmpl` chúng nằm ở dạng **dòng chú thích**
`#khoa=giá_trị_mặc_định`: bỏ dấu `#` và sửa số là xong. Mã nguồn:
[com/htbb/log/Nhat.java](../server/src/com/htbb/log/Nhat.java),
[com/htbb/chongspam/ChongSpam.java](../server/src/com/htbb/chongspam/ChongSpam.java),
[com/htbb/chongspam/ThongBaoGop.java](../server/src/com/htbb/chongspam/ThongBaoGop.java).

### 12a. Nhật ký `sv.log.*`

Mọi `System.out`/`System.err` cũ (kể cả `printStackTrace()` và dòng in từ `lib/htbb-core.jar`) giờ đi qua
một bộ chặn dòng: phân loại theo tiền tố `[LOẠI]`, lọc theo mức, gộp dòng lặp, ghi **bất đồng bộ**
(luồng game chỉ đẩy vào hàng đợi). stdout vẫn còn — systemd ghi vào `game.out` như cũ — chỉ là ít
hơn nhiều. Mỗi dòng có giờ + chữ mức: `10-18 19:02:11.532 W [NGAP_GOI] ...` (E/W/I/D).

| Khoá | Mặc định | Nghĩa |
|---|---|---|
| `sv.log.muc` | `THONG_TIN` | mức tối thiểu ra **stdout** (`LOI` / `CANH_BAO` / `THONG_TIN` / `GO_LOI`; nhận cả ERROR/WARN/INFO/DEBUG) |
| `sv.log.mucTep` | = `sv.log.muc` | mức tối thiểu ghi **tệp** |
| `sv.log.tat` | `GOI3,GOI5,GOI26,VKTT,SQL,LUU_CHAR` | loại bị **tắt hẳn** (chữ trong `[..]` đầu dòng; dấu cách thành `_`). Cứ 10 phút một dòng đếm số đã bỏ |
| `sv.log.kiemToan` | `ADMIN,GM,NAP,NAP_TIEN,GIFTCODE,CAM,BAN,KINH_TE,KINHTE,CHONG_SPAM` | loại **kiểm toán**: không tắt được, luôn ghi nguyên vẹn vào tệp `kiem_toan-*.log` |
| `sv.log.lapToiDa` | `5` | số dòng **cùng mẫu** (số → `#`, chuỗi `'..'` → `?`) được ghi mỗi cửa sổ; quá thì đếm, hết cửa sổ in **một** dòng `gop dong lap xN lan trong 60s` |
| `sv.log.loaiToiDa` | `120` | trần chung mỗi **loại** mỗi cửa sổ (tên nhân vật khác nhau vẫn cùng loại). Dòng `LOI` không bị trần này chặn |
| `sv.log.cuaSoGiay` | `60` | độ dài cửa sổ gộp |
| `sv.log.cuaSoStackGiay` | `600` | stack trace của **cùng một lỗi** (lớp + mẫu thông điệp + khung đầu) chỉ in đầy đủ **một lần** trong khoảng này; các lần sau một dòng, cuối kỳ in số lần lặp |
| `sv.log.hangDoi` | `50000` | sức chứa hàng đợi ghi; đầy thì bỏ dòng thường (đếm vào `nhatKy.mat` của `trang_thai.json`), dòng LOI chờ tối đa 50 ms |
| `sv.log.tep` | `1` | `0` = không ghi tệp `may_chu-*.log` (tệp kiểm toán vẫn ghi) |
| `sv.log.thuMuc` | `<sv.pathLog>/nhat_ky/` | thư mục tệp. **Đừng** đặt trùng `/opt/htbb/log/`: `vps/htbb-logrotate.conf` có glob `/opt/htbb/log/*.log` sẽ tranh xoay vòng |
| `sv.log.tepToiDaMB` | `100` | dung lượng mỗi phần tệp; đầy sang `.1.log`, `.2.log`… |
| `sv.log.soTepMoiNgay` | `5` | số phần tối đa mỗi ngày; hết thì **chỉ còn ghi dòng LOI** (đếm số bỏ, báo đầu tệp hôm sau) |
| `sv.log.giuNgay` | `14` | xoá tệp `may_chu-*` cũ hơn chừng này ngày |
| `sv.log.giuNgayKiemToan` | `90` | như trên cho `kiem_toan-*` |

Phân loại dòng cũ: `[ABC DEF] ...` → loại `ABC_DEF`; câu SQL trần (`select/update/insert...`) → `SQL`
mức `GO_LOI` (riêng `SQL ERROR...` và câu SQL kèm chữ lỗi → `SQL_LOI`, không bị tắt cùng `SQL`); `info VKTT` → `VKTT`; `SAVE CHARSELL`/`savechar` → `LUU_CHAR`; dòng chứa
`exception/error/loi/lỗi/that bai` → `LOI` (stderr) hoặc `CANH_BAO` (stdout); stack trace gom thành
**một khối** loại `STACK`.

Đã gỡ hẳn: câu SQL `update tob_char_luong set nPK=... where charname='<tên>'` in ra cho **từng** nhân
vật top PK (vừa rác vừa lộ tên, và còn ghép chuỗi tên vào SQL — nay dùng tham số), `info VKTT` mỗi
lần gửi gói 3. Chuỗi do client gửi lên (tên tài khoản, nền tảng, phiên bản, chat admin) được làm sạch
bằng `Nhat.sach()`: xuống dòng thành `\n` chữ, ký tự điều khiển/ESC thành `?` — không giả mạo được
dòng log.

**Nhật ký kiểm toán giữ nguyên chỗ cũ**: `AdminChat.txt`, `AdminChat_lenh47.txt`, `errorlog.txt`,
bảng `saveOrtherLog` (nạp tiền, giftcode, nhận tiêu…) không đổi. `kiem_toan-*.log` là **bản thứ hai**,
không gộp, không tắt được.

Chỉ số trong `trang_thai.json`: `nhatKy.ghi / bo / loi / mat` (cộng dồn), `chatChan`, `thaoTacChan`.

### 12b. Chống spam chat `sv.spam.*`

Nhịp dạng `số/giây` (cửa sổ trượt). Admin (`isAdmin`) miễn. Kiểm trên nội dung **gốc**, trước bộ
lọc từ xấu (bộ lọc cũ xoá dấu chấm nên `abc.com` thành `abccom` và lọt).

| Khoá | Mặc định | Nghĩa |
|---|---|---|
| `sv.spam.bat` | `1` | `0` = tắt toàn bộ chống spam chat + thao tác |
| `sv.spam.chat.gan` | `5/10` | chat thường (lệnh 27, người xung quanh) |
| `sv.spam.chat.rieng` | `6/10` | tin riêng (lệnh -5) |
| `sv.spam.chat.theGioi` | `1/30` | loa thế giới, mỗi người (cộng thêm `Char.LOA_COOLDOWN_MS`); bị chặn thì **không trừ loa** |
| `sv.spam.theGioiToanServer` | `12/60` | trần **chung** kênh thế giới cho cả máy chủ (không tính vi phạm cho người gửi) |
| `sv.spam.chat.doi` | `8/10` | dự phòng cho kênh tổ đội |
| `sv.spam.chat.bang` | `8/10` | chat bang (lệnh bang mới + bảng tin bang — mỗi tin bảng là một lần ghi CSDL) |
| `sv.spam.trungLapGiay` | `30` | cùng nội dung (chuẩn hoá: thường, bỏ dấu, bỏ ký tự lặp) trong chừng này giây = trùng lặp |
| `sv.spam.chanQuangCao` | `1` | chặn link/quảng cáo: `http://`, `www.`, tên miền `.com/.net/.vn/.xyz…` (cả `abc (dot) com`, `abc chấm com`), zalo/fb/telegram/discord, số điện thoại VN, địa chỉ IP |
| `sv.spam.quangCaoThem` | trống | thêm từ cấm quảng cáo, phân cách dấu phẩy (so khớp không dấu, không phân biệt hoa thường) |
| `sv.spam.viPhamCam` | `4` | đủ chừng này điểm vi phạm thì **tự động cấm chat** (quá nhanh / trùng = 1 điểm, quảng cáo = 2) |
| `sv.spam.viPhamQuenGiay` | `600` | điểm vi phạm về 0 sau chừng này giây sạch |
| `sv.spam.camGiay` | `60,300,1800,7200` | thời gian cấm **tăng dần** theo bậc (1 → 5 → 30 → 120 phút) |
| `sv.spam.bacQuenGio` | `24` | bậc cấm hạ về 0 sau chừng này giờ không bị cấm |
| `sv.spam.baoCachGiay` | `5` | báo người bị chặn tối đa 1 lần / chừng này giây (không thì chính câu báo thành spam server→client) |

Thay cho luật cũ: quá 5 tin/30 giây thì bỏ im lặng, 5 tin trong 5 giây thì **cấm thẳng 30 phút**
không báo. Tin riêng bỏ luôn `Thread.sleep(5000)` từng ngủ **trên luồng đọc gói** của kết nối. Mỗi
lần tự động cấm ghi một dòng kiểm toán `CHONG_SPAM`.

### 12c. Chống spam thao tác `sv.spam.hd.*`

Vượt nhịp thì **bỏ thao tác** (không cắt kết nối), báo "Thao tác quá nhanh" tối đa 1 lần/5 giây.

| Khoá | Mặc định | Thao tác |
|---|---|---|
| `sv.spam.hd.moiDoi` | `4/10` | mời tổ đội (lệnh 49, kiểu 0) |
| `sv.spam.hd.moiBang` | `4/10` | mời vào bang (lệnh -11) |
| `sv.spam.hd.ketBan` | `4/10` | lời mời kết bạn (lệnh 101, kiểu 0) |
| `sv.spam.hd.moiGiaoDich` | `4/10` | dự phòng — mời giao dịch đã có sẵn chờ 60 giây (`timeInviteTrade`) |
| `sv.spam.hd.npc` | `25/5` | menu NPC (lệnh 53/55) |
| `sv.spam.hd.raoBan` | `6/30` | rao bán chợ (lệnh 62/92) |
| `sv.spam.hd.muaBan` | `25/5` | mua / bán cửa hàng (lệnh 24/28/95) |
| `sv.spam.hd.dangKy` | `5/60` | dự phòng cho đăng ký; hạn mức IP 3/giờ 10/ngày của `DangKyTrongGame` vẫn áp |

### 12d. Thông báo toàn máy chủ `sv.thongBao.*`

"Chúc mừng X nhận được…", bùng phát sinh thái, boss thế giới xuất hiện: mỗi tin là **một gói cho mọi
người online**; đông người thì chính đông người sinh nhiều tin (N người × N tin), client lại hiện
từng tin 15–20 giây nên hàng đợi dồn hàng phút. Nay đi qua `ThongBaoGop.dua()`; tin **ưu tiên**
(bảo trì, admin, giờ vàng, PK tự do, chiếm thành) vẫn gửi thẳng như cũ.

| Khoá | Mặc định | Nghĩa |
|---|---|---|
| `sv.thongBao.bat` | `1` | `0` = gửi thẳng như trước |
| `sv.thongBao.toiDa` / `sv.thongBao.cuaSoGiay` | `6` / `60` | tối đa 6 tin / 60 giây cho cả máy chủ |
| `sv.thongBao.moiLoai` | `3` | trần riêng mỗi loại tin trong cùng cửa sổ (`nhan_thuong`, `sinh_thai`, `boss`) |
| `sv.thongBao.trungLapGiay` | `120` | cùng nội dung trong chừng này giây thì bỏ |
| `sv.thongBao.gopGiay` | `15` | tin vượt trần vào hàng chờ (tối đa 50); cứ chừng này giây gửi **một** tin gộp `tin 1 \| tin 2 (+N tin khác)` |

### 12e. Ngập gói: lệnh tra cứu và máy chạy bot

Đo bằng `tools/pokemon/kiem_tai/BotTai` (100 bot, 10 GM): bot GM quét id quái 0..399 bằng lệnh `7`
trong ~2,4 giây, chạm trần 300 lệnh thường và sinh 67 dòng `NGAP GOI TIN` trong 10 phút. Client thật
chỉ hỏi quái **đang thấy**, nên lệnh `7` (chỉ đọc, rẻ) giờ có ngân sách riêng `sv.tranGoiTraCuu`
(600/5 s); lệnh thường giữ nguyên 300. Máy chạy bot tải có thể đặt `sv.tranGoiMienIp=127.0.0.1`
trong `server.ini` **của máy đó** — VPS để trống.

