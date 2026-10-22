# tools/pokemon/loai — hình Pokémon tải theo nhu cầu

Các công cụ đóng gói sprite cho những loài **không** đóng sẵn trong jar. Máy chủ gửi gói của từng loài cho client
khi cần (client J2ME lưu RMS, có LRU), nên jar không phình lên theo số loài.

Tất cả chạy ở **gốc repo**. Dữ liệu tải về là dữ liệu không tin cậy: chỉ đọc bằng `python3 -I`, không bao giờ chạy.

## Các bước

| Bước | Lệnh | Ra |
|---|---|---|
| 1. Kiểm kê (không dùng mạng) | `python3 -I tools/pokemon/loai/kiem_ke_sprite_loai.py` | `du_lieu/kiem_ke_sprite_loai.csv` (1.025 dòng, commit, không sửa tay) + bảng tóm tắt theo thế hệ |
| 2. Tải sprite ở commit ghim | `python3 -I tools/pokemon/loai/tai_sprite_loai.py 1 251` | `tools/pokemon/pmd_raw/<NNNN>/` (không commit, `.gitignore`) |
| 3. Đóng gói | xem lệnh `javac` / `java` bên dưới | `server/map/loai/<NNNN>.goi` (commit) |
| 4. Ghi công | `python3 -I tools/pokemon/loai/ghi_cong_loai.py` | `server/map/loai/GHI_CONG.txt` (commit) |
| 5. Kiểm lại | `java -cp $B DongGoiLoai --kiem` rồi chạy lại bước 1 | mọi gói đọc được, cột `goi_server` cập nhật |

```sh
B=/tmp/dg_loai; mkdir -p $B
javac -nowarn -encoding UTF-8 -d $B client_jar/src/game/pokemon/PokeDuLieu.java \
    client_jar/src/game/pokemon/PokeDongTac.java client_jar/src/game/pokemon/HieuUngBang.java \
    client_jar/tools/DongGoiPokeJ2me.java tools/pokemon/DongGoiSprite.java tools/pokemon/loai/DongGoiLoai.java
java -Djava.awt.headless=true -cp $B DongGoiLoai 1 251     # đóng gói dải số Pokédex
java -Djava.awt.headless=true -cp $B DongGoiLoai --kiem     # kiểm mọi gói trong server/map/loai
```

## Bổ sung (loai-bo-sung): bản sắc màu, neo phụ kiện, mô tả Pokédex, chân dung tĩnh

| Bước | Lệnh | Ra |
|---|---|---|
| Tải bản sắc màu (shiny) ở commit ghim | `python3 -I tools/pokemon/loai/tai_sprite_loai.py --shiny 1 251` | `tools/pokemon/pmd_raw/<NNNN>s/` (không commit) |
| Đóng gói thường + shiny (cả hai có khối neo) | `java … DongGoiLoai 1 251` rồi `java … DongGoiLoai --shiny 1 251` (thêm `tools/pokemon/TinhNeoPoke.java` vào lệnh `javac`) | `server/map/loai/<NNNN>.goi`, `<NNNN>s.goi` (commit) |
| Ghi công | `python3 -I tools/pokemon/loai/ghi_cong_loai.py` | `GHI_CONG.txt`: người vẽ bản thường, bản sắc màu, chân dung tĩnh |
| Chân dung Pokédex tĩnh (loài chưa có gói) | `python3 -I tools/pokemon/loai/tai_chan_dung_loai.py` (`--kiem`) | `web/public/pokemon/loai/<dex>.png` (40×40 PMDCollab, không đổi điểm ảnh) |
| Khối SQL | `python3 -I tools/pokemon/loai/sinh_bo_sung_loai.py` (`--kiem`) | `sql/nang_cap.sql` khối `them_loai_bo_sung.sql` (ngay sau `them_loai_day_du.sql`) |
| Kiểm neo + bảng soát | `client_pc/test/KiemNeoLoai.java` (lệnh ở đầu tệp) | `du_lieu/loai/neo_loai.csv`, ảnh soát `neo_<a>_<b>.png` |
| Kiểm thẻ Pokédex có mô tả | `client_pc/test/KiemPokedexMoTa.java` (lệnh ở đầu tệp) | ảnh thẻ 4 cỡ màn hình |
| Kiểm kho sprite + shiny + ngân sách | `client_jar/tools/KiemKhoSprite.java` | thoát 1 = hỏng |

