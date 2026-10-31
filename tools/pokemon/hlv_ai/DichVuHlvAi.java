import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * DICH VU HUAN LUYEN VIEN AI (bot lap may chu, chu game duyet 31/10/2026). Tien trinh rieng chay canh may chu game
 * (vps/htbb-hlv-ai.service, KHONG tu bat), noi giao thuc that nhu client (BotChucNang.Bot + KichBanCap140.NguoiChoi cua
 * bo bot kiem thu tools/pokemon/kiem_tai) - bot khong co quyen dac biet nao: may chu ap moi luat nhu nguoi choi (chong spam,
 * khoa ban do, chong clone...) cong luat chong lam phat rieng (real.HlvAiGame, co account.team_user.la_bot).
 *
 * <pre>
 *  Cau hinh: bang cau_hinh nhom hlv_ai (sua o /admin/hlv-ai), doc lai moi 20 giay:
 *    hlv_ai.bat                 0/1 - tat thi moi bot thoat dan
 *    hlv_ai.so_luong            so tai khoan bot (thieu thi tao: team_user.la_bot = 1, mat khau = HMAC(bi mat, ten))
 *    hlv_ai.lich_gio            24 so % online theo gio; moi bot co phien dai / ngan, nghi, treo may
 *    hlv_ai.ty_le_lop / ty_le_tinh_cach / cap_muc_tieu / ban_do / hoat_dong
 *  Ghi: tob_hlv_ai_trang_thai moi phut (bot nao online, cap, ban do, viec) - trang web doc.
 *  CSDL: MOT ket noi JDBC dung chung (BotChucNang.sql).
 *
 *  java -cp <bot>:<lop may chu>:server/lib/mysql-connector-java-5.1.49.jar -Ddb=htbb_db -Ddbcnf=/etc/htbb/hlv-ai.cnf \
 *       -Dnoi=/opt/htbb -Dgoc=/opt/htbb -Dcp=/var/lib/htbb-hlv-ai DichVuHlvAi 127.0.0.1 19129
 *  Bien moi truong HLV_AI_BI_MAT (bat buoc tren may that): bi mat sinh mat khau tai khoan bot.
 *  May thu: -Dhlv.tangToc=N (lich + phien nhanh N lan), -Dhlv.chiTiet=1 (in nhat ky tung bot), -Dhlv.phut=N (tu dung).
 * </pre>
 */
public final class DichVuHlvAi {

    static final boolean CHI_TIET = "1".equals(System.getProperty("hlv.chiTiet"));
    static final double TANG_TOC = Double.parseDouble(System.getProperty("hlv.tangToc", "1"));
    static final String TIEN_TO_TK = System.getProperty("hlv.tienTo", "hlvai");
    static String BI_MAT;
    static final long BAT_DAU = System.currentTimeMillis();
    static final Random RD = new Random();

    /** Mot tai khoan bot (thuoc tinh co dinh luu o tob_hlv_ai_trang_thai). */
    static final class Ho {
        String taiKhoan, matKhau, ten;
        int userId, lop, tinhCach, capMucTieu, vung;
        volatile int charId, cap;
        volatile boolean moi;
        volatile long henVao;
        volatile long vaoLuc, raLuc;
        volatile HlvAiNguoiChoi dang;

        String nhanh() {
            // kieu "lam" / "bo" + chu so cuoi = dong (KichBanCap140.lopCua); tep checkpoint rieng moi tai khoan
            return (tinhCach >= 3 ? "lam_" : "bo_") + taiKhoan + "_" + lop;
        }
    }

    static final List<Ho> HO = new CopyOnWriteArrayList<>();
    static volatile Map<String, String> CH = new HashMap<>();
    static volatile Set<String> HOAT_DONG = Collections.emptySet();
    static final ConcurrentHashMap<String, long[]> DEM = new ConcurrentHashMap<>();
    static final ConcurrentHashMap<String, AtomicLong> LOI = new ConcurrentHashMap<>();
    static final ArrayDeque<String> LOI_GAN = new ArrayDeque<>();

