# Pháp lý tại Việt Nam cho p2pt: phân tích sâu + hướng dẫn nhãn hiệu & bản quyền

> **Cập nhật:** sản phẩm đã đổi tên từ "p2pt" thành **Peerova** (CLI `peerova`, thư viện `libpeerova`). Các chỗ nhắc tới "p2pt" bên dưới là tên cũ.


> Tra cứu ngày 01/11/2026. Đây **không phải tư vấn pháp lý**. Các mục có ⚠️ là chỗ nguồn công khai chưa thống nhất hoặc chưa đọc được toàn văn, **phải để luật sư xác minh**. Ngân sách hợp lý cho một buổi tư vấn chuyên sâu (mật mã + SHTT + thuế): vài triệu tới vài chục triệu đồng. So với mức phạt có thể lên tới 180 triệu đồng (mục 1), khoản này đáng chi.

## 0. Bảng tổng quan (đã chỉnh lại)

| # | Vấn đề | Mức rủi ro | Tình trạng | Việc phải làm |
|---|---|---|---|---|
| 1 | **Giấy phép mật mã dân sự** | 🔴 Cao | p2pt là **phần mềm mã hoá** (Noise/ChaCha20, tương tự VPN). Kinh doanh sản phẩm thuộc danh mục mà không có phép có thể bị phạt **tới 180 triệu đồng** | Luật sư đối chiếu Phụ lục I NĐ 341/2026 (hiệu lực 01/9/2026). Nếu thuộc danh mục: xin giấy phép Ban Cơ yếu **trước khi bán ở VN** |
| 2 | Tên thương hiệu | 🟠 Trung bình | "p2pt" trùng một thư viện npm P2P. Nhãn hiệu ở VN cấp cho **người nộp đơn trước** | Chọn tên mới, tra cứu, **nộp đơn ngay** (mục 5) |
| 3 | Bảo hộ mã nguồn | 🟢 Thấp, dễ làm | Phần mềm tự động được bảo hộ **quyền tác giả** khi tạo ra. Đăng ký giúp có **bằng chứng** khi tranh chấp | Đăng ký chương trình máy tính tại Cục Bản quyền (mục 6) |
| 4 | Bằng sáng chế của người khác | 🟢 Thấp | Bằng STUN/ICE chính đã hết hạn. Noise là public domain | Không cần làm gì thêm. Giữ tài liệu ghi lại nguồn gốc thiết kế |
| 5 | Giấy phép mã nguồn mở | 🟢 Thấp | Toàn MIT/Apache/BSD, không có GPL | Luôn kèm `THIRD_PARTY_NOTICES.txt`, thêm `cargo deny` vào CI |
| 6 | Dữ liệu cá nhân | 🟠 Trung bình | Luật BVDLCN 91/2025 (hiệu lực 01/01/2026). Machine ID, IP, hostname, email đều là dữ liệu cá nhân. Lưu trên Cloudflare là chuyển ra nước ngoài | Thu thập tối thiểu (mục 7). Chính sách quyền riêng tư. Hồ sơ đánh giá tác động |
| 7 | Thuế & hoá đơn | 🟢 Thấp, cần đúng thủ tục | Có ưu đãi lớn cho doanh nghiệp phần mềm/công nghệ số | Thành lập công ty đúng ngành nghề, dùng hoá đơn điện tử (mục 8) |
| 8 | Hợp đồng với khách | 🟠 Trung bình | Chưa có EULA hay SLA | EULA + điều khoản dịch vụ + DPA cho doanh nghiệp (mục 9) |

---

## 1. Giấy phép mật mã dân sự: việc quan trọng nhất

**Căn cứ:**
- Luật An toàn thông tin mạng 2015 (Điều 31, điều kiện kinh doanh)
- **NĐ 211/2025/NĐ-CP** (hiệu lực 09/9/2025): kinh doanh, xuất nhập khẩu mật mã dân sự; bổ sung Điều 93a vào NĐ 15/2020 để xử phạt việc kinh doanh không phép
- **NĐ 341/2026/NĐ-CP** (hiệu lực 01/9/2026): hướng dẫn **Luật An ninh mạng** về hoạt động mật mã dân sự

