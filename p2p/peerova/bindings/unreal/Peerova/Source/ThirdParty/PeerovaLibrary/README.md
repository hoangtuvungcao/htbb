# PeerovaLibrary (native library for the Unreal plugin)

`include/peerova.h` is an identical copy of the SDK's `include/peerova.h`. Put the native libraries here:

| Folder | File(s) | Where to get it |
|---|---|---|
| `lib/Win64/` | `peerova.dll` + `peerova.dll.lib` | `peerova.dll` from the SDK's `lib/windows-x86_64/`. The MSVC import library `peerova.dll.lib`: if your package does not include it, generate it from `lib/Win64/peerova.def` in a *x64 Native Tools Command Prompt for VS*: `lib /def:peerova.def /machine:x64 /out:peerova.dll.lib` |
| `lib/Linux/` | `libpeerova.so` | SDK `lib/linux-x86_64/libpeerova.so` |
| `lib/Mac/` | `libpeerova.dylib` (universal arm64 + x86_64, install name `@rpath/libpeerova.dylib`) | contact Peerova |
| `lib/Android/arm64-v8a/`, `armeabi-v7a/`, `x86_64/` | `libpeerova.so` per ABI (SONAME `libpeerova.so`) | contact Peerova |
| `lib/IOS/` | `libpeerova.a` (static, arm64) | contact Peerova |

You only need the platforms you ship. `wintun.dll` is **not** needed (it is only used by the `peerova`
command-line tool's VPN mode). Check a Linux/Android library with `readelf -d libpeerova.so | grep SONAME`.
