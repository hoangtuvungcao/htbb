import com.htbb.pokemon.ChonLoaiHoangDa;
import com.htbb.pokemon.GoiLoai;
import com.htbb.pokemon.LoaiDuLieu;
import com.htbb.pokemon.Pokedex;
import com.htbb.pokemon.PokedexDayDu;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

/**
 * LOAI DAY DU (he_thong_day_du.md 3-5): du lieu 1.025 loai, chuoi tien hoa, chieu, noi song, sprite, chon loai khi hoi
 * sinh, goi -125 / -126 va fallback client cu. Doc THANG sql/them_loai_day_du.sql (nguon su that khi trien khai, sinh
 * boi tools/pokemon/loai/sinh_loai.py) - khong can CSDL.
 */
public final class KiemLoaiDayDu {

    static final String SQL = "../sql/them_loai_day_du.sql";

    // ------------------------------------------------------------------ doc INSERT trong tep SQL

    /** bang -> danh sach dong (cot -> gia tri: Long, String hoac null). */
    static Map<String, List<Map<String, Object>>> docSql(final String s) {
        final Map<String, List<Map<String, Object>>> kq = new HashMap<String, List<Map<String, Object>>>();
        int i = 0;
        while (true) {
            final int a = s.indexOf("INSERT ", i);
            if (a < 0) {
                break;
            }
            final int into = s.indexOf("INTO `", a);
            final int tenHet = s.indexOf('`', into + 6);
            final String bang = s.substring(into + 6, tenHet);
            final int moCot = s.indexOf('(', tenHet);
            final int dongCot = s.indexOf(')', moCot);
            final String[] cot = s.substring(moCot + 1, dongCot).replace("`", "").split(",\\s*");
            int p = s.indexOf("VALUES", dongCot) + 6;
            final List<Map<String, Object>> ds = kq.computeIfAbsent(bang, (k) -> new ArrayList<Map<String, Object>>());
            while (true) {
                while (p < s.length() && Character.isWhitespace(s.charAt(p))) {
                    p++;
                }
                if (s.charAt(p) != '(') {
                    break;
                }
                p++;
                final List<Object> v = new ArrayList<Object>();
                while (true) {
                    while (s.charAt(p) == ' ') {
                        p++;
                    }
                    if (s.charAt(p) == '\'') {
                        final StringBuilder sb = new StringBuilder();
                        p++;
                        while (true) {
                            final char c = s.charAt(p);
                            if (c == '\\') {
                                sb.append(s.charAt(p + 1));
                                p += 2;
                            } else if (c == '\'' && s.charAt(p + 1) == '\'') {
                                sb.append('\'');
                                p += 2;
                            } else if (c == '\'') {
                                p++;
                                break;
                            } else {
                                sb.append(c);
                                p++;
                            }
                        }
                        v.add(sb.toString());
                    } else {
                        int q = p;
                        while (s.charAt(q) != ',' && s.charAt(q) != ')') {
                            q++;
                        }
                        final String t = s.substring(p, q).trim();
                        v.add("NULL".equals(t) ? null : Long.valueOf(t));
                        p = q;
                    }
                    if (s.charAt(p) == ',') {
                        p++;
                        continue;
                    }
                    p++; // ')'
                    break;
                }
                final Map<String, Object> dong = new LinkedHashMap<String, Object>();
                for (int k = 0; k < cot.length; k++) {
                    dong.put(cot[k], k < v.size() ? v.get(k) : null);
                }
                ds.add(dong);
                while (Character.isWhitespace(s.charAt(p))) {
                    p++;
                }
                if (s.charAt(p) == ',') {
                    p++;
                    continue;
                }
                break;
            }
            i = p;
        }
        return kq;
    }

    static int so(final Map<String, Object> d, final String c) {
        final Object o = d.get(c);
        return o == null ? 0 : ((Long) o).intValue();
    }

    static String chu(final Map<String, Object> d, final String c) {
        final Object o = d.get(c);
        return o == null ? "" : o.toString();
    }

