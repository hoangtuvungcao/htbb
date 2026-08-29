import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Kiem thu menu NPC dau truong (-31) sau khi go 4 muc.
 *
 * VI SAO CAN TEST NAY: client gui ve VI TRI NGUOI CHOI NHIN THAY tren menu, con
 * MapDoMenu case -31 lai phan nhanh theo vi tri GOC. Bo bot muc giua menu la moi muc
 * phia sau tut len, va neu khong doi lai thi chon "Nui chau bau" se chay nham nhanh
 * "Dang ky chien truong" — khong bao loi, khong ai biet, chi la bam mot dang ra mot neo.
 *
 * Cau noi hai file la MapDoMenu.VI_TRI_MENU_DAU_TRUONG. Test nay doc THANG MA NGUON de
 * bat truong hop ai do sua menu o file nay ma quen sua bang o file kia.
 *
 * Doc ma nguon chu khong goi ham vi MapDoMenu la lop package-private trong goi `real`,
 * con test thi nam o goi mac dinh nen khong voi toi duoc.
 */
public final class KiemMenuDauTruong {

    private static final String F_MENU = "src/real/cmd/RequestNPCInfoHandler.java";
    private static final String F_XU_LY = "src/real/MapDoMenu.java";

    /** Bon muc da go - khong duoc xuat hien lai trong menu dau truong. */
    private static final String[] DA_GO = {
        "Đăng ký chiến trường",
        "Nhận quà khu liên đấu",
        "Nhận quà Liên trảm",
        "Nhận quà top trụ",
    };

    public static void chay() {
        Kiem.nhom("NPC dau truong (-31) - menu va bang doi vi tri");

        final String maMenu = doc(F_MENU);
        final String maXuLy = doc(F_XU_LY);
        if (maMenu == null || maXuLy == null) {
            Kiem.dung("bo qua: khong doc duoc ma nguon (chay tu thu muc goc du an)", true);
            return;
        }

        // ---- 1. Bon muc da go khong con trong menu ----
        final List<String[]> cacMenu = docCacMenuDauTruong(maMenu);
        Kiem.dung("tim thay menu dau truong trong " + F_MENU, !cacMenu.isEmpty());

        for (final String bo : DA_GO) {
            boolean con = false;
            for (final String[] m : cacMenu) {
                for (final String muc : m) {
                    if (muc.equals(bo)) {
                        con = true;
                    }
                }
            }
            Kiem.sai("da go khoi menu: " + bo, con);
        }

        // ---- 2. Bang doi vi tri phai du dai cho moi muc con lai ----
        final int[] bang = docBangViTri(maXuLy);
        Kiem.dung("tim thay VI_TRI_MENU_DAU_TRUONG trong " + F_XU_LY, bang.length > 0);

        int daiNhat = 0;
        for (final String[] m : cacMenu) {
            if (m.length > daiNhat) {
                daiNhat = m.length;
            }
        }
        Kiem.dung("bang doi vi tri du dai cho menu dai nhat ("
                        + daiNhat + " muc, bang co " + bang.length + " o)",
                bang.length >= daiNhat);

        // ---- 3. Bang phai tang dan: menu giu dung thu tu cu, chi bot muc ----
        boolean tangDan = true;
        for (int i = 1; i < bang.length; i++) {
            if (bang[i] <= bang[i - 1]) {
                tangDan = false;
            }
        }
        Kiem.dung("bang doi vi tri tang dan (menu giu dung thu tu goc)", tangDan);

        // ---- 4. Hai muc dau khong bi doi vi tri ----
        // "Thach dau ca nhan" va "Tran yeu tran" nam truoc cho bi go nen phai giu nguyen 0, 1.
        // RequestNPCInfoHandler con ghi de menu[0] / menu[1] theo trang thai nguoi choi
        // (Vao lai dau truong / Vao lai chien truong) nen hai o nay bat buoc dung cho.
        Kiem.bang("vi tri hien thi 0 van tro ve goc 0", Integer.valueOf(0),
                Integer.valueOf(bang.length > 0 ? bang[0] : -1));
        Kiem.bang("vi tri hien thi 1 van tro ve goc 1", Integer.valueOf(1),
                Integer.valueOf(bang.length > 1 ? bang[1] : -1));

        // ---- 5. Muc thu ba bay gio la "Nui chau bau", goc cua no la 6 ----
        Kiem.bang("vi tri hien thi 2 tro ve goc 6 (Nui chau bau)", Integer.valueOf(6),
                Integer.valueOf(bang.length > 2 ? bang[2] : -1));
        // MOI ban menu deu phai co "Núi châu báu" o dung vi tri 2 — dung "co mot ban nao
        // do dung" la du, vi nhu vay them mot muc vao mot ban thoi cung khong bi bat.
        boolean moiBanDeuDung = !cacMenu.isEmpty();
        for (final String[] m : cacMenu) {
            if (m.length <= 2 || !m[2].equals("Núi châu báu")) {
                moiBanDeuDung = false;
            }
        }
        Kiem.dung("MOI ban menu deu co \"Núi châu báu\" o dung vi tri hien thi 2",
                moiBanDeuDung);

        // ---- 6. Nhanh xu ly goc van con nguyen, chua bi danh so lai ----
        Kiem.dung("case -31 van con nhanh vi tri goc 6 (idOptionMenu != 6)",
                maXuLy.contains("idOptionMenu != 6"));
        Kiem.dung("case -31 co chan bien truoc khi doi vi tri",
                maXuLy.contains("idOptionMenu >= VI_TRI_MENU_DAU_TRUONG.length"));
    }

