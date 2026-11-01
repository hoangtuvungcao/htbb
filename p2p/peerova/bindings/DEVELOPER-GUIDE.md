# Peerova SDK — Hướng dẫn cho lập trình viên / Developer Guide

- [Tiếng Việt](#tiếng-việt)
- [English](#english)

Website, license, tải về: https://peerova.pages.dev

---

# Tiếng Việt

## 1. Khái niệm

Peerova kết nối trực tiếp hai (hoặc nhiều) máy, mã hoá đầu-cuối, không cần mở port hay có IP public.
Kết nối là tự động: thử đường trực tiếp trước; nếu mạng chặn kết nối trực tiếp, SDK tự dùng một đường dự phòng
được mã hoá, rồi chuyển sang trực tiếp ngay khi có thể. Ứng dụng không cần làm gì thêm.

| Khái niệm | Ý nghĩa |
|---|---|
| **Server node** | Máy "mở phòng" (host). Gọi `peerova_server_start`. Nhận nhiều client. |
| **Client node** | Máy tham gia. Gọi `peerova_client_connect(ticket)`. Kết nối tới đúng một server. |
| **Ticket** | Chuỗi `pvt_...` mà server sinh ra, chứa đủ thông tin để client tìm và xác thực server. Gửi cho client qua bất kỳ kênh nào (chat, QR, API của bạn). Ticket cố định theo `key_file`. |
| **Connect code** | Mã 6 ký tự (ví dụ `K7Q2XD`) thay cho ticket dài. Chỉ có khi server khởi động với `app` + `share` (project trên website). Client truyền mã này vào chỗ ticket. |
| **Connection id (peer)** | Số `uint32` định danh một kết nối. Server thấy mỗi client một id; client thấy server là một id. Dùng cho `send`/`disconnect`. Coi là id mờ, đừng suy luận gì từ giá trị. |
| **Channel** | Số 0..255. Mỗi channel có thứ tự riêng; channel này nghẽn không chặn channel khác. |
| **Reliable** | Có thứ tự (trong một channel), tự gửi lại khi mất, kích thước lớn được (SDK tự chia/ghép). Dùng cho chat, inventory, RPC, lệnh, khúc file. |
| **Unreliable** | Datagram, độ trễ thấp nhất, có thể mất hoặc đảo thứ tự, **tối đa 1180 byte**. Dùng cho input/vị trí gameplay, trạng thái realtime mà gói mới thay thế gói cũ. |
| **Event** | Mọi thứ đến từ mạng đều là sự kiện trong hàng đợi của node: `CONNECTED`, `MESSAGE`, `DISCONNECTED`, `CONNECT_FAILED`. Bạn lấy chúng bằng `poll`. |
| **Dev mode** | Server không có `license` chạy chế độ dev: tối đa 3 client đồng thời. |

## 2. Vòng đời

```
SERVER                                          CLIENT
peerova_server_start(cfg)  ── chặn ≤ ~3 s
peerova_ticket() / peerova_room_code()
        │  ticket hoặc mã 6 ký tự (gửi qua kênh của bạn)  ──►  peerova_client_connect(ticket, cfg)  (trả về ngay*)
        │                                                        │
poll ─► CONNECTED(peer=A)                                poll ─► CONNECTED(peer=S)   | CONNECT_FAILED(reason)
        │◄──────────── send / MESSAGE (cả hai chiều) ──────────►│
poll ─► DISCONNECTED(A, reason)  ◄── peerova_disconnect(S) ─────┤   (hoặc mất mạng / timeout)
peerova_free(server)                                      peerova_free(client)
```
\* Khi truyền **connect code**, `peerova_client_connect` tra mã trên website trước nên chặn trong thời gian một lượt tra cứu qua mạng;
mã sai/đã đóng → trả về `NULL` ngay (đọc `peerova_last_error`), không phải sự kiện `CONNECT_FAILED`. Gọi ngoài UI thread.

## 3. Tham chiếu C API (`include/peerova.h`)

Mọi binding đều bọc đúng các hàm này. Tất cả chuỗi là UTF-8, kết thúc bằng NUL.

### Struct sự kiện
```c
typedef struct {
    int32_t        type;     // PEEROVA_EVENT_CONNECTED=1, DISCONNECTED=2, MESSAGE=3, CONNECT_FAILED=4
    uint32_t       peer;     // connection id
    uint8_t        channel;  // 0..255 (MESSAGE)
    uint8_t        reliable; // 1 nếu đến qua reliable (MESSAGE)
    const uint8_t* data;     // MESSAGE: payload; DISCONNECTED/CONNECT_FAILED: lý do dạng UTF-8 (có thể rỗng)
    uint32_t       len;
} peerova_event;
```
`data` thuộc sở hữu của node và **chỉ hợp lệ tới lần `peerova_poll`/`peerova_poll_wait`/`peerova_free` kế tiếp trên cùng node** — copy nếu cần giữ.

### Hàm

| Hàm | Mô tả | Trả về / lỗi |
|---|---|---|
| `peerova_node* peerova_server_start(const char* config_json)` | Khởi động server. `config_json` có thể `NULL` (mặc định). **Chặn tới ~3 s** chờ ticket — đừng gọi trên UI/game thread. | Node, hoặc `NULL` khi lỗi (xem `peerova_last_error`). Giải phóng bằng `peerova_free`. |
| `peerova_node* peerova_client_connect(const char* ticket, const char* config_json)` | Tạo client tới server. `ticket` = ticket `pvt_...` hoặc connect code 6 ký tự. Với ticket: trả về ngay, kết quả đến qua sự kiện `CONNECTED` / `CONNECT_FAILED`. | Node, hoặc `NULL` (ticket sai định dạng, mã không tồn tại...). |
| `int32_t peerova_ticket(peerova_node*, char* buf, int32_t buf_len)` | Copy ticket của server vào `buf`. Gọi với `buf=NULL,0` để lấy kích thước. | Kích thước cần (gồm NUL); `0` nếu không có (client). Nếu `buf_len` nhỏ hơn thì không copy gì. |
| `int32_t peerova_room_code(peerova_node*, char* buf, int32_t buf_len)` | Như trên cho connect code. Mã đến sau khi website cấp (vài giây sau start) — gọi lại định kỳ. | Kích thước cần, hoặc `0` nếu chưa có / không cấu hình `app`+`share`. |
| `int32_t peerova_send(node, uint32_t peer, uint8_t channel, const uint8_t* data, uint32_t len, int32_t reliable)` | Gửi tới `peer`. `reliable=1`: có thứ tự, gửi lại, kích thước lớn được. `reliable=0`: datagram ≤ 1180 byte. SDK copy dữ liệu trước khi trả về. | `0` thành công; `-1` lỗi (peer không tồn tại, unreliable quá 1180 byte, node NULL) → `peerova_last_error`. |
| `int32_t peerova_poll(node, peerova_event* out)` | Không chặn. Lấy một sự kiện nếu có. | `1` và điền `*out`, hoặc `0`. |
| `int32_t peerova_poll_wait(node, peerova_event* out, uint32_t timeout_ms)` | Như `poll` nhưng chờ tối đa `timeout_ms`. | `1` hoặc `0` (hết giờ). |
| `void peerova_disconnect(node, uint32_t peer)` | Đóng kết nối tới `peer` (server: đá client; client: rời server). Phía bên kia nhận `DISCONNECTED`. | — |
| `uint32_t peerova_peer_count(node)` | Số peer đang kết nối. | — |
| `void peerova_free(node)` | Đóng mọi kết nối, dừng node, giải phóng. `NULL` an toàn. Sau đó không dùng con trỏ nữa. | — |
| `const char* peerova_last_error(void)` | Thông điệp lỗi cuối **của thread hiện tại**. Đọc ngay sau lời gọi lỗi, trên cùng thread. Không free. Nội dung là văn bản cho người đọc (có thể bằng tiếng Việt) — đừng parse. | Chuỗi tĩnh tới lần gọi kế tiếp. |
| `const char* peerova_version(void)` | Phiên bản thư viện, ví dụ `"0.1.0"`. Không free. | — |

### An toàn luồng
- Mỗi node có mutex nội bộ: `send`, `poll`, `poll_wait`, `disconnect`, `peer_count`, `ticket` gọi được từ bất kỳ thread nào.
- Nhiều node độc lập chạy song song được (ví dụ server và client trong cùng process để test).
- **Không** gọi `peerova_free` khi thread khác đang dùng cùng node. Dừng vòng poll trước, rồi mới free.
- `peerova_poll_wait` giữ khoá trong khi chờ: để gửi từ thread khác với độ trễ thấp, dùng timeout ngắn (10–100 ms) hoặc `poll` không chặn trong vòng lặp frame.

### Cấu hình (JSON)

Server:

| Khoá | Mặc định | Ý nghĩa |
|---|---|---|
| `port` | `47000` | Cổng UDP lắng nghe; `0` = ngẫu nhiên. |
| `key_file` | `"server.key"` | File danh tính server; tạo mới nếu chưa có. Cùng key ⇒ cùng ticket. **Bí mật.** |
| `license` | — | Key `PVA-...`. Không có ⇒ dev mode (tối đa 3 client). |
| `max_ccu` | theo license | Giới hạn số client đồng thời (không vượt license). |
| `token` | — | Bí mật chung; client phải gửi đúng `token` mới vào được. |
| `upnp` | `true` | Thử mở port trên router (UPnP/NAT-PMP). |
| `private` | `false` | Ticket không chứa địa chỉ IP của server. |
| `app` | — | Project id trên website — cần để có connect code. |
| `share` | — | `{"name":"Phòng của Nam","public":false,"max":8,"mode":"coop","region":"VN"}` — thông tin hiển thị kèm connect code. |

Client: `token` (phải khớp server), `timeout_ms` (mặc định `20000`: thời gian tối đa trước `CONNECT_FAILED`).

## 4. Mẫu dùng

### Game
- Một thread/vòng frame: mỗi frame gọi `poll` (không chặn) cho tới khi hết sự kiện. Unity `Update()`, Godot `_process`, Unreal subsystem `Tick` (binding đã làm sẵn).
- Gọi `server_start` ngoài main thread (binding Unity/Godot/Unreal đã làm sẵn).
- Input/vị trí: unreliable, 10–30 Hz, gói nhỏ có số thứ tự / timestamp, bên nhận bỏ gói cũ hơn gói đã có.
- Sự kiện quan trọng (bắn trúng, nhặt đồ, chat, vào/ra phòng): reliable trên channel riêng.
- Host-authoritative: client gửi input lên server, server tính và phát trạng thái xuống mọi client (server forward).

### Ứng dụng
- **Chat**: reliable, channel 0, mỗi tin một message.
- **Truyền file**: reliable, chia khúc 16–64 KB trên một channel riêng (để chat không phải chờ), gửi header (tên, kích thước, hash) trước, xác nhận cuối file. Giới hạn tốc độ đẩy khúc để không chiếm hết bộ nhớ hàng đợi.
- **Điều khiển từ xa**: lệnh = reliable; luồng màn hình/telemetry tần suất cao = unreliable (khung mất thì bỏ qua) hoặc reliable trên channel riêng nếu không được mất.
- **Server dedicated**: một thread chạy `poll_wait(100)` trong vòng lặp; gửi từ các thread khác thoải mái.

## 5. Hiệu năng
- Gộp nhiều cập nhật nhỏ trong cùng frame thành một message thay vì gửi từng cái.
- Unreliable: giữ dưới 1180 byte; gửi trạng thái mới nhất, không gửi lại dữ liệu cũ.
- Reliable: message lớn được, nhưng message cực lớn làm trễ các message sau **trên cùng channel** — tách channel.
- Encode nhị phân (struct cố định, MessagePack, protobuf) thay vì JSON cho gói tần suất cao.
- Mỗi lần `poll` hãy rút hết hàng đợi (lặp tới khi trả về 0), không chỉ một sự kiện mỗi frame.
- Sidecar (Node/PHP/Lua thuần/shell) thêm vài ms + base64 cho mỗi gói; game cần độ trễ thấp nên dùng native FFI.

## 6. Bảo mật
- Toàn bộ lưu lượng mã hoá đầu-cuối; client xác thực server bằng khoá trong ticket.
- `server.key` (`key_file`) là danh tính server: ai có file này giả mạo được server. Không commit, không gửi đi, phân quyền chỉ đọc cho user chạy server.
- Ticket/connect code không phải bí mật tuyệt đối — ai có nó thì thử kết nối được. Muốn giới hạn người vào: dùng `token`, hoặc `private`, hoặc kiểm tra ở tầng ứng dụng sau `CONNECTED`.
- `license` và `token` là bí mật: không nhúng license vào bản client phát hành.

## 7. FAQ
- **Có cần mở port / IP tĩnh không?** Không. Kết nối tự động; mở port chỉ giúp nhanh hơn.
- **Client không vào được, chỉ 3 người?** Server đang ở dev mode — thêm `license`.
- **`CONNECT_FAILED` / timeout?** Chạy `peerova doctor` trên cả hai máy (công cụ dòng lệnh `peerova`), kiểm tra `token`, kiểm tra ticket còn đúng `key_file` server đang dùng.
- **Gửi unreliable lỗi?** Gói > 1180 byte bị từ chối — dùng reliable hoặc chia nhỏ.
- **Ticket đổi mỗi lần chạy?** Server đang tạo `key_file` mới (đường dẫn tạm hoặc khác thư mục). Giữ cố định `key_file`.
- **Không tìm thấy thư viện?** Đặt `libpeerova.so` / `peerova.dll` / `libpeerova.dylib` đúng chỗ (xem README của binding) hoặc đặt biến môi trường `PEEROVA_LIB` (binding nào hỗ trợ có ghi rõ).
- **Windows có cần `wintun.dll`?** Không cho SDK. `wintun.dll` chỉ cần cho chế độ VPN của công cụ dòng lệnh `peerova.exe`.
- **Android / iOS / macOS?** Liên hệ Peerova để nhận thư viện cho các nền tảng này; vị trí đặt file có trong README từng binding.

---

# English

## 1. Concepts

Peerova connects two (or more) machines directly, end-to-end encrypted, without port forwarding or a public IP.
Connections are automatic: direct first; when a network blocks direct connections the SDK uses an automatic
encrypted fallback route and upgrades to direct as soon as it can. Your application does nothing extra.

| Concept | Meaning |
|---|---|
| **Server node** | The host. Created with `peerova_server_start`. Accepts many clients. |
| **Client node** | The joining side. Created with `peerova_client_connect(ticket)`. Connects to exactly one server. |
| **Ticket** | A `pvt_...` string produced by the server; everything a client needs to find and authenticate it. Share it through any channel (chat, QR code, your API). Stable for a given `key_file`. |
| **Connect code** | A 6-character code (e.g. `K7Q2XD`) usable instead of the ticket. Only available when the server is started with `app` + `share` (a project on the website). Clients pass it where a ticket goes. |
| **Connection id (peer)** | A `uint32` identifying one connection. The server sees one id per client; a client sees the server as one id. Used with `send`/`disconnect`. Treat it as opaque. |
| **Channel** | 0..255. Ordering is per channel; a stalled channel never blocks another. |
| **Reliable** | Ordered (per channel), retransmitted, can be large (the SDK fragments/reassembles). Use for chat, inventory, RPCs, commands, file chunks. |
| **Unreliable** | Datagram, lowest latency, may be lost or reordered, **max 1180 bytes**. Use for gameplay input/positions and realtime state where a newer packet replaces an older one. |
| **Event** | Everything from the network is an event in the node's queue: `CONNECTED`, `MESSAGE`, `DISCONNECTED`, `CONNECT_FAILED`. You fetch them with `poll`. |
| **Dev mode** | A server without `license` runs in dev mode: at most 3 concurrent clients. |

## 2. Lifecycle

```
SERVER                                          CLIENT
peerova_server_start(cfg)  ── blocks ≤ ~3 s
peerova_ticket() / peerova_room_code()
        │  ticket or 6-char code (sent over your own channel)  ──►  peerova_client_connect(ticket, cfg)  (returns at once*)
        │                                                        │
poll ─► CONNECTED(peer=A)                                poll ─► CONNECTED(peer=S)   | CONNECT_FAILED(reason)
        │◄──────────── send / MESSAGE (both directions) ────────►│
poll ─► DISCONNECTED(A, reason)  ◄── peerova_disconnect(S) ─────┤   (or network loss / timeout)
peerova_free(server)                                      peerova_free(client)
```
\* With a **connect code**, `peerova_client_connect` first looks the code up on the website, so it blocks for one network lookup; an unknown/closed code returns `NULL` immediately (read `peerova_last_error`) rather than a
`CONNECT_FAILED` event. Call it off the UI thread.

## 3. C API reference (`include/peerova.h`)

Every binding wraps exactly these functions. All strings are NUL-terminated UTF-8.

### Event struct
```c
typedef struct {
    int32_t        type;     // PEEROVA_EVENT_CONNECTED=1, DISCONNECTED=2, MESSAGE=3, CONNECT_FAILED=4
    uint32_t       peer;     // connection id
    uint8_t        channel;  // 0..255 (MESSAGE)
    uint8_t        reliable; // 1 if it arrived reliably (MESSAGE)
    const uint8_t* data;     // MESSAGE: payload; DISCONNECTED/CONNECT_FAILED: UTF-8 reason (may be empty)
    uint32_t       len;
} peerova_event;
```
`data` is owned by the node and **valid only until the next `peerova_poll` / `peerova_poll_wait` / `peerova_free`
on the same node** — copy it if you keep it.

### Functions

| Function | Description | Returns / errors |
|---|---|---|
| `peerova_node* peerova_server_start(const char* config_json)` | Starts a server. `config_json` may be `NULL` (defaults). **Blocks up to ~3 s** until the ticket is ready — don't call it on a UI/game thread. | Node, or `NULL` on error (see `peerova_last_error`). Release with `peerova_free`. |
| `peerova_node* peerova_client_connect(const char* ticket, const char* config_json)` | Creates a client. `ticket` = a `pvt_...` ticket or a 6-character connect code. With a ticket it returns immediately; the outcome arrives as `CONNECTED` / `CONNECT_FAILED`. | Node, or `NULL` (malformed ticket, unknown code, ...). |
| `int32_t peerova_ticket(peerova_node*, char* buf, int32_t buf_len)` | Copies the server's ticket into `buf`. Call with `buf=NULL,0` to get the size. | Required size incl. NUL; `0` if none (clients). Nothing is copied if `buf_len` is too small. |
| `int32_t peerova_room_code(peerova_node*, char* buf, int32_t buf_len)` | Same for the connect code. The code arrives a few seconds after start, once the website assigns it — poll it periodically. | Required size, or `0` if not available yet / `app`+`share` not configured. |
| `int32_t peerova_send(node, uint32_t peer, uint8_t channel, const uint8_t* data, uint32_t len, int32_t reliable)` | Sends to `peer`. `reliable=1`: ordered, retransmitted, any size. `reliable=0`: datagram ≤ 1180 bytes. Data is copied before returning. | `0` on success; `-1` on error (unknown peer, unreliable over 1180 bytes, NULL node) → `peerova_last_error`. |
| `int32_t peerova_poll(node, peerova_event* out)` | Non-blocking. Fetches one event if available. | `1` and fills `*out`, or `0`. |
| `int32_t peerova_poll_wait(node, peerova_event* out, uint32_t timeout_ms)` | Like `poll` but waits up to `timeout_ms`. | `1`, or `0` on timeout. |
| `void peerova_disconnect(node, uint32_t peer)` | Closes the connection to `peer` (server: kick; client: leave). The other side gets `DISCONNECTED`. | — |
| `uint32_t peerova_peer_count(node)` | Number of connected peers. | — |
| `void peerova_free(node)` | Closes all connections, stops and frees the node. `NULL` is safe. Don't use the pointer afterwards. | — |
| `const char* peerova_last_error(void)` | Last error message **for the calling thread**. Read it right after the failing call, on the same thread. Don't free it. Human-readable text (may be in Vietnamese) — don't parse it. | Static until the next call. |
| `const char* peerova_version(void)` | Library version, e.g. `"0.1.0"`. Don't free it. | — |

### Thread safety
- Each node has an internal mutex: `send`, `poll`, `poll_wait`, `disconnect`, `peer_count`, `ticket` may be called from any thread.
- Independent nodes run in parallel (e.g. server and client in one process for testing).
- **Never** call `peerova_free` while another thread uses the same node. Stop your poll loop first, then free.
- `peerova_poll_wait` holds the node lock while it waits: if other threads send with low latency, use short timeouts (10–100 ms) or non-blocking `poll` in a frame loop.

### Configuration (JSON)

Server:

| Key | Default | Meaning |
|---|---|---|
| `port` | `47000` | UDP port to listen on; `0` = random. |
| `key_file` | `"server.key"` | Server identity file; created if missing. Same key ⇒ same ticket. **Secret.** |
| `license` | — | `PVA-...` key. Without it: dev mode (max 3 clients). |
| `max_ccu` | from license | Cap on concurrent clients (never above the license). |
| `token` | — | Shared secret; clients must present the same `token`. |
| `upnp` | `true` | Try to open a port on the router (UPnP/NAT-PMP). |
| `private` | `false` | The ticket carries no server IP addresses. |
| `app` | — | Project id from the website — required for a connect code. |
| `share` | — | `{"name":"Nam's room","public":false,"max":8,"mode":"coop","region":"VN"}` — info shown with the connect code. |

Client: `token` (must match the server), `timeout_ms` (default `20000`: max time before `CONNECT_FAILED`).

## 4. Patterns

### Games
- One thread / frame loop: every frame call `poll` (non-blocking) until the queue is empty. Unity `Update()`, Godot `_process`, Unreal subsystem `Tick` (the bindings already do this).
- Start the server off the main thread (the Unity/Godot/Unreal bindings already do).
- Input/positions: unreliable, 10–30 Hz, small packets with a sequence number/timestamp; the receiver drops packets older than the newest it has.
- Important events (hits, pickups, chat, join/leave): reliable on their own channel.
- Host-authoritative: clients send input to the server, the server simulates and broadcasts state to every client (server forwarding).

### Apps
- **Chat**: reliable, channel 0, one message per line.
- **File transfer**: reliable, 16–64 KB chunks on a dedicated channel (so chat doesn't wait), a header first (name, size, hash), an acknowledgement at the end. Pace the chunks so the send queue doesn't grow without bound.
- **Remote control**: commands = reliable; high-rate screen/telemetry = unreliable (drop lost frames) or reliable on a separate channel if nothing may be lost.
- **Dedicated server**: one thread runs `poll_wait(100)` in a loop; send freely from other threads.

## 5. Performance tips
- Batch many small updates from the same frame into one message.
- Unreliable: stay under 1180 bytes; send the latest state, never resend stale data.
- Reliable: large messages are fine, but a huge one delays later messages **on the same channel** — use separate channels.
- Use binary encodings (fixed structs, MessagePack, protobuf) instead of JSON for high-rate packets.
- Drain the whole queue on each poll pass (loop until it returns 0), not one event per frame.
- Sidecar bindings (Node/PHP/plain Lua/shell) add a few ms + base64 per packet; latency-sensitive games should use native FFI.

## 6. Security
- All traffic is end-to-end encrypted; clients authenticate the server with the key in the ticket.
- `server.key` (`key_file`) is the server identity: anyone holding it can impersonate your server. Never commit or send it; make it readable only by the server's user.
- Tickets/connect codes aren't strong secrets — whoever has one can attempt to connect. To restrict access use `token`, `private`, or an application-level check after `CONNECTED`.
- `license` and `token` are secrets: never embed the license in a shipped client.

## 7. FAQ
- **Do I need port forwarding / a static IP?** No. Connections are automatic; an open port only makes them faster.
- **Only 3 clients can join?** The server is in dev mode — add a `license`.
- **`CONNECT_FAILED` / timeout?** Run `peerova doctor` (command-line tool) on both machines, check `token`, check the ticket matches the server's current `key_file`.
- **Unreliable send fails?** Packets over 1180 bytes are rejected — use reliable or split them.
- **The ticket changes on every run?** The server creates a new `key_file` each time (temp path or different working directory). Keep `key_file` fixed.
- **Library not found?** Put `libpeerova.so` / `peerova.dll` / `libpeerova.dylib` where the binding's README says, or set `PEEROVA_LIB` (where supported).
- **Does Windows need `wintun.dll`?** Not for the SDK. `wintun.dll` is only needed by the VPN mode of the `peerova.exe` command-line tool.
- **Android / iOS / macOS?** Contact Peerova for these builds; each binding's README says where the files go.
