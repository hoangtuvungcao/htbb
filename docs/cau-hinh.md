# Cấu hình

Tài liệu tra cứu mọi khoá cấu hình của máy chủ game và website. Có hai nơi chứa cấu hình:

| Nơi | Chứa gì | Sửa ở đâu |
|---|---|---|
| **Bảng `cau_hinh`** (CSDL `htbb_db`) | mọi khoá trong danh mục cấu hình trung tâm: tỉ lệ rơi, EXP, sự kiện, chống spam, công tắc tính năng… | trang web **/admin → Cấu hình hệ thống** (có lịch sử, hoàn tác) |
| **`server.ini`** (cạnh `htbb-server.jar`) | khoá khởi động: cổng, kết nối CSDL, đường dẫn log, cờ cũ bắt buộc; đồng thời là giá trị khởi tạo / dự phòng cho khoá trong danh mục | VPS: sửa [`vps/server.ini.tmpl`](../vps/server.ini.tmpl) rồi commit; máy nhà: sửa tay |

Thứ tự ưu tiên: **bảng `cau_hinh` > `server.ini` > mặc định trong danh mục.**

Vận hành hằng ngày (đổi cấu hình từ web, khởi động lại an toàn): [van-hanh.md](van-hanh.md). Kiến trúc của
dịch vụ cấu hình trung tâm: [kien-truc.md](kien-truc.md). Biến môi trường của website: `web/.env.example` và
[web/README.md](../web/README.md).

## 1. Cấu hình trung tâm (danh mục)

| Phần | Tệp |
|---|---|
| Danh mục khoá (nguồn duy nhất) | [`tools/cauhinh/danh_muc.json`](../tools/cauhinh/danh_muc.json) |
| Bộ sinh | [`tools/cauhinh/sinh_cau_hinh.py`](../tools/cauhinh/sinh_cau_hinh.py) → `server/src/com/htbb/cauhinh/DanhMucSinh.java`, `sql/pokemon_cau_hinh_trung_tam.sql`, `web/shared/utils/cauHinhTrungTam.gen.ts` và bảng ở mục 4 của tài liệu này |
| Dịch vụ máy chủ | `server/src/com/htbb/cauhinh/CauHinhTrungTam.java` (+ `TinhCauHinh`, `KiemGiaTri`, `ApDungMacDinh`, `CongTac`) |
| Trang web | `/admin/cau-hinh-he-thong`, API `/api/admin/cau-hinh-he-thong/**` |
| Bộ ví dụ kiểm chung Java ↔ web | [`tools/cauhinh/vi_du_kiem.tsv`](../tools/cauhinh/vi_du_kiem.tsv) |

Thêm / sửa khoá: chỉnh `danh_muc.json` → `python3 tools/cauhinh/sinh_cau_hinh.py` ở gốc repo (kiểm lệch:
thêm `--kiem`, mã thoát 1 nếu tệp sinh khác danh mục). Đừng sửa tay các tệp sinh ra, kể cả bảng ở mục 4.

Luật áp dụng:

- **`ngay`** — luồng `CauHinhTrungTam` hỏi `cau_hinh_phien_ban` mỗi 5 giây (đọc lại cả bảng mỗi 60 giây để bắt
  người ghi thẳng bảng), áp dụng qua người nghe và ghi log `[CAU HINH] AP DUNG khoa: cũ -> mới`.
- **`khởi động lại`** (`sv.lau_dai`, `sv.he_sinh_thai`, `sv.nap_tien`, `sv.tran_cap_theo_mua`, `sv.ngay_khai_truong`,
  cờ sự kiện cũ…) — đọc một lần lúc bật máy; giá trị mới hiện ở mục *chờ khởi động lại* trên trang web.
- Giá trị CSDL sai kiểu / khoảng / lựa chọn bị **từ chối**: máy chủ giữ giá trị tốt gần nhất, ghi lý do vào
  `cau_hinh_hieu_luc.loi` và `cau_hinh_ap_dung.loi` (trang hiện đỏ). Giá trị `server.ini` sai luật vẫn được dùng
  (mã đọc riêng của tính năng tự kẹp), chỉ cảnh báo.
- `khoaProd` (`sv.tua_nhanh_ngay`, `sv.testMode`, `sv.testCode`): máy thật chỉ nhận giá trị mặc định. Máy thử thêm
  `sv.cho_phep_thu_nghiem=1` vào `server.ini` (web: chế độ dev hoặc `HTBB_CHO_PHEP_THU_NGHIEM=1`).
- Khoá `bang: env` là biến môi trường của web (bí mật cổng thanh toán…): trang chỉ hiện trạng thái, không sửa được.

### Khoá nạp lại được qua `CauHinh`

Lớp cũ `server/src/server/CauHinh.java` (bảng `setting` trong game, hằng `K_*`) vẫn còn và uỷ quyền cho dịch vụ
trung tâm. Đúng 34 khoá sau đi qua `CauHinh.napLai()` — đổi trong `cau_hinh` hoặc gõ `setting` → `8` là ăn ngay:

```
sv.tileXu   sv.tileLuong   sv.tileItemSuKien   sv.tilePotion   sv.tileNgoc   sv.tileItem   sv.chiaXuRoi
sv.tileExpLv50   sv.tileExpDuoiLv50
sv.tieuNguongMau   sv.tieuHeSoExp   sv.tieuPcExpTheoMau   sv.tieuPermilleCaoCap
sv.doiTieuNguongMauXu   sv.doiTieuNguongMauLuong
sv.gioChiemThanh   sv.phutChiemThanh   sv.ngayChiemThanh
sv.adminAccounts   sv.maxCharPerAccount   sv.choPhepTaoTenGM
sv.website   sv.linkTaiGame   sv.testMode   sv.testCode
sv.worldCupBatDau   sv.worldCupKetThuc   sv.worldCupHanNhanQua
sv.suKienTuKhoiDongLai   sv.suKienGomGiay   sv.suKienBaoTruocGiay
sv.expvantieu   sv.Luckyexp   sv.ExpQua
```

Ghi chú cho vài khoá hay chỉnh:

- **Tỉ lệ rơi** `sv.tile*` là phần trăm so với bản gốc (`100` = như gốc, `0` = tắt). `sv.chiaXuRoi` khác `sv.tileXu`:
  một cái là *bao nhiêu xu mỗi lần rơi* (`10` = như gốc, `30` = còn 1/3), một cái là *bao nhiêu lần rơi*.
- **EXP quái thường** `sv.tileExpLv50` (quái cấp ≥ 50) và `sv.tileExpDuoiLv50` độc lập nhau, không áp cho boss.
- **Chuỗi CSV** `sv.tieu*`, `sv.doiTieu*`: ngưỡng phải tăng dần, đúng số phần tử (5 / 7 / 9); sai thì máy chủ giữ
  giá trị đang chạy.
- **Chiếm Gym** `sv.gioChiemThanh=-1` = lịch chuẩn 21h thứ 3 + thứ 7; `sv.ngayChiemThanh` trống = mọi ngày,
  `yyyy-MM-dd` = đúng ngày đó, `Tue`/`Sat` = theo thứ. Phiên kéo dài 1 tiếng.
- **`sv.adminAccounts`** so với **tên nhân vật**, không phải tên tài khoản. `sv.gmCanCoPInfo=1` đòi thêm
  `pInfo[16]=1` trong `tob_char`.
- **`sv.suKienGomGiay`** (mặc định 120, kẹp 30..3600): đừng hạ thấp — `htbb-game.service` có `StartLimitBurst=5` /
  `StartLimitIntervalSec=300`, khởi động lại quá 5 lần trong 300 giây là unit vào trạng thái `failed`.
  **`sv.suKienBaoTruocGiay`** đừng dưới 10 giây (tin báo cần 15–20 giây để trôi hết trên client).
- **`sv.ExpQua`** đồng thời khoá nút "Tăng tốc" của mọi loại cây (hành vi sẵn có của mã nguồn).
- **World Cup**: bật chính thức ở trang `/admin/sukien`; ba khoá `sv.worldCup*` (`yyyy-MM-dd HH:mm:ss`,
  `batDau < ketThuc <= hanNhanQua`) chỉ là mặc định khi `tob_su_kien` chưa có dòng; để trống = tắt.

## 2. `server.ini`

### 2.1 Trên VPS: sinh từ bản mẫu

`/opt/htbb/server/server.ini` được **sinh ra** mỗi lần deploy, không sửa tay:

```
vps/server.ini.tmpl (trong git) ─┐
                                 ├─→ vps/sinh-server-ini.sh ─→ /opt/htbb/server/server.ini ─→ khởi động lại khi đổi
bí mật (${DB_USER}, ${DB_PASSWORD}…) ┘        (kiểm tra rồi mới cài)
```

- Giá trị `${TEN_BIEN}` lấy từ biến môi trường của bộ sinh (bí mật GitHub gửi qua stdin của SSH), thiếu thì đọc khoá
  cùng tên trong `/etc/htbb/htbb.env` (`DB_USER`, `DB_PASSWORD`, `DB_USER_NAP`, `DB_PASS_NAP`), vẫn thiếu thì giữ giá
  trị đang chạy. `@GIU_NGUYEN@` = giữ nguyên giá trị đang chạy trên VPS.
