-- ============================================================================
--  BUFF NHÂN VẬT: ddsa
--  Thần Binh 6x/7x/8x + đủ loại linh thú (rồng băng, hoả kỳ lân, sư tử,
--  phượng hoàng...) + trứng pet + 1 tỷ lượng + 50 tỷ xu + lv 90
--  DB: kpah_db   (db.name / db.nameNap trong server.ini)
--
--  !!! BACKUP DB TRƯỚC KHI CHẠY !!!
--     mysqldump -u root kpah_db > backup_truoc_buff.sql
-- ============================================================================


-- ############################################################################
--  PHẦN A — MÃ GÕ TAY TRONG GAME (bảng `giftcode`, ô "Mã quà tặng")
--  Đây là luồng ĐẦY ĐỦ NHẤT: có RONGBANG, HKL, SUTU, VKTBLV... mà luồng
--  tự động (phần B) không hỗ trợ.
--  Mỗi mã mỗi nhân vật gõ được 1 lần (chặn bởi bảng giftcode_log).
-- ############################################################################

INSERT INTO `giftcode`
  (`giftcode`, `xu`, `luong`, `luongLock`, `item`, `expire`, `limit_use`, `type`)
VALUES

-- ---- THẦN BINH ----
-- VKTBLV:<level>:<tinhLuyen>:<số ngày>   level chỉ nhận 60 / 70 / 80
-- Luôn max chỉ số, ra đúng vũ khí theo class của nhân vật gõ mã.
-- 9999 ngày ~ 27 năm, coi như vĩnh viễn.
  ('tb8x', 0, 0, 0,
   'VKTBLV:80:99:9999,VKTBLV:80:99:9999,VKTBLV:80:99:9999,VKTBLV:80:99:9999,VKTBLV:80:99:9999',
   0, 100, 0),
  ('tb7x', 0, 0, 0,
   'VKTBLV:70:99:9999,VKTBLV:70:99:9999,VKTBLV:70:99:9999,VKTBLV:70:99:9999,VKTBLV:70:99:9999',
   0, 100, 0),
  ('tb6x', 0, 0, 0,
   'VKTBLV:60:99:9999,VKTBLV:60:99:9999,VKTBLV:60:99:9999',
   0, 100, 0),

-- ---- LINH THÚ CƯỠI, TẠO THẲNG (level 4, chỉ số MAX, số 0 = VĨNH VIỄN) ----
-- Đây là con vật hoàn chỉnh, KHÔNG phải trứng, không cần ra NPC nở.
--   RONGBANG:<ngày>   Rồng Băng
--   HKL:<phút>        Hoả Kỳ Lân  (thật ra là thần thú item 681, vào hành trang)
--   SUTU:<ngày>       Sư tử        | SUTUBANG:<ngày>  Sư tử Băng
--   LANT:<phút>       Lân Sư Tử (kháng hoả tuyết 95)
--   DKT:<ngày>        Đương khang  | DKB:<ngày>       Đương khang Băng Giá
--   DBT:<ngày>        Đại Bạch Thử | TL:<ngày>        Tuần lộc
  ('linhthu1', 0, 0, 0,
   'RONGBANG:0,SUTU:0,SUTUBANG:0,LANT:0,DKT:0,DKB:0,DBT:0,TL:0',
   0, 100, 0),
-- ---- HOẢ KỲ LÂN (thần thú, item type 25, vào HÀNH TRANG) ----
-- KHÔNG có "trứng hoả kỳ lân" để nở. Item 883 tuy tên là "Trứng hoả kỳ lân"
-- nhưng type 21 = thẻ bài linh đanh thuê, dùng ra menu chọn Hộ vệ, không
-- liên quan gì hoả kỳ lân.
-- HKL:<phút> chỉ tạo được cấp 1 (681 hardcode trong doCreateHoaKyLanTime).
-- Muốn đủ 3 cấp thì dùng DB: — GiftCode.AddItem có nhánh isItemThanThu()
-- nên option thần thú vẫn được gắn đúng theo atb0 của từng cấp.
--   681 Hoả kỳ lân | 682 cấp 2 | 683 cấp 3      (-1 = vĩnh viễn)
  ('hoakylan', 0, 0, 0,
   'DB:681:1:-1,DB:682:1:-1,DB:683:1:-1,HKL:0',
   0, 100, 0),

