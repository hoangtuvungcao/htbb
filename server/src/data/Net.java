package data;

/**
 * Ban thay the (shim) cho class data.Net nam trong lib/htbb-core.jar.
 *
 * VI SAO CAN FILE NAY:
 *   Class data.Net goc (trong lib dong, khong co source) chua san bang URL tro ve
 *   cac server cua ben thu ba: 27.0.14.41 (nap the), 27.0.14.75 (auth, kem tai khoan
 *   teamobi/qwertyasdf ghi cung), 27.0.14.67 (quang cao), account.vtcmobile.com.vn,
 *   api.mobiplay.vn. Class do dung java.net.URL + HttpURLConnection nen goi ra Internet
 *   that. Hai thread quang cao trong data.Logdata (duoc khoi tao tu TeamServer) van goi
 *   Net.getHttpADV(...) moi vong lap -> ben kia co the day link tai game va noi dung tuy y
 *   vao chat NPC cua server nay.
 *
 *   Trong manifest, htbb-server.jar dung TRUOC lib/htbb-core.jar trong Class-Path,
 *   nen class nay se duoc nap thay cho class trong lib. Moi ket noi ra ngoai qua data.Net
 *   vi vay bi vo hieu, ke ca phan goi tu trong lib.
 *
 * LUU Y KHI SUA:
 *   Phai giu nguyen ten + chu ky (signature) cua tat ca thanh vien, dung nhu class goc:
 *     public static String[][] url;
 *     public static String getHttpADV(String);
 *     public static String getHttp(String);
 *     public static void   requestLink(String);
 *     public static int    getHttpAsync(String);
 *     public static String getUserName(String);
 *     public static String getUserID(String);
 *     public static String getAgenID(String);
 *   Thieu bat ky cai nao thi code trong lib se nem NoSuchMethodError luc chay.
 */
public class Net {

    /**
     * Bang URL cu (nap the, giftcode...). Nay de rong het.
     * Code trong src truy cap toi url[4][1] va url[1][4] nen bang phai du kich thuoc,
     * neu khong se bi ArrayIndexOutOfBoundsException.
     */
    public static String[][] url = new String[8][8];

    static {
        for (int i = 0; i < url.length; i++) {
            for (int j = 0; j < url[i].length; j++) {
                url[i][j] = "";
            }
        }
    }

    /**
     * Truoc day tai noi dung quang cao tu 27.0.14.67 roi day vao chat NPC.
     *
     * Tra ve "9;;": thread quang cao trong lib doc so dau tien, gap 9 thi bo qua dong do,
     * nen danh sach quang cao rong sach - khong loi, khong goi ra ngoai. Neu tra ve chuoi
     * rong thi Integer.parseInt("") se nem NumberFormatException trong thread cua lib.
     */
    public static String getHttpADV(final String urlstr) {
        return "9;;";
    }

    /** Truoc day GET mot URL bat ky (nap the / giftcode / su kien). Nay khong lam gi. */
    public static String getHttp(final String urlstr) {
        return "";
    }

    /** Truoc day day log dang nhap / nap tien sang server doi tac. Nay khong lam gi. */
    public static void requestLink(final String urlstr) {
    }

    /** Truoc day goi URL kieu async. Nay khong lam gi, tra ve -1 (that bai). */
    public static int getHttpAsync(final String urlstr) {
        return -1;
    }

    /** Truoc day tra cuu username qua 27.0.14.75. Nay tra ve rong. */
    public static String getUserName(final String urlstr) {
        return "";
    }

    /** Truoc day tra cuu userID qua 27.0.14.75. -3 = khong tim thay, dung nhu code cu mong doi. */
    public static String getUserID(final String urlstr) {
        return "-3";
    }

    /** Truoc day tra cuu dai ly qua api.mobiplay.vn. -1 = khong co. */
    public static String getAgenID(final String urlstr) {
        return "-1";
    }
}
