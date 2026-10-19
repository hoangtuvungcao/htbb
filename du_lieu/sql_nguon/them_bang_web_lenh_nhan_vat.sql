-- ============================================================================
-- Bang `web_lenh_nhan_vat` - hang doi lenh tu trang quan tri web cho server game.
--
-- KHONG BAT BUOC CHAY TAY: ca server game (src/data/LenhWeb.java) lan trang web
-- (HTBB-Web server/utils/lenhNhanVat.ts) deu tu CREATE TABLE IF NOT EXISTS (va tu them cot
-- `tham_so` neu bang duoc tao tu ban dau tien chua co cot nay). File nay co de:
--   1. xem duoc ten cot/cau truc ma khong phai doc ma nguon,
--   2. ai muon co bang truoc khi khoi dong lai server thi chay tay duoc.
--
--     mysql -u root htbb_db < du_lieu/sql_nguon/them_bang_web_lenh_nhan_vat.sql
--
-- VI SAO CAN (02/09/2026): "Dat cap do" / "Buff level" / "Xoa vat pham" tren web ghi
-- thang vao tob_char. Nhan vat online bi luu de tu RAM; rieng cap do con khong an ca khi
-- offline vi luc dang nhap server doc cap/exp tu cot pInfo chu KHONG doc lastLv/xp. Web
-- khong nhin thay RAM server, nen doi cach: web chi CHEN MOT DONG LENH vao bang nay;
-- server doc moi giay va tu ap dung trong RAM roi luu ngay.
--
-- loai:
--   dat_cap_do   : cap_do + phan_nghin. Online -> Map.addXPForChar (duong len cap tu
--                  nhien). Offline -> cho vao game (4). Chi NANG cap.
--   xoa_vat_pham : tham_so = "<cot>><mau>" (cot equip/inven/bag; mau = chuoi tho cua
--                  mon do). Online -> go dung mon khop trong RAM. Offline -> server xoa
--                  mau khoi cot trong CSDL roi de "cho vao game" (4) de doi chieu lai
--                  RAM khi nhan vat dang nhap.
-- trang_thai: 0 = cho server;  1 = da thuc hien cho nhan vat dang online (da luu);
--             2 = (ma cu) da ghi CSDL khi offline - khong con tao moi;
--             3 = loi (xem ket_qua, vd "chi ho tro NANG cap");
--             4 = nhan vat offline, cho vao game;  5 = nguoi quan tri huy.
-- phan_nghin: % vao cap nhan 10 (0..999), cung don vi voi LevelDetail.percent.
-- Server dang tat thi dong lenh nam cho, luc khoi dong lai se xu ly - khong mat lenh.
-- ============================================================================

CREATE TABLE IF NOT EXISTS `web_lenh_nhan_vat` (
  `id`         bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `loai`       varchar(30)  NOT NULL                          COMMENT 'dat_cap_do | xoa_vat_pham',
  `char_id`    int(11)      NOT NULL                          COMMENT 'tob_char.id',
  `charname`   varchar(50)  NOT NULL DEFAULT ''               COMMENT 'de doc log, khong dung de tim',
  `cap_do`     int(11)      NOT NULL DEFAULT 0,
  `phan_nghin` int(11)      NOT NULL DEFAULT 0                COMMENT '% vao cap x10, 0..999',
  `tham_so`    text         NULL                              COMMENT 'xoa_vat_pham: "<cot>><mau>"',
  `admin_user` varchar(50)  NOT NULL DEFAULT ''               COMMENT 'team_user.username nguoi bam',
  `trang_thai` tinyint(4)   NOT NULL DEFAULT 0                COMMENT '0 cho, 1 online da xong, 2 (cu) da ghi CSDL, 3 loi, 4 cho vao game, 5 huy',
  `ket_qua`    varchar(255) NOT NULL DEFAULT ''               COMMENT 'server ghi: viec da lam, hoac ly do loi',
  `tao_luc`    datetime     NOT NULL DEFAULT current_timestamp(),
  `xu_ly_luc`  datetime     NULL,
  PRIMARY KEY (`id`),
  KEY `idx_trang_thai` (`trang_thai`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Bang da tao truoc khi co cot tham_so (ban deploy dau 02/09/2026) thi them cot:
--   ALTER TABLE `web_lenh_nhan_vat` ADD COLUMN `tham_so` text NULL AFTER `phan_nghin`;
-- (server va web tu lam viec nay, chi ghi ra day de biet.)

-- Kiem tra lai: phai ra 1.
SELECT COUNT(*) AS co_bang FROM information_schema.tables
 WHERE table_schema = 'htbb_db' AND table_name = 'web_lenh_nhan_vat';
