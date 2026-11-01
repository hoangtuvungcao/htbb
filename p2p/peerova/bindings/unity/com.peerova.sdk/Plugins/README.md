# Native plugins

Put the native peerova library for each platform here (build from the repo root, see bindings/README.md):

| Folder | File | Build |
|---|---|---|
| `x86_64/` | `peerova.dll` (Windows x64) | `cargo build --release -p peerova-ffi --target x86_64-pc-windows-gnu` (or `-msvc`) |
| `x86_64/` | `libpeerova.so` (Linux x64) | `cargo build --release -p peerova-ffi` |
| `macOS/` | `libpeerova.dylib` (universal: `lipo -create` x86_64 + aarch64) | `--target aarch64-apple-darwin` / `x86_64-apple-darwin` |
| `Android/arm64-v8a/` | `libpeerova.so` | `cargo ndk -t arm64-v8a build --release -p peerova-ffi` |
| `Android/armeabi-v7a/` | `libpeerova.so` | `cargo ndk -t armeabi-v7a ...` |
| `Android/x86_64/` | `libpeerova.so` (emulator) | `cargo ndk -t x86_64 ...` |
| `iOS/` | `libpeerova.a` (static) | `cargo build --release -p peerova-ffi --target aarch64-apple-ios` |

After dropping the files in, select each one in the Unity Inspector and set **Plugin import settings**:

- `x86_64/peerova.dll`: Editor + Standalone, OS = Windows, CPU = x86_64.
- `x86_64/libpeerova.so`: Editor + Standalone, OS = Linux, CPU = x86_64.
- `macOS/libpeerova.dylib`: Editor + Standalone, OS = macOS, CPU = AnyCPU.
- `Android/<abi>/libpeerova.so`: Android only, CPU = matching ABI (ARM64 / ARMv7 / x86_64).
  Player Settings: Scripting Backend IL2CPP, Target Architectures must match the ABIs you ship.
- `iOS/libpeerova.a`: iOS only. The C# side automatically uses `DllImport("__Internal")` on iOS
  (`UNITY_IOS && !UNITY_EDITOR`). If the linker complains about missing system symbols, add
  `Security.framework` / `SystemConfiguration.framework` / `libresolv.tbd` in the Xcode project
  (exact list depends on the Rust crates; check the link errors).

Unity remembers these settings in `.meta` files next to the libraries — commit them.
Note: Unity loads native plugins once per Editor session; after replacing a .dll/.so, restart the Editor.
The library must be named `peerova` (peerova.dll / libpeerova.so / libpeerova.dylib) because the binding uses `DllImport("peerova")`.
