# Peerova cho PHP

[Tiếng Việt](#tiếng-việt) · [English](#english)

Website: https://peerova.pages.dev · Khái niệm và toàn bộ C API: [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md)

---

## Tiếng Việt

Hai backend, **cùng một API** (interface `Peerova\Node`):

| Backend | Lớp | Cần gì | Khi nào dùng |
|---|---|---|---|
| FFI (trong tiến trình) | `Peerova\Peerova` → `FfiNode` | extension `ffi` + thư viện `libpeerova.so` / `peerova.dll` | máy chủ riêng / VPS, hiệu năng cao nhất, đọc được mã kết nối |
| Sidecar | `Peerova\PeerovaStdio` → `StdioNode` | chỉ `proc_open` + chương trình `peerova` | hosting không có ext/ffi |

Kết nối hoàn toàn tự động: thử kết nối trực tiếp trước, nếu mạng chặn thì tự dùng một đường dự phòng đã mã hoá.

### Yêu cầu

* PHP 7.4 trở lên (đã kiểm thử: PHP 8.3 với FFI và sidecar, PHP 8.2 với sidecar), extension `json`.
* FFI: extension `ffi` (`extension=ffi`) **và** `ffi.enable`:
  * Chạy bằng CLI (`php script.php`): giá trị mặc định `ffi.enable=preload` đã đủ.
  * PHP-FPM / Apache: cần `ffi.enable=1` trong `php.ini` (hoặc dùng `opcache.preload`).
* Sidecar: hàm `proc_open` không bị tắt (`disable_functions`) và chương trình `peerova` / `peerova.exe` của SDK.

### PHP-FPM và tiến trình chạy lâu

Một request PHP-FPM chỉ sống vài giây rồi kết thúc — mọi kết nối Peerova sẽ đóng theo. Hãy chạy server/client
Peerova như một **CLI worker** chạy liên tục (systemd, supervisor, `nohup php worker.php`), và cho trang web
nói chuyện với worker qua cơ sở dữ liệu, Redis, hàng đợi hoặc socket nội bộ.

### Bộ SDK gồm những gì

```
include/peerova.h                    C header (tham chiếu)
lib/linux-x86_64/libpeerova.so       thư viện Linux (+ libpeerova.a, và chương trình sidecar peerova)
lib/windows-x86_64/peerova.dll       thư viện Windows (+ peerova.exe)
bindings/php/                        gói PHP này (src/, examples/, tests/)
```

### Đặt thư viện native / chương trình sidecar ở đâu

FFI tìm theo thứ tự: `getenv('PEEROVA_LIB')` → cạnh các file trong `src/` → bộ nạp hệ thống.

| Hệ điều hành | FFI | Sidecar |
|---|---|---|
| Linux | `libpeerova.so`: đặt `PEEROVA_LIB=/opt/peerova/libpeerova.so`, hoặc chép vào `src/`, hoặc `LD_LIBRARY_PATH` | `peerova`: đặt `PEEROVA_BIN=/opt/peerova/peerova`, hoặc đưa vào `PATH`, hoặc truyền `$binary` |
| Windows | `peerova.dll`: `PEEROVA_LIB`, hoặc chép vào `src/`, hoặc thư mục trong `PATH`. **Không** cần `wintun.dll` (chỉ chế độ VPN của công cụ dòng lệnh `peerova` mới cần) | `peerova.exe`: `PEEROVA_BIN` hoặc `PATH` |
| macOS | liên hệ Peerova để nhận bản macOS (`libpeerova.dylib`), đặt như Linux | liên hệ Peerova |

Trên Linux nhớ `chmod +x peerova`.

### Cài đặt

Composer (repository dạng path):

```json
{
  "repositories": [{ "type": "path", "url": "../peerova-sdk/bindings/php" }],
  "require": { "peerova/peerova": "*" }
}
```

Hoặc không cần Composer:

```php
require '/path/to/bindings/php/src/Common.php';
require '/path/to/bindings/php/src/Peerova.php';       // FFI
require '/path/to/bindings/php/src/PeerovaStdio.php';  // sidecar
```

### Bắt đầu trong 5 phút

Server (CLI worker):

```php
use Peerova\{Peerova, EventType};

$srv = Peerova::server(['port' => 47000, 'key_file' => '/var/lib/myapp/server.key']); // chặn tối đa ~3 s
echo $srv->ticket(), "\n";          // pvt_... — gửi cho client
echo $srv->roomCode() ?? '-', "\n"; // mã kết nối 6 ký tự (chỉ FFI, chỉ khi có 'app' + 'share'); có thể null lúc đầu

while (true) {
    $ev = $srv->poll(0.05);            // chờ tối đa 50 ms
    if ($ev && $ev->isMessage()) {
        $srv->send($ev->peer, "echo: {$ev->data}");
    }
}
```

Client:

```php
$cli = Peerova::client($ticketOrCode);   // ticket "pvt_..." hoặc mã 6 ký tự
$ev = $cli->poll(20);                    // chờ CONNECTED / CONNECT_FAILED
if ($ev && $ev->type === EventType::CONNECTED) {
    $cli->send($ev->peer, 'xin chào');   // reliable, kênh 0
}
```

Không có FFI? Đổi `Peerova::` thành `PeerovaStdio::` — phần còn lại giữ nguyên.

Với ticket, `client()` trả về ngay. Với mã kết nối, nó tra mã trên website trước (chặn chốc lát) và ném
`PeerovaException` nếu mã không tồn tại. Backend sidecar không đọc được mã kết nối của server
(`roomCode()` chỉ có ở FFI), nhưng client sidecar vẫn kết nối bằng mã được.

Ví dụ chạy được (chat + đồng bộ vị trí):

```sh
cd bindings/php
php examples/chat.php server                    # in ra ticket
php examples/chat.php client <ticket|mã>        # gõ để chat, /quit để thoát; thêm --stdio để ép dùng sidecar
```

### Gửi: reliable hay unreliable

`$node->send(int $peer, string $data, int $channel = 0, bool $reliable = true)` — `$data` là chuỗi nhị phân.

| | `$reliable = true` (mặc định) | `$reliable = false` |
|---|---|---|
| Đảm bảo | đến đủ, đúng thứ tự **trong cùng kênh** | có thể mất hoặc đảo thứ tự |
| Kích thước | lớn tuỳ ý | tối đa **1180 byte** mỗi tin; lớn hơn sẽ bị từ chối |
| Dùng cho | chat, túi đồ, RPC, lệnh, khối dữ liệu file | input gameplay, vị trí, trạng thái thời gian thực kiểu voice — tin sau thay thế tin trước |

Kênh 0..255. Các kênh khác nhau không chặn nhau.

### Nhận sự kiện

`$node->poll(float $timeout = 0)` trả về `Peerova\Event` hoặc `null` (`0` = không chờ). `$node->events($timeout)`
là generator, dừng khi `$timeout` giây không có sự kiện.

| `$ev->type` | Ý nghĩa | `$ev->data` |
|---|---|---|
| `EventType::CONNECTED` | server: một client vào; client: đã kết nối tới server | `''` |
| `EventType::MESSAGE` | có tin nhắn (`$ev->channel`, `$ev->reliable`) | nội dung |
| `EventType::DISCONNECTED` | kết nối đã đóng | lý do |
| `EventType::CONNECT_FAILED` | chỉ client: không kết nối được (hết giờ, sai token, server đầy…) | lý do |

PHP không có event loop sẵn: hãy viết một vòng lặp `while` gọi `poll()` với timeout nhỏ (10–50 ms) và làm các việc
định kỳ khác (gửi vị trí, đọc hàng đợi) giữa các lần poll — xem `examples/chat.php`.

### Ngắt kết nối và dọn dẹp

* `$node->disconnect($peer)` đóng một kết nối (sau đó có sự kiện `DISCONNECTED`).
* `$node->close()` ngắt mọi kết nối và giải phóng node (FFI) / dừng tiến trình sidecar. Gọi nhiều lần không sao;
  destructor cũng tự gọi.
* Một node chỉ dùng trong một luồng; không `close()` khi luồng khác đang `poll()` trên node đó.
* Worker nên bắt `SIGINT`/`SIGTERM` (`pcntl_signal`) để thoát vòng lặp rồi `close()`.

### Xử lý lỗi

* `Peerova\PeerovaException`: FFI — thông báo lấy từ thư viện native (`peerova_last_error`): cấu hình sai, cổng đang
  dùng, ticket hỏng, mã không tồn tại, peer không tồn tại, gói unreliable > 1180 byte…
* Sidecar: lỗi khởi động → `PeerovaException('sidecar did not become ready ...')`, lý do chi tiết in ra stderr. Lỗi
  khi gửi (ví dụ peer không tồn tại) đến không đồng bộ: đọc bằng `$node->lastErrors()`.
* `ext/ffi is not loaded` → bật extension hoặc dùng `PeerovaStdio`. `FFI\Exception: Failed loading ...` → không tìm
  thấy thư viện, xem `PEEROVA_LIB`.
* Lỗi mạng của client đến dưới dạng sự kiện `CONNECT_FAILED`, không phải exception.

### Cấu hình

Server — `Peerova::server([...])` / `PeerovaStdio::server([...])`:

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
| `share` | — | `['name' => '...', 'public' => true, 'max' => 16, 'mode' => '...', 'region' => 'VN']` — đăng phòng và nhận mã 6 ký tự |

Client — `Peerova::client($ticketOrCode, [...])`: `token` (phải khớp server), `timeout_ms` (mặc định `20000`).

### Bảo mật

* `server.key` là danh tính của server: ai có file này có thể giả mạo server. Giữ bí mật, đặt ngoài thư mục web
  (không để trong `public_html`), không commit vào git.
* Ticket gần như công khai: cứ gửi cho client. `token` và `license` là bí mật — lấy từ biến môi trường, đừng ghi cứng.
* Mọi dữ liệu được mã hoá đầu-cuối.

### Khắc phục sự cố

* Chạy `peerova doctor` (công cụ dòng lệnh trong SDK) để chẩn đoán mạng.
* Không tìm thấy thư viện / chương trình: kiểm tra `PEEROVA_LIB` / `PEEROVA_BIN`, quyền thực thi, `open_basedir`.
* `CONNECT_FAILED` do hết giờ: server chưa chạy, sai ticket/mã, hoặc tường lửa; tăng `timeout_ms`, chạy `peerova doctor` ở cả hai đầu.
* Client thứ 4 bị từ chối: server đang ở chế độ dev (tối đa 3 client) — thêm `license`.
* Gửi unreliable > 1180 byte bị từ chối — chia nhỏ hoặc dùng reliable.
* Kết nối mất ngay sau khi request web kết thúc: xem mục PHP-FPM ở trên.

### Kiểm thử

```sh
cd bindings/php && php -d ffi.enable=1 tests/test.php    # chạy cả sidecar và FFI (FFI bị bỏ qua nếu không có ext/ffi)
```

---

## English

Two backends, **one API** (interface `Peerova\Node`):

| Backend | Class | Needs | Use when |
|---|---|---|---|
| FFI (in-process) | `Peerova\Peerova` → `FfiNode` | the `ffi` extension + `libpeerova.so` / `peerova.dll` | your own server / VPS; fastest; can read the connect code |
| Sidecar | `Peerova\PeerovaStdio` → `StdioNode` | only `proc_open` + the `peerova` program | hosting without ext/ffi |

Connections are automatic: direct first, with an automatic encrypted fallback route when networks block direct
connections.

### Requirements

* PHP 7.4 or newer (tested: PHP 8.3 with FFI and sidecar, PHP 8.2 with the sidecar), extension `json`.
* FFI: the `ffi` extension (`extension=ffi`) **and** `ffi.enable`:
  * CLI (`php script.php`): the default `ffi.enable=preload` is enough.
  * PHP-FPM / Apache: needs `ffi.enable=1` in `php.ini` (or `opcache.preload`).
* Sidecar: `proc_open` must not be disabled (`disable_functions`), plus the SDK's `peerova` / `peerova.exe` program.

### PHP-FPM and long-running processes

A PHP-FPM request lives a few seconds and then ends — and every Peerova connection closes with it. Run the Peerova
server/client as a long-running **CLI worker** (systemd, supervisor, `nohup php worker.php`) and let your web pages
talk to the worker through a database, Redis, a queue or a local socket.

### SDK contents

```
include/peerova.h                    C header (reference)
lib/linux-x86_64/libpeerova.so       Linux library (+ libpeerova.a, and the peerova sidecar program)
lib/windows-x86_64/peerova.dll       Windows library (+ peerova.exe)
bindings/php/                        this PHP package (src/, examples/, tests/)
```

### Where to put the native library / sidecar program

FFI lookup order: `getenv('PEEROVA_LIB')` → next to the files in `src/` → the system loader.

| OS | FFI | Sidecar |
|---|---|---|
| Linux | `libpeerova.so`: set `PEEROVA_LIB=/opt/peerova/libpeerova.so`, or copy it into `src/`, or `LD_LIBRARY_PATH` | `peerova`: set `PEEROVA_BIN=/opt/peerova/peerova`, or put it on `PATH`, or pass `$binary` |
| Windows | `peerova.dll`: `PEEROVA_LIB`, or copy it into `src/`, or a directory on `PATH`. `wintun.dll` is **not** needed (only the VPN mode of the `peerova` command-line tool uses it) | `peerova.exe`: `PEEROVA_BIN` or `PATH` |
| macOS | contact Peerova for macOS builds (`libpeerova.dylib`); place it as on Linux | contact Peerova |

On Linux remember `chmod +x peerova`.

### Install

Composer (path repository):

```json
{
  "repositories": [{ "type": "path", "url": "../peerova-sdk/bindings/php" }],
  "require": { "peerova/peerova": "*" }
}
```

Or without Composer:

```php
require '/path/to/bindings/php/src/Common.php';
require '/path/to/bindings/php/src/Peerova.php';       // FFI
require '/path/to/bindings/php/src/PeerovaStdio.php';  // sidecar
```

### 5-minute quick start

Server (CLI worker):

```php
use Peerova\{Peerova, EventType};

$srv = Peerova::server(['port' => 47000, 'key_file' => '/var/lib/myapp/server.key']); // blocks up to ~3 s
echo $srv->ticket(), "\n";          // pvt_... — give it to clients
echo $srv->roomCode() ?? '-', "\n"; // 6-char connect code (FFI only, only with 'app' + 'share'); may be null at first

while (true) {
    $ev = $srv->poll(0.05);            // wait up to 50 ms
    if ($ev && $ev->isMessage()) {
        $srv->send($ev->peer, "echo: {$ev->data}");
    }
}
```

Client:

```php
$cli = Peerova::client($ticketOrCode);   // "pvt_..." ticket or 6-char code
$ev = $cli->poll(20);                    // wait for CONNECTED / CONNECT_FAILED
if ($ev && $ev->type === EventType::CONNECTED) {
    $cli->send($ev->peer, 'hello');      // reliable, channel 0
}
```

No FFI? Replace `Peerova::` with `PeerovaStdio::` — everything else stays the same.

With a ticket, `client()` returns immediately. With a connect code it first looks the code up on the website
(blocks briefly) and throws `PeerovaException` if the code is unknown. The sidecar backend cannot read the server's
connect code (`roomCode()` is FFI only), but a sidecar client can still connect with a code.

Runnable example (chat + position sync):

```sh
cd bindings/php
php examples/chat.php server                     # prints the ticket
php examples/chat.php client <ticket|code>       # type to chat, /quit to leave; add --stdio to force the sidecar
```

### Sending: reliable vs unreliable

`$node->send(int $peer, string $data, int $channel = 0, bool $reliable = true)` — `$data` is a binary string.

| | `$reliable = true` (default) | `$reliable = false` |
|---|---|---|
| Guarantee | delivered, in order **within the channel** | may be lost or reordered |
| Size | any size | at most **1180 bytes** per message; larger sends are rejected |
| Use for | chat, inventory, RPCs, commands, file chunks | gameplay inputs, positions, voice-like realtime state — each update supersedes the previous one |

Channels are 0..255. Different channels don't block each other.

### Receiving events

`$node->poll(float $timeout = 0)` returns a `Peerova\Event` or `null` (`0` = don't wait). `$node->events($timeout)`
is a generator that stops after `$timeout` seconds without an event.

| `$ev->type` | Meaning | `$ev->data` |
|---|---|---|
| `EventType::CONNECTED` | server: a client joined; client: connected to the server | `''` |
| `EventType::MESSAGE` | a message (`$ev->channel`, `$ev->reliable`) | payload |
| `EventType::DISCONNECTED` | the connection closed | reason |
| `EventType::CONNECT_FAILED` | client only: could not connect (timeout, wrong token, server full, ...) | reason |

PHP has no built-in event loop: write a `while` loop that calls `poll()` with a small timeout (10–50 ms) and does
other periodic work (send positions, read a queue) between polls — see `examples/chat.php`.

### Disconnect and cleanup

* `$node->disconnect($peer)` closes one connection (a `DISCONNECTED` event follows).
* `$node->close()` drops all connections and frees the node (FFI) / stops the sidecar process. Calling it twice is
  fine; the destructor calls it too.
* Use a node from one thread only; never `close()` while another thread is inside `poll()` on it.
* Workers should catch `SIGINT`/`SIGTERM` (`pcntl_signal`) to leave the loop, then `close()`.

### Error handling

* `Peerova\PeerovaException`: FFI — the message comes from the native library (`peerova_last_error`): bad config,
  port in use, malformed ticket, unknown code, unknown peer, unreliable payload > 1180 bytes, ...
* Sidecar: a startup failure throws `PeerovaException('sidecar did not become ready ...')`; the exact reason is
  printed on stderr. Send errors (e.g. unknown peer) arrive asynchronously: read them with `$node->lastErrors()`.
* `ext/ffi is not loaded` → enable the extension or use `PeerovaStdio`. `FFI\Exception: Failed loading ...` → the
  library was not found, check `PEEROVA_LIB`.
* Client network failures arrive as a `CONNECT_FAILED` event, not as exceptions.

### Configuration

Server — `Peerova::server([...])` / `PeerovaStdio::server([...])`:

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
| `share` | — | `['name' => '...', 'public' => true, 'max' => 16, 'mode' => '...', 'region' => 'VN']` — list the room and get a 6-char code |

Client — `Peerova::client($ticketOrCode, [...])`: `token` (must match the server), `timeout_ms` (default `20000`).

### Security

* `server.key` is the server's identity: anyone who has it can impersonate the server. Keep it secret, outside the
  web root (not in `public_html`), and don't commit it.
* The ticket is public-ish: share it with clients. `token` and `license` are secrets — read them from environment
  variables, don't hard-code them.
* All traffic is end-to-end encrypted.

### Troubleshooting

* Run `peerova doctor` (the SDK's command-line tool) for a network diagnosis.
* Library / program not found: check `PEEROVA_LIB` / `PEEROVA_BIN`, the execute permission, `open_basedir`.
* `CONNECT_FAILED` with a timeout: server not running, wrong ticket/code, or a firewall; raise `timeout_ms`, run
  `peerova doctor` on both ends.
* A 4th client is refused: the server runs in dev mode (max 3 clients) — add a `license`.
* Unreliable sends over 1180 bytes are rejected — split them or send reliably.
* Connections drop as soon as the web request ends: see the PHP-FPM section above.

### Tests

```sh
cd bindings/php && php -d ffi.enable=1 tests/test.php    # runs the sidecar and FFI variants (FFI skipped without ext/ffi)
```