- Bộ sinh **không bao giờ cài cấu hình hỏng**: kiểm chỗ trống chưa thay, đủ khoá bắt buộc, mật khẩu không còn là chuỗi
  mẫu `DOI_MAT_KHAU_DB`, `db.choPhepMatKhauTrong` phải là `0`, và **thử kết nối CSDL thật**. Sai là giữ nguyên tệp
  đang chạy. Bản cũ lưu ở `/opt/htbb/backups/server-ini/` (10 bản gần nhất).
- Xem trước khác biệt, không cài gì: `sudo /opt/htbb/vps/sinh-server-ini.sh --thu`.
- Khoá ghi trong bản mẫu nhưng cũng có trong danh mục: giá trị trong bảng `cau_hinh` vẫn thắng. Muốn đổi lâu dài
  khoá *chỉ có* trong `server.ini` thì sửa bản mẫu.
- `server.ini` nằm trong `.gitignore` (chứa mật khẩu) — **đừng `git add server.ini`**, đừng dán nội dung tệp cho ai.
  Bản mẫu thì ngược lại: phải ở trong git và không chứa mật khẩu thật.

Hai tệp **không** đi qua bộ sinh: `/opt/htbb/server/loginServer/server.ini` (login server, mẫu
[`vps/loginServer.server.ini.example`](../vps/loginServer.server.ini.example)) và `server/server.ini` của máy dev
(mẫu [`vps/server.ini.linux.example`](../vps/server.ini.linux.example)).

### 2.2 Khoá chỉ có trong `server.ini`

Các khoá này không nằm trong danh mục (con gà – quả trứng: phải có chúng mới kết nối được CSDL), chỉ đọc lúc khởi
động trong `server/src/server/TeamServer.java`. Đổi xong phải `systemctl restart htbb-game`.

| Khoá | Ý nghĩa |
|---|---|
| `sv.port` | cổng game TCP (19129) |
| `limit.ccu` | số người online tối đa — **bắt buộc**, không có mặc định |
| `sv.server` | số máy chủ |
| `sv.portnap` | cổng lệnh nạp nội bộ (khoá vẫn phải có) |
| `sv.iplogin`, `sv.portlogin` | địa chỉ login server (`127.0.0.1`, 8023 — không mở ra internet) |
| `db.host`, `db.name`, `db.user`, `db.password`, `db.maxco` | pool CSDL chính (`host:cổng`) |
| `db.nameNap`, `db.userNap`, `db.pass`, `db.maxNap` | pool nạp |
| `db.host1`, `db.name1`, `db.user1`, `db.password1`, `db.maxco1` | pool phụ |
| `db.thamSoJdbc` | tham số nối vào cuối mọi URL JDBC (xem 2.4) |
| `db.choPhepMatKhauTrong` | `1` = cho `db.password` trống **chỉ khi** `db.host` là máy nội bộ; chỉ dùng ở máy nhà, VPS luôn `0` |
| `sv.pathLog` | thư mục log, **có dấu `/` cuối** (VPS: `/opt/htbb/logs/`) |
| `sv.LG`, `sv.range`, `sv.lang`, `sv.me`, `api.sv`, `sv.createRace`, `sv.CodeTest`, `sv.EnableAvtiveCheck` | khoá gốc của game cũ, giữ nguyên như bản mẫu |
| `db.hour`, `db.minute` | giờ chạy việc định kỳ của CSDL (gốc) |
| `api.url`, `db.run`, `sv.dayrace`, `sv.tablerace`, `sv.ph`, `sv.choi` | khoá gốc đọc thô, thiếu không lỗi |
| `sv.cho_phep_thu_nghiem` | `1` = máy thử nhận các khoá `khoaProd` |
| `sv.thuNghiemThuCuoi` | tên **một** nhân vật để bật log chẩn đoán thú cưỡi / skin; trống = tắt |
| `sv.tranGoiMienIp` | IP không bị đếm ngập gói — chỉ cho máy chạy bot tải, VPS để trống |
| `sv.isSuKienTrungThul2016`, `sv.isSuKienTet2017`, `sv.isSuKienGioTo2016`, `sv.isSuKienNoel2023`, `sv.isSuKienTetduonglich2024` | cờ sự kiện cũ: chỉ là trạng thái ban đầu khi `tob_su_kien` chưa có dòng; bật/tắt thật ở `/admin/sukien` |

Tổng pool `db.maxco` + `db.maxNap` + `db.maxco1` + login server + web phải nhỏ hơn `max_connections` của MariaDB
(VPS: 300), không thì giờ đông người ăn lỗi *Too many connections*.

### 2.3 Cạm bẫy: sai một dòng là bỏ qua mọi dòng bên dưới

Phần đọc cấu hình trong `TeamServer` nằm trong **một khối `try`**. Một khoá bắt buộc thiếu hoặc sai kiểu là ném lỗi
tại đó và **mọi dòng phía sau không được đọc** — nhưng server vẫn khởi động với cấu hình dở dang. Dấu hiệu duy nhất
là dòng sau trong `/opt/htbb/logs/game.out`:

```
Load configuration file error!
```

Đúng 20 khoá đi qua `parseInt` / `parseByte` / `parseShort` và bắt buộc phải có:

```
api.sv        sv.port      sv.server    sv.portnap   sv.portlogin
limit.ccu     sv.LG        sv.range     sv.lang      sv.me
sv.createRace sv.CodeTest  sv.EnableAvtiveCheck      db.hour      db.minute
sv.isSuKienTrungThul2016    sv.isSuKienTet2017       sv.isSuKienGioTo2016
sv.isSuKienNoel2023         sv.isSuKienTetduonglich2024
```

Nhóm `db.*` đọc thô: thiếu không ném lỗi ở đây, chỉ là không kết nối được CSDL — `TeamServer.kiemTraCSDL()` thử kết
nối trước mọi lệnh chạm CSDL, hỏng thì in lý do rồi `System.exit(1)` để systemd báo `failed` đúng sự thật. Mọi khoá
trong danh mục đều có mặc định, xoá dòng vẫn chạy.

`sv.isSuKien83` là **khoá chết**: không được đọc ở đâu, đã bỏ khỏi bản mẫu; sự kiện 8/3 bật ở trang quản trị như mọi
sự kiện khác.

Sau mỗi lần sửa:

```bash
grep -i "configuration file error" /opt/htbb/logs/game.out | tail -n 3   # không có dòng mới là đã đọc trọn tệp
systemctl status htbb-game --no-pager
ss -lntp | grep 19129
```

Trong game gõ `setting` → `6` để xem giá trị server **đang thực sự dùng**.

### 2.4 `db.thamSoJdbc` và NovaDB

Chuỗi dạng `a=1&b=2` (không có `?`) được nối vào cuối mọi URL JDBC của game server (bốn pool và phép thử kết nối lúc
khởi động). Trống = URL như cũ (MariaDB trên VPS). Chạy trên NovaDB (cổng MySQL của NovaDB, lưu trên SQLite):

```ini
db.host=127.0.0.1:3318
db.thamSoJdbc=useSSL=false
```

`useSSL=false` nên có: cổng MySQL của NovaDB không có TLS, thiếu thì Connector/J 5.1 in cảnh báo SSL mỗi lần mở kết
nối. Mọi câu SQL trong `server/src` chạy được trên cả MariaDB lẫn NovaDB (`CREATE TABLE … LIKE`, `UPDATE … JOIN`,
`TIMESTAMPDIFF` đã viết lại). Khác biệt còn lại do NovaDB: `NOW()` trả giờ UTC; `=` phân biệt hoa/thường (server lưu
tên nhân vật chữ thường, kiểm trùng bằng `LOWER()`); `UNIQUE KEY` trong `CREATE TABLE` không xoá được; chia số nguyên
cho ra số nguyên. Chạy server trên NovaDB: `sh tools/pokemon/chay_tren_novadb.sh`.

## 3. Nhật ký, chống spam, chống DoS

Các khoá dưới đây có mặc định trong mã nguồn (thiếu dòng không lỗi). Trong `vps/server.ini.tmpl` chúng nằm dạng chú
thích `#khoa=giá_trị_mặc_định`. Khoá nào có trong danh mục (mục 4) thì sửa được ở trang web. Mã nguồn:
`server/src/com/htbb/log/Nhat.java`, `server/src/com/htbb/chongspam/ChongSpam.java`,
`server/src/com/htbb/chongspam/ThongBaoGop.java`, `server/src/io/BoDemGoiTin.java`.

### 3.1 Kết nối và gói tin

