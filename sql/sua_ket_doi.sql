-- =====================================================================================================
-- Lễ kết đôi (thay hệ thống đám cưới cũ) + nhóm trứng (Egg Group) theo Pokémon gốc.
--
-- Chạy lại được (MariaDB 10.3, utf8): bảng tạo IF NOT EXISTS; dữ liệu nhóm trứng INSERT IGNORE (không ghi đè dòng
-- admin đã sửa); đổi tên chỉ đổi ô còn giữ tên CŨ (ô đã sửa tay giữ nguyên).
--
--   data_nhom_trung_ten  15 nhóm trứng (id theo thứ tự trong game gốc), tên hiện, ket_doi_duoc = 0 cho nhóm
--                        "Chưa khám phá" (Pokémon bé như Pichu: không kết đôi với ai, kể cả cùng nhóm).
--   data_nhom_trung      nhóm trứng của MỌI loài Pokémon người chơi điều khiển (5 dòng khởi đầu + tiến hoá +
--                        8 nhánh Eevee = 21 loài, com.htbb.pokemon.LoaiPokemon). Không có luật Ditto.
--
-- Điều kiện kết đôi (server real.KetDoiGame / com.htbb.pokemon.KetDoi): hai loài HIỆN TẠI chung ít nhất một nhóm
-- trứng, xét LÚC ĐĂNG KÝ Lễ kết đôi; cặp đã kết đôi từ trước giữ nguyên (tiến hoá không xét lại).
-- Server nạp hai bảng lúc khởi động / lần đăng ký đầu; thiếu bảng thì từ chối đăng ký và báo trong log.
--
-- Đổi tên hiển thị: Thiệp cưới -> Thiệp mời kết đôi (92), Nhẫn Trái tim -> Nhẫn kết đôi (88..90), vật phẩm dịch
-- chuyển tới bạn kết đôi Vảy trái tim -> Vảy trái tim kết đôi (thuốc 98, data_item 751), bản đồ 225 / 226
-- Lễ cưới 1 / 2 -> Lễ kết đôi 1 / 2. Test: server/test/KiemKetDoi.java đọc thẳng tệp này.
-- =====================================================================================================

SET NAMES utf8;

CREATE TABLE IF NOT EXISTS `data_nhom_trung_ten` (
  `id` TINYINT UNSIGNED NOT NULL,
  `ma` VARCHAR(20) NOT NULL,
  `ten` VARCHAR(50) NOT NULL,
  `ket_doi_duoc` TINYINT NOT NULL DEFAULT 1,
  `sua_luc` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `data_nhom_trung` (
  `dex` SMALLINT UNSIGNED NOT NULL,
  `ten_loai` VARCHAR(30) NOT NULL,
  `nhom1` TINYINT UNSIGNED NOT NULL,
  `nhom2` TINYINT UNSIGNED DEFAULT NULL,
  `sua_luc` DATETIME DEFAULT NULL,
  PRIMARY KEY (`dex`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT IGNORE INTO `data_nhom_trung_ten` (`id`, `ma`, `ten`, `ket_doi_duoc`) VALUES
(1,'monster','Quái vật',1),
(2,'water1','Nước 1',1),
(3,'bug','Côn trùng',1),
(4,'flying','Bay',1),
(5,'field','Đồng cỏ',1),
(6,'fairy','Tiên',1),
(7,'grass','Thực vật',1),
(8,'humanlike','Dạng người',1),
(9,'water3','Nước 3',1),
(10,'mineral','Khoáng vật',1),
(11,'amorphous','Vô định hình',1),
(12,'water2','Nước 2',1),
(13,'ditto','Ditto',1),
(14,'dragon','Rồng',1),
(15,'undiscovered','Chưa khám phá',0);

INSERT IGNORE INTO `data_nhom_trung` (`dex`, `ten_loai`, `nhom1`, `nhom2`) VALUES
(4,'Charmander',1,14),
(5,'Charmeleon',1,14),
(6,'Charizard',1,14),
(1,'Bulbasaur',1,7),
(2,'Ivysaur',1,7),
(3,'Venusaur',1,7),
(7,'Squirtle',1,2),
(8,'Wartortle',1,2),
(9,'Blastoise',1,2),
(172,'Pichu',15,NULL),
(25,'Pikachu',5,6),
(26,'Raichu',5,6),
(133,'Eevee',5,NULL),
(134,'Vaporeon',5,NULL),
(135,'Jolteon',5,NULL),
(136,'Flareon',5,NULL),
(196,'Espeon',5,NULL),
(197,'Umbreon',5,NULL),
(470,'Leafeon',5,NULL),
(471,'Glaceon',5,NULL),
(700,'Sylveon',5,NULL);

-- Tên vật phẩm (chỉ đổi ô còn tên cũ: tên kiếm hiệp gốc hoặc tên sau pokemon_doi_chu.sql trước đợt này).
UPDATE `data_potion` SET `name`='Nhẫn kết đôi đồng 2%pt', `name2`='nhanketdoidongpt'
  WHERE `id`=88 AND `name` IN ('Nhẫn Trái tim đồng 2%pt','Nhẫn Bách niên giai lão 2%pt');
UPDATE `data_potion` SET `name`='Nhẫn kết đôi bạc 4%pt', `name2`='nhanketdoibacpt'
  WHERE `id`=89 AND `name` IN ('Nhẫn Trái tim bạc 4%pt','Nhẫn Vĩnh kết đồng tâm 4%pt');
UPDATE `data_potion` SET `name`='Nhẫn kết đôi vàng 7%pt', `name2`='nhanketdoivangpt'
  WHERE `id`=90 AND `name` IN ('Nhẫn Trái tim vàng 7%pt','Nhẫn Loan phụng hòa minh 7%pt');
UPDATE `data_potion` SET `name`='Thiệp mời kết đôi', `name2`='thiepmoiketdoi'
  WHERE `id`=92 AND `name`='Thiệp cưới';
UPDATE `data_potion` SET `name`='Vảy trái tim kết đôi', `name2`='vaytraitimketdoi'
  WHERE `id`=98 AND `name` IN ('Vảy trái tim','Phù phu thê');
UPDATE `data_item` SET `name`='Vảy trái tim kết đôi'
  WHERE `id`=751 AND `name` IN ('Vảy trái tim','Phù phu thê');

-- Phần thưởng sự kiện Valentine (tuần 8) là chính vật phẩm thuốc 98.
UPDATE `data_noi_dung_su_kien_vat_pham` SET `vat_pham`='Vảy trái tim kết đôi'
  WHERE `anh_xa`='thuoc:98' AND `vat_pham`='Vảy trái tim';

-- Tên bản đồ (game tự nạp lại data_map_ten trong 30 giây khi sua_luc đổi).
UPDATE `data_map_ten` SET `ten`='Lễ kết đôi 1', `sua_boi`='sua_ket_doi.sql', `sua_luc`=NOW()
  WHERE `map_id`=225 AND `ten`='Lễ cưới 1';
UPDATE `data_map_ten` SET `ten`='Lễ kết đôi 2', `sua_boi`='sua_ket_doi.sql', `sua_luc`=NOW()
  WHERE `map_id`=226 AND `ten`='Lễ cưới 2';
