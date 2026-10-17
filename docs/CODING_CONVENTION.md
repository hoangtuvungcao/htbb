# Coding convention HTBB — rút ra từ các bug đã sửa

Tài liệu này KHÔNG phải nguyên tắc chung chung. Mỗi mục dưới đây gắn với ít nhất
1 bug thật đã xảy ra trên chính dự án này (xem cột "Đã từng gây ra"), và quy tắc
được viết đủ cụ thể để áp dụng thẳng vào code Java/PHP/MySQL của HTBB. Cập nhật
file này mỗi khi phát hiện một pattern lỗi mới lặp lại lần thứ 2.

Trước khi đóng bất kỳ task code nào (tính năng mới, sửa bug, chỉnh số liệu),
đối chiếu qua checklist ở cuối file.

---

## 1. Số hiệu dùng chung một không gian toàn cục — luôn `grep` trước khi gán

Các mảng/chỉ số kiểu `otherAtt[N]`, id hiệu ứng `map/d_effect/<n>` (dải `8700+N`),
hằng số `type` dùng chung 1 hàm dispatch lớn (`doBuyItemShopNew`, `doBuyItemShop`,
switch theo `idNpc`/`idOptionMenu`)... không có tài liệu tập trung, ý nghĩa/đơn vị
chỉ suy ra được từ nơi khác đang đọc nó.

**Đã từng gây ra:** `TYPE_SHOP_NEW_TRUNG_THU=3` trùng với nhánh `else if (type==3)`
có sẵn (nâng cấp trang bị 860-871) → mua Shop Trung Thu báo "Vật phẩm không hợp lệ".
`otherAtt[54]` của Kim Cang Long roll sai đơn vị (thật vs phần mười %) vì không đối
chiếu với "chủ cũ" của ô đó. Gói 26 tràn 255 phần tử (client đọc 1 byte) khi thêm
hiệu ứng Sét đánh (id 131) vào dải đã gần đầy.

**Quy tắc:**
- Trước khi gán 1 giá trị mới vào không gian số hiệu dùng chung, `grep` **toàn bộ
  `src/`** theo đúng số hiệu/tên hằng số đó để liệt kê hết các giá trị đang chiếm
  chỗ, rồi mới chọn giá trị trống.
- Nếu không gian đó có giới hạn cứng phía client (đọc bằng 1 byte → tối đa 255,
  mảng độ dài cố định...), phải đếm tổng số phần tử hiện tại so với giới hạn
  trước khi thêm — không giả định "còn dư chỗ".
- Nếu 2 ô cạnh nhau trong cùng 1 mảng có thể khác đơn vị/thang đo (phần trăm thật
  vs phần nghìn vs phần mười phần trăm), xác nhận đơn vị bằng cách đọc code ở
  CẢ nơi ghi lẫn nơi đọc, không suy đoán theo tên biến.

## 2. Đừng suy đoán 2 hệ thống giống tên là cùng một nguồn

**Đã từng gây ra:** Nhầm hệ thống shop khi làm Shop Trung Thu (tưởng "Khác > Shop
Đặc Biệt" (`all_item_shop_special`, yêu cầu VIP) là nơi cần thêm mặt nạ, phải làm
lại toàn bộ bằng 1 hệ thống mới). "Tụ hồn đan" là 2 item khác hệ trùng tên
(`Item.TU_HON_DAN=714` không xếp chồng vs `Potion` 83 xếp chồng). `map/iconclan/{id}.png`
(logo bang) vs `map/iconclan.png` (sprite sheet khác hẳn). 2 định dạng file lẫn
lộn trong cùng thư mục `map/d_effect/` (part 5 byte vs 7 byte) — đọc nhầm không
báo lỗi, chỉ ra rác.

**Quy tắc:**
- Khi 2 tài nguyên/hằng số/thư mục có tên giống nhau hoặc nằm cạnh nhau, xác nhận
  chúng là MỘT nguồn bằng cách đọc code định nghĩa (constructor, cột DB thật qua
  `SELECT`), không suy đoán qua tên gọi.
- Khi tìm ra 1 cặp dễ nhầm như vậy, viết ngay 1 dòng comment tại đầu file/hàm liên
  quan dạng `// KHONG nham voi X, xem Y` để người sau (kể cả AI) không lặp lại.

