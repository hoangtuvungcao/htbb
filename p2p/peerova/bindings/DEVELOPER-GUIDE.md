# Peerova SDK — Hướng dẫn cho lập trình viên / Developer Guide

- [Tiếng Việt](#tiếng-việt)
- [English](#english)

Website, license, tải về / downloads: https://peerova.pages.dev

---

# Tiếng Việt

**Mục lục:** [1. Chọn cách tích hợp](#1-chọn-cách-tích-hợp) ·
[2. Người chơi không phải gõ gì](#2-người-chơi-không-phải-gõ-gì) ·
[3. Game có sẵn, không sửa code](#3-game-có-sẵn-không-sửa-code) ·
[4. Game tự chạy peerova ẩn](#4-game-tự-chạy-peerova-ẩn-người-chơi-không-chạy-gì) ·
[5. Game mới dùng SDK](#5-game-mới-dùng-sdk) ·
[6. Host trong game](#6-host-trong-game-một-người-chơi-mở-phòng) ·
[7. Tính năng máy chủ game](#7-tính-năng-máy-chủ-game) ·
[8. Khái niệm](#8-khái-niệm) · [9. Vòng đời](#9-vòng-đời) · [10. Tham chiếu C API](#10-tham-chiếu-c-api-includepeerovah) ·
[Mã lỗi](#mã-lỗi) · [11. Luồng & vòng poll](#11-luồng--vòng-poll-theo-engine) · [12. Mẫu dùng](#12-mẫu-dùng) ·
[13. Hiệu năng](#13-hiệu-năng) · [14. Bảo mật](#14-bảo-mật) · [15. Khắc phục sự cố](#15-khắc-phục-sự-cố) · [16. FAQ](#16-faq)

Peerova kết nối trực tiếp hai (hoặc nhiều) máy, mã hoá đầu-cuối, không cần mở port hay có IP public.
Kết nối là tự động: thử đường trực tiếp trước; nếu mạng chặn kết nối trực tiếp, Peerova tự dùng **đường mã hoá tự động**
(vẫn mã hoá đầu-cuối), rồi chuyển sang trực tiếp ngay khi có thể. Ứng dụng không cần làm gì thêm.

Có hai thứ trong gói SDK, dùng riêng hoặc kết hợp:

- **Công cụ dòng lệnh `peerova`** (`peerova.exe` trên Windows): chia sẻ cổng TCP/UDP của một chương trình có sẵn
  (`peerova server --expose …`) và mở nó trên máy khác tại `127.0.0.1` (`peerova client … --forward …`). Không cần sửa code.
- **Thư viện SDK** (`peerova.dll` / `libpeerova.so` / `libpeerova.dylib` + binding cho C#, Unity, Unreal, Java, Kotlin, Python, Go,
  Node.js, Dart/Flutter, PHP, Lua, C/C++…): gửi/nhận message reliable/unreliable ngay trong game/app.

## 1. Chọn cách tích hợp

| # | Tình huống | Dùng gì | Người chơi phải làm gì | Đọc |
|---|---|---|---|---|
| 1 | Game/app **có sẵn**, không muốn sửa code | Máy chủ: `peerova server --expose tcp:8888`. Người chơi: `peerova client <TICKET> --forward tcp:8888`, rồi game kết nối `127.0.0.1:8888` | Chạy một lệnh (hoặc file `.bat`/shortcut bạn đưa) | [§3](#3-game-có-sẵn-không-sửa-code) |
| 2 | Game **có sẵn**, người chơi **không được phải chạy gì** | Game/launcher tự chạy `peerova` ẩn (không cửa sổ console), chờ dòng sẵn sàng, rồi code socket cũ kết nối `127.0.0.1:8888` | Không gì cả | [§4](#4-game-tự-chạy-peerova-ẩn-người-chơi-không-chạy-gì) |
| 3 | Game **mới** (hoặc sẵn sàng thay lớp mạng) | SDK ở client và/hoặc server: message reliable + unreliable, sự kiện `CONNECTED`/`MESSAGE`/… | Không gì cả | [§5](#5-game-mới-dùng-sdk) |
| 4 | **Host trong game** (một người chơi mở phòng) | SDK server chạy bên trong game của người host; chia sẻ ticket hoặc mã 6 ký tự | Bạn bè bấm "Vào phòng" / nhập mã 6 ký tự | [§6](#6-host-trong-game-một-người-chơi-mở-phòng) |

Ngôn ngữ không có binding native (shell, Perl, Erlang, Lua 5.x thuần…) hoặc không muốn native module: dùng sidecar
`peerova stdio` (JSON lines qua stdin/stdout) — cùng tính năng như SDK, xem [`stdio/README.md`](stdio/README.md).

Cách 1 và 2 dùng được với **mọi** game/chương trình TCP/UDP (kể cả game bạn không có source code). Cách 3 và 4 cho độ trễ
thấp nhất và kiểm soát hoàn toàn (kênh, reliable/unreliable, ai kết nối, kick).

## 2. Người chơi không phải gõ gì

Người chơi **không bao giờ** cần gõ ticket hay mã. Chọn một trong các cách sau:

1. **Nhúng ticket vào game.** Ticket (`pvt_...`) gắn với file `server.key` của máy chủ: giữ nguyên file này thì ticket bạn
   phát hành tiếp tục dùng được qua mọi lần khởi động lại máy chủ. Ticket **không phải bí mật** (giống địa chỉ máy chủ):
   ai có ticket thì thử kết nối được, nên muốn giới hạn người vào hãy dùng `--token` / tài khoản (xem [§14](#14-bảo-mật)).
   Lấy ticket: dòng `TICKET` khi máy chủ khởi động, hoặc `--ticket-file ticket.txt` để server ghi ra file.
2. **Lấy ticket mới nhất từ URL/API của bạn khi game khởi động** — đổi/thêm máy chủ mà không phải ra bản vá.
   Máy chủ ghi `--ticket-file ticket.txt`; bạn đưa file đó lên web của mình; game tải về, dùng ticket nhúng sẵn nếu không tải được:
   ```csharp
   string ticket = EmbeddedTicket;                       // ticket nhúng trong bản build
   try { ticket = (await new HttpClient { Timeout = TimeSpan.FromSeconds(5) }
             .GetStringAsync("https://mygame.example.com/peerova-ticket.txt")).Trim(); } catch { }
   ```
3. **Nhiều máy chủ, tự chuyển khi một máy hỏng (failover).** Chép **cùng một** `server.key` sang mọi máy, chạy
   `peerova server --key server.key --ticket-file a.txt …` trên từng máy, rồi gộp:
   ```sh
   peerova ticket merge @a.txt @b.txt @c.txt > ticket.txt
   ```
   Ticket gộp chứa mọi máy: client thử tất cả cùng lúc và tự vào máy còn sống. Một người chơi đang chơi trên máy bị sập
   sẽ bị ngắt; lần kết nối lại (CLI: `--reconnect`) tự chọn máy khác. Trạng thái game giữa các máy do bạn tự đồng bộ.
   Mỗi máy đang chạy tính là một server trong license.
4. **Mã kết nối 6 ký tự — chỉ cho "mời bạn bè".** Phòng do người chơi mở (§6) cần một thứ ngắn để đọc qua voice chat:
   máy chủ chạy với `--app <project id> --share "Tên phòng"` (SDK: `app` + `share`) nhận mã như `K7Q2XD`; bạn bè nhập mã đó
   (client truyền mã vào đúng chỗ của ticket). Mã hết hiệu lực khi phòng đóng. Với máy chủ cố định, dùng ticket nhúng (cách 1–3).
5. **Danh sách phòng công khai.** Thêm `--public` (SDK: `"share":{"public":true,…}`) thì phòng hiện trong danh sách công khai của
   project tại `https://peerova.pages.dev/connect?app=<project id>` (ticket không bao giờ bị hiển thị). Game có thể tự hiện danh sách
   bằng `GET https://peerova.pages.dev/api/v1/lobby/list?app=<project id>` (tuỳ chọn `&mode=…&region=…&q=…&limit=…`), trả về
   `{"lobbies":[{"code","name","players","max","mode","region","version","tags","age"}]}`; người chơi bấm vào phòng → game kết nối bằng `code`.
   Thông tin hiển thị: `--max N`, `--mode M`, `--region VN`, `--tag T` (CLI) hoặc `share` (SDK).

## 3. Game có sẵn, không sửa code

Máy chủ (máy chạy game server, ví dụ game nghe TCP 8888 và UDP 8888):

```sh
peerova server --license PVA-... --expose tcp:8888 --expose udp:8888
```

Lần đầu chạy, `server.key` được tạo trong thư mục hiện tại (giữ file này — nó quyết định ticket). Máy chủ in ra một khung
`TICKET` chứa chuỗi `pvt_...`. Gửi chuỗi đó cho người chơi (hoặc nhúng nó, §2).

Người chơi:

```sh
peerova client pvt_... --forward tcp:8888 --forward udp:8888
```

Rồi trong game, kết nối tới `127.0.0.1:8888`. Khi đã kết nối, client in:

```
[    0.02] ✔ ĐÃ KẾT NỐI TRỰC TIẾP tới 203.0.113.7:47000 [lan] — mã hoá E2E
[    0.02] ✔ Tcp 127.0.0.1:8888 → server port 8888
[    0.02] ✔ Udp 127.0.0.1:8888 → server port 8888
```

- `--forward PROTO:LOCAL[-LOCAL][=REMOTE]`: `tcp:8888` (cổng local 8888 → cổng 8888 của máy chủ), `tcp:18888=8888` (local khác remote),
  `udp:27015-27030` (dải cổng). `--bind IP` đổi địa chỉ nghe (mặc định `127.0.0.1`).
- `--key FILE`: file danh tính của người chơi (mặc định nằm trong thư mục cấu hình của người dùng). Giữ file này thì máy chủ thấy người
  chơi ở cùng một địa chỉ ổn định (§7.3).
- `--reconnect`: tự kết nối lại khi mất kết nối. `--token SECRET`: nếu máy chủ yêu cầu token.
- PROTO: `tcp http https ws wss ssh rdp grpc` (TCP) · `udp quic game` (UDP).
- Mọi tuỳ chọn đặt được trong file JSON: `peerova server --config peerova.json`, ví dụ
  `{"port":47000,"license":"PVA-...","expose":["tcp:8888","udp:8888"],"banned":"banned.txt"}`.
- Không có license, máy chủ chạy chế độ dev (tối đa 3 client đồng thời).

## 4. Game tự chạy peerova ẩn (người chơi không chạy gì)

Phát hành `peerova.exe` (Windows) / `peerova` (Linux, macOS) **kèm game**. Khi người chơi bấm "Chơi", game hoặc launcher:

1. chạy `peerova client <TICKET> --forward tcp:8888 --key <appdata>/<game>/peerova-client.key` **ẩn** (không cửa sổ console);
2. đọc stdout từng dòng, chờ dòng sẵn sàng (có timeout, ví dụ 30 s);
3. kết nối code socket **có sẵn** tới `127.0.0.1:8888` — không đổi gì khác;
4. dừng (kill) process `peerova` khi game thoát.

**Dòng cần chờ** (stdout là UTF-8):

| Dòng | Ý nghĩa |
|---|---|
| `✔ ĐÃ KẾT NỐI TRỰC TIẾP tới …` hoặc `✔ ĐÃ KẾT NỐI qua đường dự phòng tự động …` | Đã kết nối tới máy chủ. |
| `✔ Tcp 127.0.0.1:8888 → server port 8888` (mỗi `--forward` một dòng, in ngay sau dòng trên) | Cổng local đã mở — **sẵn sàng**. Code bên dưới chờ chuỗi ASCII **`server port`** (không phụ thuộc bảng mã console). |
| `✘ …`, hoặc process thoát (mã thoát `1`, lý do trên stderr bắt đầu bằng `lỗi:`) | Thất bại: ticket sai, không tới được máy chủ, cổng local đang bận… |

Nhiều `--forward` → chờ đủ số dòng `server port`. Các dòng khác là log (tiếng Việt) — ghi vào file log, đừng phân tích.
Process `peerova client` thoát khi mất kết nối (trừ khi có `--reconnect`) — theo dõi sự kiện thoát để báo "mất kết nối" và cho chơi lại.

Lưu ý chung:
- Thư mục chứa `--key` phải tồn tại (code bên dưới tự tạo). Lưu trong thư mục dữ liệu ứng dụng của người dùng để người chơi giữ
  cùng danh tính (cấm theo địa chỉ hoạt động đúng, §7.3).
- Linux/macOS: file `peerova` phải có quyền chạy (`chmod +x`). macOS: ký (codesign) cùng app.
- Chọn cổng local ít đụng (ví dụ `tcp:38888=8888`) nếu cổng 8888 có thể đang bị chương trình khác dùng trên máy người chơi.
- Phải đọc hết stdout/stderr (code bên dưới làm sẵn), nếu không pipe đầy có thể làm `peerova` dừng.
- **Không dùng được `peerova stdio` cho cách này**: sidecar `stdio` không mở cổng `127.0.0.1`, nó trao đổi message SDK qua
  JSON lines (sự kiện có cấu trúc `connected`/`message`/… kèm `code`). Hợp khi bạn chịu thay lớp mạng của game bằng message —
  xem [`stdio/README.md`](stdio/README.md). Để giữ nguyên code socket, dùng `peerova client` như dưới.

### C# (.NET, WinForms/WPF, console)
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

### Unity (Windows/Linux/macOS standalone)
Dùng lại class `PeerovaTunnel` ở trên (đặt `PeerovaTunnel.cs` vào `Assets/Scripts/`). Mobile/console không chạy được process con —
dùng SDK (§5).
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

### Java (8+)
```java
// PeerovaTunnel.java — Java 8+. Runs the bundled `peerova` CLI and waits until the local port is open.
// Java starts console programs without a console window on Windows.
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class PeerovaTunnel implements AutoCloseable {
    private Process proc;

    /** Returns true once the forwarded port is open on 127.0.0.1; false on failure or timeout. */
    public boolean start(String exe, String ticket, String forward, Path keyFile, long timeoutMs)
            throws IOException, InterruptedException {
        Files.createDirectories(keyFile.toAbsolutePath().getParent());
        ProcessBuilder pb = new ProcessBuilder(exe, "client", ticket, "--forward", forward, "--key", keyFile.toString());
        pb.redirectErrorStream(true);                       // stdout + stderr in one stream
        proc = pb.start();
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));

        CountDownLatch ready = new CountDownLatch(1);
        Thread reader = new Thread(() -> {
            try (BufferedReader r = new BufferedReader(new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
                for (String line; (line = r.readLine()) != null; ) {
                    System.out.println("[peerova] " + line);
                    // "✔ Tcp 127.0.0.1:8888 → server port 8888" = connected and the local port is open
                    if (line.contains("server port")) ready.countDown();
                }
            } catch (IOException ignored) { }
        }, "peerova-output");
        reader.setDaemon(true);
        reader.start();

        long deadline = System.currentTimeMillis() + timeoutMs;
        while (!ready.await(100, TimeUnit.MILLISECONDS)) {
            if (!proc.isAlive() || System.currentTimeMillis() > deadline) { close(); return false; }
        }
        return true;
    }

    public boolean isAlive() { return proc != null && proc.isAlive(); }

    @Override public void close() { if (proc != null && proc.isAlive()) proc.destroyForcibly(); }
}
```
```java
boolean win = System.getProperty("os.name").startsWith("Windows");
String appData = win ? System.getenv("APPDATA") : System.getProperty("user.home") + "/.local/share";
Path key = java.nio.file.Paths.get(appData, "MyGame", "peerova-client.key");
String exe = win ? "peerova\\peerova.exe" : "peerova/peerova";   // next to your game

PeerovaTunnel tunnel = new PeerovaTunnel();
if (tunnel.start(exe, "pvt_...", "tcp:8888", key, 30_000)) {
    connectToGameServer("127.0.0.1", 8888);      // your existing socket code, unchanged
} else {
    showError("Could not reach the server");
}
```

### Node.js / Electron (main process)
```js
// peerova-tunnel.mjs — Node.js 18+ / Electron main process.
import { spawn } from "node:child_process";
import { createInterface } from "node:readline";
import { mkdirSync } from "node:fs";
import path from "node:path";

/** Runs `peerova client` hidden. `ready` resolves once the forwarded port is open on 127.0.0.1. */
export function startTunnel({ exe, ticket, forward, keyFile, timeoutMs = 30000, onLog = () => {} }) {
  mkdirSync(path.dirname(keyFile), { recursive: true });
  const child = spawn(exe, ["client", ticket, "--forward", forward, "--key", keyFile], {
    windowsHide: true,                       // no console window on Windows
    stdio: ["ignore", "pipe", "pipe"],
  });
  const stop = () => { if (child.exitCode === null && child.signalCode === null) child.kill(); };
  process.once("exit", stop);

  const ready = new Promise((resolve, reject) => {
    const timer = setTimeout(() => { stop(); reject(new Error("peerova: timed out")); }, timeoutMs);
    for (const stream of [child.stdout, child.stderr]) {
      createInterface({ input: stream }).on("line", (line) => {
        onLog(line);
        // "✔ Tcp 127.0.0.1:8888 → server port 8888" = connected and the local port is open
        if (line.includes("server port")) { clearTimeout(timer); resolve(); }
      });
    }
    child.once("error", (e) => { clearTimeout(timer); reject(e); });
    child.once("exit", (code) => { clearTimeout(timer); reject(new Error(`peerova exited (${code})`)); });
  });
  return { child, ready, stop };
}
```
```js
// Electron main process. Ship the CLI with electron-builder "extraResources": [{ "from": "bin", "to": "bin" }]
// (it must not be inside app.asar).
import { app } from "electron";
import path from "node:path";
import { startTunnel } from "./peerova-tunnel.mjs";

const exe = path.join(app.isPackaged ? process.resourcesPath : app.getAppPath(), "bin",
  process.platform === "win32" ? "peerova.exe" : "peerova");
const tunnel = startTunnel({
  exe, ticket: "pvt_...", forward: "tcp:8888",
  keyFile: path.join(app.getPath("userData"), "peerova-client.key"),
  onLog: (l) => console.log("[peerova]", l),
});
app.on("before-quit", tunnel.stop);
tunnel.child.on("exit", () => { /* tunnel closed: show "disconnected", offer to reconnect */ });

await tunnel.ready;             // throws on failure / timeout
connectToGameServer("127.0.0.1", 8888);   // your existing socket code, unchanged
```

### Python (3.8+)
```python
# peerova_tunnel.py — Python 3.8+. Runs the bundled `peerova` CLI hidden and waits until the local port is open.
import atexit, os, subprocess, sys, threading, time

def start_tunnel(exe, ticket, forward, key_file, timeout=30.0, on_log=print):
    """Returns the running Popen once the forwarded port is open on 127.0.0.1, or None on failure/timeout."""
    os.makedirs(os.path.dirname(os.path.abspath(key_file)), exist_ok=True)
    flags = subprocess.CREATE_NO_WINDOW if sys.platform == "win32" else 0   # no console window
    p = subprocess.Popen(
        [exe, "client", ticket, "--forward", forward, "--key", key_file],
        stdin=subprocess.DEVNULL, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
        text=True, encoding="utf-8", errors="replace", creationflags=flags)
    atexit.register(stop_tunnel, p)
    ready = threading.Event()

    def reader():
        for line in p.stdout:
            on_log(line.rstrip())
            # "✔ Tcp 127.0.0.1:8888 → server port 8888" = connected and the local port is open
            if "server port" in line:
                ready.set()
    threading.Thread(target=reader, daemon=True).start()

    deadline = time.monotonic() + timeout
    while not ready.wait(0.1):
        if p.poll() is not None or time.monotonic() > deadline:
            stop_tunnel(p)
            return None
    return p

def stop_tunnel(p):
    if p.poll() is None:
        p.kill()
        p.wait()
```
```python
import os, sys
from peerova_tunnel import start_tunnel

here = os.path.dirname(os.path.abspath(sys.argv[0]))
exe = os.path.join(here, "peerova.exe" if sys.platform == "win32" else "peerova")
appdata = os.environ.get("APPDATA") or os.path.expanduser("~/.local/share")
key = os.path.join(appdata, "MyGame", "peerova-client.key")

tunnel = start_tunnel(exe, "pvt_...", "tcp:8888", key)
if tunnel:
    connect_to_game_server("127.0.0.1", 8888)    # your existing socket code, unchanged
else:
    show_error("Could not reach the server")
```

### Go
```go
// peerovatunnel/tunnel.go — runs the bundled `peerova` CLI hidden and waits until the local port is open.
package peerovatunnel

import (
	"bufio"
	"errors"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"time"
)

type Tunnel struct {
	cmd  *exec.Cmd
	done chan struct{}
}

// Start returns once the forwarded port is open on 127.0.0.1.
func Start(exe, ticket, forward, keyFile string, timeout time.Duration, onLog func(string)) (*Tunnel, error) {
	if err := os.MkdirAll(filepath.Dir(keyFile), 0o700); err != nil {
		return nil, err
	}
	cmd := exec.Command(exe, "client", ticket, "--forward", forward, "--key", keyFile)
	hideWindow(cmd)
	out, err := cmd.StdoutPipe()
	if err != nil {
		return nil, err
	}
	cmd.Stderr = cmd.Stdout // same pipe for stdout + stderr
	if err := cmd.Start(); err != nil {
		return nil, err
	}
	t := &Tunnel{cmd: cmd, done: make(chan struct{})}
	ready := make(chan struct{})
	go func() {
		sc := bufio.NewScanner(out)
		signalled := false
		for sc.Scan() {
			line := sc.Text()
			if onLog != nil {
				onLog(line)
			}
			// "✔ Tcp 127.0.0.1:8888 → server port 8888" = connected and the local port is open
			if !signalled && strings.Contains(line, "server port") {
				signalled = true
				close(ready)
			}
		}
		_ = cmd.Wait()
		close(t.done)
	}()
	select {
	case <-ready:
		return t, nil
	case <-t.done:
		return nil, errors.New("peerova exited before connecting")
	case <-time.After(timeout):
		t.Stop()
		return nil, errors.New("peerova: timed out")
	}
}

// Done is closed when the tunnel process ends (connection lost or Stop).
func (t *Tunnel) Done() <-chan struct{} { return t.done }

func (t *Tunnel) Stop() {
	if t.cmd.Process != nil {
		_ = t.cmd.Process.Kill()
	}
}
```
```go
// peerovatunnel/hide_windows.go
//go:build windows

package peerovatunnel

import (
	"os/exec"
	"syscall"
)

func hideWindow(c *exec.Cmd) {
	c.SysProcAttr = &syscall.SysProcAttr{HideWindow: true, CreationFlags: 0x08000000} // CREATE_NO_WINDOW
}
```
```go
// peerovatunnel/hide_other.go
//go:build !windows

package peerovatunnel

import "os/exec"

func hideWindow(*exec.Cmd) {}
```
```go
exe := "./peerova"
if runtime.GOOS == "windows" {
	exe = `.\peerova.exe`
}
cfg, _ := os.UserConfigDir() // %APPDATA% on Windows, ~/.config on Linux
key := filepath.Join(cfg, "MyGame", "peerova-client.key")

t, err := peerovatunnel.Start(exe, "pvt_...", "tcp:8888", key, 30*time.Second, func(l string) { log.Println("[peerova]", l) })
if err != nil {
	log.Fatal(err)
}
defer t.Stop()
conn, err := net.Dial("tcp", "127.0.0.1:8888") // your existing socket code, unchanged
```

### C++ (17, Windows + Linux/macOS)
Trên Windows, class này đưa `peerova` vào một *job object*: nếu game thoát hoặc crash, Windows tự dừng `peerova`.
```cpp
// peerova_tunnel.hpp — C++17, Windows + Linux/macOS. Runs the bundled `peerova` CLI hidden
// and waits until the local port is open. Header-only, no dependencies.
#pragma once
#include <chrono>
#include <condition_variable>
#include <functional>
#include <initializer_list>
#include <mutex>
#include <string>
#include <thread>
#include <vector>
#ifdef _WIN32
#include <windows.h>
#else
#include <csignal>
#include <spawn.h>
#include <sys/wait.h>
#include <unistd.h>
extern char** environ;
#endif

class PeerovaTunnel {
public:
    std::function<void(const std::string&)> onLog;   // every output line (background thread)

    ~PeerovaTunnel() { stop(); }

    // keyFile's folder must exist. Returns true once the forwarded port is open on 127.0.0.1.
    bool start(const std::string& exe, const std::string& ticket, const std::string& forward,
               const std::string& keyFile, int timeoutMs = 30000) {
        if (!spawn(exe, {"client", ticket, "--forward", forward, "--key", keyFile})) return false;
        reader_ = std::thread([this] { readLoop(); });
        std::unique_lock<std::mutex> lk(m_);
        cv_.wait_for(lk, std::chrono::milliseconds(timeoutMs), [this] { return ready_ || ended_; });
        bool ok = ready_;
        lk.unlock();
        if (!ok) stop();
        return ok;
    }

    bool running() { std::lock_guard<std::mutex> g(m_); return ready_ && !ended_; }

    void stop() {
        terminate();
        if (reader_.joinable()) reader_.join();
        closePipe();
    }

private:
    std::thread reader_;
    std::mutex m_;
    std::condition_variable cv_;
    bool ready_ = false, ended_ = false;

    void onLine(const std::string& line) {
        if (onLog) onLog(line);
        // "✔ Tcp 127.0.0.1:8888 → server port 8888" = connected and the local port is open
        if (line.find("server port") != std::string::npos) {
            std::lock_guard<std::mutex> g(m_);
            ready_ = true;
            cv_.notify_all();
        }
    }
    void readLoop() {
        std::string buf;
        char chunk[512];
        for (long n; (n = readSome(chunk, sizeof chunk)) > 0;) {
            buf.append(chunk, (size_t)n);
            for (size_t p; (p = buf.find('\n')) != std::string::npos; buf.erase(0, p + 1)) {
                std::string line = buf.substr(0, p);
                if (!line.empty() && line.back() == '\r') line.pop_back();
                onLine(line);
            }
        }
        std::lock_guard<std::mutex> g(m_);
        ended_ = true;
        cv_.notify_all();
    }

#ifdef _WIN32
    HANDLE proc_ = nullptr, out_ = nullptr, job_ = nullptr;
    static std::wstring widen(const std::string& s) {
        int n = MultiByteToWideChar(CP_UTF8, 0, s.c_str(), -1, nullptr, 0);
        std::wstring w(n, L'\0');
        MultiByteToWideChar(CP_UTF8, 0, s.c_str(), -1, &w[0], n);
        w.pop_back();
        return w;
    }
    bool spawn(const std::string& exe, std::initializer_list<std::string> args) {
        SECURITY_ATTRIBUTES sa{sizeof sa, nullptr, TRUE};
        HANDLE rd, wr;
        if (!CreatePipe(&rd, &wr, &sa, 0)) return false;
        SetHandleInformation(rd, HANDLE_FLAG_INHERIT, 0);
        std::wstring cmd = L"\"" + widen(exe) + L"\"";
        for (const auto& a : args) cmd += L" \"" + widen(a) + L"\"";
        STARTUPINFOW si{};
        si.cb = sizeof si;
        si.dwFlags = STARTF_USESTDHANDLES;
        si.hStdOutput = wr;
        si.hStdError = wr;
        PROCESS_INFORMATION pi{};
        BOOL ok = CreateProcessW(nullptr, &cmd[0], nullptr, nullptr, TRUE,
                                 CREATE_NO_WINDOW | CREATE_SUSPENDED, nullptr, nullptr, &si, &pi);
        CloseHandle(wr);
        if (!ok) { CloseHandle(rd); return false; }
        // Job object: Windows kills peerova automatically if the game exits or crashes.
        job_ = CreateJobObjectW(nullptr, nullptr);
        JOBOBJECT_EXTENDED_LIMIT_INFORMATION li{};
        li.BasicLimitInformation.LimitFlags = JOB_OBJECT_LIMIT_KILL_ON_JOB_CLOSE;
        SetInformationJobObject(job_, JobObjectExtendedLimitInformation, &li, sizeof li);
        AssignProcessToJobObject(job_, pi.hProcess);
        ResumeThread(pi.hThread);
        CloseHandle(pi.hThread);
        proc_ = pi.hProcess;
        out_ = rd;
        return true;
    }
    long readSome(char* b, size_t n) {
        DWORD got = 0;
        return ReadFile(out_, b, (DWORD)n, &got, nullptr) ? (long)got : -1;
    }
    void terminate() {
        if (proc_) { TerminateProcess(proc_, 0); WaitForSingleObject(proc_, 2000); CloseHandle(proc_); proc_ = nullptr; }
        if (job_) { CloseHandle(job_); job_ = nullptr; }
    }
    void closePipe() { if (out_) { CloseHandle(out_); out_ = nullptr; } }
#else
    pid_t pid_ = -1;
    int out_ = -1;
    bool spawn(const std::string& exe, std::initializer_list<std::string> args) {
        int fd[2];
        if (pipe(fd) != 0) return false;
        posix_spawn_file_actions_t fa;
        posix_spawn_file_actions_init(&fa);
        posix_spawn_file_actions_adddup2(&fa, fd[1], 1);
        posix_spawn_file_actions_adddup2(&fa, fd[1], 2);
        posix_spawn_file_actions_addclose(&fa, fd[0]);
        posix_spawn_file_actions_addclose(&fa, fd[1]);
        std::vector<std::string> all{exe};
        all.insert(all.end(), args);
        std::vector<char*> argv;
        for (auto& s : all) argv.push_back(&s[0]);
        argv.push_back(nullptr);
        int rc = posix_spawn(&pid_, exe.c_str(), &fa, nullptr, argv.data(), environ);
        posix_spawn_file_actions_destroy(&fa);
        close(fd[1]);
        if (rc != 0) { close(fd[0]); pid_ = -1; return false; }
        out_ = fd[0];
        return true;
    }
    long readSome(char* b, size_t n) { return (long)read(out_, b, n); }
    void terminate() {
        if (pid_ > 0) { kill(pid_, SIGTERM); waitpid(pid_, nullptr, 0); pid_ = -1; }
    }
    void closePipe() { if (out_ >= 0) { close(out_); out_ = -1; } }
#endif
};
```
```cpp
#include <cstdio>
#include <cstdlib>
#include "peerova_tunnel.hpp"

int main() {
#ifdef _WIN32
    std::string exe = "peerova.exe";                                    // next to your game .exe
    std::string key = std::string(std::getenv("APPDATA")) + "\\MyGame\\peerova-client.key";
#else
    std::string exe = "./peerova";
    std::string key = std::string(std::getenv("HOME")) + "/.local/share/MyGame/peerova-client.key";
#endif
    // create the key file's folder first (std::filesystem::create_directories)
    PeerovaTunnel tunnel;
    tunnel.onLog = [](const std::string& l) { std::fprintf(stderr, "[peerova] %s\n", l.c_str()); };
    if (!tunnel.start(exe, "pvt_...", "tcp:8888", key)) {
        std::fprintf(stderr, "Could not reach the server\n");
        return 1;
    }
    connect_to_game_server("127.0.0.1", 8888);   // your existing socket code, unchanged
    // ... game loop; tunnel.running() turns false if the connection is lost
}   // ~PeerovaTunnel() stops peerova
```

## 5. Game mới dùng SDK

Server (máy chủ dedicated hoặc người host) và client đều dùng cùng một API: `server_start` → ticket; `client_connect(ticket)`;
cả hai `poll` sự kiện và `send(peer, channel, bytes, reliable)`.

```c
// Server
peerova_node* s = peerova_server_start("{\"port\":47000,\"key_file\":\"server.key\",\"license\":\"PVA-...\"}");
char ticket[1024]; peerova_ticket(s, ticket, sizeof ticket);           // nhúng / công bố ticket này
for (;;) {
    peerova_event ev;
    while (peerova_poll_wait(s, &ev, 100)) {
        if (ev.type == PEEROVA_EVENT_CONNECTED)    { /* người chơi ev.peer vào */ }
        if (ev.type == PEEROVA_EVENT_MESSAGE)      peerova_send(s, ev.peer, ev.channel, ev.data, ev.len, ev.reliable);
        if (ev.type == PEEROVA_EVENT_DISCONNECTED) { /* ev.peer rời; lý do trong ev.data */ }
    }
}

// Client — key_file giữ danh tính người chơi giữa các lần chạy
peerova_node* c = peerova_client_connect(ticket, "{\"key_file\":\"<appdata>/MyGame/peerova-client.key\"}");
// mỗi frame: while (peerova_poll(c, &ev)) { ... }   CONNECTED → bắt đầu gửi
peerova_send(c, server_peer, 0, (const uint8_t*)"hello", 5, 1);        // reliable, channel 0
peerova_send(c, server_peer, 1, input, input_len, 0);                  // unreliable ≤ 1180 byte
```

- **Reliable**: có thứ tự trong từng channel, tự gửi lại, kích thước bất kỳ (SDK tự chia/ghép) — chat, vật phẩm, RPC, vào/ra phòng.
- **Unreliable**: datagram, độ trễ thấp nhất, có thể mất/đảo, **tối đa 1180 byte** — input, vị trí, trạng thái realtime.
- Channel 0..255: mỗi channel có thứ tự riêng; channel nghẽn không chặn channel khác.
- Mỗi binding bọc đúng API này; xem README của ngôn ngữ bạn dùng: [C/C++](c/README.md), [C#](csharp/README.md), [Unity](unity/README.md),
  [Unreal](unreal/README.md), [Java](java/README.md), [Kotlin/Android](kotlin-android/README.md), [Python](python/README.md), [Go](go/README.md),
  [Node.js](nodejs/README.md), [Dart/Flutter](dart/README.md), [PHP](php/README.md), [Lua](lua/README.md), [stdio](stdio/README.md).
- Dùng đúng cặp: SDK client kết nối tới SDK server (hoặc `peerova stdio server`); `peerova client --forward` kết nối tới `peerova server --expose`.

## 6. Host trong game (một người chơi mở phòng)

Game của người host chạy SDK server bên trong game; bạn bè chạy SDK client.

1. Người host bấm "Mở phòng": gọi `server_start` **ngoài main thread** (chặn tới ~3 s). Binding Unity/Unreal/Godot có `Host()` làm sẵn.
   Dùng `key_file` riêng của người host (ví dụ `<appdata>/<game>/host.key`) để phòng có ticket ổn định.
2. Cách bạn bè vào:
   - qua hệ thống bạn bè/lobby của bạn (Steam, Discord, backend riêng): gửi **ticket** — game tự kết nối, không ai phải gõ;
   - hoặc **mã 6 ký tự**: cấu hình `{"app":"prj_...","share":{"name":"Phòng của Nam","max":4}}`; đọc mã bằng `peerova_room_code`
     (mã đến vài giây sau khi start — gọi lại định kỳ) rồi hiện trên màn hình; bạn bè nhập mã → `client_connect("K7Q2XD")`
     (chặn trong lúc tra mã — gọi ngoài UI thread; mã sai → lỗi `E_CODE_NOT_FOUND`);
   - thêm `"public":true` để phòng hiện trong danh sách phòng công khai (§2).
3. Người host vừa là server vừa chơi: game của host xử lý input của chính mình trực tiếp, còn của người khác qua sự kiện `MESSAGE`.

Không có license, server chạy chế độ dev: tối đa 3 client đồng thời (host + 3 bạn). Phòng lớn hơn do người chơi mở: liên hệ Peerova để
chọn gói phù hợp. **Không bao giờ nhúng license key vào bản game phát hành.**

## 7. Tính năng máy chủ game

### 7.1 Nhiều cổng trong một process
```sh
peerova server --license PVA-... --expose tcp:8888 --expose udp:8888 --expose udp:27015-27030
```
`--expose` lặp lại được, nhận dải cổng (tối đa 1024 cổng/dải) và đích khác: `--expose tcp:8888=192.168.1.20:8888`.
Một ticket mở được mọi cổng đã expose; client chọn cổng nào cần bằng `--forward`.

### 7.2 Nhiều process (ticket riêng) — ví dụ Remote Desktop cho admin + game công khai
Mỗi process cần cổng (`--port`) và file key (`--key`) riêng, nên có ticket riêng; **mỗi process tính là một server trong license**.
Tách riêng khi các nhóm người dùng khác nhau: ai có ticket game thì mở được mọi cổng của process game — đừng expose Remote Desktop chung với game.

```sh
# Game công khai — ticket phát cho mọi người chơi
peerova server --port 47000 --key game.key  --license PVA-... --expose tcp:8888 --expose udp:8888 --banned banned.txt --ticket-file game-ticket.txt

# Remote Desktop chỉ cho admin — cần token, giấu IP máy chủ
peerova server --port 47001 --key admin.key --license PVA-... --expose rdp:3389 --token "mật-khẩu-dài-ngẫu-nhiên" --hide-ip
```
Admin: `peerova client <ADMIN_TICKET> --forward tcp:13389=3389 --token "mật-khẩu-dài-ngẫu-nhiên"`, rồi mở Remote Desktop tới `127.0.0.1:13389`.
Doanh nghiệp: thay `--token` chung bằng token cấp cho từng người, giới hạn theo cổng (`peerova keygen --access`, `peerova token issue`, `--access-key`).

### 7.3 Mỗi người chơi một địa chỉ riêng & cấm người chơi
Khi game server chạy cùng máy với `peerova server` (Windows, Linux), game thấy mỗi người chơi là một địa chỉ riêng, ổn định dạng
`127.a.b.c` — kể cả khi họ đổi mạng (Wi-Fi ↔ 4G). Nhờ vậy lệnh cấm IP, giới hạn theo IP và log trong game hoạt động đúng cho từng người.
Log máy chủ in địa chỉ đó khi người chơi vào: `người chơi 9f43e9ec → IP trong game: 127.10.210.71`.

Cấm người chơi không cần khởi động lại:
```sh
peerova server --expose tcp:8888 --expose udp:8888 --banned banned.txt
```
`banned.txt`: mỗi dòng một địa chỉ (`127.10.210.71`), `#` là chú thích. File được đọc lại vài giây một lần; người bị cấm bị ngắt ngay và
không vào lại được.

Danh tính đến từ **file key của client** (`peerova client --key`, SDK `key_file`): địa chỉ chỉ ổn định khi người chơi giữ file đó —
người cố tình xoá file sẽ có địa chỉ mới. Muốn cấm chắc chắn, dùng tài khoản: cấp token theo người dùng (`--access-key` + `peerova token issue
--sub <user>`), khi đó địa chỉ gắn với tài khoản, hoặc kiểm tra tài khoản ở tầng game.

### 7.4 Ẩn IP máy chủ
**Ẩn IP máy chủ: người chơi không bao giờ biết IP thật của máy chủ (chống DDoS); ping cao hơn một chút.**
```sh
peerova server --expose udp:7777 --hide-ip          # SDK: {"hide_ip": true}
```
Mọi người chơi kết nối qua đường mã hoá tự động. Có ở gói Pro, Studio, Enterprise và bản dùng thử; với gói khác (hoặc không có license) máy chủ
không khởi động và báo cần nâng cấp gói. Lưu ý: `--private` / `"private": true` chỉ bỏ IP khỏi ticket — khi kết nối trực tiếp người chơi vẫn biết IP; muốn giấu thật sự hãy dùng `--hide-ip`.

### 7.5 Chế độ mạng riêng ảo (VPN) cho game
Cho game cần cả mạng LAN (tìm phòng bằng broadcast, nhiều cổng ngẫu nhiên, ping/ICMP): người chơi và máy chủ ở chung một mạng LAN ảo,
mỗi người chơi một IP riêng cố định (mặc định `10.77.x.x`, máy chủ `10.77.0.1`).
```sh
peerova server --vpn --vpn-allow tcp:8888,udp:8888 --banned banned.txt      # Windows: Administrator · Linux: sudo
peerova client <TICKET> --vpn                                               # trên máy người chơi, cùng quyền
```
`--vpn-allow` chỉ cho phép đúng các cổng của game — phần còn lại của máy chủ không ai truy cập được. `--subnet 10.77.0.0/16` đổi dải địa chỉ.
File cấm áp dụng cho địa chỉ VPN. Hỗ trợ Windows và Linux; Windows cần `wintun.dll` cạnh `peerova.exe` (cả máy chủ và máy người chơi).

**Quyền hệ điều hành — nói thẳng:** tạo card mạng ảo cần quyền Administrator (Windows) / root (Linux) trên **máy người chơi**. Hộp thoại xin quyền
(UAC trên Windows, mật khẩu sudo trên Linux) do hệ điều hành hiển thị — **không thể đổi nội dung, giao diện hay bỏ qua**. Khuyến nghị:
- cấp quyền **một lần lúc cài game**: trình cài đặt vốn đã chạy với quyền Administrator, hãy để nó thiết lập sẵn việc chạy `peerova` với quyền đó
  (ví dụ một Scheduled Task "Run with highest privileges" trên Windows; trên Linux `sudo setcap cap_net_admin+ep peerova`), để lúc chơi không hỏi lại;
- hoặc dùng chế độ **chuyển tiếp cổng** (§3, §4) — **không cần quyền gì**, đủ cho hầu hết game chỉ dùng vài cổng TCP/UDP cố định.

## 8. Khái niệm

| Khái niệm | Ý nghĩa |
|---|---|
| **Server node** | Máy "mở phòng" (host). Gọi `peerova_server_start`. Nhận nhiều client. |
| **Client node** | Máy tham gia. Gọi `peerova_client_connect(ticket)`. Kết nối tới đúng một server. |
| **Ticket** | Chuỗi `pvt_...` mà server sinh ra, chứa đủ thông tin để client tìm và xác thực server. Gửi qua bất kỳ kênh nào hoặc nhúng vào game. Cố định theo `key_file`. Không phải bí mật. |
| **Connect code** | Mã 6 ký tự (ví dụ `K7Q2XD`) thay cho ticket. Chỉ có khi server khởi động với `app` + `share` (project trên website). Client truyền mã này vào chỗ ticket. |
| **Connection id (peer)** | Số `uint32` định danh một kết nối. Server thấy mỗi client một id; client thấy server là một id. Dùng cho `send`/`disconnect`. Coi là id mờ. |
| **Channel** | Số 0..255. Mỗi channel có thứ tự riêng; channel này nghẽn không chặn channel khác. |
| **Reliable** | Có thứ tự (trong một channel), tự gửi lại khi mất, kích thước lớn được (SDK tự chia/ghép). |
| **Unreliable** | Datagram, độ trễ thấp nhất, có thể mất hoặc đảo thứ tự, **tối đa 1180 byte**. |
| **Event** | Mọi thứ đến từ mạng là sự kiện trong hàng đợi của node: `CONNECTED`, `MESSAGE`, `DISCONNECTED`, `CONNECT_FAILED`. Lấy bằng `poll`. |
| **Client key file** | File danh tính của người chơi (`key_file`, CLI `--key`). Giữ file ⇒ cùng danh tính, cùng địa chỉ `127.a.b.c` trên máy chủ. |
| **Dev mode** | Server không có `license` chạy chế độ dev: tối đa 3 client đồng thời. |

## 9. Vòng đời

```
SERVER                                          CLIENT
peerova_server_start(cfg)  ── chặn ≤ ~3 s
peerova_ticket() / peerova_room_code()
        │  ticket hoặc mã 6 ký tự (nhúng / gửi qua kênh của bạn)  ──►  peerova_client_connect(ticket, cfg)  (trả về ngay*)
        │                                                        │
poll ─► CONNECTED(peer=A)                                poll ─► CONNECTED(peer=S)   | CONNECT_FAILED(reason)
        │◄──────────── send / MESSAGE (cả hai chiều) ──────────►│
poll ─► DISCONNECTED(A, reason)  ◄── peerova_disconnect(S) ─────┤   (hoặc mất mạng / timeout)
peerova_free(server)                                      peerova_free(client)
```
\* Khi truyền **connect code**, `peerova_client_connect` tra mã trên website trước nên chặn trong thời gian một lượt tra cứu qua mạng;
mã sai/đã đóng → trả về `NULL` ngay (`peerova_last_error_code()` = `E_CODE_NOT_FOUND`), không phải sự kiện `CONNECT_FAILED`. Gọi ngoài UI thread.

## 10. Tham chiếu C API (`include/peerova.h`)

Mọi binding đều bọc đúng các hàm này. Tất cả chuỗi là UTF-8, kết thúc bằng NUL.

### Struct sự kiện
```c
typedef struct {
    int32_t        type;     // PEEROVA_EVENT_CONNECTED=1, DISCONNECTED=2, MESSAGE=3, CONNECT_FAILED=4
    uint32_t       peer;     // connection id
    uint8_t        channel;  // 0..255 (MESSAGE)
    uint8_t        reliable; // 1 nếu đến qua reliable (MESSAGE)
    const uint8_t* data;     // MESSAGE: payload; DISCONNECTED/CONNECT_FAILED: lý do UTF-8 tiếng Anh "E_NAME: ..."
    uint32_t       len;
} peerova_event;
```
`data` thuộc sở hữu của node và **chỉ hợp lệ tới lần `peerova_poll`/`peerova_poll_wait`/`peerova_free` kế tiếp trên cùng node** — copy nếu cần giữ
(mọi binding ngôn ngữ đã copy sẵn).

### Hàm

| Hàm | Mô tả | Trả về / lỗi |
|---|---|---|
| `peerova_node* peerova_server_start(const char* config_json)` | Khởi động server. `config_json` có thể `NULL` (mặc định). **Chặn tới ~3 s** chờ ticket — đừng gọi trên UI/game thread. | Node, hoặc `NULL` khi lỗi (xem `peerova_last_error_code`). Giải phóng bằng `peerova_free`. |
| `peerova_node* peerova_client_connect(const char* ticket, const char* config_json)` | Tạo client tới server. `ticket` = ticket `pvt_...` hoặc connect code 6 ký tự. Với ticket: trả về ngay, kết quả đến qua sự kiện `CONNECTED` / `CONNECT_FAILED`. | Node, hoặc `NULL` (ticket sai định dạng, mã không tồn tại...). |
| `int32_t peerova_ticket(peerova_node*, char* buf, int32_t buf_len)` | Copy ticket của server vào `buf`. Gọi với `buf=NULL,0` để lấy kích thước. | Kích thước cần (gồm NUL); `0` nếu không có (client). Nếu `buf_len` nhỏ hơn thì không copy gì. |
| `int32_t peerova_room_code(peerova_node*, char* buf, int32_t buf_len)` | Như trên cho connect code. Mã đến sau khi website cấp (vài giây sau start) — gọi lại định kỳ. | Kích thước cần, hoặc `0` nếu chưa có / không cấu hình `app`+`share`. |
| `int32_t peerova_send(node, uint32_t peer, uint8_t channel, const uint8_t* data, uint32_t len, int32_t reliable)` | Gửi tới `peer`. `reliable=1`: có thứ tự, gửi lại, kích thước lớn được. `reliable=0`: datagram ≤ 1180 byte. SDK copy dữ liệu trước khi trả về. | `0` thành công; `-1` lỗi (`E_UNKNOWN_PEER`, `E_TOO_LARGE`, `E_INVALID_ARG`). |
| `int32_t peerova_poll(node, peerova_event* out)` | Không chặn. Lấy một sự kiện nếu có. | `1` và điền `*out`, hoặc `0`. |
| `int32_t peerova_poll_wait(node, peerova_event* out, uint32_t timeout_ms)` | Như `poll` nhưng chờ tối đa `timeout_ms`. | `1` hoặc `0` (hết giờ). |
| `void peerova_disconnect(node, uint32_t peer)` | Đóng kết nối tới `peer` (server: đá client; client: rời server). Phía bên kia nhận `DISCONNECTED`. | — |
| `uint32_t peerova_peer_count(node)` | Số peer đang kết nối. | — |
| `void peerova_free(node)` | Đóng mọi kết nối, dừng node, giải phóng. `NULL` an toàn. Sau đó không dùng con trỏ nữa. | — |
| `const char* peerova_last_error(void)` | Thông điệp lỗi cuối **của thread hiện tại**, tiếng Anh, dạng `"E_TOO_LARGE: unreliable messages are limited to 1180 bytes"`. Đọc ngay sau lời gọi lỗi, trên cùng thread. Không free. Để xử lý theo loại lỗi, dùng `peerova_last_error_code` thay vì parse chuỗi. | Chuỗi tĩnh tới lần gọi kế tiếp. |
| `int32_t peerova_last_error_code(void)` | Mã lỗi ổn định (`PEEROVA_E_*`, xem [Mã lỗi](#mã-lỗi)) của lời gọi lỗi cuối trên thread hiện tại. `server_start`, `client_connect`, `send` đặt lại về `E_OK` khi bắt đầu. | `PEEROVA_E_*`. |
| `const char* peerova_version(void)` | Phiên bản thư viện, ví dụ `"0.1.0"`. Không free. | — |

### Mã lỗi

Mã ổn định (không đổi giữa các phiên bản; có thể thêm mã mới). `peerova_last_error_code()` trả về giá trị;
`peerova_last_error()` và lý do của `CONNECT_FAILED` / `DISCONNECTED` là văn bản tiếng Anh bắt đầu bằng tên mã,
ví dụ `"E_TIMEOUT: could not connect: timed out. ..."`. Hằng số C: `PEEROVA_E_TOO_LARGE`…; C#: `ErrorCode.TooLarge` /
`PeerovaException.Code` / `Endpoint.LastErrorCode` / `Event.ErrorCode`; Java/Kotlin: `Peerova.E_TOO_LARGE` / `PeerovaException.code` /
`Event.errorCode()`; Python: `peerova.E_TOO_LARGE` / `PeerovaError.code` / `Event.error_code`; Go: `peerova.CodeTooLarge` /
`peerova.CodeOf(err)` / `Event.ErrorCode()`; Node.js: `ErrorCode` / `PeerovaError.code === "E_TOO_LARGE"` / `connectFailed(reason, code)`;
sidecar `stdio`: trường `"code":"E_TIMEOUT"` trong sự kiện.

| Mã | Giá trị | Ý nghĩa |
|---|---|---|
| `E_OK` | 0 | Không có lỗi. |
| `E_INVALID_ARG` | 1 | Tham số/cấu hình sai: con trỏ NULL, JSON hỏng, connect code sai định dạng. |
| `E_TOO_LARGE` | 2 | Gói unreliable quá 1180 byte — chia nhỏ hoặc gửi reliable. |
| `E_UNKNOWN_PEER` | 3 | `peer` không (còn) kết nối. |
| `E_CLOSED` | 4 | Kết nối đã đóng (lý do của `DISCONNECTED`). |
| `E_CODE_NOT_FOUND` | 5 | Connect code 6 ký tự không tồn tại hoặc phòng đã đóng. |
| `E_NETWORK` | 6 | Lỗi mạng/dịch vụ: socket, cổng đang bận, không tới được website Peerova. |
| `E_TIMEOUT` | 7 | Hết thời gian: kết nối thất bại, server chưa sẵn sàng, peer ngừng phản hồi. Chạy `peerova doctor`. |
| `E_LICENSE` | 8 | License sai, hết hạn hoặc bị từ chối. |
| `E_CCU_LIMIT` | 9 | Server đã đạt giới hạn số client đồng thời. |
| `E_ACCESS_DENIED` | 10 | Token sai, hết hạn hoặc bị thu hồi. |
| `E_INVALID_TICKET` | 11 | Ticket sai định dạng, hỏng hoặc phiên bản cũ. |
| `E_IO` | 12 | Không đọc/ghi được file key (`key_file`) hoặc file hỏng (thư mục chứa file phải tồn tại). |
| `E_INTERNAL` | 99 | Lỗi nội bộ/chưa phân loại — báo cho Peerova kèm phiên bản. |

### Cấu hình (JSON)

Server:

| Khoá | Mặc định | Ý nghĩa |
|---|---|---|
| `port` | `47000` | Cổng UDP lắng nghe; `0` = ngẫu nhiên. |
| `key_file` | `"server.key"` | File danh tính server; tạo mới nếu chưa có. Cùng key ⇒ cùng ticket. **Bí mật.** |
| `license` | — | Key `PVA-...`. Không có ⇒ dev mode (tối đa 3 client). |
| `max_ccu` | theo license | Giới hạn số client đồng thời (không vượt license). |
| `token` | — | Bí mật chung; client phải gửi đúng `token` mới vào được. |
| `upnp` | `true` | Thử mở port trên router (UPnP/NAT-PMP). |
| `private` | `false` | Ticket không chứa địa chỉ IP của server. |
| `hide_ip` | `false` | Ẩn IP máy chủ: người chơi không bao giờ biết IP thật của máy chủ (chống DDoS); ping cao hơn một chút. Client kết nối qua đường mã hoá tự động. Gói Pro/Studio/Enterprise và bản dùng thử. |
| `app` | — | Project id trên website — cần để có connect code. |
| `share` | — | `{"name":"Phòng của Nam","public":false,"max":8,"mode":"coop","region":"VN"}` — thông tin hiển thị kèm connect code; `public` = hiện trong danh sách phòng công khai. |

Client:

| Khoá | Mặc định | Ý nghĩa |
|---|---|---|
| `token` | — | Phải khớp server. |
| `timeout_ms` | `20000` | Thời gian tối đa trước `CONNECT_FAILED`. |
| `key_file` | — (key ngẫu nhiên mỗi lần chạy) | File danh tính của client; tạo mới nếu chưa có (thư mục phải tồn tại). Giữ file này thì người chơi giữ cùng danh tính giữa các lần chạy, và server thấy họ ở cùng một địa chỉ ổn định `127.a.b.c` (server cấp địa chỉ theo subject của token, nếu không có thì theo key này; chủ server có thể chặn theo địa chỉ đó). Game nên lưu trong thư mục dữ liệu ứng dụng của người dùng, ví dụ `"<appdata>/<game>/peerova-client.key"`. Chặn theo địa chỉ chỉ có tác dụng với từng người chơi khi họ còn giữ file này — muốn chắc chắn hãy dùng tài khoản / access token. |

## 11. Luồng & vòng poll theo engine

- Mỗi node có mutex nội bộ: `send`, `poll`, `poll_wait`, `disconnect`, `peer_count`, `ticket` gọi được từ bất kỳ thread nào.
  Nhiều node độc lập chạy song song được (ví dụ server và client trong cùng process để test).
- **Không** gọi `peerova_free` khi thread khác đang dùng cùng node. Dừng vòng poll trước, rồi mới free.
- `peerova_poll_wait` giữ khoá trong khi chờ: để gửi từ thread khác với độ trễ thấp, dùng timeout ngắn (10–100 ms) hoặc `poll` không chặn.
- `peerova_last_error` / `_code` theo từng thread — đọc ngay sau lời gọi lỗi trên cùng thread (binding đã làm sẵn).
- Mỗi lần poll, rút **hết** hàng đợi (lặp tới khi trả về 0).

| Môi trường | Vòng poll |
|---|---|
| Unity | `PeerovaBehaviour` poll trong `Update()` và phát sự kiện trên main thread; `Host()` khởi động server trên thread nền. |
| Unreal | `UPeerovaSubsystem` poll trong `Tick`; `Host()` chạy server trên thread nền. |
| Godot | `PeerovaNode` poll trong `_process`, phát signal. |
| Flutter | `poll()` không chặn từ `Timer.periodic` trên isolate UI (xem [`dart/README.md`](dart/README.md)). |
| Android / JVM | thread nền poll có timeout (Kotlin: `startPolling { … }` hoặc `eventFlow()`); cập nhật UI qua main thread. |
| Server dedicated (mọi ngôn ngữ) | một thread chạy `poll_wait(100)` trong vòng lặp; gửi thoải mái từ thread khác. |
| Node.js / sidecar | sự kiện đến bất đồng bộ (EventEmitter / dòng JSON); không cần vòng poll. |

## 12. Mẫu dùng

### Game
- Input/vị trí: unreliable, 10–30 Hz, gói nhỏ có số thứ tự / timestamp, bên nhận bỏ gói cũ hơn gói đã có.
- Sự kiện quan trọng (bắn trúng, nhặt đồ, chat, vào/ra phòng): reliable trên channel riêng.
- Host-authoritative: client gửi input lên server, server tính và phát trạng thái xuống mọi client.

### Ứng dụng
- **Chat**: reliable, channel 0, mỗi tin một message.
- **Truyền file**: reliable, chia khúc 16–64 KB trên một channel riêng (để chat không phải chờ), gửi header (tên, kích thước, hash) trước,
  xác nhận cuối file. Giới hạn tốc độ đẩy khúc để không chiếm hết bộ nhớ hàng đợi.
- **Điều khiển từ xa**: lệnh = reliable; luồng màn hình/telemetry tần suất cao = unreliable hoặc reliable trên channel riêng nếu không được mất.

## 13. Hiệu năng
- Gộp nhiều cập nhật nhỏ trong cùng frame thành một message thay vì gửi từng cái.
- Unreliable: giữ dưới 1180 byte; gửi trạng thái mới nhất, không gửi lại dữ liệu cũ.
- Reliable: message lớn được, nhưng message cực lớn làm trễ các message sau **trên cùng channel** — tách channel.
- Encode nhị phân (struct cố định, MessagePack, protobuf) thay vì JSON cho gói tần suất cao.
- Sidecar (`peerova stdio`, Node.js, PHP/Lua không FFI) thêm vài ms + base64 mỗi gói; game cần độ trễ thấp nên dùng native FFI.
- Chuyển tiếp cổng (`peerova client --forward`) thêm rất ít độ trễ (chỉ qua `127.0.0.1`); dùng `--forward udp:…` cho gameplay UDP.
- `--hide-ip` tăng ping một chút — chỉ bật khi cần chống DDoS.
- Mở port UDP của máy chủ (mặc định 47000) hoặc bật UPnP giúp kết nối trực tiếp nhanh hơn; không bắt buộc.

## 14. Bảo mật
- Toàn bộ lưu lượng mã hoá đầu-cuối, kể cả khi đi đường mã hoá tự động; client xác thực server bằng khoá trong ticket.
- `server.key` (`key_file`) là danh tính server: ai có file này giả mạo được server. Không commit, không gửi đi, phân quyền chỉ đọc cho user chạy server.
  (Chép nó sang các máy của chính bạn để dùng ticket gộp là đúng mục đích.)
- Ticket/connect code **không phải bí mật** — nhúng vào game thoải mái; ai có nó thì thử kết nối được. Giới hạn người vào: `token`,
  token theo người dùng (`--access-key`), hoặc kiểm tra tài khoản ở tầng game sau `CONNECTED`.
- `license` và `token` là bí mật: không nhúng license vào bản client phát hành. Token đặt trên dòng lệnh có thể bị user khác trên cùng máy thấy — dùng `--config`.
- Máy chủ không trả lời gói nào thiếu ticket hợp lệ: quét port không thấy gì.
- Chỉ expose đúng các cổng cần thiết; tách dịch vụ quản trị (Remote Desktop, SSH) sang process riêng có token (§7.2).
- File key của client (`key_file`) không cần giữ bí mật với máy chủ, nhưng là danh tính của người chơi — đừng chia sẻ giữa các người chơi.

## 15. Khắc phục sự cố

| Triệu chứng | Cách xử lý |
|---|---|
| `CONNECT_FAILED` / `E_TIMEOUT` | Chạy `peerova doctor` trên cả hai máy (chẩn đoán IPv6, UPnP, NAT, CGNAT). Kiểm tra máy chủ đang chạy, ticket đúng `key_file` hiện tại, `token` khớp. |
| Chỉ 3 người vào được / `E_CCU_LIMIT` | Server ở dev mode — thêm `license`, hoặc đã đạt `max_ccu` của gói. |
| Ticket đổi mỗi lần chạy | Server tạo `key_file` mới (đường dẫn tạm hoặc khác thư mục làm việc). Dùng đường dẫn tuyệt đối cố định. |
| `E_IO` | Thư mục chứa `key_file` không tồn tại, không có quyền ghi, hoặc file hỏng. |
| `E_TOO_LARGE` | Gói unreliable > 1180 byte — dùng reliable hoặc chia nhỏ. |
| Máy chủ không khởi động khi bật `hide_ip` / `--hide-ip` | Gói hiện tại không có ẩn IP (cần Pro/Studio/Enterprise hoặc bản dùng thử, và `license` hợp lệ). |
| `peerova client` báo không mở được cổng | Cổng local đang bị chương trình khác dùng — chọn cổng khác: `--forward tcp:38888=8888`. |
| Game không thấy người chơi riêng biệt (`127.0.0.1` cho mọi người) | Game server phải chạy cùng máy với `peerova server` và đích `--expose` là `127.0.0.1` (mặc định); hỗ trợ Windows và Linux. |
| Không tìm thấy thư viện | Đặt `libpeerova.so` / `peerova.dll` / `libpeerova.dylib` đúng chỗ (xem README của binding) hoặc biến môi trường `PEEROVA_LIB` (binding nào hỗ trợ có ghi rõ). |
| Không chắc file tải về là bản chính thức | `peerova verify <file.zip>` (kiểm tra chữ ký + SHA-256). |

## 16. FAQ
- **Người chơi có phải gõ mã/ticket không?** Không. Nhúng ticket vào game hoặc tải từ URL của bạn (§2); mã 6 ký tự chỉ để mời bạn bè vào phòng do người chơi mở.
- **Có cần mở port / IP tĩnh không?** Không. Kết nối tự động; mở port chỉ giúp nhanh hơn.
- **Game có sẵn không có source code, dùng được không?** Được — §3 (người chơi chạy một lệnh) hoặc §4 (launcher chạy ẩn).
- **Ticket có phải bí mật không?** Không; `server.key`, `license`, `token` mới là bí mật.
- **Đổi máy chủ có phải phát hành bản mới?** Không, nếu game tải ticket từ URL của bạn khi khởi động (§2), hoặc chép `server.key` sang máy mới.
- **Mỗi process `peerova server` tính là một server?** Có — mỗi process (ticket riêng) là một server trong license.
- **Có cấm được người chơi gian lận?** Có: `--banned` theo địa chỉ `127.a.b.c` (§7.3); chắc chắn hơn khi dùng tài khoản/token theo người dùng.
- **Windows có cần `wintun.dll`?** Không cho SDK hay chuyển tiếp cổng. Chỉ chế độ VPN (`--vpn`) cần `wintun.dll` cạnh `peerova.exe`.
- **Android / iOS / macOS?** Liên hệ Peerova để nhận thư viện cho các nền tảng này; vị trí đặt file có trong README từng binding.
  Mobile không chạy được process con — dùng SDK.

---

# English

**Contents:** [1. Choosing an integration](#1-choosing-an-integration) ·
[2. Players never type anything](#2-players-never-type-anything) ·
[3. Existing game, no code change](#3-existing-game-no-code-change) ·
[4. The game runs peerova hidden](#4-the-game-runs-peerova-hidden-players-run-nothing) ·
[5. New game with the SDK](#5-new-game-with-the-sdk) ·
[6. Host-in-game](#6-host-in-game-one-player-hosts) ·
[7. Game-server features](#7-game-server-features) ·
[8. Concepts](#8-concepts) · [9. Lifecycle](#9-lifecycle) · [10. C API reference](#10-c-api-reference-includepeerovah) ·
[Error codes](#error-codes) · [11. Threading & poll loops](#11-threading--poll-loops-per-engine) · [12. Patterns](#12-patterns) ·
[13. Performance](#13-performance-tips) · [14. Security](#14-security) · [15. Troubleshooting](#15-troubleshooting) · [16. FAQ](#16-faq-1)

Peerova connects two (or more) machines directly, end-to-end encrypted, without port forwarding or a public IP.
Connections are automatic: direct first; when a network blocks direct connections Peerova uses the **automatic encrypted route**
(still end-to-end encrypted) and upgrades to direct as soon as it can. Your application does nothing extra.

The SDK package contains two things, usable alone or together:

- **The `peerova` command-line tool** (`peerova.exe` on Windows): shares the TCP/UDP ports of an existing program
  (`peerova server --expose …`) and opens them on another machine at `127.0.0.1` (`peerova client … --forward …`). No code changes.
- **The SDK library** (`peerova.dll` / `libpeerova.so` / `libpeerova.dylib` + bindings for C#, Unity, Unreal, Java, Kotlin, Python, Go,
  Node.js, Dart/Flutter, PHP, Lua, C/C++…): send/receive reliable/unreliable messages inside your game or app.

## 1. Choosing an integration

| # | Situation | Use | What players do | Read |
|---|---|---|---|---|
| 1 | **Existing** game/app, no code change | Server: `peerova server --expose tcp:8888`. Players: `peerova client <TICKET> --forward tcp:8888`, then the game connects to `127.0.0.1:8888` | Run one command (or a `.bat`/shortcut you ship) | [§3](#3-existing-game-no-code-change) |
| 2 | **Existing** game, players **must not run anything** | The game/launcher starts `peerova` hidden (no console window), waits for the ready line, then the existing socket code connects to `127.0.0.1:8888` | Nothing | [§4](#4-the-game-runs-peerova-hidden-players-run-nothing) |
| 3 | **New** game (or ready to replace the networking layer) | SDK in the client and/or server: reliable + unreliable messages, `CONNECTED`/`MESSAGE`/… events | Nothing | [§5](#5-new-game-with-the-sdk) |
| 4 | **Host-in-game** (one player hosts) | An SDK server runs inside the host's game; share the ticket or a 6-character code | Friends click "Join" / type a 6-character code | [§6](#6-host-in-game-one-player-hosts) |

Languages without a native binding (shell, Perl, Erlang, plain Lua 5.x…) or when you don't want a native module: use the
`peerova stdio` sidecar (JSON lines over stdin/stdout) — same features as the SDK, see [`stdio/README.md`](stdio/README.md).

Options 1 and 2 work with **any** TCP/UDP game or program (even one you have no source code for). Options 3 and 4 give the lowest
latency and full control (channels, reliable/unreliable, who connects, kicks).

## 2. Players never type anything

Players **never** need to type a ticket or a code. Pick one of these:

1. **Embed the ticket in the game.** The ticket (`pvt_...`) is tied to the server's `server.key` file: keep that file and the ticket you
   shipped keeps working across server restarts. A ticket is **not a secret** (like a server address): anyone who has it can try to
   connect, so to restrict who gets in use `--token` / accounts (see [§14](#14-security)). Get the ticket from the `TICKET` box printed
   at startup, or let the server write it with `--ticket-file ticket.txt`.
2. **Fetch the latest ticket from your own URL/API at startup** — change or add servers without shipping a patch.
   The server writes `--ticket-file ticket.txt`; you publish that file on your website; the game downloads it and falls back to the embedded ticket:
   ```csharp
   string ticket = EmbeddedTicket;                       // ticket embedded in the build
   try { ticket = (await new HttpClient { Timeout = TimeSpan.FromSeconds(5) }
             .GetStringAsync("https://mygame.example.com/peerova-ticket.txt")).Trim(); } catch { }
   ```
3. **Several servers with automatic failover.** Copy the **same** `server.key` to every machine, run
   `peerova server --key server.key --ticket-file a.txt …` on each, then merge:
   ```sh
   peerova ticket merge @a.txt @b.txt @c.txt > ticket.txt
   ```
   The merged ticket contains every machine: clients try them all at once and land on one that is alive. A player on a machine that
   goes down is disconnected; the next connect (CLI: `--reconnect`) picks another machine. Game state across machines is yours to sync.
   Each running machine counts as one server in the license.
4. **6-character connect codes — only for "invite a friend".** Player-hosted rooms (§6) need something short to read over voice chat:
   a server started with `--app <project id> --share "Room name"` (SDK: `app` + `share`) gets a code like `K7Q2XD`; friends enter it
   (clients pass the code where a ticket goes). The code stops working when the room closes. For fixed servers, embed the ticket (options 1–3).
5. **Public room lists.** Add `--public` (SDK: `"share":{"public":true,…}`) and the room appears in the project's public list at
   `https://peerova.pages.dev/connect?app=<project id>` (the ticket is never shown). Your game can show the list itself with
   `GET https://peerova.pages.dev/api/v1/lobby/list?app=<project id>` (optional `&mode=…&region=…&q=…&limit=…`), which returns
   `{"lobbies":[{"code","name","players","max","mode","region","version","tags","age"}]}`; the player clicks a room → the game connects with its `code`.
   Displayed info: `--max N`, `--mode M`, `--region VN`, `--tag T` (CLI) or `share` (SDK).

## 3. Existing game, no code change

On the server (the machine running the game server, e.g. a game listening on TCP 8888 and UDP 8888):

```sh
peerova server --license PVA-... --expose tcp:8888 --expose udp:8888
```

On first run `server.key` is created in the current folder (keep it — it determines the ticket). The server prints a `TICKET`
box containing a `pvt_...` string. Send it to players (or embed it, §2).

Players:

```sh
peerova client pvt_... --forward tcp:8888 --forward udp:8888
```

Then in the game, connect to `127.0.0.1:8888`. Once connected the client prints (the tool's human output is in Vietnamese;
"ĐÃ KẾT NỐI" = "CONNECTED"):

```
[    0.02] ✔ ĐÃ KẾT NỐI TRỰC TIẾP tới 203.0.113.7:47000 [lan] — mã hoá E2E
[    0.02] ✔ Tcp 127.0.0.1:8888 → server port 8888
[    0.02] ✔ Udp 127.0.0.1:8888 → server port 8888
```

- `--forward PROTO:LOCAL[-LOCAL][=REMOTE]`: `tcp:8888` (local port 8888 → server port 8888), `tcp:18888=8888` (different local port),
  `udp:27015-27030` (port range). `--bind IP` changes the listen address (default `127.0.0.1`).
- `--key FILE`: the player's identity file (defaults to a file in the user's config folder). Keeping it keeps the player at the same
  stable address on the server (§7.3).
- `--reconnect`: reconnect automatically after a disconnect. `--token SECRET`: when the server requires a token.
- PROTO: `tcp http https ws wss ssh rdp grpc` (TCP) · `udp quic game` (UDP).
- Every option can go in a JSON file: `peerova server --config peerova.json`, e.g.
  `{"port":47000,"license":"PVA-...","expose":["tcp:8888","udp:8888"],"banned":"banned.txt"}`.
- Without a license the server runs in dev mode (at most 3 concurrent clients).

## 4. The game runs peerova hidden (players run nothing)

Ship `peerova.exe` (Windows) / `peerova` (Linux, macOS) **with the game**. When the player clicks "Play", the game or launcher:

1. starts `peerova client <TICKET> --forward tcp:8888 --key <appdata>/<game>/peerova-client.key` **hidden** (no console window);
2. reads stdout line by line and waits for the ready line (with a timeout, e.g. 30 s);
3. connects its **existing** socket code to `127.0.0.1:8888` — nothing else changes;
4. kills the `peerova` process when the game exits.

**Lines to wait for** (stdout is UTF-8; the text is Vietnamese):

| Line | Meaning |
|---|---|
| `✔ ĐÃ KẾT NỐI TRỰC TIẾP tới …` ("connected directly to …") or `✔ ĐÃ KẾT NỐI qua đường dự phòng tự động …` ("connected through the automatic route …") | Connected to the server. |
| `✔ Tcp 127.0.0.1:8888 → server port 8888` (one per `--forward`, printed right after the line above) | The local port is open — **ready**. The code below waits for the ASCII text **`server port`** (independent of console code pages). |
| `✘ …`, or the process exits (exit code `1`, reason on stderr starting with `lỗi:` = "error:") | Failure: wrong ticket, server unreachable, local port in use… |

With several `--forward` options, wait for that many `server port` lines. Other lines are logs — write them to your log file, don't parse them.
`peerova client` exits when the connection is lost (unless `--reconnect` is given) — watch for the exit to show "disconnected" and offer to retry.

General notes:
- The folder of `--key` must exist (the code below creates it). Store it in the user's app-data folder so the player keeps the same
  identity (address bans work, §7.3).
- Linux/macOS: the `peerova` file must be executable (`chmod +x`). macOS: codesign it with the app.
- Pick an unusual local port (e.g. `tcp:38888=8888`) if 8888 may already be used by another program on the player's machine.
- Always drain stdout/stderr (the code below does) — a full pipe can stall `peerova`.
- **`peerova stdio` does not fit this pattern**: the `stdio` sidecar does not open a `127.0.0.1` port; it exchanges SDK messages as
  JSON lines (structured `connected`/`message`/… events with a `code`). It suits you if you are willing to replace the game's networking with
  messages — see [`stdio/README.md`](stdio/README.md). To keep your socket code, use `peerova client` as below.

### C# (.NET, WinForms/WPF, console)
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

### Unity (Windows/Linux/macOS standalone)
Reuse the `PeerovaTunnel` class above (put `PeerovaTunnel.cs` in `Assets/Scripts/`). Mobile/consoles can't run child processes —
use the SDK (§5).
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

### Java (8+)
```java
// PeerovaTunnel.java — Java 8+. Runs the bundled `peerova` CLI and waits until the local port is open.
// Java starts console programs without a console window on Windows.
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class PeerovaTunnel implements AutoCloseable {
    private Process proc;

    /** Returns true once the forwarded port is open on 127.0.0.1; false on failure or timeout. */
    public boolean start(String exe, String ticket, String forward, Path keyFile, long timeoutMs)
            throws IOException, InterruptedException {
        Files.createDirectories(keyFile.toAbsolutePath().getParent());
        ProcessBuilder pb = new ProcessBuilder(exe, "client", ticket, "--forward", forward, "--key", keyFile.toString());
        pb.redirectErrorStream(true);                       // stdout + stderr in one stream
        proc = pb.start();
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));

        CountDownLatch ready = new CountDownLatch(1);
        Thread reader = new Thread(() -> {
            try (BufferedReader r = new BufferedReader(new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
                for (String line; (line = r.readLine()) != null; ) {
                    System.out.println("[peerova] " + line);
                    // "✔ Tcp 127.0.0.1:8888 → server port 8888" = connected and the local port is open
                    if (line.contains("server port")) ready.countDown();
                }
            } catch (IOException ignored) { }
        }, "peerova-output");
        reader.setDaemon(true);
        reader.start();

        long deadline = System.currentTimeMillis() + timeoutMs;
        while (!ready.await(100, TimeUnit.MILLISECONDS)) {
            if (!proc.isAlive() || System.currentTimeMillis() > deadline) { close(); return false; }
        }
        return true;
    }

    public boolean isAlive() { return proc != null && proc.isAlive(); }

    @Override public void close() { if (proc != null && proc.isAlive()) proc.destroyForcibly(); }
}
```
```java
boolean win = System.getProperty("os.name").startsWith("Windows");
String appData = win ? System.getenv("APPDATA") : System.getProperty("user.home") + "/.local/share";
Path key = java.nio.file.Paths.get(appData, "MyGame", "peerova-client.key");
String exe = win ? "peerova\\peerova.exe" : "peerova/peerova";   // next to your game

PeerovaTunnel tunnel = new PeerovaTunnel();
if (tunnel.start(exe, "pvt_...", "tcp:8888", key, 30_000)) {
    connectToGameServer("127.0.0.1", 8888);      // your existing socket code, unchanged
} else {
    showError("Could not reach the server");
}
```

### Node.js / Electron (main process)
```js
// peerova-tunnel.mjs — Node.js 18+ / Electron main process.
import { spawn } from "node:child_process";
import { createInterface } from "node:readline";
import { mkdirSync } from "node:fs";
import path from "node:path";

/** Runs `peerova client` hidden. `ready` resolves once the forwarded port is open on 127.0.0.1. */
export function startTunnel({ exe, ticket, forward, keyFile, timeoutMs = 30000, onLog = () => {} }) {
  mkdirSync(path.dirname(keyFile), { recursive: true });
  const child = spawn(exe, ["client", ticket, "--forward", forward, "--key", keyFile], {
    windowsHide: true,                       // no console window on Windows
    stdio: ["ignore", "pipe", "pipe"],
  });
  const stop = () => { if (child.exitCode === null && child.signalCode === null) child.kill(); };
  process.once("exit", stop);

  const ready = new Promise((resolve, reject) => {
    const timer = setTimeout(() => { stop(); reject(new Error("peerova: timed out")); }, timeoutMs);
    for (const stream of [child.stdout, child.stderr]) {
      createInterface({ input: stream }).on("line", (line) => {
        onLog(line);
        // "✔ Tcp 127.0.0.1:8888 → server port 8888" = connected and the local port is open
        if (line.includes("server port")) { clearTimeout(timer); resolve(); }
      });
    }
    child.once("error", (e) => { clearTimeout(timer); reject(e); });
    child.once("exit", (code) => { clearTimeout(timer); reject(new Error(`peerova exited (${code})`)); });
  });
  return { child, ready, stop };
}
```
```js
// Electron main process. Ship the CLI with electron-builder "extraResources": [{ "from": "bin", "to": "bin" }]
// (it must not be inside app.asar).
import { app } from "electron";
import path from "node:path";
import { startTunnel } from "./peerova-tunnel.mjs";

const exe = path.join(app.isPackaged ? process.resourcesPath : app.getAppPath(), "bin",
  process.platform === "win32" ? "peerova.exe" : "peerova");
const tunnel = startTunnel({
  exe, ticket: "pvt_...", forward: "tcp:8888",
  keyFile: path.join(app.getPath("userData"), "peerova-client.key"),
  onLog: (l) => console.log("[peerova]", l),
});
app.on("before-quit", tunnel.stop);
tunnel.child.on("exit", () => { /* tunnel closed: show "disconnected", offer to reconnect */ });

await tunnel.ready;             // throws on failure / timeout
connectToGameServer("127.0.0.1", 8888);   // your existing socket code, unchanged
```

### Python (3.8+)
```python
# peerova_tunnel.py — Python 3.8+. Runs the bundled `peerova` CLI hidden and waits until the local port is open.
import atexit, os, subprocess, sys, threading, time

def start_tunnel(exe, ticket, forward, key_file, timeout=30.0, on_log=print):
    """Returns the running Popen once the forwarded port is open on 127.0.0.1, or None on failure/timeout."""
    os.makedirs(os.path.dirname(os.path.abspath(key_file)), exist_ok=True)
    flags = subprocess.CREATE_NO_WINDOW if sys.platform == "win32" else 0   # no console window
    p = subprocess.Popen(
        [exe, "client", ticket, "--forward", forward, "--key", key_file],
        stdin=subprocess.DEVNULL, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
        text=True, encoding="utf-8", errors="replace", creationflags=flags)
    atexit.register(stop_tunnel, p)
    ready = threading.Event()

    def reader():
        for line in p.stdout:
            on_log(line.rstrip())
            # "✔ Tcp 127.0.0.1:8888 → server port 8888" = connected and the local port is open
            if "server port" in line:
                ready.set()
    threading.Thread(target=reader, daemon=True).start()

    deadline = time.monotonic() + timeout
    while not ready.wait(0.1):
        if p.poll() is not None or time.monotonic() > deadline:
            stop_tunnel(p)
            return None
    return p

def stop_tunnel(p):
    if p.poll() is None:
        p.kill()
        p.wait()
```
```python
import os, sys
from peerova_tunnel import start_tunnel

here = os.path.dirname(os.path.abspath(sys.argv[0]))
exe = os.path.join(here, "peerova.exe" if sys.platform == "win32" else "peerova")
appdata = os.environ.get("APPDATA") or os.path.expanduser("~/.local/share")
key = os.path.join(appdata, "MyGame", "peerova-client.key")

tunnel = start_tunnel(exe, "pvt_...", "tcp:8888", key)
if tunnel:
    connect_to_game_server("127.0.0.1", 8888)    # your existing socket code, unchanged
else:
    show_error("Could not reach the server")
```

### Go
```go
// peerovatunnel/tunnel.go — runs the bundled `peerova` CLI hidden and waits until the local port is open.
package peerovatunnel

import (
	"bufio"
	"errors"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"time"
)

type Tunnel struct {
	cmd  *exec.Cmd
	done chan struct{}
}

// Start returns once the forwarded port is open on 127.0.0.1.
func Start(exe, ticket, forward, keyFile string, timeout time.Duration, onLog func(string)) (*Tunnel, error) {
	if err := os.MkdirAll(filepath.Dir(keyFile), 0o700); err != nil {
		return nil, err
	}
	cmd := exec.Command(exe, "client", ticket, "--forward", forward, "--key", keyFile)
	hideWindow(cmd)
	out, err := cmd.StdoutPipe()
	if err != nil {
		return nil, err
	}
	cmd.Stderr = cmd.Stdout // same pipe for stdout + stderr
	if err := cmd.Start(); err != nil {
		return nil, err
	}
	t := &Tunnel{cmd: cmd, done: make(chan struct{})}
	ready := make(chan struct{})
	go func() {
		sc := bufio.NewScanner(out)
		signalled := false
		for sc.Scan() {
			line := sc.Text()
			if onLog != nil {
				onLog(line)
			}
			// "✔ Tcp 127.0.0.1:8888 → server port 8888" = connected and the local port is open
			if !signalled && strings.Contains(line, "server port") {
				signalled = true
				close(ready)
			}
		}
		_ = cmd.Wait()
		close(t.done)
	}()
	select {
	case <-ready:
		return t, nil
	case <-t.done:
		return nil, errors.New("peerova exited before connecting")
	case <-time.After(timeout):
		t.Stop()
		return nil, errors.New("peerova: timed out")
	}
}

// Done is closed when the tunnel process ends (connection lost or Stop).
func (t *Tunnel) Done() <-chan struct{} { return t.done }

func (t *Tunnel) Stop() {
	if t.cmd.Process != nil {
		_ = t.cmd.Process.Kill()
	}
}
```
```go
// peerovatunnel/hide_windows.go
//go:build windows

package peerovatunnel

import (
	"os/exec"
	"syscall"
)

func hideWindow(c *exec.Cmd) {
	c.SysProcAttr = &syscall.SysProcAttr{HideWindow: true, CreationFlags: 0x08000000} // CREATE_NO_WINDOW
}
```
```go
// peerovatunnel/hide_other.go
//go:build !windows

package peerovatunnel

import "os/exec"

func hideWindow(*exec.Cmd) {}
```
```go
exe := "./peerova"
if runtime.GOOS == "windows" {
	exe = `.\peerova.exe`
}
cfg, _ := os.UserConfigDir() // %APPDATA% on Windows, ~/.config on Linux
key := filepath.Join(cfg, "MyGame", "peerova-client.key")

t, err := peerovatunnel.Start(exe, "pvt_...", "tcp:8888", key, 30*time.Second, func(l string) { log.Println("[peerova]", l) })
if err != nil {
	log.Fatal(err)
}
defer t.Stop()
conn, err := net.Dial("tcp", "127.0.0.1:8888") // your existing socket code, unchanged
```

### C++ (17, Windows + Linux/macOS)
On Windows this class puts `peerova` in a *job object*: if the game exits or crashes, Windows stops `peerova` automatically.
```cpp
// peerova_tunnel.hpp — C++17, Windows + Linux/macOS. Runs the bundled `peerova` CLI hidden
// and waits until the local port is open. Header-only, no dependencies.
#pragma once
#include <chrono>
#include <condition_variable>
#include <functional>
#include <initializer_list>
#include <mutex>
#include <string>
#include <thread>
#include <vector>
#ifdef _WIN32
#include <windows.h>
#else
#include <csignal>
#include <spawn.h>
#include <sys/wait.h>
#include <unistd.h>
extern char** environ;
#endif

class PeerovaTunnel {
public:
    std::function<void(const std::string&)> onLog;   // every output line (background thread)

    ~PeerovaTunnel() { stop(); }

    // keyFile's folder must exist. Returns true once the forwarded port is open on 127.0.0.1.
    bool start(const std::string& exe, const std::string& ticket, const std::string& forward,
               const std::string& keyFile, int timeoutMs = 30000) {
        if (!spawn(exe, {"client", ticket, "--forward", forward, "--key", keyFile})) return false;
        reader_ = std::thread([this] { readLoop(); });
        std::unique_lock<std::mutex> lk(m_);
        cv_.wait_for(lk, std::chrono::milliseconds(timeoutMs), [this] { return ready_ || ended_; });
        bool ok = ready_;
        lk.unlock();
        if (!ok) stop();
        return ok;
    }

    bool running() { std::lock_guard<std::mutex> g(m_); return ready_ && !ended_; }

    void stop() {
        terminate();
        if (reader_.joinable()) reader_.join();
        closePipe();
    }

private:
    std::thread reader_;
    std::mutex m_;
    std::condition_variable cv_;
    bool ready_ = false, ended_ = false;

    void onLine(const std::string& line) {
        if (onLog) onLog(line);
        // "✔ Tcp 127.0.0.1:8888 → server port 8888" = connected and the local port is open
        if (line.find("server port") != std::string::npos) {
            std::lock_guard<std::mutex> g(m_);
            ready_ = true;
            cv_.notify_all();
        }
    }
    void readLoop() {
        std::string buf;
        char chunk[512];
        for (long n; (n = readSome(chunk, sizeof chunk)) > 0;) {
            buf.append(chunk, (size_t)n);
            for (size_t p; (p = buf.find('\n')) != std::string::npos; buf.erase(0, p + 1)) {
                std::string line = buf.substr(0, p);
                if (!line.empty() && line.back() == '\r') line.pop_back();
                onLine(line);
            }
        }
        std::lock_guard<std::mutex> g(m_);
        ended_ = true;
        cv_.notify_all();
    }

#ifdef _WIN32
    HANDLE proc_ = nullptr, out_ = nullptr, job_ = nullptr;
    static std::wstring widen(const std::string& s) {
        int n = MultiByteToWideChar(CP_UTF8, 0, s.c_str(), -1, nullptr, 0);
        std::wstring w(n, L'\0');
        MultiByteToWideChar(CP_UTF8, 0, s.c_str(), -1, &w[0], n);
        w.pop_back();
        return w;
    }
    bool spawn(const std::string& exe, std::initializer_list<std::string> args) {
        SECURITY_ATTRIBUTES sa{sizeof sa, nullptr, TRUE};
        HANDLE rd, wr;
        if (!CreatePipe(&rd, &wr, &sa, 0)) return false;
        SetHandleInformation(rd, HANDLE_FLAG_INHERIT, 0);
        std::wstring cmd = L"\"" + widen(exe) + L"\"";
        for (const auto& a : args) cmd += L" \"" + widen(a) + L"\"";
        STARTUPINFOW si{};
        si.cb = sizeof si;
        si.dwFlags = STARTF_USESTDHANDLES;
        si.hStdOutput = wr;
        si.hStdError = wr;
        PROCESS_INFORMATION pi{};
        BOOL ok = CreateProcessW(nullptr, &cmd[0], nullptr, nullptr, TRUE,
                                 CREATE_NO_WINDOW | CREATE_SUSPENDED, nullptr, nullptr, &si, &pi);
        CloseHandle(wr);
        if (!ok) { CloseHandle(rd); return false; }
        // Job object: Windows kills peerova automatically if the game exits or crashes.
        job_ = CreateJobObjectW(nullptr, nullptr);
        JOBOBJECT_EXTENDED_LIMIT_INFORMATION li{};
        li.BasicLimitInformation.LimitFlags = JOB_OBJECT_LIMIT_KILL_ON_JOB_CLOSE;
        SetInformationJobObject(job_, JobObjectExtendedLimitInformation, &li, sizeof li);
        AssignProcessToJobObject(job_, pi.hProcess);
        ResumeThread(pi.hThread);
        CloseHandle(pi.hThread);
        proc_ = pi.hProcess;
        out_ = rd;
        return true;
    }
    long readSome(char* b, size_t n) {
        DWORD got = 0;
        return ReadFile(out_, b, (DWORD)n, &got, nullptr) ? (long)got : -1;
    }
    void terminate() {
        if (proc_) { TerminateProcess(proc_, 0); WaitForSingleObject(proc_, 2000); CloseHandle(proc_); proc_ = nullptr; }
        if (job_) { CloseHandle(job_); job_ = nullptr; }
    }
    void closePipe() { if (out_) { CloseHandle(out_); out_ = nullptr; } }
#else
    pid_t pid_ = -1;
    int out_ = -1;
    bool spawn(const std::string& exe, std::initializer_list<std::string> args) {
        int fd[2];
        if (pipe(fd) != 0) return false;
        posix_spawn_file_actions_t fa;
        posix_spawn_file_actions_init(&fa);
        posix_spawn_file_actions_adddup2(&fa, fd[1], 1);
        posix_spawn_file_actions_adddup2(&fa, fd[1], 2);
        posix_spawn_file_actions_addclose(&fa, fd[0]);
        posix_spawn_file_actions_addclose(&fa, fd[1]);
        std::vector<std::string> all{exe};
        all.insert(all.end(), args);
        std::vector<char*> argv;
        for (auto& s : all) argv.push_back(&s[0]);
        argv.push_back(nullptr);
        int rc = posix_spawn(&pid_, exe.c_str(), &fa, nullptr, argv.data(), environ);
        posix_spawn_file_actions_destroy(&fa);
        close(fd[1]);
        if (rc != 0) { close(fd[0]); pid_ = -1; return false; }
        out_ = fd[0];
        return true;
    }
    long readSome(char* b, size_t n) { return (long)read(out_, b, n); }
    void terminate() {
        if (pid_ > 0) { kill(pid_, SIGTERM); waitpid(pid_, nullptr, 0); pid_ = -1; }
    }
    void closePipe() { if (out_ >= 0) { close(out_); out_ = -1; } }
#endif
};
```
```cpp
#include <cstdio>
#include <cstdlib>
#include "peerova_tunnel.hpp"

int main() {
#ifdef _WIN32
    std::string exe = "peerova.exe";                                    // next to your game .exe
    std::string key = std::string(std::getenv("APPDATA")) + "\\MyGame\\peerova-client.key";
#else
    std::string exe = "./peerova";
    std::string key = std::string(std::getenv("HOME")) + "/.local/share/MyGame/peerova-client.key";
#endif
    // create the key file's folder first (std::filesystem::create_directories)
    PeerovaTunnel tunnel;
    tunnel.onLog = [](const std::string& l) { std::fprintf(stderr, "[peerova] %s\n", l.c_str()); };
    if (!tunnel.start(exe, "pvt_...", "tcp:8888", key)) {
        std::fprintf(stderr, "Could not reach the server\n");
        return 1;
    }
    connect_to_game_server("127.0.0.1", 8888);   // your existing socket code, unchanged
    // ... game loop; tunnel.running() turns false if the connection is lost
}   // ~PeerovaTunnel() stops peerova
```

## 5. New game with the SDK

Server (dedicated machine or a hosting player) and client share one API: `server_start` → ticket; `client_connect(ticket)`;
both `poll` events and `send(peer, channel, bytes, reliable)`.

```c
// Server
peerova_node* s = peerova_server_start("{\"port\":47000,\"key_file\":\"server.key\",\"license\":\"PVA-...\"}");
char ticket[1024]; peerova_ticket(s, ticket, sizeof ticket);           // embed / publish this ticket
for (;;) {
    peerova_event ev;
    while (peerova_poll_wait(s, &ev, 100)) {
        if (ev.type == PEEROVA_EVENT_CONNECTED)    { /* player ev.peer joined */ }
        if (ev.type == PEEROVA_EVENT_MESSAGE)      peerova_send(s, ev.peer, ev.channel, ev.data, ev.len, ev.reliable);
        if (ev.type == PEEROVA_EVENT_DISCONNECTED) { /* ev.peer left; reason in ev.data */ }
    }
}

// Client — key_file keeps the player's identity across runs
peerova_node* c = peerova_client_connect(ticket, "{\"key_file\":\"<appdata>/MyGame/peerova-client.key\"}");
// every frame: while (peerova_poll(c, &ev)) { ... }   CONNECTED → start sending
peerova_send(c, server_peer, 0, (const uint8_t*)"hello", 5, 1);        // reliable, channel 0
peerova_send(c, server_peer, 1, input, input_len, 0);                  // unreliable ≤ 1180 bytes
```

- **Reliable**: ordered per channel, retransmitted, any size (the SDK fragments/reassembles) — chat, items, RPCs, join/leave.
- **Unreliable**: datagram, lowest latency, may be lost/reordered, **max 1180 bytes** — input, positions, realtime state.
- Channels 0..255: ordering is per channel; a stalled channel never blocks another.
- Every binding wraps exactly this API; see the README for your language: [C/C++](c/README.md), [C#](csharp/README.md), [Unity](unity/README.md),
  [Unreal](unreal/README.md), [Java](java/README.md), [Kotlin/Android](kotlin-android/README.md), [Python](python/README.md), [Go](go/README.md),
  [Node.js](nodejs/README.md), [Dart/Flutter](dart/README.md), [PHP](php/README.md), [Lua](lua/README.md), [stdio](stdio/README.md).
- Use matching pairs: an SDK client connects to an SDK server (or `peerova stdio server`); `peerova client --forward` connects to `peerova server --expose`.

## 6. Host-in-game (one player hosts)

The host's game runs an SDK server inside the game; friends run an SDK client.

1. The host clicks "Host": call `server_start` **off the main thread** (it blocks up to ~3 s). The Unity/Unreal/Godot bindings have a ready-made `Host()`.
   Give the host its own `key_file` (e.g. `<appdata>/<game>/host.key`) so the room keeps a stable ticket.
2. How friends join:
   - through your friends/lobby system (Steam, Discord, your backend): send the **ticket** — the game connects by itself, nobody types;
   - or a **6-character code**: configure `{"app":"prj_...","share":{"name":"Nam's room","max":4}}`; read the code with `peerova_room_code`
     (it arrives a few seconds after start — poll it) and show it on screen; friends enter the code → `client_connect("K7Q2XD")`
     (blocks during the lookup — call it off the UI thread; an unknown code fails with `E_CODE_NOT_FOUND`);
   - add `"public":true` to list the room in the public room list (§2).
3. The host is both server and player: the host's game applies its own input directly and other players' input from `MESSAGE` events.

Without a license the server runs in dev mode: at most 3 concurrent clients (host + 3 friends). For larger player-hosted rooms, contact
Peerova about a suitable plan. **Never embed your license key in a shipped game.**

## 7. Game-server features

### 7.1 Several ports from one process
```sh
peerova server --license PVA-... --expose tcp:8888 --expose udp:8888 --expose udp:27015-27030
```
`--expose` is repeatable, accepts port ranges (up to 1024 ports per range) and other targets: `--expose tcp:8888=192.168.1.20:8888`.
One ticket reaches every exposed port; clients pick the ports they need with `--forward`.

### 7.2 Several processes (separate tickets) — example: Remote Desktop for admins + a public game
Each process needs its own port (`--port`) and key file (`--key`), so it has its own ticket; **each process counts as one server in the license**.
Split them when the audiences differ: whoever holds the game ticket can open every port of the game process — never expose Remote Desktop next to the game.

```sh
# Public game — ticket given to every player
peerova server --port 47000 --key game.key  --license PVA-... --expose tcp:8888 --expose udp:8888 --banned banned.txt --ticket-file game-ticket.txt

# Remote Desktop for admins only — token required, server IP hidden
peerova server --port 47001 --key admin.key --license PVA-... --expose rdp:3389 --token "long-random-secret" --hide-ip
```
Admin: `peerova client <ADMIN_TICKET> --forward tcp:13389=3389 --token "long-random-secret"`, then open Remote Desktop to `127.0.0.1:13389`.
Businesses: replace the shared `--token` with per-person tokens scoped to ports (`peerova keygen --access`, `peerova token issue`, `--access-key`).

### 7.3 Every player gets their own address & banning players
When the game server runs on the same machine as `peerova server` (Windows, Linux), the game sees each player as their own stable
`127.a.b.c` address — even when they switch networks (Wi-Fi ↔ mobile). In-game IP bans, per-IP limits and logs therefore work per player.
The server log prints the address when a player joins: `người chơi 9f43e9ec → IP trong game: 127.10.210.71` ("player … → in-game IP …").

Ban players without a restart:
```sh
peerova server --expose tcp:8888 --expose udp:8888 --banned banned.txt
```
`banned.txt`: one address per line (`127.10.210.71`), `#` starts a comment. The file is re-read every few seconds; a banned player is
disconnected at once and cannot come back.

The identity comes from the **client key file** (`peerova client --key`, SDK `key_file`): the address is stable only while the player keeps
that file — someone who deletes it on purpose gets a new address. For robust bans use accounts: issue per-user tokens
(`--access-key` + `peerova token issue --sub <user>`), which ties the address to the account, or check accounts in your game.

### 7.4 Hide the server IP
**Hide the server IP: players never learn its real address (DDoS protection); slightly higher latency.**
```sh
peerova server --expose udp:7777 --hide-ip          # SDK: {"hide_ip": true}
```
All players connect through the automatic encrypted route. Included in Pro, Studio, Enterprise and the trial; on other plans (or without
a license) the server refuses to start and asks you to upgrade. Note: `--private` / `"private": true` only removes IPs from the ticket —
on a direct connection players still learn the IP; to really hide it use `--hide-ip`.

### 7.5 Private network (VPN) mode for games
For games that need a whole LAN (broadcast room discovery, many random ports, ping/ICMP): players and server share a private virtual LAN,
each player with a stable private IP (default `10.77.x.x`, server `10.77.0.1`).
```sh
peerova server --vpn --vpn-allow tcp:8888,udp:8888 --banned banned.txt      # Windows: Administrator · Linux: sudo
peerova client <TICKET> --vpn                                               # on the player's machine, same permission
```
`--vpn-allow` allows only the game's ports — nothing else on the server is reachable. `--subnet 10.77.0.0/16` changes the address range.
The ban file applies to VPN addresses. Windows and Linux; Windows needs `wintun.dll` next to `peerova.exe` (server and players).

**OS permission — honestly:** creating a virtual network adapter needs Administrator (Windows) / root (Linux) on the **player's device**. The
permission prompt (UAC on Windows, the sudo password on Linux) is shown by the operating system — **its text and look can't be customized or
skipped**. Recommendations:
- grant it **once at game install time**: your installer already runs elevated, so let it set up running `peerova` with that permission
  (e.g. a Windows Scheduled Task with "Run with highest privileges"; on Linux `sudo setcap cap_net_admin+ep peerova`), so playing never asks again;
- or use **port-forwarding mode** (§3, §4) — **no permission at all**, enough for most games that use a few fixed TCP/UDP ports.

## 8. Concepts

| Concept | Meaning |
|---|---|
| **Server node** | The host. Created with `peerova_server_start`. Accepts many clients. |
| **Client node** | The joining side. Created with `peerova_client_connect(ticket)`. Connects to exactly one server. |
| **Ticket** | A `pvt_...` string produced by the server; everything a client needs to find and authenticate it. Share it through any channel or embed it in the game. Stable for a given `key_file`. Not a secret. |
| **Connect code** | A 6-character code (e.g. `K7Q2XD`) usable instead of the ticket. Only available when the server is started with `app` + `share` (a project on the website). Clients pass it where a ticket goes. |
| **Connection id (peer)** | A `uint32` identifying one connection. The server sees one id per client; a client sees the server as one id. Used with `send`/`disconnect`. Treat it as opaque. |
| **Channel** | 0..255. Ordering is per channel; a stalled channel never blocks another. |
| **Reliable** | Ordered (per channel), retransmitted, can be large (the SDK fragments/reassembles). |
| **Unreliable** | Datagram, lowest latency, may be lost or reordered, **max 1180 bytes**. |
| **Event** | Everything from the network is an event in the node's queue: `CONNECTED`, `MESSAGE`, `DISCONNECTED`, `CONNECT_FAILED`. You fetch them with `poll`. |
| **Client key file** | The player's identity file (`key_file`, CLI `--key`). Keep it ⇒ same identity, same `127.a.b.c` address on the server. |
| **Dev mode** | A server without `license` runs in dev mode: at most 3 concurrent clients. |

## 9. Lifecycle

```
SERVER                                          CLIENT
peerova_server_start(cfg)  ── blocks ≤ ~3 s
peerova_ticket() / peerova_room_code()
        │  ticket or 6-char code (embedded / sent over your own channel)  ──►  peerova_client_connect(ticket, cfg)  (returns at once*)
        │                                                        │
poll ─► CONNECTED(peer=A)                                poll ─► CONNECTED(peer=S)   | CONNECT_FAILED(reason)
        │◄──────────── send / MESSAGE (both directions) ────────►│
poll ─► DISCONNECTED(A, reason)  ◄── peerova_disconnect(S) ─────┤   (or network loss / timeout)
peerova_free(server)                                      peerova_free(client)
```
\* With a **connect code**, `peerova_client_connect` first looks the code up on the website, so it blocks for one network lookup; an
unknown/closed code returns `NULL` immediately (`peerova_last_error_code()` = `E_CODE_NOT_FOUND`) rather than a `CONNECT_FAILED` event.
Call it off the UI thread.

## 10. C API reference (`include/peerova.h`)

Every binding wraps exactly these functions. All strings are NUL-terminated UTF-8.

### Event struct
```c
typedef struct {
    int32_t        type;     // PEEROVA_EVENT_CONNECTED=1, DISCONNECTED=2, MESSAGE=3, CONNECT_FAILED=4
    uint32_t       peer;     // connection id
    uint8_t        channel;  // 0..255 (MESSAGE)
    uint8_t        reliable; // 1 if it arrived reliably (MESSAGE)
    const uint8_t* data;     // MESSAGE: payload; DISCONNECTED/CONNECT_FAILED: English UTF-8 reason "E_NAME: ..."
    uint32_t       len;
} peerova_event;
```
`data` is owned by the node and **valid only until the next `peerova_poll` / `peerova_poll_wait` / `peerova_free`
on the same node** — copy it if you keep it (every language binding already copies it).

### Functions

| Function | Description | Returns / errors |
|---|---|---|
| `peerova_node* peerova_server_start(const char* config_json)` | Starts a server. `config_json` may be `NULL` (defaults). **Blocks up to ~3 s** until the ticket is ready — don't call it on a UI/game thread. | Node, or `NULL` on error (see `peerova_last_error_code`). Release with `peerova_free`. |
| `peerova_node* peerova_client_connect(const char* ticket, const char* config_json)` | Creates a client. `ticket` = a `pvt_...` ticket or a 6-character connect code. With a ticket it returns immediately; the outcome arrives as `CONNECTED` / `CONNECT_FAILED`. | Node, or `NULL` (malformed ticket, unknown code, ...). |
| `int32_t peerova_ticket(peerova_node*, char* buf, int32_t buf_len)` | Copies the server's ticket into `buf`. Call with `buf=NULL,0` to get the size. | Required size incl. NUL; `0` if none (clients). Nothing is copied if `buf_len` is too small. |
| `int32_t peerova_room_code(peerova_node*, char* buf, int32_t buf_len)` | Same for the connect code. The code arrives a few seconds after start, once the website assigns it — poll it periodically. | Required size, or `0` if not available yet / `app`+`share` not configured. |
| `int32_t peerova_send(node, uint32_t peer, uint8_t channel, const uint8_t* data, uint32_t len, int32_t reliable)` | Sends to `peer`. `reliable=1`: ordered, retransmitted, any size. `reliable=0`: datagram ≤ 1180 bytes. Data is copied before returning. | `0` on success; `-1` on error (`E_UNKNOWN_PEER`, `E_TOO_LARGE`, `E_INVALID_ARG`). |
| `int32_t peerova_poll(node, peerova_event* out)` | Non-blocking. Fetches one event if available. | `1` and fills `*out`, or `0`. |
| `int32_t peerova_poll_wait(node, peerova_event* out, uint32_t timeout_ms)` | Like `poll` but waits up to `timeout_ms`. | `1`, or `0` on timeout. |
| `void peerova_disconnect(node, uint32_t peer)` | Closes the connection to `peer` (server: kick; client: leave). The other side gets `DISCONNECTED`. | — |
| `uint32_t peerova_peer_count(node)` | Number of connected peers. | — |
| `void peerova_free(node)` | Closes all connections, stops and frees the node. `NULL` is safe. Don't use the pointer afterwards. | — |
| `const char* peerova_last_error(void)` | Last error message **for the calling thread**, in English: `"E_TOO_LARGE: unreliable messages are limited to 1180 bytes"`. Read it right after the failing call, on the same thread. Don't free it. To branch on the kind of error use `peerova_last_error_code` instead of parsing the text. | Static until the next call. |
| `int32_t peerova_last_error_code(void)` | Stable error code (`PEEROVA_E_*`, see [Error codes](#error-codes)) of the last failed call on the calling thread. `server_start`, `client_connect` and `send` reset it to `E_OK` on entry. | `PEEROVA_E_*`. |
| `const char* peerova_version(void)` | Library version, e.g. `"0.1.0"`. Don't free it. | — |

### Error codes

Stable codes (never renumbered; new codes may be added). `peerova_last_error_code()` returns the value;
`peerova_last_error()` and the `CONNECT_FAILED` / `DISCONNECTED` reasons are English text starting with the code name,
e.g. `"E_TIMEOUT: could not connect: timed out. ..."`. C constants: `PEEROVA_E_TOO_LARGE`…; C#: `ErrorCode.TooLarge` /
`PeerovaException.Code` / `Endpoint.LastErrorCode` / `Event.ErrorCode`; Java/Kotlin: `Peerova.E_TOO_LARGE` / `PeerovaException.code` /
`Event.errorCode()`; Python: `peerova.E_TOO_LARGE` / `PeerovaError.code` / `Event.error_code`; Go: `peerova.CodeTooLarge` /
`peerova.CodeOf(err)` / `Event.ErrorCode()`; Node.js: `ErrorCode` / `PeerovaError.code === "E_TOO_LARGE"` / `connectFailed(reason, code)`;
`stdio` sidecar: the `"code":"E_TIMEOUT"` field of events.

| Code | Value | Meaning |
|---|---|---|
| `E_OK` | 0 | No error. |
| `E_INVALID_ARG` | 1 | Invalid argument/config: NULL pointer, malformed JSON, bad connect code format. |
| `E_TOO_LARGE` | 2 | Unreliable payload over 1180 bytes — split it or send it reliably. |
| `E_UNKNOWN_PEER` | 3 | The `peer` id is not (or no longer) connected. |
| `E_CLOSED` | 4 | Connection closed (`DISCONNECTED` reasons). |
| `E_CODE_NOT_FOUND` | 5 | The 6-character connect code is unknown or the room was closed. |
| `E_NETWORK` | 6 | Network/service error: socket, port in use, Peerova website unreachable. |
| `E_TIMEOUT` | 7 | Timed out: connect failed, server not ready, peer stopped responding. Run `peerova doctor`. |
| `E_LICENSE` | 8 | License invalid, expired or rejected. |
| `E_CCU_LIMIT` | 9 | The server reached its concurrent-client limit. |
| `E_ACCESS_DENIED` | 10 | Token wrong, expired or revoked. |
| `E_INVALID_TICKET` | 11 | Ticket malformed, corrupted or from an unsupported version. |
| `E_IO` | 12 | Key file (`key_file`) unreadable, unwritable or corrupted (its folder must exist). |
| `E_INTERNAL` | 99 | Internal/unclassified error — report it to Peerova with the version. |

### Configuration (JSON)

Server:

| Key | Default | Meaning |
|---|---|---|
| `port` | `47000` | UDP port to listen on; `0` = random. |
| `key_file` | `"server.key"` | Server identity file; created if missing. Same key ⇒ same ticket. **Secret.** |
| `license` | — | `PVA-...` key. Without it: dev mode (max 3 clients). |
| `max_ccu` | from license | Cap on concurrent clients (never above the license). |
| `token` | — | Shared secret; clients must present the same `token`. |
| `upnp` | `true` | Try to open a port on the router (UPnP/NAT-PMP). |
| `private` | `false` | The ticket carries no server IP addresses. |
| `hide_ip` | `false` | Hide the server IP: players never learn its real address (DDoS protection); slightly higher latency. Clients connect through the automatic encrypted route. Pro/Studio/Enterprise plans and the trial. |
| `app` | — | Project id from the website — required for a connect code. |
| `share` | — | `{"name":"Nam's room","public":false,"max":8,"mode":"coop","region":"VN"}` — info shown with the connect code; `public` = listed in the public room list. |

Client:

| Key | Default | Meaning |
|---|---|---|
| `token` | — | Must match the server. |
| `timeout_ms` | `20000` | Max time before `CONNECT_FAILED`. |
| `key_file` | — (new random key per run) | The client's identity file; created if missing (the folder must exist). Keeping it keeps the player's identity across runs, so the server sees them at the same stable address `127.a.b.c` (servers assign it from the token subject, else from this key, and operators can ban by that address). Games should store it in the user's app-data folder, e.g. `"<appdata>/<game>/peerova-client.key"`. Banning by address works per player only while they keep that file — accounts / access tokens are the robust way. |

## 11. Threading & poll loops per engine

- Each node has an internal mutex: `send`, `poll`, `poll_wait`, `disconnect`, `peer_count`, `ticket` may be called from any thread.
  Independent nodes run in parallel (e.g. server and client in one process for testing).
- **Never** call `peerova_free` while another thread uses the same node. Stop your poll loop first, then free.
- `peerova_poll_wait` holds the node lock while it waits: if other threads send with low latency, use short timeouts (10–100 ms) or non-blocking `poll`.
- `peerova_last_error` / `_code` are per thread — read them right after the failing call on the same thread (the bindings do this).
- Drain the **whole** queue on each pass (loop until poll returns 0).

| Environment | Poll loop |
|---|---|
| Unity | `PeerovaBehaviour` polls in `Update()` and raises events on the main thread; `Host()` starts the server on a background thread. |
| Unreal | `UPeerovaSubsystem` polls in `Tick`; `Host()` starts the server on a background thread. |
| Godot | `PeerovaNode` polls in `_process` and emits signals. |
| Flutter | non-blocking `poll()` from a `Timer.periodic` on the UI isolate (see [`dart/README.md`](dart/README.md)). |
| Android / JVM | a background thread polls with a timeout (Kotlin: `startPolling { … }` or `eventFlow()`); update the UI on the main thread. |
| Dedicated server (any language) | one thread runs `poll_wait(100)` in a loop; send freely from other threads. |
| Node.js / sidecar | events arrive asynchronously (EventEmitter / JSON lines); no poll loop needed. |

## 12. Patterns

### Games
- Input/positions: unreliable, 10–30 Hz, small packets with a sequence number/timestamp; the receiver drops packets older than the newest it has.
- Important events (hits, pickups, chat, join/leave): reliable on their own channel.
- Host-authoritative: clients send input to the server, the server simulates and broadcasts state to every client.

### Apps
- **Chat**: reliable, channel 0, one message per line.
- **File transfer**: reliable, 16–64 KB chunks on a dedicated channel (so chat doesn't wait), a header first (name, size, hash), an
  acknowledgement at the end. Pace the chunks so the send queue doesn't grow without bound.
- **Remote control**: commands = reliable; high-rate screen/telemetry = unreliable, or reliable on a separate channel if nothing may be lost.

## 13. Performance tips
- Batch many small updates from the same frame into one message.
- Unreliable: stay under 1180 bytes; send the latest state, never resend stale data.
- Reliable: large messages are fine, but a huge one delays later messages **on the same channel** — use separate channels.
- Use binary encodings (fixed structs, MessagePack, protobuf) instead of JSON for high-rate packets.
- Sidecars (`peerova stdio`, Node.js, PHP/Lua without FFI) add a few ms + base64 per packet; latency-sensitive games should use native FFI.
- Port forwarding (`peerova client --forward`) adds very little latency (just a hop through `127.0.0.1`); use `--forward udp:…` for UDP gameplay.
- `--hide-ip` adds a little latency — enable it when you need DDoS protection.
- Opening the server's UDP port (default 47000) or enabling UPnP makes direct connections faster; it is optional.

## 14. Security
- All traffic is end-to-end encrypted, including on the automatic encrypted route; clients authenticate the server with the key in the ticket.
- `server.key` (`key_file`) is the server identity: anyone holding it can impersonate your server. Never commit or send it; make it readable only
  by the server's user. (Copying it to your own machines for merged tickets is the intended use.)
- Tickets/connect codes are **not secrets** — embedding them in the game is fine; whoever has one can attempt to connect. To restrict access use
  `token`, per-user tokens (`--access-key`), or an account check in your game after `CONNECTED`.
- `license` and `token` are secrets: never embed the license in a shipped client. A token on the command line may be visible to other users of the same machine — use `--config`.
- The server answers no packet without a valid ticket: port scans see nothing.
- Expose only the ports you need; move admin services (Remote Desktop, SSH) to a separate process with a token (§7.2).
- The client key file (`key_file`) is not a secret towards the server, but it is the player's identity — don't share one file between players.

## 15. Troubleshooting

| Symptom | What to do |
|---|---|
| `CONNECT_FAILED` / `E_TIMEOUT` | Run `peerova doctor` on both machines (checks IPv6, UPnP, NAT, CGNAT). Check the server is running, the ticket matches its current `key_file`, and `token` matches. |
| Only 3 players can join / `E_CCU_LIMIT` | The server is in dev mode — add a `license`, or the plan's `max_ccu` is reached. |
| The ticket changes on every run | The server creates a new `key_file` each time (temp path or different working directory). Use a fixed absolute path. |
| `E_IO` | The `key_file` folder doesn't exist, isn't writable, or the file is corrupted. |
| `E_TOO_LARGE` | Unreliable packet over 1180 bytes — send it reliably or split it. |
| The server won't start with `hide_ip` / `--hide-ip` | Your plan doesn't include hiding the IP (needs Pro/Studio/Enterprise or the trial, and a valid `license`). |
| `peerova client` can't open the local port | Another program uses it — pick another port: `--forward tcp:38888=8888`. |
| The game doesn't see separate players (`127.0.0.1` for everyone) | The game server must run on the same machine as `peerova server` with the `--expose` target on `127.0.0.1` (the default); Windows and Linux. |
| Library not found | Put `libpeerova.so` / `peerova.dll` / `libpeerova.dylib` where the binding's README says, or set `PEEROVA_LIB` (where supported). |
| Not sure a download is genuine | `peerova verify <file.zip>` (checks the signature + SHA-256). |

## 16. FAQ
- **Do players need to type a code?** No. Embed the ticket in the game or fetch it from your URL (§2); 6-character codes are only for inviting friends to player-hosted rooms.
- **Do I need port forwarding / a static IP?** No. Connections are automatic; an open port only makes them faster.
- **Does it work with an existing game I have no source for?** Yes — §3 (players run one command) or §4 (a launcher runs it hidden).
- **Is the ticket a secret?** No; `server.key`, `license` and `token` are.
- **Do I need a patch to change servers?** No, if the game fetches the ticket from your URL at startup (§2), or if you copy `server.key` to the new machine.
- **Does each `peerova server` process count as a server?** Yes — each process (own ticket) is one server in the license.
- **Can I ban cheaters?** Yes: `--banned` by `127.a.b.c` address (§7.3); more robust with accounts / per-user tokens.
- **Does Windows need `wintun.dll`?** Not for the SDK or port forwarding. Only VPN mode (`--vpn`) needs `wintun.dll` next to `peerova.exe`.
- **Android / iOS / macOS?** Contact Peerova for these builds; each binding's README says where the files go. Mobile can't run child
  processes — use the SDK.
