# Peerova — Đánh giá an ninh (code audit)

Ngày 2026-11-01. Phạm vi: `crates/peerova-core`, `peerova-ffi`, `peerova-cli`, `web/`.
Mã nguồn đang được chỉnh sửa song song nên số dòng có thể lệch; các PoC kiểm chứng đã build lại trên bản mới nhất, nằm ở
`/tmp/claude-1000/-home-vantrong-Documents-tunnel-p2p/0655a381-9342-4507-a81a-593f37cb4d54/scratchpad/secreview/poc/`.

## Bảng tổng hợp

| # | Mức | Vấn đề |
|---|---|---|
| 1 | Critical | Server bị lợi dụng làm nguồn phản xạ lưu lượng tới địa chỉ bên thứ ba |
| 2 | High | Peer đã xác thực làm cạn RAM (số stream vô hạn, reassembly vượt cửa sổ) |
| 3 | High | Tunnel server: số UDP flow / TCP stream không giới hạn |
| 4 | High | Chế độ VPN bỏ qua toàn bộ scope của access token |
| 5 | High | Ghi đè registry supernode chỉ bằng public key |
| 6 | High | Web rendezvous: biết `sid` là giả được server, nuốt request và lộ IP client |
| 7 | Medium | SDK/FFI: bộ đệm ghép message reliable không giới hạn |
| 8 | Medium | VPN: chống giả nguồn chỉ cho IPv4; prefix do server cấp không kiểm tra |
| 9 | Medium | Signup không xác minh email; quyền admin gắn theo email |
| 10 | Medium | Node deploy: Host header poisoning ở link reset; XFF lấy sai entry |
| 11 | Medium | Lobby upsert không xác thực chủ app |
| 12 | Medium | Chặn IP dựa trên nguồn UDP (giả được) |
| 13 | Low | Nhóm lỗi nhỏ (xem cuối) |

---

## 1. Critical — Phản xạ/khuếch đại lưu lượng qua cơ chế đục NAT
**Vị trí:** `engine.rs` `on_control` nhánh `IntroReq` (gửi tới chính server), nhánh `Intro`, `Cmd::RdvClients`, và `start_punch_ex` + vòng `tick`.

**Bản chất:** Khi nhận `IntroReq`, server tạo "punch job" bắn gói `T_PUNCH` tới toàn bộ candidate trong gói và tới địa chỉ nguồn, lặp mỗi 150 ms trong 15 giây; với IPv4 public còn nhân thêm do `predict_ports`. Điều kiện kích hoạt chỉ là `proof = mac1(server_pub, session||server_id)`, trong khi `server_pub` nằm công khai trong mọi ticket. Địa chỉ candidate và địa chỉ nguồn UDP đều không được xác thực là của người gửi. Hệ quả: một gói điều khiển nhỏ khiến server phát ra lượng gói lớn hơn nhiều, hướng tới các địa chỉ do bên gửi chỉ định → server trở thành bộ khuếch đại/ phản xạ tới nạn nhân thứ ba. Supernode (`Intro` → punch) và `RdvClients` có cùng dạng vấn đề: địa chỉ "client" đến từ dữ liệu không được xác minh quyền sở hữu.

**PoC:** `intro_amp` — một gói IntroReq 173 byte làm server gửi ~1500 gói tới 16 socket "nạn nhân" trong 16 s.

**Khắc phục:**
- Chỉ punch tới một địa chỉ sau khi đã nhận gói đến từ chính địa chỉ đó trong cùng phiên (yêu cầu round-trip, chống spoof) — tương tự address-validation của QUIC.
- Giới hạn tổng số punch job đồng thời và tổng pps phát ra cho mỗi IP nguồn; chặn candidate trùng IP nguồn / dải mạng.
- Supernode chỉ nên chuyển tiếp địa chỉ reflexive mà nó *tự quan sát* (địa chỉ gói đến), không tin candidate tự khai khi dùng để bắn gói.

## 2. High — Peer đã xác thực làm cạn bộ nhớ
**Vị trí:** `conn.rs` `RecvStream::on_data` / `on_packet` (map `recv`, `pending`), cửa sổ `RECV_WINDOW = 1<<20` chỉ áp cho **từng** stream và chỉ theo offset, không giới hạn **tổng** bộ nhớ hay **số** stream.

