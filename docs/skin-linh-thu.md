# Skin linh thú

Trang bị vật phẩm skin → **con linh thú (thú cưỡi) đang cưỡi đổi hình, đổi hoạt ảnh và
đổi cách di chuyển**. Chỉ số của con linh thú giữ nguyên tuyệt đối.

Code: [`src/real/SkinLinhThu.java`](../src/real/SkinLinhThu.java) —
tool sinh icon: [`tools/TaoIconSkinThu.java`](../tools/TaoIconSkinThu.java) —
SQL: [`sql/skin_linh_thu.sql`](../sql/skin_linh_thu.sql)

---

## 1. Cài đặt

```bash
mysql -u root -p <ten_db> < sql/skin_linh_thu.sql
```

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
| Tháo skin | Chọn ô skin trong bảng Trang bị thú rồi bấm **Tháo** (phím trái), hoặc gõ `boskin` trong chat |
| Xem đang mang gì | Mở bảng Trang bị thú, hoặc gõ `boskin` khi không mang skin sẽ báo |

Xuống thú thì skin tự ẩn (không có con thú thì không có gì để đổi hình); cưỡi lại là hiện
lại. Đổi sang con linh thú khác vẫn giữ skin — **skin gắn theo nhân vật, không theo từng
con thú**.

## 3. Nguồn phát skin

| Nguồn | Cách |
|---|---|
| Đổi điểm | NPC đổi thưởng, 5 mục cuối danh sách, 2000 điểm mỗi skin |
| Giftcode | `DB:905:1:0` … `DB:909:1:0` (id template : số lượng : số phút, 0 phút = vĩnh viễn) |

Bản đầu tiên còn có lệnh admin `tangskin <tên> <0-4>`. Bản này **không có** — hai nguồn
trên là đủ, và giftcode làm được đúng việc mà lệnh đó làm.

## 4. Năm skin

| # | Tên | Item id | Icon | Ảnh bản đồ | Sprite nguồn |
|---|---|---|---|---|---|
| 0 | Phúc Thú | 905 | 860 | 13000 | `8907` — thú hoa hồng lá xanh |
| 1 | Gấu Trúc | 906 | 861 | 13001 | `8906` — gấu trúc cánh băng |
| 2 | Tiên Lộc | 907 | 862 | 13002 | `8908` — hươu gạc pha lê |
| 3 | Xích Dương | 908 | 863 | 13003 | `8905` — thú lửa bờm cháy |
| 4 | Chiến Lang | 909 | 864 | 13004 | `8909` — hươu nâu gạc trắng |

Cột "sprite nguồn" là tên tệp trong bộ được đưa vào. **Tên gán theo hình chứ không theo
thứ tự tệp**: bộ sprite đánh số 8905..8909 nhưng con hoa hồng hợp với "Phúc Thú" hơn là
con lửa, nên thứ tự bị hoán lại. Đổi thứ tự này thì phải đổi lại tệp trong `map/skinthu/`,
không phải đổi code.

Đổi id ở SQL thì phải đổi `SkinLinhThu.ID_ITEM` cho khớp rồi build lại.

---

## 5. Vì sao làm theo cách này

### 5.1 Không bao giờ ghi vào `Animal.idImg`

`data.Animal.idImg` nghe như "id hình ảnh" nhưng thực ra là **khoá loài thú**. Nó vừa tra
bảng hình ảnh (`Animal.fr`, `idImgPaint`, `rideHorse`, `idImgIcon`, `typeAnimal`) vừa tra
bảng **chỉ số** (`pcTN`, `addMoreSP`, `addMoreBasic`, và gần 50 nhánh `switch` trong
`upLevel` / `upAttribute` / `getGiamKhang…`), lại được lưu xuống `tob_animal.id_img`.

Ghi `idImg` để đổi hình là **mất hẳn loài thú thật** — đúng nghĩa "mất chỉ số", và không
khôi phục được. Vì vậy skin chỉ sống trong `Char.idSkinLinhThu` và chỉ được đọc ở tầng
đóng gói tin.

### 5.2 Client vẽ thú cưỡi bằng hai đường, skin phải đi đường thứ hai

| Đường | Cơ chế | Thêm hình mới được không |
|---|---|---|
| "Ngựa cổ điển" | Server gửi `userHorse`, client tra bảng **hard-code 8 phần tử** (`Char.fHorse`, `sizeHorse`, `hHorse`, `dxh`, `dyh`) | **Không** — bảng nằm trong client, mà client là bản decompile không build lại được. Gửi số ≥ 9 còn làm client ném `IndexOutOfRange`. |
| "Thú cưỡi tool" | Server gửi `idThutool`, client gọi `Char.getPartEff(id)` → `DataSkillEff`, vẽ theo dữ liệu frame nhận từ server | **Được** — hoàn toàn do dữ liệu quyết định. Rồng Băng, Poro, Lân, Sư Tử và hoá hình đều đi đường này. |

