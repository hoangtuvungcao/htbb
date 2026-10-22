package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.List;

/**
 * DAU TRUONG BAO BOI - MO PHONG TRAN 3v3 theo luot (he_thong_day_du.md 3.6, D12). May chu tinh het; client chi chon
 * hanh dong cho ben tan cong (hoac de may chon). Lop THUAN, <b>tat dinh</b>: cung hat ({@link #hat}) + cung chuoi hanh
 * dong cua ben tan cong = cung tran (test/KiemDauTruong).
 *
 * <ul>
 * <li><b>Chuan hoa cap 50:</b> chi so = cong thuc chinh ban (CaThePokemon.chiSo: nen + IV 0..31 + tinh cach +-10%, khong
 *     EV).</li>
 * <li><b>Sat thuong</b> (kieu chinh ban the he 5+): floor(floor(floor(2L/5+2) x P x A / D) / 50) + 2, roi lan luot: chi
 *     mang x1,5 (1/24, bo bac giam cua ben danh / bac tang cua ben do), ngau nhien 85..100 %, STAB x1,5, khac he CHINH
 *     XAC (x0 / x0,25 / x0,5 / x1 / x2 / x4 - che do rieng, khong lam diu nhu MMO), bong x0,5 voi don vat ly; toi thieu 1
 *     (tru x0).</li>
 * <li><b>Thu tu:</b> doi Pokemon truoc, roi Toc do (bac, te liet x0,5), bang nhau tung dong xu tu hat.</li>
 * <li><b>Trang thai don gian:</b> bong (1/16 HP / luot, vat ly x0,5), doc (1/8), te liet (25% dung im, toc do x0,5), ngu
 *     (1..3 luot), dong bang (20% tan moi luot, toi da 3), lu lan (2..5 luot, 33% tu danh minh 40 uy luc), choang (chi khi
 *     danh truoc), giam chi so (cham / giam_thu / so_hai / mu). Mien nhiem chinh ban (Lua - bong, Doc / Thep - doc, Dien - te,
 *     Bang - dong bang).</li>
 * <li><b>Ho tro:</b> khong che = gay trang thai cua chieu; buff = +1 Tan cong, +1 Tan cong DB; khien = hoi 50% HP toi da.
 *     Toi thuong: sau don, chi so tan cong vua dung -1 bac. Het PP ca 4 chieu: "Vùng vẫy" (giat lui 1/4 HP toi da).</li>
 * <li><b>AI</b> ({@link #ai}): chon chieu theo uoc sat thuong / ha guc / trang thai / hoi mau; ben phong thu doi Pokemon khi
 *     the tran xau ro (cach lan doi truoc &gt;= 2 luot); luon tra hanh dong hop le ({@link #hopLe}).</li>
 * <li>Het {@link #LUOT_TOI_DA} luot: ben con nhieu % HP hon thang; bang nhau / cung guc het: ben phong thu thang.</li>
 * </ul>
 */
public final class DauTruongTran {

    public static final int CAP = 50;
    public static final int SO_CON = 3;
    public static final int LUOT_TOI_DA = 50;
    public static final int CONG = 0, THU = 1;
    /** Chi mang: 1 / {@link #CHI_MANG_MOT_TREN}. */
    public static final int CHI_MANG_MOT_TREN = 24;

    /** Trang thai chinh (mot luc mot cai). */
    public static final int TT_KHONG = 0, TT_BONG = 1, TT_DOC = 2, TT_TE = 3, TT_NGU = 4, TT_DONG_BANG = 5;
    public static final String[] TEN_TT = {"", "Bỏng", "Trúng độc", "Tê liệt", "Ngủ", "Đóng băng"};
    /** Bac chi so: Tan cong, Phong thu, Tan cong DB, Phong thu DB, Toc do, Chinh xac. */
    public static final int B_TC = 0, B_PT = 1, B_TCDB = 2, B_PTDB = 3, B_TD = 4, B_CX = 5;
    static final String[] TEN_BAC = {"Tấn công", "Phòng thủ", "Tấn công đặc biệt", "Phòng thủ đặc biệt", "Tốc độ", "Chính xác"};

    // ================================================================== ngau nhien tat dinh

    /** SplitMix64: cung hat = cung day so tren moi JVM. */
    public static final class Rng {
        private long s;

        public Rng(final long hat) {
            this.s = hat;
        }

        public long tiep() {
            long z = (this.s += 0x9E3779B97F4A7C15L);
            z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
            z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
            return z ^ (z >>> 31);
        }

        /** 0..n-1. */
        public int so(final int n) {
            if (n <= 1) {
                return 0;
            }
            return (int) ((tiep() >>> 1) % n);
        }
    }

    // ================================================================== mot con

    public static final class Con {
        public final int dex, he1, he2, iv, tinhCach;
        public final boolean shiny;
        public final String ten;
        /** Chi so cap 50: HP, TC, PT, TCDB, PTDB, TD. */
        public final int[] chiSo;
        public final int hpMax;
        public int hp;
        public final DauTruongDuLieu.Chieu[] chieu;
        public final int[] pp = new int[4];
        /** Luot hoi chieu con lai moi o (chieu manh: {@link #hoiChieu}). */
        public final int[] hoi = new int[4];
        public int trangThai;
        int luotNgu, luotDong;
        int luLan;
        boolean choang;
        public final int[] bac = new int[6];
        /** id con trong Hop PC (0 = khong ro). */
        public int idHop;

