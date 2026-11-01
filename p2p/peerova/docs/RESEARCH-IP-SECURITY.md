# p2pt: nghiên cứu đối thủ, sở hữu trí tuệ, pháp lý và chống dịch ngược

> **Cập nhật:** sản phẩm đã đổi tên từ "p2pt" thành **Peerova** (CLI `peerova`, thư viện `libpeerova`). Các chỗ nhắc tới "p2pt" bên dưới là tên cũ.


> Ngày truy cập nguồn: **01/11/2026**. Giá cả và văn bản pháp luật thay đổi thường xuyên. Mọi kết luận pháp lý dưới đây chỉ là thông tin nghiên cứu, **không thay thế tư vấn luật sư** (nhất là phần mật mã dân sự ở mục 2.5).

---

## 1. Đối thủ & prior art

### 1.1 Bảng so sánh

| Sản phẩm | Relay? | Cách xuyên NAT | Mã hóa | Mô hình giá (giá hiện tại) | License mã nguồn |
|---|---|---|---|---|---|
| Valve GameNetworkingSockets / Steam Networking | Có: SDR, nhưng chỉ dùng được trên Steam | ICE (dựa trên WebRTC), signaling tự cắm | AES-GCM-256, Curve25519 | Miễn phí (SDR gắn với Steamworks) | BSD-3 ([GitHub](https://github.com/ValveSoftware/GameNetworkingSockets)) |
| Epic Online Services P2P | Có, tự động | NAT punch-through rồi fallback relay. Dành cho P2P giữa người chơi, không cho dedicated server ([Edgegap](https://edgegap.com/blog/can-epic-online-services-eos-relays-allow-for-dedicated-server-or-authoritative-server)) | Có (DTLS) | Miễn phí ([Epic](https://www.unrealengine.com/blog/epic-s-free-online-services-launch-for-all-game-developers)) | SDK đóng |
| Photon Fusion/PUN/Realtime | Có: mọi traffic đi qua cloud Photon | Relay. Fusion có thêm punch-through ở chế độ host | Tùy chọn | Theo CCU: 20 CCU free; 100 CCU free (1 app); 100 CCU trả 1 lần $95/12 tháng; 500 CCU $125/tháng; 1000 CCU $250/tháng; 2000 CCU $500/tháng; Premium $0,5/CCU, tối thiểu $1000/tháng; vượt traffic $0,05–0,10/GB ([pricing](https://www.photonengine.com/fusion/pricing)) | SDK đóng |
| Unity Relay + Netcode for GameObjects | Có | Relay | DTLS | 50 CCU trung bình free, sau đó $0,16/CCU; egress $0,09/GiB (US/EU), $0,16/GiB (Á/Úc) ([Unity](https://unity.com/products/gaming-services/pricing)) | NGO: Unity Companion License |
| Mirror / FishNet | Tùy transport (thường dùng Steam/EOS/Relay) | Không tự có | Tùy transport | Miễn phí; FishNet "không giới hạn CCU" ([docs](https://fish-networking.gitbook.io/docs)) | Mirror: MIT; FishNet: license riêng |
| libp2p | Có: Circuit Relay v2 do node công khai cung cấp | DCUtR hole punching + AutoNAT. Cơ chế rất giống "supernode" của p2pt | Noise XX / TLS | Miễn phí | MIT/Apache-2.0 ([rust-libp2p](https://github.com/libp2p/rust-libp2p)) |
| Tailscale | Có (DERP) | STUN-like + hole punching + UPnP/NAT-PMP/PCP | WireGuard (Noise IK) | Personal $0 (6 user, phi thương mại); Standard $8/user/tháng; Premium $18/user/tháng ([pricing](https://tailscale.com/pricing)) | Client BSD-3 |
| ZeroTier (+ libzt SDK) | Có (root/relay) | Hole punching qua root server | Riêng (C25519, AES-GMAC-SIV) | Personal free (10 thiết bị); Essential $18/tháng (10 thiết bị, +$2/thiết bị); Scale $179/tháng; Enterprise có bản self-host ([pricing](https://www.zerotier.com/pricing/)) | BSL 1.1 → Apache theo từng phiên bản; từ 1.16 là MPL + phần nonfree ([changelog](https://docs.zerotier.com/changelog/2025/08/21/zt1-v1.16.0)) |
| NetBird | Có (TURN/relay) | ICE | WireGuard | Free 5 user; Team €6/user/tháng; Business €12/user/tháng ([pricing](https://netbird.io/pricing)) | Client BSD-3; server AGPLv3 từ v0.53 ([WZ-IT](https://wz-it.com/en/blog/netbird-self-hosting-license-commercial-starter/)) |
| Nebula (Slack/Defined Networking) | Tùy chọn (từ bản 1.6) | **Lighthouse** do người dùng tự chạy + hole punching. Prior art gần nhất với "supernode không relay" | Noise IX, Curve25519, AES-GCM/ChaCha | Miễn phí; bản managed bán qua defined.net | MIT ([GitHub](https://github.com/slackhq/nebula)) |
| WireGuard | Không | Không tự có (chỉ có keepalive) | Noise IK, ChaCha20-Poly1305, BLAKE2s | Miễn phí | Kernel GPLv2; wireguard-go MIT; boringtun BSD-3 |
| ngrok | Có (toàn bộ traffic) | Reverse tunnel ra cloud | TLS | Free (1 GB); Hobbyist $8–10/tháng; Pay-as-you-go $20/tháng + phí theo dung lượng; Enterprise từ $10k/năm ([pricing](https://ngrok.com/pricing.md)) | Agent đóng; SDK ngrok-rust Apache-2.0 |
| playit.gg | Có (proxy toàn cầu) | Reverse tunnel | Không mã hóa đầu-cuối | Free; Premium $3/tháng ([playit.gg](https://playit.gg/)) | Agent BSD-2 ([GitHub](https://github.com/playit-cloud/playit-agent)) |
| Cloudflare Tunnel | Có (edge Cloudflare) | Kết nối outbound | TLS/QUIC tới edge | Tunnel miễn phí; Zero Trust free 50 user, sau đó ~$7/user/tháng | cloudflared Apache-2.0 |
| LogMeIn Hamachi | Có (fallback) | Hole punching + relay | Riêng | Free 5 máy; Standard $49/năm; Premium $199/năm; Multi-Network $299/năm ([vpn.net](https://www.vpn.net/)) | Đóng |
| Radmin VPN | Không công bố | Không công bố | Không công bố | Miễn phí (kéo khách sang Radmin trả phí) ([TechRadar](https://www.techradar.com/reviews/radmin-vpn)) | Đóng |
| ENet / yojimbo / netcode.io | Không | **Không xuyên NAT** | ENet: không; netcode.io: ChaCha20-Poly1305 + *connect token* ký bởi backend, server bỏ qua gói không hợp lệ (prior art cho "ticket" và "dark server") | Miễn phí | ENet MIT; yojimbo/netcode BSD-3 |
| Godot High-level Multiplayer | Không | ENet (không xuyên NAT) hoặc WebRTC (cần STUN/TURN tự lo) | DTLS tùy chọn | Miễn phí | MIT |
| Unreal NetDriver / Online Subsystem | Qua Steam/EOS | Nhờ OSS Steam/EOS | Tùy OSS | Unreal EULA (5% royalty khi vượt $1M) | Source-available |

### 1.2 Khoảng trống khác biệt của p2pt

**Điểm mạnh thật sự:**
1. Đường dữ liệu không đi qua hạ tầng nào. Vendor không tốn băng thông, nên có thể bán giá thấp hơn Photon/Unity/ngrok vốn tính tiền theo GB/CCU. Đây cũng là lợi thế về riêng tư: vendor không thấy traffic.
2. Ít đối thủ gộp đủ: SDK nhúng cho game (C ABI, đa engine), tunnel TCP/UDP tổng quát như ngrok/playit, và hướng tới dedicated server mà không gắn với nền tảng Steam/Epic. EOS P2P không hỗ trợ dedicated server. SDR chỉ có trên Steam.
3. Thị trường VN: định giá bằng VND, hỗ trợ tiếng Việt, xuất hóa đơn điện tử.

**Điểm yếu cần nói thẳng (và nên ghi trong tài liệu sản phẩm):**
- **Không có relay fallback thì tỷ lệ kết nối thấp hơn.** Mọi đối thủ lớn (Tailscale DERP, EOS, Steam SDR, libp2p relay) đều giữ relay vì một phần đáng kể mạng nằm sau symmetric NAT hoặc CGNAT (mạng di động, một số ISP VN). Trường hợp server ở sau CGNAT, không có IPv6 và không map được port sẽ gần như thất bại ([Tailscale: How NAT traversal works](https://tailscale.com/blog/how-nat-traversal-works)).
- **Client được cấp phép sẽ thấy IP của server.** Steam SDR che IP để chống DDoS, p2pt thì không. Dark server chỉ chặn được người chưa xác thực. Người chơi có ticket hợp lệ vẫn biết IP và có thể DDoS.
- **Supernode là server của khách hàng khác.** Điều này kéo theo rủi ro về tin cậy (supernode thấy IP và metadata của bên thứ ba), rủi ro pháp lý (khách hàng vô tình trở thành bên xử lý dữ liệu cá nhân) và rủi ro bị lạm dụng. Cần điều khoản đồng ý rõ ràng trong EULA và cho phép tắt.
- **Prior art kỹ thuật rất dày** (Nebula, libp2p, Tailscale, netcode.io). Khác biệt nằm ở đóng gói, giá và thị trường, không nằm ở phát minh.
- **Phải cạnh tranh với đồ miễn phí:** GNS, EOS, Nebula, libp2p.

---

## 2. Rủi ro bằng sáng chế & pháp lý

### 2.1 Bằng sáng chế về NAT traversal

Tra cứu IPR khai báo trên IETF Datatracker (API `ipr/iprdocrel`):

| RFC / draft | Chủ sở hữu | Bằng | Tình trạng |
|---|---|---|---|
| RFC 5245 / 5389 / 3489 (ICE, STUN) | SSH Communications (Tatu Ylönen), khai năm 2013, "unwilling to commit" ([IPR 1986](https://datatracker.ietf.org/ipr/1986/)) | US 6,795,917 và họ EP1036460 | **Hết hạn 30/12/2018** ([Google Patents](https://patents.google.com/patent/US6795917B1/en)). Có thêm EP2254311 (cùng họ, ưu tiên 1997, hẳn cũng đã hết hạn) |
| draft-ietf-ice-rfc5245bis (RFC 8445) | Cisco, cam kết RAND ([IPR 3115](https://datatracker.ietf.org/ipr/3115/)) | US 7,302,496 | **Hết hạn 14/01/2025** ([Google Patents](https://patents.google.com/patent/US7302496B1/en)) |
| draft-ietf-mmusic-ice (2005–2007) | Tandberg (nay thuộc Cisco), RAND theo nguyên tắc có đi có lại ([IPR 665](https://datatracker.ietf.org/ipr/665/)) | Đơn US 11/008,150 (nộp 2004) | Họ bằng nộp 2004, sẽ hết hạn khoảng 2024–2025. Cần xác minh |
| RFC 3489 | Nortel, nay là Genband ([IPR 918](https://datatracker.ietf.org/ipr/918/)) | US 6,993,595 | **Hết hạn 10/12/2023** ([Google Patents](https://patents.google.com/patent/US6993595B1/en)) |
| RFC 6886 (NAT-PMP), RFC 6887 (PCP), RFC 8489 | Không có khai báo IPR | | |

**Kết luận:** các bằng nền tảng về STUN/ICE đã hết hạn, và UDP hole punching đã là prior art công khai từ đầu thập niên 2000. Rủi ro còn lại nằm ở các bằng hẹp về kỹ thuật cụ thể, ví dụ dự đoán port cho symmetric NAT hay phát hiện hole-punching ([US11824831](https://patents.google.com/patent/US11824831)). Không tìm thấy vụ kiện nào về hole punching. Nên: (a) bám sát kỹ thuật đã có trong RFC hoặc mã nguồn mở lâu năm; (b) trước khi thêm "port prediction" nâng cao, làm một FTO (freedom-to-operate) rẻ, tập trung vào thị trường Mỹ.

**Noise Protocol:** đặc tả thuộc public domain ([noiseprotocol.org](https://noiseprotocol.org/), [wiki](https://github.com/trevp/noise/wiki)). **WireGuard:** giao thức dùng tự do, nhưng **tên "WireGuard" là nhãn hiệu**. Chính sách nhãn hiệu chỉ cho dùng hợp lý (fair use) trong ngữ cảnh phi thương mại ([policy](https://www.wireguard.com/trademark-policy/)). Marketing nên viết "dùng Noise IK, cùng họ mật mã với WireGuard®" kèm câu miễn trừ, hoặc xin phép. Tuyệt đối không dùng logo.

### 2.2 License mã nguồn mở (đã kiểm tra `Cargo.lock` của repo)

| Crate | License |
|---|---|
| x25519-dalek, ed25519-dalek, curve25519-dalek, subtle | **BSD-3-Clause** |
| chacha20poly1305, chacha20, poly1305, blake2, getrandom, zeroize, sha2, serde… | MIT OR Apache-2.0 |
| generic-array | MIT |
| unicode-ident (chỉ dùng lúc build) | (MIT OR Apache-2.0) AND Unicode-3.0 |

Nghĩa vụ: BSD-3 và MIT yêu cầu **kèm thông báo bản quyền và nguyên văn license** trong tài liệu hoặc gói phân phối dạng binary. BSD-3 còn **cấm dùng tên tác giả để quảng bá**. Với Apache-2.0, chọn nhánh MIT là đơn giản nhất. Việc cần làm: sinh file `THIRD_PARTY_NOTICES.txt` bằng `cargo about` (dùng `cargo deny check licenses` trong CI để chặn GPL/AGPL lọt vào), đóng file này trong mọi SDK/plugin, và in ra bằng lệnh `p2pt licenses`.

### 2.3 Nhãn hiệu "p2pt"

Đã có xung đột **cùng lĩnh vực**: thư viện npm `p2pt` ([GitHub subins2000/p2pt](https://github.com/subins2000/p2pt), MIT, khoảng 650 sao, "WebRTC P2P connections"). Có thể chưa đăng ký nhãn hiệu, nhưng gây nhầm lẫn và bất lợi SEO. Tên `p2pt` trên crates.io và PyPI vẫn còn trống. Nên tra [WIPO Global Brand Database](https://branddb.wipo.int), USPTO, EUIPO và [IP Việt Nam (WIPO Publish)](http://wipopublish.ipvietnam.gov.vn) cho nhóm **9 và 42**. Cân nhắc một thương hiệu riêng dễ phân biệt, giữ "p2pt" làm tên kỹ thuật. Nộp đơn tại Cục SHTT trước khi ra mắt, sau đó mở rộng qua Madrid.

### 2.4 Pháp luật Việt Nam

- **Quyền tác giả:** chương trình máy tính được bảo hộ như tác phẩm văn học, dù ở dạng mã nguồn hay mã máy (Luật SHTT, Điều 22). Quyền phát sinh tự động. Đăng ký tại **Cục Bản quyền tác giả** không bắt buộc, nhưng tạo chứng cứ mạnh khi tranh chấp. Hồ sơ gồm: tờ khai, 2 bản sao tác phẩm (mã nguồn hoặc tài liệu), giấy cam đoan hoặc tài liệu chứng minh quyền (hợp đồng giao việc nếu có nhân viên). **Lệ phí 600.000 đ/giấy chứng nhận** ([Thông tư 211/2016/TT-BTC](https://luatvietnam.vn/thue/thong-tu-211-2016-tt-btc-bo-tai-chinh-111335-d1.html)). Thời hạn luật định là 15 ngày làm việc (Điều 52), thực tế thường lâu hơn.
- **Sáng chế phần mềm:** Điều 59 khoản 2 loại "chương trình máy tính" khỏi đối tượng bảo hộ sáng chế ([LuatVietnam](https://luatvietnam.vn/linh-vuc-khac/sang-che-phan-mem-883-91770-article.html)). Chỉ giải pháp kỹ thuật có hiệu quả kỹ thuật mới có thể được xét ([Tilleke](https://www.tilleke.com/wp-content/uploads/2022/05/Computer-Program-Inventions-Vietnam-May2022-VN.pdf)). Với p2pt, không đáng bỏ tiền. Bảo hộ bằng quyền tác giả + bí mật kinh doanh + hợp đồng.
- **EULA:** hợp đồng điện tử và clickwrap có hiệu lực theo Luật Giao dịch điện tử 2023. Điều kiện giao dịch chung phải công khai và dễ tiếp cận (BLDS 2015, Điều 405–406). Nếu bán cho người tiêu dùng thì phải tuân Luật Bảo vệ quyền lợi người tiêu dùng 2023. Khách quốc tế: chọn luật VN và trọng tài VIAC hoặc SIAC. Ở EU, điều khoản cấm dịch ngược **không vô hiệu được** quyền decompile để tương thích (Directive 2009/24/EC, Điều 6 và 8).
- **Thuế và hóa đơn:** sản phẩm và dịch vụ phần mềm thuộc diện **không chịu thuế GTGT** (Luật Thuế GTGT 2024, Điều 5 khoản 21, hiệu lực 01/7/2025; hướng dẫn tại NĐ 181/2025) ([MISA](https://sme.misa.vn/345925/ban-phan-mem-co-chiu-thue-gtgt-khong/)). Riêng "cho thuê license định kỳ" cần xin ý kiến cơ quan thuế để xác nhận là sản phẩm hay dịch vụ phần mềm. Mỗi giao dịch phải xuất **hóa đơn điện tử** (NĐ 123/2020, sửa bởi NĐ 70/2025), kể cả khi bán cho khách nước ngoài. Doanh nghiệp sản xuất phần mềm có thể hưởng ưu đãi thuế TNDN. Với khách quốc tế, dùng Merchant of Record (Paddle, Lemon Squeezy…) để họ lo VAT/sales tax nước ngoài.
- **Dữ liệu cá nhân:** **Luật BVDLCN 91/2025/QH15** và **NĐ 356/2025** (thay NĐ 13/2023) có hiệu lực từ 01/01/2026 ([Hà Tĩnh](https://hatinh.gov.vn/vi/bai-viet/luat-bao-ve-du-lieu-ca-nhan-chinh-thuc-co-hieu-luc-thi-hanh-tu-ngay-112026)). Tài khoản, email, `hostname`, `mid` (machine id) và IP đều là dữ liệu cá nhân. Lưu trên Cloudflare (Mỹ) là chuyển dữ liệu ra nước ngoài, nên cần hồ sơ đánh giá tác động. Khách ở EU thì áp GDPR, cũng coi IP là dữ liệu cá nhân. Cần: chính sách quyền riêng tư, tối thiểu hóa telemetry (băm `hostname`, không gửi IP của người chơi), DPA cho khách B2B, và điều khoản supernode.

### 2.5 Mật mã: rủi ro lớn nhất cần hỏi luật sư

- **VN:** kinh doanh "sản phẩm mật mã dân sự" cần giấy phép của **Ban Cơ yếu Chính phủ**. NĐ 211/2025 (hiệu lực 09/9/2025) liệt kê "sản phẩm sử dụng công nghệ VPN có bảo mật" ([Báo Lâm Đồng](https://baolamdong.vn/danh-muc-san-pham-dich-vu-mat-ma-dan-su-tu-09-09-2025-384850.html)). **NĐ 341/2026** hướng dẫn Luật An ninh mạng về mật mã dân sự, hiệu lực khoảng 01/9/2026. Điều kiện có cả việc phải có ít nhất 2 nhân sự kỹ thuật có bằng đại học chuyên ngành ([VOV](https://vov.vn/kinh-te/quy-dinh-moi-ve-kinh-doanh-xuat-nhap-khau-mat-ma-dan-su-post1329409.vov), [Tạp chí Tòa án](https://tapchitoaan.vn/chinh-phu-ban-hanh-nghi-dinh-quy-dinh-chi-tiet-mot-so-dieu-va-bien-phap-thi-hanh-luat-an-ninh-mang-ve-hoat-dong-mat-ma-dan-su16356.html)). p2pt là tunnel có mã hóa nên **có khả năng bị xếp vào danh mục**, trừ khi được miễn vì "SDK game, mật mã không phải chức năng chính". **Phải xin ý kiến bằng văn bản trước khi bán ở VN.**
- **Mỹ (EAR):** p2pt có xuất xứ VN, crate mã nguồn mở là "publicly available" và chỉ dùng mật mã chuẩn, nên bản thân việc xuất từ VN sang Mỹ gần như không vướng EAR. Tuy vậy, khách Mỹ tái xuất game có nhúng p2pt sẽ cần mã ECCN. Nên công bố tự phân loại **5D992.c (mass market, §740.17(b)(1))**. Lưu ý: từ quy định năm 2021, nghĩa vụ báo cáo hằng năm chỉ còn áp cho *mass-market components* ([Thompson Hine](https://www.thompsonhine.com/publications/bis-eliminates-most-mass-market-encryption-reporting-obligations), [BIS](https://www.bis.gov/learn-support/encryption-controls/license-exception-enc-740.17-b-1)). Khi phát hành iOS/macOS, khai báo export compliance trên App Store.

---

## 3. Mô hình bản quyền

| Đối thủ | Mô hình | Thực thi |
|---|---|---|
| Photon, Unity Relay | Theo CCU + GB, có free tier | Phía server (họ vận hành relay) |
| Tailscale, NetBird | Theo user/tháng | Control plane do vendor vận hành |
| ZeroTier | Theo thiết bị; Enterprise self-host | Controller |
| ngrok | Gói + dùng bao nhiêu trả bấy nhiêu | Edge cloud |
| Unreal | Royalty 5% khi vượt $1M | Hợp đồng, audit |
| Hamachi | Theo mạng/máy, theo năm | Server LogMeIn |

**Nhận xét:** đối thủ nào cũng thực thi giới hạn **tại hạ tầng họ vận hành**. p2pt không có hạ tầng trong đường dữ liệu, nên **chỉ có thể thực thi bằng lease, supernode và hợp đồng**. Đây là điểm yếu cấu trúc cần thiết kế bù.

**Khuyến nghị (giữ khung trong `docs/CONTRACT.md`):**
- Theo **số server + trần CCU/tháng**: dev free, trial 14 ngày, indie, studio, enterprise. Giảm khoảng 2 tháng nếu trả theo năm. Gói **Enterprise self-host authority** (khách giữ khóa ký riêng, hợp đồng có audit) giá cao. Không thu revenue share vì khó kiểm chứng.
- **Thực thi mềm:** lease hết hạn hoặc vượt CCU chỉ **từ chối kết nối mới và cảnh báo**, không bao giờ ngắt người chơi đang trong trận. Grace 7 ngày đã có sẵn.
- **Điều khoản EULA chính:** (1) cấp quyền sử dụng không độc quyền, giới hạn theo gói; (2) cấm dịch ngược, sửa đổi, vượt cơ chế license, "trong phạm vi pháp luật cho phép"; (3) cấm chia sẻ key, cấm resell; (4) quyền audit (khách enterprise báo cáo số server/CCU, vendor được đối chiếu telemetry); (5) đồng ý vai trò supernode (cho opt-out) và xử lý dữ liệu; (6) chấm dứt khi vi phạm, có 15–30 ngày khắc phục; (7) giới hạn trách nhiệm bằng phí 12 tháng; (8) cấm dùng cho mục đích trái pháp luật như né kiểm duyệt hay tấn công; (9) luật áp dụng và trọng tài; (10) attribution cho mã nguồn mở.
- **Xử lý lạm dụng:** theo dõi số `mid` riêng biệt trên mỗi license (đã có), tốc độ đổi `mid`, nhiều IP/ASN dùng chung một key → tự khóa tạm và gửi email. Thu hồi key. Cấp key mới miễn phí khi khách báo bị lộ. Với crack phát tán công khai: gửi DMCA (GitHub, Cloudflare) dựa trên giấy chứng nhận quyền tác giả.

---

## 4. Chống dịch ngược trong thời đại AI

### 4.1 Mô hình đe dọa thực tế

Công cụ AI hỗ trợ RE đã phổ biến: [GhidraMCP](https://github.com/LaurieWired/GhidraMCP) (khoảng 11,7k sao, cho LLM điều khiển Ghidra), [LLM4Decompile](https://github.com/albertan017/LLM4Decompile), cùng IDA/Hex-Rays. Binary Rust vốn khó đọc (monomorphization, inline), nhưng LLM đặt lại tên hàm và giải thích luồng rất tốt. **Coi như:** kẻ tấn công có trình độ trung bình cộng LLM sẽ tìm được hàm kiểm tra lease hoặc hằng số giới hạn trong vài giờ đến vài ngày.

Các điểm crack cụ thể của p2pt hiện tại:
1. **Gói "dev" do core tự thực thi** (1 server / 3 CCU khi không có lease). Chỉ cần patch một phép so sánh là xong. Đây là mục tiêu dễ nhất.
2. **Thay public key Ed25519 nhúng trong binary** bằng khóa của kẻ crack rồi tự ký lease. Không có cách chống tuyệt đối.
3. Patch bỏ kiểm tra `mid` hoặc `exp`, hoặc giả mạo `mid`.
4. Telemetry CCU do client tự báo nên có thể bị làm giả.

### 4.2 Lớp phòng thủ, xếp theo tỷ lệ lợi ích/chi phí

| # | Lớp | Lợi/chi phí | Áp vào p2pt |
|---|---|---|---|
| 1 | **Bí mật để ở server** | Rất cao | Khóa riêng của authority chỉ nằm trong Cloudflare secrets/KMS, không bao giờ vào repo hay binary. Có quy trình xoay khóa (`kid` trong payload) |
| 2 | **Giới hạn thực thi tại mạng vendor hoặc mạng cộng đồng** | Rất cao | Supernode chỉ phản xạ và giới thiệu peer cho node trình lease hợp lệ. Danh sách supernode chính thức do web cấp, kèm chữ ký. Binary bị crack vẫn chạy được ở chế độ LAN/port-forward thủ công, nhưng mất tính năng "chạy xuyên NAT không cần cấu hình", vốn là giá trị bán hàng chính |
| 3 | **Giao thức công khai, bảo mật dựa trên khóa** | Cao (miễn phí) | Noise IK và định dạng lease có thể công bố. Không đặt cược vào việc giấu giao thức |
| 4 | **Rate-limit và phát hiện bất thường ở API lease** | Cao | Giới hạn theo key/IP, cảnh báo khi số `mid` hoặc ASN tăng đột biến, có danh sách thu hồi. Lease ngắn (7 ngày) giúp thu hồi có hiệu lực nhanh |
| 5 | **Watermark từng bản build/khách** | Cao với khách enterprise | Bản SDK enterprise nhúng `lid` (mã hóa, rải ở nhiều chỗ, kể cả trong hằng số "vô hại"). Khi bản crack rò rỉ thì truy được nguồn và kiện theo hợp đồng |
| 6 | **Ký mã** | Cao (chống malware giả mạo, tạo uy tín) | Windows Authenticode (chứng thư OV/EV trên HSM hoặc dịch vụ ký đám mây), macOS Developer ID + notarization ($99/năm), ký bản phát hành trên Linux bằng minisign/cosign |
| 7 | **Strip + LTO + `panic=abort` + `codegen-units=1`** | Trung bình (gần như miễn phí) | `[profile.release] strip=true, lto="fat", codegen-units=1, panic="abort"`. Xóa đường dẫn bằng `--remap-path-prefix`. Ẩn ký hiệu nội bộ, chỉ export `p2pt_*` |
| 8 | **Làm rối chuỗi và luồng điều khiển** | Thấp đến trung bình | [`obfstr`](https://github.com/CasualX/obfstr) (0.4.6, cập nhật 07/2026) cho chuỗi như URL API hay thông báo lỗi license; [`cryptify`](https://github.com/dronavallipranav/rust-obfuscator) (3.2.1, 01/2026). Ở mức LLVM: [eshard/obfuscator-llvm](https://github.com/eshard/obfuscator-llvm) (plugin pass, cập nhật 10/2026, dùng với Rust qua `-Z llvm-plugins`, cần toolchain nightly khớp phiên bản LLVM), [Polaris](https://github.com/za233/Polaris-Obfuscator) (05/2026). Không nên dùng O-LLVM gốc (bỏ dở từ 2023) hay `goldberg`/`debugoff` (dừng 2022). Chỉ áp cho module license, đừng áp cho đường dữ liệu nóng vì tăng ping |
| 9 | **Kiểm tra toàn vẹn / anti-tamper** | Thấp đến trung bình | Rải nhiều điểm kiểm tra lease dư thừa (khởi động, tạo ticket, sau N kết nối), kiểm tra chéo hash của public key ở 2–3 nơi, phản ứng trễ và ngẫu nhiên thay vì lỗi ngay để khó dò. Đừng làm anti-debug gắt: khách game dùng debugger hằng ngày |
| 10 | **Telemetry phát hiện crack** | Trung bình | Báo lỗi xác thực lease, dấu hiệu public key bị thay (supernode nhận lease ký bằng khóa lạ), phiên bản lạ. Tuân luật BVDLCN: tối thiểu hóa và công bố |
| 11 | **Răn đe pháp lý** | Trung bình | Giấy chứng nhận quyền tác giả, EULA, DMCA, watermark làm bằng chứng |

**Kết luận trung thực:** mọi kiểm tra license ở phía client cuối cùng đều bị crack được. Mục tiêu là (a) làm cho **mua rẻ hơn crack** (giá indie thấp, gói dev hào phóng); (b) **gắn giá trị vào thứ chỉ vendor cấp**: mạng supernode có kiểm soát, danh bạ ký số, cập nhật, plugin engine, hỗ trợ; (c) **truy vết được** khách enterprise. Ngân sách nên dồn cho lớp 1, 2, 4, 5, 6. Lớp 8 và 9 chỉ làm vừa phải.

Thay đổi kiến trúc cụ thể nên làm:
- Thêm `kid` và `sn` (quyền dùng supernode) vào payload lease. Supernode kiểm lease bằng public key chính thức trước khi phản xạ cho peer.
- Gói dev không lease: bỏ tính năng supernode và UPnP tự động, chỉ cho LAN/IPv6/port-forward thủ công. Như vậy patch giới hạn CCU cũng không thu được lợi ích lớn.
- Không đặt chỗ kiểm tra license nào trong binding ngôn ngữ cao (C#/Python) vì rất dễ sửa. Mọi kiểm tra nằm trong core Rust.

---

## 5. Khuyến nghị hành động

**Làm ngay**
- [ ] Hỏi luật sư về **giấy phép mật mã dân sự** (NĐ 211/2025, NĐ 341/2026). Đây là rủi ro chặn việc bán hàng ở VN.
- [ ] Đổi hoặc chốt thương hiệu, tra WIPO/USPTO/IP VN (xung đột npm `p2pt`), nộp đơn nhãn hiệu nhóm 9 và 42 tại VN.
- [ ] Khóa riêng authority: chỉ để trong secret store, có `kid`, có quy trình xoay khóa, sao lưu offline.
- [ ] Thêm `cargo deny` và `cargo about` vào CI, sinh `THIRD_PARTY_NOTICES.txt`.
- [ ] Cấu hình release: strip, LTO, remap-path-prefix, chỉ export `p2pt_*`.

**Trước khi ra mắt**
- [ ] Đăng ký quyền tác giả tại Cục Bản quyền tác giả (600.000 đ/chương trình). Ký hợp đồng chuyển giao quyền với mọi người đóng góp.
- [ ] EULA, Chính sách quyền riêng tư, DPA, điều khoản supernode (opt-in hoặc opt-out). Ghi rõ giới hạn: không có relay, IP server lộ cho client được cấp phép.
- [ ] Supernode yêu cầu lease hợp lệ. Gói dev không có supernode.
- [ ] Rate-limit và phát hiện bất thường ở `/api/v1/lease`, có danh sách thu hồi.
- [ ] Ký mã Windows và notarize macOS.
- [ ] Hỏi cơ quan thuế về VAT cho thuê license, thiết lập hóa đơn điện tử, chọn Merchant of Record cho khách quốc tế.
- [ ] Công bố ECCN tự phân loại 5D992.c. Marketing dùng tên WireGuard đúng chính sách nhãn hiệu.
- [ ] Đo tỷ lệ kết nối thật (telemetry `punched/mapped/failures`) và công bố trung thực.

**Sau này**
- [ ] Watermark build cho khách enterprise. Gói enterprise self-host authority có audit.
- [ ] Làm rối có chọn lọc module license (obfstr, eshard obfuscator-llvm).
- [ ] Cân nhắc relay fallback trả phí, tùy chọn, do khách tự host, để cải thiện tỷ lệ kết nối mà không phá lời hứa "không có hạ tầng bên thứ ba".
- [ ] FTO về sáng chế ở Mỹ trước khi thêm kỹ thuật port prediction hoặc symmetric NAT nâng cao.
- [ ] Theo dõi thay đổi giá đối thủ mỗi quý (Photon, Unity, Tailscale đã đổi giá trong năm 2026).
