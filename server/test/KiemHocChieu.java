import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.htbb.pokemon.ChieuPokemon;
import com.htbb.pokemon.HePokemon;
import com.htbb.pokemon.HuanLuyen;
import com.htbb.pokemon.Learnset;
import com.htbb.pokemon.LoaiPokemon;
import com.htbb.pokemon.ThongSoLauDai;

/**
 * HOC CHIEU THEO LOAI + DIEM HUAN LUYEN (de xuat 1). Doc THANG sql/hoc_chieu_huan_luyen.sql (nguon duy nhat) va kiem:
 * <ul>
 *   <li>learnset hop le cho MOI loai nguoi choi (21 loai): du o, cap hoc &gt;= cap loai xuat hien, tien hoa khong mat
 *       chieu, he chieu hop he loai (hoac Thuong / phu he co ly do), nhanh Eevee co STAB o 2/8/10, chieu NPC dung o
 *       client 906 (Map.idSkill) + co gia, moc cap chieu tang dan, icon co that (web + client);</li>
 *   <li>Diem huan luyen: tran moi dong, giam dan, tong theo cap, phan bo vuot tran / vuot tong bi tu choi, chenh lech
 *       mot chi so &lt;= x1,2 (cu x4,3), tich HP x Tan cong x Thu gan nhu khong doi theo cach phan bo;</li>
 *   <li>gia tay: lan dau 0, sau = 1 gio thu nhap; cho 24 gio;</li>
 *   <li>chuyen doi nhan vat cu: idempotent, khong mat diem nao (tong moi = 2 x (cap - 1) + huy hieu), ti le giu theo
 *       cach cong cu, May man / diem chua cong -&gt; chua dung; chieu sai loai bi go va diem duoc hoan; chay lai khong
 *       doi; lan sau (go = false) khong bao gio go chieu;</li>
 *   <li>chi so goc moi van qua phep kiem chong sua chi so CU (CharNapDuLieu) o moi cap - tat co van an toan.</li>
 * </ul>
 */
public final class KiemHocChieu {

    static final String TEP = "../sql/hoc_chieu_huan_luyen.sql";

    private KiemHocChieu() {
    }

    public static void chay() {
        Kiem.nhom("Hoc chieu theo loai + Diem huan luyen (sql/hoc_chieu_huan_luyen.sql)");
        try {
            chayThat();
        } catch (final Exception e) {
            e.printStackTrace();
            Kiem.dung("KiemHocChieu chay khong loi: " + e, false);
        }
    }

    static String doc(final String p) throws Exception {
        return new String(Files.readAllBytes(new File(p).toPath()), StandardCharsets.UTF_8);
    }

    // ------------------------------------------------------------------ doc SQL

    /** Cac bo gia tri cua khoi INSERT INTO bang (...) VALUES (...),(...) ON DUPLICATE ... */
    static List<List<String>> dong(final String sql, final String bang) {
        final List<List<String>> ra = new ArrayList<List<String>>();
        final int a = sql.indexOf("INSERT INTO " + bang + " ");
        if (a < 0) {
            return ra;
        }
        final int v = sql.indexOf("VALUES", a);
        final int z = sql.indexOf("ON DUPLICATE KEY UPDATE", v);
        final String k = sql.substring(v + 6, z);
        int i = 0;
        while (i < k.length()) {
            final char c = k.charAt(i);
            if (c == '(') {
                final List<String> bo = new ArrayList<String>();
                final StringBuilder cur = new StringBuilder();
                boolean nhay = false;
                i++;
                for (; i < k.length(); i++) {
                    final char d = k.charAt(i);
                    if (nhay) {
                        if (d == '\'') {
                            nhay = false;
                        } else {
                            cur.append(d);
                        }
                    } else if (d == '\'') {
                        nhay = true;
                    } else if (d == ',') {
                        bo.add(cur.toString().trim());
                        cur.setLength(0);
                    } else if (d == ')') {
                        bo.add(cur.toString().trim());
                        break;
                    } else {
                        cur.append(d);
                    }
                }
                ra.add(bo);
            }
            i++;
        }
        return ra;
    }

    static int n(final String s) {
        return Integer.parseInt(s.trim());
    }

    /** Nap du lieu SQL vao Learnset + HuanLuyen (nhu real.HocChieuGame.napLai). */
    static List<Learnset.Dong> nap(final String sql) {
        final List<Learnset.Dong> ls = new ArrayList<Learnset.Dong>();
        for (final List<String> r : dong(sql, "data_learnset")) {
            ls.add(new Learnset.Dong(n(r.get(0)), n(r.get(1)), n(r.get(2)), r.get(3), n(r.get(4)), r.get(5),
                    Learnset.nguon(r.get(6)), n(r.get(7)), n(r.get(8)), Learnset.moc(r.get(9))));
        }
        Learnset.dat(ls);
        final List<HuanLuyen.Dong> hl = new ArrayList<HuanLuyen.Dong>();
        for (final List<String> r : dong(sql, "data_huan_luyen")) {
            hl.add(new HuanLuyen.Dong(n(r.get(0)), r.get(1), r.get(2), r.get(3), n(r.get(4)), n(r.get(5)), n(r.get(6)),
                    n(r.get(7)), n(r.get(8))));
        }
        HuanLuyen.datDong(hl);
        final Map<Integer, int[]> tt = new HashMap<Integer, int[]>();
        for (final List<String> r : dong(sql, "data_tang_truong_dong")) {
            tt.put(n(r.get(0)), new int[]{n(r.get(2)), n(r.get(3)), n(r.get(4)), n(r.get(5)), n(r.get(6))});
        }
        HuanLuyen.datTangTruong(tt);
        return ls;
    }

