# Peerova: báo cáo kiểm định toàn diện (01/11/2026)

> Mọi số liệu dưới đây là **đo thật** trên máy phát triển (Ubuntu, Linux 7.0), không phải ước tính.
> "Lab NAT" = NAT Linux thật dựng trong network namespace (`scripts/nat-lab.sh`), có router nhà mạng ở giữa.
> **Chưa đo trên Internet thật** (VPS, 4G, Wi-Fi nhà mạng VN). Đây là việc còn lại quan trọng nhất (mục 10).

## 1. Tóm tắt

| Mảng | Đánh giá | Ghi chú |
|---|---|---|
| Đúng đắn (test tự động) | 🟢 | Core 43/43 · Website 38/38 · 5 binding PASS |
| Kết nối qua NAT | 🟢 / 🟡 | Có supernode: 5/6 kiểu NAT 100%. Chỉ dùng website: 4/6 |
| Ổn định | 🟢 | Soak 3 phút: 0% mất gói, 0 rớt kết nối |
| Tốc độ / độ trễ | 🟢 | UDP 982 Mbit/s, TCP fallback 851 Mbit/s (loopback). Ping khi link bị ép kín +12 ms |
| Độ mượt realtime (game, voice, video) | 🟢 | Mất 10% gói vẫn nhận 100% gói realtime nhỏ (gửi kèm dự phòng) |
| Tải (CCU) | 🟢 | 45 client đồng thời; giới hạn license chính xác |
| Bảo mật | 🟢 | Noise IK, server "dark", access token, thu hồi, audit |
| Giá & gói | 🟡 | Rẻ hơn đối thủ rất nhiều; có khoảng trống giữa Indie và Studio (mục 8) |
| Chuẩn doanh nghiệp | 🟡 | Đủ phần kỹ thuật; còn thiếu ký số bản phát hành, metrics, SLA, pháp lý (mục 9) |

## 2. Đúng đắn

| Bộ test | Kết quả | Nội dung chính |
|---|---|---|
| Core Rust (`cargo test --workspace`) | 43/43 | Crypto Noise, chống replay, transport mất 10–30% gói, kiểm soát tắc nghẽn, dark server, CCU, token, failover, TCP fallback, chia mảnh UDP, dải port, lease web ↔ core |
| Website (`node --test web/test`) | 38/38 | Đăng ký, lease, giới hạn số máy, thanh toán (webhook), admin, điểm hẹn, CSRF, rate-limit |
| Binding | 5/5 PASS | C, Python, C#, Java (JDK 8 & 21), Go. Unity/Godot mới compile-check |
| Sidecar stdio | PASS | Node.js gửi/nhận qua `peerova stdio` |
| License web ↔ core | PASS | Lease `pvl1.` do web ký được core chấp nhận đúng từng byte; lease sửa / sai máy / sai key bị từ chối |

## 3. Mọi trường hợp NAT (lab, không ai mở port)

| Máy chủ ↔ Người chơi | Qua supernode (3 vòng) | Chỉ qua website (2 vòng) |
|---|---|---|
| Router gia đình ↔ router gia đình | ✅ 3/3 · 0,3 s | ✅ 2/2 · 1,8 s |
| Router gia đình ↔ NAT khó (4G) | ✅ 3/3 · 1,8 s | ✅ 2/2 · 2,3 s |
| NAT khó ↔ router gia đình | ✅ 3/3 · 2,5 s | ✅ 2/2 · 4,2 s |
| Router gia đình ↔ NAT nhà mạng (CGNAT) | ✅ 3/3 · 1,6 s | ✅ 2/2 · 2,2 s |
| CGNAT ↔ CGNAT | ✅ 3/3 · 3,8 s | ❌ 0/2: website không thấy được port UDP thật |
| Symmetric ngẫu nhiên toàn dải ↔ như vậy | ❌ | ❌: giới hạn chung của cả ngành (cần khoảng 170.000 gói mỗi bên) |
| Mạng chặn UDP | ✅ TCP trực tiếp đa luồng | ✅ |
| Có IPv6 ở cả hai bên | ✅ đi thẳng | ✅ |