## 3. Một nghiệp vụ có nhiều bản sao (class chị em, hàm song song) — sửa 1 chỗ luôn nghi ngờ còn chỗ khác

**Đã từng gây ra:** `saveChar` có lưu 3 bảng phụ (tứ bình, vật phẩm đặc biệt, cây
ăn quả) mà `saveCharAuto` thiếu → mất dữ liệu khi crash giữa phiên. `canDatLaiDemNguocGiaiTan()`
cùng 1 bug (gán `timeStartDel` thô) xảy ra 2 lần ở 2 nhánh khác nhau trước khi gộp
thành 1 hàm dùng ở 5 nơi. `Edit replace_all` tưởng sửa 2 hàm cộng exp giống nhau
nhưng lệch thụt đầu dòng nên chỉ khớp 1/2. Thiếu `continue` sau `mt.isDead` lặp lại
ở nhiều file `Map*.java` (`MapDanhNhau`, `Region`...) trong khi file chị em khác đã
đúng từ trước.

**Quy tắc:**
- Khi sửa hành vi thuộc kiến trúc "nhiều class/hàm chị em cùng làm 1 việc" (họ
  `Map*.java`, cặp `saveX`/`saveXAuto`, các hàm cộng exp/level song song), `grep`
  tên hàm hoặc đoạn logic giống hệt trên toàn `src/` để liệt kê HẾT bản sao trước
  khi coi là xong.
- Không tin `replace_all` đã sửa hết — đếm lại số lần match bằng `grep -c` sau khi
  sửa, đối chiếu với số lượng đã liệt kê ở bước trên.

## 4. Thao tác tiền/vật phẩm dùng chung giữa nhiều luồng — không check-rồi-act rời rạc

Kiến trúc HTBB: lệnh trong `RealController.privateCmd` chạy trên luồng phiên
(session thread); phần lớn lệnh khác (mua shop dạng -76, giao dịch gói 66, tổ
đội, kho...) chạy trên luồng bản đồ (map/region thread). Cùng 1 `Char` có thể bị
sửa `iItems`/`potions[]`/xu/lượng từ 2 luồng gần như đồng thời.

**Đã từng gây ra:** Dupe thuốc/thẻ vận tiêu qua giao dịch (đã vá bằng
`Char.KHOA_GIAO_DICH` + `GiaoDichThuoc`). `Char.doCreatePet()` thiếu `synchronized`
làm mất pet khi client gửi gói trùng. Nhặt vật rơi trên đất bị nhặt đôi giữa gói
tay và vòng tự nhặt.

**Quy tắc:**
- Mọi handler đổi tiền/vật phẩm có thể chạy trên nhiều luồng: dùng lock đã có sẵn
  đúng phạm vi (`Char.KHOA_TIEN` qua `subXu`/`subLuong`, `Char.KHOA_GIAO_DICH`...)
  bọc TOÀN BỘ chuỗi "kiểm tra đủ → trừ → cộng", không chỉ bọc riêng bước trừ tiền.
  `subLuong()`/`subXu()` tự khoá bước trừ, nhưng KHÔNG khoá được toàn chuỗi
  check-rồi-act nếu code gọi chúng tách rời khỏi bước kiểm tra ban đầu.
- Với lệnh không nằm trong `RealController.privateCmd` (chạy trên luồng
  map/region), khi thêm 1 nhánh mua/tiêu tốn tài nguyên mới, đối chiếu xem có
  đang cùng khoá với 1 lệnh khác (kể cả lệnh nằm trong `privateCmd`) có thể sửa
  cùng field trên cùng player hay không.
- Bất kỳ `Item`/object nào được giữ tham chiếu giữa 2 gói tin (mở popup → chờ
  client xác nhận) phải `iItems.contains(item)` lại NGAY TRƯỚC khi dùng, không
  giả định tham chiếu Java còn hợp lệ.

## 5. Thứ tự "đánh dấu đã dùng trước, phát thưởng sau" — không đảo ngược

