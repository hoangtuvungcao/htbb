package real;

/**
 * Kiem toc do goi di chuyen (lenh 4) bang "quy quang duong" theo thoi gian, thay cho cach cu
 * (CharCapNhat.doPlayerMove): moi goi toi da 50 px moi truc + Thread.sleep cho du 200 ms giua hai goi.
 *
 * Cach cu co hai loi gay giat / keo lui (bao 0.9.2 "di chuyen khi danh bi giat"):
 *  - Thread.sleep nam TRONG handler goi tin: hai goi di chuyen den sat nhau (mang dồn goi, chuyen binh
 *    thuong) lam luong xu ly cua phien ngu toi 200 ms -> moi goi sau do (ke ca lenh danh) bi tre theo,
 *    nguoi xung quanh thay nhan vat dung roi nhay. Do bang bot (scratch Bo.java): tre lenh 88 sau 2 goi
 *    di chuyen cach 10 ms = 202 ms; sau khi sua ~ 0-50 ms.
 *  - Nguong 50 px moi goi khong tinh thoi gian: goi den tre (client 250 ms + jitter) ma buoc dai hon chut
 *    la bi tra vi tri cu -> client bi keo lui.
 *
 * Nay: moi nhan vat co mot "quy" quang duong (milli-pixel) nap theo thoi gian voi toc do 1,5 lan toc do
 * that (toc do = px / khung 40 ms cua client) nhan ti le nguong cu (map 17 gap doi, bi lam cham thi nua),
 * toi da 2 lan nguong cu. Buoc hop le (truc lon nhat) <= quy thi nhan va tru quy; lon hon thi tu choi
 * nhu cu. Hack toc do van bi chan: trung binh khong vuot 1,5 lan toc do, cu nhay toi da 2 lan nguong.
 */
public final class TocDoDiChuyen {

    /** Ms moi khung cua client (Canvas: 25 khung / giay). */
    public static final int MS_KHUNG = 40;

    private TocDoDiChuyen() {
    }

    /** Quy toi da (milli-px) cho nguong goi {@code nguong} px. */
    public static long tran(final int nguong) {
        return 2000L * nguong;
    }

    /**
     * Quy sau khi nap them {@code troiMs} (tinh tu lan nhan cuoi). {@code quyCu} < 0 = chua co (day quy).
     * Toc do nap = 1,5 * tocDo px / 40 ms * nguong / 50 (milli-px / ms).
     */
    public static long nap(final long quyCu, final long troiMs, final int tocDo, final int nguong) {
        final long tranQuy = tran(nguong);
        if (quyCu < 0L) {
            return tranQuy;
        }
        final int td = Math.max(3, tocDo);
        final long them = Math.max(0L, Math.min(troiMs, 60000L)) * td * 1500L * nguong / (MS_KHUNG * 50L);
        return Math.min(tranQuy, quyCu + them);
    }

    /**
     * Quyet dinh mot buoc (dx, dy px) voi quy hien tai. Tra ve quy con lai (>= 0) neu nhan,
     * -1 neu tu choi (vuot nguong co dinh 2 lan, hoac vuot quy).
     */
    public static long buoc(final long quy, final int dx, final int dy, final int nguong) {
        final long d = Math.max(Math.abs(dx), Math.abs(dy)) * 1000L;
        if (d > tran(nguong) || d > quy) {
            return -1L;
        }
        return quy - d;
    }
}
