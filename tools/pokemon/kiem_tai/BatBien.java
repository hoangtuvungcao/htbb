import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.*;

/**
 * BAT BIEN chay kem MOI bot cua bo hoi quy (gan qua BotChucNang.Bot.ngheNhan / ngheGui). Vi pham ghi vao VI_PHAM (kem
 * kich ban, tai khoan, bang chung); HoiQuy.chay1 bao FAIL "<kich ban>.bat_bien" neu kich ban co vi pham.
 * <ol>
 *   <li>NAP_LAP: &gt; 3 lan nap CUNG ban do (goi 12) trong 10 giay (vong lap nap lai map, vd ket thuyen 107).</li>
 *   <li>KEO_LUI: sau buoc di cua chinh minh (goi 4), server tra vi tri cua minh VE mot o da di qua (giat lui).</li>
 *   <li>TIEU_DE_QUAI: menu / hop co tieu de / dong dau = ten mot loai quai (menu mo nham theo muc tieu dang nham).</li>
 *   <li>DUONG_MENU: moi chu "A → B → C" server gui (37, -26, -32, -31, -64, menu) phai khop cay menu that
 *       (com.htbb.pokemon.DuongMenu.kiem + real.MenuDoiThuong.cay, cung bo kiem test/KiemDuongMenu).</li>
 *   <li>TIEN: xu / luong / luong khoa khong am; LUONG (tien nap) khong duoc TANG ngoai cua so cho phep; GIAM cua bat ky
 *       tien nao chi trong cua so cho phep sau mot hanh dong tieu cua bot (choPhep("tien", ms)); so sach chat
 *       (soSach) kiem tung khoan.</li>
 *   <li>KET: cuoi kich ban (kiemKet) nhan vat phai roi duoc ban do dang dung (qua mot cong, hoac Ve lang mien phi).</li>
 *   <li>NV_LAM_DUOC: moi nhiem vu dang giu (-64 nhom 2) co muc tieu quai: quai co cho sinh tren ban do toi duoc; chu muc
 *       tieu ghi "N <quai> ở <ban do>" thi N = so can that va quai sinh o ban do do.</li>
 * </ol>
 */
final class BatBien {

    static final List<String> VI_PHAM = Collections.synchronizedList(new ArrayList<>());
    static final Map<BotChucNang.Bot, BatBien> CUA = Collections.synchronizedMap(new WeakHashMap<>());
    /** Tat ca chu "A → B" da thay (de bao cao). */
    static final Set<String> DUONG_DA_KIEM = Collections.synchronizedSet(new TreeSet<>());

    final BotChucNang.Bot b;
    final String kichBan;
    final ArrayDeque<long[]> napMap = new ArrayDeque<>(); // {luc, mapId}
    long lanDoiMap;
    final ArrayDeque<int[]> buocDi = new ArrayDeque<>(); // vi tri gui di gan day {x, y}
    long lanDiCuoi;
    int soKeoLui;
    long xu = -1;
    int luong = -1, lk = -1;
    final Map<String, Long> choPhepDen = new HashMap<>();
    boolean daBaoNapLap;

    BatBien(BotChucNang.Bot b) {
        this.b = b;
        this.kichBan = HoiQuy.KB_HIEN.get();
    }

    static BatBien gan(BotChucNang.Bot b) {
        BatBien v = new BatBien(b);
        CUA.put(b, v);
        b.ngheNhan = v::nhan;
        b.ngheGui = v::gui;
        return v;
    }

    static BatBien of(BotChucNang.Bot b) {
        BatBien v = CUA.get(b);
        return v != null ? v : gan(b);
    }

    void viPham(String loai, String bang) {
        String d = "[" + loai + "] " + kichBan + " " + b.user + " map " + b.mapId + ": " + bang;
        VI_PHAM.add(d);
        System.out.println("  !! VI PHAM " + d);
    }

    /** Cho phep thay doi loai {@code loai} ("tien", "luong", "dich_chuyen", "cap") trong {@code ms} toi. */
    void choPhep(String loai, long ms) {
        choPhepDen.put(loai, System.currentTimeMillis() + ms);
    }

    boolean duocPhep(String loai) {
        Long t = choPhepDen.get(loai);
        return t != null && System.currentTimeMillis() <= t;
    }