        public Con(final int dex, final String ten, final int he1, final int he2, final boolean shiny, final int iv, final int tinhCach,
                final int[] nen, final DauTruongDuLieu.Chieu[] chieu) {
            this.dex = dex;
            this.ten = ten == null || ten.isEmpty() ? "#" + dex : ten;
            this.he1 = he1;
            this.he2 = he2 == he1 ? -1 : he2;
            this.shiny = shiny;
            this.iv = iv;
            this.tinhCach = tinhCach;
            this.chiSo = CaThePokemon.chiSo(nen, iv, tinhCach, CAP);
            this.hpMax = Math.max(1, this.chiSo[0]);
            this.hp = this.hpMax;
            this.chieu = new DauTruongDuLieu.Chieu[4];
            for (int i = 0; i < 4; i++) {
                this.chieu[i] = chieu != null && i < chieu.length && chieu[i] != null ? chieu[i] : DauTruongDuLieu.MAC_DINH;
                this.pp[i] = this.chieu[i].ppToiDa;
            }
        }

        public boolean song() {
            return this.hp > 0;
        }

        public boolean coHe(final int h) {
            return h >= 0 && (h == this.he1 || h == this.he2);
        }

        boolean hetPp() {
            for (int i = 0; i < 4; i++) {
                if (this.pp[i] > 0) {
                    return false;
                }
            }
            return true;
        }

        void raSan() {
            for (int i = 0; i < this.bac.length; i++) {
                this.bac[i] = 0;
            }
            this.luLan = 0;
            this.choang = false;
        }

        /** Phan nghin HP con lai. */
        public int phanNghinHp() {
            return (int) ((long) Math.max(0, this.hp) * 1000L / this.hpMax);
        }
    }

    /** Tao mot con cho tran tu du lieu loai (4 chieu cua loai, chi so cap 50). */
    public static Con taoCon(final DauTruongDuLieu d, final int dex, final boolean shiny, final int iv, final int tinhCach) {
        return new Con(dex, d.ten(dex), d.he1(dex), d.he2(dex), shiny, iv, tinhCach, d.nen(dex), d.bonChieu(dex));
    }

    // ================================================================== hanh dong

    public static final class HanhDong {
        public static final int CHIEU = 0, DOI = 1;
        public final int loai, chiSo;

        public HanhDong(final int loai, final int chiSo) {
            this.loai = loai;
            this.chiSo = chiSo;
        }

        public static HanhDong chieu(final int o) {
            return new HanhDong(CHIEU, o);
        }

        public static HanhDong doi(final int o) {
            return new HanhDong(DOI, o);
        }

        /** "c0".."c3" / "d0".."d2" (nhat ky, phat lai). */
        public String ma() {
            return (this.loai == DOI ? "d" : "c") + this.chiSo;
        }

        public static HanhDong tuMa(final String s) {
            if (s == null || s.length() < 2) {
                return null;
            }
            try {
                final int o = Integer.parseInt(s.substring(1));
                return s.charAt(0) == 'd' ? doi(o) : s.charAt(0) == 'c' ? chieu(o) : null;
            } catch (final NumberFormatException e) {
                return null;
            }
        }
    }

    // ================================================================== tran

    public final long hat;
    private final Rng rng;
    public final Con[][] doi = new Con[2][];
    public final int[] dangRa = new int[2];
    public int luot;
    /** -1 dang danh, CONG / THU = ben thang. */
    public int thang = -1;
    public final List<String> nhatKy = new ArrayList<String>();
    /** Chuoi ma hanh dong cua ben tan cong moi luot (phat lai). */
    public final List<String> hanhDongCong = new ArrayList<String>();
    private final int[] lanDoi = {-9, -9};
    /** Ben duoc AI cho doi Pokemon (phong thu: co; tan cong tu dong: khong - giu doi hinh nguoi choi xep). */
    public final boolean[] aiDoi = {false, true};
    private List<String> dongLuot = new ArrayList<String>();

    public DauTruongTran(final long hat, final Con[] cong, final Con[] thu) {
        if (cong == null || thu == null || cong.length == 0 || thu.length == 0) {
            throw new IllegalArgumentException("doi rong");
        }
        this.hat = hat;
        this.rng = new Rng(hat);
        this.doi[CONG] = cong;
        this.doi[THU] = thu;
        cong[0].raSan();
        thu[0].raSan();
    }

    public Con dangRa(final int ben) {
        return this.doi[ben][this.dangRa[ben]];
    }

    public boolean xong() {
        return this.thang >= 0;
    }

    public int conSong(final int ben) {
        int n = 0;
        for (final Con c : this.doi[ben]) {
            n += c.song() ? 1 : 0;
        }
        return n;
    }

    /** Tong phan nghin HP con lai cua mot ben (het luot). */
    public int tongPhanNghin(final int ben) {
        int t = 0;
        for (final Con c : this.doi[ben]) {
            t += c.phanNghinHp();
        }
        return t;
    }

    String goi(final int ben, final Con c) {
        return ben == THU ? c.ten + " của đối thủ" : c.ten;
    }

    void ghi(final String s) {
        ev(SK_CHU, -1, 0, null, 0, s);
    }

    // ================================================================== su kien (client dung de dien hoat canh)

