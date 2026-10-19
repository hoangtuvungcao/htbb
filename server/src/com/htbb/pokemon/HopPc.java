package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * HOP PC cua MOT nhan vat: cac Pokemon da thu phuc, Keo loai (theo dex goc cua ho), tui bong, bo dem chong bot theo
 * ngay va con "dang di theo" (ban dong hanh CHI DE NHIN - 0 chi so; spec 4.6 de chi so "dong dieu" cho ban 1.1).
 * Luu MOT chuoi "k=v;..." trong tob_hop_pc.du_lieu (bang rieng, luu cung luc luu nhan vat). Lop THUAN, dong bo tren
 * chinh doi tuong (test/KiemBatPokemon).
 */
public final class HopPc {

    /** O mac dinh / moi lan mo rong / toi da. */
    public static final int O_MAC_DINH = 30;
    public static final int O_MOI_LAN = 10;
    public static final int O_TOI_DA = 100;

    /** Mot con trong hop. */
    public static final class Con {
        public final int id;
        public int dex;
        public int cap;
        public boolean shiny;
        /** Ngay bat (yyyyMMdd). */
        public int ngay;
        /** Ma bong da dung. */
        public String bong;

        public Con(final int id, final int dex, final int cap, final boolean shiny, final int ngay, final String bong) {
            this.id = id;
            this.dex = dex;
            this.cap = Math.max(1, Math.min(100, cap));
            this.shiny = shiny;
            this.ngay = ngay;
            this.bong = bong == null ? "" : bong.replaceAll("[^a-z0-9]", "");
        }
    }

    private final List<Con> con = new ArrayList<Con>();
    private final Map<Integer, Integer> keo = new TreeMap<Integer, Integer>();
    private final Map<String, Integer> bong = new TreeMap<String, Integer>();
    private int idKe = 1;
    /** So lan da mo rong (moi lan +O_MOI_LAN o). */
    public int lanMoRong;
    /** id con dang di theo (0 = khong). */
    public int dangTheo;
    /** Bo dem theo ngay (yyyyMMdd): so lan bat duoc / so lan nem. */
    public int ngay;
    public int batNgay;
    public int nemNgay;
    /** Tong so lan bat duoc (thong ke). */
    public int tongBat;
    /** Da doi tu lan luu cuoi. */
    public boolean ban;

    // ------------------------------------------------------------------ o / suc chua

    public synchronized int sucChua() {
        return Math.min(O_TOI_DA, O_MAC_DINH + Math.max(0, this.lanMoRong) * O_MOI_LAN);
    }

    public synchronized int soCon() {
        return this.con.size();
    }

    public synchronized boolean day() {
        return this.con.size() >= sucChua();
    }

    public synchronized boolean moRongDuoc() {
        return sucChua() < O_TOI_DA;
    }

    public synchronized void moRong() {
        if (moRongDuoc()) {
            this.lanMoRong++;
            this.ban = true;
        }
    }

    /** Gia mo rong lan ke (xu): 2 gio thu nhap x 1,5^lanMoRong. */
    public synchronized long giaMoRong(final long xuMoiGio) {
        double g = 2.0 * Math.max(0L, xuMoiGio);
        for (int i = 0; i < this.lanMoRong; i++) {
            g *= 1.5;
        }
        return Math.max(1L, Math.round(g));
    }

    // ------------------------------------------------------------------ con

    /** Them con moi (null neu day). */
    public synchronized Con them(final int dex, final int cap, final boolean shiny, final int ngayBat, final String maBong) {
        if (day() || dex <= 0) {
            return null;
        }
        final Con c = new Con(this.idKe++, dex, cap, shiny, ngayBat, maBong);
        this.con.add(c);
        this.ban = true;
        return c;
    }

    public synchronized Con lay(final int id) {
        for (final Con c : this.con) {
            if (c.id == id) {
                return c;
            }
        }
        return null;
    }

    public synchronized List<Con> danhSach() {
        return new ArrayList<Con>(this.con);
    }

    /** Tha con {@code id} (tra ve con da tha, null neu khong co). Dang theo thi thoi theo. */
    public synchronized Con tha(final int id) {
        for (int i = 0; i < this.con.size(); i++) {
            if (this.con.get(i).id == id) {
                final Con c = this.con.remove(i);
                if (this.dangTheo == id) {
                    this.dangTheo = 0;
                }
                this.ban = true;
                return c;
            }
        }
        return null;
    }

    public synchronized Con conDangTheo() {
        return this.dangTheo > 0 ? lay(this.dangTheo) : null;
    }

    public synchronized void datTheo(final int id) {
        this.dangTheo = id > 0 && lay(id) != null ? id : 0;
        this.ban = true;
    }

    public synchronized void danhDau() {
        this.ban = true;
    }

    // ------------------------------------------------------------------ keo / bong

    public synchronized int keo(final int dexGoc) {
        final Integer v = this.keo.get(Integer.valueOf(dexGoc));
        return v == null ? 0 : v.intValue();
    }

    public synchronized void themKeo(final int dexGoc, final int n) {
        if (dexGoc <= 0 || n == 0) {
            return;
        }
        final int moi = Math.max(0, Math.min(999999, keo(dexGoc) + n));
        if (moi == 0) {
            this.keo.remove(Integer.valueOf(dexGoc));
        } else {
            this.keo.put(Integer.valueOf(dexGoc), Integer.valueOf(moi));
        }
        this.ban = true;
    }

