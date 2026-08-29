-- ============================================================================
--  Cấm theo MÃ MÁY (device id) — chạy một lần trên database GAME (kpah_db)
--
--      mysql -u root kpah_db < sql/them_bang_cam_may.sql
--
--  VÌ SAO CÓ HAI BẢNG
--  ------------------
--  `tob_cam_may`       : danh sách máy bị cấm. Bảng này nhỏ, server nạp vào RAM
--                        và làm mới mỗi 60 giây, nên không tra database mỗi lần
--                        ai đó đăng nhập (nếu tra thì một trận dò mật khẩu sẽ
--                        thành một trận dội database).
--
--  `tob_may_dang_nhap` : lịch sử máy nào đã dùng tài khoản nào. Đây mới là thứ
--                        làm cho việc cấm máy có ích: cấm một tài khoản thì kẻ
--                        phá chỉ cần lập tài khoản mới, còn bảng này cho bạn
--                        thấy "cái máy này đã dùng 12 tài khoản" để cấm đúng gốc.
--
--  MÃ MÁY LẤY TỪ ĐÂU
--  -----------------
--  Client Unity gửi `SystemInfo.deviceUniqueIdentifier` (Client/Canvas.cs:157).
--  KHÔNG phải địa chỉ MAC: từ Android 10 và iOS 7 trở đi ứng dụng không đọc được
--  MAC thật nữa (Android trả về 02:00:00:00:00:00 cho mọi ứng dụng).
--
--  ĐỌC KỸ TRƯỚC KHI DÙNG
--  ---------------------
--  Đây là giá trị do CLIENT tự khai, nên nằm trong tay người dùng. Nó chặn được
--  người phá lười (đa số), không chặn được người biết sửa client. Ngoài ra:
--    - Cài lại ứng dụng có thể sinh mã mới -> người bị cấm thoát ra được.
--    - Máy dùng chung (quán net, máy trong nhà) thì cấm một người là chặn cả nhóm.
--  Và nó HOÀN TOÀN vô dụng với DDoS: kẻ tấn công không đăng nhập, chỉ mở socket.
-- ============================================================================

CREATE TABLE IF NOT EXISTS `tob_cam_may` (
    `ma_may`  VARCHAR(64)  NOT NULL COMMENT 'SystemInfo.deviceUniqueIdentifier do client gửi',
    `ly_do`   VARCHAR(255) NOT NULL DEFAULT '',
    `boi_ai`  VARCHAR(64)  NOT NULL DEFAULT '' COMMENT 'admin nào cấm',
    `luc`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`ma_may`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='Danh sách máy bị cấm đăng nhập';

CREATE TABLE IF NOT EXISTS `tob_may_dang_nhap` (
    `ma_may`   VARCHAR(64) NOT NULL,
    `username` VARCHAR(64) NOT NULL,
    `ip`       VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'dài 45 để chứa được cả IPv6',
    `lan_dau`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `lan_cuoi` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `so_lan`   INT         NOT NULL DEFAULT 1,
    PRIMARY KEY (`ma_may`, `username`),
    KEY `k_username` (`username`),
    KEY `k_lan_cuoi` (`lan_cuoi`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='Máy nào đã đăng nhập tài khoản nào';

-- ============================================================================
--  DÙNG THẾ NÀO
-- ============================================================================
--
-- 1) Xem một tài khoản phá hoại đã dùng những máy nào:
--
--      SELECT ma_may, ip, so_lan, lan_cuoi
--      FROM tob_may_dang_nhap
--      WHERE username = 'tenTaiKhoan'
--      ORDER BY lan_cuoi DESC;
--
-- 2) Từ một mã máy, xem nó đã dùng những tài khoản nào (đây là chỗ lộ ra kẻ
--    lập hàng loạt tài khoản):
--
--      SELECT username, ip, so_lan, lan_dau, lan_cuoi
--      FROM tob_may_dang_nhap
--      WHERE ma_may = 'dan-ma-may-vao-day'
--      ORDER BY lan_cuoi DESC;
--
-- 3) Tìm những máy dùng nhiều tài khoản bất thường:
--
--      SELECT ma_may, COUNT(*) so_tai_khoan, MAX(lan_cuoi) gan_nhat
--      FROM tob_may_dang_nhap
--      GROUP BY ma_may
--      HAVING so_tai_khoan >= 5
--      ORDER BY so_tai_khoan DESC;
--
-- 4) Cấm một máy (có hiệu lực trong vòng 60 giây, không cần khởi động lại):
--
--      INSERT INTO tob_cam_may (ma_may, ly_do, boi_ai)
--      VALUES ('dan-ma-may-vao-day', 'dung tool', 'hoahv')
--      ON DUPLICATE KEY UPDATE ly_do = VALUES(ly_do), boi_ai = VALUES(boi_ai);
--
-- 5) Bỏ cấm:
--
--      DELETE FROM tob_cam_may WHERE ma_may = 'dan-ma-may-vao-day';
--
-- ============================================================================
