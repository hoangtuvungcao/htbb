import com.htbb.pokemon.ChuPokemon;
import com.htbb.pokemon.ThongSoLauDai;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Noi dung day du 1-2 nam (docs/thiet-ke-game.md): trang bi 11 bac, vat pham,
 * chieu, quai/Gym/boss, bang roi, su kien, trang tri, cua hang, che tao.
 *
 * VI SAO: tat ca sinh tu tools/pokemon/sinh_noi_dung.py (doc so tu sinh_sql_lau_dai.py) ra
 * du_lieu/sql_nguon/pokemon_noi_dung.sql + du_lieu/noi_dung_*.csv + du_lieu/web/*.csv. Sua so
 * trong script ma quen chay lai, hoac chinh so pha ngan sach (bac moi manh hon 1,1 x bac cu
 * +15, gia ngoai tam thu nhap, do Hoan my roi tu quai thuong, chieu lech TTK...) -> test do.
 * Chi doc file, khong can CSDL.
 */
public final class KiemNoiDung {

    private static final String D = "../du_lieu/";
    private static final String F_SQL = "../du_lieu/sql_nguon/pokemon_noi_dung.sql";
    private static final String F_DUMP = "../sql/htbb_day_du.sql";
    private static final int ID_TU = 1001;
    private static final int ID_DEN = 1199;
    private static final int SO_THUOC = 162;
    private static final int SO_NGOC = 67;

    private KiemNoiDung() {
    }

    public static void chay() {
        Kiem.nhom("Noi dung day du: trang bi, vat pham, chieu, quai, roi, su kien, cua hang");
        try {
            final String dump = doc(F_DUMP);
            // Dump co the da sinh lai CO dai noi dung (1001..1199) -> "cu" = ngoai dai do.
            final Set<Integer> itemCu = new HashSet<Integer>();
            for (final Integer i : idTrongDump(dump, "data_item")) {
                if (i.intValue() < ID_TU || i.intValue() > ID_DEN) {
                    itemCu.add(i);
                }
            }
            final Set<Integer> thuoc = idTrongDump(dump, "data_potion");
            final List<Map<String, String>> moi = csv(D + "noi_dung_item_moi.csv");
            final Set<Integer> itemMoi = new HashSet<Integer>();
            for (final Map<String, String> r : moi) {
                itemMoi.add(Integer.valueOf(r.get("id")));
            }
            kiemIdMoi(itemCu, moi, itemMoi);
            final Set<Integer> item = new HashSet<Integer>(itemCu);
            item.addAll(itemMoi);
            kiemThamChieu(item, thuoc);
            kiemTrangBi(itemCu, dump, moi);
            kiemVatPham();
            kiemChieu();
            kiemQuai();
            kiemQuaiMoi(dump);
            kiemIcon(moi);
            kiemRoi();
            kiemSuKienTrangTri();
            kiemTen(moi);
            kiemWeb();
            kiemSql(moi);
        } catch (final Exception e) {
            Kiem.dung("khong nem loi: " + e, false);
        }
    }

    // ------------------------------------------------------------------ id
    private static void kiemIdMoi(final Set<Integer> itemCu, final List<Map<String, String>> moi,
                                  final Set<Integer> itemMoi) {
        Kiem.bang("item moi khong trung id", Integer.valueOf(moi.size()), Integer.valueOf(itemMoi.size()));
        boolean trongDai = true;
        boolean khongDe = true;
        for (final Integer i : itemMoi) {
            trongDai &= i.intValue() >= ID_TU && i.intValue() <= ID_DEN;
            khongDe &= !itemCu.contains(i);
        }
        Kiem.dung("item moi nam trong dai danh rieng " + ID_TU + ".." + ID_DEN, trongDai);
        Kiem.dung("item moi khong de len data_item co san", khongDe);
        Kiem.bang("data_potion giu nguyen " + SO_THUOC + " dong (Char.SO_O_THUOC)", Integer.valueOf(SO_THUOC),
                Integer.valueOf(idTrongDump(docKhongLoi(F_DUMP), "data_potion").size()));
        // Khoa chinh moi CSV noi bo khong trung
        final String[][] khoa = {{"trang_bi", "ma"}, {"bo", "bac,so_mon"}, {"nguyen_lieu", "ma"},
            {"vat_lieu_nang_cap", "bac,cong"}, {"ngoc_he", "he,cap"}, {"vat_pham", "ma"}, {"chieu", "dong,so_hieu"},
            {"quai_dai_cap", "dai"}, {"quai_tinh_anh", "id_goc"}, {"gym", "stt"}, {"boss_the_gioi", "tuan"},
            {"roi", "stt"}, {"su_kien_vat_pham", "stt"}, {"trang_tri", "stt"}, {"cua_hang", "stt"},
            {"che_tao", "thanh_pham"}};
        final List<String> trung = new ArrayList<String>();
        for (final String[] k : khoa) {
            final Set<String> da = new HashSet<String>();
            for (final Map<String, String> r : csvKhongLoi(D + "noi_dung_" + k[0] + ".csv")) {
                final StringBuilder sb = new StringBuilder();
                for (final String c : k[1].split(",")) {
                    sb.append(r.get(c)).append('|');
                }
                if (!da.add(sb.toString())) {
                    trung.add(k[0] + ":" + sb);
                }
            }
        }
        Kiem.bang("khoa chinh cac bang noi dung khong trung", "[]", trung.toString());
    }

    // ------------------------------------------------------------------ tham chieu
    private static final Pattern THAM_CHIEU = Pattern.compile("\\b(item|thuoc|ngoc|moi):([A-Za-z0-9_/]+)");

