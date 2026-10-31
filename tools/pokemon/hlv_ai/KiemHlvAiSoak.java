import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * KIEM NGAM (soak) HUAN LUYEN VIEN AI - CHI MAY THU (sv.cho_phep_thu_nghiem=1). Chay trong cung JVM dich vu
 * (-Dhlv.soak=1) de thay danh sach bot / mon bot rao ban.
 *
 * <pre>
 *  Chuan bi (ghi vao bao cao): GM kt040 cap cap / xu cho 4 "nguoi that" kt101..kt104 (tai khoan thuong, khong GM) va dua
 *  mot nua bot len cap 20..40 (gia lap quan the bot da choi lau - bot moi van tu cap 1).
 *  Nguoi that (moi nguoi mot luong, ban do bot dang o): thu khai thac CO Y moi 2-4 phut, xoay vong:
 *    gd_lap        moi bot giao dich, dua 1 binh, nhan thuoc bot dua (lap lai)       -> tong gia tri <= han muc ngay
 *    gd_khong_dua  moi bot giao dich, khong dua gi (xin cho khong)                    -> bot huy, nguoi khong duoc gi
 *    mua_cho_bot   mua mon bot rao (gia >= gia tham chieu)                            -> khong loi khi ban lai NPC
 *    pk_bot        danh bot (goi 6) o ban do ngoai lang                                -> khong rot xu / do
 *    dau_truong    thach dau Dau truong voi bot neu co trong danh sach doi thu
 *  Kiem toan moi -Dhlv.soakPhut (10) phut -> bang markdown (-Dhlv.soakRa): tong xu bot / nguoi (CSDL), xu bot tao / dot /
 *  tieu hom nay, vat huy, gia tri bot -> nguoi (max moi nguoi vs han muc), trung vi gia cho, so bot online / ket (cung o
 *  > 15 phut ngoai treo), ket noi lai, cau chat (lap nguyen van), loi may chu (" E "), tick p99 ([NHIP] game log), ket
 *  noi CSDL cua user htbb.
 * </pre>
 */
final class KiemHlvAiSoak {

    private KiemHlvAiSoak() {
    }

    static final String RA = System.getProperty("hlv.soakRa", "hlv_ai_soak.md");
    static final int PHUT = Integer.getInteger("hlv.soakPhut", 10);
    static final String GAME_LOG = System.getProperty("hlv.gameLog", "");
    static final Map<String, long[]> KHAI_THAC = new ConcurrentHashMap<>(); // ten -> {lan, nguoi duoc loi (gia tri), bi chan}
    static final List<String> GHI_CHU = Collections.synchronizedList(new ArrayList<>());
    static final Map<String, Object[]> VI_TRI = new ConcurrentHashMap<>(); // tai khoan -> {map, ox, oy, luc doi cuoi, viec}
    static long viTriLog;

    static void batDau() {
        Thread t = new Thread(() -> {
            try {
                chay();
            } catch (Throwable e) {
                System.out.println("[SOAK] dung: " + e);
                e.printStackTrace(System.out);
            }
        }, "hlv-soak");
        t.setDaemon(true);
        t.start();
    }

