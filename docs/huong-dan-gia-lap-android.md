# Chơi bản Java (.jar) màn NGANG trên Android: J2ME Loader / CoffeeCat

Bài hướng dẫn cho người chơi (đăng web / Zalo). Bản .jar chạy được trong giả lập J2ME trên Android và chơi màn
ngang có HUD cảm ứng giống bản APK: phím di chuyển, nút chiêu, nút AUTO, nút Menu.

---

**Huyền Thoại Bảo Bối - chơi bản Java màn ngang trên Android (J2ME Loader / CoffeeCat)**

1. Tải **J2ME Loader** (hoặc CoffeeCat) và tệp game:
   - **HTBB-...-opentest.jar** (bản thường: đủ hình Pokémon, có nhạc): dùng cho hầu hết điện thoại Android.
   - **HTBB-...-nhe.jar** (bản nhẹ: ít hình, không nhạc): chỉ dùng khi máy yếu / ít RAM hoặc bản thường bị giật,
     văng game.
2. Mở giả lập, bấm **+** để thêm tệp .jar. Đừng mở game vội: **nhấn giữ** biểu tượng game -> **Cài đặt** (Settings)
   để chỉnh riêng cho game này.
3. **Màn hình** (Screen):
   - **Độ phân giải** (Resolution): nên đặt **640 x 360**. Nếu muốn hình nét hơn thì đặt **1280 x 720**
     hoặc **1920 x 1080**, game tự phóng to giao diện cho dễ đọc. Đặt 800 x 480, 854 x 480 hay 960 x 540 vẫn chơi
     được nhưng chữ và nút sẽ nhỏ hơn.
   - **Tỉ lệ** (Scale type): **Vừa màn hình** (Fit screen). Bật **Giữ tỉ lệ** (Keep aspect ratio), đặt **Tỉ lệ
     phóng** = **100 %**, vị trí **Giữa** (Center).
   - **Hướng màn hình** (Orientation): **Ngang** (Landscape).
   - **Lọc ảnh** (Filter): nên **tắt** để hình pixel sắc nét.
   - Chế độ đồ hoạ: để mặc định (OpenGL ES / Hardware acceleration).
4. **Điều khiển** (Input / Keyboard):
   - **Cảm ứng** (Touch input): **BẬT**. Có bật thì game mới hiện phím di chuyển, nút chiêu, AUTO và Menu.
   - **Bàn phím ảo** (Show keyboard / Virtual keyboard): **TẮT**. Nếu muốn để lại thì chỉnh cho bàn phím ảo trong suốt
     hoặc tự ẩn. Bàn phím ảo đè lên nút trong game nên bấm hay bị nhầm. **Không** chọn kiểu bàn phím **"Điện thoại"**
     (Phone): kiểu này bắt giả lập về màn dọc.
5. Lưu cài đặt rồi mở game. Đang chơi mà xoay máy hoặc đổi độ phân giải thì game tự sắp xếp lại màn hình, không cần
   khởi động lại.

**Mẹo**
- Màn **dọc** (ví dụ 360 x 640, 720 x 1280) vẫn chơi được, nhưng HUD giống APK chỉ có ở màn **ngang**.
- Muốn quay về điều khiển bằng phím thì tắt Cảm ứng và bật Bàn phím ảo: phím 2/4/6/8 để đi, 5 để đánh / nói chuyện,
  phím mềm trái để mở Menu.
- Máy bị giật: hạ độ phân giải về **640 x 360** (đỡ tốn máy nhất). Nếu vẫn giật thì dùng bản **nhẹ**.
- Ở độ phân giải để trống / "theo màn hình" (giả lập lấy đúng số điểm ảnh của máy), game vẫn tự phóng to giao diện.
  Tuy vậy đặt hẳn **640 x 360** hoặc **1280 x 720** cho hình ổn định hơn.

---

## Ghi chú kỹ thuật (cho đội phát triển)

- J2ME Loader (`ru.playsoftware.j2meloader`, mã nguồn mở; CoffeeCat là bản dẫn xuất) cho mỗi game một hồ sơ. Màn ảo
  `ScreenWidth` x `ScreenHeight` mặc định là **240 x 320**, kiểu phóng 1 = vừa màn, hướng 0 = theo hệ thống,
  `TouchInput` = bật, `ShowKeyboard` = bật. Nếu đặt cả rộng lẫn cao thì cỡ Canvas **cố định**: xoay máy chỉ phóng lại
  ảnh, `sizeChanged` không đổi cỡ. Nếu để trống một cạnh (hoặc cả hai = đúng cỡ màn máy) thì xoay máy làm đổi cỡ, giả lập
  gửi `sizeChanged(w, h)` mới. `hasPointerEvents()` = ô Cảm ứng. Bàn phím ảo kiểu "Phone" ép màn dọc.
- Thuộc tính MANIFEST / JAD: J2ME Loader **chỉ đọc** `Nokia-UI-Enhancement` (tìm `EnableMultiPointTouchEvents`
  để bật đa chạm). Nó **bỏ qua** `Nokia-MIDlet-Original-Display-Size`, `Nokia-MIDlet-Target-Display-Size`,
  `Nokia-MIDlet-App-Orientation`, `Nokia-MIDlet-On-Screen-Keypad`, `MIDlet-Touch-Support`. Vì vậy **không có thuộc
  tính nào làm giả lập tự chọn màn ngang**, người chơi phải chỉnh tay theo bài trên. Jar chỉ thêm
  `MIDlet-Touch-Support: True` (vô hại, chỉ để thông tin). Không thêm `EnableMultiPointTouchEvents`, vì ngón thứ
  hai sẽ ghi đè toạ độ chạm của ngón đầu. Không thêm `Original-Display-Size` (Symbian sẽ phóng Canvas từ cỡ đó), không
  thêm `On-Screen-Keypad` / `App-Orientation` (đổi cách chạy trên máy Symbian cảm ứng thật).
- Game (`Canvas.heSo`, 0.9.22): màn ảo có cạnh ngắn >= 684 px thì game vẽ ở màn logic cạnh ngắn ~360 (1280x720 ->
  640x360 x2, 1920x1080 -> x3, 720x1280 -> 360x640 x2), rồi phóng nguyên bằng `getRGB` / `drawRGB` theo dải và chia
  toạ độ chạm cho `heSo`. Màn nhỏ hơn và máy J2ME thật vẽ thẳng x1 như cũ. Phóng tốn CPU (J2ME Loader cấp phát mảng mới
  ở mỗi lần `drawRGB`), nên 640 x 360 vẫn là cỡ khuyên dùng: giả lập tự phóng bằng GPU.
- Máy J2ME cảm ứng màn ngang (HudRong): `VungCham.MIN` = 16 px logic, như bản PC 1280x720.
