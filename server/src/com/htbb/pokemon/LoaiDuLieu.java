package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Bang loai day du (~1.025 loai) nap tu CSDL (sql/them_loai_day_du.sql, sinh boi tools/pokemon/loai/sinh_loai.py):
 * data_loai, data_loai_tien_hoa, data_loai_vung, data_loai_khu_vuc_map. he_thong_day_du.md muc 3.
 *
 * Lop THUAN, BAT BIEN sau khi dung (doi bang = thay ca doi tuong qua {@link #dat}); doc tu moi luong khong can khoa.
 * Bang rong (CSDL cu chua chay tep SQL) = tinh nang tat: moi quai giu loai mac dinh cua mau.
 */
public final class LoaiDuLieu {

    /** So loai toi da cua Pokedex day du (Gen 1-9). */
    public static final int SO_LOAI = 1025;

    /** Bac hiem T1..T6 (2.3). */
    public static final int T1 = 1, T2 = 2, T3 = 3, T4 = 4, T5 = 5, T6 = 6;

    public static final String[] TEN_BAC = {"", "Phổ biến", "Không phổ biến", "Hiếm", "Rất hiếm", "Cực hiếm", "Huyền thoại"};

    /** Mot loai (data_loai + data_loai_sprite). */
    public static final class Loai {
        public final int dex;
        public final String ten;
        public final int theHe, he1, he2;
        /** HP, Tan cong, Phong thu, Tan cong DB, Phong thu DB, Toc do. */
        public final int[] chiSo;
        public final int nhom, dexGoc, tienHoaTu, giaiDoan, bac, gocBat;
        public final boolean batDuoc, chiSuKien, daMo;
        /** 0 thieu (chi Pokedex), 1 dung duoc, 2 du dong tac. */
        public final int sprite;
        public final boolean chanDung, goiMayChu, trongJar;

        public Loai(final int dex, final String ten, final int theHe, final int he1, final int he2, final int[] chiSo,
                final int nhom, final int dexGoc, final int tienHoaTu, final int giaiDoan, final int bac, final int gocBat,
                final boolean batDuoc, final boolean chiSuKien, final boolean daMo, final int sprite, final boolean chanDung,
                final boolean goiMayChu, final boolean trongJar) {
            this.dex = dex;
            this.ten = ten == null ? "" : ten;
            this.theHe = theHe;
            this.he1 = he1;
            this.he2 = he2;
            this.chiSo = chiSo == null ? new int[6] : chiSo.clone();
            this.nhom = nhom;
            this.dexGoc = dexGoc;
            this.tienHoaTu = tienHoaTu;
            this.giaiDoan = giaiDoan;
            this.bac = bac;
            this.gocBat = gocBat;
            this.batDuoc = batDuoc;
            this.chiSuKien = chiSuKien;
            this.daMo = daMo;
            this.sprite = sprite;
            this.chanDung = chanDung;
            this.goiMayChu = goiMayChu;
            this.trongJar = trongJar;
        }

        public int tongChiSo() {
            int s = 0;
            for (final int v : chiSo) {
                s += v;
            }
            return s;
        }

        public boolean coHe(final int he) {
            return he >= 0 && (he1 == he || he2 == he);
        }
    }

    /** Mot buoc tien hoa (data_loai_tien_hoa). */
    public static final class TienHoa {
        public final int tu, sang, cap, idVatPham, ngayDem, thanThiet, keo;
        public final String kieu, coChe, tenVatPham, dieuKien;

        public TienHoa(final int tu, final int sang, final String kieu, final String coChe, final int cap,
                final String tenVatPham, final int idVatPham, final int ngayDem, final int thanThiet, final int keo,
                final String dieuKien) {
            this.tu = tu;
            this.sang = sang;
            this.kieu = kieu == null ? "" : kieu;
            this.coChe = coChe == null ? "" : coChe;
            this.cap = cap;
            this.tenVatPham = tenVatPham == null ? "" : tenVatPham;
            this.idVatPham = idVatPham;
            this.ngayDem = ngayDem;
            this.thanThiet = thanThiet;
            this.keo = keo;
            this.dieuKien = dieuKien == null ? "" : dieuKien;
        }
    }

    /** Noi song (data_loai_vung). */
    public static final class Vung {
        public final int dex, vung, capMin, capMax, trongSo, ngayDem, thoiTiet, bacGap;
        public final String khuVuc;

        public Vung(final int dex, final String khuVuc, final int vung, final int capMin, final int capMax, final int trongSo,
                final int ngayDem, final int thoiTiet, final int bacGap) {
            this.dex = dex;
            this.khuVuc = khuVuc == null ? "" : khuVuc;
            this.vung = vung;
            this.capMin = capMin;
            this.capMax = capMax;
            this.trongSo = trongSo;
            this.ngayDem = ngayDem;
            this.thoiTiet = thoiTiet;
            this.bacGap = bacGap;
        }
    }

    private static volatile LoaiDuLieu hien = new LoaiDuLieu(Collections.<Loai>emptyList(),
            Collections.<TienHoa>emptyList(), Collections.<Vung>emptyList(), Collections.<Integer, String>emptyMap());

    private final Map<Integer, Loai> loai;
    private final List<TienHoa> tienHoa;
    private final Map<Integer, List<TienHoa>> tienHoaTu;
    private final List<Vung> vung;
    /** "khuVuc|vung" -> dong vung. */
    private final Map<String, List<Vung>> theoVung;
    private final Map<Integer, String> khuVucMap;
    private final int bam;

    public LoaiDuLieu(final List<Loai> dsLoai, final List<TienHoa> dsTienHoa, final List<Vung> dsVung,
            final Map<Integer, String> khuVucMap) {
        final Map<Integer, Loai> l = new TreeMap<Integer, Loai>();
        for (final Loai x : dsLoai) {
            if (x != null && x.dex > 0) {
                l.put(Integer.valueOf(x.dex), x);
            }
        }
        this.loai = Collections.unmodifiableMap(l);
        this.tienHoa = Collections.unmodifiableList(new ArrayList<TienHoa>(dsTienHoa));
        final Map<Integer, List<TienHoa>> tu = new HashMap<Integer, List<TienHoa>>();
        for (final TienHoa t : dsTienHoa) {
            List<TienHoa> ds = tu.get(Integer.valueOf(t.tu));
            if (ds == null) {
                ds = new ArrayList<TienHoa>();
                tu.put(Integer.valueOf(t.tu), ds);
            }
            ds.add(t);
        }
        this.tienHoaTu = tu;
        this.vung = Collections.unmodifiableList(new ArrayList<Vung>(dsVung));
        final Map<String, List<Vung>> tv = new HashMap<String, List<Vung>>();
        for (final Vung v : dsVung) {
            final String k = v.khuVuc + "|" + v.vung;
            List<Vung> ds = tv.get(k);
            if (ds == null) {
                ds = new ArrayList<Vung>();
                tv.put(k, ds);
            }
            ds.add(v);
        }
        this.theoVung = tv;
        this.khuVucMap = new HashMap<Integer, String>(khuVucMap);
        this.bam = tinhBam();
    }

    public static LoaiDuLieu hien() {
        return hien;
    }

    public static void dat(final LoaiDuLieu d) {
        if (d != null) {
            hien = d;
        }
    }

    public boolean rong() {
        return loai.isEmpty();
    }

    public int soLoai() {
        return loai.size();
    }

    public int soLoaiDaMo() {
        int n = 0;
        for (final Loai x : loai.values()) {
            if (x.daMo) {
                n++;
            }
        }
        return n;
    }

    public Loai lay(final int dex) {
        return loai.get(Integer.valueOf(dex));
    }

    public boolean daMo(final int dex) {
        final Loai x = lay(dex);
        return x != null && x.daMo;
    }

    public String ten(final int dex) {
        final Loai x = lay(dex);
        return x == null ? "" : x.ten;
    }

    public int bac(final int dex) {
        final Loai x = lay(dex);
        return x == null ? 0 : x.bac;
    }

    public java.util.Collection<Loai> tatCa() {
        return loai.values();
    }

    public List<TienHoa> tatCaTienHoa() {
        return tienHoa;
    }

    public List<Vung> tatCaVung() {
        return vung;
    }

    /** Moi buoc tien hoa tu {@code dex} (nhieu nhanh: Eevee 8, Tyrogue 3...). */
    public List<TienHoa> tienHoaTu(final int dex) {
        final List<TienHoa> ds = tienHoaTu.get(Integer.valueOf(dex));
        return ds == null ? Collections.<TienHoa>emptyList() : ds;
    }

    /**
     * Hop PC (nhanh bat-trade) duoc tien hoa {@code tu -> sang} khong: phai la buoc tien hoa THAT cua chuoi va loai dich
     * da mo (loai Gen 3+ chua mo thi cho den dot mo).
     */
    public boolean tienHoaDuoc(final int tu, final int sang) {
        for (final TienHoa t : tienHoaTu(tu)) {
            if (t.sang == sang) {
                return daMo(sang);
            }
        }
        return false;
    }

    /** Cac buoc tien hoa con dung duoc (loai dich da mo) - menu tien hoa Hop PC. */
    public List<TienHoa> tienHoaMo(final int tu) {
        final List<TienHoa> kq = new ArrayList<TienHoa>();
        for (final TienHoa t : tienHoaTu(tu)) {
            if (daMo(t.sang)) {
                kq.add(t);
            }
        }
        return kq;
    }

    /** Khu vuc dia ly cua ban do (Kanto / Johto / Hoenn), "" = khong ghi (ban do chung). */
    public String khuVuc(final int mapId) {
        final String s = khuVucMap.get(Integer.valueOf(mapId));
        return s == null ? "" : s;
    }

    /** Dong vung cua (khuVuc, vung). khuVuc "" = moi khu vuc. */
    public List<Vung> vung(final String khuVuc, final int vung) {
        if (khuVuc != null && !khuVuc.isEmpty()) {
            final List<Vung> ds = theoVung.get(khuVuc + "|" + vung);
            return ds == null ? Collections.<Vung>emptyList() : ds;
        }
        final List<Vung> kq = new ArrayList<Vung>();
        for (final Map.Entry<String, List<Vung>> e : theoVung.entrySet()) {
            if (e.getKey().endsWith("|" + vung)) {
                kq.addAll(e.getValue());
            }
        }
        return kq;
    }

    /** Ma bam on dinh cua bang loai gui client (-126 kieu 1): doi khi ten / he / bac / the he / co doi. */
    public int bam() {
        return bam;
    }

    private int tinhBam() {
        int h = 17;
        for (final Loai x : loai.values()) {
            h = h * 31 + x.dex;
            h = h * 31 + x.ten.hashCode();
            h = h * 31 + x.he1;
            h = h * 31 + x.he2;
            h = h * 31 + x.bac;
            h = h * 31 + x.theHe;
            h = h * 31 + coClient(x);
        }
        return h == 0 ? 1 : h;
    }

    /** Co cua loai trong bang gui client: bit0 da mo, bit1 co sprite (jar / goi may chu), bit2 co chan dung. */
    public static int coClient(final Loai x) {
        return (x.daMo ? 1 : 0) | (x.goiMayChu || x.trongJar ? 2 : 0) | (x.chanDung ? 4 : 0);
    }

    /** Ten the he cho Pokedex. */
    public static String tenTheHe(final int g) {
        switch (g) {
            case 1: return "Kanto";
            case 2: return "Johto";
            case 3: return "Hoenn";
            case 4: return "Sinnoh";
            case 5: return "Unova";
            case 6: return "Kalos";
            case 7: return "Alola";
            case 8: return "Galar";
            case 9: return "Paldea";
            default: return "?";
        }
    }
}
