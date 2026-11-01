# Peerova — Unity

- [Tiếng Việt](#tiếng-việt)
- [English](#english)

Website: https://peerova.pages.dev · Khái niệm và C API đầy đủ / Concepts and the full C API: [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md)

---

## Tiếng Việt

Gói Unity nằm trong [`com.peerova.sdk/`](com.peerova.sdk). Hướng dẫn đầy đủ (cài đặt, đặt thư viện native,
bắt đầu nhanh, reliable/unreliable, sự kiện, cấu hình, bảo mật, khắc phục sự cố) ở
[`com.peerova.sdk/README.md`](com.peerova.sdk/README.md). Ví dụ nằm trong `com.peerova.sdk/Samples~/` theo quy ước của Unity
(**Chat** và **Position Sync**), import qua *Package Manager → Peerova SDK → Samples*.
Thư viện native: [`com.peerova.sdk/Plugins/README.md`](com.peerova.sdk/Plugins/README.md).
`com.peerova.sdk/Runtime/Peerova.cs` là bản sao của [`../csharp/Peerova.cs`](../csharp/Peerova.cs) (tham chiếu API: [../csharp/README.md](../csharp/README.md)).

### Chọn cách tích hợp

- **Game Unity có sẵn, giữ nguyên networking hiện tại** (Netcode, Mirror, Fish-Net, socket riêng… — tình huống 2): game tự chạy
  `peerova client` ẩn rồi transport kết nối `127.0.0.1`. **Chỉ bản standalone Windows/Linux/macOS** — xem
  [Game có sẵn: chạy peerova ẩn](#game-có-sẵn-chạy-peerova-ẩn).
- **Game mới hoặc host trong game** (tình huống 3/4): dùng SDK — component `PeerovaBehaviour`, chạy trên mọi nền tảng có thư viện
  native (gồm Android/iOS) — xem [Dùng SDK](#dùng-sdk-peerovabehaviour).
- Bảng đầy đủ 4 tình huống: [Chọn cách tích hợp](../DEVELOPER-GUIDE.md#1-chọn-cách-tích-hợp).

**Người chơi không phải gõ gì:** nhúng ticket vào game (hằng số hoặc file trong `StreamingAssets`), hoặc tải ticket mới nhất từ URL
của bạn khi game khởi động (dùng ticket nhúng nếu không tải được); mã 6 ký tự chỉ dùng để mời bạn bè vào phòng do người chơi mở.
Chi tiết: [Người chơi không phải gõ gì](../DEVELOPER-GUIDE.md#2-người-chơi-không-phải-gõ-gì).

### Game có sẵn: chạy peerova ẩn

Game chạy `peerova client <TICKET> --forward tcp:8888 --key <persistentDataPath>/peerova-client.key` ẩn (không cửa sổ console),
đọc stdout/stderr (UTF-8) từng dòng, coi là **sẵn sàng** khi một dòng chứa chuỗi ASCII `server port` (công cụ in `✔ ĐÃ KẾT NỐI …`
rồi `✔ Tcp 127.0.0.1:8888 → server port 8888`, mỗi `--forward` một dòng), rồi networking hiện có kết nối `127.0.0.1:8888`.
Thất bại khi process thoát trước (mã thoát `1`, dòng stderr bắt đầu bằng `lỗi:`) hoặc quá 30 giây (process bị dừng). Process bị dừng
khi game thoát (`OnApplicationQuit`).

1. Chép công cụ dòng lệnh từ gói SDK vào `Assets/StreamingAssets/Peerova/`: `lib/windows-x86_64/peerova.exe` (Windows),
   `lib/linux-x86_64/peerova` (Linux; file phải có quyền chạy, `chmod +x`). macOS: liên hệ Peerova để nhận bản build, và ký (codesign) cùng app.
2. Đặt `PeerovaTunnel.cs` (class bên dưới, giống hệt bản C#) vào `Assets/Scripts/`.
3. Thêm `GameConnection.cs` vào một GameObject trong scene đầu tiên.

`PeerovaTunnel.cs`:
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

`GameConnection.cs`:
```csharp
// GameConnection.cs — Unity standalone (Windows/Linux/macOS). Put the CLI in
// Assets/StreamingAssets/Peerova/peerova.exe (Windows) or .../peerova (Linux/macOS, chmod +x).
using System.IO;
using UnityEngine;

public class GameConnection : MonoBehaviour
{
    const string Ticket = "pvt_...";        // or fetch it from your URL at startup
    readonly PeerovaTunnel tunnel = new PeerovaTunnel();

    async void Start()
    {
        bool win = Application.platform == RuntimePlatform.WindowsPlayer
                || Application.platform == RuntimePlatform.WindowsEditor;
        string exe = Path.Combine(Application.streamingAssetsPath, "Peerova", win ? "peerova.exe" : "peerova");
        string key = Path.Combine(Application.persistentDataPath, "peerova-client.key");
        tunnel.Log += line => Debug.Log("[peerova] " + line);     // Debug.Log is thread-safe

        bool ok = await tunnel.StartAsync(exe, Ticket, "tcp:8888", key);
        // `await` resumes on the Unity main thread here.
        if (ok) ConnectMyNetworking("127.0.0.1", 8888);   // e.g. transport.SetConnectionData("127.0.0.1", 8888)
        else Debug.LogError("Peerova: could not reach the server");
    }

    void OnApplicationQuit() => tunnel.Dispose();

    void ConnectMyNetworking(string host, ushort port) { /* your existing client code */ }
}
```

- **Mobile/console không chạy được process con** — trên Android, iOS, console hãy dùng SDK (`PeerovaBehaviour`).
- Nếu cổng 8888 có thể đang bận trên máy người chơi, chọn cổng local khác: `tcp:38888=8888` (rồi transport kết nối 38888).
- Tuỳ chọn khác của `peerova client`: `--reconnect`, `--token SECRET`. `peerova stdio` **không** mở cổng local (nó là SDK message
  dạng JSON lines), nên không dùng cho cách này.
- Chi tiết: [Game tự chạy peerova ẩn](../DEVELOPER-GUIDE.md#4-game-tự-chạy-peerova-ẩn-người-chơi-không-chạy-gì).

### File khoá (`key_file`)

Lưu mọi file khoá trong `Application.persistentDataPath` (thư mục ghi được trên mọi nền tảng, giữ nguyên qua các lần chạy và cập nhật game):

- **Người chơi (client):** `Path.Combine(Application.persistentDataPath, "peerova-client.key")` — dùng cho `--key` (như trên) hoặc
  `ClientConfig.KeyFile` khi gọi thẳng `Peerova.Client.Connect(ticket, new ClientConfig { KeyFile = … })`. Giữ file này thì người chơi
  giữ cùng danh tính và cùng địa chỉ ổn định `127.a.b.c` trên máy chủ; cấm theo địa chỉ chỉ có tác dụng khi họ còn giữ file (muốn chắc chắn:
  tài khoản/token). `PeerovaBehaviour.Join()` không đặt file khoá client — cần danh tính ổn định thì dùng `Client.Connect` như trên.
- **Host (server):** trường `keyFile` của `PeerovaBehaviour` (mặc định `peerova-server.key`) là đường dẫn **tương đối với**
  `Application.persistentDataPath`. Cùng file ⇒ cùng ticket. Đây là bí mật — đừng đưa vào `StreamingAssets` hay build.

### Dùng SDK (`PeerovaBehaviour`)

Component `Peerova.Unity.PeerovaBehaviour` poll trong `Update()` và bắn UnityEvent trên main thread: `OnHostStarted(ticket)`,
`OnRoomCode(code)`, `OnConnected(peer)`, `OnMessage(peer, channel, data, reliable)`, `OnDisconnected(peer)`, `OnConnectFailed(reason)`,
và event C# `EventReceived` (struct `Peerova.Event` đầy đủ). Phương thức: `Host()` (khởi động server trên luồng phụ), `Join(ticketOrCode, token)`,
`Send(peer, data, channel, reliable)`, `Broadcast(data, channel, reliable)`, `Disconnect(peer)`, `Shutdown()`; thuộc tính `Ticket`, `RoomCode`,
`Peers`, `IsRunning`, `CurrentRole`. Cấu hình trong Inspector (`port`, `license`, `token`, `maxCcu`, `keyFile`, `app`, `shareName`…).

```csharp
using Peerova.Unity;
using UnityEngine;

public class Net : MonoBehaviour
{
    PeerovaBehaviour net;

    void Start()
    {
        net = gameObject.AddComponent<PeerovaBehaviour>();
        net.OnHostStarted.AddListener(ticket => Debug.Log("TICKET " + ticket));
        net.OnConnected.AddListener(peer => net.Send(peer, System.Text.Encoding.UTF8.GetBytes("hello")));
        net.OnMessage.AddListener((peer, channel, data, reliable) => Debug.Log($"{peer}: {data.Length} B"));
        net.OnConnectFailed.AddListener(reason =>
            Debug.LogError($"{Peerova.ErrorCodes.FromText(reason)}: {reason}"));   // e.g. Timeout, CodeNotFound
    }

    public void HostGame() => net.Host();                                   // server, started off the main thread
    public void JoinGame(string ticketOrCode) => net.Join(ticketOrCode);    // ticket or 6-character code
}
```

Hướng dẫn đầy đủ: [`com.peerova.sdk/README.md`](com.peerova.sdk/README.md). Host trong game (mã 6 ký tự, danh sách phòng công khai):
[Host trong game](../DEVELOPER-GUIDE.md#6-host-trong-game-một-người-chơi-mở-phòng).

### Mã lỗi

Lý do của `OnConnectFailed` là văn bản tiếng Anh `"E_NAME: ..."`; đổi sang mã ổn định bằng
`Peerova.ErrorCodes.FromText(reason)` (enum `Peerova.ErrorCode`: `Timeout`, `CodeNotFound`, `CcuLimit`…). Lý do ngắt kết nối
(`Disconnected`) và thất bại: từ event C# `EventReceived`, `ev.ErrorCode`. Sau `Send` trả `false`: `Peerova.Endpoint.LastErrorCode`. Bảng mã: [Mã lỗi](../DEVELOPER-GUIDE.md#mã-lỗi).

### Máy chủ game

Máy chủ riêng (dedicated) cho game Unity: chạy `peerova server --expose …` cạnh bản server build, hoặc dùng SDK trong bản server.
Nhiều cổng, mỗi người chơi một địa chỉ riêng + cấm người chơi, ẩn IP máy chủ, chế độ VPN:
[Tính năng máy chủ game](../DEVELOPER-GUIDE.md#7-tính-năng-máy-chủ-game).

### FAQ

- **Người chơi có phải gõ mã không?** Không. Nhúng ticket hoặc tải từ URL của bạn; mã 6 ký tự chỉ để mời bạn bè.
- **Chạy `peerova` ẩn trên Android/iOS được không?** Không — mobile không chạy được process con; dùng `PeerovaBehaviour`.
- **Có cần `wintun.dll`?** Không, cả SDK lẫn `peerova client --forward` đều không cần.
- **Kết nối không được?** Chạy `peerova doctor` trên cả hai máy. Thêm: [Khắc phục sự cố](../DEVELOPER-GUIDE.md#15-khắc-phục-sự-cố) · [FAQ](../DEVELOPER-GUIDE.md#16-faq).

---

## English

The Unity package lives in [`com.peerova.sdk/`](com.peerova.sdk). The full guide (install, native library
placement, quick start, reliable/unreliable, events, configuration, security, troubleshooting) is
[`com.peerova.sdk/README.md`](com.peerova.sdk/README.md). Examples live in `com.peerova.sdk/Samples~/` following the Unity
convention (**Chat** and **Position Sync**); import them via *Package Manager → Peerova SDK → Samples*.
Native libraries: [`com.peerova.sdk/Plugins/README.md`](com.peerova.sdk/Plugins/README.md).
`com.peerova.sdk/Runtime/Peerova.cs` is a copy of [`../csharp/Peerova.cs`](../csharp/Peerova.cs) (API reference: [../csharp/README.md](../csharp/README.md)).

### Choosing an integration

- **Existing Unity game, keep its current networking** (Netcode, Mirror, Fish-Net, your own sockets… — scenario 2): the game starts
  `peerova client` hidden and the transport connects to `127.0.0.1`. **Standalone Windows/Linux/macOS builds only** — see
  [Existing game: run peerova hidden](#existing-game-run-peerova-hidden).
- **New game or host-in-game** (scenarios 3/4): use the SDK — the `PeerovaBehaviour` component, on every platform with a native library
  (Android/iOS included) — see [Using the SDK](#using-the-sdk-peerovabehaviour).
- All 4 scenarios: [Choosing an integration](../DEVELOPER-GUIDE.md#1-choosing-an-integration).

**Players never type anything:** embed the ticket in the game (a constant or a file in `StreamingAssets`), or fetch the latest ticket
from your own URL at startup (falling back to the embedded one); 6-character codes are only for inviting friends to player-hosted rooms.
Details: [Players never type anything](../DEVELOPER-GUIDE.md#2-players-never-type-anything).

### Existing game: run peerova hidden

The game starts `peerova client <TICKET> --forward tcp:8888 --key <persistentDataPath>/peerova-client.key` hidden (no console window),
reads stdout/stderr (UTF-8) line by line, treats it as **ready** when a line contains the ASCII text `server port` (the tool prints
`✔ ĐÃ KẾT NỐI …` ("connected") then `✔ Tcp 127.0.0.1:8888 → server port 8888`, once per `--forward`), then the existing networking
connects to `127.0.0.1:8888`. It fails if the process exits first (exit code `1`, stderr line starting with `lỗi:` = "error:") or after
30 seconds (the process is killed). The process is killed when the game exits (`OnApplicationQuit`).

1. Copy the command-line tool from the SDK package into `Assets/StreamingAssets/Peerova/`: `lib/windows-x86_64/peerova.exe` (Windows),
   `lib/linux-x86_64/peerova` (Linux; the file must be executable, `chmod +x`). macOS: contact Peerova for the build, and codesign it with the app.
2. Put `PeerovaTunnel.cs` (the class below, identical to the C# version) in `Assets/Scripts/`.
3. Add `GameConnection.cs` to a GameObject in your first scene.

`PeerovaTunnel.cs`:
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

`GameConnection.cs`:
```csharp
// GameConnection.cs — Unity standalone (Windows/Linux/macOS). Put the CLI in
// Assets/StreamingAssets/Peerova/peerova.exe (Windows) or .../peerova (Linux/macOS, chmod +x).
using System.IO;
using UnityEngine;

public class GameConnection : MonoBehaviour
{
    const string Ticket = "pvt_...";        // or fetch it from your URL at startup
    readonly PeerovaTunnel tunnel = new PeerovaTunnel();

    async void Start()
    {
        bool win = Application.platform == RuntimePlatform.WindowsPlayer
                || Application.platform == RuntimePlatform.WindowsEditor;
        string exe = Path.Combine(Application.streamingAssetsPath, "Peerova", win ? "peerova.exe" : "peerova");
        string key = Path.Combine(Application.persistentDataPath, "peerova-client.key");
        tunnel.Log += line => Debug.Log("[peerova] " + line);     // Debug.Log is thread-safe

        bool ok = await tunnel.StartAsync(exe, Ticket, "tcp:8888", key);
        // `await` resumes on the Unity main thread here.
        if (ok) ConnectMyNetworking("127.0.0.1", 8888);   // e.g. transport.SetConnectionData("127.0.0.1", 8888)
        else Debug.LogError("Peerova: could not reach the server");
    }

    void OnApplicationQuit() => tunnel.Dispose();

    void ConnectMyNetworking(string host, ushort port) { /* your existing client code */ }
}
```

- **Mobile/consoles can't run child processes** — on Android, iOS and consoles use the SDK (`PeerovaBehaviour`).
- If port 8888 may be in use on the player's machine, pick another local port: `tcp:38888=8888` (and point the transport at 38888).
- Other `peerova client` options: `--reconnect`, `--token SECRET`. `peerova stdio` does **not** open local ports (it is the message SDK
  as JSON lines), so it doesn't fit this pattern.
- Details: [The game runs peerova hidden](../DEVELOPER-GUIDE.md#4-the-game-runs-peerova-hidden-players-run-nothing).

### Key files (`key_file`)

Keep every key file in `Application.persistentDataPath` (writable on every platform, kept across runs and game updates):

- **Player (client):** `Path.Combine(Application.persistentDataPath, "peerova-client.key")` — use it for `--key` (as above) or for
  `ClientConfig.KeyFile` when calling `Peerova.Client.Connect(ticket, new ClientConfig { KeyFile = … })` directly. Keeping the file keeps the
  player's identity and stable `127.a.b.c` address on the server; banning by address works per player only while they keep it (robust:
  accounts/tokens). `PeerovaBehaviour.Join()` does not set a client key file — use `Client.Connect` as above when you need a stable identity.
- **Host (server):** the `keyFile` field of `PeerovaBehaviour` (default `peerova-server.key`) is a path **relative to**
  `Application.persistentDataPath`. Same file ⇒ same ticket. It is a secret — never put it in `StreamingAssets` or the build.

### Using the SDK (`PeerovaBehaviour`)

The `Peerova.Unity.PeerovaBehaviour` component polls in `Update()` and raises UnityEvents on the main thread: `OnHostStarted(ticket)`,
`OnRoomCode(code)`, `OnConnected(peer)`, `OnMessage(peer, channel, data, reliable)`, `OnDisconnected(peer)`, `OnConnectFailed(reason)`,
plus the C# event `EventReceived` (the full `Peerova.Event` struct). Methods: `Host()` (starts the server on a worker thread), `Join(ticketOrCode, token)`,
`Send(peer, data, channel, reliable)`, `Broadcast(data, channel, reliable)`, `Disconnect(peer)`, `Shutdown()`; properties `Ticket`, `RoomCode`,
`Peers`, `IsRunning`, `CurrentRole`. Configure it in the Inspector (`port`, `license`, `token`, `maxCcu`, `keyFile`, `app`, `shareName`…).

```csharp
using Peerova.Unity;
using UnityEngine;

public class Net : MonoBehaviour
{
    PeerovaBehaviour net;

    void Start()
    {
        net = gameObject.AddComponent<PeerovaBehaviour>();
        net.OnHostStarted.AddListener(ticket => Debug.Log("TICKET " + ticket));
        net.OnConnected.AddListener(peer => net.Send(peer, System.Text.Encoding.UTF8.GetBytes("hello")));
        net.OnMessage.AddListener((peer, channel, data, reliable) => Debug.Log($"{peer}: {data.Length} B"));
        net.OnConnectFailed.AddListener(reason =>
            Debug.LogError($"{Peerova.ErrorCodes.FromText(reason)}: {reason}"));   // e.g. Timeout, CodeNotFound
    }

    public void HostGame() => net.Host();                                   // server, started off the main thread
    public void JoinGame(string ticketOrCode) => net.Join(ticketOrCode);    // ticket or 6-character code
}
```

Full guide: [`com.peerova.sdk/README.md`](com.peerova.sdk/README.md). Host-in-game (6-character codes, public room list):
[Host-in-game](../DEVELOPER-GUIDE.md#6-host-in-game-one-player-hosts).

### Error codes

The `OnConnectFailed` reason is English text `"E_NAME: ..."`; turn it into a stable code with
`Peerova.ErrorCodes.FromText(reason)` (enum `Peerova.ErrorCode`: `Timeout`, `CodeNotFound`, `CcuLimit`…). Disconnect (`Disconnected`)
and failure reasons: from the C# event `EventReceived`, `ev.ErrorCode`. After `Send` returns `false`: `Peerova.Endpoint.LastErrorCode`. Code table: [Error codes](../DEVELOPER-GUIDE.md#error-codes).

### Game servers

A dedicated server for a Unity game: run `peerova server --expose …` next to your server build, or use the SDK inside the server build.
Several ports, a separate address per player + bans, hiding the server IP, VPN mode:
[Game-server features](../DEVELOPER-GUIDE.md#7-game-server-features).

### FAQ

- **Do players need to type a code?** No. Embed the ticket or fetch it from your URL; 6-character codes are only for inviting friends.
- **Can I run `peerova` hidden on Android/iOS?** No — mobile can't run child processes; use `PeerovaBehaviour`.
- **Do I need `wintun.dll`?** No, neither the SDK nor `peerova client --forward` needs it.
- **Can't connect?** Run `peerova doctor` on both machines. More: [Troubleshooting](../DEVELOPER-GUIDE.md#15-troubleshooting) · [FAQ](../DEVELOPER-GUIDE.md#16-faq-1).