    static LoaiDuLieu dung(final Map<String, List<Map<String, Object>>> b) {
        final Map<Integer, Map<String, Object>> sp = new HashMap<Integer, Map<String, Object>>();
        for (final Map<String, Object> d : b.get("data_loai_sprite")) {
            sp.put(so(d, "dex"), d);
        }
        final List<LoaiDuLieu.Loai> loai = new ArrayList<LoaiDuLieu.Loai>();
        for (final Map<String, Object> d : b.get("data_loai")) {
            final Map<String, Object> s = sp.get(so(d, "dex"));
            loai.add(new LoaiDuLieu.Loai(so(d, "dex"), chu(d, "ten"), so(d, "the_he"), so(d, "he1"), so(d, "he2"),
                    new int[]{so(d, "hp"), so(d, "tan_cong"), so(d, "phong_thu"), so(d, "tan_cong_db"), so(d, "phong_thu_db"),
                            so(d, "toc_do")}, so(d, "nhom"), so(d, "dex_goc"), so(d, "tien_hoa_tu"), so(d, "giai_doan"),
                    so(d, "bac"), so(d, "goc_bat"), so(d, "bat_duoc") != 0, so(d, "chi_su_kien") != 0, so(d, "bat") != 0,
                    s == null ? 0 : so(s, "trang_thai"), s != null && so(s, "chan_dung") != 0,
                    s != null && so(s, "goi_may_chu") != 0, s != null && so(s, "trong_jar") != 0));
        }
        final List<LoaiDuLieu.TienHoa> th = new ArrayList<LoaiDuLieu.TienHoa>();
        for (final Map<String, Object> d : b.get("data_loai_tien_hoa")) {
            th.add(new LoaiDuLieu.TienHoa(so(d, "tu_dex"), so(d, "sang_dex"), chu(d, "kieu"), chu(d, "co_che"), so(d, "cap"),
                    chu(d, "ten_vat_pham"), so(d, "id_vat_pham"), so(d, "ngay_dem"), so(d, "than_thiet"), so(d, "keo"),
                    chu(d, "dieu_kien")));
        }
        final List<LoaiDuLieu.Vung> vung = new ArrayList<LoaiDuLieu.Vung>();
        for (final Map<String, Object> d : b.get("data_loai_vung")) {
            vung.add(new LoaiDuLieu.Vung(so(d, "dex"), chu(d, "khu_vuc"), so(d, "vung"), so(d, "cap_min"), so(d, "cap_max"),
                    so(d, "trong_so"), so(d, "ngay_dem"), so(d, "thoi_tiet"), so(d, "bac_gap")));
        }
        final Map<Integer, String> km = new HashMap<Integer, String>();
        for (final Map<String, Object> d : b.get("data_loai_khu_vuc_map")) {
            km.put(so(d, "map_id"), chu(d, "khu_vuc"));
        }
        return new LoaiDuLieu(loai, th, vung, km);
    }

    static Map<String, List<Map<String, Object>>> bang;
    static LoaiDuLieu d;

    public static void chay() {
        Kiem.nhom("KiemLoaiDayDu: du lieu 1.025 loai + chon loai + goi -125/-126");
        try {
            chayHet();
        } catch (final Exception e) {
            e.printStackTrace();
            Kiem.dung("KiemLoaiDayDu chay het khong loi: " + e, false);
        }
    }

    static void chayHet() throws Exception {
        final String s = new String(Files.readAllBytes(new File(SQL).toPath()), StandardCharsets.UTF_8);
        bang = docSql(s);
        d = dung(bang);
        kiemTep(s);
        kiemLoai();
        kiemTienHoa();
        kiemChieu();
        kiemVung();
        kiemSprite();
        kiemBangPhu();
        kiemChon();
        kiemGoi();
        kiemPokedex();
    }

    // ------------------------------------------------------------------ tep

    static void kiemTep(final String s) {
        Kiem.dung("sql: moi CREATE TABLE deu IF NOT EXISTS (idempotent)",
                !s.matches("(?s).*CREATE TABLE (?!IF NOT EXISTS).*"));
        Kiem.sai("sql: khong DROP / TRUNCATE", s.contains("DROP TABLE") || s.contains("TRUNCATE"));
        int xoa = 0;
        for (final String l : s.split("\n")) {
            if (l.startsWith("DELETE FROM")) {
                xoa++;
                Kiem.dung("sql: DELETE chi o data_loai_vung (" + l.substring(0, Math.min(40, l.length())) + ")",
                        l.startsWith("DELETE FROM `data_loai_vung`"));
            }
        }
        Kiem.bang("sql: dung 1 DELETE", 1, xoa);
        Kiem.dung("sql: ghi nguon PokeAPI ghim commit", s.contains("PokeAPI 2bf639e5572812a67d2b94903fdcb9d095f5aeb2"));
        Kiem.dung("giay phep: docs/giay-phep.md chep BSD-3 PokeAPI + MIT Showdown", docTep("../docs/giay-phep.md")
                .contains("Redistributions of source code must retain") && docTep("../docs/giay-phep.md").contains("The MIT License"));
    }

