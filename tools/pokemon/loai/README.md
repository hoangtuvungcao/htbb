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

- 251 / 251 loài có gói; tổng ≈ 2,4 MB, trung bình ≈ 10 KB / loài (cấu hình `XA`).
- Toàn bộ 1.025 loài: PMDCollab có sprite dùng được 961, đủ động tác 200, chân dung 1.025, thiếu 64
  (Gen 5: 15, Gen 6: 2, Gen 7: 5, Gen 8: 12, Gen 9: 30).
- Đợt sau (Gen 3+): chạy lại bước 2–5 với dải số mới, ví dụ `tai_sprite_loai.py 252 386` và `DongGoiLoai 252 386`.
