import com.htbb.pokemon.BanDoHoenn;
import com.htbb.pokemon.ChuThuongNhiemVu;
import com.htbb.pokemon.DinhDangBanDo;
import com.htbb.pokemon.KhuVucNhiemVu;
import com.htbb.pokemon.LangKhoiDauLuat;
import data.InfoQuestTemplate;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import real.Char;
import real.KhuVucNhiemVuGame;
import real.NewCharQuest;

/**
 * SUA TUYEN (nhanh sua-tuyen, khoi sua_tuyen_nhiem_vu.sql cua sql/htbb.sql): ba loi kiem toan huong dan tan thu.
 *  1. Lang khoi dau theo vung: Kanto -> Pewter, Johto -> Blackthorn (truoc NGUOC), cong / diem xuat hien theo lang, nhan
 *     vat cu giu bang cu, NPC huong dan o ca ba lang.
 *  2. Buoc dia diem: chi xong khi dung trong vung dich (luat thuan + real.KhuVucNhiemVuGame voi nhan vat gia), moi vung
 *     dung ban do, o di duoc + toi duoc tu cong; ban do khong toi duoc thi tat (giu noi chuyen la xong) co ly do.
 *  3. Chu phan thuong: cot 1000, khong con dong bi cat, doan "Phần thưởng" = sinh tu dong thuong (ChuThuongNhiemVu).
 * Doc sql/htbb.sql + khoi them_huong_dan_tan_thu.sql / sua_tuyen_nhiem_vu.sql cua sql/htbb.sql, server/cMap, ma nguon. Bot dau-cuoi:
 * tools/pokemon/kiem_tai/KichBanSuaTuyen (st_*).
 */
public final class KiemSuaTuyen {

    private KiemSuaTuyen() {
    }

    public static void chay() {
        Kiem.nhom("Sua tuyen: lang theo vung, buoc dia diem (vung dich di duoc), chu phan thuong day du = dong thuong");
        try {
            lang();
            luatKhuVuc();
            gameKhuVuc();
            final String du = DayDu.sql();
            final String hd = DayDu.phanNangCap("them_huong_dan_tan_thu.sql"); // khoi cua sql/htbb.sql
            final String st = DayDu.phanNangCap("sua_tuyen_nhiem_vu.sql");
            duLieuKhuVuc(du, st);
            duLieuThuong(du, hd, st);
        } catch (final Exception e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi: " + e, false);
        }
    }

    static String doc(final String p) throws Exception {
        return new String(Files.readAllBytes(new File(p).toPath()), StandardCharsets.UTF_8);
    }

    // ------------------------------------------------------------------ 1. lang khoi dau

