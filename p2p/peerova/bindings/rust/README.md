# Peerova cho Rust

- [Tiếng Việt](#tiếng-việt)
- [English](#english)

---

## Tiếng Việt

Dùng thẳng crate `peerova` (package `peerova-ffi`, kiểu `peerova::Sdk`) từ Rust: không qua C ABI, không `unsafe`,
không cần thư viện native riêng — engine được link tĩnh vào binary của bạn. Kết nối P2P trực tiếp, mã hoá đầu-cuối.

Khái niệm (server, client, ticket, connect code, channel, sự kiện): [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md).
Website: https://peerova.pages.dev

### Chọn cách tích hợp

Xem [bảng chọn cách tích hợp](../DEVELOPER-GUIDE.md#1-chọn-cách-tích-hợp). Với Rust:
- **Tình huống 3 (game/app mới) và 4 (host trong game):** crate `peerova` (`Sdk`) — phần lớn README này.
- **Tình huống 2 (chương trình có sẵn, người dùng không chạy gì):** app tự chạy `peerova client … --forward` ẩn bằng `std::process::Command`
  rồi code socket cũ kết nối `127.0.0.1` — xem [Game có sẵn: chạy peerova ẩn](#game-có-sẵn-chạy-peerova-ẩn) (desktop; mobile không chạy được process con → dùng `Sdk`).

Thư mục này là một crate ví dụ độc lập:

```
rust/
├── (manifest dự án)    phụ thuộc: peerova = { package = "peerova-ffi", ... }
├── src/main.rs         demo trong một process + chế độ server/client echo
├── examples/chat.rs    chat + đồng bộ vị trí 20 Hz
└── tests/roundtrip.rs  test round-trip (200 tin reliable đúng thứ tự + unreliable + reply nhị phân)
```

### Yêu cầu và cài đặt

- Rust stable, edition 2021.
- Thêm vào mục `[dependencies]` trong manifest của dự án (đường dẫn hoặc git do Peerova cung cấp):
  ```toml
  [dependencies]
  peerova = { package = "peerova-ffi", path = "<đường dẫn tới peerova-ffi>" }
  ```
- Binary dùng crate trực tiếp nên **không** cần đặt `libpeerova.so` / `peerova.dll` cạnh file thực thi.
  Windows: không cần `wintun.dll` (chỉ công cụ dòng lệnh `peerova` cần nó cho chế độ VPN).
- macOS / iOS / Android: liên hệ Peerova về target được hỗ trợ.

Chạy thử (trong `bindings/rust`, bằng công cụ build của toolchain Rust, luôn với `--release`):

| Lệnh con | Làm gì |
|---|---|
| `test --release -- --nocapture` | chạy test round-trip, in `rust PASS: ...` |
| `run --release` | demo server + client trong một process |
| `run --release --example chat -- server 0 demo.key` | ví dụ chat: server, in ticket |
| `run --release --example chat -- client <TICKET>` | ví dụ chat: client (cửa sổ/máy khác) |

### API

| Hàm | Ý nghĩa |
|---|---|
| `Sdk::server(Some(json)) -> Result<Sdk, String>` | khởi động server; chặn tới khi có ticket (tối đa ~3 s) |
| `sdk.ticket() -> Option<&str>` | ticket `pvt_...` để chia sẻ cho client (hoặc nhúng vào game) |
| `sdk.room_code() -> Option<String>` | connect code 6 ký tự (chỉ khi cấu hình `app` + `share`; `None` tới khi website cấp mã) |
| `Sdk::client(ticket_or_code, Some(json)) -> Result<Sdk, String>` | kết nối bằng ticket **hoặc** connect code 6 ký tự |
| `sdk.send(peer, channel, &data, reliable) -> Result<(), String>` | gửi tin; `channel` 0..255 |
| `sdk.poll() / sdk.poll_wait(Duration) -> Option<SdkEvent>` | lấy sự kiện (không chặn / chờ tối đa) |
| `sdk.disconnect(peer)`, `sdk.peer_count()` | ngắt một peer, đếm peer |
| `peerova::classify(&err) -> SdkError` | đổi chuỗi lỗi `Err(String)` thành mã ổn định + thông điệp tiếng Anh |
| `peerova::event_error(&ev) -> Option<SdkError>` | mã + lý do tiếng Anh của sự kiện `EV_CONNECT_FAILED` / `EV_DISCONNECTED` |

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

// Client (process khác) — ticket hoặc connect code; key_file giữ danh tính người chơi
let mut cli = Sdk::client(&ticket_or_code, Some(r#"{"key_file":"<appdata>/MyGame/peerova-client.key"}"#))?;
let ev = cli.poll_wait(Duration::from_secs(20)).ok_or("timeout")?;
assert_eq!(ev.kind, EV_CONNECTED);
cli.send(ev.peer, 0, b"xin chao", true)?;
```

Connect code: khởi động server với `"app"` (mã project trên website Peerova) và
`"share":{"name":"...","public":true,"max":16,"mode":"...","region":"VN"}`, rồi gọi `room_code()` định kỳ tới khi có giá trị.
Với connect code, `Sdk::client` tra mã trên website trước (một request HTTPS) và trả `Err` nếu mã không tồn tại.

Kết nối là tự động: thử kết nối trực tiếp trước; nếu mạng chặn, tự dùng **đường mã hoá tự động** (vẫn mã hoá đầu-cuối).

### Gửi reliable hay unreliable

| | `reliable = true` | `reliable = false` |
|---|---|---|
| Đảm bảo | tới nơi, đúng thứ tự trong cùng channel | có thể mất hoặc đảo thứ tự |
| Kích thước | có thể lớn | tối đa **1180 byte** (`MAX_UNRELIABLE`); lớn hơn trả `Err` (`E_TOO_LARGE`) |
| Dùng cho | chat, inventory, RPC, khối dữ liệu file | input, vị trí, trạng thái realtime kiểu voice |

Các channel (0..255) độc lập: tin chậm trên channel này không chặn channel khác.

### Vòng lặp sự kiện và thread

Các phương thức nhận `&mut self`, nên một `Sdk` thuộc về một chỗ sở hữu: chạy vòng lặp `poll_wait` trên một thread
(hoặc `poll()` mỗi frame trong game loop) và gửi từ chính vòng đó. Thread khác muốn gửi thì đẩy lệnh qua
`std::sync::mpsc` (xem `examples/chat.rs`), hoặc bọc `Sdk` trong `Mutex` — đừng giữ khoá trong khi `poll_wait` chờ lâu.
Với async (tokio...), chạy vòng lặp trong `spawn_blocking` hoặc một thread riêng. `Sdk::server` chặn tới ~3 s — đừng gọi trên UI thread.

### Ngắt kết nối, dọn dẹp, lỗi

- `disconnect(peer)` đóng một kết nối; `drop(sdk)` ngắt mọi peer và giải phóng node.
- Lỗi trả về `Err(String)` (cấu hình sai, cổng bận, peer không tồn tại, gói unreliable quá lớn, connect code không tồn tại).
  Lỗi kết nối tới dưới dạng sự kiện `EV_CONNECT_FAILED`.
- Văn bản trong `Err(String)` và `data` của sự kiện là thông điệp gốc của engine. Để xử lý theo loại lỗi, dùng mã ổn định:
  ```rust
  use peerova::{classify, event_error, ErrorCode};
  match Sdk::client(&code, None) {
      Err(e) if classify(&e).code == ErrorCode::CodeNotFound => println!("phòng không tồn tại"),
      Err(e) => println!("{}", classify(&e)),            // "E_NAME: english message"
      Ok(cli) => { /* ... */ }
  }
  // sự kiện: if let Some(err) = event_error(&ev) { println!("{} ({})", err.message, err.code.name()); }
  ```
  `ErrorCode`: `InvalidArg`, `TooLarge`, `UnknownPeer`, `Closed`, `CodeNotFound`, `Network`, `Timeout`, `License`, `CcuLimit`,
  `AccessDenied`, `InvalidTicket`, `Io`, `Internal` — ý nghĩa ở [Mã lỗi](../DEVELOPER-GUIDE.md#mã-lỗi).

### Cấu hình

Server (`Sdk::server`, JSON):

| Khoá | Mặc định | Ý nghĩa |
|---|---|---|
| `port` | `47000` | cổng UDP; `0` = ngẫu nhiên |
| `key_file` | `"server.key"` | danh tính server; cùng khoá ⇒ cùng ticket. **Bí mật** |
| `license` | — | `PVA-...`; không có: chế độ dev, tối đa 3 client |
| `max_ccu` | theo license | số client đồng thời tối đa |
| `token` | — | bí mật client phải gửi đúng |
| `upnp` | `true` | thử mở cổng trên router |
| `private` | `false` | ticket không chứa địa chỉ IP của server (khi kết nối trực tiếp người chơi vẫn biết IP — dùng `hide_ip` để giấu thật sự) |
| `hide_ip` | `false` | Ẩn IP máy chủ: người chơi không bao giờ biết IP thật của máy chủ (chống DDoS); ping cao hơn một chút. Gói Pro/Studio/Enterprise và bản dùng thử |
| `app`, `share` | — | connect code: `app` = project id, `share` = `{"name","public","max","mode","region"}` |

Client (`Sdk::client`, JSON):

| Khoá | Mặc định | Ý nghĩa |
|---|---|---|
| `token` | — | phải khớp server |
| `timeout_ms` | `20000` | thời gian tối đa trước `EV_CONNECT_FAILED` |
| `key_file` | — (khoá ngẫu nhiên mỗi lần chạy) | danh tính người chơi, tạo mới nếu chưa có. Lưu trong thư mục dữ liệu ứng dụng, ví dụ `"<appdata>/<game>/peerova-client.key"`; **thư mục phải tồn tại**. Cấm theo địa chỉ chỉ có tác dụng với từng người chơi khi họ còn giữ file này |

### Game có sẵn: chạy peerova ẩn

Tình huống 2 ([§4 của hướng dẫn](../DEVELOPER-GUIDE.md#4-game-tự-chạy-peerova-ẩn-người-chơi-không-chạy-gì)): phát hành `peerova.exe` / `peerova`
**kèm chương trình**. Code dưới đây (chỉ dùng `std`) chạy `peerova client <TICKET> --forward tcp:8888 --key <appdata>/<game>/peerova-client.key`
ẩn (Windows: `CREATE_NO_WINDOW`, không cửa sổ console), tạo thư mục chứa key, đọc stdout+stderr từng dòng UTF-8 trên hai thread, và trả về khi một
dòng chứa chuỗi ASCII `server port` (CLI in `✔ ĐÃ KẾT NỐI …` rồi `✔ Tcp 127.0.0.1:8888 → server port 8888`, mỗi `--forward` một dòng).
Thất bại nếu process thoát trước (mã thoát 1, dòng stderr bắt đầu bằng `lỗi:`) hoặc quá thời gian (khi đó kill). `Drop` kill `peerova`.

```rust
// peerova_tunnel.rs — std only. Runs the bundled `peerova` CLI hidden and waits until the local port is open.
use std::io::{self, BufRead, BufReader, Read};
use std::path::Path;
use std::process::{Child, Command, Stdio};
use std::sync::mpsc;
use std::thread;
use std::time::{Duration, Instant};

pub struct PeerovaTunnel {
    child: Child,
}

impl PeerovaTunnel {
    /// Starts `peerova client <ticket> --forward <forward> --key <key_file>` without a console window and
    /// returns once the forwarded port is open on 127.0.0.1. `on_log` gets every output line (from a reader thread).
    pub fn start(
        exe: &Path,
        ticket: &str,
        forward: &str,
        key_file: &Path,
        timeout: Duration,
        on_log: impl Fn(&str) + Send + Sync + 'static,
    ) -> io::Result<PeerovaTunnel> {
        if let Some(dir) = key_file.parent() {
            std::fs::create_dir_all(dir)?; // the key file's folder must exist
        }
        let mut cmd = Command::new(exe);
        cmd.args(["client", ticket, "--forward", forward, "--key"])
            .arg(key_file)
            .stdin(Stdio::null())
            .stdout(Stdio::piped())
            .stderr(Stdio::piped());
        #[cfg(windows)]
        {
            use std::os::windows::process::CommandExt;
            cmd.creation_flags(0x0800_0000); // CREATE_NO_WINDOW
        }
        let mut child = cmd.spawn()?;

        // Drain stdout and stderr on two threads for the whole lifetime of the process (a full pipe stalls peerova).
        let (tx, rx) = mpsc::channel::<String>();
        let on_log = std::sync::Arc::new(on_log);
        let out: Box<dyn Read + Send> = Box::new(child.stdout.take().unwrap());
        let err: Box<dyn Read + Send> = Box::new(child.stderr.take().unwrap());
        for pipe in [out, err] {
            let (tx, on_log) = (tx.clone(), on_log.clone());
            thread::spawn(move || {
                let mut r = BufReader::new(pipe);
                let mut buf = Vec::new();
                while matches!(r.read_until(b'\n', &mut buf), Ok(n) if n > 0) {
                    let line = String::from_utf8_lossy(&buf).trim_end().to_string(); // UTF-8, strips \r\n
                    on_log(&line);
                    let _ = tx.send(line); // keeps reading after start() returns
                    buf.clear();
                }
            });
        }
        drop(tx); // rx disconnects once both pipes are closed (= the process exited)

        let deadline = Instant::now() + timeout;
        let mut reason = String::from("timed out");
        loop {
            let left = deadline.saturating_duration_since(Instant::now());
            match rx.recv_timeout(left) {
                // "✔ Tcp 127.0.0.1:8888 → server port 8888" = connected and the local port is open
                Ok(line) if line.contains("server port") => return Ok(PeerovaTunnel { child }),
                Ok(line) => {
                    if line.starts_with("lỗi:") {
                        reason = line; // exit code 1 follows
                    }
                }
                Err(mpsc::RecvTimeoutError::Disconnected) => {
                    let _ = child.wait();
                    return Err(io::Error::new(io::ErrorKind::Other, format!("peerova exited: {reason}")));
                }
                Err(mpsc::RecvTimeoutError::Timeout) => {
                    let _ = child.kill();
                    let _ = child.wait();
                    return Err(io::Error::new(io::ErrorKind::TimedOut, "peerova: timed out"));
                }
            }
        }
    }

    /// false once the connection is lost (`peerova client` exits then).
    pub fn running(&mut self) -> bool {
        matches!(self.child.try_wait(), Ok(None))
    }
}

impl Drop for PeerovaTunnel {
    fn drop(&mut self) {
        let _ = self.child.kill(); // stop peerova when the game exits
        let _ = self.child.wait();
    }
}
```
```rust
// main.rs — usage
mod peerova_tunnel;
use peerova_tunnel::PeerovaTunnel;
use std::path::PathBuf;
use std::time::Duration;

fn main() {
    // peerova.exe / peerova shipped next to the program
    let exe = std::env::current_exe().unwrap().with_file_name(if cfg!(windows) { "peerova.exe" } else { "peerova" });
    // <appdata>/<game>/peerova-client.key (Windows: %APPDATA%, else ~/.local/share)
    let data = std::env::var_os("APPDATA").map(PathBuf::from).unwrap_or_else(|| {
        PathBuf::from(std::env::var_os("HOME").unwrap_or_default()).join(".local/share")
    });
    let key = data.join("MyGame").join("peerova-client.key");

    let mut tunnel = match PeerovaTunnel::start(&exe, "pvt_...", "tcp:8888", &key, Duration::from_secs(30),
                                                |line| eprintln!("[peerova] {line}")) {
        Ok(t) => t,
        Err(e) => { eprintln!("Could not reach the server: {e}"); std::process::exit(1); }
    };
    let stream = std::net::TcpStream::connect("127.0.0.1:8888");   // your existing socket code, unchanged
    // ... game loop; tunnel.running() turns false if the connection is lost
    let _ = (stream, tunnel.running());
}   // dropping `tunnel` stops peerova
```

Lưu ý:
- Nhiều `--forward` → sửa điều kiện để chờ đủ số dòng `server port`. Linux/macOS: `peerova` phải có quyền chạy; macOS: codesign cùng app.
- Chọn cổng local ít đụng (`tcp:38888=8888`) nếu 8888 có thể bị chương trình khác dùng.
- `peerova client` thoát khi mất kết nối → `running()` thành `false`. Nếu app bị kill đột ngột (không chạy `Drop`), process `peerova` có thể còn lại.
- Mobile không chạy được process con → dùng `Sdk`. `peerova stdio` **không** mở cổng local (đó là SDK message dạng JSON lines), không dùng cho cách này.

### Người chơi không phải gõ gì

Người chơi không bao giờ phải gõ ticket hay mã: nhúng ticket vào chương trình, hoặc tải ticket mới nhất từ URL của bạn khi khởi động
(dùng ticket nhúng nếu không tải được). Mã 6 ký tự chỉ để mời bạn bè vào phòng do người chơi mở. Chi tiết và failover nhiều máy chủ
(`peerova ticket merge`): [§2 của hướng dẫn](../DEVELOPER-GUIDE.md#2-người-chơi-không-phải-gõ-gì).

**Máy chủ game:** nhiều cổng trong một process, mỗi người chơi một địa chỉ `127.a.b.c` + cấm người chơi, ẩn IP máy chủ, chế độ VPN —
xem [§7 của hướng dẫn](../DEVELOPER-GUIDE.md#7-tính-năng-máy-chủ-game).

### Bảo mật

- `server.key` là danh tính của server: ai có nó có thể giả làm server. Giữ bí mật, không commit.
- Ticket / connect code để chia sẻ cho client (bán công khai); `token` và `license` là bí mật — không nhúng license vào bản phát cho người chơi.
- Toàn bộ dữ liệu được mã hoá đầu-cuối.

### Khắc phục sự cố

- Chẩn đoán mạng: `peerova doctor` (công cụ dòng lệnh `peerova`).
- `EV_CONNECT_FAILED` (`E_TIMEOUT`) sau ~20 s: server chưa chạy, ticket/code sai, hoặc `token` không khớp (`E_ACCESS_DENIED`).
- Client thứ 4 bị từ chối (`E_CCU_LIMIT`): chế độ dev (không có `license`), tối đa 3 client.
- `send` unreliable trả `Err` (`E_TOO_LARGE`): payload vượt 1180 byte.
- `E_IO`: thư mục chứa `key_file` không tồn tại hoặc không ghi được.
- Server không khởi động khi bật `hide_ip`: gói hiện tại không có ẩn IP (cần Pro/Studio/Enterprise hoặc bản dùng thử).

### FAQ

- **Người chơi có phải gõ mã không?** Không. Nhúng ticket hoặc tải từ URL của bạn; mã 6 ký tự chỉ để mời bạn bè.
- **Có cần mở port / IP tĩnh không?** Không; mở port chỉ giúp kết nối trực tiếp nhanh hơn.
- Thêm: [FAQ của hướng dẫn](../DEVELOPER-GUIDE.md#16-faq), [Khắc phục sự cố](../DEVELOPER-GUIDE.md#15-khắc-phục-sự-cố).

---

## English

Use the `peerova` crate (package `peerova-ffi`, type `peerova::Sdk`) directly from Rust: no C ABI, no `unsafe`, and no
separate native library — the engine is linked statically into your binary. Direct P2P connections, end-to-end encrypted.

Concepts (server, client, ticket, connect code, channels, events): [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md).
Website: https://peerova.pages.dev

### Choosing an integration

See the [integration table](../DEVELOPER-GUIDE.md#1-choosing-an-integration). With Rust:
- **Scenario 3 (new game/app) and 4 (host-in-game):** the `peerova` crate (`Sdk`) — most of this README.
- **Scenario 2 (existing program, users run nothing):** the app starts `peerova client … --forward` hidden with `std::process::Command`
  and the existing socket code connects to `127.0.0.1` — see [Existing game: run peerova hidden](#existing-game-run-peerova-hidden) (desktop; mobile can't spawn processes → use `Sdk`).

This directory is a standalone example crate:

```
rust/
├── (project manifest)  dependency: peerova = { package = "peerova-ffi", ... }
├── src/main.rs         in-process demo + echo server/client modes
├── examples/chat.rs    chat + 20 Hz position sync
└── tests/roundtrip.rs  round-trip test (200 reliable in order + unreliable + binary reply)
```

### Requirements and install

- Stable Rust, edition 2021.
- Add to the `[dependencies]` section of your project manifest (path or git location provided by Peerova):
  ```toml
  [dependencies]
  peerova = { package = "peerova-ffi", path = "<path to peerova-ffi>" }
  ```
- The binary uses the crate directly, so there is **no** `libpeerova.so` / `peerova.dll` to ship next to it.
  Windows: `wintun.dll` is not needed (only the `peerova` command-line tool needs it, for VPN mode).
- macOS / iOS / Android: contact Peerova about supported targets.

Try it (in `bindings/rust`, with your Rust toolchain's build tool, always with `--release`):

| Subcommand | What it does |
|---|---|
| `test --release -- --nocapture` | runs the round-trip test, prints `rust PASS: ...` |
| `run --release` | server + client demo in one process |
| `run --release --example chat -- server 0 demo.key` | chat example: server, prints the ticket |
| `run --release --example chat -- client <TICKET>` | chat example: client (another terminal/machine) |

### API

| Function | Meaning |
|---|---|
| `Sdk::server(Some(json)) -> Result<Sdk, String>` | start a server; blocks until the ticket is ready (~3 s max) |
| `sdk.ticket() -> Option<&str>` | `pvt_...` ticket to share with clients (or embed in the game) |
| `sdk.room_code() -> Option<String>` | 6-character connect code (only with `app` + `share` config; `None` until the website assigns it) |
| `Sdk::client(ticket_or_code, Some(json)) -> Result<Sdk, String>` | connect with a ticket **or** a 6-character connect code |
| `sdk.send(peer, channel, &data, reliable) -> Result<(), String>` | send a message; `channel` 0..255 |
| `sdk.poll() / sdk.poll_wait(Duration) -> Option<SdkEvent>` | get an event (non-blocking / wait up to) |
| `sdk.disconnect(peer)`, `sdk.peer_count()` | drop a peer, count peers |
| `peerova::classify(&err) -> SdkError` | turns an `Err(String)` text into a stable code + English message |
| `peerova::event_error(&ev) -> Option<SdkError>` | code + English reason of an `EV_CONNECT_FAILED` / `EV_DISCONNECTED` event |

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

// Client (another process) — ticket or connect code; key_file keeps the player's identity
let mut cli = Sdk::client(&ticket_or_code, Some(r#"{"key_file":"<appdata>/MyGame/peerova-client.key"}"#))?;
let ev = cli.poll_wait(Duration::from_secs(20)).ok_or("timeout")?;
assert_eq!(ev.kind, EV_CONNECTED);
cli.send(ev.peer, 0, b"hello", true)?;
```

Connect code: start the server with `"app"` (your project id from the Peerova website) and
`"share":{"name":"...","public":true,"max":16,"mode":"...","region":"VN"}`, then call `room_code()` periodically until it returns a value.
With a connect code, `Sdk::client` first looks the code up on the website (one HTTPS request) and returns `Err` if it does not exist.

Connections are automatic: direct first; when the network blocks that, the **automatic encrypted route** is used (still end-to-end encrypted).

### Reliable vs unreliable

| | `reliable = true` | `reliable = false` |
|---|---|---|
| Guarantee | delivered, in order within the channel | may be lost or reordered |
| Size | can be large | at most **1180 bytes** (`MAX_UNRELIABLE`); larger returns `Err` (`E_TOO_LARGE`) |
| Use for | chat, inventory, RPCs, file chunks | inputs, positions, voice-like realtime state |

Channels (0..255) are independent: a delayed message on one channel does not block another.

### Event loop and threads

Methods take `&mut self`, so an `Sdk` has a single owner: run the `poll_wait` loop on one thread (or `poll()` once per
frame in a game loop) and send from that same loop. Other threads that want to send push commands through
`std::sync::mpsc` (see `examples/chat.rs`), or wrap the `Sdk` in a `Mutex` — do not hold the lock during a long `poll_wait`.
With async runtimes (tokio, ...), run the loop in `spawn_blocking` or a dedicated thread. `Sdk::server` blocks for up to ~3 s — keep it off the UI thread.

### Disconnect, cleanup, errors

- `disconnect(peer)` closes one connection; `drop(sdk)` disconnects all peers and frees the node.
- Errors are `Err(String)` (bad config, port in use, unknown peer, oversized unreliable payload, unknown connect code).
  Connection failures arrive as an `EV_CONNECT_FAILED` event.
- The text in `Err(String)` and in event `data` is the engine's own message. To branch on the error type, use the stable code:
  ```rust
  use peerova::{classify, event_error, ErrorCode};
  match Sdk::client(&code, None) {
      Err(e) if classify(&e).code == ErrorCode::CodeNotFound => println!("room does not exist"),
      Err(e) => println!("{}", classify(&e)),            // "E_NAME: english message"
      Ok(cli) => { /* ... */ }
  }
  // events: if let Some(err) = event_error(&ev) { println!("{} ({})", err.message, err.code.name()); }
  ```
  `ErrorCode`: `InvalidArg`, `TooLarge`, `UnknownPeer`, `Closed`, `CodeNotFound`, `Network`, `Timeout`, `License`, `CcuLimit`,
  `AccessDenied`, `InvalidTicket`, `Io`, `Internal` — meanings in [Error codes](../DEVELOPER-GUIDE.md#error-codes).

### Configuration

Server (`Sdk::server`, JSON):

| Key | Default | Meaning |
|---|---|---|
| `port` | `47000` | UDP port; `0` = random |
| `key_file` | `"server.key"` | server identity; same key ⇒ same ticket. **Secret** |
| `license` | — | `PVA-...`; without it: dev mode, max 3 clients |
| `max_ccu` | from license | maximum concurrent clients |
| `token` | — | secret clients must present |
| `upnp` | `true` | try to open the port on the router |
| `private` | `false` | the ticket does not contain the server's IP addresses (on a direct connection players still see the IP — use `hide_ip` to really hide it) |
| `hide_ip` | `false` | Hide the server IP: players never learn its real address (DDoS protection); slightly higher latency. Pro/Studio/Enterprise plans and the trial |
| `app`, `share` | — | connect code: `app` = project id, `share` = `{"name","public","max","mode","region"}` |

Client (`Sdk::client`, JSON):

| Key | Default | Meaning |
|---|---|---|
| `token` | — | must match the server |
| `timeout_ms` | `20000` | maximum time before `EV_CONNECT_FAILED` |
| `key_file` | — (random key per run) | the player's identity, created if missing. Store it in the user's app-data folder, e.g. `"<appdata>/<game>/peerova-client.key"`; **the folder must exist**. Banning by address works per player only while they keep that file |

### Existing game: run peerova hidden

Scenario 2 ([guide §4](../DEVELOPER-GUIDE.md#4-the-game-runs-peerova-hidden-players-run-nothing)): ship `peerova.exe` / `peerova` **with the
program**. The code below (`std` only) starts `peerova client <TICKET> --forward tcp:8888 --key <appdata>/<game>/peerova-client.key` hidden
(Windows: `CREATE_NO_WINDOW`, no console window), creates the key folder, reads stdout+stderr as UTF-8 lines on two threads, and returns once a
line contains the ASCII text `server port` (the CLI prints `✔ ĐÃ KẾT NỐI …` = "connected", then `✔ Tcp 127.0.0.1:8888 → server port 8888`, once
per `--forward`). It fails if the process exits first (exit code 1, a stderr line starting with `lỗi:` = "error:") or on timeout (then it is
killed). `Drop` kills `peerova`.

```rust
// peerova_tunnel.rs — std only. Runs the bundled `peerova` CLI hidden and waits until the local port is open.
use std::io::{self, BufRead, BufReader, Read};
use std::path::Path;
use std::process::{Child, Command, Stdio};
use std::sync::mpsc;
use std::thread;
use std::time::{Duration, Instant};

pub struct PeerovaTunnel {
    child: Child,
}

impl PeerovaTunnel {
    /// Starts `peerova client <ticket> --forward <forward> --key <key_file>` without a console window and
    /// returns once the forwarded port is open on 127.0.0.1. `on_log` gets every output line (from a reader thread).
    pub fn start(
        exe: &Path,
        ticket: &str,
        forward: &str,
        key_file: &Path,
        timeout: Duration,
        on_log: impl Fn(&str) + Send + Sync + 'static,
    ) -> io::Result<PeerovaTunnel> {
        if let Some(dir) = key_file.parent() {
            std::fs::create_dir_all(dir)?; // the key file's folder must exist
        }
        let mut cmd = Command::new(exe);
        cmd.args(["client", ticket, "--forward", forward, "--key"])
            .arg(key_file)
            .stdin(Stdio::null())
            .stdout(Stdio::piped())
            .stderr(Stdio::piped());
        #[cfg(windows)]
        {
            use std::os::windows::process::CommandExt;
            cmd.creation_flags(0x0800_0000); // CREATE_NO_WINDOW
        }
        let mut child = cmd.spawn()?;

        // Drain stdout and stderr on two threads for the whole lifetime of the process (a full pipe stalls peerova).
        let (tx, rx) = mpsc::channel::<String>();
        let on_log = std::sync::Arc::new(on_log);
        let out: Box<dyn Read + Send> = Box::new(child.stdout.take().unwrap());
        let err: Box<dyn Read + Send> = Box::new(child.stderr.take().unwrap());
        for pipe in [out, err] {
            let (tx, on_log) = (tx.clone(), on_log.clone());
            thread::spawn(move || {
                let mut r = BufReader::new(pipe);
                let mut buf = Vec::new();
                while matches!(r.read_until(b'\n', &mut buf), Ok(n) if n > 0) {
                    let line = String::from_utf8_lossy(&buf).trim_end().to_string(); // UTF-8, strips \r\n
                    on_log(&line);
                    let _ = tx.send(line); // keeps reading after start() returns
                    buf.clear();
                }
            });
        }
        drop(tx); // rx disconnects once both pipes are closed (= the process exited)

        let deadline = Instant::now() + timeout;
        let mut reason = String::from("timed out");
        loop {
            let left = deadline.saturating_duration_since(Instant::now());
            match rx.recv_timeout(left) {
                // "✔ Tcp 127.0.0.1:8888 → server port 8888" = connected and the local port is open
                Ok(line) if line.contains("server port") => return Ok(PeerovaTunnel { child }),
                Ok(line) => {
                    if line.starts_with("lỗi:") {
                        reason = line; // exit code 1 follows
                    }
                }
                Err(mpsc::RecvTimeoutError::Disconnected) => {
                    let _ = child.wait();
                    return Err(io::Error::new(io::ErrorKind::Other, format!("peerova exited: {reason}")));
                }
                Err(mpsc::RecvTimeoutError::Timeout) => {
                    let _ = child.kill();
                    let _ = child.wait();
                    return Err(io::Error::new(io::ErrorKind::TimedOut, "peerova: timed out"));
                }
            }
        }
    }

    /// false once the connection is lost (`peerova client` exits then).
    pub fn running(&mut self) -> bool {
        matches!(self.child.try_wait(), Ok(None))
    }
}

impl Drop for PeerovaTunnel {
    fn drop(&mut self) {
        let _ = self.child.kill(); // stop peerova when the game exits
        let _ = self.child.wait();
    }
}
```
```rust
// main.rs — usage
mod peerova_tunnel;
use peerova_tunnel::PeerovaTunnel;
use std::path::PathBuf;
use std::time::Duration;

fn main() {
    // peerova.exe / peerova shipped next to the program
    let exe = std::env::current_exe().unwrap().with_file_name(if cfg!(windows) { "peerova.exe" } else { "peerova" });
    // <appdata>/<game>/peerova-client.key (Windows: %APPDATA%, else ~/.local/share)
    let data = std::env::var_os("APPDATA").map(PathBuf::from).unwrap_or_else(|| {
        PathBuf::from(std::env::var_os("HOME").unwrap_or_default()).join(".local/share")
    });
    let key = data.join("MyGame").join("peerova-client.key");

    let mut tunnel = match PeerovaTunnel::start(&exe, "pvt_...", "tcp:8888", &key, Duration::from_secs(30),
                                                |line| eprintln!("[peerova] {line}")) {
        Ok(t) => t,
        Err(e) => { eprintln!("Could not reach the server: {e}"); std::process::exit(1); }
    };
    let stream = std::net::TcpStream::connect("127.0.0.1:8888");   // your existing socket code, unchanged
    // ... game loop; tunnel.running() turns false if the connection is lost
    let _ = (stream, tunnel.running());
}   // dropping `tunnel` stops peerova
```

Notes:
- With several `--forward` options, change the check to wait for that many `server port` lines. Linux/macOS: `peerova` must be executable; macOS: codesign it with the app.
- Pick an unusual local port (`tcp:38888=8888`) if 8888 may be used by another program.
- `peerova client` exits when the connection is lost → `running()` becomes `false`. If the app is killed abruptly (`Drop` does not run), the `peerova` process may be left behind.
- Mobile can't spawn child processes → use `Sdk`. `peerova stdio` does **not** open local ports (it is the message SDK as JSON lines), so it does not fit this pattern.

### Players never type anything

Players never type a ticket or a code: embed the ticket in the program, or fetch the latest ticket from your own URL at startup (falling
back to the embedded one). 6-character codes are only for inviting friends to player-hosted rooms. Details and multi-server failover
(`peerova ticket merge`): [guide §2](../DEVELOPER-GUIDE.md#2-players-never-type-anything).

**Game servers:** several ports in one process, one `127.a.b.c` address per player + banning, hiding the server IP, VPN mode —
see [guide §7](../DEVELOPER-GUIDE.md#7-game-server-features).

### Security

- `server.key` is the server's identity: anyone who has it can impersonate the server. Keep it secret, do not commit it.
- Ticket / connect code are for sharing with clients (semi-public); `token` and `license` are secrets — never embed the license in builds you give to players.
- All traffic is end-to-end encrypted.

### Troubleshooting

- Network diagnosis: `peerova doctor` (the `peerova` command-line tool).
- `EV_CONNECT_FAILED` (`E_TIMEOUT`) after ~20 s: server not running, wrong ticket/code, or `token` mismatch (`E_ACCESS_DENIED`).
- The 4th client is refused (`E_CCU_LIMIT`): dev mode (no `license`), max 3 clients.
- An unreliable `send` returns `Err` (`E_TOO_LARGE`): payload larger than 1180 bytes.
- `E_IO`: the folder of `key_file` does not exist or is not writable.
- The server does not start with `hide_ip`: your plan does not include hiding the IP (Pro/Studio/Enterprise or the trial).

### FAQ

- **Do players need to type a code?** No. Embed the ticket or fetch it from your URL; 6-character codes are only for inviting friends.
- **Do I need port forwarding / a static IP?** No; an open port only makes direct connections faster.
- More: [guide FAQ](../DEVELOPER-GUIDE.md#16-faq-1), [Troubleshooting](../DEVELOPER-GUIDE.md#15-troubleshooting).
