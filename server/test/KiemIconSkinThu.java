import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Icon vat pham skin linh thu (map/icon/860..864.png) - ve CA CON THU.
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
 * 2. GameData.paintIcon con lam "if (h > 25) { h /= 3; soKhung = 3; }" - anh CAO HON
 *    25 px bi hieu la dai BA KHUNG hoat hinh va client chi ve mot phan ba tren cung.
 *    Do la ly do trong map/icon co 37 tep 18x54. Nang chieu cao icon len qua 25 ma
 *    khong co du ba khung la icon hien ra cut ngang.
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

            Kiem.dung("icon " + id + " khong lap kin o (<= " + (BUOC_LUOI - 2) + "px)",
                    im.getWidth() <= BUOC_LUOI - 2 && im.getHeight() <= BUOC_LUOI - 2);
            Kiem.dung("icon " + id + " cao <= " + CAO_TOI_DA
                    + " (cao hon la client cat con mot phan ba)", im.getHeight() <= CAO_TOI_DA);
            // Cat truot ra vung rong thi con dung 1 mau, cat vao mang phang thi 2-3.
            Kiem.dung("icon " + id + " co noi dung that (>= 4 mau)", soMau(im) >= 4);
            // Ve ca con thu thi quanh hinh phai con vien trong suot - do la thu lam icon
            // nhin thoang trong o. Ban cat rieng cai dau thi lap kin, va da bi tra lai.
            Kiem.dung("icon " + id + " con vien trong suot quanh hinh", soDiemTrong(im) > 0);
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
