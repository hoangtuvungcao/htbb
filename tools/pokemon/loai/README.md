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

## Dữ liệu loài (một nguồn → SQL)

| Bước | Lệnh | Ra |
|---|---|---|
| Tải nguồn ghim commit (một lần) | `python3 -I tools/pokemon/loai/sinh_loai.py --tai` | `tools/pokemon/nguon_loai/<nguồn>-<commit>/` (không commit, trừ LICENSE) + `nguon.txt` (URL, SHA-256) |
| Sinh | `python3 -I tools/pokemon/loai/sinh_loai.py` | `sql/them_loai_day_du.sql` (nguồn sự thật khi triển khai), `du_lieu/loai/*.csv` (bản xem), `du_lieu/loai/doi_chieu.txt` |

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