| Khoá | Mặc định | Nghĩa |
|---|---|---|
| `sv.socketTimeout` | `180` | giây, ngắt kết nối im lặng quá lâu |
| `sv.maxConnPerIp` | `50` | số kết nối cùng lúc tối đa từ một IP |
| `sv.connRateMax` / `sv.connRateWindow` | `60` / `30` | số kết nối mới tối đa từ một IP trong một cửa sổ (giây) |
| `sv.dosBanSec` | `300` | cấm tạm IP flood bao lâu (giây) |
| `sv.reloginWaitSec` | `0` | phải chờ bao lâu mới vào lại sau khi thoát |
| `sv.proxyTinCay` | `127.0.0.1,::1` | IP / CIDR của WAF, Nova agent: miễn giới hạn theo IP (giới hạn theo tài khoản / kết nối vẫn giữ). Thêm dải WAF ở trang web, **không ghi IP WAF vào git** |
| `sv.dangNhapIpNguong` / `sv.dangNhapIpChoMs` | `10` / `3000` | quá ngần ấy lần đăng nhập/giây từ một IP thì bắt chờ ngần ấy ms và ghi nghi vấn; `0` = tắt |
| `sv.tranGoiThuong` | `300` | gói **thường** tối đa của một kết nối trong 5 giây |
| `sv.tranGoiTaiNguyen` | `600` | gói **tải tài nguyên** (`-47`, `-48`, `-49`, `-51`) trong 5 giây, đếm riêng |
| `sv.tranGoiTraCuu` | `600` | gói **tra cứu** (lệnh `7` hỏi máu / vị trí quái) trong 5 giây, đếm riêng |
| `sv.tranGoiMienIp` | trống | IP không bị đếm ngập gói (chỉ máy chạy bot tải) |

Vượt trần gói là **cắt kết nối** (`NGAP GOI TIN` trong log). `0` hoặc số âm = tắt phép đếm đó. Qua loopback (máy nhà)
không có độ trễ nên client vừa xoá cache có thể chạm trần một cách lương thiện: máy nhà nên đặt
`sv.tranGoiTaiNguyen=0` trong `server.ini` **của máy đó**. Trên VPS đừng hạ và đừng đặt 0 (mở cửa cho spam `-51`).

### 3.2 Nhật ký `sv.log.*`

Mọi `System.out`/`System.err` (kể cả stack trace và dòng in từ các lớp lõi cũ phục hồi ở P3) đi qua một bộ chặn: phân loại
theo tiền tố `[LOẠI]`, lọc theo mức, gộp dòng lặp, ghi bất đồng bộ. Dòng có dạng
`10-18 19:02:11.532 W [NGAP_GOI] ...` (E/W/I/D).

| Khoá | Mặc định | Nghĩa |
|---|---|---|
| `sv.log.muc` | `THONG_TIN` | mức tối thiểu ra stdout (`LOI` / `CANH_BAO` / `THONG_TIN` / `GO_LOI`; nhận cả ERROR/WARN/INFO/DEBUG) |
| `sv.log.mucTep` | = `sv.log.muc` | mức tối thiểu ghi tệp |
| `sv.log.tat` | `GOI3,GOI5,GOI26,VKTT,SQL,LUU_CHAR` | loại bị tắt hẳn (chữ trong `[..]`, dấu cách thành `_`); 10 phút một dòng đếm số đã bỏ |
| `sv.log.kiemToan` | `ADMIN,GM,NAP,NAP_TIEN,GIFTCODE,CAM,BAN,KINH_TE,KINHTE,CHONG_SPAM` | loại kiểm toán: không tắt được, luôn ghi vào `kiem_toan-*.log` |
| `sv.log.lapToiDa` | `5` | số dòng cùng mẫu được ghi mỗi cửa sổ, quá thì gộp thành một dòng `gop dong lap xN` |
| `sv.log.loaiToiDa` | `120` | trần chung mỗi loại mỗi cửa sổ (dòng `LOI` không bị chặn) |
| `sv.log.cuaSoGiay` | `60` | độ dài cửa sổ gộp |
| `sv.log.cuaSoStackGiay` | `600` | stack trace của cùng một lỗi chỉ in đầy đủ một lần trong khoảng này |
| `sv.log.hangDoi` | `50000` | sức chứa hàng đợi ghi; đầy thì bỏ dòng thường |
| `sv.log.tep` | `1` | `0` = không ghi `may_chu-*.log` (tệp kiểm toán vẫn ghi) |
| `sv.log.thuMuc` | `<sv.pathLog>/nhat_ky/` | thư mục tệp log; đừng đặt trùng thư mục mà logrotate xoay `*.log` |
| `sv.log.tepToiDaMB` | `100` | dung lượng mỗi phần tệp |
| `sv.log.soTepMoiNgay` | `5` | số phần tối đa mỗi ngày; hết thì chỉ còn ghi dòng `LOI` |
| `sv.log.giuNgay` | `14` | xoá `may_chu-*` cũ hơn chừng này ngày |
| `sv.log.giuNgayKiemToan` | `90` | như trên cho `kiem_toan-*` |

Nhật ký kiểm toán cũ giữ nguyên chỗ: `AdminChat.txt`, `AdminChat_lenh47.txt`, `errorlog.txt`, bảng log trong CSDL.
Chuỗi do client gửi lên được làm sạch (`Nhat.sach()`) nên không giả mạo được dòng log. Chỉ số trong
`trang_thai.json`: `nhatKy.ghi / bo / loi / mat`, `chatChan`, `thaoTacChan`.

### 3.3 Chống spam chat `sv.spam.*`

Nhịp dạng `số/giây` (cửa sổ trượt). Admin được miễn. Kiểm trên nội dung gốc, trước bộ lọc từ xấu.

| Khoá | Mặc định | Nghĩa |
|---|---|---|
| `sv.spam.bat` | `1` | `0` = tắt toàn bộ chống spam chat + thao tác |
| `sv.spam.chat.gan` | `5/10` | chat thường (lệnh 27) |
| `sv.spam.chat.rieng` | `6/10` | tin riêng (lệnh -5) |
| `sv.spam.chat.theGioi` | `1/30` | loa thế giới mỗi người; bị chặn thì không trừ loa |
| `sv.spam.theGioiToanServer` | `12/60` | trần chung kênh thế giới cho cả máy chủ |
| `sv.spam.chat.doi` | `8/10` | kênh tổ đội |
| `sv.spam.chat.bang` | `8/10` | chat bang và bảng tin bang |
| `sv.spam.trungLapGiay` | `30` | cùng nội dung (đã chuẩn hoá) trong chừng này giây = trùng lặp |
| `sv.spam.chanQuangCao` | `1` | chặn link / quảng cáo / số điện thoại / địa chỉ IP |
| `sv.spam.quangCaoThem` | trống | thêm từ cấm quảng cáo, cách nhau dấu phẩy |
| `sv.spam.viPhamCam` | `4` | đủ điểm vi phạm thì tự cấm chat (nhanh / trùng = 1, quảng cáo = 2) |
| `sv.spam.viPhamQuenGiay` | `600` | điểm vi phạm về 0 sau chừng này giây sạch |
| `sv.spam.camGiay` | `60,300,1800,7200` | thời gian cấm tăng dần theo bậc |
| `sv.spam.bacQuenGio` | `24` | bậc cấm hạ về 0 sau chừng này giờ |
| `sv.spam.baoCachGiay` | `5` | báo người bị chặn tối đa 1 lần / chừng này giây |

Mỗi lần tự cấm ghi một dòng kiểm toán `CHONG_SPAM`.

### 3.4 Chống spam thao tác `sv.spam.hd.*`

Vượt nhịp thì bỏ thao tác (không cắt kết nối) và báo "Thao tác quá nhanh".

| Khoá | Mặc định | Thao tác |
|---|---|---|
| `sv.spam.hd.moiDoi` | `4/10` | mời tổ đội |
| `sv.spam.hd.moiBang` | `4/10` | mời vào bang |
| `sv.spam.hd.ketBan` | `4/10` | lời mời kết bạn |
| `sv.spam.hd.moiGiaoDich` | `4/10` | mời giao dịch |
| `sv.spam.hd.npc` | `25/5` | menu NPC |
| `sv.spam.hd.raoBan` | `6/30` | rao bán chợ |
| `sv.spam.hd.muaBan` | `25/5` | mua / bán cửa hàng |
| `sv.spam.hd.dangKy` | `5/60` | đăng ký trong game (hạn mức IP 3/giờ, 10/ngày vẫn áp) |

### 3.5 Thông báo toàn máy chủ `sv.thongBao.*`

Tin "Chúc mừng X nhận được…", bùng phát sinh thái, boss thế giới đi qua `ThongBaoGop`; tin ưu tiên (bảo trì, admin,
giờ vàng, chiếm Gym) vẫn gửi thẳng.

| Khoá | Mặc định | Nghĩa |
|---|---|---|
| `sv.thongBao.bat` | `1` | `0` = gửi thẳng như trước |
| `sv.thongBao.toiDa` / `sv.thongBao.cuaSoGiay` | `6` / `60` | tối đa 6 tin / 60 giây cho cả máy chủ |
| `sv.thongBao.moiLoai` | `3` | trần riêng mỗi loại tin trong cửa sổ |
| `sv.thongBao.trungLapGiay` | `120` | cùng nội dung trong chừng này giây thì bỏ |
| `sv.thongBao.gopGiay` | `15` | tin vượt trần vào hàng chờ, cứ chừng này giây gửi một tin gộp |

### 3.6 Đăng ký trong game

