# Peerova cho Python

[Tiếng Việt](#tiếng-việt) · [English](#english)

---

## Tiếng Việt

Binding Python cho Peerova: kết nối peer-to-peer trực tiếp, mã hoá đầu-cuối. Chỉ một file `peerova.py`,
dùng `ctypes` của thư viện chuẩn, không cần cài gói nào.

### Yêu cầu

- Python 3.8 trở lên (CPython hoặc PyPy có `ctypes`).
- Thư viện native Peerova cho hệ điều hành của bạn.

### Đặt thư viện native ở đâu

Trong gói SDK: `include/peerova.h`, `lib/linux-x86_64/libpeerova.so` (+ `libpeerova.a` và binary `peerova`),
`lib/windows-x86_64/peerova.dll` (+ `peerova.exe`), `bindings/python/`.

`peerova.py` tìm thư viện theo thứ tự: biến môi trường `PEEROVA_LIB` (đường dẫn đầy đủ tới file) → cùng thư mục
với `peerova.py` → thư mục `lib/<nền tảng>` của SDK → đường tìm kiếm mặc định của hệ thống.

| Hệ điều hành | File | Đặt ở đâu |
|---|---|---|
| Windows | `peerova.dll` | cạnh `peerova.py` (hoặc cạnh file `.exe` nếu bạn đóng gói bằng PyInstaller), hoặc `PEEROVA_LIB`. SDK **không** cần `wintun.dll` — file đó chỉ dùng cho chế độ VPN của công cụ dòng lệnh `peerova`. |
| Linux | `libpeerova.so` | cạnh `peerova.py`, hoặc thư mục trong `LD_LIBRARY_PATH`, hoặc `PEEROVA_LIB=/đường/dẫn/libpeerova.so` |
| macOS | `libpeerova.dylib` | liên hệ Peerova để nhận bản build này. Khi có file: đặt cạnh `peerova.py` hoặc dùng `PEEROVA_LIB`; trong app bundle đặt vào `Contents/Frameworks`. |

### Cài đặt

1. Chép `bindings/python/peerova.py` vào project (hoặc thêm thư mục đó vào `sys.path`).
2. Chép thư viện native cạnh nó, hoặc đặt `PEEROVA_LIB`.
3. Kiểm tra: `python3 -c "import peerova; print(peerova.version())"`.

### Bắt đầu nhanh (5 phút)

Server:

```python
import peerova

srv = peerova.Server(port=47000, key_file="server.key")   # chặn tới khi có ticket (≤ ~3 s)
print("ticket:", srv.ticket)                               # gửi chuỗi này cho client

while True:
    ev = srv.poll(0.1)
    if ev and ev.type == peerova.MESSAGE:
        srv.send(ev.peer, b"echo: " + ev.data)             # trả lời
```

Mã kết nối 6 ký tự (thay cho ticket dài): khởi động server với `app` (mã project lấy trên website Peerova) và
`share`, rồi đọc mã kết nối qua `srv.room_code`. Mã do website cấp ngay sau khi khởi động, nên giá trị
có thể là `None` lúc đầu — đọc lại sau ít giây.

```python
srv = peerova.Server(app="prj_xxx", share={"name": "Phòng của Lan", "public": True, "max": 8})
code = srv.room_code      # ví dụ "K7Q2XD", hoặc None nếu chưa có
```

Client (truyền ticket **hoặc** mã 6 ký tự):

```python
import peerova

cli = peerova.Client("pvt_...")            # hoặc peerova.Client("K7Q2XD")
ev = cli.poll(20)                          # chờ CONNECTED hoặc CONNECT_FAILED
if ev and ev.type == peerova.CONNECTED:
    cli.send(ev.peer, b"hello")
elif ev and ev.type == peerova.CONNECT_FAILED:
    print("lỗi:", ev.text)
```

Kết nối là tự động: thử kết nối trực tiếp trước, và tự chuyển sang một đường dự phòng đã mã hoá khi mạng chặn
kết nối trực tiếp. Bạn không cần cấu hình gì thêm.

### Gửi: reliable hay unreliable

```python
node.send(peer, data, channel=0, reliable=True)
```

| | `reliable=True` (mặc định) | `reliable=False` |
|---|---|---|
| Đảm bảo | đến đủ, đúng thứ tự trong cùng channel | có thể mất hoặc đến sai thứ tự |
| Kích thước | lớn tuỳ ý | tối đa **1180 byte** (`peerova.MAX_UNRELIABLE_PAYLOAD`) |
| Dùng cho | chat, túi đồ, RPC, lệnh, từng khúc file | input gameplay, vị trí, trạng thái realtime kiểu giọng nói |

- `channel` từ 0 đến 255. Các channel độc lập: một tin reliable bị chậm trên channel 0 không làm kẹt channel 1.
  Ví dụ: chat ở channel 0, vị trí ở channel 1, tải file ở channel 2.
- `data` là `bytes`/`bytearray`/`memoryview`; `str` được gửi dưới dạng UTF-8.
- Với dữ liệu realtime, gửi trạng thái mới nhất thường xuyên (ví dụ 20 lần/giây) thay vì gửi lại tin bị mất.

### Nhận sự kiện

`poll(timeout)` trả về một `peerova.Event` hoặc `None`:

| `ev.type` | Ý nghĩa | Trường liên quan |
|---|---|---|
| `peerova.CONNECTED` | có peer kết nối (server: một client mới; client: đã vào server) | `peer` |
| `peerova.MESSAGE` | có tin nhắn | `peer`, `channel`, `reliable`, `data` (`bytes`), `text` |
| `peerova.DISCONNECTED` | peer rời đi | `peer`, `text` = lý do |
| `peerova.CONNECT_FAILED` | (client) kết nối thất bại | `text` = lý do |

`timeout=0` trả về ngay; `timeout>0` chặn luồng gọi tối đa chừng đó giây (GIL được nhả trong lúc chờ).
`node.events(timeout=1.0)` là generator trả về sự kiện cho tới khi `timeout` giây trôi qua mà không có gì.

**Quy tắc luồng:** mỗi node an toàn đa luồng — `send`, `disconnect`, `peer_count` gọi được từ bất kỳ luồng nào,
kể cả khi luồng khác đang chờ trong `poll`. Chỉ nên có **một** luồng `poll` cho mỗi node. **Không bao giờ** gọi
`close()` khi một luồng khác còn đang ở trong `poll` của node đó: dừng luồng poll trước, rồi mới `close()`.

Mẫu dùng luồng thường:

```python
import threading, peerova

srv = peerova.Server(port=47000)
stop = threading.Event()

def poll_loop():
    while not stop.is_set():
        ev = srv.poll(0.1)          # timeout ngắn để thấy cờ stop nhanh
        if ev and ev.type == peerova.MESSAGE:
            handle(ev)

t = threading.Thread(target=poll_loop, daemon=True)
t.start()
# ... luồng chính làm việc khác, gọi srv.send(...) thoải mái ...
stop.set(); t.join(); srv.close()   # dừng poll trước, rồi mới close
```

Mẫu asyncio (chạy `poll` có timeout trong thread pool để không chặn event loop):

```python
import asyncio, peerova

async def main():
    loop = asyncio.get_running_loop()
    cli = peerova.Client("pvt_...")
    running = True

    async def poll_loop():
        while running:
            ev = await loop.run_in_executor(None, cli.poll, 0.1)   # tối đa 1 lần poll đang chạy
            if ev:
                await on_event(ev)

    task = asyncio.create_task(poll_loop())
    try:
        await do_work(cli)          # cli.send(...) gọi thẳng từ coroutine được
    finally:
        running = False
        await task                  # chờ lần poll đang chạy trả về
        cli.close()

asyncio.run(main())
```

Vòng lặp game (một luồng): mỗi frame gọi `poll()` (timeout 0) lặp lại cho tới khi nhận `None`.

### Ngắt kết nối và dọn dẹp

- `node.disconnect(peer)`: đóng kết nối tới một peer (server đá client, hoặc client rời server).
- `node.close()`: ngắt tất cả và giải phóng node; gọi hai lần cũng an toàn. Hoặc dùng `with`:
  `with peerova.Server(...) as srv: ...`.
- Sau `close()`, mọi lời gọi khác ném `PeerovaError("node is closed")`.

### Xử lý lỗi

Mọi lỗi ném `peerova.PeerovaError`; thông báo lấy từ thư viện native.

```python
try:
    srv = peerova.Server(port=47000)
except peerova.PeerovaError as e:
    print("không khởi động được:", e)      # ví dụ: cổng đã bị dùng
```

- `Server(...)`: không mở được cổng, config sai, license không hợp lệ.
- `Client(...)`: ticket/mã sai định dạng. Lỗi mạng **không** ném ngoại lệ — chúng đến dưới dạng sự kiện
  `CONNECT_FAILED` (ví dụ hết `timeout_ms`).
- `send(...)`: peer đã rời, tin unreliable quá 1180 byte, channel ngoài 0..255.
- Không tìm thấy thư viện native khi `import peerova`: `OSError`.

### Tuỳ chọn cấu hình

Truyền dưới dạng keyword argument.

Server (`peerova.Server(**config)`):

| Khoá | Mặc định | Ý nghĩa |
|---|---|---|
| `port` | `47000` | cổng lắng nghe; `0` = cổng ngẫu nhiên |
| `key_file` | `"server.key"` | file danh tính server. Cùng key ⇒ cùng ticket. Tự tạo nếu chưa có. |
| `license` | — | key `PVA-...`. Không có: chế độ dev, tối đa 3 client |
| `max_ccu` | — | số client đồng thời tối đa |
| `token` | — | bí mật chung mà client phải gửi |
| `upnp` | `True` | thử mở cổng trên router |
| `private` | `False` | ticket không chứa địa chỉ IP của server |
| `app` | — | mã project trên website Peerova (để có mã kết nối) |
| `share` | — | `{"name", "public", "max", "mode", "region"}`: công bố mã kết nối 6 ký tự |

Client (`peerova.Client(ticket_or_code, **config)`):

| Khoá | Mặc định | Ý nghĩa |
|---|---|---|
| `token` | — | phải trùng `token` của server |
| `timeout_ms` | `20000` | quá thời gian này mà chưa kết nối được → `CONNECT_FAILED` |

### Bảo mật

- `server.key` là danh tính của server: ai có file này có thể giả mạo server. Giữ bí mật, **không** commit vào git.
- Ticket là thông tin bán công khai: cứ chia sẻ cho client.
- `token` và `license` là bí mật: không đưa vào mã nguồn công khai hay bản build phía client (trừ `token` nếu
  bạn chủ ý cho client biết).
- Toàn bộ lưu lượng được mã hoá đầu-cuối.

### Ví dụ

[`examples/chat.py`](examples/chat.py): chat + đồng bộ vị trí. Server dùng mẫu luồng thường, client dùng asyncio.
Vị trí gửi unreliable ~20 Hz, dòng chat gửi reliable, server chuyển tiếp cho các client khác.

```sh
python3 examples/chat.py server --key server.key          # in ra: TICKET pvt_...
python3 examples/chat.py client pvt_...                   # gõ một dòng rồi Enter để chat
python3 examples/chat.py client pvt_... --seconds 10      # tự thoát sau 10 giây
```

Test: `python3 test_peerova.py`.

### Khắc phục sự cố

- Chạy `peerova doctor` (công cụ dòng lệnh `peerova` trong SDK) để chẩn đoán mạng.
- `OSError: cannot find libpeerova.so`: đặt thư viện cạnh `peerova.py` hoặc đặt `PEEROVA_LIB` tới đường dẫn đầy đủ.
  Trên Windows, kiểm tra đúng kiến trúc (Python 64-bit cần `peerova.dll` 64-bit).
- `CONNECT_FAILED` sau khoảng 20 giây (timeout): kiểm tra ticket/mã, server còn chạy không, `token` có khớp không,
  rồi chạy `peerova doctor` ở cả hai phía.
- Client thứ 4 không vào được: server đang chạy chế độ dev (không có `license`) — tối đa 3 client.
- `send(..., reliable=False)` ném lỗi: payload quá 1180 byte. Chia nhỏ hoặc gửi reliable.

Khái niệm và toàn bộ C API: [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md). Website: https://peerova.pages.dev

---

## English

Python binding for Peerova: direct peer-to-peer connections, end-to-end encrypted. A single file, `peerova.py`,
built on the standard library's `ctypes` — nothing to install.

### Requirements

- Python 3.8 or newer (CPython, or PyPy with `ctypes`).
- The Peerova native library for your operating system.

### Where to put the native library

SDK package layout: `include/peerova.h`, `lib/linux-x86_64/libpeerova.so` (+ `libpeerova.a` and the `peerova`
binary), `lib/windows-x86_64/peerova.dll` (+ `peerova.exe`), `bindings/python/`.

`peerova.py` looks for the library in this order: the `PEEROVA_LIB` environment variable (full path to the file) →
the folder containing `peerova.py` → the SDK's `lib/<platform>` folder → the system's default search path.

| OS | File | Where |
|---|---|---|
| Windows | `peerova.dll` | next to `peerova.py` (or next to the `.exe` if you bundle with PyInstaller), or `PEEROVA_LIB`. The SDK does **not** need `wintun.dll` — that file is only used by the VPN mode of the `peerova` command-line tool. |
| Linux | `libpeerova.so` | next to `peerova.py`, or a folder on `LD_LIBRARY_PATH`, or `PEEROVA_LIB=/path/to/libpeerova.so` |
| macOS | `libpeerova.dylib` | contact Peerova for this build. Once you have it: next to `peerova.py` or via `PEEROVA_LIB`; inside an app bundle put it in `Contents/Frameworks`. |

### Install

1. Copy `bindings/python/peerova.py` into your project (or add that folder to `sys.path`).
2. Copy the native library next to it, or set `PEEROVA_LIB`.
3. Check: `python3 -c "import peerova; print(peerova.version())"`.

### 5-minute quick start

Server:

```python
import peerova

srv = peerova.Server(port=47000, key_file="server.key")   # blocks until the ticket is ready (<= ~3 s)
print("ticket:", srv.ticket)                               # give this string to clients

while True:
    ev = srv.poll(0.1)
    if ev and ev.type == peerova.MESSAGE:
        srv.send(ev.peer, b"echo: " + ev.data)             # reply
```

6-character connect code (instead of the long ticket): start the server with `app` (your project id from the
Peerova website) and `share`, then read the connect code from `srv.room_code`. The website assigns the code
shortly after start, so it can be `None` at first — read it again a few seconds later.

```python
srv = peerova.Server(app="prj_xxx", share={"name": "Lan's room", "public": True, "max": 8})
code = srv.room_code      # e.g. "K7Q2XD", or None if not assigned yet
```

Client (pass the ticket **or** the 6-character code):

```python
import peerova

cli = peerova.Client("pvt_...")            # or peerova.Client("K7Q2XD")
ev = cli.poll(20)                          # wait for CONNECTED or CONNECT_FAILED
if ev and ev.type == peerova.CONNECTED:
    cli.send(ev.peer, b"hello")
elif ev and ev.type == peerova.CONNECT_FAILED:
    print("failed:", ev.text)
```

Connections are automatic: direct first, with an automatic encrypted fallback route when networks block direct
connections. There is nothing extra to configure.

### Sending: reliable vs unreliable

```python
node.send(peer, data, channel=0, reliable=True)
```

| | `reliable=True` (default) | `reliable=False` |
|---|---|---|
| Guarantee | delivered, in order within the channel | may be lost or reordered |
| Size | any size | at most **1180 bytes** (`peerova.MAX_UNRELIABLE_PAYLOAD`) |
| Use for | chat, inventory, RPC, commands, file chunks | gameplay inputs, positions, voice-like realtime state |

- `channel` is 0..255. Channels are independent: a delayed reliable message on channel 0 does not block channel 1.
  For example: chat on channel 0, positions on channel 1, file transfer on channel 2.
- `data` is `bytes`/`bytearray`/`memoryview`; a `str` is sent as UTF-8.
- For realtime data, send the latest state often (e.g. 20 times per second) instead of resending lost messages.

### Receiving events

`poll(timeout)` returns a `peerova.Event` or `None`:

| `ev.type` | Meaning | Relevant fields |
|---|---|---|
| `peerova.CONNECTED` | a peer connected (server: a new client; client: joined the server) | `peer` |
| `peerova.MESSAGE` | a message arrived | `peer`, `channel`, `reliable`, `data` (`bytes`), `text` |
| `peerova.DISCONNECTED` | a peer left | `peer`, `text` = reason |
| `peerova.CONNECT_FAILED` | (client) the connection attempt failed | `text` = reason |

`timeout=0` returns immediately; `timeout>0` blocks the calling thread for up to that many seconds (the GIL is
released while waiting). `node.events(timeout=1.0)` is a generator that yields events until `timeout` seconds pass
with none.

**Threading rules:** each node is thread-safe — `send`, `disconnect` and `peer_count` may be called from any thread,
even while another thread waits in `poll`. Use **one** polling thread per node. **Never** call `close()` while
another thread is still inside `poll` on that node: stop the polling thread first, then `close()`.

Plain thread pattern:

```python
import threading, peerova

srv = peerova.Server(port=47000)
stop = threading.Event()

def poll_loop():
    while not stop.is_set():
        ev = srv.poll(0.1)          # short timeout so the stop flag is noticed quickly
        if ev and ev.type == peerova.MESSAGE:
            handle(ev)

t = threading.Thread(target=poll_loop, daemon=True)
t.start()
# ... the main thread does other work and may call srv.send(...) freely ...
stop.set(); t.join(); srv.close()   # stop polling first, then close
```

asyncio pattern (run the timed `poll` in the thread pool so the event loop is never blocked):

```python
import asyncio, peerova

async def main():
    loop = asyncio.get_running_loop()
    cli = peerova.Client("pvt_...")
    running = True

    async def poll_loop():
        while running:
            ev = await loop.run_in_executor(None, cli.poll, 0.1)   # at most one poll in flight
            if ev:
                await on_event(ev)

    task = asyncio.create_task(poll_loop())
    try:
        await do_work(cli)          # cli.send(...) can be called directly from coroutines
    finally:
        running = False
        await task                  # wait for the in-flight poll to return
        cli.close()

asyncio.run(main())
```

Game loop (single thread): every frame call `poll()` (timeout 0) repeatedly until it returns `None`.

### Disconnect and cleanup

- `node.disconnect(peer)`: close the connection to one peer (server kicks a client, or client leaves the server).
- `node.close()`: disconnect everyone and free the node; calling it twice is safe. Or use `with`:
  `with peerova.Server(...) as srv: ...`.
- After `close()`, every other call raises `PeerovaError("node is closed")`.

### Error handling

All errors raise `peerova.PeerovaError`; the message comes from the native library.

```python
try:
    srv = peerova.Server(port=47000)
except peerova.PeerovaError as e:
    print("cannot start:", e)       # e.g. port already in use
```

- `Server(...)`: port cannot be opened, invalid config, invalid license.
- `Client(...)`: malformed ticket/code. Network failures do **not** raise — they arrive as a `CONNECT_FAILED` event
  (e.g. `timeout_ms` elapsed).
- `send(...)`: peer is gone, unreliable payload over 1180 bytes, channel outside 0..255.
- Native library not found at `import peerova`: `OSError`.

### Config options

Pass them as keyword arguments.

Server (`peerova.Server(**config)`):

| Key | Default | Meaning |
|---|---|---|
| `port` | `47000` | listening port; `0` = random port |
| `key_file` | `"server.key"` | server identity file. Same key ⇒ same ticket. Created if missing. |
| `license` | — | `PVA-...` key. Without it: dev mode, at most 3 clients |
| `max_ccu` | — | maximum concurrent clients |
| `token` | — | shared secret clients must present |
| `upnp` | `True` | try to open the port on the router |
| `private` | `False` | the ticket does not contain the server's IP addresses |
| `app` | — | project id from the Peerova website (needed for connect codes) |
| `share` | — | `{"name", "public", "max", "mode", "region"}`: publish a 6-character connect code |

Client (`peerova.Client(ticket_or_code, **config)`):

| Key | Default | Meaning |
|---|---|---|
| `token` | — | must match the server's `token` |
| `timeout_ms` | `20000` | not connected within this time → `CONNECT_FAILED` |

### Security

- `server.key` is the server's identity: anyone who has it can impersonate your server. Keep it secret and do
  **not** commit it.
- The ticket is public-ish: share it with clients.
- `token` and `license` are secrets: keep them out of public source code and client builds (except `token` when you
  intentionally give it to clients).
- All traffic is end-to-end encrypted.

### Example

[`examples/chat.py`](examples/chat.py): chat + position sync. The server uses the plain-thread pattern, the client
uses asyncio. Positions are sent unreliable at ~20 Hz, chat lines reliable; the server forwards both to the other
clients.

```sh
python3 examples/chat.py server --key server.key          # prints: TICKET pvt_...
python3 examples/chat.py client pvt_...                   # type a line and press Enter to chat
python3 examples/chat.py client pvt_... --seconds 10      # quit automatically after 10 s
```

Test: `python3 test_peerova.py`.

### Troubleshooting

- Run `peerova doctor` (the `peerova` command-line tool in the SDK) to diagnose the network.
- `OSError: cannot find libpeerova.so`: put the library next to `peerova.py` or set `PEEROVA_LIB` to its full path.
  On Windows, check the architecture (64-bit Python needs a 64-bit `peerova.dll`).
- `CONNECT_FAILED` after about 20 seconds (timeout): check the ticket/code, that the server is still running, and
  that `token` matches; then run `peerova doctor` on both sides.
- The 4th client cannot join: the server runs in dev mode (no `license`) — at most 3 clients.
- `send(..., reliable=False)` raises: the payload is over 1180 bytes. Split it or send it reliably.

Concepts and the full C API: [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md). Website: https://peerova.pages.dev
