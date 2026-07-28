import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * Dung anh THU CUOI (con thu chay tren ban do) tu mot bang sprite.
 *
 * Khac han anh trong bang thong tin: con thu chay tren map duoc client ve bang
 * mot dai NGANG duy nhat, va toa do tung frame nam trong bang cung o CLIENT
 * (Char.fHorse / sizeHorse / hHorse / dxh / dyh, 8 bo "preset"). Vi vay anh moi
 * phai dat frame dung y nguyen toa do cua mot preset co san, khong duoc tu do.
 *
 * Cong cu nay dung theo PRESET 5 — bo lon nhat, dang duoc phuong hoang dung:
 *
 *   cao moi frame     : 45
 *   huong 0 (lên)     : x = 180, 210, 255, 210   rong = 30, 45, 48, 45
 *   huong 1 (xuống)   : x = 303, 333, 379, 333   rong = 30, 45, 48, 45
 *   huong 2 (trái)    : x =   0,  60, 120,  60   rong = 60, 60, 60, 60
 *   huong 3 (phải)    : x = 427, 487, 547, 487   rong = 60, 60, 60, 60
 *   => dai rong 607, cao 45
 *
 * Frame thu 4 lap lai frame thu 2 (client chay 3 frame khi di bo, 4 khi bay).
 *
 * Anh ra duoc luu thanh HAI file dung dinh dang cua game:
 *   map/skinthu/<ten>0_header : toan bo file PNG
 *   map/skinthu/<ten>_data    : file RONG
 * Client noi header + data roi decode (Res.createImgByHeader), nen de tron PNG
 * trong header va data rong la cach don gian nhat — khong phai tach palette.
 *
 *   javac -d out tools/CatSpriteSheet.java tools/TaoDataSprite.java tools/TaoThuCuoiSkin.java
 *   java -cp out TaoThuCuoiSkin <bang.png> <ten> --len=7 --xuong=9 --trai=4 --phai=6 [--botrai=0] [--nguong=48]
 *
 * So hang lay tu ket qua do cua CatSpriteSheet ... --thu (hang 0 la hang tren cung).
 */
public class TaoThuCuoiSkin {

    private static final int CAO = 45;
    private static final int RONG_DAI = 607;
    private static final int[][] X = {
        {180, 210, 255, 210},
        {303, 333, 379, 333},
        {0, 60, 120, 60},
        {427, 487, 547, 487}
    };
    private static final int[][] W = {
        {30, 45, 48, 45},
        {30, 45, 48, 45},
        {60, 60, 60, 60},
        {60, 60, 60, 60}
    };

    public static void main(final String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Dung: java TaoThuCuoiSkin <bang.png> <ten> "
                    + "--len=N --xuong=N --trai=N --phai=N [--botrai=0] [--nguong=48]");
            return;
        }
        final File sheet = new File(args[0]);
        final String ten = args[1];
        final int[] hang = {-1, -1, -1, -1};
        int boTrai = 0, nguong = 48;
        for (int i = 2; i < args.length; i++) {
            final String a = args[i];
            if (a.startsWith("--len=")) hang[0] = Integer.parseInt(a.substring(6));
            else if (a.startsWith("--xuong=")) hang[1] = Integer.parseInt(a.substring(8));
            else if (a.startsWith("--trai=")) hang[2] = Integer.parseInt(a.substring(7));
            else if (a.startsWith("--phai=")) hang[3] = Integer.parseInt(a.substring(7));
            else if (a.startsWith("--botrai=")) boTrai = Integer.parseInt(a.substring(9));
            else if (a.startsWith("--nguong=")) nguong = Integer.parseInt(a.substring(9));
        }
        for (int d = 0; d < 4; d++) {
            if (hang[d] < 0) {
                System.out.println("Thieu hang cho huong " + d + " (--len/--xuong/--trai/--phai)");
                return;
            }
        }

        final BufferedImage goc = ImageIO.read(sheet);
        if (goc == null) {
            System.out.println("Khong doc duoc anh: " + sheet);
            return;
        }
        final boolean[][] coHinh = CatSpriteSheet.locNen(goc, nguong, boTrai, 0);
        final List<int[]> hangs = CatSpriteSheet.timDai(coHinh, true);
        System.out.println("Bang " + goc.getWidth() + "x" + goc.getHeight()
                + ", tim thay " + hangs.size() + " hang");

        final BufferedImage dai = new BufferedImage(RONG_DAI, CAO, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = dai.createGraphics();

        for (int d = 0; d < 4; d++) {
            if (hang[d] >= hangs.size()) {
                System.out.println("Khong co hang " + hang[d]);
                return;
            }
            final int[] h = hangs.get(hang[d]);
            final List<int[]> cot = CatSpriteSheet.timCot(coHinh, h[0], h[1], -1, -1);
            if (cot.isEmpty()) {
                System.out.println("Hang " + hang[d] + " khong co frame nao");
                return;
            }
            // Ti le chung cho ca huong, lay theo frame cao nhat, de con thu khong
            // to nho phap phong giua cac frame.
            int caoNhat = 0;
            for (final int[] c : cot) {
                final int[] k = CatSpriteSheet.khungBao(coHinh, c[0], c[1], h[0], h[1]);
                if (k != null) caoNhat = Math.max(caoNhat, k[3] - k[1] + 1);
            }
            for (int f = 0; f < 4; f++) {
                // Frame thu 4 lap lai frame thu 2, dung dung cach cua bang preset.
                final int nguon = (f == 3) ? 1 : f;
                final int[] c = cot.get(Math.min(nguon, cot.size() - 1));
                final int[] k = CatSpriteSheet.khungBao(coHinh, c[0], c[1], h[0], h[1]);
                if (k == null) continue;
                final int oW = W[d][f];
                // Vua khung: khong duoc vuot ra ngoai o, neu khong se de len frame ben canh.
                final double tiLe = Math.min((double) CAO / caoNhat,
                        (double) oW / (k[2] - k[0] + 1));
                final BufferedImage nho = CatSpriteSheet.thuNho(goc, coHinh, k, tiLe);
                // Can giua o va dat sat day: client ve theo goc BOTTOM.
                g.drawImage(nho, X[d][f] + (oW - nho.getWidth()) / 2,
                        CAO - nho.getHeight(), null);
            }
            System.out.println("  huong " + d + ": hang " + hang[d] + ", " + cot.size()
                    + " frame nguon, khung cao nhat " + caoNhat);
        }
        g.dispose();

        final File thuMuc = new File("map/skinthu");
        thuMuc.mkdirs();
        final File pngTam = new File(thuMuc, ten + ".png");
        ImageIO.write(dai, "png", pngTam);
        final byte[] png = java.nio.file.Files.readAllBytes(pngTam.toPath());

        // Ca PNG vao file header, file data de rong: client noi header + data nen
        // ket qua van la dung file PNG do.
        try (FileOutputStream fo = new FileOutputStream(new File(thuMuc, ten + "0_header"))) {
            fo.write(png);
        }
        try (FileOutputStream fo = new FileOutputStream(new File(thuMuc, ten + "_data"))) {
            // rong
        }
        System.out.println("Xong: map/skinthu/" + ten + "0_header (" + png.length
                + " byte) + " + ten + "_data (0 byte)");
        System.out.println("Xem thu anh dai o map/skinthu/" + ten + ".png");
    }
}
