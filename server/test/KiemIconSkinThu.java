import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Icon vat pham skin linh thu (map/icon/860..865.png).
 *
 * 860..864 ve CA CON THU, mot khung tinh 16x16. 865 (Kim Cang Long, vat pham 911) la DAI
 * BA KHUNG hoat anh: 16x48 = 3 khung 16x16 xep doc, moi khung la mot dau rong nhin nghieng
 * co san trong atlas map/skinthu/5.png (anh con 25 / 28 / 31 - ba pha cua dau trong khung
 * ngang k14..20, KHONG phai cat tu than rong). Nguoi dung yeu cau 12/09/2026: "cat rieng 3
 * anh nay... ghep lai lam frame nhu cac loai mat na" - icon mat na 620..624 chinh la dai 3
 * khung 18x54, va y la ICON trong hanh trang (da tung lam nham thanh lop phu doi tren dau,
 * map/d_effect/135, roi phai go).
 *
 * HAI CAI BAY VE KICH THUOC, deu tim ra khi thu lam icon chi co cai dau roi bo:
 *
 * 1. Luoi hanh trang buoc 18 px moi o (WindowInfoScr: "num4 % 3 * 18 + 54 + 9"), va
 *    client ve icon CAN GIUA o - GameData.paintIcon goi drawRegion(..., x, y, 3) voi
 *    3 = HCENTER|VCENTER, MyGraphics tru di nua chieu rong/cao. Nen kich thuoc anh
 *    quyet dinh thang be rong vien:
 *        18x18 -> vien 0, cac icon dinh lien thanh mot dai
 *        16x16 -> vien 1 px  (co icon goc dang dung)
 *    Cac icon goc tuy la khung 16x16 nhung hinh ve ben trong chi chiem phan giua, con
 *    lai la trong suot (86..173 diem tren 256), nen nhin van thoang.
 *
 * 2. GameData.paintIcon con lam "if (h > 25) { h /= 3; frame = 3; }" - anh CAO HON
 *    25 px bi hieu la dai BA KHUNG hoat hinh: doi khung moi 5 tick (Canvas.gameTick % 5)
 *    va chi ve vung (0, indexFrame*h, w, h). Do la ly do trong map/icon co 37 tep 18x54.
 *    Icon TINH ma cao qua 25 la hien ra cut con mot phan ba; nguoc lai icon BA KHUNG
 *    phai cao > 25 VA chia het cho 3, khong thi cac khung bi lech dan tung pixel.
 *
 * Dinh dang du lieu khung cua atlas skin (map/skinthu/<n>, di kem <n>.png) - giai ma
 * duoc luc lam ban icon cai dau, ghi lai vi khong co o dau khac:
 *     byte soKhung, roi moi khung 5 byte: id, x, y, w, h   (tat ca khong dau)
 * Khung 0 luon la cai bong den. Khung 1 la hinh chinh dien (nhin thay mat).
 */
public final class KiemIconSkinThu {

    /** Nguong client coi anh la dai 3 khung hoat hinh. */
    private static final int CAO_TOI_DA = 25;
    /** Buoc nhay giua hai o hanh trang. */
    private static final int BUOC_LUOI = 18;
    private static final int ID_DAU = 860;
    private static final int SO_SKIN = 6;
    /** Icon duy nhat trong dai la dai 3 khung (dau rong Kim Cang Long). */
    private static final int ID_ICON_BA_KHUNG = 865;
    private static final int SO_KHUNG_HOAT_ANH = 3;