    /** Kieu su kien: chi chu, ra san (so = o), dung chieu (so = o chieu), trung (so = sat thuong), truot, trang thai (so = ma
     * TT_* / 6 lu lan), bac chi so (so = +-n, co = chi so), hoi mau (so), mat mau ngoai don (so, co = nguon: 1 bong 2 doc
     * 3 phan luc 4 tu danh), nga guc, bi chan hanh dong (so = TT_* / 6 lu lan / 7 choang). */
    public static final int SK_CHU = 0, SK_RA_SAN = 1, SK_DUNG = 2, SK_TRUNG = 3, SK_TRUOT = 4, SK_TRANG_THAI = 5, SK_BAC = 6,
            SK_HOI = 7, SK_MAT_MAU = 8, SK_GUC = 9, SK_BI_CHAN = 10;
    /** Co cua SK_TRUNG: chi mang, sieu hieu qua, khong hieu qua lam, mien nhiem; SK_RA_SAN: thay con guc. */
    public static final int CO_CHI_MANG = 1, CO_SIEU = 2, CO_YEU = 4, CO_MIEN = 8, CO_THAY_GUC = 1;

    /** Mot su kien trong luot (server quyet dinh ket qua, client chi dien). */
    public static final class SuKien {
        public final String chu, hieuUng;
        public final int kieu, ben, so, he, loai, co;

        SuKien(final int kieu, final int ben, final int so, final int he, final int loai, final int co, final String hieuUng,
                final String chu) {
            this.kieu = kieu;
            this.ben = ben;
            this.so = so;
            this.he = he;
            this.loai = loai;
            this.co = co;
            this.hieuUng = hieuUng == null ? "" : hieuUng;
            this.chu = chu == null ? "" : chu;
        }
    }

    public final List<SuKien> suKien = new ArrayList<SuKien>();
    private List<SuKien> suKienLuot = new ArrayList<SuKien>();

    /** Su kien cua luot vua choi. */
    public List<SuKien> suKienLuot() {
        return this.suKienLuot;
    }

    void ev(final int kieu, final int ben, final int so, final DauTruongDuLieu.Chieu c, final int co, final String chu) {
        final SuKien e = new SuKien(kieu, ben, so, c == null ? -1 : c.he, c == null ? 0 : c.loai, co, c == null ? "" : c.hieuUng, chu);
        this.suKien.add(e);
        this.suKienLuot.add(e);
        this.dongLuot.add(e.chu);
        this.nhatKy.add(e.chu);
    }

    // ------------------------------------------------------------------ hop le

    /** Ly do o chieu KHONG dung duoc: 0 dung duoc, 1 het PP, 2 dang hoi chieu, 3 muc tieu da co trang thai / mien nhiem. */
    public static final int LD_OK = 0, LD_HET_PP = 1, LD_HOI = 2, LD_DA_CO_TT = 3;

    /** So luot hoi chieu sau khi dung (du lieu chieu: toi thuong 3, uy luc >= 100 / dac trung 2, con lai 0). */
    public static int hoiChieu(final DauTruongDuLieu.Chieu c) {
        if (c.vai == DauTruongDuLieu.V_TOI_THUONG) {
            return 3;
        }
        return c.uyLuc >= 100 || c.vai == DauTruongDuLieu.V_DAC_TRUNG ? 2 : 0;
    }

    public int lyDo(final int ben, final int o) {
        final Con a = dangRa(ben);
        if (o < 0 || o > 3 || a.pp[o] <= 0) {
            return LD_HET_PP;
        }
        if (a.hoi[o] > 0) {
            return LD_HOI;
        }
        final DauTruongDuLieu.Chieu c = a.chieu[o];
        if (c.vai == DauTruongDuLieu.V_KHONG_CHE) {
            final Con d = dangRa(1 - ben);
            final String m = c.trangThai.isEmpty() ? "cham" : c.trangThai;
            final int tt = maTrangThai(m);
            if (tt != TT_KHONG && (d.trangThai != TT_KHONG || mienNhiem(d, tt))) {
                return LD_DA_CO_TT;
            }
            if (("lu_lan".equals(m) || "me_hoac".equals(m)) && d.luLan > 0) {
                return LD_DA_CO_TT;
            }
        }
        return LD_OK;
    }

    /** Con dang ra co it nhat mot chieu dung duoc (khong thi "Vùng vẫy"). */
    public boolean coChieuDung(final int ben) {
        for (int o = 0; o < 4; o++) {
            if (lyDo(ben, o) == LD_OK) {
                return true;
            }
        }
        return false;
    }

    public boolean hopLe(final int ben, final HanhDong h) {
        if (h == null || xong()) {
            return false;
        }
        final Con a = dangRa(ben);
        if (h.loai == HanhDong.CHIEU) {
            return h.chiSo >= 0 && h.chiSo < 4 && (lyDo(ben, h.chiSo) == LD_OK || !coChieuDung(ben));
        }
        if (h.loai == HanhDong.DOI) {
            return h.chiSo >= 0 && h.chiSo < this.doi[ben].length && h.chiSo != this.dangRa[ben] && this.doi[ben][h.chiSo].song();
        }
        return false;
    }

    // ------------------------------------------------------------------ mot luot

