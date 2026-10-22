import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * Doi SPRITE TRAN DAU (anh truoc / sau 96x96, bo Smogon Sprite Project / Black-White goc - du_lieu/loai/sprite_ngoai.csv)
 * thanh THU MUC PMD (AnimData.xml + &lt;Anim&gt;-Anim.png + -Shadow.png + portrait.png + credits.txt + NHAP.txt) de
 * DongGoiLoai dong goi nhu moi loai khac (tools/pokemon/pmd_raw/&lt;NNNN&gt;[s]/).
 *
 *   javac -d $B tools/pokemon/loai/ChuyenSpriteChienDau.java
 *   java -cp $B ChuyenSpriteChienDau &lt;thu muc anh: gen5/ gen5-back/ gen5-shiny/ gen5-back-shiny/&gt;
 *
 * GIU NGUYEN DIEM ANH: chi cat hop bao, THU NHO lang gieng gan nhat (nearest neighbour, khong phong to) ve co than PMD
 * theo chieu cao loai (12 + 20 * sqrt(m), 16..64 px), dat chan o giua day khung. 8 hang huong PMD: xuong / xuong-trai /
 * trai = anh truoc; xuong-phai / phai = anh truoc lat guong; len / len-phai = anh sau; len-trai = anh sau lat guong.
 * Idle = 2 khung (nhun 1 px), Walk = 4 khung (nhun 0, -1, 0, -1); danh / trung don / guc nga dung lai khung (CopyOf).
 * Offsets.png sinh tu diem anh: dau = diem dac tren cung o 1/3 giua, tam = diem dac gan tam nhat (TinhNeoPoke doc nhu PMD);
 * client tinh tiep neo trang phuc tu diem anh (NeoTrangPhuc).
 * Anh dau vao la du lieu KHONG tin cay: chi giai ma PNG bang ImageIO, kiem co 96x96.
 */
public final class ChuyenSpriteChienDau {

    static final int[] NHUN_IDLE = {0, -1};
    static final int[] NHUN_WALK = {0, -1, 0, -1};

    private ChuyenSpriteChienDau() {
    }

    public static void main(final String[] a) throws Exception {
        final File vao = new File(a[0]);
        final File goc = new File(".").getCanonicalFile();
        final File raw = new File(goc, "tools/pokemon/pmd_raw");
        final List<String> dong = Files.readAllLines(new File(goc, "du_lieu/loai/sprite_ngoai.csv").toPath(), StandardCharsets.UTF_8);
        int so = 0, loi = 0;
        for (int i = 1; i < dong.size(); i++) {
            final String[] p = tach(dong.get(i));
            final int dex = Integer.parseInt(p[0]);
            final String ten = p[2], nguon = p[3], nguoi = p[4];
            final double m = Integer.parseInt(p[5]) / 10.0;
            for (int sh = 0; sh < 2; sh++) {
                final String k = String.format("%04d", dex) + (sh == 1 ? "s" : "");
                try {
                    final BufferedImage tr = doc(new File(vao, (sh == 1 ? "gen5-shiny/" : "gen5/") + ten + ".png"));
                    final BufferedImage sa = doc(new File(vao, (sh == 1 ? "gen5-back-shiny/" : "gen5-back/") + ten + ".png"));
                    final File tm = new File(raw, k);
                    tm.mkdirs();
                    chuyen(tr, sa, m, tm);
                    final File cd = new File(goc, "web/public/pokemon/loai/" + dex + ".png");   // chan dung PMDCollab
                    Files.copy(cd.toPath(), new File(tm, "portrait.png").toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    try (PrintWriter w = new PrintWriter(new File(tm, "credits.txt"), "UTF-8")) {
                        for (final String n : nguoi.split(",")) {
                            w.print("2026-10-22 00:00:00.000000\t" + n.trim() + "\tCUR\t" + nguon + "\tIdle,Walk\n");
                        }
                    }
                    try (PrintWriter w = new PrintWriter(new File(tm, "NHAP.txt"), "UTF-8")) {
                        w.print("nguon=" + nguon + "\nten=" + p[1] + "\nban_nhap=1\nmuon=Attack=Walk;Shoot=Idle;Hurt=Idle;Faint=Idle\n"
                                + "url=" + p[6] + "\n");
                    }
                    so++;
                } catch (final Exception e) {
                    loi++;
                    System.out.println("LOI " + k + ": " + e.getMessage());
                }
            }
        }
        System.out.println("Da doi " + so + " thu muc PMD (thuong + shiny) tu sprite tran dau, loi " + loi);
        System.exit(loi == 0 ? 0 : 1);
    }

    static String[] tach(final String s) {
        final java.util.List<String> r = new java.util.ArrayList<String>();
        final StringBuilder b = new StringBuilder();
        boolean q = false;
        for (final char c : s.toCharArray()) {
            if (c == '"') {
                q = !q;
            } else if (c == ',' && !q) {
                r.add(b.toString());
                b.setLength(0);
            } else {
                b.append(c);
            }
        }
        r.add(b.toString());
        return r.toArray(new String[0]);
    }

    static BufferedImage doc(final File f) throws Exception {
        final BufferedImage b = ImageIO.read(f);
        if (b == null || b.getWidth() != 96 || b.getHeight() != 96) {
            throw new Exception("khong phai PNG 96x96: " + f);
        }
        final BufferedImage r = new BufferedImage(96, 96, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 96; y++) {
            for (int x = 0; x < 96; x++) {
                final int c = b.getRGB(x, y);
                r.setRGB(x, y, (c >>> 24) >= 128 ? (c | 0xFF000000) : 0);   // nguong alpha nhu DongGoiPokeJ2me
            }
        }
        return r;
    }

    /** Cat hop bao diem dac. */
    static BufferedImage cat(final BufferedImage b) {
        int x0 = 96, y0 = 96, x1 = -1, y1 = -1;
        for (int y = 0; y < b.getHeight(); y++) {
            for (int x = 0; x < b.getWidth(); x++) {
                if ((b.getRGB(x, y) >>> 24) != 0) {
                    x0 = Math.min(x0, x);
                    y0 = Math.min(y0, y);
                    x1 = Math.max(x1, x);
                    y1 = Math.max(y1, y);
                }
            }
        }
        return x1 < 0 ? new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB) : b.getSubimage(x0, y0, x1 - x0 + 1, y1 - y0 + 1);
    }

