# Mở port / NAT truyền thống vs Relay vs peerova

## 1. Bảng so sánh tổng quan

| Tiêu chí | Mở port (port forward) truyền thống | Relay (Photon, Unity Relay, Steam Relay, TURN) | **peerova** (P2P trực tiếp, không relay) |
|---|---|---|---|
| Cần cấu hình router | ✅ Có, phải forward tay | ❌ Không | ❌ Không: tự dùng UPnP/NAT-PMP, IPv6, hoặc hole punch |
| Port lộ ra Internet | ⚠️ Mở công khai, ai cũng quét thấy | Không (server nằm sau relay) | **Dark**: không trả lời gói nào thiếu ticket, quét port không thấy gì |
| Chạy được sau CGNAT (4G, nhiều gói cáp quang) | ❌ Không | ✅ Có | ⚠️ Được nếu có IPv6 hoặc phía server reachable; ✘ nếu **cả hai** bên đều symmetric NAT/CGNAT và không có IPv6 |
| Tỷ lệ kết nối | ~100% *nếu* mở port được | ~100% | Ước tính 85–95%. Với mô hình dedicated server dev tự kiểm soát mạng thì gần 100% (xem mục 3) |
| Độ trễ | Thấp nhất (đường thẳng) | **+10–80 ms** (đi vòng qua máy relay, phụ thuộc vị trí relay) | Thấp nhất (đường thẳng, như mở port) |
| Ping khi đang tải nặng | TCP/UDP thường làm phình hàng đợi router | Tuỳ relay | Kiểm soát tắc nghẽn theo delay: **+12 ms** khi ép kín link (đo trong mô phỏng) |
| Chi phí vận hành | 0 | Tốn tiền theo CCU / băng thông | **≈ 0** (không có máy chủ trung gian) |
| Băng thông tối đa | Băng thông thật | Bị giới hạn bởi relay | Băng thông thật (**982 Mbit/s** đo trên loopback) |
| Mã hoá | Tuỳ ứng dụng (thường không) | Thường chỉ tới relay | **E2E Noise IK** (X25519/ChaCha20-Poly1305), relay không tồn tại nên không ai đọc giữa đường |
| Chống MITM / giả server | ❌ | Phụ thuộc relay | ✅ Ticket chứa public key server |
| Ẩn IP server | ❌ Lộ hoàn toàn | ✅ Ẩn sau relay | ⚠️ Một phần: **private ticket** không chứa IP; IP chỉ lộ cho client đã có ticket và đã xác thực |
| Chống DDoS volumetric | ❌ | ✅ Relay hấp thụ | ⚠️ Không hấp thụ được nếu kẻ tấn công **đã được cấp quyền** và biết IP; người lạ thì không thấy server |
| Đổi mạng giữa trận (Wi-Fi → 4G) | ❌ Rớt | Tuỳ | ✅ Roaming: tự cập nhật địa chỉ theo gói đã xác thực |
| Phụ thuộc bên thứ 3 | Không | **Có** (dịch vụ relay) | Không (website chỉ dùng cấp license, offline vẫn chạy 7 ngày) |
| Giao thức hỗ trợ | Mọi | Thường chỉ SDK riêng | Mọi: TCP (HTTP/HTTPS/WS/SSH…) + UDP (game, QUIC, voice) + SDK message |

## 2. Hạn chế thật của P2P/NAT traversal (cần nói rõ với khách)

| Hạn chế | Nguyên nhân | peerova xử lý ra sao |
|---|---|---|
| Symmetric NAT ↔ symmetric NAT không punch được | Mỗi đích có một port ngoài khác nhau, không đoán được | Ưu tiên IPv6. Phía server bật UPnP/NAT-PMP hoặc mở 1 port UDP (dark nên an toàn). Port prediction để ở roadmap |
| CGNAT (100.64.0.0/10) | Nhà mạng NAT thêm một lớp, UPnP trên router vô dụng | IPv6, hoặc xin IP public (`peerova doctor` tự phát hiện) |
| Firewall doanh nghiệp chặn UDP | Chính sách mạng | Chưa xử lý được: không có relay, không có TCP fallback. Đây là giới hạn của lựa chọn "0 relay" |
| IP server lộ cho client hợp lệ | Kết nối trực tiếp thì gói phải mang IP thật | Private ticket, token/allow-list, giới hạn CCU, rate-limit handshake |
| Kết nối ban đầu chậm hơn kết nối thẳng | Thu thập địa chỉ, rồi punch, rồi bắt tay | Thường từ vài chục ms tới 1–2 s. Sau đó vận hành như kết nối thẳng |
| Cần một điểm hẹn để trao đổi địa chỉ | NAT ẩn địa chỉ thật | Supernode (server khác của chính hệ thống), ticket, join code thủ công |

## 3. Vì sao mô hình *dedicated server* hợp với peerova

Hole punching chỉ thất bại khi **cả hai** đầu đều ở NAT "khó". Với dedicated server, dev kiểm soát được mạng phía server: có IPv6, hoặc bật UPnP, hoặc mở một port UDP. Khi đó server reachable, và **mọi client** kết nối được bất kể NAT phía client ra sao. Mở port ở đây vẫn an toàn vì server là dark (không trả lời gói lạ). Đây là khác biệt so với P2P giữa hai người chơi, nơi không bên nào kiểm soát được mạng.

## 4. Số đo hiện có (minh bạch nguồn)

| Phép đo | Kết quả | Điều kiện |
|---|---|---|
| Ping khi ép kín link 1 MB/s, base RTT 40 ms | avg 52 ms (+12 ms), dùng 95% băng thông | Mô phỏng (`conn::tests::low_latency_under_bulk_load`) |
| Mất 10% / 30% gói | Dữ liệu đúng 100% | Mô phỏng có jitter và xáo thứ tự |
| Throughput | 982 Mbit/s | Loopback, cùng máy |
| Ping datagram | 0.34 ms avg | Loopback, cùng máy |
| HTTP + file 20 MB qua tunnel | Byte-identical | Loopback |
| **Internet thật (VPS ↔ máy nhà)** | *chưa đo*: chạy `scripts/vps-test.sh` | |

Số liệu tỷ lệ kết nối ngoài thực tế cần thu qua telemetry khi beta. Con số 85–95% ở bảng trên là **ước tính** dựa trên tài liệu công khai về UDP hole punching (Ford, Srisuresh & Kegel, *Peer-to-Peer Communication Across Network Address Translators*, USENIX 2005, báo cáo khoảng 82% NAT hỗ trợ UDP hole punching thời điểm đó), cộng với mức phổ cập IPv6 tăng lên ở Việt Nam. Chưa phải số đo của peerova.
