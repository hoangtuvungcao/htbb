import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MUC TIEU NHIEM VU KHOP VOI MAY CHU (nhanh sua-tien-do-nv; khoi "sua_muc_tieu_nhiem_vu.sql" cua sql/htbb.sql). Kiem du
 * lieu MOI nhiem vu co quai / vat roi voi luoi quai that (server/map/map&lt;N&gt;, nhu MapNapTaiNguyen.loadMap):
 *
 * <ul>
 *   <li>data_quest_moi (chuoi chinh / phu / doi, giao thuc -64): quai muc tieu co trong data_monster va co sinh o it nhat
 *       mot ban do; "… ở &lt;ban do&gt;" + "Địa điểm:" la ban do quai do sinh; so trong chu = so may chu dem
 *       (nmonskill / nitemget, client hien x/N); ten quai / vat pham trong chu = cai may chu dem; vat pham roi khong con
 *       id 0..9 (ten kiem hiep "Lông đuôi Hồng Kê"); ha quai theo cap (type 4): "Hạ N Pokémon hoang dã chênh không quá d
 *       cấp" dung N, d; NPC tra co trong data_npc va dung tren ban do; ban do quai mo (data_ban_do_mo_khoa) o cap buoc
 *       (+3).</li>
 *   <li>data_quest_lau_dai noi voi dong tren (dong_co_tuong_thich = 1, id_so): so_luong / muc tieu / dia_danh khop.</li>
 *   <li>Nhiem vu engine (So tay: ngay / tuan / cua ai / huyen thoai...): quai muc tieu sinh o dia_danh (neu la ten ban
 *       do) va o ban_do=&lt;id&gt; cua cua ai; so trong muc_tieu_text = so_luong.</li>
 *   <li>Monster.java: dem theo mau quai (idTemplate), vat nhiem vu roi &gt;= 10% moi lan ha.</li>
 * </ul>
 * Du lieu = phan A cua sql/htbb.sql + khoi sua_muc_tieu_nhiem_vu.sql ap trong bo nho (moi lenh khop dung MOT dong cua phan
 * A, ap lan hai khong doi gi). In ra danh sach loi TRUOC khi ap khoi (de doi chieu) - sau khi ap phai rong.
 */
final class KiemMucTieuNhiemVu {

    private KiemMucTieuNhiemVu() {
    }

    static final String KHOI = "sua_muc_tieu_nhiem_vu.sql";
    static final String[] TEN_CU = {"Nọc bọ cạp", "Rắn", "Khúc gỗ", "Rết đỏ", "Lá thư", "Thịt heo", "Lông đuôi Hồng Kê",
        "Móng cá sấu", "Da ếch", "Cựa gà"};
    static final Set<String> KIEU_QUAI = new TreeSet<String>(java.util.Arrays.asList("giet", "giet_tinh_anh", "ha_alpha",
            "thu_thach_gio", "thu_thap"));

    public static void chay() {
        Kiem.nhom("Muc tieu nhiem vu: quai sinh dung ban do, so dem khop chu, vat roi, NPC tra (khoi " + KHOI + ")");
        try {
            chayThat();
        } catch (final Exception e) {
            e.printStackTrace(System.out);
            Kiem.dung("KiemMucTieuNhiemVu chay het: " + e, false);
        }
    }

    // ------------------------------------------------------------------ du lieu

    static Map<Integer, Set<Integer>> sinh; // mau quai -> ban do (id nap)
    static Map<Integer, String> tenQuai;
    static Map<Integer, String> tenMap;
    static Map<String, Set<Integer>> mapTheoTen;
    static Map<Integer, String> tenVatPham;
    static Map<Integer, Integer> capMo;
    static Map<Integer, String> npcBanDo;

    static List<Map<String, String>> dongThuong(final String bang) {
        final List<Map<String, String>> ra = new ArrayList<Map<String, String>>();
        for (final Map<String, String> d : DayDu.dong(bang)) {
            final Map<String, String> m = new LinkedHashMap<String, String>();
            for (final Map.Entry<String, String> e : d.entrySet()) {
                m.put(e.getKey().toLowerCase(), e.getValue() == null ? "" : e.getValue());
            }
            ra.add(m);
        }
        return ra;
    }