    // ------------------------------------------------------------------ goi di
    void gui(int cmd, byte[] d) {
        try {
            if (cmd == 4 && d.length >= 4) {
                int x = ((d[0] & 255) << 8 | (d[1] & 255)), y = ((d[2] & 255) << 8 | (d[3] & 255));
                buocDi.addLast(new int[]{(short) x, (short) y});
                while (buocDi.size() > 30) buocDi.pollFirst();
                lanDiCuoi = System.currentTimeMillis();
            } else if (cmd == 24 || cmd == -30 || cmd == -32 || cmd == -31 || cmd == 29 || cmd == 22 || cmd == 34 || cmd == 36
                    || cmd == 66 || cmd == 82 || cmd == -64 || cmd == 23 || cmd == 31 || cmd == 9 || cmd == 106 || cmd == -113) {
                // hanh dong cua nguoi choi co the tieu / nhan tien (mua, menu dich vu, mac do, giao dich, len tau, nhiem vu)
                choPhep("tien", 6000);
                if (cmd == 12 || cmd == 31 || cmd == -30 || cmd == -32 || cmd == 82) choPhep("dich_chuyen", 8000);
            } else if (cmd == 12) {
                choPhep("dich_chuyen", 8000);
            }
        } catch (RuntimeException ignored) {
        }
    }

    // ------------------------------------------------------------------ goi den
    void nhan(BotChucNang.SuKien e) {
        long now = System.currentTimeMillis();
        switch (e.cmd) {
            case 12: {
                lanDoiMap = now;
                buocDi.clear();
                napMap.addLast(new long[]{now, b.mapId});
                while (!napMap.isEmpty() && now - napMap.peekFirst()[0] > 10000) napMap.pollFirst();
                int cung = 0;
                for (long[] n : napMap) if (n[1] == b.mapId) cung++;
                if (cung > 3 && !daBaoNapLap) {
                    daBaoNapLap = true;
                    viPham("NAP_LAP", cung + " lan nap map " + b.mapId + " (" + b.tenMap + ") trong 10 giay");
                }
                break;
            }
            case 4: {
                if (e.chu == null || !e.chu.startsWith("VI TRI MINH") || e.so == null) break;
                if (now - lanDoiMap < 2500 || duocPhep("dich_chuyen") && now - lanDiCuoi > 1500) break;
                if (now - lanDiCuoi > 2000 || buocDi.size() < 2) break;
                int[] cuoi = buocDi.peekLast();
                if (Math.abs(cuoi[0] - e.so[0]) <= 16 && Math.abs(cuoi[1] - e.so[1]) <= 16) break;
                boolean veCu = false;
                int i = 0;
                for (int[] p : buocDi) {
                    if (++i == buocDi.size()) break;
                    if (Math.abs(p[0] - e.so[0]) <= 8 && Math.abs(p[1] - e.so[1]) <= 8) veCu = true;
                }
                if (veCu) {
                    soKeoLui++;
                    viPham("KEO_LUI", "gui toi " + cuoi[0] + "," + cuoi[1] + " -> server tra " + e.so[0] + "," + e.so[1] + " (lan " + soKeoLui + ")");
                }
                break;
            }
            case 16: {
                if (e.d == null || e.d.length == 0) break;
                kiemTien();
                break;
            }
            case -30: {
                if (b.menuChu != null && !b.menuChu.isEmpty()) {
                    kiemTieuDe(b.menuChu.get(0), "menu " + (e.so == null ? "" : e.so[0] + "/" + e.so[1]));
                    for (String m : b.menuChu) kiemDuong(m);
                }
                break;
            }
            case -32:
            case -31:
                if (e.chu != null) {
                    String t = e.chu.replaceFirst("^(POPUP|NHAP)\\([^)]*\\): ", "");
                    int xd = t.indexOf('\n');
                    kiemTieuDe(xd > 0 ? t.substring(0, xd) : t, "hop " + e.cmd);
                    kiemDuong(t);
                }
                break;
            case 37:
            case -26:
                if (e.chu != null) kiemDuong(e.chu);
                break;
            case -64:
                if (e.d != null) {
                    try {
                        for (KichBanTienDoNhiemVu.Dong q : KichBanTienDoNhiemVu.doc(e.d)) {
                            kiemDuong(q.moTa);
                            kiemDuong(q.muc);
                            kiemDuong(q.tra);
                            kiemDuong(q.khoa);
                        }
                    } catch (IOException ignored) {
                    }
                }
                break;
            default:
        }
    }

