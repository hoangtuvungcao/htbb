package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * VONG QUAY GOP HU (thuong / VIP bang xu, luong) - lop THUAN (khong CSDL, khong mang), test/KiemVongQuay.java.
 * Ban noi vao game: {@link real.VongQuay}.
 *
 * Luat (chu game duyet 19/10/2026, "chung ta theo co che khong lam phat"):
 * <ul>
 *   <li>KHONG IN TIEN: hu = tong cuoc cua nguoi choi. Ket thuc luot: tong vao = tra nguoi thang + phi (phi la HO HUT),
 *       hoac tong vao = tong hoan (khi chi co 1 nguoi). Khong co hu moi, khong co thuong them.</li>
 *   <li>Dong ho bat dau tu luc co nguoi dat DAU TIEN (truoc day dung o 02:00 mai khi chua du 2 nguoi, cuoc nam
 *       treo vo thoi han). Het gio: tu 2 nguoi thi quay (xac suat = phan cuoc / hu); 1 nguoi thi HOAN DU, khong
 *       ai "thang 100%" cua chinh minh.</li>
 *   <li>Mot nguoi dat 1 lan / luot, trong [cuocToiThieu, cuocToiDa]; tran luot / ngay va tong cuoc / ngay; cap toi
 *       thieu; N giay cuoi khoa dat; toi da soNguoiToiDa nguoi.</li>
 *   <li>Chong clone (vong quay chuyen tien giua nguoi choi = duong chuyen gia tri): ngoai cong cap / tuoi tai khoan /
 *       gio choi cua ChongClone, MOI thiet bi va MOI IP chi toi da N nhan vat tham gia vong quay moi ngay
 *       ({@link SoNgay#kiemMay}).</li>
 * </ul>
 */
public final class VongQuayLogic {

    private VongQuayLogic() {
    }

    // ------------------------------------------------------------------ cau hinh

    /** Anh chup cau hinh mot loai vong quay (bat bien, da kep gia tri). */
    public static final class CauHinh {
        public final boolean bat;
        public final int capToiThieu;
        public final long cuocToiThieu;
        public final long cuocToiDa;
        public final long cuocNgayToiDa;
        public final int luotNgayToiDa;
        public final int soNguoiToiDa;
        public final int phiPhanTram;
        public final int giayMoiVong;
        public final int giayKhoa;

        public CauHinh(final boolean bat, final int capToiThieu, final long cuocToiThieu, final long cuocToiDa,
                final long cuocNgayToiDa, final int luotNgayToiDa, final int soNguoiToiDa, final int phiPhanTram,
                final int giayMoiVong, final int giayKhoa) {
            this.bat = bat;
            this.capToiThieu = Math.max(0, capToiThieu);
            this.cuocToiThieu = Math.max(1L, cuocToiThieu);
            this.cuocToiDa = Math.max(this.cuocToiThieu, cuocToiDa);
            this.cuocNgayToiDa = Math.max(this.cuocToiThieu, cuocNgayToiDa);
            this.luotNgayToiDa = Math.max(1, luotNgayToiDa);
            this.soNguoiToiDa = Math.max(2, soNguoiToiDa);
            // phi 5..50%: duoi 5% vong quay thanh duong chuyen tien re hon giao dich (thue cho 6% + 1%).
            this.phiPhanTram = Math.max(5, Math.min(50, phiPhanTram));
            this.giayMoiVong = Math.max(30, giayMoiVong);
            this.giayKhoa = Math.max(0, Math.min(this.giayMoiVong - 5, giayKhoa));
        }

        /** Tran hu cua mot luot. */
        public long tranHu() {
            return this.cuocToiDa * this.soNguoiToiDa;
        }
    }

    /** Mac dinh san xuat (khop tools/cauhinh/danh_muc.json, nhom vong_quay). 0 = thuong, 1 = VIP, 2 = luong. */
    public static CauHinh macDinh(final int loai) {
        if (loai == 1) {
            return new CauHinh(true, 50, 10000L, 50000L, 100000L, 10, 20, 10, 120, 10);
        }
        if (loai == 2) {
            return new CauHinh(true, 20, 10L, 100L, 300L, 10, 20, 15, 120, 10);
        }
        return new CauHinh(true, 20, 1000L, 10000L, 30000L, 10, 20, 10, 120, 10);
    }

    // ------------------------------------------------------------------ mot luot

    /** Mot nguoi da dat trong luot. */
    public static final class NguoiCuoc {
        /** khoa (ten viet thuong) */
        public final String khoa;
        public final String ten;
        public final int charId;
        public final long so;
        public final String may;
        public final String ip;

        public NguoiCuoc(final String ten, final int charId, final long so, final String may, final String ip) {
            this.ten = ten == null ? "" : ten;
            this.khoa = this.ten.toLowerCase();
            this.charId = charId;
            this.so = so;
            this.may = may == null ? "" : may;
            this.ip = ip == null ? "" : ip;
        }
    }

    /** Trang thai mot luot (KHONG an toan luong - nguoi goi khoa). */
    public static final class Vong {
        final LinkedHashMap<String, NguoiCuoc> ds = new LinkedHashMap<String, NguoiCuoc>();
        /** Luc nguoi dau tien dat (ms); 0 = luot chua bat dau. */
        long batDau = 0L;

        public int soNguoi() {
            return this.ds.size();
        }

        public long hu() {
            long t = 0L;
            for (final NguoiCuoc n : this.ds.values()) {
                t += n.so;
            }
            return t;
        }

        public NguoiCuoc cua(final String ten) {
            return ten == null ? null : this.ds.get(ten.toLowerCase());
        }

        public long batDau() {
            return this.batDau;
        }

        public List<NguoiCuoc> danhSach() {
            return new ArrayList<NguoiCuoc>(this.ds.values());
        }

        /** Them (khi nap lai tu CSDL luc khoi dong, hoac sau khi da kiem bang {@link #kiemCuoc}). */
        public void them(final NguoiCuoc n, final long bayGio) {
            if (this.ds.isEmpty()) {
                this.batDau = bayGio;
            }
            this.ds.put(n.khoa, n);
        }

        void xoa() {
            this.ds.clear();
            this.batDau = 0L;
        }
    }

    /** So giay con lai cua luot (luot chua bat dau = ca luot). */
    public static int conLaiGiay(final CauHinh c, final Vong v, final long bayGio) {
        if (v.batDau <= 0L) {
            return c.giayMoiVong;
        }
        final long con = c.giayMoiVong * 1000L - (bayGio - v.batDau);
        return con <= 0L ? 0 : (int) ((con + 999L) / 1000L);
    }

    public static boolean denGio(final CauHinh c, final Vong v, final long bayGio) {
        return v.batDau > 0L && bayGio - v.batDau >= c.giayMoiVong * 1000L;
    }

    public static boolean dangKhoa(final CauHinh c, final Vong v, final long bayGio) {
        return v.batDau > 0L && conLaiGiay(c, v, bayGio) <= c.giayKhoa;
    }

    // ------------------------------------------------------------------ kiem cuoc

    /** Thong tin nguoi muon dat (lay tu Char o lop noi). */
    public static final class NguoiDat {
        public final String ten;
        public final int cap;
        public final long soDu;
        public final int luotHomNay;
        public final long tongHomNay;

        public NguoiDat(final String ten, final int cap, final long soDu, final int luotHomNay, final long tongHomNay) {
            this.ten = ten == null ? "" : ten;
            this.cap = cap;
            this.soDu = soDu;
            this.luotHomNay = luotHomNay;
            this.tongHomNay = tongHomNay;
        }
    }

    /**
     * Ly do KHONG duoc dat {@code so}, null neu hop le. Khong kiem cong chong clone (lop noi kiem truoc).
     * @param donVi "xu" / "lượng" (cho loi nhan)
     */
    public static String kiemCuoc(final CauHinh c, final Vong v, final long bayGio, final NguoiDat p, final long so,
            final String donVi) {
        if (c == null || !c.bat) {
            return "Vòng quay đang đóng.";
        }
        if (p.cap < c.capToiThieu) {
            return "Cần đạt cấp " + c.capToiThieu + " mới tham gia vòng quay này.";
        }
        if (v.cua(p.ten) != null) {
            return "Bạn đã đặt " + so(v.cua(p.ten).so) + " " + donVi + " trong lượt này. Mỗi lượt chỉ đặt 1 lần.";
        }
        if (so <= 0L) {
            return "Số " + donVi + " không hợp lệ.";
        }
        if (dangKhoa(c, v, bayGio) || denGio(c, v, bayGio)) {
            return "Lượt này đã khoá đặt (" + c.giayKhoa + " giây cuối). Đợi lượt sau nhé.";
        }
        if (v.soNguoi() >= c.soNguoiToiDa) {
            return "Lượt này đã đủ " + c.soNguoiToiDa + " người. Đợi lượt sau nhé.";
        }
        if (so < c.cuocToiThieu) {
            return "Đặt tối thiểu " + so(c.cuocToiThieu) + " " + donVi + ".";
        }
        if (so > c.cuocToiDa) {
            return "Đặt tối đa " + so(c.cuocToiDa) + " " + donVi + " mỗi lượt.";
        }
        if (p.luotHomNay >= c.luotNgayToiDa) {
            return "Hôm nay bạn đã quay đủ " + c.luotNgayToiDa + " lượt. Hẹn ngày mai.";
        }
        if (p.tongHomNay + so > c.cuocNgayToiDa) {
            final long con = Math.max(0L, c.cuocNgayToiDa - p.tongHomNay);
            return "Mỗi ngày đặt tối đa " + so(c.cuocNgayToiDa) + " " + donVi + " (hôm nay còn " + so(con) + ").";
        }
        if (p.soDu < so) {
            return "Không đủ " + so(so) + " " + donVi + ".";
        }
        return null;
    }

    /** Ba muc dat goi y (toi thieu, giua, toi da), da bo trung. */
    public static long[] mucGoiY(final CauHinh c) {
        final long giua = lamTron((c.cuocToiThieu + c.cuocToiDa) / 2L);
        final List<Long> ds = new ArrayList<Long>();
        for (final long m : new long[]{c.cuocToiThieu, giua, c.cuocToiDa}) {
            if (m >= c.cuocToiThieu && m <= c.cuocToiDa && !ds.contains(Long.valueOf(m))) {
                ds.add(Long.valueOf(m));
            }
        }
        Collections.sort(ds);
        final long[] r = new long[ds.size()];
        for (int i = 0; i < r.length; i++) {
            r[i] = ds.get(i).longValue();
        }
        return r;
    }

    /** Lam tron xuong 2 chu so co nghia (25.500 -> 25.000; 5.500 -> 5.500; 55 -> 55). */
    static long lamTron(final long x) {
        long m = 1L;
        while (x / m >= 100L) {
            m *= 10L;
        }
        return Math.max(1L, (x / m) * m);
    }

    // ------------------------------------------------------------------ ket thuc luot

    /** Ket qua mot luot da xong. */
    public static final class KetQua {
        /** true = hoan tien (chua du 2 nguoi), false = co nguoi thang. */
        public final boolean hoan;
        public final long hu;
        public final long phi;
        public final NguoiCuoc nguoiThang;
        /** So tra cho nguoi thang (hu - phi); 0 khi hoan. */
        public final long tra;
        public final List<NguoiCuoc> nguoi;

        KetQua(final boolean hoan, final long hu, final long phi, final NguoiCuoc nguoiThang, final long tra,
                final List<NguoiCuoc> nguoi) {
            this.hoan = hoan;
            this.hu = hu;
            this.phi = phi;
            this.nguoiThang = nguoiThang;
            this.tra = tra;
            this.nguoi = nguoi;
        }

        /** Tong tra ra (thang + hoan). Bat bien: tongTra() + phi == hu. */
        public long tongTra() {
            if (!this.hoan) {
                return this.tra;
            }
            long t = 0L;
            for (final NguoiCuoc n : this.nguoi) {
                t += n.so;
            }
            return t;
        }
    }

    /** Phi hu (lam tron xuong - nghieng ve nguoi choi). */
    public static long phi(final CauHinh c, final long hu) {
        return hu <= 0L ? 0L : hu * c.phiPhanTram / 100L;
    }

    /**
     * Ket thuc luot neu da het gio; null neu chua. Xoa luot (luot moi bat dau o lan dat ke tiep).
     * Nguoi thang chon theo trong so = so cuoc (MOI nguoi tham gia, ke ca dang offline - lop noi tra sau khi
     * dang nhap) de clone dat roi thoat game khong "dua" chac hu cho nick chinh.
     */
    public static KetQua ketThucNeuDenGio(final CauHinh c, final Vong v, final long bayGio, final Random r) {
        if (!denGio(c, v, bayGio)) {
            return null;
        }
        return ketThuc(c, v, r);
    }

    /** Ket thuc luot ngay (bat ke gio): dung khi het gio va khi dong vong quay. */
    public static KetQua ketThuc(final CauHinh c, final Vong v, final Random r) {
        final List<NguoiCuoc> ds = v.danhSach();
        final long hu = v.hu();
        v.xoa();
        if (ds.size() < 2) {
            return new KetQua(true, hu, 0L, null, 0L, ds);
        }
        final long[] w = new long[ds.size()];
        for (int i = 0; i < w.length; i++) {
            w[i] = ds.get(i).so;
        }
        final NguoiCuoc thang = ds.get(chonTheoTrongSo(w, r));
        final long phi = phi(c, hu);
        return new KetQua(false, hu, phi, thang, hu - phi, ds);
    }

    /** Chi so ngau nhien theo trong so (trong so > 0). */
    public static int chonTheoTrongSo(final long[] w, final Random r) {
        long tong = 0L;
        for (final long x : w) {
            tong += Math.max(0L, x);
        }
        if (tong <= 0L) {
            return 0;
        }
        long k = (long) (r.nextDouble() * tong);
        if (k >= tong) {
            k = tong - 1L;
        }
        for (int i = 0; i < w.length; i++) {
            k -= Math.max(0L, w[i]);
            if (k < 0L) {
                return i;
            }
        }
        return w.length - 1;
    }

    /** Ti le thang (%) mot chu so thap phan, vd "33,3". */
    public static String tiLe(final long cuaMinh, final long hu) {
        if (cuaMinh <= 0L || hu <= 0L) {
            return "0";
        }
        final long phanNghin = (cuaMinh * 1000L + hu / 2L) / hu;
        return (phanNghin / 10L) + (phanNghin % 10L == 0L ? "" : "," + (phanNghin % 10L));
    }

    /** 1234567 -> "1.234.567". */
    public static String so(final long x) {
        final String s = Long.toString(Math.abs(x));
        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (i > 0 && (s.length() - i) % 3 == 0) {
                b.append('.');
            }
            b.append(s.charAt(i));
        }
        return (x < 0L ? "-" : "") + b;
    }

    /** "mm:ss". */
    public static String phutGiay(final int giay) {
        final int g = Math.max(0, giay);
        final int m = g / 60;
        final int s = g % 60;
        return (m < 10 ? "0" : "") + m + ":" + (s < 10 ? "0" : "") + s;
    }

    // ------------------------------------------------------------------ so ngay

    /**
     * So trong ngay (bo nho; lop noi nap lai tu tob_vong_quay_ngay luc khoi dong): luot + tong cuoc moi nhan vat
     * moi loai, va nhan vat theo thiet bi / IP (chung ca ba vong quay).
     */
    public static final class SoNgay {
        private String ngay = "";
        /** loai:ten -> {luot, tong} */
        private final Map<String, long[]> theoTen = new HashMap<String, long[]>();
        /** "m:"+may / "i:"+ip -> ten (viet thuong) */
        private final Map<String, Set<String>> theoMay = new HashMap<String, Set<String>>();

        private void doiNgay(final String n) {
            if (!n.equals(this.ngay)) {
                this.ngay = n;
                this.theoTen.clear();
                this.theoMay.clear();
            }
        }

        public synchronized int luot(final String n, final int loai, final String ten) {
            doiNgay(n);
            final long[] x = this.theoTen.get(loai + ":" + ten.toLowerCase());
            return x == null ? 0 : (int) x[0];
        }

        public synchronized long tong(final String n, final int loai, final String ten) {
            doiNgay(n);
            final long[] x = this.theoTen.get(loai + ":" + ten.toLowerCase());
            return x == null ? 0L : x[1];
        }

        /**
         * Loi nhan neu thiet bi / IP nay da co {@code toiDa} nhan vat KHAC tham gia vong quay hom nay, null neu duoc.
         * May / IP rong = khong biet (khong chan theo khoa do).
         */
        public synchronized String kiemMay(final String n, final String ten, final String may, final String ip,
                final int toiDa) {
            doiNgay(n);
            final String t = ten.toLowerCase();
            for (final String k : new String[]{may == null || may.isEmpty() ? null : "m:" + may,
                    ip == null || ip.isEmpty() ? null : "i:" + ip}) {
                if (k == null) {
                    continue;
                }
                final Set<String> s = this.theoMay.get(k);
                if (s != null && !s.contains(t) && s.size() >= Math.max(1, toiDa)) {
                    return "Hôm nay thiết bị / mạng của bạn đã có " + s.size()
                            + " Pokémon tham gia vòng quay (tối đa " + Math.max(1, toiDa) + " mỗi ngày). Thử lại vào ngày mai.";
                }
            }
            return null;
        }

        /** Ghi mot lan dat. */
        public synchronized void ghi(final String n, final int loai, final String ten, final String may, final String ip,
                final long so) {
            doiNgay(n);
            final String t = ten.toLowerCase();
            final String k = loai + ":" + t;
            long[] x = this.theoTen.get(k);
            if (x == null) {
                x = new long[2];
                this.theoTen.put(k, x);
            }
            x[0]++;
            x[1] += so;
            if (may != null && !may.isEmpty()) {
                ganMay("m:" + may, t);
            }
            if (ip != null && !ip.isEmpty()) {
                ganMay("i:" + ip, t);
            }
        }

        /** Hoan (luot chi 1 nguoi): tra lai luot va tong cuoc da tinh. Thiet bi van tinh (da tham gia). */
        public synchronized void hoan(final String n, final int loai, final String ten, final long so) {
            doiNgay(n);
            final long[] x = this.theoTen.get(loai + ":" + ten.toLowerCase());
            if (x != null) {
                x[0] = Math.max(0L, x[0] - 1L);
                x[1] = Math.max(0L, x[1] - so);
            }
        }

        /** Nap lai mot dong da luu (luc khoi dong). */
        public synchronized void nap(final String n, final int loai, final String ten, final String may, final String ip,
                final int luot, final long tong) {
            doiNgay(n);
            final String t = ten.toLowerCase();
            this.theoTen.put(loai + ":" + t, new long[]{luot, tong});
            if (may != null && !may.isEmpty()) {
                ganMay("m:" + may, t);
            }
            if (ip != null && !ip.isEmpty()) {
                ganMay("i:" + ip, t);
            }
        }

        private void ganMay(final String k, final String t) {
            Set<String> s = this.theoMay.get(k);
            if (s == null) {
                s = new HashSet<String>();
                this.theoMay.put(k, s);
            }
            s.add(t);
        }
    }
}
