# Peerova cho Node.js / Electron / Bun

[Tiếng Việt](#tiếng-việt) · [English](#english)

---

## Tiếng Việt

Kết nối peer-to-peer trực tiếp, mã hoá đầu-cuối, **không cần native module** (không node-gyp). Mỗi node chạy một
tiến trình phụ (sidecar) `peerova stdio` — chính là công cụ dòng lệnh `peerova` — và trao đổi JSON qua
stdin/stdout ([giao thức](../stdio/PROTOCOL.md)). Gói ESM, kèm `index.d.ts`, API dạng `EventEmitter`.

### Yêu cầu

- Node.js 18 trở lên, Electron (main process hoặc `utilityProcess`), hoặc Bun.
- Binary `peerova` (Linux) / `peerova.exe` (Windows) cho hệ điều hành đích.

### Đặt binary ở đâu

Trong gói SDK: `include/peerova.h`, `lib/linux-x86_64/libpeerova.so` (+ `libpeerova.a` và binary **`peerova`**),
`lib/windows-x86_64/peerova.dll` (+ **`peerova.exe`**), `bindings/nodejs/`. Gói Node chỉ cần binary `peerova`,
không cần `libpeerova.so` / `peerova.dll`.

Thứ tự tìm binary: `options.binary` → biến môi trường `PEEROVA_BIN` → `bin/peerova[.exe]` bên trong gói →
cạnh `index.js` → thư mục `lib/<nền tảng>` của SDK → `peerova` trên `PATH`.

| Hệ điều hành | File | Đặt ở đâu |
|---|---|---|
| Windows | `peerova.exe` | `bin/` trong gói, hoặc `PEEROVA_BIN`. Không cần `wintun.dll` (file đó chỉ dùng cho chế độ VPN của công cụ dòng lệnh). |
| Linux | `peerova` (nhớ `chmod +x`) | `bin/` trong gói, hoặc `PEEROVA_BIN`, hoặc thư mục trên `PATH` |
| macOS | `peerova` | liên hệ Peerova để nhận bản build này; khi có, đặt vào `bin/` hoặc dùng `PEEROVA_BIN` |

### Cài đặt

1. Chép thư mục `bindings/nodejs` vào project (ví dụ `vendor/peerova`) và thêm vào `package.json`:
   `"dependencies": { "peerova": "file:vendor/peerova" }`, rồi `npm install`.
2. Chép binary `peerova` / `peerova.exe` vào `vendor/peerova/bin/` (hoặc đặt `PEEROVA_BIN`).
3. Kiểm tra: `node -e 'import("peerova").then(m => console.log(m.findBinary()))'`.

### Bắt đầu nhanh (5 phút)

Server:

```js
import { Peerova } from "peerova";

const srv = await Peerova.server({ port: 47000, key_file: "server.key" }); // xong khi có ticket (≤ ~3 s)
console.log("ticket:", srv.ticket);                                         // gửi chuỗi này cho client
srv.on("message", (peer, data, { channel }) => srv.send(peer, `echo: ${data}`, { channel }));
```

Client (truyền ticket **hoặc** mã kết nối 6 ký tự):

```js
import { Peerova } from "peerova";

const cli = await Peerova.client("pvt_...");          // hoặc Peerova.client("K7Q2XD")
cli.on("connected", (peer) => cli.send(peer, "hello"));
cli.on("message", (peer, data) => console.log(data.toString()));
cli.on("connectFailed", (reason) => console.error("lỗi:", reason));
```

**Mã kết nối:** sidecar không báo mã 6 ký tự của server, nên server chạy bằng gói này không đọc được mã. Nếu cần
mã, chạy server bằng một binding chạy trong tiến trình (ví dụ Python `room_code`, C `peerova_room_code`) hoặc bằng
công cụ dòng lệnh `peerova`. Phía client thì gói này dùng mã bình thường: `Peerova.client("K7Q2XD")`.

Kết nối là tự động: thử kết nối trực tiếp trước, và tự chuyển sang một đường dự phòng đã mã hoá khi mạng chặn
kết nối trực tiếp.

### Gửi: reliable hay unreliable

```js
node.send(peer, data, { channel: 0, reliable: true });
```

| | `reliable: true` (mặc định) | `reliable: false` |
|---|---|---|
| Đảm bảo | đến đủ, đúng thứ tự trong cùng channel | có thể mất hoặc đến sai thứ tự |
| Kích thước | lớn tuỳ ý | tối đa **1180 byte** (`MAX_UNRELIABLE_PAYLOAD`) |
| Dùng cho | chat, túi đồ, RPC, lệnh, từng khúc file | input gameplay, vị trí, trạng thái realtime kiểu giọng nói |

- `channel` từ 0 đến 255; các channel độc lập, channel này chậm không làm kẹt channel khác.
- `data` là `string` (gửi UTF-8), `Buffer` hoặc `Uint8Array`.
- `send` trả về `false` khi đường ống tới sidecar đang đầy: tin vẫn được xếp hàng, nhưng nên `await node.drain()`
  trước khi gửi tiếp nhiều dữ liệu (ví dụ khi gửi file).

### Nhận sự kiện

| Sự kiện | Tham số | Khi nào |
|---|---|---|
| `connected` | `(peer)` | server: client mới vào; client: đã vào server |
| `message` | `(peer, data: Buffer, { channel, reliable })` | có tin nhắn |
| `disconnected` | `(peer, reason)` | peer rời đi |
| `connectFailed` | `(reason)` | (client) kết nối thất bại, ví dụ hết `timeout_ms` |
| `error` | `(err: PeerovaError)` | lỗi không nghiêm trọng, ví dụ gửi tới peer đã rời. Không bị ném ra nếu không có listener. |
| `close` | `(code)` | sidecar đã thoát; node không dùng được nữa |
| `ready` | `(ticket?)` | sidecar đã sẵn sàng (trước khi promise của `server`/`client` resolve) |

Mọi listener chạy trên event loop của Node; không có luồng phụ, không có gì bị chặn. `node.peers` (`Set`) và
`node.peerCount` luôn được cập nhật theo sự kiện.

### Ngắt kết nối và dọn dẹp

- `node.disconnect(peer)`: đóng kết nối tới một peer.
- `await node.close()`: ngắt tất cả và kết thúc sidecar (bị kill nếu sau 2 s chưa thoát); gọi nhiều lần vẫn an toàn.
  Nếu môi trường hỗ trợ: `await using node = await Peerova.server(...)`.
- Khi app thoát, hãy `close()` mọi node (ví dụ trong handler `SIGINT` / `app.on("before-quit")` của Electron).

### Xử lý lỗi

- `Peerova.server()` / `Peerova.client()` reject với `PeerovaError` khi không chạy được sidecar (không thấy binary
  → đặt `PEEROVA_BIN`), cổng đã bị dùng, config sai, ticket sai định dạng. `client("")` ném ngay lập tức.
- Lỗi mạng của client đến dưới dạng sự kiện `connectFailed`, không phải exception.
- `send()` ném `PeerovaError` khi channel ngoài 0..255, tin unreliable quá 1180 byte, hoặc node đã đóng.
  Lỗi do SDK phát hiện sau đó (peer đã rời) đến qua sự kiện `error`.

### Tuỳ chọn cấu hình

Server (`Peerova.server(config, options)`):

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

Client (`Peerova.client(ticketOrCode, config, options)`): `token` (phải trùng server), `timeout_ms` (mặc định 20000).

`options` (cả hai): `binary` (đường dẫn tới `peerova`), `stderr` (`"ignore"` mặc định, `"inherit"` để xem log của
sidecar khi gỡ lỗi).

### Electron

Chỉ dùng trong main process (hoặc `utilityProcess`), không dùng trong renderer; chuyển sự kiện sang renderer bằng IPC.
Binary không chạy được từ bên trong `app.asar`: giải nén nó ra (electron-builder:
`"asarUnpack": ["**/node_modules/peerova/bin/**"]`) hoặc đặt vào `extraResources` rồi truyền
`{ binary: path.join(process.resourcesPath, "peerova") }`.

### Bun

Gói chỉ dùng `node:child_process`, `node:readline` và `Buffer`, những thứ Bun đã hỗ trợ. Muốn gọi thư viện native
trực tiếp trong tiến trình (độ trễ thấp hơn, đọc được mã kết nối) trên Bun, xem [`../bun`](../bun).
Chưa chạy thử gói này dưới Bun.

### Bảo mật

- `server.key` là danh tính của server: ai có file này có thể giả mạo server. Giữ bí mật, **không** commit.
- Ticket là thông tin bán công khai: cứ chia sẻ cho client.
- `token` và `license` là bí mật.
- Toàn bộ lưu lượng được mã hoá đầu-cuối.

### Ví dụ

[`examples/chat.mjs`](examples/chat.mjs): chat + đồng bộ vị trí (vị trí unreliable ~20 Hz, chat reliable; server
chuyển tiếp cho các client khác).

```sh
node examples/chat.mjs server --key server.key            # in ra: TICKET pvt_...
node examples/chat.mjs client pvt_...                     # gõ một dòng rồi Enter để chat
node examples/chat.mjs client pvt_... --seconds 10        # tự thoát sau 10 giây
```

Test: `node test/test.mjs`; kiểm tra kiểu: `tsc -p test/tsconfig.json` (cần `@types/node`).

### Khắc phục sự cố

- Chạy `peerova doctor` để chẩn đoán mạng.
- `cannot start peerova sidecar ... (set PEEROVA_BIN)`: không tìm thấy binary — đặt nó vào `bin/` hoặc đặt
  `PEEROVA_BIN`. Trên Linux kiểm tra quyền thực thi (`chmod +x`).
- `connectFailed` sau khoảng 20 giây: kiểm tra ticket/mã, server còn chạy, `token` khớp; chạy `peerova doctor` ở
  cả hai phía.
- Client thứ 4 không vào được: server ở chế độ dev (không có `license`), tối đa 3 client.
- `send(..., { reliable: false })` ném lỗi: payload quá 1180 byte.
- Muốn xem log của sidecar: truyền `{ stderr: "inherit" }` làm `options`.

Khái niệm và toàn bộ C API: [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md). Website: https://peerova.pages.dev

---

## English

Direct peer-to-peer networking, end-to-end encrypted, with **no native module** (no node-gyp). Each node runs a
`peerova stdio` sidecar process — the `peerova` command-line tool — and exchanges JSON over stdin/stdout
([protocol](../stdio/PROTOCOL.md)). ESM package with `index.d.ts`, `EventEmitter` API.

### Requirements

- Node.js 18 or newer, Electron (main process or `utilityProcess`), or Bun.
- The `peerova` (Linux) / `peerova.exe` (Windows) binary for the target OS.

### Where to put the binary

SDK package layout: `include/peerova.h`, `lib/linux-x86_64/libpeerova.so` (+ `libpeerova.a` and the **`peerova`**
binary), `lib/windows-x86_64/peerova.dll` (+ **`peerova.exe`**), `bindings/nodejs/`. The Node package only needs the
`peerova` binary, not `libpeerova.so` / `peerova.dll`.

Lookup order: `options.binary` → the `PEEROVA_BIN` environment variable → `bin/peerova[.exe]` inside the package →
next to `index.js` → the SDK's `lib/<platform>` folder → `peerova` on `PATH`.

| OS | File | Where |
|---|---|---|
| Windows | `peerova.exe` | `bin/` in the package, or `PEEROVA_BIN`. `wintun.dll` is not needed (it is only used by the command-line tool's VPN mode). |
| Linux | `peerova` (remember `chmod +x`) | `bin/` in the package, or `PEEROVA_BIN`, or a folder on `PATH` |
| macOS | `peerova` | contact Peerova for this build; once you have it, put it in `bin/` or use `PEEROVA_BIN` |

### Install

1. Copy `bindings/nodejs` into your project (e.g. `vendor/peerova`) and add it to `package.json`:
   `"dependencies": { "peerova": "file:vendor/peerova" }`, then `npm install`.
2. Copy the `peerova` / `peerova.exe` binary into `vendor/peerova/bin/` (or set `PEEROVA_BIN`).
3. Check: `node -e 'import("peerova").then(m => console.log(m.findBinary()))'`.

### 5-minute quick start

Server:

```js
import { Peerova } from "peerova";

const srv = await Peerova.server({ port: 47000, key_file: "server.key" }); // resolves when the ticket is ready (<= ~3 s)
console.log("ticket:", srv.ticket);                                         // give this string to clients
srv.on("message", (peer, data, { channel }) => srv.send(peer, `echo: ${data}`, { channel }));
```

Client (pass the ticket **or** a 6-character connect code):

```js
import { Peerova } from "peerova";

const cli = await Peerova.client("pvt_...");          // or Peerova.client("K7Q2XD")
cli.on("connected", (peer) => cli.send(peer, "hello"));
cli.on("message", (peer, data) => console.log(data.toString()));
cli.on("connectFailed", (reason) => console.error("failed:", reason));
```

**Connect codes:** the sidecar does not report the server's 6-character code, so a server run through this package
cannot read it. If you need a code, run the server with an in-process binding (e.g. Python `room_code`, C
`peerova_room_code`) or with the `peerova` command-line tool. On the client side this package accepts codes as usual:
`Peerova.client("K7Q2XD")`.

Connections are automatic: direct first, with an automatic encrypted fallback route when networks block direct
connections.

### Sending: reliable vs unreliable

```js
node.send(peer, data, { channel: 0, reliable: true });
```

| | `reliable: true` (default) | `reliable: false` |
|---|---|---|
| Guarantee | delivered, in order within the channel | may be lost or reordered |
| Size | any size | at most **1180 bytes** (`MAX_UNRELIABLE_PAYLOAD`) |
| Use for | chat, inventory, RPC, commands, file chunks | gameplay inputs, positions, voice-like realtime state |

- `channel` is 0..255; channels are independent, a slow channel never blocks another.
- `data` is a `string` (sent as UTF-8), `Buffer` or `Uint8Array`.
- `send` returns `false` when the pipe to the sidecar is full: the message is still queued, but `await node.drain()`
  before sending much more (e.g. during a file transfer).

### Receiving events

| Event | Arguments | When |
|---|---|---|
| `connected` | `(peer)` | server: a client joined; client: joined the server |
| `message` | `(peer, data: Buffer, { channel, reliable })` | a message arrived |
| `disconnected` | `(peer, reason)` | a peer left |
| `connectFailed` | `(reason)` | (client) the connection attempt failed, e.g. `timeout_ms` elapsed |
| `error` | `(err: PeerovaError)` | non-fatal error, e.g. sending to a peer that has left. Not thrown when unhandled. |
| `close` | `(code)` | the sidecar exited; the node is unusable afterwards |
| `ready` | `(ticket?)` | the sidecar is ready (before the `server`/`client` promise resolves) |

All listeners run on the Node event loop; there are no extra threads and nothing blocks. `node.peers` (a `Set`) and
`node.peerCount` are kept up to date from the events.

### Disconnect and cleanup

- `node.disconnect(peer)`: close the connection to one peer.
- `await node.close()`: disconnect everyone and end the sidecar (killed if it has not exited after 2 s); safe to call
  more than once. Where supported: `await using node = await Peerova.server(...)`.
- When your app exits, `close()` every node (e.g. in a `SIGINT` handler or Electron's `app.on("before-quit")`).

### Error handling

- `Peerova.server()` / `Peerova.client()` reject with `PeerovaError` when the sidecar cannot start (binary not found →
  set `PEEROVA_BIN`), the port is in use, the config is invalid, or the ticket is malformed. `client("")` throws
  immediately.
- Client network failures arrive as the `connectFailed` event, not as exceptions.
- `send()` throws `PeerovaError` for a channel outside 0..255, an unreliable payload over 1180 bytes, or a closed node.
  Errors the SDK detects later (peer has left) arrive as `error` events.

### Config options

Server (`Peerova.server(config, options)`):

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

Client (`Peerova.client(ticketOrCode, config, options)`): `token` (must match the server), `timeout_ms` (default 20000).

`options` (both): `binary` (path to `peerova`), `stderr` (`"ignore"` by default, `"inherit"` to see the sidecar's log
while debugging).

### Electron

Use it in the main process (or a `utilityProcess`) only, not in the renderer; forward events to the renderer with IPC.
The binary cannot run from inside `app.asar`: unpack it (electron-builder:
`"asarUnpack": ["**/node_modules/peerova/bin/**"]`) or put it in `extraResources` and pass
`{ binary: path.join(process.resourcesPath, "peerova") }`.

### Bun

The package only uses `node:child_process`, `node:readline` and `Buffer`, which Bun implements. For in-process native
calls on Bun (lower latency, can read the connect code), see [`../bun`](../bun). This package has not been run under
Bun yet.

### Security

- `server.key` is the server's identity: anyone who has it can impersonate your server. Keep it secret and do **not**
  commit it.
- The ticket is public-ish: share it with clients.
- `token` and `license` are secrets.
- All traffic is end-to-end encrypted.

### Example

[`examples/chat.mjs`](examples/chat.mjs): chat + position sync (positions unreliable at ~20 Hz, chat reliable; the
server forwards to the other clients).

```sh
node examples/chat.mjs server --key server.key            # prints: TICKET pvt_...
node examples/chat.mjs client pvt_...                     # type a line and press Enter to chat
node examples/chat.mjs client pvt_... --seconds 10        # quit automatically after 10 s
```

Test: `node test/test.mjs`; type check: `tsc -p test/tsconfig.json` (needs `@types/node`).

### Troubleshooting

- Run `peerova doctor` to diagnose the network.
- `cannot start peerova sidecar ... (set PEEROVA_BIN)`: the binary was not found — put it in `bin/` or set
  `PEEROVA_BIN`. On Linux check that it is executable (`chmod +x`).
- `connectFailed` after about 20 seconds: check the ticket/code, that the server is running, and that `token` matches;
  run `peerova doctor` on both sides.
- The 4th client cannot join: the server runs in dev mode (no `license`), at most 3 clients.
- `send(..., { reliable: false })` throws: the payload is over 1180 bytes.
- To see the sidecar's log, pass `{ stderr: "inherit" }` as `options`.

Concepts and the full C API: [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md). Website: https://peerova.pages.dev
