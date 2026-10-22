import com.htbb.pokemon.HuongDanTanThu;
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
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import real.NpcTinhBanDo;

/**
 * HUONG DAN TAN THU (nhanh huong-dan): luat thuan com.htbb.pokemon.HuongDanTanThu, du lieu sql/them_huong_dan_tan_thu.sql
 * (10 buoc = 10 dong dau chuoi chinh 901..910) doi chieu CSDL day du sql/htbb_day_du.sql + server/cMap + server/map:
 * thu tu / cap chuoi chinh, NPC co o ca ba lang, moi buoc lam xong duoc (co diem moc that), ngan sach thuong, bo qua,
 * chuoi chinh sau huong dan (NPC tiep can duoc theo cap). Bot dau-cuoi: tools/pokemon/kiem_tai/KichBanHuongDan (hd_*).
 */
public final class KiemHuongDanTanThu {

    private KiemHuongDanTanThu() {
    }

    static final int[] LANG = {0, 70, 80};
    static final Map<Integer, String> TEN = new HashMap<Integer, String>();

    static {
        final Object[][] t = {{0, "Chansey"}, {1, "Kecleon"}, {2, "Machamp"}, {3, "Bronzong"}, {5, "Xatu"}, {6, "Persian"},
            {7, "Pidgeot"}, {8, "Snorlax"}, {9, "Kangaskhan"}, {21, "Blaziken"}, {22, "Meowth"}};
        for (final Object[] r : t) {
            TEN.put((Integer) r[0], (String) r[1]);
        }
    }

    public static void chay() {
        Kiem.nhom("Huong dan tan thu: 10 buoc dau chuoi chinh, bo qua, NPC lang, chuoi chinh sau huong dan");
        try {
            luat();
            duLieu();
        } catch (final Exception e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi: " + e, false);
        }
    }

    static String doc(final String p) throws Exception {
        return new String(Files.readAllBytes(new File(p).toPath()), StandardCharsets.UTF_8);
    }

    // ------------------------------------------------------------------ luat thuan

    static void luat() {
        Kiem.bang("soBuoc: 3 HD dau", 3, HuongDanTanThu.soBuoc(new String[]{"HD.01", "HD.02", "HD.03", "C01.1", "HD.09"}));
        Kiem.bang("soBuoc: khong HD", 0, HuongDanTanThu.soBuoc(new String[]{"C01.1", "HD.01"}));
        Kiem.bang("soBuoc: null", 0, HuongDanTanThu.soBuoc(null));
        Kiem.dung("khop: dung kieu, muc rong = bat ky", HuongDanTanThu.khop("dung_thuoc", "", "dung_thuoc", "4"));
        Kiem.dung("khop: danh sach muc", HuongDanTanThu.khop("dung_thuoc", "1,2,3", "dung_thuoc", "2"));
        Kiem.sai("khop: sai muc", HuongDanTanThu.khop("nang_cap", "1001", "nang_cap", "1"));
        Kiem.sai("khop: sai kieu", HuongDanTanThu.khop("tha_cau", "", "cau_ca", ""));
        Kiem.sai("khop: kieu khong phai hanh dong (noi_chuyen)", HuongDanTanThu.khop("noi_chuyen", "", "noi_chuyen", ""));
        Kiem.dung("dang huong dan: 0..9 / 10 buoc", HuongDanTanThu.dangHuongDan(0, 10) && HuongDanTanThu.dangHuongDan(9, 10));
        Kiem.sai("dang huong dan: 10 / 10 (chuong 1)", HuongDanTanThu.dangHuongDan(10, 10));
        Kiem.sai("dang huong dan: khong co buoc", HuongDanTanThu.dangHuongDan(0, 0));
        Kiem.dung("moi: buoc 1 chua nhan o Blackthorn", HuongDanTanThu.nenMoi(0, 0, 70, 10, false));
        Kiem.sai("moi: da moi trong phien", HuongDanTanThu.nenMoi(0, 0, 70, 10, true));
        Kiem.sai("moi: ngoai lang (san 105)", HuongDanTanThu.nenMoi(0, 0, 105, 10, false));
        Kiem.sai("moi: da qua buoc 1 (nhan vat cu khong bi ep)", HuongDanTanThu.nenMoi(3, 0, 0, 10, false));
        Kiem.sai("moi: nhan vat cu da xong huong dan", HuongDanTanThu.nenMoi(57, 0, 80, 10, false));
        Kiem.dung("xem lai: dang huong dan", HuongDanTanThu.coMucXemLai(40, 2, 10));
        Kiem.dung("xem lai: nhan vat cu cap thap", HuongDanTanThu.coMucXemLai(HuongDanTanThu.CAP_XEM_LAI, 30, 10));
        Kiem.sai("xem lai: nhan vat cu cap cao", HuongDanTanThu.coMucXemLai(HuongDanTanThu.CAP_XEM_LAI + 1, 30, 10));
        Kiem.bang("bo qua tu buoc 4: thuong buoc 4..10", "[3, 4, 5, 6, 7, 8, 9]", Arrays.toString(HuongDanTanThu.buocConLai(3, 10)));
        Kiem.bang("bo qua khi da xong: khong thuong", 0, HuongDanTanThu.buocConLai(10, 10).length);
        Kiem.bang("thay {thuong}", "Đã nhận: 5 xu.", HuongDanTanThu.thay("Đã nhận: {thuong}.", "thuong", "5 xu"));
        Kiem.bang("so co dau cham", "25.594", HuongDanTanThu.so(25594));
        Kiem.bang("popup bo qua khong trung id da dung", 96, HuongDanTanThu.POPUP_BO_QUA);
    }