-- ---- PHƯỢNG HOÀNG ĐỦ HỆ (số 0 = vĩnh viễn) ----
--   PHT thường | PHBT băng | PHDST đa sắc | PHMT Mộc | PHKT Kim | PHTT Thổ
  ('phuonghoang', 0, 0, 0,
   'PHT:0,PHBT:0,PHDST:0,PHMT:0,PHKT:0,PHTT:0',
   0, 100, 0),

-- ---- TRỨNG LINH THÚ CƯỠI (cộng vào ô trứng, ra NPC nở) ----
--   PT:<slot>:<số lượng>
--   64 Hắc ngưu | 65 Mãnh hổ | 66 Sói xám | 67 Tiên hạc | 68 Bạch mã
--   86 Phượng hoàng lửa | 70 Phượng hoàng băng | 74 Bạch cốt
--   72 Đương khang | 115 Lân sư tử
  ('trunglt', 0, 0, 0,
   'PT:64:20,PT:65:20,PT:66:20,PT:67:20,PT:68:20,PT:70:20,PT:72:20,PT:74:20,PT:86:20,PT:115:20',
   0, 100, 0),

-- ---- TRỨNG PET (item, -1 = vĩnh viễn) ----
--   684 Trứng Cú | 685 Trứng Rồng | 686 Trứng Yêu Tinh
--   687 Trứng Dơi | 688 Trứng Đại Bàng
  ('trungpet', 0, 0, 0,
   'DB:684:10:-1,DB:685:10:-1,DB:686:10:-1,DB:687:10:-1,DB:688:10:-1',
   0, 100, 0),

-- ---- THẺ BÀI LINH ĐANH THUÊ (item type 21, ID 883-887) ----
-- CẢNH BÁO TÊN GÂY HIỂU NHẦM: trong data_item chúng tên là "Trứng hoả kỳ
-- lân / đương khang / lân / bạch cốt / rồng băng", nhưng KHÔNG PHẢI TRỨNG
-- và KHÔNG nở ra con nào cả. Cả 5 cái đều chạy chung Map.doUseItemLinhDanhThue()
-- -> bung menu "Hộ vệ nam/nữ hệ kim, hoả, thuỷ, thổ, mộc" để triệu hồi hộ vệ.
-- Độ bền 480, yêu cầu level 65. Cho kèm để bạn tự thử, không liên quan linh thú.
  ('thebailt', 0, 0, 0,
   'DB:883:5:-1,DB:884:5:-1,DB:885:5:-1,DB:886:5:-1,DB:887:5:-1',
   0, 100, 0),

-- ---- 1 TỶ LƯỢNG ----
-- Cột luong là int(11) nên tối đa 2.147.483.647 mỗi mã.
-- CẢNH BÁO: tổng lượng trên nhân vật phải < 2.147.483.647, vượt sẽ tràn số âm
-- và bị banChar("amLuong") tự động.
  ('luong1ty', 0, 1000000000, 0, NULL, 0, 100, 0),

-- ---- 1 TỶ LƯỢNG KHÓA ----
-- Đi qua cột luongLock -> Char.addLuongLock().
-- BẮT BUỘC phải làm bằng mã này, KHÔNG sửa được bằng SQL trực tiếp:
-- cột tob_char.luonglock chỉ được GHI lúc save, lúc load thì lượng khóa
-- đọc từ trường thứ 70 của chuỗi pInfo (Char.java:11693) nên sửa cột đó
-- không có tác dụng gì.
-- CẢNH BÁO tràn số y hệt lượng thường: tổng phải < 2.147.483.647,
-- âm là dính banChar("amluongLock").
  ('lkhoa1ty', 0, 0, 1000000000, NULL, 0, 100, 0);


-- ############################################################################
--  PHẦN B — TỰ ĐỘNG NHẬN KHI CHỌN NHÂN VẬT (bảng `ngude_giftcode`)
--  Mỗi lần vào char chỉ ăn ĐÚNG 1 DÒNG status = 0, nhận xong tự set status = 1.
--  Có 3 dòng => phải thoát ra chọn lại nhân vật 3 lần.
--  Luồng này KHÔNG hỗ trợ: RONGBANG, HKL, SUTU, SUTUBANG, DKB, VKTBLV, VKID,
--  NLT, TKMAX, ITEM, TRANGPHUC — mấy cái đó nằm hết ở phần A.
-- ############################################################################

