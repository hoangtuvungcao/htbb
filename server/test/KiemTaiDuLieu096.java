import com.htbb.pokemon.BanDoHoenn;
import com.htbb.pokemon.DinhDangBanDo;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 0.9.6.1 - "LOI TAI" (chu game bao 19/10/2026): moi duong TAI cua client phai tra loi duoc, khong treo man cho.
 *
 * LOI THAT TIM DUOC (anh chup ban PC 0.9.6 tren server that, 21:36 19/10/2026): Xep hang -> tab "Bang vang" treo
 * "Dang tai bang xep hang..." mai mai. Man Xep hang (client game.pokemon.ManXepHang, tu 0.9.3) xin top kieu 7 roi
 * CHO mot danh sach -19 kieu 7, nhung server tra menu -30 (-504, 10 muc top theo vung) - man do khong ve menu nen
 * khong bao gio het cho (bot tren server thu: kieu 4 / 3 tra -19, kieu 7 tra -30). Sua hai phia:
 *   - server (real.XepHangBangVang): client &gt;= 903 xin kieu 7 -> tra thang danh sach top cong trang vung minh;
 *     client cu / Unity giu menu;
 *   - client (ManXepHang): tab Bang vang chon thang muc -504 "Top cong trang &lt;vung&gt;" (dung voi ca server cu da
 *     chay), cho toi da CHO_TOI_DA roi thoi "Dang tai", mo lai man thi xin lai.
 *
 * Ngoai ra ghim du lieu tai ban do / tai nguyen (khong CSDL, khong mang) de lan sau sua map / tile / SQL khong tao
 * ra man tai treo: moi bo cuc cMap doc va ghi lai dung tung byte (DinhDangBanDo), chi so tile trong bo .type, vat
 * trang tri co tep map/tree, moi map server nap co cMap + luoi quai + nam trong danh sach gui client (client chi dong
 * goi san map 0: map thieu bo cuc la NullPointerException luc tai), tile / .type / map 0 cua client trung server, va
 * moi id ban do / toa do trong SQL (data_npc, he sinh thai, nhiem vu) tro vao ban do co that.
 */
public final class KiemTaiDuLieu096 {

    private KiemTaiDuLieu096() {
    }

    public static void chay() {
        kiemBangVang();
        kiemBanDo();
        kiemClientDongGoi();
        kiemSql();
    }

