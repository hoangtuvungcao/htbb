package real;

/**
 * Dem goi tin client bi cut ngan (EOFException khi doc truong): bo goi, ghi MOT dong canh bao gop moi 10 phut
 * thay vi stack loi (truoc: moi goi sai tu client cu / bot thao tac bat thuong in "E java.io.EOFException").
 */
public final class GoiTinCut {
    private static final java.util.concurrent.atomic.AtomicLong DEM = new java.util.concurrent.atomic.AtomicLong();
    private static volatile long lanGhi;

    private GoiTinCut() {
    }

    static void dem(Char player, io.Message message) {
        dem(player == null ? "?" : player.charname, message == null ? -1 : message.command);
    }

    public static void dem(String ten, int lenh) {
        long n = DEM.incrementAndGet();
        long bay = System.currentTimeMillis();
        if (bay - lanGhi >= 600000L) {
            lanGhi = bay;
            System.out.println("[GOI_CUT] goi tin cut ngan da bo: " + n + " (gan nhat lenh " + lenh + " cua " + ten + ")");
        }
    }

    public static long tong() {
        return DEM.get();
    }
}
