# Cấu hình server — `server.ini`

Mọi thiết lập chạy máy chủ nằm trong `server.ini`: cổng, kết nối CSDL, tỉ lệ rơi đồ, giờ
chiếm thành, chống flood, quyền admin.

Tài liệu này là bản tra cứu. Phần dựng server lần đầu xem
[HUONG_DAN_BUILD_VPS.md](HUONG_DAN_BUILD_VPS.md).

---

## 1. Cấu hình giờ **sinh ra từ pipeline** — sửa trên GitHub

Nguồn duy nhất là [`vps/server.ini.tmpl`](../vps/server.ini.tmpl), **nằm trong git**. Mỗi
lần deploy, [`vps/sinh-server-ini.sh`](../vps/sinh-server-ini.sh) dựng lại
`/opt/kpah/server.ini` từ bản mẫu đó cộng với mật khẩu lấy từ GitHub Secrets.

```
vps/server.ini.tmpl  ─┐
                      ├─→ sinh-server-ini.sh ─→ /opt/kpah/server.ini ─→ khởi động lại
GitHub Secrets  ──────┘        (kiểm tra rồi mới cài)
```

Nghĩa là:

- **Sửa cấu hình = sửa `vps/server.ini.tmpl` rồi commit.** Sửa ngay trên web GitHub cũng
  được. Deploy tự chạy, tự khởi động lại nếu cấu hình thật sự đổi.
- **Đừng sửa tay `/opt/kpah/server.ini` nữa** — lần deploy sau ghi đè.
- Mọi thay đổi cấu hình từ giờ đều có lịch sử git, xem được ai đổi gì, và `git revert` được.

`/opt/kpah/server.ini` vẫn nằm trong `.gitignore` (nó chứa mật khẩu thật sau khi sinh ra).

Hai file khác **không** đi qua pipeline này, vẫn phải sửa tay trên VPS:

| Đường dẫn | Dùng cho |
|---|---|
| `/opt/kpah/loginServer/server.ini` | server đăng nhập |
| `KPAH\server.ini` (máy nhà) | server chạy thử ở nhà |

## 2. Đổi một thiết lập

1. Mở [`vps/server.ini.tmpl`](../vps/server.ini.tmpl) trên GitHub, bấm sửa, đổi giá trị, commit.
2. Xem tab **Actions** — deploy chạy ngay.
3. Nếu cấu hình đổi thật thì server báo trước 60 giây trong game rồi khởi động lại.

Xem trước khi commit, chạy trên VPS (không cài gì, chỉ in ra khác biệt):

```bash
sudo /opt/kpah/vps/sinh-server-ini.sh --thu
```

**Bộ sinh không bao giờ cài một cấu hình hỏng.** Trước khi ghi đè nó kiểm: còn chỗ trống
chưa thay không, đủ 20 khoá bắt buộc không, mật khẩu có còn là chuỗi mẫu không, và **thử kết
nối CSDL thật**. Sai bất kỳ điều nào thì báo lỗi, giữ nguyên file đang chạy, không khởi động
lại. Bản cũ được lưu vào `/opt/kpah/backup/server.ini.<ngày giờ>` (giữ 10 bản gần nhất).

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
| Trang quản trị `/admin/cauhinh.php` | sửa từ xa, không cần SSH — xem bên dưới |
| Sửa `/opt/kpah/server.ini` rồi gõ `setting` → `8` | chỉ với 26 khoá ở mục 4 |

**Từ 09/09/2026, cả ba cách trên đọc/ghi CHUNG một bảng SQL `kpah_db.cau_hinh`** (khoá,
giá_trị, sửa_bởi, sửa_lúc) — không còn ghi thẳng vào `server.ini` nữa (xem
[`server/CauHinh.java`](../src/server/CauHinh.java)). `server.ini` vẫn giữ 26 khoá này làm
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
lần lúc bật máy** — sửa xong bắt buộc `systemctl restart kpah-game`.

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

Từ 11/09/2026, ba khoá dưới đây đổi ở **trang quản trị `/admin/cauhinh.php`** thay vì sửa
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
[`src/real/LichSuKien.java`](../src/real/LichSuKien.java).

| Khoá | Mặc định | Ý nghĩa |
|---|---|---|
| `sv.suKienTuKhoiDongLai` | `1` | `0` = tắt hẳn. Lịch hẹn **vẫn chạy** (vẫn đổi cột `bat`), chỉ là không ai tự bật lại máy — trang quản trị sẽ hiện "chờ khởi động lại". |
| `sv.suKienGomGiay` | `120` | Đợi bao lâu không còn thay đổi nào nữa mới khởi động lại. Kẹp trong `30..3600`. |
| `sv.suKienBaoTruocGiay` | `60` | Báo trong game bao lâu trước khi tắt. Kẹp trong `10..600`. |

**Đừng hạ `sv.suKienGomGiay` xuống thấp.** `kpah-game.service` đặt `StartLimitBurst=5` /
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

Cách bật chính thức [sự kiện bình chọn Messi – Ronaldo](../src/real/SuKienWorldCup.java)
là hai dòng *World Cup* trên **trang quản trị `/admin/sukien.php`**. Ba khoá
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

## 5b. Tỉ lệ EXP quái thường (không dùng Boss)