    // ------------------------------------------------------------------ du lieu

    /** Cac bo (...) cua INSERT {@code bang} (khoi tu "INSERT INTO `bang`" toi "ON DUPLICATE KEY"), nhay '' / \'. */
    static Map<Integer, String[]> bo(final String sql, final String bang) {
        final Map<Integer, String[]> kq = new LinkedHashMap<Integer, String[]>();
        final int a = sql.indexOf("INSERT INTO `" + bang + "`");
        final int v = sql.indexOf("VALUES", a);
        final int b = sql.indexOf("ON DUPLICATE KEY", v);
        if (a < 0 || v < 0 || b < 0) {
            return kq;
        }
        final String x = sql.substring(v + 6, b);
        List<String> o = new ArrayList<String>();
        final StringBuilder sb = new StringBuilder();
        boolean nhay = false;
        int sau = 0;
        for (int i = 0; i < x.length(); i++) {
            final char c = x.charAt(i);
            if (nhay) {
                if (c == '\\' && i + 1 < x.length()) {
                    sb.append(x.charAt(++i));
                } else if (c == '\'' && i + 1 < x.length() && x.charAt(i + 1) == '\'') {
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
                }
            } else if (sau == 1 && (c == ',' || c == ')')) {
                o.add(sb.toString().trim());
                sb.setLength(0);
                if (c == ')') {
                    sau = 0;
                    try {
                        kq.put(Integer.parseInt(o.get(0)), o.toArray(new String[0]));
                    } catch (final NumberFormatException e) {
                        kq.put(Integer.valueOf(kq.size() + 100000), o.toArray(new String[0]));
                    }
                }
            } else if (sau == 1 && c != '\n') {
                sb.append(c);
            }
        }
        return kq;
    }

    /** Ten nguoi noi o dong dau (khong co "1" = null). */
    static String nguoiNoi(final String dong) {
        final Matcher m = Pattern.compile("^1([^:>]+):").matcher(dong);
        return m.find() ? m.group(1).trim() : null;
    }

    static boolean coNpc(final int load, final int loai) {
        for (final int[] n : NpcTinhBanDo.cua(load)) {
            if (n[2] == loai) {
                return true;
            }
        }
        return false;
    }

    static Set<Integer> quaiTren(final int map) throws Exception {
        final byte[] b = Files.readAllBytes(new File("map/map" + map).toPath());
        final Set<Integer> kq = new HashSet<Integer>();
        for (int i = 2; i < 2 + (b[0] & 255) * (b[1] & 255) && i < b.length; i++) {
            final int v = b[i] & 255;
            if (v != 0 && v != 255) {
                kq.add(v);
            }
        }
        return kq;
    }