- **Shiny**: PMDCollab để bản sắc màu ở nhánh `0000/0001` của mỗi loài (tracker: `subgroups.0000.subgroups.0001`, tên
  "Shiny"). Dùng được khi cùng luật bản thường (`sprite_complete ≥ 1`, có `Walk`). Gen 1–2: **240 / 251** loài có;
  **11 loài "chưa có shiny"** (PMDCollab mới có bản tô màu nháp, chưa có sprite): #16 Pidgey, #26 Raichu, #58 Growlithe,
  #147 Dratini, #161 Sentret, #162 Furret, #171 Lanturn, #191 Sunkern, #203 Girafarig, #215 Sneasel, #225 Delibird.
  **Không** tự đổi bảng màu: không có nguồn bảng màu shiny chính thức nào có giấy phép, nên loài thiếu không bao giờ
  được tung shiny ngoài trời (`data_loai_sprite.shiny = 0`) cho tới khi PMDCollab có sprite thật.
  Toàn bộ 1.025 loài: PMDCollab có shiny dùng được cho 920 loài (đợt sau chạy cùng lệnh với dải mới).
- **Neo phụ kiện**: `DongGoiLoai` gọi `TinhNeoPoke.tinh` (Offsets.png của PMDCollab: đầu / tay / tâm thân từng khung)
  rồi `DongGoiPokeJ2me.themNeo` gắn khối `'N'` cuối `.dl`, chỉ các khung jar giữ lại (cấu hình `XA`). Client tính neo
  trang phục từ chính điểm ảnh khung (`NeoTrangPhuc`) với điểm PMD làm hạt giống, nên thẻ bạn đồng hành / trang phục vẽ
  được trên loài tải theo nhu cầu (bạn đồng hành hoang dã bắt được, Pokémon Hộp PC đi theo). `--kiem` từ chối gói thiếu neo.
- **Mô tả Pokédex**: `mo_ta_loai.json` là nguồn duy nhất của `data_loai.mo_ta` (1.025 mô tả 1–2 câu, tự viết từ sự
  kiện hệ / nơi sống / đặc điểm / tiến hoá, không chép hay dịch Pokédex chính thức, Bulbapedia, Serebii; không có từ
  cấm của `ChuPokemon.TU_CAM`). Khối SQL chỉ ghi ô đang rỗng (giữ mô tả người vận hành đã sửa trên web).
