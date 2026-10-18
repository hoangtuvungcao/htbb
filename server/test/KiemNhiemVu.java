import com.htbb.pokemon.ChuPokemon;
import com.htbb.pokemon.ThongSoLauDai;
import java.io.File;
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
 * Bang nhiem vu moi (docs/pokemon/NHIEM_VU_THIET_KE.md, sinh boi tools/pokemon/sinh_nhiem_vu.py).
 *
 * VI SAO: nguoi choi phan nan "nhiem vu it, don dieu" -> bang mo rong ~1.860 dong, 45 kieu muc tieu.
 * Test giu: du so luong tung nhom, moi NPC/ban do/quai/vat pham tro toi deu ton tai, chuoi
 * dieu kien truoc la DAG khong mo coi, thuong trong ngan sach (tong khong vuot bang cu), khong
 * tu cam, du chu cho moi dong, va goi danh sach ghi so luong 1 byte (<= 127) o moi cap.
 * Chi doc file, khong can CSDL.
 */
public final class KiemNhiemVu {

    private static final String D = "docs/pokemon/";

    private KiemNhiemVu() {
    }

    public static void chay() {
        Kiem.nhom("Nhiem vu moi: so luong, tham chieu, DAG, ngan sach, chu, gioi han goi 1 byte");
        try {
            final List<Map<String, String>> nv = KiemLauDai.docCsv(D + "nhiem_vu_moi.csv");
            kiemSoLuong(nv);
            kiemChu(nv);
            kiemThamChieu(nv);
            kiemDag(nv);
            kiemNganSach(nv);
            kiemGoi(nv);
            kiemFile(nv);
            kiemHeSinhThai(nv);
        } catch (final Exception e) {
            Kiem.dung("khong nem loi: " + e, false);
        }
    }

    static int so(final String s) {
        return s == null || s.isEmpty() ? 0 : (int) Long.parseLong(s);
    }

    static Map<String, Integer> dem(final List<Map<String, String>> nv, final String cot) {
        final Map<String, Integer> m = new HashMap<String, Integer>();
        for (final Map<String, String> d : nv) {
            m.merge(d.get(cot), 1, Integer::sum);
        }
        return m;
    }

    // ------------------------------------------------------------------ so luong
    private static void kiemSoLuong(final List<Map<String, String>> nv) {
        final Map<String, Integer> l = dem(nv, "loai");
        Kiem.dung("tong >= 1800 dong (" + nv.size() + ")", nv.size() >= 1800);
        final Map<String, Integer> buocChuong = new HashMap<String, Integer>();
        final Set<String> nam2Viet = new HashSet<String>();
        for (final Map<String, String> d : nv) {
            if ("chinh".equals(d.get("loai")) && "1".equals(d.get("nam"))) {
                buocChuong.merge(d.get("chuong"), 1, Integer::sum);
            }
            if ("chinh".equals(d.get("loai")) && "2".equals(d.get("nam")) && !"dan_y".equals(d.get("kieu_muc_tieu"))) {
                nam2Viet.add(d.get("chuong"));
            }
        }
        boolean du = buocChuong.size() == 52;
        for (final int n : buocChuong.values()) {
            du &= n >= 6 && n <= 10;
        }
        Kiem.dung("nam 1: 52 chuong, moi chuong 6-10 buoc " + buocChuong.values(), du);
        Kiem.dung("nam 2: >= 26 chuong viet day du (" + nam2Viet.size() + ")", nam2Viet.size() >= 26);
        final Object[][] nguong = {{"phu", 400}, {"ngay", 200}, {"tuan", 80}, {"pokedex", 150}, {"thanh_tuu", 100},
            {"su_kien", 156}, {"an", 30}};
        for (final Object[] x : nguong) {
            final int c = l.getOrDefault((String) x[0], 0);
            Kiem.dung(x[0] + " >= " + x[1] + " (" + c + ")", c >= (Integer) x[1]);
        }
        final Set<String> tuanSk = new HashSet<String>();
        final Set<String> chuoiHt = new HashSet<String>();
        final Set<String> chuoiPhu = new HashSet<String>();
        int an = 0, nhanh = 0, cauDo = 0;
        for (final Map<String, String> d : nv) {
            if ("su_kien".equals(d.get("loai"))) {
                tuanSk.add(d.get("ma").substring(0, 5));
            }
            if ("huyen_thoai".equals(d.get("loai"))) {
                chuoiHt.add(d.get("cung"));
            }
            if ("phu".equals(d.get("loai"))) {
                chuoiPhu.add(d.get("cung"));
            }
            if ("1".equals(d.get("an")) && !d.get("dieu_kien_mo").isEmpty()) {
                an++;
            }
            if (!d.get("nhanh_lua_chon").isEmpty()) {
                nhanh++;
            }
            if ("cau_do".equals(d.get("kieu_muc_tieu")) && !d.get("dap_an").isEmpty()) {
                cauDo++;
            }
        }
        Kiem.bang("su kien phu du 52 tuan", 52, tuanSk.size());
        Kiem.dung(">= 10 chuoi huyen thoai (" + chuoiHt.size() + ")", chuoiHt.size() >= 10);
        Kiem.dung(">= 90 chuoi phu theo NPC (" + chuoiPhu.size() + ")", chuoiPhu.size() >= 90);
        Kiem.dung(">= 30 nhiem vu an co dieu kien mo (" + an + ")", an >= 30);
        Kiem.dung("co nhanh lua chon (" + nhanh + ") va cau do co dap an (" + cauDo + ")", nhanh >= 10 && cauDo >= 20);
        final Map<String, Integer> k = dem(nv, "kieu_muc_tieu");
        int kieu = 0;
        for (final String x : k.keySet()) {
            if (!x.startsWith("thanh_tuu_")) {
                kieu++;
            }
        }
        Kiem.dung(">= 35 kieu muc tieu khac nhau (" + kieu + ")", kieu >= 35);
        final int haQuai = k.getOrDefault("giet", 0) + k.getOrDefault("giet_lech_cap", 0);
        Kiem.dung("'ha N con' < 25% bang (" + haQuai + "/" + nv.size() + ")", haQuai * 4 < nv.size());
    }

