import com.htbb.pokemon.ChuPokemon;
import com.htbb.pokemon.ThongSoLauDai;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Thiet ke dai han (docs/pokemon/THIET_KE_LAU_DAI.md): du lieu sinh ra NHAT QUAN va mo
 * phong kinh te DAT muc tieu.
 *
 * VI SAO: bang nhiem vu (nhiem_vu_moi.csv), lich su kien, ThongSoLauDai.java va
 * sql/pokemon_lau_dai.sql deu sinh tu tools/pokemon/sinh_sql_lau_dai.py; mo phong
 * (tools/pokemon/MoPhongKinhTe.java) doc lai chung. Sua mot so trong script ma quen chay
 * lai, hoac chinh so lam vo can bang (lam phat, nguoi choi nhe xong cot truyen qua som)
 * -> test nay do. Chi doc file, khong can CSDL.
 */
public final class KiemLauDai {

    private static final String F_NV = "docs/pokemon/nhiem_vu_moi.csv";
    private static final String F_SK = "docs/pokemon/su_kien_nam.csv";
    private static final String F_SQL = "sql/pokemon_lau_dai.sql";
    private static final String F_DUMP = "sql/htbb_day_du.sql";
    private static final String F_XUAT = "tools/pokemon/xuat_sql_day_du.sh";
    private static final String F_MO_PHONG = "tools/pokemon/MoPhongKinhTe.java";

    private KiemLauDai() {
    }

    public static void chay() {
        Kiem.nhom("Thiet ke lau dai: bang EXP, nang cap, nhiem vu, su kien, mo phong kinh te");
        try {
            kiemThongSo();
            final List<Map<String, String>> nv = docCsv(F_NV);
            kiemNhiemVu(nv);
            kiemSuKien(docCsv(F_SK));
            kiemSql(nv);
            kiemMoPhong();
        } catch (final Exception e) {
            Kiem.dung("khong nem loi: " + e, false);
        }
    }

    // ------------------------------------------------------------------ thong so
    private static void kiemThongSo() {
        boolean tang = true;
        for (int L = 2; L < ThongSoLauDai.CAP_TOI_DA; L++) {
            tang &= ThongSoLauDai.EXP_LEN_CAP[L] > ThongSoLauDai.EXP_LEN_CAP[L - 1];
        }
        Kiem.dung("EXP_LEN_CAP tang nghiem ngat 1..99", tang);
        Kiem.bang("EXP_LEN_CAP[100] = 0 (tran)", 0L, ThongSoLauDai.EXP_LEN_CAP[ThongSoLauDai.CAP_TOI_DA]);
        long tong = 0;
        for (int L = 1; L < 91; L++) {
            tong += ThongSoLauDai.EXP_LEN_CAP[L];
        }
        Kiem.dung("tong EXP 1->91 < 2^31 x 10 (quest exp int van du) va << bang cu 7,5e13", tong < 21_474_836_470L);
        Kiem.dung("EXP 1 lan len cap <= Integer.MAX (data_quest.exp doc bang getInt)",
                ThongSoLauDai.EXP_LEN_CAP[99] < Integer.MAX_VALUE);
        boolean tranTang = true;
        for (int i = 1; i < ThongSoLauDai.TRAN_CAP_NGAY.length; i++) {
            tranTang &= ThongSoLauDai.TRAN_CAP_NGAY[i] > ThongSoLauDai.TRAN_CAP_NGAY[i - 1]
                    && ThongSoLauDai.TRAN_CAP_GIA_TRI[i] > ThongSoLauDai.TRAN_CAP_GIA_TRI[i - 1];
        }
        Kiem.dung("lich tran cap tang dan", tranTang);
        Kiem.bang("tran cap ngay 0", 60, ThongSoLauDai.tranCap(0));
        Kiem.bang("tran cap ngay 240 = 91 (cap game cu)", 91, ThongSoLauDai.tranCap(240));
        Kiem.bang("tran cap ngay 729", 100, ThongSoLauDai.tranCap(729));
        boolean giam = true;
        for (int k = 1; k < 15; k++) {
            giam &= ThongSoLauDai.TI_LE_NC[k] <= ThongSoLauDai.TI_LE_NC[k - 1];
        }
        Kiem.dung("ti le nang cap khong tang theo +k", giam);
        Kiem.bang("pity: +15 sau 0 lan truot", 10, ThongSoLauDai.tiLeNangCap(15, 0));
        Kiem.bang("pity: +15 sau 3 lan truot", 22, ThongSoLauDai.tiLeNangCap(15, 3));
        Kiem.bang("pity: tran 100%", 100, ThongSoLauDai.tiLeNangCap(10, 50));
        boolean chiPhi = true;
        for (int b = 0; b < ThongSoLauDai.CHI_PHI_NC.length; b++) {
            for (int k = 2; k <= 15; k++) {
                chiPhi &= ThongSoLauDai.CHI_PHI_NC[b][k] > ThongSoLauDai.CHI_PHI_NC[b][k - 1];
            }
            if (b > 0) {
                chiPhi &= ThongSoLauDai.CHI_PHI_NC[b][1] > ThongSoLauDai.CHI_PHI_NC[b - 1][1];
            }
        }
        Kiem.dung("chi phi nang cap tang theo +k va theo bac (ho hut lon dan)", chiPhi);
        Kiem.bang("bac met: 3 gio 100% roi 50%", 50, ThongSoLauDai.heSoMet(0));
        Kiem.bang("bac met: sau 7 gio vuot -> 0 (chong bot)", 0, ThongSoLauDai.heSoMet(7 * 60));
    }

