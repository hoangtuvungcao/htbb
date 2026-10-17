# Nguồn asset bản đồ (chế độ Pokémon)

Bộ tile bản đồ mới (`map/pokemon_tile/`, `ClientUnity/Assets/Resources/poke_tile/`) được dựng bằng
`tools/pokemon/DungBoTileMoi.java`. Nó **không dùng pixel nào** của tile, cây, nhà hay vật trang trí của game gốc.
Bản đồ mới không còn vật trang trí lệnh -48 (`map/tree`). Cây, nhà, hàng rào và biển gỗ đều là tile.
Danh sách từng tile kèm nguồn nằm ở `docs/pokemon/bang_tile_moi.csv` (cột `nguon`).

| Gói | Tác giả | Giấy phép | Nguồn gốc (đã đọc trang giấy phép) | Bản tải dùng |
|---|---|---|---|---|
| **Ninja Adventure – Asset Pack** | Pixel-boy & AAA | **CC0 1.0** ("released under the Creative Commons Zero (CC0) license", trang itch.io) | https://pixel-boy.itch.io/ninja-adventure-asset-pack | Các tệp `Backgrounds/Tilesets/Tileset*.png` của gói, lấy từ bản sao trên GitHub (Git LFS) `series-ai/jam-ready-assets`, thư mục `ninja-adventure/2D/top-down-rpg/Backgrounds/Tilesets/`. `tileset_interior_floor.png` lấy từ repo của chính tác giả, https://github.com/pixel-boy/NinjaAdventure |
| Kenney Tiny Town / Tiny Dungeon | Kenney (www.kenney.nl) | CC0 1.0 (`License.txt` trong gói) | https://kenney.nl/assets/tiny-town, https://kenney.nl/assets/tiny-dungeon | Đã tải để so sánh. **Không dùng** trong bộ tile hiện tại |
| Phần vẽ bằng code | dự án HTBB | CC0 | `DungBoTileMoi.java` | Vùng chuyển tiếp autotile (mép nước, bọt, bờ đất, viền cỏ trùm đường, mép mềm cỏ sáng/tối), nền hang, vách hang, biển hồi phục/cửa hàng |

Tải lại (chạy trong `HTBB/`; thư mục đích nằm trong `.gitignore`):

```sh
D=tools/pokemon/pmd_raw_tile_moi; mkdir -p $D/ninja_full $D/zip
for f in TilesetField TilesetNature TilesetWater TilesetHouse TilesetFloor TilesetFloorDetail TilesetRelief TilesetElement; do
  curl -sSL -o $D/ninja_full/$f.png \
    "https://media.githubusercontent.com/media/series-ai/jam-ready-assets/main/ninja-adventure/2D/top-down-rpg/Backgrounds/Tilesets/$f.png"
done
curl -sSL -o $D/zip/ninja_adventure_godot.zip https://github.com/pixel-boy/NinjaAdventure/archive/refs/heads/main.zip
unzip -oq $D/zip/ninja_adventure_godot.zip -d $D/ninja_adventure_godot
javac -d build/tools tools/pokemon/DungBoTileMoi.java && java -cp build/tools DungBoTileMoi
```

Ghi công trong game: `ClientUnity/Assets/Resources/poke/GHI_CONG_MAP.txt`. CC0 không bắt buộc ghi công, nhưng dự án vẫn ghi.

`tools/pokemon/DoiTileMap.java` (G7.A1: tô lại màu tile gốc) **đã bị thay thế**. Chạy lại nó sẽ ghi đè
`map/pokemon_tile/` bằng tile gốc tô màu, làm sai chỉ số so với `cMap_pokemon/`. Đừng chạy lại. `KiemTilePokemon`
sẽ báo lỗi nếu bị ghi đè.
