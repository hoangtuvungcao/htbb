package server.quantri;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

/**
 * Nap anh Pokemon (PMD Collab) cho bang dieu khien - AN TOAN: thieu tep / tep hong -> anh ve
 * san (Poke Ball), khong bao gio nem loi. Co bo nho dem (moi anh doc dia mot lan).
 *
 * Thu tu tim chan dung theo so Pokedex:
 *   1. client_pc_ios/Assets/Resources/poke/NNNN_cd.bytes (PNG 40x40 - chan dung client dung)
 *   2. tools/pokemon/pmd_raw_icon/Portrait/NNNN.png
 *   3. khung dau cua tools/pokemon/pmd_raw/NNNN/Idle-Anim.png (kich thuoc khung doc tu AnimData.xml)
 *   4. Poke Ball ve bang Java2D.
 * Icon vat pham (thanh ben): tools/pokemon/pmd_raw_icon/Item/&lt;Ten&gt;.None.png, roi map/icon_pokemon/N.png.
 *
 * Thu muc goc: thu muc lam viec cua server (canh server.ini), hoac -Dhtbb.goc=...
 */
public final class AnhPokemon {

    private static final Map<String, BufferedImage> DEM = new ConcurrentHashMap<String, BufferedImage>();
    private static final BufferedImage KHONG = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    private AnhPokemon() {
    }

    static List<File> dsGoc() {
        final List<File> l = new ArrayList<File>();
        final String ep = System.getProperty("htbb.goc");
        if (ep != null && !ep.trim().isEmpty()) {
            l.add(new File(ep.trim()));
        }
        final File cwd = new File(".").getAbsoluteFile();
        l.add(cwd);
        if (cwd.getParentFile() != null) {
            l.add(cwd.getParentFile());
        }
        return l;
    }

    static File tim(final String duongDan) {
        for (final File g : dsGoc()) {
            final File f = new File(g, duongDan);
            if (f.isFile()) {
                return f;
            }
        }
        return null;
    }

    static BufferedImage doc(final File f) {
        if (f == null) {
            return null;
        }
        try {
            final BufferedImage a = ImageIO.read(f);
            if (a == null || a.getWidth() <= 0 || a.getWidth() > 4096 || a.getHeight() > 4096) {
                return null;
            }
            return a;
        } catch (final Throwable t) {
            return null;
        }
    }

    /** Chan dung Pokemon theo so Pokedex (40x40 hoac khung sprite), null-safe: luon tra anh. */
    public static BufferedImage chanDung(final int dex) {
        final String k = "cd:" + dex;
        BufferedImage a = DEM.get(k);
        if (a != null) {
            return a == KHONG ? pokeBall(40) : a;
        }
        a = napChanDung(dex);
        DEM.put(k, a == null ? KHONG : a);
        return a == null ? pokeBall(40) : a;
    }

    /** Co chan dung that (khong phai Poke Ball thay the) khong. */
    public static boolean coChanDung(final int dex) {
        chanDung(dex);
        return DEM.get("cd:" + dex) != KHONG;
    }

    static BufferedImage napChanDung(final int dex) {
        if (dex <= 0 || dex > 2000) {
            return null;
        }
        final String so = String.format("%04d", dex);
        BufferedImage a = doc(tim("client_pc_ios/Assets/Resources/poke/" + so + "_cd.bytes"));
        if (a == null) {
            a = doc(tim("tools/pokemon/pmd_raw_icon/Portrait/" + so + ".png"));
        }
        if (a == null) {
            a = khungDung(dex);
        }
        return a;
    }

    /** Khung dau (huong xuong) cua hoat anh dung yen, cat sat vung co diem anh. */
    public static BufferedImage khungDung(final int dex) {
        final String so = String.format("%04d", dex);
        final BufferedImage tam = doc(tim("tools/pokemon/pmd_raw/" + so + "/Idle-Anim.png"));
        if (tam == null) {
            return null;
        }
        int fw = 0, fh = 0;
        try {
            final File x = tim("tools/pokemon/pmd_raw/" + so + "/AnimData.xml");
            if (x != null) {
                final String s = new String(Files.readAllBytes(x.toPath()), StandardCharsets.UTF_8);
                final Matcher m = Pattern.compile("<Name>Idle</Name>.*?<FrameWidth>(\\d+)</FrameWidth>\\s*<FrameHeight>(\\d+)</FrameHeight>",
                        Pattern.DOTALL).matcher(s);
                if (m.find()) {
                    fw = Integer.parseInt(m.group(1));
                    fh = Integer.parseInt(m.group(2));
                }
            }
        } catch (final Throwable ignored) {
        }
        if (fw <= 0 || fh <= 0 || fw > tam.getWidth() || fh > tam.getHeight()) {
            fh = tam.getHeight() / 8;
            fw = fh;
        }
        return catSat(tam.getSubimage(0, 0, Math.min(fw, tam.getWidth()), Math.min(fh, tam.getHeight())));
    }

