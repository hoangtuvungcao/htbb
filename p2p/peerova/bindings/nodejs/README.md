# Peerova cho Node.js / Electron / Bun

[Tiếng Việt](#tiếng-việt) · [English](#english)

---

## Tiếng Việt

Kết nối peer-to-peer trực tiếp, mã hoá đầu-cuối, **không cần native module** (không node-gyp). Mỗi node chạy một
tiến trình phụ (sidecar) `peerova stdio` — chính là công cụ dòng lệnh `peerova` — và trao đổi JSON qua
stdin/stdout ([giao thức](../stdio/PROTOCOL.md)). Gói ESM, kèm `index.d.ts`, API dạng `EventEmitter`.

### Chọn cách tích hợp

Gói này **chính là** sidecar `peerova stdio` bọc trong API `EventEmitter`: bạn gửi/nhận **message** (reliable/unreliable),
nó **không mở cổng local** nào. Xem bảng [hướng dẫn §1](../DEVELOPER-GUIDE.md#1-chọn-cách-tích-hợp):

| Tình huống | Dùng |
|---|---|
| Code mới, hoặc sẵn sàng viết lớp mạng bằng message; host trong game (cách 3/4) | **gói này** (`Peerova.server` / `Peerova.client`) |
| Code socket có sẵn (`net.connect`, `dgram`, `ws`, thư viện client HTTP/DB…) muốn giữ nguyên (cách 2) | chạy `peerova client --forward` làm process con rồi kết nối `127.0.0.1` — [Game có sẵn: chạy peerova ẩn](#game-có-sẵn-chạy-peerova-ẩn) |

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

Kết nối là tự động: thử kết nối trực tiếp trước; nếu mạng chặn, Peerova tự dùng **đường mã hoá tự động** (vẫn mã hoá
đầu-cuối) rồi chuyển sang trực tiếp khi có thể.

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
| `disconnected` | `(peer, reason, code)` | peer rời đi; `code` ví dụ `"E_CLOSED"` |
| `connectFailed` | `(reason, code)` | (client) kết nối thất bại, ví dụ hết `timeout_ms` (`code` = `"E_TIMEOUT"`) |
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
  → đặt `PEEROVA_BIN`), cổng đã bị dùng, config sai, ticket sai định dạng (`e.code` lấy từ event `error` của sidecar, ví dụ `"E_INVALID_TICKET"`). `client("")` ném ngay lập tức.
- Lỗi mạng của client đến dưới dạng sự kiện `connectFailed`, không phải exception.
- `PeerovaError.code` là tên mã lỗi ổn định (`"E_TOO_LARGE"`, `"E_UNKNOWN_PEER"`, `"E_INVALID_TICKET"`…, giá trị số trong `ErrorCode`; bảng: [Mã lỗi](../DEVELOPER-GUIDE.md#mã-lỗi)); `message` và `reason` là tiếng Anh `"E_NAME: ..."`.
- `send()` ném `PeerovaError` khi channel ngoài 0..255, tin unreliable quá 1180 byte, hoặc node đã đóng.
  Lỗi do SDK phát hiện sau đó (peer đã rời) đến qua sự kiện `error`.

```js
import { Peerova, PeerovaError, ErrorCode } from "peerova";

try {
  srv = await Peerova.server({ port: 47000, key_file: "data/server.key" });
} catch (e) {
  if (e instanceof PeerovaError && e.code === "E_IO") console.error("thư mục key_file chưa có / không ghi được");
  else throw e;
}
cli.on("connectFailed", (reason, code) => {
  if (code === "E_ACCESS_DENIED") askForNewToken();
  else if (code === "E_TIMEOUT") showRetry();          // gợi ý chạy `peerova doctor`
  console.log(code, ErrorCode[code], reason);          // "E_TIMEOUT" 7 "E_TIMEOUT: could not connect: ..."
});
```

Xử lý theo `code`, đừng phân tích văn bản.

### Tuỳ chọn cấu hình

Server (`Peerova.server(config, options)`):

| Khoá | Mặc định | Ý nghĩa |
|---|---|---|
| `port` | `47000` | cổng lắng nghe; `0` = cổng ngẫu nhiên |
| `key_file` | `"server.key"` | file danh tính server. Cùng key ⇒ cùng ticket |
| `license` | — | key `PVA-...`. Không có: chế độ dev, tối đa 3 client |
| `max_ccu` | theo license | số client đồng thời tối đa (không vượt license) |
| `token` | — | bí mật chung mà client phải gửi |
| `upnp` | `true` | thử mở cổng trên router |
| `private` | `false` | ticket không chứa địa chỉ IP của server (khi kết nối trực tiếp người chơi vẫn biết IP — muốn giấu thật dùng `hide_ip`) |
| `hide_ip` | `false` | Ẩn IP máy chủ: người chơi không bao giờ biết IP thật của máy chủ (chống DDoS); ping cao hơn một chút. Gói Pro/Studio/Enterprise và bản dùng thử |
| `app` | — | mã project trên website Peerova (để có mã kết nối) |
| `share` | — | `{ name, public, max, mode, region }`: công bố mã kết nối 6 ký tự |

Client (`Peerova.client(ticketOrCode, config, options)`):

| Khoá | Mặc định | Ý nghĩa |
|---|---|---|
| `token` | — | phải trùng `token` của server |
| `timeout_ms` | `20000` | quá thời gian này mà chưa kết nối được → `connectFailed` |
| `key_file` | — (mỗi lần chạy một khoá mới) | file danh tính client (tạo nếu chưa có, thư mục phải tồn tại), ví dụ `<appdata>/<game>/peerova-client.key` (Electron: `path.join(app.getPath("userData"), "peerova-client.key")`): giữ file thì người chơi giữ cùng danh tính và cùng địa chỉ ổn định trên server; chặn theo địa chỉ chỉ có tác dụng khi họ còn giữ file — muốn chắc chắn dùng tài khoản/token |

`options` (cả hai): `binary` (đường dẫn tới `peerova`), `stderr` (`"ignore"` mặc định, `"inherit"` để xem log của
sidecar khi gỡ lỗi).

### Electron

Chỉ dùng trong main process (hoặc `utilityProcess`), không dùng trong renderer; chuyển sự kiện sang renderer bằng IPC.
Binary không chạy được từ bên trong `app.asar`: giải nén nó ra (electron-builder:
`"asarUnpack": ["**/node_modules/peerova/bin/**"]`) hoặc đặt vào `extraResources` rồi truyền
`{ binary: path.join(process.resourcesPath, "peerova") }`.

Ví dụ chạy được: [`examples/electron/`](examples/electron) — Peerova trong main process, sự kiện chuyển sang renderer qua
preload sandbox (`contextIsolation`), round-trip server + client (100 reliable đúng thứ tự, unreliable hai chiều, trả lời
nhị phân, disconnect). Electron không phải dependency của gói — cài trong app của bạn (`npm i -D electron`):

```sh
npx electron examples/electron                  # mở cửa sổ, hiện từng bước
npx electron examples/electron --selftest       # cửa sổ ẩn, thoát mã 0 (PASS) / 1 (FAIL)
# Linux không có màn hình: thêm --ozone-platform=headless --disable-gpu (hoặc chạy dưới xvfb-run)
```

Đã chạy PASS với Electron 45.2.1 trên Linux x86_64 (`--selftest --ozone-platform=headless`); `test/test.mjs` cũng PASS
dưới `ELECTRON_RUN_AS_NODE=1`. Lưu ý: nếu môi trường đã đặt sẵn `ELECTRON_RUN_AS_NODE=1` (ví dụ terminal của VS Code),
Electron chạy như Node và báo `does not provide an export named 'BrowserWindow'` — bỏ biến đó (`env -u ELECTRON_RUN_AS_NODE`).

### Bun

Gói chỉ dùng `node:child_process`, `node:readline` và `Buffer`, những thứ Bun đã hỗ trợ: `bun test/test.mjs` PASS
(Bun 1.4.4). Muốn gọi thư viện native trực tiếp trong tiến trình (độ trễ thấp hơn, đọc được mã kết nối) trên Bun,
xem [`../bun`](../bun).

### Người chơi không phải gõ gì

Người chơi không cần gõ ticket hay mã: nhúng ticket vào game, hoặc tải ticket mới nhất từ URL của bạn khi game khởi động
(dùng ticket nhúng nếu tải lỗi). Mã 6 ký tự chỉ dùng để mời bạn bè vào phòng do người chơi mở. Chi tiết:
[hướng dẫn §2](../DEVELOPER-GUIDE.md#2-người-chơi-không-phải-gõ-gì).

```js
let ticket = EMBEDDED_TICKET;                              // ticket nhúng trong bản build
try {
  const r = await fetch("https://mygame.example.com/peerova-ticket.txt", { signal: AbortSignal.timeout(5000) });
  if (r.ok) ticket = (await r.text()).trim();
} catch {}
const cli = await Peerova.client(ticket, { key_file: keyPath });
```

### Game có sẵn: chạy peerova ẩn

Khi code socket đã có và bạn không muốn đổi sang message: không dùng gói này, mà chạy công cụ dòng lệnh
`peerova client <TICKET> --forward tcp:8888 --key <appdata>/<game>/peerova-client.key` **ẩn** (`windowsHide`), chờ dòng chứa
chuỗi ASCII `server port` (`✔ Tcp 127.0.0.1:8888 → server port 8888`), rồi code socket cũ kết nối `127.0.0.1:8888`.
Process thoát trước (mã `1`, stderr bắt đầu bằng `lỗi:`) hoặc quá 30 giây = thất bại. Máy chủ chạy
`peerova server --expose tcp:8888`. Cùng binary `peerova` như gói này; không cần thêm gì.

```js
// peerova-tunnel.mjs — Node.js 18+ / Electron main process.
import { spawn } from "node:child_process";
import { createInterface } from "node:readline";
import { mkdirSync } from "node:fs";
import path from "node:path";

/** Runs `peerova client` hidden. `ready` resolves once the forwarded port is open on 127.0.0.1. */
export function startTunnel({ exe, ticket, forward, keyFile, timeoutMs = 30000, onLog = () => {} }) {
  mkdirSync(path.dirname(keyFile), { recursive: true });
  const child = spawn(exe, ["client", ticket, "--forward", forward, "--key", keyFile], {
    windowsHide: true,                       // no console window on Windows
    stdio: ["ignore", "pipe", "pipe"],
  });
  const stop = () => { if (child.exitCode === null && child.signalCode === null) child.kill(); };
  process.once("exit", stop);

  const ready = new Promise((resolve, reject) => {
    const timer = setTimeout(() => { stop(); reject(new Error("peerova: timed out")); }, timeoutMs);
    for (const stream of [child.stdout, child.stderr]) {
      createInterface({ input: stream }).on("line", (line) => {
        onLog(line);
        // "✔ Tcp 127.0.0.1:8888 → server port 8888" = connected and the local port is open
        if (line.includes("server port")) { clearTimeout(timer); resolve(); }
      });
    }
    child.once("error", (e) => { clearTimeout(timer); reject(e); });
    child.once("exit", (code) => { clearTimeout(timer); reject(new Error(`peerova exited (${code})`)); });
  });
  return { child, ready, stop };
}
```
```js
// Electron main process. Ship the CLI with electron-builder "extraResources": [{ "from": "bin", "to": "bin" }]
// (it must not be inside app.asar).
import { app } from "electron";
import path from "node:path";
import { startTunnel } from "./peerova-tunnel.mjs";

const exe = path.join(app.isPackaged ? process.resourcesPath : app.getAppPath(), "bin",
  process.platform === "win32" ? "peerova.exe" : "peerova");
const tunnel = startTunnel({
  exe, ticket: "pvt_...", forward: "tcp:8888",
  keyFile: path.join(app.getPath("userData"), "peerova-client.key"),
  onLog: (l) => console.log("[peerova]", l),
});
app.on("before-quit", tunnel.stop);
tunnel.child.on("exit", () => { /* tunnel closed: show "disconnected", offer to reconnect */ });

await tunnel.ready;             // throws on failure / timeout
connectToGameServer("127.0.0.1", 8888);   // your existing socket code, unchanged
```

- Thư mục của `--key` được tạo sẵn; giữ file này thì người chơi giữ cùng danh tính (cấm người chơi theo địa chỉ hoạt động đúng).
- `peerova client` thoát khi mất kết nối (trừ khi thêm `--reconnect`) — sự kiện `exit` của `tunnel.child` báo điều đó.
  `--token SECRET` nếu máy chủ yêu cầu token.
- Linux/macOS: `chmod +x peerova`. Electron: binary phải nằm ngoài `app.asar`.
- Mẫu cho các ngôn ngữ khác: [hướng dẫn §4](../DEVELOPER-GUIDE.md#4-game-tự-chạy-peerova-ẩn-người-chơi-không-chạy-gì).

### Máy chủ game

Nhiều cổng trong một process, tách Remote Desktop cho admin, mỗi người chơi một địa chỉ `127.a.b.c` + `--banned`,
`hide_ip`, chế độ VPN: xem [hướng dẫn §7](../DEVELOPER-GUIDE.md#7-tính-năng-máy-chủ-game).

### Bảo mật

- `server.key` là danh tính của server: ai có file này có thể giả mạo server. Giữ bí mật, **không** commit.
- Ticket không phải bí mật (giống địa chỉ máy chủ): cứ nhúng vào game hoặc chia sẻ cho client.
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
- `E_IO` khi khởi động: thư mục của `key_file` không tồn tại / không ghi được.
- Code socket cũ không kết nối được `127.0.0.1:8888`: gói này không mở cổng — dùng `peerova client --forward` (ở trên).

### FAQ

- **Người chơi có phải gõ mã không?** Không. Nhúng ticket hoặc tải từ URL của bạn; mã 6 ký tự chỉ để mời bạn bè.
- **Có cần mở port / IP tĩnh không?** Không. Kết nối là tự động; mở port chỉ giúp nhanh hơn.
- **Gói này hay `peerova client --forward`?** Gói này cho code mới dùng message; `--forward` cho code socket có sẵn.
- **Độ trễ?** Sidecar thêm vài ms + base64 mỗi gói; game cần độ trễ thấp nhất trên Bun có thể dùng [`../bun`](../bun) (gọi native trực tiếp).
- **Ticket có phải bí mật không?** Không; `server.key`, `license` và `token` mới là bí mật.

Khái niệm và toàn bộ C API: [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md). Website: https://peerova.pages.dev

---

## English

Direct peer-to-peer networking, end-to-end encrypted, with **no native module** (no node-gyp). Each node runs a
`peerova stdio` sidecar process — the `peerova` command-line tool — and exchanges JSON over stdin/stdout
([protocol](../stdio/PROTOCOL.md)). ESM package with `index.d.ts`, `EventEmitter` API.

### Choosing an integration

This package **is** the `peerova stdio` sidecar wrapped in an `EventEmitter` API: you send/receive **messages**
(reliable/unreliable); it does **not open any local port**. See the table in [guide §1](../DEVELOPER-GUIDE.md#1-choosing-an-integration):

| Situation | Use |
|---|---|
| New code, or you're willing to write the networking layer with messages; host-in-game (options 3/4) | **this package** (`Peerova.server` / `Peerova.client`) |
| Existing socket code (`net.connect`, `dgram`, `ws`, HTTP/DB client libraries…) you want to keep (option 2) | run `peerova client --forward` as a child process and connect to `127.0.0.1` — [Existing game: run peerova hidden](#existing-game-run-peerova-hidden) |

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

Connections are automatic: direct first; when a network blocks direct connections Peerova uses the **automatic encrypted
route** (still end-to-end encrypted) and upgrades to direct when it can.

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
| `disconnected` | `(peer, reason, code)` | a peer left; `code` e.g. `"E_CLOSED"` |
| `connectFailed` | `(reason, code)` | (client) the connection attempt failed, e.g. `timeout_ms` elapsed (`code` = `"E_TIMEOUT"`) |
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
  set `PEEROVA_BIN`), the port is in use, the config is invalid, or the ticket is malformed (`e.code` comes from the sidecar's `error` event,
  e.g. `"E_INVALID_TICKET"`). `client("")` throws immediately.
- Client network failures arrive as the `connectFailed` event, not as exceptions.
- `PeerovaError.code` is the stable error code name (`"E_TOO_LARGE"`, `"E_UNKNOWN_PEER"`, `"E_INVALID_TICKET"`, ...; numeric values in `ErrorCode`; table: [Error codes](../DEVELOPER-GUIDE.md#error-codes)); `message` and `reason` are English `"E_NAME: ..."`.
- `send()` throws `PeerovaError` for a channel outside 0..255, an unreliable payload over 1180 bytes, or a closed node.
  Errors the SDK detects later (peer has left) arrive as `error` events.

```js
import { Peerova, PeerovaError, ErrorCode } from "peerova";

try {
  srv = await Peerova.server({ port: 47000, key_file: "data/server.key" });
} catch (e) {
  if (e instanceof PeerovaError && e.code === "E_IO") console.error("key_file folder missing / not writable");
  else throw e;
}
cli.on("connectFailed", (reason, code) => {
  if (code === "E_ACCESS_DENIED") askForNewToken();
  else if (code === "E_TIMEOUT") showRetry();          // suggest running `peerova doctor`
  console.log(code, ErrorCode[code], reason);          // "E_TIMEOUT" 7 "E_TIMEOUT: could not connect: ..."
});
```

Branch on `code`; don't parse the text.

### Config options

Server (`Peerova.server(config, options)`):

| Key | Default | Meaning |
|---|---|---|
| `port` | `47000` | listening port; `0` = random port |
| `key_file` | `"server.key"` | server identity file. Same key ⇒ same ticket |
| `license` | — | `PVA-...` key. Without it: dev mode, at most 3 clients |
| `max_ccu` | from license | maximum concurrent clients (never above the license) |
| `token` | — | shared secret clients must present |
| `upnp` | `true` | try to open the port on the router |
| `private` | `false` | the ticket does not contain the server's IP addresses (on a direct connection players still learn the IP — to really hide it use `hide_ip`) |
| `hide_ip` | `false` | Hide the server IP: players never learn its real address (DDoS protection); slightly higher latency. Pro/Studio/Enterprise plans and the trial |
| `app` | — | project id from the Peerova website (needed for connect codes) |
| `share` | — | `{ name, public, max, mode, region }`: publish a 6-character connect code |

Client (`Peerova.client(ticketOrCode, config, options)`):

| Key | Default | Meaning |
|---|---|---|
| `token` | — | must match the server's `token` |
| `timeout_ms` | `20000` | not connected within this time → `connectFailed` |
| `key_file` | — (new random key per run) | client identity file (created if missing; the folder must exist), e.g. `<appdata>/<game>/peerova-client.key` (Electron: `path.join(app.getPath("userData"), "peerova-client.key")`): keeping it keeps the player's identity and stable address on the server; banning by address works per player only while they keep it — accounts/tokens are the robust way |

`options` (both): `binary` (path to `peerova`), `stderr` (`"ignore"` by default, `"inherit"` to see the sidecar's log
while debugging).

### Electron

Use it in the main process (or a `utilityProcess`) only, not in the renderer; forward events to the renderer with IPC.
The binary cannot run from inside `app.asar`: unpack it (electron-builder:
`"asarUnpack": ["**/node_modules/peerova/bin/**"]`) or put it in `extraResources` and pass
`{ binary: path.join(process.resourcesPath, "peerova") }`.

Runnable example: [`examples/electron/`](examples/electron) — Peerova in the main process, events forwarded to the
renderer through a sandboxed preload (`contextIsolation`), server + client round-trip (100 reliable in order, unreliable
both ways, binary reply, disconnect). Electron is not a dependency of this package — install it in your app
(`npm i -D electron`):

```sh
npx electron examples/electron                  # opens a window showing each step
npx electron examples/electron --selftest       # hidden window, exits with code 0 (PASS) / 1 (FAIL)
# Linux without a display: add --ozone-platform=headless --disable-gpu (or run under xvfb-run)
```

Passes with Electron 45.2.1 on Linux x86_64 (`--selftest --ozone-platform=headless`); `test/test.mjs` also passes under
`ELECTRON_RUN_AS_NODE=1`. Note: if your environment already sets `ELECTRON_RUN_AS_NODE=1` (e.g. VS Code's terminal),
Electron runs as plain Node and fails with `does not provide an export named 'BrowserWindow'` — unset it
(`env -u ELECTRON_RUN_AS_NODE`).

### Bun

The package only uses `node:child_process`, `node:readline` and `Buffer`, which Bun implements: `bun test/test.mjs`
passes (Bun 1.4.4). For in-process native calls on Bun (lower latency, can read the connect code), see [`../bun`](../bun).

### Players never type anything

Players never type a ticket or a code: embed the ticket in the game, or fetch the latest ticket from your own URL at startup
(falling back to the embedded one). 6-character codes are only for inviting friends to player-hosted rooms. Details:
[guide §2](../DEVELOPER-GUIDE.md#2-players-never-type-anything).

```js
let ticket = EMBEDDED_TICKET;                              // ticket embedded in the build
try {
  const r = await fetch("https://mygame.example.com/peerova-ticket.txt", { signal: AbortSignal.timeout(5000) });
  if (r.ok) ticket = (await r.text()).trim();
} catch {}
const cli = await Peerova.client(ticket, { key_file: keyPath });
```

### Existing game: run peerova hidden

When the socket code already exists and you don't want to switch to messages: don't use this package — run the command-line
tool `peerova client <TICKET> --forward tcp:8888 --key <appdata>/<game>/peerova-client.key` **hidden** (`windowsHide`), wait for a
line containing the ASCII text `server port` (`✔ Tcp 127.0.0.1:8888 → server port 8888`), then let the existing socket code
connect to `127.0.0.1:8888`. The process exiting first (status `1`, stderr line starting with `lỗi:` = "error:") or 30 s passing
= failure. The server runs `peerova server --expose tcp:8888`. It's the same `peerova` binary this package uses; nothing else is needed.

```js
// peerova-tunnel.mjs — Node.js 18+ / Electron main process.
import { spawn } from "node:child_process";
import { createInterface } from "node:readline";
import { mkdirSync } from "node:fs";
import path from "node:path";

/** Runs `peerova client` hidden. `ready` resolves once the forwarded port is open on 127.0.0.1. */
export function startTunnel({ exe, ticket, forward, keyFile, timeoutMs = 30000, onLog = () => {} }) {
  mkdirSync(path.dirname(keyFile), { recursive: true });
  const child = spawn(exe, ["client", ticket, "--forward", forward, "--key", keyFile], {
    windowsHide: true,                       // no console window on Windows
    stdio: ["ignore", "pipe", "pipe"],
  });
  const stop = () => { if (child.exitCode === null && child.signalCode === null) child.kill(); };
  process.once("exit", stop);

  const ready = new Promise((resolve, reject) => {
    const timer = setTimeout(() => { stop(); reject(new Error("peerova: timed out")); }, timeoutMs);
    for (const stream of [child.stdout, child.stderr]) {
      createInterface({ input: stream }).on("line", (line) => {
        onLog(line);
        // "✔ Tcp 127.0.0.1:8888 → server port 8888" = connected and the local port is open
        if (line.includes("server port")) { clearTimeout(timer); resolve(); }
      });
    }
    child.once("error", (e) => { clearTimeout(timer); reject(e); });
    child.once("exit", (code) => { clearTimeout(timer); reject(new Error(`peerova exited (${code})`)); });
  });
  return { child, ready, stop };
}
```
```js
// Electron main process. Ship the CLI with electron-builder "extraResources": [{ "from": "bin", "to": "bin" }]
// (it must not be inside app.asar).
import { app } from "electron";
import path from "node:path";
import { startTunnel } from "./peerova-tunnel.mjs";

const exe = path.join(app.isPackaged ? process.resourcesPath : app.getAppPath(), "bin",
  process.platform === "win32" ? "peerova.exe" : "peerova");
const tunnel = startTunnel({
  exe, ticket: "pvt_...", forward: "tcp:8888",
  keyFile: path.join(app.getPath("userData"), "peerova-client.key"),
  onLog: (l) => console.log("[peerova]", l),
});
app.on("before-quit", tunnel.stop);
tunnel.child.on("exit", () => { /* tunnel closed: show "disconnected", offer to reconnect */ });

await tunnel.ready;             // throws on failure / timeout
connectToGameServer("127.0.0.1", 8888);   // your existing socket code, unchanged
```

- The `--key` folder is created for you; keeping that file keeps the player's identity (address bans work).
- `peerova client` exits when the connection is lost (unless you add `--reconnect`) — the `exit` event of `tunnel.child` tells you.
  Add `--token SECRET` when the server requires one.
- Linux/macOS: `chmod +x peerova`. Electron: the binary must live outside `app.asar`.
- Snippets for other languages: [guide §4](../DEVELOPER-GUIDE.md#4-the-game-runs-peerova-hidden-players-run-nothing).

### Game servers

Several ports from one process, a separate Remote Desktop process for admins, a `127.a.b.c` address per player + `--banned`,
`hide_ip`, VPN mode: see [guide §7](../DEVELOPER-GUIDE.md#7-game-server-features).

### Security

- `server.key` is the server's identity: anyone who has it can impersonate your server. Keep it secret and do **not**
  commit it.
- The ticket is not a secret (like a server address): embed it in the game or share it with clients.
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
- `E_IO` at startup: the `key_file` folder doesn't exist or isn't writable.
- Existing socket code can't connect to `127.0.0.1:8888`: this package opens no port — use `peerova client --forward` (above).

### FAQ

- **Do players need to type a code?** No. Embed the ticket or fetch it from your URL; 6-character codes are only for inviting friends.
- **Do I need port forwarding / a static IP?** No. Connections are automatic; an open port only makes them faster.
- **This package or `peerova client --forward`?** This package for new code using messages; `--forward` for existing socket code.
- **Latency?** The sidecar adds a few ms + base64 per packet; for the lowest latency on Bun see [`../bun`](../bun) (direct native calls).
- **Is the ticket a secret?** No; `server.key`, `license` and `token` are.

Concepts and the full C API: [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md). Website: https://peerova.pages.dev
