# Peerova cho Rust

- [Tiếng Việt](#tiếng-việt)
- [English](#english)

---

## Tiếng Việt

Dùng thẳng crate `peerova` (package `peerova-ffi`, kiểu `peerova::Sdk`) từ Rust: không qua C ABI, không `unsafe`,
không cần thư viện native riêng — engine được link tĩnh vào binary của bạn. Kết nối P2P trực tiếp, mã hoá đầu-cuối.

Khái niệm (server, client, ticket, connect code, channel, sự kiện): [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md).
Website: https://peerova.pages.dev

Thư mục này là một crate ví dụ độc lập:

```
rust/
├── Cargo.toml          phụ thuộc: peerova = { package = "peerova-ffi", ... }
├── src/main.rs         demo trong một process + chế độ server/client echo
├── examples/chat.rs    chat + đồng bộ vị trí 20 Hz
└── tests/roundtrip.rs  test round-trip (200 tin reliable đúng thứ tự + unreliable + reply nhị phân)
```

### Yêu cầu và cài đặt

- Rust stable, edition 2021.
- Thêm vào `Cargo.toml` (đường dẫn hoặc git do Peerova cung cấp):
  ```toml
  [dependencies]
  peerova = { package = "peerova-ffi", path = "<đường dẫn tới peerova-ffi>" }
  ```
- Binary dùng crate trực tiếp nên **không** cần đặt `libpeerova.so` / `peerova.dll` cạnh file thực thi.
  Windows: không cần `wintun.dll` (chỉ công cụ dòng lệnh `peerova` cần nó cho chế độ VPN).
- macOS / iOS / Android: liên hệ Peerova về target được hỗ trợ.

Chạy thử (trong `bindings/rust`):

```sh
cargo test --release -- --nocapture                      # "rust PASS: ..."
cargo run --release                                      # demo server + client trong một process
cargo run --release --example chat -- server 0 /tmp/demo.key
cargo run --release --example chat -- client <TICKET>
```

### API

| Hàm | Ý nghĩa |
|---|---|
| `Sdk::server(Some(json)) -> Result<Sdk, String>` | khởi động server; chặn tới khi có ticket (tối đa ~3 s) |
| `sdk.ticket() -> Option<&str>` | ticket `pvt_...` để chia sẻ cho client |
| `sdk.room_code() -> Option<String>` | connect code 6 ký tự (chỉ khi cấu hình `app` + `share`; `None` tới khi website cấp mã) |
| `Sdk::client(ticket_or_code, Some(json)) -> Result<Sdk, String>` | kết nối bằng ticket **hoặc** connect code 6 ký tự |
| `sdk.send(peer, channel, &data, reliable) -> Result<(), String>` | gửi tin; `channel` 0..255 |
| `sdk.poll() / sdk.poll_wait(Duration) -> Option<SdkEvent>` | lấy sự kiện (không chặn / chờ tối đa) |
| `sdk.disconnect(peer)`, `sdk.peer_count()` | ngắt một peer, đếm peer |

`SdkEvent { kind, peer, channel, reliable, data }`, với `kind` là `EV_CONNECTED`, `EV_MESSAGE`, `EV_DISCONNECTED`
(`data` = lý do), `EV_CONNECT_FAILED` (`data` = lý do). Hằng `MAX_UNRELIABLE` = 1180.

### Bắt đầu nhanh (5 phút)

```rust
use std::time::Duration;
use peerova::{Sdk, EV_CONNECTED, EV_MESSAGE};

// Server
let mut srv = Sdk::server(Some(r#"{"port":47000,"key_file":"server.key"}"#))?;
println!("ticket: {}", srv.ticket().unwrap_or_default());
if let Some(code) = srv.room_code() { println!("connect code: {code}"); }

// Client (process khác) — ticket hoặc connect code
let mut cli = Sdk::client(&ticket_or_code, None)?;
let ev = cli.poll_wait(Duration::from_secs(20)).ok_or("timeout")?;
assert_eq!(ev.kind, EV_CONNECTED);
cli.send(ev.peer, 0, b"xin chao", true)?;
```

Connect code: khởi động server với `"app"` (mã project trên website Peerova) và
`"share":{"name":"...","public":true,"max":16,"mode":"...","region":"VN"}`, rồi gọi `room_code()` định kỳ tới khi có giá trị.
Với connect code, `Sdk::client` tra mã trên website trước (một request HTTPS) và trả `Err` nếu mã không tồn tại.

Kết nối là tự động: thử kết nối trực tiếp trước, và có đường dự phòng mã hoá tự động khi mạng chặn kết nối trực tiếp.

### Gửi reliable hay unreliable

| | `reliable = true` | `reliable = false` |
|---|---|---|
| Đảm bảo | tới nơi, đúng thứ tự trong cùng channel | có thể mất hoặc đảo thứ tự |
| Kích thước | có thể lớn | tối đa **1180 byte** (`MAX_UNRELIABLE`); lớn hơn trả `Err` |
| Dùng cho | chat, inventory, RPC, khối dữ liệu file | input, vị trí, trạng thái realtime kiểu voice |

Các channel (0..255) độc lập: tin chậm trên channel này không chặn channel khác.

### Vòng lặp sự kiện và thread

Các phương thức nhận `&mut self`, nên một `Sdk` thuộc về một chỗ sở hữu: chạy vòng lặp `poll_wait` trên một thread
(hoặc `poll()` mỗi frame trong game loop) và gửi từ chính vòng đó. Thread khác muốn gửi thì đẩy lệnh qua
`std::sync::mpsc` (xem `examples/chat.rs`), hoặc bọc `Sdk` trong `Mutex` — đừng giữ khoá trong khi `poll_wait` chờ lâu.
Với async (tokio...), chạy vòng lặp trong `spawn_blocking` hoặc một thread riêng.

### Ngắt kết nối, dọn dẹp, lỗi

- `disconnect(peer)` đóng một kết nối; `drop(sdk)` ngắt mọi peer và giải phóng node.
- Lỗi trả về `Err(String)` (cấu hình sai, cổng bận, peer không tồn tại, gói unreliable quá lớn, connect code không tồn tại).
  Lỗi kết nối tới dưới dạng sự kiện `EV_CONNECT_FAILED`.

### Cấu hình

Server: `port` (mặc định `47000`; `0` = ngẫu nhiên), `key_file` (mặc định `"server.key"`; cùng khoá ⇒ cùng ticket),
`license` (`PVA-...`; không có: chế độ dev, tối đa 3 client), `max_ccu`, `token` (bí mật client phải gửi đúng),
`upnp` (mặc định `true`), `private` (ticket không chứa địa chỉ IP của server), `app` + `share` (connect code).
Client: `token`, `timeout_ms` (mặc định `20000`).

### Bảo mật

- `server.key` là danh tính của server: ai có nó có thể giả làm server. Giữ bí mật, không commit.
- Ticket / connect code để chia sẻ cho client (bán công khai); `token` và `license` là bí mật.
- Toàn bộ dữ liệu được mã hoá đầu-cuối.

### Khắc phục sự cố

- Chẩn đoán mạng: `peerova doctor` (công cụ dòng lệnh `peerova`).
- `EV_CONNECT_FAILED` sau ~20 s: server chưa chạy, ticket/code sai, hoặc `token` không khớp.
- Client thứ 4 bị từ chối: chế độ dev (không có `license`), tối đa 3 client.
- `send` unreliable trả `Err`: payload vượt 1180 byte.

---

## English

Use the `peerova` crate (package `peerova-ffi`, type `peerova::Sdk`) directly from Rust: no C ABI, no `unsafe`, and no
separate native library — the engine is linked statically into your binary. Direct P2P connections, end-to-end encrypted.

Concepts (server, client, ticket, connect code, channels, events): [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md).
Website: https://peerova.pages.dev

This directory is a standalone example crate:

```
rust/
├── Cargo.toml          dependency: peerova = { package = "peerova-ffi", ... }
├── src/main.rs         in-process demo + echo server/client modes
├── examples/chat.rs    chat + 20 Hz position sync
└── tests/roundtrip.rs  round-trip test (200 reliable in order + unreliable + binary reply)
```

### Requirements and install

- Stable Rust, edition 2021.
- Add to `Cargo.toml` (path or git location provided by Peerova):
  ```toml
  [dependencies]
  peerova = { package = "peerova-ffi", path = "<path to peerova-ffi>" }
  ```
- The binary uses the crate directly, so there is **no** `libpeerova.so` / `peerova.dll` to ship next to it.
  Windows: `wintun.dll` is not needed (only the `peerova` command-line tool needs it, for VPN mode).
- macOS / iOS / Android: contact Peerova about supported targets.

Try it (in `bindings/rust`):

```sh
cargo test --release -- --nocapture                      # "rust PASS: ..."
cargo run --release                                      # server + client demo in one process
cargo run --release --example chat -- server 0 /tmp/demo.key
cargo run --release --example chat -- client <TICKET>
```

### API

| Function | Meaning |
|---|---|
| `Sdk::server(Some(json)) -> Result<Sdk, String>` | start a server; blocks until the ticket is ready (~3 s max) |
| `sdk.ticket() -> Option<&str>` | `pvt_...` ticket to share with clients |
| `sdk.room_code() -> Option<String>` | 6-character connect code (only with `app` + `share` config; `None` until the website assigns it) |
| `Sdk::client(ticket_or_code, Some(json)) -> Result<Sdk, String>` | connect with a ticket **or** a 6-character connect code |
| `sdk.send(peer, channel, &data, reliable) -> Result<(), String>` | send a message; `channel` 0..255 |
| `sdk.poll() / sdk.poll_wait(Duration) -> Option<SdkEvent>` | get an event (non-blocking / wait up to) |
| `sdk.disconnect(peer)`, `sdk.peer_count()` | drop a peer, count peers |

`SdkEvent { kind, peer, channel, reliable, data }`, where `kind` is `EV_CONNECTED`, `EV_MESSAGE`, `EV_DISCONNECTED`
(`data` = reason), `EV_CONNECT_FAILED` (`data` = reason). Constant `MAX_UNRELIABLE` = 1180.

### 5-minute quick start

```rust
use std::time::Duration;
use peerova::{Sdk, EV_CONNECTED, EV_MESSAGE};

// Server
let mut srv = Sdk::server(Some(r#"{"port":47000,"key_file":"server.key"}"#))?;
println!("ticket: {}", srv.ticket().unwrap_or_default());
if let Some(code) = srv.room_code() { println!("connect code: {code}"); }

// Client (another process) — ticket or connect code
let mut cli = Sdk::client(&ticket_or_code, None)?;
let ev = cli.poll_wait(Duration::from_secs(20)).ok_or("timeout")?;
assert_eq!(ev.kind, EV_CONNECTED);
cli.send(ev.peer, 0, b"hello", true)?;
```

Connect code: start the server with `"app"` (your project id from the Peerova website) and
`"share":{"name":"...","public":true,"max":16,"mode":"...","region":"VN"}`, then call `room_code()` periodically until it returns a value.
With a connect code, `Sdk::client` first looks the code up on the website (one HTTPS request) and returns `Err` if it does not exist.

Connections are automatic: direct first, with an automatic encrypted fallback route when networks block direct connections.

### Reliable vs unreliable

| | `reliable = true` | `reliable = false` |
|---|---|---|
| Guarantee | delivered, in order within the channel | may be lost or reordered |
| Size | can be large | at most **1180 bytes** (`MAX_UNRELIABLE`); larger returns `Err` |
| Use for | chat, inventory, RPCs, file chunks | inputs, positions, voice-like realtime state |

Channels (0..255) are independent: a delayed message on one channel does not block another.

### Event loop and threads

Methods take `&mut self`, so an `Sdk` has a single owner: run the `poll_wait` loop on one thread (or `poll()` once per
frame in a game loop) and send from that same loop. Other threads that want to send push commands through
`std::sync::mpsc` (see `examples/chat.rs`), or wrap the `Sdk` in a `Mutex` — do not hold the lock during a long `poll_wait`.
With async runtimes (tokio, ...), run the loop in `spawn_blocking` or a dedicated thread.

### Disconnect, cleanup, errors

- `disconnect(peer)` closes one connection; `drop(sdk)` disconnects all peers and frees the node.
- Errors are `Err(String)` (bad config, port in use, unknown peer, oversized unreliable payload, unknown connect code).
  Connection failures arrive as an `EV_CONNECT_FAILED` event.

### Configuration

Server: `port` (default `47000`; `0` = random), `key_file` (default `"server.key"`; same key ⇒ same ticket),
`license` (`PVA-...`; without it: dev mode, max 3 clients), `max_ccu`, `token` (secret clients must present),
`upnp` (default `true`), `private` (ticket does not contain the server's IP addresses), `app` + `share` (connect code).
Client: `token`, `timeout_ms` (default `20000`).

### Security

- `server.key` is the server's identity: anyone who has it can impersonate the server. Keep it secret, do not commit it.
- Ticket / connect code are for sharing with clients (semi-public); `token` and `license` are secrets.
- All traffic is end-to-end encrypted.

### Troubleshooting

- Network diagnosis: `peerova doctor` (the `peerova` command-line tool).
- `EV_CONNECT_FAILED` after ~20 s: server not running, wrong ticket/code, or `token` mismatch.
- The 4th client is refused: dev mode (no `license`), max 3 clients.
- An unreliable `send` returns `Err`: payload larger than 1180 bytes.
