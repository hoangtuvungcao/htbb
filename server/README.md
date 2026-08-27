# KPAH — Khí Phách Anh Hùng (private server)

Private server cho game mobile MMORPG "Khí Phách Anh Hùng". Gồm 3 phần chạy độc lập:

- **Java game server** — `src/`, build bằng `build.bat`. Jar deploy thật là
  `KPAH2_patched.jar` ở gốc repo.
- **Client C#** — `Client/` (bản port từ client J2ME/MIDP gốc).
- **Web PHP** — `web/` (đăng nhập, nạp tiền, PDO + MySQL). Xem `web/README.md`.

## Thư mục

| Thư mục | Nội dung |
|---|---|
| `src/` | Source Java game server |
| `lib/` | Thư viện `.jar` bên thứ ba (nằm trên `Class-Path` của jar deploy) |
| `Client/` | Source client C# |
| `web/` | Website PHP (login, nạp tiền, admin) |
| `sql/` | Script SQL: fix dữ liệu, migrate, dump mốc |
| `vps/` | Script deploy & vận hành trên VPS (systemd service, backup DB, deploy-auto) |
| `map/`, `cMap/` | Tài nguyên bản đồ, sprite |
| `loginServer/` | Server đăng nhập riêng |
| `tools/` | Tool dev rời (cắt sprite, tạo icon, xem sprite...) |
| `test/` | Bộ kiểm thử (không phụ thuộc thư viện ngoài) — chạy bằng `test.bat` |
| `docs/` | Tài liệu (hướng dẫn build/VPS, bảng id thuộc tính...) |
| `build/` | Thư mục tạm khi build — không commit |

## Build

```
build.bat      # biên dịch src/ -> KPAH2_NEW.jar (jar sạch, dựng từ đầu)
capnhat.bat    # thay KPAH2_NEW.jar vào KPAH2_patched.jar (có backup) + chạy lại
```

Sau đó **bắt buộc** `git add KPAH2_patched.jar && git commit && git push` — VPS chỉ
pull file `.jar` có sẵn, nó **không** tự biên dịch `.java`.

Hướng dẫn đầy đủ: [docs/HUONG_DAN_BUILD_VPS.md](docs/HUONG_DAN_BUILD_VPS.md) ·
[docs/HUONG_DAN_VPS_FRP.md](docs/HUONG_DAN_VPS_FRP.md) · [web/README.md](web/README.md)

## Ràng buộc phải biết trước khi sửa cấu trúc

Đây là những thứ **không di chuyển/đổi tên được**, đã kiểm chứng trực tiếp:

| Thứ | Bị khoá bởi |
|---|---|
| `KPAH2_patched.jar` ở gốc | `ExecStart=java -jar KPAH2_patched.jar`, `WorkingDirectory=/opt/kpah` |
| `lib/` (đúng tên này) | `Class-Path: lib/...` ghi trong MANIFEST của jar |
| `map/`, `cMap/` | hard-code ~50 chỗ trong source (`"map/map"`, `"cMap/t.type"`…) |
| `server.ini` + các `.txt` ở gốc | server đọc/ghi bằng đường dẫn tương đối thư mục chạy |
| `loginServer/` | `WorkingDirectory=/opt/kpah/loginServer` |
| **Tên package `real`/`data`/`io`/`server`/`real.cmd`** | xem mục dưới |

### `lib/NQSH_5h.jar` — vì sao không đổi được tên package

`lib/NQSH_5h.jar` chứa **878 class đã biên dịch, không có mã nguồn**, trong đó
133 class package `real`, 73 package `data`, 8 `io`, 8 `real/cmd`, 4 `server` —
**trùng đúng package với `src/`**. Runtime là hợp nhất của hai nguồn: class nào có
trong `KPAH2_patched.jar` thì bản đó thắng, thiếu thì rơi xuống `NQSH_5h.jar`
(33 class tồn tại ở cả hai nơi).

**49 trên 100 class của `src/` bị bytecode trong `NQSH_5h.jar` gọi ngược theo tên.**
Ví dụ `data/BossMatQuy.class` trong jar giữ tham chiếu cứng tới `real/Char`.

