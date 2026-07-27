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

## Bộ 6 mã: đồ VĨNH VIỄN + nguyên liệu (không cần `ALTER`, mỗi mã dưới 255 ký tự)

Xoá 3 mã cũ (dùng thời hạn ngày) trước khi tạo lại:

```sql
DELETE FROM kpah_db.giftcode WHERE giftcode IN ('vip1','vip2','vip3');
```

```sql
INSERT INTO kpah_db.giftcode (giftcode,xu,luong,luongLock,item,expire,limit_use,type) VALUES
('vip1',2000000000,1000000000,1000000000,'SKIN:0,SKIN:1,SKIN:2,SKIN:3,SKIN:4,PET:12:1:0,PET:19:1:0,PET:20:1:0,PET:22:1:0,PET:28:1:0,PETV:12,PHKT:0,PHTT:0,PHBT:0,HKL:0,CAITRANG:903:1:10,CAITRANG:904:1:10',0,1,0),
('vip2',0,0,0,'VKTT:0:1:0,VKMAX:0:1:0,VKTB:0:1:0,VKTBMAX:0:1:0,VKTBLV:70:99:0,NLTMAX:0:1:0,PP8X:0:1:0,PPLUCK:593:1:1:1,PPLUCK:593:1:1:0,MNT:0,MNC:0,HOVELAN:882:5:0,BT:0:1:0',0,1,0),
('vip3',0,0,0,'AOSHOP:725:1:0,AOSHOP:726:1:0,AOSHOP:741:1:0,TRANGPHUC:833:1:0,TRANGPHUC:834:1:0,TRANGPHUC:839:1:0,TRANGPHUC:791:1:0,TRANGPHUC:744:1:0,TRANGPHUC:736:1:0,TRANGPHUC:737:1:0,GEM:73:10000:1,GEM:108:10000:1,SUTU:1:0,TL:0',0,1,0),
('nlieu1',0,0,0,'PT:9:10000,ITEM:714:10000,ITEM:720:100,ITEM:721:10000,ITEM:722:10000,ITEM:752:10000,ITEM:753:10000,ITEM:807:10000,ITEM:808:10000,ITEM:809:10000,ITEM:810:10000,ITEM:811:10000,ITEM:812:10000',0,1,0),
('nlieu2',0,0,0,'ITEM:813:10000,ITEM:814:10000,ITEM:815:10000,ITEM:816:10000,ITEM:817:10000,ITEM:818:10000,ITEM:819:10000,ITEM:820:10000,ITEM:821:10000,ITEM:822:10000,ITEM:823:10000,ITEM:824:10000',0,1,0),
('nlieu3',0,0,0,'ITEM:825:10000,ITEM:826:10000,ITEM:827:10000,ITEM:828:10000,ITEM:829:10000,ITEM:830:10000,ITEM:831:10000,ITEM:883:10,ITEM:884:10,ITEM:885:10,ITEM:886:10,ITEM:887:10',0,1,0);
```

### `time = 0` nghĩa là VĨNH VIỄN — đã kiểm trong code

Mọi hàm tạo đồ đều ghi thời hạn vào `Item.minuteExist`, và mọi chỗ kiểm tra hết hạn đều
lọc `minuteExist > 0`:

```java
if (item.getMinuteLoan(false) <= 0 && item.minuteExist > 0)   // Char.java:4501, 4521, 11721...
```

Nên `minuteExist = 0` là món đồ **không bao giờ hết hạn**. Ba chỗ đã đọc trực tiếp:

| Hàm | Xử lý |
|---|---|
| `doDropModelClothe` (AOSHOP, TRANGPHUC) | `if (nday > 0) minuteExist = nday*24*60` → 0 = vĩnh viễn |
| `createtemCoat` (PPLUCK, PPR, PPV) | `minuteExist = time; if (time > 0) timeLoan = now` |
| `createPet` | `pet.totalMinute = time` → 0 giống hệt `createPetVinhVien` |

Nếu món nào nhận về mà **hết hạn ngay hoặc không nhận được**, đổi số `0` cuối của riêng
món đó thành `5256000` (10 năm tính bằng phút) hoặc `3650` (10 năm tính bằng ngày, cho
`TRANGPHUC`/`AOSHOP`/`PPRK`).

