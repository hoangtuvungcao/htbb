<?php
/**
 * ============================================================
 *  CẤU HÌNH WEBSITE KPAH  —  SỬA CÁC GIÁ TRỊ DƯỚI ĐÂY
 * ============================================================
 *  - Website này dùng CHUNG MySQL/MariaDB với game server.
 *  - Có 2 database:
 *      + account : chứa bảng team_user (tài khoản đăng nhập) - login server dùng
 *      + kpah_db : dữ liệu game (nhân vật, kích hoạt, bảng xếp hạng)
 *  - Mật khẩu lưu bằng hàm PASSWORD() của MySQL/MariaDB để KHỚP với login server.
 *    (Login server kiểm tra: ... and `password`=password('...'))
 * ============================================================
 */

// ---- Kết nối database ----
const DB_HOST = '127.0.0.1';
const DB_PORT = 3306;
const DB_USER = 'root';
const DB_PASS = '';               // mật khẩu MySQL, để trống nếu không có

const DB_ACCOUNT = 'account';     // DB chứa bảng team_user (đúng như loginServer/server.ini)
const DB_GAME    = 'kpah_db';     // DB game (đúng như server.ini: db.name)

// ---- Thiết lập website ----
const SITE_NAME   = 'KPAH - Khí Phách Anh Hùng';
const SITE_TAGLINE = 'Máy chủ private - Hồi ức tuổi thơ';

// Link tải game (sửa thành link của bạn)
const LINK_ANDROID = '#';
const LINK_PC      = '#';
const LINK_IOS     = '#';

// Nhóm hỗ trợ (Zalo/Facebook...) - sửa hoặc để '#'
const LINK_GROUP   = '#';

// Quy tắc tài khoản (khớp với server: 4-20 ký tự, chỉ chữ và số)
const USERNAME_MIN = 4;
const USERNAME_MAX = 20;
const PASSWORD_MIN = 4;
const PASSWORD_MAX = 20;

/**
 * Tự động kích hoạt tài khoản ngay khi đăng ký.
 *   true  = đăng ký xong đăng nhập game được luôn (tiện cho test / server nhỏ)
 *   false = phải bấm "Kích hoạt" ở trang tài khoản (giống trang gốc)
 * Lưu ý: chỉ có tác dụng khi game bật kiểm tra kích hoạt (sv.EnableAvtiveCheck=1).
 */
const AUTO_ACTIVATE = true;
