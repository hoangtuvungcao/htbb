-- Thuat ngu chuan (docs/thiet-ke-game.md): doi thuat ngu cu con sot trong DU LIEU GOC.
-- SINH TU tools/pokemon/sinh_sql_thuat_ngu.py - KHONG SUA TAY (sua tu dien ChuPokemon.DOI roi sinh lai).
-- Chay SAU moi file sql/pokemon_* khac (xuat_sql_day_du.sh: buoc THUAT_NGU, cuoi cung).
-- Chay lai an toan: moi cau chi khop o con dung chu cu (WHERE ... AND cot=<chu cu>).
SET NAMES utf8;
-- htbb_db.data_attribute.decript [vũ khí]
UPDATE `data_attribute` SET `decript`='Chỉ số đồ cầm tay tăng' WHERE `id`='116' AND `decript`='Chỉ số vũ khí tăng';
-- htbb_db.data_attribute.decript [Nguyệt ảnh]
UPDATE `data_attribute` SET `decript`='Ánh Trăng gây st(10%-15%) tỷ lệ xuất hiện' WHERE `id`='119' AND `decript`='Nguyệt ảnh gây st(10%-15%) tỷ lệ xuất hiện';
-- htbb_db.data_attribute.decript [Kháng hoả]
UPDATE `data_attribute` SET `decript`='Kháng Lửa' WHERE `id`='120' AND `decript`='Kháng hoả';
-- htbb_db.data_attribute.decript [Kháng thuỷ]
UPDATE `data_attribute` SET `decript`='Kháng Nước' WHERE `id`='121' AND `decript`='Kháng thuỷ';
-- htbb_db.data_attribute.decript [Kháng mộc]
UPDATE `data_attribute` SET `decript`='Kháng Cỏ' WHERE `id`='122' AND `decript`='Kháng mộc';
-- htbb_db.data_attribute.decript [Kháng kim]
UPDATE `data_attribute` SET `decript`='Kháng Thép' WHERE `id`='123' AND `decript`='Kháng kim';
-- htbb_db.data_attribute.decript [Kháng thổ]
UPDATE `data_attribute` SET `decript`='Kháng Đất' WHERE `id`='124' AND `decript`='Kháng thổ';
-- htbb_db.data_monster.name [cổng thành]
UPDATE `data_monster` SET `name`='cổng Gym' WHERE `id`='83' AND `name`='cổng thành';
-- ===== Nang cap may chu dang chay (ten da doi trong dot nay; CSDL moi khong khop o nao) =====
-- nang cap: data_attribute.name
UPDATE `data_attribute` SET `name`='Chỉ số đồ cầm tay tăng' WHERE `id`='116' AND `name`='Chỉ số vũ khí tăng';
-- nang cap: data_item.name
UPDATE `data_item` SET `name`='Ruy băng world cup' WHERE `id`='608' AND `name`='Phi phong world cup';
-- nang cap: data_item.name
UPDATE `data_item` SET `name`='Ruy băng lông thú' WHERE `id`='616' AND `name`='Phi phong lông thú';
-- nang cap: data_item.name
UPDATE `data_item` SET `name`='Vé Mê cung 5x' WHERE `id`='678' AND `name`='Vé Tháp Pokémon 5x';
-- nang cap: data_item.name
UPDATE `data_item` SET `name`='Vé Mê cung 6x' WHERE `id`='679' AND `name`='Vé Tháp Pokémon 6x';
-- nang cap: data_item.name
UPDATE `data_item` SET `name`='Vé Mê cung 7x' WHERE `id`='680' AND `name`='Vé Tháp Pokémon 7x';
-- nang cap: data_item.name
UPDATE `data_item` SET `name`='Ruy băng lông công' WHERE `id`='742' AND `name`='Phi phong lông công';
-- nang cap: data_item.name
UPDATE `data_item` SET `name`='Ruy Băng Hoenn' WHERE `id`='750' AND `name`='Phi Phong Hoenn';
-- nang cap: data_item.name
UPDATE `data_item` SET `name`='Ruy Băng bóng đá' WHERE `id`='795' AND `name`='Phi Phong bóng đá';
-- nang cap: data_item.name
UPDATE `data_item` SET `name`='Ruy băng vô hình' WHERE `id`='910' AND `name`='Phi phong vô hình';
-- nang cap: data_nhiem_vu_noi_dung.mo_ta
UPDATE `data_nhiem_vu_noi_dung` SET `mo_ta`='Mang Phong bì xanh tới cho Persian. Quý ông giàu có này là khách quý của chuyến tàu.' WHERE `ma`='C13.4' AND `mo_ta`='Mang Phong bì xanh tới cho Persian. Phú ông là khách quý của chuyến tàu.';
-- nang cap: data_nhiem_vu_noi_dung.hoi_thoai_bat_dau
UPDATE `data_nhiem_vu_noi_dung` SET `hoi_thoai_bat_dau`='Meowth: Mang Phong bì xanh tới cho Persian. Quý ông giàu có này là khách quý của chuyến tàu.' WHERE `ma`='C13.4' AND `hoi_thoai_bat_dau`='Meowth: Mang Phong bì xanh tới cho Persian. Phú ông là khách quý của chuyến tàu.';
-- nang cap: data_noi_dung_gym.thuong_lan_dau
UPDATE `data_noi_dung_gym` SET `thuong_lan_dau`='Huy hiệu Đá (Pewter) + 1 món Hoàn mỹ bậc đồng (đồ cầm tay theo dòng)' WHERE `stt`='1' AND `thuong_lan_dau`='Huy hiệu Đá (Pewter) + 1 món Hoàn mỹ bậc đồng (vũ khí theo dòng)';
-- nang cap: data_noi_dung_gym.thuong_lan_dau
UPDATE `data_noi_dung_gym` SET `thuong_lan_dau`='Huy hiệu Thác (Cerulean) + 1 món Hoàn mỹ bậc đồng (đồ cầm tay theo dòng)' WHERE `stt`='2' AND `thuong_lan_dau`='Huy hiệu Thác (Cerulean) + 1 món Hoàn mỹ bậc đồng (vũ khí theo dòng)';
-- nang cap: data_noi_dung_gym.thuong_lan_dau
UPDATE `data_noi_dung_gym` SET `thuong_lan_dau`='Huy hiệu Sấm (Vermilion) + 1 món Hoàn mỹ bậc bạc (đồ cầm tay theo dòng)' WHERE `stt`='3' AND `thuong_lan_dau`='Huy hiệu Sấm (Vermilion) + 1 món Hoàn mỹ bậc bạc (vũ khí theo dòng)';
-- nang cap: data_noi_dung_gym.thuong_lan_dau
UPDATE `data_noi_dung_gym` SET `thuong_lan_dau`='Huy hiệu Cầu Vồng (Celadon) + 1 món Hoàn mỹ bậc vàng (đồ cầm tay theo dòng)' WHERE `stt`='4' AND `thuong_lan_dau`='Huy hiệu Cầu Vồng (Celadon) + 1 món Hoàn mỹ bậc vàng (vũ khí theo dòng)';
-- nang cap: data_noi_dung_gym.thuong_lan_dau
UPDATE `data_noi_dung_gym` SET `thuong_lan_dau`='Huy hiệu Đầm Lầy (Saffron) + 1 món Hoàn mỹ bậc vàng (đồ cầm tay theo dòng)' WHERE `stt`='5' AND `thuong_lan_dau`='Huy hiệu Đầm Lầy (Saffron) + 1 món Hoàn mỹ bậc vàng (vũ khí theo dòng)';
-- nang cap: data_noi_dung_gym.thuong_lan_dau
UPDATE `data_noi_dung_gym` SET `thuong_lan_dau`='Huy hiệu Linh Hồn (Fuchsia) + 1 món Hoàn mỹ bậc bạch kim (đồ cầm tay theo dòng)' WHERE `stt`='6' AND `thuong_lan_dau`='Huy hiệu Linh Hồn (Fuchsia) + 1 món Hoàn mỹ bậc bạch kim (vũ khí theo dòng)';
-- nang cap: data_noi_dung_gym.thuong_lan_dau
UPDATE `data_noi_dung_gym` SET `thuong_lan_dau`='Huy hiệu Núi Lửa (Cinnabar) + 1 món Hoàn mỹ bậc bạch kim (đồ cầm tay theo dòng)' WHERE `stt`='7' AND `thuong_lan_dau`='Huy hiệu Núi Lửa (Cinnabar) + 1 món Hoàn mỹ bậc bạch kim (vũ khí theo dòng)';
-- nang cap: data_noi_dung_gym.thuong_lan_dau
UPDATE `data_noi_dung_gym` SET `thuong_lan_dau`='Huy hiệu Đất (Viridian) + 1 món Hoàn mỹ bậc bạch kim (đồ cầm tay theo dòng)' WHERE `stt`='8' AND `thuong_lan_dau`='Huy hiệu Đất (Viridian) + 1 món Hoàn mỹ bậc bạch kim (vũ khí theo dòng)';
-- nang cap: data_noi_dung_gym.thuong_lan_dau
UPDATE `data_noi_dung_gym` SET `thuong_lan_dau`='Huy hiệu Gió (Violet) + 1 món Hoàn mỹ bậc kim cương (đồ cầm tay theo dòng)' WHERE `stt`='9' AND `thuong_lan_dau`='Huy hiệu Gió (Violet) + 1 món Hoàn mỹ bậc kim cương (vũ khí theo dòng)';
-- nang cap: data_noi_dung_gym.thuong_lan_dau
UPDATE `data_noi_dung_gym` SET `thuong_lan_dau`='Huy hiệu Tổ Ong (Azalea) + 1 món Hoàn mỹ bậc kim cương (đồ cầm tay theo dòng)' WHERE `stt`='10' AND `thuong_lan_dau`='Huy hiệu Tổ Ong (Azalea) + 1 món Hoàn mỹ bậc kim cương (vũ khí theo dòng)';
-- nang cap: data_noi_dung_gym.thuong_lan_dau
UPDATE `data_noi_dung_gym` SET `thuong_lan_dau`='Huy hiệu Đồng Bằng (Goldenrod) + 1 món Hoàn mỹ bậc chuyên gia (đồ cầm tay theo dòng)' WHERE `stt`='11' AND `thuong_lan_dau`='Huy hiệu Đồng Bằng (Goldenrod) + 1 món Hoàn mỹ bậc chuyên gia (vũ khí theo dòng)';
-- nang cap: data_noi_dung_gym.thuong_lan_dau
UPDATE `data_noi_dung_gym` SET `thuong_lan_dau`='Huy hiệu Sương (Ecruteak) + 1 món Hoàn mỹ bậc chuyên gia (đồ cầm tay theo dòng)' WHERE `stt`='12' AND `thuong_lan_dau`='Huy hiệu Sương (Ecruteak) + 1 món Hoàn mỹ bậc chuyên gia (vũ khí theo dòng)';
-- nang cap: data_noi_dung_gym.thuong_lan_dau
UPDATE `data_noi_dung_gym` SET `thuong_lan_dau`='Huy hiệu Khoáng (Olivine) + 1 món Hoàn mỹ bậc chuyên gia (đồ cầm tay theo dòng)' WHERE `stt`='13' AND `thuong_lan_dau`='Huy hiệu Khoáng (Olivine) + 1 món Hoàn mỹ bậc chuyên gia (vũ khí theo dòng)';
-- nang cap: data_noi_dung_gym.thuong_lan_dau
UPDATE `data_noi_dung_gym` SET `thuong_lan_dau`='Huy hiệu Bão (Cianwood) + 1 món Hoàn mỹ bậc chuyên gia (đồ cầm tay theo dòng)' WHERE `stt`='14' AND `thuong_lan_dau`='Huy hiệu Bão (Cianwood) + 1 món Hoàn mỹ bậc chuyên gia (vũ khí theo dòng)';
-- nang cap: data_noi_dung_gym.thuong_lan_dau
UPDATE `data_noi_dung_gym` SET `thuong_lan_dau`='Huy hiệu Băng (Mahogany) + 1 món Hoàn mỹ bậc tinh anh (đồ cầm tay theo dòng)' WHERE `stt`='15' AND `thuong_lan_dau`='Huy hiệu Băng (Mahogany) + 1 món Hoàn mỹ bậc tinh anh (vũ khí theo dòng)';
-- nang cap: data_noi_dung_gym.thuong_lan_dau
UPDATE `data_noi_dung_gym` SET `thuong_lan_dau`='Huy hiệu Rồng (Blackthorn) + 1 món Hoàn mỹ bậc huyền thoại (đồ cầm tay theo dòng)' WHERE `stt`='16' AND `thuong_lan_dau`='Huy hiệu Rồng (Blackthorn) + 1 món Hoàn mỹ bậc huyền thoại (vũ khí theo dòng)';
-- nang cap: data_potion.name
UPDATE `data_potion` SET `name`='Hộp quà may mắn\nQuà bình chọn World Cup.\nBấm hai lần để mở, nhận ngẫu nhiên ruy băng, nguyên liệu, xu/lượng hoặc pet.' WHERE `id`='108' AND `name`='Hộp quà may mắn\nQuà bình chọn World Cup.\nBấm hai lần để mở, nhận ngẫu nhiên phi phong, nguyên liệu, xu/lượng hoặc pet.';
-- nang cap: data_quest_lau_dai.ten
UPDATE `data_quest_lau_dai` SET `ten`='Cùng nhau giữ thành phố' WHERE `ma`='C73.4' AND `ten`='Cùng nhau giữ thành';
-- nang cap: data_quest_moi.content
UPDATE `data_quest_moi` SET `content`='1Meowth: Mang Phong bì xanh tới cho Persian. Quý ông giàu có này là khách quý của chuyến tàu.' WHERE `id`='1078' AND `content`='1Meowth: Mang Phong bì xanh tới cho Persian. Phú ông là khách quý của chuyến tàu.';
-- nang cap: data_shop.name
UPDATE `data_shop` SET `name`='Ruy băng world cup đặc biệt' WHERE `id`='112' AND `name`='Phi phong world cup đặc biệt';
-- nang cap: data_shop.name
UPDATE `data_shop` SET `name`='Ruy băng world cup vật lý \n -Chỉ số max. Thời hạn 7 ngày' WHERE `id`='115' AND `name`='Phi phong world cup vật lý \n -Chỉ số max. Thời hạn 7 ngày';
-- nang cap: nap_cua_hang.ten
UPDATE `nap_cua_hang` SET `ten`='Đổi tên Pokémon' WHERE `ma`='doi_ten' AND `ten`='Đổi tên nhân vật';
