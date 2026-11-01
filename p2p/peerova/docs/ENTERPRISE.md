# peerova cho doanh nghiệp & studio: triển khai, bảo mật, vận hành

## 1. peerova dùng cho gì
Kết nối **trực tiếp, mã hoá đầu-cuối** tới bất kỳ dịch vụ nào nằm sau NAT/firewall, **không qua máy chủ trung gian của ai**:

| Nhóm | Ví dụ |
|---|---|
| Game | Dedicated server Unity/Unreal/Godot/Minecraft; UDP game, voice |
| Web/API | HTTP/1.1, HTTP/2, **HTTPS (không giải mã giữa đường)**, WebSocket/WSS, gRPC, QUIC/HTTP3 |
| Truy cập nội bộ | SSH, RDP, VNC, công cụ nội bộ ở chi nhánh, thay VPN appliance cho SME |
| Dữ liệu | MySQL, PostgreSQL, Redis, MongoDB, MQTT |
| Thiết bị | IoT/camera/router 4G sau CGNAT |
| Thời gian thực | VoIP/RTP (dải port), livestream, DNS |

**Hỗ trợ:** mọi giao thức chạy trên TCP hoặc UDP; dải port (tối đa 1024 port mỗi rule); UDP tới 64 KB/gói (tự chia mảnh).
**Chưa hỗ trợ:** ICMP/ping, IP thô (GRE, SCTP), multicast/broadcast. Chế độ VPN L3 (TUN) nằm trong roadmap.

## 2. Mô hình bảo mật

| Lớp | Cơ chế |
|---|---|
| Mã hoá | Noise IK (X25519, ChaCha20-Poly1305, BLAKE2s), giống họ WireGuard. Khoá phiên mới cho mỗi kết nối, chống replay theo cửa sổ 1024 gói |
| Xác thực server | Ticket chứa public key của server. Client từ chối mọi server không chứng minh được khoá, nên **không MITM được** |
| Server "dark" | Gói nào không mang MAC hợp lệ (cần có ticket) đều **bị bỏ im lặng, không phản hồi**. Quét port không thấy gì. Có test tự động |
| Ẩn địa chỉ | `--private`: ticket không chứa IP. Địa chỉ chỉ được tiết lộ qua supernode cho người chứng minh được mình giữ ticket |
| Xác thực người dùng | `--access-key`: **access token Ed25519** do hệ thống của bạn cấp, có **hạn dùng**, **chủ thể** (`sub`) và **phạm vi port**. Hoặc `--token` (bí mật dùng chung) |
| Phân quyền | Mỗi kết nối TCP/UDP được kiểm tra theo phạm vi token, vượt phạm vi thì bị từ chối và ghi audit |
| Thu hồi | `revoked.txt` (dùng `sub` hoặc `jti`) tự nạp lại sau tối đa 10 s, **ngắt ngay** các phiên đang mở |
| Chống lạm dụng | Rate-limit bắt tay theo IP. Bắt tay bị từ chối được ghi nhớ nên gửi lại không tốn CPU hay log. Giới hạn CCU theo license. Tối đa 4096 kết nối TCP fallback |
| Audit | `--audit-log audit.jsonl`: connect, disconnect, open (cho phép hoặc từ chối), security |
| Dữ liệu | Traffic người dùng cuối **không bao giờ** đi qua hạ tầng peerova hay website. Website chỉ cấp license |

**Giới hạn cần biết:**
- Client đã được cấp quyền sẽ thấy IP của server, vì đây là kết nối trực tiếp.
- Không hấp thụ được DDoS volumetric từ một client đã được cấp quyền.
- Hai bên cùng nằm sau symmetric NAT cấp port ngẫu nhiên thì không kết nối được, vì không có relay.

## 3. Tích hợp đăng nhập doanh nghiệp (SSO/IdP)

1. Một lần duy nhất: `peerova keygen --access --key access.key`. Giữ `access.key` ở **backend cấp quyền** (HSM/Vault nếu có). Đưa public key vào cấu hình server (`access_key`).
2. Người dùng đăng nhập qua SSO của bạn (Azure AD, Google Workspace, Keycloak…). Backend cấp token ngắn hạn:
   ```bash
   peerova token issue --access-key access.key --sub alice@corp.vn --ports tcp:443,tcp:22 --ttl 8h
   ```
   Hoặc tự ký bằng bất kỳ thư viện Ed25519 nào, theo định dạng:
   `pva1.<b64url(JSON)>.<b64url(Ed25519 sig trên chuỗi "pva1.<b64url(JSON)>")>`
   với JSON `{"v":1,"sub":"alice@corp.vn","ports":["tcp:443","udp:10000-10100"],"exp":<unix>,"jti":"<id>"}`.
   Phạm vi viết dạng: `*`, `tcp:*`, `udp:*`, `tcp:443`, `udp:10000-10100`.
3. Client: `peerova client <TICKET> --access-token <token> --forward tcp:8443=443`, hoặc trong SDK đặt `{"token":"<token>"}`.
4. Nghỉ việc hoặc mất máy: thêm `alice@corp.vn` vào `revoked.txt`, phiên bị ngắt trong vòng 10 s. Token cũng tự hết hạn theo `ttl`.

