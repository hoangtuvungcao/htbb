# Peerova cho Bun

[Tiếng Việt](#tiếng-việt) · [English](#english)

> **Trạng thái / Status:** binding này và ví dụ **chưa được chạy thử** — máy build không cài Bun. Code đã được
> kiểm tra kiểu bằng TypeScript với khai báo `bun:ffi` rút gọn, nhưng chưa từng chạy trên Bun thật.
> This binding and its example are **untested** — Bun is not installed on the build machine. The code was
> type-checked with TypeScript against a reduced `bun:ffi` declaration, but has never run on a real Bun.

---

## Tiếng Việt

Binding Bun cho Peerova: kết nối peer-to-peer trực tiếp, mã hoá đầu-cuối. Gọi thư viện native trực tiếp trong
tiến trình qua `bun:ffi`; một file `peerova.ts`, không phụ thuộc gói nào.

Không muốn dùng thư viện native? Gói Node ([`../nodejs`](../nodejs), dùng sidecar) cũng chạy trên Bun, với API
dạng `EventEmitter`.

### Yêu cầu

- Bun 1.0 trở lên (cần 1.1+ cho cú pháp `using`).
- Thư viện native Peerova cho hệ điều hành của bạn.

### Đặt thư viện native ở đâu

Trong gói SDK: `include/peerova.h`, `lib/linux-x86_64/libpeerova.so` (+ `libpeerova.a` và binary `peerova`),
`lib/windows-x86_64/peerova.dll` (+ `peerova.exe`), `bindings/bun/`.

Thứ tự tìm: biến môi trường `PEEROVA_LIB` (đường dẫn đầy đủ) → cạnh `peerova.ts` → thư mục `lib/<nền tảng>` của
SDK → đường tìm kiếm mặc định của hệ thống.

| Hệ điều hành | File | Đặt ở đâu |
|---|---|---|
| Windows | `peerova.dll` | cạnh `peerova.ts`, hoặc `PEEROVA_LIB`. Không cần `wintun.dll` (chỉ dùng cho chế độ VPN của công cụ dòng lệnh `peerova`). Với `bun build --compile`: đặt `peerova.dll` cạnh file `.exe` và đặt `PEEROVA_LIB`. |
| Linux | `libpeerova.so` | cạnh `peerova.ts`, thư mục trong `LD_LIBRARY_PATH`, hoặc `PEEROVA_LIB` |
| macOS | `libpeerova.dylib` | liên hệ Peerova để nhận bản build này; khi có, đặt cạnh `peerova.ts` hoặc dùng `PEEROVA_LIB` |

### Cài đặt

1. Chép `bindings/bun/peerova.ts` vào project.
2. Chép thư viện native cạnh nó, hoặc đặt `PEEROVA_LIB`.
3. Kiểm tra: `bun -e 'import { version } from "./peerova.ts"; console.log(version())'`.

### Bắt đầu nhanh (5 phút)

Mọi lời gọi `bun:ffi` là đồng bộ. Cách dùng khuyên dùng: gọi `poll()` (timeout 0, không chặn) trong `setInterval`
cho tới khi nhận `null`.

Server:

```ts
import { EventType, PeerovaServer } from "./peerova.ts";

const srv = PeerovaServer.start({ port: 47000, key_file: "server.key" }); // chặn tới khi có ticket (≤ ~3 s)
console.log("ticket:", srv.ticket);                                      // gửi cho client
setInterval(() => {
  for (let ev; (ev = srv.poll()); ) {
    if (ev.type === EventType.Message) srv.send(ev.peer, `echo: ${ev.text}`);
  }
}, 10);
```

Mã kết nối 6 ký tự: khởi động server với `app` (mã project trên website Peerova) và `share`, rồi đọc
`srv.roomCode`. Website cấp mã ngay sau khi khởi động, nên lúc đầu có thể là `null` — đọc lại sau ít giây.

```ts
const srv = PeerovaServer.start({ app: "prj_xxx", share: { name: "Phòng của Lan", public: true, max: 8 } });
console.log(srv.roomCode); // ví dụ "K7Q2XD", hoặc null nếu chưa có
```

Client (truyền ticket **hoặc** mã 6 ký tự):

```ts
import { EventType, PeerovaClient } from "./peerova.ts";

const cli = PeerovaClient.connect("pvt_...");   // hoặc PeerovaClient.connect("K7Q2XD")
setInterval(() => {
  for (let ev; (ev = cli.poll()); ) {
    if (ev.type === EventType.Connected) cli.send(ev.peer, "hello");
    else if (ev.type === EventType.ConnectFailed) console.error("lỗi:", ev.text);
  }
}, 10);
```

Kết nối là tự động: thử kết nối trực tiếp trước, và tự chuyển sang một đường dự phòng đã mã hoá khi mạng chặn
kết nối trực tiếp.

### Gửi: reliable hay unreliable

```ts
node.send(peer, data, { channel: 0, reliable: true });
```

| | `reliable: true` (mặc định) | `reliable: false` |
|---|---|---|
| Đảm bảo | đến đủ, đúng thứ tự trong cùng channel | có thể mất hoặc đến sai thứ tự |
| Kích thước | lớn tuỳ ý | tối đa **1180 byte** (`MAX_UNRELIABLE_PAYLOAD`) |
| Dùng cho | chat, túi đồ, RPC, lệnh, từng khúc file | input gameplay, vị trí, trạng thái realtime kiểu giọng nói |

`channel` từ 0 đến 255; các channel độc lập, channel này chậm không làm kẹt channel khác. `data` là `string`
(gửi UTF-8) hoặc `Uint8Array`.

### Nhận sự kiện

`poll(timeoutMs)` trả về `PeerovaEvent` (hoặc `null`): `type`, `peer`, `channel`, `reliable`, `data`
(`Uint8Array`, đã chép ra, giữ lại được), `text`.

| `type` | Ý nghĩa |
|---|---|
| `EventType.Connected` | có peer kết nối (server: client mới; client: đã vào server) |
| `EventType.Message` | có tin nhắn |
| `EventType.Disconnected` | peer rời đi; `text` = lý do |
| `EventType.ConnectFailed` | (client) kết nối thất bại; `text` = lý do |

- `poll()` / `poll(0)`: không chặn — dùng trong `setInterval` (khoảng 10–16 ms) như game loop.
- `poll(ms > 0)` và `events(idleMs)`: **chặn luồng JS** trong lúc chờ. Chỉ dùng trong script không làm gì khác, hoặc
  trong một `Worker`: tạo node và chạy vòng `poll(100)` bên trong Worker, trao đổi với luồng chính bằng
  `postMessage`.
- Mỗi node chỉ dùng trong một luồng JS (luồng chính hoặc một Worker); không chia sẻ node giữa các Worker.

### Ngắt kết nối và dọn dẹp

- `node.disconnect(peer)`: đóng kết nối tới một peer.
- `node.close()`: ngắt tất cả và giải phóng node; gọi hai lần vẫn an toàn. Hoặc dùng `using node = ...`.
- Dừng timer `poll` (`clearInterval`) trước khi `close()`; sau `close()` mọi lời gọi ném `PeerovaError("node is closed")`.

### Xử lý lỗi

Mọi lỗi ném `PeerovaError`; thông báo lấy từ thư viện native.

- `PeerovaServer.start()`: không mở được cổng, config sai, license không hợp lệ.
- `PeerovaClient.connect()`: ticket/mã sai định dạng. Lỗi mạng đến dưới dạng sự kiện `ConnectFailed`.
- `send()`: channel ngoài 0..255, tin unreliable quá 1180 byte, peer đã rời, node đã đóng.
- Không tìm thấy thư viện khi import: lỗi của `dlopen`.

### Tuỳ chọn cấu hình

Server (`PeerovaServer.start(config)`):

| Khoá | Mặc định | Ý nghĩa |
|---|---|---|
| `port` | `47000` | cổng lắng nghe; `0` = cổng ngẫu nhiên |
| `key_file` | `"server.key"` | file danh tính server. Cùng key ⇒ cùng ticket |
| `license` | — | key `PVA-...`. Không có: chế độ dev, tối đa 3 client |
| `max_ccu` | — | số client đồng thời tối đa |
| `token` | — | bí mật chung mà client phải gửi |
| `upnp` | `true` | thử mở cổng trên router |
| `private` | `false` | ticket không chứa địa chỉ IP của server |
| `app` | — | mã project trên website Peerova (để có mã kết nối) |
| `share` | — | `{ name, public, max, mode, region }`: công bố mã kết nối 6 ký tự |

Client (`PeerovaClient.connect(ticketOrCode, config)`): `token` (phải trùng server), `timeout_ms` (mặc định 20000).

### Bảo mật

- `server.key` là danh tính của server: ai có file này có thể giả mạo server. Giữ bí mật, **không** commit.
- Ticket là thông tin bán công khai: cứ chia sẻ cho client.
- `token` và `license` là bí mật.
- Toàn bộ lưu lượng được mã hoá đầu-cuối.

### Ví dụ

[`examples/chat.ts`](examples/chat.ts) (chưa chạy thử): chat + đồng bộ vị trí (vị trí unreliable ~20 Hz, chat
reliable; server chuyển tiếp cho các client khác).

```sh
bun examples/chat.ts server --key server.key           # in ra: TICKET pvt_...
bun examples/chat.ts client pvt_... --seconds 10       # gõ một dòng rồi Enter để chat
```

Test: `bun test` (trong thư mục này).

### Khắc phục sự cố

- Chạy `peerova doctor` (công cụ dòng lệnh trong SDK) để chẩn đoán mạng.
- Lỗi `dlopen` / không tìm thấy thư viện: đặt thư viện cạnh `peerova.ts` hoặc đặt `PEEROVA_LIB`.
- `ConnectFailed` sau khoảng 20 giây: kiểm tra ticket/mã, server còn chạy, `token` khớp; chạy `peerova doctor`.
- Client thứ 4 không vào được: server ở chế độ dev (không có `license`), tối đa 3 client.
- `send(..., { reliable: false })` ném lỗi: payload quá 1180 byte.
- Ứng dụng bị "đơ": có chỗ gọi `poll(ms > 0)` trên luồng chính — đổi sang `poll()` trong timer hoặc chuyển vào Worker.

Khái niệm và toàn bộ C API: [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md). Website: https://peerova.pages.dev

---

## English

Bun binding for Peerova: direct peer-to-peer connections, end-to-end encrypted. Calls the native library in-process
through `bun:ffi`; a single file, `peerova.ts`, with no dependencies.

Don't want a native library? The Node package ([`../nodejs`](../nodejs), sidecar-based) also runs on Bun, with an
`EventEmitter` API.

### Requirements

- Bun 1.0 or newer (1.1+ for the `using` syntax).
- The Peerova native library for your operating system.

### Where to put the native library

SDK package layout: `include/peerova.h`, `lib/linux-x86_64/libpeerova.so` (+ `libpeerova.a` and the `peerova`
binary), `lib/windows-x86_64/peerova.dll` (+ `peerova.exe`), `bindings/bun/`.

Lookup order: the `PEEROVA_LIB` environment variable (full path) → next to `peerova.ts` → the SDK's `lib/<platform>`
folder → the system's default search path.

| OS | File | Where |
|---|---|---|
| Windows | `peerova.dll` | next to `peerova.ts`, or `PEEROVA_LIB`. `wintun.dll` is not needed (it is only used by the `peerova` command-line tool's VPN mode). With `bun build --compile`: put `peerova.dll` next to the `.exe` and set `PEEROVA_LIB`. |
| Linux | `libpeerova.so` | next to `peerova.ts`, a folder on `LD_LIBRARY_PATH`, or `PEEROVA_LIB` |
| macOS | `libpeerova.dylib` | contact Peerova for this build; once you have it, put it next to `peerova.ts` or use `PEEROVA_LIB` |

### Install

1. Copy `bindings/bun/peerova.ts` into your project.
2. Copy the native library next to it, or set `PEEROVA_LIB`.
3. Check: `bun -e 'import { version } from "./peerova.ts"; console.log(version())'`.

### 5-minute quick start

All `bun:ffi` calls are synchronous. Recommended pattern: call `poll()` (timeout 0, non-blocking) from a `setInterval`
until it returns `null`.

Server:

```ts
import { EventType, PeerovaServer } from "./peerova.ts";

const srv = PeerovaServer.start({ port: 47000, key_file: "server.key" }); // blocks until the ticket is ready (<= ~3 s)
console.log("ticket:", srv.ticket);                                      // give it to clients
setInterval(() => {
  for (let ev; (ev = srv.poll()); ) {
    if (ev.type === EventType.Message) srv.send(ev.peer, `echo: ${ev.text}`);
  }
}, 10);
```

6-character connect code: start the server with `app` (your project id from the Peerova website) and `share`, then
read `srv.roomCode`. The website assigns the code shortly after start, so it can be `null` at first — read it again a
few seconds later.

```ts
const srv = PeerovaServer.start({ app: "prj_xxx", share: { name: "Lan's room", public: true, max: 8 } });
console.log(srv.roomCode); // e.g. "K7Q2XD", or null if not assigned yet
```

Client (pass the ticket **or** the 6-character code):

```ts
import { EventType, PeerovaClient } from "./peerova.ts";

const cli = PeerovaClient.connect("pvt_...");   // or PeerovaClient.connect("K7Q2XD")
setInterval(() => {
  for (let ev; (ev = cli.poll()); ) {
    if (ev.type === EventType.Connected) cli.send(ev.peer, "hello");
    else if (ev.type === EventType.ConnectFailed) console.error("failed:", ev.text);
  }
}, 10);
```

Connections are automatic: direct first, with an automatic encrypted fallback route when networks block direct
connections.

### Sending: reliable vs unreliable

```ts
node.send(peer, data, { channel: 0, reliable: true });
```

| | `reliable: true` (default) | `reliable: false` |
|---|---|---|
| Guarantee | delivered, in order within the channel | may be lost or reordered |
| Size | any size | at most **1180 bytes** (`MAX_UNRELIABLE_PAYLOAD`) |
| Use for | chat, inventory, RPC, commands, file chunks | gameplay inputs, positions, voice-like realtime state |

`channel` is 0..255; channels are independent, a slow channel never blocks another. `data` is a `string` (sent as
UTF-8) or a `Uint8Array`.

### Receiving events

`poll(timeoutMs)` returns a `PeerovaEvent` (or `null`): `type`, `peer`, `channel`, `reliable`, `data`
(`Uint8Array`, already copied, safe to keep), `text`.

| `type` | Meaning |
|---|---|
| `EventType.Connected` | a peer connected (server: a new client; client: joined the server) |
| `EventType.Message` | a message arrived |
| `EventType.Disconnected` | a peer left; `text` = reason |
| `EventType.ConnectFailed` | (client) the connection attempt failed; `text` = reason |

- `poll()` / `poll(0)`: non-blocking — use it from a `setInterval` (about 10–16 ms) like a game loop.
- `poll(ms > 0)` and `events(idleMs)`: **block the JS thread** while waiting. Use them only in a script that has
  nothing else to do, or inside a `Worker`: create the node and run a `poll(100)` loop in the Worker, and talk to the
  main thread with `postMessage`.
- Use each node from one JS thread only (the main thread or one Worker); nodes cannot be shared between Workers.

### Disconnect and cleanup

- `node.disconnect(peer)`: close the connection to one peer.
- `node.close()`: disconnect everyone and free the node; calling it twice is safe. Or use `using node = ...`.
- Stop the `poll` timer (`clearInterval`) before `close()`; after `close()` every call throws
  `PeerovaError("node is closed")`.

### Error handling

All errors throw `PeerovaError`; the message comes from the native library.

- `PeerovaServer.start()`: port cannot be opened, invalid config, invalid license.
- `PeerovaClient.connect()`: malformed ticket/code. Network failures arrive as a `ConnectFailed` event.
- `send()`: channel outside 0..255, unreliable payload over 1180 bytes, peer gone, node closed.
- Library not found at import time: the error from `dlopen`.

### Config options

Server (`PeerovaServer.start(config)`):

| Key | Default | Meaning |
|---|---|---|
| `port` | `47000` | listening port; `0` = random port |
| `key_file` | `"server.key"` | server identity file. Same key ⇒ same ticket |
| `license` | — | `PVA-...` key. Without it: dev mode, at most 3 clients |
| `max_ccu` | — | maximum concurrent clients |
| `token` | — | shared secret clients must present |
| `upnp` | `true` | try to open the port on the router |
| `private` | `false` | the ticket does not contain the server's IP addresses |
| `app` | — | project id from the Peerova website (needed for connect codes) |
| `share` | — | `{ name, public, max, mode, region }`: publish a 6-character connect code |

Client (`PeerovaClient.connect(ticketOrCode, config)`): `token` (must match the server), `timeout_ms` (default 20000).

### Security

- `server.key` is the server's identity: anyone who has it can impersonate your server. Keep it secret and do **not**
  commit it.
- The ticket is public-ish: share it with clients.
- `token` and `license` are secrets.
- All traffic is end-to-end encrypted.

### Example

[`examples/chat.ts`](examples/chat.ts) (untested): chat + position sync (positions unreliable at ~20 Hz, chat
reliable; the server forwards to the other clients).

```sh
bun examples/chat.ts server --key server.key           # prints: TICKET pvt_...
bun examples/chat.ts client pvt_... --seconds 10       # type a line and press Enter to chat
```

Test: `bun test` (in this folder).

### Troubleshooting

- Run `peerova doctor` (the command-line tool in the SDK) to diagnose the network.
- `dlopen` error / library not found: put the library next to `peerova.ts` or set `PEEROVA_LIB`.
- `ConnectFailed` after about 20 seconds: check the ticket/code, that the server is running, and that `token`
  matches; run `peerova doctor`.
- The 4th client cannot join: the server runs in dev mode (no `license`), at most 3 clients.
- `send(..., { reliable: false })` throws: the payload is over 1180 bytes.
- The app "freezes": something calls `poll(ms > 0)` on the main thread — switch to `poll()` in a timer or move it into
  a Worker.

Concepts and the full C API: [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md). Website: https://peerova.pages.dev
