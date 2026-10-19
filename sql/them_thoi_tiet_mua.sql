-- =====================================================================================================
-- THỜI TIẾT THEO KHUNG GIỜ, PHỦ MÀU MÙA, NGÀY CỘNG ĐỒNG HẰNG THÁNG (đề xuất 8, chủ game duyệt 19/10/2026).
-- Nâng cấp IDEMPOTENT (MariaDB 10.3, utf8): chạy lại bao nhiêu lần cũng được; tự chứa, nối được vào sql/nang_cap.sql.
--   mysql htbb_db < sql/them_thoi_tiet_mua.sql
--
--   data_thoi_tiet           7 thời tiết: hệ được +% / -% sát thương chiêu, hệ sinh nhiều, gợi ý HUD. Máy chủ KẸP
--                            |%| <= 10 (com.htbb.pokemon.ThoiTietLich.TRAN_SAT_THUONG_PHAN_TRAM) dù bảng ghi gì.
--   data_thoi_tiet_map       bản đồ cố định một thời tiết (thoi_tiet_co_dinh >= 0) / tắt phủ màu mùa (tat_mau_mua = 1).
--                            Rỗng = mọi bản đồ theo vùng sinh thái (data_hst_vung).
--   data_thoi_tiet_su_kien   sự kiện đang bật (tob_su_kien.bat theo lịch năm) thêm trọng số một thời tiết (<= 60) và
--                            đặt màu phủ (uu_tien cao nhất thắng).
--   data_ngay_cong_dong      dòng Pokémon nổi bật mỗi tháng (nam_xoay 0 = năm chẵn kể từ 2026, 1 = năm lẻ): id quái
--                            của dòng (mỗi loài trên bản đồ tự nhiên của nó, data_hst_bung_phat), giờ vàng, % hồi sinh
--                            (<= 50), % EXP khi hạ (<= 25), số hạ để mở danh hiệu, danh hiệu.
--   data_danh_hieu 9801..9817 danh hiệu dòng (điều kiện cong_dong / tap:cong_dong, đếm trong giờ vàng).
-- Ngày cộng đồng bật / tắt theo lịch năm (moi:congdong, com.htbb.pokemon.NgayCongDong): một Chủ nhật mỗi tháng
-- 04:00 -> 04:00, web tự đăng tin. Không xu, không lượng: chỉ thời gian + hình + danh hiệu (ngân sách ô 1%).
-- =====================================================================================================

