# Peerova cho Swift / Peerova for Swift (iOS · macOS · Linux)

[Tiếng Việt](#tiếng-việt) · [English](#english)

---

## Tiếng Việt

Package SwiftPM `Peerova`: kết nối P2P trực tiếp, mã hoá đầu-cuối, bọc C API của Peerova
(`Sources/CPeerova/include/peerova.h`, bản sao y hệt `include/peerova.h` của SDK).

Trạng thái: **CHƯA TEST** — mã nhắm Swift 5.7+ nhưng chưa được biên dịch (máy viết mã không có Swift). Hãy báo
cho Peerova nếu gặp lỗi biên dịch.

Khái niệm chung và C API đầy đủ: [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md). Website: https://peerova.pages.dev

### Cấu trúc

```
Package.swift
Sources/CPeerova/include/{peerova.h,module.modulemap}   module C, link -lpeerova
Sources/CPeerova/shim.c                                 (SwiftPM cần ít nhất một file C)
Sources/Peerova/Peerova.swift                           PeerovaServer / PeerovaClient / PeerovaEvent
Tests/PeerovaTests/PeerovaTests.swift                   100 message reliable đúng thứ tự + 1 unreliable + trả lời
examples/PeerovaDemo/                                   demo đồng bộ vị trí 20 Hz + chat (swift run)
```

### Yêu cầu

- Swift 5.7+ (Xcode 14+), macOS 10.15+ / iOS 13+, hoặc Linux.
- Thư viện native Peerova cho nền tảng của bạn.

### Đặt thư viện native ở đâu

| Nền tảng | File | Ghi chú |
|---|---|---|
| Linux | `libpeerova.so` (gói SDK: `lib/linux-x86_64/`) | khi build: `PEEROVA_LIB_DIR=/thư/mục` (thư mục này cũng được ghi vào rpath, nên binary tìm thấy thư viện ở đó khi chạy); khi triển khai sang máy khác: `LD_LIBRARY_PATH` hoặc thêm rpath `$ORIGIN` và đặt thư viện cạnh binary |
| macOS | `libpeerova.dylib` | liên hệ Peerova. App: đặt trong `Contents/Frameworks/` |
| iOS | `libpeerova.a` (static) hoặc `CPeerova.xcframework` | liên hệ Peerova |
| Windows | `peerova.dll` cạnh file exe (gói SDK: `lib/windows-x86_64/`) | Swift trên Windows chưa được kiểm tra. `wintun.dll` không cần cho SDK |

Khi `PEEROVA_LIB_DIR` không được đặt, `Package.swift` tự dùng thư mục thư viện của gói SDK
(`lib/linux-x86_64`) nếu có.

### Cài đặt

**Phát triển / Linux / macOS (package local):** thêm package bằng đường dẫn và dùng product `Peerova`:

```swift
// Package.swift của bạn
dependencies: [.package(path: "path/to/bindings/swift")],
targets: [.executableTarget(name: "MyApp", dependencies: [.product(name: "Peerova", package: "swift")])]
```

```sh
PEEROVA_LIB_DIR=/abs/dir/with/libpeerova swift build
```

`Package.swift` dùng `unsafeFlags` để chỉ đường tới thư viện, nên chỉ dùng được như package local (không phải
dependency từ git). Với app phát hành, dùng XCFramework như dưới đây.

**macOS app:** chép `libpeerova.dylib` vào `Contents/Frameworks/` (install name `@rpath/libpeerova.dylib`), thêm
`@executable_path/../Frameworks` vào *Runpath Search Paths*, và codesign dylib (cần cho notarization). App
Sandbox cần entitlement *Outgoing Connections (Client)* và *Incoming Connections (Server)*.

**iOS (static library qua XCFramework):** iOS không cho đóng gói dylib rời; dùng `libpeerova.a`. Nếu bạn nhận
được `CPeerova.xcframework` thì bỏ qua bước tạo. Nếu nhận `libpeerova.a` cho thiết bị và simulator:

```sh
mkdir -p hdr && cp Sources/CPeerova/include/peerova.h hdr/ \
  && printf 'module CPeerova {\n  header "peerova.h"\n  export *\n}\n' > hdr/module.modulemap
xcodebuild -create-xcframework \
  -library ios-device/libpeerova.a -headers hdr \
  -library ios-simulator/libpeerova.a -headers hdr \
  -output CPeerova.xcframework
```

Rồi trong `Package.swift` thay target C `CPeerova` bằng binary target (không cần unsafe flags, nên package dùng
được như dependency từ xa):

```swift
.binaryTarget(name: "CPeerova", path: "CPeerova.xcframework"),
// hoặc .binaryTarget(name: "CPeerova", url: "https://…/CPeerova.xcframework.zip", checksum: "…")
```

và thêm vào target `Peerova`: `linkerSettings: [.linkedFramework("Security"), .linkedLibrary("resolv")]` nếu
linker báo thiếu symbol `Sec*` / `res_*`.

### Bắt đầu nhanh (5 phút)

**Server:**

```swift
import Peerova

let server = try await PeerovaServer.start(config: ["port": 47000])   // chạy ngoài main thread (≤ ~3 giây)
print("ticket:", server.ticket)                                      // gửi ticket này cho client
// Mã kết nối 6 ký tự: start với "app": "prj_...", "share": ["name": "Phòng", "public": true, "max": 8]
// rồi đọc server.roomCode (nil cho tới khi website cấp mã, thường vài giây sau khi start).
```

**Client:**

```swift
let client = try PeerovaClient(ticket: ticket)        // ticket "pvt_..." hoặc mã kết nối 6 ký tự, ví dụ "K7Q2XD"
for await ev in client.events() {
    switch ev {
    case .connected(let peer):          try client.send("hello", to: peer, channel: 0, reliable: true)
    case .message(_, _, _, let data):   print(String(decoding: data, as: UTF8.self))
    case .disconnected, .connectFailed: client.close()
    }
}
```

Với mã kết nối, `PeerovaClient(ticket:)` tra mã trên website trước nên sẽ chặn trong thời gian của request đó —
gọi ngoài main thread.

**Demo đầy đủ** (vị trí 20 Hz qua unreliable + chat reliable):

```sh
cd examples/PeerovaDemo
swift run PeerovaDemo server            # in "ticket: pvt_..."
swift run PeerovaDemo client pvt_... 10 # chạy 10 giây; bỏ số giây để chat tới khi Ctrl+D
```

### Gửi: reliable hay unreliable

| | `reliable: true` (mặc định) | `reliable: false` |
|---|---|---|
| Đảm bảo | có thứ tự, gửi lại khi mất (theo từng kênh) | datagram: có thể mất hoặc đến sai thứ tự |
| Kích thước | có thể lớn | tối đa **1180 byte** (`peerovaMaxUnreliablePayload`); lớn hơn → ném `PeerovaError` |
| Dùng cho | chat, inventory, RPC, từng phần file | input gameplay, vị trí, trạng thái thời gian thực kiểu voice |

`channel` là 0...255 (mặc định 0). Các kênh khác nhau không chặn nhau.

### Nhận event

`PeerovaEvent`: `.connected(peer:)`, `.message(peer:channel:reliable:data:)`, `.disconnected(peer:reason:)`,
`.connectFailed(reason:)` (chỉ client). `peer` là `UInt32`. Hai cách:

- **async/await:** `for await ev in node.events()` — một thread riêng poll và đẩy event vào `AsyncStream`.
  Stream kết thúc khi `close()` hoặc khi task bị huỷ. Mỗi node chỉ một stream; đừng trộn với `poll`.
- **Vòng lặp game (đồng bộ):** mỗi frame gọi `node.poll()` (không chặn) cho tới khi trả `nil`. `poll(timeout:)`
  chờ tối đa `timeout` giây nhưng giữ mutex của node trong lúc chờ, nên `send` từ thread khác phải đợi — giữ
  timeout ngắn.

Mọi phương thức an toàn khi gọi từ bất kỳ thread nào (node có mutex nội bộ).

### Ngắt kết nối và dọn dẹp

- `node.disconnect(peer)`: đóng một kết nối; cả hai phía nhận `.disconnected`.
- `node.close()`: ngắt tất cả, dừng stream `events()`, giải phóng node native. Idempotent; cũng được gọi khi
  object bị giải phóng. **Không** gọi `close()` khi thread khác đang ở trong `poll`/`send` của node đó.

### Xử lý lỗi

Mọi lỗi là `PeerovaError` (`message` = văn bản từ `peerova_last_error`):

- `PeerovaServer(...)`: port bận, config hoặc license sai.
- `PeerovaClient(...)`: ticket sai định dạng, mã không tồn tại, config sai.
- `send(...)`: node đã đóng, peer không tồn tại, unreliable > 1180 byte.
- Không kết nối được (hết thời gian, sai token, server đầy) không ném lỗi mà đến dưới dạng `.connectFailed`.

### Cấu hình

**Server** (`config:` dictionary hoặc `configJSON:`):

| Khoá | Ý nghĩa |
|---|---|
| `port` | cổng UDP, mặc định `47000`; `0` = cổng ngẫu nhiên |
| `key_file` | file khoá danh tính, mặc định `"server.key"` (tự tạo nếu chưa có). Cùng khoá ⇒ cùng ticket |
| `license` | khoá license `PVA-...`. Không có → chế độ dev, tối đa 3 client |
| `max_ccu` | số client đồng thời tối đa |
| `token` | bí mật chung mà client phải gửi kèm |
| `upnp` | xin router mở cổng, mặc định `true` |
| `private` | ticket không chứa địa chỉ IP (chia sẻ ticket không lộ địa chỉ server), mặc định `false` |
| `app`, `share` | mã kết nối 6 ký tự: `app` = project id trên website Peerova, `share` = `["name", "public", "max", "mode", "region"]` |

**Client:** `token` (phải khớp với server), `timeout_ms` (mặc định `20000`).

Kết nối là tự động: ưu tiên kết nối trực tiếp, và tự động dùng đường dự phòng (vẫn mã hoá) khi mạng chặn kết
nối trực tiếp. Trên di động, hệ điều hành có thể treo socket khi app vào nền — kết nối lại khi nhận
`.disconnected`.

### Bảo mật

- `server.key` là danh tính của server: ai có file này có thể giả mạo server. Giữ bí mật, không commit vào git,
  không đóng gói vào app bundle (đặt trong Application Support).
- Ticket có thể chia sẻ cho client. `token` và `license` là bí mật.
- Mọi dữ liệu được mã hoá đầu-cuối.

### Khắc phục sự cố

- Chạy `peerova doctor` (công cụ dòng lệnh) để chẩn đoán mạng.
- **Không tìm thấy thư viện** (`ld: library not found for -lpeerova` khi build, hoặc lỗi nạp `libpeerova` khi
  chạy): đặt `PEEROVA_LIB_DIR`; khi chạy trên Linux dùng `LD_LIBRARY_PATH` (hoặc rpath `$ORIGIN` + thư viện cạnh binary);
  macOS kiểm tra install name và *Runpath Search Paths*.
- **`.connectFailed` do hết thời gian**: kiểm tra ticket/mã còn đúng, server đang chạy, chạy `peerova doctor` ở
  cả hai máy.
- **Client thứ 4 bị từ chối**: server đang ở chế độ dev (tối đa 3 client) — thêm `license`.
- **`send(..., reliable: false)` ném lỗi**: payload > 1180 byte; dùng reliable hoặc chia nhỏ.

### Test

```sh
PEEROVA_LIB_DIR=/abs/dir/with/libpeerova swift test
```

---

## English

The `Peerova` SwiftPM package: direct, end-to-end encrypted P2P connections, wrapping the Peerova C API
(`Sources/CPeerova/include/peerova.h`, an identical copy of the SDK's `include/peerova.h`).

Status: **UNTESTED** — the code targets Swift 5.7+ but has not been compiled yet (no Swift toolchain on the
machine where it was written). Please report compile errors to Peerova.

Concepts and the full C API: [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md). Website: https://peerova.pages.dev

### Layout

```
Package.swift
Sources/CPeerova/include/{peerova.h,module.modulemap}   C module, links -lpeerova
Sources/CPeerova/shim.c                                 (SwiftPM needs at least one C source)
Sources/Peerova/Peerova.swift                           PeerovaServer / PeerovaClient / PeerovaEvent
Tests/PeerovaTests/PeerovaTests.swift                   100 reliable messages in order + 1 unreliable + reply
examples/PeerovaDemo/                                   20 Hz position sync + chat demo (swift run)
```

### Requirements

- Swift 5.7+ (Xcode 14+), macOS 10.15+ / iOS 13+, or Linux.
- The Peerova native library for your platform.

### Where to put the native library

| Platform | File | Notes |
|---|---|---|
| Linux | `libpeerova.so` (SDK package: `lib/linux-x86_64/`) | build time: `PEEROVA_LIB_DIR=/dir` (also written into the rpath, so the binary finds the library there at run time); when deploying to another machine: `LD_LIBRARY_PATH`, or add rpath `$ORIGIN` and put the library next to the binary |
| macOS | `libpeerova.dylib` | contact Peerova for this build. Apps: put it in `Contents/Frameworks/` |
| iOS | `libpeerova.a` (static) or `CPeerova.xcframework` | contact Peerova for this build |
| Windows | `peerova.dll` next to the exe (SDK package: `lib/windows-x86_64/`) | Swift on Windows is untested. `wintun.dll` is not needed by the SDK |

When `PEEROVA_LIB_DIR` is not set, `Package.swift` uses the SDK package's library folder (`lib/linux-x86_64`)
if it exists.

### Install

**Development / Linux / macOS (local package):** add the package by path and use the `Peerova` product:

```swift
// your Package.swift
dependencies: [.package(path: "path/to/bindings/swift")],
targets: [.executableTarget(name: "MyApp", dependencies: [.product(name: "Peerova", package: "swift")])]
```

```sh
PEEROVA_LIB_DIR=/abs/dir/with/libpeerova swift build
```

`Package.swift` uses `unsafeFlags` to point at the library, so it only works as a local package (not as a git
dependency). For shipping apps, use the XCFramework below.

**macOS app:** copy `libpeerova.dylib` into `Contents/Frameworks/` (install name `@rpath/libpeerova.dylib`), add
`@executable_path/../Frameworks` to *Runpath Search Paths*, and codesign the dylib (required for notarization).
App Sandbox needs the *Outgoing Connections (Client)* and *Incoming Connections (Server)* entitlements.

**iOS (static library via XCFramework):** iOS apps can't ship a loose dylib; use `libpeerova.a`. If you received
`CPeerova.xcframework`, skip the creation step. If you received `libpeerova.a` for device and simulator:

```sh
mkdir -p hdr && cp Sources/CPeerova/include/peerova.h hdr/ \
  && printf 'module CPeerova {\n  header "peerova.h"\n  export *\n}\n' > hdr/module.modulemap
xcodebuild -create-xcframework \
  -library ios-device/libpeerova.a -headers hdr \
  -library ios-simulator/libpeerova.a -headers hdr \
  -output CPeerova.xcframework
```

Then in `Package.swift` replace the `CPeerova` C target with a binary target (no unsafe flags, so the package
can be consumed as a remote dependency):

```swift
.binaryTarget(name: "CPeerova", path: "CPeerova.xcframework"),
// or .binaryTarget(name: "CPeerova", url: "https://…/CPeerova.xcframework.zip", checksum: "…")
```

and add to the `Peerova` target: `linkerSettings: [.linkedFramework("Security"), .linkedLibrary("resolv")]` if
the linker reports missing `Sec*` / `res_*` symbols.

### 5-minute quick start

**Server:**

```swift
import Peerova

let server = try await PeerovaServer.start(config: ["port": 47000])   // runs off the main thread (≤ ~3 s)
print("ticket:", server.ticket)                                      // give this ticket to clients
// 6-character connect code: start with "app": "prj_...", "share": ["name": "Room", "public": true, "max": 8]
// then read server.roomCode (nil until the website assigns it, usually a few seconds after start).
```

**Client:**

```swift
let client = try PeerovaClient(ticket: ticket)        // "pvt_..." ticket or a 6-character connect code like "K7Q2XD"
for await ev in client.events() {
    switch ev {
    case .connected(let peer):          try client.send("hello", to: peer, channel: 0, reliable: true)
    case .message(_, _, _, let data):   print(String(decoding: data, as: UTF8.self))
    case .disconnected, .connectFailed: client.close()
    }
}
```

With a connect code, `PeerovaClient(ticket:)` first looks the code up on the website, so it blocks for that
request — call it off the main thread.

**Full demo** (positions at 20 Hz unreliable + reliable chat):

```sh
cd examples/PeerovaDemo
swift run PeerovaDemo server            # prints "ticket: pvt_..."
swift run PeerovaDemo client pvt_... 10 # runs 10 seconds; omit the seconds to chat until Ctrl+D
```

### Sending: reliable or unreliable

| | `reliable: true` (default) | `reliable: false` |
|---|---|---|
| Guarantees | ordered, retransmitted (per channel) | datagram: may be lost or reordered |
| Size | can be large | max **1180 bytes** (`peerovaMaxUnreliablePayload`); larger → throws `PeerovaError` |
| Use for | chat, inventory, RPC, file chunks | gameplay inputs, positions, voice-like real-time state |

`channel` is 0...255 (default 0). Different channels don't block each other.

### Receiving events

`PeerovaEvent`: `.connected(peer:)`, `.message(peer:channel:reliable:data:)`, `.disconnected(peer:reason:)`,
`.connectFailed(reason:)` (client only). `peer` is a `UInt32`. Two ways:

- **async/await:** `for await ev in node.events()` — a dedicated thread polls and feeds an `AsyncStream`. The
  stream finishes on `close()` or when the task is cancelled. One stream per node; don't mix it with `poll`.
- **Game loop (synchronous):** each frame call `node.poll()` (non-blocking) until it returns `nil`.
  `poll(timeout:)` waits up to `timeout` seconds but holds the node's mutex meanwhile, so `send` from other
  threads waits — keep timeouts short.

Every method is safe to call from any thread (the node has an internal mutex).

### Disconnect and cleanup

- `node.disconnect(peer)`: closes one connection; both sides get `.disconnected`.
- `node.close()`: disconnects everyone, stops the `events()` stream, frees the native node. Idempotent; also done
  on deallocation. **Never** call `close()` while another thread is inside `poll`/`send` on that node.

### Error handling

Every error is a `PeerovaError` (`message` = text from `peerova_last_error`):

- `PeerovaServer(...)`: port in use, invalid config or license.
- `PeerovaClient(...)`: malformed ticket, unknown code, invalid config.
- `send(...)`: node closed, unknown peer, unreliable payload > 1180 bytes.
- Failing to connect (timeout, wrong token, server full) does not throw; it arrives as `.connectFailed`.

### Configuration

**Server** (`config:` dictionary or `configJSON:`):

| Key | Meaning |
|---|---|
| `port` | UDP port, default `47000`; `0` = random free port |
| `key_file` | identity key file, default `"server.key"` (created if missing). Same key ⇒ same ticket |
| `license` | `PVA-...` license key. Without it: dev mode, max 3 clients |
| `max_ccu` | maximum concurrent clients |
| `token` | shared secret clients must present |
| `upnp` | ask the router to open the port, default `true` |
| `private` | the ticket contains no IP addresses (sharing it does not reveal the server's address), default `false` |
| `app`, `share` | 6-character connect code: `app` = project id from the Peerova website, `share` = `["name", "public", "max", "mode", "region"]` |

**Client:** `token` (must match the server), `timeout_ms` (default `20000`).

Connections are automatic: direct first, with an automatic encrypted fallback route when networks block direct
connections. On mobile, the OS may suspend sockets when the app goes to the background — reconnect on
`.disconnected`.

### Security

- `server.key` is the server's identity: anyone who has it can impersonate the server. Keep it secret, never
  commit it, never ship it inside the app bundle (keep it in Application Support).
- The ticket is meant to be shared with clients. `token` and `license` are secrets.
- All traffic is end-to-end encrypted.

### Troubleshooting

- Run `peerova doctor` (command-line tool) for a network diagnosis.
- **Library not found** (`ld: library not found for -lpeerova` at build time, or a `libpeerova` load error at run
  time): set `PEEROVA_LIB_DIR`; on Linux use `LD_LIBRARY_PATH` (or rpath `$ORIGIN` + the library next to the binary); on
  macOS check the install name and *Runpath Search Paths*.
- **`.connectFailed` with a timeout**: check the ticket/code is current and the server is running; run
  `peerova doctor` on both machines.
- **The 4th client is refused**: the server is in dev mode (max 3 clients) — add a `license`.
- **`send(..., reliable: false)` throws**: payload > 1180 bytes; send it reliable or split it.

### Test

```sh
PEEROVA_LIB_DIR=/abs/dir/with/libpeerova swift test
```
