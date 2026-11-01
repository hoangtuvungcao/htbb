# Peerova — C# / .NET

- [Tiếng Việt](#tiếng-việt)
- [English](#english)

Website: https://peerova.pages.dev · Khái niệm và C API đầy đủ / Concepts and the full C API: [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md)
Unity: [../unity](../unity) · Godot: [../godot](../godot) (cùng file `Peerova.cs` / same `Peerova.cs`).

---

## Tiếng Việt

Binding C# là một file duy nhất, `Peerova.cs`, bọc C API qua P/Invoke. Không cần `unsafe`.

### Chọn cách tích hợp

- **Game/app có sẵn, giữ nguyên code socket** (tình huống 2): app C# tự chạy `peerova client` ẩn rồi kết nối `127.0.0.1` —
  xem [Game có sẵn: chạy peerova ẩn](#game-có-sẵn-chạy-peerova-ẩn). Dùng cho .NET desktop/server (Windows, Linux, macOS).
- **Game mới hoặc host trong game** (tình huống 3/4): dùng SDK `Peerova.cs` bên dưới — message reliable/unreliable, sự kiện.
- Bảng đầy đủ 4 tình huống: [Chọn cách tích hợp](../DEVELOPER-GUIDE.md#1-chọn-cách-tích-hợp).

**Người chơi không phải gõ gì:** nhúng ticket vào game, hoặc tải ticket mới nhất từ URL của bạn khi khởi động; mã 6 ký tự chỉ
dùng để mời bạn bè vào phòng do người chơi mở. Chi tiết: [Người chơi không phải gõ gì](../DEVELOPER-GUIDE.md#2-người-chơi-không-phải-gõ-gì).

### Yêu cầu

- .NET 6 trở lên (console, server, Godot 4 .NET) hoặc .NET Standard 2.1 (Unity 2021.3+).
- Thư viện native Peerova cho nền tảng của bạn (trong gói SDK).

### Đặt thư viện native ở đâu

| Hệ điều hành | File | Đặt ở đâu |
|---|---|---|
| Windows x64 | `lib/windows-x86_64/peerova.dll` | Cạnh file `.exe` (thư mục output). SDK **không** cần `wintun.dll` (chỉ công cụ dòng lệnh `peerova` cần nó cho chế độ VPN). |
| Linux x64 | `lib/linux-x86_64/libpeerova.so` | Cạnh file chạy (thư mục output), hoặc trong `LD_LIBRARY_PATH`, hoặc chỉ đường dẫn bằng biến môi trường `PEEROVA_LIB`. |
| macOS | `libpeerova.dylib` | Liên hệ Peerova để nhận bản build này. Trong app bundle: `Contents/Frameworks/`. |
| iOS | `libpeerova.a` (tĩnh) | Liên hệ Peerova để nhận bản build này (dùng qua Unity, xem [../unity](../unity)). |
| Android | `libpeerova.so` | Liên hệ Peerova để nhận bản build này. Đặt vào `jniLibs/<abi>/libpeerova.so`. |

Trên .NET 6+, thư viện được tìm theo thứ tự: `Peerova.Native.LibraryPath` (gán trước lần gọi Peerova đầu tiên) → biến môi trường `PEEROVA_LIB` → thư mục của ứng dụng → đường dẫn tìm kiếm mặc định của hệ điều hành.

### Cài đặt

1. Chép `bindings/csharp/Peerova.cs` vào project (hoặc tham chiếu `Peerova.csproj`).
2. Cho thư viện native được chép vào thư mục output, ví dụ trong `.csproj`:
   ```xml
   <ItemGroup>
     <None Include="path/to/lib/linux-x86_64/libpeerova.so" CopyToOutputDirectory="PreserveNewest" Link="libpeerova.so" />
     <None Include="path/to/lib/windows-x86_64/peerova.dll" CopyToOutputDirectory="PreserveNewest" Link="peerova.dll" />
   </ItemGroup>
   ```

### Bắt đầu nhanh (5 phút)

Server:
```csharp
using Peerova;

using var server = Server.Start(new ServerConfig { Port = 47000, KeyFile = "server.key" });
Console.WriteLine("TICKET " + server.Ticket);       // đưa ticket này cho client
// Mã kết nối 6 ký tự: chỉ có khi cấu hình App + Share, và đến sau vài giây.
// server.RoomCode trả null cho tới khi có.
```
Lấy mã kết nối (connect code):
```csharp
using var server = Server.Start(new ServerConfig {
    App = "prj_...",                                        // project id trên website Peerova
    Share = new ShareConfig { Name = "Phòng của tôi", Public = true, Max = 16, Region = "VN" },
});
string code = null;
while (code == null) { Thread.Sleep(500); code = server.RoomCode; }
Console.WriteLine("CODE " + code);
```
Client (ticket **hoặc** mã 6 ký tự):
```csharp
using var client = Client.Connect(ticketOrCode, new ClientConfig { TimeoutMs = 20000 });
uint serverPeer = 0;
while (serverPeer == 0)
{
    if (!client.Poll(out var ev, 1000)) continue;
    if (ev.Type == EventType.Connected) serverPeer = ev.Peer;
    else if (ev.Type == EventType.ConnectFailed) throw new Exception(ev.Text);
}
client.Send(serverPeer, "xin chào", channel: 0, reliable: true);
```
Kết nối là tự động: ưu tiên kết nối trực tiếp; khi mạng chặn kết nối trực tiếp, Peerova tự dùng **đường mã hoá tự động** (vẫn mã hoá đầu-cuối).

### Gửi: reliable hay unreliable

```csharp
client.Send(peer, bytes, channel: 1, reliable: false);   // ném PeerovaException nếu lỗi
bool ok = client.TrySend(peer, bytes, 1, false);         // trả false nếu lỗi (lý do: Endpoint.LastError)
```

| | `reliable: false` | `reliable: true` |
|---|---|---|
| Đảm bảo | Có thể mất hoặc đến sai thứ tự | Đến đủ, đúng thứ tự trong cùng một channel |
| Kích thước | Tối đa **1180 byte** (`Library.MaxUnreliablePayload`); lớn hơn bị từ chối | Có thể lớn |
| Dùng cho | Input, vị trí, trạng thái realtime kiểu voice | Chat, inventory, RPC, các phần của file |

Channel là số 0..255. Các channel khác nhau không chặn nhau.

### Nhận sự kiện

`Poll(out Event ev)` không chờ; `Poll(out ev, timeoutMs)` chờ tối đa `timeoutMs`; `Poll(timeoutMs)` trả `Event?`.

| `ev.Type` | Ý nghĩa |
|---|---|
| `EventType.Connected` | Peer đã kết nối (server: client mới; client: server). `ev.Peer` là id để gửi. |
| `EventType.Message` | Message: `ev.Data` (byte[]), `ev.Text` (UTF-8), `ev.Channel`, `ev.Reliable`. |
| `EventType.Disconnected` | Peer đã ngắt kết nối. |
| `EventType.ConnectFailed` | Chỉ ở client: không kết nối được, `ev.Text` là lý do. |

`ev.Data` là bản copy managed, giữ lại thoải mái.

Mẫu vòng lặp:
- **Game loop**: mỗi frame gọi `while (node.Poll(out var ev)) Handle(ev);`.
- **Server/console**: một luồng riêng `while (running) if (node.Poll(out var ev, 50)) Handle(ev);`.
- **Không có vòng lặp sẵn**: `Task.Run` với vòng lặp như trên, rồi đẩy sự kiện về luồng UI của bạn.

### Luồng (thread)

Mọi phương thức đều an toàn khi gọi từ nhiều luồng (node có khoá nội bộ). Không `Dispose()` một node khi luồng khác vẫn đang poll/gửi trên nó — hãy dừng luồng đó trước.

### Ngắt kết nối và dọn dẹp

- `node.Disconnect(peer)` đóng một kết nối (sẽ có `Disconnected`).
- `node.Dispose()` (hoặc `using`) đóng mọi kết nối và giải phóng node native. Gọi phương thức sau khi đã dispose sẽ ném `ObjectDisposedException`.

### Xử lý lỗi

- `Server.Start` / `Client.Connect` / `Send` ném `PeerovaException`: `Code` là mã lỗi ổn định (`ErrorCode.TooLarge`, `UnknownPeer`, `CodeNotFound`…, bảng: [Mã lỗi](../DEVELOPER-GUIDE.md#mã-lỗi)); `Message` là văn bản tiếng Anh `"E_NAME: ..."`. Sau `TrySend` thất bại: `Endpoint.LastErrorCode`. Lý do `ConnectFailed`/`Disconnected`: `ev.ErrorCode`.
- `DllNotFoundException`: không tìm thấy thư viện native.
- Lỗi kết nối xảy ra sau đó đến dưới dạng sự kiện `ConnectFailed`.

### Cấu hình

`ServerConfig` (chỉ các trường được gán mới được gửi đi):

| Trường | JSON | Mặc định | Ý nghĩa |
|---|---|---|---|
| `Port` | `port` | `47000` | Cổng UDP; `0` = ngẫu nhiên. |
| `KeyFile` | `key_file` | `"server.key"` | Khoá định danh server. Cùng khoá ⇒ cùng ticket. |
| `License` | `license` | — | License `PVA-...`; không có = chế độ dev, tối đa 3 client. |
| `MaxCcu` | `max_ccu` | theo license | Số client đồng thời tối đa. |
| `Token` | `token` | — | Mật khẩu chung client phải gửi kèm. |
| `Upnp` | `upnp` | `true` | Thử mở cổng trên router qua UPnP. |
| `Private` | `private` | `false` | Ticket không chứa địa chỉ IP của server. |
| `HideIp` | `hide_ip` | `false` | Ẩn IP máy chủ: người chơi không bao giờ biết IP thật của máy chủ (chống DDoS); ping cao hơn một chút. Gói Pro/Studio/Enterprise và bản dùng thử. |
| `App` | `app` | — | Project id trên website Peerova, cần cho mã kết nối. |
| `Share` | `share` | — | `ShareConfig { Name, Public, Max, Mode, Region }`. |

`ClientConfig`: `Token` (`token`), `TimeoutMs` (`timeout_ms`, mặc định `20000`), `KeyFile` (`key_file`, tuỳ chọn: file danh tính client, ví dụ `<appdata>/<game>/peerova-client.key`, thư mục phải tồn tại — giữ file thì người chơi giữ cùng danh tính và cùng địa chỉ ổn định trên server; chặn theo địa chỉ chỉ có tác dụng khi họ còn giữ file, muốn chắc chắn dùng tài khoản/token). Cũng có thể truyền chuỗi JSON trực tiếp: `Server.Start("{\"port\":0}")`.

### Ví dụ

[`examples/ChatPositions`](examples/ChatPositions) — chat (reliable, channel 0) + vị trí 20 Hz (unreliable, channel 1):
```sh
cd examples/ChatPositions
dotnet run -c Release -- server                 # in ra "TICKET ..."
dotnet run -c Release -- client <ticket|mã> 10  # ở cửa sổ khác; gõ một dòng + Enter để chat
```
Project này tự chép `libpeerova.so` từ `lib/linux-x86_64` của gói SDK vào output; trên Windows hãy chép `peerova.dll` vào `bin/Release/net8.0/`.

### Game có sẵn: chạy peerova ẩn

Khi game/app đã có code socket TCP/UDP và người chơi **không được phải chạy gì**: phát hành `peerova.exe` (Windows) /
`peerova` (Linux, macOS) kèm app. App chạy `peerova client <TICKET> --forward tcp:8888 --key <appdata>/<game>/peerova-client.key`
ẩn (không cửa sổ console), đọc stdout/stderr (UTF-8) từng dòng, coi là **sẵn sàng** khi một dòng chứa chuỗi ASCII `server port`
(công cụ in `✔ ĐÃ KẾT NỐI …` rồi `✔ Tcp 127.0.0.1:8888 → server port 8888`, mỗi `--forward` một dòng), rồi code socket cũ kết nối
`127.0.0.1:8888`. Thất bại khi process thoát trước (mã thoát `1`, dòng stderr bắt đầu bằng `lỗi:`) hoặc quá 30 giây (khi đó process bị
dừng). Process bị dừng khi app thoát. Chi tiết: [Game tự chạy peerova ẩn](../DEVELOPER-GUIDE.md#4-game-tự-chạy-peerova-ẩn-người-chơi-không-chạy-gì).

```csharp
// PeerovaTunnel.cs — runs the bundled `peerova` CLI hidden and waits until the local port is open.
// Works in .NET Framework 4.7+, .NET 6+, Unity (Mono/IL2CPP standalone Windows/Linux/macOS).
using System;
using System.Diagnostics;
using System.IO;
using System.Text;
using System.Threading.Tasks;

public sealed class PeerovaTunnel : IDisposable
{
    Process _proc;
    /// Every output line (Vietnamese human-readable log). Called on a background thread.
    public event Action<string> Log;
    /// The tunnel closed (connection lost, server gone, or Stop()). Called on a background thread.
    public event Action Exited;

    /// exe: path to the bundled peerova(.exe); forward: e.g. "tcp:8888";
    /// keyFile: e.g. <appdata>/MyGame/peerova-client.key (keeps the player's identity).
    /// Returns true once the forwarded port accepts connections on 127.0.0.1.
    public Task<bool> StartAsync(string exe, string ticket, string forward, string keyFile, int timeoutMs = 30000)
    {
        Directory.CreateDirectory(Path.GetDirectoryName(keyFile));
        var psi = new ProcessStartInfo(exe)
        {
            Arguments = $"client {ticket} --forward {forward} --key \"{keyFile}\"",
            UseShellExecute = false,
            CreateNoWindow = true,                    // no console window
            RedirectStandardOutput = true,
            RedirectStandardError = true,
            StandardOutputEncoding = Encoding.UTF8,   // output is UTF-8
            StandardErrorEncoding = Encoding.UTF8,
        };
        var ready = new TaskCompletionSource<bool>();
        _proc = new Process { StartInfo = psi, EnableRaisingEvents = true };
        _proc.OutputDataReceived += (_, e) =>
        {
            if (e.Data == null) return;
            Log?.Invoke(e.Data);
            // "✔ Tcp 127.0.0.1:8888 → server port 8888" = connected and the local port is open
            if (e.Data.Contains("server port")) ready.TrySetResult(true);
        };
        _proc.ErrorDataReceived += (_, e) => { if (e.Data != null) Log?.Invoke(e.Data); };
        _proc.Exited += (_, __) => { ready.TrySetResult(false); Exited?.Invoke(); };
        _proc.Start();
        _proc.BeginOutputReadLine();
        _proc.BeginErrorReadLine();
        Task.Delay(timeoutMs).ContinueWith(_ => { if (ready.TrySetResult(false)) Stop(); });
        return ready.Task;
    }

    public bool IsRunning => _proc != null && !_proc.HasExited;

    public void Stop()
    {
        try { if (_proc != null && !_proc.HasExited) _proc.Kill(); } catch { /* already gone */ }
    }

    public void Dispose() { Stop(); _proc?.Dispose(); _proc = null; }
}
```
```csharp
// Console / WinForms / WPF app
using System.Runtime.InteropServices;

var tunnel = new PeerovaTunnel();
AppDomain.CurrentDomain.ProcessExit += (_, __) => tunnel.Dispose();
tunnel.Log += line => Console.WriteLine("[peerova] " + line);

string exe = Path.Combine(AppContext.BaseDirectory,
    RuntimeInformation.IsOSPlatform(OSPlatform.Windows) ? "peerova.exe" : "peerova");
string key = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData),
    "MyGame", "peerova-client.key");

if (await tunnel.StartAsync(exe, "pvt_...", "tcp:8888", key))
    ConnectToGameServer("127.0.0.1", 8888);      // your existing socket code, unchanged
else
    ShowError("Không kết nối được máy chủ / Could not reach the server");
```

- Dùng được trên .NET Framework 4.7+, .NET 6+ và Unity standalone (Windows/Linux/macOS) — xem [../unity](../unity).
  Mobile/console không chạy được process con: dùng SDK `Peerova.cs`.
- Linux/macOS: file `peerova` phải có quyền chạy (`chmod +x`). Thư mục chứa `--key` phải tồn tại (code trên tự tạo).
- Nếu cổng 8888 có thể đang bận trên máy người chơi, chọn cổng local khác: `tcp:38888=8888`.
- Tuỳ chọn khác của `peerova client`: `--reconnect` (tự kết nối lại), `--token SECRET`, `--bind IP`.
- `peerova stdio` **không** mở cổng local (nó là SDK message dạng JSON lines), nên không dùng cho cách này.

### Máy chủ game

Một process chia sẻ nhiều cổng (`--expose` lặp lại), tách Remote Desktop cho admin và game công khai thành các process riêng, mỗi người
chơi một địa chỉ `127.a.b.c` riêng + cấm bằng `--banned`, ẩn IP máy chủ (`--hide-ip` / `HideIp`), chế độ VPN:
[Tính năng máy chủ game](../DEVELOPER-GUIDE.md#7-tính-năng-máy-chủ-game).

### Bảo mật

- `server.key` là danh tính của server: ai có file này có thể giả mạo server. Giữ bí mật, không commit.
- Ticket không phải bí mật: cứ nhúng vào game hoặc đưa cho client. `token` và `license` là bí mật.
- Toàn bộ dữ liệu được mã hoá đầu-cuối.

### Khắc phục sự cố

- Chạy `peerova doctor` (công cụ dòng lệnh trong gói SDK) để chẩn đoán mạng.
- **`DllNotFoundException`**: thư viện không nằm cạnh ứng dụng; chép vào thư mục output, hoặc đặt `PEEROVA_LIB=/đường/dẫn/libpeerova.so`, hoặc gán `Peerova.Native.LibraryPath`.
- **`ConnectFailed` do hết thời gian**: server chưa chạy, ticket/mã sai, hoặc mạng chặn; chạy `peerova doctor` ở hai phía, tăng `TimeoutMs`.
- **Client thứ 4 bị từ chối**: chế độ dev (không license) chỉ nhận 3 client.
- **`Send(..., reliable: false)` ném lỗi**: message > 1180 byte — chia nhỏ hoặc gửi reliable.

### FAQ

- **Người chơi có phải gõ mã không?** Không. Nhúng ticket vào game hoặc tải từ URL của bạn; mã 6 ký tự chỉ để mời bạn bè.
- **Game có sẵn, không muốn đổi code mạng?** Chạy `peerova client` ẩn như phần [Game có sẵn: chạy peerova ẩn](#game-có-sẵn-chạy-peerova-ẩn).
- **Ticket có phải bí mật không?** Không; `server.key`, `license`, `token` mới là bí mật.
- **Kết nối không được?** Chạy `peerova doctor` trên cả hai máy. Thêm: [Khắc phục sự cố](../DEVELOPER-GUIDE.md#15-khắc-phục-sự-cố) · [FAQ](../DEVELOPER-GUIDE.md#16-faq).

---

## English

The C# binding is a single file, `Peerova.cs`, that wraps the C API through P/Invoke. No `unsafe` code required.

### Choosing an integration

- **Existing game/app, keep your socket code** (scenario 2): your C# app starts `peerova client` hidden, then connects to `127.0.0.1` —
  see [Existing game: run peerova hidden](#existing-game-run-peerova-hidden). For .NET desktop/server apps (Windows, Linux, macOS).
- **New game or host-in-game** (scenarios 3/4): use the `Peerova.cs` SDK below — reliable/unreliable messages, events.
- All 4 scenarios: [Choosing an integration](../DEVELOPER-GUIDE.md#1-choosing-an-integration).

**Players never type anything:** embed the ticket in the game, or fetch the latest ticket from your own URL at startup; 6-character
codes are only for inviting friends to player-hosted rooms. Details: [Players never type anything](../DEVELOPER-GUIDE.md#2-players-never-type-anything).

### Requirements

- .NET 6 or later (console, servers, Godot 4 .NET) or .NET Standard 2.1 (Unity 2021.3+).
- The Peerova native library for your platform (in the SDK package).

### Where to put the native library

| OS | File | Where it goes |
|---|---|---|
| Windows x64 | `lib/windows-x86_64/peerova.dll` | Next to the `.exe` (output folder). The SDK does **not** need `wintun.dll` (only the `peerova` command-line tool needs it, for its VPN mode). |
| Linux x64 | `lib/linux-x86_64/libpeerova.so` | Next to the executable (output folder), or on `LD_LIBRARY_PATH`, or point the `PEEROVA_LIB` environment variable at it. |
| macOS | `libpeerova.dylib` | Contact Peerova for this build. In an app bundle: `Contents/Frameworks/`. |
| iOS | `libpeerova.a` (static) | Contact Peerova for this build (used through Unity, see [../unity](../unity)). |
| Android | `libpeerova.so` | Contact Peerova for this build. Put it in `jniLibs/<abi>/libpeerova.so`. |

On .NET 6+ the library is looked up in this order: `Peerova.Native.LibraryPath` (set it before the first Peerova call) → the `PEEROVA_LIB` environment variable → the application directory → the OS default search path.

### Install

1. Copy `bindings/csharp/Peerova.cs` into your project (or reference `Peerova.csproj`).
2. Make the native library land in the output folder, for example in your `.csproj`:
   ```xml
   <ItemGroup>
     <None Include="path/to/lib/linux-x86_64/libpeerova.so" CopyToOutputDirectory="PreserveNewest" Link="libpeerova.so" />
     <None Include="path/to/lib/windows-x86_64/peerova.dll" CopyToOutputDirectory="PreserveNewest" Link="peerova.dll" />
   </ItemGroup>
   ```

### 5-minute quick start

Server:
```csharp
using Peerova;

using var server = Server.Start(new ServerConfig { Port = 47000, KeyFile = "server.key" });
Console.WriteLine("TICKET " + server.Ticket);       // give this ticket to clients
// 6-character connect code: only with App + Share config, and it arrives after a few seconds.
// server.RoomCode returns null until then.
```
Getting a connect code:
```csharp
using var server = Server.Start(new ServerConfig {
    App = "prj_...",                                        // project id from the Peerova website
    Share = new ShareConfig { Name = "My room", Public = true, Max = 16, Region = "VN" },
});
string code = null;
while (code == null) { Thread.Sleep(500); code = server.RoomCode; }
Console.WriteLine("CODE " + code);
```
Client (ticket **or** 6-character code):
```csharp
using var client = Client.Connect(ticketOrCode, new ClientConfig { TimeoutMs = 20000 });
uint serverPeer = 0;
while (serverPeer == 0)
{
    if (!client.Poll(out var ev, 1000)) continue;
    if (ev.Type == EventType.Connected) serverPeer = ev.Peer;
    else if (ev.Type == EventType.ConnectFailed) throw new Exception(ev.Text);
}
client.Send(serverPeer, "hello", channel: 0, reliable: true);
```
Connections are automatic: direct first; when a network blocks direct connections Peerova uses the **automatic encrypted route** (still end-to-end encrypted).

### Sending: reliable vs unreliable

```csharp
client.Send(peer, bytes, channel: 1, reliable: false);   // throws PeerovaException on failure
bool ok = client.TrySend(peer, bytes, 1, false);         // returns false on failure (reason: Endpoint.LastError)
```

| | `reliable: false` | `reliable: true` |
|---|---|---|
| Guarantee | May be lost or reordered | Delivered, in order within the channel |
| Size | At most **1180 bytes** (`Library.MaxUnreliablePayload`); larger sends are rejected | Can be large |
| Use for | Gameplay inputs, positions, voice-like realtime state | Chat, inventory, RPC, file chunks |

Channels are numbers 0..255. Different channels don't block each other.

### Receiving events

`Poll(out Event ev)` never waits; `Poll(out ev, timeoutMs)` waits up to `timeoutMs`; `Poll(timeoutMs)` returns an `Event?`.

| `ev.Type` | Meaning |
|---|---|
| `EventType.Connected` | A peer connected (server: a new client; client: the server). `ev.Peer` is the id to send to. |
| `EventType.Message` | A message: `ev.Data` (byte[]), `ev.Text` (UTF-8), `ev.Channel`, `ev.Reliable`. |
| `EventType.Disconnected` | A peer disconnected. |
| `EventType.ConnectFailed` | Client only: could not connect; `ev.Text` is the reason. |

`ev.Data` is a managed copy; keep it as long as you like.

Loop patterns:
- **Game loop**: every frame, `while (node.Poll(out var ev)) Handle(ev);`.
- **Server/console**: a dedicated thread, `while (running) if (node.Poll(out var ev, 50)) Handle(ev);`.
- **No existing loop**: `Task.Run` with the loop above, then marshal events to your UI thread.

### Threading

All methods are safe to call from several threads (the node has an internal lock). Do not `Dispose()` a node while another thread is still polling or sending on it — stop that thread first.

### Disconnect and cleanup

- `node.Disconnect(peer)` closes one connection (a `Disconnected` event follows).
- `node.Dispose()` (or `using`) closes all connections and frees the native node. Calling a method after dispose throws `ObjectDisposedException`.

### Error handling

- `Server.Start` / `Client.Connect` / `Send` throw `PeerovaException`: `Code` is a stable error code (`ErrorCode.TooLarge`, `UnknownPeer`, `CodeNotFound`, ...; table: [Error codes](../DEVELOPER-GUIDE.md#error-codes)); `Message` is English text `"E_NAME: ..."`. After a failed `TrySend`: `Endpoint.LastErrorCode`. `ConnectFailed`/`Disconnected` reasons: `ev.ErrorCode`.
- `DllNotFoundException`: the native library was not found.
- Connection errors that happen later arrive as `ConnectFailed` events.

### Configuration

`ServerConfig` (only fields that are set are sent):

| Field | JSON | Default | Meaning |
|---|---|---|---|
| `Port` | `port` | `47000` | UDP port; `0` = random. |
| `KeyFile` | `key_file` | `"server.key"` | Server identity key. Same key ⇒ same ticket. |
| `License` | `license` | — | `PVA-...` license; none = dev mode, max 3 clients. |
| `MaxCcu` | `max_ccu` | from license | Maximum concurrent clients. |
| `Token` | `token` | — | Shared secret clients must present. |
| `Upnp` | `upnp` | `true` | Try to open the port on the router via UPnP. |
| `Private` | `private` | `false` | The ticket does not contain the server's IP addresses. |
| `HideIp` | `hide_ip` | `false` | Hide the server IP: players never learn its real address (DDoS protection); slightly higher latency. Pro/Studio/Enterprise plans and the trial. |
| `App` | `app` | — | Project id from the Peerova website, needed for a connect code. |
| `Share` | `share` | — | `ShareConfig { Name, Public, Max, Mode, Region }`. |

`ClientConfig`: `Token` (`token`), `TimeoutMs` (`timeout_ms`, default `20000`), `KeyFile` (`key_file`, optional: client identity file, e.g. `<appdata>/<game>/peerova-client.key`, the folder must exist — keeping it keeps the player's identity and stable address on the server; banning by address works per player only while they keep it, accounts/tokens are the robust way). You can also pass a JSON string directly: `Server.Start("{\"port\":0}")`.

### Example

[`examples/ChatPositions`](examples/ChatPositions) — chat (reliable, channel 0) + positions at 20 Hz (unreliable, channel 1):
```sh
cd examples/ChatPositions
dotnet run -c Release -- server                    # prints "TICKET ..."
dotnet run -c Release -- client <ticket|code> 10   # in another terminal; type a line + Enter to chat
```
The project copies `libpeerova.so` from the SDK package's `lib/linux-x86_64` into its output; on Windows copy `peerova.dll` into `bin/Release/net8.0/`.

### Existing game: run peerova hidden

When the game/app already has TCP/UDP socket code and players **must not run anything**: ship `peerova.exe` (Windows) /
`peerova` (Linux, macOS) with the app. The app starts `peerova client <TICKET> --forward tcp:8888 --key <appdata>/<game>/peerova-client.key`
hidden (no console window), reads stdout/stderr (UTF-8) line by line, treats it as **ready** when a line contains the ASCII text
`server port` (the tool prints `✔ ĐÃ KẾT NỐI …` ("connected") then `✔ Tcp 127.0.0.1:8888 → server port 8888`, once per `--forward`),
then the existing socket code connects to `127.0.0.1:8888`. It fails if the process exits first (exit code `1`, stderr line starting with
`lỗi:` = "error:") or after 30 seconds (the process is then killed). The process is killed when the app exits.
Details: [The game runs peerova hidden](../DEVELOPER-GUIDE.md#4-the-game-runs-peerova-hidden-players-run-nothing).

```csharp
// PeerovaTunnel.cs — runs the bundled `peerova` CLI hidden and waits until the local port is open.
// Works in .NET Framework 4.7+, .NET 6+, Unity (Mono/IL2CPP standalone Windows/Linux/macOS).
using System;
using System.Diagnostics;
using System.IO;
using System.Text;
using System.Threading.Tasks;

public sealed class PeerovaTunnel : IDisposable
{
    Process _proc;
    /// Every output line (Vietnamese human-readable log). Called on a background thread.
    public event Action<string> Log;
    /// The tunnel closed (connection lost, server gone, or Stop()). Called on a background thread.
    public event Action Exited;

    /// exe: path to the bundled peerova(.exe); forward: e.g. "tcp:8888";
    /// keyFile: e.g. <appdata>/MyGame/peerova-client.key (keeps the player's identity).
    /// Returns true once the forwarded port accepts connections on 127.0.0.1.
    public Task<bool> StartAsync(string exe, string ticket, string forward, string keyFile, int timeoutMs = 30000)
    {
        Directory.CreateDirectory(Path.GetDirectoryName(keyFile));
        var psi = new ProcessStartInfo(exe)
        {
            Arguments = $"client {ticket} --forward {forward} --key \"{keyFile}\"",
            UseShellExecute = false,
            CreateNoWindow = true,                    // no console window
            RedirectStandardOutput = true,
            RedirectStandardError = true,
            StandardOutputEncoding = Encoding.UTF8,   // output is UTF-8
            StandardErrorEncoding = Encoding.UTF8,
        };
        var ready = new TaskCompletionSource<bool>();
        _proc = new Process { StartInfo = psi, EnableRaisingEvents = true };
        _proc.OutputDataReceived += (_, e) =>
        {
            if (e.Data == null) return;
            Log?.Invoke(e.Data);
            // "✔ Tcp 127.0.0.1:8888 → server port 8888" = connected and the local port is open
            if (e.Data.Contains("server port")) ready.TrySetResult(true);
        };
        _proc.ErrorDataReceived += (_, e) => { if (e.Data != null) Log?.Invoke(e.Data); };
        _proc.Exited += (_, __) => { ready.TrySetResult(false); Exited?.Invoke(); };
        _proc.Start();
        _proc.BeginOutputReadLine();
        _proc.BeginErrorReadLine();
        Task.Delay(timeoutMs).ContinueWith(_ => { if (ready.TrySetResult(false)) Stop(); });
        return ready.Task;
    }

    public bool IsRunning => _proc != null && !_proc.HasExited;

    public void Stop()
    {
        try { if (_proc != null && !_proc.HasExited) _proc.Kill(); } catch { /* already gone */ }
    }

    public void Dispose() { Stop(); _proc?.Dispose(); _proc = null; }
}
```
```csharp
// Console / WinForms / WPF app
using System.Runtime.InteropServices;

var tunnel = new PeerovaTunnel();
AppDomain.CurrentDomain.ProcessExit += (_, __) => tunnel.Dispose();
tunnel.Log += line => Console.WriteLine("[peerova] " + line);

string exe = Path.Combine(AppContext.BaseDirectory,
    RuntimeInformation.IsOSPlatform(OSPlatform.Windows) ? "peerova.exe" : "peerova");
string key = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData),
    "MyGame", "peerova-client.key");

if (await tunnel.StartAsync(exe, "pvt_...", "tcp:8888", key))
    ConnectToGameServer("127.0.0.1", 8888);      // your existing socket code, unchanged
else
    ShowError("Không kết nối được máy chủ / Could not reach the server");
```

- Works on .NET Framework 4.7+, .NET 6+ and Unity standalone (Windows/Linux/macOS) — see [../unity](../unity).
  Mobile/consoles can't run child processes: use the `Peerova.cs` SDK.
- Linux/macOS: the `peerova` file must be executable (`chmod +x`). The `--key` folder must exist (the code above creates it).
- If port 8888 may be in use on the player's machine, pick another local port: `tcp:38888=8888`.
- Other `peerova client` options: `--reconnect` (reconnect automatically), `--token SECRET`, `--bind IP`.
- `peerova stdio` does **not** open local ports (it is the message SDK as JSON lines), so it doesn't fit this pattern.

### Game servers

Several ports from one process (repeat `--expose`), Remote Desktop for admins and the public game as separate processes, a separate
`127.a.b.c` address per player + bans with `--banned`, hiding the server IP (`--hide-ip` / `HideIp`), VPN mode:
[Game-server features](../DEVELOPER-GUIDE.md#7-game-server-features).

### Security

- `server.key` is the server's identity: anyone who has it can impersonate the server. Keep it secret and never commit it.
- The ticket is not a secret: embed it in the game or share it with clients. `token` and `license` are secrets.
- All traffic is end-to-end encrypted.

### Troubleshooting

- Run `peerova doctor` (the command-line tool in the SDK package) for a network diagnosis.
- **`DllNotFoundException`**: the library is not next to the application; copy it to the output folder, or set `PEEROVA_LIB=/path/to/libpeerova.so`, or assign `Peerova.Native.LibraryPath`.
- **`ConnectFailed` after a timeout**: the server is not running, the ticket/code is wrong, or the network blocks traffic; run `peerova doctor` on both sides and/or raise `TimeoutMs`.
- **The 4th client is rejected**: dev mode (no license) accepts only 3 clients.
- **`Send(..., reliable: false)` throws**: the message is larger than 1180 bytes — split it or send it reliably.

### FAQ

- **Do players need to type a code?** No. Embed the ticket in the game or fetch it from your URL; 6-character codes are only for inviting friends.
- **Existing game, don't want to change the networking code?** Run `peerova client` hidden as in [Existing game: run peerova hidden](#existing-game-run-peerova-hidden).
- **Is the ticket a secret?** No; `server.key`, `license` and `token` are.
- **Can't connect?** Run `peerova doctor` on both machines. More: [Troubleshooting](../DEVELOPER-GUIDE.md#15-troubleshooting) · [FAQ](../DEVELOPER-GUIDE.md#16-faq-1).
