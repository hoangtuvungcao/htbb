package data;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * QUY TAC DANG KY TAI KHOAN - ban dich 1-1 cua HTBB-Web/shared/dang-ky/kiem-tra.ts.
 *
 * Du lieu (do dai, ten quan tri cam, tu cam, han muc, cau bao loi) KHONG nam o day ma o
 * QuyTacDangKyDuLieu (sinh tu HTBB-Web/shared/data/quy-tac-dang-ky.json - nguon DUY NHAT cho ca
 * web lan dang ky trong game). Sua logic o day thi PHAI sua ben TS: ca hai chay chung bo ca
 * THU_NGHIEM (npm test ben web, test/KiemDangKyTrongGame.java ben nay).
 *
 * Ham thuan, khong dung CSDL - de kiem thu khong can server.
 */
public final class QuyTacDangKy {

    public static final String NGUON_WEB = "web";
    public static final String NGUON_GAME = "game";

    private static final Pattern EMAIL = Pattern.compile("^([A-Za-z0-9!#$%&'*+/=?^_`{|}~.-]{1,64})@([A-Za-z0-9.-]+)$");
    private static final Pattern NHAN_MIEN = Pattern.compile("^[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?$");

    /** Mot loi nhap lieu: ma (khoa thong bao) + cau bao tieng Viet. */
    public static final class Loi {
        public final String ma;
        public final String loi;

        Loi(final String ma) {
            this.ma = ma;
            this.loi = thongBao(ma);
        }
    }

    /** Cau bao theo ma, thay {min}/{max}. */
    public static String thongBao(final String ma) {
        String mau = ma;
        for (int i = 0; i < QuyTacDangKyDuLieu.THONG_BAO_MA.length; i++) {
            if (QuyTacDangKyDuLieu.THONG_BAO_MA[i].equals(ma)) {
                mau = QuyTacDangKyDuLieu.THONG_BAO_CHU[i];
                break;
            }
        }
        int min;
        int max;
        if (ma.startsWith("ten")) {
            min = QuyTacDangKyDuLieu.TEN_MIN;
            max = QuyTacDangKyDuLieu.TEN_MAX;
        } else if (ma.startsWith("matKhau")) {
            min = QuyTacDangKyDuLieu.MAT_KHAU_MIN;
            max = QuyTacDangKyDuLieu.MAT_KHAU_MAX;
        } else {
            return mau;
        }
        return mau.replace("{min}", String.valueOf(min)).replace("{max}", String.valueOf(max));
    }

    public static boolean tenDungDang(final String u) {
        if (u == null || u.length() < QuyTacDangKyDuLieu.TEN_MIN || u.length() > QuyTacDangKyDuLieu.TEN_MAX) {
            return false;
        }
        for (int i = 0; i < u.length(); i++) {
            final char c = u.charAt(i);
            if (!((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9'))) {
                return false;
            }
        }
        return true;
    }

    private static int soByteUtf8(final String s) {
        int n = 0;
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c < 0x80) {
                n += 1;
            } else if (c < 0x800) {
                n += 2;
            } else if (Character.isHighSurrogate(c)) {
                n += 4;
                i++;
            } else {
                n += 3;
            }
        }
        return n;
    }

