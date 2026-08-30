-- ==================================================================================
--  TEN + MO TA cua 5 vat pham skin linh thu
-- ==================================================================================
--
--  TEP NAY DUOC SINH RA, DUNG SUA TAY.
--  Nguon: cac bang hang so trong src/real/SkinLinhThu.java
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

UPDATE `data_item` SET `name` = 'Skin linh thú Phúc Thú
[hệ Kim] - hợp Pháp sư
- Hợp hệ: +5% sát thương
- Cộng 2000 HP
- Cộng 3000 MP
- Nội tại: khi bị Kiếm khách, Chiến binh hoặc Đấu sĩ đánh, chặn 30% sát thương trong 3 giây. Tỉ lệ 15%, hồi chiêu 40 giây'
 WHERE `id` = 905;

UPDATE `data_item` SET `name` = 'Skin linh thú Gấu Trúc
[hệ Mộc] - hợp Chiến binh
- Hợp hệ: +5% sát thương
- Cộng 200 chính xác
- Nội tại: khi bị Pháp sư hoặc Cung thủ đánh, phản sát thương +50% (tối đa 80%) trong 3 giây. Tỉ lệ 15%, hồi chiêu 40 giây'
 WHERE `id` = 906;

UPDATE `data_item` SET `name` = 'Skin linh thú Tiên Lộc
[hệ Thủy] - hợp Cung thủ
- Hợp hệ: +5% sát thương
- Cộng 20% điểm tiềm năng
- Nội tại: khi nhận sát thương, chặn 50% sát thương trong 5 giây. Tỉ lệ 10%, hồi chiêu 40 giây'
 WHERE `id` = 907;

UPDATE `data_item` SET `name` = 'Skin linh thú Xích Dương
[hệ Hỏa] - hợp Đấu sĩ
- Hợp hệ: +5% sát thương
- 5% đòn đánh xuyên toàn bộ giáp
- Nội tại: khi máu xuống dưới 30%, tăng 50% tấn công trong 3 giây, hồi chiêu 40 giây'
 WHERE `id` = 908;

UPDATE `data_item` SET `name` = 'Skin linh thú Chiến Lang
[hệ Thổ] - hợp Kiếm khách
- Hợp hệ: +5% sát thương
- Cộng 20% phòng thủ
- Hấp thụ 15% sát thương'
 WHERE `id` = 909;

-- Kiem tra: phai ra dung 5 dong, moi ten deu co dong "[he ...]".
SELECT `id`, `name` FROM `data_item` WHERE `id` BETWEEN 905 AND 909 ORDER BY `id`;