    // ------------------------------------------------------------------ Bang vang (man Xep hang)
    private static void kiemBangVang() {
        Kiem.nhom("0.9.6.1 loi tai: Xep hang -> Bang vang khong treo");
        Kiem.bang("client 906 vung Kanto -> top cong trang Kanto (muc 3)", 3, real.XepHangBangVang.muc(906, 0));
        Kiem.bang("client 903 vung Johto -> muc 4", 4, real.XepHangBangVang.muc(903, 1));
        Kiem.bang("client 906 vung Hoenn -> muc 5", 5, real.XepHangBangVang.muc(906, 2));
        Kiem.bang("client 906 chua co vung (-1) -> Kanto", 3, real.XepHangBangVang.muc(906, -1));
        Kiem.bang("Unity / client cu (-1) -> giu menu -504", -1, real.XepHangBangVang.muc(-1, 0));
        Kiem.bang("client 902 (truoc man Xep hang) -> giu menu", -1, real.XepHangBangVang.muc(902, 1));
        try {
            final String xl = doc("src/real/MapXuLyLenh.java");
            final int k7 = xl.indexOf("case 7:", xl.indexOf("static void doSendTop("));
            Kiem.dung("doSendTop kieu 7 goi XepHangBangVang.muc -> doSendTopNation truoc menu", k7 > 0
                    && xl.indexOf("XepHangBangVang.muc(", k7) > k7 && xl.indexOf("doSendTopNation(", k7) < xl.indexOf("createMsgMenu(", k7));
            final String menu = doc("src/real/MapDoMenu.java");
            Kiem.dung("MapDoMenu case -504 -> doSendTopNation (duong client chon thang)",
                    Pattern.compile("case -504:\\s*map\\.doSendTopNation\\(player, idOptionMenu\\)").matcher(menu).find());
            final String m = doc("src/real/Map.java");
            final Matcher t = Pattern.compile("topnation = new String\\[\\]\\{([^}]*)\\}").matcher(m);
            Kiem.dung("Map.topnation co", t.find());
            final String[] ten = t.group(1).split(",");
            Kiem.dung("topnation[3..5] = Top cong trang Kanto / Johto / Hoenn", ten.length >= 6
                    && ten[3].contains("công trạng Kanto") && ten[4].contains("công trạng Johto") && ten[5].contains("công trạng Hoenn"));
            final String c = doc("../client_jar/src/game/pokemon/ManXepHang.java");
            Kiem.dung("client ManXepHang: tab kieu 7 chon thang menu -504 (khong cho menu)",
                    c.contains("NPC_TOP_VUNG = -504") && c.contains("MUC_CONG_TRANG = 3")
                    && c.contains("doMenuOption(NPC_TOP_VUNG, (byte) 0, MUC_CONG_TRANG + vung())"));
            Kiem.dung("client ManXepHang: het thoi gian cho -> thoi 'Dang tai'", c.contains("hetCho(this.tab, System.currentTimeMillis())"));
            Kiem.dung("client ManXepHang: mo lai man -> xin lai (xoa cho / DS)", c.contains("cho[i] = false;"));
        } catch (final Exception e) {
            Kiem.dung("doc ma nguon Bang vang: " + e, false);
        }
    }

