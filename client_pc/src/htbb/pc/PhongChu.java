package htbb.pc;

import java.awt.Font;
import java.io.InputStream;

/** Phông chữ thật đi kèm (client_pc/res/phong, SIL OFL 1.1): Noto Sans (chữ, đủ tiếng Việt) + Baloo 2 (tiêu đề). */
public final class PhongChu {
    private static Font thuong;
    private static Font dam;
    private static Font tieuDe;

    private PhongChu() {
    }

    /**
     * Nạp phông: chép từ jar ra <thư mục dữ liệu>/phong (một lần) rồi Font.createFont(File) - createFont(InputStream)
     * tạo tệp tạm cần SecureRandom (chậm lúc mở game; trên wine còn lỗi API mạng).
     */
    private static Font nap(String ten, String duPhong, int kieu) {
        InputStream in = PhongChu.class.getResourceAsStream("/phong/" + ten);
        if (in != null) {
            try {
                java.io.File d = new java.io.File(CaiDat.thuMucDuLieu(), "phong");
                d.mkdirs();
                java.io.File f0 = new java.io.File(d, ten);
                java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
                byte[] b = new byte[16384];
                int n;
                while ((n = in.read(b)) > 0) bo.write(b, 0, n);
                byte[] du = bo.toByteArray();
                if (!f0.isFile() || f0.length() != du.length) {
                    java.io.File tam = new java.io.File(d, ten + ".tam");
                    java.io.FileOutputStream fo = new java.io.FileOutputStream(tam);
                    try {
                        fo.write(du);
                    } finally {
                        fo.close();
                    }
                    if (!tam.renameTo(f0)) {
                        f0.delete();
                        tam.renameTo(f0);
                    }
                }
                Font f = Font.createFont(Font.TRUETYPE_FONT, f0);
                return f.deriveFont(12f);
            } catch (Throwable e) {
                NhatKy.loi("nap phong " + ten, e);
            } finally {
                try {
                    in.close();
                } catch (Throwable e) {
                    // bỏ qua
                }
            }
        }
        return new Font(duPhong, kieu, 12);
    }

    /** Noto Sans thường / đậm (cỡ 12, deriveFont để đổi cỡ). */
    public static synchronized Font thuong(boolean dam0) {
        if (dam0) {
            if (dam == null) dam = nap("NotoSans-Bold.ttf", Font.SANS_SERIF, Font.BOLD);
            return dam;
        }
        if (thuong == null) thuong = nap("NotoSans-Regular.ttf", Font.SANS_SERIF, Font.PLAIN);
        return thuong;
    }

    private static final Font[] DEM = new Font[2 * 1024];

    /** Noto Sans cỡ co (nhớ sẵn theo 0,1 px: deriveFont mỗi khung rất tốn + cấp phát). */
    public static Font co(boolean dam0, float co) {
        int k = Math.max(0, Math.min(1023, Math.round(co * 10))) * 2 + (dam0 ? 1 : 0);
        Font r = DEM[k];
        if (r == null) {
            r = thuong(dam0).deriveFont(k / 2 / 10f);
            DEM[k] = r;
        }
        return r;
    }

    /** Baloo 2 (tiêu đề, logo chữ) - đậm giả bằng deriveFont(BOLD). */
    public static synchronized Font tieuDe() {
        if (tieuDe == null) tieuDe = nap("Baloo2-wght.ttf", Font.SANS_SERIF, Font.BOLD).deriveFont(Font.BOLD);   // 0.9.6: đậm thật (ảnh chữ dựng đậm)
        return tieuDe;
    }
}
