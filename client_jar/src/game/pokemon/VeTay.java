package game.pokemon;

/**
 * Đếm ICON VẼ BẰNG CODE (0.9.7, quy tắc chủ dự án: mọi icon / logo là ẢNH PNG - /nro/icon.png, icon vật phẩm, sprite).
 * Mọi đường vẽ icon bằng hình (vector BieuTuong, đồng xu vector TienTe, Poké Ball vẽ cung, dấu X nét...) gọi
 * {@link #ghi} - chỉ còn là đường DỰ PHÒNG khi thiếu ảnh. Kiểm thử (ChupTrongGame) đòi dem == 0 ở mọi màn.
 */
public final class VeTay {
    private VeTay() {
    }

    /** số lần vẽ icon bằng code từ lúc xoá */
    public static int dem;
    /** chỗ vẽ gần nhất (chẩn đoán) */
    public static String cuoi;

    public static void ghi(String noi) {
        dem++;
        cuoi = noi;
    }

    public static void xoa() {
        dem = 0;
        cuoi = null;
    }
}