    /** Luoi quai server/map/map&lt;N&gt; (u8 w, u8 h, w*h mau; 0 / 255 trong; map 17 mau &gt; 81 +3) + quai dat tay. */
    static Map<Integer, Set<Integer>> napSinh() throws Exception {
        final Map<Integer, Set<Integer>> m = new TreeMap<Integer, Set<Integer>>();
        final File[] tep = new File("map").listFiles();
        for (final File f : tep == null ? new File[0] : tep) {
            final String n = f.getName();
            if (!n.matches("map\\d+") || !f.isFile()) {
                continue;
            }
            final int id = Integer.parseInt(n.substring(3));
            final byte[] b = Files.readAllBytes(f.toPath());
            if (b.length < 2) {
                continue;
            }
            final int w = b[0] & 0xff;
            final int h = b[1] & 0xff;
            for (int i = 0; i < w * h && 2 + i < b.length; i++) {
                int v = b[2 + i] & 0xff;
                if (v == 0 || v == 255) {
                    continue;
                }
                if (id == 17 && v > 81) {
                    v += 3;
                }
                them(m, v, id);
            }
        }
        them(m, 136, 28); // MapNapTaiNguyen.loadMap: quai dat tay map 28
        them(m, 83, 201); // map 201 (cong thanh)
        return m;
    }

    static void them(final Map<Integer, Set<Integer>> m, final int k, final int v) {
        Set<Integer> s = m.get(Integer.valueOf(k));
        if (s == null) {
            m.put(Integer.valueOf(k), s = new TreeSet<Integer>());
        }
        s.add(Integer.valueOf(v));
    }

    static int so(final String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (final RuntimeException e) {
            return Integer.MIN_VALUE;
        }
    }

    static void napBang() throws Exception {
        sinh = napSinh();
        tenQuai = new HashMap<Integer, String>();
        for (final Map<String, String> d : dongThuong("data_monster")) {
            tenQuai.put(Integer.valueOf(so(d.get("id"))), d.get("name"));
        }
        tenMap = new HashMap<Integer, String>();
        mapTheoTen = new HashMap<String, Set<Integer>>();
        for (final Map<String, String> d : dongThuong("data_map_ten")) {
            final int id = so(d.get("map_id"));
            final String t = d.get("ten").trim();
            tenMap.put(Integer.valueOf(id), t);
            Set<Integer> s = mapTheoTen.get(t);
            if (s == null) {
                mapTheoTen.put(t, s = new TreeSet<Integer>());
            }
            s.add(Integer.valueOf(id));
        }
        tenVatPham = new HashMap<Integer, String>();
        for (int i = 0; i < TEN_CU.length; i++) {
            tenVatPham.put(Integer.valueOf(i), TEN_CU[i]);
        }
        for (final Map<String, String> d : dongThuong("data_vat_pham_nhiem_vu")) {
            tenVatPham.put(Integer.valueOf(so(d.get("id"))), d.get("ten"));
        }
        capMo = new HashMap<Integer, Integer>();
        for (final Map<String, String> d : dongThuong("data_ban_do_mo_khoa")) {
            capMo.put(Integer.valueOf(so(d.get("map_id"))), Integer.valueOf(so(d.get("cap_toi_thieu"))));
        }
        npcBanDo = new HashMap<Integer, String>();
        for (final Map<String, String> d : dongThuong("data_npc")) {
            npcBanDo.put(Integer.valueOf(so(d.get("type"))), d.get("ban_do").trim());
        }
    }

    /** Ban do (id) ten {@code ten} co sinh quai {@code mau}. */
    static boolean sinhO(final int mau, final String ten) {
        final Set<Integer> s = sinh.get(Integer.valueOf(mau));
        final Set<Integer> m = mapTheoTen.get(ten == null ? "" : ten.trim());
        if (s == null || m == null) {
            return false;
        }
        for (final Integer x : m) {
            if (s.contains(x)) {
                return true;
            }
        }
        return false;
    }

