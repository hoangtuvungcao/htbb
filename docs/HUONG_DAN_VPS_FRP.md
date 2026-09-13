# Hướng dẫn mở server ra internet bằng VPS Ubuntu 20.04 + frp

Dành cho trường hợp: mạng nhà bị CGNAT hoặc không forward được port trên router.
VPS chỉ làm trạm chuyển tiếp, **game server vẫn chạy ở máy nhà**.

```
Người chơi ──► 180.93.57.30:19129 ──┐
                              │  frps  (VPS Ubuntu, ~30 MB RAM)
                              │    ▲ đường hầm cổng 7000
                              │    │  (máy nhà GỌI RA, không mở port nào)
                        frpc (máy nhà, Windows 10)
                              │
                              ├─► 127.0.0.1:19129   game server
                              └─► 127.0.0.1:80      Apache (web + NQSH2.txt)
```

- **VPS**: `180.93.57.30`, 1 GB RAM, **Ubuntu 20.04 LTS** — đã cài xong, SSH đang chạy
- VPS không chạy Java, không chạy MySQL. Chỉ mỗi frps, tốn ~30 MB nên 1 GB là dư.

---

## PHẦN A — VPS Ubuntu 20.04

### A0. Kết nối vào VPS — dùng SSH, KHÔNG dùng Remote Desktop

Linux **không có RDP**. Mở `mstsc` sẽ luôn báo *"Remote Desktop can't connect to the
remote computer"* — đó không phải lỗi, chỉ là sai công cụ.

**Cách 1 — dùng luôn Windows, không cần cài gì.** Windows 10 có sẵn OpenSSH client.
Mở PowerShell hoặc CMD:

```
ssh root@180.93.57.30
```

Lần đầu nó hỏi `Are you sure you want to continue connecting (yes/no)?` → gõ `yes`.
Rồi nhập mật khẩu root (gõ mật khẩu sẽ **không hiện gì trên màn hình**, kể cả dấu sao —
cứ gõ rồi Enter).

**Cách 2 — PuTTY** (`putty.org`) nếu thích giao diện: Host Name `180.93.57.30`,
Port `22`, Connection type **SSH**, bấm Open.

Nếu tên user không phải `root` thì thay bằng user nhà phân phối cấp, ví dụ
`ssh ubuntu@180.93.57.30`.

Cài lại OS là **đổi mật khẩu** — lấy mật khẩu mới trong panel hoặc email.

### A1. Cập nhật hệ thống

```bash
apt update && apt upgrade -y
```

### A2. Bật Ubuntu Pro (miễn phí, nên làm)

Ubuntu 20.04 hết hỗ trợ tiêu chuẩn từ 4/2025. Ubuntu Pro miễn phí cho cá nhân
(tối đa 5 máy), kéo dài bản vá bảo mật tới 2030. Lấy token tại `ubuntu.com/pro`:

```bash
sudo pro attach <TOKEN_CUA_BAN>
sudo apt update && sudo apt upgrade -y
```

### A3. Cài frps

Vào `github.com/fatedier/frp/releases`, xem bản mới nhất, thay `<VERSION>` bên dưới.
Ghi nhớ VERSION — máy nhà phải dùng **y hệt**, lệch version là bắt tay thất bại.

```bash
cd /tmp
wget https://github.com/fatedier/frp/releases/download/v<VERSION>/frp_<VERSION>_linux_amd64.tar.gz
tar -xzf frp_<VERSION>_linux_amd64.tar.gz
cd frp_<VERSION>_linux_amd64
sudo cp frps /usr/local/bin/
sudo mkdir -p /etc/frp
```

### A4. Sinh token bí mật

```bash
openssl rand -hex 32
```

Copy chuỗi hiện ra, lát nữa dán vào **cả hai** file config, giống hệt từng ký tự.
Ai biết token này là cắm được vào VPS của bạn.

### A5. File `/etc/frp/frps.toml`

```bash
sudo nano /etc/frp/frps.toml
```

```toml
bindPort = 7000

auth.method = "token"
auth.token  = "DAN_CHUOI_VUA_SINH_O_BUOC_A4"

# Chỉ cho phép mở đúng 2 cổng này, tránh bị lạm dụng
allowPorts = [
  { start = 80,    end = 80 },
  { start = 19129, end = 19129 }
]

log.to      = "/var/log/frps.log"
log.level   = "info"
log.maxDays = 7
```