    // ------------------------------------------------------------------ ban do
    private static void kiemBanDo() {
        Kiem.nhom("0.9.6.1 loi tai: ban do cMap / luoi quai / danh sach gui client");
        try {
            final int[] soTile = {
                Files.readAllBytes(new File("cMap/t.type").toPath()).length,
                Files.readAllBytes(new File("cMap/t_hang.type").toPath()).length,
                Files.readAllBytes(new File("cMap/t_thanh.type").toPath()).length};
            final File[] tep = new File("cMap").listFiles();
            final Set<Integer> coTep = new TreeSet<Integer>();
            final List<String> docHong = new ArrayList<String>(), tileNgoai = new ArrayList<String>(), cayThieu = new ArrayList<String>();
            for (final File f : tep) {
                if (!f.getName().matches("\\d+")) {
                    continue;
                }
                final int id = Integer.parseInt(f.getName());
                coTep.add(id);
                final byte[] b = Files.readAllBytes(f.toPath());
                final DinhDangBanDo d;
                try {
                    d = DinhDangBanDo.doc(b);
                } catch (final Exception e) {
                    docHong.add(id + ":" + e);
                    continue;
                }
                if (!Arrays.equals(b, d.ghi())) {
                    docHong.add(id + ":ghi lai lech");
                }
                final int n = soTile[id < 110 ? 0 : id > 200 ? 2 : 1];
                int ngoai = 0;
                for (final int t : d.tile) {
                    if (!DinhDangBanDo.trong(t) && t >= n) {
                        ngoai++;
                    }
                }
                for (final DinhDangBanDo.Khoi k : d.khoiTop) {
                    for (final int t : k.tile) {
                        if (!DinhDangBanDo.trong(t) && t >= n) {
                            ngoai++;
                        }
                    }
                }
                for (final DinhDangBanDo.Diem p : d.topLe) {
                    if (!DinhDangBanDo.trong(p.v) && p.v >= n) {
                        ngoai++;
                    }
                }
                if (ngoai > 0) {
                    tileNgoai.add(id + "(" + ngoai + " o)");
                }
                for (final DinhDangBanDo.Diem p : d.cay) {
                    // data.DataGame nap map/tree/0..52; cay ngoai mang -> lenh -48 nem loi, client hoi lai mai
                    if (p.v >= 53 || !new File("map/tree/" + p.v).isFile()) {
                        cayThieu.add(id + ":" + p.v);
                    }
                }
            }
            Kiem.bang("moi cMap/<id> doc duoc va ghi lai dung tung byte", "[]", docHong.toString());
            Kiem.bang("chi so tile < so tile cua bo .type", "[]", tileNgoai.toString());
            Kiem.bang("vat trang tri co tep map/tree/<id> (id < 53)", "[]", cayThieu.toString());

            // map server nap (RealController.initData + vung Hoenn): bo cuc + luoi quai + gui client
            final String rc = doc("src/real/RealController.java");
            final Set<Integer> nap = new TreeSet<Integer>();
            final Matcher m = Pattern.compile("new (\\w+)\\((\\d+),\\s*(-?\\d+),\\s*(\\d+),\\s*(\\d+)").matcher(rc);
            while (m.find()) {
                if (m.group(1).endsWith("Map") || m.group(1).startsWith("Map")) {
                    nap.add(Integer.valueOf(m.group(5)));
                }
            }
            final Set<Integer> hoenn = new TreeSet<Integer>();
            for (final BanDoHoenn.BanDo b : BanDoHoenn.ds()) {
                hoenn.add(b.id);
            }
            Kiem.dung("doc duoc danh sach map nap (" + nap.size() + " ma nap)", nap.size() > 40 && nap.contains(0) && nap.contains(201));
            nap.addAll(hoenn);
            final Set<Integer> guiThem = new TreeSet<Integer>(Arrays.asList(28)); // RealController.add_more_idMapSend2Client
            guiThem.addAll(hoenn); // VungHoenn.dangKyBang
            final List<String> thieuCMap = new ArrayList<String>(), thieuLuoi = new ArrayList<String>(), khongGui = new ArrayList<String>();
            for (final int id : nap) {
                if (!coTep.contains(id)) {
                    thieuCMap.add(String.valueOf(id));
                }
                if (!new File("map/map" + id).isFile()) {
                    thieuLuoi.add(String.valueOf(id));
                }
                if (!real.Map.idMapSend2Client.containsKey((short) id) && !guiThem.contains(id)) {
                    khongGui.add(String.valueOf(id));
                }
            }
            Kiem.bang("moi map nap co cMap/<id>", "[]", thieuCMap.toString());
            Kiem.bang("moi map nap co luoi quai map/map<id>", "[]", thieuLuoi.toString());
            Kiem.bang("moi map nap duoc gui bo cuc cho client (goi 12) - client chi dong goi map 0", "[]", khongGui.toString());
            final List<String> lech = new ArrayList<String>();
            for (final java.util.Map.Entry<Short, byte[]> e : real.Map.idMapSend2Client.entrySet()) {
                final File f = new File("cMap/" + e.getKey());
                if (f.isFile() && !Arrays.equals(e.getValue(), Files.readAllBytes(f.toPath()))) {
                    lech.add(String.valueOf(e.getKey()));
                }
            }
            Kiem.bang("bo cuc gui client = dung tep cMap", "[]", lech.toString());
            // luoi quai nam trong ban do (quai ngoai ban do: client khong toi duoc, bot / nhiem vu treo)
            final List<String> luoiTran = new ArrayList<String>();
            for (final int id : nap) {
                final File f = new File("map/map" + id);
                if (!f.isFile() || !coTep.contains(id)) {
                    continue;
                }
                final DinhDangBanDo.LuoiQuai q = DinhDangBanDo.LuoiQuai.doc(Files.readAllBytes(f.toPath()));
                final DinhDangBanDo d = DinhDangBanDo.doc(Files.readAllBytes(new File("cMap/" + id).toPath()));
                for (int y = 0; y < q.h; y++) {
                    for (int x = 0; x < q.w; x++) {
                        if (q.coQuai(x, y) && (x >= d.w || y >= d.h)) {
                            luoiTran.add(id + ":" + x + "," + y);
                            y = q.h;
                            break;
                        }
                    }
                }
            }
            Kiem.bang("quai trong luoi nam trong ban do", "[]", luoiTran.toString());
        } catch (final Exception e) {
            Kiem.dung("doc ban do: " + e, false);
        }
    }