    /** dong (class) cua loai nguoi choi. */
    static int dongCua(final int dex) {
        for (int c = 0; c < 5; c++) {
            int d = LoaiPokemon.khoiDau(c);
            if (d == dex) {
                return c;
            }
            if (c == 2 && LoaiPokemon.laNhanhEevee(dex)) {
                return 2;
            }
            for (int i = 0; i < 3; i++) {
                final int sau = LoaiPokemon.tienHoaKeTiep(d, 100);
                if (sau == LoaiPokemon.KHONG) {
                    break;
                }
                d = sau;
                if (d == dex) {
                    return c;
                }
            }
        }
        return -1;
    }

    /** Cap som nhat loai xuat hien (tien hoa theo cap / nhanh Eevee tu 25). */
    static int capXuatHien(final int dex) {
        if (LoaiPokemon.laNhanhEevee(dex)) {
            return LoaiPokemon.CAP_NHANH_EEVEE;
        }
        for (int c = 0; c < 5; c++) {
            int d = LoaiPokemon.khoiDau(c);
            int cap = 1;
            for (int i = 0; i < 4; i++) {
                if (d == dex) {
                    return cap;
                }
                int len = -1;
                for (int l = 1; l <= 100; l++) {
                    if (LoaiPokemon.tienHoaKeTiep(d, l) != LoaiPokemon.KHONG) {
                        len = l;
                        break;
                    }
                }
                if (len < 0) {
                    break;
                }
                d = LoaiPokemon.tienHoaKeTiep(d, len);
                cap = len;
            }
        }
        return -1;
    }

    /** He phu co ly do theo dong (chieu canon cua loai, ghi trong docs §2.3). */
    static final Map<Integer, Set<Integer>> PHU_HE = new HashMap<Integer, Set<Integer>>();

    static {
        PHU_HE.put(0, new HashSet<Integer>(Arrays.asList((int) HePokemon.RONG)));                    // Charizard: Vuot rong
        PHU_HE.put(1, new HashSet<Integer>());
        PHU_HE.put(2, new HashSet<Integer>(Arrays.asList((int) HePokemon.BONG_TOI, (int) HePokemon.SIEU_LINH, (int) HePokemon.MA,
                (int) HePokemon.THEP)));                                                                  // Can, Nghi ngoi, Chia se, Duoi sat
        PHU_HE.put(3, new HashSet<Integer>());
        PHU_HE.put(4, new HashSet<Integer>(Arrays.asList((int) HePokemon.SIEU_LINH)));               // Nhanh nhen (Agility)
    }

    /** So o chieu moi dong ma client 906 co (PokeChieu.TEN) - dem tu ma nguon client. */
    static int[] soOClient() throws Exception {
        final String s = doc("../client_jar/src/game/pokemon/PokeChieu.java");
        final int a = s.indexOf("TEN = {");
        final int b = s.indexOf("};", a);
        final Matcher m = Pattern.compile("\\{([^{}]*)\\}").matcher(s.substring(a + 6, b));
        final List<Integer> ra = new ArrayList<Integer>();
        while (m.find()) {
            ra.add(m.group(1).split(",").length);
        }
        final int[] r = new int[ra.size()];
        for (int i = 0; i < r.length; i++) {
            r[i] = ra.get(i);
        }
        return r;
    }

    static void chayThat() throws Exception {
        final String sql = doc(TEP);
        Kiem.dung("SQL idempotent: CREATE TABLE IF NOT EXISTS", !sql.matches("(?s).*CREATE TABLE (?!IF NOT EXISTS).*"));
        Kiem.sai("SQL khong INSERT ... SELECT ... ON DUPLICATE (luat repo)", sql.matches("(?s).*INSERT INTO[^;]*SELECT[^;]*ON DUPLICATE.*"));
        Kiem.sai("SQL khong 'col=col' trong ON DUPLICATE", sql.matches("(?s).*\\b(\\w+)=\\1\\b.*"));
        Kiem.sai("SQL khong sua du lieu nguoi choi (UPDATE / DELETE tob_)", sql.matches("(?s).*(UPDATE|DELETE FROM)\\s+tob_.*"));
        final List<Learnset.Dong> ls = nap(sql);
        Kiem.dung("data_learnset co du lieu (>= 150 dong)", ls.size() >= 150);
        Kiem.dung("data_huan_luyen 4 dong (HP, Tan cong, Phong thu, PP)", HuanLuyen.soDong() == 4
                && HuanLuyen.viTri(HuanLuyen.HP) >= 0 && HuanLuyen.viTri(HuanLuyen.TAN_CONG) >= 0
                && HuanLuyen.viTri(HuanLuyen.PHONG_THU) >= 0 && HuanLuyen.viTri(HuanLuyen.PP) >= 0);
        Kiem.dung("data_tang_truong_dong du 5 dong", HuanLuyen.coDuLieu() && HuanLuyen.tangTruong(4) != null);

        kiemLearnset(ls);
        kiemHuanLuyen();
        kiemChuyenDoi();
        kiemMaNguon();
    }

    // ------------------------------------------------------------------ learnset

    /** Loai tien hoa thanh {@code dex} (nhanh Eevee: Eevee), -1 neu la dang dau. */
    static int truocTienHoa(final int dex) {
        if (LoaiPokemon.laNhanhEevee(dex)) {
            return LoaiPokemon.EEVEE;
        }
        for (final int d : LoaiPokemon.tatCaDex()) {
            if (LoaiPokemon.tienHoaKeTiep(d, 100) == dex) {
                return d;
            }
        }
        return -1;
    }

    /** Map.idSkill (o chieu NPC day, client 906 gui chi so trong mang nay) - doc tu ma nguon (Map nap CSDL luc nap lop). */
    static int[][] idSkill() throws Exception {
        final String s = doc("src/real/Map.java");
        final int a = s.indexOf("idSkill = new byte[][]{");
        final int b = s.indexOf("};", a);
        final Matcher m = Pattern.compile("\\{([0-9, ]+)\\}").matcher(s.substring(a, b));
        final List<int[]> ra = new ArrayList<int[]>();
        while (m.find()) {
            final String[] p = m.group(1).split(",");
            final int[] x = new int[p.length];
            for (int i = 0; i < p.length; i++) {
                x[i] = Integer.parseInt(p[i].trim());
            }
            ra.add(x);
        }
        return ra.toArray(new int[0][]);
    }

