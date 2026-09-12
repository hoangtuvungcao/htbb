# Skin linh thú

Trang bị vật phẩm skin → **con linh thú (thú cưỡi) đang cưỡi đổi hình**, và **chủ nhân
được thêm chỉ số + một nội tại**.

Một câu phải nhớ: **chỉ số của bản thân con linh thú không bị đụng tới một chút nào.**
Skin không ghi gì vào `Animal`, không ghi vào `tob_animal`. Nó chỉ ghi đè ở tầng đóng gói
tin (phần hình ảnh) và cộng thêm vào chỉ số của **nhân vật** (phần sức mạnh). Bán con thú,
đổi con thú, nâng cấp con thú — skin không liên quan.

Code: [`src/real/SkinLinhThu.java`](../src/real/SkinLinhThu.java) —
kiểm thử: [`test/KiemSkinLinhThu.java`](../test/KiemSkinLinhThu.java) —
SQL: [`sql/skin_linh_thu.sql`](../sql/skin_linh_thu.sql) +
[`sql/ten_mo_ta_skin_linh_thu.sql`](../sql/ten_mo_ta_skin_linh_thu.sql)

---

## 1. Cài đặt

**Hai** tệp SQL, chạy theo thứ tự:

```bash
mysql -u root -p <ten_db> < sql/skin_linh_thu.sql
mysql -u root -p <ten_db> < sql/ten_mo_ta_skin_linh_thu.sql
```

Tệp thứ nhất tạo 5 vật phẩm với **tên ngắn**. Tệp thứ hai ghi đè bằng **tên đầy đủ kèm mô
tả** (hệ + các dòng chỉ số + nội tại). Bỏ tệp thứ hai thì vật phẩm vẫn chạy đúng nhưng
người chơi không đọc được nó làm gì.

Tệp thứ hai **được sinh ra**, đừng sửa tay — xem mục 5.5.

Rồi build lại server và khởi động lại (bảng item template và bộ ảnh chỉ nạp một lần lúc
bật máy):

```bash
build.bat
```

Ảnh đã có sẵn trong `map/skinthu/` và `map/icon/860..864.png`, không cần dựng lại.

## 2. Người chơi dùng thế nào

| Việc | Cách làm |
|---|---|
| Trang bị | Cưỡi linh thú → mở **Trang bị thú** → mặc vật phẩm skin vào **ô đầu tiên bên phải** |
| Đổi skin | Mặc skin khác đè lên, skin cũ tự về hành trang |
| Gỡ skin | **Xuống thú** (tự gỡ), hoặc NPC đổi thưởng → *Gỡ skin linh thú*, hoặc gõ `boskin` trong chat |
| Xem đang mang gì | Mở bảng Trang bị thú, hoặc gõ `boskin` khi không mang skin sẽ báo |

**Xuống thú là gỡ hẳn**, vật phẩm trả về hành trang — không phải chỉ ẩn đi. Thiết kế gốc là
skin gắn theo nhân vật nên xuống thú chỉ ẩn, nhưng bản JAR **không có nút tháo** trong bảng
Trang bị thú (đã kiểm: `wearingAnimal` bên client chỉ được dùng ở hai chỗ *vẽ* và một chỗ
*xem thông tin*), nên người chơi bản JAR không có đường nào gỡ ngoài lệnh chat và menu NPC
— cả hai đều khó tìm. Hành trang đầy thì báo và **giữ nguyên skin đang mặc**, không mất gì.

Đổi sang con linh thú khác vẫn giữ skin — **skin gắn theo nhân vật, không theo từng con
thú**.

## 3. Nguồn phát skin

| Nguồn | Cách | Hạn | Chỉ số |
|---|---|---|---|
| Đổi điểm | NPC đổi thưởng, 5 mục cuối danh sách, 2000 điểm mỗi skin — **chỉ 5 skin đầu**, không có Kim Cang Long | vĩnh viễn | cố định |
| Giftcode / quà gõ tay `DB:<id>:1:<phút>` | `DB:905:1:0` … `DB:911:1:0` (id template : số lượng : số phút; 0 = vĩnh viễn, >0 thì hết hạn tự xoá như trang phục có hạn) | theo phút | 905–909 cố định; **911 roll 2–3 dòng** lúc phát (`GiftCode.AddItem`) |
| Bộ chọn phần thưởng trên trang quản trị, nhóm "Skin linh thú" | sinh `DB:<id>:1:<phút>` (từ 12/09/2026; trước đó sinh `ITEM:<id>:1`) | theo ô "Thời hạn (phút)" | như trên |
| `ITEM:<id>:1` gõ tay | `Char.createItemSoLuong` — từ 12/09/2026 cũng roll cho 911 (trước đó 0 chỉ số) | **luôn vĩnh viễn**, không đặt được hạn | 911 roll |

Bản đầu tiên còn có lệnh admin `tangskin <tên> <0-4>`. Bản này **không có** — các nguồn
trên là đủ, và giftcode làm được đúng việc mà lệnh đó làm. Bật/tắt sự kiện **không** phát
skin nào: 33 sự kiện cũ dùng menu cứng, không có skin trong đó.

## 4. Năm skin

| # | Tên | Item id | Hệ | Hợp môn phái | Chỉ số thường trực | Nội tại |
|---|---|---|---|---|---|---|
| 0 | Phúc Thú | 905 | Kim | Pháp sư | +2000 HP, +3000 MP | bị đánh đòn vật lý → chặn 30% ST |
| 1 | Gấu Trúc | 906 | Mộc | Chiến binh | +200 chính xác | bị đánh đòn phép → phản ST +50 điểm |
| 2 | Tiên Lộc | 907 | Thủy | Cung thủ | +20% điểm tiềm năng | nhận ST → lá chắn chặn 50% ST |
| 3 | Xích Dương | 908 | Hỏa | Đấu sĩ | 5% đòn xuyên toàn bộ giáp | máu dưới 30% → +50% tấn công |
| 4 | Chiến Lang | 909 | Thổ | Kiếm khách | +20% thủ, +15% hấp thụ ST | — (15% hấp thụ luôn bật) |

Cưỡi skin **hợp hệ** với môn phái của mình thì thêm **+5% sát thương**. Chi tiết ở mục 5.3.

Ảnh: icon `860+n`, ảnh bản đồ `13000+n`, tệp `map/skinthu/<n>` + `<n>.png`.

Sprite nguồn (tên tệp trong bộ được đưa vào): Phúc Thú `8907`, Gấu Trúc `8906`, Tiên Lộc
`8908`, Xích Dương `8905`, Chiến Lang `8909`. **Tên gán theo hình chứ không theo thứ tự
tệp** — con hoa hồng hợp với "Phúc Thú" hơn là con lửa, nên thứ tự bị hoán lại. Đổi thứ tự
này thì đổi tệp trong `map/skinthu/`, không phải đổi code.

Đổi id ở SQL thì phải đổi `SkinLinhThu.ID_ITEM` cho khớp rồi build lại.

---

## 5. Chỉ số, hệ và nội tại

### 5.1 Số đặt trong mã nguồn, không đặt trong `data_item.atb`

Mọi con số nằm trong các bảng hằng số của `SkinLinhThu`, tra theo số hiệu skin. Ba lý do:

1. **Số là cố định theo từng skin**, không quay ngẫu nhiên theo từng món đồ — nên nó là
   hằng số của tính năng, không phải thuộc tính của vật phẩm.
2. **Đường `atb` của trang bị thú bị lọc theo type ở mọi chỗ tính chỉ số** —
   `calculateAttack` chỉ nhận type 15/17, `calculateDefend` chỉ nhận 14/16/18. Type 30 rơi
   ra ngoài hết, nên đi đường đó vẫn phải sửa từng chỗ. Sửa thẳng thì rõ ràng hơn là nhồi
   số vào DB rồi vẫn phải sửa.
3. **Phép kiểm đọc được**, và đổi số không phải chạy SQL trên máy chủ.

Hệ quả: `Item.attribShowedForType[30]` để **trống** — không có dòng `atb` nào để hiện, bật
cờ lên chỉ hiện số 0. Người chơi đọc chỉ số qua **mô tả trong tên vật phẩm** (mục 5.5).

**Mọi thứ đều đi qua `SkinLinhThu.dangMang()`** — cùng một cổng với phần hình ảnh. Xuống
thú, bang chủ đang giữ thành, hay máy chủ thiếu file ảnh thì không được chỉ số nào. Thưởng
cho thứ người chơi không hề nhìn thấy là nói dối họ.