    // ------------------------------------------------------------------ client dong goi
    private static void kiemClientDongGoi() {
        Kiem.nhom("0.9.6.1 loi tai: tai nguyen dong goi client trung server");
        try {
            final String[][] cap = {
                {"cMap/t.type", "../client_jar/res/t.type"},
                {"cMap/t_hang.type", "../client_jar/res/t_hang.type"},
                {"cMap/t_thanh.type", "../client_jar/res/t_thanh.type"},
                {"cMap/0", "../client_jar/res/0"},
                {"map/pokemon_tile/t.png", "../client_jar/res/pt/t.png"},
                {"map/pokemon_tile/t_small.png", "../client_jar/res/pt/t_small.png"},
                {"map/pokemon_tile/t_hang.png", "../client_jar/res/pt/t_hang.png"},
                {"map/pokemon_tile/t_hang_s.png", "../client_jar/res/pt/t_hang_s.png"},
                {"map/pokemon_tile/t_thanh.png", "../client_jar/res/pt/t_thanh.png"},
                {"map/pokemon_tile/t_thanh_s.png", "../client_jar/res/pt/t_thanh_s.png"}};
            final List<String> lech = new ArrayList<String>();
            for (final String[] c : cap) {
                final File a = new File(c[0]), b = new File(c[1]);
                if (!a.isFile() || !b.isFile() || !Arrays.equals(Files.readAllBytes(a.toPath()), Files.readAllBytes(b.toPath()))) {
                    lech.add(c[1]);
                }
            }
            // client doc .type tu jar (Res.loadTileImage) nhung bo cuc tu server: lech = di xuyen tuong / ket o
            Kiem.bang("client_jar/res (.type, map 0, bo tile) trung server", "[]", lech.toString());
            for (final String t : data.DataGame.duongTileGuiClient()) {
                final long n = new File(t).length();
                Kiem.dung("anh tile gui trong goi dang nhap " + t + " < 32768 byte (" + n + ")", n > 0 && n < 32768);
            }
        } catch (final Exception e) {
            Kiem.dung("doc tai nguyen client: " + e, false);
        }
    }

    // ------------------------------------------------------------------ SQL
    private static void kiemSql() {
        Kiem.nhom("0.9.6.1 loi tai: id ban do / toa do trong SQL tro vao ban do co that");
        try {
            final String sql = doc("../sql/htbb_day_du.sql");
            final List<String> loi = new ArrayList<String>();
            int dem = 0;
            for (final List<String> r : hang(sql, "data_npc")) {
                for (final String s : r.get(6).split(",")) {
                    if (!s.trim().isEmpty()) {
                        dem++;
                        kiemMap("data_npc " + r.get(0), Integer.parseInt(s.trim()), -1, -1, false, loi);
                    }
                }
            }
            for (final List<String> r : hang(sql, "data_hst_vung")) {
                dem++;
                kiemMap("data_hst_vung", Integer.parseInt(r.get(0)), -1, -1, false, loi);
            }
            for (final List<String> r : hang(sql, "data_hst_bung_phat")) {
                dem++;
                kiemMap("data_hst_bung_phat", Integer.parseInt(r.get(0)), -1, -1, false, loi);
            }
            for (final List<String> r : hang(sql, "data_hst_ho_cau")) {
                dem++;
                kiemMap("data_hst_ho_cau", Integer.parseInt(r.get(0)), Integer.parseInt(r.get(3)), Integer.parseInt(r.get(4)), false, loi);
            }
            for (final List<String> r : hang(sql, "data_nhiem_vu_noi_dung")) {
                final String mp = r.get(4), td = r.get(5);
                if (mp.matches("\\d+")) {
                    dem++;
                    int x = -1, y = -1;
                    if (td.matches("\\d+;\\d+")) {
                        x = Integer.parseInt(td.split(";")[0]);
                        y = Integer.parseInt(td.split(";")[1]);
                    }
                    kiemMap("nhiem vu " + r.get(0), Integer.parseInt(mp), x, y, true, loi);
                }
            }
            Kiem.dung("doc duoc id ban do trong SQL (" + dem + " tham chieu)", dem > 500);
            Kiem.bang("moi id ban do / toa do SQL hop le (co cMap, trong ban do, nhiem vu dung o di duoc)", "[]", loi.toString());
        } catch (final Exception e) {
            Kiem.dung("doc SQL: " + e, false);
        }
    }

