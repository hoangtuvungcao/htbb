package game.render.screen;

import java.util.Vector;

/**
 * Máy chủ của bản build: thuộc tính MANIFEST / htbb-app.properties "HTBB-May-Chu: host:cong" (build.sh của jar,
 * client_pc/build.sh, client_apk/pokemon/build.gradle luôn ghi). Mở màn chọn server thì danh sách = đúng một server
 * này (ghi vào RMS.KHO_DS_MAY_CHU như nút "Cập nhật" gốc). Không còn tải danh sách server của game cũ.
 */
final class TaiDanhSach {
    private static boolean daTai;

    private TaiDanhSach() {
    }

    /** "host:cong" gắn lúc build; null nếu thiếu / sai dạng. */
    static String mayChuThu() {
        try {
            String s = game.GameMidlet.instance == null ? null : game.GameMidlet.instance.getAppProperty("HTBB-May-Chu");
            if (s == null) {
                // vai trinh gia lap (chay jar khong JAD, giả lập web) khong tra thuoc tinh MANIFEST qua getAppProperty:
                // doc thang META-INF/MANIFEST.MF trong jar
                s = docManifest("HTBB-May-Chu");
            }
            if (s == null) {
                return null;
            }
            s = s.trim();
            int i = s.lastIndexOf(':');
            if (i <= 0 || i == s.length() - 1) {
                return null;
            }
            Short.parseShort(s.substring(i + 1).trim());
            return s;
        } catch (Throwable e) {
            return null;
        }
    }

    /** Đọc một thuộc tính trong /META-INF/MANIFEST.MF của jar (CLDC: không có java.util.jar). null nếu không có. */
    static String docManifest(String khoa) {
        java.io.InputStream in = null;
        try {
            in = (game.GameMidlet.instance != null ? game.GameMidlet.instance.getClass() : "".getClass()).getResourceAsStream("/META-INF/MANIFEST.MF");   // lop cua MIDlet: tai nguyen trong jar game (CLDC: khong co class literal)
            if (in == null) {
                return null;
            }
            java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
            byte[] b = new byte[512];
            int n;
            while ((n = in.read(b)) > 0) {
                bo.write(b, 0, n);
            }
            String t = new String(bo.toByteArray(), "UTF-8");
            String dau = khoa + ":";
            int i = 0;
            while (i < t.length()) {
                int j = t.indexOf('\n', i);
                if (j < 0) {
                    j = t.length();
                }
                String dong = t.substring(i, j).trim();
                if (dong.startsWith(dau)) {
                    return dong.substring(dau.length()).trim();
                }
                i = j + 1;
            }
        } catch (Throwable e) {
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Throwable e) {
            }
        }
        return null;
    }

    /**
     * pokemon: tên server hiện trên màn chọn. Danh sách lưu RMS từ bản cũ có thể mang tên server của
     * game gốc: hiện thành "Máy chủ n" để giao diện không lộ tên cũ.
     */
    static String tenHien(String ten, int i) {
        if (ten == null) {
            return "";
        }
        String hoa = ten.toUpperCase();
        // hai chuỗi dưới là tên server game gốc cần LỌC khỏi RMS cũ (không vẽ) - ngoại lệ du_lieu/ten_cu_cho_phep.csv
        if (hoa.indexOf("NQSH") >= 0 || hoa.indexOf("KPAH") >= 0) {
            return "Máy chủ " + (i + 1);
        }
        return ten;
    }

    /** Mở màn chọn server lần đầu: đặt danh sách = server gắn lúc build. */
    static void taiNgam() {
        if (daTai) {
            return;
        }
        daTai = true;
        String thu = mayChuThu();
        if (thu == null) {
            return;
        }
        int i = thu.lastIndexOf(':');
        String ten = "Huyền Thoại Bảo Bối";
        try {
            if ("1".equals(game.GameMidlet.instance.getAppProperty("HTBB-Ban-Thu"))) {
                ten = "Thử nghiệm";
            }
        } catch (Throwable e) {
            // giu ten mac dinh
        }
        apDung(ten + "|" + thu.substring(0, i).trim() + "|" + thu.substring(i + 1).trim());
    }

    /** Nút "Cập nhật": danh sách cố định theo bản build - không tải gì. */
    static void capNhat() {
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
