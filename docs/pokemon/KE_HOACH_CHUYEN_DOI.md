# Kế hoạch chuyển đổi KPAH → Game Pokémon

> **Nhánh:** `pokemon/chuyen-doi`, có ở cả 3 repo: `KPAH`, `kpah-mod-pb`, `KPAH-Web`.
> **File này là nguồn sự thật duy nhất** của việc chuyển đổi. Ai làm phần nào cũng đọc
> mục 0 và mục 2 trước, rồi tick ô `[ ]` ở mục 9 khi xong việc.
>
> Các file đi kèm (nằm cùng thư mục `docs/pokemon/`):
>
> | File | Là gì | Sinh bằng |
> |---|---|---|
> | `bang_quai.csv` | 138/138 dòng `data_monster` → Pokémon thay thế | `tools/pokemon/sinh_bang_quai.py` |
> | `kiem_ke_sprite.csv` | Mỗi loài cần dùng: có đủ động tác chưa, kích thước khung | `tools/pokemon/tai_sprite_pmd.py` |
>
> Hai file CSV là **file sinh ra, không sửa tay**. Muốn đổi Pokémon cho một con quái thì sửa
> `ANH_XA` trong script rồi chạy lại.

---

## 0. Quy tắc để không bị lệch hướng

1. **Không tự ý đổi một quyết định ở mục 2.** Muốn đổi thì sửa mục 2 trong cùng commit, ghi
   lý do và ngày.
2. **Một nguồn asset duy nhất là PMDCollab** (mục 3). Muốn thêm asset từ nơi khác thì phải ghi
   vào bảng 3.2 trước, nếu không game sẽ lẫn nhiều phong cách vẽ.
3. **Mọi con số dùng chung phải `grep` trước khi gán**, gồm id lệnh mạng, id ảnh, id item, id
   NPC, cột DB (theo `docs/CODING_CONVENTION.md` mục 1). Các giới hạn 1 byte nằm ở mục 7.
4. **Không xoá code KPAH cũ.** Phần cũ bị thay thì đặt sau cờ `che_do_pokemon` (quyết định
   Q8) để các test cũ vẫn chạy được.
5. **Mỗi việc ở mục 9 có tiêu chí "Xong khi".** Chỉ tick ô khi đạt đúng tiêu chí đó, không
   tick theo cảm giác.
6. **Thêm logic thuần thì phải thêm test** vào `test/` (cùng kiểu với `test/KiemPokemon.java`).
7. **Không commit `tools/pokemon/pmd_raw/`.** Thư mục này là bản tải về (CC BY-NC), đã nằm
   trong `.gitignore`.

---

## 1. Mục tiêu và phạm vi

- Trong game **không có nhân vật người**. Người chơi điều khiển **một con Pokémon**. Quái,
  boss, NPC và thú cưỡi cũng đều là Pokémon.
- **Giữ nguyên lối chơi MMO đánh thời gian thực** của KPAH: di chuyển, đánh quái, lên cấp,
  bang hội, sự kiện, PvP. Không chuyển sang đánh theo lượt.
- Chất Pokémon đến từ ba thứ: **loài + 18 hệ khắc chế + tiến hoá**.
- Phong cách hình: **Pokémon Mystery Dungeon** (PMD), sprite 4 hướng nhìn từ trên xuống, rất
  hợp với góc nhìn của KPAH.

**Ngoài phạm vi nhánh này:** bắt Pokémon bằng bóng, đội hình 6 con, đánh theo lượt.

---

## 2. Quyết định đã chốt

| # | Quyết định | Lý do |
|---|---|---|
| Q1 | **5 class cũ ánh xạ sang 5 dòng Pokémon khởi đầu** (bảng 4.1). Cột `tob_char.class` giữ nguyên. | `CreateCharHandler` chỉ có bytecode trong `lib/NQSH_5h.jar`, không có source. Giữ `class` thì không phải viết lại phần tạo nhân vật, và toàn bộ bảng skill `CharManager[class]` vẫn dùng được. |
| Q2 | **Loài lưu ở cột mới `tob_char.dex`** (smallint, 0 = tự suy ra từ class + cấp). | Nhánh Eevee do người chơi chọn nên không suy ra được từ cấp. |
| Q3 | **Tiến hoá theo cấp**, xử lý ngay sau khối lên cấp trong `MapXuLyLenh` (bảng 5). Eevee chọn nhánh ở cấp 25 bằng vật phẩm đá. | Cấp tối đa là 91 (`LevelDetail.expMain`), nên các mốc 10/16/25/30/32/36 rải đều. |
| Q4 | **18 hệ thay cho ngũ hành.** Hệ số được "làm dịu": x4→x2, x2→x1.5, x0.5→x0.7, x0.25→x0.5, x0→x0.3. Code ở `HePokemon.heSoTrongGame`. | Người chơi không đổi được Pokémon giữa trận, nên giữ x0 thì có quái không thể đánh được, còn x4 sẽ đánh chết ngay trong PvP. |
| Q5 | **Thân Pokémon là một sprite nguyên con**, bỏ kiểu ghép nhiều phần (paper doll). Trang bị **không đổi hình**, chỉ cộng chỉ số. | PMD không có bộ phận rời. Client đã có sẵn đường vẽ "nguyên con" (`isLoadAnimalOk` → `myAnimal.paint`, `Char.cs:1298`). |
| Q6 | **Sprite đóng gói sẵn trong client** (`Resources/poke/`). Server chỉ gửi `dex` và `shiny`. | Đường gửi ảnh của server dùng toạ độ u8 (ảnh ghép tối đa 255 px) và `w/h` kiểu sbyte (≤127). Trong khi đó tấm Attack của PMD rộng tới 800×640, có khung 120×136. |
| Q7 | **Chỉ làm `ClientUnity`** (Windows/WebGL/Android/iOS). Ba client trong `kpah-mod-pb` (pc/android/j2me) **không port**. Nhánh bên đó chỉ để đánh dấu. | J2ME không đủ bộ nhớ cho sprite PMD. Ba client mod là mã decompile, sửa 3 lần sẽ rất tốn công. |
| Q8 | **Cờ `che_do_pokemon`** trong `vps/server.ini.tmpl`, mặc định 1 trên nhánh này. Mọi chỗ rẽ nhánh đọc qua một hàm duy nhất. | Giữ được bộ test cũ (characterization) và có thể so sánh A/B khi cân bằng. |
| Q9 | **Hệ của quái lấy từ template** (`data_monster.he/he2`), bỏ việc random hệ khi spawn. | Code hiện tại random 0..4 ở `MapNapTaiNguyen.java:1187-1265`. Nếu giữ, Kyogre có thể ra hệ Lửa. |
| Q10 | **Không thu tiền** (tắt nạp) khi còn dùng asset PMDCollab hoặc tên Pokémon thật (mục 3.3). | Asset là CC BY-NC 4.0 và Pokémon là IP của Nintendo/Game Freak. |