**Kết luận:** kịch bản chính "máy chủ ở nhà/văn phòng ↔ người dùng Wi-Fi/4G" thông trong mọi biến thể đã thử, ngoại trừ hai ca cuối. Để phủ cả CGNAT ↔ CGNAT, cần **ít nhất một supernode**: máy của bất kỳ khách hàng nào có IP public hoặc IPv6 cũng làm được, không cần VPS riêng.

## 4. Ổn định (soak test, 180 s)
| Chỉ số | Kết quả |
|---|---|
| Gói realtime UDP 60 Hz (100 B, kiểu game/voice) | 10.800 gửi · **0 mất (0,000%)** |
| Độ trễ khứ hồi | trung bình 0,53 · p50 0,51 · **p99 1,03** · max 8,35 ms |
| TCP 64 KB mỗi giây | 180/180 đúng từng byte |
| Rớt / kết nối lại | 0 |
| RAM server / client | 3,4 → 4,0 MB / 3,4 → 3,8 MB (tăng nhẹ lúc khởi động; cần soak 24 h để kết luận về rò rỉ) |

## 5. Tải (CCU, license trial 50 CCU thật từ website)
| Chỉ số | Kết quả |
|---|---|
| 45 client kết nối đồng thời | 45/45, chậm nhất 2,1 s (tạo client tuần tự) |
| Tin reliable khứ hồi | 900/900 |
| Vượt hạn mức (thêm 6) | đúng 5 vào, 1 bị từ chối |

## 6. Tốc độ & độ mượt
| Phép đo | Kết quả | Điều kiện |
|---|---|---|
| Throughput UDP / TCP fallback | 982 / 851 Mbit/s | loopback |
| Ping khi đang tải nặng | 52 ms trên nền 40 ms, dùng 95% băng thông | mô phỏng nút cổ chai 1 MB/s |
| Gói realtime nhỏ ở mức mất 10% | 100% tới nơi (gửi kèm dự phòng), 0 bản trùng | mô phỏng |
| Dữ liệu đúng khi mất 30% gói | 100% | mô phỏng |
| Phát hiện server chết → failover | 7,7 s; tắt máy A thì vào máy B trong 21 ms | loopback |

## 7. So sánh

| | Mở port | Relay (Photon / Unity Relay) | Tunnel cloud (ngrok / playit) | **Peerova** |
|---|---|---|---|---|
| Người chơi phải mở port | Không | Không | Không | **Không** |
| Máy chủ phải mở port | **Có** | Không | Không | **Không** |
| Dữ liệu đi qua bên thứ 3 | Không | **Có** | **Có** | **Không** |
| Độ trễ cộng thêm | 0 | +10–80 ms | +10–80 ms | **0** (đường thẳng) |
| Máy chủ bị quét / tấn công | **Có** | Không | Ít | **Không (dark)** |
| Mã hoá đầu-cuối | Tuỳ ứng dụng | Tới relay | Tới cloud | **Có** |
| Ẩn IP với người lạ | Không | Có | Có | **Có (private ticket)** |
| Kết nối 100% mọi NAT | — | **Có** | **Có** | Gần đủ (trừ sym↔sym toàn dải) |
| Chịu DDoS băng thông | Không | **Có** | **Có** | Một phần (failover) |
| Chi phí vận hành của nhà cung cấp | 0 | Cao (băng thông) | Cao | **≈ 0** |

## 8. Giá & gói

**Đối thủ** (trang giá chính thức, xem RESEARCH-IP-SECURITY.md):
- Photon khoảng $125 / 500 CCU và $250 / 1.000 CCU mỗi tháng
- Unity Relay 50 CCU miễn phí, sau đó $0,16 / CCU (+ phí băng thông)
- ngrok $8–20 / tháng
- playit.gg $3 / tháng

**Peerova hiện tại:**

