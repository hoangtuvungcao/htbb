import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.htbb.pokemon.ChuPokemon;
import com.htbb.pokemon.TenHien;
import com.htbb.pokemon.TenMapPokemon;

/**
 * TEN NPC + TEN BAN DO nguoi choi thay (P6: du lieu hien thi o SQL data_npc / data_map_ten).
 *
 * VI SAO: chu game bao ten NPC khong dau ("Ba Ngoai") va ten ban do kiem hiep ("Bến bắc" o /admin/online). Kiem
 *  - migration sql/nang_cap.sql (phan 5) = khoi data_npc / data_map_ten trong CSDL day du sql/htbb_day_du.sql (SQL la
 *    NGUON, sua o web /admin/ten-hien; khong con bang ten trong Java, khong bo sinh);
 *  - moi type NPC co tren cMap co ten; NPCNAME client J2ME + Unity = data_npc (giao thuc chua gui ten NPC tinh);
 *  - ten ban do theo dung thiet ke du_lieu/thiet_ke_map.csv (bo dau so khop), Hoenn = BanDoHoenn;
 *  - SAU KHI server ap bang (real.TenBanDoMayChu), MOI ten ban do / NPC server gui duoc (goi 12, 105, xa phu,
 *    /admin/online, NPC su kien -50) co dau tieng Viet (tru danh tu rieng Pokemon), khong tu cam kiem hiep, khong
 *    con ten goc ("Bến bắc", "Dao Châu"...).
 */
public final class KiemTenHien {

    /** id ban do co ten CO Y lech thiet ke: ly do. */
    static final Map<Integer, String> LECH_THIET_KE = new HashMap<Integer, String>();

    static {
        LECH_THIET_KE.put(30, "map 30/31 server la MapLienDau (Sân Liên minh), cMap 30/31 Rung Ilex khong dung id nay");
        LECH_THIET_KE.put(31, "nhu 30");
        LECH_THIET_KE.put(227, "thiet ke 'Loi dai' = lôi đài (tu cam) -> Sàn đấu");
    }

    /** Tu tieng Viet viet DUNG khi khong dau. */
    static final Set<String> KHONG_DAU_HOP_LE = new HashSet<String>(Arrays.asList("khu", "nam", "hang"));

    /** Danh tu rieng (dia danh Pokemon, thuat ngu) - ten chi gom chung + so thi khong can dau. */
    static final Set<String> RIENG = new HashSet<String>(Arrays.asList(
            "pewter", "pallet", "viridian", "vermilion", "cerulean", "lavender", "celadon", "saffron", "fuchsia",
            "cinnabar", "new", "bark", "cherrygrove", "goldenrod", "ecruteak", "violet", "s.s.", "anne", "azalea",
            "olivine", "mahogany", "ilex", "silver", "blackthorn", "cianwood", "indigo", "victory", "safari",
            "littleroot", "petalburg", "rustboro", "dewford", "slateport", "route", "mauville", "lavaridge", "chimney",
            "fortree", "lilycove", "pyre", "mossdeep", "sootopolis", "ever", "grande", "granite", "meteor", "hoenn",
            "kanto", "johto", "zapdos", "darkrai", "salamence", "litleo", "lucario", "pokémon", "poké", "mart"));

    static final String DAU = "àáảãạăằắẳẵặâầấẩẫậèéẻẽẹêềếểễệìíỉĩịòóỏõọôồốổỗộơờớởỡợùúủũụưừứửữựỳýỷỹỵđ";

    public static void chay() {
        Kiem.nhom("Ten NPC + ten ban do (SQL data_npc / data_map_ten, P6)");
        try {
            chayThat();
        } catch (final Exception e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi: " + e, false);
        }
    }

    private static String doc(final String p) throws Exception {
        return new String(Files.readAllBytes(new File(p).toPath()), StandardCharsets.UTF_8);
    }

