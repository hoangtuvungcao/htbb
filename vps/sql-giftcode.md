# Câu SQL để kiểm tra / quản lý giftcode

Chạy từ Windows: `vps\win\vps-sql.bat "<câu SQL>"`
Hoặc trên VPS: `mysql -u root -t -e "<câu SQL>"`

## Ba bảng liên quan

| Bảng | Là gì |
|---|---|
| `giftcode` | mã dùng chung — người chơi gõ vào ô giftcode |
| `giftcode_log` | ai đã nhận mã nào; đây là thứ chặn nhận lại lần hai |
| `ngude_giftcode` | mã riêng cho từng nhân vật (quà đăng ký / tri ân), có cột `charname` |

## Xem

Tất cả mã và số lượt còn lại:

```sql
SELECT id, giftcode, xu, luong, luongLock, limit_use, type, item FROM kpah_db.giftcode ORDER BY id;
```

Chỉ những mã còn dùng được:

```sql
SELECT giftcode, limit_use, xu, luong, luongLock, item FROM kpah_db.giftcode WHERE type = 0 AND limit_use <> 0 ORDER BY id;
```

30 lần nhận mã gần nhất (ai nhận gì):

```sql
SELECT id, giftcode, player, xu, luong, luongK, item FROM kpah_db.giftcode_log ORDER BY id DESC LIMIT 30;
```

Một nhân vật đã nhận những mã nào:

```sql
SELECT giftcode, xu, luong, luongK FROM kpah_db.giftcode_log WHERE player = 'admin';
```

Một mã đã có bao nhiêu người nhận:

```sql
SELECT giftcode, COUNT(*) AS so_nguoi_nhan FROM kpah_db.giftcode_log WHERE giftcode = 'oke212' GROUP BY giftcode;
```

Thống kê tổng: mã nào được dùng nhiều nhất:

```sql
SELECT giftcode, COUNT(*) AS lan_nhan FROM kpah_db.giftcode_log GROUP BY giftcode ORDER BY lan_nhan DESC LIMIT 20;
```

## Câu quan trọng nhất — vì sao một người không nhận được mã

Mô phỏng đúng logic server kiểm tra (đổi `oke212` và `admin` theo nhu cầu):

```sql
SELECT g.giftcode, g.type, g.limit_use, (SELECT COUNT(*) FROM kpah_db.giftcode_log l WHERE l.giftcode = g.giftcode AND l.player = 'admin') AS da_nhan FROM kpah_db.giftcode g WHERE g.giftcode = 'oke212';
```

Nhận được khi **cả ba** đúng: `type = 0`, `limit_use <> 0`, `da_nhan = 0`.
Không ra dòng nào = mã không tồn tại (sai chính tả, hoặc mã nằm ở `ngude_giftcode`).

## Sửa

Nạp thêm lượt cho một mã:

```sql
UPDATE kpah_db.giftcode SET limit_use = 100 WHERE giftcode = 'oke212';
```

Cho một nhân vật nhận lại mã (xoá dấu vết đã nhận):

```sql
DELETE FROM kpah_db.giftcode_log WHERE giftcode = 'oke212' AND player = 'admin';
```

Tạo mã mới (`id` là AUTO_INCREMENT nên không cần điền):

```sql
INSERT INTO kpah_db.giftcode (giftcode, xu, luong, luongLock, item, expire, limit_use, type) VALUES ('tanthu2026', 1000000, 500, 0, 'SKIN:2', 0, 100, 0);
```

Tắt một mã ngay:

```sql
UPDATE kpah_db.giftcode SET limit_use = 0 WHERE giftcode = 'tanthu2026';
```

Xoá hẳn một mã:

```sql
DELETE FROM kpah_db.giftcode WHERE giftcode = 'tanthu2026';
```

## Bốn điều về cách server xử lý mã

**1. `type` phải bằng 0.** Câu truy vấn của server là
`select * from giftcode where giftcode='...' and type = 0 limit 1`
([Database.java:2314](../src/data/Database.java#L2314)). Đặt `type` khác 0 là mã vô hình
với người chơi.

**2. `limit_use` là số lượt còn lại, không phải số lượt đã dùng.** Mỗi lần nhận:
`UPDATE giftcode SET limit_use = limit_use - 1`. Đến `0` thì server báo *"Mã Quà Tặng đã
hết lượt sử dụng"*. Muốn **không giới hạn** thì đặt số âm, ví dụ `-1`: điều kiện chặn chỉ
so `limit == 0` nên số âm luôn qua và cứ giảm tiếp (-2, -3...).

**3. Chặn nhận lại tính theo TÊN NHÂN VẬT, không theo tài khoản.** Server kiểm
`select * from giftcode_log where giftcode='X' AND player='<charname>'`. Một tài khoản có
3 nhân vật thì nhận được 3 lần cùng một mã.

**4. Cột `expire` KHÔNG có tác dụng.** Bảng có cột đó nhưng code chưa bao giờ đọc — nó chỉ
lấy `xu`, `luong`, `luongLock`, `item`, `giftcode`, `limit_use`. Muốn hết hạn thì tự
`UPDATE limit_use = 0` khi tới ngày.

## Cú pháp cột `item`

Nhiều phần thưởng ngăn nhau bằng dấu phẩy, mỗi phần thưởng là `LOẠI:tham_số...`

| Loại | Ý nghĩa | Ví dụ |
|---|---|---|
| `SKIN:<0-4>` | skin thú nuôi | `SKIN:2` = Tiên Lộc |
| `DB:<id>:<sl>:<time>` | vật phẩm theo id template | `DB:1234:1:0` |
| `PET:<id>:<sl>:<time>` | thú nuôi | `PET:5:1:0` |
| `PETV:<id>` | thú nuôi vĩnh viễn | `PETV:5` |
| `DH:<id>:<sl>:<time>` | danh hiệu | `DH:8767:1:0` |
| `EGG:<id>:<sl>` | trứng phượng hoàng | `EGG:3:1` |
| `TK:<id>::<time>` | thần khí | `TK:12::0` |
| `GEM`, `PP`, `PPR`, `PPV`, `AOTT`, `MATNA`, `VKTT`... | ngọc / phi phong / áo / mặt nạ / vũ khí | xem [Char.java:8764](../src/real/Char.java#L8764) |

Mã có sẵn trong bản dump, dùng làm mẫu:

```
oke212   -> 1000 lượng khoá + 12 loại item     (limit_use 2154)
400ksz   -> 2 phi phong may mắn                (limit_use 99937)
123      -> DN:500000:500000                   (limit_use 5)
```

Thêm skin thú nuôi vào một mã đang có:

```sql
UPDATE kpah_db.giftcode SET item = CONCAT(item, ',SKIN:2') WHERE giftcode = 'oke212';
```

## Mã riêng theo nhân vật (`ngude_giftcode`)

```sql
SELECT id, code, type, charname, userId, status, level, xu, luong, item FROM kpah_db.ngude_giftcode ORDER BY id DESC LIMIT 20;
```

Khác `giftcode` ở chỗ: mỗi dòng gắn với **một nhân vật cụ thể** (`charname`), có yêu cầu
`level` tối thiểu và cột `status` (đã nhận hay chưa). Dùng cho quà đăng ký / tri ân — trao
tay cho từng người.