**Vì sao p2pt có thể thuộc diện:** "Sản phẩm mật mã dân sự" gồm cả **phần mềm** dùng mật mã để bảo vệ thông tin không thuộc bí mật nhà nước. Danh mục xuất nhập khẩu theo giấy phép có nhắc tới **sản phẩm VPN có bảo mật (IPSec VPN, TLS VPN)**. p2pt là tunnel mã hoá đầu-cuối, về bản chất gần với VPN.

⚠️ **Chưa xác định được:** p2pt có nằm trong **Phụ lục I** (danh mục kinh doanh phải có phép) của NĐ 341/2026 hay không. Nghị định có điều khoản riêng cho sản phẩm **lưỡng dụng** (vừa có chức năng mật mã vừa có chức năng khác). Đây là câu hỏi số 1 cần đưa cho luật sư.

**Nếu thuộc diện, điều kiện cấp phép:**
- Doanh nghiệp đáp ứng khoản 2 Điều 31 Luật ATTTM
- Tối thiểu **2 cán bộ kỹ thuật có bằng đại học trở lên** (CNTT, ATTT, toán hoặc điện tử viễn thông)
- Phương án kỹ thuật, phương án bảo mật và hồ sơ sản phẩm nộp cho **Ban Cơ yếu Chính phủ**
- Xuất nhập khẩu sản phẩm mật mã dân sự cần giấy phép riêng, thời hạn 3 năm

**Các hướng xử lý để hỏi luật sư:**
1. **Xin giấy phép**: an toàn nhất nếu bán ở VN, nhưng cần nhân sự và thời gian.
2. Hỏi xem cấu hình phân phối nào giảm được phạm vi. Ví dụ: chỉ bán **SDK cho nhà phát triển** và khách tự tích hợp, hay bán **dịch vụ**. Đừng tự kết luận.
3. Giai đoạn đầu chỉ bán cho **thị trường quốc tế**, trong lúc làm thủ tục ở VN. Vẫn phải hỏi luật sư, vì cách này còn liên quan tới pháp nhân bán hàng và quy định xuất khẩu.

**Không nên:** quảng cáo "VPN" hay "mã hoá cấp quân sự" ở thị trường VN trước khi có ý kiến pháp lý.

---

## 2. Quyền tác giả phần mềm: bạn đang có gì

- Chương trình máy tính được bảo hộ như **tác phẩm văn học**, **tự động** kể từ khi được tạo ra và thể hiện dưới dạng vật chất. Không bắt buộc đăng ký.
- **Đăng ký** cho ra Giấy chứng nhận. Khi tranh chấp, bên kia phải chứng minh ngược lại, nên việc đăng ký rất có lợi khi gặp đối thủ sao chép hoặc nhân viên cũ mang code đi.
- **Phần mềm không được cấp bằng sáng chế ở VN** (phương pháp thuần tuý toán học/chương trình máy tính bị loại trừ). Thuật toán chống nghẽn hay giao thức chỉ được bảo vệ qua **bí mật kinh doanh** và **quyền tác giả** đối với mã nguồn.
- **Quyền sở hữu khi có nhân viên hoặc thuê ngoài:** phải có **hợp đồng ghi rõ chuyển giao quyền tài sản** đối với mã nguồn cho công ty. Nếu không, tranh chấp quyền sở hữu rất dễ xảy ra.
- **Bí mật kinh doanh** (khoá ký license, quy trình chống crack): bảo vệ bằng NDA với nhân viên và đối tác, phân quyền truy cập, ghi log truy cập.

---

## 3. Nhãn hiệu: vì sao phải làm sớm