    private static final java.util.Map<Integer, DinhDangBanDo> BAN_DO = new java.util.HashMap<Integer, DinhDangBanDo>();
    private static byte[][] kieu;

    private static void kiemMap(final String nguon, final int id, final int x, final int y, final boolean diDuoc, final List<String> loi)
            throws Exception {
        DinhDangBanDo d = BAN_DO.get(id);
        if (d == null) {
            final File f = new File("cMap/" + id);
            if (!f.isFile()) {
                loi.add(nguon + ": map " + id + " khong co cMap");
                return;
            }
            d = DinhDangBanDo.doc(Files.readAllBytes(f.toPath()));
            BAN_DO.put(id, d);
        }
        if (x < 0 && y < 0) {
            return;
        }
        if (x < 0 || y < 0 || x >= d.w || y >= d.h) {
            loi.add(nguon + ": (" + x + "," + y + ") ngoai map " + id + " " + d.w + "x" + d.h);
            return;
        }
        if (diDuoc) {
            if (kieu == null) {
                kieu = new byte[][]{
                    Files.readAllBytes(new File("cMap/t.type").toPath()),
                    Files.readAllBytes(new File("cMap/t_hang.type").toPath()),
                    Files.readAllBytes(new File("cMap/t_thanh.type").toPath())};
            }
            final byte[] k = kieu[id < 110 ? 0 : id > 200 ? 2 : 1];
            final int t = d.o(x, y);
            if (DinhDangBanDo.trong(t) || t >= k.length || (k[t] & 2) != 0) {
                loi.add(nguon + ": (" + x + "," + y + ") o chan tren map " + id);
            }
        }
    }

    /** Cac hang cua moi lenh INSERT INTO `bang` VALUES (...),(...); trong dump (gia tri NULL -> chuoi rong). */
    static List<List<String>> hang(final String sql, final String bang) {
        final List<List<String>> ra = new ArrayList<List<String>>();
        final String dau = "INSERT INTO `" + bang + "` VALUES ";
        int i = sql.indexOf(dau);
        while (i >= 0) {
            int p = i + dau.length();
            List<String> r = null;
            StringBuilder o = null;
            boolean chuoi = false;
            for (; p < sql.length(); p++) {
                final char c = sql.charAt(p);
                if (chuoi) {
                    if (c == '\\') {
                        o.append(sql.charAt(++p));
                    } else if (c == '\'') {
                        chuoi = false;
                    } else {
                        o.append(c);
                    }
                } else if (c == '(' && r == null) {
                    r = new ArrayList<String>();
                    o = new StringBuilder();
                } else if (c == '\'') {
                    chuoi = true;
                } else if (c == ',' && r != null) {
                    r.add(o.toString().equals("NULL") ? "" : o.toString());
                    o = new StringBuilder();
                } else if (c == ')' && r != null) {
                    r.add(o.toString().equals("NULL") ? "" : o.toString());
                    ra.add(r);
                    r = null;
                } else if (c == ';' && r == null) {
                    break;
                } else if (r != null) {
                    o.append(c);
                }
            }
            i = sql.indexOf(dau, p);
        }
        return ra;
    }

    private static String doc(final String duong) throws Exception {
        return new String(Files.readAllBytes(new File(duong).toPath()), StandardCharsets.UTF_8);
    }
}