    /** Choi mot luot: {@code hdCong} = hanh dong ben tan cong (null / khong hop le = may chon). Tra cac dong nhat ky luot nay. */
    public List<String> choi(final HanhDong hdCong) {
        this.dongLuot = new ArrayList<String>();
        this.suKienLuot = new ArrayList<SuKien>();
        if (xong()) {
            return this.dongLuot;
        }
        this.luot++;
        // AI ben tan cong LUON chay (ke ca khi nguoi choi da chon) de day ngau nhien giong nhau giua tran that va phat lai
        final HanhDong goiY = ai(CONG);
        final HanhDong a = hopLe(CONG, hdCong) ? hdCong : goiY;
        final HanhDong b = ai(THU);
        this.hanhDongCong.add(a.ma());
        final HanhDong[] hd = {a, b};
        // 1. doi Pokemon truoc (tan cong roi phong thu)
        for (int ben = 0; ben < 2; ben++) {
            if (hd[ben].loai == HanhDong.DOI) {
                doiCon(ben, hd[ben].chiSo, false);
            }
        }
        // 2. chieu theo toc do
        final int nhanh = benNhanh();
        final int[] thuTu = {nhanh, 1 - nhanh};
        boolean daDanhTruoc = false;
        for (int k = 0; k < 2; k++) {
            final int ben = thuTu[k];
            if (hd[ben].loai != HanhDong.CHIEU || xong()) {
                continue;
            }
            final Con c = dangRa(ben);
            if (!c.song()) {
                continue;
            }
            dungChieu(ben, hd[ben].chiSo, !daDanhTruoc);
            daDanhTruoc = true;
            kiemHet();
            if (xong()) {
                break;
            }
            if (!dangRa(1 - ben).song()) {
                break; // muc tieu guc: ben kia khong danh luot nay
            }
        }
        // 3. cuoi luot
        if (!xong()) {
            cuoiLuot();
        }
        for (int ben = 0; ben < 2; ben++) {
            dangRa(ben).choang = false;
            for (final Con c : this.doi[ben]) {
                for (int o = 0; o < 4; o++) {
                    c.hoi[o] = Math.max(0, c.hoi[o] - 1);
                }
            }
        }
        if (!xong() && this.luot >= LUOT_TOI_DA) {
            final int pa = tongPhanNghin(CONG), pb = tongPhanNghin(THU);
            this.thang = pa > pb ? CONG : THU;
            ghi("Hết " + LUOT_TOI_DA + " lượt: " + (this.thang == CONG ? "bạn còn nhiều máu hơn." : "đối thủ còn nhiều máu hơn."));
        }
        return this.dongLuot;
    }

    /** Ben tan cong dau hang (nut thoat / roi tran): thua ngay, ghi su kien. */
    public void dauHang() {
        if (xong()) {
            return;
        }
        this.suKienLuot = new ArrayList<SuKien>();
        this.dongLuot = new ArrayList<String>();
        this.thang = THU;
        ghi("Bạn đã đầu hàng - trận tính là thua.");
    }