### 5.2 Chỉ số thường trực — cộng ở đâu

| Chỉ số | Chỗ cộng |
|---|---|
| +2000 HP, +3000 MP | `CharChiSo.calculateMaxHPMP`, cuối hàm |
| +200 chính xác | `Char.calculateAccurate`, **cuối hàm** |
| +20% điểm tiềm năng | 25 chỗ trong `CharChiSo` + 2 chỗ trong `Char` + bảng gửi client |
| 5% xuyên giáp | `Char.calculateAttrib` → `attNguHanh[3]` |
| +20% thủ | `CharChiSo.calculateDefend`, cả **bốn** bộ đếm phần trăm |
| +15% hấp thụ | `Char.checkHapthuSatThuong` |

Bốn điểm dễ làm sai:

**"+200 chính xác" phải cộng ở CUỐI hàm.** `calculateAccurate` làm
`(accurate1 + accurate2 + basic) / 7` rồi **chặn trần ở 99**. Cộng vào `accurate2` giữa hàm
thì 200 còn khoảng 28, và thành 0 hẳn với nhân vật đã chạm trần. Cộng sau cả phép chia,
trần, nhân 700 và nhân nội lực thì 200 mới là 200 thật.
*(Công thức trúng đòn là `random(accurate + 10) < accurate` — bão hoà rất nhanh, nên +200
đáng kể với nhân vật chính xác thấp và gần như không đổi với nhân vật đã cao. Đó là cách
game dùng chỉ số này từ trước, không phải riêng skin.)*

**"Xuyên giáp" ở server này là TỈ LỆ, không phải trừ giáp.** `Char.getBuffEffAttack` quay
`r.nextInt(100) < attNguHanh[3]`, trúng thì đòn đó **bỏ qua hoàn toàn** giáp mục tiêu
(`attackDam` đặt `dam = att`). Nên 5 = "5% số đòn xuyên thẳng", không phải "giảm 5% giáp
mỗi đòn". Cộng **sau** `resetNguHanh()` — hàm đó cấp phát lại `attNguHanh` mỗi lần chạy, mà
`calculateAttrib()` chạy lại mỗi lần mặc/tháo đồ.

**"+20% tiềm năng" tính TẠI CHỖ, không ghi vào bốn trường chỉ số.** `strength`, `agitity`,
`spirit`, `health` được **ghi xuống CSDL nguyên si** (`DatabaseNhanVat` gộp chuỗi
`"strength,agitity,spirit,health,…"`). Cách "nâng lên → tính → trả lại" là đặt cược vào việc
không luồng nào lưu nhân vật đúng khoảng giữa — thua một lần là số phồng lên nằm **vĩnh
viễn** trong CSDL và còn phồng tiếp mỗi lần lưu sau đó. Nên bốn hàm
`Char.tnSucManh/tnKheoLeo/tnTinhThan/tnSucKhoe` tính tại chỗ, và mọi công thức phải cộng
tay một lần. Cũng không đi qua `newAtb` (`byte[10]`, trần 127) hay `upAbility` (`byte[5]`,
**không hề được đặt lại** mỗi lần tính → cộng dồn đến tràn kiểu thành số âm).

**"+20% thủ" phải vào cả bốn bộ đếm.** `upVat`/`upMa` ra thủ hiện trong bảng nhân vật,
`upvP`/`upmP` ra thủ dùng khi đánh nhau với người chơi (`defendVatPlayer`/`defendMaPlayer`).
Thiếu cặp sau thì người chơi thấy +20% trong bảng mà vào PK không được gì.

Bảng chỉ số gửi cho client (`MessageCreator`) cũng cộng phần tiềm năng — không thì người
chơi thấy số cũ trong khi sát thương và máu đã tăng, trông như lỗi và không có cách nào tự
kiểm chứng.

### 5.3 Hệ ngũ hành — hợp hệ thì mạnh hơn

Năm hệ được **xếp theo vòng tương sinh**, nên "hệ này sinh ra hệ nào" chỉ là `(i + 1) % 5`,
không còn bảng tra 5×5 để gõ nhầm một ô:

```
Mộc → Hỏa → Thổ → Kim → Thủy → Mộc
 0     1     2     3     4
```

Ánh xạ môn phái → hệ **đã có sẵn trong game** (xem `data/Animal.getKhangNguHanh` và
`Char.getTkKhangTheoClass`), không phải tự đặt:

| Môn phái | 0 Kiếm khách | 1 Chiến binh | 2 Pháp sư | 3 Đấu sĩ | 4 Cung thủ |
|---|---|---|---|---|---|
| Hệ | Kim | Hỏa | Thủy | Thổ | Mộc |

**Hợp hệ = hệ của SKIN sinh ra hệ của MÔN PHÁI** → **+5% sát thương**. Một chiều thôi: Tiên
Lộc (Thủy) hợp Cung thủ (Mộc) vì Thủy sinh Mộc, nhưng **không** hợp Pháp sư (Thủy) dù Kim
sinh ra Thủy. Mỗi môn phái đúng một skin.

Cộng trong `CharChiSo.applyThanKhiOnHit` — chỗ duy nhất mọi đòn của nhân vật đều đi qua
**sau khi đã trừ giáp và nhân hệ số kỹ năng**, nên 5% là 5% của sát thương thật.

### 5.4 Nội tại có tỉ lệ và hồi chiêu

| Skin | Điều kiện | Hiệu lực | Tỉ lệ | Hồi chiêu |
|---|---|---|---|---|
| Phúc Thú | bị Kiếm khách / Chiến binh / Đấu sĩ đánh | chặn 30% ST, 3 giây | 15% | 40 giây |
| Gấu Trúc | bị Pháp sư / Cung thủ đánh | phản ST +50 điểm (trần 80%), 3 giây | 15% | 40 giây |
| Tiên Lộc | nhận sát thương (bất kỳ) | lá chắn chặn 50% ST, 5 giây | 10% | 40 giây |
| Xích Dương | máu xuống dưới 30% | +50% tấn công, 3 giây | 100% | 40 giây |

**Móc ở `Char.checkHapthuSatThuong` — và chỉ ở đó.** Đó là chỗ **duy nhất** mọi đường sát
thương đánh vào nhân vật đều đi qua: PvP (`MapDanhNhau`, `MapLienDau`, `Region`,
`RegionVantienTran`), quái thường (`Monster`), và **toàn bộ các lớp Boss** (~30 chỗ gọi).
Móc ở `subDam` sẽ **sót sạch đường boss**; móc ở từng nơi gọi sẽ sót khi ai đó thêm đường
mới. `test/KiemSkinLinhThu` chốt lại cả vị trí lẫn sự tồn tại của năm đường gọi.

**Trạng thái chỉ nằm trong bộ nhớ** — hai mảng `long` trên `Char`, tra theo số hiệu skin.
Hồi chiêu 40 giây ngắn hơn mọi phiên chơi nên mất khi đăng xuất là đúng; thêm cột CSDL chỉ
để lưu 40 giây là thêm một chỗ có thể lệch. Tra theo số hiệu skin để đổi skin giữa chừng
không mang buff cũ sang.

**Hồi chiêu phải dài hơn thời gian hiệu lực**, không thì nội tại chạy liên tục và tỉ lệ
thành trang trí. Phép kiểm chốt bất biến này.

**Hiệu ứng hình dùng bốn id không có cơ chế nào kèm theo** — `LIGHT_YELLOW`,
`LIGHT_MAGENTA`, `BUFF_DAM_5`, `BUFF_DAM_10`. Xem mục 10 về cái bẫy ở đây.

**Trần phản sát thương 80% chỉ áp khi nội tại góp vào.** Áp vô điều kiện sẽ âm thầm cắt bớt
phản sát thương của những người đã có trên 80% từ trang bị, dù họ không mặc skin nào.

Hai chỗ phải tự quyết vì bản mô tả không nói rõ:

- **"CB - KK - CT" của Phúc Thú** làm là Chiến binh, Kiếm khách, **Đấu sĩ**. Trên máy chủ
  này Cung thủ là class đánh đòn **phép** (`Char.isCharMagic` = class 2 và 4) nên nó không
  thể vừa ở nhóm vật lý vừa ở nhóm phép — mà dòng Gấu Trúc lại ghi đúng "pháp sư - cung
  thủ" là phép. Phép kiểm chốt hai nhóm phủ kín và không chồng nhau.
