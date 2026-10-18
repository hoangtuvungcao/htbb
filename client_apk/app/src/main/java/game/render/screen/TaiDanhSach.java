package game.render.screen;

/**
 * Bản Android chỉ là lớp chuyển tiếp: danh sách server trên Android do NqshMod tải (tự động lúc mở game
 * + nút "Cập nhật"). AutoGuard.dsGuard / capNhatGuard dùng chung với jar nhưng không được gắn trên Android.
 */
final class TaiDanhSach {
    private TaiDanhSach() {
    }

    static void taiNgam() {
    }

    static void capNhat() {
        NqshMod.capNhat();
    }
}