    // ================================================================== dem / loi
    static void dem(String viec, int ok) {
        long[] a = DEM.computeIfAbsent(viec, k -> new long[2]);
        synchronized (a) {
            a[0]++;
            a[1] += ok;
        }
    }

    static void loi(String s) {
        String k = s.replaceAll("\\d+", "N");
        if (k.length() > 160) k = k.substring(0, 160);
        LOI.computeIfAbsent(k, z -> new AtomicLong()).incrementAndGet();
        synchronized (LOI_GAN) {
            LOI_GAN.addLast(new SimpleDateFormat("HH:mm:ss").format(new Date()) + " " + s);
            while (LOI_GAN.size() > 50) LOI_GAN.removeFirst();
        }
    }

    static boolean bat(String viec) {
        return HOAT_DONG.contains(viec);
    }

    // ================================================================== cau hinh
    static void docCauHinh() {
        Map<String, String> m = new HashMap<>();
        for (String d : BotChucNang.sql("SELECT khoa, mac_dinh FROM cau_hinh_danh_muc WHERE nhom = 'hlv_ai'")) {
            String[] f = d.split("\t", 2);
            if (f.length == 2 && !d.startsWith("LOI SQL")) m.put(f[0], f[1]);
        }
        for (String d : BotChucNang.sql("SELECT khoa, gia_tri FROM cau_hinh WHERE khoa LIKE 'hlv\\_ai.%'")) {
            String[] f = d.split("\t", 2);
            if (f.length == 2 && !d.startsWith("LOI SQL")) m.put(f[0], f[1]);
        }
        if (m.isEmpty()) return; // CSDL loi: giu cau hinh cu
        CH = m;
        Set<String> hd = new java.util.HashSet<>();
        for (String x : m.getOrDefault("hlv_ai.hoat_dong", "").split(",")) if (!x.trim().isEmpty()) hd.add(x.trim());
        HOAT_DONG = hd;
    }

    static long so(String k, long md) {
        try {
            return Long.parseLong(CH.getOrDefault(k, "").trim());
        } catch (NumberFormatException e) {
            return md;
        }
    }

    static int[] dsSo(String k, int n, int md) {
        int[] a = new int[n];
        java.util.Arrays.fill(a, md);
        String[] f = CH.getOrDefault(k, "").split(",");
        if (f.length != n) return a;
        try {
            for (int i = 0; i < n; i++) a[i] = Integer.parseInt(f[i].trim());
        } catch (NumberFormatException e) {
            java.util.Arrays.fill(a, md);
        }
        return a;
    }

    static int chonTrongSo(int[] w) {
        int t = 0;
        for (int x : w) t += Math.max(0, x);
        if (t <= 0) return RD.nextInt(w.length);
        int r = RD.nextInt(t);
        for (int i = 0; i < w.length; i++) {
            if (r < Math.max(0, w[i])) return i;
            r -= Math.max(0, w[i]);
        }
        return 0;
    }

    /** Gio trong ngay (0..23) cua lich online: gio that, hoac gio ao tang toc tren may thu. */
    static int gio() {
        Calendar c = Calendar.getInstance();
        if (TANG_TOC <= 1.0) return c.get(Calendar.HOUR_OF_DAY);
        double phut = (System.currentTimeMillis() - BAT_DAU) / 60000.0 * TANG_TOC;
        return (int) ((c.get(Calendar.HOUR_OF_DAY) + (long) (phut / 60.0)) % 24);
    }

