-- ============================================================================
-- Bang `web_lenh_nhan_vat` - hang doi lenh tu trang quan tri web cho server game.
--
-- KHONG BAT BUOC CHAY TAY: ca server game (src/data/LenhWeb.java) lan trang web
-- (web/admin/inc/lenh_nhan_vat.php) deu tu CREATE TABLE IF NOT EXISTS. File nay co de:
--   1. bo kiem web/admin/test/kiem_schema.php doi chieu duoc ten cot,
--   2. ai muon co bang truoc khi khoi dong lai server thi chay tay duoc.
--
--     mysql -u root kpah_db < sql/them_bang_web_lenh_nhan_vat.sql
--
-- VI SAO CAN (02/09/2026): "Dat cap do" / "Buff level" tren web ghi thang lastLv/xp
-- vao tob_char. Cach do chua bao gio co tac dung: nhan vat online bi luu de tu RAM, con
-- luc dang nhap server doc cap/exp tu cot pInfo chu KHONG doc lastLv/xp (hai cot do chi
-- cho bang xep hang / man chon nhan vat). Web khong nhin thay RAM server, nen doi cach:
-- web chi CHEN MOT DONG LENH vao bang nay; server doc moi giay va tu ap dung bang dung
-- duong len cap tu nhien (Map.addXPForChar, nhu nut Buff EXP cua AdminPanel) roi luu ngay.
-- Nhan vat offline thi dong lenh nam cho, server cong ngay khi thay nhan vat vao game.
--
-- trang_thai: 0 = cho server;  1 = da cong cho nhan vat dang online (da luu);
--             2 = (ma cu) da ghi CSDL khi offline - khong con tao moi;
--             3 = loi (xem ket_qua, vd "chi ho tro NANG cap");
--             4 = nhan vat offline, cho vao game;  5 = nguoi quan tri huy.
-- phan_nghin: % vao cap nhan 10 (0..999), cung don vi voi LevelDetail.percent.
-- Server dang tat thi dong lenh nam cho, luc khoi dong lai se xu ly - khong mat lenh.
-- ============================================================================

CREATE TABLE IF NOT EXISTS `web_lenh_nhan_vat` (
  `id`         bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `loai`       varchar(30)  NOT NULL                          COMMENT 'dat_cap_do',
  `char_id`    int(11)      NOT NULL                          COMMENT 'tob_char.id',
  `charname`   varchar(50)  NOT NULL DEFAULT ''               COMMENT 'de doc log, khong dung de tim',
  `cap_do`     int(11)      NOT NULL DEFAULT 0,
  `phan_nghin` int(11)      NOT NULL DEFAULT 0                COMMENT '% vao cap x10, 0..999',
  `admin_user` varchar(50)  NOT NULL DEFAULT ''               COMMENT 'team_user.username nguoi bam',
  `trang_thai` tinyint(4)   NOT NULL DEFAULT 0                COMMENT '0 cho, 1 online da cong, 2 (cu) da ghi CSDL, 3 loi, 4 cho vao game, 5 huy',
  `ket_qua`    varchar(255) NOT NULL DEFAULT ''               COMMENT 'server ghi: lv cu -> lv moi, hoac ly do loi',
  `tao_luc`    datetime     NOT NULL DEFAULT current_timestamp(),
  `xu_ly_luc`  datetime     NULL,
  PRIMARY KEY (`id`),
  KEY `idx_trang_thai` (`trang_thai`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Kiem tra lai: phai ra 1.
SELECT COUNT(*) AS co_bang FROM information_schema.tables
 WHERE table_schema = 'kpah_db' AND table_name = 'web_lenh_nhan_vat';
