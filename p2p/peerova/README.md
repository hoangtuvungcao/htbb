# Peerova — kết nối trực tiếp mọi dịch vụ sau NAT (không relay, mã hoá E2E)

Client kết nối **thẳng** tới máy chủ dịch vụ (web/API, SSH, RDP, database, game…), kể cả khi cả hai bên đều nằm sau NAT và **không ai mở port**. Không có relay hay máy chủ trung gian, dịch vụ mạng không phụ thuộc bên thứ 3. Tunnel được **mọi giao thức** (TCP: HTTP/HTTPS/WebSocket/SSH…; UDP: game, QUIC, voice). Có SDK cho mọi ngôn ngữ qua C ABI.

```bash
cargo build --release            # target/release/peerova (+ libpeerova.so / peerova.dll cho SDK)
cargo test --release --workspace # 44 test: crypto, transport mô phỏng mất gói, engine, tunnel, SDK
```

## Bắt đầu nhanh
```bash
peerova doctor                                              # kiểm tra mạng
# Máy chủ (không mở port): chia sẻ web HTTPS + SSH + một dịch vụ UDP
peerova server --rdv --expose https:443 --expose ssh:22 --expose udp:19132   # in TICKET
# Máy người dùng (không mở port)
peerova client <TICKET> --forward tcp:8443=443 --forward tcp:2222=22 --forward udp:19132
#   → mở https://localhost:8443 · ssh -p 2222 localhost · app UDP trỏ 127.0.0.1:19132
peerova ping   <TICKET>                                     # đo RTT/jitter/throughput thật
```
Tuỳ chọn bảo mật server: `--private` (ticket không chứa IP), `--token SECRET` (chỉ client có token mới vào được), `--license PVA-...` (nâng giới hạn CCU theo gói).

## Cách kết nối hoạt động
Client gửi bắt tay **song song tới mọi địa chỉ** của server, đường nào trả lời trước thì dùng đường đó (thường cũng là đường RTT thấp nhất):

| Đường | Khi nào có | Ghi chú |
|---|---|---|
| `internet` (IP public trực tiếp) | Server là VPS / modem bridge có IP public | Phổ biến nhất cho máy chủ dịch vụ (web, API, game…). Chỉ cần cho UDP port qua firewall; server *dark* nên mở port vẫn an toàn |
| `ipv6` | Server có IPv6 global | Không có NAT, chỉ cần punch qua firewall. Viettel/VNPT/FPT/4G phần lớn đã có |
| `mapped` | Router server hỗ trợ UPnP/NAT-PMP và IP ngoài là public | Tự mở port, tự gia hạn, tự xoá khi tắt |
| `reflexive` / `punched` | Server khai báo `--supernode` | Supernode = một server peerova reachable khác, chỉ giới thiệu địa chỉ, **không chuyển dữ liệu** |
| join code | Không có gì cả | Client in mã `pvj_`, dán vào console server |
| `lan` | Cùng mạng LAN | Tự động |

## Kết quả lab NAT thật (`scripts/nat-lab.sh`, không cần root)
| Máy chủ (không mở port) ↔ Người chơi (không mở port) | Kết quả | Thời gian |
|---|---|---|
| Router gia đình ↔ Wi-Fi gia đình (cone ↔ cone) | ✅ | ≈0,3 s |
| Router gia đình ↔ 4G/NAT khó (cone ↔ symmetric) | ✅ birthday | ≈1,7 s |
| NAT khó ↔ router gia đình | ✅ | ≈2–4 s |
| **NAT nhà mạng ↔ NAT nhà mạng (CGNAT ↔ CGNAT, khối port RFC 7422)** | ✅ **birthday hai phía trong cửa sổ port**: trường hợp các dự án khác bỏ cuộc | ≈3–8 s |
| Symmetric ngẫu nhiên toàn dải 64k port ở **cả hai** bên | ❌ cần khoảng 170.000 gói mỗi bên (xác suất 1/R²), không khả thi nếu không relay | — |

## Không có VPS / supernode? Dùng chính website làm điểm hẹn
```bash
peerova server --rdv --private --expose tcp:25565     # máy bạn, KHÔNG mở port
peerova client <TICKET> --forward tcp:25565           # máy người dùng, KHÔNG mở port
```
Website (Cloudflare Pages của bạn) chỉ trao địa chỉ lúc bắt đầu. **Dữ liệu luôn đi thẳng P2P, không qua website.**
Lab NAT thật (`LAB_MODE=rdv scripts/nat-lab.sh`): cone↔cone 4/4 (≈1,8 s), cone↔symmetric 4/4, symmetric↔cone 3/4, symmetric↔symmetric 0/4 (giới hạn chung của cả ngành, xem [docs/PRIOR-ART-NAT.md](docs/PRIOR-ART-NAT.md)).

## Các trường hợp hay gặp

