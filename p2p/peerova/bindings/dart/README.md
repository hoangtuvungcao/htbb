# peerova for Dart / Flutter (dart:ffi)

`lib/peerova.dart` binds the C ABI (`crates/peerova-ffi/include/peerova.h`) with `dart:ffi` + `package:ffi`.
Dart 3.0+ (sealed event classes, pattern matching). Tested here with Dart 3.10.8 on Linux x64.

```sh
cd bindings/dart
dart pub get                       # (--offline works if package:ffi is in ~/.pub-cache)
dart run test/peerova_test.dart    # -> "dart PASS: 100 reliable msgs in order + 1 unreliable + reply"
```

## Use

```yaml
dependencies:
  peerova:
    path: ../peerova/bindings/dart    # or a git dependency
```

```dart
import 'package:peerova/peerova.dart';

final srv = PeerovaServer.start({'port': 47000, 'key_file': 'server.key'}); // blocks <= ~3 s
print(srv.ticket);                                                          // pvt_...
final cli = PeerovaClient.connect(srv.ticket);

switch (cli.pollWait(const Duration(seconds: 10))) {
  case ConnectedEvent(:final peer):
    cli.sendText(peer, 'hello', channel: 0);                 // reliable, ordered
    cli.send(peer, [1, 2, 3], channel: 1, reliable: false);  // datagram, <= 1180 B
  case ConnectFailedEvent(:final reason):
    print('failed: $reason');
  default:
}
for (final ev in srv.events()) {          // until 1 s idle
  if (ev is MessageEvent) print('${ev.peer} ch${ev.channel}: ${ev.text}');
}
cli.close(); srv.close();                 // also freed by a NativeFinalizer if forgotten
```

Events: `ConnectedEvent`, `DisconnectedEvent(reason)`, `MessageEvent(channel, reliable, data, text)`,
`ConnectFailedEvent(reason)`. `event.data` in C is valid only until the next poll; the binding copies it
into a `Uint8List` immediately, so events are safe to keep.

Library lookup order: `peerovaLibraryPath` (set before first call) → `PEEROVA_LIB` env →
`./libpeerova.so` / `../../target/release/libpeerova.so` (dev, desktop only) → `DynamicLibrary.open(<platform name>)`;
iOS always uses `DynamicLibrary.process()` (static link).

## Flutter

**Polling.** `PeerovaServer.start` blocks up to ~3 s and `pollWait` blocks — keep both off the UI isolate:

* Simple (game-loop style): a client connect returns immediately; a server start costs a one-off
  hitch of up to ~3 s (show a spinner first). Then poll non-blocking on the UI isolate:
  ```dart
  Timer.periodic(const Duration(milliseconds: 16), (_) {
    PeerovaEvent? ev;
    while ((ev = node.poll()) != null) handle(ev!);   // non-blocking
  });
  ```
* Background: spawn an `Isolate` that owns the node, loops `pollWait(Duration(milliseconds: 50))` and forwards
  events to the UI via a `SendPort`; send commands back through a `ReceivePort`. (Native nodes are thread-safe.)

**Bundling the native library** (name must stay `peerova`):

| Platform | File | Where |
|---|---|---|
| Android | `libpeerova.so` per ABI (`cargo ndk -t arm64-v8a -t armeabi-v7a -t x86_64 -o android/app/src/main/jniLibs build --release -p peerova-ffi`) | `android/app/src/main/jniLibs/<abi>/libpeerova.so`; add `<uses-permission android:name="android.permission.INTERNET"/>` |
| iOS | `libpeerova.a` (`--target aarch64-apple-ios`, sim: `aarch64-apple-ios-sim`), ideally as `Peerova.xcframework` (`xcodebuild -create-xcframework -library ...a -headers include ...`) | Xcode → Runner → *Frameworks, Libraries* (or a podspec `vendored_frameworks`). Add `-force_load` / `-all_load` (or `OTHER_LDFLAGS = -lpeerova`) so the symbols are not dead-stripped, since Dart looks them up at runtime via `DynamicLibrary.process()`. |
| macOS | `libpeerova.dylib` (lipo arm64+x86_64) | Xcode: *Copy Files → Frameworks*; or link statically (then `process()` fallback is used). Enable `com.apple.security.network.client`/`server` entitlements. |
| Windows | `peerova.dll` | next to the runner `.exe` (CMake `install(FILES ... DESTINATION ${INSTALL_BUNDLE_LIB_DIR})` in `windows/CMakeLists.txt`) |
| Linux | `libpeerova.so` | `linux/CMakeLists.txt`: `install(FILES ... DESTINATION ${INSTALL_BUNDLE_LIB_DIR})` → `bundle/lib/` (Flutter's rpath is `$ORIGIN/lib`) |

Status: Dart VM on Linux x64 **tested**; Flutter (all platforms) **untested** — the bundling steps above are
the standard Flutter FFI recipes. Alternatively wrap this as an FFI plugin (`flutter create --template=plugin_ffi`)
and put the prebuilt libraries in its platform folders.
