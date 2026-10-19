import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 * KIỂM ICON CHIÊU (0.9.5): mọi chiêu của game.pokemon.PokeChieu.TEN có ô icon KHÔNG RỖNG trong cả 6 tấm
 * res/ui/chieu_{,tron_}{32,64,96}.png (tools/pokemon/sinh_icon_chieu.py), ô nằm trong tấm, tấm đúng cỡ ô x 8 cột,
 * và không có ô thừa (tấm sinh lệch bảng chiêu). Mã thoát 1 = thiếu icon.
 *
 *   java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemIconChieu
 */
public class KiemIconChieu {
    static int dat, hong;

    static void kiem(boolean ok, String ten) {
        if (ok) dat++;
        else {
            hong++;
            System.out.println("HONG: " + ten);
        }
    }

    static boolean coAnh(BufferedImage b, int x0, int y0, int n) {
        int dem = 0;
        for (int y = y0; y < y0 + n; y++) {
            for (int x = x0; x < x0 + n; x++) {
                if ((b.getRGB(x, y) >>> 24) > 32) dem++;
            }
        }
        return dem > n * n / 3;   // huy hiệu phủ phần lớn ô
    }

    public static void main(String[] a) throws Exception {
        String[][] ten = game.pokemon.PokeChieu.TEN;
        int tong = 0;
        for (String[] t : ten) tong += t.length;
        int cot = game.pokemon.AnhUI.COT_CHIEU;
        for (int t = 0; t < 2; t++) {
            for (int co : game.pokemon.AnhUI.CO_CHIEU) {
                String tep = game.pokemon.AnhUI.tamChieu(t == 1, co);
                InputStream in = KiemIconChieu.class.getResourceAsStream(tep);
                kiem(in != null, "thiếu tấm " + tep);
                if (in == null) continue;
                BufferedImage b = ImageIO.read(in);
                in.close();
                int hang = (tong + cot - 1) / cot;
                kiem(b.getWidth() == cot * co && b.getHeight() == hang * co, tep + " sai cỡ " + b.getWidth() + "x" + b.getHeight());
                for (int lop = 0; lop < ten.length; lop++) {
                    for (int sk = 0; sk < ten[lop].length; sk++) {
                        int o = game.pokemon.AnhUI.oChieu(lop, sk);
                        int x = (o % cot) * co, y = (o / cot) * co;
                        boolean trong = x + co <= b.getWidth() && y + co <= b.getHeight();
                        kiem(trong && coAnh(b, x, y, co), tep + ": chiêu không có icon - dòng " + lop + " #" + sk + " " + ten[lop][sk]);
                    }
                }
                for (int o = tong; o < hang * cot; o++) {
                    int x = (o % cot) * co, y = (o / cot) * co;
                    kiem(!coAnh(b, x, y, co), tep + ": ô thừa " + o + " (tấm lệch bảng chiêu)");
                }
            }
        }
        kiem(game.pokemon.AnhUI.oChieu(9, 0) < 0 && game.pokemon.AnhUI.oChieu(0, 99) < 0, "oChieu ngoài bảng phải -1");
        System.out.println("KiemIconChieu: " + tong + " chiêu, dat " + dat + ", hong " + hong);
        System.exit(hong > 0 ? 1 : 0);
    }
}