Lưu: `Ctrl+O` → `Enter` → `Ctrl+X`.

### A6. Chạy frps như dịch vụ

```bash
sudo nano /etc/systemd/system/frps.service
```

```ini
[Unit]
Description=frp server
After=network.target

[Service]
Type=simple
ExecStart=/usr/local/bin/frps -c /etc/frp/frps.toml
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now frps
sudo systemctl status frps
```

Phải thấy `active (running)`.

### A7. Tường lửa

```bash
sudo ufw allow 22/tcp       # SSH — LÀM ĐẦU TIÊN, quên là tự khoá mình ngoài
sudo ufw allow 7000/tcp     # đường hầm frp
sudo ufw allow 19129/tcp    # game
sudo ufw allow 80/tcp       # web
sudo ufw enable
sudo ufw status
```

Nếu nhà phân phối còn firewall riêng ở panel (Security Group), mở y hệt 4 cổng đó.

**Không mở 8023 và 19153.** 8023 là login server, chỉ game server gọi nội bộ.
19153 là socket lệnh admin chỉ khoá bằng chuỗi hardcode trong mã nguồn công khai.

### A8. Bù lại phần chống DoS

Qua frp, game server thấy mọi người chơi đều là `127.0.0.1` nên lớp chống DoS trong
`dosAccept()` bị vô hiệu. Chặn ở tầng VPS thay thế:

```bash
sudo apt install -y iptables-persistent
sudo iptables -A INPUT -p tcp --dport 19129 -m connlimit --connlimit-above 20 -j REJECT
sudo netfilter-persistent save
```

---

## PHẦN B — Máy nhà (Windows 10)

### B1. Tải frpc

Tải `frp_<VERSION>_windows_amd64.zip` — **cùng VERSION với VPS**. Giải nén ra `C:\frp\`.

### B2. Tạo `C:\frp\frpc.toml`

Notepad → dán nội dung → *Save as type: **All Files*** với tên `C:\frp\frpc.toml`
(coi chừng Notepad tự thêm đuôi `.txt`):

```toml
serverAddr = "180.93.57.30"
serverPort = 7000

auth.method = "token"
auth.token  = "DAN_CHUOI_GIONG_HET_BEN_frps.toml"

[[proxies]]
name        = "kpah-game"
type        = "tcp"
localIP     = "127.0.0.1"
localPort   = 19129
remotePort  = 19129

[[proxies]]
name        = "kpah-web"
type        = "tcp"
localIP     = "127.0.0.1"
localPort   = 80
remotePort  = 80
```

### B3. Chạy thử

```
C:\frp\frpc.exe -c C:\frp\frpc.toml
```

Thấy `start proxy success` cho cả `kpah-game` và `kpah-web` là thông.

**Không cần tạo rule tường lửa trên máy nhà.** frpc kết nối tới game server qua loopback
(`127.0.0.1`), mà Windows Firewall không chặn loopback. Cũng **không cần forward port
trên router** — máy nhà chỉ gọi ra ngoài, đó là lý do cách này vượt được CGNAT.

### B4. Cho frpc tự chạy cùng Windows

Tải NSSM tại `nssm.cc`, rồi CMD **as Administrator**:

```
nssm install frpc "C:\frp\frpc.exe" "-c C:\frp\frpc.toml"
nssm set frpc AppDirectory C:\frp
nssm start frpc
```

Không muốn cài NSSM thì dùng Task Scheduler, trigger *At startup*, chọn
*Run whether user is logged on or not*.

---

## PHẦN C — Client

### C1. IP đã sửa sẵn

[ServerListScr.cs](../ClientUnity/Assets/Scripts/Game/ServerListScr.cs) dòng 36 đã là:

```csharp
public const string SERVER_HOST = "180.93.57.30";
```

`SERVER_PORT` giữ nguyên `19129`.

> Client lưu port bằng kiểu `short` nên **không nhận port > 32767**. Đây là lý do chọn
> frp thay vì tunnel miễn phí — frp cho bạn tự chọn port.

### C2. File danh sách server — đã sửa sẵn cả hai nơi

- `C:\xampp\htdocs\NQSH2.txt`
- [web/server/NQSH2.txt](../web/server/NQSH2.txt) (`web/server.txt` là symlink trỏ tới đây)

Nội dung, một dòng: `Server 1:180.93.57.30:19129:0`

Định dạng: `tên:địa_chỉ:port:index`, nhiều server ngăn nhau bằng dấu phẩy.

### C3. Build lại client

Chép `ServerListScr.cs` vào project Unity gốc rồi build.

Sau này đổi IP chỉ cần sửa `NQSH2.txt` trên web, người chơi bấm **"Cập nhật"** trong
client là xong — không phải build lại, không phải phát hành client mới.

---

## PHẦN D — Kiểm tra

### D1. Trên VPS

```bash
sudo ss -lntp | grep -E ':(7000|19129|80)'
```

Phải thấy đủ 3 cổng ở trạng thái LISTEN. Thiếu 19129 hoặc 80 nghĩa là frpc chưa kết nối.

### D2. Từ máy khác, ngoài mạng nhà

Mở `http://180.93.57.30/NQSH2.txt`. Ra đúng nội dung nghĩa là đường hầm thông và
Apache ở nhà đã ra được internet.

