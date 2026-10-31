# Login server — đặc tả, vận hành, quay lui

Login server kiểm tên tài khoản + mật khẩu cho game server. Từ 31/10/2026 HTBB có login server **tự viết**
(`server/loginServer/htbb-login.jar`, mã nguồn [server/src/com/htbb/dangnhap/](../server/src/com/htbb/dangnhap/MayChuDangNhap.java))
thay cho jar thừa kế từ game gốc `CheckLoginSocket.jar` (không có mã nguồn, in nhãn game cũ). Bản mới **giữ nguyên giao
thức và ngữ nghĩa CSDL** của bản cũ — game server, CSDL, web không phải đổi gì; jar cũ vẫn nằm cạnh để quay lui bằng
một dòng (mục 7).

Kiến trúc chung: [kien-truc.md](kien-truc.md) mục 3. Vận hành VPS: [van-hanh.md](van-hanh.md).

## 1. Vị trí trong hệ thống

```
client ─(1: user, pass, ...)─▶ game server (LoginHandler: kiểm ký tự, bảo trì, IP...)
                                   │ io.LogInController: MỘT kết nối TCP bền, tự nối lại mỗi giây
                                   ▼
                     login server :8023 (127.0.0.1, ufw chặn ngoài)
                                   │ SELECT ... FROM team_user WHERE username=? AND password=PASSWORD(?)
                                   ▼
                     MariaDB, CSDL trong db.url (production: account)
```

- Game gửi **một** yêu cầu cho mỗi lần người chơi bấm Đăng nhập; login server trả **userId** (hoặc mã lỗi). Mọi dữ liệu
  nhân vật nằm ở `htbb_db`, login server không đụng tới.
- Game chờ tối đa **15 s** (`io.LogInController.CHO_LOGIN_MS`, `-Dhtbb.choLoginMs`); quá hạn thì báo người chơi
  "Máy chủ đăng nhập chưa phản hồi. Vui lòng thử lại sau ít giây." và bỏ trả lời đến muộn.
- Game từ chối ngay ("Máy chủ quá tải") nếu hàng gửi tới login server quá 50 yêu cầu.

## 2. Giao thức (TCP, không mã hoá, không nén)

Khung: `u8 lệnh | u16 độ dài (big-endian, không dấu) | dữ liệu`. `UTF` = `DataOutputStream.writeUTF` (u16 độ dài +
modified UTF-8). Chỉ có **một** lệnh:

| Hướng | Lệnh | Dữ liệu |
|---|---|---|
| game → login | `1` | `i64 sessionId`, `UTF tên`, `UTF mật khẩu`, `i8 mã game` (HTBB luôn gửi `9`) |
| login → game | `1` | `i64 sessionId`, `i32 kết quả`, `bool vinaphone`, `i8 fromgame` — luôn 3 + 14 byte |

- Lệnh khác `1`: bỏ qua, **không** trả lời (giống bản cũ).
- Trả lời có thể **khác thứ tự** gửi: game ghép theo `sessionId` (bảng băm `htLoginSessionList`).
- `fromgame` được game dùng làm `bRegion`: `LoginHandler.onLoginOK` chỉ cho vào khi bằng `0` hoặc `9`
  (đăng ký trong game ghi `0`, xem [KiemDangKyTrongGame](../server/test/KiemDangKyTrongGame.java)).
- Bản mới thêm (bản cũ không có): gói lệnh 1 hỏng nhưng đọc được `sessionId` → trả `-3` cho phiên đó thay vì im lặng;
  khung dài hơn `login.khungToiDa` (mặc định 4096, yêu cầu thật ~50 byte) → cắt kết nối (không đồng bộ lại được).

## 3. Ngữ nghĩa kết quả (giữ y bản cũ)