    /** Tru keo (tat ca hoac khong). */
    public synchronized boolean truKeo(final int dexGoc, final int n) {
        if (n < 0 || keo(dexGoc) < n) {
            return false;
        }
        themKeo(dexGoc, -n);
        return true;
    }

    public synchronized int tongKeo() {
        int t = 0;
        for (final Integer v : this.keo.values()) {
            t += v.intValue();
        }
        return t;
    }

    public synchronized int bong(final String ma) {
        final Integer v = this.bong.get(ma);
        return v == null ? 0 : v.intValue();
    }

    public synchronized void themBong(final String ma, final int n) {
        if (ma == null || ma.isEmpty() || n == 0) {
            return;
        }
        final int moi = Math.max(0, Math.min(9999, bong(ma) + n));
        if (moi == 0) {
            this.bong.remove(ma);
        } else {
            this.bong.put(ma, Integer.valueOf(moi));
        }
        this.ban = true;
    }

    public synchronized boolean truBong(final String ma) {
        if (bong(ma) <= 0) {
            return false;
        }
        themBong(ma, -1);
        return true;
    }

    // ------------------------------------------------------------------ bo dem ngay (chong bot)

    /** Doi ngay thi dat lai bo dem. */
    public synchronized void doiNgay(final int homNay) {
        if (this.ngay != homNay) {
            this.ngay = homNay;
            this.batNgay = 0;
            this.nemNgay = 0;
            this.ban = true;
        }
    }

    // ------------------------------------------------------------------ ma hoa

    public synchronized String maHoa() {
        final StringBuilder sb = new StringBuilder();
        sb.append("v=1;ik=").append(this.idKe).append(";mr=").append(this.lanMoRong).append(";dt=").append(this.dangTheo)
                .append(";ng=").append(this.ngay).append(";bn=").append(this.batNgay).append(";nn=").append(this.nemNgay)
                .append(";tb=").append(this.tongBat);
        for (final Map.Entry<Integer, Integer> e : this.keo.entrySet()) {
            sb.append(";k").append(e.getKey()).append('=').append(e.getValue());
        }
        for (final Map.Entry<String, Integer> e : this.bong.entrySet()) {
            sb.append(";b_").append(e.getKey()).append('=').append(e.getValue());
        }
        for (final Con c : this.con) {
            sb.append(";c").append(c.id).append('=').append(c.dex).append('.').append(c.cap).append('.')
                    .append(c.shiny ? 1 : 0).append('.').append(c.ngay).append('.').append(c.bong);
        }
        return sb.toString();
    }

    public static HopPc giaiMa(final String s) {
        final HopPc h = new HopPc();
        if (s == null || s.isEmpty()) {
            return h;
        }
        int idLonNhat = 0;
        for (final String cap : s.split(";")) {
            final int eq = cap.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            final String k = cap.substring(0, eq).trim();
            final String v = cap.substring(eq + 1).trim();
            try {
                if (k.equals("ik")) {
                    h.idKe = Math.max(1, Integer.parseInt(v));
                } else if (k.equals("mr")) {
                    h.lanMoRong = Math.max(0, Math.min((O_TOI_DA - O_MAC_DINH) / O_MOI_LAN, Integer.parseInt(v)));
                } else if (k.equals("dt")) {
                    h.dangTheo = Math.max(0, Integer.parseInt(v));
                } else if (k.equals("ng")) {
                    h.ngay = Integer.parseInt(v);
                } else if (k.equals("bn")) {
                    h.batNgay = Math.max(0, Integer.parseInt(v));
                } else if (k.equals("nn")) {
                    h.nemNgay = Math.max(0, Integer.parseInt(v));
                } else if (k.equals("tb")) {
                    h.tongBat = Math.max(0, Integer.parseInt(v));
                } else if (k.startsWith("b_")) {
                    final int n = Integer.parseInt(v);
                    if (n > 0) {
                        h.bong.put(k.substring(2), Integer.valueOf(Math.min(9999, n)));
                    }
                } else if (k.startsWith("k")) {
                    final int n = Integer.parseInt(v);
                    if (n > 0) {
                        h.keo.put(Integer.valueOf(Integer.parseInt(k.substring(1))), Integer.valueOf(n));
                    }
                } else if (k.startsWith("c")) {
                    final int id = Integer.parseInt(k.substring(1));
                    final String[] p = v.split("\\.");
                    final Con c = new Con(id, Integer.parseInt(p[0]), Integer.parseInt(p[1]),
                            p.length > 2 && "1".equals(p[2]), p.length > 3 ? Integer.parseInt(p[3]) : 0,
                            p.length > 4 ? p[4] : "");
                    if (c.dex > 0 && h.lay(id) == null) {
                        h.con.add(c);
                        idLonNhat = Math.max(idLonNhat, id);
                    }
                }
            } catch (final Exception ignored) {
                // dong hong: bo qua, khong lam mat ca hop
            }
        }
        h.idKe = Math.max(h.idKe, idLonNhat + 1);
        if (h.dangTheo > 0 && h.lay(h.dangTheo) == null) {
            h.dangTheo = 0;
        }
        h.ban = false;
        return h;
    }
}