    static void ghi(String s) {
        System.out.println("[SOAK] " + s);
        try (FileOutputStream f = new FileOutputStream(RA, true)) {
            f.write((s + "\n").getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignored) {
        }
    }

    static void khaiThac(String ten, long loi, boolean chan) {
        long[] a = KHAI_THAC.computeIfAbsent(ten, k -> new long[3]);
        synchronized (a) {
            a[0]++;
            a[1] += loi;
            a[2] += chan ? 1 : 0;
        }
    }

    static BotChucNang.Bot gm;

    static synchronized void gm(String lenh) throws Exception {
        if (gm == null || gm.dut) {
            gm = new BotChucNang.Bot("kt040");
            String l = gm.dangNhap("123456");
            if (l == null) l = gm.vaoGame(0);
            if (l != null) throw new IllegalStateException("GM kt040: " + l);
        }
        gm.gm(lenh, 1200);
        GHI_CHU.add(new SimpleDateFormat("HH:mm").format(new Date()) + " GM: " + lenh);
    }

    // ================================================================== nguoi that
    static final class NguoiThat extends BotXuyenSuot.Dong {
        final java.util.Random rd = new java.util.Random();
        int luot;

        NguoiThat(String user) {
            super(0, user, Long.MAX_VALUE);
        }

        @Override
        void ghi(String s) {
            System.out.println("[NGUOI " + user + " lv" + (b == null ? 0 : b.lv) + " map" + (b == null ? -1 : b.mapId) + "] " + s);
        }

        @Override
        void vao() throws Exception {
            for (int lan = 0; lan < 10; lan++) {
                b = new BotChucNang.Bot(user);
                String l = b.dangNhap("123456");
                if (l == null) l = b.vaoGame(lan % 5);
                if (l == null) break;
                b.close();
                b = null;
                Thread.sleep(5000);
            }
            if (b == null) throw new IllegalStateException("nguoi that " + user + " khong vao duoc");
            HoiQuy.napTheGioi(b);
            KichBanCap140.napTen();
            KichBanHuongDan.roiSan(b);
            KichBanHoiQuy.boQuaHuongDan(b);
            b.lamMoi();
            capTruoc = b.lv;
        }

        void run2() {
            while (true) {
                try {
                    vongChinh();
                } catch (java.net.SocketException | java.io.EOFException e) {
                    try { b.close(); Thread.sleep(5000); vao(); } catch (Exception x) { System.out.println("[SOAK] " + user + " vao lai: " + x); }
                } catch (Exception e) {
                    System.out.println("[SOAK] " + user + ": " + e);
                    try { Thread.sleep(10000); } catch (InterruptedException ie) { return; }
                }
            }
        }

        @Override
        void vongChinh() throws Exception {
            if (b.su.size() > 3000) b.su.subList(0, 2500).clear();
            kiem();
            HlvAiNguoiChoi bot = chonBot();
            if (bot == null) { luyen(2); return; }
            String[] viec = {"gd_lap", "gd_khong_dua", "mua_cho_bot", "pk_bot", "dau_truong"};
            String v = viec[luot++ % viec.length];
            try {
                switch (v) {
                    case "gd_lap": gdLap(bot, true); break;
                    case "gd_khong_dua": gdLap(bot, false); break;
                    case "mua_cho_bot": muaChoBot(); break;
                    case "pk_bot": pkBot(bot); break;
                    default: dauTruong(); break;
                }
            } catch (java.net.SocketException | java.io.EOFException e) {
                throw e;
            } catch (Exception e) {
                ghi(v + ": " + e);
                khaiThac(v + ".loi", 0, false);
            }
            b.bom(60000 + rd.nextInt(60000));
        }

        HlvAiNguoiChoi chonBot() {
            List<HlvAiNguoiChoi> ds = new ArrayList<>();
            for (DichVuHlvAi.Ho h : DichVuHlvAi.HO) {
                HlvAiNguoiChoi p = h.dang;
                if (p != null && p.b != null && !p.b.dut && p.b.mapId >= 0 && BotXuyenSuot.LOAD.containsKey(p.b.mapId)) ds.add(p);
            }
            if (ds.isEmpty()) return null;
            for (HlvAiNguoiChoi p : ds) if (p.b.mapId == b.mapId) return p;
            return ds.get(rd.nextInt(ds.size()));
        }

        boolean gapBot(HlvAiNguoiChoi bot) throws Exception {
            if (bot.b.mapId != b.mapId && !toi(bot.b.mapId)) return false;
            BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
            if (bd == null) return false;
            int[] o = bd.oGan(bot.b.px / 16, bot.b.py / 16, 2);
            if (o != null) b.diDuong(bd, o[0], o[1]);
            b.bom(800);
            return Math.abs(bot.b.px - b.px) <= 160 && Math.abs(bot.b.py - b.py) <= 160 && bot.b.mapId == b.mapId;
        }

        /** Moi bot giao dich; dua = dat 1 binh thuoc 1 roi khoa + xac nhan; tinh gia tri thuoc nhan duoc tu bot. */
        void gdLap(HlvAiNguoiChoi bot, boolean dua) throws Exception {
            if (!gapBot(bot)) { khaiThac((dua ? "gd_lap" : "gd_khong_dua") + ".khong_gap", 0, false); return; }
            b.lamMoi();
            Map<Integer, Integer> truoc = new HashMap<>(b.thuoc);
            int m = b.moc();
            b.gui(66, (byte) 0, (short) bot.b.actor);
            boolean mo = b.cho(x -> KichBanBatThuong.giaoDich(x, m, 1), 15000);
            if (!mo) { khaiThac(dua ? "gd_lap.khong_mo" : "gd_khong_dua.khong_mo", 0, false); return; }
            if (dua && b.soThuoc(1) > 0) b.gui(66, (byte) 2, (byte) 1, (byte) 1, (short) 1);
            b.bom(1000);
            b.gui(66, (byte) 4);
            b.bom(25000); // cho bot quyet dinh (bot cho toi 20 giay doi phuong dat)
            b.gui(66, (byte) 5);
            b.bom(3000);
            b.gui(66, (byte) 3);
            b.bom(500);
            b.lamMoi();
            long loi = 0;
            for (Map.Entry<Integer, Integer> e : b.thuoc.entrySet()) {
                int them = e.getValue() - truoc.getOrDefault(e.getKey(), 0);
                if (them > 0) loi += them * 500L; // uoc gia binh (gia NPC binh thuong ~ 100..1000 xu)
            }
            if (dua) loi -= 500L;
            boolean chan = b.doiChu(m, 0, "giới hạn giao dịch") != null;
            khaiThac(dua ? "gd_lap" : "gd_khong_dua", Math.max(0, loi), chan);
        }

        /** Mua mon bot rao: gia phai >= gia tham chieu NPC (ban lai NPC khong loi). */
        void muaChoBot() throws Exception {
            for (Object[] o : DichVuHlvAi.CHO) {
                HlvAiNguoiChoi ban = (HlvAiNguoiChoi) o[0];
                if (ban.b == null || ban.b.dut) continue;
                long gia = (Long) o[5], tham = KichBanCap140.NguoiChoi.gia((Integer) o[4]);
                if (gia > b.xu) continue;
                if (!KichBanDS.laLang(b.mapId)) toi(0);
                long xu0 = b.xu;
                boolean ok = DichVuHlvAi.muaCho(b, (Integer) o[1], (Integer) o[2], ban.b.actor, (Integer) o[3]);
                DichVuHlvAi.CHO.remove(o);
                // ban lai NPC duoc toi da gia tham chieu (thuong it hon): loi = tham - gia tra
                khaiThac("mua_cho_bot", ok ? Math.max(0, tham - (xu0 - b.xu)) : 0, !ok);
                return;
            }
            khaiThac("mua_cho_bot.khong_co_tin", 0, false);
        }

        /** Danh bot o ngoai lang: khong rot xu / do cho nguoi danh. */
        void pkBot(HlvAiNguoiChoi bot) throws Exception {
            if (KichBanDS.laLang(bot.b.mapId) || !gapBot(bot)) { khaiThac("pk_bot.khong_gap", 0, false); return; }
            b.lamMoi();
            long xu0 = b.xu;
            int mon0 = b.tongMon();
            for (int i = 0; i < 12; i++) { b.gui(6, (short) bot.b.actor, (byte) 0); b.bom(500); }
            b.bom(2000);
            b.lamMoi();
            khaiThac("pk_bot", Math.max(0, b.xu - xu0) + Math.max(0, b.tongMon() - mon0) * 1000L, false);
        }

        void dauTruong() throws Exception {
            if (!toi(0)) return;
            if (KichBanDauTruong.chonDauTruong(b, "Đấu trường Bảo Bối") < 0) { khaiThac("dau_truong.khong_mo", 0, false); return; }
            int m = b.moc();
            b.gui(-109, (byte) 2, (byte) 0);
            byte[] dt = KichBanDauTruong.cho(b, m, 1, 5000);
            List<Integer> ds = dt == null ? new ArrayList<>() : KichBanDauTruong.doiThu(dt);
            int doi = -1;
            for (int id : ds) for (DichVuHlvAi.Ho h : DichVuHlvAi.HO) if (h.charId == id) doi = id;
            if (doi < 0) { khaiThac("dau_truong.khong_co_bot", 0, false); return; }
            m = b.moc();
            b.lamMoi();
            long xu0 = b.xu;
            b.gui(-109, (byte) 3, doi, (byte) 0);
            byte[] bd = KichBanDauTruong.cho(b, m, 2, 5000);
            b.bom(30000);
            b.lamMoi();
            khaiThac("dau_truong", Math.max(0, b.xu - xu0), bd == null);
        }
    }

    // ================================================================== kiem toan
    static long so(String q) {
        String s = BotChucNang.sql1(q);
        try {
            return s == null ? 0L : Long.parseLong(s.trim().split("\\.")[0]);
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

    static String quetGameLog() {
        if (GAME_LOG.isEmpty()) return "-";
        double p99 = -1;
        int loi = 0;
        try (RandomAccessFile r = new RandomAccessFile(GAME_LOG, "r")) {
            if (r.length() < viTriLog) viTriLog = 0;
            r.seek(viTriLog);
            String l;
            Pattern p = Pattern.compile("tre p50=\\S+ p99=<?(\\d+)ms");
            while ((l = r.readLine()) != null) {
                Matcher m = p.matcher(l);
                if (m.find()) p99 = Math.max(p99, Double.parseDouble(m.group(1)));
                if (l.length() > 19 && l.charAt(19) == 'E' && l.contains(" E ")) loi++;
                if (l.contains("Exception") && !l.contains("SocketException")) loi++;
            }
            viTriLog = r.getFilePointer();
        } catch (IOException e) {
            return "?";
        }
        return (p99 < 0 ? "-" : (long) p99 + " ms") + " / loi " + loi;
    }

    static int dem(String viec) {
        long[] a = DichVuHlvAi.DEM.get(viec);
        return a == null ? 0 : (int) a[0];
    }

    static void chay() throws Exception {
        ghi("# Soak Huấn luyện viên AI - " + new Date());
        Thread.sleep(90000);
        // ---- chuan bi: nguoi that + mot nua bot len cap 20..40 (gia lap quan the bot da choi lau)
        List<NguoiThat> nt = new ArrayList<>();
        for (int i = 101; i <= 104; i++) {
            NguoiThat n = new NguoiThat(String.format("kt%03d", i));
            nt.add(n);
            Thread t = new Thread(() -> {
                try { n.vao(); gm("slv " + n.b.ten + " 30 0"); gm("smn " + n.b.ten + " 300000 0 0"); n.b.lamMoi(); } catch (Exception e) { System.out.println("[SOAK] chuan bi " + n.user + ": " + e); }
                n.run2();
            }, "soak-" + n.user);
            t.setDaemon(true);
            t.start();
            Thread.sleep(5000);
        }
        int len = 0;
        for (DichVuHlvAi.Ho h : DichVuHlvAi.HO) {
            HlvAiNguoiChoi p = h.dang;
            if (p != null && p.b != null && !p.b.dut && len < DichVuHlvAi.HO.size() / 2 && p.b.lv < 15) {
                gm("slv " + p.b.ten + " " + (20 + DichVuHlvAi.RD.nextInt(21)) + " 0");
                len++;
            }
        }
        ghi("\nChuẩn bị: 4 người thật kt101..kt104 (cấp 30, 300.000 xu qua GM), " + len + " bot đang online lên cấp 20..40 (GM, giả lập bot chơi lâu).\n");
        ghi("| Phút | Bot online | Tổng xu bot | Tổng xu người | Ảnh chụp máy chủ bot/người (gồm online) | Xu bot tạo / đốt / tiêu (ngày) | Vật huỷ | Bot→người max/người (hạn mức) | Chuyển / chặn | Trung vị chợ | Kẹt | Kết nối lại | Chat (lặp) | Tick p99 / lỗi log | Kết nối CSDL |");
        ghi("|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|");
        long batDau = System.currentTimeMillis();
        long lanAudit = 0;
        while (true) {
            theoDoiViTri();
            if (System.currentTimeMillis() - lanAudit >= PHUT * 60000L) {
                lanAudit = System.currentTimeMillis();
                kiemToan((int) ((lanAudit - batDau) / 60000));
            }
            Thread.sleep(30000);
        }
    }

    /** Vi tri moi bot moi 30 giay: cung map + o > 15 phut (khong treo, khong nghi) = ket. */
    static void theoDoiViTri() {
        long bay = System.currentTimeMillis();
        for (DichVuHlvAi.Ho h : DichVuHlvAi.HO) {
            HlvAiNguoiChoi p = h.dang;
            if (p == null || p.b == null || p.b.dut) { VI_TRI.remove(h.taiKhoan); continue; }
            String vt = p.b.mapId + ":" + p.b.px / 16 + "," + p.b.py / 16;
            Object[] cu = VI_TRI.get(h.taiKhoan);
            if (cu == null || !cu[0].equals(vt) || "treo".equals(p.viec)) VI_TRI.put(h.taiKhoan, new Object[]{vt, bay, p.viec});
        }
    }

    static final List<String> MOC_XU = new ArrayList<>();
    static final List<String> LOI_MAU = Collections.synchronizedList(new ArrayList<>());

    static void kiemToan(int phut) {
        String tk = "SELECT id FROM account.team_user WHERE la_bot = 1";
        long xuBot = so("SELECT IFNULL(SUM(gold + kho_xu),0) FROM tob_char WHERE userid IN (" + tk + ")");
        long xuNguoi = so("SELECT IFNULL(SUM(gold + kho_xu),0) FROM tob_char WHERE userid NOT IN (" + tk + ")");
        String ngay = BotChucNang.sql1("SELECT CONCAT(xu_tao,'/',xu_dot,'/',xu_tieu,'|',vat_huy,'|',so_chuyen,'/',so_chan) FROM tob_hlv_ai_kinh_te_ngay ORDER BY ngay DESC LIMIT 1");
        String[] f = ngay == null ? new String[]{"-", "-", "-"} : ngay.split("\\|");
        long maxNguoi = so("SELECT IFNULL(MAX(s),0) FROM (SELECT SUM(gia_tri) s FROM tob_hlv_ai_chuyen WHERE huong = 1 AND bi_chan = 0 AND luc >= CURDATE() GROUP BY nguoi_id) x");
        String han = BotChucNang.sql1("SELECT IFNULL((SELECT gia_tri FROM cau_hinh WHERE khoa='hlv_ai.chuyen_moi_nguoi_ngay'),(SELECT mac_dinh FROM cau_hinh_danh_muc WHERE khoa='hlv_ai.chuyen_moi_nguoi_ngay'))");
        String tv = BotChucNang.sql1("SELECT CONCAT(COUNT(*),' bán, ',IFNULL(GROUP_CONCAT(DISTINCT CONCAT(loai,':',ma) ORDER BY id DESC SEPARATOR ' '),'')) FROM tob_hlv_ai_gia WHERE luc >= NOW() - INTERVAL 1 DAY");
        int ket = 0;
        long bay = System.currentTimeMillis();
        for (Map.Entry<String, Object[]> e : VI_TRI.entrySet()) if (bay - (Long) e.getValue()[1] > 15 * 60000L) ket++;
        int chat = dem("chat_ban_do") + dem("chat_the_gioi") + dem("tra_loi_chat") + dem("tra_loi_rieng");
        long ketNoi = so("SELECT COUNT(*) FROM information_schema.PROCESSLIST WHERE USER = 'htbb'");
        String tick = quetGameLog();
        BotXuyenSuot.quetLog();
        int loiMoi;
        synchronized (BotXuyenSuot.LOI_SERVER) {
            loiMoi = BotXuyenSuot.LOI_SERVER.size();
            for (String s : BotXuyenSuot.LOI_SERVER) if (LOI_MAU.size() < 30) LOI_MAU.add(s);
            BotXuyenSuot.LOI_SERVER.clear();
        }
        tick += " / nhat ky E " + loiMoi;
        String moc = BotChucNang.sql1("SELECT CONCAT(FORMAT(tong_xu_bot+kho_xu_bot,0),' / ',FORMAT(tong_xu_nguoi+kho_xu_nguoi,0),' @',DATE_FORMAT(luc,'%H:%i')) FROM tob_hlv_ai_moc ORDER BY luc DESC LIMIT 1");
        ghi(String.format("| %d | %d/%d | %,d | %,d | %s | %s | %s | %,d (%s) | %s | %s | %d | %d | %d (0) | %s | %d |", phut, DichVuHlvAi.dangOnline(), DichVuHlvAi.HO.size(),
                xuBot, xuNguoi, moc == null ? "-" : moc, f[0], f.length > 1 ? f[1] : "-", maxNguoi, han, f.length > 2 ? f[2] : "-", tv == null ? "-" : tv.length() > 60 ? tv.substring(0, 60) : tv,
                ket, dem("ket_noi_lai"), chat, tick, ketNoi));
        MOC_XU.add(phut + ":" + xuBot + ":" + xuNguoi);
        StringBuilder kt = new StringBuilder("  khai thác (lần / lợi cho người / bị chặn):");
        for (Map.Entry<String, long[]> e : new TreeMap<>(KHAI_THAC).entrySet()) kt.append(' ').append(e.getKey()).append('=').append(e.getValue()[0]).append('/').append(e.getValue()[1]).append('/').append(e.getValue()[2]);
        System.out.println("[SOAK] " + kt);
        System.out.println("[SOAK] " + DichVuHlvAi.baoCao().replace('\n', ' '));
    }

    /** Ket thuc: bao cao phu tinh nang + khai thac + ghi chu GM. */
    static void ketThuc() {
        ghi("\n## Khai thác có chủ ý (lần / giá trị người được lợi / bị chặn)\n");
        for (Map.Entry<String, long[]> e : new TreeMap<>(KHAI_THAC).entrySet()) ghi("- " + e.getKey() + ": " + e.getValue()[0] + " lần, lợi " + e.getValue()[1] + " xu, chặn " + e.getValue()[2]);
        ghi("\n## Phủ hoạt động (lần / đạt)\n\n" + DichVuHlvAi.baoCao());
        ghi("\n## Lỗi máy chủ (mẫu)\n");
        for (String s : LOI_MAU) ghi("- `" + s.replace('`', '\'') + "`");
        ghi("\n## Ghi chú chuẩn bị (GM)\n");
        for (String s : GHI_CHU) ghi("- " + s);
    }

    static Map<String, Object> tomTat() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("khai_thac", KHAI_THAC.size());
        return m;
    }
}