    /** Lay cac mang menu cua NPC dau truong: nhan dang bang muc dau "Thách đấu cá nhân". */
    private static List<String[]> docCacMenuDauTruong(final String ma) {
        final List<String[]> ra = new ArrayList<String[]>();
        final Pattern p = Pattern.compile("new String\\[\\]\\s*\\{([^}]*\"Thách đấu cá nhân\"[^}]*)\\}");
        final Matcher m = p.matcher(ma);
        while (m.find()) {
            ra.add(tachChuoi(m.group(1)));
        }
        return ra;
    }

    /** Tach cac chuoi trong "a", "b", "c" thanh mang. */
    private static String[] tachChuoi(final String than) {
        final List<String> ra = new ArrayList<String>();
        final Matcher m = Pattern.compile("\"([^\"]*)\"").matcher(than);
        while (m.find()) {
            ra.add(m.group(1));
        }
        return ra.toArray(new String[0]);
    }

    private static int[] docBangViTri(final String ma) {
        final Matcher m = Pattern.compile(
                "VI_TRI_MENU_DAU_TRUONG\\s*=\\s*\\{([^}]*)\\}").matcher(ma);
        if (!m.find()) {
            return new int[0];
        }
        final List<Integer> ra = new ArrayList<Integer>();
        final Matcher so = Pattern.compile("-?\\d+").matcher(m.group(1));
        while (so.find()) {
            ra.add(Integer.valueOf(so.group()));
        }
        final int[] kq = new int[ra.size()];
        for (int i = 0; i < kq.length; i++) {
            kq[i] = ra.get(i).intValue();
        }
        return kq;
    }

    private static String doc(final String duongDan) {
        final File f = new File(duongDan);
        if (!f.isFile()) {
            return null;
        }
        Reader r = null;
        try {
            r = new InputStreamReader(new FileInputStream(f), "UTF-8");
            final StringBuilder sb = new StringBuilder();
            final char[] dem = new char[8192];
            int n;
            while ((n = r.read(dem)) > 0) {
                sb.append(dem, 0, n);
            }
            return sb.toString();
        } catch (final Exception e) {
            return null;
        } finally {
            try {
                if (r != null) {
                    r.close();
                }
            } catch (final Exception ignored) {
            }
        }
    }

    private KiemMenuDauTruong() {
    }
}