Hệ quả: đổi `real.Char` thành `com.kpah.game.Char` sẽ **biên dịch sạch, mọi kiểm
tra đều pass, rồi server ném `NoClassDefFoundError: real/Char` lúc chạy** — đúng
lúc người chơi chạm vào đường code đó. Compiler không thể phát hiện, vì jar là
bytecode đóng.

Muốn đổi tên package thật sự thì phải gỡ phụ thuộc `NQSH_5h.jar` trước
(dịch ngược ~233 class game trong đó về `src/`; 642 class còn lại chỉ là bản sao
thừa của `jxl` và `mysql-connector` đã có sẵn trong `lib/`).


## Cấu trúc ba file lớn

### `real/Map.java` — 41.541 → 17.525 dòng (−58%)

`Map.java` từng là 41.541 dòng. Đã tách còn 17.525 dòng (−58%) bằng **hai kỹ
thuật, cả hai đều không đổi API nên `lib/NQSH_5h.jar` vẫn gọi được**:

1. **Chuyển hẳn method `private`** sang class mới cùng package. An toàn vì
   bytecode ngoài không thể gọi tới `private`.
2. **Uỷ nhiệm** với method `public`/`protected` (jar gọi trực tiếp, không được
   chuyển đi): giữ nguyên method và chữ ký, chỉ chuyển *thân* sang class khác:

   ```java
   protected void doMenu(Char player, Message message) {
       MapDoMenu.doMenu(this, player, message);
   }
   ```

| File | Nội dung |
|---|---|
| `MapDoMenu.java` | thân `Map.doMenu` — chọn mục trong menu NPC |
| `MapChat.java` | thân `Map.doChat` — chat + lệnh admin |
| `MapNhapText.java` | thân `Map.doProcessInputText` — ô nhập liệu |
| `MapDanhNhau.java` | thân `doAttackPlayer` / `doAttackMonster` / `doAttackMultiTarget` |
| `MapDungThuoc.java` | thân `Map.doUsePotion` |
| `MapSuKien.java` | NPC sự kiện theo mùa (Tết, Noel, Trung Thu, Giỗ Tổ, Vu Lan) |
| `MapMenuNpc.java` | menu NPC riêng lẻ (Hằng Nga, Vua Hùng, Dracula…) |
| `MapBangHoi.java` | quản lý bang hội |
| `MapQuaTang.java` | hộp quà, vòng quay, phần thưởng |
| `MapMuaBan.java` | mua bán, giao dịch |
| `MapCheTao.java` | chế tạo / sửa chữa vật phẩm |
| `MapEpNgoc.java` | ép ngọc, khảm ngọc |
| `MapDungThuoc.java` | dùng bình thuốc |
| `MapXaHoi.java` | tổ đội, bạn bè, thách đấu |
| `MapXuLyLenh.java` | `processMessage`, `doChangeMap`, `doDotDen`, `doNapSMS`, xếp hạng |
| `MapNapTaiNguyen.java` | nạp bảng ngọc, NPC, bản đồ, tile map, boss |
| `MapVongLap.java` | `update`, `updateWind`, `updateSand`, lặp gửi thông tin |
| `MapKhoiTao.java` | thân constructor `Map` (476 dòng) |

**Ba cái bẫy khi tách tiếp** (đều đã cắn một lần, javac không bắt được cái 2 và 3):

1. Method `private synchronized` khoá trên chính đối tượng `Map`. Chuyển sang
   `static` là **mất khoá** → phải bọc lại `synchronized (map) { … }`.
2. Biến cục bộ trùng tên `map` che mất tham số mới thêm. Nguy hiểm hơn: nếu
   **trong khối đó** bản gốc có dùng `this`, phép đổi `this`→`map` sẽ trỏ nhầm
   vào biến cục bộ. **Luôn kiểm bản gốc có `this` trong khối trước khi đổi tên.**
3. `import static real.Map.x` **không dùng được cho phép gán** (`x = 5`).
   Dùng tiền tố tường minh `Map.x` — luôn đúng cả khi đọc lẫn ghi.