    private static void kiemThamChieu(final Set<Integer> item, final Set<Integer> thuoc) throws Exception {
        final Set<String> maMoi = new HashSet<String>();
        for (final Map<String, String> r : csv(D + "noi_dung_vat_pham.csv")) {
            if ("can_code".equals(r.get("trang_thai"))) {
                maMoi.add(r.get("ma"));
            }
        }
        maMoi.add("trang_tri");
        final String[] tep = {"roi", "cua_hang", "che_tao", "vat_pham", "su_kien_vat_pham", "boss_the_gioi",
            "nguyen_lieu", "gym", "trang_bi"};
        final List<String> loi = new ArrayList<String>();
        int dem = 0;
        for (final String t : tep) {
            for (final Map<String, String> r : csv(D + "noi_dung_" + t + ".csv")) {
                for (final String v : r.values()) {
                    final Matcher m = THAM_CHIEU.matcher(v);
                    while (m.find()) {
                        dem++;
                        final String loai = m.group(1);
                        for (final String x : m.group(2).split("/")) {
                            if ("moi".equals(loai)) {
                                if (!maMoi.contains(x)) {
                                    loi.add(t + " moi:" + x);
                                }
                                continue;
                            }
                            final int id = Integer.parseInt(x);
                            final boolean ok = "item".equals(loai) ? item.contains(Integer.valueOf(id))
                                    : "thuoc".equals(loai) ? thuoc.contains(Integer.valueOf(id)) && id < SO_THUOC
                                    : id >= 0 && id < SO_NGOC;
                            if (!ok) {
                                loi.add(t + " " + loai + ":" + id);
                            }
                        }
                    }
                }
            }
        }
        // trang_bi.id_item phai ton tai
        for (final Map<String, String> r : csv(D + "noi_dung_trang_bi.csv")) {
            if (!item.contains(Integer.valueOf(r.get("id_item")))) {
                loi.add("trang_bi " + r.get("ma") + " id_item " + r.get("id_item"));
            }
        }
        // vat pham thuong nhiem vu (nhiem_vu_moi.csv) phai ton tai
        int nv = 0;
        for (final Map<String, String> r : csv(D + "nhiem_vu_moi.csv")) {
            final String v = r.get("vat_pham");
            if (v != null && !v.isEmpty()) {
                nv++;
                if (!item.contains(Integer.valueOf(v))) {
                    loi.add("nhiem_vu " + r.get("ma") + " vat_pham " + v);
                }
            }
        }
        Kiem.dung("co tham chieu vat pham trong roi/cua hang/che tao/su kien (" + dem + ")", dem > 500);
        Kiem.dung("nhiem vu co thuong vat pham (" + nv + ")", nv > 50);
        Kiem.bang("moi vat pham duoc roi/ban/che tao/thuong/nhiem vu tro toi deu ton tai", "[]", loi.toString());
    }

    // ------------------------------------------------------------------ trang bi
    private static void kiemTrangBi(final Set<Integer> itemCu, final String dump,
                                    final List<Map<String, String>> moi) throws Exception {
        final List<Map<String, String>> tb = csv(D + "noi_dung_trang_bi.csv");
        final Map<Integer, Map<String, String>> capDo = theoCap();
        final int soBac = ThongSoLauDai.BAC_TB_CAP.length;
        Kiem.bang("11 bac trang bi", Integer.valueOf(11), Integer.valueOf(soBac));
        // moi bac du 11 o (huy hieu x2, 1 vu khi moi dong)
        final List<String> loi = new ArrayList<String>();
        final int[] tc = new int[soBac];
        final int[] thu = new int[soBac];
        final int[] hp = new int[soBac];
        final Map<String, Integer> truoc = new HashMap<String, Integer>();
        for (int b = 0; b < soBac; b++) {
            int soO = 0;
            final Set<String> loai = new HashSet<String>();
            for (final Map<String, String> r : tb) {
                if (Integer.parseInt(r.get("bac")) != b) {
                    continue;
                }
                loai.add(r.get("o"));
                final boolean vk = "vu_khi".equals(r.get("o"));
                if (!vk || "Charmander".equals(r.get("dong"))) {
                    final int n = Integer.parseInt(r.get("so_o"));
                    soO += n;
                    tc[b] += n * Integer.parseInt(r.get("tan_cong"));
                    thu[b] += n * Integer.parseInt(r.get("phong_thu"));
                    hp[b] += n * Integer.parseInt(r.get("hp"));
                }
                // cap yeu cau = cap bac; mo dung ngay, tran mua luc mo >= cap
                if (Integer.parseInt(r.get("cap_yeu_cau")) != ThongSoLauDai.BAC_TB_CAP[b]
                        || Integer.parseInt(r.get("ngay_mo")) != ThongSoLauDai.BAC_TB_NGAY_MO[b]
                        || tranCap(ThongSoLauDai.BAC_TB_NGAY_MO[b]) < ThongSoLauDai.BAC_TB_CAP[b]) {
                    loi.add("cap/ngay " + r.get("ma"));
                }
                if (Long.parseLong(r.get("gia_xu")) != ThongSoLauDai.BAC_TB_GIA[b]) {
                    loi.add("gia " + r.get("ma"));
                }
                // gia = 1,5 gio thu nhap o cap cua bac (+-1%)
                final double xg = Double.parseDouble(capDo.get(ThongSoLauDai.BAC_TB_CAP[b]).get("xu_moi_gio"));
                final double gio = Long.parseLong(r.get("gia_xu")) / xg;
                if (gio < 1.48 || gio > 1.52) {
                    loi.add("gia ngoai 1,5 gio " + r.get("ma") + " " + gio);
                }
                final String k = r.get("o") + "|" + r.get("dong");
                final int v = Integer.parseInt(r.get("tan_cong")) + Integer.parseInt(r.get("phong_thu"))
                        + Integer.parseInt(r.get("hp"));
                if (truoc.containsKey(k) && v < truoc.get(k).intValue()) {
                    loi.add("chi so giam " + r.get("ma"));
                }
                truoc.put(k, Integer.valueOf(v));
            }
            if (soO != ThongSoLauDai.SO_O_TRANG_BI || loai.size() != 10) {
                loi.add("bac " + b + " so o " + soO + " loai " + loai.size());
            }
        }
        Kiem.bang("moi bac du 11 o, cap/ngay/gia dung thiet ke, chi so khong giam", "[]", loi.toString());
        // Ngan sach mua: bac moi (+0) <= 1,1 x bac cu (+15) o cung cap (THIET_KE 3.6)
        final double nc15 = 1 + tongNc() / 100.0;
        final List<String> vuot = new ArrayList<String>();
        for (int b = 1; b < soBac; b++) {
            final int L = ThongSoLauDai.BAC_TB_CAP[b];
            final double goc = 30 + 12 * L;
            final double moiTc = goc + tc[b];
            final double cuTc = goc + nc15 * tc[b - 1];
            final double gocHp = 70.0 * (25 + L);
            if (moiTc > 1.10 * cuTc || gocHp + hp[b] > 1.10 * (gocHp + nc15 * hp[b - 1])) {
                vuot.add("bac " + b + ": " + Math.round(moiTc) + " > 1,1 x " + Math.round(cuTc));
            }
            if (tc[b] <= tc[b - 1] || thu[b] <= thu[b - 1] || hp[b] <= hp[b - 1]) {
                vuot.add("bac " + b + " khong tang");
            }
        }
        Kiem.bang("bac moi +0 <= 1,1 x bac cu +15 (tan cong, HP) va van tang theo bac", "[]", vuot.toString());
        // Do do Gym/boss/alpha moi duoc Hoan my -> kiem o kiemRoi. Bo set: giong nhau moi bac.
        final List<Map<String, String>> bo = csv(D + "noi_dung_bo.csv");
        final Set<String> thuongBo = new HashSet<String>();
        for (final Map<String, String> r : bo) {
            thuongBo.add(r.get("so_mon") + "|" + r.get("chi_so") + "|" + r.get("phan_tram"));
        }
        Kiem.bang("bo trang bi: 11 bac x 3 moc", Integer.valueOf(33), Integer.valueOf(bo.size()));
        Kiem.bang("thuong bo giong nhau moi bac (4/7/11 mon)", Integer.valueOf(3), Integer.valueOf(thuongBo.size()));
        // Chi so engine (data_item.atb) cua dong moi: khong vuot 1,1 x 1,55 x dong that cung type cao nhat
        final Map<Integer, Integer> maxCu = new HashMap<Integer, Integer>();
        for (final String[] r : hangDump(dump, "data_item")) {
            final int type = Integer.parseInt(r[2]);
            final int id = Integer.parseInt(r[0]);
            if (id >= ID_TU || (type > 12 && type != 19) || Integer.parseInt(r[17]) != 0) {
                continue;
            }
            int s = 0;
            for (int k = 8; k <= 13; k++) {
                s += Integer.parseInt(r[k]);
            }
            final Integer c = maxCu.get(Integer.valueOf(type));
            if (c == null || s > c.intValue()) {
                maxCu.put(Integer.valueOf(type), Integer.valueOf(s));
            }
        }
        final List<String> manh = new ArrayList<String>();
        int soTb = 0;
        for (final Map<String, String> r : moi) {
            final int type = Integer.parseInt(r.get("type"));
            if (type > 12 && type != 19) {
                continue;
            }
            soTb++;
            int s = 0;
            for (int k = 0; k <= 5; k++) {
                s += Integer.parseInt(r.get("atb" + k));
            }
            final Integer c = maxCu.get(Integer.valueOf(type));
            if (s <= 0 || c == null || s > 1.1 * nc15 * c.intValue()) {
                manh.add(r.get("id") + " " + r.get("name") + " " + s + "/" + c);
            }
            if (Integer.parseInt(r.get("level")) > ThongSoLauDai.CAP_TOI_DA) {
                manh.add(r.get("id") + " cap");
            }
        }
        Kiem.dung("co trang bi moi bac than thoai/toi thuong (" + soTb + ")", soTb >= 30);
        Kiem.bang("chi so engine trang bi moi > 0 va <= 1,1 x (+15) dong that manh nhat cung o", "[]", manh.toString());
        // Vat lieu nang cap: 11 bac x 15, xu = ThongSoLauDai.CHI_PHI_NC
        final List<String> vl = new ArrayList<String>();
        final List<Map<String, String>> vat = csv(D + "noi_dung_vat_lieu_nang_cap.csv");
        for (final Map<String, String> r : vat) {
            final int b = Integer.parseInt(r.get("bac"));
            final int k = Integer.parseInt(r.get("cong"));
            if (Long.parseLong(r.get("xu")) != ThongSoLauDai.CHI_PHI_NC[b][k]
                    || Integer.parseInt(r.get("da")) != ThongSoLauDai.DA_NC[k - 1]
                    || Integer.parseInt(r.get("ti_le")) != ThongSoLauDai.TI_LE_NC[k - 1]
                    || (k >= 11) == r.get("nguyen_lieu_phu").isEmpty()) {
                vl.add(b + "+" + k);
            }
        }
        Kiem.bang("vat lieu nang cap 11 bac x 15", Integer.valueOf(165), Integer.valueOf(vat.size()));
        Kiem.bang("vat lieu nang cap khop ThongSoLauDai (xu, da, ti le, nguyen lieu phu tu +11)", "[]", vl.toString());
        final List<Map<String, String>> ngoc = csv(D + "noi_dung_ngoc_he.csv");
        Kiem.bang("ngoc he 18 he x 10 cap", Integer.valueOf(180), Integer.valueOf(ngoc.size()));
    }