    /** Cap mo som nhat cua ban do ten {@code ten} co sinh quai {@code mau} (0 = khong khoa). */
    static int capMoO(final int mau, final String ten) {
        int c = Integer.MAX_VALUE;
        final Set<Integer> s = sinh.get(Integer.valueOf(mau));
        for (final Integer x : mapTheoTen.get(ten.trim())) {
            if (s.contains(x)) {
                final Integer k = capMo.get(x);
                c = Math.min(c, k == null ? 0 : k.intValue());
            }
        }
        return c;
    }

    // ------------------------------------------------------------------ ap khoi phan B

    static final Pattern P_LENH = Pattern.compile("^UPDATE `(\\w+)` SET (.+) WHERE (.+);$");

    /** Tach "`c` = 'v', `d` = 5" (va "AND" o WHERE) thanh cot -> gia tri. */
    static LinkedHashMap<String, String> cap(final String s, final String noi) {
        final LinkedHashMap<String, String> m = new LinkedHashMap<String, String>();
        int i = 0;
        while (i < s.length()) {
            while (i < s.length() && s.charAt(i) == ' ') {
                i++;
            }
            if (s.startsWith(noi, i)) {
                i += noi.length();
                continue;
            }
            if (i >= s.length()) {
                break;
            }
            if (s.charAt(i) != '`') {
                throw new IllegalArgumentException("lenh sai dang: " + s);
            }
            final int e = s.indexOf('`', i + 1);
            final String cot = s.substring(i + 1, e).toLowerCase();
            i = s.indexOf('=', e) + 1;
            while (s.charAt(i) == ' ') {
                i++;
            }
            final StringBuilder v = new StringBuilder();
            if (s.charAt(i) == '\'') {
                i++;
                while (true) {
                    final char c = s.charAt(i);
                    if (c == '\\') {
                        v.append(s.charAt(i + 1));
                        i += 2;
                    } else if (c == '\'' && i + 1 < s.length() && s.charAt(i + 1) == '\'') {
                        v.append('\'');
                        i += 2;
                    } else if (c == '\'') {
                        i++;
                        break;
                    } else {
                        v.append(c);
                        i++;
                    }
                }
            } else {
                while (i < s.length() && (Character.isDigit(s.charAt(i)) || s.charAt(i) == '-')) {
                    v.append(s.charAt(i++));
                }
            }
            m.put(cot, v.toString());
        }
        return m;
    }

    /** Ap khoi len bang (id / ma); tra ve so dong khop cua tung lenh. */
    static List<Integer> ap(final String khoi, final Map<String, List<Map<String, String>>> bang) {
        final List<Integer> khop = new ArrayList<Integer>();
        for (final String l : khoi.split("\n")) {
            if (!l.startsWith("UPDATE ")) {
                continue;
            }
            final Matcher m = P_LENH.matcher(l);
            if (!m.matches()) {
                throw new IllegalArgumentException("lenh ngoai dang cho phep: " + l);
            }
            final List<Map<String, String>> ds = bang.get(m.group(1));
            if (ds == null) {
                throw new IllegalArgumentException("bang ngoai khoi: " + m.group(1));
            }
            final LinkedHashMap<String, String> dat = cap(m.group(2), ",");
            final LinkedHashMap<String, String> dk = cap(m.group(3), "AND ");
            int n = 0;
            for (final Map<String, String> d : ds) {
                boolean ok = true;
                for (final Map.Entry<String, String> e : dk.entrySet()) {
                    ok &= e.getValue().equals(d.get(e.getKey()));
                }
                boolean doi = false;
                for (final Map.Entry<String, String> e : dat.entrySet()) {
                    doi |= !e.getValue().equals(d.get(e.getKey()));
                }
                if (ok && doi) {
                    d.putAll(dat);
                    n++;
                }
            }
            khop.add(Integer.valueOf(n));
        }
        return khop;
    }