**Cách kiểm chứng bắt buộc sau mỗi lần tách**: dump `javap -p` trước/sau rồi
diff. Mọi chữ ký biến mất phải là `private`; mất bất kỳ thành viên
`public`/`protected`/package-private nào nghĩa là đã phá API mà jar đang gọi.
Với uỷ nhiệm thì so thêm **từng dòng** thân method với bản gốc (chuẩn hoá
`this`→`map`, bỏ tiền tố `Map.`/`map.` ở cả hai phía) — phải khác 0 dòng.



### Cái giá phải trả: mất tính đóng gói

Uỷ nhiệm không đổi API công khai, nhưng để class mới gọi được thì nhiều thành
viên `private` phải nới thành package-private. Đo trên `real.Map` bằng `javap`:

| | |
|---|---|
| `private` trong bản gốc | 224 |
| chuyển hẳn sang file khác | 120 |
| **vẫn ở `Map` nhưng đã nới quyền** | **63** |
| còn nguyên `private` | 41 |

Nới quyền **không bao giờ phá được gì** (chỉ thêm khả năng truy cập, không bỏ
đi cái gì), nhưng 63 là nhiều — `Map` mất dần tính đóng gói. Đây là cái giá
bắt buộc khi tách một lớp 41k dòng mà **không được** đổi API, vì
`lib/NQSH_5h.jar` gọi thẳng vào nó.

Nếu sau này gỡ được phụ thuộc `NQSH_5h.jar` thì có thể gộp mỗi nhóm method
với dữ liệu của nó thành lớp thực sự độc lập, và trả lại `private`.

### `real/Char.java` — 21.888 → 14.307 dòng (−35%)

Chỉ có 35 method `private` trên tổng 679 method, nên gần như toàn bộ phải
dùng **uỷ nhiệm**.

| File | Nội dung |
|---|---|
| `CharNapDuLieu.java` | `initInfoFromDB`, `initPinfo` — nạp nhân vật từ CSDL |
| `CharChiSo.java` | `attackDam`, `calculateDefend/Attack/MaxHPMP`, `applyThanKhiOnHit` |
| `CharTrangBi.java` | `checkNewEffectItem`, `setItemWearing`, `setitemInvenBag` |
| `CharSuKien.java` | quà tích luỹ, vòng quay, quà offline, 8/3 |
| `CharCapNhat.java` | `subUpdate`, `updateNearChar`, `doPlayerMove`, buff, pet |
| `CharVongLap.java` | `update` (613 dòng), `sendItem`, `getDBInfo`, xổ số, quà Noel |
| `CharKhoiTao.java` | thân constructor `Char` (413 dòng) — `super()` ở lại trong `Char` |

**Ghi chú**: `Char.update` và `startThreadXoso` có lớp ẩn danh nhưng **vẫn tách được** —
lớp ẩn danh chỉ thành vấn đề khi nó **bắt `this` ngầm**; ở đây chúng chỉ dùng biến
cục bộ. Phải mở ra đọc mới biết, không thể kết luận từ việc "có lớp ẩn danh".

### `data/Database.java` — 16.423 → 12.398 dòng (−25%)

Chỉ 6 method `private` trên 335.

| File | Nội dung |
|---|---|
| `DatabaseNhanVat.java` | `getChar`, `saveChar`, `saveCharAuto`, `addChar`, `saveCharLienDau`, `saveCharBackup`, `getCharList` |
| `DatabaseGiftCode.java` | `newDoGetGiftCode`, `doAddNewColumInfoPart`, `loadEvent`, `getCheckQua` |
| `DatabaseXepHang.java` | `getTopLV`, `getTopRich`, `getTopNapLuong`, nạp bảng nhiệm vụ/vật phẩm |

### Hai bẫy nữa (ngoài ba bẫy đã ghi ở trên)

4. Method vốn là **`static`** thì không có `this` — thêm tham số `self` rồi
   sinh lời gọi `X.foo(this)` sẽ hỏng. javac bắt được ngay, nhưng phải nhớ
   rà trước (`loadAllItemLucky` là ca duy nhất trong 14 method).
