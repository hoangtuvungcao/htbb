-- ============================================================================
--  ĐỔI TÊN NHÂN VẬT (dùng khi cần đặt tên bị chặn, ví dụ tên GM "admin")
--
--  Cách dùng trên VPS — chạy BẢN SAO trong /tmp để không sửa file trong repo
--  (sửa trực tiếp là lần `update.sh` sau sẽ báo "file bị sửa tay" và dừng):
--
--    systemctl stop htbb-game
--    sed -e "s/^SET @ten_cu .*/SET @ten_cu  = 'ten_hien_tai';/" \
--        -e "s/^SET @ten_moi .*/SET @ten_moi = 'ten_muon_doi';/" \
--        /opt/htbb/vps/doi_ten_nhan_vat.sql > /tmp/doiten.sql
--    grep "@ten_cu\|@ten_moi" /tmp/doiten.sql | head -2      # kiem tra lai
--    mysql -u root --default-character-set=utf8 -t < /tmp/doiten.sql
--    systemctl start htbb-game
--
--  BẮT BUỘC tắt server trước: server giữ Char trong RAM, còn chạy là nó ghi đè
--  lại tên cũ khi tự động lưu (mỗi vài phút), coi như công cốc.
--
--  Script tự tìm MỌI cột lưu tên nhân vật trong htbb_db (charname / namechar /
--  bangchu / owner / charnhan) rồi cập nhật hết — kể cả bảng log xoay vòng theo
--  tháng mà mỗi server một khác. Không viết cứng danh sách bảng.
-- ============================================================================

SET @ten_cu  = 'gmhtbb';
SET @ten_moi = 'admin';

USE htbb_db;

DROP PROCEDURE IF EXISTS htbb_doi_ten;
DELIMITER $$
CREATE PROCEDURE htbb_doi_ten()
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE tbl  VARCHAR(128);
    DECLARE col  VARCHAR(128);
    DECLARE n    INT DEFAULT 0;
    -- CHI lay cot kieu CHUOI. Cot "owner" trong tob_pet / tob_animal / tob_farm /
    -- tob_gem_new / tob_sp_new / 5h_notify la INT (charDBID, khong phai ten) — so sanh
    -- owner='tenchar' bi MySQL ep chuoi thanh 0 roi khop sai hang loat dong owner=0.
    DECLARE cur CURSOR FOR
        SELECT TABLE_NAME, COLUMN_NAME
          FROM information_schema.COLUMNS
         WHERE TABLE_SCHEMA = 'htbb_db'
           AND COLUMN_NAME IN ('charname','namechar','bangchu','owner','charnhan')
           AND DATA_TYPE IN ('varchar','char','text','tinytext','mediumtext','longtext');
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    -- Chan truoc: ten moi da co nguoi dung thi dung lai (tob_char.charname la UNIQUE)
    IF (SELECT COUNT(*) FROM tob_char WHERE charname = @ten_moi) > 0 THEN
        SIGNAL SQLSTATE '45000'
          SET MESSAGE_TEXT = 'DUNG LAI: da co nhan vat mang ten moi. Doi @ten_moi khac.';
    END IF;
    IF (SELECT COUNT(*) FROM tob_char WHERE charname = @ten_cu) = 0 THEN
        SIGNAL SQLSTATE '45000'
          SET MESSAGE_TEXT = 'DUNG LAI: khong tim thay nhan vat mang @ten_cu.';
    END IF;

    OPEN cur;
    vong_lap: LOOP
        FETCH cur INTO tbl, col;
        IF done THEN LEAVE vong_lap; END IF;
        SET @s = CONCAT('UPDATE `htbb_db`.`', tbl, '` SET `', col, '` = ',
                        QUOTE(@ten_moi), ' WHERE `', col, '` = ', QUOTE(@ten_cu));
        PREPARE st FROM @s;
        EXECUTE st;
        SET n = n + ROW_COUNT();
        DEALLOCATE PREPARE st;
    END LOOP;
    CLOSE cur;

    SELECT CONCAT('Da doi ', n, ' dong tu "', @ten_cu, '" sang "', @ten_moi, '"') AS ket_qua;
END$$
DELIMITER ;

CALL htbb_doi_ten();
DROP PROCEDURE htbb_doi_ten;

-- Kiem tra
SELECT id, charname, userId, ban, del FROM htbb_db.tob_char;
