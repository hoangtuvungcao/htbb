# peerova bindings

Tất cả binding đều gọi cùng một C ABI: [`include/peerova.h`](c/peerova.h) (bản copy nằm trong `c/`).
Mô hình: **server** = `peerova_server_start(json)` → có *ticket* (và *connect code* 6 ký tự nếu cấu hình `app` + `share`);
**client** = `peerova_client_connect(ticket hoặc mã 6 ký tự, json)`; cả hai `poll` sự kiện (`CONNECTED=1`, `DISCONNECTED=2`, `MESSAGE=3`, `CONNECT_FAILED=4`)
và `send(peer, channel 0..255, bytes, reliable)`. Không có license key thì server chạy chế độ dev (tối đa 3 client).
Kết nối là tự động: trực tiếp trước, tự dùng đường dự phòng mã hoá khi mạng chặn kết nối trực tiếp.

**Bắt đầu từ đây: [DEVELOPER-GUIDE.md](DEVELOPER-GUIDE.md)** — khái niệm, tham chiếu đầy đủ C API, vòng đời, mẫu cho game/app, hiệu năng, bảo mật, FAQ (Tiếng Việt + English).
Mỗi thư mục binding có `README.md` riêng (cài đặt, đặt thư viện theo OS, quick start, xử lý sự kiện, lỗi, troubleshooting) và thư mục `examples/` chạy được.

## Chọn cách tích hợp

Có 4 cách, cùng một engine bên dưới:

| Cách | Nghĩa là | Khi nào chọn |
|---|---|---|
| **Native FFI** | nạp `libpeerova.so` / `peerova.dll` / `libpeerova.dylib` / `libpeerova.a` và gọi C ABI trong process | mặc định cho game, mobile, desktop: độ trễ thấp nhất, không có process phụ |
| **JNI** | Java/Kotlin gọi `libpeerova_jni.so` → `libpeerova.so` | JVM, Android |
| **Sidecar (stdio)** | spawn `peerova stdio server\|client`, trao đổi JSON lines qua stdin/stdout ([PROTOCOL.md](stdio/PROTOCOL.md)) | ngôn ngữ không có FFI hoặc không muốn native module (Node/Electron không cần node-gyp, PHP không có ext/ffi, Lua 5.x thuần, shell, Perl...). Thêm vài ms + JSON/base64 mỗi gói; server sidecar không đọc được connect code (client vẫn kết nối bằng mã được) |
| **Rust trực tiếp** | dùng crate `peerova` → `peerova::Sdk` | app Rust: không cần C ABI, không `unsafe` |

Gợi ý nhanh: **Unity** → [`unity/`](unity/README.md); **Unreal** → [`unreal/`](unreal/README.md); **Godot** → [`godot/`](godot/README.md); **Flutter** → [`dart/`](dart/README.md);
**Android** native → [`kotlin-android/`](kotlin-android/README.md) (Java thuần: [`java/`](java/README.md)); **iOS/macOS** → [`swift/`](swift/README.md); **Electron/Node** → [`nodejs/`](nodejs/README.md);
**Deno/Bun** cần hiệu năng → [`deno/`](deno/README.md) / [`bun/`](bun/README.md) (FFI), còn không thì `nodejs/` cũng chạy trên Bun;
**PHP** → [`php/`](php/README.md) (ext/ffi hoặc sidecar); **Lua / LuaJIT / LÖVE / OpenResty** → [`lua/`](lua/README.md); **Python** → [`python/`](python/README.md);
**Go** → [`go/`](go/README.md); **C# / .NET** → [`csharp/`](csharp/README.md); **C / C++** → [`c/`](c/README.md); **Ruby** → [`ruby/`](ruby/README.md); ngôn ngữ khác → sidecar ([`stdio/`](stdio/README.md)).

## Bảng nền tảng