    // ================================================================== tai khoan
    static String matKhau(String tk) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(BI_MAT.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] h = mac.doFinal(tk.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 9; i++) sb.append(String.format("%02x", h[i]));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** Nap cac tai khoan bot da co (la_bot = 1) kem thuoc tinh da luu. */
    static void napHo() {
        List<String> r = BotChucNang.sql("SELECT u.username, u.id, IFNULL(t.ten,''), IFNULL(t.lop,-1), IFNULL(t.tinh_cach,''), IFNULL(t.cap_muc_tieu,0),"
                + " IFNULL(t.cap,0) FROM account.team_user u LEFT JOIN tob_hlv_ai_trang_thai t ON t.tai_khoan = u.username"
                + " WHERE u.la_bot = 1 AND u.username LIKE '" + TIEN_TO_TK + "%' ORDER BY u.username");
        for (String d : r) {
            if (d.startsWith("LOI SQL")) { loi("nap tai khoan: " + d); return; }
            String[] f = d.split("\t");
            if (f.length < 7 || timHo(f[0]) != null) continue;
            Ho h = new Ho();
            h.taiKhoan = f[0];
            h.userId = Integer.parseInt(f[1]);
            h.ten = f[2].isEmpty() ? HlvAiTen.moi(RD) : f[2];
            h.lop = Integer.parseInt(f[3]) >= 0 ? Integer.parseInt(f[3]) : chonTrongSo(dsSo("hlv_ai.ty_le_lop", 5, 20));
            int tc = java.util.Arrays.asList(HlvAiNguoiChoi.TINH_CACH).indexOf(f[4]);
            h.tinhCach = tc >= 0 ? tc : chonTrongSo(dsSo("hlv_ai.ty_le_tinh_cach", 5, 20));
            h.capMucTieu = Integer.parseInt(f[5]) > 0 ? Integer.parseInt(f[5]) : capMucTieuMoi();
            h.cap = Integer.parseInt(f[6]);
            h.vung = RD.nextInt(4) == 0 ? 1 : 0;
            h.matKhau = matKhau(h.taiKhoan);
            h.henVao = System.currentTimeMillis() + RD.nextInt(120000);
            HO.add(h);
            luuHo(h);
        }
    }

    static int capMucTieuMoi() {
        int[] c = dsSo("hlv_ai.cap_muc_tieu", 2, 0);
        int lo = c[0] > 0 ? c[0] : 10, hi = c[1] >= lo ? c[1] : lo + 50;
        return lo + RD.nextInt(hi - lo + 1);
    }

    static Ho timHo(String tk) {
        for (Ho h : HO) if (h.taiKhoan.equals(tk)) return h;
        return null;
    }

    /** Tao tai khoan bot moi (it moi nhip) cho du hlv_ai.so_luong. */
    static void taoThem(int can) {
        int tao = 0;
        int so = 1;
        for (Ho h : HO) {
            try { so = Math.max(so, Integer.parseInt(h.taiKhoan.substring(TIEN_TO_TK.length())) + 1); } catch (NumberFormatException ignored) { }
        }
        while (HO.size() < can && tao < 3) {
            String tk = String.format("%s%05d", TIEN_TO_TK, so++);
            String mk = matKhau(tk);
            List<String> r = BotChucNang.sql("INSERT IGNORE INTO account.team_user (username,password,phone,regdate,ban,provider,fromgame,la_bot)"
                    + " VALUES ('" + tk + "', PASSWORD('" + mk + "'), '', NOW(), 0, 0, 0, 1)");
            if (!r.isEmpty() && r.get(0).startsWith("LOI SQL")) { loi("tao tai khoan " + tk + ": " + r.get(0)); return; }
            // kich hoat nhu nguoi choi kich hoat tren web (open test mo kich hoat cho moi tai khoan)
            BotChucNang.sql("INSERT INTO 5h_active (userID, username, time_end) SELECT id, username, '" + (System.currentTimeMillis() / 1000 + 86400L * 3650)
                    + "' FROM account.team_user WHERE username = '" + tk + "' AND NOT EXISTS (SELECT 1 FROM 5h_active a WHERE a.username = '" + tk + "')");
            napHo();
            tao++;
            System.out.println("[HLV AI] tao tai khoan " + tk);
        }
    }