5. Symbol thiếu có thể là **thành viên instance** chứ không phải `static` —
   khi đó phải thêm tiền tố tham số (`self.`), không phải tên lớp (`Char.`).


### Kỹ thuật này đã gần chạm trần

Đo phần còn tách được (tính đến commit 20):

| File | Tổng dòng | Thân method | Field + khối `static` + import<br>(**không** tách được) | Method ≥80 dòng còn lại |
|---|---|---|---|---|
| `Map.java` | 17.525 | 11.001 | **6.524** | 23 method / 2.457 dòng |
| `Char.java` | 14.307 | 11.535 | 2.772 | 12 method / 1.046 dòng |
| `Database.java` | 12.398 | 11.598 | 800 | 4 method / 587 dòng |

Còn ~4.000 dòng tách được, rải trên ~39 method. Mỗi lần tách giờ phải nới thêm
quyền và sinh thêm file nhỏ, nên **lợi ích trên mỗi lần tách đã thấp hơn cái giá**.

Riêng `Map.java` còn **6.524 dòng khai báo field + khối `static`** — đây mới là
phần lớn nhất còn lại, và nó **không thể** tách: field phải ở nguyên trong `Map`
vì `lib/NQSH_5h.jar` truy cập trực tiếp.

Ba việc còn lại đáng làm hơn là tách tiếp:

1. ~~**Gộp 9 handler trùng lặp**~~ — đã làm, xem mục dưới. Xoá được 2 cái chết,
   7 cái còn lại không gộp được vì cả hai bản đều sống qua hai lối vào khác nhau.

2. **Gỡ phụ thuộc `NQSH_5h.jar`** — đã khảo sát đầy đủ, xem mục dưới. Làm được
   nhưng chưa nên làm trước khi có test phủ các đường đi chính.
3. ~~**Viết test**~~ — đã có `test/` + `test.bat` (62 kiểm tra). Nhưng mới phủ được
   hàm thuần; các đường đi chính vẫn chưa có test.


### Gỡ phụ thuộc `NQSH_5h.jar` — đã khảo sát xong, **chưa nên làm**

Chạy `tools/giai-ma-nqsh.bat` để đọc được mã nguồn trong jar (kết quả ra
`build/nqsh-src/`, không commit). Đây là số liệu đo thật:

| | |
|---|---|
| Tổng class trong jar | 878 |
| `jxl` — **trùng khít 484/484** với `lib/jxl-2.6.jar` | 484 |
| `com/mysql` — trùng 132/158 với connector riêng | 158 |
| **Code game thật** (`real` 141, `data` 73, `io` 8, `server` 4, …) | **236** |
| File `.java` cần thêm vào `src/` (đã trừ 15 class `src/` đã có) | **175** |
| — dịch ngược **sạch, biên dịch được ngay** | **163** |
| — **phải viết lại luồng điều khiển bằng tay** | **12** |

12 file hỏng: `BossThuThanh`, `ArenaData`, `ArenaMap`, `BossWedding`,
`DungeonMap`, `MapTongKim`, `PrivateDataMap`, `PrivateMap`, `RegionLoiDai`,
`RegionMapMoba`, `RegionNuiChauBau`, `TongKimData`. CFR sinh ra `** GOTO lbl183`
và `while (true) lbl-1000:` — chỗ luồng nhảy trong bytecode **không diễn đạt
được bằng Java**, phải đọc `javap -c` rồi dựng lại vòng lặp bằng tay.

**Vì sao chưa nên làm ngay** — khác hẳn mọi việc đã làm từ đầu đến giờ:

Toàn bộ refactor trước đây **chứng minh được là không đổi hành vi**: thân method
giống bản gốc từng dòng, `javap` xác nhận API không đổi. Việc này thì ngược lại —
nó **thay 236 class bytecode đang chạy tốt bằng code dịch ngược rồi biên dịch
lại**. CFR *thường* đúng, nhưng "thường" trên 236 class là rủi ro thật, và 12 file
kia thì luồng điều khiển do người viết lại chứ không phải máy sinh.

