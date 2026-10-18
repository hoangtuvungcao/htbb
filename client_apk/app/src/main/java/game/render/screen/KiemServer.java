package game.render.screen;

import network.Message;

/**
 * Bản Android chỉ là lớp chuyển tiếp: xác thực -88 trên Android do NqshMod làm (gắn vào GameService.login /
 * MessageHandler.onMessage bằng patch.py). Code mod dùng chung (AutoGuard, PauseMenu) vẫn gọi các tên này.
 */
final class KiemServer {
    static boolean daQua = true;

    private KiemServer() {
    }

    static boolean chanDangNhap(String tk, String mk, String ver) {
        return false;
    }

    static boolean nhan(Message message) {
        return false;
    }

    /** Bản jar ngắt kết nối khi vào game mà chưa qua xác thực; Android: server đã chặn ở đăng nhập. */
    static boolean canNgat() {
        return false;
    }
}