- VN áp dụng nguyên tắc **nộp đơn đầu tiên** (first-to-file). Ai nộp trước người đó có quyền, kể cả khi bạn dùng tên trước.
- Nhãn hiệu chỉ có hiệu lực **trong lãnh thổ VN**. Muốn bảo hộ ở nước khác thì nộp qua **Hệ thống Madrid** (dựa trên đơn VN) hoặc nộp từng nước.
- Hiệu lực văn bằng **10 năm**, gia hạn được không giới hạn.
- **Tên "p2pt" yếu ở hai điểm:** đã có thư viện cùng lĩnh vực dùng tên này, và chuỗi viết tắt mô tả ("P2P tunnel") có **khả năng phân biệt thấp**, dễ bị từ chối. **Khuyến nghị: chọn tên mới tự đặt**, có thể kèm logo (đã có bộ nhận diện trong `web/public/brand/`, chỉ cần đổi chữ).

**Tiêu chí chọn tên:**
- Từ tự tạo, không mô tả trực tiếp sản phẩm (tránh kiểu "P2P", "Tunnel", "Net", "Connect" đứng một mình)
- Dễ đọc bằng cả tiếng Việt lẫn tiếng Anh
- Còn trống tên miền `.com`/`.vn`/`.io`, tên crate/npm/PyPI, tên GitHub org
- Tra cứu sơ bộ không thấy trùng hoặc gần giống trong **nhóm 9 và 42**

---

## 4. Chọn nhóm (Nice) cho nhãn hiệu

| Nhóm | Nội dung nên khai | Bắt buộc? |
|---|---|---|
| **9** | Phần mềm máy tính tải xuống được; bộ công cụ phát triển phần mềm (SDK); phần mềm kết nối mạng ngang hàng; phần mềm bảo mật kết nối | ✅ |
| **42** | Phần mềm như một dịch vụ (SaaS); thiết kế, phát triển phần mềm; cho thuê phần mềm; dịch vụ cấp phép sử dụng phần mềm (nếu Cục chấp nhận diễn đạt) | ✅ |
| 38 | Dịch vụ truyền dữ liệu / cung cấp kết nối viễn thông | Tuỳ chọn, cân nhắc nếu quảng bá như dịch vụ kết nối |
| 45 | Cấp phép quyền sở hữu trí tuệ | Tuỳ chọn |

Mỗi nhóm khai **tối đa 6 sản phẩm/dịch vụ** để giữ mức phí cơ bản (vượt thì tính thêm phí trên mỗi mục).

---

## 5. Hướng dẫn đăng ký nhãn hiệu tại Cục Sở hữu trí tuệ (IP Vietnam)

**Bước 1: Tra cứu trước khi nộp (tự làm, miễn phí)**
- Tra cứu trên cơ sở dữ liệu của Cục SHTT (IP Lib / WIPO Global Brand Database), lọc VN, nhóm 9 và 42
- Tra cả tên gần giống về cách đọc (ví dụ "p2pt" với "p2p.t", "pi-pi-ti")
- Nếu muốn chắc hơn: nộp **dịch vụ tra cứu chính thức** của Cục (có phí) hoặc thuê đại diện SHTT

**Bước 2: Chuẩn bị hồ sơ**
- Tờ khai đăng ký nhãn hiệu (mẫu theo Thông tư 23/2023/TT-BKHCN)
- Mẫu nhãn hiệu (ảnh logo hoặc chữ, đúng kích thước quy định)
- Danh mục sản phẩm/dịch vụ theo nhóm Nice (mục 4)
- Chứng từ nộp phí
- Giấy uỷ quyền, nếu nộp qua đại diện SHTT

**Bước 3: Nộp đơn**
- **Trực tuyến:** qua cổng dịch vụ công trực tuyến của Cục SHTT, cần chữ ký số
- **Hoặc** nộp trực tiếp / qua bưu điện tại Cục SHTT (Hà Nội) hoặc văn phòng đại diện TP.HCM / Đà Nẵng
- Chủ đơn: **công ty** (khuyến nghị, để tài sản thuộc pháp nhân) hoặc cá nhân
- **Ngày nộp đơn = ngày ưu tiên.** Đây là mốc giữ quyền, cũng là mốc để nộp quốc tế qua Madrid với quyền ưu tiên **6 tháng**

