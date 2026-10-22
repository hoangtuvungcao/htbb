-- =====================================================================================================================
--  HUONG DAN TAN THU + SUA CHUOI CHINH (nhanh huong-dan, client 0.9.7 tro len)
-- =====================================================================================================================
--
--  Chay lai nhieu lan van an toan (MariaDB 10.3, utf8). Khong can khoi dong lai de doi CHU (game nap lai bang
--  data_huong_dan_tan_thu moi 60 giay); doi buoc nhiem vu (data_quest_moi / data_quest_lau_dai) can khoi dong lai.
--
--  1. data_huong_dan_tan_thu: moi chu cua he huong dan (menu moi khi vao lang, Bo qua + xac nhan, So tay -> cong dung
--     tung NPC). Dong co sua_boi khac rong (sua tay / web) thi giu noi dung da sua.
--  2. data_vat_pham_nhiem_vu 51..58: thu / dau tap cua buoc huong dan (client hien tien do x/y nhu nhiem vu nhat do).
--  3. 10 buoc huong dan = 10 dong DAU chuoi chinh (data_quest_moi 901..910, ma HD.01..HD.10) - VIET LAI TAI CHO, cung id,
--     cung so dong: chi so chuoi chinh cua moi nhan vat cu khong doi. Moi buoc mot NPC trong lang (co o ca ba lang
--     Pewter / Blackthorn / Cianwood), muc tieu ro, loi thoai ngan, thuong nho: xu it, thuoc, mot Ruy bang; KHONG luong /
--     luong khoa. Buoc hanh dong (kieu 2) dem bang su kien that (dung_thuoc / hoc_chieu / nang_cap / tha_cau).
--  4. data_nhiem_vu_kieu: ba kieu muc tieu moi (diem moc o real.DiemMocNhiemVu).
--  5. data_npc.ban_do: Chansey + Snorlax nay dung o ca ba lang (server/cMap 0 / 70 / 80, tools/pokemon/ThemNpcLang.java).
--  6. Chuoi chinh sau huong dan: chuong 1-2 sua tay (loi dau lap lai "Tinh giac", NPC giao khac nguoi noi, muc tieu
--     khong kiem duoc); khoi SUA CHUOI CHINH (sinh boi tools/pokemon/sinh_sua_chuoi_chinh.py) sua "Dia diem" tro ban do
--     khong co quai / NPC va buoc noi chuyen giao nham NPC.
--
--  Kiem: server/test/KiemHuongDanTanThu (doc tep nay), bot tools/pokemon/kiem_tai/KichBanHuongDan.java (hd_*).
-- ---------------------------------------------------------------------------------------------------------------------

-- 1. Chu he huong dan ---------------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `data_huong_dan_tan_thu` (
  `ma` varchar(48) NOT NULL,
  `thu_tu` int(11) NOT NULL DEFAULT 0,
  `tieu_de` varchar(128) NOT NULL DEFAULT '',
  `noi_dung` text NOT NULL,
  `sua_boi` varchar(50) NOT NULL DEFAULT '',
  `sua_luc` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`ma`)
) DEFAULT CHARSET=utf8;

INSERT INTO `data_huong_dan_tan_thu` (`ma`, `thu_tu`, `tieu_de`, `noi_dung`) VALUES
('moi.chao', 1, '', 'Chào mừng tới làng! Đi theo dấu nhiệm vụ trên đầu NPC và chấm trên bản đồ nhỏ.'),
('moi.chao_phu', 2, '', 'Mở Nhiệm vụ, chọn nhiệm vụ rồi bấm Đi tới để tự tìm đường.'),
('moi.lam', 3, 'Làm hướng dẫn tân thủ (khuyên dùng)', 'Làm hướng dẫn tân thủ (khuyên dùng)'),
('moi.bo_qua', 4, 'Bỏ qua hướng dẫn (đã quen chơi)', 'Bỏ qua hướng dẫn (đã quen chơi)'),
('moi.bat_dau', 5, '', 'Hãy gặp Trưởng làng Xatu, NPC có dấu nhiệm vụ trên đầu.'),
('so_tay.bo_qua', 10, 'Bỏ qua hướng dẫn tân thủ', 'Bỏ qua hướng dẫn tân thủ'),
('so_tay.huong_dan', 11, 'Hướng dẫn tân thủ: công dụng các NPC', 'Hướng dẫn tân thủ: công dụng các NPC'),
('bo_qua.hoi', 20, '', 'Bỏ qua hướng dẫn tân thủ? Bạn vẫn nhận đủ thưởng của các bước còn lại: {thuong}. Không làm lại được.'),
('bo_qua.xong', 21, '', 'Đã bỏ qua hướng dẫn. Đã nhận: {thuong}. Gặp Trưởng làng Xatu để bắt đầu cốt truyện.'),
('bo_qua.da_xong', 22, '', 'Bạn đã xong hướng dẫn tân thủ.'),
('bo_qua.tui_day', 23, '', 'Hành trang không đủ chỗ cho phần thưởng. Hãy dọn bớt vài ô rồi thử lại.'),
('npc.01_xatu', 30, 'Xatu · trưởng làng', 'Trưởng làng Xatu: giao nhiệm vụ cốt truyện chính. Có mặt ở cả ba làng Pewter, Blackthorn, Cianwood.'),
('npc.02_chansey', 31, 'Chansey · hồi phục', 'Y tá Chansey đứng trước cửa Trung tâm Pokémon: hồi đầy HP và PP miễn phí, bán thuốc hồi phục.'),
('npc.03_kecleon', 32, 'Kecleon · tạp hoá', 'Kecleon tạp hoá: bán Thuốc hồi máu, Ether và đồ dùng hằng ngày. Chạm vào rồi chọn Mua.'),
('npc.04_blaziken', 33, 'Blaziken · huấn luyện', 'Huấn luyện viên Blaziken: dạy chiêu mới (trả xu). Điểm kỹ năng nhận khi lên cấp: Menu → Pokémon → Kỹ năng → Học.'),
('npc.05_machamp', 34, 'Machamp · thợ rèn', 'Machamp thợ rèn: nâng cấp, khảm ngọc, sửa và chế tạo trang bị.'),
('npc.06_bronzong', 35, 'Bronzong · bán giáp', 'Bronzong thợ giáp: bán trang bị phòng thủ theo hệ và cấp.'),
('npc.07_persian', 36, 'Persian · đá quý', 'Ngài Persian: bán đá quý và nguyên liệu nâng cấp.'),
('npc.08_meowth', 37, 'Meowth · ngân hàng', 'Meowth thương nhân: ngân hàng, gửi và rút xu, lượng; chuyển lượng cho Pokémon khác.'),
('npc.09_kangaskhan', 38, 'Kangaskhan · giữ đồ', 'Kangaskhan thủ kho: giữ bớt đồ khi hành trang đầy.'),
('npc.10_pidgeot', 39, 'Pidgeot · chở bay', 'Pidgeot: chở bay tới các vùng xa, trả bằng xu. Vùng mới mở dần theo cấp.'),
('npc.11_snorlax', 40, 'Snorlax · cốt truyện', 'Ông Snorlax: giao nhiệm vụ cốt truyện chương đầu, ngay trong làng.'),
('npc.12_doi_thuong', 41, 'Đổi thưởng · Sổ tay', 'NPC Đổi thưởng: đổi điểm lấy vật phẩm; mở Pokédex, Sổ tay huấn luyện (nhiệm vụ ngày, tuần, thách đấu Gym) và Hệ sinh thái.'),
('npc.13_tong_quan', 42, 'Tổng quản · quà tặng', 'Tổng quản: nhận quà Giftcode, mở rộng hành trang, chuyển vùng. Mã quà cũng nhập được ở Menu → Thư/Quà.'),
('npc.14_cau_ca', 43, 'Câu cá ở ao làng', 'Đứng sát mép ao trong làng, mở Menu → Hệ sinh thái → Câu cá. Mỗi lần câu tốn thể lực sinh hoạt và ít xu mua mồi.'),
('npc.15_tu_dong', 44, 'Tự động đánh', 'Nút Tự động trên màn hình: Pokémon tự đánh quái gần nhất và tự dùng thuốc theo ngưỡng. Chỉ dùng ở bản đồ có Pokémon hoang dã.'),
('npc.16_pokedex', 45, 'Pokédex', 'Menu → Pokédex: ghi mọi loài đã gặp và đã hạ; xong một vùng có thưởng.'),
('npc.17_thu', 46, 'Thư và quà', 'Menu → Thư/Quà: hộp thư và ô nhập mã quà tặng.')
ON DUPLICATE KEY UPDATE
  `thu_tu` = VALUES(`thu_tu`),
  `tieu_de` = IF(`sua_boi` = '', VALUES(`tieu_de`), `tieu_de`),
  `noi_dung` = IF(`sua_boi` = '', VALUES(`noi_dung`), `noi_dung`);

-- 2. Thu / dau tap cua buoc huong dan ----------------------------------------------------------------------------------
INSERT INTO `data_vat_pham_nhiem_vu` (`id`, `ten`) VALUES
(51, 'Thư giới thiệu của Xatu'),
(52, 'Điểm kỹ năng đã cộng'),
(53, 'Bình thuốc đã dùng'),
(54, 'Đơn thuốc của Chansey'),
(55, 'Ruy băng đã mặc'),
(56, 'Phiếu gửi của Machamp'),
(57, 'Lần thả câu'),
(58, 'Lông vũ Pidgeot')
ON DUPLICATE KEY UPDATE `ten` = VALUES(`ten`);

-- 3. Muoi buoc huong dan (cung id 901..910) ---------------------------------------------------------------------------
--    type: 3 noi chuyen (nghe het loi = xong), 1 mang do (nhan o npcrcv, tra o npcres), 0 ha quai, 2 hanh dong / nhat do.
--    content: dong "1<NPC>: ..." (NPC noi) / "0..." (nguoi choi); buoc khong phai noi chuyen: dong CUOI la cau hoi nhan.
INSERT INTO `data_quest_moi` (`id`, `name`, `npcrcv`, `npcres`, `lv`, `mainsub`, `content`, `scontent`, `rescontent`,
  `idItemget`, `idMonskill`, `type`, `nitemget`, `nmonskill`, `gold`, `exp`, `potiongift`, `npotion`, `itemgift`, `deltaLv`,
  `infogift`, `infofinish`, `pactive`, `luong`, `luongLock`) VALUES
(901, 'Chào trưởng làng', 5, 32000, 1, 0,
 '1Xatu: ...Cháu tới rồi. Ông nội cháu nhờ ta trông nom cháu. Ta là Xatu, trưởng làng.>1Xatu: Chạm xuống đất để đi. Chạm vào một Pokémon trong làng để nói chuyện.>1Xatu: Ai có dấu nhiệm vụ trên đầu là người cháu cần gặp. Bản đồ nhỏ cũng chấm chỗ họ đứng.>1Xatu: Mở Nhiệm vụ, chọn nhiệm vụ rồi bấm Đi tới, cháu sẽ tự tìm đường.>1Xatu: Đã quen chơi thì vào Sổ tay huấn luyện (NPC Đổi thưởng) mà bỏ qua hướng dẫn. Còn không, ta dẫn cháu đi một vòng làng.',
 'Nói chuyện với Xatu ở làng', '32000', '32000', '32000', 3, '32000', '32000', 100, 1292, '32000', '32000', '32000', 0,
 'Nhiệm vụ:|Nói chuyện với Xatu|Địa điểm:|Làng khởi đầu|NPC:|Xatu|Phần thưởng:|1.292 EXP, 100 xu',
 'Nhiệm vụ hoàn thành|Xatu: Việc đầu tiên: mang thư của ta cho Blaziken.', 0, 0, 0),