Từ 14/09/2026, chuyển từ hằng số hardcode (`Monster.PC_TILE_EXP_LV50` /
`PC_TILE_EXP_DUOI_LV50`) sang bảng `cau_hinh`, sửa qua `/admin/cauhinh.php` không cần
restart. Cũng là **phần trăm so với bản gốc**, giống mục 5. Xem
[`Monster.rcvXpSauGiam()`](../src/real/Monster.java).

| Khoá | Mặc định | Áp dụng cho |
|---|---|---|
| `sv.tileExpLv50` | `10` | quái từ cấp 50 trở lên |
| `sv.tileExpDuoiLv50` | `50` | quái dưới cấp 50 |

Hai khoá **độc lập nhau** — đổi một cái không đụng tới cái kia. Không áp dụng cho Boss.

## 5c. Tỉ lệ màu + EXP Vận Tiêu

Từ 14/09/2026, chuyển từ mảng hardcode trong `Char.doInitMonter()` /
`CharSuKien.doResponseTieu()` sang bảng `cau_hinh`, sửa qua `/admin/cauhinh.php`. Xem
[`Char.java`](../src/real/Char.java) (các trường `TIEU_*`) và
[`docs/skin-linh-thu.md`](skin-linh-thu.md) là ví dụ khác của cùng cơ chế "quản lý qua SQL".

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
xong thì mở lại trang `/admin/cauhinh.php`, giá trị đọc thẳng từ CSDL sẽ phản ánh đúng những
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

## 7b. Chế độ Pokémon

```ini
sv.che_do_pokemon = 1
```

Cờ của nhánh `pokemon/chuyen-doi` (quyết định Q8 trong
[`pokemon/KE_HOACH_CHUYEN_DOI.md`](pokemon/KE_HOACH_CHUYEN_DOI.md)). **Phải khởi động lại**
mới có tác dụng: chỉ đọc một lần lúc bật máy, mọi chỗ trong mã nguồn đọc qua
`com.kpah.pokemon.CheDoPokemon.bat()`.

| Giá trị | Nghĩa |
|---|---|
| `1` | Bật. Sát thương dùng 18 hệ khắc chế thay ngũ hành, nhân vật tiến hoá theo cấp, quái lấy hệ từ `data_monster.he/he2` (không random), server gửi thêm lệnh `-90/-91/-92` cho ClientUnity. Bản mẫu đặt `1`. |
| `0` hoặc thiếu dòng | KPAH gốc, y như trước — không thêm câu SQL, không thêm gói tin nào. |

Trước khi bật trên một CSDL mới: chạy `sql/them_cot_pokemon.sql` (thêm cột `dex`, `shiny`,
`he2`; server cũng tự thêm nếu thiếu, nhưng chỉ khi cờ đang bật) rồi
`sql/pokemon_du_lieu_quai.sql`. Đá tiến hoá Eevee: `sql/pokemon_vat_pham_eevee.sql`.

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
| `limit.ccu` | số người online tối đa — **bắt buộc có**, không có mặc định |

### Hai ngưỡng `sv.tranGoi*` — vì sao máy nhà phải nâng lên

Lệnh tải tài nguyên (`-47` ảnh nhân vật, `-48` quần áo, `-49` hiệu ứng, `-51` ảnh) được đếm
**riêng** với hạn mức rộng hơn, vì lúc vào bản đồ mới chúng trả về một chùm hoàn toàn bình
thường. Vượt ngưỡng là **cắt kết nối** ngay ([io/BoDemGoiTin.java](../src/io/BoDemGoiTin.java)).

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
deploy **sinh lại** `/opt/kpah/server.ini` từ bản mẫu mỗi lần deploy;
[`vps/sinh-server-ini.sh`](../vps/sinh-server-ini.sh) từ chối cài nếu giá trị khác `0`; và
thiếu khoá thì mặc định là nghiêm ngặt. Xem `TeamServer.choPhepMatKhauTrong()` và
[test/KiemMayDevCSDL.java](../test/KiemMayDevCSDL.java).

## 10. Cạm bẫy

### Sai một dòng là **bỏ qua mọi dòng bên dưới**

Toàn bộ phần đọc cấu hình nằm trong **một khối `try`**
([TeamServer.java:404](../src/server/TeamServer.java#L404)). Một khoá thiếu hoặc sai kiểu là
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
journalctl -u kpah-game -n 50 | grep -i "configuration file error"
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

Từ 30/08/2026, chỗ bật/tắt sự kiện là **trang quản trị `/admin/sukien.php`** (bảng
`tob_su_kien` trong CSDL), không phải file này. Mọi sự kiện đều bật được bất kỳ lúc nào —
không còn mốc ngày ghi cứng nào trong mã nguồn nữa, xem
[`src/real/SuKien.java`](../src/real/SuKien.java).

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

`/opt/kpah/server.ini` bị chép đè bằng `vps/server.ini.linux.example`, nên `db.password`
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
- `StartLimitBurst` trong [`vps/kpah-game.service`](../vps/kpah-game.service) để systemd
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
3. **Đừng sửa tay `/opt/kpah/server.ini`.** Lần deploy sau ghi đè. Sửa `vps/server.ini.tmpl`.

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
