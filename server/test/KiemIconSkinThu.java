import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Icon vat pham skin linh thu (map/icon/860..864.png).
 *
 * CAI BAY PHAI GIU: GameData.paintIcon ben client lam the nay
 *
 *     int h = anh.getHeight();
 *     int soKhung = 1;
 *     if (h > 25) { h /= 3; soKhung = 3; }      // coi la dai 3 khung hoat hinh
 *     g.drawRegion(anh, 0, khungHienTai * h, w, h, ...);
 *
 * Nghia la anh icon CAO HON 25 px bi hieu la dai ba khung xep doc, va client chi ve
 * mot phan ba tren cung. Cat icon dau to hon mot chut ma vo tinh vuot 25 la icon hien
 * ra cut ngang, khong ai doan duoc nguyen nhan.
 *
 * Icon dau duoc sinh bang tools/CatDauSkinThu.java, cat thang tu atlas map/skinthu/<n>.png
 * dung do phan giai goc (khong phong to thu nho, vi anh pixel thu nho la nhoe).
 */
public final class KiemIconSkinThu {

    /** Nguong client coi anh la dai 3 khung hoat hinh. */
    private static final int CAO_TOI_DA = 25;
    /** Buoc nhay giua hai o hanh trang (WindowInfoScr: "num4 % 3 * 18 + 54 + 9"). */
    private static final int BUOC_LUOI = 18;
    private static final int ID_DAU = 860;
    private static final int SO_SKIN = 5;
    private static final int CANH = 16;

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
            Kiem.bang("icon " + id + " rong " + CANH + "px",
                    Integer.valueOf(CANH), Integer.valueOf(im.getWidth()));
            Kiem.bang("icon " + id + " cao " + CANH + "px",
                    Integer.valueOf(CANH), Integer.valueOf(im.getHeight()));
            // Hai phep chan doc lap voi con so 16, giu ca khi sau nay doi cach cat.
            //
            // Icon phai NHO HON o mot chut, khong thi cac icon dinh lien thanh mot dai.
            // Da di dung vet nay: ban 18x18 lap kin o (buoc luoi cung 18, icon ve tu goc
            // tren trai) nen trong hanh trang khong con khe nao giua cac o.
            Kiem.dung("icon " + id + " con khe voi o ben canh (<= " + (BUOC_LUOI - 2) + "px)",
                    im.getWidth() <= BUOC_LUOI - 2 && im.getHeight() <= BUOC_LUOI - 2);
            Kiem.dung("icon " + id + " cao <= " + CAO_TOI_DA
                    + " (cao hon la client cat con mot phan ba)", im.getHeight() <= CAO_TOI_DA);
            // Chan dung kieu hong that: cat truot ra vung rong (icon trong tron) hoac ra
            // mot mang mot mau (khong phai hinh con thu). KHONG doi phai co diem trong
            // suot - dau Gau Truc lap kin 18x18 va do la ket qua dung.
            Kiem.dung("icon " + id + " co noi dung that (nhieu hon 8 mau)", soMau(im) > 8);
        }

        // Cong cu sinh lai phai con day de con dung lai duoc.
        Kiem.dung("con tools/CatDauSkinThu.java de sinh lai icon",
                new File("tools/CatDauSkinThu.java").isFile());
    }

    /** So mau khac nhau (ke ca trong suot). Cat truot ra vung rong thi con dung 1 mau. */
    private static int soMau(final BufferedImage im) {
        final java.util.Set<Integer> mau = new java.util.HashSet<Integer>();
        for (int y = 0; y < im.getHeight(); y++) {
            for (int x = 0; x < im.getWidth(); x++) {
                mau.add(Integer.valueOf(im.getRGB(x, y)));
            }
        }
        return mau.size();
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