    // ------------------------------------------------------------------ vat pham tieu hao
    private static void kiemVatPham() throws Exception {
        final List<Map<String, String>> vp = csv(D + "noi_dung_vat_pham.csv");
        final Map<Integer, Map<String, String>> capDo = theoCap();
        final Set<String> nhom = new HashSet<String>();
        final List<String> loi = new ArrayList<String>();
        for (final Map<String, String> r : vp) {
            nhom.add(r.get("nhom"));
            final String tien = r.get("tien");
            final int gia = Integer.parseInt(r.get("gia"));
            if ("xu".equals(tien)) {
                final int giua = (Integer.parseInt(r.get("cap_tu")) + Integer.parseInt(r.get("cap_den"))) / 2;
                final double g = gia / Double.parseDouble(capDo.get(giua).get("xu_moi_gio"));
                if (g < 0.0005 || g > 0.05) {
                    loi.add(r.get("ma") + " " + g);
                }
            } else if ("luong_khoa".equals(tien)) {
                if (gia <= 0 || gia > 25 || r.get("gioi_han").isEmpty()) {
                    loi.add(r.get("ma") + " luong khoa " + gia);
                }
            } else if ("luong".equals(tien)) {
                loi.add(r.get("ma") + " ban bang luong (chi trang tri)");
            }
            if ("keo_exp".equals(r.get("nhom")) && (!r.get("hieu_ung").contains("tru_nang_luong")
                    || !"khong_ban".equals(tien))) {
                loi.add(r.get("ma") + " keo EXP phai tru nang luong va khong ban");
            }
        }
        final String[] can = {"hoi_hp", "hoi_pp", "trang_thai", "hoi_sinh", "x_tang_cuong", "xua_quai", "keo_exp",
            "da_tien_hoa", "qua_mong", "do_giu"};
        for (final String c : can) {
            if (!nhom.contains(c)) {
                loi.add("thieu nhom " + c);
            }
        }
        // Tong phan ngan sach thuoc (xu) dang dung o moi cap <= 100% (THIET_KE 2.2: thuoc 12% thu nhap)
        for (int L = 1; L <= ThongSoLauDai.CAP_TOI_DA; L++) {
            double s = 0;
            for (final Map<String, String> r : vp) {
                if ("xu".equals(r.get("tien")) && Integer.parseInt(r.get("cap_tu")) <= L
                        && L <= Integer.parseInt(r.get("cap_den"))) {
                    s += Double.parseDouble(r.get("phan_ngan_sach"));
                }
            }
            if (s > 1.0001) {
                loi.add("cap " + L + " tong ngan sach thuoc " + s);
            }
        }
        // 5 trang thai deu co thuoc chua (TrangThaiPokemon 1..5)
        for (int t = 1; t <= 5; t++) {
            boolean co = false;
            for (final Map<String, String> r : vp) {
                co |= "trang_thai".equals(r.get("nhom")) && r.get("hieu_ung").contains("go:" + t);
            }
            if (!co) {
                loi.add("thieu thuoc chua trang thai " + t);
            }
        }
        // Da tien hoa: dung 8 da 960..967, du 8 nhanh Eevee
        int da = 0;
        for (final Map<String, String> r : vp) {
            if ("da_tien_hoa".equals(r.get("nhom"))) {
                da++;
                final int id = Integer.parseInt(r.get("anh_xa").substring(5));
                if (id < 960 || id > 967) {
                    loi.add("da tien hoa " + id);
                }
            }
        }
        Kiem.bang("8 da tien hoa (960..967)", Integer.valueOf(8), Integer.valueOf(da));
        Kiem.bang("vat pham tieu hao: du nhom, gia trong tam thu nhap, ngan sach thuoc <= 12% thu nhap", "[]", loi.toString());
    }