INSERT INTO `ngude_giftcode`
  (`code`, `type`, `xu`, `luong`, `luongLock`, `item`, `charname`, `status`, `level`, `userId`)
VALUES
-- Thần binh 6x max + đủ trứng linh thú cưỡi
  ('buffddsa1', 'admin', 0, 0, 0,
   'VKTBMAX:0:1:0,PT:64:20,PT:65:20,PT:66:20,PT:67:20,PT:68:20,PT:70:20,PT:72:20,PT:74:20,PT:86:20,PT:115:20',
   'ddsa', 0, 1, 0),
-- Trứng pet các loại
  ('buffddsa2', 'admin', 0, 0, 0,
   'DB:684:10:-1,DB:685:10:-1,DB:686:10:-1,DB:687:10:-1,DB:688:10:-1',
   'ddsa', 0, 1, 0),
-- Phượng hoàng đủ hệ + Lân sư tử + Tuần lộc + Đương khang + Đại Bạch Thử
  ('buffddsa3', 'admin', 0, 0, 0,
   'PHT:0,PHBT:0,PHDST:0,PHMT:0,PHKT:0,PHTT:0,LANT:0,TL:0,DKT:0,DBT:0',
   'ddsa', 0, 1, 0);


-- ############################################################################
--  PHẦN C — XU / LƯỢNG SỬA THẲNG DB
--
--  !!! NHÂN VẬT PHẢI OFFLINE (thoát game hẳn) TRƯỚC KHI CHẠY !!!
--  Server ghi đè toàn bộ khi saveChar() lúc logout, chạy lúc đang online
--  là mất sạch.
--
--  gold  = xu, kiểu bigint unsigned  -> 50 tỷ vô tư
--  luong = int(10) unsigned, Java đọc bằng getInt -> PHẢI < 2.147.483.647
--
--  KHÔNG có lượng khóa ở đây: cột tob_char.luonglock chỉ được ghi chứ không
--  được đọc lại (lúc load lấy từ pInfo trường thứ 70). Lượng khóa phải nhận
--  bằng mã 'lkhoa1ty' ở phần A.
-- ############################################################################

UPDATE `tob_char`
   SET `gold`  = 50000000000,
       `luong` = 1000000000
 WHERE `charname` = 'ddsa';


-- ############################################################################
--  PHẦN D — LÊN LEVEL 90
--
--  CÁCH TỐT NHẤT: dùng Admin Panel của server (cửa sổ Swing tự bật khi chạy
--  KPAH2_patched.jar) -> chuột phải dòng nhân vật -> "Buff Exp"
--  -> chọn "Lên Level/Phần trăm" -> Level 90, (%) 0.
--  Cách này đi qua Map.addXPForChar nên được cộng đủ:
--     +5 điểm tiềm năng, +1 mỗi chỉ số, +1 điểm kỹ năng cho MỖI level.
--  Nhân vật phải đang ONLINE.
--
--  CÁCH SQL DƯỚI ĐÂY LÀ PHƯƠNG ÁN DỰ PHÒNG (nhân vật offline).
--  Hạn chế: chỉ set exp/level, KHÔNG cộng điểm tiềm năng và điểm kỹ năng,
--  vì lúc load Char.java sẽ kéo lastLV lên bằng level luôn nên không kích
--  hoạt được nhánh cộng điểm. Muốn có điểm thì vẫn phải qua Admin Panel.
--
--  Mốc exp (từ LevelDetail.expMain):
--     lv 88 = 22.312.130.008.720
--     lv 89 = 33.468.195.013.080
--     lv 90 = 50.202.292.519.620   <-- dùng mốc này
--     lv 91 = 75.303.438.779.430   (đừng vượt quá, 91 là trần)
--
--  Level thật đọc từ chuỗi pInfo (trường thứ 5 = exp, thứ 6 = lastLV),
--  KHÔNG phải từ cột xp, nên phải vá cả pInfo.
-- ############################################################################