**Đã từng gây ra:** Một số luồng cấp thưởng cũ (gift code, top xếp hạng, xổ số)
phát thưởng trước rồi mới ghi "đã dùng" — exception giữa chừng làm bước ghi nhận
không chạy nhưng thưởng đã phát, gây lặp thưởng.

**Quy tắc:** Nghiệp vụ "tiêu 1 tài nguyên có hạn rồi phát thưởng" luôn viết theo
thứ tự: (1) đánh dấu đã dùng/trừ tài nguyên bằng câu có điều kiện, kiểm tra đã
áp dụng thành công; (2) chỉ phát thưởng nếu bước (1) xác nhận OK. Không dựa vào
try/catch bên ngoài để coi là "đủ an toàn".

## 6. Thêm loại thực thể mới → rà hết mảng hằng số cùng trục chỉ số

**Đã từng gây ra:** `Animal.addMoreSP/addMoreBasic/pcTN` thiếu 6 loại thú mới →
AIOOBE ngay khi luyện. `Pet.MATERIAL_UPDGRADE[5]` thiếu trong khi
`PC_UPDGRADE_OK[5]` đã có. 5 skin thú cưỡi dùng chung 1 dòng toạ độ ngồi, riêng
Tiên Lộc thân thấp hơn lộ sai lệch rõ.

**Quy tắc:** Khi thêm 1 loại thực thể mới (thú, pet, skin, level cap, mặt nạ...)
vào hệ có sẵn, `grep` toàn bộ mảng song song cùng trục chỉ số (không chỉ mảng
được yêu cầu ban đầu) — các mảng song song trong HTBB KHÔNG có kiểm tra độ dài
lẫn nhau, sai lệch chỉ lộ ra khi đúng loại thực thể đó được dùng tới.

## 7. Encoding UTF-8 khi thao tác DB qua dòng lệnh

**Đã từng gây ra:** `mysql -e "UPDATE ... SET name='Mặt nạ...'"` qua Bash làm
mất dấu tiếng Việt dù đã thêm `--default-character-set=utf8`.

**Quy tắc:** Không bao giờ UPDATE/INSERT text tiếng Việt bằng `mysql -e` inline
qua Bash. Luôn viết ra file `.sql` bắt đầu bằng `SET NAMES utf8;`, chạy bằng
`mysql ... < file.sql`, rồi `SELECT` lại để xác nhận UTF-8 đúng trước khi báo hoàn
thành.

## 8. Build/test/chạy Java trên máy này CHỈ qua PowerShell

**Đã từng gây ra:** `build.bat` qua Git Bash treo hàng giờ vì `find /c /v ""`
trong .bat bị Git Bash hiểu nhầm sang `find` của MSYS (quét cả ổ C:). Chạy
`java -cp ...` qua Bash báo "Could not find or load main class" dù class tồn tại
(do jar có `Class-Path` trong MANIFEST xử lý khác nhau giữa 2 shell). `java` gọi
qua PATH launcher ra PID khác với PID thật đang bind cổng.

**Quy tắc:**
- Mọi `.bat` (build/test) và mọi `java -cp ... <MainClass>` PHẢI chạy qua
  PowerShell, không bao giờ qua Bash/Git Bash trên máy Windows này.
- Khi launch server, luôn gọi thẳng đường dẫn đầy đủ
  `"C:\Program Files\Java\jdk-26.0.1\bin\java.exe"`, không gọi `java` trần qua
  PATH. Sau khi launch, xác nhận PID thật bằng
  `Get-CimInstance Win32_Process -Filter "..."` + `netstat -ano | findstr "19129"`,
  không tin PID mà `Start-Process` trả về là chắc chắn đúng.

## 9. Refactor đổi tần suất gọi hoặc thêm dòng dữ liệu → rà lại giả định ngầm cũ

**Đã từng gây ra:** `getAllTopBegin()` trước chỉ chạy 1 lần lúc khởi động; thêm
vòng lặp 60s làm lộ bug cũ `topLvNation.add(new Vector())` luôn thêm thay vì gán
đè → phình vô hạn. `kickOutTown()` dựa vào `myCountry` chỉ đồng bộ ở 1 nơi (login);
thêm tính năng "duyệt vào bang có sẵn giữa phiên" phá giả định đó.