`sv.dangKyTrongGame` (`0` = chỉ đăng ký trên web), `sv.dangKyTuKichHoat` (`web` = theo `HTBB_AUTO_ACTIVATE` của web,
`1` = luôn kích hoạt, `0` = không), `sv.dangKyCsdlTaiKhoan` (CSDL chứa `team_user`), `sv.dangKyTreMs` (trả lời chậm
chống bot). Luồng: màn "Đăng ký" ở client → lệnh 39 `#dk2` qua kết nối game → `real.cmd.DangKyTrongGame`. Một bộ quy
tắc chung cho web và game: `web/shared/data/quy-tac-dang-ky.json`, sinh sang Java bằng
`node web/scripts/sinh-quy-tac-dang-ky-java.mjs`. Tài khoản tạo trong game có `fromgame=0` như tài khoản web (login
server chỉ cho vào khi region là 0/9).

## 4. Bảng tra cứu khoá trong danh mục

Phần dưới đây **sinh tự động** từ `tools/cauhinh/danh_muc.json`.

<!-- SINH TU tools/cauhinh/danh_muc.json: BAT DAU (khong sua tay, chay python3 tools/cauhinh/sinh_cau_hinh.py) -->

Tổng cộng **185 khoá** trong **14 nhóm**. Cột *Áp dụng*: **ngay** = máy chủ nhận trong vài giây; **khởi động lại** = chỉ đổi khi máy chủ khởi động lại. Cột *Ở*: máy chủ game, web hay cả hai.

### Chế độ game (`che_do`)

Bật/tắt các chế độ lớn. Hầu hết cần khởi động lại máy chủ (đổi bảng EXP, schema, nội dung).

| Khoá | Tên | Kiểu | Mặc định | Giới hạn | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|---|
| `sv.lau_dai` | Thiết kế dài hạn | cờ 0/1 | `0` |  | khởi động lại | game | 1 = bật GD2 (bảng EXP mới, chuyển EXP Pokémon một lần có sao lưu). Bật/tắt chạy chuyển đổi EXP lúc khởi động. |
| `sv.he_sinh_thai` | Hệ sinh thái | cờ 0/1 | `0` |  | khởi động lại | game | 1 = bật hệ sinh thái thế giới (data_hst_*, tob_hst_*). |
| `sv.tran_cap_theo_mua` | Trần cấp theo mùa | cờ 0/1 | `0` |  | khởi động lại | game | 1 = bật lại trần cấp 60→100 theo ngày khai trương (thiết kế cũ). 0 = không trần, lên cấp chậm + vật phẩm tăng tốc. |
| `sv.ngay_khai_truong` | Ngày khai trương | ngày | *(trống)* |  | khởi động lại | game | yyyy-MM-dd — ngày 0 của lịch mùa. Để trống = theo lau_dai.ngay_khai_truong game tự ghi. |
| `sv.tua_nhanh_ngay` | Tua nhanh thử nghiệm (ngày) | số | `0` | 0..3650 | khởi động lại | game | Cộng thêm N ngày vào ngày hiện tại của lịch dài hạn (thử mùa, vé mùa, lịch). chỉ máy thử — máy thật luôn 0. (máy thật chỉ nhận mặc định) |

### Kinh tế (`kinh_te`)

Tỉ lệ rơi, EXP, thuế chợ, phí, ngân sách giftcode. Áp dụng ngay.

| Khoá | Tên | Kiểu | Mặc định | Giới hạn | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|---|
| `sv.tileXu` | Tỉ lệ rơi xu (%) | số | `10` | 0..100 | ngay | game | % so với tỉ lệ gốc. 100 = như cũ, 0 = tắt. |
| `sv.tileLuong` | Tỉ lệ rơi lượng (%) | số | `20` | 0..100 | ngay | game | % so với tỉ lệ gốc. |
| `sv.tileItemSuKien` | Tỉ lệ rơi đồ sự kiện (%) | số | `20` | 0..100 | ngay | game | % so với tỉ lệ gốc. |
| `sv.tilePotion` | Tỉ lệ rơi bình thuốc (%) | số | `30` | 0..100 | ngay | game | % so với tỉ lệ gốc. |
| `sv.tileNgoc` | Tỉ lệ rơi ngọc (%) | số | `30` | 0..100 | ngay | game | % so với tỉ lệ gốc. |
| `sv.tileItem` | Tỉ lệ rơi đồ thường (%) | số | `20` | 0..100 | ngay | game | % so với tỉ lệ gốc. |
| `sv.chiaXuRoi` | Chia số xu rơi | số | `10` | 1..1000000 | ngay | game | Xu rơi = xu gốc chia số này (tối thiểu 1). |
| `sv.tileExpLv50` | EXP quái từ cấp 50 (%) | số | `10` | 0..100 | ngay | game | % so với EXP gốc. |
| `sv.tileExpDuoiLv50` | EXP quái dưới cấp 50 (%) | số | `50` | 0..100 | ngay | game | % so với EXP gốc. |
| `sv.expvantieu` | Thưởng EXP giao hàng | cờ 0/1 | `0` |  | ngay | game | 1 = bật. |
| `sv.Luckyexp` | Ô EXP rương may mắn | cờ 0/1 | `0` |  | ngay | game | 1 = bật. |
| `sv.ExpQua` | EXP cây ăn quả | cờ 0/1 | `0` |  | ngay | game | 1 = bật (cũng khoá nút tăng tốc cây). |
| `sv.giaXuBoHoaDoVuLan` | Giá xu bó hoa Vu Lan | số | `150000` | 0..2000000000 | ngay | game | Số xu. |
| `sv.tieuNguongMau` | Giao hàng: ngưỡng màu khi nhận | danh sách số | `60189,89389,99689,99989,99999` | 0..100000; 5 phần tử | ngay | game | 5 ngưỡng cộng dồn trên thang 100000 (Trắng..Vàng, còn lại Tím). |
| `sv.tieuHeSoExp` | Giao hàng: hệ số EXP chung (%) | số | `200` | 0..10000 | ngay | game | 200 = ×2. |
| `sv.tieuPcExpTheoMau` | Giao hàng: % EXP theo màu | danh sách số | `50,100,150,250,250,500,0` | 0..100000; 7 phần tử | ngay | game | 7 số (Trắng..Tím, số cuối không dùng). |
| `sv.tieuPermilleCaoCap` | Giao hàng: phần nghìn EXP cao cấp | danh sách số | `50,70,90,35,55,75,5,25,45` | 0..1000; 9 phần tử | ngay | game | 9 số (3 khung cấp × 3 màu). |
| `sv.doiTieuNguongMauXu` | Đổi hàng (xu): ngưỡng màu | danh sách số | `50545,85545,98945,99945,99995` | 0..100000; 5 phần tử | ngay | game | 5 ngưỡng cộng dồn. |
| `sv.doiTieuNguongMauLuong` | Đổi hàng (lượng): ngưỡng màu | danh sách số | `22890,52890,96890,99890,99990` | 0..100000; 5 phần tử | ngay | game | 5 ngưỡng cộng dồn. |
| `roi.xu.phan_tram` | Tỉ lệ một quái rơi xu (%) — dài hạn | số | `30` | 0..100 | ngay | game | Xác suất rơi xu; số xu mỗi lần tự chia ngược để kỳ vọng xu/quái không đổi. |
| `roi.da_nang_cap.phan_nghin` | Tỉ lệ rơi đá nâng cấp (‰) | số | `15` | 0..200 | ngay | game | Phần nghìn mỗi quái khi lệch cấp ≤ 5. |
| `exp.nang_luong_phut` | Phút năng lượng 100% / ngày | số | `90` | 0..1440 | ngay | game | Số phút EXP/rơi đầy đủ mỗi ngày. |
| `exp.nang_luong_tich_phut` | Năng lượng tích luỹ tối đa (phút) | số | `180` | 0..4320 | ngay | game | Phút năng lượng dồn tối đa khi không chơi. |
| `thue.cho` | Thuế chợ ký gửi (%) | số | `6` | 0..50 | ngay | game | Đốt khỏi nền kinh tế khi bán được. |
| `thue.dang_ban` | Phí đăng bán (%) | số | `1` | 0..20 | ngay | game | Đốt, không hoàn; tối thiểu 1 xu. |
| `thue.chuyen_luong` | Phí chuyển lượng (%) | số | `5` | 0..50 | ngay | game | Làm tròn lên, tối thiểu 1 khi > 0. |
| `thue.tran_chuyen_xu_gio` | Trần chuyển xu / ngày (giờ thu nhập) | số | `5` | 0..100 | ngay | game | Bội số thu nhập 1 giờ ở cấp người gửi. |
| `phi.dich_chuyen_phan_tram_gio` | Phí dịch chuyển (% thu nhập 1 giờ) | số | `3` | 0..100 | ngay | game | Tối thiểu 1 xu. |
| `phi.hoi_sinh_phan_tram_gio` | Phí hồi sinh (% thu nhập 1 giờ) | số | `5` | 0..100 | ngay | game | Tối thiểu 1 xu. |
| `phi.ban_npc_phan_tram` | Giá bán đồ cho NPC (% giá) | số | `5` | 0..100 | ngay | game | Không cộng theo cấp cường hoá. |
| `tieu_hao.sua_do_phan_tram_thu_nhap` | Trần phí sửa đồ (% thu nhập 1 giờ / món) | số | `8` | 0..100 | ngay | game | Phí sửa = min(công thức cũ, trần × số món). |
| `giftcode.toi_da_thang` | Giftcode dài hạn tối đa / tháng | số | `2` | 0..20 | ngay | game | Số giftcode một Pokémon nhập được mỗi tháng. |
| `giftcode.gia_tri_toi_da` | Trần giá trị một giftcode | số | `30` | 0..100000 | ngay | cả hai | Điểm giá trị tối đa của một mã (GiftcodeAnToanLogic). |
| `giftcode.ngan_sach_thang` | Ngân sách giftcode / tháng | số | `20000` | 0..100000000 | ngay | cả hai | Tổng giá trị giftcode được phát mỗi tháng. |

