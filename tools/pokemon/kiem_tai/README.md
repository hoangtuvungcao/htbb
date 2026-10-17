# Test tải / ổn định — `BotTai`

Bot nói giao thức thật của server (khoá -40, đăng nhập, tạo/chọn nhân vật, di chuyển, đánh quái,
chat). Dùng để kiểm tra server chịu được nhiều người chơi cùng lúc và không rơi kết nối.

## 1. Chạy server

```sh
MYSQL=mysql sh tools/pokemon/chay_tren_novadb.sh      # NovaDB + login + game (cổng 19129)
```

## 2. Tạo tài khoản bot (chạy một lần)

NovaDB không hỗ trợ truy vấn chéo CSDL (`account.team_user` từ `kpah_db`), nên tạo theo 2 bước:

```sh
m() { mysql --no-defaults -h127.0.0.1 -P3318 -ukpah -phtbb --skip-ssl --default-character-set=utf8mb4 "$@"; }
for i in $(seq -w 1 50); do echo "INSERT INTO team_user(username,password,phone,regdate) VALUES ('tai0$i',PASSWORD('123456'),'',NOW());"; done | m account
m account -N -e "SELECT id,username FROM team_user WHERE username LIKE 'tai0%'" \
  | awk '{printf "INSERT INTO 5h_active(userID,username,time_end) VALUES (%s,\x27%s\x27,\x271794740314\x27);\n",$1,$2}' | m kpah_db
```

Chạy bot một lần ngắn để mọi bot tự tạo nhân vật (`tai001`… cùng tên tài khoản).

## 3. Cấp quyền GM cho bot đánh quái (tuỳ chọn)

Bot GM dùng lệnh chat `slv` (lên cấp) và `mto` (dịch chuyển). Server cần **cả hai**: tên nhân vật nằm
trong `cau_hinh.sv.adminAccounts` **và** `pInfo` trường 16 = 1. Chỉ làm trên server thử.

```sh
m kpah_db -N -e "SELECT id,pInfo FROM tob_char WHERE charname IN ('tai001','tai002','tai003','tai004','tai005','tai006','tai007','tai008','tai009','tai010')" \
  | python3 -c "import sys
for l in sys.stdin:
    i,p=l.rstrip('\n').split('\t',1); d=p.split(','); d[16]='1'
    print(\"UPDATE tob_char SET pInfo='%s' WHERE id=%s;\" % (','.join(d), i))" | m kpah_db
m kpah_db -e "UPDATE cau_hinh SET gia_tri='admin,admin2,chienthan,tai001,tai002,tai003,tai004,tai005,tai006,tai007,tai008,tai009,tai010' WHERE khoa='sv.adminAccounts'"
sh tools/pokemon/chay_tren_novadb.sh dung && MYSQL=mysql sh tools/pokemon/chay_tren_novadb.sh   # nạp lại cấu hình
```

## 4. Chạy

```sh
javac -d build/kiem_tai tools/pokemon/kiem_tai/BotTai.java
java -cp build/kiem_tai -Dgm=10 BotTai 127.0.0.1 19129 50 600 tai   # 50 bot, 10 GM, 10 phút
```

Mỗi 10 giây in: số bot đang chơi, rơi kết nối, gói tin, đòn đánh, quái hạ, tiến hoá, RAM/CPU server.
Sau khi chạy, kiểm tra log server (`chay_novadb/log/game.log`) không có `Exception` / `SQLite error`,
và dữ liệu đã lưu: `SELECT charname,xp,dex FROM tob_char`, `SELECT COUNT(*),SUM(so_ha) FROM tob_pokedex`.

## Kết quả đã đo (máy dev, NovaDB 0.3.0) — xem `docs/pokemon/KE_HOACH_CHUYEN_DOI.md` mục 16
