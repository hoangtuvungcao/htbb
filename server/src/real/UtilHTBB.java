package real;

/**
 * Ham tien ich dung chung (goc, khoang cach, ngau nhien, gio/ngay) cho code server.
 *
 * Ban cai dat that la lop real.UtilKPAH nam trong lib/NQSH_5h.jar (KHONG co source). Lop do
 * phai giu nguyen ten: cac lop chi co ban nhi phan trong NQSH_5h.jar (ItemShop,
 * RequestRegisterHandler, data.Logdata...) goi no theo ten. Code trong src/ goi qua lop nay
 * de khong lap lai ten cu; moi ham chi chuyen tiep, dung chung bo sinh so ngau nhien cua jar.
 */
public final class UtilHTBB {
    private UtilHTBB() {
    }

    public static int angle(int dx, int dy) {
        return UtilKPAH.angle(dx, dy);
    }

    public static int distance(int x1, int y1, int x2, int y2) {
        return UtilKPAH.distance(x1, y1, x2, y2);
    }

    public static int[] getDXDy(int goc, int tocDo) {
        return UtilKPAH.getDXDy(goc, tocDo);
    }

    public static int random(int n) {
        return UtilKPAH.random(n);
    }

    public static int getRandomMinMax(int min, int max) {
        return UtilKPAH.getRandomMinMax(min, max);
    }

    public static int getHour() {
        return UtilKPAH.getHour();
    }

    public static int getMinute() {
        return UtilKPAH.getMinute();
    }

    public static int getDayOfMonth() {
        return UtilKPAH.getDayOfMonth();
    }

    public static boolean isMonday() {
        return UtilKPAH.isMonday();
    }

    public static boolean isTuesday() {
        return UtilKPAH.isTuesday();
    }

    public static boolean isWednesay() {
        return UtilKPAH.isWednesay();
    }

    public static boolean isThursday() {
        return UtilKPAH.isThursday();
    }

    public static boolean isFriday() {
        return UtilKPAH.isFriday();
    }

    public static boolean isSaturday() {
        return UtilKPAH.isSaturday();
    }

    public static boolean isSunday() {
        return UtilKPAH.isSunday();
    }
}
