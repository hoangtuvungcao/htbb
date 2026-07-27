-- ============================================================================
--  DỰNG DATABASE CHO SERVER MỚI — TRẮNG TINH DỮ LIỆU NGƯỜI CHƠI
--
--  Chạy trên VPS:
--      mysql -u root < /opt/kpah/vps/db_new_server.sql
--
--  CHẠY SAU KHI đã import 2 file:
--      mysql -u root kpah_db < /opt/kpah/gamedb7_clean.sql
--      mysql -u root account < /opt/kpah/loginServer/account.sql
--
--  Script này làm 5 việc:
--    1. Xoá sạch MỌI bảng dữ liệu người chơi trong kpah_db (đọc từ
--       information_schema nên không sót bảng log xoay vòng theo tháng)
--    2. Giữ nguyên 16 bảng dữ liệu game (item, monster, quest, shop, giftcode...)
--    3. Xoá sạch tài khoản: account.team_user + kpah_db.5h_active
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
SET @admin_pass = 'ijOwvrLkRzYcgmjP';

-- Phai chon database TRUOC khi tao procedure, neu khong MariaDB bao
-- "ERROR 1046 (3D000): No database selected".
USE kpah_db;


-- ############################################################################
--  PHẦN 0 — CHỐT AN TOÀN: kiểm tra mật khẩu TRƯỚC KHI xoá bất cứ thứ gì.
--  Đặt ở đây (không phải ở phần tạo tài khoản) để nếu bạn quên sửa mật khẩu
--  thì script dừng khi database vẫn còn nguyên vẹn.
-- ############################################################################
DROP PROCEDURE IF EXISTS kpah_kiem_tra_truoc;
DELIMITER $$
CREATE PROCEDURE kpah_kiem_tra_truoc()
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

CALL kpah_kiem_tra_truoc();
DROP PROCEDURE kpah_kiem_tra_truoc;


-- ############################################################################
--  PHẦN 1 — XOÁ SẠCH DỮ LIỆU NGƯỜI CHƠI TRONG kpah_db
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
DROP PROCEDURE IF EXISTS kpah_wipe_players;
DELIMITER $$
CREATE PROCEDURE kpah_wipe_players()
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE tbl  VARCHAR(128);
    DECLARE cur CURSOR FOR
        SELECT TABLE_NAME
          FROM information_schema.TABLES
         WHERE TABLE_SCHEMA = 'kpah_db'
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
        SET @s = CONCAT('TRUNCATE TABLE `kpah_db`.`', tbl, '`');
        PREPARE st FROM @s;
        EXECUTE st;
        DEALLOCATE PREPARE st;
    END LOOP;
    CLOSE cur;
END$$
DELIMITER ;

CALL kpah_wipe_players();
DROP PROCEDURE kpah_wipe_players;


-- ############################################################################
--  PHẦN 2 — GIỮ KHO MÃ LÌ XÌ, CHỈ XOÁ DẤU VẾT AI ĐÃ NHẬN
-- ############################################################################
UPDATE `tob_code_lixi` SET `nhan` = 0, `timeNhan` = NULL, `charnhan` = NULL;


-- ############################################################################
--  PHẦN 2B — BÙ 2 THỨ MÀ gamedb7_clean.sql BỊ THIẾU
--
--  Đã đối chiếu từng bảng giữa DB đang chạy và bản dump sạch: dump thiếu đúng
--  2 thứ, cả 2 đều cần cho server mới.
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
--  PHẦN 3 — XOÁ SẠCH TÀI KHOẢN ĐĂNG NHẬP
--
--  account.team_user   = tài khoản đăng nhập thật (login server đọc DB này)
--  kpah_db.5h_active   = bảng kích hoạt; thiếu dòng là bị chặn ngay màn login
--                        (sv.EnableAvtiveCheck=1)
--  kpah_db.team_user   = bảng KHÁC hoàn toàn, chỉ map provider/agent
-- ############################################################################
TRUNCATE TABLE `account`.`team_user`;
TRUNCATE TABLE `kpah_db`.`5h_active`;
ALTER TABLE `account`.`team_user` AUTO_INCREMENT = 1;