    static void kiemLearnset(final List<Learnset.Dong> ls) throws Exception {
        final int[] oClient = soOClient();
        final int[][] idSkill = idSkill();
        Kiem.bang("Map.idSkill doc duoc 5 dong", 5, idSkill.length);
        final Map<Integer, Integer> soCua = new HashMap<Integer, Integer>();
        final Set<String> trung = new HashSet<String>();
        final Map<String, String> mocTheoO = new HashMap<String, String>();
        final Map<String, String> giaTheoO = new HashMap<String, String>();
        int hong = 0;
        for (final Learnset.Dong r : ls) {
            final String k = r.loai + "/" + r.o;
            if (!trung.add(k)) {
                Kiem.dung("learnset khong trung (loai, o) " + k, false);
            }
            soCua.put(r.loai, soCua.containsKey(r.loai) ? soCua.get(r.loai) + 1 : 1);
            final int dong = dongCua(r.loai);
            final String ten = LoaiPokemon.ten(r.loai) + " o " + r.o + " " + r.ten;
            boolean ok = dong == r.dong && dong >= 0;
            ok &= r.o >= 0 && r.o < ChieuPokemon.soChieu(r.dong) && r.dong < oClient.length && r.o < oClient[r.dong];
            ok &= new File("../web/public/chieu/" + r.dong + "-" + r.o + ".png").isFile();
            // chieu MOI cua loai (dang truoc khong co) chi hoc khi loai da xuat hien (tien hoa / chon nhanh)
            final int truoc = truocTienHoa(r.loai);
            final boolean moi = truoc < 0 || Learnset.lay(truoc, r.o) == null;
            ok &= (!moi || r.capHoc >= capXuatHien(r.loai)) && r.capHoc >= 1 && r.capHoc <= 100;
            ok &= r.ten != null && !r.ten.trim().isEmpty();
            ok &= r.moc != null && r.moc[0] == r.capHoc;
            for (int i = 1; r.moc != null && i < r.moc.length; i++) {
                ok &= r.moc[i] >= r.moc[i - 1] && r.moc[i] <= 100;
            }
            // NPC: dung o client cu gui (Map.idSkill) + co gia; tu hoc: 0 xu
            boolean npcO = false;
            for (final int b : idSkill[r.dong]) {
                npcO |= b == r.o;
            }
            ok &= npcO == r.laNpc() && (r.laNpc() ? r.giaXu > 0 : r.giaXu == 0);
            // he: cung he loai / Thuong / phu he co ly do; nhanh Eevee o 2/8/10 = he nhanh (STAB)
            final int h1 = LoaiPokemon.he1(r.loai), h2 = LoaiPokemon.he2(r.loai);
            final boolean heOk = r.he == h1 || r.he == h2 || r.he == HePokemon.THUONG || PHU_HE.get(r.dong).contains(r.he)
                    || (r.dong == 2 && r.loai == LoaiPokemon.EEVEE);
            ok &= heOk && r.he >= 0 && r.he < 18;
            if (LoaiPokemon.laNhanhEevee(r.loai) && (r.o == 2 || r.o == 8 || r.o == 10)) {
                ok &= r.he == h1;
            } else {
                ok &= r.he == ChieuPokemon.heChieu(r.dong, r.o); // dong thuong = bang he server (STAB, khac he)
            }
            if (!ok) {
                hong++;
                Kiem.dung("learnset hop le: " + ten + " (dong " + r.dong + ", he " + r.he + ", cap " + r.capHoc + ", moc "
                        + Arrays.toString(r.moc) + ")", false);
            }
            final String ko = r.dong + "/" + r.o;
            final String moc = Arrays.toString(r.moc);
            if (mocTheoO.containsKey(ko) && !mocTheoO.get(ko).equals(moc)) {
                Kiem.dung("moc cap chieu cung (dong, o) giong nhau moi loai: " + ko, false);
            }
            mocTheoO.put(ko, moc);
            final String gia = r.giaXu + "/" + r.nguon;
            if (giaTheoO.containsKey(ko) && !giaTheoO.get(ko).equals(gia)) {
                Kiem.dung("gia / nguon cung (dong, o) giong nhau moi loai: " + ko, false);
            }
            giaTheoO.put(ko, gia);
        }
        Kiem.bang("learnset: moi dong hop le (loai, he, cap, moc, NPC, icon)", 0, hong);
        for (final int dex : LoaiPokemon.tatCaDex()) {
            final int dong = dongCua(dex);
            final Learnset.Dong o0 = Learnset.lay(dex, 0);
            Kiem.dung("learnset " + LoaiPokemon.ten(dex) + ": co chieu o 0 hoc o cap <= cap xuat hien (danh duoc ngay)",
                    o0 != null && o0.capHoc <= Math.max(1, capXuatHien(dex)));
            Kiem.dung("learnset " + LoaiPokemon.ten(dex) + ": >= 4 chieu", soCua.containsKey(dex) && soCua.get(dex) >= 4);
            // tien hoa khong mat chieu, cung cap hoc
            final int sau = LoaiPokemon.tienHoaKeTiep(dex, 100);
            if (sau != LoaiPokemon.KHONG) {
                boolean giu = true;
                for (int o = 0; o < 15; o++) {
                    final Learnset.Dong a = Learnset.lay(dex, o);
                    final Learnset.Dong b = Learnset.lay(sau, o);
                    if (a != null) {
                        giu &= b != null && b.capHoc == a.capHoc;
                    }
                }
                Kiem.dung("tien hoa " + LoaiPokemon.ten(dex) + " -> " + LoaiPokemon.ten(sau) + " khong mat chieu nao", giu);
            }
            if (dex == LoaiPokemon.EEVEE) {
                for (final int nh : LoaiPokemon.NHANH_EEVEE) {
                    boolean giu = true;
                    for (int o = 0; o < 15; o++) {
                        if (Learnset.lay(dex, o) != null) {
                            giu &= Learnset.lay(nh, o) != null && Learnset.lay(nh, o).capHoc == Learnset.lay(dex, o).capHoc;
                        }
                    }
                    Kiem.dung("nhanh Eevee " + LoaiPokemon.ten(nh) + " giu moi chieu cua Eevee", giu);
                }
            }
            // dang cuoi hoc du moi o cua dong
            if (sau == LoaiPokemon.KHONG && dex != LoaiPokemon.EEVEE) {
                boolean du = true;
                for (int o = 0; o < ChieuPokemon.soChieu(dong); o++) {
                    du &= Learnset.lay(dex, o) != null;
                }
                Kiem.dung("dang cuoi " + LoaiPokemon.ten(dex) + " hoc duoc du " + ChieuPokemon.soChieu(dong) + " chieu", du);
            }
        }
        // khop bang thiet ke data_noi_dung_chieu (du_lieu/noi_dung_chieu.csv): ten + cap mo som nhat cua moi (dong, o)
        int lech = 0;
        for (final String l : doc("../du_lieu/noi_dung_chieu.csv").split("\n")) {
            final String[] c = csv(l);
            if (c.length < 9 || !c[0].matches("\\d+")) {
                continue;
            }
            final Learnset.Dong r = Learnset.somNhat(n(c[0]), n(c[2]));
            if (r == null || r.capHoc != n(c[8]) || !r.ten.equals(c[3]) && !LoaiPokemon.laNhanhEevee(r.loai)) {
                lech++;
                Kiem.dung("learnset khop noi_dung_chieu: dong " + c[0] + " o " + c[2] + " " + c[3] + " cap mo " + c[8], false);
            }
        }
        Kiem.bang("learnset: cap hoc som nhat + ten khop bang thiet ke noi_dung_chieu (47 chieu)", 0, lech);
        // hoc theo giai doan: dang 1 chua co chieu dang 3 (thiet ke 2.3)
        Kiem.dung("Charmander chua hoc duoc Sung phun lua (Charizard cap 36)", Learnset.lay(4, 6) == null && Learnset.lay(6, 6).capHoc == 36);
        Kiem.dung("Pichu hoc Tia chop khi la Pikachu cap 10", Learnset.lay(172, 3) == null && Learnset.lay(25, 3).capHoc == 10);
        Kiem.bang("Vaporeon o 2 he Nuoc (STAB)", (int) HePokemon.NUOC, Learnset.heChieu(134, 2, 2));
        Kiem.bang("Eevee goc o 2 van Bong toi (Can)", (int) HePokemon.BONG_TOI, Learnset.heChieu(133, 2, 2));
        Kiem.bang("loai ngoai bang -> he theo dong (ChieuPokemon)", ChieuPokemon.heChieu(0, 7), Learnset.heChieu(9999, 0, 7));
        Kiem.bang("so o dong Eevee = 11", 11, Learnset.soO(2));
        Kiem.dung("chieu NPC Eevee = o 8/9/10 (Map.idSkill)", Learnset.laNpc(2, 8) && Learnset.laNpc(2, 10) && !Learnset.laNpc(2, 6));
    }