**Bản chất:** Peer gửi các STREAM frame lệch thứ tự (offset > read, byte 0 thiếu) được giữ trong `pending` vượt xa 1 MiB; và mở số lượng stream id tuỳ ý, mỗi stream tạo `RecvStream`/`SendStream` không được giải phóng. Không có trần tổng.

**PoC:** `conn_mem` — [1] 200k frame lệch cho một stream → RSS +245 MiB (cửa sổ danh nghĩa 1 MiB); [2] 300k stream → +237 MiB. Ngoài ra `off = u64::MAX-100` + len gây `attempt to add with overflow` (`on_data: let end = off + data.len()`), panic ở build có overflow-checks, và ở release thì `end` wrap → qua được kiểm tra cửa sổ.

**Khắc phục:** Giới hạn tổng byte đang đệm toàn connection; giới hạn số stream đồng thời mỗi peer; dùng `checked_add`/`saturating_add` cho `off + len`, bỏ frame khi `end` vượt trần. Giới hạn kích thước `pending` theo cửa sổ thực nhận.

## 3. High — Tunnel server không giới hạn flow/stream
**Vị trí:** `tunnel.rs` `serve_audited`: `flows.entry(key)` tạo `UdpSocket` + thread cho mỗi `(conn, flow)` mới; mỗi `StreamOpened` tạo một `Ctx`. `Defrag.parts` có trần 4096 nhưng flow/stream thì không.

**Bản chất:** Một client hợp lệ (sau khi qua ACL) tăng `flow u32` hoặc mở stream id liên tục → server mở vô số socket UDP + thread tới dịch vụ nội bộ, cạn fd/thread. `gc_flows` chỉ dọn sau `UDP_IDLE = 120s`.

**Khắc phục:** Trần số flow và số stream mở đồng thời cho mỗi conn; từ chối/relay-reset khi vượt; rút ngắn idle. Cân nhắc giới hạn pps mở-flow.

## 4. High — VPN bỏ qua ACL scope
**Vị trí:** `vpn.rs` `serve`. `tunnel::serve_audited` kiểm tra `scope_allows(scopes, …)` theo port, nhưng `vpn::serve` chuyển tiếp mọi datagram IP tới TUN và giữa các peer mà không hề đọc `scopes` từ `EngineEvent::Connected`.

**Bản chất:** Access token enterprise giới hạn `ports:["tcp:443"]` trở nên vô nghĩa ở chế độ VPN: client chạm được mọi IP/port mà interface TUN định tuyến tới (toàn bộ L3). Đây là leo thang phạm vi so với cam kết của token.

**Khắc phục:** Hoặc chặn VPN khi access là `Signed` với scope port, hoặc áp scope ở tầng L4 (lọc theo protocol/port trong gói IP). Ít nhất phải tài liệu hoá rõ VPN = toàn quyền L3.

## 5. High — Chiếm registry supernode bằng public key
**Vị trí:** `engine.rs` `on_control` nhánh `Register`. Khi không bật `authority`, `Register{server_pub, lease}` được chấp nhận không cần chứng minh sở hữu private key; registry giữ tối đa 8 instance/khóa, cũ nhất bị loại (`list.remove(0)`).

**Bản chất:** Biết `server_pub` (công khai trong ticket), kẻ tấn công đăng ký 8 địa chỉ của mình dưới id server đó, đẩy server thật ra. Client dùng ticket private sẽ chỉ được supernode giới thiệu tới địa chỉ của attacker (DoS định tuyến; cũng có thể biến thành điểm hút lưu lượng). Khi bật `authority` thì lease bảo vệ được, nhưng mặc định supernode public không bật.

**PoC:** `register_hijack` — sau 8 gói Register, 100% IntroResp trả về địa chỉ attacker, 0% địa chỉ server thật.

**Khắc phục:** Bắt buộc chứng minh sở hữu khóa khi Register (vd Register là gói đã ký / hoặc đi qua handshake Noise với server_pub), và/hoặc chỉ ghi nhận địa chỉ nguồn đã được xác nhận round-trip. Không loại instance cũ chỉ vì có bản ghi mới cùng khóa từ IP khác.

## 6. High — Web rendezvous thiếu xác thực chủ `sid`
**Vị trí:** `web/src/app.js` `/api/v1/rdv/poll`, `/api/v1/rdv/connect`. `sid` = public key server (công khai). `poll` cập nhật bản ghi server theo `sid` và **nuốt** (DELETE) mọi pending request của `sid`. `lease` chỉ dùng để nới rate limit, không bắt buộc.

