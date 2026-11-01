# Peerova cho Java (JNI)

- [Tiếng Việt](#tiếng-việt)
- [English](#english)

---

## Tiếng Việt

Binding Java 8+ cho Peerova: kết nối P2P trực tiếp, mã hoá đầu-cuối. Gồm lớp `dev.peerova.Peerova`
(`src/dev/peerova/Peerova.java`) và lớp keo JNI `peerova_jni.c`, gọi vào thư viện lõi `libpeerova`.
Dùng được trên JVM desktop/server và Android (trên Android nên dùng wrapper Kotlin trong
[`../kotlin-android`](../kotlin-android), cùng mã Java này bên dưới).

Khái niệm (server, client, ticket, connect code, channel, sự kiện) và toàn bộ C API: [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md).
Website: https://peerova.pages.dev

### Chọn cách tích hợp

- **Game/app Java có sẵn, giữ nguyên code socket** (tình huống 2, JVM desktop/server): app tự chạy `peerova client` ẩn rồi kết nối
  `127.0.0.1` — xem [Game có sẵn: chạy peerova ẩn](#game-có-sẵn-chạy-peerova-ẩn). Không dùng được trên Android (dùng SDK).
- **Game mới hoặc host trong game** (tình huống 3/4): dùng SDK `peerova.jar` bên dưới — message reliable/unreliable, sự kiện.
- Bảng đầy đủ 4 tình huống: [Chọn cách tích hợp](../DEVELOPER-GUIDE.md#1-chọn-cách-tích-hợp).

**Người chơi không phải gõ gì:** nhúng ticket vào game, hoặc tải ticket mới nhất từ URL của bạn khi khởi động; mã 6 ký tự chỉ
dùng để mời bạn bè vào phòng do người chơi mở. Chi tiết: [Người chơi không phải gõ gì](../DEVELOPER-GUIDE.md#2-người-chơi-không-phải-gõ-gì).

### Yêu cầu

- JDK 8 trở lên để chạy (`peerova.jar` được biên dịch với `--release 8`). Để build lớp JNI: JDK (có `include/jni.h`) và `gcc`/`clang` (Windows: MinGW-w64).
- Thư viện lõi trong gói SDK:
  `lib/linux-x86_64/libpeerova.so`, `lib/windows-x86_64/peerova.dll`. Header: `include/peerova.h` (bản sao: `bindings/c/peerova.h`).

### Thêm `peerova.jar` vào project (khuyên dùng)

Gói SDK có **`java/peerova.jar`**: một file duy nhất gồm API Java và thư viện native cho
**Windows x86_64** (kèm bản legacy cho Windows 7 / 8.1 / Server 2012) và **Linux x86_64**. Chỉ cần thêm nó vào classpath —
không cần `-Djava.library.path`, không cần chép `.dll`/`.so`:

```sh
java -cp peerova.jar:. MyApp          # Windows: java -cp peerova.jar;. MyApp
```

- Maven/Gradle: dùng như một jar cục bộ, ví dụ Gradle `implementation(files("libs/peerova.jar"))`.
- Lần đầu nạp lớp `Peerova`, thư viện native đúng hệ điều hành/kiến trúc được giải nén vào thư mục cache **của người dùng**
  (Windows `%LOCALAPPDATA%\Peerova\java`, Linux `~/.cache/peerova/java`), theo phiên bản và mã SHA-256, kiểm tra
  SHA-256 trước khi nạp. Không ghi gì vào thư mục làm việc. Các lần sau dùng lại file đã có.
- Windows 7 / 8.1 / Server 2012 tự dùng bản legacy (ép bằng `-Dpeerova.windows.legacy=true|false`).
- Đổi thư mục cache: `-Dpeerova.cache.dir=/thư/mục`. Tắt giải nén (chỉ dùng thư viện bạn tự đặt): `-Dpeerova.native.extract=false`.
- Nền tảng khác (macOS…) hoặc khi không giải nén được: `Peerova` tự quay về cách nạp từ `java.library.path` ở bảng dưới.
- Android **không** dùng jar này: dùng AAR / `jniLibs`, xem [`../kotlin-android`](../kotlin-android).

### Tự đặt thư viện native (tuỳ chọn)

Chỉ cần khi không dùng thư viện có sẵn trong `peerova.jar` (Android, macOS, bản build riêng). Cần **hai** thư viện:
`libpeerova` (lõi) và `libpeerova_jni` (lớp keo JNI, build từ `peerova_jni.c`). `Peerova` nạp chúng bằng
`System.loadLibrary`, tức là tìm trong `java.library.path`; hoặc đặt đường dẫn tuyệt đối qua
`-Dpeerova.lib.path=/…/libpeerova.so` và `-Dpeerova.jni.path=/…/libpeerova_jni.so` (khi có hai thuộc tính này, thư viện trong jar bị bỏ qua).

| Hệ điều hành | Tệp | Đặt ở đâu |
|---|---|---|
| Linux x86_64 | `libpeerova.so` + `libpeerova_jni.so` | cùng một thư mục, chạy `java -Djava.library.path=<thư mục>`. `libpeerova_jni.so` do `build.sh` tạo có rpath `$ORIGIN` nên tự tìm `libpeerova.so` nằm cạnh nó; cách khác: `LD_LIBRARY_PATH` |
| Windows x86_64 | `peerova.dll` + `peerova_jni.dll` | cạnh file `.exe`/launcher, hoặc trong thư mục `-Djava.library.path`. **Không** cần `wintun.dll` (chỉ công cụ dòng lệnh `peerova` cần nó cho chế độ VPN) |
| macOS | `libpeerova.dylib` + `libpeerova_jni.dylib` | liên hệ Peerova để nhận bản build macOS; trong app bundle đặt ở `Contents/Frameworks` |
| Android | `jniLibs/<abi>/libpeerova.so` + `libpeerova_jni.so` | liên hệ Peerova để nhận bản build Android; xem [`../kotlin-android/README.md`](../kotlin-android/README.md) |
| iOS | — | JNI không chạy trên iOS; liên hệ Peerova (thư viện tĩnh `libpeerova.a`, dùng binding Swift) |

### Cài đặt

Linux / macOS (từ `bindings/java`):

```sh
./build.sh build              # -> build/peerova.jar, build/libpeerova_jni.so, build/libpeerova.so (bản sao)
./build.sh                    # build + chạy test round-trip (in "PASS")
```

`build.sh` tìm `libpeerova.so` trong `lib/linux-x86_64` của gói SDK; đổi bằng `PEEROVA_LIB_DIR=/thư/mục`.
Thông thường bạn **không cần build**: dùng `java/peerova.jar` có sẵn trong gói SDK.
Header lấy từ `../c` (đổi bằng `PEEROVA_INCLUDE`). `JAVA_HOME` tự dò từ `javac`.

Windows (MinGW-w64, trong `bindings\java`):

```bat
x86_64-w64-mingw32-gcc -O2 -shared peerova_jni.c -I"%JAVA_HOME%\include" -I"%JAVA_HOME%\include\win32" -I..\c ^
    -L..\..\lib\windows-x86_64 -lpeerova -o peerova_jni.dll
javac --release 8 -d classes src\dev\peerova\Peerova.java src\dev\peerova\NativeLoader.java && jar cf peerova.jar -C classes .
```

Sau đó thêm `peerova.jar` vào classpath (Maven/Gradle: dùng như một jar cục bộ) và đặt các thư viện native như bảng trên.

### Bắt đầu nhanh (5 phút)

Server:

```java
import dev.peerova.Peerova;

try (Peerova.Server srv = Peerova.Server.start("{\"port\":47000,\"key_file\":\"server.key\"}")) {
    System.out.println("ticket: " + srv.ticket());        // gửi chuỗi này cho client
    while (true) {
        Peerova.Event ev = srv.poll(100);                  // chờ tối đa 100 ms
        if (ev == null) continue;
        if (ev.type == Peerova.CONNECTED) srv.send(ev.peer, "chào", 0, true);
        if (ev.type == Peerova.MESSAGE) System.out.println(ev.peer + ": " + ev.text());
        if (srv.roomCode() != null) { /* connect code 6 ký tự, chỉ có khi cấu hình "app" + "share" */ }
    }
}
```

`Server.start` chặn tới khi ticket sẵn sàng (tối đa khoảng 3 giây). Ticket ổn định với cùng `key_file`.

Connect code: khởi động server với `"app"` (mã project trên website Peerova) và `"share"`, rồi gọi
`srv.roomCode()` định kỳ tới khi nó khác `null` (website cấp mã ngay sau khi server khởi động):

```java
Peerova.Server srv = Peerova.Server.start(
    "{\"app\":\"prj_xxx\",\"share\":{\"name\":\"Phòng của Lan\",\"public\":true,\"max\":8}}");
String code = srv.roomCode();   // ví dụ "K7Q2XD", hoặc null nếu chưa có
```

Client — truyền ticket **hoặc** connect code 6 ký tự:

```java
try (Peerova.Client cli = Peerova.Client.connect(ticketOrCode)) {   // trả về ngay
    Peerova.Event ev = cli.poll(20000);
    if (ev == null || ev.type == Peerova.CONNECT_FAILED) throw new RuntimeException(ev == null ? "timeout" : ev.text());
    long server = ev.peer;                                           // CONNECTED
    cli.send(server, "xin chào", 0, true);
}
```

Với ticket, `connect` trả về ngay. Với connect code, `connect` tra mã trên website Peerova trước (một request HTTPS,
cần Internet) và ném `PeerovaException` nếu mã không tồn tại.

Kết nối là tự động: thử kết nối trực tiếp trước; khi mạng chặn kết nối trực tiếp, Peerova tự dùng **đường mã hoá tự động** (vẫn mã hoá đầu-cuối).

Ví dụ đầy đủ: [`examples/Chat.java`](examples/Chat.java) (chat + đồng bộ vị trí 20 Hz). Cách chạy ở đầu file.

### Gửi reliable hay unreliable

`send(peer, data, channel, reliable)` — `channel` từ 0 đến 255.

| | `reliable = true` | `reliable = false` |
|---|---|---|
| Đảm bảo | tới nơi, đúng thứ tự trong cùng channel | có thể mất hoặc đảo thứ tự |
| Kích thước | có thể lớn | tối đa **1180 byte** (`Peerova.MAX_UNRELIABLE_PAYLOAD`); lớn hơn sẽ bị từ chối |
| Dùng cho | chat, inventory, RPC, khối dữ liệu file | input, vị trí, trạng thái realtime kiểu voice — gói mới thay gói cũ |

Các channel độc lập: một tin reliable bị chậm trên channel 0 không chặn channel 1. Nên tách loại dữ liệu theo channel
(ví dụ 0 = chat, 1 = vị trí, 2 = RPC). Có thêm `send(peer, String, …)`, `send(peer, byte[], off, len, …)` và
`send(peer, ByteBuffer, …)` (ByteBuffer direct: không copy).

### Nhận sự kiện

`poll()` (không chặn) hoặc `poll(timeoutMs)` trả về `Peerova.Event` hoặc `null`:

| `ev.type` | Ý nghĩa | Trường dùng được |
|---|---|---|
| `CONNECTED` | server: một client vừa vào; client: đã kết nối tới server | `peer` |
| `MESSAGE` | có tin nhắn | `peer`, `channel`, `reliable`, `data` / `text()` |
| `DISCONNECTED` | peer rời đi | `peer`, `text()` = lý do |
| `CONNECT_FAILED` | (client) kết nối thất bại | `text()` = lý do |

`ev.data` là bản sao Java, giữ lại thoải mái. Mẫu vòng lặp thường dùng:

- **Ứng dụng/server**: một thread riêng gọi `poll(100)` liên tục, chuyển sự kiện sang thread khác qua hàng đợi nếu cần.
- **Game loop**: mỗi frame gọi `poll()` (không chặn) trong vòng `while ((ev = node.poll()) != null)` để xử lý hết sự kiện đang chờ.

### Quy tắc thread

`send`, `poll`, `disconnect`, `peerCount` an toàn khi gọi từ nhiều thread. Chỉ chạy **một** vòng poll cho mỗi node.
**Không bao giờ** gọi `close()` khi thread khác đang ở trong `poll`/`send` của cùng node: dừng thread poll trước, rồi mới `close()`.
Trên Android không gọi `Server.start`, `poll(timeout)` hay `Client.connect` với connect code trên main thread.

### Ngắt kết nối và dọn dẹp

- `node.disconnect(peer)` đóng một kết nối (sau đó có sự kiện `DISCONNECTED`).
- `node.close()` (hoặc try-with-resources) ngắt mọi peer và giải phóng node native; gọi nhiều lần không sao.
  Sau khi đóng, mọi phương thức ném `IllegalStateException`.
- Trước khi đóng client, nên `poll` thêm vài trăm ms để tin reliable vừa gửi kịp tới nơi.

### Xử lý lỗi

- Lỗi native ném `Peerova.PeerovaException`: `e.code` là mã lỗi ổn định (`Peerova.E_TOO_LARGE`, `E_UNKNOWN_PEER`, `E_CODE_NOT_FOUND`…,
  bảng đầy đủ: [Mã lỗi](../DEVELOPER-GUIDE.md#mã-lỗi)); message là văn bản tiếng Anh `"E_NAME: ..."`. Lý do của `CONNECT_FAILED` /
  `DISCONNECTED` cùng định dạng: `ev.errorCode()`.
- `trySend(...)` trả `false` thay vì ném ngoại lệ (hợp cho gửi unreliable tần suất cao).
- Lỗi tham số: `IllegalArgumentException` (channel ngoài 0..255, ticket rỗng), `IndexOutOfBoundsException` (off/len sai).
- `UnsatisfiedLinkError` khi nạp lớp `Peerova`: không tìm thấy thư viện native (xem phần Khắc phục sự cố).

### Cấu hình

Server (`Server.start(json)`, `json` có thể `null`):

| Khoá | Mặc định | Ý nghĩa |
|---|---|---|
| `port` | `47000` | cổng lắng nghe; `0` = cổng ngẫu nhiên |
| `key_file` | `"server.key"` | file khoá định danh server; cùng khoá ⇒ cùng ticket. Được tạo nếu chưa có |
| `license` | — | license key `PVA-...`. Không có: chế độ dev, tối đa 3 client |
| `max_ccu` | theo license | giới hạn số client đồng thời |
| `token` | — | bí mật chung; client phải gửi đúng `token` mới được vào |
| `upnp` | `true` | tự mở cổng trên router qua UPnP |
| `private` | `false` | ticket riêng tư: ticket không chứa địa chỉ IP của server |
| `hide_ip` | `false` | Ẩn IP máy chủ: người chơi không bao giờ biết IP thật của máy chủ (chống DDoS); ping cao hơn một chút. Gói Pro/Studio/Enterprise và bản dùng thử |
| `app` | — | mã project trên website Peerova (cần cho connect code) |
| `share` | — | `{"name":"...","public":true,"max":16,"mode":"...","region":"VN"}`: thông tin đăng lên website để nhận connect code |

Client (`Client.connect(ticket, json)`): `token` (phải khớp server), `timeout_ms` (mặc định `20000`), `key_file` (tuỳ chọn:
file danh tính client, tạo nếu chưa có, thư mục phải tồn tại — lưu trong thư mục dữ liệu ứng dụng, ví dụ `"<appdata>/<game>/peerova-client.key"`, để người
chơi giữ cùng danh tính và cùng địa chỉ ổn định trên server giữa các lần chạy; chặn theo địa chỉ chỉ có tác dụng khi họ còn giữ file này,
muốn chắc chắn hãy dùng tài khoản / token).

### Game có sẵn: chạy peerova ẩn

Khi game/app đã có code socket TCP/UDP và người chơi **không được phải chạy gì**: phát hành `peerova.exe` (Windows) /
`peerova` (Linux, macOS) kèm app (lấy từ `lib/<nền tảng>/` của gói SDK). App chạy
`peerova client <TICKET> --forward tcp:8888 --key <appdata>/<game>/peerova-client.key` (Java không mở cửa sổ console trên Windows),
đọc output (UTF-8) từng dòng, coi là **sẵn sàng** khi một dòng chứa chuỗi ASCII `server port` (công cụ in `✔ ĐÃ KẾT NỐI …` rồi
`✔ Tcp 127.0.0.1:8888 → server port 8888`, mỗi `--forward` một dòng), rồi code socket cũ kết nối `127.0.0.1:8888`. Thất bại khi
process thoát trước (mã thoát `1`, dòng bắt đầu bằng `lỗi:`) hoặc quá 30 giây (process bị dừng). Process bị dừng khi JVM thoát.
Không cần `peerova.jar` cho cách này.

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

- Linux/macOS: file `peerova` phải có quyền chạy (`chmod +x`). Thư mục chứa `--key` phải tồn tại (code trên tự tạo).
- Nếu cổng 8888 có thể đang bận trên máy người chơi, chọn cổng local khác: `tcp:38888=8888`.
- Tuỳ chọn khác của `peerova client`: `--reconnect`, `--token SECRET`, `--bind IP`.
- Android không chạy được công cụ dòng lệnh như process con — dùng SDK (xem [`../kotlin-android`](../kotlin-android)).
- `peerova stdio` **không** mở cổng local (nó là SDK message dạng JSON lines), nên không dùng cho cách này.
- Chi tiết: [Game tự chạy peerova ẩn](../DEVELOPER-GUIDE.md#4-game-tự-chạy-peerova-ẩn-người-chơi-không-chạy-gì).

### Máy chủ game

Nhiều cổng trong một process (`--expose` lặp lại), tách Remote Desktop cho admin và game công khai thành các process riêng, mỗi người
chơi một địa chỉ `127.a.b.c` riêng + cấm bằng `--banned`, ẩn IP máy chủ (`--hide-ip` / `"hide_ip": true`), chế độ VPN:
[Tính năng máy chủ game](../DEVELOPER-GUIDE.md#7-tính-năng-máy-chủ-game).

### Bảo mật

- `server.key` là danh tính của server: ai có file này có thể giả làm server của bạn. Giữ bí mật, **không commit** vào git.
- Ticket và connect code không phải bí mật: cứ nhúng vào game hoặc chia sẻ cho client.
- `token` và `license` là bí mật: không nhúng vào client phát hành công khai nếu không cần.
- Toàn bộ dữ liệu được mã hoá đầu-cuối.

### Khắc phục sự cố

- Chẩn đoán mạng: chạy `peerova doctor` bằng công cụ dòng lệnh `peerova` (có trong `lib/<nền tảng>/` của gói SDK).
- `UnsatisfiedLinkError` khi dùng `peerova.jar`: thông báo lỗi nêu lý do không dùng được thư viện trong jar (ví dụ thư mục cache không
  ghi được → đặt `-Dpeerova.cache.dir`; nền tảng không có trong jar → tự đặt thư viện).
- `UnsatisfiedLinkError: no peerova_jni in java.library.path`: kiểm tra `-Djava.library.path` trỏ tới thư mục chứa
  `libpeerova_jni.so`/`peerova_jni.dll`, và `libpeerova.so`/`peerova.dll` nằm cạnh nó (hoặc trong `LD_LIBRARY_PATH`/`PATH`).
- `CONNECT_FAILED` sau khoảng 20 giây (timeout): server chưa chạy, ticket/code sai, hoặc `token` không khớp. Chạy `peerova doctor` ở cả hai máy.
- Client thứ 4 bị từ chối: server đang ở chế độ dev (không có `license`), tối đa 3 client.
- `PeerovaException` khi gửi unreliable: payload vượt 1180 byte — chia nhỏ hoặc gửi reliable.

### FAQ

- **Người chơi có phải gõ mã không?** Không. Nhúng ticket vào game hoặc tải từ URL của bạn; mã 6 ký tự chỉ để mời bạn bè.
- **Có cần chép `.dll`/`.so` không?** Không, với Windows/Linux x86_64: `peerova.jar` có sẵn thư viện native và tự giải nén vào cache của người dùng.
- **Ticket có phải bí mật không?** Không; `server.key`, `license`, `token` mới là bí mật.
- **Kết nối không được?** Chạy `peerova doctor` trên cả hai máy. Thêm: [Khắc phục sự cố](../DEVELOPER-GUIDE.md#15-khắc-phục-sự-cố) · [FAQ](../DEVELOPER-GUIDE.md#16-faq).

---

## English

Java 8+ binding for Peerova: direct P2P connections, end-to-end encrypted. It consists of the class
`dev.peerova.Peerova` (`src/dev/peerova/Peerova.java`) and the JNI glue `peerova_jni.c`, which calls the core
library `libpeerova`. It runs on desktop/server JVMs and on Android (on Android prefer the Kotlin wrapper in
[`../kotlin-android`](../kotlin-android), which uses this same Java code underneath).

Concepts (server, client, ticket, connect code, channels, events) and the full C API: [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md).
Website: https://peerova.pages.dev

### Choosing an integration

- **Existing Java game/app, keep your socket code** (scenario 2, desktop/server JVM): the app starts `peerova client` hidden, then connects
  to `127.0.0.1` — see [Existing game: run peerova hidden](#existing-game-run-peerova-hidden). Not possible on Android (use the SDK).
- **New game or host-in-game** (scenarios 3/4): use the `peerova.jar` SDK below — reliable/unreliable messages, events.
- All 4 scenarios: [Choosing an integration](../DEVELOPER-GUIDE.md#1-choosing-an-integration).

**Players never type anything:** embed the ticket in the game, or fetch the latest ticket from your own URL at startup; 6-character
codes are only for inviting friends to player-hosted rooms. Details: [Players never type anything](../DEVELOPER-GUIDE.md#2-players-never-type-anything).

### Requirements

- JDK 8 or newer to run (`peerova.jar` is compiled with `--release 8`). To build the JNI glue: a JDK (with `include/jni.h`) and `gcc`/`clang` (Windows: MinGW-w64).
- Core library from the SDK package:
  `lib/linux-x86_64/libpeerova.so`, `lib/windows-x86_64/peerova.dll`. Header: `include/peerova.h` (copy: `bindings/c/peerova.h`).

### Add `peerova.jar` to your project (recommended)

The SDK package contains **`java/peerova.jar`**: a single file with the Java API and the native library for
**Windows x86_64** (plus a legacy build for Windows 7 / 8.1 / Server 2012) and **Linux x86_64**. Just put it on the
classpath — no `-Djava.library.path`, no `.dll`/`.so` files to copy:

```sh
java -cp peerova.jar:. MyApp          # Windows: java -cp peerova.jar;. MyApp
```

- Maven/Gradle: use it as a local jar, e.g. Gradle `implementation(files("libs/peerova.jar"))`.
- The first time the `Peerova` class loads, the native library for the current OS/architecture is extracted to a
  **per-user** cache directory (Windows `%LOCALAPPDATA%\Peerova\java`, Linux `~/.cache/peerova/java`), versioned by
  release and SHA-256, and its SHA-256 is verified before loading. Nothing is written to the working directory. Later runs reuse the file.
- Windows 7 / 8.1 / Server 2012 automatically get the legacy build (force with `-Dpeerova.windows.legacy=true|false`).
- Change the cache directory with `-Dpeerova.cache.dir=/some/dir`. Disable extraction (use only libraries you place yourself): `-Dpeerova.native.extract=false`.
- Other platforms (macOS, ...) or when extraction is impossible: `Peerova` falls back to loading from `java.library.path` as in the table below.
- Android does **not** use this jar: use the AAR / `jniLibs`, see [`../kotlin-android`](../kotlin-android).

### Placing the native libraries yourself (optional)

Only needed when you don't use the libraries inside `peerova.jar` (Android, macOS, custom builds). **Two** libraries
are needed: `libpeerova` (core) and `libpeerova_jni` (JNI glue, built from `peerova_jni.c`). `Peerova` loads them with
`System.loadLibrary`, i.e. from `java.library.path`; or give absolute paths with
`-Dpeerova.lib.path=/…/libpeerova.so` and `-Dpeerova.jni.path=/…/libpeerova_jni.so` (when set, the jar's libraries are skipped).

| OS | Files | Location |
|---|---|---|
| Linux x86_64 | `libpeerova.so` + `libpeerova_jni.so` | same directory, run `java -Djava.library.path=<dir>`. The `libpeerova_jni.so` built by `build.sh` has rpath `$ORIGIN`, so it finds `libpeerova.so` next to it; alternatively `LD_LIBRARY_PATH` |
| Windows x86_64 | `peerova.dll` + `peerova_jni.dll` | next to the `.exe`/launcher, or in the `-Djava.library.path` directory. `wintun.dll` is **not** needed (only the `peerova` command-line tool needs it, for its VPN mode) |
| macOS | `libpeerova.dylib` + `libpeerova_jni.dylib` | contact Peerova for macOS builds; inside an app bundle put them in `Contents/Frameworks` |
| Android | `jniLibs/<abi>/libpeerova.so` + `libpeerova_jni.so` | contact Peerova for Android builds; see [`../kotlin-android/README.md`](../kotlin-android/README.md) |
| iOS | — | JNI does not run on iOS; contact Peerova (static library `libpeerova.a`, use the Swift binding) |

### Install

Linux / macOS (from `bindings/java`):

```sh
./build.sh build              # -> build/peerova.jar, build/libpeerova_jni.so, build/libpeerova.so (copy)
./build.sh                    # build + run the round-trip test (prints "PASS")
```

`build.sh` looks for `libpeerova.so` in the SDK package's `lib/linux-x86_64`; override with `PEEROVA_LIB_DIR=/some/dir`.
Usually you **don't need to build** anything: use the ready-made `java/peerova.jar` from the SDK package.
The header comes from `../c` (override with `PEEROVA_INCLUDE`). `JAVA_HOME` is detected from `javac`.

Windows (MinGW-w64, in `bindings\java`):

```bat
x86_64-w64-mingw32-gcc -O2 -shared peerova_jni.c -I"%JAVA_HOME%\include" -I"%JAVA_HOME%\include\win32" -I..\c ^
    -L..\..\lib\windows-x86_64 -lpeerova -o peerova_jni.dll
javac --release 8 -d classes src\dev\peerova\Peerova.java src\dev\peerova\NativeLoader.java && jar cf peerova.jar -C classes .
```

Then add `peerova.jar` to the classpath (Maven/Gradle: as a local jar) and place the native libraries as in the table above.

### 5-minute quick start

Server:

```java
import dev.peerova.Peerova;

try (Peerova.Server srv = Peerova.Server.start("{\"port\":47000,\"key_file\":\"server.key\"}")) {
    System.out.println("ticket: " + srv.ticket());        // give this string to clients
    while (true) {
        Peerova.Event ev = srv.poll(100);                  // wait up to 100 ms
        if (ev == null) continue;
        if (ev.type == Peerova.CONNECTED) srv.send(ev.peer, "welcome", 0, true);
        if (ev.type == Peerova.MESSAGE) System.out.println(ev.peer + ": " + ev.text());
        if (srv.roomCode() != null) { /* 6-char connect code, only with "app" + "share" config */ }
    }
}
```

`Server.start` blocks until the ticket is ready (about 3 seconds at most). The ticket is stable for the same `key_file`.

Connect code: start the server with `"app"` (your project id from the Peerova website) and `"share"`, then call
`srv.roomCode()` periodically until it is non-null (the website assigns it shortly after the server starts):

```java
Peerova.Server srv = Peerova.Server.start(
    "{\"app\":\"prj_xxx\",\"share\":{\"name\":\"Lan's room\",\"public\":true,\"max\":8}}");
String code = srv.roomCode();   // e.g. "K7Q2XD", or null if not available yet
```

Client — pass the ticket **or** the 6-character connect code:

```java
try (Peerova.Client cli = Peerova.Client.connect(ticketOrCode)) {   // returns immediately
    Peerova.Event ev = cli.poll(20000);
    if (ev == null || ev.type == Peerova.CONNECT_FAILED) throw new RuntimeException(ev == null ? "timeout" : ev.text());
    long server = ev.peer;                                           // CONNECTED
    cli.send(server, "hello", 0, true);
}
```

With a ticket, `connect` returns immediately. With a connect code, `connect` first looks the code up on the Peerova
website (one HTTPS request, needs Internet) and throws `PeerovaException` if the code does not exist.

Connections are automatic: direct first; when a network blocks direct connections Peerova uses the **automatic encrypted route** (still end-to-end encrypted).

Complete example: [`examples/Chat.java`](examples/Chat.java) (chat + 20 Hz position sync). How to run is at the top of the file.

### Reliable vs unreliable

`send(peer, data, channel, reliable)` — `channel` is 0 to 255.

| | `reliable = true` | `reliable = false` |
|---|---|---|
| Guarantee | delivered, in order within the channel | may be lost or reordered |
| Size | can be large | at most **1180 bytes** (`Peerova.MAX_UNRELIABLE_PAYLOAD`); larger is rejected |
| Use for | chat, inventory, RPCs, file chunks | inputs, positions, voice-like realtime state — newer packets replace older ones |

Channels are independent: a delayed reliable message on channel 0 does not block channel 1. Split data kinds across
channels (e.g. 0 = chat, 1 = positions, 2 = RPC). Overloads: `send(peer, String, …)`, `send(peer, byte[], off, len, …)`
and `send(peer, ByteBuffer, …)` (direct ByteBuffer: no copy).

### Receiving events

`poll()` (non-blocking) or `poll(timeoutMs)` returns a `Peerova.Event` or `null`:

| `ev.type` | Meaning | Fields |
|---|---|---|
| `CONNECTED` | server: a client joined; client: connected to the server | `peer` |
| `MESSAGE` | a message arrived | `peer`, `channel`, `reliable`, `data` / `text()` |
| `DISCONNECTED` | a peer left | `peer`, `text()` = reason |
| `CONNECT_FAILED` | (client) the connection attempt failed | `text()` = reason |

`ev.data` is a Java copy and may be kept. Common loop patterns:

- **App/server**: a dedicated thread calling `poll(100)` in a loop, handing events to other threads through a queue if needed.
- **Game loop**: each frame drain pending events with the non-blocking `while ((ev = node.poll()) != null)`.

### Threading rules

`send`, `poll`, `disconnect`, `peerCount` are safe to call from multiple threads. Run only **one** poll loop per node.
**Never** call `close()` while another thread is inside `poll`/`send` on the same node: stop the poll thread first, then `close()`.
On Android do not call `Server.start`, `poll(timeout)` or `Client.connect` with a connect code on the main thread.

### Disconnect and cleanup

- `node.disconnect(peer)` closes one connection (a `DISCONNECTED` event follows).
- `node.close()` (or try-with-resources) disconnects all peers and frees the native node; calling it twice is fine.
  After closing, every method throws `IllegalStateException`.
- Before closing a client, `poll` for a few hundred ms so reliable messages you just sent can arrive.

### Error handling

- Native failures throw `Peerova.PeerovaException`: `e.code` is a stable error code (`Peerova.E_TOO_LARGE`, `E_UNKNOWN_PEER`,
  `E_CODE_NOT_FOUND`, ...; full table: [Error codes](../DEVELOPER-GUIDE.md#error-codes)); the message is English text `"E_NAME: ..."`.
  `CONNECT_FAILED` / `DISCONNECTED` reasons use the same format: `ev.errorCode()`.
- `trySend(...)` returns `false` instead of throwing (handy for high-rate unreliable sends).
- Argument errors: `IllegalArgumentException` (channel outside 0..255, empty ticket), `IndexOutOfBoundsException` (bad off/len).
- `UnsatisfiedLinkError` when the `Peerova` class loads: native library not found (see Troubleshooting).

### Configuration

Server (`Server.start(json)`, `json` may be `null`):

| Key | Default | Meaning |
|---|---|---|
| `port` | `47000` | listen port; `0` = random port |
| `key_file` | `"server.key"` | server identity key file; same key ⇒ same ticket. Created if missing |
| `license` | — | `PVA-...` license key. Without it: dev mode, max 3 clients |
| `max_ccu` | from license | cap on concurrent clients |
| `token` | — | shared secret; clients must present the same `token` |
| `upnp` | `true` | open the port on the router via UPnP |
| `private` | `false` | private ticket: the ticket does not contain the server's IP addresses |
| `hide_ip` | `false` | Hide the server IP: players never learn its real address (DDoS protection); slightly higher latency. Pro/Studio/Enterprise plans and the trial |
| `app` | — | project id from the Peerova website (needed for connect codes) |
| `share` | — | `{"name":"...","public":true,"max":16,"mode":"...","region":"VN"}`: listing published on the website to get a connect code |

Client (`Client.connect(ticket, json)`): `token` (must match the server), `timeout_ms` (default `20000`), `key_file` (optional:
the client identity file, created if missing, the folder must exist — store it in the user's app-data folder, e.g. `"<appdata>/<game>/peerova-client.key"`,
so the player keeps the same identity and the same stable address on the server across runs; banning by address works per player
only while they keep that file — accounts / tokens are the robust way).

### Existing game: run peerova hidden

When the game/app already has TCP/UDP socket code and players **must not run anything**: ship `peerova.exe` (Windows) /
`peerova` (Linux, macOS) with the app (from the SDK package's `lib/<platform>/`). The app starts
`peerova client <TICKET> --forward tcp:8888 --key <appdata>/<game>/peerova-client.key` (Java opens no console window on Windows),
reads the output (UTF-8) line by line, treats it as **ready** when a line contains the ASCII text `server port` (the tool prints
`✔ ĐÃ KẾT NỐI …` ("connected") then `✔ Tcp 127.0.0.1:8888 → server port 8888`, once per `--forward`), then the existing socket code
connects to `127.0.0.1:8888`. It fails if the process exits first (exit code `1`, a line starting with `lỗi:` = "error:") or after 30 seconds
(the process is killed). The process is killed when the JVM exits. `peerova.jar` is not needed for this.

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

- Linux/macOS: the `peerova` file must be executable (`chmod +x`). The `--key` folder must exist (the code above creates it).
- If port 8888 may be in use on the player's machine, pick another local port: `tcp:38888=8888`.
- Other `peerova client` options: `--reconnect`, `--token SECRET`, `--bind IP`.
- Android can't run the command-line tool as a child process — use the SDK (see [`../kotlin-android`](../kotlin-android)).
- `peerova stdio` does **not** open local ports (it is the message SDK as JSON lines), so it doesn't fit this pattern.
- Details: [The game runs peerova hidden](../DEVELOPER-GUIDE.md#4-the-game-runs-peerova-hidden-players-run-nothing).

### Game servers

Several ports from one process (repeat `--expose`), Remote Desktop for admins and the public game as separate processes, a separate
`127.a.b.c` address per player + bans with `--banned`, hiding the server IP (`--hide-ip` / `"hide_ip": true`), VPN mode:
[Game-server features](../DEVELOPER-GUIDE.md#7-game-server-features).

### Security

- `server.key` is the server's identity: anyone with this file can impersonate your server. Keep it secret, **do not commit** it.
- The ticket and connect code are not secrets: embed them in the game or share them with clients.
- `token` and `license` are secrets: do not embed them in publicly distributed clients unless required.
- All traffic is end-to-end encrypted.

### Troubleshooting

- Network diagnosis: run `peerova doctor` with the `peerova` command-line tool (in the SDK package's `lib/<platform>/`).
- `UnsatisfiedLinkError` with `peerova.jar`: the message says why the jar's library could not be used (e.g. cache directory not
  writable → set `-Dpeerova.cache.dir`; platform not in the jar → place the libraries yourself).
- `UnsatisfiedLinkError: no peerova_jni in java.library.path`: make sure `-Djava.library.path` points to the directory
  containing `libpeerova_jni.so`/`peerova_jni.dll`, and `libpeerova.so`/`peerova.dll` sits next to it (or is on `LD_LIBRARY_PATH`/`PATH`).
- `CONNECT_FAILED` after about 20 seconds (timeout): server not running, wrong ticket/code, or `token` mismatch. Run `peerova doctor` on both machines.
- The 4th client is refused: the server is in dev mode (no `license`), max 3 clients.
- `PeerovaException` on an unreliable send: payload larger than 1180 bytes — split it or send it reliably.

### FAQ

- **Do players need to type a code?** No. Embed the ticket in the game or fetch it from your URL; 6-character codes are only for inviting friends.
- **Do I need to copy `.dll`/`.so` files?** No, on Windows/Linux x86_64: `peerova.jar` contains the native libraries and extracts them to a per-user cache.
- **Is the ticket a secret?** No; `server.key`, `license` and `token` are.
- **Can't connect?** Run `peerova doctor` on both machines. More: [Troubleshooting](../DEVELOPER-GUIDE.md#15-troubleshooting) · [FAQ](../DEVELOPER-GUIDE.md#16-faq-1).