    /** Cat bo vien trong suot. */
    static BufferedImage catSat(final BufferedImage a) {
        int x0 = a.getWidth(), y0 = a.getHeight(), x1 = -1, y1 = -1;
        for (int y = 0; y < a.getHeight(); y++) {
            for (int x = 0; x < a.getWidth(); x++) {
                if ((a.getRGB(x, y) >>> 24) > 16) {
                    x0 = Math.min(x0, x);
                    y0 = Math.min(y0, y);
                    x1 = Math.max(x1, x);
                    y1 = Math.max(y1, y);
                }
            }
        }
        if (x1 < 0) {
            return null;
        }
        final int w = x1 - x0 + 1, h = y1 - y0 + 1, c = Math.max(w, h);
        final BufferedImage r = new BufferedImage(c, c, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = r.createGraphics();
        g.drawImage(a.getSubimage(x0, y0, w, h), (c - w) / 2, c - h, null);
        g.dispose();
        return r;
    }

    /** Icon vat pham PMD theo ten (vd "Orb_Blue", "Apple_Red"); null-safe. */
    public static BufferedImage vatPham(final String ten) {
        final String k = "vp:" + ten;
        BufferedImage a = DEM.get(k);
        if (a != null) {
            return a == KHONG ? pokeBall(16) : a;
        }
        a = doc(tim("tools/pokemon/pmd_raw_icon/Item/" + ten + ".None.png"));
        DEM.put(k, a == null ? KHONG : a);
        return a == null ? pokeBall(16) : a;
    }

    /** Icon tu dai map/icon_pokemon (so thu tu n). */
    public static BufferedImage iconBang(final int n) {
        final String k = "ib:" + n;
        BufferedImage a = DEM.get(k);
        if (a != null) {
            return a == KHONG ? pokeBall(16) : a;
        }
        a = doc(tim("map/icon_pokemon/" + n + ".png"));
        DEM.put(k, a == null ? KHONG : a);
        return a == null ? pokeBall(16) : a;
    }

    /** Poke Ball ve san (khong can tep). */
    public static BufferedImage pokeBall(final int co) {
        final String k = "pb:" + co;
        final BufferedImage dem = DEM.get(k);
        if (dem != null) {
            return dem;
        }
        final BufferedImage a = new BufferedImage(co, co, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = a.createGraphics();
        vePokeBall(g, 0, 0, co, false);
        g.dispose();
        DEM.put(k, a);
        return a;
    }

    /** Ve Poke Ball tai (x,y) duong kinh d. mo = ban mo (trang tri nen). */
    public static void vePokeBall(final Graphics2D g0, final float x, final float y, final float d, final boolean mo) {
        final Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        final float vien = Math.max(1f, d * 0.07f);
        final Ellipse2D.Float tron = new Ellipse2D.Float(x + vien / 2, y + vien / 2, d - vien, d - vien);
        if (mo) {
            g.setColor(new Color(255, 255, 255, 34));
            g.setStroke(new BasicStroke(vien * 1.4f));
            g.draw(tron);
            g.fill(new java.awt.geom.Rectangle2D.Float(x, y + d / 2 - vien * 0.7f, d, vien * 1.4f));
            g.fill(new Ellipse2D.Float(x + d * 0.36f, y + d * 0.36f, d * 0.28f, d * 0.28f));
            g.dispose();
            return;
        }
        g.setClip(tron);
        g.setPaint(new GradientPaint(x, y, new Color(0xFF5A3C), x, y + d / 2, GiaoDien.DO));
        g.fill(new java.awt.geom.Rectangle2D.Float(x, y, d, d / 2));
        g.setPaint(new GradientPaint(x, y + d / 2, Color.WHITE, x, y + d, new Color(0xDCDDE3)));
        g.fill(new java.awt.geom.Rectangle2D.Float(x, y + d / 2, d, d / 2));
        g.setClip(null);
        g.setColor(new Color(0x22252E));
        g.setStroke(new BasicStroke(vien));
        g.draw(tron);
        g.fill(new java.awt.geom.Rectangle2D.Float(x + vien / 2, y + d / 2 - vien * 0.75f, d - vien, vien * 1.5f));
        final float nd = d * 0.34f;
        g.fill(new Ellipse2D.Float(x + (d - nd) / 2, y + (d - nd) / 2, nd, nd));
        g.setColor(Color.WHITE);
        final float nt = d * 0.2f;
        g.fill(new Ellipse2D.Float(x + (d - nt) / 2, y + (d - nt) / 2, nt, nt));
        g.dispose();
    }

    /** Phong to anh pixel giu net (lang gieng gan nhat), giu ti le, can giua trong o w x h. */
    public static BufferedImage phongTo(final BufferedImage a, final int w, final int h) {
        final BufferedImage r = new BufferedImage(Math.max(1, w), Math.max(1, h), BufferedImage.TYPE_INT_ARGB);
        if (a == null) {
            return r;
        }
        final Graphics2D g = r.createGraphics();
        final boolean pixel = a.getWidth() <= 64;
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, pixel ? RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
                : RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        final double tl = Math.min(w / (double) a.getWidth(), h / (double) a.getHeight());
        final int dw = (int) Math.round(a.getWidth() * tl), dh = (int) Math.round(a.getHeight() * tl);
        g.drawImage(a, (w - dw) / 2, (h - dh) / 2, dw, dh, null);
        g.dispose();
        return r;
    }

    /** Anh tron (chan dung trong vong tron) - dung cho bang nguoi choi. */
    public static BufferedImage tron(final BufferedImage a, final int co, final Color nen) {
        final BufferedImage r = new BufferedImage(co, co, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = r.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(nen);
        g.fill(new Ellipse2D.Float(0, 0, co, co));
        g.setClip(new Ellipse2D.Float(0, 0, co, co));
        g.drawImage(phongTo(a, co, co), 0, 0, null);
        g.dispose();
        return r;
    }

    /** So anh dang trong bo nho dem (kiem thu). */
    static int soDem() {
        return DEM.size();
    }
}
