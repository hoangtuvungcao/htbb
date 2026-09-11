import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Thu cho ngoi tren thu cuoi tool: ve con thu + NGUOI NGOI (hinh que) DUNG THU TU VE cua
 * client, o ca bon huong, roi in so do de sua cho dung thay vi chinh mo.
 *
 * Thu tu ve (Client/Char.cs paint, nhanh orderPaint == 1):
 *   - xuong / trai / phai : THAN nguoi ve truoc -> con thu (tren0 roi tren1) -> DAU nguoi sau
 *   - len                 : con thu -> than -> dau
 * Nen o ba huong dau, bat cu manh thu nao nam trong cot nguoi la ve DE LEN nguoi (do la ly
 * do phai doi soi duoi cua Kim Cang Long ra mep than, xem docs/skin-linh-thu.md muc 9).
 *
 * Huong PHAI: client lat guong khung trai (DataSkillEff.paintTopHorse voi rotale = 2 lam
 * dx' = -(dx + w) va dao co lat), o day lam y het. Nguoi: chan tai (x + DX, y + DY) lay tu
 * dong bang dx_horse_tool / dy_horse_tool duoc thu; KHONG ve chan (client bo chan khi cuoi
 * thu tool); than = mong (chan-12) len toi chan-30, dau tron o chan-41..-29.
 *
 * Ngoai anh, in ra: khung bao con thu, vi tri nguoi, vung DO/CAM (yen) do bang mau, va bao
 * nhieu % than nguoi bi thu che - so cuoi la thu can nhin: xuong/ngang thi thu che den mong
 * la vua, che len toi vai la "nguoi bi nuot".
 *
 * Tham so (moi so la pixel, am = len / sang trai):
 *   ra.png  tep-du-lieu  dxX,dxL,dxT  dyX,dyL,dyT  shX,shL,shT  [shdxT] [phong-to] [anh:dx]
 *     dx / dy : bang bu nguoi theo huong xuong / len / trai (phai = trai lat: dx doi dau)
 *     sh      : DICH dy cua ca khoi khung (xuong / len / ngang) trong bo nho, KHONG ghi tep
 *     shdxT   : dich dx khoi ngang
 *     anh:dx  : dich rieng vai anh con, vd "2,5,10:20" (dung de tach mot bo phan ra)
 *
 * Ung so nao roi thi ghi vao tep bang tools/DichSkinThu, con dong bang dx/dy thi sua
 * Char.dx_horse_tool / dy_horse_tool va SkinLinhThu.INDEX_DXY.
 *
 *   javac -encoding UTF-8 -d build/tools-classes tools/ThuChoNgoiSkinThu.java
 *   java -cp build/tools-classes ThuChoNgoiSkinThu thu.png map/skinthu/5 "0,0,1" "-23,-15,-15" "0,0,0" 0 5
 */
public final class ThuChoNgoiSkinThu {

    /** Char.getFrameHorse: xuong +0, len +7, ngang +14; slot 0 dung yen, 2..4 di. */
    private static final int[] KHUNG_DAU = {0, 7, 14, 14};
    private static final String[] TEN_HUONG = {"xuong", "len", "trai", "phai"};
    private static final int[] SLOT = {0, 2, 3};

    private static final class AnhCon { int x, y, w, h; }
    private static final class Manh { int dx, dy, idAnh, lat, tren; }

    public static void main(final String[] a) throws Exception {
        if (a.length < 5) {
            System.out.println("Dung: java ThuChoNgoiSkinThu ra.png <tep> dxX,dxL,dxT dyX,dyL,dyT shX,shL,shT [shdxT] [phong-to] [anh:dx]");
            System.exit(2);
        }
        final String ra = a[0];
        final String duong = a[1];
        final int[] dx3 = so3(a[2]);
        final int[] dy3 = so3(a[3]);
        final int[] sh3 = so3(a[4]);
        final int shdxT = a.length > 5 ? Integer.parseInt(a[5]) : 0;
        final int tl = a.length > 6 ? Integer.parseInt(a[6]) : 5;
        final java.util.Set<Integer> anhDich = new java.util.HashSet<Integer>();
        int dxAnhDich = 0;
        if (a.length > 7 && a[7].contains(":")) {
            final String[] p = a[7].split(":");
            for (final String s : p[0].split(",")) {
                anhDich.add(Integer.valueOf(s.trim()));
            }
            dxAnhDich = Integer.parseInt(p[1].trim());
        }
        final int[] dx = {dx3[0], dx3[1], dx3[2], -dx3[2]};
        final int[] dy = {dy3[0], dy3[1], dy3[2], dy3[2]};

        final BufferedImage atlas = ImageIO.read(new File(duong + ".png"));
        final List<AnhCon> anh = new ArrayList<AnhCon>();
        final List<List<Manh>> khung = new ArrayList<List<Manh>>();
        doc(duong, anh, khung);
        for (int f = 0; f < khung.size(); f++) {
            final int kh = f < 7 ? 0 : (f < 14 ? 1 : 2);
            for (final Manh m : khung.get(f)) {
                m.dy += sh3[kh];
                if (kh == 2) {
                    m.dx += shdxT;
                }
                if (anhDich.contains(Integer.valueOf(m.idAnh))) {
                    m.dx += dxAnhDich;
                }
            }
        }

        final int oRong = 140, oCao = 130, gocX = 70, gocY = 100;
        final BufferedImage out = new BufferedImage(oRong * tl * 4, oCao * tl * SLOT.length, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = out.createGraphics();
        g.setColor(new Color(0x30, 0x34, 0x3C));
        g.fillRect(0, 0, out.getWidth(), out.getHeight());
        g.setFont(new Font("Consolas", Font.PLAIN, 13));

        System.out.println("=== " + duong + "  DX " + java.util.Arrays.toString(dx) + " DY " + java.util.Arrays.toString(dy)
                + " dich khoi dy " + java.util.Arrays.toString(sh3) + " dich dx ngang " + shdxT
                + (anhDich.isEmpty() ? "" : " dich anh " + anhDich + " dx " + dxAnhDich) + " ===");
        for (int si = 0; si < SLOT.length; si++) {
            for (int h = 0; h < 4; h++) {
                final int f = KHUNG_DAU[h] + SLOT[si];
                if (f >= khung.size()) {
                    continue;
                }
                final boolean phai = (h == 3);
                final int nx = gocX + dx[h];
                final int chan = gocY + dy[h];

                // Anh day du (thu + nguoi) theo dung thu tu ve.
                final BufferedImage o = new BufferedImage(oRong, oCao, BufferedImage.TYPE_INT_ARGB);
                final Graphics2D go = o.createGraphics();
                if (h != 1) {
                    veThan(go, nx, chan);
                }
                ve(go, atlas, anh, khung.get(f), gocX, gocY, phai);
                if (h == 1) {
                    veThan(go, nx, chan);
                }
                veDau(go, nx, chan);
                go.dispose();

                // Rieng con thu, de do.
                final BufferedImage chi = new BufferedImage(oRong, oCao, BufferedImage.TYPE_INT_ARGB);
                final Graphics2D gc = chi.createGraphics();
                ve(gc, atlas, anh, khung.get(f), gocX, gocY, phai);
                gc.dispose();
                int tx0 = 999, tx1 = -999, ty0 = 999, ty1 = -999;
                int yx0 = 999, yx1 = -999, yy0 = 999, yy1 = -999, soDo = 0;
                for (int yy = 0; yy < oCao; yy++) {
                    for (int xx = 0; xx < oRong; xx++) {
                        final int p = chi.getRGB(xx, yy);
                        if ((p >>> 24) < 128) {
                            continue;
                        }
                        tx0 = Math.min(tx0, xx); tx1 = Math.max(tx1, xx);
                        ty0 = Math.min(ty0, yy); ty1 = Math.max(ty1, yy);
                        final int r = (p >> 16) & 255, gg = (p >> 8) & 255, b = p & 255;
                        if (r > 150 && b < 100 && gg < 160 && r - b > 80) {
                            soDo++;
                            yx0 = Math.min(yx0, xx); yx1 = Math.max(yx1, xx);
                            yy0 = Math.min(yy0, yy); yy1 = Math.max(yy1, yy);
                        }
                    }
                }
                int che = 0, tongT = 0;
                for (int yy = chan - 30; yy < chan - 12; yy++) {
                    for (int xx = nx - 6; xx <= nx + 6; xx++) {
                        tongT++;
                        if (xx >= 0 && yy >= 0 && xx < oRong && yy < oCao && (chi.getRGB(xx, yy) >>> 24) >= 128) {
                            che++;
                        }
                    }
                }
                System.out.printf("  %-5s k%2d: thu x[%4d..%4d] y[%4d..%4d] | nguoi tam x=%3d chan=%3d mong=%3d | do/cam: %s | than bi che %d%%%n",
                        TEN_HUONG[h], f, tx0 - gocX, tx1 - gocX, ty0 - gocY, ty1 - gocY, dx[h], dy[h], dy[h] - 12,
                        soDo == 0 ? "-" : String.format("%3dpx x[%4d..%4d] y[%4d..%4d]", soDo, yx0 - gocX, yx1 - gocX, yy0 - gocY, yy1 - gocY),
                        che * 100 / Math.max(1, tongT));

                final int ox = h * oRong * tl, oy = si * oCao * tl;
                g.drawImage(o, ox, oy, oRong * tl, oCao * tl, null);
                g.setColor(new Color(0xC0, 0x30, 0x30));
                g.drawLine(ox, oy + gocY * tl, ox + oRong * tl, oy + gocY * tl);
                g.setColor(new Color(0x80, 0x80, 0x90));
                for (int t = -90; t <= 20; t += 10) {
                    final int yy = oy + (gocY + t) * tl;
                    g.drawLine(ox, yy, ox + 4 * tl, yy);
                    g.drawString(String.valueOf(t), ox + 5 * tl, yy + 4);
                }
                for (int t = -60; t <= 60; t += 10) {
                    final int xx = ox + (gocX + t) * tl;
                    g.drawLine(xx, oy + (oCao - 4) * tl, xx, oy + oCao * tl);
                    g.drawString(String.valueOf(t), xx - 6, oy + (oCao - 5) * tl);
                }
                g.setColor(new Color(0x40, 0xC0, 0xFF));
                g.setStroke(new BasicStroke(1f));
                g.drawLine(ox + (nx - 12) * tl, oy + chan * tl, ox + (nx + 12) * tl, oy + chan * tl);
                g.setColor(Color.YELLOW);
                g.drawString(TEN_HUONG[h] + " k" + f + "  dx" + dx[h] + " dy" + dy[h], ox + 6, oy + 14);
            }
        }
        g.dispose();
        ImageIO.write(out, "png", new File(ra));
        System.out.println("Da ghi " + ra);
    }

    private static int[] so3(final String s) {
        final String[] p = s.split(",");
        return new int[]{Integer.parseInt(p[0].trim()), Integer.parseInt(p[1].trim()), Integer.parseInt(p[2].trim())};
    }

    /** Than nguoi (khong chan): cam, tu mong len vai, hai tay hai ben. */
    private static void veThan(final Graphics2D g, final int nx, final int chan) {
        g.setColor(new Color(0xE0, 0x70, 0x30));
        g.fillRect(nx - 6, chan - 30, 13, 18);
        g.setColor(new Color(0xF0, 0xA0, 0x60));
        g.fillRect(nx - 9, chan - 26, 3, 10);
        g.fillRect(nx + 7, chan - 26, 3, 10);
    }

    private static void veDau(final Graphics2D g, final int nx, final int chan) {
        g.setColor(new Color(0xFF, 0xE0, 0xB0));
        g.fillOval(nx - 6, chan - 41, 13, 12);
        g.setColor(new Color(0x40, 0x20, 0x10));
        g.fillRect(nx - 6, chan - 41, 13, 4);
    }

    private static void doc(final String duong, final List<AnhCon> anh, final List<List<Manh>> khung) throws Exception {
        final DataInputStream in = new DataInputStream(new FileInputStream(duong));
        try {
            final int soAnh = in.readByte() & 0xFF;
            for (int i = 0; i < soAnh; i++) {
                in.readUnsignedByte();
                final AnhCon c = new AnhCon();
                c.x = in.readUnsignedByte(); c.y = in.readUnsignedByte();
                c.w = in.readUnsignedByte(); c.h = in.readUnsignedByte();
                anh.add(c);
            }
            final int soKhung = in.readShort();
            for (int f = 0; f < soKhung; f++) {
                final int soManh = in.readByte();
                final List<Manh> ds = new ArrayList<Manh>();
                for (int k = 0; k < soManh; k++) {
                    final Manh m = new Manh();
                    m.dx = in.readShort(); m.dy = in.readShort();
                    m.idAnh = in.readByte(); m.lat = in.readByte(); m.tren = in.readByte();
                    ds.add(m);
                }
                khung.add(ds);
            }
        } finally {
            in.close();
        }
    }

    /** Y het DataSkillEff.paintTopHorse (tren == 0, ve truoc) roi paintBottomHorse (tren == 1). */
    private static void ve(final Graphics2D g, final BufferedImage atlas, final List<AnhCon> anh,
                           final List<Manh> ds, final int ox, final int oy, final boolean phai) {
        for (int lop = 0; lop <= 1; lop++) {
            for (final Manh m : ds) {
                if (m.tren != lop) {
                    continue;
                }
                final AnhCon c = anh.get(m.idAnh);
                if (c.w <= 0 || c.h <= 0 || c.x + c.w > atlas.getWidth() || c.y + c.h > atlas.getHeight()) {
                    continue;
                }
                final BufferedImage con = atlas.getSubimage(c.x, c.y, c.w, c.h);
                int dx = m.dx;
                boolean lat = (m.lat == 1);
                if (phai) {
                    dx = -(dx + c.w);
                    lat = !lat;
                }
                final int x = ox + dx, y = oy + m.dy;
                if (lat) {
                    g.drawImage(con, x + c.w, y, x, y + c.h, 0, 0, c.w, c.h, null);
                } else {
                    g.drawImage(con, x, y, null);
                }
            }
        }
    }

    private ThuChoNgoiSkinThu() {
    }
}