Đừng test bằng chính máy chạy server.

### D3. Vào game

Chạy client vừa build, chọn Server 1.

---

## PHẦN E — Vận hành

### Thứ tự khởi động ở máy nhà

1. XAMPP: **MySQL** và **Apache**
2. `loginServer\run.bat`
3. `run.bat` (game server)
4. `frpc` — cài service ở B4 rồi thì nó tự chạy

Game server tự thử kết nối lại login server mỗi giây nên bước 2 và 3 không khắt khe
về thứ tự, nhưng bật login server trước cho sạch log.

### Xem log

| Ở đâu | Lệnh / nơi xem |
|---|---|
| frps trên VPS | `sudo journalctl -u frps -f` hoặc `/var/log/frps.log` |
| frpc máy nhà | cửa sổ CMD, hoặc log NSSM |
| game server | cửa sổ console của `run.bat` |

### Lỗi hay gặp

**`Remote Desktop can't connect`** — bạn đang dùng `mstsc` để vào Linux. Dùng
`ssh root@180.93.57.30` thay thế. Linux không có RDP.

**`login to server failed` ở frpc** — sai token, sai `serverAddr`, cổng 7000 chưa mở
trên ufw hoặc firewall nhà phân phối, hoặc **frpc và frps khác version**.

**`port already used` ở frps** — có tiến trình khác đang giữ 19129 hoặc 80 trên VPS.

**frpc báo thành công nhưng không vào được game** — game server ở nhà chưa chạy.
Kiểm tra: `netstat -an | findstr 19129` trên máy nhà.

**Người chơi bị "Vui lòng kích hoạt tài khoản"** — tài khoản chưa có dòng trong `5h_active`:

```sql
INSERT INTO kpah_db.5h_active (userID, username, time_end)
SELECT id, username, UNIX_TIMESTAMP() + 2592000 FROM account.team_user
WHERE id NOT IN (SELECT userID FROM kpah_db.5h_active);
```

Hoặc `server.ini` → `sv.EnableAvtiveCheck=0` rồi khởi động lại server.

---

## Ba điều phải nhớ khi dùng frp

frpc kết nối tới game server qua loopback, nên `clientSocket.getInetAddress()` ở
`TeamServer.java:639` luôn trả về `127.0.0.1` cho **mọi** người chơi:

1. **Chống DoS trong game bị vô hiệu.** `isLocalIp()` coi `127.0.0.1` là nội bộ nên
   `dosAccept()` cho qua ngay. `sv.maxConnPerIp`, `sv.connRateMax`, `sv.dosBanSec`
   trong `server.ini` không còn tác dụng. Đã bù bằng iptables ở bước A8.

2. **Ban IP hết dùng được.** Bảng chặn IP giờ chỉ có một khoá `127.0.0.1` — ban nó là
   ban sạch người chơi. Cấm ai thì cấm bằng tài khoản hoặc nhân vật.

3. **Sau 10 lượt đăng nhập, mọi người dính delay 3 giây.** `LoginHandler.java:243` đếm
   theo IP mà cả server chung một IP. Không chết ai, chỉ hơi ì. `SelectCharHandler.java:82`
   cũng có delay 2 giây tương tự.

## Bảo mật

- Đổi token frp định kỳ, đổi ngay nếu lỡ lộ.
- Trên VPS chỉ mở đúng 4 cổng: 22, 7000, 19129, 80.
- **Tuyệt đối không** tạo proxy frp cho cổng 8023 và 19153.
- Nên chuyển SSH sang xác thực bằng key thay vì mật khẩu, và đổi cổng 22 sang cổng khác.