**Quy tắc:** Khi (a) đổi 1 hàm từ "chạy 1 lần lúc khởi động" sang "chạy lặp lại
định kỳ", hoặc (b) thêm 1 dòng/nhánh mới vào danh sách vốn có giả định ngầm
("luôn là bản ghi cuối", "list luôn rỗng khi add"), chủ động rà lại mọi nơi dựa
vào giả định cũ. Test phải kiểm invariant khi gọi LẶP LẠI, không chỉ đúng cho lần
gọi đầu tiên.

## 10. Đừng tự ý đổi văn bản hiển thị cho người chơi

**Đã từng gây ra:** Tự đổi mô tả skin để "làm rõ" → phải revert cả code lẫn DB vì
user muốn giữ nguyên văn. Lặp lại ở mô tả khác.

**Quy tắc:** Mọi thay đổi vào `data_item.name`, thông báo server, tooltip hiển
thị cho người chơi phải đề xuất nguyên văn và được xác nhận trước khi áp dụng,
tách biệt khỏi các thay đổi logic đi kèm trong cùng task.

## 11. "Ẩn ở UI" không phải là "khoá ở server" — tính năng có điều kiện bật/tắt (sự kiện, VIP, level) phải tự kiểm tra lại ở đúng nơi xử lý lệnh

**Đã từng gây ra:** Nhánh mua Shop Trung Thu ban đầu chỉ bị ẩn ở tầng menu
(`isFinishAllSuKienTrungThul2016()` kiểm ở MapDoMenu) nhưng KHÔNG tự kiểm tra ở
chính nhánh xử lý gói mua (`doBuyItemShopNew`) — gói tin gửi thẳng (không qua
menu) vẫn mua được sau khi sự kiện đã kết thúc. Đã vá 19/09/2026 bằng cách thêm
`if (Char.isFinishAllSuKienTrungThul2016()) { return; }` ngay đầu nhánh xử lý.

**Quy tắc:** Mọi tính năng có điều kiện bật/tắt (sự kiện theo thời gian, VIP,
level, đã hoàn thành nhiệm vụ...) phải kiểm tra điều kiện đó ở CHÍNH nơi xử lý
lệnh cuối cùng (nơi thực sự trừ tiền/tạo item/thay đổi state), không chỉ ở nơi
build menu/hiển thị UI. Coi menu chỉ là gợi ý cho người chơi thấy, không phải
hàng rào bảo vệ — client luôn có thể gửi gói tin trực tiếp bỏ qua UI.

## 12. Không đổi tên package/class hiện có trong `src/`

`lib/NQSH_5h.jar` chứa hàng trăm class đã biên dịch sẵn cùng package với `src/`,
không có mã nguồn. Đổi tên package/class trong `src/` biên dịch sạch nhưng ném
`NoClassDefFoundError` lúc chạy vì class trong jar gọi ngược theo tên cũ.

**Quy tắc:** Không đổi tên package/class hiện có trong `real`/`data`/`server`/
`io`/`real/cmd` trừ khi đã xác nhận không có class nào trong `lib/NQSH_5h.jar`
tham chiếu tới tên cũ (xem `project_kpah_nqsh_jar_khoa_ten_package.md`).

## 13. SQL mới luôn dùng PreparedStatement

Một số tool admin cũ (`AdminPanel`, `DatabaseNhanVat`...) còn SQL ghép chuỗi thô
— không copy các đoạn đó làm mẫu.

**Quy tắc:** Mọi câu SQL MỚI viết thêm bắt buộc dùng PreparedStatement với tham
số bind, không nối chuỗi trực tiếp giá trị từ người chơi/HTTP request vào câu
lệnh SQL.

## 14. Một tham số/công thức bị yêu cầu sửa ≥2 lần trong thời gian ngắn → dừng lại hỏi có phải sai *loại* nguyên nhân

**Đã từng gây ra:** Chuỗi 4-6 commit liên tiếp sửa đi sửa lại "giảm exp theo
level" và "Vận tiêu" trong 1-2 ngày — mỗi lần chỉ vá đúng 1 trường hợp biên mới
lộ ra. Cuối cùng hoá ra nhiều trường hợp là do sửa SAI LOẠI nguyên nhân (sửa số
liệu lặp lại thay vì sửa cấu trúc dữ liệu dùng chung, ví dụ 5 skin thú dùng chung
1 dòng toạ độ thay vì mỗi skin 1 dòng riêng).

