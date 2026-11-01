# Peerova cho Deno

[Tiếng Việt](#tiếng-việt) · [English](#english)

> **Trạng thái / Status:** **TESTED** với Deno 2.9.8 trên Linux x86_64: `deno test ...` chạy round-trip thật
> (200 reliable đúng thứ tự, unreliable hai chiều, trả lời nhị phân, disconnect) và `examples/chat.ts` chạy với 1 server + 2 client.
> Windows / macOS chưa được kiểm tra.
> **TESTED** with Deno 2.9.8 on Linux x86_64: the test runs a real round-trip (200 reliable in order, unreliable both ways,
> binary reply, disconnect) and `examples/chat.ts` runs with 1 server + 2 clients. Windows / macOS are not tested yet.

---

## Tiếng Việt

Binding Deno cho Peerova: kết nối peer-to-peer trực tiếp, mã hoá đầu-cuối. Gọi thư viện native trực tiếp trong
tiến trình qua `Deno.dlopen` (FFI), một file `mod.ts`, không phụ thuộc gói nào.

### Yêu cầu

- Deno 1.38 trở lên (hoặc Deno 2).
- Quyền: `--allow-ffi`, cộng `--allow-env --allow-read` để tìm thư viện (và `--allow-write` cho server khi file
  `key_file` chưa tồn tại).
- Thư viện native Peerova cho hệ điều hành của bạn.

### Đặt thư viện native ở đâu

Trong gói SDK: `include/peerova.h`, `lib/linux-x86_64/libpeerova.so` (+ `libpeerova.a` và binary `peerova`),
`lib/windows-x86_64/peerova.dll` (+ `peerova.exe`), `bindings/deno/`.

Thứ tự tìm: biến môi trường `PEEROVA_LIB` (đường dẫn đầy đủ) → cạnh `mod.ts` → thư mục `lib/<nền tảng>` của SDK →
đường tìm kiếm mặc định của hệ thống.

| Hệ điều hành | File | Đặt ở đâu |
|---|---|---|
| Windows | `peerova.dll` | cạnh `mod.ts`, hoặc `PEEROVA_LIB`. Không cần `wintun.dll` (chỉ dùng cho chế độ VPN của công cụ dòng lệnh `peerova`). Với `deno compile`: đặt `peerova.dll` cạnh file `.exe` và đặt `PEEROVA_LIB`. |
| Linux | `libpeerova.so` | cạnh `mod.ts`, thư mục trong `LD_LIBRARY_PATH`, hoặc `PEEROVA_LIB` |
| macOS | `libpeerova.dylib` | liên hệ Peerova để nhận bản build này; khi có, đặt cạnh `mod.ts` hoặc dùng `PEEROVA_LIB` |

### Cài đặt

1. Chép `bindings/deno/mod.ts` vào project.
2. Chép thư viện native cạnh nó, hoặc đặt `PEEROVA_LIB`.
3. Kiểm tra: `deno eval --allow-ffi --allow-env --allow-read 'import { version } from "./mod.ts"; console.log(version())'`.

### Bắt đầu nhanh (5 phút)

Server:

```ts
import { EventType, PeerovaServer } from "./mod.ts";

using srv = await PeerovaServer.startAsync({ port: 47000, key_file: "server.key" }); // xong khi có ticket
console.log("ticket:", srv.ticket);                                                   // gửi cho client
for (;;) {
  const ev = await srv.pollAsync(1000);
  if (ev?.type === EventType.Message) srv.send(ev.peer, `echo: ${ev.text}`);
}
```

Mã kết nối 6 ký tự: khởi động server với `app` (mã project trên website Peerova) và `share`, rồi đọc
`srv.roomCode`. Website cấp mã ngay sau khi khởi động, nên lúc đầu có thể là `null` — đọc lại sau ít giây.

```ts
const srv = PeerovaServer.start({ app: "prj_xxx", share: { name: "Phòng của Lan", public: true, max: 8 } });
console.log(srv.roomCode); // ví dụ "K7Q2XD", hoặc null nếu chưa có
```

Client (truyền ticket **hoặc** mã 6 ký tự):

```ts
import { EventType, PeerovaClient } from "./mod.ts";

using cli = PeerovaClient.connect("pvt_...");   // hoặc PeerovaClient.connect("K7Q2XD")
const ev = await cli.pollAsync(20_000);
if (ev?.type === EventType.Connected) cli.send(ev.peer, "hello");
else if (ev?.type === EventType.ConnectFailed) console.error("lỗi:", ev.text);
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

`poll`/`pollAsync` trả về `PeerovaEvent` (hoặc `null`): `type`, `peer`, `channel`, `reliable`, `data`
(`Uint8Array`, đã chép ra, giữ lại được), `text`.

| `type` | Ý nghĩa |
|---|---|
| `EventType.Connected` | có peer kết nối (server: client mới; client: đã vào server) |
| `EventType.Message` | có tin nhắn |
| `EventType.Disconnected` | peer rời đi; `text` = lý do |
| `EventType.ConnectFailed` | (client) kết nối thất bại; `text` = lý do |

Cách nhận:

- `await node.pollAsync(ms)`: chờ trên một luồng native, event loop vẫn chạy. **Chỉ một** `pollAsync` đang chờ cho
  mỗi node; gọi thêm sẽ ném lỗi.
- `for await (const ev of node.events(idleMs))`: lặp cho tới khi `idleMs` trôi qua mà không có sự kiện.
- `node.poll()` (timeout 0): không chặn, dùng trong `setInterval` theo kiểu game loop. `poll(ms > 0)` chặn luồng JS.

`send`, `disconnect`, `peerCount` gọi được bất cứ lúc nào, kể cả khi một `pollAsync` đang chờ.

Mẫu vòng lặp có dừng:

```ts
let running = true;
const loop = (async () => {
  while (running) {
    const ev = await cli.pollAsync(100);   // timeout ngắn để thấy cờ running nhanh
    if (ev) handle(ev);
  }
})();
// ... khi muốn dừng:
running = false;
await loop;        // bảo đảm không còn pollAsync nào đang chờ
cli.close();
```

### Ngắt kết nối và dọn dẹp

- `node.disconnect(peer)`: đóng kết nối tới một peer.
- `node.close()`: ngắt tất cả và giải phóng node; gọi hai lần vẫn an toàn. Hoặc dùng `using node = ...`.
- **Không** gọi `close()` khi đang có `pollAsync` chờ (sẽ ném `PeerovaError`): dừng vòng lặp và `await` nó trước.

### Xử lý lỗi

Mọi lỗi ném `PeerovaError`; thông báo lấy từ thư viện native.

- `PeerovaServer.start()`: không mở được cổng, config sai, license không hợp lệ. `startAsync()` không có chi tiết
  lỗi (thông báo lỗi lưu theo từng luồng) — gọi `start()` để xem.
- `PeerovaClient.connect()`: ticket/mã sai định dạng. Lỗi mạng đến dưới dạng sự kiện `ConnectFailed`.
- `send()`: channel ngoài 0..255, tin unreliable quá 1180 byte, peer đã rời, node đã đóng.
- Không tìm thấy thư viện khi import: lỗi của `Deno.dlopen`.

### Tuỳ chọn cấu hình

Server (`PeerovaServer.start(config)` / `startAsync(config)`):

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

[`examples/chat.ts`](examples/chat.ts): chat + đồng bộ vị trí (vị trí unreliable ~20 Hz, chat
reliable; server chuyển tiếp cho các client khác).

```sh
deno run --allow-ffi --allow-env --allow-read --allow-write examples/chat.ts server --key server.key
deno run --allow-ffi --allow-env --allow-read examples/chat.ts client pvt_... --seconds 10
```

Test: `deno test --allow-ffi --allow-env --allow-read --allow-write test.ts`.

### Khắc phục sự cố

- Chạy `peerova doctor` (công cụ dòng lệnh trong SDK) để chẩn đoán mạng.
- Lỗi `dlopen` / không tìm thấy thư viện: đặt thư viện cạnh `mod.ts` hoặc đặt `PEEROVA_LIB`; nhớ `--allow-env`.
- `NotCapable` / `PermissionDenied`: thiếu cờ `--allow-ffi` / `--allow-read` / `--allow-env`.
- `ConnectFailed` sau khoảng 20 giây: kiểm tra ticket/mã, server còn chạy, `token` khớp; chạy `peerova doctor`.
- Client thứ 4 không vào được: server ở chế độ dev (không có `license`), tối đa 3 client.
- `send(..., { reliable: false })` ném lỗi: payload quá 1180 byte.

Khái niệm và toàn bộ C API: [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md). Website: https://peerova.pages.dev

---

## English

Deno binding for Peerova: direct peer-to-peer connections, end-to-end encrypted. Calls the native library in-process
through `Deno.dlopen` (FFI); a single file, `mod.ts`, with no dependencies.

### Requirements

- Deno 1.38 or newer (or Deno 2).
- Permissions: `--allow-ffi`, plus `--allow-env --allow-read` for the library lookup (and `--allow-write` on the
  server when `key_file` does not exist yet).
- The Peerova native library for your operating system.

### Where to put the native library

SDK package layout: `include/peerova.h`, `lib/linux-x86_64/libpeerova.so` (+ `libpeerova.a` and the `peerova`
binary), `lib/windows-x86_64/peerova.dll` (+ `peerova.exe`), `bindings/deno/`.

Lookup order: the `PEEROVA_LIB` environment variable (full path) → next to `mod.ts` → the SDK's `lib/<platform>`
folder → the system's default search path.

| OS | File | Where |
|---|---|---|
| Windows | `peerova.dll` | next to `mod.ts`, or `PEEROVA_LIB`. `wintun.dll` is not needed (it is only used by the `peerova` command-line tool's VPN mode). With `deno compile`: put `peerova.dll` next to the `.exe` and set `PEEROVA_LIB`. |
| Linux | `libpeerova.so` | next to `mod.ts`, a folder on `LD_LIBRARY_PATH`, or `PEEROVA_LIB` |
| macOS | `libpeerova.dylib` | contact Peerova for this build; once you have it, put it next to `mod.ts` or use `PEEROVA_LIB` |

### Install

1. Copy `bindings/deno/mod.ts` into your project.
2. Copy the native library next to it, or set `PEEROVA_LIB`.
3. Check: `deno eval --allow-ffi --allow-env --allow-read 'import { version } from "./mod.ts"; console.log(version())'`.

### 5-minute quick start

Server:

```ts
import { EventType, PeerovaServer } from "./mod.ts";

using srv = await PeerovaServer.startAsync({ port: 47000, key_file: "server.key" }); // resolves when the ticket is ready
console.log("ticket:", srv.ticket);                                                   // give it to clients
for (;;) {
  const ev = await srv.pollAsync(1000);
  if (ev?.type === EventType.Message) srv.send(ev.peer, `echo: ${ev.text}`);
}
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
import { EventType, PeerovaClient } from "./mod.ts";

using cli = PeerovaClient.connect("pvt_...");   // or PeerovaClient.connect("K7Q2XD")
const ev = await cli.pollAsync(20_000);
if (ev?.type === EventType.Connected) cli.send(ev.peer, "hello");
else if (ev?.type === EventType.ConnectFailed) console.error("failed:", ev.text);
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

`poll`/`pollAsync` return a `PeerovaEvent` (or `null`): `type`, `peer`, `channel`, `reliable`, `data`
(`Uint8Array`, already copied, safe to keep), `text`.

| `type` | Meaning |
|---|---|
| `EventType.Connected` | a peer connected (server: a new client; client: joined the server) |
| `EventType.Message` | a message arrived |
| `EventType.Disconnected` | a peer left; `text` = reason |
| `EventType.ConnectFailed` | (client) the connection attempt failed; `text` = reason |

Ways to receive:

- `await node.pollAsync(ms)`: waits on a native thread while the event loop keeps running. **Only one** `pollAsync`
  may be pending per node; a second call throws.
- `for await (const ev of node.events(idleMs))`: loops until `idleMs` passes without an event.
- `node.poll()` (timeout 0): non-blocking, for a game-loop style `setInterval`. `poll(ms > 0)` blocks the JS thread.

`send`, `disconnect` and `peerCount` can be called at any time, even while a `pollAsync` is pending.

Stoppable loop pattern:

```ts
let running = true;
const loop = (async () => {
  while (running) {
    const ev = await cli.pollAsync(100);   // short timeout so the running flag is noticed quickly
    if (ev) handle(ev);
  }
})();
// ... when you want to stop:
running = false;
await loop;        // guarantees no pollAsync is pending
cli.close();
```

### Disconnect and cleanup

- `node.disconnect(peer)`: close the connection to one peer.
- `node.close()`: disconnect everyone and free the node; calling it twice is safe. Or use `using node = ...`.
- Do **not** call `close()` while a `pollAsync` is pending (it throws `PeerovaError`): stop the loop and `await` it
  first.

### Error handling

All errors throw `PeerovaError`; the message comes from the native library.

- `PeerovaServer.start()`: port cannot be opened, invalid config, invalid license. `startAsync()` has no error detail
  (the error text is kept per thread) — call `start()` to see it.
- `PeerovaClient.connect()`: malformed ticket/code. Network failures arrive as a `ConnectFailed` event.
- `send()`: channel outside 0..255, unreliable payload over 1180 bytes, peer gone, node closed.
- Library not found at import time: the error from `Deno.dlopen`.

### Config options

Server (`PeerovaServer.start(config)` / `startAsync(config)`):

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

[`examples/chat.ts`](examples/chat.ts): chat + position sync (positions unreliable at ~20 Hz, chat
reliable; the server forwards to the other clients).

```sh
deno run --allow-ffi --allow-env --allow-read --allow-write examples/chat.ts server --key server.key
deno run --allow-ffi --allow-env --allow-read examples/chat.ts client pvt_... --seconds 10
```

Test: `deno test --allow-ffi --allow-env --allow-read --allow-write test.ts`.

### Troubleshooting

- Run `peerova doctor` (the command-line tool in the SDK) to diagnose the network.
- `dlopen` error / library not found: put the library next to `mod.ts` or set `PEEROVA_LIB`; remember `--allow-env`.
- `NotCapable` / `PermissionDenied`: missing `--allow-ffi` / `--allow-read` / `--allow-env`.
- `ConnectFailed` after about 20 seconds: check the ticket/code, that the server is running, and that `token`
  matches; run `peerova doctor`.
- The 4th client cannot join: the server runs in dev mode (no `license`), at most 3 clients.
- `send(..., { reliable: false })` throws: the payload is over 1180 bytes.

Concepts and the full C API: [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md). Website: https://peerova.pages.dev
