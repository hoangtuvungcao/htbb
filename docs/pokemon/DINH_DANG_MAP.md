# Định dạng bản đồ HTBB và bản đồ vẽ lại (chế độ Pokémon)

Công cụ liên quan:

- `tools/pokemon/GiaiMaMap.java`: giải mã mọi bản đồ gốc ra JSON và ảnh, ở `docs/pokemon/map_cu/`.
- `tools/pokemon/DungBoTileMoi.java`: dựng bộ tile mới CC0.
- `tools/pokemon/VeMapMoi.java`: vẽ lại toàn bộ bản đồ.
- `src/com/htbb/pokemon/DinhDangBanDo.java`: bộ đọc/ghi dùng chung. Đọc rồi ghi lại cho ra đúng từng byte.
- `src/com/htbb/pokemon/BanDoPokemon.java`: chọn đường tệp theo cờ chế độ.

## 1. Tệp và ai đọc

| Tệp | Nội dung | Server | Unity | J2ME |
|---|---|---|---|---|
| `cMap/<id>` | Bố cục: tile, tileTop, cây, cổng, NPC client | `Map.java` (đọc vào danh sách gửi client), `MapKhoiTao/MapLienDau/MapClan/OfflineMap/MapChienTruongMoba/MapChauBau/MaterialMap/MapLoiDai` (lấy tile + kiểu), `MapNapTaiNguyen.loadTileMap` (cổng), `RealController.add_more_idMapSend2Client` (map 28) | `Tilemap.loadMap(level, byteMap)` | `game/model/Tilemap.loadMap` |
| `cMap/{t,t_hang,t_thanh}.type` | 1 byte cho mỗi tile: 0 = đi được, 2 = chặn (client và server chỉ xét bit `& 2`) | các `Map*.java` ở trên | `Res.loadTileImage` (`res/t*`) | `Res.loadTileImage` (`/t*.type`) |
| `map/map<N>` | Lưới sinh quái | `MapNapTaiNguyen.loadMap` (các `loadMap` ghi đè đều nhận tên tệp từ nơi gọi) | — | — |
| `map/tree/<i>` | Vật trang trí lệnh -48 | `DataGame` (53 tệp) | `TreeInfo.onImage` | có |
| Ảnh tile | Dải ảnh 16 × 16n | gửi `t_hang`, `t_thanh` trong gói đăng nhập (`DataGame.duongTileGuiClient`, độ dài ghi bằng short nên ảnh ≤ 32767 byte) | `Resources/poke_tile/*` (chế độ Pokémon) | `res/pt/*` (chế độ Pokémon) |

**Bộ tile đi theo id nạp** (`getMapLoad`): id < 110 dùng `t`, 110–200 dùng `t_hang`, id > 200 dùng `t_thanh`. Quy tắc này nằm trong cả ba nơi đọc.

**Client lấy bố cục từ đâu.** Gói 12 (`MessageCreator.createMapMessage`) gửi `short mapID, short x, short y, byte idXaphu, short mapLoad, UTF tên, bool có, [toàn bộ byte cMap]`. Byte cMap lấy từ `Map.idMapSend2Client`, gồm mọi map trong danh sách ở `Map.java` (khối static) cộng map 28. Hai client đều dùng byte server gửi khi có. Khi không có thì đọc bản đóng gói sẵn (`res/<id>`).

- Game gốc không gửi map 0 và map 122.
- Lúc khởi động và khi gặp lỗi, client gọi `Tilemap.loadMap(0, null)`, tức đọc bản đóng gói sẵn. Bản J2ME chỉ đóng gói sẵn `res/0`.

Phiên bản: không có kiểm tra phiên bản bản đồ. Byte server gửi luôn thắng.

## 2. `cMap/<id>` (từng byte)

```
u8 w, u8 h                                   ≤ 255 (đơn vị ô 16 px)
w*h × u8 tile                                chỉ số trong bộ tile; 254 và 255 = ô trống (kiểu 0 = ĐI ĐƯỢC!)
{ u8 x,u8 y,u8 ww,u8 hh, hh*ww × u8 } … 255  khối tileTop (vẽ đè lên người)
{ u8 x,u8 y,u8 tile } … 255                  tileTop lẻ
{ u8 x,u8 y,u8 idCay } … 255 hoặc 254        vật trang trí map/tree/<id> (vẽ tại (x<<4)+dx, (y<<4)+dy, không chặn)
u8 offline
u8 n, n × { u8 x,u8 y, u16 LE toM, u8 xOut, u8 yOut }   ô cổng (CMLocation)
u8 nNpc, nNpc × { u8 x, u8 y, u8 loại }      NPC làng chỉ client đọc (ảnh npc<idImg[loại]>)
[byte còn lại — thường 1 byte 0; giữ nguyên]
```