- **Xích Dương không có hồi chiêu trong bản mô tả.** Không có hồi chiêu thì "trong 3 giây"
  thành vô nghĩa — cứ ở dưới 30% máu là bật lại ngay. Đặt 40 giây cho đồng bộ.

### 5.5 Mô tả vật phẩm sinh từ chính bảng số

Game này **không có cột mô tả** cho đồ mặc được: không template nào mang mô tả, và
`Item.getInfoSend2Char()` trả chuỗi rỗng. Chỗ duy nhất hiện được chữ tự do là **cột
`name`** — và game đã làm sẵn như vậy (áo Nghê Thường nhét cả bảng mô tả vào `name`, xuống
dòng bằng ký tự xuống dòng thật). Skin đi theo đúng lối đó.

```
Skin linh thú Tiên Lộc
[hệ Thủy] - hợp Cung thủ
- Hợp hệ: +5% sát thương
- Cộng 20% điểm tiềm năng
- Nội tại: khi nhận sát thương, chặn 50% sát thương trong 5 giây.
  Tỉ lệ 10%, hồi chiêu 40 giây
```

Chuỗi này do `SkinLinhThu.moTaVatPham()` **sinh ra từ chính các bảng số**, và
[`tools/SinhSqlTenSkinThu.java`](../tools/SinhSqlTenSkinThu.java) sinh tệp SQL từ hàm đó:

```bash
javac --release 8 -encoding UTF-8 -cp build/sb-src -d build/sb-tools tools/SinhSqlTenSkinThu.java
java -cp "build/sb-tools;build/sb-src" SinhSqlTenSkinThu
```

Vì sao phải sinh chứ không gõ tay: con số **người chơi đọc** nằm ở CSDL còn con số **game
dùng** nằm ở mã nguồn. Hai chỗ, một sự thật — gõ tay là sớm muộn lệch, và lệch kiểu đó
không ai phát hiện trừ khi ngồi cộng tay. Phép kiểm còn đối chiếu lại lần nữa: đổi một số
trong mã mà quên sinh lại SQL là nó báo.

Chuỗi tiếng Việt đi **thẳng từ hằng số Java ra tệp UTF-8**, không qua `sed`/`perl`/heredoc —
xem mục 10.

**Riêng Kim Cang Long (911) — mô tả KHÔNG ghi dải chỉ số ngẫu nhiên.** Ba chỉ số của skin này
do `Item.createAttributeSkinKimCangLong()` roll lúc phát (2–3 trong Bạo kích 1–5% /
Sát thương chuẩn 1–5% / Tăng công 3–15%, lưu `otherAtt[4]/[54]/[47]`) và hiện trong tooltip
chỉ số của từng món. Bản đầu (11/09/2026) ghi thêm dòng "Ngẫu nhiên 2 hoặc 3 trong: …" vào
tên template; người chơi xem món **đã roll** vẫn thấy dòng đó ngay trên các con số thật và
báo là gây hiểu nhầm (12/09/2026, kèm ảnh chụp). Vì `data_item.name` là **một chuỗi dùng chung
cho mọi bản** của vật phẩm, không có cách nào hiện "dải ngẫu nhiên" cho món chưa nhận mà
"số thật" cho món đã nhận — nên bỏ dòng đó khỏi tên: tooltip món đã nhận chỉ còn `[hệ Kim]`,
dòng Chổi Sét và chỉ số thật. Dải ngẫu nhiên là đặc tả để server roll, chỉ còn ở chú thích
`createAttributeSkinKimCangLong` và đoạn này. Skin này chỉ phát qua `GiftCode.AddItem`
(giftcode / lệnh web), không có NPC giới thiệu trước khi nhận. Đổi tên template thì phải chạy
lại `sql/ten_mo_ta_skin_linh_thu.sql` trên CSDL **và khởi động lại** máy chủ (bảng template
nạp một lần lúc bật), đồng thời sửa `sql/gamedb7_clean.sql` + sinh lại `docs/danh_muc_item.md`.

**`otherAtt[54]` (Sát thương chuẩn) là một ngoại lệ về đơn vị lưu trữ — báo lỗi
12/09/2026, kèm ảnh chụp tooltip hiện "0,2%" thay vì đúng khoảng 1–5%.** `Item.getInfoAtbItem()`
gửi thẳng giá trị thô cho client, không quy đổi gì ở server; client tự định dạng theo **một
bảng tra dùng chung cho mọi vật phẩm theo số hiệu `otherAtt`**, không phân biệt vật phẩm nào.
Chỉ số 54 đã có "chủ" từ trước — vũ khí thời trang / Nguyệt Linh Trưởng (`createAttributeVuKhiThoiTrang`)
lưu theo **phần mười phần trăm** (vd `160` nghĩa là 16,0%), nên bảng tra của client chia 10
cho chỉ số này. Kim Cang Long roll thẳng "% thật" (vd `3` nghĩa là 3%) thì bị chia 10 oan,
hiện thành "0,3%".

Sửa: `Item.createAttributeSkinKimCangLong()` nhân 10 trước khi lưu vào `otherAtt[54]` (vd 3%
→ lưu `30`), còn `SkinLinhThu.kimCangLongSatThuongChuan()` chia lại cho 10 khi đọc ra — nên
công thức áp dụng vào nhân vật ở `CharChiSo.java` (nhân sát thương theo % maxHP mục tiêu)
**không phải đổi gì**, vẫn nhận đúng "% thật" như trước. `otherAtt[4]` (Bạo kích) và
`otherAtt[47]` (Tăng công) không dính lỗi này — hai chỉ số đó ở MỌI vật phẩm khác đang dùng
(mặt nạ Bạo kích, pet rồng Tăng công) đều lưu % thật/giá trị thô, không qua bước chia nào,
nên roll thẳng ở Kim Cang Long đã đúng từ đầu.

**Mô tả chỉ ghi thứ đang chạy.** Hứa với người chơi một thứ không tồn tại thì tệ hơn là
không hứa.

### 5.6 Đầu rồng đội trên đầu khi mang Kim Cang Long (12/09/2026)

Đang **hiển thị** skin Kim Cang Long (`SkinLinhThu.hienThi`) thì nhân vật đội thêm đầu
rồng — một **lớp phủ đầu** (`CharVongLap.getIdHeadDynamic` → `map/d_effect/135`, id gửi đi
`8700 + 135`), cùng cơ chế với mặt nạ: client vẽ bằng `paintHair`, chọn khung bằng
`getFrameHair()` = xuống `f`, lên `6 + f`, ngang `12 + f`, mỗi bộ 18 khung, 3 bộ là 3 pha
xoay vòng mỗi 6 tick. Không có vật phẩm riêng — đầu rồng là một phần của chính skin 911.
Yêu cầu gốc: "hoạt ảnh tương tự mặt nạ, còn khung là các ảnh đã gửi".

Ảnh: **chỉ 3 đầu rồng nhìn nghiêng** — ảnh con 25 / 28 / 31 của `map/skinthu/5.png` (atlas
có sẵn đầu tách riêng cho hướng ngang: khung k14–20 ghép thân 24/27/29/30/32 + đầu
25/28/31). Ba ảnh này là **3 pha**, và dùng chung cho **cả ba hướng** xuống/lên/ngang theo
yêu cầu ("chỉ cần 3 ảnh ngang, 6 ảnh còn lại không cần"); quay phải thì client tự lật gương.
Thu 0,6 lần (~23px cao).

*Bẫy đã vấp: tưởng đầu nghiêng dính liền trong sảnh thân rồng (24/29/32) rồi cắt góc
dưới-trái ra — đó là đuôi/chân, không phải đầu; phải đọc bảng khung (mảnh nào ghép vào khung
nào, dx/dy) trước khi đoán mảnh nào là gì.* Bố cục tệp giống hệt mặt nạ 100–104: 3 ảnh con,
54 khung, mỗi khung 1 mảnh, neo tâm (1, −20) như mặt nạ 20×20 tại (−9, −30), độ nhấp nhô theo
6 khung thân chép từ mặt nạ 100. Ô 135 vốn không ai dùng (rà toàn repo + CSDL thật) và **đã nằm
sẵn** trong `id_eff_tool_ngu_long`, nên ghi đè file cũ mà không tốn thêm chỗ nào của trần
255 gói 26.

