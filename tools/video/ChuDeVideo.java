import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import javax.imageio.ImageIO;
import javax.microedition.lcdui.Graphics;

/**
 * Biển chữ đề cho video bằng CHÍNH bộ vẽ giao diện NRO của client (game.pokemon.KhungNro tấm gỗ + giấy da,
 * ChuDe.bien biển đỏ đinh vàng, chữ HD Baloo 2 / Noto Sans của bản PC) - cùng kiểu với tiêu đề bảng trong game.
 * Gọi sau khi client đã khởi động (tài nguyên /nro/khung.png, phông chữ đã nạp).
 *
 * Tệp danh sách: mỗi dòng "tên|tiêu đề|dòng phụ" (dòng phụ có thể trống) -> <ra>/<tên>.png (ARGB, nền trong).
 */
public class ChuDeVideo {
    /** Vẽ một biển: k = hệ số phóng (3 = cỡ chữ trong game ở 1080p; 4-5 = to hơn cho chữ đề). */
    public static BufferedImage bien(final String tieuDe, final String phu, final int k) {
        final BufferedImage[] ra = {null};
        Quay.tren(new Runnable() {
            public void run() {
                int vaiT = game.pokemon.ChuDe.TIEU_DE_TO, vaiP = game.pokemon.ChuDe.THUONG;
                int hd = Math.max(20, game.pokemon.ChuDe.cao(vaiT) + 8);
                int wT = game.pokemon.ChuDe.rong(tieuDe, vaiT) + 52;
                int wP = phu == null || phu.isEmpty() ? 0 : game.pokemon.ChuDe.rong(phu, vaiP) + 40;
                int w = Math.max(wT + 28, wP);
                int hP = wP == 0 ? 0 : game.pokemon.ChuDe.cao(vaiP) + 10;
                int h = 6 + hd + hP + 12;
                int le = 4;
                BufferedImage b = new BufferedImage((w + 2 * le) * k, (h + 2 * le) * k, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = b.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                Graphics2D gg = (Graphics2D) g2.create();
                gg.scale(k, k);
                Graphics g = new Graphics(gg, w + 2 * le, h + 2 * le, k);
                g.translate(le, le);
                game.pokemon.KhungNro.bong(g, 0, 0, w, h);
                if (!game.pokemon.KhungNro.ve(g, game.pokemon.KhungNroBang.TAM, 0, 0, w, h)) {
                    g.setColor(game.pokemon.ChuDe.NEN);
                    g.fillRect(0, 0, w, h);
                }
                int wb = wT;
                game.pokemon.ChuDe.bien(g, (w - wb) / 2, 6, wb, hd);
                game.pokemon.ChuDe.chuVienGiua(g, tieuDe, w / 2, 6, hd, vaiT, 0xFFFFFF, game.pokemon.ChuDe.VIEN, 2);
                if (wP > 0) {
                    game.pokemon.ChuDe.chuGiua(g, phu, w / 2, 6 + hd + 4, hP, vaiP, game.pokemon.ChuDe.CHU, 2);
                }
                gg.dispose();
                g2.dispose();
                ra[0] = b;
            }
        });
        return ra[0];
    }

    /** Sinh mọi biển trong tệp danh sách ở hai cỡ: <tên>.png (k) và <tên>_doc.png (kDoc, cho video dọc). */
    public static void sinh(File ds, File ra, int k, int kDoc) throws Exception {
        ra.mkdirs();
        List<String> dong = Files.readAllLines(ds.toPath(), StandardCharsets.UTF_8);
        for (String l : dong) {
            if (l.trim().isEmpty() || l.startsWith("#")) continue;
            String[] p = l.split("\\|", -1);
            String phu = p.length > 2 ? p[2] : "";
            ImageIO.write(bien(p[1], phu, k), "png", new File(ra, p[0] + ".png"));
            ImageIO.write(bien(p[1], phu, kDoc), "png", new File(ra, p[0] + "_doc.png"));
            System.out.println("[chu de] " + p[0]);
        }
    }
}
