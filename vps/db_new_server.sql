-- ============================================================================
--  DỰNG DATABASE CHO SERVER MỚI — TRẮNG TINH DỮ LIỆU NGƯỜI CHƠI
--
--  Chạy trên VPS:
--      mysql -u root < /opt/htbb/vps/db_new_server.sql
--
--  CHẠY SAU KHI đã import 2 file:
--      mysql -u root htbb_db < /opt/htbb/du_lieu/sql_nguon/gamedb7_clean.sql
--      mysql -u root account < /opt/htbb/loginServer/account.sql
--
--  Script này làm 5 việc:
--    1. Xoá sạch MỌI bảng dữ liệu người chơi trong htbb_db (đọc từ
--       information_schema nên không sót bảng log xoay vòng theo tháng)
--    2. Giữ nguyên 16 bảng dữ liệu game (item, monster, quest, shop, giftcode...)
--    3. Xoá sạch tài khoản: account.team_user + htbb_db.5h_active
--    4. Tạo lại 1 tài khoản admin với mật khẩu bạn đặt, kích hoạt luôn
--    5. In bảng kiểm tra cuối cùng
--
--  ####################  KHÔNG HOÀN TÁC ĐƯỢC  ####################
--  TẮT SERVER trước khi chạy (server giữ Char trong RAM, còn chạy là nó ghi
--  đè lại sau khi bạn xoá).
-- ============================================================================


-- ############################################################################
--  ĐẶT MẬT KHẨU ADMIN Ở ĐÂY TRƯỚC KHI CHẠY
--  Không sửa 2 dòng này thì script sẽ dừng và báo lỗi, không tạo tài khoản
--  với mật khẩu mặc định (đó là cách để bạn không quên).
-- ############################################################################
SET @admin_user = 'admin';
SET @admin_pass = 'DOI_MAT_KHAU_NAY';

-- Phai chon database TRUOC khi tao procedure, neu khong MariaDB bao
-- "ERROR 1046 (3D000): No database selected".
USE htbb_db;


-- ############################################################################
--  PHẦN 0 — CHỐT AN TOÀN: kiểm tra mật khẩu TRƯỚC KHI xoá bất cứ thứ gì.
--  Đặt ở đây (không phải ở phần tạo tài khoản) để nếu bạn quên sửa mật khẩu
--  thì script dừng khi database vẫn còn nguyên vẹn.
-- ############################################################################
DROP PROCEDURE IF EXISTS htbb_kiem_tra_truoc;
DELIMITER $$
CREATE PROCEDURE htbb_kiem_tra_truoc()
BEGIN
    IF @admin_pass IS NULL OR @admin_pass = 'DOI_MAT_KHAU_NAY' OR CHAR_LENGTH(@admin_pass) < 4 THEN
        SIGNAL SQLSTATE '45000'
          SET MESSAGE_TEXT = 'DUNG LAI - chua xoa gi ca: hay sua @admin_pass o dau file thanh mat khau that (>= 4 ky tu) roi chay lai.';
    END IF;
    IF @admin_user IS NULL OR CHAR_LENGTH(@admin_user) < 4 THEN
        SIGNAL SQLSTATE '45000'
          SET MESSAGE_TEXT = 'DUNG LAI - chua xoa gi ca: @admin_user phai tu 4 ky tu tro len.';
    END IF;
END$$
DELIMITER ;

CALL htbb_kiem_tra_truoc();
DROP PROCEDURE htbb_kiem_tra_truoc;


