# Nạp tiền không lạm phát, không trả tiền để mạnh hơn

> Trạng thái: **code xong, TẮT mặc định**. Server `sv.nap_tien=0` (và chỉ bật được khi `sv.lau_dai=1`),
> web `HTBB_NAP_BAT=false`. **Chỉ bật sau khi có giấy phép hình ảnh/IP bằng văn bản** (mục 9).
>
> Liên quan: [`THIET_KE_LAU_DAI.md`](THIET_KE_LAU_DAI.md) (mô hình tiền tệ 2.1, Q10 cũ “không thu tiền”),
> [`KIEM_TOAN_KINH_TE.md`](KIEM_TOAN_KINH_TE.md) (mốc nạp, TileNap, lượng rơi, hoàn xu khi tiêu lượng),
> [`KE_HOACH_LAU_DAI_GD2.md`](KE_HOACH_LAU_DAI_GD2.md) (bước 2.11). Mô phỏng: `tools/pokemon/MoPhongKinhTe.java`
> (mô-đun NẠP TIỀN). Số liệu gốc: `src/com/htbb/pokemon/NapTienLogic.java`.

## 1. Nguyên tắc

| # | Nguyên tắc | Cơ chế trong code |
|---|---|---|
| P1 | **Bán thời gian/tiện ích/trang trí, không bán sức mạnh** | Cửa hàng chỉ có 4 cách trao (whitelist `TRAO_HOP_LE`): `trang_tri`, `mo_rong_tui`, `doi_ten`, **`tang_toc`** (18/10/2026, mục 3b); cột `co_chi_so` phải = 0; tên chứa từ cấm (EXP, x2, tỉ lệ, đá, chỉ số…) bị từ chối (`NapTienLogic.kiemMon`, bản TS `kiemMon`) — riêng `tang_toc` chỉ nhận **đúng 5 mã cố định** (`TangToc.MA`), giá + giới hạn tuần cố định (`kiemTangToc`) |
| P1b | **Thời gian mua được phải có bản chơi được, chung trần, không thành sức mạnh** (18/10/2026) | Mỗi món tăng tốc có bản khoá kiếm bằng chơi; bản cao cấp + bản khoá chung trần 60 điểm/ngày, 210 điểm/tuần (`TangToc`); không dùng ở cấp 100 (không mua tinh thông); chỉ cho EXP — không xu, đá, vật phẩm, tỉ lệ rơi. Vẫn **không bao giờ** bán chỉ số, trang bị, đá, tỉ lệ/bảo hộ nâng cấp |
| P2 | **Lượng chỉ vào qua nạp** (+ thành tựu nhỏ có trần đã thiết kế) | `NapTien.bat()` = `sv.nap_tien` **và** `sv.lau_dai` (fail closed): lau_dai đã tắt lượng rơi từ quái, giftcode lượng, mốc nạp, shop bán đồ chỉ số bằng lượng |
| P3 | **Mọi lượng tiêu là hố hút (đốt)** | `Char.subLuong` / `subLuongTang`: khi bật thì return ngay sau khi trừ — không cộng điểm xài VIP, không đổi ra xu, không vé sự kiện |
| P4 | **Tỷ giá cố định, không ngẫu nhiên** | 10 lượng / 1.000 đ; `kiemGoi` chặn khuyến mãi %; `TileNap` bị ép = 1; không có gacha |
| P5 | **Giao dịch hàng cao cấp giữa người chơi chỉ qua chợ có thuế** | Chuyển lượng trực tiếp bị chặn (`CharChuyenLuong`); Chợ trang trí: phí đăng 1% + thuế bán 6% (đốt), giá phải trong 50–200% trung vị 20 giao dịch gần nhất |
| P6 | **Bảo vệ người chơi** | Trần 1.000.000 đ/tuần, 2.000.000 đ/tháng/tài khoản (kẹp ≤ 5.000.000); tự đặt trần thấp hơn (hạ ngay, nâng sau 72 giờ) |
| P7 | **Hoàn tiền/chargeback có thu hồi** | Đơn đã cộng → `cho_thu_hoi` → server chuyển thành **nợ**, trừ dần; nợ > 0 khoá cửa hàng; chargeback khoá tạo đơn mới |

## 2. Gói (bảng `nap_goi`, sửa ở /admin/naptien › Gói)