    static String docTep(final String p) {
        try {
            return new String(Files.readAllBytes(new File(p).toPath()), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }

    // ------------------------------------------------------------------ loai

    static final int[] GOC_BAT = {0, 400, 250, 120, 60, 25, 30};
    static final int[] DAU_GEN = {0, 1, 152, 252, 387, 494, 650, 722, 810, 906, 1026};

    static void kiemLoai() {
        Kiem.bang("data_loai: 1.025 loai", 1025, d.soLoai());
        Kiem.bang("data_loai: so dong SQL = 1.025 (khong trung dex)", 1025, bang.get("data_loai").size());
        int heSai = 0, bstSai = 0, bacSai = 0, genSai = 0, tenRong = 0, t6Bat = 0, gocSai = 0;
        final int[] demBac = new int[7];
        for (int dex = 1; dex <= 1025; dex++) {
            final LoaiDuLieu.Loai l = d.lay(dex);
            if (l == null) {
                Kiem.dung("thieu loai " + dex, false);
                continue;
            }
            if (l.he1 < 0 || l.he1 > 17 || l.he2 < -1 || l.he2 > 17 || l.he1 == l.he2) {
                heSai++;
            }
            if (l.bac < 1 || l.bac > 6) {
                bacSai++;
            } else {
                demBac[l.bac]++;
                if (l.gocBat != GOC_BAT[l.bac]) {
                    gocSai++;
                }
            }
            if (dex < DAU_GEN[l.theHe] || dex >= DAU_GEN[l.theHe + 1]) {
                genSai++;
            }
            if (l.ten.trim().isEmpty()) {
                tenRong++;
            }
            if (l.bac == 6 && l.batDuoc) {
                t6Bat++;
            }
        }
        for (final Map<String, Object> r : bang.get("data_loai")) {
            final int t = so(r, "hp") + so(r, "tan_cong") + so(r, "phong_thu") + so(r, "tan_cong_db") + so(r, "phong_thu_db")
                    + so(r, "toc_do");
            if (t != so(r, "tong_chi_so")) {
                bstSai++;
            }
        }
        Kiem.bang("loai: he 0..17, he1 != he2", 0, heSai);
        Kiem.bang("loai: tong chi so = tong 6 chi so", 0, bstSai);
        Kiem.bang("loai: bac T1..T6", 0, bacSai);
        Kiem.bang("loai: goc bat dung bac (400/250/120/60/25/30 phan nghin)", 0, gocSai);
        Kiem.bang("loai: the he khop dai so dex", 0, genSai);
        Kiem.bang("loai: ten khong rong", 0, tenRong);
        Kiem.bang("loai: T6 khong bat ngoai troi (chi dot kich)", 0, t6Bat);
        Kiem.dung("loai: co du 6 bac (T6 >= 100 huyen thoai / huyen ao / UB / Nghich ly)", demBac[6] >= 100 && demBac[5] >= 5);
        Kiem.bang("ten chinh thuc: Pikachu", "Pikachu", d.ten(25));
        Kiem.bang("ten chinh thuc: Farfetch'd (dau nhay ASCII)", "Farfetch'd", d.ten(83));
        Kiem.bang("bac: Dragonite T5", 5, d.bac(149));
        Kiem.bang("bac: Mewtwo T6", 6, d.bac(150));
        Kiem.bang("bac: Snorlax T4 (loai don, khong tinh baby)", 4, d.bac(143));
        Kiem.bang("bac: Rattata T1", 1, d.bac(19));
        int mo = 0, moSai = 0;
        for (final LoaiDuLieu.Loai l : d.tatCa()) {
            if (l.daMo) {
                mo++;
                if (l.theHe > 2) {
                    moSai++;
                }
            }
        }
        Kiem.bang("dot A: 251 loai da mo (Gen 1-2)", 251, mo);
        Kiem.bang("dot A: khong mo loai Gen 3+", 0, moSai);
    }

    // ------------------------------------------------------------------ tien hoa

    static void kiemTienHoa() {
        int thieuDich = 0, thieuCanh = 0, lechTu = 0, vong = 0, gocSai = 0, coCheSai = 0;
        final Set<String> canh = new HashSet<String>();
        for (final LoaiDuLieu.TienHoa t : d.tatCaTienHoa()) {
            canh.add(t.tu + ">" + t.sang);
            final LoaiDuLieu.Loai a = d.lay(t.tu), b = d.lay(t.sang);
            if (a == null || b == null) {
                thieuDich++;
                continue;
            }
            if (b.tienHoaTu != t.tu) {
                lechTu++;
            }
            if (!Arrays.asList("cap", "vat_pham", "trao_doi", "than_thiet", "keo").contains(t.coChe) || t.cap < 1 || t.cap > 100) {
                coCheSai++;
            }
            if ("vat_pham".equals(t.coChe) && t.idVatPham <= 0) {
                coCheSai++;
            }
        }
        for (final LoaiDuLieu.Loai l : d.tatCa()) {
            if (l.tienHoaTu > 0 && !canh.contains(l.tienHoaTu + ">" + l.dex)) {
                thieuCanh++;
            }
            final Set<Integer> da = new HashSet<Integer>();
            int cur = l.dex, goc = l.dex;
            while (cur > 0) {
                if (!da.add(cur)) {
                    vong++;
                    break;
                }
                goc = cur;
                final LoaiDuLieu.Loai x = d.lay(cur);
                cur = x == null ? 0 : x.tienHoaTu;
            }
            if (goc != l.dexGoc) {
                gocSai++;
            }
        }
        Kiem.bang("tien hoa: moi loai dich / nguon ton tai", 0, thieuDich);
        Kiem.bang("tien hoa: moi loai co tien_hoa_tu deu co canh (chuoi day du)", 0, thieuCanh);
        Kiem.bang("tien hoa: canh khop tien_hoa_tu cua loai dich", 0, lechTu);
        Kiem.bang("tien hoa: khong co vong", 0, vong);
        Kiem.bang("tien hoa: dex_goc = goc chuoi", 0, gocSai);
        Kiem.bang("tien hoa: co che HTBB hop le (vat_pham phai co id data_item)", 0, coCheSai);
        Kiem.bang("Eevee 8 nhanh", 8, d.tienHoaTu(133).size());
        Kiem.bang("Tyrogue 3 nhanh", 3, d.tienHoaTu(236).size());
        Kiem.bang("Oddish -> Gloom", 44, d.tienHoaTu(43).get(0).sang);
        Kiem.bang("Gloom 2 nhanh (Vileplume da La / Bellossom da Mat Troi)", 2, d.tienHoaTu(44).size());
        Kiem.bang("Bulbasaur -> Ivysaur cap 16", "cap16", d.tienHoaTu(1).get(0).coChe + d.tienHoaTu(1).get(0).cap);
        Kiem.bang("Pikachu -> Raichu bang Da Sam (961)", 961, d.tienHoaTu(25).get(0).idVatPham);
        Kiem.bang("Sandshrew -> Sandslash theo cap (khong lay dang Alola)", "cap", d.tienHoaTu(27).get(0).coChe);
        Kiem.bang("Haunter -> Gengar trao doi", "trao_doi", d.tienHoaTu(93).get(0).coChe);
        Kiem.bang("Espeon ban ngay", 1, timTienHoa(133, 196).ngayDem);
        Kiem.bang("Umbreon ban dem", 2, timTienHoa(133, 197).ngayDem);
        Kiem.dung("Hop PC: Pikachu -> Raichu duoc (ca hai da mo)", d.tienHoaDuoc(25, 26));
        Kiem.sai("Hop PC: Magneton -> Magnezone chua duoc (Gen 4 chua mo)", d.tienHoaDuoc(82, 462));
        Kiem.sai("Hop PC: buoc khong co trong chuoi (Pikachu -> Charizard)", d.tienHoaDuoc(25, 6));
        Kiem.bang("Hop PC: Eevee da mo 5 nhanh Gen 1-2", 5, d.tienHoaMo(133).size());
    }

    static LoaiDuLieu.TienHoa timTienHoa(final int tu, final int sang) {
        for (final LoaiDuLieu.TienHoa t : d.tienHoaTu(tu)) {
            if (t.sang == sang) {
                return t;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------ chieu

    static void kiemChieu() throws Exception {
        final Map<String, Map<String, Object>> chieu = new HashMap<String, Map<String, Object>>();
        for (final Map<String, Object> r : bang.get("data_chieu")) {
            chieu.put(chu(r, "ma"), r);
        }
        final Set<String> hu = new HashSet<String>();
        for (final String l : Files.readAllLines(new File("../du_lieu/bang_hieu_ung.csv").toPath(), StandardCharsets.UTF_8)) {
            final String[] c = l.split(",");
            if (c.length > 4) {
                hu.add(c[4].replace(".bytes", ""));
            }
        }
        int heSai = 0, huSai = 0, dacTrung = 0;
        final Set<String> ten = new HashSet<String>();
        for (final Map<String, Object> r : chieu.values()) {
            if (so(r, "he") < 0 || so(r, "he") > 17) {
                heSai++;
            }
            if (!hu.contains(chu(r, "hieu_ung"))) {
                huSai++;
            }
            ten.add(chu(r, "ten"));
            if (r.get("dac_trung_ho") != null) {
                dacTrung++;
            }
        }
        Kiem.dung("data_chieu: ~180 chieu dung chung (150..220)", chieu.size() >= 150 && chieu.size() <= 220);
        Kiem.bang("data_chieu: ten khong trung", chieu.size(), ten.size());
        Kiem.bang("data_chieu: he 0..17", 0, heSai);
        Kiem.bang("data_chieu: hieu ung co san (du_lieu/bang_hieu_ung.csv)", 0, huSai);
        Kiem.dung("data_chieu: co chieu dac trung", dacTrung >= 20);
        final Map<Integer, List<Map<String, Object>>> theoLoai = new TreeMap<Integer, List<Map<String, Object>>>();
        for (final Map<String, Object> r : bang.get("data_loai_chieu")) {
            theoLoai.computeIfAbsent(so(r, "dex"), (k) -> new ArrayList<Map<String, Object>>()).add(r);
        }
        int thieu4 = 0, khongCo = 0, stabSai = 0, dacTrungSai = 0, trung = 0;
        final Set<Integer> khoiDau = new HashSet<Integer>(Arrays.asList(1, 4, 7, 152, 155, 158, 252, 255, 258, 387, 390, 393,
                495, 498, 501, 650, 653, 656, 722, 725, 728, 810, 813, 816, 906, 909, 912));
        final Map<Integer, Integer> bacHo = new HashMap<Integer, Integer>();
        for (final LoaiDuLieu.Loai l : d.tatCa()) {
            bacHo.merge(l.dexGoc, l.bac, Math::max);
        }
        for (int dex = 1; dex <= 1025; dex++) {
            final List<Map<String, Object>> ds = theoLoai.get(dex);
            if (ds == null || ds.size() != 4) {
                thieu4++;
                continue;
            }
            final Set<String> ma = new HashSet<String>();
            final Set<Integer> o = new HashSet<Integer>();
            int stab = 0;
            final LoaiDuLieu.Loai l = d.lay(dex);
            for (final Map<String, Object> r : ds) {
                o.add(so(r, "o"));
                ma.add(chu(r, "ma_chieu"));
                final Map<String, Object> c = chieu.get(chu(r, "ma_chieu"));
                if (c == null) {
                    khongCo++;
                    continue;
                }
                if (l.coHe(so(c, "he"))) {
                    stab++;
                }
                if (c.get("dac_trung_ho") != null) {
                    final int ho = so(c, "dac_trung_ho");
                    if (ho != l.dexGoc || (bacHo.get(ho) < 4 && !khoiDau.contains(ho))) {
                        dacTrungSai++;
                    }
                }
            }
            if (ma.size() != 4 || !o.equals(new HashSet<Integer>(Arrays.asList(0, 1, 2, 3)))) {
                trung++;
            }
            if (stab < 2) {
                stabSai++;
            }
        }
        Kiem.bang("chieu loai: moi loai dung 4 o", 0, thieu4);
        Kiem.bang("chieu loai: moi chieu co trong data_chieu", 0, khongCo);
        Kiem.bang("chieu loai: 4 o 0..3 khac nhau", 0, trung);
        Kiem.bang("chieu loai: >= 2 chieu cung he loai (STAB)", 0, stabSai);
        Kiem.bang("chieu loai: chieu dac trung chi cho ho T4-T6 / khoi dau, dung ho", 0, dacTrungSai);
        boolean coCharizard = false;
        for (final Map<String, Object> r : theoLoai.get(6)) {
            coCharizard |= "dt.hoa_thieu".equals(chu(r, "ma_chieu"));
        }
        Kiem.dung("Charizard co chieu dac trung Hoa thieu", coCharizard);
    }

    // ------------------------------------------------------------------ vung

    static void kiemVung() {
        int chuaMo = 0, t6 = 0, sai = 0, baby = 0;
        final Set<Integer> coVung = new HashSet<Integer>();
        for (final LoaiDuLieu.Vung v : d.tatCaVung()) {
            final LoaiDuLieu.Loai l = d.lay(v.dex);
            if (l == null || !l.daMo) {
                chuaMo++;
                continue;
            }
            if (l.bac >= 6 || v.bacGap >= 6) {
                t6++;
            }
            if (l.giaiDoan == 0) {
                baby++;
            }
            if (!Arrays.asList("Kanto", "Johto", "Hoenn").contains(v.khuVuc) || v.vung < 0 || v.vung > 13 || v.capMin < 1
                    || v.capMax > 100 || v.capMin > v.capMax || v.trongSo <= 0 || v.bacGap < 1 || v.ngayDem < 0 || v.ngayDem > 2
                    || v.thoiTiet < -1 || v.thoiTiet > 5) {
                sai++;
            }
            coVung.add(v.dex);
        }
        Kiem.bang("vung: chi loai DA MO", 0, chuaMo);
        Kiem.bang("vung: khong co T6 ngoai troi", 0, t6);
        Kiem.bang("vung: khong co baby (chi no tu trung)", 0, baby);
        Kiem.bang("vung: gia tri hop le (khu vuc, vung, cap, trong so, gio, thoi tiet)", 0, sai);
        int thieu = 0;
        for (final LoaiDuLieu.Loai l : d.tatCa()) {
            if (l.daMo && l.bac <= 4 && l.giaiDoan > 0 && !coVung.contains(l.dex)) {
                thieu++;
                System.out.println("        loai da mo khong co noi song: " + l.dex + " " + l.ten);
            }
        }
        Kiem.bang("vung: moi loai da mo T1-T4 (tru baby) co noi song", 0, thieu);
        Kiem.sai("vung: Dragonite (T5 dang cuoi) khong hoang da", coVung.contains(149));
        Kiem.dung("vung: Dratini (goc gia huyen thoai) gap o luot T5", timVung(147, 5));
        Kiem.bang("khu vuc ban do: Pallet (1) = Kanto", "Kanto", d.khuVuc(1));
        Kiem.bang("khu vuc ban do: khop du_lieu/vung_map.csv", demVungMap(), bang.get("data_loai_khu_vuc_map").size());
    }

    static boolean timVung(final int dex, final int bacGap) {
        for (final LoaiDuLieu.Vung v : d.tatCaVung()) {
            if (v.dex == dex && v.bacGap == bacGap) {
                return true;
            }
        }
        return false;
    }

    static int demVungMap() {
        int n = 0;
        for (final String l : docTep("../du_lieu/vung_map.csv").split("\n")) {
            if (!l.isEmpty() && !l.startsWith("#") && !l.startsWith("id;")) {
                n++;
            }
        }
        return n;
    }

    // ------------------------------------------------------------------ sprite

    static void kiemSprite() throws Exception {
        int moKhongHinh = 0, goiHong = 0, goiLech = 0, n = 0;
        long tong = 0;
        for (final Map<String, Object> r : bang.get("data_loai_sprite")) {
            final int dex = so(r, "dex");
            final File f = new File("map/loai/" + GoiLoai.tenGoi(dex));
            final boolean co = f.isFile();
            if (co != (so(r, "goi_may_chu") != 0)) {
                goiLech++;
            }
            if (co) {
                n++;
                tong += f.length();
                if (GoiLoai.docGoi(Files.readAllBytes(f.toPath()), dex) == null) {
                    goiHong++;
                }
            }
            if (d.daMo(dex) && !co && so(r, "trong_jar") == 0) {
                moKhongHinh++;
            }
        }
        Kiem.bang("sprite: loai da mo deu co hinh (jar hoac goi may chu)", 0, moKhongHinh);
        Kiem.bang("sprite: goi may chu doc duoc, dung dex, co .dl", 0, goiHong);
        Kiem.bang("sprite: data_loai_sprite.goi_may_chu khop tep server/map/loai", 0, goiLech);
        Kiem.dung("sprite: >= 251 goi Gen 1-2, trung binh <= 40 KB", n >= 251 && tong / Math.max(1, n) <= 40 * 1024);
        Kiem.dung("sprite: co ghi cong CC BY-NC (server/map/loai/GHI_CONG.txt)",
                docTep("map/loai/GHI_CONG.txt").contains("CC BY-NC 4.0"));
    }

    // ------------------------------------------------------------------ bang phu (ket-doi / bat-trade)

    static void kiemBangPhu() {
        Kiem.bang("data_nhom_trung: 1.025 loai (luoc do ket-doi)", 1025, bang.get("data_nhom_trung").size());
        int nhomSai = 0;
        for (final Map<String, Object> r : bang.get("data_nhom_trung")) {
            if (so(r, "nhom1") < 1 || so(r, "nhom1") > 15 || (r.get("nhom2") != null && (so(r, "nhom2") < 1 || so(r, "nhom2") > 15))) {
                nhomSai++;
            }
        }
        Kiem.bang("data_nhom_trung: nhom 1..15", 0, nhomSai);
        int chuaMo = 0, dichSai = 0, t6 = 0;
        for (final Map<String, Object> r : bang.get("data_bat_loai")) {
            final LoaiDuLieu.Loai l = d.lay(so(r, "dex"));
            if (l == null || !l.daMo) {
                chuaMo++;
            }
            if (so(r, "tien_hoa_thanh") > 0 && d.lay(so(r, "tien_hoa_thanh")) == null) {
                dichSai++;
            }
            if (l != null && l.bac == 6 && so(r, "bat_duoc") != 0) {
                t6++;
            }
        }
        Kiem.bang("data_bat_loai (bat-trade): 251 loai da mo", 251, bang.get("data_bat_loai").size());
        Kiem.bang("data_bat_loai: chi loai da mo", 0, chuaMo);
        Kiem.bang("data_bat_loai: tien_hoa_thanh ton tai", 0, dichSai);
        Kiem.bang("data_bat_loai: T6 khong bat duoc", 0, t6);
        Kiem.bang("data_bat_chi_so_loai: 1.025 loai", 1025, bang.get("data_bat_chi_so_loai").size());
    }

    // ------------------------------------------------------------------ chon loai khi hoi sinh

    static void kiemChon() {
        final ChonLoaiHoangDa.ThamSo ts = new ChonLoaiHoangDa.ThamSo();
        final Random r = new Random(42);
        final int N = 400000;
        final int[] dem = new int[7];
        int ngoaiCap = 0, chuaMo = 0, giu = 0;
        for (int i = 0; i < N; i++) {
            final ChonLoaiHoangDa.KetQua k = ChonLoaiHoangDa.chon(d, "Kanto", 2, 20, 12, false, 0, 16, ts, r);
            if (k.dex == 0) {
                giu++;
                continue;
            }
            dem[k.bac]++;
            final LoaiDuLieu.Loai l = d.lay(k.dex);
            if (l == null || !l.daMo || l.bac >= 6) {
                chuaMo++;
            }
            boolean trongCap = false;
            for (final LoaiDuLieu.Vung v : d.vung("Kanto", 2)) {
                trongCap |= v.dex == k.dex && v.capMin <= 20 && v.capMax >= 20;
            }
            if (!trongCap) {
                ngoaiCap++;
            }
        }
        Kiem.bang("chon: chi loai da mo, khong T6", 0, chuaMo);
        Kiem.bang("chon: loai trong dai cap va vung", 0, ngoaiCap);
        final double p3 = dem[3] / (double) N, p4 = dem[4] / (double) N;
        System.out.println("        Kanto dong co cap 20, trua: T3 " + p3 + ", T4 " + p4 + ", T1-T2 " + (dem[1] + dem[2]) / (double) N
                + ", giu " + giu / (double) N);
        Kiem.dung("chon: T3 ~ 1/100 (0,7..1,3%)", p3 > 0.007 && p3 < 0.013);
        Kiem.dung("chon: T4 ~ 1/600 (0,10..0,24%) hoac khong co ung vien", (p4 > 0.0010 && p4 < 0.0024)
                || ChonLoaiHoangDa.ungVien(d, "Kanto", 2, 20, false, 0, 4, 4).isEmpty());
        Kiem.bang("chon: T5 khong ra ban ngay quang dang", 0, dem[5]);
        Kiem.dung("chon: doi T1-T2 ~ 30% (khong lam phong spawn: chi doi loai)", (dem[1] + dem[2]) / (double) N > 0.25
                && (dem[1] + dem[2]) / (double) N < 0.33);
        Kiem.bang("he so: gio it nguoi x1,5", 150, ChonLoaiHoangDa.heSoPhanTram(2, ts));
        Kiem.bang("he so: ban ngay x1", 100, ChonLoaiHoangDa.heSoPhanTram(14, ts));
        ts.heSoToiDaPhanTram = 120;
        Kiem.bang("he so: kep tran", 120, ChonLoaiHoangDa.heSoPhanTram(2, ts));

        // du lieu tu dung: loai chua mo / T6 / T5 dieu kien / khong ung vien
        final List<LoaiDuLieu.Loai> ls = new ArrayList<LoaiDuLieu.Loai>();
        ls.add(loaiThu(1, 1, true));
        ls.add(loaiThu(2, 1, false)); // chua mo
        ls.add(loaiThu(3, 6, true)); // T6
        ls.add(loaiThu(4, 4, true));
        final List<LoaiDuLieu.Vung> vs = new ArrayList<LoaiDuLieu.Vung>();
        vs.add(new LoaiDuLieu.Vung(1, "Kanto", 3, 1, 100, 100, 0, -1, 1));
        vs.add(new LoaiDuLieu.Vung(2, "Kanto", 3, 1, 100, 100000, 0, -1, 1));
        vs.add(new LoaiDuLieu.Vung(3, "Kanto", 3, 1, 100, 100000, 0, -1, 6));
        vs.add(new LoaiDuLieu.Vung(4, "Kanto", 3, 1, 100, 100, 2, 2, 5));
        final LoaiDuLieu thu = new LoaiDuLieu(ls, new ArrayList<LoaiDuLieu.TienHoa>(), vs, new HashMap<Integer, String>());
        final ChonLoaiHoangDa.ThamSo t2 = new ChonLoaiHoangDa.ThamSo();
        t2.doiThuongPhanTram = 100;
        t2.gapT5 = 1;
        boolean xauChon = false;
        int ra5Dem = 0, ra5Ngay = 0, ra5Mua = 0;
        for (int i = 0; i < 2000; i++) {
            final ChonLoaiHoangDa.KetQua a = ChonLoaiHoangDa.chon(thu, "Kanto", 3, 50, 12, false, 0, 0, t2, r);
            xauChon |= a.dex == 2 || a.dex == 3;
            ra5Ngay += a.dex == 4 ? 1 : 0;
            ra5Dem += ChonLoaiHoangDa.chon(thu, "Kanto", 3, 50, 22, true, 0, 0, t2, r).dex == 4 ? 1 : 0;
            ra5Mua += ChonLoaiHoangDa.chon(thu, "Kanto", 3, 50, 12, false, 2, 0, t2, r).dex == 4 ? 1 : 0;
        }
        Kiem.sai("chon: khong bao gio ra loai chua mo / T6 (du trong so lon)", xauChon);
        Kiem.bang("chon: T5 khong ra khi sai gio va sai thoi tiet", 0, ra5Ngay);
        Kiem.dung("chon: T5 ra khi dung gio (dem)", ra5Dem > 0);
        Kiem.dung("chon: T5 ra khi dung thoi tiet (mua)", ra5Mua > 0);
        Kiem.bang("chon: khong ung vien -> giu loai mau (khong roi bac)", 0,
                ChonLoaiHoangDa.chon(thu, "Johto", 3, 50, 12, false, 0, 0, t2, r).dex);
        Kiem.bang("chon: bang rong -> giu", 0, ChonLoaiHoangDa.chon(new LoaiDuLieu(new ArrayList<LoaiDuLieu.Loai>(),
                new ArrayList<LoaiDuLieu.TienHoa>(), new ArrayList<LoaiDuLieu.Vung>(), new HashMap<Integer, String>()),
                "Kanto", 3, 50, 12, false, 0, 0, t2, r).dex);
    }

    static LoaiDuLieu.Loai loaiThu(final int dex, final int bac, final boolean mo) {
        return new LoaiDuLieu.Loai(dex, "L" + dex, 1, 0, -1, new int[]{50, 50, 50, 50, 50, 50}, bac == 6 ? 1 : 0, dex, 0, 1, bac,
                GOC_BAT[bac], bac != 6, false, mo, 1, true, true, false);
    }

    // ------------------------------------------------------------------ goi

    static void kiemGoi() throws Exception {
        // fallback client cu (D9: gate 909)
        Kiem.bang("fallback: client 908 thay loai cua mau", 16, real.LoaiQuaiGame.dexHienThi(147, 16, 908, 909));
        Kiem.bang("fallback: PC Unity (-1) thay loai cua mau", 16, real.LoaiQuaiGame.dexHienThi(147, 16, -1, 909));
        Kiem.bang("client 909 thay loai rieng", 147, real.LoaiQuaiGame.dexHienThi(147, 16, 909, 909));
        Kiem.bang("khong loai rieng: loai mau", 16, real.LoaiQuaiGame.dexHienThi(0, 16, 950, 909));

        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        final List<int[]> ds = new ArrayList<int[]>();
        ds.add(new int[]{1234, 147, 0, 5});
        ds.add(new int[]{7, 0, 0, 0});
        GoiLoai.ghiQuaiCon(new DataOutputStream(bo), ds);
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(bo.toByteArray()));
        Kiem.bang("-125: n", 2, in.readUnsignedByte());
        Kiem.bang("-125: idQuai, dex, shiny, bac", "1234,147,0,5", in.readShort() + "," + in.readShort() + ","
                + in.readUnsignedByte() + "," + in.readUnsignedByte());
        Kiem.bang("-125: dex 0 = ve loai mau", "7,0", in.readShort() + "," + in.readShort());
        Kiem.bang("-125: do dai 1 + 2 x 6", 13, bo.size());
        final List<int[]> nhieu = new ArrayList<int[]>();
        for (int i = 0; i < 300; i++) {
            nhieu.add(new int[]{i, 1, 0, 1});
        }
        bo = new ByteArrayOutputStream();
        Kiem.bang("-125: toi da 255 quai mot goi", 255, GoiLoai.ghiQuaiCon(new DataOutputStream(bo), nhieu));

        // -126 bang loai + bam
        bo = new ByteArrayOutputStream();
        GoiLoai.ghiBangLoai(new DataOutputStream(bo), d);
        in = new DataInputStream(new ByteArrayInputStream(bo.toByteArray()));
        Kiem.bang("-126 bang: kieu 1", 1, in.readUnsignedByte());
        Kiem.bang("-126 bang: bam", d.bam(), in.readInt());
        final int n = in.readUnsignedShort();
        Kiem.bang("-126 bang: 1.025 loai", 1025, n);
        int moClient = 0;
        String ten25 = "";
        for (int i = 0; i < n; i++) {
            final int dex = in.readShort();
            final String ten = in.readUTF();
            in.readByte();
            in.readByte();
            in.readUnsignedByte();
            in.readUnsignedByte();
            final int co = in.readUnsignedByte();
            moClient += (co & 1);
            if (dex == 25) {
                ten25 = ten;
            }
        }
        Kiem.bang("-126 bang: het goi", 0, in.available());
        Kiem.bang("-126 bang: 251 loai co bit da mo", 251, moClient);
        Kiem.bang("-126 bang: ten Pikachu", "Pikachu", ten25);
        Kiem.dung("-126 bang: < 32 KB (gui mot lan, client cache theo bam)", bo.size() < 32 * 1024);
        Kiem.bang("bam on dinh", dung(bang).bam(), d.bam());

        // -126 sprite theo khuc ghep lai dung goi
        final File f = new File("map/loai/0016.goi");
        final byte[] goi = Files.readAllBytes(f.toPath());
        final ByteArrayOutputStream ghep = new ByteArrayOutputStream();
        int khuc = 0;
        for (int vt = 0; vt < goi.length; vt += GoiLoai.KHUC) {
            bo = new ByteArrayOutputStream();
            GoiLoai.ghiKhucSprite(new DataOutputStream(bo), 16, 0, goi, vt);
            in = new DataInputStream(new ByteArrayInputStream(bo.toByteArray()));
            in.readUnsignedByte();
            in.readShort();
            in.readUnsignedByte();
            final int tong = in.readInt();
            final int viTri = in.readInt();
            final int len = in.readUnsignedShort();
            final byte[] b = new byte[len];
            in.readFully(b);
            if (tong != goi.length || viTri != vt || len > GoiLoai.KHUC) {
                Kiem.dung("-126 sprite: khuc sai", false);
            }
            ghep.write(b);
            khuc++;
        }
        Kiem.dung("-126 sprite: ghep cac khuc = goi goc", Arrays.equals(goi, ghep.toByteArray()));
        Kiem.bang("-126 sprite: so khuc", GoiLoai.soKhuc(goi.length), khuc);
        bo = new ByteArrayOutputStream();
        GoiLoai.ghiKhucSprite(new DataOutputStream(bo), 999, 0, null, 0);
        Kiem.bang("-126 sprite: khong co goi = tong 0, khong len (1+2+1+4+4)", 12, bo.size());
        final byte[] hong = goi.clone();
        hong[0] = 'X';
        Kiem.bang("goi: sai ma -> null", null, GoiLoai.docGoi(hong, 16));
        Kiem.bang("goi: sai dex -> null", null, GoiLoai.docGoi(goi, 17));
        Kiem.bang("goi: cat cut -> null", null, GoiLoai.docGoi(Arrays.copyOf(goi, goi.length / 2), 16));
        Kiem.bang("ten goi 4 chu so", "0016.goi", GoiLoai.tenGoi(16));

        // bang thong 64 KB/s
        final GoiLoai.ThungToken t = new GoiLoai.ThungToken(64 * 1024, 0);
        long gui = 0;
        for (long ms = 0; ms <= 10000; ms += 50) {
            while (t.lay(4112, ms)) {
                gui += 4112;
            }
        }
        Kiem.dung("bang thong: 10 giay <= 64 KB/s x 11 (ke ca du tru)", gui <= 64L * 1024 * 11 && gui >= 64L * 1024 * 9);
    }

    // ------------------------------------------------------------------ Pokedex

    static void kiemPokedex() {
        final Pokedex dx = new Pokedex();
        dx.ghiGap(16, 1);
        dx.ghiHa(16, 1);
        dx.ghiGap(152, 1);
        Kiem.bang("Pokedex co: da gap + da ha", 3, PokedexDayDu.co(dx, 16));
        Kiem.bang("Pokedex co: chi gap", 1, PokedexDayDu.co(dx, 152));
        Kiem.bang("Pokedex co: chua gap", 0, PokedexDayDu.co(dx, 25));
        final String s = PokedexDayDu.tomTat(dx, d);
        System.out.println("        " + s.replace("\n", "\n        "));
        Kiem.dung("Pokedex: tong 1.025 loai", s.contains("1.025 loài"));
        Kiem.dung("Pokedex: Kanto gap 1/151", s.contains("Kanto (#1–151): gặp 1/151"));
        Kiem.dung("Pokedex: the he chua mo hien 'Chưa xuất hiện'", s.contains("Chưa xuất hiện") && s.contains("Hoenn … Paldea"));
    }
}
