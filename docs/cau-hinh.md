# Cấu hình

Tài liệu tra cứu mọi khoá cấu hình của máy chủ game và website. Có hai nơi chứa cấu hình:

| Nơi | Chứa gì | Sửa ở đâu |
|---|---|---|
| **Bảng `cau_hinh`** (CSDL `htbb_db`) | mọi khoá trong danh mục cấu hình trung tâm: tỉ lệ rơi, EXP, sự kiện, chống spam, công tắc tính năng… | trang web **/admin → Cấu hình hệ thống** (có lịch sử, hoàn tác) |
| **`server.ini`** (cạnh `htbb-server.jar`) | khoá khởi động: cổng, kết nối CSDL, đường dẫn log, cờ cũ bắt buộc; đồng thời là giá trị khởi tạo / dự phòng cho khoá trong danh mục | VPS: sửa [`vps/server.ini.tmpl`](../vps/server.ini.tmpl) rồi commit; máy nhà: sửa tay |

Thứ tự ưu tiên: **bảng `cau_hinh` > `server.ini` > mặc định trong danh mục** — riêng **khoá ẩn** (cố định / luôn bật,
xem dưới) máy thật bỏ qua bảng `cau_hinh`: **`server.ini` > giá trị cố định**.

**Phân loại (19/10/2026).** Mỗi khoá có cột `lop` trong bảng `cau_hinh_danh_muc`:

- **Chỉnh trên web** (`chinh`) — thứ người vận hành thật sự chỉnh: tỉ lệ rơi / EXP, giờ chiếm Gym, giftcode, công tắc bán
  hàng nạp tiền, Thành viên / kích hoạt, giới hạn Tự động đánh, giá hồi sinh, bảo trì, đăng ký, link, GM. Hiện trên
  **/admin/cau-hinh-he-thong** theo 6 nhóm, mỗi ô có mặc định và nút *Về mặc định*.
- **Cố định** (`co_dinh`) — giá trị sản xuất không nên đổi tuỳ tiện: nội bộ chống DoS / spam / nhật ký, ngưỡng gói tin,
  công thức kinh tế (thuế, phí, giao hàng), trần chỉ số, luật an toàn Tự động đánh / hồi sinh, cờ sự kiện cũ,
  khoá thử nghiệm. **Ẩn** khỏi trang; máy thật luôn chạy giá trị trong danh mục (đã đặt bằng đúng giá trị VPS đang chạy).
- **Luôn bật** (`luon_bat`) — công tắc tính năng cốt lõi (dài hạn, hệ sinh thái, Gym, Hầm Ngục, Tự động đánh, hồi sinh,
  chống spam, trần chỉ số, hoàn tiền…): luôn `1`, ẩn.

Đổi khẩn cấp một khoá ẩn: thêm `khoa=gia_tri` vào [`vps/server.ini.tmpl`](../vps/server.ini.tmpl) (máy nhà: `server.ini`)
rồi khởi động lại máy chủ game. Máy thử (`sv.cho_phep_thu_nghiem=1`; web: dev hoặc `HTBB_CHO_PHEP_THU_NGHIEM=1`) vẫn đọc
`cau_hinh` cho mọi khoá và trang có nút *Hiện khoá cố định*. Bảng đầy đủ ở mục 4 (bản chụp danh mục).

Vận hành hằng ngày (đổi cấu hình từ web, khởi động lại an toàn): [van-hanh.md](van-hanh.md). Kiến trúc của
dịch vụ cấu hình trung tâm: [kien-truc.md](kien-truc.md). Biến môi trường của website: `web/.env.example` và
[web/README.md](../web/README.md).

## 1. Cấu hình trung tâm (danh mục)

| Phần | Tệp |
|---|---|
| Danh mục khoá (nguồn duy nhất) | bảng `cau_hinh_danh_muc` + `cau_hinh_nhom` trong CSDL (tệp `sql/htbb.sql`: phần A cho CSDL mới, phần B nạp lại trên CSDL đang chạy). Máy chủ nạp danh mục từ đây lúc khởi động và mỗi 60 giây, thiếu bảng là không khởi động |
| Bản dự phòng của web | `web/shared/utils/cauHinhTrungTam.gen.ts` (tệp tĩnh, test đối chiếu với phần A của `sql/htbb.sql`) |
| Dịch vụ máy chủ | `server/src/com/htbb/cauhinh/CauHinhTrungTam.java` (+ `TinhCauHinh`, `KiemGiaTri`, `ApDungMacDinh`, `CongTac`) |
| Trang web | `/admin/cau-hinh-he-thong` (trang cấu hình DUY NHẤT; trang cũ `/admin/cauhinh` đã gỡ), API `/api/admin/cau-hinh-he-thong/**` |
| Bộ ví dụ kiểm chung Java ↔ web | [`tools/cauhinh/vi_du_kiem.tsv`](../tools/cauhinh/vi_du_kiem.tsv) |

Thêm / sửa khoá: sửa dòng trong `cau_hinh_danh_muc` (SQL; khoá mới cần mã đọc nó), khởi động lại máy chủ, xuất lại
phần A của `sql/htbb.sql` ([sql/README.md](../sql/README.md)) và chép các dòng mới vào khối `cau_hinh_danh_muc` ở phần B
mục 2 của cùng tệp; cập nhật bảng ở mục 4 và `cauHinhTrungTam.gen.ts` cho khớp (test báo lệch). Bộ sinh cũ
`sinh_cau_hinh.py` + `danh_muc.json` đã lưu trữ ở `tools/luu_tru_sinh_du_lieu/cauhinh/`.

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
- **World Cup**: đã bỏ khỏi danh mục sự kiện (19/10/2026); ba khoá `sv.worldCup*` còn trong danh mục cấu hình nhưng không
  bật gì nữa. Mọi sự kiện khác tự bật / tắt theo lịch năm ([van-hanh.md](van-hanh.md) mục 7.1).
- **Chống clone & chuyển xu** (`chong_clone.*`): cấp / tuổi tài khoản / giờ chơi tối thiểu để chuyển hay nhận xu, giới
  hạn Pokémon cùng thiết bị / IP gửi cho một người mỗi ngày, khoảng giá ký gửi ([thiet-ke-game.md](thiet-ke-game.md) mục 9.6).
- **Hạn mức đăng ký** `dang_ky.toi_da_gio` / `dang_ky.toi_da_ngay` (nhóm Bảo trì): số tài khoản mới tối đa từ một IP
  trong 1 giờ / 24 giờ gần nhất, đếm GỘP web (`dang_ky`) + trong game (`dang_ky_game`) ở bảng `web_chan_spam`; trong
  game còn đếm thêm theo mã thiết bị với cùng hạn mức. Chỉ lượt tạo THÀNH CÔNG mới tính. Áp dụng ≤ 5 giây cho cả web
  (`server/utils/dangKy.ts`) lẫn máy chủ game (`DangKyTrongGame`). Không có dòng / giá trị hỏng = `hanMuc` trong
  `web/shared/data/quy-tac-dang-ky.json` (3 / 10). Mạng di động Việt Nam (CGNAT) cho nhiều người chung một IP, nên
  **open test** đặt **6 / giờ, 30 / 24 giờ** (khối `-- >>> mo_han_muc_dang_ky.sql` của `sql/htbb.sql`, INSERT IGNORE —
  không đè giá trị admin đã sửa). **Xem lại lúc khai trương chính thức** (cùng lúc hạ trần giftcode open test).

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
| `sv.ipLangNghe` | địa chỉ lắng nghe cổng game; trống (mặc định) = mọi giao diện. Staging đặt `127.0.0.1` (`vps/staging/`) |
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

Kênh chat 0.9.8 (bảng chat nhiều kênh, `com.htbb.pokemon.KenhChatLogic`, lớp `co_dinh`): luật kênh chạy TRƯỚC chống spam ở trên.

| Khoá | Mặc định | Nghĩa |
|---|---|---|
| `sv.chat.capGan` | `5` | cấp tối thiểu chat bản đồ (trước 0.9.8 cố định 20 và bỏ im lặng) |
| `sv.chat.capTheGioi` / `sv.chat.xuTheGioi` / `sv.chat.giayTheGioi` | `10` / `100` / `30` | kênh Thế giới: cấp, phí xu mỗi tin (hố hút; bị chặn thì không trừ), giãn cách mỗi người (client đếm ngược) |
| `sv.chat.capRieng` | `3` | cấp tối thiểu nhắn riêng |
| `sv.chat.doDai` | `80` | độ dài tối đa một tin (thế giới / đội / nhóm / riêng) |
| `sv.chat.lichSu` | `50` | số dòng Thế giới / Đội gần nhất gửi cho người vừa vào game |
| `sv.chat.chanToiDa` | `100` | số tên tối đa trong danh sách chặn (`tob_chat_chan`) |
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

Phần dưới đây là bản chụp bảng `cau_hinh_danh_muc` / `cau_hinh_nhom` (sinh lần cuối 19/10/2026; CSDL thắng nếu lệch).

<!-- BAN CHUP cau_hinh_danh_muc: BAT DAU -->

Tổng cộng **400 khoá** trong **22 nhóm**. Phân loại (cột `lop`):

| Lớp | Số khoá | Trên trang quản trị | Máy thật lấy giá trị từ |
|---|---|---|---|
| **Chỉnh trên web** (`chinh`) | 109 | hiện, sửa được | CSDL `cau_hinh` > server.ini > mặc định |
| **Cố định** (`co_dinh`) | 252 | ẩn | server.ini (khẩn cấp) > giá trị cố định — dòng CSDL bị bỏ qua |
| **Luôn bật** (`luon_bat`) | 39 | ẩn | server.ini (khẩn cấp) > `1` — dòng CSDL bị bỏ qua |

Máy thử (`sv.cho_phep_thu_nghiem=1` trong server.ini; web: chế độ dev hoặc `HTBB_CHO_PHEP_THU_NGHIEM=1`) vẫn đọc dòng CSDL của khoá ẩn và trang có nút "Hiện khoá cố định". Cột *Áp dụng*: **ngay** = máy chủ nhận trong vài giây; **khởi động lại** = chỉ đổi khi máy chủ khởi động lại. Cột *Ở*: máy chủ game, web hay cả hai.

### Khoá chỉnh trên web (109)

#### Kinh tế: tỉ lệ rơi & EXP (`kinh_te`)

Tỉ lệ rơi xu / lượng / đồ và EXP quái. Áp dụng ngay. Thuế, phí và công thức kinh tế cốt lõi đã cố định (không hiện ở đây).

