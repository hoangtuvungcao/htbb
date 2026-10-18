# Thành viên Huyền Thoại Bảo Bối

> Trạng thái: **code xong, TẮT mặc định** (`thanh_vien.bat=0`, `thanh_vien.ban_bat=0`). Giai đoạn TEST
> không bắt buộc gì. Khi mở game chính thức, admin bật trên web — **không sửa code, không khởi động lại**.
> Liên quan: [`NAP_TIEN.md`](NAP_TIEN.md) (đơn, sổ cái, SePay, hoàn tiền dùng chung).

## 1. Mục đích và nguyên tắc

- Thành viên **theo TÀI KHOẢN** (mọi nhân vật của tài khoản), giá mặc định **20.000 đ**, thời hạn mặc định
  **vĩnh viễn** (admin đổi được).
- Mục đích chính: **chặn tài khoản phụ / clone** — một clone phải trả tiền mới giao dịch, chat thế giới, lên chợ…,
  nên nuôi hàng chục clone để rửa đồ/xu không còn rẻ.
- **Không cho sức mạnh, không cho tiền tệ**: thành viên chỉ *mở khoá* tính năng. Doanh thu không tạo ra xu/lượng
  trong game (đơn `thanh_vien` có `luong = 0`, server game không cộng gì — `NapTienGame.congDon` bỏ qua loại này).
  Vì không có ưu đãi kinh tế nào nên mô phỏng kinh tế không đổi (không thêm mô-đun).
- Tắt (mặc định) = hành vi cũ hoàn toàn: `ThanhVien.chan` trả về ngay, không đọc CSDL.

Vì sao **không** dùng `5h_active`: bảng đó là cổng **đăng nhập** (kích hoạt 30 ngày, `sv.EnableAvtiveCheck`) —
gộp vào thì người chưa mua không vào được game. Thành viên là bảng riêng `tv_thanh_vien`; trang `/kichhoat`
và `/account` hiện cả hai trạng thái.

## 1b. Kích hoạt tài khoản và Thành viên — hai bậc, MỘT cổng

| | Kích hoạt (`5h_active`) | Thành viên HTBB (`tv_thanh_vien`) |
|---|---|---|
| Giá | miễn phí — admin cấp, hoặc tự kích hoạt khi đăng ký (`web.autoActivate`, `sv.dangKyTuKichHoat`) | trả phí (mặc định 20.000 đ) |
| Mục đích | bước chống clone rẻ (người thật được duyệt) | bậc trả phí chống clone mạnh hơn khi mở chính thức |
| Đăng nhập | **chưa kích hoạt VẪN vào chơi** (`kich_hoat.chan_dang_nhap=0`, mặc định); bật 1 = hành vi cũ | không bao giờ chặn đăng nhập |
| Khoá tính năng | `kich_hoat.chuc_nang` (mặc định giao dịch, chat thế giới, chợ, chuyển lượng, nhận tặng, giftcode) | `thanh_vien.chuc_nang` khi `thanh_vien.bat=1` |

Một cổng `ThanhVien.chan(char, mã)` → `ThanhVienLogic.quyetDinh`: bị chặn nếu **(cần kích hoạt và chưa kích hoạt) hoặc
(cần Thành viên và chưa là Thành viên)**. Lời nhắn chỉ đúng chỗ: `/kichhoat` hoặc `/thanh-vien`. Admin gộp hai bậc làm
một bằng `kich_hoat.thanh_vien_la_kich_hoat=1` (mặc định): Thành viên được coi như đã kích hoạt — kể cả khi bật chặn
đăng nhập. Vào game, tài khoản chưa kích hoạt nhận một dòng nhắc các tính năng đang khoá.

Tất cả chỉ có nghĩa khi `sv.EnableAvtiveCheck=1` (server.ini, khởi động lại); `=0` = coi mọi tài khoản đã kích hoạt.
Cổng đăng nhập cũ nằm **hoàn toàn trong game server** (`LoginHandler` ×3, `SelectCharHandler`); login server
`CheckLoginSocket.jar` chỉ đọc `team_user` (đã soi chuỗi SQL trong jar: không đụng `5h_active`), nên không cần vá jar.
Tài khoản đã kích hoạt không đổi gì.

