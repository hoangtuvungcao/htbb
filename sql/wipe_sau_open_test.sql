-- ============================================================================
--  WIPE DU LIEU NGUOI CHOI SAU OPEN TEST (Huyen Thoai Bao Boi) - kpah_db
--  San sang production 17/10/2026 - xem docs/pokemon/SAN_SANG_PRODUCTION.md muc 7.
--
--  VI SAO KHONG DUNG sql/wipe_player_data.sql NUA:
--    File cu giu DANH SACH CO DINH 16 bang roi TRUNCATE MOI BANG CON LAI. Tu khi chuyen sang
--    Pokemon co them hang chuc bang CAU HINH / NOI DUNG khong nam trong danh sach do:
--    cau_hinh (ti le drop...), tob_su_kien (bat/tat su kien), web_* (tin tuc, trang, FAQ, vai tro
--    admin, 2FA), data_*_lau_dai / data_noi_dung_* (nhiem vu, Gym, boss), tob_cam_may (khoa may)...
--    Chay file cu la xoa sach cau hinh + noi dung web + toan bo nhiem vu giai doan 2.
--
--  CACH NAY: GIU moi bang data_% + danh sach cau hinh duoi day; chi sinh lenh TRUNCATE cho phan con
--  lai, de BAN DOC LAI truoc khi chay.
--
--  THU TU (KHONG HOAN TAC DUOC):
--    1. Thong bao truoc >= 7 ngay (mau o SAN_SANG_PRODUCTION.md muc 7).
--    2. sudo systemctl stop kpah-game kpah-login      (server giu nhan vat trong RAM, con chay la ghi de lai)
--    3. sudo /opt/kpah/vps/backup_db.sh               (+ cat rieng 1 ban: cp /opt/kpah/backup/kpah_db_<ngay>.sql.gz ~/truoc-wipe/)
--    4. mysql -N -r kpah_db < sql/wipe_sau_open_test.sql > lenh_wipe.sql   (CHI in lenh, khong xoa gi;
--       -r de xuong dong that thay vi chuoi \n)
--    5. Doc lai lenh_wipe.sql (~85 bang), roi chay: mysql kpah_db < lenh_wipe.sql
--    6. Tai khoan (CSDL account): mac dinh GIU - nguoi choi open test dung lai tai khoan.
--       Muon xoa ca tai khoan: xem cuoi file.
--    7. sudo systemctl start kpah-login kpah-game ; dang nhap thu bang tai khoan GM.
-- ============================================================================

SET SESSION group_concat_max_len = 1000000;

SELECT GROUP_CONCAT(CONCAT('TRUNCATE TABLE `', TABLE_NAME, '`;') ORDER BY TABLE_NAME SEPARATOR '\n')
         AS lenh_can_chay
  FROM information_schema.TABLES
 WHERE TABLE_SCHEMA = DATABASE()
   AND TABLE_TYPE   = 'BASE TABLE'
   -- moi bang du lieu game (data_item, data_monster, data_quest_lau_dai, data_noi_dung_*...)
   AND TABLE_NAME NOT LIKE 'data\_%'
   -- noi dung + quan tri web (tin tuc, trang, FAQ, media, vai tro, 2FA, nhat ky admin, khoa tai khoan)
   AND TABLE_NAME NOT LIKE 'web\_%'
   AND TABLE_NAME NOT IN (
        -- cau hinh / dieu khien server
        'cau_hinh','dieu_khien_server','tob_su_kien','tob_event','tob_x2exp','5h_systems','admin_msg',
        -- bang tu dien / template (khong phai du lieu nguoi choi du ten tob_)
        'bad_word','tob_fruit','tob_tubinh',
        -- dinh nghia ma qua (log nhan ma VAN bi xoa -> nhan vat moi nhap lai duoc)
        'giftcode','tob_giftcode','tob_code_lixi',
        -- kich hoat tai khoan: xoa = khong ai dang nhap duoc (LoginHandler check_active)
        '5h_active',
        -- ban sao bang tai khoan trong kpah_db (khong phai du lieu nhan vat)
        'team_user','team_user_1',
        -- khoa may cua ke gian lan open test: GIU (chu server co the bo dong nay neu muon xoa an)
        'tob_cam_may'
   );

-- SAU KHI CHAY KHOI LENH TREN:
--   UPDATE tob_code_lixi SET nhan=0, charnhan=NULL, timeNhan=NULL;   -- giu kho ma li xi, xoa dau vet nhan
--
-- TUY CHON - xoa ca tai khoan (nguoi choi phai dang ky lai; NEN TRANH, mat lien lac voi nguoi test):
--   TRUNCATE TABLE `5h_active`;   (kpah_db)
--   DELETE FROM account.team_user WHERE username NOT IN ('<tai khoan GM 1>','<tai khoan GM 2>');