    static void lang() throws Exception {
        final int[][] dung = LangKhoiDauLuat.bang(true);
        final int[][] cu = LangKhoiDauLuat.bang(false);
        Kiem.bang("Kanto -> Pewter (0 / 301)", "[0, 301]", Arrays.toString(dung[LangKhoiDauLuat.KANTO]));
        Kiem.bang("Johto -> Blackthorn (70 / 1701)", "[70, 1701]", Arrays.toString(dung[LangKhoiDauLuat.JOHTO]));
        Kiem.bang("Hoenn (dong luc tao) -> Cianwood nhu cu", "[80, 1901]", Arrays.toString(dung[LangKhoiDauLuat.HOENN]));
        Kiem.bang("nhan vat cu giu bang goc (Kanto Blackthorn, Johto Pewter)", "[[70, 1701], [0, 301], [80, 1901]]", Arrays.deepToString(cu));
        // vung dia ly cua lang theo CSDL ten (data_map_ten)
        final String tenDay = DayDu.sql(); // data_map_ten trong dump day du (mot tep SQL)
        final String ten = tenDay.substring(tenDay.indexOf("INSERT INTO `data_map_ten`"));
        for (final int v : new int[]{LangKhoiDauLuat.KANTO, LangKhoiDauLuat.JOHTO}) {
            final int l = LangKhoiDauLuat.langCua(v, true);
            final Matcher m = Pattern.compile("\\(" + l + ",\\s*'([^']*)',\\s*'([^']*)'").matcher(ten);
            Kiem.dung("lang cua vung " + LangKhoiDauLuat.TEN_VUNG[v] + " (ban do " + l + ") thuoc " + LangKhoiDauLuat.TEN_VUNG[v]
                    + " trong data_map_ten", m.find() && LangKhoiDauLuat.TEN_VUNG[v].equals(m.group(2)));
        }
        // cong cac map ve lang (Map.getLocaltion): o cong cua vung = lang cua vung
        final String map = doc("src/real/Map.java");
        final Map<Integer, Integer> congLang = new HashMap<Integer, Integer>();
        final Matcher mc = Pattern.compile("idCountry == (\\d)\\) \\{\\s*location\\[1\\]\\[0\\] = new short\\[\\]\\{(\\d+),").matcher(map);
        while (mc.find()) {
            congLang.put(Integer.valueOf(mc.group(1)), Integer.valueOf(mc.group(2)));
        }
        Kiem.bang("Map.getLocaltion co cong ve lang cho 3 o", 3, congLang.size());
        for (int v = 0; v < 3; v++) {
            Kiem.bang("cong ve lang khop lang (vung " + v + ", nhan vat moi)", Integer.valueOf(LangKhoiDauLuat.langCua(v, true)),
                    congLang.get(Integer.valueOf(LangKhoiDauLuat.oCong(v, true))));
            Kiem.bang("cong ve lang khop lang (vung " + v + ", nhan vat cu)", Integer.valueOf(LangKhoiDauLuat.langCua(v, false)),
                    congLang.get(Integer.valueOf(LangKhoiDauLuat.oCong(v, false))));
        }
        // diem xuat hien di duoc trong dung lang
        for (final int l : new int[]{0, 70, 80, 301, 1701}) {
            final int[] p = LangKhoiDauLuat.viTri(l);
            final int nap = LangKhoiDauLuat.napCuaLang(l);
            final boolean[] di = diDuoc(nap);
            final DinhDangBanDo b = banDo(nap);
            String hong = "";
            for (int i = 0; i + 1 < p.length; i += 2) {
                boolean co = false;
                for (int dx = -1; dx <= 1 && !co; dx++) {
                    for (int dy = -1; dy <= 1 && !co; dy++) {
                        final int x = p[i] + dx;
                        final int y = p[i + 1] + dy;
                        co = x >= 0 && y >= 0 && x < b.w && y < b.h && di[y * b.w + x];
                    }
                }
                if (!co) {
                    hong += p[i] + "," + p[i + 1] + " ";
                }
            }
            Kiem.bang("diem xuat hien lang " + l + " di duoc", "", hong.trim());
        }
        Kiem.sai("viTri: khong phai lang", LangKhoiDauLuat.viTri(1) != null);
        // ma nguon: khong con bang lang viet cung / vi tri theo vung
        final List<String> conBang = new ArrayList<String>();
        for (final File f : javaTrong(new File("src"))) {
            final String s = doc(f.getPath());
            if (f.getName().equals("LangKhoiDauLuat.java")) {
                continue;
            }
            if (s.contains("{70, 1701}, {0, 301}") || s.contains("{{10, 23, 14, 38, 30, 35, 21, 49}, {22, 41")) {
                conBang.add(f.getName());
            }
        }
        Kiem.bang("khong con bang lang {{70,1701},{0,301},..} / bang diem xuat hien viet cung (moi noi qua real.LangKhoiDau)", "[]", conBang.toString());
        Kiem.dung("tao nhan vat ghi lang dung vung", doc("src/data/DatabaseNhanVat.java").contains("real.LangKhoiDau.ghiNhanVatMoi(charId, nation)"));
        Kiem.dung("dang nhap nap co lang", doc("src/real/CharNapDuLieu.java").contains("real.LangKhoiDau.nap(self)"));
        Kiem.dung("goi vi tri cong theo lang cua nhan vat", !Pattern.compile("createMessageLocation\\(\\w+\\.(my|in)Country\\)")
                .matcher(doc("src/real/MapDoMenu.java") + doc("src/real/CharNapDuLieu.java") + doc("src/real/MapXuLyLenh.java")).find());
        // NPC huong dan + chuong dau o ca ba lang
        for (final int l : new int[]{0, 70, 80}) {
            final Set<Integer> co = new HashSet<Integer>();
            for (final DinhDangBanDo.Diem n : banDo(l).npc) {
                co.add(Integer.valueOf(n.v));
            }
            String thieu = "";
            for (final int t : new int[]{5, 21, 0, 1, 2, 22, 7, 8}) { // Xatu Blaziken Chansey Kecleon Machamp Meowth Pidgeot Snorlax
                if (!co.contains(Integer.valueOf(t))) {
                    thieu += t + " ";
                }
            }
            Kiem.bang("NPC huong dan du o lang " + l, "", thieu.trim());
        }
        // chu huong dan khong gan voi mot lang cu the (dung o lang nao cung dung)
        final String hd = DayDu.phanNangCap("them_huong_dan_tan_thu.sql"); // khoi cua sql/htbb.sql
        final int a = hd.indexOf("INSERT INTO `data_quest_moi`");
        final String khoi = hd.substring(a, hd.indexOf("ON DUPLICATE KEY", a));
        Kiem.sai("10 buoc huong dan khong nhac ten lang Pewter / Blackthorn / Cianwood",
                khoi.contains("Pewter") || khoi.contains("Blackthorn") || khoi.contains("Cianwood"));
    }

    static List<File> javaTrong(final File d) {
        final List<File> kq = new ArrayList<File>();
        final File[] ds = d.listFiles();
        if (ds != null) {
            for (final File f : ds) {
                if (f.isDirectory()) {
                    kq.addAll(javaTrong(f));
                } else if (f.getName().endsWith(".java")) {
                    kq.add(f);
                }
            }
        }
        return kq;
    }

    // ------------------------------------------------------------------ 2a. luat vung

    static void luatKhuVuc() {
        final KhuVucNhiemVu.Vung v = new KhuVucNhiemVu.Vung("C08.5", 13, 27, 27, 33, 33, false, "Núi Mặt Trăng");
        final List<KhuVucNhiemVu.Vung> ds = Arrays.asList(v);
        Kiem.dung("trong vung: tam", KhuVucNhiemVu.trong(ds, 13, 30, 30));
        Kiem.dung("trong vung: mep", KhuVucNhiemVu.trong(ds, 13, 27, 33));
        Kiem.sai("ngoai vung 1 o", KhuVucNhiemVu.trong(ds, 13, 26, 30));
        Kiem.sai("dung o nhung khac ban do", KhuVucNhiemVu.trong(ds, 14, 30, 30));
        Kiem.bang("cach mep", 7, v.cach(20, 30));
        Kiem.bang("huong Dong", "phía Đông", KhuVucNhiemVu.huong(5, 0));
        Kiem.bang("huong Bac (y giam)", "phía Bắc", KhuVucNhiemVu.huong(0, -4));
        Kiem.bang("huong Tay Nam", "phía Tây Nam", KhuVucNhiemVu.huong(-3, 3));
        Kiem.bang("chi duong cung ban do", "còn khoảng 7 ô về phía Đông (ô 30,30)", KhuVucNhiemVu.chiDuong(ds, 13, 20, 30, null));
        Kiem.bang("chi duong khac ban do", "hãy tới Núi Mặt Trăng (ô 30,30)", KhuVucNhiemVu.chiDuong(ds, 2, 5, 5, "Núi Mặt Trăng"));
        Kiem.bang("chi duong da toi", "đã tới nơi", KhuVucNhiemVu.chiDuong(ds, 13, 30, 31, null));
        final KhuVucNhiemVu.Vung ca = new KhuVucNhiemVu.Vung("C03.3", 2, 0, 0, 255, 255, false, "Viridian");
        Kiem.dung("den_noi: ca ban do", KhuVucNhiemVu.laCaBanDo(ca) && KhuVucNhiemVu.trong(Arrays.asList(ca), 2, 140, 3));
        Kiem.bang("den_noi khac ban do: chi ten", "hãy tới Viridian", KhuVucNhiemVu.chiDuong(Arrays.asList(ca), 1, 5, 5, "Viridian"));
        // nhieu vung (ba lang): dung lang nao cung duoc
        final List<KhuVucNhiemVu.Vung> lang = Arrays.asList(new KhuVucNhiemVu.Vung("P", 0, 10, 7, 16, 13, false, "làng"),
                new KhuVucNhiemVu.Vung("P", 70, 10, 7, 16, 13, false, "làng"));
        Kiem.dung("ba lang: dung lang Blackthorn", KhuVucNhiemVu.trong(lang, 70, 12, 9));
        Kiem.dung("ba lang: gan nhat chon vung cua ban do dang dung", KhuVucNhiemVu.ganNhat(lang, 70, 1, 1).map == 70);
    }