**Cổng.**

- `type[ô] = 2000000000 + i`. Với i có bit 2 bật (i = 2, 3, 6, 7…), ô cổng bị client và server coi là "chặn". Đây là đặc điểm của game gốc, giữ nguyên.
- Server cho đổi map khi người chơi đứng trong phạm vi ±8 ô quanh ô cổng (`Map.checkChangeMap`).
- Điểm đến lấy từ `locationMap[nhóm][toM]` (bản thật được gán ở `RealController.add_more_idMapSend2Client`), theo dạng `{map, x, y} × 5 bản sao`.
- `ALL_LOCALTION_MAP` (`MapNapTaiNguyen.loadAllLocaltionMap`) là vùng chữ nhật mà người chơi phải đứng bên trong khi qua cổng.
- `localXaphu` là điểm đến khi bay. Server không đọc bảng này, chỉ gửi cho client.

## 3. `map/map<N>` (lưới quái)

`u8 w, u8 h, h × w × u8 id mẫu quái` (0 và 255 = không có). Mã id quái tăng theo thứ tự hàng → cột.

Trường hợp đặc biệt:

- **Map 17**: giá trị > 81 được cộng thêm 3 lúc đọc.
- **Map 28**: thêm 8 quái mẫu 136 ở toạ độ viết cứng.
- **Map 40 (MOBA) và 42 (núi kho báu)**: vị trí quái gắn với logic tháp/lane. Tool **không đổi chỗ** quái ở hai map này.
- `map/map109` có kích thước khác `cMap/109` (luôn 0 quái).

## 4. Điểm cố định phía server

`docs/pokemon/diem_co_dinh.csv` liệt kê điểm có file:dòng nguồn. Gồm:

- NPC làng `posNPCInVilage`.
- Điểm hồi sinh / về làng (đơn vị ±ô).
- NPC hoa tiêu, boss, điểm boss ngẫu nhiên, ma.
- Tháp, trụ của 201/30–39 (thành).
- Tháp 3 lane của 40/42.
- Sảnh 228, đám cưới 225/226, lôi đài 227, vạn tiên trận (25 → 208–210).

Điểm đến qua cổng và điểm bay nằm ở `docs/pokemon/map_cu/bang_den.json`. `GiaiMaMap` đọc bảng này bằng phản chiếu từ lớp server đã build.

Bảng SQL không lưu toạ độ map, trừ vị trí đã lưu của nhân vật (`tob_char.x/y`) và xe tiêu. Nhân vật đang đứng ở chỗ nay thành vật cản có thể bị kẹt sau khi đổi bản đồ. Các map làng (`CharNapDuLieu`) đã được đặt lại điểm vào.

## 5. Ràng buộc khi vẽ lại

- Giữ nguyên: kích thước, tập id, danh sách cổng (x, y, toM, xOut, yOut), cờ offline, danh sách NPC client, đuôi tệp.
- Chỉ số tile < số tile của bộ (≤ 253; 254/255 = trống).
- Số tile mỗi bộ ≤ mảng màu bản đồ nhỏ của Unity: `t` 232, `t_hang` 116, `t_thanh` 216.
- Ảnh `t_hang`/`t_thanh` ≤ 32767 byte.
- Mọi ô cổng, NPC, điểm đến, điểm cố định và ô quái phải đi được, và đi tới được từ cổng (loang 4 hướng trên kiểu tile thật).
- Không ô trống (vực, kiểu 0) nào được kề ô người chơi tới được.
- Bản mới không dùng cây/tileTop của game gốc (danh sách cây rỗng).

## 6. Bản đồ vẽ lại (chế độ Pokémon)

| Ra | Ai dùng |
|---|---|
| `cMap_pokemon/<id>` (99 map), `cMap_pokemon/{t,t_hang,t_thanh}.type` | Server, qua `BanDoPokemon.duong` ở mọi chỗ mở `cMap/…` (10 lớp). Client nhận qua gói 12 |
| `map/pokemon_spawn/map<N>` (map biển/hồ + map có bãi cỏ cao thiết kế) | Server, qua `BanDoPokemon.duongQuai`. Cùng số lượng từng mẫu quái |
| `map/pokemon_tile/*.png`, `Resources/poke_tile/*.bytes` | Ảnh tile mới (server gửi, Unity và J2ME đọc) |
| `Resources/poke_map/kieu_*.bytes`, `Resources/poke_map/0.bytes` | Unity: `Res.loadTileImage` lấy kiểu tile mới; `Tilemap.loadMap(0, null)` lấy map 0 mới |
| `tools/pokemon/map_j2me/{0,*.type}` | J2ME, qua `tools/pokemon/cai_map_j2me.sh` |