-- UPDATE `tob_char`
--    SET `pInfo` = CONCAT(
--          SUBSTRING_INDEX(`pInfo`, ',', 4), ',',
--          '50202292519620', ',',
--          '90', ',',
--          SUBSTRING_INDEX(`pInfo`, ',',
--            -(LENGTH(`pInfo`) - LENGTH(REPLACE(`pInfo`, ',', '')) + 1 - 6))
--        ),
--        `xp`     = 50202292519620,
--        `lastLv` = 90
--  WHERE `charname` = 'ddsa';

-- Kiểm tra lại sau khi chạy (trường 5 phải là exp, trường 6 phải là 90):
-- SELECT SUBSTRING_INDEX(SUBSTRING_INDEX(`pInfo`, ',', 5), ',', -1) AS exp_moi,
--        SUBSTRING_INDEX(SUBSTRING_INDEX(`pInfo`, ',', 6), ',', -1) AS lastLV_moi,
--        `xp`, `lastLv`, `gold`, `luong`
--   FROM `tob_char` WHERE `charname` = 'ddsa';


-- ############################################################################
--  PHẦN A2 — MÃ NGUYÊN LIỆU / BỘT / THUỐC HOÁ HÌNH
--  ĐÃ CHẠY VÀO DB kpah_db rồi. Giữ lại đây để cài lại khi cần.
--
--  Tất cả dùng prefix GEM:<id>:<số lượng>:<khoá>
--    tham số thứ 4: -1 hoặc bỏ trống = đồ KHOÁ, còn 1 = đồ THƯỜNG (bán/trao được)
--    (Database.java: lockitemgem = parseInt(tachTime[3]) < 0)
--
--  ID lấy từ Map.loadGemTemplate() và GemTemplate.ID_MATERIAL_LOW / _HIGHT:
--    Nguyên liệu SƠ CẤP (30 id): Vải 68-73 | Sắt 75-80 | Ngọc 82-87
--                                Gỗ thường 89-94 | Da mềm 96-101
--    Nguyên liệu CAO CẤP (30 id): Tơ lụa 103-108 | Bạc 110-115
--                                 Thuỷ tinh 117-122 | Gỗ sưa 124-129 | Da cứng 131-136
--    Bột: 246 trắng | 247 xanh | 248 đỏ | 249 xanh lá | 250 Đá tiến giai
--    268 Thuốc hoá hình (Animal.doHoaHinh cần 30 lọ cho mỗi lần hoá hình)
--
--  Phải tách làm nhiều mã vì cột giftcode.item chỉ là varchar(255),
--  30 nguyên liệu một chuỗi sẽ dài ~390 ký tự và bị cắt cụt.
-- ############################################################################

DELETE FROM `giftcode` WHERE `giftcode` IN
  ('botxanh','botall','nlsocap1','nlsocap2','nlcaocap1','nlcaocap2','hoahinhthu');

INSERT INTO `giftcode` (`giftcode`,`xu`,`luong`,`luongLock`,`item`,`expire`,`limit_use`,`type`) VALUES

-- Bột xanh, 500 viên
('botxanh', 0,0,0, 'GEM:247:500:1', 0, 100, 0),

-- Trọn bộ 4 loại bột + Đá tiến giai, 500 mỗi loại
('botall', 0,0,0,
 'GEM:246:500:1,GEM:247:500:1,GEM:248:500:1,GEM:249:500:1,GEM:250:500:1',
 0, 100, 0),

-- Nguyên liệu sơ cấp phần 1: Vải 1-6, Sắt 1-6, Ngọc 1-3
('nlsocap1', 0,0,0,
 'GEM:68:200:1,GEM:69:200:1,GEM:70:200:1,GEM:71:200:1,GEM:72:200:1,GEM:73:200:1,GEM:75:200:1,GEM:76:200:1,GEM:77:200:1,GEM:78:200:1,GEM:79:200:1,GEM:80:200:1,GEM:82:200:1,GEM:83:200:1,GEM:84:200:1',
 0, 100, 0),

-- Nguyên liệu sơ cấp phần 2: Ngọc 4-6, Gỗ thường 1-6, Da mềm 1-6
('nlsocap2', 0,0,0,
 'GEM:85:200:1,GEM:86:200:1,GEM:87:200:1,GEM:89:200:1,GEM:90:200:1,GEM:91:200:1,GEM:92:200:1,GEM:93:200:1,GEM:94:200:1,GEM:96:200:1,GEM:97:200:1,GEM:98:200:1,GEM:99:200:1,GEM:100:200:1,GEM:101:200:1',
 0, 100, 0),