    // ------------------------------------------------------------------ nhiem vu
    private static void kiemNhiemVu(final List<Map<String, String>> nv) throws Exception {
        final Set<String> ma = new HashSet<String>();
        final Set<String> idSo = new HashSet<String>();
        boolean trungMa = false, trungId = false, truocThieu = false, tuCam = false, tenDai = false, vuot = false;
        final Set<String> quai = idQuai();
        boolean quaiThieu = false;
        String loiDau = "";
        int phuNgay = 0, chuongNam1 = 0, huyHieu = 0, nam2 = 0;
        final Set<String> chuong1 = new HashSet<String>();
        for (final Map<String, String> d : nv) {
            final String m = d.get("ma");
            if (!ma.add(m)) {
                trungMa = true;
                loiDau = loiDau.isEmpty() ? "trung ma " + m : loiDau;
            }
            if (!idSo.add(d.get("id_so"))) {
                trungId = true;
            }
            final String truoc = d.get("truoc");
            if (!truoc.isEmpty() && !ma.contains(truoc)) {
                truocThieu = true;
                loiDau = loiDau.isEmpty() ? m + " truoc " + truoc + " chua co (hoac dung sau)" : loiDau;
            }
            if (!ChuPokemon.tuCamTrong(d.get("ten")).isEmpty() || !ChuPokemon.tuCamTrong(d.get("muc_tieu_ten")).isEmpty()) {
                tuCam = true;
                loiDau = loiDau.isEmpty() ? m + " co tu cam: " + d.get("ten") : loiDau;
            }
            if (d.get("ten").length() > 45) {
                tenDai = true;
            }
            final String kieu = d.get("kieu_muc_tieu");
            if (("giet".equals(kieu) || "giet_tinh_anh".equals(kieu)) && !quai.contains(d.get("muc_tieu_id"))) {
                quaiThieu = true;
            }
            if ("thu_thap".equals(kieu) && !quai.contains(d.get("quai_roi"))) {
                quaiThieu = true;
            }
            final String loi = vuotNganSach(d);
            if (loi != null) {
                vuot = true;
                loiDau = loiDau.isEmpty() ? loi : loiDau;
            }
            final String loai = d.get("loai");
            if (!"chinh".equals(loai) && !"huong_dan".equals(loai) && !"thanh_tuu".equals(loai)) {
                phuNgay++;
            }
            if ("chinh".equals(loai) && "1".equals(d.get("nam"))) {
                chuong1.add(d.get("chuong"));
                if (!d.get("huy_hieu").isEmpty()) {
                    huyHieu++;
                }
            }
            if ("chinh".equals(loai) && "2".equals(d.get("nam"))) {
                nam2++;
            }
        }
        chuongNam1 = chuong1.size();
        Kiem.dung("ma nhiem vu duy nhat " + (trungMa ? loiDau : ""), !trungMa);
        Kiem.dung("id_so duy nhat", !trungId);
        Kiem.dung("dieu kien truoc ton tai va dung truoc " + (truocThieu ? loiDau : ""), !truocThieu);
        Kiem.dung("ten nhiem vu khong co tu cam " + (tuCam ? loiDau : ""), !tuCam);
        Kiem.dung("ten <= 45 ky tu (data_quest.name varchar(45))", !tenDai);
        Kiem.dung("quai muc tieu/quai roi co trong data_monster", !quaiThieu);
        Kiem.dung("thuong trong ngan sach " + (vuot ? loiDau : ""), !vuot);
        Kiem.bang("cot truyen nam 1 = 52 chuong", 52, chuongNam1);
        Kiem.bang("16 huy hieu Gym (8 Kanto + 8 Johto) la moc cot truyen", 16, huyHieu);
        Kiem.dung("dan y nam 2 >= 40 chuong", nam2 >= 40);
        Kiem.dung(">= 150 nhiem vu phu/ngay/tuan/Pokedex (" + phuNgay + ")", phuNgay >= 150);
    }

