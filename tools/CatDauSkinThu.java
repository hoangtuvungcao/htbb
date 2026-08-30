import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.imageio.ImageIO;

/**
 * Cat DAU cua tung skin linh thu tu atlas ra lam icon hanh trang.
 *
 * Chay (tu thu muc goc repo):
 *     javac -d out_tools tools/CatDauSkinThu.java
 *     java -cp out_tools CatDauSkinThu
 *
 * VI SAO. Icon cu la ca con thu nhet vao 16x16 nen nhin ra mot cham nho, khong phan biet
 * duoc skin nao voi skin nao. Cat rieng cai dau roi de nguyen do phan giai goc thi dau to
 * gap khoang hai lan rui, va van la anh pixel sac net.
 *
 * BA RANG BUOC PHAI GIU:
 *  1. KHONG QUA 16x16. Luoi hanh trang co BUOC NHAY 18 px moi o
 *     (WindowInfoScr: "num4 % 3 * 18 + 54 + 9"), va icon duoc ve tu GOC TREN TRAI cua o
 *     (paintIcon -> drawRegion(..., x, y, 3) voi 3 = TOP|LEFT). Nen icon 18x18 lap kin o,
 *     khong con khe nao, cac icon dinh lien thanh mot dai - da thu va bi tra lai.
 *     16x16 chua 2 px khe, dung bang co icon cu.
 *     Con mot nguong nua o tren cao: GameData.paintIcon lam "if (h > 25) { h /= 3;
 *     soKhung = 3; }" - anh cao hon 25 px bi hieu la dai 3 khung hoat hinh va chi hien
 *     1/3 tren cung. Nguong 18 chat hon nen 25 khong bao gio cham toi, nhung dung nang
 *     kich thuoc len ma quen no.
 *  2. CAT DUNG DO PHAN GIAI GOC, khong phong to thu nho. Anh pixel ma thu nho la nhoe het.
 *  3. Giu kenh trong suot: ghi thang ARGB, khong dan len nen.
 *
 * NGUON. map/skinthu/<n>.png la atlas, map/skinthu/<n> la du lieu khung:
 *     byte soKhung, roi moi khung 5 byte: id, x, y, w, h   (deu khong dau)
 * Khung 0 luon la cai bong den. Khung 1 la hinh CHINH DIEN (nhin thay mat) - dung khung do.
 *
 * O CAT chon bang mat: dung tools/ ve khung chinh dien phong to kem thuoc toa do roi doc so.
 * Khong do tu dong duoc: canh/la/gac moc ra hai ben lam moi phep do theo be rong hang deu
 * sai, va nam con thu nay hinh dang khac han nhau.
 */
public final class CatDauSkinThu {

    private static final int CANH = 16;
    private static final int ID_ICON_DAU = 860;
    private static final int KHUNG_CHINH_DIEN = 1;

    /**
     * {x0, y0} goc tren trai cua o cat, tinh trong khung chinh dien cua tung skin.
     * Chon bang mat. Doi CANH thi nho doi theo de giu nguyen TAM: canh giam 2 px thi
     * moi so cong 1.
     */
    private static final int[][] O_CAT = {
        {19, 10},  // 0 Phuc Thu   - mu + hoa van vang + mat lon hong
        {11,  7},  // 1 Gau Truc   - tron mat gau truc, ca hai tai
        { 7,  8},  // 2 Tien Loc   - goc gac + mat trang mui hong
        { 6,  5},  // 3 Xich Duong - vien ngoc giua, lua toa deu hai ben
        { 6,  5},  // 4 Chien Lang - van vang chu V + hai mat do
    };

    public static void main(final String[] args) throws Exception {
        for (int n = 0; n < O_CAT.length; n++) {
            final byte[] dt = Files.readAllBytes(Paths.get("map/skinthu/" + n));
            final BufferedImage atlas = ImageIO.read(new File("map/skinthu/" + n + ".png"));
            final int p = 1 + KHUNG_CHINH_DIEN * 5;
            final int fx = dt[p + 1] & 0xFF;
            final int fy = dt[p + 2] & 0xFF;
            final int fw = dt[p + 3] & 0xFF;
            final int fh = dt[p + 4] & 0xFF;

            final int x0 = O_CAT[n][0];
            final int y0 = O_CAT[n][1];
            if (x0 + CANH > fw || y0 + CANH > fh) {
                throw new IllegalStateException("skin " + n + ": o cat " + x0 + "," + y0
                        + " vuot khung " + fw + "x" + fh);
            }

            final BufferedImage icon = new BufferedImage(CANH, CANH, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < CANH; y++) {
                for (int x = 0; x < CANH; x++) {
                    icon.setRGB(x, y, atlas.getRGB(fx + x0 + x, fy + y0 + y));
                }
            }
            final File ra = new File("map/icon/" + (ID_ICON_DAU + n) + ".png");
            ImageIO.write(icon, "png", ra);
            System.out.println("skin " + n + ": khung " + fw + "x" + fh + " o (" + fx + "," + fy
                    + "), cat (" + x0 + "," + y0 + ") " + CANH + "x" + CANH + " -> " + ra.getPath());
        }
    }

    private CatDauSkinThu() {
    }
}