    // ------------------------------------------------------------------ chieu
    private static void kiemChieu() throws Exception {
        final List<Map<String, String>> ch = csv(D + "noi_dung_chieu.csv");
        final List<Map<String, String>> bang = csv(D + "bang_chieu.csv");
        final Map<String, String> tenClient = new HashMap<String, String>();
        final int[] soClient = new int[5];
        for (final Map<String, String> r : bang) {
            tenClient.put(r.get("class") + "|" + r.get("so_hieu_skill"), r.get("ten_moi"));
            soClient[Integer.parseInt(r.get("class"))]++;
        }
        final int[] so = new int[5];
        final List<String> loi = new ArrayList<String>();
        final int[][] tienHoa = {{16, 36}, {16, 32}, {16, 25}, {16, 36}, {10, 30}};
        final Map<Integer, List<double[]>> theoDong = new HashMap<Integer, List<double[]>>();
        for (final Map<String, String> r : ch) {
            final int c = Integer.parseInt(r.get("dong"));
            so[c]++;
            final String k = c + "|" + r.get("so_hieu");
            if (!r.get("ten").equals(tenClient.get(k))) {
                loi.add("ten lech client " + k);
            }
            final int gd = Integer.parseInt(r.get("giai_doan"));
            final int mo = Integer.parseInt(r.get("cap_mo"));
            final int moToiDa = gd == 1 ? tienHoa[c][0] - 1 : (gd == 2 ? tienHoa[c][0] : tienHoa[c][1]);
            if (mo > moToiDa || (gd >= 2 && mo != moToiDa)) {
                loi.add("cap mo " + k + " " + mo);
            }
            final String[] capK = r.get("cap_moi_cap_chieu").split("/");
            int tr = 0;
            for (final String x : capK) {
                final int v = Integer.parseInt(x);
                if (v < tr || v > ThongSoLauDai.CAP_TOI_DA) {
                    loi.add("cap chieu " + k);
                }
                tr = v;
            }
            if (capK.length != 10 || Integer.parseInt(capK[0]) != mo) {
                loi.add("10 cap chieu " + k);
            }
            final int pt = Integer.parseInt(r.get("phan_tram_cap1"));
            List<double[]> l = theoDong.get(Integer.valueOf(c));
            if (l == null) {
                l = new ArrayList<double[]>();
                theoDong.put(Integer.valueOf(c), l);
            }
            l.add(new double[]{pt, Double.parseDouble(r.get("hoi_chieu_giay")), mo, gd,
                "toi_thuong".equals(r.get("kieu")) ? 1 : 0, Integer.parseInt(r.get("phan_tram_cap10"))});
        }
        for (int c = 0; c < 5; c++) {
            if (so[c] != soClient[c]) {
                loi.add("dong " + c + " so chieu " + so[c] + "/" + soClient[c]);
            }
            // moi giai doan co chieu sat thuong; giai doan 3 co toi thuong manh hon moi chieu dang 1
            final boolean[] coGd = new boolean[4];
            double maxGd1 = 0;
            double toiThuong = 0;
            for (final double[] s : theoDong.get(Integer.valueOf(c))) {
                if (s[0] > 0) {
                    coGd[(int) s[3]] = true;
                }
                if (s[3] == 1) {
                    maxGd1 = Math.max(maxGd1, s[0]);
                }
                if (s[4] == 1) {
                    toiThuong = s[0];
                }
            }
            if (!coGd[1] || !coGd[2] || !coGd[3] || toiThuong <= maxGd1) {
                loi.add("dong " + c + " cay chieu thieu giai doan/toi thuong");
            }
        }
        Kiem.bang("cay chieu: du so chieu, ten khop client, cap mo theo tien hoa, 10 cap <= 100", "[]", loi.toString());
        // TTK: tinh lai xoay chieu (cung thuat toan script) va doi chieu noi_dung_chieu_ttk.csv
        final List<Map<String, String>> ttk = csv(D + "noi_dung_chieu_ttk.csv");
        final Map<Integer, Map<String, String>> capDo = theoCap();
        final double[][] chiSo = {{100, 105, 90}, {115, 90, 115}, {90, 100, 95}, {120, 85, 125}, {85, 110, 85}};
        final List<String> lech = new ArrayList<String>();
        final Map<String, List<Double>> heSoTheoGd = new HashMap<String, List<Double>>();
        for (final Map<String, String> r : ttk) {
            final int c = Integer.parseInt(r.get("dong"));
            final int L = Integer.parseInt(r.get("cap"));
            final List<double[]> ds = new ArrayList<double[]>();
            for (final Map<String, String> s : ch) {
                if (Integer.parseInt(s.get("dong")) != c || Integer.parseInt(s.get("phan_tram_cap1")) <= 0) {
                    continue;
                }
                final String[] capK = s.get("cap_moi_cap_chieu").split("/");
                if (L < Integer.parseInt(capK[0])) {
                    continue;
                }
                int k = 1;
                for (int i = 0; i < capK.length; i++) {
                    if (Integer.parseInt(capK[i]) <= L) {
                        k = i + 1;
                    }
                }
                final double pt1 = Integer.parseInt(s.get("phan_tram_cap1"));
                ds.add(new double[]{Math.round(pt1 / 1.0) * (1 + 0.02 * (k - 1)), Double.parseDouble(s.get("hoi_chieu_giay"))});
            }
            final double R = xoayChieu(ds);
            final Map<String, String> cd = capDo.get(L);
            final double atk = Double.parseDouble(cd.get("tan_cong_tran")) * (1 + 0.6 * (1 + tongNc(6) / 100.0))
                    * chiSo[c][1] / 100.0;
            final double t = Double.parseDouble(cd.get("quai_hp")) / ((atk - Double.parseDouble(cd.get("quai_thu"))) * 1.1 * R);
            final double mt = Double.parseDouble(r.get("ttk_muc_tieu"));
            final double cs = chiSo[c][0] * chiSo[c][1] * chiSo[c][2] / 1e6;
            if (Math.abs(t - Double.parseDouble(r.get("ttk_giay"))) > 0.05 * t) {
                lech.add("tinh lai " + c + "/" + L + ": " + t + " vs " + r.get("ttk_giay"));
            }
            final double heSo = t / mt / cs;
            if (heSo < 0.65 || heSo > 1.05) {
                lech.add("TTK ngoai muc tieu " + c + "/" + L + " he so " + heSo);
            }
            List<Double> l = heSoTheoGd.get(r.get("giai_doan"));
            if (l == null) {
                l = new ArrayList<Double>();
                heSoTheoGd.put(r.get("giai_doan"), l);
            }
            l.add(Double.valueOf(heSo));
        }
        // Can bang dong: HP x thu x (tan cong x xoay chieu) bang nhau +-5% o moi moc
        for (final Map.Entry<String, List<Double>> e : heSoTheoGd.entrySet()) {
            double mn = 9, mx = 0;
            for (final Double v : e.getValue()) {
                mn = Math.min(mn, v.doubleValue());
                mx = Math.max(mx, v.doubleValue());
            }
            if (mx / mn > 1.10) {
                lech.add("giai doan " + e.getKey() + " lech dong " + mn + ".." + mx);
            }
        }
        Kiem.bang("chieu: 5 dong x 4 moc TTK", Integer.valueOf(20), Integer.valueOf(ttk.size()));
        Kiem.bang("TTK theo chieu trong [0,65..1,05] x muc tieu x chi so dong, cac dong can bang +-5%", "[]", lech.toString());
        Kiem.bang("8 nhanh Eevee", Integer.valueOf(8), Integer.valueOf(csv(D + "noi_dung_chieu_nhanh_eevee.csv").size()));
    }

