import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

/**
* Cat mot bang sprite (sprite sheet) thanh dai frame cho game.
 *
 * Bang sprite ve tay hoac ve bang AI thuong co: nen den, chu ghi ten hang o cot
 * trai, moi hang la mot huong (Idle / Run / Left / Right / Up / Down), moi hang
 * vai frame. Game thi can: nen TRONG SUOT, moi frame la mot o nho gon, xep thanh
 * mot dai doc, kem file data mo ta toa do tung o.
 *
 * Cong cu nay lam: tach nen den -> trong suot, do tim cac hang co hinh, trong
 * hang do tim tung frame, cat sat vien, thu nho ve dung co game, ghep thanh dai
 * doc, roi goi TaoDataSprite sinh file data.
 *
 *   javac -d out tools/TaoDataSprite.java tools/CatSpriteSheet.java
 *   java -cp out CatSpriteSheet <bang.png> <id dich> [tuy chon...]
 *
 *   --hang=1        chi lay hang thu may (0 = hang tren cung). Mac dinh 1 (thuong
 *                   la hang "Run"). Ghi --hang=tatca de lay het cac hang.
 *   --cao=32        chieu cao mong muon cua mot frame sau khi thu nho (px).
 *   --botrai=250    bo qua N px ben trai (vung chu "Idle/Run/Left..." va le).
 *   --botren=90     bo qua N px ben tren (vung tieu de).
 *   --nguong=48     do sang toi da con bi coi la nen (0..255). Nen den nhieu
 *                   nhieu hat thi tang len, nen sach thi giam xuong.
 *   --thu           chi in ra ket qua do tim, KHONG ghi file (chay thu truoc).
 *
 * Ket qua: map/d_effect/<id>.png + map/d_effect/<id>
 *
 * Luu y id phai < 300, vi DataGame chi nap map/d_effect/0..299.
 */
public class CatSpriteSheet {

    /** Do rong / cao toi thieu de mot vung duoc coi la frame that, khong phai chu hay bui. */
    private static final int TOI_THIEU = 10;

