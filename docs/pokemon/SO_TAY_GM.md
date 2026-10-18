# Sổ tay GM — Huyền Thoại Bảo Bối (open test)

Dành cho người được giao quyền GM trong đợt open test công khai. Đọc hết trước khi nhận quyền.
Tổng quan sẵn sàng production: [SAN_SANG_PRODUCTION.md](SAN_SANG_PRODUCTION.md).

## 1. Quyền GM hoạt động thế nào

- Quyền gắn với **tên nhân vật**, không phải tài khoản: tên phải nằm trong `sv.adminAccounts`
  (bảng `cau_hinh`, sửa ở web `/admin/cauhinh` hoặc `vps/server.ini.tmpl` lần đầu).
- Khi chủ server bật `sv.gmCanCoPInfo=1` (khuyến nghị cho open test), nhân vật còn phải có
  `pInfo[16]=1` trong `tob_char`. Danh sách tên mà người chơi trùng thì **không** thành GM.
- Người chơi **không tạo được** nhân vật trùng tên trong danh sách hoặc chứa chữ `admin`
  (`Database.laTenDanhRieng`). Muốn tạo nhân vật GM mới: chủ server bật tạm `sv.choPhepTaoTenGM=1`,
  tạo xong tắt ngay.
- Mọi lệnh chat GM được ghi vào `AdminChat.txt`, mọi lệnh gói 47 vào `AdminChat_lenh47.txt`
  (thư mục `/opt/kpah`, xoay vòng bởi logrotate). **Mọi thứ bạn gõ đều có dấu vết.**

## 2. Lệnh được dùng trong open test

| Lệnh (gõ ở ô chat) | Tác dụng | Khi nào |
|---|---|---|
| `kick <tên>` | Đá nhân vật ra (dữ liệu được lưu) | Người chơi kẹt, spam, gây rối |
| `getchar <tên>` | Xem chỉ số nhân vật đang online | Điều tra báo cáo gian lận |
| `mto <map> <x> <y>` | Dịch chuyển chính mình | Đến chỗ người chơi báo lỗi |
| `tgct` | Xem giờ chiếm thành đang đặt | — |
| `setting` / `setting menu` | Bảng cài đặt (tỉ lệ, nạp lại server.ini) | **Chỉ chủ server** |
| `stown …`, `ktct` | Đặt/huỷ giờ chiếm thành | **Chỉ chủ server**, báo trước trên kênh |

## 3. Lệnh CẤM dùng trong open test

Mọi lệnh tạo ra giá trị trong game: `slv` (lên cấp), `pet`, `gem`, `ngoc`, `qua*`, `ruong*`, `att`,
`dvip`, `rsvip`, `tlixi`, `fna`, `spt`/`sph`/`spe`… và các lệnh bật/tắt boss/sự kiện (`openboss*`,
`offboss*`, `onx2`…). Lý do: open test đo kinh tế thật (xem `docs/pokemon/KIEM_TOAN_KINH_TE.md`);
một lần phát đồ làm sai số liệu và tạo tiền lệ "xin GM". Cần trao thưởng → dùng **giftcode** tạo ở
web `/admin/giftcode` (có log, giới hạn lượt) sau khi chủ server duyệt.

Lệnh `ban <tên>` trong game **không có tác dụng** (`Database.banChar(String,int)` đã bị vô hiệu) —
đừng tin là đã khoá. Khoá đúng cách ở mục 4.

## 4. Xử lý vi phạm

| Mức | Hành vi | Xử lý | Công cụ |
|---|---|---|---|
| 1 | Spam chat, ngôn từ xấu | Nhắc nhở → `kick` | chat |
| 2 | Lợi dụng lỗi (bug), quấy rối lặp lại | Khoá tài khoản 3 ngày, thu hồi đồ do lỗi | web `/admin/taikhoan`, `/admin/nhanvat` |
| 3 | Hack/tool, gian lận giao dịch, mạo danh GM | Khoá vĩnh viễn tài khoản + khoá máy | web `/admin/taikhoan` + `/admin/cammay` |

- Luôn ghi lý do vào nhật ký (web `/admin/nhatky` lưu thao tác admin) và chụp màn hình bằng chứng.
- Nghi gian lận: xem `docs/nghi-van-gian-lan.md` và bảng `nghi_van_gian_lan` (server tự ghi ngập gói,
  tiền âm…).
- Không bao giờ hỏi mật khẩu người chơi. GM thật không cần mật khẩu để làm gì cả.

## 5. Kháng cáo (ban appeal)

1. Người chơi gửi kháng cáo qua kênh hỗ trợ chính thức (mục 6) — **không** qua chat game.
2. GM **khác** với người đã khoá xem lại bằng chứng trong vòng 72 giờ.
3. Kết quả: giữ nguyên / giảm / gỡ. Gỡ khoá → ghi lý do vào `/admin/nhatky`.
4. Mỗi lần khoá chỉ được kháng cáo một lần.

## 6. Kênh hỗ trợ và báo lỗi

Game **chưa có** nút báo lỗi trong client. Trong open test dùng:

- Kênh cộng đồng chính thức (Discord/Zalo/Facebook — chủ server chọn, ghi link ở trang chủ và
  trong thông báo đăng nhập `sv.website`).
- Mẫu báo lỗi ghim sẵn: *tên nhân vật · thời gian (giờ:phút) · bản đồ · nền tảng (jar/APK/PC + số bản)
  · mô tả · ảnh/clip*. Giờ + tên nhân vật đủ để tra log `[CLIENT]` trong `game.out`.
- GM gom lỗi vào danh sách "Lỗi đã biết" (mục 5 của `SAN_SANG_PRODUCTION.md`) mỗi ngày.

## 7. Bảo trì

- Báo trước trên kênh cộng đồng ≥ 30 phút (trừ sự cố).
- Chặn đăng nhập mới: tạo `/opt/kpah/bao_tri.txt` (dòng đầu = câu báo người chơi, dòng
  `cho: tentaikhoan1 tentaikhoan2` = tài khoản GM vẫn vào được). Xoá tệp = mở lại.
- Tắt hẳn: `sudo systemctl stop kpah-game` (shutdown hook lưu toàn bộ nhân vật, chờ tối đa 180 s).
