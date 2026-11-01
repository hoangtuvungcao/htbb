# peerova bindings

- [Tiếng Việt](#tiếng-việt)
- [English](#english)

Website, license, tải về / downloads: https://peerova.pages.dev

---

## Tiếng Việt

**Bắt đầu từ đây: [DEVELOPER-GUIDE.md](DEVELOPER-GUIDE.md)** — chọn cách tích hợp, người chơi không phải gõ gì, game có sẵn chạy `peerova` ẩn,
tính năng máy chủ game, tham chiếu đầy đủ C API, mã lỗi, vòng đời, hiệu năng, bảo mật, FAQ (Tiếng Việt + English).
Mỗi thư mục binding có `README.md` riêng (cài đặt, đặt thư viện theo OS, quick start, xử lý sự kiện, lỗi, troubleshooting) và ví dụ chạy được.

### Chọn cách tích hợp

| # | Tình huống | Dùng gì | Người chơi phải làm gì |
|---|---|---|---|
| 1 | Game/app **có sẵn**, không muốn sửa code | Máy chủ: `peerova server --expose tcp:8888`. Người chơi: `peerova client <TICKET> --forward tcp:8888`, rồi game kết nối `127.0.0.1:8888` | Chạy một lệnh (hoặc file `.bat`/shortcut bạn đưa) |
| 2 | Game **có sẵn**, người chơi **không được phải chạy gì** | Game/launcher tự chạy `peerova` ẩn, chờ dòng sẵn sàng, rồi code socket cũ kết nối `127.0.0.1:8888` — mã mẫu cho C#, Unity, Java, Node/Electron, Python, Go, C++ trong hướng dẫn; Rust, Unreal trong README của chúng | Không gì cả |
| 3 | Game **mới** (hoặc sẵn sàng thay lớp mạng) | SDK: message reliable + unreliable, sự kiện `CONNECTED`/`MESSAGE`/… | Không gì cả |
| 4 | **Host trong game** (một người chơi mở phòng) | SDK server chạy trong game của người host; chia sẻ ticket hoặc mã 6 ký tự | Bạn bè bấm "Vào phòng" / nhập mã |

Chi tiết từng tình huống: [DEVELOPER-GUIDE.md §1](DEVELOPER-GUIDE.md#1-chọn-cách-tích-hợp). Người chơi không bao giờ phải gõ ticket hay mã
(nhúng ticket / tải từ URL của bạn; mã 6 ký tự chỉ để mời bạn bè): [§2](DEVELOPER-GUIDE.md#2-người-chơi-không-phải-gõ-gì).
Máy chủ game (nhiều cổng, mỗi người chơi một địa chỉ + cấm, ẩn IP máy chủ, VPN): [§7](DEVELOPER-GUIDE.md#7-tính-năng-máy-chủ-game).

### Cơ chế tích hợp SDK

Tất cả binding đều gọi cùng một C ABI: [`peerova.h`](c/peerova.h) (bản copy nằm trong `c/`).
Mô hình: **server** = `peerova_server_start(json)` → có *ticket* (và *connect code* 6 ký tự nếu cấu hình `app` + `share`);
**client** = `peerova_client_connect(ticket hoặc mã 6 ký tự, json)`; cả hai `poll` sự kiện (`CONNECTED=1`, `DISCONNECTED=2`, `MESSAGE=3`, `CONNECT_FAILED=4`)
và `send(peer, channel 0..255, bytes, reliable)`. Lỗi có mã ổn định (`peerova_last_error_code()`, xem [Mã lỗi](DEVELOPER-GUIDE.md#mã-lỗi)).
Không có license key thì server chạy chế độ dev (tối đa 3 client).
Kết nối là tự động: trực tiếp trước; nếu mạng chặn kết nối trực tiếp, tự dùng **đường mã hoá tự động** (vẫn mã hoá đầu-cuối).

| Cơ chế | Nghĩa là | Khi nào chọn |
|---|---|---|
| **Native FFI** | nạp `libpeerova.so` / `peerova.dll` / `libpeerova.dylib` / `libpeerova.a` và gọi C ABI trong process | mặc định cho game, mobile, desktop: độ trễ thấp nhất, không có process phụ |
| **JNI** | Java/Kotlin gọi `libpeerova_jni.so` → `libpeerova.so` | JVM, Android |
| **Sidecar (stdio)** | spawn `peerova stdio server\|client`, trao đổi JSON lines qua stdin/stdout ([PROTOCOL.md](stdio/PROTOCOL.md)) | ngôn ngữ không có FFI hoặc không muốn native module (Node/Electron không cần node-gyp, PHP không có ext/ffi, Lua 5.x thuần, shell, Perl...). Thêm vài ms + JSON/base64 mỗi gói; server sidecar không đọc được connect code (client vẫn kết nối bằng mã được). **Không** mở cổng local — để giữ code socket cũ, dùng `peerova client --forward` (tình huống 1/2) |
| **Rust trực tiếp** | dùng crate `peerova` → `peerova::Sdk` | app Rust: không cần C ABI, không `unsafe` |

### Gợi ý nhanh theo ngôn ngữ

**Unity** → [`unity/`](unity/README.md); **Unreal** → [`unreal/`](unreal/README.md); **Godot** → [`godot/`](godot/README.md); **Flutter** → [`dart/`](dart/README.md);
**Android** native → [`kotlin-android/`](kotlin-android/README.md) (Java thuần: [`java/`](java/README.md)); **iOS/macOS** → [`swift/`](swift/README.md); **Electron/Node** → [`nodejs/`](nodejs/README.md);
**Deno/Bun** cần hiệu năng → [`deno/`](deno/README.md) / [`bun/`](bun/README.md) (FFI), còn không thì `nodejs/` cũng chạy trên Bun;
**PHP** → [`php/`](php/README.md) (ext/ffi hoặc sidecar); **Lua / LuaJIT / LÖVE / OpenResty** → [`lua/`](lua/README.md); **Python** → [`python/`](python/README.md);
**Go** → [`go/`](go/README.md); **C# / .NET** → [`csharp/`](csharp/README.md); **C / C++** → [`c/`](c/README.md); **Rust** → [`rust/`](rust/README.md); **Ruby** → [`ruby/`](ruby/README.md); ngôn ngữ khác → sidecar ([`stdio/`](stdio/README.md)).

### Bảng nền tảng

Trạng thái: **TESTED** = test round-trip (server + client, ≥50 tin reliable đúng thứ tự + unreliable + trả lời theo chiều ngược lại + disconnect) đã chạy PASS;
**compile-checked** = biên dịch được (và chạy với stub engine) nhưng chưa chạy trong runtime/engine thật; **UNTESTED** = chưa có toolchain để chạy, code viết cẩn thận nhưng chưa từng được biên dịch/chạy.
Mặc định nền tảng test là Linux x86_64.

| Ngôn ngữ / engine | Thư mục | Cơ chế | Lệnh test (từ `bindings/`) | Trạng thái |
|---|---|---|---|---|
| C / C++ | `c/` | header trực tiếp | `c/test_peerova.c` (dòng build ở đầu file); `c/examples/` | TESTED (gcc 13; cả Android 16 x86_64 emulator, server trên máy host ↔ client trong emulator) |
| Python 3.8+ | `python/` | native FFI (ctypes) | `python3 python/test_peerova.py` | TESTED |
| C# (.NET Standard 2.1 / .NET 6+) | `csharp/` | native FFI (P/Invoke, `SafeHandle`) | `cd csharp/Test && dotnet run -c Release` | TESTED (.NET 8) |
| Unity 2021.3+ | `unity/com.peerova.sdk/` | native FFI (C# binding + `PeerovaBehaviour`) | `Tests~/StubCheck`: `dotnet run -c Release --framework net8.0` (stub UnityEngine) | compile-checked + round-trip chạy với stub; chưa chạy trong Unity |
| Godot 4.2+ (.NET) | `godot/addons/peerova/` | native FFI (addon C#, `PeerovaNode` + signals) | `GODOT=<Godot .NET binary> godot/test/test.sh` (không có Godot: `cd godot/compile-check && dotnet run -c Release`) | TESTED trong Godot 4.7.2 .NET thật, `--headless` trên Linux (round-trip C# và GDScript, `net_demo` C# + GDScript 3 process); export/Windows/macOS/mobile UNTESTED |
| Java 8+ | `java/` | JNI | `java/build.sh` | TESTED (JDK 21 trên Linux; Android 16 x86_64 emulator qua ART `app_process`) |
| **Kotlin (JVM / Android)** | `kotlin-android/` | JNI (wrapper Kotlin trên `java/`, Flow tuỳ chọn) | `kotlin-android/test.sh` | TESTED trên JVM (kotlinc 2.x, JDK 21) và Android 16 x86_64 emulator (ART, in-process); Gradle/AGP + APK + thiết bị thật UNTESTED |
| Go 1.20+ | `go/` | native FFI (cgo) | `cd go && go test -v ./...` | TESTED |
| **Node.js ≥18 / Electron main** | `nodejs/` | sidecar (ESM + `.d.ts`, EventEmitter) | `node nodejs/test/test.mjs`; types: `tsc -p nodejs/test/tsconfig.json` | TESTED (Node 20.20; `.d.ts` type-check với tsc 5.9 + @types/node ≥22); Electron: TESTED (Electron 45.2.1 main process, `npx electron nodejs/examples/electron --selftest --ozone-platform=headless`, sự kiện tới renderer qua IPC) |
| Bun (qua package Node) | `nodejs/` | sidecar | `bun nodejs/test/test.mjs` | TESTED (Bun 1.4.4) |
| **Deno ≥1.38** | `deno/` | native FFI (`Deno.dlopen`, `pollAsync` nonblocking) | `cd deno && deno test --allow-ffi --allow-env --allow-read --allow-write test.ts` | TESTED (Deno 2.9.8; cả `examples/chat.ts` 1 server + 2 client) |
| **Bun ≥1.0** | `bun/` | native FFI (`bun:ffi`) | `cd bun && bun test` | TESTED (Bun 1.4.4; cả `examples/chat.ts` 1 server + 2 client) |
| **Dart 3** | `dart/` | native FFI (`dart:ffi` + `package:ffi`) | `cd dart && dart pub get && dart run test/peerova_test.dart` | TESTED (Dart VM 3.10) |
| **Flutter** | `dart/` | native FFI | integration test (`flutter test integration_test -d linux`), xem `dart/README.md` | TESTED trên Flutter 3.38 Linux desktop (lib đóng gói trong bundle, `poll()` từ `Timer.periodic`); Flutter Android/iOS/macOS/Windows UNTESTED |
| **Ruby 3.0+** | `ruby/` | native FFI (Fiddle, stdlib) | `cd ruby && ruby -Ilib test/test_peerova.rb` | TESTED (Ruby 4.0.9, Fiddle 1.1.8; cả `examples/chat.rb` 1 server + 2 client); Ruby 3.x chưa chạy |
| **PHP 8.1+** (ext/ffi) | `php/src/Peerova.php` | native FFI | `cd php && php -d ffi.enable=1 tests/test.php` | TESTED (PHP 8.3) |
| **PHP 8.1+** (không ext) | `php/src/PeerovaStdio.php` | sidecar (`proc_open`) | `cd php && php tests/test.php` | TESTED (PHP 8.2, 8.3) |
| **LuaJIT 2.1** (LÖVE, OpenResty, Defold-native...) | `lua/peerova.lua` | native FFI (`ffi.cdef`) | `lua/test/run_luajit.sh` | TESTED (libluajit 2.1 qua host C nhỏ) |
| **Lua 5.1–5.4 thuần** | `lua/peerova_stdio.lua` | sidecar (popen + FIFO, POSIX) | `cd lua && lua test/test_peerova_stdio.lua` | TESTED (Lua 5.4) |
| **Swift 5.7+ (iOS/macOS/Linux)** | `swift/` | native FFI (C module `CPeerova` + wrapper) | `cd swift && swift test` | TESTED trên Linux (Swift 6.4.2; cả `examples/PeerovaDemo` 1 server + 2 client); iOS/macOS UNTESTED |
| **Unreal Engine 5** | `unreal/Peerova/` | native FFI (plugin, `UPeerovaSubsystem`) | core C++ không-UE: `unreal/examples/core_demo.cpp` (lệnh build trong `unreal/README.md`) | core `PeerovaCore.h` TESTED với g++ 13 / clang++ 18; phần UE (Build.cs, subsystem, APL) cần Unreal Editor — UNTESTED |
| **Rust** | `rust/` | crate `peerova` trực tiếp | bộ test Rust `rust/tests/roundtrip.rs` (chạy ở chế độ release, xem `rust/README.md`) | TESTED |
| **Bất kỳ (shell, Perl...)** | `stdio/` | sidecar ([PROTOCOL.md](stdio/PROTOCOL.md)) | `stdio/test_stdio.sh` (`PV_NO_JQ=1` để ép đường bash thuần) | TESTED (bash 5, có/không jq; ví dụ Perl 5) |

### Thư viện native theo nền tảng

Trong gói SDK: `include/peerova.h`, `lib/linux-x86_64/` (`libpeerova.so`, `libpeerova.a`, sidecar `peerova`), `lib/windows-x86_64/` (`peerova.dll`, sidecar `peerova.exe`),
`lib/windows-legacy-x86_64/peerova.dll` (Windows cũ). Tên file phải là `peerova` (C# `DllImport("peerova")`, Java `loadLibrary("peerova")`).

| Nền tảng | File | Đặt ở đâu |
|---|---|---|
| Windows x64 | `peerova.dll` | cạnh `.exe`; Unity `Plugins/x86_64/`; Java: thêm `peerova_jni.dll`. SDK **không** cần `wintun.dll` (chỉ chế độ VPN của công cụ dòng lệnh `peerova.exe` cần) |
| Linux x64 | `libpeerova.so` (SONAME `libpeerova.so`) hoặc `libpeerova.a` | cạnh binary + rpath `$ORIGIN`, hoặc `LD_LIBRARY_PATH`, hoặc `PEEROVA_LIB` (binding hỗ trợ); Unity `Plugins/x86_64/`; Flutter: `bundle/lib/` |
| macOS | `libpeerova.dylib` | `Contents/Frameworks/` (rpath `@executable_path/../Frameworks`) hoặc cạnh binary; phải codesign khi notarize |
| Android | `libpeerova.so` theo ABI | `jniLibs/arm64-v8a/`, `jniLibs/armeabi-v7a/`, `jniLibs/x86_64/`; Unity `Plugins/Android/<abi>/`; cần quyền `INTERNET` |
| iOS | `libpeerova.a` (static) | link tĩnh vào app; Unity `Plugins/iOS/` — C# tự dùng `DllImport("__Internal")`; Xcode có thể cần thêm `Security.framework`/`libresolv` |

Thư viện cho Android / iOS / macOS: liên hệ Peerova (https://peerova.pages.dev).
Tình huống 2 (chạy `peerova` ẩn) cần phát hành thêm công cụ dòng lệnh `peerova.exe` / `peerova` cạnh game; mobile/console không chạy được process con → dùng SDK.

### Ghi chú về luồng (threading)

- Mỗi node có mutex nội bộ: `send`/`poll` gọi được từ bất kỳ luồng nào; nhiều node độc lập chạy song song được.
- `peerova_server_start` **chặn tới ~3 s** (chờ ticket); `peerova_client_connect` với connect code chặn trong lúc tra mã. Trong game/app đừng gọi trên main thread: `PeerovaBehaviour.Host()`, `PeerovaNode.Host()`, `UPeerovaSubsystem::Host()` đã chạy server trên thread nền rồi phát sự kiện ở main thread.
- `poll_wait` giữ khoá node trong lúc chờ: nếu gửi từ luồng khác, dùng timeout ngắn (10–100 ms) hoặc `poll` không chặn.
- `event.data` chỉ hợp lệ tới lần `poll` kế tiếp trên cùng node — mọi binding ở đây đều **copy ngay**, nên giữ lại thoải mái.
- Không gọi `free`/`Dispose`/`close`/`Close` trong khi luồng khác đang `poll`/`send` cùng node (Go binding có RWMutex chặn việc này; C#: `SafeHandle` giữ ref-count; Java: đồng bộ phía ứng dụng).
- `peerova_last_error` / `peerova_last_error_code` là theo từng thread — các binding đọc chúng ngay sau lời gọi lỗi trên cùng thread.
- Sidecar (Node, PHP stdio, Lua stdio, shell): mỗi node là một process; mọi I/O là bất đồng bộ qua pipe.
- Unreliable: tối đa 1180 byte/gói, có thể mất/đảo thứ tự. Reliable: có thứ tự theo từng channel, channel khác nhau không chặn nhau.

Chẩn đoán mạng: `peerova doctor` (công cụ dòng lệnh trong gói SDK). Khắc phục sự cố: [§15](DEVELOPER-GUIDE.md#15-khắc-phục-sự-cố) · FAQ: [§16](DEVELOPER-GUIDE.md#16-faq).

---

## English

**Start here: [DEVELOPER-GUIDE.md](DEVELOPER-GUIDE.md)** — choosing an integration, players never type anything, an existing game running `peerova`
hidden, game-server features, the full C API reference, error codes, lifecycle, performance, security, FAQ (Vietnamese + English).
Every binding folder has its own `README.md` (install, per-OS library placement, quick start, events, errors, troubleshooting) and runnable examples.

### Choosing an integration

| # | Situation | Use | What players do |
|---|---|---|---|
| 1 | **Existing** game/app, no code change | Server: `peerova server --expose tcp:8888`. Players: `peerova client <TICKET> --forward tcp:8888`, then the game connects to `127.0.0.1:8888` | Run one command (or a `.bat`/shortcut you ship) |
| 2 | **Existing** game, players **must not run anything** | The game/launcher starts `peerova` hidden, waits for the ready line, then the existing socket code connects to `127.0.0.1:8888` — sample code for C#, Unity, Java, Node/Electron, Python, Go, C++ in the guide; Rust, Unreal in their READMEs | Nothing |
| 3 | **New** game (or ready to replace the networking layer) | The SDK: reliable + unreliable messages, `CONNECTED`/`MESSAGE`/… events | Nothing |
| 4 | **Host-in-game** (one player hosts) | An SDK server runs inside the host's game; share the ticket or a 6-character code | Friends click "Join" / type the code |

Details for each scenario: [DEVELOPER-GUIDE.md §1](DEVELOPER-GUIDE.md#1-choosing-an-integration). Players never type a ticket or a code
(embed the ticket / fetch it from your own URL; 6-character codes are only for inviting friends): [§2](DEVELOPER-GUIDE.md#2-players-never-type-anything).
Game servers (several ports, one address per player + banning, hiding the server IP, VPN): [§7](DEVELOPER-GUIDE.md#7-game-server-features).

### SDK integration mechanisms

Every binding calls the same C ABI: [`peerova.h`](c/peerova.h) (a copy lives in `c/`).
Model: **server** = `peerova_server_start(json)` → a *ticket* (and a 6-character *connect code* when `app` + `share` are configured);
**client** = `peerova_client_connect(ticket or 6-character code, json)`; both `poll` events (`CONNECTED=1`, `DISCONNECTED=2`, `MESSAGE=3`, `CONNECT_FAILED=4`)
and `send(peer, channel 0..255, bytes, reliable)`. Errors carry stable codes (`peerova_last_error_code()`, see [Error codes](DEVELOPER-GUIDE.md#error-codes)).
Without a license key the server runs in dev mode (max 3 clients).
Connections are automatic: direct first; when the network blocks direct connections, the **automatic encrypted route** is used (still end-to-end encrypted).

| Mechanism | Meaning | When to choose it |
|---|---|---|
| **Native FFI** | load `libpeerova.so` / `peerova.dll` / `libpeerova.dylib` / `libpeerova.a` and call the C ABI in-process | the default for games, mobile, desktop: lowest latency, no extra process |
| **JNI** | Java/Kotlin call `libpeerova_jni.so` → `libpeerova.so` | JVM, Android |
| **Sidecar (stdio)** | spawn `peerova stdio server\|client` and exchange JSON lines over stdin/stdout ([PROTOCOL.md](stdio/PROTOCOL.md)) | languages without FFI or when you don't want a native module (Node/Electron without node-gyp, PHP without ext/ffi, plain Lua 5.x, shell, Perl...). Adds a few ms + JSON/base64 per packet; a sidecar server can't read its connect code (clients can still connect with codes). It does **not** open local ports — to keep existing socket code, use `peerova client --forward` (scenarios 1/2) |
| **Rust, direct** | use the `peerova` crate → `peerova::Sdk` | Rust apps: no C ABI, no `unsafe` |

### Quick picks by language

**Unity** → [`unity/`](unity/README.md); **Unreal** → [`unreal/`](unreal/README.md); **Godot** → [`godot/`](godot/README.md); **Flutter** → [`dart/`](dart/README.md);
**Android** native → [`kotlin-android/`](kotlin-android/README.md) (plain Java: [`java/`](java/README.md)); **iOS/macOS** → [`swift/`](swift/README.md); **Electron/Node** → [`nodejs/`](nodejs/README.md);
**Deno/Bun** when performance matters → [`deno/`](deno/README.md) / [`bun/`](bun/README.md) (FFI), otherwise `nodejs/` also runs on Bun;
**PHP** → [`php/`](php/README.md) (ext/ffi or sidecar); **Lua / LuaJIT / LÖVE / OpenResty** → [`lua/`](lua/README.md); **Python** → [`python/`](python/README.md);
**Go** → [`go/`](go/README.md); **C# / .NET** → [`csharp/`](csharp/README.md); **C / C++** → [`c/`](c/README.md); **Rust** → [`rust/`](rust/README.md); **Ruby** → [`ruby/`](ruby/README.md); any other language → sidecar ([`stdio/`](stdio/README.md)).

### Platform table

Status: **TESTED** = a round-trip test (server + client, ≥50 reliable messages in order + unreliable + a reply in the other direction + disconnect) ran and PASSED;
**compile-checked** = compiles (and runs against a stub engine) but has not run inside the real runtime/engine; **UNTESTED** = no toolchain was available, the code was written carefully but never compiled/run.
The default test platform is Linux x86_64.

| Language / engine | Folder | Mechanism | Test command (from `bindings/`) | Status |
|---|---|---|---|---|
| C / C++ | `c/` | header directly | `c/test_peerova.c` (build line at the top of the file); `c/examples/` | TESTED (gcc 13; also Android 16 x86_64 emulator, server on the host ↔ client in the emulator) |
| Python 3.8+ | `python/` | native FFI (ctypes) | `python3 python/test_peerova.py` | TESTED |
| C# (.NET Standard 2.1 / .NET 6+) | `csharp/` | native FFI (P/Invoke, `SafeHandle`) | `cd csharp/Test && dotnet run -c Release` | TESTED (.NET 8) |
| Unity 2021.3+ | `unity/com.peerova.sdk/` | native FFI (C# binding + `PeerovaBehaviour`) | `Tests~/StubCheck`: `dotnet run -c Release --framework net8.0` (stub UnityEngine) | compile-checked + round-trip run against the stub; not yet run inside Unity |
| Godot 4.2+ (.NET) | `godot/addons/peerova/` | native FFI (C# addon, `PeerovaNode` + signals) | `GODOT=<Godot .NET binary> godot/test/test.sh` (without Godot: `cd godot/compile-check && dotnet run -c Release`) | TESTED in real Godot 4.7.2 .NET, `--headless` on Linux (C# and GDScript round-trip, `net_demo` C# + GDScript with 3 processes); export/Windows/macOS/mobile UNTESTED |
| Java 8+ | `java/` | JNI | `java/build.sh` | TESTED (JDK 21 on Linux; Android 16 x86_64 emulator via ART `app_process`) |
| **Kotlin (JVM / Android)** | `kotlin-android/` | JNI (Kotlin wrapper over `java/`, optional Flow) | `kotlin-android/test.sh` | TESTED on the JVM (kotlinc 2.x, JDK 21) and the Android 16 x86_64 emulator (ART, in-process); Gradle/AGP + APK + real devices UNTESTED |
| Go 1.20+ | `go/` | native FFI (cgo) | `cd go && go test -v ./...` | TESTED |
| **Node.js ≥18 / Electron main** | `nodejs/` | sidecar (ESM + `.d.ts`, EventEmitter) | `node nodejs/test/test.mjs`; types: `tsc -p nodejs/test/tsconfig.json` | TESTED (Node 20.20; `.d.ts` type-checked with tsc 5.9 + @types/node ≥22); Electron: TESTED (Electron 45.2.1 main process, `npx electron nodejs/examples/electron --selftest --ozone-platform=headless`, events reach the renderer over IPC) |
| Bun (via the Node package) | `nodejs/` | sidecar | `bun nodejs/test/test.mjs` | TESTED (Bun 1.4.4) |
| **Deno ≥1.38** | `deno/` | native FFI (`Deno.dlopen`, non-blocking `pollAsync`) | `cd deno && deno test --allow-ffi --allow-env --allow-read --allow-write test.ts` | TESTED (Deno 2.9.8; also `examples/chat.ts` 1 server + 2 clients) |
| **Bun ≥1.0** | `bun/` | native FFI (`bun:ffi`) | `cd bun && bun test` | TESTED (Bun 1.4.4; also `examples/chat.ts` 1 server + 2 clients) |
| **Dart 3** | `dart/` | native FFI (`dart:ffi` + `package:ffi`) | `cd dart && dart pub get && dart run test/peerova_test.dart` | TESTED (Dart VM 3.10) |
| **Flutter** | `dart/` | native FFI | integration test (`flutter test integration_test -d linux`), see `dart/README.md` | TESTED on Flutter 3.38 Linux desktop (library bundled, `poll()` from `Timer.periodic`); Flutter Android/iOS/macOS/Windows UNTESTED |
| **Ruby 3.0+** | `ruby/` | native FFI (Fiddle, stdlib) | `cd ruby && ruby -Ilib test/test_peerova.rb` | TESTED (Ruby 4.0.9, Fiddle 1.1.8; also `examples/chat.rb` 1 server + 2 clients); Ruby 3.x not yet run |
| **PHP 8.1+** (ext/ffi) | `php/src/Peerova.php` | native FFI | `cd php && php -d ffi.enable=1 tests/test.php` | TESTED (PHP 8.3) |
| **PHP 8.1+** (no extension) | `php/src/PeerovaStdio.php` | sidecar (`proc_open`) | `cd php && php tests/test.php` | TESTED (PHP 8.2, 8.3) |
| **LuaJIT 2.1** (LÖVE, OpenResty, Defold-native...) | `lua/peerova.lua` | native FFI (`ffi.cdef`) | `lua/test/run_luajit.sh` | TESTED (libluajit 2.1 through a small C host) |
| **Plain Lua 5.1–5.4** | `lua/peerova_stdio.lua` | sidecar (popen + FIFO, POSIX) | `cd lua && lua test/test_peerova_stdio.lua` | TESTED (Lua 5.4) |
| **Swift 5.7+ (iOS/macOS/Linux)** | `swift/` | native FFI (C module `CPeerova` + wrapper) | `cd swift && swift test` | TESTED on Linux (Swift 6.4.2; also `examples/PeerovaDemo` 1 server + 2 clients); iOS/macOS UNTESTED |
| **Unreal Engine 5** | `unreal/Peerova/` | native FFI (plugin, `UPeerovaSubsystem`) | engine-free C++ core: `unreal/examples/core_demo.cpp` (build line in `unreal/README.md`) | core `PeerovaCore.h` TESTED with g++ 13 / clang++ 18; the UE parts (Build.cs, subsystem, APL) need the Unreal Editor — UNTESTED |
| **Rust** | `rust/` | `peerova` crate directly | Rust test suite `rust/tests/roundtrip.rs` (release mode, see `rust/README.md`) | TESTED |
| **Anything (shell, Perl...)** | `stdio/` | sidecar ([PROTOCOL.md](stdio/PROTOCOL.md)) | `stdio/test_stdio.sh` (`PV_NO_JQ=1` forces the pure-bash path) | TESTED (bash 5, with/without jq; Perl 5 example) |

### Native library per platform

In the SDK package: `include/peerova.h`, `lib/linux-x86_64/` (`libpeerova.so`, `libpeerova.a`, sidecar `peerova`), `lib/windows-x86_64/` (`peerova.dll`, sidecar `peerova.exe`),
`lib/windows-legacy-x86_64/peerova.dll` (older Windows). The file name must be `peerova` (C# `DllImport("peerova")`, Java `loadLibrary("peerova")`).

| Platform | File | Where it goes |
|---|---|---|
| Windows x64 | `peerova.dll` | next to the `.exe`; Unity `Plugins/x86_64/`; Java: add `peerova_jni.dll`. The SDK does **not** need `wintun.dll` (only the VPN mode of the `peerova.exe` command-line tool does) |
| Linux x64 | `libpeerova.so` (SONAME `libpeerova.so`) or `libpeerova.a` | next to the binary + rpath `$ORIGIN`, or `LD_LIBRARY_PATH`, or `PEEROVA_LIB` (where the binding supports it); Unity `Plugins/x86_64/`; Flutter: `bundle/lib/` |
| macOS | `libpeerova.dylib` | `Contents/Frameworks/` (rpath `@executable_path/../Frameworks`) or next to the binary; must be codesigned for notarization |
| Android | `libpeerova.so` per ABI | `jniLibs/arm64-v8a/`, `jniLibs/armeabi-v7a/`, `jniLibs/x86_64/`; Unity `Plugins/Android/<abi>/`; needs the `INTERNET` permission |
| iOS | `libpeerova.a` (static) | linked statically into the app; Unity `Plugins/iOS/` — C# switches to `DllImport("__Internal")` automatically; Xcode may need `Security.framework`/`libresolv` |

Libraries for Android / iOS / macOS: contact Peerova (https://peerova.pages.dev).
Scenario 2 (running `peerova` hidden) also ships the `peerova.exe` / `peerova` command-line tool next to the game; mobile/consoles can't spawn child processes → use the SDK.

### Threading notes

- Each node has an internal mutex: `send`/`poll` may be called from any thread; several independent nodes can run in parallel.
- `peerova_server_start` **blocks for up to ~3 s** (waiting for the ticket); `peerova_client_connect` with a connect code blocks during the lookup. Don't call them on the main thread of a game/app: `PeerovaBehaviour.Host()`, `PeerovaNode.Host()`, `UPeerovaSubsystem::Host()` already start the server on a background thread and raise events on the main thread.
- `poll_wait` holds the node lock while waiting: when sending from another thread, use a short timeout (10–100 ms) or the non-blocking `poll`.
- `event.data` is only valid until the next `poll` on the same node — every binding here **copies it immediately**, so keep it as long as you like.
- Don't call `free`/`Dispose`/`close`/`Close` while another thread is in `poll`/`send` on the same node (the Go binding blocks this with an RWMutex; C#: `SafeHandle` ref-counts; Java: synchronize in the application).
- `peerova_last_error` / `peerova_last_error_code` are per thread — the bindings read them right after the failing call on the same thread.
- Sidecars (Node, PHP stdio, Lua stdio, shell): each node is a process; all I/O is asynchronous over pipes.
- Unreliable: at most 1180 bytes per packet, may be lost/reordered. Reliable: ordered per channel; different channels don't block each other.

Network diagnosis: `peerova doctor` (the command-line tool in the SDK package). Troubleshooting: [§15](DEVELOPER-GUIDE.md#15-troubleshooting) · FAQ: [§16](DEVELOPER-GUIDE.md#16-faq-1).
