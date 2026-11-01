# Peerova cho Ruby

[Tiếng Việt](#tiếng-việt) · [English](#english)

Website: https://peerova.pages.dev · Khái niệm và toàn bộ C API: [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md)

---

## Tiếng Việt

Binding Ruby (`lib/peerova.rb`) cho thư viện Peerova qua **Fiddle** (có sẵn trong Ruby) — không cần trình biên dịch
hay native extension. Kết nối ngang hàng, mã hoá đầu-cuối, gửi reliable / unreliable trên 256 kênh. Kết nối hoàn toàn
tự động: thử kết nối trực tiếp trước, nếu mạng chặn thì tự dùng một đường dự phòng đã mã hoá.

Không dùng được Fiddle? Ruby có thể chạy chương trình sidecar `peerova stdio` (giao thức JSON theo dòng, xem
`bindings/stdio/`) bằng `Open3.popen2`.

### Yêu cầu

* Ruby 3.0 trở lên, gem `fiddle` (có sẵn; từ Ruby 3.5 là bundled gem, đã khai báo trong gemspec), `json`.
* Thư viện native Peerova cho nền tảng đích.

### Bộ SDK gồm những gì

```
include/peerova.h                    C header (tham chiếu)
lib/linux-x86_64/libpeerova.so       thư viện Linux (+ libpeerova.a, và chương trình sidecar peerova)
lib/windows-x86_64/peerova.dll       thư viện Windows (+ peerova.exe)
bindings/ruby/                       gem này (lib/, examples/, test/)
```

### Đặt thư viện native ở đâu

Thứ tự tìm: `ENV["PEEROVA_LIB"]` → cạnh `lib/peerova.rb` → bộ nạp hệ thống.

| Hệ điều hành | File | Vị trí |
|---|---|---|
| Linux | `libpeerova.so` | `PEEROVA_LIB=/opt/peerova/libpeerova.so`, hoặc chép vào `lib/` của gem, hoặc `LD_LIBRARY_PATH` |
| Windows | `peerova.dll` | `PEEROVA_LIB`, hoặc chép vào `lib/` của gem, hoặc thư mục trong `PATH` / cạnh `ruby.exe`. **Không** cần `wintun.dll` (chỉ chế độ VPN của công cụ dòng lệnh `peerova` mới cần) |
| macOS | `libpeerova.dylib` | liên hệ Peerova để nhận bản macOS; đặt như Linux (`PEEROVA_LIB` hoặc `DYLD_LIBRARY_PATH`) |

### Cài đặt

```ruby
# Gemfile
gem "peerova", path: "../peerova-sdk/bindings/ruby"
```

hoặc `cd bindings/ruby && gem build peerova.gemspec && gem install peerova-0.1.0.gem`, hoặc chỉ cần
`ruby -I/path/to/bindings/ruby/lib app.rb`.

### Bắt đầu trong 5 phút

Server:

```ruby
require "peerova"

srv = Peerova::Server.new(port: 47000, key_file: "server.key") # chặn tối đa ~3 s
puts srv.ticket                 # pvt_... — gửi cho client
puts srv.room_code.inspect      # mã kết nối 6 ký tự (chỉ khi có app + share); có thể nil lúc đầu
```

Client:

```ruby
cli = Peerova::Client.new(ticket_or_code)     # ticket "pvt_..." hoặc mã 6 ký tự
ev = cli.poll(20)                             # chờ :connected / :connect_failed
cli.send_message(ev.peer, "xin chào") if ev&.type == :connected   # reliable, kênh 0
```

Với ticket, `Client.new` trả về ngay. Với mã kết nối, nó tra mã trên website trước (chặn chốc lát) và raise
`Peerova::Error` nếu mã không tồn tại.

`Server.open` / `Client.open` nhận block và tự `close` khi xong.

Ví dụ (chat + đồng bộ vị trí):

```sh
cd bindings/ruby
ruby -Ilib examples/chat.rb server                     # in ra ticket
ruby -Ilib examples/chat.rb client <ticket|mã>         # gõ để chat, /quit để thoát
```

### Gửi: reliable hay unreliable

`node.send_message(peer, data, channel: 0, reliable: true)` — `data` là String bất kỳ (gửi nguyên byte).

| | `reliable: true` (mặc định) | `reliable: false` |
|---|---|---|
| Đảm bảo | đến đủ, đúng thứ tự **trong cùng kênh** | có thể mất hoặc đảo thứ tự |
| Kích thước | lớn tuỳ ý | tối đa **1180 byte** mỗi tin (`Peerova::MAX_UNRELIABLE_PAYLOAD`); lớn hơn sẽ bị từ chối |
| Dùng cho | chat, túi đồ, RPC, lệnh, khối dữ liệu file | input gameplay, vị trí, trạng thái thời gian thực kiểu voice — tin sau thay thế tin trước |

Kênh 0..255. Các kênh khác nhau không chặn nhau.

### Nhận sự kiện

`node.poll(timeout = 0)` trả về `Peerova::Event` hoặc `nil` (`0` = không chờ). `node.each_event(timeout: 1.0)` gọi
block cho mỗi sự kiện tới khi `timeout` giây không có sự kiện (không có block thì trả về Enumerator).

| `ev.type` | Ý nghĩa | `ev.data` / `ev.text` |
|---|---|---|
| `:connected` | server: một client vào; client: đã kết nối tới server | `""` |
| `:message` | có tin nhắn (`ev.channel`, `ev.reliable`) | nội dung (String nhị phân; `text` = UTF-8) |
| `:disconnected` | kết nối đã đóng | lý do |
| `:connect_failed` | chỉ client: không kết nối được (hết giờ, sai token, server đầy…) | lý do |

Vòng lặp: chạy trong một `Thread` riêng (hoặc luồng chính của một worker):

```ruby
net = Thread.new do
  until stop
    ev = node.poll(0.05) or next      # chờ tối đa 50 ms, không giữ GVL
    handle(ev)
  end
end
```

Ứng dụng web (Rails, Sinatra…): chạy server Peerova trong một tiến trình worker riêng chạy liên tục, không đặt trong
request; trao đổi với web qua cơ sở dữ liệu, Redis hoặc hàng đợi.

### Ngắt kết nối và dọn dẹp

* `node.disconnect(peer)` đóng một kết nối (sau đó có sự kiện `:disconnected`).
* `node.close` ngắt mọi kết nối và giải phóng node; gọi nhiều lần không sao. Finalizer giải phóng node bị quên khi GC
  chạy — hãy tự gọi `close` (hoặc dùng `Server.open { }` / `Client.open { }`).
* Không `close` khi một thread khác đang `poll` / `send_message` trên node đó: dừng thread mạng (`net.join`) trước.

### Xử lý lỗi

* `Peerova::Error`: thông báo lấy từ thư viện native (`peerova_last_error`): cấu hình sai, cổng đang dùng, ticket hỏng,
  mã không tồn tại, peer không tồn tại, gói unreliable > 1180 byte, node đã đóng…
* `Peerova::Error: cannot load libpeerova.so (set PEEROVA_LIB)` khi `require "peerova"`: không tìm thấy thư viện.
* Lỗi mạng của client đến dưới dạng sự kiện `:connect_failed`, không phải exception.

### Cấu hình

Server — `Peerova::Server.new(**config)`:

| Khoá | Mặc định | Ý nghĩa |
|---|---|---|
| `port` | `47000` | cổng UDP; `0` = ngẫu nhiên |
| `key_file` | `"server.key"` | file danh tính server; cùng key ⇒ cùng ticket |
| `license` | — | khoá `PVA-...`; không có ⇒ chế độ dev, tối đa 3 client |
| `max_ccu` | theo license | giới hạn số client đồng thời |
| `token` | — | mật khẩu chung client phải gửi |
| `upnp` | `true` | tự mở cổng trên router |
| `private` | `false` | ticket không chứa địa chỉ IP của server |
| `app` | — | mã dự án trên website Peerova (cần cho mã kết nối) |
| `share` | — | `{ name: "...", public: true, max: 16, mode: "...", region: "VN" }` — đăng phòng và nhận mã 6 ký tự |

Client — `Peerova::Client.new(ticket_or_code, token: ..., timeout_ms: 20000)`: `token` (phải khớp server),
`timeout_ms` (mặc định `20000`).

### Bảo mật

* `server.key` là danh tính của server: ai có file này có thể giả mạo server. Giữ bí mật, không commit vào git.
* Ticket gần như công khai: cứ gửi cho client. `token` và `license` là bí mật — đọc từ `ENV`, đừng ghi cứng.
* Mọi dữ liệu được mã hoá đầu-cuối.

### Khắc phục sự cố

* Chạy `peerova doctor` (công cụ dòng lệnh trong SDK) để chẩn đoán mạng.
* Không tìm thấy thư viện: đặt `PEEROVA_LIB` tới file `.so` / `.dll` / `.dylib`.
* `:connect_failed` do hết giờ: server chưa chạy, sai ticket/mã, hoặc tường lửa; tăng `timeout_ms`, chạy `peerova doctor` ở cả hai đầu.
* Client thứ 4 bị từ chối: server đang ở chế độ dev (tối đa 3 client) — thêm `license`.
* Gửi unreliable > 1180 byte bị từ chối — chia nhỏ hoặc dùng reliable.

### Kiểm thử

```sh
cd bindings/ruby && ruby -Ilib test/test_peerova.rb
```

---

## English

Ruby binding (`lib/peerova.rb`) for the Peerova library via **Fiddle** (part of Ruby) — no compiler or native
extension needed. Peer-to-peer connections, end-to-end encryption, reliable / unreliable delivery on 256 channels.
Connections are automatic: direct first, with an automatic encrypted fallback route when networks block direct
connections.

Can't use Fiddle? Ruby can drive the `peerova stdio` sidecar program (line-based JSON protocol, see `bindings/stdio/`)
with `Open3.popen2`.

### Requirements

* Ruby 3.0 or newer, the `fiddle` gem (built in; a bundled gem since Ruby 3.5, declared in the gemspec), `json`.
* The Peerova native library for the target platform.

### SDK contents

```
include/peerova.h                    C header (reference)
lib/linux-x86_64/libpeerova.so       Linux library (+ libpeerova.a, and the peerova sidecar program)
lib/windows-x86_64/peerova.dll       Windows library (+ peerova.exe)
bindings/ruby/                       this gem (lib/, examples/, test/)
```

### Where to put the native library

Lookup order: `ENV["PEEROVA_LIB"]` → next to `lib/peerova.rb` → the system loader.

| OS | File | Location |
|---|---|---|
| Linux | `libpeerova.so` | `PEEROVA_LIB=/opt/peerova/libpeerova.so`, or copy it into the gem's `lib/`, or `LD_LIBRARY_PATH` |
| Windows | `peerova.dll` | `PEEROVA_LIB`, or copy it into the gem's `lib/`, or a directory on `PATH` / next to `ruby.exe`. `wintun.dll` is **not** needed (only the VPN mode of the `peerova` command-line tool uses it) |
| macOS | `libpeerova.dylib` | contact Peerova for macOS builds; place it as on Linux (`PEEROVA_LIB` or `DYLD_LIBRARY_PATH`) |

### Install

```ruby
# Gemfile
gem "peerova", path: "../peerova-sdk/bindings/ruby"
```

or `cd bindings/ruby && gem build peerova.gemspec && gem install peerova-0.1.0.gem`, or simply
`ruby -I/path/to/bindings/ruby/lib app.rb`.

### 5-minute quick start

Server:

```ruby
require "peerova"

srv = Peerova::Server.new(port: 47000, key_file: "server.key") # blocks up to ~3 s
puts srv.ticket                 # pvt_... — give it to clients
puts srv.room_code.inspect      # 6-char connect code (only with app + share); may be nil at first
```

Client:

```ruby
cli = Peerova::Client.new(ticket_or_code)     # "pvt_..." ticket or 6-char code
ev = cli.poll(20)                             # wait for :connected / :connect_failed
cli.send_message(ev.peer, "hello") if ev&.type == :connected   # reliable, channel 0
```

With a ticket, `Client.new` returns immediately. With a connect code it first looks the code up on the website
(blocks briefly) and raises `Peerova::Error` if the code is unknown.

`Server.open` / `Client.open` take a block and close the node when it returns.

Example (chat + position sync):

```sh
cd bindings/ruby
ruby -Ilib examples/chat.rb server                     # prints the ticket
ruby -Ilib examples/chat.rb client <ticket|code>       # type to chat, /quit to leave
```

### Sending: reliable vs unreliable

`node.send_message(peer, data, channel: 0, reliable: true)` — `data` is any String (sent as raw bytes).

| | `reliable: true` (default) | `reliable: false` |
|---|---|---|
| Guarantee | delivered, in order **within the channel** | may be lost or reordered |
| Size | any size | at most **1180 bytes** per message (`Peerova::MAX_UNRELIABLE_PAYLOAD`); larger sends are rejected |
| Use for | chat, inventory, RPCs, commands, file chunks | gameplay inputs, positions, voice-like realtime state — each update supersedes the previous one |

Channels are 0..255. Different channels don't block each other.

### Receiving events

`node.poll(timeout = 0)` returns a `Peerova::Event` or `nil` (`0` = don't wait). `node.each_event(timeout: 1.0)` yields
each event until `timeout` seconds pass without one (returns an Enumerator without a block).

| `ev.type` | Meaning | `ev.data` / `ev.text` |
|---|---|---|
| `:connected` | server: a client joined; client: connected to the server | `""` |
| `:message` | a message (`ev.channel`, `ev.reliable`) | payload (binary String; `text` = UTF-8) |
| `:disconnected` | the connection closed | reason |
| `:connect_failed` | client only: could not connect (timeout, wrong token, server full, ...) | reason |

Loop: run it in its own `Thread` (or the main thread of a worker):

```ruby
net = Thread.new do
  until stop
    ev = node.poll(0.05) or next      # waits up to 50 ms without holding the GVL
    handle(ev)
  end
end
```

Web apps (Rails, Sinatra, ...): run the Peerova server in a separate long-running worker process, not inside a
request; exchange data with the web app through a database, Redis or a queue.

### Disconnect and cleanup

* `node.disconnect(peer)` closes one connection (a `:disconnected` event follows).
* `node.close` drops all connections and frees the node; calling it twice is fine. A finalizer frees forgotten nodes
  when the GC runs — call `close` yourself (or use `Server.open { }` / `Client.open { }`).
* Never `close` while another thread is inside `poll` / `send_message` on that node: stop the network thread
  (`net.join`) first.

### Error handling

* `Peerova::Error`: the message comes from the native library (`peerova_last_error`): bad config, port in use,
  malformed ticket, unknown code, unknown peer, unreliable payload > 1180 bytes, closed node, ...
* `Peerova::Error: cannot load libpeerova.so (set PEEROVA_LIB)` on `require "peerova"`: the library was not found.
* Client network failures arrive as a `:connect_failed` event, not as exceptions.

### Configuration

Server — `Peerova::Server.new(**config)`:

| Key | Default | Meaning |
|---|---|---|
| `port` | `47000` | UDP port; `0` = random |
| `key_file` | `"server.key"` | server identity file; same key ⇒ same ticket |
| `license` | — | `PVA-...` key; without it: dev mode, max 3 clients |
| `max_ccu` | from license | cap on concurrent clients |
| `token` | — | shared secret clients must present |
| `upnp` | `true` | open the port on the router automatically |
| `private` | `false` | the ticket does not contain the server's IP addresses |
| `app` | — | project id from the Peerova website (needed for connect codes) |
| `share` | — | `{ name: "...", public: true, max: 16, mode: "...", region: "VN" }` — list the room and get a 6-char code |

Client — `Peerova::Client.new(ticket_or_code, token: ..., timeout_ms: 20000)`: `token` (must match the server),
`timeout_ms` (default `20000`).

### Security

* `server.key` is the server's identity: anyone who has it can impersonate the server. Keep it secret, don't commit it.
* The ticket is public-ish: share it with clients. `token` and `license` are secrets — read them from `ENV`, don't
  hard-code them.
* All traffic is end-to-end encrypted.

### Troubleshooting

* Run `peerova doctor` (the SDK's command-line tool) for a network diagnosis.
* Library not found: set `PEEROVA_LIB` to the `.so` / `.dll` / `.dylib` file.
* `:connect_failed` with a timeout: server not running, wrong ticket/code, or a firewall; raise `timeout_ms`, run
  `peerova doctor` on both ends.
* A 4th client is refused: the server runs in dev mode (max 3 clients) — add a `license`.
* Unreliable sends over 1180 bytes are rejected — split them or send reliably.

### Tests

```sh
cd bindings/ruby && ruby -Ilib test/test_peerova.rb
```