    void kiemTien() {
        long xuMoi = b.xu;
        int luongMoi = b.luong, lkMoi = b.lk;
        if (xuMoi < 0 && xu >= 0 || luongMoi < -1 || lkMoi < -1) viPham("TIEN_AM", "xu " + xuMoi + " luong " + luongMoi + " lk " + lkMoi);
        if (xu >= 0 && xuMoi >= 0 && xuMoi < xu && !duocPhep("tien")) viPham("TIEN_MAT", "xu " + xu + " -> " + xuMoi + " khong co hanh dong tieu");
        if (luong >= 0 && luongMoi >= 0 && luongMoi > luong && !duocPhep("luong")) viPham("LUONG_TU_TANG", "luong " + luong + " -> " + luongMoi);
        if (luong >= 0 && luongMoi >= 0 && luongMoi < luong && !duocPhep("tien")) viPham("TIEN_MAT", "luong " + luong + " -> " + luongMoi + " khong co hanh dong tieu");
        if (lk >= 0 && lkMoi >= 0 && lkMoi < lk && !duocPhep("tien")) viPham("TIEN_MAT", "luong khoa " + lk + " -> " + lkMoi + " khong co hanh dong tieu");
        if (xuMoi >= 0) xu = xuMoi;
        if (luongMoi >= 0) luong = luongMoi;
        if (lkMoi >= 0) lk = lkMoi;
    }

    // ------------------------------------------------------------------ tieu de = ten quai
    void kiemTieuDe(String t, String noi) {
        if (t == null) return;
        String s = t.trim();
        if (s.isEmpty() || HoiQuy.TEN_QUAI.isEmpty()) return;
        for (String q : HoiQuy.TEN_QUAI.values()) {
            if (q != null && q.length() >= 3 && s.equalsIgnoreCase(q)) {
                viPham("TIEU_DE_QUAI", noi + " co tieu de / dong dau la ten quai '" + s + "'");
                return;
            }
        }
    }

    // ------------------------------------------------------------------ duong menu
    static Object cay;
    static Method kiem;
    static boolean daThuCay;

    static synchronized void napCay() {
        if (daThuCay) return;
        daThuCay = true;
        try {
            String s = new String(Files.readAllBytes(new File(BotChucNang.NOI, "sql/htbb.sql").toPath()), StandardCharsets.UTF_8);
            int i = s.indexOf("INSERT INTO `data_noi_dung_gym` VALUES");
            String dong = s.substring(i, s.indexOf('\n', i));
            Matcher m = Pattern.compile("\\((\\d+),'([^']*)','([^']*)','([^']*)','([^']*)',(\\d+),(\\d+),'([^']*)'").matcher(dong);
            List<Object[]> gym = new ArrayList<>();
            while (m.find()) gym.add(new Object[]{m.group(8), Integer.valueOf(m.group(6)), Integer.valueOf(m.group(1)), m.group(2), Integer.valueOf(m.group(7))});
            List<String> hd = new ArrayList<>();
            for (String ma : new String[]{"so_tay.bo_qua", "so_tay.huong_dan"}) {
                Matcher mm = Pattern.compile("\\('" + Pattern.quote(ma) + "', ?\\d+, ?'([^']*)'").matcher(s);
                if (mm.find()) hd.add(mm.group(1));
            }
            Class<?> md = Class.forName("real.MenuDoiThuong");
            cay = md.getMethod("cay", List.class, List.class).invoke(null, gym, hd);
            Class<?> dm = Class.forName("com.htbb.pokemon.DuongMenu");
            kiem = dm.getMethod("kiem", String.class, Class.forName("com.htbb.pokemon.DuongMenu$Nut"));
            System.out.println("[BAT BIEN] cay menu that: " + gym.size() + " Gym, " + hd.size() + " muc huong dan");
        } catch (Throwable t) {
            System.out.println("[BAT BIEN] khong dung duoc cay menu (thieu lop server tren classpath?): " + t);
            cay = null;
        }
    }