    // ------------------------------------------------------------------ kiem

    static final Pattern P_HA = Pattern.compile("^(?:Hạ|Đánh bại) (\\d+) (.+?)(?: mạnh)? ở (.+)$");
    static final Pattern P_THU = Pattern.compile("^Thu (\\d+) (.+?) \\(rơi từ (.+?)\\) ở (.+)$");
    static final Pattern P_CAP = Pattern.compile("^Hạ (\\d+) Pokémon hoang dã chênh không quá (\\d+) cấp");
    static final Pattern P_DD = Pattern.compile("Địa điểm:\\|([^|]*)\\|");

    /** Danh sach loi (rong = dat). {@code canCua}: buoc can cua ai / khoa ban do (thong tin). */
    static List<String> kiem(final List<Map<String, String>> qm, final List<Map<String, String>> ql,
            final List<Map<String, String>> nd, final List<String> canCua) {
        final List<String> loi = new ArrayList<String>();
        final Map<String, Map<String, String>> noi = new HashMap<String, Map<String, String>>();
        for (final Map<String, String> l : ql) {
            if ("1".equals(l.get("dong_co_tuong_thich"))) {
                noi.put(l.get("id_so"), l);
            }
        }
        for (final Map<String, String> q : qm) {
            final int id = so(q.get("id"));
            final String ms = q.get("mainsub");
            final int t = so(q.get("type"));
            final String sc = q.get("scontent").trim();
            final String inf = q.get("infogift");
            final String nhan = id + " " + q.get("name") + ": ";
            // NPC tra (moi nhiem vu)
            int npc = so(q.get("npcres"));
            if (npc == 32000) {
                npc = so(q.get("npcrcv"));
            }
            if (npc >= 0 && npc != 32000 && (npcBanDo.get(Integer.valueOf(npc)) == null || npcBanDo.get(Integer.valueOf(npc)).isEmpty())) {
                loi.add(nhan + "NPC tra " + npc + " khong co / khong dung tren ban do nao");
            }
            if (id <= 910 || !("0".equals(ms) || "1".equals(ms) || "2".equals(ms))) {
                continue; // 901..910: huong dan tan thu (HuongDanTanThuGame dem rieng, KiemHuongDanTanThu)
            }
            final Matcher mdd = P_DD.matcher(inf);
            final String dd = mdd.find() ? mdd.group(1).trim() : "";
            if (t == 4) {
                final Matcher m = P_CAP.matcher(sc);
                if (!m.find() || so(m.group(1)) != so(q.get("nmonskill")) || so(m.group(2)) != so(q.get("deltalv"))) {
                    loi.add(nhan + "\"" + sc + "\" != may chu dem ha " + q.get("nmonskill") + " quai chenh <= " + q.get("deltalv") + " cap");
                }
                continue;
            }
            if (t != 0 && t != 2) {
                continue;
            }
            final String[] ids = q.get("idmonskill").split(",");
            if (ids.length != 1) {
                loi.add(nhan + "nhieu muc tieu (" + q.get("idmonskill") + ") - kiem chua ho tro");
                continue;
            }
            final int mau = so(ids[0]);
            final String tq = tenQuai.get(Integer.valueOf(mau));
            if (tq == null) {
                loi.add(nhan + "quai " + mau + " khong co trong data_monster");
                continue;
            }
            if (sinh.get(Integer.valueOf(mau)) == null) {
                loi.add(nhan + "quai " + tq + " khong sinh o ban do nao");
                continue;
            }
            final int can;
            final String mapChu;
            if (t == 0) {
                can = so(q.get("nmonskill"));
                final Matcher m = P_HA.matcher(sc);
                if (!m.matches() || so(m.group(1)) != can || !m.group(2).equals(tq)) {
                    loi.add(nhan + "\"" + sc + "\" != may chu dem ha " + can + " " + tq);
                    continue;
                }
                mapChu = m.group(3).trim();
            } else {
                can = so(q.get("nitemget"));
                final int vp = so(q.get("iditemget"));
                final String tv = tenVatPham.get(Integer.valueOf(vp));
                if (vp < 10 || tv == null) {
                    loi.add(nhan + "vat pham roi " + vp + " (" + tv + ") la vat pham cu / khong co ten");
                    continue;
                }
                final Matcher m = P_THU.matcher(sc);
                if (!m.matches() || so(m.group(1)) != can || !m.group(2).equals(tv) || !m.group(3).equals(tq)) {
                    loi.add(nhan + "\"" + sc + "\" != may chu dem nhat " + can + " " + tv + " roi tu " + tq);
                    continue;
                }
                mapChu = m.group(4).trim();
            }
            if (!sinhO(mau, mapChu)) {
                loi.add(nhan + tq + " khong sinh o " + mapChu + " (sinh o " + tenCac(sinh.get(Integer.valueOf(mau))) + ")");
                continue;
            }
            if (!dd.equals(mapChu)) {
                loi.add(nhan + "Địa điểm " + dd + " != " + mapChu);
            }
            final int mo = capMoO(mau, mapChu);
            if (mo > so(q.get("lv")) + 3) {
                loi.add(nhan + mapChu + " mo tu cap " + mo + " > cap buoc " + q.get("lv"));
            }
            for (final Integer x : mapTheoTen.get(mapChu)) {
                if (sinh.get(Integer.valueOf(mau)).contains(x) && capMo.containsKey(x) && capMo.get(x).intValue() > 0) {
                    canCua.add(id + " cap " + q.get("lv") + " " + sc + " (" + mapChu + " mo tu cap " + capMo.get(x) + ")");
                    break;
                }
            }
            final Map<String, String> l = noi.get(q.get("id"));
            if (l != null && ("giet".equals(l.get("kieu_muc_tieu")) || "thu_thap".equals(l.get("kieu_muc_tieu")))) {
                final boolean thu = "thu_thap".equals(l.get("kieu_muc_tieu"));
                if (so(l.get("so_luong")) != can) {
                    loi.add(nhan + "data_quest_lau_dai " + l.get("ma") + " so_luong " + l.get("so_luong") + " != " + can);
                }
                if (so(thu ? l.get("quai_roi") : l.get("muc_tieu_id")) != mau || thu && so(l.get("muc_tieu_id")) != so(q.get("iditemget"))) {
                    loi.add(nhan + "data_quest_lau_dai " + l.get("ma") + " muc tieu khac data_quest_moi");
                }
                if (!sinhO(mau, l.get("dia_danh"))) {
                    loi.add(nhan + "data_quest_lau_dai " + l.get("ma") + " dia_danh " + l.get("dia_danh") + " khong co " + tq);
                }
            }
        }
        // ---- nhiem vu engine (So tay)
        final Map<String, Map<String, String>> noiDung = new HashMap<String, Map<String, String>>();
        for (final Map<String, String> d : nd) {
            noiDung.put(d.get("ma"), d);
        }
        final Pattern pBd = Pattern.compile("(?:^|;)\\s*ban_do\\s*=\\s*(\\d+)");
        for (final Map<String, String> l : ql) {
            if ("1".equals(l.get("dong_co_tuong_thich")) || !KIEU_QUAI.contains(l.get("kieu_muc_tieu"))) {
                continue;
            }
            final boolean thu = "thu_thap".equals(l.get("kieu_muc_tieu"));
            final String mt = thu ? l.get("quai_roi") : l.get("muc_tieu_id");
            final String nhan = l.get("ma") + " " + l.get("ten") + ": ";
            final List<Integer> ds = new ArrayList<Integer>();
            for (final String x : mt.split("[,|]")) {
                if (x.trim().matches("\\d+")) {
                    ds.add(Integer.valueOf(x.trim()));
                }
            }
            if (ds.isEmpty()) {
                continue; // muc tieu rong = moi quai (khop() rong -> true)
            }
            if ("cua_ai".equals(l.get("loai")) && so(l.get("so_luong")) == 1 && !sinh.containsKey(ds.get(0))) {
                continue; // cua ai ha Gym: muc_tieu_id = stt Gym
            }
            final int mau = ds.get(0).intValue();
            if (!sinh.containsKey(Integer.valueOf(mau))) {
                loi.add(nhan + "quai " + mau + " khong sinh o ban do nao");
                continue;
            }
            if (mapTheoTen.containsKey(l.get("dia_danh").trim()) && !sinhO(mau, l.get("dia_danh"))) {
                loi.add(nhan + "dia_danh " + l.get("dia_danh") + " khong co " + tenQuai.get(Integer.valueOf(mau)) + " (sinh o "
                        + tenCac(sinh.get(Integer.valueOf(mau))) + ")");
            }
            final Map<String, String> n = noiDung.get(l.get("ma"));
            if (n != null) {
                final Matcher b = pBd.matcher(n.get("dieu_kien"));
                if (b.find() && !sinh.get(Integer.valueOf(mau)).contains(Integer.valueOf(so(b.group(1))))) {
                    loi.add(nhan + "chi dem o ban do " + b.group(1) + " nhung " + tenQuai.get(Integer.valueOf(mau)) + " khong sinh o do");
                }
                final Matcher s = Pattern.compile("^(?:Hạ|Thu|Đánh bại) (\\d+) ").matcher(n.get("muc_tieu_text"));
                if (s.find() && so(s.group(1)) != so(l.get("so_luong"))) {
                    loi.add(nhan + "\"" + n.get("muc_tieu_text") + "\" != so_luong " + l.get("so_luong"));
                }
            }
        }
        return loi;
    }