    public static boolean matKhauHopLe(final String p) {
        if (p == null) {
            return false;
        }
        final int n = soByteUtf8(p);
        if (n < QuyTacDangKyDuLieu.MAT_KHAU_MIN || n > QuyTacDangKyDuLieu.MAT_KHAU_MAX) {
            return false;
        }
        for (int i = 0; i < p.length(); i++) {
            final char c = p.charAt(i);
            if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9'))) {
                return false;
            }
        }
        for (final String cam : QuyTacDangKyDuLieu.MAT_KHAU_CAM) {
            if (cam.equalsIgnoreCase(p)) {
                return false;
            }
        }
        return true;
    }

    /** Nguyen goc, doi so giong chu (d1t -> dit), bo het so. */
    private static String[] cacDang(final String u) {
        final StringBuilder doi = new StringBuilder(u.length());
        final StringBuilder boSo = new StringBuilder(u.length());
        for (int i = 0; i < u.length(); i++) {
            final char ch = u.charAt(i);
            final int k = QuyTacDangKyDuLieu.THAY_THE_SO_TU.indexOf(ch);
            doi.append(k >= 0 ? QuyTacDangKyDuLieu.THAY_THE_SO_THANH.charAt(k) : ch);
            if (ch < '0' || ch > '9') {
                boSo.append(ch);
            }
        }
        return new String[]{u, doi.toString(), boSo.toString()};
    }

    /** "tenQuanTri" | "tenTuCam" | null. */
    public static String tenBiCam(final String u) {
        final String[] ds = cacDang(u);
        for (final String t : ds) {
            for (final String b : QuyTacDangKyDuLieu.TEN_QUAN_TRI_BANG) {
                if (t.equals(b)) {
                    return "tenQuanTri";
                }
            }
            for (final String b : QuyTacDangKyDuLieu.TEN_QUAN_TRI_BAT_DAU) {
                if (t.startsWith(b)) {
                    return "tenQuanTri";
                }
            }
            for (final String c : QuyTacDangKyDuLieu.TEN_QUAN_TRI_CHUA) {
                if (t.contains(c)) {
                    return "tenQuanTri";
                }
            }
        }
        for (final String t : ds) {
            for (final String w : QuyTacDangKyDuLieu.TU_CAM) {
                if (t.contains(w)) {
                    return "tenTuCam";
                }
            }
        }
        return null;
    }

    public static boolean emailHopLe(final String email) {
        if (email == null || email.length() > QuyTacDangKyDuLieu.EMAIL_TOI_DA) {
            return false;
        }
        final Matcher m = EMAIL.matcher(email);
        if (!m.matches()) {
            return false;
        }
        final String local = m.group(1);
        final String domain = m.group(2);
        if (local.startsWith(".") || local.endsWith(".") || local.contains("..")) {
            return false;
        }
        final String[] nhan = domain.split("\\.", -1);
        if (nhan.length < 2) {
            return false;
        }
        for (final String n : nhan) {
            if (!NHAN_MIEN.matcher(n).matches()) {
                return false;
            }
        }
        return true;
    }

    /** Moi loi nhap lieu theo thu tu o tren form (ten, mat khau, nhap lai, email). Rong = hop le. */
    public static List<Loi> kiemTra(final String username, final String password, final String confirm,
                                    final String email, final String nguon) {
        final List<Loi> ds = new ArrayList<Loi>();
        final String u = username == null ? "" : username;
        final String p = password == null ? "" : password;
        final String c = confirm == null ? "" : confirm;
        final String e = email == null ? "" : email;
        if (!tenDungDang(u)) {
            ds.add(new Loi("tenSai"));
        } else {
            final String cam = tenBiCam(u);
            if (cam != null) {
                ds.add(new Loi(cam));
            }
        }
        if (!matKhauHopLe(p)) {
            ds.add(new Loi("matKhauSai"));
        }
        if (!p.equals(c)) {
            ds.add(new Loi("matKhauKhongKhop"));
        }
        final boolean batBuoc = NGUON_WEB.equals(nguon)
                ? QuyTacDangKyDuLieu.EMAIL_BAT_BUOC_WEB : QuyTacDangKyDuLieu.EMAIL_BAT_BUOC_GAME;
        if (e.isEmpty()) {
            if (batBuoc) {
                ds.add(new Loi("emailThieu"));
            }
        } else if (!emailHopLe(e)) {
            ds.add(new Loi("emailSai"));
        }
        return ds;
    }

    /** "quaGio" | "quaNgay" | null - xem vuotHanMuc ben TS. Dem gop web + game cung IP. */
    public static String vuotHanMuc(final int demGio, final int demNgay, final int dangChay) {
        if (demNgay + dangChay >= QuyTacDangKyDuLieu.HAN_MUC_MOI_NGAY) {
            return "quaNgay";
        }
        if (demGio + dangChay >= QuyTacDangKyDuLieu.HAN_MUC_MOI_GIO) {
            return "quaGio";
        }
        return null;
    }

    private QuyTacDangKy() {
    }
}