    void kiemDuong(String chu) {
        if (chu == null || !chu.contains("→")) return;
        napCay();
        if (cay == null) return;
        try {
            @SuppressWarnings("unchecked")
            List<String> loi = (List<String>) kiem.invoke(null, chu, cay);
            DUONG_DA_KIEM.add(chu.length() > 160 ? chu.substring(0, 160) : chu);
            if (loi != null && !loi.isEmpty()) viPham("DUONG_MENU", "'" + chu.replace('\n', ' ') + "' -> " + loi);
        } catch (Throwable t) {
            System.out.println("[BAT BIEN] kiem duong loi: " + t);
        }
    }

    // ------------------------------------------------------------------ cuoi kich ban
    /** Ket thuc kich ban cho bot: kiem nhiem vu dang giu lam duoc + khong ket o ban do. */
    static void kiemKet(BotChucNang.Bot b) {
        if (b == null || b.dut || !b.conSong()) return;
        BatBien v = of(b);
        try {
            v.kiemNhiemVu();
        } catch (Exception e) {
            System.out.println("  [BAT BIEN] kiem nhiem vu loi " + e);
        }
    }

    /** NV_LAM_DUOC: mo so Nhiem vu (-64 nhom 2) va kiem tung muc tieu quai. */
    void kiemNhiemVu() throws IOException {
        if (!HoiQuy.daNapTheGioi) HoiQuy.napTheGioi(b);
        if (!HoiQuy.daNapTheGioi) return;
        int m = b.moc();
        b.gui(-64, (byte) 2, (short) 0, (byte) 0);
        b.bom(1200);
        List<KichBanTienDoNhiemVu.Dong> ds = KichBanTienDoNhiemVu.ds(b, m, 2);
        if (ds == null) return;
        for (KichBanTienDoNhiemVu.Dong q : ds) {
            for (int[] t : q.td) {
                if (t[0] < 0 || q.loai != 0) continue;
                Set<Integer> maps = BotXuyenSuot.QUAI_MAP.get(t[0]);
                String tenQ = HoiQuy.TEN_QUAI.getOrDefault(t[0], "#" + t[0]);
                if (maps == null || maps.isEmpty()) {
                    viPham("NV_LAM_DUOC", "nhiem vu '" + q.ten + "' can ha " + tenQ + " nhung quai nay khong sinh o ban do nao");
                    continue;
                }
                boolean toi = false;
                for (int mid : maps) if (BotXuyenSuot.duong(b.mapId, mid) != null) { toi = true; break; }
                if (!toi) viPham("NV_LAM_DUOC", "nhiem vu '" + q.ten + "': khong co duong toi ban do co " + tenQ + " " + maps);
                String muc = (q.muc.isEmpty() ? q.moTa : q.muc);
                Matcher mm = Pattern.compile("(\\d+) " + Pattern.quote(tenQ) + " ở ([^·,.(]+)").matcher(muc);
                if (mm.find()) {
                    int n = Integer.parseInt(mm.group(1));
                    if (n != t[2]) viPham("NV_SO_LUONG", "nhiem vu '" + q.ten + "' ghi " + n + " " + tenQ + " nhung can " + t[2] + ": " + muc);
                    String tenBd = mm.group(2).trim();
                    boolean khop = false;
                    for (int mid : maps) {
                        String tm = HoiQuy.TEN_MAP.get(mid);
                        if (tm != null && (tm.contains(tenBd) || tenBd.contains(tm))) khop = true;
                    }
                    if (!khop && !HoiQuy.TEN_MAP.isEmpty()) {
                        List<String> that = new ArrayList<>();
                        for (int mid : maps) that.add(mid + ":" + HoiQuy.TEN_MAP.get(mid));
                        viPham("NV_BAN_DO", "nhiem vu '" + q.ten + "' ghi " + tenQ + " ở '" + tenBd + "' nhung quai sinh o " + that);
                    }
                }
            }
        }
    }

    /** KET: thu roi ban do hien tai qua mot cong (khong vao nha); khong duoc thi Ve lang mien phi. true = roi duoc. */
    static boolean thuRoi(BotChucNang.Bot b) throws IOException {
        int truoc = b.mapId;
        if (KichBanDS2.raNgoai(b) && b.mapId != truoc) return true;
        int m = b.moc();
        KichBanDS4.chonSoTay(b, 11);
        b.bom(1500);
        if (b.mapId != truoc) return true;
        of(b).viPham("KET", "khong roi duoc ban do " + truoc + " (" + b.tenMap + ") qua cong hay Ve lang: " + b.chuTu(m));
        return false;
    }
}
