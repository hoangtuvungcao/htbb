# sql — một tệp CSDL đầy đủ

Thư mục này chỉ có **`htbb_day_du.sql`**: cả hai CSDL `account` và `htbb_db` — schema, dữ liệu Pokémon, danh mục cấu
hình, chỉ mục. Không có dữ liệu người chơi.

## Máy mới

Cài cấu hình MariaDB **trước** (`vps/htbb-mariadb.cnf`: utf8, `lower_case_table_names=1`; xem
[docs/trien-khai.md](../docs/trien-khai.md)), rồi:

```sh
mysql -u root < sql/htbb_day_du.sql      # tạo account + htbb_db, xong
mysql -u root -e "UPDATE account.team_user SET password = PASSWORD('mật-khẩu-mạnh') WHERE username = 'admin';"
```

Tài khoản `admin` trong tệp có mật khẩu ngẫu nhiên không ai biết: đổi ngay như trên. Server tự tạo thêm bảng theo kỳ và
bảng phụ (`CREATE TABLE IF NOT EXISTS`) lúc chạy lần đầu.

## CSDL đang chạy (production): nâng cấp, không nạp lại

**Không** nạp `htbb_day_du.sql` lên CSDL có người chơi (tệp `DROP TABLE` rồi tạo lại). Dùng một lệnh:

```sh
sudo bash /opt/htbb/vps/nang_cap_csdl.sh
```

Script sao lưu (`vps/backup_db.sh`, hỏng là dừng) → nạp [`vps/nang_cap_csdl.sql`](../vps/nang_cap_csdl.sql) → kiểm 8
bảng thiết kế và danh mục cấu hình. Chạy lại bao nhiêu lần cũng được; bảng người chơi chỉ được **thêm** bảng / cột /
chỉ mục, không bị xoá hay sửa dòng. Phải chạy **trước** khi jar từ P1 trở đi khởi động, không thì máy chủ dừng với dòng
`THIEU DU LIEU SQL QUAN TRONG - DUNG SERVER`.

## Sinh lại

`htbb_day_du.sql` là **tệp sinh** — đừng sửa tay. Nguồn nằm ở [`du_lieu/sql_nguon/`](../du_lieu/sql_nguon/README.md)
(seed, migration, tệp `pokemon_*.sql` sinh từ `du_lieu/*.csv`). Sửa nguồn / chạy lại bộ sinh, rồi:

```sh
MYSQL="mysql -uroot -h127.0.0.1 -P<cổng MariaDB TRỐNG>" MYSQLDUMP="mysqldump -uroot -h127.0.0.1 -P<cổng>" \
  sh tools/pokemon/xuat_sql_day_du.sh          # XOÁ và dựng lại account + htbb_db trên máy đó - không trỏ vào CSDL thật
python3 -I tools/pokemon/sinh_nang_cap_csdl.py  # sinh lại vps/nang_cap_csdl.sql cho CSDL đang chạy
```

`server/test/KiemDuLieuSql` đòi bảng thiết kế trong tệp sinh, `htbb_day_du.sql` và `vps/nang_cap_csdl.sql` khớp từng dòng.