-- ############################################################################
--  PHẦN 1 — XOÁ SẠCH DỮ LIỆU NGƯỜI CHƠI TRONG htbb_db
--
--  GIỮ LẠI 16 bảng: data_attribute, data_item, data_monster, data_potion,
--  data_quest, data_shop, bad_word, admin_msg, 5h_systems, 5h_active,
--  tob_x2exp, giftcode, tob_event, tob_fruit, tob_code_lixi, tob_tubinh
--
--  Vài bảng nghe như "của người chơi" nhưng LÀ DATA, xoá là lỗi server:
--    tob_tubinh    -> template tứ bình (16 dòng). Xoá là chọn nhân vật lv>=40
--                     ném "Duplicate entry" + ArrayIndexOutOfBounds.
--    tob_fruit      -> định nghĩa quả trong nông trại.
--    tob_event      -> trạng thái sự kiện toàn server.
--    tob_code_lixi  -> kho mã lì xì (bên dưới chỉ reset lượt nhận).
--    5h_active      -> bảng kích hoạt; ở đây xoá riêng ở PHẦN 3 cùng team_user.
-- ############################################################################
DROP PROCEDURE IF EXISTS htbb_wipe_players;
DELIMITER $$
CREATE PROCEDURE htbb_wipe_players()
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE tbl  VARCHAR(128);
    DECLARE cur CURSOR FOR
        SELECT TABLE_NAME
          FROM information_schema.TABLES
         WHERE TABLE_SCHEMA = 'htbb_db'
           AND TABLE_TYPE   = 'BASE TABLE'
           AND TABLE_NAME NOT IN (
                'data_attribute','data_item','data_monster','data_potion',
                'data_quest','data_shop','bad_word','admin_msg','5h_systems',
                '5h_active','tob_x2exp','giftcode','tob_event','tob_fruit',
                'tob_code_lixi','tob_tubinh'
           );
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    OPEN cur;
    vong_lap: LOOP
        FETCH cur INTO tbl;
        IF done THEN LEAVE vong_lap; END IF;
        SET @s = CONCAT('TRUNCATE TABLE `htbb_db`.`', tbl, '`');
        PREPARE st FROM @s;
        EXECUTE st;
        DEALLOCATE PREPARE st;
    END LOOP;
    CLOSE cur;
END$$
DELIMITER ;

CALL htbb_wipe_players();
DROP PROCEDURE htbb_wipe_players;


-- ############################################################################
--  PHẦN 2 — GIỮ KHO MÃ LÌ XÌ, CHỈ XOÁ DẤU VẾT AI ĐÃ NHẬN
-- ############################################################################
UPDATE `tob_code_lixi` SET `nhan` = 0, `timeNhan` = NULL, `charnhan` = NULL;


-- ############################################################################
--  PHẦN 2B — BÙ 2 THỨ MÀ gamedb7_clean.sql BỊ THIẾU
--
--  Đã đối chiếu từng bảng giữa DB đang chạy và bản dump sạch: dump thiếu đúng
--  2 thứ, cả 2 đều cần cho server mới.
--  (Thứ ba là 46 dòng data_attribute của Thần Khí — nằm riêng ở PHẦN 2C.)
--
--  1) tob_tubinh — TEMPLATE tứ bình 16 dòng (Mai/Lan/Cúc/Trúc ... Long/Lân/Quy/
--     Phụng). Bảng này nằm trong danh sách GIỮ ở phần 1 nên không bị xoá, nhưng
--     bản dump vốn đã không có dữ liệu. Để trống thì nhân vật lv>=40 vào game
--     sẽ ném "Duplicate entry ... for key PRIMARY" ở addCharTubinh và
--     ArrayIndexOutOfBounds ở createMsgCharTuBinh.
--     Chỉ nạp khi bảng đang trống -> chạy lại script nhiều lần không nhân đôi.
--
--  2) history_win_xoso — bảng lưu lịch sử thắng xổ số. Database.java:1619 có
--     INSERT vào bảng này nhưng KHÔNG có lệnh tạo bảng, và dump cũng không có.
--     Thiếu là lúc ai đó thắng xổ số sẽ ăn "Table doesn't exist".
-- ############################################################################
INSERT INTO `tob_tubinh` (`id`,`name`,`attack`,`def`,`dog`,`crit`,`hp`,`mp`,`level`,`nmanh`)
SELECT * FROM (
    SELECT  1 AS id,'Mai'   AS name, 15 AS attack, 24 AS def, 2 AS dog, 2 AS crit, 100 AS hp, 30 AS mp,40 AS level, 5 AS nmanh
    UNION ALL SELECT  2,'Lan',   30, 45, 4, 4, 200, 60,40, 5
    UNION ALL SELECT  3,'Cúc',   45, 69, 6, 6, 300, 90,40, 5
    UNION ALL SELECT  4,'Trúc',  60, 90, 8, 8, 400,120,40, 5
    UNION ALL SELECT  5,'Sáo',   30, 45, 4, 4, 200, 60,50,10
    UNION ALL SELECT  6,'Quạt',  60, 90, 8, 8, 400,120,50,10
    UNION ALL SELECT  7,'Sênh',  90,135,12,12, 600,180,50,10
    UNION ALL SELECT  8,'Đàn',  120,180,16,16, 800,240,50,10
    UNION ALL SELECT  9,'Ngư',   45, 69, 6, 6, 600, 90,60,15
    UNION ALL SELECT 10,'Tiều',  90,135,12,12, 700,180,60,15
    UNION ALL SELECT 11,'Canh', 135,204,18,18, 800,270,60,15
    UNION ALL SELECT 12,'Độc',  180,270,24,24, 900,360,60,15
    UNION ALL SELECT 13,'Long',  60, 90, 8, 8, 850,120,70,20
    UNION ALL SELECT 14,'Lân',  120,180,16,16, 950,240,70,20
    UNION ALL SELECT 15,'Quy',  180,270,24,24,1050,360,70,20
    UNION ALL SELECT 16,'Phụng',240,360,32,32,1150,480,70,20
) AS mau
WHERE (SELECT COUNT(*) FROM `tob_tubinh`) = 0;

