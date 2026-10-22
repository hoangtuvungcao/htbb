-- =====================================================================================================
-- NGẮM TAY TRONG PvP (0.9.8): thông số bắn theo hướng của từng chiêu. IDEMPOTENT (MariaDB 10.3, utf8), tự chứa.
--
--   data_chieu_ban   (lop, chieu) -> kieu: dan (đạn bay thẳng, trúng người đầu tiên) / xuyen (tia xuyên, trúng mọi
--                    người trên đường tới toi_da) / non (hình nón góc `goc` độ) / vung (vùng tròn đường kính be_rong ở
--                    cuối tầm) / tu_than (buff, tức thì - không ngắm);
--                    tam (px, 0 = tầm gốc CharManager.SKILL_RANGE của chiêu; lớn hơn tầm gốc cũng bị kẹp về tầm gốc),
--                    be_rong (px; đạn / tia = bề rộng cả đường, cộng thêm bán kính thân 10 px khi xét trúng),
--                    toc_do (px/giây; 0 = tức thì), goc (độ, chỉ nón), toi_da (số mục tiêu tối đa; đạn luôn 1).
-- Máy chủ (real.BanHuongGame.nap) tự tạo bảng nếu thiếu; chiêu không có dòng thì dùng đúng mặc định dưới đây
-- (com.htbb.pokemon.BanHuong.macDinh). INSERT IGNORE: chạy lại không ghi đè dòng đã chỉnh.
-- Khoá cấu hình: pvp.ngam_tay (bật/tắt), pvp.ngam_tay.giay_giao_chien, pvp.ngam_tay.bu_tre_ms, pvp.ngam_tay.map.
-- =====================================================================================================

CREATE TABLE IF NOT EXISTS `data_chieu_ban` (
  `lop` TINYINT NOT NULL,
  `chieu` TINYINT NOT NULL,
  `kieu` VARCHAR(10) NOT NULL DEFAULT 'dan',
  `tam` SMALLINT NOT NULL DEFAULT 0,
  `be_rong` SMALLINT NOT NULL DEFAULT 20,
  `toc_do` SMALLINT NOT NULL DEFAULT 0,
  `goc` SMALLINT NOT NULL DEFAULT 60,
  `toi_da` TINYINT NOT NULL DEFAULT 1,
  `ghi_chu` VARCHAR(120) NOT NULL DEFAULT '',
  PRIMARY KEY (`lop`, `chieu`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT IGNORE INTO `data_chieu_ban` (`lop`, `chieu`, `kieu`, `tam`, `be_rong`, `toc_do`, `goc`, `toi_da`, `ghi_chu`) VALUES
  (0, 0, 'non', 0, 24, 0, 100, 2, 'đánh thường cận chiến: nón 100°'),
  (0, 1, 'dan', 0, 20, 480, 30, 1, 'đạn bay thẳng, trúng người đầu tiên'),
  (0, 2, 'xuyen', 0, 24, 0, 30, 3, 'tia xuyên tức thì, tối đa 3'),
  (0, 3, 'non', 0, 32, 0, 70, 4, 'nón 70°, tối đa 4'),
  (0, 4, 'tu_than', 0, 32, 0, 360, 1, 'buff / bản thân: tức thì (lệnh 51)'),
  (0, 5, 'tu_than', 0, 32, 0, 360, 1, 'buff / bản thân: tức thì (lệnh 51)'),
  (0, 6, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5'),
  (0, 7, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5'),
  (0, 8, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5'),
  (0, 9, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5'),
  (1, 0, 'non', 0, 24, 0, 100, 2, 'đánh thường cận chiến: nón 100°'),
  (1, 1, 'dan', 0, 20, 480, 30, 1, 'đạn bay thẳng, trúng người đầu tiên'),
  (1, 2, 'xuyen', 0, 24, 0, 30, 3, 'tia xuyên tức thì, tối đa 3'),
  (1, 3, 'non', 0, 32, 0, 70, 4, 'nón 70°, tối đa 4'),
  (1, 4, 'tu_than', 0, 32, 0, 360, 1, 'buff / bản thân: tức thì (lệnh 51)'),
  (1, 5, 'tu_than', 0, 32, 0, 360, 1, 'buff / bản thân: tức thì (lệnh 51)'),
  (1, 6, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5'),
  (1, 7, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5'),
  (1, 8, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5'),
  (1, 9, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5'),
  (2, 0, 'dan', 0, 16, 420, 30, 1, 'đánh thường: đạn bay thẳng'),
  (2, 1, 'dan', 0, 20, 480, 30, 1, 'đạn bay thẳng, trúng người đầu tiên'),
  (2, 2, 'xuyen', 0, 24, 0, 30, 3, 'tia xuyên tức thì, tối đa 3'),
  (2, 3, 'non', 0, 32, 0, 70, 4, 'nón 70°, tối đa 4'),
  (2, 4, 'tu_than', 0, 32, 0, 360, 1, 'buff / bản thân: tức thì (lệnh 51)'),
  (2, 5, 'tu_than', 0, 32, 0, 360, 1, 'buff / bản thân: tức thì (lệnh 51)'),
  (2, 6, 'tu_than', 0, 32, 0, 360, 1, 'buff / bản thân: tức thì (lệnh 51)'),
  (2, 7, 'tu_than', 0, 32, 0, 360, 1, 'buff / bản thân: tức thì (lệnh 51)'),
  (2, 8, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5'),
  (2, 9, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5'),
  (3, 0, 'non', 0, 24, 0, 100, 2, 'đánh thường cận chiến: nón 100°'),
  (3, 1, 'dan', 0, 20, 480, 30, 1, 'đạn bay thẳng, trúng người đầu tiên'),
  (3, 2, 'xuyen', 0, 24, 0, 30, 3, 'tia xuyên tức thì, tối đa 3'),
  (3, 3, 'non', 0, 32, 0, 70, 4, 'nón 70°, tối đa 4'),
  (3, 4, 'tu_than', 0, 32, 0, 360, 1, 'buff / bản thân: tức thì (lệnh 51)'),
  (3, 5, 'tu_than', 0, 32, 0, 360, 1, 'buff / bản thân: tức thì (lệnh 51)'),
  (3, 6, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5'),
  (3, 7, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5'),
  (3, 8, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5'),
  (3, 9, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5'),
  (4, 0, 'dan', 0, 16, 420, 30, 1, 'đánh thường: đạn bay thẳng'),
  (4, 1, 'dan', 0, 20, 480, 30, 1, 'đạn bay thẳng, trúng người đầu tiên'),
  (4, 2, 'xuyen', 0, 24, 0, 30, 3, 'tia xuyên tức thì, tối đa 3'),
  (4, 3, 'non', 0, 32, 0, 70, 4, 'nón 70°, tối đa 4'),
  (4, 4, 'tu_than', 0, 32, 0, 360, 1, 'buff / bản thân: tức thì (lệnh 51)'),
  (4, 5, 'tu_than', 0, 32, 0, 360, 1, 'buff / bản thân: tức thì (lệnh 51)'),
  (4, 6, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5'),
  (4, 7, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5'),
  (4, 8, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5'),
  (4, 9, 'vung', 0, 64, 0, 360, 5, 'vùng tròn Ø64 ở cuối tầm, tối đa 5');
