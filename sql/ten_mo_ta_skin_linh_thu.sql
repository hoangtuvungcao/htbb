-- ==================================================================================
--  TEN + MO TA cua 6 vat pham skin linh thu
-- ==================================================================================
--
--  TEP NAY DUOC SINH RA, DUNG SUA TAY.
--  Nguon: cac bang hang so trong src/real/SkinThuCuoi.java
--  Sinh lai: java -cp build/sb-src SinhSqlTenSkinThu
--
--  Cot `name` cua game nay vua la TEN vua la MO TA: khong co cot mo ta rieng cho
--  do mac duoc, nen mo ta duoc nhet vao name va xuong dong bang ky tu xuong dong
--  that. Game da lam san nhu vay voi ao Nghe Thuong, Ngoc Nu, Kim Dong...
--
--  Chay sau sql/skin_linh_thu.sql, roi khoi dong lai may chu (bang item template
--  chi nap mot lan luc bat may).
--
--      mysql -u root -p <ten_db> < sql/ten_mo_ta_skin_linh_thu.sql
-- ==================================================================================

UPDATE `data_item` SET `name` = 'Skin thú cưỡi Copperajah
[hệ Thép] - hợp Dòng Eevee
- Cộng 2000 HP
- Cộng 3000 MP
- Đặc tính (khi hợp hệ): khi bị Dòng Charmander, Dòng Bulbasaur hoặc Dòng Squirtle đánh, chặn 30% sát thương trong 3 giây. Tỉ lệ 15%, hồi chiêu 40 giây'
 WHERE `id` = 905;

UPDATE `data_item` SET `name` = 'Skin thú cưỡi Gấu Trúc
[hệ Cỏ] - hợp Dòng Bulbasaur
- Cộng 200 chính xác (khi hợp hệ)
- Đặc tính: khi bị Dòng Eevee hoặc Dòng Pichu đánh, phản sát thương +50% (tối đa 80%) trong 3 giây. Tỉ lệ 15%, hồi chiêu 40 giây'
 WHERE `id` = 906;

UPDATE `data_item` SET `name` = 'Skin thú cưỡi Xerneas
[hệ Nước] - hợp Dòng Pichu
- Cộng 20% điểm tiềm năng (khi hợp hệ)
- Đặc tính: khi nhận sát thương, chặn 50% sát thương trong 5 giây. Tỉ lệ 10%, hồi chiêu 40 giây'
 WHERE `id` = 907;

UPDATE `data_item` SET `name` = 'Skin thú cưỡi Camerupt
[hệ Lửa] - hợp Dòng Squirtle
- 5% đòn đánh xuyên toàn bộ giáp (khi hợp hệ)
- Đặc tính: khi máu xuống dưới 30%, tăng 50% tấn công trong 3 giây, hồi chiêu 40 giây'
 WHERE `id` = 908;

UPDATE `data_item` SET `name` = 'Skin thú cưỡi Lycanroc
[hệ Đất] - hợp Dòng Charmander
- Cộng 20% phòng thủ (khi hợp hệ)
- Hấp thụ 15% sát thương'
 WHERE `id` = 909;

UPDATE `data_item` SET `name` = 'Skin thú cưỡi Dialga
[hệ Thép]
- Khi trang bị Chổi Sét: 5% tỉ lệ mỗi lần Sét Lan phát động sẽ triệu hồi Sét đánh, tăng thêm 3 giây choáng'
 WHERE `id` = 911;

-- Kiem tra: phai ra dung 6 dong.
SELECT `id`, `name` FROM `data_item` WHERE `id` IN (905,906,907,908,909,911) ORDER BY `id`;
