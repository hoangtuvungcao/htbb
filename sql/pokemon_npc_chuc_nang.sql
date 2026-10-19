-- Migration production (chay lai duoc): NPC va menu NPC dung duoc het (kiem toan NPC 19/10/2026, nhanh npcfix).
--   mysql htbb_db < sql/pokemon_npc_chuc_nang.sql
-- Cung du lieu da nam trong sql/htbb_day_du.sql va sql/pokemon_ten_hien.sql (CSDL moi khong can chay file nay).
--
--  * Heracross (data_npc type 25) nay dung o Cao nguyen Indigo (cMap 201, o 37,15): 13 nhiem vu (1203 "Heracross giữ Gym"
--    va 12 nhiem vu doi 5001..5012) giao / tra o Heracross ma truoc day NPC nay khong co tren ban do nao.
--  * chuc_vu dung chuc nang that cua NPC (server real.NpcDichVu): Chansey = Trung tam Pokemon (hoi phuc + ban thuoc),
--    Persian = ban da quy / nguyen lieu (goi 23 kieu 3, khong phai giu do), Meowth = ngan hang xu / luong (kho xu luong,
--    chuyen luong, cua hang va nap khi bat) thay cho "Nạp xu" SMS cu.
-- Chi doi dong con dung gia tri mac dinh cu (GIU chuc_vu admin da sua o /admin/ten-hien). sua_luc = NOW() de server /
-- web nap lai trong <= 30 giay (com.htbb.pokemon.TenHien).
SET NAMES utf8;

UPDATE `data_npc` SET `ban_do` = '201', `sua_luc` = NOW() WHERE `type` = 25 AND `ban_do` = '';
UPDATE `data_npc` SET `chuc_vu` = 'Nhiệm vụ đội, Chiếm Gym', `sua_luc` = NOW()
 WHERE `type` = 25 AND `chuc_vu` = 'Chỉ huy giải Liên minh';
UPDATE `data_npc` SET `chuc_vu` = 'Trung tâm Pokémon: hồi phục, bán thuốc', `sua_luc` = NOW()
 WHERE `type` = 0 AND `chuc_vu` = 'Bán thuốc hồi phục';
UPDATE `data_npc` SET `chuc_vu` = 'Bán đá quý, nguyên liệu', `sua_luc` = NOW()
 WHERE `type` IN (6, 29) AND `chuc_vu` = 'Giữ đồ';
UPDATE `data_npc` SET `chuc_vu` = 'Ngân hàng xu, lượng', `sua_luc` = NOW()
 WHERE `type` IN (22, 31) AND `chuc_vu` = 'Nạp xu';
