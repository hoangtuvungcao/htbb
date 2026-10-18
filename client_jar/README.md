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
# KiemPokeDuLieu: sprite/hiệu ứng/âm thanh jar khớp Unity + từ điển khớp server (xem đầu tệp tools/KiemPokeDuLieu.java)
```