    // ------------------------------------------------------------------ 2b. game: nhan vat gia

    static void gameKhuVuc() {
        final HashMap<String, List<KhuVucNhiemVu.Vung>> m = new HashMap<String, List<KhuVucNhiemVu.Vung>>();
        m.put("ST.1", new ArrayList<KhuVucNhiemVu.Vung>(Arrays.asList(new KhuVucNhiemVu.Vung("ST.1", 13, 27, 27, 33, 33, false, "Núi"))));
        m.put("ST.2", new ArrayList<KhuVucNhiemVu.Vung>(Arrays.asList(new KhuVucNhiemVu.Vung("ST.2", 13, 27, 27, 33, 33, true, "Núi"))));
        KhuVucNhiemVuGame.dat(m);
        final real.GemTemplate[] gemCu = real.Map.gemTemplate;
        if (real.Map.gemTemplate == null) {
            real.Map.gemTemplate = new real.GemTemplate[0]; // Char(null) can mang ngoc (giong KiemDanhHieu)
        }
        try {
            final InfoQuestTemplate a = mau("ST.1", (short) 0);
            final InfoQuestTemplate b = mau("ST.2", (short) 1);
            final InfoQuestTemplate kv = mau("KHAC", (short) 2);
            final InfoQuestTemplate giet = mau("ST.1", (short) 3);
            giet.type = 0;
            Kiem.dung("buoc noi chuyen co vung -> la buoc dia diem", KhuVucNhiemVuGame.cua(a) != null);
            Kiem.sai("buoc khong co vung -> buoc thuong", KhuVucNhiemVuGame.cua(kv) != null);
            Kiem.sai("buoc ha quai (type 0) khong bao gio la buoc dia diem", KhuVucNhiemVuGame.cua(giet) != null);
            final Char p = new Char(null);
            final real.Map ban = new real.Map();
            ban.mapIDLoadMap = 13;
            p.map = ban;
            p.mapID = 13;
            p.x = 10 * 16;
            p.y = 10 * 16;
            final NewCharQuest q = new NewCharQuest(true);
            q.state_quest = NewCharQuest.UN_RECEIVE;
            Kiem.dung("chua nhan + ngoai vung: nghe NPC chua xong (chuaToi)", KhuVucNhiemVuGame.chuaToi(p, q, a));
            q.state_quest = NewCharQuest.WORKING;
            Kiem.dung("dang lam + ngoai vung: tra bi chan", KhuVucNhiemVuGame.chuaToi(p, q, a));
            Kiem.dung("mo ta dang lam co chi duong", KhuVucNhiemVuGame.moTaDangLam(p, q, a).contains("còn khoảng 17 ô"));
            p.x = 30 * 16 + 5;
            p.y = 29 * 16;
            Kiem.sai("dang lam + TRONG vung: tra duoc", KhuVucNhiemVuGame.chuaToi(p, q, a));
            p.x = 30 * 16;
            p.y = 30 * 16;
            ban.mapIDLoadMap = 14;
            Kiem.dung("dung o (30,30) ban do KHAC: van chua toi", KhuVucNhiemVuGame.chuaToi(p, q, a));
            ban.mapIDLoadMap = 13;
            q.state_quest = NewCharQuest.FINISH;
            p.x = 0;
            p.y = 0;
            Kiem.sai("da toi (trang thai du) roi di cho khac: noi chuyen tra duoc", KhuVucNhiemVuGame.chuaToi(p, q, a));
            // di chuyen vao vung voi buoc "toi roi noi chuyen": trang thai -> du, chua tra
            final NewCharQuest q2 = new NewCharQuest(true);
            q2.id_quest = 1;
            q2.main_sub = 1;
            q2.state_quest = NewCharQuest.WORKING;
            real.Map.allSubQuest.put(Short.valueOf((short) 1), b);
            p.subQuest = q2;
            p.mainQuest = new NewCharQuest(true);
            p.x = 12 * 16;
            p.y = 12 * 16;
            KhuVucNhiemVuGame.khiDiChuyen(p);
            Kiem.bang("di ngoai vung: van dang lam", Integer.valueOf(NewCharQuest.WORKING), Integer.valueOf(q2.state_quest));
            p.x = 28 * 16;
            p.y = 32 * 16;
            KhuVucNhiemVuGame.khiDiChuyen(p);
            Kiem.bang("vao vung (noi_chuyen_sau): da toi = du, cho noi chuyen", Integer.valueOf(NewCharQuest.FINISH), Integer.valueOf(q2.state_quest));
            KhuVucNhiemVuGame.khiDiChuyen(p);
            Kiem.bang("di tiep trong vung: khong doi them", Integer.valueOf(NewCharQuest.FINISH), Integer.valueOf(q2.state_quest));
            real.Map.allSubQuest.remove(Short.valueOf((short) 1));
        } finally {
            KhuVucNhiemVuGame.dat(null);
            real.Map.gemTemplate = gemCu;
        }
    }