    /** null neu trong ngan sach; nguoc lai mo ta loi. Quy tac giong sinh_sql_lau_dai.py. */
    private static String vuotNganSach(final Map<String, String> d) {
        final String loai = d.get("loai");
        int i = -1;
        for (int k = 0; k < ThongSoLauDai.NGAN_SACH_LOAI.length; k++) {
            if (ThongSoLauDai.NGAN_SACH_LOAI[k].equals(loai)) {
                i = k;
            }
        }
        if (i < 0) {
            return d.get("ma") + ": loai la " + loai;
        }
        int[] ns = ThongSoLauDai.NGAN_SACH[i];
        if ("chinh".equals(loai)) {
            ns = ThongSoLauDai.NGAN_SACH[java.util.Arrays.asList(ThongSoLauDai.NGAN_SACH_LOAI).indexOf("chinh_moc")];
        }
        final int L = Math.max(1, Math.min(ThongSoLauDai.CAP_TOI_DA, Integer.parseInt(d.get("cap_toi_thieu"))));
        double he = 1.0, heLk = 1.0;
        if (L < ThongSoLauDai.CAP_TAN_THU && ("huong_dan".equals(loai) || "chinh".equals(loai) || "phu".equals(loai)
                || "tuyen".equals(loai))) {
            he = ThongSoLauDai.HE_SO_TAN_THU_PHAN_TRAM / 100.0;
        }
        if ("pokedex".equals(loai)) {
            he = 2.0;
            heLk = 3.0;
        }
        if ("chinh".equals(loai) && "2".equals(d.get("nam"))) {
            he = 4.0;
            heLk = 4.0;
        }
        final double expMax = ns[0] / 1000.0 * he * ThongSoLauDai.expMoiGio(L) * 1.001 + 1;
        final double xuMax = ns[1] / 1000.0 * he * ThongSoLauDai.xuMoiGio(L) * 1.001 + 1;
        if (Long.parseLong(d.get("exp")) > expMax) {
            return d.get("ma") + ": exp " + d.get("exp") + " > " + (long) expMax;
        }
        if (Long.parseLong(d.get("xu")) > xuMax) {
            return d.get("ma") + ": xu " + d.get("xu") + " > " + (long) xuMax;
        }
        if (Integer.parseInt(d.get("luong_khoa")) > ns[2] * heLk) {
            return d.get("ma") + ": luong khoa " + d.get("luong_khoa");
        }
        if (Integer.parseInt(d.get("luong")) > ns[3]) {
            return d.get("ma") + ": luong " + d.get("luong");
        }
        return null;
    }

    private static Set<String> idQuai() throws Exception {
        final String s = new String(Files.readAllBytes(Paths.get(F_DUMP)), StandardCharsets.UTF_8);
        final int i = s.indexOf("INSERT INTO `data_monster` VALUES");
        final int j = s.indexOf(";\n", i);
        final Set<String> ra = new HashSet<String>();
        final Matcher m = Pattern.compile("\\((\\d+),'").matcher(s.substring(i, j));
        while (m.find()) {
            ra.add(m.group(1));
        }
        return ra;
    }

    // ------------------------------------------------------------------ su kien
    private static void kiemSuKien(final List<Map<String, String>> sk) {
        Kiem.bang("lich su kien 52 tuan", 52, sk.size());
        final Set<String> tuan = new HashSet<String>();
        double tong = 0, max = 0;
        boolean coTen2 = true;
        for (final Map<String, String> d : sk) {
            tuan.add(d.get("tuan"));
            final double g = Double.parseDouble(d.get("ngan_sach_gio"));
            tong += g;
            max = Math.max(max, g);
            coTen2 &= !d.get("ten_nam2").isEmpty();
        }
        Kiem.bang("tuan duy nhat 1..52", 52, tuan.size());
        Kiem.dung("ngan sach su kien/tuan <= 3 gio thu nhap (" + max + ")", max <= 3.0);
        Kiem.dung("ngan sach su kien/nam <= 70 gio thu nhap (" + tong + ")", tong <= 70.0);
        Kiem.dung("moi tuan co ban xoay vong nam 2", coTen2);
        final String all = sk.toString();
        for (final String le : new String[]{"Tết Nguyên Đán", "Valentine", "8/3", "30/4", "Hè", "Trung Thu", "20/10",
            "Halloween", "Giáng sinh", "Ngày cộng đồng", "Shiny", "Đột kích", "Tuần hệ", "Pokédex", "Thế chiến vùng"}) {
            Kiem.dung("lich co: " + le, all.contains(le));
        }
    }

