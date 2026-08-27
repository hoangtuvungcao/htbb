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


## Cấu trúc `real/Map.java`

`Map.java` từng là 41.541 dòng. Đã tách còn 22.434 dòng (−45%) bằng **hai kỹ
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

## Tính năng

- Skin linh thú (đổi hình thú cưỡi, giữ nguyên chỉ số): [docs/skin-linh-thu.md](docs/skin-linh-thu.md)

**Lưu ý quan trọng**: mỗi lần push lên nhánh `main` sẽ tự động deploy lên VPS sản
xuất (`.github/workflows/deploy.yml`). Đổi `KPAH2_patched.jar`, `lib/`, `map/`,
`cMap/`, hoặc `loginServer/` sẽ khiến server live tự restart (có cảnh báo 60 giây
trong game trước khi tắt) — xem `vps/deploy-auto.sh`. Commit chỉ đổi `src/` hoặc
tài liệu thì **không** ai bị kick.
