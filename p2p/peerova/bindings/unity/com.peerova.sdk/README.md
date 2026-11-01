# Peerova SDK for Unity (`com.peerova.sdk`)

- [Tiếng Việt](#tiếng-việt)
- [English](#english)

Website: https://peerova.pages.dev · Khái niệm và C API / Concepts and the C API: `bindings/DEVELOPER-GUIDE.md` trong gói SDK / in the SDK package.

---

## Tiếng Việt

Mạng P2P cho Unity 2021.3+ (.NET Standard 2.1, Mono hoặc IL2CPP), mã hoá đầu-cuối. Gói gồm:
- `Runtime/Peerova.cs` — API C# cấp thấp (`Peerova.Server`, `Peerova.Client`, `Poll`, `Send`).
- `Runtime/PeerovaBehaviour.cs` — component tự poll trong `Update()` và bắn UnityEvent trên main thread.
- Samples: **Chat** và **Position Sync** (vị trí 20 Hz + chat).

### Yêu cầu

- Unity 2021.3 trở lên.
- Thư viện native Peerova cho từng nền tảng bạn build (xem bảng dưới).

### Đặt thư viện native ở đâu

Chép vào thư mục `Plugins/` của gói (chi tiết import settings: [`Plugins/README.md`](Plugins/README.md)):

| Nền tảng | File trong gói SDK | Đặt vào |
|---|---|---|
| Windows x64 (Editor + Standalone) | `lib/windows-x86_64/peerova.dll` | `Plugins/x86_64/peerova.dll`. **Không** cần `wintun.dll` (chỉ công cụ dòng lệnh `peerova` cần cho chế độ VPN). |
| Linux x64 (Editor + Standalone) | `lib/linux-x86_64/libpeerova.so` | `Plugins/x86_64/libpeerova.so` |
| macOS | `libpeerova.dylib` — liên hệ Peerova để nhận bản build | `Plugins/macOS/libpeerova.dylib` (Unity đặt vào `Contents/Frameworks/` khi build) |
| iOS | `libpeerova.a` (tĩnh) — liên hệ Peerova | `Plugins/iOS/libpeerova.a` |
| Android | `libpeerova.so` — liên hệ Peerova | `Plugins/Android/<abi>/libpeerova.so` (`arm64-v8a`, `armeabi-v7a`, `x86_64`) |

Unity tự đặt thư viện cạnh file chạy khi build Standalone. Sau khi thay file `.dll/.so` phải khởi động lại Editor (Unity chỉ nạp native plugin một lần mỗi phiên).

### Cài đặt

1. Chép thư mục này vào `Packages/com.peerova.sdk` của project, hoặc *Package Manager → + → Add package from disk…* rồi chọn `package.json`.
   (Cài từ git URL làm gói chỉ-đọc và Unity sẽ cảnh báo thiếu file `.meta` — nên dùng bản copy cục bộ/embedded, hoặc commit các file `.meta` Unity tạo ra.)
2. Chép thư viện native vào `Plugins/` như bảng trên.
3. Tuỳ chọn: *Package Manager → Peerova SDK → Samples → Import* (Chat, Position Sync).

### Bắt đầu nhanh (5 phút)

```csharp
using Peerova.Unity;
using UnityEngine;

public class Net : MonoBehaviour
{
    PeerovaBehaviour net;

    void Start()
    {
        net = gameObject.AddComponent<PeerovaBehaviour>();
        net.OnHostStarted.AddListener(ticket => Debug.Log("TICKET " + ticket));   // đưa cho client
        net.OnRoomCode.AddListener(code => Debug.Log("CODE " + code));            // mã 6 ký tự (nếu có app)
        net.OnConnected.AddListener(peer => net.Send(peer, System.Text.Encoding.UTF8.GetBytes("xin chào")));
        net.OnMessage.AddListener((peer, channel, data, reliable) => Debug.Log($"{peer}: {data.Length} byte"));
        net.OnDisconnected.AddListener(peer => Debug.Log($"{peer} rời đi"));
        net.OnConnectFailed.AddListener(reason => Debug.LogError(reason));
    }

    public void HostGame() => net.Host();                     // server
    public void JoinGame(string ticketOrCode) => net.Join(ticketOrCode);   // client: ticket hoặc mã 6 ký tự
}
```
- `Host()` khởi động server trên một luồng phụ (khởi động mất tới ~3 giây) rồi bắn `OnHostStarted(ticket)`.
- Mã kết nối (connect code) chỉ có khi điền `app` (project id trên website Peerova) trong Inspector; nó đến sau vài giây qua `OnRoomCode`, và đọc được ở `net.RoomCode`.
- `Join(ticketOrCode, token)` nhận ticket **hoặc** mã 6 ký tự; ném `PeerovaException` nếu chuỗi sai định dạng.

Kết nối là tự động: ưu tiên kết nối trực tiếp, và tự dùng một tuyến dự phòng được mã hoá khi mạng chặn kết nối trực tiếp.

### Gửi: reliable hay unreliable

```csharp
net.Send(peer, positionBytes, channel: 1, reliable: false);   // trả false nếu lỗi
net.Broadcast(chatBytes, channel: 0, reliable: true);          // host: tới mọi client; client: tới server
```

| | `reliable: false` | `reliable: true` |
|---|---|---|
| Đảm bảo | Có thể mất hoặc đến sai thứ tự | Đến đủ, đúng thứ tự trong cùng một channel |
| Kích thước | Tối đa **1180 byte**; lớn hơn bị từ chối | Có thể lớn |
| Dùng cho | Input, vị trí, trạng thái realtime kiểu voice (gửi liên tục, gói mới thay gói cũ) | Chat, inventory, RPC, các phần của file |

Channel là số 0..255; các channel khác nhau không chặn nhau.

### Nhận sự kiện

`PeerovaBehaviour` poll trong `Update()` (tối đa `maxEventsPerFrame` sự kiện/frame) và gọi trên **main thread**:

| Sự kiện | Khi nào |
|---|---|
| `OnHostStarted(string ticket)` | Server đã chạy. |
| `OnRoomCode(string code)` | Đã có mã kết nối 6 ký tự. |
| `OnConnected(uint peer)` | Peer kết nối (host: client mới; client: server). |
| `OnMessage(uint peer, byte channel, byte[] data, bool reliable)` | Có message; `data` là bản copy. |
| `OnDisconnected(uint peer)` | Peer ngắt kết nối. |
| `OnConnectFailed(string reason)` | Client không kết nối được, hoặc server không khởi động được. |
| `EventReceived` (C# event) | Toàn bộ struct `Peerova.Event`, sau UnityEvent tương ứng. |

Không dùng component? Dùng API cấp thấp và poll trong `Update` hoặc coroutine:
```csharp
Peerova.Client client = Peerova.Client.Connect(ticketOrCode);
void Update() { while (client.Poll(out Peerova.Event ev)) Handle(ev); }   // không bao giờ dùng Poll có timeout trên main thread
```
Lưu ý: `Peerova.Event` / `Peerova.EventType` trùng tên với `UnityEngine.Event` / `UnityEngine.EventType`; hãy ghi đầy đủ (`Peerova.Event`) hoặc thêm `using PeerovaEvent = Peerova.Event;`.

### Ngắt kết nối và dọn dẹp

- `Disconnect(peer)` đóng một kết nối; `Shutdown()` dừng server/client và giải phóng node native.
- Component tự gọi `Shutdown()` trong `OnDestroy` / `OnApplicationQuit`. Bật `dontDestroyOnLoad` để giữ kết nối khi đổi scene.
- Với API cấp thấp: gọi `Dispose()`; không dispose khi luồng khác vẫn đang poll node đó.

### Xử lý lỗi

- Lỗi khởi động server và lỗi kết nối đến qua `OnConnectFailed(reason)` (lý do là nội dung `peerova_last_error()`).
- `Send` trả `false` khi lỗi; lý do ở `Peerova.Endpoint.LastError`.
- `DllNotFoundException`: thiếu thư viện native cho nền tảng hiện tại.

### Cấu hình (Inspector)

| Trường | Ý nghĩa |
|---|---|
| `port` | Cổng UDP (`0` = ngẫu nhiên; mặc định của server khi không cấu hình là 47000). |
| `license` | License `PVA-...`; để trống = chế độ dev, tối đa 3 client. |
| `upnp` | Thử mở cổng trên router qua UPnP (mặc định bật). |
| `token` | Mật khẩu chung client phải gửi (`Join(ticket, token)`). |
| `maxCcu` | Số client đồng thời tối đa (`0` = theo license). |
| `keyFile` | Khoá định danh server, tương đối với `Application.persistentDataPath`. Cùng khoá ⇒ cùng ticket. |
| `app`, `shareName`, `sharePublic`, `shareMax`, `shareMode`, `shareRegion` | Mã kết nối: project id trên website và thông tin hiển thị. |
| `connectTimeoutMs` | Client bỏ cuộc sau bấy nhiêu ms (`0` = mặc định 20000). |

### Ví dụ

Trong `Samples~/` (import qua Package Manager):
- **Chat** — host/join và chat reliable.
- **Position Sync** — mỗi người chơi là một khối lập phương; vị trí gửi 20 lần/giây qua channel 1 (unreliable), chat qua channel 0 (reliable); host chuyển tiếp cho các client khác.
  Cách chạy: thêm `PositionSyncSample` vào một GameObject trống, chạy hai player (Editor + bản build), bấm **Host** ở một bên, dán ticket hoặc mã vào bên kia rồi bấm **Join**.

### Bảo mật

- File key của server là danh tính của server: ai có nó có thể giả mạo server. Giữ bí mật, không commit, không đưa vào build cho client.
- Ticket là bán công khai: cứ đưa cho người chơi. `token` và `license` là bí mật — không nhúng license vào bản build phát cho người chơi nếu họ không phải là host.
- Toàn bộ dữ liệu được mã hoá đầu-cuối.

### Khắc phục sự cố

- Chạy `peerova doctor` (công cụ dòng lệnh trong gói SDK) để chẩn đoán mạng.
- **`DllNotFoundException: peerova`**: thiếu thư viện trong `Plugins/` hoặc import settings sai nền tảng/CPU; khởi động lại Editor sau khi thêm file.
- **Kết nối thất bại do hết thời gian**: host chưa chạy, ticket/mã sai, hoặc mạng chặn; chạy `peerova doctor` ở hai phía, tăng `connectTimeoutMs`.
- **Client thứ 4 bị từ chối**: chế độ dev chỉ nhận 3 client — nhập `license`.
- **`Send` unreliable trả false**: message > 1180 byte.

### Tình trạng kiểm thử

`Tests~/StubCheck` biên dịch `Runtime/` và cả hai sample với một stub `UnityEngine` (netstandard2.1 và net8.0), rồi chạy host + join qua `Update()`:
`cd Tests~/StubCheck && dotnet run -c Release --framework net8.0`. Gói chưa được chạy bên trong Unity Editor trong quy trình kiểm thử tự động.

---

## English

Peer-to-peer networking for Unity 2021.3+ (.NET Standard 2.1, Mono or IL2CPP), end-to-end encrypted. The package contains:
- `Runtime/Peerova.cs` — the low-level C# API (`Peerova.Server`, `Peerova.Client`, `Poll`, `Send`).
- `Runtime/PeerovaBehaviour.cs` — a component that polls in `Update()` and raises UnityEvents on the main thread.
- Samples: **Chat** and **Position Sync** (20 Hz positions + chat).

### Requirements

- Unity 2021.3 or later.
- The Peerova native library for each platform you build for (see the table below).

### Where to put the native library

Copy it into the package's `Plugins/` folder (import settings: [`Plugins/README.md`](Plugins/README.md)):

| Platform | File in the SDK package | Put it in |
|---|---|---|
| Windows x64 (Editor + Standalone) | `lib/windows-x86_64/peerova.dll` | `Plugins/x86_64/peerova.dll`. `wintun.dll` is **not** needed (only the `peerova` command-line tool needs it, for its VPN mode). |
| Linux x64 (Editor + Standalone) | `lib/linux-x86_64/libpeerova.so` | `Plugins/x86_64/libpeerova.so` |
| macOS | `libpeerova.dylib` — contact Peerova for this build | `Plugins/macOS/libpeerova.dylib` (Unity places it in `Contents/Frameworks/` when building) |
| iOS | `libpeerova.a` (static) — contact Peerova | `Plugins/iOS/libpeerova.a` |
| Android | `libpeerova.so` — contact Peerova | `Plugins/Android/<abi>/libpeerova.so` (`arm64-v8a`, `armeabi-v7a`, `x86_64`) |

Unity places the library next to the executable when building Standalone players. After replacing a `.dll/.so`, restart the Editor (Unity loads native plugins once per session).

### Install

1. Copy this folder into your project's `Packages/com.peerova.sdk`, or use *Package Manager → + → Add package from disk…* and pick `package.json`.
   (Installing from a git URL makes the package read-only and Unity warns about missing `.meta` files — use a local/embedded copy, or commit the `.meta` files Unity generates.)
2. Copy the native libraries into `Plugins/` as above.
3. Optional: *Package Manager → Peerova SDK → Samples → Import* (Chat, Position Sync).

### 5-minute quick start

```csharp
using Peerova.Unity;
using UnityEngine;

public class Net : MonoBehaviour
{
    PeerovaBehaviour net;

    void Start()
    {
        net = gameObject.AddComponent<PeerovaBehaviour>();
        net.OnHostStarted.AddListener(ticket => Debug.Log("TICKET " + ticket));   // give to clients
        net.OnRoomCode.AddListener(code => Debug.Log("CODE " + code));            // 6-char code (if app is set)
        net.OnConnected.AddListener(peer => net.Send(peer, System.Text.Encoding.UTF8.GetBytes("hello")));
        net.OnMessage.AddListener((peer, channel, data, reliable) => Debug.Log($"{peer}: {data.Length} bytes"));
        net.OnDisconnected.AddListener(peer => Debug.Log($"{peer} left"));
        net.OnConnectFailed.AddListener(reason => Debug.LogError(reason));
    }

    public void HostGame() => net.Host();                                  // server
    public void JoinGame(string ticketOrCode) => net.Join(ticketOrCode);   // client: ticket or 6-char code
}
```
- `Host()` starts the server on a worker thread (starting takes up to ~3 seconds), then raises `OnHostStarted(ticket)`.
- A connect code is only assigned when `app` (the project id from the Peerova website) is set in the Inspector; it arrives a few seconds later through `OnRoomCode` and can be read from `net.RoomCode`.
- `Join(ticketOrCode, token)` takes a ticket **or** a 6-character code; it throws `PeerovaException` if the string is malformed.

Connections are automatic: direct first, with an automatic encrypted fallback route when networks block direct connections.

### Sending: reliable vs unreliable

```csharp
net.Send(peer, positionBytes, channel: 1, reliable: false);   // returns false on failure
net.Broadcast(chatBytes, channel: 0, reliable: true);          // host: to all clients; client: to the server
```

| | `reliable: false` | `reliable: true` |
|---|---|---|
| Guarantee | May be lost or reordered | Delivered, in order within the channel |
| Size | At most **1180 bytes**; larger sends are rejected | Can be large |
| Use for | Gameplay inputs, positions, voice-like realtime state (sent continuously, newer replaces older) | Chat, inventory, RPC, file chunks |

Channels are numbers 0..255; different channels don't block each other.

### Receiving events

`PeerovaBehaviour` polls in `Update()` (up to `maxEventsPerFrame` events per frame) and invokes on the **main thread**:

| Event | When |
|---|---|
| `OnHostStarted(string ticket)` | The server is running. |
| `OnRoomCode(string code)` | The 6-character connect code is available. |
| `OnConnected(uint peer)` | A peer connected (host: a new client; client: the server). |
| `OnMessage(uint peer, byte channel, byte[] data, bool reliable)` | A message arrived; `data` is a copy. |
| `OnDisconnected(uint peer)` | A peer disconnected. |
| `OnConnectFailed(string reason)` | The client could not connect, or the server could not start. |
| `EventReceived` (C# event) | The full `Peerova.Event` struct, after the matching UnityEvent. |

Not using the component? Use the low-level API and poll from `Update` or a coroutine:
```csharp
Peerova.Client client = Peerova.Client.Connect(ticketOrCode);
void Update() { while (client.Poll(out Peerova.Event ev)) Handle(ev); }   // never use a Poll timeout on the main thread
```
Note: `Peerova.Event` / `Peerova.EventType` share names with `UnityEngine.Event` / `UnityEngine.EventType`; qualify them (`Peerova.Event`) or add `using PeerovaEvent = Peerova.Event;`.

### Disconnect and cleanup

- `Disconnect(peer)` closes one connection; `Shutdown()` stops the server/client and frees the native node.
- The component calls `Shutdown()` itself in `OnDestroy` / `OnApplicationQuit`. Enable `dontDestroyOnLoad` to keep the connection across scene loads.
- With the low-level API: call `Dispose()`; never dispose while another thread is still polling that node.

### Error handling

- Server start errors and connection errors arrive through `OnConnectFailed(reason)` (the reason is the text of `peerova_last_error()`).
- `Send` returns `false` on failure; the reason is in `Peerova.Endpoint.LastError`.
- `DllNotFoundException`: the native library for the current platform is missing.

### Configuration (Inspector)

| Field | Meaning |
|---|---|
| `port` | UDP port (`0` = random; the server default without configuration is 47000). |
| `license` | `PVA-...` license; empty = dev mode, max 3 clients. |
| `upnp` | Try to open the port on the router via UPnP (on by default). |
| `token` | Shared secret clients must present (`Join(ticket, token)`). |
| `maxCcu` | Maximum concurrent clients (`0` = license limit). |
| `keyFile` | Server identity key, relative to `Application.persistentDataPath`. Same key ⇒ same ticket. |
| `app`, `shareName`, `sharePublic`, `shareMax`, `shareMode`, `shareRegion` | Connect code: project id from the website and listing details. |
| `connectTimeoutMs` | The client gives up after this many ms (`0` = default 20000). |

### Examples

In `Samples~/` (import through the Package Manager):
- **Chat** — host/join and reliable chat.
- **Position Sync** — every player is a cube; positions go 20 times per second on channel 1 (unreliable), chat on channel 0 (reliable); the host forwards to the other clients.
  How to run: add `PositionSyncSample` to an empty GameObject, run two players (Editor + a build), press **Host** in one, paste the ticket or code into the other and press **Join**.

### Security

- The server key file is the server's identity: anyone who has it can impersonate the server. Keep it secret, never commit it, never ship it in client builds.
- The ticket is public-ish: share it with players. `token` and `license` are secrets — do not embed the license in builds you give to players who are not hosts.
- All traffic is end-to-end encrypted.

### Troubleshooting

- Run `peerova doctor` (the command-line tool in the SDK package) for a network diagnosis.
- **`DllNotFoundException: peerova`**: the library is missing from `Plugins/`, or its import settings target the wrong platform/CPU; restart the Editor after adding files.
- **Connect failed (timeout)**: the host is not running, the ticket/code is wrong, or the network blocks traffic; run `peerova doctor` on both sides and/or raise `connectTimeoutMs`.
- **The 4th client is rejected**: dev mode accepts only 3 clients — enter a `license`.
- **Unreliable `Send` returns false**: the message is larger than 1180 bytes.

### Testing status

`Tests~/StubCheck` compiles `Runtime/` and both samples against a stub `UnityEngine` (netstandard2.1 and net8.0), then drives a host + join through `Update()`:
`cd Tests~/StubCheck && dotnet run -c Release --framework net8.0`. The package has not been run inside the Unity Editor as part of automated testing.
