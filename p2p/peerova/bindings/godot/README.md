# Peerova — Godot 4

- [Tiếng Việt](#tiếng-việt)
- [English](#english)

Website: https://peerova.pages.dev · Khái niệm và C API đầy đủ / Concepts and the full C API: [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md)
API C# cấp thấp / low-level C# API: [../csharp/README.md](../csharp/README.md)

---

## Tiếng Việt

`addons/peerova/` là một addon Godot 4 dựa trên binding C# (`addons/peerova/Peerova.cs` là bản copy của `bindings/csharp/Peerova.cs`).
Nó cung cấp node **PeerovaNode**: tự poll trong `_process` và phát signal trên main thread, dùng được từ cả C# và GDScript.

### Yêu cầu

- Godot **4.2+ bản .NET** (bản chuẩn không có C#). Project cần một C# solution
  (*Project → Tools → C# → Create C# solution*). Mã game có thể viết hoàn toàn bằng GDScript.
- Thư viện native Peerova (xem bảng dưới).

### Đặt thư viện native ở đâu

| Hệ điều hành | Trong editor | Khi export |
|---|---|---|
| Windows x64 | `addons/peerova/bin/windows/peerova.dll` (từ `lib/windows-x86_64/`) | `peerova.dll` cạnh `game.exe`. **Không** cần `wintun.dll` (chỉ công cụ dòng lệnh `peerova` cần cho chế độ VPN). |
| Linux x64 | `addons/peerova/bin/linux/libpeerova.so` (từ `lib/linux-x86_64/`) | `libpeerova.so` cạnh file chạy, hoặc trong `LD_LIBRARY_PATH`. |
| macOS | `addons/peerova/bin/macos/libpeerova.dylib` — liên hệ Peerova để nhận bản build này | `Contents/Frameworks/` (hoặc `Contents/MacOS/`) của app. |
| Android / iOS | Liên hệ Peerova để nhận bản build (`jniLibs/<abi>/libpeerova.so`, `libpeerova.a` tĩnh) | Export C# cho Android/iOS trên Godot 4 vẫn đang thử nghiệm; chưa được kiểm thử với Peerova. |

Game đã export không thể nạp thư viện nằm trong file `.pck`, vì vậy phải chép thư viện cạnh file chạy.
`PeerovaNode.ConfigureNativeLibrary()` tìm ở `res://addons/peerova/bin/<os>/` (editor), rồi thư mục chứa file chạy; nếu không có thì
dùng cơ chế tìm mặc định (`PATH`, `LD_LIBRARY_PATH`), hoặc biến môi trường `PEEROVA_LIB`.

### Cài đặt

1. Chép `addons/peerova` vào thư mục `addons/` của project.
2. Chép thư viện native vào `addons/peerova/bin/<os>/` như bảng trên.
3. Build project C# một lần (nút *Build*), rồi *Project Settings → Plugins → peerova → Enable*.
4. Thêm một **PeerovaNode** vào scene (nó là `[GlobalClass]`, nên có trong *Add Node*), hoặc tạo trong code.

### Bắt đầu nhanh (5 phút)

C#:
```csharp
var net = GetNode<Peerova.GodotAddon.PeerovaNode>("PeerovaNode");
net.HostStarted += ticket => GD.Print("TICKET ", ticket);        // đưa cho client
net.RoomCodeReady += code => GD.Print("CODE ", code);            // mã 6 ký tự (nếu đặt App)
net.Connected += peer => net.SendText(peer, "xin chào", 0, true);
net.Message += (peer, channel, data, reliable) => GD.Print(System.Text.Encoding.UTF8.GetString(data));
net.ConnectFailed += reason => GD.PushError(reason);
net.Host();                  // server
// net.Join(ticketOrCode);   // client: ticket hoặc mã 6 ký tự
```
GDScript (gọi qua node C#; tên signal/phương thức giữ nguyên như C#):
```gdscript
@onready var net = $PeerovaNode
func _ready():
    net.connect("HostStarted", func(ticket): print("TICKET ", ticket))
    net.connect("RoomCodeReady", func(code): print("CODE ", code))
    net.connect("Connected", func(peer): net.SendText(peer, "xin chào", 0, true))
    net.connect("Message", func(peer, channel, data: PackedByteArray, reliable): print(data.get_string_from_utf8()))
    net.connect("Disconnected", func(peer): print("rời đi ", peer))
    net.connect("ConnectFailed", func(reason): push_error(reason))
    net.Host()          # hoặc net.Join(ticket_or_code)
```
- `Host()` khởi động server trên luồng phụ (mất tới ~3 giây) rồi phát `HostStarted(ticket)`.
- Mã kết nối (connect code) chỉ có khi đặt `App` (project id trên website Peerova); nó đến sau vài giây qua `RoomCodeReady`, đọc lại bằng `GetRoomCode()`.
- `Join(ticketOrCode)` / `JoinWithToken(ticketOrCode, token)` nhận ticket **hoặc** mã 6 ký tự.

Kết nối là tự động: ưu tiên kết nối trực tiếp, và tự dùng một tuyến dự phòng được mã hoá khi mạng chặn kết nối trực tiếp.

### Gửi: reliable hay unreliable

`SendBytes(peer, bytes, channel, reliable)`, `SendText(peer, text, channel, reliable)`, `Broadcast(bytes, channel, reliable)` (host: tới mọi client; client: tới server),
`SendSpan(...)` (chỉ C#, không cấp phát). Các hàm gửi trả `false` khi lỗi.

| | `reliable = false` | `reliable = true` |
|---|---|---|
| Đảm bảo | Có thể mất hoặc đến sai thứ tự | Đến đủ, đúng thứ tự trong cùng một channel |
| Kích thước | Tối đa **1180 byte**; lớn hơn bị từ chối | Có thể lớn |
| Dùng cho | Input, vị trí, trạng thái realtime kiểu voice | Chat, inventory, RPC, các phần của file |

Channel là số 0..255; các channel khác nhau không chặn nhau.

### Nhận sự kiện

PeerovaNode poll trong `_Process` (tối đa `MaxEventsPerFrame` sự kiện/frame) và phát signal trên main thread:

| Signal | Khi nào |
|---|---|
| `HostStarted(ticket: String)` | Server đã chạy. |
| `RoomCodeReady(code: String)` | Đã có mã kết nối 6 ký tự. |
| `Connected(peer: int)` | Peer kết nối (host: client mới; client: server). |
| `Message(peer: int, channel: int, data: PackedByteArray, reliable: bool)` | Có message. |
| `Disconnected(peer: int)` | Peer ngắt kết nối. |
| `ConnectFailed(reason: String)` | Client không kết nối được, hoặc server không khởi động được. |

Các phương thức khác gọi được từ GDScript: `DisconnectPeer(peer)`, `Shutdown()`, `GetTicket()`, `GetRoomCode()`, `GetPeerCount()`, `GetPeers()`.

### Ngắt kết nối và dọn dẹp

- `DisconnectPeer(peer)` đóng một kết nối; `Shutdown()` dừng server/client và giải phóng node native.
- Node tự gọi `Shutdown()` trong `_ExitTree` (khi bị xoá khỏi scene hoặc khi thoát game).
- Nếu dùng API cấp thấp (`Peerova.Server` / `Peerova.Client`): gọi `PeerovaNode.ConfigureNativeLibrary()` trước, `Dispose()` khi xong, và không dispose khi luồng khác vẫn đang poll.

### Xử lý lỗi

Lỗi khởi động server và lỗi kết nối được phát qua `ConnectFailed(reason)` (lý do là nội dung `peerova_last_error()`); lỗi khởi động server cũng được ghi bằng `GD.PushError`.
Các hàm gửi trả `false` khi lỗi.

### Cấu hình (Inspector / thuộc tính)

| Thuộc tính | Ý nghĩa |
|---|---|
| `Port` | Cổng UDP (`0` = ngẫu nhiên; mặc định của server khi không cấu hình là 47000). |
| `License` | License `PVA-...`; trống = chế độ dev, tối đa 3 client. |
| `Upnp` | Thử mở cổng trên router qua UPnP (mặc định bật). |
| `Token` | Mật khẩu chung client phải gửi (`JoinWithToken`). |
| `MaxCcu` | Số client đồng thời tối đa (`0` = theo license). |
| `KeyFile` | Khoá định danh server (mặc định `user://peerova-server.key`). Cùng khoá ⇒ cùng ticket. |
| `App`, `ShareName`, `SharePublic`, `ShareMax`, `ShareMode`, `ShareRegion` | Mã kết nối: project id trên website và thông tin hiển thị. |
| `ConnectTimeoutMs` | Client bỏ cuộc sau bấy nhiêu ms (`0` = mặc định 20000). |
| `MaxEventsPerFrame` | Số sự kiện tối đa xử lý mỗi frame (`0` = không giới hạn). |

### Ví dụ

[`examples/net_demo/`](examples/net_demo) — script scene (`NetDemo.cs` cho C#, `net_demo.gd` cho GDScript): mỗi người chơi là một hình tròn,
vị trí gửi 20 lần/giây qua channel 1 (unreliable), chat qua channel 0 (reliable); host chuyển tiếp cho các client khác.
Cách chạy: tạo scene có gốc là `Node2D`, gắn một trong hai script, rồi chạy hai lần:
```sh
godot --path <project> res://net_demo.tscn -- server
godot --path <project> res://net_demo.tscn -- client <ticket|mã> --auto
```
Phím mũi tên di chuyển hình tròn của bạn; `--auto` cho nó tự chạy vòng tròn.

### GDExtension / MultiplayerPeer

Hiện addon cần bản .NET của Godot. Một GDExtension (C/C++, bọc `peerova.h`) cho bản Godot chuẩn và một `MultiplayerPeerExtension`
(để `@rpc` / `MultiplayerSynchronizer` chạy trên Peerova) chưa có sẵn.

### Bảo mật

- File key của server là danh tính của server: ai có nó có thể giả mạo server. Giữ bí mật, không commit, không đưa vào bản build cho client.
- Ticket là bán công khai: cứ đưa cho người chơi. `Token` và `License` là bí mật.
- Toàn bộ dữ liệu được mã hoá đầu-cuối.

### Khắc phục sự cố

- Chạy `peerova doctor` (công cụ dòng lệnh trong gói SDK) để chẩn đoán mạng.
- **`DllNotFoundException`**: thiếu thư viện ở `addons/peerova/bin/<os>/` (editor) hoặc cạnh file chạy (bản export); hoặc đặt `PEEROVA_LIB`.
- **Kết nối thất bại do hết thời gian**: host chưa chạy, ticket/mã sai, hoặc mạng chặn; chạy `peerova doctor` ở hai phía, tăng `ConnectTimeoutMs`.
- **Client thứ 4 bị từ chối**: chế độ dev chỉ nhận 3 client — nhập `License`.
- **Gửi unreliable trả false**: message > 1180 byte.

### Tình trạng kiểm thử

`compile-check/` biên dịch `addons/peerova/*.cs` và `examples/**/*.cs` với một stub của Godot API (gồm cả phần source generator của Godot sinh ra),
chạy một host + join `PeerovaNode` qua `_Process()`, rồi chạy `NetDemo` (host + client) và kiểm tra vị trí và chat:
`cd compile-check && dotnet run -c Release`. Chưa được chạy bên trong Godot trong quy trình kiểm thử tự động; `net_demo.gd` chưa được kiểm thử.

---

## English

`addons/peerova/` is a Godot 4 addon built on the C# binding (`addons/peerova/Peerova.cs` is a copy of `bindings/csharp/Peerova.cs`).
It provides the **PeerovaNode** node: it polls in `_process` and emits signals on the main thread, usable from both C# and GDScript.

### Requirements

- Godot **4.2+ .NET edition** (the standard build has no C#). The project needs a C# solution
  (*Project → Tools → C# → Create C# solution*). Your game code can be 100% GDScript.
- The Peerova native library (see the table below).

### Where to put the native library

| OS | In the editor | In exported games |
|---|---|---|
| Windows x64 | `addons/peerova/bin/windows/peerova.dll` (from `lib/windows-x86_64/`) | `peerova.dll` next to `game.exe`. `wintun.dll` is **not** needed (only the `peerova` command-line tool needs it, for its VPN mode). |
| Linux x64 | `addons/peerova/bin/linux/libpeerova.so` (from `lib/linux-x86_64/`) | `libpeerova.so` next to the executable, or on `LD_LIBRARY_PATH`. |
| macOS | `addons/peerova/bin/macos/libpeerova.dylib` — contact Peerova for this build | The app's `Contents/Frameworks/` (or `Contents/MacOS/`). |
| Android / iOS | Contact Peerova for these builds (`jniLibs/<abi>/libpeerova.so`, static `libpeerova.a`) | C# export to Android/iOS is experimental in Godot 4 and is untested with Peerova. |

Exported games can't load libraries from inside the `.pck`, so the library must sit next to the executable.
`PeerovaNode.ConfigureNativeLibrary()` looks in `res://addons/peerova/bin/<os>/` (editor), then the executable's folder; otherwise the
default search (`PATH`, `LD_LIBRARY_PATH`) applies, or set the `PEEROVA_LIB` environment variable.

### Install

1. Copy `addons/peerova` into your project's `addons/`.
2. Copy the native libraries into `addons/peerova/bin/<os>/` as above.
3. Build the C# project once (*Build* button), then *Project Settings → Plugins → peerova → Enable*.
4. Add a **PeerovaNode** to the scene (it is a `[GlobalClass]`, so it appears in *Add Node*), or create one in code.

### 5-minute quick start

C#:
```csharp
var net = GetNode<Peerova.GodotAddon.PeerovaNode>("PeerovaNode");
net.HostStarted += ticket => GD.Print("TICKET ", ticket);        // give to clients
net.RoomCodeReady += code => GD.Print("CODE ", code);            // 6-char code (if App is set)
net.Connected += peer => net.SendText(peer, "hello", 0, true);
net.Message += (peer, channel, data, reliable) => GD.Print(System.Text.Encoding.UTF8.GetString(data));
net.ConnectFailed += reason => GD.PushError(reason);
net.Host();                  // server
// net.Join(ticketOrCode);   // client: ticket or 6-character code
```
GDScript (through the C# node; signal and method names are the C# names):
```gdscript
@onready var net = $PeerovaNode
func _ready():
    net.connect("HostStarted", func(ticket): print("TICKET ", ticket))
    net.connect("RoomCodeReady", func(code): print("CODE ", code))
    net.connect("Connected", func(peer): net.SendText(peer, "hello", 0, true))
    net.connect("Message", func(peer, channel, data: PackedByteArray, reliable): print(data.get_string_from_utf8()))
    net.connect("Disconnected", func(peer): print("left ", peer))
    net.connect("ConnectFailed", func(reason): push_error(reason))
    net.Host()          # or net.Join(ticket_or_code)
```
- `Host()` starts the server on a worker thread (takes up to ~3 seconds), then emits `HostStarted(ticket)`.
- A connect code is only assigned when `App` (the project id from the Peerova website) is set; it arrives a few seconds later through `RoomCodeReady` and can be read back with `GetRoomCode()`.
- `Join(ticketOrCode)` / `JoinWithToken(ticketOrCode, token)` take a ticket **or** a 6-character code.

Connections are automatic: direct first, with an automatic encrypted fallback route when networks block direct connections.

### Sending: reliable vs unreliable

`SendBytes(peer, bytes, channel, reliable)`, `SendText(peer, text, channel, reliable)`, `Broadcast(bytes, channel, reliable)` (host: to all clients; client: to the server),
`SendSpan(...)` (C# only, allocation-free). Send methods return `false` on failure.

| | `reliable = false` | `reliable = true` |
|---|---|---|
| Guarantee | May be lost or reordered | Delivered, in order within the channel |
| Size | At most **1180 bytes**; larger sends are rejected | Can be large |
| Use for | Gameplay inputs, positions, voice-like realtime state | Chat, inventory, RPC, file chunks |

Channels are numbers 0..255; different channels don't block each other.

### Receiving events

PeerovaNode polls in `_Process` (up to `MaxEventsPerFrame` events per frame) and emits signals on the main thread:

| Signal | When |
|---|---|
| `HostStarted(ticket: String)` | The server is running. |
| `RoomCodeReady(code: String)` | The 6-character connect code is available. |
| `Connected(peer: int)` | A peer connected (host: a new client; client: the server). |
| `Message(peer: int, channel: int, data: PackedByteArray, reliable: bool)` | A message arrived. |
| `Disconnected(peer: int)` | A peer disconnected. |
| `ConnectFailed(reason: String)` | The client could not connect, or the server could not start. |

Other methods callable from GDScript: `DisconnectPeer(peer)`, `Shutdown()`, `GetTicket()`, `GetRoomCode()`, `GetPeerCount()`, `GetPeers()`.

### Disconnect and cleanup

- `DisconnectPeer(peer)` closes one connection; `Shutdown()` stops the server/client and frees the native node.
- The node calls `Shutdown()` itself in `_ExitTree` (when removed from the scene or when the game quits).
- With the low-level API (`Peerova.Server` / `Peerova.Client`): call `PeerovaNode.ConfigureNativeLibrary()` first, `Dispose()` when done, and never dispose while another thread is still polling.

### Error handling

Server start errors and connection errors are emitted as `ConnectFailed(reason)` (the reason is the text of `peerova_last_error()`); server start errors are also logged with `GD.PushError`.
Send methods return `false` on failure.

### Configuration (Inspector / properties)

| Property | Meaning |
|---|---|
| `Port` | UDP port (`0` = random; the server default without configuration is 47000). |
| `License` | `PVA-...` license; empty = dev mode, max 3 clients. |
| `Upnp` | Try to open the port on the router via UPnP (on by default). |
| `Token` | Shared secret clients must present (`JoinWithToken`). |
| `MaxCcu` | Maximum concurrent clients (`0` = license limit). |
| `KeyFile` | Server identity key (default `user://peerova-server.key`). Same key ⇒ same ticket. |
| `App`, `ShareName`, `SharePublic`, `ShareMax`, `ShareMode`, `ShareRegion` | Connect code: project id from the website and listing details. |
| `ConnectTimeoutMs` | The client gives up after this many ms (`0` = default 20000). |
| `MaxEventsPerFrame` | Maximum events handled per frame (`0` = unlimited). |

### Example

[`examples/net_demo/`](examples/net_demo) — a scene script (`NetDemo.cs` for C#, `net_demo.gd` for GDScript): every player is a circle,
positions go 20 times per second on channel 1 (unreliable), chat on channel 0 (reliable); the host forwards to the other clients.
How to run: create a scene with a `Node2D` root, attach one of the two scripts, and run it twice:
```sh
godot --path <project> res://net_demo.tscn -- server
godot --path <project> res://net_demo.tscn -- client <ticket|code> --auto
```
Arrow keys move your circle; `--auto` makes it move in a circle by itself.

### GDExtension / MultiplayerPeer

The addon currently requires the .NET edition of Godot. A GDExtension (C/C++, wrapping `peerova.h`) for the standard editor and a
`MultiplayerPeerExtension` (so `@rpc` / `MultiplayerSynchronizer` run over Peerova) are not available yet.

### Security

- The server key file is the server's identity: anyone who has it can impersonate the server. Keep it secret, never commit it, never ship it in client builds.
- The ticket is public-ish: share it with players. `Token` and `License` are secrets.
- All traffic is end-to-end encrypted.

### Troubleshooting

- Run `peerova doctor` (the command-line tool in the SDK package) for a network diagnosis.
- **`DllNotFoundException`**: the library is missing from `addons/peerova/bin/<os>/` (editor) or from next to the executable (exported game); or set `PEEROVA_LIB`.
- **Connect failed (timeout)**: the host is not running, the ticket/code is wrong, or the network blocks traffic; run `peerova doctor` on both sides and/or raise `ConnectTimeoutMs`.
- **The 4th client is rejected**: dev mode accepts only 3 clients — enter a `License`.
- **Unreliable send returns false**: the message is larger than 1180 bytes.

### Testing status

`compile-check/` compiles `addons/peerova/*.cs` and `examples/**/*.cs` against a stub of the Godot API (including what Godot's source generator emits),
runs a host + join `PeerovaNode` through `_Process()`, then runs `NetDemo` (host + client) and checks positions and chat:
`cd compile-check && dotnet run -c Release`. Not run inside Godot as part of automated testing; `net_demo.gd` is untested.