| Khoá | Tên | Kiểu | Mặc định | Giới hạn | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|---|
| `sv.tileXu` | Tỉ lệ rơi xu (%) | số | `4` | 0..100 | ngay | game | % so với tỉ lệ gốc. 100 = như cũ, 0 = tắt. |
| `sv.tileLuong` | Tỉ lệ rơi lượng (%) | số | `10` | 0..100 | ngay | game | % so với tỉ lệ gốc. |
| `sv.tileItemSuKien` | Tỉ lệ rơi đồ sự kiện (%) | số | `10` | 0..100 | ngay | game | % so với tỉ lệ gốc. |
| `sv.tilePotion` | Tỉ lệ rơi bình thuốc (%) | số | `14` | 0..100 | ngay | game | % so với tỉ lệ gốc. |
| `sv.tileNgoc` | Tỉ lệ rơi ngọc (%) | số | `14` | 0..100 | ngay | game | % so với tỉ lệ gốc. |
| `sv.tileItem` | Tỉ lệ rơi đồ thường (%) | số | `10` | 0..100 | ngay | game | % so với tỉ lệ gốc. |
| `sv.tileExpLv50` | EXP quái từ cấp 50 (%) | số | `10` | 0..100 | ngay | game | % so với EXP gốc. |
| `sv.tileExpDuoiLv50` | EXP quái dưới cấp 50 (%) | số | `50` | 0..100 | ngay | game | % so với EXP gốc. |
| `roi.xu.phan_tram` | Tỉ lệ một quái rơi xu (%) — dài hạn | số | `30` | 0..100 | ngay | game | Xác suất rơi xu; số xu mỗi lần tự chia ngược để kỳ vọng xu/quái không đổi. |
| `roi.da_nang_cap.phan_nghin` | Tỉ lệ rơi đá nâng cấp (‰) | số | `15` | 0..200 | ngay | game | Phần nghìn mỗi quái khi lệch cấp ≤ 5. |
| `exp.nang_luong_phut` | Phút năng lượng 100% / ngày | số | `90` | 0..1440 | ngay | game | Số phút EXP/rơi đầy đủ mỗi ngày. |
| `exp.nang_luong_thanh_vien_phut` | Năng lượng: phút thưởng Thành viên HTBB | số | `30` | 0..180 | ngay | game | Phút năng lượng 100% cộng thêm mỗi ngày khi tài khoản có Thành viên HTBB còn hạn lúc bắt đầu ngày. Chỉ EXP, không tích sang hôm sau; cộng dồn với Thẻ tháng và Vé mùa cao cấp. 0 = tắt. |
| `exp.nang_luong_the_thang_phut` | Năng lượng: phút thưởng Thẻ tháng | số | `30` | 0..180 | ngay | game | Phút năng lượng 100% cộng thêm mỗi ngày khi Pokémon có Thẻ tháng còn hạn lúc bắt đầu ngày. Chỉ EXP, không tích sang hôm sau; cộng dồn với Thành viên và Vé mùa cao cấp. 0 = tắt. |
| `exp.nang_luong_ve_mua_phut` | Năng lượng: phút thưởng Vé mùa cao cấp | số | `30` | 0..180 | ngay | game | Phút năng lượng 100% cộng thêm mỗi ngày khi Pokémon đã mở Vé mùa cao cấp của mùa hiện tại lúc bắt đầu ngày. Chỉ EXP, không tích sang hôm sau; cộng dồn với Thành viên và Thẻ tháng. 0 = tắt. |
| `tang_toc.bua_ngay_toi_da` | Bùa thể lực: số bùa tối đa / ngày | số | `0` | 0..100 | ngay | game | Số Bùa thể lực một Pokémon dùng được mỗi ngày. 0 = không giới hạn (mặc định 31/10/2026). Bùa không tính vào trần 60 / 210 điểm tăng tốc; mỗi bùa +60 phút bậc bùa (EXP / xu theo tang_toc.bua_exp_phan_tram / tang_toc.bua_xu_phan_tram). |
| `tang_toc.bua_exp_phan_tram` | Bùa thể lực: EXP ở bậc bùa (%) | số | `50` | 0..100 | ngay | game | Mỗi Bùa thể lực thêm 60 phút bậc bùa ngay sau phần năng lượng 100% của ngày; ở bậc này EXP từ quái nhận mức này (%). Mặc định 50. Không giới hạn số bùa, không tích sang hôm sau. |
| `tang_toc.bua_xu_phan_tram` | Bùa thể lực: xu / đá / đồ rơi ở bậc bùa (%) | số | `50` | 0..100 | ngay | game | Xu, đá và đồ rơi từ quái ở bậc bùa (%). Mặc định 50: 60 phút bậc bùa thay 60 phút mức 20% nên một bùa 30 lượng thêm ~0,3 giờ thu nhập xu = 1% / lượng (đúng tỷ giá tham chiếu). |
| `thu.o_co_ban` | Ô thú cơ bản | số | `20` | 1..120 | ngay | game | Số ô thú (thú cưỡi + Pokémon đi theo, chung một danh sách) mỗi người chơi trước khi dùng Vé mở ô thú. Đầy ô: không nhận thêm thú; người chơi cũ đang vượt giữ nguyên, chỉ không nhận thêm. |
| `thu.o_toi_da` | Ô thú tối đa | số | `60` | 1..120 | ngay | game | Trần ô thú mở bằng Vé mở ô thú (+1 ô mỗi vé). Dùng vé khi đã tối đa bị từ chối, không mất vé; cửa hàng không bán vé thừa. |
| `thu.tha_xu_co_ban` | Thả thú: xu cơ bản | số | `20` | 0..1000 | ngay | game | Xu khi thả một thú VĨNH VIỄN về tự nhiên = (cơ bản + mỗi cấp × cấp) × bậc hiếm, không quá trần mỗi con. Thú có hạn: 0 xu. |
| `thu.tha_xu_moi_cap` | Thả thú: xu mỗi cấp | số | `2` | 0..100 | ngay | game | Cộng thêm theo cấp của thú (cấp tính tối đa 100). |
| `thu.tha_xu_toi_da` | Thả thú: trần xu mỗi con | số | `300` | 0..5000 | ngay | game | Trần xu nhận được khi thả một con (hút xu an toàn: giữ nhỏ so với giờ thu nhập). |
| `thu.tha_xu_ngay` | Thả thú: trần xu mỗi ngày | số | `2000` | 0..1000000 | ngay | game | Xu tối đa một người chơi nhận được từ thả thú mỗi ngày (theo lịch máy chủ, lưu trong CSDL: đăng nhập lại / khởi động lại không đặt lại). Quá trần vẫn thả được nhưng 0 xu. |
| `doi_luong.ti_le` | Đổi lượng → lượng khoá: lượng khoá mỗi lượng | số | `1` | 0..100 | ngay | cả hai | NPC Tổng Quản (mục Đổi lượng) và web /account: 1 lượng thường = N lượng khoá. Một chiều (không đổi ngược, lượng khoá không bao giờ là đầu vào), không trần ngày. 0 = tắt. |
| `doi_luong_xu.phan_tram_gio` | Đổi lượng → xu: % thu nhập 1 giờ | số | `1` | 0..100 | ngay | cả hai | Tỉ giá chung toàn máy chủ: 1 lượng = làm tròn (N% × thu nhập 1 giờ ở cấp trung vị người chơi thật hoạt động 7 ngày), tính lúc 00:00 và giữ cố định cả ngày (bảng tob_ti_gia_luong_xu; đổi khoá có hiệu lực từ ngày sau). Xu sinh ra đếm vào xu vào (tob_kinh_te_ngay). 0 = tắt. |
| `doi_luong_xu.tran_ngay` | Đổi lượng → xu: trần lượng / tài khoản / ngày | số | `5000` | 0..1000000 | ngay | cả hai | Số lượng thường tối đa mỗi tài khoản đổi ra xu với hệ thống mỗi ngày (gộp mọi Pokémon của tài khoản, NPC + web). 0 = tắt. |
| `san_luong.phi_phan_tram` | Sàn lượng ↔ xu: phí bên mua (%) | số | `5` | 0..50 | ngay | cả hai | Phí xu bên mua trả thêm trên mỗi lượng khớp (làm tròn lên theo từng lượng), bị đốt (hố hút). Sàn chỉ giao dịch lượng thường; lượng khoá không bao giờ lên sàn. |
| `san_luong.tran_ngay` | Sàn lượng ↔ xu: trần lượng đặt lệnh / tài khoản / ngày | số | `50000` | 0..10000000 | ngay | cả hai | Tổng lượng đặt lệnh bán + mua mỗi tài khoản mỗi ngày (huỷ lệnh trong ngày trả lại phần chưa khớp). Cổng chống clone như chợ ký gửi; bot không dùng sàn. 0 = đóng sàn. |

#### Sự kiện & giftcode (`su_kien`)

Giờ chiếm Gym, trần & ngân sách giftcode. Bật/tắt từng sự kiện ở trang Sự kiện (bảng tob_su_kien).

| Khoá | Tên | Kiểu | Mặc định | Giới hạn | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|---|
| `giftcode.toi_da_thang` | Giftcode dài hạn tối đa / tháng | số | `2` | 0..20 | ngay | game | Số giftcode một Pokémon nhập được mỗi tháng. |
| `giftcode.gia_tri_toi_da` | Trần giá trị một giftcode | số | `30` | 0..100000 | ngay | cả hai | Điểm giá trị tối đa của một mã (GiftcodeAnToanLogic). |
| `giftcode.ngan_sach_thang` | Ngân sách giftcode / tháng | số | `20000` | 0..100000000 | ngay | cả hai | Tổng giá trị giftcode được phát mỗi tháng. |
| `sv.gioChiemThanh` | Giờ chiếm Gym | số | `-1` | -1..23 | ngay | game | 0–23, −1 = lịch chuẩn 21h thứ 3 + thứ 7. |
| `sv.phutChiemThanh` | Phút chiếm Gym | số | `-1` | -1..59 | ngay | game | 0–59. |
| `sv.ngayChiemThanh` | Ngày chiếm Gym | chuỗi | *(trống)* | ≤ 64 ký tự | ngay | game | Mon,Tue,... hoặc yyyy-MM-dd; trống = mỗi ngày. |

#### Nạp tiền (`nap_tien`)

Công tắc bán từng loại sản phẩm, trần chi tiêu. Web vẫn KHOÁ (fail closed) khi thiếu HTBB_NAP_BAT, giấy phép hoặc bí mật cổng thanh toán — các giá trị đó nằm trong biến môi trường, không ở CSDL.

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
| `tran_tuan_vnd` | Trần nạp / tuần / tài khoản (VND) | số | `1000000` | 0..5000000 | ngay | web | 0 = mặc định 1.000.000 đ. Bị kẹp ≤ 5.000.000. (bảng `nap_cau_hinh`) |
| `tran_thang_vnd` | Trần nạp / tháng / tài khoản (VND) | số | `2000000` | 0..5000000 | ngay | web | 0 = mặc định 2.000.000 đ. Bị kẹp ≤ 5.000.000. (bảng `nap_cau_hinh`) |

#### Thành viên & kích hoạt (`thanh_vien`)

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

#### Chống clone & chuyển xu (`chong_clone`)

Cổng cho mọi đường chuyển xu / giá trị giữa hai người chơi (giao dịch, ký gửi, thú cưng lấy xu, chợ trang trí, chợ sinh thái, thách đấu): cấp, tuổi tài khoản, giờ chơi, giới hạn cùng thiết bị, giá ký gửi. Nhật ký + báo cáo "Nghi clone" ở /admin/kinh-te.

| Khoá | Tên | Kiểu | Mặc định | Giới hạn | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|---|
| `chong_clone.bat` | Bật chống clone | cờ 0/1 | `1` |  | ngay | game | 1 = áp các cổng dưới cho mọi đường chuyển xu / giá trị giữa hai người chơi. 0 = tắt hết (chỉ để khẩn cấp). |
| `chong_clone.cap_toi_thieu` | Cấp tối thiểu để chuyển / nhận xu | số | `20` | 0..200 | ngay | game | Cả hai bên của giao dịch, ký gửi, giao dịch thú cưng lấy xu, chợ trang trí / sinh thái, thách đấu cược xu. |
| `chong_clone.tuoi_tai_khoan_gio` | Tuổi tài khoản tối thiểu (giờ) | số | `24` | 0..2160 | ngay | game | Tính từ lúc đăng ký (team_user.regdate). 0 = không xét. |
| `chong_clone.gio_choi_toi_thieu` | Giờ chơi tối thiểu của Pokémon (giờ) | số | `10` | 0..1000 | ngay | game | tob_char.totalTimePlay + phiên đang chơi. 0 = không xét. |
| `chong_clone.nv_cung_may_toi_da` | Pokémon cùng thiết bị / IP gửi cho một người / ngày | số | `2` | 1..50 | ngay | game | Mỗi người nhận, mỗi ngày: tối đa N Pokémon (tài khoản phụ) dùng chung thiết bị hoặc IP được chuyển cho họ; Pokémon thứ N+1 bị chặn. IP proxy tin cậy chỉ tính theo thiết bị. |
| `chong_clone.ky_gui_gia_toi_da_lan` | Giá ký gửi tối đa (lần giá gốc) | số | `10` | 1..1000 | ngay | game | Giá rao ≤ max(giá gốc × N, thu nhập 1 giờ ở cấp người bán × số giờ bên dưới). |

#### Tự động đánh & hồi sinh (`tu_dong`)

Giới hạn Tự động đánh (chỉ khi online) và giá hồi sinh tại chỗ. Luật an toàn (cấm ở PvP / Gym / boss, nhịp, bán kính…) đã cố định.

| Khoá | Tên | Kiểu | Mặc định | Giới hạn | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|---|
| `tu_dong.gio_moi_ngay` | Giờ Tự động đánh mỗi ngày | số | `5` | 0..24 | ngay | game | Miễn phí, chỉ tính lúc online và đang bật. Đặt lại lúc 00:00. |
| `tu_dong.gio_them_the_thang` | Giờ cộng thêm khi có thẻ tháng | số | `2` | 0..24 | ngay | game | Cộng vào giờ mỗi ngày khi thẻ tháng còn hạn. |
| `tu_dong.cap_toi_thieu` | Cấp tối thiểu để bật | số | `0` | 0..1000 | ngay | game | 0 = mọi cấp. Dưới cấp này bấm Bắt đầu thì được báo rõ cấp cần. |
| `tu_dong.danh_boss` | Cho đánh boss | cờ 0/1 | `0` |  | ngay | game | 0 = không chọn boss / Pokémon huyền thoại làm mục tiêu (đánh trúng boss vẫn dừng theo tu_dong.tat_khi_pvp). |
| `tu_dong.map_cam` | Bản đồ cấm Tự động đánh thêm (id) | chuỗi | *(trống)* | ≤ 255 ký tự | ngay | game | Ngoài sàn đấu / Gym / chiến trường / mê cung / bản đồ boss. Cách nhau dấu phẩy; trống = không. |
| `tu_dong.hoi_sinh_tra_phi` | Cho tự dùng vật phẩm hồi sinh trả phí | cờ 0/1 | `0` |  | ngay | game | 1 = khi người chơi đồng ý riêng (bảng Tự động: Tự hồi sinh "cả vật phẩm lượng") mới được tự dùng Thuốc hồi sinh tối đa (lượng khoá). 0 = chỉ Thuốc hồi sinh thường; không bao giờ tự trả xu / lượng. |
| `hoi_sinh.xu_phan_tram_gio` | Phí xu tại chỗ (% thu nhập 1 giờ) | số | `40` | 0..1000 | ngay | game | Theo cấp (ThongSoLauDai.xuMoiGio). 40 = 0,4 giờ cày. Nhân hệ số gục lặp. |
| `hoi_sinh.xu_toi_thieu` | Phí xu tại chỗ tối thiểu | số | `200` | 0..100000000 | ngay | game | Xu. |
| `hoi_sinh.luong_khoa` | Phí lượng khoá tại chỗ | số | `2` | 0..1000 | ngay | game | 0 = tắt lựa chọn trả lượng khoá. Nhân hệ số gục lặp (làm tròn lên). |
| `hoi_sinh.luong_khoa_toi_da` | Trần phí lượng khoá | số | `10` | 0..1000 | ngay | game | Lượng khoá tối đa một lần. |
| `hoi_sinh.hoi_chieu_giay` | Hồi chiêu hồi sinh tại chỗ (giây) | số | `60` | 0..3600 | ngay | game | Giữa hai lần hồi sinh tại chỗ. Về Trung tâm luôn được. |
| `hoi_sinh.tan_thu_cap` | Bảo vệ tân thủ dưới cấp | số | `15` | 0..100 | ngay | game | Dưới cấp này: không mất EXP / độ bền / Mệt mỏi, hồi sinh tại chỗ miễn phí. |
| `hoi_sinh.gia_thuoc_phan_tram_gio` | Giá Thuốc hồi sinh (% thu nhập 1 giờ) | số | `30` | 0..1000 | ngay | game | Giá ở cửa hàng NPC theo cấp người mua (cùng bậc phí xu tại chỗ). 0 = giá gốc data_potion. |
| `hoi_sinh.gia_thuoc_toi_thieu` | Giá Thuốc hồi sinh tối thiểu | số | `407` | 0..100000000 | ngay | game | Xu / viên (không thấp hơn giá gốc). |
| `ve_lang.moi_ngay` | Về làng miễn phí: lượt mỗi ngày | số | `1` | 0..10 | ngay | game | Số lượt Về làng miễn phí (Sổ tay huấn luyện) mỗi ngày lịch máy chủ, không cộng dồn sang ngày sau. Lưu trong hồ sơ dài hạn: đăng nhập lại / khởi động lại không đặt lại. 0 = chỉ còn lượt thắng Gym. |
| `ve_lang.thang_gym` | Về làng miễn phí: lượt thêm mỗi trận thắng Gym | số | `1` | 0..10 | ngay | game | Cộng vào lượt thêm đang giữ mỗi lần thắng một trận Gym (lần đầu hoặc đánh lại tuần). 0 = tắt. |
| `ve_lang.toi_da` | Về làng miễn phí: giữ tối đa lượt thêm | số | `3` | 0..99 | ngay | game | Trần lượt thêm (từ thắng Gym) giữ được; lượt mỗi ngày tính riêng. Dùng lượt ngày trước, hết mới trừ lượt thêm. |

