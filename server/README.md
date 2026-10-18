# server — game server + login server Huyền Thoại Bảo Bối

Game server Java (cổng TCP 19129) và login server `CheckLoginSocket` (cổng 8023, chỉ nội bộ). Chạy ở thư mục này:
mọi đường dẫn trong mã nguồn (`map/`, `cMap/`, `server.ini`, các tệp `.txt` log) là tương đối với `server/`.

```
src/            mã nguồn (package real, data, io, server, com.htbb.*)    test/        bộ kiểm thử (Kiem.java + Kiem*.java)
lib/            htbb-core.jar (thư viện đóng của game gốc), jxl, mysql-connector
map/            tài nguyên: ảnh, tile pokemon_tile/, trang trí , dữ liệu hiệu ứng
cMap/   bản đồ Pokémon                                            loginServer/ CheckLoginSocket.jar + server.ini (tạo tay)
htbb-server.jar jar đang chạy (nằm trong git — VPS chạy đúng tệp này)    chay_novadb/ chạy thử trên NovaDB
```

## Build, chạy, kiểm thử

| Việc | Linux / macOS | Windows |
|---|---|---|
| Build jar | lệnh `javac --release 8` + `jar` trong [docs/build.md](../docs/build.md) | `build.bat` → `HTBB_NEW.jar`, rồi `capnhat.bat` |
| Kiểm thử | `java … Kiem` (xem [docs/build.md](../docs/build.md)) | `test.bat` |
| Chạy thử | `cd loginServer && java -jar CheckLoginSocket.jar`, rồi `java -jar htbb-server.jar` | `loginServer\run.bat`, rồi `run.bat` |

Sửa `src/` xong phải build lại **và commit `htbb-server.jar`**: VPS không tự biên dịch. Chạy đúng thì log in
`CONNECTED TO SERVER LOGIN` rồi `Listen on port 19129`; thiếu login server thì log kêu `CONNECT TO SERVER LOG IN FAIL`
và không ai đăng nhập được.

`server.ini` là tệp riêng của từng máy (bị `.gitignore`): mẫu cho máy dev [`../vps/server.ini.linux.example`](../vps/server.ini.linux.example),
trên VPS sinh từ [`../vps/server.ini.tmpl`](../vps/server.ini.tmpl). Mọi khoá: [docs/cau-hinh.md](../docs/cau-hinh.md).
CSDL: [sql/README.md](../sql/README.md). Triển khai: [docs/trien-khai.md](../docs/trien-khai.md). Kiến trúc:
[docs/kien-truc.md](../docs/kien-truc.md).

## Ràng buộc trước khi sửa cấu trúc

| Thứ | Bị khoá bởi |
|---|---|
| `htbb-server.jar` ở `server/` | `WorkingDirectory=/opt/htbb/server` + `java -jar htbb-server.jar` trong `vps/htbb-game.service` |
| `lib/` (đúng tên) | `Class-Path: lib/...` trong MANIFEST |
| `map/`, `cMap/` | đường dẫn ghi cứng trong mã nguồn |
| `server.ini`, các `.txt` | đọc/ghi theo thư mục chạy |
| tên package `real`, `data`, `io`, `server`, `real.cmd` | `lib/htbb-core.jar` (xem dưới) |

`lib/htbb-core.jar` chứa hàng trăm lớp đã biên dịch **không có mã nguồn**, trùng package với `src/`. Lúc chạy, lớp nào
có trong `htbb-server.jar` thì thắng, thiếu thì rơi xuống `htbb-core.jar`; nhiều lớp trong jar đóng gọi ngược lớp của
`src/` theo tên (vd `real/Char`). Đổi tên package hay tên lớp bị tham chiếu sẽ **biên dịch sạch, test đạt, rồi ném
`NoClassDefFoundError` lúc chạy**. Muốn đổi phải gỡ phụ thuộc `htbb-core.jar` trước.

Các lớp lớn (`real/Map.java`, `real/Char.java`, `data/Database.java`) đã được tách bớt sang lớp phụ cùng package
(`MapDoMenu`, `MapSuKien`, `CharChiSo`, `DatabaseNhanVat`…); phần Pokémon và hệ thống mới nằm trong `com.htbb.*`.