    /** Ban Java cua sinh_noi_dung.xoay_chieu: xoay tham lam 60 giay, GCD 0,7 giay. */
    static double xoayChieu(final List<double[]> ds) {
        final double gcd = 0.7;
        final double[] san = new double[ds.size()];
        double t = 0, tong = 0;
        while (t < 60.0 - 1e-9) {
            int best = -1;
            for (int i = 0; i < ds.size(); i++) {
                if (san[i] <= t + 1e-9 && (best < 0 || ds.get(i)[0] > ds.get(best)[0])) {
                    best = i;
                }
            }
            if (best >= 0) {
                tong += ds.get(best)[0] / 100.0;
                san[best] = t + Math.max(ds.get(best)[1], gcd);
            }
            t += gcd;
        }
        return tong / 60.0;
    }

    // ------------------------------------------------------------------ quai
    private static void kiemQuai() throws Exception {
        final List<String> loi = new ArrayList<String>();
        final List<Map<String, String>> dai = csv(D + "noi_dung_quai_dai_cap.csv");
        Kiem.bang("20 dai cap quai (1..100)", Integer.valueOf(20), Integer.valueOf(dai.size()));
        for (final Map<String, String> r : dai) {
            final int c1 = Integer.parseInt(r.get("cap_tu"));
            final boolean du = "1".equals(r.get("du"));
            if (!du) {
                loi.add("dai thieu loai/he (ca template nam 2) " + r.get("dai"));
            }
            if (!du) {
                // dai nam 2: phai co de xuat >= 3 loai, >= 2 he
                final Set<String> he = new HashSet<String>();
                for (final String x : r.get("de_xuat_moi").split("\\|")) {
                    final int a = x.indexOf('(');
                    if (a > 0) {
                        he.add(x.substring(a + 1, x.indexOf(',', a)).split("/")[0]);
                    }
                }
                if (Integer.parseInt(r.get("so_de_xuat")) < 3 || he.size() < 2) {
                    loi.add("dai nam 2 de xuat thieu " + r.get("dai"));
                }
            }
        }
        for (final Map<String, String> r : csv(D + "noi_dung_quai_tinh_anh.csv")) {
            if (!r.get("ten").startsWith("Alpha ") || Double.parseDouble(r.get("ttk_giay")) < 12
                    || Double.parseDouble(r.get("ttk_giay")) > 32) {
                loi.add("alpha " + r.get("id_goc") + " ttk " + r.get("ttk_giay"));
            }
        }
        final List<Map<String, String>> gym = csv(D + "noi_dung_gym.csv");
        final Set<String> huy = new HashSet<String>();
        for (final Map<String, String> r : csv(D + "nhiem_vu_moi.csv")) {
            if (!r.get("huy_hieu").isEmpty()) {
                huy.add(r.get("huy_hieu"));
            }
        }
        int capTruoc = 0;
        for (final Map<String, String> r : gym) {
            if (!huy.remove(r.get("huy_hieu"))) {
                loi.add("gym khong khop huy hieu nhiem vu " + r.get("huy_hieu"));
            }
            final int c = Integer.parseInt(r.get("cap"));
            if (c < capTruoc) {
                loi.add("gym cap giam " + r.get("stt"));
            }
            capTruoc = c;
        }
        Kiem.bang("16 Gym nam 1", Integer.valueOf(16), Integer.valueOf(gym.size()));
        Kiem.bang("moi huy hieu nhiem vu co Gym", "[]", huy.toString());
        final List<Map<String, String>> boss = csv(D + "noi_dung_boss_the_gioi.csv");
        int dk = 0;
        for (final Map<String, String> r : boss) {
            if ("dot_kich".equals(r.get("loai"))) {
                dk++;
                if (r.get("vat_pham_dac_trung").isEmpty()) {
                    loi.add("dot kich thieu vat pham " + r.get("tuan"));
                }
            }
        }
        Kiem.bang("boss the gioi 52 tuan", Integer.valueOf(52), Integer.valueOf(boss.size()));
        Kiem.bang("5 tuan dot kich huyen thoai", Integer.valueOf(5), Integer.valueOf(dk));
        Kiem.bang("quai: dai cap du loai/he, alpha ~20 giay, Gym tang dan", "[]", loi.toString());
    }

