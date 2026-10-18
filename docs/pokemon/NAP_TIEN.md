# Nạp tiền không lạm phát, không trả tiền để mạnh hơn

> Trạng thái: **code xong, TẮT mặc định**. Server `sv.nap_tien=0` (và chỉ bật được khi `sv.lau_dai=1`),
> web `KPAH_NAP_BAT=false`. **Chỉ bật sau khi có giấy phép hình ảnh/IP bằng văn bản** (mục 9).
>
> Liên quan: [`THIET_KE_LAU_DAI.md`](THIET_KE_LAU_DAI.md) (mô hình tiền tệ 2.1, Q10 cũ “không thu tiền”),
> [`KIEM_TOAN_KINH_TE.md`](KIEM_TOAN_KINH_TE.md) (mốc nạp, TileNap, lượng rơi, hoàn xu khi tiêu lượng),
> [`KE_HOACH_LAU_DAI_GD2.md`](KE_HOACH_LAU_DAI_GD2.md) (bước 2.11). Mô phỏng: `tools/pokemon/MoPhongKinhTe.java`
> (mô-đun NẠP TIỀN). Số liệu gốc: `src/com/htbb/pokemon/NapTienLogic.java`.

## 1. Nguyên tắc

| # | Nguyên tắc | Cơ chế trong code |
|---|---|---|
| P1 | **Bán thời gian/tiện ích/trang trí, không bán sức mạnh** | Cửa hàng chỉ có 3 cách trao (whitelist `TRAO_HOP_LE`): `trang_tri`, `mo_rong_tui`, `doi_ten`; cột `co_chi_so` phải = 0; tên chứa từ cấm (EXP, x2, tỉ lệ, đá, chỉ số…) bị từ chối (`NapTienLogic.kiemMon`, bản TS `kiemMon`) |
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
| THE_THANG | Thẻ tháng | 50.000 đ | 20 lượng/ngày × 30 (= 600) | ngày bỏ lỡ **không** cộng bù; không mua khi còn > 30 ngày (tối đa 2 tháng chồng); QoL: +1 lượt Hầm ngục/ngày (thưởng **chỉ** 20 điểm Vé mùa — không EXP/vật liệu), +2 ô bán kính nhặt đồ, +10 ô tủ đồ trang trí |
| VE_MUA | Vé mùa cao cấp | 99.000 đ | mở track cao cấp mùa hiện tại | mùa vé 56 ngày; tiến độ **chỉ bằng chơi** |

**Quà nạp lần đầu** (1 lần/tài khoản): khung tên “Người ủng hộ” — trang trí, không giao dịch được.

### Vé mùa (56 ngày, 30 bậc × 100 điểm)

- Điểm: 1 điểm / 3 phút online (trần 40/ngày) + móc `NapTienGame.congDiemVeMua` cho nhiệm vụ (đề xuất 10/nhiệm vụ ngày) + 20 từ lượt Hầm ngục thêm của Thẻ tháng. Trần tổng 120 điểm/ngày.
- Track miễn phí (mọi người): bậc 3, 6, …, 30 = 3 đá nâng cấp **khoá** (tổng 30), bậc 5/10/20/25 trang trí.
- Track cao cấp: **đúng 30 đá khoá** (bằng track miễn phí — test chặn), còn lại trang phục/biểu cảm/hào quang. Không xu, lượng, lượng khoá, EXP, đồ chỉ số (test chặn).
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

Đồ trang trí là **quyền sở hữu** (`nap_so_huu`) + trạng thái đang dùng. Hình ảnh hiển thị trên client cần
asset có giấy phép — việc của nhóm client khi có asset (mục 10).

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
Đã chạy 2 lần liên tiếp trên NovaDB (CSDL nháp `kpah_db_nap`, đã xoá bảng sau thử).

## 6. Web (HTBB-Web)

| Phần | File |
|---|---|
| Luật thuần (bản TS của `NapTienLogic`, chữ ký webhook) | `server/utils/napTienThuan.ts` |
| CSDL: tạo đơn, **`napXacNhanThanhToan`**, hoàn tiền, đối soát | `server/utils/napTien.ts` |
| Người chơi | `/api/nap/goi`, `tao-don`, `don`, `trang-thai`, `lich-su`, `tran`; trang `/naptien` |
| Webhook | `/api/nap/webhook-sepay` (Apikey), `/webhook-casso` (HMAC-SHA512 `X-Casso-Signature: t=,v1=` trên JSON đã sắp khoá, lệch giờ ≤ 5 phút — **đối chiếu tài liệu Casso trước khi bật**), `/webhook-mock` (HMAC-SHA256, chỉ `KPAH_NAP_SANDBOX=1`) |
| Admin (ghi: Siêu quản trị) | `/admin/naptien`: Đơn (xác nhận theo mã GD sao kê, hoàn tiền/chargeback, giả lập sandbox), Cộng tay, Đối soát, Gói (CMS), Cửa hàng, Nhật ký, Mốc cũ (chỉ xem) |
| Mật khẩu chung | `/xac-nhan-nap` (đơn mới, bắt buộc mã GD) |
| Pháp lý (bản nháp) | `/dieu-khoan-thanh-toan`, `/chinh-sach-hoan-tien` |