    private static void chayThat() throws Exception {
        final String sql = doc("../sql/nang_cap.sql");
        final Map<Integer, String[]> npc = dong(sql, "data_npc");
        final Map<Integer, String[]> map = dong(sql, "data_map_ten");
        final String du = doc("../sql/htbb_day_du.sql");
        Kiem.bang("htbb_day_du.sql: data_npc = migration", chuoi(npc), chuoi(dongDump(du, "data_npc", 7)));
        Kiem.bang("htbb_day_du.sql: data_map_ten = migration", chuoi(map), chuoi(dongDump(du, "data_map_ten", 5)));
        Kiem.bang("data_npc du type 0..31", 32, npc.size());
        for (int i = 0; i < 32; i++) {
            Kiem.dung("data_npc co type " + i, npc.containsKey(i));
        }
        for (final String[] r : npc.values()) {
            RIENG.add(r[3].toLowerCase(Locale.ROOT)); // cot pokemon
        }
        for (final String l : doc("../du_lieu/bang_npc_su_kien.csv").split("\n")) {
            final String[] p = l.split(",");
            if (p.length > 6) {
                RIENG.add(p[6].trim().toLowerCase(Locale.ROOT));
            }
        }

        // --- NPC: client = SQL, moi ten hop le
        final List<String> tenNpc = new ArrayList<String>();
        for (int i = 0; i < 32; i++) {
            tenNpc.add(npc.get(i)[1]);
        }
        Kiem.bang("client J2ME NPC.NPCNAME = data_npc", tenNpc, chuoiTrongKhoi(doc("../client_jar/src/game/model/NPC.java"), "NPCNAME = {"));
        Kiem.bang("Unity NPC.cs NPCNAME = data_npc", tenNpc,
                chuoiTrongKhoi(doc("../client_pc_ios/Assets/Scripts/Assembly-CSharp/NPC.cs"), "NPCNAME = new string[32]"));
        final List<String> loi = new ArrayList<String>();
        for (final String t : tenNpc) {
            kiemTen("NPC", t, loi);
            if (!ChuPokemon.doiLuon(t).equals(t)) {
                loi.add("NPC '" + t + "' bi tu dien client doi thanh '" + ChuPokemon.doiLuon(t) + "'");
            }
        }
        // NPC su kien (goi -50): ten viet cung qua ChuPokemon.doi (MessageCreator.createMsgNpc)
        final Pattern p = Pattern.compile("createMsgNpc\\(\\s*\"([^\"]*)\"");
        int soSuKien = 0;
        for (final String f : new String[]{"src/real/Map.java", "src/real/MapLienDau.java", "src/real/plugins/addNPC.java"}) {
            final Matcher m = p.matcher(doc(f).replaceAll("(?m)^\\s*//.*$", "")); // bo dong chu thich
            while (m.find()) {
                soSuKien++;
                kiemTen("NPC su kien (" + f + ")", ChuPokemon.doi(m.group(1)).trim(), loi);
            }
        }
        Kiem.dung("tim thay ten NPC su kien trong ma (>= 10)", soSuKien >= 10);

        // --- moi type NPC dat tren cMap co dong
        final Set<Integer> coTrenMap = new TreeSet<Integer>();
        for (final File f : new File("cMap").listFiles()) {
            if (f.getName().matches("\\d+")) {
                try {
                    for (final com.htbb.pokemon.DinhDangBanDo.Diem d : com.htbb.pokemon.DinhDangBanDo.doc(Files.readAllBytes(f.toPath())).npc) {
                        coTrenMap.add(d.v);
                    }
                } catch (final Exception ignored) {
                }
            }
        }
        coTrenMap.removeAll(npc.keySet());
        final Set<Integer> mapThieu = new TreeSet<Integer>();
        for (final File f : new File("cMap").listFiles()) {
            if (f.getName().matches("\\d+") && !map.containsKey(Integer.parseInt(f.getName()))) {
                mapThieu.add(Integer.parseInt(f.getName()));
            }
        }
        Kiem.bang("moi ban do cMap co dong data_map_ten", "[]", mapThieu.toString());
        Kiem.bang("moi NPC dat tren ban do co ten trong data_npc", "[]", coTrenMap.toString());

        // --- ban do: theo thiet ke
        final Map<Integer, String> thietKe = new TreeMap<Integer, String>();
        for (final String l : doc("../du_lieu/thiet_ke_map.csv").split("\n")) {
            if (!l.isEmpty() && Character.isDigit(l.charAt(0))) {
                final String[] c = l.split(";", -1);
                thietKe.put(Integer.parseInt(c[0].trim()), c[2].trim());
            }
        }
        final List<String> lech = new ArrayList<String>();
        for (final Map.Entry<Integer, String> e : thietKe.entrySet()) {
            final String[] r = map.get(e.getKey());
            if (r == null) {
                lech.add(e.getKey() + ": thieu dong data_map_ten");
                continue;
            }
            if (LECH_THIET_KE.containsKey(e.getKey())) {
                continue;
            }
            final String a = bo(r[1]), b = bo(e.getValue());
            if (!(a.equals(b) || b.startsWith(a + " - ") || b.startsWith(a + " (") || a.startsWith(b + " "))) {
                lech.add(e.getKey() + ": '" + r[1] + "' khac thiet ke '" + e.getValue() + "'");
            }
        }
        Kiem.bang("data_map_ten theo du_lieu/thiet_ke_map.csv", "[]", lech.toString());
        final List<String> lechHoenn = new ArrayList<String>();
        for (int i = 0; i < TenMapPokemon.HOENN_ID.length; i++) {
            final String[] r = map.get(TenMapPokemon.HOENN_ID[i]);
            if (r == null || !r[1].equals(TenMapPokemon.HOENN_TEN[i])) {
                lechHoenn.add(TenMapPokemon.HOENN_ID[i] + "");
            }
        }
        Kiem.bang("data_map_ten Hoenn = BanDoHoenn", "[]", lechHoenn.toString());
        for (final String[] r : map.values()) {
            kiemTen("ban do " + r[0], r[1], loi);
        }

        // --- ap vao bang Map cua server nhu luc bat may
        final Map<Integer, String> mTen = new HashMap<Integer, String>();
        final Map<Integer, String> nTen = new HashMap<Integer, String>();
        for (final Map.Entry<Integer, String[]> e : map.entrySet()) {
            mTen.put(e.getKey(), e.getValue()[1]);
        }
        for (final Map.Entry<Integer, String[]> e : npc.entrySet()) {
            nTen.put(e.getKey(), e.getValue()[1]);
        }
        real.RealController.add_more_idMapSend2Client(); // (khong CSDL: TenHien bao loi to roi giu ten goc)
        TenHien.datBang(mTen, nTen);
        real.TenBanDoMayChu.ap();
        Kiem.bang("2424 (Bến bắc cu) -> ten Pokemon", "Bến tàu phía bắc", real.Map.getNameMap(2424));
        Kiem.bang("301 -> Đông Pewter", "Đông Pewter", real.Map.getNameMap(301));
        Kiem.bang("2541 dong rieng", "Tổ Zapdos", real.TenBanDoMayChu.ten(2541));
        Kiem.bang("/admin/online map trong nha (103)", "Trung tâm Pokémon", real.TenBanDoMayChu.ten(103));
        Kiem.bang("ChuPokemon.tenNpc doc data_npc", "Ông Snorlax", ChuPokemon.tenNpc(8, "ông nội"));
        final Set<String> goc = new HashSet<String>();
        for (final String[] d : TenMapPokemon.BANG) {
            goc.add(bo(d[0]));
        }
        goc.addAll(Arrays.asList("ben bac", "ben dong", "ben tay", "ben nam", "khu dong", "khu tay", "khu nam", "khu bac",
                "hang phia dong", "tyt 5x", "chien truong la ma", "can nui", "nui kho bao", "rung gia"));
        final Set<String> conGoc = new TreeSet<String>();
        int soTen = 0;
        for (final String[] hang : real.Map.namMap) {
            for (final String t : hang) {
                if (t != null && !t.isEmpty()) {
                    soTen++;
                    kiemTen("Map.namMap", t, loi);
                    final String b = bo(t);
                    if (goc.contains(b) || goc.contains(b.replaceFirst("^(dong|tay|nam|bac) ", ""))) {
                        conGoc.add(t); // ten goc (ke ca kem huong "Đông Dao Châu")
                    }
                }
            }
        }
        for (int c = 0; c < 2; c++) {
            for (final String[][] nhom : real.Map.getNameMapLocation(c)) {
                for (final String[] cong : nhom) {
                    for (final String t0 : cong) {
                        final String t = TenMapPokemon.doi(t0); // nhu MessageCreator.createMessageLocation
                        if (t != null && !t.isEmpty()) {
                            soTen++;
                            kiemTen("Map.nameMap (goi 105)", t, loi);
                            if (goc.contains(bo(t))) {
                                conGoc.add(t);
                            }
                        }
                    }
                }
            }
        }
        for (final short[] nhom : realMapId()) {
            for (final short id : nhom) {
                final String t = real.TenBanDoMayChu.ten(id);
                Kiem.dung("co ten cho map " + id, !t.isEmpty());
                kiemTen("TenBanDoMayChu.ten(" + id + ")", t, loi);
            }
        }
        for (final String t : real.MapLienDau.name_town) {
            kiemTen("MapLienDau.name_town", t, loi);
        }
        Kiem.dung("so ten ban do da quet > 300 (" + soTen + ")", soTen > 300);
        Kiem.bang("khong con ten ban do goc kiem hiep trong bang Map", "[]", conGoc.toString());
        Kiem.bang("moi ten NPC / ban do server gui co dau, khong tu cam", "[]", loi.toString());
    }