| Điều kiện (theo thứ tự) | Kết quả | Game báo người chơi |
|---|---|---|
| tên hoặc mật khẩu chứa `'` | `-3` (không truy vấn) | "Có lỗi xảy ra khi đăng nhập" |
| mã game `2`, `6`, `15` (CSDL game cũ `nr2`, `hs2`, `am` — không tồn tại) | `-3` | như trên |
| mật khẩu bắt đầu `#` (token) | bảng `usertoken`: có dòng → `id` (quá 3600 s từ `dateCreate` → `-1`); không dòng → `-3`; bảng không có (HTBB) → `-3` | |
| không có dòng `team_user` khớp `username = ?` và `password = PASSWORD(?)` | `-1` | "Sai tên đăng nhập hoặc mật khẩu." |
| `ban = 1` | `-2` | "Tài khoản của bạn hiện đang bị khoá" |
| `ban = 3` | `-5` | "…chưa kích hoạt Email…" |
| `ban` khác (0, 2, …) | `id` (> 0) | vào game |
| lỗi CSDL / hết chờ kết nối / quá hạn xử lý | `-3` | "Có lỗi xảy ra khi đăng nhập" |

- `vinaphone` = `provider == 0` và `phone` bắt đầu `8491`, `8494`, `84123`, `84124`, `84125`, `84127`, `84129`
  (di sản, game HTBB không dùng). `fromgame` = cột `team_user.fromgame`.