Skin đi đường 2, và **phải tắt đường 1 cùng lúc** (gửi `userHorse = 0`) — không thì client
vẽ cả hai, con thú thật và con skin chồng lên nhau.

### 5.3 Dải id ảnh mới tinh 13000–13004

Client giữ ảnh theo id trong `GameData.listImgIcon` và **không hề có số phiên bản**; mục
chỉ bị bỏ khi bảng quá 100 phần tử *và* rảnh quá 120 giây, nên thực tế nó sống đến lúc tắt
hẳn app. Dùng lại một id đã từng phát ảnh khác là người chơi cũ ghép **ảnh cũ với dữ liệu
frame mới** → hiện ra một khối vụn.

Dải `sendByteImg` cũ cao nhất hết ở 12099, còn `idImg` là `short` nên 13000 vẫn hợp lệ.
Chọn dải mới tinh thì không client nào có sẵn ảnh cũ của id đó → khỏi cần cơ chế đẩy ảnh
ép buộc nào cả.

Ảnh cũng để ở thư mục **riêng** `map/skinthu/`, không chen vào `map/d_effect/`: thư mục đó
chỉ nạp id 0..299 (dải 8700..8999) và cả 300 id đều đã có file.

### 5.4 Skin suy ra từ vật phẩm, không có cột DB riêng

`Char.idSkinLinhThu` không lưu xuống DB. Vật phẩm skin nằm trong `Char.awItems`, mà
`awItems` thì đã được lưu sẵn qua `getAllItemInfoSave`. Mỗi lần đăng nhập / mặc đồ / cưỡi
thú đều gọi `SkinLinhThu.dongBoTheoTrangBi` để tính lại, nên **không bao giờ lệch** giữa
"skin đang hiện" và "vật phẩm đang mặc", và không phải thêm cột hay bảng nào.

### 5.5 Vật phẩm skin phải đi nhánh trang bị thú lúc nạp lại

Cả đồ nhân vật lẫn trang bị thú đều được lưu vào **cùng một cột `equip`**
(`Database.getInfoCharSave` nối `getCharWearing()` rồi nối tiếp `awItems`), nên lúc nạp
lại chỉ còn **type** để phân luồng — `Char.setItemWearing()` dùng
`Map.isWearingAnimal(type)` = 14..18.

Vật phẩm skin mang type 30, không nằm trong dải 14..18, nên nếu không chặn nó rơi vào
nhánh đồ nhân vật — và ở đó `POS_WEARING_CHAR` không có ô 30, `get(30)` trả `null` nên
`getIndexItemWearing` ném `NullPointerException` giữa lúc nạp nhân vật.

(Bản đầu tiên đặt type 19 = type Phi phong: khi đó **không** ném lỗi mà âm thầm **chiếm ô
Phi phong** — `POS_WEARING_CHAR[19] = 11` — đăng nhập lại là mất skin, nhân vật lại mọc
thêm áo choàng, và không gỡ ra được. Xem mục 5.7 về việc đổi sang type 30.) Vì vậy `Char.setItemWearing()` và nhánh nạp legacy đều lọc thêm
`SkinLinhThu.laItemSkin`, và cho skin vào `awItems` **bỏ qua `checkExistAnimal`** — skin
gắn theo nhân vật nên bán con thú đi vẫn phải giữ được.

### 5.6 Thiếu file ảnh thì tắt hẳn skin, không tắt một nửa

Bật skin là **tắt** đường ngựa cổ điển (`userHorse = 0`). Nếu lúc đó dữ liệu frame không
nạp được thì đường thú cưỡi tool cũng không vẽ được gì (client vẫn tạo `DataSkillEff`
nhưng `listFrame` rỗng nên `paintTopHorse`/`paintBottomHorse` thoát ngay) → **người chơi
cưỡi hư không**. Nên `SkinLinhThu.dangMang` kiểm đủ cả hai file
(`SkinLinhThu.coDuAnh`) trước khi bật.

### 5.7 Vật phẩm dùng type 30 — và vì sao không phải 19

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
   cho hai thứ khác nhau chính là gốc của cả loạt lỗi ở mục 5.5.
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

## 6. Thay bộ ảnh khác

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

Thiếu một trong hai tệp thì `DataGame.napSkinThu` **tắt hẳn skin đó** — xem mục 5.6.

Muốn soi hình học trước khi dùng (con thú so với chỗ người ngồi):

