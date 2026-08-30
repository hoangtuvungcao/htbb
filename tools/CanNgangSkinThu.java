import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.imageio.ImageIO;

/**
 * Dich con thu skin sang NGANG cho no do duoc chan nguoi cuoi, o hai huong trai/phai.
 *
 * VI SAO CAN: client ve con thu tai x, nhung ve NGUOI tai x + Char.dx_horse[0][dir],
 * ma dong 0 cua bang do la {0, 0, 7, -7} — tuc huong ngang nguoi bi day sang ben 7px so
 * voi con thu. Con thu tool cua game rong ~50px nen van do het; bo sprite skin chi rong
 * 30..50px va lai nam lech ve mot ben, nen NUA DUOI BEN PHAI cua nguoi tho ra ngoai than
 * con thu. Ma khi cuoi thu tool thi client KHONG VE CHAN NGUOI (Char.java:1838 bo qua o
 * thu tu 0 khi idhorse != -1) — no trong cay hoan toan vao con thu che cho do. Ket qua la
 * nhin xuyen thay nen dat, dung cho dang le la chan.
 *
 * CHI CAN MOT SO DUONG CHO CA HAI HUONG: huong phai client ve voi rotale = 2, va trong
 * DataSkillEff no lam "dx = -(dx + w)" — tuc lat guong qua x = 0. Nen dich +5 o du lieu
 * thanh -5 khi quay phai, khop dung voi nguoi bi day -7 o huong do.
 *
 * CHI DUNG KHOI NGANG (khung 14..20). Huong xuong/len co dx_horse = 0 nen khong lech,
 * dung sang la con thu tu nhien lech khoi nguoi.
 *
 * CACH CHON SO: khong tu nghi ra, ma do do PHU cua sprite len dai chan nguoi roi doi
 * chieu voi mot con thu tool CO SAN cua game. Poro (map/d_effect/56) o vi tri goc phu
 * 51% va nhin binh thuong, nen do la muc san. Cong cu tim so dich NHO NHAT dat toi muc
 * san cong bien an toan, va khong dich qua gioiHan de con thu khong troi ra xa nguoi.
 *
 * Cach dung:
 *   java CanNgangSkinThu map/skinthu 5 map/d_effect/56 14 8
 *                        thu-muc     so mau            bien gioi-han
 */
public final class CanNgangSkinThu {

    /** Char.dx_horse[0][2] — nguoi bi day sang ben bao nhieu o huong ngang. */
    private static final int DAY_NGUOI = 7;

    /** Khung dung yen cua khoi ngang (Char.getFrameHorse: ngang = +14). */
    private static final int KHUNG_NGANG = 14;
    private static final int SO_KHUNG_KHOI = 7;

    /** Dai chan nguoi can duoc che, tinh theo goc toa do con thu. */
    private static final int DAI_TREN = -32;
    private static final int DAI_DUOI = -15;
    private static final int DAI_RONG = 11;

    public static void main(final String[] t) throws Exception {
        if (t.length < 5) {
            System.out.println("Dung: java CanNgangSkinThu <thu-muc> <so-skin> <tep-mau>"
                    + " <bien> <gioi-han>");
            System.exit(2);
        }
        final File thuMuc = new File(t[0]);
        final int soSkin = Integer.parseInt(t[1]);
        final File mau = new File(t[2]);
        final int bien = Integer.parseInt(t[3]);
        final int gioiHan = Integer.parseInt(t[4]);

        final int phuMau = doPhu(mau, 0);
        final int muc = phuMau + bien;
        System.out.println("Mau " + mau + " phu " + phuMau + "%, dat muc san " + muc
                + "% (bien +" + bien + "), khong dich qua " + gioiHan + "px.");

        for (int n = 0; n < soSkin; n++) {
            final File tep = new File(thuMuc, String.valueOf(n));
            if (!tep.isFile()) {
                System.out.println("  skin " + n + ": khong thay " + tep);
                continue;
            }
            final int truoc = doPhu(tep, 0);
            int chon = 0;
            for (int d = 1; d <= gioiHan; d++) {
                if (doPhu(tep, d) >= muc) {
                    chon = d;
                    break;
                }
                chon = d;                      // chua dat muc thi cu dich toi gioi han
            }
            if (truoc >= muc) {
                System.out.println("  skin " + n + ": phu " + truoc + "% - da du, khong dich");
                continue;
            }
            final int sau = doPhu(tep, chon);
            dich(tep, chon);
            System.out.println("  skin " + n + ": dich +" + chon + "px  (phu " + truoc
                    + "% -> " + sau + "%)" + (sau < muc ? "  [cham muc, sprite von hep]" : ""));
        }
    }