**Bước 4: Quy trình xử lý (theo luật)**

| Giai đoạn | Thời hạn luật định |
|---|---|
| Thẩm định hình thức | khoảng 1 tháng |
| Công bố đơn trên Công báo SHCN | trong 2 tháng sau khi đơn hợp lệ |
| Thẩm định nội dung | **9 tháng** kể từ ngày công bố |
| Nộp phí cấp văn bằng | trong 3 tháng sau thông báo dự định cấp |
| **Tổng theo luật** | **khoảng 12–13 tháng** |
| Thực tế | ⚠️ thường **18–24 tháng** do tồn đọng |

Trong thời gian này nhãn hiệu đã có "ngày ưu tiên", nên bạn **vẫn dùng tên bình thường**.

**Bước 5: Lệ phí nhà nước**
- Biểu phí gốc: Thông tư 263/2016/TT-BTC
- **Giảm 50%** một số khoản từ 01/7/2025 đến 31/12/2026 (Thông tư 64/2025/TT-BTC)
- Thông tin công bố cho biết **miễn lệ phí nộp đơn và lệ phí cấp văn bằng** khi làm qua **VNeID**
- ⚠️ Tổng lệ phí cho 1 nhóm (≤ 6 sản phẩm) được các nguồn nêu khoảng **1,2–1,3 triệu đồng** trong giai đoạn giảm phí, mỗi nhóm thêm tốn thêm vài trăm nghìn. Các nguồn chưa khớp nhau, hãy xem biểu phí trên cổng của Cục khi nộp.
- Nếu thuê đại diện SHTT: thêm phí dịch vụ, thường vài triệu đồng mỗi nhóm

**Bước 6: Sau khi có văn bằng**
- Dùng ký hiệu ® (trước khi có văn bằng thì dùng ™)
- Theo dõi Công báo để **phản đối** đơn của người khác giống nhãn của bạn
- Gia hạn mỗi 10 năm
- Nộp **Madrid** để mở rộng ra thị trường quốc tế (Mỹ, EU, Nhật, Hàn, ASEAN…)

---

## 6. Hướng dẫn đăng ký quyền tác giả chương trình máy tính

**Cơ quan:** Cục Bản quyền tác giả (Bộ VHTTDL), Hà Nội / TP.HCM / Đà Nẵng. Có thể nộp trực tuyến qua cổng dịch vụ công.

**Hồ sơ** (theo NĐ 17/2023/NĐ-CP và Thông tư 08/2023/TT-BVHTTDL):
1. **Tờ khai đăng ký quyền tác giả** (mẫu dành cho chương trình máy tính trong Thông tư 08/2023)
2. **Bản sao tác phẩm:** thường là đĩa hoặc USB chứa chương trình kèm **bản in một phần mã nguồn** và tài liệu mô tả. Mẹo: in các file lõi như `crypto.rs`, `conn.rs`, `engine.rs`, chèn tên tác giả và năm ở đầu file
3. **Giấy tờ chứng minh quyền nộp đơn:**
   - Nếu chủ sở hữu là **công ty**: quyết định giao nhiệm vụ, hoặc hợp đồng lao động/thuê việc có điều khoản chuyển giao quyền
   - Cam đoan của tác giả (các lập trình viên) rằng tác phẩm do họ tự sáng tạo
4. Giấy uỷ quyền, nếu nộp qua đại diện
5. Giấy tờ pháp lý của chủ sở hữu (ĐKKD hoặc CCCD)