```bash
java -cp build/tools-classes XemSkinNhuTrongGame xem.png map/d_effect/56 map/skinthu/0
```

### Kiểm bộ ảnh trước khi dùng

Điều duy nhất phải kiểm bằng tay là **đúng 21 frame** và mọi ô nằm trong biên atlas. Đọc
byte đầu tiên ra số ảnh con `n`, nhảy `1 + n*5` byte rồi đọc `short` — phải ra 21. Nếu bộ
sprite ít hơn 21 frame thì con thú biến mất đúng tick đó, xem mục "Ba cái bẫy" dưới đây.

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

### Chiều cao quyết định người ngồi đúng hay lơ lửng

Người ngồi được nhấc lên bằng `Char.dy_horse_tool[indexdxy]`, là bảng **cố định**. Skin gửi
`indexdxy = 0` (dòng `{-20,-15,-15,-15}`, đúng dòng con Poro dùng). Nên con thú phải cao
xấp xỉ con Poro: đo từ `map/d_effect/56` được xuống 39px, lên 40px, ngang 42px, đáy thò
xuống 7px dưới chân nhân vật. Tool tự thu về đúng ba số đó.

Con nào có phần vươn cao bất thường (Tiên Lộc có bộ gạc rất cao) thì dùng `--cao=140` để
phóng to lên — không thì phần *thân* bị bóp nhỏ và người ngồi lơ lửng trên ngọn gạc.

---

## 7. Các điểm đã nối vào code gốc

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
| `Map.exchangeItem` | Chặn vật phẩm skin, đẩy sang đường trang bị thú |
| `Map.doUseItem` | Coi vật phẩm skin là trang bị thú; đồng bộ + gửi lại sau khi mặc |
| `Map.doChat` | Lệnh `boskin` (mọi người) |
| `Map` đổi thưởng | 5 mục mới |
| `Char.idSkinLinhThu` | Trường skin |
| `Char.getAnimalWearing` | Nhận vật phẩm skin bất kể `idAnimal`, buộc lại vào con đang cưỡi |
| `doRideAnimal` | Đồng bộ skin khi cưỡi thú |
| `SelectCharHandler` | Đồng bộ skin lúc đăng nhập |

## 8. Giới hạn đã biết

- ~~**Icon skin vẽ sai vị trí trong bảng "Trang bị thú"**~~ — **đã sửa, cả hai bản client.**
  Gốc lỗi không phải ở cách vẽ mà ở **số type**: bản JAR đánh số cột phải là 30–34 còn bản
  Unity (port từ jar cũ hơn) đánh 19–23. Đặt type 19 thì bản Unity vẽ đúng, còn bản JAR tra
  `posPaintWearing[19]` = `(34, 92)` — ô **Phi phong của bảng nhân vật** — nên icon rơi
  xuống góc dưới. Đã thống nhất theo bản JAR: server gửi type **30**, bản Unity sửa
  `idWearAnimal` cho khớp. Xem mục 5.7.
  Bản JAR **không phải đóng gói lại**: server gửi `type` của từng template xuống client mỗi
  lần đăng nhập, nên chỉ cần chạy `sql/sua_type_skin_linh_thu.sql` rồi khởi động lại server.
  Bản Unity vẫn giữ `paintIconTrangBiThu` lấy vị trí ô thẳng từ `idWearAnimal`, vì mảng
  `posPaintWearing` bên đó mới dài 20 phần tử, chưa có ô 30–34.
- **Bộ sprite này thấp hơn con thú chuẩn, nên người ngồi hơi cao.** Client vẽ con thú ở
  **chân** nhân vật rồi nhấc riêng nhân vật lên bằng `Char.dy_horse[0]` = `{-20,-15,-15,-15}`
  — một bảng **cố định**, không theo con thú. Nên con thú càng thấp thì chân người càng
  gần đỉnh lưng nó. Đo bằng `tools/XemSkinNhuTrongGame.java` (độ "lụt" của chân người vào
  thân thú, hướng xuống / lên / ngang):

  | | xuống | lên | ngang |
  |---|---|---|---|
  | Poro (`d_effect/56`, mốc chuẩn) | 12 | 18 | 20 |
  | Lân (`d_effect/64`) | 15 | 20 | 17 |
  | 0 Phúc Thú | **6** | 19 | **12** |
  | 1 Gấu Trúc | **9** | 21 | **12** |
  | 2 Tiên Lộc | 12 | 18 | 18 |
  | 3 Xích Dương | 10 | 17 | 14 |
  | 4 Chiến Lang | 11 | 16 | 13 |

  Tiên Lộc khớp mốc chuẩn; Phúc Thú và Gấu Trúc hụt nhiều nhất. **Không chữa được bằng
  cách dời** toạ độ: dời con thú lên cho khớp chỗ ngồi thì chân nó rời mặt đất đúng bấy
  nhiêu. Muốn hết hẳn thì phải **phóng to sprite** về đúng cỡ con Poro (cao 39/40/42),
  đúng như công cụ của bản đầu tiên vẫn làm — đổi lại ảnh bị nội suy.
