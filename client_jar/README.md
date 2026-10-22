# client_jar — bản J2ME (MIDP 2.0) Huyền Thoại Bảo Bối

Client gốc của game (code TeaMobi dịch ngược + phần Pokémon `src/game/pokemon`). Đây là **nguồn code game duy nhất**:
bản PC (`client_pc/`) và bản Android (`client_apk/pokemon`) chép `src/` + `shared/mod/` lúc build.

```
src/          code game + phần Pokémon        shared/mod/   mod dùng chung (Tự động đánh, menu nhanh, Tiện ích...)
res/          ảnh, dữ liệu, âm thanh (cả APK/PC dùng)   res_nhe/  bản nhẹ (res_nhe/pk, res_nhe/am) cho GOI=nhe
shared/banner, shared/brand   key art, logo, biểu tượng   api/   khai báo WMA chỉ để biên dịch
tools/        Down45, ProGuard, DongGoiPokeJ2me, KiemPokeDuLieu, kiem_ve_map/, script ảnh/âm thanh
```

## Build

Cần JDK 9+ (`javac --release 8`) và `rt.jar` của JRE 8 (ProGuard): `JAVA8_HOME=/usr/lib/jvm/java-8-openjdk-amd64`
hoặc `RT_JAR=...`.

```sh
MAY_CHU=127.0.0.1:19129 HTBB_ASSETS=pc sh client_jar/build.sh 97     # bản DEV/thử (mọi jar vào server MAY_CHU)
MAY_CHU_CHINH=host:cong GOI=nhe sh client_jar/build.sh               # bản phát hành (+ bản nhẹ)
```

Ra `client_jar/out/`: `HTBB-v<N>.jar`, `HTBB-v<N>-release.jar` (ProGuard, `mapping-v<N>.txt` giữ riêng),
`HTBB-v<N>-thu.jar` (khi có `MAY_CHU`), `HTBB-v<N>-nhe.jar` (khi `GOI=nhe`).

## Cấu hình

- `MAY_CHU_CHINH=host:cong` (hoặc `MAY_CHU_CHINH=` trong `tools/phat-hanh/may_chu.conf`): máy chủ của mọi bản,
  ghi vào MANIFEST `HTBB-May-Chu`. Thiếu cả hai và không có `MAY_CHU` là dừng.
- `MAY_CHU=host:cong`: bản thử `-thu.jar`; chỉ đặt `MAY_CHU` = bản DEV (`HTBB-Ban-Thu: 1`, không phát hành).
- Số bản / phiên bản: tham số đầu hoặc `BAN=` / `PHIEN_BAN=` trong `tools/phat-hanh/phien_ban.conf`.
- `HTBB_ASSETS=pc` (= `../client_pc_ios/Assets`): đóng gói lại sprite Pokémon `res/pk`, `res/pt`, `res_nhe/pk`
  (đọc thêm `du_lieu/*.csv`).
- `res/GHI_CONG.txt` (ghi công PMDCollab, CC BY-NC 4.0) bắt buộc có — sinh bằng `tools/sinh_ghi_cong.py`.

## Kiểm tra

```sh
sh client_jar/tools/kiem_ve_map/chay.sh /tmp/kiem_ve_map      # client vẽ mọi bản đồ server/cMap (LOI phải 0)
# 0.9.7.1: jar J2ME THẬT trong MicroEmulator, heap nhỏ + RMS giới hạn + socket trả từng mảnh nhỏ, cache rỗng, tự đăng
# nhập -> tạo / chọn Pokémon -> vào thế giới (cần server dev; CAT_SAU=310000 MONG_LOI=1 DUNG_MS=8000 [THU_LAI=1]: máy
# chủ "im" giữa chừng -> phải hiện hộp lỗi + Thử lại, không quay mãi). Đọc đầu tệp chay.sh.
W=176 H=208 RMS=4096 RMS_BAN_GHI=1024 sh client_jar/tools/kiem_tai_j2me/chay.sh client_jar/out/HTBB-v<N>-thu.jar 19129 <tk> 7168
# KiemPokeDuLieu: sprite/hiệu ứng/âm thanh jar khớp Unity + từ điển khớp server (xem đầu tệp tools/KiemPokeDuLieu.java)
# KiemKhoSprite (1.0): -125 loài riêng từng quái, -126 bảng loài / Pokédex / sprite tải theo nhu cầu (kho RMS LRU 1 MB)
#   chạy trên lớp bản PC: xem đầu tệp tools/KiemKhoSprite.java
```

## Giao diện kiểu NRO (0.9.7)

Một lớp thành phần chung cho cả jar J2ME, APK và PC (`client_jar/src/game/pokemon`):

| Lớp | Việc |
|---|---|
| `ChuDe` | bảng màu (tên hằng giữ từ 0.9.6, giá trị NRO: `NEN` giấy da, `CHINH` cam, `CHU` nâu đậm, `BONG` bóng cứng), chữ theo vai, `chuVien` (chữ viền), `bien` (biển tiêu đề đỏ), `cham` |
| `KhungNro` + `KhungNroBang` (sinh) | vẽ mảnh 9-slice / hình cố định / icon pixel từ `res/nro/khung.png`, `icon.png` (lát `drawRegion`, phóng nguyên) |
| `Bang` | tấm (`khungTam`, `oTam`), tab gỗ (`phanDoan`), nút khối (`nut` / `vienNut`, kiểu `CHINH` cam, `XAC_NHAN` xanh lá, `PHU` xanh dương, `TRUNG` giấy da, `NGUY` đỏ, `TAT` đá, `GO` gỗ), hàng nổi (`hang`), thẻ lõm (`the`), ô đồ (`oDo`, `oKhoa`, `oTron` = ô vuông), thanh trượt, công tắc, `nutVuong` (HUD) |
| `GiaoDien` | hộp / bảng / dải tiêu đề / thanh HP-PP-EXP / viên (`khoiPixel`) cho màn của game gốc |
| `LuoiTui` | lưới hành trang (tab lọc, đếm ô, ô khoá -> mở rộng `-107`, hàng ví) |
| `TheVatPham` | thẻ thông tin vật phẩm + nút Dùng / Mặc / Tháo / Vứt / Bán (chọn số lượng) |
| `TheNguoiChoi`, `ViTien` | thẻ người chơi HUD, hàng ví Xu / Lượng / Lượng khoá |

Màn mới: dùng `Bang` / `ChuDe` / `KhungNro`, **không** tô khối trắng / teal trực tiếp - `python3 tools/giao_dien_nro/kiem_kieu_cu.py`
(trên ảnh `ChupTrongGame`) gắn cờ màn còn kiểu cũ. Sửa hình: `tools/giao_dien_nro/sinh_khung_nro.py` (mảnh) và `icon_nro.py`
(icon vẽ bằng lưới ký tự), chạy lại bộ sinh; `--kiem` kiểm tệp trong repo khớp.