**Lệ phí và thời gian:**
- ⚠️ Mức thường được nêu là **600.000 đồng** cho chương trình máy tính (Thông tư 211/2016/TT-BTC). Một số nguồn ghi mức khác theo đối tượng, nên kiểm tra biểu phí hiện hành
- Thời hạn cấp: **15 ngày làm việc** từ khi hồ sơ hợp lệ. Thực tế có thể lâu hơn nếu phải bổ sung

**Lưu ý chiến lược:**
- Đăng ký cho **phiên bản chính**. Các phiên bản lớn về sau có thể đăng ký như tác phẩm phái sinh nếu cần
- **Không cần nộp toàn bộ mã nguồn.** Một phần đủ để nhận diện là được, tránh lộ bí mật kinh doanh
- Đăng ký riêng cho **website/dashboard** nếu muốn bảo hộ thêm giao diện và mã web

---

## 7. Dữ liệu cá nhân: những gì p2pt đang thu thập và nên giảm

**Căn cứ:** Luật Bảo vệ dữ liệu cá nhân 2025 (số 91/2025/QH15, hiệu lực 01/01/2026) và nghị định hướng dẫn (NĐ 356/2025).

| Dữ liệu | Ở đâu | Đánh giá | Khuyến nghị kỹ thuật |
|---|---|---|---|
| Email, mật khẩu (băm), tên | Website | Dữ liệu cá nhân cơ bản | Giữ |
| Machine ID | Lease request | Đã băm BLAKE2s có salt nên rủi ro thấp, nhưng vẫn có thể bị coi là định danh | Giữ (cần cho chống chia sẻ license) |
| **Hostname** | Lease request | Thường chứa tên người, ví dụ "Vantrong-PC". **Không cần thiết** | **Bỏ khỏi request**, hoặc chỉ gửi bản băm |
| IP của server | Log Cloudflare, danh sách supernode | Dữ liệu cá nhân nếu khách là cá nhân | Giữ thời gian ngắn (supernode 30 phút) |
| Telemetry | Tuỳ chọn | Chỉ gửi số tổng hợp | Opt-in, ghi rõ trong Privacy Policy |
| **Dữ liệu người dùng cuối** (traffic game/app) | **Không bao giờ đi qua hạ tầng p2pt** | ✅ Điểm mạnh lớn về tuân thủ | Nêu rõ trong DPA và tài liệu bán hàng |

**Việc cần làm:**
- **Chính sách quyền riêng tư**: thông báo mục đích, loại dữ liệu, thời gian lưu và quyền của chủ thể. Website có trang này nhưng mới ở mức sơ bộ
- **Đồng ý** rõ ràng khi đăng ký tài khoản (checkbox riêng, không gộp)
- **Hồ sơ đánh giá tác động xử lý dữ liệu cá nhân**, và **chuyển dữ liệu ra nước ngoài** vì D1/Cloudflare đặt ngoài VN. Hoặc chuyển website sang VPS tại VN để tránh phần chuyển ra nước ngoài (thiết kế đã sẵn sàng để chuyển)
- Quy trình xử lý yêu cầu xoá dữ liệu và thông báo vi phạm dữ liệu

---

## 8. Doanh nghiệp, thuế, hoá đơn

- **Thành lập công ty** (TNHH là đủ). Ngành nghề: **6201** lập trình máy vi tính, **6202** tư vấn và quản trị hệ thống, **5820** xuất bản phần mềm, **6311/6312** xử lý dữ liệu và cổng thông tin. Nếu được cấp phép mật mã thì bổ sung ngành tương ứng
- **Luật Công nghiệp công nghệ số 2025** (71/2025/QH15, hiệu lực 01/01/2026) cùng **Luật Thuế TNDN 2025**: các nguồn cho biết doanh nghiệp công nghệ số/phần mềm đủ điều kiện có thể hưởng **thuế suất 10% trong 15 năm, miễn thuế tối đa 4 năm, giảm 50% tối đa 9 năm tiếp theo**. ⚠️ Điều kiện cụ thể cần kế toán thuế xác nhận (chứng nhận sản phẩm phần mềm, tỷ lệ doanh thu…)
- **VAT:** sản phẩm và dịch vụ phần mềm thường thuộc diện **không chịu thuế GTGT**. ⚠️ Cần xác nhận "cho thuê license theo tháng" được coi là **dịch vụ phần mềm**, không phải dịch vụ khác
- **Hoá đơn điện tử:** bắt buộc cho mọi giao dịch, kể cả bán cho khách nước ngoài qua Lemon Squeezy/Paddle (đối soát theo merchant of record)
- **Doanh thu qua Lemon Squeezy/Paddle:** họ là merchant of record nên tự lo VAT/sales tax ở nước ngoài. Công ty bạn nhận khoản thanh toán và hạch toán theo hợp đồng với họ

