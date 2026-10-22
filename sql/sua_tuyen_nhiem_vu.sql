-- =====================================================================================================================
--  SUA TUYEN NHIEM VU (nhanh sua-tuyen, server-only, client 0.9.7 tro len)
-- =====================================================================================================================
--
--  Chay SAU sql/htbb_day_du.sql + sql/them_huong_dan_tan_thu.sql. Chay lai nhieu lan van an toan (MariaDB 10.3, utf8).
--  Doi xong khoi dong lai server game (bang nhiem vu / vung dich nap luc khoi dong).
--
--  1. Lang khoi dau theo vung: bang tob_lang_khoi_dau (server ghi khi tao nhan vat moi: Kanto -> Pewter, Johto ->
--     Blackthorn). Nhan vat cu khong co dong -> giu lang dang song (khong ai bi doi cho). Server cung tu tao bang.
--  2. Buoc nhiem vu dia diem: bang data_nhiem_vu_khu_vuc - vung dich (ban do nap + khung o, nhieu dong = vao dong nao cung
--     duoc) cua moi buoc "Đứng tại điểm đánh dấu..." / "Đến ...". Chuoi chinh / phu (-64, real.KhuVucNhiemVuGame): nghe
--     NPC = nhan buoc, chi xong khi dung trong vung (noi_chuyen_sau = 1: toi roi noi chuyen moi xong). Nhiem vu So tay
--     (NhiemVuEngine) dung chung vung. bat = 0: ban do khong toi duoc -> buoc giu nhu cu (ghi_chu = ly do).
--     O dich khong di duoc / khong toi duoc tu cong da doi sang o di duoc gan nhat (va sua "(ô x,y)" trong chu).
--  3. Chu phan thuong: data_quest_moi.infogift noi len varchar(1000) (cu 200: 14 dong bi cat giua chu) va doan sau
--     "Phần thưởng:|" viet lai tu chinh dong thuong (exp, xu, luong, luong khoa, vat pham, thuoc, thuong_khac phat that) -
--     bo huy hieu (nhan khi thang Gym) / trang truyen / trang tri (chi ghi nhat ky). Moi lenh chi ghi khi cot thuong con
--     dung gia tri luc sinh. Server cung sinh lai doan nay luc gui -64 (real.ChuThuongNhiemVuGame).
--
--  Khoi SINH TU DONG: tools/pokemon/sinh_sua_tuyen.py (doc CSDL + server/cMap + bang cong RealController / BanDoHoenn).
--  Kiem: server/test/KiemSuaTuyen (doc tep nay), bot tools/pokemon/kiem_tai/KichBanSuaTuyen.java (st_*).
-- ---------------------------------------------------------------------------------------------------------------------
SET NAMES utf8;