CREATE TABLE IF NOT EXISTS `data_thoi_tiet` (
  `id` TINYINT NOT NULL,
  `ma` VARCHAR(16) NOT NULL DEFAULT '',
  `ten` VARCHAR(32) NOT NULL DEFAULT '',
  `he_tang` VARCHAR(40) NOT NULL DEFAULT '' COMMENT 'hệ chiêu +phan_tram (HePokemon, phẩy)',
  `he_giam` VARCHAR(40) NOT NULL DEFAULT '' COMMENT 'hệ chiêu -phan_tram',
  `he_sinh` VARCHAR(40) NOT NULL DEFAULT '' COMMENT 'hệ sinh nhiều (x3)',
  `phan_tram` TINYINT NOT NULL DEFAULT 10 COMMENT 'kẹp <= 10',
  `goi_y` VARCHAR(160) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `data_thoi_tiet` (`id`,`ma`,`ten`,`he_tang`,`he_giam`,`he_sinh`,`phan_tram`,`goi_y`) VALUES
(0,'quang','Quang đãng','','','',0,'Trời đẹp, không ảnh hưởng chiêu.'),
(1,'nang','Nắng gắt','1','2','1,4',10,'Pokémon hệ Lửa, Cỏ xuất hiện nhiều.'),
(2,'mua','Mưa','2','1','2,3',10,'Pokémon hệ Nước, Điện xuất hiện nhiều.'),
(3,'tuyet','Tuyết','5','','5',10,'Pokémon hệ Băng xuất hiện nhiều.'),
(4,'bao_cat','Bão cát','12','','12,8,16',10,'Pokémon hệ Đá, Đất, Thép xuất hiện nhiều.'),
(5,'suong_mu','Sương mù','13','','13,10',10,'Pokémon hệ Ma, Siêu linh xuất hiện nhiều.'),
(6,'gio','Gió mạnh','9','','9,14',10,'Pokémon hệ Bay, Rồng xuất hiện nhiều.')
ON DUPLICATE KEY UPDATE `ma`=VALUES(`ma`), `ten`=VALUES(`ten`), `he_tang`=VALUES(`he_tang`), `he_giam`=VALUES(`he_giam`),
  `he_sinh`=VALUES(`he_sinh`), `phan_tram`=VALUES(`phan_tram`), `goi_y`=VALUES(`goi_y`);

CREATE TABLE IF NOT EXISTS `data_thoi_tiet_map` (
  `map_id` INT NOT NULL,
  `thoi_tiet_co_dinh` TINYINT NOT NULL DEFAULT -1 COMMENT '-1 = theo vùng',
  `tat_mau_mua` TINYINT NOT NULL DEFAULT 0,
  `ghi_chu` VARCHAR(120) NOT NULL DEFAULT '',
  PRIMARY KEY (`map_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `data_thoi_tiet_su_kien` (
  `ma_su_kien` VARCHAR(40) NOT NULL,
  `vung` TINYINT NOT NULL DEFAULT -1 COMMENT '-1 = mọi vùng ngoài trời',
  `thoi_tiet` TINYINT NOT NULL,
  `trong_so` TINYINT NOT NULL DEFAULT 0 COMMENT 'kẹp <= 60',
  `mau_mua` TINYINT NOT NULL DEFAULT 0 COMMENT 'ThoiTietLich.MAU_*: 5 Halloween, 6 Tết, 7 Noel, 8 Trung Thu',
  `uu_tien` INT NOT NULL DEFAULT 0,
  `bat` TINYINT NOT NULL DEFAULT 1,
  PRIMARY KEY (`ma_su_kien`, `vung`, `thoi_tiet`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `data_thoi_tiet_su_kien` (`ma_su_kien`,`vung`,`thoi_tiet`,`trong_so`,`mau_mua`,`uu_tien`) VALUES
('halloween',-1,5,30,5,50),
('noel',-1,3,25,7,50),
('tet2017',-1,0,20,6,60),
('trungthu',-1,0,20,8,40),
('he2017',-1,1,15,0,10)
ON DUPLICATE KEY UPDATE `trong_so`=VALUES(`trong_so`), `mau_mua`=VALUES(`mau_mua`), `uu_tien`=VALUES(`uu_tien`);

CREATE TABLE IF NOT EXISTS `data_ngay_cong_dong` (
  `thang` TINYINT NOT NULL,
  `nam_xoay` TINYINT NOT NULL DEFAULT 0 COMMENT '0 năm chẵn (2026, 2028...), 1 năm lẻ',
  `dong` TINYINT NOT NULL COMMENT 'số dòng (bộ đếm danh hiệu)',
  `ten` VARCHAR(32) NOT NULL DEFAULT '',
  `id_quai` VARCHAR(60) NOT NULL DEFAULT '' COMMENT 'id template các loài của dòng (phẩy)',
  `map_ids` VARCHAR(120) NOT NULL DEFAULT '' COMMENT 'bản đồ thêm (rỗng = chỉ bản đồ tự nhiên)',
  `gio_bat_dau` TINYINT NOT NULL DEFAULT 14,
  `so_gio` TINYINT NOT NULL DEFAULT 3 COMMENT 'kẹp <= 4',
  `ti_le` TINYINT NOT NULL DEFAULT 40 COMMENT '% lần hồi sinh, kẹp <= 50',
  `exp_phan_tram` TINYINT NOT NULL DEFAULT 20 COMMENT 'kẹp <= 25, chung trần +100%',
  `so_ha` INT NOT NULL DEFAULT 30,
  `id_danh_hieu` INT NOT NULL DEFAULT 0,
  `chieu` VARCHAR(60) NOT NULL DEFAULT '' COMMENT 'chiêu nổi bật (chờ learnset hoc-chieu)',
  `bat` TINYINT NOT NULL DEFAULT 1,
  PRIMARY KEY (`thang`, `nam_xoay`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `data_ngay_cong_dong` (`thang`,`nam_xoay`,`dong`,`ten`,`id_quai`,`map_ids`,`gio_bat_dau`,`so_gio`,`ti_le`,`exp_phan_tram`,`so_ha`,`id_danh_hieu`,`chieu`) VALUES
(1,0,1,'Torchic','4,14,44','',14,3,40,20,30,9801,''),
(2,0,11,'Caterpie','2,24','',14,3,40,20,30,9811,''),
(3,0,2,'Oddish','10,20,58','',14,3,40,20,30,9802,''),
(4,0,8,'Paras','19,32','',14,3,40,20,30,9808,''),
(5,0,12,'Wooper','3,13','',14,3,40,20,30,9812,''),
(6,0,7,'Growlithe','28,48','',14,3,40,20,30,9807,''),
(7,0,13,'Luxio','62,137','',14,3,40,20,30,9813,''),
(8,0,5,'Ekans','5,23','',14,3,40,20,30,9805,''),
(9,0,14,'Swablu','143,164','',14,3,40,20,30,9814,''),
(10,0,4,'Gastly','25,52','',14,3,40,20,30,9804,''),
(11,0,3,'Venipede','21,40,42,41','',14,3,40,20,30,9803,''),
(12,0,10,'Aron','146,156,169','',14,3,40,20,30,9810,''),
(1,1,2,'Oddish','10,20,58','',14,3,40,20,30,9802,''),
(2,1,1,'Torchic','4,14,44','',14,3,40,20,30,9801,''),
(3,1,8,'Paras','19,32','',14,3,40,20,30,9808,''),
(4,1,11,'Caterpie','2,24','',14,3,40,20,30,9811,''),
(5,1,7,'Growlithe','28,48','',14,3,40,20,30,9807,''),
(6,1,12,'Wooper','3,13','',14,3,40,20,30,9812,''),
(7,1,5,'Ekans','5,23','',14,3,40,20,30,9805,''),
(8,1,13,'Luxio','62,137','',14,3,40,20,30,9813,''),
(9,1,16,'Baltoy','148,163','',14,3,40,20,30,9816,''),
(10,1,9,'Duskull','55,57','',14,3,40,20,30,9809,''),
(11,1,6,'Cubone','27,59','',14,3,40,20,30,9806,''),
(12,1,15,'Bagon','157,167','',14,3,40,20,30,9815,'')
ON DUPLICATE KEY UPDATE `dong`=VALUES(`dong`), `ten`=VALUES(`ten`), `id_quai`=VALUES(`id_quai`), `id_danh_hieu`=VALUES(`id_danh_hieu`);

-- Danh hiệu dòng ngày cộng đồng (Thường, +0,5% HP - một danh hiệu đeo, ngân sách ô 1%) + "Trụ cột cộng đồng" (6 dòng).
-- Bảng do sql/them_danh_hieu.sql tạo; ON DUPLICATE không ghi đè chi_so / phan_nghin / bat (chủ game chỉnh).
CREATE TABLE IF NOT EXISTS `data_danh_hieu` (
  `id` INT NOT NULL,
  `ma` VARCHAR(48) NOT NULL,
  `ten` VARCHAR(64) NOT NULL DEFAULT '',
  `mo_ta` VARCHAR(255) NOT NULL DEFAULT '',
  `goi_y` VARCHAR(255) NOT NULL DEFAULT '',
  `do_hiem` TINYINT NOT NULL DEFAULT 0,
  `hinh` VARCHAR(16) NOT NULL DEFAULT '',
  `chi_so` VARCHAR(24) NOT NULL DEFAULT '',
  `phan_nghin` INT NOT NULL DEFAULT 0,
  `dieu_kien` VARCHAR(40) NOT NULL DEFAULT 'trao',
  `tham_so` VARCHAR(64) NOT NULL DEFAULT '',
  `so_luong` INT NOT NULL DEFAULT 1,
  `an` TINYINT NOT NULL DEFAULT 0,
  `thoi_han_phut` INT NOT NULL DEFAULT 0,
  `nguon` VARCHAR(16) NOT NULL DEFAULT '',
  `thu_tu` INT NOT NULL DEFAULT 0,
  `bat` TINYINT NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_ddh_ma` (`ma`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
INSERT INTO `data_danh_hieu` (`id`,`ma`,`ten`,`mo_ta`,`goi_y`,`do_hiem`,`hinh`,`chi_so`,`phan_nghin`,`dieu_kien`,`tham_so`,`so_luong`,`an`,`thoi_han_phut`,`nguon`,`thu_tu`,`bat`) VALUES
(9801,'cong_dong_1','Bạn đồng hành Torchic','Hạ 30 Pokémon dòng Torchic trong giờ vàng ngày cộng đồng.','',0,'tim','hp',5,'cong_dong','1',30,0,0,'su_kien',301,1),
(9802,'cong_dong_2','Bạn đồng hành Oddish','Hạ 30 Pokémon dòng Oddish trong giờ vàng ngày cộng đồng.','',0,'tim','hp',5,'cong_dong','2',30,0,0,'su_kien',302,1),
(9803,'cong_dong_3','Bạn đồng hành Venipede','Hạ 30 Pokémon dòng Venipede trong giờ vàng ngày cộng đồng.','',0,'tim','hp',5,'cong_dong','3',30,0,0,'su_kien',303,1),
(9804,'cong_dong_4','Bạn đồng hành Gastly','Hạ 30 Pokémon dòng Gastly trong giờ vàng ngày cộng đồng.','',0,'tim','hp',5,'cong_dong','4',30,0,0,'su_kien',304,1),
(9805,'cong_dong_5','Bạn đồng hành Ekans','Hạ 30 Pokémon dòng Ekans trong giờ vàng ngày cộng đồng.','',0,'tim','hp',5,'cong_dong','5',30,0,0,'su_kien',305,1),
(9806,'cong_dong_6','Bạn đồng hành Cubone','Hạ 30 Pokémon dòng Cubone trong giờ vàng ngày cộng đồng.','',0,'tim','hp',5,'cong_dong','6',30,0,0,'su_kien',306,1),
(9807,'cong_dong_7','Bạn đồng hành Growlithe','Hạ 30 Pokémon dòng Growlithe trong giờ vàng ngày cộng đồng.','',0,'tim','hp',5,'cong_dong','7',30,0,0,'su_kien',307,1),
(9808,'cong_dong_8','Bạn đồng hành Paras','Hạ 30 Pokémon dòng Paras trong giờ vàng ngày cộng đồng.','',0,'tim','hp',5,'cong_dong','8',30,0,0,'su_kien',308,1),
(9809,'cong_dong_9','Bạn đồng hành Duskull','Hạ 30 Pokémon dòng Duskull trong giờ vàng ngày cộng đồng.','',0,'tim','hp',5,'cong_dong','9',30,0,0,'su_kien',309,1),
(9810,'cong_dong_10','Bạn đồng hành Aron','Hạ 30 Pokémon dòng Aron trong giờ vàng ngày cộng đồng.','',0,'tim','hp',5,'cong_dong','10',30,0,0,'su_kien',310,1),
(9811,'cong_dong_11','Bạn đồng hành Caterpie','Hạ 30 Pokémon dòng Caterpie trong giờ vàng ngày cộng đồng.','',0,'tim','hp',5,'cong_dong','11',30,0,0,'su_kien',311,1),
(9812,'cong_dong_12','Bạn đồng hành Wooper','Hạ 30 Pokémon dòng Wooper trong giờ vàng ngày cộng đồng.','',0,'tim','hp',5,'cong_dong','12',30,0,0,'su_kien',312,1),
(9813,'cong_dong_13','Bạn đồng hành Luxio','Hạ 30 Pokémon dòng Luxio trong giờ vàng ngày cộng đồng.','',0,'tim','hp',5,'cong_dong','13',30,0,0,'su_kien',313,1),
(9814,'cong_dong_14','Bạn đồng hành Swablu','Hạ 30 Pokémon dòng Swablu trong giờ vàng ngày cộng đồng.','',0,'tim','hp',5,'cong_dong','14',30,0,0,'su_kien',314,1),
(9815,'cong_dong_15','Bạn đồng hành Bagon','Hạ 30 Pokémon dòng Bagon trong giờ vàng ngày cộng đồng.','',0,'tim','hp',5,'cong_dong','15',30,0,0,'su_kien',315,1),
(9816,'cong_dong_16','Bạn đồng hành Baltoy','Hạ 30 Pokémon dòng Baltoy trong giờ vàng ngày cộng đồng.','',0,'tim','hp',5,'cong_dong','16',30,0,0,'su_kien',316,1),
(9817,'cong_dong_6_dong','Trụ cột cộng đồng','Tham gia giờ vàng ngày cộng đồng của 6 dòng khác nhau.','',1,'sao','ne_tranh',7,'tap:cong_dong','',6,0,0,'su_kien',317,1)
ON DUPLICATE KEY UPDATE `ma`=VALUES(`ma`), `ten`=VALUES(`ten`), `mo_ta`=VALUES(`mo_ta`), `goi_y`=VALUES(`goi_y`), `do_hiem`=VALUES(`do_hiem`), `hinh`=VALUES(`hinh`), `dieu_kien`=VALUES(`dieu_kien`), `tham_so`=VALUES(`tham_so`), `so_luong`=VALUES(`so_luong`), `an`=VALUES(`an`), `thoi_han_phut`=VALUES(`thoi_han_phut`), `nguon`=VALUES(`nguon`), `thu_tu`=VALUES(`thu_tu`);