### Nạp tiền (`nap_tien`)

Công tắc bán từng loại sản phẩm, hoàn tiền, trần chi tiêu. Web vẫn KHOÁ (fail closed) khi thiếu HTBB_NAP_BAT, giấy phép hoặc bí mật cổng thanh toán — các giá trị đó nằm trong biến môi trường, không ở CSDL.

| Khoá | Tên | Kiểu | Mặc định | Giới hạn | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|---|
| `sv.nap_tien` | Hệ thống nạp tiền (máy chủ game) | cờ 0/1 | `0` |  | khởi động lại | game | Công tắc tổng phía game: 1 = bật (chỉ chạy khi sv.lau_dai=1). Phía web còn cần HTBB_NAP_BAT. |
| `HTBB_NAP_BAT` | Web: bật nạp tiền (biến môi trường) | cờ 0/1 | `0` |  | khởi động lại | web | Đặt trong .env của web, không sửa từ trang này. Tắt hoặc cấu hình cổng chưa an toàn = KHOÁ toàn bộ thu tiền. (bảng `env`; chỉ đọc) |
| `NAP_DA_CO_GIAY_PHEP` | Xác nhận giấy phép hình ảnh/IP (biến môi trường) | cờ 0/1 | `0` |  | khởi động lại | web | Chủ máy chủ tự xác nhận đã có giấy phép. Thiếu = không bán Thành viên, cảnh báo đỏ ở trang Nạp tiền. (bảng `env`; chỉ đọc) |
| `nap.luong.bat` | Bán gói lượng | cờ 0/1 | `1` |  | ngay | web | 0 = web ngừng tạo đơn gói lượng mới (đơn đã thanh toán vẫn được cộng). |
| `nap.the_thang.bat` | Bán thẻ tháng | cờ 0/1 | `1` |  | ngay | web | 0 = web ngừng tạo đơn thẻ tháng mới. Người đã mua vẫn nhận lượng hằng ngày. |
| `nap.ve_mua.bat` | Bán vé mùa cao cấp | cờ 0/1 | `1` |  | ngay | web | 0 = web ngừng tạo đơn vé mùa mới. |
| `thanh_vien.ban_bat` | Bán Thành viên HTBB | cờ 0/1 | `0` |  | ngay | cả hai | 1 = mở bán (còn cần giấy phép + cổng an toàn). Cùng khoá với trang Thành viên. |
| `nap.tang_toc.bat` | Bán vật phẩm tăng tốc (bằng lượng, trong game) | cờ 0/1 | `1` |  | ngay | game | 0 = cửa hàng lượng trong game ngừng bán vé EXP / bùa thể lực / ủy thác / kẹo hiếm bản cao cấp. |
| `nap.hoan_tien.thu_cong` | Cho phép hoàn tiền thủ công | cờ 0/1 | `1` |  | ngay | web | 0 = khoá nút Hoàn tiền / Chargeback ở trang Nạp tiền. |
| `nap.hoan_tien.tu_dong` | Tự động thu hồi khi hoàn đơn đã cộng | cờ 0/1 | `1` |  | ngay | game | 1 = máy chủ game tự chuyển lượng thành nợ / trừ thẻ tháng / gỡ vé mùa cho đơn 'chờ thu hồi'. 0 = đơn đứng chờ, xử lý tay. |
| `tran_tuan_vnd` | Trần nạp / tuần / tài khoản (VND) | số | `1000000` | 0..5000000 | ngay | web | 0 = mặc định 1.000.000 đ. Bị kẹp ≤ 5.000.000. (bảng `nap_cau_hinh`) |
| `tran_thang_vnd` | Trần nạp / tháng / tài khoản (VND) | số | `2000000` | 0..5000000 | ngay | web | 0 = mặc định 2.000.000 đ. Bị kẹp ≤ 5.000.000. (bảng `nap_cau_hinh`) |

### Thành viên (`thanh_vien`)

Thành viên HTBB theo tài khoản (trả phí) và Kích hoạt tài khoản (miễn phí): tính năng bị khoá, giá, thời hạn, chặn đăng nhập.

| Khoá | Tên | Kiểu | Mặc định | Giới hạn | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|---|
| `thanh_vien.bat` | Bật yêu cầu Thành viên | cờ 0/1 | `0` |  | ngay | cả hai | 1 = các tính năng trong danh sách dưới chỉ dành cho Thành viên. 0 = không khoá gì. |
| `thanh_vien.gia` | Giá Thành viên (VND) | số | `20000` | 10000..2000000 | ngay | cả hai | Đồng bộ sang gói nạp THANH_VIEN khi lưu. |
| `thanh_vien.thoi_han_ngay` | Thời hạn (ngày) | số | `0` | 0..36500 | ngay | cả hai | 0 = vĩnh viễn. |
| `thanh_vien.chuc_nang` | Tính năng yêu cầu Thành viên | danh sách chuỗi | `giao_dich,nhan_tang,chat_the_gioi,cho,cho_sinh_thai,chuyen_luong,bang,pvp_xep_hang,giftcode` | `giao_dich`, `nhan_tang`, `chat_the_gioi`, `cho`, `cho_sinh_thai`, `chuyen_luong`, `bang`, `pvp_xep_hang`, `giftcode`, `qua_online`, `gui_thu` | ngay | cả hai | Chọn các tính năng bị khoá với tài khoản thường. |
| `thanh_vien.mien_cap` | Miễn yêu cầu dưới cấp | số | `0` | 0..200 | ngay | cả hai | Người chơi dưới cấp này được dùng thử (0 = không miễn). |
| `thanh_vien.thong_bao` | Câu thông báo khi bị khoá | chuỗi | *(trống)* | ≤ 250 ký tự | ngay | cả hai | Trống = câu mặc định. |
| `kich_hoat.chan_dang_nhap` | Chặn đăng nhập tài khoản chưa kích hoạt | cờ 0/1 | `0` |  | ngay | cả hai | 0 (mặc định) = chưa kích hoạt vẫn vào chơi, chỉ bị khoá các tính năng dưới. 1 = hành vi cũ: không vào được game. Chỉ có tác dụng khi sv.EnableAvtiveCheck=1. |
| `kich_hoat.chuc_nang` | Tính năng khoá khi chưa kích hoạt | danh sách chuỗi | `giao_dich,chat_the_gioi,cho,chuyen_luong,nhan_tang,giftcode` | `giao_dich`, `nhan_tang`, `chat_the_gioi`, `cho`, `cho_sinh_thai`, `chuyen_luong`, `bang`, `pvp_xep_hang`, `giftcode`, `qua_online`, `gui_thu` | ngay | cả hai | Cùng cổng với Thành viên: bị chặn nếu (cần kích hoạt và chưa kích hoạt) hoặc (cần Thành viên và chưa là Thành viên). |
| `kich_hoat.thong_bao` | Câu thông báo khi bị khoá vì chưa kích hoạt | chuỗi | *(trống)* | ≤ 250 ký tự | ngay | cả hai | Trống = câu mặc định (chỉ tới /kichhoat). Thay được {ten} {web}. |
| `kich_hoat.thanh_vien_la_kich_hoat` | Thành viên được coi như đã kích hoạt | cờ 0/1 | `1` |  | ngay | cả hai | 1 (mặc định) = tài khoản là Thành viên HTBB không bị khoá vì chưa kích hoạt (gộp hai bậc làm một nếu muốn). |

### Đăng ký (`dang_ky`)

Đăng ký trong game / trên web, giới hạn Pokémon.

| Khoá | Tên | Kiểu | Mặc định | Giới hạn | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|---|
| `sv.dangKyTrongGame` | Đăng ký trong game | cờ 0/1 | `1` |  | ngay | game | 0 = nút Đăng ký trong game báo 'đăng ký trên web'. |
| `sv.dangKyTuKichHoat` | Tự kích hoạt tài khoản đăng ký trong game | lựa chọn | `web` | `web`, `1`, `0` | ngay | game | web = theo cấu hình web (web.autoActivate). |
| `sv.dangKyTreMs` | Trễ phản hồi đăng ký (ms) | số | `800` | 0..5000 | ngay | game | Làm chậm dò tài khoản. |
| `sv.dangKyCsdlTaiKhoan` | CSDL tài khoản | chuỗi | `account` |  | khởi động lại | game | Tên database chứa team_user. |
| `dang_ky.web.bat` | Đăng ký trên web | cờ 0/1 | `1` |  | ngay | web | 0 = trang Đăng ký từ chối tài khoản mới. |
| `sv.maxCharPerAccount` | Số Pokémon tối đa / tài khoản | số | `3` | 1..3 | ngay | game | Client hỗ trợ tối đa 3. |