-- ############################################################################
--  PHẦN 4 — TẠO TÀI KHOẢN ADMIN
--
--  Mật khẩu băm bằng hàm PASSWORD() của MariaDB để KHỚP với login server
--  (CheckLoginSocket.jar so: ... and `password`=password('...')).
--  Đây là lý do KHÔNG dùng được MySQL 8 — bản đó đã bỏ hàm PASSWORD().
-- ############################################################################
DROP PROCEDURE IF EXISTS kpah_tao_admin;
DELIMITER $$
CREATE PROCEDURE kpah_tao_admin()
BEGIN
    SET @sql = CONCAT(
        'INSERT INTO `account`.`team_user` ',
        '(username,password,phone,regdate,ban,provider,fromgame) VALUES (',
        QUOTE(@admin_user), ',PASSWORD(', QUOTE(@admin_pass), "),'',NOW(),0,0,0)");
    PREPARE st FROM @sql;
    EXECUTE st;
    DEALLOCATE PREPARE st;

    -- Kich hoat tai khoan vua tao (time_end = '-1' la vinh vien)
    INSERT INTO `kpah_db`.`5h_active` (userID, username, time_end)
    SELECT id, username, '-1' FROM `account`.`team_user` WHERE username = @admin_user;
END$$
DELIMITER ;

CALL kpah_tao_admin();
DROP PROCEDURE kpah_tao_admin;


-- ############################################################################
--  PHẦN 5 — KIỂM TRA
--
--  Cột "phai_la" nói rõ giá trị đúng. Lệch là có gì sai, đừng bật server.
-- ############################################################################
SELECT 'tob_char (nhan vat)'        AS bang, COUNT(*) AS so_dong, '0' AS phai_la FROM kpah_db.tob_char
UNION ALL SELECT 'tob_animal',       COUNT(*), '0'      FROM kpah_db.tob_animal
UNION ALL SELECT 'tob_pet',          COUNT(*), '0'      FROM kpah_db.tob_pet
UNION ALL SELECT 'tob_clan',         COUNT(*), '0'      FROM kpah_db.tob_clan
UNION ALL SELECT 'tob_market',       COUNT(*), '0'      FROM kpah_db.tob_market
UNION ALL SELECT 'giftcode_log',     COUNT(*), '0'      FROM kpah_db.giftcode_log
UNION ALL SELECT 'board_created',    COUNT(*), '0'      FROM kpah_db.board_created
UNION ALL SELECT '--- du lieu game phai CON ---', NULL, NULL
UNION ALL SELECT 'data_item',        COUNT(*), '~904'   FROM kpah_db.data_item
UNION ALL SELECT 'data_monster',     COUNT(*), '138'    FROM kpah_db.data_monster
UNION ALL SELECT 'data_potion',      COUNT(*), '162'    FROM kpah_db.data_potion
UNION ALL SELECT 'data_shop',        COUNT(*), '148'    FROM kpah_db.data_shop
UNION ALL SELECT 'data_quest',       COUNT(*), '49'     FROM kpah_db.data_quest
UNION ALL SELECT 'data_attribute',   COUNT(*), '141'    FROM kpah_db.data_attribute
UNION ALL SELECT 'tob_tubinh (template)', COUNT(*), '16' FROM kpah_db.tob_tubinh
UNION ALL SELECT 'bad_word',         COUNT(*), '147'    FROM kpah_db.bad_word
UNION ALL SELECT '--- tai khoan ---', NULL, NULL
UNION ALL SELECT 'account.team_user', COUNT(*), '1 (admin)' FROM account.team_user
UNION ALL SELECT '5h_active',        COUNT(*), '1'      FROM kpah_db.5h_active;

-- ============================================================================
--  SAU KHI CHẠY — 2 VIỆC BẮT BUỘC
--
--  1. Bảng tob_skin_thu và tob_train_luong KHÔNG có trong bản dump.
--     Không cần làm gì: server tự CREATE TABLE IF NOT EXISTS lúc khởi động
--     (TeamServer gọi createTableSkinThuNuoi + createTableTrainLuong).
--
--  2. TẠO NHÂN VẬT ADMIN TRONG GAME.
--     Quyền admin xét theo TÊN NHÂN VẬT, không phải tên tài khoản:
--     server.ini -> sv.adminAccounts=admin,admin2
--     Nên đăng nhập bằng tài khoản vừa tạo rồi tạo nhân vật đặt tên đúng
--     "admin" (hoặc "admin2"). Tên chứa chữ "admin" đã bị chặn với người chơi
--     thường nên không ai giành được tên này trước bạn.
--
--  Muốn thêm mã giftcode buff của bạn (tb8x, linhthu1, hoakylan, lkhoa1ty...):
--      mysql -u root kpah_db < /opt/kpah/buff_ddsa.sql
-- ============================================================================