| Gói | Server | CCU | Giá / tháng | $ / 1.000 CCU |
|---|---|---|---|---|
| Dev | 1 | 3 | 0 | — |
| Trial 14 ngày | 1 | 50 | 0 | — |
| Indie | 3 | 200 | 290.000đ / $12 | $60 |
| Studio | 10 | 2.000 | 1.490.000đ / $59 | **$30** |
| Enterprise | tuỳ | tuỳ | liên hệ | — |

So với Photon ($250 / 1.000 CCU) và Unity Relay (khoảng $152 / 1.000 CCU + băng thông), Peerova rẻ hơn **5–8 lần**.

**Vấn đề:**
1. Indie lên Studio là bước nhảy 10× CCU và 5× giá. Studio nhỏ (300–1.000 CCU) không có gói vừa.
2. Studio có thể đang **định giá thấp**, vì giá trị mang lại (không trả băng thông, ping thấp) lớn hơn nhiều.

**Đề xuất** (chỉnh được ngay trong trang Admin, không cần sửa code):

| Gói | Server | CCU | VND / tháng | USD / tháng |
|---|---|---|---|---|
| Dev | 1 | 3 | 0 | 0 |
| Indie | 2 | 200 | 290.000 | 12 |
| **Pro (mới)** | 5 | 1.000 | 790.000 | 32 |
| Studio | 15 | 5.000 | 2.490.000 | 99 |
| Enterprise | tuỳ, tự host authority, SLA | — | từ 9.900.000 | từ 399 |

Năm = giá 10 tháng (đã có). Gói Pro cần thêm vào code website, vì danh sách gói đang cố định là dev/trial/indie/studio/enterprise.

## 9. Checklist chuẩn doanh nghiệp

| Hạng mục | Trạng thái |
|---|---|
| Mã hoá E2E, chống MITM, chống replay | ✅ |
| Server dark, rate-limit, chống flood bắt tay / log | ✅ |
| Access token có hạn và phạm vi port (tích hợp SSO / IdP) | ✅ |
| Thu hồi người dùng tức thì | ✅ |
| Audit log JSON (SIEM) | ✅ |
| File cấu hình, systemd (sandbox), Windows Task Scheduler | ✅ |
| Failover nhiều máy, ticket merge | ✅ |
| License offline 7 ngày, không bóp tốc độ | ✅ |
| Website: admin, người dùng, thanh toán VN + quốc tế, hoá đơn, mobile | ✅ (thanh toán chưa test với sandbox thật của cổng) |
| Ghi công mã nguồn mở (THIRD_PARTY_NOTICES) | ✅ |
| Gói phát hành + checksum | ✅ |
| **Ký số bản phát hành** (Authenticode, minisign) | ❌ cần chứng chỉ |
| **Metrics** (Prometheus / OpenTelemetry) | ❌ chưa có |
| **Build macOS / Android / iOS** | ❌ chưa build (code đã đa nền tảng) |
| **Test Windows thật** | ❌ mới cross-compile |
| **Soak 24 h, test Internet thật** | ❌ |
| **Pentest / audit bảo mật độc lập** | ❌ |
| **SLA, EULA, DPA, Privacy Policy hoàn chỉnh** | 🟡 có bản sơ bộ trên web |
| **Pháp lý VN** (giấy phép mật mã, nhãn hiệu) | ❌ cần luật sư (LEGAL-VN.md) |

## 10. Việc cần làm trước khi bán (theo thứ tự)
1. **Test Internet thật:** máy nhà ↔ 4G Viettel / VNPT / FPT, qua `--rdv` và qua supernode (`scripts/vps-test.sh`).
2. **Pháp lý:** luật sư (giấy phép mật mã dân sự), đăng ký nhãn hiệu Peerova nhóm 9/42.
3. **Deploy website** lên Cloudflare Pages; test cổng thanh toán ở sandbox.
4. **Chốt bảng giá** (mục 8).
5. Soak 24 h, test trên Windows thật, ký số bản phát hành.
6. Thêm metrics; build macOS / Android / iOS.
