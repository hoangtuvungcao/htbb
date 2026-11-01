# Peerova — sidecar stdio (mọi ngôn ngữ) / stdio sidecar (any language)

[Tiếng Việt](#tiếng-việt) · [English](#english)

---

## Tiếng Việt

`peerova stdio` chạy một node Peerova (server hoặc client) như một process con và trao đổi **JSON theo
dòng** qua stdin/stdout. Ngôn ngữ nào spawn được process và đọc/ghi được từng dòng là dùng được toàn bộ
SDK, không cần binding native: Perl, Ruby, Erlang/Elixir, Tcl, R, shell, Python không có ctypes, Node.js,
PHP không có ext/ffi, Lua thuần...

Đặc tả: [PROTOCOL.md](PROTOCOL.md) (README này là bản mới nhất về trường `code`, event `error` khi khởi động lỗi
và mã thoát). Hướng dẫn chung: [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md). Website: https://peerova.pages.dev

### Chọn cách tích hợp

- `peerova stdio` là **SDK message** (cách 3/4 trong [hướng dẫn §1](../DEVELOPER-GUIDE.md#1-chọn-cách-tích-hợp)): bạn gửi/nhận
  message reliable/unreliable bằng lệnh và event JSON. Nó **không mở cổng local** nào trên `127.0.0.1`.
- Game/app **có sẵn** dùng socket TCP/UDP và không muốn sửa code (cách 1/2): dùng `peerova client --forward` thay cho
  `stdio` — xem [Game có sẵn: chạy peerova ẩn](#game-có-sẵn-chạy-peerova-ẩn).
- Ngôn ngữ có binding native (C#, Java, Python, Go, Node.js…) cho độ trễ thấp hơn — xem README của ngôn ngữ đó.

### Thư mục này có gì

| File | Nội dung |
|---|---|
| [PROTOCOL.md](PROTOCOL.md) | đặc tả giao thức (lệnh, event, mã hoá dữ liệu) |
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
2. Đọc dòng đầu tiên: bình thường là `{"event":"ready",...}`. Server: lấy `ticket` trong đó để gửi cho client.
   Khởi động lỗi: dòng đầu là **một** event `{"event":"error","message":"E_...: ...","code":"E_..."}`, rồi EOF và mã thoát `1`
   (một số lỗi tham số dòng lệnh chỉ in ra stderr — EOF trước `ready` luôn nghĩa là khởi động lỗi).
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
mã trong code. Mã chỉ dùng cho "mời bạn bè" — xem [Người chơi không phải gõ gì](#người-chơi-không-phải-gõ-gì).

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
| Kích thước | có thể lớn | tối đa **1180 byte** mỗi message (lớn hơn → event `error` với `code` `E_TOO_LARGE`) |
| Dùng cho | chat, inventory, RPC, từng phần file | input gameplay, vị trí, trạng thái thời gian thực kiểu voice — chỉ giá trị mới nhất có ý nghĩa |

Kênh `channel` là 0..255 (mặc định 0). Các kênh khác nhau không chặn nhau: một message reliable lớn trên kênh 0
không làm chậm kênh 1.

```json
{"cmd":"send","peer":N,"channel":1,"reliable":false,"data":"<base64url>"}
```

`send` thành công không in gì; lỗi (peer không tồn tại, datagram quá lớn...) đến sau dưới dạng
`{"event":"error","message":"E_UNKNOWN_PEER: ...","code":"E_UNKNOWN_PEER"}` — không làm chết node.

### Nhận event

| Event | Trường | Ý nghĩa |
|---|---|---|
| `ready` | `ticket` (chỉ server) | node đã chạy; luôn là dòng đầu khi khởi động thành công |
| `connected` | `peer` | Server: một client vừa kết nối. Client: đã kết nối tới server. |
| `message` | `peer` `channel` `reliable` `data` | dữ liệu từ `peer` |
| `disconnected` | `peer` `reason` `code` | `peer` đã ngắt (thường `E_CLOSED`, `E_TIMEOUT`) |
| `connect_failed` | `reason` `code` | client không kết nối được (`E_TIMEOUT`, `E_ACCESS_DENIED`, `E_CCU_LIMIT`...). Process **vẫn chạy** — gửi `quit` rồi thử lại nếu muốn |
| `error` | `message` `code` | một lệnh bị từ chối, không nghiêm trọng; hoặc khởi động lỗi (dòng duy nhất trước khi thoát mã `1`) |

Ví dụ thật:

```json
{"event":"disconnected","peer":3409873815,"reason":"E_CLOSED: the peer closed the connection","code":"E_CLOSED"}
{"event":"connect_failed","reason":"E_TIMEOUT: could not connect: timed out. Run `peerova doctor` on both machines ...","code":"E_TIMEOUT"}
{"event":"error","message":"E_TOO_LARGE: unreliable messages are limited to 1180 bytes","code":"E_TOO_LARGE"}
```

`peer` là số nguyên không dấu 32 bit (tới 4294967295). `reason`/`message` là văn bản **tiếng Anh** dạng
`"E_NAME: ..."` (để ghi log); `code` là tên mã ổn định (ví dụ `"E_UNKNOWN_PEER"`, `"E_TIMEOUT"`) — xử lý theo `code`,
đừng phân tích văn bản. Bảng mã: [Mã lỗi](../DEVELOPER-GUIDE.md#mã-lỗi). Có thể có thêm trường mới trong bản sau: bỏ qua
trường bạn không biết.

**Vòng lặp:** đọc stdout của sidecar **liên tục** (pipe đầy sẽ làm sidecar dừng). Cách thông dụng: một
thread / goroutine / coroutine đọc từng dòng; hoặc `select`/`poll` trên pipe cùng các nguồn khác (như
`IO::Select` trong `examples/demo.pl`); hoặc đọc có timeout ngắn trong vòng lặp game (như `pv_event NAME 0.05`
trong `chat.sh`). Mỗi lệnh là một dòng ghi xong thì flush. stdout chỉ có dòng giao thức; stderr là log tiếng Việt
cho người đọc — ghi vào file log hoặc bỏ đi, đừng phân tích.

### Ngắt kết nối và dọn dẹp

- `{"cmd":"disconnect","peer":N}` đóng một kết nối; cả hai phía nhận `disconnected`.
- `{"cmd":"quit"}` hoặc đóng stdin → node dừng, process thoát mã `0`. Chờ process thoát; quá vài giây thì kill.
- Mỗi process là một node. Cần nhiều node thì chạy nhiều process. Mỗi process chỉ nên có một nơi ghi lệnh
  (hoặc khoá lại nếu ghi từ nhiều thread) để các dòng không bị trộn lẫn.

### Xử lý lỗi

| Tình huống | Cách nhận biết |
|---|---|
| Khởi động lỗi (config JSON sai, ticket sai, port bận, license...) | **một** dòng `{"event":"error","message":"E_...: ...","code":"E_..."}` thay cho `ready`, rồi EOF, mã thoát `1`; lý do tiếng Việt ở stderr |
| Sai tham số dòng lệnh (thiếu ticket...), mã kết nối không tồn tại | EOF trước `ready` (không có dòng JSON), mã thoát `1`, lý do ở stderr |
| Không kết nối được | event `connect_failed` có `code` (sau `timeout_ms`, mặc định 20 giây); process vẫn chạy tới khi bạn gửi `quit` |
| Mất kết nối | event `disconnected` có `code` |
| Lệnh sai / peer không tồn tại / datagram > 1180 byte | event `error`: `E_INVALID_ARG` / `E_UNKNOWN_PEER` / `E_TOO_LARGE` |
| Không tìm thấy binary | lỗi spawn của ngôn ngữ bạn (ENOENT) — kiểm tra `PEEROVA_BIN` / `PATH` |

### Cấu hình

Truyền dạng JSON ở tham số cuối. **Server:**

| Khoá | Ý nghĩa |
|---|---|
| `port` | cổng UDP, mặc định `47000`; `0` = cổng ngẫu nhiên |
| `key_file` | file khoá danh tính, mặc định `"server.key"` (tự tạo nếu chưa có). Cùng khoá ⇒ cùng ticket. **Bí mật** |
| `license` | khoá license `PVA-...`. Không có → chế độ dev, tối đa 3 client |
| `max_ccu` | số client đồng thời tối đa (không vượt license) |
| `token` | bí mật chung mà client phải gửi kèm |
| `upnp` | xin router mở cổng, mặc định `true` |
| `private` | ticket không chứa địa chỉ IP, mặc định `false` (khi kết nối trực tiếp người chơi vẫn biết IP — muốn giấu thật dùng `hide_ip`) |
| `hide_ip` | Ẩn IP máy chủ: người chơi không bao giờ biết IP thật của máy chủ (chống DDoS); ping cao hơn một chút. Gói Pro/Studio/Enterprise và bản dùng thử. Mặc định `false` |
| `app` | project id trên website Peerova — cần để có mã kết nối 6 ký tự |
| `share` | `{"name","public","max","mode","region"}` — thông tin hiện kèm mã kết nối; `public` = hiện trong danh sách phòng công khai |

**Client:**

| Khoá | Ý nghĩa |
|---|---|
| `token` | phải khớp với server |
| `timeout_ms` | thời gian tối đa trước `connect_failed`, mặc định `20000` |
| `key_file` | file danh tính của người chơi, tự tạo nếu chưa có (thư mục phải tồn tại). Lưu trong thư mục dữ liệu ứng dụng của người dùng, ví dụ `"<appdata>/<game>/peerova-client.key"`. Không đặt → mỗi lần chạy một danh tính mới. Cấm theo địa chỉ chỉ có tác dụng với từng người chơi khi họ còn giữ file này |

```sh
peerova stdio server '{"port":47000,"key_file":"/var/lib/myapp/server.key","license":"PVA-...","token":"s3cret"}'
peerova stdio client pvt_... '{"token":"s3cret","timeout_ms":10000,"key_file":"<appdata>/MyGame/peerova-client.key"}'
```

Kết nối là tự động: ưu tiên kết nối trực tiếp; nếu mạng chặn, Peerova tự dùng **đường mã hoá tự động** (vẫn mã hoá
đầu-cuối) rồi chuyển sang trực tiếp khi có thể.

### Người chơi không phải gõ gì

Người chơi không cần gõ ticket hay mã: nhúng ticket vào game, hoặc tải ticket mới nhất từ URL của bạn khi game khởi động
(dùng ticket nhúng nếu tải lỗi). Mã 6 ký tự chỉ dùng để mời bạn bè vào phòng do người chơi mở. Chi tiết:
[hướng dẫn §2](../DEVELOPER-GUIDE.md#2-người-chơi-không-phải-gõ-gì).

### Game có sẵn: chạy peerova ẩn

`peerova stdio` **không** mở cổng local nào. Nếu chương trình của bạn đã có code socket kết nối tới máy chủ
(`127.0.0.1:8888`…), hãy chạy `peerova client <TICKET> --forward tcp:8888` ở nền thay vì `stdio`: process đó mở cổng
`127.0.0.1:8888` trên máy người chơi, code socket cũ không đổi gì. Máy chủ chạy `peerova server --expose tcp:8888`.

Cách làm: chạy ẩn, đọc stdout+stderr (UTF-8), **sẵn sàng** khi có dòng chứa chuỗi ASCII `server port`
(`✔ Tcp 127.0.0.1:8888 → server port 8888`, mỗi `--forward` một dòng); thất bại nếu process thoát trước (mã `1`, stderr
bắt đầu bằng `lỗi:`) hoặc quá 30 giây; kill process khi chương trình thoát. Ví dụ bash (Linux/macOS):

```bash
#!/usr/bin/env bash
# Start `peerova client --forward` in the background, wait until the local port is open, kill it on exit.
PEEROVA=${PEEROVA_BIN:-./peerova}
TICKET=${1:?usage: tunnel.sh <TICKET>}
FORWARD=${FORWARD:-tcp:8888}
KEY="${XDG_DATA_HOME:-$HOME/.local/share}/MyApp/peerova-client.key"
mkdir -p "$(dirname "$KEY")"

LOG=$(mktemp)
"$PEEROVA" client "$TICKET" --forward "$FORWARD" --key "$KEY" >"$LOG" 2>&1 </dev/null &
PV_PID=$!
trap 'kill "$PV_PID" 2>/dev/null; rm -f "$LOG"' EXIT

# READY = a line containing "server port" ("✔ Tcp 127.0.0.1:8888 → server port 8888"), 30 s max
for _ in $(seq 300); do
  grep -q "server port" "$LOG" && break
  kill -0 "$PV_PID" 2>/dev/null || { cat "$LOG" >&2; echo "peerova exited before connecting" >&2; exit 1; }
  sleep 0.1
done
grep -q "server port" "$LOG" || { echo "peerova: timed out" >&2; exit 1; }

# Your existing socket code / program, unchanged:
curl -s http://127.0.0.1:8888/
```

- `--key` giữ danh tính người chơi qua các lần chạy (máy chủ thấy cùng một địa chỉ, cấm người chơi hoạt động đúng).
- `peerova client` thoát khi mất kết nối (trừ khi thêm `--reconnect`); `--token SECRET` nếu máy chủ yêu cầu token.
- Mẫu cho C#, Unity, Java, Node.js/Electron, Python, Go, C++ (ẩn cửa sổ console trên Windows):
  [hướng dẫn §4](../DEVELOPER-GUIDE.md#4-game-tự-chạy-peerova-ẩn-người-chơi-không-chạy-gì).
- Điện thoại / console không chạy được process con — dùng binding native (SDK).

### Máy chủ game

Nhiều cổng trong một process, tách Remote Desktop cho admin, mỗi người chơi một địa chỉ `127.a.b.c` + `--banned`,
`--hide-ip`, chế độ VPN: xem [hướng dẫn §7](../DEVELOPER-GUIDE.md#7-tính-năng-máy-chủ-game).

### Bảo mật

- `server.key` là danh tính của server: ai có file này có thể giả mạo server. Giữ bí mật, không commit vào git.
- Ticket có thể chia sẻ cho client (không phải bí mật). `token` và `license` là bí mật.
- Mọi dữ liệu được mã hoá đầu-cuối.
- Tham số dòng lệnh có thể bị user khác trên cùng máy nhìn thấy (`ps`). Nếu máy dùng chung, đừng để
  `license`/`token` trong lệnh của bạn hiển thị cho người khác.

### Khắc phục sự cố

- Chạy `peerova doctor` (ở cả hai máy) để chẩn đoán mạng.
- **Không tìm thấy binary**: đặt `PEEROVA_BIN=/đường/dẫn/peerova` hoặc thêm vào `PATH`; trên Linux nhớ `chmod +x`.
- **`connect_failed` với `code` `E_TIMEOUT`**: kiểm tra ticket/mã còn đúng, server đang chạy, chạy `peerova doctor` ở
  cả hai máy.
- **Client thứ 4 bị từ chối (`E_CCU_LIMIT`)**: server đang ở chế độ dev (tối đa 3 client) — thêm `license`.
- **`error` `E_TOO_LARGE` khi gửi unreliable**: payload > 1180 byte; dùng `"reliable":true` hoặc chia nhỏ.
- **`E_IO` khi khởi động**: thư mục của `key_file` không tồn tại / không ghi được.
- **Không thấy event nào**: chưa flush lệnh, hoặc bạn đọc stderr thay vì stdout.
- **Code socket cũ không kết nối được tới `127.0.0.1`**: `stdio` không mở cổng — dùng `peerova client --forward` (ở trên).

### FAQ

- **Người chơi có phải gõ mã không?** Không. Nhúng ticket hoặc tải từ URL của bạn; mã 6 ký tự chỉ để mời bạn bè.
- **`stdio` có thay được `peerova client --forward` không?** Không — `stdio` trao đổi message, không mở cổng local.
- **Độ trễ?** Sidecar thêm vài ms + base64 mỗi gói; game cần độ trễ thấp nhất nên dùng binding native.

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

Specification: [PROTOCOL.md](PROTOCOL.md) (this README is the most current reference for the `code` field, the startup
`error` event and exit status). General guide: [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md). Website: https://peerova.pages.dev

### Choosing an integration

- `peerova stdio` is the **message SDK** (options 3/4 in [guide §1](../DEVELOPER-GUIDE.md#1-choosing-an-integration)): you
  send/receive reliable/unreliable messages with JSON commands and events. It does **not open any local port** on `127.0.0.1`.
- An **existing** game/app with TCP/UDP socket code you don't want to change (options 1/2): use `peerova client --forward`
  instead of `stdio` — see [Existing game: run peerova hidden](#existing-game-run-peerova-hidden).
- Languages with a native binding (C#, Java, Python, Go, Node.js…) get lower latency — see that language's README.

### What is in this folder

| File | Contents |
|---|---|
| [PROTOCOL.md](PROTOCOL.md) | protocol specification (commands, events, data encoding) |
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
2. Read the first line: normally `{"event":"ready",...}`. Server: take `ticket` from it and give it to clients.
   Startup failure: the first line is **one** `{"event":"error","message":"E_...: ...","code":"E_..."}` event, then EOF and
   exit status `1` (a few command-line argument errors are printed on stderr only — EOF before `ready` always means startup failed).
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
(`peerova_room_code`) if your server needs it in code. Codes are only for "invite a friend" — see
[Players never type anything](#players-never-type-anything).

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
| Size | can be large | max **1180 bytes** per message (larger → `error` event with `code` `E_TOO_LARGE`) |
| Use for | chat, inventory, RPC, file chunks | gameplay inputs, positions, voice-like real-time state — only the latest value matters |

`channel` is 0..255 (default 0). Different channels don't block each other: a large reliable message on
channel 0 does not delay channel 1.

```json
{"cmd":"send","peer":N,"channel":1,"reliable":false,"data":"<base64url>"}
```

A successful `send` prints nothing; failures (unknown peer, datagram too large...) arrive later as
`{"event":"error","message":"E_UNKNOWN_PEER: ...","code":"E_UNKNOWN_PEER"}` — the node keeps running.

### Receiving events

| Event | Fields | Meaning |
|---|---|---|
| `ready` | `ticket` (server only) | the node is running; always the first line after a successful start |
| `connected` | `peer` | Server: a client connected. Client: connected to the server. |
| `message` | `peer` `channel` `reliable` `data` | payload from `peer` |
| `disconnected` | `peer` `reason` `code` | `peer` is gone (usually `E_CLOSED`, `E_TIMEOUT`) |
| `connect_failed` | `reason` `code` | client could not connect (`E_TIMEOUT`, `E_ACCESS_DENIED`, `E_CCU_LIMIT`...). The process **keeps running** — send `quit`, then retry if you like |
| `error` | `message` `code` | a command was rejected, non-fatal; or startup failed (the only line before exit status `1`) |

Real examples:

```json
{"event":"disconnected","peer":3409873815,"reason":"E_CLOSED: the peer closed the connection","code":"E_CLOSED"}
{"event":"connect_failed","reason":"E_TIMEOUT: could not connect: timed out. Run `peerova doctor` on both machines ...","code":"E_TIMEOUT"}
{"event":"error","message":"E_TOO_LARGE: unreliable messages are limited to 1180 bytes","code":"E_TOO_LARGE"}
```

`peer` is an unsigned 32-bit integer (up to 4294967295). `reason`/`message` are **English** text `"E_NAME: ..."` (for logs);
`code` is the stable code name (e.g. `"E_UNKNOWN_PEER"`, `"E_TIMEOUT"`) — branch on `code`, don't parse the text.
Code table: [Error codes](../DEVELOPER-GUIDE.md#error-codes). Later versions may add fields: ignore fields you don't know.

**The loop:** read the sidecar's stdout **continuously** (a full pipe stalls the sidecar). Common patterns: a
reader thread / goroutine / coroutine that handles one line at a time; `select`/`poll` on the pipe together
with your other inputs (like `IO::Select` in `examples/demo.pl`); or a read with a short timeout in your game
loop (like `pv_event NAME 0.05` in `chat.sh`). Write each command as one line and flush. stdout carries only protocol
lines; stderr is a human-readable log (in Vietnamese) — write it to a log file or discard it, don't parse it.

### Disconnect and cleanup

- `{"cmd":"disconnect","peer":N}` closes one connection; both sides get `disconnected`.
- `{"cmd":"quit"}` or closing stdin stops the node and the process exits with status `0`. Wait for it; kill it
  after a few seconds.
- One process is one node. Run several processes for several nodes. Have a single writer per process (or a
  lock if several threads write) so command lines never interleave.

### Error handling

| Situation | How you see it |
|---|---|
| Startup failed (bad config JSON, bad ticket, port in use, license...) | **one** `{"event":"error","message":"E_...: ...","code":"E_..."}` line instead of `ready`, then EOF, exit status `1`; a Vietnamese reason on stderr |
| Bad command-line arguments (missing ticket...), unknown connect code | EOF before `ready` (no JSON line), exit status `1`, reason on stderr |
| Could not connect | `connect_failed` event with `code` (after `timeout_ms`, default 20 s); the process keeps running until you send `quit` |
| Connection lost | `disconnected` event with `code` |
| Bad command / unknown peer / datagram > 1180 bytes | `error` event: `E_INVALID_ARG` / `E_UNKNOWN_PEER` / `E_TOO_LARGE` |
| Binary not found | your language's spawn error (ENOENT) — check `PEEROVA_BIN` / `PATH` |

### Configuration

Passed as JSON in the last argument. **Server:**

| Key | Meaning |
|---|---|
| `port` | UDP port, default `47000`; `0` = random free port |
| `key_file` | identity key file, default `"server.key"` (created if missing). Same key ⇒ same ticket. **Secret** |
| `license` | `PVA-...` license key. Without it: dev mode, max 3 clients |
| `max_ccu` | maximum concurrent clients (never above the license) |
| `token` | shared secret clients must present |
| `upnp` | ask the router to open the port, default `true` |
| `private` | the ticket contains no IP addresses, default `false` (on a direct connection players still learn the IP — to really hide it use `hide_ip`) |
| `hide_ip` | Hide the server IP: players never learn its real address (DDoS protection); slightly higher latency. Pro/Studio/Enterprise plans and the trial. Default `false` |
| `app` | project id from the Peerova website — required for a 6-character connect code |
| `share` | `{"name","public","max","mode","region"}` — info shown with the connect code; `public` = listed in the public room list |

**Client:**

| Key | Meaning |
|---|---|
| `token` | must match the server |
| `timeout_ms` | max time before `connect_failed`, default `20000` |
| `key_file` | the player's identity file, created if missing (the folder must exist). Store it in the user's app-data folder, e.g. `"<appdata>/<game>/peerova-client.key"`. Not set → a new identity every run. Banning by address works per player only while they keep that file |

```sh
peerova stdio server '{"port":47000,"key_file":"/var/lib/myapp/server.key","license":"PVA-...","token":"s3cret"}'
peerova stdio client pvt_... '{"token":"s3cret","timeout_ms":10000,"key_file":"<appdata>/MyGame/peerova-client.key"}'
```

Connections are automatic: direct first; when a network blocks direct connections Peerova uses the **automatic encrypted
route** (still end-to-end encrypted) and upgrades to direct when it can.

### Players never type anything

Players never type a ticket or a code: embed the ticket in the game, or fetch the latest ticket from your own URL at startup
(falling back to the embedded one). 6-character codes are only for inviting friends to player-hosted rooms. Details:
[guide §2](../DEVELOPER-GUIDE.md#2-players-never-type-anything).

### Existing game: run peerova hidden

`peerova stdio` does **not** open any local port. If your program already has socket code that connects to a server
(`127.0.0.1:8888`…), run `peerova client <TICKET> --forward tcp:8888` in the background instead of `stdio`: that process opens
`127.0.0.1:8888` on the player's machine and your socket code stays unchanged. The server runs `peerova server --expose tcp:8888`.

How: start it hidden, read stdout+stderr (UTF-8); it is **ready** when a line contains the ASCII text `server port`
(`✔ Tcp 127.0.0.1:8888 → server port 8888`, one line per `--forward`); it failed if the process exits first (status `1`, stderr
line starting with `lỗi:` = "error:") or after 30 s; kill it when your program exits. Bash example (Linux/macOS):

```bash
#!/usr/bin/env bash
# Start `peerova client --forward` in the background, wait until the local port is open, kill it on exit.
PEEROVA=${PEEROVA_BIN:-./peerova}
TICKET=${1:?usage: tunnel.sh <TICKET>}
FORWARD=${FORWARD:-tcp:8888}
KEY="${XDG_DATA_HOME:-$HOME/.local/share}/MyApp/peerova-client.key"
mkdir -p "$(dirname "$KEY")"

LOG=$(mktemp)
"$PEEROVA" client "$TICKET" --forward "$FORWARD" --key "$KEY" >"$LOG" 2>&1 </dev/null &
PV_PID=$!
trap 'kill "$PV_PID" 2>/dev/null; rm -f "$LOG"' EXIT

# READY = a line containing "server port" ("✔ Tcp 127.0.0.1:8888 → server port 8888"), 30 s max
for _ in $(seq 300); do
  grep -q "server port" "$LOG" && break
  kill -0 "$PV_PID" 2>/dev/null || { cat "$LOG" >&2; echo "peerova exited before connecting" >&2; exit 1; }
  sleep 0.1
done
grep -q "server port" "$LOG" || { echo "peerova: timed out" >&2; exit 1; }

# Your existing socket code / program, unchanged:
curl -s http://127.0.0.1:8888/
```

- `--key` keeps the player's identity across runs (the server sees the same address, so banning players works).
- `peerova client` exits when the connection is lost (unless you add `--reconnect`); add `--token SECRET` when the server requires one.
- Snippets for C#, Unity, Java, Node.js/Electron, Python, Go, C++ (no console window on Windows):
  [guide §4](../DEVELOPER-GUIDE.md#4-the-game-runs-peerova-hidden-players-run-nothing).
- Phones / consoles can't run child processes — use a native binding (the SDK).

### Game servers

Several ports from one process, a separate Remote Desktop process for admins, a `127.a.b.c` address per player + `--banned`,
`--hide-ip`, VPN mode: see [guide §7](../DEVELOPER-GUIDE.md#7-game-server-features).

### Security

- `server.key` is the server's identity: anyone who has it can impersonate the server. Keep it secret and never
  commit it.
- The ticket is meant to be shared with clients (not a secret). `token` and `license` are secrets.
- All traffic is end-to-end encrypted.
- Command-line arguments can be visible to other users on the same machine (`ps`). On shared machines, keep
  `license`/`token` out of sight of other users.

### Troubleshooting

- Run `peerova doctor` (on both machines) for a network diagnosis.
- **Binary not found**: set `PEEROVA_BIN=/path/to/peerova` or add it to `PATH`; on Linux remember `chmod +x`.
- **`connect_failed` with `code` `E_TIMEOUT`**: check the ticket/code is current and the server is running; run
  `peerova doctor` on both machines.
- **The 4th client is refused (`E_CCU_LIMIT`)**: the server is in dev mode (max 3 clients) — add a `license`.
- **`error` `E_TOO_LARGE` when sending unreliable**: payload > 1180 bytes; use `"reliable":true` or split it.
- **`E_IO` at startup**: the `key_file` folder doesn't exist or isn't writable.
- **No events at all**: commands not flushed, or you are reading stderr instead of stdout.
- **Existing socket code can't connect to `127.0.0.1`**: `stdio` opens no port — use `peerova client --forward` (above).

### FAQ

- **Do players need to type a code?** No. Embed the ticket or fetch it from your URL; 6-character codes are only for inviting friends.
- **Can `stdio` replace `peerova client --forward`?** No — `stdio` exchanges messages, it opens no local port.
- **Latency?** The sidecar adds a few ms + base64 per packet; latency-sensitive games should use a native binding.

### Test

```sh
./test_stdio.sh             # uses jq if installed
PV_NO_JQ=1 ./test_stdio.sh  # forces the pure-bash JSON path
```