Bí mật chỉ ở biến môi trường máy chủ: `KPAH_SEPAY_WEBHOOK_APIKEY`, `KPAH_CASSO_WEBHOOK_SECRET`,
`KPAH_NAP_MOCK_SECRET`. Không có gì về nạp đi vào bundle trình duyệt ngoài `napBat`.

## 7. Mô phỏng (mô-đun NẠP TIỀN trong `MoPhongKinhTe.java`)

Nhóm trả tiền (RNG riêng, không đổi chuỗi số của mô phỏng gốc): **không nạp 85%, nạp nhẹ 12%, cá voi 3%**.
Cá voi: vé mùa mỗi mùa, thẻ tháng liên tục, mua trang trí ~25%/ngày; nạp nhẹ: vé 50% mùa, thẻ 25% tháng,
trang trí 5%/ngày. Mọi người có track miễn phí. Người không nạp dư xu (> 6 giờ thu nhập) mua trang trí
của người nạp trên Chợ (thuế 7%); người nạp chỉ rao khi giá ≥ 1 giờ thu nhập, tồn kho giao dịch ≤ 3.

Kết quả (730 ngày, dân số 100%, `docs/pokemon/mo_phong_tom_tat.txt`): **12/12 mục tiêu cũ giữ nguyên + 6/6 mục tiêu nạp**.

| Mục tiêu | Kết quả |
|---|---|
| Chênh sức mạnh người nạp / không nạp **cùng giờ chơi** (dân chuẩn 1.000/nhóm, max theo nhóm & tháng 3–24) ≤ 5% | người nạp ≤ 0,31%, cá voi ≤ 0,95% |
| Hố hút/nguồn xu 80–105%, lệch ≤ 2 điểm so với không có nạp | 99,3% (không nạp 99,3%) |
| Xu thực tăng/tháng lệch ≤ 1 điểm | 3,57% (không nạp 3,45%) |
| Lượng nạp bị đốt ≥ 90% (từ tháng 3) | 97,0% |
| Số dư lượng TB/người nạp ≤ 1 gói (1.000), trôi ≤ 5%/tháng | cao nhất 260, trôi 0,23%/tháng |
| Giá chợ trang trí ổn định (≤ 10%/tháng, ±50% giá tháng 3) | đổi tối đa 0,6%/tháng, 1,00 giờ thu nhập |

Doanh thu mô hình ≈ 105 triệu đ/tháng ở dân số ~21.000 lượt người, ARPPU ≈ 100.000 đ/tháng (chỉ để so sánh phương án).
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
3. CSDL: `mysql kpah_db < sql/pokemon_nap_tien.sql`.
4. Server game: `sv.lau_dai=1`, `sv.nap_tien=1`, khởi động lại. Log phải in `NAP TIEN -> BAT`; /admin/naptien › Đối soát thấy nhịp server.
5. Web: `KPAH_NAP_BAT=true`, `KPAH_TPBANK_ACCOUNT_NO`, `KPAH_TPBANK_ACCOUNT_NAME` (+ `KPAH_NAP_BANK_BIN` nếu không phải TPBank),
   `KPAH_SEPAY_WEBHOOK_APIKEY` và/hoặc `KPAH_CASSO_WEBHOOK_SECRET`, `KPAH_ADMIN_NAPTIEN_PASSWORD` (nếu dùng /xac-nhan-nap),
   `NAP_DA_CO_GIAY_PHEP=1`. **Không** đặt `KPAH_NAP_SANDBOX` trên máy thật.
6. Thử trên máy dev trước: `KPAH_NAP_SANDBOX=1 KPAH_NAP_MOCK_SECRET=…` → nút “Giả lập” trong /admin/naptien.

## 10. Còn lại / giới hạn

- Hiển thị đồ trang trí trên client (skin, hào quang, màu tên…) cần asset có giấy phép + gói tin client; server đã lưu sở hữu/đang dùng.
- Điểm Vé mùa từ nhiệm vụ: các hệ thống nhiệm vụ gọi `NapTienGame.congDiemVeMua(p, 10, "nhiem_vu_ngay")` (chưa nối, chỉ online + Hầm ngục).
- `sql/htbb_day_du.sql` cần sinh lại bằng `tools/pokemon/xuat_sql_day_du.sh` (script xoá CSDL — chỉ chạy trên MariaDB trống).
- Casso v2: định dạng chữ ký viết theo tài liệu công khai, phải đối chiếu bản hiện hành trước khi bật.