Thứ tự ưu tiên trong `getIdHeadDynamic`: cải trang/cải tạo toàn thân → **mặt nạ đang đeo**
→ đầu rồng → tóc của bộ thời trang. Nút "Tắt hiển thị skin thú" ẩn cả con rồng lẫn đầu
rồng (dùng `hienThi`, không dùng `dangMang`). `test/KiemSkinLinhThu.kiemDauRongKimCangLong`
chốt bố cục tệp, ô 135 trong gói 26 và thứ tự nhánh. Cần restart server (nạp d_effect lúc
khởi động) và người chơi đăng nhập lại (gói 26 gửi khung hình lúc đăng nhập).

---

## 6. Vì sao làm theo cách này (phần đổi hình)

### 6.1 Không bao giờ ghi vào `Animal.idImg`

`data.Animal.idImg` nghe như "id hình ảnh" nhưng thực ra là **khoá loài thú**. Nó vừa tra
bảng hình ảnh (`Animal.fr`, `idImgPaint`, `rideHorse`, `idImgIcon`, `typeAnimal`) vừa tra
bảng **chỉ số** (`pcTN`, `addMoreSP`, `addMoreBasic`, và gần 50 nhánh `switch` trong
`upLevel` / `upAttribute` / `getGiamKhang…`), lại được lưu xuống `tob_animal.id_img`.

Ghi `idImg` để đổi hình là **mất hẳn loài thú thật** — đúng nghĩa "mất chỉ số", và không
khôi phục được. Vì vậy skin chỉ sống trong `Char.idSkinLinhThu` và chỉ được đọc ở tầng
đóng gói tin.

### 6.2 Client vẽ thú cưỡi bằng hai đường, skin phải đi đường thứ hai

| Đường | Cơ chế | Thêm hình mới được không |
|---|---|---|
| "Ngựa cổ điển" | Server gửi `userHorse`, client tra bảng **hard-code 8 phần tử** (`Char.fHorse`, `sizeHorse`, `hHorse`, `dxh`, `dyh`) | **Không** — bảng nằm trong client, mà client là bản decompile không build lại được. Gửi số ≥ 9 còn làm client ném `IndexOutOfRange`. |
| "Thú cưỡi tool" | Server gửi `idThutool`, client gọi `Char.getPartEff(id)` → `DataSkillEff`, vẽ theo dữ liệu frame nhận từ server | **Được** — hoàn toàn do dữ liệu quyết định. Rồng Băng, Poro, Lân, Sư Tử và hoá hình đều đi đường này. |

Skin đi đường 2, và **phải tắt đường 1 cùng lúc** (gửi `userHorse = 0`) — không thì client
vẽ cả hai, con thú thật và con skin chồng lên nhau.

### 6.3 Dải id ảnh mới tinh 13000–13004

Client giữ ảnh theo id trong `GameData.listImgIcon` và **không hề có số phiên bản**; mục
chỉ bị bỏ khi bảng quá 100 phần tử *và* rảnh quá 120 giây, nên thực tế nó sống đến lúc tắt
hẳn app. Dùng lại một id đã từng phát ảnh khác là người chơi cũ ghép **ảnh cũ với dữ liệu
frame mới** → hiện ra một khối vụn.

Dải `sendByteImg` cũ cao nhất hết ở 12099, còn `idImg` là `short` nên 13000 vẫn hợp lệ.
Chọn dải mới tinh thì không client nào có sẵn ảnh cũ của id đó → khỏi cần cơ chế đẩy ảnh
ép buộc nào cả.

Ảnh cũng để ở thư mục **riêng** `map/skinthu/`, không chen vào `map/d_effect/`: thư mục đó
chỉ nạp id 0..299 (dải 8700..8999) và cả 300 id đều đã có file.

### 6.4 Skin suy ra từ vật phẩm, không có cột DB riêng

`Char.idSkinLinhThu` không lưu xuống DB. Vật phẩm skin nằm trong `Char.awItems`, mà
`awItems` thì đã được lưu sẵn qua `getAllItemInfoSave`. Mỗi lần đăng nhập / mặc đồ / cưỡi
thú đều gọi `SkinLinhThu.dongBoTheoTrangBi` để tính lại, nên **không bao giờ lệch** giữa
"skin đang hiện" và "vật phẩm đang mặc", và không phải thêm cột hay bảng nào.

### 6.5 Vật phẩm skin phải đi nhánh trang bị thú lúc nạp lại

Cả đồ nhân vật lẫn trang bị thú đều được lưu vào **cùng một cột `equip`**
(`Database.getInfoCharSave` nối `getCharWearing()` rồi nối tiếp `awItems`), nên lúc nạp
lại chỉ còn **type** để phân luồng — `Char.setItemWearing()` dùng
`Map.isWearingAnimal(type)` = 14..18.

Vật phẩm skin mang type 30, không nằm trong dải 14..18, nên nếu không chặn nó rơi vào
nhánh đồ nhân vật — và ở đó `POS_WEARING_CHAR` không có ô 30, `get(30)` trả `null` nên
`getIndexItemWearing` ném `NullPointerException` giữa lúc nạp nhân vật.

(Bản đầu tiên đặt type 19 = type Phi phong: khi đó **không** ném lỗi mà âm thầm **chiếm ô
Phi phong** — `POS_WEARING_CHAR[19] = 11` — đăng nhập lại là mất skin, nhân vật lại mọc
thêm áo choàng, và không gỡ ra được. Xem mục 6.7.) Vì vậy `Char.setItemWearing()` và nhánh
nạp legacy đều lọc thêm `SkinLinhThu.laItemSkin`, và cho skin vào `awItems` **bỏ qua
`checkExistAnimal`** — skin gắn theo nhân vật nên bán con thú đi vẫn phải giữ được.

### 6.6 Thiếu file ảnh thì tắt hẳn skin, không tắt một nửa

Bật skin là **tắt** đường ngựa cổ điển (`userHorse = 0`). Nếu lúc đó dữ liệu frame không
nạp được thì đường thú cưỡi tool cũng không vẽ được gì (client vẫn tạo `DataSkillEff`
nhưng `listFrame` rỗng nên `paintTopHorse`/`paintBottomHorse` thoát ngay) → **người chơi
cưỡi hư không**. Nên `SkinLinhThu.dangMang` kiểm đủ cả hai file (`SkinLinhThu.coDuAnh`)
trước khi bật — và vì mọi chỉ số cũng đi qua `dangMang`, thiếu ảnh là **tắt cả sức mạnh**,
không có chuyện được chỉ số mà không thấy hình.

### 6.7 Vật phẩm dùng type 30 — và vì sao không phải 19

Bảng "Trang bị thú" của client có 10 ô, thứ tự type do client quy định sẵn trong
`idWearAnimal`. **Hai bản client đánh số cột phải khác nhau:**

```
bản JAR   : idWearAnimal = {18, 30, 14, 31, 15, 32, 16, 33, 17, 34}   mảng pos dài 36
bản Unity : idWearAnimal = {18, 19, 14, 20, 15, 21, 16, 22, 17, 23}   mảng pos dài 20
 ô chẵn (0,2,4,6,8) = cột trái   |   ô lẻ (1,3,5,7,9) = cột phải
```

Bản Unity được port từ một bản jar cũ hơn nên còn giữ cách đánh số cũ. Bản đầu tiên đặt
type 19 theo bản Unity — **bản Unity vẽ đúng, bản JAR vẽ sai**: nó tra
`posPaintWearing[19]` = `(34, 92)`, mà đó là **ô Phi phong của bảng nhân vật**, nên icon
skin rơi xuống khoảng trống giữa hai cột ở góc dưới.

Đã thống nhất theo cách đánh số của **bản JAR (type 30)**, vì ba lý do:

1. **Bản JAR chạy đúng ngay, không phải đóng gói lại jar client.** Server gửi cột `type`
   của từng template xuống client trong `createMsgLogin` (`MessageCreator.java:834`), nạp
   mới mỗi lần đăng nhập, không cache, không cổng phiên bản — nên đổi `data_item` là tới
   được người chơi ngay sau khi khởi động lại server.
2. **Type 19 vốn là type của Phi phong nhân vật** (`Map.isPhiPhong`). Dùng chung một số
   cho hai thứ khác nhau chính là gốc của cả loạt lỗi ở mục 6.5.
3. **Type 30 không đụng đồ mặc nào.** Trong `data_item` chỉ có đúng dòng `id=30` "ếch
   quỷ" — đó là bản ghi **loài thú**, không phải vật phẩm mặc được. Trang bị thú thật của
   server chỉ có type 14–18 (Giáp, Hộ uyển, Nón, Bàn đạp, Yên cương), đúng bằng
   `Map.isWearingAnimal`; cột phải xưa nay bỏ trống.

