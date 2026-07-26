-- ============================================================================
--  VÁ DỮ LIỆU THUỘC TÍNH CHO HỆ THỐNG THẦN KHÍ
--
--  VẤN ĐỀ: mở món Thần Khí là đơ client.
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
--  (xem Map.java:15586). Danh sách chỉ có 141 phần tử nên get(163..179) ném
--  IndexOutOfBounds ngay lúc dựng gói tin mô tả vật phẩm -> client chờ mãi
--  không nhận được gói -> ĐƠ.
--
--  VÌ TRA CỨU THEO VỊ TRÍ nên id BẮT BUỘC phải liên tục, không được hở.
--  File này thêm đủ id 141..186, kể cả 158-159 chưa rõ công dụng (đặt tạm)
--  để các id phía sau không bị lệch.
--
--  Tên lấy từ chính comment trong ThoRenThanBi.java.
--
--  SAU KHI CHẠY PHẢI RESTART SERVER (loadNameAttributeItem chỉ chạy lúc khởi động).
-- ============================================================================

INSERT INTO `data_attribute` (`id`,`name`,`idcolor`,`ispercent`,`decript`,`namein`) VALUES
(141,'Sát thương chí mạng',0,1,'',''),
(142,'Sát thương theo hp',0,1,'',''),
(143,'Sát thương theo mp',0,1,'',''),
(144,'Sát thương vật',0,1,'',''),
(145,'Sát thương ma',0,1,'',''),
(146,'Xuyên thủ ma',0,1,'',''),
(147,'Xuyên thủ vật',0,1,'',''),
(148,'Tăng sát thương boss',0,1,'',''),
(149,'Tăng sát thương quái',0,1,'',''),
(150,'Giảm sát thương boss',0,1,'',''),
(151,'Tăng thủ cơ bản',0,0,'',''),
(152,'Kháng kim',0,1,'',''),
(153,'Kháng mộc',0,1,'',''),
(154,'Kháng thuỷ',0,1,'',''),
(155,'Kháng hoả',0,1,'',''),
(156,'Kháng thổ',0,1,'',''),
(157,'Tăng kháng khi tổ đội',0,1,'',''),
(158,'Dự phòng 158',0,0,'',''),
(159,'Dự phòng 159',0,0,'',''),
(160,'Giảm hồi chiêu',0,1,'',''),
(161,'Tốc độ di chuyển',0,1,'',''),
(162,'Tăng câm lặng',0,1,'',''),
(163,'Sát thương hệ kim',0,1,'',''),
(164,'Sát thương hệ mộc',0,1,'',''),
(165,'Sát thương hệ thuỷ',0,1,'',''),
(166,'Sát thương hệ hoả',0,1,'',''),
(167,'Sát thương hệ thổ',0,1,'',''),
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
(179,'Đá ngũ hợp',0,0,'',''),
(180,'Giảm sát thương cuối',0,1,'',''),
(181,'Hút mp',0,1,'',''),
(182,'Hút hp',0,1,'',''),
(183,'Tăng né tránh',0,1,'',''),
(184,'Giảm sức mạnh và phòng thủ địch',0,1,'',''),
(185,'Phản đòn',0,1,'',''),
(186,'Kháng tất cả',0,1,'','');

-- Kiểm tra: phải ra 187 dòng, id nhỏ nhất 0, lớn nhất 186, KHÔNG hở id nào.
SELECT COUNT(*) AS so_dong, MIN(id) AS nho_nhat, MAX(id) AS lon_nhat,
       (MAX(id) - MIN(id) + 1) = COUNT(*) AS lien_tuc_khong_ho
  FROM `data_attribute`;

-- ============================================================================
--  LƯU Ý CÒN LẠI
--
--  1. id 139 và 140 trong DB hiện là 'HP' và 'Trí Mạng', nhưng ThanKhi mong đợi
--     'Sát thương ma' và 'Sát thương vật'. Module này rõ ràng được viết cho một
--     bảng data_attribute đời mới hơn bản dump của bạn. Hai dòng ẩn đó sẽ hiện
--     SAI TÊN, nhưng không gây lỗi. Không sửa vì id 139/140 đang được phần khác
--     của game dùng đúng nghĩa HP / Trí Mạng.
--
--  2. Món Thần Khí đã lỡ tạo ra trước khi vá vẫn nằm trong hành trang và vẫn
--     có thể gây đơ. Nếu sau khi restart mà vẫn đơ, phải xoá món đó đi.
--
--  3. Chưa test được trong game. Chạy xong hãy thử tạo ĐÚNG MỘT món trước,
--     đừng phát hàng loạt.
-- ============================================================================