    private static short[][] realMapId() throws Exception {
        final java.lang.reflect.Field f = real.Map.class.getDeclaredField("mapID");
        f.setAccessible(true);
        return (short[][]) f.get(null);
    }

    /** Ten hop le: co dau tieng Viet, hoac chi gom danh tu rieng / so / tu khong dau dung; khong tu cam. */
    static void kiemTen(final String noi, final String t, final List<String> loi) {
        if (t == null || t.trim().isEmpty()) {
            loi.add(noi + ": ten rong");
            return;
        }
        if (!ChuPokemon.tuCamTrong(t).isEmpty()) {
            loi.add(noi + ": '" + t + "' co tu cam " + ChuPokemon.tuCamTrong(t));
        }
        if (coDau(t)) {
            return;
        }
        for (final String w0 : t.split("[\\s()/,-]+")) {
            final String w = w0.toLowerCase(Locale.ROOT);
            if (w.isEmpty() || w.matches("\\d+x?") || RIENG.contains(w) || KHONG_DAU_HOP_LE.contains(w)) {
                continue;
            }
            loi.add(noi + ": '" + t + "' khong dau tieng Viet (tu '" + w0 + "')");
            return;
        }
    }

    static boolean coDau(final String t) {
        final String s = t.toLowerCase(Locale.ROOT);
        for (int i = 0; i < s.length(); i++) {
            if (DAU.indexOf(s.charAt(i)) >= 0) {
                return true;
            }
        }
        return false;
    }

