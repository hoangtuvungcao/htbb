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