    // ------------------------------------------------------------------ sql
    private static void kiemSql(final List<Map<String, String>> nv) throws Exception {
        final String s = new String(Files.readAllBytes(Paths.get(F_SQL)), StandardCharsets.UTF_8);
        int tuongThich = 0;
        for (final Map<String, String> d : nv) {
            if ("1".equals(d.get("dong_co_tuong_thich")) && "1".equals(d.get("nam"))) {
                tuongThich++;
            }
        }
        final Matcher m = Pattern.compile("INSERT INTO `data_quest_moi`[^\\n]*\\n((?:\\(.*\\)[,;]\\n)+)").matcher(s);
        int dong = 0;
        while (m.find()) {
            for (final String l : m.group(1).split("\n")) {
                if (l.startsWith("(")) {
                    dong++;
                }
            }
        }
        Kiem.bang("data_quest_moi co dung so dong tuong thich", tuongThich, dong);
        Kiem.dung("SQL bat dau bang SET NAMES utf8", s.contains("SET NAMES utf8;"));
        Kiem.sai("SQL KHONG sua bang dang chay (UPDATE/INSERT data_quest that)",
                Pattern.compile("(?m)^(UPDATE|INSERT INTO `data_quest` |DELETE)").matcher(s).find());
        final String xuat = new String(Files.readAllBytes(Paths.get(F_XUAT)), StandardCharsets.UTF_8);
        Kiem.sai("pokemon_lau_dai CHUA nam trong xuat_sql_day_du.sh", xuat.contains("pokemon_lau_dai"));
    }

    // ------------------------------------------------------------------ mo phong
    private static void kiemMoPhong() throws Exception {
        final javax.tools.JavaCompiler jc = javax.tools.ToolProvider.getSystemJavaCompiler();
        if (jc == null) {
            System.out.println("   (bo qua mo phong: JVM khong co javac)");
            return;
        }
        final File ra = Files.createTempDirectory("mophong").toFile();
        final int kq = jc.run(null, null, null, "-encoding", "UTF-8", "-nowarn", "-cp",
                System.getProperty("java.class.path"), "-d", ra.getPath(), F_MO_PHONG);
        Kiem.bang("bien dich MoPhongKinhTe", 0, kq);
        if (kq != 0) {
            return;
        }
        try (URLClassLoader cl = new URLClassLoader(new URL[]{ra.toURI().toURL()}, KiemLauDai.class.getClassLoader())) {
            final Method m = cl.loadClass("MoPhongKinhTe").getMethod("chayKiem", String.class, int.class, int.class);
            final String[][] r = (String[][]) m.invoke(null, ".", 730, 25);
            Kiem.dung("mo phong co >= 10 muc tieu", r.length >= 10);
            for (final String[] x : r) {
                Kiem.bang("mo phong: " + x[0] + " (" + x[2] + ")", "1", x[1]);
            }
            final String[][] r2 = (String[][]) m.invoke(null, ".", 730, 25);
            boolean giong = r.length == r2.length;
            for (int i = 0; giong && i < r.length; i++) {
                giong = r[i][2].equals(r2[i][2]);
            }
            Kiem.dung("mo phong tat dinh (chay 2 lan ra y het)", giong);
        }
    }

    // ------------------------------------------------------------------ csv
    static List<Map<String, String>> docCsv(final String f) throws Exception {
        final List<String> dong = Files.readAllLines(Paths.get(f), StandardCharsets.UTF_8);
        final List<Map<String, String>> ra = new ArrayList<Map<String, String>>();
        final List<String> cot = tach(dong.get(0));
        for (int i = 1; i < dong.size(); i++) {
            if (dong.get(i).isEmpty()) {
                continue;
            }
            final List<String> o = tach(dong.get(i));
            final Map<String, String> m = new HashMap<String, String>();
            for (int k = 0; k < cot.size(); k++) {
                m.put(cot.get(k), k < o.size() ? o.get(k) : "");
            }
            ra.add(m);
        }
        return ra;
    }

    private static List<String> tach(final String s) {
        final List<String> ra = new ArrayList<String>();
        final StringBuilder b = new StringBuilder();
        boolean q = false;
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (q) {
                if (c == '"') {
                    if (i + 1 < s.length() && s.charAt(i + 1) == '"') {
                        b.append('"');
                        i++;
                    } else {
                        q = false;
                    }
                } else {
                    b.append(c);
                }
            } else if (c == '"') {
                q = true;
            } else if (c == ',') {
                ra.add(b.toString());
                b.setLength(0);
            } else {
                b.append(c);
            }
        }
        ra.add(b.toString());
        return ra;
    }
}
