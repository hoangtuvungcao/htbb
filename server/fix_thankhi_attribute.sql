-- ============================================================================
--  VÁ DỮ LIỆU THUỘC TÍNH CHO HỆ THỐNG THẦN KHÍ
--
--  TRIỆU CHỨNG: bấm vào Thần Khí (tạo / nâng cấp / đổi dòng / xem món trong
--  hành trang) là ĐƠ CLIENT. Server KHÔNG ghi log gì cả.
--
--  NGUYÊN NHÂN: bảng data_attribute chỉ có id 0-140 (141 dòng), trong khi
--  module ThanKhi/ThoRenThanBi.java dùng tới id 186:
--      item_attribute_ids        = 163..167   (sát thương ngũ hành)
--      otherAtt[168-61]          = 168        (số dòng ẩn)
--      item_upgrade_material_ids = 169..179   (nguyên liệu nâng cấp)
--      HIDDEN_STAT_IDS           = tới 186
--
--  Tên thuộc tính được nạp bằng:
--      select * from data_attribute order by id   ->  add() lần lượt vào
--      ItemTemplates.ALL_NAME_ATTRIBUTE_ITEM
--  và tra cứu THEO VỊ TRÍ: otherAtt[i] -> ALL_NAME_ATTRIBUTE_ITEM.get(i + 61)
--  (xem Map.java:15658, Item.java:1031). Danh sách chỉ có 141 phần tử nên
--  get(163..186) ném IndexOutOfBounds lúc dựng gói mô tả vật phẩm; phía client
--  cũng chỉ nhận 141 dòng tên (MessageCreator.createMsgLogin -> cmd 25) nên tra
--  key "163" ra null -> client chết giữa lúc vẽ -> ĐƠ.
--
--  VÌ TRA CỨU THEO VỊ TRÍ nên id BẮT BUỘC phải liên tục, không được hở.
--  File này thêm đủ id 141..186, kể cả 158-159 (2 ô dự phòng Thần Khí đang mượn
--  làm "Tăng điểm sát thương vật lý / ma pháp") để các id phía sau không bị lệch.
--
--  CHẠY Ở ĐÂU: mọi DB chưa có đủ 187 dòng. Cụ thể là DB dựng từ
--  gamedb7_clean.sql (bản dump sạch chỉ có 141 dòng) — tức DB của server VPS
--  mới. DB Windows ở nhà đã vá từ 25/07 nên test ở nhà mới thấy chạy bình thường.
--
--  Dùng ON DUPLICATE KEY UPDATE nên file này là NGUỒN DUY NHẤT cho tên và cột
--  ispercent của id 141..186: chạy lại nhiều lần không hỏng, và chạy trên DB đã
--  có sẵn thì tên cũ được ghi đè lại cho đúng.
--  SAU KHI CHẠY PHẢI RESTART SERVER (loadNameAttributeItem chỉ chạy lúc khởi động).
--
-- ----------------------------------------------------------------------------
--  QUY ƯỚC ĐƠN VỊ — ĐỌC TRƯỚC KHI SỬA SỐ
--
--  ispercent = 0  ->  client in thẳng, không có dấu %      (dòng cộng ĐIỂM)
--  ispercent = 1  ->  client in thẳng, kèm %               (phần trăm chẵn)
--  ispercent = 2  ->  client in value/10, kèm %            (phần trăm 1 số lẻ)
--                     xem Client/InfoAttributeItem.cs:37-40
--
--  Vì vậy các dòng ispercent = 2 lưu số GẤP 10 trong HIDDEN_STAT_TABLE
--  (ThoRenThanBi.java:44): bảng ghi 15 thì trong game hiện "1.5%". Phần tính
--  sát thương cũng chia lại đúng như vậy (Char.java:4840-4845, raw/1000).
--  ĐỔI ispercent CỦA MỘT DÒNG LÀ ĐỔI LUÔN GIÁ TRỊ HIỆN RA GẤP/GIẢM 10 LẦN.
-- ============================================================================