    public static void main(final String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Dung: java CatSpriteSheet <bang.png> <id dich> [--hang=1] "
                    + "[--cao=32] [--botrai=0] [--botren=0] [--nguong=48] [--thu]");
            return;
        }
        final File sheet = new File(args[0]);
        final int idDich = Integer.parseInt(args[1]);

        String hang = "1";
        int cao = 32, boTrai = 0, boTren = 0, nguong = 48, kheTay = -1, soFrameTay = -1;
        boolean chayThu = false, raPet = false, raAvt = false;
        for (int i = 2; i < args.length; i++) {
            final String a = args[i];
            if (a.startsWith("--hang=")) hang = a.substring(7);
            else if (a.startsWith("--cao=")) cao = Integer.parseInt(a.substring(6));
            else if (a.startsWith("--botrai=")) boTrai = Integer.parseInt(a.substring(9));
            else if (a.startsWith("--botren=")) boTren = Integer.parseInt(a.substring(9));
            else if (a.startsWith("--nguong=")) nguong = Integer.parseInt(a.substring(9));
            else if (a.startsWith("--khe=")) kheTay = Integer.parseInt(a.substring(6));
            else if (a.startsWith("--soframe=")) soFrameTay = Integer.parseInt(a.substring(10));
            else if (a.equals("--thu")) chayThu = true;
            else if (a.equals("--pet")) raPet = true;
            else if (a.equals("--avt")) raAvt = true;
        }

        final BufferedImage goc = ImageIO.read(sheet);
        if (goc == null) {
            System.out.println("Khong doc duoc anh: " + sheet);
            return;
        }
        System.out.println("Bang sprite: " + goc.getWidth() + "x" + goc.getHeight());

        final boolean[][] coHinh = locNen(goc, nguong, boTrai, boTren);
        final List<int[]> hangs = timDai(coHinh, true);
        System.out.println("Tim thay " + hangs.size() + " hang:");
        for (int i = 0; i < hangs.size(); i++) {
            final int[] h = hangs.get(i);
            final List<int[]> cot = timCot(coHinh, h[0], h[1], kheTay, soFrameTay);
            final StringBuilder sb = new StringBuilder();
            for (final int[] c : cot) sb.append(" [").append(c[0]).append("-").append(c[1]).append("]");
            System.out.println("  hang " + i + ": y " + h[0] + ".." + h[1]
                    + " (cao " + (h[1] - h[0] + 1) + "), "
                    + cot.size() + " frame:" + sb);
        }

        final List<Integer> chon = new ArrayList<>();
        if ("tatca".equalsIgnoreCase(hang)) {
            for (int i = 0; i < hangs.size(); i++) chon.add(i);
        } else {
            chon.add(Integer.parseInt(hang));
        }

        // Lay khung bao sat vien cua tung frame truoc, CHUA thu nho.
        final List<int[]> khung = new ArrayList<>();
        for (final int idx : chon) {
            if (idx < 0 || idx >= hangs.size()) {
                System.out.println("Khong co hang " + idx);
                return;
            }
            final int[] h = hangs.get(idx);
            for (final int[] c : timCot(coHinh, h[0], h[1], kheTay, soFrameTay)) {
                final int[] k = khungBao(coHinh, c[0], c[1], h[0], h[1]);
                if (k != null) khung.add(k);
            }
        }
        if (khung.isEmpty()) {
            System.out.println("Khong cat duoc frame nao. Thu chinh --nguong / --botrai / --botren.");
            return;
        }

        // Mot ti le thu nho DUY NHAT cho ca loat, lay theo frame cao nhat. Neu thu nho
        // rieng tung frame ve cung chieu cao thi con thu se to nho phap phong theo tung
        // khung — nhin nhu dang thoi phong len xep xuong.
        int caoNhat = 0, rongNhat = 0;
        for (final int[] k : khung) {
            caoNhat = Math.max(caoNhat, k[3] - k[1] + 1);
            rongNhat = Math.max(rongNhat, k[2] - k[0] + 1);
        }
        final double tiLe = (double) cao / caoNhat;
        final int rongMax = Math.max(1, (int) Math.ceil(rongNhat * tiLe));
        System.out.println("Cat duoc " + khung.size() + " frame, khung goc lon nhat "
                + rongNhat + "x" + caoNhat + " -> o " + rongMax + "x" + cao
                + " (ti le " + String.format("%.3f", tiLe) + ")");
        if (rongMax > 255 || cao > 255) {
            System.out.println("CANH BAO: canh > 255 px. Giam --cao.");
            return;
        }
        if (chayThu) {
            System.out.println("(--thu) khong ghi file.");
            return;
        }

        final BufferedImage dai = new BufferedImage(rongMax, cao * khung.size(),
                BufferedImage.TYPE_INT_ARGB);
        final java.awt.Graphics2D g = dai.createGraphics();
        for (int i = 0; i < khung.size(); i++) {
            final BufferedImage f = thuNho(goc, coHinh, khung.get(i), tiLe);
            // Dat sat day o va can giua: client ve pet theo goc BOTTOM|HCENTER nen
            // chan con thu phai nam dung day khung, khong thi no se lo lung.
            g.drawImage(f, (rongMax - f.getWidth()) / 2, (i + 1) * cao - f.getHeight(), null);
        }
        g.dispose();

        final File png = new File((raAvt ? "server/map/avtathu/" : raPet ? "server/map/pet/" : "server/map/d_effect/") + idDich + ".png");
        ImageIO.write(dai, "png", png);
        if (raPet || raAvt) {
            // Hinh pet KHONG can file data: client tu chia dai theo so frame
            // (AnimalMove.paint: hImg = anh.cao / sumFrame, ve can giua o day).
            System.out.println("Xong: " + png);
        } else {
            TaoDataSprite.ghiFileData(png, khung.size(), true);
            System.out.println("Xong: " + png + " va map/d_effect/" + idDich);
        }
        System.out.println("  -> so frame: " + khung.size());
    }

    /** true = diem co hinh (khong phai nen den, khong trong suot). */
    public static boolean[][] locNen(final BufferedImage img, final int nguong,
                                      final int boTrai, final int boTren) {
        final boolean[][] m = new boolean[img.getHeight()][img.getWidth()];
        for (int y = boTren; y < img.getHeight(); y++) {
            for (int x = boTrai; x < img.getWidth(); x++) {
                final int p = img.getRGB(x, y);
                if (((p >>> 24) & 0xFF) < 16) continue;
                final int r = (p >> 16) & 0xFF, gg = (p >> 8) & 0xFF, b = p & 0xFF;
                m[y][x] = (r * 30 + gg * 59 + b * 11) / 100 > nguong;
            }
        }
        return m;
    }

    /** Gom cac hang (hoac cot) lien tiep co hinh thanh tung dai. */
    public static List<int[]> timDai(final boolean[][] m, final boolean theoHang) {
        final int n = theoHang ? m.length : m[0].length;
        final int k = theoHang ? m[0].length : m.length;
        final List<int[]> ds = new ArrayList<>();
        int dau = -1;
        for (int i = 0; i < n; i++) {
            boolean co = false;
            for (int j = 0; j < k; j++) {
                if (theoHang ? m[i][j] : m[j][i]) { co = true; break; }
            }
            if (co && dau < 0) dau = i;
            if (!co && dau >= 0) {
                if (i - dau >= TOI_THIEU) ds.add(new int[]{dau, i - 1});
                dau = -1;
            }
        }
        if (dau >= 0 && n - dau >= TOI_THIEU) ds.add(new int[]{dau, n - 1});
        return ds;
    }

    /**
     * Cac cot co hinh trong pham vi hang y1..y2, da gop lai.
     *
     * Mot con thu thuong bi tach lam nhieu manh roi rac theo chieu ngang (bom toc,
     * duoi, chan truoc...) nen neu cu thay khoang trong la cat thi mot con se ra
     * hai ba frame. Vi vay hai manh cach nhau qua gan thi coi la cung mot frame.
     */
    public static List<int[]> timCot(final boolean[][] m, final int y1, final int y2,
                                      final int kheTay, final int soFrameTay) {
        final List<int[]> tho = new ArrayList<>();
        int dau = -1;
        for (int x = 0; x < m[0].length; x++) {
            boolean co = false;
            for (int y = y1; y <= y2; y++) {
                if (m[y][x]) { co = true; break; }
            }
            if (co && dau < 0) dau = x;
            if (!co && dau >= 0) {
                tho.add(new int[]{dau, x - 1});
                dau = -1;
            }
        }
        if (dau >= 0) tho.add(new int[]{dau, m[0].length - 1});
        if (tho.isEmpty()) return tho;

        // Neu biet truoc hang co bao nhieu frame thi chia deu — cach nay chac an nhat
        // voi bang ke o vuong vuc, ke ca khi hai con dung sat nhau khong co khe.
        if (soFrameTay > 0) {
            final int x0 = tho.get(0)[0], x1 = tho.get(tho.size() - 1)[1];
            final List<int[]> deu = new ArrayList<>();
            for (int i = 0; i < soFrameTay; i++) {
                deu.add(new int[]{x0 + (x1 - x0 + 1) * i / soFrameTay,
                        x0 + (x1 - x0 + 1) * (i + 1) / soFrameTay - 1});
            }
            return deu;
        }
        if (tho.size() < 2) return tho;

        // Nguong gop: neu khong chi dinh tay thi lay 40% khoang trong trung vi. Khe
        // giua hai con thu deu nhau nen nam quanh trung vi; khe trong long mot con
        // (giua bom toc va than chang han) nho hon han nen bi gop lai.
        int nguongGop = kheTay;
        if (nguongGop < 0) {
            final List<Integer> khoang = new ArrayList<>();
            for (int i = 1; i < tho.size(); i++) {
                khoang.add(tho.get(i)[0] - tho.get(i - 1)[1] - 1);
            }
            java.util.Collections.sort(khoang);
            nguongGop = khoang.get(khoang.size() / 2) * 2 / 5;
        }

        final List<int[]> ds = new ArrayList<>();
        for (final int[] b : tho) {
            if (!ds.isEmpty() && b[0] - ds.get(ds.size() - 1)[1] - 1 <= nguongGop) {
                ds.get(ds.size() - 1)[1] = b[1];
            } else {
                ds.add(new int[]{b[0], b[1]});
            }
        }
        ds.removeIf(b -> b[1] - b[0] + 1 < TOI_THIEU);
        return ds;
    }

    /** Khung bao sat vien cua mot frame: {x1, y1, x2, y2}. null neu qua nho. */
    public static int[] khungBao(final boolean[][] m, final int x1, final int x2,
                                  final int y1, final int y2) {
        int tx1 = x2, tx2 = x1, ty1 = y2, ty2 = y1;
        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                if (!m[y][x]) continue;
                if (x < tx1) tx1 = x;
                if (x > tx2) tx2 = x;
                if (y < ty1) ty1 = y;
                if (y > ty2) ty2 = y;
            }
        }
        if (tx2 - tx1 + 1 < TOI_THIEU || ty2 - ty1 + 1 < TOI_THIEU) return null;
        return new int[]{tx1, ty1, tx2, ty2};
    }

    /**
     * Cat khung ra khoi bang, bo nen, thu nho theo ti le chung.
     * Sau khi thu nho thi don lai kenh trong suot: duoi 40 cho hang trong suot han,
     * tren 200 cho dac han — de vien khong bi mo nhoe kieu anh chup.
     */
    public static BufferedImage thuNho(final BufferedImage goc, final boolean[][] m,
                                        final int[] k, final double tiLe) {
        final int w = k[2] - k[0] + 1, h = k[3] - k[1] + 1;
        final BufferedImage cat = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (m[k[1] + y][k[0] + x]) {
                    cat.setRGB(x, y, goc.getRGB(k[0] + x, k[1] + y) | 0xFF000000);
                }
            }
        }
        final int rongDich = Math.max(1, (int) Math.round(w * tiLe));
        final int caoDich = Math.max(1, (int) Math.round(h * tiLe));
        // Ti le 1: TRA VE Y NGUYEN, khong cho di qua getScaledInstance. Bo loc AREA_AVERAGING
        // vẫn lam mem vien du kich thuoc khong doi, ma sprite pixel-art thi vien mem la
        // nhin "mo mo" ngay.
        if (rongDich == w && caoDich == h) {
            return cat;
        }
        final BufferedImage nho = new BufferedImage(rongDich, caoDich, BufferedImage.TYPE_INT_ARGB);
        final java.awt.Graphics2D g = nho.createGraphics();
        g.drawImage(cat.getScaledInstance(rongDich, caoDich, Image.SCALE_AREA_AVERAGING), 0, 0, null);
        g.dispose();
        for (int y = 0; y < caoDich; y++) {
            for (int x = 0; x < rongDich; x++) {
                final int p = nho.getRGB(x, y);
                final int a = (p >>> 24) & 0xFF;
                if (a < 40) nho.setRGB(x, y, 0);
                else if (a > 200) nho.setRGB(x, y, p | 0xFF000000);
            }
        }
        return nho;
    }
}
