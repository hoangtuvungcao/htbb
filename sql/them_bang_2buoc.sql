-- ============================================================================
--  Bang `web_admin_2fa` - xac thuc hai buoc (TOTP) cho Trang quan tri web.
--
--      mysql -u root htbb_db < sql/them_bang_2buoc.sql
--
--  KHONG BAT BUOC CHAY TAY: lenh ghi danh tu tao bang
--  (HTBB-Web scripts/tao-ma-2buoc.mjs). De o day de xem cau truc ma khong phai
--  doc ma nguon web.
--
-- ----------------------------------------------------------------------------
--  VI SAO CAN
-- ----------------------------------------------------------------------------
--  Truoc day vao Trang quan tri chi can DUNG MOT thu: mat khau tai khoan game.
--  Ma toan bo trang dang chay tren HTTP TRAN (may chu chua co cong 443, module
--  ssl cua Apache con chua cai) - nen moi lan dang nhap, ten va mat khau di qua
--  Internet duoi dang chu thuong, ai dung tren duong truyen cung doc duoc.
--  Do la duong ro that su, khong phai viec "duong dan /admin bi lo".
--
--  Voi 2FA thi biet mat khau van chua du: phai co them ma 6 so sinh tu dien
--  thoai, doi 30 giay mot lan, va moi ma chi dung duoc DUNG MOT LAN.
--
-- ----------------------------------------------------------------------------
--  KHOA BI MAT KHONG BAO GIO DI QUA WEB
-- ----------------------------------------------------------------------------
--  Ghi danh CHI lam bang lenh chay tren may chu qua SSH:
--
--      cd <thu muc HTBB-Web> && sudo -u htbb-web node --env-file=/etc/htbb-web.env scripts/tao-ma-2buoc.mjs <ten>
--
--  Web KHONG co duong nao tao hay doi khoa. Neu de nguoi go dung mat khau tu
--  ghi danh tren web thi ke trom duoc mat khau cung tu ghi danh duoc bang dien
--  thoai cua no - luc do 2FA khong chan ai ca, chi them mot buoc cho chu nha.
--
-- ----------------------------------------------------------------------------
--  MAT DIEN THOAI THI SAO
-- ----------------------------------------------------------------------------
--   1. Dung mot trong 8 ma du phong in ra luc ghi danh (moi ma mot lan).
--   2. Het ma du phong thi vao may chu bang SSH va xoa khoa di:
--          DELETE FROM web_admin_2fa WHERE admin_user = 'ten';
--      roi ghi danh lai. Co SSH la luon con duong ve - do la chot cuoi, va
--      cung la ly do KHONG can cua hau nao tren web.
-- ============================================================================

CREATE TABLE IF NOT EXISTS `web_admin_2fa` (
  `admin_user`   VARCHAR(50) NOT NULL
      COMMENT 'team_user.username, chu thuong. Khop ADMIN_ACCOUNTS trong config.local.php',
  `khoa_bimat`   VARCHAR(64) NOT NULL
      COMMENT 'Khoa TOTP dang Base32 (20 byte = 32 ky tu). CHI sinh boi tao_ma_2buoc.php',
  `kich_hoat`    TINYINT     NOT NULL DEFAULT 0
      COMMENT '1 = tai khoan nay BUOC PHAI go ma hai buoc, khong co cach bo qua',
  `buoc_da_dung` BIGINT      NOT NULL DEFAULT 0
      COMMENT 'Buoc thoi gian cao nhat da dung. Chong dung lai mot ma da bi doc len',
  `ma_du_phong`  TEXT        NULL
      COMMENT '8 ma du phong DA BAM, ngan nhau bang \\n. Moi ma dung dung mot lan',
  `tao_luc`      DATETIME    NULL COMMENT 'Luc ghi danh',
  `dung_luc`     DATETIME    NULL COMMENT 'Lan cuoi go ma thanh cong',
  PRIMARY KEY (`admin_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
--  KIEM TRA
-- ----------------------------------------------------------------------------
-- Ai da bat hai buoc, con bao nhieu ma du phong:
-- SELECT admin_user, kich_hoat, tao_luc, dung_luc,
--        CASE WHEN ma_du_phong IS NULL OR ma_du_phong = '' THEN 0
--             ELSE LENGTH(ma_du_phong) - LENGTH(REPLACE(ma_du_phong, '\n', '')) + 1
--        END AS ma_du_phong_con_lai
--   FROM web_admin_2fa;
--
-- Go hai buoc cua mot tai khoan (mat dien thoai, het ma du phong):
-- DELETE FROM web_admin_2fa WHERE admin_user = 'ten';
