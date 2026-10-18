-- Bang dem so lan ha boss theo nhan vat (data.Database.updateCountBoss). Server tu tao neu thieu
-- (taoTopBossNeuThieu) - file nay de chay tay / gop vao htbb_day_du.sql. 18/10/2026.
CREATE TABLE IF NOT EXISTS `top_boss` (
  `charname` varchar(45) NOT NULL,
  `count` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`charname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