    public static void chay() {
        Kiem.nhom("Icon skin linh thu - kich thuoc phai an toan voi client");

        for (int n = 0; n < SO_SKIN; n++) {
            final int id = ID_DAU + n;
            final File f = new File("map/icon/" + id + ".png");
            Kiem.dung("co tep map/icon/" + id + ".png", f.isFile());
            if (!f.isFile()) {
                continue;
            }
            final BufferedImage im = doc(f);
            Kiem.dung("doc duoc " + id + ".png", im != null);
            if (im == null) {
                continue;
            }

            final boolean baKhung = id == ID_ICON_BA_KHUNG;
            if (baKhung) {
                // Client chi bat hoat anh khi cao > 25; cao khong chia het cho 3 thi
                // indexFrame * (h / 3) truot dan, khung cuoi bi cat.
                Kiem.dung("icon " + id + " la dai 3 khung: cao > " + CAO_TOI_DA + " va chia het cho 3",
                        im.getHeight() > CAO_TOI_DA && im.getHeight() % SO_KHUNG_HOAT_ANH == 0);
            } else {
                Kiem.dung("icon " + id + " cao <= " + CAO_TOI_DA
                        + " (cao hon la client cat con mot phan ba)", im.getHeight() <= CAO_TOI_DA);
            }
            final int soKhung = baKhung ? SO_KHUNG_HOAT_ANH : 1;
            final int caoKhung = im.getHeight() / soKhung;
            Kiem.dung("icon " + id + " khong lap kin o (moi khung <= " + (BUOC_LUOI - 2) + "px)",
                    im.getWidth() <= BUOC_LUOI - 2 && caoKhung <= BUOC_LUOI - 2);

            for (int k = 0; k < soKhung; k++) {
                final BufferedImage khung = im.getSubimage(0, k * caoKhung, im.getWidth(), caoKhung);
                final String ten = soKhung == 1 ? "icon " + id : "icon " + id + " khung " + k;
                // Cat truot ra vung rong thi con dung 1 mau, cat vao mang phang thi 2-3.
                Kiem.dung(ten + " co noi dung that (>= 4 mau)", soMau(khung) >= 4);
                // Quanh hinh phai con vien trong suot - do la thu lam icon nhin thoang trong o.
                Kiem.dung(ten + " con vien trong suot quanh hinh", soDiemTrong(khung) > 0);
            }
            if (baKhung) {
                // Ba khung la ba PHA cua cung mot cai dau: phai khac nhau (co hoat anh that)
                // nhung vung ve phai gan nhu trung nhau (khong nhay hinh khi doi khung).
                final int[] bb0 = vungVe(im.getSubimage(0, 0, im.getWidth(), caoKhung));
                boolean khacNhau = false;
                boolean cungCho = true;
                for (int k = 1; k < soKhung; k++) {
                    final BufferedImage khung = im.getSubimage(0, k * caoKhung, im.getWidth(), caoKhung);
                    if (!giongHet(im.getSubimage(0, 0, im.getWidth(), caoKhung), khung)) {
                        khacNhau = true;
                    }
                    final int[] bb = vungVe(khung);
                    for (int i = 0; i < 4; i++) {
                        if (Math.abs(bb[i] - bb0[i]) > 2) {
                            cungCho = false;
                        }
                    }
                }
                Kiem.dung("icon " + id + ": ba khung khac nhau (co hoat anh that)", khacNhau);
                Kiem.dung("icon " + id + ": vung ve ba khung lech nhau <= 2px (khong nhay hinh)", cungCho);
            }
        }
    }

    private static int soMau(final BufferedImage im) {
        final java.util.Set<Integer> mau = new java.util.HashSet<Integer>();
        for (int y = 0; y < im.getHeight(); y++) {
            for (int x = 0; x < im.getWidth(); x++) {
                mau.add(Integer.valueOf(im.getRGB(x, y)));
            }
        }
        return mau.size();
    }

    private static int soDiemTrong(final BufferedImage im) {
        int n = 0;
        for (int y = 0; y < im.getHeight(); y++) {
            for (int x = 0; x < im.getWidth(); x++) {
                if ((im.getRGB(x, y) >>> 24) < 16) {
                    n++;
                }
            }
        }
        return n;
    }

    /** {xMin, yMin, xMax, yMax} cua cac diem khong trong suot; toan trong suot -> {0,0,0,0}. */
    private static int[] vungVe(final BufferedImage im) {
        int x0 = Integer.MAX_VALUE, y0 = Integer.MAX_VALUE, x1 = -1, y1 = -1;
        for (int y = 0; y < im.getHeight(); y++) {
            for (int x = 0; x < im.getWidth(); x++) {
                if ((im.getRGB(x, y) >>> 24) >= 16) {
                    x0 = Math.min(x0, x);
                    y0 = Math.min(y0, y);
                    x1 = Math.max(x1, x);
                    y1 = Math.max(y1, y);
                }
            }
        }
        return x1 < 0 ? new int[] {0, 0, 0, 0} : new int[] {x0, y0, x1, y1};
    }

    private static boolean giongHet(final BufferedImage a, final BufferedImage b) {
        if (a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) {
            return false;
        }
        for (int y = 0; y < a.getHeight(); y++) {
            for (int x = 0; x < a.getWidth(); x++) {
                if (a.getRGB(x, y) != b.getRGB(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static BufferedImage doc(final File f) {
        try {
            return ImageIO.read(f);
        } catch (final IOException e) {
            return null;
        }
    }

    private KiemIconSkinThu() {
    }
}
