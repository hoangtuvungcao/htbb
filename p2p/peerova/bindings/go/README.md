# Peerova cho Go

- [Tiếng Việt](#tiếng-việt)
- [English](#english)

---

## Tiếng Việt

Binding Go (cgo) cho Peerova: kết nối P2P trực tiếp, mã hoá đầu-cuối. Package `peerova`
(module `github.com/peerova/peerova-go`, tệp `peerova.go`) gọi thẳng vào thư viện lõi `libpeerova` qua C ABI.

Khái niệm (server, client, ticket, connect code, channel, sự kiện) và toàn bộ C API: [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md).
Website: https://peerova.pages.dev

### Yêu cầu

- Go 1.20 trở lên, bật cgo (`CGO_ENABLED=1`) và có trình biên dịch C (`gcc`/`clang`; Windows: MinGW-w64).
- Thư viện lõi trong gói SDK: `lib/linux-x86_64/libpeerova.so` (hoặc `libpeerova.a`), `lib/windows-x86_64/peerova.dll`.
  Header: `include/peerova.h` (bản sao: `bindings/c/peerova.h`, package dùng bản này).

### Đặt thư viện native ở đâu

| Hệ điều hành | Lúc build | Lúc chạy |
|---|---|---|
| Linux x86_64 | mặc định package link `lib/linux-x86_64/libpeerova.so` của gói SDK, kèm rpath tới thư mục đó | khi phát hành: đặt `libpeerova.so` cạnh file thực thi và build với rpath `$ORIGIN` (xem dưới), hoặc dùng `LD_LIBRARY_PATH` |
| Windows x86_64 | `CGO_LDFLAGS="-L<thư mục chứa peerova.dll> -lpeerova"` | `peerova.dll` cạnh file `.exe`. **Không** cần `wintun.dll` (chỉ công cụ dòng lệnh `peerova` cần nó cho chế độ VPN) |
| macOS | liên hệ Peerova để nhận bản build macOS | `libpeerova.dylib`; trong app bundle đặt ở `Contents/Frameworks` |
| Android / iOS | liên hệ Peerova | Android: `jniLibs/<abi>/libpeerova.so`; iOS: thư viện tĩnh `libpeerova.a` |

Đổi đường dẫn header/thư viện bằng `CGO_CFLAGS` / `CGO_LDFLAGS`. Ví dụ build bản phát hành Linux, thư viện nằm cạnh binary:

```sh
CGO_LDFLAGS='-L/opt/peerova/lib -lpeerova -Wl,-rpath,$ORIGIN' go build -o mygame .
cp /opt/peerova/lib/libpeerova.so .      # phát hành mygame + libpeerova.so cùng thư mục
```

### Cài đặt

Chép thư mục `bindings/go` vào dự án (hoặc giữ nguyên trong gói SDK) và trỏ module tới nó:

```sh
go mod edit -require=github.com/peerova/peerova-go@v0.0.0 -replace=github.com/peerova/peerova-go=/đường/dẫn/tới/bindings/go
```

```go
import "github.com/peerova/peerova-go" // package peerova
```

Kiểm tra: `cd bindings/go && go test -count=1 -v ./...` (in `PASS`).

### Bắt đầu nhanh (5 phút)

Server:

```go
srv, err := peerova.StartServer(`{"port":47000,"key_file":"server.key"}`) // chặn tối đa ~3 s
if err != nil { log.Fatal(err) }
defer srv.Close()
fmt.Println("ticket:", srv.Ticket()) // gửi chuỗi này cho client
for {
    ev, ok := srv.PollWait(100 * time.Millisecond)
    if !ok { continue }
    switch ev.Type {
    case peerova.Connected:
        srv.Send(ev.Peer, []byte("chào"), 0, true)
    case peerova.Message:
        fmt.Printf("%d: %s\n", ev.Peer, ev.Data)
    }
}
```

Ticket ổn định với cùng `key_file`.

Connect code: khởi động server với `"app"` (mã project trên website Peerova) và `"share"`, rồi gọi `srv.RoomCode()`
định kỳ tới khi `ok == true` (website cấp mã ngay sau khi server khởi động):

```go
srv, _ := peerova.StartServer(`{"app":"prj_xxx","share":{"name":"Phòng của Lan","public":true,"max":8}}`)
if code, ok := srv.RoomCode(); ok { fmt.Println("connect code:", code) } // ví dụ "K7Q2XD"
```

Client — truyền ticket **hoặc** connect code 6 ký tự:

```go
cli, err := peerova.Connect(ticketOrCode, "") // trả về ngay
if err != nil { log.Fatal(err) }
defer cli.Close()
ev, ok := cli.PollWait(20 * time.Second)
if !ok || ev.Type != peerova.Connected { log.Fatalf("không kết nối được: %s", ev.Data) }
cli.Send(ev.Peer, []byte("xin chào"), 0, true)
```

Với ticket, `Connect` trả về ngay. Với connect code, `Connect` tra mã trên website Peerova trước (một request HTTPS,
cần Internet) và trả lỗi nếu mã không tồn tại.

Kết nối là tự động: thử kết nối trực tiếp trước, và có đường dự phòng mã hoá tự động khi mạng chặn kết nối trực tiếp.

Ví dụ đầy đủ: [`examples/chat/main.go`](examples/chat/main.go) (chat + đồng bộ vị trí 20 Hz, tắt sạch bằng Ctrl+C):

```sh
go run ./examples/chat server 0 /tmp/demo.key      # in ticket
go run ./examples/chat client <TICKET>
```

### Gửi reliable hay unreliable

`Send(peer, data, channel, reliable)` — `channel` từ 0 đến 255.

| | `reliable = true` | `reliable = false` |
|---|---|---|
| Đảm bảo | tới nơi, đúng thứ tự trong cùng channel | có thể mất hoặc đảo thứ tự |
| Kích thước | có thể lớn | tối đa **1180 byte** (`peerova.MaxUnreliablePayload`); lớn hơn trả về lỗi |
| Dùng cho | chat, inventory, RPC, khối dữ liệu file | input, vị trí, trạng thái realtime kiểu voice — gói mới thay gói cũ |

Các channel độc lập: một tin reliable bị chậm trên channel 0 không chặn channel 1. Nên tách loại dữ liệu theo channel
(ví dụ 0 = chat, 1 = vị trí, 2 = RPC).

### Nhận sự kiện

`Poll()` (không chặn) hoặc `PollWait(timeout)` trả về `(Event, ok)`:

| `ev.Type` | Ý nghĩa | Trường dùng được |
|---|---|---|
| `peerova.Connected` | server: một client vừa vào; client: đã kết nối tới server | `Peer` |
| `peerova.Message` | có tin nhắn | `Peer`, `Channel`, `Reliable`, `Data` |
| `peerova.Disconnected` | peer rời đi | `Peer`, `Data` = lý do |
| `peerova.ConnectFailed` | (client) kết nối thất bại | `Data` = lý do |

`ev.Data` là bản sao Go, giữ lại thoải mái. Mẫu thường dùng: **một goroutine** chạy vòng `PollWait(50 * time.Millisecond)`,
đẩy sự kiện vào một `chan peerova.Event` hoặc xử lý tại chỗ; goroutine khác gọi `Send` thoải mái. Trong game loop:
mỗi frame `for ev, ok := n.Poll(); ok; ev, ok = n.Poll() { ... }`.

### Quy tắc goroutine

`Send`, `Poll`, `PollWait`, `Disconnect`, `PeerCount` an toàn khi gọi đồng thời. Chỉ chạy **một** goroutine poll cho mỗi node.
Không gọi `Close` khi goroutine khác còn đang poll: báo goroutine poll dừng (ví dụ đóng một channel `done`), chờ nó thoát, rồi mới `Close`.
(`Close` an toàn về bộ nhớ — nó chờ lời gọi đang chạy kết thúc — nhưng sẽ bị trễ bởi `PollWait` dài.)

### Ngắt kết nối và dọn dẹp

- `node.Disconnect(peer)` đóng một kết nối (sau đó có sự kiện `Disconnected`).
- `node.Close()` ngắt mọi peer và giải phóng node native; gọi nhiều lần không sao. Sau đó `Send` trả `peerova.ErrClosed`,
  `Poll` trả `ok == false`.
- Trước khi đóng client, chờ vài trăm ms để tin reliable vừa gửi kịp tới nơi.

### Xử lý lỗi

- `StartServer`, `Connect`, `Send` trả `error`; nội dung là văn bản lỗi từ thư viện (`peerova_last_error`), có tiền tố `peerova: `.
- `peerova.ErrClosed`: dùng node đã đóng.
- Lỗi kết nối không trả về từ `Connect` mà tới qua sự kiện `ConnectFailed`.

### Cấu hình

Server (`StartServer(json)`, `json` có thể là `""`):

| Khoá | Mặc định | Ý nghĩa |
|---|---|---|
| `port` | `47000` | cổng lắng nghe; `0` = cổng ngẫu nhiên |
| `key_file` | `"server.key"` | file khoá định danh server; cùng khoá ⇒ cùng ticket. Được tạo nếu chưa có |
| `license` | — | license key `PVA-...`. Không có: chế độ dev, tối đa 3 client |
| `max_ccu` | theo license | giới hạn số client đồng thời |
| `token` | — | bí mật chung; client phải gửi đúng `token` mới được vào |
| `upnp` | `true` | tự mở cổng trên router qua UPnP |
| `private` | `false` | ticket riêng tư: ticket không chứa địa chỉ IP của server |
| `app` | — | mã project trên website Peerova (cần cho connect code) |
| `share` | — | `{"name":"...","public":true,"max":16,"mode":"...","region":"VN"}`: thông tin đăng lên website để nhận connect code |

Client (`Connect(ticket, json)`): `token` (phải khớp server), `timeout_ms` (mặc định `20000`).

### Bảo mật

- `server.key` là danh tính của server: ai có file này có thể giả làm server của bạn. Giữ bí mật, **không commit** vào git.
- Ticket và connect code dùng để chia sẻ cho client, coi như thông tin bán công khai.
- `token` và `license` là bí mật: không nhúng vào client phát hành công khai nếu không cần.
- Toàn bộ dữ liệu được mã hoá đầu-cuối.

### Khắc phục sự cố

- Chẩn đoán mạng: chạy `peerova doctor` bằng công cụ dòng lệnh `peerova` (có trong `lib/<nền tảng>/` của gói SDK).
- `error while loading shared libraries: libpeerova.so` khi chạy: đặt `libpeerova.so` cạnh binary (build với rpath `$ORIGIN`) hoặc đặt `LD_LIBRARY_PATH`.
  Windows: `peerova.dll` phải nằm cạnh `.exe`.
- `peerova.h: No such file` / `cannot find -lpeerova` khi build: đặt `CGO_CFLAGS=-I<include>` và `CGO_LDFLAGS=-L<lib> -lpeerova`; kiểm tra `CGO_ENABLED=1`.
- `ConnectFailed` sau khoảng 20 giây (timeout): server chưa chạy, ticket/code sai, hoặc `token` không khớp. Chạy `peerova doctor` ở cả hai máy.
- Client thứ 4 bị từ chối: server đang ở chế độ dev (không có `license`), tối đa 3 client.
- `Send` unreliable trả lỗi: payload vượt 1180 byte — chia nhỏ hoặc gửi reliable.

---

## English

Go (cgo) binding for Peerova: direct P2P connections, end-to-end encrypted. Package `peerova`
(module `github.com/peerova/peerova-go`, file `peerova.go`) calls the core library `libpeerova` directly through the C ABI.

Concepts (server, client, ticket, connect code, channels, events) and the full C API: [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md).
Website: https://peerova.pages.dev

### Requirements

- Go 1.20 or newer with cgo enabled (`CGO_ENABLED=1`) and a C compiler (`gcc`/`clang`; Windows: MinGW-w64).
- Core library from the SDK package: `lib/linux-x86_64/libpeerova.so` (or `libpeerova.a`), `lib/windows-x86_64/peerova.dll`.
  Header: `include/peerova.h` (copy: `bindings/c/peerova.h`, which the package uses).

### Where to put the native library

| OS | Build time | Run time |
|---|---|---|
| Linux x86_64 | by default the package links the SDK's `lib/linux-x86_64/libpeerova.so`, with an rpath to that directory | for distribution: put `libpeerova.so` next to the executable and build with rpath `$ORIGIN` (see below), or use `LD_LIBRARY_PATH` |
| Windows x86_64 | `CGO_LDFLAGS="-L<dir containing peerova.dll> -lpeerova"` | `peerova.dll` next to the `.exe`. `wintun.dll` is **not** needed (only the `peerova` command-line tool needs it, for its VPN mode) |
| macOS | contact Peerova for macOS builds | `libpeerova.dylib`; inside an app bundle put it in `Contents/Frameworks` |
| Android / iOS | contact Peerova | Android: `jniLibs/<abi>/libpeerova.so`; iOS: static library `libpeerova.a` |

Override header/library locations with `CGO_CFLAGS` / `CGO_LDFLAGS`. Example release build on Linux with the library next to the binary:

```sh
CGO_LDFLAGS='-L/opt/peerova/lib -lpeerova -Wl,-rpath,$ORIGIN' go build -o mygame .
cp /opt/peerova/lib/libpeerova.so .      # ship mygame + libpeerova.so in the same directory
```

### Install

Copy the `bindings/go` directory into your project (or keep it inside the SDK package) and point your module at it:

```sh
go mod edit -require=github.com/peerova/peerova-go@v0.0.0 -replace=github.com/peerova/peerova-go=/path/to/bindings/go
```

```go
import "github.com/peerova/peerova-go" // package peerova
```

Check: `cd bindings/go && go test -count=1 -v ./...` (prints `PASS`).

### 5-minute quick start

Server:

```go
srv, err := peerova.StartServer(`{"port":47000,"key_file":"server.key"}`) // blocks up to ~3 s
if err != nil { log.Fatal(err) }
defer srv.Close()
fmt.Println("ticket:", srv.Ticket()) // give this string to clients
for {
    ev, ok := srv.PollWait(100 * time.Millisecond)
    if !ok { continue }
    switch ev.Type {
    case peerova.Connected:
        srv.Send(ev.Peer, []byte("welcome"), 0, true)
    case peerova.Message:
        fmt.Printf("%d: %s\n", ev.Peer, ev.Data)
    }
}
```

The ticket is stable for the same `key_file`.

Connect code: start the server with `"app"` (your project id from the Peerova website) and `"share"`, then call
`srv.RoomCode()` periodically until `ok == true` (the website assigns it shortly after the server starts):

```go
srv, _ := peerova.StartServer(`{"app":"prj_xxx","share":{"name":"Lan's room","public":true,"max":8}}`)
if code, ok := srv.RoomCode(); ok { fmt.Println("connect code:", code) } // e.g. "K7Q2XD"
```

Client — pass the ticket **or** the 6-character connect code:

```go
cli, err := peerova.Connect(ticketOrCode, "") // returns immediately
if err != nil { log.Fatal(err) }
defer cli.Close()
ev, ok := cli.PollWait(20 * time.Second)
if !ok || ev.Type != peerova.Connected { log.Fatalf("could not connect: %s", ev.Data) }
cli.Send(ev.Peer, []byte("hello"), 0, true)
```

With a ticket, `Connect` returns immediately. With a connect code, `Connect` first looks the code up on the Peerova
website (one HTTPS request, needs Internet) and returns an error if the code does not exist.

Connections are automatic: direct first, with an automatic encrypted fallback route when networks block direct connections.

Complete example: [`examples/chat/main.go`](examples/chat/main.go) (chat + 20 Hz position sync, clean shutdown on Ctrl+C):

```sh
go run ./examples/chat server 0 /tmp/demo.key      # prints the ticket
go run ./examples/chat client <TICKET>
```

### Reliable vs unreliable

`Send(peer, data, channel, reliable)` — `channel` is 0 to 255.

| | `reliable = true` | `reliable = false` |
|---|---|---|
| Guarantee | delivered, in order within the channel | may be lost or reordered |
| Size | can be large | at most **1180 bytes** (`peerova.MaxUnreliablePayload`); larger returns an error |
| Use for | chat, inventory, RPCs, file chunks | inputs, positions, voice-like realtime state — newer packets replace older ones |

Channels are independent: a delayed reliable message on channel 0 does not block channel 1. Split data kinds across
channels (e.g. 0 = chat, 1 = positions, 2 = RPC).

### Receiving events

`Poll()` (non-blocking) or `PollWait(timeout)` returns `(Event, ok)`:

| `ev.Type` | Meaning | Fields |
|---|---|---|
| `peerova.Connected` | server: a client joined; client: connected to the server | `Peer` |
| `peerova.Message` | a message arrived | `Peer`, `Channel`, `Reliable`, `Data` |
| `peerova.Disconnected` | a peer left | `Peer`, `Data` = reason |
| `peerova.ConnectFailed` | (client) the connection attempt failed | `Data` = reason |

`ev.Data` is a Go copy and may be kept. Common pattern: **one goroutine** loops on `PollWait(50 * time.Millisecond)`
and either pushes events into a `chan peerova.Event` or handles them in place; other goroutines may call `Send` freely.
In a game loop: each frame `for ev, ok := n.Poll(); ok; ev, ok = n.Poll() { ... }`.

### Goroutine rules

`Send`, `Poll`, `PollWait`, `Disconnect`, `PeerCount` are safe to call concurrently. Run only **one** polling goroutine per node.
Do not call `Close` while another goroutine is still polling: signal the poller to stop (e.g. close a `done` channel),
wait for it to exit, then `Close`. (`Close` is memory-safe — it waits for in-flight calls — but a long `PollWait` delays it.)

### Disconnect and cleanup

- `node.Disconnect(peer)` closes one connection (a `Disconnected` event follows).
- `node.Close()` disconnects all peers and frees the native node; calling it twice is fine. Afterwards `Send` returns
  `peerova.ErrClosed` and `Poll` returns `ok == false`.
- Before closing a client, wait a few hundred ms so reliable messages you just sent can arrive.

### Error handling

- `StartServer`, `Connect`, `Send` return an `error` whose text is the library's error message (`peerova_last_error`), prefixed with `peerova: `.
- `peerova.ErrClosed`: the node was closed.
- Connection failures are not returned by `Connect`; they arrive as a `ConnectFailed` event.

### Configuration

Server (`StartServer(json)`, `json` may be `""`):

| Key | Default | Meaning |
|---|---|---|
| `port` | `47000` | listen port; `0` = random port |
| `key_file` | `"server.key"` | server identity key file; same key ⇒ same ticket. Created if missing |
| `license` | — | `PVA-...` license key. Without it: dev mode, max 3 clients |
| `max_ccu` | from license | cap on concurrent clients |
| `token` | — | shared secret; clients must present the same `token` |
| `upnp` | `true` | open the port on the router via UPnP |
| `private` | `false` | private ticket: the ticket does not contain the server's IP addresses |
| `app` | — | project id from the Peerova website (needed for connect codes) |
| `share` | — | `{"name":"...","public":true,"max":16,"mode":"...","region":"VN"}`: listing published on the website to get a connect code |

Client (`Connect(ticket, json)`): `token` (must match the server), `timeout_ms` (default `20000`).

### Security

- `server.key` is the server's identity: anyone with this file can impersonate your server. Keep it secret, **do not commit** it.
- The ticket and connect code are meant to be shared with clients; treat them as semi-public.
- `token` and `license` are secrets: do not embed them in publicly distributed clients unless required.
- All traffic is end-to-end encrypted.

### Troubleshooting

- Network diagnosis: run `peerova doctor` with the `peerova` command-line tool (in the SDK package's `lib/<platform>/`).
- `error while loading shared libraries: libpeerova.so` at run time: put `libpeerova.so` next to the binary (built with rpath `$ORIGIN`) or set `LD_LIBRARY_PATH`.
  Windows: `peerova.dll` must sit next to the `.exe`.
- `peerova.h: No such file` / `cannot find -lpeerova` at build time: set `CGO_CFLAGS=-I<include>` and `CGO_LDFLAGS=-L<lib> -lpeerova`; check `CGO_ENABLED=1`.
- `ConnectFailed` after about 20 seconds (timeout): server not running, wrong ticket/code, or `token` mismatch. Run `peerova doctor` on both machines.
- The 4th client is refused: the server is in dev mode (no `license`), max 3 clients.
- An unreliable `Send` returns an error: payload larger than 1180 bytes — split it or send it reliably.
