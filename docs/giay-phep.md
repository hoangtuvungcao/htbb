# Giấy phép tài nguyên

Huyền Thoại Bảo Bối là **game fan phi thương mại**, không liên kết và không được bảo trợ bởi Nintendo, Creatures,
GAME FREAK hay The Pokémon Company. Pokémon © Nintendo / Creatures / GAME FREAK / The Pokémon Company.

Tài liệu này liệt kê nguồn và giấy phép của mọi tài nguyên đi kèm game, nơi giữ ghi công, và những việc **bắt buộc**
khi phát hành. Trang công khai tương ứng: `/giay-phep` trên website (`web/app/pages/giay-phep.vue`).

## 1. Tổng hợp

| Tài nguyên | Dùng cho | Giấy phép | Ghi công bắt buộc? |
|---|---|---|---|
| [PMDCollab SpriteCollab](https://github.com/PMDCollab/SpriteCollab) | sprite động + chân dung Pokémon (mọi client, website), biểu tượng ứng dụng | **CC BY-NC 4.0** (một số tệp cũ ghi PMDCollab v1/v2 hoặc không ghi) | **Có** — từng người vẽ |
| [Smogon Sprite Project](https://www.smogon.com/forums/threads/smogon-sprite-project.3647722/) | sprite trận đấu (trước / sau) của 29 loài Gen 6–9 PMDCollab chưa có, đổi sang 4 hướng (`du_lieu/loai/sprite_ngoai.csv`, `nguon_sprite = 'smogon'`) | điều khoản riêng của dự án: "free for use as long as the spriter or the project itself is credited", "free for non-profit use" (không phải CC; chủ game chấp nhận, đang xin giấy phép) | **Có** — từng người vẽ theo bảng ghi công Gen 6 / 7 / 8 / 9 của dự án |
| Pokémon Black / White (Game Freak / Nintendo), bản lưu trên play.pokemonshowdown.com/sprites/gen5 | sprite trận đấu gốc của 13 loài Gen 5 PMDCollab chưa có (`nguon_sprite = 'bw_goc'`) | hình gốc của trò chơi, không có giấy phép; chủ game duyệt dùng phi thương mại (cùng cách với sprite CHUNSOFT của PMDCollab), có thể thay khi có hình được cấp phép | ghi nguồn |
| [PMDCollab RawAsset](https://github.com/PMDCollab/RawAsset) | hiệu ứng chiêu (Particle), icon vật phẩm, vật thể trên bản đồ (Object, Item) | không có tệp LICENSE; trích từ Pokémon Mystery Dungeon (© Nintendo / Chunsoft) | chỉ dùng phi thương mại; vẫn ghi nguồn |
| [Ninja Adventure – Asset Pack](https://pixel-boy.itch.io/ninja-adventure-asset-pack) (Pixel-boy & AAA) | bộ tile bản đồ, vật trang trí bản đồ máy chủ gửi (`server/map/tree`, P2) | CC0 1.0 | không (dự án vẫn ghi) |
| Phần vẽ bằng code của dự án | mép chuyển tiếp autotile, nền / vách hang, biển hiệu, chữ và giao diện kiểu Pokémon, logo; **P2**: Poké Ball, quả mọng, 1344 huy hiệu đội, icon kỹ năng đội, huy hiệu hệ, khung tranh, đèn lồng / tượng / ô vườn / rương, mảnh băng, băng-rôn danh hiệu, vầng sáng, nút và khung giao diện, khiên vùng, vòng quay (mục 7) | CC0 | — |
| Bộ icon + da nút giao diện PNG của dự án (`client_jar/res/ui/`, 0.9.5) | icon HUD / menu / thông báo / hệ / ô trang bị, da nút 9-slice (PC, APK) | CC0 1.0 | — |
| Bộ giao diện kiểu NRO của dự án (`client_jar/res/nro/`, 0.9.7; sinh bởi `tools/giao_dien_nro/sinh_khung_nro.py` + `icon_nro.py`) | khung gỗ / giấy da 9-slice, biển tiêu đề, nút khối, tab, ô đồ, thanh HP / PP / EXP, công tắc, 62 icon pixel 16x16 (mọi client) | CC0 1.0 (tự vẽ, không lấy hình của TeaMobi hay trò chơi khác) | — |
| Icon đồ họ mới, trang phục, hào quang, yên thú, thẻ bạn đồng hành, khung độ hiếm (0.9.8) | `server/map/icon_do_moi/`, `web/public/icon-do/`, `client_jar/res/pk/trang_phuc.png`, `khung_hiem.png` | CC0 1.0 | — |
| [Kenney](https://kenney.nl/assets) — Interface Sounds, Impact Sounds, RPG Audio, Digital Audio, Music Jingles | tiếng hiệu ứng | CC0 1.0 | không (dự án vẫn ghi) |
| Nhạc nền chiptune soạn riêng (`client_jar/tools/lam_am_thanh.py`) | nhạc nền MIDI | CC0 1.0 | — |
| Noto Sans (© Google), Baloo 2 (© The Baloo 2 Project Authors / Ek Type), Nunito, Press Start 2P | chữ bản PC, logo, website; chữ vẽ sẵn vào ảnh băng-rôn danh hiệu (Noto Sans Bold, P2 — không kèm tệp font) | SIL Open Font License 1.1 | giữ nguyên tệp giấy phép |
| TimGM6mb.sf2 (Tim Brechbill, David Bolton) | soundbank MIDI của bản PC Java | GNU GPL v2 | giữ nguyên tệp + GPL-2 |
| Eclipse Temurin JRE 21 | Java đi kèm bản PC Windows / Linux | GPL v2 + Classpath Exception | giữ tệp giấy phép của JRE |
| Launch4j | `HTBB.exe` bọc jar | BSD-3 | chép `LICENSE.txt` |
| Lucide Icons | biểu tượng website | ISC | — |
| [PokéAPI](https://github.com/PokeAPI/pokeapi) `data/v2/csv`, commit ghim `2bf639e5572812a67d2b94903fdcb9d095f5aeb2` | dữ kiện 1.025 loài (hệ, chỉ số nền, tiến hoá, nhóm trứng, môi trường sống, learnset để chọn chiêu) → `sql/htbb.sql` (khối `them_loai_day_du.sql`) | **BSD-3-Clause** (xem 8.1) | **Có** — giữ văn bản bản quyền + giấy phép |
| [Pokémon Showdown](https://github.com/smogon/pokemon-showdown) `data/pokedex.ts`, commit ghim `d601443456c795f77c6e4f2b5b459167f2b10a3c` | chỉ **đối chiếu chéo** hệ / chỉ số / tiến hoá / nhóm trứng (không chép vào game) | **MIT** (xem 8.2) | giữ văn bản giấy phép |

Không dùng nhạc, tiếng hay tile của game Pokémon gốc. Bộ tile bản đồ hiện tại dựng bằng
`tools/pokemon/DungBoTileMoi.java` và **không dùng pixel nào** của tile, cây, nhà hay vật trang trí của game kiếm hiệp
gốc; danh sách từng tile kèm nguồn ở cột `nguon` của [`du_lieu/bang_tile_moi.csv`](../du_lieu/bang_tile_moi.csv). Các gói
Kenney Tiny Town / Tiny Dungeon (CC0) từng được tải về để so sánh, không nằm trong bộ tile hiện tại.

## 2. CC BY-NC 4.0 — hệ quả

Sprite và chân dung PMDCollab theo [CC BY-NC 4.0](https://creativecommons.org/licenses/by-nc/4.0/):

1. **Ghi công (BY):** mọi bản phân phối (jar, APK, bản PC, website) phải kèm tên người vẽ của từng sprite, nguồn và
   link giấy phép, và ghi rõ hình đã được chỉnh sửa (thu nhỏ, cắt khung, đổi định dạng).
2. **Phi thương mại (NC):** chừng nào game còn dùng các sprite này thì **không được thu tiền dưới bất kỳ hình thức
   nào gắn với game** — bán vật phẩm, thẻ tháng, thành viên trả phí, quảng cáo, kể cả "donate đổi quyền lợi". Vì thế hệ
   thống nạp tiền mặc định **tắt** (`sv.nap_tien=0`, `HTBB_NAP_BAT=false`) và web còn đòi xác nhận
   `NAP_DA_CO_GIAY_PHEP` trước khi bán gì (xem [van-hanh.md](van-hanh.md)). Muốn thương mại hoá phải thay toàn bộ bộ
   hình bằng tài nguyên có giấy phép cho phép.
3. Ngoài giấy phép của sprite, tên loài và hình tượng Pokémon là tài sản trí tuệ của chủ sở hữu: "phi thương mại" chỉ
   giảm rủi ro, không phải miễn trừ. Giữ tên game không chứa chữ "Pokémon", không đưa lên cửa hàng ứng dụng, phát hành
   từ website riêng, và có kênh liên hệ để nhận yêu cầu gỡ.

## 3. Ghi công nằm ở đâu

**Không xoá, không rút gọn** các tệp dưới đây khi phát hành. Chúng được sinh tự động — sửa nguồn rồi chạy lại công cụ.

| Tệp | Nội dung | Sinh bởi |
|---|---|---|
| `client_pc_ios/Assets/Resources/poke/GHI_CONG.txt` | ghi công **từng loài**: thời điểm, người vẽ, trạng thái, giấy phép, động tác (nguồn chính) | `DongGoiSprite` (`tools/pokemon/`) khi đóng gói sprite |
| `client_pc_ios/Assets/Resources/poke/GHI_CONG_MAP.txt` | nguồn bộ tile bản đồ | `tools/pokemon/DungBoTileMoi.java` |
| `client_pc_ios/Assets/Resources/poke/GHI_CONG_VAT_THE.txt` | nguồn hình vật thể (RawAsset) | `tools/pokemon/tai_vat_the.py` |
| `client_pc_ios/Assets/Resources/ghicong/GHI_CONG.txt` | bản gộp dễ đọc cho người chơi: tuyên bố không liên kết, danh sách người vẽ (43 sau P2), người vẽ theo loài, hiệu ứng, bản đồ, âm thanh, phông | công cụ ghi công của client |
| `client_jar/res/GHI_CONG.txt` | bản cho jar và APK (bắt buộc có, build dừng nếu thiếu) | `client_jar/tools/sinh_ghi_cong.py` (+ `client_jar/tools/pmdcollab_credit_names.txt`) |
| `client_jar/res/LICENSE-audio.txt` | nguồn và giấy phép từng tệp âm thanh | — |
| `client_pc/res/phong/GIAY_PHEP.txt`, `OFL-NotoSans.txt`, `OFL-Baloo2.txt` | phông bản PC | — |
| `client_pc/res/soundbank/GIAY_PHEP.txt`, `GPL-2.txt` | soundbank TimGM6mb | — |
| `web/public/pokemon/GHI_CONG.txt` | bản sao cho trang `/giay-phep` (đọc lúc build) | chép từ tệp Unity khi đồng bộ dữ liệu web |
| `server/map/loai/GHI_CONG.txt` | gói tải theo nhu cầu: người vẽ bản thường (251 loài), **bản sắc màu** PMDCollab (240 loài, `<NNNN>s.goi`) và **chân dung Pokédex tĩnh** (`web/public/pokemon/loai/<dex>.png`, loài chưa có gói) - cùng commit ghim `4702f10d`, CC BY-NC 4.0 | `tools/pokemon/loai/ghi_cong_loai.py` |

Trong game: Menu màn đăng nhập → **Ghi công** (jar/APK), nút **Ghi công** dưới khung đăng nhập (Unity), F10 → **Giấy
phép** (PC Java). Bản PC Windows đóng gói (`client_pc/dong_goi_windows.sh`) kèm `GHI_CONG.txt` cạnh `HTBB.exe` và thư
mục `giay_phep/` (phông, Launch4j, âm thanh).

## 4. Icon giao diện PNG (0.9.5)

`client_jar/res/ui/ui_96.png` (53 icon 96x96: Poké Ball, túi, nhiệm vụ, bản đồ, menu, chat, thuốc, 16 bóng trang bị,
13 huy hiệu hệ, máy ảnh, xu, thông tin, cảnh báo, lỗi, đóng, lùi, hộp đồ) và `nut_96.png` (da nút thường / nhấn / tắt /
tròn) do dự án **tự vẽ**: `tools/pokemon/sinh_icon_ui.py` render offline bằng Pillow từ thiết kế vector của
`game.pokemon.BieuTuong` (khối có độ sáng, ánh, viền tối mềm, bóng đổ). Không dùng tệp icon bên ngoài (không Kenney, không
game-icons.net cho các biểu tượng này - riêng hình trên icon CHIÊU xem đoạn dưới), không cần ghi công; phát hành theo CC0 1.0. Bản J2ME không dùng các ảnh này (vẽ vector).

Icon CHIÊU (0.9.6, chỉ dùng trong giao diện: nút chiêu HUD, đòn chính, tab Kỹ năng / Ô nhanh, chọn chiêu, gợi ý, bảng
Tự động, trang cẩm nang Chiêu thức): `client_jar/res/ui/chieu_{32,64,96}.png`, `chieu_tron_{32,64,96}.png` và
`web/public/chieu/*.png` — huy hiệu pixel-art màu hệ (dự án tự vẽ, CC0 1.0) mang **hình bóng từ game-icons.net**
(https://game-icons.net), giấy phép **CC BY 3.0** (https://creativecommons.org/licenses/by/3.0/), tác giả: **Lorc,
Delapouite, Skoll, sbed, darkzaitzev**. 45 tệp SVG nguyên bản ở `tools/pokemon/icon_chieu_svg/` (`nguon.txt` = URL ghim
commit `82d94881` của github.com/game-icons/icons); `tools/pokemon/sinh_icon_chieu.py` vẽ SVG (ImageMagick) rồi dựng
huy hiệu và thu BOX + cắt ngưỡng (đã sửa đổi: tô màu, viền, thu nhỏ). Ghi công trong game: màn Giấy phép (GHI_CONG.txt).
Hiệu ứng chiêu trong trận không đổi.

### Đồ họ mới và trang phục (0.9.8)

689 icon 16x16 (`server/map/icon_do_moi/<n>.png`, id ảnh 15000+n, bản sao `web/public/icon-do/`), atlas lớp phủ trang
phục / hào quang `client_jar/res/pk/trang_phuc.png` (+ `trang_phuc.bin`) và khung độ hiếm `khung_hiem.png` do dự án **tự
vẽ bằng lưới ký tự** (`tools/do_trang_phuc/hinh_trang_bi.py`, `hinh_trang_phuc.py`; dựng lại bằng
`python3 -I tools/do_trang_phuc/sinh.py`). Không dùng tệp ảnh bên ngoài (không PMDCollab, không gói icon nào), phát
hành CC0 1.0, không cần ghi công.

## 5. Âm thanh

| Tệp trong jar (`/am/`) | Nguồn | Giấy phép |
|---|---|---|
| `s_click`, `s_xacnhan`, `s_huy`, `s_mo`, `s_nhat` | Kenney "Interface Sounds" | CC0 1.0 |
| `s_danh`, `s_chimang`, `s_bidanh`, `s_ithieuqua` | Kenney "Impact Sounds" | CC0 1.0 |
| `s_truot`, `s_vang` | Kenney "RPG Audio" | CC0 1.0 |
| `s_phep`, `s_hieuqua`, `s_chet`, `s_dichchuyen`, `s_hoimau`, `s_canhbao` | Kenney "Digital Audio" | CC0 1.0 |
| `s_lencap`, `s_tienhoa`, `s_nhiemvu` | Kenney "Music Jingles" (8-Bit) | CC0 1.0 |
| `s_boss` | tự tổng hợp | CC0 1.0 |
| `n_*.mid` (tiêu đề, thị trấn, đường, rừng, hang, tuyết, sa mạc, đấu chiến, boss) | chiptune soạn bằng thuật toán | CC0 1.0 |

Gói Kenney tải về **không** đưa vào git, chỉ đưa tệp đã chuyển đổi. Sinh lại:
`python3 -P client_jar/tools/lam_am_thanh.py <thư mục giải nén> client_jar/res/am client_jar/res_nhe/am`.
Bản PC Java dùng thêm tiếng 44,1 kHz (`client_pc/res/am_hq`) cùng nguồn.

Soundbank TimGM6mb (GPL-2) được phát hành **nguyên vẹn, tách riêng** (thư mục `am/` cạnh `HTBB-PC.jar`, không đóng vào
jar) và chỉ đọc lúc chạy — gộp đơn thuần, không làm game thành tác phẩm phái sinh. Bỏ được bằng cách xoá tệp (game dùng
soundbank mặc định của Java) hoặc chạy với `-Dhtbb.soundbank=<tệp .sf2>`.

## 6. Tải lại bộ tile bản đồ

Tệp tải về nằm trong `.gitignore`. Chạy ở gốc repo:

```sh
D=tools/pokemon/pmd_raw_tile_moi; mkdir -p $D/ninja_full $D/zip
for f in TilesetField TilesetNature TilesetWater TilesetHouse TilesetFloor TilesetFloorDetail TilesetRelief TilesetElement; do
  curl -sSL -o $D/ninja_full/$f.png \
    "https://media.githubusercontent.com/media/series-ai/jam-ready-assets/main/ninja-adventure/2D/top-down-rpg/Backgrounds/Tilesets/$f.png"
done
curl -sSL -o $D/zip/ninja_adventure_godot.zip https://github.com/pixel-boy/NinjaAdventure/archive/refs/heads/main.zip
unzip -oq $D/zip/ninja_adventure_godot.zip -d $D/ninja_adventure_godot
javac -d /tmp/htbb-tools tools/pokemon/DungBoTileMoi.java && java -cp /tmp/htbb-tools DungBoTileMoi
```

`tileset_interior_floor.png` lấy từ repo của chính tác giả (`pixel-boy/NinjaAdventure`).

## 7. Phần mềm

- `server/loginServer/CheckLoginSocket.jar` là thư viện đóng của game gốc, không có mã nguồn: không phát hành lại
  riêng, giữ repo private.
- Thư viện bên thứ ba còn lại trong `server/lib`: Connector/J 5.1.49, `jxl-2.6.jar`, `json-20250107.jar`. Bản
  Connector/J 5.0.8 và `jxl` mà jar lõi cũ gói kèm đã bỏ cùng jar (Connector/J 5.0.8 vốn bị 5.1.49 che trên classpath,
  `jxl` gói kèm giống hệt `lib/jxl-2.6.jar`).

### Mã nguồn lõi cũ

- Tới 10/2026 server chạy kèm `server/lib/htbb-core.jar`: 190 lớp lõi **đã biên dịch** của game kiếm hiệp gốc
  (mạng `io.SessionManager`, `server.ServerController`, `real.Actor`, `real.cmd.CreateCharHandler`, `real.ItemTemplates`,
  `real.QuestTemplate`, `data.NewClan`, chợ, lôi đài, MOBA, áp tiêu, xổ số…), không có mã nguồn. Bytecode phiên bản 50
  (Java 6), có dấu hiệu dịch bằng trình biên dịch Eclipse (ECJ: tên `access$0`, mảng hằng bỏ phần tử 0).
- P3 dịch ngược 169 lớp chỉ có trong jar thành mã nguồn trong `server/src` (cùng package và tên lớp) bằng
  **Vineflower 1.11.1** (`org.vineflower:vineflower:1.11.1` từ Maven Central, SHA-256
  `a615d07ddbbcd489369674f40e42df639c32be95410890b38f173d5c1e2ea39c`), sửa tay tối thiểu để biên dịch `--release 8`.
  21 lớp còn lại đã có bản ghi đè trong `src/` từ trước nên bỏ bản trong jar. Mỗi tệp phục hồi mở đầu bằng chú thích
  `PHUC HOI TU LOI CU`.
- Bản quyền: mã này **thuộc tác giả game gốc**, không phải của dự án và không đổi giấy phép khi chuyển thành mã nguồn.
  Cùng điều kiện như client gốc ở dưới: chỉ dùng cho dự án fan phi thương mại, không phát hành lại riêng, giữ repo
  private. Jar gốc còn trong lịch sử git (commit trước P3) nếu cần đối chiếu bytecode.
- Client gốc TeaMobi (J2ME, Unity) đã được dịch ngược và sửa thành HTBB; dự án không có quyền với mã gốc ngoài mục
  đích fan phi thương mại.
- Thư viện server: MySQL Connector/J 5.1 (GPL v2 + FOSS exception), JExcelAPI `jxl` (LGPL). Công cụ build client J2ME:
  ProGuard (GPL v2), xem `client_jar/tools/LICENSE`.

## 7. Thay toàn bộ hình của máy chủ cũ (P2)

Mọi hình của game kiếm hiệp gốc (KPAH / NQSH) mà client Pokémon còn có thể vẽ đã được thay **tại chỗ** (giữ tên tệp, kích
thước, bảng khung) bằng hình Pokémon PMDCollab hoặc hình dự án tự vẽ bằng code. Không dùng sprite vật phẩm chính thức của
Nintendo (Bulbapedia / Serebii...) ở bất kỳ đâu.

| Nhóm | Nguồn hình mới | Công cụ sinh lại |
|---|---|---|
| Thú cưỡi, pet, skin linh thú, hoá hình (ô dự phòng `d_effect`, `pet/`, `avtathu/`, `skinthu/`); 5 loài mới 0483 Dialga, 0675 Pangoro, 0716 Xerneas, 0745 Lycanroc, 0879 Copperajah | PMDCollab SpriteCollab (CC BY-NC 4.0, ghi công theo loài) | `tools/pokemon/sinh_bang_thu.py`, `tai_sprite_pmd.py`, `DongGoiSprite.java`, `ve_thu_pet_thay_cu.py` |
| NPC dự phòng (`npc.sh`), quái / boss dự phòng (`map/monster`, `thanthu`, `rong/`), tranh Tủ bình, icon skin | PMDCollab SpriteCollab | `sinh_npc_sh.py`, `sinh_anh_quai_cu.py`, `sinh_icon_thay_cu.py` |
| Icon vật phẩm còn ở dải cũ (`map/icon`, `images_*`, `iconquest`, `fruit`) | PMDCollab RawAsset (đã dùng từ trước) | `sinh_icon_pokemon.py` → `sinh_icon_thay_cu.py` |
| Huy hiệu đội, icon kỹ năng đội, huy hiệu hệ, Poké Ball, quả mọng; hiệu ứng kiếm (mảnh băng), băng-rôn danh hiệu, vầng sáng, icon chiêu `sk0..4`; đèn lồng, tượng, ô vườn, rương; nút / khung giao diện Unity, khiên vùng, vòng quay | tự vẽ bằng code (CC0) | `sinh_icon_thay_cu.py`, `ve_hieu_ung_thay_cu.py`, `ve_giao_dien_thay_cu.py`, `ve_vong_quay.py` |
| Vật trang trí bản đồ `map/tree`; đèn lồng, tượng, ô vườn, rương | Ninja Adventure (CC0); tự vẽ (CC0) | `tools/pokemon/p2_ban_do/tree_moi.py`, `ve_trang_tri.py`, `ve_dat.py` |
| Bộ phận người / vũ khí paper-doll (`c/`, `wpa/`, `wpsplash/`, ngựa cũ gửi trong `-8`) | làm trong suốt, giữ định dạng (client Pokémon không vẽ) | `xoa_hinh_nguoi_cu.py` |

**Kiểm tra:** `python3 -I tools/pokemon/kiem_tai_nguyen_cu.py` (và test `KiemTaiNguyenCu` trong bộ kiểm server) so dấu
vân tay của mọi tệp / ảnh trong gói đang phát hành với `du_lieu/tai_nguyen_cu.txt` (tệp của máy chủ cũ, sinh từ lịch sử
git); hình cũ chỉ được giữ khi có dòng lý do trong `du_lieu/tai_nguyen_cu_giu.csv` (hiệu ứng chiến đấu chủ game cho
giữ, khung giao diện chung, phông chữ, dải `d_effect` còn lại).

**Lưu ý giấy phép cho chủ game:** sprite SpriteCollab ghi `CHUNSOFT` và toàn bộ PMDCollab RawAsset là hình trích từ game
Pokémon Mystery Dungeon (© Nintendo / Chunsoft) — giống sprite chính thức, phải nằm trong giấy phép chủ game định xin.

## 8. Dữ liệu loài (1.025 loài, `sql/htbb.sql` (khối `them_loai_day_du.sql`))

Bộ sinh `tools/pokemon/loai/sinh_loai.py` đọc dữ liệu tải về **đúng commit ghim** (thư mục
`tools/pokemon/nguon_loai/<nguồn>-<commit>/`, tệp dữ liệu không commit, `nguon.txt` giữ URL + SHA-256 từng tệp) và sinh
tệp SQL là nguồn sự thật khi triển khai. Văn bản giấy phép dưới đây chép nguyên văn từ tệp LICENSE ở đúng commit ghim
(bản gốc cũng nằm trong `tools/pokemon/nguon_loai/*/LICENSE*`). Không dùng chữ mô tả của Bulbapedia / Serebii / wiki
(CC BY-NC-SA hoặc không có giấy phép mở); mô tả Pokédex (`data_loai.mo_ta`) tự viết. Không dùng `PokeAPI/sprites`
(hình không có giấy phép mở) và không dùng tiếng kêu gốc. Tên loài giữ tên chính thức (là nhãn hiệu của TPC, xem 2.3).

Sprite Gen 1–2 tải theo nhu cầu (`server/map/loai/<NNNN>.goi`) lấy từ PMDCollab SpriteCollab ghim commit
`4702f10d888d19e804c5986a91170dfc92ae01b8` (CC BY-NC 4.0, mục 2), ghi công từng loài ở `server/map/loai/GHI_CONG.txt`
(sinh bởi `tools/pokemon/loai/ghi_cong_loai.py`) và cột `data_loai_sprite.ghi_cong`.

### 8.1 PokéAPI — BSD-3-Clause (`LICENSE.md` tại commit `2bf639e5`)

```
Copyright (c) © 2013–2023 Paul Hallett and PokéAPI contributors (https://github.com/PokeAPI/pokeapi#contributing). Pokémon and Pokémon character names are trademarks of Nintendo.

All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

* Neither the name of PokéAPI nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
```

### 8.2 Pokémon Showdown — MIT (`LICENSE` tại commit `d6014434`)

```
The MIT License (MIT)

Copyright (c) 2011-2026 Guangcong Luo and other contributors http://pokemonshowdown.com/

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
```