    /** Doi luu de xem lai: "dex.shiny.iv.tinhCach" x 3, cach dau phay. */
    public static String maDoi(final Con[] doi) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < doi.length; i++) {
            sb.append(i > 0 ? "," : "").append(doi[i].dex).append('.').append(doi[i].shiny ? 1 : 0).append('.').append(doi[i].iv).append('.')
                    .append(doi[i].tinhCach);
        }
        return sb.toString();
    }

    public static Con[] giaiDoi(final DauTruongDuLieu d, final String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        final String[] p = s.split(",");
        final Con[] kq = new Con[p.length];
        try {
            for (int i = 0; i < p.length; i++) {
                final String[] q = p[i].split("\\.");
                kq[i] = taoCon(d, Integer.parseInt(q[0]), "1".equals(q[1]), Integer.parseInt(q[2]), Integer.parseInt(q[3]));
            }
        } catch (final RuntimeException e) {
            return null;
        }
        return kq.length == SO_CON ? kq : null;
    }

    /** Ca hai ben may chon den het tran (ben tan cong roi tran / tu dong). */
    public void chayHet() {
        int an = 0;
        while (!xong() && an++ < LUOT_TOI_DA + 5) {
            choi(null);
        }
    }

    public int tocDo(final int ben) {
        final Con c = dangRa(ben);
        long v = (long) c.chiSo[5] * heSoBac(c.bac[B_TD]) / 100L;
        if (c.trangThai == TT_TE) {
            v /= 2;
        }
        return (int) v;
    }

    int benNhanh() {
        final int a = tocDo(CONG), b = tocDo(THU);
        if (a != b) {
            return a > b ? CONG : THU;
        }
        return this.rng.so(2);
    }

    /** He so bac chi so (phan tram): +n = (2+n)/2, -n = 2/(2+n). */
    public static int heSoBac(final int b) {
        final int x = Math.max(-6, Math.min(6, b));
        return x >= 0 ? (2 + x) * 100 / 2 : 200 / (2 - x);
    }

    /** He so bac chinh xac (phan tram): +n = (3+n)/3, -n = 3/(3+n). */
    public static int heSoBacChinhXac(final int b) {
        final int x = Math.max(-6, Math.min(6, b));
        return x >= 0 ? (3 + x) * 100 / 3 : 300 / (3 - x);
    }

    void doiCon(final int ben, final int o, final boolean thayGuc) {
        final Con cu = dangRa(ben);
        this.dangRa[ben] = o;
        final Con moi = dangRa(ben);
        moi.raSan();
        this.lanDoi[ben] = this.luot;
        if (thayGuc) {
            ev(SK_RA_SAN, ben, o, null, CO_THAY_GUC, ben == THU ? "Đối thủ đưa " + moi.ten + " ra!" : "Tiến lên, " + moi.ten + "!");
        } else {
            ev(SK_RA_SAN, ben, o, null, 0, ben == THU ? "Đối thủ thu " + cu.ten + " về, đưa " + moi.ten + " ra!"
                    : cu.ten + ", quay về! Tiến lên, " + moi.ten + "!");
        }
    }

    void dungChieu(final int ben, final int o, final boolean danhTruoc) {
        final Con a = dangRa(ben);
        final Con d = dangRa(1 - ben);
        final String ta = goi(ben, a);
        // trang thai chan hanh dong
        if (a.trangThai == TT_NGU) {
            if (a.luotNgu <= 0) {
                a.trangThai = TT_KHONG;
                ghi(ta + " tỉnh dậy!");
            } else {
                a.luotNgu--;
                ev(SK_BI_CHAN, ben, TT_NGU, null, 0, ta + " đang ngủ say.");
                return;
            }
        }
        if (a.trangThai == TT_DONG_BANG) {
            a.luotDong++;
            if (this.rng.so(100) < 20 || a.luotDong >= 3) {
                a.trangThai = TT_KHONG;
                ghi(ta + " đã tan băng!");
            } else {
                ev(SK_BI_CHAN, ben, TT_DONG_BANG, null, 0, ta + " bị đóng băng, không cử động được!");
                return;
            }
        }
        if (a.choang) {
            ev(SK_BI_CHAN, ben, 7, null, 0, ta + " bị choáng, không ra đòn được!");
            return;
        }
        if (a.trangThai == TT_TE && this.rng.so(100) < 25) {
            ev(SK_BI_CHAN, ben, TT_TE, null, 0, ta + " bị tê liệt, không cử động được!");
            return;
        }
        if (a.luLan > 0) {
            a.luLan--;
            if (a.luLan == 0) {
                ghi(ta + " hết lú lẫn.");
            } else if (this.rng.so(100) < 33) {
                final int st = satThuong(CAP, 40, chiSoDung(a, B_TC, false), chiSoDung(a, B_PT, false), false, 100, false,
                        85 + this.rng.so(16), a.trangThai == TT_BONG);
                a.hp = Math.max(0, a.hp - st);
                ev(SK_MAT_MAU, ben, st, null, 4, ta + " lú lẫn, tự đánh mình (-" + st + " HP).");
                if (!a.song()) {
                    ev(SK_GUC, ben, 0, null, 0, ta + " ngã gục!");
                }
                return;
            }
        }
        DauTruongDuLieu.Chieu c;
        if (o < 0 || o > 3 || a.pp[o] <= 0 || a.hoi[o] > 0) {
            c = DauTruongDuLieu.VUNG_VAY;
            ev(SK_DUNG, ben, -1, c, 0, ta + " không còn chiêu dùng được, đành Vùng vẫy!");
        } else {
            c = a.chieu[o];
            a.pp[o] = Math.max(0, a.pp[o] - 1);
            a.hoi[o] = hoiChieu(c) > 0 ? hoiChieu(c) + 1 : 0; // +1: luot nay cung tru o cuoi luot
            ev(SK_DUNG, ben, o, c, 0, ta + " dùng " + c.ten + "!");
        }
        // chinh xac
        final int cx = c.chinhXac * heSoBacChinhXac(a.bac[B_CX]) / 100;
        if (c.vai != DauTruongDuLieu.V_BUFF && c.vai != DauTruongDuLieu.V_KHIEN && this.rng.so(100) >= cx) {
            ev(SK_TRUOT, 1 - ben, 0, c, 0, "Nhưng đòn đánh trượt!");
            return;
        }
        if (!c.danh()) {
            hoTro(ben, a, d, c);
            return;
        }
        final int heSo = HePokemon.heSoChinhXac(c.he, d.he1, d.he2);
        if (heSo == 0) {
            ev(SK_TRUNG, 1 - ben, 0, c, CO_MIEN, "Không có tác dụng với " + goi(1 - ben, d) + "...");
            return;
        }
        int tong = 0;
        int soDon = 0;
        boolean chiMangCo = false;
        for (int h = 0; h < c.soDon && d.song(); h++) {
            final boolean chiMang = this.rng.so(CHI_MANG_MOT_TREN) == 0;
            final boolean vatLy = c.loai == DauTruongDuLieu.LOAI_VAT_LY;
            final int iA = vatLy ? B_TC : B_TCDB, iD = vatLy ? B_PT : B_PTDB;
            final int A = chiSoDung(a, iA, chiMang && a.bac[iA] < 0);
            final int D = chiSoDung(d, iD, chiMang && d.bac[iD] > 0);
            final boolean stab = c != DauTruongDuLieu.VUNG_VAY && a.coHe(c.he);
            final int st = satThuong(CAP, c.uyLuc, A, D, stab, c == DauTruongDuLieu.VUNG_VAY ? 100 : heSo, chiMang,
                    85 + this.rng.so(16), vatLy && a.trangThai == TT_BONG);
            d.hp = Math.max(0, d.hp - st);
            tong += st;
            soDon++;
            chiMangCo |= chiMang;
        }
        final StringBuilder s = new StringBuilder();
        if (chiMangCo) {
            s.append("Chí mạng! ");
        }
        if (c != DauTruongDuLieu.VUNG_VAY) {
            if (heSo > 100) {
                s.append("Hiệu quả tuyệt vời! ");
            } else if (heSo < 100) {
                s.append("Không hiệu quả lắm... ");
            }
        }
        s.append(goi(1 - ben, d)).append(" mất ").append(tong).append(" HP");
        if (soDon > 1) {
            s.append(" (trúng ").append(soDon).append(" lần)");
        }
        s.append('.');
        ev(SK_TRUNG, 1 - ben, tong, c, (chiMangCo ? CO_CHI_MANG : 0) | (c != DauTruongDuLieu.VUNG_VAY && heSo > 100 ? CO_SIEU : 0)
                | (c != DauTruongDuLieu.VUNG_VAY && heSo < 100 ? CO_YEU : 0), s.toString());
        if (c == DauTruongDuLieu.VUNG_VAY) {
            final int lui = Math.max(1, a.hpMax / 4);
            a.hp = Math.max(0, a.hp - lui);
            ev(SK_MAT_MAU, ben, lui, null, 3, ta + " bị phản lực (-" + lui + " HP).");
        }
        if (c.vai == DauTruongDuLieu.V_TOI_THUONG && a.song()) {
            final int iA = c.loai == DauTruongDuLieu.LOAI_VAT_LY ? B_TC : B_TCDB;
            if (doiBac(a, iA, -1)) {
                ev(SK_BAC, ben, -1, null, iA, ta + " hao lực: " + TEN_BAC[iA] + " giảm.");
            }
        }
        if (!d.song()) {
            ev(SK_GUC, 1 - ben, 0, null, 0, goi(1 - ben, d) + " ngã gục!");
        } else if (c.tiLeTrangThai > 0 && this.rng.so(1000) < c.tiLeTrangThai) {
            gayTrangThai(1 - ben, d, c.trangThai, danhTruoc, false);
        }
        if (!a.song()) {
            ev(SK_GUC, ben, 0, null, 0, ta + " ngã gục!");
        }
    }

    void hoTro(final int ben, final Con a, final Con d, final DauTruongDuLieu.Chieu c) {
        final String ta = goi(ben, a);
        if (c.vai == DauTruongDuLieu.V_BUFF) {
            final boolean x = doiBac(a, B_TC, 1);
            final boolean y = doiBac(a, B_TCDB, 1);
            ev(SK_BAC, ben, x || y ? 1 : 0, c, B_TC, x || y ? ta + " tăng Tấn công và Tấn công đặc biệt!" : "Chỉ số của " + ta + " không tăng được nữa!");
            return;
        }
        if (c.vai == DauTruongDuLieu.V_KHIEN) {
            if (a.hp >= a.hpMax) {
                ghi(ta + " đã đầy máu, không có tác dụng.");
                return;
            }
            final int hoi = Math.min(a.hpMax - a.hp, Math.max(1, a.hpMax / 2));
            a.hp += hoi;
            ev(SK_HOI, ben, hoi, c, 0, ta + " hồi " + hoi + " HP.");
            return;
        }
        // khong che
        if (!gayTrangThai(1 - ben, d, c.trangThai.isEmpty() ? "cham" : c.trangThai, false, true)) {
            ghi("Không có tác dụng với " + goi(1 - ben, d) + ".");
        }
    }

    /** Ma trang thai data_chieu -&gt; TT_* chinh (0 = khong phai trang thai chinh). */
    public static int maTrangThai(final String s) {
        if ("bong".equals(s)) {
            return TT_BONG;
        }
        if ("doc".equals(s)) {
            return TT_DOC;
        }
        if ("te".equals(s)) {
            return TT_TE;
        }
        if ("ngu".equals(s)) {
            return TT_NGU;
        }
        if ("dong_bang".equals(s)) {
            return TT_DONG_BANG;
        }
        return TT_KHONG;
    }

    /** Mien nhiem trang thai chinh theo he (chinh ban). */
    public static boolean mienNhiem(final Con d, final int tt) {
        switch (tt) {
            case TT_BONG:
                return d.coHe(HePokemon.LUA);
            case TT_DOC:
                return d.coHe(HePokemon.DOC) || d.coHe(HePokemon.THEP);
            case TT_TE:
                return d.coHe(HePokemon.DIEN);
            case TT_DONG_BANG:
                return d.coHe(HePokemon.BANG);
            default:
                return false;
        }
    }

    /** Gay trang thai {@code ma} cho ben {@code ben}. Tra ve co tac dung. */
    boolean gayTrangThai(final int ben, final Con d, final String ma, final boolean danhTruoc, final boolean hoTro) {
        final String td = goi(ben, d);
        final int tt = maTrangThai(ma);
        if (tt != TT_KHONG) {
            if (d.trangThai != TT_KHONG || mienNhiem(d, tt)) {
                return false;
            }
            d.trangThai = tt;
            if (tt == TT_NGU) {
                d.luotNgu = 1 + this.rng.so(3);
            }
            d.luotDong = 0;
            final String[] chu = {"", " bị bỏng!", " bị trúng độc!", " bị tê liệt!", " ngủ thiếp đi!", " bị đóng băng!"};
            ev(SK_TRANG_THAI, ben, tt, null, 0, td + chu[tt]);
            return true;
        }
        if ("lu_lan".equals(ma) || "me_hoac".equals(ma)) {
            if (d.luLan > 0) {
                return false;
            }
            d.luLan = 2 + this.rng.so(4);
            ev(SK_TRANG_THAI, ben, 6, null, 0, td + ("me_hoac".equals(ma) ? " bị mê hoặc, lú lẫn!" : " bị lú lẫn!"));
            return true;
        }
        if ("choang".equals(ma) || "day_lui".equals(ma)) {
            if (!danhTruoc || hoTro) {
                return hoTro && doiBacBao(ben, d, B_TD, -1);
            }
            d.choang = true;
            return true;
        }
        if ("cham".equals(ma)) {
            return doiBacBao(ben, d, B_TD, -1);
        }
        if ("giam_thu".equals(ma)) {
            return doiBacBao(ben, d, B_PT, -1);
        }
        if ("so_hai".equals(ma)) {
            return doiBacBao(ben, d, B_TC, -1);
        }
        if ("mu".equals(ma)) {
            return doiBacBao(ben, d, B_CX, -1);
        }
        return false;
    }

    boolean doiBacBao(final int ben, final Con d, final int i, final int n) {
        if (!doiBac(d, i, n)) {
            return false;
        }
        ev(SK_BAC, ben, n, null, i, TEN_BAC[i] + " của " + goi(ben, d) + (n > 0 ? " tăng!" : " giảm!"));
        return true;
    }

    static boolean doiBac(final Con c, final int i, final int n) {
        final int moi = Math.max(-6, Math.min(6, c.bac[i] + n));
        if (moi == c.bac[i]) {
            return false;
        }
        c.bac[i] = moi;
        return true;
    }

    /** Chi so dung khi danh: chi so cap 50 x bac ({@code boBac} = chi mang bo bac). */
    static int chiSoDung(final Con c, final int b, final boolean boBac) {
        final int goc = c.chiSo[1 + b];
        return Math.max(1, (int) ((long) goc * (boBac ? 100 : heSoBac(c.bac[b])) / 100L));
    }

    /**
     * Sat thuong mot don (chinh ban the he 5+, so nguyen, lam tron xuong tung buoc).
     *
     * @param heSo        khac he phan tram (0, 25, 50, 100, 200, 400)
     * @param ngauNhien   85..100
     */
    public static int satThuong(final int cap, final int uyLuc, final int A, final int D, final boolean stab, final int heSo,
            final boolean chiMang, final int ngauNhien, final boolean bongVatLy) {
        if (heSo <= 0 || uyLuc <= 0) {
            return 0;
        }
        long b = (long) (2 * cap / 5 + 2) * uyLuc * Math.max(1, A) / Math.max(1, D);
        b = b / 50 + 2;
        if (chiMang) {
            b = b * 3 / 2;
        }
        b = b * Math.max(85, Math.min(100, ngauNhien)) / 100;
        if (stab) {
            b = b * 3 / 2;
        }
        b = b * heSo / 100;
        if (bongVatLy) {
            b = b / 2;
        }
        return (int) Math.max(1L, Math.min(Integer.MAX_VALUE, b));
    }

    void cuoiLuot() {
        for (int ben = 0; ben < 2; ben++) {
            final Con c = dangRa(ben);
            if (!c.song()) {
                continue;
            }
            if (c.trangThai == TT_BONG || c.trangThai == TT_DOC) {
                final int st = Math.max(1, c.hpMax / (c.trangThai == TT_BONG ? 16 : 8));
                c.hp = Math.max(0, c.hp - st);
                ev(SK_MAT_MAU, ben, st, null, c.trangThai == TT_BONG ? 1 : 2, goi(ben, c) + (c.trangThai == TT_BONG ? " bị bỏng rát" : " bị độc ngấm")
                        + " (-" + st + " HP).");
                if (!c.song()) {
                    ev(SK_GUC, ben, 0, null, 0, goi(ben, c) + " ngã gục!");
                }
            }
        }
        kiemHet();
        if (xong()) {
            return;
        }
        for (int ben = 0; ben < 2; ben++) {
            if (!dangRa(ben).song()) {
                final int o = chonThayThe(ben);
                if (o >= 0) {
                    doiCon(ben, o, true);
                }
            }
        }
    }

    void kiemHet() {
        if (xong()) {
            return;
        }
        final boolean hetA = conSong(CONG) == 0, hetB = conSong(THU) == 0;
        if (hetA || hetB) {
            this.thang = hetB && !hetA ? CONG : THU;
            ghi(this.thang == CONG ? "Bạn thắng! Đội phòng thủ đã gục hết." : "Bạn thua! Cả đội của bạn đã gục.");
        } else {
            // con dang ra guc giua luot: thay ngay neu ben kia con danh (giu thu tu "ngã gục" -> "đưa ra" o cuoi luot)
        }
    }

    // ================================================================== AI

    /** Uoc sat thuong (ngau nhien 92, khong chi mang) cua chieu c tu a vao d. */
    static int uoc(final Con a, final Con d, final DauTruongDuLieu.Chieu c) {
        if (!c.danh()) {
            return 0;
        }
        final int heSo = HePokemon.heSoChinhXac(c.he, d.he1, d.he2);
        final boolean vatLy = c.loai == DauTruongDuLieu.LOAI_VAT_LY;
        final int st = satThuong(CAP, c.uyLuc, chiSoDung(a, vatLy ? B_TC : B_TCDB, false), chiSoDung(d, vatLy ? B_PT : B_PTDB, false),
                a.coHe(c.he), heSo, false, 92, vatLy && a.trangThai == TT_BONG);
        return heSo == 0 ? 0 : st * c.soDon;
    }

    /** He so khac he tot nhat (phan tram) cua cac chieu danh cua a vao d. */
    static int heSoTotNhat(final Con a, final Con d) {
        int t = 0;
        for (final DauTruongDuLieu.Chieu c : a.chieu) {
            if (c.danh()) {
                t = Math.max(t, HePokemon.heSoChinhXac(c.he, d.he1, d.he2) * (a.coHe(c.he) ? 3 : 2) / 2);
            }
        }
        return t;
    }

    int diemChieu(final Con a, final Con d, final int o) {
        if (a.pp[o] <= 0 || a.hoi[o] > 0 || a == dangRa(CONG) && lyDo(CONG, o) != LD_OK || a == dangRa(THU) && lyDo(THU, o) != LD_OK) {
            return Integer.MIN_VALUE;
        }
        final DauTruongDuLieu.Chieu c = a.chieu[o];
        final int hpPt = a.hp * 100 / a.hpMax;
        if (c.danh()) {
            final int u = uoc(a, d, c) * c.chinhXac / 100;
            if (u <= 0) {
                return 1;
            }
            int diem = Math.min(100, u * 100 / Math.max(1, d.hp)) * 10;
            if (u >= d.hp) {
                diem += 500;
            } else if (c.vai == DauTruongDuLieu.V_TOI_THUONG) {
                diem -= 60;
            }
            if (c.tiLeTrangThai > 0 && d.trangThai == TT_KHONG) {
                diem += c.tiLeTrangThai / 20;
            }
            return diem;
        }
        if (c.vai == DauTruongDuLieu.V_KHIEN) {
            return a.hp >= a.hpMax ? 0 : hpPt <= 40 ? 450 : hpPt <= 60 ? 150 : 20;
        }
        if (c.vai == DauTruongDuLieu.V_BUFF) {
            return hpPt >= 70 && a.bac[B_TC] < 2 && a.bac[B_TCDB] < 2 ? 250 : 5;
        }
        final String m = c.trangThai.isEmpty() ? "cham" : c.trangThai;
        final int tt = maTrangThai(m);
        if (tt != TT_KHONG) {
            return d.trangThai == TT_KHONG && !mienNhiem(d, tt) ? 300 : 2;
        }
        if ("lu_lan".equals(m) || "me_hoac".equals(m)) {
            return d.luLan == 0 ? 200 : 2;
        }
        final int b = "giam_thu".equals(m) ? B_PT : "so_hai".equals(m) ? B_TC : "mu".equals(m) ? B_CX : B_TD;
        return d.bac[b] > -2 ? 120 : 2;
    }

    /** Diem the tran cua con x truoc d (cao = tot). */
    static int theTran(final Con x, final Con d) {
        return heSoTotNhat(x, d) - heSoTotNhat(d, x);
    }

    /** Hanh dong may chon cho ben {@code ben} - LUON hop le. */
    public HanhDong ai(final int ben) {
        final Con a = dangRa(ben);
        final Con d = dangRa(1 - ben);
        // doi Pokemon khi the tran xau ro
        if (this.aiDoi[ben] && this.luot - this.lanDoi[ben] >= 2) {
            boolean coHaGuc = false;
            for (int o = 0; o < 4; o++) {
                if (a.pp[o] > 0 && a.chieu[o].danh() && uoc(a, d, a.chieu[o]) >= d.hp) {
                    coHaGuc = true;
                }
            }
            if (!coHaGuc) {
                final int hien = theTran(a, d);
                int tot = -1;
                int diemTot = hien + 150;
                for (int j = 0; j < this.doi[ben].length; j++) {
                    final Con x = this.doi[ben][j];
                    if (j != this.dangRa[ben] && x.song() && x.hp * 2 > x.hpMax) {
                        final int t = theTran(x, d);
                        if (t >= diemTot) {
                            diemTot = t;
                            tot = j;
                        }
                    }
                }
                if (tot >= 0) {
                    return HanhDong.doi(tot);
                }
            }
        }
        if (!coChieuDung(ben)) {
            return HanhDong.chieu(0);
        }
        int max = Integer.MIN_VALUE;
        final int[] bang = new int[4];
        int n = 0;
        for (int o = 0; o < 4; o++) {
            final int s = diemChieu(a, d, o);
            if (s > max) {
                max = s;
                n = 0;
            }
            if (s == max && s != Integer.MIN_VALUE) {
                bang[n++] = o;
            }
        }
        if (n == 0) {
            for (int o = 0; o < 4; o++) {
                if (a.pp[o] > 0) {
                    return HanhDong.chieu(o);
                }
            }
            return HanhDong.chieu(0);
        }
        return HanhDong.chieu(bang[n == 1 ? 0 : this.rng.so(n)]);
    }

    /** Con thay khi con dang ra guc: phong thu = the tran tot nhat, tan cong = con song ke tiep theo thu tu. -1 = het. */
    int chonThayThe(final int ben) {
        final Con d = dangRa(1 - ben);
        int tot = -1;
        int diem = Integer.MIN_VALUE;
        for (int j = 0; j < this.doi[ben].length; j++) {
            final Con x = this.doi[ben][j];
            if (!x.song()) {
                continue;
            }
            if (ben == CONG) {
                return j;
            }
            final int t = theTran(x, d) * 1000 + x.phanNghinHp();
            if (t > diem) {
                diem = t;
                tot = j;
            }
        }
        return tot;
    }
}