#### Máy chủ, đăng ký & bảo trì (`bao_tri`)

Bảo trì, đăng ký, bản client tối thiểu, link website / tải game, GM, ngày khai trương.

| Khoá | Tên | Kiểu | Mặc định | Giới hạn | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|---|
| `sv.ngay_khai_truong` | Ngày khai trương | ngày | *(trống)* |  | khởi động lại | game | yyyy-MM-dd — ngày 0 của lịch mùa. Để trống = theo lau_dai.ngay_khai_truong game tự ghi. |
| `sv.dangKyTrongGame` | Đăng ký trong game | cờ 0/1 | `1` |  | ngay | game | 0 = nút Đăng ký trong game báo 'đăng ký trên web'. |
| `sv.dangKyTuKichHoat` | Tự kích hoạt tài khoản đăng ký trong game | lựa chọn | `web` | `web`, `1`, `0` | ngay | game | web = theo cấu hình web (web.autoActivate). |
| `dang_ky.web.bat` | Đăng ký trên web | cờ 0/1 | `1` |  | ngay | web | 0 = trang Đăng ký từ chối tài khoản mới. |
| `dang_ky.toi_da_gio` | Đăng ký: tối đa tài khoản / mạng / giờ | số | `3` | 1..1000 | ngay | cả hai | Số tài khoản mới tối đa từ một địa chỉ IP (trong game: và một thiết bị) trong 1 giờ gần nhất, đếm gộp web + game. Mạng di động 4G dùng chung IP giữa nhiều người: đừng đặt quá thấp. Mặc định = shared/data/quy-tac-dang-ky.json (hanMuc.moiGio). |
| `dang_ky.toi_da_ngay` | Đăng ký: tối đa tài khoản / mạng / 24 giờ | số | `10` | 1..10000 | ngay | cả hai | Như trên nhưng trong 24 giờ gần nhất. Mặc định = shared/data/quy-tac-dang-ky.json (hanMuc.moiNgay). |
| `sv.maxCharPerAccount` | Số Pokémon tối đa / tài khoản | số | `3` | 1..3 | ngay | game | Client hỗ trợ tối đa 3. |
| `bao_tri.bat` | Chế độ bảo trì đăng nhập | cờ 0/1 | `0` |  | ngay | game | 1 = từ chối đăng nhập mới (người đang chơi không bị đá). Tương đương tệp bao_tri.txt — bật một trong hai là bảo trì. |
| `bao_tri.cau` | Câu thông báo bảo trì | chuỗi | *(trống)* | ≤ 200 ký tự | ngay | game | Trống = câu mặc định. |
| `bao_tri.cho` | Tài khoản được vào khi bảo trì | danh sách chuỗi | *(trống)* | ≤ 255 ký tự | ngay | game | Tên tài khoản (chữ thường), cách nhau dấu phẩy. |
| `sv.gioBaoTri` | Giờ bảo trì hằng ngày | chuỗi | *(trống)* | ≤ 5 ký tự | ngay | game | HH:MM cố định, 'tat' = không tự bảo trì, trống = ngẫu nhiên 05:01–05:30. |
| `sv.website` | Địa chỉ website (hiện trong game) | chuỗi | `https://huyenthoaibaoboi.fun/` | ≤ 200 ký tự | ngay | game |  |
| `sv.linkTaiGame` | Link tải game (hiện trong game) | chuỗi | `https://huyenthoaibaoboi.fun/` | ≤ 200 ký tự | ngay | game |  |
| `sv.adminAccounts` | Danh sách GM trong game | danh sách chuỗi | *(trống)* | ≤ 255 ký tự | ngay | game | Tên Pokémon GM, cách nhau dấu phẩy. |
| `sv.banToiThieu` | Bản tối thiểu (bắt buộc cập nhật) | số | `0` | 0..1000000 | ngay | game | Số bản (BAN, vd 907). Client gửi số bản nhỏ hơn bị chặn đăng nhập, hiện hộp Cần cập nhật + nút Tải bản mới. Chỉ chặn nền tảng đã có bản tải đạt mức này ở trang Bản tải game. 0 = tắt. |
| `sv.banMoiNhat` | Bản mới nhất (dự phòng) | số | `0` | 0..1000000 | ngay | game | Bản mới nhất lấy theo số bản từng nền tảng ở trang Bản tải game (/admin/tai-game). Ô này chỉ dùng khi bản tải chưa ghi số bản. 0 = chỉ theo trang Bản tải game. |

#### Huấn luyện viên AI (bot) (`hlv_ai`)

Bot lấp máy chủ chơi như người (tên có tiền tố, mặc định "ai "): số lượng, lịch online, tỉ lệ dòng / tính cách, cấp mục tiêu, bản đồ, hoạt động và luật chống lạm phát (hệ số thu nhập, trần xu / túi, giá bán tối thiểu, hạn mức chuyển cho người thật mỗi ngày). Trang riêng: /admin/hlv-ai.

| Khoá | Tên | Kiểu | Mặc định | Giới hạn | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|---|
| `hlv_ai.bat` | Bật Huấn luyện viên AI | cờ 0/1 | `0` |  | ngay | game | 1 = dịch vụ bot (tiến trình riêng htbb-hlv-ai) giữ đủ bot online theo lịch; 0 = mọi bot thoát dần trong vài phút. |
| `hlv_ai.so_luong` | Số Huấn luyện viên AI | số | `30` | 0..500 | ngay | game | Số tài khoản bot tối đa (giờ cao điểm 100% = ngần này bot online). Thiếu thì dịch vụ tự tạo tài khoản (cờ la_bot). |
| `hlv_ai.lich_gio` | Lịch online theo giờ (%) | danh sách số | `30,20,15,10,8,8,12,20,30,40,45,50,55,50,45,50,60,70,85,100,100,90,70,45` | 24 số, 0..100 | ngay | game | 24 số (0h..23h): % số bot online ở mỗi giờ; mỗi bot có phiên dài ngắn ngẫu nhiên, nghỉ, treo máy. |
| `hlv_ai.ty_le_lop` | Tỉ lệ dòng khởi đầu | danh sách số | `20,20,20,20,20` | 5 số, 0..100 | ngay | game | 5 trọng số: Charmander, Bulbasaur, Eevee, Squirtle, Pichu (bot mới chọn dòng theo tỉ lệ này). |
| `hlv_ai.ty_le_tinh_cach` | Tỉ lệ tính cách | danh sách số | `30,15,15,20,20` | 5 số, 0..100 | ngay | game | 5 trọng số: cày cấp, PvP, buôn bán, xã hội, chơi thường - quyết định hoạt động hay làm. |
| `hlv_ai.cap_muc_tieu` | Cấp mục tiêu (thấp, cao) | danh sách số | `10,60` | 2 số, 1..200 | ngay | game | Mỗi bot nhận một cấp mục tiêu trong khoảng này; tới cấp thì thôi cày EXP, chuyển sang PvP / chợ / xã hội / treo. |
| `hlv_ai.ban_do` | Bản đồ ưu tiên (id) | danh sách chuỗi | *(trống)* | ≤ 255 ký tự | ngay | game | Bãi luyện bot ưu tiên khi cày, cách nhau dấu phẩy; trống = rải đều mọi bãi hợp cấp. |
| `hlv_ai.hoat_dong` | Hoạt động được bật | danh sách chuỗi | `nhiem_vu,cay,treo,gym,boss,pk,dau_truong,pho_ban,doi,chat,ban_be,giao_dich,cho,sinh_thai,vuon,nha_tre,bat,nang_cap,su_kien` | ≤ 255 ký tự | ngay | game | Danh sách hoạt động bot được làm: nhiem_vu, cay, treo, gym, boss, pk, dau_truong, pho_ban, doi, chat, ban_be, giao_dich, cho, sinh_thai, vuon, nha_tre, bat, nang_cap, su_kien. Bỏ một mục = tắt hoạt động đó. |
| `hlv_ai.he_so_thu_nhap` | Hệ số thu nhập xu của bot (%) | số | `60` | 0..100 | ngay | game | Xu sinh ra (quái, nhiệm vụ, thưởng…) bot nhận = ngần này % người thật. Không áp cho tiền mua bán với người chơi. |
| `hlv_ai.tran_xu` | Trần xu mỗi bot | số | `300000` | 0..2000000000 | ngay | game | Số dư xu tối đa của một bot; phần vượt bị đốt (ghi tob_hlv_ai_kinh_te_ngay.xu_dot). |
| `hlv_ai.tran_vat_pham` | Trần ô túi mỗi bot | số | `60` | 0..500 | ngay | game | Số ô hành trang tối đa (không tính đồ đang rao); mỗi phút ô thừa bị huỷ, món rẻ nhất trước. |
| `hlv_ai.gia_ban_toi_thieu` | Giá rao tối thiểu của bot (% trung vị) | số | `100` | 50..1000 | ngay | game | Bot rao ở chợ (ký gửi, chợ Pokémon, chợ sinh thái) không dưới ngần này % giá trung vị 7 ngày (ít mẫu: giá tham chiếu) - không phá giá. |
| `hlv_ai.chuyen_moi_nguoi_ngay` | Hạn mức bot → mỗi người thật / ngày | số | `50000` | 0..100000000 | ngay | game | Tổng giá trị (xu) bot chuyển cho MỘT người chơi thật mỗi ngày (mua đồ của họ, thua cược, đổi thuốc / Pokémon…); vượt thì giao dịch bị từ chối. 0 = cấm. |
| `hlv_ai.chuyen_tong_ngay` | Hạn mức bot → người thật / ngày (cả máy chủ) | số | `500000` | 0..2000000000 | ngay | game | Tổng giá trị mọi bot chuyển cho người chơi thật mỗi ngày (kể cả góp quỹ đội có người thật). |
| `hlv_ai.doi_bot_toi_da` | Số đội do bot làm chủ tối đa | số | `3` | 0..50 | ngay | game | Bot cấp 50 lập đội không mất 100.000.000 xu / huy hiệu khi số đội do bot làm chủ còn dưới ngần này (0 = bot không lập đội). Lương đội của bot cho người thật vẫn tính vào hạn mức ngày. |
| `hlv_ai.tien_to_ten` | Tiền tố tên bot | chuỗi | `ai ` | ≤ 12 ký tự | ngay | game | Hiện trước tên mọi Huấn luyện viên AI ở mọi chỗ người chơi thấy tên (trên đầu, chat, top, đội, chợ...), gõ tự do 1-12 ký tự, có thể dính liền tên (không cần dấu cách / hai chấm), nhưng PHẢI có chữ AI hoặc BOT để người chơi nhận ra bot; không dùng dấu ":". Giá trị sai: máy chủ dùng mặc định "ai ". |
| `hlv_ai.cap_theo_nguoi` | Cấp mục tiêu theo người chơi thật | cờ 0/1 | `1` |  | ngay | game | 1 = mỗi ngày (và lúc dịch vụ khởi động) tính khoảng cấp từ người chơi thật hoạt động 7 ngày: thấp = phân vị 25%, cao = trung bình top 10 trừ 2 (không quá mức cao của hlv_ai.cap_muc_tieu, không dưới thấp + 5). Bot mới nhận cấp mục tiêu trong khoảng này; bot cũ dưới mức thấp được nâng dần (tối đa +3 / ngày); bot không cày quá mức cao. 0 = dùng hlv_ai.cap_muc_tieu. |

### Khoá ẩn: cố định & luôn bật (293)

Không hiện trên trang quản trị, máy thật luôn chạy giá trị dưới đây. Đổi khẩn cấp: thêm dòng `khoa=gia_tri` vào server.ini (vps/server.ini.tmpl) rồi khởi động lại máy chủ game.

#### Kinh tế: tỉ lệ rơi & EXP (`kinh_te`)