Bản Unity đã sửa `idWearAnimal` cho khớp bản JAR ở cả ba màn hình (`WindowInfoScr.cs:361`,
`MenuWindows.cs:229`, `ShopNew.cs:371`).

Vẫn giữ lọc theo **id template** (`SkinLinhThu.laItemSkin`) ở mọi chỗ —
`Map.exchangeItem`, `Map.doUseItem`, `Char.getAnimalWearing` — vì type 30 cũng không nằm
trong dải `isWearingAnimal` (14..18), và `POS_WEARING_CHAR` không có ô 30 nên nếu skin lọt
vào nhánh đồ nhân vật thì `getIndexItemWearing` sẽ ném `NullPointerException`.

Bốn nơi phải khớp số này — `SkinLinhThu.TYPE_ITEM`, cột `type` trong sql, `idWearAnimal`
của ba màn hình Unity, `idWearAnimal` của bản JAR — được `test/KiemSkinLinhThu.java`
(`kiemSoTypeTrangBiThu`) chốt lại.

Máy đã chạy bản sql cũ thì chạy thêm `sql/sua_type_skin_linh_thu.sql`.

---

## 7. Thay bộ ảnh khác

Bản này dùng bộ sprite dựng sẵn, **không** dựng từ bảng vẽ gốc như bản đầu tiên. Mỗi skin
cần đúng hai tệp trong `map/skinthu/`:

| Tệp | Là gì | Ở đâu ra |
|---|---|---|
| `<n>.png` | atlas con thú trên bản đồ | có sẵn trong bộ sprite |
| `<n>` | dữ liệu 21 frame đi kèm atlas | có sẵn trong bộ sprite |

Chép hai tệp đó vào `map/skinthu/` theo số thứ tự 0..4, rồi sinh icon vật phẩm:

```bash
javac --release 8 -encoding UTF-8 -d build/tools-classes tools/TaoIconSkinThu.java
java -cp build/tools-classes TaoIconSkinThu map/skinthu 5 860 map/icon
```

Lệnh này đọc thẳng atlas + dữ liệu frame, ghép lại đúng cách client ghép (mảnh `onTop = 0`
vẽ trước, `onTop = 1` vẽ sau), rồi ghi `map/icon/860+n.png`.

Thiếu một trong hai tệp thì `DataGame.napSkinThu` **tắt hẳn skin đó** — xem mục 6.6.

Bộ ảnh mới thì phải **căn lại chỗ ngồi** — xem mục 9, và hai công cụ
`tools/CanChoNgoiSkinThu.java` (dọc) + `tools/CanNgangSkinThu.java` (ngang).

Muốn soi hình học trước khi dùng (con thú so với chỗ người ngồi):

```bash
java -cp build/tools-classes XemSkinNhuTrongGame xem.png map/d_effect/56 map/skinthu/0
```

Bản đầy đủ hơn — vẽ đúng thứ tự vẽ của client (thân người → thú → đầu người; hướng lên thì
thú trước), lật gương hướng phải như client, kèm hình que người ngồi, dò yên đỏ bằng màu, và
**thử tham số ngay trong bộ nhớ** (dòng bảng dx/dy, dịch cả khối, dịch riêng vài ảnh con)
trước khi sửa tệp:

```bash
javac -encoding UTF-8 -d build/tools-classes tools/ThuChoNgoiSkinThu.java tools/DichSkinThu.java
# ra.png  tep  dx(xuong,len,trai)  dy(xuong,len,trai)  dich-dy-khoi(xuong,len,ngang)  dich-dx-ngang  phong-to  [anh:dx]
java -cp build/tools-classes ThuChoNgoiSkinThu thu.png map/skinthu/5 "0,0,1" "-28,-10,-15" "0,0,0" 0 5
# thử dời riêng vài ảnh con trong bộ nhớ (ví dụ đuôi Kim Cang Long +20px — đã thử rồi bỏ, xem mục 9):
java -cp build/tools-classes ThuChoNgoiSkinThu thu.png map/skinthu/5 "0,0,1" "-28,-10,-15" "0,0,0" 0 5 "2,5,10:20"
```

Ưng con số rồi thì ghi vào tệp bằng `tools/DichSkinThu` (dịch tương đối, sửa tại chỗ — chạy
lại với số đối dấu là hoàn tác):

```bash
java -cp build/tools-classes DichSkinThu map/skinthu/5 khoi xuong dy -24
java -cp build/tools-classes DichSkinThu map/skinthu/5 anh 2,5,10 dx 20    # rồi ... dx -20 để trả lại
```

Còn **dòng bảng bù người ngồi** (`Char.dx_horse_tool` / `dy_horse_tool`, tra theo
`SkinLinhThu.INDEX_DXY[skin]`) thì sửa trong mã nguồn — xem mục 9, phần Kim Cang Long.

### Kiểm bộ ảnh trước khi dùng

Điều duy nhất phải kiểm bằng tay là **đúng 21 frame** và mọi ô nằm trong biên atlas. Đọc
byte đầu tiên ra số ảnh con `n`, nhảy `1 + n*5` byte rồi đọc `short` — phải ra 21. Nếu bộ
sprite ít hơn 21 frame thì con thú biến mất đúng tick đó, xem "Ba cái bẫy" dưới đây.

### Chọn tư thế: để tool làm, đừng chọn tay

`--xuong` / `--len` / `--ngang` là **danh sách ứng viên**, không phải thứ tự frame. Cứ
liệt kê hết các ô thuộc hướng đó rồi tool tự chọn 5 và tự xếp:

- hai tư thế **giống nhau nhất** → lúc đứng yên
- bộ ba **ít lệch nhau nhất** → lúc di chuyển
- không tìm được bộ nào đủ giống thì tự rút xuống lặp lại tư thế (`A-B-A`, hoặc đứng im
  hoàn toàn), và in ra để biết

Lý do phải làm vậy: các bảng này **không phải bộ frame của một hoạt ảnh**, mà là nhiều
bức vẽ rời rạc, mỗi bức một cỡ. Lấy theo thứ tự trong bảng thì các tư thế lệch nhau
14–48% về hình dáng và tới 48% về bề ngang — con thú đổi cả kích thước lẫn hình dáng mỗi
khi đổi frame, nhìn rất giật. Cho tool chọn theo độ giống nhau thì còn 0–25%.

Toạ độ `dx` của từng frame cũng không lấy theo tâm khung bao (khung bao đổi theo đuôi/tai
thò ra) mà tính bằng cách **thử các độ lệch, chọn cái làm frame chồng khít với frame mẫu
nhất**. Cách này giữ *thân* đứng yên thay vì giữ *khung bao* đứng yên.

### Ba cái bẫy khi làm sprite

1. **Phải đúng 21 frame** (bội của 21). Client không đọc số frame từ file, nó tính chỉ số
   bằng công thức cố định `Char.getFrameHorse`: xuống `+0`, lên `+7`, ngang `+14`. Slot 5,
   6 không dùng nhưng **phải tồn tại**, thiếu là `paintTopHorse` thoát sớm và con thú biến
   mất đúng tick đó — nhìn như nhảy giật.

   Trong mỗi khối 7, **`Fhorse` chỉ nhận hai tập rời nhau**:

   | Trạng thái | Nhánh trong `Char.paint` | `Fhorse` |
   |---|---|---|
   | Đứng yên | thú cưỡi tool làm `useHorse = -1` → `frame = STANDFRAME[p1]`, `STANDFRAME = {0×8, 1×4}` | **0, 1** |
   | Di chuyển | `if (idhorse != -1)` → `Fhorse = horseMove[fh]`, `horseMove = {2,2,2,3,3,4,4,4}` | **2, 3, 4** |

   Nên slot 0–1 là *tư thế đứng*, slot 2–3–4 là *tư thế đi*, không dùng chung slot nào.
   Chu kỳ đi là vòng `2→3→4→2` nên thứ tự trong bộ ba không ảnh hưởng độ mượt (tổng ba
   cạnh của vòng không đổi), chỉ việc **chọn đúng bộ ba** mới ảnh hưởng.

2. **`onTop` bị đảo.** `Client/FrameEff` có hàm tạo bị đảo tên tham số: part có `onTop==0`
   lại rơi vào `listPartTop`. Kết quả thực tế: `onTop = 0` vẽ trước (bóng đổ), `onTop = 1`
   vẽ sau (thân con thú).

