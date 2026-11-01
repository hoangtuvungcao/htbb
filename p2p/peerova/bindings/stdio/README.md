# Peerova — sidecar stdio (mọi ngôn ngữ) / stdio sidecar (any language)

[Tiếng Việt](#tiếng-việt) · [English](#english)

---

## Tiếng Việt

`peerova stdio` chạy một node Peerova (server hoặc client) như một process con và trao đổi **JSON theo
dòng** qua stdin/stdout. Ngôn ngữ nào spawn được process và đọc/ghi được từng dòng là dùng được toàn bộ
SDK, không cần binding native: Perl, Ruby, Erlang/Elixir, Tcl, R, shell, Python không có ctypes, Node.js,
PHP không có ext/ffi, Lua thuần...

Đặc tả đầy đủ: [PROTOCOL.md](PROTOCOL.md). Khái niệm chung và C API: [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md).
Website: https://peerova.pages.dev

### Thư mục này có gì

| File | Nội dung |
|---|---|
| [PROTOCOL.md](PROTOCOL.md) | đặc tả giao thức (lệnh, event, mã thoát, mã hoá dữ liệu) |
| [peerova.sh](peerova.sh) | thư viện bash (≥ 4.1; dùng `jq` nếu có, không có vẫn chạy) |
| [chat.sh](chat.sh) | chat P2P viết bằng bash thuần |
| [examples/demo.pl](examples/demo.pl) | demo đồng bộ vị trí + chat bằng Perl 5 (chỉ dùng module có sẵn) |
| [test_stdio.sh](test_stdio.sh) | test tự động |

### Yêu cầu

- Binary `peerova` (công cụ dòng lệnh) — đây là "sidecar". Không cần `libpeerova` hay trình biên dịch.
- Ngôn ngữ của bạn có thể spawn process với stdin/stdout là pipe, và encode/decode base64url.

### Đặt binary ở đâu

Trong gói SDK: `lib/linux-x86_64/peerova` (Linux x86_64), `lib/windows-x86_64/peerova.exe` (Windows x86_64).

- **Linux**: chép `peerova` cạnh ứng dụng của bạn (nhớ `chmod +x`) hoặc vào một thư mục trong `PATH`.
- **Windows**: chép `peerova.exe` cạnh ứng dụng. `wintun.dll` **không** cần cho chế độ stdio (nó chỉ dùng cho
  chế độ VPN của công cụ dòng lệnh).
- **macOS / iOS / Android**: liên hệ Peerova để nhận bản build. Trên macOS đặt binary trong app bundle
  (ví dụ `Contents/MacOS/peerova`) hoặc cạnh ứng dụng. iOS không cho chạy process con — hãy dùng binding
  native (`../swift`). Android: dùng binding native (`../kotlin-android`).

Các client mẫu trong thư mục này tìm binary theo thứ tự: biến môi trường `PEEROVA_BIN` → bản trong gói SDK →
`peerova` trong `PATH`.

### Cài đặt

Không có bước cài đặt: chép binary, rồi trong code của bạn spawn `peerova stdio ...`. Với bash:
`source peerova.sh`.

### Bắt đầu nhanh (5 phút)

**Giao thức trong 6 dòng:**

1. Spawn: `peerova stdio server '<config json>'` hoặc `peerova stdio client <TICKET|MÃ> '<config json>'`.
2. Đọc dòng đầu tiên: phải là `{"event":"ready",...}`. Server: lấy `ticket` trong đó để gửi cho client.
   Gặp EOF trước dòng này = khởi động lỗi (lý do nằm ở stderr, mã thoát `1`).
3. Đọc các dòng event: `connected`, `message`, `disconnected`, `connect_failed`, `error`.
4. Ghi các dòng lệnh: `send`, `disconnect`, `quit`.
5. Trường `data` là **base64url không padding** (`A-Z a-z 0-9 - _`, không có `=`).
6. Dừng: ghi `{"cmd":"quit"}` (hoặc đóng stdin), chờ process thoát (mã `0`), quá ~3 giây thì kill.

**Thử bằng tay trong terminal:**

```sh
peerova stdio server '{"port":47000}'
# {"event":"ready","ticket":"pvt_..."}            <- gửi ticket này cho client

peerova stdio client pvt_...                       # terminal khác; hoặc mã kết nối 6 ký tự: peerova stdio client K7Q2XD
# {"event":"ready"}
# {"event":"connected","peer":2633379007}
{"cmd":"send","peer":2633379007,"channel":0,"reliable":true,"data":"aGVsbG8"}     <- bạn gõ ("hello")
```

**Mã kết nối 6 ký tự:** client có thể truyền mã kết nối (ví dụ `K7Q2XD`) thay cho ticket. Server nhận mã khi
khởi động với `app` (project id trên website Peerova) và `share`. Sidecar **không** in mã kết nối của server ra
stdout — xem mã trên dashboard của website, hoặc dùng binding native (`peerova_room_code`) nếu server cần đọc
mã trong code.

**Bash:**

```bash
source peerova.sh
pv_start srv server '{"port":0}' || exit 1        # đặt PV_TICKET
pv_start cli client "$PV_TICKET" || exit 1
pv_event cli 10 && [[ $EV == connected ]] && pv_send cli "$EV_PEER" 0 true "hello"
while pv_event srv 5; do [[ $EV == message ]] && { echo "got: $EV_TEXT"; break; }; done
pv_stop cli; pv_stop srv
```

**Perl** (đồng bộ vị trí 20 Hz qua kênh unreliable + chat reliable):

```sh
perl examples/demo.pl server              # in "ticket: pvt_..."
perl examples/demo.pl client pvt_... 10   # chạy 10 giây; bỏ số giây để chat tới khi Ctrl+C
```

### Gửi: reliable hay unreliable

| | `"reliable":true` | `"reliable":false` |
|---|---|---|
| Đảm bảo | có thứ tự, gửi lại khi mất (theo từng kênh) | datagram: có thể mất hoặc đến sai thứ tự |
| Kích thước | có thể lớn | tối đa **1180 byte** mỗi message (lớn hơn → event `error`) |
| Dùng cho | chat, inventory, RPC, từng phần file | input gameplay, vị trí, trạng thái thời gian thực kiểu voice — chỉ giá trị mới nhất có ý nghĩa |

Kênh `channel` là 0..255 (mặc định 0). Các kênh khác nhau không chặn nhau: một message reliable lớn trên kênh 0
không làm chậm kênh 1.

```json
{"cmd":"send","peer":N,"channel":1,"reliable":false,"data":"<base64url>"}
```

`send` thành công không in gì; lỗi (peer không tồn tại, datagram quá lớn...) đến sau dưới dạng
`{"event":"error","message":"..."}` — không làm chết node.

### Nhận event

| Event | Ý nghĩa |
|---|---|
| `connected` `peer` | Server: một client vừa kết nối. Client: đã kết nối tới server. |
| `message` `peer` `channel` `reliable` `data` | dữ liệu từ `peer` |
| `disconnected` `peer` `reason` | `peer` đã ngắt |
| `connect_failed` `reason` | client không kết nối được (hết thời gian, sai token, server đầy...) |
| `error` `message` | một lệnh bị từ chối, không nghiêm trọng |

`peer` là số nguyên không dấu 32 bit (tới 4294967295). `reason`/`message` là văn bản cho người đọc (có thể
là tiếng Việt) — ghi log, đừng phân tích.

**Vòng lặp:** đọc stdout của sidecar **liên tục** (pipe đầy sẽ làm sidecar dừng). Cách thông dụng: một
thread / goroutine / coroutine đọc từng dòng; hoặc `select`/`poll` trên pipe cùng các nguồn khác (như
`IO::Select` trong `examples/demo.pl`); hoặc đọc có timeout ngắn trong vòng lặp game (như `pv_event NAME 0.05`
trong `chat.sh`). Mỗi lệnh là một dòng ghi xong thì flush.

### Ngắt kết nối và dọn dẹp

- `{"cmd":"disconnect","peer":N}` đóng một kết nối; cả hai phía nhận `disconnected`.
- `{"cmd":"quit"}` hoặc đóng stdin → node dừng, process thoát mã `0`. Chờ process thoát; quá vài giây thì kill.
- Mỗi process là một node. Cần nhiều node thì chạy nhiều process. Mỗi process chỉ nên có một nơi ghi lệnh
  (hoặc khoá lại nếu ghi từ nhiều thread) để các dòng không bị trộn lẫn.

### Xử lý lỗi

| Tình huống | Cách nhận biết |
|---|---|
| Khởi động lỗi (config sai, ticket sai, port bận) | EOF trước `ready`, lý do ở stderr, mã thoát `1` |
| Không kết nối được | event `connect_failed` (sau `timeout_ms`, mặc định 20 giây) |
| Lệnh sai / peer không tồn tại / datagram > 1180 byte | event `error` |
| Không tìm thấy binary | lỗi spawn của ngôn ngữ bạn (ENOENT) — kiểm tra `PEEROVA_BIN` / `PATH` |

### Cấu hình

Truyền dạng JSON ở tham số cuối. **Server:**

| Khoá | Ý nghĩa |
|---|---|
| `port` | cổng UDP, mặc định `47000`; `0` = cổng ngẫu nhiên |
| `key_file` | file khoá danh tính, mặc định `"server.key"` (tự tạo nếu chưa có). Cùng khoá ⇒ cùng ticket |
| `license` | khoá license `PVA-...`. Không có → chế độ dev, tối đa 3 client |
| `max_ccu` | số client đồng thời tối đa |
| `token` | bí mật chung mà client phải gửi kèm |
| `upnp` | xin router mở cổng, mặc định `true` |
| `private` | ticket không chứa địa chỉ IP (chia sẻ ticket không lộ địa chỉ server), mặc định `false` |
| `app`, `share` | lấy mã kết nối 6 ký tự: `app` = project id trên website Peerova, `share` = `{"name","public","max","mode","region"}` |

**Client:** `token` (phải khớp với server), `timeout_ms` (mặc định `20000`).

```sh
peerova stdio server '{"port":47000,"key_file":"/var/lib/myapp/server.key","license":"PVA-...","token":"s3cret"}'
peerova stdio client pvt_... '{"token":"s3cret","timeout_ms":10000}'
```

Kết nối là tự động: ưu tiên kết nối trực tiếp, và tự động dùng đường dự phòng (vẫn mã hoá) khi mạng chặn kết
nối trực tiếp.

### Bảo mật

- `server.key` là danh tính của server: ai có file này có thể giả mạo server. Giữ bí mật, không commit vào git.
- Ticket có thể chia sẻ cho client (gần như công khai). `token` và `license` là bí mật.
- Mọi dữ liệu được mã hoá đầu-cuối.
- Tham số dòng lệnh có thể bị user khác trên cùng máy nhìn thấy (`ps`). Nếu máy dùng chung, đừng để
  `license`/`token` trong lệnh của bạn hiển thị cho người khác.

### Khắc phục sự cố

- Chạy `peerova doctor` để chẩn đoán mạng.
- **Không tìm thấy binary**: đặt `PEEROVA_BIN=/đường/dẫn/peerova` hoặc thêm vào `PATH`; trên Linux nhớ `chmod +x`.
- **`connect_failed` do hết thời gian**: kiểm tra ticket/mã còn đúng, server đang chạy, chạy `peerova doctor` ở
  cả hai máy.
- **Client thứ 4 bị từ chối**: server đang ở chế độ dev (tối đa 3 client) — thêm `license`.
- **`error` khi gửi unreliable**: payload > 1180 byte; dùng `"reliable":true` hoặc chia nhỏ.
- **Không thấy event nào**: chưa flush lệnh, hoặc bạn đọc stderr thay vì stdout.

### Test

```sh
./test_stdio.sh             # dùng jq nếu có
PV_NO_JQ=1 ./test_stdio.sh  # ép đường bash thuần
```

---

## English

`peerova stdio` runs one Peerova node (server or client) as a child process and talks
**newline-delimited JSON** on stdin/stdout. Any language that can spawn a process and read/write lines gets
the full SDK with no native binding: Perl, Ruby, Erlang/Elixir, Tcl, R, shell, Python without ctypes,
Node.js, PHP without ext/ffi, plain Lua...

Full specification: [PROTOCOL.md](PROTOCOL.md). Concepts and the C API: [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md).
Website: https://peerova.pages.dev

### What is in this folder

| File | Contents |
|---|---|
| [PROTOCOL.md](PROTOCOL.md) | protocol specification (commands, events, exit status, data encoding) |
| [peerova.sh](peerova.sh) | bash library (≥ 4.1; uses `jq` if installed, works without it) |
| [chat.sh](chat.sh) | P2P chat in plain bash |
| [examples/demo.pl](examples/demo.pl) | position-sync + chat demo in Perl 5 (core modules only) |
| [test_stdio.sh](test_stdio.sh) | automated test |

### Requirements

- The `peerova` binary (the command-line tool) — this is the "sidecar". No `libpeerova` or compiler needed.
- Your language can spawn a process with stdin/stdout as pipes, and encode/decode base64url.

### Where to put the binary

In the SDK package: `lib/linux-x86_64/peerova` (Linux x86_64), `lib/windows-x86_64/peerova.exe` (Windows x86_64).

- **Linux**: copy `peerova` next to your application (remember `chmod +x`) or into a directory on `PATH`.
- **Windows**: copy `peerova.exe` next to your application. `wintun.dll` is **not** needed for stdio mode (it is
  only used by the command-line tool's VPN mode).
- **macOS / iOS / Android**: contact Peerova for these builds. On macOS put the binary inside your app bundle
  (e.g. `Contents/MacOS/peerova`) or next to your application. iOS does not allow child processes — use the
  native binding (`../swift`). Android: use the native binding (`../kotlin-android`).

The sample clients in this folder look for the binary in this order: the `PEEROVA_BIN` environment variable →
the copy in the SDK package → `peerova` on `PATH`.

### Install

Nothing to install: copy the binary, then spawn `peerova stdio ...` from your code. In bash:
`source peerova.sh`.

### 5-minute quick start

**The protocol in 6 lines:**

1. Spawn `peerova stdio server '<config json>'` or `peerova stdio client <TICKET|CODE> '<config json>'`.
2. Read the first line: it must be `{"event":"ready",...}`. Server: take `ticket` from it and give it to clients.
   EOF before this line = startup failure (reason on stderr, exit status `1`).
3. Read event lines: `connected`, `message`, `disconnected`, `connect_failed`, `error`.
4. Write command lines: `send`, `disconnect`, `quit`.
5. The `data` field is **base64url without padding** (`A-Z a-z 0-9 - _`, no `=`).
6. Stop: write `{"cmd":"quit"}` (or close stdin), wait for the process to exit (status `0`), kill it after ~3 s.

**Try it by hand in a terminal:**

```sh
peerova stdio server '{"port":47000}'
# {"event":"ready","ticket":"pvt_..."}            <- give this ticket to clients

peerova stdio client pvt_...                       # another terminal; or a 6-char connect code: peerova stdio client K7Q2XD
# {"event":"ready"}
# {"event":"connected","peer":2633379007}
{"cmd":"send","peer":2633379007,"channel":0,"reliable":true,"data":"aGVsbG8"}     <- typed by you ("hello")
```

**6-character connect code:** a client can pass a connect code (e.g. `K7Q2XD`) instead of a ticket. The server
gets a code when it starts with `app` (project id from the Peerova website) and `share`. The sidecar does
**not** print the server's connect code on stdout — read it in the website dashboard, or use a native binding
(`peerova_room_code`) if your server needs it in code.

**Bash:**

```bash
source peerova.sh
pv_start srv server '{"port":0}' || exit 1        # sets PV_TICKET
pv_start cli client "$PV_TICKET" || exit 1
pv_event cli 10 && [[ $EV == connected ]] && pv_send cli "$EV_PEER" 0 true "hello"
while pv_event srv 5; do [[ $EV == message ]] && { echo "got: $EV_TEXT"; break; }; done
pv_stop cli; pv_stop srv
```

**Perl** (positions at 20 Hz on an unreliable channel + reliable chat):

```sh
perl examples/demo.pl server              # prints "ticket: pvt_..."
perl examples/demo.pl client pvt_... 10   # runs 10 seconds; omit the seconds to chat until Ctrl+C
```

### Sending: reliable or unreliable

| | `"reliable":true` | `"reliable":false` |
|---|---|---|
| Guarantees | ordered, retransmitted (per channel) | datagram: may be lost or reordered |
| Size | can be large | max **1180 bytes** per message (larger → `error` event) |
| Use for | chat, inventory, RPC, file chunks | gameplay inputs, positions, voice-like real-time state — only the latest value matters |

`channel` is 0..255 (default 0). Different channels don't block each other: a large reliable message on
channel 0 does not delay channel 1.

```json
{"cmd":"send","peer":N,"channel":1,"reliable":false,"data":"<base64url>"}
```

A successful `send` prints nothing; failures (unknown peer, datagram too large...) arrive later as
`{"event":"error","message":"..."}` — the node keeps running.

### Receiving events

| Event | Meaning |
|---|---|
| `connected` `peer` | Server: a client connected. Client: connected to the server. |
| `message` `peer` `channel` `reliable` `data` | payload from `peer` |
| `disconnected` `peer` `reason` | `peer` is gone |
| `connect_failed` `reason` | client could not connect (timeout, wrong token, server full...) |
| `error` `message` | a command was rejected; non-fatal |

`peer` is an unsigned 32-bit integer (up to 4294967295). `reason`/`message` are human-readable text (possibly
Vietnamese) — log them, don't parse them.

**The loop:** read the sidecar's stdout **continuously** (a full pipe stalls the sidecar). Common patterns: a
reader thread / goroutine / coroutine that handles one line at a time; `select`/`poll` on the pipe together
with your other inputs (like `IO::Select` in `examples/demo.pl`); or a read with a short timeout in your game
loop (like `pv_event NAME 0.05` in `chat.sh`). Write each command as one line and flush.

### Disconnect and cleanup

- `{"cmd":"disconnect","peer":N}` closes one connection; both sides get `disconnected`.
- `{"cmd":"quit"}` or closing stdin stops the node and the process exits with status `0`. Wait for it; kill it
  after a few seconds.
- One process is one node. Run several processes for several nodes. Have a single writer per process (or a
  lock if several threads write) so command lines never interleave.

### Error handling

| Situation | How you see it |
|---|---|
| Startup failed (bad config, bad ticket, port in use) | EOF before `ready`, reason on stderr, exit status `1` |
| Could not connect | `connect_failed` event (after `timeout_ms`, default 20 s) |
| Bad command / unknown peer / datagram > 1180 bytes | `error` event |
| Binary not found | your language's spawn error (ENOENT) — check `PEEROVA_BIN` / `PATH` |

### Configuration

Passed as JSON in the last argument. **Server:**

| Key | Meaning |
|---|---|
| `port` | UDP port, default `47000`; `0` = random free port |
| `key_file` | identity key file, default `"server.key"` (created if missing). Same key ⇒ same ticket |
| `license` | `PVA-...` license key. Without it: dev mode, max 3 clients |
| `max_ccu` | maximum concurrent clients |
| `token` | shared secret clients must present |
| `upnp` | ask the router to open the port, default `true` |
| `private` | the ticket contains no IP addresses (sharing it does not reveal the server's address), default `false` |
| `app`, `share` | get a 6-character connect code: `app` = project id from the Peerova website, `share` = `{"name","public","max","mode","region"}` |

**Client:** `token` (must match the server), `timeout_ms` (default `20000`).

```sh
peerova stdio server '{"port":47000,"key_file":"/var/lib/myapp/server.key","license":"PVA-...","token":"s3cret"}'
peerova stdio client pvt_... '{"token":"s3cret","timeout_ms":10000}'
```

Connections are automatic: direct first, with an automatic encrypted fallback route when networks block
direct connections.

### Security

- `server.key` is the server's identity: anyone who has it can impersonate the server. Keep it secret and never
  commit it.
- The ticket is meant to be shared with clients (public-ish). `token` and `license` are secrets.
- All traffic is end-to-end encrypted.
- Command-line arguments can be visible to other users on the same machine (`ps`). On shared machines, keep
  `license`/`token` out of sight of other users.

### Troubleshooting

- Run `peerova doctor` for a network diagnosis.
- **Binary not found**: set `PEEROVA_BIN=/path/to/peerova` or add it to `PATH`; on Linux remember `chmod +x`.
- **`connect_failed` with a timeout**: check the ticket/code is current and the server is running; run
  `peerova doctor` on both machines.
- **The 4th client is refused**: the server is in dev mode (max 3 clients) — add a `license`.
- **`error` when sending unreliable**: payload > 1180 bytes; use `"reliable":true` or split it.
- **No events at all**: commands not flushed, or you are reading stderr instead of stdout.

### Test

```sh
./test_stdio.sh             # uses jq if installed
PV_NO_JQ=1 ./test_stdio.sh  # forces the pure-bash JSON path
```