- Tắt `sv.che_do_pokemon` (hoặc thiếu tệp) thì mọi đường trả về tệp gốc.
- Chế độ Pokémon gửi thêm map 0 và 122 (`BanDoPokemon.mapGuiThem`) để client không vẽ bố cục cũ.
- Bản đồ và `.type` mới phải đi cùng nhau, vì chỉ số tile khác hẳn bộ gốc. `KiemMapPokemon` bắt buộc mọi `cMap/<id>` đều có bản mới.

**Cách vẽ** (xem đầu `VeMapMoi.java`):

1. Lập kế hoạch vật liệu theo loại map trong `docs/pokemon/thiet_ke_map.csv`.
   - Viền rừng/vách dày.
   - Đường đất uốn lượn nối cổng, NPC và điểm cố định (cây khung nhỏ nhất + A*).
   - Hồ, biển, rừng, vách đá đặt ở chỗ xa điểm cố định.
   - Bãi cỏ cao 5–30 ô dọc đường. Quái cũ được xếp vào ô cỏ cao gần chỗ cũ nhất.
   - Map mang tên thị trấn có làng nhỏ: Trung tâm, Cửa hàng, 2 nhà, sân, rào, bồn hoa.
   - Thị trấn đặt nhà đúng ô cửa (cổng giữa map).
2. Autotile góc. Góc là vật liệu A khi cả 4 ô quanh góc là A, rồi lấy tile "A/nền_bit" (14 kiểu cho mỗi cặp).
   - Rừng: khối cây 2×2 xếp lệch hàng.
   - Vách đá/vách hang: 2 hàng mặt vách + mặt trên viền tối.
   - Cỏ có 4 biến thể, cỏ sáng theo nhiễu tần số thấp, hoa thành đám.
3. Kiểm trên tile thật rồi sửa lặp (đào đường, gỡ vật cản).

**Ảnh xem trước:**

- `docs/pokemon/map_moi/<id>.png`: ảnh sạch.
- `docs/pokemon/map_moi/kiem/<id>.png`: cổng, NPC, quái, điểm đến.
- `docs/pokemon/map_moi/tong_hop.png`: ảnh tổng hợp.
- `docs/pokemon/map_moi/kiem_tra.txt`: kết quả kiểm.

**Kiểm cách client vẽ** (`htbb-client/j2me/tools/kiem_ve_map/chay.sh [thư_mục_ra]`): vẽ mọi bản đồ bằng chính mã vẽ J2ME (PokeBody/VatThePokemon trên stub lcdui AWT + phép tính `Tilemap.paintTile`/`TileTop`/`Tree`/`NPC.paint`), so từng pixel với ảnh xem trước, và báo: tile ngoài bộ, `.type` jar ≠ server, vật thể nhiều ô thiếu mảnh (sprite nguồn bị cắt), vật thể có nền khác nền xung quanh ("lỗ nền"), ô nền bị chặn (tường vô hình), ô vật thể đi xuyên được, NPC đứng ô chặn/thiếu sprite. Phải ra `LOI 0`.

Mọi vật thể trong bản đồ mới là tile của lớp nền (không dùng tileTop/cây), và mọi ô có hình vật thể đều chặn (trừ ô cửa, cầu, cỏ cao, hoa, sỏi), nên nhân vật không bao giờ đứng "sau" vật thể: vẽ nhân vật sau lớp tile là đúng chiều sâu. Vật thể cao hơn 1 ô (cờ đấu trường, tượng 2×2, cây 2×2, nhà w×3) chiếm đủ ô.

`VeMapMoi.suaNenVat`: vật trang trí 1 ô (biển gỗ, đá, sỏi, hàng rào, đèn, hoa...) nằm giữa vật liệu khác nền của nó thì đổi sang bản cùng loại đúng nền (đá cát/tuyết) hoặc gỡ.

Ảnh bản gốc ở `docs/pokemon/map_cu/`.

```sh
# trong HTBB/ (lớp server đã build ở <build>)
javac -encoding UTF-8 -cp <build> -sourcepath src -d build/tools tools/pokemon/GiaiMaMap.java
java -cp build/tools:<build>:lib/htbb-core.jar GiaiMaMap               # map_cu + bang_den.json
javac -d build/tools tools/pokemon/DungBoTileMoi.java && java -cp build/tools DungBoTileMoi
javac -encoding UTF-8 -sourcepath src -d build/tools tools/pokemon/GiaiMaMap.java tools/pokemon/VeMapMoi.java
java -Djava.awt.headless=true -cp build/tools VeMapMoi               # [id ...] để vẽ vài map
sh tools/pokemon/cai_map_j2me.sh                                     # chép vào htbb-client/j2me/res (không build jar)
```