Cộng thêm: bộ test hiện chỉ phủ được hàm thuần, **không phủ một dòng nào** trong
236 class đó. Nên nếu có sai, không có gì bắt được trước khi người chơi gặp.

**Đề xuất thứ tự**: viết test phủ các đường đi chính trước → rồi mới đổi, làm
từng nhóm nhỏ, mỗi nhóm build + chạy thử trên máy riêng trước khi đẩy lên server.

Cái được nếu làm xong: đổi được tên package sang `com.kpah.*`, trả lại `private`
cho 63 thành viên đã nới, và bỏ được mối nguy trộn hai phiên bản MySQL connector
(26 class `com/mysql` chỉ có trong NQSH là inner class của bản cũ hơn — ở đó
`Connection` là *class*, còn 5.1.49 là *interface*).


### 9 handler trùng lặp — đã xử lý 2, còn 7 **không gộp được**

`RealController.processMessage` có một `switch` nằm trong
`if (privateMessage(command))`. Bảng `privateCmd` nạp **một lần duy nhất** trong
khối `static` từ mảng `pCmd` (96 lệnh) — đã grep toàn bộ `src/`: chỉ ghi ở một
chỗ, không nơi nào sửa lúc chạy. Nên tập lệnh là **cố định**, đối chiếu được.

**Đã xoá** (chứng minh được là chết, và `NQSH_5h.jar` không tham chiếu):

| Thứ | Vì sao chết |
|---|---|
| `real/doTrade.java` + `case 66` | lệnh 66 **không** có trong `pCmd` → case không bao giờ chạy |
| `case -76` + `RealController.doBuyItemShopNew` (153 dòng) | lệnh −76 **không** có trong `pCmd` |
| `real/sendInfoNpc.java` | 0 lời gọi trong toàn bộ `src/` |

**Không gộp được — cả hai bản đều sống, qua hai lối vào khác nhau:**

| Handler | Lối vào 1 (class tách rời) | Lối vào 2 (bản trong `Map`) |
|---|---|---|
| `doRideAnimal` | lệnh −45 | menu NPC → `MapDoMenu` |
| `doUnRideHorse` | menu `case -50` | menu NPC → `MapDoMenu` |
| `doMenuSuKienHe2017` | menu `case -49` | menu NPC → `MapDoMenu` |
| `doMenuSuKienMiniChucNu` | menu `case -49` | menu NPC → `MapDoMenu` |
| `domenuWorldcup2017` | menu `case -49` | menu NPC → `MapDoMenu` |

Hai bản **đã trôi lệch** (bản trong `Map` được dọn tay, bản kia còn thô từ trình
dịch ngược). Gộp nghĩa là chọn một bản làm chuẩn cho **cả hai** lối vào — đó là
quyết định về *hành vi game*, không phải phép biến đổi máy móc. Ai biết luật chơi
mới quyết được, nên tôi để nguyên.

Riêng `doBuySpecialItem` (lệnh 85) và `doUsePotion` (lệnh 22): bản trong `Map`
chỉ tới được qua `Map.processMessage`, mà hai lệnh này **có** trong `pCmd` nên
không bao giờ tới đó → đường đó chết. Nhưng `Map.doUsePotion` là `protected` và
`NQSH_5h.jar` có class nhắc tới `doUsePotion`, nên **không xoá** — chỉ ghi lại.

## Tính năng

- Skin linh thú (đổi hình thú cưỡi, giữ nguyên chỉ số): [docs/skin-linh-thu.md](docs/skin-linh-thu.md)

**Lưu ý quan trọng**: mỗi lần push lên nhánh `main` sẽ tự động deploy lên VPS sản
xuất (`.github/workflows/deploy.yml`). Đổi `KPAH2_patched.jar`, `lib/`, `map/`,
`cMap/`, hoặc `loginServer/` sẽ khiến server live tự restart (có cảnh báo 60 giây
trong game trước khi tắt) — xem `vps/deploy-auto.sh`. Commit chỉ đổi `src/` hoặc
tài liệu thì **không** ai bị kick.
