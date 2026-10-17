# R8 cho bản phát hành: rút gọn + tối ưu + đổi tên code (khó dịch ngược hơn).
# Bảng tên cũ <-> mới: app/build/outputs/mapping/release/mapping.txt (build.sh chép ra out/mapping-BAN.txt) -
# GIỮ RIÊNG, KHÔNG phát kèm, KHÔNG đưa lên git (cần nó để đọc stack trace lỗi từ máy người chơi).

# Game tra ảnh/tài nguyên theo TÊN qua reflection: Image.getDrawableID -> R.drawable.class.getField(ten)
-keep class com.htbb.client.R$drawable { public static <fields>; }

# Giữ số dòng trong stack trace (tên tệp ẩn đi), đọc lại bằng mapping
-keepattributes LineNumberTable
-renamesourcefileattribute SourceFile