    // ------------------------------------------------------------------ chu
    private static void kiemChu(final List<Map<String, String>> nv) {
        String thieu = "", cam = "", dai = "";
        for (final Map<String, String> d : nv) {
            final boolean tt = "thanh_tuu".equals(d.get("loai"));
            for (final String c : new String[]{"ten", "mo_ta", "muc_tieu_text", "goi_y", "dong_thuong"}) {
                if (d.get(c).trim().isEmpty() && thieu.isEmpty()) {
                    thieu = d.get("ma") + "." + c;
                }
            }
            if (!tt) {
                for (final String c : new String[]{"hoi_thoai_bat_dau", "hoi_thoai_dang_lam", "hoi_thoai_xong"}) {
                    if (d.get(c).trim().isEmpty() && thieu.isEmpty()) {
                        thieu = d.get("ma") + "." + c;
                    }
                }
            }
            for (final String c : new String[]{"ten", "mo_ta", "muc_tieu_text", "muc_tieu_ten", "hoi_thoai_bat_dau",
                "hoi_thoai_dang_lam", "hoi_thoai_xong", "goi_y", "dong_thuong", "thuong_khac", "nhanh_lua_chon", "cung",
                "dieu_kien_mo"}) {
                if (!ChuPokemon.tuCamTrong(d.get(c)).isEmpty() && cam.isEmpty()) {
                    cam = d.get("ma") + "." + c + " " + ChuPokemon.tuCamTrong(d.get(c));
                }
            }
            if (d.get("ten").length() > 45 && dai.isEmpty()) {
                dai = d.get("ma");
            }
            if (d.get("hoi_thoai_bat_dau").contains("{") && !"thanh_tuu".equals(d.get("loai")) && thieu.isEmpty()) {
                thieu = d.get("ma") + " con cho {..} chua thay";
            }
        }
        Kiem.bang("moi dong du ten/mo ta/muc tieu/goi y/thuong + 3 doan hoi thoai", "", thieu);
        Kiem.bang("khong tu cam trong chu nguoi choi thay", "", cam);
        Kiem.bang("ten <= 45 ky tu", "", dai);
    }

    // ------------------------------------------------------------------ tham chieu
    static Set<String> cot(final String file, final String cot, final char ngan) throws Exception {
        final Set<String> ra = new HashSet<String>();
        final List<String> dong = new ArrayList<String>();
        for (final String l : Files.readAllLines(Paths.get(D + file), StandardCharsets.UTF_8)) {
            if (!l.startsWith("#") && !l.isEmpty()) {
                dong.add(l);
            }
        }
        final List<String> dau = tach(dong.get(0), ngan);
        final int i = dau.indexOf(cot);
        for (int j = 1; j < dong.size(); j++) {
            final List<String> o = tach(dong.get(j), ngan);
            if (i < o.size()) {
                ra.add(o.get(i));
            }
        }
        return ra;
    }