---

## 9. Bộ hợp đồng nên có trước khi bán

| Văn bản | Nội dung chính |
|---|---|
| **EULA / Điều khoản cấp phép** | Cấp quyền sử dụng (không chuyển nhượng); giới hạn theo gói (server/CCU); **cấm dịch ngược, bẻ khoá, vượt giới hạn license** (phù hợp ngoại lệ luật định); quyền ngừng license khi vi phạm; giới hạn trách nhiệm; luật áp dụng là Việt Nam; giải quyết tranh chấp (toà án hoặc VIAC) |
| **Điều khoản sử dụng website** | Tài khoản, thanh toán, hoàn tiền, cấm lạm dụng |
| **Chính sách quyền riêng tư** | Mục 7 |
| **Đồng ý tham gia mạng supernode** | Server licensed có thể giới thiệu địa chỉ cho server khác (không chuyển dữ liệu). Cho phép tắt (`--no-auto-supernode`) |
| **DPA** (cho doanh nghiệp) | p2pt không xử lý dữ liệu người dùng cuối. Liệt kê dữ liệu tài khoản mà p2pt có xử lý |
| **SLA** (gói Studio/Enterprise) | Cam kết website/license authority. **Không cam kết khả năng kết nối P2P 100%**, nêu rõ giới hạn NAT |
| **NDA + hợp đồng chuyển giao quyền tác giả** với nhân viên và freelancer | Mục 2 |

---

## 10. Lộ trình đề xuất (thứ tự ưu tiên)

1. **Tuần này:** chọn 3–5 tên ứng viên, tra cứu nhãn hiệu, tên miền, crates.io/npm/PyPI
2. **Tuần này:** đặt lịch luật sư cho **ba câu hỏi**:
   - (a) p2pt có thuộc Phụ lục I NĐ 341/2026 không, và lộ trình xin phép thế nào
   - (b) "cho thuê license" có thuộc diện không chịu VAT không
   - (c) điều kiện ưu đãi thuế TNDN
3. **Trong 2 tuần:** thành lập hoặc chuẩn hoá công ty. Ký hợp đồng chuyển giao quyền với mọi người đã viết code
4. **Ngay sau khi chốt tên:** **nộp đơn nhãn hiệu** nhóm 9 và 42 để giữ ngày ưu tiên. Trong 6 tháng sau đó, cân nhắc nộp Madrid
5. **Song song:** đăng ký quyền tác giả cho bản core và website
6. **Trước khi mở bán:** EULA, Privacy Policy, DPA hoàn chỉnh. Bỏ `hostname` khỏi lease request. Thêm checkbox đồng ý
7. **Nếu phải xin phép mật mã:** chuẩn bị 2 kỹ sư đủ điều kiện và hồ sơ kỹ thuật (tài liệu kiến trúc trong repo dùng lại được). Trong thời gian chờ, bán cho khách quốc tế nếu luật sư xác nhận được phép