- **64 loài chưa có sprite PMDCollab hoàn chỉnh** (4702f10d = commit mới nhất ngày 22/10/2026; 0 PR đang mở; 66 fork:
  chỉ 2 fork của nhóm Pokémon Auto Chess có thêm tệp - #520 là hình **Unfezant chép tạm** ("unfezant temporary
  sprites", không credits) và #874 chỉ thêm động tác cho nhánh shiny, không credits → không dùng).
  `tai_sprite_nhap.py` lấy hình THẬT còn lại của chính PMDCollab (`du_lieu/loai/sprite_nhap.csv`):
  - **dạng khác hoàn chỉnh** (4): #870 Falinks "Brass", #999 Gimmighoul "Roaming", #1003 Ting-Lu "Temp", #1008 Miraidon
    "Low_Power";
  - **bản nháp** (`sprite_complete = 0` nhưng repo có tệp, 18): #593, #618, #683, #733, #865, #868, #874, #879, #883,
    #917, #929, #945, #948, #968, #990, #1020, #1021, #1025. Động tác thiếu dùng lại khung của chính người vẽ bằng
    `<CopyOf>` (vd Walk = Idle) - không vẽ thêm; 9 loài chỉ có Idle (đi lại bằng khung đứng).
  Còn **42 loài** không có hình trong game (chỉ chân dung tĩnh, không bao giờ ra ngoài trời): #514 Simisear, #516 Simipour,
  #520 Tranquill, #522 Blitzle, #523 Zebstrika, #538 Throh, #558 Crustle, #564 Tirtouga, #565 Carracosta, #591 Amoonguss,
  #592 Frillish, #616 Shelmet, #626 Bouffalant, #668 Pyroar, #732 Trumbeak, #735 Gumshoos, #756 Shiinotic, #765 Oranguru,
  #837 Rolycoly, #838 Carkol, #839 Coalossal, #866 Mr. Rime, #878 Cufant, #893 Zarude, #931 Squawkabilly, #942 Maschiff,
  #943 Mabosstiff, #944 Shroodle, #947 Brambleghast, #949 Toedscruel, #950 Klawf, #954 Rabsca, #956 Espathra,
  #962 Bombirdier, #973 Flamigo, #986 Brute Bonnet, #993 Iron Jugulis, #1001 Wo-Chien, #1002 Chien-Pao, #1014 Okidogi,
  #1022 Iron Boulder, #1023 Iron Crown (chỉ có ảnh nháp trên Discord, không có tệp trong repo).
  `ChonLoaiHoangDa` không bao giờ chọn loài `trang_thai = 0`; `trang_thai = 1` khi có gói / jar.
- **Mở loài (loai-bo-sung):** đợt B Gen 3 (Hoenn, cấp 83–100) + Vùng hoang dã xoay vòng Gen 4–9 (`loai_duyet.json`
  `xoay_vong`, `docs/thiet-ke-game.md` 2.6b). Tải + đóng gói: `tai_sprite_loai.py 252 1025` (+ `--shiny`),
  `tai_sprite_nhap.py` (+ `--shiny`), `DongGoiLoai 252 1025` (+ `--shiny`). Đã mở theo thế hệ: 151 / 100 / 135 / 84 / 129 /
  64 / 56 / 77 / 76 = 872 loài; gói 983 thường + 939 sắc màu (≈ 21,7 MB máy chủ).
- Nguồn khác: không có kho sprite Pokémon nào khác có giấy phép tương thích CC BY-NC (sprite chính thức / bản rip game
  và kho fan không ghi giấy phép đều không dùng được), nên không lấp bằng nguồn khác.

## Nguồn và giấy phép

- Sprite: PMDCollab SpriteCollab, **ghim commit** `4702f10d888d19e804c5986a91170dfc92ae01b8`
  (`raw.githubusercontent.com/PMDCollab/SpriteCollab/<commit>/sprite/<NNNN>/…`). Giấy phép **CC BY-NC 4.0**:
  không thương mại, phải ghi công từng người vẽ (`credits.txt` → `server/map/loai/GHI_CONG.txt`).
- `tracker.json` cùng commit nằm ở `tools/pokemon/nguon_loai/pmdcollab-<commit>/`. Một loài **dùng được** khi
  `sprite_complete ≥ 1` và có khoá `Walk` trong `sprite_files` (giá trị true/false của khoá là cờ khoá chỉnh sửa
  của PMDCollab, không phải có / không có). Loài không dùng được chỉ có trong Pokédex (chân dung), không xuất hiện
  ngoài trời.
- Nếu repo chính đã có `tools/pokemon/pmd_raw/<NNNN>/` (tải trước đây) thì bước 2 chép sang thay vì tải lại
  (`HTBB_PMD_RAW_CU` đổi chỗ tìm).

## Đường ống đóng gói

`DongGoiLoai` gọi lại đúng hai công cụ đang dùng cho jar, không viết lại thuật toán:

1. `tools/pokemon/DongGoiSprite.dongGoi` — `pmd_raw/<NNNN>` → `<NNNN>_{anh,dl,cd}.bytes` (định dạng Unity,
   ghi vào thư mục tạm, **không** đụng `client_pc_ios`).
2. `client_jar/tools/DongGoiPokeJ2me.dongGoi` với cấu hình `XA` (loài chỉ nhìn xa: ít khung, đánh xa dùng chung
   ảnh đánh gần, ảnh bảng màu) → `<NNNN>.dl`, `<NNNN>_<a>.png`; chân dung 40×40 → `<NNNN>_c.png`.
   Ảnh động tác giống hệt cách jar đóng gói loài quái; `.dl` không có khối điểm neo phụ kiện (chỉ dùng cho người chơi).
3. Gom các tệp của một loài vào **một** gói (big-endian, `DataOutputStream`):

```
'H' 'T' 'P' 'K'   u8 phienBan = 1   i16 dex   u8 shiny (0)   u8 n
n × ( writeUTF tenTep  ("0016.dl" / "0016_0.png" / "0016_c.png")   i32 len   len byte )
```

Tên tệp và nội dung giống hệt `client_jar/res/pk`, nên `PokeBody` đọc được nguyên vẹn sau khi giải gói.

## Số liệu (22/10/2026, Gen 1–2)

- 251 / 251 loài có gói; tổng ≈ 2,7 MB, trung bình ≈ 10,7 KB / loài (cấu hình `XA`, có khối neo phụ kiện).
- 240 / 251 loài có gói sắc màu `<NNNN>s.goi`; tổng ≈ 2,6 MB, trung bình ≈ 10,8 KB. Gói lớn nhất 34 KB, cặp thường +
  shiny lớn nhất 68 KB (< 1/4 ngân sách RMS 1 MB; `KiemKhoSprite` mô phỏng nạp cả 491 gói qua LRU, không lúc nào vượt).
- Toàn bộ 1.025 loài: PMDCollab có sprite dùng được 961, đủ động tác 200, chân dung 1.025, thiếu 64
  (Gen 5: 15, Gen 6: 2, Gen 7: 5, Gen 8: 12, Gen 9: 30).
- Đợt sau (Gen 3+): chạy lại bước 2–5 với dải số mới, ví dụ `tai_sprite_loai.py 252 386` và `DongGoiLoai 252 386`.

## Dữ liệu loài (một nguồn → SQL)

| Bước | Lệnh | Ra |
|---|---|---|
| Tải nguồn ghim commit (một lần) | `python3 -I tools/pokemon/loai/sinh_loai.py --tai` | `tools/pokemon/nguon_loai/<nguồn>-<commit>/` (không commit, trừ LICENSE) + `nguon.txt` (URL, SHA-256) |
| Sinh | `python3 -I tools/pokemon/loai/sinh_loai.py` | `sql/nang_cap.sql` (khối `them_loai_day_du.sql`) (nguồn sự thật khi triển khai), `du_lieu/loai/*.csv` (bản xem), `du_lieu/loai/doi_chieu.txt` |

- Nguồn: PokeAPI `data/v2/csv` commit `2bf639e5572812a67d2b94903fdcb9d095f5aeb2` (BSD-3-Clause), đối chiếu chéo Pokémon
  Showdown `data/pokedex.ts` commit `d601443456c795f77c6e4f2b5b459167f2b10a3c` (MIT), trạng thái sprite từ `tracker.json`
  PMDCollab. Văn bản giấy phép: `docs/giay-phep.md` mục 8.
- Chỗ **duy nhất** sửa tay: `loai_duyet.json` (đợt mở, luật bậc + ghi đè, môi trường → vùng sinh thái, dải cấp, vật phẩm
  tiến hoá, danh mục chiêu dùng chung + chiêu đặc trưng, lệch Showdown đã chấp nhận). Sửa xong chạy lại bộ sinh và
  `server/test` (`KiemLoaiDayDu`).
- Bộ sinh dừng (mã 1) nếu: chuỗi tiến hoá thiếu / có vòng, hệ sai, loài thiếu chiêu hoặc trùng chiêu, chiêu đặc trưng
  gán cho họ dưới T4, loài thuộc đợt đang mở mà không có hình, lệch Showdown ngoài danh sách cho phép.
- **Mở đợt mới** (vd Gen 3): thêm số đợt vào `dot_mo.dang_mo`, tải + đóng gói sprite (bảng trên), thêm khu vực nhà
  (`vung.nha`), chiêu đặc trưng cho họ T4–T6 / khởi đầu của thế hệ đó, chạy lại bộ sinh rồi chạy tệp SQL trên máy chủ
  (máy chủ tự nạp lại bảng loài ≤ 5 phút).