Trạng thái: **TESTED** = test round-trip (server + client, ≥50 tin reliable đúng thứ tự + unreliable + trả lời theo chiều ngược lại + disconnect) đã chạy PASS;
**compile-checked** = biên dịch được (và chạy với stub engine) nhưng chưa chạy trong runtime/engine thật; **UNTESTED** = chưa có toolchain để chạy, code viết cẩn thận nhưng chưa từng được biên dịch/chạy.
Mặc định nền tảng test là Linux x86_64.

| Ngôn ngữ / engine | Thư mục | Cơ chế | Lệnh test (từ `bindings/`) | Trạng thái |
|---|---|---|---|---|
| C / C++ | `c/` | header trực tiếp | `c/test_peerova.c` (dòng build ở đầu file); `c/examples/` | TESTED (gcc 13; cả Android 16 x86_64 emulator, server trên máy host ↔ client trong emulator) |
| Python 3.8+ | `python/` | native FFI (ctypes) | `python3 python/test_peerova.py` | TESTED |
| C# (.NET Standard 2.1 / .NET 6+) | `csharp/` | native FFI (P/Invoke, `SafeHandle`) | `cd csharp/Test && dotnet run -c Release` | TESTED (.NET 8) |
| Unity 2021.3+ | `unity/com.peerova.sdk/` | native FFI (C# binding + `PeerovaBehaviour`) | `Tests~/StubCheck`: `dotnet run -c Release --framework net8.0` (stub UnityEngine) | compile-checked + round-trip chạy với stub; chưa chạy trong Unity |
| Godot 4.2+ (.NET) | `godot/addons/peerova/` | native FFI (addon C#, `PeerovaNode` + signals) | `cd godot/compile-check && dotnet run -c Release` | compile-checked + round-trip chạy với stub; chưa chạy trong Godot; ví dụ GDScript UNTESTED |
| Java 8+ | `java/` | JNI | `java/build.sh` | TESTED (JDK 21 trên Linux; Android 16 x86_64 emulator qua ART `app_process`) |
| **Kotlin (JVM / Android)** | `kotlin-android/` | JNI (wrapper Kotlin trên `java/`, Flow tuỳ chọn) | `kotlin-android/test.sh` | TESTED trên JVM (kotlinc 2.x, JDK 21) và Android 16 x86_64 emulator (ART, in-process); Gradle/AGP + APK + thiết bị thật UNTESTED |
| Go 1.20+ | `go/` | native FFI (cgo) | `cd go && go test -v ./...` | TESTED |
| **Node.js ≥18 / Electron main** | `nodejs/` | sidecar (ESM + `.d.ts`, EventEmitter) | `node nodejs/test/test.mjs`; types: `tsc -p nodejs/test/tsconfig.json` | TESTED (Node 20.20; `.d.ts` type-check với tsc 5.9 + @types/node ≥22) — Electron chưa chạy |
| Bun (qua package Node) | `nodejs/` | sidecar | `bun nodejs/test/test.mjs` | UNTESTED |
| **Deno ≥1.38** | `deno/` | native FFI (`Deno.dlopen`, `pollAsync` nonblocking) | `cd deno && deno test --allow-ffi --allow-env --allow-read --allow-write test.ts` | UNTESTED (chỉ type-check bằng tsc với stub `Deno`) |
| **Bun ≥1.0** | `bun/` | native FFI (`bun:ffi`) | `cd bun && bun test` | UNTESTED (chỉ type-check bằng tsc với stub `bun:ffi`) |
| **Dart 3** | `dart/` | native FFI (`dart:ffi` + `package:ffi`) | `cd dart && dart pub get && dart run test/peerova_test.dart` | TESTED (Dart VM 3.10) |
| **Flutter** | `dart/` | native FFI | integration test (`flutter test integration_test -d linux`), xem `dart/README.md` | TESTED trên Flutter 3.38 Linux desktop (lib đóng gói trong bundle, `poll()` từ `Timer.periodic`); Flutter Android/iOS/macOS/Windows UNTESTED |
| **Ruby 3.0+** | `ruby/` | native FFI (Fiddle, stdlib) | `cd ruby && ruby -Ilib test/test_peerova.rb` | UNTESTED |
| **PHP 8.1+** (ext/ffi) | `php/src/Peerova.php` | native FFI | `cd php && php -d ffi.enable=1 tests/test.php` | TESTED (PHP 8.3) |
| **PHP 8.1+** (không ext) | `php/src/PeerovaStdio.php` | sidecar (`proc_open`) | `cd php && php tests/test.php` | TESTED (PHP 8.2, 8.3) |
| **LuaJIT 2.1** (LÖVE, OpenResty, Defold-native...) | `lua/peerova.lua` | native FFI (`ffi.cdef`) | `lua/test/run_luajit.sh` | TESTED (libluajit 2.1 qua host C nhỏ) |
| **Lua 5.1–5.4 thuần** | `lua/peerova_stdio.lua` | sidecar (popen + FIFO, POSIX) | `cd lua && lua test/test_peerova_stdio.lua` | TESTED (Lua 5.4) |
| **Swift 5.7+ (iOS/macOS/Linux)** | `swift/` | native FFI (C module `CPeerova` + wrapper) | `cd swift && swift test` | UNTESTED |
| **Unreal Engine 5** | `unreal/Peerova/` | native FFI (plugin, `UPeerovaSubsystem`) | core C++ không-UE: `unreal/examples/core_demo.cpp` (lệnh build trong `unreal/README.md`) | core `PeerovaCore.h` TESTED với g++ 13 / clang++ 18; phần UE (Build.cs, subsystem, APL) cần Unreal Editor — UNTESTED |
| **Rust** | `rust/` | crate `peerova` trực tiếp | `cd rust && cargo test --release` | TESTED |
| **Bất kỳ (shell, Perl...)** | `stdio/` | sidecar ([PROTOCOL.md](stdio/PROTOCOL.md)) | `stdio/test_stdio.sh` (`PV_NO_JQ=1` để ép đường bash thuần) | TESTED (bash 5, có/không jq; ví dụ Perl 5) |

## Thư viện native theo nền tảng

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

## Ghi chú về luồng (threading)

- Mỗi node có mutex nội bộ: `send`/`poll` gọi được từ bất kỳ luồng nào; nhiều node độc lập chạy song song được.
- `peerova_server_start` **chặn tới ~3 s** (chờ ticket); `peerova_client_connect` với connect code chặn trong lúc tra mã. Trong game/app đừng gọi trên main thread: `PeerovaBehaviour.Host()`, `PeerovaNode.Host()`, `UPeerovaSubsystem::Host()` đã chạy server trên thread nền rồi phát sự kiện ở main thread.
- `poll_wait` giữ khoá node trong lúc chờ: nếu gửi từ luồng khác, dùng timeout ngắn (10–100 ms) hoặc `poll` không chặn.
- `event.data` chỉ hợp lệ tới lần `poll` kế tiếp trên cùng node — mọi binding ở đây đều **copy ngay**, nên giữ lại thoải mái.
- Không gọi `free`/`Dispose`/`close`/`Close` trong khi luồng khác đang `poll`/`send` cùng node (Go binding có RWMutex chặn việc này; C#: `SafeHandle` giữ ref-count; Java: đồng bộ phía ứng dụng).
- `peerova_last_error` là theo từng thread — các binding đọc nó ngay sau lời gọi lỗi trên cùng thread.
- Sidecar (Node, PHP stdio, Lua stdio, shell): mỗi node là một process; mọi I/O là bất đồng bộ qua pipe.
- Unreliable: tối đa 1180 byte/gói, có thể mất/đảo thứ tự. Reliable: có thứ tự theo từng channel, channel khác nhau không chặn nhau.

Chẩn đoán mạng: `peerova doctor` (công cụ dòng lệnh trong gói SDK).