-- Nguyên liệu cao cấp phần 1: Tơ lụa 1-6, Bạc 1-6, Thuỷ tinh 1-3
('nlcaocap1', 0,0,0,
 'GEM:103:200:1,GEM:104:200:1,GEM:105:200:1,GEM:106:200:1,GEM:107:200:1,GEM:108:200:1,GEM:110:200:1,GEM:111:200:1,GEM:112:200:1,GEM:113:200:1,GEM:114:200:1,GEM:115:200:1,GEM:117:200:1,GEM:118:200:1,GEM:119:200:1',
 0, 100, 0),

-- Nguyên liệu cao cấp phần 2: Thuỷ tinh 4-6, Gỗ sưa 1-6, Da cứng 1-6
('nlcaocap2', 0,0,0,
 'GEM:120:200:1,GEM:121:200:1,GEM:122:200:1,GEM:124:200:1,GEM:125:200:1,GEM:126:200:1,GEM:127:200:1,GEM:128:200:1,GEM:129:200:1,GEM:131:200:1,GEM:132:200:1,GEM:133:200:1,GEM:134:200:1,GEM:135:200:1,GEM:136:200:1',
 0, 100, 0),

-- Thuốc hoá hình thú cưỡi, 300 lọ = 10 lần hoá hình
('hoahinhthu', 0,0,0, 'GEM:268:300:1', 0, 100, 0);

-- ---- NHÂN SÂM / TIÊN DƯỢC (potion, prefix PT:<slot>:<số lượng>) ------------
-- Đây là potion chứ không phải gem, nên dùng PT chứ không phải GEM.
--   potion 9  Nhân sâm                        -> mỗi lọ cộng 1% exp của level hiện tại
--   potion 10 Tiên dược thường     100k exp
--   potion 11 Tiên dược cao cấp    500k exp
--   potion 12 Tiên dược đặc biệt   1 tỷ exp
-- LƯU Ý: nhân sâm CHỈ dùng được khi level dưới 90 (doUsePotion.java case 9).
-- Tiên dược 10/11/12 không bị chặn level.
DELETE FROM `giftcode` WHERE `giftcode` IN ('nhansam','tienduoc');
INSERT INTO `giftcode` (`giftcode`,`xu`,`luong`,`luongLock`,`item`,`expire`,`limit_use`,`type`) VALUES
('nhansam',  0,0,0, 'PT:9:9999', 0, 100, 0),
('tienduoc', 0,0,0, 'PT:10:999,PT:11:999,PT:12:999', 0, 100, 0);


-- ---- 50 TỶ XU: PHẢI CHIA 25 MÃ -------------------------------------------
-- Cột giftcode.xu là int(11) nên trần 2.147.483.647 mỗi mã, VÀ code đọc bằng
-- rs.getInt("xu") nên nới cột không cũng vô ích.
-- 25 mã x 2 tỷ = đúng 50 tỷ. Gõ trong game: xu01, xu02, ... xu25
DELETE FROM `giftcode` WHERE `giftcode` REGEXP '^xu[0-9][0-9]$';
INSERT INTO `giftcode` (`giftcode`,`xu`,`luong`,`luongLock`,`item`,`expire`,`limit_use`,`type`) VALUES
('xu01',2000000000,0,0,NULL,0,100,0),('xu02',2000000000,0,0,NULL,0,100,0),
('xu03',2000000000,0,0,NULL,0,100,0),('xu04',2000000000,0,0,NULL,0,100,0),
('xu05',2000000000,0,0,NULL,0,100,0),('xu06',2000000000,0,0,NULL,0,100,0),
('xu07',2000000000,0,0,NULL,0,100,0),('xu08',2000000000,0,0,NULL,0,100,0),
('xu09',2000000000,0,0,NULL,0,100,0),('xu10',2000000000,0,0,NULL,0,100,0),
('xu11',2000000000,0,0,NULL,0,100,0),('xu12',2000000000,0,0,NULL,0,100,0),
('xu13',2000000000,0,0,NULL,0,100,0),('xu14',2000000000,0,0,NULL,0,100,0),
('xu15',2000000000,0,0,NULL,0,100,0),('xu16',2000000000,0,0,NULL,0,100,0),
('xu17',2000000000,0,0,NULL,0,100,0),('xu18',2000000000,0,0,NULL,0,100,0),
('xu19',2000000000,0,0,NULL,0,100,0),('xu20',2000000000,0,0,NULL,0,100,0),
('xu21',2000000000,0,0,NULL,0,100,0),('xu22',2000000000,0,0,NULL,0,100,0),
('xu23',2000000000,0,0,NULL,0,100,0),('xu24',2000000000,0,0,NULL,0,100,0),
('xu25',2000000000,0,0,NULL,0,100,0);

