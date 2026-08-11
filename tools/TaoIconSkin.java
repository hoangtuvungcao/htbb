import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Tao icon vat pham skin thu cuoi tu dai anh map/avtathu/<id>.png.
 *
 * Icon vat pham cua game nam o map/icon/<id>.png, co 15x15..18x18 px, co kenh trong
 * suot. Server nap ca thu muc luc khoi dong (DataGame: allImg[i] = map/icon/i.png,
 * nIconItem = 855) roi phuc vu theo id khi client xin (DataGame.sendByteImg, nhanh
 * idImg < nIconItem). Cot data_item.xstart CHINH LA id icon nay
 * (Database: item.idIcon = rs.getShort("xstart")).
 *
 * Cach lam: lay frame dau cua dai (dai co 3 frame nen frame 0 cao = h/3), cat sat
 * vien theo kenh trong suot, thu nho vua khung 16x16 giu ti le, dat can giua.
 *
 *   javac -d out tools/TaoIconSkin.java
 *   java -cp out TaoIconSkin map/avtathu/21.png 815 [--khung=16] [--frame=3]
 *
 * Ghi ra map/icon/<idIcon>.png. Nho chon id icon CON TRONG (khong co file san) va
 * < 855; id 800..806 danh cho imgOther nen khong dung duoc.
 */
public class TaoIconSkin {

    public static void main(final String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Dung: java TaoIconSkin <map/avtathu/N.png> <idIcon> [--khung=16] [--frame=3]");
            return;
        }
        final File nguon = new File(args[0]);
        final int idIcon = Integer.parseInt(args[1]);
        int khung = 16, soFrame = 3;
        for (int i = 2; i < args.length; i++) {
            if (args[i].startsWith("--khung=")) khung = Integer.parseInt(args[i].substring(8));
            else if (args[i].startsWith("--frame=")) soFrame = Integer.parseInt(args[i].substring(8));
        }
        if (idIcon >= 855 || (idIcon >= 800 && idIcon <= 806)) {
            System.out.println("Id icon " + idIcon + " khong dung duoc: phai < 855 va khong nam 800..806");
            return;
        }

        final BufferedImage dai = ImageIO.read(nguon);
        if (dai == null) {
            System.out.println("Khong doc duoc anh: " + nguon);
            return;
        }
        final int hFrame = dai.getHeight() / soFrame;
        final BufferedImage f0 = dai.getSubimage(0, 0, dai.getWidth(), hFrame);

        // Cat sat vien theo kenh trong suot (alpha >= 16 moi tinh la co hinh).
        int x1 = f0.getWidth(), y1 = hFrame, x2 = -1, y2 = -1;
        for (int y = 0; y < hFrame; y++) {
            for (int x = 0; x < f0.getWidth(); x++) {
                if (((f0.getRGB(x, y) >>> 24) & 0xFF) < 16) continue;
                if (x < x1) x1 = x;
                if (x > x2) x2 = x;
                if (y < y1) y1 = y;
                if (y > y2) y2 = y;
            }
        }
        if (x2 < x1 || y2 < y1) {
            System.out.println("Frame dau trong rong, khong cat duoc");
            return;
        }
        final int w = x2 - x1 + 1, h = y2 - y1 + 1;
        final double tiLe = Math.min((double) khung / w, (double) khung / h);
        final int wDich = Math.max(1, (int) Math.round(w * tiLe));
        final int hDich = Math.max(1, (int) Math.round(h * tiLe));

        final BufferedImage cat = f0.getSubimage(x1, y1, w, h);
        final BufferedImage ra = new BufferedImage(khung, khung, BufferedImage.TYPE_INT_ARGB);
        final java.awt.Graphics2D g = ra.createGraphics();
        g.drawImage(cat.getScaledInstance(wDich, hDich, Image.SCALE_AREA_AVERAGING),
                (khung - wDich) / 2, (khung - hDich) / 2, null);
        g.dispose();
        // Don kenh trong suot sau khi thu nho: duoi 40 cho trong han, tren 200 cho dac
        // han — de vien icon khong bi mo nhoe kieu anh chup.
        for (int y = 0; y < khung; y++) {
            for (int x = 0; x < khung; x++) {
                final int p = ra.getRGB(x, y);
                final int al = (p >>> 24) & 0xFF;
                if (al < 40) ra.setRGB(x, y, 0);
                else if (al > 200) ra.setRGB(x, y, p | 0xFF000000);
            }
        }

        final File dich = new File("map/icon/" + idIcon + ".png");
        ImageIO.write(ra, "png", dich);
        System.out.println("Xong: " + dich + "  " + khung + "x" + khung
                + " (tu " + nguon.getName() + " frame 0, khung goc " + w + "x" + h + ")");
    }
}
