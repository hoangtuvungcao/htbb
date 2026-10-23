package com.htbb.pokemon;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Bo loc tu cam dung chung cho TEN (nhan vat, doi ten, ten doi) va CHE chat (0.9.14, sua bao cao QA #7).
 *
 * Truoc day Database.haveBadWord / Map.checkBadWord so khop CHUOI CON voi moi dong bad_word, nen tu ngan
 * ("ngu", "phong", "tay", "ad", "dm"...) chan ca ten that: Nguyenvu, Phongvu, Shadow, Gyarados, Kadabra, Tayson,
 * va che chu giua tu khi chat ("Nguyen" -&gt; "*yen"). Bay gio moi tu cam co mot KIEU:
 * <ul>
 *   <li>{@link #CA_TU} - chi khop CA TU: mot tu (hoac vai tu lien nhau ghep lai, vd "d m" / "Du Ma") bang dung tu cam.
 *       Tach tu o dau cach / dau cau / so / chu hoa giua tu ("ThangNgu" = "thang" + "ngu"). Mac dinh cho tu ngan
 *       (&lt;= 2 chu, hoac 3 chu co nguyen am) - de nam trong mot tu binh thuong.</li>
 *   <li>{@link #CHUOI} - chuoi con cua ban "khung" (chu thuong, bo dau, so giong chu 0-o 1-i 3-e 4-a 5-s 7-t @-a $-s,
 *       bo moi ky tu khong phai chu / so: "d.i.t.m.e" -&gt; "ditme"). Mac dinh cho tu dai va cum phu am ngan
 *       khong nam trong tu that ("dkm", "vcl").</li>
 *   <li>{@link #NGUYEN_VAN} - tu co ky tu dac biet (".com", "lon`", "*sh"): chuoi con nguyen van, khong phan biet
 *       hoa thuong (nhu cu - chan quang cao web).</li>
 * </ul>
 * Tu co dau tieng Viet ("cặc", "đại lý") so tren ban GIU DAU (khong thi "cặc" chan ca "các", "lồn" chan "lớn").
 *
 * Lop THUAN (khong CSDL): test/KiemLocTuCam.java. Noi CSDL: data.Database.loadBadWords (bang bad_word, cot kieu).
 */
public final class LocTuCam {

    public static final int TU_DONG = 0;
    public static final int CA_TU = 1;
    public static final int CHUOI = 2;
    public static final int NGUYEN_VAN = 3;

    /** Mot tu cam da bien dich. */
    static final class Muc {
        final String goc;
        final int kieu;
        final boolean coDau;
        /** Mau da chuan hoa (khung, hoac chu thuong nguyen van). */
        final String mau;

        Muc(final String goc, final int kieu, final boolean coDau, final String mau) {
            this.goc = goc;
            this.kieu = kieu;
            this.coDau = coDau;
            this.mau = mau;
        }
    }

    /** Mot vi tri khop trong chuoi GOC: [dau, cuoi). */
    public static final class Khop {
        public final int dau;
        public final int cuoi;
        public final String tuCam;

        Khop(final int dau, final int cuoi, final String tuCam) {
            this.dau = dau;
            this.cuoi = cuoi;
            this.tuCam = tuCam;
        }
    }

    private final List<Muc> ds = new ArrayList<Muc>();

    public LocTuCam() {
    }

    /** Them mot tu cam. kieu: "tu" / "chuoi" / "" (tu dong theo {@link #kieuTuDong}). Bo qua tu rong. */
    public LocTuCam them(final String tu, final String kieu) {
        if (tu == null) {
            return this;
        }
        final String t = Normalizer.normalize(tu, Normalizer.Form.NFC).trim().toLowerCase(Locale.ROOT);
        if (t.isEmpty()) {
            return this;
        }
        int k = docKieu(kieu);
        if (laNguyenVan(t)) {
            k = NGUYEN_VAN;
        } else if (k == TU_DONG) {
            k = kieuTuDong(t);
        }
        final boolean dau = coDau(t);
        final String mau = k == NGUYEN_VAN ? t : (dau ? khungDau(t) : khung(t));
        if (mau.isEmpty()) {
            return this;
        }
        ds.add(new Muc(tu.trim(), k, dau, mau));
        return this;
    }

    public LocTuCam them(final String[] tu) {
        if (tu != null) {
            for (final String t : tu) {
                them(t, "");
            }
        }
        return this;
    }

    public int soTu() {
        return ds.size();
    }

    static int docKieu(final String kieu) {
        if (kieu == null) {
            return TU_DONG;
        }
        final String k = kieu.trim().toLowerCase(Locale.ROOT);
        if (k.equals("tu") || k.equals("ca_tu") || k.equals("catu")) {
            return CA_TU;
        }
        if (k.equals("chuoi")) {
            return CHUOI;
        }
        return TU_DONG;
    }

    /** Tu co ky tu khac chu / so / dau cach (".com", "a.d", "lon`"). */
    static boolean laNguyenVan(final String t) {
        for (int i = 0; i < t.length(); i++) {
            final char c = t.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != ' ') {
                return true;
            }
        }
        return false;
    }

    static boolean coDau(final String t) {
        for (int i = 0; i < t.length(); i++) {
            final char c = t.charAt(i);
            if (Character.isLetter(c) && c > 127) {
                return true;
            }
        }
        return false;
    }

    /**
     * Kieu mac dinh: tu (sau khi bo dau cach) &lt;= 2 chu, hoac 3 chu co nguyen am -&gt; CA_TU (de nam trong ten that:
     * dm, ngu, dit, mod, sex...); con lai (tu dai, cum ba phu am "dkm", "vcl", "www") -&gt; CHUOI.
     */
    public static int kieuTuDong(final String tu) {
        final String k = coDau(tu) ? khungDau(tu) : khung(tu);
        if (k.length() > 3) {
            return CHUOI;
        }
        if (k.length() <= 2) {
            return CA_TU; // "dm", "lt": hai chu nam trong qua nhieu tu that (Admiral, Saltwater)
        }
        final String bo = boDau(k);
        for (int i = 0; i < bo.length(); i++) {
            if ("aeiouy".indexOf(bo.charAt(i)) >= 0) {
                return CA_TU;
            }
        }
        return CHUOI;
    }

    // ================================================================== chuan hoa

    static char leet(final char c) {
        switch (c) {
            case '0':
                return 'o';
            case '1':
            case '!':
                return 'i';
            case '3':
                return 'e';
            case '4':
            case '@':
                return 'a';
            case '5':
            case '$':
                return 's';
            case '7':
                return 't';
            default:
                return 0;
        }
    }

    /** Bo dau mot ky tu (da chu thuong): "ặ" -&gt; 'a', 'đ' -&gt; 'd'. */
    static char boDauKyTu(final char c) {
        if (c < 128) {
            return c;
        }
        if (c == 'đ' || c == 'Đ') {
            return 'd';
        }
        final String n = Normalizer.normalize(String.valueOf(c), Normalizer.Form.NFD);
        return n.isEmpty() ? c : Character.toLowerCase(n.charAt(0));
    }

    static String boDau(final String s) {
        final StringBuilder b = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            b.append(boDauKyTu(s.charAt(i)));
        }
        return b.toString();
    }

    /** Ban khung khong dau: chu thuong, bo dau, leet, chi giu a-z 0-9. */
    public static String khung(final String s) {
        return xem(s == null ? "" : s, true).khung.toString();
    }

    /** Ban khung GIU dau: chu thuong NFC, leet, chi giu chu / so. */
    static String khungDau(final String s) {
        return xem(s == null ? "" : s, false).khung.toString();
    }

    /** Cac ban chuan hoa cua mot chuoi kem vi tri trong chuoi goc. */
    static final class Xem {
        final StringBuilder khung = new StringBuilder();
        final List<Integer> viTriKhung = new ArrayList<Integer>();
        /** Tu: chu (leet giua hai chu). */
        final List<String> tu = new ArrayList<String>();
        final List<int[]> viTriTu = new ArrayList<int[]>();
    }

    static Xem xem(final String goc, final boolean boDau) {
        final Xem x = new Xem();
        final int n = goc.length();
        final StringBuilder tu = new StringBuilder();
        int dauTu = -1;
        int cuoiTu = -1;
        for (int i = 0; i < n; i++) {
            final char g = goc.charAt(i);
            final char thuong = Character.toLowerCase(g);
            final char c = boDau ? boDauKyTu(thuong) : thuong;
            final boolean chu = Character.isLetter(c);
            final char l = chu ? 0 : leet(c);
            // khung
            if (chu) {
                x.khung.append(c);
                x.viTriKhung.add(Integer.valueOf(i));
            } else if (l != 0) {
                x.khung.append(l);
                x.viTriKhung.add(Integer.valueOf(i));
            } else if (Character.isDigit(c)) {
                x.khung.append(c);
                x.viTriKhung.add(Integer.valueOf(i));
            }
            // tu
            boolean laChuTu = chu;
            char ghi = c;
            if (!chu && l != 0 && tu.length() > 0 && i + 1 < n && Character.isLetter(goc.charAt(i + 1))) {
                laChuTu = true; // "d1tme": so giua hai chu la chu
                ghi = l;
            }
            final boolean hoaGiuaTu = chu && Character.isUpperCase(g) && i > 0 && Character.isLowerCase(goc.charAt(i - 1));
            if (!laChuTu || hoaGiuaTu) {
                if (tu.length() > 0) {
                    x.tu.add(tu.toString());
                    x.viTriTu.add(new int[]{dauTu, cuoiTu});
                    tu.setLength(0);
                }
            }
            if (laChuTu) {
                if (tu.length() == 0) {
                    dauTu = i;
                }
                tu.append(ghi);
                cuoiTu = i + 1;
            }
        }
        if (tu.length() > 0) {
            x.tu.add(tu.toString());
            x.viTriTu.add(new int[]{dauTu, cuoiTu});
        }
        return x;
    }

    // ================================================================== tim

    /** Tat ca vi tri khop (co the chong nhau), theo thu tu tu cam. */
    public List<Khop> timHet(final String chuoi) {
        final List<Khop> kq = new ArrayList<Khop>();
        if (chuoi == null || chuoi.isEmpty() || ds.isEmpty()) {
            return kq;
        }
        final String goc = Normalizer.normalize(chuoi, Normalizer.Form.NFC);
        final String thuong = goc.toLowerCase(Locale.ROOT);
        Xem khongDau = null;
        Xem coDau = null;
        for (final Muc m : ds) {
            if (m.kieu == NGUYEN_VAN) {
                int i = thuong.indexOf(m.mau);
                while (i >= 0) {
                    kq.add(new Khop(i, i + m.mau.length(), m.goc));
                    i = thuong.indexOf(m.mau, i + 1);
                }
                continue;
            }
            final Xem x;
            if (m.coDau) {
                if (coDau == null) {
                    coDau = xem(goc, false);
                }
                x = coDau;
            } else {
                if (khongDau == null) {
                    khongDau = xem(goc, true);
                }
                x = khongDau;
            }
            if (m.kieu == CHUOI && m.mau.length() <= 3) {
                // cum ngan ("dm", "vcl"): chuoi con TRONG mot tu, hoac qua mot day tu mot chu ("d m", "v.c.l") - khong
                // vat qua ranh gioi hai tu that ("and more" khong thanh "dm")
                for (int a = 0; a < x.tu.size(); a++) {
                    final String w = x.tu.get(a);
                    int i = w.indexOf(m.mau);
                    while (i >= 0) {
                        // vi tri goc: tu khong co ky tu bi bo giua chung (chu / leet lien nhau) nen lech = i
                        kq.add(new Khop(x.viTriTu.get(a)[0] + i, x.viTriTu.get(a)[0] + i + m.mau.length(), m.goc));
                        i = w.indexOf(m.mau, i + 1);
                    }
                    if (w.length() == 1) {
                        final StringBuilder ghep = new StringBuilder();
                        for (int b = a; b < x.tu.size() && x.tu.get(b).length() == 1 && ghep.length() < m.mau.length(); b++) {
                            ghep.append(x.tu.get(b));
                            if (b > a && ghep.length() == m.mau.length() && ghep.toString().equals(m.mau)) {
                                kq.add(new Khop(x.viTriTu.get(a)[0], x.viTriTu.get(b)[1], m.goc));
                            }
                        }
                    }
                }
            } else if (m.kieu == CHUOI) {
                final String k = x.khung.toString();
                int i = k.indexOf(m.mau);
                while (i >= 0) {
                    kq.add(new Khop(x.viTriKhung.get(i).intValue(), x.viTriKhung.get(i + m.mau.length() - 1).intValue() + 1, m.goc));
                    i = k.indexOf(m.mau, i + 1);
                }
            } else {
                // CA_TU: mot tu, hoac vai tu lien nhau ghep lai, bang dung tu cam
                for (int a = 0; a < x.tu.size(); a++) {
                    final StringBuilder ghep = new StringBuilder();
                    for (int b = a; b < x.tu.size() && ghep.length() < m.mau.length(); b++) {
                        ghep.append(x.tu.get(b));
                        if (ghep.length() == m.mau.length() && ghep.toString().equals(m.mau)) {
                            kq.add(new Khop(x.viTriTu.get(a)[0], x.viTriTu.get(b)[1], m.goc));
                        }
                    }
                }
            }
        }
        return kq;
    }

    /** Phan cua chuoi goc chua tu cam dau tien (de bao cho nguoi choi), null = sach. */
    public String tim(final String chuoi) {
        final List<Khop> k = timHet(chuoi);
        if (k.isEmpty()) {
            return null;
        }
        final String goc = Normalizer.normalize(chuoi, Normalizer.Form.NFC);
        final Khop d = k.get(0);
        return goc.substring(d.dau, d.cuoi);
    }

    public boolean co(final String chuoi) {
        return !timHet(chuoi).isEmpty();
    }

    /** Thay moi doan khop bang {@code thay} (gop doan chong nhau). */
    public String che(final String chuoi, final String thay) {
        if (chuoi == null) {
            return null;
        }
        final List<Khop> k = timHet(chuoi);
        if (k.isEmpty()) {
            return chuoi;
        }
        final String goc = Normalizer.normalize(chuoi, Normalizer.Form.NFC);
        final boolean[] bi = new boolean[goc.length()];
        for (final Khop d : k) {
            for (int i = d.dau; i < d.cuoi && i < bi.length; i++) {
                bi[i] = true;
            }
        }
        final StringBuilder b = new StringBuilder(goc.length());
        for (int i = 0; i < goc.length(); i++) {
            if (bi[i]) {
                if (i == 0 || !bi[i - 1]) {
                    b.append(thay);
                }
            } else {
                b.append(goc.charAt(i));
            }
        }
        return b.toString();
    }

    /** Cau bao cho nguoi choi khi ten bi chan. */
    public static String cauBao(final String phan) {
        return "Tên có từ không phù hợp: «" + phan + "». Hãy chọn tên khác.";
    }
}