3. **Ảnh phải nhìn sang TRÁI.** Client lật ngang khi đi sang phải
   (`paintTopHorse(..., (dir == RIGHT) ? 2 : 0)`). Bảng nào vẽ nhìn phải thì thêm `--lat`.

---

## 8. Các điểm đã nối vào code gốc

**Phần hình ảnh**

| File | Nối gì |
|---|---|
| `DataGame.load` | Nạp `map/skinthu/` (`napSkinThu`) |
| `DataGame.sendByteImg` | Phục vụ dải id 13000..13004 |
| `DataGame.nIconItem` | 860 → 865 để phục vụ icon 860..864 |
| `MessageCreator` gói 26 | Gửi kèm dữ liệu 21 frame của 5 skin (client chỉ có **một** chỗ ghi vào `DataSkillEff.ALL_DATA_EFF`, chính là vòng lặp đọc gói này) |
| `MessageCreator.createCharInfo` | `userHorse = 0`, `fly`, `idThutool`, `indexdxy` |
| `MessageCreator.createMainCharInfoMessage` | y hệt trên (phải giống nhau, không thì mình và người bên cạnh thấy hai con thú khác nhau) |
| `MessageCreator.createAnimalWearingMessage` | Ảnh 3 frame bảng Trang bị thú |
| `Map.doSendDetailChar` | Ảnh bảng Trang bị thú khi người khác xem mình |

**Phần vật phẩm**

| File | Nối gì |
|---|---|
| `Map.exchangeItem` | Chặn vật phẩm skin, đẩy sang đường trang bị thú |
| `Map.doUseItem` | Coi vật phẩm skin là trang bị thú; đồng bộ + gửi lại sau khi mặc |
| `Map.doChat` | Lệnh `boskin` |
| `MapDoMenu` | NPC đổi thưởng (`-90`) → *Gỡ skin linh thú*, đọc `idOptionMenu` |
| `Map` đổi thưởng | 5 mục mới |
| `Char.getAnimalWearing` | Nhận vật phẩm skin bất kể `idAnimal`, buộc lại vào con đang cưỡi |
| `Char.setItemWearing` | Chặn skin rơi vào nhánh đồ nhân vật |
| `Item.attribShowedForType` | Dài 36 thay vì 30 (type 30..34 phải tra được) |
| `doRideAnimal` | Đồng bộ skin khi cưỡi thú, rồi **tính lại chỉ số sau đó** |
| `doUnRideHorse` | Gỡ skin và trả vật phẩm về hành trang khi xuống thú |
| `SelectCharHandler` | Đồng bộ skin lúc đăng nhập, rồi **tính lại chỉ số** trước gói đầu tiên |
| `SkinLinhThu.dongBoVaGui` | Tính lại chỉ số **trước** khi dựng gói tin |

**Phần chỉ số và nội tại**

| File | Nối gì |
|---|---|
| `Char.idSkinLinhThu` | Skin đang mang |
| `Char.skinHetHieuLuc` / `skinHetHoiChieu` | Trạng thái nội tại (chỉ trong bộ nhớ) |
| `Char.tnSucManh/tnKheoLeo/tnTinhThan/tnSucKhoe` | +20% tiềm năng, tính tại chỗ |
| `Char.calculateAttrib` | Xuyên giáp → `attNguHanh[3]` |
| `Char.calculateAccurate` | +200 chính xác (cuối hàm) |
| `Char.checkHapthuSatThuong` | Kích hoạt nội tại + hấp thụ + chặn sát thương |
| `Char.buffAttackSkill` | Phản sát thương của Gấu Trúc, trần 80% |
| `Char.checkKick5AttAnimal` | Rào chỉ số `groupKickAnimal` (xem mục 10) |
| `CharChiSo.calculateMaxHPMP` | +HP/+MP, và +20% tiềm năng |
| `CharChiSo.calculateDefend` | +20% thủ (bốn bộ đếm), và +20% tiềm năng |
| `CharChiSo.calculateAttack` | +20% tiềm năng |
| `CharChiSo.attackDam` | +50% tấn công của Xích Dương |
| `CharChiSo.applyThanKhiOnHit` | +5% sát thương khi hợp hệ |
| `MessageCreator` bảng chỉ số | Cộng phần tiềm năng vào số người chơi nhìn thấy |

## 9. Giới hạn đã biết

- ~~**Icon skin vẽ sai vị trí trong bảng "Trang bị thú"**~~ — **đã sửa, cả hai bản client.**
  Gốc lỗi không phải ở cách vẽ mà ở **số type** — xem mục 6.7. Bản JAR **không phải đóng
  gói lại**: chỉ cần chạy `sql/sua_type_skin_linh_thu.sql` rồi khởi động lại server.
- ~~**Người ngồi lơ lửng / "rỗng mất thân"**~~ — **đã sửa bằng cách căn lại dữ liệu**, cả
  trục dọc lẫn trục ngang. Số hiện tại (đo bằng logic của hai công cụ căn):

  | | lụt xuống | lụt lên | lụt ngang | che chân người |
  |---|---|---|---|---|
  | Poro (`d_effect/56`, mốc chuẩn) | 12 | 20 | 22 | 51% |
  | Lân (`d_effect/64`) | 15 | 21 | 17 | 44% |
  | 0 Phúc Thú | 12 | 20 | 22 | 65% |
  | 1 Gấu Trúc | 12 | 20 | 18 | 67% |
  | 2 Tiên Lộc | 12 | 20 | 22 | 55% |
  | 3 Xích Dương | 12 | 20 | 19 | 68% |
  | 4 Chiến Lang | 12 | 20 | 20 | 65% |

  Cột cuối là thứ quan trọng nhất và cũng là thứ tôi đo sai hai lần trước khi tìm ra gốc:
  client vẽ con thú ở `x` nhưng vẽ **người** ở `x + Char.dx_horse[0][dir]` = `x + 7` khi
  quay ngang, mà cưỡi thú tool thì client **không vẽ chân người** (`Char.java:1838` bỏ ô
  thứ tự 0 khi `idhorse != -1`) — nó trông cậy hoàn toàn vào con thú che chỗ đó. Sprite hẹp
  và lệch trái thì nửa dưới bên phải của người thò ra ngoài, nhìn xuyên thấy nền đất.
  **Tiên Lộc vẫn yếu nhất (55%)** vì là con nai mảnh; dịch hết giới hạn cũng chỉ tới đó,
  muốn hơn thì phải vẽ lại ảnh cho dày thân.
  `test/KiemSkinLinhThu.kiemCheChanNguoi` chốt lại mọi skin phải che ≥ mức của Poro.

  ### Kim Cang Long (skin 5) — dòng bảng riêng, không dùng "lụt" của thú bốn chân

  Ba lần chỉnh "lụt" khối xuống (12px như Poro → người nổi lơ lửng; căn đáy về 0..7px →
  người chui hẳn vào đầu rồng; mức giữa 47px) đều bị người chơi báo lỗi kèm ảnh chụp, vì cả
  ba chỉ dời **con rồng** mà vẫn dùng **dòng 0** của bảng `dx/dy_horse_tool` (dòng của Poro):
  hướng ngang người bị đẩy +7px về phía đuôi trong khi yên nằm đúng tâm thân; hướng xuống
  thì rồng chạm đất là người (nhấc −20) chui vào đầu rồng, nâng rồng thì chân rồng rời đất.

  **Hoá ra bảng đó không cố định như hai chú thích cũ nói.** Server gửi nguyên bảng kèm
  **số dòng** trong gói 26 (`MessageCreator`, khối `dx_horse_tool`), và cả bản Unity
  (`MessageHandler.cs:1795`) lẫn bản JAR (`MessageHandler.java:1754`) đều đọc số dòng từ gói
  rồi tra `dy_horse[typehorse][dir]` với `typehorse` = `indexdxy` server gửi. Rồng Lớn của
  game vốn đã dùng dòng 3 riêng. Nên **thêm dòng là đủ, không phải đóng gói lại client nào.**

  Sửa 11/09/2026, ba việc tách nhau:

  | Việc | Ở đâu | Số |
  |---|---|---|
  | Rồng đứng **chạm đất** ở khối xuống (trước đó lọt dưới vạch chân tới +27px) | `map/skinthu/5`, khung 0..6 dịch dy −24 | đáy +3 (Poro +5) |
  | Người đi theo **dòng 5** riêng của bảng | `Char.dx_horse_tool[5]` = `{0,0,+1,−1}`, `dy_horse_tool[5]` = `{−28,0,−15,−15}`, `SkinLinhThu.INDEX_DXY[5] = 5` | ngang: người vào đúng tâm yên; xuống: người ngồi sau đầu rồng (bắt đầu từ −23 = quan hệ của Rồng Lớn dòng 3, rồi nâng thêm 5 theo ảnh chụp người chơi gửi); lên: hạ từ −15 → −10 → **0** qua hai lần ảnh chụp — ở −10 thân người vẫn *đậu trên* yên, cả yên đỏ lộ nguyên bên dưới (hướng lên client vẽ thú trước nên yên không thể che người, cách duy nhất cho "ngồi trong yên" là hạ thân xuống che phần trên yên); ở 0 chỉ còn mép sau yên lộ ra, giống hướng ngang |
  | **Sợi đuôi cuộn lên** ở khối xuống (ảnh con 2, 5, 10): 11/09 dời sang phải +20px, **12/09 trả về chỗ gốc** theo ý người chơi | `map/skinthu/5` | đuôi giữ nguyên như gốc, x < 7 (chạy xuyên cột người) |

  Vì sao từng dời đuôi, rồi vì sao trả lại: client **luôn vẽ con thú SAU thân người**
  (`Char.cs` `paint`, nhánh `orderPaint == 1`: `paintBody` rồi mới `paintTopHorse`/
  `paintBottomHorse`, chỉ hướng lên là ngược lại) — không có cách nào cho một mảnh thú nằm
  *sau lưng* người ở hướng xuống. Đuôi mọc giữa lưng thì vẽ đè ngang thân người cưỡi (che
  49% thân với người ở −20; dời +20px thì còn 18%). Nhưng người chơi nhìn bản đã dời và hỏi
  *"sao đuôi lại bị thay đổi chứ không phải vị trí nhân vật"* — họ thích đuôi ở chỗ gốc hơn
  là thân người lộ rõ, và chọn trả lại dù đã được báo trước là đuôi sẽ cắt ngang thân
  (~65–74% với người ở −28, vì rồng đã nâng 24px còn người chỉ nhấc 8px). Đo các mức thử:
  0px → 65–74%, +10 → 40–52%, +15 → 17–27%, +20 → 13–24%. **Bài học: dời một bộ phận của
  con thú là đổi *hình dáng* nó — phải hỏi người chơi trước, kể cả khi số đo nói là tốt hơn.**
  Chỉ hướng xuống dùng ba ảnh con này; hướng lên và ngang không hề đổi trong cả hai lần.

  Hai điều phải giữ khi thêm dòng: `dy` dòng mới **không được cao hơn dòng 0 quá ~10px**
  (dy âm hơn dòng 0) vì `paintName` của client (`Char.cs:1520`: `num = dy_horse[0][dir]`,
  `Char.java:2081`) luôn đặt tên nhân vật theo dòng 0 — người vẽ cao hơn dòng 0 bao nhiêu
  thì tên sát đầu thêm bấy nhiêu (ảnh chụp thực tế ở dy −23 tên còn cách đỉnh đầu ~14px, −28
  còn ~9px). Ngược lại **thấp hơn dòng 0 thì không sao**, chỉ làm tên cách đầu xa thêm —
  Kim Cang Long hướng lên ở 0 thấp hơn dòng 0 (−15) tới 15px là cố ý, chấp nhận được;
  và dòng mới phải là **dòng cuối** để không ai chèn vào giữa mà quên đổi `INDEX_DXY`. `test/KiemSkinLinhThu.kiemChoNgoiKimCangLong` chốt cả bảng
  lẫn dữ liệu (đáy chạm đất, đuôi ở đúng vị trí gốc — không ai "sửa giúp" lại mà không hỏi).

  Đo trước khi sửa bằng `tools/ThuChoNgoiSkinThu` (vẽ đúng thứ tự vẽ của client, kể cả
  hướng phải lật gương, kèm hình que người ngồi và dò yên đỏ bằng màu), dời số bằng
  `tools/DichSkinThu` — xem mục 7.
