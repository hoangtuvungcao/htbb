# Peerova cho Dart / Flutter

[Tiếng Việt](#tiếng-việt) · [English](#english)

Website: https://peerova.pages.dev · Khái niệm và toàn bộ C API: [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md)

---

## Tiếng Việt

Binding `dart:ffi` (`lib/peerova.dart`) cho thư viện Peerova: kết nối ngang hàng (P2P), mã hoá đầu-cuối,
gửi tin cậy (reliable) và không tin cậy (unreliable) trên 256 kênh. Chạy trong Dart VM (CLI, server) và Flutter
(Android, iOS, macOS, Windows, Linux). Kết nối hoàn toàn tự động: thử kết nối trực tiếp trước, nếu mạng chặn thì
tự dùng **đường mã hoá tự động** (vẫn mã hoá đầu-cuối).

### Chọn cách tích hợp

* **Game/app mới, hoặc host trong game** (tình huống 3/4 của hướng dẫn): dùng SDK Dart này — mọi nền tảng Flutter, kể cả Android/iOS.
* **Game có sẵn dùng socket TCP/UDP, người chơi không phải chạy gì** (tình huống 2): app desktop tự chạy `peerova client` ẩn —
  xem [Game có sẵn: chạy peerova ẩn](#game-có-sẵn-chạy-peerova-ẩn) (chỉ desktop; mobile không chạy được process con → dùng SDK).
* Bảng so sánh đầy đủ: [hướng dẫn §1](../DEVELOPER-GUIDE.md#1-chọn-cách-tích-hợp).

**Người chơi không phải gõ gì:** nhúng ticket `pvt_...` vào app, hoặc tải ticket mới nhất từ URL của bạn khi app khởi động
(dùng ticket nhúng nếu tải không được). Mã 6 ký tự chỉ dùng để mời bạn bè vào phòng do người chơi mở. Chi tiết:
[hướng dẫn §2](../DEVELOPER-GUIDE.md#2-người-chơi-không-phải-gõ-gì).

```dart
var ticket = embeddedTicket;                       // ticket nhúng trong bản build
try {
  final c = HttpClient()..connectionTimeout = const Duration(seconds: 5);
  final r = await (await c.getUrl(Uri.parse('https://mygame.example.com/peerova-ticket.txt'))).close();
  if (r.statusCode == 200) ticket = (await r.transform(utf8.decoder).join()).trim();
} catch (_) {}
```

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
print('ticket: ${srv.ticket}');           // pvt_... — nhúng vào game / gửi cho client
// Mã kết nối 6 ký tự (chỉ khi khởi động với 'app' + 'share', xem Cấu hình); có thể là null lúc đầu:
print('code: ${srv.roomCode}');
```

Client:

```dart
// ticket "pvt_..." hoặc mã 6 ký tự. Với ticket: trả về ngay; với mã: tra mã trên website trước (chặn chốc lát)
// key_file: giữ danh tính người chơi giữa các lần chạy (thư mục phải tồn tại)
final cli = PeerovaClient.connect(ticketOrCode, {'key_file': '$appData/MyGame/peerova-client.key'});
switch (cli.pollWait(const Duration(seconds: 20))) {
  case ConnectedEvent(:final peer):
    cli.sendText(peer, 'xin chào');                // reliable, kênh 0
  case ConnectFailedEvent(:final reason):
    print('không kết nối được: $reason');          // "E_TIMEOUT: ..."
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

`key_file` của server/client phải nằm ở thư mục ghi được — trong app thật dùng thư mục dữ liệu ứng dụng
(ví dụ `getApplicationSupportDirectory()` của gói `path_provider`), không dùng thư mục làm việc.

### Game có sẵn: chạy peerova ẩn

Dành cho app/game desktop (Dart CLI, Flutter Windows/Linux/macOS) đã có code socket TCP/UDP và không muốn đổi sang SDK:
phát hành `peerova.exe` (Windows) / `peerova` (Linux, macOS) kèm app; app tự chạy `peerova client`, chờ cổng local mở,
rồi code socket cũ kết nối `127.0.0.1:8888`. Máy chủ chạy `peerova server --expose tcp:8888`
([hướng dẫn §3](../DEVELOPER-GUIDE.md#3-game-có-sẵn-không-sửa-code)). Chi tiết và các dòng log:
[hướng dẫn §4](../DEVELOPER-GUIDE.md#4-game-tự-chạy-peerova-ẩn-người-chơi-không-chạy-gì).

```dart
// peerova_tunnel.dart — chỉ desktop (Dart CLI, Flutter Windows/Linux/macOS).
// Chạy chương trình `peerova` đi kèm và chờ tới khi cổng local mở.
import 'dart:async';
import 'dart:convert';
import 'dart:io';

class PeerovaTunnel {
  PeerovaTunnel._(this.process);
  final Process process;

  /// Hoàn thành khi `peerova` thoát (mất kết nối, bị kill...).
  Future<int> get exitCode => process.exitCode;

  /// Trả về tunnel khi cổng forward đã mở trên 127.0.0.1, hoặc null nếu lỗi / hết giờ.
  static Future<PeerovaTunnel?> start(String exe, String ticket, String forward, String keyFile,
      {Duration timeout = const Duration(seconds: 30), void Function(String)? onLog}) async {
    await File(keyFile).parent.create(recursive: true);          // thư mục chứa key phải tồn tại
    final p = await Process.start(
        exe, ['client', ticket, '--forward', forward, '--key', keyFile]);
    p.stdin.close();
    final ready = Completer<bool>();
    void onLine(String line) {
      (onLog ?? print)(line);
      // "✔ Tcp 127.0.0.1:8888 → server port 8888" = đã kết nối và cổng local đã mở
      if (line.contains('server port') && !ready.isCompleted) ready.complete(true);
    }
    for (final s in [p.stdout, p.stderr]) {                       // đọc hết cả hai pipe, UTF-8, từng dòng
      s.transform(const Utf8Decoder(allowMalformed: true))
          .transform(const LineSplitter())
          .listen(onLine);
    }
    p.exitCode.then((_) { if (!ready.isCompleted) ready.complete(false); });   // thoát trước = thất bại
    final ok = await ready.future.timeout(timeout, onTimeout: () => false);
    if (!ok) { p.kill(); return null; }
    return PeerovaTunnel._(p);
  }

  /// Dừng `peerova`. Gọi khi game thoát.
  void stop() => process.kill();
}
```

```dart
final dir = File(Platform.resolvedExecutable).parent.path;
final exe = '$dir${Platform.pathSeparator}${Platform.isWindows ? 'peerova.exe' : 'peerova'}';
final appData = Platform.environment['APPDATA'] ?? '${Platform.environment['HOME']}/.local/share';
final key = '$appData/MyGame/peerova-client.key';

final tunnel = await PeerovaTunnel.start(exe, ticket, 'tcp:8888', key);
if (tunnel != null) {
  await connectToGameServer('127.0.0.1', 8888);   // code socket có sẵn, không đổi
  tunnel.exitCode.then((_) => showError('Mất kết nối'));
} else {
  showError('Không kết nối được máy chủ');
}
// khi thoát game: tunnel?.stop();
```

* Đã kiểm thử trên Linux (Dart VM): đúng ticket → sẵn sàng rồi HTTP qua `127.0.0.1` chạy được; ticket sai → process thoát
  với dòng `lỗi: ...` trên stderr và hàm trả về `null`.
* **Windows:** `Process.start` của Dart không có tuỳ chọn ẩn cửa sổ console. App Flutter Windows là app GUI; việc chạy
  `peerova.exe` (chương trình console) từ đó có làm hiện cửa sổ console hay không thì **chúng tôi chưa kiểm chứng** — hãy thử
  bằng bản build release trên máy Windows. Nếu cửa sổ hiện ra, dùng SDK Dart ở trên (không cần process con) cho cách chắc chắn nhất.
  Từ Dart CLI chạy trong console, `peerova.exe` dùng chung console đó.
* **Thoát app:** Dart không tự kill process con khi app thoát. Gọi `tunnel.stop()` trong đường tắt app của bạn (Flutter:
  `AppLifecycleListener(onExitRequested: ...)`; CLI: `ProcessSignal.sigint.watch()`).
* Linux/macOS: file `peerova` phải có quyền chạy (`chmod +x`); macOS: ký (codesign) cùng app.
* Nhiều `--forward` → chờ đủ số dòng `server port`. Muốn tránh đụng cổng: `tcp:38888=8888`.
* **Android / iOS không chạy được process con** → dùng SDK Dart.
* `peerova stdio` **không** dùng được cho cách này: nó không mở cổng local, mà trao đổi message SDK dạng JSON lines
  (xem [`../stdio/README.md`](../stdio/README.md)). Trong Dart cứ dùng thẳng SDK FFI ở trên.

### Ngắt kết nối và dọn dẹp

* `node.disconnect(peer)` đóng một kết nối (sau đó có `DisconnectedEvent`).
* `node.close()` ngắt mọi kết nối và giải phóng node; gọi nhiều lần không sao. Một `NativeFinalizer` sẽ giải phóng
  node bị quên, nhưng chỉ khi GC chạy — hãy tự gọi `close()`.
* **Không bao giờ** `close()` khi một isolate khác đang `poll`/`pollWait`/`send` trên node đó: dừng vòng lặp
  (`timer.cancel()`) trước rồi mới `close()`.

### Xử lý lỗi

* `PeerovaException(message)`: lỗi từ thư viện native (cấu hình sai, cổng đang dùng, ticket hỏng, peer không tồn
  tại, mã kết nối không tồn tại / phòng đã đóng, gói unreliable > 1180 byte…). `message` lấy từ `peerova_last_error`
  và bắt đầu bằng tên mã lỗi ổn định, ví dụ `"E_TOO_LARGE: ..."`.
* `ConnectFailedEvent.reason` và `DisconnectedEvent.reason` cũng bắt đầu bằng tên mã (`"E_TIMEOUT: ..."`, `"E_ACCESS_DENIED: ..."`).
  Xử lý theo loại lỗi bằng tiền tố, không phân tích phần còn lại:

```dart
try {
  cli = PeerovaClient.connect(code);
} on PeerovaException catch (e) {
  if (e.message.startsWith('E_CODE_NOT_FOUND')) showError('Phòng không tồn tại hoặc đã đóng');
}
// ...
if (ev case ConnectFailedEvent(:final reason) when reason.startsWith('E_CCU_LIMIT')) showError('Máy chủ đầy');
```

  Danh sách mã (`E_INVALID_ARG`, `E_TOO_LARGE`, `E_UNKNOWN_PEER`, `E_CLOSED`, `E_CODE_NOT_FOUND`, `E_NETWORK`, `E_TIMEOUT`,
  `E_LICENSE`, `E_CCU_LIMIT`, `E_ACCESS_DENIED`, `E_INVALID_TICKET`, `E_IO`, `E_INTERNAL`): [hướng dẫn — Mã lỗi](../DEVELOPER-GUIDE.md#mã-lỗi).
* `StateError`: dùng node đã `close()`. `RangeError`: kênh ngoài 0..255. `ArgumentError`: ticket rỗng, hoặc
  không tìm thấy thư viện native (`Failed to load dynamic library`).
* Lỗi mạng của client đến dưới dạng `ConnectFailedEvent`, không phải exception.

### Cấu hình

Server — `PeerovaServer.start({...})`:

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
| `share` | — | `{"name": "...", "public": true, "max": 16, "mode": "...", "region": "VN"}` — đăng phòng và nhận mã 6 ký tự; `public` = hiện trong danh sách phòng công khai |

Client — `PeerovaClient.connect(ticketOrCode, {...})`:

| Khoá | Mặc định | Ý nghĩa |
|---|---|---|
| `token` | — | phải khớp server |
| `timeout_ms` | `20000` | thời gian tối đa trước `ConnectFailedEvent` |
| `key_file` | — (key ngẫu nhiên mỗi lần chạy) | file danh tính người chơi; tạo mới nếu chưa có (**thư mục phải tồn tại**). Lưu trong thư mục dữ liệu ứng dụng, ví dụ `"<appdata>/<game>/peerova-client.key"`. Giữ file này thì máy chủ thấy người chơi ở cùng một địa chỉ ổn định; cấm theo địa chỉ chỉ có tác dụng với từng người chơi khi họ còn giữ file này |

### Máy chủ game

Máy chủ dedicated thường chạy công cụ dòng lệnh `peerova server` (nhiều `--expose`, tách process cho Remote Desktop và game,
mỗi người chơi một địa chỉ `127.a.b.c` + `--banned`, `--hide-ip`, chế độ VPN): xem [hướng dẫn §7](../DEVELOPER-GUIDE.md#7-tính-năng-máy-chủ-game).

### Bảo mật

* `server.key` là danh tính của server: ai có file này có thể giả mạo server. Giữ bí mật, không commit vào git.
* Ticket không phải bí mật: nhúng vào app hoặc gửi cho client thoải mái. `token` và `license` là bí mật — **không nhúng
  license vào bản app phát hành**.
* Mọi dữ liệu được mã hoá đầu-cuối.

### Khắc phục sự cố

* Chạy `peerova doctor` (công cụ dòng lệnh trong SDK) để chẩn đoán mạng.
* `Failed to load dynamic library 'libpeerova.so'`: kiểm tra vị trí file, hoặc đặt `PEEROVA_LIB` / `peerovaLibraryPath`.
* `ConnectFailedEvent` với `E_TIMEOUT`: server chưa chạy, sai ticket/mã, hoặc tường lửa; tăng `timeout_ms`, chạy `peerova doctor` ở cả hai đầu.
* Client thứ 4 bị từ chối (`E_CCU_LIMIT`): server đang ở chế độ dev (tối đa 3 client) — thêm `license`.
* `PeerovaException` `E_TOO_LARGE` khi gửi unreliable: gói > 1180 byte — chia nhỏ hoặc dùng reliable.
* `E_IO`: thư mục chứa `key_file` không tồn tại hoặc không ghi được.
* Server không khởi động khi bật `hide_ip`: gói hiện tại không có ẩn IP (cần Pro/Studio/Enterprise hoặc bản dùng thử).
* Thêm: [hướng dẫn §15](../DEVELOPER-GUIDE.md#15-khắc-phục-sự-cố).

### FAQ

* **Người chơi có phải gõ mã không?** Không. Nhúng ticket hoặc tải từ URL của bạn; mã 6 ký tự chỉ để mời bạn bè vào phòng do người chơi mở.
* **Flutter mobile chạy `peerova client` được không?** Không — Android/iOS không chạy được process con. Dùng SDK Dart.
* **Có cần mở port / IP tĩnh không?** Không; mở port chỉ giúp kết nối trực tiếp nhanh hơn.
* Thêm: [hướng dẫn §16](../DEVELOPER-GUIDE.md#16-faq).

### Kiểm thử

```sh
cd bindings/dart && dart pub get && dart run test/peerova_test.dart
```

---

## English

`dart:ffi` binding (`lib/peerova.dart`) for the Peerova library: peer-to-peer connections, end-to-end
encryption, reliable and unreliable delivery on 256 channels. Runs in the Dart VM (CLI tools, servers) and in Flutter
(Android, iOS, macOS, Windows, Linux). Connections are automatic: direct first, with the **automatic encrypted route**
(still end-to-end encrypted) when networks block direct connections.

### Choosing an integration

* **New game/app, or host-in-game** (scenarios 3/4 of the guide): use this Dart SDK — every Flutter platform, Android/iOS included.
* **Existing game with TCP/UDP socket code, players must not run anything** (scenario 2): the desktop app starts `peerova client`
  itself — see [Existing game: run peerova hidden](#existing-game-run-peerova-hidden) (desktop only; mobile can't spawn processes → use the SDK).
* Full comparison: [guide §1](../DEVELOPER-GUIDE.md#1-choosing-an-integration).

**Players never type anything:** embed the `pvt_...` ticket in the app, or fetch the latest ticket from your own URL at startup
(falling back to the embedded one). 6-character codes are only for inviting friends to player-hosted rooms. Details:
[guide §2](../DEVELOPER-GUIDE.md#2-players-never-type-anything).

```dart
var ticket = embeddedTicket;                       // ticket embedded in the build
try {
  final c = HttpClient()..connectionTimeout = const Duration(seconds: 5);
  final r = await (await c.getUrl(Uri.parse('https://mygame.example.com/peerova-ticket.txt'))).close();
  if (r.statusCode == 200) ticket = (await r.transform(utf8.decoder).join()).trim();
} catch (_) {}
```

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
print('ticket: ${srv.ticket}');           // pvt_... — embed it in the game / give it to clients
// 6-character connect code (only when started with 'app' + 'share', see Configuration); may be null at first:
print('code: ${srv.roomCode}');
```

Client:

```dart
// "pvt_..." ticket or 6-char code. Ticket: returns immediately; code: looked up on the website first (blocks briefly)
// key_file: keeps the player's identity across runs (the folder must exist)
final cli = PeerovaClient.connect(ticketOrCode, {'key_file': '$appData/MyGame/peerova-client.key'});
switch (cli.pollWait(const Duration(seconds: 20))) {
  case ConnectedEvent(:final peer):
    cli.sendText(peer, 'hello');                   // reliable, channel 0
  case ConnectFailedEvent(:final reason):
    print('could not connect: $reason');           // "E_TIMEOUT: ..."
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

Server and client `key_file`s must be in a writable folder — in a real app use the app-data folder
(e.g. `getApplicationSupportDirectory()` from the `path_provider` package), not the working directory.

### Existing game: run peerova hidden

For desktop apps/games (Dart CLI, Flutter Windows/Linux/macOS) that already have TCP/UDP socket code and don't want to move to
the SDK: ship `peerova.exe` (Windows) / `peerova` (Linux, macOS) with the app; the app starts `peerova client`, waits for the
local port to open, then the existing socket code connects to `127.0.0.1:8888`. The server runs `peerova server --expose tcp:8888`
([guide §3](../DEVELOPER-GUIDE.md#3-existing-game-no-code-change)). Details and the log lines:
[guide §4](../DEVELOPER-GUIDE.md#4-the-game-runs-peerova-hidden-players-run-nothing).

```dart
// peerova_tunnel.dart — desktop only (Dart CLI, Flutter Windows/Linux/macOS).
// Runs the bundled `peerova` CLI and waits until the local port is open.
import 'dart:async';
import 'dart:convert';
import 'dart:io';

class PeerovaTunnel {
  PeerovaTunnel._(this.process);
  final Process process;

  /// Completes when `peerova` exits (connection lost, killed, ...).
  Future<int> get exitCode => process.exitCode;

  /// Returns the tunnel once the forwarded port is open on 127.0.0.1, or null on failure/timeout.
  static Future<PeerovaTunnel?> start(String exe, String ticket, String forward, String keyFile,
      {Duration timeout = const Duration(seconds: 30), void Function(String)? onLog}) async {
    await File(keyFile).parent.create(recursive: true);          // the key folder must exist
    final p = await Process.start(
        exe, ['client', ticket, '--forward', forward, '--key', keyFile]);
    p.stdin.close();
    final ready = Completer<bool>();
    void onLine(String line) {
      (onLog ?? print)(line);
      // "✔ Tcp 127.0.0.1:8888 → server port 8888" = connected and the local port is open
      if (line.contains('server port') && !ready.isCompleted) ready.complete(true);
    }
    for (final s in [p.stdout, p.stderr]) {                       // drain both pipes, UTF-8, line by line
      s.transform(const Utf8Decoder(allowMalformed: true))
          .transform(const LineSplitter())
          .listen(onLine);
    }
    p.exitCode.then((_) { if (!ready.isCompleted) ready.complete(false); });   // exited first = failure
    final ok = await ready.future.timeout(timeout, onTimeout: () => false);
    if (!ok) { p.kill(); return null; }
    return PeerovaTunnel._(p);
  }

  /// Stops `peerova`. Call it when the game exits.
  void stop() => process.kill();
}
```

```dart
final dir = File(Platform.resolvedExecutable).parent.path;
final exe = '$dir${Platform.pathSeparator}${Platform.isWindows ? 'peerova.exe' : 'peerova'}';
final appData = Platform.environment['APPDATA'] ?? '${Platform.environment['HOME']}/.local/share';
final key = '$appData/MyGame/peerova-client.key';

final tunnel = await PeerovaTunnel.start(exe, ticket, 'tcp:8888', key);
if (tunnel != null) {
  await connectToGameServer('127.0.0.1', 8888);   // your existing socket code, unchanged
  tunnel.exitCode.then((_) => showError('Disconnected'));
} else {
  showError('Could not reach the server');
}
// on game exit: tunnel?.stop();
```

* Tested on Linux (Dart VM): with a valid ticket it becomes ready and HTTP through `127.0.0.1` works; with a bad ticket the
  process exits with a `lỗi: ...` line on stderr and the function returns `null`.
* **Windows:** Dart's `Process.start` has no option to hide a console window. A Flutter Windows app is a GUI app; whether starting
  `peerova.exe` (a console program) from it shows a console window **has not been verified by us** — try it with a release build
  on a Windows machine. If a window appears, use the Dart SDK above instead (no child process) — the most robust option.
  From a Dart CLI running in a console, `peerova.exe` shares that console.
* **App exit:** Dart does not kill child processes when the app exits. Call `tunnel.stop()` from your shutdown path (Flutter:
  `AppLifecycleListener(onExitRequested: ...)`; CLI: `ProcessSignal.sigint.watch()`).
* Linux/macOS: the `peerova` file must be executable (`chmod +x`); macOS: codesign it with the app.
* With several `--forward` options, wait for that many `server port` lines. To avoid port clashes: `tcp:38888=8888`.
* **Android / iOS can't spawn child processes** → use the Dart SDK.
* `peerova stdio` does **not** fit this pattern: it opens no local port; it exchanges SDK messages as JSON lines
  (see [`../stdio/README.md`](../stdio/README.md)). In Dart, just use the FFI SDK above.

### Disconnect and cleanup

* `node.disconnect(peer)` closes one connection (a `DisconnectedEvent` follows).
* `node.close()` drops all connections and frees the node; calling it twice is fine. A `NativeFinalizer` frees
  forgotten nodes, but only when the GC runs — call `close()` yourself.
* **Never** `close()` while another isolate is inside `poll`/`pollWait`/`send` on that node: stop the loop
  (`timer.cancel()`) first, then `close()`.

### Error handling

* `PeerovaException(message)`: a native failure (bad config, port in use, malformed ticket, unknown peer,
  unknown or closed connect code, unreliable payload > 1180 bytes, ...). `message` comes from `peerova_last_error`
  and starts with a stable error-code name, e.g. `"E_TOO_LARGE: ..."`.
* `ConnectFailedEvent.reason` and `DisconnectedEvent.reason` also start with the code name (`"E_TIMEOUT: ..."`, `"E_ACCESS_DENIED: ..."`).
  Branch on the prefix; don't parse the rest:

```dart
try {
  cli = PeerovaClient.connect(code);
} on PeerovaException catch (e) {
  if (e.message.startsWith('E_CODE_NOT_FOUND')) showError('That room does not exist or has closed');
}
// ...
if (ev case ConnectFailedEvent(:final reason) when reason.startsWith('E_CCU_LIMIT')) showError('Server is full');
```

  Code list (`E_INVALID_ARG`, `E_TOO_LARGE`, `E_UNKNOWN_PEER`, `E_CLOSED`, `E_CODE_NOT_FOUND`, `E_NETWORK`, `E_TIMEOUT`,
  `E_LICENSE`, `E_CCU_LIMIT`, `E_ACCESS_DENIED`, `E_INVALID_TICKET`, `E_IO`, `E_INTERNAL`): [guide — Error codes](../DEVELOPER-GUIDE.md#error-codes).
* `StateError`: using a closed node. `RangeError`: channel outside 0..255. `ArgumentError`: empty ticket, or the
  native library could not be loaded (`Failed to load dynamic library`).
* Client network failures arrive as `ConnectFailedEvent`, not as exceptions.

### Configuration

Server — `PeerovaServer.start({...})`:

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
| `share` | — | `{"name": "...", "public": true, "max": 16, "mode": "...", "region": "VN"}` — list the room and get a 6-char code; `public` = shown in the public room list |

Client — `PeerovaClient.connect(ticketOrCode, {...})`:

| Key | Default | Meaning |
|---|---|---|
| `token` | — | must match the server |
| `timeout_ms` | `20000` | max time before `ConnectFailedEvent` |
| `key_file` | — (new random key per run) | the player's identity file; created if missing (**the folder must exist**). Store it in the user's app-data folder, e.g. `"<appdata>/<game>/peerova-client.key"`. Keeping it lets the server see the player at the same stable address; banning by address works per player only while they keep that file |

### Game servers

Dedicated servers usually run the `peerova server` command-line tool (several `--expose`, separate processes for Remote Desktop and
the game, a `127.a.b.c` address per player + `--banned`, `--hide-ip`, VPN mode): see [guide §7](../DEVELOPER-GUIDE.md#7-game-server-features).

### Security

* `server.key` is the server's identity: anyone who has it can impersonate the server. Keep it secret, don't commit it.
* The ticket is not a secret: embed it in the app or share it with clients. `token` and `license` are secrets — **never embed
  the license in a shipped app**.
* All traffic is end-to-end encrypted.

### Troubleshooting

* Run `peerova doctor` (the SDK's command-line tool) for a network diagnosis.
* `Failed to load dynamic library 'libpeerova.so'`: check the file location, or set `PEEROVA_LIB` / `peerovaLibraryPath`.
* `ConnectFailedEvent` with `E_TIMEOUT`: server not running, wrong ticket/code, or a firewall; raise `timeout_ms`,
  run `peerova doctor` on both ends.
* A 4th client is refused (`E_CCU_LIMIT`): the server runs in dev mode (max 3 clients) — add a `license`.
* `PeerovaException` `E_TOO_LARGE` on an unreliable send: payload > 1180 bytes — split it or send reliably.
* `E_IO`: the `key_file` folder doesn't exist or isn't writable.
* The server won't start with `hide_ip`: your plan doesn't include hiding the IP (needs Pro/Studio/Enterprise or the trial).
* More: [guide §15](../DEVELOPER-GUIDE.md#15-troubleshooting).

### FAQ

* **Do players need to type a code?** No. Embed the ticket or fetch it from your URL; 6-character codes are only for inviting friends to player-hosted rooms.
* **Can Flutter mobile run `peerova client`?** No — Android/iOS can't spawn child processes. Use the Dart SDK.
* **Do I need port forwarding / a static IP?** No; an open port only makes direct connections faster.
* More: [guide §16](../DEVELOPER-GUIDE.md#16-faq-1).

### Tests

```sh
cd bindings/dart && dart pub get && dart run test/peerova_test.dart
```