## 2. Khoá cấu hình (bảng `cau_hinh`, sửa ở /admin/thanh-vien)

Tất cả **đọc lại khi đang chạy** (đăng ký trong `tools/cauhinh/danh_muc.json`, trang "Cấu hình hệ thống"): server game áp
dụng trong ≤ 60 giây (dịch vụ cấu hình trung tâm, log `[CAU HINH] AP DUNG ...`) → `ThanhVien.apDungCauHinh`;
web nhớ 5 giây. Trạng thái thành viên của một tài khoản server nhớ 30 giây.

| Khoá | Kiểu | Mặc định | Nghĩa |
|---|---|---|---|
| `thanh_vien.bat` | 0/1 | 0 | BẮT BUỘC thành viên cho các tính năng trong `chuc_nang` |
| `thanh_vien.ban_bat` | 0/1 | 0 | Mở bán trên web (công tắc RIÊNG, độc lập `HTBB_NAP_BAT`) |
| `thanh_vien.gia` | số (đ) | 20000 | Giá; lưu ở admin thì đồng bộ luôn `nap_goi.THANH_VIEN` |
| `thanh_vien.thoi_han_ngay` | số | 0 | 0 = vĩnh viễn; > 0 = số ngày mỗi lần mua (gia hạn nối tiếp) |
| `thanh_vien.chuc_nang` | danh sách mã, phẩy | xem mục 3 | Tính năng bị khoá khi `bat=1`; mã lạ bị bỏ qua |
| `thanh_vien.mien_cap` | số | 0 | Nhân vật dưới cấp N dùng thử miễn phí (0 = không miễn) |
| `thanh_vien.thong_bao` | chuỗi | "" | Lời nhắn khi bị chặn; `{ten}` `{gia}` `{web}`; trống = mặc định |
| `kich_hoat.chan_dang_nhap` | 0/1 | 0 | 1 = chưa kích hoạt không vào được game (Thành viên vẫn vào nếu dòng cuối = 1) |
| `kich_hoat.chuc_nang` | danh sách mã | `giao_dich,chat_the_gioi,cho,chuyen_luong,nhan_tang,giftcode` | Khoá khi CHƯA kích hoạt (trống = không khoá) |
| `kich_hoat.thong_bao` | chuỗi | "" | Lời nhắn khi bị khoá vì chưa kích hoạt; `{ten}` `{web}` |
| `kich_hoat.thanh_vien_la_kich_hoat` | 0/1 | 1 | Thành viên được coi như đã kích hoạt |

Bán thật sự chỉ khi `ban_bat=1` **và** `NAP_DA_CO_GIAY_PHEP=1` **và** cổng thanh toán đủ an toàn (cùng luật fail
closed của nạp: khoá SePay, HTBB_SECRET, mật khẩu, tài khoản… — `kiemCauHinhNapThat`). Không có công tắc thứ hai
cho cổng: dịch vụ cấu hình tập trung nên đăng ký đúng các khoá trên (không nhân bản).

## 3. Tính năng khoá được (một hàm `ThanhVien.chan(char, mã)`)

| Mã | Tính năng | Chỗ chặn (file) |
|---|---|---|
| `giao_dich` | Giao dịch trực tiếp (mời / nhận lời) | `MapMuaBan.doTradeDuoiKhoa` cmd 0/1 |
| `nhan_tang` | Là người được mời giao dịch / nhận hoa-quà | `MapMuaBan` (người được mời), `MapNhapText` tặng hoa |
| `chat_the_gioi` | Loa (chat toàn máy chủ) | `MapNhapText` nhánh loa |
| `cho` | Chợ ký gửi (rao ×3, mua ×2) + Chợ trang trí | `OfflineMap`, `NapTienGame.chonGiaRao/muaCho` |
| `cho_sinh_thai` | Chợ sinh thái (rao, rao trứng, mua, đơn giá, giao đơn) | `HeSinhThaiMenu` |
| `chuyen_luong` | Chuyển lượng | `CharChuyenLuong` (3 cửa vào) |
| `bang` | Đội: tạo, mời/nhận lời vào đội | `MapDoi.doCreateClan`, `doInviteChar2Clan` |
| `pvp_xep_hang` | Đăng ký giải Liên minh xếp hạng | `MapDoMenu` (NPC −10) |
| `giftcode` | Nhập giftcode | `DatabaseGiftCode.newDoGetGiftCode` |
| `qua_online` | Quà online | `QuaHoatDong.nhan`, `RealController` case 100 |
| `gui_thu` | Nhắn tin riêng, tặng hoa/quà | `MapXaHoi.doSendMessage`, `MapNhapText` |

