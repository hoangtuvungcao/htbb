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
Công cụ đóng gói nguyên ảnh gốc, chia vùng vẽ qua metadata; nguồn chỉ có
hai khung nhìn trước nên chưa có động tác quay lưng, chạy, đánh riêng.
Có thể thay các tài nguyên này khi có đủ bộ chuyển động.
Công cụ tái tạo: tools/TaoThoiTrangTrungThu2026.java.

Triển khai: cập nhật JAR cùng các file map/icon/866..869.png và
map/c/{head/84,head/85,body/81,body/82,leg/81,leg/82}, nạp
sql/them_thoi_trang_trung_thu_2026.sql vào DB đích rồi khởi động lại server.
Kiểm tra ID 912/913 chưa được dùng cho vật phẩm khác trước khi nạp.
Migration dùng INSERT IGNORE để chạy lại không ghi đè vật phẩm đang có.
Server mới dùng sql/gamedb7_clean.sql đã có hai dòng này.

Kiểm tra trong game: nhận hai item bằng công cụ quản trị; thử đúng/sai
giới tính, mặc/tháo, mở bảng trang bị, di chuyển bốn hướng và cưỡi thú.