    static List<String> tach(final String s, final char ngan) {
        final List<String> ra = new ArrayList<String>();
        final StringBuilder b = new StringBuilder();
        boolean q = false;
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c == '"') {
                if (q && i + 1 < s.length() && s.charAt(i + 1) == '"') {
                    b.append('"');
                    i++;
                } else {
                    q = !q;
                }
            } else if (c == ngan && !q) {
                ra.add(b.toString());
                b.setLength(0);
            } else {
                b.append(c);
            }
        }
        ra.add(b.toString());
        return ra;
    }

    static Set<String> idQuai() throws Exception {
        final String s = new String(Files.readAllBytes(Paths.get("sql/htbb_day_du.sql")), StandardCharsets.UTF_8);
        final int i = s.indexOf("INSERT INTO `data_monster` VALUES");
        final int j = s.indexOf(";\n", i);
        final Set<String> ra = new HashSet<String>();
        final Matcher m = Pattern.compile("\\((\\d+),'").matcher(s.substring(i, j));
        while (m.find()) {
            ra.add(m.group(1));
        }
        return ra;
    }

    private static final Set<String> KIEU_QUAI = new HashSet<String>(java.util.Arrays.asList("giet", "giet_tinh_anh",
            "ha_alpha", "ha_gym", "quan_sat", "thu_thach_gio", "pokedex_gap", "pokedex_ha", "pokedex_shiny"));
    private static final Set<String> KIEU_VAT = new HashSet<String>(java.util.Arrays.asList("giao_hang", "tim_vat_an",
            "thu_thap"));

    private static void kiemThamChieu(final List<Map<String, String>> nv) throws Exception {
        final Set<String> npc = cot("bang_npc.csv", "type", ',');
        final Set<String> map = cot("thiet_ke_map.csv", "id", ';');
        final Set<String> quai = idQuai();
        final Set<String> alpha = cot("noi_dung_quai_tinh_anh.csv", "id_goc", ',');
        final Set<String> itemMoi = cot("noi_dung_item_moi.csv", "id", ',');
        final Set<String> skVp = cot("noi_dung_su_kien_vat_pham.csv", "anh_xa", ',');
        final Set<String> huyGym = cot("noi_dung_gym.csv", "huy_hieu", ',');
        // trang bi co chi so: id -> {cap yeu cau}
        final List<Map<String, String>> tb = KiemLauDai.docCsv(D + "noi_dung_trang_bi.csv");
        final Map<String, Integer> tbCap = new HashMap<String, Integer>();
        final List<Integer> capBac = new ArrayList<Integer>();
        for (final Map<String, String> r : tb) {
            final int st = so(r.get("tan_cong")) + so(r.get("phong_thu")) + so(r.get("hp"));
            if (st > 0) {
                tbCap.put(r.get("id_item"), so(r.get("cap_yeu_cau")));
                if (!capBac.contains(so(r.get("cap_yeu_cau")))) {
                    capBac.add(so(r.get("cap_yeu_cau")));
                }
            }
        }
        final String sqlNv = new String(Files.readAllBytes(Paths.get("sql/pokemon_nhiem_vu.sql")), StandardCharsets.UTF_8);
        final Set<String> vatNv = new HashSet<String>(java.util.Arrays.asList("Nọc bọ cạp", "Khúc gỗ", "Rết đỏ", "Lá thư",
                "Lông đuôi Hồng Kê", "Móng cá sấu", "Da ếch", "Cựa gà"));
        final Matcher mv = Pattern.compile("\\((\\d+),'([^']*)'\\)").matcher(
                sqlNv.substring(sqlNv.indexOf("INSERT INTO `data_vat_pham_nhiem_vu`")));
        while (mv.find()) {
            vatNv.add(mv.group(2));
        }
        final Set<String> kieu = new HashSet<String>();
        final Matcher mk = Pattern.compile("\\('([a-z_]+)','").matcher(
                sqlNv.substring(sqlNv.indexOf("INSERT INTO `data_nhiem_vu_kieu`"), sqlNv.indexOf("DROP TABLE IF EXISTS `data_vat_pham_nhiem_vu`")));
        while (mk.find()) {
            kieu.add(mk.group(1));
        }
        final List<String> loi = new ArrayList<String>();
        final Set<String> gymCo = new HashSet<String>();
        for (final Map<String, String> d : nv) {
            final String ma = d.get("ma");
            final String k = d.get("kieu_muc_tieu");
            if (!k.startsWith("thanh_tuu_") && !kieu.contains(k)) {
                loi.add(ma + " kieu " + k);
            }
            if (!d.get("npc_giao").isEmpty() && !npc.contains(d.get("npc_giao"))) {
                loi.add(ma + " npc " + d.get("npc_giao"));
            }
            if (!d.get("map_id").isEmpty()) {
                if (!map.contains(d.get("map_id"))) {
                    loi.add(ma + " map " + d.get("map_id"));
                } else if (!d.get("toa_do").isEmpty()) {
                    final String[] xy = d.get("toa_do").split(";");
                    final int[] wh = kichThuoc(d.get("map_id"));
                    if (wh != null && (so(xy[0]) >= wh[0] || so(xy[1]) >= wh[1])) {
                        loi.add(ma + " toa do ngoai ban do " + d.get("toa_do"));
                    }
                }
            }
            if (KIEU_QUAI.contains(k) && !d.get("muc_tieu_id").isEmpty() && !quai.contains(d.get("muc_tieu_id"))) {
                loi.add(ma + " quai " + d.get("muc_tieu_id"));
            }
            if ("ha_alpha".equals(k) && !alpha.contains(d.get("muc_tieu_id"))) {
                loi.add(ma + " alpha " + d.get("muc_tieu_id"));
            }
            if ("thu_thap".equals(k) && !quai.contains(d.get("quai_roi"))) {
                loi.add(ma + " quai roi " + d.get("quai_roi"));
            }
            final String dp = d.get("du_phong");
            if (dp.startsWith("0:") || dp.startsWith("2:")) {
                if (!quai.contains(dp.split(":")[1])) {
                    loi.add(ma + " du phong quai " + dp);
                }
            }
            if ((dp.startsWith("1:") || dp.startsWith("3:")) && !npc.contains(dp.split(":")[1])) {
                loi.add(ma + " du phong npc " + dp);
            }
            if (KIEU_VAT.contains(k) && !vatNv.contains(d.get("muc_tieu_ten"))) {
                loi.add(ma + " vat pham nhiem vu " + d.get("muc_tieu_ten"));
            }
            final String vp = d.get("vat_pham");
            if (!vp.isEmpty()) {
                final Integer c = tbCap.get(vp);
                final int L = so(d.get("cap_toi_thieu"));
                int bacDung = 0;
                for (final int cb : capBac) {
                    if (cb <= Math.max(1, L) && cb > bacDung) {
                        bacDung = cb;
                    }
                }
                if (c == null) {
                    loi.add(ma + " vat_pham " + vp + " khong phai trang bi co chi so");
                } else if (c != bacDung) {
                    loi.add(ma + " vat_pham " + vp + " cap " + c + " lech bac cua cap " + L);
                }
            }
            for (final String x : d.get("thuong_khac").split(";")) {
                if (x.startsWith("item:")) {
                    final String id = x.substring(5, x.indexOf('x'));
                    if (!itemMoi.contains(id) && !skVp.contains("item:" + id)) {
                        loi.add(ma + " thuong item " + id);
                    }
                } else if (x.startsWith("thuoc:")) {
                    if (!skVp.contains(x.substring(0, x.indexOf('x')))) {
                        loi.add(ma + " thuong " + x);
                    }
                }
            }
            if (!d.get("huy_hieu").isEmpty()) {
                if (!"ha_gym".equals(k)) {
                    loi.add(ma + " huy hieu khong o buoc Gym");
                }
                gymCo.add(d.get("huy_hieu"));
            }
        }
        Kiem.bang("moi NPC/ban do/toa do/quai/alpha/vat pham/kieu tro toi deu ton tai; vat_pham la trang bi co chi so dung bac",
                "[]", loi.size() > 8 ? loi.subList(0, 8).toString() + "... (" + loi.size() + ")" : loi.toString());
        Kiem.bang("16 huy hieu Gym deu o buoc ha_gym", huyGym, gymCo);
    }

    static int[] kichThuoc(final String map) throws Exception {
        final File f = new File(D + "map_cu/" + map + ".json");
        if (!f.exists()) {
            return null;
        }
        final String s = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
        final Matcher w = Pattern.compile("\"w\":\\s*(\\d+)").matcher(s);
        final Matcher h = Pattern.compile("\"h\":\\s*(\\d+)").matcher(s);
        return w.find() && h.find() ? new int[]{Integer.parseInt(w.group(1)), Integer.parseInt(h.group(1))} : null;
    }

    // ------------------------------------------------------------------ DAG
    private static void kiemDag(final List<Map<String, String>> nv) {
        final Set<String> ma = new HashSet<String>();
        final Set<String> id = new HashSet<String>();
        String loi = "";
        final Map<String, String> cungCua = new HashMap<String, String>();
        for (final Map<String, String> d : nv) {
            final String m = d.get("ma");
            if (!ma.add(m) && loi.isEmpty()) {
                loi = "trung ma " + m;
            }
            if (!id.add(d.get("id_so")) && loi.isEmpty()) {
                loi = "trung id_so " + d.get("id_so");
            }
            cungCua.put(m, d.get("cung"));
            final String t = d.get("truoc");
            final String l = d.get("loai");
            if (!t.isEmpty() && !cungCua.containsKey(t) && loi.isEmpty()) {
                loi = m + " truoc " + t + " chua co (phai dung truoc -> khong vong)";
            }
            final int buoc = so(d.get("buoc"));
            final boolean chuoi = "phu".equals(l) || "huyen_thoai".equals(l) || "su_kien".equals(l)
                    || m.startsWith("TV-");
            if (chuoi && buoc > 1 && (t.isEmpty() || !d.get("cung").equals(cungCua.get(t))) && loi.isEmpty()) {
                loi = m + " buoc " + buoc + " mo coi (truoc khong cung chuoi)";
            }
            if (chuoi && buoc == 1 && !t.isEmpty() && loi.isEmpty()) {
                loi = m + " dau chuoi khong duoc co truoc";
            }
        }
        Kiem.bang("ma/id_so duy nhat; dieu kien truoc la DAG (truoc dung truoc), khong buoc mo coi", "", loi);
    }

    // ------------------------------------------------------------------ ngan sach
    static double gio(final Map<String, String> d) {
        final int L = Math.max(1, Math.min(ThongSoLauDai.CAP_TOI_DA, so(d.get("cap_toi_thieu"))));
        return Long.parseLong(d.get("exp")) / (double) ThongSoLauDai.expMoiGio(L);
    }

    private static void kiemNganSach(final List<Map<String, String>> nv) {
        // Tong cu (bang 721 dong, tools/pokemon/sinh_nhiem_vu.tong_cu): phu+tuyen 69,4 gio EXP, 420 luong khoa;
        // thanh tuu 600 luong khoa + 180 luong.
        double phu = 0, dexMoi = 0, dexCu = 0;
        int lkPhu = 0, lkTt = 0, lgTt = 0;
        boolean suKien = true;
        final Map<String, Double> chuong = new HashMap<String, Double>();
        final Map<String, Double> tran = new HashMap<String, Double>();
        for (final Map<String, String> d : nv) {
            final String l = d.get("loai");
            if ("phu".equals(l)) {
                phu += gio(d);
                lkPhu += so(d.get("luong_khoa"));
            }
            if ("pokedex".equals(l)) {
                dexMoi += gio(d);
                if (d.get("ma").startsWith("D-")) {
                    final int bac = d.get("ma").charAt(d.get("ma").length() - 1) - '0';
                    dexCu += 0.25 * (bac == 1 ? 0.5 : bac == 2 ? 1.0 : 2.0);
                }
            }
            if ("thanh_tuu".equals(l) || "huyen_thoai".equals(l) || "an".equals(l)) {
                lkTt += so(d.get("luong_khoa"));
                lgTt += so(d.get("luong"));
            }
            if ("su_kien".equals(l)) {
                suKien &= "0".equals(d.get("exp")) && "0".equals(d.get("xu")) && "0".equals(d.get("luong_khoa"));
            }
            if ("chinh".equals(l) && "1".equals(d.get("nam"))) {
                chuong.merge(d.get("chuong"), gio(d), Double::sum);
                final int L = so(d.get("cap_toi_thieu"));
                final double h = L < ThongSoLauDai.CAP_TAN_THU ? ThongSoLauDai.HE_SO_TAN_THU_PHAN_TRAM / 100.0 : 1.0;
                tran.put(d.get("chuong"), (0.9 + 0.45) * h);
            }
        }
        boolean ch = true;
        String chuongVuot = "";
        for (final Map.Entry<String, Double> e : chuong.entrySet()) {
            if (e.getValue() > tran.get(e.getKey()) * 1.01) {
                ch = false;
                chuongVuot = e.getKey();
            }
        }
        Kiem.dung("tong EXP phu <= bang cu 69,4 gio (" + String.format("%.1f", phu) + ")", phu <= 69.4 * 1.01);
        Kiem.dung("tong luong khoa phu <= 420 (" + lkPhu + ")", lkPhu <= 420);
        Kiem.dung("tong EXP Pokedex <= tong cua cac dong D- kieu cu (" + String.format("%.1f <= %.1f", dexMoi, dexCu) + ")",
                dexMoi <= dexCu * 1.01);
        Kiem.dung("quy thanh tuu + huyen thoai + an <= 600 luong khoa, 180 luong (" + lkTt + ", " + lgTt + ")",
                lkTt <= 600 && lgTt <= 180);
        Kiem.dung("moi chuong nam 1 <= ngan sach 4 buoc cu " + chuongVuot, ch);
        Kiem.dung("nhiem vu su kien khong them EXP/xu/luong khoa (nam trong ngan sach su kien)", suKien);
    }

    // ------------------------------------------------------------------ goi 1 byte
    private static void kiemGoi(final List<Map<String, String>> nv) throws Exception {
        int maxGoi = 0, capMax = 0;
        for (int L = 1; L <= 100; L++) {
            int n = 0;
            for (final Map<String, String> d : nv) {
                final boolean phu = "phu".equals(d.get("loai")) || "doi".equals(d.get("loai"));
                if (!"1".equals(d.get("dong_co_tuong_thich")) || !phu) {
                    continue;
                }
                final int lv = so(d.get("cap_toi_thieu"));
                if ("doi".equals(d.get("loai")) || (lv <= L && L < lv + 10)) {
                    n++;
                }
            }
            if (n + 1 > maxGoi) {
                maxGoi = n + 1;
                capMax = L;
            }
        }
        Kiem.dung("danh sach gui toi da <= 127 o moi cap (lon nhat " + maxGoi + " o cap " + capMax + ")", maxGoi <= 127);
        final List<Map<String, String>> nd = KiemLauDai.docCsv(D + "nhiem_vu_nhip_do.csv");
        int it = 999;
        for (final Map<String, String> r : nd) {
            it = Math.min(it, so(r.get("lua_chon_y_nghia")));
        }
        Kiem.bang("nhip do co du 100 cap", 100, nd.size());
        Kiem.dung("moi cap >= 10 lua chon co y nghia (it nhat " + it + ")", it >= 10);
    }

    // ------------------------------------------------------------------ file
    private static void kiemFile(final List<Map<String, String>> nv) throws Exception {
        final String sql = new String(Files.readAllBytes(Paths.get("sql/pokemon_nhiem_vu.sql")), StandardCharsets.UTF_8);
        Kiem.dung("pokemon_nhiem_vu.sql: SET NAMES + data_nhiem_vu_noi_dung", sql.contains("SET NAMES utf8;")
                && sql.contains("CREATE TABLE `data_nhiem_vu_noi_dung`"));
        int dong = 0;
        final Matcher m = Pattern.compile("(?m)^\\('").matcher(sql.substring(0, sql.indexOf("DROP TABLE IF EXISTS `data_nhiem_vu_kieu`")));
        while (m.find()) {
            dong++;
        }
        Kiem.bang("data_nhiem_vu_noi_dung du dong", nv.size(), dong);
        Kiem.sai("SQL nhiem vu khong sua bang dang chay", Pattern.compile("(?m)^(UPDATE|DELETE|INSERT INTO `data_quest` )").matcher(sql).find());
        final String xuat = new String(Files.readAllBytes(Paths.get("tools/pokemon/xuat_sql_day_du.sh")), StandardCharsets.UTF_8);
        Kiem.dung("pokemon_nhiem_vu nam trong xuat_sql_day_du.sh", xuat.contains("pokemon_nhiem_vu"));
        final String web = new String(Files.readAllBytes(Paths.get(D + "web/nhiem_vu_tom_tat.json")), StandardCharsets.UTF_8);
        boolean sach = web.contains("\"cot_truyen\"") && !web.contains("\"dap_an\"") && !web.contains("\"id_so\"")
                && !web.contains("\"exp\"") && !web.contains("\"xu\"");
        Kiem.dung("web/nhiem_vu_tom_tat.json: co cot truyen, khong id/ngan sach/dap an", sach);
        final String doc = new String(Files.readAllBytes(Paths.get(D + "NHIEM_VU_THIET_KE.md")), StandardCharsets.UTF_8);
        Kiem.dung("NHIEM_VU_THIET_KE.md co muc 'Yêu cầu engine' va bang da sinh", doc.contains("## 5. Yêu cầu engine")
                && doc.contains("| `ho_tong` |"));
    }

    // ------------------------------------------------------------------ he sinh thai
    /** Muc tieu he sinh thai phai khop docs/pokemon/HE_SINH_THAI.md + cac lop sv.he_sinh_thai. */
    private static void kiemHeSinhThai(final List<Map<String, String>> nv) throws Exception {
        final String vl = new String(Files.readAllBytes(Paths.get("src/com/htbb/pokemon/VatLieuSinhThai.java")),
                StandardCharsets.UTF_8);
        final Set<String> ten = new HashSet<String>();
        final Matcher m = Pattern.compile("dn\\(\\d+, \"([^\"]+)\"").matcher(vl);
        while (m.find()) {
            ten.add(m.group(1));
        }
        final String tg = new String(Files.readAllBytes(Paths.get("src/com/htbb/pokemon/SinhThaiTheGioi.java")),
                StandardCharsets.UTF_8);
        final Map<String, String> loaiMap = new HashMap<String, String>();
        for (final String l : Files.readAllLines(Paths.get(D + "thiet_ke_map.csv"), StandardCharsets.UTF_8)) {
            final String[] o = l.split(";");
            if (!l.startsWith("#") && o.length > 1 && o[0].matches("\\d+")) {
                loaiMap.put(o[0], o[1]);
            }
        }
        final Set<String> khaoCo = new HashSet<String>(java.util.Arrays.asList("hang", "nui", "sa_mac", "tuyet"));
        final List<String> loi = new ArrayList<String>();
        int so = 0;
        for (final Map<String, String> d : nv) {
            final String k = d.get("kieu_muc_tieu");
            final String dk = d.get("dieu_kien");
            final Matcher v = Pattern.compile("vat_sinh_thai=([^;]+)").matcher(dk);
            if (v.find()) {
                so++;
                if (!ten.contains(v.group(1))) {
                    loi.add(d.get("ma") + " vat lieu " + v.group(1));
                }
            } else if (("cau_ca".equals(k) && !dk.contains("vat_nhiem_vu_khi_cau=")) || "lam_vuon".equals(k)
                    || "nau_an".equals(k)) {
                loi.add(d.get("ma") + " thieu vat_sinh_thai");
            }
            final Matcher q = Pattern.compile("dieu_kien_quan_sat=thoi_tiet=([^;]+)").matcher(dk);
            if (q.find() && !tg.contains("\"" + q.group(1) + "\"")) {
                loi.add(d.get("ma") + " thoi tiet " + q.group(1));
            }
            if ("quan_sat".equals(k) && !dk.matches(".*dieu_kien_quan_sat=(thoi_tiet=[^;]+|dem|gio=\\d+-\\d+|su_kien=\\w+).*")) {
                loi.add(d.get("ma") + " dieu kien quan sat " + dk);
            }
            if ("dao_dat".equals(k)) {
                final boolean kc = dk.contains("khao_co=1");
                final boolean dung = khaoCo.contains(loaiMap.get(d.get("map_id")));
                if (kc != dung || (!kc && !dk.contains("diem_dao_nhiem_vu=1"))) {
                    loi.add(d.get("ma") + " khao co tren map " + d.get("map_id"));
                }
            }
        }
        Kiem.dung("co muc tieu dung vat lieu he sinh thai (" + so + ")", so >= 50);
        Kiem.bang("cau ca/vuon/nau an/khao co/quan sat khop HE_SINH_THAI (VatLieuSinhThai, SinhThaiTheGioi, loai map)",
                "[]", loi.toString());
    }
}