    static void luuHo(Ho h) {
        BotChucNang.sql("INSERT INTO tob_hlv_ai_trang_thai (tai_khoan,user_id,ten,lop,tinh_cach,cap_muc_tieu,cap_nhat) VALUES ('" + h.taiKhoan + "',"
                + h.userId + ",'" + h.ten + "'," + h.lop + ",'" + HlvAiNguoiChoi.TINH_CACH[h.tinhCach] + "'," + h.capMucTieu + ",NOW())"
                + " ON DUPLICATE KEY UPDATE user_id=VALUES(user_id), ten=VALUES(ten), lop=VALUES(lop), tinh_cach=VALUES(tinh_cach),"
                + " cap_muc_tieu=VALUES(cap_muc_tieu)");
    }

    static void luuTen(Ho h) {
        if (h.ten.matches("[0-9a-zA-Z]{1,20}")) BotChucNang.sql("UPDATE tob_hlv_ai_trang_thai SET ten='" + h.ten + "' WHERE tai_khoan='" + h.taiKhoan + "'");
    }

    // ================================================================== lich online
    /** Do dai phien (ms) theo tinh cach: cay / pvp dai, thuong ngan; chia tang toc. */
    static long doDaiPhien(Ho h) {
        int[][] khoang = {{60, 240}, {40, 180}, {30, 150}, {30, 150}, {15, 70}};
        int[] k = khoang[h.tinhCach];
        double phut = k[0] + RD.nextDouble() * RD.nextDouble() * (k[1] - k[0]);
        return (long) (phut * 60000L / Math.max(1.0, TANG_TOC));
    }

    static long doDaiNghi() {
        double phut = 20 + RD.nextDouble() * RD.nextDouble() * 300;
        return (long) (phut * 60000L / Math.max(1.0, TANG_TOC));
    }

    static int dangOnline() {
        int n = 0;
        for (Ho h : HO) if (h.dang != null) n++;
        return n;
    }

    static final Map<Integer, Integer> DUONG_CONG = new TreeMap<>(); // gio ao -> online (bao cao)

    static void nhip() {
        docCauHinh();
        boolean bat = "1".equals(CH.getOrDefault("hlv_ai.bat", "0").trim());
        int soLuong = (int) Math.max(0, Math.min(500, so("hlv_ai.so_luong", 30)));
        if (bat && HO.size() < soLuong) taoThem(soLuong);
        int[] lich = dsSo("hlv_ai.lich_gio", 24, 50);
        int g = gio();
        int dich = !bat ? 0 : (int) Math.round(Math.min(HO.size(), soLuong) * Math.max(0, Math.min(100, lich[g])) / 100.0);
        long bay = System.currentTimeMillis();
        // don phien da xong
        for (Ho h : HO) {
            HlvAiNguoiChoi p = h.dang;
            if (p != null && !p.dangChay()) {
                h.dang = null;
                h.raLuc = bay;
                h.henVao = bay + doDaiNghi();
            }
        }
        int on = dangOnline();
        if (on < dich) {
            List<Ho> cho = new ArrayList<>();
            for (Ho h : HO) if (h.dang == null && h.henVao <= bay) cho.add(h);
            Collections.shuffle(cho, RD);
            for (int i = 0; i < Math.min(3, Math.min(cho.size(), dich - on)); i++) batDau(cho.get(i));
        } else if (on > dich + (bat ? 1 : 0)) {
            List<Ho> dang = new ArrayList<>();
            for (Ho h : HO) if (h.dang != null) dang.add(h);
            dang.sort(Comparator.comparingLong(h -> h.dang.henRa));
            for (int i = 0; i < Math.min(bat ? 2 : dang.size(), on - dich); i++) dang.get(i).dang.henRa = bay;
        }
        DUONG_CONG.put(g, dangOnline());
    }

    static void batDau(Ho h) {
        HlvAiNguoiChoi p = new HlvAiNguoiChoi(h);
        p.henRa = System.currentTimeMillis() + doDaiPhien(h);
        h.dang = p;
        h.vaoLuc = System.currentTimeMillis();
        Thread t = new Thread(p, "hlv-" + h.taiKhoan);
        t.setDaemon(true);
        p.luong = t;
        t.start();
    }

