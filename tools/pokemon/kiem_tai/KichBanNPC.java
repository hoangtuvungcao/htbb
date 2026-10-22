import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Kich ban NPC (BotChucNang): tham MOI NPC trong game va chon MOI muc menu.
 *
 *   npc_do      : do tham (khong khang dinh) - in bang NPC -> muc -> tra loi server (menu con, thong bao,
 *                 doi map, shop...) va Exception moi trong log server (-Dlog=<game.log>). Dung khi kiem toan.
 *   npc_tat_ca  : hoi quy - moi NPC trong {@link #DS} phai mo duoc menu, MOI muc phai co tra loi hop ly
 *                 (khong im lang, khong Exception, khong chu kiem hiep); cac muc da go khong duoc xuat hien.
 *   npc_dich_vu : NPC tinh co dich vu dung tren ban do NGOAI TROI (thi tran Hoenn...): Chansey hoi phuc + ban thuoc,
 *                 mua that (goi 24) o Kecleon / Machamp, sua do (72) khong bi da, Bronzong, Persian, Kangaskhan,
 *                 Meowth ngan hang, Lapras cho do, Pidgeot bay; dung xa NPC thi bi tu choi.
 *   npc_su_kien : NPC su kien dang hien o thi tran (goi -50: -49, -65, -66, -67, -35...) mo duoc menu va moi muc
 *                 co tra loi (chay sau khi bat su kien o tob_su_kien + khoi dong lai server).
 *
 * Toa do NPC: server/src/real/MapNapTaiNguyen.loadNpcServer, MapKhoiTao, cMap (NPC tinh).
 */
public class KichBanNPC {

    static void dangKy() {
        BotChucNang.dk("npc_do", true, KichBanNPC::doTham);
        BotChucNang.dk("npc_tat_ca", true, KichBanNPC::tatCa);
        BotChucNang.dk("npc_dich_vu", true, KichBanNPC::dichVu);
        BotChucNang.dk("npc_su_kien", true, KichBanNPC::suKien);
    }

    /** Mot NPC can tham: nhan, id gui goi 23, map + o dung, byte phu (shop 3/27, ban 11..20). */
    static final class Npc {
        final String nhan;
        final int id, map, x, y;
        final byte[] phu;

        Npc(String nhan, int id, int map, int x, int y, int... phu) {
            this.nhan = nhan;
            this.id = id;
            this.map = map;
            this.x = x;
            this.y = y;
            this.phu = new byte[phu.length];
            for (int i = 0; i < phu.length; i++) this.phu[i] = (byte) phu[i];
        }
    }

    static final List<Npc> DS = new ArrayList<>();

    static {
        // --- NPC bot o thi tran (Map.npcVilage, hien o map 0/70/80) -------------------------
        DS.add(new Npc("-8 Thợ rèn bí ẩn", -8, 0, 25, 25));
        DS.add(new Npc("-10 Tổng quản", -10, 0, 25, 30));
        DS.add(new Npc("-90 Đổi thưởng", -90, 0, 27, 30));
        DS.add(new Npc("-11 Hợp thành sơ cấp", -11, 0, 7, 28));
        DS.add(new Npc("-12 Hợp thành cao cấp", -12, 0, 7, 34));
        DS.add(new Npc("-15 Đổi hệ", -15, 0, 11, 38));
        DS.add(new Npc("-31 Đấu trường", -31, 0, 31, 51));
        DS.add(new Npc("-49 Sự kiện", -49, 0, 35, 51));
        DS.add(new Npc("-24 Meowth thần tài", -24, 0, 14, 27));
        DS.add(new Npc("-32 Chuyển vùng", -32, 80, 32, 26));
        // --- NPC bot theo ban do --------------------------------------------------------
        DS.add(new Npc("-9 Pidgeot (Bay) map 17", -9, 17, 99, 54));
        DS.add(new Npc("-26 Xa phu map 10", -26, 10, 58, 125));
        DS.add(new Npc("-37 Chợ map 10", -37, 10, 31, 17));
        DS.add(new Npc("-3 Đội (201)", -3, 201, 65, 10));
        DS.add(new Npc("-4 Liên minh trái (201)", -4, 201, 18, 45));
        DS.add(new Npc("-5 Liên minh phải (201)", -5, 201, 61, 45));
        DS.add(new Npc("-29 Liên minh giữa (201)", -29, 201, 40, 15));
        DS.add(new Npc("-6 Thú cưỡi (201)", -6, 201, 15, 10));
        DS.add(new Npc("-16 Vật nuôi (201)", -16, 201, 25, 10));
        DS.add(new Npc("-25 Lễ quan (201)", -25, 201, 24, 15));
        DS.add(new Npc("-13 Kỹ năng đội (201)", -13, 201, 69, 13));
        DS.add(new Npc("-27 Lễ kết đôi (201)", -27, 201, 35, 10));
        DS.add(new Npc("-31 Đấu trường (201)", -31, 201, 45, 10));
        DS.add(new Npc("-19 Hoa tiêu 114", -19, 114, 120, 119));
        DS.add(new Npc("-20 Hoa tiêu 21", -20, 21, 38, 3));
        DS.add(new Npc("-21 Hoa tiêu 22", -21, 22, 2, 46));
        DS.add(new Npc("-38 Hoa tiêu 11", -38, 11, 68, 63));
        DS.add(new Npc("-39 Hoa tiêu 13", -39, 13, 12, 48));
        DS.add(new Npc("-40 Hoa tiêu 14", -40, 14, 30, 6));
        DS.add(new Npc("-41 Hoa tiêu 15", -41, 15, 9, 19));
        DS.add(new Npc("-42 Hoa tiêu 18", -42, 18, 44, 54));
        DS.add(new Npc("-43 Hoa tiêu 19", -43, 19, 55, 53));
        DS.add(new Npc("-44 Hoa tiêu 20", -44, 20, 8, 42));
        DS.add(new Npc("-45 Hoa tiêu 23", -45, 23, 47, 5));
        DS.add(new Npc("-46 Hoa tiêu 24", -46, 24, 21, 5));
        DS.add(new Npc("-47 Hoa tiêu 10", -47, 10, 71, 123));
        DS.add(new Npc("-48 Hoa tiêu 114b", -48, 114, 64, 8));
        DS.add(new Npc("-100 Hoa tiêu 26", -100, 26, 10, 10));
        DS.add(new Npc("-101 Hoa tiêu 113", -101, 113, 10, 10));
        DS.add(new Npc("-51 Dịch trạm 118", -51, 118, 55, 20));
        DS.add(new Npc("-22 Cổng dịch chuyển 118", -22, 118, 46, 40));
        DS.add(new Npc("-34 Khu", -34, 21, 20, 20));
        // --- NPC tinh (cMap) - goi 23 do client gui ---------------------------------------
        DS.add(new Npc("0 Y tá Chansey (45)", 0, 45, 30, 28));
        DS.add(new Npc("1 Kecleon (45)", 1, 45, 28, 28));
        DS.add(new Npc("2 Machamp (45)", 2, 45, 25, 24));
        DS.add(new Npc("3 Bronzong (47) hệ 0", 3, 47, 32, 30, 0));
        DS.add(new Npc("6 Persian (53)", 6, 53, 34, 50));
        DS.add(new Npc("9 Kangaskhan (0)", 9, 0, 6, 45));
        DS.add(new Npc("9 Kangaskhan (78 nhà)", 9, 78, 6, 5));
        DS.add(new Npc("11 Golem 1 (201) bán", 11, 201, 60, 27, 0));
        DS.add(new Npc("11 Golem 1 (201) mua", 11, 201, 60, 27, 1));
        DS.add(new Npc("24 Sableye (201)", 24, 201, 8, 59));
        DS.add(new Npc("26 Kecleon (201)", 26, 201, 22, 59));
        DS.add(new Npc("27 Bronzong (201)", 27, 201, 22, 27, 0));
        DS.add(new Npc("28 Farfetch'd (201)", 28, 201, 9, 38));
        DS.add(new Npc("29 Persian (201)", 29, 201, 19, 38));
        DS.add(new Npc("30 Kangaskhan (201)", 30, 201, 10, 27));
        DS.add(new Npc("4 Arcanine (0)", 4, 0, 22, 52));
        DS.add(new Npc("5 Xatu (0)", 5, 0, 53, 10));
        DS.add(new Npc("8 Snorlax (51)", 8, 51, 38, 20));
        DS.add(new Npc("23 Torkoal (51)", 23, 51, 38, 27));
        // cuoi cung: "Nhan hang" bat trang thai giao hang (chan dich chuyen) cho phan con lai
        DS.add(new Npc("-50 Tổng tiêu đầu", -50, 0, 29, 20));
    }

    static final Set<Integer> ON = new HashSet<>(Arrays.asList(4, 5, 7, 8, 9, 11, -90, -91, -93, -94, -102, 88, 3, 105, 16, 73, 15, 65, 106, 60, -80));

    /** Tom tat cac goi den tu moc m (bo goi on ao). */
    static String tomTat(BotChucNang.Bot b, int m) {
        StringBuilder sb = new StringBuilder();
        Set<Integer> khac = new TreeSet<>();
        for (int i = m; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.chu != null) {
                if (e.cmd == 27 && !e.chu.startsWith("CHAT(minh)")) continue;
                sb.append('[').append(e.cmd).append("] ").append(e.chu.replace('\n', ' ')).append(" | ");
            } else if (!ON.contains(e.cmd)) {
                khac.add(e.cmd);
            }
        }
        if (!khac.isEmpty()) sb.append("goi ").append(khac);
        String s = sb.toString();
        if (s.isEmpty()) s = "(IM LANG)";
        return s.length() > 400 ? s.substring(0, 400) + "..." : s;
    }

    static long logDai() {
        String f = System.getProperty("log");
        return f == null ? 0 : new File(f).length();
    }

    /** Exception moi trong log server tu vi tri tu. */
    static String loiMoi(long tu) {
        String f = System.getProperty("log");
        if (f == null) return "";
        try (RandomAccessFile r = new RandomAccessFile(f, "r")) {
            if (r.length() <= tu) return "";
            r.seek(tu);
            byte[] d = new byte[(int) Math.min(r.length() - tu, 200000)];
            r.readFully(d);
            String s = new String(d, StandardCharsets.UTF_8);
            StringBuilder kq = new StringBuilder();
            String[] dong = s.split("\n");
            for (int i = 0; i < dong.length; i++) {
                if (dong[i].contains("Exception") && !dong[i].contains("[MANG]")) {
                    kq.append(dong[i].trim());
                    if (i + 1 < dong.length) kq.append(" @ ").append(dong[i + 1].trim());
                    break;
                }
            }
            return kq.toString();
        } catch (IOException e) {
            return "";
        }
    }

    static boolean veCho(BotChucNang.Bot b, Npc n) throws IOException {
        if (b.mapId == n.map && Math.abs(b.px / 16 - n.x) <= 1 && Math.abs(b.py / 16 - n.y) <= 1) return true;
        return toiGan(b, n.map, n.x, n.y);
    }

    /** GM mto toi (x,y); thi tran 0/70/80 dat lai cho xuat hien -> di bo (BFS) toi noi. */
    static boolean toiGan(BotChucNang.Bot b, int map, int x, int y) throws IOException {
        if (!b.den(map, x, y)) return false;
        if (Math.abs(b.px / 16 - x) <= 2 && Math.abs(b.py / 16 - y) <= 2) return true;
        BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
        if (bd == null) return false;
        int[] o = bd.oGan(x, y, 3);
        return o != null && b.diDuong(bd, o[0], o[1]);
    }

    static void moNpc(BotChucNang.Bot b, Npc n) throws IOException {
        byte[] d = new byte[1 + n.phu.length];
        d[0] = (byte) n.id;
        System.arraycopy(n.phu, 0, d, 1, n.phu.length);
        b.gui(23, d);
    }

    /** Menu -30 dau tien den sau moc m: {npc, menu} hoac null. */
    static int[] menuSau(BotChucNang.Bot b, int m) {
        for (int i = m; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd == -30 && e.so != null) return e.so;
        }
        return null;
    }

    static List<String> chuMenuSau(BotChucNang.Bot b, int m) {
        for (int i = m; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd == -30 && e.chu != null) {
                String t = e.chu.substring(e.chu.indexOf(':') + 1).trim();
                return new ArrayList<>(Arrays.asList(t.split(" ; ")));
            }
        }
        return null;
    }

    static PrintWriter ra;

    static void in(String s) {
        System.out.println(s);
        if (ra != null) { ra.println(s); ra.flush(); }
    }

    static void doTham() throws Exception {
        String tep = System.getProperty("npcra");
        if (tep != null) ra = new PrintWriter(new OutputStreamWriter(new FileOutputStream(tep), StandardCharsets.UTF_8));
        String chi = System.getProperty("npc");
        BotChucNang.Bot b = BotChucNang.botMoi(0);
        try {
            KichBanDS.veLang(b);
            b.gm("slv " + b.ten + " 60 0", 800);
            b.gm("smn " + b.ten + " 500000000 50000 50000", 800);
            b.lamMoi();
            for (Npc n : DS) {
                if (chi != null && !Arrays.asList(chi.split(",")).contains(String.valueOf(n.id))) continue;
                if (b.dut || !b.conSong()) { b.close(); b = BotChucNang.botMoi(0); b.gm("smn " + b.ten + " 500000000 50000 50000", 800); }
                boolean toi = veCho(b, n);
                long l0 = logDai();
                int m = b.moc();
                moNpc(b, n);
                b.bom(1800);
                int[] mn = menuSau(b, m);
                List<String> chu = chuMenuSau(b, m);
                in("NPC\t" + n.nhan + "\tmap " + b.mapId + (toi ? "" : " (KHONG TOI " + n.map + ")") + "\t" + tomTat(b, m) + "\t" + loiMoi(l0));
                if (mn == null || chu == null) continue;
                for (int i = 0; i < chu.size() && i < 25; i++) {
                    // mo lai tu dau moi muc (trang thai idx* tren server)
                    veCho(b, n);
                    int m1 = b.moc();
                    moNpc(b, n);
                    b.bom(900);
                    int[] mn1 = menuSau(b, m1);
                    if (mn1 == null) { in("  " + i + "\t" + chu.get(i) + "\t(KHONG MO LAI DUOC MENU)"); continue; }
                    long l1 = logDai();
                    int m2 = b.moc();
                    b.menu(mn1[0], mn1[1], i);
                    b.bom(2000);
                    in("  " + i + "\t" + chu.get(i) + "\t" + tomTat(b, m2) + "\t" + loiMoi(l1));
                    int[] con = menuSau(b, m2);
                    List<String> chuCon = chuMenuSau(b, m2);
                    if (con == null || chuCon == null || System.getProperty("sau") == null) continue;
                    for (int j = 0; j < chuCon.size() && j < 15; j++) {
                        veCho(b, n);
                        int a = b.moc();
                        moNpc(b, n);
                        b.bom(800);
                        int[] r = menuSau(b, a);
                        if (r == null) continue;
                        int a2 = b.moc();
                        b.menu(r[0], r[1], i);
                        b.bom(1000);
                        int[] r2 = menuSau(b, a2);
                        if (r2 == null) continue;
                        long l2 = logDai();
                        int a3 = b.moc();
                        b.menu(r2[0], r2[1], j);
                        b.bom(1800);
                        in("    " + i + "." + j + "\t" + chuCon.get(j) + "\t" + tomTat(b, a3) + "\t" + loiMoi(l2));
                    }
                }
            }
        } finally {
            b.close();
            if (ra != null) ra.close();
        }
        BotChucNang.ket("npc_do", true, "xem bang in o tren");
    }

    // ================================================================ hoi quy

    /** Loai NPC tinh client tu xu ly (khong gui goi 23) hoac chi chao: khong kiem qua goi 23. */
    static final Set<Integer> KHONG_GOI_23 = new HashSet<>(Arrays.asList(4, 5, 8, 21, 23, 25));
    /** Muc chi doi trang thai im (chuyen khu...) - khong doi chu tra loi. */
    static boolean imHopLe(String muc) {
        String k = BotChucNang.khongDau(muc);
        return k.startsWith("khu ");
    }

    /** Muc da go khoi menu (chi bao "dang phat trien" / "sap ra mat" / khong co xu ly) - khong duoc xuat hien. */
    static final String[] MUC_DA_GO = {"Đăng ký giải Liên minh", "Hoạt động giờ", "Nhận mốc nạp"};
    /** Chu kiem hiep / viet tat tien te khong duoc con trong menu NPC. */
    static final String[] TU_CAM = {"Trang bị ma", "Trang bị vật\"", "vât lý", "VKTB", "Dương đông", "Sơn nam", "Lâm tây", "Bình kiều",
        "Chiến hạm", "Hồi hồ", "Trường giang", "Thuý Vũ", "Tuần Lộc", "(150L)", "(500L)", "(300l)", "(1t xu)", "(10l)", "(10t xu)",
        "lkd 30", "Bút", "Búa"};

    static boolean imLang(String tt) {
        return tt.equals("(IM LANG)");
    }

    static String kiemChu(List<String> menu) {
        StringBuilder loi = new StringBuilder();
        for (String m : menu) {
            for (String g : MUC_DA_GO) if (m.contains(g)) loi.append("muc da go '").append(m).append("' ");
            for (String t : TU_CAM) if ((m + "\"").contains(t)) loi.append("chu cu '").append(m).append("' ");
        }
        return loi.toString();
    }

    static void tatCa() throws Exception {
        BotChucNang.Bot b = BotChucNang.botMoi(0);
        try {
            KichBanDS.veLang(b);
            b.gm("slv " + b.ten + " 60 0", 800);
            b.gm("smn " + b.ten + " 500000000 50000 50000", 800);
            b.lamMoi();
            for (Npc n : DS) {
                if (KHONG_GOI_23.contains(n.id)) continue;
                if (b.dut || !b.conSong()) { b.close(); b = BotChucNang.botMoi(0); b.gm("smn " + b.ten + " 500000000 50000 50000", 800); }
                veCho(b, n);
                long l0 = logDai();
                int m = b.moc();
                moNpc(b, n);
                b.bom(1500);
                String tt = tomTat(b, m);
                String loi = loiMoi(l0);
                List<String> chu = chuMenuSau(b, m);
                int[] mn = menuSau(b, m);
                boolean coShop = b.coLenh(m, 23) && b.layLenh(m, 23).d.length > 0;
                BotChucNang.ket("npc." + n.nhan, !imLang(tt) && loi.isEmpty() && (mn != null || coShop || !tt.contains("SHOP NPC (0 byte)")),
                        tt + (loi.isEmpty() ? "" : " LOI: " + loi));
                if (mn == null || chu == null) continue;
                String c = kiemChu(chu);
                if (!c.isEmpty()) BotChucNang.ket("npc." + n.nhan + ".chu", false, c);
                for (int i = 0; i < chu.size() && i < 25; i++) {
                    if (n.id == -50 && i == 0) continue; // "Nhận hàng giao": bat trang thai giao hang - kiem rieng cuoi cung
                    veCho(b, n);
                    int m1 = b.moc();
                    moNpc(b, n);
                    b.bom(900);
                    int[] mn1 = menuSau(b, m1);
                    if (mn1 == null) { BotChucNang.ket("npc." + n.nhan + "." + i, false, "khong mo lai duoc menu"); continue; }
                    long l1 = logDai();
                    int m2 = b.moc();
                    b.menu(mn1[0], mn1[1], i);
                    b.bom(1800);
                    String t2 = tomTat(b, m2);
                    String loi2 = loiMoi(l1);
                    boolean ok = loi2.isEmpty() && (!imLang(t2) || imHopLe(chu.get(i)));
                    BotChucNang.ket("npc." + n.nhan + "." + i + " " + chu.get(i), ok, t2 + (loi2.isEmpty() ? "" : " LOI: " + loi2));
                    List<String> con = chuMenuSau(b, m2);
                    if (con != null) {
                        String cc = kiemChu(con);
                        if (!cc.isEmpty()) BotChucNang.ket("npc." + n.nhan + "." + i + ".chu", false, cc);
                    }
                }
            }
        } finally {
            b.close();
        }
    }

    // ================================================================ dich vu NPC tinh ngoai troi

    static int doiGoi23(BotChucNang.Bot b, int m) {
        BotChucNang.SuKien e = b.layLenh(m, 23);
        return e == null || e.d.length == 0 ? -1 : e.d[0];
    }

    static void dichVu() throws Exception {
        try (BotChucNang.Bot b = BotChucNang.botMoi(0)) {
            KichBanDS.veLang(b);
            b.gm("slv " + b.ten + " 30 0", 800);
            b.gm("smn " + b.ten + " 50000000 5000 0", 800);
            b.lamMoi();
            // --- Chansey (0) o Rustboro 45: menu hoi phuc + ban thuoc
            b.den(45, 30, 30);
            int m = b.moc();
            b.gui(23, (byte) 0);
            int n = b.doiMenu(m, 0, 0, 3000);
            BotChucNang.ket("dich_vu.chansey.menu", n == 2 && b.timMuc("Hồi phục") == 0, b.chuTu(m));
            m = b.moc();
            b.menu(0, 0, 0);
            String hp = b.doiChu(m, 2500, "hồi phục hoàn toàn", "khoẻ mạnh");
            BotChucNang.ket("dich_vu.chansey.hoi_phuc", hp != null, b.chuTu(m));
            m = b.moc();
            b.menu(0, 0, 1);
            b.bom(1500);
            BotChucNang.ket("dich_vu.chansey.cua_so_thuoc", doiGoi23(b, m) == 0 && b.layLenh(m, 23).d.length > 2, b.chuTu(m));
            b.lamMoi();
            int t0 = b.soThuoc(1);
            long xu0 = b.xu;
            b.gui(24, (byte) 1, (byte) 4, (short) 1, (short) 5);
            b.bom(1200);
            b.lamMoi();
            BotChucNang.ket("dich_vu.chansey.mua_thuoc_ngoai_troi", b.soThuoc(1) == t0 + 5 && b.xu < xu0, "thuoc 1: " + t0 + " -> " + b.soThuoc(1) + ", xu " + xu0 + " -> " + b.xu);
            // --- dung xa NPC: tu choi
            b.den(45, 10, 40);
            m = b.moc();
            b.gui(23, (byte) 0);
            BotChucNang.ket("dich_vu.chansey.xa_bi_tu_choi", b.doiChu(m, 2500, "Hãy đến gần") != null, b.chuTu(m));
            // --- Kecleon (1) o 45: mua thuoc
            b.den(45, 28, 30);
            m = b.moc();
            b.gui(23, (byte) 1);
            b.bom(1200);
            BotChucNang.ket("dich_vu.kecleon.cua_so", doiGoi23(b, m) == 0, b.chuTu(m));
            // --- Machamp (2) o 45: mua do cam tay, sua do khong bi da
            b.den(45, 25, 26);
            m = b.moc();
            b.gui(23, (byte) 2);
            n = b.doiMenu(m, 2, 0, 3000);
            if (n > 0) { m = b.moc(); b.menu(2, 0, 0); b.bom(1200); }
            BotChucNang.ket("dich_vu.machamp.shop", doiGoi23(b, m) == 1, b.chuTu(m));
            b.lamMoi();
            java.util.Set<Integer> cu = new java.util.HashSet<>();
            for (int[] d : b.doDung) cu.add(d[0]);
            long xu1 = b.xu;
            b.gui(24, (byte) 1, (byte) 3, (short) 79, (short) 1, (byte) 0);
            b.bom(1200);
            b.lamMoi();
            int[] moi = b.timDo(d -> d[1] == 79 && !cu.contains(d[0]));
            BotChucNang.ket("dich_vu.machamp.mua_ngoai_troi", moi != null && b.xu < xu1, "xu " + xu1 + " -> " + b.xu + " moi=" + (moi != null));
            m = b.moc();
            b.gui(72, (byte) 0);
            b.bom(1500);
            BotChucNang.ket("dich_vu.machamp.sua_do_khong_bi_da", b.conSong() && b.coLenh(m, 72), "con song=" + b.conSong() + " " + b.chuTu(m));
            // --- Bronzong (3) o Slateport 47
            b.den(47, 32, 31);
            m = b.moc();
            b.gui(23, (byte) 3, (byte) 0);
            n = b.doiMenu(m, 3, 0, 3000);
            m = b.moc();
            if (n > 0) b.menu(3, 0, 0);
            b.bom(1500);
            BotChucNang.ket("dich_vu.bronzong.ban_giap", doiGoi23(b, m) == 1, b.chuTu(m));
            // --- Meowth (22) o 47: ngan hang
            b.den(47, 26, 26);
            m = b.moc();
            b.gui(23, (byte) 22);
            n = b.doiMenu(m, 22, 0, 3000);
            BotChucNang.ket("dich_vu.meowth.menu", n >= 2 && b.timMuc("Kho xu") == 0, b.chuTu(m));
            m = b.moc();
            b.menu(22, 0, 0);
            BotChucNang.ket("dich_vu.meowth.kho", b.doiMenu(m, -522, 0, 2500) > 0, b.chuTu(m));
            m = b.moc();
            b.menu(22, 0, 1);
            BotChucNang.ket("dich_vu.meowth.chuyen_luong", b.choLenh(m, -31, 2500), b.chuTu(m));
            // --- Persian (6) o 53: da quy
            b.den(53, 34, 51);
            m = b.moc();
            b.gui(23, (byte) 6);
            b.bom(1200);
            BotChucNang.ket("dich_vu.persian.da_quy", doiGoi23(b, m) == 3, b.chuTu(m));
            // --- Kangaskhan (9) o Pewter 0 (ngoai troi): kho do
            boolean toi = toiGan(b, 0, 6, 46);
            m = b.moc();
            b.gui(23, (byte) 9);
            b.bom(1200);
            BotChucNang.ket("dich_vu.kangaskhan.kho_ngoai_troi", doiGoi23(b, m) == 2, "toi=" + toi + " o " + b.px / 16 + "," + b.py / 16 + " " + b.chuTu(m));
            // --- Lapras (10) o Littleroot 43: mua ve + cho do sang ben khac
            b.den(43, 23, 23);
            m = b.moc();
            b.gui(81, new byte[0]);
            b.bom(1200);
            BotChucNang.ket("dich_vu.lapras.mua_ve", b.coLenh(m, 81), b.chuTu(m));
            m = b.moc();
            b.gui(82, new byte[0]);
            n = b.doiMenu(m, 10, 0, 2500);
            BotChucNang.ket("dich_vu.lapras.ben_do", n > 0 && b.timMuc("Đến") == 0, b.chuTu(m));
            if (n > 0) {
                int mapTruoc = b.mapId;
                m = b.moc();
                b.menu(10, 0, 0);
                boolean doi = b.choLenh(m, 12, 4000);
                if (doi) { b.gui(12, (short) -500, (short) 0, (short) 0); b.bom(600); }
                BotChucNang.ket("dich_vu.lapras.di_do", doi && b.mapId != mapTruoc, "map " + mapTruoc + " -> " + b.mapId + " " + b.chuTu(m));
            }
            // --- Pidgeot (7) o Fortree 54: khong co tuyen san -> server cho bay toi Trung tam Hoenn
            b.den(54, 26, 25);
            m = b.moc();
            b.gui(23, (byte) 7);
            n = b.doiMenu(m, 7, 0, 2500);
            BotChucNang.ket("dich_vu.pidgeot.menu_bay", n > 0, b.chuTu(m));
            if (n > 0) {
                long xu2 = b.xu;
                m = b.moc();
                b.menu(7, 0, 0);
                boolean doi = b.choLenh(m, 12, 4000);
                if (doi) { b.gui(12, (short) -500, (short) 0, (short) 0); b.bom(600); }
                b.lamMoi();
                BotChucNang.ket("dich_vu.pidgeot.bay", doi && b.xu < xu2, "map " + b.mapId + " xu " + xu2 + " -> " + b.xu + " " + b.chuTu(m));
            }
        }
    }

    // ================================================================ NPC su kien dang hien

    static void suKien() throws Exception {
        try (BotChucNang.Bot b = BotChucNang.botMoi(0)) {
            KichBanDS.veLang(b);
            b.gm("slv " + b.ten + " 60 0", 800);
            b.gm("smn " + b.ten + " 500000000 50000 50000", 800);
            int m0 = b.moc();
            b.den(0, 22, 33);
            b.bom(1500);
            // goi -50 (NPC goi -50) va goi 5 cua bot NPC: lay id NPC su kien dang hien
            Set<Integer> ids = new TreeSet<>();
            for (int i = m0; i < b.su.size(); i++) {
                BotChucNang.SuKien e = b.su.get(i);
                if (e.cmd == -50 && e.d != null) {
                    try {
                        DataInputStream r = new DataInputStream(new ByteArrayInputStream(e.d));
                        r.readUTF();
                        ids.add((int) r.readShort());
                    } catch (IOException ignored) { }
                }
            }
            ids.add(-49); // bot dang ky (Trung Thu, Mini) khong qua -50: luon thu
            ids.add(-31); // Vu Lan: muc "Sự kiện Vu Lan" o Dau truong
            ids.add(-15); // Black Friday: muc mua / dau gia o NPC doi he
            for (int id : ids) {
                if (id == -24 || id == -22 || id == -34) continue; // khong phai NPC su kien (da kiem o npc_tat_ca)
                Npc n = new Npc("su kien " + id, id, b.mapId, b.px / 16, b.py / 16);
                long l0 = logDai();
                int m = b.moc();
                moNpc(b, n);
                b.bom(1500);
                String tt = tomTat(b, m);
                List<String> chu = chuMenuSau(b, m);
                BotChucNang.ket("su_kien." + id, !imLang(tt) && loiMoi(l0).isEmpty(), tt);
                if (chu == null) continue;
                String c = kiemChu(chu);
                if (!c.isEmpty()) BotChucNang.ket("su_kien." + id + ".chu", false, c);
                for (int i = 0; i < chu.size() && i < 20; i++) {
                    int m1 = b.moc();
                    moNpc(b, n);
                    b.bom(900);
                    int[] mn1 = menuSau(b, m1);
                    if (mn1 == null) continue;
                    long l1 = logDai();
                    int m2 = b.moc();
                    b.menu(mn1[0], mn1[1], i);
                    b.bom(1800);
                    String t2 = tomTat(b, m2);
                    String loi2 = loiMoi(l1);
                    BotChucNang.ket("su_kien." + id + "." + i + " " + chu.get(i), !imLang(t2) && loi2.isEmpty(), t2 + (loi2.isEmpty() ? "" : " LOI: " + loi2));
                    if (b.mapId != n.map) b.den(n.map, n.x, n.y);
                }
            }
        }
    }
}