Mặc định khi bật: `giao_dich,nhan_tang,chat_the_gioi,cho,cho_sinh_thai,chuyen_luong,bang,pvp_xep_hang,giftcode`.
Giao dịch/tặng giữa hai người: người làm cần mã của mình, người kia cần `nhan_tang` — clone chưa mua không
nhận đồ được từ tài khoản chính. Bot/lính thuê của server không bị chặn. Lỗi CSDL khi tra trạng thái = cho qua
(không khoá nhầm người chơi). Lời nhắn dùng hộp thoại có sẵn của client — **không cần sửa client**.

## 4. Mua, cấp, hoàn tiền

- Web `/thanh-vien` → `POST /api/thanh-vien/mua` → đơn `THANH_VIEN` (loại `thanh_vien`) trong luồng nạp chung
  (`napTaoDon`: khoá idempotent, trần nạp tuần/tháng, chặn khi đã vĩnh viễn) → VietQR → SePay/Casso/mock →
  `napXacNhanThanhToan` **cấp ngay trong cùng transaction** (`tvCap`) và đơn chuyển thẳng `da_cong` — không cần
  vào game. Webhook mở cả khi `HTBB_NAP_BAT=false` miễn là đang bán thành viên.
- Hoàn tiền ở /admin/naptien: đơn thành viên → thu hồi đúng số ngày lần mua đó đã cấp (lần vĩnh viễn → thu hồi
  hẳn) → `da_thu_hoi`.
- Admin cấp / gia hạn / thu hồi tay (một hoặc **hàng loạt** ≤ 500 tài khoản cho tester), lý do bắt buộc.
- Mọi lần cấp/gia hạn/thu hồi: một dòng `tv_nhat_ky` (tác nhân, lý do, IP, mã đơn) + nhật ký admin.

## 5. Chống clone (tín hiệu rẻ, chỉ đọc)

/admin/thanh-vien › Nhiều tài khoản: mã máy (`tob_may_dang_nhap`, client gửi mỗi lần đăng nhập) dùng chung bởi ≥ N
tài khoản, kèm ai là thành viên; tra một tài khoản → các máy/IP nó dùng và tài khoản khác trên cùng máy/IP. Dùng
để xem xét (máy gia đình, quán net cũng chung máy), xử lý bằng Cấm theo máy. Không giới hạn số lần mua theo
máy/IP (đã phải trả tiền).

## 6. Bảng, file, phân quyền

- `sql/pokemon_thanh_vien.sql` (chạy SAU `pokemon_nap_tien.sql`, đã gom vào `tools/pokemon/xuat_sql_day_du.sh`):
  `tv_thanh_vien` (user_id, het_han_ms: 0 vĩnh viễn / <0 thu hồi / >0 hạn), `tv_nhat_ky`, gói `THANH_VIEN`
  (không bán), các khoá cau_hinh (TẮT). Chạy lại an toàn (INSERT IGNORE). Đã chạy 2 lần trên NovaDB.
- Server: `src/com/htbb/pokemon/ThanhVienLogic.java` (thuần), `ThanhVien.java` (cổng); móc `// thanh vien:` ở các
  file mục 3 + `server/CauHinh.java`; `NapTienLogic` nhận loại `thanh_vien`.
- Web: `server/utils/thanhVienThuan.ts`, `thanhVien.ts`; `/api/thanh-vien/{thong-tin,cua-toi,mua,don}`;
  `/api/admin/thanh-vien/{tong-quan,cau-hinh,danh-sach,cap,thu-hoi,nhat-ky,tin-hieu}`; trang `/thanh-vien`,
  `/admin/thanh-vien` (nhóm sidebar Nạp tiền), dòng trạng thái ở `/account` và `/kichhoat`.