- **Icon con thú nhỏ cạnh tên nhân vật vẫn là của linh thú thật** (`Animal.idImgIcon`).
  Cố ý: nó cho biết con linh thú thật là con gì, đúng tinh thần "chỉ số của con thú vẫn là
  của nó". Tên và chỉ số con thú trong bảng thông tin cũng vậy.
- **Bang chủ đang giữ thành** không hiện skin, và do đó **không được chỉ số nào**. Cả hai
  chỗ ghi `idThutool` đều nằm trong `if (animalRide != null && !isMasterClanAndHaveTown())`,
  nên với người đó id ảnh skin không bao giờ được gửi; nếu vẫn tắt đường ngựa cổ điển thì
  họ **mất hẳn con thú**. Vì vậy `SkinLinhThu.dangMang` chặn sẵn trường hợp này.
- **Thú đang hoá hình + mang skin**: skin thắng (đè lên hình hoá hình). Nhánh hoá hình
  ghi `-1` cho `userHorse`/`fly`/`idImgHorse` rồi bỏ qua, còn `idThutool` phía dưới vẫn
  đổi sang skin — an toàn vì bản thân hoá hình cũng đi đường thú cưỡi tool.
- **Số hiệu ứng trong gói 26 đã chạm đúng trần 255** (6 skin + 249 id, từ 12/09/2026).
  Dữ liệu skin đã được xếp **trước** danh sách hiệu ứng nên nếu có ngày tràn thì bị cắt là
  hiệu ứng chứ không phải skin — cắt hiệu ứng chỉ làm hiệu ứng đó không vẽ, còn cắt skin
  thì người đang mặc skin sẽ cưỡi hư không. **Đã tràn thật một lần** (256 → cắt mất id
  9000 "Sét đánh", xem mục 10). Muốn thêm hiệu ứng mới thì gỡ một id trong danh sách dự
  phòng đã rà soát là không dùng (ghi ngay trên mảng `id_eff_tool_ngu_long`).
- **Nội tại mất khi đăng xuất.** Cố ý — hồi chiêu 40 giây ngắn hơn mọi phiên chơi, xem
  mục 5.4.

## 10. Bẫy đã gỡ (ghi lại để đừng đặt lại)

**Mảng tra theo type phía server**

- **`Item.attribShowedForType` dài 30 mà skin mang type 30.** `getInfoAtbItem` ném
  `ArrayIndexOutOfBoundsException` **ngay giữa lúc dựng gói tin**, hỏng cả
  `createAnimalWearingMessage` lẫn `createCharInventoryMessage`: người chơi thấy bảng Trang
  bị thú **rỗng** và vật phẩm biến mất khỏi hành trang, dù dữ liệu vẫn còn nguyên. Lỗi bị
  `try/catch` ở ngoài nuốt nên chỉ thấy hậu quả, không thấy lỗi. Đã nâng lên 36 hàng.
  *Bài học: soát mảng tra theo type ở cả hai phía, lần đó chỉ soát phía client.*
- **`checkKick5AttAnimal` tra `groupKickAnimal[type - 14]`, mà mảng đó dài đúng 5**
  (type 14..18). Skin type 30 → chỉ số 16 → văng, và rơi vào một `catch` **rỗng** nên im
  lặng. Chưa nổ vì skin đang có `heItem = -1`, nhưng `heItem` là trường của **từng món** và
  có nhiều đường gán nó (`ItemChangeColor`, nâng cấp ngũ hành). Đã rào bằng chính độ dài
  mảng. Rào **không được dùng `continue`**: vòng lặp đó tăng biến đếm ở cuối thân vòng.

**Hiệu ứng và cơ chế ẩn**

- **`EFF_SHIELD_1` / `EFF_SHIELD_2` không phải hiệu ứng hình suông.**
  `Char.getPcGiamSatThuong` và `getBackDam` đọc hai id đó và tự động cho **15%/30% giảm sát
  thương cộng 20%/30% phản đòn**. Dùng chúng làm hình cho lá chắn thì muốn 30% mà ăn 45%,
  không ai biết là sai. Nội tại skin dùng `LIGHT_YELLOW`, `LIGHT_MAGENTA`, `BUFF_DAM_5`,
  `BUFF_DAM_10` — đã soát cả mã nguồn: không được đọc ở bất cứ đâu, `stune` bằng 0.
- **`checkHapthuSatThuong` có một dòng gán đè.** Khối hấp thụ của linh thú viết `a = …` chứ
  không phải `a += …`, nên nó **xoá sạch** mọi thứ cộng vào trước — cụ thể là 3% nội tại
  của Lân. Đã đổi sang cộng dồn.