    /** Thu nho lang gieng gan nhat (s <= 1). */
    static BufferedImage thu(final BufferedImage b, final double s) {
        final int w = Math.max(1, (int) Math.round(b.getWidth() * s)), h = Math.max(1, (int) Math.round(b.getHeight() * s));
        final BufferedImage r = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                r.setRGB(x, y, b.getRGB(Math.min(b.getWidth() - 1, (int) (x / s)), Math.min(b.getHeight() - 1, (int) (y / s))));
            }
        }
        return r;
    }

    static BufferedImage lat(final BufferedImage b) {
        final BufferedImage r = new BufferedImage(b.getWidth(), b.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < b.getHeight(); y++) {
            for (int x = 0; x < b.getWidth(); x++) {
                r.setRGB(b.getWidth() - 1 - x, y, b.getRGB(x, y));
            }
        }
        return r;
    }

    static void chuyen(final BufferedImage truoc, final BufferedImage sau, final double m, final File tm) throws Exception {
        BufferedImage t = cat(truoc), s = cat(sau);
        final int mucTieu = (int) Math.max(16, Math.min(64, Math.round(12 + 20 * Math.sqrt(Math.max(0.1, m)))));
        final double k = Math.min(1.0, mucTieu / (double) t.getHeight());
        if (k < 1.0) {
            t = thu(t, k);
            s = thu(s, k);
        }
        final int w = chan(Math.max(t.getWidth(), s.getWidth()) + 8), h = chan(Math.max(t.getHeight(), s.getHeight()) + 8);
        // hang PMD: 0 xuong, 1 xuong-phai, 2 phai, 3 len-phai, 4 len, 5 len-trai, 6 trai, 7 xuong-trai
        final BufferedImage tl = lat(t), sl = lat(s);
        final BufferedImage[] hang = {t, tl, tl, s, s, sl, t, t};
        ghiAnim(tm, "Idle", hang, w, h, NHUN_IDLE);
        ghiAnim(tm, "Walk", hang, w, h, NHUN_WALK);
        try (PrintWriter x = new PrintWriter(new File(tm, "AnimData.xml"), "UTF-8")) {
            x.print("<?xml version='1.0' encoding='utf-8'?>\n<AnimData>\n  <ShadowSize>1</ShadowSize>\n  <Anims>\n");
            anim(x, "Walk", 0, w, h, 4, 8);
            anim(x, "Idle", 7, w, h, 2, 30);
            copy(x, "Attack", "Walk");
            copy(x, "Shoot", "Idle");
            copy(x, "Hurt", "Idle");
            copy(x, "Faint", "Idle");
            x.print("  </Anims>\n</AnimData>\n");
        }
    }

    static int chan(final int v) {
        return v + (v & 1);
    }

    static void anim(final PrintWriter x, final String ten, final int idx, final int w, final int h, final int n, final int d) {
        x.print("    <Anim>\n      <Name>" + ten + "</Name>\n      <Index>" + idx + "</Index>\n      <FrameWidth>" + w
                + "</FrameWidth>\n      <FrameHeight>" + h + "</FrameHeight>\n      <Durations>\n");
        for (int i = 0; i < n; i++) {
            x.print("        <Duration>" + d + "</Duration>\n");
        }
        x.print("      </Durations>\n    </Anim>\n");
    }

    static void copy(final PrintWriter x, final String ten, final String goc) {
        x.print("    <Anim>\n      <Name>" + ten + "</Name>\n      <CopyOf>" + goc + "</CopyOf>\n    </Anim>\n");
    }

    /** Anh dong tac (8 hang x n khung) + Shadow (diem chan o giua day than, moi khung). */
    static void ghiAnim(final File tm, final String ten, final BufferedImage[] hang, final int w, final int h, final int[] nhun)
            throws Exception {
        final int n = nhun.length;
        final BufferedImage a = new BufferedImage(w * n, h * 8, BufferedImage.TYPE_INT_ARGB);
        final BufferedImage b = new BufferedImage(w * n, h * 8, BufferedImage.TYPE_INT_ARGB);
        final BufferedImage o = new BufferedImage(w * n, h * 8, BufferedImage.TYPE_INT_ARGB);
        for (int r = 0; r < 8; r++) {
            final BufferedImage s = hang[r];
            for (int f = 0; f < n; f++) {
                final int ox = f * w + (w - s.getWidth()) / 2, oy = r * h + h - 4 - s.getHeight() + nhun[f];
                for (int y = 0; y < s.getHeight(); y++) {
                    for (int x = 0; x < s.getWidth(); x++) {
                        final int c = s.getRGB(x, y);
                        if ((c >>> 24) != 0) {
                            a.setRGB(ox + x, oy + y, c);
                        }
                    }
                }
                for (int x = -1; x <= 1; x++) {
                    b.setRGB(f * w + w / 2 + x, r * h + h - 4, 0xFF000000);
                }
                // Offsets PMD: DEN = dau (diem dac tren cung o cot giua than), XANH LA = tam (diem dac gan tam hop bao nhat)
                final int[] dau = dinhGiua(s), tam = ganTam(s);
                o.setRGB(ox + dau[0], oy + dau[1], 0xFF000000);
                o.setRGB(ox + tam[0], oy + tam[1], 0xFF00FF00);
            }
        }
        ImageIO.write(a, "png", new File(tm, ten + "-Anim.png"));
        ImageIO.write(b, "png", new File(tm, ten + "-Shadow.png"));
        ImageIO.write(o, "png", new File(tm, ten + "-Offsets.png"));
    }

    static boolean dac(final BufferedImage s, final int x, final int y) {
        return x >= 0 && y >= 0 && x < s.getWidth() && y < s.getHeight() && (s.getRGB(x, y) >>> 24) != 0;
    }

    /** Diem dac tren cung trong 1/3 giua be ngang (dau / dinh than nhin thay duoc). */
    static int[] dinhGiua(final BufferedImage s) {
        final int w = s.getWidth(), a = w / 3, b = Math.max(a, w - 1 - w / 3);
        for (int y = 0; y < s.getHeight(); y++) {
            int tot = -1;
            for (int x = a; x <= b; x++) {
                if (dac(s, x, y) && (tot < 0 || Math.abs(x - w / 2) < Math.abs(tot - w / 2))) {
                    tot = x;
                }
            }
            if (tot >= 0) {
                return new int[]{tot, y};
            }
        }
        return ganTam(s);
    }

    /** Diem dac gan tam hop bao nhat. */
    static int[] ganTam(final BufferedImage s) {
        final int cx = s.getWidth() / 2, cy = s.getHeight() / 2;
        int[] kq = {cx, cy};
        long tot = Long.MAX_VALUE;
        for (int y = 0; y < s.getHeight(); y++) {
            for (int x = 0; x < s.getWidth(); x++) {
                if (dac(s, x, y)) {
                    final long d = (long) (x - cx) * (x - cx) + (long) (y - cy) * (y - cy);
                    if (d < tot) {
                        tot = d;
                        kq = new int[]{x, y};
                    }
                }
            }
        }
        return kq;
    }
}