| Khoá | Tên | Lớp | Giá trị | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|
| `sv.chiaXuRoi` | Chia số xu rơi | Cố định (ẩn) | `30` | ngay | game | Xu rơi = xu gốc chia số này (tối thiểu 1). |
| `sv.expvantieu` | Thưởng EXP giao hàng | Cố định (ẩn) | `1` | ngay | game | 1 = bật. |
| `sv.Luckyexp` | Ô EXP rương may mắn | Cố định (ẩn) | `1` | ngay | game | 1 = bật. |
| `sv.ExpQua` | EXP cây ăn quả | Cố định (ẩn) | `1` | ngay | game | 1 = bật (cũng khoá nút tăng tốc cây). |
| `sv.giaXuBoHoaDoVuLan` | Giá xu bó hoa Vu Lan | Cố định (ẩn) | `150000` | ngay | game | Số xu. |
| `sv.tieuNguongMau` | Giao hàng: ngưỡng màu khi nhận | Cố định (ẩn) | `60189,89389,99689,99989,99999` | ngay | game | 5 ngưỡng cộng dồn trên thang 100000 (Trắng..Vàng, còn lại Tím). |
| `sv.tieuHeSoExp` | Giao hàng: hệ số EXP chung (%) | Cố định (ẩn) | `200` | ngay | game | 200 = ×2. |
| `sv.tieuPcExpTheoMau` | Giao hàng: % EXP theo màu | Cố định (ẩn) | `50,100,150,250,250,500,0` | ngay | game | 7 số (Trắng..Tím, số cuối không dùng). |
| `sv.tieuPermilleCaoCap` | Giao hàng: phần nghìn EXP cao cấp | Cố định (ẩn) | `50,70,90,35,55,75,5,25,45` | ngay | game | 9 số (3 khung cấp × 3 màu). |
| `sv.doiTieuNguongMauXu` | Đổi hàng (xu): ngưỡng màu | Cố định (ẩn) | `50545,85545,98945,99945,99995` | ngay | game | 5 ngưỡng cộng dồn. |
| `sv.doiTieuNguongMauLuong` | Đổi hàng (lượng): ngưỡng màu | Cố định (ẩn) | `22890,52890,96890,99890,99990` | ngay | game | 5 ngưỡng cộng dồn. |
| `exp.nang_luong_tich_phut` | Năng lượng tích luỹ tối đa (phút) | Cố định (ẩn) | `180` | ngay | game | Phút năng lượng dồn tối đa khi không chơi. |
| `thue.cho` | Thuế chợ ký gửi (%) | Cố định (ẩn) | `6` | ngay | game | Đốt khỏi nền kinh tế khi bán được. |
| `thue.dang_ban` | Phí đăng bán (%) | Cố định (ẩn) | `1` | ngay | game | Đốt, không hoàn; tối thiểu 1 xu. |
| `thue.chuyen_luong` | Phí chuyển lượng (%) | Cố định (ẩn) | `5` | ngay | game | Làm tròn lên, tối thiểu 1 khi > 0. |
| `thue.tran_chuyen_xu_gio` | Trần chuyển xu / ngày (giờ thu nhập) | Cố định (ẩn) | `5` | ngay | game | Bội số thu nhập 1 giờ ở cấp người gửi. 0.9.27: áp cho ô xu trong cửa sổ giao dịch 1-1 (`real/GiaoDichXu`), cộng chung mọi nhân vật một tài khoản; `0` = tắt giao dịch xu, `< 0` = không giới hạn; HLV AI không tính. |
| `phi.dich_chuyen_phan_tram_gio` | Phí dịch chuyển (% thu nhập 1 giờ) | Cố định (ẩn) | `3` | ngay | game | Tối thiểu 1 xu. |
| `phi.hoi_sinh_phan_tram_gio` | Phí hồi sinh (% thu nhập 1 giờ) | Cố định (ẩn) | `5` | ngay | game | Tối thiểu 1 xu. |
| `phi.ban_npc_phan_tram` | Giá bán đồ cho NPC (% giá) | Cố định (ẩn) | `5` | ngay | game | Không cộng theo cấp cường hoá. |
| `tieu_hao.sua_do_phan_tram_thu_nhap` | Trần phí sửa đồ (% thu nhập 1 giờ / món) | Cố định (ẩn) | `8` | ngay | game | Phí sửa = min(công thức cũ, trần × số món). |
| `cau_ca.cho_min_giay` | Câu cá: chờ cá cắn tối thiểu (giây) | Cố định (ẩn) | `6` | ngay | game | Sau khi thả câu, cá cắn câu sau ngẫu nhiên [tối thiểu, tối đa] giây (server quyết định). |
| `cau_ca.cho_max_giay` | Câu cá: chờ cá cắn tối đa (giây) | Cố định (ẩn) | `15` | ngay | game | Nhỏ hơn tối thiểu thì dùng tối thiểu. |
| `cau_ca.cho_them_ho_lon_giay` | Câu cá: chờ thêm ở hồ lớn (giây) | Cố định (ẩn) | `3` | ngay | game | Hồ lớn (có cá hiếm) chờ lâu hơn ngần này giây (cả hai đầu khoảng). |
| `cau_ca.cua_so_ms` | Câu cá: khung kéo cần (ms) | Cố định (ẩn) | `1500` | ngay | game | Sau dấu "Cá cắn câu!", phải kéo cần trong ngần này mili giây; sớm hoặc muộn thì cá thoát. |
| `cau_ca.tre_mang_ms` | Câu cá: bù trễ mạng (ms) | Cố định (ẩn) | `700` | ngay | game | Lệnh kéo cần tới server muộn thêm tối đa ngần này ms vẫn tính đúng lúc (bù đường truyền hai chiều). |
| `cau_ca.trong_phan_tram` | Câu cá: tỉ lệ không câu được gì (%) | Cố định (ẩn) | `30` | ngay | game | Kéo đúng lúc vẫn có ngần này % không được gì (mồi và thể lực vẫn mất). |
| `cau_ca.hoi_giay` | Câu cá: nghỉ giữa hai lần thả (giây) | Cố định (ẩn) | `3` | ngay | game | Sau khi một lần câu kết thúc, chờ ngần này giây mới thả câu tiếp. |
| `cau_ca.giam_tu_lan_gio` | Câu cá: số lần câu được mỗi giờ trước khi giảm | Cố định (ẩn) | `30` | ngay | game | Trong một giờ đồng hồ, câu được quá ngần này lần thì tỉ lệ không được gì tăng dần (chống cày). |
| `cau_ca.giam_moi_lan_phan_tram` | Câu cá: tăng tỉ lệ trượt mỗi lần vượt (%) | Cố định (ẩn) | `2` | ngay | game | Mỗi lần câu được vượt mốc trong giờ cộng ngần này % vào tỉ lệ không được gì. |
| `cau_ca.trong_toi_da_phan_tram` | Câu cá: tỉ lệ không được gì tối đa (%) | Cố định (ẩn) | `75` | ngay | game | Trần của tỉ lệ không được gì sau khi cộng giảm theo giờ. |

#### Sự kiện & giftcode (`su_kien`)

| Khoá | Tên | Lớp | Giá trị | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|
| `sv.suKienTuKhoiDongLai` | Tự khởi động lại khi đổi sự kiện | Cố định (ẩn) | `1` | ngay | game | 1 = lệnh bật/tắt sự kiện có hẹn sẽ tự khởi động lại máy chủ. |
| `sv.suKienGomGiay` | Gom lệnh sự kiện (giây) | Cố định (ẩn) | `120` | ngay | game | Gom nhiều lệnh thành một lần khởi động lại. |
| `sv.suKienBaoTruocGiay` | Báo trước khi đổi sự kiện (giây) | Cố định (ẩn) | `60` | ngay | game |  |
| `sv.worldCupBatDau` | World Cup bắt đầu | Cố định (ẩn) | *(trống)* | ngay | game | yyyy-MM-dd HH:mm:ss. Trống = tắt. |
| `sv.worldCupKetThuc` | World Cup kết thúc | Cố định (ẩn) | *(trống)* | ngay | game | Trống = tắt. |
| `sv.worldCupHanNhanQua` | World Cup hạn nhận quà | Cố định (ẩn) | *(trống)* | ngay | game | Trống = tắt. |
| `sv.isSuKienTrungThul2016` | Sự kiện cũ: Trung thu 2016 | Cố định (ẩn) | `0` | khởi động lại | game | Cờ sự kiện gốc (đọc lúc khởi động). |
| `sv.isSuKienTet2017` | Sự kiện cũ: Tết 2017 | Cố định (ẩn) | `0` | khởi động lại | game |  |
| `sv.isSuKienGioTo2016` | Sự kiện cũ: Giỗ Tổ 2016 | Cố định (ẩn) | `0` | khởi động lại | game |  |
| `sv.isSuKienNoel2023` | Sự kiện cũ: Noel 2023 | Cố định (ẩn) | `0` | khởi động lại | game |  |
| `sv.isSuKienTetduonglich2024` | Sự kiện cũ: Tết dương lịch 2024 | Cố định (ẩn) | `0` | khởi động lại | game |  |

#### Nạp tiền (`nap_tien`)

| Khoá | Tên | Lớp | Giá trị | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|
| `nap.hoan_tien.thu_cong` | Cho phép hoàn tiền thủ công | Luôn bật (ẩn) | `1` | ngay | web | 0 = khoá nút Hoàn tiền / Chargeback ở trang Nạp tiền. |
| `nap.hoan_tien.tu_dong` | Tự động thu hồi khi hoàn đơn đã cộng | Luôn bật (ẩn) | `1` | ngay | game | 1 = máy chủ game tự chuyển lượng thành nợ / trừ thẻ tháng / gỡ vé mùa cho đơn 'chờ thu hồi'. 0 = đơn đứng chờ, xử lý tay. |

#### Thành viên & kích hoạt (`thanh_vien`)

| Khoá | Tên | Lớp | Giá trị | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|
| `kich_hoat.thanh_vien_la_kich_hoat` | Thành viên được coi như đã kích hoạt | Luôn bật (ẩn) | `1` | ngay | cả hai | 1 (mặc định) = tài khoản là Thành viên HTBB không bị khoá vì chưa kích hoạt (gộp hai bậc làm một nếu muốn). |

#### Chống clone & chuyển xu (`chong_clone`)

| Khoá | Tên | Lớp | Giá trị | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|
| `chong_clone.nghi_so_nguoi_gui` | Ngưỡng "nghi clone" (số người gửi / ngày) | Cố định (ẩn) | `3` | ngay | game | Một người nhận có từ N người gửi khác nhau trong ngày thì đánh dấu nghi (không chặn), hiện ở /admin/kinh-te. |
| `chong_clone.ky_gui_gia_toi_da_gio` | Giá ký gửi tối đa (giờ thu nhập) | Cố định (ẩn) | `200` | ngay | game | Trần cho món không có giá gốc (và sàn của trần trên). |
| `chong_clone.ky_gui_gia_toi_thieu_phan_tram` | Giá ký gửi tối thiểu (% giá gốc) | Cố định (ẩn) | `5` | ngay | game | Chặn bán phá giá cho clone. |
| `chong_clone.ban_npc_do_khoa` | Bán đồ / nguyên liệu khoá cho NPC lấy xu | Cố định (ẩn) | `0` | ngay | game | 0 = đồ khoá chỉ bán ở giá thấp (chong_clone.ban_npc_khoa_phan_tram). 1 = bán giá NPC thường như cũ. |
| `chong_clone.ban_npc_khoa_phan_tram` | Giá bán đồ / nguyên liệu khoá cho NPC (% giá NPC thường) | Cố định (ẩn) | `25` | ngay | game | 0.9.8: đồ khoá (phần thưởng khoá, mua bằng lượng khoá) bán NPC ở giá thấp = số này % giá NPC thường (mặc định 25% = 1/4, khớp thẻ vật phẩm client; trần cứng 25%; giá NPC thường đã là 5% giá mẫu). 0 = không bán. Thuốc không bán. Vẫn không giao dịch / vứt cho người khác. |

#### Vòng quay may mắn (cố định) (`vong_quay`)