    /** Do phan tram dai chan nguoi duoc sprite che, khi dich sprite sang phai d pixel. */
    private static int doPhu(final File tep, final int d) throws Exception {
        final byte[] b = Files.readAllBytes(tep.toPath());
        final BufferedImage at = ImageIO.read(new File(tep.getPath() + ".png"));
        final int soAnh = b[0] & 0xFF;
        final int[] ix = new int[soAnh];
        final int[] iy = new int[soAnh];
        final int[] iw = new int[soAnh];
        final int[] ih = new int[soAnh];
        for (int i = 0; i < soAnh; i++) {
            ix[i] = b[1 + i * 5 + 1] & 0xFF;
            iy[i] = b[1 + i * 5 + 2] & 0xFF;
            iw[i] = b[1 + i * 5 + 3] & 0xFF;
            ih[i] = b[1 + i * 5 + 4] & 0xFF;
        }
        int vt = 1 + soAnh * 5;
        final int soKhung = ((b[vt] & 0xFF) << 8) | (b[vt + 1] & 0xFF);
        vt += 2;
        final boolean[][] dac = new boolean[200][200];
        for (int f = 0; f < soKhung; f++) {
            final int soManh = b[vt] & 0xFF;
            vt++;
            for (int k = 0; k < soManh; k++) {
                final int ddx = doc16(b, vt);
                final int ddy = doc16(b, vt + 2);
                final int s = b[vt + 4] & 0xFF;
                vt += 7;
                if (f != KHUNG_NGANG) {
                    continue;
                }
                for (int yy = 0; yy < ih[s]; yy++) {
                    for (int xx = 0; xx < iw[s]; xx++) {
                        if (ix[s] + xx >= at.getWidth() || iy[s] + yy >= at.getHeight()) {
                            continue;
                        }
                        if ((at.getRGB(ix[s] + xx, iy[s] + yy) >>> 24) < 128) {
                            continue;
                        }
                        final int gx = ddx + d + xx + 100;
                        final int gy = ddy + yy + 100;
                        if (gx >= 0 && gx < 200 && gy >= 0 && gy < 200) {
                            dac[gy][gx] = true;
                        }
                    }
                }
            }
        }
        int tong = 0;
        int phu = 0;
        for (int y = DAI_TREN; y <= DAI_DUOI; y++) {
            for (int x = -DAI_RONG; x <= DAI_RONG; x++) {
                tong++;
                if (dac[y + 100][x + DAY_NGUOI + 100]) {
                    phu++;
                }
            }
        }
        return tong == 0 ? 0 : phu * 100 / tong;
    }

    /** Cong them d vao dx cua moi manh trong khoi ngang. */
    private static void dich(final File tep, final int d) throws Exception {
        final byte[] b = Files.readAllBytes(tep.toPath());
        final int soAnh = b[0] & 0xFF;
        int vt = 1 + soAnh * 5;
        final int soKhung = ((b[vt] & 0xFF) << 8) | (b[vt + 1] & 0xFF);
        vt += 2;
        for (int f = 0; f < soKhung; f++) {
            final int soManh = b[vt] & 0xFF;
            vt++;
            for (int k = 0; k < soManh; k++) {
                if (f >= KHUNG_NGANG && f < KHUNG_NGANG + SO_KHUNG_KHOI) {
                    ghi16(b, vt, doc16(b, vt) + d);
                }
                vt += 7;
            }
        }
        Files.write(Paths.get(tep.getPath()), b);
    }

    private static int doc16(final byte[] b, final int vt) {
        return (short) (((b[vt] & 0xFF) << 8) | (b[vt + 1] & 0xFF));
    }

    private static void ghi16(final byte[] b, final int vt, final int v) {
        b[vt] = (byte) (v >> 8);
        b[vt + 1] = (byte) v;
    }

    private CanNgangSkinThu() {
    }
}
