-- CHONG CLONE (chu game duyet 19/10/2026). Idempotent, MariaDB >= 10.0.2.
-- May chu game (data/DatabaseChongClone) cung tu tao bang luc dung lan dau; chay tep nay de trang
-- /admin/kinh-te (muc "Nghi clone") co bang ngay, khong can doi.
--   mysql htbb_db < sql/them_bang_chong_clone.sql
--
-- Moi dong = mot lan chuyen xu / gia tri giua HAI nguoi choi (giao dich, ky gui, thu cung lay xu, cho trang tri,
-- cho sinh thai), ke ca lan bi chan (bi_chan = 1). nghi = 1: nguoi nhan co nhieu nguoi gui trong ngay / nguoi gui
-- chung thiet bi - IP / nguoi gui cung thiet bi voi nguoi nhan (ly_do). Cung DDL voi DatabaseChongClone.SQL_TAO_BANG.
CREATE TABLE IF NOT EXISTS tob_log_chuyen_gia_tri (
  id BIGINT NOT NULL AUTO_INCREMENT,
  luc DATETIME NOT NULL,
  kieu VARCHAR(20) NOT NULL,
  gui_id INT NOT NULL DEFAULT 0, gui_ten VARCHAR(40) NOT NULL DEFAULT '', gui_tk INT NOT NULL DEFAULT 0,
  gui_cap INT NOT NULL DEFAULT 0, gui_may VARCHAR(64) NOT NULL DEFAULT '', gui_ip VARCHAR(64) NOT NULL DEFAULT '',
  nhan_id INT NOT NULL DEFAULT 0, nhan_ten VARCHAR(40) NOT NULL DEFAULT '', nhan_tk INT NOT NULL DEFAULT 0,
  nhan_cap INT NOT NULL DEFAULT 0, nhan_may VARCHAR(64) NOT NULL DEFAULT '', nhan_ip VARCHAR(64) NOT NULL DEFAULT '',
  xu BIGINT NOT NULL DEFAULT 0,
  chi_tiet VARCHAR(255) NOT NULL DEFAULT '',
  bi_chan TINYINT NOT NULL DEFAULT 0,
  nghi TINYINT NOT NULL DEFAULT 0,
  ly_do VARCHAR(255) NOT NULL DEFAULT '',
  PRIMARY KEY (id), KEY k_nhan (nhan_id, luc), KEY k_luc (luc), KEY k_nghi (nghi, luc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
