package com.htbb.cauhinh;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Kiem + chuan hoa MOT gia tri theo dinh nghia khoa. THUAN (khong CSDL) - test/KiemCauHinhTrungTam goi
 * truc tiep. Ban web cung luat: HTBB-Web/shared/utils/cauHinhTrungTam.ts#kiemGiaTriTT (test hai ben
 * doi chieu cung bo vi du).
 *
 * KHONG kep ngam: ngoai khoang = loi (gia tri bi tu choi, may chu giu gia tri tot gan nhat).
 */
public final class KiemGiaTri {

    private static final Pattern SO = Pattern.compile("^-?(0|[1-9]\\d{0,17})$");
    private static final Pattern NHIP = Pattern.compile("^([1-9]\\d{0,5})/([1-9]\\d{0,5})$");
    private static final Pattern NGAY = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final Pattern NGAY_GIO = Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$");
    /** Cot gia_tri cua bang cau_hinh la varchar(255). */
    public static final int DO_DAI_COT = 255;

    private KiemGiaTri() {
    }

    /** Ket qua kiem: dung = giaTri da chuan hoa, loi = null; sai = giaTri null, loi = ly do (tieng Viet). */
    public static final class KetQua {
        public final String giaTri;
        public final String loi;

        KetQua(final String g, final String l) {
            this.giaTri = g;
            this.loi = l;
        }

        public boolean dung() {
            return loi == null;
        }
    }

    static KetQua dung(final String g) {
        return new KetQua(g, null);
    }

    static KetQua sai(final String l) {
        return new KetQua(null, l);
    }

    public static KetQua kiem(final DinhNghiaKhoa d, final String tho) {
        if (tho == null) {
            return sai("thiếu giá trị");
        }
        String v = tho.trim();
        if (d.chuHoa()) {
            v = v.toUpperCase(Locale.ROOT);
        }
        if (v.length() > DO_DAI_COT) {
            return sai("dài quá " + DO_DAI_COT + " ký tự");
        }
        for (int i = 0; i < v.length(); i++) {
            if (Character.isISOControl(v.charAt(i))) {
                return sai("có ký tự điều khiển / xuống dòng");
            }
        }
        final String k = d.kieu;
        if ("co".equals(k)) {
            return "0".equals(v) || "1".equals(v) ? dung(v) : sai("chỉ nhận 0 (tắt) hoặc 1 (bật)");
        }
        if ("so".equals(k)) {
            return kiemSo(d, v);
        }
        if ("chon".equals(k)) {
            return d.trongLuaChon(v) ? dung(v) : sai("không nằm trong danh sách lựa chọn");
        }
        if ("chuoi".equals(k)) {
            if (v.codePointCount(0, v.length()) > d.doDai()) {
                return sai("tối đa " + d.doDai() + " ký tự");
            }
            if (d.mauRegex() != null && !d.mauRegex().matcher(v).matches()) {
                return sai("sai định dạng");
            }
            return dung(v);
        }
        if ("ds_chuoi".equals(k)) {
            final Set<String> ds = new LinkedHashSet<String>();
            for (final String phan : v.split(",")) {
                final String x = phan.trim();
                if (x.isEmpty()) {
                    continue;
                }
                if (d.mauRegex() != null && !d.mauRegex().matcher(x).matches()) {
                    return sai("phần tử \"" + x + "\" sai định dạng");
                }
                if (d.coLuaChon() && !d.trongLuaChon(x)) {
                    return sai("phần tử \"" + x + "\" không nằm trong danh sách lựa chọn");
                }
                ds.add(x);
            }
            final String ra = noi(ds.toArray(new String[0]));
            if (ra.length() > d.doDai()) {
                return sai("tối đa " + d.doDai() + " ký tự");
            }
            return dung(ra);
        }
        if ("ds_so".equals(k)) {
            if (v.isEmpty()) {
                return sai("cần ít nhất một số");
            }
            final String[] phan = v.split(",");
            if (d.soPhanTu() > 0 && phan.length != d.soPhanTu()) {
                return sai("cần đúng " + d.soPhanTu() + " số");
            }
            final String[] ra = new String[phan.length];
            for (int i = 0; i < phan.length; i++) {
                final KetQua s = kiemSo(d, phan[i].trim());
                if (!s.dung()) {
                    return sai("số thứ " + (i + 1) + ": " + s.loi);
                }
                ra[i] = s.giaTri;
            }
            return dung(noi(ra));
        }
        if ("ngay".equals(k)) {
            return v.isEmpty() || (NGAY.matcher(v).matches() && ngayHopLe(v, "yyyy-MM-dd")) ? dung(v) : sai("cần yyyy-MM-dd hoặc để trống");
        }
        if ("ngay_gio".equals(k)) {
            return v.isEmpty() || (NGAY_GIO.matcher(v).matches() && ngayHopLe(v, "yyyy-MM-dd HH:mm:ss")) ? dung(v)
                    : sai("cần yyyy-MM-dd HH:mm:ss hoặc để trống");
        }
        if ("nhip".equals(k)) {
            return NHIP.matcher(v).matches() ? dung(v) : sai("cần dạng số_lần/giây, vd 5/10");
        }
        return sai("kiểu chưa hỗ trợ: " + k);
    }

    static KetQua kiemSo(final DinhNghiaKhoa d, final String v) {
        if (!SO.matcher(v).matches()) {
            return sai("phải là số nguyên");
        }
        final long n;
        try {
            n = Long.parseLong(v);
        } catch (final NumberFormatException e) {
            return sai("phải là số nguyên");
        }
        if (n < d.min()) {
            return sai("nhỏ nhất " + d.min());
        }
        if (n > d.max()) {
            return sai("lớn nhất " + d.max());
        }
        if (d.buoc() > 0 && n % d.buoc() != 0) {
            return sai("phải là bội số của " + d.buoc());
        }
        return dung(String.valueOf(n));
    }

    private static boolean ngayHopLe(final String v, final String mau) {
        final SimpleDateFormat f = new SimpleDateFormat(mau);
        f.setLenient(false);
        try {
            f.parse(v);
            return true;
        } catch (final ParseException e) {
            return false;
        }
    }

    private static String noi(final String[] a) {
        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < a.length; i++) {
            if (i > 0) {
                b.append(',');
            }
            b.append(a[i]);
        }
        return b.toString();
    }
}