    // ------------------------------------------------------------------ quai nam 2 (data_monster moi)
    private static void kiemQuaiMoi(final String dump) throws Exception {
        final List<String> loi = new ArrayList<String>();
        final List<Map<String, String>> moi = csv(D + "noi_dung_quai_moi.csv");
        int maxCu = 0;
        for (final String[] r : hangDump(dump, "data_monster")) {
            final int id = Integer.parseInt(r[0].trim());
            if (id < 139 || id > 199) {
                maxCu = Math.max(maxCu, id);
            }
        }
        // Client cap mang template theo SO LUONG (goi 26, 1 byte) roi tra theo id -> id lien tiep, <= 255.
        int ky = maxCu + 1;
        final Map<String, String> dexTheoId = new HashMap<String, String>();
        for (final Map<String, String> r : moi) {
            if (Integer.parseInt(r.get("id")) != ky++) {
                loi.add("id khong lien tiep " + r.get("id"));
            }
            if (Integer.parseInt(r.get("dex")) <= 0 || Integer.parseInt(r.get("maxhp")) <= 0) {
                loi.add("dex/hp " + r.get("id"));
            }
            dexTheoId.put(r.get("id"), r.get("dex"));
        }
        if (ky > 256) {
            loi.add("qua 255 template");
        }
        // bang_quai.csv co du dong moi; kiem_ke_sprite.csv co sprite + chan dung cho moi loai moi
        final Map<String, String> bq = new HashMap<String, String>();
        for (final Map<String, String> r : csv(D + "bang_quai.csv")) {
            bq.put(r.get("id_quai"), r.get("dex"));
        }
        final Map<String, Map<String, String>> kk = new HashMap<String, Map<String, String>>();
        for (final Map<String, String> r : csv(D + "kiem_ke_sprite.csv")) {
            if ("0".equals(r.get("shiny"))) {
                kk.put(r.get("dex"), r);
            }
        }
        for (final Map.Entry<String, String> e : dexTheoId.entrySet()) {
            if (!e.getValue().equals(bq.get(e.getKey()))) {
                loi.add("bang_quai.csv thieu/lech " + e.getKey());
            }
            final Map<String, String> k = kk.get(e.getValue());
            if (k == null || !"OK".equals(k.get("trang_thai")) || !"co".equals(k.get("portrait"))) {
                loi.add("sprite/chan dung PMD " + e.getValue());
            }
        }
        Kiem.bang("40 template quai nam 2 (36 Hoenn + 4 huyen thoai)", Integer.valueOf(40), Integer.valueOf(moi.size()));
        Kiem.bang("quai nam 2: id lien tiep sau " + maxCu + ", co trong bang_quai.csv, du sprite + chan dung", "[]",
                loi.toString());
        Kiem.dung("SQL chen data_monster nam 2", doc(F_SQL).contains("DELETE FROM `data_monster` WHERE `id` >= 139 AND `id` <= 199;"));
    }

    // ------------------------------------------------------------------ icon
    private static void kiemIcon(final List<Map<String, String>> itemMoi) throws Exception {
        final List<String> loi = new ArrayList<String>();
        final Set<String> da = new HashSet<String>();
        for (final Map<String, String> r : itemMoi) {
            if ("28".equals(r.get("type")) && !da.add(r.get("xstart"))) {
                loi.add("icon trung " + r.get("name"));
            }
            coIcon(r.get("xstart"), r.get("name"), loi, false);
        }
        for (final String[] t : new String[][]{{"vat_pham", "ten"}, {"ngoc_he", "ten"}, {"trang_tri", "ten"}}) {
            for (final Map<String, String> r : csv(D + "noi_dung_" + t[0] + ".csv")) {
                if ("can_code".equals(r.get("trang_thai"))) {
                    coIcon(r.get("icon"), r.get(t[1]), loi, true);
                }
            }
        }
        Kiem.bang("icon PMD (map/icon_pokemon) cho moi vat pham/ngoc/trang tri moi, vat pham moi khong dung chung icon",
                "[]", loi.toString());
    }

    private static void coIcon(final String id, final String ten, final List<String> loi, final boolean batBuocMoi) {
        final int x = Integer.parseInt(id);
        if (x < 14000 || x > 14999 || !new java.io.File("map/icon_pokemon/" + (x - 14000) + ".png").isFile()
                || (batBuocMoi && x < 14401)) {
            loi.add(ten + "=" + id);
        }
    }

    // ------------------------------------------------------------------ roi
    private static void kiemRoi() throws Exception {
        final List<Map<String, String>> roi = csv(D + "noi_dung_roi.csv");
        final Map<String, Integer> tbTheoDai = new LinkedHashMap<String, Integer>();
        final List<String> loi = new ArrayList<String>();
        for (final Map<String, String> r : roi) {
            final int ppm = Integer.parseInt(r.get("ppm"));
            final String nguon = r.get("nguon");
            if (ppm < 0 || ppm > 1000000 || Integer.parseInt(r.get("sl_min")) > Integer.parseInt(r.get("sl_max"))) {
                loi.add("ppm/so luong " + r.get("stt"));
            }
            if ("hoan_my".equals(r.get("do_hiem")) && !"alpha".equals(nguon) && !"gym".equals(nguon)
                    && !"boss_the_gioi".equals(nguon)) {
                loi.add("Hoan my tu nguon " + nguon);
            }
            if ("dai".equals(nguon) && r.get("vat_pham").startsWith("item:") && !r.get("do_hiem").isEmpty()) {
                final Integer c = tbTheoDai.get(r.get("khoa_nguon"));
                tbTheoDai.put(r.get("khoa_nguon"), Integer.valueOf((c == null ? 0 : c.intValue()) + ppm));
            }
            if ("dai".equals(nguon) && r.get("vat_pham").equals("ngoc:8") && ppm != ThongSoLauDai.DA_ROI_PHAN_NGHIN * 1000) {
                loi.add("da nang cap " + r.get("khoa_nguon") + " " + ppm);
            }
            if (nguon.startsWith("boss") || nguon.startsWith("gym")) {
                if (!"1".equals(r.get("khoa"))) {
                    loi.add("thuong boss/gym phai khoa " + r.get("stt"));
                }
            }
        }
        for (final Map.Entry<String, Integer> e : tbTheoDai.entrySet()) {
            if (e.getValue().intValue() != 150) {
                loi.add("trang bi roi dai " + e.getKey() + " = " + e.getValue() + " ppm");
            }
        }
        Kiem.bang("20 dai co bang roi trang bi", Integer.valueOf(20), Integer.valueOf(tbTheoDai.size()));
        Kiem.bang("bang roi: trang bi 150 ppm/quai, da 1,5%, Hoan my chi tu alpha/Gym/boss, thuong boss khoa", "[]", loi.toString());
    }

