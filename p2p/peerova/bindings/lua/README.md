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

Kết nối hoàn toàn tự động: thử kết nối trực tiếp trước, nếu mạng chặn thì tự dùng một đường dự phòng đã mã hoá.

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
print("ticket: " .. srv.ticket)                      -- pvt_... — gửi cho client
print("code: " .. tostring(srv:room_code()))         -- mã 6 ký tự (chỉ peerova.lua, chỉ khi có app + share); có thể nil lúc đầu
```

Client:

```lua
local cli = peerova.client(ticket_or_code)            -- ticket "pvt_..." hoặc mã 6 ký tự
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
* `peerova_stdio.lua`: lỗi khởi động → `error("peerova_stdio: sidecar exited before ready ...")`, lý do chi tiết in ra
  stderr; lỗi khi gửi (ví dụ peer không tồn tại) được thêm vào bảng `node.errors`.
* Lỗi mạng của client đến dưới dạng sự kiện `"connect_failed"`, không phải error.

### Cấu hình

Server — `peerova.server { ... }`:

| Khoá | Mặc định | Ý nghĩa |
|---|---|---|
| `port` | `47000` | cổng UDP; `0` = ngẫu nhiên |
| `key_file` | `"server.key"` | file danh tính server; cùng key ⇒ cùng ticket |
| `license` | — | khoá `PVA-...`; không có ⇒ chế độ dev, tối đa 3 client |
| `max_ccu` | theo license | giới hạn số client đồng thời |
| `token` | — | mật khẩu chung client phải gửi |
| `upnp` | `true` | tự mở cổng trên router |
| `private` | `false` | ticket không chứa địa chỉ IP của server |
| `app` | — | mã dự án trên website Peerova (cần cho mã kết nối) |
| `share` | — | `{ name = "...", public = true, max = 16, mode = "...", region = "VN" }` — đăng phòng và nhận mã 6 ký tự |

Client — `peerova.client(ticket_or_code, { ... })`: `token` (phải khớp server), `timeout_ms` (mặc định `20000`).
Có thể truyền chuỗi JSON thay cho bảng.

### Bảo mật

* `server.key` là danh tính của server: ai có file này có thể giả mạo server. Giữ bí mật, không commit vào git, không
  đóng gói vào bản game phát cho người chơi.
* Ticket gần như công khai: cứ gửi cho client. `token` và `license` là bí mật.
* Mọi dữ liệu được mã hoá đầu-cuối.

### Khắc phục sự cố

* Chạy `peerova doctor` (công cụ dòng lệnh trong SDK) để chẩn đoán mạng.
* `ffi.load` lỗi / không tìm thấy thư viện: kiểm tra vị trí thư viện hoặc đặt `PEEROVA_LIB`.
* `"connect_failed"` do hết giờ: server chưa chạy, sai ticket/mã, hoặc tường lửa; tăng `timeout_ms`, chạy `peerova doctor` ở cả hai đầu.
* Client thứ 4 bị từ chối: server đang ở chế độ dev (tối đa 3 client) — thêm `license`.
* Gửi unreliable > 1180 byte bị từ chối — chia nhỏ hoặc dùng reliable.

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

Connections are automatic: direct first, with an automatic encrypted fallback route when networks block direct
connections.

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
print("ticket: " .. srv.ticket)                      -- pvt_... — give it to clients
print("code: " .. tostring(srv:room_code()))         -- 6-char code (peerova.lua only, only with app + share); may be nil at first
```

Client:

```lua
local cli = peerova.client(ticket_or_code)            -- "pvt_..." ticket or 6-char code
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
* `peerova_stdio.lua`: a startup failure raises `error("peerova_stdio: sidecar exited before ready ...")` with the
  exact reason printed on stderr; send errors (e.g. unknown peer) are appended to the `node.errors` table.
* Client network failures arrive as a `"connect_failed"` event, not as errors.

### Configuration

Server — `peerova.server { ... }`:

| Key | Default | Meaning |
|---|---|---|
| `port` | `47000` | UDP port; `0` = random |
| `key_file` | `"server.key"` | server identity file; same key ⇒ same ticket |
| `license` | — | `PVA-...` key; without it: dev mode, max 3 clients |
| `max_ccu` | from license | cap on concurrent clients |
| `token` | — | shared secret clients must present |
| `upnp` | `true` | open the port on the router automatically |
| `private` | `false` | the ticket does not contain the server's IP addresses |
| `app` | — | project id from the Peerova website (needed for connect codes) |
| `share` | — | `{ name = "...", public = true, max = 16, mode = "...", region = "VN" }` — list the room and get a 6-char code |

Client — `peerova.client(ticket_or_code, { ... })`: `token` (must match the server), `timeout_ms` (default `20000`).
A JSON string may be passed instead of a table.

### Security

* `server.key` is the server's identity: anyone who has it can impersonate the server. Keep it secret, don't commit
  it, and don't ship it inside the game build you give to players.
* The ticket is public-ish: share it with clients. `token` and `license` are secrets.
* All traffic is end-to-end encrypted.

### Troubleshooting

* Run `peerova doctor` (the SDK's command-line tool) for a network diagnosis.
* `ffi.load` fails / library not found: check the library location or set `PEEROVA_LIB`.
* `"connect_failed"` with a timeout: server not running, wrong ticket/code, or a firewall; raise `timeout_ms`, run
  `peerova doctor` on both ends.
* A 4th client is refused: the server runs in dev mode (max 3 clients) — add a `license`.
* Unreliable sends over 1180 bytes are rejected — split them or send reliably.

### Tests

```sh
cd bindings/lua
lua test/test_peerova_stdio.lua      # plain Lua + sidecar
test/run_luajit.sh                   # LuaJIT FFI (uses luajit, or builds a small host embedding libluajit)
```