(902, 'Thư gửi Blaziken', 5, 21, 1, 0,
 '1Xatu: Blaziken là huấn luyện viên của làng. Cậu ấy dạy cách đánh và cách dùng chiêu thức.>1Xatu: Cháu mang thư giới thiệu này cho Blaziken nhé?',
 'Mang thư cho Blaziken, người có dấu nhiệm vụ trên đầu',
 '1Blaziken: Lửa lên nào! Thư của trưởng làng à? Tốt.>1Blaziken: Muốn mạnh lên thì đánh quái, cộng điểm kỹ năng và học chiêu mới ở chỗ ta. Cầm mấy bình thuốc này.',
 '51', '32000', 1, '1', '32000', 100, 1292, '1', '3', '32000', 0,
 'Nhiệm vụ:|Mang Thư giới thiệu cho Blaziken|Địa điểm:|Làng khởi đầu|NPC:|Xatu → Blaziken|Phần thưởng:|1.292 EXP, 100 xu, 3 Thuốc hồi máu',
 'Nhiệm vụ hoàn thành|Blaziken: Giờ ra đồng tập đánh thôi.', 0, 0, 0),
(903, 'Bình thuốc khi máu thấp', 21, 21, 1, 0,
 '1Blaziken: Ra cổng phía bắc của làng là đồng cỏ Pallet. Chạm vào quái để đánh, bấm nút chiêu để đánh mạnh hơn.>1Blaziken: Bị đánh sẽ mất máu. Máu vơi thì mở Hành trang dùng Thuốc hồi máu, hoặc đặt bình thuốc vào ô phím tắt.>1Blaziken: Ra đồng đánh thử, máu vơi thì uống 1 bình rồi về đây. Đi chứ?',
 'Đánh quái ở Pallet, máu vơi thì dùng 1 bình thuốc',
 '1Blaziken: Biết giữ máu là sống lâu. Cầm mấy bình Ether này, hết PP thì uống.>1Blaziken: Giờ là bài thật: hạ 8 Caterpie.',
 '53', '32000', 2, '1', '32000', 150, 1735, '4', '2', '32000', 0,
 'Nhiệm vụ:|Dùng 1 bình thuốc khi máu vơi|Địa điểm:|Pallet (cổng phía bắc làng)|NPC:|Blaziken|Phần thưởng:|1.735 EXP, 150 xu, 2 Ether',
 'Nhiệm vụ hoàn thành|Blaziken: Giờ là bài thật: hạ 8 Caterpie.', 0, 0, 0),
(904, 'Bài học chiến đấu', 21, 21, 1, 0,
 '1Blaziken: Caterpie sống ở đồng cỏ Pallet. Hạ 8 con rồi quay về đây.>1Blaziken: Mỏi tay thì bật Tự động: Pokémon tự đánh quái gần nhất, tự uống thuốc khi máu thấp. Đi chứ?',
 'Hạ 8 Caterpie ở Pallet',
 '1Blaziken: Khá lắm! Mỗi lần lên cấp cháu có thêm 1 điểm kỹ năng.>1Blaziken: Ta chỉ cháu cách dùng điểm đó.',
 '32000', '2', 0, '32000', '8', 100, 1735, '32000', '32000', '32000', 0,
 'Nhiệm vụ:|Hạ 8 Caterpie|Địa điểm:|Pallet (cổng phía bắc làng)|NPC:|Blaziken|Phần thưởng:|1.735 EXP, 100 xu',
 'Nhiệm vụ hoàn thành|Blaziken: Khá lắm! Mỗi lần lên cấp cháu có thêm 1 điểm kỹ năng.', 0, 0, 0),
(905, 'Chiêu thức đầu tiên', 21, 0, 1, 0,
 '1Blaziken: Lên cấp là được điểm kỹ năng. Mở Menu, chọn Pokémon, sang thẻ Kỹ năng, chọn chiêu rồi bấm Học để cộng 1 điểm.>1Blaziken: Chiêu mới thì học ở chỗ ta khi đủ xu. Cộng 1 điểm xong thì tới Chansey trước cửa Trung tâm Pokémon hồi sức nhé?',
 'Cộng 1 điểm kỹ năng, rồi gặp Chansey ở Trung tâm Pokémon',
 '1Chansey: Chan-sey! Chị là y tá của làng. Chạm vào chị để hồi đầy HP và PP, miễn phí.>1Chansey: Chị cũng bán thuốc. Cầm thêm mấy bình này nhé.',
 '52', '32000', 2, '1', '32000', 100, 2370, '1', '3', '32000', 0,
 'Nhiệm vụ:|Cộng 1 điểm kỹ năng|Địa điểm:|Menu → Pokémon → Kỹ năng, rồi Chansey|NPC:|Blaziken → Chansey|Phần thưởng:|2.370 EXP, 100 xu, 3 Thuốc hồi máu',
 'Nhiệm vụ hoàn thành|Chansey: Máu thấp thì về đây với chị.', 0, 0, 0),
(906, 'Đơn thuốc cho Kecleon', 0, 1, 1, 0,
 '1Chansey: Thuốc của chị nhập từ Kecleon, tiệm tạp hoá của làng.>1Chansey: Em mang đơn thuốc này cho Kecleon giúp chị nhé?',
 'Mang đơn thuốc cho Kecleon ở tiệm tạp hoá',
 '1Kecleon: Hí hí, khách mới! Tiệm tôi bán Thuốc hồi máu, Ether và đồ dùng hằng ngày.>1Kecleon: Chạm vào tôi, chọn Mua là xem hàng. Quà làm quen: một chiếc Ruy băng!',
 '54', '32000', 1, '1', '32000', 100, 2370, '32000', '32000', '1001,1001,1001,1001,1001,0', 0,
 'Nhiệm vụ:|Mang Đơn thuốc cho Kecleon|Địa điểm:|Làng khởi đầu|NPC:|Chansey → Kecleon|Phần thưởng:|2.370 EXP, 100 xu, Ruy băng sơ cấp',
 'Nhiệm vụ hoàn thành|Kecleon: Thử mặc Ruy băng xem nào.', 0, 0, 0),
(907, 'Mặc đồ mới', 1, 2, 1, 0,
 '1Kecleon: Mở Hành trang, chọn Ruy băng rồi chọn Mặc. Đồ mặc vào là tăng chỉ số ngay.>1Kecleon: Mặc xong thì ghé Machamp thợ rèn, cậu ấy lo nâng cấp đồ. Đi nhé?',
 'Mặc Ruy băng, rồi gặp Machamp thợ rèn',
 '1Machamp: Khà khà! Ruy băng hợp với cháu đấy.>1Machamp: Ta là thợ rèn: nâng cấp, khảm ngọc, sửa đồ. Bronzong bán giáp, Persian bán đá quý và nguyên liệu.',
 '55', '32000', 2, '1', '32000', 200, 3131, '32000', '32000', '32000', 0,
 'Nhiệm vụ:|Mặc Ruy băng sơ cấp|Địa điểm:|Hành trang, rồi Machamp|NPC:|Kecleon → Machamp|Phần thưởng:|3.131 EXP, 200 xu',
 'Nhiệm vụ hoàn thành|Machamp: Đồ tốt thì phải giữ cho kỹ.', 0, 0, 0),
(908, 'Ghé ngân hàng Meowth', 2, 22, 1, 0,
 '1Machamp: Xu kiếm được nên gửi bớt ở Meowth, ngân hàng của làng.>1Machamp: Cháu đưa phiếu này cho Meowth giúp ta nhé?',
 'Mang phiếu cho Meowth ở ngân hàng',
 '1Meowth: Meo! Ta giữ xu và lượng giúp cháu, chạm vào ta rồi chọn Kho xu, lượng.>1Meowth: Đồ nhiều thì gửi Kangaskhan thủ kho. Thư và mã quà tặng xem ở Menu → Thư/Quà.',
 '56', '32000', 1, '1', '32000', 200, 3131, '32000', '32000', '32000', 0,
 'Nhiệm vụ:|Mang Phiếu gửi cho Meowth|Địa điểm:|Làng khởi đầu|NPC:|Machamp → Meowth|Phần thưởng:|3.131 EXP, 200 xu',
 'Nhiệm vụ hoàn thành|Meowth: Ao trong làng có cá đấy, thử câu xem.', 0, 0, 0),
(909, 'Thả câu ở ao làng', 22, 7, 1, 0,
 '1Meowth: Ao trong làng có cá. Đứng sát mép ao, mở Menu → Hệ sinh thái → Câu cá để thả câu.>1Meowth: Mỗi lần câu tốn ít thể lực và xu mồi. Câu thử một lần rồi tới chỗ Pidgeot nhé?',
 'Thả câu 1 lần ở ao trong làng, rồi gặp Pidgeot',
 '1Pidgeot: Gió lên rồi! Ta chở cháu bay tới các vùng xa, trả bằng xu.>1Pidgeot: Vùng mới mở dần theo cấp. Cầm thêm mấy bình Ether cho chuyến đi.',
 '57', '32000', 2, '1', '32000', 150, 4269, '4', '2', '32000', 0,
 'Nhiệm vụ:|Thả câu 1 lần ở ao làng|Địa điểm:|Ao trong làng, rồi Pidgeot|NPC:|Meowth → Pidgeot|Phần thưởng:|4.269 EXP, 150 xu, 2 Ether',
 'Nhiệm vụ hoàn thành|Pidgeot: Về báo trưởng làng đi.', 0, 0, 0),