    static InfoQuestTemplate mau(final String ma, final short id) {
        final InfoQuestTemplate t = new InfoQuestTemplate();
        t.id = id;
        t.type = 3;
        t.ma = ma;
        t.name = "Thử";
        t.shortContent = "Đứng tại điểm";
        t.main_sub = 1;
        t.idItemgift = new short[]{32000};
        t.potiongift = new short[]{32000};
        t.npotiongift = new short[]{32000};
        return t;
    }

    // ------------------------------------------------------------------ 2c. du lieu vung dich

    static final Set<Integer> LANG = new HashSet<Integer>(Arrays.asList(0, 70, 80));

    static void duLieuKhuVuc(final String du, final String st) throws Exception {
        // CSDL day du (sau ca khoi mo_duong_dia_diem.sql cua ban-098: mo duong / doi ban do mot so buoc)
        final List<List<String>> kv = dong(du, "data_nhiem_vu_khu_vuc");
        Kiem.dung("co bang vung dich (>= 120 dong)", kv.size() >= 120);
        final Map<String, List<List<String>>> theoMa = new LinkedHashMap<String, List<List<String>>>();
        for (final List<String> r : kv) {
            theoMa.computeIfAbsent(r.get(0), k -> new ArrayList<List<String>>()).add(r);
        }
        // buoc dia diem trong CSDL day du
        final Map<String, List<String>> ld = new HashMap<String, List<String>>();
        for (final List<String> r : dong(du, "data_quest_lau_dai")) {
            ld.put(r.get(0), r);
        }
        final Map<String, List<String>> nd = new HashMap<String, List<String>>();
        for (final List<String> r : dong(du, "data_nhiem_vu_noi_dung")) {
            nd.put(r.get(0), r);
        }
        final Set<Integer> moi = new HashSet<Integer>();
        for (final List<String> r : dong(du, "data_quest_moi")) {
            moi.add(Integer.valueOf(r.get(0).trim()));
        }
        int chuoi = 0;
        int chuoiBat = 0;
        final List<String> thieu = new ArrayList<String>();
        final List<String> saiMap = new ArrayList<String>();
        for (final List<String> r : ld.values()) {
            final String kieu = r.get(15);
            if (!"dung_toa_do".equals(kieu) && !"den_noi".equals(kieu)) {
                continue;
            }
            final String ma = r.get(0);
            final boolean laChuoi = moi.contains(Integer.valueOf(r.get(1).trim()));
            final List<List<String>> v = theoMa.get(ma);
            if (v == null) {
                thieu.add(ma);
                continue;
            }
            if (laChuoi) {
                chuoi++;
                if ("1".equals(v.get(0).get(8))) {
                    chuoiBat++;
                }
            }
            final List<String> n = nd.get(ma);
            final int mapGoc = Integer.parseInt(n.get(4).trim());
            final Set<Integer> duoc = new HashSet<Integer>();
            duoc.add(Integer.valueOf(mapGoc));
            if (LANG.contains(Integer.valueOf(mapGoc))) {
                duoc.addAll(LANG);
            }
            for (final int[] nha : NHA_LANG) {
                if (nha[0] == mapGoc || nha[1] == mapGoc || nha[2] == mapGoc) {
                    duoc.add(nha[0]);
                    duoc.add(nha[1]);
                    duoc.add(nha[2]);
                }
            }
            for (final List<String> d : v) {
                if (!duoc.contains(Integer.valueOf(d.get(2).trim()))) {
                    saiMap.add(ma + "@" + d.get(2));
                }
            }
            if (duoc.size() > 1 && v.size() != duoc.size()) {
                saiMap.add(ma + " (lang: " + v.size() + " vung / " + duoc.size() + " ban do)");
            }
        }
        Kiem.bang("moi buoc dung_toa_do / den_noi deu co vung dich", "[]", thieu.toString());
        Kiem.dung("~70 buoc chuoi -64 (chinh + phu) co vung (" + chuoi + ")", chuoi >= 60 && chuoi <= 80);
        Kiem.dung("da so buoc chuoi bat (" + chuoiBat + "/" + chuoi + ")", chuoiBat >= chuoi - 15);
        Kiem.bang("vung nam tren dung ban do cua buoc (lang / nha trong lang: du ca ba)", "[]", saiMap.toString());

        // o di duoc + toi duoc tu cong; ban do toi duoc trong the gioi
        final Set<Integer> toi = toiDuoc(du);
        final List<String> loi = new ArrayList<String>();
        final List<String> tat = new ArrayList<String>();
        for (final List<String> r : kv) {
            final String ma = r.get(0);
            final int mp = Integer.parseInt(r.get(2).trim());
            final int x1 = Integer.parseInt(r.get(3).trim());
            final int y1 = Integer.parseInt(r.get(4).trim());
            final int x2 = Integer.parseInt(r.get(5).trim());
            final int y2 = Integer.parseInt(r.get(6).trim());
            final boolean bat = "1".equals(r.get(8).trim());
            if (!bat) {
                tat.add(ma);
                if (r.get(10).trim().isEmpty()) {
                    loi.add(ma + " tat ma khong ghi ly do");
                }
                if (toi.contains(Integer.valueOf(mp))) {
                    loi.add(ma + " tat nhung ban do " + mp + " toi duoc");
                }
                continue;
            }
            if (!new File("cMap/" + mp).isFile()) {
                loi.add(ma + " ban do " + mp + " khong co cMap");
                continue;
            }
            if (!toi.contains(Integer.valueOf(mp)) && !LANG.contains(Integer.valueOf(mp)) && !laNha(mp)) {
                loi.add(ma + " ban do " + mp + " khong toi duoc");
            }
            final DinhDangBanDo b = banDo(mp);
            final boolean[] vao = vao(mp);
            boolean co = false;
            for (int y = Math.max(0, y1); y <= Math.min(b.h - 1, y2) && !co; y++) {
                for (int x = Math.max(0, x1); x <= Math.min(b.w - 1, x2) && !co; x++) {
                    co = vao[y * b.w + x];
                }
            }
            if (!co) {
                loi.add(ma + "@" + mp + " khung " + x1 + "," + y1 + "-" + x2 + "," + y2 + " khong co o di duoc toi tu cong");
            }
            final boolean caBanDo = x1 <= 0 && y1 <= 0 && x2 >= 250 && y2 >= 250;
            if (!caBanDo) {
                final int cx = (x1 + x2) / 2;
                final int cy = (y1 + y2) / 2;
                if (x2 >= b.w + 3 || y2 >= b.h + 3) {
                    loi.add(ma + "@" + mp + " khung ra ngoai ban do " + b.w + "x" + b.h);
                }
                final boolean tamGiua = x1 > 0 && y1 > 0 && x2 < b.w - 1 && y2 < b.h - 1;
                if (tamGiua && !vao[cy * b.w + cx]) {
                    loi.add(ma + "@" + mp + " o tam " + cx + "," + cy + " khong di duoc / khong toi duoc");
                }
            }
        }
        Kiem.bang("moi vung bat: ban do co, toi duoc, co o di duoc lien thong voi cong (tam di duoc)", "[]", loi.toString());
        Kiem.dung("ban do tat chi la ban do khong toi duoc (" + new TreeSet<String>(tat) + ")", tat.size() <= 20);
        // chu "(ô x,y)" cua buoc dung_toa_do bat khop tam vung tren ban do goc
        final List<String> lechChu = new ArrayList<String>();
        final Map<String, String> toaMoi = new HashMap<String, String>(); // CSDL day du da co toa_do moi
        for (final Map.Entry<String, List<List<String>>> e : theoMa.entrySet()) {
            final List<String> n = nd.get(e.getKey());
            if (n == null || !"dung_toa_do".equals(ld.get(e.getKey()).get(15)) || n.get(5).trim().isEmpty()) {
                continue;
            }
            final List<String> d = e.getValue().get(0);
            final String tam = (Integer.parseInt(d.get(3).trim()) + Integer.parseInt(d.get(5).trim())) / 2 + ","
                    + (Integer.parseInt(d.get(4).trim()) + Integer.parseInt(d.get(6).trim())) / 2;
            final String chu = toaMoi.containsKey(e.getKey()) ? toaMoi.get(e.getKey()) : n.get(5).trim().replace(';', ',');
            final boolean mep = Integer.parseInt(d.get(3).trim()) == 0 || Integer.parseInt(d.get(4).trim()) == 0;
            if (!mep && !tam.equals(chu)) {
                lechChu.add(e.getKey() + " chu " + chu + " / vung " + tam);
            }
        }
        Kiem.bang("o trong chu = tam vung (o da doi thi chu doi theo)", "[]", lechChu.toString());
    }