    // ------------------------------------------------------------------ su kien + trang tri + cua hang
    private static void kiemSuKienTrangTri() throws Exception {
        final List<String> loi = new ArrayList<String>();
        final Map<String, Double> nganSach = new HashMap<String, Double>();
        for (final Map<String, String> r : csv(D + "su_kien_nam.csv")) {
            nganSach.put(r.get("tuan"), Double.valueOf(r.get("ngan_sach_gio")));
        }
        final Map<String, Double> dung = new HashMap<String, Double>();
        for (final Map<String, String> r : csv(D + "noi_dung_su_kien_vat_pham.csv")) {
            final double v = Integer.parseInt(r.get("so_luong_toi_da_tuan")) * Double.parseDouble(r.get("gio_thu_nhap_moi_cai"));
            final Double c = dung.get(r.get("tuan"));
            dung.put(r.get("tuan"), Double.valueOf((c == null ? 0 : c.doubleValue()) + v));
            if (!"1".equals(r.get("khoa")) || !"1".equals(r.get("het_han_khi_ket_thuc"))) {
                loi.add("vat pham su kien phai khoa + het han: tuan " + r.get("tuan"));
            }
        }
        double tong = 0;
        for (int t = 1; t <= 52; t++) {
            final Double v = dung.get(String.valueOf(t));
            if (v == null) {
                loi.add("tuan " + t + " khong co vat pham su kien");
                continue;
            }
            tong += v.doubleValue();
            if (v.doubleValue() > nganSach.get(String.valueOf(t)).doubleValue() * 1.01) {
                loi.add("tuan " + t + " vuot ngan sach " + v);
            }
        }
        Kiem.dung("tong gia tri vat pham su kien/nam <= 70 gio thu nhap (" + Math.round(tong) + ")", tong <= 70.0);
        for (final Map<String, String> r : csv(D + "noi_dung_trang_tri.csv")) {
            if (!"0".equals(r.get("chi_so"))) {
                loi.add("trang tri co chi so " + r.get("ten"));
            }
        }
        Kiem.bang("trang tri: 6 mua", Integer.valueOf(6), Integer.valueOf(soKhac(csv(D + "noi_dung_trang_tri.csv"), "mua")));
        // Cua hang: luong chi ban trang tri; luong khoa <= 25; xu trong tam thu nhap
        for (final Map<String, String> r : csv(D + "noi_dung_cua_hang.csv")) {
            final String tien = r.get("tien");
            if ("luong".equals(tien) && !r.get("cua_hang").contains("trang trí")) {
                loi.add("luong ban do khong trang tri " + r.get("ten"));
            }
            if ("luong_khoa".equals(tien) && Integer.parseInt(r.get("gia")) > 25) {
                loi.add("luong khoa > 25 " + r.get("ten"));
            }
            if ("xu".equals(tien)) {
                final double g = Double.parseDouble(r.get("gio_thu_nhap"));
                if (g <= 0 || g > 1.6) {
                    loi.add("gia xu ngoai tam " + r.get("ten") + " " + g);
                }
            }
        }
        for (final Map<String, String> r : csv(D + "noi_dung_che_tao.csv")) {
            if (Integer.parseInt(r.get("da")) < 2 || Integer.parseInt(r.get("so_nl_phu")) < 1) {
                loi.add("che tao " + r.get("ten"));
            }
        }
        Kiem.bang("su kien 52 tuan trong ngan sach, trang tri 0 chi so, cua hang dung tien", "[]", loi.toString());
    }