**Bản chất:** Ai biết `sid` (tức ai có ticket) cũng có thể: (a) ghi đè địa chỉ server công bố tại rendezvous → chỉ client tới địa chỉ sai; (b) poll liên tục để nuốt request của client thật (từ chối dịch vụ ghép nối); (c) thu thập IP:port của mọi client đang chờ (`connect` trả về IP server, `poll` trả về IP client). Không có liên kết giữa `sid` và lease đã trả tiền ở nhánh dev.

**Khắc phục:** Yêu cầu chứng minh sở hữu private key của `sid` khi *poll* (ký thử thách), không chỉ khi lease; hoặc buộc lease hợp lệ gắn `spk == sid` mới được công bố/nuốt request. Giới hạn chặt số server-record theo sid+lease.

## 7. Medium — FFI/SDK: bộ đệm ghép message không giới hạn
**Vị trí:** `peerova-ffi/src/lib.rs` `handle`: `reasm.entry((conn,id)).or_default()` rồi `buf.extend_from_slice`. Length-prefix 4 byte do peer điều khiển; nếu peer gửi prefix lớn và không bao giờ đủ, `buf` phình vô hạn (một lần nữa không có trần tổng). C ABI phần còn lại xử lý null/len hợp lý (`peerova_send` chặn `data.is_null()` khi len>0, `catch_unwind` bọc mọi hàm).

**Khắc phục:** Giới hạn độ dài message reliable (ví dụ vài MiB) và tổng `reasm` theo conn; reset stream khi vượt.

## 8. Medium — VPN chống giả nguồn chỉ IPv4; prefix server cấp không kiểm tra
**Vị trí:** `vpn.rs` `serve`: `src_ok` chỉ so khớp `pkt[12..16]` cho IPv4 (`pkt[0]>>4==4`). Gói IPv6 hoặc gói IPv4 khác (ví dụ tới IP ngoài subnet) rơi vào nhánh `w.write_all(pkt)` ghi thẳng vào TUN của server. `client`: `prefix` lấy từ chuỗi do server gửi, parse không giới hạn; `dst_v4`/route không ràng buộc client chỉ gửi trong subnet.

**Bản chất:** Client có thể bơm gói có IP đích tuỳ ý vào ngăn xếp mạng của máy chủ qua TUN (chạm dịch vụ loopback/LAN của server); server ác ý có thể cấp CIDR bất thường cho client. (Kiểm tra `src` chỉ bảo vệ chuyển tiếp peer↔peer, không bảo vệ hướng ghi vào TUN.)

**Khắc phục:** Bỏ gói có src/dst ngoài subnet trước khi ghi TUN; chỉ ghi TUN khi dst thuộc dải server/không thuộc peer; kiểm tra prefix cấp cho client nằm trong khoảng cho phép.

## 9. Medium — Signup không xác minh email; admin theo email
**Vị trí:** `app.js` `/auth/signup` (tạo tài khoản + trial ngay, không bước verify), `isAdminEmail` so khớp `ADMIN_EMAILS`. Nếu luồng đổi email admin (`/admin/users/:id` action update) hoặc nhà cung cấp mail cho phép đặt email tuỳ ý mà không xác minh, quyền admin gắn thuần vào chuỗi email là rủi ro. Chưa thấy đường leo thang trực tiếp, nhưng thiếu email verification tạo bề mặt lạm dụng trial và giả danh.

**Khắc phục:** Thêm xác minh email; không cấp quyền admin chỉ dựa trên khớp chuỗi email có thể do người dùng tự đặt.

## 10. Medium — Node deploy: Host poisoning & XFF
**Vị trí:** `server/node-server.mjs`: `host = req.headers.host`, `siteUrl` fallback `c.url.origin` → link reset mật khẩu (`/reset?token=…`) dựng theo Host do client gửi nếu `SITE_URL` trống. Kẻ tấn công kích hoạt "forgot" cho nạn nhân với Host của mình → token reset gửi tới hộp thư nạn nhân nhưng link trỏ về domain attacker (đánh cắp token nếu nạn nhân bấm). Ngoài ra XFF lấy entry trái nhất khi `TRUST_PROXY=1` — nếu proxy nối thêm thay vì ghi đè, client giả được IP (bỏ qua rate-limit theo IP).

