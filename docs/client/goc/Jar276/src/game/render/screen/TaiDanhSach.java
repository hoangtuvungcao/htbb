package game.render.screen;

import game.render.Canvas;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

/**
 * Danh sách server lấy từ GitHub (mỗi dòng "Tên|host|port", dòng trống / # bỏ qua), thay
 * cho danh sách ghi cứng trong jar: đổi IP / tên miền server chỉ cần sửa file trên GitHub.
 * Tải xong thì ghi vào RMS "ipnqsh" như nút "Cập nhật" gốc, lần sau mở game không có mạng
 * vẫn dùng danh sách đã lưu.
 */
final class TaiDanhSach {
    /**
     * Thử lần lượt: http trước (máy J2ME / giả lập như CoffeeCat không nói được TLS đời mới nên
     * HTTPS hay hỏng), không được thì https (giả lập trên PC đọc được).
     */
    private static final String[] LINK = {"http://nqsh-h2.top/robots.txt", "https://nqsh-h2.top/robots.txt"};
    /** Danh sách ghi sẵn: dùng khi không tải được LINK nào - không có thì client không biết vào server nào. */
    private static final String MAC_DINH_HOST = "nqsh-h2.top";
    private static final short MAC_DINH_CONG = 30774;
    private static final String MAC_DINH = "NQSH-H2|" + MAC_DINH_HOST + "|" + MAC_DINH_CONG;
    private static boolean daTai;
    private static boolean dangTai;

    private TaiDanhSach() {
    }

    /** Mở màn chọn server lần đầu: tải ngầm một lần, không hiện gì. */
    static void taiNgam() {
        if (daTai) {
            return;
        }
        daTai = true;
        // có ngay server ghi sẵn, không phải đợi tải (HTTPS trên máy J2ME có thể treo lâu rồi mới hỏng)
        try {
            if (!TaiDanhSach.coMacDinh()) {
                apDung(MAC_DINH);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        tai(false);
    }

    /** Nút "Cập nhật": tải lại, hiện chờ / báo lỗi như bản gốc. */
    static void capNhat() {
        Canvas.startWaitDlg();
        tai(true);
    }

    private static void tai(final boolean bl) {
        if (dangTai) {
            return;
        }
        dangTai = true;
        new Thread(){

            public void run() {
                boolean bl2 = false;
                for (int i = 0; i < LINK.length && !bl2; ++i) {
                    try {
                        bl2 = apDung(doc(LINK[i]));
                    }
                    catch (Throwable throwable) {
                        // link này hỏng (TLS / mạng / WAF): thử link kế, hết thì giữ danh sách cũ
                    }
                }
                if (!bl2) {
                    try {
                        bl2 = TaiDanhSach.coMacDinh() || apDung(MAC_DINH);
                    }
                    catch (Throwable throwable) {
                        // empty catch block
                    }
                }
                dangTai = false;
                if (bl) {
                    if (bl2) {
                        Canvas.endDlg();
                    } else {
                        Canvas.startOKDlg("Không thể kết nối, xin kiểm tra lại GPRS/3G/Wifi.");
                    }
                }
            }
        }.start();
    }

    /**
     * Danh sách đang dùng đã có server ghi sẵn (đúng host VÀ cổng) chưa - thì giữ nguyên, kể cả server
     * khác đã lưu. So cả cổng: danh sách cũ đã lưu "nqsh-h2.top|17949" phải được thay bằng cổng mới.
     */
    private static boolean coMacDinh() {
        String[] stringArray = ServerListScr.address;
        short[] sArray = ServerListScr.port;
        if (stringArray == null || sArray == null) {
            return false;
        }
        for (int i = 0; i < stringArray.length && i < sArray.length; ++i) {
            if (MAC_DINH_HOST.equals(stringArray[i]) && sArray[i] == MAC_DINH_CONG) {
                return true;
            }
        }
        return false;
    }

    private static String doc(String link) throws Exception {
        HttpConnection httpConnection = null;
        InputStream inputStream = null;
        try {
            httpConnection = (HttpConnection)Connector.open(link);
            if (httpConnection.getResponseCode() != 200) {
                return null;
            }
            inputStream = httpConnection.openInputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] byArray = new byte[512];
            int n;
            while ((n = inputStream.read(byArray)) > 0 && byteArrayOutputStream.size() < 16384) {
                byteArrayOutputStream.write(byArray, 0, n);
            }
            return new String(byteArrayOutputStream.toByteArray(), "UTF-8");
        }
        finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (httpConnection != null) {
                    httpConnection.close();
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }

    private static boolean apDung(String string) {
        if (string == null) {
            return false;
        }
        Vector vector = new Vector();
        int n = 0;
        while (n < string.length()) {
            int n2 = string.indexOf(10, n);
            if (n2 < 0) {
                n2 = string.length();
            }
            String string2 = string.substring(n, n2).trim();
            n = n2 + 1;
            if (string2.length() == 0 || string2.charAt(0) == '#') continue;
            int n3 = string2.indexOf(124);
            int n4 = n3 < 0 ? -1 : string2.indexOf(124, n3 + 1);
            if (n4 < 0) continue;
            try {
                String[] stringArray = new String[]{string2.substring(0, n3).trim(), string2.substring(n3 + 1, n4).trim(), String.valueOf(Short.parseShort(string2.substring(n4 + 1).trim()))};
                if (stringArray[0].length() == 0 || stringArray[1].length() == 0) continue;
                vector.addElement(stringArray);
            }
            catch (Throwable throwable) {
                // dòng hỏng thì bỏ
            }
        }
        if (vector.size() == 0) {
            return false;
        }
        int n5 = vector.size();
        String[] stringArray = new String[n5];
        String[] stringArray2 = new String[n5];
        short[] sArray = new short[n5];
        byte[] byArray = new byte[n5];
        for (int i = 0; i < n5; ++i) {
            String[] stringArray3 = (String[])vector.elementAt(i);
            stringArray[i] = stringArray3[0];
            stringArray2[i] = stringArray3[1];
            sArray[i] = Short.parseShort(stringArray3[2]);
        }
        ServerListScr.address = stringArray2;
        ServerListScr.port = sArray;
        ServerListScr.index_server = byArray;
        ServerListScr.nameServer = stringArray;
        ServerListScr.saveIP();
        ServerListScr.gI().init();
        return true;
    }
}