-- 1. Lang khoi dau ------------------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tob_lang_khoi_dau` (
  `id_char` int(11) NOT NULL,
  `vung` tinyint(4) NOT NULL,
  `lang` int(11) NOT NULL,
  `tao_luc` datetime NOT NULL,
  PRIMARY KEY (`id_char`)
) DEFAULT CHARSET=utf8;

-- 2. Vung dich buoc dia diem --------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `data_nhiem_vu_khu_vuc` (
  `ma` varchar(32) NOT NULL,
  `stt` int(11) NOT NULL DEFAULT 0,
  `map_id` int(11) NOT NULL,
  `x1` int(11) NOT NULL,
  `y1` int(11) NOT NULL,
  `x2` int(11) NOT NULL,
  `y2` int(11) NOT NULL,
  `noi_chuyen_sau` tinyint(4) NOT NULL DEFAULT 0,
  `bat` tinyint(4) NOT NULL DEFAULT 1,
  `ten_noi` varchar(128) NOT NULL DEFAULT '',
  `ghi_chu` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`ma`, `stt`)
) DEFAULT CHARSET=utf8;

-- 3. Cot chu phan thuong (200 -> 1000) ---------------------------------------------------------------------------------
ALTER TABLE `data_quest_moi` MODIFY `infogift` varchar(1000) DEFAULT NULL;

-- 4. Chuong 1 (viet tay): C01.5 / C02.4 la buoc dia diem that (loi Chansey / Kecleon: "ra dung cho do", loi xong:
--    "ban dung giua vong co chay xem") - huong dan tan thu da doi chu thanh "Nghe ... ke", nay chu noi dung viec phai lam.
UPDATE `data_quest_moi` SET `scontent` = 'Ra đồng cỏ Pallet, đứng đúng chỗ ngôi sao rơi',
  `infogift` = REPLACE(`infogift`, 'Nhiệm vụ:|Nghe Chansey kể về nơi ngôi sao rơi|Địa điểm:|Làng khởi đầu|',
    'Nhiệm vụ:|Đứng tại chỗ ngôi sao rơi trên đồng cỏ Pallet (ô 53,19)|Địa điểm:|Pallet|')
  WHERE `id` = 1005 AND `mainsub` = 0;
UPDATE `data_quest_moi` SET `scontent` = 'Ra bãi cỏ phía bắc Pallet, đứng chờ kẻ giả mạo',
  `infogift` = REPLACE(`infogift`, 'Nhiệm vụ:|Nghe Kecleon bày cách phục kích|Địa điểm:|Làng khởi đầu|',
    'Nhiệm vụ:|Đứng chờ ở bãi cỏ Pallet (ô 53,19)|Địa điểm:|Pallet|')
  WHERE `id` = 1010 AND `mainsub` = 0;

-- ==== SINH TU DONG (tools/pokemon/sinh_sua_tuyen.py) - BAT DAU ====
-- [khu-vuc] 126 buoc dia diem (dung_toa_do / den_noi)
DELETE FROM `data_nhiem_vu_khu_vuc` WHERE `ma` IN ('C01.5','C02.4','C03.3','C04.4','C05.2','C07.6','C08.5','C12.2','C13.6','C16.6','C17.4','C19.2','C21.4','C23.2','C24.4','C25.5','C27.4','C29.5','C30.2','C31.2','C32.2','C33.2','C34.4','C35.3','C37.3','C38.2','C39.3','C40.2','C41.3','C42.2','C43.3','C46.2','C47.2','C49.3','C50.2','C51.2','C51.5','C51.7','C53.2','C56.2','C57.5','C65.2','C70.4','C74.2','C77.2','C81.2','C82.2','C83.2','C84.2','C85.2','C86.2','C87.2','C89.2','C90.2','C91.2','C92.2','C93.2','C94.2','C95.2','C96.2','C97.2','C98.2','C99.2','C100.2','C103.2','C104.2','P-01.0.4','P-01.1.3','P-02.0.2','P-03.2.2','P-04.2.4','P-05.0.4','P-06.1.2','P-09.0.2','P-09.1.2','P-10.2.2','P-11.0.2','P-11.0.3','P-14.1.2','P-14.1.3','P-15.0.2','P-15.2.3','P-17.0.2','P-18.0.2','P-18.2.2','P-19.2.4','P-21.1.2','P-23.2.2','P-24.2.2','P-27.0.4','P-28.1.2','P-28.1.3','P-29.2.3','P-32.1.2','P-33.1.4','N-1.08','N-1.09','N-2.08','N-2.09','N-3.08','N-3.09','N-4.08','N-4.09','N-5.08','N-5.09','TV-02.1','TV-04.3','TV-07.1','TV-13.1','AN-02','AN-07','AN-10','AN-17','AN-29','HT-ZAP.5','HT-DAR.3','HT-RAY.2','HT-ART.3','HT-MOL.2','HT-MOL.6','HT-RAI.3','HT-SUI.5','HT-LUG.3','HT-HOO.2','HT-CEL.2','HT-MEW.2');
INSERT INTO `data_nhiem_vu_khu_vuc` (`ma`,`stt`,`map_id`,`x1`,`y1`,`x2`,`y2`,`noi_chuyen_sau`,`bat`,`ten_noi`,`ghi_chu`) VALUES
('C01.5',0,1,50,16,56,22,0,1,'Pallet',''),
('C02.4',0,1,50,16,56,22,0,1,'Pallet',''),
('C03.3',0,2,0,0,255,255,0,1,'Viridian',''),
('C04.4',0,11,0,0,255,255,0,1,'Rừng Viridian',''),
('C05.2',0,100,0,0,255,255,0,0,'Nhà (Pewter)','ban do 100 khong toi duoc (khong cong / tau / Pidgeot dan vao)'),
('C07.6',0,13,0,0,255,255,0,1,'Núi Mặt Trăng',''),
('C08.5',0,13,27,27,33,33,0,1,'Núi Mặt Trăng',''),
('C12.2',0,3,47,47,53,53,0,1,'Vermilion',''),
('C13.6',0,22,0,0,255,255,0,0,'Tàu S.S. Anne','ban do 22 khong toi duoc (khong cong / tau / Pidgeot dan vao)'),
('C16.6',0,5,0,0,255,255,0,1,'Lavender',''),
('C17.4',0,5,41,38,47,44,0,1,'Lavender',''),
('C19.2',0,12,0,0,255,255,0,1,'Khu mua bán',''),
('C21.4',0,7,0,0,255,255,0,1,'Saffron',''),
('C23.2',0,14,0,0,255,255,0,1,'Vùng Safari',''),
('C24.4',0,8,37,37,43,43,0,1,'Fuchsia',''),
('C25.5',0,9,0,0,255,255,0,1,'Cinnabar',''),
('C27.4',0,9,0,0,255,255,0,1,'Cinnabar',''),
('C29.5',0,10,68,120,74,126,0,1,'New Bark',''),
('C30.2',0,15,0,0,255,255,0,1,'Cherrygrove',''),
('C31.2',0,20,5,39,11,45,0,1,'Violet',''),
('C32.2',0,21,0,0,255,255,0,0,'Đồng cỏ phía tây','ban do 21 khong toi duoc (khong cong / tau / Pidgeot dan vao)'),
('C33.2',0,23,0,0,255,255,0,1,'Azalea',''),
('C34.4',0,29,19,18,25,24,0,1,'Rừng Ilex (cạnh làng khởi đầu)','o 23,12 khong toi duoc -> 22,21'),
('C34.4',1,31,19,18,25,24,0,1,'Rừng Ilex (cạnh làng khởi đầu)','o 23,12 khong toi duoc -> 22,21'),
('C34.4',2,30,21,8,27,14,0,1,'Rừng Ilex (cạnh làng khởi đầu)','o 23,12 khong toi duoc -> 22,21'),
('C35.3',0,29,19,18,25,24,0,1,'Rừng Ilex (cạnh làng khởi đầu)','o 23,12 khong toi duoc -> 22,21'),
('C35.3',1,31,19,18,25,24,0,1,'Rừng Ilex (cạnh làng khởi đầu)','o 23,12 khong toi duoc -> 22,21'),
('C35.3',2,30,21,8,27,14,0,1,'Rừng Ilex (cạnh làng khởi đầu)','o 23,12 khong toi duoc -> 22,21'),
('C37.3',0,19,52,50,58,56,0,1,'Ecruteak',''),
('C38.2',0,19,0,0,255,255,0,1,'Ecruteak',''),
('C39.3',0,24,18,2,24,8,0,1,'Olivine',''),
('C40.2',0,0,0,0,255,255,0,1,'làng khởi đầu',''),
('C40.2',1,70,0,0,255,255,0,1,'làng khởi đầu',''),
('C40.2',2,80,0,0,255,255,0,1,'làng khởi đầu',''),
('C41.3',0,28,64,18,70,24,0,1,'Mahogany',''),
('C42.2',0,114,0,0,255,255,0,1,'Hồ Phẫn Nộ',''),
('C43.3',0,113,0,0,255,255,0,1,'Hang Tối',''),
('C46.2',0,112,0,0,255,255,0,1,'Hang Salamence',''),
('C47.2',0,110,0,0,255,255,0,1,'Hang động',''),
('C49.3',0,118,0,0,255,255,0,0,'Đường Victory','ban do 118 khong toi duoc (khong cong / tau / Pidgeot dan vao)'),
('C50.2',0,201,0,0,255,255,0,1,'Cao nguyên Indigo',''),
('C51.2',0,41,0,0,255,255,0,0,'Chân núi Silver','ban do 41 khong toi duoc (khong cong / tau / Pidgeot dan vao)'),
('C51.5',0,42,0,0,255,255,0,0,'Núi Silver','ban do 42 khong toi duoc (khong cong / tau / Pidgeot dan vao)'),
('C51.7',0,42,13,26,19,32,0,0,'Núi Silver','ban do 42 khong toi duoc (khong cong / tau / Pidgeot dan vao)'),
('C53.2',0,43,0,0,255,255,0,1,'Littleroot',''),
('C56.2',0,160,0,0,255,255,0,1,'Hang Granite',''),
('C57.5',0,47,13,13,19,19,0,1,'Slateport',''),
('C65.2',0,54,15,35,21,41,0,1,'Fortree',''),
('C70.4',0,57,27,21,33,27,0,1,'Núi Pyre',''),
('C74.2',0,163,0,0,255,255,0,1,'Hang Khởi Nguyên','');
INSERT INTO `data_nhiem_vu_khu_vuc` (`ma`,`stt`,`map_id`,`x1`,`y1`,`x2`,`y2`,`noi_chuyen_sau`,`bat`,`ten_noi`,`ghi_chu`) VALUES
('C77.2',0,164,0,0,255,255,0,1,'Tháp Trời',''),
('C81.2',0,165,0,0,255,255,0,1,'Đường Victory Hoenn',''),
('C82.2',0,281,15,19,21,25,0,1,'Liên minh Hoenn',''),
('C83.2',0,281,39,19,45,25,0,1,'Liên minh Hoenn',''),
('C84.2',0,282,17,13,23,19,0,1,'Sảnh Nhà vô địch',''),
('C85.2',0,62,40,39,46,45,0,1,'Ever Grande',''),
('C86.2',0,56,43,34,49,40,0,1,'Lilycove',''),
('C87.2',0,47,13,13,19,19,0,1,'Slateport',''),
('C89.2',0,61,0,0,255,255,0,1,'Biển Route 131',''),
('C90.2',0,58,0,0,255,255,0,1,'Biển Route 124',''),
('C91.2',0,59,40,35,46,41,0,1,'Mossdeep',''),
('C92.2',0,61,0,0,255,255,0,1,'Biển Route 131',''),
('C93.2',0,43,36,29,42,35,0,1,'Littleroot',''),
('C94.2',0,47,43,34,49,40,0,1,'Slateport',''),
('C95.2',0,62,14,39,20,45,0,1,'Ever Grande',''),
('C96.2',0,281,15,19,21,25,0,1,'Liên minh Hoenn',''),
('C97.2',0,161,18,19,24,25,0,1,'Thác Meteor',''),
('C98.2',0,52,21,43,27,49,0,1,'Núi Chimney',''),
('C99.2',0,163,21,42,27,48,0,1,'Hang Khởi Nguyên',''),
('C100.2',0,282,17,13,23,19,0,1,'Sảnh Nhà vô địch',''),
('C103.2',0,46,25,25,31,31,0,1,'Bờ biển Dewford',''),
('C104.2',0,280,19,15,25,21,0,1,'Đỉnh Tháp Trời',''),
('P-01.0.4',0,1,50,16,56,22,0,1,'Pallet',''),
('P-01.1.3',0,1,50,16,56,22,0,1,'Pallet',''),
('P-02.0.2',0,11,0,0,255,255,0,1,'Rừng Viridian',''),
('P-03.2.2',0,11,65,60,71,66,0,1,'Rừng Viridian',''),
('P-04.2.4',0,0,10,7,16,13,0,1,'làng khởi đầu',''),
('P-04.2.4',1,70,10,7,16,13,0,1,'làng khởi đầu',''),
('P-04.2.4',2,80,10,7,16,13,0,1,'làng khởi đầu',''),
('P-05.0.4',0,13,27,27,33,33,0,1,'Núi Mặt Trăng',''),
('P-06.1.2',0,4,0,0,255,255,0,1,'Cerulean',''),
('P-09.0.2',0,5,41,38,47,44,0,1,'Lavender',''),
('P-09.1.2',0,5,0,0,255,255,0,1,'Lavender',''),
('P-10.2.2',0,6,0,0,255,255,0,1,'Celadon',''),
('P-11.0.2',0,6,0,0,255,255,0,1,'Celadon',''),
('P-11.0.3',0,3,0,0,255,255,0,1,'Vermilion',''),
('P-14.1.2',0,9,0,0,255,255,0,1,'Cinnabar',''),
('P-14.1.3',0,9,30,41,36,47,0,1,'Cinnabar',''),
('P-15.0.2',0,10,68,120,74,126,0,1,'New Bark',''),
('P-15.2.3',0,10,68,120,74,126,0,1,'New Bark',''),
('P-17.0.2',0,15,0,0,255,255,0,1,'Cherrygrove',''),
('P-18.0.2',0,20,5,39,11,45,0,1,'Violet',''),
('P-18.2.2',0,20,5,39,11,45,0,1,'Violet',''),
('P-19.2.4',0,23,44,2,50,8,0,1,'Azalea',''),
('P-21.1.2',0,18,0,0,255,255,0,1,'Goldenrod',''),
('P-23.2.2',0,26,0,0,255,255,0,0,'Rừng Cổ','ban do 26 khong toi duoc (khong cong / tau / Pidgeot dan vao)'),
('P-24.2.2',0,24,0,0,255,255,0,1,'Olivine',''),
('P-27.0.4',0,114,117,116,123,122,0,1,'Hồ Phẫn Nộ',''),
('P-28.1.2',0,0,0,0,255,255,0,1,'làng khởi đầu',''),
('P-28.1.2',1,70,0,0,255,255,0,1,'làng khởi đầu','');
INSERT INTO `data_nhiem_vu_khu_vuc` (`ma`,`stt`,`map_id`,`x1`,`y1`,`x2`,`y2`,`noi_chuyen_sau`,`bat`,`ten_noi`,`ghi_chu`) VALUES
('P-28.1.2',2,80,0,0,255,255,0,1,'làng khởi đầu',''),
('P-28.1.3',0,0,10,7,16,13,0,1,'làng khởi đầu',''),
('P-28.1.3',1,70,10,7,16,13,0,1,'làng khởi đầu',''),
('P-28.1.3',2,80,10,7,16,13,0,1,'làng khởi đầu',''),
('P-29.2.3',0,112,24,8,30,14,0,1,'Hang Salamence',''),
('P-32.1.2',0,201,62,7,68,13,0,1,'Cao nguyên Indigo',''),
('P-33.1.4',0,111,19,22,25,28,0,0,'Hang Darkrai','ban do 111 khong toi duoc (khong cong / tau / Pidgeot dan vao)'),
('N-1.08',0,1,50,16,56,22,0,1,'Pallet',''),
('N-1.09',0,1,0,0,255,255,0,1,'Pallet',''),
('N-2.08',0,4,47,43,53,49,0,1,'Cerulean',''),
('N-2.09',0,4,0,0,255,255,0,1,'Cerulean',''),
('N-3.08',0,8,37,37,43,43,0,1,'Fuchsia',''),
('N-3.09',0,8,0,0,255,255,0,1,'Fuchsia',''),
('N-4.08',0,18,41,51,47,57,0,1,'Goldenrod',''),
('N-4.09',0,18,0,0,255,255,0,1,'Goldenrod',''),
('N-5.08',0,112,24,8,30,14,0,1,'Hang Salamence',''),
('N-5.09',0,112,0,0,255,255,0,1,'Hang Salamence',''),
('TV-02.1',0,13,0,0,255,255,0,1,'Núi Mặt Trăng',''),
('TV-04.3',0,5,41,38,47,44,0,1,'Lavender',''),
('TV-07.1',0,10,68,120,74,126,0,1,'New Bark',''),
('TV-13.1',0,42,0,0,255,255,0,0,'Núi Silver','ban do 42 khong toi duoc (khong cong / tau / Pidgeot dan vao)'),
('AN-02',0,5,41,38,47,44,0,1,'Lavender',''),
('AN-07',0,4,47,43,53,49,0,1,'Cerulean',''),
('AN-10',0,1,50,16,56,22,0,1,'Pallet',''),
('AN-17',0,13,27,27,33,33,0,1,'Núi Mặt Trăng',''),
('AN-29',0,15,6,16,12,22,0,1,'Cherrygrove',''),
('HT-ZAP.5',0,110,47,37,53,43,0,1,'Hang động',''),
('HT-DAR.3',0,111,0,0,255,255,0,0,'Hang Darkrai','ban do 111 khong toi duoc (khong cong / tau / Pidgeot dan vao)'),
('HT-RAY.2',0,42,13,26,19,32,0,0,'Núi Silver','ban do 42 khong toi duoc (khong cong / tau / Pidgeot dan vao)'),
('HT-ART.3',0,42,0,0,255,255,0,0,'Núi Silver','ban do 42 khong toi duoc (khong cong / tau / Pidgeot dan vao)'),
('HT-MOL.2',0,9,0,0,255,255,0,1,'Cinnabar',''),
('HT-MOL.6',0,9,30,41,36,47,0,1,'Cinnabar',''),
('HT-RAI.3',0,28,0,0,255,255,0,1,'Mahogany',''),
('HT-SUI.5',0,114,117,116,123,122,0,1,'Hồ Phẫn Nộ',''),
('HT-LUG.3',0,24,0,0,255,255,0,1,'Olivine',''),
('HT-HOO.2',0,19,52,50,58,56,0,1,'Ecruteak',''),
('HT-CEL.2',0,29,0,0,255,255,0,1,'Rừng Ilex (cạnh làng khởi đầu)',''),
('HT-CEL.2',1,31,0,0,255,255,0,1,'Rừng Ilex (cạnh làng khởi đầu)',''),
('HT-CEL.2',2,30,0,0,255,255,0,1,'Rừng Ilex (cạnh làng khởi đầu)',''),
('HT-MEW.2',0,9,0,0,255,255,0,1,'Cinnabar','');
-- [khu-vuc] 140 dong vung, 13 buoc tat (giu noi chuyen la xong):
--   C05.2 (den_noi, ban do 100): ban do 100 khong toi duoc (khong cong / tau / Pidgeot dan vao)
--   C13.6 (den_noi, ban do 22): ban do 22 khong toi duoc (khong cong / tau / Pidgeot dan vao)
--   C32.2 (den_noi, ban do 21): ban do 21 khong toi duoc (khong cong / tau / Pidgeot dan vao)
--   C49.3 (den_noi, ban do 118): ban do 118 khong toi duoc (khong cong / tau / Pidgeot dan vao)
--   C51.2 (den_noi, ban do 41): ban do 41 khong toi duoc (khong cong / tau / Pidgeot dan vao)
--   C51.5 (den_noi, ban do 42): ban do 42 khong toi duoc (khong cong / tau / Pidgeot dan vao)
--   C51.7 (dung_toa_do, ban do 42): ban do 42 khong toi duoc (khong cong / tau / Pidgeot dan vao)
--   P-23.2.2 (den_noi, ban do 26): ban do 26 khong toi duoc (khong cong / tau / Pidgeot dan vao)
--   P-33.1.4 (dung_toa_do, ban do 111): ban do 111 khong toi duoc (khong cong / tau / Pidgeot dan vao)
--   TV-13.1 (den_noi, ban do 42): ban do 42 khong toi duoc (khong cong / tau / Pidgeot dan vao)
--   HT-DAR.3 (den_noi, ban do 111): ban do 111 khong toi duoc (khong cong / tau / Pidgeot dan vao)
--   HT-RAY.2 (dung_toa_do, ban do 42): ban do 42 khong toi duoc (khong cong / tau / Pidgeot dan vao)
--   HT-ART.3 (den_noi, ban do 42): ban do 42 khong toi duoc (khong cong / tau / Pidgeot dan vao)
-- [khu-vuc-chu] 4 buoc doi chu o / lang
UPDATE `data_nhiem_vu_noi_dung` SET `toa_do` = '22;21', `muc_tieu_text` = REPLACE(`muc_tieu_text`, ' trên Rừng Ilex (ô 23,12)', ' trên Rừng Ilex, cạnh làng khởi đầu') WHERE `ma` = 'C34.4' AND `toa_do` = '23;12';
UPDATE `data_quest_lau_dai` SET `muc_tieu_ten` = REPLACE(`muc_tieu_ten`, ' trên Rừng Ilex (ô 23,12)', ' trên Rừng Ilex, cạnh làng khởi đầu') WHERE `ma` = 'C34.4';
UPDATE `data_quest_moi` SET `scontent` = REPLACE(`scontent`, ' trên Rừng Ilex (ô 23,12)', ' trên Rừng Ilex, cạnh làng khởi đầu'), `infogift` = REPLACE(`infogift`, ' trên Rừng Ilex (ô 23,12)', ' trên Rừng Ilex, cạnh làng khởi đầu') WHERE `id` = 1208;
UPDATE `data_nhiem_vu_noi_dung` SET `toa_do` = '22;21', `muc_tieu_text` = REPLACE(`muc_tieu_text`, ' trên Rừng Ilex (ô 23,12)', ' trên Rừng Ilex, cạnh làng khởi đầu') WHERE `ma` = 'C35.3' AND `toa_do` = '23;12';
UPDATE `data_quest_lau_dai` SET `muc_tieu_ten` = REPLACE(`muc_tieu_ten`, ' trên Rừng Ilex (ô 23,12)', ' trên Rừng Ilex, cạnh làng khởi đầu') WHERE `ma` = 'C35.3';
UPDATE `data_quest_moi` SET `scontent` = REPLACE(`scontent`, ' trên Rừng Ilex (ô 23,12)', ' trên Rừng Ilex, cạnh làng khởi đầu'), `infogift` = REPLACE(`infogift`, ' trên Rừng Ilex (ô 23,12)', ' trên Rừng Ilex, cạnh làng khởi đầu') WHERE `id` = 1213;
UPDATE `data_nhiem_vu_noi_dung` SET `muc_tieu_text` = REPLACE(`muc_tieu_text`, ' trên Pewter (ô 13,10)', ' trong làng khởi đầu') WHERE `ma` = 'P-04.2.4';
UPDATE `data_quest_lau_dai` SET `muc_tieu_ten` = REPLACE(`muc_tieu_ten`, ' trên Pewter (ô 13,10)', ' trong làng khởi đầu') WHERE `ma` = 'P-04.2.4';
UPDATE `data_quest_moi` SET `scontent` = REPLACE(`scontent`, ' trên Pewter (ô 13,10)', ' trong làng khởi đầu'), `infogift` = REPLACE(`infogift`, ' trên Pewter (ô 13,10)', ' trong làng khởi đầu') WHERE `id` = 2055;
UPDATE `data_nhiem_vu_noi_dung` SET `muc_tieu_text` = REPLACE(`muc_tieu_text`, ' trên Blackthorn (ô 13,10)', ' trong làng khởi đầu') WHERE `ma` = 'P-28.1.3';
UPDATE `data_quest_lau_dai` SET `muc_tieu_ten` = REPLACE(`muc_tieu_ten`, ' trên Blackthorn (ô 13,10)', ' trong làng khởi đầu') WHERE `ma` = 'P-28.1.3';
UPDATE `data_quest_moi` SET `scontent` = REPLACE(`scontent`, ' trên Blackthorn (ô 13,10)', ' trong làng khởi đầu'), `infogift` = REPLACE(`infogift`, ' trên Blackthorn (ô 13,10)', ' trong làng khởi đầu') WHERE `id` = 2345;
-- [thuong] 79 / 757 dong infogift viet lai tu dong thuong
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '6.461 EXP, 1.615 xu, 4 lượng khoá, Đai sơ cấp') WHERE `id` = 1006 AND `mainsub` = 0 AND `exp` = 6461 AND `gold` = 1615 AND `luong` = 0 AND `luongLock` = 4 AND `itemgift` = '27,27,27,27,27,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '11.854 EXP, 1.922 xu, 4 lượng khoá, Găng đấm sơ cấp') WHERE `id` = 1012 AND `mainsub` = 0 AND `exp` = 11854 AND `gold` = 1922 AND `luong` = 0 AND `luongLock` = 4 AND `itemgift` = '150,150,150,150,150,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '21.346 EXP, 2.070 xu, 4 lượng khoá, Huy hiệu sơ cấp') WHERE `id` = 1018 AND `mainsub` = 0 AND `exp` = 21346 AND `gold` = 2070 AND `luong` = 0 AND `luongLock` = 4 AND `itemgift` = '114,114,114,114,114,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '44.678 EXP, 2.043 xu, 3 lượng khoá, Khăn quàng sơ cấp') WHERE `id` = 1025 AND `mainsub` = 0 AND `exp` = 44678 AND `gold` = 2043 AND `luong` = 0 AND `luongLock` = 3 AND `itemgift` = '1,1,1,1,1,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '93.689 EXP, 2.498 xu, 4 lượng khoá, Lông vũ tốc độ đồng') WHERE `id` = 1031 AND `mainsub` = 0 AND `exp` = 93689 AND `gold` = 2498 AND `luong` = 0 AND `luongLock` = 4 AND `itemgift` = '140,140,140,140,140,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '130.264 EXP, 3.250 xu, 10 lượng khoá, Phiến đá hệ đồng, Huy chương Gym x3') WHERE `id` = 1037 AND `mainsub` = 0 AND `exp` = 130264 AND `gold` = 3250 AND `luong` = 0 AND `luongLock` = 10 AND `itemgift` = '164,164,164,164,164,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '143.434 EXP, 3.077 xu, 4 lượng khoá, Ruy băng đồng') WHERE `id` = 1043 AND `mainsub` = 0 AND `exp` = 143434 AND `gold` = 3077 AND `luong` = 0 AND `luongLock` = 4 AND `itemgift` = '1002,1002,1002,1002,1002,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '31.884 EXP, 604 xu, 1 lượng khoá') WHERE `id` = 1049 AND `mainsub` = 0 AND `exp` = 31884 AND `gold` = 604 AND `luong` = 0 AND `luongLock` = 1 AND `itemgift` = '32000' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '51.014 EXP, 967 xu, 3 lượng khoá, Vòng cổ đồng') WHERE `id` = 1050 AND `mainsub` = 0 AND `exp` = 51014 AND `gold` = 967 AND `luong` = 0 AND `luongLock` = 3 AND `itemgift` = '128,128,128,128,128,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '72.994 EXP, 1.213 xu, 4 lượng khoá, Than lửa đồng / Hạt giống thần đồng / Khăn lụa đồng / Nước thần đồng / Nam châm đồng (theo dòng Pokémon)') WHERE `id` = 1056 AND `mainsub` = 0 AND `exp` = 72994 AND `gold` = 1213 AND `luong` = 0 AND `luongLock` = 4 AND `itemgift` = '81,88,95,102,109,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '91.128 EXP, 1.465 xu, 10 lượng khoá, Than lửa đồng / Hạt giống thần đồng / Khăn lụa đồng / Nước thần đồng / Nam châm đồng (theo dòng Pokémon), Huy chương Gym x3') WHERE `id` = 1062 AND `mainsub` = 0 AND `exp` = 91128 AND `gold` = 1465 AND `luong` = 0 AND `luongLock` = 10 AND `itemgift` = '81,88,95,102,109,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '97.402 EXP, 1.398 xu, 4 lượng khoá, Than lửa bạc / Hạt giống thần bạc / Khăn lụa bạc / Nước thần bạc / Nam châm bạc (theo dòng Pokémon)') WHERE `id` = 1068 AND `mainsub` = 0 AND `exp` = 97402 AND `gold` = 1398 AND `luong` = 0 AND `luongLock` = 4 AND `itemgift` = '83,90,97,104,111,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '127.412 EXP, 1.790 xu, 10 lượng khoá, Than lửa bạc / Hạt giống thần bạc / Khăn lụa bạc / Nước thần bạc / Nam châm bạc (theo dòng Pokémon), Huy chương Gym x3') WHERE `id` = 1074 AND `mainsub` = 0 AND `exp` = 127412 AND `gold` = 1790 AND `luong` = 0 AND `luongLock` = 10 AND `itemgift` = '83,90,97,104,111,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '132.052 EXP, 1.672 xu, 4 lượng khoá, Than lửa bạc / Hạt giống thần bạc / Khăn lụa bạc / Nước thần bạc / Nam châm bạc (theo dòng Pokémon)') WHERE `id` = 1080 AND `mainsub` = 0 AND `exp` = 132052 AND `gold` = 1672 AND `luong` = 0 AND `luongLock` = 4 AND `itemgift` = '83,90,97,104,111,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '142.103 EXP, 1.760 xu, 4 lượng khoá, Băng đô bạc') WHERE `id` = 1086 AND `mainsub` = 0 AND `exp` = 142103 AND `gold` = 1760 AND `luong` = 0 AND `luongLock` = 4 AND `itemgift` = '63,63,63,63,63,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '165.726 EXP, 1.895 xu, 4 lượng khoá, Đai bạc') WHERE `id` = 1092 AND `mainsub` = 0 AND `exp` = 165726 AND `gold` = 1895 AND `luong` = 0 AND `luongLock` = 4 AND `itemgift` = '37,37,37,37,37,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '178.420 EXP, 1.980 xu, 4 lượng khoá, Găng đấm bạc') WHERE `id` = 1098 AND `mainsub` = 0 AND `exp` = 178420 AND `gold` = 1980 AND `luong` = 0 AND `luongLock` = 4 AND `itemgift` = '154,154,154,154,154,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '192.600 EXP, 2.070 xu, 4 lượng khoá, Huy hiệu vàng') WHERE `id` = 1104 AND `mainsub` = 0 AND `exp` = 192600 AND `gold` = 2070 AND `luong` = 0 AND `luongLock` = 4 AND `itemgift` = '120,120,120,120,120,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '137.674 EXP, 1.430 xu, 1 lượng khoá') WHERE `id` = 1109 AND `mainsub` = 0 AND `exp` = 137674 AND `gold` = 1430 AND `luong` = 0 AND `luongLock` = 1 AND `itemgift` = '32000' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '220.278 EXP, 2.289 xu, 4 lượng khoá, Khăn quàng vàng') WHERE `id` = 1110 AND `mainsub` = 0 AND `exp` = 220278 AND `gold` = 2289 AND `luong` = 0 AND `luongLock` = 4 AND `itemgift` = '272,272,272,272,272,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '235.943 EXP, 2.377 xu, 4 lượng khoá, Lông vũ tốc độ vàng') WHERE `id` = 1116 AND `mainsub` = 0 AND `exp` = 235943 AND `gold` = 2377 AND `luong` = 0 AND `luongLock` = 4 AND `itemgift` = '144,144,144,144,144,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '262.497 EXP, 2.613 xu, 10 lượng khoá, Phiến đá hệ vàng, Huy chương Gym x3') WHERE `id` = 1123 AND `mainsub` = 0 AND `exp` = 262497 AND `gold` = 2613 AND `luong` = 0 AND `luongLock` = 10 AND `itemgift` = '168,168,168,168,168,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '288.930 EXP, 2.723 xu, 4 lượng khoá, Ruy băng vàng') WHERE `id` = 1129 AND `mainsub` = 0 AND `exp` = 288930 AND `gold` = 2723 AND `luong` = 0 AND `luongLock` = 4 AND `itemgift` = '584,584,584,584,584,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '322.444 EXP, 3.012 xu, 10 lượng khoá, Vòng cổ vàng, Huy chương Gym x3') WHERE `id` = 1136 AND `mainsub` = 0 AND `exp` = 322444 AND `gold` = 3012 AND `luong` = 0 AND `luongLock` = 10 AND `itemgift` = '132,132,132,132,132,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '353.743 EXP, 3.105 xu, 4 lượng khoá, Than lửa vàng / Hạt giống thần vàng / Khăn lụa vàng / Nước thần vàng / Nam châm vàng (theo dòng Pokémon)') WHERE `id` = 1142 AND `mainsub` = 0 AND `exp` = 353743 AND `gold` = 3105 AND `luong` = 0 AND `luongLock` = 4 AND `itemgift` = '85,92,99,106,113,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '440.871 EXP, 3.975 xu, 10 lượng khoá, Than lửa bạch kim / Hạt giống thần bạch kim / Khăn lụa bạch kim / Nước thần bạch kim / Nam châm bạch kim (theo dòng Pokémon), Huy chương Gym x3') WHERE `id` = 1148 AND `mainsub` = 0 AND `exp` = 440871 AND `gold` = 3975 AND `luong` = 0 AND `luongLock` = 10 AND `itemgift` = '175,183,191,199,207,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '404.064 EXP, 3.568 xu, 4 lượng khoá, Than lửa bạch kim / Hạt giống thần bạch kim / Khăn lụa bạch kim / Nước thần bạch kim / Nam châm bạch kim (theo dòng Pokémon)') WHERE `id` = 1154 AND `mainsub` = 0 AND `exp` = 404064 AND `gold` = 3568 AND `luong` = 0 AND `luongLock` = 4 AND `itemgift` = '175,183,191,199,207,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '432.229 EXP, 3.908 xu, 4 lượng khoá, Than lửa bạch kim / Hạt giống thần bạch kim / Khăn lụa bạch kim / Nước thần bạch kim / Nam châm bạch kim (theo dòng Pokémon)') WHERE `id` = 1160 AND `mainsub` = 0 AND `exp` = 432229 AND `gold` = 3908 AND `luong` = 0 AND `luongLock` = 4 AND `itemgift` = '175,183,191,199,207,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '448.433 EXP, 4.268 xu, 10 lượng khoá, Than lửa bạch kim / Hạt giống thần bạch kim / Khăn lụa bạch kim / Nước thần bạch kim / Nam châm bạch kim (theo dòng Pokémon), Huy chương Gym x3') WHERE `id` = 1167 AND `mainsub` = 0 AND `exp` = 448433 AND `gold` = 4268 AND `luong` = 0 AND `luongLock` = 10 AND `itemgift` = '175,183,191,199,207,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '507.219 EXP, 4.958 xu, 10 lượng khoá, Huy chương Gym x3') WHERE `id` = 1172 AND `mainsub` = 0 AND `exp` = 507219 AND `gold` = 4958 AND `luong` = 0 AND `luongLock` = 10 AND `itemgift` = '32000' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '507.219 EXP, 4.958 xu, 1 lượng khoá, Băng đô bạch kim') WHERE `id` = 1173 AND `mainsub` = 0 AND `exp` = 507219 AND `gold` = 4958 AND `luong` = 0 AND `luongLock` = 1 AND `itemgift` = '71,71,71,71,71,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '508.291 EXP, 4.839 xu, 4 lượng khoá, Đai bạch kim') WHERE `id` = 1179 AND `mainsub` = 0 AND `exp` = 508291 AND `gold` = 4839 AND `luong` = 0 AND `luongLock` = 4 AND `itemgift` = '45,45,45,45,45,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '546.134 EXP, 5.292 xu, 4 lượng khoá, Găng đấm kim cương') WHERE `id` = 1185 AND `mainsub` = 0 AND `exp` = 546134 AND `gold` = 5292 AND `luong` = 0 AND `luongLock` = 4 AND `itemgift` = '160,160,160,160,160,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '660.871 EXP, 6.773 xu, 10 lượng khoá, Huy hiệu kim cương, Huy chương Gym x3') WHERE `id` = 1191 AND `mainsub` = 0 AND `exp` = 660871 AND `gold` = 6773 AND `luong` = 0 AND `luongLock` = 10 AND `itemgift` = '124,124,124,124,124,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '610.218 EXP, 6.046 xu, 4 lượng khoá, Khăn quàng kim cương') WHERE `id` = 1197 AND `mainsub` = 0 AND `exp` = 610218 AND `gold` = 6046 AND `luong` = 0 AND `luongLock` = 4 AND `itemgift` = '23,23,23,23,23,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '384.176 EXP, 3.977 xu, 1 lượng khoá') WHERE `id` = 1202 AND `mainsub` = 0 AND `exp` = 384176 AND `gold` = 3977 AND `luong` = 0 AND `luongLock` = 1 AND `itemgift` = '32000' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '614.682 EXP, 6.364 xu, 10 lượng khoá, Lông vũ tốc độ kim cương, Huy chương Gym x3') WHERE `id` = 1204 AND `mainsub` = 0 AND `exp` = 614682 AND `gold` = 6364 AND `luong` = 0 AND `luongLock` = 10 AND `itemgift` = '148,148,148,148,148,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '681.681 EXP, 6.906 xu, 4 lượng khoá, Phiến đá hệ kim cương') WHERE `id` = 1210 AND `mainsub` = 0 AND `exp` = 681681 AND `gold` = 6906 AND `luong` = 0 AND `luongLock` = 4 AND `itemgift` = '172,172,172,172,172,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '733.026 EXP, 7.556 xu, 4 lượng khoá, Ruy băng kim cương') WHERE `id` = 1216 AND `mainsub` = 0 AND `exp` = 733026 AND `gold` = 7556 AND `luong` = 0 AND `luongLock` = 4 AND `itemgift` = '587,587,587,587,587,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '713.709 EXP, 7.710 xu, 10 lượng khoá, Huy chương Gym x3') WHERE `id` = 1222 AND `mainsub` = 0 AND `exp` = 713709 AND `gold` = 7710 AND `luong` = 0 AND `luongLock` = 10 AND `itemgift` = '32000' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '713.709 EXP, 7.710 xu, 1 lượng khoá, Vòng cổ chuyên gia') WHERE `id` = 1223 AND `mainsub` = 0 AND `exp` = 713709 AND `gold` = 7710 AND `luong` = 0 AND `luongLock` = 1 AND `itemgift` = '503,503,503,503,503,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '925.711 EXP, 10.057 xu, 10 lượng khoá, Than lửa chuyên gia / Hạt giống thần chuyên gia / Khăn lụa chuyên gia / Nước thần chuyên gia / Nam châm chuyên gia (theo dòng Pokémon), Huy chương Gym x3') WHERE `id` = 1229 AND `mainsub` = 0 AND `exp` = 925711 AND `gold` = 10057 AND `luong` = 0 AND `luongLock` = 10 AND `itemgift` = '179,187,195,203,211,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '892.494 EXP, 9.431 xu, 4 lượng khoá, Than lửa chuyên gia / Hạt giống thần chuyên gia / Khăn lụa chuyên gia / Nước thần chuyên gia / Nam châm chuyên gia (theo dòng Pokémon)') WHERE `id` = 1235 AND `mainsub` = 0 AND `exp` = 892494 AND `gold` = 9431 AND `luong` = 0 AND `luongLock` = 4 AND `itemgift` = '179,187,195,203,211,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '1.084.916 EXP, 11.987 xu, 10 lượng khoá, Than lửa chuyên gia / Hạt giống thần chuyên gia / Khăn lụa chuyên gia / Nước thần chuyên gia / Nam châm chuyên gia (theo dòng Pokémon), Huy chương Gym x3') WHERE `id` = 1241 AND `mainsub` = 0 AND `exp` = 1084916 AND `gold` = 11987 AND `luong` = 0 AND `luongLock` = 10 AND `itemgift` = '179,187,195,203,211,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '1.075.665 EXP, 12.011 xu, 10 lượng khoá, Huy chương Gym x3') WHERE `id` = 1245 AND `mainsub` = 0 AND `exp` = 1075665 AND `gold` = 12011 AND `luong` = 0 AND `luongLock` = 10 AND `itemgift` = '32000' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '1.075.665 EXP, 12.011 xu, 1 lượng khoá, Than lửa chuyên gia / Hạt giống thần chuyên gia / Khăn lụa chuyên gia / Nước thần chuyên gia / Nam châm chuyên gia (theo dòng Pokémon)') WHERE `id` = 1247 AND `mainsub` = 0 AND `exp` = 1075665 AND `gold` = 12011 AND `luong` = 0 AND `luongLock` = 1 AND `itemgift` = '179,187,195,203,211,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '1.084.995 EXP, 11.745 xu, 4 lượng khoá, Than lửa chuyên gia / Hạt giống thần chuyên gia / Khăn lụa chuyên gia / Nước thần chuyên gia / Nam châm chuyên gia (theo dòng Pokémon)') WHERE `id` = 1253 AND `mainsub` = 0 AND `exp` = 1084995 AND `gold` = 11745 AND `luong` = 0 AND `luongLock` = 4 AND `itemgift` = '179,187,195,203,211,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '1.187.910 EXP, 12.823 xu, 4 lượng khoá, Băng đô tinh anh') WHERE `id` = 1259 AND `mainsub` = 0 AND `exp` = 1187910 AND `gold` = 12823 AND `luong` = 0 AND `luongLock` = 4 AND `itemgift` = '487,487,487,487,487,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '1.308.331 EXP, 13.992 xu, 4 lượng khoá, Đai tinh anh') WHERE `id` = 1265 AND `mainsub` = 0 AND `exp` = 1308331 AND `gold` = 13992 AND `luong` = 0 AND `luongLock` = 4 AND `itemgift` = '479,479,479,479,479,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '1.620.830 EXP, 17.789 xu, 10 lượng khoá, Găng đấm tinh anh, Huy chương Gym x3') WHERE `id` = 1271 AND `mainsub` = 0 AND `exp` = 1620830 AND `gold` = 17789 AND `luong` = 0 AND `luongLock` = 10 AND `itemgift` = '497,497,497,497,497,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '1.514.713 EXP, 15.973 xu, 4 lượng khoá, Huy hiệu tinh anh') WHERE `id` = 1277 AND `mainsub` = 0 AND `exp` = 1514713 AND `gold` = 15973 AND `luong` = 0 AND `luongLock` = 4 AND `itemgift` = '501,501,501,501,501,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '1.674.201 EXP, 17.482 xu, 4 lượng khoá, Khăn quàng tinh anh') WHERE `id` = 1283 AND `mainsub` = 0 AND `exp` = 1674201 AND `gold` = 17482 AND `luong` = 0 AND `luongLock` = 4 AND `itemgift` = '471,471,471,471,471,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '1.905.538 EXP, 19.890 xu, 4 lượng khoá, Lông vũ tốc độ huyền thoại') WHERE `id` = 1289 AND `mainsub` = 0 AND `exp` = 1905538 AND `gold` = 19890 AND `luong` = 0 AND `luongLock` = 4 AND `itemgift` = '660,660,660,660,660,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '1.426.341 EXP, 15.820 xu, 2 lượng khoá') WHERE `id` = 1292 AND `mainsub` = 0 AND `exp` = 1426341 AND `gold` = 15820 AND `luong` = 0 AND `luongLock` = 2 AND `itemgift` = '32000' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '2.282.146 EXP, 25.313 xu, 10 lượng khoá, Phiến đá hệ huyền thoại, Huy chương Gym x3') WHERE `id` = 1295 AND `mainsub` = 0 AND `exp` = 2282146 AND `gold` = 25313 AND `luong` = 0 AND `luongLock` = 10 AND `itemgift` = '668,668,668,668,668,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '2.097.105 EXP, 22.665 xu, 4 lượng khoá, Ruy băng huyền thoại') WHERE `id` = 1301 AND `mainsub` = 0 AND `exp` = 2097105 AND `gold` = 22665 AND `luong` = 0 AND `luongLock` = 4 AND `itemgift` = '593,593,593,593,593,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '1.930.378 EXP, 21.362 xu, 3 lượng khoá, Vòng cổ huyền thoại') WHERE `id` = 1308 AND `mainsub` = 0 AND `exp` = 1930378 AND `gold` = 21362 AND `luong` = 0 AND `luongLock` = 3 AND `itemgift` = '656,656,656,656,656,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '2.038.443 EXP, 23.360 xu, 3 lượng khoá, Than lửa huyền thoại / Hạt giống thần huyền thoại / Khăn lụa huyền thoại / Nước thần huyền thoại / Nam châm huyền thoại (theo dòng Pokémon)') WHERE `id` = 1315 AND `mainsub` = 0 AND `exp` = 2038443 AND `gold` = 23360 AND `luong` = 0 AND `luongLock` = 3 AND `itemgift` = '599,601,603,605,607,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '1.239.300 EXP, 14.707 xu, 1 lượng khoá') WHERE `id` = 1321 AND `mainsub` = 0 AND `exp` = 1239300 AND `gold` = 14707 AND `luong` = 0 AND `luongLock` = 1 AND `itemgift` = '32000' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '1.982.880 EXP, 23.532 xu, 3 lượng khoá, Than lửa thần thoại / Hạt giống thần thần thoại / Khăn lụa thần thoại / Nước thần thần thoại / Nam châm thần thoại (theo dòng Pokémon)') WHERE `id` = 1322 AND `mainsub` = 0 AND `exp` = 1982880 AND `gold` = 23532 AND `luong` = 0 AND `luongLock` = 3 AND `itemgift` = '1008,1009,1010,1011,1012,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '20.676 EXP, 1.846 xu, 2 lượng khoá, danh hiệu Người bạn của Luvdisc, Bụi sao x1') WHERE `id` = 2009 AND `mainsub` = 1 AND `exp` = 20676 AND `gold` = 1846 AND `luong` = 0 AND `luongLock` = 2 AND `itemgift` = '32000' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '183.211 EXP, 2.252 xu, 2 lượng khoá, Than lửa vàng / Hạt giống thần vàng / Khăn lụa vàng / Nước thần vàng / Nam châm vàng (theo dòng Pokémon), danh hiệu Người hùng Lavender') WHERE `id` = 2107 AND `mainsub` = 1 AND `exp` = 183211 AND `gold` = 2252 AND `luong` = 0 AND `luongLock` = 2 AND `itemgift` = '85,92,99,106,113,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '224.401 EXP, 2.559 xu, 2 lượng khoá, Than lửa vàng / Hạt giống thần vàng / Khăn lụa vàng / Nước thần vàng / Nam châm vàng (theo dòng Pokémon), danh hiệu Người hùng Celadon') WHERE `id` = 2120 AND `mainsub` = 1 AND `exp` = 224401 AND `gold` = 2559 AND `luong` = 0 AND `luongLock` = 2 AND `itemgift` = '85,92,99,106,113,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '240.326 EXP, 2.676 xu, 2 lượng khoá, danh hiệu Người bạn của Loudred, Mảnh sao x1') WHERE `id` = 2124 AND `mainsub` = 1 AND `exp` = 240326 AND `gold` = 2676 AND `luong` = 0 AND `luongLock` = 2 AND `itemgift` = '32000' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '284.188 EXP, 3.055 xu, 2 lượng khoá, Than lửa bạch kim / Hạt giống thần bạch kim / Khăn lụa bạch kim / Nước thần bạch kim / Nam châm bạch kim (theo dòng Pokémon), danh hiệu Người hùng Saffron') WHERE `id` = 2133 AND `mainsub` = 1 AND `exp` = 284188 AND `gold` = 3055 AND `luong` = 0 AND `luongLock` = 2 AND `itemgift` = '175,183,191,199,207,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '293.020 EXP, 3.199 xu, 2 lượng khoá, Than lửa bạch kim / Hạt giống thần bạch kim / Khăn lụa bạch kim / Nước thần bạch kim / Nam châm bạch kim (theo dòng Pokémon), danh hiệu Người hùng Vùng Safari') WHERE `id` = 2145 AND `mainsub` = 1 AND `exp` = 293020 AND `gold` = 3199 AND `luong` = 0 AND `luongLock` = 2 AND `itemgift` = '175,183,191,199,207,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '303.038 EXP, 3.311 xu, 2 lượng khoá, Than lửa bạch kim / Hạt giống thần bạch kim / Khăn lụa bạch kim / Nước thần bạch kim / Nam châm bạch kim (theo dòng Pokémon), danh hiệu Người hùng Fuchsia') WHERE `id` = 2157 AND `mainsub` = 1 AND `exp` = 303038 AND `gold` = 3311 AND `luong` = 0 AND `luongLock` = 2 AND `itemgift` = '175,183,191,199,207,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '475.904 EXP, 5.943 xu, 2 lượng khoá, danh hiệu Người bạn của Skitty, Mảnh sao x1') WHERE `id` = 2190 AND `mainsub` = 1 AND `exp` = 475904 AND `gold` = 5943 AND `luong` = 0 AND `luongLock` = 2 AND `itemgift` = '32000' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '699.344 EXP, 9.212 xu, 2 lượng khoá, Vòng cổ chuyên gia, danh hiệu Người hùng Ecruteak') WHERE `id` = 2268 AND `mainsub` = 1 AND `exp` = 699344 AND `gold` = 9212 AND `luong` = 0 AND `luongLock` = 2 AND `itemgift` = '503,503,503,503,503,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '758.385 EXP, 10.095 xu, 2 lượng khoá, Than lửa chuyên gia / Hạt giống thần chuyên gia / Khăn lụa chuyên gia / Nước thần chuyên gia / Nam châm chuyên gia (theo dòng Pokémon), danh hiệu Người hùng Rừng Cổ') WHERE `id` = 2280 AND `mainsub` = 1 AND `exp` = 758385 AND `gold` = 10095 AND `luong` = 0 AND `luongLock` = 2 AND `itemgift` = '179,187,195,203,211,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '786.819 EXP, 10.529 xu, 2 lượng khoá, Than lửa chuyên gia / Hạt giống thần chuyên gia / Khăn lụa chuyên gia / Nước thần chuyên gia / Nam châm chuyên gia (theo dòng Pokémon), danh hiệu Người hùng Olivine') WHERE `id` = 2292 AND `mainsub` = 1 AND `exp` = 786819 AND `gold` = 10529 AND `luong` = 0 AND `luongLock` = 2 AND `itemgift` = '179,187,195,203,211,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '861.451 EXP, 11.497 xu, 2 lượng khoá, danh hiệu Người bạn của Exploud, Mảnh thiên thạch x1') WHERE `id` = 2296 AND `mainsub` = 1 AND `exp` = 861451 AND `gold` = 11497 AND `luong` = 0 AND `luongLock` = 2 AND `itemgift` = '32000' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '861.451 EXP, 11.497 xu, 2 lượng khoá, Than lửa tinh anh / Hạt giống thần tinh anh / Khăn lụa tinh anh / Nước thần tinh anh / Nam châm tinh anh (theo dòng Pokémon), danh hiệu Người hùng Cianwood') WHERE `id` = 2304 AND `mainsub` = 1 AND `exp` = 861451 AND `gold` = 11497 AND `luong` = 0 AND `luongLock` = 2 AND `itemgift` = '181,189,197,205,213,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '948.779 EXP, 12.544 xu, 2 lượng khoá, Than lửa tinh anh / Hạt giống thần tinh anh / Khăn lụa tinh anh / Nước thần tinh anh / Nam châm tinh anh (theo dòng Pokémon), danh hiệu Người hùng Mahogany') WHERE `id` = 2316 AND `mainsub` = 1 AND `exp` = 948779 AND `gold` = 12544 AND `luong` = 0 AND `luongLock` = 2 AND `itemgift` = '181,189,197,205,213,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '1.044.798 EXP, 13.670 xu, 2 lượng khoá, Than lửa tinh anh / Hạt giống thần tinh anh / Khăn lụa tinh anh / Nước thần tinh anh / Nam châm tinh anh (theo dòng Pokémon), danh hiệu Người hùng Hồ Phẫn Nộ') WHERE `id` = 2329 AND `mainsub` = 1 AND `exp` = 1044798 AND `gold` = 13670 AND `luong` = 0 AND `luongLock` = 2 AND `itemgift` = '181,189,197,205,213,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '1.618.608 EXP, 22.145 xu, 2 lượng khoá, danh hiệu Người bạn của Luvdisc, Mảnh thiên thạch x1') WHERE `id` = 2362 AND `mainsub` = 1 AND `exp` = 1618608 AND `gold` = 22145 AND `luong` = 0 AND `luongLock` = 2 AND `itemgift` = '32000' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '2.008.279 EXP, 32.690 xu, 2 lượng khoá, Khăn quàng thần thoại, danh hiệu Người hùng Cao nguyên Indigo') WHERE `id` = 2392 AND `mainsub` = 1 AND `exp` = 2008279 AND `gold` = 32690 AND `luong` = 0 AND `luongLock` = 2 AND `itemgift` = '1004,1004,1004,1004,1004,0' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '2.056.621 EXP, 34.194 xu, 2 lượng khoá, danh hiệu Người bạn của Hoothoot, Mảnh thiên thạch x1') WHERE `id` = 2396 AND `mainsub` = 1 AND `exp` = 2056621 AND `gold` = 34194 AND `luong` = 0 AND `luongLock` = 2 AND `itemgift` = '32000' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
UPDATE `data_quest_moi` SET `infogift` = CONCAT(SUBSTRING_INDEX(`infogift`, 'Phần thưởng:|', 1), 'Phần thưởng:|', '2.251.692 EXP, 40.674 xu, 2 lượng khoá, danh hiệu Người bạn của Jigglypuff, Mảnh thiên thạch x1') WHERE `id` = 2409 AND `mainsub` = 1 AND `exp` = 2251692 AND `gold` = 40674 AND `luong` = 0 AND `luongLock` = 2 AND `itemgift` = '32000' AND `potiongift` = '32000' AND `npotion` = '32000' AND `infogift` LIKE '%Phần thưởng:|%';
-- ==== SINH TU DONG - HET ====
