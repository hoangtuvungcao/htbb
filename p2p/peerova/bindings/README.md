# peerova bindings

Tất cả binding đều gọi cùng một C ABI: [`crates/peerova-ffi/include/peerova.h`](../crates/peerova-ffi/include/peerova.h).
Mô hình: **server** = `peerova_server_start(json)` → có *ticket*; **client** = `peerova_client_connect(ticket, json)`;
cả hai `poll` sự kiện (`CONNECTED=1`, `DISCONNECTED=2`, `MESSAGE=3`, `CONNECT_FAILED=4`) và `send(peer, channel 0..255, bytes, reliable)`.
Không có license key thì server chạy chế độ dev (tối đa 3 client).

## Chọn cách tích hợp

Có 4 cách, cùng một engine Rust bên dưới:

| Cách | Nghĩa là | Khi nào chọn |
|---|---|---|
| **Native FFI** | nạp `libpeerova.so` / `peerova.dll` / `libpeerova.dylib` / `libpeerova.a` và gọi C ABI trong process | mặc định cho game, mobile, desktop: độ trễ thấp nhất, không có process phụ |
| **JNI** | Java/Kotlin gọi `libpeerova_jni.so` → `libpeerova.so` | JVM, Android |
| **Sidecar (stdio)** | spawn `peerova stdio server\|client`, trao đổi JSON lines qua stdin/stdout ([PROTOCOL.md](stdio/PROTOCOL.md)) | ngôn ngữ không có FFI hoặc không muốn native module (Node/Electron không cần node-gyp, PHP không có ext/ffi, Lua 5.x thuần, shell, Erlang, Perl...). Thêm vài ms + JSON/base64 mỗi gói |
| **Rust trực tiếp** | dùng crate `peerova` (package `peerova-ffi`) → `peerova::Sdk` | app Rust: không cần C ABI, không `unsafe` |

Gợi ý nhanh: **Unity** → `unity/`; **Unreal** → `unreal/`; **Godot** → `godot/`; **Flutter** → `dart/`;
**Android** native → `kotlin-android/` (Java thuần: `java/`); **iOS/macOS** → `swift/`; **Electron/Node** → `nodejs/`;
**Deno/Bun** cần hiệu năng → `deno/` / `bun/` (FFI), còn không thì `nodejs/` cũng chạy trên Bun;
**PHP** có ext/ffi → `php/src/Peerova.php`, shared hosting không có ext/ffi → `php/src/PeerovaStdio.php`;
**LuaJIT / LÖVE / OpenResty** → `lua/peerova.lua`, Lua 5.x thuần → `lua/peerova_stdio.lua`; ngôn ngữ khác → sidecar (`stdio/`).

## Bảng nền tảng

Trạng thái: **TESTED** = test round-trip (≥50 tin reliable đúng thứ tự + 1 unreliable + reply) đã chạy PASS trên máy build này (Linux x86_64);
**compile-checked** = biên dịch được nhưng chưa chạy trong runtime/engine thật; **UNTESTED** = toolchain không có trên máy này, code viết cẩn thận nhưng chưa từng được biên dịch/chạy.