| # | Mạng server | Mạng client | Kết quả | Cách xử lý nếu lỗi |
|---|---|---|---|---|
| 1 | VPS / IP public | Bất kỳ (Wi-Fi, 4G, CGNAT) | ✅ Luôn được | Mở 1 port UDP trên firewall (`ufw allow 47000/udp`) |
| 2 | Cáp quang nhà, IP public, UPnP bật | Bất kỳ | ✅ `mapped` | Bật UPnP trên modem nếu `doctor` báo tắt |
| 3 | Cáp quang nhà, **double NAT** (modem + router riêng) | Bất kỳ | ✅ tự động qua supernode punch (+ dự đoán port) | Nếu NAT quá khó: bật bridge mode trên modem |
| 4 | Nhà bị **CGNAT** (IP ngoài 100.64.x.x), có IPv6 | Có IPv6 (đa số 4G Viettel) | ✅ `ipv6` | — |
| 5 | Nhà bị CGNAT, có IPv6 | **Không** có IPv6 | ✅ tự động punch qua supernode (server licensed tự lấy danh sách supernode) | Nếu CGNAT kiểu symmetric: xin IP public / dùng VPS |
| 6 | Người chơi tự host (NAT nhà) | Người chơi khác (NAT nhà) | ✅ phần lớn trường hợp: punch + **dự đoán port** cho NAT cấp port tuần tự | Symmetric ngẫu nhiên ↔ symmetric ngẫu nhiên: không thể nếu không có relay |
| 7 | Server reachable | Wi-Fi công ty/trường **chặn UDP** | ✅ tự động chuyển **TCP trực tiếp** sau 1.5 s (`--tcp` để ép) | Mạng chỉ cho cổng 443: chạy server `--port 443` |
| 8 | Cùng LAN | Cùng LAN | ✅ `lan` | — |

Chạy `peerova doctor` ở cả hai máy, công cụ này nhận diện được các trường hợp 2 → 5. Bảng so sánh chi tiết giữa mở port, relay và peerova: [docs/COMPARISON.md](docs/COMPARISON.md).

## Chống crack mà không bóp người dùng
- **Đường dữ liệu không bao giờ bị giới hạn tốc độ.** License chỉ ảnh hưởng số CCU và quyền dùng mạng supernode.
- Lease do website ký Ed25519, gắn với **machine id + public key của server**, kiểm tra offline, gia hạn ngầm 6 giờ/lần. Web sập 7 ngày vẫn chạy.
- **Thực thi qua mạng:** supernode có license chỉ phục vụ server mang lease hợp lệ đúng key. Bản crack không tự ký được lease nên bị cả mạng từ chối (có test: không license, và lease ăn cắp của server khác).
- Private key của authority chỉ nằm trên website, binary chỉ chứa public key. Chi tiết và lộ trình: [docs/RESEARCH-IP-SECURITY.md](docs/RESEARCH-IP-SECURITY.md).

## Hiệu năng và độ mượt (đã đo)
| Phép đo | Kết quả |
|---|---|
| Ping khi đang ép kín đường truyền (1 MB/s, base 40 ms) | 52 ms (+12 ms), vẫn dùng 95% băng thông, nhờ kiểm soát tắc nghẽn theo delay |
| Mất 10% / 30% gói | Dữ liệu vẫn đúng 100% |
| Throughput / ping (loopback) | UDP 982 Mbit/s · 0.34 ms — TCP fallback 751 Mbit/s · 0.5 ms |
| Internet thật | `scripts/vps-test.sh user@host`: so sánh ICMP ping với peerova ping |

Cơ chế giữ ping thấp: datagram realtime (game, voice, video) ưu tiên trước kênh SDK, kênh SDK ưu tiên trước bulk tunnel. Stream độc lập nên không có head-of-line blocking. Tunnel bật TCP_NODELAY. ACK delay 10 ms. Datagram không retransmit, gói cũ bị bỏ thay vì gây trễ.

## Hướng tối ưu tiếp theo (theo mức ưu tiên)
1. ✅ ~~Fallback TCP trực tiếp~~ (đã xong) · ✅ ~~Dự đoán port tuần tự~~ (đã xong) · ✅ ~~Supernode tự động có license~~ (đã xong)
2. Birthday punching (mở nhiều socket) cho symmetric NAT cấp port ngẫu nhiên.
3. **Đa đường liên tục**: giữ dò song song IPv4/IPv6 suốt phiên và tự chuyển sang đường RTT thấp hơn.
4. **Path MTU discovery**: gói lớn hơn 1280 khi đường cho phép, giảm overhead.
5. **Engine đa luồng** (shard theo connection) + `recvmmsg`/`sendmmsg` trên Linux, phục vụ hàng nghìn CCU trên một server.
6. **FEC tuỳ chọn** cho datagram realtime: bù mất gói mà không phải chờ retransmit.
7. Đăng ký supernode có chữ ký, chọn supernode theo vùng.

## Cấu trúc
| Thư mục | Nội dung |
|---|---|
| [crates/peerova-core](crates/peerova-core/src) | crypto (Noise IK), conn (transport), engine, tunnel, nat (UPnP/NAT-PMP), license, ticket |
| [crates/peerova-cli](crates/peerova-cli/src/main.rs) | lệnh `peerova` |
| [crates/peerova-ffi](crates/peerova-ffi) | C ABI + [peerova.h](crates/peerova-ffi/include/peerova.h) |
| [bindings/](bindings) | C, Python (+ C#/Unity/Godot/Java/Go) |
| [web/](web) | website, dashboard, admin, license authority (Cloudflare Pages, chuyển sang VPS được) |
| [docs/](docs) | [CONTRACT](docs/CONTRACT.md) web ↔ core, [COMPARISON](docs/COMPARISON.md) |
| [scripts/vps-test.sh](scripts/vps-test.sh) | deploy và đo trên VPS thật |

Build cho nền tảng khác: `--target x86_64-unknown-linux-musl` (Linux static, chạy trên mọi distro) · `--target x86_64-pc-windows-gnu` (Windows).
