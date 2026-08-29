import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Sinh icon vat pham 16x16 cho tung skin linh thu, tu chinh atlas + du lieu frame.
 *
 * VI SAO CAN: bo sprite dua vao chi co atlas ban do va du lieu frame, khong co icon
 * hanh trang. Ma cot `xstart` cua data_item tro toi map/icon/&lt;id&gt;.png, thieu file thi
 * vat pham hien khong co hinh trong hanh trang.
 *
 * GHEP FRAME: lay frame 0 cua khoi huong XUONG (tu the dung yen theo Char.STANDFRAME).
 * Moi frame gom nhieu manh, moi manh ve tai (dx, dy) neo GOC TREN TRAI, manh onTop = 0
 * ve truoc roi den onTop = 1. Day dung theo DataSkillEff.paintBottomHorse /
 * paintTopHorse cua client, KHONG phai theo ten truong: ham dung FrameEff bi dao ten
 * tham so nen manh onTop == 0 lai roi vao danh sach ve truoc.
 *
 * ICON KHONG DUOC DAT VAO DAI 855..859: sau khi ban skin dau tien bi go, dung nam id do
 * da duoc giao cho CANH (commit 2c651ce) lam anh o xem truoc cua so Trang bi.
 *
 * Cach dung:
 *   java TaoIconSkinThu map/skinthu 5 860 map/icon
 */
public final class TaoIconSkinThu {

    /** Frame lay lam icon: slot 0 cua khoi huong XUONG, tuc tu the dung yen. */
    private static final int SLOT_DUNG_YEN = 0;

    private static final class AnhCon {
        int x, y, w, h;
    }

    private static final class Manh {
        int dx, dy, idAnh, lat, tren;
    }

    public static void main(final String[] thamSo) throws Exception {
        if (thamSo.length < 4) {
            System.out.println("Dung: java TaoIconSkinThu <thu-muc-skinthu> <so-skin>"
                    + " <id-icon-dau> <thu-muc-icon>");
            System.exit(2);
        }
        final File thuMuc = new File(thamSo[0]);
        final int soSkin = Integer.parseInt(thamSo[1]);
        final int idIconDau = Integer.parseInt(thamSo[2]);
        final File thuMucIcon = new File(thamSo[3]);

        for (int n = 0; n < soSkin; n++) {
            final File tepDu = new File(thuMuc, String.valueOf(n));
            final File tepAnh = new File(thuMuc, n + ".png");
            if (!tepDu.isFile() || !tepAnh.isFile()) {
                System.out.println("  bo qua skin " + n + ": thieu " + tepDu + " hoac " + tepAnh);
                continue;
            }
            final BufferedImage atlas = ImageIO.read(tepAnh);
            final AnhCon[] anhCon;
            final List<List<Manh>> khung = new ArrayList<List<Manh>>();

            final DataInputStream in = new DataInputStream(new FileInputStream(tepDu));
            try {
                final int soAnh = in.readByte() & 0xFF;
                anhCon = new AnhCon[soAnh];
                for (int i = 0; i < soAnh; i++) {
                    in.readUnsignedByte();          // id: thu tu chinh la id, khong dung
                    final AnhCon a = new AnhCon();
                    a.x = in.readUnsignedByte();
                    a.y = in.readUnsignedByte();
                    a.w = in.readUnsignedByte();
                    a.h = in.readUnsignedByte();
                    anhCon[i] = a;
                }
                final int soKhung = in.readShort();
                for (int f = 0; f < soKhung; f++) {
                    final int soManh = in.readByte();
                    final List<Manh> ds = new ArrayList<Manh>();
                    for (int k = 0; k < soManh; k++) {
                        final Manh m = new Manh();
                        m.dx = in.readShort();
                        m.dy = in.readShort();
                        m.idAnh = in.readByte();
                        m.lat = in.readByte();
                        m.tren = in.readByte();
                        ds.add(m);
                    }
                    khung.add(ds);
                }
            } finally {
                in.close();
            }

            if (khung.size() <= SLOT_DUNG_YEN) {
                System.out.println("  bo qua skin " + n + ": chi co " + khung.size() + " khung");
                continue;
            }

            final List<Manh> ds = khung.get(SLOT_DUNG_YEN);
            int tr = Integer.MAX_VALUE, tren = Integer.MAX_VALUE;
            int ph = Integer.MIN_VALUE, duoi = Integer.MIN_VALUE;
            for (final Manh m : ds) {
                final AnhCon a = anhCon[m.idAnh];
                tr = Math.min(tr, m.dx);
                tren = Math.min(tren, m.dy);
                ph = Math.max(ph, m.dx + a.w);
                duoi = Math.max(duoi, m.dy + a.h);
            }
            final int rong = ph - tr;
            final int cao = duoi - tren;
            if (rong <= 0 || cao <= 0) {
                System.out.println("  bo qua skin " + n + ": khung bao rong");
                continue;
            }

            final BufferedImage than = new BufferedImage(rong, cao, BufferedImage.TYPE_INT_ARGB);
            final Graphics2D g = than.createGraphics();
            veKhung(g, atlas, anhCon, ds, -tr, -tren);
            g.dispose();

            final File tepIcon = new File(thuMucIcon, (idIconDau + n) + ".png");
            ghiIcon(tepIcon, than);
            System.out.printf("  %-22s 16 x 16  (tu khung %dx%d cua skin %d)%n",
                    tepIcon.getPath(), rong, cao, n);
        }
    }

    /** Ve mot frame: manh onTop = 0 truoc (bong do), roi den manh onTop = 1 (than). */
    private static void veKhung(final Graphics2D g, final BufferedImage atlas,
                                final AnhCon[] anhCon, final List<Manh> ds,
                                final int ox, final int oy) {
        for (int lop = 0; lop <= 1; lop++) {
            for (final Manh m : ds) {
                if (m.tren != lop) {
                    continue;
                }
                final AnhCon a = anhCon[m.idAnh];
                if (a.w <= 0 || a.h <= 0
                        || a.x + a.w > atlas.getWidth() || a.y + a.h > atlas.getHeight()) {
                    continue;
                }
                final BufferedImage con = atlas.getSubimage(a.x, a.y, a.w, a.h);
                final int x = ox + m.dx;
                final int y = oy + m.dy;
                if (m.lat == 1) {
                    g.drawImage(con, x + a.w, y, x, y + a.h, 0, 0, a.w, a.h, null);
                } else {
                    g.drawImage(con, x, y, null);
                }
            }
        }
    }

    /** Icon hanh trang: thu vua khung 16x16, giu ti le, can giua. */
    private static void ghiIcon(final File ra, final BufferedImage nguon) throws Exception {
        final int cd = 16;
        final double tiLe = Math.min((double) cd / nguon.getWidth(), (double) cd / nguon.getHeight());
        final int w = Math.max(1, (int) Math.round(nguon.getWidth() * tiLe));
        final int h = Math.max(1, (int) Math.round(nguon.getHeight() * tiLe));
        final BufferedImage im = new BufferedImage(cd, cd, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = im.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(nguon, (cd - w) / 2, (cd - h) / 2, w, h, null);
        g.dispose();
        if (ra.getParentFile() != null) {
            ra.getParentFile().mkdirs();
        }
        ImageIO.write(im, "png", ra);
    }

    private TaoIconSkinThu() {
    }
}