| Ngôn ngữ / engine | Thư mục | Cơ chế | Lệnh test (từ `bindings/`) | Trạng thái |
|---|---|---|---|---|
| C / C++ | `c/` | header trực tiếp | `example.c` | chạy được |
| Python 3.8+ | `python/` | native FFI (ctypes) | `python3 python/test_peerova.py` | TESTED |
| C# (.NET Standard 2.1 / .NET 6+) | `csharp/` | native FFI (P/Invoke, `SafeHandle`) | `cd csharp/Test && dotnet run -c Release` | TESTED |
| Unity 2021.3+ | `unity/com.peerova.sdk/` | native FFI (C# binding + `PeerovaBehaviour`) | `Tests~/StubCheck` (stub UnityEngine) | compile-checked + chạy với stub; chưa chạy trong Unity |
| Godot 4.2+ (.NET) | `godot/addons/peerova/` | native FFI (addon C#, `PeerovaNode` + signals) | `cd godot/compile-check && dotnet run -c Release` | compile-checked + chạy với stub; chưa chạy trong Godot |
| Java 8+ | `java/` | JNI | `java/build.sh` | TESTED (JDK 21, JDK 8) |
| Go 1.20+ | `go/` | native FFI (cgo) | `cd go && go test -v ./...` | TESTED (kể cả `-race`) |
| **Node.js ≥18 / Electron main** | `nodejs/` | sidecar (ESM + `.d.ts`, EventEmitter) | `node nodejs/test/test.mjs`; types: `tsc -p nodejs/test/tsconfig.json` | TESTED (Node 20.20; `.d.ts` type-check với tsc 5.9) — Electron chưa chạy |
| Bun (qua package Node) | `nodejs/` | sidecar | `bun nodejs/test/test.mjs` | UNTESTED (không có bun) |
| **Deno ≥1.38** | `deno/` | native FFI (`Deno.dlopen`, `pollAsync` nonblocking) | `cd deno && deno test --allow-ffi --allow-env --allow-read --allow-write test.ts` | UNTESTED (không có deno) |
| **Bun ≥1.0** | `bun/` | native FFI (`bun:ffi`) | `cd bun && bun test` | UNTESTED (không có bun) |
| **Dart 3 / Flutter** | `dart/` | native FFI (`dart:ffi` + `package:ffi`) | `cd dart && dart pub get && dart run test/peerova_test.dart` | TESTED trên Dart VM 3.10.8; Flutter (Android/iOS/desktop) UNTESTED |
| **Ruby 3.0+** | `ruby/` | native FFI (Fiddle, stdlib) | `cd ruby && ruby -Ilib test/test_peerova.rb` | UNTESTED (không có ruby) |
| **PHP 8.1+** (ext/ffi) | `php/src/Peerova.php` | native FFI | `cd php && php -d ffi.enable=1 tests/test.php` | TESTED (`/usr/bin/php8.3`) |
| **PHP 8.1+** (không ext) | `php/src/PeerovaStdio.php` | sidecar (`proc_open`) | `cd php && php tests/test.php` | TESTED (PHP 8.2 XAMPP, 8.3) |
| **LuaJIT 2.1** (LÖVE, OpenResty, Defold-native...) | `lua/peerova.lua` | native FFI (`ffi.cdef`) | `lua/test/run_luajit.sh` | TESTED (libluajit 2.1 qua host C nhỏ — máy không có binary `luajit`) |
| **Lua 5.1–5.4 thuần** | `lua/peerova_stdio.lua` | sidecar (popen + FIFO, POSIX) | `cd lua && lua test/test_peerova_stdio.lua` | TESTED (Lua 5.4.6; cũng chạy dưới LuaJIT) |
| **Swift 5.7+ (iOS/macOS/Linux)** | `swift/` | native FFI (C module `CPeerova` + wrapper) | `cd swift && swift test` | UNTESTED (không có Swift) |
| **Kotlin (JVM / Android)** | `kotlin-android/` | JNI (wrapper Kotlin trên `java/`, Flow tuỳ chọn) | `kotlin-android/test.sh` | TESTED trên JVM (kotlinc 2.x, JDK 21); `peerova_jni.c` compile-checked bằng NDK r28 cho arm64-v8a/armeabi-v7a/x86_64; Gradle/AGP + thiết bị UNTESTED |
| **Unreal Engine 5** | `unreal/Peerova/` | native FFI (plugin, `UPeerovaSubsystem`) | `unreal/compile-check/run.sh` (core C++ không-UE) | core `PeerovaCore.h` TESTED với g++ 13 / clang++ 18; phần UE (Build.cs, subsystem, APL) UNTESTED — Unreal không cài trên máy này |
| **Rust** | `rust/` | crate `peerova` trực tiếp | `cd rust && cargo test --release -- --nocapture` ; `cargo run --release` | TESTED |
| **Bất kỳ (shell...)** | `stdio/` | sidecar ([PROTOCOL.md](stdio/PROTOCOL.md)) | `stdio/test_stdio.sh` (`PV_NO_JQ=1` để ép đường bash thuần) | TESTED (bash 5, có/không jq) |

Header C được chép vào `swift/` và `unreal/` (hai nơi đó cần header nằm trong package/plugin): chạy `./sync-headers.sh` sau mỗi lần sửa
`peerova.h`, `./sync-headers.sh --check` để kiểm tra.

Native lib (từ thư mục gốc repo):

```sh
cargo build --release -p peerova-ffi                                   # Linux  -> target/release/libpeerova.so (+ libpeerova.a)
cargo build --release -p peerova-cli                                   # sidecar -> target/release/peerova (cho nodejs/, php stdio, lua stdio, stdio/)
cargo build --release -p peerova-ffi --target x86_64-pc-windows-gnu    # Windows -> target/x86_64-pc-windows-gnu/release/peerova.dll
```

`bindings/sync-csharp.sh` chép `csharp/Peerova.cs` sang Unity và Godot (hai nơi đó không tham chiếu được file ngoài thư mục của mình) — chạy sau mỗi lần sửa binding C#.

## Cài đặt & ví dụ tối thiểu

### C#
Thêm `csharp/Peerova.cs` vào project (hoặc build `csharp/Peerova.csproj` → `Peerova.dll`, targets `netstandard2.1;net6.0`), đặt `peerova.dll`/`libpeerova.so`/`libpeerova.dylib` cạnh file thực thi
(trên .NET 6+ có thể đặt biến môi trường `PEEROVA_LIB=/đường/dẫn/libpeerova.so` hoặc gán `Peerova.Native.LibraryPath` trước lần gọi đầu).
```csharp
using Peerova;
using var srv = Server.Start(new ServerConfig { Port = 0, Upnp = false });
using var cli = Client.Connect(srv.Ticket);
while (true) {
    if (cli.Poll(out Event ev, 100) && ev.Type == EventType.Connected) { cli.Send(ev.Peer, "hello"u8, channel: 0, reliable: true); }
    if (srv.Poll(out Event e) && e.Type == EventType.Message) Console.WriteLine(e.Text);
}
```
(`"hello"u8` cần C# 11; với Unity dùng `Encoding.UTF8.GetBytes("hello")`.)

### Unity
Copy `unity/com.peerova.sdk` vào `Packages/` (hoặc *Package Manager → Add package from disk*), đặt native lib vào `Plugins/` theo `Plugins/README.md`.
```csharp
var net = gameObject.AddComponent<Peerova.Unity.PeerovaBehaviour>();
net.OnHostStarted.AddListener(ticket => Debug.Log(ticket));
net.OnMessage.AddListener((peer, ch, data, reliable) => Debug.Log(System.Text.Encoding.UTF8.GetString(data)));
net.Host();   // hoặc net.Join(ticket)
```
Lưu ý: `Peerova.Event`/`Peerova.EventType` trùng tên `UnityEngine.Event`/`UnityEngine.EventType` → viết đầy đủ `Peerova.Event`.

### Godot 4 (.NET)
Copy `godot/addons/peerova` vào project, native lib vào `addons/peerova/bin/<linux|windows|macos>/`, Build, bật plugin, thêm node **PeerovaNode**.
GDScript (project vẫn phải là bản .NET):
```gdscript
@onready var net = $PeerovaNode
func _ready():
    net.connect("Message", func(peer, ch, data, reliable): print(data.get_string_from_utf8()))
    net.connect("Connected", func(peer): net.SendText(peer, "hi", 0, true))
    net.Join(ticket)
```
Chi tiết và các lựa chọn cho GDScript thuần (GDExtension — chưa làm): [`godot/README.md`](godot/README.md).

### Java / Android
```sh
cd java && ./build.sh       # -> build/libpeerova_jni.so, build/libpeerova.so, build/peerova.jar, chạy test
```
```java
try (Peerova.Server srv = Peerova.Server.start("{\"port\":0}");
     Peerova.Client cli = Peerova.Client.connect(srv.ticket())) {
    Peerova.Event ev = cli.poll(10000);                       // CONNECTED
    cli.send(ev.peer, "hello", 0, true);
    Peerova.Event m = srv.poll(5000);                         // CONNECTED, rồi MESSAGE
}
```
Chạy: `java -Djava.library.path=build -cp build/peerova.jar ...` (hoặc `-Dpeerova.jni.path=/abs/libpeerova_jni.so -Dpeerova.lib.path=/abs/libpeerova.so`).
Android: build `peerova_jni.c` bằng NDK (CMake/ndk-build) cho từng ABI, link với `libpeerova.so` cùng ABI, đặt cả hai vào `src/main/jniLibs/<abi>/`; thêm `peerova.jar` (hoặc `Peerova.java`). Cần quyền `android.permission.INTERNET`.

### Go
```sh
cd go && go test -v ./...
```
```go
srv, _ := peerova.StartServer(`{"port":0}`)
defer srv.Close()
cli, _ := peerova.Connect(srv.Ticket(), "")
ev, _ := cli.PollWait(10 * time.Second)        // Connected
cli.Send(ev.Peer, []byte("hello"), 0, true)
```
Mặc định link `../../target/release/libpeerova` (có rpath). Ngoài repo: `CGO_CFLAGS=-I<include> CGO_LDFLAGS="-L<lib> -lpeerova" go build`.
Muốn binary tĩnh: link `libpeerova.a` qua `CGO_LDFLAGS` (có thể cần thêm `-lm -ldl -lpthread` trên Linux; chưa thử).

### Python
```python
import peerova
srv = peerova.Server(port=0, upnp=False)
cli = peerova.Client(srv.ticket)
```

### Node.js / Electron / Bun (sidecar) — `nodejs/`
Không có native module: package spawn binary `peerova` (tìm theo `options.binary` → `$PEEROVA_BIN` → `nodejs/bin/` → `../../target/release/` → `PATH`).
Cài: `npm install /đường/dẫn/bindings/nodejs` (hoặc copy thư mục), copy `target/release/peerova` (Windows: `peerova.exe`) vào `nodejs/bin/` khi đóng gói.
```js
import { Peerova } from "peerova";
const srv = await Peerova.server({ port: 47000 });                 // srv.ticket
srv.on("message", (peer, data, { channel, reliable }) => console.log(data.toString()));
const cli = await Peerova.client(srv.ticket);
cli.on("connected", (peer) => cli.send(peer, "hello", { channel: 0, reliable: true }));
```
Electron: chỉ main process / `utilityProcess`; binary phải nằm ngoài `app.asar` (`asarUnpack` hoặc `extraResources` + `{ binary: path.join(process.resourcesPath, "peerova") }`). Chi tiết: [`nodejs/README.md`](nodejs/README.md).

### Deno — `deno/` (FFI)
```ts
import { PeerovaServer, PeerovaClient, EventType } from "./bindings/deno/mod.ts";
using srv = await PeerovaServer.startAsync({ port: 47000 });      // không chặn event loop
using cli = PeerovaClient.connect(srv.ticket);
const ev = await cli.pollAsync(10_000);                            // poll_wait chạy trên thread native
if (ev?.type === EventType.Connected) cli.send(ev.peer, "hello");
```
Chạy với `--allow-ffi --allow-env --allow-read`; lib: `$PEEROVA_LIB` → `../../target/release/` → loader hệ thống.

### Bun — `bun/` (FFI)
```ts
import { PeerovaServer, PeerovaClient, EventType } from "./bindings/bun/peerova.ts";
const srv = PeerovaServer.start({ port: 47000 });                  // chặn ≤ ~3 s
const cli = PeerovaClient.connect(srv.ticket);
setInterval(() => { for (let e; (e = srv.poll()); ) if (e.type === EventType.Message) console.log(e.text); }, 16);
```
`bun:ffi` là đồng bộ: dùng `poll()` (không chờ) trong timer, hoặc `poll(timeout)` trong Worker.

### Dart / Flutter — `dart/`
```sh
cd dart && dart pub get && dart run test/peerova_test.dart
```
```dart
import 'package:peerova/peerova.dart';
final srv = PeerovaServer.start({'port': 0, 'upnp': false});
final cli = PeerovaClient.connect(srv.ticket);
final ev = cli.pollWait(const Duration(seconds: 10));
if (ev is ConnectedEvent) cli.sendText(ev.peer, 'hello', channel: 0);
for (final e in srv.events()) { if (e is MessageEvent) print(e.text); }
cli.close(); srv.close();
```
Flutter: trên UI isolate gọi `poll()` (không chờ) trong `Timer.periodic`, hoặc `pollWait` trong Isolate nền rồi chuyển event qua `SendPort`.
Đặt lib: Android `android/app/src/main/jniLibs/<abi>/libpeerova.so`; iOS `libpeerova.a`/xcframework (link `-force_load`, nạp bằng `DynamicLibrary.process()`);
Windows/Linux: thư mục lib của bundle; macOS: `Frameworks/`. Chi tiết: [`dart/README.md`](dart/README.md).

### Ruby — `ruby/` (Fiddle, stdlib — không cần compiler)
`gem build peerova.gemspec && gem install peerova-0.1.0.gem` (hoặc `ruby -I bindings/ruby/lib`); đặt lib cạnh `lib/peerova.rb` hoặc `PEEROVA_LIB`.
```ruby
require "peerova"
srv = Peerova::Server.new(port: 0, upnp: false)
cli = Peerova::Client.new(srv.ticket)
ev  = cli.poll(10)                                   # ev.type == :connected
cli.send_message(ev.peer, "hello", channel: 0, reliable: true)
srv.each_event(timeout: 2) { |e| puts e.text if e.message? }
cli.close; srv.close
```
(`send_message` chứ không phải `send` để không đè `Object#send`. Cần Ruby ≥ 3.0.)

### PHP — `php/` (ext/ffi hoặc sidecar)
`composer require peerova/peerova` (path repository `bindings/php`) hoặc `require` 3 file trong `src/`.
FFI: cần `extension=ffi` + `ffi.enable=true` (mặc định `preload` chỉ cho preload — CLI chạy `php -d ffi.enable=1`), lib qua `PEEROVA_LIB`.
Sidecar: chỉ cần `proc_open` + binary `peerova` (`PEEROVA_BIN`) — dùng được trên hosting không bật ext/ffi.
```php
$srv = Peerova\Peerova::server(['port' => 0, 'upnp' => false]);   // hoặc Peerova\PeerovaStdio::server(...)
$cli = Peerova\Peerova::client($srv->ticket());
$ev  = $cli->poll(10);                                              // EventType::CONNECTED
$cli->send($ev->peer, "hello", 0, true);
foreach ($srv->events(2) as $e) if ($e->isMessage()) echo $e->data, "\n";
$cli->close(); $srv->close();
```
Lưu ý: PHP-FPM sống theo request — node P2P nên chạy trong worker CLI dài hạn (Swoole/RoadRunner/`php worker.php`), không phải trong một request web.

### Lua — `lua/`
LuaJIT / LÖVE / OpenResty: `peerova.lua` (FFI) — đặt vào `package.path`, lib cạnh nó hoặc `PEEROVA_LIB`.
Lua 5.1–5.4 thuần: `peerova_stdio.lua` (sidecar; chỉ POSIX vì dùng `mkfifo`; `poll()` chặn tới event kế tiếp; cần binary `peerova` hoặc `PEEROVA_BIN`).
```lua
local peerova = require "peerova"            -- hoặc require "peerova_stdio"
local srv = peerova.server{ port = 0, upnp = false }
local cli = peerova.client(srv.ticket)
local ev = cli:poll(10)                       -- peerova_stdio: cli:poll() (chặn)
cli:send(ev.peer, "hello", { channel = 0, reliable = true })
for e in srv:events(2) do if e.type == "message" then print(e.data) break end end
cli:close(); srv:close()
```

### Swift (iOS / macOS) — `swift/`
SwiftPM: thêm package `bindings/swift`, target dùng product `Peerova`. Dev: `PEEROVA_LIB_DIR=/dir swift test` (mặc định `../../target/release`; dùng `unsafeFlags` nên chỉ hợp cho build local).
iOS: build `libpeerova.a` cho `aarch64-apple-ios` + `aarch64-apple-ios-sim`, gộp bằng `xcodebuild -create-xcframework`, thay `CPeerova` bằng `.binaryTarget(name: "CPeerova", path: "CPeerova.xcframework")` (có thể cần `Security.framework` / `libresolv`). Chi tiết: [`swift/README.md`](swift/README.md).
```swift
import Peerova
let server = try await PeerovaServer.start(config: ["port": 47000])
let client = try PeerovaClient(ticket: server.ticket)
for await ev in client.events() {
    if case .connected(let peer) = ev { try client.send("hello", to: peer, channel: 0, reliable: true) }
    if case .message(_, _, _, let data) = ev { print(String(decoding: data, as: UTF8.self)) }
}
```

### Kotlin / Android — `kotlin-android/`
```sh
kotlin-android/test.sh                 # test JVM
kotlin-android/build-android-libs.sh   # cargo-ndk -> peerova/src/main/jniLibs/<abi>/libpeerova.so
```
Gradle: `settings.gradle.kts` → `include(":peerova"); project(":peerova").projectDir = file("<repo>/bindings/kotlin-android/peerova")`; app → `implementation(project(":peerova"))`.
Module build `libpeerova_jni.so` bằng CMake (NDK) và đóng gói cùng `libpeerova.so` prebuilt: APK có `lib/{arm64-v8a,armeabi-v7a,x86_64}/{libpeerova.so,libpeerova_jni.so}`. Quyền `INTERNET` đã có trong manifest của module.
```kotlin
import dev.peerova.kt.*
PeerovaServer.start(mapOf("port" to 0)).use { srv ->            // chặn ≤ ~3 s: gọi ngoài main thread (hoặc startAsync() với coroutines)
    srv.startPolling(ContextCompat.getMainExecutor(ctx)) { ev ->
        if (ev is PeerovaEvent.Message) Log.d("p2p", ev.text)
    }
    PeerovaClient.connect(srv.ticket).use { cli ->
        val ev = cli.poll(10_000)
        if (ev is PeerovaEvent.Connected) cli.send(ev.peer, "hello")
    }
}
// coroutines: srv.eventFlow().collect { ... }
```
Chi tiết (jniLibs, R8 rules, 16 KB page size): [`kotlin-android/README.md`](kotlin-android/README.md).

### Unreal Engine 5 — `unreal/Peerova/`
1. Copy `unreal/Peerova` vào `<Project>/Plugins/`.
2. Đặt lib vào `Source/ThirdParty/PeerovaLibrary/lib/<Platform>/` (Win64 dùng bản MSVC: `cargo build --release -p peerova-ffi --target x86_64-pc-windows-msvc` → `peerova.dll` + `peerova.dll.lib`; Linux `.so`; Mac `.dylib`; Android `<abi>/libpeerova.so` (đóng gói qua `Peerova_APL.xml`); iOS `libpeerova.a`).
3. Bật plugin, regenerate project files.
```cpp
UPeerovaSubsystem* Net = GetGameInstance()->GetSubsystem<UPeerovaSubsystem>();
Net->OnHostStarted.AddDynamic(this, &AMyActor::HandleHostStarted);   // void(const FString& Ticket)
Net->OnMessage.AddDynamic(this, &AMyActor::HandleMessage);           // void(int32 Peer, const TArray<uint8>& Data, int32 Channel, bool bReliable)
Net->Host(47000);            // client: Net->Join(Ticket); OnConnected -> Net->SendString(Peer, TEXT("hi"), 0, true);
```
Blueprint: Get Game Instance → Get Subsystem (Peerova Subsystem) → Host / Join, bind các event `On*`. `Host` chạy server trên thread nền, `Tick` poll mỗi frame.
Peer id là `uint32` nhưng Blueprint chỉ có `int32` → subsystem bit-cast (có thể âm); coi nó là id mờ, truyền nguyên lại cho `Send`/`Disconnect`.
Chi tiết: [`unreal/README.md`](unreal/README.md).

### Rust — `rust/`
Crate ví dụ riêng (có `[workspace]` riêng nên không ảnh hưởng workspace gốc):
```toml
[dependencies]
peerova = { package = "peerova-ffi", path = "<repo>/crates/peerova-ffi" }
```
```rust
use peerova::{Sdk, EV_CONNECTED, EV_MESSAGE};
let mut srv = Sdk::server(Some(r#"{"port":47000}"#))?;
let mut cli = Sdk::client(srv.ticket().unwrap(), None)?;
let ev = cli.poll_wait(std::time::Duration::from_secs(10)).unwrap();   // EV_CONNECTED
cli.send(ev.peer, 0, b"hello", true)?;
```
`cargo run --release` (demo), `cargo run --release -- server` / `-- client <TICKET>`.

### Bất kỳ ngôn ngữ nào — `stdio/` (sidecar)
```sh
peerova stdio server '{"port":47000}'          # -> {"event":"ready","ticket":"pvt_..."}
peerova stdio client pvt_...                   # gõ {"cmd":"send","peer":N,"channel":0,"reliable":true,"data":"aGVsbG8"}
```
Giao thức đầy đủ: [`stdio/PROTOCOL.md`](stdio/PROTOCOL.md). Client tham khảo bằng bash: `stdio/peerova.sh` (thư viện), `stdio/chat.sh` (chat P2P: `./chat.sh server` / `./chat.sh client <TICKET>`).

## Ghi chú về luồng (threading)

- Mỗi node có mutex nội bộ: `send`/`poll` gọi được từ bất kỳ luồng nào; nhiều node độc lập chạy song song được.
- `peerova_server_start` **chặn tới ~3 s** (chờ ticket). Trong game đừng gọi trên main thread: `PeerovaBehaviour.Host()` và `PeerovaNode.Host()` đã chạy nó trên thread pool rồi phát sự kiện ở main thread.
- `event.data` chỉ hợp lệ tới lần `poll` kế tiếp trên cùng node — mọi binding ở đây đều **copy ngay** (C# `byte[]`, Java `byte[]`, Go `[]byte`, Python `bytes`), nên giữ lại thoải mái.
- Engine: poll trong vòng lặp frame (`Update()` / `_Process()`), không cần thread riêng. Server dedicated: một thread `poll_wait(timeout)` là đủ.
- Không gọi `free`/`Dispose`/`close`/`Close` trong khi luồng khác đang `poll`/`send` cùng node (Go binding có RWMutex chặn việc này; C#: `SafeHandle` giữ ref-count nên Dispose an toàn với lời gọi đang chạy; Java: đồng bộ phía ứng dụng).
- `peerova_last_error` là theo từng thread — các binding đọc nó ngay sau lời gọi lỗi trên cùng thread.
- Sidecar (Node, PHP stdio, Lua stdio, shell): mỗi node là một process; mọi I/O là bất đồng bộ qua pipe, không có vấn đề luồng. Deno: `pollAsync` chạy `poll_wait` trên thread native — chỉ một lời gọi đang chờ mỗi node, không `close()` khi đang chờ.
- Unreliable: tối đa 1180 byte/gói (hằng `MaxUnreliablePayload`), có thể mất/đảo thứ tự. Reliable: có thứ tự theo từng channel, channel khác nhau không chặn nhau.

## Đóng gói native lib theo nền tảng

Tên file phải là `peerova` (C# `DllImport("peerova")`, Java `loadLibrary("peerova")`):

| Nền tảng | File | Build | Đặt ở đâu |
|---|---|---|---|
| Windows x64 | `peerova.dll` | `cargo build --release -p peerova-ffi --target x86_64-pc-windows-gnu` (hoặc `-msvc`) | cạnh `.exe`; Unity `Plugins/x86_64/`; Java: thêm `peerova_jni.dll` (build bằng mingw: `x86_64-w64-mingw32-gcc -shared ... -lpeerova`) |
| Linux x64 | `libpeerova.so` | `cargo build --release -p peerova-ffi` | cạnh binary + rpath `$ORIGIN`, hoặc `LD_LIBRARY_PATH`; Unity `Plugins/x86_64/` |
| macOS | `libpeerova.dylib` | `--target aarch64-apple-darwin` và `x86_64-apple-darwin`, gộp bằng `lipo -create -output libpeerova.dylib ...` | `Contents/Frameworks/` (rpath `@executable_path/../Frameworks`) hoặc cạnh binary; phải codesign khi notarize |
| Android | `libpeerova.so` theo ABI | `cargo ndk -t arm64-v8a -t armeabi-v7a -t x86_64 -o jniLibs build --release -p peerova-ffi` | `jniLibs/arm64-v8a/`, `jniLibs/armeabi-v7a/`, `jniLibs/x86_64/`; Unity `Plugins/Android/<abi>/` (IL2CPP) |
| iOS | `libpeerova.a` (static) | `cargo build --release -p peerova-ffi --target aarch64-apple-ios` (simulator: `aarch64-apple-ios-sim`) | link tĩnh vào app; Unity `Plugins/iOS/` — C# tự dùng `DllImport("__Internal")`; Xcode có thể cần thêm `Security.framework`/`libresolv` |

`libpeerova.so` hiện không có SONAME: nếu link bằng đường dẫn đầy đủ, đường dẫn tuyệt đối của máy build sẽ bị ghi vào binary — luôn link bằng `-L<dir> -lpeerova` (CMake: `IMPORTED_NO_SONAME`), hoặc build với `RUSTFLAGS="-C link-arg=-Wl,-soname,libpeerova.so"` (chưa thử).

Sidecar `peerova` (CLI) cho Windows: `cargo build --release -p peerova-cli --target x86_64-pc-windows-gnu` → `peerova.exe` (chưa test trong repo này).

Mobile targets (Android/iOS/macOS) chưa được build hay test trong repo này — các lệnh trên là hướng dẫn chuẩn của Rust/cargo-ndk.
