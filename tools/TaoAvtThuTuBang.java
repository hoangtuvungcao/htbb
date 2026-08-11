import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Tao anh thu cuoi cho bang "Trang bi thu" (map/avtathu/<id>.png) tu bang chuan do
 * tools/ChuanBiBangSprite.java sinh ra.
 *
 * Bang do co dang <so hang> x <so frame>, o deu nhau. Bang Trang bi thu chi ve MOT
 * huong (nhin truoc) va can DUNG 3 FRAME xep doc:
 *
 *     client: himg = anh.cao / numFrame,  idFrame = arrFrame[(numFrame != 3) ? 1 : 0][...]
 *             arrFrame = { {0,1,2}, {0,1} },  nhip = (numFrame != 3) ? 6 : 3 tick
 *
 * Gui khac 3 frame thi client chi ve frame 0 va 1 voi nhip cham gap doi — xem
 * SkinThuNuoi.TONG_FRAME. Vi vay lay 3 o cua hang nhin truoc, thu nho ve cung chieu
 * cao roi ghep doc.
 *
 *   javac -d out tools/TaoAvtThuTuBang.java
 *   java -cp out TaoAvtThuTuBang <bang.png> <hang> <o0,o1,o2> <cao> <map/avtathu/21.png>
 *
 * Ti le thu nho lay theo frame CAO NHAT trong ba o, dung mot ti le duy nhat cho ca ba —
 * thu nho rieng tung frame ve cung chieu cao thi con thu to nho phap phong theo tung
 * khung, nhin nhu dang thoi phong len xep xuong.
 */
public class TaoAvtThuTuBang {

    public static void main(final String[] args) throws Exception {
        if (args.length < 5) {
            System.out.println("Dung: java TaoAvtThuTuBang <bang.png> <hang> <o0,o1,o2> <cao> <ra.png>");
            return;
        }
        final BufferedImage bang = ImageIO.read(new File(args[0]));
        if (bang == null) {
            System.out.println("Khong doc duoc anh: " + args[0]);
            return;
        }
        final int hang = Integer.parseInt(args[1]);
        final String[] sO = args[2].split(",");
        final int cao = Integer.parseInt(args[3]);
        final File dich = new File(args[4]);

        // Do so hang / so o cua bang bang cach do khoang trong (bang chuan co le 8px).
        final int soHang = demDai(bang, true);
        final int soO = demDai(bang, false);
        if (soHang <= 0 || soO <= 0) {
            System.out.println("Khong do duoc luoi cua bang");
            return;
        }
        final int wO = bang.getWidth() / soO, hO = bang.getHeight() / soHang;
        System.out.println("Bang " + bang.getWidth() + "x" + bang.getHeight() + " = " + soHang
                + " hang x " + soO + " o (o " + wO + "x" + hO + ")");

        // Cat ba frame ra truoc, chua thu nho
        final BufferedImage[] f = new BufferedImage[3];
        int caoNhat = 0, rongNhat = 0;
        for (int i = 0; i < 3; i++) {
            final int c = Integer.parseInt(sO[Math.min(i, sO.length - 1)].trim());
            f[i] = catSatVien(bang, c * wO, hang * hO, wO, hO);
            caoNhat = Math.max(caoNhat, f[i].getHeight());
            rongNhat = Math.max(rongNhat, f[i].getWidth());
        }
        final double tiLe = (double) cao / caoNhat;
        final int rong = Math.max(1, (int) Math.ceil(rongNhat * tiLe));
        System.out.println("Frame goc lon nhat " + rongNhat + "x" + caoNhat + " -> o " + rong + "x" + cao);

        final BufferedImage ra = new BufferedImage(rong, cao * 3, BufferedImage.TYPE_INT_ARGB);
        final java.awt.Graphics2D g = ra.createGraphics();
        for (int i = 0; i < 3; i++) {
            final int w2 = Math.max(1, (int) Math.round(f[i].getWidth() * tiLe));
            final int h2 = Math.max(1, (int) Math.round(f[i].getHeight() * tiLe));
            // Client ve o nay theo goc giua o (drawRegion ... 3 = HCENTER|VCENTER) nen can
            // giua ca hai chieu, va dat sat day de con thu khong lo lung.
            g.drawImage(f[i].getScaledInstance(w2, h2, Image.SCALE_AREA_AVERAGING),
                    (rong - w2) / 2, (i + 1) * cao - h2, null);
        }
        g.dispose();
        // Don kenh trong suot sau khi thu nho: duoi 40 cho trong han, tren 200 cho dac han
        for (int y = 0; y < ra.getHeight(); y++) {
            for (int x = 0; x < ra.getWidth(); x++) {
                final int p = ra.getRGB(x, y);
                final int al = (p >>> 24) & 0xFF;
                if (al < 40) ra.setRGB(x, y, 0);
                else if (al > 200) ra.setRGB(x, y, p | 0xFF000000);
            }
        }
        ImageIO.write(ra, "png", dich);
        System.out.println("Xong: " + dich + "  " + rong + "x" + (cao * 3) + " (3 frame)");
    }

    /** Dem so dai co hinh theo hang (true) hoac theo cot (false). */
    private static int demDai(final BufferedImage img, final boolean theoHang) {
        final int n = theoHang ? img.getHeight() : img.getWidth();
        final int k = theoHang ? img.getWidth() : img.getHeight();
        int dem = 0;
        boolean dang = false;
        for (int i = 0; i < n; i++) {
            boolean co = false;
            for (int j = 0; j < k; j++) {
                final int c = theoHang ? img.getRGB(j, i) : img.getRGB(i, j);
                if (((c >>> 24) & 0xFF) != 0) { co = true; break; }
            }
            if (co && !dang) dem++;
            dang = co;
        }
        return dem;
    }

    /** Cat mot o roi bo sat vien theo kenh trong suot. */
    private static BufferedImage catSatVien(final BufferedImage img, final int ox, final int oy,
                                             final int w, final int h) {
        int x1 = ox + w, y1 = oy + h, x2 = ox - 1, y2 = oy - 1;
        for (int y = oy; y < Math.min(oy + h, img.getHeight()); y++) {
            for (int x = ox; x < Math.min(ox + w, img.getWidth()); x++) {
                if (((img.getRGB(x, y) >>> 24) & 0xFF) == 0) continue;
                if (x < x1) x1 = x;
                if (x > x2) x2 = x;
                if (y < y1) y1 = y;
                if (y > y2) y2 = y;
            }
        }
        if (x2 < x1 || y2 < y1) return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        final BufferedImage ra = new BufferedImage(x2 - x1 + 1, y2 - y1 + 1, BufferedImage.TYPE_INT_ARGB);
        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) ra.setRGB(x - x1, y - y1, img.getRGB(x, y));
        }
        return ra;
    }
}