INSERT INTO `data_attribute` (`id`,`name`,`idcolor`,`ispercent`,`decript`,`namein`) VALUES
-- 29 dòng ẩn của Thần Khí — tên lấy đúng theo bảng thiết kế (cột "Dòng ẩn")
(158,'Tăng điểm sát thương vật lý',0,0,'',''),
(159,'Tăng điểm sát thương ma pháp',0,0,'',''),
(141,'Tăng sát thương chí mạng',0,2,'',''),
(142,'Tăng sát thương theo % HP kẻ địch',0,2,'',''),
(143,'Tăng sát thương theo % MP kẻ địch',0,2,'',''),
(144,'Tăng % sát thương vật lý',0,1,'',''),
(145,'Tăng % sát thương ma pháp',0,1,'',''),
(146,'Tăng % Xuyên thủ ma',0,2,'',''),
(147,'Tăng % Xuyên thủ vật',0,2,'',''),
(148,'Tăng % sát thương lên boss/trụ',0,2,'',''),
(149,'Tăng % sát thương lên quái',0,2,'',''),
(150,'Giảm % sát thương nhận vào từ Boss',0,2,'',''),
(180,'Giảm sát thương nhận vào từ người chơi',0,2,'',''),
(151,'Tăng điểm phòng thủ cơ bản',0,0,'',''),
(155,'Kháng Hỏa',0,1,'',''),
(154,'Kháng Thủy',0,1,'',''),
(153,'Kháng Mộc',0,1,'',''),
(152,'Kháng Kim',0,1,'',''),
(156,'Kháng Thổ',0,1,'',''),
(186,'Kháng tất cả',0,2,'',''),
(157,'Tăng điểm giáp khi đang ở trong tổ đội',0,0,'',''),
(182,'Hút HP',0,1,'',''),
(181,'Hút MP',0,1,'',''),
(160,'Tăng tốc độ đánh',0,1,'',''),
(161,'Tăng tốc độ di chuyển',0,0,'',''),
(183,'Tăng % né tránh',0,1,'',''),
(185,'Tăng % phản đòn',0,1,'',''),
(184,'Giảm % sức mạnh và % phòng thủ của kẻ địch',0,2,'',''),
(162,'Tăng % câm lặng của bản thân',0,1,'',''),
-- Dòng hệ hiện trên món (item_attribute_ids 163..167)
(163,'Sát thương hệ kim',0,1,'',''),
(164,'Sát thương hệ mộc',0,1,'',''),
(165,'Sát thương hệ thuỷ',0,1,'',''),
(166,'Sát thương hệ hoả',0,1,'',''),
(167,'Sát thương hệ thổ',0,1,'',''),
-- Bộ đếm số dòng ẩn + 11 ô nguyên liệu nâng cấp (item_upgrade_material_ids)
(168,'Số dòng ẩn',0,0,'',''),
(169,'Vải',0,0,'',''),
(170,'Sắt',0,0,'',''),
(171,'Ngọc',0,0,'',''),
(172,'Gỗ thường',0,0,'',''),
(173,'Da mềm',0,0,'',''),
(174,'Tơ lụa',0,0,'',''),
(175,'Bạc',0,0,'',''),
(176,'Thuỷ tinh',0,0,'',''),
(177,'Gỗ sưa',0,0,'',''),
(178,'Da cứng',0,0,'',''),
(179,'Đá ngũ hợp',0,0,'','')
ON DUPLICATE KEY UPDATE
  `name`      = VALUES(`name`),
  `idcolor`   = VALUES(`idcolor`),
  `ispercent` = VALUES(`ispercent`);

-- Kiểm tra: phải ra 187 dòng, id nhỏ nhất 0, lớn nhất 186, KHÔNG hở id nào.
SELECT COUNT(*) AS so_dong, MIN(id) AS nho_nhat, MAX(id) AS lon_nhat,
       (MAX(id) - MIN(id) + 1) = COUNT(*) AS lien_tuc_khong_ho
  FROM `data_attribute`;

-- ============================================================================
--  LƯU Ý CÒN LẠI
--
--  1. id 139 và 140 trong DB hiện là 'HP' và 'Trí Mạng', nhưng ThanKhi mong đợi
--     'Sát thương ma' và 'Sát thương vật'. Module này rõ ràng được viết cho một
--     bảng data_attribute đời mới hơn bản dump của bạn. Vì vậy HIDDEN_STAT_IDS
--     đã chuyển 2 dòng đó sang ô dự phòng 158/159 (xem ThoRenThanBi.java:80-85).
--     Không sửa 139/140 vì phần khác của game đang dùng đúng nghĩa HP / Trí Mạng.
--
--  2. Món Thần Khí đã lỡ tạo ra trước khi vá vẫn nằm trong hành trang và vẫn
--     có thể gây đơ. Nếu sau khi restart mà vẫn đơ, phải xoá món đó đi.
--
--  3. Server mới dựng bằng vps/db_new_server.sql đã tự nạp khối này ở PHẦN 2C,
--     không cần chạy tay file này nữa. Sửa tên ở đây thì sửa luôn ở đó.
-- ============================================================================
