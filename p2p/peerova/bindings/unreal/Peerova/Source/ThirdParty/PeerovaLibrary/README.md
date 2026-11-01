# PeerovaLibrary (third-party native lib for the Unreal plugin)

`include/peerova.h` is a copy of `crates/peerova-ffi/include/peerova.h` (refresh: `bindings/sync-headers.sh`).
Drop the prebuilt libraries here (from the repo root):

| Folder | File(s) | Build command |
|---|---|---|
| `lib/Win64/` | `peerova.dll`, `peerova.dll.lib` | `cargo build --release -p peerova-ffi --target x86_64-pc-windows-msvc` (needs MSVC; the `-gnu` target's `libpeerova.dll.a` is not usable by UE's MSVC linker) |
| `lib/Linux/` | `libpeerova.so` | `cargo build --release -p peerova-ffi` |
| `lib/Mac/` | `libpeerova.dylib` (universal) | build `aarch64-apple-darwin` + `x86_64-apple-darwin`, `lipo -create -output libpeerova.dylib …`, `install_name_tool -id @rpath/libpeerova.dylib libpeerova.dylib` |
| `lib/Android/arm64-v8a/` etc. | `libpeerova.so` per ABI | `RUSTFLAGS="-C link-arg=-Wl,-soname,libpeerova.so" cargo ndk -t arm64-v8a -t armeabi-v7a -t x86_64 -o out build --release -p peerova-ffi`, copy `out/<abi>/libpeerova.so` |
| `lib/IOS/` | `libpeerova.a` | `cargo build --release -p peerova-ffi --target aarch64-apple-ios` |

Why the SONAME on Android: the Rust cdylib has no `SONAME`, and UBT links Android libs by full
path, which would bake the build machine's absolute path into `libUnreal.so`'s `DT_NEEDED`.
(Linux avoids this because `Peerova.Build.cs` links with `-L … -lpeerova`.) Check with
`readelf -d libpeerova.so | grep SONAME`.
