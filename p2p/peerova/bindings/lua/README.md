# Peerova cho Lua

[Tiếng Việt](#tiếng-việt) · [English](#english)

Website: https://peerova.pages.dev · Khái niệm và toàn bộ C API: [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md)

---

## Tiếng Việt

Hai module, **cùng một API**:

| Module | Lua | Cần gì | `poll()` |
|---|---|---|---|
| `peerova.lua` | LuaJIT 2.0/2.1 (LÖVE, Defold, OpenResty…) | thư viện `libpeerova.so` / `peerova.dll` | không chặn, hoặc chờ tối đa `timeout` giây |
| `peerova_stdio.lua` | Lua 5.1 / 5.2 / 5.3 / 5.4 thuần (POSIX) | chương trình sidecar `peerova` | **luôn chặn** tới sự kiện kế tiếp |

Kết nối hoàn toàn tự động: thử kết nối trực tiếp trước, nếu mạng chặn thì tự dùng **đường mã hoá tự động** (vẫn mã hoá đầu-cuối).

### Chọn cách tích hợp

* **Game mới, hoặc host trong game** (tình huống 3/4 của hướng dẫn): dùng SDK — `peerova.lua` với LuaJIT (LÖVE, Defold, OpenResty), hoặc
  `peerova_stdio.lua` với Lua thuần.
* **Game có sẵn dùng socket TCP/UDP, người chơi không phải chạy gì** (tình huống 2): Lua rất hạn chế trong việc chạy process — xem
  [Game có sẵn: chạy peerova ẩn](#game-có-sẵn-chạy-peerova-ẩn) (chỉ Linux/macOS); thường tốt hơn là dùng SDK hoặc launcher native.
* Bảng so sánh đầy đủ: [hướng dẫn §1](../DEVELOPER-GUIDE.md#1-chọn-cách-tích-hợp).

**Người chơi không phải gõ gì:** nhúng ticket `pvt_...` vào game, hoặc tải ticket mới nhất từ URL của bạn khi game khởi động (dùng ticket
nhúng nếu tải không được). Mã 6 ký tự chỉ để mời bạn bè vào phòng do người chơi mở. Chi tiết:
[hướng dẫn §2](../DEVELOPER-GUIDE.md#2-người-chơi-không-phải-gõ-gì).

### Yêu cầu

* `peerova.lua`: LuaJIT (module `ffi`). Đã kiểm thử với libluajit 2.1 trên Linux x64.
* `peerova_stdio.lua`: Lua 5.1–5.4, hệ POSIX (`sh`, `mkfifo`, `io.popen`). Đã kiểm thử với Lua 5.4 trên Linux.
  Trên Windows hãy dùng LuaJIT + `peerova.lua`.

### Bộ SDK gồm những gì

```
include/peerova.h                    C header (tham chiếu)
lib/linux-x86_64/libpeerova.so       thư viện Linux (+ libpeerova.a, và chương trình sidecar peerova)
lib/windows-x86_64/peerova.dll       thư viện Windows (+ peerova.exe)
bindings/lua/                        peerova.lua, peerova_stdio.lua, examples/, test/
```

### Đặt thư viện native / chương trình sidecar ở đâu

`peerova.lua` tìm theo thứ tự: `os.getenv("PEEROVA_LIB")` → cạnh file `peerova.lua` → bộ nạp hệ thống
(`ffi.load("peerova")`). `peerova_stdio.lua` tìm: `opts.binary` → `PEEROVA_BIN` → `peerova` trong `PATH`.

| Hệ điều hành | `peerova.lua` | `peerova_stdio.lua` |
|---|---|---|
| Linux | `libpeerova.so` cạnh `peerova.lua`, hoặc `PEEROVA_LIB=/opt/peerova/libpeerova.so`, hoặc `LD_LIBRARY_PATH` | `peerova` (nhớ `chmod +x`) trong `PATH` hoặc `PEEROVA_BIN` |
| Windows | `peerova.dll` cạnh file `.exe` của game / cạnh `peerova.lua`, hoặc `PEEROVA_LIB`. **Không** cần `wintun.dll` (chỉ chế độ VPN của công cụ dòng lệnh `peerova` mới cần) | không hỗ trợ |
| macOS | liên hệ Peerova để nhận bản macOS (`libpeerova.dylib`); trong app bundle đặt ở `Contents/Frameworks/` và trỏ `PEEROVA_LIB` tới đó | liên hệ Peerova |
| Android / iOS | liên hệ Peerova; Android: `jniLibs/<abi>/libpeerova.so`, iOS: `libpeerova.a` liên kết tĩnh | — |

LÖVE: đặt `libpeerova.so` / `peerova.dll` cạnh file thực thi của game khi phát hành (thư mục `.love` không nạp được
thư viện native), rồi `require "peerova"`.

### Cài đặt

Chép `peerova.lua` (và/hoặc `peerova_stdio.lua`) vào thư mục module của dự án (nằm trong `package.path`).

### Bắt đầu trong 5 phút

Server:

```lua
local peerova = require "peerova"                    -- hoặc require "peerova_stdio"
local srv = peerova.server { port = 47000, key_file = "server.key" }  -- chặn tối đa ~3 s
print("ticket: " .. srv.ticket)                      -- pvt_... — nhúng vào game / gửi cho client
print("code: " .. tostring(srv:room_code()))         -- mã 6 ký tự (chỉ peerova.lua, chỉ khi có app + share); có thể nil lúc đầu
```

Client:

```lua
-- ticket "pvt_..." hoặc mã 6 ký tự; key_file giữ danh tính người chơi giữa các lần chạy (thư mục phải tồn tại)
local cli = peerova.client(ticket_or_code, { key_file = save_dir .. "/peerova-client.key" })
local ev = cli:poll(20)                              -- chờ "connected" / "connect_failed"
if ev and ev.type == "connected" then
  cli:send(ev.peer, "xin chào")                      -- reliable, kênh 0
end
```

Với ticket, `client()` trả về ngay. Với mã kết nối, nó tra mã trên website trước (chặn chốc lát) và báo lỗi nếu mã
không tồn tại. `peerova_stdio.lua` không đọc được mã kết nối của server (không có `room_code()`), nhưng client vẫn
kết nối bằng mã được.

Ví dụ chạy được:

```sh
cd bindings/lua
luajit examples/chat.lua server                     # chat + vị trí 20 Hz, kiểu game loop (in ra ticket)
luajit examples/chat.lua client <ticket|mã>         # gõ để chat, /quit để thoát
examples/run_chat.sh server                         # như trên khi máy không có lệnh luajit (dùng libluajit)
lua examples/echo_stdio.lua server                  # Lua thuần qua sidecar: server echo
lua examples/echo_stdio.lua client <ticket|mã>
```

### Gửi: reliable hay unreliable

`node:send(peer, data, { channel = 0, reliable = true })` — `data` là chuỗi Lua (nhị phân được).

| | `reliable = true` (mặc định) | `reliable = false` |
|---|---|---|
| Đảm bảo | đến đủ, đúng thứ tự **trong cùng kênh** | có thể mất hoặc đảo thứ tự |
| Kích thước | lớn tuỳ ý | tối đa **1180 byte** mỗi tin (`peerova.MAX_UNRELIABLE_PAYLOAD`); lớn hơn sẽ bị từ chối |
| Dùng cho | chat, túi đồ, RPC, lệnh, khối dữ liệu file | input gameplay, vị trí, trạng thái thời gian thực kiểu voice — tin sau thay thế tin trước |

Kênh 0..255. Các kênh khác nhau không chặn nhau.

### Nhận sự kiện

`node:poll(timeout)` trả về bảng `{ type, peer, channel, reliable, data }` hoặc `nil`. `node:events(timeout)` là
iterator dùng với `for ... in`.

| `ev.type` | Ý nghĩa | `ev.data` |
|---|---|---|
| `"connected"` | server: một client vào; client: đã kết nối tới server | `""` |
| `"message"` | có tin nhắn (`ev.channel`, `ev.reliable`) | nội dung |
| `"disconnected"` | kết nối đã đóng | lý do |
| `"connect_failed"` | chỉ client: không kết nối được (hết giờ, sai token, server đầy…) | lý do |

Vòng lặp theo khung hình (LuaJIT) — xả hết sự kiện mỗi frame, không bao giờ chặn:

```lua
-- LÖVE
function love.update(dt)
  local ev = node:poll()          -- không chặn
  while ev do handle(ev); ev = node:poll() end
end
-- Defold: làm tương tự trong function update(self, dt) của script.
-- OpenResty: ngx.timer.every(0.02, function() ... node:poll() ... end) trong một worker.
```

Server riêng (LuaJIT): `while running do local ev = srv:poll(0.05) ... end` (chờ tối đa 50 ms mỗi vòng).

`peerova_stdio.lua`: `poll()` luôn chặn tới sự kiện kế tiếp — viết chương trình theo kiểu phản ứng với từng sự kiện
(`for ev in node:events() do ... end`, xem `examples/echo_stdio.lua`), mỗi node một tiến trình. Cần vòng lặp game
không chặn thì dùng LuaJIT + `peerova.lua`.

### Game có sẵn: chạy peerova ẩn

Với game đã có code socket TCP/UDP: game tự chạy `peerova client`, chờ cổng local mở, rồi code socket cũ kết nối `127.0.0.1:8888`.
Máy chủ chạy `peerova server --expose tcp:8888` ([hướng dẫn §3](../DEVELOPER-GUIDE.md#3-game-có-sẵn-không-sửa-code)). Chi tiết các dòng log:
[hướng dẫn §4](../DEVELOPER-GUIDE.md#4-game-tự-chạy-peerova-ẩn-người-chơi-không-chạy-gì).

**Nói thẳng về giới hạn của Lua:** Lua chuẩn chỉ có `io.popen` — đọc được một luồng, không chặn được việc đọc, không có PID nên
không kill chắc chắn, và trên Windows nó chạy qua `cmd.exe` (cửa sổ console có thể hiện ra, không ẩn được). LÖVE không có API chạy process.
Vì vậy:

* **Khuyến nghị:** LÖVE / Defold / OpenResty đều là LuaJIT → dùng thẳng SDK `peerova.lua` ở trên (không cần process con, chạy cả Windows).
  Hoặc để launcher viết bằng ngôn ngữ khác (C#, C++, Go, Python…) chạy `peerova` — có sẵn code trong hướng dẫn §4.
* Chỉ khi buộc phải giữ code socket cũ trên **Linux/macOS**: dùng đoạn dưới. Nó chạy `peerova` nền qua `sh`, ghi PID (để dừng bằng
  `kill`) và ghi output vào file log (nên không pipe nào bị đầy, và `status()` không chặn game loop).

```lua
-- peerova_tunnel.lua — POSIX only (Linux, macOS); plain Lua 5.1–5.4, LuaJIT, LÖVE.
-- Chạy chương trình `peerova` đi kèm ở chế độ nền; output (UTF-8) ghi vào file log,
-- nên không phải liên tục đọc pipe và status() không bao giờ chặn (gọi mỗi frame).
local T = {}
T.__index = T

local function q(s) return "'" .. tostring(s):gsub("'", "'\\''") .. "'" end
local function ok(r) return r == true or r == 0 end      -- os.execute: 5.1/LuaJIT → 0, 5.2+ → true

--- Trả về đối tượng tunnel (process đang chạy), hoặc nil nếu không chạy được.
local function start(exe, ticket, forward, key_file, log_file)
  os.execute("mkdir -p " .. q(key_file:match("^(.*)/[^/]*$") or "."))   -- thư mục chứa key phải tồn tại
  os.remove(log_file)
  local p = io.popen(("(exec %s client %s --forward %s --key %s </dev/null >%s 2>&1) & echo $!")
    :format(q(exe), q(ticket), q(forward), q(key_file), q(log_file)))
  local pid = tonumber(p:read("*l"))                       -- PID của peerova chạy nền
  p:close()
  if not pid then return nil end
  return setmetatable({ pid = pid, log = log_file }, T)
end

function T:alive() return ok(os.execute("kill -0 " .. self.pid .. " 2>/dev/null")) end

--- Không chặn: "ready" (cổng local đã mở), "waiting", hoặc "failed" (thoát trước; xem log).
function T:status()
  local f = io.open(self.log, "r")
  local text = f and f:read("*a") or ""
  if f then f:close() end
  -- "✔ Tcp 127.0.0.1:8888 → server port 8888" = đã kết nối và cổng local đã mở
  if text:find("server port", 1, true) then return "ready" end
  return self:alive() and "waiting" or "failed"
end

--- Chờ có chặn, cho script (không dùng trong game loop): true khi sẵn sàng, false khi lỗi / hết giờ.
function T:wait(timeout)
  local deadline = os.time() + (timeout or 30)
  while os.time() <= deadline do
    local s = self:status()
    if s ~= "waiting" then if s == "failed" then self:stop() end return s == "ready" end
    os.execute("sleep 0.1")
  end
  self:stop()
  return false
end

--- Dừng peerova. Gọi khi game thoát.
function T:stop() os.execute("kill " .. self.pid .. " 2>/dev/null") end

return { start = start }
```

```lua
local tunnel = require "peerova_tunnel"
local key = os.getenv("HOME") .. "/.local/share/MyGame/peerova-client.key"
local t = tunnel.start("./peerova", TICKET, "tcp:8888", key, os.getenv("HOME") .. "/.local/share/MyGame/peerova.log")

-- LÖVE: kiểm tra mỗi frame, không bao giờ chặn
local waited = 0
function love.update(dt)
  if t and not connected then
    local s = t:status()
    waited = waited + dt
    if s == "ready" then connected = true; connect_to_game_server("127.0.0.1", 8888)   -- code socket có sẵn
    elseif s == "failed" or waited > 30 then t:stop(); t = nil; show_error("Không kết nối được máy chủ") end
  end
end
function love.quit() if t then t:stop() end end

-- script thường: if t and t:wait(30) then ... end
```

* Đã kiểm thử trên Linux với Lua 5.4: đúng ticket → `ready`, HTTP qua `127.0.0.1` chạy được, `stop()` dừng process; ticket sai → `failed`,
  log chứa dòng `lỗi: ...`.
* Chỉ POSIX (cần `sh`, `kill`, `mkdir -p`; `wait()` dùng `sleep 0.1`). Windows: dùng `peerova.lua` hoặc launcher native.
* File `peerova` phải có quyền chạy (`chmod +x`); macOS: ký (codesign) cùng app. Nhiều `--forward` → chờ đủ số dòng `server port`.
* Mobile / console không chạy được process con → dùng SDK.
* `peerova stdio` (module `peerova_stdio.lua`) **không** mở cổng local: nó trao đổi message SDK dạng JSON lines — không dùng được cho cách này.

### Ngắt kết nối và dọn dẹp

* `node:disconnect(peer)` đóng một kết nối (sau đó có sự kiện `"disconnected"`).
* `node:close()` ngắt mọi kết nối và giải phóng node / dừng sidecar. Gọi nhiều lần không sao. Với `peerova.lua`, GC
  cũng giải phóng node bị quên, nhưng hãy tự gọi `close()`.
* Không `close()` khi một luồng khác (ví dụ `love.thread`) đang `poll()` trên node đó.

### Xử lý lỗi

* Các hàm báo lỗi bằng `error("peerova: <thông báo>")` (thông báo từ `peerova_last_error`): cấu hình sai, cổng đang
  dùng, ticket hỏng, mã không tồn tại, peer không tồn tại, gói unreliable > 1180 byte… Bắt bằng `pcall`:
  ```lua
  local ok, err = pcall(node.send, node, peer, data, { reliable = false })
  ```
* Thông báo lỗi của thư viện, và lý do trong sự kiện `"connect_failed"` / `"disconnected"` (`ev.data`), chứa tên mã lỗi ổn định,
  ví dụ `"... peerova: E_INVALID_TICKET: ..."`, `"E_TIMEOUT: ..."`. Xử lý theo mã, không phân tích phần còn lại:
  ```lua
  local ok, err = pcall(peerova.client, code)
  if not ok and tostring(err):match("E_CODE_NOT_FOUND") then show_error("Phòng không tồn tại hoặc đã đóng") end
  if ev.type == "connect_failed" and ev.data:match("^E_CCU_LIMIT") then show_error("Máy chủ đầy") end
  ```
  Danh sách mã (`E_INVALID_ARG`, `E_TOO_LARGE`, `E_UNKNOWN_PEER`, `E_CLOSED`, `E_CODE_NOT_FOUND`, `E_NETWORK`, `E_TIMEOUT`, `E_LICENSE`,
  `E_CCU_LIMIT`, `E_ACCESS_DENIED`, `E_INVALID_TICKET`, `E_IO`, `E_INTERNAL`): [hướng dẫn — Mã lỗi](../DEVELOPER-GUIDE.md#mã-lỗi).
* `peerova_stdio.lua`: lỗi khởi động → `error("peerova_stdio: sidecar exited before ready ...")` (không kèm mã lỗi), lý do chi tiết in ra
  stderr; lỗi khi gửi (ví dụ peer không tồn tại) được thêm vào bảng `node.errors`.
* Lỗi mạng của client đến dưới dạng sự kiện `"connect_failed"`, không phải error.

### Cấu hình

Server — `peerova.server { ... }`:

| Khoá | Mặc định | Ý nghĩa |
|---|---|---|
| `port` | `47000` | cổng UDP; `0` = ngẫu nhiên |
| `key_file` | `"server.key"` | file danh tính server; cùng key ⇒ cùng ticket. **Bí mật** |
| `license` | — | khoá `PVA-...`; không có ⇒ chế độ dev, tối đa 3 client |
| `max_ccu` | theo license | giới hạn số client đồng thời |
| `token` | — | mật khẩu chung client phải gửi |
| `upnp` | `true` | tự mở cổng trên router |
| `private` | `false` | ticket không chứa địa chỉ IP của server |
| `hide_ip` | `false` | Ẩn IP máy chủ: người chơi không bao giờ biết IP thật của máy chủ (chống DDoS); ping cao hơn một chút. Gói Pro/Studio/Enterprise và bản dùng thử |
| `app` | — | mã dự án trên website Peerova (cần cho mã kết nối) |
| `share` | — | `{ name = "...", public = true, max = 16, mode = "...", region = "VN" }` — đăng phòng và nhận mã 6 ký tự; `public` = hiện trong danh sách phòng công khai |

Client — `peerova.client(ticket_or_code, { ... })`:

| Khoá | Mặc định | Ý nghĩa |
|---|---|---|
| `token` | — | phải khớp server |
| `timeout_ms` | `20000` | thời gian tối đa trước `"connect_failed"` |
| `key_file` | — (key ngẫu nhiên mỗi lần chạy) | file danh tính người chơi; tạo mới nếu chưa có (**thư mục phải tồn tại**). Lưu trong thư mục dữ liệu của người dùng, ví dụ `"<appdata>/<game>/peerova-client.key"` (LÖVE: `love.filesystem.getSaveDirectory()`). Giữ file này thì máy chủ thấy người chơi ở cùng một địa chỉ ổn định; cấm theo địa chỉ chỉ có tác dụng với từng người chơi khi họ còn giữ file này |

Có thể truyền chuỗi JSON thay cho bảng.

### Máy chủ game

Máy chủ dedicated thường chạy công cụ dòng lệnh `peerova server` (nhiều `--expose`, tách process cho Remote Desktop và game,
mỗi người chơi một địa chỉ `127.a.b.c` + `--banned`, `--hide-ip`, chế độ VPN): xem [hướng dẫn §7](../DEVELOPER-GUIDE.md#7-tính-năng-máy-chủ-game).

### Bảo mật

* `server.key` là danh tính của server: ai có file này có thể giả mạo server. Giữ bí mật, không commit vào git, không
  đóng gói vào bản game phát cho người chơi.
* Ticket không phải bí mật: nhúng vào game hoặc gửi cho client thoải mái. `token` và `license` là bí mật — không nhúng license vào bản game phát hành.
* Mọi dữ liệu được mã hoá đầu-cuối.

### Khắc phục sự cố

* Chạy `peerova doctor` (công cụ dòng lệnh trong SDK) để chẩn đoán mạng.
* `ffi.load` lỗi / không tìm thấy thư viện: kiểm tra vị trí thư viện hoặc đặt `PEEROVA_LIB`.
* `"connect_failed"` do hết giờ: server chưa chạy, sai ticket/mã, hoặc tường lửa; tăng `timeout_ms`, chạy `peerova doctor` ở cả hai đầu.
* Client thứ 4 bị từ chối: server đang ở chế độ dev (tối đa 3 client) — thêm `license`.
* Gửi unreliable > 1180 byte bị từ chối — chia nhỏ hoặc dùng reliable.
* `E_IO`: thư mục chứa `key_file` không tồn tại hoặc không ghi được.
* Server không khởi động khi bật `hide_ip`: gói hiện tại không có ẩn IP (cần Pro/Studio/Enterprise hoặc bản dùng thử).
* Thêm: [hướng dẫn §15](../DEVELOPER-GUIDE.md#15-khắc-phục-sự-cố).

### FAQ

* **Người chơi có phải gõ mã không?** Không. Nhúng ticket hoặc tải từ URL của bạn; mã 6 ký tự chỉ để mời bạn bè vào phòng do người chơi mở.
* **Game LÖVE có chạy `peerova client` ẩn được không?** Trên Windows thì không ẩn được bằng Lua; dùng `peerova.lua` (LÖVE có LuaJIT) — đơn giản và chắc chắn hơn.
* **Có cần mở port / IP tĩnh không?** Không; mở port chỉ giúp kết nối trực tiếp nhanh hơn.
* Thêm: [hướng dẫn §16](../DEVELOPER-GUIDE.md#16-faq).

### Kiểm thử

```sh
cd bindings/lua
lua test/test_peerova_stdio.lua      # Lua thuần + sidecar
test/run_luajit.sh                   # LuaJIT FFI (dùng luajit, hoặc tự dựng host nhúng libluajit)
```

---

## English

Two modules, **one API**:

| Module | Lua | Needs | `poll()` |
|---|---|---|---|
| `peerova.lua` | LuaJIT 2.0/2.1 (LÖVE, Defold, OpenResty, ...) | `libpeerova.so` / `peerova.dll` | non-blocking, or waits up to `timeout` seconds |
| `peerova_stdio.lua` | plain Lua 5.1 / 5.2 / 5.3 / 5.4 (POSIX) | the `peerova` sidecar program | **always blocks** until the next event |

Connections are automatic: direct first, with the **automatic encrypted route** (still end-to-end encrypted) when networks
block direct connections.

### Choosing an integration

* **New game, or host-in-game** (scenarios 3/4 of the guide): use the SDK — `peerova.lua` with LuaJIT (LÖVE, Defold, OpenResty), or
  `peerova_stdio.lua` with plain Lua.
* **Existing game with TCP/UDP socket code, players must not run anything** (scenario 2): Lua is very limited at running processes — see
  [Existing game: run peerova hidden](#existing-game-run-peerova-hidden) (Linux/macOS only); the SDK or a native launcher is usually better.
* Full comparison: [guide §1](../DEVELOPER-GUIDE.md#1-choosing-an-integration).

**Players never type anything:** embed the `pvt_...` ticket in the game, or fetch the latest ticket from your own URL at startup (falling
back to the embedded one). 6-character codes are only for inviting friends to player-hosted rooms. Details:
[guide §2](../DEVELOPER-GUIDE.md#2-players-never-type-anything).

### Requirements

* `peerova.lua`: LuaJIT (the `ffi` module). Tested with libluajit 2.1 on Linux x64.
* `peerova_stdio.lua`: Lua 5.1–5.4 on a POSIX system (`sh`, `mkfifo`, `io.popen`). Tested with Lua 5.4 on Linux.
  On Windows use LuaJIT + `peerova.lua`.

### SDK contents

```
include/peerova.h                    C header (reference)
lib/linux-x86_64/libpeerova.so       Linux library (+ libpeerova.a, and the peerova sidecar program)
lib/windows-x86_64/peerova.dll       Windows library (+ peerova.exe)
bindings/lua/                        peerova.lua, peerova_stdio.lua, examples/, test/
```

### Where to put the native library / sidecar program

`peerova.lua` looks in: `os.getenv("PEEROVA_LIB")` → next to `peerova.lua` → the system loader
(`ffi.load("peerova")`). `peerova_stdio.lua` looks in: `opts.binary` → `PEEROVA_BIN` → `peerova` on `PATH`.

| OS | `peerova.lua` | `peerova_stdio.lua` |
|---|---|---|
| Linux | `libpeerova.so` next to `peerova.lua`, or `PEEROVA_LIB=/opt/peerova/libpeerova.so`, or `LD_LIBRARY_PATH` | `peerova` (remember `chmod +x`) on `PATH` or `PEEROVA_BIN` |
| Windows | `peerova.dll` next to the game's `.exe` / next to `peerova.lua`, or `PEEROVA_LIB`. `wintun.dll` is **not** needed (only the VPN mode of the `peerova` command-line tool uses it) | not supported |
| macOS | contact Peerova for macOS builds (`libpeerova.dylib`); in an app bundle put it in `Contents/Frameworks/` and point `PEEROVA_LIB` at it | contact Peerova |
| Android / iOS | contact Peerova; Android: `jniLibs/<abi>/libpeerova.so`, iOS: `libpeerova.a` linked statically | — |

LÖVE: ship `libpeerova.so` / `peerova.dll` next to the game executable (native libraries cannot be loaded from inside
a `.love` archive), then `require "peerova"`.

### Install

Copy `peerova.lua` (and/or `peerova_stdio.lua`) into your project's module directory (on `package.path`).

### 5-minute quick start

Server:

```lua
local peerova = require "peerova"                    -- or require "peerova_stdio"
local srv = peerova.server { port = 47000, key_file = "server.key" }  -- blocks up to ~3 s
print("ticket: " .. srv.ticket)                      -- pvt_... — embed it in the game / give it to clients
print("code: " .. tostring(srv:room_code()))         -- 6-char code (peerova.lua only, only with app + share); may be nil at first
```

Client:

```lua
-- "pvt_..." ticket or 6-char code; key_file keeps the player's identity across runs (the folder must exist)
local cli = peerova.client(ticket_or_code, { key_file = save_dir .. "/peerova-client.key" })
local ev = cli:poll(20)                              -- wait for "connected" / "connect_failed"
if ev and ev.type == "connected" then
  cli:send(ev.peer, "hello")                         -- reliable, channel 0
end
```

With a ticket, `client()` returns immediately. With a connect code it first looks the code up on the website
(blocks briefly) and raises an error if the code is unknown. `peerova_stdio.lua` cannot read the server's connect code
(no `room_code()`), but its clients can still connect with a code.

Runnable examples:

```sh
cd bindings/lua
luajit examples/chat.lua server                     # chat + 20 Hz positions, game-loop style (prints the ticket)
luajit examples/chat.lua client <ticket|code>       # type to chat, /quit to leave
examples/run_chat.sh server                         # same, when no luajit command is installed (uses libluajit)
lua examples/echo_stdio.lua server                  # plain Lua via the sidecar: echo server
lua examples/echo_stdio.lua client <ticket|code>
```

### Sending: reliable vs unreliable

`node:send(peer, data, { channel = 0, reliable = true })` — `data` is a Lua string (binary-safe).

| | `reliable = true` (default) | `reliable = false` |
|---|---|---|
| Guarantee | delivered, in order **within the channel** | may be lost or reordered |
| Size | any size | at most **1180 bytes** per message (`peerova.MAX_UNRELIABLE_PAYLOAD`); larger sends are rejected |
| Use for | chat, inventory, RPCs, commands, file chunks | gameplay inputs, positions, voice-like realtime state — each update supersedes the previous one |

Channels are 0..255. Different channels don't block each other.

### Receiving events

`node:poll(timeout)` returns a table `{ type, peer, channel, reliable, data }` or `nil`. `node:events(timeout)` is an
iterator for `for ... in`.

| `ev.type` | Meaning | `ev.data` |
|---|---|---|
| `"connected"` | server: a client joined; client: connected to the server | `""` |
| `"message"` | a message (`ev.channel`, `ev.reliable`) | payload |
| `"disconnected"` | the connection closed | reason |
| `"connect_failed"` | client only: could not connect (timeout, wrong token, server full, ...) | reason |

Frame loop (LuaJIT) — drain all events every frame, never block:

```lua
-- LÖVE
function love.update(dt)
  local ev = node:poll()          -- non-blocking
  while ev do handle(ev); ev = node:poll() end
end
-- Defold: do the same in the script's function update(self, dt).
-- OpenResty: ngx.timer.every(0.02, function() ... node:poll() ... end) in one worker.
```

Dedicated server (LuaJIT): `while running do local ev = srv:poll(0.05) ... end` (waits up to 50 ms per round).

`peerova_stdio.lua`: `poll()` always blocks until the next event — write the program in a react-to-each-event style
(`for ev in node:events() do ... end`, see `examples/echo_stdio.lua`), one node per process. For a non-blocking game
loop use LuaJIT + `peerova.lua`.

### Existing game: run peerova hidden

For a game that already has TCP/UDP socket code: the game starts `peerova client`, waits for the local port to open, then the existing
socket code connects to `127.0.0.1:8888`. The server runs `peerova server --expose tcp:8888` ([guide §3](../DEVELOPER-GUIDE.md#3-existing-game-no-code-change)).
Log lines in detail: [guide §4](../DEVELOPER-GUIDE.md#4-the-game-runs-peerova-hidden-players-run-nothing).

**Lua's limits, honestly:** standard Lua only has `io.popen` — one stream, blocking reads, no PID so no reliable kill, and on Windows it
goes through `cmd.exe` (a console window may appear and cannot be hidden). LÖVE has no process API. Therefore:

* **Recommended:** LÖVE / Defold / OpenResty are all LuaJIT → use the `peerova.lua` SDK above directly (no child process, works on Windows too).
  Or let a launcher written in another language (C#, C++, Go, Python…) run `peerova` — ready-made code is in guide §4.
* Only when you must keep the old socket code on **Linux/macOS**: use the snippet below. It starts `peerova` in the background through `sh`,
  records the PID (to stop it with `kill`) and sends the output to a log file (so no pipe fills up and `status()` never blocks the game loop).

```lua
-- peerova_tunnel.lua — POSIX only (Linux, macOS); plain Lua 5.1–5.4, LuaJIT, LÖVE.
-- Starts the bundled `peerova` CLI in the background; its output (UTF-8) goes to a log file,
-- so nothing has to keep draining a pipe and status() never blocks (call it once per frame).
local T = {}
T.__index = T

local function q(s) return "'" .. tostring(s):gsub("'", "'\\''") .. "'" end
local function ok(r) return r == true or r == 0 end      -- os.execute: 5.1/LuaJIT → 0, 5.2+ → true

--- Returns a tunnel object (the process is running), or nil if it could not be started.
local function start(exe, ticket, forward, key_file, log_file)
  os.execute("mkdir -p " .. q(key_file:match("^(.*)/[^/]*$") or "."))   -- the key folder must exist
  os.remove(log_file)
  local p = io.popen(("(exec %s client %s --forward %s --key %s </dev/null >%s 2>&1) & echo $!")
    :format(q(exe), q(ticket), q(forward), q(key_file), q(log_file)))
  local pid = tonumber(p:read("*l"))                       -- PID of the background peerova
  p:close()
  if not pid then return nil end
  return setmetatable({ pid = pid, log = log_file }, T)
end

function T:alive() return ok(os.execute("kill -0 " .. self.pid .. " 2>/dev/null")) end

--- Non-blocking: "ready" (local port open), "waiting", or "failed" (exited first; see the log).
function T:status()
  local f = io.open(self.log, "r")
  local text = f and f:read("*a") or ""
  if f then f:close() end
  -- "✔ Tcp 127.0.0.1:8888 → server port 8888" = connected and the local port is open
  if text:find("server port", 1, true) then return "ready" end
  return self:alive() and "waiting" or "failed"
end

--- Blocking wait for scripts (not for a game loop): true when ready, false on failure/timeout.
function T:wait(timeout)
  local deadline = os.time() + (timeout or 30)
  while os.time() <= deadline do
    local s = self:status()
    if s ~= "waiting" then if s == "failed" then self:stop() end return s == "ready" end
    os.execute("sleep 0.1")
  end
  self:stop()
  return false
end

--- Stops peerova. Call it when the game exits.
function T:stop() os.execute("kill " .. self.pid .. " 2>/dev/null") end

return { start = start }
```

```lua
local tunnel = require "peerova_tunnel"
local key = os.getenv("HOME") .. "/.local/share/MyGame/peerova-client.key"
local t = tunnel.start("./peerova", TICKET, "tcp:8888", key, os.getenv("HOME") .. "/.local/share/MyGame/peerova.log")

-- LÖVE: check once per frame, never block
local waited = 0
function love.update(dt)
  if t and not connected then
    local s = t:status()
    waited = waited + dt
    if s == "ready" then connected = true; connect_to_game_server("127.0.0.1", 8888)   -- existing socket code
    elseif s == "failed" or waited > 30 then t:stop(); t = nil; show_error("Could not reach the server") end
  end
end
function love.quit() if t then t:stop() end end

-- plain script: if t and t:wait(30) then ... end
```

* Tested on Linux with Lua 5.4: valid ticket → `ready`, HTTP through `127.0.0.1` works, `stop()` ends the process; bad ticket → `failed`,
  the log contains a `lỗi: ...` line.
* POSIX only (needs `sh`, `kill`, `mkdir -p`; `wait()` uses `sleep 0.1`). Windows: use `peerova.lua` or a native launcher.
* The `peerova` file must be executable (`chmod +x`); macOS: codesign it with the app. Several `--forward` → wait for that many `server port` lines.
* Mobile / consoles can't spawn child processes → use the SDK.
* `peerova stdio` (the `peerova_stdio.lua` module) does **not** open a local port: it exchanges SDK messages as JSON lines — it can't be used here.

### Disconnect and cleanup

* `node:disconnect(peer)` closes one connection (a `"disconnected"` event follows).
* `node:close()` drops all connections and frees the node / stops the sidecar. Calling it twice is fine. With
  `peerova.lua` the GC also frees forgotten nodes, but call `close()` yourself.
* Never `close()` while another thread (e.g. a `love.thread`) is inside `poll()` on that node.

### Error handling

* Functions raise `error("peerova: <message>")` (message from `peerova_last_error`): bad config, port in use, malformed
  ticket, unknown code, unknown peer, unreliable payload > 1180 bytes, ... Catch with `pcall`:
  ```lua
  local ok, err = pcall(node.send, node, peer, data, { reliable = false })
  ```
* Library error messages, and the reason in `"connect_failed"` / `"disconnected"` events (`ev.data`), contain a stable error-code name,
  e.g. `"... peerova: E_INVALID_TICKET: ..."`, `"E_TIMEOUT: ..."`. Branch on the code; don't parse the rest:
  ```lua
  local ok, err = pcall(peerova.client, code)
  if not ok and tostring(err):match("E_CODE_NOT_FOUND") then show_error("That room does not exist or has closed") end
  if ev.type == "connect_failed" and ev.data:match("^E_CCU_LIMIT") then show_error("Server is full") end
  ```
  Code list (`E_INVALID_ARG`, `E_TOO_LARGE`, `E_UNKNOWN_PEER`, `E_CLOSED`, `E_CODE_NOT_FOUND`, `E_NETWORK`, `E_TIMEOUT`, `E_LICENSE`,
  `E_CCU_LIMIT`, `E_ACCESS_DENIED`, `E_INVALID_TICKET`, `E_IO`, `E_INTERNAL`): [guide — Error codes](../DEVELOPER-GUIDE.md#error-codes).
* `peerova_stdio.lua`: a startup failure raises `error("peerova_stdio: sidecar exited before ready ...")` (no error code) with the
  exact reason printed on stderr; send errors (e.g. unknown peer) are appended to the `node.errors` table.
* Client network failures arrive as a `"connect_failed"` event, not as errors.

### Configuration

Server — `peerova.server { ... }`:

| Key | Default | Meaning |
|---|---|---|
| `port` | `47000` | UDP port; `0` = random |
| `key_file` | `"server.key"` | server identity file; same key ⇒ same ticket. **Secret** |
| `license` | — | `PVA-...` key; without it: dev mode, max 3 clients |
| `max_ccu` | from license | cap on concurrent clients |
| `token` | — | shared secret clients must present |
| `upnp` | `true` | open the port on the router automatically |
| `private` | `false` | the ticket does not contain the server's IP addresses |
| `hide_ip` | `false` | Hide the server IP: players never learn its real address (DDoS protection); slightly higher latency. Pro/Studio/Enterprise plans and the trial |
| `app` | — | project id from the Peerova website (needed for connect codes) |
| `share` | — | `{ name = "...", public = true, max = 16, mode = "...", region = "VN" }` — list the room and get a 6-char code; `public` = shown in the public room list |

Client — `peerova.client(ticket_or_code, { ... })`:

| Key | Default | Meaning |
|---|---|---|
| `token` | — | must match the server |
| `timeout_ms` | `20000` | max time before `"connect_failed"` |
| `key_file` | — (new random key per run) | the player's identity file; created if missing (**the folder must exist**). Store it in the user's data folder, e.g. `"<appdata>/<game>/peerova-client.key"` (LÖVE: `love.filesystem.getSaveDirectory()`). Keeping it lets the server see the player at the same stable address; banning by address works per player only while they keep that file |

A JSON string may be passed instead of a table.

### Game servers

Dedicated servers usually run the `peerova server` command-line tool (several `--expose`, separate processes for Remote Desktop and
the game, a `127.a.b.c` address per player + `--banned`, `--hide-ip`, VPN mode): see [guide §7](../DEVELOPER-GUIDE.md#7-game-server-features).

### Security

* `server.key` is the server's identity: anyone who has it can impersonate the server. Keep it secret, don't commit
  it, and don't ship it inside the game build you give to players.
* The ticket is not a secret: embed it in the game or share it with clients. `token` and `license` are secrets — never embed the license in a shipped game.
* All traffic is end-to-end encrypted.

### Troubleshooting

* Run `peerova doctor` (the SDK's command-line tool) for a network diagnosis.
* `ffi.load` fails / library not found: check the library location or set `PEEROVA_LIB`.
* `"connect_failed"` with a timeout: server not running, wrong ticket/code, or a firewall; raise `timeout_ms`, run
  `peerova doctor` on both ends.
* A 4th client is refused: the server runs in dev mode (max 3 clients) — add a `license`.
* Unreliable sends over 1180 bytes are rejected — split them or send reliably.
* `E_IO`: the `key_file` folder doesn't exist or isn't writable.
* The server won't start with `hide_ip`: your plan doesn't include hiding the IP (needs Pro/Studio/Enterprise or the trial).
* More: [guide §15](../DEVELOPER-GUIDE.md#15-troubleshooting).

### FAQ

* **Do players need to type a code?** No. Embed the ticket or fetch it from your URL; 6-character codes are only for inviting friends to player-hosted rooms.
* **Can a LÖVE game run `peerova client` hidden?** Not hidden on Windows from Lua; use `peerova.lua` (LÖVE has LuaJIT) — simpler and more robust.
* **Do I need port forwarding / a static IP?** No; an open port only makes direct connections faster.
* More: [guide §16](../DEVELOPER-GUIDE.md#16-faq-1).

### Tests

```sh
cd bindings/lua
lua test/test_peerova_stdio.lua      # plain Lua + sidecar
test/run_luajit.sh                   # LuaJIT FFI (uses luajit, or builds a small host embedding libluajit)
```