| Khoá | Tên | Lớp | Giá trị | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|
| `vong_quay.giay_moi_vong` | Thời gian một lượt (giây) | Cố định (ẩn) | `120` | ngay | game | Đếm từ lúc có người đặt đầu tiên. Hết giờ: từ 2 người trở lên thì quay, chỉ 1 người thì hoàn lại đủ. |
| `vong_quay.giay_khoa` | Khoá đặt cuối lượt (giây) | Cố định (ẩn) | `10` | ngay | game | Mấy giây cuối không nhận thêm cược. |
| `vong_quay.thuong.bat` | Vòng quay thường: bật | Cố định (ẩn) | `1` | ngay | game | 0 = đóng (người đang chờ vẫn được quay / hoàn như thường). |
| `vong_quay.thuong.cap_toi_thieu` | Vòng quay thường: cấp tối thiểu | Cố định (ẩn) | `20` | ngay | game | Luôn cộng thêm cổng chống clone (chong_clone.cap_toi_thieu, tuổi tài khoản, giờ chơi): cấp hiệu lực = max(hai số). |
| `vong_quay.thuong.cuoc_toi_thieu` | Vòng quay thường: đặt tối thiểu mỗi lượt (xu) | Cố định (ẩn) | `1000` | ngay | game | Mỗi người đặt 1 lần / lượt. |
| `vong_quay.thuong.cuoc_toi_da` | Vòng quay thường: đặt tối đa mỗi lượt (xu) | Cố định (ẩn) | `10000` | ngay | game | Trần hũ = số này × số người tối đa. |
| `vong_quay.thuong.cuoc_ngay_toi_da` | Vòng quay thường: tổng đặt tối đa mỗi ngày (xu) | Cố định (ẩn) | `30000` | ngay | game | Mỗi Pokémon của người chơi, tính theo ngày. |
| `vong_quay.thuong.luot_ngay_toi_da` | Vòng quay thường: số lượt tối đa mỗi ngày | Cố định (ẩn) | `10` | ngay | game | Mỗi Pokémon của người chơi. |
| `vong_quay.thuong.so_nguoi_toi_da` | Vòng quay thường: số người tối đa mỗi lượt | Cố định (ẩn) | `20` | ngay | game |  |
| `vong_quay.thuong.phi_phan_tram` | Vòng quay thường: phí hũ (%) | Cố định (ẩn) | `10` | ngay | game | Phần trăm hũ bị huỷ (hố hút xu) khi có người thắng. Tối thiểu 5% để vòng quay không thành đường chuyển xu rẻ hơn giao dịch. |
| `vong_quay.vip.bat` | Vòng quay VIP: bật | Cố định (ẩn) | `1` | ngay | game | 0 = đóng (người đang chờ vẫn được quay / hoàn như thường). |
| `vong_quay.vip.cap_toi_thieu` | Vòng quay VIP: cấp tối thiểu | Cố định (ẩn) | `50` | ngay | game | Luôn cộng thêm cổng chống clone (chong_clone.cap_toi_thieu, tuổi tài khoản, giờ chơi): cấp hiệu lực = max(hai số). |
| `vong_quay.vip.cuoc_toi_thieu` | Vòng quay VIP: đặt tối thiểu mỗi lượt (xu) | Cố định (ẩn) | `10000` | ngay | game | Mỗi người đặt 1 lần / lượt. |
| `vong_quay.vip.cuoc_toi_da` | Vòng quay VIP: đặt tối đa mỗi lượt (xu) | Cố định (ẩn) | `50000` | ngay | game | Trần hũ = số này × số người tối đa. |
| `vong_quay.vip.cuoc_ngay_toi_da` | Vòng quay VIP: tổng đặt tối đa mỗi ngày (xu) | Cố định (ẩn) | `100000` | ngay | game | Mỗi Pokémon của người chơi, tính theo ngày. |
| `vong_quay.vip.luot_ngay_toi_da` | Vòng quay VIP: số lượt tối đa mỗi ngày | Cố định (ẩn) | `10` | ngay | game | Mỗi Pokémon của người chơi. |
| `vong_quay.vip.so_nguoi_toi_da` | Vòng quay VIP: số người tối đa mỗi lượt | Cố định (ẩn) | `20` | ngay | game |  |
| `vong_quay.vip.phi_phan_tram` | Vòng quay VIP: phí hũ (%) | Cố định (ẩn) | `10` | ngay | game | Phần trăm hũ bị huỷ (hố hút xu) khi có người thắng. Tối thiểu 5% để vòng quay không thành đường chuyển xu rẻ hơn giao dịch. |
| `vong_quay.luong.bat` | Vòng quay lượng: bật | Cố định (ẩn) | `1` | ngay | game | 0 = đóng (người đang chờ vẫn được quay / hoàn như thường). |
| `vong_quay.luong.cap_toi_thieu` | Vòng quay lượng: cấp tối thiểu | Cố định (ẩn) | `20` | ngay | game | Luôn cộng thêm cổng chống clone (chong_clone.cap_toi_thieu, tuổi tài khoản, giờ chơi): cấp hiệu lực = max(hai số). |
| `vong_quay.luong.cuoc_toi_thieu` | Vòng quay lượng: đặt tối thiểu mỗi lượt (lượng) | Cố định (ẩn) | `10` | ngay | game | Mỗi người đặt 1 lần / lượt. |
| `vong_quay.luong.cuoc_toi_da` | Vòng quay lượng: đặt tối đa mỗi lượt (lượng) | Cố định (ẩn) | `100` | ngay | game | Trần hũ = số này × số người tối đa. |
| `vong_quay.luong.cuoc_ngay_toi_da` | Vòng quay lượng: tổng đặt tối đa mỗi ngày (lượng) | Cố định (ẩn) | `300` | ngay | game | Mỗi Pokémon của người chơi, tính theo ngày. |
| `vong_quay.luong.luot_ngay_toi_da` | Vòng quay lượng: số lượt tối đa mỗi ngày | Cố định (ẩn) | `10` | ngay | game | Mỗi Pokémon của người chơi. |
| `vong_quay.luong.so_nguoi_toi_da` | Vòng quay lượng: số người tối đa mỗi lượt | Cố định (ẩn) | `20` | ngay | game |  |
| `vong_quay.luong.phi_phan_tram` | Vòng quay lượng: phí hũ (%) | Cố định (ẩn) | `15` | ngay | game | Phần trăm hũ bị huỷ (hố hút lượng) khi có người thắng. Tối thiểu 5% để vòng quay không thành đường chuyển lượng rẻ hơn giao dịch. |

#### Thách đấu cược xu & lì xì (cố định) (`cuoc_xu`)

| Khoá | Tên | Lớp | Giá trị | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|
| `thach_dau.bat` | Thách đấu cược xu: bật | Cố định (ẩn) | `1` | ngay | game | 0 = không nhận trận cược mới (trận đang đánh vẫn chia như thường). |
| `thach_dau.cap_toi_thieu` | Thách đấu cược xu: cấp tối thiểu | Cố định (ẩn) | `40` | ngay | game | Mọi người trong trận (1-1 hoặc cả hai nhóm). Luôn cộng thêm cổng chống clone: cấp hiệu lực = max(hai số). |
| `thach_dau.cuoc_toi_thieu` | Thách đấu cược xu: cược tối thiểu (xu) | Cố định (ẩn) | `1000` | ngay | game | Mỗi bên. |
| `thach_dau.cuoc_toi_da` | Thách đấu cược xu: cược tối đa (xu) | Cố định (ẩn) | `100000` | ngay | game | Trần tuyệt đối mỗi bên; trần thật = min(số này, số giờ dưới × thu nhập 1 giờ ở cấp thấp nhất trong trận). |
| `thach_dau.cuoc_toi_da_gio` | Thách đấu cược xu: cược tối đa (giờ thu nhập) | Cố định (ẩn) | `2` | ngay | game | Theo cấp thấp nhất trong trận (LauDai.xuMoiGio). |
| `thach_dau.phi_phan_tram` | Thách đấu cược xu: phí bên thắng (%) | Cố định (ẩn) | `10` | ngay | game | Phần trăm của 2 × cược bị huỷ (hố hút) khi có bên thắng; hoà thì hoàn đủ. Tối thiểu 5% để thua cố ý không rẻ hơn giao dịch. |
| `li_xi.tui_nhan_ngay_toi_da` | Bao lì xì: nhận tối đa / ngày | Cố định (ẩn) | `2` | ngay | game | Mỗi Pokémon của người chơi. |
| `li_xi.tui_mo_ngay_toi_da` | Bao lì xì: mở tối đa / ngày | Cố định (ẩn) | `3` | ngay | game | Mỗi Pokémon của người chơi (bao nhận qua giao dịch cũng tính). Vượt thì trả lại bao. |
| `li_xi.tui_mo_dot_toi_da` | Bao lì xì: mở tối đa cả đợt Tết | Cố định (ẩn) | `15` | ngay | game | Mỗi Pokémon của người chơi, đợt = năm. |
| `li_xi.tui_xu_phan_tram_gio_min` | Bao lì xì: xu thấp nhất (% giờ thu nhập) | Cố định (ẩn) | `5` | ngay | game | % thu nhập 1 giờ ở cấp người mở. |
| `li_xi.tui_xu_phan_tram_gio_max` | Bao lì xì: xu cao nhất (% giờ thu nhập) | Cố định (ẩn) | `10` | ngay | game |  |
| `li_xi.tui_giai_lon_phan_tram_gio` | Bao lì xì: giải lớn (% giờ thu nhập) | Cố định (ẩn) | `100` | ngay | game | Trước: 1.000.000 – 5.000.000 xu. Tối đa 300% (kẹp trong code); 5 giải / đợt toàn máy chủ. |
| `li_xi.ma_luong_khoa_min` | Mã lì xì: lượng khoá thấp nhất | Cố định (ẩn) | `5` | ngay | game | Trả lượng KHOÁ (không giao dịch). |
| `li_xi.ma_luong_khoa_max` | Mã lì xì: lượng khoá cao nhất | Cố định (ẩn) | `15` | ngay | game |  |
| `li_xi.ma_tai_khoan_moi_may` | Mã lì xì: tài khoản / thiết bị / IP / đợt | Cố định (ẩn) | `1` | ngay | game | Mỗi tài khoản 1 mã / đợt; mỗi thiết bị và mỗi IP tối đa N tài khoản / đợt. |
| `li_xi.ma_tong_luong_khoa_dot` | Mã lì xì: tổng lượng khoá / đợt (cả máy chủ) | Cố định (ẩn) | `20000` | ngay | game | Hết quỹ thì mã còn lại không nhận được. |

#### Tự động đánh & hồi sinh (`tu_dong`)

| Khoá | Tên | Lớp | Giá trị | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|
| `tu_dong.bat` | Bật Tự động đánh | Luôn bật (ẩn) | `1` | ngay | game | 0 = tạm khoá (người đang dùng bị dừng ở lần kiểm tiếp theo). |
| `tu_dong.chi_map_da_mo` | Chỉ ở bãi luyện tập | Cố định (ẩn) | `1` | ngay | game | 1 = chỉ bật ở bản đồ có Pokémon hoang dã đã mở (không ở thành phố, nhà, sàn đấu, sự kiện). |
| `tu_dong.tat_khi_pvp` | Dừng khi PvP / sàn đấu / Gym / boss | Cố định (ẩn) | `1` | ngay | game | 1 = dừng khi bị Pokémon khác tấn công, bật PK, vào sàn đấu / Gym / chiến trường hay đánh boss. |
| `tu_dong.tu_dung_thuoc` | Cho tự uống thuốc | Luôn bật (ẩn) | `1` | ngay | game | 0 = Tự động đánh không tự uống thuốc hồi phục (người chơi tự bấm). |
| `tu_dong.ban_kinh` | Bán kính tối đa (px) | Cố định (ẩn) | `240` | ngay | game | Pokémon chỉ đánh quanh điểm bật trong bán kính này; ra xa hơn (+64 px) thì dừng. 1 ô = 16 px. |
| `tu_dong.nhip_danh` | Nhịp đánh tối đa khi tự động | Cố định (ẩn) | `12/5` | ngay | game | số lệnh đánh / giây (vd 12/5). Vượt thì bỏ lệnh và tính vi phạm. |
| `tu_dong.nhip_thuoc` | Nhịp uống thuốc tối đa khi tự động | Cố định (ẩn) | `4/5` | ngay | game |  |
| `tu_dong.nhip_nhat` | Nhịp nhặt đồ tối đa khi tự động | Cố định (ẩn) | `8/5` | ngay | game |  |
| `tu_dong.nhip_nguoi` | Nhịp đánh nghi bot (mọi người chơi) | Cố định (ẩn) | `30/5` | ngay | game | Chỉ ghi nghi vấn (hack.txt), không chặn. |
| `tu_dong.vi_pham_tat` | Số vi phạm nhịp thì dừng | Cố định (ẩn) | `20` | ngay | game | Dừng Tự động đánh + ghi nghi vấn. |
| `tu_dong.hoi_sinh_moi_gio` | Tự hồi sinh tối đa / giờ | Cố định (ẩn) | `2` | ngay | game | Khi đang tự động mà Pokémon gục: chỉ dùng vật phẩm hồi sinh nếu người chơi bật "Tự hồi sinh", tối đa ngần này lần / 60 phút; còn lại dừng tự động và về Trung tâm Pokémon. 0 = không bao giờ. |
| `tu_dong.ke_hoach` | Máy chủ điều khiển từng bước | Cố định (ẩn) | `1` | ngay | game | 1 = máy chủ chọn mục tiêu, chiêu, thuốc, nhặt đồ, rút lui cho client 0.9.2+ (client chỉ thực hiện). 0 = client tự chọn như bản 0.9.1 (máy chủ vẫn giới hạn giờ / nhịp / vùng). |
| `tu_dong.chenh_cap_tren` | Bỏ Pokémon cao hơn quá N cấp | Cố định (ẩn) | `5` | ngay | game | Không đánh Pokémon hoang dã cao hơn cấp mình quá ngần này. -1 = không giới hạn. |
| `tu_dong.chenh_cap_duoi` | Bỏ Pokémon thấp hơn quá N cấp | Cố định (ẩn) | `0` | ngay | game | 0 = không giới hạn dưới (vẫn đánh Pokémon yếu). |
| `tu_dong.hp_quai_toi_da_pt` | Bỏ Pokémon quá trâu (% HP mình) | Cố định (ẩn) | `500` | ngay | game | Bỏ Pokémon có HP tối đa lớn hơn ngần này % HP tối đa của mình. 0 = tắt. |
| `tu_dong.chon_chieu_theo_he` | Chọn chiêu theo khắc hệ | Cố định (ẩn) | `1` | ngay | game | 1 = điểm chiêu = sát thương × khắc hệ × STAB, bỏ chiêu đang hồi / thiếu PP. 0 = chỉ theo sát thương. |
| `tu_dong.nguong_thuoc_hp` | Ngưỡng thuốc HP mặc định (%) | Cố định (ẩn) | `50` | ngay | game | Uống thuốc HP khi HP dưới ngưỡng người chơi chọn trong bảng; client cũ dùng số này. 0 = máy chủ không cho tự uống thuốc HP. |
| `tu_dong.nguong_thuoc_mp` | Ngưỡng thuốc PP mặc định (%) | Cố định (ẩn) | `30` | ngay | game | Như trên cho PP. 0 = tắt. |
| `tu_dong.nguong_hp_thap` | HP thấp thì rút lui (%) | Cố định (ẩn) | `25` | ngay | game | HP dưới ngưỡng mà hết thuốc: rút về điểm bắt đầu nghỉ (hoặc dừng nếu tu_dong.hp_thap_dung=1). 0 = tắt. |
| `tu_dong.hp_thap_dung` | HP thấp thì dừng hẳn | Cố định (ẩn) | `0` | ngay | game | 1 = dừng Tự động đánh thay vì rút lui nghỉ hồi phục. |
| `tu_dong.hoi_phuc_toi` | Nghỉ hồi phục tới (%) | Cố định (ẩn) | `80` | ngay | game | Rút lui nghỉ tới khi HP đạt ngần này % rồi đánh tiếp. |
| `tu_dong.nhat_ban_kinh` | Bán kính nhặt đồ (px) | Cố định (ẩn) | `96` | ngay | game | Chỉ đi nhặt đồ rơi của mình trong bán kính này quanh Pokémon (và trong vùng). 0 = không nhặt. 1 ô = 16 px. |
| `tu_dong.dung_khi_day_tui` | Dừng khi hành trang đầy | Cố định (ẩn) | `1` | ngay | game | 1 = hành trang đầy thì dừng và báo. |
| `tu_dong.dung_khi_tu_di` | Dừng khi người chơi tự di chuyển | Cố định (ẩn) | `1` | ngay | game | 1 = bấm phím di chuyển / chạm bản đồ thì dừng Tự động đánh. 0 = nhường 3 giây rồi chạy tiếp. |
| `tu_dong.tranh_pk_px` | Tránh người bật PK (px) | Cố định (ẩn) | `160` | ngay | game | Có người chơi khác bật PK trong bán kính này thì lùi về điểm bắt đầu chờ. 0 = tắt. |