## 4. Audit log (JSON Lines → SIEM)
```json
{"ts":1793511597,"event":"connect","conn":"63900b0f","addr":"203.0.113.9:48536","path":"internet","key":"85f4…","sub":"bob"}
{"ts":1793511599,"event":"open","conn":"63900b0f","proto":"tcp","port":18090,"allowed":false}
{"ts":1793511600,"event":"security","msg":"từ chối client từ 198.51.100.7:35048: client không gửi access token (lặp lại sẽ bị bỏ qua im lặng)"}
{"ts":1793511700,"event":"disconnect","conn":"63900b0f","reason":"peer closed"}
```
Gửi về Splunk, Elastic, Loki hoặc Wazuh bằng agent đọc file (Filebeat, Promtail, Vector…).

## 4b. Chống DDoS & failover nhiều máy (không relay)
Ai biết IP của server đều có thể DDoS băng thông. Đây là sự thật với **mọi** máy chủ có IP public. Peerova xử lý theo bốn lớp:
1. **Không cho biết IP:** server dark, `private` ticket, IP chỉ lộ sau khi xác thực bằng access token có tên (`sub`). IP bị lộ thì truy được ai làm lộ và thu hồi ngay.
2. **Chạy nhiều máy cùng một danh tính:** copy `server.key` sang 2–3 máy, tốt nhất ở các nhà mạng hoặc nhà cung cấp khác nhau, rồi:
   ```bash
   peerova ticket merge @a/ticket.txt @b/ticket.txt @c/ticket.txt > ticket.txt
   ```
   Client thử song song mọi địa chỉ và vào máy còn sống. Supernode giới thiệu client tới **mọi** máy cùng key.
3. **Phát hiện máy chết nhanh:** khoảng 6–8 s không phản hồi là client tự ngắt. Dùng `--reconnect`, hoặc trong SDK gọi kết nối lại, để chuyển sang máy khác.
4. **Đổi IP không cần phát lại ticket:** với `private` ticket + supernode, server bị đánh chỉ cần đổi IP (xin IP mới từ nhà cung cấp) rồi khởi động lại. Client tìm lại qua supernode, ticket cũ vẫn dùng được.

Tuỳ chọn thêm: đặt server ở nhà cung cấp hosting có sẵn chống DDoS. Đây là lớp bảo vệ mạng của chính bạn, không phải relay.

## 4c. Không có VPS / supernode thì sao?
| Tình huống | Kết nối |
|---|---|
| Một bên có IPv6, IP public hoặc UPnP | ✅ Không cần máy chủ nào, chỉ cần trao ticket |
| Cùng LAN | ✅ |
| Cả hai chỉ có IPv4 sau NAT, không UPnP | ❌ Cần một điểm hẹn (supernode) để biết địa chỉ public. **Không có dữ liệu nào đi qua nó.** Máy rẻ nhất, hoặc server licensed của khách khác (tự làm supernode), là đủ |

Lab NAT thật (`scripts/nat-lab.sh`, không cần root): cone↔cone 5/5, cone↔symmetric 5/5, symmetric↔cone 4/5, symmetric↔symmetric 0/5 (giới hạn toán học khi không có relay).

## 5. Triển khai
| Nền tảng | Cách chạy |
|---|---|
| Linux | `deploy/peerova.service` (systemd, user riêng, sandbox: ProtectSystem=strict, NoNewPrivileges, chỉ ghi được `/opt/peerova`) |
| Windows Server/PC | `deploy/install-windows.ps1` (Task Scheduler chạy lúc khởi động bằng SYSTEM, tự khởi động lại, mở đúng port trên firewall) |
| Cấu hình | `deploy/peerova.example.json`. Mọi tham số CLI đều đặt được trong file, tham số CLI được ưu tiên |
| Firewall | Chỉ cần **1 port UDP**, cộng cùng port đó qua TCP nếu muốn hỗ trợ mạng chặn UDP. Server dark nên mở port vẫn an toàn |
| Nhiều server | Mỗi server một `server.key`, tức một ticket riêng. License tính theo số server và CCU |

## 6. Hiệu năng (số đo minh bạch)
| Phép đo | Kết quả | Điều kiện |
|---|---|---|
| Ping khi link bị ép kín | +12 ms trên nền 40 ms, dùng 95% băng thông | Mô phỏng nút cổ chai 1 MB/s |
| Mất 10% / 30% gói | Dữ liệu đúng 100% | Mô phỏng |
| UDP / TCP fallback (3 làn) | 982 / 851 Mbit/s, ping 0.3 ms | Loopback |
| Internet thật | `scripts/vps-test.sh` | Cần chạy ở môi trường của bạn |

## 7. Checklist trước khi đưa vào production
- [ ] `server.key` và `access.key` có quyền 600, được backup an toàn
- [ ] Bật `private` cho dịch vụ nội bộ. Dùng `access_key` thay vì `token` dùng chung
- [ ] `expose` chỉ những port thật sự cần. Dịch vụ phía sau chỉ lắng nghe trên 127.0.0.1
- [ ] Audit log được gửi về SIEM. Có cảnh báo cho `event=security`
- [ ] Chạy `peerova doctor` trên server: có IPv6 hoặc IP public hoặc UPnP để mọi client kết nối được
- [ ] Đo bằng `peerova ping` từ các mạng khách hàng tiêu biểu (cáp quang, 4G, Wi-Fi công ty)
- [ ] Kiểm tra checksum (`SHA256SUMS`) khi cài đặt