| Mã | Gói | Giá | Nhận | Ghi chú |
|---|---|---|---|---|
| L20 | 200 lượng | 20.000 đ | 200 lượng | tỷ giá cố định |
| L50 | 500 lượng | 50.000 đ | 500 lượng | |
| L100 | 1.000 lượng | 100.000 đ | 1.000 lượng | |
| L200 | 2.000 lượng | 200.000 đ | 2.000 lượng | |
| L500 | 5.000 lượng | 500.000 đ | 5.000 lượng | |
| THE_THANG | Thẻ tháng | 50.000 đ | 20 lượng/ngày × 30 (= 600) | ngày bỏ lỡ **không** cộng bù; không mua khi còn > 30 ngày (tối đa 2 tháng chồng); QoL: +1 lượt Hầm ngục/ngày (thưởng **chỉ** 20 điểm Vé mùa — không EXP/vật liệu), +2 ô bán kính nhặt đồ, +10 ô tủ đồ trang trí, **+1 giờ ủy thác luyện tập/ngày** (vẫn trong trần điểm tăng tốc) |
| VE_MUA | Vé mùa cao cấp | 99.000 đ | mở track cao cấp mùa hiện tại | mùa vé 56 ngày; tiến độ **chỉ bằng chơi** |

**Quà nạp lần đầu** (1 lần/tài khoản): khung tên “Người ủng hộ” — trang trí, không giao dịch được.

### Vé mùa (56 ngày, 30 bậc × 100 điểm)

- Điểm: 1 điểm / 3 phút online (trần 40/ngày) + móc `NapTienGame.congDiemVeMua` cho nhiệm vụ (đề xuất 10/nhiệm vụ ngày) + 20 từ lượt Hầm ngục thêm của Thẻ tháng. Trần tổng 120 điểm/ngày.
- Track miễn phí (mọi người): bậc 3, 6, …, 30 = 3 đá nâng cấp **khoá** (tổng 30), bậc 5/10/20/25 trang trí, bậc 7/14/22
  1 Vé EXP x1,5 **khoá**, bậc 28 1 Bùa thể lực **khoá** (`TG:ma:n`).
- Track cao cấp: **đúng 30 đá khoá** (bằng track miễn phí — test chặn), bậc 26 2 Vé EXP x2 **khoá** (60 điểm tăng tốc, ít hơn
  90 điểm của track miễn phí — test chặn), còn lại trang phục/biểu cảm/hào quang. Vật phẩm tăng tốc từ vé mùa là bản khoá,
  dùng chung trần ngày/tuần. Không xu, lượng, lượng khoá, EXP trực tiếp, đồ chỉ số (test chặn).
- 30 đá ≈ 3,5% số đá người chơi vừa (3 giờ/ngày, ~5 đá/giờ hiệu dụng) kiếm trong 56 ngày.

## 3. Cửa hàng lượng trong game (bảng `nap_cua_hang`, xoay vòng 4 tuần)

NPC Tổng Quản › **Cửa hàng & Nạp** (npc ảo −540…−549): số dư/nợ · Cửa hàng · Thẻ tháng (nhận 20 lượng/ngày) · Vé mùa (nhận thưởng) · Tủ đồ trang trí (dùng/bỏ, rao bán) · Chợ trang trí · Hướng dẫn.

| Nhóm | Món mặc định | Giá (lượng) | Giao dịch |
|---|---|---|---|
| Tiện ích (luôn bán) | Mở rộng hành trang +42 ô (tối đa 4 lần, tới `Char.MAX_BAG_LIMIT`) | 300 | không |
| | Đổi tên (1 lần/30 ngày, dùng luồng `CharDoiTenNhanVat`) | 500 | không |
| Màu tên (luôn bán) | Vàng / Xanh ngọc, 30 ngày | 150 | không |
| Khung | Khung tên Bạc | 200 | có |
| Xoay tuần 1–4 | Skin (4 mẫu) 600, Hào quang 400, Thú cưỡi 500, Biểu cảm 80, Trang trí nhà 120 | | có |

| Tăng tốc luyện cấp (luôn bán, mục 3b) | Vé EXP x1,5 (30 phút) 20 · Vé EXP x2 (30 phút) 40 · Bùa thể lực 60 · Phiếu ủy thác (1 giờ) 15 · Kẹo hiếm 40 | giới hạn mua/tuần 14 / 7 / 4 / 17 / 2 | không (vào túi, khoá) |

