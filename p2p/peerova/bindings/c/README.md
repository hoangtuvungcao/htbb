# Peerova — C / C++

- [Tiếng Việt](#tiếng-việt)
- [English](#english)

Website: https://peerova.pages.dev · Khái niệm và toàn bộ C API / Concepts and the full C API: [../DEVELOPER-GUIDE.md](../DEVELOPER-GUIDE.md)

---

## Tiếng Việt

Đây là API gốc của Peerova: một header `peerova.h` và một thư viện native. Mọi binding khác đều bọc API này.
Dùng được từ C99/C11 và C++ (header có sẵn `extern "C"`).

### Chọn cách tích hợp

C/C++ dùng được cho cả 4 tình huống trong [bảng chọn cách tích hợp](../DEVELOPER-GUIDE.md#1-chọn-cách-tích-hợp):
- **Tình huống 3 (game mới) và 4 (host trong game):** SDK `peerova.h` — phần lớn README này.
- **Tình huống 2 (game có sẵn, người chơi không chạy gì):** class C++ `PeerovaTunnel` ở mục [Game có sẵn: chạy peerova ẩn](#game-có-sẵn-chạy-peerova-ẩn) — game tự chạy `peerova` ẩn rồi code socket cũ kết nối `127.0.0.1`. Chỉ cho desktop (Windows/Linux/macOS); mobile/console không chạy được process con → dùng SDK.
- **Tình huống 1:** không cần code — xem [§3 của hướng dẫn](../DEVELOPER-GUIDE.md#3-game-có-sẵn-không-sửa-code).

### Yêu cầu

- Trình biên dịch C/C++ bất kỳ (gcc, clang, MSVC, MinGW).
- Gói SDK Peerova: `include/peerova.h` và thư viện cho nền tảng của bạn.

### Đặt thư viện native ở đâu

| Hệ điều hành | File | Đặt ở đâu |
|---|---|---|
| Windows x64 | `lib/windows-x86_64/peerova.dll` | Cạnh file `.exe` của bạn. SDK **không** cần `wintun.dll` (chỉ công cụ dòng lệnh `peerova` cần nó cho chế độ VPN). |
| Linux x64 | `lib/linux-x86_64/libpeerova.so` (hoặc `libpeerova.a` để link tĩnh) | Cạnh file chạy và link với rpath `$ORIGIN`, hoặc đặt trong một thư mục thuộc `LD_LIBRARY_PATH`. |
| macOS | `libpeerova.dylib` | Liên hệ Peerova để nhận bản build này. Trong app bundle: `Contents/Frameworks/`. |
| iOS | `libpeerova.a` (tĩnh) | Liên hệ Peerova để nhận bản build này. Link tĩnh vào target Xcode. |
| Android | `libpeerova.so` | Liên hệ Peerova để nhận bản build này. Đặt vào `jniLibs/<abi>/libpeerova.so`. |

### Cài đặt và biên dịch

Linux (thư viện động, tìm `.so` cạnh file chạy):
```sh
cc app.c -Iinclude -Llib/linux-x86_64 -lpeerova -Wl,-rpath,'$ORIGIN' -o app
cp lib/linux-x86_64/libpeerova.so .
```
Linux (link tĩnh, không cần file `.so` khi chạy):
```sh
cc app.c -Iinclude lib/linux-x86_64/libpeerova.a -lpthread -ldl -lm -o app
```
Windows (MinGW): `gcc app.c -Iinclude -Llib/windows-x86_64 -lpeerova -o app.exe`, rồi chép `peerova.dll` cạnh `app.exe`.
Với MSVC, link với import library đi kèm gói SDK (nếu gói của bạn không có, hãy liên hệ Peerova).

### Bắt đầu nhanh (5 phút)

Server:
```c
#include "peerova.h"
peerova_node* srv = peerova_server_start("{\"port\":47000,\"key_file\":\"server.key\"}");
if (!srv) { printf("lỗi: %s\n", peerova_last_error()); return 1; }

char ticket[2048];
peerova_ticket(srv, ticket, sizeof ticket);   /* đưa ticket này cho client (hoặc nhúng vào game) */
printf("TICKET %s\n", ticket);

char code[16];                                 /* mã kết nối 6 ký tự, chỉ có khi cấu hình "app" + "share" */
if (peerova_room_code(srv, code, sizeof code) > 0) printf("CODE %s\n", code);
```
Mã kết nối (connect code) được website cấp ngay sau khi khởi động, nên `peerova_room_code` có thể trả 0 lúc đầu — gọi lại khoảng mỗi giây cho tới khi > 0.
Hai hàm `peerova_ticket` / `peerova_room_code` trả về kích thước cần thiết (tính cả ký tự NUL); gọi với `NULL, 0` để biết kích thước trước.

Client (dùng ticket **hoặc** mã 6 ký tự):
```c
peerova_node* cli = peerova_client_connect(ticket_or_code,
    "{\"timeout_ms\":20000,\"key_file\":\"<appdata>/MyGame/peerova-client.key\"}");
if (!cli) { printf("lỗi %d: %s\n", peerova_last_error_code(), peerova_last_error()); return 1; }
peerova_event ev;
uint32_t server = 0;
while (!server) {
    if (!peerova_poll_wait(cli, &ev, 1000)) continue;
    if (ev.type == PEEROVA_EVENT_CONNECTED) server = ev.peer;
    else if (ev.type == PEEROVA_EVENT_CONNECT_FAILED) {
        printf("thất bại: %.*s\n", (int)ev.len, (const char*)ev.data);   /* "E_TIMEOUT: ..." */
        peerova_free(cli); return 1;
    }
}
peerova_send(cli, server, 0, (const uint8_t*)"xin chào", 9, 1);
```
Với ticket, `peerova_client_connect` trả về ngay; với mã 6 ký tự, hàm tra mã trên website trước (chặn trong lúc tra — gọi ngoài UI thread)
và trả `NULL` nếu mã không tồn tại (`PEEROVA_E_CODE_NOT_FOUND`).
Kết nối là tự động: ưu tiên kết nối trực tiếp; nếu mạng chặn, tự dùng **đường mã hoá tự động** (vẫn mã hoá đầu-cuối).

### Gửi: reliable hay unreliable

`peerova_send(node, peer, channel, data, len, reliable)` trả 0 nếu thành công, `-1` nếu lỗi (xem `peerova_last_error_code()` / `peerova_last_error()`).

| | `reliable = 0` (unreliable) | `reliable = 1` (reliable) |
|---|---|---|
| Đảm bảo | Có thể mất hoặc đến sai thứ tự | Đến đủ, đúng thứ tự trong cùng một channel |
| Kích thước | Tối đa **1180 byte** mỗi message (lớn hơn → `E_TOO_LARGE`) | Có thể lớn |
| Dùng cho | Input, vị trí, trạng thái realtime kiểu voice — dữ liệu gửi liên tục, gói mới thay gói cũ | Chat, inventory, RPC, các phần của file |

Channel là số 0..255. Các channel khác nhau không chặn nhau: một message reliable bị gửi lại trên channel 0 không làm chậm channel 1.

### Nhận sự kiện

Có hai hàm poll: `peerova_poll` (không chờ) và `peerova_poll_wait` (chờ tối đa `timeout_ms`). Cả hai trả 1 và điền `*out` nếu có sự kiện.

| `ev.type` | Ý nghĩa |
|---|---|
| `PEEROVA_EVENT_CONNECTED` | Peer đã kết nối (server: một client mới; client: server). `ev.peer` là id dùng để gửi. |
| `PEEROVA_EVENT_MESSAGE` | Có message: `ev.data`, `ev.len`, `ev.channel`, `ev.reliable`. |
| `PEEROVA_EVENT_DISCONNECTED` | Peer đã ngắt kết nối; `ev.data`/`ev.len` là lý do `"E_NAME: ..."`. |
| `PEEROVA_EVENT_CONNECT_FAILED` | Chỉ ở client: không kết nối được, `ev.data`/`ev.len` là lý do `"E_NAME: ..."` (UTF-8, tiếng Anh). |

`ev.data` chỉ hợp lệ tới lần gọi `peerova_poll*` / `peerova_free` kế tiếp trên cùng node — hãy copy nếu cần giữ lại.

Vòng lặp chuẩn: mỗi frame (hoặc mỗi vòng của một luồng mạng), lấy hết sự kiện rồi mới làm việc khác:
```c
for (;;) {
    while (peerova_poll(node, &ev)) handle(&ev);   /* game: gọi trong vòng lặp frame */
    update_and_send();
}
```
Server không có game loop có thể dùng `peerova_poll_wait(node, &ev, 10)` để khỏi tốn CPU.

### Luồng (thread)

Mỗi node có khoá nội bộ, nên `peerova_send`/`peerova_poll`/`peerova_disconnect` có thể gọi từ nhiều luồng.
`peerova_poll_wait` giữ khoá trong lúc chờ: nếu gửi từ luồng khác, dùng timeout ngắn (10–100 ms).
`peerova_last_error()` / `peerova_last_error_code()` là theo từng luồng. **Không bao giờ** gọi `peerova_free` khi một luồng khác vẫn đang dùng node đó.
`peerova_server_start` chặn tới ~3 s — trong game, gọi trên thread nền.

### Ngắt kết nối và dọn dẹp

- `peerova_disconnect(node, peer)` đóng một kết nối; sẽ có sự kiện `DISCONNECTED`.
- `peerova_free(node)` đóng tất cả kết nối và giải phóng node. Gọi đúng một lần, sau khi các luồng khác đã ngừng dùng node.

### Xử lý lỗi

Hàm trả `NULL` (start/connect) hoặc `-1` (send) khi lỗi. Ngay sau đó, trên cùng luồng:
- `peerova_last_error_code()` — mã ổn định `PEEROVA_E_*` (dùng mã này để xử lý theo loại lỗi);
- `peerova_last_error()` — văn bản tiếng Anh `"E_NAME: ..."` (để log / hiển thị).

Lỗi kết nối xảy ra sau đó (hết thời gian, sai token…) đến dưới dạng sự kiện `PEEROVA_EVENT_CONNECT_FAILED`, lý do bắt đầu bằng tên mã.
Danh sách mã đầy đủ: [Mã lỗi](../DEVELOPER-GUIDE.md#mã-lỗi). Hay gặp nhất:

| Mã | Ý nghĩa |
|---|---|
| `PEEROVA_E_INVALID_ARG` (1) | Tham số/JSON sai, mã kết nối sai định dạng. |
| `PEEROVA_E_TOO_LARGE` (2) | Gói unreliable quá 1180 byte. |
| `PEEROVA_E_UNKNOWN_PEER` (3) | `peer` không (còn) kết nối. |
| `PEEROVA_E_CODE_NOT_FOUND` (5) | Mã 6 ký tự không tồn tại / phòng đã đóng. |
| `PEEROVA_E_NETWORK` (6) | Lỗi mạng, cổng đang bận. |
| `PEEROVA_E_TIMEOUT` (7) | Hết thời gian kết nối — chạy `peerova doctor`. |
| `PEEROVA_E_LICENSE` (8) / `PEEROVA_E_CCU_LIMIT` (9) | License sai/hết hạn / đã đủ số client đồng thời. |
| `PEEROVA_E_ACCESS_DENIED` (10) | Token sai. |
| `PEEROVA_E_INVALID_TICKET` (11) | Ticket hỏng hoặc sai định dạng. |
| `PEEROVA_E_IO` (12) | Không đọc/ghi được `key_file` (thư mục phải tồn tại). |

### Cấu hình

Server (`peerova_server_start`, JSON, có thể `NULL`):

| Khoá | Mặc định | Ý nghĩa |
|---|---|---|
| `port` | `47000` | Cổng UDP; `0` = cổng ngẫu nhiên. |
| `key_file` | `"server.key"` | Khoá định danh server (tạo mới nếu chưa có). Cùng khoá ⇒ cùng ticket. **Bí mật.** |
| `license` | — | License `PVA-...`. Không có license: chế độ dev, tối đa 3 client. |
| `max_ccu` | theo license | Số client đồng thời tối đa. |
| `token` | — | Mật khẩu chung client phải gửi kèm. |
| `upnp` | `true` | Thử mở cổng trên router qua UPnP. |
| `private` | `false` | Ticket không chứa địa chỉ IP của server (khi kết nối trực tiếp người chơi vẫn biết IP — muốn giấu thật sự dùng `hide_ip`). |
| `hide_ip` | `false` | Ẩn IP máy chủ: người chơi không bao giờ biết IP thật của máy chủ (chống DDoS); ping cao hơn một chút. Gói Pro/Studio/Enterprise và bản dùng thử. |
| `app` | — | Project id trên website Peerova (`prj_...`), cần để có mã kết nối. |
| `share` | — | `{"name":"...","public":true,"max":16,"mode":"...","region":"VN"}` — thông tin hiển thị cho mã kết nối; `public` = hiện trong danh sách phòng công khai. |

Client (`peerova_client_connect`, JSON, có thể `NULL`):

| Khoá | Mặc định | Ý nghĩa |
|---|---|---|
| `token` | — | Phải khớp server (nếu server yêu cầu). |
| `timeout_ms` | `20000` | Thời gian tối đa trước `CONNECT_FAILED`. |
| `key_file` | — (khoá ngẫu nhiên mỗi lần chạy) | File danh tính người chơi, tạo mới nếu chưa có. Lưu trong thư mục dữ liệu ứng dụng của người dùng, ví dụ `"<appdata>/<game>/peerova-client.key"`; **thư mục phải tồn tại**. Giữ file ⇒ cùng danh tính và cùng địa chỉ trên máy chủ; cấm theo địa chỉ chỉ có tác dụng với từng người chơi khi họ còn giữ file này. |
| `cache_dir` | — (thư mục của `key_file`; không có cả hai ⇒ chỉ trong bộ nhớ) | Thư mục lưu `peerova-paths.cache`: đường trực tiếp và loại NAT đã học (hết hạn sau 24 h) ⇒ lần kết nối sau nhanh hơn. Thư viện không tự ghi file nếu không có khoá này hay `key_file`. |

### Tham chiếu API

| Hàm | Mô tả |
|---|---|
| `peerova_server_start(config)` | Khởi động server. Chặn tới khi có ticket (≤ ~3 s). `NULL` nếu lỗi. |
| `peerova_client_connect(ticket, config)` | Kết nối bằng ticket (trả về ngay) hoặc mã 6 ký tự (tra mã trước, chặn ngắn); chờ `CONNECTED`/`CONNECT_FAILED`. `NULL` nếu lỗi. |
| `peerova_ticket(node, buf, len)` | Copy ticket. Trả kích thước cần (tính NUL), 0 nếu không có. |
| `peerova_room_code(node, buf, len)` | Copy mã kết nối. Trả kích thước cần (tính NUL), 0 nếu chưa có. |
| `peerova_send(node, peer, channel, data, len, reliable)` | Gửi. 0 = thành công, -1 = lỗi. |
| `peerova_poll(node, &ev)` / `peerova_poll_wait(node, &ev, ms)` | Lấy một sự kiện. 1 = có sự kiện. |
| `peerova_disconnect(node, peer)` | Đóng một kết nối. |
| `peerova_peer_count(node)` | Số peer đang kết nối. |
| `peerova_free(node)` | Giải phóng node (`NULL` an toàn). |
| `peerova_last_error()` / `peerova_last_error_code()` | Lỗi gần nhất của luồng: văn bản `"E_NAME: ..."` / mã `PEEROVA_E_*`. |
| `peerova_version()` | Phiên bản thư viện. |

### Ví dụ

- [`examples/chat_positions.c`](examples/chat_positions.c) — chat (reliable) + đồng bộ vị trí 20 Hz (unreliable), chạy server và client ở hai tiến trình:
  ```sh
  cd examples
  cc chat_positions.c -I../../../include -L../../../lib/linux-x86_64 -lpeerova -Wl,-rpath,'$ORIGIN/../../../lib/linux-x86_64' -o chat_positions
  ./chat_positions server            # in ra "TICKET ..."
  ./chat_positions client <ticket>   # ở cửa sổ khác (hoặc máy khác)
  ```
- [`example.c`](example.c) — server và client trong cùng một tiến trình, gửi một message.
- [`test_peerova.c`](test_peerova.c) — kiểm thử round-trip (`./test_peerova`, `./test_peerova server [port]`, `./test_peerova client <ticket>`).

### Game có sẵn: chạy peerova ẩn

Tình huống 2 ([§4 của hướng dẫn](../DEVELOPER-GUIDE.md#4-game-tự-chạy-peerova-ẩn-người-chơi-không-chạy-gì)): phát hành `peerova.exe` / `peerova` **kèm game**.
Game chạy `peerova client <TICKET> --forward tcp:8888 --key <appdata>/<game>/peerova-client.key` **ẩn** (Windows: không cửa sổ console),
đọc stdout+stderr từng dòng (UTF-8), coi là **sẵn sàng** khi một dòng chứa chuỗi ASCII `server port`
(CLI in `✔ ĐÃ KẾT NỐI …` rồi `✔ Tcp 127.0.0.1:8888 → server port 8888`, mỗi `--forward` một dòng). Thất bại nếu process thoát trước
(mã thoát 1, dòng stderr bắt đầu bằng `lỗi:`) hoặc quá 30 s (khi đó kill). Sau đó code socket **có sẵn** kết nối `127.0.0.1:8888`; kill `peerova` khi game thoát.

Header C++17 dưới đây làm tất cả việc đó (Windows + Linux/macOS, không phụ thuộc gì). Trên Windows, class đưa `peerova` vào một *job object*:
nếu game thoát hoặc crash, Windows tự dừng `peerova`.

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

Dùng:

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

Lưu ý:
- Thư mục chứa file `--key` phải tồn tại (`std::filesystem::create_directories`). Linux/macOS: `peerova` phải có quyền chạy (`chmod +x`); macOS: codesign cùng app.
- Chọn cổng local ít đụng (ví dụ `tcp:38888=8888`) nếu 8888 có thể bị chương trình khác dùng.
- `peerova client` thoát khi mất kết nối → `tunnel.running()` thành `false`: báo "mất kết nối" và cho chơi lại.
- Mobile / console không chạy được process con → dùng SDK `peerova.h` (tình huống 3/4).
- `peerova stdio` **không** mở cổng local: đó là SDK message dạng JSON lines ([`../stdio/README.md`](../stdio/README.md)), không dùng cho cách này.

### Người chơi không phải gõ gì

Người chơi không bao giờ phải gõ ticket hay mã: nhúng ticket vào game, hoặc tải ticket mới nhất từ URL của bạn khi game khởi động
(dùng ticket nhúng nếu không tải được). Mã 6 ký tự chỉ để mời bạn bè vào phòng do người chơi mở. Chi tiết và failover nhiều máy chủ
(`peerova ticket merge`): [§2 của hướng dẫn](../DEVELOPER-GUIDE.md#2-người-chơi-không-phải-gõ-gì).

**Máy chủ game:** nhiều cổng trong một process, tách Remote Desktop cho admin, mỗi người chơi một địa chỉ `127.a.b.c` + cấm người chơi,
ẩn IP máy chủ, chế độ VPN — xem [§7 của hướng dẫn](../DEVELOPER-GUIDE.md#7-tính-năng-máy-chủ-game).

### Bảo mật

- `server.key` là danh tính của server: ai có file này có thể giả mạo server của bạn. Giữ bí mật, không commit vào git.
- Ticket là thông tin "bán công khai": cứ đưa cho người chơi/client hoặc nhúng vào game.
- `token` và `license` là bí mật — không nhúng license vào bản game phát hành cho người chơi.
- Toàn bộ dữ liệu được mã hoá đầu-cuối.

### Khắc phục sự cố

- Chạy `peerova doctor` (công cụ dòng lệnh `peerova` trong gói SDK) để chẩn đoán mạng.
- **Không tìm thấy thư viện** (`error while loading shared libraries: libpeerova.so` / thiếu `peerova.dll`): đặt thư viện cạnh file chạy (Linux: link với rpath `$ORIGIN`) hoặc thêm thư mục vào `LD_LIBRARY_PATH` / `PATH`.
- **`E_TIMEOUT`**: server chưa chạy, ticket/mã sai hoặc đã hết hạn, hay mạng chặn; thử `peerova doctor` ở cả hai phía, tăng `timeout_ms`.
- **Client thứ 4 bị từ chối (`E_CCU_LIMIT`)**: server đang ở chế độ dev (không có license) chỉ nhận tối đa 3 client.
- **`peerova_send` unreliable trả lỗi (`E_TOO_LARGE`)**: message lớn hơn 1180 byte — chia nhỏ hoặc gửi reliable.
- **`E_IO`**: thư mục chứa `key_file` không tồn tại hoặc không ghi được.
- **Server không khởi động khi bật `hide_ip`**: gói hiện tại không có ẩn IP (cần Pro/Studio/Enterprise hoặc bản dùng thử).
- **`PeerovaTunnel::start` trả `false`**: xem các dòng log (`onLog`) — ticket sai, máy chủ không tới được, hoặc cổng local đang bận (đổi `tcp:38888=8888`).

### FAQ

- **Người chơi có phải gõ mã không?** Không. Nhúng ticket hoặc tải từ URL của bạn; mã 6 ký tự chỉ để mời bạn bè.
- **Có cần mở port / IP tĩnh không?** Không; mở port chỉ giúp kết nối trực tiếp nhanh hơn.
- **Game không có source code dùng được không?** Được — [§3](../DEVELOPER-GUIDE.md#3-game-có-sẵn-không-sửa-code) hoặc launcher chạy `peerova` ẩn (mục trên).
- Thêm: [FAQ của hướng dẫn](../DEVELOPER-GUIDE.md#16-faq), [Khắc phục sự cố](../DEVELOPER-GUIDE.md#15-khắc-phục-sự-cố).

---

## English

This is Peerova's native API: one header, `peerova.h`, and one native library. Every other binding wraps it.
It works from C99/C11 and C++ (the header has `extern "C"` guards).

### Choosing an integration

C/C++ covers all 4 scenarios of the [integration table](../DEVELOPER-GUIDE.md#1-choosing-an-integration):
- **Scenario 3 (new game) and 4 (host-in-game):** the `peerova.h` SDK — most of this README.
- **Scenario 2 (existing game, players run nothing):** the C++ `PeerovaTunnel` class in [Existing game: run peerova hidden](#existing-game-run-peerova-hidden) — the game starts `peerova` hidden, then the existing socket code connects to `127.0.0.1`. Desktop only (Windows/Linux/macOS); mobile/consoles can't spawn processes → use the SDK.
- **Scenario 1:** no code at all — see [guide §3](../DEVELOPER-GUIDE.md#3-existing-game-no-code-change).

### Requirements

- Any C/C++ compiler (gcc, clang, MSVC, MinGW).
- The Peerova SDK package: `include/peerova.h` and the library for your platform.

### Where to put the native library

| OS | File | Where it goes |
|---|---|---|
| Windows x64 | `lib/windows-x86_64/peerova.dll` | Next to your `.exe`. The SDK does **not** need `wintun.dll` (only the `peerova` command-line tool needs it, for its VPN mode). |
| Linux x64 | `lib/linux-x86_64/libpeerova.so` (or `libpeerova.a` for static linking) | Next to your binary, linked with rpath `$ORIGIN`, or in a directory on `LD_LIBRARY_PATH`. |
| macOS | `libpeerova.dylib` | Contact Peerova for this build. In an app bundle: `Contents/Frameworks/`. |
| iOS | `libpeerova.a` (static) | Contact Peerova for this build. Link it statically into your Xcode target. |
| Android | `libpeerova.so` | Contact Peerova for this build. Put it in `jniLibs/<abi>/libpeerova.so`. |

### Install and build

Linux (shared library, `.so` found next to the binary):
```sh
cc app.c -Iinclude -Llib/linux-x86_64 -lpeerova -Wl,-rpath,'$ORIGIN' -o app
cp lib/linux-x86_64/libpeerova.so .
```
Linux (static, no `.so` needed at run time):
```sh
cc app.c -Iinclude lib/linux-x86_64/libpeerova.a -lpthread -ldl -lm -o app
```
Windows (MinGW): `gcc app.c -Iinclude -Llib/windows-x86_64 -lpeerova -o app.exe`, then copy `peerova.dll` next to `app.exe`.
With MSVC, link the import library shipped with the SDK package (if your package does not contain one, contact Peerova).

### 5-minute quick start

Server:
```c
#include "peerova.h"
peerova_node* srv = peerova_server_start("{\"port\":47000,\"key_file\":\"server.key\"}");
if (!srv) { printf("error: %s\n", peerova_last_error()); return 1; }

char ticket[2048];
peerova_ticket(srv, ticket, sizeof ticket);   /* give this ticket to clients (or embed it in the game) */
printf("TICKET %s\n", ticket);

char code[16];                                 /* 6-character connect code, only with "app" + "share" config */
if (peerova_room_code(srv, code, sizeof code) > 0) printf("CODE %s\n", code);
```
The connect code is assigned by the website shortly after start, so `peerova_room_code` may return 0 at first — call it again about once a second until it returns > 0.
Both `peerova_ticket` and `peerova_room_code` return the required size including the NUL; call them with `NULL, 0` to get the size first.

Client (with a ticket **or** a 6-character code):
```c
peerova_node* cli = peerova_client_connect(ticket_or_code,
    "{\"timeout_ms\":20000,\"key_file\":\"<appdata>/MyGame/peerova-client.key\"}");
if (!cli) { printf("error %d: %s\n", peerova_last_error_code(), peerova_last_error()); return 1; }
peerova_event ev;
uint32_t server = 0;
while (!server) {
    if (!peerova_poll_wait(cli, &ev, 1000)) continue;
    if (ev.type == PEEROVA_EVENT_CONNECTED) server = ev.peer;
    else if (ev.type == PEEROVA_EVENT_CONNECT_FAILED) {
        printf("failed: %.*s\n", (int)ev.len, (const char*)ev.data);   /* "E_TIMEOUT: ..." */
        peerova_free(cli); return 1;
    }
}
peerova_send(cli, server, 0, (const uint8_t*)"hello", 5, 1);
```
With a ticket, `peerova_client_connect` returns immediately; with a 6-character code it first looks the code up on the website (blocks
during the lookup — call it off the UI thread) and returns `NULL` if the code does not exist (`PEEROVA_E_CODE_NOT_FOUND`).
Connections are automatic: direct first; when the network blocks that, the **automatic encrypted route** is used (still end-to-end encrypted).

### Sending: reliable vs unreliable

`peerova_send(node, peer, channel, data, len, reliable)` returns 0 on success, `-1` on error (see `peerova_last_error_code()` / `peerova_last_error()`).

| | `reliable = 0` (unreliable) | `reliable = 1` (reliable) |
|---|---|---|
| Guarantee | May be lost or reordered | Delivered, in order within the channel |
| Size | At most **1180 bytes** per message (larger → `E_TOO_LARGE`) | Can be large |
| Use for | Gameplay inputs, positions, voice-like realtime state — data sent continuously where a newer packet replaces an older one | Chat, inventory, RPC, file chunks |

Channels are numbers 0..255. Different channels don't block each other: a reliable message being retransmitted on channel 0 does not delay channel 1.

### Receiving events

There are two poll functions: `peerova_poll` (never waits) and `peerova_poll_wait` (waits up to `timeout_ms`). Both return 1 and fill `*out` when an event is available.

| `ev.type` | Meaning |
|---|---|
| `PEEROVA_EVENT_CONNECTED` | A peer connected (server: a new client; client: the server). `ev.peer` is the id to send to. |
| `PEEROVA_EVENT_MESSAGE` | A message: `ev.data`, `ev.len`, `ev.channel`, `ev.reliable`. |
| `PEEROVA_EVENT_DISCONNECTED` | A peer disconnected; `ev.data`/`ev.len` is the reason `"E_NAME: ..."`. |
| `PEEROVA_EVENT_CONNECT_FAILED` | Client only: could not connect; `ev.data`/`ev.len` is the reason `"E_NAME: ..."` (UTF-8, English). |

`ev.data` is only valid until the next `peerova_poll*` / `peerova_free` call on the same node — copy it if you need to keep it.

Standard loop: every frame (or every iteration of a network thread), drain all events, then do your work:
```c
for (;;) {
    while (peerova_poll(node, &ev)) handle(&ev);   /* games: call from the frame loop */
    update_and_send();
}
```
A server without a game loop can use `peerova_poll_wait(node, &ev, 10)` to avoid spinning.

### Threading

Each node has an internal lock, so `peerova_send` / `peerova_poll` / `peerova_disconnect` may be called from several threads.
`peerova_poll_wait` holds the lock while waiting: when sending from another thread, use a short timeout (10–100 ms).
`peerova_last_error()` / `peerova_last_error_code()` are per thread. **Never** call `peerova_free` while another thread is still using that node.
`peerova_server_start` blocks for up to ~3 s — in a game, call it on a background thread.

### Disconnect and cleanup

- `peerova_disconnect(node, peer)` closes one connection; a `DISCONNECTED` event follows.
- `peerova_free(node)` closes all connections and frees the node. Call it exactly once, after other threads have stopped using the node.

### Error handling

Functions return `NULL` (start/connect) or `-1` (send) on error. Right after that, on the same thread:
- `peerova_last_error_code()` — the stable `PEEROVA_E_*` code (branch on this);
- `peerova_last_error()` — English text `"E_NAME: ..."` (for logs / display).

Connection errors that happen later (timeout, wrong token, …) arrive as `PEEROVA_EVENT_CONNECT_FAILED` events whose reason starts with the code name.
Full list: [Error codes](../DEVELOPER-GUIDE.md#error-codes). The common ones:

| Code | Meaning |
|---|---|
| `PEEROVA_E_INVALID_ARG` (1) | Bad argument/JSON, malformed connect code. |
| `PEEROVA_E_TOO_LARGE` (2) | Unreliable payload over 1180 bytes. |
| `PEEROVA_E_UNKNOWN_PEER` (3) | `peer` is not (or no longer) connected. |
| `PEEROVA_E_CODE_NOT_FOUND` (5) | 6-character code unknown / room closed. |
| `PEEROVA_E_NETWORK` (6) | Network error, port in use. |
| `PEEROVA_E_TIMEOUT` (7) | Connect timed out — run `peerova doctor`. |
| `PEEROVA_E_LICENSE` (8) / `PEEROVA_E_CCU_LIMIT` (9) | License invalid/expired / concurrent-client limit reached. |
| `PEEROVA_E_ACCESS_DENIED` (10) | Wrong token. |
| `PEEROVA_E_INVALID_TICKET` (11) | Ticket malformed or corrupted. |
| `PEEROVA_E_IO` (12) | `key_file` unreadable/unwritable (its folder must exist). |

### Configuration

Server (`peerova_server_start`, JSON, may be `NULL`):

| Key | Default | Meaning |
|---|---|---|
| `port` | `47000` | UDP port; `0` = random port. |
| `key_file` | `"server.key"` | Server identity key (created if missing). Same key ⇒ same ticket. **Secret.** |
| `license` | — | `PVA-...` license key. Without a license: dev mode, max 3 clients. |
| `max_ccu` | from license | Maximum concurrent clients. |
| `token` | — | Shared secret clients must present. |
| `upnp` | `true` | Try to open the port on the router via UPnP. |
| `private` | `false` | The ticket does not contain the server's IP addresses (on a direct connection players still see the IP — use `hide_ip` to really hide it). |
| `hide_ip` | `false` | Hide the server IP: players never learn its real address (DDoS protection); slightly higher latency. Pro/Studio/Enterprise plans and the trial. |
| `app` | — | Project id from the Peerova website (`prj_...`), needed for a connect code. |
| `share` | — | `{"name":"...","public":true,"max":16,"mode":"...","region":"VN"}` — listing details for the connect code; `public` = shown in the public room list. |

Client (`peerova_client_connect`, JSON, may be `NULL`):

| Key | Default | Meaning |
|---|---|---|
| `token` | — | Must match the server (when it requires one). |
| `timeout_ms` | `20000` | Maximum time before `CONNECT_FAILED`. |
| `key_file` | — (random key per run) | The player's identity file, created if missing. Store it in the user's app-data folder, e.g. `"<appdata>/<game>/peerova-client.key"`; **the folder must exist**. Keeping the file keeps the same identity and the same address on the server; banning by address works per player only while they keep that file. |
| `cache_dir` | — (the `key_file` folder; neither given ⇒ memory only) | Folder for `peerova-paths.cache`: learned direct paths and NAT classes (expire after 24 h) ⇒ faster reconnects. The library writes no file unless this or `key_file` is set. |

### API reference

| Function | Description |
|---|---|
| `peerova_server_start(config)` | Starts a server. Blocks until the ticket is ready (≤ ~3 s). `NULL` on error. |
| `peerova_client_connect(ticket, config)` | Connects with a ticket (returns immediately) or a 6-character code (looked up first, short block); wait for `CONNECTED`/`CONNECT_FAILED`. `NULL` on error. |
| `peerova_ticket(node, buf, len)` | Copies the ticket. Returns the required size incl. NUL, 0 if none. |
| `peerova_room_code(node, buf, len)` | Copies the connect code. Returns the required size incl. NUL, 0 if not available yet. |
| `peerova_send(node, peer, channel, data, len, reliable)` | Sends. 0 = success, -1 = error. |
| `peerova_poll(node, &ev)` / `peerova_poll_wait(node, &ev, ms)` | Gets one event. 1 = an event was returned. |
| `peerova_disconnect(node, peer)` | Closes one connection. |
| `peerova_peer_count(node)` | Number of connected peers. |
| `peerova_free(node)` | Frees the node (`NULL` is safe). |
| `peerova_last_error()` / `peerova_last_error_code()` | Last error of this thread: text `"E_NAME: ..."` / code `PEEROVA_E_*`. |
| `peerova_version()` | Library version. |

### Examples

- [`examples/chat_positions.c`](examples/chat_positions.c) — chat (reliable) + position sync at 20 Hz (unreliable), server and client in two processes:
  ```sh
  cd examples
  cc chat_positions.c -I../../../include -L../../../lib/linux-x86_64 -lpeerova -Wl,-rpath,'$ORIGIN/../../../lib/linux-x86_64' -o chat_positions
  ./chat_positions server            # prints "TICKET ..."
  ./chat_positions client <ticket>   # in another terminal (or on another machine)
  ```
- [`example.c`](example.c) — server and client in one process, sends one message.
- [`test_peerova.c`](test_peerova.c) — round-trip test (`./test_peerova`, `./test_peerova server [port]`, `./test_peerova client <ticket>`).

### Existing game: run peerova hidden

Scenario 2 ([guide §4](../DEVELOPER-GUIDE.md#4-the-game-runs-peerova-hidden-players-run-nothing)): ship `peerova.exe` / `peerova` **with the game**.
The game starts `peerova client <TICKET> --forward tcp:8888 --key <appdata>/<game>/peerova-client.key` **hidden** (Windows: no console window),
reads stdout+stderr line by line (UTF-8) and is **ready** when a line contains the ASCII text `server port`
(the CLI prints `✔ ĐÃ KẾT NỐI …` = "connected", then `✔ Tcp 127.0.0.1:8888 → server port 8888`, once per `--forward`). It has failed if the process
exits first (exit code 1, a stderr line starting with `lỗi:` = "error:") or after 30 s (then kill it). After that the **existing** socket code connects
to `127.0.0.1:8888`; kill `peerova` when the game exits.

The C++17 header below does all of that (Windows + Linux/macOS, no dependencies). On Windows the class puts `peerova` in a *job object*:
if the game exits or crashes, Windows stops `peerova` automatically.

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

Usage:

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

Notes:
- The folder of the `--key` file must exist (`std::filesystem::create_directories`). Linux/macOS: `peerova` must be executable (`chmod +x`); macOS: codesign it with the app.
- Pick an unusual local port (e.g. `tcp:38888=8888`) if 8888 may be used by another program.
- `peerova client` exits when the connection is lost → `tunnel.running()` becomes `false`: show "disconnected" and offer to retry.
- Mobile / consoles can't spawn child processes → use the `peerova.h` SDK (scenarios 3/4).
- `peerova stdio` does **not** open local ports: it is the message SDK as JSON lines ([`../stdio/README.md`](../stdio/README.md)), not for this pattern.

### Players never type anything

Players never type a ticket or a code: embed the ticket in the game, or fetch the latest ticket from your own URL at startup (falling back
to the embedded one). 6-character codes are only for inviting friends to player-hosted rooms. Details and multi-server failover
(`peerova ticket merge`): [guide §2](../DEVELOPER-GUIDE.md#2-players-never-type-anything).

**Game servers:** several ports in one process, a separate Remote Desktop for admins, one `127.a.b.c` address per player + banning,
hiding the server IP, VPN mode — see [guide §7](../DEVELOPER-GUIDE.md#7-game-server-features).

### Security

- `server.key` is the server's identity: anyone who has it can impersonate your server. Keep it secret and never commit it.
- The ticket is public-ish: share it with players/clients or embed it in the game.
- `token` and `license` are secrets — never embed the license in the game build you ship to players.
- All traffic is end-to-end encrypted.

### Troubleshooting

- Run `peerova doctor` (the `peerova` command-line tool in the SDK package) for a network diagnosis.
- **Library not found** (`error while loading shared libraries: libpeerova.so` / missing `peerova.dll`): put the library next to the binary (Linux: link with rpath `$ORIGIN`) or add its directory to `LD_LIBRARY_PATH` / `PATH`.
- **`E_TIMEOUT`**: the server is not running, the ticket/code is wrong or expired, or the network blocks traffic; run `peerova doctor` on both sides and/or raise `timeout_ms`.
- **The 4th client is rejected (`E_CCU_LIMIT`)**: the server runs in dev mode (no license), which accepts at most 3 clients.
- **Unreliable `peerova_send` fails (`E_TOO_LARGE`)**: the message is larger than 1180 bytes — split it or send it reliably.
- **`E_IO`**: the folder of `key_file` does not exist or is not writable.
- **The server does not start with `hide_ip`**: your plan does not include hiding the IP (Pro/Studio/Enterprise or the trial).
- **`PeerovaTunnel::start` returns `false`**: look at the log lines (`onLog`) — wrong ticket, server unreachable, or the local port is in use (try `tcp:38888=8888`).

### FAQ

- **Do players need to type a code?** No. Embed the ticket or fetch it from your URL; 6-character codes are only for inviting friends.
- **Do I need port forwarding / a static IP?** No; an open port only makes direct connections faster.
- **Does it work with a game I have no source for?** Yes — [§3](../DEVELOPER-GUIDE.md#3-existing-game-no-code-change) or a launcher that runs `peerova` hidden (section above).
- More: [guide FAQ](../DEVELOPER-GUIDE.md#16-faq-1), [Troubleshooting](../DEVELOPER-GUIDE.md#15-troubleshooting).
