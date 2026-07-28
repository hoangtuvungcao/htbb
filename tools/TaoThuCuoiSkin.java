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

        // ---- Vong 1: lay khung bao cua tat ca 16 frame roi tinh MOT ti le chung ----
        // Truoc day tinh ti le rieng cho tung frame theo do rong o cua no, ma o huong
        // len/xuong chi rong 30-48px con huong ngang 60px, nen con thu o huong len/xuong
        // bi thu nho han -> nhin nhu doi size giua cac huong. Nay mot ti le cho tat ca:
        // lay muc nho nhat de khong frame nao tran ra khoi o cua no.
        final int[][][] khung = new int[4][4][];
        double tiLe = Double.MAX_VALUE;
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
            for (int f = 0; f < 4; f++) {
                // Frame thu 4 lap lai frame thu 2, dung dung cach cua bang preset.
                final int nguon = (f == 3) ? 1 : f;
                final int[] c = cot.get(Math.min(nguon, cot.size() - 1));
                final int[] k = CatSpriteSheet.khungBao(coHinh, c[0], c[1], h[0], h[1]);
                if (k == null) continue;
                khung[d][f] = k;
                tiLe = Math.min(tiLe, Math.min((double) CAO / (k[3] - k[1] + 1),
                        (double) W[d][f] / (k[2] - k[0] + 1)));
            }
            System.out.println("  huong " + d + ": hang " + hang[d] + ", "
                    + cot.size() + " frame nguon");
        }
        if (tiLe == Double.MAX_VALUE) {
            System.out.println("Khong lay duoc frame nao");
            return;
        }

        // ---- Vong 2: thu nho tat ca theo cung ti le roi dat vao dung o ----
        final BufferedImage dai = new BufferedImage(RONG_DAI, CAO, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = dai.createGraphics();
        int rongMax = 0, caoMax = 0;
        for (int d = 0; d < 4; d++) {
            for (int f = 0; f < 4; f++) {
                if (khung[d][f] == null) continue;
                final BufferedImage nho = CatSpriteSheet.thuNho(goc, coHinh, khung[d][f], tiLe);
                rongMax = Math.max(rongMax, nho.getWidth());
                caoMax = Math.max(caoMax, nho.getHeight());
                // Can giua o va dat sat day: client ve theo goc BOTTOM.
                g.drawImage(nho, X[d][f] + (W[d][f] - nho.getWidth()) / 2,
                        CAO - nho.getHeight(), null);
            }
        }
        g.dispose();
        System.out.println("Ti le chung " + String.format("%.3f", tiLe)
                + ", frame lon nhat " + rongMax + "x" + caoMax);

        final File thuMuc = new File("map/skinthu");
        thuMuc.mkdirs();
        final File pngTam = new File(thuMuc, ten + ".png");
        ImageIO.write(dai, "png", pngTam);
        final byte[] png = java.nio.file.Files.readAllBytes(pngTam.toPath());

        // Cat PNG lam hai phan. KHONG duoc de mot phan qua 32767 byte vi
        // OtherHandle gui do dai bang writeShort (co dau) — vuot nguong la client
        // doc ra so am roi vo ca goi anh, mat luon 5 anh skin.
        final int nua = (png.length + 1) / 2;
        if (png.length - nua > 32767 || nua > 32767) {
            System.out.println("PNG " + png.length + " byte, cat doi van vuot 32767 —"
                    + " phai giam kich thuoc anh.");
            return;
        }
        try (FileOutputStream fo = new FileOutputStream(new File(thuMuc, ten + "0_header"))) {
            fo.write(png, 0, nua);
        }
        try (FileOutputStream fo = new FileOutputStream(new File(thuMuc, ten + "_data"))) {
            fo.write(png, nua, png.length - nua);
        }
        System.out.println("Xong: " + ten + "0_header (" + nua + " byte) + "
                + ten + "_data (" + (png.length - nua) + " byte), tong " + png.length);
        System.out.println("Xem thu anh dai o map/skinthu/" + ten + ".png");
    }
}
