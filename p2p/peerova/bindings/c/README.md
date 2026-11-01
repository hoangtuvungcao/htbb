# Peerova — C / C++

- [Tiếng Việt](#tiếng-việt)
- [English](#english)

Website: https://peerova.pages.dev · Khái niệm và toàn bộ C API / Concepts and the full C API: [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md)

---

## Tiếng Việt

Đây là API gốc của Peerova: một header `peerova.h` và một thư viện native. Mọi binding khác đều bọc API này.
Dùng được từ C99/C11 và C++ (header có sẵn `extern "C"`).

### Yêu cầu

- Trình biên dịch C/C++ bất kỳ (gcc, clang, MSVC, MinGW).
- Gói SDK Peerova: `include/peerova.h` và thư viện cho nền tảng của bạn.

### Đặt thư viện native ở đâu

| Hệ điều hành | File | Đặt ở đâu |
|---|---|---|
| Windows x64 | `lib/windows-x86_64/peerova.dll` | Cạnh file `.exe` của bạn. SDK **không** cần `wintun.dll` (chỉ công cụ dòng lệnh `peerova` cần nó cho chế độ VPN). |
| Linux x64 | `lib/linux-x86_64/libpeerova.so` (hoặc `libpeerova.a` để link tĩnh) | Cạnh file chạy và link với rpath `$ORIGIN`, hoặc đặt trong một thư mục thuộc `LD_LIBRARY_PATH`. |
| macOS | `libpeerova.dylib` | Liên hệ Peerova để nhận bản build này. Trong app bundle: `Contents/Frameworks/`. |
| iOS | `libpeerova.a` (tĩnh) | Liên hệ Peerova để nhận bản build này. Link tĩnh vào target Xcode. |
| Android | `libpeerova.so` | Liên hệ Peerova để nhận bản build này. Đặt vào `jniLibs/<abi>/libpeerova.so`. |

### Cài đặt và biên dịch

Linux (thư viện động, tìm `.so` cạnh file chạy):
```sh
cc app.c -Iinclude -Llib/linux-x86_64 -lpeerova -Wl,-rpath,'$ORIGIN' -o app
cp lib/linux-x86_64/libpeerova.so .
```
Linux (link tĩnh, không cần file `.so` khi chạy):
```sh
cc app.c -Iinclude lib/linux-x86_64/libpeerova.a -lpthread -ldl -lm -o app
```
Windows (MinGW): `gcc app.c -Iinclude -Llib/windows-x86_64 -lpeerova -o app.exe`, rồi chép `peerova.dll` cạnh `app.exe`.
Với MSVC, link với import library đi kèm gói SDK (nếu gói của bạn không có, hãy liên hệ Peerova).

### Bắt đầu nhanh (5 phút)

Server:
```c
#include "peerova.h"
peerova_node* srv = peerova_server_start("{\"port\":47000,\"key_file\":\"server.key\"}");
if (!srv) { printf("lỗi: %s\n", peerova_last_error()); return 1; }

char ticket[2048];
peerova_ticket(srv, ticket, sizeof ticket);   /* đưa ticket này cho client */
printf("TICKET %s\n", ticket);

char code[16];                                 /* mã kết nối 6 ký tự, chỉ có khi cấu hình "app" + "share" */
if (peerova_room_code(srv, code, sizeof code) > 0) printf("CODE %s\n", code);
```
Mã kết nối (connect code) được website cấp ngay sau khi khởi động, nên `peerova_room_code` có thể trả 0 lúc đầu — gọi lại khoảng mỗi giây cho tới khi > 0.
Hai hàm `peerova_ticket` / `peerova_room_code` trả về kích thước cần thiết (tính cả ký tự NUL); gọi với `NULL, 0` để biết kích thước trước.

Client (dùng ticket **hoặc** mã 6 ký tự):
```c
peerova_node* cli = peerova_client_connect(ticket_or_code, "{\"timeout_ms\":20000}");
if (!cli) { printf("lỗi: %s\n", peerova_last_error()); return 1; }
peerova_event ev;
uint32_t server = 0;
while (!server) {
    if (!peerova_poll_wait(cli, &ev, 1000)) continue;
    if (ev.type == PEEROVA_EVENT_CONNECTED) server = ev.peer;
    else if (ev.type == PEEROVA_EVENT_CONNECT_FAILED) {
        printf("thất bại: %.*s\n", (int)ev.len, (const char*)ev.data);
        peerova_free(cli); return 1;
    }
}
peerova_send(cli, server, 0, (const uint8_t*)"xin chào", 9, 1);
```
Kết nối là tự động: ưu tiên kết nối trực tiếp, và tự dùng một tuyến dự phòng được mã hoá khi mạng chặn kết nối trực tiếp.

### Gửi: reliable hay unreliable

`peerova_send(node, peer, channel, data, len, reliable)` trả 0 nếu thành công, khác 0 nếu lỗi (xem `peerova_last_error()`).

| | `reliable = 0` (unreliable) | `reliable = 1` (reliable) |
|---|---|---|
| Đảm bảo | Có thể mất hoặc đến sai thứ tự | Đến đủ, đúng thứ tự trong cùng một channel |
| Kích thước | Tối đa **1180 byte** mỗi message (lớn hơn sẽ bị từ chối) | Có thể lớn |
| Dùng cho | Input, vị trí, trạng thái realtime kiểu voice — dữ liệu gửi liên tục, gói mới thay gói cũ | Chat, inventory, RPC, các phần của file |

Channel là số 0..255. Các channel khác nhau không chặn nhau: một message reliable bị gửi lại trên channel 0 không làm chậm channel 1.

### Nhận sự kiện

Có hai hàm poll: `peerova_poll` (không chờ) và `peerova_poll_wait` (chờ tối đa `timeout_ms`). Cả hai trả 1 và điền `*out` nếu có sự kiện.

| `ev.type` | Ý nghĩa |
|---|---|
| `PEEROVA_EVENT_CONNECTED` | Peer đã kết nối (server: một client mới; client: server). `ev.peer` là id dùng để gửi. |
| `PEEROVA_EVENT_MESSAGE` | Có message: `ev.data`, `ev.len`, `ev.channel`, `ev.reliable`. |
| `PEEROVA_EVENT_DISCONNECTED` | Peer đã ngắt kết nối. |
| `PEEROVA_EVENT_CONNECT_FAILED` | Chỉ ở client: không kết nối được, `ev.data`/`ev.len` là lý do (UTF-8). |

`ev.data` chỉ hợp lệ tới lần gọi `peerova_poll*` / `peerova_free` kế tiếp trên cùng node — hãy copy nếu cần giữ lại.

Vòng lặp chuẩn: mỗi frame (hoặc mỗi vòng của một luồng mạng), lấy hết sự kiện rồi mới làm việc khác:
```c
for (;;) {
    while (peerova_poll(node, &ev)) handle(&ev);   /* game: gọi trong vòng lặp frame */
    update_and_send();
}
```
Server không có game loop có thể dùng `peerova_poll_wait(node, &ev, 10)` để khỏi tốn CPU.

### Luồng (thread)

Mỗi node có khoá nội bộ, nên `peerova_send`/`peerova_poll`/`peerova_disconnect` có thể gọi từ nhiều luồng.
`peerova_last_error()` là theo từng luồng. **Không bao giờ** gọi `peerova_free` khi một luồng khác vẫn đang dùng node đó.

### Ngắt kết nối và dọn dẹp

- `peerova_disconnect(node, peer)` đóng một kết nối; sẽ có sự kiện `DISCONNECTED`.
- `peerova_free(node)` đóng tất cả kết nối và giải phóng node. Gọi đúng một lần, sau khi các luồng khác đã ngừng dùng node.

### Xử lý lỗi

Hàm trả `NULL` (start/connect) hoặc khác 0 (send) khi lỗi; đọc lý do bằng `peerova_last_error()` (UTF-8, hợp lệ tới lần gọi kế tiếp trên luồng đó).
Lỗi kết nối xảy ra sau đó (hết thời gian, sai token…) đến dưới dạng sự kiện `PEEROVA_EVENT_CONNECT_FAILED`.

### Cấu hình

Server (`peerova_server_start`, JSON, có thể `NULL`):

| Khoá | Mặc định | Ý nghĩa |
|---|---|---|
| `port` | `47000` | Cổng UDP; `0` = cổng ngẫu nhiên. |
| `key_file` | `"server.key"` | Khoá định danh server (tạo mới nếu chưa có). Cùng khoá ⇒ cùng ticket. |
| `license` | — | License `PVA-...`. Không có license: chế độ dev, tối đa 3 client. |
| `max_ccu` | theo license | Số client đồng thời tối đa. |
| `token` | — | Mật khẩu chung client phải gửi kèm. |
| `upnp` | `true` | Thử mở cổng trên router qua UPnP. |
| `private` | `false` | Ticket không chứa địa chỉ IP của server. |
| `app` | — | Project id trên website Peerova (`prj_...`), cần để có mã kết nối. |
| `share` | — | `{"name":"...","public":true,"max":16,"mode":"...","region":"VN"}` — thông tin hiển thị cho mã kết nối. |

Client (`peerova_client_connect`, JSON, có thể `NULL`): `token` (nếu server yêu cầu), `timeout_ms` (mặc định `20000`).

### Tham chiếu API

| Hàm | Mô tả |
|---|---|
| `peerova_server_start(config)` | Khởi động server. Chặn tới khi có ticket (≤ ~3 s). `NULL` nếu lỗi. |
| `peerova_client_connect(ticket, config)` | Bắt đầu kết nối bằng ticket hoặc mã 6 ký tự. Trả về ngay; chờ `CONNECTED`/`CONNECT_FAILED`. |
| `peerova_ticket(node, buf, len)` | Copy ticket. Trả kích thước cần (tính NUL), 0 nếu không có. |
| `peerova_room_code(node, buf, len)` | Copy mã kết nối. Trả kích thước cần (tính NUL), 0 nếu chưa có. |
| `peerova_send(node, peer, channel, data, len, reliable)` | Gửi. 0 = thành công. |
| `peerova_poll(node, &ev)` / `peerova_poll_wait(node, &ev, ms)` | Lấy một sự kiện. 1 = có sự kiện. |
| `peerova_disconnect(node, peer)` | Đóng một kết nối. |
| `peerova_peer_count(node)` | Số peer đang kết nối. |
| `peerova_free(node)` | Giải phóng node. |
| `peerova_last_error()` / `peerova_version()` | Thông báo lỗi gần nhất của luồng / phiên bản thư viện. |

### Ví dụ

- [`examples/chat_positions.c`](examples/chat_positions.c) — chat (reliable) + đồng bộ vị trí 20 Hz (unreliable), chạy server và client ở hai tiến trình:
  ```sh
  cd examples
  cc chat_positions.c -I../../../include -L../../../lib/linux-x86_64 -lpeerova -Wl,-rpath,'$ORIGIN/../../../lib/linux-x86_64' -o chat_positions
  ./chat_positions server            # in ra "TICKET ..."
  ./chat_positions client <ticket>   # ở cửa sổ khác (hoặc máy khác)
  ```
- [`example.c`](example.c) — server và client trong cùng một tiến trình, gửi một message.
- [`test_peerova.c`](test_peerova.c) — kiểm thử round-trip (`./test_peerova`, `./test_peerova server [port]`, `./test_peerova client <ticket>`).

### Bảo mật

- `server.key` là danh tính của server: ai có file này có thể giả mạo server của bạn. Giữ bí mật, không commit vào git.
- Ticket là thông tin "bán công khai": cứ đưa cho người chơi/client.
- `token` và `license` là bí mật.
- Toàn bộ dữ liệu được mã hoá đầu-cuối.

### Khắc phục sự cố

- Chạy `peerova doctor` (công cụ dòng lệnh `peerova` trong gói SDK) để chẩn đoán mạng.
- **Không tìm thấy thư viện** (`error while loading shared libraries: libpeerova.so` / thiếu `peerova.dll`): đặt thư viện cạnh file chạy (Linux: link với rpath `$ORIGIN`) hoặc thêm thư mục vào `LD_LIBRARY_PATH` / `PATH`.
- **Kết nối thất bại do hết thời gian**: server chưa chạy, ticket/mã sai hoặc đã hết hạn, hay mạng chặn; thử `peerova doctor` ở cả hai phía, tăng `timeout_ms`.
- **Client thứ 4 bị từ chối**: server đang ở chế độ dev (không có license) chỉ nhận tối đa 3 client.
- **`peerova_send` unreliable trả lỗi**: message lớn hơn 1180 byte — chia nhỏ hoặc gửi reliable.

---

## English

This is Peerova's native API: one header, `peerova.h`, and one native library. Every other binding wraps it.
It works from C99/C11 and C++ (the header has `extern "C"` guards).

### Requirements

- Any C/C++ compiler (gcc, clang, MSVC, MinGW).
- The Peerova SDK package: `include/peerova.h` and the library for your platform.

### Where to put the native library

| OS | File | Where it goes |
|---|---|---|
| Windows x64 | `lib/windows-x86_64/peerova.dll` | Next to your `.exe`. The SDK does **not** need `wintun.dll` (only the `peerova` command-line tool needs it, for its VPN mode). |
| Linux x64 | `lib/linux-x86_64/libpeerova.so` (or `libpeerova.a` for static linking) | Next to your binary, linked with rpath `$ORIGIN`, or in a directory on `LD_LIBRARY_PATH`. |
| macOS | `libpeerova.dylib` | Contact Peerova for this build. In an app bundle: `Contents/Frameworks/`. |
| iOS | `libpeerova.a` (static) | Contact Peerova for this build. Link it statically into your Xcode target. |
| Android | `libpeerova.so` | Contact Peerova for this build. Put it in `jniLibs/<abi>/libpeerova.so`. |

### Install and build

Linux (shared library, `.so` found next to the binary):
```sh
cc app.c -Iinclude -Llib/linux-x86_64 -lpeerova -Wl,-rpath,'$ORIGIN' -o app
cp lib/linux-x86_64/libpeerova.so .
```
Linux (static, no `.so` needed at run time):
```sh
cc app.c -Iinclude lib/linux-x86_64/libpeerova.a -lpthread -ldl -lm -o app
```
Windows (MinGW): `gcc app.c -Iinclude -Llib/windows-x86_64 -lpeerova -o app.exe`, then copy `peerova.dll` next to `app.exe`.
With MSVC, link the import library shipped with the SDK package (if your package does not contain one, contact Peerova).

### 5-minute quick start

Server:
```c
#include "peerova.h"
peerova_node* srv = peerova_server_start("{\"port\":47000,\"key_file\":\"server.key\"}");
if (!srv) { printf("error: %s\n", peerova_last_error()); return 1; }

char ticket[2048];
peerova_ticket(srv, ticket, sizeof ticket);   /* give this ticket to clients */
printf("TICKET %s\n", ticket);

char code[16];                                 /* 6-character connect code, only with "app" + "share" config */
if (peerova_room_code(srv, code, sizeof code) > 0) printf("CODE %s\n", code);
```
The connect code is assigned by the website shortly after start, so `peerova_room_code` may return 0 at first — call it again about once a second until it returns > 0.
Both `peerova_ticket` and `peerova_room_code` return the required size including the NUL; call them with `NULL, 0` to get the size first.

Client (with a ticket **or** a 6-character code):
```c
peerova_node* cli = peerova_client_connect(ticket_or_code, "{\"timeout_ms\":20000}");
if (!cli) { printf("error: %s\n", peerova_last_error()); return 1; }
peerova_event ev;
uint32_t server = 0;
while (!server) {
    if (!peerova_poll_wait(cli, &ev, 1000)) continue;
    if (ev.type == PEEROVA_EVENT_CONNECTED) server = ev.peer;
    else if (ev.type == PEEROVA_EVENT_CONNECT_FAILED) {
        printf("failed: %.*s\n", (int)ev.len, (const char*)ev.data);
        peerova_free(cli); return 1;
    }
}
peerova_send(cli, server, 0, (const uint8_t*)"hello", 5, 1);
```
Connections are automatic: direct first, with an automatic encrypted fallback route when networks block direct connections.

### Sending: reliable vs unreliable

`peerova_send(node, peer, channel, data, len, reliable)` returns 0 on success, non-zero on error (see `peerova_last_error()`).

| | `reliable = 0` (unreliable) | `reliable = 1` (reliable) |
|---|---|---|
| Guarantee | May be lost or reordered | Delivered, in order within the channel |
| Size | At most **1180 bytes** per message (larger sends are rejected) | Can be large |
| Use for | Gameplay inputs, positions, voice-like realtime state — data sent continuously where a newer packet replaces an older one | Chat, inventory, RPC, file chunks |

Channels are numbers 0..255. Different channels don't block each other: a reliable message being retransmitted on channel 0 does not delay channel 1.

### Receiving events

There are two poll functions: `peerova_poll` (never waits) and `peerova_poll_wait` (waits up to `timeout_ms`). Both return 1 and fill `*out` when an event is available.

| `ev.type` | Meaning |
|---|---|
| `PEEROVA_EVENT_CONNECTED` | A peer connected (server: a new client; client: the server). `ev.peer` is the id to send to. |
| `PEEROVA_EVENT_MESSAGE` | A message: `ev.data`, `ev.len`, `ev.channel`, `ev.reliable`. |
| `PEEROVA_EVENT_DISCONNECTED` | A peer disconnected. |
| `PEEROVA_EVENT_CONNECT_FAILED` | Client only: could not connect; `ev.data`/`ev.len` is the reason (UTF-8). |

`ev.data` is only valid until the next `peerova_poll*` / `peerova_free` call on the same node — copy it if you need to keep it.

Standard loop: every frame (or every iteration of a network thread), drain all events, then do your work:
```c
for (;;) {
    while (peerova_poll(node, &ev)) handle(&ev);   /* games: call from the frame loop */
    update_and_send();
}
```
A server without a game loop can use `peerova_poll_wait(node, &ev, 10)` to avoid spinning.

### Threading

Each node has an internal lock, so `peerova_send` / `peerova_poll` / `peerova_disconnect` may be called from several threads.
`peerova_last_error()` is per thread. **Never** call `peerova_free` while another thread is still using that node.

### Disconnect and cleanup

- `peerova_disconnect(node, peer)` closes one connection; a `DISCONNECTED` event follows.
- `peerova_free(node)` closes all connections and frees the node. Call it exactly once, after other threads have stopped using the node.

### Error handling

Functions return `NULL` (start/connect) or non-zero (send) on error; read the reason with `peerova_last_error()` (UTF-8, valid until the next call on that thread).
Connection errors that happen later (timeout, wrong token, …) arrive as `PEEROVA_EVENT_CONNECT_FAILED` events.

### Configuration

Server (`peerova_server_start`, JSON, may be `NULL`):

| Key | Default | Meaning |
|---|---|---|
| `port` | `47000` | UDP port; `0` = random port. |
| `key_file` | `"server.key"` | Server identity key (created if missing). Same key ⇒ same ticket. |
| `license` | — | `PVA-...` license key. Without a license: dev mode, max 3 clients. |
| `max_ccu` | from license | Maximum concurrent clients. |
| `token` | — | Shared secret clients must present. |
| `upnp` | `true` | Try to open the port on the router via UPnP. |
| `private` | `false` | The ticket does not contain the server's IP addresses. |
| `app` | — | Project id from the Peerova website (`prj_...`), needed for a connect code. |
| `share` | — | `{"name":"...","public":true,"max":16,"mode":"...","region":"VN"}` — listing details for the connect code. |

Client (`peerova_client_connect`, JSON, may be `NULL`): `token` (if the server requires one), `timeout_ms` (default `20000`).

### API reference

| Function | Description |
|---|---|
| `peerova_server_start(config)` | Starts a server. Blocks until the ticket is ready (≤ ~3 s). `NULL` on error. |
| `peerova_client_connect(ticket, config)` | Starts connecting with a ticket or a 6-character code. Returns immediately; wait for `CONNECTED`/`CONNECT_FAILED`. |
| `peerova_ticket(node, buf, len)` | Copies the ticket. Returns the required size incl. NUL, 0 if none. |
| `peerova_room_code(node, buf, len)` | Copies the connect code. Returns the required size incl. NUL, 0 if not available yet. |
| `peerova_send(node, peer, channel, data, len, reliable)` | Sends. 0 = success. |
| `peerova_poll(node, &ev)` / `peerova_poll_wait(node, &ev, ms)` | Gets one event. 1 = an event was returned. |
| `peerova_disconnect(node, peer)` | Closes one connection. |
| `peerova_peer_count(node)` | Number of connected peers. |
| `peerova_free(node)` | Frees the node. |
| `peerova_last_error()` / `peerova_version()` | Last error message of this thread / library version. |

### Examples

- [`examples/chat_positions.c`](examples/chat_positions.c) — chat (reliable) + position sync at 20 Hz (unreliable), server and client in two processes:
  ```sh
  cd examples
  cc chat_positions.c -I../../../include -L../../../lib/linux-x86_64 -lpeerova -Wl,-rpath,'$ORIGIN/../../../lib/linux-x86_64' -o chat_positions
  ./chat_positions server            # prints "TICKET ..."
  ./chat_positions client <ticket>   # in another terminal (or on another machine)
  ```
- [`example.c`](example.c) — server and client in one process, sends one message.
- [`test_peerova.c`](test_peerova.c) — round-trip test (`./test_peerova`, `./test_peerova server [port]`, `./test_peerova client <ticket>`).

### Security

- `server.key` is the server's identity: anyone who has it can impersonate your server. Keep it secret and never commit it.
- The ticket is public-ish: share it with players/clients.
- `token` and `license` are secrets.
- All traffic is end-to-end encrypted.

### Troubleshooting

- Run `peerova doctor` (the `peerova` command-line tool in the SDK package) for a network diagnosis.
- **Library not found** (`error while loading shared libraries: libpeerova.so` / missing `peerova.dll`): put the library next to the binary (Linux: link with rpath `$ORIGIN`) or add its directory to `LD_LIBRARY_PATH` / `PATH`.
- **Connect failed (timeout)**: the server is not running, the ticket/code is wrong or expired, or the network blocks traffic; run `peerova doctor` on both sides and/or raise `timeout_ms`.
- **The 4th client is rejected**: the server runs in dev mode (no license), which accepts at most 3 clients.
- **Unreliable `peerova_send` fails**: the message is larger than 1180 bytes — split it or send it reliably.