    // ------------------------------------------------------------------ diem huan luyen

    static void kiemHuanLuyen() {
        final HuanLuyen.Dong hp = HuanLuyen.dong(HuanLuyen.viTri(HuanLuyen.HP));
        Kiem.bang("0 diem = 0", 0, HuanLuyen.phanNghin(hp, 0));
        Kiem.bang("40 diem = +12,0%", 120, HuanLuyen.phanNghin(hp, 40));
        Kiem.bang("80 diem (tran) = +20,0%", 200, HuanLuyen.phanNghin(hp, 80));
        Kiem.bang("vuot tran khong tang (200 diem van +20%)", 200, HuanLuyen.phanNghin(hp, 200));
        Kiem.dung("giam dan: diem 41..80 tang it hon diem 1..40",
                HuanLuyen.phanNghin(hp, 80) - HuanLuyen.phanNghin(hp, 79) < HuanLuyen.phanNghin(hp, 1));
        for (int i = 0; i < HuanLuyen.soDong(); i++) {
            Kiem.dung("dong " + HuanLuyen.dong(i).ten + ": tran <= +20% (spec 4.2)", HuanLuyen.phanNghinToiDa(HuanLuyen.dong(i)) <= 200);
        }
        Kiem.bang("tong diem cap 1 = 0", 0, HuanLuyen.tongDiem(1, 0, 2, 16));
        Kiem.bang("tong diem cap 100 = 198 (2 / cap)", 198, HuanLuyen.tongDiem(100, 0, 2, 16));
        Kiem.bang("tong diem cap 100 + 16 huy hieu = 214", 214, HuanLuyen.tongDiem(100, 16, 2, 16));
        Kiem.bang("huy hieu co tran (30 huy hieu van +16)", 214, HuanLuyen.tongDiem(100, 30, 2, 16));

        final int[] rong = new int[4];
        final int tc = HuanLuyen.viTri(HuanLuyen.TAN_CONG);
        int[] them = new int[4];
        them[tc] = 81;
        Kiem.dung("phan bo vuot tran 1 dong bi tu choi", HuanLuyen.kiemPhanBo(rong, them, 214) != null);
        them = new int[4];
        them[tc] = 50;
        Kiem.dung("phan bo vuot tong diem bi tu choi", HuanLuyen.kiemPhanBo(rong, them, 40) != null);
        them = new int[4];
        them[tc] = -5;
        Kiem.dung("phan bo am bi tu choi", HuanLuyen.kiemPhanBo(rong, them, 100) != null);
        Kiem.dung("phan bo sai do dai bi tu choi", HuanLuyen.kiemPhanBo(rong, new int[2], 100) != null);
        them = new int[4];
        them[tc] = 30;
        them[HuanLuyen.viTri(HuanLuyen.HP)] = 10;
        Kiem.dung("phan bo hop le duoc nhan", HuanLuyen.kiemPhanBo(rong, them, 40) == null);

        // chenh lech: moi cach phan bo hop le o cap 100 + 16 huy hieu -> mot chi so <= x1,2 so voi khong cong
        int maxMot = 0;
        double tichMax = 0, tichMin = Double.MAX_VALUE;
        final int tong = HuanLuyen.tongDiem(100, 16, 2, 16);
        for (int a = 0; a <= 80; a += 2) {
            for (int b = 0; b <= 80 && a + b <= tong; b += 2) {
                final int c = Math.min(80, tong - a - b);
                final int[] pb = new int[4];
                pb[HuanLuyen.viTri(HuanLuyen.HP)] = a;
                pb[tc] = b;
                pb[HuanLuyen.viTri(HuanLuyen.PHONG_THU)] = c;
                pb[HuanLuyen.viTri(HuanLuyen.PP)] = Math.max(0, Math.min(80, tong - a - b - c));
                for (int i = 0; i < 4; i++) {
                    maxMot = Math.max(maxMot, HuanLuyen.phanNghin(HuanLuyen.dong(i), pb[i]));
                }
                if (a + b + c == tong) {
                    final double t = (1000 + HuanLuyen.phanNghin(hp, a)) / 1000.0
                            * (1000 + HuanLuyen.phanNghin(HuanLuyen.dong(tc), b)) / 1000.0
                            * (1000 + HuanLuyen.phanNghin(HuanLuyen.dong(HuanLuyen.viTri(HuanLuyen.PHONG_THU)), c)) / 1000.0;
                    tichMax = Math.max(tichMax, t);
                    tichMin = Math.min(tichMin, t);
                }
            }
        }
        Kiem.dung("mot chi so lech toi da x1,20 (cu: x4,3)", maxMot <= 200);
        Kiem.dung("tich HP x Tan cong x Thu theo cach phan bo du diem (cap 100) lech <= 2% (" + tichMin + " .. " + tichMax + ")",
                tichMax / tichMin <= 1.02);

        // tay diem
        final long gio = ThongSoLauDai.xuMoiGio(50);
        Kiem.bang("tay lan dau mien phi", 0L, HuanLuyen.giaTay(0, gio, 1000));
        Kiem.bang("tay lan 2 = 1 gio thu nhap cap 50", gio, HuanLuyen.giaTay(1, gio, 1000));
        Kiem.dung("gia tay theo cap (cap 90 > cap 20)", HuanLuyen.giaTay(3, ThongSoLauDai.xuMoiGio(90), 1000)
                > HuanLuyen.giaTay(3, ThongSoLauDai.xuMoiGio(20), 1000));
        Kiem.bang("tay lan dau khong cho", 0L, HuanLuyen.choTay(0, 0L, 1000L, 24));
        Kiem.bang("vua tay xong: cho 24 gio", 24L * 3600000L, HuanLuyen.choTay(1, 5000L, 5000L, 24));
        Kiem.bang("qua 24 gio: het cho", 0L, HuanLuyen.choTay(1, 5000L, 5000L + 24L * 3600000L, 24));

        // chuoi luu theo id
        final int[] pb = new int[4];
        pb[tc] = 37;
        pb[HuanLuyen.viTri(HuanLuyen.PP)] = 5;
        Kiem.dung("luu / doc phan bo (id:diem) khong lech", Arrays.equals(pb, HuanLuyen.docChuoi(HuanLuyen.viet(pb))));
        Kiem.bang("chuoi hong -> 0", 0, HuanLuyen.daDung(HuanLuyen.docChuoi("x:1,9:4,abc")));
        Kiem.bang("cat theo tong (ha cap) bo bot dong cuoi", 30, HuanLuyen.daDung(HuanLuyen.catTheoTong(new int[]{20, 20, 0, 0}, 30)));
    }