    static final int[][] NHA_LANG = {{29, 31, 30}, {102, 72, 82}, {103, 73, 83}, {104, 74, 84}, {108, 78, 88}, {109, 79, 89}};

    static boolean laNha(final int m) {
        for (final int[] n : NHA_LANG) {
            if (n[0] == m || n[1] == m || n[2] == m) {
                return true;
            }
        }
        return false;
    }

    static final Map<Integer, DinhDangBanDo> BD = new HashMap<Integer, DinhDangBanDo>();
    static final Map<Integer, boolean[]> DI = new HashMap<Integer, boolean[]>();
    static final Map<Integer, boolean[]> VAO = new HashMap<Integer, boolean[]>();

    static DinhDangBanDo banDo(final int id) throws Exception {
        DinhDangBanDo b = BD.get(Integer.valueOf(id));
        if (b == null) {
            b = DinhDangBanDo.doc(Files.readAllBytes(new File("cMap/" + id).toPath()));
            BD.put(Integer.valueOf(id), b);
        }
        return b;
    }

    /** Giong KiemHoiSinh.diDuoc: tile khong rong va (type & 2) == 0. */
    static boolean[] diDuoc(final int id) throws Exception {
        boolean[] di = DI.get(Integer.valueOf(id));
        if (di == null) {
            final DinhDangBanDo m = banDo(id);
            final byte[] k = Files.readAllBytes(new File("cMap/" + DinhDangBanDo.boTile(id) + ".type").toPath());
            di = new boolean[m.w * m.h];
            for (int i = 0; i < di.length; i++) {
                final int t = m.tile[i];
                di[i] = !DinhDangBanDo.trong(t) && t < k.length && (k[t] & 2) == 0;
            }
            DI.put(Integer.valueOf(id), di);
        }
        return di;
    }

