# Peerova cho Kotlin / Android

- [Tiếng Việt](#tiếng-việt)
- [English](#english)

---

## Tiếng Việt

API Kotlin (`dev.peerova.kt`) cho Peerova: kết nối P2P trực tiếp, mã hoá đầu-cuối. Đây là lớp bọc trên binding JNI
của Java trong [`../java`](../java) (`dev.peerova.Peerova` + `peerova_jni.c`). Cùng một mã chạy trên JVM
(Kotlin desktop/server) và Android (API 21+).

Khái niệm (server, client, ticket, connect code, channel, sự kiện) và toàn bộ C API: [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md).
Website: https://peerova.pages.dev

### Chọn cách tích hợp

- **Android:** chỉ dùng SDK (tình huống 3/4 — game mới hoặc host trong game). Android không chạy được công cụ dòng lệnh `peerova`
  như process con, nên không có cách "game có sẵn chạy peerova ẩn" trên Android.
- **Kotlin trên JVM desktop/server** (Windows, Linux, macOS): dùng SDK như trên, hoặc — nếu game đã có code socket TCP/UDP — chạy
  `peerova client` ẩn rồi kết nối `127.0.0.1` (tình huống 2): xem [Game có sẵn: chạy peerova ẩn](#game-có-sẵn-chạy-peerova-ẩn-jvm-desktop).
- Bảng đầy đủ 4 tình huống: [Chọn cách tích hợp](../DEVELOPER-GUIDE.md#1-chọn-cách-tích-hợp).

**Người chơi không phải gõ gì:** nhúng ticket vào app, hoặc tải ticket mới nhất từ URL của bạn khi khởi động; mã 6 ký tự chỉ
dùng để mời bạn bè vào phòng do người chơi mở. Chi tiết: [Người chơi không phải gõ gì](../DEVELOPER-GUIDE.md#2-người-chơi-không-phải-gõ-gì).

```
kotlin-android/
├── test.sh                         test trên JVM (kotlinc + JDK)          → "kotlin PASS: ..."
├── test/PeerovaKtTest.kt
├── examples/ChatDemo.kt            ví dụ chat + đồng bộ vị trí (chạy trên JVM)
└── peerova/                        module thư viện Android (chép hoặc include)
    ├── build.gradle.kts            AGP + CMake + jniLibs + mã Java từ ../../java/src
    ├── consumer-rules.pro          luật giữ lớp cho R8 (lớp Event được tạo từ JNI)
    └── src/main/
        ├── AndroidManifest.xml     quyền INTERNET
        ├── kotlin/dev/peerova/kt/Peerova.kt       PeerovaServer / PeerovaClient / PeerovaEvent
        ├── kotlin/dev/peerova/kt/PeerovaFlow.kt   coroutines (tuỳ chọn): eventFlow(), startAsync()
        ├── cpp/CMakeLists.txt      build libpeerova_jni.so cho từng ABI, link với libpeerova.so có sẵn
        └── jniLibs/<abi>/libpeerova.so            (bạn đặt vào, xem dưới)
```

### Yêu cầu

- Kotlin 1.8+ (đã kiểm tra với Kotlin 2.x), JVM 8+. Coroutines (`PeerovaFlow.kt`) cần `kotlinx-coroutines` 1.6+ (tuỳ chọn).
- Android: minSdk 21, Android Gradle Plugin + NDK r23 trở lên nếu để Gradle build `libpeerova_jni.so` (cách A bên dưới).
- Desktop/JVM: `java/peerova.jar` của gói SDK (một file, có sẵn thư viện native cho Windows x86_64 — kèm bản legacy cho Windows 7 /
  8.1 / Server 2012 — và Linux x86_64, tự giải nén vào thư mục cache của người dùng) + hai file `.kt`. Xem [`../java/README.md`](../java/README.md).

### Đặt thư viện native ở đâu

| Nền tảng | Tệp | Vị trí |
|---|---|---|
| Android | `libpeerova.so` + `libpeerova_jni.so` cho từng ABI | liên hệ Peerova để nhận bản build Android. Đặt vào `src/main/jniLibs/<abi>/` (`arm64-v8a`, `armeabi-v7a`, `x86_64`); APK cuối cùng chứa `lib/<abi>/libpeerova.so` và `lib/<abi>/libpeerova_jni.so` |
| Linux / Windows x86_64 (JVM) | — | không cần đặt gì: có sẵn trong `peerova.jar`. Muốn tự đặt thư viện: xem [`../java/README.md`](../java/README.md). **Không** cần `wintun.dll` (chỉ công cụ dòng lệnh `peerova` cần cho chế độ VPN) |
| macOS (JVM) | `libpeerova.dylib` + `libpeerova_jni.dylib` | liên hệ Peerova; trong app bundle đặt ở `Contents/Frameworks` |
| iOS | — | Kotlin/JNI không dùng trên iOS; liên hệ Peerova (thư viện tĩnh `libpeerova.a`, binding Swift) |

### Cài đặt trên Android

**Cách A — dùng module `:peerova` (Gradle build `libpeerova_jni.so` bằng CMake, cần NDK):**

1. Đặt `libpeerova.so` của từng ABI vào `bindings/kotlin-android/peerova/src/main/jniLibs/<abi>/`.
   Xoá ABI bạn không có khỏi `abiFilters` trong `peerova/build.gradle.kts`.
2. Include module:
   ```kotlin
   // settings.gradle.kts
   include(":peerova")
   project(":peerova").projectDir = file("<thư mục SDK>/bindings/kotlin-android/peerova")
   // app/build.gradle.kts
   dependencies { implementation(project(":peerova")) }
   ```
   Nếu chép module ra khỏi gói SDK, truyền `-DPEEROVA_REPO=<thư mục SDK>` cho CMake (cần `bindings/java/peerova_jni.c` và `bindings/c/peerova.h`).

**Cách B — không cần NDK:** đặt cả `libpeerova.so` và `libpeerova_jni.so` (bản build sẵn) vào
`app/src/main/jniLibs/<abi>/`, chép `../java/src/dev/peerova/Peerova.java`, `../java/src/dev/peerova/NativeLoader.java` và hai file `.kt` vào app, thêm nội dung
`peerova/consumer-rules.pro` vào luật R8 của app.

Cả hai cách:

- Quyền `android.permission.INTERNET` là bắt buộc (cách A tự gộp từ manifest của module; cách B tự thêm vào `AndroidManifest.xml`).
- Luật R8/ProGuard: lớp `dev.peerova.Peerova$Event` được tạo từ mã C, phải giữ nguyên (`consumer-rules.pro`).
- **Không** gọi `PeerovaServer.start` hay `poll(timeout)` trên main thread (start chặn tới ~3 giây): dùng `startAsync`,
  `Dispatchers.IO` hoặc thread nền. `PeerovaClient.connect` với ticket trả về ngay; với connect code nó tra mã trên
  website Peerova (một request HTTPS) nên cũng phải gọi ngoài main thread.
- `CMakeLists.txt` link `libpeerova_jni.so` với căn trang 16 KB (yêu cầu cho app nhắm Android 15+).

### Bắt đầu nhanh (5 phút)

Server:

```kotlin
import dev.peerova.kt.*

val srv = PeerovaServer.startAsync(mapOf("port" to 47000, "key_file" to "$filesDir/server.key"))  // trong coroutine
println(srv.ticket)                     // "pvt_..." — gửi cho client
srv.startPolling(ContextCompat.getMainExecutor(context)) { ev ->   // listener chạy trên UI thread
    when (ev) {
        is PeerovaEvent.Connected     -> srv.send(ev.peer, "chào")
        is PeerovaEvent.Message       -> Log.d("p2p", "${ev.peer} ch${ev.channel}: ${ev.text}")
        is PeerovaEvent.Disconnected  -> Log.d("p2p", "rời: ${ev.reason}")
        is PeerovaEvent.ConnectFailed -> {}
    }
}
// ... srv.close() dừng vòng poll trước rồi giải phóng node
```

Trên Android, đặt `key_file` trong `filesDir` (thư mục làm việc của app không ghi được). Ticket ổn định với cùng `key_file`.

Connect code: khởi động server với `"app"` (mã project trên website Peerova) và `"share"`, rồi đọc `srv.roomCode`
định kỳ tới khi khác `null` (website cấp mã ngay sau khi server khởi động):

```kotlin
val srv = PeerovaServer.startAsync(mapOf(
    "app" to "prj_xxx",
    "share" to mapOf("name" to "Phòng của Lan", "public" to true, "max" to 8)))
val code: String? = srv.roomCode        // ví dụ "K7Q2XD", hoặc null nếu chưa có
```

Client — truyền ticket **hoặc** connect code 6 ký tự:

```kotlin
lifecycleScope.launch {
    val cli = withContext(Dispatchers.IO) { PeerovaClient.connect(ticketOrCode) }   // connect code: cần tra website
    cli.eventFlow().collect { ev ->                        // thu thập trên Main
        when (ev) {
            is PeerovaEvent.Connected     -> cli.send(ev.peer, "xin chào")
            is PeerovaEvent.Message       -> show(ev.text)
            is PeerovaEvent.ConnectFailed -> showError(ev.reason)
            is PeerovaEvent.Disconnected  -> {}
        }
    }
}
```

Kết nối là tự động: thử kết nối trực tiếp trước; khi mạng chặn kết nối trực tiếp, Peerova tự dùng **đường mã hoá tự động** (vẫn mã hoá đầu-cuối).

Ví dụ đầy đủ: [`examples/ChatDemo.kt`](examples/ChatDemo.kt) (chat + đồng bộ vị trí 20 Hz, chạy trên JVM). Cách chạy ở đầu file.

### Gửi reliable hay unreliable

`send(peer, data, channel = 0, reliable = true)` — `channel` từ 0 đến 255; có overload cho `String` và `ByteArray`.

| | `reliable = true` | `reliable = false` |
|---|---|---|
| Đảm bảo | tới nơi, đúng thứ tự trong cùng channel | có thể mất hoặc đảo thứ tự |
| Kích thước | có thể lớn | tối đa **1180 byte** (`MAX_UNRELIABLE_PAYLOAD`); lớn hơn bị từ chối |
| Dùng cho | chat, inventory, RPC, khối dữ liệu file | input, vị trí, trạng thái realtime kiểu voice — gói mới thay gói cũ |

Các channel độc lập: một tin reliable bị chậm trên channel 0 không chặn channel 1.

### Nhận sự kiện

`PeerovaEvent` là sealed class: `Connected(peer)`, `Message(peer, channel, reliable, data)` (+ `text`),
`Disconnected(peer, reason)`, `ConnectFailed(reason)` (chỉ client). Payload là bản sao, giữ lại thoải mái. Ba cách nhận:

- `poll(timeoutMs)` / `events(idleMs)`: kéo thủ công (game loop: `poll()` không chặn mỗi frame).
- `startPolling(executor) { ev -> }`: thread nền poll, gọi listener trên `executor` (Android: `ContextCompat.getMainExecutor(context)`).
  `close()` của node tự dừng vòng này.
- `eventFlow()` (coroutines): Flow lạnh, poll trên `Dispatchers.IO`, phát trong context của collector.

Chỉ dùng **một** cách cho mỗi node (sự kiện bị tiêu thụ khi đọc).

### Quy tắc thread

`send`, `poll`, `disconnect`, `peerCount` an toàn khi gọi từ nhiều thread. Chỉ một vòng poll cho mỗi node.
Không bao giờ `close()` khi thread khác đang ở trong `poll`/`send`: huỷ coroutine đang collect `eventFlow()` trước khi `close()`
(vòng `startPolling` thì `close()` tự dừng).

### Ngắt kết nối và dọn dẹp

- `disconnect(peer)` đóng một kết nối (sau đó có `Disconnected`).
- `close()` (hoặc `use { }`) ngắt mọi peer và giải phóng node; gọi nhiều lần không sao. Trên Android nên đóng trong `onDestroy`
  (hoặc khi `ViewModel.onCleared`). Sau khi đóng, mọi phương thức ném `IllegalStateException`.

### Xử lý lỗi

- Lỗi native ném `PeerovaException` (= `dev.peerova.Peerova.PeerovaException`): `e.code` là mã lỗi ổn định (`Peerova.E_TOO_LARGE`,
  `E_UNKNOWN_PEER`, `E_CODE_NOT_FOUND`…, hằng số trong `dev.peerova.Peerova`; bảng đầy đủ: [Mã lỗi](../DEVELOPER-GUIDE.md#mã-lỗi));
  `message` là văn bản tiếng Anh `"E_NAME: ..."`.
- `trySend(...)` trả `false` thay vì ném ngoại lệ.
- Lỗi kết nối tới qua sự kiện `ConnectFailed(reason)`, không phải ngoại lệ. Mã lỗi: `ev.errorCode` (cũng có ở `Disconnected`), ví dụ
  `if (ev.errorCode == Peerova.E_TIMEOUT) …`.
- `UnsatisfiedLinkError` khi nạp lần đầu: thiếu `libpeerova.so`/`libpeerova_jni.so` cho ABI của thiết bị.

### Cấu hình

Server (`PeerovaServer.start(map)` hoặc `start(json)`):

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
| `share` | — | map `{"name","public","max","mode","region"}`: thông tin đăng lên website để nhận connect code |

Client (`PeerovaClient.connect(ticket, map)`): `token` (phải khớp server), `timeout_ms` (mặc định `20000`), `key_file` (tuỳ chọn:
file danh tính client, tạo nếu chưa có, thư mục phải tồn tại — Android: `"$filesDir/peerova-client.key"`; desktop: thư mục dữ liệu ứng dụng,
ví dụ `"<appdata>/<game>/peerova-client.key"`. Giữ file thì người chơi giữ cùng danh tính và cùng địa chỉ ổn định trên server giữa các lần
chạy; chặn theo địa chỉ chỉ có tác dụng khi họ còn giữ file này, muốn chắc chắn hãy dùng tài khoản / token).

### Game có sẵn: chạy peerova ẩn (JVM desktop)

**Chỉ cho Kotlin trên JVM desktop/server — không dùng được trên Android.** Khi game đã có code socket TCP/UDP và người chơi
**không được phải chạy gì**: phát hành `peerova.exe` (Windows) / `peerova` (Linux, macOS) kèm game (lấy từ `lib/<nền tảng>/` của gói SDK).
Game chạy `peerova client <TICKET> --forward tcp:8888 --key <appdata>/<game>/peerova-client.key` (JVM không mở cửa sổ console trên
Windows), đọc output (UTF-8) từng dòng, coi là **sẵn sàng** khi một dòng chứa chuỗi ASCII `server port` (công cụ in `✔ ĐÃ KẾT NỐI …` rồi
`✔ Tcp 127.0.0.1:8888 → server port 8888`, mỗi `--forward` một dòng), rồi code socket cũ kết nối `127.0.0.1:8888`. Thất bại khi process
thoát trước (mã thoát `1`, dòng bắt đầu bằng `lỗi:`) hoặc quá 30 giây (process bị dừng). Process bị dừng khi JVM thoát.
Cách này không cần `peerova.jar`. (Bản Kotlin của đoạn Java trong hướng dẫn; đã chạy thử trên Linux.)

```kotlin
// PeerovaTunnel.kt — Kotlin on a desktop JVM (Windows/Linux/macOS), not Android.
// Runs the bundled `peerova` CLI and waits until the local port is open.
// The JVM starts console programs without a console window on Windows.
import java.io.File
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class PeerovaTunnel : AutoCloseable {
    private var proc: Process? = null

    /** Returns true once the forwarded port is open on 127.0.0.1; false on failure or timeout. */
    fun start(
        exe: String, ticket: String, forward: String, keyFile: File, timeoutMs: Long = 30_000,
        log: (String) -> Unit = { println("[peerova] $it") },
    ): Boolean {
        keyFile.absoluteFile.parentFile.mkdirs()                       // the --key folder must exist
        val p = ProcessBuilder(exe, "client", ticket, "--forward", forward, "--key", keyFile.path)
            .redirectErrorStream(true)                                 // stdout + stderr in one stream
            .start()
        proc = p
        Runtime.getRuntime().addShutdownHook(Thread { close() })

        val ready = CountDownLatch(1)
        thread(isDaemon = true, name = "peerova-output") {
            try {
                p.inputStream.bufferedReader(Charsets.UTF_8).forEachLine { line ->
                    log(line)
                    // "✔ Tcp 127.0.0.1:8888 → server port 8888" = connected and the local port is open
                    if ("server port" in line) ready.countDown()
                }
            } catch (_: IOException) { }
        }

        val deadline = System.currentTimeMillis() + timeoutMs
        while (!ready.await(100, TimeUnit.MILLISECONDS)) {
            if (!p.isAlive || System.currentTimeMillis() > deadline) { close(); return false }
        }
        return true
    }

    val isAlive: Boolean get() = proc?.isAlive == true

    override fun close() { proc?.takeIf { it.isAlive }?.destroyForcibly()?.waitFor(2, TimeUnit.SECONDS) }
}
```
```kotlin
// Usage (desktop JVM)
val win = System.getProperty("os.name").startsWith("Windows")
val appData = if (win) System.getenv("APPDATA") else System.getProperty("user.home") + "/.local/share"
val key = File(appData, "MyGame/peerova-client.key")
val exe = if (win) "peerova\\peerova.exe" else "peerova/peerova"   // next to your game

val tunnel = PeerovaTunnel()
if (tunnel.start(exe, "pvt_...", "tcp:8888", key)) {
    connectToGameServer("127.0.0.1", 8888)      // your existing socket code, unchanged
} else {
    showError("Could not reach the server")
}
```

- Linux/macOS: file `peerova` phải có quyền chạy (`chmod +x`). Thư mục chứa `--key` phải tồn tại (code trên tự tạo).
- Nếu cổng 8888 có thể đang bận trên máy người chơi, chọn cổng local khác: `tcp:38888=8888`.
- Tuỳ chọn khác của `peerova client`: `--reconnect`, `--token SECRET`, `--bind IP`.
- `peerova stdio` **không** mở cổng local (nó là SDK message dạng JSON lines), nên không dùng cho cách này.
- Chi tiết: [Game tự chạy peerova ẩn](../DEVELOPER-GUIDE.md#4-game-tự-chạy-peerova-ẩn-người-chơi-không-chạy-gì).

### Máy chủ game

Nhiều cổng trong một process (`--expose` lặp lại), tách Remote Desktop cho admin và game công khai thành các process riêng, mỗi người
chơi một địa chỉ `127.a.b.c` riêng + cấm bằng `--banned`, ẩn IP máy chủ (`--hide-ip` / `"hide_ip" to true`), chế độ VPN:
[Tính năng máy chủ game](../DEVELOPER-GUIDE.md#7-tính-năng-máy-chủ-game).

### Bảo mật

- `server.key` là danh tính của server: ai có file này có thể giả làm server của bạn. Giữ bí mật, **không commit** vào git;
  trên Android để trong `filesDir` (không phải bộ nhớ ngoài).
- Ticket và connect code không phải bí mật: cứ nhúng vào app hoặc chia sẻ cho client.
- `token` và `license` là bí mật: không nhúng vào APK phát hành công khai nếu không cần.
- Toàn bộ dữ liệu được mã hoá đầu-cuối.

### Khắc phục sự cố

- Chẩn đoán mạng: chạy `peerova doctor` bằng công cụ dòng lệnh `peerova` (trong `lib/<nền tảng>/` của gói SDK) trên máy tính cùng mạng.
- `UnsatisfiedLinkError: dlopen failed: library "libpeerova.so" not found`: thiếu file trong `jniLibs/<abi>/` cho ABI của thiết bị
  (emulator thường là `x86_64`), hoặc tên file sai.
- Ứng dụng đứng hình/ANR: đang gọi `start` hoặc `poll(timeout)` trên main thread.
- `ConnectFailed` sau khoảng 20 giây (timeout): server chưa chạy, ticket/code sai, `token` không khớp, hoặc thiếu quyền `INTERNET`.
- Client thứ 4 bị từ chối: server đang ở chế độ dev (không có `license`), tối đa 3 client.
- `PeerovaException` khi gửi unreliable: payload vượt 1180 byte — chia nhỏ hoặc gửi reliable.

### FAQ

- **Người chơi có phải gõ mã không?** Không. Nhúng ticket vào app hoặc tải từ URL của bạn; mã 6 ký tự chỉ để mời bạn bè.
- **Trên Android có chạy `peerova client` ẩn được không?** Không — Android không chạy được công cụ dòng lệnh như process con; dùng SDK.
- **Ticket có phải bí mật không?** Không; `server.key`, `license`, `token` mới là bí mật.
- **Kết nối không được?** Chạy `peerova doctor` trên máy tính cùng mạng. Thêm: [Khắc phục sự cố](../DEVELOPER-GUIDE.md#15-khắc-phục-sự-cố) · [FAQ](../DEVELOPER-GUIDE.md#16-faq).

### Trạng thái kiểm thử

- Test JVM (`./test.sh`): PASS (Kotlin 2.x, JDK 21, kotlinx-coroutines 1.9).
- Android: binding Java/Kotlin đã chạy round-trip trong process trên emulator Android 16 x86_64 (qua `app_process`).
  Build Gradle/AGP đầy đủ của module chưa được kiểm thử.

---

## English

Kotlin API (`dev.peerova.kt`) for Peerova: direct P2P connections, end-to-end encrypted. It wraps the Java JNI binding
in [`../java`](../java) (`dev.peerova.Peerova` + `peerova_jni.c`). The same code runs on the JVM (desktop/server Kotlin)
and on Android (API 21+).

Concepts (server, client, ticket, connect code, channels, events) and the full C API: [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md).
Website: https://peerova.pages.dev

### Choosing an integration

- **Android:** SDK only (scenarios 3/4 — new game or host-in-game). Android can't run the `peerova` command-line tool as a child
  process, so the "existing game runs peerova hidden" pattern is not available on Android.
- **Kotlin on a desktop/server JVM** (Windows, Linux, macOS): use the SDK as well, or — if the game already has TCP/UDP socket code —
  start `peerova client` hidden and connect to `127.0.0.1` (scenario 2): see [Existing game: run peerova hidden](#existing-game-run-peerova-hidden-desktop-jvm).
- All 4 scenarios: [Choosing an integration](../DEVELOPER-GUIDE.md#1-choosing-an-integration).

**Players never type anything:** embed the ticket in the app, or fetch the latest ticket from your own URL at startup; 6-character
codes are only for inviting friends to player-hosted rooms. Details: [Players never type anything](../DEVELOPER-GUIDE.md#2-players-never-type-anything).

```
kotlin-android/
├── test.sh                         JVM test (kotlinc + JDK)          → "kotlin PASS: ..."
├── test/PeerovaKtTest.kt
├── examples/ChatDemo.kt            chat + position-sync example (runs on the JVM)
└── peerova/                        Android library module (copy or include it)
    ├── build.gradle.kts            AGP + CMake + jniLibs + Java sources from ../../java/src
    ├── consumer-rules.pro          R8 keep rules for the JNI-constructed Event class
    └── src/main/
        ├── AndroidManifest.xml     INTERNET permission
        ├── kotlin/dev/peerova/kt/Peerova.kt       PeerovaServer / PeerovaClient / PeerovaEvent
        ├── kotlin/dev/peerova/kt/PeerovaFlow.kt   optional coroutines: eventFlow(), startAsync()
        ├── cpp/CMakeLists.txt      builds libpeerova_jni.so per ABI, links the prebuilt libpeerova.so
        └── jniLibs/<abi>/libpeerova.so            (you provide it, see below)
```

### Requirements

- Kotlin 1.8+ (tested with Kotlin 2.x), JVM 8+. Coroutines (`PeerovaFlow.kt`) need `kotlinx-coroutines` 1.6+ (optional).
- Android: minSdk 21, Android Gradle Plugin + NDK r23 or newer if Gradle builds `libpeerova_jni.so` (option A below).
- Desktop/JVM: the SDK package's `java/peerova.jar` (a single file with the native libraries for Windows x86_64 — plus a legacy build
  for Windows 7 / 8.1 / Server 2012 — and Linux x86_64, auto-extracted to a per-user cache directory) + the two `.kt` files. See [`../java/README.md`](../java/README.md).

### Where to put the native libraries

| Platform | Files | Location |
|---|---|---|
| Android | `libpeerova.so` + `libpeerova_jni.so` per ABI | contact Peerova for Android builds. Put them in `src/main/jniLibs/<abi>/` (`arm64-v8a`, `armeabi-v7a`, `x86_64`); the final APK contains `lib/<abi>/libpeerova.so` and `lib/<abi>/libpeerova_jni.so` |
| Linux / Windows x86_64 (JVM) | — | nothing to place: included in `peerova.jar`. To place the libraries yourself see [`../java/README.md`](../java/README.md). `wintun.dll` is **not** needed (only the `peerova` command-line tool needs it, for VPN mode) |
| macOS (JVM) | `libpeerova.dylib` + `libpeerova_jni.dylib` | contact Peerova; inside an app bundle put them in `Contents/Frameworks` |
| iOS | — | Kotlin/JNI is not used on iOS; contact Peerova (static library `libpeerova.a`, Swift binding) |

### Android setup

**Option A — use the `:peerova` module (Gradle builds `libpeerova_jni.so` with CMake, needs the NDK):**

1. Put each ABI's `libpeerova.so` in `bindings/kotlin-android/peerova/src/main/jniLibs/<abi>/`.
   Remove ABIs you do not have from `abiFilters` in `peerova/build.gradle.kts`.
2. Include the module:
   ```kotlin
   // settings.gradle.kts
   include(":peerova")
   project(":peerova").projectDir = file("<SDK dir>/bindings/kotlin-android/peerova")
   // app/build.gradle.kts
   dependencies { implementation(project(":peerova")) }
   ```
   If you copy the module out of the SDK package, pass `-DPEEROVA_REPO=<SDK dir>` to CMake (it needs `bindings/java/peerova_jni.c` and `bindings/c/peerova.h`).

**Option B — no NDK:** put both prebuilt `libpeerova.so` and `libpeerova_jni.so` in `app/src/main/jniLibs/<abi>/`,
copy `../java/src/dev/peerova/Peerova.java`, `../java/src/dev/peerova/NativeLoader.java` and the two `.kt` files into your app, and add the contents of
`peerova/consumer-rules.pro` to your app's R8 rules.

Either way:

- The `android.permission.INTERNET` permission is required (option A merges it from the module manifest; with option B add it to your `AndroidManifest.xml`).
- R8/ProGuard: `dev.peerova.Peerova$Event` is constructed from C code and must be kept (`consumer-rules.pro`).
- **Do not** call `PeerovaServer.start` or `poll(timeout)` on the main thread (start blocks up to ~3 s): use `startAsync`,
  `Dispatchers.IO` or a background thread. `PeerovaClient.connect` with a ticket returns immediately; with a connect
  code it looks the code up on the Peerova website (one HTTPS request), so call it off the main thread too.
- `CMakeLists.txt` links `libpeerova_jni.so` with 16 KB page alignment (required for apps targeting Android 15+).

### 5-minute quick start

Server:

```kotlin
import dev.peerova.kt.*

val srv = PeerovaServer.startAsync(mapOf("port" to 47000, "key_file" to "$filesDir/server.key"))  // in a coroutine
println(srv.ticket)                     // "pvt_..." — give it to clients
srv.startPolling(ContextCompat.getMainExecutor(context)) { ev ->   // listener runs on the UI thread
    when (ev) {
        is PeerovaEvent.Connected     -> srv.send(ev.peer, "welcome")
        is PeerovaEvent.Message       -> Log.d("p2p", "${ev.peer} ch${ev.channel}: ${ev.text}")
        is PeerovaEvent.Disconnected  -> Log.d("p2p", "left: ${ev.reason}")
        is PeerovaEvent.ConnectFailed -> {}
    }
}
// ... srv.close() stops the poll loop first, then frees the node
```

On Android, put `key_file` in `filesDir` (the process working directory is not writable). The ticket is stable for the same `key_file`.

Connect code: start the server with `"app"` (your project id from the Peerova website) and `"share"`, then read
`srv.roomCode` periodically until it is non-null (the website assigns it shortly after the server starts):

```kotlin
val srv = PeerovaServer.startAsync(mapOf(
    "app" to "prj_xxx",
    "share" to mapOf("name" to "Lan's room", "public" to true, "max" to 8)))
val code: String? = srv.roomCode        // e.g. "K7Q2XD", or null if not available yet
```

Client — pass the ticket **or** the 6-character connect code:

```kotlin
lifecycleScope.launch {
    val cli = withContext(Dispatchers.IO) { PeerovaClient.connect(ticketOrCode) }   // a code needs a website lookup
    cli.eventFlow().collect { ev ->                        // collected on Main
        when (ev) {
            is PeerovaEvent.Connected     -> cli.send(ev.peer, "hello")
            is PeerovaEvent.Message       -> show(ev.text)
            is PeerovaEvent.ConnectFailed -> showError(ev.reason)
            is PeerovaEvent.Disconnected  -> {}
        }
    }
}
```

Connections are automatic: direct first; when a network blocks direct connections Peerova uses the **automatic encrypted route** (still end-to-end encrypted).

Complete example: [`examples/ChatDemo.kt`](examples/ChatDemo.kt) (chat + 20 Hz position sync, runs on the JVM). How to run is at the top of the file.

### Reliable vs unreliable

`send(peer, data, channel = 0, reliable = true)` — `channel` is 0 to 255; overloads for `String` and `ByteArray`.

| | `reliable = true` | `reliable = false` |
|---|---|---|
| Guarantee | delivered, in order within the channel | may be lost or reordered |
| Size | can be large | at most **1180 bytes** (`MAX_UNRELIABLE_PAYLOAD`); larger is rejected |
| Use for | chat, inventory, RPCs, file chunks | inputs, positions, voice-like realtime state — newer packets replace older ones |

Channels are independent: a delayed reliable message on channel 0 does not block channel 1.

### Receiving events

`PeerovaEvent` is a sealed class: `Connected(peer)`, `Message(peer, channel, reliable, data)` (+ `text`),
`Disconnected(peer, reason)`, `ConnectFailed(reason)` (client only). Payloads are copies and may be kept. Three ways to receive:

- `poll(timeoutMs)` / `events(idleMs)`: pull manually (game loop: non-blocking `poll()` each frame).
- `startPolling(executor) { ev -> }`: a background thread polls and calls the listener on `executor` (Android: `ContextCompat.getMainExecutor(context)`).
  The node's `close()` stops this loop.
- `eventFlow()` (coroutines): cold Flow, polls on `Dispatchers.IO`, emits in the collector's context.

Use only **one** of them per node (events are consumed when read).

### Threading rules

`send`, `poll`, `disconnect`, `peerCount` are safe to call from multiple threads. One poll loop per node.
Never `close()` while another thread is inside `poll`/`send`: cancel the coroutine collecting `eventFlow()` before `close()`
(a `startPolling` loop is stopped by `close()` itself).

### Disconnect and cleanup

- `disconnect(peer)` closes one connection (a `Disconnected` follows).
- `close()` (or `use { }`) disconnects all peers and frees the node; calling it twice is fine. On Android close it in `onDestroy`
  (or `ViewModel.onCleared`). After closing, every method throws `IllegalStateException`.

### Error handling

- Native failures throw `PeerovaException` (= `dev.peerova.Peerova.PeerovaException`): `e.code` is a stable error code (`Peerova.E_TOO_LARGE`,
  `E_UNKNOWN_PEER`, `E_CODE_NOT_FOUND`, ..., constants in `dev.peerova.Peerova`; full table: [Error codes](../DEVELOPER-GUIDE.md#error-codes));
  `message` is English text `"E_NAME: ..."`.
- `trySend(...)` returns `false` instead of throwing.
- Connection failures arrive as a `ConnectFailed(reason)` event, not as an exception. Error code: `ev.errorCode` (also on `Disconnected`), e.g.
  `if (ev.errorCode == Peerova.E_TIMEOUT) …`.
- `UnsatisfiedLinkError` on first load: `libpeerova.so`/`libpeerova_jni.so` missing for the device's ABI.

### Configuration

Server (`PeerovaServer.start(map)` or `start(json)`):

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
| `share` | — | map `{"name","public","max","mode","region"}`: listing published on the website to get a connect code |

Client (`PeerovaClient.connect(ticket, map)`): `token` (must match the server), `timeout_ms` (default `20000`), `key_file` (optional:
the client identity file, created if missing, the folder must exist — Android: `"$filesDir/peerova-client.key"`; desktop: the user's
app-data folder, e.g. `"<appdata>/<game>/peerova-client.key"`. Keeping it keeps the player's identity and the same stable address on the
server across runs; banning by address works per player only while they keep that file — accounts / tokens are the robust way).

### Existing game: run peerova hidden (desktop JVM)

**Kotlin on a desktop/server JVM only — not available on Android.** When the game already has TCP/UDP socket code and players
**must not run anything**: ship `peerova.exe` (Windows) / `peerova` (Linux, macOS) with the game (from the SDK package's `lib/<platform>/`).
The game starts `peerova client <TICKET> --forward tcp:8888 --key <appdata>/<game>/peerova-client.key` (the JVM opens no console window
on Windows), reads the output (UTF-8) line by line, treats it as **ready** when a line contains the ASCII text `server port` (the tool prints
`✔ ĐÃ KẾT NỐI …` ("connected") then `✔ Tcp 127.0.0.1:8888 → server port 8888`, once per `--forward`), then the existing socket code connects
to `127.0.0.1:8888`. It fails if the process exits first (exit code `1`, a line starting with `lỗi:` = "error:") or after 30 seconds (the
process is killed). The process is killed when the JVM exits. `peerova.jar` is not needed for this. (A Kotlin version of the guide's
Java snippet; tested on Linux.)

```kotlin
// PeerovaTunnel.kt — Kotlin on a desktop JVM (Windows/Linux/macOS), not Android.
// Runs the bundled `peerova` CLI and waits until the local port is open.
// The JVM starts console programs without a console window on Windows.
import java.io.File
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class PeerovaTunnel : AutoCloseable {
    private var proc: Process? = null

    /** Returns true once the forwarded port is open on 127.0.0.1; false on failure or timeout. */
    fun start(
        exe: String, ticket: String, forward: String, keyFile: File, timeoutMs: Long = 30_000,
        log: (String) -> Unit = { println("[peerova] $it") },
    ): Boolean {
        keyFile.absoluteFile.parentFile.mkdirs()                       // the --key folder must exist
        val p = ProcessBuilder(exe, "client", ticket, "--forward", forward, "--key", keyFile.path)
            .redirectErrorStream(true)                                 // stdout + stderr in one stream
            .start()
        proc = p
        Runtime.getRuntime().addShutdownHook(Thread { close() })

        val ready = CountDownLatch(1)
        thread(isDaemon = true, name = "peerova-output") {
            try {
                p.inputStream.bufferedReader(Charsets.UTF_8).forEachLine { line ->
                    log(line)
                    // "✔ Tcp 127.0.0.1:8888 → server port 8888" = connected and the local port is open
                    if ("server port" in line) ready.countDown()
                }
            } catch (_: IOException) { }
        }

        val deadline = System.currentTimeMillis() + timeoutMs
        while (!ready.await(100, TimeUnit.MILLISECONDS)) {
            if (!p.isAlive || System.currentTimeMillis() > deadline) { close(); return false }
        }
        return true
    }

    val isAlive: Boolean get() = proc?.isAlive == true

    override fun close() { proc?.takeIf { it.isAlive }?.destroyForcibly()?.waitFor(2, TimeUnit.SECONDS) }
}
```
```kotlin
// Usage (desktop JVM)
val win = System.getProperty("os.name").startsWith("Windows")
val appData = if (win) System.getenv("APPDATA") else System.getProperty("user.home") + "/.local/share"
val key = File(appData, "MyGame/peerova-client.key")
val exe = if (win) "peerova\\peerova.exe" else "peerova/peerova"   // next to your game

val tunnel = PeerovaTunnel()
if (tunnel.start(exe, "pvt_...", "tcp:8888", key)) {
    connectToGameServer("127.0.0.1", 8888)      // your existing socket code, unchanged
} else {
    showError("Could not reach the server")
}
```

- Linux/macOS: the `peerova` file must be executable (`chmod +x`). The `--key` folder must exist (the code above creates it).
- If port 8888 may be in use on the player's machine, pick another local port: `tcp:38888=8888`.
- Other `peerova client` options: `--reconnect`, `--token SECRET`, `--bind IP`.
- `peerova stdio` does **not** open local ports (it is the message SDK as JSON lines), so it doesn't fit this pattern.
- Details: [The game runs peerova hidden](../DEVELOPER-GUIDE.md#4-the-game-runs-peerova-hidden-players-run-nothing).

### Game servers

Several ports from one process (repeat `--expose`), Remote Desktop for admins and the public game as separate processes, a separate
`127.a.b.c` address per player + bans with `--banned`, hiding the server IP (`--hide-ip` / `"hide_ip" to true`), VPN mode:
[Game-server features](../DEVELOPER-GUIDE.md#7-game-server-features).

### Security

- `server.key` is the server's identity: anyone with this file can impersonate your server. Keep it secret, **do not commit** it;
  on Android keep it in `filesDir` (not external storage).
- The ticket and connect code are not secrets: embed them in the app or share them with clients.
- `token` and `license` are secrets: do not embed them in a publicly distributed APK unless required.
- All traffic is end-to-end encrypted.

### Troubleshooting

- Network diagnosis: run `peerova doctor` with the `peerova` command-line tool (in the SDK package's `lib/<platform>/`) on a computer on the same network.
- `UnsatisfiedLinkError: dlopen failed: library "libpeerova.so" not found`: the file is missing from `jniLibs/<abi>/` for the device's ABI
  (emulators are usually `x86_64`), or misnamed.
- UI freezes / ANR: `start` or `poll(timeout)` is being called on the main thread.
- `ConnectFailed` after about 20 seconds (timeout): server not running, wrong ticket/code, `token` mismatch, or missing `INTERNET` permission.
- The 4th client is refused: the server is in dev mode (no `license`), max 3 clients.
- `PeerovaException` on an unreliable send: payload larger than 1180 bytes — split it or send it reliably.

### FAQ

- **Do players need to type a code?** No. Embed the ticket in the app or fetch it from your URL; 6-character codes are only for inviting friends.
- **Can I run `peerova client` hidden on Android?** No — Android can't run the command-line tool as a child process; use the SDK.
- **Is the ticket a secret?** No; `server.key`, `license` and `token` are.
- **Can't connect?** Run `peerova doctor` on a computer on the same network. More: [Troubleshooting](../DEVELOPER-GUIDE.md#15-troubleshooting) · [FAQ](../DEVELOPER-GUIDE.md#16-faq-1).

### Test status

- JVM test (`./test.sh`): PASS (Kotlin 2.x, JDK 21, kotlinx-coroutines 1.9).
- Android: the Java/Kotlin binding ran an in-process round trip on an Android 16 x86_64 emulator (via `app_process`).
  A full Gradle/AGP build of the module has not been tested yet.