## Nguồn
- NĐ 211/2025 & mật mã dân sự: [luatvietnam.vn: phạt tới 180 triệu](https://luatvietnam.vn/tin-van-ban-moi/kinh-doanh-mat-ma-dan-su-khong-co-giay-phep-phat-den-180-trieu-dong-186-103254-article.html), [baovanhoa.vn](https://baovanhoa.vn/kinh-te/siet-chat-quan-ly-mat-ma-dan-su-bo-sung-che-tai-manh-ve-an-ninh-mang-164776.html), [baophapluat.vn: điều kiện](https://baophapluat.vn/doanh-nghiep-kinh-doanh-mat-ma-dan-su-phai-dap-ung-nhung-dieu-kien-nao.html), [tienphong.vn](https://tienphong.vn/quy-dinh-moi-ve-kinh-doanh-xuat-nhap-khau-mat-ma-dan-su-post1873317.tpo)
- NĐ 341/2026: [Thư viện Pháp luật](https://thuvienphapluat.vn/van-ban/Cong-nghe-thong-tin/Nghi-dinh-341-2026-ND-CP-huong-dan-Luat-An-ninh-mang-ve-hoat-dong-mat-ma-dan-su-723218.aspx), [bản tiếng Anh](https://thuvienphapluat.vn/van-ban/Cong-nghe-thong-tin/Decree-341-2026-ND-CP-elaborating-Law-Cybersecurity-pertaining-to-activities-related-to-civil-cryptography-724237.aspx), [Nghệ An HCC](https://hcc.nghean.gov.vn/tin-tong-hop/nghi-dinh-so-341-2026-nd-cp-quy-dinh-chi-tiet-luat-an-ninh-mang-ve-hoat-dong-mat-ma-dan-su-2794.html), [lsvn.vn](https://lsvn.vn/quy-dinh-moi-ve-hoat-dong-mat-ma-dan-su-a178646.html)
- Nhãn hiệu, phí và thời hạn: [thuvienphapluat: thủ tục 2025](https://thuvienphapluat.vn/hoi-dap-phap-luat/thu-tuc-dang-ky-nhan-hieu-nam-2025-thuc-hien-theo-trinh-tu-nao-138065410.html), [luatvietan: chi phí](https://luatvietan.vn/chi-phi-dang-ky-nhan-hieu.html), [luatvietan: các giai đoạn thẩm định](https://luatvietan.vn/cac-giai-doan-tham-dinh-don-dang-ky-nhan-hieu.html), [luatvietnam: điểm mới Luật SHTT 2022](https://luatvietnam.vn/doanh-nghiep/diem-moi-cua-luat-so-huu-tri-tue-2022-ve-nhan-hieu-561-92153-article.html)
- Quyền tác giả phần mềm: [thuvienphapluat: mức phí](https://thuvienphapluat.vn/hoi-dap-phap-luat/muc-thu-phi-cap-giay-chung-nhan-dang-ky-quyen-tac-gia-va-quyen-lien-quan-la-bao-nhieu-138020221.html), [luatminhkhue: thủ tục](https://luatminhkhue.vn/thu-tuc-dang-ky-ban-quyen-phan-mem-may-tinh.aspx), [Thông tư 211/2016](https://luatvietnam.vn/thue/thong-tu-211-2016-tt-btc-bo-tai-chinh-111335-d1.html)
- Công nghệ số và thuế: [Luật CNCNS 71/2025 (TVPL)](https://thuvienphapluat.vn/van-ban/cong-nghe-thong-tin/Luat-Cong-nghiep-cong-nghe-so-2025-so-71-2025-QH15-621341.aspx), [luatvietnam: 8 điểm đáng chú ý](https://luatvietnam.vn/linh-vuc-khac/diem-dang-chu-y-tai-luat-cong-nghiep-cong-nghe-so-2025-883-103086-article.html), [luatvietnam: bảng thuế suất ưu đãi 2026](https://luatvietnam.vn/thue-phi-le-phi/bang-thue-suat-uu-dai-thue-thu-nhap-doanh-nghiep-565-103567-article.html)
- Báo cáo đối thủ, bằng sáng chế quốc tế, xuất khẩu: [RESEARCH-IP-SECURITY.md](RESEARCH-IP-SECURITY.md)
