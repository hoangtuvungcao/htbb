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
 * Ve lai con thu cuoi DUNG NHU CLIENT VE, kem cho nhan vat ngoi, de soi khoang ho.
 *
 * VI SAO CAN: nhin anh chup man hinh thi chi doan duoc. Phep ve cua client la:
 *
 *   - Con thu ve tai (x, y) — y la CHAN nhan vat, khong bu gi ca
 *     (Char.paint: partEff.paintTopHorse(g, x, y, getFrameHorse(), ...))
 *   - Cac bo phan NHAN VAT ve tai y + Char.dy_horse[typehorse][dir]
 *     (Char.getdyParHorse), voi dy_horse = {{-20, -15, -15, -15}} — CHI MOT DONG, nen
 *     typehorse buoc phai la 0.
 *
 * Nen neu con thu thap qua, chan nhan vat bi nhac len cao hon lung con thu va ho ra mot
 * khoang trong. Cong cu nay ve vach chan (do), khung nhan vat (xanh) va con thu, roi in
 * so do bang so de sua cho dung thay vi chinh mo.
 *
 * Cach dung:
 *   java XemSkinNhuTrongGame ra.png map/d_effect/56 map/skinthu/0 map/skinthu/1 ...
 */
public final class XemSkinNhuTrongGame {

    /** Char.dy_horse[0] — nhac nhan vat len theo huong {xuong, len, trai, phai}. */
    private static final int[] NHAC_NGUOI = { -20, -15, -15, -15 };

    /** Char.getFrameHorse: xuong +0, len +7, ngang +14. Slot 0 la tu the dung yen. */
    private static final int[] KHUNG_DAU_HUONG = { 0, 7, 14, 14 };

    private static final String[] TEN_HUONG = { "xuong", "len", "trai", "phai" };

    /** Nhan vat KPAH cao xap xi 40px, rong xap xi 20px, chan o day khung. */
    private static final int NGUOI_RONG = 20;
    private static final int NGUOI_CAO = 40;

    private static final int O_RONG = 130;
    private static final int O_CAO = 150;
    private static final int CHAN_Y = 105;

    private static final class AnhCon {
        int x, y, w, h;
    }

    private static final class Manh {
        int dx, dy, idAnh, lat, tren;
    }

    public static void main(final String[] thamSo) throws Exception {
        if (thamSo.length < 2) {
            System.out.println("Dung: java XemSkinNhuTrongGame <ra.png> <tep-du-lieu>...");
            System.exit(2);
        }
        final int soCon = thamSo.length - 1;
        final BufferedImage ra = new BufferedImage(O_RONG * 4 + 120, O_CAO * soCon,
                BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = ra.createGraphics();
        g.setColor(new Color(0x2A, 0x2E, 0x36));
        g.fillRect(0, 0, ra.getWidth(), ra.getHeight());
        g.setFont(new Font("Consolas", Font.PLAIN, 12));

        for (int n = 0; n < soCon; n++) {
            final String duong = thamSo[n + 1];
            final int oy = n * O_CAO;
            g.setColor(Color.YELLOW);
            g.drawString(duong, 6, oy + 16);

            final BufferedImage atlas = ImageIO.read(new File(duong + ".png"));
            final AnhCon[] anhCon;
            final List<List<Manh>> khung = new ArrayList<List<Manh>>();
            final DataInputStream in = new DataInputStream(new FileInputStream(duong));
            try {
                final int soAnh = in.readByte() & 0xFF;
                anhCon = new AnhCon[soAnh];
                for (int i = 0; i < soAnh; i++) {
                    in.readUnsignedByte();
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

            for (int h = 0; h < 4; h++) {
                final int ox = 120 + h * O_RONG + O_RONG / 2;
                final int chan = oy + CHAN_Y;
                final int khungSo = KHUNG_DAU_HUONG[h];
                if (khungSo >= khung.size()) {
                    continue;
                }

                // Vach chan (mat dat)
                g.setColor(new Color(0xC0, 0x30, 0x30));
                g.drawLine(ox - 55, chan, ox + 55, chan);
                g.setColor(new Color(0x88, 0x88, 0x99));
                g.drawString(TEN_HUONG[h], ox - 50, oy + 16);

                // Con thu ve tai chan, khong bu gi
                veKhung(g, atlas, anhCon, khung.get(khungSo), ox, chan);

                // Khung nhan vat: chan o chan + NHAC_NGUOI[h]
                final int chanNguoi = chan + NHAC_NGUOI[h];
                g.setColor(new Color(0x40, 0xC0, 0xFF, 0x90));
                g.setStroke(new BasicStroke(1.5f));
                g.drawRect(ox - NGUOI_RONG / 2, chanNguoi - NGUOI_CAO, NGUOI_RONG, NGUOI_CAO);
                g.setColor(new Color(0x40, 0xC0, 0xFF));
                g.drawLine(ox - NGUOI_RONG / 2 - 4, chanNguoi, ox + NGUOI_RONG / 2 + 4, chanNguoi);

                // So do: dinh con thu so voi chan nguoi. Am = con thu cao hon chan nguoi
                // (nguoi ngoi lut vao than thu, dung). Duong = HO RA mot khoang trang.
                int tren = Integer.MAX_VALUE;
                int duoi = Integer.MIN_VALUE;
                for (final Manh m : khung.get(khungSo)) {
                    final AnhCon a = anhCon[m.idAnh];
                    tren = Math.min(tren, m.dy);
                    duoi = Math.max(duoi, m.dy + a.h);
                }
                final int ho = tren - NHAC_NGUOI[h];
                g.setColor(ho > 0 ? new Color(0xFF, 0x70, 0x70) : new Color(0x70, 0xE0, 0x70));
                g.drawString((ho > 0 ? "HO " + ho + "px" : "lut " + (-ho) + "px"), ox - 30, oy + O_CAO - 14);
                g.setColor(new Color(0x99, 0x99, 0x99));
                g.drawString("dy " + tren + ".." + duoi, ox - 30, oy + O_CAO - 2);
            }
        }
        g.dispose();
        ImageIO.write(ra, "png", new File(thamSo[0]));
        System.out.println("Da ghi " + thamSo[0]);
    }

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

    private XemSkinNhuTrongGame() {
    }
}