**Quy tắc:** Khi cùng 1 khu vực code bị yêu cầu sửa lại lần thứ 2 trong vài ngày,
trước khi vá tiếp: (a) đặt tên hằng số rõ + comment ghi ngày/giá trị lịch sử;
(b) tự hỏi "đây là thêm 1 trường hợp biên hay do cấu trúc dữ liệu dùng chung
không đủ chỗ cho trường hợp này" — nếu nghi ngờ là (b), đề xuất đổi cấu trúc thay
vì vá thêm số liệu.

---

## Checklist trước khi báo "xong" một task code

- [ ] Nếu thêm hằng số/số hiệu vào không gian dùng chung: đã `grep` toàn bộ
      `src/` xác nhận không trùng, và không vượt giới hạn cứng phía client? (#1)
- [ ] Nếu chạm vào 1 trong các cặp hệ thống dễ nhầm đã biết (shop VIP vs shop sự
      kiện, Item vs Potion, `images_potion` vs `images_gem`...): đã xác nhận bằng
      code/DB thật, không suy đoán theo tên? (#2)
- [ ] Nếu sửa 1 hành vi thuộc kiến trúc nhiều-bản-sao (`Map*.java`, `saveX`/
      `saveXAuto`, hàm cộng exp...): đã `grep` liệt kê HẾT bản sao và sửa toàn
      bộ, đếm lại số match sau khi sửa? (#3)
- [ ] Nếu đổi tiền/vật phẩm: chuỗi check-rồi-act có được khoá đúng phạm vi, có
      đối chiếu với các lệnh khác có thể chạy trên luồng khác cùng player? (#4)
- [ ] Nếu cấp thưởng từ tài nguyên có hạn: đánh dấu đã dùng TRƯỚC, phát thưởng
      SAU khi xác nhận đánh dấu thành công? (#5)
- [ ] Nếu thêm loại thực thể mới (thú/pet/skin/mặt nạ...): đã rà hết mảng hằng
      số song song cùng trục chỉ số, không chỉ mảng được yêu cầu? (#6)
- [ ] Nếu UPDATE/INSERT text tiếng Việt qua mysql CLI: dùng file `.sql` UTF-8 +
      `SET NAMES utf8`, không dùng `-e` inline qua Bash? (#7)
- [ ] Build/test/chạy Java: chạy qua PowerShell, launch bằng đường dẫn
      `java.exe` đầy đủ, xác nhận PID thật bind đúng cổng? (#8)
- [ ] Nếu đổi tần suất gọi 1 hàm (1 lần → lặp định kỳ) hoặc thêm dòng vào 1 danh
      sách có giả định ngầm: đã rà lại nơi dựa vào giả định cũ, test invariant
      khi gọi lặp lại? (#9)
- [ ] Nếu có đổi text hiển thị cho người chơi: đã đề xuất nguyên văn và được xác
      nhận trước, tách riêng khỏi thay đổi logic? (#10)
- [ ] Nếu tính năng có điều kiện bật/tắt (sự kiện/VIP/level): điều kiện đó có
      được kiểm tra lại ở CHÍNH nơi xử lý lệnh cuối cùng, không chỉ ở tầng menu/UI? (#11)
- [ ] Không đổi tên package/class có sẵn trong `src/` mà chưa xác nhận
      `lib/NQSH_5h.jar` không tham chiếu tên cũ? (#12)
- [ ] Mọi SQL mới dùng PreparedStatement, không nối chuỗi? (#13)
- [ ] Nếu đây là lần sửa thứ ≥2 cho cùng khu vực trong vài ngày: đã cân nhắc khả
      năng sai loại nguyên nhân (cấu trúc dữ liệu) thay vì tiếp tục vá số liệu? (#14)
- [ ] Đã build (`PowerShell` → `.\build.bat`) sạch, deploy, khởi động lại server
      và kiểm `server_err.log` không có exception mới (loại trừ các cảnh báo SQL
      quen thuộc đã biết)?