    static String tenCac(final Set<Integer> s) {
        final Set<String> t = new TreeSet<String>();
        for (final Integer x : s) {
            t.add(tenMap.containsKey(x) ? tenMap.get(x) : String.valueOf(x));
        }
        return t.toString();
    }

    static List<Map<String, String>> sao(final List<Map<String, String>> ds) {
        final List<Map<String, String>> r = new ArrayList<Map<String, String>>();
        for (final Map<String, String> d : ds) {
            r.add(new LinkedHashMap<String, String>(d));
        }
        return r;
    }

    static void chayThat() throws Exception {
        napBang();
        Kiem.dung("luoi quai: co mau quai sinh (" + sinh.size() + ")", sinh.size() > 100);
        Kiem.dung("Weedle (11) chi sinh o Lavender, Litwick (6) o Cerulean, Pewter (0) khong co quai",
                "[5]".equals(String.valueOf(sinh.get(11))) && "[4]".equals(String.valueOf(sinh.get(6)))
                && !sinhO(11, "Pewter"));
        final List<Map<String, String>> qm = dongThuong("data_quest_moi");
        final List<Map<String, String>> ql = dongThuong("data_quest_lau_dai");
        final List<Map<String, String>> nd = dongThuong("data_nhiem_vu_noi_dung");
        Kiem.dung("doc du data_quest_moi / data_quest_lau_dai / data_nhiem_vu_noi_dung", qm.size() > 700 && ql.size() > 1500 && nd.size() > 500);

        // truoc khi sua (phan A): in de doi chieu
        final List<String> truoc = kiem(qm, ql, nd, new ArrayList<String>());
        System.out.println("  [KiemMucTieuNhiemVu] phan A (truoc khoi " + KHOI + "): " + truoc.size() + " loi");
        for (int i = 0; i < truoc.size(); i++) {
            System.out.println("    truoc: " + truoc.get(i));
        }

        // ap khoi
        final String khoi = DayDu.phanNangCap(KHOI);
        Kiem.dung("co khoi " + KHOI + " o phan B", khoi.length() > 1000);
        final Map<String, List<Map<String, String>>> bang = new HashMap<String, List<Map<String, String>>>();
        final List<Map<String, String>> qm2 = sao(qm);
        final List<Map<String, String>> ql2 = sao(ql);
        bang.put("data_quest_moi", qm2);
        bang.put("data_quest_lau_dai", ql2);
        int saiDang = 0;
        for (final String l : khoi.split("\n")) {
            if (!l.isEmpty() && !l.startsWith("--") && !P_LENH.matcher(l).matches()) {
                saiDang++;
            }
        }
        Kiem.bang("khoi chi gom UPDATE `bang` SET ... WHERE khoa AND gia tri cu (mot dong / lenh)", Integer.valueOf(0), Integer.valueOf(saiDang));
        final List<Integer> lan1 = ap(khoi, bang);
        final List<Integer> lech = new ArrayList<Integer>();
        for (int i = 0; i < lan1.size(); i++) {
            if (lan1.get(i).intValue() != 1) {
                lech.add(Integer.valueOf(i + 1));
            }
        }
        Kiem.dung("khoi co lenh (" + lan1.size() + ")", lan1.size() > 100);
        Kiem.bang("moi lenh khop dung mot dong phan A (gia tri cu dung)", "[]", lech.toString());
        int lai = 0;
        for (final Integer n : ap(khoi, bang)) {
            lai += n.intValue();
        }
        Kiem.bang("ap lan hai khong doi gi (chay lai an toan)", Integer.valueOf(0), Integer.valueOf(lai));

        final List<String> canCua = new ArrayList<String>();
        final List<String> sau = kiem(qm2, ql2, nd, canCua);
        Kiem.bang("sau khoi: moi nhiem vu - quai sinh dung ban do, so / ten khop may chu dem, NPC tra, ban do mo dung cap", "[]",
                sau.toString());
        System.out.println("  [KiemMucTieuNhiemVu] buoc can mo ban do bang cua ai / cap (thong tin, may chu nhac): " + canCua.size());
        for (int i = 0; i < Math.min(12, canCua.size()); i++) {
            System.out.println("    can mo: " + canCua.get(i));
        }

        // ca nguoi choi bao
        final Map<String, String> c032 = tim(qm2, "1014");
        Kiem.bang("C03.2 Gom hàng rơi: may chu dem 5 (client 0/5)", "5", c032.get("nitemget"));
        Kiem.bang("C03.2: chu", "Thu 5 Gói hàng (rơi từ Litwick) ở Cerulean", c032.get("scontent"));
        final Map<String, String> p042 = tim(qm2, "2044");
        Kiem.bang("P-04.0.2 Chứng tỏ sức mạnh: Weedle ở Lavender", "Hạ 20 Weedle ở Lavender", p042.get("scontent"));
        Kiem.dung("P-04.0.2: Địa điểm Lavender", p042.get("infogift").contains("Địa điểm:|Lavender|"));

        // ma nguon: dem theo mau quai, ti le roi vat nhiem vu
        final String mon = new String(Files.readAllBytes(Paths.get("src/real/Monster.java")), StandardCharsets.UTF_8);
        // 29/10/2026: roi vat qua bao hiem xui (checkDropItemQuestBaoHiem, ti le tiLeNv) - KiemCanBangCapLop
        Kiem.dung("Monster: dem nhiem vu theo mau quai (idTemplate)", mon.contains("p.checkFinishQuest(this.idTemplate, true, this.level);")
                && mon.contains("p.checkDropItemQuestBaoHiem(this.idTemplate,"));
        final Matcher r = Pattern.compile("final int tiLeNv = (\\d+)").matcher(mon);
        Kiem.dung("Monster: vat nhiem vu roi >= 10% moi lan ha (" + (r.find() ? r.group(1) : "?") + "/1e6)",
                r.find(0) && Integer.parseInt(r.group(1)) >= 100000);
    }

    static Map<String, String> tim(final List<Map<String, String>> ds, final String id) {
        for (final Map<String, String> d : ds) {
            if (id.equals(d.get("id"))) {
                return d;
            }
        }
        return new HashMap<String, String>();
    }
}
