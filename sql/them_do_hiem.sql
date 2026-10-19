-- DO HIEM CHUNG (de xuat 4, chu game duyet 19/10/2026) - SINH TU tools/cauhinh/do_hiem.json boi
-- tools/cauhinh/sinh_do_hiem.py, KHONG SUA TAY. Idempotent (MariaDB 10.3, utf8): chay lai bao nhieu lan cung duoc.
-- Mau trong game / web doc ban sinh trong ma nguon (cung nguon); bang nay de tra cuu, admin va cong cu ngoai.
--   mysql htbb_db < sql/them_do_hiem.sql

CREATE TABLE IF NOT EXISTS `data_do_hiem` (
  `id` TINYINT NOT NULL,
  `ma` VARCHAR(16) NOT NULL DEFAULT '',
  `ten` VARCHAR(32) NOT NULL DEFAULT '',
  `mau` VARCHAR(7) NOT NULL DEFAULT '' COMMENT 'chu / vien tren nen sang',
  `nhat` VARCHAR(7) NOT NULL DEFAULT '' COMMENT 'nen nhat cung tong',
  `sang` VARCHAR(7) NOT NULL DEFAULT '' COMMENT 'chu tren nen toi / the gioi',
  `phu_kien` VARCHAR(40) NOT NULL DEFAULT '' COMMENT '5 tong phu kien: vien,toi,goc,sang,diem',
  `vien_dong` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `data_do_hiem` (`id`,`ma`,`ten`,`mau`,`nhat`,`sang`,`phu_kien`,`vien_dong`) VALUES
(0,'thuong','Thường','#5F6B7A','#EEF1F5','#F2F4F7','#3A4250,#9AA2B0,#D8DCE4,#FFFFFF,#8FA8C8',0),
(1,'hiem','Hiếm','#23803A','#E2F5E5','#7EE08A','#123A1C,#2A8A3E,#52C866,#C2F2C8,#FFFFFF',0),
(2,'quy','Quý','#2366C0','#E3EEFC','#8CC8FF','#123060,#2A60B8,#48A0F8,#B8E0FF,#FFFFFF',0),
(3,'su_thi','Sử thi','#7B45D6','#EEE8FC','#CDB8FF','#30104A,#6A2CA0,#A45CE8,#E0C0FF,#FFE070',0),
(4,'huyen_thoai','Huyền thoại','#B95A0E','#FFEEDB','#FFC46B','#4A2006,#B45A10,#F08A28,#FFD8A0,#FFFFFF',1),
(5,'trang_tri','Trang trí','#8A6A00','#FFF5D0','#FFE070','#4A3208,#C89018,#FFD040,#FFF8C8,#FFFFFF',0)
ON DUPLICATE KEY UPDATE `ma`=VALUES(`ma`), `ten`=VALUES(`ten`), `mau`=VALUES(`mau`), `nhat`=VALUES(`nhat`), `sang`=VALUES(`sang`), `phu_kien`=VALUES(`phu_kien`), `vien_dong`=VALUES(`vien_dong`);

-- Item.colorName (data_item.colortype) -> bac; ma khong co dong = Thuong.
CREATE TABLE IF NOT EXISTS `data_do_hiem_vat_pham` (
  `color_name` TINYINT NOT NULL,
  `bac` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`color_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `data_do_hiem_vat_pham` (`color_name`,`bac`) VALUES
(0,0),(1,3),(2,2),(3,1),(4,3),(5,5),(6,4),(7,1)
ON DUPLICATE KEY UPDATE `bac`=VALUES(`bac`);

-- 11 bac trang bi theo cap (vong sang / vien Pokemon).
CREATE TABLE IF NOT EXISTS `data_bac_trang_bi` (
  `bac` TINYINT NOT NULL,
  `ten` VARCHAR(32) NOT NULL DEFAULT '',
  `mau` VARCHAR(7) NOT NULL DEFAULT '',
  PRIMARY KEY (`bac`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `data_bac_trang_bi` (`bac`,`ten`,`mau`) VALUES
(0,'Sơ cấp','#9AA0A8'),
(1,'Đồng','#C88048'),
(2,'Bạc','#D0D8E0'),
(3,'Vàng','#F8D030'),
(4,'Bạch kim','#E4F4FF'),
(5,'Kim cương','#78D8F8'),
(6,'Chuyên gia','#52C866'),
(7,'Tinh anh','#A45CE8'),
(8,'Huyền thoại','#F08A28'),
(9,'Thần thoại','#F84868'),
(10,'Tối thượng','#FFFFFF')
ON DUPLICATE KEY UPDATE `ten`=VALUES(`ten`), `mau`=VALUES(`mau`);

-- Mau danh hieu (them_danh_hieu.sql tao bang; cot mau chi cho web) = mau bac chung.
CREATE TABLE IF NOT EXISTS `data_danh_hieu_do_hiem` (
  `id` TINYINT NOT NULL,
  `ten` VARCHAR(32) NOT NULL DEFAULT '',
  `mau` VARCHAR(7) NOT NULL DEFAULT '',
  `thu_tu` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `data_danh_hieu_do_hiem` (`id`,`ten`,`mau`,`thu_tu`) VALUES
(0,'Thường','#5F6B7A',0),(1,'Hiếm','#23803A',1),(2,'Sử thi','#7B45D6',2),(3,'Huyền thoại','#B95A0E',3)
ON DUPLICATE KEY UPDATE `ten`=VALUES(`ten`), `mau`=VALUES(`mau`), `thu_tu`=VALUES(`thu_tu`);
