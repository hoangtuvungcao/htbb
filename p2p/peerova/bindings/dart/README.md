# Peerova cho Dart / Flutter

[Tiếng Việt](#tiếng-việt) · [English](#english)

Website: https://peerova.pages.dev · Khái niệm và toàn bộ C API: [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md)

---

## Tiếng Việt

Binding `dart:ffi` (`lib/peerova.dart`) cho thư viện Peerova: kết nối ngang hàng (P2P), mã hoá đầu-cuối,
gửi tin cậy (reliable) và không tin cậy (unreliable) trên 256 kênh. Chạy trong Dart VM (CLI, server) và Flutter
(Android, iOS, macOS, Windows, Linux). Kết nối hoàn toàn tự động: thử kết nối trực tiếp trước, nếu mạng chặn thì
tự dùng một đường dự phòng đã mã hoá.

### Yêu cầu

* Dart SDK 3.0 trở lên (Flutter 3.10+). Đã kiểm thử: Dart VM trên Linux x64; Flutter 3.38 Linux desktop.
* Gói `ffi` (`^2.1.0`, tự cài khi `dart pub get`).
* Thư viện native của Peerova cho nền tảng đích (xem bên dưới).

### Bộ SDK gồm những gì

```
include/peerova.h                    C header (tham chiếu)
lib/linux-x86_64/libpeerova.so       thư viện Linux (+ libpeerova.a, và chương trình peerova)
lib/windows-x86_64/peerova.dll       thư viện Windows (+ peerova.exe)
bindings/dart/                       gói Dart này
```

### Đặt thư viện native ở đâu

| Nền tảng | File | Vị trí |
|---|---|---|
| Linux (Dart CLI) | `libpeerova.so` | cạnh file chạy / thư mục làm việc, hoặc đặt biến môi trường `PEEROVA_LIB=/đường/dẫn/libpeerova.so`, hoặc `LD_LIBRARY_PATH` |
| Windows (Dart CLI) | `peerova.dll` | cạnh file `.exe`, hoặc `PEEROVA_LIB`. **Không** cần `wintun.dll` (chỉ chế độ VPN của công cụ dòng lệnh `peerova` mới cần) |
| Flutter Linux / Windows / Android / iOS / macOS | | xem mục [Flutter](#flutter) |

Trong code có thể chỉ định đường dẫn trước lần gọi Peerova đầu tiên: `peerovaLibraryPath = '/opt/app/libpeerova.so';`

Thứ tự tìm: `peerovaLibraryPath` → `PEEROVA_LIB` → (desktop) thư mục làm việc / thư mục script → (macOS)
`Contents/Frameworks/` của app → bộ nạp của hệ thống (`DynamicLibrary.open`). iOS luôn dùng ký hiệu đã liên kết
tĩnh vào app (`DynamicLibrary.process()`).

### Cài đặt

```yaml
# pubspec.yaml của bạn
dependencies:
  peerova:
    path: ../peerova-sdk/bindings/dart
```

```sh
dart pub get        # hoặc: flutter pub get
```

### Bắt đầu trong 5 phút

Server:

```dart
import 'package:peerova/peerova.dart';

final srv = PeerovaServer.start({'port': 47000, 'key_file': 'server.key'}); // chặn tối đa ~3 s
print('ticket: ${srv.ticket}');           // pvt_... — gửi cho client
// Mã kết nối 6 ký tự (chỉ khi khởi động với 'app' + 'share', xem Cấu hình); có thể là null lúc đầu:
print('code: ${srv.roomCode}');
```

Client:

```dart
// ticket "pvt_..." hoặc mã 6 ký tự. Với ticket: trả về ngay; với mã: tra mã trên website trước (chặn chốc lát)
final cli = PeerovaClient.connect(ticketOrCode);
switch (cli.pollWait(const Duration(seconds: 20))) {
  case ConnectedEvent(:final peer):
    cli.sendText(peer, 'xin chào');                // reliable, kênh 0
  case ConnectFailedEvent(:final reason):
    print('không kết nối được: $reason');
  default:
}
```

Ví dụ chạy được: `examples/chat.dart` (chat + đồng bộ vị trí, chạy trong terminal) và
`examples/flutter_chat/` (widget Flutter):

```sh
cd bindings/dart && dart pub get
dart run examples/chat.dart server                   # in ra ticket
dart run examples/chat.dart client <ticket|mã>       # gõ để chat, /quit để thoát
```

### Gửi: reliable hay unreliable

```dart
node.send(peer, bytes, channel: 1, reliable: false);  // unreliable
node.sendText(peer, 'gg', channel: 0);                // reliable (mặc định)
```

| | `reliable: true` (mặc định) | `reliable: false` |
|---|---|---|
| Đảm bảo | đến đủ, đúng thứ tự **trong cùng kênh** | có thể mất hoặc đảo thứ tự |
| Kích thước | lớn tuỳ ý | tối đa **1180 byte** mỗi tin (`maxUnreliablePayload`); lớn hơn sẽ bị từ chối |
| Dùng cho | chat, túi đồ, RPC, lệnh, khối dữ liệu file | input gameplay, vị trí, trạng thái thời gian thực kiểu voice — tin sau thay thế tin trước |

Kênh 0..255. Các kênh khác nhau không chặn nhau: một file lớn trên kênh 5 không làm chậm chat trên kênh 0.

### Nhận sự kiện

`poll()` không chặn, trả về `null` khi hết sự kiện. `pollWait(timeout)` chờ tối đa `timeout` (chặn cả isolate).

| Lớp | Ý nghĩa |
|---|---|
| `ConnectedEvent(peer)` | server: một client vào; client: đã kết nối tới server |
| `MessageEvent(peer, channel, reliable, data, text)` | có tin nhắn; `data` là `Uint8List` (bản sao, giữ lại được) |
| `DisconnectedEvent(peer, reason)` | kết nối đã đóng |
| `ConnectFailedEvent(reason)` | chỉ client: không kết nối được (hết giờ, sai token, server đầy…) |

Vòng lặp trong CLI / server (một isolate riêng):

```dart
while (running) {
  final ev = srv.pollWait(const Duration(milliseconds: 50));
  if (ev case MessageEvent(:final peer, :final text)) print('$peer: $text');
}
```

Vòng lặp trên event loop (Flutter, hoặc CLI dùng `async`): xả hết sự kiện mỗi tick:

```dart
final timer = Timer.periodic(const Duration(milliseconds: 16), (_) {
  PeerovaEvent? ev;
  while ((ev = node.poll()) != null) handle(ev!);
});
```

### Flutter

**Vòng lặp.** `PeerovaClient.connect` với ticket trả về ngay (với mã kết nối thì chặn chốc lát để tra mã); `PeerovaServer.start` chặn tối đa ~3 s (hiện spinner trước).
Sau đó gọi `poll()` (không chặn) từ `Timer.periodic` trên isolate UI như trên — đây là cách đã kiểm thử. Không
gọi `pollWait` trên isolate UI. Nếu cần tải nặng: tạo một `Isolate` sở hữu node, lặp `pollWait(50 ms)` và gửi sự
kiện về UI qua `SendPort` (node native an toàn đa luồng).

**Đóng gói thư viện** (tên file phải giữ nguyên):

| Nền tảng | Cách làm |
|---|---|
| Linux | Thêm vào cuối `linux/CMakeLists.txt`:<br>`install(FILES "${CMAKE_CURRENT_SOURCE_DIR}/libs/libpeerova.so" DESTINATION "${INSTALL_BUNDLE_LIB_DIR}" COMPONENT Runtime)`<br>File sẽ nằm ở `build/linux/x64/<mode>/bundle/lib/`, cách tìm mặc định nạp được (đã kiểm thử). |
| Windows | Thêm vào `windows/CMakeLists.txt`:<br>`install(FILES "${CMAKE_CURRENT_SOURCE_DIR}/libs/peerova.dll" DESTINATION "${INSTALL_BUNDLE_LIB_DIR}" COMPONENT Runtime)`<br>→ `peerova.dll` nằm cạnh `.exe` của app. |
| Android | Liên hệ Peerova để nhận bản Android. Đặt `android/app/src/main/jniLibs/<abi>/libpeerova.so` (`arm64-v8a`, `armeabi-v7a`, `x86_64`); thêm `<uses-permission android:name="android.permission.INTERNET"/>` vào `AndroidManifest.xml`. |
| iOS | Liên hệ Peerova để nhận bản iOS (`libpeerova.a`, liên kết tĩnh). Thêm vào Runner (*Frameworks, Libraries*), và đặt `OTHER_LDFLAGS = -force_load $(PROJECT_DIR)/libpeerova.a` để ký hiệu không bị loại bỏ (Dart tìm chúng lúc chạy qua `DynamicLibrary.process()`). |
| macOS | Liên hệ Peerova để nhận bản macOS (`libpeerova.dylib`). Xcode → Runner → *Build Phases* → *Copy Files* (Destination: Frameworks) → app có `Contents/Frameworks/libpeerova.dylib` (binding tự tìm ở đó). Bật entitlement `com.apple.security.network.client` và `com.apple.security.network.server`. |

Ví dụ: `examples/flutter_chat/lib/main.dart` + `examples/flutter_chat/pubspec.yaml`. Tạo project bằng
`flutter create my_chat`, chép `main.dart`, thêm dependency `peerova`, thêm dòng `install(...)` ở trên, rồi `flutter run`.

### Ngắt kết nối và dọn dẹp

* `node.disconnect(peer)` đóng một kết nối (sau đó có `DisconnectedEvent`).
* `node.close()` ngắt mọi kết nối và giải phóng node; gọi nhiều lần không sao. Một `NativeFinalizer` sẽ giải phóng
  node bị quên, nhưng chỉ khi GC chạy — hãy tự gọi `close()`.
* **Không bao giờ** `close()` khi một isolate khác đang `poll`/`pollWait`/`send` trên node đó: dừng vòng lặp
  (`timer.cancel()`) trước rồi mới `close()`.

### Xử lý lỗi

* `PeerovaException(message)`: lỗi từ thư viện native (cấu hình sai, cổng đang dùng, ticket hỏng, peer không tồn
  tại, mã kết nối không tồn tại / phòng đã đóng, gói unreliable > 1180 byte…). `message` lấy từ `peerova_last_error`.
* `StateError`: dùng node đã `close()`. `RangeError`: kênh ngoài 0..255. `ArgumentError`: ticket rỗng, hoặc
  không tìm thấy thư viện native (`Failed to load dynamic library`).
* Lỗi mạng của client đến dưới dạng `ConnectFailedEvent`, không phải exception.

### Cấu hình

Server — `PeerovaServer.start({...})`:

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
| `share` | — | `{"name": "...", "public": true, "max": 16, "mode": "...", "region": "VN"}` — đăng phòng và nhận mã 6 ký tự |

Client — `PeerovaClient.connect(ticketOrCode, {...})`: `token` (phải khớp server), `timeout_ms` (mặc định `20000`).

### Bảo mật

* `server.key` là danh tính của server: ai có file này có thể giả mạo server. Giữ bí mật, không commit vào git.
* Ticket gần như công khai: cứ gửi cho client. `token` và `license` là bí mật.
* Mọi dữ liệu được mã hoá đầu-cuối.

### Khắc phục sự cố

* Chạy `peerova doctor` (công cụ dòng lệnh trong SDK) để chẩn đoán mạng.
* `Failed to load dynamic library 'libpeerova.so'`: kiểm tra vị trí file, hoặc đặt `PEEROVA_LIB` / `peerovaLibraryPath`.
* `ConnectFailedEvent` do hết giờ: server chưa chạy, sai ticket/mã, hoặc tường lửa; tăng `timeout_ms`, chạy `peerova doctor` ở cả hai đầu.
* Client thứ 4 bị từ chối: server đang ở chế độ dev (tối đa 3 client) — thêm `license`.
* `PeerovaException` khi gửi unreliable: gói > 1180 byte — chia nhỏ hoặc dùng reliable.

### Kiểm thử

```sh
cd bindings/dart && dart pub get && dart run test/peerova_test.dart
```

---

## English

`dart:ffi` binding (`lib/peerova.dart`) for the Peerova library: peer-to-peer connections, end-to-end
encryption, reliable and unreliable delivery on 256 channels. Runs in the Dart VM (CLI tools, servers) and in Flutter
(Android, iOS, macOS, Windows, Linux). Connections are automatic: direct first, with an automatic encrypted fallback
route when networks block direct connections.

### Requirements

* Dart SDK 3.0 or newer (Flutter 3.10+). Tested: Dart VM on Linux x64; Flutter 3.38 Linux desktop.
* Package `ffi` (`^2.1.0`, installed by `dart pub get`).
* The Peerova native library for the target platform (see below).

### SDK contents

```
include/peerova.h                    C header (reference)
lib/linux-x86_64/libpeerova.so       Linux library (+ libpeerova.a, and the peerova program)
lib/windows-x86_64/peerova.dll       Windows library (+ peerova.exe)
bindings/dart/                       this Dart package
```

### Where to put the native library

| Platform | File | Location |
|---|---|---|
| Linux (Dart CLI) | `libpeerova.so` | next to the executable / working directory, or set `PEEROVA_LIB=/path/to/libpeerova.so`, or `LD_LIBRARY_PATH` |
| Windows (Dart CLI) | `peerova.dll` | next to the `.exe`, or `PEEROVA_LIB`. `wintun.dll` is **not** needed (only the VPN mode of the `peerova` command-line tool uses it) |
| Flutter Linux / Windows / Android / iOS / macOS | | see [Flutter](#flutter-1) |

You can also set the path in code before the first Peerova call: `peerovaLibraryPath = '/opt/app/libpeerova.so';`

Lookup order: `peerovaLibraryPath` → `PEEROVA_LIB` → (desktop) working directory / script directory → (macOS) the
app's `Contents/Frameworks/` → the platform loader (`DynamicLibrary.open`). iOS always uses the symbols statically
linked into the app (`DynamicLibrary.process()`).

### Install

```yaml
# your pubspec.yaml
dependencies:
  peerova:
    path: ../peerova-sdk/bindings/dart
```

```sh
dart pub get        # or: flutter pub get
```

### 5-minute quick start

Server:

```dart
import 'package:peerova/peerova.dart';

final srv = PeerovaServer.start({'port': 47000, 'key_file': 'server.key'}); // blocks up to ~3 s
print('ticket: ${srv.ticket}');           // pvt_... — give it to clients
// 6-character connect code (only when started with 'app' + 'share', see Configuration); may be null at first:
print('code: ${srv.roomCode}');
```

Client:

```dart
// "pvt_..." ticket or 6-char code. Ticket: returns immediately; code: looked up on the website first (blocks briefly)
final cli = PeerovaClient.connect(ticketOrCode);
switch (cli.pollWait(const Duration(seconds: 20))) {
  case ConnectedEvent(:final peer):
    cli.sendText(peer, 'hello');                   // reliable, channel 0
  case ConnectFailedEvent(:final reason):
    print('could not connect: $reason');
  default:
}
```

Runnable examples: `examples/chat.dart` (chat + position sync in a terminal) and `examples/flutter_chat/`
(a Flutter widget):

```sh
cd bindings/dart && dart pub get
dart run examples/chat.dart server                    # prints the ticket
dart run examples/chat.dart client <ticket|code>      # type to chat, /quit to leave
```

### Sending: reliable vs unreliable

```dart
node.send(peer, bytes, channel: 1, reliable: false);  // unreliable
node.sendText(peer, 'gg', channel: 0);                // reliable (default)
```

| | `reliable: true` (default) | `reliable: false` |
|---|---|---|
| Guarantee | delivered, in order **within the channel** | may be lost or reordered |
| Size | any size | at most **1180 bytes** per message (`maxUnreliablePayload`); larger sends are rejected |
| Use for | chat, inventory, RPCs, commands, file chunks | gameplay inputs, positions, voice-like realtime state — each update supersedes the previous one |

Channels are 0..255. Different channels don't block each other: a large file on channel 5 does not delay chat on
channel 0.

### Receiving events

`poll()` never blocks and returns `null` when no event is pending. `pollWait(timeout)` waits up to `timeout`
(blocking the whole isolate).

| Class | Meaning |
|---|---|
| `ConnectedEvent(peer)` | server: a client joined; client: connected to the server |
| `MessageEvent(peer, channel, reliable, data, text)` | a message; `data` is a `Uint8List` copy, safe to keep |
| `DisconnectedEvent(peer, reason)` | the connection closed |
| `ConnectFailedEvent(reason)` | client only: could not connect (timeout, wrong token, server full, ...) |

Loop in a CLI tool / server (its own isolate):

```dart
while (running) {
  final ev = srv.pollWait(const Duration(milliseconds: 50));
  if (ev case MessageEvent(:final peer, :final text)) print('$peer: $text');
}
```

Loop on the event loop (Flutter, or an `async` CLI): drain all pending events every tick:

```dart
final timer = Timer.periodic(const Duration(milliseconds: 16), (_) {
  PeerovaEvent? ev;
  while ((ev = node.poll()) != null) handle(ev!);
});
```

### Flutter

**Loop.** `PeerovaClient.connect` with a ticket returns immediately (with a connect code it blocks briefly for the lookup); `PeerovaServer.start` blocks up to ~3 s (show a spinner
first). Then call the non-blocking `poll()` from `Timer.periodic` on the UI isolate as above — this is the tested
setup. Never call `pollWait` on the UI isolate. For heavy traffic, spawn an `Isolate` that owns the node, loops
`pollWait(50 ms)` and forwards events to the UI through a `SendPort` (native nodes are thread-safe).

**Bundling the library** (keep the file name unchanged):

| Platform | How |
|---|---|
| Linux | Append to `linux/CMakeLists.txt`:<br>`install(FILES "${CMAKE_CURRENT_SOURCE_DIR}/libs/libpeerova.so" DESTINATION "${INSTALL_BUNDLE_LIB_DIR}" COMPONENT Runtime)`<br>The file lands in `build/linux/x64/<mode>/bundle/lib/` and the default lookup loads it (tested). |
| Windows | Append to `windows/CMakeLists.txt`:<br>`install(FILES "${CMAKE_CURRENT_SOURCE_DIR}/libs/peerova.dll" DESTINATION "${INSTALL_BUNDLE_LIB_DIR}" COMPONENT Runtime)`<br>→ `peerova.dll` ends up next to the app's `.exe`. |
| Android | Contact Peerova for Android builds. Put `android/app/src/main/jniLibs/<abi>/libpeerova.so` (`arm64-v8a`, `armeabi-v7a`, `x86_64`); add `<uses-permission android:name="android.permission.INTERNET"/>` to `AndroidManifest.xml`. |
| iOS | Contact Peerova for iOS builds (`libpeerova.a`, static). Add it to Runner (*Frameworks, Libraries*) and set `OTHER_LDFLAGS = -force_load $(PROJECT_DIR)/libpeerova.a` so the symbols are not stripped (Dart looks them up at runtime via `DynamicLibrary.process()`). |
| macOS | Contact Peerova for macOS builds (`libpeerova.dylib`). Xcode → Runner → *Build Phases* → *Copy Files* (Destination: Frameworks) → the app contains `Contents/Frameworks/libpeerova.dylib` (the binding looks there). Enable the `com.apple.security.network.client` and `com.apple.security.network.server` entitlements. |

Example: `examples/flutter_chat/lib/main.dart` + `examples/flutter_chat/pubspec.yaml`. Create a project with
`flutter create my_chat`, copy `main.dart`, add the `peerova` dependency and the `install(...)` line above, then
`flutter run`.

### Disconnect and cleanup

* `node.disconnect(peer)` closes one connection (a `DisconnectedEvent` follows).
* `node.close()` drops all connections and frees the node; calling it twice is fine. A `NativeFinalizer` frees
  forgotten nodes, but only when the GC runs — call `close()` yourself.
* **Never** `close()` while another isolate is inside `poll`/`pollWait`/`send` on that node: stop the loop
  (`timer.cancel()`) first, then `close()`.

### Error handling

* `PeerovaException(message)`: a native failure (bad config, port in use, malformed ticket, unknown peer,
  unknown or closed connect code, unreliable payload > 1180 bytes, ...). `message` comes from `peerova_last_error`.
* `StateError`: using a closed node. `RangeError`: channel outside 0..255. `ArgumentError`: empty ticket, or the
  native library could not be loaded (`Failed to load dynamic library`).
* Client network failures arrive as `ConnectFailedEvent`, not as exceptions.

### Configuration

Server — `PeerovaServer.start({...})`:

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
| `share` | — | `{"name": "...", "public": true, "max": 16, "mode": "...", "region": "VN"}` — list the room and get a 6-char code |

Client — `PeerovaClient.connect(ticketOrCode, {...})`: `token` (must match the server), `timeout_ms` (default `20000`).

### Security

* `server.key` is the server's identity: anyone who has it can impersonate the server. Keep it secret, don't commit it.
* The ticket is public-ish: share it with clients. `token` and `license` are secrets.
* All traffic is end-to-end encrypted.

### Troubleshooting

* Run `peerova doctor` (the SDK's command-line tool) for a network diagnosis.
* `Failed to load dynamic library 'libpeerova.so'`: check the file location, or set `PEEROVA_LIB` / `peerovaLibraryPath`.
* `ConnectFailedEvent` with a timeout: server not running, wrong ticket/code, or a firewall; raise `timeout_ms`,
  run `peerova doctor` on both ends.
* A 4th client is refused: the server runs in dev mode (max 3 clients) — add a `license`.
* `PeerovaException` on an unreliable send: payload > 1180 bytes — split it or send reliably.

### Tests

```sh
cd bindings/dart && dart pub get && dart run test/peerova_test.dart
```