#### Máy chủ, đăng ký & bảo trì (`bao_tri`)

| Khoá | Tên | Lớp | Giá trị | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|
| `sv.chiSoMayChu` | Ghi chỉ số máy chủ + đọc bao_tri.txt | Luôn bật (ẩn) | `1` | khởi động lại | game | 0 = tắt luồng chỉ số (tob_chi_so_may_chu). |
| `sv.nhacCapNhat` | Nhắc khi có bản mới | Cố định (ẩn) | `1` | ngay | game | Client cũ hơn bản mới nhất thấy thẻ Đã có bản mới (Cập nhật ngay / Để sau, nhắc một lần mỗi ngày). Không chặn chơi. Cố định bật; tắt khẩn cấp: sv.nhacCapNhat=0 trong server.ini. |

#### Chế độ game (cố định) (`che_do`)

| Khoá | Tên | Lớp | Giá trị | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|
| `sv.lau_dai` | Thiết kế dài hạn | Luôn bật (ẩn) | `1` | khởi động lại | game | 1 = bật GD2 (bảng EXP mới, chuyển EXP Pokémon một lần có sao lưu). Bật/tắt chạy chuyển đổi EXP lúc khởi động. |
| `sv.he_sinh_thai` | Hệ sinh thái | Luôn bật (ẩn) | `1` | khởi động lại | game | 1 = bật hệ sinh thái thế giới (data_hst_*, tob_hst_*). |
| `ban_do.khoa.bat` | Khoá bản đồ theo cấp / cửa ải | Luôn bật (ẩn) | `1` | ngay | game | 1 = bản đồ cần đủ cấp và xong cửa ải (bảng `data_ban_do_mo_khoa`) mới vào được, kèm luật Tự động đánh theo bản đồ; 0 = mở mọi bản đồ (người chơi cũ luôn được miễn trừ theo cấp). |
| `sv.tran_cap_theo_mua` | Trần cấp theo mùa | Cố định (ẩn) | `0` | khởi động lại | game | 1 = bật lại trần cấp 60→100 theo ngày khai trương (thiết kế cũ). 0 = không trần, lên cấp chậm + vật phẩm tăng tốc. |
| `sv.tua_nhanh_ngay` | Tua nhanh thử nghiệm (ngày) | Cố định (ẩn) | `0` | khởi động lại | game | Cộng thêm N ngày vào ngày hiện tại của lịch dài hạn (thử mùa, vé mùa, lịch). chỉ máy thử — máy thật luôn 0. (máy thật chỉ nhận mặc định) |

#### Đăng ký (cố định) (`dang_ky`)

| Khoá | Tên | Lớp | Giá trị | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|
| `sv.dangKyTreMs` | Trễ phản hồi đăng ký (ms) | Cố định (ẩn) | `800` | ngay | game | Làm chậm dò tài khoản. |
| `sv.dangKyCsdlTaiKhoan` | CSDL tài khoản | Cố định (ẩn) | `account` | khởi động lại | game | Tên database chứa team_user. |

#### Chống spam, DoS & nhật ký (cố định) (`chong_spam`)

| Khoá | Tên | Lớp | Giá trị | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|
| `sv.spam.bat` | Chống spam chat / thao tác | Luôn bật (ẩn) | `1` | ngay | game | 0 = tắt toàn bộ. |
| `sv.spam.chat.gan` | Nhịp chat gần | Cố định (ẩn) | `5/10` | ngay | game | số tin / giây (vd 5/10). |
| `sv.spam.chat.rieng` | Nhịp chat riêng | Cố định (ẩn) | `6/10` | ngay | game |  |
| `sv.spam.chat.theGioi` | Nhịp chat thế giới / người | Cố định (ẩn) | `1/30` | ngay | game |  |
| `sv.spam.theGioiToanServer` | Nhịp chat thế giới toàn máy chủ | Cố định (ẩn) | `12/60` | ngay | game |  |
| `sv.spam.chat.doi` | Nhịp chat đội | Cố định (ẩn) | `8/10` | ngay | game |  |
| `sv.spam.chat.bang` | Nhịp chat đội | Cố định (ẩn) | `8/10` | ngay | game |  |
| `sv.spam.trungLapGiay` | Chặn tin trùng lặp (giây) | Cố định (ẩn) | `30` | ngay | game |  |
| `sv.spam.chanQuangCao` | Chặn quảng cáo / link | Cố định (ẩn) | `1` | ngay | game |  |
| `sv.spam.viPhamCam` | Số vi phạm thì cấm chat | Cố định (ẩn) | `4` | ngay | game |  |
| `sv.spam.camGiay` | Thang cấm chat (giây) | Cố định (ẩn) | `60,300,1800,7200` | ngay | game | Các bậc cấm tăng dần, cách nhau dấu phẩy. |
| `sv.chat.capGan` | Chat bản đồ: cấp tối thiểu | Cố định (ẩn) | `5` | ngay | game | Dưới cấp này tin bản đồ không phát (người gửi được báo lý do). Trước 0.9.8 cố định 20 và bỏ im lặng. |
| `sv.chat.capTheGioi` | Chat thế giới: cấp tối thiểu | Cố định (ẩn) | `10` | ngay | game | Kênh Thế giới trong bảng chat (0.9.8). Loa vẫn theo luật loa cũ. |
| `sv.chat.xuTheGioi` | Chat thế giới: phí xu mỗi tin | Cố định (ẩn) | `100` | ngay | game | Hố hút xu nhỏ; 0 = miễn phí. Bị chặn (spam, cấp, giãn cách) thì không trừ. |
| `sv.chat.giayTheGioi` | Chat thế giới: giãn cách (giây) | Cố định (ẩn) | `30` | ngay | game | Mỗi người một tin / N giây; client hiện đếm ngược. sv.spam.chat.theGioi vẫn áp. |
| `sv.chat.capRieng` | Nhắn riêng: cấp tối thiểu | Cố định (ẩn) | `3` | ngay | game | Chặn tài khoản rác mới tạo nhắn riêng hàng loạt. |
| `sv.chat.doDai` | Độ dài tối đa một tin (ký tự) | Cố định (ẩn) | `80` | ngay | game | Kênh thế giới / đội / nhóm / riêng của bảng chat 0.9.8. |
| `sv.chat.lichSu` | Lịch sử kênh gửi khi vào game (dòng) | Cố định (ẩn) | `50` | ngay | game | Số dòng thế giới / đội gần nhất (trong bộ nhớ) gửi cho người vừa vào. |
| `sv.chat.chanToiDa` | Danh sách chặn: tối đa tên / người | Cố định (ẩn) | `100` | ngay | game | tob_chat_chan. |
| `sv.socketTimeout` | Hết hạn socket (giây) | Cố định (ẩn) | `180` | ngay | game | Áp dụng cho kết nối mới. 0 = tắt. |
| `sv.maxConnPerIp` | Kết nối tối đa / IP | Cố định (ẩn) | `50` | ngay | game | 0 = tắt. |
| `sv.connRateMax` | Số kết nối tối đa / cửa sổ | Cố định (ẩn) | `60` | ngay | game | 0 = tắt. |
| `sv.connRateWindow` | Cửa sổ đếm kết nối (giây) | Cố định (ẩn) | `30` | ngay | game | 0 = tắt. |
| `sv.dosBanSec` | Thời gian chặn IP ngập (giây) | Cố định (ẩn) | `300` | ngay | game |  |
| `sv.reloginWaitSec` | Chờ đăng nhập lại (giây) | Cố định (ẩn) | `0` | ngay | game |  |
| `sv.proxyTinCay` | Proxy tin cậy (miễn giới hạn theo IP) | Cố định (ẩn) | `127.0.0.1,::1` | ngay | game | IP hoặc dải CIDR (IPv4/IPv6) của WAF / Nova agent, cách nhau dấu phẩy. Kết nối từ đây không bị tính sv.maxConnPerIp, sv.connRate*, chặn sv.dosBanSec, chờ đăng nhập theo IP, hạn mức đăng ký theo IP. Giới hạn theo tài khoản / kết nối / thiết bị vẫn giữ. |
| `sv.dangNhapIpNguong` | Ngưỡng đăng nhập dồn dập / IP | Cố định (ẩn) | `10` | ngay | game | Số lần đăng nhập trong ~1 giây từ một IP (không phải proxy tin cậy) thì bắt đầu chờ + ghi nghi vấn. 0 = tắt. |
| `sv.dangNhapIpChoMs` | Chờ khi đăng nhập dồn dập (ms) | Cố định (ẩn) | `3000` | ngay | game | Thời gian chờ mỗi lần đăng nhập khi vượt sv.dangNhapIpNguong. 0 = không chờ. |
| `sv.tranGoiThuong` | Trần gói tin thường / cửa sổ | Cố định (ẩn) | `300` | ngay | game |  |
| `sv.tranGoiTaiNguyen` | Trần gói tải tài nguyên / cửa sổ | Cố định (ẩn) | `600` | ngay | game |  |
| `sv.tranGoiTraCuu` | Trần gói tra cứu / cửa sổ | Cố định (ẩn) | `600` | ngay | game |  |
| `sv.log.muc` | Mức nhật ký (màn hình) | Cố định (ẩn) | `THONG_TIN` | ngay | game |  |
| `sv.log.mucTep` | Mức nhật ký (tệp) | Cố định (ẩn) | *(trống)* | ngay | game | Trống = như màn hình. |
| `sv.log.tat` | Loại nhật ký tắt | Cố định (ẩn) | `GOI3,GOI5,GOI26,VKTT,SQL,LUU_CHAR` | ngay | game | Mã loại, cách nhau dấu phẩy. |
| `sv.log.kiemToan` | Loại nhật ký kiểm toán | Cố định (ẩn) | `ADMIN,GM,NAP,NAP_TIEN,GIFTCODE,CAM,BAN,KINH_TE,KINHTE,CHONG_SPAM` | ngay | game | Ghi riêng tệp kiem_toan (giữ lâu). |

#### PvP / Gym / Hầm Ngục (luôn bật) (`pvp`)

| Khoá | Tên | Lớp | Giá trị | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|
| `pvp.tinh_diem.bat` | Tính điểm thang PvP | Luôn bật (ẩn) | `1` | ngay | game | 0 = trận PvP không cộng/trừ điểm thang. |
| `gym.bat` | Thách đấu Gym | Luôn bật (ẩn) | `1` | ngay | game | 0 = tạm đóng Gym. |
| `ham_nguc.bat` | Hầm Ngục bí ẩn | Luôn bật (ẩn) | `1` | ngay | game | 0 = không vào tầng mới (vẫn nhận thưởng đã đạt). |
| `pvp.ngam_tay` | Ngắm tay trong PvP | Luôn bật (ẩn) | `1` | ngay | game | 1 = trận giữa người chơi (PK, thách đấu, xếp hạng, sàn đấu, chiến trường, chiếm Gym) không tự động đánh, không tự nhắm: kéo chiêu để ngắm, chiêu bay thẳng theo hướng kéo (client 0.9.8; bản cũ bắn theo hướng Pokémon đang quay). 0 = như 0.9.7. |
| `pvp.ngam_tay.giay_giao_chien` | Giây coi là đang giao chiến PvP | Cố định (ẩn) | `10` | ngay | game | Đánh trúng / bị người chơi đánh trúng trong ngần này giây thì vẫn tính là trận giữa người chơi (tắt tự động, bắt buộc ngắm tay). |
| `pvp.ngam_tay.bu_tre_ms` | Bù trễ vị trí (ms) | Cố định (ẩn) | `150` | ngay | game | Vị trí mục tiêu trong ngần này ms trước lúc đạn tới đều tính trúng (ưu tiên người bắn khi mạng trễ). Trần 300. |
| `pvp.ngam_tay.map` | Bản đồ PvP bắt buộc ngắm tay (id) | Cố định (ẩn) | `202,204,205,227` | ngay | game | Ngoài sàn đấu / chiến trường / giải Liên minh / thách đấu / chiếm Gym đang đánh (tự nhận theo loại bản đồ). So cả id bản đồ lẫn id nạp. |
| `pho_ban.bat` | Phó bản Hầm ngục (tổ đội 5 tầng) | Luôn bật (ẩn) | `1` | ngay | game | 0 = không mở phòng mới (phòng đang chạy vẫn chạy hết). |
| `pho_ban.luot_ngay` | Phó bản: lượt vào mỗi ngày | Cố định (ẩn) | `3` | ngay | game | Mỗi Pokémon, đếm cả lần vào lại sau khi thua. Thưởng ngày (0,2 giờ EXP + 5 lượng khoá + rương boss) vẫn chỉ 1 lần, chung với Hầm ngục bí ẩn. |
| `pho_ban.phut_toi_da` | Phó bản: thời gian tối đa (phút) | Cố định (ẩn) | `20` | ngay | game | Hết giờ chưa hạ boss tầng 5 thì phòng đóng, không có thưởng ngày. |
| `pho_ban.cap_toi_thieu` | Phó bản: cấp tối thiểu | Cố định (ẩn) | `20` | ngay | game | Mọi thành viên phải đạt cấp này; chênh cấp trong đội tối đa 10. |
| `cup.bat` | Cúp PvP tuần | Luôn bật (ẩn) | `1` | ngay | game | 0 = tạm đóng cúp (không nhận đăng ký, không ghép vòng). Tắt riêng một tuần: /admin/sukien (mã moi:cuptuan, tắt năm nay). |
| `cup.gio_vong_dau` | Cúp: giờ vòng 1 (Chủ nhật) | Cố định (ẩn) | `20` | ngay | game | Giờ Việt Nam. Mỗi 10 phút một vòng; đóng đăng ký trước vòng 1 năm phút. |
| `cup.so_vong` | Cúp: số vòng Thuỵ Sĩ | Cố định (ẩn) | `5` | ngay | game | Vòng ghép tự động theo điểm; hai bên cùng thiết bị / IP / tài khoản không bao giờ gặp nhau. |
| `cup.so_doi_toi_thieu` | Cúp: số đội tối thiểu | Cố định (ẩn) | `4` | ngay | game | Ít hơn số này lúc đóng đăng ký thì cúp tuần đó hoãn (không phát thưởng). |