### Sự kiện (`su_kien`)

Lịch hẹn sự kiện, World Cup, chiếm Gym. Bật/tắt từng sự kiện ở trang Sự kiện (bảng tob_su_kien).

| Khoá | Tên | Kiểu | Mặc định | Giới hạn | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|---|
| `sv.suKienTuKhoiDongLai` | Tự khởi động lại khi đổi sự kiện | cờ 0/1 | `1` |  | ngay | game | 1 = lệnh bật/tắt sự kiện có hẹn sẽ tự khởi động lại máy chủ. |
| `sv.suKienGomGiay` | Gom lệnh sự kiện (giây) | số | `120` | 30..3600 | ngay | game | Gom nhiều lệnh thành một lần khởi động lại. |
| `sv.suKienBaoTruocGiay` | Báo trước khi đổi sự kiện (giây) | số | `60` | 10..600 | ngay | game |  |
| `sv.worldCupBatDau` | World Cup bắt đầu | ngày giờ | *(trống)* |  | ngay | game | yyyy-MM-dd HH:mm:ss. Trống = tắt. |
| `sv.worldCupKetThuc` | World Cup kết thúc | ngày giờ | *(trống)* |  | ngay | game | Trống = tắt. |
| `sv.worldCupHanNhanQua` | World Cup hạn nhận quà | ngày giờ | *(trống)* |  | ngay | game | Trống = tắt. |
| `sv.gioChiemThanh` | Giờ chiếm Gym | số | `-1` | -1..23 | ngay | game | 0–23, −1 = lịch chuẩn 21h thứ 3 + thứ 7. |
| `sv.phutChiemThanh` | Phút chiếm Gym | số | `0` | -1..59 | ngay | game | 0–59. |
| `sv.ngayChiemThanh` | Ngày chiếm Gym | chuỗi | *(trống)* | ≤ 64 ký tự | ngay | game | Mon,Tue,... hoặc yyyy-MM-dd; trống = mỗi ngày. |
| `sv.isSuKienTrungThul2016` | Sự kiện cũ: Trung thu 2016 | cờ 0/1 | `0` |  | khởi động lại | game | Cờ sự kiện gốc (đọc lúc khởi động). |
| `sv.isSuKienTet2017` | Sự kiện cũ: Tết 2017 | cờ 0/1 | `0` |  | khởi động lại | game |  |
| `sv.isSuKienGioTo2016` | Sự kiện cũ: Giỗ Tổ 2016 | cờ 0/1 | `0` |  | khởi động lại | game |  |
| `sv.isSuKienNoel2023` | Sự kiện cũ: Noel 2023 | cờ 0/1 | `0` |  | khởi động lại | game |  |
| `sv.isSuKienTetduonglich2024` | Sự kiện cũ: Tết dương lịch 2024 | cờ 0/1 | `0` |  | khởi động lại | game |  |

### Bảo trì (`bao_tri`)

Giờ bảo trì hằng ngày, chế độ bảo trì đăng nhập, danh sách được vào khi bảo trì.

| Khoá | Tên | Kiểu | Mặc định | Giới hạn | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|---|
| `bao_tri.bat` | Chế độ bảo trì đăng nhập | cờ 0/1 | `0` |  | ngay | game | 1 = từ chối đăng nhập mới (người đang chơi không bị đá). Tương đương tệp bao_tri.txt — bật một trong hai là bảo trì. |
| `bao_tri.cau` | Câu thông báo bảo trì | chuỗi | *(trống)* | ≤ 200 ký tự | ngay | game | Trống = câu mặc định. |
| `bao_tri.cho` | Tài khoản được vào khi bảo trì | danh sách chuỗi | *(trống)* | ≤ 255 ký tự | ngay | game | Tên tài khoản (chữ thường), cách nhau dấu phẩy. |
| `sv.gioBaoTri` | Giờ bảo trì hằng ngày | chuỗi | *(trống)* | ≤ 5 ký tự | ngay | game | HH:MM cố định, 'tat' = không tự bảo trì, trống = ngẫu nhiên 4:30–5:30. |
| `sv.chiSoMayChu` | Ghi chỉ số máy chủ + đọc bao_tri.txt | cờ 0/1 | `1` |  | khởi động lại | game | 0 = tắt luồng chỉ số (tob_chi_so_may_chu). |

### Chống spam, DoS & nhật ký (`chong_spam`)

Nhịp chat/thao tác, chống ngập kết nối/gói tin, mức nhật ký.

| Khoá | Tên | Kiểu | Mặc định | Giới hạn | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|---|
| `sv.spam.bat` | Chống spam chat / thao tác | cờ 0/1 | `1` |  | ngay | game | 0 = tắt toàn bộ. |
| `sv.spam.chat.gan` | Nhịp chat gần | nhịp số/giây | `5/10` |  | ngay | game | số tin / giây (vd 5/10). |
| `sv.spam.chat.rieng` | Nhịp chat riêng | nhịp số/giây | `6/10` |  | ngay | game |  |
| `sv.spam.chat.theGioi` | Nhịp chat thế giới / người | nhịp số/giây | `1/30` |  | ngay | game |  |
| `sv.spam.theGioiToanServer` | Nhịp chat thế giới toàn máy chủ | nhịp số/giây | `12/60` |  | ngay | game |  |
| `sv.spam.chat.doi` | Nhịp chat đội | nhịp số/giây | `8/10` |  | ngay | game |  |
| `sv.spam.chat.bang` | Nhịp chat đội | nhịp số/giây | `8/10` |  | ngay | game |  |
| `sv.spam.trungLapGiay` | Chặn tin trùng lặp (giây) | số | `30` | 0..3600 | ngay | game |  |
| `sv.spam.chanQuangCao` | Chặn quảng cáo / link | cờ 0/1 | `1` |  | ngay | game |  |
| `sv.spam.viPhamCam` | Số vi phạm thì cấm chat | số | `4` | 1..100 | ngay | game |  |
| `sv.spam.camGiay` | Thang cấm chat (giây) | danh sách số | `60,300,1800,7200` | 1..31536000 | ngay | game | Các bậc cấm tăng dần, cách nhau dấu phẩy. |
| `sv.socketTimeout` | Hết hạn socket (giây) | số | `180` | 0..3600 | ngay | game | Áp dụng cho kết nối mới. 0 = tắt. |
| `sv.maxConnPerIp` | Kết nối tối đa / IP | số | `50` | 0..100000 | ngay | game | 0 = tắt. |
| `sv.connRateMax` | Số kết nối tối đa / cửa sổ | số | `60` | 0..100000 | ngay | game | 0 = tắt. |
| `sv.connRateWindow` | Cửa sổ đếm kết nối (giây) | số | `30` | 0..3600 | ngay | game | 0 = tắt. |
| `sv.dosBanSec` | Thời gian chặn IP ngập (giây) | số | `300` | 0..86400 | ngay | game |  |
| `sv.reloginWaitSec` | Chờ đăng nhập lại (giây) | số | `0` | 0..600 | ngay | game |  |
| `sv.proxyTinCay` | Proxy tin cậy (miễn giới hạn theo IP) | danh sách chuỗi | `127.0.0.1,::1` | ≤ 255 ký tự | ngay | game | IP hoặc dải CIDR (IPv4/IPv6) của WAF / Nova agent, cách nhau dấu phẩy. Kết nối từ đây không bị tính sv.maxConnPerIp, sv.connRate*, chặn sv.dosBanSec, chờ đăng nhập theo IP, hạn mức đăng ký theo IP. Giới hạn theo tài khoản / kết nối / thiết bị vẫn giữ. |
| `sv.dangNhapIpNguong` | Ngưỡng đăng nhập dồn dập / IP | số | `10` | 0..100000 | ngay | game | Số lần đăng nhập trong ~1 giây từ một IP (không phải proxy tin cậy) thì bắt đầu chờ + ghi nghi vấn. 0 = tắt. |
| `sv.dangNhapIpChoMs` | Chờ khi đăng nhập dồn dập (ms) | số | `3000` | 0..10000 | ngay | game | Thời gian chờ mỗi lần đăng nhập khi vượt sv.dangNhapIpNguong. 0 = không chờ. |
| `sv.tranGoiThuong` | Trần gói tin thường / cửa sổ | số | `300` | 10..100000 | ngay | game |  |
| `sv.tranGoiTaiNguyen` | Trần gói tải tài nguyên / cửa sổ | số | `600` | 10..100000 | ngay | game |  |
| `sv.tranGoiTraCuu` | Trần gói tra cứu / cửa sổ | số | `600` | 10..100000 | ngay | game |  |
| `sv.log.muc` | Mức nhật ký (màn hình) | lựa chọn | `THONG_TIN` | `LOI`, `CANH_BAO`, `THONG_TIN`, `GO_LOI`, `ERROR`, `WARN`, `INFO`, `DEBUG` | ngay | game |  |
| `sv.log.mucTep` | Mức nhật ký (tệp) | lựa chọn | *(trống)* | ``, `LOI`, `CANH_BAO`, `THONG_TIN`, `GO_LOI`, `ERROR`, `WARN`, `INFO`, `DEBUG` | ngay | game | Trống = như màn hình. |
| `sv.log.tat` | Loại nhật ký tắt | danh sách chuỗi | `GOI3,GOI5,GOI26,VKTT,SQL,LUU_CHAR` | ≤ 255 ký tự | ngay | game | Mã loại, cách nhau dấu phẩy. |
| `sv.log.kiemToan` | Loại nhật ký kiểm toán | danh sách chuỗi | `ADMIN,GM,NAP,NAP_TIEN,GIFTCODE,CAM,BAN,KINH_TE,KINHTE,CHONG_SPAM` | ≤ 255 ký tự | ngay | game | Ghi riêng tệp kiem_toan (giữ lâu). |

