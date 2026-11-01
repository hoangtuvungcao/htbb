# Native plugins / Thư viện native

- [Tiếng Việt](#tiếng-việt)
- [English](#english)

## Tiếng Việt

Chép thư viện native Peerova của từng nền tảng vào đây:

| Thư mục | File | Lấy ở đâu |
|---|---|---|
| `x86_64/` | `peerova.dll` (Windows x64) | Gói SDK: `lib/windows-x86_64/peerova.dll` (không cần `wintun.dll`) |
| `x86_64/` | `libpeerova.so` (Linux x64) | Gói SDK: `lib/linux-x86_64/libpeerova.so` |
| `macOS/` | `libpeerova.dylib` | Liên hệ Peerova để nhận bản build này |
| `Android/arm64-v8a/`, `Android/armeabi-v7a/`, `Android/x86_64/` | `libpeerova.so` | Liên hệ Peerova để nhận bản build này |
| `iOS/` | `libpeerova.a` (tĩnh) | Liên hệ Peerova để nhận bản build này |

Sau khi chép, chọn từng file trong Inspector của Unity và chỉnh **Plugin import settings**:

- `x86_64/peerova.dll`: Editor + Standalone, OS = Windows, CPU = x86_64.
- `x86_64/libpeerova.so`: Editor + Standalone, OS = Linux, CPU = x86_64.
- `macOS/libpeerova.dylib`: Editor + Standalone, OS = macOS, CPU = AnyCPU.
- `Android/<abi>/libpeerova.so`: chỉ Android, CPU khớp ABI (ARM64 / ARMv7 / x86_64).
  Player Settings: Scripting Backend IL2CPP, Target Architectures phải khớp các ABI bạn phát hành.
- `iOS/libpeerova.a`: chỉ iOS. Trên iOS phần C# tự dùng `DllImport("__Internal")`. Nếu linker báo thiếu
  symbol hệ thống, thêm `Security.framework`, `SystemConfiguration.framework` và `libresolv.tbd` vào project Xcode.

Unity lưu các thiết lập này trong file `.meta` cạnh thư viện — hãy commit chúng.
Unity chỉ nạp native plugin một lần mỗi phiên Editor: sau khi thay `.dll/.so`, khởi động lại Editor.
Tên thư viện phải là `peerova` (`peerova.dll` / `libpeerova.so` / `libpeerova.dylib`) vì binding dùng `DllImport("peerova")`.

## English

Put the Peerova native library for each platform here:

| Folder | File | Where to get it |
|---|---|---|
| `x86_64/` | `peerova.dll` (Windows x64) | SDK package: `lib/windows-x86_64/peerova.dll` (`wintun.dll` is not needed) |
| `x86_64/` | `libpeerova.so` (Linux x64) | SDK package: `lib/linux-x86_64/libpeerova.so` |
| `macOS/` | `libpeerova.dylib` | Contact Peerova for this build |
| `Android/arm64-v8a/`, `Android/armeabi-v7a/`, `Android/x86_64/` | `libpeerova.so` | Contact Peerova for this build |
| `iOS/` | `libpeerova.a` (static) | Contact Peerova for this build |

After copying, select each file in the Unity Inspector and set its **Plugin import settings**:

- `x86_64/peerova.dll`: Editor + Standalone, OS = Windows, CPU = x86_64.
- `x86_64/libpeerova.so`: Editor + Standalone, OS = Linux, CPU = x86_64.
- `macOS/libpeerova.dylib`: Editor + Standalone, OS = macOS, CPU = AnyCPU.
- `Android/<abi>/libpeerova.so`: Android only, CPU = matching ABI (ARM64 / ARMv7 / x86_64).
  Player Settings: Scripting Backend IL2CPP; Target Architectures must match the ABIs you ship.
- `iOS/libpeerova.a`: iOS only. On iOS the C# side automatically uses `DllImport("__Internal")`. If the linker
  reports missing system symbols, add `Security.framework`, `SystemConfiguration.framework` and `libresolv.tbd` to the Xcode project.

Unity stores these settings in `.meta` files next to the libraries — commit them.
Unity loads native plugins once per Editor session: after replacing a `.dll/.so`, restart the Editor.
The library must be named `peerova` (`peerova.dll` / `libpeerova.so` / `libpeerova.dylib`) because the binding uses `DllImport("peerova")`.
