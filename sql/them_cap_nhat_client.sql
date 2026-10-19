-- ============================================================================
-- CAP NHAT CLIENT (0.9.7, ban 907) - chay lai bao nhieu lan cung duoc (MariaDB 10.3+, utf8mb4).
--
--   mysql -u root -p gamedb7 < sql/them_cap_nhat_client.sql
--
-- 1. Danh muc cau hinh trung tam: 3 khoa cua com.htbb.capnhat.CapNhatClient (ban sinh tu tools/cauhinh/danh_muc.json,
--    giong sql/pokemon_cau_hinh_trung_tam.sql - chay tep do cung duoc):
--      sv.banToiThieu  ban toi thieu: client gui so ban nho hon bi CHAN dang nhap (hop "Can cap nhat"), 0 = tat
--      sv.banMoiNhat   ban moi nhat DU PHONG khi ban tai chua ghi so ban, 0 = chi theo trang Ban tai game
--      sv.nhacCapNhat  nhac "Da co ban moi" (co dinh 1; tat khan cap trong server.ini)
--    Gia tri dat o web /admin/cau-hinh-he-thong (bang cau_hinh) - tep nay KHONG dat gia tri nao.
-- 2. Danh muc tai game (web_cai_dat khoa 'taiGame'): them truong "ban" = 0 cho moi ban tai chua co (so ban cua tep dang
--    phat; admin dien o /admin/tai-game khi phat hanh). Khong dong cham gia tri da co. Chua co bang / khoa thi khong lam gi.
-- ============================================================================
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `cau_hinh_danh_muc` (`khoa` VARCHAR(64) NOT NULL, `nhom` VARCHAR(20) NOT NULL, `ten` VARCHAR(160) NOT NULL DEFAULT '', `mo_ta` TEXT NOT NULL, `kieu` VARCHAR(12) NOT NULL, `mac_dinh` VARCHAR(255) NOT NULL DEFAULT '', `gioi_han` TEXT NOT NULL, `ap_dung` VARCHAR(16) NOT NULL, `nhay` TINYINT NOT NULL DEFAULT 0, `khoa_prod` TINYINT NOT NULL DEFAULT 0, `chi_doc` TINYINT NOT NULL DEFAULT 0, `bang` VARCHAR(16) NOT NULL DEFAULT 'cau_hinh', `dung_o` VARCHAR(10) NOT NULL DEFAULT 'server', `nguon` VARCHAR(255) NOT NULL DEFAULT '', `thu_tu` INT NOT NULL DEFAULT 0, `lop` VARCHAR(10) NOT NULL DEFAULT 'chinh', `an` TINYINT NOT NULL DEFAULT 0, PRIMARY KEY (`khoa`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.banToiThieu', 'bao_tri', 'Bản tối thiểu (bắt buộc cập nhật)', 'Số bản (BAN, vd 907). Client gửi số bản nhỏ hơn bị CHẶN đăng nhập, hiện hộp Cần cập nhật + nút Tải bản mới. Chỉ chặn nền tảng đã có bản tải đạt mức này ở trang Bản tải game. 0 = tắt.', 'so', '0', '{"min": 0, "max": 1000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.banToiThieu', 210, 'chinh', 0) ON DUPLICATE KEY UPDATE `nhom`=VALUES(`nhom`), `ten`=VALUES(`ten`), `mo_ta`=VALUES(`mo_ta`), `kieu`=VALUES(`kieu`), `mac_dinh`=VALUES(`mac_dinh`), `gioi_han`=VALUES(`gioi_han`), `ap_dung`=VALUES(`ap_dung`), `nguon`=VALUES(`nguon`), `lop`=VALUES(`lop`), `an`=VALUES(`an`);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.banMoiNhat', 'bao_tri', 'Bản mới nhất (dự phòng)', 'Bản mới nhất lấy theo số bản từng nền tảng ở trang Bản tải game (/admin/tai-game). Ô này chỉ dùng khi bản tải chưa ghi số bản. 0 = chỉ theo trang Bản tải game.', 'so', '0', '{"min": 0, "max": 1000000}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.banMoiNhat', 211, 'chinh', 0) ON DUPLICATE KEY UPDATE `nhom`=VALUES(`nhom`), `ten`=VALUES(`ten`), `mo_ta`=VALUES(`mo_ta`), `kieu`=VALUES(`kieu`), `mac_dinh`=VALUES(`mac_dinh`), `gioi_han`=VALUES(`gioi_han`), `ap_dung`=VALUES(`ap_dung`), `nguon`=VALUES(`nguon`), `lop`=VALUES(`lop`), `an`=VALUES(`an`);
INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`, `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`, `thu_tu`, `lop`, `an`) VALUES ('sv.nhacCapNhat', 'bao_tri', 'Nhắc khi có bản mới', 'Client cũ hơn bản mới nhất thấy thẻ Đã có bản mới (Cập nhật ngay / Để sau, nhắc một lần mỗi ngày). Không chặn chơi. Cố định bật; tắt khẩn cấp: sv.nhacCapNhat=0 trong server.ini.', 'co', '1', '{}', 'ngay', 0, 0, 0, 'cau_hinh', 'server', 'TeamServer.nhacCapNhat', 212, 'co_dinh', 1) ON DUPLICATE KEY UPDATE `nhom`=VALUES(`nhom`), `ten`=VALUES(`ten`), `mo_ta`=VALUES(`mo_ta`), `kieu`=VALUES(`kieu`), `mac_dinh`=VALUES(`mac_dinh`), `gioi_han`=VALUES(`gioi_han`), `ap_dung`=VALUES(`ap_dung`), `nguon`=VALUES(`nguon`), `lop`=VALUES(`lop`), `an`=VALUES(`an`);

CREATE TABLE IF NOT EXISTS `web_cai_dat` (
  `khoa`         varchar(64)  NOT NULL,
  `gia_tri`      mediumtext   NOT NULL,
  `cap_nhat_luc` datetime     NOT NULL DEFAULT current_timestamp(),
  `cap_nhat_boi` varchar(50)  NOT NULL DEFAULT '',
  PRIMARY KEY (`khoa`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

UPDATE `web_cai_dat` SET `gia_tri` = JSON_SET(`gia_tri`, '$.ban[0].ban', 0) WHERE `khoa` = 'taiGame' AND JSON_VALID(`gia_tri`) AND JSON_EXTRACT(`gia_tri`, '$.ban[0].ma') IS NOT NULL AND JSON_EXTRACT(`gia_tri`, '$.ban[0].ban') IS NULL;
UPDATE `web_cai_dat` SET `gia_tri` = JSON_SET(`gia_tri`, '$.ban[1].ban', 0) WHERE `khoa` = 'taiGame' AND JSON_VALID(`gia_tri`) AND JSON_EXTRACT(`gia_tri`, '$.ban[1].ma') IS NOT NULL AND JSON_EXTRACT(`gia_tri`, '$.ban[1].ban') IS NULL;
UPDATE `web_cai_dat` SET `gia_tri` = JSON_SET(`gia_tri`, '$.ban[2].ban', 0) WHERE `khoa` = 'taiGame' AND JSON_VALID(`gia_tri`) AND JSON_EXTRACT(`gia_tri`, '$.ban[2].ma') IS NOT NULL AND JSON_EXTRACT(`gia_tri`, '$.ban[2].ban') IS NULL;
UPDATE `web_cai_dat` SET `gia_tri` = JSON_SET(`gia_tri`, '$.ban[3].ban', 0) WHERE `khoa` = 'taiGame' AND JSON_VALID(`gia_tri`) AND JSON_EXTRACT(`gia_tri`, '$.ban[3].ma') IS NOT NULL AND JSON_EXTRACT(`gia_tri`, '$.ban[3].ban') IS NULL;
UPDATE `web_cai_dat` SET `gia_tri` = JSON_SET(`gia_tri`, '$.ban[4].ban', 0) WHERE `khoa` = 'taiGame' AND JSON_VALID(`gia_tri`) AND JSON_EXTRACT(`gia_tri`, '$.ban[4].ma') IS NOT NULL AND JSON_EXTRACT(`gia_tri`, '$.ban[4].ban') IS NULL;
UPDATE `web_cai_dat` SET `gia_tri` = JSON_SET(`gia_tri`, '$.ban[5].ban', 0) WHERE `khoa` = 'taiGame' AND JSON_VALID(`gia_tri`) AND JSON_EXTRACT(`gia_tri`, '$.ban[5].ma') IS NOT NULL AND JSON_EXTRACT(`gia_tri`, '$.ban[5].ban') IS NULL;
UPDATE `web_cai_dat` SET `gia_tri` = JSON_SET(`gia_tri`, '$.ban[6].ban', 0) WHERE `khoa` = 'taiGame' AND JSON_VALID(`gia_tri`) AND JSON_EXTRACT(`gia_tri`, '$.ban[6].ma') IS NOT NULL AND JSON_EXTRACT(`gia_tri`, '$.ban[6].ban') IS NULL;
UPDATE `web_cai_dat` SET `gia_tri` = JSON_SET(`gia_tri`, '$.ban[7].ban', 0) WHERE `khoa` = 'taiGame' AND JSON_VALID(`gia_tri`) AND JSON_EXTRACT(`gia_tri`, '$.ban[7].ma') IS NOT NULL AND JSON_EXTRACT(`gia_tri`, '$.ban[7].ban') IS NULL;
