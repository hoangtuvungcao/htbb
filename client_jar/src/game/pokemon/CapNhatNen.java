package game.pokemon;

/**
 * Bộ cập nhật RIÊNG từng nền tảng (0.9.7) cho {@link CapNhat}: tải tệp bản mới (có tiến độ), kiểm SHA-256, rồi cài
 * (APK: trình cài của Android; PC Windows: chạy bộ cài; PC Linux: thay AppImage + mở lại). Bản PC (htbb.pc.CapNhatPc)
 * và APK (com.htbb.pokemon.CapNhatApk) có lớp cài đặt; J2ME không có -> CapNhat mở link bằng platformRequest.
 * Mọi hàm gọi từ luồng game, không được chặn (tải chạy luồng riêng).
 */
public interface CapNhatNen {
    int CHUA = 0, DANG_TAI = 1, DANG_KIEM = 2, XONG = 3, LOI = 4;

    /** true = tự tải + cài được tệp này (có link http(s) + SHA-256, và nền tảng cài được ở chỗ đang chạy). */
    boolean hoTro(String url, String sha256);

    /** Bắt đầu tải (luồng riêng); đang tải thì bỏ qua. */
    void batDau(String url, String sha256, String phienBan);

    /** {@link #CHUA} / {@link #DANG_TAI} / {@link #DANG_KIEM} / {@link #XONG} / {@link #LOI}. */
    int trangThai();

    /** Số byte đã tải / tổng (tổng <= 0 = chưa biết). */
    long daTai();

    long tong();

    /** Lý do lỗi cho người chơi (trạng thái LOI). */
    String loi();

    /** Câu sau khi xong (vd "Đang mở bộ cài…"). */
    String ghiChu();

    /** Huỷ tải (xoá tệp dở). */
    void huy();

    /** XONG mà người chơi đóng trình cài: mở lại trình cài tệp đã tải (false = không làm được). */
    boolean caiLai();
}