    static void duLieu() throws Exception {
        final String sql = doc("../sql/them_huong_dan_tan_thu.sql");
        final String du = doc("../sql/htbb_day_du.sql");
        final Map<Integer, String[]> hd = bo(sql, "data_quest_moi");
        final Map<Integer, String[]> goc = KiemTenHien.dongDump(du, "data_quest_moi", 25);

        // --- 10 buoc, dung id, cung so dong (chi so chuoi chinh cua nhan vat cu khong doi)
        Kiem.bang("10 buoc id 901..910", "[901, 902, 903, 904, 905, 906, 907, 908, 909, 910]", new TreeMap<Integer, String[]>(hd).keySet().toString());
        int hdGoc = 0;
        for (final String[] r : goc.values()) {
            if ("0".equals(r[5].trim()) && Integer.parseInt(r[0].trim()) >= 901 && Integer.parseInt(r[0].trim()) <= 910) {
                hdGoc++;
            }
        }
        Kiem.bang("CSDL day du cung co dung 10 dong 901..910 (viet lai tai cho, khong chen dong)", 10, hdGoc);

        // --- chuoi chinh: cap khong giam (huong dan ghi de 901..910)
        final TreeMap<Integer, Integer> cap = new TreeMap<Integer, Integer>();
        for (final String[] r : goc.values()) {
            if ("0".equals(r[5].trim())) {
                cap.put(Integer.parseInt(r[0].trim()), Integer.parseInt(r[4].trim()));
            }
        }
        for (final String[] r : hd.values()) {
            cap.put(Integer.parseInt(r[0]), Integer.parseInt(r[4]));
        }
        String giam = "";
        int truoc = 0;
        for (final Map.Entry<Integer, Integer> e : cap.entrySet()) {
            if (e.getValue() < truoc) {
                giam += e.getKey() + "(" + truoc + "->" + e.getValue() + ") ";
            }
            truoc = Math.max(truoc, e.getValue());
        }
        Kiem.bang("cap chuoi chinh khong giam (huong dan -> chuong 1 -> ...)", "", giam.trim());
        Kiem.dung("chuoi chinh du dai (>= 300 buoc)", cap.size() >= 300);

        // --- moi buoc: NPC o ca ba lang, loi thoai dung nguoi, muc tieu lam duoc, chu ro
        final Map<String, String[]> meta = new HashMap<String, String[]>();
        final Matcher mm = Pattern.compile("`kieu_muc_tieu` = '([a-z_]*)', `muc_tieu_id` = '([0-9,]*)'.*?WHERE `ma` = '(HD\\.\\d\\d)'").matcher(sql);
        while (mm.find()) {
            meta.put(mm.group(3), new String[]{mm.group(1), mm.group(2)});
        }
        Kiem.bang("meta kieu muc tieu du 10 buoc", 10, meta.size());
        final Map<Integer, String[]> vat = bo(sql, "data_vat_pham_nhiem_vu");
        final List<String> loi = new ArrayList<String>();
        long tongXu = 0;
        long tongExp = 0;
        long tongExpGoc = 0;
        int tongThuoc = 0;
        int soDo = 0;
        final Set<String> kieuBuoc = new HashSet<String>();
        int k = 0;
        for (final String[] r : new TreeMap<Integer, String[]>(hd).values()) {
            k++;
            final String ma = String.format("HD.%02d", k);
            final int rcv = Integer.parseInt(r[2]);
            final int res = Integer.parseInt(r[3]);
            final int type = Integer.parseInt(r[11]);
            final String[] content = r[6].split(">");
            for (final int l : LANG) {
                if (!coNpc(l, rcv)) {
                    loi.add(ma + " NPC giao " + rcv + " khong o lang " + l);
                }
                if (res != 32000 && !coNpc(l, res)) {
                    loi.add(ma + " NPC tra " + res + " khong o lang " + l);
                }
            }
            if (!TEN.get(rcv).equals(nguoiNoi(content[0]))) {
                loi.add(ma + " loi dau cua " + nguoiNoi(content[0]) + " ma NPC giao la " + TEN.get(rcv));
            }
            if (type != 3 && !TEN.get(res).equals(nguoiNoi(r[8].split(">")[0]))) {
                loi.add(ma + " loi tra cua " + nguoiNoi(r[8]) + " ma NPC tra la " + TEN.get(res));
            }
            for (final String c : content) {
                if (!(c.startsWith("1") || c.startsWith("0"))) {
                    loi.add(ma + " dong thoai thieu tien to 1/0: " + c);
                }
            }
            if (type != 3 && !content[content.length - 1].trim().endsWith("?")) {
                loi.add(ma + " dong cuoi khong phai cau hoi nhan");
            }
            final String[] mt = meta.get(ma);
            kieuBuoc.add(mt == null ? "?" : mt[0]);
            if (type == 1 || type == 2) {
                final int id = Integer.parseInt(r[9]);
                if (id < 51 || id > 58 || !vat.containsKey(id)) {
                    loi.add(ma + " vat pham nhiem vu " + id + " khong khai bao");
                }
            }
            if (type == 2 && (mt == null || !HuongDanTanThu.laKieuHanhDong(mt[0]))) {
                loi.add(ma + " buoc hanh dong kieu " + (mt == null ? null : mt[0]) + " khong dem duoc");
            }
            if (type == 0 && !quaiTren(1).contains(Integer.parseInt(r[10]))) {
                loi.add(ma + " quai " + r[10] + " khong moc o Pallet (map 1)");
            }
            if (!r[20].contains("Địa điểm:|") || !r[20].contains("NPC:|") || r[20].length() > 200 || r[7].isEmpty()) {
                loi.add(ma + " infogift thieu dia diem / NPC hoac qua 200 ky tu");
            }
            if (!r[20].contains(HuongDanTanThu.so(Long.parseLong(r[15])) + " EXP, " + HuongDanTanThu.so(Long.parseLong(r[14])) + " xu")) {
                loi.add(ma + " dong thuong khong khop EXP / xu");
            }
            if (r[20].contains("lượng") || !"0".equals(r[23]) || !"0".equals(r[24])) {
                loi.add(ma + " thuong co luong / luong khoa");
            }
            tongXu += Long.parseLong(r[14]);
            tongExp += Long.parseLong(r[15]);
            final String[] g = goc.get(Integer.parseInt(r[0]));
            tongExpGoc += g == null ? 0 : Long.parseLong(g[15].trim());
            if (!"32000".equals(r[16])) {
                for (final String n : r[17].split(",")) {
                    tongThuoc += Integer.parseInt(n);
                }
                for (final String t : r[16].split(",")) {
                    if (!"1".equals(t) && !"4".equals(t)) {
                        loi.add(ma + " thuong thuoc la " + t);
                    }
                }
            }
            if (!"32000".equals(r[18])) {
                soDo++;
                if (!r[18].startsWith("1001,")) {
                    loi.add(ma + " trang bi thuong khong phai Ruy bang so cap");
                }
            }
            if (!r[6].matches("(?s).*[ăâđêôơưáàảãạéèẻẽẹíìóòõọúùủũụýỳ].*")) {
                loi.add(ma + " loi thoai khong dau");
            }
        }
        Kiem.bang("moi buoc: NPC o ca ba lang, loi dung nguoi, muc tieu lam duoc, chu ro", "[]", loi.toString());
        Kiem.dung("du hoat dong: noi chuyen, mang do, ha quai, dung thuoc, cong diem, mac do, tha cau",
                kieuBuoc.containsAll(Arrays.asList("noi_chuyen", "dua_thu", "giet", "dung_thuoc", "hoc_chieu", "nang_cap", "tha_cau")));
        Kiem.dung("ngan sach: xu it (<= 2.000 ca chuoi)", tongXu > 0 && tongXu <= 2000);
        Kiem.dung("ngan sach: EXP khong vuot huong dan cu (" + tongExp + " <= " + tongExpGoc + ")", tongExp <= tongExpGoc);
        Kiem.dung("ngan sach: thuoc <= 12 binh (" + tongThuoc + ")", tongThuoc > 0 && tongThuoc <= 12);
        Kiem.bang("ngan sach: dung 1 trang bi (Ruy bang)", 1, soDo);

        // --- diem moc that cho tung kieu hanh dong
        final String dung = doc("src/real/DungVatPham.java");
        final String map = doc("src/real/Map.java");
        final String hst = doc("src/com/htbb/pokemon/HeSinhThaiMenu.java");
        final String nve = doc("src/real/NhiemVuEngine.java");
        Kiem.dung("diem moc dung_thuoc: binh da tru khoi tui", dung.contains("DiemMocNhiemVu.khiDungThuoc(p, t)"));
        Kiem.dung("diem moc hoc_chieu: cong diem + hoc chieu moi", map.contains("DiemMocNhiemVu.khiHocChieu(player, skillType)")
                && map.contains("DiemMocNhiemVu.khiHocChieu(player, idSkill[charclass][skillSelectIndex])"));
        Kiem.dung("diem moc nang_cap: mac do", map.contains("DiemMocNhiemVu.khiMacDo(player, item.getTemplate().id)"));
        Kiem.dung("diem moc tha_cau: tha cau thanh cong", hst.contains("real.DiemMocNhiemVu.khiThaCau(p)"));
        Kiem.dung("engine su kien -> huong dan (mot diem goi)", nve.contains("HuongDanTanThuGame.suKien(p, kieu, thamSo, n)"));
        Kiem.dung("popup 96 = xac nhan bo qua", map.contains("idpopup == com.htbb.pokemon.HuongDanTanThu.POPUP_BO_QUA"));
        Kiem.sai("popup 96 chua ai dung truoc", map.contains("idpopup == 96") || doc("src/real/MapTown.java").contains("idpopup == 96"));
        Kiem.dung("nhan nhiem vu chinh -> kiem dieu kien dat san", doc("src/real/Char.java").contains("HuongDanTanThuGame.khiNhan(this, info)"));
        Kiem.dung("vao game -> lam lai buoc luu lech", doc("src/real/LauDaiNhanVat.java").contains("HuongDanTanThuGame.khiNap(p)"));
        Kiem.dung("So tay: muc huong dan noi vao cuoi", doc("src/real/SoTayLauDai.java").contains("HuongDanTanThuGame.themMucSoTay(p,"));

        // --- chu he huong dan (CSDL, khong viet cung)
        final Map<Integer, String[]> chu = bo(sql, "data_huong_dan_tan_thu");
        final Map<String, String> c = new HashMap<String, String>();
        int npcDong = 0;
        for (final String[] r : chu.values()) {
            c.put(r[0], r[3]);
            if (r[0].startsWith("npc.") && !r[2].isEmpty() && r[3].length() > 20) {
                npcDong++;
            }
        }
        Kiem.dung("chu: moi Lam / Bo qua, hoi xac nhan co {thuong}, xong, So tay",
                !c.getOrDefault("moi.lam", "").isEmpty() && !c.getOrDefault("moi.bo_qua", "").isEmpty()
                && c.getOrDefault("bo_qua.hoi", "").contains("{thuong}") && c.getOrDefault("bo_qua.xong", "").contains("{thuong}")
                && !c.getOrDefault("so_tay.bo_qua", "").isEmpty() && !c.getOrDefault("so_tay.huong_dan", "").isEmpty());
        Kiem.dung("chu: cong dung >= 12 NPC / tinh nang (" + npcDong + ")", npcDong >= 12);
        final String game = doc("src/real/HuongDanTanThuGame.java");
        Kiem.sai("HuongDanTanThuGame khong viet cung chu nguoi choi (chi doc data_huong_dan_tan_thu)",
                Pattern.compile("\"[^\"]*[ăâđêôơưạảãáàếềểễệốồổỗộớờởỡợứừửữựíìỉĩịúùủũụ][^\"]*\"").matcher(game).find());

        // --- Chansey + Snorlax o ca ba lang (cMap) + data_npc.ban_do
        String thieuLang = "";
        for (final int l : LANG) {
            for (final int t : new int[]{0, 8, 5, 21, 1, 2, 22, 7}) {
                if (!coNpc(l, t)) {
                    thieuLang += l + ":" + t + " ";
                }
            }
        }
        Kiem.bang("ba lang co du NPC huong dan + Chansey + Snorlax", "", thieuLang.trim());
        final String ten = doc("../sql/pokemon_ten_hien.sql");
        Kiem.dung("data_npc.ban_do Chansey / Snorlax co 0,70,80", ten.contains("'Chansey',113,0,'0,43,") && ten.contains(",70,80,100,281'")
                && ten.contains("'Snorlax',143,8,'0,51,57,60,70,80,105,160'"));
        Kiem.dung("ban do 0 dong goi client = server", Arrays.equals(Files.readAllBytes(new File("cMap/0").toPath()),
                Files.readAllBytes(new File("../client_jar/res/0").toPath())));

        // --- chuoi chinh sau huong dan: NPC tiep can duoc theo cap (khong chi o san 105 / Hoenn cap 90)
        final Set<Integer> hoenn = new HashSet<Integer>();
        for (final String[] r : KiemTenHien.dong(ten, "data_map_ten").values()) {
            if ("Hoenn".equals(r[2])) {
                hoenn.add(Integer.parseInt(r[0].trim()));
            }
        }
        final Map<Integer, Integer> doiGiao = new HashMap<Integer, Integer>();
        final Matcher md = Pattern.compile("`npcrcv` = (\\d+)[^;]*WHERE `id` = (\\d+) AND `mainsub` = 0").matcher(sql);
        while (md.find()) {
            doiGiao.put(Integer.parseInt(md.group(2)), Integer.parseInt(md.group(1)));
        }
        final List<String> khongToi = new ArrayList<String>();
        for (final String[] r : goc.values()) {
            final int id = Integer.parseInt(r[0].trim());
            if (!"0".equals(r[5].trim()) || id < 1000) {
                continue;
            }
            final int lv = Integer.parseInt(r[4].trim());
            final int type = Integer.parseInt(r[11].trim());
            final int rcv = doiGiao.containsKey(id) ? doiGiao.get(id) : Integer.parseInt(r[2].trim());
            final int res = Integer.parseInt(r[3].trim());
            for (final int n : type == 3 ? new int[]{rcv} : new int[]{rcv, res}) {
                if (n >= 32000) {
                    continue;
                }
                boolean toi = false;
                for (final File f : new File("cMap").listFiles()) {
                    if (!f.getName().matches("\\d+")) {
                        continue;
                    }
                    final int m = Integer.parseInt(f.getName());
                    if (m != 105 && (lv >= 90 || !hoenn.contains(m)) && coNpc(m, n)) {
                        toi = true;
                        break;
                    }
                }
                if (!toi) {
                    khongToi.add(id + ":npc" + n);
                }
            }
        }
        Kiem.bang("chuoi chinh: moi NPC giao / tra tiep can duoc o cap cua buoc", "[]",
                khongToi.size() > 12 ? khongToi.subList(0, 12) + "... (" + khongToi.size() + ")" : khongToi.toString());
        Kiem.dung("chuong 1: Chansey ke ve sao roi (C01.5 giao = Chansey)", sql.contains("UPDATE `data_quest_moi` SET `npcrcv` = 0,") && doiGiao.get(1005) == 0);
        Kiem.dung("chuong 1 noi tiep huong dan (khong 'tinh giac' lan hai)", sql.contains("`name` = 'Đêm mưa sao băng'"));

        // --- SQL nang cap chay lai duoc
        Kiem.sai("SQL khong DROP / DELETE / TRUNCATE", Pattern.compile("(?i)\\b(DROP|DELETE|TRUNCATE)\\b").matcher(sql.replaceAll("--[^\n]*", "")).find());
        Kiem.sai("SQL khong INSERT ... SELECT ... ON DUPLICATE KEY UPDATE", Pattern.compile("(?is)INSERT[^;]*SELECT[^;]*ON DUPLICATE KEY").matcher(sql).find());
        int capNhat = 0;
        int coDieuKien = 0;
        final String khoi = sql.substring(sql.indexOf("-- SUA CHUOI CHINH"), sql.indexOf("-- /SUA CHUOI CHINH"));
        for (final String l : khoi.split("\n")) {
            if (l.startsWith("UPDATE `data_quest_moi`")) {
                capNhat++;
                if (l.contains("WHERE `id` = ") && l.endsWith("AND `mainsub` = 0;")) {
                    coDieuKien++;
                }
            }
        }
        Kiem.dung("khoi sua chuoi chinh co (>= 150 lenh, moi lenh mot id chuoi chinh): " + capNhat, capNhat >= 150 && capNhat == coDieuKien);
    }
}
