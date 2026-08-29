import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Sinh anh dai dien avt&lt;n&gt;.png cho tung skin linh thu, tu chinh atlas + du lieu frame.
 *
 * VI SAO CAN: bang "Trang bi thu" cua client ve con thu bang MOT DAI DOC 3 FRAME
 * (WindowInfoScr.cs:5706 va hai cho giong het o MenuWindows / ShopNew):
 *
 *     g.drawRegion(imgAnimal, 0, himg * idFrame, wimg, himg, ...)
 *
 * tuc anh phai cao dung 3 lan chieu cao mot frame. Bo sprite dua vao chi co atlas ban do
 * va du lieu frame, khong co dai nay, ma DataGame.napSkinThu TAT HAN skin nao thieu du
 * ba tep - nen khong sinh ra thi skin khong bat duoc.
 *
 * LAY FRAME NAO: {0, 2, 4} cua khoi huong XUONG. Ba slot do dung theo bang cua client
 * (Char.STANDFRAME cho slot 0-1 luc dung yen, Char.horseMove cho slot 2-4 luc di) nen
 * dai nay chinh la "mot tu the dung + hai tu the di" - dung nhu cong cu SkinThu cu lam.
 *
 * GHEP FRAME: moi frame gom nhieu manh, moi manh ve tai (dx, dy) neo GOC TREN TRAI, manh
 * onTop = 0 ve truoc roi den onTop = 1. Day dung theo DataSkillEff.paintBottomHorse /
 * paintTopHorse cua client, KHONG phai theo ten truong: ham dung FrameEff bi dao ten
 * tham so nen manh onTop == 0 lai roi vao danh sach ve truoc.
 *
 * Kem theo sinh luon icon vat pham 16x16 cho hanh trang (map/icon/&lt;idIcon&gt;.png), lay tu
 * frame dung yen. Icon KHONG duoc dat vao dai 855..859 nua: sau khi tinh nang skin bi go,
 * commit 2c651ce da dung lai dung nam id do cho CANH.
 *
 * Cach dung:
 *   java TaoAvtSkinThu map/skinthu 5 [id-icon-dau map/icon]
 */
public final class TaoAvtSkinThu {

    /** Ba slot lay lam dai dai dien, trong khoi 7 frame cua huong XUONG. */
    private static final int[] SLOT = { 0, 2, 4 };

    private static final class AnhCon {
        int x, y, w, h;
    }

    private static final class Manh {
        int dx, dy, idAnh, lat, tren;
    }

    public static void main(final String[] thamSo) throws Exception {
        if (thamSo.length < 2) {
            System.out.println("Dung: java TaoAvtSkinThu <thu-muc-skinthu> <so-skin>");
            System.exit(2);
        }
        final File thuMuc = new File(thamSo[0]);
        final int soSkin = Integer.parseInt(thamSo[1]);
        final int idIconDau = (thamSo.length > 2) ? Integer.parseInt(thamSo[2]) : -1;
        final File thuMucIcon = new File((thamSo.length > 3) ? thamSo[3] : "map/icon");

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
                    in.readUnsignedByte();          // id, khong dung: thu tu chinh la id
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

            if (khung.size() < 5) {
                System.out.println("  bo qua skin " + n + ": chi co " + khung.size() + " khung");
                continue;
            }

            // Mot khung bao chung cho ca ba frame, de con thu khong nhay khi doi frame.
            int tr = Integer.MAX_VALUE, tren = Integer.MAX_VALUE;
            int ph = Integer.MIN_VALUE, duoi = Integer.MIN_VALUE;
            for (final int s : SLOT) {
                for (final Manh m : khung.get(s)) {
                    final AnhCon a = anhCon[m.idAnh];
                    tr = Math.min(tr, m.dx);
                    tren = Math.min(tren, m.dy);
                    ph = Math.max(ph, m.dx + a.w);
                    duoi = Math.max(duoi, m.dy + a.h);
                }
            }
            final int rong = ph - tr;
            final int cao = duoi - tren;
            if (rong <= 0 || cao <= 0) {
                System.out.println("  bo qua skin " + n + ": khung bao rong");
                continue;
            }

            final BufferedImage ra = new BufferedImage(rong, cao * SLOT.length,
                    BufferedImage.TYPE_INT_ARGB);
            final Graphics2D g = ra.createGraphics();
            for (int i = 0; i < SLOT.length; i++) {
                veKhung(g, atlas, anhCon, khung.get(SLOT[i]), -tr, i * cao - tren);
            }
            g.dispose();
            final File dich = new File(thuMuc, "avt" + n + ".png");
            ImageIO.write(ra, "png", dich);
            System.out.printf("  %-22s %d x %d  (3 frame %dx%d)%n",
                    dich.getPath(), rong, cao * SLOT.length, rong, cao);

            if (idIconDau >= 0) {
                final BufferedImage than = new BufferedImage(rong, cao, BufferedImage.TYPE_INT_ARGB);
                final Graphics2D g2 = than.createGraphics();
                veKhung(g2, atlas, anhCon, khung.get(SLOT[0]), -tr, -tren);
                g2.dispose();
                final File tepIcon = new File(thuMucIcon, (idIconDau + n) + ".png");
                ghiIcon(tepIcon, than);
                System.out.printf("  %-22s 16 x 16  (icon vat pham)%n", tepIcon.getPath());
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
        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(nguon, (cd - w) / 2, (cd - h) / 2, w, h, null);
        g.dispose();
        if (ra.getParentFile() != null) {
            ra.getParentFile().mkdirs();
        }
        ImageIO.write(im, "png", ra);
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

    private TaoAvtSkinThu() {
    }
}