## 7. Vùng Hoenn (năm 2): bản đồ mới không có bản gốc

29 bản đồ chỉ có ở chế độ Pokémon (`cMap_pokemon/<id>`, không có `cMap/<id>`). Ảnh xem trước nằm ở `docs/pokemon/map_moi/<id>.png` và `kiem/<id>.png`.

| id | Bộ tile | Loại |
|---|---|---|
| 43–62 | `t` | 10 thị trấn (Littleroot, Rustboro, Slateport, Mauville, Lavaridge, Fortree, Lilycove, Mossdeep, Sootopolis, Ever Grande), tuyến đường, rừng, biển, sa mạc, núi lửa |
| 160–165 | `t_hang` | Hang Granite, Thác Meteor, Hang dưới biển, Hang Khởi Nguyên, Tháp Trời, Đường Victory Hoenn |
| 280–282 | `t_thanh` | Đỉnh Tháp Trời, Liên minh Hoenn, Sảnh Nhà vô địch |

Lý do chọn id: không trùng `cMap` gốc, không nằm trong `offMapID` của client, và ngoài khoảng 200–271 (luật thành, công thành, "trong thành").

**Nguồn duy nhất của khung** là `src/com/htbb/pokemon/BanDoHoenn.java`: kích thước, cụm cổng 5 ô trên mép, NPC client và mẫu quái 139–174.

- **Vẽ bản đồ.** `VeMapMoi` dựng "bản gốc ảo" từ lớp này (`dauVaoHoenn`) rồi vẽ như mọi bản đồ khác.
  - Cột `moc` của `thiet_ke_map.csv` có thêm các cờ: `[bien]` bờ biển chạm mép, `[khoi]` biển chiếm gần nửa bản đồ, `[ho]` hồ giữa thị trấn hoặc thêm hồ câu, `[tro]` tro núi lửa, `[nuoc]` hang ngập nước, `[pho]` nhiều nhà, `[rung]` làng giữa rừng.
  - `[dam]` (map loại `bien`, cả map cũ 2/8/19 và Hoenn 46/61): một đầm nước lợ kín hình elip có viền cát và cầu gỗ (`datDamPha`). Hồ trên map biển được câu cả cá nước mặn (`HeSinhThaiMenu.cauCa`: vùng `V_BIEN` → `NUOC_MAN | NUOC_NGOT`), xem `diem_cau_ca.csv`.
  - Thị trấn không có cổng cửa nhà sẽ có một khối Trung tâm + Cửa hàng + 2 nhà ngay trên quảng trường (`datLangGan`).
- **Server.** `real.VungHoenn` đăng ký `Map` (một bản sao, `nregion` 0). Nó nối thêm hàng vào `mapID`/`idArrMap`/`namMap`/`nameMap`/`locationMap` (gói 105), `mapChange`, `ALL_LOCALTION_MAP` và danh sách gửi bố cục (gói 12).
  - **Lối vào duy nhất:** cụm cổng bắc của map 9 (nhóm 9, `toM` 1) có thêm khe 2 là "Littleroot (Hoenn)".
  - **Khoá theo mùa:** `MapXuLyLenh.doChangeMap` gọi `VungHoenn.chanVao`. Khi `sv.lau_dai` bật và trần cấp mùa < 91, hoặc khi nhân vật dưới cấp 84, cổng không cho qua và hiện thông báo.
- **Client.**
  - `TenMapPokemon.HOENN_ID/HOENN_TEN` có bản giống hệt nhau ở server, J2ME, PC và ClientUnity. Client dùng nó cho `Tilemap.setNameMap`.
  - Bản đồ lớn vẽ vùng Hoenn riêng bằng `BanDoVung.veHoenn`.
  - `HePoke.MAP` đã nối dài tới id 282 để chọn nhạc.
- **Kiểm tra.** `test/KiemVungHoenn` kiểm khung, cổng hai chiều, điểm đến, tính liên thông, quái, tên và luật mở cổng. `KiemMapPokemon` chỉ duyệt các map có trong `cMap` gốc.
- **Điểm nhiệm vụ.** Nằm trong `diem_co_dinh.csv`, phần "Vung Hoenn". Hồ câu được sinh lại bằng `sinh_sql_he_sinh_thai.py` ra `diem_cau_ca.csv`.
