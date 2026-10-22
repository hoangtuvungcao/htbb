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
        /** Chi so ca the dong goi (CaThePokemon, 6 x 5 bit). */
        public int iv;
        /** Tinh cach 0..24 (CaThePokemon). */
        public int tinhCach;
        /** charDBID nguoi thu phuc goc (0 = khong ro / con cu). Khac chu hop = nhan qua trao doi. */
        public int chuGoc;
        /** Lan trao doi cuoi (epoch giay, 0 = chua bao gio): khoa trao doi tiep / chuyen doi lay xu mot thoi gian. */
        public long doiLuc;

        public Con(final int id, final int dex, final int cap, final boolean shiny, final int ngay, final String bong) {
            this.id = id;
            this.dex = dex;
            this.cap = Math.max(1, Math.min(100, cap));
            this.shiny = shiny;
            this.ngay = ngay;
            this.bong = bong == null ? "" : bong.replaceAll("[^a-z0-9]", "");
            this.iv = CaThePokemon.ivTatDinh(id, dex, ngay);
            this.tinhCach = CaThePokemon.tinhCachTatDinh(id, dex, ngay);
        }

        /** Ban sao voi id moi (trao doi: giu loai, cap, sac mau, ngay, bong, ca the, chu goc). */
        public Con saoVoiId(final int idMoi) {
            final Con c = new Con(idMoi, this.dex, this.cap, this.shiny, this.ngay, this.bong);
            c.iv = this.iv;
            c.tinhCach = this.tinhCach;
            c.chuGoc = this.chuGoc;
            c.doiLuc = this.doiLuc;
            return c;
        }

        /** Cung mot con (loai, cap, sac mau, ca the) - kiem "con da doi" giua luc chon va luc chot trao doi. */
        public boolean giong(final Con o) {
            return o != null && o.dex == this.dex && o.cap == this.cap && o.shiny == this.shiny && o.iv == this.iv
                    && o.tinhCach == this.tinhCach;
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
    /** Bo dem theo ngay (cung ngay voi batNgay): so Pokemon da NHAN qua trao doi, xu / luong khoa tu chuyen doi. */
    public int doiNgay;
    public long chuyenXuNgay;
    public int chuyenLuongNgay;
    /** Lan trao doi cuoi (epoch giay) - nhip giua hai lan trao doi. */
    public long doiCuoi;
    /** Tong so lan trao doi / chuyen doi (thong ke). */
    public int tongDoi;
    public int tongChuyen;
    /** Cho Pokemon: so con da MUA hom nay (cung ngay voi batNgay). */
    public int muaNgay;
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

    /** Them con moi co ca the + chu goc (thu phuc). */
    public synchronized Con them(final int dex, final int cap, final boolean shiny, final int ngayBat, final String maBong,
            final int iv, final int tinhCach, final int chuGoc) {
        final Con c = them(dex, cap, shiny, ngayBat, maBong);
        if (c != null) {
            c.iv = iv & 0x3FFFFFFF;
            c.tinhCach = Math.max(0, Math.min(CaThePokemon.SO_TINH_CACH - 1, tinhCach));
            c.chuGoc = chuGoc;
        }
        return c;
    }

    /** Nhan mot con tu hop khac (trao doi): id moi trong hop nay, giu moi thu khac. null neu day. */
    public synchronized Con nhan(final Con mau) {
        if (mau == null || day()) {
            return null;
        }
        final Con c = mau.saoVoiId(this.idKe++);
        this.con.add(c);
        this.ban = true;
        return c;
    }

    /** Tra mot con ve hop (cho Pokemon: huy / het han tin) - KHONG xet suc chua (con da o day truoc khi dang ban). */
    public synchronized Con traVe(final Con mau) {
        if (mau == null) {
            return null;
        }
        final Con c = mau.saoVoiId(this.idKe++);
        this.con.add(c);
        this.ban = true;
        return c;
    }

    /** Chuoi mot con (cung dinh dang muc "c" cua maHoa, khong id): dex.cap.shiny.ngay.bong.iv.tc.goc.doi. */
    public static String maHoaCon(final Con c) {
        return c.dex + "." + c.cap + "." + (c.shiny ? 1 : 0) + "." + c.ngay + "." + c.bong + "." + c.iv + "." + c.tinhCach + "."
                + c.chuGoc + "." + c.doiLuc;
    }

    /** Nguoc lai {@link #maHoaCon} (id = 0); null neu hong. */
    public static Con giaiMaCon(final String s) {
        try {
            final String[] p = s.split("\\.");
            final Con c = new Con(0, Integer.parseInt(p[0]), Integer.parseInt(p[1]), "1".equals(p[2]), Integer.parseInt(p[3]),
                    p.length > 4 ? p[4] : "");
            if (p.length > 6) {
                c.iv = Integer.parseInt(p[5]) & 0x3FFFFFFF;
                c.tinhCach = Math.max(0, Math.min(CaThePokemon.SO_TINH_CACH - 1, Integer.parseInt(p[6])));
            }
            if (p.length > 8) {
                c.chuGoc = Integer.parseInt(p[7]);
                c.doiLuc = Math.max(0L, Long.parseLong(p[8]));
            }
            return c.dex > 0 ? c : null;
        } catch (final Exception e) {
            return null;
        }
    }

    /** So con cua loai {@code dex} (khongShiny: chi dem con thuong). */
    public synchronized int soCungLoai(final int dex, final boolean khongShiny) {
        int n = 0;
        for (final Con c : this.con) {
            if (c.dex == dex && (!khongShiny || !c.shiny)) {
                n++;
            }
        }
        return n;
    }

    /** Ban sao doc lap (trao doi chuan bi tren ban sao, chi ap vao hop that sau khi CSDL ghi xong). */
    public synchronized HopPc banSao() {
        return giaiMa(maHoa());
    }

    /** Thay toan bo noi dung bang {@code o} (sau khi CSDL da ghi o). */
    public synchronized void thayBang(final HopPc o) {
        synchronized (o) {
            this.con.clear();
            for (final Con c : o.con) {
                this.con.add(c.saoVoiId(c.id));
            }
            this.keo.clear();
            this.keo.putAll(o.keo);
            this.bong.clear();
            this.bong.putAll(o.bong);
            this.idKe = o.idKe;
            this.lanMoRong = o.lanMoRong;
            this.dangTheo = o.dangTheo;
            this.ngay = o.ngay;
            this.batNgay = o.batNgay;
            this.nemNgay = o.nemNgay;
            this.tongBat = o.tongBat;
            this.doiNgay = o.doiNgay;
            this.chuyenXuNgay = o.chuyenXuNgay;
            this.chuyenLuongNgay = o.chuyenLuongNgay;
            this.doiCuoi = o.doiCuoi;
            this.tongDoi = o.tongDoi;
            this.tongChuyen = o.tongChuyen;
            this.muaNgay = o.muaNgay;
        }
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
            this.doiNgay = 0;
            this.chuyenXuNgay = 0;
            this.chuyenLuongNgay = 0;
            this.muaNgay = 0;
            this.ban = true;
        }
    }

    // ------------------------------------------------------------------ ma hoa

    public synchronized String maHoa() {
        final StringBuilder sb = new StringBuilder();
        sb.append("v=1;ik=").append(this.idKe).append(";mr=").append(this.lanMoRong).append(";dt=").append(this.dangTheo)
                .append(";ng=").append(this.ngay).append(";bn=").append(this.batNgay).append(";nn=").append(this.nemNgay)
                .append(";tb=").append(this.tongBat).append(";dn=").append(this.doiNgay).append(";cx=").append(this.chuyenXuNgay)
                .append(";cl=").append(this.chuyenLuongNgay).append(";dc=").append(this.doiCuoi).append(";td=").append(this.tongDoi)
                .append(";tc=").append(this.tongChuyen).append(";mn=").append(this.muaNgay);
        for (final Map.Entry<Integer, Integer> e : this.keo.entrySet()) {
            sb.append(";k").append(e.getKey()).append('=').append(e.getValue());
        }
        for (final Map.Entry<String, Integer> e : this.bong.entrySet()) {
            sb.append(";b_").append(e.getKey()).append('=').append(e.getValue());
        }
        for (final Con c : this.con) {
            sb.append(";c").append(c.id).append('=').append(c.dex).append('.').append(c.cap).append('.')
                    .append(c.shiny ? 1 : 0).append('.').append(c.ngay).append('.').append(c.bong).append('.').append(c.iv)
                    .append('.').append(c.tinhCach).append('.').append(c.chuGoc).append('.').append(c.doiLuc);
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
                } else if (k.equals("dn")) {
                    h.doiNgay = Math.max(0, Integer.parseInt(v));
                } else if (k.equals("cx")) {
                    h.chuyenXuNgay = Math.max(0L, Long.parseLong(v));
                } else if (k.equals("cl")) {
                    h.chuyenLuongNgay = Math.max(0, Integer.parseInt(v));
                } else if (k.equals("dc")) {
                    h.doiCuoi = Math.max(0L, Long.parseLong(v));
                } else if (k.equals("td")) {
                    h.tongDoi = Math.max(0, Integer.parseInt(v));
                } else if (k.equals("mn")) {
                    h.muaNgay = Math.max(0, Integer.parseInt(v));
                } else if (k.equals("tc")) {
                    h.tongChuyen = Math.max(0, Integer.parseInt(v));
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
                    // 0.9.8 trao doi: ca the + chu goc + lan doi (con cu thieu thi giu IV tat dinh tu id / dex / ngay)
                    if (p.length > 6) {
                        c.iv = Integer.parseInt(p[5]) & 0x3FFFFFFF;
                        c.tinhCach = Math.max(0, Math.min(CaThePokemon.SO_TINH_CACH - 1, Integer.parseInt(p[6])));
                    }
                    if (p.length > 8) {
                        c.chuGoc = Integer.parseInt(p[7]);
                        c.doiLuc = Math.max(0L, Long.parseLong(p[8]));
                    }
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