-- CÁCH NHANH HƠN: sửa thẳng DB, nhân vật phải OFFLINE (cột gold là bigint).
-- UPDATE `tob_char` SET `gold` = 50000000000 WHERE `charname` = 'TEN_NHAN_VAT';


-- ---- MẢNH TRANH (TỨ BÌNH) 4x / 5x / 6x / 7x ------------------------------
-- Là gem, dùng prefix GEM. ID lấy từ GemTemplate.ID_TU_BINH:
--   4x Tứ Bình  159-174  (Mai, Lan, Cúc, Trúc)   — cần  5 mảnh/tranh
--   5x Tứ Nhạc  179-194  (Sáo, Quạt, Sênh, Đàn)  — cần 10 mảnh/tranh
--   6x Tứ Khoái 195-210  (Ngư, Tiều, Canh, Độc)  — cần 15 mảnh/tranh
--   7x Tứ Linh  211-226  (Long, Lân, Quy, Phụng) — cần 20 mảnh/tranh
-- Số mảnh cần lấy từ cột nmanh trong bảng tob_tubinh.
-- Mỗi mã phát gấp 4 lần số cần, đủ ghép thoải mái. Mỗi mã 16 id, dài 207 ký tự.
DELETE FROM `giftcode` WHERE `giftcode` IN ('tranh4x','tranh5x','tranh6x','tranh7x','sachpet');
INSERT INTO `giftcode` (`giftcode`,`xu`,`luong`,`luongLock`,`item`,`expire`,`limit_use`,`type`) VALUES
('tranh4x', 0,0,0,'GEM:159:20:1,GEM:160:20:1,GEM:161:20:1,GEM:162:20:1,GEM:163:20:1,GEM:164:20:1,GEM:165:20:1,GEM:166:20:1,GEM:167:20:1,GEM:168:20:1,GEM:169:20:1,GEM:170:20:1,GEM:171:20:1,GEM:172:20:1,GEM:173:20:1,GEM:174:20:1',0,100,0),
('tranh5x', 0,0,0,'GEM:179:40:1,GEM:180:40:1,GEM:181:40:1,GEM:182:40:1,GEM:183:40:1,GEM:184:40:1,GEM:185:40:1,GEM:186:40:1,GEM:187:40:1,GEM:188:40:1,GEM:189:40:1,GEM:190:40:1,GEM:191:40:1,GEM:192:40:1,GEM:193:40:1,GEM:194:40:1',0,100,0),
('tranh6x', 0,0,0,'GEM:195:60:1,GEM:196:60:1,GEM:197:60:1,GEM:198:60:1,GEM:199:60:1,GEM:200:60:1,GEM:201:60:1,GEM:202:60:1,GEM:203:60:1,GEM:204:60:1,GEM:205:60:1,GEM:206:60:1,GEM:207:60:1,GEM:208:60:1,GEM:209:60:1,GEM:210:60:1',0,100,0),
('tranh7x', 0,0,0,'GEM:211:80:1,GEM:212:80:1,GEM:213:80:1,GEM:214:80:1,GEM:215:80:1,GEM:216:80:1,GEM:217:80:1,GEM:218:80:1,GEM:219:80:1,GEM:220:80:1,GEM:221:80:1,GEM:222:80:1,GEM:223:80:1,GEM:224:80:1,GEM:225:80:1,GEM:226:80:1',0,100,0),
-- Sách kỹ năng pet: item 722, dùng DB (-1 = vĩnh viễn)
('sachpet', 0,0,0,'DB:722:50:-1',0,100,0);