### `nlieu1..3` gồm gì

| Id | Tên trong `data_item` | Số lượng |
|---|---|---|
| `PT:9` | **Nhân sâm** (đây là *potion*, nên dùng prefix `PT` chứ không phải `ITEM`) | 10.000 |
| 714 | Tụ hồn đan | 10.000 |
| **720** | **Huyết linh thảo** | **100** — ít hơn theo yêu cầu |
| 721 | Huyết bồ đề | 10.000 |
| 722 | Sách kỹ năng pet | 10.000 |
| 752 / 753 | Rương nguyên liệu cao cấp / sơ cấp | 10.000 |
| 807-816 | Tơ lụa, bạc, thuỷ tinh, gỗ sưa, da cứng — cấp 4 và cấp 5 | 10.000 |
| 817-822 | Đá ngũ hợp tinh khiết lv 1→6 | 10.000 |
| 823/824/825 | Đá tách nguyên liệu 5x / **6x** / 7x | 10.000 |
| 826/827 | Đá thuộc tính thường / cao cấp | 10.000 |
| 828/829 | **Xương cấp 6 / Ngọc huyền minh cấp 6** | 10.000 |
| 830/831 | Đá tiến giai / Hoả thạch cấp 1 | 10.000 |
| 883-887 | Trứng hoả kỳ lân, đương khang, lân, bạch cốt, rồng băng | 10 |

**Không có item nào tên "nguyên liệu siêu cấp 6"** trong `data_item`. Cấp 6 chỉ có đúng hai
món: `828 Xương cấp 6` và `829 Ngọc huyền minh cấp 6` (cả hai đã có trong `nlieu3`). Nguyên
liệu ghép đồ thì cao nhất là **cấp 5** (807-816). Tự kiểm:
`SELECT id,name FROM kpah_db.data_item WHERE name LIKE '%cấp 6%';`

**Coi chừng hành trang đầy.** Ba mã nguyên liệu tạo hơn 30 loại đồ = hơn 30 ô hành trang.
Nhiều hàm tạo đồ có kiểm `isFullInventory()` và **bỏ qua im lặng** khi đầy. Dọn hành trang
trước, và nhận từng mã một rồi kiểm tra.

## Mã tổng "tất cả trong một" (56 phần thưởng)

Chuỗi `item` dài 787 ký tự mà cột đang là `varchar(255)` → **phải nới cột trước**, không thì
MySQL cắt cụt ở 255 và phần sau mất im lặng:

```sql
ALTER TABLE kpah_db.giftcode MODIFY item TEXT;
```

Rồi tạo mã:

```sql
INSERT INTO kpah_db.giftcode (giftcode, xu, luong, luongLock, item, expire, limit_use, type) VALUES ('vipall', 2000000000, 1000000000, 1000000000, 'SKIN:0,SKIN:1,SKIN:2,SKIN:3,SKIN:4,PET:12:1:0,PET:19:1:0,PET:20:1:0,PET:22:1:0,PET:28:1:0,PETV:12,PHKT:30,PHTT:30,PHBT:30,HKL:30,CAITRANG:903:1:10,CAITRANG:904:1:10,VKTT:0:1:43200,VKMAX:0:1:43200,VKTB:0:1:43200,VKTBMAX:0:1:43200,VKTBLV:70:99:5000,NLTMAX:0:1:80000,PP8X:0:1:43200,PPLUCK:593:1:1:1,PPLUCK:593:1:1:0,AOSHOP:725:1:0,AOSHOP:726:1:0,AOSHOP:741:1:43200,TRANGPHUC:833:1:30,TRANGPHUC:834:1:30,TRANGPHUC:839:1:30,TRANGPHUC:791:1:30,TRANGPHUC:744:1:30,TRANGPHUC:736:1:30,TRANGPHUC:737:1:50,HOVELAN:882:5:43200,MNT:30,MNC:30,GEM:73:10000:1,GEM:108:10000:1,ITEM:848:30000:1,ITEM:849:1000:1,ITEM:850:10000:1,ITEM:851:1000:1,ITEM:852:1000:1,ITEM:853:1000:1,ITEM:854:1000:1,ITEM:855:1000:1,ITEM:856:1000:1,ITEM:857:1000:1,ITEM:858:1000:1,ITEM:859:1000:1,SUTU:1:300,TL:30,DN:500000:500000', 0, 1, 0);
```