### PvP / Gym / Hầm Ngục (`pvp`)

Công tắc nội dung chiến đấu thiết kế dài hạn (chỉ có tác dụng khi sv.lau_dai=1).

| Khoá | Tên | Kiểu | Mặc định | Giới hạn | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|---|
| `pvp.tinh_diem.bat` | Tính điểm thang PvP | cờ 0/1 | `1` |  | ngay | game | 0 = trận PvP không cộng/trừ điểm thang. |
| `gym.bat` | Thách đấu Gym | cờ 0/1 | `1` |  | ngay | game | 0 = tạm đóng Gym. |
| `ham_nguc.bat` | Hầm Ngục bí ẩn | cờ 0/1 | `1` |  | ngay | game | 0 = không vào tầng mới (vẫn nhận thưởng đã đạt). |

### Chỉ số & chống lạm phát (`chi_so`)

Trần + giảm dần cho chỉ số trang bị, pet, thú cưỡi, skin (docs/thiet-ke-game.md). Chỉ có tác dụng khi sv.lau_dai=1. Áp dụng ngay (đòn kế tiếp; kẹp đồ: lần mặc/tháo kế tiếp).

| Khoá | Tên | Kiểu | Mặc định | Giới hạn | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|---|
| `chi_so.bat` | Bật giới hạn chỉ số | cờ 0/1 | `1` |  | ngay | game | 0 = tắt mọi trần/giảm dần chỉ số (quay về công thức cũ). |
| `chi_so.kep_trang_bi` | Kẹp chỉ số trang bị theo ngân sách | cờ 0/1 | `1` |  | ngay | game | Mỗi món ≤ chuẩn bậc × 1,30 × nâng cấp; đồ bậc cũ ≤ 130% đồ bậc hiện tại. |
| `chi_so.tran_chi_mang` | Trần tỉ lệ chí mạng (‰) | số | `400` | 100..1000 | ngay | game | Phần nghìn, có giảm dần. PvP cố định 300‰. |
| `chi_so.tran_st_chi_mang` | Trần ST chí mạng cộng thêm (‰) | số | `500` | 0..1000 | ngay | game | Trên nền ×1,5. 500 = đòn chí mạng tối đa ×2,0. |
| `chi_so.tran_ne` | Trần né tránh (‰) | số | `300` | 0..900 | ngay | game | Phần nghìn, có giảm dần. PvP cố định 200‰. |
| `chi_so.tran_hut` | Trần hút HP PvE (‰ sát thương) | số | `100` | 0..1000 | ngay | game | Gộp mọi nguồn hút HP. |
| `chi_so.tran_hut_pvp` | Trần hút HP PvP (‰ sát thương thật) | số | `50` | 0..1000 | ngay | game | Gộp mọi nguồn hút HP, tính trên sát thương sau ×0,30. |
| `chi_so.tran_giam_st` | Trần giảm sát thương nhận (‰) | số | `500` | 0..900 | ngay | game | Gộp mọi nguồn thụ động (PvP cố định 400‰). |
| `chi_so.tran_khang_he` | Trần kháng một hệ (‰) | số | `600` | 0..900 | ngay | game | Kháng hệ + kháng mọi hệ + phiến đá. |
| `chi_so.san_sat_thuong` | Sàn sát thương mỗi đòn (‰) | số | `400` | 0..1000 | ngay | game | Sau mọi lớp giảm, một đòn còn ít nhất chừng này phần nghìn (chống bất tử). |
| `chi_so.tran_phan_tram_cong` | Trần tổng % cộng thêm (‰) | số | `500` | 0..5000 | ngay | game | Tổng % tấn công/phòng thủ/HP từ pet, thú cưỡi, thời trang, bộ, VIP… so với chỉ số gốc. |

### Tự động đánh (`tu_dong`)

Tự động đánh chính thức (chỉ khi online, theo luật năng lượng như đánh tay; vé treo máy offline tách riêng). Áp dụng ngay.

| Khoá | Tên | Kiểu | Mặc định | Giới hạn | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|---|
| `tu_dong.bat` | Bật Tự động đánh | cờ 0/1 | `1` |  | ngay | game | 0 = tạm khoá (người đang dùng bị dừng ở lần kiểm tiếp theo). |
| `tu_dong.gio_moi_ngay` | Giờ Tự động đánh mỗi ngày | số | `5` | 0..24 | ngay | game | Miễn phí, chỉ tính lúc online và đang bật. Đặt lại lúc 00:00. |
| `tu_dong.gio_them_the_thang` | Giờ cộng thêm khi có thẻ tháng | số | `2` | 0..24 | ngay | game | Cộng vào giờ mỗi ngày khi thẻ tháng còn hạn. |
| `tu_dong.chi_map_da_mo` | Chỉ ở bãi luyện tập | cờ 0/1 | `1` |  | ngay | game | 1 = chỉ bật ở bản đồ có Pokémon hoang dã đã mở (không ở thành phố, nhà, sàn đấu, sự kiện). |
| `tu_dong.tat_khi_pvp` | Dừng khi PvP / sàn đấu / Gym / boss | cờ 0/1 | `1` |  | ngay | game | 1 = dừng khi bị Pokémon khác tấn công, bật PK, vào sàn đấu / Gym / chiến trường hay đánh boss. |
| `tu_dong.tu_dung_thuoc` | Cho tự uống thuốc | cờ 0/1 | `1` |  | ngay | game | 0 = Tự động đánh không tự uống thuốc hồi phục (người chơi tự bấm). |
| `tu_dong.ban_kinh` | Bán kính tối đa (px) | số | `240` | 48..960 | ngay | game | Pokémon chỉ đánh quanh điểm bật trong bán kính này; ra xa hơn (+64 px) thì dừng. 1 ô = 16 px. |
| `tu_dong.nhip_danh` | Nhịp đánh tối đa khi tự động | nhịp số/giây | `12/5` |  | ngay | game | số lệnh đánh / giây (vd 12/5). Vượt thì bỏ lệnh và tính vi phạm. |
| `tu_dong.nhip_thuoc` | Nhịp uống thuốc tối đa khi tự động | nhịp số/giây | `4/5` |  | ngay | game |  |
| `tu_dong.nhip_nhat` | Nhịp nhặt đồ tối đa khi tự động | nhịp số/giây | `8/5` |  | ngay | game |  |
| `tu_dong.nhip_nguoi` | Nhịp đánh nghi bot (mọi người chơi) | nhịp số/giây | `30/5` |  | ngay | game | Chỉ ghi nghi vấn (hack.txt), không chặn. |
| `tu_dong.vi_pham_tat` | Số vi phạm nhịp thì dừng | số | `20` | 1..1000 | ngay | game | Dừng Tự động đánh + ghi nghi vấn. |
| `tu_dong.hoi_sinh_moi_gio` | Tự hồi sinh tối đa / giờ | số | `2` | 0..20 | ngay | game | Khi đang tự động mà Pokémon gục: chỉ dùng vật phẩm hồi sinh nếu người chơi bật "Tự hồi sinh", tối đa ngần này lần / 60 phút; còn lại dừng tự động và về Trung tâm Pokémon. 0 = không bao giờ. |

### Hồi sinh (`hoi_sinh`)

Khi Pokémon gục: hồi sinh tại chỗ (vật phẩm / xu theo cấp / lượng khoá, có chờ + hồi chiêu, cấm ở PvP / Gym / boss / Mê cung), về Trung tâm Pokémon (miễn phí, phạt nhẹ: EXP cấp, độ bền, Mệt mỏi) hoặc chờ đồng đội. Áp dụng ngay.