    // ================================================================== tuong tac giua bot
    /** Ban do uu tien (hlv_ai.ban_do) hop cap minh va di toi duoc; -1 = khong co / khong cau hinh. */
    static int banDoUuTien(HlvAiNguoiChoi p) {
        String s = CH.getOrDefault("hlv_ai.ban_do", "").trim();
        if (s.isEmpty() || p.b == null) return -1;
        List<Integer> hop = new ArrayList<>();
        for (String x : s.split(",")) {
            try {
                int m = Integer.parseInt(x.trim());
                Integer l = BotXuyenSuot.LOAD.get(m);
                if (l == null || KichBanDS.laLang(m)) continue;
                boolean vua = false;
                for (int t : BotXuyenSuot.luoiQuai(l)) {
                    Integer c = BotXuyenSuot.CAP_QUAI.get(t);
                    if (c != null && c <= p.b.lv + 3 && c >= p.b.lv - 12) vua = true;
                }
                if (vua && BotXuyenSuot.duong(p.b.mapId, m) != null) hop.add(m);
            } catch (NumberFormatException ignored) {
            }
        }
        return hop.isEmpty() ? -1 : hop.get(RD.nextInt(hop.size()));
    }

    /** Doi thu PK: bot PvP khac dang online, cung bac 10 cap, khong o lang. */
    static HlvAiNguoiChoi doiThuPk(HlvAiNguoiChoi p) {
        List<HlvAiNguoiChoi> ds = new ArrayList<>();
        for (Ho h : HO) {
            HlvAiNguoiChoi q = h.dang;
            if (q == null || q == p || q.b == null || q.b.dut || q.ho.tinhCach != 1) continue;
            if ((q.b.lv - 1) / 10 != (p.b.lv - 1) / 10 || q.b.mapId < 0 || KichBanDS.laLang(q.b.mapId)) continue;
            ds.add(q);
        }
        return ds.isEmpty() ? null : ds.get(RD.nextInt(ds.size()));
    }

    /** Mon bot dang rao o cho ky gui: {npc, shop, actor nguoi ban, id mon, tpl, gia, charId ban}. */
    static final List<Object[]> CHO = new CopyOnWriteArrayList<>();

    static void raoBan(HlvAiNguoiChoi p, int npc, int shop, int id, int tpl, long gia) {
        CHO.add(new Object[]{p, npc, shop, id, tpl, gia, System.currentTimeMillis()});
        while (CHO.size() > 200) CHO.remove(0);
    }

    /** Mua mot mon bot khac dang rao (gia <= 1,3 gia NPC), nguoi ban phai con online. */
    static boolean muaChoBot(HlvAiNguoiChoi p) throws Exception {
        for (Object[] o : CHO) {
            HlvAiNguoiChoi ban = (HlvAiNguoiChoi) o[0];
            if (ban == p || ban.b == null || ban.b.dut) continue; // nguoi ban phai online (may chu kiem)
            long gia = (Long) o[5];
            if (gia > KichBanCap140.NguoiChoi.gia((Integer) o[4]) * 13 / 10 + 200 || gia > p.b.xu) continue;
            CHO.remove(o);
            return muaCho(p.b, (Integer) o[1], (Integer) o[2], ban.b.actor, (Integer) o[3]);
        }
        return false;
    }

    /** Mua mon id cua nguoi ban (actor) o quay npc/shop (goi 94, 95, xac nhan -32) - dung chung cho kich ban soak. */
    static boolean muaCho(BotChucNang.Bot b, int npc, int shop, int actorBan, int id) throws Exception {
        if (!KichBanDS.laLang(b.mapId) && b.mapId != 103) return false;
        if (b.mapId != 103 && !KichBanDS2.vaoNha(b, 103)) return false;
        long xu0 = b.xu;
        b.gui(94, (byte) 0, (byte) npc, (byte) shop);
        b.bom(800);
        b.gui(94, (byte) 1, (byte) npc, (byte) shop, (short) actorBan);
        b.bom(800);
        int m2 = b.moc();
        b.gui(95, (byte) npc, (byte) shop, (short) actorBan, (short) id, (byte) 0);
        b.cho(x -> x.layLenh(m2, -32) != null, 3000);
        BotChucNang.SuKien pp = b.layLenh(m2, -32);
        if (pp != null) {
            b.gui(-32, (short) pp.so[0], (byte) pp.so[1], "", (byte) 1);
            b.bom(1500);
        }
        b.lamMoi();
        KichBanDS2.raNgoai(b);
        return b.xu < xu0;
    }

