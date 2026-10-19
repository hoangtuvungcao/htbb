-- ============================================================================
--  WIPE TOÀN BỘ DỮ LIỆU NGƯỜI CHƠI — DB htbb_db
--
--  ####################  KHÔNG HOÀN TÁC ĐƯỢC  ####################
--
--  LÀM ĐÚNG THỨ TỰ:
--    1. TẮT SERVER HẲN (đóng htbb-server.jar và cả loginServer).
--       Server giữ Char trong RAM, còn chạy là nó ghi đè lại sau khi bạn xoá.
--    2. BACKUP:
--         mysqldump -u root htbb_db  > /opt/htbb/backups/htbb_db_truoc_xoa.sql
--         mysqldump -u root account  > /opt/htbb/backups/account_truoc_xoa.sql
--    3. Chạy PHẦN 1 (hoặc PHẦN 2) bên dưới.
--    4. Đọc mục "SAU KHI WIPE" ở cuối, có 2 việc bắt buộc phải làm.
--
--  TẠI SAO KHÔNG DÙNG DANH SÁCH BẢNG CỐ ĐỊNH:
--    Server tự tạo bảng log xoay vòng theo tuần/tháng —
--    Database.java:94-95 khai báo tableNameWeek_month_year = tob_log_use_luong
--    và tableNamemonth_year = tob_nap_xai_luong, rồi nối thêm hậu tố kỳ hiện
--    tại (vd tob_log_use_luong_1_7_2025, tob_nap_xai_luong_72025).
--    Nên mỗi DB có một bộ bảng khác nhau, viết cứng là dính
--    "#1146 Table doesn't exist". Hai cách dưới đây đọc thẳng
--    information_schema nên luôn khớp với DB thật.
--
--  GIỮ LẠI 16 bảng data gốc / cấu hình:
--    data_attribute, data_item, data_monster, data_potion, data_quest,
--    data_shop, bad_word, admin_msg, 5h_systems, 5h_active, tob_x2exp,
--    giftcode, tob_event, tob_fruit, tob_code_lixi, tob_tubinh
--  Mọi bảng còn lại đều bị xoá sạch.
--
--  Tại sao GIỮ vài bảng nghe có vẻ "của người chơi":
--    5h_active     -> bảng KÍCH HOẠT TÀI KHOẢN. Xoá là KHOÁ CỬA CẢ SERVER:
--                     LoginHandler:297/383/444 và SelectCharHandler:162 đều
--                     gọi check_active(), không có dòng là không đăng nhập
--                     nổi. Chỉ xoá nếu bạn wipe luôn bảng account (mục TUỲ CHỌN).
--    tob_event     -> trạng thái/cấu hình sự kiện toàn server, không per-player.
--    tob_fruit     -> định nghĩa quả (name, id1-5, decript, time), là data.
--    tob_code_lixi -> kho mã lì xì. Xoá là mất mã; bên dưới reset lượt nhận.
--    tob_tubinh    -> BẢNG TEMPLATE TỨ BÌNH (16 dòng: name, attack, def, crit,
--                     hp, mp, level, nmanh), KHÔNG phải dữ liệu người chơi dù
--                     tên bắt đầu bằng tob_. Xoá là Database.loadAllTubinhTemplate()
--                     nạp rỗng -> mỗi lần chọn nhân vật lv>=40 sẽ ném
--                     "Duplicate entry ... for key PRIMARY" ở addCharTubinh và
--                     ArrayIndexOutOfBounds ở createMsgCharTuBinh.
--                     Dữ liệu người chơi của tứ bình nằm ở tob_char_tubinh, khác bảng.
--    giftcode      -> định nghĩa mã quà (gồm mấy mã buff bạn vừa tạo).
--
--  Ghi chú: KHÔNG có bảng tob_item. Trang bị / hành trang / rương được
--  serialize thẳng vào 3 cột equip, inven, bag của tob_char, nên xoá
--  tob_char là bay hết đồ, không sót bảng nào.
-- ============================================================================


-- ############################################################################
--  PHẦN 1 — CÁCH KHUYẾN NGHỊ: SINH LỆNH RỒI TỰ CHẠY
--  Ưu điểm: bạn NHÌN THẤY chính xác những bảng nào sắp bị xoá trước khi bấm.
--
--  Bước 1a: chạy 2 câu này cùng lúc. Câu SET là bắt buộc — mặc định
--           group_concat_max_len chỉ 1024 ký tự, không đủ cho ~70 bảng,
--           thiếu nó là output bị cắt cụt giữa chừng.
-- ############################################################################

SET SESSION group_concat_max_len = 1000000;

SELECT GROUP_CONCAT(CONCAT('TRUNCATE TABLE `', TABLE_NAME, '`;') SEPARATOR '\n')
         AS lenh_can_chay
  FROM information_schema.TABLES
 WHERE TABLE_SCHEMA = DATABASE()
   AND TABLE_TYPE   = 'BASE TABLE'
   AND TABLE_NAME NOT IN (
        'data_attribute','data_item','data_monster','data_potion',
        'data_quest','data_shop','bad_word','admin_msg','5h_systems',
        '5h_active','tob_x2exp','giftcode','tob_event','tob_fruit',
        'tob_code_lixi','tob_tubinh'
   );

