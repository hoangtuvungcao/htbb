# Thời trang Trung Thu 2026

| ID vật phẩm | Tên | Avatar | Icon hành trang |
|---|---|---|---|
| 912 | Trung Thu 2026 Nam | map/icon/866.png | map/icon/868.png |
| 913 | Trung Thu 2026 Nữ | map/icon/867.png | map/icon/869.png |

Chỉ số theo Hắc Lang (723) / Thần Nữ (724): công +10%, HP +5.000,
MP +5.000, tăng 50% băng/sét/độc/lửa lan. Giữ hiệu ứng tương ứng của
hai mẫu; không có giảm sát thương của Bạch Mi/Thúy Vũ.
Thời hạn mặc định trong template: 10.080 phút, giống hai bộ mẫu.
Không tự thêm vào cửa hàng hoặc phát cho người chơi.

Hình ngoài bản đồ dùng các phần đầu 84/85, thân 81/82, chân 81/82.
Đã có hình trước/sau/ngang và các tư thế di chuyển; xem chi tiết bên dưới.
Công cụ tái tạo: tools/TaoThoiTrangTrungThu2026.java.

Triển khai: cập nhật JAR cùng các file map/icon/866..869.png và
map/c/{head/84,head/85,body/81,body/82,leg/81,leg/82}, nạp
sql/them_thoi_trang_trung_thu_2026.sql vào DB đích rồi khởi động lại server.
Kiểm tra ID 912/913 chưa được dùng cho vật phẩm khác trước khi nạp.
Migration dùng INSERT IGNORE để chạy lại không ghi đè vật phẩm đang có.
Server mới dùng sql/gamedb7_clean.sql đã có hai dòng này.

Kiểm tra trong game: nhận hai item bằng công cụ quản trị; thử đúng/sai
giới tính, mặc/tháo, mở bảng trang bị, di chuyển bốn hướng và cưỡi thú.

## Chuyển động theo Hắc Lang / Thần Nữ

Nam dùng chuyển động Hắc Lang: đầu map/c/head/76, thân map/c/body/73,
chân map/c/leg/73. Nữ dùng Thần Nữ: đầu 77, thân 74, chân 74.

Công cụ đọc trực tiếp metadata gốc mỗi lần chạy: lấy thay đổi vị trí X/Y
của cả sáu frame theo từng hướng, cùng thay đổi rộng/cao của từng tư thế.
Điểm neo được bù theo kích thước trang phục Trung Thu; vùng nối các phần
chồng nhẹ để tránh hở khi nhún. Không chép hình Hắc Lang/Thần Nữ vào bộ mới.
Hình Trung Thu vẫn lấy từ nam-source.png/nu-source.png đã dựng ở bước trước;
đây là ghép chuyển động bộ gốc lên hình mới, không phải sao chép pixel các
tư thế Hắc Lang/Thần Nữ. Chưa thay bằng bộ tranh động tác vẽ tay riêng.

Client dùng PART_OF_FRAME: 0/1 -> tư thế 0, 2 -> tư thế 1, 3 -> tư thế 2,
4 -> tư thế 3, 5 -> tư thế 4. Hướng phải lật hướng trái theo client.
Đầu cố định hình theo hướng nhưng nhún đúng các offset của bộ mẫu.
Thứ tự chạy/đánh vẫn do client và kỹ năng từng phái quyết định.

Xem trước: map/trungthu2026/xem-chuyen-dong.html.
Bảng nam-motion.png/nu-motion.png được dựng lại bằng cách đọc chính sáu
file đã đóng gói, có đủ bốn hướng và sáu frame, kể cả nhịp nhún đầu.
nam-frames.png/nu-frames.png chỉ là bảng hình trung gian trước ghép chuyển động.
Nguồn vẽ trước đó và prompt vẫn lưu trong map/trungthu2026/prompts.md.

Nếu server đã có hai bộ, cập nhật sáu file head/84,85; body/81,82; leg/81,82,
rồi khởi động lại server/client để nạp lại ảnh. Không cần SQL hoặc JAR mới.
Nếu ảnh còn bị cache, xóa bộ đệm tài nguyên của client rồi đăng nhập lại.
Kiểm thử đối chiếu trực tiếp mọi delta vị trí/kích thước với tài nguyên bộ
mẫu. Cần kiểm tra trong game khi chạy, đánh và cưỡi thú.

## Căn kích thước và vị trí: bản đối chiếu đã sửa

Bản xem trước dùng ô 128x144, điểm gốc (64,104), còn khoảng trống dưới y=0.
Công cụ báo lỗi nếu bất kỳ pixel nhìn thấy nào ra ngoài ô, thay vì bỏ pixel.
So sánh đồng thời Hắc Lang/Thần Nữ và Trung Thu cùng tỷ lệ, có dấu gốc.
Có nút dừng và chuyển khung để kiểm tra cả bốn hướng.

Số đo cũ 33/28 px bị ảnh hưởng bởi việc cắt mép dưới. Đo lại toàn thân đứng:
Hắc Lang 36 px, Thần Nữ 31 px. Hình đi bộ Trung Thu được thu theo số đo mới.

Khi cưỡi, đầu và thân được căn độc lập theo vùng nhìn thấy của từng phần
bộ mẫu, gồm kích thước, điểm đặt và biến đổi theo khung. Không còn dịch toàn
bộ đầu theo mép dưới áo. Bản đối chiếu cưỡi của cả hai bên đều chỉ vẽ đầu/thân,
không có ngựa; không dùng hình toàn thân bộ mẫu để đối chiếu với nửa người mới.
Hình bộ mới khác tỷ lệ nguyên bản: bước căn theo bộ mẫu có điều chỉnh tỷ lệ
từng phần. Cần duyệt trực tiếp trong game với từng loại thú.

Tài nguyên cưỡi: đầu 86/87, thân 83/84. Đi bộ: đầu 84/85, thân 81/82,
chân 81/82. Server chọn tài nguyên riêng khi đang cưỡi và khôi phục khi xuống.
Cần dùng bản JAR có hỗ trợ chuyển ID đã build ở bước trước cùng tài nguyên mới.
Không thay SQL. Kiểm thử đối chiếu vùng pixel thật cả đầu/thân với bộ mẫu,
cho phép sai số làm tròn 1 px, và kiểm tra lên/xuống thú.