---

## 3. Nguồn asset (đồng bộ một phong cách)

### 3.1 Nguồn chính: PMDCollab

| Repo | Nội dung | Số lượng | Dùng cho |
|---|---|---|---|
| [PMDCollab/SpriteCollab](https://github.com/PMDCollab/SpriteCollab) `sprite/<dex>/` | Sprite động. Mỗi động tác là một file `<Anim>-Anim.png`, **8 hàng = 8 hướng**, kèm `AnimData.xml` (kích thước khung, thời lượng từng khung theo đơn vị 1/60 giây) | 962/1026 loài có sprite, 201 loài đủ toàn bộ động tác | Người chơi, quái, boss, NPC, thú cưỡi, Pokémon đi theo |
| SpriteCollab `portrait/<dex>/Normal.png` | Chân dung 40×40 | Gần như đủ | Avatar, khung chat, màn chọn nhân vật, hộp thoại NPC |
| SpriteCollab `sprite/<dex>/0000/0001/` | Bản **shiny** | Tuỳ loài | Skin shiny, quái hiếm |
| [PMDCollab/RawAsset](https://github.com/PMDCollab/RawAsset) `TileDtef/` | Tileset hầm ngục | 185 bộ | Bản đồ (xem câu hỏi mở M2) |
| RawAsset `Tile/24x24/` | Tileset thị trấn/trại | 21 | Thị trấn |
| RawAsset `Item/` | Icon vật phẩm | 287 | Icon item |
| RawAsset `Particle/`, `Beam/` | Hiệu ứng chiêu | 667 + 15 | Hiệu ứng skill |
| RawAsset `Object/` | Rương, khối, lửa trại… | 89 | Các `vat_the` trong `bang_quai.csv` |
| RawAsset `BG/` | Nền thời tiết | 25 | Hiệu ứng mưa, tuyết |
| RawAsset `Icon/` | Mũi tên, biểu tượng UI | 109 | UI |

Đã kiểm tra trên Pikachu (`0025`):
- `Walk-Anim.png` 128×320 = 4 khung × 8 hướng, khung 32×40.
- `Attack-Anim.png` 800×640, khung 80×80.
- Hiện có 35 loại động tác.

### 3.2 Bảng nguồn được phép

| Loại asset | Nguồn | Ghi chú |
|---|---|---|
| Sprite Pokémon | SpriteCollab | Bắt buộc. Không trộn sprite Gen 5 hay Showdown. |
| Chân dung | SpriteCollab portrait | |
| Tile, item, hiệu ứng, object | RawAsset | |
| Âm thanh | **Chưa chọn** (M4) | |
| *(thêm dòng mới ở đây trước khi dùng nguồn khác)* | | |

### 3.3 Giấy phép (đọc kỹ)

- SpriteCollab: **CC BY-NC 4.0**. Được dùng và sửa, **nhưng không được thương mại** và
  **phải ghi công**. Mỗi loài có `credits.txt`; tool tải sprite đã tải file này về.
- RawAsset **không có file LICENSE**. Phần lớn là tài nguyên trích từ game PMD gốc
  (Chunsoft/Nintendo).
- Tên, hình và thương hiệu Pokémon thuộc Nintendo/Game Freak/The Pokémon Company, và họ đã
  từng gỡ các fan game.
- **Hệ quả:** nhánh này chỉ dùng cho server phi thương mại (Q10). Phải có trang ghi công
  (việc W3). Muốn thương mại thì phải thay toàn bộ asset bằng quái tự vẽ: kiến trúc
  `dex` + sprite đóng gói vẫn giữ nguyên, chỉ thay thư mục ảnh.

---

## 4. Bảng chuyển đổi: nhân vật

### 4.1 Class → dòng Pokémon khởi đầu

Code: `LoaiPokemon.KHOI_DAU_THEO_CLASS`.

| `class` | Class cũ | Vai trò cũ | Pokémon | Hệ | Tiến hoá (cấp) |
|---|---|---|---|---|---|
| 0 | Kiếm khách | Cận chiến cân bằng | Charmander | Lửa | →Charmeleon 16 →Charizard 36 (Lửa/Bay) |
| 1 | Chiến binh | Cận chiến chịu đòn | Bulbasaur | Cỏ/Độc | →Ivysaur 16 →Venusaur 32 |
| 2 | Pháp sư | Phép, 8 skill | Eevee | Thường | Cấp 25 chọn 1/8: Vaporeon, Jolteon, Flareon, Espeon, Umbreon, Leafeon, Glaceon, Sylveon |
| 3 | Đấu sĩ | Cận chiến thủ | Squirtle | Nước | →Wartortle 16 →Blastoise 36 |
| 4 | Cung thủ | Đánh xa | Pichu | Điện | →Pikachu 10 →Raichu 30 |

Cả 21 loài đều có sprite và chân dung trên SpriteCollab (đã kiểm tra bằng `tracker.json`).

### 4.2 Thuộc tính nhân vật: cũ → mới

| KPAH (cũ) | Ở đâu | Pokémon (mới) | Việc phải làm |
|---|---|---|---|
| `charClass` | `Char.java:164`, `tob_char.class` | Dòng Pokémon khởi đầu | Giữ nguyên (Q1) |
| `gender`, `headStyle` | `Char.java:415`, `:163` | **Bỏ.** Client gửi giá trị mặc định | Ẩn ô chọn trong `CreateCharScr.cs` |
| *(chưa có)* | | `dex`, `shiny` | Thêm cột `tob_char.dex`, `tob_char.shiny`; nạp ở `CharNapDuLieu`, lưu ở `saveChar` (`DatabaseNhanVat.java:718,967,1451`) |
| `he` (0..4 ngũ hành) | `LiveActor.he`, `CharNapDuLieu.java:32,52` | `he` = hệ 1 (0..17), thêm `he2` | Gán từ `LoaiPokemon.he1/he2(dex)` khi nạp và khi tiến hoá |
| Cấp 1..91 | `LevelDetail` | Giữ nguyên | |
| `reclass` / chuyển sinh | Không có thật (chỉ là cờ 0/1) | Không dùng | |
| Chỉ số theo class | `CharChiSo.java:22,155,1174` | **Giữ nguyên ở giai đoạn 2.** Cân bằng theo loài ở giai đoạn 6 | |
| 15 skill theo class | `CharManager` mảng `[class][skill][level]` | Chiêu Pokémon: **đổi tên và hiệu ứng, giữ số liệu** | Tên ở `Text.java:412`; hiệu ứng ở client `Skill_*_Type*.cs` |
| Áo/quần/nón/vũ khí đổi hình | `Char.cs:2823-2866` | Không đổi hình (Q5) | Bỏ vẽ part khi `dex > 0` |
| Cải trang `idModel` | `GameScr.cs:6318` | Tạm ẩn. Sau này dùng cho trang phục sự kiện | |
| Chết: `imgDie` + `imgEye` | `Char.cs:1146` | Động tác `guc_nga` (Faint) | |

### 4.3 Trạng thái vẽ: client → động tác PMD

Cột "Ưu tiên" lấy theo `DONG_TAC` trong `tai_sprite_pmd.py`: lấy tên đầu tiên mà loài đó có.

| Trạng thái client | Frame KPAH hiện tại | Động tác (ưu tiên) |
|---|---|---|
| Đứng | 0/1 (`STANDFRAME`) | Idle → Walk khung 0 |
| Chạy | 2/3 (`RUNFRAMEWAL`) | Walk |
| Đánh gần | 4/5 (`SkillAnimate.cs`) | Attack → Strike → QuickStrike → Swing |
| Đánh xa, phép | 4/5 | Shoot → Charge → Attack |
| Trúng đòn | *(chưa có)* | Hurt → Pain |
| Gục | `imgDie` | Faint → Sleep → Hurt |

**Hướng:** client có 4 hướng (`Char.cs:97-103`: DOWN=0, UP=1, LEFT=2, RIGHT=3). PMD có
8 hàng, theo thứ tự Xuống, Xuống-Phải, Phải, Lên-Phải, Lên, Lên-Trái, Trái, Xuống-Trái.
Cách lấy hàng:

- **DOWN → hàng 0**
- **UP → hàng 4**
- **LEFT → hàng 6**
- **RIGHT → hàng 2**

Không cần lật ảnh như kiểu cũ. Tool đóng gói (việc A3) chỉ cắt 4 hàng này để giảm dung lượng.

---

## 5. Bảng chuyển đổi: hệ thống server

| Hệ thống | File:dòng | Thay đổi | Giai đoạn |
|---|---|---|---|
| Ngũ hành trong sát thương người → mục tiêu | `CharChiSo.java:744-755` | Thay khối `(he+2)%5` bằng `att = att * HePokemon.heSoTrongGame(heTanCong, actor.he, actor.he2) / 100`. Giai đoạn 2: `heTanCong` = hệ 1 của người đánh. Giai đoạn 3: hệ của chiêu. | 2 |
| Ngũ hành trong sát thương quái → người | `LiveActor.java:~446` (`attackDam` ở :389) | Như trên | 2 |
| Random hệ khi spawn | `MapNapTaiNguyen.java:1187-1265`, `BossSkelonton.java:472`, `BossNguoiTuyet.java:352` | Bỏ random khi bật `che_do_pokemon`. Lấy hệ từ template (Q9) | 2 |
| Lên cấp → tiến hoá | `MapXuLyLenh.java:114-130` và `:240-260` (sau `newlevel > lastLV`) | `dexMoi = LoaiPokemon.tienHoaKeTiep(dex, level)`. Nếu khác `KHONG` thì cập nhật `dex`, `he`, `he2`, gửi lệnh POKE_THAN (mục 7) cho mình và người ở gần, kèm thông báo "… đã tiến hoá thành …" | 2 |
| Nạp nhân vật | `CharNapDuLieu.initPinfo` :31, `initInfoFromDB` :278 | Đọc `dex`. Nếu bằng 0 thì `dangTheoCap(khoiDau(class), level)` | 2 |
| Lưu nhân vật | `DatabaseNhanVat.java:718, 967, 1451` | Thêm `dex`, `shiny` vào câu UPDATE | 2 |
| Tạo nhân vật | `DatabaseNhanVat.addChar` :1140 (INSERT :1306) | Ghi `dex = khoiDau(class)`. Đồ mặc định `defaultVK` (:1291) giữ để chỉ số không đổi | 2 |
| Eevee chọn nhánh | Mới: menu NPC trong `MapDoMenu` (grep case trống) + 8 item đá | Chỉ cho chọn khi `dex == 133` và `level >= 25` | 4 |
| Tên skill | `Text.java:412` (VI), `:898` (ID) | Bảng tên chiêu theo dòng Pokémon | 3 |
| Template quái | `data_monster` (+ cột `dex`, `shiny`, `he2`), `Database.loadMonsterTemplate` :5632 | Nạp thêm cột, gửi bằng lệnh POKE_QUAI (mục 7) | 3 |
| Boss | 16 file `Boss*.java` (bảng 6.2) | Không đổi logic. Chỉ đổi template, nên đổi hình | 3 |
| NPC tĩnh | `RequestNPCInfoHandler` :148, `res/npc.sh`, `NPC.cs:9-60` | Bảng NPC → Pokémon (việc N1) | 4 |
| NPC sự kiện | `plugins/addNPC.java`, msg -50 | `idImg` → dex | 4 |
| Bot NPC (Char) | `MapNapTaiNguyen.loadNpcServer` :836 | Gán `dex` cho bot | 4 |
| Thú cưỡi | `Animal.java`, `doRideAnimal.java` | Giai đoạn 5 (bảng 6.3) | 5 |
| Pet đi theo | `Pet.java`, `AnimalMove.cs` | Pokémon nhỏ đi theo bằng PokeBody | 5 |

---

## 6. Bảng chuyển đổi: dữ liệu và asset

### 6.1 Quái (`bang_quai.csv`, đủ 138/138 dòng)

| Phân loại | Số dòng | Cách vẽ |
|---|---|---|
| `quai` | 84 | PokeBody, cỡ 1x |
| `boss` | 20 | PokeBody, cỡ 1.5–2x (huyền thoại: Zapdos, Rayquaza, Kyogre, Groudon, Entei, Darkrai…) |
| `thu_cuoi` | 18 | Ponyta, Rapidash, Mudsdale (xem 6.3) |
| `vat_the` | 16 | **Không phải Pokémon.** Dùng ảnh `RawAsset/Object` (cổng thành, nguyên liệu, ngọc 7 sao, rương, thùng) |

**Kết quả kiểm kê sprite** (`kiem_ke_sprite.csv`, 130 loài gồm 9 bản shiny; khoảng 18 MB
bản gốc):

- 130/130 loài có đủ 6 động tác. Một số động tác phải dùng bản thay thế.
- Đứng (Idle), Chạy (Walk) và Trúng đòn (Hurt) có ở 130/130 loài.
- Gục: chỉ 36 loài có `Faint`; 94 loài dùng `Sleep` thay thế, nên client hiện "nằm ngủ" khi
  chết. Nếu muốn đẹp hơn thì thêm hiệu ứng mờ dần.
- Ghi công: 130/130 có `credits.txt`, ghi theo Discord ID hoặc tên người vẽ, cùng giấy phép
  từng dòng (`CC_BY-NC_4`, `PMDCollab_1`…). Việc W3 phải đọc cột giấy phép này.

Script báo lỗi nếu có dòng `data_monster` chưa ánh xạ, hoặc loài được chọn không có sprite
PMD. Lần chạy đầu đã bắt được 3 loài như vậy (Pyroar, Bouffalant, Amoonguss) và đã đổi sang
Arcanine, Tauros shiny, Breloom.

### 6.2 Boss: class Java → Pokémon

Các `id` dưới đây lấy theo `Map.java:3336-3345` và `MapNapTaiNguyen.java:1184-1277`.

| File | Template id | Pokémon | Renderer client cũ cần thay |
|---|---|---|---|
| `BossLinhXa` / `BossBachXa` | 91 / 92 | Serperior / Servine | `Boss_Snake.cs` |
| `BossThanLan` | 39, 82 | Salamence, Zapdos | |
| `BossThuongLuong` | 38 | Gyarados | |
| `BossSkelonton`, `BossSkelontonMoba` | 94 | Dusknoir | |
| `BossNguoiTuyet` | 90 | Abomasnow | |
| `BossDracula` | 116, 117 | Darkrai, Gourgeist | |
| `BossTruRong`, `BossTruRongMoba` | 120 | Rayquaza | `Boss_Dragon.cs` |
| `BossSonTinhThuyTinh` | 129, 130 | Kyogre, Groudon | |
| `BossTrungThu` | 48 | Arcanine (theo bảng quái). **Cần xác nhận** | |
| `BossMoba`, `BossVanTienTran`, `BossLocaltion`, `Boss` (lớp cha) | xem code | Theo template | |
| Mắt quỷ, Thỏ điên, Gấu xám | 93, 113, 115 | Gengar, Lopunny, Ursaring | |

### 6.3 Thú cưỡi, pet, skin

| Cũ | Dữ liệu | Mới |
|---|---|---|
| Ngựa cá nhân, ngựa bang, ngựa quốc gia (6 màu × 3) | `data_monster` 95–112, `map/horse…` | Ponyta, Rapidash, Mudsdale. Màu thứ 6 là shiny; 5 màu còn lại đổi màu bằng palette ở client |
| Linh thú (`tob_animal`, `Animal.java`), 20+ loại | `map/d_effect`, `skinthu/` | Pokémon cưỡi được: Lapras, Arcanine, Gyarados, Dragonite… (việc T1 lập bảng) |
| Skin linh thú (item 905–909, 911) | `SkinLinhThu.java` | Bản shiny của thú cưỡi |
| Pet (`tob_pet`, `Pet.java`), 24 ảnh | `map/pet/` | Pokémon nhỏ đi theo |
| Thần thú | `map/thanthu/` | Pokémon huyền thoại đi theo |
| Hoá hình (`isHoaHinh`) | `Animal.java` | Giữ cơ chế, đổi sang hình Pokémon |

### 6.4 Vật phẩm (915 item, 31 type, xem `docs/danh_muc_item.md`)

| type | Cũ (ví dụ) | Số item | Mới | Hình |
|---|---|---|---|---|
| 0 | Áo | 132 | Khăn quàng (Scarf) | Icon RawAsset/Item |
| 1 | Quần | 71 | Đai (Band) | |
| 2 | Băng đô / nón | 64 | Kính / băng đô (Specs/Band) | |
| 3 | Kiếm (class 0) | 48 | Than lửa (Charcoal) | |
| 4 | Đao (class 1) | 51 | Hạt giống thần (Miracle Seed) | |
| 5 | Bút (class 2) | 49 | Khăn lụa (Silk Scarf) | |
| 6 | Thổ búa (class 3) | 48 | Nước thần (Mystic Water) | |
| 7 | Cung (class 4) | 48 | Nam châm (Magnet) | |
| 8 | Nhẫn | 32 | Huy hiệu (Badge) | |
| 9 | Dây chuyền | 32 | Vòng cổ (Amulet) | |
| 10 | Giày | 32 | Lông vũ tốc độ | |
| 11 | Găng | 32 | Găng đấm (Punching Glove) | |
| 12 | Ngọc bội | 32 | Phiến đá hệ (Plate) | |
| 13 | Cuốc | 8 | Giữ nguyên (đào mỏ) | |
| 14–18 | Bộ Vũ Dũng (đồ thú cưỡi) | 60 | Yên, giáp cho Pokémon cưỡi | |
| 19 | Phi phong | 25 | Ruy băng (không vẽ lên thân) | |
| 20–24 | Vật phẩm vận tiêu, buôn bán | 31 | Giữ, chỉ đổi tên | |
| 25 | Hoả kỳ lân | 3 | Thú cưỡi Pokémon | |
| 26 | Trứng | 5 | **Trứng Pokémon** | |
| 27 | Pet | 33 | Pokémon đi theo | |
| 28 | Tụ hồn đan (tiêu hao) | 44 | Đổi tên thành Berry hoặc thuốc | |
| 29 | Mảnh | 29 | Giữ, chỉ đổi tên | |
| 30 | Skin linh thú | 6 | Skin shiny | |
| *(mới)* | | 8 | **Đá tiến hoá Eevee** (grep id trống trước khi thêm) | |

**Chỉ đổi tên, icon và mô tả. Không đổi `atb`, `price` hay `type`** để giữ cân bằng và để
`Map.isWeapone` (`Map.java:6539`) vẫn chạy đúng.

### 6.5 Thư mục asset: cũ → mới

| Thư mục cũ | Số lượng | Nội dung | Cách xử lý | Giai đoạn |
|---|---|---|---|---|
| `map/c/{leg,body,head,hat,coat,flag}` | 340 | Các bộ phận người | **Không dùng** khi `dex > 0` | 1 |
| `map/wpa/` | 345 | Vũ khí | Không vẽ | 1 |
| `map/sk0..4.png` | 5 | Dải icon skill ở màn chọn nhân vật | Vẽ lại bằng icon chiêu | 3 |
| `map/m/` | 148 | Quái | Thay bằng PokeBody (bỏ qua khi template có `dex > 0`) | 3 |
| `map/boss/` + `data_monster_new/` | 35 | Boss | PokeBody | 3 |
| `map/monster/0..9.png` | 12 | Biến hình (-57) | Không cần nữa. Biến hình chỉ còn là đổi `dex` | 3 |
| `map/d_effect/` | 606 | Hiệu ứng skill, đồ tool, thú, pet | Hiệu ứng skill dùng RawAsset/Particle; phần thú và pet làm ở giai đoạn 5 | 3, 5 |
| `map/imgEffs`, `imgArrow`, `hieuung`, `EFF_PACK` | ~90 | Hiệu ứng, đạn | RawAsset/Particle, Beam | 3 |
| `map/icon/` | 868 | Icon item 16–18 px | RawAsset/Item (bảng 6.4) | 4 |
| `map/pet/`, `thanthu/`, `skinthu/`, `horse`… | ~100 | Thú | PokeBody | 5 |
| `res/npc.sh` (client) | | NPC | PokeBody theo bảng NPC | 4 |
| `map/t*.png`, `cMap/` | 3 tileset + 102 bản đồ | Tile 16 px | **Giữ ở GĐ1–5.** Xem câu hỏi mở M2 | (6) |
| `map/iconclan` | 1344 | Logo bang | Giữ | — |
| Âm thanh | | | M4 | (6) |

---

## 7. Thiết kế kỹ thuật PokeBody

### 7.1 Đóng gói sprite (tool A3: `tools/pokemon/DongGoiSprite.java`)

**Đầu vào:** `tools/pokemon/pmd_raw/<khoa>/`, trong đó `<khoa>` là `0004` (bản thường) hoặc
`0004s` (bản shiny).

**Đầu ra:** `ClientUnity/Assets/Resources/poke/`, gồm 3 file cho mỗi khóa:

| File | Nội dung |
|---|---|
| `<khoa>_anh.bytes` | PNG atlas |
| `<khoa>_dl.bytes` | Dữ liệu khung (định dạng bên dưới) |
| `<khoa>_cd.bytes` | PNG chân dung 40×40 |

Đuôi `.bytes` là bắt buộc để Unity nạp được dưới dạng `TextAsset`, giống
`Image.createImage(string)`. Thư mục `poke/` **được commit**: `.gitignore` có ngoại lệ
`!/ClientUnity/Assets/Resources/poke/`, để build client không cần mạng. Ghi công tổng hợp nằm ở
`ClientUnity/Assets/Resources/poke/GHI_CONG.txt`.

**Atlas:** gồm 6 động tác, xếp dọc theo thứ tự trong bảng 4.3. Mỗi động tác có 4 hàng hướng
theo thứ tự client DOWN(0), UP(1), LEFT(2), RIGHT(3), lấy từ hàng PMD 0, 4, 6, 2. Mỗi hàng chứa
`soKhung` khung đặt ngang nhau.

**`<khoa>_dl.bytes`:** big-endian, đọc bằng `DataInputStream` của client.

```
u8  phienBan = 1
u8  soDongTac = 6                 // 0 dung, 1 chay, 2 danh_gan, 3 danh_xa, 4 trung_don, 5 guc_nga
lặp lại soDongTac lần:
  u16 rongKhung, u16 caoKhung
  u8  soKhung
  u8  thoiLuong[soKhung]          // đơn vị 1/60 s (AnimData.xml)
  u16 yAtlas                      // y của hàng DOWN; hàng hướng d nằm ở yAtlas + d*caoKhung; x bắt đầu từ 0
  i8  dxChan[4], i8 dyChan[4]     // theo hướng: vị trí điểm chân (tâm bóng ở khung 0) so với TÂM khung
u8  coDongTacThat[6]              // 1 = có thật, 0 = dùng bản thay thế (ví dụ guc_nga = Sleep)
```

**Cách vẽ ở client:** đặt khung sao cho điểm chân (`tâm khung + dxChan/dyChan`) trùng toạ độ
`(x, y)` của actor. Đây cũng là điểm chân mà paper doll cũ đang dùng.

### 7.2 Client (`ClientUnity/Assets/Scripts/Game/`)

- Thêm `PokeBody.cs`:
  - Tự nạp `Resources/poke/<khoa>_*` và giữ cache.
  - Có hàm `paint(g, x, y, dir, trangThai, tick, tiLe%)`.
  - Nếu nạp lỗi (không tìm thấy file) thì trả về false, để code gọi quay về cách vẽ cũ.
- `Char.paint`: khi `pokeDex > 0` thì gọi PokeBody và bỏ qua toàn bộ phần vẽ ghép, vũ khí và ngựa.
- `Monster`: template nào có `pokeDex > 0` (nhận qua lệnh -91) thì vẽ bằng PokeBody theo `tiLe`.
- NPC tĩnh: bảng client `type → dex` ghi ở `docs/pokemon/bang_npc.csv`.
- `CreateCharScr.cs`:
  - 5 Pokémon khởi đầu, hiện chân dung.
  - Ẩn ô giới tính và kiểu tóc; gửi `gender = 0`, `headStyle = 0`.
- Màn chọn nhân vật và avatar dùng chân dung.

### 7.3 Lệnh mạng mới

Đã grep `src/` và `ClientUnity/` ngày 2026-10-16: client đang xử lý lệnh từ -76 đến 106, còn
-90, -91, -92, -93 **chưa ai dùng làm số lệnh**. Có chỗ dùng -90/-91/-92 nhưng đó là **id NPC**, một
không gian số khác.

| Số | Tên | Hướng | Nội dung | Gửi khi |
|---|---|---|---|---|
| -90 | POKE_THAN | S→C | `i16 charId, i16 dex, u8 shiny` | Ngay sau mỗi lần gửi lệnh 5 về một Char (kể cả bot NPC); gửi về chính mình sau lệnh 3; khi tiến hoá |
| -91 | POKE_QUAI | S→C | `u8 n`, rồi n × `(u8 idTemplate, i16 dex, u8 shiny, u8 tiLe%)` | Ngay sau lệnh 26 |
| -92 | POKE_TIEN_HOA | S→C | `i16 charId, i16 dexCu, i16 dexMoi` | Khi tiến hoá: gửi cho chính mình và người ở gần, trước lệnh -90 |
| -93 | POKE_CUOI | S→C | `i16 charId, i16 dexThu (0 = không cưỡi), u8 shiny` | Ngay sau MỖI lệnh -90 (`Message.kemTheo`). Lên/xuống thú gửi lại lệnh 3 + 5 nên tự cập nhật |

Chỉ gửi các lệnh này khi `sv.che_do_pokemon=1` (tên khoá thật trong `server.ini` có tiền tố `sv.` như mọi khoá khác). Giữ nguyên định dạng các lệnh cũ.

**Giới hạn đã biết, không được vượt:**

| Thứ | Giới hạn | Nguồn |
|---|---|---|
| Số template quái | ≤ 255 (đếm bằng 1 byte) | Lệnh 26 |
| `palate` quái | ≤ 127 (sbyte) | -47 |
| `w`/`h` quái | ≤ 127 (sbyte) | `MonsterTemplate`. Lý do PokeBody không đi đường này |
| Toạ độ ảnh ghép `DataSkillEff` | ≤ 255 px | `DataSkillEff.cs:166` |
| id cloth | ≤ 127 | -48 |
| Ô biến hình | 10 | `IsAnimal.img` |
| d_effect đã nạp | 301 (dải 8700–8999) | `DataGame.java:254` |

---

## 8. Cơ sở dữ liệu

Thêm file migration `sql/them_cot_pokemon.sql`, đặt tên theo kiểu các file `sql/them_*.sql`
hiện có:

```sql
ALTER TABLE tob_char     ADD COLUMN dex   SMALLINT NOT NULL DEFAULT 0,
                         ADD COLUMN shiny TINYINT  NOT NULL DEFAULT 0;
ALTER TABLE data_monster ADD COLUMN dex   SMALLINT NOT NULL DEFAULT 0,
                         ADD COLUMN shiny TINYINT  NOT NULL DEFAULT 0,
                         ADD COLUMN he2   TINYINT  NOT NULL DEFAULT -1;
-- Phần UPDATE data_monster SET dex=…, shiny=…, he=…, he2=… sinh từ bang_quai.csv (tool Q2)
```

- **Kiểm tra trước khi chạy:** grep `tob_char` và `data_monster` trong `src/` để xem có câu
  `SELECT *` nào đọc theo **chỉ số cột** không. Thêm cột vào cuối bảng thì an toàn, nhưng vẫn
  phải kiểm tra.
- Sao lưu DB bằng `vps/backup_db.sh` trước khi chạy migration.

---

## 9. Lộ trình và checklist

Ký hiệu: `[ ]` chưa làm, `[x]` xong. Ghi tên người làm sau mỗi dòng khi nhận việc.

### Giai đoạn 0: Nền tảng

- [x] **G0.1** Tạo nhánh `pokemon/chuyen-doi` ở 3 repo.
- [x] **G0.2** `com.kpah.pokemon.HePokemon`: 18 hệ và bảng khắc chế.
  - Xong khi: test 51 ô x2, 61 ô x0.5, 8 ô x0 đều đạt.
- [x] **G0.3** `com.kpah.pokemon.LoaiPokemon`: 5 dòng khởi đầu, 21 loài, tiến hoá.
  - Xong khi: `test/KiemPokemon.java` đạt.
- [x] **G0.4** `tools/pokemon/sinh_bang_quai.py` sinh ra `bang_quai.csv`.
  - Xong khi: đủ 138/138 dòng, không còn LỖI.
- [x] **G0.5** `tools/pokemon/tai_sprite_pmd.py` sinh ra `kiem_ke_sprite.csv`.
  - Xong khi: mọi loài có đủ 6 động tác (hoặc động tác thay thế).
- [x] **G0.6** Viết file kế hoạch này.

### Giai đoạn 1: Nhìn thấy Pokémon trong game (chỉ client và tool)

- [x] **A3** Viết `tools/pokemon/DongGoiSprite.java` (Java + ImageIO, máy không có PIL) theo định dạng 7.1.
  - Xong khi: đóng gói đủ 21 loài người chơi, `.bytes` đọc lại khớp `AnimData.xml`.
- [x] **C1** Viết `PokeBody.cs` (nạp, cache, vẽ 6 trạng thái × 4 hướng).
- [x] **C2** `Char.paint`: rẽ nhánh sang PokeBody khi `dex > 0`. Tạm thời suy `dex` ở client
  từ `charClass` + cấp, để chưa phải đụng server.
  - Xong khi: vào game thấy mình là Charmander, đi 4 hướng, đứng, đánh, chết đều đúng.
- [x] **C3** Màn tạo và chọn nhân vật dùng 5 Pokémon (mục 7.2).

### Giai đoạn 2: Server hiểu Pokémon

- [x] **S0** Thêm cờ `che_do_pokemon` (Q8) vào `vps/server.ini.tmpl` và
  `docs/cau-hinh-server-ini.md`, đọc qua một hàm duy nhất.
- [x] **S1** Migration `sql/them_cot_pokemon.sql` (mục 8).
- [x] **S2** Nạp, lưu, tạo nhân vật có `dex` (bảng 5).
- [x] **S3** Thêm field `he2` vào `LiveActor`; gán `he/he2` từ loài.
  - Xong khi: grep mọi chỗ dùng `.he` (pet skin "hợp hệ", item `he`, ngọc) và ghi cách xử lý
    từng chỗ vào bảng ở mục 10.
- [x] **S4** Thay khối ngũ hành bằng `HePokemon.heSoTrongGame` ở 2 chỗ.
  - Xong khi: có test cho hàm nhận hệ số (tách thành hàm thuần) và đánh thử thấy số đúng.
- [x] **S5** Tiến hoá khi lên cấp, kèm lệnh POKE_THAN và POKE_TIEN_HOA.
  - Xong khi: lên cấp 16 ở Charmander thì người ở gần thấy đổi thành Charmeleon.
- [x] **C4** Client nhận POKE_THAN và POKE_TIEN_HOA, bỏ phần suy `dex` tạm ở C2, làm hiệu
  ứng tiến hoá.

### Giai đoạn 3: Quái, boss, chiêu

- [x] **Q2** Tool sinh phần UPDATE `data_monster` từ `bang_quai.csv`.
- [x] **Q3** Server gửi POKE_QUAI; bỏ random hệ (Q9).
- [x] **C5** Client vẽ quái và boss bằng PokeBody (boss phóng to); `vat_the` dùng
  RawAsset/Object.
  - Xong khi: đi hết các map quái, không còn sprite người hay quái KPAH nào.
- [x] **K1** Bảng 5 × 15 chiêu: tên Pokémon, hệ của chiêu, hiệu ứng Particle tương ứng.
  Ghi thành `docs/pokemon/bang_chieu.csv`.
- [x] **K2** Áp tên chiêu (`Text.java:412`) và hệ của chiêu vào S4.
- [ ] **K3** Client: hiệu ứng chiêu dùng RawAsset/Particle.

### Giai đoạn 4: NPC, vật phẩm, Eevee

- [x] **N1** Lập `bang_npc.csv` bằng tool, gồm mọi NPC tĩnh (`NPC.cs:9-60`), NPC sự kiện và
  bot. Mỗi NPC gán một Pokémon (Chansey hồi máu, Kecleon bán đồ, Xatu nhiệm vụ…).
- [x] **N2** Áp bảng NPC vào server và client.
- [x] **I1** Tool đổi tên, icon, mô tả item theo bảng 6.4. Sinh SQL, không sửa tay.
- [x] **I2** 8 item đá tiến hoá + menu chọn nhánh Eevee.

### Giai đoạn 5: Thú cưỡi, pet

- [x] **T1** Bảng linh thú → Pokémon cưỡi được (`docs/pokemon/bang_thu.csv`).
- [x] **T2** Vẽ cưỡi: Pokémon người chơi ngồi trên Pokémon thú. Offset lấy từ `Offsets.png`.
- [ ] **T3** Pet và thần thú → Pokémon đi theo.

### Giai đoạn 6: Hoàn thiện

- [x] **W1** KPAH-Web: đổi thương hiệu, bỏ tên class, thêm Pokédex.
- [ ] **W2** Đổi tên map (192 instance trong `RealController.initMapKPAH`).
- [x] **W3** Trang ghi công: gom mọi `credits.txt` (bắt buộc theo CC BY-NC).
- [ ] **B1** Cân bằng chỉ số theo loài và giai đoạn tiến hoá.
- [ ] Giải quyết các câu hỏi mở M1–M5.

---

## 10. Các chỗ dùng `he` cũ (đã rà ở việc S3)

Nguyên tắc chung:
- Khi cờ bật, **hệ Pokémon (0..17) nằm ở `hePokemon`/`he2`**.
- Trường `he` cũ vẫn là ngũ hành 0..4. Client vẽ `nh[he]` có 5 ô, và vài class chỉ có trong jar gửi thẳng `monster.he`.
- Gửi xuống client thì đổi qua `CheDoPokemon.heGuiClient` (dùng `HePokemon.nguHanhCu`).

| File:dòng | Đang dùng `he` để làm gì | Xử lý |
|---|---|---|
| `CharChiSo.java:747` | Khắc ngũ hành, người → mục tiêu | Cờ bật: `SatThuongHe.apDung` (HePokemon) |
| `LiveActor.java:452` + `attackDam` của 7 boss (BossSkelonton, BossMoba, BossSkelontonMoba, BossBachXa, BossLinhXa, BossThanLan, data/BossMatQuy) | Khắc ngũ hành, quái → người | Như trên |
| `CharNapDuLieu.java:52` | `he[charClass]` | Sau đó `TienHoaPokemon.napLoai` ghi đè bằng hệ của loài |
| `Monster.java:79` | `he = template.he` | Giữ 0..4 (qua `nguHanhCu`); hệ thật nằm ở `hePokemon`/`he2` |
| ~65 chỗ random hệ khi sinh quái (MapNapTaiNguyen 1187–1569, Map, MapTown, MapLienDau, Region…, Boss*) | Random ngũ hành | `heKhiSinh(...)`: cờ bật thì lấy hệ của template. Vẫn gọi random để chuỗi random giữ nguyên khi cờ tắt |
| `MapChienTruongMoba`, `BossSkelontonMoba:479/562/713` | Random; `CharChienTruong` trong jar đánh chỉ số theo 0..4 | Giữ nguyên; sát thương vẫn dùng hệ template |
| `MessageCreator:1734/1938/1999`, `Map.java:15062` | `he` nhân vật gửi trong lệnh 5/3 | `heGuiClient` → 0..4 |
| `SkinLinhThu` "hợp hệ" | Dựa vào `charClass`, không dựa vào `he` | Không đổi |
| `data_item.he`, `GemTemplate.he`, ThanKhi | Hệ của trang bị, ngọc | Không đổi; độc lập với hệ loài |
| Các class chỉ có trong jar: Material, Monster_Clone, MapBoss, PrivateDataMap, RegionMapMoba, RegionNuiChauBau | Random riêng | Không sửa được. Vô hại vì sát thương dùng hệ template |

## 11. Câu hỏi còn mở (cần chủ dự án quyết)

| # | Câu hỏi | Gợi ý |
|---|---|---|
| M1 | Có cần làm lại cho 3 client `kpah-mod-pb` không? | Không (Q7). Đóng băng ở `main`. |
| M2 | Tile 16 px của KPAH so với tile PMD 24 px: làm lại bản đồ không? | Giai đoạn 1–5 giữ tile cũ. Sau đó thử thu nhỏ tile PMD trên một map; nếu xấu thì vẽ lại. |
| M3 | Bang hội, quốc gia giữ tên cũ hay đổi thành "Đội"? | Chỉ đổi chữ, giữ logic. |
| M4 | Âm thanh, nhạc lấy từ đâu? | Nhạc tự do bản quyền (CC0). Không dùng nhạc Pokémon. |
| M5 | `BossTrungThu` (template 48) đang ra Arcanine. Có muốn Pokémon riêng cho sự kiện không? | |

---

## 12. Kết quả kiểm thử (2026-10-16)

| Kiểm thử | Kết quả |
|---|---|
| `test/` đầy đủ (Linux, JDK 21, `--release 8`) | 65/5267 hỏng. **Đúng y bộ 65 lỗi cũ** (14 nhóm, đã so khớp). Mọi nhóm Pokémon đạt: KiemPokemon, KiemPokemonServer, KiemPokemonClient, KiemThuPokemon |
| Biên dịch client: `dotnet build` toàn bộ `ClientUnity/Assets/Scripts` với DLL UnityEngine thật | 0 lỗi |
| Bộ đọc `_dl` C# chạy độc lập (`PokeDuLieu`) trên 178 file sprite thật | Đạt hết: khung nằm trong atlas, điểm chân nằm trong khung |
| `DongGoiSprite` tự đọc lại và so pixel | 178/178 loài khớp |
| Migration và SQL dữ liệu trên MariaDB 10.4 nạp `gamedb7_clean.sql` + `db_new_server.sql` | Chạy hết không lỗi: 122 quái có dex, 696 trang bị đổi tên, 8 đá Eevee (id 960–967) |
| **Đầu–cuối:** game server + login server thật, client giả lập theo giao thức (khoá -40, đăng nhập, tạo, chọn nhân vật, chat GM) | Đăng nhập → -91 (122 template, boss 160%) ngay sau 26. Vào game → -90 dex=4 + -93. `slv 16` → -92 4→5, `slv 36` → -92 5→6. Thoát → DB lưu `dex=6`. Đăng nhập lại → -90 dex=6. Server log không có exception |
| Cờ `sv.che_do_pokemon=0` | Không gửi gói -90…-93 nào; server chạy như cũ |
| KPAH-Web: `nuxi typecheck` + `npm run build` (Node 22) | 0 lỗi. API nạp trả 403 khi `KPAH_NAP_BAT=false` |

**Chưa kiểm được:** hình ảnh thật trong Unity (vị trí, kích cỡ, hiệu ứng tiến hoá), vì máy dev không có Unity Editor. Việc đầu tiên khi mở Unity: vào game nhìn bằng mắt các mục C2, C3, C5, T2.

## 13. Việc còn lại sau đợt này

| Việc | Ghi chú |
|---|---|
| **K3** hiệu ứng chiêu bằng RawAsset/Particle | Hiệu ứng chiêu vẫn là của KPAH (`Skill_*_Type*.cs`), chỉ đổi tên chiêu. Hệ của từng chiêu đã có trong `bang_chieu.csv` (cột `so_he`), nhưng S4 vẫn dùng hệ 1 của người đánh |
| **T3** pet và thần thú đi theo dạng Pokémon | `bang_thu.csv` đã có 41 item pet → dex, và SQL đổi tên đã chạy. Client vẫn vẽ pet bằng ảnh cũ: ảnh pet chọn qua `PetTemplate`/`idImg`, cần thêm một lệnh giống -93 |
| Icon item theo PMD | Mới đổi tên. `xstart` (icon) giữ nguyên; đá Eevee dùng tạm icon 747 |
| Nguồn phát đá Eevee | Chưa có shop hay giftcode nào phát id 960–967 |
| NPC sự kiện (`Npc_Server`) | Vẫn dùng hình cũ |
| `vat_the` (16 dòng) | Giữ hình cũ, chưa dùng RawAsset/Object |
| **W2** tên map, **B1** cân bằng, **M1–M5** | Chưa làm |
| Atlas cao hơn 2048 px (5 loài, cao nhất 3008 px: Gyarados) | Máy cũ giới hạn texture 2048 có thể không nạp được. Nếu gặp, tách atlas theo động tác |

## 14. Lệnh hay dùng

```bash
# Kiểm thử (Windows): test.bat. Trên Linux:
#   javac src → build, javac test → build, java Kiem (giống test.bat, dùng ':' thay ';' trong classpath)

python3 -I tools/pokemon/sinh_bang_quai.py      # sinh lại bang_quai.csv (thoát mã 1 nếu sót)
python3 -I tools/pokemon/tai_sprite_pmd.py      # tải sprite + kiểm kê
python3 -I tools/pokemon/tai_sprite_pmd.py --chi-kiem
python3 -I tools/pokemon/sinh_bang_thu.py       # bang_thu.csv + sql/pokemon_doi_ten_thu.sql
python3 -I tools/pokemon/sinh_sql_vat_pham.py   # sql/pokemon_doi_ten_vat_pham.sql
python3 -I tools/pokemon/ap_ten_chieu.py        # bang_chieu.csv + tên chiêu vào 3 chỗ (chạy lại an toàn)
javac -encoding UTF-8 -d build/tools tools/pokemon/DongGoiSprite.java && java -cp build/tools DongGoiSprite

# Thứ tự SQL trên server mới (sau gamedb7_clean.sql + vps/db_new_server.sql):
#   sql/them_cot_pokemon.sql, sql/pokemon_du_lieu_quai.sql, sql/pokemon_doi_ten_vat_pham.sql,
#   sql/pokemon_doi_ten_thu.sql, sql/pokemon_vat_pham_eevee.sql
```

**65 kiểm tra cũ đang hỏng** (Sự kiện, Hoá hình Pet, Tinh luyện thần binh…) đã có từ trước khi
tạo nhánh, chạy trên Linux. Một số có thể chỉ hỏng trên Linux (đường dẫn, encoding); hãy chạy
`test.bat` trên Windows để có con số gốc. Khi thêm việc mới, không được làm tăng con số này.
Nhóm `com.kpah.pokemon.*` đạt toàn bộ. Khi thêm việc mới, **không được làm tăng** con số 65 này.