    // ------------------------------------------------------------------ chuyen doi nhan vat cu

    static int[] goc(final int c) {
        final byte[] b = data.Database.basicPoint[c];
        return new int[]{b[0], b[1], b[2], b[3], b[4]};
    }

    /** Chi so cu khi cong f diem tu do theo ti le w (5 chi so). */
    static int[] cu(final int c, final int cap, final int[] f) {
        final int[] g = goc(c);
        final int[] r = new int[5];
        for (int i = 0; i < 5; i++) {
            r[i] = g[i] + (cap - 1) + f[i];
        }
        return r;
    }

    static void kiemChuyenDoi() {
        data.Database.basicPoint = new byte[][]{{25, 20, 10, 25, 10}, {30, 20, 10, 20, 10}, {10, 25, 35, 10, 10}, {20, 30, 10, 20, 10}, {20, 30, 15, 15, 10}};
        final int hp = HuanLuyen.viTri(HuanLuyen.HP), tc = HuanLuyen.viTri(HuanLuyen.TAN_CONG),
                th = HuanLuyen.viTri(HuanLuyen.PHONG_THU), pp = HuanLuyen.viTri(HuanLuyen.PP);
        // Charmander cap 50 don het Suc manh
        final int tong = HuanLuyen.tongDiem(50, 0, 2, 16);
        int[] pb = HuanLuyen.quyDoi(0, 50, cu(0, 50, new int[]{245, 0, 0, 0, 0}), goc(0), 0, tong);
        Kiem.bang("Charmander 50 don Suc manh -> Tan cong 80 (tran), 18 diem chua dung", "80/18",
                pb[tc] + "/" + (tong - HuanLuyen.daDung(pb)));
        pb = HuanLuyen.quyDoi(0, 50, cu(0, 50, new int[]{0, 0, 0, 245, 0}), goc(0), 0, tong);
        Kiem.bang("Charmander 50 don Suc khoe -> HP 80", 80, pb[hp]);
        pb = HuanLuyen.quyDoi(0, 50, cu(0, 50, new int[]{100, 45, 0, 100, 0}), goc(0), 0, tong);
        Kiem.dung("chia deu -> giu ti le (Tan cong = HP > Phong thu > 0)", pb[tc] == pb[hp] && pb[hp] > pb[th] && pb[th] > 0);
        pb = HuanLuyen.quyDoi(0, 50, cu(0, 50, new int[]{0, 0, 0, 0, 245}), goc(0), 0, tong);
        Kiem.bang("don May man -> het thanh diem chua dung", 0, HuanLuyen.daDung(pb));
        pb = HuanLuyen.quyDoi(0, 50, cu(0, 50, new int[]{100, 0, 0, 0, 0}), goc(0), 145, tong);
        Kiem.bang("diem tiem nang chua cong -> diem huan luyen chua dung (ti le)", tong * 100 / 245, pb[tc]);
        pb = HuanLuyen.quyDoi(2, 40, cu(2, 40, new int[]{0, 0, 195, 0, 0}), goc(2), 0, HuanLuyen.tongDiem(40, 0, 2, 16));
        Kiem.bang("Eevee: Tinh than -> Tan cong", 78, pb[tc]);
        pb = HuanLuyen.quyDoi(4, 40, cu(4, 40, new int[]{0, 195, 0, 0, 0}), goc(4), 0, HuanLuyen.tongDiem(40, 0, 2, 16));
        Kiem.bang("Pichu: Kheo leo -> Tan cong", 78, pb[tc]);
        pb = HuanLuyen.quyDoi(3, 40, cu(3, 40, new int[]{0, 0, 195, 0, 0}), goc(3), 0, HuanLuyen.tongDiem(40, 0, 2, 16));
        Kiem.bang("Squirtle: Tinh than -> PP", 78, pb[pp]);

        // khong ai mat diem, moi dong <= tran, chay lai cung ket qua - quet cap x cach cong x dong
        int hong = 0, hongIdem = 0;
        final java.util.Random rd = new java.util.Random(7);
        for (int lan = 0; lan < 3000; lan++) {
            final int c = rd.nextInt(5), cap = 1 + rd.nextInt(100);
            final int tu = 5 * (cap - 1);
            final int[] f = new int[5];
            int con = tu;
            for (int i = 0; i < 5 && con > 0; i++) {
                f[i] = i == 4 ? con : rd.nextInt(con + 1);
                con -= f[i];
            }
            final int t = HuanLuyen.tongDiem(cap, rd.nextInt(17), 2, 16);
            final int[] a = HuanLuyen.quyDoi(c, cap, cu(c, cap, f), goc(c), con, t);
            final int[] b = HuanLuyen.quyDoi(c, cap, cu(c, cap, f), goc(c), con, t);
            boolean ok = HuanLuyen.daDung(a) <= t;
            for (int i = 0; i < a.length; i++) {
                ok &= a[i] >= 0 && a[i] <= HuanLuyen.dong(i).tran;
            }
            if (!ok) {
                hong++;
            }
            if (!Arrays.equals(a, b)) {
                hongIdem++;
            }
        }
        Kiem.bang("quy doi: 3000 nhan vat ngau nhien - khong vuot tong / tran", 0, hong);
        Kiem.bang("quy doi idempotent (chay lai cung ket qua)", 0, hongIdem);

        // chi so goc moi
        final int[] g = HuanLuyen.chiSoGoc(0, 50, goc(0));
        Kiem.bang("Charmander 50: chi so goc moi = goc + 49 x (1 + tang truong)", "[270, 69, 59, 123, 59]", Arrays.toString(g));
        // van qua phep kiem chong sua chi so CU (CharNapDuLieu: tong diem cong <= allBasicPoint, basepoint 0)
        int sai = 0;
        for (int c = 0; c < 5; c++) {
            final int[] t = HuanLuyen.tangTruong(c);
            int s = 0;
            for (final int x : t) {
                s += x;
            }
            if (s != 5) {
                sai++;
            }
            for (int cap = 1; cap <= 100; cap++) {
                final int[] m = HuanLuyen.chiSoGoc(c, cap, goc(c));
                int tongCong = 0;
                for (int i = 0; i < 5; i++) {
                    tongCong += m[i] - goc(c)[i];
                }
                final int allBasic = 5 * (cap - 1) + (cap - 1) * 5;
                if (tongCong > allBasic || m[3] <= 0) {
                    sai++;
                }
            }
        }
        Kiem.bang("tang truong dong tong 5 diem / cap + qua phep kiem chi so cu o moi cap (tat co an toan)", 0, sai);
        Kiem.dung("Squirtle: Kheo leo luon >= Suc manh (khong bi x2/3 tan cong)", HuanLuyen.chiSoGoc(3, 100, goc(3))[1]
                > HuanLuyen.chiSoGoc(3, 100, goc(3))[0]);

        // --- chieu
        // Charmander cap 10: Man khoi (o 4, cap 16) da cong 4 diem, mua Vuot rong (o 7) cap 0, Sung phun lua chua mua
        byte[] sk = {3, 2, 1, 0, 4, 1, -1, 0, -1};
        Learnset.KetQua kq = Learnset.chuanHoa(sk, 9, 4, 0, 10, 10, 6, false, true);
        Kiem.bang("chuyen doi Charmander 10: chieu sau chuan hoa", "[3, 2, 1, 0, 0, 1, -1, -1, -1]", Arrays.toString(sk));
        Kiem.bang("go Man khoi + Vuot rong", "[4, 7]", kq.go.toString());
        Kiem.bang("hoan xu Vuot rong (NPC)", "[7]", kq.hoanNpc.toString());
        // diem: ngan sach 9 - (2 + 1 + 0 + 0) = 6 (cu: 9 - (3+2+1+4+1 - 1 chieu 0 mien phi) = 1)
        Kiem.bang("diem ky nang hoan: 6 (cu con 0 -> hoan diem hoc + diem Man khoi)", 6, kq.diemKyNang);
        final byte[] lai = sk.clone();
        final Learnset.KetQua kq2 = Learnset.chuanHoa(lai, 9, 4, 0, 10, 10, 6, false, true);
        Kiem.dung("chuan hoa lan 2: khong doi (idempotent)", Arrays.equals(sk, lai) && kq2.go.isEmpty() && kq2.diemKyNang == 6);
        // len cap 16 -> Charmeleon tu hoc Xoay lua + Man khoi cap 1
        kq = Learnset.chuanHoa(sk, 9, 5, 0, 16, 16, 6, false, false);
        Kiem.bang("Charmeleon 16 tu hoc o 3 + 4", "[3, 4]", kq.hocMoi.toString());
        Kiem.bang("diem ky nang tang theo cap (16 - 1 - 3)", 12, kq.diemKyNang);
        // go = false khong bao gio go
        sk = new byte[]{1, 1, 1, 5, 5, 1, 2, -1, -1};
        kq = Learnset.chuanHoa(sk, 9, 4, 0, 8, 30, 6, false, false);
        Kiem.dung("lan nap sau (go = false): khong go chieu nao", kq.go.isEmpty() && sk[3] == 5 && sk[6] == 2);
        // vuot ngan sach (sua tay) -> ve cap 1
        sk = new byte[]{9, 9, 9, 0, 0, 0, -1, -1, -1};
        kq = Learnset.chuanHoa(sk, 9, 4, 0, 10, 10, 6, false, true);
        Kiem.dung("vuot ngan sach diem -> chieu da hoc ve cap 1", kq.vuotNganSach && sk[0] == 1 && kq.diemKyNang == 9);
        sk = new byte[]{9, 9, 9, 0, 0, 0, -1, -1, -1};
        kq = Learnset.chuanHoa(sk, 9, 4, 0, 10, 10, 6, true, true);
        Kiem.dung("admin khong bi ha cap chieu", !kq.vuotNganSach && sk[0] == 9);
        // Eevee cap 20 da cong o 6/7 (cu: cap 3/4) -> go (cap 25); cap 30 -> giu
        sk = new byte[]{2, 1, 1, 1, 1, 1, 3, 2, -1, -1, -1};
        kq = Learnset.chuanHoa(sk, 11, 133, 2, 20, 20, 8, false, true);
        Kiem.dung("Eevee 20: Phuoc lanh / Chia se no dau ve 0 (tu hoc lai o cap 25)", sk[6] == 0 && sk[7] == 0 && kq.hoanNpc.isEmpty());
        sk = new byte[]{2, 1, 1, 1, 1, 1, 3, 2, 0, 0, -1};
        kq = Learnset.chuanHoa(sk, 11, 134, 2, 30, 30, 8, false, true);
        Kiem.dung("Vaporeon 30: giu o 6/7, Bứt phá da mua (cap 0) -> cap 1, o NPC dau (8) cap 0 = chua mua -> -1",
                sk[6] == 3 && sk[7] == 2 && sk[9] == 1 && sk[8] == -1 && kq.go.isEmpty() && kq.hoanNpc.isEmpty());
        // o NPC dau (chuoi tao nhan vat cu lech: ai cung co 0 o day, luat cu khong cho mua) - khong bao gio hoan xu
        sk = new byte[]{1, 1, 1, 0, 0, 1, 0, -1, -1};
        kq = Learnset.chuanHoa(sk, 9, 4, 0, 10, 10, 6, false, true);
        Kiem.dung("Charmander moi cu: o 6 = 0 (loi chuoi) -> -1, KHONG hoan xu", sk[6] == -1 && kq.hoanNpc.isEmpty() && kq.go.isEmpty());
        sk = new byte[]{1, 1, 1, 1, 1, 1, 0, -1, -1};
        kq = Learnset.chuanHoa(sk, 9, 6, 0, 50, 50, 6, false, true);
        Kiem.dung("Charizard 50 chua mua Sung phun lua (o 6 = 0) -> phai hoc o NPC (-1), khong duoc mien phi", sk[6] == -1);
        sk = new byte[]{1, 1, 1, 1, 1, 1, 3, -1, -1};
        kq = Learnset.chuanHoa(sk, 9, 6, 0, 50, 50, 6, false, true);
        Kiem.dung("Charizard 50 da cong diem o 6 (khong qua NPC): giu cap 3", sk[6] == 3 && kq.go.isEmpty());
        sk = new byte[]{1, 1, 1, 1, 1, 1, 2, -1, -1};
        kq = Learnset.chuanHoa(sk, 9, 5, 0, 30, 30, 6, false, true);
        Kiem.dung("Charmeleon 30 cong diem o 6: go (-1), hoan diem, KHONG hoan xu", sk[6] == -1 && kq.go.contains(6)
                && kq.hoanNpc.isEmpty() && kq.diemKyNang == 29);
        // nhan vat moi: cap 1 chi co o 0
        sk = new byte[]{0, 0, 0, 0, 0, 0, -1, -1, -1};
        kq = Learnset.chuanHoa(sk, 9, 4, 0, 1, 1, 6, false, true);
        Kiem.bang("Charmander moi: tu hoc Cao cap 1", "[1, 0, 0, 0, 0, 0, -1, -1, -1]", Arrays.toString(sk));
        // moi nhan vat ngau nhien: diem ky nang sau >= diem cu (khong ai mat diem), chay lai khong doi
        final java.util.Random rd2 = new java.util.Random(11);
        int matDiem = 0, khongIdem = 0;
        final int[][] loaiDong = {{4, 0}, {5, 0}, {6, 0}, {1, 1}, {3, 1}, {133, 2}, {136, 2}, {7, 3}, {9, 3}, {172, 4}, {26, 4}};
        for (int lan = 0; lan < 2000; lan++) {
            final int[] ld = loaiDong[rd2.nextInt(loaiDong.length)];
            final int cap = Math.max(LoaiPokemon.laNhanhEevee(ld[0]) ? 25 : 1, 1 + rd2.nextInt(100));
            final int so = ChieuPokemon.soChieu(ld[1]);
            final byte[] s = new byte[15];
            Arrays.fill(s, (byte) -1);
            int con = cap - 1;
            for (int o = 0; o < so; o++) {
                final boolean npc = Learnset.laNpc(ld[1], o);
                if (npc) {
                    s[o] = (byte) (rd2.nextInt(3) == 0 ? 0 : -1);
                } else {
                    s[o] = 0;
                }
            }
            // luat cu: o 0 mien phi cap 1; hoc = 1 diem; cong toi 9
            s[0] = 1;
            for (int b = 0; b < 40 && con > 0; b++) {
                final int o = rd2.nextInt(so);
                if (s[o] >= 0 && s[o] < 9) {
                    s[o]++;
                    con--;
                }
            }
            final int diemCu = con;
            final byte[] s1 = s.clone();
            final Learnset.KetQua k1 = Learnset.chuanHoa(s1, so, ld[0], ld[1], cap, cap, oLoi(ld[1]), false, true);
            if (k1.diemKyNang < diemCu) {
                matDiem++;
            }
            final byte[] s2 = s1.clone();
            final Learnset.KetQua k2 = Learnset.chuanHoa(s2, so, ld[0], ld[1], cap, cap, oLoi(ld[1]), false, true);
            if (!Arrays.equals(s1, s2) || k2.diemKyNang != k1.diemKyNang || !k2.go.isEmpty()) {
                khongIdem++;
            }
        }
        Kiem.bang("chuyen doi chieu 2000 nhan vat: khong ai mat diem ky nang", 0, matDiem);
        Kiem.bang("chuyen doi chieu idempotent (lan 2 khong go, khong doi)", 0, khongIdem);
    }