    // ================================================================== trang thai
    static void ghiTrangThai() {
        StringBuilder sb = new StringBuilder();
        for (Ho h : HO) {
            HlvAiNguoiChoi p = h.dang;
            boolean on = p != null && p.b != null && !p.b.dut;
            String tenMap = on ? p.b.tenMap.replaceAll("[^\\p{L}\\p{N} .()-]", "") : "";
            if (sb.length() > 0) sb.append(',');
            sb.append("('").append(h.taiKhoan).append("',").append(h.userId).append(',').append(h.charId).append(",'").append(h.ten).append("',")
                    .append(h.lop).append(",'").append(HlvAiNguoiChoi.TINH_CACH[h.tinhCach]).append("',").append(on ? p.b.lv : h.cap).append(',')
                    .append(h.capMucTieu).append(',').append(on ? p.b.mapId : -1).append(",'").append(tenMap.length() > 60 ? tenMap.substring(0, 60) : tenMap)
                    .append("','").append(on ? p.viec : "nghi").append("',").append(on ? 1 : 0).append(',').append(on ? p.b.xu : 0).append(',')
                    .append(h.vaoLuc > 0 ? "FROM_UNIXTIME(" + h.vaoLuc / 1000 + ")" : "NULL").append(',')
                    .append(h.raLuc > 0 ? "FROM_UNIXTIME(" + h.raLuc / 1000 + ")" : "NULL").append(",NOW())");
        }
        if (sb.length() == 0) return;
        BotChucNang.sql("INSERT INTO tob_hlv_ai_trang_thai (tai_khoan,user_id,char_id,ten,lop,tinh_cach,cap,cap_muc_tieu,map_id,ten_map,hoat_dong,online,xu,"
                + "vao_luc,ra_luc,cap_nhat) VALUES " + sb + " ON DUPLICATE KEY UPDATE user_id=VALUES(user_id), char_id=VALUES(char_id), ten=VALUES(ten),"
                + " cap=VALUES(cap), map_id=VALUES(map_id), ten_map=VALUES(ten_map), hoat_dong=VALUES(hoat_dong), online=VALUES(online),"
                + " xu=IF(VALUES(online)=1, VALUES(xu), xu), vao_luc=VALUES(vao_luc), ra_luc=VALUES(ra_luc), cap_nhat=NOW()");
    }

    /** Don bo nho dung chung cua bo bot kiem thu (dich vu chay nhieu ngay). */
    static void donBoNho() {
        HoiQuy.KQ.clear();
        KichBanCap140.LOI.clear();
        KichBanCap140.CHU_NV.clear();
        if (!"1".equals(System.getProperty("hlv.soak"))) BotXuyenSuot.LOI_SERVER.clear();
        BotXuyenSuot.NHAT_KY_TUA.clear();
        for (Ho h : HO) {
            HlvAiNguoiChoi p = h.dang;
            if (p == null) continue;
            if (p.ket.size() > 50) p.ket.clear();
            if (p.loi.size() > 50) p.loi.clear();
            if (p.ketChi.size() > 50) p.ketChi.clear();
            if (p.dongThoiGian.size() > 300) p.dongThoiGian.clear();
        }
    }