- Quyền (`shared/utils/quyenAdmin.ts`): Hỗ trợ **xem** danh sách/nhật ký/tín hiệu; cấu hình, cấp, thu hồi: chỉ
  Siêu quản trị; Chỉ xem / Biên tập không vào.

## 7. Kiểm thử (18/10/2026)

- `test/KiemThanhVien.java` (69 kiểm, có trong `test/Kiem.java`): cấu hình, coQuyen, hạn/gia hạn/thu hồi, cổng
  `chan` (tắt = không chặn, bot không chặn, hết hạn chặn), đủ móc nối, SQL mặc định TẮT, hằng số khớp web.
- `HTBB-Web/tests/thanh-vien.test.ts` (node --test, cùng `npm test`: 53/53 cả bộ).
- Đầu-cuối trên CSDL nháp + web dev riêng + cổng mock (sandbox), phía game đọc **đúng bảng cau_hinh /
  tv_thanh_vien** qua `ThanhVien.chan`: **36/36** — mặc định tắt không chặn ai; chưa mở bán → 403; Hỗ trợ không
  sửa được cấu hình; mở bán (nạp lượng vẫn tắt) → mua → webhook → kích hoạt ngay; webhook lặp/8 giao dịch khác
  cho cùng đơn → chỉ cấp 1 lần; vĩnh viễn không mua lại; bật bắt buộc → thành viên được, clone bị chặn với lời
  nhắn; hoàn tiền → thu hồi → bị chặn lại; cấp hàng loạt, thu hồi; tín hiệu máy dùng chung 3 tài khoản; trả về TẮT.
- Ảnh: [đơn chờ thanh toán](anh_thanh_vien/tv-cho-thanh-toan.png), [điện thoại](anh_thanh_vien/tv-mobile.png),
  [đã kích hoạt](anh_thanh_vien/tv-thanh-cong.png), [admin](anh_thanh_vien/tv-admin.png).

### Kích hoạt — đầu-cuối trên máy chủ game RIÊNG (18/10/2026)

NovaDB riêng (cổng 3338, nạp `sql/htbb_day_du.sql`), login server riêng (8033) + game server riêng (19139) từ mã
hiện tại, `sv.lau_dai=1`, `sv.EnableAvtiveCheck=1`; bot nói giao thức thật (`tools/pokemon/kiem_tai/BotChucNang`):
**13/13** — tài khoản CHƯA kích hoạt đăng nhập, tạo Pokémon, vào bản đồ; nhận dòng nhắc; chat bản đồ không bị khoá;
mời giao dịch bị khoá với lời nhắn `/kichhoat`; tài khoản đã kích hoạt mời nó → "người chơi kia chưa kích hoạt";
bật `kich_hoat.chan_dang_nhap=1` lúc đang chạy → tài khoản chưa kích hoạt không vào được (hành vi cũ), Thành viên
chưa kích hoạt vẫn vào và giao dịch được; log game 0 exception. Ảnh: [/kichhoat](anh_thanh_vien/kh-kichhoat.png),
[/account](anh_thanh_vien/kh-account.png), [admin](anh_thanh_vien/kh-admin.png).

## 8. Bật khi mở game chính thức

1. Chạy `sql/pokemon_nap_tien.sql` rồi `sql/pokemon_thanh_vien.sql` trên `htbb_db`.
2. Cổng thanh toán theo [`NAP_TIEN.md`](NAP_TIEN.md) mục 6b (SePay) + `NAP_DA_CO_GIAY_PHEP=1`.
3. /admin/thanh-vien: chọn tính năng, giá, thời hạn, miễn cấp → bật **Mở BÁN** (kiểm hộp đỏ nếu chưa bán được) →
   khi thông báo mở chính thức, bật **BẮT BUỘC**. Server áp dụng sau ≤ 10 giây.
4. Cấp miễn phí cho tester/đối tác bằng tab Cấp / thu hồi (hàng loạt).