#### Học chiêu & Điểm huấn luyện (cố định) (`hoc_chieu`)

| Khoá | Tên | Lớp | Giá trị | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|
| `chieu.learnset.bat` | Học chiêu theo loài | Luôn bật (ẩn) | `1` | ngay | game | 1 = chiêu tự học theo cấp của loài, NPC Blaziken dạy đúng loài (data_learnset). 0 = luật cũ (1 điểm kỹ năng để học, mua ô ở NPC). |
| `huan_luyen.bat` | Điểm huấn luyện | Luôn bật (ẩn) | `1` | ngay | game | 1 = chỉ số gốc tự tăng theo cấp + Điểm huấn luyện có trần thay 5 điểm tiềm năng tự do mỗi cấp. Đổi có hiệu lực từ lần vào game kế tiếp của mỗi Pokémon. |
| `huan_luyen.diem_moi_cap` | Điểm huấn luyện mỗi cấp | Cố định (ẩn) | `2` | ngay | game | Tổng = số này × (cấp − 1) + 1 / huy hiệu Gym (trần riêng). Cấp 100: 198 + 16. |
| `huan_luyen.tran_diem_thuong` | Trần điểm thưởng (huy hiệu Gym) | Cố định (ẩn) | `16` | ngay | game | Mỗi huy hiệu Gym +1 Điểm huấn luyện, tối đa số này. |
| `huan_luyen.gia_tay_phan_nghin_gio` | Giá tẩy điểm (‰ giờ thu nhập) | Cố định (ẩn) | `1000` | ngay | game | Lần tẩy đầu miễn phí; sau đó = giờ thu nhập ở cấp hiện tại × số này / 1000 (hố hút). 1000 = 1 giờ. |
| `huan_luyen.hoi_tay_gio` | Chờ giữa hai lần tẩy (giờ) | Cố định (ẩn) | `24` | ngay | game | Không áp cho lần đầu và cho Kẹo tiềm năng. |

#### Chỉ số & chống lạm phát (cố định) (`chi_so`)

| Khoá | Tên | Lớp | Giá trị | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|
| `chi_so.bat` | Bật giới hạn chỉ số | Luôn bật (ẩn) | `1` | ngay | game | 0 = tắt mọi trần/giảm dần chỉ số (quay về công thức cũ). |
| `chi_so.kep_trang_bi` | Kẹp chỉ số trang bị theo ngân sách | Luôn bật (ẩn) | `1` | ngay | game | Mỗi món ≤ chuẩn bậc × 1,30 × nâng cấp; đồ bậc cũ ≤ 130% đồ bậc hiện tại. |
| `chi_so.tran_chi_mang` | Trần tỉ lệ chí mạng (‰) | Cố định (ẩn) | `400` | ngay | game | Phần nghìn, có giảm dần. PvP cố định 300‰. |
| `chi_so.tran_st_chi_mang` | Trần ST chí mạng cộng thêm (‰) | Cố định (ẩn) | `500` | ngay | game | Trên nền ×1,5. 500 = đòn chí mạng tối đa ×2,0. |
| `chi_so.tran_ne` | Trần né tránh (‰) | Cố định (ẩn) | `300` | ngay | game | Phần nghìn, có giảm dần. PvP cố định 200‰. |
| `chi_so.tran_hut` | Trần hút HP PvE (‰ sát thương) | Cố định (ẩn) | `100` | ngay | game | Gộp mọi nguồn hút HP. |
| `chi_so.tran_hut_pvp` | Trần hút HP PvP (‰ sát thương thật) | Cố định (ẩn) | `50` | ngay | game | Gộp mọi nguồn hút HP, tính trên sát thương sau ×0,30. |
| `chi_so.tran_giam_st` | Trần giảm sát thương nhận (‰) | Cố định (ẩn) | `500` | ngay | game | Gộp mọi nguồn thụ động (PvP cố định 400‰). |
| `chi_so.tran_khang_he` | Trần kháng một hệ (‰) | Cố định (ẩn) | `600` | ngay | game | Kháng hệ + kháng mọi hệ + phiến đá. |
| `chi_so.san_sat_thuong` | Sàn sát thương mỗi đòn (‰) | Cố định (ẩn) | `400` | ngay | game | Sau mọi lớp giảm, một đòn còn ít nhất chừng này phần nghìn (chống bất tử). |
| `chi_so.tran_phan_tram_cong` | Trần tổng % cộng thêm (‰) | Cố định (ẩn) | `500` | ngay | game | Tổng % tấn công/phòng thủ/HP từ pet, thú cưỡi, thời trang, bộ, VIP… so với chỉ số gốc. |
| `chi_so.khac_he_pve_ra_toi_thieu` | Khắc hệ PvE: hệ số đòn người chơi tối thiểu (%) | Cố định (ẩn) | `60` | ngay | game | Đòn người chơi vào quái / boss / trưởng Gym bị kháng (×0,7 / ×0,5) hoặc miễn nhiễm (×0,3) không thấp hơn mức này - dòng chỉ có chiêu bị khắc (Pichu chỉ có chiêu Điện gặp quái Đất) vẫn đi tiếp bước hạ quái bắt buộc. 30 = như Pokémon gốc. PvP không đổi. |
| `chi_so.khac_he_pve_ra_toi_da` | Khắc hệ PvE: hệ số đòn người chơi tối đa (%) | Cố định (ẩn) | `150` | ngay | game | Đòn người chơi vào quái / boss / trưởng Gym có lợi thế khắc hệ (×1,5 / ×2) không vượt mức này. 200 = như Pokémon gốc. PvP không đổi. |
| `chi_so.khac_he_pve_vao_toi_da` | Khắc hệ PvE: hệ số đòn quái vào người chơi tối đa (%) | Cố định (ẩn) | `130` | ngay | game | Đòn quái / boss / trưởng Gym có lợi thế khắc hệ vào người chơi (×1,5 / ×2) không vượt mức này; kháng / miễn nhiễm giữ nguyên. Trưởng Gym còn trần riêng gym.he_toi_da. 200 = như Pokémon gốc. PvP không đổi. |
| `danh_hieu.bat` | Bật danh hiệu thành tựu | Cố định (ẩn) | `1` | ngay | game | 0 = ngừng tự mở khoá danh hiệu và tắt dòng chỉ số của danh hiệu đang đeo (danh hiệu đã có vẫn giữ, vẫn hiện trên đầu). Danh mục + điều kiện ở bảng data_danh_hieu. |
| `danh_hieu.tran_phan_nghin` | Trần chỉ số ô danh hiệu (‰) | Cố định (ẩn) | `10` | ngay | game | Ngân sách ô danh hiệu (thiết kế cân bằng 4.5, đề xuất 1%): chỉ một danh hiệu đang đeo được tính, giá trị từng danh hiệu (data_danh_hieu.phan_nghin) bị kẹp về trần này và vào trần tổng trang trí. 10 = +1%. |

#### Trang bị, ngọc hệ, chế tạo & thu phục (cố định) (`trang_bi`)