- Sai mật khẩu của tài khoản bị khoá vẫn là `-1` (không lộ tài khoản bị khoá).
- **Mật khẩu**: cột `team_user.password` giữ `PASSWORD(pw)` của MariaDB (`*` + SHA1(SHA1(pw)) hex hoa, không salt). Bản
  mới vẫn so **trong SQL** bằng `PASSWORD(?)`, cùng bảng mã kết nối (`characterEncoding=UTF-8` trong `db.url`), cùng
  collation tên (`utf8_general_ci`: không phân biệt hoa thường) — mọi tài khoản cũ đăng nhập y như trước. Khác duy nhất:
  bản cũ **nối chuỗi** vào SQL (chặn `'` nhưng không chặn `\`), bản mới dùng `PreparedStatement`. Game đã chỉ cho
  `[A-Za-z0-9]` trong tên và mật khẩu (`CreateCharHandler.checkInfoLogin`) nên với dữ liệu thật hai bản cho cùng kết quả.
- Trạng thái kích hoạt khác (`ThanhVien.chanDangNhap` / `5h_active`), cấm máy, trùng đăng nhập (đá phiên cũ),
  giới hạn đăng nhập theo **IP người chơi** (`sv.dangNhapIpNguong`) đều do **game** làm sau khi có `userId`, không phải
  login server.
- Login server **không ghi** CSDL (bản cũ có nhánh `INSERT INTO loginlog` nhưng tắt cứng).

## 4. Bản cũ `CheckLoginSocket.jar` — hành vi và lỗi đã biết

Đọc từ bytecode (CFR) chỉ để làm tương thích; jar giữ nguyên làm bản quay lui.

| | Bản cũ | Hậu quả |
|---|---|---|
| Xử lý | mỗi kết nối 1 luồng đọc + 1 luồng xử lý **tuần tự** (game chỉ mở 1 kết nối → mọi đăng nhập xếp một hàng, mỗi lần một truy vấn) | CSDL chậm 50 ms/truy vấn → tối đa ~20 đăng nhập/s; đợt đăng nhập dồn hàng tới > 15 s → game báo "chưa phản hồi" (mục 8) |
| Luồng | luồng xử lý **không bao giờ thoát** khi kết nối đóng | rò 1 luồng mỗi kết nối (4536 luồng sau 3 ngày khi kiểm sức khoẻ còn mở TCP — [kiem-suc-khoe.sh](../vps/kiem-suc-khoe.sh)) |
| Kết nối CSDL | tự viết, tối đa 8, `Limited Connection` ngay khi đủ; **cướp** kết nối đã mượn quá 30 s; không kiểm kết nối chết; không `socketTimeout` | MariaDB cắt kết nối lúc rảnh → người đăng nhập kế tiếp nhận `-3`; CSDL treo → login server treo theo, không giới hạn |
| Mạng | không `TCP_NODELAY` | trả lời bị Nagle giữ ~30-40 ms khi có nhiều yêu cầu liền nhau |
| Log | mỗi lần đăng nhập 1 dòng `count\|ngày/giờ \| ip NQSH \| tên \| kq=…`, in stack trace thô | không theo dạng `[LOẠI]` của HTBB |
| Driver | Connector/J 5.0.8 (2007) đóng gói bên trong | collation `utf8mb4` (id 45/224) → "Unknown initial character set index" (vì vậy MariaDB phải để `utf8`, [trien-khai.md](trien-khai.md)) |

## 5. Bản mới `htbb-login.jar`

```
luồng nhận kết nối ─▶ mỗi kết nối 1 luồng ĐỌC: đọc khung, kiểm giới hạn, xếp việc
                         │ hàng đợi CHẶN TRÊN (login.hangDoi.toiDa); đầy → -3 ngay
                         ▼
              bể luồng xử lý cố định (login.luong) ─▶ bể kết nối CSDL (login.csdl.toiDa, mượn có hạn)
                         │
                         ▼ ghi trả lời (khoá theo kết nối, TCP_NODELAY)
canh gác hạn: đúng login.hanXuLyMs sau khi nhận mà chưa trả lời → trả -3 (bất kể kẹt ở hàng, chờ kết nối hay CSDL treo)
```

- **Hạn xử lý** `login.hanXuLyMs` (13 s) < 15 s chờ của game: mọi yêu cầu đều có trả lời **trước** khi game bỏ cuộc.
  Yêu cầu quá hạn khi còn trong hàng thì không tốn truy vấn. Truy vấn dùng phần thời gian còn lại làm
  `setNetworkTimeout` / `setQueryTimeout`.
- **Bể kết nối CSDL**: mượn chờ tối đa `login.csdl.choMuonMs`, kết nối rảnh quá `kiemSauRanhGiay` được `isValid()` trước
  khi dùng, quá `tuoiToiDaPhut` thì mở mới; lỗi **kết nối** (SQLState `08xxx`, ví dụ MariaDB vừa cắt) xảy ra nhanh thì
  thử lại **một** lần trên kết nối mới — người chơi không nhận `-3` oan. Thêm `connectTimeout` / `socketTimeout` nếu
  `db.url` chưa đặt.
- **Giới hạn theo IP bên gọi** (game server, không phải người chơi): tối đa `login.ketNoi.toiDa` kết nối, mỗi IP
  `login.ketNoi.moiIp`; `login.ipChoPhep` (trống = mọi IP như bản cũ; ufw đã chặn cổng); `login.yeuCauMoiGiayMoiIp`
  (xô token, 0 = tắt — mặc định, vì mọi đăng nhập đều đến từ MỘT IP của game).
- **Log** `logs/login.out`, dạng game: `yyyy-MM-dd HH:mm:ss.SSS <I|W|E> [LOẠI] …`, ghi bất đồng bộ (hàng 20000 dòng, đầy
  thì bỏ và đếm), làm sạch ký tự điều khiển. **Không bao giờ** ghi mật khẩu người chơi hay `db.password`.

| Loại | Khi nào |
|---|---|
| `[KHOI_DONG]`, `[CSDL]`, `[TAT]` | bật (cấu hình tóm tắt, không mật khẩu), thử CSDL, tắt êm (tổng kết) |
| `[DANG_NHAP]` | mỗi lần kiểm: `tên kq=… tre=…ms` (`login.logMoiLan=0` để tắt); lỗi CSDL → dòng `E` kèm lý do |
| `[KET_NOI]` | game nối / rớt / bị từ chối |
| `[QUA_TAI]` | hàng đầy, quá hạn xử lý, việc xong sau hạn |
| `[GIOI_HAN]` | vượt `login.yeuCauMoiGiayMoiIp` |
| `[NHIP]` | mỗi `login.nhipGiay` (60 s): `dang_nhap yc= ok= sai= khoa= loi= tuChoi= hetHan= tre p50= p99= max= hang=x/y xuLy= ketNoi= csdl=dangMuon/dangMo/toiDa tong=` |

- **Tắt êm** (SIGTERM / `systemctl stop`): ngừng nhận kết nối và yêu cầu mới, trả lời nốt yêu cầu đã nhận (tối đa
  `login.dungChoMs`, 10 s), đóng kết nối và CSDL, in tổng kết. JVM thoát mã 143 (unit đặt `SuccessExitStatus=143`).

### Cấu hình (`server/loginServer/server.ini` — cùng tệp bản cũ đọc)

| Khoá | Mặc định | Ý nghĩa |
|---|---|---|
| `port` | (bắt buộc) | cổng nghe (production 8023, staging 28023) |
| `db.url`, `db.user`, `db.password` | (bắt buộc) | như bản cũ; giữ `characterEncoding=UTF-8` |
| `login.diaChi` | trống = mọi địa chỉ | địa chỉ bind (đặt `127.0.0.1` để khỏi dựa vào ufw) |
| `login.csdl.toiDa` | 8 | số kết nối CSDL tối đa (bản cũ 8) |
| `login.luong` | = `csdl.toiDa` | số luồng xử lý |
| `login.hangDoi.toiDa` | 2000 | yêu cầu chờ tối đa; đầy → `-3` ngay |
| `login.hanXuLyMs` | 13000 | hạn mỗi yêu cầu (phải < `htbb.choLoginMs` của game) |
| `login.csdl.choMuonMs` / `truyVanGiay` / `ketNoiMs` / `socketMs` | 3000 / 5 / 3000 / 6000 | các hạn CSDL |
| `login.csdl.kiemSauRanhGiay` / `tuoiToiDaPhut` | 30 / 30 | kiểm kết nối rảnh / thay kết nối già |
| `login.ketNoi.toiDa` / `login.ketNoi.moiIp` | 64 / 16 | kết nối vào tối đa |
| `login.ipChoPhep` | trống | danh sách IP được nối (phẩy / cách) |
| `login.yeuCauMoiGiayMoiIp` | 0 | 0 = tắt |
| `login.khungToiDa` | 4096 | byte |
| `login.nhipGiay` | 60 | 0 = tắt `[NHIP]` |
| `login.logMoiLan` | 1 | 0 = không ghi từng lần đăng nhập thành công/sai |
| `login.dungChoMs` | 10000 | thời gian tắt êm |

Đổi tệp khác: `java -Dhtbb.login.ini=/đường/dẫn -jar htbb-login.jar`. Thiếu khoá bắt buộc / sai cổng / cổng bị chiếm →
log `E [KHOI_DONG]` rồi thoát mã 1 (systemd bật lại). CSDL chưa lên khi bật → vẫn nghe cổng, yêu cầu trả `-3` tới khi
CSDL lên (tự nối lại).

## 6. Build và kiểm

```sh
sh server/loginServer/build.sh        # -> server/loginServer/htbb-login.jar (javac --release 8, kèm Connector/J 5.1.49)
```

`htbb-login.jar` nằm trong git như `htbb-server.jar`: sửa `server/src/com/htbb/dangnhap/` thì **phải** build lại và
commit jar (`KiemDangNhapServer` báo nếu jar thiếu lớp). Lớp `com.htbb.dangnhap` cũng được biên dịch vào
`htbb-server.jar` (build gom cả `src/`) nhưng game không dùng.

| Kiểm | Lệnh / nơi |
|---|---|
| Đơn vị: giao thức khớp byte với `io.Message`/`LogInController`, ngữ nghĩa mục 3, bể kết nối, giới hạn, hạn xử lý, tắt êm, unit systemd | [KiemDangNhapServer](../server/test/KiemDangNhapServer.java) trong bộ `Kiem` ([build.md](build.md)) |
| Bot người chơi thật qua game: đăng nhập đúng / sai mật khẩu (qua login server) / bị khoá (`ban=1`) / chưa kích hoạt (`ban=3`) / đăng nhập lần hai đá phiên cũ / rớt mạng vào lại | nhóm `cn_dang_nhap` của [hoi_quy.conf](../tools/pokemon/kiem_tai/hoi_quy.conf) (kịch bản `dang_nhap`, `dang_nhap_khoa`, `mat_ket_noi`) |
| Đo tải trực tiếp login server (đóng vai game) | [DoTaiDangNhap.java](../tools/pokemon/kiem_tai/DoTaiDangNhap.java): `java DoTaiDangNhap 127.0.0.1 <cổng> 1000 30` |
| CSDL chậm / treo / cắt kết nối giả lập | [ProxyTre.java](../tools/pokemon/kiem_tai/ProxyTre.java) đứng giữa login server và MariaDB |

## 7. Triển khai và quay lui

Unit [vps/htbb-login.service](../vps/htbb-login.service) chạy `-jar ${HTBB_LOGIN_JAR}`: mặc định `htbb-login.jar`, ghi
đè được bằng tệp tuỳ chọn `/etc/htbb/login.env`. Staging: [htbb-staging-login.service](../vps/staging/htbb-staging-login.service)
+ `/etc/htbb/staging-login.env`. `server.ini` của login server **không đổi** (khoá `login.*` đều tuỳ chọn).

**Lên bản mới (một lần, trên VPS)** — sau khi commit đã qua cổng kiểm staging
([quy trình](trien-khai.md)), lúc ít người chơi:

```sh
cd /opt/htbb && sudo -u htbb git pull                        # có server/loginServer/htbb-login.jar
sudo install -m 644 vps/htbb-login.service /etc/systemd/system/ && sudo systemctl daemon-reload
sudo systemctl restart htbb-login && sleep 2 && sudo systemctl restart htbb-game   # game nối lại login mới
tail -n 20 /opt/htbb/logs/login.out                          # [KHOI_DONG] ... [CSDL] ket noi CSDL tai khoan ok
```

Chưa cài unit mới thì máy vẫn chạy `CheckLoginSocket.jar` như cũ (unit cũ ghi cứng tên jar) — kéo code về một mình
không đổi gì. Restart game sau login vì game server **làm mất yêu cầu đầu tiên** gửi sau khi login server khởi động lại
(mục 9); `vps/update.sh` / `deploy-auto.sh` vốn đã restart login trước rồi mới bật game.

**Quay lui về jar cũ — một dòng:**

```sh
echo HTBB_LOGIN_JAR=CheckLoginSocket.jar | sudo tee /etc/htbb/login.env && sudo systemctl restart htbb-login && sleep 2 && sudo systemctl restart htbb-game
```

Về lại bản mới: `sudo rm /etc/htbb/login.env && sudo systemctl restart htbb-login && sleep 2 && sudo systemctl restart htbb-game`.
Kiểm đang chạy jar nào: `systemctl show htbb-login -p ExecStart` hoặc `ps -o args= -C java | grep -- -jar`.

Máy dev / Windows: `server/loginServer/run.bat` (`set HTBB_LOGIN_JAR=CheckLoginSocket.jar` để chạy jar cũ);
`tools/pokemon/chay_tren_novadb.sh` và `htbb_cong_cu/kiem_truoc.sh` đọc cùng biến `HTBB_LOGIN_JAR`.

## 8. Số đo cũ và mới (31/10/2026)

Máy dev, MariaDB 10.4 riêng, mỗi lần đo bật **lạnh** login server, công cụ `DoTaiDangNhap` đóng vai game (MỘT kết
nối), 10 % yêu cầu sai mật khẩu. "Quá hạn" = trả lời sau 15 s hoặc không có = người chơi thấy "chưa phản hồi".

| Tình huống | `CheckLoginSocket.jar` (cũ) | `htbb-login.jar` (mới) |
|---|---|---|
| A. 1000 / 30 s, CSDL thẳng, CPU không giới hạn (như production), 2 lần | quá hạn 0; p50 0 ms, p99 1–30 ms, max 95 ms | quá hạn 0; p50 0–1 ms, p99 2 ms, max 13 ms |
| B. như A nhưng CPU 20 % (unit staging), 2 lần | quá hạn 0; p99 1–30 ms, max 66 ms | quá hạn 0; p99 35–95 ms, max 435 ms (JVM lạnh, 8 luồng dưới quota) |
| C. 3000 / 10 s (300/s) | quá hạn 0; p50 3 ms, p99 5 ms | quá hạn 0; p50 1 ms, p99 40 ms |
| D. CSDL chậm thêm 30 ms / truy vấn, 1000 / 30 s | quá hạn 0 nhưng **p50 1708 ms, p99 2461 ms** (dồn hàng) | p50 32 ms, p99 110 ms |
| E. CSDL chậm thêm 50 ms / truy vấn, 1000 / 30 s | **370 / 1000 quá hạn** (156 không trả lời, 214 trả muộn), p50 10.7 s | **0 quá hạn**, p50 51 ms, p99 67 ms |
| F. CSDL treo 20 s giữa đợt, 600 / 30 s | **113 quá hạn** (trả lời sau 15–20.6 s, game đã bỏ) | **0 quá hạn**: 153 nhận `-3` đúng 13 s, còn lại đăng nhập được |
| G. MariaDB cắt mọi kết nối giữa đợt | 1 `-3`, tự hồi phục | 1 `-3`, tự hồi phục (lần đo trước: 0 nhờ thử lại) |
| H. Mở/đóng 300 kết nối | luồng 15 → **315** (rò) | luồng 17 → 30 |

Đọc bảng: khi CSDL nhanh, bản cũ đủ sức (một truy vấn chưa tới 1 ms); nó **sụp khi mỗi truy vấn chậm đi vài chục ms**
(MariaDB bận vì game lưu nhân vật, khoá bảng MyISAM `team_user`) vì xử lý tuần tự trên một hàng — đúng triệu chứng
"Máy chủ đăng nhập chưa phản hồi" lúc đông người. Bản mới xử lý song song 8 truy vấn và không bao giờ để game chờ quá
13 s. Bot người chơi thật qua game (`cn_dang_nhap`, 18 phép: đúng, sai mật khẩu, khoá, chưa kích hoạt, đăng nhập lần
hai, rớt mạng vào lại) **ĐẠT** với cả hai jar.

## 9. Rủi ro / việc còn lại

- **Game làm mất yêu cầu đầu tiên sau khi login server khởi động lại** (cả jar cũ lẫn mới): luồng gửi của
  `io.LogInController` đã kiểm "còn nối" rồi mới chờ hàng; login rớt trong lúc chờ thì yêu cầu kế tiếp ghi vào socket chết
  → người đó chờ 15 s rồi thấy "chưa phản hồi" (thấy được ở bot `dang_nhap.dung` khi chỉ restart login). Cách tránh khi
  vận hành: restart game sau login (mục 7). Sửa tận gốc (gửi lại yêu cầu còn chờ khi nối lại) là việc của game server.
- Bản mới dùng Connector/J 5.1.49 (như game) thay 5.0.8: hết lỗi `utf8mb4` của driver cũ, nhưng khi còn giữ đường quay
  lui về jar cũ thì MariaDB **vẫn** phải để `utf8` ([htbb-mariadb.cnf](../vps/htbb-mariadb.cnf)).
- Mật khẩu vẫn là `PASSWORD()` không salt (yêu cầu tương thích). Login server tự viết là điều kiện để làm giai đoạn 3 của
  kế hoạch mật khẩu hai cột ([matKhauV2.ts](../web/server/utils/matKhauV2.ts)).