    // ------------------------------------------------------------------ noi vao game (ma nguon)

    static void kiemMaNguon() throws Exception {
        final String g = doc("src/real/HocChieuGame.java");
        final String db = doc("src/data/DatabaseHocChieu.java");
        Kiem.dung("chuyen doi diem tiem nang: dau + trang thai trong MOT giao dich (INSERT IGNORE + commit)",
                db.contains("INSERT IGNORE INTO tob_chuyen_doi_can_bang") && db.contains("conn.commit()") && db.contains("conn.rollback()"));
        Kiem.dung("hoan xu: luu nhan vat TRUOC khi danh dau da_hoan", g.indexOf("saveCharAuto(p);") < g.indexOf("\"da_hoan\")")
                && g.indexOf("saveCharAuto(p);") > 0);
        Kiem.dung("goi -120 chi gui client >= 907", g.contains("BAN = 907") && g.contains("banClient >= BAN"));
        final String xl = doc("src/real/MapXuLyLenh.java");
        Kiem.bang("hai khoi len cap dung HocChieuGame.congLenCap", 2, dem(xl, "HocChieuGame.congLenCap(p, newlevel)"));
        Kiem.sai("MapXuLyLenh khong con tu cong 5 diem tiem nang", xl.contains("p.basepoint + 5 * (newlevel - p.lastLV)"));
        final String cs = doc("src/real/CharChiSo.java");
        Kiem.dung("CharChiSo ap Diem huan luyen HP / PP / Tan cong / Phong thu",
                cs.contains("HuanLuyen.HP") && cs.contains("HuanLuyen.PP") && cs.contains("HuanLuyen.TAN_CONG") && cs.contains("HuanLuyen.PHONG_THU"));
        final String danhMuc = doc("../tools/cauhinh/danh_muc.json");
        for (final String k : new String[]{real.HocChieuGame.K_CHIEU, real.HocChieuGame.K_HUAN_LUYEN, real.HocChieuGame.K_DIEM_MOI_CAP,
            real.HocChieuGame.K_TRAN_THUONG, real.HocChieuGame.K_GIA_TAY, real.HocChieuGame.K_HOI_TAY}) {
            Kiem.dung("khoa cau hinh " + k + " co trong danh_muc.json", danhMuc.contains("\"khoa\": \"" + k + "\""));
        }
        final String nv = doc("src/data/DatabaseNhanVat.java");
        Kiem.sai("chuoi chieu tao nhan vat khong con lech mot o (mainSkill[k - 7])", nv.contains("mainSkill[k - 7]"));
        Kiem.dung("idSkill[dong][0] la o NPC dau (6 / Eevee 8) - Learnset.chuanHoa oLoi",
                Arrays.deepToString(idSkill()).equals("[[6, 7, 8], [6, 7, 8], [8, 9, 10], [6, 7, 8], [6, 7, 8]]"));
        Kiem.bang("so lenh -120 khong trung lenh khac cua server", 1, dem(doc("src/real/MapXuLyLenh.java"), "case HocChieuGame.LENH"));
    }

    /** Mot dong CSV (co ngoac kep). */
    static String[] csv(final String l) {
        final List<String> ra = new ArrayList<String>();
        final StringBuilder cur = new StringBuilder();
        boolean q = false;
        for (int i = 0; i < l.length(); i++) {
            final char c = l.charAt(i);
            if (c == '"') {
                q = !q;
            } else if (c == ',' && !q) {
                ra.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        ra.add(cur.toString());
        return ra.toArray(new String[0]);
    }

    /** O NPC dau cua dong (Map.idSkill[dong][0]): 8 cho Eevee, 6 cho dong khac. */
    static int oLoi(final int dong) {
        return dong == 2 ? 8 : 6;
    }

    static int dem(final String s, final String k) {
        int n = 0;
        for (int i = s.indexOf(k); i >= 0; i = s.indexOf(k, i + 1)) {
            n++;
        }
        return n;
    }
}