    /** Bo dau + thuong (so khop voi thiet ke khong dau). */
    static String bo(final String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "").replace('đ', 'd').replace('Đ', 'D')
                .toLowerCase(Locale.ROOT).trim();
    }

    static List<String> chuoiTrongKhoi(final String src, final String dau) {
        final int a = src.indexOf(dau);
        if (a < 0) {
            return new ArrayList<String>();
        }
        final int b = src.indexOf('}', src.indexOf('{', a + dau.length() - 1));
        final List<String> kq = new ArrayList<String>();
        final Matcher m = Pattern.compile("\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(src.substring(a + dau.length(), b));
        while (m.find()) {
            kq.add(m.group(1));
        }
        return kq;
    }

    /** Dong INSERT cua bang {@code bang} trong file SQL sinh: khoa (cot 1) -> moi cot (chuoi bo nhay). */
    static Map<Integer, String[]> dong(final String sql, final String bang) {
        final Map<Integer, String[]> kq = new LinkedHashMap<Integer, String[]>();
        final int a = sql.indexOf("INSERT INTO `" + bang + "`");
        final int b = sql.indexOf("ON DUPLICATE KEY", a);
        if (a < 0 || b < 0) {
            return kq;
        }
        for (final String l : sql.substring(sql.indexOf('\n', a) + 1, b).split("\n")) {
            if (!l.startsWith("(")) {
                continue;
            }
            final List<String> o = new ArrayList<String>();
            final StringBuilder sb = new StringBuilder();
            boolean nhay = false;
            final String x = l.substring(1, l.lastIndexOf(')'));
            for (int i = 0; i < x.length(); i++) {
                final char c = x.charAt(i);
                if (c == '\'') {
                    if (nhay && i + 1 < x.length() && x.charAt(i + 1) == '\'') {
                        sb.append('\'');
                        i++;
                    } else {
                        nhay = !nhay;
                    }
                } else if (c == ',' && !nhay) {
                    o.add(sb.toString());
                    sb.setLength(0);
                } else {
                    sb.append(c);
                }
            }
            o.add(sb.toString());
            kq.put(Integer.parseInt(o.get(0).trim()), o.toArray(new String[0]));
        }
        return kq;
    }

    static String chuoi(final Map<Integer, String[]> m) {
        final StringBuilder sb = new StringBuilder();
        for (final Map.Entry<Integer, String[]> e : new TreeMap<Integer, String[]>(m).entrySet()) {
            sb.append(Arrays.toString(e.getValue())).append('\n');
        }
        return sb.toString();
    }

    /** Dong INSERT kieu mysqldump (mot dong, nhay thoat \') cua {@code bang}: lay {@code soCot} cot dau. */
    static Map<Integer, String[]> dongDump(final String sql, final String bang, final int soCot) {
        final Map<Integer, String[]> kq = new LinkedHashMap<Integer, String[]>();
        final String dau = "INSERT INTO `" + bang + "` VALUES ";
        final int a = sql.indexOf(dau);
        if (a < 0) {
            return kq;
        }
        final String x = sql.substring(a + dau.length(), sql.indexOf(";\n", a));
        List<String> o = new ArrayList<String>();
        final StringBuilder sb = new StringBuilder();
        boolean nhay = false;
        boolean trong = false;
        for (int i = 0; i < x.length(); i++) {
            final char c = x.charAt(i);
            if (nhay) {
                if (c == '\\' && i + 1 < x.length()) {
                    sb.append(x.charAt(++i));
                } else if (c == '\'') {
                    nhay = false;
                } else {
                    sb.append(c);
                }
            } else if (c == '\'') {
                nhay = true;
            } else if (c == '(') {
                trong = true;
                o = new ArrayList<String>();
                sb.setLength(0);
            } else if (trong && (c == ',' || c == ')')) {
                o.add(sb.toString());
                sb.setLength(0);
                if (c == ')') {
                    trong = false;
                    kq.put(Integer.parseInt(o.get(0).trim()), o.subList(0, Math.min(soCot, o.size())).toArray(new String[0]));
                }
            } else if (trong) {
                sb.append(c);
            }
        }
        return kq;
    }
}
