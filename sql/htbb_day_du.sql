-- Huyen Thoai Bao Boi (HTBB) - CSDL DAY DU cho server moi (account + kpah_db).
-- SINH TU tools/pokemon/xuat_sql_day_du.sh - KHONG SUA TAY. Gom: gamedb7_clean.sql + vps/db_new_server.sql
-- + moi migration trong sql/ + moi file sql/pokemon_*.sql (thu tu xem script). Nap: mysql < sql/htbb_day_du.sql

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `account` /*!40100 DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci */;

USE `account`;
DROP TABLE IF EXISTS `team_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `team_user` (
  `id` int(4) NOT NULL AUTO_INCREMENT,
  `username` varchar(20) NOT NULL,
  `password` varchar(41) DEFAULT NULL,
  `phone` varchar(20) NOT NULL DEFAULT '',
  `regdate` datetime DEFAULT NULL,
  `ban` tinyint(1) NOT NULL DEFAULT 0,
  `provider` int(1) NOT NULL DEFAULT 0,
  `fromgame` tinyint(4) NOT NULL DEFAULT 0,
  `email` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `username` (`username`) USING BTREE,
  KEY `Index_3` (`email`) USING BTREE,
  KEY `Index_4` (`regdate`) USING BTREE,
  KEY `Index_5` (`phone`) USING BTREE
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `team_user` WRITE;
/*!40000 ALTER TABLE `team_user` DISABLE KEYS */;
INSERT INTO `team_user` VALUES (1,'admin','*FC12668B9D0D2FAA2E4966BA1D80E023D10AAAC0','','2026-10-17 10:25:12',0,0,0,NULL);
/*!40000 ALTER TABLE `team_user` ENABLE KEYS */;
UNLOCK TABLES;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `kpah_db` /*!40100 DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci */;

USE `kpah_db`;
DROP TABLE IF EXISTS `5h_active`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `5h_active` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `userID` int(11) NOT NULL,
  `username` varchar(255) NOT NULL,
  `time_end` varchar(50) DEFAULT '-1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `5h_active` WRITE;
/*!40000 ALTER TABLE `5h_active` DISABLE KEYS */;
INSERT INTO `5h_active` VALUES (1,1,'admin','1794799512');
/*!40000 ALTER TABLE `5h_active` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `5h_notify`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `5h_notify` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `owner` varchar(255) NOT NULL,
  `content` text NOT NULL,
  `time_end` int(11) NOT NULL DEFAULT 1702874482,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `5h_notify` WRITE;
/*!40000 ALTER TABLE `5h_notify` DISABLE KEYS */;
/*!40000 ALTER TABLE `5h_notify` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `5h_systems`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `5h_systems` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `command` varchar(255) DEFAULT NULL,
  `status` int(255) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `5h_systems` WRITE;
/*!40000 ALTER TABLE `5h_systems` DISABLE KEYS */;
INSERT INTO `5h_systems` VALUES (1,'restart',1),(2,'tangexp',1),(5,'tangnap',2),(6,'maxccu',5000),(7,'percentDrop',70),(8,'tileDrop',1);
/*!40000 ALTER TABLE `5h_systems` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `admin_msg`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `admin_msg` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `msg` varchar(1000) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `admin_msg` WRITE;
/*!40000 ALTER TABLE `admin_msg` DISABLE KEYS */;
INSERT INTO `admin_msg` VALUES (5,'setinfo hệ thống gặp lỗi không thể khởi động lại, vui lòng thoát game trước 10h sáng.');
/*!40000 ALTER TABLE `admin_msg` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `bad_word`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bad_word` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `word` varchar(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=170 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `bad_word` WRITE;
/*!40000 ALTER TABLE `bad_word` DISABLE KEYS */;
INSERT INTO `bad_word` VALUES (48,'*sc'),(47,'.sc'),(46,'vvap'),(45,'wáp'),(5,'dm'),(6,'dkm'),(62,'háck'),(61,'http'),(10,'iwin'),(65,'*mw'),(12,'fuck'),(13,'lon`'),(14,'l0n`'),(15,'lam tinh'),(28,'dis'),(63,'.mw'),(19,'www'),(20,'wap'),(21,'.com'),(22,'.vn'),(23,'sex'),(56,',lt'),(55,',yn'),(54,',sh'),(53,'.lt'),(29,'hack'),(50,'*in'),(31,'dcm'),(51,'wáp'),(33,'tinhduc'),(52,'.yn'),(35,'hak'),(44,'*sh'),(37,'xtgem'),(38,'.sh'),(49,'.in'),(40,'.tk'),(41,'.net'),(42,'cặc'),(43,'lồn'),(57,',in'),(58,',sc'),(59,'*yn'),(60,'nhan qua'),(66,'du ma'),(67,'du ma may'),(69,'lon`'),(70,'l0n`'),(71,'lam tinh'),(72,'test'),(73,'dis'),(74,'.mw'),(75,'www'),(76,'wap'),(77,'.com'),(78,'.vn'),(79,'sex'),(80,'dit'),(81,',yn'),(82,'.it'),(83,'.sh'),(84,',sh'),(85,'hack'),(86,'dcm'),(87,'wáp'),(88,'tinhduc'),(89,'tinh duc'),(90,'.yn'),(91,'hak'),(92,'*sh'),(93,'xtgem'),(94,'.in'),(95,'.tk'),(96,'.net'),(97,'cặc'),(98,'lồn'),(99,'giftcode'),(100,',in'),(101,',sc'),(102,'giftc0de'),(103,'daily'),(104,'dai ly'),(105,'đại lý'),(106,'đại lí'),(107,'dai li'),(108,'banxu'),(109,'ban xu'),(110,'bán xu'),(111,'giá rẻ'),(112,'giare'),(113,'gia re'),(114,'gja re'),(115,'team0bi'),(116,'teamobj'),(117,'team0bj'),(118,'admjn'),(119,'admin'),(120,'teamobi'),(121,'mua bán'),(122,'muaban'),(123,'mua ban'),(124,'nhận thưởng'),(125,'nhan thuong'),(126,'nhan thuong'),(127,'nhan thu0ng'),(128,'nhậnthưởng'),(129,'nhận quà'),(130,'nhanqua'),(131,'.mobi'),(132,'.m0bi'),(133,'.mobj'),(134,'.m0bj'),(135,'tặng quà'),(136,'tang qua'),(137,'tangqua'),(138,'mod'),(139,'m0d'),(140,'xuandiem'),(141,'xuandjem'),(142,'xuan diem'),(143,'xuân điềm'),(144,'ngao binh'),(145,'ngaobinh'),(146,'ngaobjnh'),(147,'ngao bjnh'),(148,'hangkt'),(149,'topxu'),(150,'top xu'),(151,'liên hệ'),(152,'ngu'),(153,'phong'),(154,'puaru'),(155,'ad'),(156,'adm'),(157,'a.d'),(158,'pua'),(159,'kpah'),(160,'gino'),(161,'suc'),(162,'vat'),(163,'sv'),(164,'vv'),(165,'lau'),(166,'game'),(167,'tay'),(168,'chay'),(169,'chai');
/*!40000 ALTER TABLE `bad_word` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `board_created`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `board_created` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `xu` bigint(20) unsigned NOT NULL DEFAULT 0,
  `luong` bigint(20) unsigned NOT NULL DEFAULT 0,
  `luongK` bigint(20) unsigned NOT NULL DEFAULT 0,
  `svID` int(11) NOT NULL DEFAULT -1,
  `username` varchar(45) NOT NULL,
  `ve` int(10) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `Index_2` (`username`) USING BTREE,
  KEY `Index_3` (`svID`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `board_created` WRITE;
/*!40000 ALTER TABLE `board_created` DISABLE KEYS */;
/*!40000 ALTER TABLE `board_created` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `board_naptien`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `board_naptien` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `xu` bigint(20) unsigned NOT NULL DEFAULT 0,
  `luong` bigint(20) unsigned NOT NULL DEFAULT 0,
  `svID` int(11) NOT NULL DEFAULT -1,
  `username` varchar(45) NOT NULL,
  `ve` int(10) unsigned NOT NULL DEFAULT 0,
  `luongKhoa` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `Index_2` (`username`) USING BTREE,
  KEY `Index_3` (`svID`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `board_naptien` WRITE;
/*!40000 ALTER TABLE `board_naptien` DISABLE KEYS */;
/*!40000 ALTER TABLE `board_naptien` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `cau_hinh`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cau_hinh` (
  `khoa` varchar(64) NOT NULL COMMENT 'ten setting, khop hang so K_* trong src/server/CauHinh.java',
  `gia_tri` varchar(255) NOT NULL DEFAULT '',
  `sua_boi` varchar(50) DEFAULT NULL COMMENT 'ten dang nhap admin (web) hoac "game" (bang setting trong game)',
  `sua_luc` datetime DEFAULT NULL,
  PRIMARY KEY (`khoa`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `cau_hinh` WRITE;
/*!40000 ALTER TABLE `cau_hinh` DISABLE KEYS */;
/*!40000 ALTER TABLE `cau_hinh` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `data_attribute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `data_attribute` (
  `id` int(10) unsigned NOT NULL,
  `name` varchar(45) NOT NULL,
  `idcolor` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `ispercent` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `decript` varchar(100) NOT NULL,
  `namein` varchar(45) DEFAULT '""',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `data_attribute` WRITE;
/*!40000 ALTER TABLE `data_attribute` DISABLE KEYS */;
INSERT INTO `data_attribute` VALUES (0,'Tấn công',0,0,'','Serangan'),(1,'Phòng thủ',0,0,'','Hewan'),(2,'Né tránh',0,2,'','Menghindar'),(3,'Chính xác',0,2,'','Benar sekali'),(4,'Chí mạng',0,2,'','Kritis'),(5,'Sức khỏe',0,0,'','Kesehatan'),(6,'Phòng thủ đặc biệt',0,0,'','Hantu Utama'),(7,'chỉ số 7',0,0,'','Indikator 7'),(8,'chỉ số 8',0,0,'','Indikator 8'),(9,'chỉ số 9',0,0,'','Indeks 9'),(10,'Tăng sức mạnh',3,0,'','Tambah kekuatan'),(11,'Tăng nhanh nhẹn',3,0,'','Tambah kelincahan'),(12,'Tăng tinh thần',3,0,'','Tambah semangat'),(13,'Tăng sức khoẻ',3,0,'','Tambah kesehatan'),(14,'Gây mù',3,1,'','Kebutaan'),(15,'Đóng băng',3,1,'','Lapisan Es'),(16,'Trúng độc',3,1,'','Meracun'),(17,'Gây choáng',3,1,'','Membius'),(18,'Hoá thạch',3,1,'','Fosil'),(19,'Giảm tốc',3,1,'','Melambat'),(20,'Kháng giảm tốc',3,1,'','Anti pelambat'),(21,'Kháng trúng độc',3,1,'','Anti racun'),(22,'Kháng gây mù',3,1,'','Anti kebutaan'),(23,'Kháng đóng băng',3,1,'','Anti beku'),(24,'Kháng gây choáng',3,1,'','Anti setrum'),(25,'Kháng hoá thạch',3,1,'','Anti fosil'),(26,'Tăng x2 mỗi lần đánh',3,1,'','Meningkatkan hit 2x lipat'),(27,'Tăng tỷ lệ rớt bảo vật',3,1,'','Tambah penurunan kadar harta'),(28,'Giảm st vật',1,1,'','Bahan kurangi kerusakan'),(29,'Giảm st đặc biệt',1,1,'','Kurangi kerusakan hantu'),(30,'Tăng tấn công',1,1,'','Meningkatkan serangan'),(31,'Xuyên giáp',1,1,'','Baja'),(32,'Phản st',1,1,'','Respon Kerusakan'),(33,'Tăng HP',0,0,'','Tingkatkan HP'),(34,'Tăng MP',0,0,'','Tingkatkan MP'),(35,'Cộng sức mạnh',0,0,'','Tingkatkan kekuatan'),(36,'Cộng khéo léo',0,0,'','Tingkatkan kelincahan'),(37,'Cộng tinh thần',0,0,'','Tingkatkan moral'),(38,'Cộng sức khoẻ',0,0,'','Tingkatkan kesehatan'),(39,'Cộng kỹ năng',0,0,'','Tingkatkan skil'),(40,'Tăng chí mạng',0,0,'','Tingkatkan fatal'),(41,'Tăng st chí mạng',0,0,'','Tingkatkan kritis'),(42,'chưa dùng',0,0,'','berisi'),(43,'kỹ năng 1 cộng thêm',0,0,'','1 Jurus tambahan'),(44,'kỹ năng 2 cộng thêm',0,0,'','2 Jurus tambahan'),(45,'kỹ năng 3 cộng thêm',0,0,'','3 Jurus tambahan'),(46,'kỹ năng 4 cộng thêm',0,0,'','4 Jurus tambahan'),(47,'kỹ năng 5 cộng thêm',0,0,'','5 Jurus tambahan'),(48,'kỹ năng 6 cộng thêm',0,0,'','Jurus 6 ditambah'),(49,'kỹ năng 7 cộng thêm',0,0,'','Jurus 7 ditambah'),(50,'kỹ năng 8 cộng thêm',0,0,'','Jurus 8 ditambah'),(51,'kỹ năng 9 cộng thêm',0,0,'','9 Jurus tambahan'),(52,'kỹ năng 10 cộng thêm',0,0,'','Jurus 10 ditambah'),(53,'kỹ năng 11 cộng thêm',0,0,'','Jurus 11 ditambah'),(54,'kỹ năng 12 cộng thêm',0,0,'','Jurus 12 ditambah'),(55,'kỹ năng 13 cộng thêm',0,0,'','Jurus 13 ditambah'),(56,'kỹ năng 14 cộng thêm',0,0,'','Jurus 14 ditambah'),(57,'kỹ năng 15 cộng thêm',0,0,'','Jurus 15 ditambah'),(58,'Tăng công',0,1,'','Tambah'),(59,'Tăng phòng thủ đặc biệt',0,1,'','Tambah pelindung ajaib'),(60,'Tăng phòng thủ',0,1,'','Tambah pelindung'),(61,'Bỏ qua tc đặc biệt',1,1,'','Abaikan Serangan Fantom'),(62,'Bỏ qua tc vật',1,1,'','Lewati Serangan Hewan'),(63,'Tăng phòng thủ đặc biệt trang bị',0,1,'\"\"','Tambah alat perlindungan'),(64,'Tăng phòng thủ trang bị',0,1,' ','Tambah alat perlindungan'),(65,'Bạo kích',0,2,' ','Klik pertama'),(66,'Sét lan',2,2,'set danh 5 muc tieu xung quanh',''),(67,'Độc lan',2,2,'ban doc cho 5 muc tieu xung quanh',''),(68,'Băng lan',2,2,'dong bang 5 muc tieu xung quanh',''),(69,'Tăng sét lan',0,2,'tang % xuat hien set lan',''),(70,'Tăng độc lan',0,2,'tang % xuat hien doc lan',''),(71,'Tăng băng lan',0,2,'tang % xuat hien bang lan',''),(72,'Tăng st băng lan',0,1,'tang sat thuong bang lan',''),(73,'Tăng st sét lan',0,1,'tang sat thuong set lan',''),(74,'Tăng st độc lan',0,1,'tang sat thuong doc lan',''),(75,'Hoá người tuyết',0,2,'hoa ng choi thanh nguoi tuyet trong 8s',''),(76,'Kháng bạo kích',0,0,'khang bao kick',''),(77,'Hút máu',0,0,'hut mau doi phuong sau moi nhat danh, min 20, max 55',''),(78,'Hồi HP',0,0,'hoi hp moi 5s max 10 giot',''),(79,'Hồi MP',0,0,'hoi mp moi 5s max 10 giot',''),(80,'Tàn phế',0,2,'giam 50% toc do max 3%, tong vuot 10 se =10, trong 10s',''),(81,'Hấp thu sát thương',0,0,'hap thu mot phan sat thuong',''),(82,'Câm lặng',0,2,'không thể chơi skill max 3% trong 5s',''),(83,'Tăng tỷ lệ rớt vật phẩm',0,2,'tang ty le rot item, max 10%, ko tinh trang bi tan cong',''),(84,'Tăng tỷ lệ rớt xu',0,2,'tang ty le rot xu, max 10%, ko tinh trang bi tan cong',''),(85,'Kháng băng lan',0,2,'khang bang lan, max 4%',''),(86,'Kháng sét lan',0,2,'khang set lan, max 4%, giap',''),(87,'Kháng độc lan',0,2,'khang doc lan, max 4%, giap',''),(88,'Tăng thủ trang bị',0,2,'tăng phòng thủ trang tinh tren trang bi nguoi max 3%',''),(89,'Lửa lan',2,2,'làm tàn phế + câm lặng 5 muc tieu xung quanh',''),(90,'Tăng lửa lan',0,2,'tang % xuat hien lua lan',''),(91,'Tăng st lửa lan',0,1,'tang sat thuong set lan',''),(92,'Kháng lửa lan',0,2,'khang bang lan, max 4%',''),(93,'St hắc ám',0,0,'dam hắc ám',''),(94,'St ánh sáng',0,0,'dam ánh sáng',''),(95,'Xh hắc ám',0,2,'ty le xuat hien',''),(96,'Xh ánh sáng',0,2,'ty le xuat hien',''),(97,'Gây hoảng sợ:',0,0,'gây hoang so cho doi phuong trong 5s',''),(98,'Xh hoảng sợ',0,1,'ty le xuat hien hoang so',''),(99,'St theo chủ',0,1,'st theo chu nhan',''),(100,'Sức khoẻ',0,0,'suc khoe',''),(101,'St max Hp',0,1,'St max Hp','\"\"'),(102,'Ru ngủ',0,2,'ru ngu','\"\"'),(103,'Choáng',0,2,'choang','\"\"'),(104,'Bỏng',0,2,'bong','\"\"'),(105,'Trúng độc',0,2,'trung doc','\"\"'),(106,'Suy yếu',0,2,'suy yeu','\"\"'),(107,'Chuyển st qua mp',0,0,'chuyen st qua mp','\"\"'),(108,'Gia tăng tc',0,0,'tang tan cong','\"\"'),(109,'Hút hp',0,0,'hut mau','\"\"'),(110,'Sát thương độc',0,0,'Sát thương độc,   otherAtt','\"\"'),(111,'Tăng exp',0,1,'Tăng exp, otheratt','\"\"'),(112,'Tác động suy yếu',0,1,'Tác động suy yếu','\"\"'),(113,'Tl hút hp',0,2,'Tl hút hp','\"\"'),(114,'Nhân thêm st khi bỏng',0,1,'Nhân thêm st khi bỏng','\"\"'),(115,'Sát thương chuẩn',1,2,'Sát thương chuẩn','\"\"'),(116,'Chỉ số vũ khí tăng',1,2,'Chỉ số vũ khí tăng','\"\"'),(117,'Tỷ lệ gây sát thương chuẩn',1,2,'Tỷ lệ gây sát thương chuẩn','\"\"'),(118,'Giảm sát thương cuối',2,2,'Giảm sát thương cuối','\"\"'),(119,'Ánh Trăng gây st(10%-15%) tl xuất hiện',2,2,'Nguyệt ảnh gây st(10%-15%) tỷ lệ xuất hiện','\"\"'),(120,'Kháng Lửa',1,2,'Kháng hoả',''),(121,'Kháng Nước',1,2,'Kháng thuỷ',''),(122,'Kháng Cỏ',1,2,'Kháng mộc',''),(123,'Kháng Thép',1,2,'Kháng kim',''),(124,'Kháng Đất',1,2,'Kháng thổ',''),(125,'Giá lượng',2,0,'Giá lượng',''),(126,'Số lượng',2,0,'Số lượng',''),(127,'Kháng st lan',2,0,'','\"\"'),(128,'Hồi hp theo maxhp',2,0,'','\"\"'),(129,'Chuyển sát thương thành hp',0,1,'','\"\"'),(130,'Giảm thủ đối phương',0,0,'','\"\"'),(131,'Né tránh',2,0,'','\"\"'),(132,'Hoá giải kỹ năng pet',0,1,'','\"\"'),(133,'Tăng hiệu quả bình hp mp khi hp dưới 30%',2,0,'','\"\"'),(134,'Sát thương theo hp',1,1,'','\"\"'),(135,'Điều kiện chủ nhận đủ 500k st từ hvl',1,0,'','\"\"'),(136,'Hấp thu sát thương',1,1,'','\"\"'),(137,'Tăng tốc',1,1,'','\"\"'),(138,'Tiềm năng',0,1,'','\"\"'),(139,'HP',0,0,'','\"\"'),(140,'Trí Mạng',0,1,'','\"\"'),(141,'Tăng sát thương chí mạng',0,2,'',''),(142,'Tăng sát thương theo % HP kẻ địch',0,2,'',''),(143,'Tăng sát thương theo % MP kẻ địch',0,2,'',''),(144,'Tăng % sát thương vật lý',0,1,'',''),(145,'Tăng % sát thương đặc biệt',0,1,'',''),(146,'Tăng % Xuyên phòng thủ đặc biệt',0,2,'',''),(147,'Tăng % Xuyên phòng thủ',0,2,'',''),(148,'Tăng % sát thương lên boss/trụ',0,2,'',''),(149,'Tăng % sát thương lên quái',0,2,'',''),(150,'Giảm % sát thương nhận vào từ Boss',0,2,'',''),(151,'Tăng điểm phòng thủ cơ bản',0,0,'',''),(152,'Kháng Thép',0,1,'',''),(153,'Kháng Cỏ',0,1,'',''),(154,'Kháng Nước',0,1,'',''),(155,'Kháng Lửa',0,1,'',''),(156,'Kháng Đất',0,1,'',''),(157,'Tăng điểm giáp khi đang ở trong tổ đội',0,0,'',''),(158,'Tăng điểm sát thương vật lý',0,0,'',''),(159,'Tăng điểm sát thương đặc biệt',0,0,'',''),(160,'Tăng tốc độ đánh',0,1,'',''),(161,'Tăng tốc độ di chuyển',0,0,'',''),(162,'Tăng % câm lặng của bản thân',0,1,'',''),(163,'Sát thương hệ Thép',0,1,'',''),(164,'Sát thương hệ Cỏ',0,1,'',''),(165,'Sát thương hệ Nước',0,1,'',''),(166,'Sát thương hệ Lửa',0,1,'',''),(167,'Sát thương hệ Đất',0,1,'',''),(168,'Số dòng ẩn',0,0,'',''),(169,'Vải',0,0,'',''),(170,'Sắt',0,0,'',''),(171,'Ngọc',0,0,'',''),(172,'Gỗ thường',0,0,'',''),(173,'Da mềm',0,0,'',''),(174,'Tơ lụa',0,0,'',''),(175,'Bạc',0,0,'',''),(176,'Thuỷ tinh',0,0,'',''),(177,'Gỗ sưa',0,0,'',''),(178,'Da cứng',0,0,'',''),(179,'Đá cầu vồng',0,0,'',''),(180,'Giảm sát thương nhận vào từ người chơi',0,2,'',''),(181,'Hút MP',0,1,'',''),(182,'Hút HP',0,1,'',''),(183,'Tăng % né tránh',0,1,'',''),(184,'Giảm % sức mạnh và % phòng thủ của kẻ địch',0,2,'',''),(185,'Tăng % phản đòn',0,1,'',''),(186,'Kháng tất cả',0,2,'',''),(200,'Sinh lực',0,0,'skin thu cuoi: Phuc Thu +HP',''),(201,'Năng lượng',0,0,'skin thu cuoi: Phuc Thu +MP',''),(202,'Giảm sát thương khi bị đánh',0,1,'skin thu cuoi: Phuc Thu buff bi dong',''),(203,'Chính xác cộng thêm',0,0,'skin thu cuoi: Gau Truc +chinh xac',''),(204,'Phản sát thương',0,1,'skin thu cuoi: Gau Truc buff bi dong',''),(205,'Tiềm năng cộng thêm',0,1,'skin thu cuoi: Tien Loc +% tiem nang',''),(206,'Lá chắn chặn sát thương',0,1,'skin thu cuoi: Tien Loc buff bi dong',''),(207,'Xuyên giáp cộng thêm',0,1,'skin thu cuoi: Xich Duong +% xuyen giap',''),(208,'Nộ khí tăng tấn công',0,1,'skin thu cuoi: Xich Duong buff HP thap',''),(209,'Phòng thủ cộng thêm',0,1,'skin thu cuoi: Chien Lang +% thu',''),(210,'Hấp thụ sát thương',0,1,'skin thu cuoi: Chien Lang hap thu','');
/*!40000 ALTER TABLE `data_attribute` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `data_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `data_item` (
  `id` int(4) unsigned NOT NULL,
  `name` text NOT NULL,
  `type` int(1) unsigned NOT NULL,
  `style` int(1) unsigned NOT NULL DEFAULT 0,
  `he` int(1) NOT NULL DEFAULT 0,
  `gender` int(1) unsigned NOT NULL DEFAULT 0,
  `level` int(1) unsigned NOT NULL DEFAULT 0,
  `durable` int(2) unsigned NOT NULL DEFAULT 0,
  `atb0` int(2) NOT NULL DEFAULT 0,
  `atb1` int(2) NOT NULL DEFAULT 0,
  `atb2` int(2) NOT NULL DEFAULT 0,
  `atb3` int(2) NOT NULL DEFAULT 0,
  `atb4` int(2) NOT NULL DEFAULT 0,
  `atb5` int(2) NOT NULL DEFAULT 0,
  `atb6` int(2) NOT NULL DEFAULT 0,
  `atb7` int(2) NOT NULL DEFAULT 0,
  `atb8` int(2) NOT NULL DEFAULT 0,
  `atb9` int(2) NOT NULL DEFAULT 0,
  `price` int(4) NOT NULL DEFAULT 0,
  `clazz` int(1) NOT NULL DEFAULT -1,
  `xstart` int(10) unsigned NOT NULL DEFAULT 0,
  `ystart` int(10) unsigned NOT NULL DEFAULT 0,
  `colortype` int(10) NOT NULL DEFAULT -1,
  `nloan` int(10) unsigned NOT NULL DEFAULT 0,
  `idUpLevel` int(11) NOT NULL DEFAULT -1,
  `namein` varchar(45) DEFAULT NULL,
  `ideff` int(11) DEFAULT -1,
  `pricecu` int(10) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `data_item` WRITE;
/*!40000 ALTER TABLE `data_item` DISABLE KEYS */;
INSERT INTO `data_item` VALUES (1,'Khăn quàng sơ cấp',0,1,0,0,1,200,0,10,1,1,0,0,0,0,0,0,500,-1,14000,1,0,0,-1,NULL,-1,0),(2,'Khăn quàng sơ cấp',0,0,0,0,1,200,0,10,1,1,0,0,0,0,0,0,500,-1,14000,2,0,0,-1,NULL,-1,0),(3,'Khăn quàng sơ cấp',0,3,0,0,4,220,0,15,1,2,0,0,0,0,0,0,1000,-1,14000,3,0,0,-1,NULL,-1,0),(4,'Khăn quàng sơ cấp',0,2,0,0,4,220,0,15,1,2,0,0,0,0,0,0,1000,-1,14000,4,0,0,-1,NULL,-1,0),(5,'Khăn quàng sơ cấp',0,3,0,0,9,250,0,20,2,2,0,0,0,0,0,0,2000,-1,14000,5,0,0,-1,NULL,-1,0),(6,'Khăn quàng sơ cấp',0,2,0,0,9,250,0,20,2,2,0,0,0,0,0,0,2000,-1,14000,6,0,0,-1,NULL,-1,0),(7,'Khăn quàng đồng',0,5,0,0,14,280,0,25,2,3,0,0,0,0,0,0,5000,-1,14000,7,0,0,-1,NULL,-1,0),(8,'Khăn quàng đồng',0,4,0,0,14,280,0,25,2,3,0,0,0,0,0,0,5000,-1,14000,8,0,0,-1,NULL,-1,0),(9,'Khăn quàng đồng',0,5,0,0,19,300,0,30,3,3,0,0,0,0,0,0,10000,-1,14000,9,0,0,-1,NULL,-1,0),(10,'Khăn quàng đồng',0,4,0,0,19,300,0,30,3,3,0,0,0,0,0,0,10000,-1,14000,10,0,0,-1,NULL,-1,0),(11,'Khăn quàng bạc',0,7,0,0,24,320,0,35,0,3,0,0,0,0,0,0,20000,-1,14000,11,0,0,-1,NULL,-1,0),(12,'Khăn quàng bạc',0,6,0,0,24,320,0,35,0,3,0,0,0,0,0,0,20000,-1,14000,12,0,0,-1,NULL,-1,0),(13,'Khăn quàng bạc',0,7,0,0,29,350,0,40,1,4,0,0,0,0,0,0,25000,-1,14000,13,0,0,-1,NULL,-1,0),(14,'Khăn quàng bạc',0,6,0,0,29,350,0,40,1,4,0,0,0,0,0,0,25000,-1,14000,14,0,0,-1,NULL,-1,0),(15,'Khăn quàng vàng',0,9,0,0,34,390,0,45,1,4,0,0,0,0,0,0,34000,-1,14000,15,0,0,-1,NULL,-1,0),(16,'Khăn quàng vàng',0,8,0,0,34,390,0,45,1,4,0,0,0,0,0,0,34000,-1,14000,16,0,0,-1,NULL,-1,0),(17,'Khăn quàng vàng',0,9,0,0,39,420,0,50,0,4,0,0,0,0,0,0,40000,-1,14000,17,0,0,-1,NULL,-1,0),(18,'Khăn quàng vàng',0,8,0,0,39,420,0,50,0,4,0,0,0,0,0,0,40000,-1,14000,18,0,0,-1,NULL,-1,0),(19,'Khăn quàng bạch kim',0,11,0,0,44,440,0,55,0,4,0,0,0,0,0,0,44000,-1,14000,19,0,0,-1,NULL,-1,0),(20,'Khăn quàng bạch kim',0,10,0,0,44,440,0,55,0,4,0,0,0,0,0,0,44000,-1,14000,20,0,0,-1,NULL,-1,0),(21,'Khăn quàng bạch kim',0,11,0,0,49,460,0,60,0,5,0,0,0,0,0,0,49000,-1,14000,21,0,0,-1,NULL,-1,0),(22,'Khăn quàng bạch kim',0,10,0,0,49,460,0,60,0,5,0,0,0,0,0,0,49000,-1,14000,22,0,0,-1,NULL,-1,0),(23,'Khăn quàng kim cương',0,13,0,0,54,470,0,65,0,5,0,0,0,0,0,0,54000,-1,14000,23,0,0,-1,NULL,-1,0),(24,'Khăn quàng kim cương',0,12,0,0,54,470,0,65,0,5,0,0,0,0,0,0,54000,-1,14000,24,0,0,-1,NULL,-1,0),(25,'Khăn quàng kim cương',0,13,0,0,59,480,0,70,0,5,0,0,0,0,0,0,59000,-1,14000,25,0,0,-1,NULL,-1,0),(26,'Khăn quàng kim cương',0,12,0,0,59,480,0,70,0,5,0,0,0,0,0,0,59000,-1,14000,26,0,0,-1,NULL,-1,0),(27,'Đai sơ cấp',1,1,0,0,1,200,0,7,0,0,0,0,0,0,0,0,500,-1,14001,27,0,0,-1,NULL,-1,0),(28,'Đai sơ cấp',1,0,0,0,1,200,0,7,0,0,0,0,0,0,0,0,500,-1,14001,28,0,0,-1,NULL,-1,0),(29,'Đai sơ cấp',1,3,0,0,4,250,0,12,0,1,0,0,0,0,0,0,800,-1,14001,29,0,0,-1,NULL,-1,0),(30,'Đai sơ cấp',1,2,0,0,4,250,0,12,0,1,0,0,0,0,0,0,800,-1,14001,30,0,0,-1,NULL,-1,0),(31,'Đai sơ cấp',1,3,0,0,9,270,0,17,0,1,0,0,0,0,0,0,1000,-1,14001,31,0,0,-1,NULL,-1,0),(32,'Đai sơ cấp',1,2,0,0,9,270,0,17,0,1,0,0,0,0,0,0,1000,-1,14001,32,0,0,-1,NULL,-1,0),(33,'Đai đồng',1,5,0,0,14,290,0,22,0,2,0,0,0,0,0,0,3000,-1,14001,33,0,0,-1,NULL,-1,0),(34,'Đai đồng',1,4,0,0,14,290,0,22,0,2,0,0,0,0,0,0,3000,-1,14001,34,0,0,-1,NULL,-1,0),(35,'Đai đồng',1,5,0,0,19,310,0,27,0,2,0,0,0,0,0,0,8000,-1,14001,35,0,0,-1,NULL,-1,0),(36,'Đai đồng',1,4,0,0,19,310,0,27,0,2,0,0,0,0,0,0,8000,-1,14001,36,0,0,-1,NULL,-1,0),(37,'Đai bạc',1,7,0,0,24,330,0,32,0,2,0,0,0,0,0,0,14000,-1,14001,37,0,0,-1,NULL,-1,0),(38,'Đai bạc',1,6,0,0,24,330,0,32,0,2,0,0,0,0,0,0,14000,-1,14001,38,0,0,-1,NULL,-1,0),(39,'Đai bạc',1,7,0,0,29,360,0,37,0,3,0,0,0,0,0,0,19000,-1,14001,39,0,0,-1,NULL,-1,0),(40,'Đai bạc',1,6,0,0,29,360,0,37,0,3,0,0,0,0,0,0,19000,-1,14001,40,0,0,-1,NULL,-1,0),(41,'Đai vàng',1,9,0,0,34,400,0,42,0,3,0,0,0,0,0,0,24000,-1,14001,41,0,0,-1,NULL,-1,0),(42,'Đai vàng',1,8,0,0,34,400,0,42,0,3,0,0,0,0,0,0,24000,-1,14001,42,0,0,-1,NULL,-1,0),(43,'Đai vàng',1,9,0,0,39,420,0,47,0,3,0,0,0,0,0,0,29000,-1,14001,43,0,0,-1,NULL,-1,0),(44,'Đai vàng',1,8,0,0,39,420,0,47,0,3,0,0,0,0,0,0,29000,-1,14001,44,0,0,-1,NULL,-1,0),(45,'Đai bạch kim',1,11,0,0,44,440,0,52,0,3,0,0,0,0,0,0,34000,-1,14001,45,0,0,-1,NULL,-1,0),(46,'Đai bạch kim',1,10,0,0,44,440,0,52,0,3,0,0,0,0,0,0,34000,-1,14001,46,0,0,-1,NULL,-1,0),(47,'Đai bạch kim',1,11,0,0,49,450,0,57,0,4,0,0,0,0,0,0,40000,-1,14001,47,0,0,-1,NULL,-1,0),(48,'Đai bạch kim',1,10,0,0,49,450,0,57,0,4,0,0,0,0,0,0,40000,-1,14001,48,0,0,-1,NULL,-1,0),(49,'Đai kim cương',1,13,0,0,54,460,0,62,0,4,0,0,0,0,0,0,45000,-1,14001,49,0,0,-1,NULL,-1,0),(50,'Đai kim cương',1,12,0,0,54,460,0,62,0,4,0,0,0,0,0,0,45000,-1,14001,50,0,0,-1,NULL,-1,0),(51,'Đai kim cương',1,13,0,0,59,480,0,67,0,4,0,0,0,0,0,0,50000,-1,14001,51,0,0,-1,NULL,-1,0),(52,'Đai kim cương',1,12,0,0,59,480,0,67,0,4,0,0,0,0,0,0,50000,-1,14001,52,0,0,-1,NULL,-1,0),(53,'Băng đô sơ cấp',2,1,0,0,1,200,0,5,0,1,0,0,0,0,0,0,500,-1,14002,53,0,0,-1,NULL,-1,0),(54,'Băng đô sơ cấp',2,0,0,0,1,200,0,5,0,1,0,0,0,0,0,0,500,-1,14002,54,0,0,-1,NULL,-1,0),(55,'Băng đô sơ cấp',2,3,0,0,4,250,0,10,0,2,0,0,0,0,0,0,800,-1,14002,55,0,0,-1,NULL,-1,0),(56,'Băng đô sơ cấp',2,2,0,0,4,250,0,10,0,2,0,0,0,0,0,0,800,-1,14002,56,0,0,-1,NULL,-1,0),(57,'Băng đô sơ cấp',2,3,0,0,9,280,0,15,0,2,0,0,0,0,0,0,1000,-1,14002,57,0,0,-1,NULL,-1,0),(58,'Băng đô sơ cấp',2,2,0,0,9,280,0,15,0,2,0,0,0,0,0,0,1000,-1,14002,58,0,0,-1,NULL,-1,0),(59,'Băng đô đồng',2,5,0,0,14,300,0,20,0,2,0,0,0,0,0,0,2000,-1,14002,59,0,0,-1,NULL,-1,0),(60,'Băng đô đồng',2,4,0,0,14,300,0,20,0,2,0,0,0,0,0,0,2000,-1,14002,60,0,0,-1,NULL,-1,0),(61,'Băng đô đồng',2,5,0,0,19,320,0,25,0,2,0,0,0,0,0,0,6000,-1,14002,61,0,0,-1,NULL,-1,0),(62,'Băng đô đồng',2,4,0,0,19,320,0,25,0,2,0,0,0,0,0,0,6000,-1,14002,62,0,0,-1,NULL,-1,0),(63,'Băng đô bạc',2,7,0,0,24,350,0,30,0,3,0,0,0,0,0,0,12000,-1,14002,63,0,0,-1,NULL,-1,0),(64,'Băng đô bạc',2,6,0,0,24,350,0,30,0,3,0,0,0,0,0,0,12000,-1,14002,64,0,0,-1,NULL,-1,0),(65,'Băng đô bạc',2,7,0,0,29,370,0,35,0,3,0,0,0,0,0,0,18000,-1,14002,65,0,0,-1,NULL,-1,0),(66,'Băng đô bạc',2,6,0,0,29,370,0,35,0,3,0,0,0,0,0,0,18000,-1,14002,66,0,0,-1,NULL,-1,0),(67,'Băng đô vàng',2,9,0,0,34,390,0,40,0,3,0,0,0,0,0,0,22000,-1,14002,67,0,0,-1,NULL,-1,0),(68,'Băng đô vàng',2,8,0,0,34,390,0,40,0,3,0,0,0,0,0,0,22000,-1,14002,68,0,0,-1,NULL,-1,0),(69,'Băng đô vàng',2,9,0,0,39,410,0,45,0,3,0,0,0,0,0,0,26000,-1,14002,69,0,0,-1,NULL,-1,0),(70,'Băng đô vàng',2,8,0,0,39,410,0,45,0,3,0,0,0,0,0,0,26000,-1,14002,70,0,0,-1,NULL,-1,0),(71,'Băng đô bạch kim',2,11,0,0,44,430,0,50,0,3,0,0,0,0,0,0,34000,-1,14002,71,0,0,-1,NULL,-1,0),(72,'Băng đô bạch kim',2,10,0,0,44,430,0,50,0,3,0,0,0,0,0,0,34000,-1,14002,72,0,0,-1,NULL,-1,0),(73,'Băng đô bạch kim',2,11,0,0,49,450,0,55,0,3,0,0,0,0,0,0,36000,-1,14002,73,0,0,-1,NULL,-1,0),(74,'Băng đô bạch kim',2,10,0,0,49,450,0,55,0,4,0,0,0,0,0,0,36000,-1,14002,74,0,0,-1,NULL,-1,0),(75,'Băng đô kim cương',2,13,0,0,54,480,0,60,0,4,0,0,0,0,0,0,40000,-1,14002,75,0,0,-1,NULL,-1,0),(76,'Băng đô kim cương',2,12,0,0,54,480,0,60,0,4,0,0,0,0,0,0,40000,-1,14002,76,0,0,-1,NULL,-1,0),(77,'Băng đô kim cương',2,13,0,0,59,510,0,65,0,4,0,0,0,0,0,0,45000,-1,14002,77,0,0,-1,NULL,-1,0),(78,'Băng đô kim cương',2,12,0,0,59,510,0,65,0,4,0,0,0,0,0,0,45000,-1,14002,78,0,0,-1,NULL,-1,0),(79,'Than lửa sơ cấp',3,0,0,0,1,500,30,0,0,1,0,0,0,0,0,0,500,0,14003,0,0,0,-1,NULL,-1,0),(80,'Than lửa sơ cấp',3,0,0,0,6,550,50,0,0,4,0,0,0,0,0,0,5000,0,14003,0,1,0,-1,NULL,-1,0),(81,'Than lửa đồng',3,0,0,0,11,600,70,0,0,4,0,0,0,0,0,0,15000,0,14003,0,2,0,-1,NULL,-1,0),(82,'Than lửa đồng',3,1,0,0,16,650,90,0,0,8,0,0,0,0,0,0,30000,0,14003,1,0,0,-1,NULL,-1,0),(83,'Than lửa bạc',3,1,0,0,21,700,110,0,0,8,0,0,0,0,0,0,45000,0,14003,1,1,0,-1,NULL,-1,0),(86,'Hạt giống thần sơ cấp',4,0,0,0,1,400,50,0,0,1,0,0,0,0,0,0,500,1,14004,5,0,0,-1,NULL,-1,0),(87,'Hạt giống thần sơ cấp',4,0,0,0,6,450,70,0,0,2,0,0,0,0,0,0,5000,1,14004,5,1,0,-1,NULL,-1,0),(88,'Hạt giống thần đồng',4,0,0,0,11,500,90,0,0,2,0,0,0,0,0,0,15000,1,14004,5,2,0,-1,NULL,-1,0),(89,'Hạt giống thần đồng',4,1,0,0,16,600,110,0,0,3,0,0,0,0,0,0,30000,1,14004,6,0,0,-1,NULL,-1,0),(90,'Hạt giống thần bạc',4,1,0,0,21,650,130,0,0,3,0,0,0,0,0,0,45000,1,14004,6,1,0,-1,NULL,-1,0),(93,'Khăn lụa sơ cấp',5,0,0,0,1,1000,70,0,0,0,0,0,0,0,0,0,500,2,14005,10,0,0,-1,NULL,-1,0),(94,'Khăn lụa sơ cấp',5,0,0,0,6,1100,90,0,0,1,0,0,0,0,0,0,5000,2,14005,10,1,0,-1,NULL,-1,0),(95,'Khăn lụa đồng',5,0,0,0,11,1200,110,0,0,1,0,0,0,0,0,0,15000,2,14005,10,2,0,-1,NULL,-1,0),(96,'Khăn lụa đồng',5,1,0,0,16,1300,130,0,0,2,0,0,0,0,0,0,30000,2,14005,11,0,0,-1,NULL,-1,0),(97,'Khăn lụa bạc',5,1,0,0,21,1400,150,0,0,2,0,0,0,0,0,0,45000,2,14005,11,1,0,-1,NULL,-1,0),(100,'Nước thần sơ cấp',6,0,0,0,1,450,90,0,0,0,0,0,0,0,0,0,500,3,14006,15,0,0,-1,NULL,-1,0),(101,'Nước thần sơ cấp',6,0,0,0,6,550,110,0,0,1,0,0,0,0,0,0,5000,3,14006,15,1,0,-1,NULL,-1,0),(102,'Nước thần đồng',6,0,0,0,11,600,130,0,0,1,0,0,0,0,0,0,15000,3,14006,15,2,0,-1,NULL,-1,0),(103,'Nước thần đồng',6,1,0,0,16,700,150,0,0,2,0,0,0,0,0,0,30000,3,14006,16,0,0,-1,NULL,-1,0),(104,'Nước thần bạc',6,1,0,0,21,750,170,0,0,2,0,0,0,0,0,0,45000,3,14006,16,1,0,-1,NULL,-1,0),(107,'Nam châm sơ cấp',7,0,0,0,1,800,20,0,0,5,1,0,0,0,0,0,500,4,14007,20,0,0,-1,NULL,-1,0),(108,'Nam châm sơ cấp',7,0,0,0,6,900,40,0,0,7,2,0,0,0,0,0,5000,4,14007,20,1,0,-1,NULL,-1,0),(109,'Nam châm đồng',7,0,0,0,11,1000,60,0,0,7,3,0,0,0,0,0,15000,4,14007,20,2,0,-1,NULL,-1,0),(110,'Nam châm đồng',7,1,0,0,16,1100,80,0,0,10,4,0,0,0,0,0,30000,4,14007,21,0,0,-1,NULL,-1,0),(111,'Nam châm bạc',7,1,0,0,21,1200,100,0,0,10,5,0,0,0,0,0,45000,4,14007,21,1,0,-1,NULL,-1,0),(105,'Nước thần bạc',6,1,0,0,26,800,190,0,0,2,0,0,0,0,0,0,55000,3,14006,16,2,0,-1,NULL,-1,0),(84,'Than lửa bạc',3,1,0,0,26,800,130,0,0,8,0,0,0,0,0,0,60000,0,14003,1,2,0,-1,NULL,-1,0),(85,'Than lửa vàng',3,2,0,0,31,900,150,0,0,8,0,0,0,0,0,0,80000,0,14003,2,0,0,-1,NULL,-1,0),(91,'Hạt giống thần bạc',4,1,0,0,26,700,150,0,0,4,0,0,0,0,0,0,60000,1,14004,6,2,0,-1,NULL,-1,0),(92,'Hạt giống thần vàng',4,2,0,0,31,800,170,0,0,4,0,0,0,0,0,0,80000,1,14004,7,0,0,-1,NULL,-1,0),(98,'Khăn lụa bạc',5,1,0,0,26,1500,170,0,0,3,0,0,0,0,0,0,60000,2,14005,11,2,0,-1,NULL,-1,0),(99,'Khăn lụa vàng',5,2,0,0,31,1600,190,0,0,3,0,0,0,0,0,0,80000,2,14005,12,0,0,-1,NULL,-1,0),(106,'Nước thần vàng',6,2,0,0,31,850,210,0,0,3,0,0,0,0,0,0,75000,3,14006,17,0,0,-1,NULL,-1,0),(112,'Nam châm bạc',7,1,0,0,26,1250,110,0,0,12,6,0,0,0,0,0,55000,4,14007,21,2,0,-1,NULL,-1,0),(113,'Nam châm vàng',7,2,0,0,31,1300,120,0,0,12,7,0,0,0,0,0,75000,4,14007,22,0,0,-1,NULL,-1,0),(138,'Lông vũ tốc độ sơ cấp',10,12,0,0,4,250,0,12,2,0,0,0,0,0,0,0,500,-1,14010,12,0,0,-1,NULL,-1,0),(115,'Huy hiệu sơ cấp',8,0,0,0,9,280,10,0,0,1,0,0,0,0,0,0,2000,-1,14008,0,0,0,-1,NULL,-1,0),(116,'Huy hiệu đồng',8,1,0,0,14,300,15,0,0,2,0,0,0,0,0,0,4000,-1,14008,1,0,0,-1,NULL,-1,0),(117,'Huy hiệu đồng',8,1,0,0,19,320,20,0,0,2,0,0,0,0,0,0,10000,-1,14008,1,0,0,-1,NULL,-1,0),(118,'Huy hiệu bạc',8,2,0,0,24,350,25,0,0,3,0,0,0,0,0,0,19000,-1,14008,2,0,0,-1,NULL,-1,0),(119,'Huy hiệu bạc',8,2,0,0,29,370,30,0,0,3,0,0,0,0,0,0,31000,-1,14008,2,0,0,-1,NULL,-1,0),(120,'Huy hiệu vàng',8,3,0,0,34,390,35,0,0,4,0,0,0,0,0,0,39000,-1,14008,3,0,0,-1,NULL,-1,0),(121,'Huy hiệu vàng',8,3,0,0,39,410,40,0,0,4,0,0,0,0,0,0,45000,-1,14008,3,0,0,-1,NULL,-1,0),(122,'Huy hiệu bạch kim',8,4,0,0,44,430,45,0,0,5,0,0,0,0,0,0,50000,-1,14008,4,0,0,-1,NULL,-1,0),(123,'Huy hiệu bạch kim',8,4,0,0,49,450,50,0,0,5,0,0,0,0,0,0,59000,-1,14008,4,0,0,-1,NULL,-1,0),(124,'Huy hiệu kim cương',8,5,0,0,54,480,55,0,0,6,0,0,0,0,0,0,65000,-1,14008,5,0,0,-1,NULL,-1,0),(125,'Huy hiệu kim cương',8,5,0,0,59,510,60,0,0,6,0,0,0,0,0,0,71000,-1,14008,5,0,0,-1,NULL,-1,0),(126,'Vòng cổ sơ cấp',9,6,0,0,4,250,7,0,0,0,1,0,0,0,0,0,1500,-1,14009,6,0,0,-1,NULL,-1,0),(127,'Vòng cổ sơ cấp',9,6,0,0,9,280,12,0,0,0,2,0,0,0,0,0,2500,-1,14009,6,0,0,-1,NULL,-1,0),(128,'Vòng cổ đồng',9,7,0,0,14,300,17,0,0,0,2,0,0,0,0,0,4500,-1,14009,7,0,0,-1,NULL,-1,0),(129,'Vòng cổ đồng',9,7,0,0,19,320,22,0,0,0,3,0,0,0,0,0,12000,-1,14009,7,0,0,-1,NULL,-1,0),(130,'Vòng cổ bạc',9,8,0,0,24,350,27,0,0,0,3,0,0,0,0,0,21000,-1,14009,8,0,0,-1,NULL,-1,0),(131,'Vòng cổ bạc',9,8,0,0,29,370,32,0,0,0,3,0,0,0,0,0,34000,-1,14009,8,0,0,-1,NULL,-1,0),(132,'Vòng cổ vàng',9,9,0,0,34,390,37,0,0,0,4,0,0,0,0,0,41000,-1,14009,9,0,0,-1,NULL,-1,0),(133,'Vòng cổ vàng',9,9,0,0,39,410,42,0,0,0,4,0,0,0,0,0,47000,-1,14009,9,0,0,-1,NULL,-1,0),(134,'Vòng cổ bạch kim',9,10,0,0,44,430,47,0,0,0,4,0,0,0,0,0,52000,-1,14009,10,0,0,-1,NULL,-1,0),(135,'Vòng cổ bạch kim',9,10,0,0,49,450,52,0,0,0,5,0,0,0,0,0,61000,-1,14009,10,0,0,-1,NULL,-1,0),(136,'Vòng cổ kim cương',9,11,0,0,54,480,57,0,0,0,5,0,0,0,0,0,67000,-1,14009,11,0,0,-1,NULL,-1,0),(137,'Vòng cổ kim cương',9,11,0,0,59,510,62,0,0,0,5,0,0,0,0,0,75000,-1,14009,11,0,0,-1,NULL,-1,0),(114,'Huy hiệu sơ cấp',8,0,0,0,4,250,5,0,0,1,0,0,0,0,0,0,1000,-1,14008,0,0,0,-1,NULL,-1,0),(139,'Lông vũ tốc độ sơ cấp',10,12,0,0,9,280,0,17,2,0,0,0,0,0,0,0,1000,-1,14010,12,0,0,-1,NULL,-1,0),(140,'Lông vũ tốc độ đồng',10,13,0,0,14,300,0,22,3,0,0,0,0,0,0,0,2000,-1,14010,13,0,0,-1,NULL,-1,0),(141,'Lông vũ tốc độ đồng',10,13,0,0,19,320,0,27,3,0,0,0,0,0,0,0,5000,-1,14010,13,0,0,-1,NULL,-1,0),(142,'Lông vũ tốc độ bạc',10,14,0,0,24,350,0,32,4,0,0,0,0,0,0,0,13000,-1,14010,14,0,0,-1,NULL,-1,0),(143,'Lông vũ tốc độ bạc',10,14,0,0,29,370,0,37,4,0,0,0,0,0,0,0,19000,-1,14010,14,0,0,-1,NULL,-1,0),(144,'Lông vũ tốc độ vàng',10,15,0,0,34,390,0,42,5,0,0,0,0,0,0,0,23000,-1,14010,15,0,0,-1,NULL,-1,0),(145,'Lông vũ tốc độ vàng',10,15,0,0,39,410,0,47,5,0,0,0,0,0,0,0,29000,-1,14010,15,0,0,-1,NULL,-1,0),(146,'Lông vũ tốc độ bạch kim',10,16,0,0,44,430,0,52,6,0,0,0,0,0,0,0,34000,-1,14010,16,0,0,-1,NULL,-1,0),(147,'Lông vũ tốc độ bạch kim',10,16,0,0,49,450,0,57,6,0,0,0,0,0,0,0,39000,-1,14010,16,0,0,-1,NULL,-1,0),(148,'Lông vũ tốc độ kim cương',10,17,0,0,54,480,0,62,7,0,0,0,0,0,0,0,45000,-1,14010,17,0,0,-1,NULL,-1,0),(149,'Lông vũ tốc độ kim cương',10,17,0,0,59,510,0,67,7,0,0,0,0,0,0,0,50000,-1,14010,17,0,0,-1,NULL,-1,0),(150,'Găng đấm sơ cấp',11,18,0,0,4,250,0,10,0,1,1,0,0,0,0,0,500,-1,14011,18,0,0,-1,NULL,-1,0),(151,'Găng đấm sơ cấp',11,18,0,0,9,280,0,15,0,1,1,0,0,0,0,0,1000,-1,14011,18,0,0,-1,NULL,-1,0),(152,'Găng đấm đồng',11,19,0,0,14,300,0,20,0,2,2,0,0,0,0,0,2000,-1,14011,19,0,0,-1,NULL,-1,0),(153,'Găng đấm đồng',11,19,0,0,19,320,0,25,0,2,2,0,0,0,0,0,6000,-1,14011,19,0,0,-1,NULL,-1,0),(154,'Găng đấm bạc',11,20,0,0,24,350,0,30,0,3,2,0,0,0,0,0,12000,-1,14011,20,0,0,-1,NULL,-1,0),(155,'Găng đấm bạc',11,20,0,0,29,370,0,35,0,3,3,0,0,0,0,0,18000,-1,14011,20,0,0,-1,NULL,-1,0),(156,'Găng đấm vàng',11,21,0,0,34,390,0,40,0,4,3,0,0,0,0,0,22000,-1,14011,21,0,0,-1,NULL,-1,0),(157,'Găng đấm vàng',11,21,0,0,39,410,0,45,0,4,3,0,0,0,0,0,26000,-1,14011,21,0,0,-1,NULL,-1,0),(158,'Găng đấm bạch kim',11,22,0,0,44,430,0,50,0,5,4,0,0,0,0,0,34000,-1,14011,22,0,0,-1,NULL,-1,0),(159,'Găng đấm bạch kim',11,22,0,0,49,450,0,55,0,5,4,0,0,0,0,0,36000,-1,14011,22,0,0,-1,NULL,-1,0),(160,'Găng đấm kim cương',11,23,0,0,54,480,0,60,0,6,4,0,0,0,0,0,40000,-1,14011,23,0,0,-1,NULL,-1,0),(161,'Găng đấm kim cương',11,23,0,0,59,510,0,65,0,6,5,0,0,0,0,0,45000,-1,14011,23,0,0,-1,NULL,-1,0),(162,'Phiến đá hệ sơ cấp',12,25,0,0,4,250,0,0,0,1,1,1,0,0,0,0,2000,-1,14012,25,0,0,-1,NULL,-1,0),(163,'Phiến đá hệ sơ cấp',12,25,0,0,9,280,0,0,0,1,2,2,0,0,0,0,3000,-1,14012,25,0,0,-1,NULL,-1,0),(164,'Phiến đá hệ đồng',12,26,0,0,14,300,0,0,0,2,2,3,0,0,0,0,5000,-1,14012,26,0,0,-1,NULL,-1,0),(165,'Phiến đá hệ đồng',12,26,0,0,19,320,0,0,0,2,3,4,0,0,0,0,15000,-1,14012,26,0,0,-1,NULL,-1,0),(166,'Phiến đá hệ bạc',12,27,0,0,24,350,0,0,0,3,3,5,0,0,0,0,25000,-1,14012,27,0,0,-1,NULL,-1,0),(167,'Phiến đá hệ bạc',12,27,0,0,29,370,0,0,0,3,3,6,0,0,0,0,35000,-1,14012,27,0,0,-1,NULL,-1,0),(168,'Phiến đá hệ vàng',12,28,0,0,34,390,0,0,0,4,4,7,0,0,0,0,45000,-1,14012,28,0,0,-1,NULL,-1,0),(169,'Phiến đá hệ vàng',12,28,0,0,39,410,0,0,0,4,4,8,0,0,0,0,55000,-1,14012,28,0,0,-1,NULL,-1,0),(170,'Phiến đá hệ bạch kim',12,29,0,0,44,430,0,0,0,5,4,9,0,0,0,0,65000,-1,14012,29,0,0,-1,NULL,-1,0),(171,'Phiến đá hệ bạch kim',12,29,0,0,49,450,0,0,0,5,5,10,0,0,0,0,75000,-1,14012,29,0,0,-1,NULL,-1,0),(172,'Phiến đá hệ kim cương',12,30,0,0,54,480,0,0,0,6,5,11,0,0,0,0,85000,-1,14012,30,0,0,-1,NULL,-1,0),(173,'Phiến đá hệ kim cương',12,30,0,0,59,510,0,0,0,6,5,12,0,0,0,0,95000,-1,14012,30,0,0,-1,NULL,-1,0),(174,'Than lửa vàng',3,2,0,0,36,900,180,0,0,8,0,0,0,0,0,0,110000,0,14003,2,1,0,-1,NULL,-1,0),(175,'Than lửa bạch kim',3,2,0,0,41,900,200,0,0,8,0,0,0,0,0,0,140000,0,14003,2,2,0,-1,NULL,-1,0),(176,'Than lửa bạch kim',3,3,0,0,46,900,230,0,0,8,0,0,0,0,0,0,170000,0,14003,3,0,0,-1,NULL,-1,0),(177,'Than lửa kim cương',3,3,0,0,51,900,240,0,0,8,0,0,0,0,0,0,200000,0,14003,3,1,0,-1,NULL,-1,0),(178,'Than lửa kim cương',3,3,0,0,56,900,260,0,0,8,0,0,0,0,0,0,230000,0,14003,3,2,0,-1,NULL,-1,0),(179,'Than lửa chuyên gia',3,4,0,0,61,900,280,0,0,8,0,0,0,0,0,0,260000,0,14003,4,0,0,-1,NULL,-1,0),(180,'Than lửa chuyên gia',3,4,0,0,66,900,300,0,0,8,0,0,0,0,0,0,290000,0,14003,4,1,0,-1,NULL,-1,0),(181,'Than lửa tinh anh',3,4,0,0,71,900,320,0,0,8,0,0,0,0,0,0,320000,0,14003,4,2,0,-1,NULL,-1,0),(182,'Hạt giống thần vàng',4,2,0,0,36,800,190,0,0,4,0,0,0,0,0,0,120000,1,14004,7,1,0,-1,NULL,-1,0),(183,'Hạt giống thần bạch kim',4,2,0,0,41,800,210,0,0,4,0,0,0,0,0,0,150000,1,14004,7,2,0,-1,NULL,-1,0),(184,'Hạt giống thần bạch kim',4,3,0,0,46,800,230,0,0,4,0,0,0,0,0,0,180000,1,14004,8,0,0,-1,NULL,-1,0),(185,'Hạt giống thần kim cương',4,3,0,0,51,800,250,0,0,4,0,0,0,0,0,0,210000,1,14004,8,1,0,-1,NULL,-1,0),(186,'Hạt giống thần kim cương',4,3,0,0,56,800,270,0,0,4,0,0,0,0,0,0,240000,1,14004,8,2,0,-1,NULL,-1,0),(187,'Hạt giống thần chuyên gia',4,4,0,0,61,800,290,0,0,4,0,0,0,0,0,0,270000,1,14004,9,0,0,-1,NULL,-1,0),(188,'Hạt giống thần chuyên gia',4,4,0,0,66,800,310,0,0,4,0,0,0,0,0,0,300000,1,14004,9,1,0,-1,NULL,-1,0),(189,'Hạt giống thần tinh anh',4,4,0,0,71,800,330,0,0,4,0,0,0,0,0,0,330000,1,14004,9,2,0,-1,NULL,-1,0),(190,'Khăn lụa vàng',5,2,0,0,36,1600,200,0,0,3,0,0,0,0,0,0,120000,2,14005,12,1,0,-1,NULL,-1,0),(191,'Khăn lụa bạch kim',5,2,0,0,41,1600,225,0,0,3,0,0,0,0,0,0,155000,2,14005,12,2,0,-1,NULL,-1,0),(192,'Khăn lụa bạch kim',5,3,0,0,46,1600,245,0,0,3,0,0,0,0,0,0,190000,2,14005,13,0,0,-1,NULL,-1,0),(193,'Khăn lụa kim cương',5,3,0,0,51,1600,265,0,0,3,0,0,0,0,0,0,225000,2,14005,13,1,0,-1,NULL,-1,0),(194,'Khăn lụa kim cương',5,3,0,0,56,1600,285,0,0,3,0,0,0,0,0,0,260000,2,14005,13,2,0,-1,NULL,-1,0),(195,'Khăn lụa chuyên gia',5,4,0,0,61,1600,305,0,0,3,0,0,0,0,0,0,295000,2,14005,14,0,0,-1,NULL,-1,0),(196,'Khăn lụa chuyên gia',5,4,0,0,66,1600,325,0,0,3,0,0,0,0,0,0,320000,2,14005,14,1,0,-1,NULL,-1,0),(197,'Khăn lụa tinh anh',5,4,0,0,71,1600,345,0,0,3,0,0,0,0,0,0,355000,2,14005,14,2,0,-1,NULL,-1,0),(198,'Nước thần vàng',6,2,0,0,36,850,230,0,0,3,0,0,0,0,0,0,105000,3,14006,17,1,0,-1,NULL,-1,0),(199,'Nước thần bạch kim',6,2,0,0,41,850,250,0,0,3,0,0,0,0,0,0,135000,3,14006,17,2,0,-1,NULL,-1,0),(200,'Nước thần bạch kim',6,3,0,0,46,850,270,0,0,3,0,0,0,0,0,0,165000,3,14006,18,0,0,-1,NULL,-1,0),(201,'Nước thần kim cương',6,3,0,0,51,850,290,0,0,3,0,0,0,0,0,0,195000,3,14006,18,1,0,-1,NULL,-1,0),(202,'Nước thần kim cương',6,3,0,0,56,850,310,0,0,3,0,0,0,0,0,0,225000,3,14006,18,2,0,-1,NULL,-1,0),(203,'Nước thần chuyên gia',6,4,0,0,61,850,330,0,0,3,0,0,0,0,0,0,255000,3,14006,19,0,0,-1,NULL,-1,0),(204,'Nước thần chuyên gia',6,4,0,0,66,850,350,0,0,3,0,0,0,0,0,0,285000,3,14006,19,1,0,-1,NULL,-1,0),(205,'Nước thần tinh anh',6,4,0,0,71,850,370,0,0,3,0,0,0,0,0,0,315000,3,14006,19,2,0,-1,NULL,-1,0),(206,'Nam châm vàng',7,2,0,0,36,1300,150,0,0,12,0,0,0,0,0,0,100000,4,14007,22,1,0,-1,NULL,-1,0),(207,'Nam châm bạch kim',7,2,0,0,41,1300,170,0,0,12,0,0,0,0,0,0,130000,4,14007,22,2,0,-1,NULL,-1,0),(208,'Nam châm bạch kim',7,3,0,0,46,1300,190,0,0,12,0,0,0,0,0,0,160000,4,14007,23,0,0,-1,NULL,-1,0),(209,'Nam châm kim cương',7,3,0,0,51,1300,210,0,0,12,0,0,0,0,0,0,190000,4,14007,23,1,0,-1,NULL,-1,0),(210,'Nam châm kim cương',7,3,0,0,56,1300,230,0,0,12,0,0,0,0,0,0,220000,4,14007,23,2,0,-1,NULL,-1,0),(211,'Nam châm chuyên gia',7,4,0,0,61,1300,240,0,0,12,0,0,0,0,0,0,250000,4,14007,24,0,0,-1,NULL,-1,0),(212,'Nam châm chuyên gia',7,4,0,0,66,1300,260,0,0,12,0,0,0,0,0,0,280000,4,14007,24,1,0,-1,NULL,-1,0),(213,'Nam châm tinh anh',7,4,0,0,71,1300,280,0,0,12,0,0,0,0,0,0,310000,4,14007,24,2,0,-1,NULL,-1,0),(214,'Than lửa bạc',3,1,0,0,21,700,193,10,10,13,10,10,0,0,0,0,10,0,14003,0,4,4320,-1,NULL,-1,0),(215,'Than lửa bạc',3,1,0,0,26,800,219,10,10,14,10,10,0,0,0,0,15,0,14003,0,4,4320,-1,NULL,-1,0),(216,'Than lửa vàng',3,2,0,0,31,900,244,10,10,15,10,10,0,0,0,0,20,0,14003,1,4,4320,-1,NULL,-1,0),(217,'Than lửa vàng',3,2,0,0,36,900,281,10,10,15,10,10,0,0,0,0,25,0,14003,1,4,4320,-1,NULL,-1,0),(218,'Than lửa bạch kim',3,2,0,0,41,900,306,10,10,15,10,10,0,0,0,0,30,0,14003,2,4,4320,-1,NULL,-1,0),(219,'Than lửa bạch kim',3,3,0,0,46,900,343,10,10,15,10,10,0,0,0,0,40,0,14003,2,4,4320,-1,NULL,-1,0),(220,'Than lửa kim cương',3,3,0,0,51,900,356,10,10,15,10,10,0,0,0,0,50,0,14003,3,4,4320,-1,NULL,-1,0),(221,'Than lửa kim cương',3,3,0,0,56,900,381,10,10,15,10,10,0,0,0,0,60,0,14003,3,4,4320,-1,NULL,-1,0),(222,'Than lửa chuyên gia',3,4,0,0,61,900,406,10,10,15,10,10,0,0,0,0,70,0,14003,4,4,4320,-1,NULL,-1,0),(223,'Than lửa chuyên gia',3,4,0,0,66,900,431,10,10,15,10,10,0,0,0,0,80,0,14003,4,4,4320,-1,NULL,-1,0),(224,'Hạt giống thần bạc',4,1,0,0,21,650,218,10,10,13,10,10,0,0,0,0,10,1,14004,5,4,4320,-1,NULL,-1,0),(225,'Hạt giống thần bạc',4,1,0,0,26,700,243,10,10,14,10,10,0,0,0,0,15,1,14004,5,4,4320,-1,NULL,-1,0),(226,'Hạt giống thần vàng',4,2,0,0,31,800,268,10,10,15,10,10,0,0,0,0,20,1,14004,6,4,4320,-1,NULL,-1,0),(227,'Hạt giống thần vàng',4,2,0,0,36,800,293,10,10,15,10,10,0,0,0,0,25,1,14004,6,4,4320,-1,NULL,-1,0),(228,'Hạt giống thần bạch kim',4,2,0,0,41,800,318,10,10,15,10,10,0,0,0,0,30,1,14004,7,4,4320,-1,NULL,-1,0),(229,'Hạt giống thần bạch kim',4,3,0,0,46,800,343,10,10,15,10,10,0,0,0,0,40,1,14004,7,4,4320,-1,NULL,-1,0),(230,'Hạt giống thần kim cương',4,3,0,0,51,800,368,10,10,15,10,10,0,0,0,0,50,1,14004,8,4,4320,-1,NULL,-1,0),(231,'Hạt giống thần kim cương',4,3,0,0,56,800,393,10,10,15,10,10,0,0,0,0,60,1,14004,8,4,4320,-1,NULL,-1,0),(232,'Hạt giống thần chuyên gia',4,4,0,0,61,800,418,10,10,15,10,10,0,0,0,0,70,1,14004,9,4,4320,-1,NULL,-1,0),(233,'Hạt giống thần chuyên gia',4,4,0,0,66,800,443,10,10,15,10,10,0,0,0,0,80,1,14004,9,4,4320,-1,NULL,-1,0),(234,'Khăn lụa bạc',5,1,0,0,21,1400,243,10,10,13,10,10,0,0,0,0,10,2,14005,10,4,4320,-1,NULL,-1,0),(235,'Khăn lụa bạc',5,1,0,0,26,1500,268,10,10,10,10,10,0,0,0,0,15,2,14005,10,4,4320,-1,NULL,-1,0),(236,'Khăn lụa vàng',5,2,0,0,31,1600,293,10,10,15,10,10,0,0,0,0,20,2,14005,11,4,4320,-1,NULL,-1,0),(237,'Khăn lụa vàng',5,2,0,0,36,1600,306,10,10,15,10,10,0,0,0,0,25,2,14005,11,4,4320,-1,NULL,-1,0),(238,'Khăn lụa bạch kim',5,2,0,0,41,1600,337,10,10,15,10,10,0,0,0,0,30,2,14005,12,4,4320,-1,NULL,-1,0),(239,'Khăn lụa bạch kim',5,3,0,0,46,1600,372,10,10,15,10,10,0,0,0,0,40,2,14005,12,4,4320,-1,NULL,-1,0),(240,'Khăn lụa kim cương',5,3,0,0,51,1600,387,10,10,15,10,10,0,0,0,0,50,2,14005,13,4,4320,-1,NULL,-1,0),(241,'Khăn lụa kim cương',5,3,0,0,56,1600,412,10,10,15,10,10,0,0,0,0,60,2,14005,13,4,4320,-1,NULL,-1,0),(242,'Khăn lụa chuyên gia',5,4,0,0,61,1600,437,10,10,15,10,10,0,0,0,0,70,2,14005,14,4,4320,-1,NULL,-1,0),(243,'Khăn lụa chuyên gia',5,4,0,0,66,1600,472,10,10,15,10,10,0,0,0,0,80,2,14005,14,4,4320,-1,NULL,-1,0),(244,'Nước thần bạc',6,1,0,0,21,750,268,10,10,13,10,10,0,0,0,0,10,3,14006,15,4,4320,-1,NULL,-1,0),(245,'Nước thần bạc',6,1,0,0,26,800,293,10,10,14,10,10,0,0,0,0,15,3,14006,15,4,4320,-1,NULL,-1,0),(246,'Nước thần vàng',6,2,0,0,31,8500,318,10,10,15,10,10,0,0,0,0,20,3,14006,16,4,4320,-1,NULL,-1,0),(247,'Nước thần vàng',6,2,0,0,36,8500,343,10,10,13,10,10,0,0,0,0,25,3,14006,16,4,4320,-1,NULL,-1,0),(248,'Nước thần bạch kim',6,2,0,0,41,8500,368,10,10,10,10,10,0,0,0,0,30,3,14006,17,4,4320,-1,NULL,-1,0),(249,'Nước thần bạch kim',6,3,0,0,46,8500,393,10,10,10,10,10,0,0,0,0,40,3,14006,17,4,4320,-1,NULL,-1,0),(250,'Nước thần kim cương',6,3,0,0,51,8500,418,10,10,10,10,10,0,0,0,0,50,3,14006,18,4,4320,-1,NULL,-1,0),(251,'Nước thần kim cương',6,3,0,0,56,850,443,10,10,10,10,10,0,0,0,0,60,3,14006,18,4,4320,-1,NULL,-1,0),(252,'Nước thần chuyên gia',6,4,0,0,61,8500,468,10,10,10,10,10,0,0,0,0,70,3,14006,19,4,4320,-1,NULL,-1,0),(253,'Nước thần chuyên gia',6,4,0,0,66,8500,493,10,10,10,10,10,0,0,0,0,80,3,14006,19,4,4320,-1,NULL,-1,0),(254,'Nam châm bạc',7,1,0,0,21,1200,181,10,10,15,10,10,0,0,0,0,10,4,14007,20,4,4320,-1,NULL,-1,0),(255,'Nam châm bạc',7,1,0,0,26,1250,194,10,10,16,10,10,0,0,0,0,15,4,14007,20,4,4320,-1,NULL,-1,0),(256,'Nam châm vàng',7,2,0,0,31,1300,206,10,10,17,10,10,0,0,0,0,20,4,14007,21,4,4320,-1,NULL,-1,0),(257,'Nam châm vàng',7,2,0,0,36,1300,244,10,10,17,10,10,0,0,0,0,25,4,14007,21,4,4320,-1,NULL,-1,0),(258,'Nam châm bạch kim',7,2,0,0,41,1300,269,10,10,17,10,10,0,0,0,0,30,4,14007,22,4,4320,-1,NULL,-1,0),(259,'Nam châm bạch kim',7,3,0,0,46,1300,294,10,10,17,10,10,0,0,0,0,40,4,14007,22,4,4320,-1,NULL,-1,0),(260,'Nam châm kim cương',7,3,0,0,51,1300,318,10,10,17,10,10,0,0,0,0,50,4,14007,23,4,4320,-1,NULL,-1,0),(261,'Nam châm kim cương',7,3,0,0,56,1300,343,10,10,17,10,10,0,0,0,0,60,4,14007,23,4,4320,-1,NULL,-1,0),(262,'Nam châm chuyên gia',7,4,0,0,61,1300,356,10,10,17,10,10,0,0,0,0,70,4,14007,24,4,4320,-1,NULL,-1,0),(263,'Nam châm chuyên gia',7,4,0,0,66,1300,381,10,10,17,10,10,0,0,0,0,80,4,14007,24,4,4320,-1,NULL,-1,0),(267,'Áo dài',0,37,0,0,1,5000,6,6,3000,3000,14,14,14,14,14,1,100,-1,14000,31,0,10080,-1,NULL,-1,0),(264,'Áo Lunala',0,38,0,0,1,5000,6,6,3000,3000,19,19,19,19,19,1,100,-1,14000,32,0,10080,-1,NULL,-1,0),(265,'Áo Rayquaza',0,39,0,0,1,5000,10,10,3000,3000,15,15,15,15,15,1,100,-1,14000,33,0,10080,-1,NULL,-1,0),(266,'Áo giáo sư',0,40,0,0,1,5000,6,6,3000,3000,18,18,18,18,18,1,100,-1,14000,34,0,10080,-1,NULL,-1,0),(268,'Khăn quàng bạc',0,22,0,0,20,1300,0,75,0,6,0,0,0,0,0,0,25000,-1,14000,35,0,0,-1,NULL,-1,0),(269,'Khăn quàng bạc',0,34,0,0,20,1300,0,75,0,6,0,0,0,0,0,0,25000,-1,14000,36,0,0,-1,NULL,-1,0),(270,'Khăn quàng bạc',0,22,0,0,25,1300,0,80,0,6,0,0,0,0,0,0,25000,-1,14000,35,0,0,-1,NULL,-1,0),(271,'Khăn quàng bạc',0,34,0,0,25,1300,0,80,0,6,0,0,0,0,0,0,25000,-1,14000,36,0,0,-1,NULL,-1,0),(272,'Khăn quàng vàng',0,22,0,0,30,1300,0,85,0,7,0,0,0,0,0,0,25000,-1,14000,35,0,0,-1,NULL,-1,0),(273,'Khăn quàng vàng',0,34,0,0,30,1300,0,85,0,7,0,0,0,0,0,0,25000,-1,14000,36,0,0,-1,NULL,-1,0),(274,'Khăn quàng vàng',0,22,0,0,35,1300,0,90,0,7,0,0,0,0,0,0,25000,-1,14000,37,0,0,-1,NULL,-1,0),(275,'Khăn quàng vàng',0,34,0,0,35,1300,0,90,0,7,0,0,0,0,0,0,25000,-1,14000,38,0,0,-1,NULL,-1,0),(276,'Khăn quàng bạch kim',0,23,0,0,40,1300,0,1,1,1,1,1,1,1,1,0,25000,-1,14000,37,0,0,-1,NULL,-1,0),(277,'Khăn quàng bạch kim',0,35,0,0,40,1300,0,1,1,1,1,1,1,1,1,0,25000,-1,14000,38,0,0,-1,NULL,-1,0),(278,'Khăn quàng bạch kim',0,23,0,0,45,1300,0,1,1,1,1,1,1,1,1,0,25000,-1,14000,37,0,0,-1,NULL,-1,0),(279,'Khăn quàng bạch kim',0,35,0,0,45,1300,0,1,1,1,1,1,1,1,1,0,25000,-1,14000,38,0,0,-1,NULL,-1,0),(280,'Khăn quàng kim cương',0,23,0,0,50,1300,0,1,1,1,1,1,1,1,1,0,25000,-1,14000,39,0,0,-1,NULL,-1,0),(281,'Khăn quàng kim cương',0,35,0,0,50,1300,0,1,1,1,1,1,1,1,1,0,25000,-1,14000,40,0,0,-1,NULL,-1,0),(282,'Khăn quàng kim cương',0,24,0,0,55,1300,0,1,1,1,1,1,1,1,1,0,25000,-1,14000,39,0,0,-1,NULL,-1,0),(283,'Khăn quàng kim cương',0,36,0,0,55,1300,0,1,1,1,1,1,1,1,1,0,25000,-1,14000,40,0,0,-1,NULL,-1,0),(284,'Khăn quàng chuyên gia',0,24,0,0,60,1300,0,1,1,1,1,1,1,1,1,0,25000,-1,14000,39,0,0,-1,NULL,-1,0),(285,'Khăn quàng chuyên gia',0,36,0,0,60,1300,0,1,1,1,1,1,1,1,1,0,25000,-1,14000,40,0,0,-1,NULL,-1,0),(286,'Khăn quàng chuyên gia',0,24,0,0,65,1300,0,1,1,1,1,1,1,1,1,0,25000,-1,14000,41,0,0,-1,NULL,-1,0),(287,'Khăn quàng chuyên gia',0,36,0,0,65,1300,0,1,1,1,1,1,1,1,1,0,25000,-1,14000,42,0,0,-1,NULL,-1,0),(288,'Khăn quàng tinh anh',0,25,0,0,70,1300,0,1,1,1,1,1,1,1,1,0,25000,-1,14000,41,0,0,-1,NULL,-1,0),(289,'Khăn quàng tinh anh',0,37,0,0,70,1300,0,1,1,1,1,1,1,1,1,0,25000,-1,14000,42,0,0,-1,NULL,-1,0),(290,'Khăn quàng tinh anh',0,25,0,0,75,1300,0,1,1,1,1,1,1,1,1,0,25000,-1,14000,41,0,0,640,NULL,-1,0),(291,'Khăn quàng tinh anh',0,37,0,0,75,1300,0,1,1,1,1,1,1,1,1,0,25000,-1,14000,42,0,0,639,NULL,-1,0),(292,'Đai bạc',1,22,0,0,20,1300,0,1,1,1,1,1,1,1,1,0,25000,-1,14001,59,0,0,-1,NULL,-1,0),(293,'Đai bạc',1,34,0,0,20,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14001,60,0,0,-1,NULL,-1,0),(294,'Đai bạc',1,22,0,0,25,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14001,59,0,0,-1,NULL,-1,0),(295,'Đai bạc',1,34,0,0,25,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14001,60,0,0,-1,NULL,-1,0),(296,'Đai vàng',1,22,0,0,30,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14001,59,0,0,-1,NULL,-1,0),(297,'Đai vàng',1,34,0,0,30,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14001,60,0,0,-1,NULL,-1,0),(298,'Đai vàng',1,22,0,0,35,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14001,61,0,0,-1,NULL,-1,0),(299,'Đai vàng',1,34,0,0,35,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14001,62,0,0,-1,NULL,-1,0),(300,'Đai bạch kim',1,23,0,0,40,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14001,61,0,0,-1,NULL,-1,0),(301,'Đai bạch kim',1,35,0,0,40,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14001,62,0,0,-1,NULL,-1,0),(302,'Đai bạch kim',1,23,0,0,45,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14001,61,0,0,-1,NULL,-1,0),(303,'Đai bạch kim',1,35,0,0,45,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14001,62,0,0,-1,NULL,-1,0),(304,'Đai kim cương',1,23,0,0,50,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14001,63,0,0,-1,NULL,-1,0),(305,'Đai kim cương',1,35,0,0,50,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14001,64,0,0,-1,NULL,-1,0),(306,'Đai kim cương',1,24,0,0,55,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14001,63,0,0,-1,NULL,-1,0),(307,'Đai kim cương',1,36,0,0,55,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14001,64,0,0,-1,NULL,-1,0),(308,'Đai chuyên gia',1,24,0,0,60,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14001,63,0,0,-1,NULL,-1,0),(309,'Đai chuyên gia',1,36,0,0,60,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14001,64,0,0,-1,NULL,-1,0),(310,'Đai chuyên gia',1,24,0,0,65,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14001,65,0,0,-1,NULL,-1,0),(311,'Đai chuyên gia',1,36,0,0,65,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14001,66,0,0,-1,NULL,-1,0),(312,'Đai tinh anh',1,25,0,0,70,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14001,65,0,0,-1,NULL,-1,0),(313,'Đai tinh anh',1,37,0,0,70,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14001,66,0,0,-1,NULL,-1,0),(314,'Đai tinh anh',1,25,0,0,75,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14001,65,0,0,644,NULL,-1,0),(315,'Đai tinh anh',1,37,0,0,75,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14001,66,0,0,643,NULL,-1,0),(316,'Băng đô bạc',2,22,0,0,20,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14002,5,0,0,-1,NULL,-1,0),(317,'Băng đô bạc',2,34,0,0,20,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14002,6,0,0,-1,NULL,-1,0),(318,'Băng đô bạc',2,22,0,0,25,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14002,5,0,0,-1,NULL,-1,0),(319,'Băng đô bạc',2,34,0,0,25,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14002,6,0,0,-1,NULL,-1,0),(320,'Băng đô vàng',2,22,0,0,30,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14002,5,0,0,-1,NULL,-1,0),(321,'Băng đô vàng',2,34,0,0,30,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14002,6,0,0,-1,NULL,-1,0),(322,'Băng đô vàng',2,22,0,0,35,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14002,7,0,0,-1,NULL,-1,0),(323,'Băng đô vàng',2,34,0,0,35,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14002,8,0,0,-1,NULL,-1,0),(324,'Băng đô bạch kim',2,23,0,0,40,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14002,7,0,0,-1,NULL,-1,0),(325,'Băng đô bạch kim',2,35,0,0,40,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14002,8,0,0,-1,NULL,-1,0),(326,'Băng đô bạch kim',2,23,0,0,45,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14002,7,0,0,-1,NULL,-1,0),(327,'Băng đô bạch kim',2,35,0,0,45,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14002,8,0,0,-1,NULL,-1,0),(328,'Băng đô kim cương',2,23,0,0,50,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14002,9,0,0,-1,NULL,-1,0),(329,'Băng đô kim cương',2,35,0,0,50,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14002,10,0,0,-1,NULL,-1,0),(330,'Băng đô kim cương',2,24,0,0,55,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14002,9,0,0,-1,NULL,-1,0),(331,'Băng đô kim cương',2,36,0,0,55,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14002,10,0,0,-1,NULL,-1,0),(332,'Băng đô chuyên gia',2,24,0,0,60,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14002,9,0,0,-1,NULL,-1,0),(333,'Băng đô chuyên gia',2,36,0,0,60,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14002,10,0,0,-1,NULL,-1,0),(334,'Băng đô chuyên gia',2,24,0,0,65,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14002,11,0,0,-1,NULL,-1,0),(335,'Băng đô chuyên gia',2,36,0,0,65,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14002,12,0,0,-1,NULL,-1,0),(336,'Băng đô tinh anh',2,25,0,0,70,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14002,11,0,0,-1,NULL,-1,0),(337,'Băng đô tinh anh',2,37,0,0,70,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14002,12,0,0,-1,NULL,-1,0),(338,'Băng đô tinh anh',2,25,0,0,75,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14002,11,0,0,648,NULL,-1,0),(339,'Băng đô tinh anh',2,37,0,0,75,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14002,12,0,0,647,NULL,-1,0),(352,'Găng đấm bạc',11,18,0,0,20,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14011,41,0,0,-1,NULL,-1,0),(357,'Găng đấm bạch kim',11,20,0,0,45,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14011,42,0,0,-1,NULL,-1,0),(356,'Găng đấm bạch kim',11,20,0,0,40,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14011,42,0,0,-1,NULL,-1,0),(355,'Găng đấm vàng',11,19,0,0,35,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14011,42,0,0,-1,NULL,-1,0),(354,'Găng đấm vàng',11,19,0,0,30,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14011,41,0,0,-1,NULL,-1,0),(353,'Găng đấm bạc',11,18,0,0,25,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14011,41,0,0,-1,NULL,-1,0),(358,'Găng đấm kim cương',11,21,0,0,50,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14011,43,0,0,-1,NULL,-1,0),(359,'Găng đấm kim cương',11,21,0,0,55,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14011,43,0,0,-1,NULL,-1,0),(360,'Găng đấm chuyên gia',11,22,0,0,60,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14011,43,0,0,-1,NULL,-1,0),(361,'Găng đấm chuyên gia',11,22,0,0,65,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14011,44,0,0,-1,NULL,-1,0),(362,'Găng đấm tinh anh',11,23,0,0,70,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14011,44,0,0,-1,NULL,-1,0),(363,'Găng đấm tinh anh',11,23,0,0,75,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14011,44,0,0,663,NULL,-1,0),(340,'Lông vũ tốc độ bạc',10,12,0,0,20,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14010,29,0,0,-1,NULL,-1,0),(341,'Lông vũ tốc độ bạc',10,12,0,0,25,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14010,29,0,0,-1,NULL,-1,0),(342,'Lông vũ tốc độ vàng',10,23,0,0,30,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14010,29,0,0,-1,NULL,-1,0),(343,'Lông vũ tốc độ vàng',10,13,0,0,35,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14010,30,0,0,-1,NULL,-1,0),(344,'Lông vũ tốc độ bạch kim',10,14,0,0,40,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14010,30,0,0,-1,NULL,-1,0),(345,'Lông vũ tốc độ bạch kim',10,14,0,0,45,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14010,30,0,0,-1,NULL,-1,0),(346,'Lông vũ tốc độ kim cương',10,15,0,0,50,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14010,31,0,0,-1,NULL,-1,0),(347,'Lông vũ tốc độ kim cương',10,15,0,0,55,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14010,31,0,0,-1,NULL,-1,0),(348,'Lông vũ tốc độ chuyên gia',10,16,0,0,60,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14010,31,0,0,-1,NULL,-1,0),(349,'Lông vũ tốc độ chuyên gia',10,16,0,0,65,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14010,32,0,0,-1,NULL,-1,0),(350,'Lông vũ tốc độ tinh anh',10,17,0,0,70,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14010,32,0,0,-1,NULL,-1,0),(351,'Lông vũ tốc độ tinh anh',10,17,0,0,75,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14010,32,0,0,659,NULL,-1,0),(364,'Huy hiệu bạc',8,0,0,0,20,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14008,53,0,0,-1,NULL,-1,0),(365,'Huy hiệu bạc',8,0,0,0,25,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14008,53,0,0,-1,NULL,-1,0),(366,'Huy hiệu vàng',8,1,0,0,30,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14008,53,0,0,-1,NULL,-1,0),(367,'Huy hiệu vàng',8,1,0,0,35,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14008,54,0,0,-1,NULL,-1,0),(368,'Huy hiệu bạch kim',8,2,0,0,40,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14008,54,0,0,-1,NULL,-1,0),(369,'Huy hiệu bạch kim',8,2,0,0,45,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14008,54,0,0,-1,NULL,-1,0),(370,'Huy hiệu kim cương',8,3,0,0,50,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14008,55,0,0,-1,NULL,-1,0),(371,'Huy hiệu kim cương',8,3,0,0,55,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14008,55,0,0,-1,NULL,-1,0),(372,'Huy hiệu chuyên gia',8,4,0,0,60,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14008,55,0,0,-1,NULL,-1,0),(373,'Huy hiệu chuyên gia',8,4,0,0,65,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14008,56,0,0,-1,NULL,-1,0),(374,'Huy hiệu tinh anh',8,5,0,0,70,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14008,56,0,0,-1,NULL,-1,0),(375,'Huy hiệu tinh anh',8,5,0,0,75,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14008,56,0,0,651,NULL,-1,0),(376,'Vòng cổ bạc',9,6,0,0,20,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14009,65,0,0,-1,NULL,-1,0),(377,'Vòng cổ bạc',9,6,0,0,25,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14009,65,0,0,-1,NULL,-1,0),(378,'Vòng cổ vàng',9,7,0,0,30,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14009,65,0,0,-1,NULL,-1,0),(379,'Vòng cổ vàng',9,7,0,0,35,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14009,66,0,0,-1,NULL,-1,0),(380,'Vòng cổ bạch kim',9,8,0,0,40,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14009,66,0,0,-1,NULL,-1,0),(381,'Vòng cổ bạch kim',9,8,0,0,45,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14009,66,0,0,-1,NULL,-1,0),(382,'Vòng cổ kim cương',9,9,0,0,50,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14009,67,0,0,-1,NULL,-1,0),(383,'Vòng cổ kim cương',9,9,0,0,55,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14009,67,0,0,-1,NULL,-1,0),(384,'Vòng cổ chuyên gia',9,10,0,0,60,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14009,67,0,0,-1,NULL,-1,0),(385,'Vòng cổ chuyên gia',9,10,0,0,65,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14009,68,0,0,-1,NULL,-1,0),(386,'Vòng cổ tinh anh',9,11,0,0,70,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14009,68,0,0,-1,NULL,-1,0),(387,'Vòng cổ tinh anh',9,11,0,0,75,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14009,68,0,0,655,NULL,-1,0),(388,'Phiến đá hệ bạc',12,25,0,0,20,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14012,0,0,0,-1,NULL,-1,0),(389,'Phiến đá hệ bạc',12,25,0,0,25,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14012,0,0,0,-1,NULL,-1,0),(390,'Phiến đá hệ vàng',12,26,0,0,30,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14012,0,0,0,-1,NULL,-1,0),(391,'Phiến đá hệ vàng',12,26,0,0,35,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14012,1,0,0,-1,NULL,-1,0),(392,'Phiến đá hệ bạch kim',12,27,0,0,40,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14012,1,0,0,-1,NULL,-1,0),(393,'Phiến đá hệ bạch kim',12,27,0,0,45,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14012,1,0,0,-1,NULL,-1,0),(394,'Phiến đá hệ kim cương',12,28,0,0,50,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14012,2,0,0,-1,NULL,-1,0),(395,'Phiến đá hệ kim cương',12,28,0,0,55,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14012,2,0,0,-1,NULL,-1,0),(396,'Phiến đá hệ chuyên gia',12,29,0,0,60,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14012,2,0,0,-1,NULL,-1,0),(397,'Phiến đá hệ chuyên gia',12,29,0,0,65,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14012,3,0,0,-1,NULL,-1,0),(398,'Phiến đá hệ tinh anh',12,30,0,0,70,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14012,3,0,0,-1,NULL,-1,0),(399,'Phiến đá hệ tinh anh',12,30,0,0,75,1300,0,0,0,0,0,0,0,0,0,0,25000,-1,14012,3,0,0,667,NULL,-1,0),(413,'Hạt giống thần bạc',4,1,0,0,21,1300,0,0,0,0,0,0,0,0,0,0,25000,1,14004,1,4,0,-1,NULL,-1,0),(414,'Hạt giống thần bạc',4,1,0,0,26,1300,0,0,0,0,0,0,0,0,0,0,25000,1,14004,1,4,0,-1,NULL,-1,0),(415,'Hạt giống thần vàng',4,1,0,0,31,1300,0,0,0,0,0,0,0,0,0,0,25000,1,14004,1,5,0,-1,NULL,-1,0),(416,'Hạt giống thần vàng',4,1,0,0,36,1300,0,0,0,0,0,0,0,0,0,0,25000,1,14004,1,5,0,-1,NULL,-1,0),(417,'Hạt giống thần bạch kim',4,2,0,0,41,1300,0,0,0,0,0,0,0,0,0,0,25000,1,14004,1,4,0,-1,NULL,-1,0),(418,'Hạt giống thần bạch kim',4,2,0,0,46,1300,0,0,0,0,0,0,0,0,0,0,25000,1,14004,1,4,0,-1,NULL,-1,0),(419,'Hạt giống thần kim cương',4,2,0,0,51,1300,0,0,0,0,0,0,0,0,0,0,25000,1,14004,1,5,0,-1,NULL,-1,0),(420,'Hạt giống thần kim cương',4,2,0,0,56,1300,0,0,0,0,0,0,0,0,0,0,25000,1,14004,1,5,0,-1,NULL,-1,0),(421,'Hạt giống thần chuyên gia',4,3,0,0,61,1300,0,0,0,0,0,0,0,0,0,0,25000,1,14004,1,4,0,-1,NULL,-1,0),(422,'Hạt giống thần chuyên gia',4,3,0,0,66,1300,0,0,0,0,0,0,0,0,0,0,25000,1,14004,1,4,0,-1,NULL,-1,0),(423,'Hạt giống thần tinh anh',4,3,0,0,71,1300,0,0,0,0,0,0,0,0,0,0,25000,1,14004,1,5,0,-1,NULL,-1,0),(424,'Hạt giống thần tinh anh',4,3,0,0,76,1300,0,0,0,0,0,0,0,0,0,0,25000,1,14004,1,5,0,-1,NULL,-1,0),(425,'Hạt giống thần huyền thoại',4,4,0,0,81,1300,0,0,0,0,0,0,0,0,0,0,25000,1,14004,5,4,0,894,NULL,-1,0),(400,'Than lửa bạc',3,1,0,0,21,1300,0,0,0,0,0,0,0,0,0,0,25000,0,14003,1,4,0,-1,NULL,-1,0),(401,'Than lửa bạc',3,1,0,0,26,1300,0,0,0,0,0,0,0,0,0,0,25000,0,14003,1,4,0,-1,NULL,-1,0),(402,'Than lửa vàng',3,1,0,0,31,1300,0,0,0,0,0,0,0,0,0,0,25000,0,14003,1,5,0,-1,NULL,-1,0),(403,'Than lửa vàng',3,1,0,0,36,1300,0,0,0,0,0,0,0,0,0,0,25000,0,14003,1,5,0,-1,NULL,-1,0),(404,'Than lửa bạch kim',3,2,0,0,41,1300,0,0,0,0,0,0,0,0,0,0,25000,0,14003,1,4,0,-1,NULL,-1,0),(405,'Than lửa bạch kim',3,2,0,0,46,1300,0,0,0,0,0,0,0,0,0,0,25000,0,14003,1,4,0,-1,NULL,-1,0),(406,'Than lửa kim cương',3,2,0,0,51,1300,0,0,0,0,0,0,0,0,0,0,25000,0,14003,1,5,0,-1,NULL,-1,0),(407,'Than lửa kim cương',3,2,0,0,56,1300,0,0,0,0,0,0,0,0,0,0,25000,0,14003,1,5,0,-1,NULL,-1,0),(408,'Than lửa chuyên gia',3,3,0,0,61,1300,0,0,0,0,0,0,0,0,0,0,25000,0,14003,1,4,0,-1,NULL,-1,0),(409,'Than lửa chuyên gia',3,3,0,0,66,1300,0,0,0,0,0,0,0,0,0,0,25000,0,14003,1,4,0,-1,NULL,-1,0),(411,'Than lửa tinh anh',3,3,0,0,76,1300,0,0,0,0,0,0,0,0,0,0,25000,0,14003,1,5,0,-1,NULL,-1,0),(412,'Than lửa huyền thoại',3,4,0,0,81,1300,0,0,0,0,0,0,0,0,0,0,25000,0,14003,5,3,0,893,NULL,-1,0),(410,'Than lửa tinh anh',3,3,0,0,71,1300,0,0,0,0,0,0,0,0,0,0,25000,0,14003,1,5,0,-1,NULL,-1,0),(439,'Nước thần bạc',6,1,0,0,21,1300,0,0,0,0,0,0,0,0,0,0,25000,3,14006,1,4,0,-1,NULL,-1,0),(440,'Nước thần bạc',6,1,0,0,26,1300,0,0,0,0,0,0,0,0,0,0,25000,3,14006,1,4,0,-1,NULL,-1,0),(441,'Nước thần vàng',6,1,0,0,31,1300,0,0,0,0,0,0,0,0,0,0,25000,3,14006,1,5,0,-1,NULL,-1,0),(442,'Nước thần vàng',6,1,0,0,36,1300,0,0,0,0,0,0,0,0,0,0,25000,3,14006,1,5,0,-1,NULL,-1,0),(443,'Nước thần bạch kim',6,2,0,0,41,1300,0,0,0,0,0,0,0,0,0,0,25000,3,14006,1,4,0,-1,NULL,-1,0),(444,'Nước thần bạch kim',6,2,0,0,46,1300,0,0,0,0,0,0,0,0,0,0,25000,3,14006,1,4,0,-1,NULL,-1,0),(445,'Nước thần kim cương',6,2,0,0,51,1300,0,0,0,0,0,0,0,0,0,0,25000,3,14006,1,5,0,-1,NULL,-1,0),(446,'Nước thần kim cương',6,2,0,0,56,1300,0,0,0,0,0,0,0,0,0,0,25000,3,14006,1,5,0,-1,NULL,-1,0),(447,'Nước thần chuyên gia',6,3,0,0,61,1300,0,0,0,0,0,0,0,0,0,0,25000,3,14006,1,4,0,-1,NULL,-1,0),(448,'Nước thần chuyên gia',6,3,0,0,66,1300,0,0,0,0,0,0,0,0,0,0,25000,3,14006,1,4,0,-1,NULL,-1,0),(449,'Nước thần tinh anh',6,3,0,0,71,1300,0,0,0,0,0,0,0,0,0,0,25000,3,14006,1,5,0,-1,NULL,-1,0),(450,'Nước thần tinh anh',6,3,0,0,76,1300,0,0,0,0,0,0,0,0,0,0,25000,3,14006,1,5,0,-1,NULL,-1,0),(451,'Nước thần huyền thoại',6,4,0,0,81,1300,0,0,0,0,0,0,0,0,0,0,25000,3,14006,1,3,0,896,NULL,-1,0),(426,'Khăn lụa bạc',5,1,0,0,21,1300,0,0,0,0,0,0,0,0,0,0,25000,2,14005,1,4,0,-1,NULL,-1,0),(427,'Khăn lụa bạc',5,1,0,0,26,1300,0,0,0,0,0,0,0,0,0,0,25000,2,14005,1,4,0,-1,NULL,-1,0),(428,'Khăn lụa vàng',5,1,0,0,31,1300,0,0,0,0,0,0,0,0,0,0,25000,2,14005,1,5,0,-1,NULL,-1,0),(429,'Khăn lụa vàng',5,1,0,0,36,1300,0,0,0,0,0,0,0,0,0,0,25000,2,14005,1,5,0,-1,NULL,-1,0),(430,'Khăn lụa bạch kim',5,2,0,0,41,1300,0,0,0,0,0,0,0,0,0,0,25000,2,14005,1,4,0,-1,NULL,-1,0),(431,'Khăn lụa bạch kim',5,2,0,0,46,1300,0,0,0,0,0,0,0,0,0,0,25000,2,14005,1,4,0,-1,NULL,-1,0),(432,'Khăn lụa kim cương',5,2,0,0,51,1300,0,0,0,0,0,0,0,0,0,0,25000,2,14005,1,5,0,-1,NULL,-1,0),(433,'Khăn lụa kim cương',5,2,0,0,56,1300,0,0,0,0,0,0,0,0,0,0,25000,2,14005,1,5,0,-1,NULL,-1,0),(434,'Khăn lụa chuyên gia',5,3,0,0,61,1300,0,0,0,0,0,0,0,0,0,0,25000,2,14005,1,4,0,-1,NULL,-1,0),(435,'Khăn lụa chuyên gia',5,3,0,0,66,1300,0,0,0,0,0,0,0,0,0,0,25000,2,14005,1,4,0,-1,NULL,-1,0),(436,'Khăn lụa tinh anh',5,3,0,0,71,1300,0,0,0,0,0,0,0,0,0,0,25000,2,14005,1,5,0,-1,NULL,-1,0),(437,'Khăn lụa tinh anh',5,3,0,0,76,1300,0,0,0,0,0,0,0,0,0,0,25000,2,14005,1,5,0,-1,NULL,-1,0),(438,'Khăn lụa huyền thoại',5,4,0,0,81,1300,0,0,0,0,0,0,0,0,0,0,25000,2,14005,1,3,0,895,NULL,-1,0),(452,'Nam châm bạc',7,1,0,0,21,1300,0,0,0,0,0,0,0,0,0,0,25000,4,14007,1,4,0,-1,NULL,-1,0),(453,'Nam châm bạc',7,1,0,0,26,1300,0,0,0,0,0,0,0,0,0,0,25000,4,14007,1,4,0,-1,NULL,-1,0),(454,'Nam châm vàng',7,1,0,0,31,1300,0,0,0,0,0,0,0,0,0,0,25000,4,14007,1,5,0,-1,NULL,-1,0),(455,'Nam châm vàng',7,1,0,0,36,1300,0,0,0,0,0,0,0,0,0,0,25000,4,14007,1,5,0,-1,NULL,-1,0),(456,'Nam châm bạch kim',7,2,0,0,41,1300,0,0,0,0,0,0,0,0,0,0,25000,4,14007,1,4,0,-1,NULL,-1,0),(457,'Nam châm bạch kim',7,2,0,0,46,1300,0,0,0,0,0,0,0,0,0,0,25000,4,14007,1,4,0,-1,NULL,-1,0),(458,'Nam châm kim cương',7,2,0,0,51,1300,0,0,0,0,0,0,0,0,0,0,25000,4,14007,1,5,0,-1,NULL,-1,0),(459,'Nam châm kim cương',7,2,0,0,56,1300,0,0,0,0,0,0,0,0,0,0,25000,4,14007,1,5,0,-1,NULL,-1,0),(460,'Nam châm chuyên gia',7,3,0,0,61,1300,0,0,0,0,0,0,0,0,0,0,25000,4,14007,1,4,0,-1,NULL,-1,0),(461,'Nam châm chuyên gia',7,3,0,0,66,1300,0,0,0,0,0,0,0,0,0,0,25000,4,14007,1,4,0,-1,NULL,-1,0),(462,'Nam châm tinh anh',7,3,0,0,71,1300,0,0,0,0,0,0,0,0,0,0,25000,4,14007,1,5,0,-1,NULL,-1,0),(463,'Nam châm tinh anh',7,3,0,0,76,1300,0,0,0,0,0,0,0,0,0,0,25000,4,14007,1,5,0,-1,NULL,-1,0),(464,'Nam châm huyền thoại',7,4,0,0,81,1300,0,0,0,0,0,0,0,0,0,0,25000,4,14007,1,3,0,897,NULL,-1,0),(466,'Cuốc xịn',13,0,0,0,1,1300,5,0,0,0,0,0,0,0,0,0,5,0,14023,24,0,1440,-1,NULL,-1,0),(465,'Cuốc thường',13,0,0,0,1,1300,1,0,0,0,0,0,0,0,0,0,2,0,14022,24,0,1440,-1,NULL,-1,0),(467,'Khăn quàng chuyên gia',0,13,0,0,64,480,0,75,0,5,0,0,0,0,0,0,80000,-1,14000,25,0,0,-1,NULL,-1,0),(468,'Khăn quàng chuyên gia',0,12,0,0,64,480,0,75,0,5,0,0,0,0,0,0,80000,-1,14000,26,0,0,-1,NULL,-1,0),(469,'Khăn quàng chuyên gia',0,13,0,0,69,480,0,80,0,5,0,0,0,0,0,0,100000,-1,14000,25,0,0,-1,NULL,-1,0),(470,'Khăn quàng chuyên gia',0,12,0,0,69,480,0,80,0,5,0,0,0,0,0,0,100000,-1,14000,26,0,0,-1,NULL,-1,0),(471,'Khăn quàng tinh anh',0,13,0,0,74,480,0,85,0,5,0,0,0,0,0,0,120000,-1,14000,25,0,0,-1,NULL,-1,0),(472,'Khăn quàng tinh anh',0,12,0,0,74,480,0,85,0,5,0,0,0,0,0,0,120000,-1,14000,26,0,0,-1,NULL,-1,0),(473,'Khăn quàng tinh anh',0,13,0,0,79,480,0,90,0,5,0,0,0,0,0,0,140000,-1,14000,25,0,0,-1,NULL,-1,0),(474,'Khăn quàng tinh anh',0,12,0,0,79,480,0,90,0,5,0,0,0,0,0,0,140000,-1,14000,26,0,0,-1,NULL,-1,0),(475,'Đai chuyên gia',1,13,0,0,64,500,0,72,0,4,0,0,0,0,0,0,70000,-1,14001,51,0,0,-1,NULL,-1,0),(476,'Đai chuyên gia',1,12,0,0,64,500,0,72,0,4,0,0,0,0,0,0,70000,-1,14001,52,0,0,-1,NULL,-1,0),(477,'Đai chuyên gia',1,13,0,0,69,500,0,77,0,4,0,0,0,0,0,0,90000,-1,14001,51,0,0,-1,NULL,-1,0),(478,'Đai chuyên gia',1,12,0,0,69,500,0,77,0,4,0,0,0,0,0,0,90000,-1,14001,52,0,0,-1,NULL,-1,0),(479,'Đai tinh anh',1,13,0,0,74,500,0,82,0,4,0,0,0,0,0,0,110000,-1,14001,51,0,0,-1,NULL,-1,0),(480,'Đai tinh anh',1,12,0,0,74,500,0,82,0,4,0,0,0,0,0,0,110000,-1,14001,52,0,0,-1,NULL,-1,0),(481,'Đai tinh anh',1,13,0,0,79,500,0,87,0,4,0,0,0,0,0,0,130000,-1,14001,51,0,0,-1,NULL,-1,0),(482,'Đai tinh anh',1,12,0,0,79,500,0,87,0,4,0,0,0,0,0,0,130000,-1,14001,52,0,0,-1,NULL,-1,0),(483,'Băng đô chuyên gia',2,13,0,0,64,530,0,70,0,4,0,0,0,0,0,0,65000,-1,14002,77,-1,0,-1,NULL,-1,0),(484,'Băng đô chuyên gia',2,12,0,0,64,530,0,70,0,4,0,0,0,0,0,0,65000,-1,14002,78,-1,0,-1,NULL,-1,0),(485,'Băng đô chuyên gia',2,13,0,0,69,530,0,75,0,4,0,0,0,0,0,0,85000,-1,14002,77,-1,0,-1,NULL,-1,0),(486,'Băng đô chuyên gia',2,12,0,0,69,530,0,75,0,4,0,0,0,0,0,0,85000,-1,14002,78,-1,0,-1,NULL,-1,0),(487,'Băng đô tinh anh',2,13,0,0,74,530,0,80,0,4,0,0,0,0,0,0,110000,-1,14002,77,-1,0,-1,NULL,-1,0),(488,'Băng đô tinh anh',2,12,0,0,74,530,0,80,0,4,0,0,0,0,0,0,110000,-1,14002,78,-1,0,-1,NULL,-1,0),(489,'Băng đô tinh anh',2,13,0,0,79,530,0,85,0,4,0,0,0,0,0,0,130000,-1,14002,77,-1,0,-1,NULL,-1,0),(490,'Băng đô tinh anh',2,12,0,0,79,530,0,85,0,4,0,0,0,0,0,0,130000,-1,14002,78,-1,0,-1,NULL,-1,0),(491,'Lông vũ tốc độ chuyên gia',10,17,0,0,64,530,0,72,7,0,0,0,0,0,0,0,70000,-1,14010,17,-1,0,-1,NULL,-1,0),(492,'Lông vũ tốc độ chuyên gia',10,17,0,0,69,530,0,77,7,0,0,0,0,0,0,0,90000,-1,14010,17,-1,0,-1,NULL,-1,0),(493,'Lông vũ tốc độ tinh anh',10,17,0,0,74,530,0,82,7,0,0,0,0,0,0,0,110000,-1,14010,17,-1,0,-1,NULL,-1,0),(494,'Lông vũ tốc độ tinh anh',10,17,0,0,79,530,0,87,7,0,0,0,0,0,0,0,130000,-1,14010,17,-1,0,-1,NULL,-1,0),(495,'Găng đấm chuyên gia',11,23,0,0,64,530,0,70,0,6,5,0,0,0,0,0,65000,-1,14011,23,-1,0,-1,NULL,-1,0),(496,'Găng đấm chuyên gia',11,23,0,0,69,530,0,75,0,6,5,0,0,0,0,0,85000,-1,14011,23,-1,0,-1,NULL,-1,0),(497,'Găng đấm tinh anh',11,23,0,0,74,530,0,80,0,6,5,0,0,0,0,0,105000,-1,14011,23,-1,0,-1,NULL,-1,0),(498,'Găng đấm tinh anh',11,23,0,0,79,530,0,85,0,6,5,0,0,0,0,0,125000,-1,14011,23,-1,0,-1,NULL,-1,0),(499,'Huy hiệu chuyên gia',8,5,0,0,64,530,65,0,0,6,0,0,0,0,0,0,91000,-1,14008,5,-1,0,-1,NULL,-1,0),(500,'Huy hiệu chuyên gia',8,5,0,0,69,530,70,0,0,6,0,0,0,0,0,0,115000,-1,14008,5,-1,0,-1,NULL,-1,0),(501,'Huy hiệu tinh anh',8,5,0,0,74,530,75,0,0,6,0,0,0,0,0,0,135000,-1,14008,5,-1,0,-1,NULL,-1,0),(502,'Huy hiệu tinh anh',8,5,0,0,79,530,80,0,0,6,0,0,0,0,0,0,155000,-1,14008,5,-1,0,-1,NULL,-1,0),(503,'Vòng cổ chuyên gia',9,11,0,0,64,530,67,0,0,0,5,0,0,0,0,0,95000,-1,14009,11,-1,0,-1,NULL,-1,0),(504,'Vòng cổ chuyên gia',9,11,0,0,69,530,72,0,0,0,5,0,0,0,0,0,115000,-1,14009,11,-1,0,-1,NULL,-1,0),(505,'Vòng cổ tinh anh',9,11,0,0,74,530,77,0,0,0,5,0,0,0,0,0,135000,-1,14009,11,-1,0,-1,NULL,-1,0),(506,'Vòng cổ tinh anh',9,11,0,0,79,530,82,0,0,0,5,0,0,0,0,0,155000,-1,14009,11,-1,0,-1,NULL,-1,0),(507,'Áo Bellossom',0,38,0,0,1,5000,10,10,3000,3000,20,20,20,20,20,1,100,-1,14000,32,0,10080,-1,NULL,-1,0),(508,'Áo Lucario',0,39,0,0,1,5000,8,8,2000,2000,17,17,17,17,17,1,100,-1,14000,32,0,10080,-1,NULL,-1,0),(509,'Mũ cưỡi Poké',16,0,0,0,30,500,0,5,0,0,0,0,0,0,0,0,60000,-1,14038,0,-1,0,-1,NULL,-1,0),(510,'Mũ cưỡi Great',16,0,0,0,35,500,0,8,0,0,0,0,0,0,0,0,85000,-1,14038,0,-1,0,-1,NULL,-1,0),(511,'Mũ cưỡi Ultra',16,0,0,0,40,500,0,11,0,0,0,0,0,0,0,0,110000,-1,14038,0,-1,0,-1,NULL,-1,0),(512,'Mũ cưỡi Quick',16,0,0,0,45,500,0,13,0,0,0,0,0,0,0,0,135000,-1,14039,0,-1,0,-1,NULL,-1,0),(513,'Mũ cưỡi Dusk',16,0,0,0,50,500,0,15,0,0,0,0,0,0,0,0,160000,-1,14039,0,-1,0,-1,NULL,-1,0),(514,'Mũ cưỡi Timer',16,0,0,0,55,500,0,18,0,0,0,0,0,0,0,0,185000,-1,14039,0,-1,0,-1,NULL,-1,0),(515,'Mũ cưỡi Luxury',16,0,0,0,60,500,0,21,0,0,0,0,0,0,0,0,210000,-1,14040,0,-1,0,-1,NULL,-1,0),(516,'Mũ cưỡi Heavy',16,0,0,0,65,500,0,23,0,0,0,0,0,0,0,0,235000,-1,14040,0,-1,0,-1,NULL,-1,0),(517,'Mũ cưỡi Level',16,0,0,0,70,500,0,25,0,0,0,0,0,0,0,0,260000,-1,14040,0,-1,0,-1,NULL,-1,0),(518,'Mũ cưỡi Moon',16,0,0,0,75,500,0,28,0,0,0,0,0,0,0,0,285000,-1,14041,0,-1,0,-1,NULL,-1,0),(519,'Mũ cưỡi Dream',16,0,0,0,80,500,0,31,0,0,0,0,0,0,0,0,310000,-1,14041,0,-1,0,-1,NULL,-1,0),(520,'Mũ cưỡi Master',16,0,0,0,85,600,0,90,0,0,0,0,0,0,0,0,335000,-1,14041,0,-1,0,-1,NULL,-1,0),(521,'Giáp cưỡi Poké',14,0,0,0,30,500,0,6,0,0,0,0,0,0,0,0,70000,-1,14030,0,-1,0,-1,NULL,-1,0),(522,'Giáp cưỡi Great',14,0,0,0,35,500,0,9,0,0,0,0,0,0,0,0,95000,-1,14030,0,-1,0,-1,NULL,-1,0),(523,'Giáp cưỡi Ultra',14,0,0,0,40,500,0,12,0,0,0,0,0,0,0,0,120000,-1,14030,0,-1,0,-1,NULL,-1,0),(524,'Giáp cưỡi Quick',14,0,0,0,45,500,0,14,0,0,0,0,0,0,0,0,145000,-1,14031,0,-1,0,-1,NULL,-1,0),(525,'Giáp cưỡi Dusk',14,0,0,0,50,500,0,16,0,0,0,0,0,0,0,0,170000,-1,14031,0,-1,0,-1,NULL,-1,0),(526,'Giáp cưỡi Timer',14,0,0,0,55,500,0,19,0,0,0,0,0,0,0,0,195000,-1,14031,0,-1,0,-1,NULL,-1,0),(527,'Giáp cưỡi Luxury',14,0,0,0,60,500,0,22,0,0,0,0,0,0,0,0,220000,-1,14032,0,-1,0,-1,NULL,-1,0),(528,'Giáp cưỡi Heavy',14,0,0,0,65,500,0,24,0,0,0,0,0,0,0,0,245000,-1,14032,0,-1,0,-1,NULL,-1,0),(529,'Giáp cưỡi Level',14,0,0,0,70,500,0,26,0,0,0,0,0,0,0,0,270000,-1,14032,0,-1,0,-1,NULL,-1,0),(530,'Giáp cưỡi Moon',14,0,0,0,75,500,0,29,0,0,0,0,0,0,0,0,295000,-1,14033,0,-1,0,-1,NULL,-1,0),(531,'Giáp cưỡi Dream',14,0,0,0,80,500,0,32,0,0,0,0,0,0,0,0,320000,-1,14033,0,-1,0,-1,NULL,-1,0),(532,'Giáp cưỡi Master',14,0,0,0,85,500,0,34,0,0,0,0,0,0,0,0,345000,-1,14033,0,-1,0,-1,NULL,-1,0),(533,'Yên cương Poké',18,0,0,0,30,500,0,6,5,0,0,0,0,0,0,0,50000,-1,14046,0,-1,0,-1,NULL,-1,0),(534,'Yên cương Great',18,0,0,0,35,500,0,9,5,0,0,0,0,0,0,0,75000,-1,14046,0,-1,0,-1,NULL,-1,0),(535,'Yên cương Ultra',18,0,0,0,40,500,0,12,6,0,0,0,0,0,0,0,100000,-1,14046,0,-1,0,-1,NULL,-1,0),(536,'Yên cương Quick',18,0,0,0,45,500,0,14,6,0,0,0,0,0,0,0,125000,-1,14047,0,-1,0,-1,NULL,-1,0),(537,'Yên cương Dusk',18,0,0,0,50,500,0,16,7,0,0,0,0,0,0,0,150000,-1,14047,0,-1,0,-1,NULL,-1,0),(538,'Yên cương Timer',18,0,0,0,55,500,0,19,7,0,0,0,0,0,0,0,175000,-1,14047,0,-1,0,-1,NULL,-1,0),(539,'Yên cương Luxury',18,0,0,0,60,500,0,22,8,0,0,0,0,0,0,0,200000,-1,14048,0,-1,0,-1,NULL,-1,0),(540,'Yên cương Heavy',18,0,0,0,65,500,0,24,8,0,0,0,0,0,0,0,225000,-1,14048,0,-1,0,-1,NULL,-1,0),(541,'Yên cương Level',18,0,0,0,70,500,0,26,9,0,0,0,0,0,0,0,250000,-1,14048,0,-1,0,-1,NULL,-1,0),(542,'Yên cương Moon',18,0,0,0,75,500,0,29,9,0,0,0,0,0,0,0,275000,-1,14049,0,-1,0,-1,NULL,-1,0),(543,'Yên cương Dream',18,0,0,0,80,500,0,32,10,0,0,0,0,0,0,0,300000,-1,14049,0,-1,0,-1,NULL,-1,0),(544,'Yên cương Master',18,0,0,0,85,500,0,34,10,0,0,0,0,0,0,0,325000,-1,14049,0,-1,0,-1,NULL,-1,0),(545,'Bàn đạp Poké',17,0,0,0,30,500,5,0,0,0,3,0,0,0,0,0,55000,-1,14042,0,-1,0,-1,NULL,-1,0),(546,'Bàn đạp Great',17,0,0,0,35,500,8,0,0,0,3,0,0,0,0,0,80000,-1,14042,0,-1,0,-1,NULL,-1,0),(547,'Bàn đạp Ultra',17,0,0,0,40,500,11,0,0,0,4,0,0,0,0,0,105000,-1,14042,0,-1,0,-1,NULL,-1,0),(548,'Bàn đạp Quick',17,0,0,0,45,500,14,0,0,0,4,0,0,0,0,0,130000,-1,14043,0,-1,0,-1,NULL,-1,0),(549,'Bàn đạp Dusk',17,0,0,0,50,500,17,0,0,0,5,0,0,0,0,0,155000,-1,14043,0,-1,0,-1,NULL,-1,0),(550,'Bàn đạp Timer',17,0,0,0,55,500,20,0,0,0,5,0,0,0,0,0,180000,-1,14043,0,-1,0,-1,NULL,-1,0),(551,'Bàn đạp Luxury',17,0,0,0,60,500,23,0,0,0,6,0,0,0,0,0,205000,-1,14044,0,-1,0,-1,NULL,-1,0),(552,'Bàn đạp Heavy',17,0,0,0,65,500,26,0,0,0,6,0,0,0,0,0,230000,-1,14044,0,-1,0,-1,NULL,-1,0),(553,'Bàn đạp Level',17,0,0,0,70,500,29,0,0,0,7,0,0,0,0,0,255000,-1,14044,0,-1,0,-1,NULL,-1,0),(554,'Bàn đạp Moon',17,0,0,0,75,500,32,0,0,0,7,0,0,0,0,0,280000,-1,14045,0,-1,0,-1,NULL,-1,0),(555,'Bàn đạp Dream',17,0,0,0,80,500,35,0,0,0,8,0,0,0,0,0,305000,-1,14045,0,-1,0,-1,NULL,-1,0),(556,'Bàn đạp Master',17,0,0,0,85,500,38,0,0,0,8,0,0,0,0,0,330000,-1,14045,0,-1,0,-1,NULL,-1,0),(557,'Vòng chân Poké',15,0,0,0,30,500,6,0,0,5,0,0,0,0,0,0,65000,-1,14034,0,-1,0,-1,NULL,-1,0),(558,'Vòng chân Great',15,0,0,0,35,500,9,0,0,5,0,0,0,0,0,0,90000,-1,14034,0,-1,0,-1,NULL,-1,0),(559,'Vòng chân Ultra',15,0,0,0,40,500,12,0,0,6,0,0,0,0,0,0,115000,-1,14034,0,-1,0,-1,NULL,-1,0),(560,'Vòng chân Quick',15,0,0,0,45,500,15,0,0,6,0,0,0,0,0,0,140000,-1,14035,0,-1,0,-1,NULL,-1,0),(561,'Vòng chân Dusk',15,0,0,0,50,500,18,0,0,7,0,0,0,0,0,0,165000,-1,14035,0,-1,0,-1,NULL,-1,0),(562,'Vòng chân Timer',15,0,0,0,55,500,21,0,0,7,0,0,0,0,0,0,190000,-1,14035,0,-1,0,-1,NULL,-1,0),(563,'Vòng chân Luxury',15,0,0,0,60,500,24,0,0,8,0,0,0,0,0,0,215000,-1,14036,0,-1,0,-1,NULL,-1,0),(564,'Vòng chân Heavy',15,0,0,0,65,500,27,0,0,8,0,0,0,0,0,0,240000,-1,14036,0,-1,0,-1,NULL,-1,0),(565,'Vòng chân Level',15,0,0,0,70,500,30,0,0,9,0,0,0,0,0,0,265000,-1,14036,0,-1,0,-1,NULL,-1,0),(566,'Vòng chân Moon',15,0,0,0,75,500,33,0,0,9,0,0,0,0,0,0,290000,-1,14037,0,-1,0,-1,NULL,-1,0),(567,'Vòng chân Dream',15,0,0,0,80,500,36,0,0,10,0,0,0,0,0,0,315000,-1,14037,0,-1,0,-1,NULL,-1,0),(568,'Vòng chân Master',15,0,0,0,85,500,39,0,0,10,0,0,0,0,0,0,340000,-1,14037,0,-1,0,-1,NULL,-1,0),(571,'Phiến đá hệ tinh anh',12,30,0,0,74,480,0,0,0,8,7,13,0,0,0,0,150000,-1,14012,30,-1,0,-1,NULL,-1,0),(572,'Phiến đá hệ tinh anh',12,30,0,0,79,480,0,0,0,8,7,14,0,0,0,0,170000,-1,14012,30,-1,0,-1,NULL,-1,0),(569,'Phiến đá hệ chuyên gia',12,30,0,0,64,480,0,0,0,7,6,15,0,0,0,0,110000,-1,14012,30,-1,0,-1,NULL,-1,0),(570,'Phiến đá hệ chuyên gia',12,30,0,0,69,480,0,0,0,7,6,16,0,0,0,0,130,-1,14012,30,-1,0,-1,NULL,-1,0),(573,'Áo Darkrai nữ',0,0,0,0,0,480,10,10,3000,3000,21,25,49,49,49,1,0,-1,14000,0,-1,10080,-1,NULL,-1,0),(574,'Áo Clefairy',0,0,0,0,0,480,10,10,3000,3000,22,23,47,47,47,1,0,-1,14000,0,-1,10080,-1,NULL,-1,0),(575,'Áo Darkrai nam',0,0,0,0,0,480,10,10,3000,3000,23,24,48,48,48,1,0,-1,14000,0,-1,10080,-1,NULL,-1,0),(576,'Áo Clefable',0,0,0,0,0,480,10,10,3000,3000,24,22,46,46,46,1,0,-1,14000,0,-1,10080,-1,NULL,-1,0),(577,'Đầu Pumpkaboo',1,0,0,0,0,480,0,0,3000,3000,25,26,0,0,0,1,0,-1,14001,0,-1,10080,-1,NULL,-1,0),(578,'Nón Mismagius',2,0,0,0,0,480,0,0,3000,3000,26,0,50,0,0,1,0,-1,14002,0,-1,10080,-1,NULL,-1,0),(579,'áo noel nữ',0,0,0,0,0,480,10,10,5000,5000,16,16,0,16,16,1,0,-1,14000,0,-1,10080,-1,NULL,-1,0),(580,'áo noel nam',0,0,0,0,0,480,10,10,5000,5000,21,21,0,21,21,1,0,-1,14000,0,-1,10080,-1,NULL,-1,0),(581,'nón noel',2,0,0,0,0,480,5,5,2000,2000,22,0,16,0,0,1,0,-1,14002,0,-1,10080,-1,NULL,-1,0),(582,'áo dài nữ',0,0,0,0,0,480,10,10,3000,3000,27,0,52,51,51,1,0,-1,14000,0,-1,10080,-1,NULL,-1,0),(583,'áo dài nam',0,0,0,0,0,480,10,10,3000,3000,28,0,51,50,50,1,0,-1,14000,0,-1,10080,-1,NULL,-1,0),(584,'Ruy băng vàng',19,0,0,0,35,480,0,100,0,0,0,0,0,0,0,0,0,-1,14013,0,-1,0,-1,NULL,-1,0),(585,'Ruy băng bạch kim',19,1,0,0,40,480,0,110,0,0,0,0,0,0,0,0,0,-1,14013,0,-1,0,-1,NULL,-1,0),(586,'Ruy băng bạch kim',19,2,0,0,45,480,0,120,0,0,0,0,0,0,0,0,0,-1,14013,0,-1,0,-1,NULL,-1,0),(587,'Ruy băng kim cương',19,3,0,0,50,480,0,130,0,0,0,0,0,0,0,0,0,-1,14013,0,-1,0,-1,NULL,-1,0),(588,'Ruy băng kim cương',19,4,0,0,55,480,0,140,0,0,0,0,0,0,0,0,0,-1,14013,0,-1,0,-1,NULL,-1,0),(589,'Ruy băng chuyên gia',19,5,0,0,60,480,0,150,0,0,0,0,0,0,0,0,0,-1,14013,0,-1,0,-1,NULL,-1,0),(590,'Ruy băng chuyên gia',19,6,0,0,65,480,0,160,0,0,0,0,0,0,0,0,0,-1,14013,0,-1,0,-1,NULL,-1,0),(591,'Ruy băng tinh anh',19,7,0,0,70,480,0,170,0,0,0,0,0,0,0,0,0,-1,14013,0,-1,0,-1,NULL,-1,0),(592,'Ruy băng tinh anh',19,8,0,0,75,480,0,180,0,0,0,0,0,0,0,0,0,-1,14013,0,-1,0,-1,NULL,-1,0),(593,'Ruy băng huyền thoại',19,9,0,0,80,480,0,190,0,0,0,0,0,0,0,0,0,-1,14013,0,-1,0,-1,NULL,-1,0),(594,'Ruy băng huyền thoại',19,10,0,0,85,480,0,200,0,0,0,0,0,0,0,0,0,-1,14013,0,-1,0,-1,NULL,-1,0),(595,'Ruy băng huyền thoại',19,11,0,0,90,480,0,210,0,0,0,0,0,0,0,0,0,-1,14013,0,-1,0,-1,NULL,-1,0),(596,'Ruy băng huyền thoại',19,12,0,0,95,480,0,220,0,0,0,0,0,0,0,0,0,-1,14013,0,-1,0,-1,NULL,-1,0),(597,'Ruy băng huyền thoại',19,13,0,0,100,480,0,230,0,0,0,0,0,0,0,0,0,-1,14013,0,-1,0,-1,NULL,-1,0),(598,'Than lửa tinh anh',3,4,0,0,76,480,340,0,0,8,0,0,0,0,0,0,370000,0,14003,0,1,0,-1,NULL,-1,0),(599,'Than lửa huyền thoại',3,4,0,0,81,480,360,0,0,8,0,0,0,0,0,0,380000,0,14003,0,2,0,-1,NULL,-1,0),(600,'Hạt giống thần tinh anh',4,4,0,0,76,480,350,0,0,4,0,0,0,0,0,0,370000,1,14004,0,1,0,-1,NULL,-1,0),(601,'Hạt giống thần huyền thoại',4,4,0,0,81,480,370,0,0,4,0,0,0,0,0,0,380000,1,14004,0,2,0,-1,NULL,-1,0),(602,'Khăn lụa tinh anh',5,4,0,0,76,480,365,0,0,3,0,0,0,0,0,0,370000,2,14005,0,1,0,-1,NULL,-1,0),(603,'Khăn lụa huyền thoại',5,4,0,0,81,480,385,0,0,3,0,0,0,0,0,0,380000,2,14005,0,2,0,-1,NULL,-1,0),(604,'Nước thần tinh anh',6,4,0,0,76,480,390,0,0,3,0,0,0,0,0,0,370000,3,14006,0,1,0,-1,NULL,-1,0),(605,'Nước thần huyền thoại',6,4,0,0,81,480,401,0,0,3,0,0,0,0,0,0,380000,3,14006,0,2,0,-1,NULL,-1,0),(606,'Nam châm tinh anh',7,4,0,0,76,480,300,0,0,12,0,0,0,0,0,0,370000,4,14007,0,1,0,-1,NULL,-1,0),(607,'Nam châm huyền thoại',7,4,0,0,81,480,320,0,0,12,0,0,0,0,0,0,380000,4,14007,0,2,0,-1,NULL,-1,0),(608,'Phi phong world cup',19,14,0,0,1,480,0,170,0,0,0,0,0,0,0,0,0,-1,14013,0,-1,0,-1,NULL,-1,0),(609,'áo brazin nam,tăng100% băng lan, sét lan, độc lan',0,52,0,0,1,480,10,10,3000,3000,0,0,0,52,52,1,0,-1,14000,0,-1,10080,-1,NULL,-1,0),(610,'áo brazin nữ,tăng100% băng lan, sét lan, độc lan',0,53,0,0,1,480,10,10,3000,3000,0,0,0,53,53,1,0,-1,14000,0,-1,10080,-1,NULL,-1,0),(611,'Rương giao hàng',20,3,0,0,1,480,10,10,3000,3000,0,0,0,0,0,0,0,-1,14050,0,-1,0,-1,NULL,-1,0),(612,'Rương cướp hàng',20,3,0,0,1,480,10,10,3000,3000,0,0,0,0,0,0,0,-1,14051,0,-1,0,-1,NULL,-1,0),(613,'Chổi băng ánh',13,0,0,0,1,480,0,0,0,0,1,0,0,0,10,2,0,0,14025,0,-1,10080,-1,NULL,-1,0),(614,'Chổi điện ánh',13,0,0,0,1,480,0,0,0,0,1,0,0,0,9,2,0,0,14024,0,-1,10080,-1,NULL,-1,0),(615,'Chổi độc ánh',13,0,0,0,1,480,0,0,0,0,1,0,0,0,11,2,0,0,14026,0,-1,10080,-1,NULL,-1,0),(616,'Phi phong lông thú',19,15,0,0,1,480,0,170,0,0,0,0,0,0,0,0,0,-1,14013,0,-1,10080,-1,NULL,-1,0),(617,'Chổi băng',13,0,0,0,1,480,0,0,0,0,1,0,0,0,12,2,0,0,14027,0,-1,0,-1,NULL,-1,0),(618,'Chổi set',13,0,0,0,1,480,0,0,0,0,1,0,0,0,13,2,0,0,14028,0,-1,0,-1,NULL,-1,0),(619,'Chổi độc',13,0,0,0,1,480,0,0,0,0,1,0,0,0,14,2,0,0,14029,0,-1,0,-1,NULL,-1,0),(620,'Thời trang băng tuyết(nam)\\ntăng100% băng ,sét ,độc ,lửa lan',0,55,0,0,1,480,10,10,5000,5000,55,0,57,55,55,1,0,-1,14000,0,-1,0,-1,NULL,-1,0),(621,'Thời trang băng tuyết\\ntăng100% băng ,sét ,độc ,lửa lan',0,56,0,0,1,480,10,10,5000,5000,56,19,58,56,56,1,0,-1,14000,0,-1,0,-1,NULL,-1,0),(622,'Áo Magby nam \\ntăng 100% băng ,sét ,độc ,lửa lan',0,63,0,0,1,480,10,10,5000,5000,63,70,0,63,63,1,0,-1,14000,0,-1,10080,-1,NULL,-1,0),(623,'Áo Magby nữ \\ntăng 100% băng ,sét ,độc ,lửa lan',0,64,0,0,1,480,10,10,5000,5000,64,71,0,64,64,1,0,-1,14000,0,-1,10080,-1,NULL,-1,0),(624,'Học trò cấp 1(nam)',0,59,0,0,1,480,10,10,5000,5000,59,0,0,59,59,1,0,-1,14000,0,-1,0,-1,NULL,-1,0),(625,'Học trò cấp 2(nam)',0,58,0,0,1,480,10,10,5000,5000,58,0,0,58,58,1,0,-1,14000,0,-1,0,-1,NULL,-1,0),(626,'Học trò cấp 3(nam)',0,57,0,0,1,480,10,10,5000,5000,57,0,0,57,57,1,0,-1,14000,0,-1,0,-1,NULL,-1,0),(627,'Học trò cấp 1(nữ)',0,62,0,0,1,480,10,10,5000,5000,62,0,0,62,62,1,0,-1,14000,0,-1,0,-1,NULL,-1,0),(628,'Học trò cấp 2(nữ)',0,61,0,0,1,480,10,10,5000,5000,61,0,0,61,61,1,0,-1,14000,0,-1,0,-1,NULL,-1,0),(629,'Học trò cấp 3(nữ)',0,60,0,0,1,480,10,10,5000,5000,60,0,0,60,60,1,0,-1,14000,0,-1,0,-1,NULL,-1,0),(630,'Vé gọi Pokémon hỗ trợ 4x lv 1',21,0,0,0,45,480,0,0,0,0,0,0,0,0,0,0,15,-1,14052,0,-1,10080,-1,NULL,-1,0),(631,'Vé gọi Pokémon hỗ trợ 4x lv 2',21,0,0,0,45,480,1,0,0,0,0,0,0,0,0,0,20,-1,14052,0,-1,10080,-1,NULL,-1,0),(632,'Vé gọi Pokémon hỗ trợ 4x lv 3',21,0,0,0,45,480,2,0,0,0,0,0,0,0,0,0,25,-1,14052,0,-1,10080,-1,NULL,-1,0),(633,'Vé gọi Pokémon hỗ trợ 5x lv 1',21,0,0,0,55,480,0,0,0,0,0,0,0,0,0,0,30,-1,14053,0,-1,10080,-1,NULL,-1,0),(634,'Vé gọi Pokémon hỗ trợ 5x lv 2',21,0,0,0,55,480,1,0,0,0,0,0,0,0,0,0,35,-1,14053,0,-1,10080,-1,NULL,-1,0),(635,'Vé gọi Pokémon hỗ trợ 5x lv 3',21,0,0,0,55,480,2,0,0,0,0,0,0,0,0,0,40,-1,14053,0,-1,10080,-1,NULL,-1,0),(636,'Vé gọi Pokémon hỗ trợ 6x lv 1',21,0,0,0,65,480,0,0,0,0,0,0,0,0,0,0,45,-1,14054,0,-1,10080,-1,NULL,-1,0),(637,'Vé gọi Pokémon hỗ trợ 6x lv 2',21,0,0,0,65,480,1,0,0,0,0,0,0,0,0,0,50,-1,14054,0,-1,10080,-1,NULL,-1,0),(638,'Vé gọi Pokémon hỗ trợ 6x lv 3',21,0,0,0,65,480,2,0,0,0,0,0,0,0,0,0,55,-1,14054,0,-1,10080,-1,NULL,-1,0),(639,'Khăn quàng huyền thoại',0,65,0,0,80,480,0,0,0,0,0,0,0,0,0,0,25000,-1,14000,0,-1,0,641,NULL,-1,0),(640,'Khăn quàng huyền thoại',0,66,0,0,80,480,0,0,0,0,0,0,0,0,0,0,25000,-1,14000,0,-1,0,642,NULL,-1,0),(641,'Khăn quàng huyền thoại',0,67,0,0,85,480,0,95,0,5,0,0,0,0,0,0,25000,-1,14000,25,-1,0,-1,NULL,-1,0),(642,'Khăn quàng huyền thoại',0,68,0,0,85,480,0,95,0,5,0,0,0,0,0,0,25000,-1,14000,26,-1,0,-1,NULL,-1,0),(643,'Đai huyền thoại',1,65,0,0,80,480,0,0,0,0,0,0,0,0,0,0,25000,-1,14001,0,-1,0,645,NULL,-1,0),(644,'Đai huyền thoại',1,66,0,0,80,480,0,0,0,0,0,0,0,0,0,0,25000,-1,14001,0,-1,0,646,NULL,-1,0),(645,'Đai huyền thoại',1,67,0,0,85,480,0,0,0,0,0,0,0,0,0,0,25000,-1,14001,0,-1,0,-1,NULL,-1,0),(646,'Đai huyền thoại',1,68,0,0,85,600,0,90,0,4,0,0,0,0,0,0,25000,-1,14001,51,0,0,-1,NULL,-1,0),(647,'Băng đô huyền thoại',2,59,0,0,80,480,0,0,0,0,0,0,0,0,0,0,25000,-1,14002,0,-1,0,649,NULL,-1,0),(648,'Băng đô huyền thoại',2,60,0,0,80,480,0,0,0,0,0,0,0,0,0,0,25000,-1,14002,0,-1,0,650,NULL,-1,0),(649,'Băng đô huyền thoại',2,61,0,0,85,600,0,90,0,4,0,0,0,0,0,0,25000,-1,14002,52,0,0,-1,NULL,-1,0),(650,'Băng đô huyền thoại',2,62,0,0,85,480,0,0,0,0,0,0,0,0,0,0,25000,-1,14002,0,-1,0,-1,NULL,-1,0),(651,'Huy hiệu huyền thoại',8,30,0,0,80,480,0,0,0,0,0,0,0,0,0,0,25000,-1,14008,0,-1,0,652,NULL,-1,0),(652,'Huy hiệu huyền thoại',8,30,0,0,85,480,85,0,0,6,0,0,0,0,0,0,25000,-1,14008,5,-1,0,653,NULL,-1,0),(653,'Huy hiệu huyền thoại',8,30,0,0,90,480,0,0,0,0,0,0,0,0,0,0,25000,-1,14008,0,-1,0,654,NULL,-1,0),(654,'Huy hiệu huyền thoại',8,30,0,0,95,480,0,0,0,0,0,0,0,0,0,0,25000,-1,14008,0,-1,0,-1,NULL,-1,0),(655,'Vòng cổ huyền thoại',9,30,0,0,80,480,0,0,0,0,0,0,0,0,0,0,25000,-1,14009,0,-1,0,656,NULL,-1,0),(656,'Vòng cổ huyền thoại',9,30,0,0,85,480,85,0,0,0,5,0,0,0,0,0,25000,-1,14009,11,-1,0,657,NULL,-1,0),(657,'Vòng cổ huyền thoại',9,30,0,0,90,480,0,0,0,0,0,0,0,0,0,0,25000,-1,14009,0,-1,0,658,NULL,-1,0),(658,'Vòng cổ huyền thoại',9,30,0,0,95,480,0,0,0,0,0,0,0,0,0,0,25000,-1,14009,0,-1,0,-1,NULL,-1,0),(659,'Lông vũ tốc độ huyền thoại',10,30,0,0,80,480,0,0,0,0,0,0,0,0,0,0,25000,-1,14010,0,-1,0,660,NULL,-1,0),(660,'Lông vũ tốc độ huyền thoại',10,30,0,0,85,480,0,87,7,0,0,0,0,0,0,0,25000,-1,14010,17,-1,0,661,NULL,-1,0),(661,'Lông vũ tốc độ huyền thoại',10,30,0,0,90,480,0,0,0,0,0,0,0,0,0,0,25000,-1,14010,0,-1,0,662,NULL,-1,0),(662,'Lông vũ tốc độ huyền thoại',10,30,0,0,95,480,0,0,0,0,0,0,0,0,0,0,25000,-1,14010,0,-1,0,-1,NULL,-1,0),(663,'Găng đấm huyền thoại',11,30,0,0,80,480,0,0,0,0,0,0,0,0,0,0,25000,-1,14011,0,-1,0,664,NULL,-1,0),(664,'Găng đấm huyền thoại',11,30,0,0,85,480,0,90,0,6,5,0,0,0,0,0,25000,-1,14011,23,-1,0,665,NULL,-1,0),(665,'Găng đấm huyền thoại',11,30,0,0,90,480,0,0,0,0,0,0,0,0,0,0,25000,-1,14011,0,-1,0,666,NULL,-1,0),(666,'Găng đấm huyền thoại',11,30,0,0,95,480,0,0,0,0,0,0,0,0,0,0,25000,-1,14011,0,-1,0,-1,NULL,-1,0),(667,'Phiến đá hệ huyền thoại',12,30,0,0,80,480,0,0,0,0,0,0,0,0,0,0,25000,-1,14012,0,-1,0,668,NULL,-1,0),(668,'Phiến đá hệ huyền thoại',12,30,0,0,85,480,0,0,0,8,7,15,0,0,0,0,25000,-1,14012,63,-1,0,669,NULL,-1,0),(669,'Phiến đá hệ huyền thoại',12,30,0,0,90,480,0,0,0,0,0,0,0,0,0,0,25000,-1,14012,0,-1,0,670,NULL,-1,0),(670,'Phiến đá hệ huyền thoại',12,30,0,0,95,480,0,0,0,0,0,0,0,0,0,0,25000,-1,14012,0,-1,0,-1,NULL,-1,0),(671,'Áo Banette\r\nTăng100% băng ,sét ,độc ,lửa lan',0,69,0,0,1,480,10,10,5000,5000,69,72,63,69,69,1,0,0,14000,0,-1,10080,-1,NULL,-1,0),(672,'Áo Dusknoir\r\nTăng100% băng ,sét ,độc ,lửa lan',0,70,0,0,1,480,10,10,5000,5000,70,74,0,71,71,1,0,0,14000,0,-1,10080,-1,NULL,-1,0),(673,'Áo Alakazam\r\nTăng100% băng ,sét ,độc ,lửa lan',0,71,0,0,1,480,10,10,5000,5000,71,73,0,70,70,1,0,0,14000,0,-1,10080,-1,NULL,-1,0),(674,'Áo Giratina\r\nTăng100% băng ,sét ,độc ,lửa lan',0,72,0,0,1,480,10,10,5000,5000,72,75,0,72,72,1,0,0,14000,0,-1,10080,-1,NULL,-1,0),(675,'Thẻ mua bán',22,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,10,-1,14055,0,-1,10080,-1,NULL,-1,0),(676,'Vé xe giao hàng 2x',23,0,0,0,40,1,0,0,0,0,0,0,0,0,0,1,10,-1,14056,0,-1,0,-1,NULL,-1,0),(677,'Vé xe giao hàng 3x',23,0,0,0,40,1,0,0,0,0,0,0,0,0,0,1,10,-1,14057,0,-1,0,-1,NULL,-1,0),(678,'Vé Tháp Pokémon 5x',24,0,0,0,40,1,0,0,0,0,0,0,0,0,0,1,10,-1,14058,0,-1,0,-1,NULL,-1,0),(679,'Vé Tháp Pokémon 6x',24,0,0,0,40,1,0,0,0,0,0,0,0,0,0,1,10,-1,14059,0,-1,0,-1,NULL,-1,0),(680,'Vé Tháp Pokémon 7x',24,0,0,0,40,1,0,0,0,0,0,0,0,0,0,1,10,-1,14060,0,-1,0,-1,NULL,-1,0),(681,'Ponyta lửa',25,0,0,0,40,1,1,0,0,0,0,0,0,0,0,0,10,-1,14062,0,-1,0,682,NULL,-1,0),(682,'Rapidash lửa',25,0,0,0,40,1,2,0,0,0,0,0,0,0,0,0,0,-1,14063,0,-1,0,683,NULL,-1,0),(683,'Rapidash lửa Shiny',25,0,0,0,40,1,3,0,0,0,0,0,0,0,0,0,0,-1,14064,0,-1,0,-1,NULL,-1,0),(684,'Trứng Hoothoot',26,0,0,0,1,0,1,23,0,0,0,0,0,0,0,0,0,-1,14096,0,-1,0,689,NULL,-1,0),(685,'Trứng Dratini',26,0,0,0,1,0,1,24,0,0,0,0,0,0,0,0,0,-1,14097,0,-1,0,694,NULL,-1,0),(686,'Trứng Cleffa',26,0,0,0,1,0,1,25,0,0,0,0,0,0,0,0,0,-1,14098,0,-1,0,699,NULL,-1,0),(687,'Trứng Zubat',26,0,0,0,1,0,1,26,0,0,0,0,0,0,0,0,0,-1,14099,0,-1,0,704,NULL,-1,0),(688,'Trứng Rufflet',26,0,0,0,1,0,1,27,0,0,0,0,0,0,0,0,0,-1,14100,0,-1,0,709,NULL,-1,0),(689,'Hoothoot cấp 1',27,0,0,0,0,0,1,24,0,0,0,0,0,0,0,0,0,-1,14065,0,-1,0,690,NULL,-1,0),(690,'Hoothoot cấp 2',27,0,0,0,0,0,2,24,0,0,0,0,0,0,0,0,0,-1,14065,0,-1,0,691,NULL,-1,0),(691,'Noctowl cấp 3',27,0,0,0,0,0,3,24,0,0,0,0,0,0,0,0,0,-1,14066,0,-1,0,692,NULL,-1,0),(692,'Noctowl cấp 4',27,0,0,0,0,0,4,24,0,0,0,0,0,0,0,0,0,-1,14066,0,-1,0,693,NULL,-1,0),(693,'Noctowl Shiny cấp 5',27,0,0,0,0,0,5,24,0,0,0,0,0,0,0,0,0,-1,14067,0,-1,0,-1,NULL,-1,0),(694,'Dratini cấp 1',27,0,0,0,0,0,1,25,0,0,0,0,0,0,0,0,0,-1,14068,0,-1,0,695,NULL,-1,0),(695,'Dratini cấp 2',27,0,0,0,0,0,2,25,0,0,0,0,0,0,0,0,0,-1,14068,0,-1,0,696,NULL,-1,0),(696,'Dragonair cấp 3',27,0,0,0,0,0,3,25,0,0,0,0,0,0,0,0,0,-1,14069,0,-1,0,697,NULL,-1,0),(697,'Dragonair cấp 4',27,0,0,0,0,0,4,25,0,0,0,0,0,0,0,0,0,-1,14069,0,-1,0,698,NULL,-1,0),(698,'Dragonite cấp 5',27,0,0,0,0,0,5,25,0,0,0,0,0,0,0,0,0,-1,14070,0,-1,0,-1,NULL,-1,0),(699,'Cleffa cấp 1',27,0,0,0,0,0,1,26,0,0,0,0,0,0,0,0,0,-1,14071,0,-1,0,700,NULL,-1,0),(700,'Clefairy cấp 2',27,0,0,0,0,0,2,26,0,0,0,0,0,0,0,0,0,-1,14072,0,-1,0,701,NULL,-1,0),(701,'Clefairy cấp 3',27,0,0,0,0,0,3,26,0,0,0,0,0,0,0,0,0,-1,14072,0,-1,0,702,NULL,-1,0),(702,'Clefable cấp 4',27,0,0,0,0,0,4,26,0,0,0,0,0,0,0,0,0,-1,14073,0,-1,0,703,NULL,-1,0),(703,'Clefable Shiny cấp 5',27,0,0,0,0,0,5,26,0,0,0,0,0,0,0,0,0,-1,14074,0,-1,0,-1,NULL,-1,0),(704,'Zubat cấp 1',27,0,0,0,0,0,1,27,0,0,0,0,0,0,0,0,0,-1,14075,0,-1,0,705,NULL,-1,0),(705,'Zubat cấp 2',27,0,0,0,0,0,2,27,0,0,0,0,0,0,0,0,0,-1,14075,0,-1,0,706,NULL,-1,0),(706,'Golbat cấp 3',27,0,0,0,0,0,3,27,0,0,0,0,0,0,0,0,0,-1,14076,0,-1,0,707,NULL,-1,0),(707,'Golbat cấp 4',27,0,0,0,0,0,4,27,0,0,0,0,0,0,0,0,0,-1,14076,0,-1,0,708,NULL,-1,0),(708,'Crobat cấp 5',27,0,0,0,0,0,5,27,0,0,0,0,0,0,0,0,0,-1,14077,0,-1,0,-1,NULL,-1,0),(709,'Rufflet cấp 1',27,0,0,0,0,0,1,28,0,0,0,0,0,0,0,0,0,-1,14078,0,-1,0,710,NULL,-1,0),(710,'Rufflet cấp 2',27,0,0,0,0,0,2,28,0,0,0,0,0,0,0,0,0,-1,14078,0,-1,0,711,NULL,-1,0),(711,'Braviary cấp 3',27,0,0,0,0,0,3,28,0,0,0,0,0,0,0,0,0,-1,14079,0,-1,0,712,NULL,-1,0),(712,'Braviary cấp 4',27,0,0,0,0,0,4,28,0,0,0,0,0,0,0,0,0,-1,14079,0,-1,0,713,NULL,-1,0),(713,'Braviary Shiny cấp 5',27,0,0,0,0,0,5,28,0,0,0,0,0,0,0,0,0,-1,14080,0,-1,0,-1,NULL,-1,0),(714,'Kẹo Poké',28,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-1,14108,0,-1,0,-1,NULL,-1,0),(715,'Than lửa bạch kim',3,4,0,0,40,0,0,0,0,0,0,0,0,0,0,0,0,0,14003,0,6,0,-1,NULL,-1,0),(716,'Hạt giống thần bạch kim',4,4,0,0,40,0,0,0,0,0,0,0,0,0,0,0,0,1,14004,0,6,0,-1,NULL,-1,0),(717,'Khăn lụa bạch kim',5,4,0,0,40,0,0,0,0,0,0,0,0,0,0,0,0,2,14005,0,6,0,-1,NULL,-1,0),(718,'Nước thần bạch kim',6,4,0,0,40,0,0,0,0,0,0,0,0,0,0,0,0,3,14006,0,6,0,-1,NULL,-1,0),(719,'Nam châm bạch kim',7,4,0,0,40,0,0,0,0,0,0,0,0,0,0,0,0,4,14007,0,6,0,-1,NULL,-1,0),(720,'Kẹo hiếm.\r\nTiến hoá trạng thái thú nuôi',28,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,14109,0,-1,0,-1,NULL,-1,0),(721,'Bạc hà tính cách\r\nThay đổi độ thông thạo của thú nuôi',28,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,14110,0,-1,0,-1,NULL,-1,0),(950,'Skin Copperajah (hệ Thép)',19,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-1,14013,0,-1,0,-1,NULL,-1,0),(723,'Áo Zoroark\n- Tăng 50% băng ,sét ,độc ,lửa lan',0,73,0,0,1,480,10,0,5000,5000,73,76,0,73,73,1,0,-1,14000,0,-1,10080,-1,NULL,8754,0),(724,'Áo Gardevoir\n- Tăng 50% băng ,sét ,độc ,lửa lan',0,74,0,0,1,480,10,0,5000,5000,74,77,64,74,74,1,0,-1,14000,0,-1,10080,-1,NULL,8887,0),(725,'Áo Absol\n- Tăng 50% băng ,sét ,độc ,lửa lan',0,75,0,0,1,480,10,0,5000,5000,75,78,0,75,75,1,0,-1,14000,0,-1,10080,-1,NULL,8758,0),(726,'Áo Oricorio\n- Tăng 50% băng ,sét ,độc ,lửa lan',0,76,0,0,1,480,10,0,5000,5000,76,79,0,76,76,1,0,-1,14000,0,-1,10080,-1,NULL,8759,0),(727,'Pachirisu',27,0,0,0,5,0,5,60,0,0,0,0,0,0,0,0,0,0,14081,0,-1,10080,-1,NULL,-1,0),(728,'Delibird',27,0,0,0,10,0,0,0,0,0,0,0,0,0,0,0,0,0,14082,587,-1,0,-1,NULL,8757,0),(729,'Chổi lửa',28,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,14111,588,-1,0,-1,NULL,8766,0),(730,'Đồng xu bạc\r\nDùng đổi Suicune vĩnh viễn',28,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,14113,0,-1,0,-1,NULL,-1,0),(731,'Đồng xu xanh\r\nDùng đổi trang bị thời trang vĩnh viễn',28,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,14114,0,-1,0,-1,NULL,-1,0),(732,'Đồng xu vàng\r\nDùng đổi pet Sandshrew vĩnh viễn',28,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,14112,0,-1,0,-1,NULL,-1,0),(733,'Đồng xu đỏ\r\nDùng đổi Lotad vĩnh viễn',28,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,14115,0,-1,0,-1,NULL,-1,0),(734,'Đồng xu xanh lá\r\nDùng đổi chổi lửa lan vĩnh viễn',28,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,14116,0,-1,0,-1,NULL,-1,0),(735,'Rương may mắn',28,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,14117,0,-1,15,-1,NULL,-1,0),(736,'Áo Groudon\r\nTăng 50% băng ,sét ,độc ,lửa lan',0,77,-1,0,1,480,10,0,5000,5000,77,80,65,77,77,1,0,-1,14000,0,-1,10080,-1,NULL,8782,0),(737,'Áo Kyogre\r\nTăng 50% băng ,sét ,độc ,lửa lan',0,78,-1,0,1,480,10,0,5000,5000,78,81,66,78,78,1,0,-1,14000,0,-1,10080,-1,NULL,8781,0),(738,'Rương kho báu',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,14120,0,-1,0,-1,NULL,-1,0),(739,'Thuốc hồi sinh\r\nHồi sinh sau khi chết',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,14121,0,-1,0,-1,NULL,-1,0),(740,'Áo Latias\r\nTăng 50% băng ,sét ,độc ,lửa lan',0,80,-1,0,1,480,10,0,5000,5000,80,83,0,80,80,1,0,-1,14000,0,-1,10080,-1,NULL,-1,0),(741,'Áo Latios\r\nTăng 50% băng ,sét ,độc ,lửa lan',0,79,-1,0,1,480,10,0,5000,5000,79,82,0,79,79,1,0,-1,14000,0,-1,10080,-1,NULL,-1,0),(742,'Phi phong lông công',19,15,0,0,1,480,0,170,0,0,0,0,0,0,0,0,0,-1,14013,0,-1,10080,-1,'',8791,0),(743,'Áo Lunala\r\nTăng 50% băng ,sét ,độc ,lửa lan',0,0,-1,0,1,480,10,10,5000,5000,0,0,0,0,0,1,0,-1,14000,0,-1,10080,-1,NULL,-1,0),(744,'Áo Solgaleo\r\nTăng 50% băng ,sét ,độc ,lửa lan',0,0,-1,0,1,480,10,10,5000,5000,0,0,0,0,0,1,0,-1,14000,0,-1,10080,-1,NULL,-1,0),(745,'Mặt nạ Pumpkaboo',1,0,0,0,1,480,0,0,0,0,0,0,0,0,0,1,0,-1,14001,0,-1,10080,-1,NULL,8800,0),(746,'Mặt nạ Haunter',1,0,0,0,1,480,0,0,0,0,0,0,0,0,0,1,0,-1,14001,0,-1,10080,-1,NULL,8801,0),(747,'Mặt nạ Houndoom',1,0,0,0,1,480,0,0,0,0,0,0,0,0,0,1,0,-1,14001,0,-1,10080,-1,NULL,8802,0),(748,'Mặt nạ Yamask',1,0,0,0,1,480,0,0,0,0,0,0,0,0,0,1,0,-1,14001,0,-1,10080,-1,NULL,8803,0),(749,'Mặt nạ Gengar',1,0,0,0,1,480,0,0,0,0,0,0,0,0,0,1,0,-1,14001,0,-1,10080,-1,NULL,8804,0),(750,'Phi Phong Hoenn',19,15,0,0,1,480,0,170,0,0,0,0,0,0,0,0,0,-1,14013,0,-1,10080,-1,'',8806,0),(751,'Vảy trái tim',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,14122,0,-1,0,-1,'',-1,0),(752,'Rương nguyên liệu cao cấp',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,14118,0,-1,0,-1,'',-1,0),(753,'Rương nguyên liệu sơ cấp',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,14119,0,-1,0,-1,'',-1,0),(754,'Áo dài nam \nTăng 100% băng ,sét ,độc ,lửa lan',0,0,0,0,1,480,10,10,5000,5000,0,0,0,0,0,1,0,-1,14000,0,-1,10080,-1,'',-1,0),(755,'Áo dài nữ \nTăng 100% băng ,sét ,độc ,lửa lan',0,0,0,0,1,480,10,10,5000,5000,0,0,0,0,0,1,0,-1,14000,0,-1,10080,-1,'',-1,0),(790,'Voltorb vàng',27,0,0,0,5,0,5,159,0,0,0,0,159,0,0,0,0,0,14083,0,-1,10080,-1,'',-1,0),(791,'Bóng đá nam\r\ntăng 100% băng ,sét ,độc ,lửa lan\r\nrơi thẻ khi hạ quái và x2 tl rơi bóng bạc',0,0,0,0,1,480,10,10,5000,5000,0,0,0,0,0,1,0,-1,14000,0,-1,10080,-1,'',-1,0),(789,'Bóng đá nữ\r\ntăng 100% băng ,sét ,độc ,lửa lan',0,0,0,0,1,480,10,10,5000,5000,0,0,0,0,0,1,0,-1,14000,0,-1,10080,-1,'',-1,0),(788,'Bóng đá nam\r\ntăng 100% băng ,sét ,độc ,lửa lan',0,0,0,0,1,480,10,10,5000,5000,0,0,0,0,0,1,0,-1,14000,0,-1,10080,-1,'',-1,0),(787,'Nam châm huyền thoại',7,4,0,0,80,1500,0,0,0,0,0,0,0,0,0,0,50000,4,14007,668,6,0,-1,'',138,0),(786,'Nước thần huyền thoại',6,4,0,0,80,1500,0,0,0,0,0,0,0,0,0,0,50000,3,14006,644,6,0,-1,'',126,0),(785,'Khăn lụa huyền thoại',5,4,0,0,80,1500,0,0,0,0,0,0,0,0,0,0,50000,2,14005,656,6,0,-1,'',132,0),(784,'Hạt giống thần huyền thoại',4,4,0,0,80,1500,0,0,0,0,0,0,0,0,0,0,50000,1,14004,679,6,0,-1,'',145,0),(783,'Than lửa huyền thoại',3,5,0,0,80,1500,0,0,0,0,0,0,0,0,0,0,50000,0,14003,692,2,0,-1,'',151,0),(782,'Nam châm tinh anh',7,4,0,0,70,1500,0,0,0,0,0,0,0,0,0,0,50000,4,14007,664,6,0,-1,'',136,0),(781,'Nước thần tinh anh',6,4,0,0,70,1500,0,0,0,0,0,0,0,0,0,0,50000,3,14006,640,6,0,-1,'',124,0),(780,'Khăn lụa tinh anh',5,4,0,0,70,1500,0,0,0,0,0,0,0,0,0,0,50000,2,14005,652,6,0,-1,'',130,0),(779,'Hạt giống thần tinh anh',4,4,0,0,70,1500,0,0,0,0,0,0,0,0,0,0,50000,1,14004,675,6,0,-1,'',142,0),(778,'Than lửa tinh anh',3,5,0,0,70,1500,0,0,0,0,0,0,0,0,0,0,50000,0,14003,688,2,0,-1,'',149,0),(777,'Nam châm chuyên gia',7,4,0,0,60,1500,0,0,0,0,0,0,0,0,0,0,50000,4,14007,660,6,0,-1,'',134,0),(776,'Nước thần chuyên gia',6,4,0,0,60,1500,0,0,0,0,0,0,0,0,0,0,50000,3,14006,636,6,0,-1,'',122,0),(775,'Khăn lụa chuyên gia',5,4,0,0,60,1500,0,0,0,0,0,0,0,0,0,0,50000,2,14005,648,6,0,-1,'',128,0),(774,'Hạt giống thần chuyên gia',4,4,0,0,60,1500,0,0,0,0,0,0,0,0,0,0,50000,1,14004,671,6,0,-1,'',142,0),(773,'Than lửa chuyên gia',3,5,0,0,60,1500,0,0,0,0,0,0,0,0,0,0,50000,0,14003,684,2,0,-1,'',147,0),(772,'Nam châm huyền thoại',7,4,0,0,80,0,0,0,0,0,0,0,0,0,0,0,0,4,14007,670,6,0,-1,'',139,0),(771,'Nước thần huyền thoại',6,4,0,0,80,0,0,0,0,0,0,0,0,0,0,0,0,3,14006,646,6,0,-1,'',127,0),(770,'Khăn lụa huyền thoại',5,4,0,0,80,0,0,0,0,0,0,0,0,0,0,0,0,2,14005,658,6,0,-1,'',133,0),(769,'Hạt giống thần huyền thoại',4,4,0,0,80,0,0,0,0,0,0,0,0,0,0,0,0,1,14004,680,6,0,-1,'',146,0),(768,'Than lửa huyền thoại',3,5,0,0,80,0,0,0,0,0,0,0,0,0,0,0,0,0,14003,694,2,0,-1,'',152,0),(767,'Nam châm tinh anh',7,4,0,0,70,0,0,0,0,0,0,0,0,0,0,0,0,4,14007,666,6,0,-1,'',137,0),(766,'Nước thần tinh anh',6,4,0,0,70,0,0,0,0,0,0,0,0,0,0,0,0,3,14006,652,6,0,-1,'',125,0),(764,'Hạt giống thần tinh anh',4,4,0,0,70,0,0,0,0,0,0,0,0,0,0,0,0,1,14004,676,6,0,-1,'',143,0),(765,'Khăn lụa tinh anh',5,4,0,0,70,0,0,0,0,0,0,0,0,0,0,0,0,2,14005,654,6,0,-1,'',133,0),(763,'Than lửa tinh anh',3,5,0,0,70,0,0,0,0,0,0,0,0,0,0,0,0,0,14003,690,2,0,-1,'',150,0),(761,'Nước thần chuyên gia',6,4,0,0,60,0,0,0,0,0,0,0,0,0,0,0,0,3,14006,638,6,0,-1,'',123,0),(762,'Nam châm chuyên gia',7,4,0,0,60,0,0,0,0,0,0,0,0,0,0,0,0,4,14007,660,6,0,-1,'',134,0),(759,'Hạt giống thần chuyên gia',4,4,0,0,60,0,0,0,0,0,0,0,0,0,0,0,0,1,14004,672,6,0,-1,'',143,0),(760,'Khăn lụa chuyên gia',5,4,0,0,60,0,0,0,0,0,0,0,0,0,0,0,0,2,14005,650,6,0,-1,'',129,0),(758,'Than lửa chuyên gia',3,5,0,0,60,0,0,0,0,0,0,0,0,0,0,0,0,0,14003,686,2,0,-1,'',148,0),(757,'Áo Ho-Oh\r\nTăng 50% băng ,sét ,độc ,lửa lan\r\nCòn 1hp khi bị quái cắn hoặc bị PK',0,0,0,0,1,480,10,10,5000,5000,0,0,0,0,0,1,0,-1,14000,0,-1,10080,-1,'',-1,0),(792,'Bóng đá nữ\r\ntăng 100% băng ,sét ,độc ,lửa lan\r\nrơi thẻ khi hạ quái và x2 tl rơi bóng bạc',0,0,0,0,1,480,10,10,5000,5000,0,0,0,0,0,1,0,-1,14000,0,-1,10080,-1,'',-1,0),(793,'Thẻ vàng',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,14124,0,-1,0,-1,'',-1,0),(794,'Thẻ đỏ',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,14123,0,-1,0,-1,'',-1,0),(795,'Phi Phong bóng đá',19,15,0,0,1,480,0,170,0,0,0,0,170,0,0,0,0,-1,14013,0,-1,10080,-1,'',-1,0),(796,'Gậy Ánh Trăng',5,0,0,0,10,0,0,0,0,0,0,0,0,0,0,0,0,-1,14005,715,2,0,-1,'',170,0),(797,'Áo Milotic\r\n- Tăng 50% băng ,sét ,độc ,lửa lan\r\n- Quyền năng ma thuật:\r\n. Tỷ lệ xh: 5%\r\n. Gây st 1%-10% hp\r\n- Đòn Đánh Lén:\r\n. Gây st 1%-5% hp\r\n. Hồi 1%-10%hp',0,0,-1,0,1,480,10,10,5000,5000,0,0,0,0,0,1,0,-1,14000,0,-1,10080,-1,'',8886,0),(798,'Áo Minun\r\n- Tăng 50% băng ,sét ,độc ,lửa lan\r\n- Quyền năng ma thuật:\r\n. Tỷ lệ xh: 5%\r\n. Gây st 1%-10% hp\r\n- Đòn Đánh Lén:\r\n. Gây st 1%-5% hp\r\n. Hồi 1%-10%hp',0,0,-1,0,1,480,10,10,5000,5000,0,0,0,0,0,1,0,-1,14000,0,-1,10080,-1,'',8887,0),(799,'Áo Blaziken\n- Tăng 50% băng ,sét ,độc ,lửa lan\n- Quyền năng ma thuật:\n. Tỷ lệ xh: 5%\n. Gây st 1%-10% hp\n- Đòn Đánh Lén:\n. Gây st 1%-5% hp\n. Hồi 1%-10%hp',0,0,-1,0,1,480,10,10,5000,5000,0,0,0,0,0,1,0,-1,14000,0,-1,10080,-1,'',8889,0),(800,'Áo Plusle\r\n- Tăng 50% băng ,sét ,độc ,lửa lan\r\n- Quyền năng ma thuật:\r\n. Tỷ lệ xh: 5%\r\n. Gây st 1%-10% hp\r\n- Đòn Đánh Lén:\r\n. Gây st 1%-5% hp\r\n. Hồi 1%-10%hp',0,0,-1,0,1,480,10,10,5000,5000,0,0,0,0,0,1,0,-1,14000,0,-1,10080,-1,'',8889,0),(801,'Áo Zapdos\n- Tăng 50% băng ,sét ,độc ,lửa lan\n- Quyền năng ma thuật:\n. Tỷ lệ xh: 5%\n. Gây st 1%-10% hp\n- Đòn Đánh Lén:\n. Gây st 1%-5% hp\n. Hồi 1%-10%hp',0,0,-1,0,1,480,10,10,5000,5000,0,0,0,0,0,1,0,-1,14000,0,-1,10080,-1,'',8890,0),(802,'Shinx vàng',27,0,0,0,5,0,5,199,0,0,0,0,199,0,0,0,0,-1,14086,0,-1,0,-1,'',-1,0),(803,'Litleo',27,0,0,0,1,0,5,197,0,0,0,0,197,0,0,0,0,-1,14084,0,-1,0,-1,'',-1,0),(804,'Growlithe',27,0,0,0,0,0,5,198,0,0,0,0,198,0,0,0,0,-1,14085,0,-1,0,-1,'',-1,0),(805,'Áo Raikou nam',0,0,-1,0,1,480,10,10,5000,5000,0,0,0,0,0,1,0,-1,14000,0,-1,10080,-1,'',-1,0),(806,'Áo Raikou nữ',0,0,-1,0,1,480,10,10,5000,5000,0,0,0,0,0,1,0,-1,14000,0,-1,10080,-1,'',-1,0),(807,'Tơ lụa cấp 4',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,3,-1,14126,0,-1,0,-1,'',-1,3),(808,'Tơ lụa cấp 5',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,8,-1,14126,0,-1,0,-1,'',-1,8),(809,'Bạc cấp 4',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,3,-1,14127,0,-1,0,-1,'',-1,3),(810,'Bạc cấp 5',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,8,-1,14127,0,-1,0,-1,'',-1,8),(811,'Thủy tinh cấp 4',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,3,-1,14128,0,-1,0,-1,'',-1,3),(812,'Thủy tinh cấp 5',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,8,-1,14128,0,-1,0,-1,'',-1,8),(813,'Gỗ sưa cấp 4',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,3,-1,14129,0,-1,0,-1,'',-1,3),(814,'Gỗ sưa cấp 5',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,8,-1,14129,0,-1,0,-1,'',-1,8),(815,'Da cứng cấp 4',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,3,-1,14130,0,-1,0,-1,'',-1,3),(816,'Da cứng cấp 5',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,8,-1,14130,0,-1,0,-1,'',-1,8),(817,'Đá cầu vồng tinh khiết lv 1',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,8,-1,14125,0,-1,0,-1,'',-1,8),(818,'Đá cầu vồng tinh khiết lv 2',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,10,-1,14125,0,-1,0,-1,'',-1,10),(819,'Đá cầu vồng tinh khiết lv 3',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,13,-1,14125,0,-1,0,-1,'',-1,13),(820,'Đá cầu vồng tinh khiết lv 4',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,15,-1,14125,0,-1,0,-1,'',-1,15),(821,'Đá cầu vồng tinh khiết lv 5',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,20,-1,14125,0,-1,0,-1,'',-1,20),(822,'Đá cầu vồng tinh khiết lv 6',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,25,-1,14125,0,-1,0,-1,'',-1,25),(823,'Đá tách nguyên liệu 5x',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,8,-1,14131,0,-1,0,-1,'',-1,8),(824,'Đá tách nguyên liệu 6x',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,10,-1,14132,0,-1,0,-1,'',-1,10),(825,'Đá tách nguyên liệu 7x',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,13,-1,14133,0,-1,0,-1,'',-1,13),(826,'Đá thuộc tính thường',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,5,-1,14134,0,-1,0,-1,'',-1,5),(827,'Đá thuộc tính cao cấp',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,10,-1,14135,0,-1,0,-1,'',-1,10),(828,'Xương cấp 6',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,13,-1,14136,0,-1,0,-1,'',-1,13),(829,'Ngọc hoàng hôn cấp 6',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,125,-1,14137,0,-1,0,-1,'',-1,125),(830,'Đá thăng bậc',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,5,-1,14138,0,-1,0,-1,'',-1,5),(831,'Nham thạch cấp 1',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,5,-1,14139,0,-1,0,-1,'',-1,5),(832,'Deerling',27,0,0,0,1,0,5,212,0,0,0,0,212,0,0,0,0,-1,14087,0,-1,10080,-1,'',-1,0),(833,'Áo Glameow-Tăng 100% băng ,sét ,độc ,lửa lan-Kháng 90% sát thương lan-Tỷ lệ xh:15%',0,0,-1,0,1,480,10,10,5000,5000,0,0,0,0,0,1,0,-1,14000,0,-1,10080,-1,'',-1,0),(834,'Áo Persian-Tăng 100% băng ,sét ,độc ,lửa lan-Kháng 90% sát thương lan-Tỷ lệ xh:15%',0,0,-1,0,1,480,10,10,5000,5000,0,0,0,0,0,1,0,-1,14000,0,-1,10080,-1,'',-1,0),(835,'Nhánh hoa đào',28,0,0,0,10,0,0,0,0,0,0,0,0,0,0,0,0,0,14140,0,-1,0,-1,'',-1,0),(836,'Thẻ đổi tên',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,14141,0,-1,0,-1,'',-1,0),(837,'Mặt nạ thỏ',1,0,0,0,1,480,0,0,0,0,0,0,0,0,0,1,0,-1,14001,0,-1,10080,-1,'',-1,0),(838,'Mặt nạ cáo',1,0,0,0,1,480,0,0,0,0,0,0,0,0,0,1,0,-1,14001,0,-1,10080,-1,'',-1,0),(839,'Giáp rồng-Tăng 100% băng ,sét ,độc ,lửa lan-Kháng 90% sát thương lan-Tỷ lệ xh:15%',0,73,0,0,1,480,10,10,5000,5000,80,0,0,73,73,1,0,-1,14000,0,-1,10080,-1,'',8932,0),(756,'Áo Rayquaza\r\nTăng 50% băng ,sét ,độc ,lửa lan\r\nCòn 1hp khi bị quái cắn hoặc bị PK',0,0,0,0,1,480,10,10,5000,5000,0,0,0,0,0,1,0,-1,14000,0,-1,10080,-1,'',-1,0),(841,'Áo Swampert nam cấp 1',0,0,-1,0,1,480,10,10,5000,5000,0,0,10,0,0,0,0,-1,14000,0,-1,10080,-1,'',-1,0),(842,'Áo Swampert nam cấp 2',0,0,-1,0,1,480,10,10,5000,5000,0,0,10,0,0,0,0,-1,14000,0,-1,10080,-1,'',-1,0),(843,'Áo Swampert nữ cấp 1',0,0,-1,0,1,480,10,10,5000,5000,0,0,10,0,0,0,0,-1,14000,0,-1,10080,-1,'',-1,0),(844,'Áo Swampert nữ cấp 2',0,0,-1,0,1,480,10,10,5000,5000,0,0,10,0,0,0,0,-1,14000,0,-1,10080,-1,'',-1,0),(845,'Áo Emboar nam cấp 2',0,0,-1,0,1,480,10,10,5000,5000,0,0,10,0,0,0,0,-1,14000,0,-1,10080,-1,'',-1,0),(846,'Áo Emboar nữ cấp 1',0,0,-1,0,1,480,10,10,5000,5000,0,0,10,0,0,0,0,-1,14000,0,-1,10080,-1,'',-1,0),(847,'Áo Emboar nữ cấp 2',0,0,-1,0,1,480,10,10,5000,5000,0,0,10,0,0,0,0,-1,14000,0,-1,10080,-1,'',-1,0),(848,'Mảnh áo Emboar',29,0,-1,0,1,480,0,0,0,0,0,0,0,0,0,0,0,-1,14143,0,-1,10080,-1,'',-1,0),(849,'Mảnh quần Emboar',29,0,-1,0,1,480,0,0,0,0,0,0,0,0,0,0,0,-1,14145,0,-1,10080,-1,'',-1,0),(850,'Mảnh vòng tay Emboar',29,0,-1,0,1,480,0,0,0,0,0,0,0,0,0,0,0,-1,14147,0,-1,10080,-1,'',-1,0),(851,'Mảnh vòng đầu Emboar',29,0,-1,0,1,480,0,0,0,0,0,0,0,0,0,0,0,-1,14149,0,-1,10080,-1,'',-1,0),(852,'Mảnh giày Emboar',29,0,-1,0,1,480,0,0,0,0,0,0,0,0,0,0,0,-1,14151,0,-1,10080,-1,'',-1,0),(853,'Mảnh găng Emboar',29,0,-1,0,1,480,0,0,0,0,0,0,0,0,0,0,0,-1,14153,0,-1,10080,-1,'',-1,0),(854,'Mảnh áo Swampert',29,0,-1,0,1,480,0,0,0,0,0,0,0,0,0,0,0,-1,14155,0,-1,10080,-1,'',-1,0),(855,'Mảnh quần Swampert',29,0,-1,0,1,480,0,0,0,0,0,0,0,0,0,0,0,-1,14157,0,-1,10080,-1,'',-1,0),(856,'Mảnh vòng tay Swampert',29,0,-1,0,1,480,0,0,0,0,0,0,0,0,0,0,0,-1,14159,0,-1,10080,-1,'',-1,0),(857,'Mảnh vòng đầu Swampert',29,0,-1,0,1,480,0,0,0,0,0,0,0,0,0,0,0,-1,14161,0,-1,10080,-1,'',-1,0),(858,'Mảnh giày Swampert',29,0,-1,0,1,480,0,0,0,0,0,0,0,0,0,0,0,-1,14163,0,-1,10080,-1,'',-1,0),(859,'Mảnh găng Swampert',29,0,-1,0,1,480,0,0,0,0,0,0,0,0,0,0,0,-1,14165,0,-1,10080,-1,'',-1,0),(860,'Áo Emboar',29,0,-1,0,1,480,0,0,0,0,0,0,0,0,0,0,0,-1,14142,0,-1,10080,-1,'',-1,0),(861,'Quần Emboar',29,0,-1,0,1,480,0,0,0,0,0,0,0,0,0,0,0,-1,14144,0,-1,10080,-1,'',-1,0),(862,'Vòng tay Emboar',29,0,-1,0,1,480,0,0,0,0,0,0,0,0,0,0,0,-1,14146,0,-1,10080,-1,'',-1,0),(863,'Vòng đầu Emboar',29,0,-1,0,1,480,0,0,0,0,0,0,0,0,0,0,0,-1,14148,0,-1,10080,-1,'',-1,0),(864,'Giày Emboar',29,0,-1,0,1,480,0,0,0,0,0,0,0,0,0,0,0,-1,14150,0,-1,10080,-1,'',-1,0),(865,'Găng Emboar',29,0,-1,0,1,480,0,0,0,0,0,0,0,0,0,0,0,-1,14152,0,-1,10080,-1,'',-1,0),(866,'Áo Swampert',29,0,-1,0,1,480,0,0,0,0,0,0,0,0,0,0,0,-1,14154,0,-1,10080,-1,'',-1,0),(867,'Quần Swampert',29,0,-1,0,1,480,0,0,0,0,0,0,0,0,0,0,0,-1,14156,0,-1,10080,-1,'',-1,0),(868,'Vòng tay Swampert',29,0,-1,0,1,480,0,0,0,0,0,0,0,0,0,0,0,-1,14158,0,-1,10080,-1,'',-1,0),(869,'Vòng đầu Swampert',29,0,-1,0,1,480,0,0,0,0,0,0,0,0,0,0,0,-1,14160,0,-1,10080,-1,'',-1,0),(870,'Giày Swampert',29,0,-1,0,1,480,0,0,0,0,0,0,0,0,0,0,0,-1,14162,0,-1,10080,-1,'',-1,0),(871,'Găng Swampert',29,0,-1,0,1,480,0,0,0,0,0,0,0,0,0,0,0,-1,14164,0,-1,10080,-1,'',-1,0),(872,'Mặt nạ Snorlax',1,0,0,0,1,480,10,0,0,0,0,0,0,0,0,1,0,-1,14001,0,-1,10080,-1,'',-1,0),(873,'Bidoof',27,0,0,0,5,0,5,245,0,0,0,0,245,0,0,0,0,0,14088,0,-1,10080,-1,'',-1,0),(874,'Pokémon hỗ trợ Suicune 4x lv 1',21,0,0,0,45,480,0,0,0,0,0,0,0,0,0,0,55,-1,14089,0,-1,10080,-1,'',-1,55),(875,'Pokémon hỗ trợ Suicune 4x lv 2',21,0,0,0,45,480,1,0,0,0,0,0,0,0,0,0,55,-1,14089,0,-1,10080,-1,'',-1,55),(876,'Pokémon hỗ trợ Suicune 4x lv 3',21,0,0,0,45,480,2,0,0,0,0,0,0,0,0,0,55,-1,14089,0,-1,10080,-1,'',-1,55),(877,'Pokémon hỗ trợ Suicune 5x lv 1',21,0,0,0,55,480,0,0,0,0,0,0,0,0,0,0,55,-1,14089,0,-1,10080,-1,'',-1,55),(878,'Pokémon hỗ trợ Suicune 5x lv 2',21,0,0,0,55,480,1,0,0,0,0,0,0,0,0,0,55,-1,14089,0,-1,10080,-1,'',-1,55),(879,'Pokémon hỗ trợ Suicune 5x lv 3',21,0,0,0,55,480,2,0,0,0,0,0,0,0,0,0,55,-1,14089,0,-1,10080,-1,'',-1,55),(880,'Pokémon hỗ trợ Suicune 6x lv 1',21,0,0,0,65,480,0,0,0,0,0,0,0,0,0,0,55,-1,14089,0,-1,10080,-1,'',-1,55),(881,'Pokémon hỗ trợ Suicune 6x lv 2',21,0,0,0,65,480,1,0,0,0,0,0,0,0,0,0,55,-1,14089,0,-1,10080,-1,'',-1,55),(882,'Pokémon hỗ trợ Suicune 6x lv 3',21,0,0,0,64,480,2,0,0,0,0,0,0,0,0,0,55,-1,14089,0,-1,10080,-1,'',-1,55),(840,'Khăn quàng sơ cấp',0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-1,14000,0,-1,0,-1,NULL,-1,0),(883,'Trứng Ponyta',21,0,0,0,65,480,2,0,0,0,0,0,0,0,0,0,55,-1,14101,0,-1,0,-1,'',-1,55),(884,'Trứng Piloswine',21,0,0,0,65,480,2,0,0,0,0,0,0,0,0,0,55,-1,14104,0,-1,0,-1,'',-1,55),(885,'Trứng Suicune',21,0,0,0,65,480,2,0,0,0,0,0,0,0,0,0,55,-1,14103,0,-1,0,-1,'',-1,55),(886,'Trứng Spectrier',21,0,0,0,65,480,2,0,0,0,0,0,0,0,0,0,55,-1,14105,0,-1,0,-1,'',-1,55),(887,'Trứng Dragonite',21,0,0,0,1,480,2,0,0,0,0,0,0,0,0,0,55,-1,14102,0,-1,0,-1,'',-1,55),(888,'Áo noel nam 2024\n- Bỏng lạnh trong [10s]- Thời gian hồi: [30s]- Tỷ lệ xh: 5%',0,0,-1,0,1,480,10,10,5000,5000,0,0,0,0,0,1,0,-1,14000,0,-1,10080,-1,'',-1,0),(889,'Áo noel nữ 2024\n- Bỏng lạnh trong [10s]- Thời gian hồi: [30s]- Tỷ lệ xh: 5%',0,0,-1,0,1,480,10,10,5000,5000,0,0,0,0,0,1,0,-1,14000,0,-1,10080,-1,'',-1,0),(890,'Quạt lá Shiftry',4,0,0,0,10,0,0,0,0,0,0,0,0,0,0,0,0,-1,14004,822,2,0,-1,'',267,0),(891,'Gậy xương Marowak',4,0,0,0,10,0,0,0,0,0,0,0,0,0,0,0,0,-1,14004,821,2,0,-1,'',268,0),(892,'Lưỡi liềm Scyther',4,0,0,0,10,0,0,0,0,0,0,0,0,0,0,0,0,-1,14004,820,2,0,-1,'',269,0),(893,'Than lửa huyền thoại',3,4,0,0,86,1500,380,0,0,0,0,0,0,0,0,0,12500,0,14003,5,3,0,-1,NULL,-1,0),(894,'Hạt giống thần huyền thoại',4,4,0,0,86,1500,380,0,0,0,0,0,0,0,0,0,12500,1,14004,5,3,0,-1,NULL,-1,0),(895,'Khăn lụa huyền thoại',5,4,0,0,86,1500,380,0,0,0,0,0,0,0,0,0,12500,2,14005,5,3,0,-1,NULL,-1,0),(896,'Nước thần huyền thoại',6,4,0,0,86,1500,380,0,0,0,0,0,0,0,0,0,12500,3,14006,5,3,0,-1,NULL,-1,0),(897,'Nam châm huyền thoại',7,4,0,0,86,1500,380,0,0,0,0,0,0,0,0,0,12500,4,14007,5,3,0,-1,NULL,-1,0),(903,'Cải Trang Emboar',0,0,-1,0,1,480,0,0,0,0,0,0,0,0,0,0,0,-1,14000,0,-1,10080,-1,'',-1,0),(898,'Cánh Articuno',29,0,0,0,10,1500,0,0,0,0,0,0,0,0,0,1,12500,-1,14166,0,0,0,-1,NULL,270,0),(899,'Cánh Lugia',29,0,0,0,10,1500,0,0,0,0,0,0,0,0,0,1,12500,-1,14167,0,0,0,-1,NULL,271,0),(900,'Cánh Moltres',29,0,0,0,10,1500,0,0,0,0,0,0,0,0,0,1,12500,-1,14168,0,0,0,-1,NULL,272,0),(901,'Cánh Togekiss',29,0,0,0,10,1500,0,0,0,0,0,0,0,0,0,1,12500,-1,14169,0,0,0,-1,NULL,273,0),(902,'Cánh Crobat',29,0,0,0,10,1500,0,0,0,0,0,0,0,0,0,1,12500,-1,14170,0,0,0,-1,NULL,274,0),(904,'Cải Trang Swampert',0,0,-1,0,1,480,0,0,0,0,0,0,0,0,0,0,0,-1,14000,0,-1,10080,-1,'',-1,0),(951,'Skin Pangoro (hệ Cỏ)',19,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-1,14013,0,-1,0,-1,NULL,-1,0),(952,'Skin Xerneas (hệ Nước)',19,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-1,14013,0,-1,0,-1,NULL,-1,0),(953,'Skin Camerupt (hệ Lửa)',19,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-1,14013,0,-1,0,-1,NULL,-1,0),(954,'Skin Lycanroc (hệ Đất)',19,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-1,14013,0,-1,0,-1,NULL,-1,0),(911,'Skin thú cưỡi Dialga\n[hệ Thép]\n- Khi trang bị Chổi Sét: 5% tỉ lệ mỗi lần Sét Lan phát động sẽ triệu hồi Sét đánh, tăng thêm 3 giây choáng',30,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,14095,865,2,0,-1,NULL,-1,0),(910,'Phi phong vô hình',19,16,0,0,0,480,0,0,0,0,0,0,0,0,0,0,0,-1,14013,0,-1,0,-1,'',-1,0),(908,'Skin thú cưỡi Camerupt\n[hệ Lửa] - hợp Dòng Squirtle\n- 5% đòn đánh xuyên toàn bộ giáp (khi hợp hệ)\n- Đặc tính: khi máu xuống dưới 30%, tăng 50% tấn công trong 3 giây, hồi chiêu 40 giây',30,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,14093,863,2,0,-1,NULL,-1,0),(909,'Skin thú cưỡi Lycanroc\n[hệ Đất] - hợp Dòng Charmander\n- Cộng 20% phòng thủ (khi hợp hệ)\n- Hấp thụ 15% sát thương',30,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,14094,864,2,0,-1,NULL,-1,0),(905,'Skin thú cưỡi Copperajah\n[hệ Thép] - hợp Dòng Eevee\n- Cộng 2000 HP\n- Cộng 3000 MP\n- Đặc tính (khi hợp hệ): khi bị Dòng Charmander, Dòng Bulbasaur hoặc Dòng Squirtle đánh, chặn 30% sát thương trong 3 giây. Tỉ lệ 15%, hồi chiêu 40 giây',30,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,14090,860,2,0,-1,NULL,-1,0),(906,'Skin thú cưỡi Pangoro\n[hệ Cỏ] - hợp Dòng Bulbasaur\n- Cộng 200 chính xác (khi hợp hệ)\n- Đặc tính: khi bị Dòng Eevee hoặc Dòng Pichu đánh, phản sát thương +50% (tối đa 80%) trong 3 giây. Tỉ lệ 15%, hồi chiêu 40 giây',30,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,14091,861,2,0,-1,NULL,-1,0),(907,'Skin thú cưỡi Xerneas\n[hệ Nước] - hợp Dòng Pichu\n- Cộng 20% điểm tiềm năng (khi hợp hệ)\n- Đặc tính: khi nhận sát thương, chặn 50% sát thương trong 5 giây. Tỉ lệ 10%, hồi chiêu 40 giây',30,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,14092,862,2,0,-1,NULL,-1,0),(722,'Sách kỹ năng pet (cũ)',24,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,14061,0,-1,0,-1,NULL,-1,0),(960,'Đá Nước',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,14014,0,-1,0,-1,'',-1,0),(961,'Đá Sấm',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,14015,0,-1,0,-1,'',-1,0),(962,'Đá Lửa',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,14016,0,-1,0,-1,'',-1,0),(963,'Đá Mặt Trời',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,14017,0,-1,0,-1,'',-1,0),(964,'Đá Mặt Trăng',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,14018,0,-1,0,-1,'',-1,0),(965,'Đá Lá',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,14019,0,-1,0,-1,'',-1,0),(966,'Đá Băng',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,14020,0,-1,0,-1,'',-1,0),(967,'Ruy Băng Tiên',28,0,-1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,-1,14021,0,-1,0,-1,'',-1,0);
/*!40000 ALTER TABLE `data_item` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `data_monster`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `data_monster` (
  `id` int(4) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `style` int(1) unsigned NOT NULL,
  `he` int(1) NOT NULL,
  `level` int(1) NOT NULL,
  `maxhp` int(2) NOT NULL,
  `attack` int(2) NOT NULL DEFAULT 0,
  `defent` int(2) NOT NULL DEFAULT 0,
  `accurate` int(1) NOT NULL DEFAULT 0,
  `dodge` int(2) NOT NULL DEFAULT 0,
  `rcvxp` int(4) unsigned NOT NULL DEFAULT 0,
  `rcvgold` int(2) unsigned NOT NULL DEFAULT 0,
  `active` int(2) unsigned NOT NULL DEFAULT 0,
  `palate` int(10) unsigned NOT NULL DEFAULT 0,
  `spalate` int(10) unsigned NOT NULL DEFAULT 0,
  `isuse` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `move` tinyint(3) unsigned NOT NULL DEFAULT 1,
  `speed` int(10) unsigned NOT NULL DEFAULT 1,
  `h` int(10) unsigned NOT NULL DEFAULT 20,
  `w` int(10) unsigned NOT NULL DEFAULT 20,
  `xcenter` int(10) unsigned NOT NULL DEFAULT 8,
  `ycenter` int(10) unsigned NOT NULL DEFAULT 8,
  `height` int(10) unsigned NOT NULL DEFAULT 20,
  `isnew` int(10) unsigned NOT NULL DEFAULT 0,
  `dex` smallint(6) NOT NULL DEFAULT 0 COMMENT 'Pokemon: so Pokedex cua template',
  `shiny` tinyint(4) NOT NULL DEFAULT 0 COMMENT 'Pokemon: 1 = shiny',
  `he2` tinyint(4) NOT NULL DEFAULT -1 COMMENT 'Pokemon: he thu hai, -1 = khong co',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=141 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `data_monster` WRITE;
/*!40000 ALTER TABLE `data_monster` DISABLE KEYS */;
INSERT INTO `data_monster` VALUES (1,'Sandshrew',1,8,1,200,8,2,690,5,20,10,0,1,0,1,1,1,20,20,8,8,20,0,27,0,-1),(2,'Caterpie',2,11,2,300,11,2,690,5,30,15,1,2,0,1,1,1,20,20,8,8,20,0,10,0,-1),(3,'Wooper',3,2,3,450,17,3,690,5,45,20,0,3,0,1,1,1,20,20,8,8,20,0,194,0,8),(4,'Torchic',4,1,4,650,25,5,690,10,65,30,1,4,0,1,1,1,20,20,8,8,20,0,255,0,-1),(5,'Ekans',5,7,5,900,34,6,690,15,90,40,0,5,0,1,1,1,20,20,8,8,20,0,23,0,-1),(6,'Litwick',6,13,6,1200,45,8,690,30,120,50,1,6,0,1,1,1,20,20,8,8,20,0,607,0,1),(7,'Bellsprout',7,4,7,1550,57,10,690,10,155,60,0,7,0,1,1,1,20,20,8,8,20,0,69,0,7),(8,'Venonat',8,11,8,1950,74,13,690,0,195,70,1,8,0,1,1,1,20,20,8,8,20,0,48,0,7),(9,'Rattata',9,0,9,2400,90,24,690,5,240,80,0,9,0,1,1,1,20,20,8,8,20,0,19,0,-1),(10,'Oddish',10,4,10,3000,113,30,690,20,300,100,1,10,0,1,1,1,20,20,8,8,20,0,43,0,7),(11,'Weedle',11,11,11,3660,125,37,690,20,366,120,0,11,0,1,1,1,20,20,8,8,20,0,13,0,7),(12,'Vulpix',12,1,12,4380,139,44,690,20,438,120,1,12,0,1,1,1,20,20,8,8,20,0,37,0,-1),(13,'Quagsire',13,2,13,5160,150,52,690,20,516,150,0,13,0,1,1,1,20,20,8,8,20,0,195,0,8),(14,'Combusken',14,1,18,9960,184,100,690,10,996,400,1,14,0,1,1,1,20,20,8,8,20,0,256,0,6),(15,'Tepig',15,1,14,6000,154,60,690,10,600,200,1,15,0,1,1,1,20,20,8,8,20,0,498,0,-1),(16,'Skorupi',16,7,15,6900,165,63,690,15,690,240,1,16,0,1,1,1,20,20,8,8,20,0,451,0,11),(17,'Torkoal',17,1,13,5160,150,52,690,5,516,170,0,17,0,1,1,1,20,20,8,8,20,0,324,0,-1),(18,'Ariados',18,11,24,18600,638,220,690,15,1390,450,1,18,0,1,1,1,20,20,8,8,20,0,168,0,7),(19,'Paras',19,11,16,7860,188,78,690,10,786,280,0,19,0,1,1,1,20,20,8,8,20,0,46,0,4),(20,'Gloom',20,4,17,8880,225,88,690,20,888,320,1,20,0,1,1,1,20,20,8,8,20,0,44,0,7),(21,'Venipede',21,11,15,6900,263,69,690,10,690,240,0,21,0,1,1,1,20,20,8,8,20,0,543,0,7),(22,'Dodrio',22,0,30,30150,1088,340,690,10,1990,610,1,22,0,1,1,1,20,20,8,8,20,0,85,0,9),(23,'Arbok',23,7,31,32630,1163,360,690,15,2090,630,1,23,0,1,1,1,20,20,8,8,20,0,24,0,-1),(24,'Butterfree',24,11,18,9960,184,100,690,10,996,400,1,24,0,1,1,1,20,20,8,8,20,0,12,0,9),(25,'Gastly',25,13,22,15310,563,180,690,15,1190,400,1,25,0,1,1,1,20,20,8,8,20,0,92,0,7),(26,'Tentacool',26,2,21,13770,488,160,690,15,1090,360,1,26,0,1,1,1,20,20,8,8,20,0,72,0,7),(27,'Cubone',27,8,24,18600,638,220,690,10,1390,450,1,27,0,1,1,1,20,20,8,8,20,0,104,0,-1),(28,'Growlithe',28,1,20,12300,413,160,690,5,890,320,1,28,0,1,1,1,20,20,8,8,20,0,58,0,-1),(29,'Croconaw',29,2,26,22170,750,260,690,10,1590,500,1,29,0,1,1,1,20,20,8,8,20,0,159,0,-1),(30,'Croagunk',30,7,27,24060,938,280,690,15,1690,530,1,30,0,1,1,1,20,20,8,8,20,0,453,0,6),(31,'Kricketune',31,11,33,37830,1313,365,690,10,2290,660,1,31,0,1,1,1,20,20,8,8,20,0,402,0,-1),(32,'Parasect',32,11,32,35190,1238,360,690,10,2190,590,1,32,0,1,1,1,20,20,8,8,20,0,47,0,4),(33,'Tauros',33,0,34,40550,1463,380,690,20,2390,720,1,33,0,1,1,1,20,20,8,8,20,0,128,0,-1),(34,'Scraggy',34,15,35,43350,1575,420,690,20,3000,800,1,34,0,1,1,1,20,20,8,8,20,0,559,0,6),(35,'Crawdaunt',35,2,36,46230,1733,420,690,15,3500,900,1,35,0,1,1,1,20,20,8,8,20,0,342,0,15),(36,'Bronzong',36,16,33,50000000,1,28000,690,10,2290,680,1,66,0,1,0,1,20,20,8,8,20,0,437,0,10),(37,'Bronzor',37,16,37,25000000,1,25000,690,20,3800,950,1,67,0,1,0,1,20,20,8,8,20,0,436,0,10),(38,'Gyarados',38,2,38,500000,2250,900,690,30,50000,100000,1,38,0,2,1,1,20,20,8,8,20,0,130,0,9),(39,'Salamence',39,14,39,200000000,30000,25500,690,30,400000,200000,1,39,0,2,1,1,20,20,8,8,20,0,373,0,9),(40,'Whirlipede',40,11,38,53678,1778,494,690,20,3800,1100,1,21,1,1,1,1,20,20,8,8,20,0,544,0,7),(41,'Scolipede',41,11,49,106654,3000,650,690,20,5736,2200,1,21,2,1,1,1,20,20,8,8,20,0,545,0,7),(42,'Whirlipede Shiny',42,11,43,75098,2280,600,690,20,4620,1600,1,21,3,1,1,1,20,20,8,8,20,0,544,1,7),(43,'Sudowoodo',43,12,39,20000000,20000,23000,690,20,3956,1200,1,68,0,1,0,1,20,20,8,8,20,0,185,0,-1),(44,'Blaziken',44,1,51,118730,3015,670,690,20,6140,2400,1,22,2,1,1,1,20,20,8,8,20,0,257,0,6),(45,'Farfetch\'d',45,0,39,57634,1793,534,690,20,3956,1200,1,22,3,1,1,1,20,20,8,8,20,0,83,0,9),(46,'Aegislash',46,16,42,80000000,22000,22000,690,20,4448,1500,1,69,0,2,1,1,20,20,8,8,20,0,681,0,13),(134,'Seviper Shiny',47,7,48,100918,2318,640,690,20,5540,2100,1,23,2,1,1,1,20,20,8,8,20,0,336,1,-1),(48,'Arcanine',48,1,60,193830,6810,900,690,20,9020,5200,1,49,0,1,1,1,20,20,8,8,20,0,59,0,-1),(49,'Gastrodon',49,2,72,460530,14000,16000,690,20,16000,8100,1,62,0,1,1,1,20,20,8,8,20,0,423,0,8),(50,'Dustox',50,11,44,79894,2288,610,690,20,4796,1700,1,24,2,1,1,1,20,20,8,8,20,0,269,0,7),(51,'Trevenant',51,13,74,560530,17000,18000,690,20,18000,10000,1,63,0,1,1,1,20,20,8,8,20,0,709,0,4),(133,'Haunter Shiny',52,13,46,90030,2303,620,690,20,5160,1900,1,25,1,1,1,1,20,20,8,8,20,0,93,1,7),(53,'Whiscash',53,2,68,330530,12000,6400,690,20,12848,5600,1,50,0,1,1,1,20,20,8,8,20,0,340,0,8),(54,'Kingler',54,2,70,360530,12000,7100,690,20,14048,7600,1,51,0,1,1,1,20,20,8,8,20,0,99,0,-1),(55,'Duskull',55,13,37,49878,1763,456,690,20,3648,1000,1,26,1,1,1,1,20,20,8,8,20,0,355,0,-1),(56,'Sharpedo',56,2,76,680530,20000,20000,690,20,20000,11000,1,64,0,1,1,1,20,20,8,8,20,0,319,0,15),(57,'Dusclops',57,13,45,84870,2295,615,690,20,4976,1800,1,26,3,1,1,1,20,20,8,8,20,0,356,0,-1),(58,'Vileplume',58,4,78,800530,23000,22000,690,20,22000,14000,1,65,0,1,1,1,20,20,8,8,20,0,45,0,7),(60,'Rhydon',60,8,62,240878,7040,1000,690,20,9622,2900,1,52,0,1,1,1,20,20,8,8,20,0,112,0,12),(61,'Liepard',61,15,57,160078,3045,710,690,20,7448,3000,1,28,1,0,1,1,20,20,8,8,20,0,510,0,-1),(62,'Luxio',62,3,50,112590,3008,660,690,20,5936,2300,1,28,2,1,1,1,20,20,8,8,20,0,404,0,-1),(63,'Litleo',63,1,40,61750,2258,550,690,20,4116,1300,1,28,3,1,1,1,20,20,8,8,20,0,667,0,0),(64,'Krokorok',64,8,60,183830,3053,720,690,20,8156,3300,1,29,1,0,1,1,20,20,8,8,20,0,552,0,15),(65,'Crabrawler',65,6,58,172230,6590,900,690,20,8400,3400,1,53,0,1,1,1,20,20,8,8,20,0,739,0,-1),(66,'Feraligatr',66,2,62,200878,3068,740,690,20,8648,3500,1,29,3,0,1,1,20,20,8,8,20,0,160,0,-1),(67,'Seismitoad',67,2,63,209778,3750,750,690,20,8900,3600,1,30,1,0,1,1,20,20,8,8,20,0,537,0,8),(68,'Palpitoad',68,2,42,70478,2273,590,690,20,4448,1500,1,30,2,1,1,1,20,20,8,8,20,0,536,0,8),(69,'Toxicroak',69,7,65,228350,3758,760,690,20,9416,3800,1,30,3,0,1,1,20,20,8,8,20,0,454,0,6),(70,'Volcarona',70,11,66,238030,3765,770,690,20,9680,3900,1,31,1,0,1,1,20,20,8,8,20,0,637,0,1),(71,'Kricketot',71,11,41,66030,2265,570,690,20,4280,1400,1,31,2,1,1,1,20,20,8,8,20,0,401,0,-1),(72,'Heracross',72,11,68,258198,3773,780,690,20,10220,4100,1,31,3,0,1,1,20,20,8,8,20,0,214,0,6),(73,'Tauros Shiny',73,0,69,268694,3780,790,690,20,10496,4200,1,33,1,0,1,1,20,20,8,8,20,0,128,1,-1),(74,'Golem',74,12,54,158414,6130,800,690,20,6776,4300,1,54,0,1,1,1,20,20,8,8,20,0,76,0,8),(75,'Mudsdale',75,8,71,290530,3795,810,690,20,11060,4400,1,33,3,0,1,1,20,20,8,8,20,0,750,0,-1),(76,'Magcargo',76,1,56,152630,6360,820,690,20,7220,4500,1,55,0,1,1,1,20,20,8,8,20,0,219,0,12),(77,'Pinsir',77,11,64,268350,10500,5000,690,20,11640,4600,1,56,0,1,1,1,20,20,8,8,20,0,127,0,-1),(78,'Scrafty',78,15,74,325454,3818,840,690,20,11936,4700,1,34,3,0,1,1,20,20,8,8,20,0,560,0,6),(79,'Dhelmise',79,13,75,337690,4500,850,690,20,12236,4800,1,35,1,0,1,1,20,20,8,8,20,0,781,0,4),(80,'Breloom',80,4,52,125078,5900,800,690,20,6348,4900,1,57,0,1,1,1,20,20,8,8,20,0,286,0,6),(81,'Scyther',81,11,66,290530,11000,5700,690,20,11848,5000,1,58,0,1,1,1,20,20,8,8,20,0,123,0,9),(82,'Zapdos',82,3,40,200000000,30000,25500,690,20,200000,100000,1,40,0,2,1,1,20,20,8,8,20,0,145,0,9),(83,'cổng thành',83,0,40,100000000,1,27000,690,20,0,0,1,41,0,2,0,1,20,20,8,8,20,0,0,0,-1),(84,'Yamask',84,13,40,5000,3750,500,690,20,5000,5000,1,42,0,2,1,1,20,20,8,8,20,0,562,0,-1),(90,'Abomasnow',90,4,40,150000000,28000,27000,690,20,2000000,900000,1,48,0,2,1,1,20,20,8,8,20,0,460,0,5),(85,'Đá',85,0,40,50,0,0,0,0,0,0,1,46,0,2,0,1,20,20,8,8,20,0,0,0,-1),(86,'Bông',86,0,40,50,0,0,0,0,0,0,1,43,0,2,0,1,20,20,8,8,20,0,0,0,-1),(87,'Gỗ',87,0,40,50,0,0,0,0,0,0,1,45,0,2,0,1,20,20,8,8,20,0,0,0,-1),(88,'Da',88,0,40,50,0,0,0,0,0,0,1,44,0,2,0,1,20,20,8,8,20,0,0,0,-1),(89,'Sắt',89,0,40,50,0,0,0,0,0,0,1,47,0,2,0,1,20,20,8,8,20,0,0,0,-1),(91,'Serperior',91,4,50,150000000,27000,25000,690,20,1000000,500000,1,59,1,2,1,1,20,20,8,8,20,0,497,0,-1),(92,'Servine',92,4,50,150000000,27000,25000,690,20,1000000,500000,1,59,0,2,1,1,20,20,8,8,20,0,496,0,-1),(93,'Gengar',93,13,55,100000000,24000,20000,690,20,1000000,500000,1,60,0,2,1,1,20,20,8,8,20,0,94,0,7),(94,'Dusknoir',94,13,60,150000000,28000,27000,690,20,2000000,900000,1,61,0,2,1,1,20,20,8,8,20,0,477,0,-1),(95,'Ponyta',95,1,1,158414,1,1,1,1,1,1,0,70,0,1,1,1,20,20,8,8,20,0,77,0,-1),(96,'Ponyta',96,1,1,158414,1,1,1,1,1,1,0,71,0,1,1,1,20,20,8,8,20,0,77,0,-1),(97,'Ponyta',97,1,1,158414,1,1,1,1,1,1,0,72,0,1,1,1,20,20,8,8,20,0,77,0,-1),(98,'Ponyta',98,1,1,158414,1,0,0,0,0,0,0,73,0,1,1,1,20,20,8,8,20,0,77,0,-1),(99,'Ponyta',99,1,1,158414,1,0,0,0,0,0,0,74,0,1,1,1,20,20,8,8,20,0,77,0,-1),(100,'Ponyta Shiny',100,1,1,158414,1,0,0,0,0,0,0,75,0,1,1,1,20,20,8,8,20,0,77,1,-1),(101,'Rapidash',101,1,1,158414,1,0,0,0,0,0,0,76,1,1,1,1,20,20,8,8,20,0,78,0,-1),(102,'Rapidash',102,1,1,158414,1,0,0,0,0,0,0,77,1,1,1,1,20,20,8,8,20,0,78,0,-1),(103,'Rapidash',103,1,1,158414,1,0,0,0,0,0,0,78,1,1,1,1,20,20,8,8,20,0,78,0,-1),(104,'Rapidash',104,1,1,158414,1,0,0,0,0,0,0,79,1,1,1,1,20,20,8,8,20,0,78,0,-1),(105,'Rapidash',105,1,1,158414,1,0,0,0,0,0,0,80,1,1,1,1,20,20,8,8,20,0,78,0,-1),(106,'Rapidash Shiny',106,1,1,158414,1,0,0,0,0,0,0,81,1,1,1,1,20,20,8,8,20,0,78,1,-1),(107,'Mudsdale',107,8,1,158414,1,0,0,0,0,0,0,82,2,1,1,1,20,20,8,8,20,0,750,0,-1),(108,'Mudsdale',108,8,1,158414,1,0,0,0,0,0,0,83,2,1,1,1,20,20,8,8,20,0,750,0,-1),(109,'Mudsdale',109,8,1,158414,1,0,0,0,0,0,0,84,2,1,1,1,20,20,8,8,20,0,750,0,-1),(110,'Mudsdale',110,8,1,158414,1,0,0,0,0,0,0,85,2,1,1,1,20,20,8,8,20,0,750,0,-1),(111,'Mudsdale',111,8,1,158414,1,0,0,0,0,0,0,86,2,1,1,1,20,20,8,8,20,0,750,0,-1),(112,'Mudsdale Shiny',112,8,1,158414,1,0,0,0,0,0,0,87,2,1,1,1,20,20,8,8,20,0,750,1,-1),(113,'Lopunny',113,0,1,200000000,27000,25000,690,20,1000000,500000,1,88,3,2,1,1,20,20,8,8,20,0,428,0,-1),(114,'Shuppet',114,13,1,1,1,1,1,1,1,1,1,89,0,0,1,1,20,20,8,8,20,0,353,0,-1),(115,'Ursaring',115,0,1,400000000,27000,25000,690,20,1000000,500000,1,90,4,2,1,1,20,20,8,8,20,0,217,0,-1),(116,'Darkrai',116,15,1,500000000,35000,50000,690,20,20000000,10000000,1,91,5,2,1,1,20,20,8,8,20,0,491,0,-1),(117,'Gourgeist',117,13,1,500000000,35000,50000,690,20,20000000,10000000,1,92,6,2,1,1,20,20,8,8,20,0,711,0,4),(118,'Galvantula',118,11,80,900530,25000,25000,690,20,32000,15000,1,93,0,1,1,1,20,20,8,8,20,0,596,0,3),(119,'cong thanh',119,0,1,1,1,1,1,1,1,1,0,94,0,1,1,1,20,20,8,8,20,0,0,0,-1),(120,'Rayquaza',120,14,50,100000000,35000,20000,690,20,1000000,1,1,96,7,2,1,1,20,20,8,8,20,1,384,0,9),(121,'Rương ma quái',121,0,1,1,1,1,1,1,1,1,0,97,8,2,0,0,20,20,8,8,30,1,0,0,-1),(122,'Ngọc 1 sao',122,0,1,1,0,0,0,0,0,0,0,98,9,2,1,1,20,20,8,8,20,1,0,0,-1),(123,'Ngọc 2 sao',123,0,1,1,0,0,0,0,0,0,0,99,9,2,1,1,20,20,8,8,20,1,0,0,-1),(124,'Ngọc 3 sao',124,0,1,1,0,0,0,0,0,0,0,100,9,2,1,1,20,20,8,8,20,1,0,0,-1),(125,'Ngọc 4 sao',125,0,1,1,0,0,0,0,0,0,0,101,9,2,1,1,20,20,8,8,20,1,0,0,-1),(126,'Ngọc 5 sao',126,0,1,1,0,0,0,0,0,0,0,102,9,2,1,1,20,20,8,8,20,1,0,0,-1),(127,'Ngọc 6 sao',127,0,1,1,0,0,0,0,0,0,0,103,9,2,1,1,20,20,8,8,20,1,0,0,-1),(128,'Ngọc 7 sao',128,0,1,1,0,0,0,0,0,0,0,104,9,2,1,1,20,20,8,8,20,1,0,0,-1),(129,'Kyogre',129,2,1,1,0,0,0,20,0,0,1,105,11,0,1,1,20,20,8,8,20,1,382,0,-1),(130,'Groudon',130,8,1,1,0,0,0,0,0,0,1,106,10,0,1,1,20,20,8,8,20,1,383,0,-1),(131,'Thùng gỗ',131,0,1,1,0,0,0,0,0,0,0,107,12,0,0,0,20,20,0,0,0,1,0,0,-1),(59,'Marowak',59,8,47,95378,2310,630,690,20,5348,2000,1,27,2,1,1,1,20,20,8,8,20,0,105,0,-1),(52,'Haunter',52,13,46,90030,2303,620,690,20,5160,1900,1,25,1,1,1,1,20,20,8,8,20,0,93,0,7),(135,'Ursaluna',115,8,90,2000530,75000,75000,1290,20,75000,35000,1,93,0,1,1,1,20,20,8,8,20,0,901,0,0),(47,'Seviper',47,7,48,100918,2318,640,690,20,5540,2100,1,23,2,1,1,1,20,20,8,8,20,0,336,0,-1),(136,'Liepard Shiny',136,15,82,1000000,26000,25000,690,20,53000,15000,1,28,1,0,1,1,20,20,8,8,20,0,510,1,-1),(137,'Luxray',137,3,84,5000000,27000,26000,690,20,54000,15000,1,28,2,1,1,1,20,20,8,8,20,0,405,0,-1),(138,'Entei',138,1,86,10000000,28000,27000,690,20,55000,15000,1,28,3,1,1,1,20,20,8,8,20,0,244,0,-1),(132,'Marowak Shiny',59,8,47,95378,2310,630,690,20,5348,2000,1,27,2,1,1,1,20,20,8,8,20,0,105,1,-1);
/*!40000 ALTER TABLE `data_monster` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `data_potion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `data_potion` (
  `id` smallint(6) NOT NULL,
  `idImage` smallint(6) DEFAULT NULL,
  `delay` smallint(6) DEFAULT NULL,
  `isTrade` tinyint(1) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `name2` varchar(255) DEFAULT NULL,
  `price` int(11) DEFAULT NULL,
  `recovered` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `data_potion` WRITE;
/*!40000 ALTER TABLE `data_potion` DISABLE KEYS */;
INSERT INTO `data_potion` VALUES (0,237,0,0,'Xu','xu',0,0),(1,1,350,1,'Thuốc hồi máu','thuochoimau',100,80),(2,2,350,1,'Siêu thuốc hồi máu','sieuthuochoimau',500,300),(3,3,350,1,'Thuốc hồi máu cực mạnh','thuochoimaucucmanh',800,1000),(4,4,350,1,'Ether','ether',50,160),(5,5,350,1,'Ether lớn','etherlon',150,600),(6,6,350,1,'Ether tối đa','ethertoida',500,2000),(7,22,350,0,'Bình tăng tốc thường','binhtangtocthuong',500,1),(8,88,350,0,'Bình tăng tốc vàng.\nGiảm 20% thời gian chín của trái vàng','binhtangtocvanggiamthoigianchincuatraivang',500,2),(9,26,350,0,'Rễ năng lượng','renangluong',500,1),(10,62,350,0,'Kẹo kinh nghiệm S: cộng 100k exp','keokinhnghiemscongkexp',500,2),(11,40,0,0,'Kẹo kinh nghiệm M: cộng 500k exp','keokinhnghiemmcongkexp',500,1),(12,36,0,0,'Kẹo kinh nghiệm L: cộng 1t exp','keokinhnghiemlcongtexp',500,0),(13,23,0,0,'Bình tăng tốc bạc.\nGiảm 20% thời gian chín của trái bạc','binhtangtocbacgiamthoigianchincuatraibac',500,0),(14,15,0,0,'Khăn đỏ','khando',500,0),(15,16,0,0,'Khăn xanh','khanxanh',500,0),(16,17,0,0,'Khăn vàng','khanvang',500,0),(17,18,0,0,'Khăn xanh lá','khanxanhla',500,0),(18,19,0,0,'Khăn tím','khantim',500,0),(19,13,0,0,'Dây thoát hiểm','daythoathiem',1000,-1),(20,13,0,0,'Vé tàu','vetau',15000,-1),(21,9,350,1,'Siêu thuốc hồi máu đặc biệt','sieuthuochoimaudacbiet',0,0),(22,10,350,1,'Thuốc hồi máu tối đa','thuochoimautoida',0,0),(23,7,350,1,'Elixir','elixir',0,0),(24,8,350,1,'Elixir tối đa','elixirtoida',0,0),(25,20,350,0,'Thuốc khôi phục tiềm năng','thuockhoiphuctiemnang',0,0),(26,21,350,0,'Thuốc hồi phục toàn diện','thuochoiphuctoandien',0,0),(27,32,0,0,'Vé bay Pidgeot','vebaypidgeot',0,0),(28,32,0,0,'Thẻ cấm chat','thecamchat',0,0),(29,32,0,0,'Phiếu công ích','phieucongich',0,0),(30,27,0,1,'Sáo gọi Ponyta','saogoiponyta',0,0),(31,33,0,0,'Huy hiệu lập đội','huyhieulapdoi',0,0),(32,13,0,0,'Thẻ đấu trường','thedautruong',0,0),(33,39,0,0,'Thẻ chiếm Gym','thechiemgym',0,0),(34,37,0,0,'Sáo gọi Ponyta lửa','saogoiponytalua',0,0),(35,11,0,0,'Vé giờ vàng 100, x2 kinh nghiệm trong một giờ chơi. Tính theo thời gian chơi','vegiovangxkinhnghiemtrongmotgiochoitinhtheothoigianchoi',0,0),(36,236,0,0,'Sáo gọi Stantler','saogoistantler',0,0),(37,113,0,1,'Lông đinh','longdinh',0,0),(38,114,0,1,'Lông búi','longbui',0,0),(39,115,0,1,'Lông cột cao','longcotcao',0,0),(40,116,0,1,'Lông chéo','longcheo',0,0),(41,117,0,1,'Lông xù','longxu',0,0),(42,118,0,1,'Lông ngang vai','longngangvai',0,0),(43,119,0,1,'Lông đinh so le','longdinhsole',0,0),(44,120,0,1,'Lông so le','longsole',0,0),(45,121,0,1,'Lông gọn gàng','longgongang',0,0),(46,122,0,1,'Lông cài hoa','longcaihoa',0,0),(47,123,0,1,'Bờm Ponyta','bomponyta',0,0),(48,124,0,1,'Lông Clefairy','longclefairy',0,0),(49,125,0,1,'Lông dựng đứng','longdungdung',0,0),(50,43,0,0,'Hoa mai','hoamai',0,0),(51,43,0,0,'Hoa mai','hoamai',0,0),(52,43,0,0,'Hoa mai','hoamai',0,0),(53,43,0,0,'Hoa mai','hoamai',0,0),(54,44,0,0,'Hoa đào','hoadao',0,0),(55,44,0,0,'Hoa đào','hoadao',0,0),(56,44,0,0,'Hoa đào','hoadao',0,0),(57,44,0,0,'Hoa đào','hoadao',0,0),(58,45,0,0,'Hoa hồng','hoahong',0,0),(59,45,0,0,'Hoa hồng','hoahong',0,0),(60,45,0,0,'Hoa hồng','hoahong',0,0),(61,45,0,0,'Hoa hồng','hoahong',0,0),(62,240,0,0,'Bí ma','bima',0,0),(63,42,0,0,'Hộp may mắn','hopmayman',0,0),(64,47,0,1,'Sáo gọi Tauros','saogoitauros',0,0),(65,46,0,1,'Sáo gọi Arcanine','saogoiarcanine',0,0),(66,48,0,1,'Sáo gọi Mightyena','saogoimightyena',0,0),(67,49,0,1,'Sáo gọi Pidgeot','saogoipidgeot',0,0),(68,38,0,1,'Sáo gọi Rapidash','saogoirapidash',0,0),(69,32,0,0,'Vé quay số','vequayso',0,0),(70,24,0,1,'Trứng Articuno','trungarticuno',0,0),(71,227,0,1,'Trứng Ponyta','trungponyta',0,0),(72,146,0,1,'Trứng Piloswine','trungpiloswine',0,0),(73,141,0,0,'Túi nhặt ngọc','tuinhatngoc',0,0),(74,145,0,1,'Trứng Spectrier','trungspectrier',0,0),(75,11,1000,0,'Vé giờ vàng 100, x2 kinh nghiệm trong 3 giờ chơi. Tính theo thời gian chơi','vegiovangxkinhnghiemtronggiochoitinhtheothoigianchoi',0,0),(76,32,0,0,'Vé quay số đặc biệt','vequaysodacbiet',0,0),(77,213,0,0,'Mảnh bản đồ 1','manhbando',0,0),(78,32,0,0,'Thẻ liên hoa','thelienhoa',0,0),(79,42,0,0,'Hộp thần kỳ','hopthanky',0,0),(80,60,0,0,'Bình tăng lực 5%/n. Tăng 5% các chỉ số trong 3 ngày','binhtanglucntangcacchisotrongngay',0,0),(81,11,0,0,'Vé giờ vàng tăng 150% kinh nghiệm.','vegiovangtangkinhnghiem',0,0),(82,107,0,0,'Thẻ giao hàng. Thêm một lượt giao hàng khi sử dụng','thegiaohangthemmotluotgiaohangkhisudung',0,0),(83,245,0,0,'Kẹo Poké. Tăng 1% kinh nghiệm cho thú nuôi khi cho ăn.','keopoketangkinhnghiemchothunuoikhichoan',0,0),(84,31,0,0,'Thuốc X','thuocx',0,0),(85,32,0,0,'Thẻ triệu hồi','thetrieuhoi',0,0),(86,65,0,1,'Sáo gọi Moltres','saogoimoltres',0,0),(87,32,0,0,'Thẻ phong chức','thephongchuc',0,0),(88,73,0,0,'Nhẫn Trái tim đồng 2%pt','nhantraitimdongpt',0,0),(89,74,0,0,'Nhẫn Trái tim bạc 4%pt','nhantraitimbacpt',0,0),(90,75,0,0,'Nhẫn Trái tim vàng 7%pt','nhantraitimvangpt',0,0),(91,32,0,0,'Vé vào mỏ','vevaomo',0,0),(92,76,0,0,'Thiệp cưới','thiepcuoi',0,0),(93,10,350,1,'Thuốc hồi máu 7k','thuochoimauk',0,0),(94,10,350,1,'Thuốc hồi máu 15k','thuochoimauk',0,0),(95,238,350,1,'Ether 7k','etherk',0,0),(96,238,350,1,'Ether 15k','etherk',0,0),(97,105,0,0,'Thẻ đổi hàng','thedoihang',0,0),(98,93,0,0,'Vảy trái tim','vaytraitim',0,0),(99,64,0,0,'Pháo hoa','phaohoa',0,0),(100,77,0,0,'loa','loa',0,0),(101,67,0,0,'Rương vàng','ruongvang',0,0),(102,68,0,0,'Rương bạc','ruongbac',0,0),(103,43,0,0,'Lá vàng','lavang',0,0),(104,246,0,0,'Poké Ball. Dùng để bắt thú nuôi','pokeballdungdebatthunuoi',0,0),(105,214,0,0,'Mảnh bản đồ 2','manhbando',0,0),(106,45,0,0,'Nhánh hoa hồng vàng','nhanhhoahongvang',0,0),(107,45,0,0,'Hoa hồng đỏ','hoahongdo',0,0),(108,220,0,0,'Hộp quà may mắn\nQuà bình chọn World Cup.\nBấm hai lần để mở, nhận ngẫu nhiên phi phong, nguyên liệu, xu/lượng hoặc pet.','hopquamayman',0,0),(109,45,0,1,'Bó hoa vàng','bohoavang',0,0),(110,45,0,0,'Bó hoa đỏ','bohoado',0,0),(111,95,0,1,'Đĩa chiêu (TM) cho pet','diachieutmchopet',0,0),(112,85,0,0,'Thức ăn tươi','thucantuoi',0,0),(113,86,0,0,'Thức ăn khô','thucankho',0,0),(114,87,0,0,'Thức ăn tổng hợp','thucantonghop',0,0),(115,166,0,1,'Trứng Suicune','trungsuicune',0,0),(116,106,0,0,'Thẻ đổi hàng cao cấp','thedoihangcaocap',0,0),(117,215,0,0,'Mảnh bản đồ 3','manhbando',0,0),(118,32,0,0,'Vé Moltres.\nDùng quay số Moltres','vemoltresdungquaysomoltres',0,0),(119,152,0,0,'Thuốc hồi sinh\nHồi sinh bản thân.','thuochoisinhhoisinhbanthan',0,0),(120,91,0,0,'Thịt','thit',0,0),(121,89,0,0,'Trứng','trung',0,0),(122,199,0,0,'Đậu xanh','dauxanh',0,0),(123,31,0,0,'Thuốc X siêu cấp','thuocxsieucap',0,0),(124,66,0,0,'Bánh dẻo','banhdeo',0,0),(125,92,0,0,'Bánh nướng','banhnuong',0,0),(126,162,0,0,'Thuốc hồi máu chiến trường.\nHồi 10.000 hp','thuochoimauchientruonghoihp',0,0),(127,239,0,0,'Ether chiến trường.\nHồi 10.000 mp','etherchientruonghoimp',0,0),(128,163,0,0,'Thuốc X chiến trường','thuocxchientruong',0,0),(129,156,0,0,'X Phòng thủ.\nTăng 20% giáp trong 90s','xphongthutanggiaptrongs',0,0),(130,158,0,0,'X Tấn công.\nTăng 20% sát thương.','xtancongtangsatthuong',0,0),(131,159,0,0,'Thuốc săn trùm.\nTăng 50% sát thương boss.','thuocsantrumtangsatthuongboss',0,0),(132,157,0,0,'Bom lửa. Gây sát thương theo x10% năng lượng bản thân lên toàn bộ mục tiêu trong bán kính 5m.','bomluagaysatthuongtheoxnangluongbanthanlentoanbomuctieutrongbankinhm',0,0),(133,171,0,0,'Hộp bánh thập cẩm','hopbanhthapcam',0,0),(134,212,0,0,'Hộp bánh đặc biệt','hopbanhdacbiet',0,0),(135,108,0,0,'Hạt mầm','hatmam',0,0),(136,28,0,0,'Nến','nen',0,0),(137,208,0,0,'Kẹo','keo',0,0),(138,207,0,0,'Bí ngô','bingo',0,0),(139,35,0,0,'Hộp quà','hopqua',0,0),(140,222,0,0,'Hộp quà halloween','hopquahalloween',0,0),(141,223,0,0,'Hộp quà tặng halloween','hopquatanghalloween',0,0),(142,30,0,0,'Bột mỳ','botmy',0,0),(143,0,0,0,'Hộp quà bí mật','hopquabimat',0,0),(144,230,0,0,'Sao vàng','saovang',0,0),(145,14,0,0,'Dưa Hấu','duahau',0,0),(146,99,0,0,'Kem','kem',0,0),(147,220,0,0,'Túi quà may mắn','tuiquamayman',0,0),(148,177,0,0,'Gạo nếp','gaonep',0,0),(149,233,0,0,'Thịt mỡ','thitmo',0,0),(150,172,0,0,'Lá dong','ladong',0,0),(151,180,0,0,'Bánh chưng','banhchung',0,0),(152,175,0,0,'Bánh tét','banhtet',0,0),(153,170,0,0,'Giấy','giay',0,0),(154,174,0,0,'Thuốc pháo','thuocphao',0,0),(155,64,0,0,'Pháo hoa tết','phaohoatet',0,0),(156,200,0,0,'Lửa thường','luathuong',0,0),(157,201,0,0,'Lửa vừa','luavua',0,0),(158,202,0,0,'Lửa to','luato',0,0),(159,76,0,0,'Bao lì xì','baolixi',0,0),(160,247,0,0,'Quả bóng bạc','quabongbac',0,0),(161,248,0,0,'Quả bóng vàng','quabongvang',0,0);
/*!40000 ALTER TABLE `data_potion` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `data_quest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `data_quest` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `npcrcv` int(10) NOT NULL,
  `npcres` int(10) NOT NULL,
  `lv` int(10) unsigned NOT NULL DEFAULT 1,
  `mainsub` tinyint(3) unsigned NOT NULL DEFAULT 0 COMMENT '0-main-1sub',
  `content` varchar(1000) NOT NULL COMMENT 'noi dung oi chuyen nhan nhiem vu',
  `scontent` varchar(500) NOT NULL DEFAULT '32000' COMMENT 'mo ta ngan gon nhiem vu',
  `rescontent` varchar(500) NOT NULL DEFAULT '32000',
  `idItemget` varchar(45) NOT NULL DEFAULT '32000',
  `idMonskill` varchar(45) NOT NULL DEFAULT '32000',
  `type` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `nitemget` varchar(45) NOT NULL DEFAULT '32000' COMMENT 'so luong item can lay tuong ung',
  `nmonskill` varchar(45) NOT NULL DEFAULT '32000' COMMENT 'so luong quai can giet tuong ung',
  `gold` bigint(20) unsigned NOT NULL DEFAULT 0,
  `exp` bigint(20) unsigned NOT NULL DEFAULT 0,
  `potiongift` varchar(45) NOT NULL DEFAULT '32000',
  `npotion` varchar(45) NOT NULL DEFAULT '32000',
  `itemgift` varchar(45) NOT NULL DEFAULT '32000',
  `deltaLv` int(10) unsigned NOT NULL DEFAULT 0,
  `infogift` varchar(200) DEFAULT NULL,
  `infofinish` varchar(200) NOT NULL DEFAULT 'Nhiệm vụ hoàn thành',
  `pactive` int(10) unsigned NOT NULL DEFAULT 0,
  `luong` int(10) DEFAULT 0,
  `luongLock` int(10) DEFAULT 100,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=50 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `data_quest` WRITE;
/*!40000 ALTER TABLE `data_quest` DISABLE KEYS */;
INSERT INTO `data_quest` VALUES (1,'Nhiệm vụ tân thủ',5,32000,1,0,'1Chào cháu, ta là Trưởng làng Xatu. Ta sẽ cho cháu ít tiền để bắt đầu cuộc phiêu lưu>0Chào Trưởng làng>1Cháu là Pokémon mới đến từ Rừng Viridian phải không>0Dạ phải ạ>1Như đã hứa, ta sẽ giới thiệu cháu với các nhà huấn luyện của làng Pewter>1Trong thế giới Pokémon, sức mạnh luôn đi cùng lòng tốt: hãy giúp kẻ yếu và sống vì lẽ phải>1Cháu hãy đi chào hỏi mọi người trong làng>1và giúp họ vài việc nhỏ để làm quen>1Đầu tiên, hãy gặp Machamp thợ rèn.>1Cậu ấy là chuyên gia vật phẩm của làng này đó.>0Dạ, cháu đi ngay>1Tốt lắm','32000','32000','32000','32000',3,'32000','32000',1000,100,'32000','32000','32000',0,'Nhiệm vụ:|chào tất cả NPC trong làng|Địa điểm:|trong làng|NPC nv:|tất cả NPC|Phần thưởng:|999999999 xu và 100 exp','32000',0,0,0),(2,'Nhiệm vụ tân thủ',2,32000,1,0,'1Khà khà!>1Ta đã nghe Trưởng làng kể về cháu>1Còn nhỏ mà đã biết sống vì lẽ phải>1Thật đáng quý!>1Ta là Machamp, thợ rèn của làng>1Niềm đam mê của ta là chế tạo và mua bán vật phẩm cho Pokémon>1Có gì thắc mắc về vật phẩm cứ đến tìm ta>1Giờ hãy đi gặp Bronzong thợ giáp nào.','32000','32000','32000','32000',3,'32000','32000',100,100,'32000','32000','32000',0,'Nhiệm vụ:|chào tất cả NPC trong làng|Địa điểm:|trong làng|NPC nv:|tất cả NPC|Phần thưởng:|100 xu và 100 exp','32000',0,0,0),(3,'Nhiệm vụ tân thủ',3,32000,1,0,'1Tuổi nhỏ chí lớn, tuổi nhỏ chí lớn!>1Ta là Bronzong, thợ làm đồ phòng thủ duy nhất của làng>1Khăn quàng, đai, băng đô... loại nào ta cũng rành>1Cần gì về đồ phòng thủ cứ đến tìm ta nhé!>1Cũng xế trưa rồi.>1Giờ này chắc Kecleon tạp hoá đang chờ cháu đấy>1Hãy qua chào hỏi đi.','32000','32000','32000','32000',3,'32000','32000',100,100,'32000','32000','32000',0,'Nhiệm vụ:|chào tất cả NPC trong làng|Địa điểm:|trong làng|NPC nv:|tất cả NPC|Phần thưởng:|100 xu và 100 exp','32000',0,0,0),(4,'Nhiệm vụ tân thủ',1,32000,1,0,'1Phù! Ta đợi cháu từ sáng sớm đến giờ>1Nghe chuyện của cháu mà ta thương quá>1Cửa hàng ta bán đủ thứ cho cả làng, từ Thuốc hồi máu đến Ether.>1Kangaskhan thủ kho đang háo hức được gặp cháu>1Ăn xong thì ghé chỗ cô ấy nhé.','32000','32000','32000','32000',3,'32000','32000',100,100,'32000','32000','32000',0,'Nhiệm vụ:|chào tất cả NPC trong làng|Địa điểm:|trong làng|NPC nv:|tất cả NPC|Phần thưởng:|100 xu và 100 exp','32000',0,0,0),(5,'Nhiệm vụ tân thủ',9,32000,1,0,'1Ha ha! Cháu đây rồi>1Lạ thật, chưa gặp lần nào mà ta thấy như quen cháu từ lâu>1Thật là vui>1Ta vốn ít nói nên cháu đừng ngại>1Sau này trên đường phiêu lưu>1nếu có món đồ quý mà không tiện mang theo>1cứ gửi ta giữ trong túi của ta>1Bảo đảm không ai lấy được.>1Giờ hãy đến gặp Persian>1cứ tìm ngôi nhà to và đẹp nhất làng là thấy.','32000','32000','32000','32000',3,'32000','32000',100,100,'32000','32000','32000',0,'Nhiệm vụ:|chào tất cả NPC trong làng|Địa điểm:|trong làng|NPC nv:|tất cả NPC|Phần thưởng:|100 xu và 100 exp','32000',0,0,0),(6,'Nhiệm vụ tân thủ',6,32000,1,0,'1Rất vui được gặp cháu>1Cháu tuy một mình nhưng ngoan và có chí lớn>1Thật tốt khi cháu trở thành nhà huấn luyện của làng>1Nếu cần nguyên liệu nâng cấp vật phẩm thì cứ đến tìm ta>1Ta đã nhắn Meowth thương nhân, bạn ta, về cháu rồi>1Hãy đến chào cậu ấy nhé.','32000','32000','32000','32000',3,'32000','32000',100,100,'32000','32000','32000',0,'Nhiệm vụ:|chào tất cả NPC trong làng|Địa điểm:|trong làng|NPC nv:|tất cả NPC|Phần thưởng:|100 xu và 100 exp','32000',0,0,0),(7,'Nhiệm vụ tân thủ',22,32000,1,0,'1Chào bạn trẻ!>1Ta hay đi buôn bán khắp các vùng>1Gặp được nhau thật là có duyên>1Meo meo ha ha>1Muốn phiêu lưu khắp thế giới Pokémon>1thì cần có chút bản lĩnh.>1Hãy đến gặp Blaziken để học hỏi nhé.>1Blaziken đang chờ cháu ở doanh trại>1Mau đến đó đi nào.','32000','32000','32000','32000',3,'32000','32000',100,100,'32000','32000','32000',0,'Nhiệm vụ:|chào tất cả NPC trong làng|Địa điểm:|trong làng|NPC nv:|tất cả NPC|Phần thưởng:|100 xu và 100 exp','32000',0,0,0),(8,'Nhiệm vụ tân thủ',21,32000,1,0,'1Hây dà! Cuối cùng cũng đến!>1Cháu thật sự muốn trở thành nhà huấn luyện của làng Pewter chứ?>1Nơi đây không có chỗ cho kẻ lười biếng đâu nhé>1Hãy chăm chỉ luyện tập>1ta thấy cháu có tiềm năng đấy>1Ta luôn có những nhiệm vụ cần người giải quyết>1khi nào đủ tự tin thì đến tìm ta.>1Nhân đây ta dạy cháu 1 chiêu>1Nó sẽ giúp cháu khi bắt đầu chuyến phiêu lưu.>1Giờ hãy quay lại gặp Trưởng làng>1Ông ấy có vài điều muốn dặn trước khi cháu lên đường.','32000','32000','32000','32000',3,'32000','32000',100,100,'32000','32000','32000',0,'Nhiệm vụ:|chào tất cả NPC trong làng|Địa điểm:|trong làng|NPC nv:|tất cả NPC|Phần thưởng:|100 xu và 100 exp','32000',0,0,0),(9,'Nhiệm vụ tân thủ',5,32000,1,0,'1Chúc mừng cháu>1Từ giờ cháu đã là nhà huấn luyện của làng Pewter>1Đừng phụ lòng mọi người nhé>0Dạ vâng. Cháu sẽ cố gắng.','32000','32000','32000','32000',3,'32000','32000',100,100,'32000','32000','32000',0,'Nhiệm vụ:|chào tất cả NPC trong làng|Địa điểm:|trong làng|NPC nv:|tất cả NPC|Phần thưởng:|100 xu và 100 exp','32000',0,0,0),(10,'Tiệc chào mừng',1,1,2,0,'1Cháu biết không, Pewter là ngôi làng có lịch sử lâu đời.>1Đây không chỉ là nơi ta sinh sống>1mà còn là nơi lưu giữ ý chí của những nhà huấn luyện đầu tiên khám phá thế giới Pokémon>1Nơi đây cũng là quê hương thứ hai của bao nhà vô địch khắp các vùng>1ai cũng tự hào là thành viên của làng>1Họ là tấm gương cho các Pokémon đi sau>1Cháu hãy cố gắng nối tiếp truyền thống của làng nhé>1Để ta làm bữa tiệc chào mừng cháu>1cháu hãy hạ 20 con Sandshrew ở Viridian giúp ta nhé>Cháu có muốn giúp ta việc này không ?','Sắp đến giờ tiệc rồi, nhanh lên cháu nhé.','1Tốt lắm, cảm ơn cháu!','32000','1',0,'32000','20',200,200,'32000','32000','32000',0,'Nhiệm vụ:|bắt 20 con Sandshrew|Địa điểm:|Viridian|NPC nv:|Kecleon|Phần thưởng:|200 xu và 200 exp','Nhiệm vụ hoàn thành|Quay về gặp Kecleon|Để nhận thưởng',0,0,0),(11,'Đuổi Caterpie phá lúa',6,6,2,0,'1Nhà ta có mấy thửa ruộng trước cổng làng đang bị dịch sâu hại lúa>1cậu hãy giúp ta diệt sâu>1để chúng không phá lúa nhà ta nữa nhé>Con có muốn giúp ta làm việc này không ?','Ôi lúa của ta, cậu hãy đi nhanh đi.','1Chà,cậu làm nhanh thế!>1Ta nghĩ cũng phải hơn nửa ngày mới xong chứ>1cậu làm ta thật bất ngờ !','32000','2',0,'32000','40',200,300,'32000','32000','32000',0,'Nhiệm vụ:|diệt 40 con Caterpie|Địa điểm:|Viridian|NPC nv:|Persian|Phần thưởng:|200 xu và 300 exp','Nhiệm vụ hoàn thành|Quay về gặp Persian|Để nhận thưởng',0,0,0),(12,'Thu nọc Ekans',6,6,5,0,'1À, Ekans ở Viridian dạo này sinh sôi quá nhiều>1Chúng hay trườn vào ruộng làm mọi người sợ hãi>1Vùng Viridian cách làng ta chừng 5 dặm có rất nhiều Ekans>1Cháu hãy đến đó và mang về giúp ta 60 túi nọc Ekans để làm thuốc giải độc>Cháu có muốn giúp ta không ?','Cẩn thận kẻo bị Ekans cắn đấy!','1Cảm ơn cháu!>1Cháu thật tốt bụng.','1','5',2,'60','32000',300,300,'32000','32000','32000',0,'Nhiệm vụ:|thu 60 túi nọc Ekans|Địa điểm:|Viridian|NPC nv:|Persian|Phần thưởng:|300 xu và 300 exp','Nhiệm vụ hoàn thành|Quay về gặp Persian|Để nhận thưởng',0,0,0),(13,'Bắt chuột phá lúa',6,6,9,0,'1Haiiiiii !>1Mùa màng năm nay của làng ta ở ruộng xa Vermilion thất bát quá>1Do bọn chuột từ đâu kéo về sinh sôi nảy nở quá nhiều>1cậu hãy giúp dân làng diệt lũ chuột để chúng khỏi phá hại cây lúa>Cậu có muốn giúp ta không ?','Hãy mau đi đi, kẻo chuột ăn hết lúa của ta.','1Nhờ có cậu mà bọn chuột đã bớt phá hại>1thu hoạch cũng được khá hơn>Hãy nhận chút quà mọn của ta nhé.','32000','9',0,'32000','50',500,50000,'32000','32000','32000',0,'Nhiệm vụ:|diệt 50 con Rattata|Địa điểm:|Vermilion|NPC nv:|Persian|Phần thưởng:|500 xu và 50000 exp','Nhiệm vụ hoàn thành|Quay về gặp Persian|Để nhận thưởng',0,0,0),(14,'Đưa thư cho Torkoal',3,23,12,0,'1Ta có một người bạn tâm giao cùng học nghệ năm xưa sống tại vùng Cerulean>1lão tên là Torkoal.>1Ta và lão tình như thủ túc>1lâu ngày không gặp sinh lòng nhớ thương bạn hiền>1ta muốn gửi thư hỏi thăm xem lão độ này sinh sống thế nào>Cậu có thể giúp ta mang phong thư này trao tận tay lão không ?','Hãy mang lá thư của ta đến vùng Cerulean giao cho Torkoal','1Oh,Bronzong anh gửi thư cho ta àh?>1Thật quá bất ngờ !>1Ta cũng định ít bữa nữa thu xếp đến thăm anh ấy>1chưa kịp đi thì anh ấy đã cho người đưa thư đến hỏi thăm.>1ta đã xem xong thư để ta hồi âm cho anh ấy vài chữ, cậu đợi nhé !>1Hãy giúp ta chuyển lời cảm ơn đến Bronzong anh nhé>1Phiền cậu nhắn với anh ấy>1vài bữa nữa ta sẽ đến thăm anh ấy sau>1Thật cảm ơn cậu rất nhiều.','4','32000',1,'32000','32000',0,0,'32000','32000','32000',0,'32000','Nhiệm vụ hoàn thành|Quay về gặp Bronzong|Để nhận thưởng',0,0,0),(15,'Đưa thư cho Torkoal',3,32000,12,0,'1Được tin lão Torkoal khỏe mạnh ta thật an lòng>1Thật vất vả cho cậu>1Đây ta tặng cậu cái áo>1để cảm ơn cậu đã giúp ta.','','','32000','32000',3,'32000','32000',500,60000,'32000','32000','81,88,95,102,109,10',0,'Nhiệm vụ:|đưa thư|Địa điểm:|Cerulean|NPC nv:|Torkoal|Phần thưởng:|500 xu và 60000 exp,|vk lv11 +10','',0,0,0),(16,'Làm bánh Poké',1,1,14,0,'1Cả làng rất thích bánh Poké do ta làm nên hôm nay ta muốn đãi mọi người một bữa>1Sáng nay ta đã mua hết Berry ngoài chợ nhưng vẫn chưa đủ>1Đám Tepig ở Celadon hay giấu Berry trong hang>1cháu hãy hạ chúng và mang về đây cho ta 50 túi Berry nhé>Cháu có sẵn sàng giúp ta làm bánh không ?','Mọi người đang đợi bánh Poké của ta đấy, đi sớm về sớm.','1Con về hơi trễ so với dự kiến của ta>1nhưng không sao vẫn còn kịp>1ta có chút quà cho con đây.','5','15',2,'50','32000',800,150000,'32000','32000','32000',0,'Nhiệm vụ:|lấy 50 túi Berry|Địa điểm:|Celadon|NPC nv:|Kecleon|Phần thưởng:|800 xu và 150000 exp','Nhiệm vụ hoàn thành|Quay về gặp Kecleon|Để nhận thưởng',0,0,0),(17,'Điều chế dược phẩm',1,1,15,0,'1Ta đang cần nọc độc của Skorupi, rết để điều chế một số dược phẩm>1con hãy đến Saffron tìm 30 nọc Skorupi mang về cho ta nhé>Con có muốn giúp ta không ?','Có dược liệu cho ta chưa ?','1Cảm ơn con !>1Hãy nhận chút quà của ta.','0','16',2,'30','32000',1000,500000,'32000','32000','32000',0,'Nhiệm vụ:|lấy 30 đuôi Skorupi|30 con Venipede|Địa điểm:|Saffron, Fuchsia|NPC nv:|Kecleon|Phần thưởng:|1000 xu và 500000 exp','Nhiệm vụ hoàn thành|Quay về gặp Kecleon|Để nhận thưởng',0,0,0),(18,'Sửa nhà cho trưởng làng',5,5,17,0,'1Nhà của ta đã lâu lắm không tu sửa gì nên nó đã quá cũ kĩ>1đã có nhiều chổ bị mối mọt ăn mục>1Con hãy giúp ta  sửa lại căn nhà cho kịp mùa mưa đang đến>1vùng Fuchsia nổi tiếng nhiều gỗ tốt>1con hãy đến đấy tìm gỗ giúp ta nhé>1Rừng thiêng nước độc>1trên núi có rất nhiều hoa ăn thịt và quái thú con phải hết sức cẩn thận>Con có muốn giúp ta việc này không ?','Con còn chậm chạp như thế thì nhà ta sẽ sập mất !','1Nhà ta đã kiên cố và đẹp hơn rồi>1Thật vất vả cho con.','2','20',2,'50','32000',2000,900000,'21,23','10,10','32000',0,'Nhiệm vụ:|nhặt 50 khúc gỗ|từ Gloom|Địa điểm:|Fuchsia|NPC nv:|Trưởng Làng|Phần thưởng:|2000 xu, 900000 exp,|10HP vừa, 10MP vừa','Nhiệm vụ hoàn thành|Quay về gặp Trưởng Làng|Để nhận thưởng',0,0,0),(19,'Rèn luyện bản thân (dễ)',21,21,20,1,'1Ta thấy cậu tiền đồ sáng lạn<0Chỉ huy quá khen>1Hãy cố rèn luyện bản thân sau này sẽ nên người tài mà giúp ích cho đời.>1Nếu muốn rèn luyện thì đây là cách>1Đánh đủ số 50 con quái (+-5Lv Pokémon) rồi về đây gặp ta.>Cậu có muốn rèn luyện không ?','Đã đủ số quái ta yêu cầu chưa ?','1Cậu hoàn thành tốt đấy, cố gắng hơn nữa nhé.>0Dạ ','32000','1',4,'32000','50',2000,500000,'32000','32000','32000',5,'Nhiệm vụ:|Hạ 50 quái +-5Lv|NPC nv:|Blaziken|Phần thưởng:|500000 exp','Nhiệm vụ hoàn thành|Quay về gặp Blaziken|Để nhận thưởng',30,0,0),(20,'Rèn luyện bản thân (vừa)',21,21,20,1,'1Ta thấy cậu tiền đồ sáng lạn>0Chỉ huy quá khen>1Hãy cố rèn luyện bản thân sau này sẽ nên người tài mà giúp ích cho đời.>1Nếu muốn rèn luyện thì đây là cách>1Đánh đủ số 100 con quái (+-5Lv Pokémon) rồi về đây gặp ta.>Cậu có muốn rèn luyện không ?','Đã đủ số quái ta yêu cầu chưa ?','1Cậu hoàn thành tốt đấy, cố gắng hơn nữa nhé.>0Dạ ','32000','32000',4,'32000','100',0,1200000,'32000','32000','32000',5,'Nhiệm vụ:|Hạ 100 quái +-5Lv|NPC nv:|Blaziken|Phần thưởng:|1200000 exp','Nhiệm vụ hoàn thành|Quay về gặp Blaziken|Để nhận thưởng',60,0,0),(21,'Rèn luyện bản thân (khó)',21,21,20,1,'1Ta thấy cậu tiền đồ sáng lạn<0Chỉ huy quá khen>1Hãy cố rèn luyện bản thân sau này sẽ nên người tài mà giúp ích cho đời.>1Nếu muốn rèn luyện thì đây là cách>1Đánh đủ số 150 con quái (+-5Lv Pokémon) rồi về đây gặp ta.>Cậu có muốn rèn luyện không ?','Đã đủ số quái ta yêu cầu chưa ?','1Cậu hoàn thành tốt đấy, cố gắng hơn nữa nhé.>0Dạ ','32000','32000',4,'32000','150',0,2000000,'32000','32000','32000',5,'Nhiệm vụ:|Hạ 150 quái +-5Lv|NPC nv:|Blaziken|Phần thưởng:|2000000 exp','Nhiệm vụ hoàn thành|Quay về gặp Blaziken|Để nhận thưởng',100,0,0),(22,'Rèn luyện thể lực (dễ)',21,21,30,1,'1Thế giới Pokémon hiểm ác biết bao nhiêu nguy hiểm tiềm ẩn>1muốn phiêu lưu cần phải có một cơ thể khỏe mạnh>1Hãy tiếp tục rèn luyện bản thân đi.>0Dạ!<1Đi đánh 85 con quái (+-5 Lv Pokémon) rồi về đây ta gặp ta.>Cậu có muốn thực hiện việc này không?','Cố gắng lên, cậu vẫn chưa hoàn thành nhiệm vụ đâu !','1Tốt rồi, nhận chút quà của ta xem như khích lệ nhé>0Vâng xin cảm ơn chỉ huy','32000','32000',4,'32000','85',0,1200000,'32000','32000','32000',5,'Nhiệm vụ:|Hạ 85 quái +-5Lv|NPC nv:|Blaziken|Phần thưởng:|1200000 exp','Nhiệm vụ hoàn thành|Quay về gặp Blaziken|Để nhận thưởng',30,0,0),(23,'Rèn luyện thể lực (vừa)',21,21,30,1,'1Thế giới Pokémon hiểm ác biết bao nhiêu nguy hiểm tiềm ẩn>1muốn phiêu lưu cần phải có một cơ thể khỏe mạnh>1Hãy tiếp tục rèn luyện bản thân đi.>0Dạ!<1Đi đánh 170 con quái (+-5 Lv Pokémon) rồi về đây ta gặp ta.>Cậu có muốn thực hiện việc này không?','Cố gắng lên, cậu vẫn chưa hoàn thành nhiệm vụ đâu !','1Tốt rồi, nhận chút quà của ta xem như khích lệ nhé>0Vâng xin cảm ơn chỉ huy','32000','32000',4,'32000','170',0,2500000,'32000','32000','32000',5,'Nhiệm vụ:|Hạ 170 quái +-5Lv|NPC nv:|Blaziken|Phần thưởng:|2500000 exp','Nhiệm vụ hoàn thành|Quay về gặp Blaziken|Để nhận thưởng',60,0,0),(24,'Rèn luyện thể lực (khó)',21,21,30,1,'1Thế giới Pokémon hiểm ác biết bao nhiêu nguy hiểm tiềm ẩn>1muốn phiêu lưu cần phải có một cơ thể khỏe mạnh>1Hãy tiếp tục rèn luyện bản thân đi.>0Dạ!<1Đi đánh 250 con quái (+-5 Lv Pokémon) rồi về đây ta gặp ta.>Cậu có muốn thực hiện việc này không?','Cố gắng lên, cậu vẫn chưa hoàn thành nhiệm vụ đâu !','1Tốt rồi, nhận chút quà của ta xem như khích lệ nhé>0Vâng xin cảm ơn chỉ huy','32000','32000',4,'32000','250',0,4000000,'32000','32000','32000',5,'Nhiệm vụ:|Hạ 250 quái +-5Lv|NPC nv:|Blaziken|Phần thưởng:|4000000 exp','Nhiệm vụ hoàn thành|Quay về gặp Blaziken|Để nhận thưởng',100,0,0),(25,'Dẹp loạn khu hoang dã (dễ)',21,21,40,1,'1Cướp bóc, quái vật đang nổi lên hoành hành khắp mọi nơi>1thiết nghĩ phải có người ra tay tiêu diệt.>0Đó là điều nên làm ạ>1Ta thấy cậu có thể đảm đương việc này>1Tiêu diệt 117 con quái (+-5Lv Pokémon) quay về ta trọng thưởng>Cậu có muốn làm việc này không ?','Hoàn thành sớm đi còn về nhận thưởng','1Hãy nhận lấy chút phần thưởng nhé','32000','32000',4,'32000','117',0,3000000,'32000','32000','32000',5,'Nhiệm vụ:|Hạ 117 quái +-5Lv|NPC nv:|Blaziken|Phần thưởng:|3000000 exp','Nhiệm vụ hoàn thành|Quay về gặp Blaziken|Để nhận thưởng',30,0,0),(26,'Dẹp loạn khu hoang dã (vừa)',21,21,40,1,'1Cướp bóc, quái vật đang nổi lên hoành hành khắp mọi nơi>1thiết nghĩ phải có người ra tay tiêu diệt.>0Đó là điều nên làm ạ>1Ta thấy cậu có thể đảm đương việc này>1Tiêu diệt 234 con quái (+-5Lv Pokémon) quay về ta trọng thưởng>Cậu có muốn làm việc này không ?','Hoàn thành sớm đi còn về nhận thưởng','1Hãy nhận lấy chút phần thưởng nhé','32000','32000',4,'32000','234',0,6000000,'32000','32000','32000',5,'Nhiệm vụ:|Hạ 234 quái +-5Lv|NPC nv:|Blaziken|Phần thưởng:|6000000 exp','Nhiệm vụ hoàn thành|Quay về gặp Blaziken|Để nhận thưởng',60,0,0),(27,'Dẹp loạn khu hoang dã (khó)',21,21,40,1,'1Cướp bóc, quái vật đang nổi lên hoành hành khắp mọi nơi>1thiết nghĩ phải có người ra tay tiêu diệt.>0Đó là điều nên làm ạ>1Ta thấy cậu có thể đảm đương việc này>1Tiêu diệt 350 con quái (+-5Lv Pokémon) quay về ta trọng thưởng>Cậu có muốn làm việc này không ?','Hoàn thành sớm đi còn về nhận thưởng','1Hãy nhận lấy chút phần thưởng nhé','32000','32000',4,'32000','350',0,10000000,'32000','32000','32000',5,'Nhiệm vụ:|Hạ 350 quái +-5Lv|NPC nv:|Blaziken|Phần thưởng:|10000000 exp','Nhiệm vụ hoàn thành|Quay về gặp Blaziken|Để nhận thưởng',100,0,0),(28,'Giúp đỡ mọi người (dễ)',-29,-29,50,1,'1Trên đường phiêu lưu cố gắng tiêu diệt quái vật càng nhiều càng tốt>1đó cũng là một cách bảo vệ dân làng>0Tôi xin ghi nhớ lời chỉ huy dặn>1Khi nào đủ số 135 con quái (+-5Lv Pokémon) về gặp ta nhận thưởng>Cậu có muốn nhận nhiệm vụ này không?','Ta vừa xem qua, vẫn chưa đủ số quái so với yêu cầu. Tiếp tục đi nhé!','1Cậu thật có lòng, ta xin thay mặt mọi người cảm ơn cậu','32000','32000',4,'32000','135',0,6000000,'32000','32000','32000',5,'Nhiệm vụ:|Hạ 135 quái +-5Lv|NPC nv:|Heracross|Phần thưởng:|6000000 exp','Nhiệm vụ hoàn thành|Quay về gặp Heracross|Để nhận thưởng',30,0,0),(29,'Giúp đỡ mọi người (vừa)',-29,-29,50,1,'1Trên đường phiêu lưu cố gắng tiêu diệt quái vật càng nhiều càng tốt>1đó cũng là một cách bảo vệ dân làng>0Tôi xin ghi nhớ lời chỉ huy dặn>1Khi nào đủ số 270 con quái (+-5Lv Pokémon) về gặp ta nhận thưởng>Cậu có muốn nhận nhiệm vụ này không?','Ta vừa xem qua, vẫn chưa đủ số quái so với yêu cầu. Tiếp tục đi nhé!','1Cậu thật có lòng, ta xin thay mặt mọi người cảm ơn cậu','32000','32000',4,'32000','270',0,16000000,'32000','32000','32000',5,'Nhiệm vụ:|Hạ 270 quái +-5Lv|NPC nv:|Heracross|Phần thưởng:|16000000 exp','Nhiệm vụ hoàn thành|Quay về gặp Heracross|Để nhận thưởng',60,0,0),(30,'Giúp đỡ mọi người (khó)',-29,-29,50,1,'1Trên đường phiêu lưu cố gắng tiêu diệt quái vật càng nhiều càng tốt>1đó cũng là một cách bảo vệ dân làng>0Tôi xin ghi nhớ lời chỉ huy dặn>1Khi nào đủ số 400 con quái (+-5Lv Pokémon) về gặp ta nhận thưởng>Cậu có muốn nhận nhiệm vụ này không?','Ta vừa xem qua, vẫn chưa đủ số quái so với yêu cầu. Tiếp tục đi nhé!','1Cậu thật có lòng, ta xin thay mặt mọi người cảm ơn cậu','32000','32000',4,'32000','400',0,25000000,'32000','32000','32000',5,'Nhiệm vụ:|Hạ 400 quái +-5Lv|NPC nv:|Heracross|Phần thưởng:|25000000 exp','Nhiệm vụ hoàn thành|Quay về gặp Heracross|Để nhận thưởng',100,0,0),(31,'Bảo vệ dân làng (dễ)',-29,-29,60,1,'1Cậu hãy đi giúp những người dân ở vùng sâu vùng xa, Pokémon hung dữ còn nhiều>Có như vậy họ mới an cư lạc nghiệp được>0Chí phải ạ!>1Tiêu diệt 150 con quái (+-5Lv Pokémon) sau đó về đây báo công nhận thưởng.>Việc này rất gian nan, cậu có muốn thực hiện không?','Đi sớm cho kịp, họ đang đợi cậu đến giúp đó.','1Cậu thật vất vả, sẽ có phần thưởng xứng đáng cho cậu>0Vâng, xin cảm ơn!','32000','32000',4,'32000','150',0,24000000,'32000','32000','32000',5,'Nhiệm vụ:|Hạ 150 quái +-5Lv|NPC nv:|Heracross|Phần thưởng:|24000000 exp','Nhiệm vụ hoàn thành|Quay về gặp Heracross|Để nhận thưởng',30,0,0),(32,'Bảo vệ dân làng (vừa)',-29,-29,60,1,'1Cậu hãy đi giúp những người dân ở vùng sâu vùng xa, Pokémon hung dữ còn nhiều>Có như vậy họ mới an cư lạc nghiệp được>0Chí phải ạ!>1Tiêu diệt 300 con quái (+-5Lv Pokémon) sau đó về đây báo công nhận thưởng.>Việc này rất gian nan, cậu có muốn thực hiện không?','Đi sớm cho kịp, họ đang đợi cậu đến giúp đó.','1Cậu thật vất vả, sẽ có phần thưởng xứng đáng cho cậu>0Vâng, xin cảm ơn!','32000','32000',4,'32000','300',0,50000000,'32000','32000','32000',5,'Nhiệm vụ:|Hạ 300 quái +-5Lv|NPC nv:|Heracross|Phần thưởng:|50000000 exp','Nhiệm vụ hoàn thành|Quay về gặp Heracross|Để nhận thưởng',60,0,0),(33,'Bảo vệ dân làng (khó)',-29,-29,60,1,'1Cậu hãy đi giúp những người dân ở vùng sâu vùng xa, Pokémon hung dữ còn nhiều>Có như vậy họ mới an cư lạc nghiệp được>0Chí phải ạ!>1Tiêu diệt 450 con quái (+-5Lv Pokémon) sau đó về đây báo công nhận thưởng.>Việc này rất gian nan, cậu có muốn thực hiện không?','Đi sớm cho kịp, họ đang đợi cậu đến giúp đó.','1Cậu thật vất vả, sẽ có phần thưởng xứng đáng cho cậu>0Vâng, xin cảm ơn!','32000','32000',4,'32000','450',0,80000000,'32000','32000','32000',5,'Nhiệm vụ:|Hạ 450 quái +-5Lv|NPC nv:|Heracross|Phần thưởng:|80000000 exp','Nhiệm vụ hoàn thành|Quay về gặp Heracross|Để nhận thưởng',100,0,0),(34,'Đẩy lùi Pokémon hung dữ (dễ)',-29,-29,70,1,'1Pokémon hung dữ xuất hiện khắp nơi, việc tiêu diệt không thể chậm trễ>1Nếu để chúng sinh sôi nãy nở thì sẽ thành họa cho muôn dân>0Dạ!>1Giúp ta hạ 167 con quái (+-5Lv Pokémon) ta sẽ trọng thưởng>Cậu có muốn làm việc này không ?','Sao còn đứng đây ?','1Cậu đã lập công to rồi đấy, cố gắng phát huy nhé.>0Xin vâng!','32000','32000',4,'32000','167',0,40000000,'32000','32000','32000',5,'Nhiệm vụ:|Hạ 167 quái +-5Lv|NPC nv:|Heracross|Phần thưởng:|40000000 exp','Nhiệm vụ hoàn thành|Quay về gặp Heracross|Để nhận thưởng',30,0,0),(35,'Đẩy lùi Pokémon hung dữ (vừa)',-29,-29,70,1,'1Pokémon hung dữ xuất hiện khắp nơi, việc tiêu diệt không thể chậm trễ>1Nếu để chúng sinh sôi nãy nở thì sẽ thành họa cho muôn dân>0Dạ!>1Giúp ta hạ 334 con quái (+-5Lv Pokémon) ta sẽ trọng thưởng>Cậu có muốn làm việc này không ?','Sao còn đứng đây ?','1Cậu đã lập công to rồi đấy, cố gắng phát huy nhé.>0Xin vâng!','32000','32000',4,'32000','334',0,90000000,'32000','32000','32000',5,'Nhiệm vụ:|Hạ 334 quái +-5Lv|NPC nv:|Heracross|Phần thưởng:|90000000 exp','Nhiệm vụ hoàn thành|Quay về gặp Heracross|Để nhận thưởng',60,0,0),(36,'Đẩy lùi Pokémon hung dữ (khó)',-29,-29,70,1,'1Pokémon hung dữ xuất hiện khắp nơi, việc tiêu diệt không thể chậm trễ>1Nếu để chúng sinh sôi nãy nở thì sẽ thành họa cho muôn dân>0Dạ!>1Giúp ta hạ 500 con quái (+-5Lv Pokémon) ta sẽ trọng thưởng>Cậu có muốn làm việc này không ?','Sao còn đứng đây ?','1Cậu đã lập công to rồi đấy, cố gắng phát huy nhé.>0Xin vâng!','32000','32000',4,'32000','500',0,150000000,'32000','32000','32000',5,'Nhiệm vụ:|Hạ 500 quái +-5Lv|NPC nv:|Heracross|Phần thưởng:|150000000 exp','Nhiệm vụ hoàn thành|Quay về gặp Heracross|Để nhận thưởng',100,0,0),(37,'Răng Croconaw',-3,-3,1,2,'1Ta đang làm một chiếc vòng cổ may mắn từ những chiếc răng mà Croconaw thay ra>1nhưng hiện tại ta đang bận việc.>1Cậu hãy đánh bại 100 con Croconaw và nhặt giúp ta những chiếc răng chúng rơi lại nhé.>Cậu có đồng ý không?','Sao còn đứng đây ?','1Cậu đã lập công to rồi đấy, cố gắng phát huy nhé.>0Xin vâng!','7','29',2,'100','32000',0,5,'32000','32000','32000',0,'Nhiệm vụ:|Hạ 100 con Croconaw|Địa điểm:|Cinnabar|NPC nv:|Lucario|Phần thưởng:|5exp đội','Nhiệm vụ hoàn thành|Về gặp Lucario|Nhận thưởng',0,0,0),(38,'Nọc Croagunk',-3,-3,1,2,'1Croagunk có túi nọc độc rất quý, dùng để điều chế thuốc giải độc.>1Ta cần nọc của 100 con Croagunk.>Cậu có thể giúp ta không?','Sao còn đứng đây ?','1Cậu đã lập công to rồi đấy, cố gắng phát huy nhé.>0Xin vâng!','8','30',2,'100','32000',0,5,'32000','32000','32000',0,'Nhiệm vụ:|Hạ 100 con Croagunk|Địa điểm:|Cinnabar|NPC nv:|Lucario|Phần thưởng:|5exp đội','Nhiệm vụ hoàn thành|Về gặp Lucario|Nhận thưởng',0,0,0),(39,'Đua sức với Dodrio',-3,-3,1,2,'1Nghe nói Dodrio ở vùng đảo New Bark chạy nhanh như gió,>1rất hợp để các Pokémon trẻ luyện sức bền.>1Hãy đánh bại 100 con Dodrio để rèn luyện nhé.>Cậu có làm được không?','Sao còn đứng đây ?','1Cậu đã lập công to rồi đấy, cố gắng phát huy nhé.>0Xin vâng!','9','22',2,'100','32000',0,5,'32000','32000','32000',0,'Nhiệm vụ:|Hạ 100 con Dodrio|Địa điểm:|New Bark|NPC nv:|Lucario|Phần thưởng:|5exp đội','Nhiệm vụ hoàn thành|Về gặp Lucario|Nhận thưởng',0,0,0),(40,'Bắt Kricketune',-3,-3,1,2,'1Kricketune ở Rừng Viridian kêu rất hay,>1sắp tới có tiệc chiêu đãi các nhà huấn luyện của các đội.>1Ta muốn mời chúng đến biểu diễn, nhưng trước hết cậu phải thắng 100 con Kricketune đã.>Cậu có thể giúp ta không?','Sao còn đứng đây ?','1Cậu đã lập công to rồi đấy, cố gắng phát huy nhé.>0Xin vâng!','32000','31',0,'32000','100',0,5,'32000','32000','32000',0,'Nhiệm vụ:|Hạ 100 con Kricketune|Địa điểm:|Rừng Viridian|NPC nv:|Lucario|Phần thưởng:|5exp đội','Nhiệm vụ hoàn thành|Về gặp Lucario|Nhận thưởng',0,0,0),(41,'Thuần hoá Tauros',-3,-3,1,2,'1Ta nghe nói một con Tauros khoẻ gấp 3 lần con trâu nhà,> 1giá mà mang được loài này về thuần hoá phục vụ cho việc cầy bừa thì thật là tốt.> Cậu có thể thuần hoá 100 con Tauros cho ta không ?','Sao còn đứng đây ?','1Cậu đã lập công to rồi đấy, cố gắng phát huy nhé.>0Xin vâng!','32000','33',0,'32000','100',0,5,'32000','32000','32000',0,'Nhiệm vụ:|Hạ 100 con Tauros|Địa điểm:|Rừng Viridian|NPC nv:|Lucario|Phần thưởng:|5exp đội','Nhiệm vụ hoàn thành|Về gặp Lucario|Nhận thưởng',0,0,0),(42,'Trừng trị Scraggy',-3,-3,1,2,'1Bọn Scraggy dạo này rất hoành hành, >1chúng thường chặn đường cướp bóc người qua lại >1trên các con đường trọng yếu giữa các vùng.>1Đây đang là một vấn nạn cho mọi người,>1buộc phải dẹp yên.>1Theo nguồn tin ta có được>1sào huyệt của bọn chúng tại phía bắc Rừng Viridian,>1chúng có khoảng 100 tên.>Cậu có sẵn sàng tiêu diệt hết bọn chúng cho ta không ?','Sao còn đứng đây ?','1Cậu đã lập công to rồi đấy, cố gắng phát huy nhé.>0Xin vâng!','32000','34',0,'32000','100',0,5,'32000','32000','32000',0,'Nhiệm vụ:|Hạ 100 tên Scraggy|Địa điểm:|Rừng Viridian|NPC nv:|Lucario|Phần thưởng:|5exp đội','Nhiệm vụ hoàn thành|Về gặp Lucario|Nhận thưởng',0,0,0),(43,'Trừng trị Crawdaunt',-3,-3,1,2,'1Vùng biển New Bark rất hay xuất hiện Crawdaunt,>1chúng thường tấn công vào các tàu đánh cá của ngư dân>1cướp bóc không thương tiếc.>1Hãy diệt hết bọn chúng >1mang bình yên lại cho ngư dân lương thiện nhé.>1Ít nhất phải mang về đây cho ta 100 tên đấy>Cậu có muốn làm việc này không ?','Sao còn đứng đây ?','1Cậu đã lập công to rồi đấy, cố gắng phát huy nhé.>0Xin vâng!','32000','35',0,'32000','100',0,5,'32000','32000','32000',0,'Nhiệm vụ:|Hạ 100 con Crawdaunt|Địa điểm:|Rừng Viridian|NPC nv:|Lucario|Phần thưởng:|5exp đội','Nhiệm vụ hoàn thành|Về gặp Lucario|Nhận thưởng',0,0,0),(44,'Tìm Whirlipede',-3,-3,1,2,'1Whirlipede ở Núi Mặt Trăng tiết ra một loại chất dịch>1kết hợp với thảo dược sẽ thành thuốc chữa thương rất hiệu quả.>1Cậu hãy giúp ta đánh bại 100 con Whirlipede để thu chất dịch đó nhé.>Liệu cậu có giúp ta không?','Sao còn đứng đây ?','1Cậu đã lập công to rồi đấy, cố gắng phát huy nhé.>0Xin vâng!','3','40',2,'100','32000',0,10,'32000','32000','32000',0,'Nhiệm vụ:|Hạ 100 con Whirlipede|Địa điểm:|Núi Mặt Trăng|NPC nv:|Lucario|Phần thưởng:|5exp đội','Nhiệm vụ hoàn thành|Về gặp Lucario|Nhận thưởng',0,0,0),(45,'Tìm Farfetch\'d',-3,-3,1,2,'1Những chiếc lông vũ Farfetch\'d thay ra rất bền và đẹp,>1dùng để trang trí đồ phòng thủ thì không gì bằng.>1Cậu hãy giúp ta mang về 100 chiếc lông vũ Farfetch\'d nhé.>1Cẩn thận, Farfetch\'d ở Núi Mặt Trăng rất hung dữ.>Cậu có muốn làm việc này không?','Sao còn đứng đây ?','1Cậu đã lập công to rồi đấy, cố gắng phát huy nhé.>0Xin vâng!','6','45',2,'100','32000',0,10,'32000','32000','32000',0,'Nhiệm vụ:|Hạ 100 con Farfetch\'d|Địa điểm:|Núi Mặt Trăng|NPC nv:|Lucario|Phần thưởng:|5exp đội','Nhiệm vụ hoàn thành|Về gặp Lucario|Nhận thưởng',0,0,0),(46,'Hơi ấm của Litleo',-3,-3,1,2,'1Khi trời trở lạnh ta thường đau nhức khắp người,>1thật sự ta đã già rồi.>1Nghe nói hơi ấm từ bờm lửa của Litleo giúp giảm đau rất tốt.>1Cậu hãy thắng 100 con Litleo để chúng chịu đến sưởi ấm cho ta nhé.>Cậu có muốn giúp ta không?','Sao còn đứng đây ?','1Cậu đã lập công to rồi đấy, cố gắng phát huy nhé.>0Xin vâng!','32000','63',0,'32000','100',0,10,'32000','32000','32000',0,'Nhiệm vụ:|Hạ 100 con Litleo|Địa điểm:|Vùng Safari|NPC nv:|Lucario|Phần thưởng:|5exp đội','Nhiệm vụ hoàn thành|Về gặp Lucario|Nhận thưởng',0,0,0),(47,'Đẩy lùi Pokémon hung dữ (dễ)',-29,-29,80,1,'1Pokémon hung dữ xuất hiện khắp nơi, việc tiêu diệt không thể chậm trễ>1Nếu để chúng sinh sôi nãy nở thì sẽ thành họa cho muôn dân>0Dạ!>1Giúp ta hạ 5000 con quái (+-5Lv Pokémon) ta sẽ trọng thưởng>Cậu có muốn làm việc này không ?','Sao còn đứng đây ?','1Cậu đã lập công to rồi đấy, cố gắng phát huy nhé.>0Xin vâng!','32000','32000',4,'32000','5000',0,150000000,'32000','32000','32000',0,'Nhiệm vụ:|Hạ 5000 quái +-5Lv|NPC nv:|Heracross|Phần thưởng:|150000000 exp','Nhiệm vụ hoàn thành|Quay về gặp Heracross|Để nhận thưởng',30,0,0),(48,'Đẩy lùi Pokémon hung dữ (vừa)',-29,-29,80,1,'1Pokémon hung dữ xuất hiện khắp nơi, việc tiêu diệt không thể chậm trễ>1Nếu để chúng sinh sôi nãy nở thì sẽ thành họa cho muôn dân>0Dạ!>1Giúp ta hạ 6000 con quái (+-5Lv Pokémon) ta sẽ trọng thưởng>Cậu có muốn làm việc này không ?','Sao còn đứng đây ?','1Cậu đã lập công to rồi đấy, cố gắng phát huy nhé.>0Xin vâng!','32000','32000',4,'32000','6000',0,200000000,'32000','32000','32000',0,'Nhiệm vụ:|Hạ 6000 quái +-5Lv|NPC nv:|Heracross|Phần thưởng:|200000000 exp','Nhiệm vụ hoàn thành|Quay về gặp Heracross|Để nhận thưởng',60,0,0),(49,'Đẩy lùi Pokémon hung dữ (khó)',-29,-29,80,1,'1Pokémon hung dữ xuất hiện khắp nơi, việc tiêu diệt không thể chậm trễ>1Nếu để chúng sinh sôi nãy nở thì sẽ thành họa cho muôn dân>0Dạ!>1Giúp ta hạ 7000 con quái (+-5Lv Pokémon) ta sẽ trọng thưởng>Cậu có muốn làm việc này không ?','Sao còn đứng đây ?','1Cậu đã lập công to rồi đấy, cố gắng phát huy nhé.>0Xin vâng!','32000','32000',4,'32000','7000',0,250000000,'32000','32000','32000',0,'Nhiệm vụ:|Hạ 7000 quái +-5Lv|NPC nv:|Heracross|Phần thưởng:|250000000 exp','Nhiệm vụ hoàn thành|Quay về gặp Heracross|Để nhận thưởng',100,0,0);
/*!40000 ALTER TABLE `data_quest` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `data_shop`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `data_shop` (
  `id` int(11) NOT NULL,
  `idImg` int(11) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `decript` varchar(255) DEFAULT NULL,
  `value` int(11) DEFAULT NULL,
  `price` int(11) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `tab` int(11) DEFAULT NULL,
  `sell` int(11) DEFAULT NULL,
  `priceSale` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `data_shop` WRITE;
/*!40000 ALTER TABLE `data_shop` DISABLE KEYS */;
INSERT INTO `data_shop` VALUES (0,11,'Vé giờ vàng 100','Tăng 100% kinh nghiệm',2,15,0,0,1,10),(1,12,'Vé giờ vàng 50','Tăng 50% kinh nghiệm',1,10,0,0,1,7),(2,9,'200 Siêu thuốc hồi máu','Hồi phục 1500 hp',200,10,0,0,1,7),(3,10,'200 Thuốc hồi máu cực mạnh','Hồi phục 3000 hp',200,15,0,0,1,10),(4,7,'200 Ether lớn','Hồi phục 2500 mp',200,10,0,0,1,7),(5,8,'200 Ether tối đa','Hồi phục 3500 mp',200,15,0,0,1,10),(6,20,'Kẹo tiềm năng','Khôi phục lại tiềm năng',1,50,0,0,1,35),(7,21,'Kẹo kỹ năng','Khôi phục lại kỹ năng',1,50,0,0,1,35),(8,32,'Vé bay Pidgeot','Cho phép bay tức thời đến 1 vị trí giặc trong vùng của mình',10,100000,1,1,1,100000),(9,22,'Tự động lượm đồ','',1,15,0,1,0,10),(10,32,'Phiếu công ích','Giảm trừ 100 điểm PK',1,1,0,1,1,1),(11,27,'Sáo gọi Ponyta','Tốc độ tăng 1 và tăng 700 MP và 700 HP',1,10,0,1,1,7),(12,33,'Huy hiệu lập đội','Huy hiệu dùng để tạo đội',1,250,0,1,1,175),(13,13,'Thẻ đấu trường','Thẻ vào đấu trường',10,1000,1,1,0,1000),(14,39,'Thẻ xác nhận','Thẻ xác nhận chiếm Gym',5,10,0,1,1,7),(15,37,'Sáo gọi Ponyta lửa','Cộng 3 điểm vào tất cả tiềm năng và 1000 HP MP',1,70,0,1,1,49),(18,113,'Lông đinh','Đổi thành lông đinh',1,3,0,2,1,2),(19,114,'Lông búi','Đổi thành lông búi',1,3,0,2,1,2),(20,115,'Lông cột cao','Đổi thành lông cột cao',1,3,0,2,1,2),(21,116,'Lông chéo','Đổi thành lông chéo',1,3,0,2,1,2),(22,117,'Lông xù','Đổi thành lông xù',1,3,0,2,1,2),(23,118,'Lông ngang vai','Đổi thành lông ngang vai',1,3,0,2,1,2),(24,119,'Lông đinh so le','Đổi thành lông đinh so le',1,3,0,2,1,2),(25,120,'Lông so le','Đổi thành lông so le',1,3,0,2,1,2),(26,121,'Lông gọn gàng','Đổi thành lông gọn gàng',1,3,0,2,1,2),(27,122,'Lông cài hoa','Đổi thành lông cài hoa',1,3,0,2,1,2),(28,123,'Bờm Ponyta','Đổi thành bờm Ponyta',1,3,0,2,1,2),(29,124,'Lông Clefairy','Đổi thành lông Clefairy',1,3,0,2,1,2),(30,125,'Lông dựng đứng','Đổi thành lông dựng đứng',1,3,0,2,1,2),(31,43,'Hoa Mai','Tặng 1 Hoa mai cho người khác',1,2,0,2,0,2),(32,43,'Hoa Mai','Tặng 10 Hoa mai cho người khác',1,15,0,2,0,8),(33,43,'Hoa Mai','Tặng 39 Hoa mai cho người khác',1,50,0,2,0,25),(34,43,'Hoa Mai','Tặng 99 Hoa mai cho người khác',1,99,0,2,0,50),(35,44,'Hoa đào','Hoa dùng để tặng bạn',1,2,0,2,0,2),(36,44,'Hoa đào','Hoa dùng để tặng bạn',1,15,0,2,0,8),(37,44,'Hoa đào','Hoa dùng để tặng bạn',1,50,0,2,0,25),(38,44,'Hoa đào','Hoa dùng để tặng bạn',1,99,0,2,0,50),(39,45,'Hoa hồng','Tặng 1 Hoa hồng cho người khác',1,2,0,2,1,2),(40,45,'Hoa hồng','Tặng 9 Hoa hồng cho người khác',1,15,0,2,1,8),(41,45,'Hoa hồng','Tặng 39 Hoa hồng cho người khác',1,60,0,2,1,30),(42,45,'Hoa hồng','Tặng 99 Hoa hồng cho người khác',1,120,0,2,1,60),(45,47,'Sáo gọi Tauros','Vật phẩm gọi thú cưỡi Tauros',1,300,0,1,1,210),(46,46,'Sáo gọi Arcanine','Vật phẩm gọi thú cưỡi Arcanine',1,300,0,1,1,210),(47,48,'Sáo gọi Mightyena','Vật phẩm gọi thú cưỡi Mightyena',1,300,0,1,1,210),(48,49,'Sáo gọi Pidgeot','Vật phẩm gọi thú cưỡi Pidgeot',1,300,0,1,1,210),(49,38,'Sáo gọi Rapidash','Vật phẩm gọi thú cưỡi Rapidash',1,300,0,1,1,210),(50,52,'Áo dài','Cộng 6 điểm vào tất cả tiềm năng và 3000 HP MP. Thời hạn 7 ngày',1,70,0,1,0,35),(51,55,'Áo Rayquaza','Cộng 6 điểm vào tất cả tiềm năng và 3000 HP MP. Thời hạn 7 ngày',1,70,0,1,0,70),(52,58,'Noel nữ','Cộng 10%tc 10%pt và 5000 HP MP',1,70,0,1,0,70),(53,54,'Áo Lucario','Cộng 8%tc 8%pt và 2000 HP MP. Thời hạn 7 ngày',1,100,0,1,0,70),(54,56,'Áo giáo sư','Cộng 6 điểm vào tất cả tiềm năng và 3000 HP MP. Thời hạn 7 ngày',1,70,0,1,0,49),(55,53,'Áo Lunala','Cộng 6 điểm vào tất cả tiềm năng và 3000 HP MP. Thời hạn 7 ngày',1,70,0,1,0,49),(56,51,'Áo Bellossom','Cộng 8%tc 8%pt và 2000 HP MP. Thời hạn 7 ngày',1,100,0,1,0,70),(57,59,'Noel nam','Cộng 10%tc 10%pt và 5000 HP MP',1,70,0,1,0,70),(58,50,'Nón noel','Cộng 5%tc 5%pt và 3000 HP MP',1,70,0,1,0,70),(65,31,'Thuốc X','Tăng 100% các chỉ số trong 1 phút',1,500000,1,0,1,500000),(72,32,'Vé vào khu mỏ','Vé vào khu mỏ để đào nguyện liệu.',1,50000,1,0,1,50000),(74,10,'200 Thuốc hồi máu 7000','Hồi phục  7000 hp',200,20,0,0,1,14),(75,10,'200 Thuốc hồi máu 15000','Hồi phục  15000 hp',200,25,0,0,1,17),(76,8,'200 Ether 7000','Hồi phục  7000 mp',200,20,0,0,1,14),(77,8,'200 Ether 15000','Hồi phục  15000 mp',200,25,0,0,1,17),(81,77,'Loa','Loa',10,30,0,1,1,15),(86,80,'Áo Darkrai nữ \n -Cộng 10%tc 10%pt và 3000 HP MP. Thời hạn 15 ngày','',1,700,0,1,0,700),(87,79,'Áo Clefairy \n -Cộng 10%tc 10%pt và 3000 HP MP. Thời hạn 7 ngày','',1,800,0,1,0,800),(88,81,'Áo Darkrai nam \n -Cộng 10%tc 10%pt và 3000 HP MP. Thời hạn 15 ngày','',1,700,0,1,0,700),(89,78,'Áo Clefable \n -Cộng 10%tc 10%pt  và 3000 HP MP. Thời hạn 7 ngày','',1,800,0,1,0,800),(90,83,'Đầu Pumpkaboo \n-Tăng 50% tỷ lệ xuất hiện thuộc tính lan và 3000 HP MP. Thời hạn 10 ngày','',1,100,0,1,0,100),(91,82,'Nón Mismagius \n -Tăng 50% tỷ lệ xuất hiện thuộc tính lan và 3000 HP MP. Thời hạn 10 ngày','',1,100,0,1,0,100),(92,84,'Zubat \n -Cộng 30% tấn công. Thời hạn 7 ngày','',1,90,0,1,0,90),(93,85,'Thức ăn tươi','tăng 2% phòng thủ. Thời hạn 4h',1,5,0,1,0,3),(94,86,'Thức ăn khô','tăng 2% phòng tấn công. Thời hạn 4h',1,5,0,1,0,3),(95,87,'Thức ăn tổng hợp','tăng 2% phòng tấn công,phòng thủ, thuộc tính đặc biệt. Thời hạn 4h',1,5,0,1,0,3),(104,31,'Thuốc X siêu cấp','Tăng 150% các chỉ số trong 30s',1,5000000,1,0,0,5000000),(105,216,'Áo dài nữ \n -Cộng 10%tc 10%pt và 3000 HP MP. Thời hạn 7 ngày','',1,100,0,1,0,100),(106,217,'Áo dài nam \n -Cộng 10%tc 10%pt và 3000 HP MP. Thời hạn 7 ngày','',1,100,0,1,0,100),(107,94,'Pet Sandshrew \n -Tăng 10%exp,10% tấn công,10% phòng thủ, x3 thuộc tính băng lan. Thời hạn 7 ngày','',1,100,0,1,0,50),(108,23,'Bình tăng tốc bạc.','Giảm 20% thời gian chín của trái bạc',1,5,0,0,1,3),(109,88,'Bình tăng tốc vàng.','Giảm 20% thời gian chín của trái vàng',1,10,0,0,1,5),(110,241,'Pet Pachirisu \n -Tăng 10%exp,10% tấn công,10% phòng thủ, x3 thuộc tính sét lan. Thời hạn 7 ngày','',1,100,0,1,0,50),(111,96,'Pet Nidoran \n -Tăng 10%exp,10% tấn công,10% phòng thủ, x3 thuộc tính độc lan. Thời hạn 7 ngày','',1,100,0,1,0,50),(112,97,'Phi phong world cup đặc biệt','Chỉ số max. Thời hạn 7 ngày',1,90,0,1,0,90),(113,98,'áo brazin nam \n -Cộng 10%tc 10%pt và 3000 HP MP. Tăng 100% băng lan, sét lan, độc lan. Thời hạn 7 ngày','',1,100,0,1,0,50),(114,98,'áo brazin nữ \n -Cộng 10%tc 10%pt và 3000 HP MP. Tăng 100% băng lan, sét lan, độc lan. Thời hạn 7 ngày','',1,100,0,1,0,50),(115,97,'Phi phong world cup vật lý \n -Chỉ số max. Thời hạn 7 ngày','',1,90,0,1,0,90),(116,100,'Vải cấp 4 khoá \n - Dùng chế tạo trang bị','',1,15,0,2,1,15),(117,100,'Vải cấp 5 khoá \n - Dùng chế tạo trang bị','',1,45,0,2,1,45),(118,101,'Sắt cấp 4 khoá \n - Dùng chế tạo trang bị','',1,15,0,2,1,15),(119,101,'Sắt cấp 5 khoá \n - Dùng chế tạo trang bị','',1,45,0,2,1,45),(120,102,'Ngọc cấp 4 khoá \n - Dùng chế tạo trang bị','',1,15,0,2,1,15),(121,102,'Ngọc cấp 5 khoá \n - Dùng chế tạo trang bị','',1,45,0,2,1,45),(122,103,'Gỗ thường cấp 4 khoá \n - Dùng chế tạo trang bị','',1,15,0,2,1,15),(123,103,'Gỗ thường cấp 5 khoá \n - Dùng chế tạo trang bị','',1,45,0,2,1,45),(124,104,'Da mềm cấp 4 khoá \n - Dùng chế tạo trang bị','',1,15,0,2,1,15),(125,104,'Da mềm cấp 5 khoá \n - Dùng chế tạo trang bị','',1,45,0,2,1,45),(126,11,'Vé giờ vàng  \n - Tăng 100% exp trong 1 giờ chơi. Tính theo thời gian chơi','',1,25,0,2,1,25),(127,10,'200 Thuốc hồi máu 15k \n - Hồi phục 15000 hp','',200,50,0,2,1,50),(128,8,'200 Ether 15k \n - Hồi phục 15000 mp','',200,50,0,2,1,50),(129,105,'Thẻ đổi hàng \n - Đổi màu hàng đang nhận','',5,2000000,1,0,1,2000000),(130,106,'Thẻ đổi hàng cao cấp \n - Đổi màu hàng đang nhận','',10,20,0,0,1,20),(131,107,'Thẻ giao hàng \n - Thêm 1 lượt giao hàng khi sử dụng','',1,1000000,1,0,1,1000000),(132,251,'Bạc hà tính cách \n - Thay đổi phẩm chất thú cưng','',1,10,0,0,1,10),(133,169,'Pet Piplup \n -Tăng 10%exp,10% tấn công,10% phòng thủ, tăng 100% thuộc tính độc lan, băng lan, sét lan, 2% bạo kích. Thời hạn 7 ngày','',1,100,0,0,0,100),(134,181,'Nhánh hoa hồng \n - Gửi lời chúc đến bạn bè nhân dịp 8/3','',1,10,0,0,0,10),(135,112,'Socola \n - Dùng tặng cho con nít.','',10,125,0,0,0,125),(136,186,'Lá dong','',1,10000,1,0,0,10000),(137,231,'Băng tuyết nam \n -Cộng 10%tc 10%pt và 5000 HP MP.Tăng 100% băng lan, sét lan, độc lan, lửa lan','',1,70,0,1,0,70),(138,232,'Băng tuyết nữ \n -Cộng 10%tc 10%pt và 5000 HP MP.Tăng 100% băng lan, sét lan, độc lan, lửa lan','',1,70,0,1,0,70),(139,192,'Áo Magby nam \n -Cộng 10%tc 10%pt và 5000 HP MP.Tăng 100% băng lan, sét lan, độc lan, lửa lan. Thời hạn 60 ngày','',1,111,0,0,0,111),(140,192,'Áo Magby nữ \n -Cộng 10%tc 10%pt và 5000 HP MP.Tăng 100% băng lan, sét lan, độc lan, lửa lan. Thời hạn 60 ngày','',1,111,0,0,0,111),(141,198,'Lồng đèn rồng \n -Tăng 100% băng,sét,độc,lửa lan + 10% exp. Thời hạn 7 ngày','',1,700,0,1,0,700),(142,253,'Áo Banette \n -Cộng 10%tc 10%pt và 5000 HP MP.Tăng 100% băng lan, sét lan, độc lan, lửa lan','',1,70,0,1,0,70),(143,252,'Áo Dusknoir \n -Cộng 10%tc 10%pt và 5000 HP MP.Tăng 100% băng lan, sét lan, độc lan, lửa lan','',1,70,0,1,0,70),(144,254,'Áo Alakazam \n -Cộng 10%tc 10%pt và 5000 HP MP.Tăng 100% băng lan, sét lan, độc lan, lửa lan','',1,70,0,1,0,70),(145,255,'Áo Giratina \n -Cộng 10%tc 10%pt và 5000 HP MP.Tăng 100% băng lan, sét lan, độc lan, lửa lan','',1,70,0,1,0,70),(146,246,'Poké Ball \n - Dùng bắt thú nuôi','',1,100,0,0,0,100),(147,246,'Poké Ball \n - Dùng bắt thú nuôi','',3,1000000000,1,0,0,10000000),(148,64,'Pháo hoa tết\n- Vật phẩm sự kiện','',1,100,0,0,1,100),(149,64,'Pháo hoa tết\n- Vật phẩm sự kiện','',1,10000000,1,0,1,10000000),(150,141,'Túi nhặt ngọc \n -Vật phẩm sự kiện','',1,50,0,2,0,50),(151,142,'Gậy giáng sinh \n -Vật phẩm sự kiện. Thời hạn đến hết ngày tính từ lúc mua','',1,5000000,1,0,0,5000000),(152,143,'Suicune \n - Vật phẩm sự kiện. Thời hạn đến hết ngày tính từ lúc mua','',1,2,0,0,0,2),(153,144,'Rương may mắn \n - Vật phẩm sự kiện. Thời hạn 15 phút','',1,250,0,0,0,250),(154,128,'Lông thời trang nữ \n - Dùng thay đổi kiểu lông','',29,5,0,2,1,5),(155,130,'Lông thời trang nữ \n - Dùng thay đổi kiểu lông','',31,5,0,2,1,5),(156,132,'Lông thời trang nữ \n - Dùng thay đổi kiểu lông','',33,5,0,2,1,5),(157,134,'Lông thời trang nữ \n - Dùng thay đổi kiểu lông','',35,5,0,2,1,5),(158,136,'Lông thời trang nữ \n - Dùng thay đổi kiểu lông','',37,5,0,2,1,5),(159,138,'Lông thời trang nữ \n - Dùng thay đổi kiểu lông','',39,5,0,2,1,5),(160,140,'Lông thời trang nữ \n - Dùng thay đổi kiểu lông','',41,5,0,2,1,5),(161,131,'Lông thời trang nam \n - Dùng thay đổi kiểu lông','',32,5,0,2,1,5),(162,133,'Lông thời trang nam \n - Dùng thay đổi kiểu lông','',34,5,0,2,1,5),(163,137,'Lông thời trang nam \n - Dùng thay đổi kiểu lông','',38,5,0,2,1,5),(164,139,'Lông thời trang nam \n - Dùng thay đổi kiểu lông','',40,5,0,2,1,5),(165,250,'Kẹo hiếm \n -Vật phẩm nuôi thú cưng','',1,0,0,0,0,0),(166,150,'Áo Groudon \n -Tăng 50% băng-sét-độc-lửa lan, tăng công 10% , tăng 5.000 hp-mp, có hiệu ứng. Thời hạn 24h','',1,3,0,0,0,2),(167,150,'Áo Groudon \n -Tăng 50% băng-sét-độc-lửa lan, tăng công 10% , tăng 5.000 hp-mp, có hiệu ứng. Thời hạn 24h','',1,3000000,1,0,0,3000000),(168,151,'Áo Kyogre \n -Tăng 50% băng-sét-độc-lửa lan, tăng công 10% , tăng 5.000 hp-mp, có hiệu ứng. Thời hạn 24h','',1,3,0,0,0,2),(169,151,'Áo Kyogre \n -Tăng 50% băng-sét-độc-lửa lan, tăng công 10% , tăng 5.000 hp-mp, có hiệu ứng. Thời hạn 24h','',1,3000000,1,0,0,3000000),(170,154,'Áo Latias \n -Tăng 50% băng-sét-độc-lửa lan, tăng công 10% , tăng 5.000 hp-mp, có hiệu ứng. Thời hạn 24h','',1,3,0,0,0,3),(171,155,'Áo Latios\n -Tăng 50% băng-sét-độc-lửa lan, tăng công 10% , tăng 5.000 hp-mp, có hiệu ứng. Thời hạn 24h','',1,3,0,0,0,3),(172,63,'Lá thư của Volbeat','',10,25,0,0,0,25),(173,143,'Suicune','Vật phẩm sự kiện. Thời hạn 7 ngày',1,10000,0,0,0,10000),(174,164,'Áo Solgaleo \n - Tăng 50% băng-sét-độc-lửa lan, tăng công 10% , tăng 5.000 hp-mp.\n- Ánh Trăng gây st 10%-15% maxhp.\n- Tỷ lệ xh :10%\n- Thời hạn 7 ngày','',1,1000,0,0,0,1000),(175,165,'Áo Lunala \n - Tăng 50% băng-sét-độc-lửa lan, tăng công 10% , tăng 5.000 hp-mp.\n- Ánh Trăng gây st 10%-15% maxhp.\n- Tỷ lệ xh :10%\n- Thời hạn 7 ngày','',1,1000,0,0,0,1000),(176,191,'Bản đồ khoá \n -Dùng vào Núi châu báu','',1,100,0,2,0,100),(177,214,'Mảnh bản đồ 2 \n -Vật phẩm sự kiện','',10,20,0,0,0,20),(178,167,'Áo dài nam \n -Cộng 10%tc 10%pt và 5000 HP MP.Tăng 100% băng lan, sét lan, độc lan, lửa lan. Thời hạn 7 ngày','',1,70,0,0,0,70),(179,168,'áo dài nữ \n -Cộng 10%tc 10%pt và 5000 HP MP.Tăng 100% băng lan, sét lan, độc lan, lửa lan. Thời hạn 7 ngày','',1,70,0,0,0,70),(180,242,'Bóng đá nam\n- Nhân đôi tỉ lệ rơi nhánh hoa hồng vàng','',1,1000,0,0,0,1000),(181,243,'Bóng đá nữ\n- Nhân đôi tỉ lệ rơi nhánh hoa hồng vàng','',1,1000,0,0,0,1000);
/*!40000 ALTER TABLE `data_shop` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `dieu_khien_server`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dieu_khien_server` (
  `id` tinyint(3) unsigned NOT NULL,
  `muon_chay` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1 = muon server dang chay, 0 = muon tat han',
  `gio_hen` datetime DEFAULT NULL COMMENT 'NULL = ap dung ngay; co gio = doi den gio nay watcher moi ap dung',
  `da_ap_dung` tinyint(1) NOT NULL DEFAULT 1 COMMENT '0 = dang cho watcher xu ly, 1 = da xong (khong con viec gi cho watcher)',
  `trang_thai_thuc_te` varchar(20) DEFAULT NULL COMMENT 'watcher tu ghi lai: active/inactive/failed/... (ket qua systemctl is-active)',
  `lan_kiem_cuoi` datetime DEFAULT NULL COMMENT 'nhip tim: lan watcher chay gan nhat - web dung de biet watcher con song khong',
  `yeu_cau_boi` varchar(50) DEFAULT NULL COMMENT 'ten admin vua bam nut',
  `yeu_cau_luc` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `dieu_khien_server` WRITE;
/*!40000 ALTER TABLE `dieu_khien_server` DISABLE KEYS */;
INSERT INTO `dieu_khien_server` VALUES (1,1,NULL,1,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `dieu_khien_server` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `even_online_game`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `even_online_game` (
  `playerId` int(11) NOT NULL,
  `lastLoginTime` datetime DEFAULT NULL,
  `totalOnlineTimeInSeconds` bigint(20) DEFAULT 0,
  `lastResetTime` datetime DEFAULT NULL,
  `nhanQuaOnline` text DEFAULT '0,0,0,0',
  PRIMARY KEY (`playerId`),
  UNIQUE KEY `unique_player` (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `even_online_game` WRITE;
/*!40000 ALTER TABLE `even_online_game` DISABLE KEYS */;
/*!40000 ALTER TABLE `even_online_game` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `giftcode`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `giftcode` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `giftcode` varchar(255) NOT NULL,
  `xu` int(11) NOT NULL DEFAULT 0,
  `luong` int(11) NOT NULL DEFAULT 0,
  `luongLock` int(11) NOT NULL DEFAULT 0,
  `item` varchar(255) DEFAULT NULL,
  `expire` int(11) NOT NULL DEFAULT 0,
  `limit_use` int(11) NOT NULL DEFAULT 0,
  `type` int(11) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `giftcode` WRITE;
/*!40000 ALTER TABLE `giftcode` DISABLE KEYS */;
INSERT INTO `giftcode` VALUES (1,'oke212',0,0,1000,'ITEM:848:30000:1,ITEM:849:1000:1,ITEM:850:10000:1,ITEM:851:1000:1,ITEM:852:1000:1,ITEM:853:1000:1,ITEM:854:1000:1,ITEM:855:1000:1,ITEM:856:1000:1,ITEM:857:1000:1,ITEM:858:1000:1,ITEM:859:1000:1',0,2154,0),(2,'400ksz',0,0,0,'PPLUCK:593:1:1:1,PPLUCK:593:1:1:0',0,99937,0),(3,'123',0,0,0,'DN:500000:500000',0,5,0),(4,'ao4',0,0,0,'AOSHOP:725:1:0,AOSHOP:726:1:0',0,99982,0),(5,'bm1',0,0,0,'SUTU:1:300',0,0,0),(6,'hvl',0,0,0,'AOSHOP:741:1:43200',0,9988,0),(7,'NLT3',0,0,0,'NLT:0:1:43200',0,986,0),(8,'tbSz',0,0,0,'VKTBLV:70:99:5000',0,995,0),(9,'vk',0,0,0,'VKTT:0:1:43200',0,997,0),(10,'vailua',0,0,0,'GEM:73:10000:1,GEM:108:10000:1',0,99998,0),(12,'tl',0,0,0,'TL:30',0,9998,0),(13,'t',0,0,0,'NLTMAX:0:1:80000,TRANGPHUC:833:1:30,TRANGPHUC:834:1:30,TRANGPHUC:839:1:30,TRANGPHUC:791:1:30,TRANGPHUC:744:1:30,TRANGPHUC:736:1:30,TRANGPHUC:737:1:50',0,994,0),(14,'hvl2',0,0,0,'HOVELAN:882:5:43200',0,9993,0),(15,'tt',0,2000000,2000000,NULL,0,998,0),(16,'canhvohinh',0,0,0,'ITEM:910:1',0,99999,0);
/*!40000 ALTER TABLE `giftcode` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `giftcode_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `giftcode_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `giftcode` text DEFAULT NULL,
  `player` text DEFAULT NULL,
  `userId` int(11) NOT NULL DEFAULT 0,
  `item` text DEFAULT NULL,
  `xu` int(11) DEFAULT 0,
  `luong` int(11) DEFAULT 0,
  `luongK` int(11) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_giftcode_userid` (`giftcode`(191),`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `giftcode_log` WRITE;
/*!40000 ALTER TABLE `giftcode_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `giftcode_log` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `history_win_xoso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `history_win_xoso` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_user` int(11) NOT NULL,
  `money_win` bigint(20) NOT NULL DEFAULT 0,
  `time_win` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_user` (`id_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `history_win_xoso` WRITE;
/*!40000 ALTER TABLE `history_win_xoso` DISABLE KEYS */;
/*!40000 ALTER TABLE `history_win_xoso` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `ngude_giftcode`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ngude_giftcode` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL DEFAULT 'dangky',
  `xu` int(11) NOT NULL DEFAULT 0,
  `luong` int(11) NOT NULL DEFAULT 0,
  `luongLock` int(11) NOT NULL DEFAULT 0,
  `item` varchar(255) DEFAULT NULL,
  `charname` varchar(255) NOT NULL,
  `status` int(11) NOT NULL DEFAULT 0,
  `level` int(11) DEFAULT 40,
  `userId` int(11) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `ngude_giftcode` WRITE;
/*!40000 ALTER TABLE `ngude_giftcode` DISABLE KEYS */;
/*!40000 ALTER TABLE `ngude_giftcode` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `nin_xoso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nin_xoso` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(45) NOT NULL,
  `money` bigint(20) unsigned NOT NULL DEFAULT 0,
  `win` int(10) unsigned NOT NULL DEFAULT 0,
  `moneywin` bigint(20) unsigned NOT NULL DEFAULT 0,
  `moneyBid` bigint(20) unsigned NOT NULL DEFAULT 0,
  `moneyTax` bigint(20) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`charname`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `nin_xoso` WRITE;
/*!40000 ALTER TABLE `nin_xoso` DISABLE KEYS */;
/*!40000 ALTER TABLE `nin_xoso` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `nin_xoso_low`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nin_xoso_low` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(45) NOT NULL,
  `money` bigint(20) unsigned NOT NULL DEFAULT 0,
  `win` int(10) unsigned NOT NULL DEFAULT 0,
  `moneywin` bigint(20) unsigned NOT NULL DEFAULT 0,
  `moneyBid` bigint(20) unsigned NOT NULL DEFAULT 0,
  `moneyTax` bigint(20) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`charname`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `nin_xoso_low` WRITE;
/*!40000 ALTER TABLE `nin_xoso_low` DISABLE KEYS */;
/*!40000 ALTER TABLE `nin_xoso_low` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `team_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `team_user` (
  `id` int(4) NOT NULL AUTO_INCREMENT,
  `userid` int(10) unsigned NOT NULL,
  `provider` int(2) unsigned NOT NULL,
  `agent` int(2) NOT NULL DEFAULT -1,
  `newagent` varchar(45) NOT NULL DEFAULT '-1',
  `newprovider` varchar(45) NOT NULL DEFAULT '-1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`userid`) USING BTREE,
  KEY `Index_3` (`provider`),
  KEY `Index_4` (`agent`),
  KEY `newagent` (`newagent`),
  KEY `newprovider` (`newprovider`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `team_user` WRITE;
/*!40000 ALTER TABLE `team_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `team_user` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_animal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_animal` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `owner` int(10) unsigned NOT NULL,
  `att` varchar(500) NOT NULL,
  `place` int(10) unsigned NOT NULL DEFAULT 0,
  `id_img` int(10) unsigned NOT NULL,
  `lv` int(2) unsigned NOT NULL DEFAULT 1,
  `name` varchar(45) NOT NULL,
  `texpire` bigint(20) unsigned NOT NULL DEFAULT 0,
  `del` int(10) unsigned NOT NULL DEFAULT 0,
  `isHoaHinh` int(11) DEFAULT 0,
  `timeHoaHinh` bigint(20) DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `Index_2` (`owner`),
  KEY `Index_4` (`place`),
  KEY `Index_5` (`id_img`),
  KEY `Index_6` (`lv`),
  KEY `Index_7` (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci PACK_KEYS=1;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_animal` WRITE;
/*!40000 ALTER TABLE `tob_animal` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_animal` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_buy_ruong`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_buy_ruong` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(100) NOT NULL,
  `dateLog` varchar(100) DEFAULT NULL,
  `timeBuy` datetime DEFAULT NULL,
  `soluong` int(10) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_buy_ruong` WRITE;
/*!40000 ALTER TABLE `tob_buy_ruong` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_buy_ruong` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_cam_may`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_cam_may` (
  `ma_may` varchar(64) NOT NULL COMMENT 'SystemInfo.deviceUniqueIdentifier do client gửi',
  `ly_do` varchar(255) NOT NULL DEFAULT '',
  `boi_ai` varchar(64) NOT NULL DEFAULT '' COMMENT 'admin nào cấm',
  `luc` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`ma_may`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Danh sách máy bị cấm đăng nhập';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_cam_may` WRITE;
/*!40000 ALTER TABLE `tob_cam_may` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_cam_may` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_char`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_char` (
  `id` int(4) NOT NULL AUTO_INCREMENT,
  `charname` varchar(20) NOT NULL,
  `userId` int(4) NOT NULL,
  `xp` bigint(20) unsigned NOT NULL DEFAULT 0,
  `lastLv` int(2) unsigned NOT NULL DEFAULT 0,
  `hp` int(4) NOT NULL DEFAULT 0,
  `mp` int(4) NOT NULL DEFAULT 0,
  `map` int(4) NOT NULL DEFAULT -1,
  `x` int(4) NOT NULL DEFAULT 0,
  `y` int(4) NOT NULL DEFAULT 0,
  `gold` bigint(20) unsigned NOT NULL DEFAULT 0,
  `headStyle` int(1) NOT NULL DEFAULT 0,
  `class` int(1) NOT NULL DEFAULT 0,
  `nHP1` int(2) NOT NULL DEFAULT 0,
  `nHP2` int(2) NOT NULL DEFAULT 0,
  `nHP3` int(2) NOT NULL DEFAULT 0,
  `nMP1` int(2) NOT NULL DEFAULT 0,
  `nMP2` int(2) NOT NULL DEFAULT 0,
  `nMP3` int(2) NOT NULL DEFAULT 0,
  `nAr1` int(2) NOT NULL DEFAULT 0,
  `nAr2` int(2) NOT NULL DEFAULT 0,
  `nAr3` int(2) NOT NULL DEFAULT 0,
  `skill1` int(1) NOT NULL DEFAULT 0,
  `skill2` int(1) NOT NULL DEFAULT 0,
  `skill3` int(1) NOT NULL DEFAULT 0,
  `skill4` int(1) NOT NULL DEFAULT 0,
  `skill5` int(11) NOT NULL DEFAULT 0,
  `strength` int(2) NOT NULL DEFAULT 0,
  `agitity` int(2) NOT NULL DEFAULT 0,
  `spirit` int(2) NOT NULL DEFAULT 0,
  `dexterity` int(2) NOT NULL DEFAULT 0,
  `luck` int(2) NOT NULL DEFAULT 0,
  `basepoint` int(2) NOT NULL DEFAULT 3,
  `skillpoint` int(2) NOT NULL DEFAULT 0,
  `itemq1` int(2) unsigned NOT NULL DEFAULT 0,
  `itemq2` int(2) unsigned NOT NULL DEFAULT 0,
  `itemq3` int(2) unsigned NOT NULL DEFAULT 0,
  `itemq4` int(2) unsigned NOT NULL DEFAULT 0,
  `skill6` int(2) unsigned NOT NULL DEFAULT 0,
  `skill7` int(2) unsigned NOT NULL DEFAULT 0,
  `skill8` int(2) unsigned NOT NULL DEFAULT 0,
  `skill9` int(2) unsigned NOT NULL DEFAULT 0,
  `skill10` int(2) unsigned NOT NULL DEFAULT 0,
  `skill11` int(2) unsigned NOT NULL DEFAULT 0,
  `skill12` int(2) unsigned NOT NULL DEFAULT 0,
  `skill13` int(2) unsigned NOT NULL DEFAULT 0,
  `skill14` int(2) unsigned NOT NULL DEFAULT 0,
  `skill15` int(2) unsigned NOT NULL DEFAULT 0,
  `killer` int(2) unsigned NOT NULL DEFAULT 0,
  `khan1` int(2) unsigned NOT NULL DEFAULT 0,
  `khan2` int(2) unsigned NOT NULL DEFAULT 0,
  `khan3` int(2) unsigned NOT NULL DEFAULT 0,
  `khan4` int(2) unsigned NOT NULL DEFAULT 0,
  `khan5` int(2) unsigned NOT NULL DEFAULT 0,
  `book` int(2) unsigned NOT NULL DEFAULT 0,
  `timeKiller` bigint(20) unsigned NOT NULL DEFAULT 0,
  `nPKill` int(2) unsigned NOT NULL DEFAULT 0,
  `gender` int(2) unsigned NOT NULL DEFAULT 0,
  `ticket` int(2) unsigned NOT NULL DEFAULT 0,
  `totalTimePlay` int(2) unsigned NOT NULL DEFAULT 0,
  `fullGoldTime` bigint(20) unsigned NOT NULL DEFAULT 0,
  `halfGoldTime` bigint(20) unsigned NOT NULL DEFAULT 0,
  `luong` int(10) unsigned NOT NULL DEFAULT 0,
  `isAdmin` int(2) unsigned NOT NULL DEFAULT 0,
  `nHP4` int(2) unsigned NOT NULL DEFAULT 0,
  `nHP5` int(2) unsigned NOT NULL DEFAULT 0,
  `nMP4` int(2) unsigned NOT NULL DEFAULT 0,
  `nMP5` int(2) unsigned NOT NULL DEFAULT 0,
  `potion` varchar(2000) DEFAULT NULL,
  `skill` varchar(2000) DEFAULT NULL,
  `basic` varchar(2000) DEFAULT NULL,
  `pInfo` varchar(2000) DEFAULT NULL,
  `del` int(1) unsigned NOT NULL DEFAULT 1,
  `tDelChar` bigint(20) unsigned NOT NULL DEFAULT 0,
  `blacklist` int(1) unsigned NOT NULL DEFAULT 0,
  `nhack` int(1) unsigned NOT NULL DEFAULT 0,
  `newiddb` int(10) unsigned NOT NULL DEFAULT 0,
  `newuserid` int(10) unsigned NOT NULL DEFAULT 0,
  `ban` int(10) unsigned NOT NULL DEFAULT 0,
  `idClan` int(10) NOT NULL DEFAULT -1,
  `reupclass` int(10) unsigned NOT NULL DEFAULT 0,
  `lastLog` datetime NOT NULL DEFAULT '2010-01-01 00:00:00',
  `infotop` varchar(2000) NOT NULL DEFAULT '-1',
  `itemquest` varchar(200) DEFAULT NULL,
  `allitem` text DEFAULT NULL,
  `equip` mediumtext DEFAULT NULL,
  `inven` mediumtext DEFAULT NULL,
  `bag` mediumtext DEFAULT NULL,
  `killme` varchar(600) DEFAULT NULL,
  `luonglock` int(10) unsigned DEFAULT 0,
  `idserver` int(10) unsigned NOT NULL DEFAULT 0,
  `tuido` mediumtext DEFAULT NULL,
  `tichluy` int(11) DEFAULT 0,
  `tichluy_bosung` int(11) NOT NULL DEFAULT 0,
  `tichluy_tuan` int(11) DEFAULT 0,
  `topNap` int(11) NOT NULL DEFAULT 0,
  `qua_npc` int(11) DEFAULT 0,
  `isPP` int(11) DEFAULT 0,
  `isMN` int(11) NOT NULL DEFAULT 0,
  `diemluong` int(11) DEFAULT 0,
  `kho_xu` bigint(20) NOT NULL DEFAULT 0 COMMENT 'Kho xu o NPC Tong Quan',
  `kho_luong` int(11) NOT NULL DEFAULT 0 COMMENT 'Kho luong mo o NPC Tong Quan',
  `kho_luong_khoa` int(11) NOT NULL DEFAULT 0 COMMENT 'Kho luong khoa o NPC Tong Quan',
  `dex` smallint(6) NOT NULL DEFAULT 0 COMMENT 'Pokemon: so Pokedex, 0 = suy tu class+cap',
  `shiny` tinyint(4) NOT NULL DEFAULT 0 COMMENT 'Pokemon: 1 = shiny',
  PRIMARY KEY (`id`),
  UNIQUE KEY `charname` (`charname`),
  KEY `Index_3` (`userId`),
  KEY `Index_6` (`idClan`),
  KEY `Index_5` (`pInfo`(333)),
  KEY `Index_7` (`gold`),
  KEY `Index_8` (`xp`),
  KEY `Index_9` (`ticket`),
  KEY `Index_10` (`basepoint`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_char` WRITE;
/*!40000 ALTER TABLE `tob_char` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_char` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_char_fruit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_char_fruit` (
  `id_char` int(10) unsigned NOT NULL,
  `listidfruit` varchar(200) NOT NULL,
  `stFruit` varchar(100) NOT NULL,
  `tFruit` varchar(500) NOT NULL,
  `gift` varchar(500) NOT NULL COMMENT 'vi tri ve tren cay',
  `theo` varchar(500) NOT NULL,
  `tkho` varchar(500) NOT NULL,
  PRIMARY KEY (`id_char`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_char_fruit` WRITE;
/*!40000 ALTER TABLE `tob_char_fruit` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_char_fruit` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_char_luong`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_char_luong` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(45) NOT NULL,
  `country` tinyint(4) NOT NULL DEFAULT -1,
  `exp` bigint(20) unsigned NOT NULL DEFAULT 0,
  `idclan` int(11) NOT NULL DEFAULT -1,
  `honor` int(10) unsigned NOT NULL,
  `lv` int(10) unsigned NOT NULL DEFAULT 1,
  `nPK` bigint(20) unsigned NOT NULL DEFAULT 0,
  `wearing` varchar(500) NOT NULL DEFAULT '""',
  `luong` int(10) unsigned NOT NULL DEFAULT 0,
  `gold` bigint(20) unsigned NOT NULL DEFAULT 0,
  `headStyle` int(10) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `charname` (`charname`) USING BTREE,
  KEY `country` (`country`),
  KEY `exp` (`exp`),
  KEY `Index_5` (`honor`),
  KEY `Index_6` (`nPK`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_char_luong` WRITE;
/*!40000 ALTER TABLE `tob_char_luong` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_char_luong` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_char_quest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_char_quest` (
  `id_char` int(10) unsigned NOT NULL,
  `itemget` varchar(200) NOT NULL COMMENT 'cac loai item cach nhau = dau , item cua cac nhiem vu khac nhau cach nhau bang dau |',
  `monskilled` varchar(200) NOT NULL COMMENT 'so luong quai da giet',
  `isfinish` varchar(200) NOT NULL COMMENT 'da hoan thanh nhiem vu hay chua',
  `idsubRcv` varchar(200) DEFAULT NULL,
  `itemquest` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id_char`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_char_quest` WRITE;
/*!40000 ALTER TABLE `tob_char_quest` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_char_quest` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_char_thach_dau`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_char_thach_dau` (
  `id` int(10) unsigned NOT NULL,
  `charname` varchar(45) NOT NULL,
  `point` int(10) unsigned NOT NULL DEFAULT 0,
  `lv` int(10) unsigned NOT NULL DEFAULT 1,
  `timeupdate` datetime NOT NULL,
  `timeReg` datetime NOT NULL,
  `nhom` int(10) unsigned NOT NULL DEFAULT 0,
  `pointrank` int(10) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`charname`),
  KEY `Index_3` (`point`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_char_thach_dau` WRITE;
/*!40000 ALTER TABLE `tob_char_thach_dau` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_char_thach_dau` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_char_tubinh`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_char_tubinh` (
  `id_char` int(10) unsigned NOT NULL,
  `listid` varchar(200) NOT NULL,
  `listmanh` varchar(200) NOT NULL,
  PRIMARY KEY (`id_char`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_char_tubinh` WRITE;
/*!40000 ALTER TABLE `tob_char_tubinh` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_char_tubinh` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_char_vip`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_char_vip` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(100) NOT NULL,
  `vip` int(10) unsigned DEFAULT 0,
  `timeRcv` datetime NOT NULL,
  `reset` int(10) unsigned DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_char_vip` WRITE;
/*!40000 ALTER TABLE `tob_char_vip` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_char_vip` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_chiemthanh`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_chiemthanh` (
  `clan` varchar(255) NOT NULL,
  `bangchu` varchar(255) NOT NULL,
  `win` int(11) NOT NULL DEFAULT 0,
  `xu` varchar(255) NOT NULL DEFAULT '0',
  PRIMARY KEY (`clan`,`bangchu`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_chiemthanh` WRITE;
/*!40000 ALTER TABLE `tob_chiemthanh` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_chiemthanh` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_chien_truong`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_chien_truong` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(45) NOT NULL,
  `sttru` bigint(20) unsigned NOT NULL DEFAULT 0,
  `lientram` int(10) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`charname`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_chien_truong` WRITE;
/*!40000 ALTER TABLE `tob_chien_truong` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_chien_truong` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_clan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_clan` (
  `id_clan` int(10) unsigned NOT NULL,
  `clanName` varchar(45) DEFAULT NULL,
  `charMaster` varchar(45) DEFAULT NULL,
  `lvClan` int(10) unsigned DEFAULT NULL,
  `member` varchar(8000) DEFAULT NULL,
  `money` bigint(20) unsigned DEFAULT NULL,
  `xp` bigint(20) unsigned DEFAULT NULL,
  `datecreate` datetime NOT NULL,
  `slogan` varchar(300) NOT NULL,
  `isdel` int(10) unsigned NOT NULL DEFAULT 0,
  `timedel` bigint(20) unsigned NOT NULL DEFAULT 0,
  `town` int(10) unsigned NOT NULL DEFAULT 0,
  `tax` int(10) unsigned NOT NULL DEFAULT 0,
  `devote` int(10) unsigned NOT NULL DEFAULT 0,
  `otherinfo` varchar(800) NOT NULL DEFAULT '""',
  `skillclan` varchar(800) NOT NULL DEFAULT '""',
  `country` int(11) NOT NULL DEFAULT -1,
  `tcheckdel` bigint(20) unsigned NOT NULL DEFAULT 0,
  `id_cu` int(10) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id_clan`) USING BTREE,
  KEY `Index_2` (`lvClan`),
  KEY `Index_3` (`clanName`),
  KEY `Index_4` (`charMaster`),
  KEY `Index_5` (`money`),
  KEY `Index_6` (`xp`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_clan` WRITE;
/*!40000 ALTER TABLE `tob_clan` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_clan` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_clan_msg`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_clan_msg` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(45) NOT NULL,
  `info` varchar(1000) NOT NULL,
  `datesend` datetime NOT NULL,
  `id_clan` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `Index_2` (`charname`),
  KEY `Index_3` (`id_clan`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_clan_msg` WRITE;
/*!40000 ALTER TABLE `tob_clan_msg` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_clan_msg` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_code_lixi`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_code_lixi` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `code` varchar(100) NOT NULL,
  `nhan` int(10) unsigned DEFAULT 0,
  `timeNhan` datetime DEFAULT NULL,
  `charnhan` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`code`) USING BTREE,
  KEY `Index_3` (`charnhan`),
  KEY `Index_4` (`nhan`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_code_lixi` WRITE;
/*!40000 ALTER TABLE `tob_code_lixi` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_code_lixi` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_danh_hieu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_danh_hieu` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(80) NOT NULL,
  `iddh` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`charname`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_danh_hieu` WRITE;
/*!40000 ALTER TABLE `tob_danh_hieu` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_danh_hieu` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_dau_gia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_dau_gia` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(100) NOT NULL,
  `luongbid` int(10) unsigned NOT NULL DEFAULT 0,
  `luongcoc` int(10) unsigned NOT NULL DEFAULT 0,
  `timebid` datetime DEFAULT NULL,
  `hang` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '0 la chua xep hang, 1 la dc quyen mua, 2 la nhan lai tien',
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`charname`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_dau_gia` WRITE;
/*!40000 ALTER TABLE `tob_dau_gia` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_dau_gia` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_dau_gia2`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_dau_gia2` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(100) NOT NULL,
  `luongbid` int(10) unsigned NOT NULL DEFAULT 0,
  `luongcoc` int(10) unsigned NOT NULL DEFAULT 0,
  `timebid` datetime DEFAULT NULL,
  `hang` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '0 la chua xep hang, 1 la dc quyen mua, 2 la nhan lai tien',
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`charname`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_dau_gia2` WRITE;
/*!40000 ALTER TABLE `tob_dau_gia2` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_dau_gia2` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_event` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `part1` varchar(450) NOT NULL,
  `part2` varchar(450) NOT NULL,
  `part3` varchar(850) NOT NULL,
  `part4` varchar(450) NOT NULL,
  `shield` varchar(45) NOT NULL DEFAULT '0,0,0,0,0,0,0,0,0,0,0,0',
  `gov1` varchar(200) NOT NULL DEFAULT '""',
  `gov2` varchar(200) NOT NULL DEFAULT '""',
  `gov3` varchar(200) NOT NULL DEFAULT '""',
  `quayso` varchar(200) NOT NULL,
  `topluong` varchar(500) NOT NULL DEFAULT '0',
  `topNapLuong` varchar(500) DEFAULT NULL,
  `phaohoa` varchar(100) NOT NULL DEFAULT '0,0',
  `iditemsell` int(10) unsigned NOT NULL DEFAULT 0,
  `openphuonghoang` varchar(100) NOT NULL DEFAULT '5000,5,0,0',
  `trung` varchar(45) NOT NULL DEFAULT '0|10000|0',
  `ruongmm` varchar(200) DEFAULT NULL,
  `lixi` varchar(100) DEFAULT '100,18,30',
  `vip` varchar(100) DEFAULT '500,100,5',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_event` WRITE;
/*!40000 ALTER TABLE `tob_event` DISABLE KEYS */;
INSERT INTO `tob_event` VALUES (1,'0,0,0,0,0,0,0,0,0,0','0,0,0,0,0,0,0,0',',-1,-1,,-1,-1,,-1,-1,-1,-1,-1,0,0,0,3,3,3,0,0,0,0,0,0,0,0,0','0,10,0,-1','0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2025-07-08',',,,,,,',',,,,,,',',,,,,,','2025-07-08,0,0,0,5000','0',',0','0,0',0,'10000,0,0,0','0|5300|4067','2,20,20,20,20,3,3,5|2,5,5,5,5,3,3,5|1580144452356|300|242','100,18,30','500,100,5');
/*!40000 ALTER TABLE `tob_event` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_event_top`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_event_top` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(100) NOT NULL,
  `luongnap` bigint(20) unsigned NOT NULL DEFAULT 0,
  `toppoint` bigint(20) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `Index_2` (`charname`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_event_top` WRITE;
/*!40000 ALTER TABLE `tob_event_top` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_event_top` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_event_world_cup_state`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_event_world_cup_state` (
  `event_key` varchar(64) NOT NULL,
  `winner_side` tinyint(4) NOT NULL DEFAULT 0,
  `is_finalized` tinyint(4) NOT NULL DEFAULT 0,
  `finalized_by` varchar(100) DEFAULT NULL,
  `timeupdate` datetime DEFAULT NULL,
  PRIMARY KEY (`event_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_event_world_cup_state` WRITE;
/*!40000 ALTER TABLE `tob_event_world_cup_state` DISABLE KEYS */;
INSERT INTO `tob_event_world_cup_state` VALUES ('WORLD_CUP',0,0,NULL,'2026-10-17 10:25:12');
/*!40000 ALTER TABLE `tob_event_world_cup_state` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_farm`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_farm` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `owner` int(11) NOT NULL,
  `farmId` tinyint(4) NOT NULL,
  `treeId` int(11) NOT NULL DEFAULT -1,
  `timeBegin` bigint(20) unsigned NOT NULL DEFAULT 0,
  `lv` tinyint(4) NOT NULL,
  `buy` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `lvPlot` tinyint(3) unsigned NOT NULL DEFAULT 1,
  `rank` tinyint(3) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `Index_2` (`treeId`),
  KEY `Index_3` (`lv`) USING BTREE,
  KEY `Index_4` (`lvPlot`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_farm` WRITE;
/*!40000 ALTER TABLE `tob_farm` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_farm` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_friendlist`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_friendlist` (
  `charId` int(11) NOT NULL,
  `friendId` varchar(200) NOT NULL,
  `name` varchar(1000) NOT NULL,
  `ischange` int(10) unsigned NOT NULL DEFAULT 1,
  PRIMARY KEY (`charId`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_friendlist` WRITE;
/*!40000 ALTER TABLE `tob_friendlist` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_friendlist` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_fruit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_fruit` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `id1` int(10) unsigned DEFAULT NULL,
  `id2` int(10) unsigned DEFAULT NULL,
  `id3` int(10) unsigned DEFAULT NULL,
  `id4` int(10) unsigned DEFAULT NULL,
  `id5` int(10) unsigned DEFAULT NULL,
  `decript` varchar(200) DEFAULT NULL,
  `time1` int(10) unsigned DEFAULT NULL,
  `time2` int(10) unsigned DEFAULT NULL,
  `time3` int(10) unsigned DEFAULT NULL,
  `time4` int(10) unsigned DEFAULT NULL,
  `time5` int(10) unsigned DEFAULT NULL,
  `tfruit` int(10) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=9 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_fruit` WRITE;
/*!40000 ALTER TABLE `tob_fruit` DISABLE KEYS */;
INSERT INTO `tob_fruit` VALUES (2,'Quả lkd',9,10,NULL,NULL,NULL,'Quả lkd',360,120,72,NULL,NULL,5),(3,'Quả bạc',2,4,NULL,NULL,NULL,'Quả bạc',4320,0,864,NULL,NULL,2),(4,'Quả nguyên liệu',0,1,NULL,NULL,NULL,'Quả nguyên liệu',480,120,96,NULL,NULL,3),(5,'Quả may mắn',7,8,NULL,NULL,NULL,'Quả may mắn',360,120,72,NULL,NULL,4),(6,'Quả vàng',2,3,NULL,NULL,NULL,'Quả vàng',10080,0,2016,NULL,NULL,1),(7,'Quả cầu vồng',11,12,NULL,NULL,NULL,'Quả cầu vồng',360,120,72,NULL,NULL,6),(8,'Quả exp',5,6,NULL,NULL,NULL,'Quả exp',360,120,72,NULL,NULL,0);
/*!40000 ALTER TABLE `tob_fruit` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_gem_new`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_gem_new` (
  `owner` int(10) unsigned NOT NULL,
  `listtemplate` mediumtext NOT NULL,
  `soluong` mediumtext NOT NULL,
  `slock` mediumtext NOT NULL,
  PRIMARY KEY (`owner`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_gem_new` WRITE;
/*!40000 ALTER TABLE `tob_gem_new` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_gem_new` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_get_board_nap`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_get_board_nap` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(45) NOT NULL,
  `info` varchar(45) NOT NULL,
  `dateGet` varchar(100) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `Index_2` (`charname`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_get_board_nap` WRITE;
/*!40000 ALTER TABLE `tob_get_board_nap` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_get_board_nap` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_inbox`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_inbox` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(45) NOT NULL,
  `sender` varchar(45) NOT NULL,
  `info` varchar(800) NOT NULL,
  `dateSend` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `Index_2` (`charname`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci ROW_FORMAT=FIXED;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_inbox` WRITE;
/*!40000 ALTER TABLE `tob_inbox` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_inbox` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_log_all_gem`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_log_all_gem` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(45) NOT NULL,
  `aclog` varchar(45) NOT NULL DEFAULT '""',
  `info` varchar(8000) NOT NULL DEFAULT '""',
  `timelog` datetime NOT NULL,
  `charname2` varchar(45) NOT NULL DEFAULT '""',
  PRIMARY KEY (`id`),
  KEY `charname` (`charname`),
  KEY `aclog` (`aclog`),
  KEY `timelog` (`timelog`),
  KEY `charname2` (`charname2`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_log_all_gem` WRITE;
/*!40000 ALTER TABLE `tob_log_all_gem` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_log_all_gem` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_log_all_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_log_all_item` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(45) NOT NULL,
  `aclog` varchar(45) NOT NULL DEFAULT '""',
  `info` varchar(8000) NOT NULL DEFAULT '""',
  `timelog` datetime NOT NULL,
  `charname2` varchar(45) NOT NULL DEFAULT '""',
  `idchar` int(10) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `charname` (`charname`),
  KEY `aclog` (`aclog`),
  KEY `timelog` (`timelog`),
  KEY `charname2` (`charname2`),
  KEY `idchar` (`idchar`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_log_all_item` WRITE;
/*!40000 ALTER TABLE `tob_log_all_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_log_all_item` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_log_buy_shop`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_log_buy_shop` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(45) NOT NULL,
  `info` varchar(450) NOT NULL,
  `timeBuy` datetime NOT NULL,
  `typeitem` int(10) unsigned NOT NULL DEFAULT 0,
  `price` int(10) unsigned NOT NULL,
  `money` varchar(450) NOT NULL DEFAULT '""',
  `soluong` int(10) unsigned NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  KEY `Index_2` (`charname`),
  KEY `Index_4` (`timeBuy`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_log_buy_shop` WRITE;
/*!40000 ALTER TABLE `tob_log_buy_shop` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_log_buy_shop` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_log_buy_store`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_log_buy_store` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(45) NOT NULL,
  `info` varchar(500) NOT NULL,
  `timeBuy` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_log_buy_store` WRITE;
/*!40000 ALTER TABLE `tob_log_buy_store` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_log_buy_store` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_log_clan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_log_clan` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(45) NOT NULL,
  `actioninfo` varchar(45) NOT NULL,
  `timeLog` datetime NOT NULL,
  `info` varchar(8000) NOT NULL DEFAULT '""',
  PRIMARY KEY (`id`),
  KEY `Index_2` (`charname`),
  KEY `Index_3` (`actioninfo`),
  KEY `Index_4` (`timeLog`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_log_clan` WRITE;
/*!40000 ALTER TABLE `tob_log_clan` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_log_clan` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_log_online`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_log_online` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `thoi_gian` datetime NOT NULL COMMENT 'luc lay mau',
  `ket_noi` int(11) NOT NULL DEFAULT 0 COMMENT 'so socket dang mo',
  `nhan_vat` int(11) NOT NULL DEFAULT 0 COMMENT 'so nhan vat that trong game',
  PRIMARY KEY (`id`),
  KEY `idx_thoi_gian` (`thoi_gian`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_log_online` WRITE;
/*!40000 ALTER TABLE `tob_log_online` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_log_online` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_log_use_luong`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_log_use_luong` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `idtemplate` int(10) unsigned NOT NULL DEFAULT 0,
  `soluong` bigint(20) unsigned NOT NULL DEFAULT 1,
  `solanmua` int(10) unsigned NOT NULL DEFAULT 1,
  `totalmoney` bigint(20) unsigned NOT NULL DEFAULT 0,
  `moneytype` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`name`) USING BTREE,
  KEY `Index_3` (`idtemplate`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_log_use_luong` WRITE;
/*!40000 ALTER TABLE `tob_log_use_luong` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_log_use_luong` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_log_use_luong_1_7_2025`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_log_use_luong_1_7_2025` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `idtemplate` int(10) unsigned NOT NULL DEFAULT 0,
  `soluong` bigint(20) unsigned NOT NULL DEFAULT 1,
  `solanmua` int(10) unsigned NOT NULL DEFAULT 1,
  `totalmoney` bigint(20) unsigned NOT NULL DEFAULT 0,
  `moneytype` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`name`) USING BTREE,
  KEY `Index_3` (`idtemplate`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_log_use_luong_1_7_2025` WRITE;
/*!40000 ALTER TABLE `tob_log_use_luong_1_7_2025` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_log_use_luong_1_7_2025` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_log_use_luong_2_6_2025`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_log_use_luong_2_6_2025` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `idtemplate` int(10) unsigned NOT NULL DEFAULT 0,
  `soluong` bigint(20) unsigned NOT NULL DEFAULT 1,
  `solanmua` int(10) unsigned NOT NULL DEFAULT 1,
  `totalmoney` bigint(20) unsigned NOT NULL DEFAULT 0,
  `moneytype` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`name`) USING BTREE,
  KEY `Index_3` (`idtemplate`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_log_use_luong_2_6_2025` WRITE;
/*!40000 ALTER TABLE `tob_log_use_luong_2_6_2025` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_log_use_luong_2_6_2025` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_log_use_luong_2_7_2025`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_log_use_luong_2_7_2025` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `idtemplate` int(10) unsigned NOT NULL DEFAULT 0,
  `soluong` bigint(20) unsigned NOT NULL DEFAULT 1,
  `solanmua` int(10) unsigned NOT NULL DEFAULT 1,
  `totalmoney` bigint(20) unsigned NOT NULL DEFAULT 0,
  `moneytype` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`name`) USING BTREE,
  KEY `Index_3` (`idtemplate`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_log_use_luong_2_7_2025` WRITE;
/*!40000 ALTER TABLE `tob_log_use_luong_2_7_2025` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_log_use_luong_2_7_2025` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_log_use_luong_3_6_2025`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_log_use_luong_3_6_2025` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `idtemplate` int(10) unsigned NOT NULL DEFAULT 0,
  `soluong` bigint(20) unsigned NOT NULL DEFAULT 1,
  `solanmua` int(10) unsigned NOT NULL DEFAULT 1,
  `totalmoney` bigint(20) unsigned NOT NULL DEFAULT 0,
  `moneytype` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`name`) USING BTREE,
  KEY `Index_3` (`idtemplate`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_log_use_luong_3_6_2025` WRITE;
/*!40000 ALTER TABLE `tob_log_use_luong_3_6_2025` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_log_use_luong_3_6_2025` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_log_use_luong_4_6_2025`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_log_use_luong_4_6_2025` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `idtemplate` int(10) unsigned NOT NULL DEFAULT 0,
  `soluong` bigint(20) unsigned NOT NULL DEFAULT 1,
  `solanmua` int(10) unsigned NOT NULL DEFAULT 1,
  `totalmoney` bigint(20) unsigned NOT NULL DEFAULT 0,
  `moneytype` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`name`) USING BTREE,
  KEY `Index_3` (`idtemplate`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_log_use_luong_4_6_2025` WRITE;
/*!40000 ALTER TABLE `tob_log_use_luong_4_6_2025` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_log_use_luong_4_6_2025` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_log_use_luong_5_6_2025`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_log_use_luong_5_6_2025` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `idtemplate` int(10) unsigned NOT NULL DEFAULT 0,
  `soluong` bigint(20) unsigned NOT NULL DEFAULT 1,
  `solanmua` int(10) unsigned NOT NULL DEFAULT 1,
  `totalmoney` bigint(20) unsigned NOT NULL DEFAULT 0,
  `moneytype` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`name`) USING BTREE,
  KEY `Index_3` (`idtemplate`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_log_use_luong_5_6_2025` WRITE;
/*!40000 ALTER TABLE `tob_log_use_luong_5_6_2025` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_log_use_luong_5_6_2025` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_log_xoso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_log_xoso` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `win` int(10) unsigned NOT NULL,
  `timelog` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `Index_2` (`win`),
  KEY `Index_3` (`timelog`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_log_xoso` WRITE;
/*!40000 ALTER TABLE `tob_log_xoso` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_log_xoso` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_market`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_market` (
  `charname` varchar(45) NOT NULL,
  `itemsell` text NOT NULL COMMENT 'danh sach item dang ban',
  `money` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT 'tien ban duoc',
  `items` text NOT NULL,
  `listBid` text NOT NULL,
  PRIMARY KEY (`charname`) USING BTREE,
  KEY `money` (`money`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_market` WRITE;
/*!40000 ALTER TABLE `tob_market` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_market` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_may_dang_nhap`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_may_dang_nhap` (
  `ma_may` varchar(64) NOT NULL,
  `username` varchar(64) NOT NULL,
  `ip` varchar(45) NOT NULL DEFAULT '' COMMENT 'dài 45 để chứa được cả IPv6',
  `lan_dau` datetime NOT NULL DEFAULT current_timestamp(),
  `lan_cuoi` datetime NOT NULL DEFAULT current_timestamp(),
  `so_lan` int(11) NOT NULL DEFAULT 1,
  PRIMARY KEY (`ma_may`,`username`),
  KEY `k_username` (`username`),
  KEY `k_lan_cuoi` (`lan_cuoi`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Máy nào đã đăng nhập tài khoản nào';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_may_dang_nhap` WRITE;
/*!40000 ALTER TABLE `tob_may_dang_nhap` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_may_dang_nhap` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_nap_moc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_nap_moc` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `userID` int(11) NOT NULL COMMENT 'team_user.id / player.userID',
  `moc` tinyint(3) unsigned NOT NULL COMMENT '1 = 49k, 2 = 99k, 3 = 299k',
  `amount` int(10) unsigned NOT NULL COMMENT 'so tien VND',
  `ma_don` varchar(20) NOT NULL COMMENT 'noi dung chuyen khoan, dung de doi chieu voi SePay',
  `phuong_hoang` tinyint(4) NOT NULL DEFAULT -1 COMMENT 'moc 3 only: 0=Phuong Hoang, 1=Phuong Hoang Bang, -1=chua chon',
  `trang_thai` enum('cho_thanh_toan','da_thanh_toan','da_nhan') NOT NULL DEFAULT 'cho_thanh_toan',
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `paid_at` datetime DEFAULT NULL,
  `claimed_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_ma_don` (`ma_don`),
  KEY `idx_user_moc` (`userID`,`moc`,`trang_thai`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_nap_moc` WRITE;
/*!40000 ALTER TABLE `tob_nap_moc` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_nap_moc` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_nap_xai_luong`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_nap_xai_luong` (
  `int` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(100) NOT NULL,
  `luongxai` int(10) unsigned NOT NULL DEFAULT 0,
  `luongnap` int(10) unsigned NOT NULL DEFAULT 0,
  `tnap` datetime DEFAULT NULL,
  `txai` datetime DEFAULT NULL,
  PRIMARY KEY (`int`),
  UNIQUE KEY `Index_2` (`charname`),
  KEY `Index_3` (`luongxai`),
  KEY `Index_4` (`luongnap`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_nap_xai_luong` WRITE;
/*!40000 ALTER TABLE `tob_nap_xai_luong` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_nap_xai_luong` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_nap_xai_luong_62025`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_nap_xai_luong_62025` (
  `int` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(100) NOT NULL,
  `luongxai` int(10) unsigned NOT NULL DEFAULT 0,
  `luongnap` int(10) unsigned NOT NULL DEFAULT 0,
  `tnap` datetime DEFAULT NULL,
  `txai` datetime DEFAULT NULL,
  PRIMARY KEY (`int`),
  UNIQUE KEY `Index_2` (`charname`),
  KEY `Index_3` (`luongxai`),
  KEY `Index_4` (`luongnap`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_nap_xai_luong_62025` WRITE;
/*!40000 ALTER TABLE `tob_nap_xai_luong_62025` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_nap_xai_luong_62025` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_nap_xai_luong_72025`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_nap_xai_luong_72025` (
  `int` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(100) NOT NULL,
  `luongxai` int(10) unsigned NOT NULL DEFAULT 0,
  `luongnap` int(10) unsigned NOT NULL DEFAULT 0,
  `tnap` datetime DEFAULT NULL,
  `txai` datetime DEFAULT NULL,
  PRIMARY KEY (`int`),
  UNIQUE KEY `Index_2` (`charname`),
  KEY `Index_3` (`luongxai`),
  KEY `Index_4` (`luongnap`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_nap_xai_luong_72025` WRITE;
/*!40000 ALTER TABLE `tob_nap_xai_luong_72025` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_nap_xai_luong_72025` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_nau_banh`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_nau_banh` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `listchar` text NOT NULL,
  `timecook` int(10) unsigned NOT NULL DEFAULT 60,
  `water` int(10) unsigned NOT NULL DEFAULT 0,
  `chamnc` int(10) unsigned NOT NULL DEFAULT 0,
  `listCharGift` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_nau_banh` WRITE;
/*!40000 ALTER TABLE `tob_nau_banh` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_nau_banh` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_pet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_pet` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(300) NOT NULL,
  `idImg` tinyint(3) unsigned NOT NULL,
  `owner` int(10) unsigned NOT NULL,
  `info` varchar(200) NOT NULL COMMENT 'thong tin ve thoi gian mua, tong thoi gian ton tai',
  `place` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `itPet` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `name` (`name`),
  KEY `idimg` (`idImg`),
  KEY `owner` (`owner`),
  KEY `place` (`place`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_pet` WRITE;
/*!40000 ALTER TABLE `tob_pet` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_pet` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_pokedex`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_pokedex` (
  `id_char` int(11) NOT NULL,
  `dex` smallint(6) NOT NULL,
  `so_ha` int(11) NOT NULL DEFAULT 0,
  `lan_dau` datetime NOT NULL,
  PRIMARY KEY (`id_char`,`dex`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_pokedex` WRITE;
/*!40000 ALTER TABLE `tob_pokedex` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_pokedex` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_pokedex_thuong`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_pokedex_thuong` (
  `id_char` int(11) NOT NULL,
  `moc` int(11) NOT NULL,
  `thoi_gian` datetime NOT NULL,
  PRIMARY KEY (`id_char`,`moc`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_pokedex_thuong` WRITE;
/*!40000 ALTER TABLE `tob_pokedex_thuong` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_pokedex_thuong` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_qua`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_qua` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(100) NOT NULL,
  `qua` bigint(20) unsigned NOT NULL DEFAULT 1,
  `typeQua` bigint(20) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`charname`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_qua` WRITE;
/*!40000 ALTER TABLE `tob_qua` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_qua` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_qua_ct`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_qua_ct` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(45) NOT NULL,
  `soluong` int(10) unsigned NOT NULL DEFAULT 1,
  `nhan` int(10) unsigned NOT NULL DEFAULT 0,
  `tlog` datetime DEFAULT NULL,
  `tset` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`charname`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_qua_ct` WRITE;
/*!40000 ALTER TABLE `tob_qua_ct` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_qua_ct` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_qua_ct_bk`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_qua_ct_bk` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(45) NOT NULL,
  `soluong` int(10) unsigned NOT NULL DEFAULT 1,
  `nhan` int(10) unsigned NOT NULL DEFAULT 0,
  `tlog` datetime DEFAULT NULL,
  `tset` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_qua_ct_bk` WRITE;
/*!40000 ALTER TABLE `tob_qua_ct_bk` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_qua_ct_bk` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_qua_ct_top`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_qua_ct_top` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(45) NOT NULL,
  `soluong` int(10) unsigned NOT NULL DEFAULT 1 COMMENT '1-lien tram, 2 dam tru',
  `nhan` int(10) unsigned NOT NULL DEFAULT 0,
  `tlog` datetime DEFAULT NULL,
  `tset` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_qua_ct_top` WRITE;
/*!40000 ALTER TABLE `tob_qua_ct_top` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_qua_ct_top` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_qua_ct_top_bk`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_qua_ct_top_bk` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(45) NOT NULL,
  `soluong` int(10) unsigned NOT NULL DEFAULT 1,
  `nhan` int(10) unsigned NOT NULL DEFAULT 0,
  `tlog` datetime DEFAULT NULL,
  `tset` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_qua_ct_top_bk` WRITE;
/*!40000 ALTER TABLE `tob_qua_ct_top_bk` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_qua_ct_top_bk` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_qua_loi_dai`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_qua_loi_dai` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(100) NOT NULL,
  `qua` int(10) unsigned NOT NULL DEFAULT 0,
  `typeQua` int(10) unsigned NOT NULL DEFAULT 0,
  `nhom` int(10) unsigned NOT NULL DEFAULT 0,
  `diem` int(10) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_qua_loi_dai` WRITE;
/*!40000 ALTER TABLE `tob_qua_loi_dai` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_qua_loi_dai` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_quest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_quest` (
  `idchar` int(10) unsigned NOT NULL,
  `idquest` int(10) unsigned NOT NULL,
  `state` tinyint(3) unsigned NOT NULL DEFAULT 0 COMMENT '0 chua nhan, 1 nhan, 2 hoan thanh',
  `totalmonskill` varchar(100) NOT NULL DEFAULT '0',
  `totalitemget` varchar(100) NOT NULL DEFAULT '0',
  PRIMARY KEY (`idchar`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_quest` WRITE;
/*!40000 ALTER TABLE `tob_quest` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_quest` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_quest_clan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_quest_clan` (
  `id_char` int(10) unsigned NOT NULL,
  `idquest` int(10) unsigned NOT NULL,
  `rcv` int(10) unsigned NOT NULL DEFAULT 0,
  `finish` int(10) unsigned NOT NULL DEFAULT 0,
  `nitem1` int(10) unsigned NOT NULL DEFAULT 0,
  `nitem2` int(10) unsigned NOT NULL DEFAULT 0,
  `nitem3` int(10) unsigned NOT NULL DEFAULT 0,
  `nmons1` int(10) unsigned NOT NULL DEFAULT 0,
  `nmons2` int(10) unsigned NOT NULL DEFAULT 0,
  `nmons3` int(10) unsigned NOT NULL DEFAULT 0,
  `id_clan` int(10) unsigned NOT NULL DEFAULT 0,
  `n` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `timeEnd` bigint(20) unsigned zerofill NOT NULL DEFAULT 00000000000000000000,
  PRIMARY KEY (`id_char`) USING BTREE,
  KEY `Index_2` (`idquest`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_quest_clan` WRITE;
/*!40000 ALTER TABLE `tob_quest_clan` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_quest_clan` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_reg_chientruong`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_reg_chientruong` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(45) NOT NULL,
  `timeReg` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`charname`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_reg_chientruong` WRITE;
/*!40000 ALTER TABLE `tob_reg_chientruong` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_reg_chientruong` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_smsnap`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_smsnap` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `text` varchar(50) NOT NULL,
  `syntax` varchar(100) NOT NULL,
  `port` varchar(10) NOT NULL,
  `provider` int(1) NOT NULL,
  `game` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `agent` int(10) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_smsnap` WRITE;
/*!40000 ALTER TABLE `tob_smsnap` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_smsnap` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_sp_new`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_sp_new` (
  `owner` int(10) unsigned NOT NULL,
  `listtemplate` varchar(300) NOT NULL,
  `soluong` varchar(500) NOT NULL,
  PRIMARY KEY (`owner`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_sp_new` WRITE;
/*!40000 ALTER TABLE `tob_sp_new` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_sp_new` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_su_kien`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_su_kien` (
  `ma` varchar(40) NOT NULL COMMENT 'khoa, khop hang so trong src/real/SuKien.java',
  `bat` tinyint(4) NOT NULL DEFAULT 0 COMMENT 'WEB ghi: 1 = bat su kien',
  `ten` varchar(100) DEFAULT NULL COMMENT 'SERVER ghi',
  `nhom` varchar(40) DEFAULT NULL COMMENT 'SERVER ghi',
  `mo_ta` varchar(500) DEFAULT NULL COMMENT 'SERVER ghi',
  `dang_bat` tinyint(4) NOT NULL DEFAULT 0 COMMENT 'SERVER ghi: trang thai that luc khoi dong',
  `sua_boi` varchar(50) DEFAULT NULL COMMENT 'WEB ghi',
  `sua_luc` datetime DEFAULT NULL COMMENT 'WEB ghi',
  `may_chu_luc` datetime DEFAULT NULL COMMENT 'SERVER ghi: lan khoi dong gan nhat',
  `mo_luc` datetime DEFAULT NULL,
  `dong_luc` datetime DEFAULT NULL,
  PRIMARY KEY (`ma`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_su_kien` WRITE;
/*!40000 ALTER TABLE `tob_su_kien` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_su_kien` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_top_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_top_event` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(50) NOT NULL,
  `point1` int(11) NOT NULL DEFAULT 0,
  `timeupdate` datetime DEFAULT current_timestamp(),
  `xu` bigint(20) DEFAULT 0,
  `luong` int(11) DEFAULT 0,
  `datexu` datetime DEFAULT current_timestamp(),
  `dateluong` datetime DEFAULT current_timestamp(),
  `crtime` bigint(20) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `charname` (`charname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_top_event` WRITE;
/*!40000 ALTER TABLE `tob_top_event` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_top_event` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_top_event_wc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_top_event_wc` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `charname` varchar(200) NOT NULL,
  `point1` int(10) unsigned NOT NULL DEFAULT 1,
  `timeupdate` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `xu` bigint(20) unsigned NOT NULL DEFAULT 0,
  `luong` bigint(20) unsigned NOT NULL DEFAULT 0,
  `datexu` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `dateluong` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `crtime` bigint(20) unsigned DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Index_2` (`charname`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_top_event_wc` WRITE;
/*!40000 ALTER TABLE `tob_top_event_wc` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_top_event_wc` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_top_event_world_cup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_top_event_world_cup` (
  `charname` varchar(100) NOT NULL,
  `point_messi` int(11) NOT NULL DEFAULT 0,
  `point_ronaldo` int(11) NOT NULL DEFAULT 0,
  `claim_normal` tinyint(4) NOT NULL DEFAULT 0,
  `claim_top` tinyint(4) NOT NULL DEFAULT 0,
  `timeupdate` datetime DEFAULT NULL,
  PRIMARY KEY (`charname`),
  KEY `idx_messi` (`point_messi`),
  KEY `idx_ronaldo` (`point_ronaldo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_top_event_world_cup` WRITE;
/*!40000 ALTER TABLE `tob_top_event_world_cup` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_top_event_world_cup` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_top_thuong`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_top_thuong` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `vi_tri` tinyint(4) NOT NULL COMMENT 'hang 1..10',
  `charname` varchar(50) NOT NULL COMMENT 'nhan vat duoc thuong',
  `item` text DEFAULT NULL COMMENT 'chuoi phan thuong co dinh, cu phap giong giftcode.item',
  `item_chon` text DEFAULT NULL COMMENT 'phan thuong de nguoi choi tu chon: "nhan=chuoi|nhan=chuoi|..."',
  `da_nhan` tinyint(4) NOT NULL DEFAULT 0 COMMENT '0 cho nhan, 1 da nhan',
  `admin_user` varchar(50) NOT NULL DEFAULT '' COMMENT 'team_user.username nguoi tao',
  `tao_luc` datetime NOT NULL DEFAULT current_timestamp(),
  `nhan_luc` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_charname_da_nhan` (`charname`,`da_nhan`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_top_thuong` WRITE;
/*!40000 ALTER TABLE `tob_top_thuong` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_top_thuong` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_tubinh`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_tubinh` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `attack` int(10) unsigned NOT NULL,
  `def` int(10) unsigned NOT NULL,
  `dog` int(10) unsigned NOT NULL,
  `crit` int(10) unsigned NOT NULL,
  `hp` int(10) unsigned NOT NULL,
  `mp` int(10) unsigned NOT NULL,
  `level` int(10) unsigned NOT NULL,
  `nmanh` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=17 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_tubinh` WRITE;
/*!40000 ALTER TABLE `tob_tubinh` DISABLE KEYS */;
INSERT INTO `tob_tubinh` VALUES (1,'Mai',15,24,2,2,100,30,40,5),(2,'Lan',30,45,4,4,200,60,40,5),(3,'Cúc',45,69,6,6,300,90,40,5),(4,'Trúc',60,90,8,8,400,120,40,5),(5,'Sáo',30,45,4,4,200,60,50,10),(6,'Quạt',60,90,8,8,400,120,50,10),(7,'Sênh',90,135,12,12,600,180,50,10),(8,'Đàn',120,180,16,16,800,240,50,10),(9,'Ngư',45,69,6,6,600,90,60,15),(10,'Tiều',90,135,12,12,700,180,60,15),(11,'Canh',135,204,18,18,800,270,60,15),(12,'Độc',180,270,24,24,900,360,60,15),(13,'Long',60,90,8,8,850,120,70,20),(14,'Lân',120,180,16,16,950,240,70,20),(15,'Quy',180,270,24,24,1050,360,70,20),(16,'Phụng',240,360,32,32,1150,480,70,20);
/*!40000 ALTER TABLE `tob_tubinh` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_vantieu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_vantieu` (
  `idchar` int(10) unsigned NOT NULL,
  `namechar` varchar(45) NOT NULL,
  `typetieu` int(10) unsigned NOT NULL,
  `tReceive` bigint(20) unsigned NOT NULL,
  `lv` int(10) unsigned NOT NULL,
  `mapid` int(10) unsigned NOT NULL,
  `x` int(10) unsigned NOT NULL,
  `y` int(10) unsigned NOT NULL,
  `idcountry` tinyint(3) unsigned NOT NULL,
  `hp` bigint(20) unsigned NOT NULL DEFAULT 0,
  `maxhp` bigint(20) unsigned NOT NULL DEFAULT 0,
  `count` int(11) DEFAULT 0,
  PRIMARY KEY (`idchar`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_vantieu` WRITE;
/*!40000 ALTER TABLE `tob_vantieu` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_vantieu` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_web_info_char`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_web_info_char` (
  `charname` varchar(45) NOT NULL,
  `infoWearing` varchar(150) NOT NULL,
  `otherInfo` varchar(300) NOT NULL,
  `userid` int(10) unsigned NOT NULL,
  `lv` int(10) unsigned NOT NULL DEFAULT 0,
  `acountname` varchar(45) NOT NULL,
  PRIMARY KEY (`charname`) USING BTREE,
  KEY `userid` (`userid`) USING BTREE,
  KEY `lv` (`lv`),
  KEY `acountname` (`acountname`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_web_info_char` WRITE;
/*!40000 ALTER TABLE `tob_web_info_char` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_web_info_char` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_wedding`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_wedding` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `idhusband` int(10) unsigned NOT NULL,
  `idwife` int(10) unsigned NOT NULL,
  `married` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `timeParty` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `dayparty` varchar(45) NOT NULL DEFAULT '""',
  `typeparty` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `idwedding` int(10) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `husband` (`idhusband`),
  KEY `idwife` (`idwife`),
  KEY `Index_4` (`married`),
  KEY `Index_5` (`typeparty`),
  KEY `Index_6` (`idwedding`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_wedding` WRITE;
/*!40000 ALTER TABLE `tob_wedding` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_wedding` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_x2exp`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_x2exp` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `dayopenx2` varchar(500) DEFAULT NULL,
  `dayopenx3` varchar(500) DEFAULT NULL,
  `dayopenx50` varchar(500) DEFAULT NULL,
  `badwod` text NOT NULL,
  `tradepet` int(10) unsigned NOT NULL DEFAULT 0,
  `thamdinh` int(10) unsigned NOT NULL DEFAULT 0,
  `banchoi` int(11) NOT NULL DEFAULT 20,
  `itemevent` varchar(100) DEFAULT NULL,
  `pause` int(10) unsigned NOT NULL DEFAULT 1,
  `pauseT` int(10) unsigned NOT NULL DEFAULT 0,
  `vxmm` int(10) unsigned NOT NULL DEFAULT 1,
  `gopbang` int(10) unsigned NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_x2exp` WRITE;
/*!40000 ALTER TABLE `tob_x2exp` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_x2exp` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `tob_xoso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tob_xoso` (
  `owner` int(10) unsigned NOT NULL,
  `vsxu` varchar(500) NOT NULL,
  `vsl` varchar(500) NOT NULL,
  `open` tinyint(3) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`owner`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `tob_xoso` WRITE;
/*!40000 ALTER TABLE `tob_xoso` DISABLE KEYS */;
/*!40000 ALTER TABLE `tob_xoso` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `top_char_quest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `top_char_quest` (
  `id_char` int(10) unsigned NOT NULL,
  `id_quest` int(10) unsigned NOT NULL DEFAULT 1,
  `receive` tinyint(3) unsigned NOT NULL DEFAULT 0 COMMENT 'cho biet da nhan quest hay chua',
  `finish` tinyint(3) unsigned NOT NULL DEFAULT 0 COMMENT 'cho biet da hoan thanh quest hay chua, neu roi thi gui thong tin reponse ',
  `nmonster1` int(10) unsigned NOT NULL DEFAULT 0 COMMENT 'so luogn quai da giet',
  `nmonster2` int(10) unsigned NOT NULL DEFAULT 0,
  `nmonster3` int(10) unsigned NOT NULL DEFAULT 0,
  `nitem1` int(10) unsigned NOT NULL DEFAULT 0 COMMENT 'so luong item da lay',
  `nitem2` int(10) unsigned NOT NULL DEFAULT 0,
  `nitem3` int(10) unsigned NOT NULL DEFAULT 0,
  `currentnpc` int(10) unsigned NOT NULL DEFAULT 0,
  `ischeck` int(10) unsigned NOT NULL DEFAULT 0 COMMENT 'da kt roi thi ko kiem tra nua',
  PRIMARY KEY (`id_char`),
  KEY `Index_2` (`id_quest`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `top_char_quest` WRITE;
/*!40000 ALTER TABLE `top_char_quest` DISABLE KEYS */;
/*!40000 ALTER TABLE `top_char_quest` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `web_admin_2fa`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `web_admin_2fa` (
  `admin_user` varchar(50) NOT NULL COMMENT 'team_user.username, chu thuong. Khop ADMIN_ACCOUNTS trong config.local.php',
  `khoa_bimat` varchar(64) NOT NULL COMMENT 'Khoa TOTP dang Base32 (20 byte = 32 ky tu). CHI sinh boi tao_ma_2buoc.php',
  `kich_hoat` tinyint(4) NOT NULL DEFAULT 0 COMMENT '1 = tai khoan nay BUOC PHAI go ma hai buoc, khong co cach bo qua',
  `buoc_da_dung` bigint(20) NOT NULL DEFAULT 0 COMMENT 'Buoc thoi gian cao nhat da dung. Chong dung lai mot ma da bi doc len',
  `ma_du_phong` text DEFAULT NULL COMMENT '8 ma du phong DA BAM, ngan nhau bang \\n. Moi ma dung dung mot lan',
  `tao_luc` datetime DEFAULT NULL COMMENT 'Luc ghi danh',
  `dung_luc` datetime DEFAULT NULL COMMENT 'Lan cuoi go ma thanh cong',
  PRIMARY KEY (`admin_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `web_admin_2fa` WRITE;
/*!40000 ALTER TABLE `web_admin_2fa` DISABLE KEYS */;
/*!40000 ALTER TABLE `web_admin_2fa` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `web_admin_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `web_admin_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `admin_user` varchar(50) NOT NULL DEFAULT '' COMMENT 'team_user.username cua nguoi thao tac',
  `hanh_dong` varchar(50) NOT NULL DEFAULT '' COMMENT 'khoa_tk, mo_tk, tao_giftcode, dang_nhap_that_bai...',
  `doi_tuong` varchar(100) NOT NULL DEFAULT '' COMMENT 'ten tai khoan / ten nhan vat / ma giftcode bi tac dong',
  `chi_tiet` varchar(1000) NOT NULL DEFAULT '' COMMENT 'mo ta ro hon, vd gia tri cu -> gia tri moi',
  `ip` varchar(45) NOT NULL DEFAULT '' COMMENT 'IPv6 dai nhat 45 ky tu',
  `thoi_gian` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_thoi_gian` (`thoi_gian`),
  KEY `idx_admin_user` (`admin_user`),
  KEY `idx_hanh_dong` (`hanh_dong`),
  KEY `idx_doi_tuong` (`doi_tuong`),
  KEY `idx_chan_do_mk` (`ip`,`hanh_dong`,`thoi_gian`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `web_admin_log` WRITE;
/*!40000 ALTER TABLE `web_admin_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `web_admin_log` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `web_chan_spam`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `web_chan_spam` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ip` varchar(45) NOT NULL DEFAULT '' COMMENT 'IPv6 dai nhat 45 ky tu',
  `hanh_dong` varchar(30) NOT NULL DEFAULT '' COMMENT 'dang_ky, tao_don_nap...',
  `thoi_gian` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_dem` (`ip`,`hanh_dong`,`thoi_gian`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `web_chan_spam` WRITE;
/*!40000 ALTER TABLE `web_chan_spam` DISABLE KEYS */;
/*!40000 ALTER TABLE `web_chan_spam` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `web_lenh_nhan_vat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `web_lenh_nhan_vat` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `loai` varchar(30) NOT NULL COMMENT 'dat_cap_do | xoa_vat_pham',
  `char_id` int(11) NOT NULL COMMENT 'tob_char.id',
  `charname` varchar(50) NOT NULL DEFAULT '' COMMENT 'de doc log, khong dung de tim',
  `cap_do` int(11) NOT NULL DEFAULT 0,
  `phan_nghin` int(11) NOT NULL DEFAULT 0 COMMENT '% vao cap x10, 0..999',
  `tham_so` text DEFAULT NULL COMMENT 'xoa_vat_pham: "<cot>><mau>"',
  `admin_user` varchar(50) NOT NULL DEFAULT '' COMMENT 'team_user.username nguoi bam',
  `trang_thai` tinyint(4) NOT NULL DEFAULT 0 COMMENT '0 cho, 1 online da xong, 2 (cu) da ghi CSDL, 3 loi, 4 cho vao game, 5 huy',
  `ket_qua` varchar(255) NOT NULL DEFAULT '' COMMENT 'server ghi: viec da lam, hoac ly do loi',
  `tao_luc` datetime NOT NULL DEFAULT current_timestamp(),
  `xu_ly_luc` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_trang_thai` (`trang_thai`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `web_lenh_nhan_vat` WRITE;
/*!40000 ALTER TABLE `web_lenh_nhan_vat` DISABLE KEYS */;
/*!40000 ALTER TABLE `web_lenh_nhan_vat` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

