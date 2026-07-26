-- ============================================================================
--  VÁ LỖI TRÀN CỘT LƯU DỮ LIỆU NHÂN VẬT
--
--  LỖI GẶP PHẢI:
--    com.mysql.jdbc.MysqlDataTruncation: Data truncation:
--    Data too long for column 'inven' at row 1
--        at data.Database.saveCharAuto(Database.java:12903)
--
--  MỨC ĐỘ: NGHIÊM TRỌNG. Câu UPDATE thất bại nghĩa là lần autosave đó
--  KHÔNG lưu được gì cả — nhân vật mất toàn bộ tiến trình từ lần lưu trước.
--
--  NGUYÊN NHÂN:
--    Toàn bộ hành trang được serialize thành MỘT chuỗi rồi nhét vào cột `inven`.
--    Cột đó kiểu TEXT = trần 65.535 byte.
--    Lúc phát hiện, nhân vật 'dasdf' đã dùng 52.890 byte — sát trần.
--
--    Hai thứ cộng hưởng làm chuỗi phình ra:
--      1. Nâng số túi hành trang từ 3 lên 5 (Char.MAX_BAG_LIMIT)
--         -> chứa được ~210 món thay vì ~126.
--      2. Phát nhiều đồ qua giftcode (88 món thời trang, sách, trứng...).
--    Mỗi món serialize khoảng 250 byte, 210 món là đã vượt 65.535.
--
--  CÁCH VÁ: nới TEXT -> MEDIUMTEXT (16 MB). Không mất dữ liệu đang có.
--
--  ĐÃ CHẠY trên kpah_db. Giữ file để cài lại sau khi wipe/import.
-- ============================================================================

ALTER TABLE `tob_char`
  MODIFY `equip` MEDIUMTEXT,
  MODIFY `inven` MEDIUMTEXT,
  MODIFY `bag`   MEDIUMTEXT,
  MODIFY `tuido` MEDIUMTEXT;


-- ---- Cùng loại lỗi, vá luôn cho kho nguyên liệu ----------------------------
--  tob_gem_new lưu 3 chuỗi song song cho 269 loại nguyên liệu:
--    listtemplate = "0,1,2,...,268"  -> đã 965/1000 ký tự, gần tràn
--    soluong      = số lượng từng loại, càng nhiều số càng dài
--  Cột cũ là varchar(1000). Tràn thì MySQL cắt cụt ÂM THẦM -> hỏng kho đồ.
ALTER TABLE `tob_gem_new`
  MODIFY `listtemplate` MEDIUMTEXT NOT NULL,
  MODIFY `soluong`      MEDIUMTEXT NOT NULL,
  MODIFY `slock`        MEDIUMTEXT NOT NULL;


-- ---- Kiểm tra sau khi chạy: cả 7 cột phải là mediumtext --------------------
SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE
  FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA = DATABASE()
   AND ( (TABLE_NAME='tob_char'     AND COLUMN_NAME IN ('equip','inven','bag','tuido'))
      OR (TABLE_NAME='tob_gem_new'  AND COLUMN_NAME IN ('listtemplate','soluong','slock')) )
 ORDER BY TABLE_NAME, COLUMN_NAME;

-- ---- Xem nhân vật nào đang phình to nhất -----------------------------------
SELECT `charname`, LENGTH(`inven`) AS len_inven, LENGTH(`equip`) AS len_equip
  FROM `tob_char` ORDER BY LENGTH(`inven`) DESC LIMIT 10;

-- ============================================================================
--  GHI CHÚ
--  Bảng tob_char2 được nhắc tới trong Database.java (saveChar bản 2) nhưng
--  KHÔNG tồn tại trong DB. Đường lưu đó luôn ném exception. Không sửa vì
--  đường lưu chính (tob_char) vẫn chạy đúng.
-- ============================================================================
