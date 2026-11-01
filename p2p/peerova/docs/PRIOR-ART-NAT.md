# Prior art: kết nối P2P không relay — khảo sát và khuyến nghị cho Peerova

> Phạm vi: các dự án/giao thức thật, ưu tiên nguồn gốc (RFC, paper, tài liệu chính thức, mã nguồn). Ngày khảo sát: 2026-11. Ký hiệu: **EIM** = Endpoint-Independent Mapping (cone), **EDM/ADPM** = mapping phụ thuộc đích (symmetric) theo [RFC 4787](https://www.rfc-editor.org/rfc/rfc4787).

## 1. Kết luận nhanh

1. **Không có hệ thống nào trên thế giới giải được symmetric↔symmetric (mapping ngẫu nhiên toàn dải) một cách tin cậy mà không relay.** Mọi dự án lớn (Tailscale, libp2p, HyperDHT, WebRTC, ZeroTier, Nebula, Syncthing, EOS, Steam) đều rơi về relay hoặc báo lỗi. HyperDHT thậm chí không thử: `_punch()` trả `false` khi cả hai bên RANDOM ([holepuncher.js](https://app.unpkg.com/hyperdht@6.20.5/files/lib/holepuncher.js)), và tài liệu Pear ghi rõ "holepunching will fail if both … are on randomizing NATs… must be relayed" ([Pear docs](https://docs.pears.com/p2p/how-to/connect-to-peers/relay-connections-through-a-blind-relay/)).
2. **Mô hình "không hạ tầng" khả thi nhất là của HyperDHT / BEP 55 / I2P introducers:** chính các peer đến được (reachable) làm điểm hẹn và **chỉ chuyển tín hiệu (signaling)**, không chuyển dữ liệu. Peerova đã có "supernode" — chỉ cần nâng chúng thành DHT.
3. **Đòn bẩy lớn nhất cho thị trường VN là IPv6:** VNNIC công bố tỷ lệ dùng IPv6 của Việt Nam **67,68%** (hạng 2 ASEAN, hạng 7 thế giới; số liệu cuối 11/2025, công bố 6/2026) ([VietnamNews](https://vietnamnews.vn/society/1783748/viet-nam-s-ipv6-rate-hits-67-68-ranks-second-in-asean.html), [VnExpress](https://e.vnexpress.net/news/tech/vietnam-innovation/vietnam-ranks-second-in-asean-for-next-generation-internet-ipv6-adoption-5087562.html)); trình bày VNNIC 9/2025 cho FTTH ~80%, di động ~88% ([APNIC 60](https://conference.apnic.net/60/assets/presentation-files/390332a1-c8a1-449a-94cf-77c4a7be5162.pdf)). Không tìm được số liệu riêng Viettel/VNPT/FPT công khai — nên tự đo bằng telemetry. Đối chiếu thêm: [Google IPv6 per-country](https://www.google.com/intl/en/ipv6/statistics.html#tab=per-country-ipv6-adoption), [APNIC Labs VN](https://stats.labs.apnic.net/ipv6/VN).

## 2. Bảng tổng hợp các dự án

| Dự án | (1) Tìm nhau | (2) Học địa chỉ public | (3) Xuyên NAT | (4) Sym↔Sym | (5) Server bắt buộc? Ai chạy | (6) License |
|---|---|---|---|---|---|---|
| **pwnat** ([repo](https://github.com/samyk/pwnat)) | Client phải biết sẵn IP public của server | Server học IP client qua gói ICMP Time Exceeded giả | UDP punch cổng cố định 2222 | Thất bại (cần port preservation) | Không server nào; nhưng IP server phải biết trước (out-of-band) | GPL-3.0 |
| **chownat** (Samy Kamkar) | Hai bên biết IP nhau | Không cần | UDP/TCP punch đồng thời, cổng cố định | Thất bại | Không | GPL |
| **HyperDHT/Hyperswarm** ([repo](https://github.com/holepunchto/hyperdht)) | Kademlia DHT; 3 bootstrap `node{1,2,3}.hyperdht.org:49737`; có thể tự chạy bootstrap | Các DHT node phản hồi địa chỉ quan sát được; phân loại OPEN/CONSISTENT/RANDOM | DHT node giữ announce **chuyển tiếp Noise handshake + holepunch signaling**; `relayAddresses` (≤3) trong announce; birthday 256 socket, TTL 5, 1750 probe ngẫu nhiên/20 ms | Không thử; cần `relayThrough` (blind relay, tùy chọn) | Bootstrap do Holepunch chạy "for the commons"; mạng riêng cần ≥1 node bền | MIT |
| **BitTorrent** ([BEP 55](https://www.bittorrent.org/beps/bep_0055.html), [BEP 29 uTP](https://www.bittorrent.org/beps/bep_0029.html), [BEP 5 DHT](https://www.bittorrent.org/beps/bep_0005.html)) | Tracker / Mainline DHT / PEX | Header `yourip` trong extension handshake ([BEP 10](https://www.bittorrent.org/beps/bep_0010.html)) | Peer chung đang nối với cả hai gửi `rendezvous`→`connect` cho cả hai; cả hai mở uTP đồng thời | Thất bại (không relay) | Chỉ bootstrap DHT (router.bittorrent.com…) | Giao thức mở |
| **libp2p / IPFS** ([DCUtR spec](https://github.com/libp2p/specs/blob/master/relay/DCUtR.md)) | Kademlia DHT, bootstrap nodes của IPFS, mDNS | Identify (observed addr), AutoNAT v2 | Circuit Relay v2 (giới hạn thời gian/byte, chỉ để phối hợp) + DCUtR punch đồng bộ RTT | Thất bại, ở lại relay | Relay v2 do **peer public bất kỳ** chạy | MIT/Apache-2.0 |
| **WebRTC/ICE** ([RFC 8445](https://www.rfc-editor.org/rfc/rfc8445)) | Signaling tự định (không chuẩn hóa) | STUN [RFC 8489](https://www.rfc-editor.org/rfc/rfc8489) | Ghép cặp candidate, connectivity check | Cần TURN [RFC 8656](https://www.rfc-editor.org/rfc/rfc8656) | Signaling + STUN + (TURN) | Mở |
| **Tailscale** ([bài NAT](https://tailscale.com/blog/how-nat-traversal-works)) | Coordination server (hoặc Headscale) | STUN tới DERP | disco ping qua DERP, punch, birthday | Ở lại DERP (relay) | Control plane + DERP (Tailscale chạy, tự host được) | Client BSD-3 |
| **ZeroTier** ([docs](https://docs.zerotier.com/)) | Roots ("planet"), "moon" tự dựng | Roots phản hồi địa chỉ | Roots làm điểm hẹn, punch | **Roots relay** dữ liệu | Roots (ZeroTier Inc.) / moon tự chạy | BSL 1.1 (1.x) |
| **Nebula** ([docs](https://nebula.defined.net/docs/)) | Lighthouse (do người dùng chạy) | Lighthouse ghi địa chỉ quan sát | Lighthouse báo cả hai punch; `punchy` giữ mapping | Relay tùy chọn (≥v1.6) | Lighthouse có IP public | MIT |
| **n2n** ([repo](https://github.com/ntop/n2n)) | Supernode | Supernode | Punch UDP | Supernode relay | Supernode tự chạy | GPL-3.0 |
| **tinc** ([manual](https://www.tinc-vpn.org/documentation/)) | `ConnectTo` cấu hình tay, meta-connection TCP | Qua meta-connection | Punch UDP gián tiếp | Chuyển qua node trung gian trong mesh | Ít nhất 1 node reachable | GPL-2.0 |
| **Yggdrasil** ([docs](https://yggdrasil-network.github.io/)) | Peer tĩnh + multicast LAN | Không cần | **Không** xuyên NAT; định tuyến overlay qua peer | Overlay tự "relay" qua nút trung gian | Public peers cộng đồng | LGPL-3.0 |
| **Syncthing** ([docs](https://docs.syncthing.net/)) | Global discovery (Syncthing Foundation) + broadcast LAN 21027 | STUN (QUIC), UPnP/NAT-PMP | QUIC punch, TCP | Relay pool cộng đồng | Discovery + relay pool | MPL-2.0 |
| **Skype (cổ)** ([Baset & Schulzrinne, INFOCOM 2006](https://arxiv.org/abs/cs/0412017)) | Login server trung tâm + overlay supernode | Supernode | Supernode phối hợp | **Supernode = máy người dùng relay** | Login server của Skype | Độc quyền |
| **I2P** ([SSU2](https://geti2p.net/spec/ssu2)) | Reseed servers + netDb (floodfill) | Peer Test (3 bên) | **Introducers**: router reachable gửi RelayIntro cho router tường lửa → punch | Không, router bị đánh dấu firewalled | Reseed (tình nguyện) | Public domain/mix |
| **Tor** | Directory authorities (9–10 cố định) | Không cần | Không punch; mọi thứ qua circuit | — | Directory authorities | BSD-3 |
| **Bitcoin Core** | DNS seeds + hardcoded seeds + `addr` gossip | Peer báo `addrMe` trong `version` | Không punch; v29 thay UPnP bằng **PCP/NAT-PMP** ([release notes 29.0](https://bitcoincore.org/en/releases/29.0/)) | — | DNS seeds (nhà phát triển chạy) | MIT |
| **Ethereum discv5** ([spec](https://github.com/ethereum/devp2p/blob/master/discv5/discv5.md)) | Kademlia UDP, bootnodes, ENR | PONG trả endpoint quan sát | Đề xuất discv5.2: node chung chuyển `RELAYINIT/RELAYMSG` để punch | — | Bootnodes (EF/team client) | Mở |
| **Valve GameNetworkingSockets** ([repo](https://github.com/ValveSoftware/GameNetworkingSockets)) | Signaling qua Steam hoặc tự viết (`ISteamNetworkingConnectionSignaling`) | ICE/STUN | ICE (libjuice/WebRTC) | Steam Datagram Relay | Signaling của bạn + STUN | BSD-3 |
| **Epic EOS P2P** ([docs](https://dev.epicgames.com/docs/game-services/p2p)) | EOS backend | EOS | NAT traversal của EOS | `EOS_RC_NoRelays` → thất bại; mặc định cho relay | Epic (miễn phí) | Độc quyền |
| **Hamachi / Radmin VPN** | Server trung gian LogMeIn / Famatech | Server | Punch | Relay qua server của hãng | Hãng | Độc quyền |
| **Parsec** ([docs](https://support.parsec.app/hc/en-us/articles/32381606495124)) | Server Parsec | STUN-like của Parsec | Punch UDP (BUD) | Relay chỉ bản Teams/Enterprise | Parsec | Độc quyền |
| **Moonlight/Sunshine** ([Sunshine](https://github.com/LizardByte/Sunshine)) | Nhập IP thủ công | Không | Không — cần port forward/UPnP hoặc VPN | — | Không | GPL-3.0 |
| **Chrome Remote Desktop** | Signaling Google | STUN Google | WebRTC ICE | TURN của Google | Google | Độc quyền |

## 3. Chi tiết các kỹ thuật đáng học

### 3.1 pwnat — xuyên NAT bằng ICMP, "không bên thứ ba"
Cơ chế ([README](https://github.com/samyk/pwnat); phân tích học thuật: Müller, Evans, Grothoff, *Autonomous NAT Traversal*, IEEE P2P 2010, [PDF](https://grothoff.org/christian/pwnat.pdf)):
1. **Server** (sau NAT) liên tục gửi ICMP Echo Request tới một IP không phản hồi (`3.3.3.3`). NAT của server tạo một entry ICMP.
2. **Client** (biết IP public của server) **giả** một gói ICMP *Time Exceeded* như thể một router trên đường đi trả lời, bên trong nhúng bản sao Echo Request của server (đích 3.3.3.3, đúng ID/seq). NAT server khớp payload nhúng với entry → chuyển vào trong → server biết IP public của client.
3. Hai bên punch UDP tới nhau với cổng nguồn/đích cố định (mặc định 2222), sau đó tunnel TCP qua UDP; keepalive ≥5 s.

Giới hạn:
- **Vẫn cần biết IP public của server** — "không bên thứ ba" chỉ vì việc trao đổi IP được làm ngoài băng. Peerova đã giải được việc này bằng ticket/website.
- **Cần raw socket**: gửi Time Exceeded tùy ý và đọc ICMP thô cần `CAP_NET_RAW`/root trên Linux; socket ICMP không đặc quyền (`SOCK_DGRAM, IPPROTO_ICMP`, bị giới hạn bởi `net.ipv4.ping_group_range`) chỉ cho Echo, không cho giả Time Exceeded. Windows cần quyền Administrator cho raw socket (và Windows chặn nhiều kiểu raw gửi đi từ XP SP2); macOS cũng cần root. → **Không chạy được unprivileged**.
- Phụ thuộc NAT chuyển tiếp ICMP error khớp payload và **giữ nguyên cổng nguồn** UDP (port preservation); CGNAT, NAT symmetric và nhiều firewall lọc ICMP → thất bại. Bài báo gốc thừa nhận chỉ hoạt động trên một phần NAT.
- **Đánh giá cho Peerova: không đáng đưa vào lõi.** Lợi ích duy nhất (server không cần biết IP client trước) đã có qua rendezvous; chi phí (root, độ tin cậy thấp, dễ bị IDS coi là spoof) cao.

### 3.2 HyperDHT — DHT làm điểm hẹn
- Server announce khóa công khai vào các DHT node gần nhất theo Kademlia; **chính các node lưu announce chuyển tiếp handshake Noise và tin nhắn holepunch** giữa client và server. Dữ liệu không đi qua chúng.
- Mỗi node tự phân loại firewall (OPEN/CONSISTENT/RANDOM) bằng cách so địa chỉ mà nhiều DHT node phản chiếu. Chiến lược: CONSISTENT↔CONSISTENT → probe 10 vòng; CONSISTENT↔RANDOM → bên consistent phóng ~1750 probe cổng ngẫu nhiên; RANDOM↔CONSISTENT → bên random mở 256 "birthday socket" TTL thấp; RANDOM↔RANDOM → không punch, thử "reopen" socket tối đa 3 lần với hy vọng NAT trở nên consistent ([holepuncher.js](https://app.unpkg.com/hyperdht@6.20.5/files/lib/holepuncher.js)).
- Node chỉ được nhận vào bảng routing khi **không firewalled** (đã được peer khác kiểm chứng gọi vào được) — ý tưởng giống cờ "firewalled" Peerova nên có.
- Peerova đã gần như sao chép đúng chiến lược này; chênh lệch chính là **HyperDHT không cần website rendezvous** vì DHT gánh signaling.

### 3.3 BEP 55 và I2P introducers — "peer làm rendezvous"
BEP 55: A và B cùng nối với C. A gửi `rendezvous(B)` tới C; C gửi `connect(A)` cho B và `connect(B)` cho A; cả hai mở uTP đồng thời. Lỗi trả mã `NoSuchPeer/NotConnected/NoSupport/NoSelf`. I2P SSU2 làm tương tự: router tường lửa đăng ký *introducer* (router reachable) trong RouterInfo; người gọi gửi RelayRequest qua introducer ([SSU2 spec](https://geti2p.net/spec/ssu2)). **Đây chính là mẫu Peerova nên dùng để bỏ website rendezvous khỏi đường nóng.**

### 3.4 libp2p DCUtR — số đo thực tế lớn nhất
Đo trên ~85.000 mạng IPFS: tỷ lệ punch thành công ~**70% ±7,1%**, TCP và QUIC **tương đương thống kê** (bác bỏ "UDP luôn tốt hơn") ([Trautwein et al., arXiv 2510.27500](https://arxiv.org/abs/2510.27500), [ProbeLab](https://probelab.io/blog/can-libp2p-punch-through-nats/)). Port-restricted cone ~82,9%. Hàm ý: nỗ lực TCP simultaneous-open của Peerova là đáng giá.

### 3.5 Tailscale — con số birthday
Từ [How NAT traversal works](https://tailscale.com/blog/how-nat-traversal-works):

| Kịch bản | Số probe | Xác suất |
|---|---|---|
| easy ↔ hard (bên hard mở 256 cổng) | 174 | 50% |
| | 256 | 64% |
| | 1024 | 98% |
| | 2048 | 99,9% (≈20 s @100 pps) |
| hard ↔ hard (256/2048 như trên, 20 s) | — | ~0,01% |
| hard ↔ hard để đạt 50% | ~54.000 gói/bên | ~9 phút |
| hard ↔ hard để đạt 99,9% | ~170.000 gói/bên | ~28 phút; có thể làm cạn bảng phiên NAT (Juniper SRX300: 64k phiên) |

Tailscale kết luận: hard↔hard → dùng DERP. Đây là căn cứ định lượng cho việc Peerova "symmetric↔symmetric impossible".

### 3.6 Nghiên cứu học thuật về tỷ lệ thành công

| Nguồn | Kết quả |
|---|---|
| Ford, Srisuresh, Kegel, *P2P Communication Across NATs*, USENIX ATC 2005 ([PDF](https://bford.info/pub/net/p2pnat/)) | UDP hole punching được ~82% NAT trong mẫu NAT Check hỗ trợ; TCP ~64% |
| Guha & Francis, *Characterization and Measurement of TCP Traversal through NATs and Firewalls*, IMC 2005 ([PDF](https://www.usenix.org/legacy/events/imc05/tech/full_papers/guha/guha.pdf)) | STUNT/TCP traversal ~85–90% với NAT thực tế; mô tả TTL thấp, port prediction |
| Takeda, *Symmetric NAT Traversal using STUN*, draft IETF 2003 ([draft](https://datatracker.ietf.org/doc/html/draft-takeda-symmetric-nat-traversal-00)) | Port prediction cho NAT tăng tuần tự — chỉ có ích khi mapping không ngẫu nhiên |
| Halkes & Pouwelse, *UDP NAT and Firewall Puncturing in the Wild*, IFIP Networking 2011 | Đo trên Tribler: ~64% lần puncture thành công ngoài thực tế |
| [RFC 5128](https://www.rfc-editor.org/rfc/rfc5128) | Tổng hợp kỹ thuật P2P qua NAT; xác nhận symmetric↔symmetric cần relay |
| Trautwein et al. 2025 | ~70% DCUtR, TCP≈QUIC |

## 4. Port preservation và hành vi NAT gia dụng
- Linux netfilter MASQUERADE/SNAT **giữ nguyên cổng nguồn khi còn trống** và tái dùng mapping cho cùng 5-tuple nguồn; phần lớn router gia dụng (OpenWrt, firmware dựa Linux) vì thế cư xử như EIM + port preservation khi ít xung đột ([netfilter NAT HOWTO](https://www.netfilter.org/documentation/HOWTO/NAT-HOWTO-6.html)). Ngoại lệ: tùy chọn `--random`/`--random-fully`.
- Symmetric "thật" thường đến từ **CGNAT của nhà mạng (đặc biệt di động)** và firewall doanh nghiệp. CGNAT thường dùng **Port Block Allocation** (mỗi thuê bao một khối vài trăm–vài nghìn cổng, [RFC 7422](https://www.rfc-editor.org/rfc/rfc7422)) — điểm khai thác ở mục 5.
- Không có thống kê toàn cầu gần đây đáng tin về tỷ lệ port preservation; khuyến nghị Peerova tự thu (ẩn danh) qua telemetry NAT-type.

## 5. Có cách nào cho symmetric↔symmetric không relay?
Trung thực: **với mapping ngẫu nhiên đều trên toàn dải ~64k cổng, về mặt xác suất là không thực tế** (bảng 3.5). Các khe hở có thật:

| Khe hở | Điều kiện | Ước lượng |
|---|---|---|
| **Một bên có IPv6** | Cả hai có IPv6 → chỉ còn firewall stateful, punch IPv6 gần như luôn được | VN ~68% → xác suất cả hai có v6 ≈ 45% nếu độc lập (cao hơn nếu cùng nhà mạng) |
| **Thu hẹp không gian cổng (CGNAT PBA)** | Mapping ngẫu nhiên nhưng trong khối kích thước N | Số cặp trúng kỳ vọng ≈ m²/N² với m probe mỗi bên: N=1024, m=1024 → ~63%, ~10 s @100 pps; N=4096 cần ~4k probe/bên. Peerova nên **đo dải cổng quan sát được** (nhiều reflector) để biết N |
| **Symmetric nhưng tuần tự** | Đã có port prediction | Đã làm |
| **Hành vi khác theo giao thức** | Một số NAT symmetric với UDP nhưng TCP khác (hoặc ngược lại) | Thử TCP simultaneous-open song song — đo libp2p cho thấy TCP≈UDP |
| **Đổi đường mạng** | Thiết bị có cả Wi-Fi và 4G/5G; một mạng có thể là cone | Thử mọi interface (đa đường ICE-style) |
| **Mở cổng chủ động** | UPnP/NAT-PMP/**PCP** trên một trong hai bên → bên đó thành "open" | Đã có UPnP/NAT-PMP; thêm PCP |

Mọi thứ còn lại (ví dụ dự đoán PRNG cổng của NAT) chưa thấy công trình nào làm được ở quy mô.

## 6. Khuyến nghị ưu tiên cho Peerova

| # | Khuyến nghị | Lợi ích | Cần hạ tầng always-on? |
|---|---|---|---|
| 1 | **IPv6-first + punch IPv6 đồng thời** (gửi gói từ cả hai phía để mở pinhole firewall CPE); thêm mở pinhole qua UPnP IGDv2 `WANIPv6FirewallControl` và PCP | Lớn nhất ở VN; vượt qua mọi NAT44 | **Không** |
| 2 | **Nhúng Kademlia DHT** (kiểu HyperDHT/discv5) giữa supernode + client: khóa = hash(peer pubkey); node lưu announce **chuyển signaling** (giống BEP 55/I2P introducer); chỉ node `firewalled=false` (đã được kiểm chứng gọi vào) mới vào bảng routing; reflection địa chỉ = trường "observed addr" trong mọi PONG (như discv5/Identify). Giới hạn tốc độ và kích thước tin signaling để không bị lợi dụng thành relay dữ liệu | Bỏ website khỏi đường nóng; phân loại NAT bằng nhiều reflector | Không cần server của Peerova, **nhưng cần ít nhất vài node reachable trực tuyến** (supernode của người dùng). Đây là hạ tầng phân tán do người dùng chạy |
| 3 | **Bootstrap nhiều tầng**: (a) cache peer/supernode lần trước (như Bitcoin `peers.dat`), (b) danh sách ký số từ website (tĩnh, có thể phục vụ qua CDN/GitHub Pages), (c) DNS seed TXT, (d) mDNS/broadcast LAN (như Syncthing 21027), (e) ticket ký chứa sẵn địa chỉ | Website chỉ cần cho **lần chạy đầu tiên** | (b)/(c) là file tĩnh, không phải server động; (a)(d)(e) không cần gì |
| 4 | **Thêm PCP (RFC 6887)** cạnh NAT-PMP (PCP tương thích ngược NAT-PMP qua cùng cổng 5351; hỗ trợ IPv6 và CGNAT có PCP server) — Bitcoin Core 29 đã đi đúng hướng này | Tăng tỷ lệ "open" | Không |
| 5 | **Đo dải cổng mapping** (nhiều reflector, nhiều socket) → nếu khối nhỏ (PBA), chạy birthday **hai phía** giới hạn trong khối; có trần thời gian/số gói để không phá bảng NAT | Có thể cứu một phần symmetric↔symmetric trên CGNAT di động | Không (dùng supernode/DHT làm reflector) |
| 6 | **TCP simultaneous-open song song UDP** và thử mọi interface (Wi-Fi + di động) | +vài % theo dữ liệu libp2p | Không |
| 7 | **Telemetry NAT ẩn danh, opt-in**: loại mapping/filtering, port preservation, kích thước khối, có IPv6 hay không, theo ASN (Viettel AS7552, VNPT AS45899, FPT AS18403) | Ra quyết định dựa trên số thật | Cần endpoint thu thập (có thể tắt) |
| 8 | **Không dùng pwnat/ICMP** trong sản phẩm: cần root/Admin, không unprivileged trên Linux/Windows/macOS, độ tin cậy thấp với CGNAT | Tránh rủi ro | — |
| 9 | Với sym↔sym còn lại: **báo lỗi rõ ràng + hướng dẫn** (bật UPnP, IPv6, đổi mạng, port-forward) thay vì relay ngầm — đúng chính sách "no relay" như `EOS_RC_NoRelays` | Trung thực với khách hàng | Không |

**Giới hạn không vượt qua được:** một client mới tinh không có cache, không có LAN peer, không có ticket thì **phải** lấy được ít nhất một địa chỉ từ đâu đó (website tĩnh, DNS, hoặc người dùng dán vào). "Hoàn toàn không có hạ tầng" chỉ đạt được khi chấp nhận bootstrap thủ công (như Moonlight/tinc/pwnat). Tối thiểu hợp lý: **một file tĩnh ký số + mạng DHT do supernode của người dùng duy trì**.
