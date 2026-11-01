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

Kết nối hoàn toàn tự động: thử kết nối trực tiếp trước, nếu mạng chặn thì tự dùng **đường mã hoá tự động** (vẫn mã hoá đầu-cuối).

### Chọn cách tích hợp

* **Máy chủ / worker PHP nói chuyện với game hoặc app dùng SDK Peerova** (tình huống 3/4 của hướng dẫn): dùng binding này (FFI hoặc sidecar).
* **Chương trình có sẵn dùng socket TCP/UDP** (tình huống 1/2): không cần SDK — chạy `peerova client --forward` ở cạnh nó;
  từ một script PHP CLI chạy lâu có thể tự chạy nó, xem [Chương trình có sẵn: chạy peerova ẩn](#chương-trình-có-sẵn-chạy-peerova-ẩn).
  Không làm việc này trong request PHP-FPM.
* Bảng so sánh đầy đủ: [hướng dẫn §1](../DEVELOPER-GUIDE.md#1-chọn-cách-tích-hợp).

**Người chơi không phải gõ gì:** game nhúng ticket `pvt_...`, hoặc tải ticket mới nhất từ URL của bạn khi khởi động. Một worker PHP
có thể ghi ticket của server ra file mà web của bạn phục vụ (`file_put_contents('/var/www/peerova-ticket.txt', $srv->ticket())`).
Mã 6 ký tự chỉ để mời bạn bè vào phòng do người chơi mở. Chi tiết: [hướng dẫn §2](../DEVELOPER-GUIDE.md#2-người-chơi-không-phải-gõ-gì).

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
// ticket "pvt_..." hoặc mã 6 ký tự; key_file giữ danh tính client giữa các lần chạy (thư mục phải tồn tại)
$cli = Peerova::client($ticketOrCode, ['key_file' => '/var/lib/myapp/peerova-client.key']);
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

### Chương trình có sẵn: chạy peerova ẩn

Khi một chương trình có sẵn (game client, bot, công cụ quản trị…) đã dùng socket TCP/UDP và bạn muốn một **script PHP CLI chạy lâu**
mở nó tới máy chủ Peerova mà không sửa code: script chạy `peerova client`, chờ cổng local mở, rồi chương trình kết nối `127.0.0.1:8888`.
Máy chủ chạy `peerova server --expose tcp:8888` ([hướng dẫn §3](../DEVELOPER-GUIDE.md#3-game-có-sẵn-không-sửa-code)). Chi tiết các dòng log:
[hướng dẫn §4](../DEVELOPER-GUIDE.md#4-game-tự-chạy-peerova-ẩn-người-chơi-không-chạy-gì). Launcher game hiếm khi viết bằng PHP — nếu launcher của bạn
dùng ngôn ngữ khác, lấy đoạn code tương ứng trong hướng dẫn §4.

```php
<?php
// peerova_tunnel.php — PHP 7.4+ CLI / worker chạy lâu. Chạy chương trình `peerova` và chờ tới khi cổng local mở.
// Output của CLI (UTF-8) ghi vào file log, nên PHP không phải liên tục đọc pipe.

/** @return resource|null process đang chạy khi cổng forward đã mở trên 127.0.0.1, hoặc null nếu lỗi / hết giờ */
function peerova_tunnel_start(string $exe, string $ticket, string $forward, string $keyFile, string $logFile, float $timeout = 30.0)
{
    if (!is_dir(dirname($keyFile))) {
        mkdir(dirname($keyFile), 0700, true);                       // thư mục chứa key phải tồn tại
    }
    $proc = proc_open(
        [$exe, 'client', $ticket, '--forward', $forward, '--key', $keyFile],   // mảng = không qua shell
        [0 => ['file', PHP_OS_FAMILY === 'Windows' ? 'NUL' : '/dev/null', 'r'],
         1 => ['file', $logFile, 'w'],
         2 => ['redirect', 1]],                                    // stderr ("lỗi: ...") vào cùng file log
        $pipes);
    if (!is_resource($proc)) {
        return null;
    }
    register_shutdown_function('peerova_tunnel_stop', $proc);     // kill khi script thoát
    $deadline = microtime(true) + $timeout;
    while (microtime(true) < $deadline) {
        clearstatcache();
        // "✔ Tcp 127.0.0.1:8888 → server port 8888" = đã kết nối và cổng local đã mở
        if (strpos((string)@file_get_contents($logFile), 'server port') !== false) {
            return $proc;
        }
        if (!proc_get_status($proc)['running']) {                 // thoát trước = thất bại (xem log)
            break;
        }
        usleep(100000);
    }
    peerova_tunnel_stop($proc);
    return null;
}

/** @param resource $proc */
function peerova_tunnel_stop($proc): void
{
    if (is_resource($proc)) {
        if (proc_get_status($proc)['running']) {
            proc_terminate($proc);
        }
        proc_close($proc);
    }
}
```

```php
require __DIR__ . '/peerova_tunnel.php';

$exe = __DIR__ . (PHP_OS_FAMILY === 'Windows' ? '/peerova.exe' : '/peerova');
$appData = getenv('APPDATA') ?: getenv('HOME') . '/.local/share';
$tunnel = peerova_tunnel_start($exe, $ticket, 'tcp:8888', "$appData/MyApp/peerova-client.key", "$appData/MyApp/peerova.log");
if ($tunnel) {
    $sock = stream_socket_client('tcp://127.0.0.1:8888');   // code socket có sẵn, không đổi
    // ... proc_get_status($tunnel)['running'] === false ⇒ mất kết nối
} else {
    fwrite(STDERR, "Không kết nối được máy chủ (xem peerova.log)\n");
}
```

* Đã kiểm thử trên Linux (PHP 8.2 CLI): đúng ticket → sẵn sàng rồi HTTP qua `127.0.0.1` chạy được; ticket sai → process thoát ngay,
  log chứa dòng `lỗi: ...` và hàm trả về `null`.
* Cần PHP 7.4+ (lệnh dạng mảng và `['redirect', 1]`). Lệnh dạng mảng không qua shell, nên `proc_terminate` dừng đúng process `peerova`.
* Ghi ra file log thay vì pipe vì PHP chạy một luồng: nếu để pipe mà không đọc tiếp, pipe đầy có thể làm `peerova` dừng.
* PHP CLI chạy trong console nên `peerova.exe` dùng chung console đó, không mở cửa sổ mới.
* **PHP-FPM / Apache: không chạy tunnel cho từng request** — process sẽ bị kill (hoặc bị bỏ lại) khi request kết thúc. Chạy nó trong một
  worker CLI cố định (systemd, supervisor) như mục PHP-FPM ở trên.
* `peerova stdio` (backend `PeerovaStdio`) **không** mở cổng local: nó trao đổi message SDK dạng JSON lines — dùng cho cách này là sai.

### Ngắt kết nối và dọn dẹp

* `$node->disconnect($peer)` đóng một kết nối (sau đó có sự kiện `DISCONNECTED`).
* `$node->close()` ngắt mọi kết nối và giải phóng node (FFI) / dừng tiến trình sidecar. Gọi nhiều lần không sao;
  destructor cũng tự gọi.
* Một node chỉ dùng trong một luồng; không `close()` khi luồng khác đang `poll()` trên node đó.
* Worker nên bắt `SIGINT`/`SIGTERM` (`pcntl_signal`) để thoát vòng lặp rồi `close()`.

### Xử lý lỗi

* `Peerova\PeerovaException`: FFI — thông báo lấy từ thư viện native (`peerova_last_error`): cấu hình sai, cổng đang
  dùng, ticket hỏng, mã không tồn tại, peer không tồn tại, gói unreliable > 1180 byte…
* Thông báo lỗi của thư viện, và lý do trong sự kiện `CONNECT_FAILED` / `DISCONNECTED` (`$ev->data`), bắt đầu bằng tên mã lỗi ổn định,
  ví dụ `"E_INVALID_TICKET: ..."`, `"E_TIMEOUT: ..."`. Xử lý theo tiền tố, không phân tích phần còn lại:

```php
try {
    $cli = Peerova::client($code);
} catch (\Peerova\PeerovaException $e) {
    if (strpos($e->getMessage(), 'E_CODE_NOT_FOUND') === 0) { /* phòng không tồn tại hoặc đã đóng */ }
}
if ($ev && $ev->type === EventType::CONNECT_FAILED && strpos($ev->data, 'E_CCU_LIMIT') === 0) { /* máy chủ đầy */ }
```

  Danh sách mã (`E_INVALID_ARG`, `E_TOO_LARGE`, `E_UNKNOWN_PEER`, `E_CLOSED`, `E_CODE_NOT_FOUND`, `E_NETWORK`, `E_TIMEOUT`, `E_LICENSE`,
  `E_CCU_LIMIT`, `E_ACCESS_DENIED`, `E_INVALID_TICKET`, `E_IO`, `E_INTERNAL`): [hướng dẫn — Mã lỗi](../DEVELOPER-GUIDE.md#mã-lỗi).
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
| `key_file` | `"server.key"` | file danh tính server; cùng key ⇒ cùng ticket. **Bí mật** |
| `license` | — | khoá `PVA-...`; không có ⇒ chế độ dev, tối đa 3 client |
| `max_ccu` | theo license | giới hạn số client đồng thời |
| `token` | — | mật khẩu chung client phải gửi |
| `upnp` | `true` | tự mở cổng trên router |
| `private` | `false` | ticket không chứa địa chỉ IP của server |
| `hide_ip` | `false` | Ẩn IP máy chủ: người chơi không bao giờ biết IP thật của máy chủ (chống DDoS); ping cao hơn một chút. Gói Pro/Studio/Enterprise và bản dùng thử |
| `app` | — | mã dự án trên website Peerova (cần cho mã kết nối) |
| `share` | — | `['name' => '...', 'public' => true, 'max' => 16, 'mode' => '...', 'region' => 'VN']` — đăng phòng và nhận mã 6 ký tự; `public` = hiện trong danh sách phòng công khai |

Client — `Peerova::client($ticketOrCode, [...])` / `PeerovaStdio::client(...)`:

| Khoá | Mặc định | Ý nghĩa |
|---|---|---|
| `token` | — | phải khớp server |
| `timeout_ms` | `20000` | thời gian tối đa trước `CONNECT_FAILED` |
| `key_file` | — (key ngẫu nhiên mỗi lần chạy) | file danh tính client; tạo mới nếu chưa có (**thư mục phải tồn tại**). Lưu trong thư mục dữ liệu của ứng dụng / người dùng, ví dụ `"<appdata>/<game>/peerova-client.key"`. Giữ file này thì máy chủ thấy client ở cùng một địa chỉ ổn định; cấm theo địa chỉ chỉ có tác dụng với từng người chơi khi họ còn giữ file này |

### Máy chủ game

Máy chủ game dedicated thường chạy công cụ dòng lệnh `peerova server` (nhiều `--expose`, tách process cho Remote Desktop và game,
mỗi người chơi một địa chỉ `127.a.b.c` + `--banned`, `--hide-ip`, chế độ VPN): xem [hướng dẫn §7](../DEVELOPER-GUIDE.md#7-tính-năng-máy-chủ-game).

### Bảo mật

* `server.key` là danh tính của server: ai có file này có thể giả mạo server. Giữ bí mật, đặt ngoài thư mục web
  (không để trong `public_html`), không commit vào git.
* Ticket không phải bí mật: cứ gửi cho client hoặc nhúng vào game. `token` và `license` là bí mật — lấy từ biến môi trường, đừng ghi cứng.
* Mọi dữ liệu được mã hoá đầu-cuối.

### Khắc phục sự cố

* Chạy `peerova doctor` (công cụ dòng lệnh trong SDK) để chẩn đoán mạng.
* Không tìm thấy thư viện / chương trình: kiểm tra `PEEROVA_LIB` / `PEEROVA_BIN`, quyền thực thi, `open_basedir`.
* `CONNECT_FAILED` do hết giờ: server chưa chạy, sai ticket/mã, hoặc tường lửa; tăng `timeout_ms`, chạy `peerova doctor` ở cả hai đầu.
* Client thứ 4 bị từ chối: server đang ở chế độ dev (tối đa 3 client) — thêm `license`.
* Gửi unreliable > 1180 byte bị từ chối — chia nhỏ hoặc dùng reliable.
* Kết nối mất ngay sau khi request web kết thúc: xem mục PHP-FPM ở trên.
* `E_IO`: thư mục chứa `key_file` không tồn tại hoặc không ghi được.
* Server không khởi động khi bật `hide_ip`: gói hiện tại không có ẩn IP (cần Pro/Studio/Enterprise hoặc bản dùng thử).
* Thêm: [hướng dẫn §15](../DEVELOPER-GUIDE.md#15-khắc-phục-sự-cố).

### FAQ

* **Người chơi có phải gõ mã không?** Không. Nhúng ticket hoặc tải từ URL của bạn; mã 6 ký tự chỉ để mời bạn bè vào phòng do người chơi mở.
* **Chạy Peerova trong request PHP-FPM được không?** Không nên — kết nối và process đóng khi request kết thúc. Dùng worker CLI chạy lâu.
* **Có cần mở port / IP tĩnh không?** Không; mở port chỉ giúp kết nối trực tiếp nhanh hơn.
* Thêm: [hướng dẫn §16](../DEVELOPER-GUIDE.md#16-faq).

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

Connections are automatic: direct first, with the **automatic encrypted route** (still end-to-end encrypted) when networks
block direct connections.

### Choosing an integration

* **A PHP server / worker talking to a game or app that uses the Peerova SDK** (scenarios 3/4 of the guide): use this binding (FFI or sidecar).
* **An existing program with TCP/UDP socket code** (scenarios 1/2): no SDK needed — run `peerova client --forward` next to it;
  a long-running PHP CLI script can start it itself, see [Existing program: run peerova hidden](#existing-program-run-peerova-hidden).
  Don't do this inside a PHP-FPM request.
* Full comparison: [guide §1](../DEVELOPER-GUIDE.md#1-choosing-an-integration).

**Players never type anything:** the game embeds the `pvt_...` ticket, or fetches the latest ticket from your own URL at startup. A PHP
worker can write the server's ticket to a file your website serves (`file_put_contents('/var/www/peerova-ticket.txt', $srv->ticket())`).
6-character codes are only for inviting friends to player-hosted rooms. Details: [guide §2](../DEVELOPER-GUIDE.md#2-players-never-type-anything).

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
// "pvt_..." ticket or 6-char code; key_file keeps the client's identity across runs (the folder must exist)
$cli = Peerova::client($ticketOrCode, ['key_file' => '/var/lib/myapp/peerova-client.key']);
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

### Existing program: run peerova hidden

When an existing program (game client, bot, admin tool…) already uses TCP/UDP sockets and you want a **long-running PHP CLI script**
to open it to a Peerova server without code changes: the script starts `peerova client`, waits for the local port to open, then the
program connects to `127.0.0.1:8888`. The server runs `peerova server --expose tcp:8888` ([guide §3](../DEVELOPER-GUIDE.md#3-existing-game-no-code-change)).
Log lines in detail: [guide §4](../DEVELOPER-GUIDE.md#4-the-game-runs-peerova-hidden-players-run-nothing). Game launchers are rarely written in PHP — if
yours uses another language, take the matching snippet from guide §4.

```php
<?php
// peerova_tunnel.php — PHP 7.4+ CLI / long-running worker. Runs the bundled `peerova` CLI and waits until the local port is open.
// The CLI's output (UTF-8) goes to a log file, so PHP never has to keep draining a pipe.

/** @return resource|null the running process once the forwarded port is open on 127.0.0.1, or null on failure/timeout */
function peerova_tunnel_start(string $exe, string $ticket, string $forward, string $keyFile, string $logFile, float $timeout = 30.0)
{
    if (!is_dir(dirname($keyFile))) {
        mkdir(dirname($keyFile), 0700, true);                       // the key folder must exist
    }
    $proc = proc_open(
        [$exe, 'client', $ticket, '--forward', $forward, '--key', $keyFile],   // array = no shell
        [0 => ['file', PHP_OS_FAMILY === 'Windows' ? 'NUL' : '/dev/null', 'r'],
         1 => ['file', $logFile, 'w'],
         2 => ['redirect', 1]],                                    // stderr ("lỗi: ...") into the same log
        $pipes);
    if (!is_resource($proc)) {
        return null;
    }
    register_shutdown_function('peerova_tunnel_stop', $proc);     // kill it when the script exits
    $deadline = microtime(true) + $timeout;
    while (microtime(true) < $deadline) {
        clearstatcache();
        // "✔ Tcp 127.0.0.1:8888 → server port 8888" = connected and the local port is open
        if (strpos((string)@file_get_contents($logFile), 'server port') !== false) {
            return $proc;
        }
        if (!proc_get_status($proc)['running']) {                 // exited first = failure (see the log)
            break;
        }
        usleep(100000);
    }
    peerova_tunnel_stop($proc);
    return null;
}

/** @param resource $proc */
function peerova_tunnel_stop($proc): void
{
    if (is_resource($proc)) {
        if (proc_get_status($proc)['running']) {
            proc_terminate($proc);
        }
        proc_close($proc);
    }
}
```

```php
require __DIR__ . '/peerova_tunnel.php';

$exe = __DIR__ . (PHP_OS_FAMILY === 'Windows' ? '/peerova.exe' : '/peerova');
$appData = getenv('APPDATA') ?: getenv('HOME') . '/.local/share';
$tunnel = peerova_tunnel_start($exe, $ticket, 'tcp:8888', "$appData/MyApp/peerova-client.key", "$appData/MyApp/peerova.log");
if ($tunnel) {
    $sock = stream_socket_client('tcp://127.0.0.1:8888');   // your existing socket code, unchanged
    // ... proc_get_status($tunnel)['running'] === false ⇒ disconnected
} else {
    fwrite(STDERR, "Could not reach the server (see peerova.log)\n");
}
```

* Tested on Linux (PHP 8.2 CLI): with a valid ticket it becomes ready and HTTP through `127.0.0.1` works; with a bad ticket the process
  exits at once, the log contains a `lỗi: ...` line and the function returns `null`.
* Needs PHP 7.4+ (array commands and `['redirect', 1]`). An array command bypasses the shell, so `proc_terminate` stops the `peerova` process itself.
* Output goes to a log file instead of a pipe because PHP is single-threaded: a pipe nobody keeps reading can fill up and stall `peerova`.
* PHP CLI runs in a console, so `peerova.exe` shares that console and opens no new window.
* **PHP-FPM / Apache: don't start a tunnel per request** — the process is killed (or left behind) when the request ends. Run it in a
  fixed CLI worker (systemd, supervisor) as in the PHP-FPM section above.
* `peerova stdio` (the `PeerovaStdio` backend) does **not** open a local port: it exchanges SDK messages as JSON lines — it is the wrong tool here.

### Disconnect and cleanup

* `$node->disconnect($peer)` closes one connection (a `DISCONNECTED` event follows).
* `$node->close()` drops all connections and frees the node (FFI) / stops the sidecar process. Calling it twice is
  fine; the destructor calls it too.
* Use a node from one thread only; never `close()` while another thread is inside `poll()` on it.
* Workers should catch `SIGINT`/`SIGTERM` (`pcntl_signal`) to leave the loop, then `close()`.

### Error handling

* `Peerova\PeerovaException`: FFI — the message comes from the native library (`peerova_last_error`): bad config,
  port in use, malformed ticket, unknown code, unknown peer, unreliable payload > 1180 bytes, ...
* Library error messages, and the reason in `CONNECT_FAILED` / `DISCONNECTED` events (`$ev->data`), start with a stable error-code name,
  e.g. `"E_INVALID_TICKET: ..."`, `"E_TIMEOUT: ..."`. Branch on the prefix; don't parse the rest:

```php
try {
    $cli = Peerova::client($code);
} catch (\Peerova\PeerovaException $e) {
    if (strpos($e->getMessage(), 'E_CODE_NOT_FOUND') === 0) { /* room does not exist or has closed */ }
}
if ($ev && $ev->type === EventType::CONNECT_FAILED && strpos($ev->data, 'E_CCU_LIMIT') === 0) { /* server is full */ }
```

  Code list (`E_INVALID_ARG`, `E_TOO_LARGE`, `E_UNKNOWN_PEER`, `E_CLOSED`, `E_CODE_NOT_FOUND`, `E_NETWORK`, `E_TIMEOUT`, `E_LICENSE`,
  `E_CCU_LIMIT`, `E_ACCESS_DENIED`, `E_INVALID_TICKET`, `E_IO`, `E_INTERNAL`): [guide — Error codes](../DEVELOPER-GUIDE.md#error-codes).
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
| `key_file` | `"server.key"` | server identity file; same key ⇒ same ticket. **Secret** |
| `license` | — | `PVA-...` key; without it: dev mode, max 3 clients |
| `max_ccu` | from license | cap on concurrent clients |
| `token` | — | shared secret clients must present |
| `upnp` | `true` | open the port on the router automatically |
| `private` | `false` | the ticket does not contain the server's IP addresses |
| `hide_ip` | `false` | Hide the server IP: players never learn its real address (DDoS protection); slightly higher latency. Pro/Studio/Enterprise plans and the trial |
| `app` | — | project id from the Peerova website (needed for connect codes) |
| `share` | — | `['name' => '...', 'public' => true, 'max' => 16, 'mode' => '...', 'region' => 'VN']` — list the room and get a 6-char code; `public` = shown in the public room list |

Client — `Peerova::client($ticketOrCode, [...])` / `PeerovaStdio::client(...)`:

| Key | Default | Meaning |
|---|---|---|
| `token` | — | must match the server |
| `timeout_ms` | `20000` | max time before `CONNECT_FAILED` |
| `key_file` | — (new random key per run) | the client's identity file; created if missing (**the folder must exist**). Store it in the app's / user's data folder, e.g. `"<appdata>/<game>/peerova-client.key"`. Keeping it lets the server see the client at the same stable address; banning by address works per player only while they keep that file |

### Game servers

Dedicated game servers usually run the `peerova server` command-line tool (several `--expose`, separate processes for Remote Desktop
and the game, a `127.a.b.c` address per player + `--banned`, `--hide-ip`, VPN mode): see [guide §7](../DEVELOPER-GUIDE.md#7-game-server-features).

### Security

* `server.key` is the server's identity: anyone who has it can impersonate the server. Keep it secret, outside the
  web root (not in `public_html`), and don't commit it.
* The ticket is not a secret: share it with clients or embed it in the game. `token` and `license` are secrets — read them
  from environment variables, don't hard-code them.
* All traffic is end-to-end encrypted.

### Troubleshooting

* Run `peerova doctor` (the SDK's command-line tool) for a network diagnosis.
* Library / program not found: check `PEEROVA_LIB` / `PEEROVA_BIN`, the execute permission, `open_basedir`.
* `CONNECT_FAILED` with a timeout: server not running, wrong ticket/code, or a firewall; raise `timeout_ms`, run
  `peerova doctor` on both ends.
* A 4th client is refused: the server runs in dev mode (max 3 clients) — add a `license`.
* Unreliable sends over 1180 bytes are rejected — split them or send reliably.
* Connections drop as soon as the web request ends: see the PHP-FPM section above.
* `E_IO`: the `key_file` folder doesn't exist or isn't writable.
* The server won't start with `hide_ip`: your plan doesn't include hiding the IP (needs Pro/Studio/Enterprise or the trial).
* More: [guide §15](../DEVELOPER-GUIDE.md#15-troubleshooting).

### FAQ

* **Do players need to type a code?** No. Embed the ticket or fetch it from your URL; 6-character codes are only for inviting friends to player-hosted rooms.
* **Can I run Peerova inside a PHP-FPM request?** Not usefully — connections and processes end with the request. Use a long-running CLI worker.
* **Do I need port forwarding / a static IP?** No; an open port only makes direct connections faster.
* More: [guide §16](../DEVELOPER-GUIDE.md#16-faq-1).

### Tests

```sh
cd bindings/php && php -d ffi.enable=1 tests/test.php    # runs the sidecar and FFI variants (FFI skipped without ext/ffi)
```
