-- =====================================================================================================
-- ĐIỂM MÓC NHIỆM VỤ (0.9.7): vật phẩm thật của từng sự kiện + tiến độ chợ cho người bán offline.
-- Nâng cấp IDEMPOTENT (MariaDB 10.3, utf8), tự chứa, nối được vào sql/nang_cap.sql.
--
--   data_su_kien_vat_pham  vật phẩm (id trong potions[] / data_potion) mà CODE của từng sự kiện thả / thưởng vào túi:
--                          quái rơi (real.Monster.onDropItem), NPC sự kiện (MapMenuNpc), giao hàng (TraHang), chế đồ /
--                          tiến giải Vu Lan (MapCheTao, Map). Kiểu mục tiêu "su_kien_vat_pham" (real.DiemMocNhiemVu)
--                          chỉ đếm vật phẩm thuộc một sự kiện ĐANG BẬT (SuKien.bat(ma_su_kien)). Nhiệm vụ đặt
--                          muc_tieu_id = item_id để nhắm một vật phẩm, để trống = bất kỳ vật phẩm của sự kiện đang chạy.
--                          Sự kiện trong lịch năm KHÔNG có vật phẩm rơi trong code (tet123, quocte83, gioto,
--                          demhalloween, sinhnhat2011, blackfriday*, noel, demnoel) không có dòng.
--   tob_nhiem_vu_cho       người bán OFFLINE bán được hàng trên Chợ sinh thái: một dòng chờ, áp vào nhiệm vụ "cho_ban"
--                          lần đầu vào bản đồ (da_ap 0 -> 1, mỗi dòng áp đúng một lần).
-- Kiểm: server/test/KiemPvpPk (mọi id vật phẩm trong các khối rơi sự kiện của Monster.java đều có dòng ở đây).
-- =====================================================================================================

CREATE TABLE IF NOT EXISTS `data_su_kien_vat_pham` (
  `ma_su_kien` VARCHAR(32) NOT NULL,
  `item_id` INT NOT NULL,
  `ten` VARCHAR(64) NOT NULL DEFAULT '',
  `nguon` VARCHAR(128) NOT NULL DEFAULT '',
  PRIMARY KEY (`ma_su_kien`, `item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `data_su_kien_vat_pham` (`ma_su_kien`, `item_id`, `ten`, `nguon`) VALUES
('trungthu', 120, 'Thịt', 'Monster: quái rơi nguyên liệu Trung Thu'),
('trungthu', 121, 'Trứng', 'Monster: quái rơi nguyên liệu Trung Thu'),
('trungthu', 142, 'Bột mỳ', 'Monster: quái rơi nguyên liệu Trung Thu; MapMenuNpc: hộp quà NPC'),
('trungthu', 122, 'Đậu xanh', 'Monster: quái rơi khi có buff sôcôla'),
('tet2017', 148, 'Gạo nếp', 'Monster: quái rơi nguyên liệu bánh chưng'),
('tet2017', 122, 'Đậu xanh', 'Monster: quái rơi nguyên liệu bánh chưng'),
('tet2017', 149, 'Thịt mỡ', 'Monster: quái rơi nguyên liệu bánh chưng'),
('tet2017', 150, 'Lá dong', 'Monster: quái rơi nguyên liệu bánh chưng'),
('tet2017', 153, 'Giấy', 'Monster: quái rơi nguyên liệu pháo'),
('tet2017', 154, 'Thuốc pháo', 'Monster: quái rơi nguyên liệu pháo'),
('halloween', 137, 'Kẹo', 'Monster: quái rơi'),
('halloween', 138, 'Bí ngô', 'Monster: quái rơi'),
('tetduonglich', 137, 'Kẹo', 'Monster: quái rơi (mặc gậy Giáng sinh)'),
('noel2023', 145, 'Dưa Hấu', 'Monster: quái rơi'),
('he2017', 146, 'Kem', 'Monster: quái rơi'),
('vulan', 106, 'Nhánh hoa hồng vàng', 'Monster: quái rơi; MapCheTao / Map: chế đồ, tiến giải'),
('worldcup2017', 160, 'Quả bóng bạc', 'Monster: quái rơi; TraHang: giao hàng'),
('worldcup', 161, 'Quả bóng vàng', 'SuKienWorldCup: quái / boss rơi'),
('mininuichaubau', 77, 'Mảnh bản đồ 1', 'Monster: quái rơi')
ON DUPLICATE KEY UPDATE `ten` = VALUES(`ten`), `nguon` = VALUES(`nguon`);

CREATE TABLE IF NOT EXISTS `tob_nhiem_vu_cho` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `id_char` INT NOT NULL,
  `so` INT NOT NULL DEFAULT 1,
  `ghi_chu` VARCHAR(128) NOT NULL DEFAULT '',
  `da_ap` TINYINT NOT NULL DEFAULT 0,
  `luc` DATETIME NOT NULL,
  `ap_luc` DATETIME NULL,
  PRIMARY KEY (`id`),
  KEY `idx_nvc_char` (`id_char`, `da_ap`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
