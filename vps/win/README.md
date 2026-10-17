# Xem DB và log của VPS từ máy Windows

5 file `.bat` trong thư mục này, bấm đôi là chạy (hoặc gọi từ PowerShell).
Tất cả đi qua **SSH**, không cần mở thêm cổng nào trên VPS.

| File | Dùng để |
|---|---|
| `vps-trangthai.bat` | Ảnh chụp nhanh: service, cổng, RAM, số thread, nhân vật, tài khoản, commit đang chạy |
| `vps-log.bat` | Xem log game server **theo thời gian thực** (`tail -f`), Ctrl+C để thoát |
| `vps-loi.bat` | Lọc riêng các dòng lỗi trong log + log login server |
| `vps-sql.bat "<câu SQL>"` | Chạy một câu SQL trên DB của VPS |
| `vps-db-tunnel.bat` | Mở đường hầm để dùng HeidiSQL/Workbench xem DB như DB local |
| `vps-db-tai-ve.bat` | **Tải** `kpah_db` + `account` từ VPS về máy này |
| `vps-db-nap-vao-local.bat` | **Nạp** bản vừa tải vào MySQL local (XAMPP) — ghi đè DB local |

## Cho DB local giống VPS

Chạy hai file, theo thứ tự:

```
vps-db-tai-ve.bat          -> tải về %USERPROFILE%\Desktop\htbb-db-backup\
vps-db-nap-vao-local.bat   -> nạp vào MySQL local
```

Vì sao tách làm hai: bước tải chỉ **đọc** VPS (an toàn, chạy lúc nào cũng được), bước nạp thì
**ghi đè** DB trên máy này. Tách ra thì tải xong vẫn xem lại được rồi mới quyết định nạp.

Những thứ hai script này tự lo:

- **Không khoá DB production**: dump bằng `--single-transaction --quick`, server thật vẫn chạy bình thường.
- **Bắt được dump bị cắt giữa đường**: `mysqldump` chết giữa đường vẫn để lại file trông như thật —
  đúng cạm bẫy từng làm `backup_db.sh` xoá bản cũ sau khi tạo ra một bản dump hỏng. Nên có **hai** phép
  kiểm độc lập: mã thoát của `ssh`, và dòng `-- Dump completed` ở cuối file. Bước nạp kiểm **lại** lần nữa
  trước khi xoá gì.
- **Sao lưu DB local trước khi ghi đè**, ra `truoc_khi_nap_*.sql` cùng thư mục — muốn lấy lại nhân vật thử
  cũ thì nạp file đó.
- **Chờ bạn tắt server tử tế** (Admin Panel → "Bảo Trì", hoặc Ctrl+C ở console) chứ không tự kill: kill
  cứng thì hook lưu dữ liệu không chạy kịp.
- **Xoá hẳn DB rồi nạp lại** chứ không nạp đè: nạp đè thì bảng nào local có mà VPS không có sẽ còn lại,
  hai bên vẫn không giống nhau.

Lệnh `DROP DATABASE` cố ý nằm trong `vps-db-nap-vao-local.bat` (ghim cứng `127.0.0.1`), **không** nhúng
vào file dump: file dump nằm trên Desktop, ai chạy nhầm nó ngược lên VPS thì chỉ mất máy bàn chứ không
xoá sạch DB thật.

> Bản dump chứa dữ liệu người chơi thật (tên đăng nhập, nhân vật, nạp thẻ). Đừng chia sẻ file ra ngoài.

## Ví dụ `vps-sql.bat`

```
vps-sql "SELECT id,charname,userId,ban FROM kpah_db.tob_char;"
vps-sql "SELECT username,ban FROM account.team_user;"
vps-sql "SELECT COUNT(*) FROM kpah_db.tob_char;"
vps-sql "SHOW TABLES FROM kpah_db LIKE 'tob_%';"
```

Câu SQL được đưa qua stdin nên không phải lo escape dấu nháy.

## Xem DB bằng giao diện (HeidiSQL)

1. Chạy `vps-db-tunnel.bat`, **để cửa sổ đó mở**
2. Trong HeidiSQL tạo session mới:
   - Hostname/IP: `127.0.0.1`
   - Port: **3307** (3306 đã bị MySQL của XAMPP chiếm)
   - User: `kpah`
   - Password: mật khẩu DB trên VPS
3. Mở `kpah_db` và `account` như DB thường

Cổng 3306 trên VPS vẫn **đóng** với internet — đường hầm chạy bên trong kết nối SSH.

> HeidiSQL có sẵn mục "SSH tunnel" trong tab Settings, dùng cũng được và không cần
> chạy file .bat. Nhưng nó cần `plink.exe`; cách dùng .bat ở trên đơn giản hơn.

## Đỡ phải nhập mật khẩu SSH mỗi lần

Tạo khoá SSH trên Windows rồi đưa lên VPS — sau đó mọi file .bat chạy không cần mật khẩu:

```powershell
ssh-keygen -t ed25519          # Enter hết, không cần passphrase
```

```powershell
$key = (Get-Content $env:USERPROFILE\.ssh\id_ed25519.pub -Raw).Trim()
```

```powershell
ssh root@180.93.57.30 "mkdir -p ~/.ssh && printf '%s\n' '$key' >> ~/.ssh/authorized_keys && chmod 700 ~/.ssh && chmod 600 ~/.ssh/authorized_keys && echo DA_THEM_XONG"
```

> **ĐỪNG dùng kiểu `type file.pub | ssh root@... "cat >> ..."`.** Pipe chiếm stdin nên
> OpenSSH trên Windows **không mở được ô nhập mật khẩu** — bạn gõ mật khẩu mà nó không
> nhận, tưởng sai mật khẩu. Cách trên đưa khoá vào biến `$key` nên `ssh` giữ được terminal
> để hỏi mật khẩu bình thường.
>
> Lúc nhập mật khẩu, màn hình **không hiện gì cả** — không dấu sao, không con trỏ nhảy.
> Cứ gõ rồi Enter.

Ký tự `\r` của Windows lọt vào `authorized_keys` là OpenSSH coi khoá không hợp lệ và vẫn
hỏi mật khẩu, dù file trông đúng. Vì vậy phải có `.Trim()` và `printf '%s\n'`.

Thử lại: `ssh root@180.93.57.30 "echo OK"` — không hỏi mật khẩu là xong.

## Lưu ý

- Các file này **không chứa mật khẩu** nào (SSH dùng khoá hoặc hỏi tay, `mysql -u root`
  trên VPS dùng unix_socket nên không cần mật khẩu). An toàn khi để trong Git.
- Sửa IP `180.93.57.30` trong cả 5 file nếu đổi VPS.