| Khoá | Tên | Kiểu | Mặc định | Giới hạn | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|---|
| `hoi_sinh.bat` | Bật cơ chế hồi sinh mới | cờ 0/1 | `1` |  | ngay | game | 0 = luồng cũ (popup hồi sinh tại chỗ 5% giờ thu nhập, tự hồi sinh dưới cấp 50). |
| `hoi_sinh.xu_phan_tram_gio` | Phí xu tại chỗ (% thu nhập 1 giờ) | số | `40` | 0..1000 | ngay | game | Theo cấp (ThongSoLauDai.xuMoiGio). 40 = 0,4 giờ cày. Nhân hệ số gục lặp. |
| `hoi_sinh.xu_toi_thieu` | Phí xu tại chỗ tối thiểu | số | `200` | 0..100000000 | ngay | game | Xu. |
| `hoi_sinh.he_so_lap` | Hệ số gục lặp (%) | danh sách số | `100,150,200,300` | 0..10000 | ngay | game | Lần gục thứ 1, 2, 3... trong cửa sổ; lần sau dùng số cuối (trần). Áp cho phí xu và lượng khoá. |
| `hoi_sinh.cua_so_phut` | Cửa sổ gục lặp (phút) | số | `30` | 1..1440 | ngay | game | Đếm số lần gục trong ngần này phút. |
| `hoi_sinh.luong_khoa` | Phí lượng khoá tại chỗ | số | `2` | 0..1000 | ngay | game | 0 = tắt lựa chọn trả lượng khoá. Nhân hệ số gục lặp (làm tròn lên). |
| `hoi_sinh.luong_khoa_toi_da` | Trần phí lượng khoá | số | `10` | 0..1000 | ngay | game | Lượng khoá tối đa một lần. |
| `hoi_sinh.cho_sau_guc_giay` | Chờ sau khi gục (giây) | số | `5` | 0..600 | ngay | game | Hồi sinh tại chỗ chỉ bấm được sau ngần này giây (đếm ngược trên hộp thoại). |
| `hoi_sinh.hoi_chieu_giay` | Hồi chiêu hồi sinh tại chỗ (giây) | số | `60` | 0..3600 | ngay | game | Giữa hai lần hồi sinh tại chỗ. Về Trung tâm luôn được. |
| `hoi_sinh.hp_thuoc` | HP khi dùng Thuốc hồi sinh (%) | số | `30` | 1..100 | ngay | game | Thuốc hồi sinh tối đa lấy % trong data_vat_pham_lau_dai (hoi_sinh:100). |
| `hoi_sinh.hp_tra_phi` | HP khi trả xu / lượng khoá / tân thủ (%) | số | `50` | 1..100 | ngay | game |  |
| `hoi_sinh.exp_mat_phan_nghin` | Mất EXP khi về Trung tâm (phần nghìn) | số | `20` | 0..200 | ngay | game | Phần nghìn EXP lên cấp hiện tại (20 = 2%). Không bao giờ tụt cấp: chỉ trừ trong tiến độ cấp. |
| `hoi_sinh.exp_mat_toi_da_phut` | Trần mất EXP (phút cày) | số | `15` | 0..600 | ngay | game | Không mất quá ngần này phút EXP đánh quái ở cấp đó. 0 = không chặn. |
| `hoi_sinh.do_ben_mat_phan_tram` | Mất độ bền khi về Trung tâm (%) | số | `5` | 0..100 | ngay | game | % độ bền tối đa của mỗi trang bị đang mặc (không làm vỡ đồ). |
| `hoi_sinh.met_moi_phut` | Mệt mỏi (phút) | số | `5` | 0..120 | ngay | game | Sau khi về Trung tâm. 0 = tắt. |
| `hoi_sinh.met_moi_exp` | Mệt mỏi: giảm EXP (%) | số | `20` | 0..90 | ngay | game | EXP hạ quái. |
| `hoi_sinh.met_moi_tan_cong` | Mệt mỏi: giảm tấn công (%) | số | `10` | 0..90 | ngay | game |  |
| `hoi_sinh.tan_thu_cap` | Bảo vệ tân thủ dưới cấp | số | `15` | 0..100 | ngay | game | Dưới cấp này: không mất EXP / độ bền / Mệt mỏi, hồi sinh tại chỗ miễn phí. |
| `hoi_sinh.tan_thu_hoi_chieu_giay` | Hồi chiêu hồi sinh miễn phí tân thủ (giây) | số | `30` | 0..3600 | ngay | game |  |
| `hoi_sinh.dong_doi_bat` | Cho chờ đồng đội hồi sinh | cờ 0/1 | `1` |  | ngay | game | Đồng đội ở gần mang Thuốc hồi sinh được mời cứu. |
| `hoi_sinh.dong_doi_giay` | Thời gian chờ đồng đội (giây) | số | `60` | 10..600 | ngay | game |  |
| `hoi_sinh.cam_pvp` | Cấm tại chỗ ở PvP / sàn đấu | cờ 0/1 | `1` |  | ngay | game | Sàn đấu, giải Liên minh, chiến trường, lãnh địa đội, chiếm Gym đang đánh, đấu trường + hoi_sinh.map_pvp. |
| `hoi_sinh.cam_gym` | Cấm tại chỗ khi thách đấu Gym | cờ 0/1 | `1` |  | ngay | game |  |
| `hoi_sinh.cam_boss` | Cấm tại chỗ giữa trận boss | cờ 0/1 | `1` |  | ngay | game | Đã gây sát thương vào boss thế giới / Gym còn sống cùng bản đồ. |
| `hoi_sinh.cam_me_cung` | Cấm tại chỗ trong Mê cung | cờ 0/1 | `1` |  | ngay | game |  |
| `hoi_sinh.map_pvp` | Bản đồ PvP (id) | danh sách số | `202,203,204,205,227,228` | 0..100000 | ngay | game | Thêm vào luật cấm PvP (so cả id bản đồ lẫn id nạp). |
| `hoi_sinh.map_cam_them` | Bản đồ cấm tại chỗ thêm (id) | chuỗi | *(trống)* | ≤ 255 ký tự | ngay | game | Cách nhau dấu phẩy; trống = không. |
| `hoi_sinh.auto_ve_map_up` | Cho "Auto về Map Up" sau khi gục | cờ 0/1 | `0` |  | ngay | game | 0 = gục thì không tự bay lại bãi luyện (chống treo máy vô hạn). |
| `hoi_sinh.gia_thuoc_phan_tram_gio` | Giá Thuốc hồi sinh (% thu nhập 1 giờ) | số | `30` | 0..1000 | ngay | game | Giá ở cửa hàng NPC theo cấp người mua (cùng bậc phí xu tại chỗ). 0 = giá gốc data_potion. |
| `hoi_sinh.gia_thuoc_toi_thieu` | Giá Thuốc hồi sinh tối thiểu | số | `407` | 0..100000000 | ngay | game | Xu / viên (không thấp hơn giá gốc). |
| `hoi_sinh.thuoc_moi_gio` | Hồi sinh bằng vật phẩm tối đa / giờ | số | `6` | 0..100 | ngay | game | Thuốc hồi sinh / tối đa của chính mình (tay, Tự động đánh, dùng từ túi) trong 60 phút. Đồng đội cứu không tính. 0 = không giới hạn. |

### Thông báo (`thong_bao`)

Gộp thông báo toàn máy chủ, địa chỉ website / link tải hiện trong game.

| Khoá | Tên | Kiểu | Mặc định | Giới hạn | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|---|
| `sv.thongBao.bat` | Gộp thông báo toàn máy chủ | cờ 0/1 | `1` |  | ngay | game | 0 = gửi thẳng như cũ. |
| `sv.thongBao.toiDa` | Thông báo tối đa / cửa sổ | số | `6` | 1..1000 | ngay | game |  |
| `sv.thongBao.cuaSoGiay` | Cửa sổ đếm thông báo (giây) | số | `60` | 1..3600 | ngay | game |  |
| `sv.thongBao.moiLoai` | Tối đa mỗi loại / cửa sổ | số | `3` | 1..1000 | ngay | game |  |
| `sv.thongBao.trungLapGiay` | Bỏ thông báo trùng (giây) | số | `120` | 0..86400 | ngay | game |  |
| `sv.thongBao.gopGiay` | Gộp mỗi (giây) | số | `15` | 1..3600 | ngay | game |  |
| `sv.website` | Địa chỉ website (hiện trong game) | chuỗi | *(trống)* | ≤ 200 ký tự | ngay | game |  |
| `sv.linkTaiGame` | Link tải game (hiện trong game) | chuỗi | *(trống)* | ≤ 200 ký tự | ngay | game |  |

### Quản trị & thử nghiệm (`quan_tri`)

Danh sách GM, chế độ thử. Các khoá thử nghiệm bị khoá ở máy thật.

| Khoá | Tên | Kiểu | Mặc định | Giới hạn | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|---|
| `sv.adminAccounts` | Danh sách GM trong game | danh sách chuỗi | *(trống)* | ≤ 255 ký tự | ngay | game | Tên Pokémon GM, cách nhau dấu phẩy. |
| `sv.gmCanCoPInfo` | GM cần cả cờ pInfo | cờ 0/1 | `0` |  | ngay | game | 1 = quyền GM cần tên trong danh sách VÀ pInfo[16]=1. |
| `sv.banToiThieu` | Bản client tối thiểu | số | `0` | 0..1000000 | ngay | game | > 0 = từ chối client jar/Android cũ hơn. |
| `sv.choPhepTaoTenGM` | Cho tạo Pokémon tên GM | cờ 0/1 | `0` |  | ngay | game | Chỉ bật tạm thời. |
| `sv.testMode` | Chế độ thử (testMode) | cờ 0/1 | `0` |  | ngay | game | Mã test cấp tài nguyên miễn phí. Khoá ở máy thật. (máy thật chỉ nhận mặc định) |
| `sv.testCode` | Mã test | chuỗi | *(trống)* | ≤ 64 ký tự | ngay | game | Chuỗi gõ trong game khi testMode=1. (máy thật chỉ nhận mặc định) |

<!-- SINH TU tools/cauhinh/danh_muc.json: KET THUC -->
