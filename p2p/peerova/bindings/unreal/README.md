# Peerova — plugin Unreal Engine 5 / Unreal Engine 5 plugin

[Tiếng Việt](#tiếng-việt) · [English](#english)

---

## Tiếng Việt

Plugin `Peerova` cho Unreal Engine 5.x: kết nối P2P trực tiếp, mã hoá đầu-cuối, dùng được từ Blueprint và
C++ qua `UPeerovaSubsystem` (một node host hoặc client cho mỗi game instance). Lõi C++ không phụ thuộc engine
`PeerovaCore.h` dùng được trong mọi dự án C++.

Trạng thái: lõi `PeerovaCore.h` và `examples/core_demo.cpp` đã được biên dịch và chạy thử (g++ / clang++). Phần
Unreal (subsystem, `Peerova.Build.cs`, `Peerova_APL.xml`, ví dụ actor) **chưa được build trong Unreal** — hãy
báo cho Peerova nếu gặp lỗi biên dịch.

Khái niệm chung và C API đầy đủ: [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md). Website: https://peerova.pages.dev

### Cấu trúc

```
Peerova/                                    chép thư mục này vào <Project>/Plugins/Peerova
  Peerova.uplugin
  Source/Peerova/Peerova.Build.cs           đường dẫn thư viện theo nền tảng, delay-load, RuntimeDependencies
  Source/Peerova/Peerova_APL.xml            Android: đóng gói lib/<abi>/libpeerova.so, quyền INTERNET
  Source/Peerova/Public/PeerovaCore.h       wrapper C++17 RAII header-only (không dùng kiểu của UE)
  Source/Peerova/Public/PeerovaSubsystem.h  UPeerovaSubsystem (UGameInstanceSubsystem + FTickableGameObject)
  Source/Peerova/Public/PeerovaModule.h     module: nạp peerova.dll / libpeerova.dylib
  Source/ThirdParty/PeerovaLibrary/         include/peerova.h + lib/<Platform>/ (xem README trong đó)
examples/PeerovaDemoActor.h/.cpp            actor mẫu: đồng bộ vị trí 20 Hz + chat
examples/core_demo.cpp                      cùng demo, không cần Unreal (chỉ dùng PeerovaCore.h)
```

### Yêu cầu

- Unreal Engine 5.x, dự án có C++ (plugin được build từ mã nguồn; dự án chỉ có Blueprint thì thêm một class
  C++ bất kỳ để có toolchain).
- Thư viện native Peerova cho từng nền tảng bạn phát hành.

### Đặt thư viện native ở đâu

Trong `Peerova/Source/ThirdParty/PeerovaLibrary/lib/`:

| Nền tảng | File | Lấy từ đâu |
|---|---|---|
| Windows (Win64) | `Win64/peerova.dll` + `Win64/peerova.dll.lib` | `lib/windows-x86_64/peerova.dll` trong gói SDK. Import library MSVC `peerova.dll.lib`: nếu gói không có, tạo từ `Win64/peerova.def` trong *x64 Native Tools Command Prompt for VS*: `lib /def:peerova.def /machine:x64 /out:peerova.dll.lib` |
| Linux | `Linux/libpeerova.so` | `lib/linux-x86_64/libpeerova.so` trong gói SDK |
| macOS | `Mac/libpeerova.dylib` | liên hệ Peerova |
| Android | `Android/<arm64-v8a\|armeabi-v7a\|x86_64>/libpeerova.so` | liên hệ Peerova |
| iOS | `IOS/libpeerova.a` (static) | liên hệ Peerova |

Khi build/đóng gói, plugin tự chép thư viện: Windows → `peerova.dll` cạnh file exe; Linux → `libpeerova.so`
cạnh binary (tìm qua rpath `$ORIGIN`); macOS → `libpeerova.dylib` cạnh binary của game; Android → APK
`lib/<abi>/`; iOS → link tĩnh vào app. `wintun.dll` **không** cần cho SDK (chỉ dùng cho chế độ VPN của công cụ
dòng lệnh `peerova`).

### Cài đặt

1. Chép `Peerova/` vào `<Project>/Plugins/Peerova/`.
2. Đặt thư viện native như bảng trên.
3. Generate project files, build, bật plugin (Edit → Plugins → Networking → Peerova).
4. C++: thêm `"Peerova"` vào `PublicDependencyModuleNames` trong `<YourGame>.Build.cs`.

### Bắt đầu nhanh (5 phút)

**Blueprint** — `Get Game Instance → Get Subsystem (Peerova Subsystem)`, rồi:

- **Host (server):** bind **OnHostStarted** (Ticket) → hiển thị/gửi ticket cho người chơi; gọi **Host**
  (Port 47000, ExtraConfigJson tuỳ chọn, ví dụ `{"license":"PVA-..."}`). Mã kết nối 6 ký tự: host với
  `{"app":"prj_...","share":{"name":"Phòng của tôi","public":true,"max":8}}` rồi đọc **Get Room Code** (rỗng
  cho tới khi website cấp mã, thường vài giây sau OnHostStarted).
- **Join (client):** gọi **Join** (Ticket — ticket `pvt_...` hoặc mã kết nối 6 ký tự); bind **OnConnected**
  (Peer).
- **OnMessage** (Peer, Data, Channel, bReliable) — dùng **Bytes To Utf8 String** để đọc text.
- **Send** / **SendString** (Peer, …, Channel 0..255, bReliable), **Disconnect** (Peer), **Shutdown**.
- **OnDisconnected** (Peer, Reason), **OnConnectFailed** (Reason — cũng được gọi khi Host thất bại).
- **Get Ticket**, **Get Room Code**, **Get Peer Count**, **Is Active**, **Is Host Pending**, **Get Last Error**.

**C++:**

```cpp
#include "PeerovaSubsystem.h"

UPeerovaSubsystem* Net = GetGameInstance()->GetSubsystem<UPeerovaSubsystem>();
Net->OnHostStarted.AddDynamic(this, &AMyActor::HandleHostStarted);   // UFUNCTION void(const FString& Ticket)
Net->OnConnected.AddDynamic(this, &AMyActor::HandleConnected);       // UFUNCTION void(int32 Peer)
Net->OnMessage.AddDynamic(this, &AMyActor::HandleMessage);           // UFUNCTION void(int32, const TArray<uint8>&, int32, bool)

Net->Host(47000);                        // server: khởi động trên thread nền (≤ ~3 giây), rồi OnHostStarted(Ticket)
// client:
Net->Join(Ticket);                       // hoặc Net->Join(TEXT("K7Q2XD")); rồi OnConnected(Peer)
Net->SendString(Peer, TEXT("hello"), /*Channel*/ 0, /*bReliable*/ true);
```

Ví dụ đầy đủ: [examples/PeerovaDemoActor.cpp](examples/PeerovaDemoActor.cpp) (chép vào module game của bạn).

**Không dùng Unreal** — dùng thẳng `PeerovaCore.h`:

```cpp
auto srv = peerova::Node::server(R"({"port":47000})");   // chặn ≤ ~3 giây
auto cli = peerova::Node::client(srv.ticket());           // hoặc mã kết nối 6 ký tự
peerova::Event ev;
while (cli.poll(ev, 100)) if (ev.type == peerova::EventType::Connected) cli.send(ev.peer, "hi");
```

### Gửi: reliable hay unreliable

| | `bReliable = true` | `bReliable = false` |
|---|---|---|
| Đảm bảo | có thứ tự, gửi lại khi mất (theo từng kênh) | datagram: có thể mất hoặc đến sai thứ tự |
| Kích thước | có thể lớn | tối đa **1180 byte** mỗi message (lớn hơn → `Send` trả `false`) |
| Dùng cho | chat, inventory, RPC, từng phần file | input gameplay, vị trí, trạng thái thời gian thực kiểu voice |

Kênh 0..255. Các kênh khác nhau không chặn nhau: một message reliable lớn trên kênh 0 không làm chậm kênh 1.

### Nhận event (Tick)

`UPeerovaSubsystem` là `FTickableGameObject`: mỗi frame nó gọi `poll` không chặn và phát tối đa
`MaxEventsPerTick` event (mặc định 4096; phần còn lại để frame sau). Vì vậy **mọi delegate chạy trên game
thread**, kể cả khi game đang pause. Không cần tự tạo thread.

| Delegate | Khi nào |
|---|---|
| `OnHostStarted(Ticket)` | Host đã sẵn sàng |
| `OnConnected(Peer)` | Host: một client vừa vào. Client: đã kết nối tới host |
| `OnMessage(Peer, Data, Channel, bReliable)` | có dữ liệu |
| `OnDisconnected(Peer, Reason)` | peer đã ngắt |
| `OnConnectFailed(Reason)` | client không kết nối được (hết thời gian, sai token, host đầy...) hoặc Host thất bại |

**Peer id là `int32`:** id gốc là số không dấu 32 bit; Blueprint không có `uint32` nên plugin chuyển nguyên
bit sang `int32` — id lớn hơn 2147483647 hiện thành **số âm**, đó là id hợp lệ. Coi nó như handle: truyền lại
nguyên vẹn cho `Send`/`SendString`/`Disconnect`, so sánh, làm key của map; đừng kiểm tra `Peer >= 0`. Trong C++
có thể lấy lại giá trị gốc bằng `static_cast<uint32>(Peer)`.

Dùng `PeerovaCore.h` trực tiếp: gọi `node.poll(ev, 0)` trong vòng lặp tick cho tới khi trả `false`.

### Ngắt kết nối và dọn dẹp

- `Disconnect(Peer)`: đóng một kết nối; cả hai phía nhận `OnDisconnected`.
- `Shutdown()`: ngắt tất cả và giải phóng node; an toàn khi gọi bất cứ lúc nào (kể cả khi Host đang chờ, hoặc
  trong một delegate). Được gọi tự động khi game instance kết thúc. Gọi `Host`/`Join` lần nữa sẽ thay node cũ.
- `PeerovaCore.h`: `node.reset()` hoặc hủy `Node`. Không hủy node khi thread khác đang `poll`/`send` trên nó.

### Xử lý lỗi

- `Join` trả `false` (và `OnConnectFailed` được gọi ngay) khi ticket sai, mã không tồn tại hoặc config sai.
- Host thất bại (port bận, license sai...) → `OnConnectFailed(Reason)`.
- `Send`/`SendString` trả `false` khi chưa có node, kênh ngoài 0..255, peer không tồn tại, hoặc unreliable >
  1180 byte. Lý do: `GetLastError()` (văn bản từ thư viện native) và log `LogPeerova`.
- `PeerovaCore.h` không ném exception (build được với exceptions tắt): factory trả `Node` không hợp lệ
  (`!node`) và `node.error`; `send()` trả `false` và đặt `node.error`.

### Cấu hình

`Host(Port, ExtraConfigJson)` — `ExtraConfigJson` là một object JSON:

| Khoá | Ý nghĩa |
|---|---|
| `license` | khoá license `PVA-...`. Không có → chế độ dev, tối đa 3 client |
| `max_ccu` | số client đồng thời tối đa |
| `token` | bí mật chung mà client phải gửi kèm |
| `upnp` | xin router mở cổng, mặc định `true` |
| `private` | ticket không chứa địa chỉ IP (chia sẻ ticket không lộ địa chỉ host), mặc định `false` |
| `key_file` | file khoá danh tính, mặc định `Saved/Peerova/server.key`. Cùng khoá ⇒ cùng ticket |
| `app`, `share` | mã kết nối 6 ký tự: `app` = project id trên website Peerova, `share` = `{"name","public","max","mode","region"}` |

`Port`: cổng UDP, mặc định `47000`; `0` = cổng ngẫu nhiên (ghi đè `port` trong JSON).

`Join(Ticket, ConfigJson)` — `ConfigJson`: `token` (phải khớp với host), `timeout_ms` (mặc định `20000`).

Kết nối là tự động: ưu tiên kết nối trực tiếp, và tự động dùng đường dự phòng (vẫn mã hoá) khi mạng chặn kết
nối trực tiếp. Lưu ý: `Join` với mã kết nối sẽ tra mã trên website trước, việc này chặn game thread trong thời
gian của request đó.

### Bảo mật

- `server.key` là danh tính của host: ai có file này có thể giả mạo host. Giữ bí mật, không commit, không đóng
  gói vào content (mặc định nằm trong `Saved/`, không bị đóng gói).
- Ticket có thể chia sẻ cho người chơi. `token` và `license` là bí mật — đừng hard-code license trong bản build
  phát cho người chơi nếu người chơi không phải là người host.
- Mọi dữ liệu được mã hoá đầu-cuối.

### Khắc phục sự cố

- Chạy `peerova doctor` (công cụ dòng lệnh) để chẩn đoán mạng.
- **Không nạp được thư viện** (log `Peerova: could not load peerova.dll`, hoặc lỗi link): kiểm tra file nằm
  đúng `ThirdParty/PeerovaLibrary/lib/<Platform>/`; Windows cần cả `peerova.dll.lib`.
- **`OnConnectFailed` do hết thời gian**: kiểm tra ticket/mã còn đúng, host đang chạy, chạy `peerova doctor`
  ở cả hai máy.
- **Client thứ 4 bị từ chối**: host đang ở chế độ dev (tối đa 3 client) — thêm `license`.
- **`Send` unreliable trả `false`**: payload > 1180 byte; dùng reliable hoặc chia nhỏ.
- **Không gửi được tới peer có id âm**: bạn đang lọc `Peer >= 0` — id âm là hợp lệ (xem phần Peer id).

### Kiểm tra không cần Unreal

Biên dịch và chạy `examples/core_demo.cpp` trên Linux (từ thư mục `bindings/unreal` của gói SDK):

```sh
g++ -std=c++17 -IPeerova/Source/ThirdParty/PeerovaLibrary/include -IPeerova/Source/Peerova/Public \
    examples/core_demo.cpp -L../../lib/linux-x86_64 -lpeerova -Wl,-rpath,'$ORIGIN' -o core_demo
cp ../../lib/linux-x86_64/libpeerova.so .
./core_demo server 10                 # in "ticket: pvt_..."
./core_demo client pvt_... 5          # terminal khác: gửi vị trí 20 Hz + một dòng chat mỗi giây
```

---

## English

The `Peerova` plugin for Unreal Engine 5.x: direct, end-to-end encrypted P2P connections, usable from Blueprint
and C++ through `UPeerovaSubsystem` (one host or client node per game instance). The engine-agnostic C++ core
`PeerovaCore.h` works in any C++ project.

Status: the `PeerovaCore.h` core and `examples/core_demo.cpp` are compiled and run (g++ / clang++). The Unreal parts
(subsystem, `Peerova.Build.cs`, `Peerova_APL.xml`, example actor) are **not yet built inside Unreal** — please
report compile errors to Peerova.

Concepts and the full C API: [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md). Website: https://peerova.pages.dev

### Layout

```
Peerova/                                    copy this folder to <Project>/Plugins/Peerova
  Peerova.uplugin
  Source/Peerova/Peerova.Build.cs           per-platform library paths, delay-load, RuntimeDependencies
  Source/Peerova/Peerova_APL.xml            Android: packages lib/<abi>/libpeerova.so, INTERNET permission
  Source/Peerova/Public/PeerovaCore.h       header-only C++17 RAII wrapper (no UE types)
  Source/Peerova/Public/PeerovaSubsystem.h  UPeerovaSubsystem (UGameInstanceSubsystem + FTickableGameObject)
  Source/Peerova/Public/PeerovaModule.h     module: loads peerova.dll / libpeerova.dylib
  Source/ThirdParty/PeerovaLibrary/         include/peerova.h + lib/<Platform>/ (see its README)
examples/PeerovaDemoActor.h/.cpp            example actor: 20 Hz position sync + chat
examples/core_demo.cpp                      the same demo without Unreal (PeerovaCore.h only)
```

### Requirements

- Unreal Engine 5.x and a C++ project (the plugin is built from source; for a Blueprint-only project add any C++
  class to get the toolchain).
- The Peerova native library for each platform you ship.

### Where to put the native library

In `Peerova/Source/ThirdParty/PeerovaLibrary/lib/`:

| Platform | File | Where to get it |
|---|---|---|
| Windows (Win64) | `Win64/peerova.dll` + `Win64/peerova.dll.lib` | `lib/windows-x86_64/peerova.dll` from the SDK package. The MSVC import library `peerova.dll.lib`: if your package does not include it, generate it from `Win64/peerova.def` in an *x64 Native Tools Command Prompt for VS*: `lib /def:peerova.def /machine:x64 /out:peerova.dll.lib` |
| Linux | `Linux/libpeerova.so` | `lib/linux-x86_64/libpeerova.so` from the SDK package |
| macOS | `Mac/libpeerova.dylib` | contact Peerova |
| Android | `Android/<arm64-v8a\|armeabi-v7a\|x86_64>/libpeerova.so` | contact Peerova |
| iOS | `IOS/libpeerova.a` (static) | contact Peerova |

When building/packaging, the plugin copies the library for you: Windows → `peerova.dll` next to the exe;
Linux → `libpeerova.so` next to the binary (found through rpath `$ORIGIN`); macOS → `libpeerova.dylib` next
to the game binary; Android → the APK's `lib/<abi>/`; iOS → statically linked into the app. `wintun.dll` is
**not** needed by the SDK (only by the `peerova` command-line tool's VPN mode).

### Install

1. Copy `Peerova/` to `<Project>/Plugins/Peerova/`.
2. Put the native libraries in place (table above).
3. Regenerate project files, build, enable the plugin (Edit → Plugins → Networking → Peerova).
4. C++: add `"Peerova"` to `PublicDependencyModuleNames` in `<YourGame>.Build.cs`.

### 5-minute quick start

**Blueprint** — `Get Game Instance → Get Subsystem (Peerova Subsystem)`, then:

- **Host (server):** bind **OnHostStarted** (Ticket) → show/send the ticket to players; call **Host**
  (Port 47000, optional ExtraConfigJson such as `{"license":"PVA-..."}`). 6-character connect code: host with
  `{"app":"prj_...","share":{"name":"My room","public":true,"max":8}}` and read **Get Room Code** (empty until the
  website assigns the code, usually a few seconds after OnHostStarted).
- **Join (client):** call **Join** (Ticket — a `pvt_...` ticket or a 6-character connect code); bind
  **OnConnected** (Peer).
- **OnMessage** (Peer, Data, Channel, bReliable) — use **Bytes To Utf8 String** for text.
- **Send** / **SendString** (Peer, …, Channel 0..255, bReliable), **Disconnect** (Peer), **Shutdown**.
- **OnDisconnected** (Peer, Reason), **OnConnectFailed** (Reason — also fired when Host fails).
- **Get Ticket**, **Get Room Code**, **Get Peer Count**, **Is Active**, **Is Host Pending**, **Get Last Error**.

**C++:**

```cpp
#include "PeerovaSubsystem.h"

UPeerovaSubsystem* Net = GetGameInstance()->GetSubsystem<UPeerovaSubsystem>();
Net->OnHostStarted.AddDynamic(this, &AMyActor::HandleHostStarted);   // UFUNCTION void(const FString& Ticket)
Net->OnConnected.AddDynamic(this, &AMyActor::HandleConnected);       // UFUNCTION void(int32 Peer)
Net->OnMessage.AddDynamic(this, &AMyActor::HandleMessage);           // UFUNCTION void(int32, const TArray<uint8>&, int32, bool)

Net->Host(47000);                        // server: starts on a background thread (≤ ~3 s), then OnHostStarted(Ticket)
// client:
Net->Join(Ticket);                       // or Net->Join(TEXT("K7Q2XD")); then OnConnected(Peer)
Net->SendString(Peer, TEXT("hello"), /*Channel*/ 0, /*bReliable*/ true);
```

Full example: [examples/PeerovaDemoActor.cpp](examples/PeerovaDemoActor.cpp) (copy it into your game module).

**Without Unreal** — use `PeerovaCore.h` directly:

```cpp
auto srv = peerova::Node::server(R"({"port":47000})");   // blocks ≤ ~3 s
auto cli = peerova::Node::client(srv.ticket());           // or a 6-character connect code
peerova::Event ev;
while (cli.poll(ev, 100)) if (ev.type == peerova::EventType::Connected) cli.send(ev.peer, "hi");
```

### Sending: reliable or unreliable

| | `bReliable = true` | `bReliable = false` |
|---|---|---|
| Guarantees | ordered, retransmitted (per channel) | datagram: may be lost or reordered |
| Size | can be large | max **1180 bytes** per message (larger → `Send` returns `false`) |
| Use for | chat, inventory, RPC, file chunks | gameplay inputs, positions, voice-like real-time state |

Channels are 0..255. Different channels don't block each other: a large reliable message on channel 0 does
not delay channel 1.

### Receiving events (Tick)

`UPeerovaSubsystem` is an `FTickableGameObject`: every frame it calls the non-blocking `poll` and broadcasts up
to `MaxEventsPerTick` events (default 4096; the rest wait for the next frame). So **every delegate fires on the
game thread**, also while the game is paused. You don't need your own thread.

| Delegate | When |
|---|---|
| `OnHostStarted(Ticket)` | hosting is ready |
| `OnConnected(Peer)` | Host: a client joined. Client: connected to the host |
| `OnMessage(Peer, Data, Channel, bReliable)` | data arrived |
| `OnDisconnected(Peer, Reason)` | the peer is gone |
| `OnConnectFailed(Reason)` | the client could not connect (timeout, wrong token, host full...) or Host failed |

**Peer ids are `int32`:** the native id is an unsigned 32-bit number; Blueprint has no `uint32`, so the plugin
bit-casts it to `int32` — ids above 2147483647 show up as **negative numbers**, and they are valid. Treat the
id as a handle: pass it back unchanged to `Send`/`SendString`/`Disconnect`, compare it, use it as a map key;
never check `Peer >= 0`. In C++ you get the native value back with `static_cast<uint32>(Peer)`.

Using `PeerovaCore.h` directly: call `node.poll(ev, 0)` in your tick loop until it returns `false`.

### Disconnect and cleanup

- `Disconnect(Peer)`: closes one connection; both sides get `OnDisconnected`.
- `Shutdown()`: disconnects everyone and frees the node; safe at any time (also while Host is pending, or from
  inside a delegate). Called automatically when the game instance shuts down. Calling `Host`/`Join` again
  replaces the current node.
- `PeerovaCore.h`: `node.reset()` or destroy the `Node`. Never destroy it while another thread is inside
  `poll`/`send` on it.

### Error handling

- `Join` returns `false` (and `OnConnectFailed` fires immediately) for a malformed ticket, unknown code or bad
  config.
- Host failure (port in use, invalid license...) → `OnConnectFailed(Reason)`.
- `Send`/`SendString` return `false` when there is no node, the channel is outside 0..255, the peer is unknown,
  or an unreliable payload exceeds 1180 bytes. The reason: `GetLastError()` (text from the native library) and
  the `LogPeerova` log.
- `PeerovaCore.h` never throws (it builds with exceptions disabled): factories return an invalid `Node`
  (`!node`) with `node.error`; `send()` returns `false` and sets `node.error`.

### Configuration

`Host(Port, ExtraConfigJson)` — `ExtraConfigJson` is a JSON object:

| Key | Meaning |
|---|---|
| `license` | `PVA-...` license key. Without it: dev mode, max 3 clients |
| `max_ccu` | maximum concurrent clients |
| `token` | shared secret clients must present |
| `upnp` | ask the router to open the port, default `true` |
| `private` | the ticket contains no IP addresses (sharing it does not reveal the host's address), default `false` |
| `key_file` | identity key file, default `Saved/Peerova/server.key`. Same key ⇒ same ticket |
| `app`, `share` | 6-character connect code: `app` = project id from the Peerova website, `share` = `{"name","public","max","mode","region"}` |

`Port`: UDP port, default `47000`; `0` = random free port (overrides `port` in the JSON).

`Join(Ticket, ConfigJson)` — `ConfigJson`: `token` (must match the host), `timeout_ms` (default `20000`).

Connections are automatic: direct first, with an automatic encrypted fallback route when networks block direct
connections. Note: `Join` with a connect code first looks the code up on the website, which blocks the game
thread for that request.

### Security

- `server.key` is the host's identity: anyone who has it can impersonate the host. Keep it secret, never commit
  it, never package it as content (the default location `Saved/` is not packaged).
- The ticket is meant to be shared with players. `token` and `license` are secrets — don't hard-code the
  license in builds you give to players unless they are the ones hosting.
- All traffic is end-to-end encrypted.

### Troubleshooting

- Run `peerova doctor` (command-line tool) for a network diagnosis.
- **Library not loaded** (log `Peerova: could not load peerova.dll`, or link errors): check the files are in
  `ThirdParty/PeerovaLibrary/lib/<Platform>/`; Windows also needs `peerova.dll.lib`.
- **`OnConnectFailed` with a timeout**: check the ticket/code is current and the host is running; run
  `peerova doctor` on both machines.
- **The 4th client is refused**: the host is in dev mode (max 3 clients) — add a `license`.
- **Unreliable `Send` returns `false`**: payload > 1180 bytes; send it reliable or split it.
- **Can't send to a peer with a negative id**: you are filtering on `Peer >= 0` — negative ids are valid (see
  Peer ids).

### Checking without Unreal

Build and run `examples/core_demo.cpp` on Linux (from the SDK package's `bindings/unreal` folder):

```sh
g++ -std=c++17 -IPeerova/Source/ThirdParty/PeerovaLibrary/include -IPeerova/Source/Peerova/Public \
    examples/core_demo.cpp -L../../lib/linux-x86_64 -lpeerova -Wl,-rpath,'$ORIGIN' -o core_demo
cp ../../lib/linux-x86_64/libpeerova.so .
./core_demo server 10                 # prints "ticket: pvt_..."
./core_demo client pvt_... 5          # another terminal: positions at 20 Hz + one chat line per second
```