**Khắc phục:** Luôn đặt `SITE_URL` và dựng link tuyệt đối từ nó, bỏ qua Host header; với XFF lấy entry phải-nhất đáng tin theo số hop proxy.

## 11. Medium — Lobby upsert không xác thực chủ app
**Vị trí:** `app.js` `/lobby/upsert`: chỉ kiểm tra `app` khớp regex và tồn tại project id; không kiểm tra người gọi sở hữu project hay có lease. Bất kỳ ai cũng tạo được phòng public dưới `prj_...` của người khác, bơm danh sách phòng giả (các trường name/mode/tags đã được `cleanText` loại ký tự điều khiển nên XSS lưu trữ bị chặn ở tầng này — front-end dùng `textContent`/`el`, không `innerHTML`, nên XSS khó; vấn đề còn lại là spam/giả mạo directory).

**Khắc phục:** Buộc lease hợp lệ (hoặc secret chủ phòng) gắn với project cho upsert; rate-limit theo project.

## 12. Medium — Chặn IP theo nguồn UDP giả được
**Vị trí:** `engine.rs` `bad_packet`/`blocked`. Chặn/ cấm dựa trên `from.ip()` của gói UDP (dễ spoof). Kẻ tấn công spoof nguồn = IP nạn nhân có thể khiến server tự chặn lưu lượng hợp lệ của nạn nhân (trừ khi nạn nhân đang là peer đã xác thực — điều kiện đã có giúp giảm nhẹ). Ngược lại attacker đổi IP nguồn liên tục để né chặn.

**Khắc phục:** Chỉ nên dùng block như biện pháp nhẹ; không để nó ảnh hưởng tới quá trình bắt tay của IP khác. Ưu tiên giới hạn tài nguyên theo công việc thay vì theo IP nguồn.

---

## 13. Low / quan sát
- `license.rs verify_for_supernode`: lease **không** gắn `spk` thì `map_or(true, …)` → chấp nhận cho mọi server_pub. Lease trả tiền không gắn khóa có thể dùng lại cho nhiều khóa server. Nên buộc `spk` ở nhánh supernode.
- `util::random_u64` (session id, idx) là non-crypto (RandomState); `engine` còn dùng `crypto::random_u32` cho idx/nonce nên ổn, nhưng session id rendezvous đoán được phần nào. Dùng CSPRNG cho mọi giá trị chống đoán.
- `json.rs num()`: chấp nhận chuỗi số lạ (nhiều dấu `+/-/e`) rồi `parse::<f64>` — không panic, nhưng `u64` ép từ f64 mất chính xác với số lớn; lease dùng số nhỏ nên thấp.
- `wire.rs`/`ticket.rs`/`conn.rs decode`: đã rà, dùng `get`/`try_into`/`checked_add`, không thấy index panic trên input mạng (đã có fuzz). `Frame::Ack` len là u32 → `s.checked_add(len)` có xử lý.
- `payos` webhook: chữ ký HMAC so sánh constant-time, có dedupe theo event — tốt. `sepay` so khớp nội dung regex `PEEROVA\s*(\d{8})` ⇒ cần đảm bảo `order code` không va chạm không gian 8 chữ số với tham chiếu khác; hiện có kiểm `underpaid`/`currency_mismatch`, hợp lý.
- `conn.rs` replay/Ack, `crypto.rs` Noise IK, `access.rs` Ed25519 verify, so sánh token bằng `ct_eq`: nhìn chắc chắn; nonce = packet counter không tái sử dụng trong một phiên.
- FFI `peerova_ticket`/`peerova_room_code`: trả `need` để caller cấp buffer; copy có kiểm `buf_len >= need` — an toàn.

## Điểm làm tốt
- Noise IK + mac1 "dark server", anti-replay window, so sánh constant-time, dedupe handshake.
- Parser mạng viết phòng thủ (bounds-checked), có bộ fuzz std-only.
- Web: CSRF (custom header + origin + sec-fetch), PBKDF2 + pepper + nâng cấp hash, timing-equalised login/signup, TOTP có khoá thử sai, webhook ký + idempotent, lobby/connect-code tra cứu đồng nhất chống dò, front-end dựng DOM bằng `textContent` (không innerHTML) nên XSS lưu trữ khó, `_headers` có CSP chặt.
- IDOR cơ bản được chặn bằng `ownedLicense`/so `user_id`.