    // ------------------------------------------------------------------ ten
    private static final Pattern VIET = Pattern.compile("[àáảãạăằắẳẵặâầấẩẫậèéẻẽẹêềếểễệìíỉĩịòóỏõọôồốổỗộơờớởỡợùúủũụưừứửữựỳýỷỹỵđ]",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    /** Ten vat pham CO SAN trong data_potion (giu nguyen ten goc) khong co dau. */
    private static final Set<String> TEN_GOC_KHONG_DAU = new HashSet<String>(java.util.Arrays.asList("Kem"));

    private static void kiemTen(final List<Map<String, String>> moi) throws Exception {
        final List<String> ten = new ArrayList<String>();
        for (final Map<String, String> r : moi) {
            ten.add(r.get("name"));
        }
        final String[][] cot = {{"trang_bi", "ten"}, {"vat_pham", "ten"}, {"vat_pham", "mo_ta"}, {"nguyen_lieu", "ten"},
            {"trang_tri", "ten"}, {"gym", "ten_boss"}, {"su_kien_vat_pham", "vat_pham"}, {"bo", "ten_bo"},
            {"ngoc_he", "ten"}, {"chieu", "vai_tro"}};
        for (final String[] c : cot) {
            for (final Map<String, String> r : csv(D + "noi_dung_" + c[0] + ".csv")) {
                ten.add(r.get(c[1]));
            }
        }
        final List<String> camLoi = new ArrayList<String>();
        final List<String> khongViet = new ArrayList<String>();
        for (final String t : ten) {
            if (!ChuPokemon.tuCamTrong(t).isEmpty()) {
                camLoi.add(t + " " + ChuPokemon.tuCamTrong(t));
            }
            if (!VIET.matcher(t).find() && !TEN_GOC_KHONG_DAU.contains(t)) {
                khongViet.add(t);
            }
        }
        Kiem.dung("co ten de kiem (" + ten.size() + ")", ten.size() > 500);
        Kiem.bang("ten noi dung khong co tu cam kiem hiep (ChuPokemon.TU_CAM)", "[]", camLoi.toString());
        Kiem.bang("ten noi dung la tieng Viet co dau", "[]", khongViet.toString());
    }

    // ------------------------------------------------------------------ web
    private static void kiemWeb() throws Exception {
        final String[] tep = {"trang_bi", "bo_trang_bi", "vat_pham", "nang_cap", "ngoc_he", "chieu", "nhanh_eevee",
            "quai", "gym", "boss_the_gioi", "su_kien", "trang_tri", "cua_hang", "che_tao"};
        final List<String> loi = new ArrayList<String>();
        final Pattern noiBo = Pattern.compile("\\b(item|thuoc|ngoc|moi):|\\bppm\\b|\\b(id|ma|anh_xa)\\b|_");
        for (final String t : tep) {
            final String s = doc(D + "web/" + t + ".csv");
            final String dau = s.substring(0, s.indexOf('\n'));
            if (noiBo.matcher(dau).find()) {
                loi.add(t + " cot noi bo: " + dau);
            }
            final Matcher m = Pattern.compile("\\b(item|thuoc|ngoc|moi):\\w+").matcher(s);
            if (m.find()) {
                loi.add(t + " lo id: " + m.group());
            }
            if (csv(D + "web/" + t + ".csv").isEmpty()) {
                loi.add(t + " rong");
            }
        }
        Kiem.bang("du_lieu/web/*.csv chi cot nguoi choi (khong id/cong thuc)", "[]", loi.toString());
    }

    // ------------------------------------------------------------------ sql
    private static void kiemSql(final List<Map<String, String>> moi) throws Exception {
        final String sql = doc(F_SQL);
        Kiem.dung("SQL xoa dai id truoc khi chen (chay lai duoc)",
                sql.contains("DELETE FROM `data_item` WHERE `id` >= " + ID_TU + " AND `id` <= " + ID_DEN + ";"));
        boolean du = true;
        for (final Map<String, String> r : moi) {
            du &= sql.contains("(" + r.get("id") + ", '");
        }
        Kiem.dung("SQL chen du " + moi.size() + " item moi", du);
        final Matcher m = Pattern.compile("CREATE TABLE `(data_noi_dung_\\w+)`").matcher(sql);
        int n = 0;
        final List<String> thieuDrop = new ArrayList<String>();
        while (m.find()) {
            n++;
            if (!sql.contains("DROP TABLE IF EXISTS `" + m.group(1) + "`;")) {
                thieuDrop.add(m.group(1));
            }
        }
        Kiem.dung("SQL co >= 19 bang staging data_noi_dung_* (" + n + ")", n >= 19);
        Kiem.bang("moi bang staging co DROP IF EXISTS", "[]", thieuDrop.toString());
        Kiem.sai("SQL portable: khong CREATE TABLE ... LIKE / ENGINE=", sql.contains(" LIKE ") || sql.contains("ENGINE="));
        Kiem.dung("xuat_sql_day_du.sh gom pokemon_noi_dung", doc("../tools/pokemon/xuat_sql_day_du.sh").contains("pokemon_noi_dung"));
    }

    // ================================================================== tien ich
    private static int tranCap(final int ngay) {
        int t = 0;
        for (int i = 0; i < ThongSoLauDai.TRAN_CAP_NGAY.length; i++) {
            if (ngay >= ThongSoLauDai.TRAN_CAP_NGAY[i]) {
                t = ThongSoLauDai.TRAN_CAP_GIA_TRI[i];
            }
        }
        return t;
    }

    private static double tongNc() {
        return tongNc(ThongSoLauDai.CHI_SO_NC_PHAN_TRAM.length);
    }

    private static double tongNc(final int k) {
        double s = 0;
        for (int i = 0; i < k; i++) {
            s += ThongSoLauDai.CHI_SO_NC_PHAN_TRAM[i];
        }
        return s;
    }

    private static int soKhac(final List<Map<String, String>> ds, final String cot) {
        final Set<String> s = new HashSet<String>();
        for (final Map<String, String> r : ds) {
            s.add(r.get(cot));
        }
        return s.size();
    }

    private static Map<Integer, Map<String, String>> theoCap() throws Exception {
        final Map<Integer, Map<String, String>> m = new HashMap<Integer, Map<String, String>>();
        for (final Map<String, String> r : csv(D + "bang_cap_do_lau_dai.csv")) {
            m.put(Integer.valueOf(r.get("cap")), r);
        }
        return m;
    }

    private static String doc(final String p) throws Exception {
        return new String(Files.readAllBytes(Paths.get(p)), StandardCharsets.UTF_8);
    }

    private static String docKhongLoi(final String p) {
        try {
            return doc(p);
        } catch (final Exception e) {
            return "";
        }
    }

    private static List<Map<String, String>> csvKhongLoi(final String p) {
        try {
            return csv(p);
        } catch (final Exception e) {
            return new ArrayList<Map<String, String>>();
        }
    }

    /** CSV (RFC 4180, dau phay, nhay kep) -> list map theo header. */
    static List<Map<String, String>> csv(final String p) throws Exception {
        final String s = doc(p);
        final List<List<String>> hang = new ArrayList<List<String>>();
        List<String> cur = new ArrayList<String>();
        final StringBuilder o = new StringBuilder();
        boolean q = false;
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (q) {
                if (c == '"') {
                    if (i + 1 < s.length() && s.charAt(i + 1) == '"') {
                        o.append('"');
                        i++;
                    } else {
                        q = false;
                    }
                } else {
                    o.append(c);
                }
            } else if (c == '"') {
                q = true;
            } else if (c == ',') {
                cur.add(o.toString());
                o.setLength(0);
            } else if (c == '\n') {
                cur.add(o.toString());
                o.setLength(0);
                hang.add(cur);
                cur = new ArrayList<String>();
            } else if (c != '\r') {
                o.append(c);
            }
        }
        if (o.length() > 0 || !cur.isEmpty()) {
            cur.add(o.toString());
            hang.add(cur);
        }
        final List<Map<String, String>> ra = new ArrayList<Map<String, String>>();
        final List<String> dau = hang.get(0);
        for (int i = 1; i < hang.size(); i++) {
            final Map<String, String> m = new LinkedHashMap<String, String>();
            for (int j = 0; j < dau.size(); j++) {
                m.put(dau.get(j), j < hang.get(i).size() ? hang.get(i).get(j) : "");
            }
            ra.add(m);
        }
        return ra;
    }

    /** Cac dong INSERT INTO `bang` VALUES (...) cua dump mysqldump -> mang gia tri (chuoi bo nhay). */
    static List<String[]> hangDump(final String dump, final String bang) {
        final List<String[]> ra = new ArrayList<String[]>();
        int i = 0;
        final String mo = "INSERT INTO `" + bang + "` VALUES ";
        while ((i = dump.indexOf(mo, i)) >= 0) {
            i += mo.length();
            while (i < dump.length() && dump.charAt(i) == '(') {
                i++;
                final List<String> r = new ArrayList<String>();
                final StringBuilder o = new StringBuilder();
                boolean q = false;
                while (true) {
                    final char c = dump.charAt(i);
                    if (q) {
                        if (c == '\\') {
                            o.append(dump.charAt(i + 1));
                            i += 2;
                            continue;
                        }
                        if (c == '\'') {
                            q = false;
                        } else {
                            o.append(c);
                        }
                        i++;
                        continue;
                    }
                    if (c == '\'') {
                        q = true;
                        i++;
                        continue;
                    }
                    if (c == ',' || c == ')') {
                        r.add(o.toString());
                        o.setLength(0);
                        i++;
                        if (c == ')') {
                            break;
                        }
                        continue;
                    }
                    o.append(c);
                    i++;
                }
                ra.add(r.toArray(new String[0]));
                if (dump.charAt(i) == ',') {
                    i++;
                }
            }
        }
        return ra;
    }

    static Set<Integer> idTrongDump(final String dump, final String bang) {
        final Set<Integer> s = new HashSet<Integer>();
        for (final String[] r : hangDump(dump, bang)) {
            s.add(Integer.valueOf(r[0].trim()));
        }
        return s;
    }
}