-- Kiểm tra không bị cắt cụt (do_dai phải khớp: 13 / 69 / 194 / 196 / 209 / 209 / 13):
-- SELECT giftcode, CHAR_LENGTH(item) AS do_dai FROM giftcode
--  WHERE giftcode IN ('botxanh','botall','nlsocap1','nlsocap2','nlcaocap1','nlcaocap2','hoahinhthu');


-- ############################################################################
--  PHẦN E — CHẨN ĐOÁN KHI GÕ MÃ MÀ KHÔNG NHẬN ĐƯỢC GÌ
--  Chạy lần lượt 4 câu dưới, dừng ở câu nào ra kết quả sai là biết lỗi ở đâu.
-- ############################################################################

-- E1. Mã đã có trong DB chưa? Phải ra 10 dòng.
--     Ra 0 dòng = chưa import file này, hoặc import nhầm database.
SELECT `giftcode`, `luong`, `luongLock`, `limit_use`, `type`, `item`
  FROM `giftcode`
 WHERE `giftcode` IN ('tb8x','tb7x','tb6x','linhthu1','hoakylan',
                      'phuonghoang','trunglt','trungpet','thebailt',
                      'luong1ty','lkhoa1ty');

-- E2. *** NGHI PHẠM SỐ 1 *** Tài khoản đã kích hoạt chưa?
--     server.ini đang để sv.EnableAvtiveCheck=1, nên newDoGetGiftCode()
--     gọi check_active_putcode(userID) NGAY TỪ ĐẦU. Không có dòng trong
--     5h_active là mọi mã đều bị chặn, chỉ hiện
--     "Vui lòng kích hoạt tài khoản để sử dụng.<userId>" rồi return.
--     Ra 0 dòng = đây chính là lỗi.
SELECT c.`charname`, c.`userId`, a.`username`, a.`time_end`
  FROM `tob_char` c
  LEFT JOIN `5h_active` a ON a.`userID` = c.`userId`
 WHERE c.`charname` = 'ddsa';

--     CÁCH SỬA (chọn 1 trong 2):
--     (a) Kích hoạt riêng tài khoản này — không cần restart server:
-- INSERT INTO `5h_active` (`userID`, `username`, `time_end`)
-- SELECT `userId`, `charname`, '-1' FROM `tob_char` WHERE `charname` = 'ddsa';
--     (b) Tắt hẳn kiểm tra kích hoạt: sửa server.ini
--         sv.EnableAvtiveCheck=0   rồi RESTART server.

-- E3. Mã đã bị đánh dấu là đã dùng chưa?
--     Nếu có tên mã ở đây mà không nhận được đồ thì mã đã bị "cháy".
SELECT `giftcode`, `player`, `xu`, `luong`, `luongK`
  FROM `giftcode_log` WHERE `player` = 'ddsa';
--     Xoá để gõ lại:
-- DELETE FROM `giftcode_log` WHERE `player` = 'ddsa';

-- E4. Linh thú đã thực sự vào DB chưa?
--     saveAnimal() ghi ngay lúc nhận, không đợi logout.
--     Có dòng ở đây = server đã cấp xong, chỉ là nhìn nhầm chỗ:
--     LINH THÚ CƯỠI KHÔNG NẰM Ở HÀNH TRANG, nó ở tab linh thú (gói type 2).
SELECT a.`id`, a.`name`, a.`lv`, a.`place`, a.`texpire`
  FROM `tob_animal` a
  JOIN `tob_char`  c ON c.`id` = a.`owner`
 WHERE c.`charname` = 'ddsa' AND a.`del` = 0;


-- ############################################################################
--  TIỆN ÍCH
-- ############################################################################

-- Gõ lại mã ở PHẦN A trên cùng nhân vật:
-- DELETE FROM `giftcode_log` WHERE `player` = 'ddsa';

-- Nhận lại quà PHẦN B:
-- UPDATE `ngude_giftcode` SET `status` = 0 WHERE `charname` = 'ddsa';

-- Kiểm tra tên nhân vật cho đúng (phân biệt hoa/thường):
-- SELECT `charname`, `lastLv`, `gold`, `luong` FROM `tob_char` WHERE `charname` LIKE '%ddsa%';