(910, 'Về báo trưởng làng', 7, 5, 1, 0,
 '1Pidgeot: Cầm chiếc lông vũ này về cho Xatu, ông ấy sẽ biết cháu đã đi hết làng.>1Pidgeot: Nhận lông vũ chứ?',
 'Mang Lông vũ Pidgeot về cho Xatu',
 '1Xatu: ...Cháu đã đi hết một vòng làng. Giỏi lắm.>1Xatu: Pokédex ở Menu ghi mọi loài cháu gặp. Sổ tay huấn luyện ở NPC Đổi thưởng có nhiệm vụ ngày, tuần và thách đấu Gym.>1Xatu: Từ giờ là hành trình thật sự. Đêm cháu tới có mưa sao băng... ta có chuyện muốn kể.',
 '58', '32000', 1, '1', '32000', 250, 4269, '32000', '32000', '32000', 0,
 'Nhiệm vụ:|Mang Lông vũ Pidgeot cho Xatu|Địa điểm:|Làng khởi đầu|NPC:|Pidgeot → Xatu|Phần thưởng:|4.269 EXP, 250 xu',
 'Nhiệm vụ hoàn thành|Xatu: Hết hướng dẫn. Nói chuyện với ta để bắt đầu cốt truyện.', 0, 0, 0)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`), `npcrcv` = VALUES(`npcrcv`), `npcres` = VALUES(`npcres`), `lv` = VALUES(`lv`),
  `mainsub` = VALUES(`mainsub`), `content` = VALUES(`content`), `scontent` = VALUES(`scontent`),
  `rescontent` = VALUES(`rescontent`), `idItemget` = VALUES(`idItemget`), `idMonskill` = VALUES(`idMonskill`),
  `type` = VALUES(`type`), `nitemget` = VALUES(`nitemget`), `nmonskill` = VALUES(`nmonskill`), `gold` = VALUES(`gold`),
  `exp` = VALUES(`exp`), `potiongift` = VALUES(`potiongift`), `npotion` = VALUES(`npotion`), `itemgift` = VALUES(`itemgift`),
  `deltaLv` = VALUES(`deltaLv`), `infogift` = VALUES(`infogift`), `infofinish` = VALUES(`infofinish`),
  `pactive` = VALUES(`pactive`), `luong` = VALUES(`luong`), `luongLock` = VALUES(`luongLock`);

-- meta (engine doc kieu_muc_tieu / muc_tieu_id cua buoc hanh dong; cap 1 het: cap chuoi chinh khong giam sau huong dan)
UPDATE `data_quest_lau_dai` SET `ten` = 'Chào trưởng làng', `npc_giao` = 5, `ten_npc` = 'Xatu', `dia_danh` = 'Làng', `cap_toi_thieu` = 1, `kieu_muc_tieu` = 'noi_chuyen', `muc_tieu_id` = '', `muc_tieu_ten` = '', `so_luong` = 1, `exp` = 1292, `xu` = 100, `luong_khoa` = 0, `luong` = 0, `vat_pham` = '', `ghi_chu` = 'Hướng dẫn tân thủ: 10 bước trong làng ~20 phút' WHERE `ma` = 'HD.01';
UPDATE `data_quest_lau_dai` SET `ten` = 'Thư gửi Blaziken', `npc_giao` = 5, `ten_npc` = 'Xatu', `dia_danh` = 'Làng', `cap_toi_thieu` = 1, `kieu_muc_tieu` = 'dua_thu', `muc_tieu_id` = '51', `muc_tieu_ten` = 'Thư giới thiệu của Xatu', `so_luong` = 1, `exp` = 1292, `xu` = 100, `luong_khoa` = 0, `luong` = 0, `vat_pham` = '', `ghi_chu` = 'Hướng dẫn tân thủ: 10 bước trong làng ~20 phút' WHERE `ma` = 'HD.02';
UPDATE `data_quest_lau_dai` SET `ten` = 'Bình thuốc khi máu thấp', `npc_giao` = 21, `ten_npc` = 'Blaziken', `dia_danh` = 'Pallet', `cap_toi_thieu` = 1, `kieu_muc_tieu` = 'dung_thuoc', `muc_tieu_id` = '', `muc_tieu_ten` = 'Bình thuốc', `so_luong` = 1, `exp` = 1735, `xu` = 150, `luong_khoa` = 0, `luong` = 0, `vat_pham` = '', `ghi_chu` = 'Hướng dẫn tân thủ: 10 bước trong làng ~20 phút' WHERE `ma` = 'HD.03';
UPDATE `data_quest_lau_dai` SET `ten` = 'Bài học chiến đấu', `npc_giao` = 21, `ten_npc` = 'Blaziken', `dia_danh` = 'Pallet', `cap_toi_thieu` = 1, `kieu_muc_tieu` = 'giet', `muc_tieu_id` = '2', `muc_tieu_ten` = 'Caterpie', `so_luong` = 8, `exp` = 1735, `xu` = 100, `luong_khoa` = 0, `luong` = 0, `vat_pham` = '', `ghi_chu` = 'Hướng dẫn tân thủ: 10 bước trong làng ~20 phút' WHERE `ma` = 'HD.04';
UPDATE `data_quest_lau_dai` SET `ten` = 'Chiêu thức đầu tiên', `npc_giao` = 21, `ten_npc` = 'Blaziken', `dia_danh` = 'Làng', `cap_toi_thieu` = 1, `kieu_muc_tieu` = 'hoc_chieu', `muc_tieu_id` = '', `muc_tieu_ten` = 'Điểm kỹ năng', `so_luong` = 1, `exp` = 2370, `xu` = 100, `luong_khoa` = 0, `luong` = 0, `vat_pham` = '', `ghi_chu` = 'Hướng dẫn tân thủ: 10 bước trong làng ~20 phút' WHERE `ma` = 'HD.05';
UPDATE `data_quest_lau_dai` SET `ten` = 'Đơn thuốc cho Kecleon', `npc_giao` = 0, `ten_npc` = 'Chansey', `dia_danh` = 'Làng', `cap_toi_thieu` = 1, `kieu_muc_tieu` = 'dua_thu', `muc_tieu_id` = '54', `muc_tieu_ten` = 'Đơn thuốc của Chansey', `so_luong` = 1, `exp` = 2370, `xu` = 100, `luong_khoa` = 0, `luong` = 0, `vat_pham` = 'Ruy băng sơ cấp', `ghi_chu` = 'Hướng dẫn tân thủ: 10 bước trong làng ~20 phút' WHERE `ma` = 'HD.06';
UPDATE `data_quest_lau_dai` SET `ten` = 'Mặc đồ mới', `npc_giao` = 1, `ten_npc` = 'Kecleon', `dia_danh` = 'Làng', `cap_toi_thieu` = 1, `kieu_muc_tieu` = 'nang_cap', `muc_tieu_id` = '1001', `muc_tieu_ten` = 'Ruy băng sơ cấp', `so_luong` = 1, `exp` = 3131, `xu` = 200, `luong_khoa` = 0, `luong` = 0, `vat_pham` = '', `ghi_chu` = 'Hướng dẫn tân thủ: 10 bước trong làng ~20 phút' WHERE `ma` = 'HD.07';
UPDATE `data_quest_lau_dai` SET `ten` = 'Ghé ngân hàng Meowth', `npc_giao` = 2, `ten_npc` = 'Machamp', `dia_danh` = 'Làng', `cap_toi_thieu` = 1, `kieu_muc_tieu` = 'dua_thu', `muc_tieu_id` = '56', `muc_tieu_ten` = 'Phiếu gửi của Machamp', `so_luong` = 1, `exp` = 3131, `xu` = 200, `luong_khoa` = 0, `luong` = 0, `vat_pham` = '', `ghi_chu` = 'Hướng dẫn tân thủ: 10 bước trong làng ~20 phút' WHERE `ma` = 'HD.08';
UPDATE `data_quest_lau_dai` SET `ten` = 'Thả câu ở ao làng', `npc_giao` = 22, `ten_npc` = 'Meowth', `dia_danh` = 'Làng', `cap_toi_thieu` = 1, `kieu_muc_tieu` = 'tha_cau', `muc_tieu_id` = '', `muc_tieu_ten` = 'Lần thả câu', `so_luong` = 1, `exp` = 4269, `xu` = 150, `luong_khoa` = 0, `luong` = 0, `vat_pham` = '', `ghi_chu` = 'Hướng dẫn tân thủ: 10 bước trong làng ~20 phút' WHERE `ma` = 'HD.09';
UPDATE `data_quest_lau_dai` SET `ten` = 'Về báo trưởng làng', `npc_giao` = 7, `ten_npc` = 'Pidgeot', `dia_danh` = 'Làng', `cap_toi_thieu` = 1, `kieu_muc_tieu` = 'dua_thu', `muc_tieu_id` = '58', `muc_tieu_ten` = 'Lông vũ Pidgeot', `so_luong` = 1, `exp` = 4269, `xu` = 250, `luong_khoa` = 0, `luong` = 0, `vat_pham` = '', `ghi_chu` = 'Hướng dẫn tân thủ: 10 bước trong làng ~20 phút' WHERE `ma` = 'HD.10';

-- noi dung (web / So tay doc; client doc data_quest_moi)
UPDATE `data_nhiem_vu_noi_dung` d JOIN `data_quest_moi` q ON q.`id` = CAST(SUBSTRING(d.`ma`, 4) AS UNSIGNED) + 900
  SET d.`mo_ta` = q.`scontent`, d.`muc_tieu_text` = q.`scontent`, d.`goi_y` = q.`scontent`,
      d.`hoi_thoai_bat_dau` = REPLACE(REPLACE(SUBSTRING(q.`content`, 2), '>1', ' | '), '>0', ' | Bạn: '),
      d.`hoi_thoai_xong` = SUBSTRING_INDEX(q.`infofinish`, '|', -1),
      d.`dong_thuong` = CONCAT('Thưởng: ', SUBSTRING_INDEX(q.`infogift`, '|', -1)), d.`thuong_khac` = ''
  WHERE d.`ma` LIKE 'HD.%' AND q.`mainsub` = 0;

-- 4. Kieu muc tieu moi -------------------------------------------------------------------------------------------------
INSERT INTO `data_nhiem_vu_kieu` (`ma`, `ten`, `co_che`, `du_phong`, `trang_thai`, `so_dong`) VALUES
('dung_thuoc', 'Dùng thuốc', 'DungVatPham.dungThuoc (bình đã trừ khỏi túi) → DiemMocNhiemVu.khiDungThuoc', '2', 'co_san', 1),
('hoc_chieu', 'Học chiêu', 'Map.doAddSkillPoint / doLearnSkil → DiemMocNhiemVu.khiHocChieu', '2', 'co_san', 1),
('tha_cau', 'Thả câu', 'HeSinhThaiMenu.thaCau (đã trừ thể lực + mồi) → DiemMocNhiemVu.khiThaCau', '2', 'co_san', 1)
ON DUPLICATE KEY UPDATE `ten` = VALUES(`ten`), `co_che` = VALUES(`co_che`), `du_phong` = VALUES(`du_phong`),
  `trang_thai` = VALUES(`trang_thai`), `so_dong` = VALUES(`so_dong`);

-- 5. Chansey + Snorlax o ca ba lang (cMap 0 / 70 / 80) ---------------------------------------------------------------
UPDATE `data_npc` SET `ban_do` = '0,43,45,47,49,51,54,56,59,60,62,70,80,100,281' WHERE `type` = 0;
UPDATE `data_npc` SET `ban_do` = '0,51,57,60,70,80,105,160' WHERE `type` = 8;

-- 6. Chuong 1-2: noi tiep huong dan, NPC giao = nguoi noi, muc tieu dung voi cach game tinh (noi chuyen) ---------------
UPDATE `data_quest_moi` SET `name` = 'Đêm mưa sao băng',
  `content` = '1Xatu: Đêm cháu tới, cả làng thấy một trận mưa sao băng.>1Xatu: Ta mơ thấy một ngôi sao rơi xuống đồng cỏ, và cháu đứng ngay chỗ nó rơi.>0Cháu... không nhớ gì về đêm đó cả.>1Xatu: Không sao. Ký ức sẽ về khi cháu bước đi.',
  `scontent` = 'Nói chuyện với Xatu ở làng',
  `infogift` = REPLACE(REPLACE(`infogift`, 'Nói chuyện với Xatu ở Pallet|', 'Nói chuyện với Xatu|'), 'Địa điểm:|Pallet|', 'Địa điểm:|Làng khởi đầu|'),
  `infofinish` = 'Nhiệm vụ hoàn thành|Xatu: Ông Snorlax ở làng sẽ cho cháu ăn sáng, rồi ông kể tiếp.'
  WHERE `id` = 1001 AND `mainsub` = 0;
UPDATE `data_quest_lau_dai` SET `ten` = 'Đêm mưa sao băng', `dia_danh` = 'Làng' WHERE `ma` = 'C01.1';
UPDATE `data_quest_moi` SET `npcrcv` = 0,
  `scontent` = 'Nghe Chansey kể về nơi ngôi sao rơi',
  `infogift` = REPLACE(REPLACE(`infogift`, 'Đứng tại điểm đánh dấu trên Pallet (ô 53,19)|', 'Nghe Chansey kể về nơi ngôi sao rơi|'), 'Địa điểm:|Pallet|', 'Địa điểm:|Làng khởi đầu|')
  WHERE `id` = 1005 AND `mainsub` = 0;
UPDATE `data_quest_lau_dai` SET `npc_giao` = 0, `ten_npc` = 'Chansey' WHERE `ma` = 'C01.5';
UPDATE `data_quest_moi` SET
  `scontent` = 'Nghe Kecleon bày cách phục kích ở bụi cỏ cao',
  `infogift` = REPLACE(REPLACE(`infogift`, 'Đứng tại điểm đánh dấu trên Pallet (ô 53,19)|', 'Nghe Kecleon bày cách phục kích|'), 'Địa điểm:|Pallet|', 'Địa điểm:|Làng khởi đầu|')
  WHERE `id` = 1010 AND `mainsub` = 0;

-- SUA CHUOI CHINH (sinh: tools/pokemon/sinh_sua_chuoi_chinh.py) ------------------------------------------------------
-- 1002 C01.2: NPC 8 chi o [0, 51, 57, 60, 70, 80, 105, 160]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Pallet', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Pallet|', 'Địa điểm:|Làng khởi đầu|'), ' ở Pallet|', ' ở làng|'), 200) WHERE `id` = 1002 AND `mainsub` = 0;
-- 1004 C01.4: NPC 0 chi o [0, 43, 45, 47, 49, 51, 54, 56, 59, 60, 62, 70, 80, 100, 281]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Pallet', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Pallet|', 'Địa điểm:|Làng khởi đầu|'), ' ở Pallet|', ' ở làng|'), 200) WHERE `id` = 1004 AND `mainsub` = 0;
-- 1007 C02.1: NPC 6 chi o [0, 53, 70, 79, 80, 89, 109]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Pallet', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Pallet|', 'Địa điểm:|Làng khởi đầu|'), ' ở Pallet|', ' ở làng|'), 200) WHERE `id` = 1007 AND `mainsub` = 0;
-- 1009 C02.3: NPC 1 chi o [0, 45, 47, 49, 54, 56, 62, 70, 72, 80, 82, 102]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Pallet', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Pallet|', 'Địa điểm:|Làng khởi đầu|'), ' ở Pallet|', ' ở làng|'), 200) WHERE `id` = 1009 AND `mainsub` = 0;
-- 1011 C02.5: quai 5 chi moc o [2]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Pallet', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở Viridian'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Pallet|', 'Địa điểm:|Viridian|'), 200) WHERE `id` = 1011 AND `mainsub` = 0;
-- 1013 C03.1: NPC 1 chi o [0, 45, 47, 49, 54, 56, 62, 70, 72, 80, 82, 102]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Viridian', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Viridian|', 'Địa điểm:|Làng khởi đầu|'), ' ở Viridian|', ' ở làng|'), 200) WHERE `id` = 1013 AND `mainsub` = 0;
-- 1014 C03.2: quai 6 chi moc o [4]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Viridian', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Cerulean'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Viridian|', 'Địa điểm:|Cerulean|'), 200) WHERE `id` = 1014 AND `mainsub` = 0;
-- 1017 C03.5: quai 9 chi moc o [3]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Viridian', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Vermilion'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Viridian|', 'Địa điểm:|Vermilion|'), 200) WHERE `id` = 1017 AND `mainsub` = 0;
-- 1018 C03.6: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Viridian', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Viridian|', 'Địa điểm:|Làng khởi đầu|'), ' ở Viridian|', ' ở làng|'), 200) WHERE `id` = 1018 AND `mainsub` = 0;
-- 1019 C04.1: NPC 21 chi o [0, 44, 50, 51, 52, 62, 70, 80, 163, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Rừng Viridian', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Rừng Viridian|', 'Địa điểm:|Làng khởi đầu|'), ' ở Rừng Viridian|', ' ở làng|'), 200) WHERE `id` = 1019 AND `mainsub` = 0;
-- 1023 C04.5: quai 11 chi moc o [5]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Rừng Viridian', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở Lavender'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Rừng Viridian|', 'Địa điểm:|Lavender|'), 200) WHERE `id` = 1023 AND `mainsub` = 0;
-- 1024 C04.6: quai 7 chi moc o [3, 4]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Rừng Viridian', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở Vermilion'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Rừng Viridian|', 'Địa điểm:|Vermilion|'), 200) WHERE `id` = 1024 AND `mainsub` = 0;
-- 1025 C04.7: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Rừng Viridian', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Rừng Viridian|', 'Địa điểm:|Làng khởi đầu|'), ' ở Rừng Viridian|', ' ở làng|'), 200) WHERE `id` = 1025 AND `mainsub` = 0;
-- 1029 C05.4: quai 10 chi moc o [3]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Pewter', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở Vermilion'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Pewter|', 'Địa điểm:|Vermilion|'), 200) WHERE `id` = 1029 AND `mainsub` = 0;
-- 1030 C05.5: quai 9 chi moc o [3]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Pewter', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở Vermilion'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Pewter|', 'Địa điểm:|Vermilion|'), 200) WHERE `id` = 1030 AND `mainsub` = 0;
-- 1035 C06.4: quai 17 chi moc o [6]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Pewter', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở Celadon'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Pewter|', 'Địa điểm:|Celadon|'), 200) WHERE `id` = 1035 AND `mainsub` = 0;
-- 1037 C06.6: quai 16 chi moc o [7]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Pewter|', 'Địa điểm:|Saffron|'), 200) WHERE `id` = 1037 AND `mainsub` = 0;
-- 1038 C07.1: NPC 8 chi o [0, 51, 57, 60, 70, 80, 105, 160]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Núi Mặt Trăng', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Núi Mặt Trăng|', 'Địa điểm:|Làng khởi đầu|'), ' ở Núi Mặt Trăng|', ' ở làng|'), 200) WHERE `id` = 1038 AND `mainsub` = 0;
-- 1039 C07.2: quai 15 chi moc o [6]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Núi Mặt Trăng', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở Celadon'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Núi Mặt Trăng|', 'Địa điểm:|Celadon|'), 200) WHERE `id` = 1039 AND `mainsub` = 0;
UPDATE `data_quest_lau_dai` SET `npc_giao` = 0, `ten_npc` = 'Chansey' WHERE `id_so` = 1040;
-- 1040 C07.3: loi dau cua Chansey, giao boi Snorlax
UPDATE `data_quest_moi` SET `npcrcv` = 0, `infogift` = LEFT(REPLACE(`infogift`, 'NPC:|Snorlax|', 'NPC:|Chansey|'), 200) WHERE `id` = 1040 AND `mainsub` = 0;
-- 1041 C07.4: NPC 8 chi o [0, 51, 57, 60, 70, 80, 105, 160]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Núi Mặt Trăng', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Núi Mặt Trăng|', 'Địa điểm:|Làng khởi đầu|'), ' ở Núi Mặt Trăng|', ' ở làng|'), 200) WHERE `id` = 1041 AND `mainsub` = 0;
-- 1042 C07.5: quai 16 chi moc o [7]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Núi Mặt Trăng', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở Saffron'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Núi Mặt Trăng|', 'Địa điểm:|Saffron|'), 200) WHERE `id` = 1042 AND `mainsub` = 0;
-- 1044 C08.1: NPC 21 chi o [0, 44, 50, 51, 52, 62, 70, 80, 163, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Núi Mặt Trăng', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Núi Mặt Trăng|', 'Địa điểm:|Làng khởi đầu|'), ' ở Núi Mặt Trăng|', ' ở làng|'), 200) WHERE `id` = 1044 AND `mainsub` = 0;
-- 1046 C08.3: quai 19 chi moc o [7]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Núi Mặt Trăng', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở Saffron'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Núi Mặt Trăng|', 'Địa điểm:|Saffron|'), 200) WHERE `id` = 1046 AND `mainsub` = 0;
-- 1047 C08.4: quai 14 chi moc o [6]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Núi Mặt Trăng', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở Celadon'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Núi Mặt Trăng|', 'Địa điểm:|Celadon|'), 200) WHERE `id` = 1047 AND `mainsub` = 0;
-- 1050 C08.7: quai 28 chi moc o [8]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Núi Mặt Trăng', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở Fuchsia'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Núi Mặt Trăng|', 'Địa điểm:|Fuchsia|'), 200) WHERE `id` = 1050 AND `mainsub` = 0;
-- 1053 C09.3: quai 26 chi moc o [9]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cerulean', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Cinnabar'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cerulean|', 'Địa điểm:|Cinnabar|'), 200) WHERE `id` = 1053 AND `mainsub` = 0;
-- 1058 C10.2: NPC 10 chi o [43, 58, 60, 106, 162]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cerulean', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Bến tàu'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Cerulean|', 'Địa điểm:|Bến tàu|'), ' ở Cerulean|', ' ở Bến tàu|'), 200) WHERE `id` = 1058 AND `mainsub` = 0;
-- 1060 C10.4: quai 28 chi moc o [8]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cerulean', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Fuchsia'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cerulean|', 'Địa điểm:|Fuchsia|'), 200) WHERE `id` = 1060 AND `mainsub` = 0;
-- 1061 C10.5: quai 28 chi moc o [8]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cerulean', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Fuchsia'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cerulean|', 'Địa điểm:|Fuchsia|'), 200) WHERE `id` = 1061 AND `mainsub` = 0;
-- 1062 C10.6: quai 26 chi moc o [9]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cerulean|', 'Địa điểm:|Cinnabar|'), 200) WHERE `id` = 1062 AND `mainsub` = 0;
-- 1063 C11.1: NPC 22 chi o [0, 47, 49, 56, 62, 70, 80]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Vermilion', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Vermilion|', 'Địa điểm:|Làng khởi đầu|'), ' ở Vermilion|', ' ở làng|'), 200) WHERE `id` = 1063 AND `mainsub` = 0;
-- 1065 C11.3: NPC 9 chi o [0, 70, 78, 80, 88, 108]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Vermilion', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Vermilion|', 'Địa điểm:|Làng khởi đầu|'), ' ở Vermilion|', ' ở làng|'), 200) WHERE `id` = 1065 AND `mainsub` = 0;
-- 1066 C11.4: quai 26 chi moc o [9]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Vermilion', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở Cinnabar'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Vermilion|', 'Địa điểm:|Cinnabar|'), 200) WHERE `id` = 1066 AND `mainsub` = 0;
-- 1067 C11.5: quai 26 chi moc o [9]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Vermilion', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở Cinnabar'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Vermilion|', 'Địa điểm:|Cinnabar|'), 200) WHERE `id` = 1067 AND `mainsub` = 0;
-- 1068 C11.6: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Vermilion', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Vermilion|', 'Địa điểm:|Làng khởi đầu|'), ' ở Vermilion|', ' ở làng|'), 200) WHERE `id` = 1068 AND `mainsub` = 0;
-- 1069 C12.1: NPC 21 chi o [0, 44, 50, 51, 52, 62, 70, 80, 163, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Vermilion', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Vermilion|', 'Địa điểm:|Làng khởi đầu|'), ' ở Vermilion|', ' ở làng|'), 200) WHERE `id` = 1069 AND `mainsub` = 0;
-- 1072 C12.4: quai 27 chi moc o [110]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Vermilion', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở Hang động'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Vermilion|', 'Địa điểm:|Hang động|'), 200) WHERE `id` = 1072 AND `mainsub` = 0;
-- 1073 C12.5: NPC 21 chi o [0, 44, 50, 51, 52, 62, 70, 80, 163, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Vermilion', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Vermilion|', 'Địa điểm:|Làng khởi đầu|'), ' ở Vermilion|', ' ở làng|'), 200) WHERE `id` = 1073 AND `mainsub` = 0;
-- 1074 C12.6: quai 29 chi moc o [9]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Vermilion|', 'Địa điểm:|Cinnabar|'), 200) WHERE `id` = 1074 AND `mainsub` = 0;
-- 1075 C13.1: NPC 22 chi o [0, 47, 49, 56, 62, 70, 80]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Vermilion', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Vermilion|', 'Địa điểm:|Làng khởi đầu|'), ' ở Vermilion|', ' ở làng|'), 200) WHERE `id` = 1075 AND `mainsub` = 0;
-- 1077 C13.3: quai 27 chi moc o [110]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Vermilion', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở Hang động'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Vermilion|', 'Địa điểm:|Hang động|'), 200) WHERE `id` = 1077 AND `mainsub` = 0;
-- 1078 C13.4: NPC 6 chi o [0, 53, 70, 79, 80, 89, 109]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Vermilion', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Vermilion|', 'Địa điểm:|Làng khởi đầu|'), ' ở Vermilion|', ' ở làng|'), 200) WHERE `id` = 1078 AND `mainsub` = 0;
-- 1079 C13.5: NPC 22 chi o [0, 47, 49, 56, 62, 70, 80]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Vermilion', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Vermilion|', 'Địa điểm:|Làng khởi đầu|'), ' ở Vermilion|', ' ở làng|'), 200) WHERE `id` = 1079 AND `mainsub` = 0;
-- 1081 C14.1: NPC 6 chi o [0, 53, 70, 79, 80, 89, 109]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Tàu S.S. Anne', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Tàu S.S. Anne|', 'Địa điểm:|Làng khởi đầu|'), ' ở Tàu S.S. Anne|', ' ở làng|'), 200) WHERE `id` = 1081 AND `mainsub` = 0;
-- 1082 C14.2: NPC 9 chi o [0, 70, 78, 80, 88, 108]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Tàu S.S. Anne', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Tàu S.S. Anne|', 'Địa điểm:|Làng khởi đầu|'), ' ở Tàu S.S. Anne|', ' ở làng|'), 200) WHERE `id` = 1082 AND `mainsub` = 0;
-- 1083 C14.3: NPC 1 chi o [0, 45, 47, 49, 54, 56, 62, 70, 72, 80, 82, 102]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Tàu S.S. Anne', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Tàu S.S. Anne|', 'Địa điểm:|Làng khởi đầu|'), ' ở Tàu S.S. Anne|', ' ở làng|'), 200) WHERE `id` = 1083 AND `mainsub` = 0;
-- 1084 C14.4: quai 29 chi moc o [9]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Tàu S.S. Anne', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở Cinnabar'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Tàu S.S. Anne|', 'Địa điểm:|Cinnabar|'), 200) WHERE `id` = 1084 AND `mainsub` = 0;
-- 1086 C14.6: quai 18 chi moc o [110]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Tàu S.S. Anne', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở Hang động'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Tàu S.S. Anne|', 'Địa điểm:|Hang động|'), 200) WHERE `id` = 1086 AND `mainsub` = 0;
-- 1087 C15.1: NPC 9 chi o [0, 70, 78, 80, 88, 108]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Tàu S.S. Anne', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Tàu S.S. Anne|', 'Địa điểm:|Làng khởi đầu|'), ' ở Tàu S.S. Anne|', ' ở làng|'), 200) WHERE `id` = 1087 AND `mainsub` = 0;
-- 1088 C15.2: quai 29 chi moc o [9]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Tàu S.S. Anne', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở Cinnabar'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Tàu S.S. Anne|', 'Địa điểm:|Cinnabar|'), 200) WHERE `id` = 1088 AND `mainsub` = 0;
-- 1090 C15.4: quai 22 chi moc o [10]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Tàu S.S. Anne', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở New Bark'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Tàu S.S. Anne|', 'Địa điểm:|New Bark|'), 200) WHERE `id` = 1090 AND `mainsub` = 0;
-- 1091 C15.5: quai 30 chi moc o [9]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Tàu S.S. Anne', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở Cinnabar'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Tàu S.S. Anne|', 'Địa điểm:|Cinnabar|'), 200) WHERE `id` = 1091 AND `mainsub` = 0;
-- 1092 C15.6: NPC 6 chi o [0, 53, 70, 79, 80, 89, 109]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Tàu S.S. Anne', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Tàu S.S. Anne|', 'Địa điểm:|Làng khởi đầu|'), ' ở Tàu S.S. Anne|', ' ở làng|'), 200) WHERE `id` = 1092 AND `mainsub` = 0;
-- 1093 C16.1: NPC 21 chi o [0, 44, 50, 51, 52, 62, 70, 80, 163, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Tàu S.S. Anne', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Tàu S.S. Anne|', 'Địa điểm:|Làng khởi đầu|'), ' ở Tàu S.S. Anne|', ' ở làng|'), 200) WHERE `id` = 1093 AND `mainsub` = 0;
-- 1095 C16.3: NPC 10 chi o [43, 58, 60, 106, 162]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Tàu S.S. Anne', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở Bến tàu'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Tàu S.S. Anne|', 'Địa điểm:|Bến tàu|'), ' ở Tàu S.S. Anne|', ' ở Bến tàu|'), 200) WHERE `id` = 1095 AND `mainsub` = 0;
-- 1096 C16.4: NPC 0 chi o [0, 43, 45, 47, 49, 51, 54, 56, 59, 60, 62, 70, 80, 100, 281]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Tàu S.S. Anne', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Tàu S.S. Anne|', 'Địa điểm:|Làng khởi đầu|'), ' ở Tàu S.S. Anne|', ' ở làng|'), 200) WHERE `id` = 1096 AND `mainsub` = 0;
-- 1097 C16.5: quai 31 chi moc o [11]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Tàu S.S. Anne', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở Rừng Viridian'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Tàu S.S. Anne|', 'Địa điểm:|Rừng Viridian|'), 200) WHERE `id` = 1097 AND `mainsub` = 0;
-- 1099 C17.1: NPC 8 chi o [0, 51, 57, 60, 70, 80, 105, 160]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Lavender', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Lavender|', 'Địa điểm:|Làng khởi đầu|'), ' ở Lavender|', ' ở làng|'), 200) WHERE `id` = 1099 AND `mainsub` = 0;
-- 1101 C17.3: quai 32 chi moc o [10]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Lavender', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở New Bark'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Lavender|', 'Địa điểm:|New Bark|'), 200) WHERE `id` = 1101 AND `mainsub` = 0;
-- 1103 C17.5: quai 23 chi moc o [10]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Lavender', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở New Bark'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Lavender|', 'Địa điểm:|New Bark|'), 200) WHERE `id` = 1103 AND `mainsub` = 0;
-- 1104 C17.6: NPC 8 chi o [0, 51, 57, 60, 70, 80, 105, 160]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Lavender', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Lavender|', 'Địa điểm:|Làng khởi đầu|'), ' ở Lavender|', ' ở làng|'), 200) WHERE `id` = 1104 AND `mainsub` = 0;
-- 1105 C18.1: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Lavender', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Lavender|', 'Địa điểm:|Làng khởi đầu|'), ' ở Lavender|', ' ở làng|'), 200) WHERE `id` = 1105 AND `mainsub` = 0;
-- 1107 C18.3: quai 31 chi moc o [11]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Lavender', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Rừng Viridian'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Lavender|', 'Địa điểm:|Rừng Viridian|'), 200) WHERE `id` = 1107 AND `mainsub` = 0;
-- 1108 C18.4: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Lavender', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Lavender|', 'Địa điểm:|Làng khởi đầu|'), ' ở Lavender|', ' ở làng|'), 200) WHERE `id` = 1108 AND `mainsub` = 0;
-- 1110 C18.6: NPC 8 chi o [0, 51, 57, 60, 70, 80, 105, 160]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Lavender', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Lavender|', 'Địa điểm:|Làng khởi đầu|'), ' ở Lavender|', ' ở làng|'), 200) WHERE `id` = 1110 AND `mainsub` = 0;
-- 1111 C19.1: NPC 1 chi o [0, 45, 47, 49, 54, 56, 62, 70, 72, 80, 82, 102]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Celadon', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Celadon|', 'Địa điểm:|Làng khởi đầu|'), ' ở Celadon|', ' ở làng|'), 200) WHERE `id` = 1111 AND `mainsub` = 0;
-- 1114 C19.4: quai 55 chi moc o [13]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Celadon', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở Núi Mặt Trăng'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Celadon|', 'Địa điểm:|Núi Mặt Trăng|'), 200) WHERE `id` = 1114 AND `mainsub` = 0;
-- 1115 C19.5: quai 31 chi moc o [11]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Celadon', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở Rừng Viridian'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Celadon|', 'Địa điểm:|Rừng Viridian|'), 200) WHERE `id` = 1115 AND `mainsub` = 0;
-- 1116 C19.6: NPC 1 chi o [0, 45, 47, 49, 54, 56, 62, 70, 72, 80, 82, 102]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Celadon', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Celadon|', 'Địa điểm:|Làng khởi đầu|'), ' ở Celadon|', ' ở làng|'), 200) WHERE `id` = 1116 AND `mainsub` = 0;
-- 1117 C20.1: NPC 6 chi o [0, 53, 70, 79, 80, 89, 109]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Celadon', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Celadon|', 'Địa điểm:|Làng khởi đầu|'), ' ở Celadon|', ' ở làng|'), 200) WHERE `id` = 1117 AND `mainsub` = 0;
-- 1118 C20.2: quai 33 chi moc o [11]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Celadon', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở Rừng Viridian'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Celadon|', 'Địa điểm:|Rừng Viridian|'), 200) WHERE `id` = 1118 AND `mainsub` = 0;
-- 1119 C20.3: quai 35 chi moc o [11]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Celadon', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở Rừng Viridian'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Celadon|', 'Địa điểm:|Rừng Viridian|'), 200) WHERE `id` = 1119 AND `mainsub` = 0;
-- 1120 C20.4: quai 35 chi moc o [11]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Celadon', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở Rừng Viridian'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Celadon|', 'Địa điểm:|Rừng Viridian|'), 200) WHERE `id` = 1120 AND `mainsub` = 0;
-- 1121 C20.5: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Celadon', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Celadon|', 'Địa điểm:|Làng khởi đầu|'), ' ở Celadon|', ' ở làng|'), 200) WHERE `id` = 1121 AND `mainsub` = 0;
-- 1122 C20.6: quai 35 chi moc o [11]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Celadon', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở Rừng Viridian'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Celadon|', 'Địa điểm:|Rừng Viridian|'), 200) WHERE `id` = 1122 AND `mainsub` = 0;
-- 1123 C20.7: quai 40 chi moc o [13]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Celadon|', 'Địa điểm:|Núi Mặt Trăng|'), 200) WHERE `id` = 1123 AND `mainsub` = 0;
-- 1124 C21.1: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Saffron', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Saffron|', 'Địa điểm:|Làng khởi đầu|'), ' ở Saffron|', ' ở làng|'), 200) WHERE `id` = 1124 AND `mainsub` = 0;
UPDATE `data_quest_lau_dai` SET `npc_giao` = 3, `ten_npc` = 'Bronzong' WHERE `id_so` = 1125;
-- 1125 C21.2: loi dau cua Bronzong, giao boi Xatu
UPDATE `data_quest_moi` SET `npcrcv` = 3, `infogift` = LEFT(REPLACE(`infogift`, 'NPC:|Xatu|', 'NPC:|Bronzong|'), 200) WHERE `id` = 1125 AND `mainsub` = 0;
UPDATE `data_quest_lau_dai` SET `npc_giao` = 0, `ten_npc` = 'Chansey' WHERE `id_so` = 1128;
-- 1128 C21.5: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]; loi dau cua Chansey, giao boi Xatu
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Saffron', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở làng'), `scontent`), `npcrcv` = 0, `infogift` = LEFT(REPLACE(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Saffron|', 'Địa điểm:|Làng khởi đầu|'), ' ở Saffron|', ' ở làng|'), 'NPC:|Xatu|', 'NPC:|Chansey|'), 200) WHERE `id` = 1128 AND `mainsub` = 0;
-- 1129 C21.6: quai 55 chi moc o [13]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Saffron', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở Núi Mặt Trăng'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Saffron|', 'Địa điểm:|Núi Mặt Trăng|'), 200) WHERE `id` = 1129 AND `mainsub` = 0;
-- 1130 C22.1: NPC 2 chi o [0, 45, 70, 73, 80, 83, 103]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Saffron', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Saffron|', 'Địa điểm:|Làng khởi đầu|'), ' ở Saffron|', ' ở làng|'), 200) WHERE `id` = 1130 AND `mainsub` = 0;
-- 1132 C22.3: quai 40 chi moc o [13]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Saffron', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở Núi Mặt Trăng'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Saffron|', 'Địa điểm:|Núi Mặt Trăng|'), 200) WHERE `id` = 1132 AND `mainsub` = 0;
-- 1133 C22.4: quai 71 chi moc o [14]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Saffron', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở Vùng Safari'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Saffron|', 'Địa điểm:|Vùng Safari|'), 200) WHERE `id` = 1133 AND `mainsub` = 0;
-- 1134 C22.5: quai 40 chi moc o [13]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Saffron', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở Núi Mặt Trăng'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Saffron|', 'Địa điểm:|Núi Mặt Trăng|'), 200) WHERE `id` = 1134 AND `mainsub` = 0;
-- 1135 C22.6: NPC 2 chi o [0, 45, 70, 73, 80, 83, 103]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Saffron', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Saffron|', 'Địa điểm:|Làng khởi đầu|'), ' ở Saffron|', ' ở làng|'), 200) WHERE `id` = 1135 AND `mainsub` = 0;
-- 1136 C22.7: quai 68 chi moc o [14]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Saffron|', 'Địa điểm:|Vùng Safari|'), 200) WHERE `id` = 1136 AND `mainsub` = 0;
-- 1137 C23.1: NPC 22 chi o [0, 47, 49, 56, 62, 70, 80]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Vùng Safari', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 14), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Vùng Safari|', 'Địa điểm:|Làng khởi đầu|'), ' ở Vùng Safari|', ' ở làng|'), 200) WHERE `id` = 1137 AND `mainsub` = 0;
-- 1141 C23.5: quai 45 chi moc o [13]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Vùng Safari', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 14), ' ở Núi Mặt Trăng'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Vùng Safari|', 'Địa điểm:|Núi Mặt Trăng|'), 200) WHERE `id` = 1141 AND `mainsub` = 0;
-- 1142 C23.6: NPC 22 chi o [0, 47, 49, 56, 62, 70, 80]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Vùng Safari', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 14), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Vùng Safari|', 'Địa điểm:|Làng khởi đầu|'), ' ở Vùng Safari|', ' ở làng|'), 200) WHERE `id` = 1142 AND `mainsub` = 0;
-- 1143 C24.1: NPC 21 chi o [0, 44, 50, 51, 52, 62, 70, 80, 163, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Fuchsia', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Fuchsia|', 'Địa điểm:|Làng khởi đầu|'), ' ở Fuchsia|', ' ở làng|'), 200) WHERE `id` = 1143 AND `mainsub` = 0;
-- 1144 C24.2: quai 71 chi moc o [14]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Fuchsia', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở Vùng Safari'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Fuchsia|', 'Địa điểm:|Vùng Safari|'), 200) WHERE `id` = 1144 AND `mainsub` = 0;
-- 1145 C24.3: quai 71 chi moc o [14]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Fuchsia', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở Vùng Safari'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Fuchsia|', 'Địa điểm:|Vùng Safari|'), 200) WHERE `id` = 1145 AND `mainsub` = 0;
-- 1148 C24.6: quai 50 chi moc o [15]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Fuchsia|', 'Địa điểm:|Cherrygrove|'), 200) WHERE `id` = 1148 AND `mainsub` = 0;
-- 1149 C25.1: NPC 9 chi o [0, 70, 78, 80, 88, 108]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cinnabar', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Cinnabar|', 'Địa điểm:|Làng khởi đầu|'), ' ở Cinnabar|', ' ở làng|'), 200) WHERE `id` = 1149 AND `mainsub` = 0;
-- 1150 C25.2: NPC 10 chi o [43, 58, 60, 106, 162]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cinnabar', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Bến tàu'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Cinnabar|', 'Địa điểm:|Bến tàu|'), ' ở Cinnabar|', ' ở Bến tàu|'), 200) WHERE `id` = 1150 AND `mainsub` = 0;
-- 1151 C25.3: quai 42 chi moc o [15]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Bến tàu', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở Cherrygrove'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Bến tàu|', 'Địa điểm:|Cherrygrove|'), 200) WHERE `id` = 1151 AND `mainsub` = 0;
-- 1152 C25.4: quai 68 chi moc o [14]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cinnabar', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Vùng Safari'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cinnabar|', 'Địa điểm:|Vùng Safari|'), 200) WHERE `id` = 1152 AND `mainsub` = 0;
-- 1155 C26.1: NPC 3 chi o [0, 47, 55, 59, 70, 74, 80, 84, 104]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cinnabar', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Cinnabar|', 'Địa điểm:|Làng khởi đầu|'), ' ở Cinnabar|', ' ở làng|'), 200) WHERE `id` = 1155 AND `mainsub` = 0;
-- 1156 C26.2: quai 42 chi moc o [15]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cinnabar', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Cherrygrove'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cinnabar|', 'Địa điểm:|Cherrygrove|'), 200) WHERE `id` = 1156 AND `mainsub` = 0;
-- 1157 C26.3: quai 57 chi moc o [15]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cinnabar', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Cherrygrove'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cinnabar|', 'Địa điểm:|Cherrygrove|'), 200) WHERE `id` = 1157 AND `mainsub` = 0;
-- 1158 C26.4: quai 57 chi moc o [15]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cinnabar', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Cherrygrove'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cinnabar|', 'Địa điểm:|Cherrygrove|'), 200) WHERE `id` = 1158 AND `mainsub` = 0;
-- 1160 C26.6: NPC 3 chi o [0, 47, 55, 59, 70, 74, 80, 84, 104]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cinnabar', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Cinnabar|', 'Địa điểm:|Làng khởi đầu|'), ' ở Cinnabar|', ' ở làng|'), 200) WHERE `id` = 1160 AND `mainsub` = 0;
-- 1161 C27.1: NPC 21 chi o [0, 44, 50, 51, 52, 62, 70, 80, 163, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cinnabar', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Cinnabar|', 'Địa điểm:|Làng khởi đầu|'), ' ở Cinnabar|', ' ở làng|'), 200) WHERE `id` = 1161 AND `mainsub` = 0;
-- 1165 C27.5: quai 59 chi moc o [113]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cinnabar', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Hang Tối'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cinnabar|', 'Địa điểm:|Hang Tối|'), 200) WHERE `id` = 1165 AND `mainsub` = 0;
-- 1166 C27.6: NPC 21 chi o [0, 44, 50, 51, 52, 62, 70, 80, 163, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cinnabar', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Cinnabar|', 'Địa điểm:|Làng khởi đầu|'), ' ở Cinnabar|', ' ở làng|'), 200) WHERE `id` = 1166 AND `mainsub` = 0;
-- 1167 C27.7: quai 47 chi moc o [113]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cinnabar|', 'Địa điểm:|Hang Tối|'), 200) WHERE `id` = 1167 AND `mainsub` = 0;
-- 1168 C28.1: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Viridian', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Viridian|', 'Địa điểm:|Làng khởi đầu|'), ' ở Viridian|', ' ở làng|'), 200) WHERE `id` = 1168 AND `mainsub` = 0;
-- 1169 C28.2: quai 41 chi moc o [114]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Viridian', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Hồ Phẫn Nộ'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Viridian|', 'Địa điểm:|Hồ Phẫn Nộ|'), 200) WHERE `id` = 1169 AND `mainsub` = 0;
-- 1170 C28.3: NPC 4 chi o [0, 70, 80, 201, 281]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Viridian', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Viridian|', 'Địa điểm:|Làng khởi đầu|'), ' ở Viridian|', ' ở làng|'), 200) WHERE `id` = 1170 AND `mainsub` = 0;
-- 1172 C28.5: quai 44 chi moc o [114]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Viridian|', 'Địa điểm:|Hồ Phẫn Nộ|'), 200) WHERE `id` = 1172 AND `mainsub` = 0;
-- 1173 C28.6: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Viridian', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Viridian|', 'Địa điểm:|Làng khởi đầu|'), ' ở Viridian|', ' ở làng|'), 200) WHERE `id` = 1173 AND `mainsub` = 0;
-- 1174 C29.1: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở New Bark', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|New Bark|', 'Địa điểm:|Làng khởi đầu|'), ' ở New Bark|', ' ở làng|'), 200) WHERE `id` = 1174 AND `mainsub` = 0;
-- 1175 C29.2: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở New Bark', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|New Bark|', 'Địa điểm:|Làng khởi đầu|'), ' ở New Bark|', ' ở làng|'), 200) WHERE `id` = 1175 AND `mainsub` = 0;
-- 1179 C29.6: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở New Bark', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|New Bark|', 'Địa điểm:|Làng khởi đầu|'), ' ở New Bark|', ' ở làng|'), 200) WHERE `id` = 1179 AND `mainsub` = 0;
-- 1180 C30.1: NPC 6 chi o [0, 53, 70, 79, 80, 89, 109]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cherrygrove', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 14), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Cherrygrove|', 'Địa điểm:|Làng khởi đầu|'), ' ở Cherrygrove|', ' ở làng|'), 200) WHERE `id` = 1180 AND `mainsub` = 0;
-- 1183 C30.4: quai 62 chi moc o [114]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cherrygrove', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 14), ' ở Hồ Phẫn Nộ'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cherrygrove|', 'Địa điểm:|Hồ Phẫn Nộ|'), 200) WHERE `id` = 1183 AND `mainsub` = 0;
-- 1184 C30.5: NPC 6 chi o [0, 53, 70, 79, 80, 89, 109]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cherrygrove', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 14), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Cherrygrove|', 'Địa điểm:|Làng khởi đầu|'), ' ở Cherrygrove|', ' ở làng|'), 200) WHERE `id` = 1184 AND `mainsub` = 0;
-- 1185 C30.6: quai 44 chi moc o [114]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cherrygrove', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 14), ' ở Hồ Phẫn Nộ'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cherrygrove|', 'Địa điểm:|Hồ Phẫn Nộ|'), 200) WHERE `id` = 1185 AND `mainsub` = 0;
-- 1186 C31.1: NPC 8 chi o [0, 51, 57, 60, 70, 80, 105, 160]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Violet', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Violet|', 'Địa điểm:|Làng khởi đầu|'), ' ở Violet|', ' ở làng|'), 200) WHERE `id` = 1186 AND `mainsub` = 0;
-- 1188 C31.3: quai 76 chi moc o [19]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Violet', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở Ecruteak'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Violet|', 'Địa điểm:|Ecruteak|'), 200) WHERE `id` = 1188 AND `mainsub` = 0;
-- 1190 C31.5: NPC 21 chi o [0, 44, 50, 51, 52, 62, 70, 80, 163, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Violet', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Violet|', 'Địa điểm:|Làng khởi đầu|'), ' ở Violet|', ' ở làng|'), 200) WHERE `id` = 1190 AND `mainsub` = 0;
-- 1191 C31.6: quai 65 chi moc o [19]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Violet|', 'Địa điểm:|Ecruteak|'), 200) WHERE `id` = 1191 AND `mainsub` = 0;
-- 1192 C32.1: NPC 21 chi o [0, 44, 50, 51, 52, 62, 70, 80, 163, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Violet', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Violet|', 'Địa điểm:|Làng khởi đầu|'), ' ở Violet|', ' ở làng|'), 200) WHERE `id` = 1192 AND `mainsub` = 0;
-- 1194 C32.3: quai 44 chi moc o [114]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Violet', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở Hồ Phẫn Nộ'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Violet|', 'Địa điểm:|Hồ Phẫn Nộ|'), 200) WHERE `id` = 1194 AND `mainsub` = 0;
-- 1197 C32.6: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Violet', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Violet|', 'Địa điểm:|Làng khởi đầu|'), ' ở Violet|', ' ở làng|'), 200) WHERE `id` = 1197 AND `mainsub` = 0;
-- 1198 C33.1: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Azalea', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Azalea|', 'Địa điểm:|Làng khởi đầu|'), ' ở Azalea|', ' ở làng|'), 200) WHERE `id` = 1198 AND `mainsub` = 0;
-- 1200 C33.3: quai 76 chi moc o [19]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Azalea', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở Ecruteak'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Azalea|', 'Địa điểm:|Ecruteak|'), 200) WHERE `id` = 1200 AND `mainsub` = 0;
-- 1201 C33.4: quai 65 chi moc o [19]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Azalea', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở Ecruteak'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Azalea|', 'Địa điểm:|Ecruteak|'), 200) WHERE `id` = 1201 AND `mainsub` = 0;
-- 1203 C33.6: NPC 25 chi o [201]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Azalea', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 9), ' ở Cao nguyên Indigo'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Azalea|', 'Địa điểm:|Cao nguyên Indigo|'), ' ở Azalea|', ' ở Cao nguyên Indigo|'), 200) WHERE `id` = 1203 AND `mainsub` = 0;
-- 1204 C33.7: quai 48 chi moc o [20]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Azalea|', 'Địa điểm:|Violet|'), 200) WHERE `id` = 1204 AND `mainsub` = 0;
-- 1205 C34.1: NPC 23 chi o [4, 51]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Rừng Ilex', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở Cerulean'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Rừng Ilex|', 'Địa điểm:|Cerulean|'), ' ở Rừng Ilex|', ' ở Cerulean|'), 200) WHERE `id` = 1205 AND `mainsub` = 0;
-- 1206 C34.2: quai 65 chi moc o [19]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Rừng Ilex', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở Ecruteak'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Rừng Ilex|', 'Địa điểm:|Ecruteak|'), 200) WHERE `id` = 1206 AND `mainsub` = 0;
-- 1207 C34.3: quai 48 chi moc o [20]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Rừng Ilex', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở Violet'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Rừng Ilex|', 'Địa điểm:|Violet|'), 200) WHERE `id` = 1207 AND `mainsub` = 0;
-- 1209 C34.5: quai 65 chi moc o [19]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Rừng Ilex', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở Ecruteak'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Rừng Ilex|', 'Địa điểm:|Ecruteak|'), 200) WHERE `id` = 1209 AND `mainsub` = 0;
-- 1210 C34.6: NPC 23 chi o [4, 51]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Rừng Ilex', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở Cerulean'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Rừng Ilex|', 'Địa điểm:|Cerulean|'), ' ở Rừng Ilex|', ' ở Cerulean|'), 200) WHERE `id` = 1210 AND `mainsub` = 0;
-- 1211 C35.1: NPC 8 chi o [0, 51, 57, 60, 70, 80, 105, 160]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Rừng Ilex', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Rừng Ilex|', 'Địa điểm:|Làng khởi đầu|'), ' ở Rừng Ilex|', ' ở làng|'), 200) WHERE `id` = 1211 AND `mainsub` = 0;
-- 1212 C35.2: quai 65 chi moc o [19]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Rừng Ilex', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở Ecruteak'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Rừng Ilex|', 'Địa điểm:|Ecruteak|'), 200) WHERE `id` = 1212 AND `mainsub` = 0;
-- 1215 C35.5: NPC 8 chi o [0, 51, 57, 60, 70, 80, 105, 160]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Rừng Ilex', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Rừng Ilex|', 'Địa điểm:|Làng khởi đầu|'), ' ở Rừng Ilex|', ' ở làng|'), 200) WHERE `id` = 1215 AND `mainsub` = 0;
-- 1216 C35.6: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Rừng Ilex', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Rừng Ilex|', 'Địa điểm:|Làng khởi đầu|'), ' ở Rừng Ilex|', ' ở làng|'), 200) WHERE `id` = 1216 AND `mainsub` = 0;
-- 1217 C36.1: NPC 22 chi o [0, 47, 49, 56, 62, 70, 80]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Goldenrod', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Goldenrod|', 'Địa điểm:|Làng khởi đầu|'), ' ở Goldenrod|', ' ở làng|'), 200) WHERE `id` = 1217 AND `mainsub` = 0;
-- 1220 C36.4: quai 48 chi moc o [20]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Goldenrod', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở Violet'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Goldenrod|', 'Địa điểm:|Violet|'), 200) WHERE `id` = 1220 AND `mainsub` = 0;
-- 1221 C36.5: NPC 16 chi o [94, 201, 271]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Goldenrod', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở Cửa hàng 5'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Goldenrod|', 'Địa điểm:|Cửa hàng 5|'), ' ở Goldenrod|', ' ở Cửa hàng 5|'), 200) WHERE `id` = 1221 AND `mainsub` = 0;
-- 1222 C36.6: quai 77 chi moc o [21]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Goldenrod|', 'Địa điểm:|Đồng cỏ phía tây|'), 200) WHERE `id` = 1222 AND `mainsub` = 0;
-- 1223 C36.7: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Goldenrod', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Goldenrod|', 'Địa điểm:|Làng khởi đầu|'), ' ở Goldenrod|', ' ở làng|'), 200) WHERE `id` = 1223 AND `mainsub` = 0;
-- 1224 C37.1: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Ecruteak', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Ecruteak|', 'Địa điểm:|Làng khởi đầu|'), ' ở Ecruteak|', ' ở làng|'), 200) WHERE `id` = 1224 AND `mainsub` = 0;
-- 1225 C37.2: quai 48 chi moc o [20]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Ecruteak|', 'Địa điểm:|Violet|'), 200) WHERE `id` = 1225 AND `mainsub` = 0;
-- 1228 C37.5: quai 60 chi moc o [20]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Ecruteak', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Violet'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Ecruteak|', 'Địa điểm:|Violet|'), 200) WHERE `id` = 1228 AND `mainsub` = 0;
-- 1229 C37.6: quai 81 chi moc o [21]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Ecruteak|', 'Địa điểm:|Đồng cỏ phía tây|'), 200) WHERE `id` = 1229 AND `mainsub` = 0;
-- 1230 C38.1: NPC 8 chi o [0, 51, 57, 60, 70, 80, 105, 160]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Ecruteak', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Ecruteak|', 'Địa điểm:|Làng khởi đầu|'), ' ở Ecruteak|', ' ở làng|'), 200) WHERE `id` = 1230 AND `mainsub` = 0;
-- 1232 C38.3: quai 60 chi moc o [20]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Ecruteak', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Violet'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Ecruteak|', 'Địa điểm:|Violet|'), 200) WHERE `id` = 1232 AND `mainsub` = 0;
-- 1233 C38.4: quai 77 chi moc o [21]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Ecruteak', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Đồng cỏ phía tây'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Ecruteak|', 'Địa điểm:|Đồng cỏ phía tây|'), 200) WHERE `id` = 1233 AND `mainsub` = 0;
-- 1235 C38.6: NPC 8 chi o [0, 51, 57, 60, 70, 80, 105, 160]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Ecruteak', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Ecruteak|', 'Địa điểm:|Làng khởi đầu|'), ' ở Ecruteak|', ' ở làng|'), 200) WHERE `id` = 1235 AND `mainsub` = 0;
-- 1236 C39.1: NPC 2 chi o [0, 45, 70, 73, 80, 83, 103]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Olivine', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Olivine|', 'Địa điểm:|Làng khởi đầu|'), ' ở Olivine|', ' ở làng|'), 200) WHERE `id` = 1236 AND `mainsub` = 0;
-- 1239 C39.4: quai 81 chi moc o [21]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Olivine', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 10), ' ở Đồng cỏ phía tây'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Olivine|', 'Địa điểm:|Đồng cỏ phía tây|'), 200) WHERE `id` = 1239 AND `mainsub` = 0;
-- 1241 C39.6: quai 53 chi moc o [22]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Olivine|', 'Địa điểm:|Tàu S.S. Anne|'), 200) WHERE `id` = 1241 AND `mainsub` = 0;
-- 1244 C40.3: quai 81 chi moc o [21]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cianwood', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Đồng cỏ phía tây'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cianwood|', 'Địa điểm:|Đồng cỏ phía tây|'), 200) WHERE `id` = 1244 AND `mainsub` = 0;
-- 1245 C40.4: quai 51 chi moc o [23]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cianwood|', 'Địa điểm:|Azalea|'), 200) WHERE `id` = 1245 AND `mainsub` = 0;
-- 1248 C41.1: NPC 6 chi o [0, 53, 70, 79, 80, 89, 109]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Mahogany', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Mahogany|', 'Địa điểm:|Làng khởi đầu|'), ' ở Mahogany|', ' ở làng|'), 200) WHERE `id` = 1248 AND `mainsub` = 0;
-- 1249 C41.2: NPC 1 chi o [0, 45, 47, 49, 54, 56, 62, 70, 72, 80, 82, 102]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Mahogany', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Mahogany|', 'Địa điểm:|Làng khởi đầu|'), ' ở Mahogany|', ' ở làng|'), 200) WHERE `id` = 1249 AND `mainsub` = 0;
-- 1251 C41.4: quai 54 chi moc o [22]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Mahogany', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Tàu S.S. Anne'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Mahogany|', 'Địa điểm:|Tàu S.S. Anne|'), 200) WHERE `id` = 1251 AND `mainsub` = 0;
-- 1252 C41.5: quai 54 chi moc o [22]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Mahogany', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Tàu S.S. Anne'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Mahogany|', 'Địa điểm:|Tàu S.S. Anne|'), 200) WHERE `id` = 1252 AND `mainsub` = 0;
-- 1253 C41.6: NPC 6 chi o [0, 53, 70, 79, 80, 89, 109]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Mahogany', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Mahogany|', 'Địa điểm:|Làng khởi đầu|'), ' ở Mahogany|', ' ở làng|'), 200) WHERE `id` = 1253 AND `mainsub` = 0;
-- 1254 C42.1: NPC 21 chi o [0, 44, 50, 51, 52, 62, 70, 80, 163, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Hồ Phẫn Nộ', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 13), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Hồ Phẫn Nộ|', 'Địa điểm:|Làng khởi đầu|'), ' ở Hồ Phẫn Nộ|', ' ở làng|'), 200) WHERE `id` = 1254 AND `mainsub` = 0;
-- 1256 C42.3: quai 49 chi moc o [23]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Hồ Phẫn Nộ', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 13), ' ở Azalea'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Hồ Phẫn Nộ|', 'Địa điểm:|Azalea|'), 200) WHERE `id` = 1256 AND `mainsub` = 0;
-- 1257 C42.4: quai 49 chi moc o [23]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Hồ Phẫn Nộ', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 13), ' ở Azalea'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Hồ Phẫn Nộ|', 'Địa điểm:|Azalea|'), 200) WHERE `id` = 1257 AND `mainsub` = 0;
-- 1258 C42.5: quai 53 chi moc o [22]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Hồ Phẫn Nộ', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 13), ' ở Tàu S.S. Anne'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Hồ Phẫn Nộ|', 'Địa điểm:|Tàu S.S. Anne|'), 200) WHERE `id` = 1258 AND `mainsub` = 0;
-- 1259 C42.6: NPC 21 chi o [0, 44, 50, 51, 52, 62, 70, 80, 163, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Hồ Phẫn Nộ', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 13), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Hồ Phẫn Nộ|', 'Địa điểm:|Làng khởi đầu|'), ' ở Hồ Phẫn Nộ|', ' ở làng|'), 200) WHERE `id` = 1259 AND `mainsub` = 0;
-- 1260 C43.1: NPC 9 chi o [0, 70, 78, 80, 88, 108]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Hang Tối', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Hang Tối|', 'Địa điểm:|Làng khởi đầu|'), ' ở Hang Tối|', ' ở làng|'), 200) WHERE `id` = 1260 AND `mainsub` = 0;
-- 1261 C43.2: quai 49 chi moc o [23]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Hang Tối', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Azalea'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Hang Tối|', 'Địa điểm:|Azalea|'), 200) WHERE `id` = 1261 AND `mainsub` = 0;
-- 1263 C43.4: quai 56 chi moc o [24]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Hang Tối', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Olivine'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Hang Tối|', 'Địa điểm:|Olivine|'), 200) WHERE `id` = 1263 AND `mainsub` = 0;
-- 1265 C43.6: quai 51 chi moc o [23]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Hang Tối', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Azalea'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Hang Tối|', 'Địa điểm:|Azalea|'), 200) WHERE `id` = 1265 AND `mainsub` = 0;
-- 1266 C44.1: NPC 3 chi o [0, 47, 55, 59, 70, 74, 80, 84, 104]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Mahogany', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Mahogany|', 'Địa điểm:|Làng khởi đầu|'), ' ở Mahogany|', ' ở làng|'), 200) WHERE `id` = 1266 AND `mainsub` = 0;
-- 1267 C44.2: quai 51 chi moc o [23]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Mahogany', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Azalea'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Mahogany|', 'Địa điểm:|Azalea|'), 200) WHERE `id` = 1267 AND `mainsub` = 0;
-- 1269 C44.4: quai 58 chi moc o [24]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Mahogany', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Olivine'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Mahogany|', 'Địa điểm:|Olivine|'), 200) WHERE `id` = 1269 AND `mainsub` = 0;
-- 1270 C44.5: quai 56 chi moc o [24]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Mahogany', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 11), ' ở Olivine'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Mahogany|', 'Địa điểm:|Olivine|'), 200) WHERE `id` = 1270 AND `mainsub` = 0;
-- 1271 C44.6: quai 118 chi moc o [26]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Mahogany|', 'Địa điểm:|Rừng Cổ|'), 200) WHERE `id` = 1271 AND `mainsub` = 0;
-- 1275 C45.4: quai 51 chi moc o [23]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Blackthorn', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 13), ' ở Azalea'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Blackthorn|', 'Địa điểm:|Azalea|'), 200) WHERE `id` = 1275 AND `mainsub` = 0;
-- 1278 C46.1: NPC 21 chi o [0, 44, 50, 51, 52, 62, 70, 80, 163, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Hang Salamence', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 17), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Hang Salamence|', 'Địa điểm:|Làng khởi đầu|'), ' ở Hang Salamence|', ' ở làng|'), 200) WHERE `id` = 1278 AND `mainsub` = 0;
-- 1280 C46.3: quai 118 chi moc o [26]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Hang Salamence', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 17), ' ở Rừng Cổ'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Hang Salamence|', 'Địa điểm:|Rừng Cổ|'), 200) WHERE `id` = 1280 AND `mainsub` = 0;
-- 1282 C46.5: quai 118 chi moc o [26]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Hang Salamence', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 17), ' ở Rừng Cổ'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Hang Salamence|', 'Địa điểm:|Rừng Cổ|'), 200) WHERE `id` = 1282 AND `mainsub` = 0;
-- 1283 C46.6: NPC 21 chi o [0, 44, 50, 51, 52, 62, 70, 80, 163, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Hang Salamence', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 17), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Hang Salamence|', 'Địa điểm:|Làng khởi đầu|'), ' ở Hang Salamence|', ' ở làng|'), 200) WHERE `id` = 1283 AND `mainsub` = 0;
-- 1284 C47.1: NPC 8 chi o [0, 51, 57, 60, 70, 80, 105, 160]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Tổ Zapdos', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Tổ Zapdos|', 'Địa điểm:|Làng khởi đầu|'), ' ở Tổ Zapdos|', ' ở làng|'), 200) WHERE `id` = 1284 AND `mainsub` = 0;
-- 1287 C47.4: quai 118 chi moc o [26]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Tổ Zapdos', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở Rừng Cổ'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Tổ Zapdos|', 'Địa điểm:|Rừng Cổ|'), 200) WHERE `id` = 1287 AND `mainsub` = 0;
-- 1289 C47.6: NPC 8 chi o [0, 51, 57, 60, 70, 80, 105, 160]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Tổ Zapdos', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 12), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Tổ Zapdos|', 'Địa điểm:|Làng khởi đầu|'), ' ở Tổ Zapdos|', ' ở làng|'), 200) WHERE `id` = 1289 AND `mainsub` = 0;
-- 1293 C48.4: quai 136 chi moc o [28]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Blackthorn', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 13), ' ở Mahogany'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Blackthorn|', 'Địa điểm:|Mahogany|'), 200) WHERE `id` = 1293 AND `mainsub` = 0;
-- 1295 C48.6: quai 138 chi moc o [28]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Blackthorn|', 'Địa điểm:|Mahogany|'), 200) WHERE `id` = 1295 AND `mainsub` = 0;
-- 1296 C49.1: NPC 21 chi o [0, 44, 50, 51, 52, 62, 70, 80, 163, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Đường Victory', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Đường Victory|', 'Địa điểm:|Làng khởi đầu|'), ' ở Đường Victory|', ' ở làng|'), 200) WHERE `id` = 1296 AND `mainsub` = 0;
UPDATE `data_quest_lau_dai` SET `npc_giao` = 3, `ten_npc` = 'Bronzong' WHERE `id_so` = 1297;
-- 1297 C49.2: loi dau cua Bronzong, giao boi Blaziken
UPDATE `data_quest_moi` SET `npcrcv` = 3, `infogift` = LEFT(REPLACE(`infogift`, 'NPC:|Blaziken|', 'NPC:|Bronzong|'), 200) WHERE `id` = 1297 AND `mainsub` = 0;
-- 1300 C49.5: quai 138 chi moc o [28]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Đường Victory', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 16), ' ở Mahogany'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Đường Victory|', 'Địa điểm:|Mahogany|'), 200) WHERE `id` = 1300 AND `mainsub` = 0;
-- 1302 C50.1: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cao nguyên Indigo', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 20), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Cao nguyên Indigo|', 'Địa điểm:|Làng khởi đầu|'), ' ở Cao nguyên Indigo|', ' ở làng|'), 200) WHERE `id` = 1302 AND `mainsub` = 0;
-- 1304 C50.3: quai 138 chi moc o [28]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cao nguyên Indigo', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 20), ' ở Mahogany'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cao nguyên Indigo|', 'Địa điểm:|Mahogany|'), 200) WHERE `id` = 1304 AND `mainsub` = 0;
-- 1305 C50.4: quai 138 chi moc o [28]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cao nguyên Indigo', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 20), ' ở Mahogany'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cao nguyên Indigo|', 'Địa điểm:|Mahogany|'), 200) WHERE `id` = 1305 AND `mainsub` = 0;
-- 1306 C50.5: quai 137 chi moc o [28]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cao nguyên Indigo', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 20), ' ở Mahogany'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cao nguyên Indigo|', 'Địa điểm:|Mahogany|'), 200) WHERE `id` = 1306 AND `mainsub` = 0;
-- 1307 C50.6: quai 136 chi moc o [28]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Cao nguyên Indigo', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 20), ' ở Mahogany'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Cao nguyên Indigo|', 'Địa điểm:|Mahogany|'), 200) WHERE `id` = 1307 AND `mainsub` = 0;
-- 1309 C51.1: NPC 8 chi o [0, 51, 57, 60, 70, 80, 105, 160]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Núi Silver', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 13), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Núi Silver|', 'Địa điểm:|Làng khởi đầu|'), ' ở Núi Silver|', ' ở làng|'), 200) WHERE `id` = 1309 AND `mainsub` = 0;
-- 1311 C51.3: quai 138 chi moc o [28]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Núi Silver', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 13), ' ở Mahogany'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Núi Silver|', 'Địa điểm:|Mahogany|'), 200) WHERE `id` = 1311 AND `mainsub` = 0;
-- 1314 C51.6: quai 138 chi moc o [28]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Núi Silver', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 13), ' ở Mahogany'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Núi Silver|', 'Địa điểm:|Mahogany|'), 200) WHERE `id` = 1314 AND `mainsub` = 0;
-- 1316 C52.1: NPC 21 chi o [0, 44, 50, 51, 52, 62, 70, 80, 163, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Núi Silver', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 13), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Núi Silver|', 'Địa điểm:|Làng khởi đầu|'), ' ở Núi Silver|', ' ở làng|'), 200) WHERE `id` = 1316 AND `mainsub` = 0;
-- 1317 C52.2: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Núi Silver', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 13), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Núi Silver|', 'Địa điểm:|Làng khởi đầu|'), ' ở Núi Silver|', ' ở làng|'), 200) WHERE `id` = 1317 AND `mainsub` = 0;
-- 1319 C52.4: quai 136 chi moc o [28]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Núi Silver', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 13), ' ở Mahogany'), `scontent`), `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Núi Silver|', 'Địa điểm:|Mahogany|'), 200) WHERE `id` = 1319 AND `mainsub` = 0;
-- 1320 C52.5: quai 138 chi moc o [28]
UPDATE `data_quest_moi` SET `infogift` = LEFT(REPLACE(`infogift`, 'Địa điểm:|Núi Silver|', 'Địa điểm:|Mahogany|'), 200) WHERE `id` = 1320 AND `mainsub` = 0;
-- 1322 C52.7: NPC 5 chi o [0, 43, 45, 50, 56, 59, 60, 70, 80, 101, 163, 164, 280, 282]
UPDATE `data_quest_moi` SET `scontent` = IF(`scontent` LIKE '% ở Núi Silver', CONCAT(LEFT(`scontent`, CHAR_LENGTH(`scontent`) - 13), ' ở làng'), `scontent`), `infogift` = LEFT(REPLACE(REPLACE(`infogift`, 'Địa điểm:|Núi Silver|', 'Địa điểm:|Làng khởi đầu|'), ' ở Núi Silver|', ' ở làng|'), 200) WHERE `id` = 1322 AND `mainsub` = 0;
-- /SUA CHUOI CHINH ---------------------------------------------------------------------------------------------------
