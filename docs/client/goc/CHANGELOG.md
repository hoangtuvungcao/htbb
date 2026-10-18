# Nhật ký phiên bản — Huyền Thoại Bảo Bối

Huyền Thoại Bảo Bối là game fan **phi thương mại**, không liên kết và không được bảo trợ bởi Nintendo,
Creatures, GAME FREAK hay The Pokémon Company. Không nạp tiền, không quảng cáo.

Mỗi bản có **tên phiên bản** (vd `0.9.0-opentest`) và **số bản** (vd `900`). Khi báo lỗi, hãy gửi kèm cả hai:
xem ở màn **Ghi công** (Menu ở màn đăng nhập trên điện thoại / nút "Ghi công" dưới khung đăng nhập trên PC).

<!-- Mẫu cho bản sau (chủ server: jar / APK / PC cùng một số bản; sửa tools/phat-hanh/phien_ban.conf trước khi build):
## <phiên bản> (số bản <N>) — <yyyy-mm-dd>
Máy chủ: <host:cổng>   Bắt buộc cập nhật: có/không (sv.banToiThieu=<N>)
### Mới
### Sửa lỗi
### Cân bằng
### Lỗi còn biết
-->

## Bản sau 0.9.0 (chưa phát hành)

### Mới
- **Tự động đánh chính thức** (máy chủ điều khiển): mở từ Menu nhanh / phím # / menu chính. Chọn thứ tự 4 chiêu,
  ngưỡng thuốc HP / MP, nhặt đồ, về hồi phục, vùng đánh, tự hồi sinh; bảng hiện giờ còn lại hôm nay (mặc định 5 giờ /
  ngày, +2 giờ khi có thẻ tháng). Chỉ chạy khi online, chỉ ở bãi luyện tập, EXP / đồ rơi theo năng lượng như đánh tay;
  tự dừng khi hết giờ, PvP, sàn đấu / Gym / boss, đổi bản đồ hay đi xa. Vé treo máy (offline) giữ nguyên.
- **Bỏ các mod tự động cũ**: Luyện cấp / Tự PK / Mã PK / Nhảy vùng / Giao hàng tự động / Vận may / Lên Gym / chế đồ
  nhanh / dòng ẩn, mở Pidgeot (Bay) từ xa, tự đăng nhập lại, tự mua, tầm chiêu 300, về làng không chờ.
- **Thuật ngữ Pokémon thống nhất** ở mọi màn hình, thông báo, vật phẩm, nhiệm vụ và trang web (bảng đầy đủ:
  `HTBB/docs/pokemon/THUAT_NGU.md`): bang hội → **đội**, chiếm thành → **Chiếm Gym**, vận tiêu → **giao hàng**,
  liên đấu → **giải Liên minh** (sân Liên minh), lôi đài → **sàn đấu**, linh thú / ngựa → **thú cưỡi**,
  phi phong → **ruy băng**, vũ khí → **đồ cầm tay**, thần binh / thần khí → **bảo vật** / **bảo vật huyền thoại**,
  nội lực → **năng lượng**, nhân vật → **Pokémon**, cao thủ → **nhà vô địch** (Top sức mạnh), ngũ hành → **hệ**
  (hệ Thép, Cỏ, Nước, Lửa, Đất). Tên chiêu trên điện thoại Java/Android cũng dùng tên chiêu Pokémon.

### Sửa lỗi
- Điện thoại Java / Android: Pokémon hoang dã không hiện trên bản đồ (lỗi đọc gói vị trí) — đã sửa.

## 0.9.0-opentest (số bản 900) — Open test công khai

Bản đầu tiên mở cho mọi người chơi thử. Có ba bản cài, chơi chung một máy chủ:

| Bản | Tệp | Dành cho |
|---|---|---|
| Điện thoại Java (J2ME) | `HTBB-0.9.0-opentest-thuong.jar` | máy Java có bộ nhớ khá |
| Điện thoại Java, bản nhẹ | `HTBB-0.9.0-opentest-nhe.jar` | máy Java yếu (ít khung hình, chân dung nhỏ, chỉ tiếng hiệu ứng, không nhạc nền) |
| Android | `HTBB-0.9.0-opentest-android.apk` | Android 8.0 trở lên, màn hình ngang |
| Máy tính Windows | `HTBB-0.9.0-opentest-PC.zip` | Windows 64-bit; giải nén rồi chạy `HuyenThoaiBaoBoi.exe` |

Chỉ tải game từ trang chính thức của game. Không có bản trên CH Play / App Store.

### Có gì trong bản này

- **Thế giới Pokémon**: chọn một trong 5 Pokémon khởi đầu, Pokémon của bạn, quái và boss mang hình Pokémon (hình
  PMDCollab), bản đồ vẽ lại theo phong cách Pokémon, tên bản đồ và chữ trong game theo thế giới Pokémon.
- **Hệ và khắc hệ**: mỗi Pokémon có hệ (Lửa, Nước, Cỏ, …); đòn khắc hệ mạnh hơn, hiện màu riêng trên số sát thương.
- **Chiến đấu sinh động hơn**: hiệu ứng chiêu theo hệ, số sát thương bay có viền (màu khác khi chí mạng / khắc hệ),
  chớp sáng và rung màn khi trúng đòn, vùng cảnh báo trước đòn của boss, hiệu ứng lên cấp và tiến hoá.
- **Giao diện mới kiểu Pokémon**: hộp thoại, thanh máu, thanh chiêu, hành trang, rương, trang bị và cửa hàng
  vẽ lại; Menu nhanh không che màn hình; chữ hoa tiếng Việt có dấu hiển thị đúng trên điện thoại Java.
- **Âm thanh**: nhạc nền chiptune và tiếng hiệu ứng mới; bật/tắt nhạc, tiếng, chỉnh âm lượng trong Menu nhanh.
- **Màn đăng nhập mới**: tranh nền nhiều lớp chuyển động, hàng Pokémon khởi đầu.
- **Màn "Ghi công"**: danh sách hoạ sĩ PMDCollab vẽ hình Pokémon (theo từng loài), nguồn hiệu ứng, bản đồ, âm
  thanh, và phiên bản game đang chạy.
- Game tự kết nối đúng máy chủ open test, không cần chọn máy chủ.

### Lưu ý cho người chơi thử

- **Sau open test, toàn bộ Pokémon của người chơi sẽ bị xoá (wipe)**; tài khoản được giữ để chơi bản chính thức.
- Bản PC **chưa tự cập nhật** trong open test: khi có bản mới, tải lại từ trang chủ. Bản Java và Android luôn
  phải tải lại bản mới (game sẽ báo khi bản của bạn quá cũ).
- Đăng ký: từ 13 tuổi; dưới 16 tuổi cần cha mẹ đồng ý. **Không dùng lại mật khẩu** đang dùng ở nơi khác.
- Dùng tool / hack, lợi dụng lỗi, mạo danh GM: khoá tài khoản.

### Lỗi còn biết

- Một vài chữ HOA có dấu hiếm gặp có thể hiện chưa đúng trên điện thoại Java.
- Bản nhẹ thiếu hình một số loài Pokémon ít gặp (hiện bóng đen thay thế).
- Bản Android chưa có trên cửa hàng ứng dụng: cần cho phép "Cài ứng dụng không rõ nguồn gốc".

Báo lỗi qua kênh hỗ trợ trên trang chủ: tên Pokémon, giờ, bản đồ, nền tảng + phiên bản/số bản, ảnh hoặc clip.
