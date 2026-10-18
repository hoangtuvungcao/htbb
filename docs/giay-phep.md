# Giấy phép tài nguyên

Huyền Thoại Bảo Bối là **game fan phi thương mại**, không liên kết và không được bảo trợ bởi Nintendo, Creatures,
GAME FREAK hay The Pokémon Company. Pokémon © Nintendo / Creatures / GAME FREAK / The Pokémon Company.

Tài liệu này liệt kê nguồn và giấy phép của mọi tài nguyên đi kèm game, nơi giữ ghi công, và những việc **bắt buộc**
khi phát hành. Trang công khai tương ứng: `/giay-phep` trên website (`web/app/pages/giay-phep.vue`).

## 1. Tổng hợp

| Tài nguyên | Dùng cho | Giấy phép | Ghi công bắt buộc? |
|---|---|---|---|
| [PMDCollab SpriteCollab](https://github.com/PMDCollab/SpriteCollab) | sprite động + chân dung Pokémon (mọi client, website), biểu tượng ứng dụng | **CC BY-NC 4.0** (một số tệp cũ ghi PMDCollab v1/v2 hoặc không ghi) | **Có** — từng người vẽ |
| [PMDCollab RawAsset](https://github.com/PMDCollab/RawAsset) | hiệu ứng chiêu (Particle), icon vật phẩm, vật thể trên bản đồ (Object, Item) | không có tệp LICENSE; trích từ Pokémon Mystery Dungeon (© Nintendo / Chunsoft) | chỉ dùng phi thương mại; vẫn ghi nguồn |
| [Ninja Adventure – Asset Pack](https://pixel-boy.itch.io/ninja-adventure-asset-pack) (Pixel-boy & AAA) | bộ tile bản đồ | CC0 1.0 | không (dự án vẫn ghi) |
| Phần vẽ bằng code của dự án | mép chuyển tiếp autotile, nền / vách hang, biển hiệu, chữ và giao diện kiểu Pokémon, logo | CC0 | — |
| [Kenney](https://kenney.nl/assets) — Interface Sounds, Impact Sounds, RPG Audio, Digital Audio, Music Jingles | tiếng hiệu ứng | CC0 1.0 | không (dự án vẫn ghi) |
| Nhạc nền chiptune soạn riêng (`client_jar/tools/lam_am_thanh.py`) | nhạc nền MIDI | CC0 1.0 | — |
| Noto Sans (© Google), Baloo 2 (© The Baloo 2 Project Authors / Ek Type), Nunito, Press Start 2P | chữ bản PC, logo, website | SIL Open Font License 1.1 | giữ nguyên tệp giấy phép |
| TimGM6mb.sf2 (Tim Brechbill, David Bolton) | soundbank MIDI của bản PC Java | GNU GPL v2 | giữ nguyên tệp + GPL-2 |
| Eclipse Temurin JRE 21 | Java đi kèm bản PC Windows / Linux | GPL v2 + Classpath Exception | giữ tệp giấy phép của JRE |
| Launch4j | `HTBB.exe` bọc jar | BSD-3 | chép `LICENSE.txt` |
| Lucide Icons | biểu tượng website | ISC | — |

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
| `client_pc_ios/Assets/Resources/ghicong/GHI_CONG.txt` | bản gộp dễ đọc cho người chơi: tuyên bố không liên kết, danh sách 42 người vẽ, người vẽ theo loài, hiệu ứng, bản đồ, âm thanh, phông | công cụ ghi công của client |
| `client_jar/res/GHI_CONG.txt` | bản cho jar và APK (bắt buộc có, build dừng nếu thiếu) | `client_jar/tools/sinh_ghi_cong.py` (+ `client_jar/tools/pmdcollab_credit_names.txt`) |
| `client_jar/res/LICENSE-audio.txt` | nguồn và giấy phép từng tệp âm thanh | — |
| `client_pc/res/phong/GIAY_PHEP.txt`, `OFL-NotoSans.txt`, `OFL-Baloo2.txt` | phông bản PC | — |
| `client_pc/res/soundbank/GIAY_PHEP.txt`, `GPL-2.txt` | soundbank TimGM6mb | — |
| `web/public/pokemon/GHI_CONG.txt` | bản sao cho trang `/giay-phep` (đọc lúc build) | chép từ tệp Unity khi đồng bộ dữ liệu web |

Trong game: Menu màn đăng nhập → **Ghi công** (jar/APK), nút **Ghi công** dưới khung đăng nhập (Unity), F10 → **Giấy
phép** (PC Java). Bản PC Windows đóng gói (`client_pc/dong_goi_windows.sh`) kèm `GHI_CONG.txt` cạnh `HTBB.exe` và thư
mục `giay_phep/` (phông, Launch4j, âm thanh).

## 4. Âm thanh

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

## 5. Tải lại bộ tile bản đồ

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

## 6. Phần mềm

- `server/lib/htbb-core.jar` và `server/loginServer/CheckLoginSocket.jar` là thư viện đóng của game gốc, không có mã
  nguồn: không phát hành lại riêng, giữ repo private.
- Client gốc TeaMobi (J2ME, Unity) đã được dịch ngược và sửa thành HTBB; dự án không có quyền với mã gốc ngoài mục
  đích fan phi thương mại.
- Thư viện server: MySQL Connector/J 5.1 (GPL v2 + FOSS exception), JExcelAPI `jxl` (LGPL). Công cụ build client J2ME:
  ProGuard (GPL v2), xem `client_jar/tools/LICENSE`.