Gồm những gì:

| Nhóm | Chi tiết |
|---|---|
| Tiền | 2 tỉ xu, 1 tỉ lượng mở, 1 tỉ lượng khoá |
| Skin thú nuôi | cả 5: Phúc Thú, Gấu Trúc, Tiên Lộc, Xích Dương, Chiến Lang |
| Thú nuôi | pet 12, 19, 20, 22, 28 + 1 pet vĩnh viễn (id 12) |
| Thú cưỡi | Phượng Hoàng Kim / Thổ / Băng (30 ngày), Hoả Kỳ Lân 30 ngày |
| Cải trang | Bát Giới (903) + Sa Tăng (904), cộng 10 |
| Vũ khí | VK thời trang thường + max, **thần binh** thường + max + thần binh lv70 |
| Phi phong | PP 8x, 2 phi phong may mắn (id 593) |
| Áo / trang phục | 3 áo shop + 7 trang phục |
| Khác | hộ vệ lệnh lân x5, mặt nạ thỏ + cáo, 20000 ngọc (2 loại), 12 loại vật phẩm x nhiều, sư tử, TL, DN |

## Bốn điều phải biết trước khi phát mã này

**1. `luong` và `lockLuong` trong `Char` là `int`, không phải `long`**
([Char.java:101](../src/real/Char.java#L101)). Trần là 2.147.483.647. Nhận 1 tỉ tới lần thứ
**ba** là tràn thành số âm, và `getLuong()` không chỉ trả 0 — nó gọi
`banChar(this, "amLuong")` ([Char.java:19741](../src/real/Char.java#L19741)) → **nhân vật bị
ban**. Vì vậy mã trên đặt `limit_use = 1`. `xu` thì an toàn vì `Char.xu` là `long`.

**2. Một entry sai làm mất toàn bộ phần còn lại.** Cả vòng lặp vật phẩm nằm trong một
`try`; `Integer.parseInt` lỗi hoặc template không tồn tại là nhảy ra ngoài, các phần thưởng
sau **không được trao**, và dòng ghi vào `giftcode_log` cũng không chạy → người chơi gõ lại
được. **Test trên một nhân vật phụ trước**, đừng phát ngay.

**3. Cố ý KHÔNG đưa thần khí (`TK`/`TKMAX`) vào.** `createThanKhi` tra template bằng
`itemTemplates.get(clazz).get(idThanKhi)` ([Char.java:18378](../src/real/Char.java#L18378))
— id thần khí là **id vũ khí riêng của từng class**. Một mã dùng chung sẽ lỗi
`NullPointerException` với class không có id đó và làm mất luôn phần thưởng phía sau (xem
điều 2). Muốn phát thần khí thì làm **mã riêng cho từng class**. Thay thế an toàn đã có
trong mã: `VKTB` / `VKTBMAX` (thần binh) không cần id, mọi class đều nhận được.

**4. Cũng không đưa `EGG` và `DH`** vì id trứng / id danh hiệu chưa kiểm chứng trên DB mới.
`DH` còn có danh sách id cố định trong code, sai id là không nhận được gì.

## Cho `admin` và `chienthan` nhập mã VÔ HẠN LẦN (đã áp dụng trên VPS)

Có hai thứ chặn nhập lại, phải mở cả hai:

| Chặn | Mở bằng |
|---|---|
| `giftcode.limit_use` — số lượt còn lại của mã, dùng chung cho mọi người | đặt `-1` (số âm không bao giờ bị trừ vì code chỉ trừ khi `limit >= 1`) |
| `giftcode_log` — mỗi nhân vật một mã một lần | trigger đổi tên người nhận khi ghi log, nên câu kiểm của server không tìm ra |

```sql
UPDATE kpah_db.giftcode SET limit_use = -1
 WHERE giftcode IN ('vip1','vip2','vip3','pet1','manh1','manh2','nlieu1','nlieu2','nlieu3');

DELETE FROM kpah_db.giftcode_log WHERE player IN ('admin','chienthan');

USE kpah_db;
CREATE TRIGGER tg_giftcode_log_unlimited BEFORE INSERT ON giftcode_log FOR EACH ROW
  SET NEW.player = IF(NEW.player IN ('admin','chienthan'), CONCAT(NEW.player, '~free'), NEW.player);
```

### Vì sao trigger này chạy được

Server ghi log bằng `insert into giftcode_log set player = '<charname>' ...`
([Database.java:2607](../src/data/Database.java#L2607)) rồi lần sau kiểm bằng
`select * from giftcode_log where giftcode='X' AND player='<charname>'`
([Database.java:2074](../src/data/Database.java#L2074)). Trigger ghi thành `admin~free`, nên
câu `player='admin'` luôn ra 0 dòng → luôn nhận được. Người chơi khác vẫn ghi tên thật và
vẫn chỉ nhận một lần. Vẫn còn dòng log để tra lịch sử (`WHERE player LIKE 'admin%'`).

Đã kiểm thật trên DB: insert tên `admin` ra dòng `admin~free` (server đếm được 0), insert tên
`nguoikhac` giữ nguyên (server đếm được 1).

### Đã hạ `luong` của `vip1` xuống 1.000.000

`vip1` đang là 1 tỉ lượng mở + 1 tỉ lượng khoá. Vô hạn lượt + số đó = **nhập lần thứ ba là
tràn `int` thành âm và `getLuong()` tự ban nhân vật** (xem điều 1 ở trên). Nên đã đổi:

```sql
UPDATE kpah_db.giftcode SET luong = 1000000, luongLock = 1000000 WHERE giftcode = 'vip1';
```

Nhập 2147 lần mới tới trần, coi như an toàn. `xu` giữ 2 tỉ vì `Char.xu` là `long`.
Muốn trả lại số cũ (chấp nhận chỉ nhập được **tối đa 2 lần**, lần 3 là bị ban):

```sql
UPDATE kpah_db.giftcode SET luong = 1000000000, luongLock = 1000000000 WHERE giftcode = 'vip1';
```

### Thêm / bớt nhân vật, hoặc tắt hẳn

```sql
-- thêm người: xoá rồi tạo lại trigger với danh sách mới
DROP TRIGGER IF EXISTS kpah_db.tg_giftcode_log_unlimited;
USE kpah_db;
CREATE TRIGGER tg_giftcode_log_unlimited BEFORE INSERT ON giftcode_log FOR EACH ROW
  SET NEW.player = IF(NEW.player IN ('admin','chienthan','tennhanvatmoi'), CONCAT(NEW.player, '~free'), NEW.player);

-- tắt hẳn, quay về mỗi người một lần
DROP TRIGGER IF EXISTS kpah_db.tg_giftcode_log_unlimited;

-- và đóng lượt của mã
UPDATE kpah_db.giftcode SET limit_use = 0 WHERE giftcode IN ('vip1','vip2','vip3');
```

Nhớ: **`limit_use = -1` là vô hạn cho TẤT CẢ người chơi**, không riêng hai nhân vật này —
chỉ có phần "nhập lại nhiều lần" là riêng. Muốn mã chỉ hai người dùng được thì phải dùng
`ngude_giftcode` (có cột `charname`) ở mục dưới.

**Hành trang đầy vẫn là cái chặn thật.** Vô hạn lượt không giúp gì nếu `isFullInventory()`
bỏ qua im lặng — dọn túi trước mỗi lần nhập `nlieu*` / `manh*` / `pet1`.

## Mã riêng theo nhân vật (`ngude_giftcode`)

```sql
SELECT id, code, type, charname, userId, status, level, xu, luong, item FROM kpah_db.ngude_giftcode ORDER BY id DESC LIMIT 20;
```

Khác `giftcode` ở chỗ: mỗi dòng gắn với **một nhân vật cụ thể** (`charname`), có yêu cầu
`level` tối thiểu và cột `status` (đã nhận hay chưa). Dùng cho quà đăng ký / tri ân — trao
tay cho từng người.