    static String baoCao() {
        StringBuilder sb = new StringBuilder();
        sb.append("Viec (lan / dat):");
        for (Map.Entry<String, long[]> e : new TreeMap<>(DEM).entrySet()) sb.append(' ').append(e.getKey()).append('=').append(e.getValue()[0]).append('/').append(e.getValue()[1]);
        sb.append("\nLoi (gom):");
        for (Map.Entry<String, AtomicLong> e : new TreeMap<>(LOI).entrySet()) sb.append("\n  x").append(e.getValue()).append(' ').append(e.getKey());
        sb.append("\nDuong online theo gio: ").append(DUONG_CONG);
        return sb.toString();
    }

    // ================================================================== main
    public static void main(String[] a) throws Exception {
        if (a.length < 2) {
            System.out.println("DichVuHlvAi <host> <cong game>  (xem chu thich dau tep)");
            System.exit(2);
        }
        BotChucNang.HOST = a[0];
        BotChucNang.PORT = Integer.parseInt(a[1]);
        BotChucNang.TIEN_TO = TIEN_TO_TK;
        BotChucNang.Bot.CHUAN_CLIENT = true; // di lai / qua cong giong client that (khong CONG_SAI_O / NAP_MAP_LAP)
        BI_MAT = System.getenv("HLV_AI_BI_MAT");
        if (BI_MAT == null || BI_MAT.length() < 16) {
            if (!"1".equals(System.getProperty("hlv.thu"))) {
                System.out.println("[HLV AI] thieu HLV_AI_BI_MAT (>= 16 ky tu) - dat trong /etc/htbb/hlv-ai.env");
                System.exit(2);
            }
            BI_MAT = "hlv-ai-may-thu-khong-dung-that";
        }
        if (BotChucNang.DB.isEmpty()) {
            System.out.println("[HLV AI] thieu -Ddb=<CSDL game>");
            System.exit(2);
        }
        long het = Long.getLong("hlv.phut", 0L) > 0 ? System.currentTimeMillis() + Long.getLong("hlv.phut") * 60000L : Long.MAX_VALUE;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (Ho h : HO) if (h.dang != null) h.dang.dungLai = true;
            try { Thread.sleep(3000); } catch (InterruptedException ignored) { }
            for (Ho h : HO) if (h.dang != null && h.dang.b != null) h.dang.b.close();
            try { ghiTrangThaiTat(); } catch (Exception ignored) { }
            System.out.println("[HLV AI] dung.\n" + baoCao());
            if ("1".equals(System.getProperty("hlv.soak"))) KiemHlvAiSoak.ketThuc();
        }));
        docCauHinh();
        napHo();
        System.out.println("[HLV AI] bat dau: " + HO.size() + " tai khoan bot, cau hinh " + CH);
        if ("1".equals(System.getProperty("hlv.soak"))) KiemHlvAiSoak.batDau();
        long lanTT = 0, lanIn = 0, lanDon = 0;
        while (System.currentTimeMillis() < het) {
            try {
                nhip();
                long bay = System.currentTimeMillis();
                if (bay - lanTT > 60000) { ghiTrangThai(); lanTT = bay; }
                if (bay - lanDon > 600000) { donBoNho(); lanDon = bay; }
                if (bay - lanIn > 300000) {
                    lanIn = bay;
                    Runtime r = Runtime.getRuntime();
                    System.out.printf("[HLV AI] %s gio ao %d: online %d/%d, heap %d MB, luong %d | %s%n", new SimpleDateFormat("HH:mm:ss").format(new Date()),
                            gio(), dangOnline(), HO.size(), (r.totalMemory() - r.freeMemory()) >> 20, Thread.activeCount(), baoCao().split("\n")[0]);
                }
            } catch (Throwable e) {
                loi("nhip: " + e);
                System.out.println("[HLV AI] loi nhip: " + e);
            }
            Thread.sleep(20000);
        }
        System.exit(0);
    }

    static void ghiTrangThaiTat() {
        BotChucNang.sql("UPDATE tob_hlv_ai_trang_thai SET online = 0, hoat_dong = 'nghi', cap_nhat = NOW() WHERE online = 1");
    }

    static Map<String, Object> tomTat() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("online", dangOnline());
        m.put("tong", HO.size());
        return m;
    }
}