Đồ trang trí là **quyền sở hữu** (`nap_so_huu`) + trạng thái đang dùng. Hình ảnh hiển thị trên client cần
asset có giấy phép — việc của nhóm client khi có asset (mục 10).

## 3b. Tăng tốc luyện cấp (18/10/2026)

Quyết định: không trần cấp theo mùa; lên cấp chậm; người chơi được **mua thời gian** (cày/treo nhanh hơn) nhưng không mua
sức mạnh. Thiết kế đầy đủ: [`THIET_KE_LAU_DAI.md`](THIET_KE_LAU_DAI.md) mục 1.8; code `com.htbb.pokemon.TangToc` (thuần) +
`real.TangTocGame`.

- **Whitelist**: trao `tang_toc` chỉ cho 5 mã `ve_exp_15`, `ve_exp_2`, `bua_the_luc`, `uy_thac`, `keo_hiem`; giá cố định
  20/40/60/15/40 lượng; `gioi_han` = số lần mua/**tuần** (≤ 14/7/4/17/2, đếm `nap_mua_dem` theo khoá `ma@tuần`); không hạn,
  không giao dịch, không chỉ số. Admin không thêm được món tăng tốc mới bằng dữ liệu (server + web TS cùng luật).
- **Chung trần** với bản khoá kiếm bằng chơi (nhiệm vụ tuần, lịch đăng nhập, vé mùa, Kecleon bằng lượng khoá):
  60 điểm/ngày, 210 điểm/tuần (≈ 3,5 giờ EXP), tính khi **dùng**. Mua thêm không vượt được trần.
- Chỉ EXP: không xu, đá, vật phẩm, không đổi tỉ lệ rơi; Bùa thể lực chỉ kéo dài năng lượng cho EXP (xu/đồ rơi vẫn theo
  năng lượng gốc). Không dùng ở cấp 100 (tinh thông = sức mạnh, không bán).
- **Trần chi tiêu** vẫn áp (1.000.000 đ/tuần, 2.000.000 đ/tháng, tự đặt thấp hơn). Lấp đủ trần tăng tốc cả tuần tốn
  ≈ 260 lượng ≈ 26.000 đ/tuần.
- Lượng tiêu cho tăng tốc **bị đốt** như mọi lượng khác (P3).

## 4. Luồng tiền và trạng thái đơn

```
web: tạo đơn (khoá idempotent, trần, nhân vật nhận)  -> cho_thanh_toan  (mã HTxxxxxxxx = nội dung CK)
cổng (SePay/Casso/mock) hoặc admin                   -> da_thanh_toan   [web: napXacNhanThanhToan — HÀM DUY NHẤT]
server game (vòng 15 giây, nhân vật online)          -> da_cong         [game: NapTienGame.congDon — HÀM DUY NHẤT]
quá 24 giờ chưa trả                                   -> that_bai  (tiền đến muộn vẫn -> da_thanh_toan)
hoàn trước khi vào game                               -> hoan_tien
hoàn / chargeback sau khi vào game                    -> cho_thu_hoi -> da_thu_hoi (server: lượng -> nợ, thẻ −30 ngày, vé mất cao cấp)
```

Idempotent 3 lớp: `UPDATE … WHERE trang_thai=<cũ>` (chỉ 1 lần đổi); `UNIQUE(nha_cung_cap, ma_gd_ncc)`
(một giao dịch ngân hàng không xác nhận 2 đơn, webhook gửi lặp bị chặn ở `nap_webhook`); sổ cái
`UNIQUE(ma_tham_chieu)` (`cong:<mã>`, `tt:<mã>`, `thethang:<char>:<ngày>`…). Không dùng khoá hàng/khác
biệt CSDL: chạy y hệt MariaDB và NovaDB. Mốc thời gian do ứng dụng ghi (NOW() của NovaDB là UTC).

Rủi ro còn lại: server sập giữa “chốt da_cong” và `saveCharAuto` → đơn `da_cong` với `da_luu_nv=0`;
trang Đối soát liệt kê để admin kiểm và cộng tay.

## 5. Bảng (`sql/pokemon_nap_tien.sql`, đã gom vào `tools/pokemon/xuat_sql_day_du.sh`)

`nap_goi` (danh mục) · `nap_don` (đơn: mã duy nhất, khoá idempotent, số tiền, trạng thái, mã GD cổng) ·
`nap_vi` (theo tài khoản: tổng nạp, nợ, khoá nạp, thưởng lần đầu, trần tự đặt) · `nap_so_cai` (sổ cái bất biến) ·
`nap_nhat_ky` (kiểm toán: ai, làm gì, IP) · `nap_webhook` (webhook thô) · `nap_cau_hinh` (trần + nhịp server) ·
`nap_cua_hang` · `nap_so_huu` · `nap_cho` · `nap_the_thang` · `nap_ve_mua` · `nap_mua_dem`.
Đã chạy 2 lần liên tiếp trên NovaDB (CSDL nháp `htbb_db_nap`, đã xoá bảng sau thử).

## 6. Web (HTBB-Web)

| Phần | File |
|---|---|
| Luật thuần (bản TS của `NapTienLogic`, chữ ký webhook) | `server/utils/napTienThuan.ts` |
| CSDL: tạo đơn, **`napXacNhanThanhToan`**, hoàn tiền, đối soát | `server/utils/napTien.ts` |
| Người chơi | `/api/nap/goi`, `tao-don`, `don`, `trang-thai`, `lich-su`, `tran`; trang `/naptien` |
| Webhook | `/api/nap/webhook-sepay` (Apikey, mục 6b), job `server/plugins/do-sepay.ts` (dò user API), `/webhook-casso` (HMAC-SHA512 `X-Casso-Signature: t=,v1=` trên JSON đã sắp khoá, lệch giờ ≤ 5 phút — **đối chiếu tài liệu Casso trước khi bật**), `/webhook-mock` (HMAC-SHA256, chỉ `HTBB_NAP_SANDBOX=1`) |
| Admin (ghi: Siêu quản trị) | `/admin/naptien`: Đơn (xác nhận theo mã GD sao kê, hoàn tiền/chargeback, giả lập sandbox), Cộng tay, Đối soát, Gói (CMS), Cửa hàng, Nhật ký, Mốc cũ (chỉ xem) |
| Mật khẩu chung | `/xac-nhan-nap` (đơn mới, bắt buộc mã GD) |
| Pháp lý (bản nháp) | `/dieu-khoan-thanh-toan`, `/chinh-sach-hoan-tien` |

Bí mật chỉ ở biến môi trường máy chủ: `HTBB_SEPAY_WEBHOOK_APIKEY`, `HTBB_CASSO_WEBHOOK_SECRET`,
`HTBB_NAP_MOCK_SECRET`. Không có gì về nạp đi vào bundle trình duyệt ngoài `napBat`.

## 6b. SePay — nạp tự động (hướng dẫn cho chủ server)

Đối chiếu tài liệu công khai SePay ngày 18/10/2026: [tích hợp webhook](https://docs.sepay.vn/tich-hop-webhooks.html),
[API giao dịch](https://docs.sepay.vn/api-giao-dich.html), [API token](https://docs.sepay.vn/tao-api-token.html).
Tên menu trong trang quản lý SePay có thể đổi theo phiên bản — làm theo ý, kiểm lại trên my.sepay.vn.

**Bước 1 — Đăng ký và liên kết ngân hàng.** Tạo tài khoản ở https://my.sepay.vn, chọn gói, liên kết tài khoản
ngân hàng nhận tiền (đúng số tài khoản sẽ đặt ở `HTBB_NAP_ACCOUNT_NO`). Ghi lại mã BIN ngân hàng (bảng trong
`HTBB-Web/.env.example`, `HTBB_NAP_BANK_BIN`) và tên chủ tài khoản viết HOA không dấu (`HTBB_NAP_ACCOUNT_NAME`).

**Bước 2 — Cấu trúc mã thanh toán.** Trong phần cấu hình chung của công ty trên SePay, đặt cấu trúc mã thanh
toán: **tiền tố `HT`, phần sau 8 ký tự chữ + số**. Mã đơn của web là `HT` + 8 ký tự `A–Z, 2–9` (bỏ 0/O/1/I),
ví dụ `HT7F3AQ2BX`. Có cấu hình này thì SePay tự điền trường `code` của webhook. Web khớp **`code` trước**,
không có thì tìm mã trong `content`/`description` (người chơi gõ tay vẫn khớp, không phân biệt hoa thường).

**Bước 3 — Webhook.** SePay › Webhooks › Thêm webhook:
- Sự kiện: **Có tiền vào**. Tài khoản: tài khoản ở bước 1 (có thể lọc theo tiền tố mã thanh toán `HT`).
- URL: `https://<tên-miền>/api/nap/webhook-sepay`.
- Kiểu chứng thực: **API Key**, khoá là chuỗi ngẫu nhiên ≥ 24 ký tự (`openssl rand -hex 24`), đặt **cùng chuỗi** vào
  `HTBB_SEPAY_WEBHOOK_APIKEY`. SePay gửi header `Authorization: Apikey <khoá>`; sai/thiếu → 401.
- Content-Type `application/json`. Thân: `id, gateway, transactionDate, accountNumber, code, content, transferType,
  transferAmount, accumulated, subAccount, referenceCode, description`.
- SePay coi là thành công khi HTTP 200/201 + `{"success": true}` trong 30 giây, ngược lại gửi lại (tối đa 7 lần / 5 giờ).
  Web trả `{"success": true}` cho mọi giao dịch đã ghi sổ (kể cả trùng / sai tài khoản / không mã / thiếu tiền) để
  SePay không gửi lại vô ích; chỉ lỗi CSDL mới trả 500 (để SePay gửi lại).

**Bước 4 (tuỳ chọn) — Job dò giao dịch lỡ webhook.** SePay › Cấu hình công ty › API Access › tạo API token, đặt vào
`HTBB_SEPAY_API_TOKEN`. Web gọi `GET https://my.sepay.vn/userapi/transactions/list?account_number=…&transaction_date_min=…`
(header `Authorization: Bearer <token>`) mỗi `HTBB_SEPAY_DO_PHUT` phút (2–5), nhìn lại `HTBB_SEPAY_DO_GIO` giờ;
giao dịch vào cùng hàm xử lý với webhook, khoá trùng là `id` của SePay ⇒ không bao giờ cộng hai lần. Giới hạn
SePay 3 lần/giây; nhận 429 thì chờ theo `x-sepay-userapi-retry-after`. Để trống token = tắt job.

**Bước 5 — Biến môi trường** (đủ ở `HTBB-Web/.env.example`; deploy GitHub đã thêm khoá vào `deploy.yml` +
`deploy/deploy-web.sh`): `HTBB_NAP_BAT=true`, `NAP_DA_CO_GIAY_PHEP=1`, `HTBB_NAP_ACCOUNT_NO`, `HTBB_NAP_ACCOUNT_NAME`,
`HTBB_NAP_BANK_BIN`, `HTBB_SEPAY_WEBHOOK_APIKEY`, (`HTBB_SEPAY_API_TOKEN`), `HTBB_ADMIN_NAPTIEN_PASSWORD` (≥ 12 ký tự),
`HTBB_SECRET` (≥ 32 ký tự). **Fail closed**: trên máy thật thiếu/yếu/mặc định một khoá thì web TỰ TẮT nạp, log
`NAP TIEN BI TAT (fail closed)` kèm danh sách, và /admin/naptien hiện hộp đỏ liệt kê lý do. `HTBB_NAP_SANDBOX` /
`HTBB_NAP_MOCK_SECRET` không đi qua deploy (chỉ máy dev); bật sandbox trên máy thật cũng làm nạp tắt.

**Bước 6 — Giao dịch thử.** Bật server game (`sv.lau_dai=1`, `sv.nap_tien=1`), vào game bằng một nhân vật. Trên web
tạo đơn **L20 (20.000 đ)** cho nhân vật đó, quét QR (số tiền + nội dung đã điền sẵn; trên điện thoại có nút mở app
ngân hàng qua `dl.vietqr.io`). Trong vài giây: trang đổi sang "Đã thanh toán", ~15 giây sau "Đã cộng vào nhân vật",
trong game +200 lượng. Kiểm /admin/naptien › Nhật ký (webhook `ok`, sổ cái `thanh_toan` + `cong`). SePay › Webhooks
cũng có nhật ký từng lần gửi.

**Luật khớp tiền** (`napXuLyGiaoDich` + `napXacNhanThanhToan`):

| Tình huống | Kết quả |
|---|---|
| Đúng tài khoản, đúng mã, đủ tiền | `da_thanh_toan` → server game cộng (`da_cong`) |
| Gửi lặp cùng `id` (webhook lặp, hoặc job + webhook) | bỏ qua (`duplicate`), không cộng lại |
| Tiền vào tài khoản khác (`accountNumber`/`subAccount` ≠ `HTBB_NAP_ACCOUNT_NO`) | bỏ qua, ghi `sai_tai_khoan` |
| Không có mã đơn / mã không tồn tại | ghi `khong_co_ma_don` / `khong_thay` để admin đối soát (cộng tay nếu cần) |
| Chuyển **thiếu** | không xác nhận; đơn giữ "chờ", ghi số đã nhận (`so_tien_nhan`, `ly_do THIEU…`); Đối soát liệt kê — admin xác nhận tay khi khách chuyển bù, hoặc hoàn |
| Chuyển **dư** | xác nhận + giao hàng; ghi `DU: thua N d`, Đối soát liệt kê để admin hoàn phần dư |
| Đơn đã hết hạn 24 giờ (`that_bai`) rồi mới trả | **vẫn xác nhận và giao hàng** (chính sách: tiền đã vào thì giao) |
| Đơn đã hoàn / thu hồi | `sai_trang_thai`, không giao — admin xử lý |
| Một giao dịch dùng cho hai đơn | chặn bằng `UNIQUE(nha_cung_cap, ma_gd_ncc)` |

### Thử đầu-cuối (18/10/2026, máy dev, CSDL nháp NovaDB `htbb_db_nap`, không gọi SePay thật)

Web dev ở cổng riêng + SePay user API giả lập ở cổng riêng; webhook đúng định dạng SePay ở trên (header `Apikey`):
**23/23 đạt** — tạo 7 đơn; webhook đúng → 200 `{success:true}` + đã thanh toán; gửi lặp → duplicate; sai khoá / thiếu
header → 401; sai tài khoản → bỏ qua; mã không tồn tại → không xác nhận; `code` rỗng → khớp theo nội dung; chuyển
thiếu → giữ chờ + ghi 50.000; chuyển dư → xác nhận + ghi phần dư; **10 webhook cùng `id` song song → đúng 1 lần
xác nhận, 1 dòng sổ cái**; đơn quá hạn trả muộn → vẫn xác nhận; Đối soát liệt kê dư/thiếu; đơn lỡ webhook → job dò
user API xác nhận trong ≤ 2 phút, giao dịch đã có qua webhook không xử lý lại.

Phía game: `NapTienGame.congDon` chạy với một đối tượng `Char` thật nhưng **không có client/bản đồ** (dựng một tiến
trình game + đăng nhập client thật trên CSDL nháp không làm được trong môi trường thử): 6 đơn đã thanh toán × 4 luồng
cộng song song mỗi đơn → **2.800 lượng, mỗi đơn đúng 1 luồng thắng**, 6 dòng sổ cái `cong`, quà lần đầu 1 lần; chạy
lại lần 2 → 0. (`da_luu_nv=0` vì không có CSDL nhân vật thật — đúng trường hợp Đối soát cảnh báo.) Ảnh:
[QR trên điện thoại](anh_nap_tien/qr-mobile.png), [đã cộng](anh_nap_tien/thanh-cong.png).

## 7. Mô phỏng (mô-đun NẠP TIỀN trong `MoPhongKinhTe.java`)

Nhóm trả tiền (RNG riêng, không đổi chuỗi số của mô phỏng gốc): **không nạp 85%, nạp nhẹ 12%, cá voi 3%**.
Cá voi: vé mùa mỗi mùa, thẻ tháng liên tục, mua trang trí ~25%/ngày; nạp nhẹ: vé 50% mùa, thẻ 25% tháng,
trang trí 5%/ngày. Mọi người có track miễn phí. Người không nạp dư xu (> 6 giờ thu nhập) mua trang trí
của người nạp trên Chợ (thuế 7%); người nạp chỉ rao khi giá ≥ 1 giờ thu nhập, tồn kho giao dịch ≤ 3.

Kết quả (730 ngày, dân số 100%, `docs/pokemon/mo_phong_tom_tat.txt`, 18/10/2026 có tăng tốc): **17/17 mục tiêu chung + 6/6 mục tiêu nạp**.
Người nạp mua tăng tốc theo nhóm (cá voi lấp đủ trần, nạp nhẹ ~40% ngày một món); mô phỏng thêm ba dân chuẩn (không ai dùng /
mọi người mua tối đa): mua tối đa nhanh hơn ≤ +29% (mục tiêu ≤ 40%), sức mạnh cùng cấp +0,36% (≤ 1%).

| Mục tiêu | Kết quả |
|---|---|
| Chênh sức mạnh người nạp / không nạp **cùng giờ chơi** (dân chuẩn 1.000/nhóm, max theo nhóm & tháng 3–24) ≤ 5% | người nạp ≤ 4,18%, cá voi ≤ 4,98% (trước tăng tốc: 0,31 / 0,95%; người nạp lên cấp sớm hơn nên thu nhập xu theo cấp cao hơn vài tháng — cùng cấp chỉ +0,36%) |
| Hố hút/nguồn xu 80–105%, lệch ≤ 2 điểm so với không có nạp | 98,8% (không nạp 98,8%) |
| Xu thực tăng/tháng lệch ≤ 1 điểm | 4,09% (không nạp 4,05%) |
| Lượng nạp bị đốt ≥ 95% (từ tháng 3; cũ 90%) | 97,8% |
| Số dư lượng TB/người nạp ≤ 1 gói (1.000), trôi ≤ 5%/tháng | cao nhất 225, trôi 1,31%/tháng |
| Giá chợ trang trí ổn định (≤ 10%/tháng, ±50% giá tháng 3) | đổi tối đa 0,6%/tháng, 1,00 giờ thu nhập |

Doanh thu mô hình ≈ 113 triệu đ/tháng ở dân số ~21.000 lượt người, ARPPU ≈ 114.000 đ/tháng (chỉ để so sánh phương án; trước tăng tốc ≈ 105 triệu / 100.000 đ).
Kiểm tự động: `test/KiemNapTien.java` gọi `MoPhongKinhTe.chayKiemNapTien(".", 730, 25)`.

## 8. Hoàn tiền / chargeback

Trang `/chinh-sach-hoan-tien` (bản nháp): hoàn khi lỗi giao hàng 24 giờ, chuyển trùng/dư, hoặc trong 7 ngày nếu
chưa dùng. Thao tác: /admin/naptien › Đơn › Hoàn tiền (lý do bắt buộc, công tắc chargeback). Chuyển tiền trả lại
làm ngoài hệ thống, ghi mã giao dịch vào lý do. Trong game: lượng của đơn thành nợ của tài khoản, trừ vào lượng
đang có khi online (`NapTienGame.thuNo`), lượng nạp/thẻ tháng sau trả nợ trước; nợ > 0 khoá cửa hàng + đổi tên.

## 9. Bật nạp (checklist)

1. **Giấy phép**: asset PMDCollab là CC BY-NC 4.0, tên/hình Pokémon là IP của Nintendo/The Pokémon Company.
   Phải có giấy phép bằng văn bản (hoặc thay toàn bộ asset/tên bằng tài sản tự sở hữu) **trước** khi thu tiền.
   Web in cảnh báo khi khởi động và /admin/naptien hiện cảnh báo đỏ tới khi đặt `NAP_DA_CO_GIAY_PHEP=1`.
2. Luật sư rà soát `/dieu-khoan-thanh-toan`, `/chinh-sach-hoan-tien` (và cập nhật `/dieu-khoan` đang ghi “không nạp tiền”).
3. CSDL: `mysql htbb_db < sql/pokemon_nap_tien.sql`.
4. Server game: `sv.lau_dai=1`, `sv.nap_tien=1`, khởi động lại. Log phải in `NAP TIEN -> BAT`; /admin/naptien › Đối soát thấy nhịp server.
5. Web: làm theo mục 6b (SePay) bước 1–6. Cấu hình yếu = nạp tự tắt (fail closed). **Không** đặt `HTBB_NAP_SANDBOX` trên máy thật.
6. Thử trên máy dev trước: `HTBB_NAP_SANDBOX=1 HTBB_NAP_MOCK_SECRET=…` → nút “Giả lập” trong /admin/naptien.

## 10. Còn lại / giới hạn

- Hiển thị đồ trang trí trên client (skin, hào quang, màu tên…) cần asset có giấy phép + gói tin client; server đã lưu sở hữu/đang dùng.
- Điểm Vé mùa từ nhiệm vụ: các hệ thống nhiệm vụ gọi `NapTienGame.congDiemVeMua(p, 10, "nhiem_vu_ngay")` (chưa nối, chỉ online + Hầm ngục).
- `sql/htbb_day_du.sql` cần sinh lại bằng `tools/pokemon/xuat_sql_day_du.sh` (script xoá CSDL — chỉ chạy trên MariaDB trống).
- Casso v2: định dạng chữ ký viết theo tài liệu công khai, phải đối chiếu bản hiện hành trước khi bật.