| Khoá | Tên | Lớp | Giá trị | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|
| `bo.bat` | Bật bộ trang bị cùng bậc | Luôn bật (ẩn) | `1` | ngay | game | 4 / 7 / 11 món cùng bậc (data_noi_dung_bo): +5% HP / +5% tấn công / +3% chí mạng. 0 = tắt thưởng bộ cùng bậc (thưởng bộ nâng cấp cũ vẫn giữ). |
| `bo.tran_phan_tram` | Trần thưởng bộ HP / tấn công (%) | Cố định (ẩn) | `5` | ngay | game | Chung cho bộ cùng bậc + bộ nâng cấp cũ (+10/+13/+15). Thiết kế: ≤ 5%. |
| `bo.tran_chi_mang_phan_nghin` | Trần chí mạng từ bộ (‰) | Cố định (ẩn) | `30` | ngay | game | Bộ 11 món: +3% chí mạng, vẫn qua trần chí mạng chung 40%. |
| `ngoc_he.bat` | Bật ngọc hệ | Luôn bật (ẩn) | `1` | ngay | game | Khảm ngọc 18 hệ vào lỗ trang bị ở Machamp: +% chỉ số món, + sát thương hệ, − kháng hệ khắc. 0 = ngọc đã khảm không còn tác dụng (vẫn giữ trên món). |
| `ngoc_he.tran_phan_tram_mon` | Trần % chỉ số món từ ngọc | Cố định (ẩn) | `10` | ngay | game | Tổng % chỉ số chính của một món từ mọi ngọc trên món. Thiết kế: ≤ 10%. |
| `ngoc_he.st_he_phan_van_moi_cap` | Sát thương hệ mỗi cấp ngọc (phần vạn) | Cố định (ẩn) | `15` | ngay | game | 15 = 1,5‰ mỗi cấp; cộng vào ST hệ, trần chung 20%. |
| `ngoc_he.khang_tru_phan_nghin_moi_cap` | Kháng hệ khắc bị trừ mỗi cấp ngọc (‰) | Cố định (ẩn) | `1` | ngay | game | Phần “hại” của ngọc (Ngọc Lửa → −kháng Nước). Mài ngọc giảm một nửa. |
| `ngoc_he.gia_thao_phan_nghin_gio` | Giá tháo ngọc (‰ giờ thu nhập) | Cố định (ẩn) | `500` | ngay | game | 500 = 0,5 giờ thu nhập ở cấp người chơi. Ngọc giữ cấp, vào túi ngọc. |
| `ngoc_he.gia_mai_phan_nghin_gio` | Giá mài ngọc (‰ giờ thu nhập) | Cố định (ẩn) | `300` | ngay | game | 300 = 0,3 giờ thu nhập. |
| `che_tao.bat` | Bật chế tạo | Luôn bật (ẩn) | `1` | ngay | game | Machamp thợ rèn: công thức data_noi_dung_che_tao, bậc hiếm data_che_tao_bac. |
| `che_tao.pity` | Pity chế tạo Sử thi (lần) | Cố định (ẩn) | `20` | ngay | game | Lần thứ N liên tiếp không ra Sử thi trở lên thì chắc Sử thi. 0 = tắt. |
| `nang_cap.moc_neo` | Mốc neo nâng cấp | Luôn bật (ẩn) | `1` | ngay | game | Đã đạt +7 / +10 / +13 (data_nang_cap_moc) thì nâng hỏng không rơi dưới mốc. 0 = luật cũ (−1 từ +7). |
| `nang_cap.he_so_xu_bac_cao` | Hệ số xu nâng +11..+15 (‰) | Cố định (ẩn) | `1060` | ngay | game | Bù phần kỳ vọng giảm do mốc neo (thiết kế: +6%). Chỉ áp khi bật mốc neo. |
| `bat.bat` | Bật thu phục Pokémon | Luôn bật (ẩn) | `1` | ngay | game | Làm Pokémon hoang dã yếu đi rồi ném bóng (Hộp PC, Kẹo loài, Pokédex “Đã bắt”). 0 = tạm khoá ném bóng / mua bóng. |
| `bat.nguong_hp_phan_tram` | Ngưỡng HP để ném bóng (%) | Cố định (ẩn) | `35` | ngay | game | Pokémon phải còn ≤ chừng này % HP. |
| `bat.tran_ngay` | Trần thu phục / ngày | Cố định (ẩn) | `30` | ngay | game | Số Pokémon bắt được tối đa mỗi người chơi mỗi ngày (chống bot). |
| `bat.tran_nem_ngay` | Trần ném bóng / ngày | Cố định (ẩn) | `150` | ngay | game | Số lần ném tối đa mỗi ngày. |
| `bat.cach_ms` | Nhịp ném tối thiểu (ms) | Cố định (ẩn) | `1500` | ngay | game | Hai lần ném cách nhau ít nhất chừng này (gói đến sớm hơn bị bỏ qua). |
| `bat.so_lan_bo_chay` | Số lần trượt thì Pokémon bỏ chạy | Cố định (ẩn) | `4` | ngay | game | Mỗi lần trượt trên cùng một con: lần sau cao hơn (+25% tỉ lệ, tối đa +5%); đến lần này thì nó bỏ chạy. |
| `bat.su_kien` | Mở loài chỉ bắt trong sự kiện | Cố định (ẩn) | `0` | ngay | game | 1 = cho bắt các loài data_bat_loai.chi_su_kien = 1 (bật trong thời gian sự kiện). |
| `bat.dong_hanh` | Bạn đồng hành đi theo (chỉ hình) | Luôn bật (ẩn) | `1` | ngay | game | Một Pokémon trong Hộp PC đi theo người chơi (0 chỉ số). 0 = ẩn. |
| `bat.giu_moi_loai` | Số con thường giữ mỗi loài | Cố định (ẩn) | `2` | ngay | game | Bắt thêm con thường khi Hộp PC đã có chừng này con cùng loài: con kém nhất (IV thấp) tự chuyển đổi thành Kẹo (+ xu nhỏ). Sắc màu không bao giờ tự chuyển. 0 = tắt. |
| `bat.chuyen_doi.tran_xu_phan_nghin_gio` | Trần xu chuyển đổi / ngày (‰ giờ thu nhập) | Cố định (ẩn) | `300` | ngay | game | Xu nhận từ Chuyển đổi con trùng loài mỗi ngày tối đa chừng này phần nghìn giờ thu nhập (vượt: chỉ ra Kẹo). |
| `bat.chuyen_doi.tran_luong_ngay` | Trần lượng khoá chuyển đổi / ngày | Cố định (ẩn) | `1` | ngay | game | Lượng khoá tối đa mỗi ngày từ Chuyển đổi loài Rất hiếm. |
| `trao_doi.bat` | Bật trao đổi Pokémon | Luôn bật (ẩn) | `1` | ngay | game | Hai người chơi trao đổi Pokémon trong Hộp PC (khoá + xác nhận hai lần, đổi nguyên tử). 0 = tạm khoá. |
| `trao_doi.cap_toi_thieu` | Cấp tối thiểu trao đổi Pokémon | Cố định (ẩn) | `10` | ngay | game | Cả hai bên phải đạt cấp này (ngoài cổng chống clone chung). |
| `trao_doi.tran_ngay` | Trần Pokémon nhận qua trao đổi / ngày | Cố định (ẩn) | `10` | ngay | game | Số Pokémon mỗi người chơi nhận tối đa mỗi ngày qua trao đổi. |
| `trao_doi.khoa_gio` | Khoá trao đổi lại (giờ) | Cố định (ẩn) | `24` | ngay | game | Pokémon vừa nhận qua trao đổi phải chờ chừng này giờ mới trao đổi tiếp (chống chuyền tay). |
| `trao_doi.cach_giay` | Nhịp giữa hai lần trao đổi (giây) | Cố định (ẩn) | `60` | ngay | game | Mỗi người chơi chỉ trao đổi xong một lần mỗi chừng này giây. |
| `trao_doi.phi_phan_nghin_gio` | Phí trao đổi mỗi Pokémon (‰ giờ thu nhập) | Cố định (ẩn) | `50` | ngay | game | Người nhận trả cho mỗi con: chừng này phần nghìn giờ thu nhập ở cấp mình × bậc hiếm (1/2/4/8) × sắc màu 4 × 3 sao 2 (hố hút xu). |
| `trao_doi.phi_tran_phan_nghin_gio` | Trần phí trao đổi mỗi Pokémon (‰ giờ thu nhập) | Cố định (ẩn) | `2000` | ngay | game | Phí một con không vượt chừng này phần nghìn giờ thu nhập. |
| `do_moi.bat` | Bật đồ họ mới (bộ hệ / bộ vùng) | Luôn bật (ẩn) | `1` | ngay | game | Đồ họ (data_trang_bi_moi) thay món gốc khi rơi / chế tạo (cùng chỉ số, không thêm món) và thưởng bộ họ trong trần chung. 0 = không đổi món rơi, không thưởng bộ họ. |
| `trang_phuc.bat` | Bật tủ đồ trang trí (trang phục, hào quang, skin thú, thẻ bạn đồng hành) | Luôn bật (ẩn) | `1` | ngay | game | Gói -116 + hình trên thân (-102) + chỉ số nhỏ theo ô (data_trang_phuc_o, tổng cùng danh hiệu ≤ trang_tri.tran_tong_phan_nghin). 0 = tạm khoá. |
| `cho_pokemon.bat` | Bật chợ Pokémon | Luôn bật (ẩn) | `1` | ngay | game | Bán / mua Pokémon trong Hộp PC giữa người chơi (khung giá máy chủ, ký quỹ, mua nguyên tử). 0 = tạm khoá. |
| `cho_pokemon.thue_phan_tram` | Thuế chợ Pokémon (%) | Cố định (ẩn) | `8` | ngay | game | Đốt khi bán được; luôn ≥ thuế chợ ký gửi (thue.cho). |
| `cho_pokemon.phi_dang_phan_tram` | Phí đăng bán Pokémon (%) | Cố định (ẩn) | `1` | ngay | game | Đốt, không hoàn (huỷ / hết hạn cũng mất); tối thiểu 1 xu. |
| `cho_pokemon.cap_toi_thieu` | Cấp tối thiểu chợ Pokémon | Cố định (ẩn) | `20` | ngay | game | Người bán và người mua phải đạt cấp này (ngoài cổng chống clone chung). |
| `cho_pokemon.toi_da_dang` | Số tin bán Pokémon tối đa / người bán | Cố định (ẩn) | `3` | ngay | game | Số Pokémon đang bán cùng lúc tối đa. |
| `cho_pokemon.mua_ngay` | Số Pokémon mua tối đa / ngày | Cố định (ẩn) | `5` | ngay | game | Mỗi người mua tối đa chừng này Pokémon ở chợ mỗi ngày. |
| `cho_pokemon.khoa_gio` | Khoá bán lại (giờ) | Cố định (ẩn) | `72` | ngay | game | Pokémon nhận qua trao đổi / chợ phải chờ chừng này giờ mới bán được. |
| `cho_pokemon.han_gio` | Hạn tin bán Pokémon (giờ) | Cố định (ẩn) | `48` | ngay | game | Hết hạn: Pokémon về Hộp PC người bán, phí đăng không hoàn. |
| `cho_pokemon.he_so_sac_mau` | Hệ số khung giá sắc màu | Cố định (ẩn) | `3` | ngay | game | Nhân cả sàn lẫn trần khung giá cho Pokémon sắc màu. |
| `cho_pokemon.he_so_3_sao` | Hệ số khung giá 3 sao | Cố định (ẩn) | `2` | ngay | game | Nhân cả sàn lẫn trần khung giá cho Pokémon cá thể 3 sao. |

#### Hồi sinh — luật (cố định) (`hoi_sinh`)

| Khoá | Tên | Lớp | Giá trị | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|
| `hoi_sinh.bat` | Bật cơ chế hồi sinh mới | Luôn bật (ẩn) | `1` | ngay | game | 0 = luồng cũ (popup hồi sinh tại chỗ 5% giờ thu nhập, tự hồi sinh dưới cấp 50). |
| `hoi_sinh.he_so_lap` | Hệ số gục lặp (%) | Cố định (ẩn) | `100,150,200,300` | ngay | game | Lần gục thứ 1, 2, 3... trong cửa sổ; lần sau dùng số cuối (trần). Áp cho phí xu và lượng khoá. |
| `hoi_sinh.cua_so_phut` | Cửa sổ gục lặp (phút) | Cố định (ẩn) | `30` | ngay | game | Đếm số lần gục trong ngần này phút. |
| `hoi_sinh.cho_sau_guc_giay` | Chờ sau khi gục (giây) | Cố định (ẩn) | `5` | ngay | game | Hồi sinh tại chỗ chỉ bấm được sau ngần này giây (đếm ngược trên hộp thoại). |
| `hoi_sinh.hp_thuoc` | HP khi dùng Thuốc hồi sinh (%) | Cố định (ẩn) | `30` | ngay | game | Thuốc hồi sinh tối đa lấy % trong data_vat_pham_lau_dai (hoi_sinh:100). |
| `hoi_sinh.hp_tra_phi` | HP khi trả xu / lượng khoá / tân thủ (%) | Cố định (ẩn) | `50` | ngay | game |  |
| `hoi_sinh.exp_mat_phan_nghin` | Mất EXP khi về Trung tâm (phần nghìn) | Cố định (ẩn) | `20` | ngay | game | Phần nghìn EXP lên cấp hiện tại (20 = 2%). Không bao giờ tụt cấp: chỉ trừ trong tiến độ cấp. |
| `hoi_sinh.exp_mat_toi_da_phut` | Trần mất EXP (phút cày) | Cố định (ẩn) | `15` | ngay | game | Không mất quá ngần này phút EXP đánh quái ở cấp đó. 0 = không chặn. |
| `hoi_sinh.do_ben_mat_phan_tram` | Mất độ bền khi về Trung tâm (%) | Cố định (ẩn) | `2` | ngay | game | % độ bền tối đa của mỗi trang bị đang mặc (không làm vỡ đồ). |
| `hoi_sinh.met_moi_phut` | Mệt mỏi (phút) | Cố định (ẩn) | `5` | ngay | game | Sau khi về Trung tâm. 0 = tắt. |
| `hoi_sinh.met_moi_exp` | Mệt mỏi: giảm EXP (%) | Cố định (ẩn) | `20` | ngay | game | EXP hạ quái. |
| `hoi_sinh.met_moi_tan_cong` | Mệt mỏi: giảm tấn công (%) | Cố định (ẩn) | `10` | ngay | game |  |
| `hoi_sinh.tan_thu_hoi_chieu_giay` | Hồi chiêu hồi sinh miễn phí tân thủ (giây) | Cố định (ẩn) | `30` | ngay | game |  |
| `hoi_sinh.dong_doi_bat` | Cho chờ đồng đội hồi sinh | Luôn bật (ẩn) | `1` | ngay | game | Đồng đội ở gần mang Thuốc hồi sinh được mời cứu. |
| `hoi_sinh.dong_doi_giay` | Thời gian chờ đồng đội (giây) | Cố định (ẩn) | `60` | ngay | game |  |
| `hoi_sinh.cam_pvp` | Cấm tại chỗ ở PvP / sàn đấu | Luôn bật (ẩn) | `1` | ngay | game | Sàn đấu, giải Liên minh, chiến trường, lãnh địa đội, chiếm Gym đang đánh, đấu trường + hoi_sinh.map_pvp. |
| `hoi_sinh.cam_gym` | Cấm tại chỗ khi thách đấu Gym | Luôn bật (ẩn) | `1` | ngay | game |  |
| `hoi_sinh.cam_boss` | Cấm tại chỗ giữa trận boss | Luôn bật (ẩn) | `1` | ngay | game | Đã gây sát thương vào boss thế giới / Gym còn sống cùng bản đồ. |
| `hoi_sinh.cam_me_cung` | Cấm tại chỗ trong Mê cung | Luôn bật (ẩn) | `1` | ngay | game |  |
| `hoi_sinh.map_pvp` | Bản đồ PvP (id) | Cố định (ẩn) | `202,203,204,205,227,228` | ngay | game | Thêm vào luật cấm PvP (so cả id bản đồ lẫn id nạp). |
| `hoi_sinh.map_cam_them` | Bản đồ cấm tại chỗ thêm (id) | Cố định (ẩn) | *(trống)* | ngay | game | Cách nhau dấu phẩy; trống = không. |
| `hoi_sinh.auto_ve_map_up` | Cho "Auto về Map Up" sau khi gục | Cố định (ẩn) | `0` | ngay | game | 0 = gục thì không tự bay lại bãi luyện (chống treo máy vô hạn). |
| `hoi_sinh.thuoc_moi_gio` | Hồi sinh bằng vật phẩm tối đa / giờ | Cố định (ẩn) | `6` | ngay | game | Thuốc hồi sinh / tối đa của chính mình (tay, Tự động đánh, dùng từ túi) trong 60 phút. Đồng đội cứu không tính. 0 = không giới hạn. |

#### Thông báo (cố định) (`thong_bao`)

| Khoá | Tên | Lớp | Giá trị | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|
| `sv.thongBao.bat` | Gộp thông báo toàn máy chủ | Luôn bật (ẩn) | `1` | ngay | game | 0 = gửi thẳng như cũ. |
| `sv.thongBao.toiDa` | Thông báo tối đa / cửa sổ | Cố định (ẩn) | `6` | ngay | game |  |
| `sv.thongBao.cuaSoGiay` | Cửa sổ đếm thông báo (giây) | Cố định (ẩn) | `60` | ngay | game |  |
| `sv.thongBao.moiLoai` | Tối đa mỗi loại / cửa sổ | Cố định (ẩn) | `3` | ngay | game |  |
| `sv.thongBao.trungLapGiay` | Bỏ thông báo trùng (giây) | Cố định (ẩn) | `120` | ngay | game |  |
| `sv.thongBao.gopGiay` | Gộp mỗi (giây) | Cố định (ẩn) | `15` | ngay | game |  |

#### Quản trị & thử nghiệm (cố định) (`quan_tri`)

| Khoá | Tên | Lớp | Giá trị | Áp dụng | Ở | Mô tả |
|---|---|---|---|---|---|---|
| `sv.gmCanCoPInfo` | GM cần cả cờ pInfo | Cố định (ẩn) | `0` | ngay | game | 1 = quyền GM cần tên trong danh sách VÀ pInfo[16]=1. |
| `sv.choPhepTaoTenGM` | Cho tạo Pokémon tên GM | Cố định (ẩn) | `0` | ngay | game | Chỉ bật tạm thời. |
| `sv.testMode` | Chế độ thử (testMode) | Cố định (ẩn) | `0` | ngay | game | Mã test cấp tài nguyên miễn phí. Khoá ở máy thật. (máy thật chỉ nhận mặc định) |
| `sv.testCode` | Mã test | Cố định (ẩn) | *(trống)* | ngay | game | Chuỗi gõ trong game khi testMode=1. (máy thật chỉ nhận mặc định) |

<!-- BAN CHUP cau_hinh_danh_muc: KET THUC -->
