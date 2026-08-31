# KPAH — Khí Phách Anh Hùng (private server)

Private server cho game mobile MMORPG "Khí Phách Anh Hùng". Gồm 3 phần chạy độc lập:

- **Java game server** — `src/`, build bằng `build.bat`. Jar deploy thật là
  `KPAH2_patched.jar` ở gốc repo.
- **Client C#** — `Client/` (bản port từ client J2ME/MIDP gốc).
  ⚠️ **Đây là mã DỊCH NGƯỢC, không build được** — xem mục "Thư mục" bên dưới.
- **Web PHP** — `web/` (đăng nhập, nạp tiền, PDO + MySQL). Xem `web/README.md`.

## Thư mục

| Thư mục | Nội dung |
|---|---|
| `src/` | Source Java game server |
| `lib/` | Thư viện `.jar` bên thứ ba (nằm trên `Class-Path` của jar deploy) |
| `ClientUnity/` | Client Unity (C#) — **build được**, ra Windows/WebGL/Android/iOS (xem dưới) |
| `web/` | Website PHP (login, nạp tiền, admin) |
| `sql/` | Script SQL: fix dữ liệu, migrate, dump mốc |
| `vps/` | Script deploy & vận hành trên VPS (systemd service, backup DB, deploy-auto) |
| `map/`, `cMap/` | Tài nguyên bản đồ, sprite |
| `loginServer/` | Server đăng nhập riêng |
| `tools/` | Tool dev rời (cắt sprite, tạo icon, xem sprite...) |
| `test/` | Bộ kiểm thử (không phụ thuộc thư viện ngoài) — chạy bằng `test.bat` |
| `docs/` | Tài liệu (hướng dẫn build/VPS, bảng id thuộc tính...) |
| `build/` | Thư mục tạm khi build — không commit |

## `ClientUnity/` — client dựng lại được, chạy đa nền tảng

> **Đính chính.** Bản README trước ghi rằng `Client/` "không phải dự án Unity" và
> "không build được". Điều đó **sai**. Kết luận ấy dựa trên việc thiếu `Assets/` và
> `ProjectSettings/` — nhưng đó chỉ là phần khung Unity tự sinh, không phải bằng chứng.
> Mã bên trong **là mã Unity**: 33 file `using UnityEngine`, và
> `Properties/AssemblyInfo.cs.meta` chứa `MonoImporter` — dấu vết chỉ Unity mới tạo ra.
> Toàn bộ 269 file biên dịch sạch với **đúng một lỗi** cần sửa (đã sửa, xem dưới).
> Thư mục nay đã đổi thành `ClientUnity/Assets/Scripts/Game/`.

Đây là bản port của client J2ME sang Unity theo kiểu "Unity làm khung vẽ": **một**
`MonoBehaviour` duy nhất (`Main`), `OnGUI()` gọi `canvas.paint(g)`, `FixedUpdate()` chạy
vòng lặp game. Phần còn lại là mã J2ME đã dịch sang C#. Phụ thuộc Unity rất nông — chỉ
khoảng 15 kiểu (`Texture2D`, `Resources`, `Input`, `SystemInfo`...) — nên nó dựng được
cho mọi nền tảng Unity hỗ trợ.

### Cách build

```
xay-client.bat taonguon C:\duong\dan\KPAH_276-1   # tạo tài nguyên từ bản J2ME
xay-client.bat chuanbi                            # tạo cảnh + cấu hình
xay-client.bat windows                            # -> ClientUnity\Build\Windows\
xay-client.bat webgl                              # -> ClientUnity\Build\WebGL\
```

Lần đầu phải chạy đủ ba bước theo thứ tự. `taonguon` và `chuanbi` chỉ cần chạy lại khi
đổi tài nguyên hoặc đổi cấu hình.

### Nền tảng nào build được

| Nền tảng | Trạng thái | Cần thêm gì |
|---|---|---|
| **Windows** (`.exe`) | ✅ build được ngay | — |
| **WebGL** (trình duyệt) | ✅ build được ngay | — |
| **Android** (`.apk`) | ⚠️ cần cài module | Unity Hub → Installs → ⚙ → Add modules → *Android Build Support* (~2–3 GB) |
| **iOS** (`.ipa`) | ⚠️ cần module **và máy Mac** | Module *iOS Build Support*; Unity chỉ xuất dự án Xcode, khâu ký và đóng gói bắt buộc chạy trên macOS |
| **J2ME** (`.jar`) | ❌ **không bao giờ** | Unity không xuất được J2ME. Bản `.jar` là sản phẩm riêng, giữ nguyên như hiện tại |

**WebGL đáng chú ý**: nó chạy trong trình duyệt trên iPhone, Android, Windows, Mac —
tức là một bản build phủ gần hết nhu cầu mà không cần module nào thêm, không cần Mac,
không cần lên chợ ứng dụng.

### Đã phải sửa gì để build được

**1. Shader — lỗi biên dịch duy nhất trên cả 269 file.**
`MyGraphics.CreateLineMaterial()` tạo `Material` từ **chuỗi mã shader**; Unity đã bỏ hẳn
cách này. Thay bằng shader dựng sẵn `Hidden/Internal-Colored` và đặt lại đúng bốn trạng
thái của shader cũ (`Blend SrcAlpha OneMinusSrcAlpha`, `ZWrite Off`, `Cull Off`).

**2. Hàm native của iOS — chặn bằng biên dịch có điều kiện.**
`iOSPlugins.cs` khai báo sáu hàm `[DllImport("__Internal")]` (`_SMSsend`, `_purchaseItem`,
`_back`, `_Send`, `_unpause`, `_checkRotation`). IL2CPP sinh tham chiếu tới chúng **dù lúc
chạy không hề gọi**, nên WebGL hỏng ngay ở khâu liên kết (`undefined symbol: _SMSsend`).
Bọc trong `#if UNITY_IOS && !UNITY_EDITOR`.

Việc này vá luôn một lỗi tiềm ẩn: điều kiện `Application.platform != 0` trong file đó vẫn
**đúng** trên Windows (0 là `OSXEditor`), nên bản Windows sẽ ném `DllNotFoundException`
giữa lúc chơi nếu có đoạn nào gọi tới.

`Net.cs` còn dùng `WWW` (đã lỗi thời nhưng vẫn chạy) — để nguyên cho đỡ rủi ro.

### Tài nguyên game: 93/108, còn thiếu 15

`tools/ChuyenTaiNguyen.java` dựng `Assets/Resources/res/` từ client J2ME. Nó xử lý ba
khác biệt về đóng gói:

- Unity chỉ nhận file làm `TextAsset` khi đuôi là `.bytes`, và tra cứu bằng đường dẫn
  **không có đuôi** → `res/logo` phải là `Assets/Resources/res/logo.bytes`.
- Nhiều ảnh Unity nạp lẻ (ví dụ `res/bar`) lại nằm **bên trong** gói của bản J2ME
  (định dạng `FilePack`, XOR với khoá `NguyenVanMinh`) → công cụ giải gói để lấy ra.
- Bản Unity nạp vài gói (`other`, `items`) mà bản J2ME không có → công cụ **tự dựng**
  gói thay thế, xem phần dưới.

Bản Unity dùng bộ tài nguyên **mới hơn** bản J2ME 2.7.6, nên 15 mục vẫn thiếu hẳn —
chủ yếu `c/hat/`, `c/coat/`, `body`/`head`/`leg`/`hat`.

> **Khoá `NguyenVanMinh`** là khoá XOR 13 byte của định dạng `FilePack` — tên người viết
> công cụ đóng gói gốc. Trong mã C# nó bị giấu thành mảng số (`78, 103, 117, ...`) nên
> `grep` không thấy. Nó lại **hiện nguyên chữ** bên trong các tệp `.sh`: chỗ nào dữ liệu
> gốc có 13 byte 0 liên tiếp thì `0 XOR khoá = khoá`. Đây là che mắt, không phải mã hoá —
> XOR lặp 13 byte thì đảo ngược trong vài dòng.

⚠️ Công cụ **không** dò tài nguyên theo tên cuối trên khắp cây thư mục. Bản J2ME có cả
`c/body/42` lẫn `c/leg/42`, nên `newEf/42` sẽ nhận nhầm ảnh thân nhân vật — **sai âm thầm**,
tệ hơn là thiếu. Chỉ chấp nhận file ở gốc hoặc ảnh trong gói.

#### Kết quả chạy thử thật (bản Windows)

Chạy `KhiPhachAnhHung.exe -batchmode -nographics -logfile <tệp>` rồi đọc nhật ký:

| | Ngoại lệ | Dừng ở |
|---|---|---|
| Lúc đầu | **1475** | `Res.loadRes()` — thiếu gói `other` |
| Sau khi dựng gói thay thế | 1482 | `Res.GetWPSplashInfo()` |
| Sau khi sửa sót đuôi `.wp` | 1 | `GameScr.setDataForButton()` |
| Sau khi đặt ảnh giữ chỗ | 1 | `Tilemap.loadMap()` tràn ngăn xếp |
| **Hiện tại** | **0** | — chạy hết chuỗi khởi động |

Nhật ký hiện tại đi hết: `Init Scale GUI` → `Ban: -- khi phach anh hung` →
`init Session_ME` (lớp mạng sẵn sàng).

Bốn thứ đã sửa để tới được đây:

1. **Sót đuôi tệp** — `tools/ChuyenTaiNguyen.java` chỉ cắt `.png .sh .type .txt`,
   bỏ sót `.wp` và `.wps` (10 tệp). `wpsplash/0.wp` bị ghi thành `wpsplash/0.wp.bytes`
   trong khi mã tìm `wpsplash/0`. Tài nguyên **có thật mà coi như mất**.

2. **Dựng gói thay thế** cho `other` và `items`. Công cụ đọc từ mã nguồn xem gói nào
   được nạp và tên ảnh nào được lấy ra, rồi đóng gói đúng định dạng `FilePack`. Ảnh
   thật nếu tìm được, không thì ảnh trong suốt 1×1. Dựng xong **giải lại ngay và đối
   chiếu** — lệch một byte ở phần đầu là client đọc ra rác mà không báo lỗi gì.

3. **Ảnh giữ chỗ** cho 45 ảnh nạp lẻ còn thiếu. Nhiều chỗ dùng thẳng kết quả không
   kiểm null (`Res.imgNewBt[4].getWidth()`). Nhưng có **49 chỗ rẽ nhánh theo
   `ảnh == null`** — `imgHorse == null` nghĩa là "không có ngựa thì khỏi vẽ". Nên quy
   tắc: tên trường nào có xuất hiện trong phép so `== null` ở bất cứ đâu thì **không**
   đặt ảnh giữ chỗ (bỏ qua 7 ảnh). Thừa còn hơn thiếu: bỏ sót một ảnh chỉ là thiếu ảnh,
   đặt nhầm vào chỗ có nhánh null là đổi logic.

4. **`Tilemap.loadMap` tự gọi mình mãi** — lỗi thật trong client, không liên quan tài
   nguyên. Khối `catch` gọi `loadMap(0, null)` **vô điều kiện**, kể cả khi `level` đã
   là 0 → đệ quy vô hạn → `StackOverflowException`. Nay chỉ lùi về bản đồ 0 khi đang ở
   bản đồ khác.

#### ⚠️ Vẫn chưa phải bản hoàn chỉnh

Client **khởi động sạch**, nhưng 45 ảnh là **trong suốt 1×1**, không phải hình thật.
Giao diện sẽ thiếu nhiều nút và khung. Còn 5 ảnh thiếu hẳn (`b3`, `bar1`, `inv3`,
`skill_da`, `skill_ice`) — đều là ảnh có nhánh rẽ theo null nên cố ý để nguyên.

Muốn đầy đủ thì vẫn phải xin `Assets/Resources/res/` của **dự án Unity gốc**. Không có
đường vòng — ảnh giữ chỗ chỉ để chạy thử, không thay thế được tài nguyên thật.


### Liên hệ với việc cấm theo thiết bị

Client này **đã có sẵn** `Canvas.IMEI = SystemInfo.deviceUniqueIdentifier`
([Canvas.cs:157](ClientUnity/Assets/Scripts/Game/Canvas.cs#L157)) — chạy được trên
Windows, Android, iOS. Dòng gửi mã máy lúc đăng nhập đã có ở
[GameService.cs:402](ClientUnity/Assets/Scripts/Game/GameService.cs#L402), khớp đúng thứ
tự trường mà `real/cmd/LoginHandler.java` đọc.

Nghĩa là: **ngày người chơi chuyển sang bản Unity này, tính năng cấm theo thiết bị tự
hoạt động** mà không phải sửa thêm gì ở server. Bảng `tob_cam_may` trống thì mọi thứ chạy
bình thường như hiện nay.

## Build

```
build.bat      # biên dịch src/ -> KPAH2_NEW.jar (jar sạch, dựng từ đầu)
capnhat.bat    # thay KPAH2_NEW.jar vào KPAH2_patched.jar (có backup) + chạy lại
test.bat       # chạy bộ kiểm thử (mã thoát 0 = đạt) — không đụng jar deploy
```

Sau đó **bắt buộc** `git add KPAH2_patched.jar && git commit && git push` — VPS chỉ
pull file `.jar` có sẵn, nó **không** tự biên dịch `.java`.

Hướng dẫn đầy đủ: [docs/HUONG_DAN_BUILD_VPS.md](docs/HUONG_DAN_BUILD_VPS.md) ·
[docs/HUONG_DAN_VPS_FRP.md](docs/HUONG_DAN_VPS_FRP.md) · [web/README.md](web/README.md)

Chỉnh cấu hình đang chạy (tỉ lệ rơi, giờ chiếm thành, quyền admin, chống flood):
[docs/cau-hinh-server-ini.md](docs/cau-hinh-server-ini.md)

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
- **Sự kiện**: mọi sự kiện bật/tắt được bất kỳ lúc nào — hoặc **hẹn giờ mở/đóng** — ở trang
  quản trị `/admin/sukien.php` ([web/admin/sukien.php](web/admin/sukien.php), bảng `tob_su_kien`).
  Trước đây mốc thời gian của từng sự kiện ghi cứng trong `Char.java` (toàn 2015–2024, nên đều
  tắt vĩnh viễn); nay danh mục nằm ở [src/real/SuKien.java](src/real/SuKien.java).
  Đổi xong **server tự khởi động lại**: gom các thay đổi trong 2 phút, báo trong game 60 giây,
  rồi lưu dữ liệu và thoát cho systemd bật lại — xem [src/real/LichSuKien.java](src/real/LichSuKien.java)
  và [docs/cau-hinh-server-ini.md](docs/cau-hinh-server-ini.md) mục 4.
- Sự kiện World Cup (bình chọn Messi/Ronaldo): [src/real/SuKienWorldCup.java](src/real/SuKienWorldCup.java) ·
  chạy [sql/them_su_kien_world_cup.sql](sql/them_su_kien_world_cup.sql) một lần trước khi mở
- **Hóa hình Pet** (port từ một bản KPAH khác — xem `AGENT_MEMORY`/lịch sử trò chuyện): NPC dịch
  vụ chung (npcType `-8`) có thêm mục "Hoá Hình Pet" — pet phải vĩnh viễn + đã siêu tiến hoá
  (`itemPet.level>=2`), tốn `cấp_mới×10` lọ thuốc hoá hình (item 268, **dùng lại** item của tính
  năng hóa hình Thú có sẵn, không tạo item mới) + 5000 lượng, lên tối đa cấp 5, mỗi cấp cộng dồn
  "kháng xuyên giáp" (giảm 5–25% sát thương nhận, tỷ lệ 10–30%, hồi chiêu 60–100s — cooldown riêng,
  KHÔNG dùng chung `timeBienHinh` với nội tại theo loài, tránh xung đột). Hết hạn sau 10 ngày nếu
  không hóa hình tiếp — dọn dữ liệu lúc đăng nhập lại (`Database.getCharPet`), không real-time.
  Xem [src/data/Pet.java](src/data/Pet.java) (`applyKhangXuyenGiap`), [src/real/RealController.java](src/real/RealController.java)
  (case 13 của `doCreateItem`), test [test/KiemHoaHinhPet.java](test/KiemHoaHinhPet.java).
  Có đổi hình khi hoá hình: `getIdEffPet()` trả id 49–53 theo loài (49=Rồng, 50=Cú, 51=Yêu
  tinh, 52=Đọi, 53=Đại bàng), gửi qua `MessageCreator.java` làm trường hiển thị chính cho pet
  đã tiến hoá (`TYPE_PET_TOOL`) trên session `version180`.
  **Sự cố đã xảy ra và đã sửa (31/08/2026, hai lượt)**: lượt đầu bật tính năng lên thì pet
  **biến mất khỏi màn hình** hoàn toàn sau khi hoá hình — người dùng báo lại. Đoán ban đầu là id
  49–53 "chưa được client đăng ký" nên đã gỡ bỏ hẳn nhánh đổi hiệu ứng. Nhưng người dùng chỉ ra
  đúng: **chưa xem kỹ project tham khảo**. Đối chiếu lại mới thấy `map/d_effect/49..53` trên repo
  này vẫn là ảnh icon nhỏ gốc (30×13, không liên quan pet) — bản KPAH tham khảo đã **thay ảnh
  mới** thành sprite hoá hình thật (kèm `*.bak_orig` lưu ảnh cũ, đối chiếu md5 khớp 100% với ảnh
  cũ ở đây, xác nhận repo này chưa từng được cập nhật ảnh). Đã chép 10 file (`49`, `49.png`, ...,
  `53`, `53.png`) từ project tham khảo sang `map/d_effect/`, xem lại bằng mắt (đúng là sprite
  sinh vật — 49 rồng, 53 chim), rồi bật lại nhánh trong `getIdEffPet()`. Vì `map/d_effect/` được
  gửi qua mạng lúc đăng nhập (`DataGame.java` quét cả thư mục lúc khởi động server), **chỉ cần
  deploy server, không cần sửa gì phía client**. Có test chặn tái diễn — `KiemHoaHinhPet.chay()`
  kiểm tra CẢ code (nhánh hoá hình còn tồn tại) LẪN file ảnh trên đĩa (đúng là bản mới, không
  phải icon cũ) — thiếu một trong hai là tái diễn y hệt sự cố.
- **Hóa hình Thú lên 5 cấp** (nâng cấp tính năng hóa hình Thú có sẵn, cho khớp hệ thống Pet ở
  trên): cấp 1 (kích hoạt lần đầu, NPC "Luyện thú" idNpc `-6`) **giữ nguyên y hệt** hành vi cũ —
  30 lọ thuốc hoá hình (item 268), tỷ lệ 70%, có thể mất nguyên liệu mà thất bại. Cấp 2–5 (nâng
  cấp thêm, cùng nút "Hoá Hình") **chắc chắn thành công** nếu đủ 30 lọ thuốc — khớp cách Pet đang
  làm. Mỗi cấp cộng dồn "kháng xuyên giáp" dùng đúng bảng tỷ lệ của Pet, tồn tại **song song** với
  hiệu ứng tấn công "Lửa thượng cổ" cũ (giảm giáp đối phương) — không đụng vào hiệu ứng đó. Lưu
  vào chuỗi cột `att` có sẵn (giống cách `KHANG_DONG_BANG`/... từng được thêm trước đây), **không
  ALTER TABLE**. Xem [src/data/Animal.java](src/data/Animal.java) (`doHoaHinh`,
  `applyKhangXuyenGiap`), test [test/KiemHoaHinhThu.java](test/KiemHoaHinhThu.java).
- **Tinh Luyện Thần Binh — xem vũ khí trước khi chọn nguyên liệu**: bấm mục menu NPC (`-8`)
  trước đây nhảy THẲNG vào ô chọn nguyên liệu, không cho xem chỉ số/% tinh luyện hiện tại của vũ
  khí đang mặc. Nay thêm một bước trung gian: menu gửi một "shop" chỉ chứa đúng vũ khí thần binh
  đang mặc (type 7) để xem, bấm "Tiếp tục" mới mở danh sách nguyên liệu thật — xem
  [src/real/RealController.java](src/real/RealController.java) (case 8) và
  [src/real/Map.java](src/real/Map.java) (`doBuyItemShopNew`, nhánh `type == 7`).
  Nhân tiện phát hiện và sửa một chỗ **lệch dữ liệu có sẵn từ trước**: nhánh `type == 6` (dựng hộp
  thoại xác nhận "Bạn có muốn dùng X để tinh luyện Y") tự lọc nguyên liệu riêng bằng một mảng
  `validMaterialIDs` cứng, KHÔNG có trường hợp mốc 99% (`locNguyenLieuTinhLuyen()` đòi nguyên liệu
  phải CAO HƠN trang bị ở mốc này) và hiện giá SAI (500 lượng, không trừ xu) so với lúc THỰC THI
  thật (`idpopup` 124: 1 lượng + 3.000.000 xu) — khiến chỉ số nguyên liệu người chơi gửi về có thể
  trỏ sang MÓN KHÁC trong danh sách dùng để thực thi. Đã đổi sang dùng chung
  `locNguyenLieuTinhLuyen()` và đồng bộ giá tiền. Test:
  [test/KiemTinhLuyenThanBinh.java](test/KiemTinhLuyenThanBinh.java).
- **Xu rơi quá ít khi đánh quái level cao — lỗi tràn số**: `Potion.quantity` là kiểu `short` (giới
  hạn giao thức: `Char`/`Map`/`MapClan` gửi nó về client bằng `dos.writeShort(...)`, tối đa
  32767), nhưng `Monster.java` tính số xu rơi ra từ `rcvgold` của quái (có con tới hàng trăm
  nghìn/hàng triệu trong `data_monster`, ví dụ Dracula 10.000.000) rồi ép thẳng `(short)` KHÔNG
  kẹp biên — quái càng mạnh, số xu tính ra càng dễ VƯỢT 32767 và **tràn thành số nhỏ, có khi ÂM**
  (vd 40000 tràn thành −25536, nghĩa là nhặt xu lại bị TRỪ xu; phần thưởng đặc biệt 1.000.000 của
  quái ma `idGhost` tràn còn 16960). Đây là gốc thật của cảm giác "quái mạnh mà xu rơi ít" — không
  phải do tỷ lệ rơi (`Monster.PC_TILE_XU`, xem mục cấu hình admin bên dưới). Sửa bằng cách kẹp số
  lượng về `[0, Short.MAX_VALUE]` ngay trong constructor `Potion` — xem
  [src/real/Potion.java](src/real/Potion.java), test
  [test/KiemTranXuRoi.java](test/KiemTranXuRoi.java). Do giới hạn giao thức là CỨNG (client chỉ
  đọc 2 byte), quái cực mạnh vẫn chỉ nhận tối đa 32767 xu MỖI MÓN rơi ra — nếu muốn phần thưởng
  cao hơn nữa cho boss, cách làm sẵn có trong chính `Monster.java` (nhánh quái ma) là RẢI NHIỀU
  món 32767 thay vì một món khổng lồ, chưa áp dụng cho quái thường vì đổi cách rơi (nhiều vật
  phẩm trên đất thay vì một) là thay đổi lớn hơn, chưa rõ có phải điều muốn hay không.
  Tỷ lệ rơi xu (CHANCE quái có rơi xu không, khác với SỐ LƯỢNG rơi bao nhiêu — mục trên) đã là
  công tắc admin có sẵn: `Monster.PC_TILE_XU` (mặc định 10, tức 10% tỷ lệ gốc — CHỈ áp dụng cho
  quái thường lúc train, không ảnh hưởng boss/quest/tiêu), chỉnh trực tiếp trong game (menu admin
  → `xuLyDrop`/`xuLyLenh` lệnh `5`) hoặc `server.ini` khoá `sv.tileXu`, lưu lại ngay không cần
  build lại — xem [src/real/AdminMenu.java](src/real/AdminMenu.java).
- **Dọn rác trong log `game.out`**: `vps/kpah-logrotate.conf` đã lo xoay vòng theo dung lượng/thời
  gian (30 ngày, tối đa 20MB/file) — vấn đề là NỘI DUNG, không phải dung lượng. Hai nhóm gây rác
  nhiều nhất: (1) [src/io/Session.java](src/io/Session.java) in đầy đủ stack trace mỗi khi gửi dữ
  liệu cho client thất bại — mà nguyên nhân phổ biến nhất là "socket đã đóng" (client vừa ngắt kết
  nối), xảy ra ở MỌI lần logout/đổi map/rớt mạng, tức RẤT thường xuyên trong một game online; nay
  chỉ in 1 dòng gọn thay vì cả stack trace, không đổi logic xử lý kết nối. (2) Ba vòng lặp cập
  nhật bản đồ chạy mãi cho MỖI map (`real/Map.java` — `updateWind`/`update` — và
  `real/MapVongLap.java` — `updateSand`) in TRÙNG (cả `printStackTrace()` lẫn một dòng
  `println` cho CÙNG một lỗi) và không có gì chặn lặp lại — nếu lỗi thật sự xảy ra sẽ lặp vô hạn
  mỗi 300ms cho tới khi restart server; nay chỉ báo lỗi **1 lần cho cả server** (giữ đầy đủ stack
  trace ở lần báo đó để còn debug được), giống đúng cách `data.GhiSoOnline.baoLoiMotLan()` đã làm
  sẵn cho một vòng lặp khác. Test: [test/KiemDonLog.java](test/KiemDonLog.java). Chưa động tới
  hơn 50 chỗ `e.printStackTrace()` rải rác trong `Char.java` (đường xử lý hành động người chơi) —
  phạm vi quá rộng để sửa an toàn trong một lượt, để lại cho lần sau nếu cần.

**Lưu ý quan trọng**: mỗi lần push lên nhánh `main` sẽ tự động deploy lên VPS sản
xuất (`.github/workflows/deploy.yml`). Đổi `KPAH2_patched.jar`, `lib/`, `map/`,
`cMap/`, hoặc `loginServer/` sẽ khiến server live tự restart (có cảnh báo 60 giây
trong game trước khi tắt) — xem `vps/deploy-auto.sh`. Commit chỉ đổi `src/` hoặc
tài liệu thì **không** ai bị kick.