    /** O di duoc lien thong 4 huong voi o (<= 2) canh mot cong; khong co cong -> vung lon nhat. */
    static boolean[] vao(final int id) throws Exception {
        boolean[] v = VAO.get(Integer.valueOf(id));
        if (v != null) {
            return v;
        }
        final DinhDangBanDo m = banDo(id);
        final boolean[] di = diDuoc(id);
        v = new boolean[di.length];
        final ArrayDeque<Integer> q = new ArrayDeque<Integer>();
        for (final DinhDangBanDo.Cong c : m.cong) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dy = -2; dy <= 2; dy++) {
                    final int x = c.x + dx;
                    final int y = c.y + dy;
                    if (x >= 0 && y >= 0 && x < m.w && y < m.h && di[y * m.w + x] && !v[y * m.w + x]) {
                        v[y * m.w + x] = true;
                        q.add(Integer.valueOf(y * m.w + x));
                    }
                }
            }
        }
        if (q.isEmpty()) {
            boolean[] tot = v;
            int nTot = 0;
            final boolean[] tham = new boolean[di.length];
            for (int i = 0; i < di.length; i++) {
                if (di[i] && !tham[i]) {
                    final boolean[] c = new boolean[di.length];
                    c[i] = true;
                    final int n = lan(m, di, c, new ArrayDeque<Integer>(Arrays.asList(Integer.valueOf(i))));
                    for (int j = 0; j < c.length; j++) {
                        tham[j] |= c[j];
                    }
                    if (n > nTot) {
                        nTot = n;
                        tot = c;
                    }
                }
            }
            v = tot;
        } else {
            lan(m, di, v, q);
        }
        VAO.put(Integer.valueOf(id), v);
        return v;
    }

    static int lan(final DinhDangBanDo m, final boolean[] di, final boolean[] v, final ArrayDeque<Integer> q) {
        int n = q.size();
        while (!q.isEmpty()) {
            final int c = q.poll().intValue();
            final int x = c % m.w;
            final int y = c / m.w;
            final int[][] hg = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            for (final int[] h : hg) {
                final int nx = x + h[0];
                final int ny = y + h[1];
                if (nx >= 0 && ny >= 0 && nx < m.w && ny < m.h && di[ny * m.w + nx] && !v[ny * m.w + nx]) {
                    v[ny * m.w + nx] = true;
                    n++;
                    q.add(Integer.valueOf(ny * m.w + nx));
                }
            }
        }
        return n;
    }

    /**
     * Ban do toi duoc tu lang (Kanto / Johto), mo hinh doc lap voi bo sinh: cong cua cMap -> bang cong RealController
     * (Map.mapID nhom / Map.locationMap), cong thang vao ban do offline, Pidgeot tu lang, Lapras, Hoenn (BanDoHoenn tu 9).
     */
    static Set<Integer> toiDuoc(final String du) throws Exception {
        final String rc = doc("src/real/RealController.java");
        final List<List<int[]>> loc = new ArrayList<List<int[]>>();
        final String khLoc = khoi(rc, "Map.locationMap = new short[][][]");
        for (final String g : nhom(khLoc)) {
            final List<int[]> l = new ArrayList<int[]>();
            for (final String a : mang(g)) {
                l.add(so(a));
            }
            loc.add(l);
        }
        final List<int[]> mid = new ArrayList<int[]>();
        for (final String a : mang(khoi(rc, "Map.mapID = new short[][]"))) {
            mid.add(so(a));
        }
        final Map<Integer, Integer> grp = new HashMap<Integer, Integer>();
        for (int i = 0; i < mid.size(); i++) {
            for (final int x : mid.get(i)) {
                grp.putIfAbsent(Integer.valueOf(x), Integer.valueOf(i));
            }
        }
        final Set<Integer> lapras = new HashSet<Integer>();
        for (final List<String> r : dong(du, "data_npc")) {
            if ("10".equals(r.get(0).trim())) {
                for (final String x : r.get(6).split(",")) {
                    if (!x.trim().isEmpty()) {
                        lapras.add(Integer.valueOf(x.trim()));
                    }
                }
            }
        }
        final Set<Integer> pidgeot = new HashSet<Integer>(Arrays.asList(3, 4, 5, 6, 7, 8, 9, 24));
        final Set<Integer> arcanine = new HashSet<Integer>();
        {
            final Matcher ma = Pattern.compile("\\(4,'[^']*','[^']*','Arcanine',\\d+,\\d+,'([0-9,]*)'").matcher(du);
            if (ma.find()) {
                for (final String x : ma.group(1).split(",")) {
                    if (!x.trim().isEmpty()) {
                        arcanine.add(Integer.valueOf(x.trim()));
                    }
                }
            }
        }
        final Map<Integer, List<Integer>> hoenn = new HashMap<Integer, List<Integer>>();
        for (final BanDoHoenn.BanDo b : BanDoHoenn.ds()) {
            final List<Integer> den = new ArrayList<Integer>();
            for (final BanDoHoenn.Cong c : b.cong) {
                den.add(Integer.valueOf(c.den));
            }
            hoenn.put(Integer.valueOf(b.id), den);
        }
        final Set<Integer> kq = new HashSet<Integer>();
        for (final int lang : new int[]{0, 70}) {
            final Set<Integer> seen = new HashSet<Integer>();
            final ArrayDeque<Integer> q = new ArrayDeque<Integer>();
            seen.add(Integer.valueOf(lang));
            q.add(Integer.valueOf(lang));
            while (!q.isEmpty()) {
                final int m = q.poll().intValue();
                final Set<Integer> ke = new HashSet<Integer>();
                if (new File("cMap/" + m).isFile()) {
                    final Integer g = grp.get(Integer.valueOf(m));
                    for (final DinhDangBanDo.Cong c : banDo(m).cong) {
                        if (g != null && g.intValue() < loc.size() && c.toM < loc.get(g.intValue()).size()) {
                            ke.add(Integer.valueOf(loc.get(g.intValue()).get(c.toM)[0]));
                        } else if (new File("cMap/" + c.toM).isFile() && banDo(c.toM).offline) {
                            ke.add(Integer.valueOf(c.toM));
                        }
                    }
                }
                if (hoenn.containsKey(Integer.valueOf(m))) {
                    ke.addAll(hoenn.get(Integer.valueOf(m)));
                }
                if (m == BanDoHoenn.MAP_CONG_VAO) {
                    ke.add(Integer.valueOf(43));
                }
                if (lapras.contains(Integer.valueOf(m))) {
                    ke.addAll(lapras);
                }
                if (LANG.contains(Integer.valueOf(m))) {
                    ke.addAll(pidgeot);
                }
                // 0.9.8 (ban-098): Arcanine gac cong (data_npc loai 4) dan toi khu xa real.NpcDichVu.LOI_DI
                if (arcanine.contains(Integer.valueOf(m))) {
                    for (final int[] d : real.NpcDichVuKiem.loiDi()) {
                        ke.add(Integer.valueOf(d[0]));
                    }
                }
                for (final Integer d : ke) {
                    if (LANG.contains(d) || laNha(d.intValue())) {
                        continue; // lang / nha cua phe khac: cong ep ve lang minh
                    }
                    if (seen.add(d) && new File("cMap/" + d).isFile()) {
                        q.add(d);
                    }
                }
            }
            kq.addAll(seen);
        }
        kq.remove(Integer.valueOf(111)); // Hang Darkrai: chi mo trong su kien He (MapXuLyLenh)
        kq.remove(Integer.valueOf(206)); // chi thanh vien doi
        return kq;
    }

    static String khoi(final String s, final String dau) {
        int i = s.indexOf(dau) + dau.length();
        while (s.charAt(i) != '{') {
            i++;
        }
        int d = 0;
        for (int k = i; k < s.length(); k++) {
            if (s.charAt(k) == '{') {
                d++;
            } else if (s.charAt(k) == '}' && --d == 0) {
                return s.substring(i, k + 1);
            }
        }
        throw new IllegalStateException(dau);
    }

    static List<String> nhom(final String b) {
        final List<String> out = new ArrayList<String>();
        final String in = b.substring(1, b.length() - 1);
        int d = 0;
        int st = 0;
        for (int k = 0; k < in.length(); k++) {
            if (in.charAt(k) == '{') {
                if (d == 0) {
                    st = k;
                }
                d++;
            } else if (in.charAt(k) == '}' && --d == 0) {
                out.add(in.substring(st, k + 1));
            }
        }
        return out;
    }

    static List<String> mang(final String t) {
        final List<String> kq = new ArrayList<String>();
        final Matcher m = Pattern.compile("\\{([-0-9, ]+)\\}").matcher(t);
        while (m.find()) {
            kq.add(m.group(1));
        }
        return kq;
    }

    static int[] so(final String a) {
        final List<Integer> l = new ArrayList<Integer>();
        for (final String x : a.split(",")) {
            if (!x.trim().isEmpty()) {
                l.add(Integer.valueOf(x.trim()));
            }
        }
        final int[] r = new int[l.size()];
        for (int i = 0; i < r.length; i++) {
            r[i] = l.get(i).intValue();
        }
        return r;
    }

    // ------------------------------------------------------------------ 3. chu phan thuong

    static void duLieuThuong(final String du, final String hd, final String st) throws Exception {
        Kiem.dung("noi cot infogift len varchar(1000)", st.contains("ALTER TABLE `data_quest_moi` MODIFY `infogift` varchar(1000)"));
        Kiem.dung("huong dan tan thu khong con cat infogift 200", !hd.contains("), 200) WHERE `id`")
                && hd.contains("ALTER TABLE `data_quest_moi` MODIFY `infogift` varchar(1000)"));
        final Map<Integer, String> tenVp = new HashMap<Integer, String>();
        for (final List<String> r : dong(du, "data_item")) {
            tenVp.put(Integer.valueOf(r.get(0).trim()), r.get(1));
        }
        final Map<Integer, String> tenTh = new HashMap<Integer, String>();
        for (final List<String> r : dong(du, "data_potion")) {
            tenTh.put(Integer.valueOf(r.get(0).trim()), r.get(4));
        }
        final Map<String, String> tk = new HashMap<String, String>();
        for (final List<String> r : dong(du, "data_nhiem_vu_noi_dung")) {
            tk.put(r.get(0), r.get(15));
        }
        final Map<Integer, String[]> tkTheoId = new HashMap<Integer, String[]>(); // id_so -> {loai, dong_co, thuong_khac}
        for (final List<String> r : dong(du, "data_quest_lau_dai")) {
            tkTheoId.put(Integer.valueOf(r.get(1).trim()), new String[]{r.get(2), r.get(28).trim(), tk.get(r.get(0))});
        }
        // data_quest_moi = CSDL day du, 901..910 viet lai boi huong dan tan thu
        final Map<Integer, List<String>> qm = new LinkedHashMap<Integer, List<String>>();
        for (final List<String> r : dong(du, "data_quest_moi")) {
            qm.put(Integer.valueOf(r.get(0).trim()), r);
        }
        for (final List<String> r : dong(hd, "data_quest_moi")) {
            final List<String> goc = qm.get(Integer.valueOf(r.get(0).trim()));
            final List<String> moi = new ArrayList<String>(r);
            while (moi.size() < 25 && goc != null) {
                moi.add(goc.get(moi.size()));
            }
            qm.put(Integer.valueOf(r.get(0).trim()), moi);
        }
        // lenh viet lai cua sua_tuyen (id -> {dong thuong, exp, gold, luong, luongLock, itemgift, potiongift, npotion})
        final Map<Integer, String[]> viet = new HashMap<Integer, String[]>();
        final Matcher m = Pattern.compile("'Phần thưởng:\\|', '((?:[^'\\\\]|\\\\.)*)'\\) WHERE `id` = (\\d+) AND `mainsub` = \\d+ AND `exp` = (\\d+)"
                + " AND `gold` = (\\d+) AND `luong` = (\\d+) AND `luongLock` = (\\d+) AND `itemgift` = '([^']*)' AND `potiongift` = '([^']*)'"
                + " AND `npotion` = '([^']*)'").matcher(st);
        while (m.find()) {
            viet.put(Integer.valueOf(m.group(2)), new String[]{m.group(1).replace("\\'", "'"), m.group(3), m.group(4), m.group(5), m.group(6),
                m.group(7), m.group(8), m.group(9)});
        }
        Kiem.dung("co lenh viet lai chu thuong (" + viet.size() + ")", viet.size() >= 14);
        final List<String> lech = new ArrayList<String>();
        final List<String> cat = new ArrayList<String>();
        final List<String> dieuKien = new ArrayList<String>();
        int dai = 0;
        for (final List<String> r : qm.values()) {
            final int id = Integer.parseInt(r.get(0).trim());
            final int ms = Integer.parseInt(r.get(5).trim());
            final String[] t = tkTheoId.get(Integer.valueOf(id));
            String khac = null;
            if (t != null) {
                final boolean chinh = "chinh".equals(t[0]) || "huong_dan".equals(t[0]);
                if (ms == 0 && chinh && !"0".equals(t[1]) || ms == 1 && !chinh) {
                    khac = t[2];
                }
            }
            if (id >= 901 && id <= 910) {
                khac = ""; // huong dan dat thuong_khac = ''
            }
            final String mong = ChuThuongNhiemVu.dong(Long.parseLong(r.get(15).trim()), Long.parseLong(r.get(14).trim()),
                    Long.parseLong(r.get(23).trim()), Long.parseLong(r.get(24).trim()), r.get(18), -1, r.get(16), r.get(17), khac,
                    ChuThuongNhiemVu.tu(tenVp), ChuThuongNhiemVu.tu(tenTh));
            final String[] v = viet.get(Integer.valueOf(id));
            final String co;
            if (v != null) {
                co = v[0];
                if (!(v[1].equals(r.get(15).trim()) && v[2].equals(r.get(14).trim()) && v[3].equals(r.get(23).trim())
                        && v[4].equals(r.get(24).trim()) && v[5].equals(r.get(18)) && v[6].equals(r.get(16)) && v[7].equals(r.get(17)))) {
                    dieuKien.add(String.valueOf(id));
                }
            } else {
                co = ChuThuongNhiemVu.phanThuong(r.get(20));
                if (r.get(20).length() >= 200 && !co.equals(mong)) {
                    cat.add(String.valueOf(id)); // dai dung bang cot cu ma khac chu sinh = bi cat
                }
            }
            if (!mong.equals(co)) {
                lech.add(id + ": [" + co + "] != [" + mong + "]");
            }
            dai = Math.max(dai, r.get(20).length() - ChuThuongNhiemVu.phanThuong(r.get(20)).length() + co.length());
        }
        Kiem.bang("dieu kien cot thuong cua lenh viet = dong thuong CSDL", "[]", dieuKien.toString());
        Kiem.bang("khong con chu bi cat o 200 ky tu (dong khong viet lai)", "[]", cat.toString());
        Kiem.bang("doan Phan thuong moi dong = sinh tu dong thuong (" + qm.size() + " dong)", "[]",
                lech.size() > 5 ? lech.subList(0, 5) + " ... " + lech.size() : lech.toString());
        Kiem.dung("chu dai nhat vua cot 1000 (" + dai + ")", dai < 1000);
        // luat thuan
        final Map<Integer, String> vp = new HashMap<Integer, String>();
        vp.put(81, "Than lửa đồng");
        vp.put(88, "Hạt giống thần đồng");
        vp.put(1039, "Huy chương Gym");
        final Map<Integer, String> th = new HashMap<Integer, String>();
        th.put(1, "Thuốc hồi máu");
        Kiem.bang("dong thuong: so, vat pham theo lop, thuoc, thuong khac",
                "91.128 EXP, 1.465 xu, 10 lượng khoá, Hạt giống thần đồng, 3 Thuốc hồi máu, Huy chương Gym x3, danh hiệu Bạn Gym",
                ChuThuongNhiemVu.dong(91128, 1465, 0, 10, "81,88,81,81,81,0", 1, "1", "3",
                        "truyen:Chương 10;item:1039x3;trang_tri:x;danh_hieu:Bạn Gym", ChuThuongNhiemVu.tu(vp), ChuThuongNhiemVu.tu(th)));
        Kiem.bang("dong thuong: moi lop -> ten / ten (theo dong)", "Than lửa đồng / Hạt giống thần đồng (theo dòng Pokémon)",
                ChuThuongNhiemVu.dong(0, 0, 0, 0, "81,88,81,81,81,0", -1, "32000", "32000", null, ChuThuongNhiemVu.tu(vp), null));
        Kiem.bang("dong thuong: trong", "Không có", ChuThuongNhiemVu.dong(0, 0, 0, 0, "32000", -1, "32000", "32000", "truyen:x", null, null));
        Kiem.bang("thay doan phan thuong", "Nhiệm vụ:|A|Phần thưởng:|5 xu", ChuThuongNhiemVu.thayPhanThuong("Nhiệm vụ:|A|Phần thưởng:|5 x", "5 xu"));
        Kiem.dung("server gui chu thuong sinh luc gui (-64 nhom 1, 2)", doc("src/real/MessageCreator.java").split("ChuThuongNhiemVuGame.infoGift").length == 3);
        Kiem.dung("tra phu qua NPC phat thuong_khac nhu So tay", doc("src/real/Char.java").contains("NhiemVuEngine.khiXongPhuCu(this, info.ma)"));
    }

    /** Moi dong cua moi lenh INSERT INTO `bang` (co / khong danh sach cot) trong tep SQL. */
    static List<List<String>> dong(final String sql, final String bang) {
        final List<List<String>> kq = new ArrayList<List<String>>();
        final String dau = "INSERT INTO `" + bang + "`";
        int a = sql.indexOf(dau);
        while (a >= 0) {
            final int v = sql.indexOf("VALUES", a);
            int i = v + 6;
            List<String> o = null;
            final StringBuilder sb = new StringBuilder();
            boolean nhay = false;
            int sau = 0;
            for (; i < sql.length(); i++) {
                final char c = sql.charAt(i);
                if (nhay) {
                    if (c == '\\' && i + 1 < sql.length()) {
                        final char n = sql.charAt(++i);
                        sb.append(n == 'n' ? '\n' : n == 'r' ? '\r' : n == 't' ? '\t' : n);
                    } else if (c == '\'' && i + 1 < sql.length() && sql.charAt(i + 1) == '\'') {
                        sb.append('\'');
                        i++;
                    } else if (c == '\'') {
                        nhay = false;
                    } else {
                        sb.append(c);
                    }
                } else if (c == '\'') {
                    nhay = true;
                } else if (c == '(') {
                    sau++;
                    if (sau == 1) {
                        o = new ArrayList<String>();
                        sb.setLength(0);
                    } else {
                        sb.append(c);
                    }
                } else if (c == ')') {
                    sau--;
                    if (sau == 0) {
                        o.add(sb.toString().trim().equals("NULL") ? "" : sb.toString());
                        kq.add(o);
                    } else {
                        sb.append(c);
                    }
                } else if (c == ',' && sau == 1) {
                    o.add(sb.toString().trim().equals("NULL") ? "" : sb.toString());
                    sb.setLength(0);
                } else if (sau >= 1) {
                    if (!(sau == 1 && Character.isWhitespace(c) && sb.length() == 0)) {
                        sb.append(c);
                    }
                } else if (c == ';' || sql.startsWith("ON DUPLICATE", i)) {
                    break;
                }
            }
            a = sql.indexOf(dau, i);
        }
        return kq;
    }
}