- **Icon con thú nhỏ cạnh tên nhân vật vẫn là của linh thú thật** (`Animal.idImgIcon`).
  Cố ý: nó cho biết con linh thú thật là con gì, đúng tinh thần "chỉ số vẫn là của nó".
  Tên và chỉ số trong bảng thông tin cũng vậy.
- **Bang chủ đang giữ thành** không hiện skin. Cả hai chỗ ghi `idThutool` đều nằm trong
  `if (animalRide != null && !isMasterClanAndHaveTown())`, nên với người đó id ảnh skin
  không bao giờ được gửi; nếu vẫn tắt đường ngựa cổ điển thì họ **mất hẳn con thú**. Vì
  vậy `SkinLinhThu.dangMang` chặn sẵn trường hợp này, con thú vẽ như bình thường.
- **Thú đang hoá hình + mang skin**: skin thắng (đè lên hình hoá hình). Nhánh hoá hình
  ghi `-1` cho `userHorse`/`fly`/`idImgHorse` rồi bỏ qua, còn `idThutool` phía dưới vẫn
  đổi sang skin — an toàn vì bản thân hoá hình cũng đi đường thú cưỡi tool.
- **Số hiệu ứng trong gói 26 chỉ còn dư 1 chỗ** (254/255). Dữ liệu skin đã được xếp
  **trước** danh sách hiệu ứng nên nếu có ngày tràn thì bị cắt là hiệu ứng chứ không phải
  skin — cắt hiệu ứng chỉ làm hiệu ứng đó không vẽ, còn cắt skin thì người đang mặc skin
  sẽ cưỡi hư không.

## 9. Bẫy đã gỡ (ghi lại để đừng đặt lại)

- **Không gửi `createAnimalWearingMessage` cho người xung quanh.** Gói đó vào
  `GameScr.onAnimalWearingInfo`; khi `charID` khác nhân vật của người nhận thì nó gọi
  `onViewAnimalInfo` → `WindowInfoScr.switchToMe()` + `Canvas.endDlg()` — **đổi hẳn màn
  hình của họ** sang bảng "Trang bị thú" của mình, cắt ngang việc họ đang làm. Đổi skin
  qua lại sẽ thành công cụ phá người khác. Họ cũng không cần gói đó: dữ liệu duy nhất nó
  mang là `imgAnimal`, mà `imgAnimal` chỉ dùng vẽ trong cửa sổ thông tin chứ không dùng
  vẽ trên bản đồ. Hình trên bản đồ đi bằng `createCharInfo`.
  *(Lưu ý: `doRideAnimal.java` vẫn còn một dòng như vậy từ trước — lỗi cũ, chưa đụng tới.)*
- **Không nâng `nIconItem` quá số icon thật sự thêm vào.** Id nào `< nIconItem` mà thiếu
  file thì `allImg[id]` là null, `sendByteImg` ném NPE rồi bỏ qua cả lệnh gửi → client
  hỏi mãi không bao giờ được trả lời. Thêm 5 icon (860..864) thì nâng đúng lên 865.
- **`exchangeItemAnimal` so cả `type` lẫn `idAnimal`** khi gỡ món cũ ra. Với skin (gắn
  theo nhân vật) phải bỏ điều kiện `idAnimal`, không thì món skin cũ đang dính vào con
  thú trước đó sẽ không bị gỡ — người chơi mặc skin mới mà không có gì xảy ra, cũng không
  có thông báo nào.
- **Số hiệu ứng trong gói 26 tối đa 255** (client đọc `readUnsignedByte`). Hiện dùng 254
  (249 cũ + 5 skin). Thêm skin thứ 6 là tràn — code đã có đoạn cắt bớt và in cảnh báo thay
  vì để gói hỏng, nhưng muốn thêm nữa thì phải gỡ bớt id không dùng khỏi
  `MessageCreator.id_eff_tool_ngu_long`.
- **Icon không được đặt lại vào dải 855..859.** Bản skin đầu tiên dùng dải đó, nhưng sau
  khi tính năng bị gỡ, đúng năm id ấy đã được giao cho **Cánh** (commit `2c651ce`) làm ảnh
  ô xem trước cửa sổ Trang bị. Đặt đè lên là ô xem trước của Cánh hiện ra con thú. Bản này
  dùng 860..864 và nâng `nIconItem` lên 865.