CREATE TABLE IF NOT EXISTS `history_win_xoso` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_user` int(11) NOT NULL,
  `money_win` bigint(20) NOT NULL DEFAULT 0,
  `time_win` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_user` (`id_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- ############################################################################
--  PHẦN 2C — BÙ 46 DÒNG data_attribute CHO HỆ THỐNG THẦN KHÍ
--
--  gamedb7_clean.sql chỉ có data_attribute id 0..140 (141 dòng). Module Thần Khí
--  (ThanKhi/ThoRenThanBi.java) dùng tới id 186:
--      163..167 sát thương ngũ hành | 168 số dòng ẩn
--      169..179 nguyên liệu nâng cấp | HIDDEN_STAT_IDS tới 186
--
--  Tên thuộc tính tra cứu THEO VỊ TRÍ trong danh sách nạp lúc khởi động
--  (otherAtt[i] -> ALL_NAME_ATTRIBUTE_ITEM.get(i + 61), xem Item.java:1031 và
--  Map.java:15658), nên id phải LIÊN TỤC, không được hở. Thiếu là bấm vào Thần
--  Khí ĐƠ CLIENT — và đơ im lặng: server không ghi log, vì client mới là bên
--  chết khi tra tên thuộc tính không có (cmd 25 chỉ gửi đúng số dòng đang có).
--
--  DB Windows ở nhà đã vá tay từ 25/07 nên test ở nhà thấy chạy bình thường;
--  server mới dựng từ bản dump sạch thì không có -> vá luôn ở đây.
--  Đây là NGUỒN DUY NHẤT cho khối này (file sql/fix_thankhi_attribute.sql cũ
--  đã xoá vì trùng nội dung 100% với đây).
--
--  QUY ƯỚC CỘT ispercent (đừng đổi nếu chưa đọc):
--    0 -> in thẳng, không % (dòng cộng ĐIỂM) | 1 -> in thẳng kèm %
--    2 -> in value/10 kèm % (Client/InfoAttributeItem.cs:37-40)
--  Nên các dòng ispercent = 2 lưu số GẤP 10 trong HIDDEN_STAT_TABLE
--  (ThoRenThanBi.java:44): ghi 15 thì trong game hiện "1.5%". Đổi ispercent là
--  đổi luôn con số hiện ra gấp/giảm 10 lần.
-- ############################################################################
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


-- ############################################################################
--  PHẦN 2D — NỚI CÁC CỘT LƯU HÀNH TRANG (TEXT -> MEDIUMTEXT)
--
--  gamedb7_clean.sql khai `tob_char.equip/inven/bag/tuido` là TEXT = trần
--  65.535 byte, và `tob_gem_new` là varchar(1000). Cả hành trang được serialize
--  thành MỘT chuỗi rồi nhét vào `inven`, mỗi món ~470 byte kể từ khi otherAtt
--  nới lên 126 ô cho Thần Khí (Item.java:615) -> chỉ ~139 món là tràn, trong khi
--  túi chứa tới 42 * 5 = 210 ô.
--
--  Tràn thì tuỳ sql_mode:
--    - có STRICT_TRANS_TABLES (mặc định MySQL 8 / nhiều bản MariaDB mới):
--      UPDATE NÉM LỖI "Data too long for column 'inven'", Database.java chỉ
--      printStackTrace rồi đi tiếp -> lần lưu đó KHÔNG lưu gì cả, nhân vật mất
--      hết tiến trình từ lần lưu trước (mất luôn Thần Khí vừa tạo).
--    - không strict: MySQL CẮT CỤT ÂM THẦM phần đuôi -> mất mấy chục món.
--
--  DB Windows ở nhà đã nới tay từ trước (nên ở nhà không thấy lỗi), server VPS
--  dựng từ bản dump sạch thì chưa -> nới luôn ở đây.
--  Đây là NGUỒN DUY NHẤT cho khối này (file sql/fix_cot_du_lieu_tran.sql cũ
--  đã xoá vì trùng nội dung 100% với đây).
-- ############################################################################
ALTER TABLE `htbb_db`.`tob_char`
  MODIFY `equip` MEDIUMTEXT,
  MODIFY `inven` MEDIUMTEXT,
  MODIFY `bag`   MEDIUMTEXT,
  MODIFY `tuido` MEDIUMTEXT;

ALTER TABLE `htbb_db`.`tob_gem_new`
  MODIFY `listtemplate` MEDIUMTEXT NOT NULL,
  MODIFY `soluong`      MEDIUMTEXT NOT NULL,
  MODIFY `slock`        MEDIUMTEXT NOT NULL;


-- ############################################################################
--  PHẦN 2E — VÁ MÀU CHỮ SAI CỦA DÒNG "Tăng lửa lan"
--
--  data_attribute id 90 "Tăng lửa lan" đang idcolor=2 (đỏ) trong khi 3 dòng anh
--  em id 69/70/71 (Tăng sét lan/độc lan/băng lan) đều idcolor=0 (trắng) — lệch
--  không lý do, hiện lên áo/mặt nạ/cánh/chổi bất kỳ có dòng "tăng lan" (xem
--  Item.java:816-819 và Item.java:921-926, khối code dùng chung 4 id này).
--  Bản gốc của khối này: fix_mau_tang_lua_lan.sql ở thư mục gốc repo.
--  SAU KHI CHẠY PHẢI RESTART SERVER (loadNameAttributeItem chỉ chạy lúc khởi động).
-- ############################################################################
UPDATE `htbb_db`.`data_attribute` SET `idcolor` = 0 WHERE `id` = 90;


-- ############################################################################
--  PHẦN 2G — BÙ DÒNG data_item id=722 ("Sách kỹ năng pet (cũ)", nghỉ hưu)
--
--  Migrate sách pet -> potion (tháng 8) có xoá data_item id=722 trên DB sống.
--  Client cấp phát mảng template theo SỐ LƯỢNG dòng nhưng ghi theo ID, nên nếu
--  bảng có id lớn nhất > số dòng thì lệch mảng (mất template cuối, nhân vật có
--  thể "mất thân"). Dump sạch có thể chưa từng xoá dòng này nên INSERT này chỉ
--  chạy khi thật sự thiếu, để bảng luôn liền mạch.
--  Bản gốc của khối này: sql/fix_data_item_lo_id.sql (đã xoá vì trùng nội dung 100%).
-- ############################################################################
INSERT INTO `htbb_db`.`data_item`
(id, name, type, style, he, gender, level, durable,
 atb0, atb1, atb2, atb3, atb4, atb5, atb6, atb7, atb8, atb9,
 price, clazz, xstart, ystart, colortype, nloan, idUpLevel, namein, ideff, pricecu)
SELECT 722, 'Sách kỹ năng pet (cũ)', 24, 0, 0, 0, 1, 0,
       0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
       0, -1, 580, 0, -1, 0, -1, NULL, -1, 0
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM `htbb_db`.`data_item` WHERE id = 722) t);


-- ############################################################################
--  PHẦN 2H — BÙ ĐỦ DÒNG data_potion (0..161) + THIẾT LẬP POTION 111 "SÁCH KỸ
--  NĂNG PET"
--
--  Nhân vật có đúng 162 ô thuốc (Char.SO_O_THUOC); client cấp phát mảng theo
--  SỐ DÒNG của data_potion nhưng gán thẳng theo id không kiểm tra biên — thiếu
--  dòng ở id cao (như 111) làm client vẽ hành trang lỗi giữa vòng lặp, nạn nhân
--  là NGUYÊN CẢ HÀNH TRANG (vòng lặp potion nằm trước nguyên liệu/thú/vật phẩm
--  trong cùng một try). Bù đủ dòng còn thiếu 0..161 (giữ nguyên dòng đã có).
--  Tên potion 111 dùng bản mô tả đầy đủ nhất (đã gộp từ
--  sql/add_mo_ta_sach_pet.php — 2 file phụ này đã xoá vì đã gộp hết vào đây).
--  SAU KHI CHẠY PHẢI RESTART SERVER (bảng potion chỉ đọc lúc khởi động).
-- ############################################################################
DROP TEMPORARY TABLE IF EXISTS tmp_so_potion;
CREATE TEMPORARY TABLE tmp_so_potion (n INT PRIMARY KEY);
INSERT INTO tmp_so_potion (n)
SELECT a.i + b.i * 10 + c.i * 100 AS n
FROM      (SELECT 0 i UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
           UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) a
CROSS JOIN (SELECT 0 i UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
           UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) b
CROSS JOIN (SELECT 0 i UNION SELECT 1) c
WHERE a.i + b.i * 10 + c.i * 100 <= 161;

INSERT INTO `htbb_db`.`data_potion` (id, idImage, delay, isTrade, name, name2, price, recovered)
SELECT s.n, 0, 0, 0, '', '', 0, 0
FROM tmp_so_potion s
LEFT JOIN `htbb_db`.`data_potion` p ON p.id = s.n
WHERE p.id IS NULL;

DROP TEMPORARY TABLE IF EXISTS tmp_so_potion;

UPDATE `htbb_db`.`data_potion`
   SET idImage = 95, delay = 0, isTrade = 1,
       name = 'Sách kỹ năng pet\nDùng để pet học kỹ năng.',
       name2 = 'sachkynangpet', price = 0, recovered = 0
 WHERE id = 111;


-- ############################################################################
--  PHẦN 2I — TẠO BẢNG tob_nap_moc (mốc nạp tân thủ 49k/99k/299k)
--
--  Luồng: HTBB-Web /naptien tạo dòng 'cho_thanh_toan' -> /api/nap/webhook-sepay hoặc
--  trang quản trị /admin/naptien xác nhận -> 'da_thanh_toan' -> NPC Tổng Quản trong game
--  trao quà, chuyển 'da_nhan'.
--  Bản gốc của khối này: sql/tob_nap_moc.sql (đã xoá vì trùng nội dung 100%).
-- ############################################################################
CREATE TABLE IF NOT EXISTS `htbb_db`.`tob_nap_moc` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `userID` int(11) NOT NULL COMMENT 'team_user.id / player.userID',
  `moc` tinyint(3) unsigned NOT NULL COMMENT '1 = 49k, 2 = 99k, 3 = 299k',
  `amount` int(10) unsigned NOT NULL COMMENT 'so tien VND',
  `ma_don` varchar(20) NOT NULL COMMENT 'noi dung chuyen khoan, dung de doi chieu voi SePay',
  `phuong_hoang` tinyint(4) NOT NULL DEFAULT -1 COMMENT 'moc 3 only: 0=Phuong Hoang, 1=Phuong Hoang Bang, -1=chua chon',
  `trang_thai` enum('cho_thanh_toan','da_thanh_toan','da_nhan') NOT NULL DEFAULT 'cho_thanh_toan',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `paid_at` datetime NULL DEFAULT NULL,
  `claimed_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_ma_don` (`ma_don`),
  KEY `idx_user_moc` (`userID`, `moc`, `trang_thai`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ############################################################################
--  PHẦN 3 — XOÁ SẠCH TÀI KHOẢN ĐĂNG NHẬP
--
--  account.team_user   = tài khoản đăng nhập thật (login server đọc DB này)
--  htbb_db.5h_active   = bảng kích hoạt; thiếu dòng là bị chặn ngay màn login
--                        (sv.EnableAvtiveCheck=1)
--  htbb_db.team_user   = bảng KHÁC hoàn toàn, chỉ map provider/agent
-- ############################################################################
TRUNCATE TABLE `account`.`team_user`;
TRUNCATE TABLE `htbb_db`.`5h_active`;
ALTER TABLE `account`.`team_user` AUTO_INCREMENT = 1;


-- ############################################################################
--  PHẦN 4 — TẠO TÀI KHOẢN ADMIN
--
--  Mật khẩu băm bằng hàm PASSWORD() của MariaDB để KHỚP với login server
--  (CheckLoginSocket.jar so: ... and `password`=password('...')).
--  Đây là lý do KHÔNG dùng được MySQL 8 — bản đó đã bỏ hàm PASSWORD().
-- ############################################################################
DROP PROCEDURE IF EXISTS htbb_tao_admin;
DELIMITER $$
CREATE PROCEDURE htbb_tao_admin()
BEGIN
    SET @sql = CONCAT(
        'INSERT INTO `account`.`team_user` ',
        '(username,password,phone,regdate,ban,provider,fromgame) VALUES (',
        QUOTE(@admin_user), ',PASSWORD(', QUOTE(@admin_pass), "),'',NOW(),0,0,0)");
    PREPARE st FROM @sql;
    EXECUTE st;
    DEALLOCATE PREPARE st;

    -- Kich hoat tai khoan vua tao trong 30 ngay
    INSERT INTO `htbb_db`.`5h_active` (userID, username, time_end)
    SELECT id, username, UNIX_TIMESTAMP() + 2592000 FROM `account`.`team_user` WHERE username = @admin_user;
END$$
DELIMITER ;

CALL htbb_tao_admin();
DROP PROCEDURE htbb_tao_admin;


-- ############################################################################
--  PHẦN 5 — KIỂM TRA
--
--  Cột "phai_la" nói rõ giá trị đúng. Lệch là có gì sai, đừng bật server.
-- ############################################################################
SELECT 'tob_char (nhan vat)'        AS bang, COUNT(*) AS so_dong, '0' AS phai_la FROM htbb_db.tob_char
UNION ALL SELECT 'tob_animal',       COUNT(*), '0'      FROM htbb_db.tob_animal
UNION ALL SELECT 'tob_pet',          COUNT(*), '0'      FROM htbb_db.tob_pet
UNION ALL SELECT 'tob_clan',         COUNT(*), '0'      FROM htbb_db.tob_clan
UNION ALL SELECT 'tob_market',       COUNT(*), '0'      FROM htbb_db.tob_market
UNION ALL SELECT 'giftcode_log',     COUNT(*), '0'      FROM htbb_db.giftcode_log
UNION ALL SELECT 'board_created',    COUNT(*), '0'      FROM htbb_db.board_created
UNION ALL SELECT '--- du lieu game phai CON ---', NULL, NULL
UNION ALL SELECT 'data_item',        COUNT(*), '~904'   FROM htbb_db.data_item
UNION ALL SELECT 'data_monster',     COUNT(*), '138'    FROM htbb_db.data_monster
UNION ALL SELECT 'data_potion',      COUNT(*), '162'    FROM htbb_db.data_potion
UNION ALL SELECT 'data_shop',        COUNT(*), '148'    FROM htbb_db.data_shop
UNION ALL SELECT 'data_quest',       COUNT(*), '49'     FROM htbb_db.data_quest
UNION ALL SELECT 'data_attribute',   COUNT(*), '198'    FROM htbb_db.data_attribute
UNION ALL SELECT 'tob_tubinh (template)', COUNT(*), '16' FROM htbb_db.tob_tubinh
UNION ALL SELECT 'bad_word',         COUNT(*), '147'    FROM htbb_db.bad_word
UNION ALL SELECT '--- cot luu hanh trang (PHAN 2D) ---', NULL, NULL
UNION ALL SELECT 'cot da la mediumtext', COUNT(*), '7'
  FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA = 'htbb_db' AND DATA_TYPE = 'mediumtext'
   AND ( (TABLE_NAME='tob_char'    AND COLUMN_NAME IN ('equip','inven','bag','tuido'))
      OR (TABLE_NAME='tob_gem_new' AND COLUMN_NAME IN ('listtemplate','soluong','slock')) )
UNION ALL SELECT 'idcolor dong "Tang lua lan" (PHAN 2E)', idcolor, '0'
  FROM htbb_db.data_attribute WHERE id = 90
UNION ALL SELECT '--- sach pet (PHAN 2G/2H) ---', NULL, NULL
UNION ALL SELECT 'data_potion (0..161 lien tuc)', COUNT(*), '162'
  FROM htbb_db.data_potion
UNION ALL SELECT 'data_potion id=111 ten', name, 'Sach ky nang pet...'
  FROM htbb_db.data_potion WHERE id = 111
UNION ALL SELECT '--- moc nap (PHAN 2I) ---', NULL, NULL
UNION ALL SELECT 'bang tob_nap_moc ton tai', COUNT(*), '1 (bang, khong can co dong)'
  FROM information_schema.TABLES
 WHERE TABLE_SCHEMA = 'htbb_db' AND TABLE_NAME = 'tob_nap_moc'
UNION ALL SELECT '--- tai khoan ---', NULL, NULL
UNION ALL SELECT 'account.team_user', COUNT(*), '1 (admin)' FROM account.team_user
UNION ALL SELECT '5h_active',        COUNT(*), '1'      FROM htbb_db.5h_active;

-- ============================================================================
--  SAU KHI CHẠY — 2 VIỆC BẮT BUỘC
--
--  1. Bảng tob_train_luong KHÔNG có trong bản dump.
--     Không cần làm gì: server tự CREATE TABLE IF NOT EXISTS lúc khởi động
--     (TeamServer gọi createTableTrainLuong).
--
--  2. TẠO NHÂN VẬT ADMIN TRONG GAME.
--     Quyền admin xét theo TÊN NHÂN VẬT, không phải tên tài khoản:
--     server.ini -> sv.adminAccounts=admin,admin2
--     Nên đăng nhập bằng tài khoản vừa tạo rồi tạo nhân vật đặt tên đúng
--     "admin" (hoặc "admin2"). Tên chứa chữ "admin" đã bị chặn với người chơi
--     thường nên không ai giành được tên này trước bạn.
--
--  Muốn thêm mã giftcode buff của bạn (tb8x, linhthu1, hoakylan, lkhoa1ty...):
--      mysql -u root htbb_db < /opt/htbb/sql/buff_ddsa.sql
-- ============================================================================