--  Bước 1b: copy nguyên khối kết quả ở ô lenh_can_chay, đọc lướt một lượt
--           xem có bảng nào bạn muốn giữ mà lọt vào không, rồi dán chạy.


-- ############################################################################
--  PHẦN 2 — CÁCH CHẠY MỘT PHÁT (không cần copy/dán)
--  Bỏ comment cả khối rồi chạy. Trong phpMyAdmin nhớ đặt ô "Delimiter"
--  thành $$ hoặc dùng mysql CLI.
-- ############################################################################

-- DROP PROCEDURE IF EXISTS wipe_players$$
-- CREATE PROCEDURE wipe_players()
-- BEGIN
--     DECLARE done INT DEFAULT 0;
--     DECLARE tbl  VARCHAR(128);
--     DECLARE cur CURSOR FOR
--         SELECT TABLE_NAME
--           FROM information_schema.TABLES
--          WHERE TABLE_SCHEMA = DATABASE()
--            AND TABLE_TYPE   = 'BASE TABLE'
--            AND TABLE_NAME NOT IN (
--                 'data_attribute','data_item','data_monster','data_potion',
--                 'data_quest','data_shop','bad_word','admin_msg','5h_systems',
--                 '5h_active','tob_x2exp','giftcode','tob_event','tob_fruit',
--                 'tob_code_lixi','tob_tubinh'
--            );
--     DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;
--
--     OPEN cur;
--     vong_lap: LOOP
--         FETCH cur INTO tbl;
--         IF done THEN LEAVE vong_lap; END IF;
--         SET @s = CONCAT('TRUNCATE TABLE `', tbl, '`');
--         PREPARE st FROM @s;
--         EXECUTE st;
--         DEALLOCATE PREPARE st;
--     END LOOP;
--     CLOSE cur;
-- END$$
--
-- CALL wipe_players()$$
-- DROP PROCEDURE wipe_players$$


-- ############################################################################
--  PHẦN 3 — CHẠY SAU KHI WIPE XONG (cả 2 cách trên đều cần)
--  Giữ nguyên kho mã lì xì, chỉ xoá dấu vết ai đã nhận.
-- ############################################################################

UPDATE `tob_code_lixi` SET `nhan` = 0, `timeNhan` = NULL, `charnhan` = NULL;


-- ============================================================================
--  TUỲ CHỌN — XOÁ LUÔN TÀI KHOẢN ĐĂNG NHẬP
--  Mặc định KHÔNG chạy. Bật khi bạn muốn mọi người đăng ký lại từ đầu.
--  Tài khoản nằm ở DB RIÊNG tên `account` (xem loginServer/server.ini),
--  không phải htbb_db. Bảng account.team_user KHÁC hoàn toàn
--  htbb_db.team_user (cái sau chỉ map provider/agent).
-- ============================================================================

-- TRUNCATE TABLE `account`.`team_user`;
-- TRUNCATE TABLE `htbb_db`.`5h_active`;
--   Chạy 2 câu trên rồi thì BẮT BUỘC sửa server.ini: sv.EnableAvtiveCheck=0
--   (nếu không thì tài khoản mới đăng ký sẽ không đăng nhập được vì
--    5h_active trống), hoặc tự thêm dòng vào 5h_active cho từng tài khoản.


-- ============================================================================
--  SAU KHI WIPE — 2 VIỆC BẮT BUỘC
-- ============================================================================
--
--  1. TẠO LẠI NHÂN VẬT ADMIN.
--     tob_char trống nghĩa là `ddsa` cũng mất. Vào game tạo lại nhân vật,
--     đặt tên CHÍNH XÁC là `ddsa` để khớp sv.adminAccounts trong server.ini
--     thì mới có lại quyền admin (Char.java:11934 so sánh charname viết thường).
--
--  2. KÍCH HOẠT LẠI TÀI KHOẢN nếu bạn đã chạy phần TUỲ CHỌN ở trên.
--     Nhắc lại: sv.EnableAvtiveCheck đang = 1, không có dòng trong 5h_active
--     là chặn ngay từ màn đăng nhập.
--
--  Mấy mã giftcode buff (tb8x, linhthu1, hoakylan, lkhoa1ty...) vẫn còn
--  nguyên vì bảng `giftcode` được giữ, và giftcode_log đã sạch nên gõ lại
--  được hết từ đầu.
--
--  Kiểm tra sau khi chạy — cả 3 phải ra 0:
--    SELECT COUNT(*) FROM tob_char;
--    SELECT COUNT(*) FROM tob_animal;
--    SELECT COUNT(*) FROM giftcode_log;
--  Và 2 cái này phải CÒN dữ liệu:
--    SELECT COUNT(*) FROM data_item;      -- ~900+
--    SELECT COUNT(*) FROM data_monster;   -- 138
-- ============================================================================