**Chỉ số dẫn xuất là trường được lưu sẵn, phải tính lại đúng lúc**

- **`attack` / `maxhp` / `defend_physic` / `accurate` chỉ đổi khi `calculateAttrib()`
  chạy**, còn bảng điểm tiềm năng lại được tính **tươi** ngay lúc dựng gói tin. Thiếu lời
  gọi `calculateAttrib()` thì người chơi **thấy tiềm năng tăng mà tấn công đứng im** —
  trông như skin chỉ cộng một nửa. Đúng lỗi đã bị báo, và nó có **ba lỗ**:
  `dongBoVaGui` không gọi (mặc/tháo skin), `doRideAnimal` gọi **trước** khi đồng bộ skin
  (lên thú), `SelectCharHandler` không gọi lại sau khi đồng bộ (đăng nhập).
- **Ở đường cưỡi thú, lời gọi đó phải VÔ ĐIỀU KIỆN**, không được gắn vào
  `dongBoTheoTrangBi(...) == true`. `dangMang()` phụ thuộc vào **cả `animalRide`**, mà lên
  thú thì số hiệu skin *không* đổi (đã có sẵn từ lúc đăng nhập) — cái đổi là `animalRide`.
  Gắn vào điều kiện đó là chỉ số skin không bao giờ bật khi lên thú.
- `test/KiemSkinLinhThu.kiemTinhLaiChiSo` chốt **thứ tự**, không chỉ sự có mặt: sai thứ tự
  thì chạy đúng cú pháp mà vẫn không ăn gì, y hệt lúc chưa sửa.

**Chỉ số được lưu xuống CSDL**

- **Không bao giờ ghi tạm vào `strength`/`agitity`/`spirit`/`health`.** Bốn trường đó được
  `DatabaseNhanVat` gộp chuỗi ghi thẳng xuống CSDL. Cách "nâng lên → tính → trả lại" thua
  một lần là số phồng nằm vĩnh viễn và còn phồng tiếp. Xem mục 5.2.
- **`upAbility` không hề được đặt lại** mỗi lần `calculateAttrib()` chạy, và là `byte[5]`.
  Cộng vào là dồn vô hạn rồi tràn kiểu thành số âm. `attNguHanh` thì **có** được đặt lại
  (trong `resetNguHanh`) nên cộng vào được — nhưng phải cộng **sau** lời gọi đó.

**Gói tin và giao diện**

- **Không gửi `createAnimalWearingMessage` cho người xung quanh.** Gói đó vào
  `GameScr.onAnimalWearingInfo`; khi `charID` khác nhân vật của người nhận thì nó gọi
  `onViewAnimalInfo` → `WindowInfoScr.switchToMe()` + `Canvas.endDlg()` — **đổi hẳn màn
  hình của họ** sang bảng "Trang bị thú" của mình, cắt ngang việc họ đang làm. Đổi skin
  qua lại sẽ thành công cụ phá người khác. Họ cũng không cần gói đó: dữ liệu duy nhất nó
  mang là `imgAnimal`, mà `imgAnimal` chỉ dùng vẽ trong cửa sổ thông tin chứ không dùng
  vẽ trên bản đồ. Hình trên bản đồ đi bằng `createCharInfo`.
  *(Đã gỡ dòng tương tự trong `doRideAnimal.java` — người chơi báo bị hất màn hình khi
  đứng gần ai đó lên thú.)*
- **Menu NPC đọc `idOptionMenu`, không phải `idMenu`.** `idMenu` là số hiệu **trang** menu,
  `idOptionMenu` mới là **dòng** người chơi chọn. Menu một dòng che mất lỗi này vì cả hai
  cùng bằng 0.
- **Không nâng `nIconItem` quá số icon thật sự thêm vào.** Id nào `< nIconItem` mà thiếu
  file thì `allImg[id]` là null, `sendByteImg` ném NPE rồi bỏ qua cả lệnh gửi → client
  hỏi mãi không bao giờ được trả lời. Thêm 5 icon (860..864) thì nâng đúng lên 865.
- **`exchangeItemAnimal` so cả `type` lẫn `idAnimal`** khi gỡ món cũ ra. Với skin (gắn
  theo nhân vật) phải bỏ điều kiện `idAnimal`, không thì món skin cũ đang dính vào con
  thú trước đó sẽ không bị gỡ — người chơi mặc skin mới mà không có gì xảy ra, cũng không
  có thông báo nào.
- **Số hiệu ứng trong gói 26 tối đa 255** với client cũ (< 2.7 đọc `readUnsignedByte`;
  2.7+ đọc short nên không cắt). Skin thứ 6 (Kim Cang Long) + hiệu ứng "Sét đánh" (id
  300 → 9000) đã đẩy lên **256**: đoạn cắt-bớt hoạt động đúng như thiết kế, cắt từ cuối —
  và phần tử cuối chính là 300, nên **"Sét đánh" không bao giờ vẽ được trên client cũ**
  (757/782 lượt đăng nhập là 2.0.9), server thì vẫn cộng 3 giây choáng. Log
  `CANH BAO: goi 26 co 256 hieu ung ... Bo bot id 9000` xuất hiện 422 lần trước khi có
  người báo (12/09/2026). Sửa: gỡ id 131 (không dùng — rà toàn repo + CSDL thật) để về đúng
  255, và `test/KiemGoi26SetDanh.java` chốt tổng ≤ 255 để lần sau vượt là test đỏ ngay.
  *Bài học: dòng cảnh báo trong log không thay được test — 422 lần in ra mà không ai đọc.*
- **`EffectBuff.dyhore` phải dài đủ tới id d_effect lớn nhất** (`createMsgNewEffectSkill5Long`
  tra `dyhore[idEff - 8700]`). Dài 300 thì id 300 (9000) tràn mảng, bị `catch` nuốt và gói
  `-74` gửi thiếu byte cuối — nâng lên 301 cùng lúc với sửa trên. Thêm id d_effect mới
  (301+) là phải nâng cả `dyhore` lẫn `DataGame.nMonster`.
- **"Sét đánh" của Kim Cang Long chỉ phát động trong PvP, và khi mình BỊ đánh.** Không
  phải lỗi — là hệ quả của cơ chế Sét Lan gốc: `checkNewEffectItem` chạy khi nhân vật
  **nhận** đòn (từ người → lan sang người xung quanh; từ quái → lan sang quái), và nhánh
  choáng của Sét Lan chỉ có cho mục tiêu là **người chơi** (`actor.cat == 0`) — quái
  không có trạng thái choáng (`Monster` không đọc `CHOANG`). "Sét đánh" chỉ là "+3 giây
  vào cú choáng đó" nên nó bám theo đúng giới hạn ấy. Đánh quái để test sẽ không bao giờ
  thấy. Tỉ lệ thật mỗi lần bị đánh = P(Sét Lan phát động) × 5%.
- **Icon không được đặt lại vào dải 855..859.** Bản skin đầu tiên dùng dải đó, nhưng sau
  khi tính năng bị gỡ, đúng năm id ấy đã được giao cho **Cánh** (commit `2c651ce`) làm ảnh
  ô xem trước cửa sổ Trang bị. Đặt đè lên là ô xem trước của Cánh hiện ra con thú. Bản này
  dùng 860..864 và nâng `nIconItem` lên 865.

**Sửa văn bản tiếng Việt**

- **Đừng sửa file tiếng Việt bằng `perl` đọc với `:encoding(UTF-8)` rồi ghi không đặt lớp
  mã hoá cho đầu ra.** Nó chỉ in một dòng cảnh báo `Wide character in print` rồi **vẫn
  ghi** — toàn bộ tiếng Việt trong file bị mã hoá hai lần. Lỗi này **âm**: file vẫn là
  UTF-8 hợp lệ nên `iconv` vẫn báo đạt, `javac` vẫn biên dịch trót lọt, không có cảnh báo
  nào lúc build. Chỉ mở game lên nhìn mới biết. Đã hỏng 178 dòng ở hai file như vậy.
  Dùng `sed r <tep>` (theo byte), hoặc tốt hơn là **sinh chuỗi thẳng từ Java ra UTF-8** như
  `tools/SinhSqlTenSkinThu.java`. `test/KiemBangMaNguon.java` quét toàn bộ mã nguồn để chặn
  việc này tái diễn — nhận biết bằng **cặp ký tự**, không phải từng ký tự lẻ (`Ã` là chữ
  cái tiếng Việt thật, trong "MÃ", "XÃ", "ĐÃ").
