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

    static void batDauKhaiThac() {
        Thread t = new Thread(() -> {
            try {
                khaiThacTapTrung();
            } catch (Throwable e) {
                System.out.println("[KHAI THAC] dung: " + e);
                e.printStackTrace(System.out);
            }
        }, "hlv-khai-thac");
        t.setDaemon(true);
        t.start();
    }

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
        if (gm == null || gm.dut || !gm.conSong()) {
            if (gm != null) gm.close();
            gm = new BotChucNang.Bot("kt040");
            String l = gm.dangNhap("123456");
            if (l == null) l = gm.vaoGame(0);
            if (l != null) throw new IllegalStateException("GM kt040: " + l);
        }
        try {
            gm.gm(lenh, 1200);
        } catch (java.net.SocketException e) { // GM im lang > 180 s bi may chu ngat (nua mo): vao lai, gui lai mot lan
            gm.close();
            gm = new BotChucNang.Bot("kt040");
            String l = gm.dangNhap("123456");
            if (l == null) l = gm.vaoGame(0);
            if (l != null) throw new IllegalStateException("GM kt040: " + l);
            gm.gm(lenh, 1200);
        }
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
            String[] viec0 = {"gd_lap", "thach_dau_bot", "trao_doi_bot", "gd_khong_dua", "pk_bot", "mua_cho_bot", "moi_doi", "dau_truong"};
            boolean canLang = !"pk_bot".equals(viec0[luot % viec0.length]);
            HlvAiNguoiChoi bot = chonBot(canLang);
            if (bot == null) { cho(30000); return; } // dung o lang cho bot ghe qua (nguoi that khong roi lang: khong ket ban do)
            String v = viec0[luot++ % viec0.length];
            try {
                switch (v) {
                    case "gd_lap": gdLap(bot, true); break;
                    case "gd_khong_dua": gdLap(bot, false); break;
                    case "mua_cho_bot": muaChoBot(); break;
                    case "pk_bot": pkBot(bot); break;
                    case "thach_dau_bot": thachDauBot(bot); break;
                    case "trao_doi_bot": traoDoiBot(bot); break;
                    case "moi_doi": moiDoi(bot); break;
                    default: dauTruong(); break;
                }
            } catch (java.net.SocketException | java.io.EOFException e) {
                throw e;
            } catch (Exception e) {
                ghi(v + ": " + e);
                khaiThac(v + ".loi", 0, false);
            }
            // nguoi that cho 1-2 phut (client that van gui nhip: may chu ngat sau 180 giay im lang)
            cho(60000 + rd.nextInt(60000));
        }

        void cho(long ms) throws Exception {
            long het = System.currentTimeMillis() + ms;
            while (System.currentTimeMillis() < het && !b.dut) { b.bom(Math.min(20000, Math.max(100, het - System.currentTimeMillis()))); b.lamMoi(); }
        }

        /** Bot de khai thac; giao dich: uu tien bot dung trong lang (it di chuyen, gap duoc) cap >= 20. */
        HlvAiNguoiChoi chonBot(boolean lang) {
            List<HlvAiNguoiChoi> ds = new ArrayList<>();
            for (DichVuHlvAi.Ho h : DichVuHlvAi.HO) {
                HlvAiNguoiChoi p = h.dang;
                if (p != null && p.b != null && !p.b.dut && p.b.mapId >= 0 && BotXuyenSuot.LOAD.containsKey(p.b.mapId)
                        && (!lang || KichBanDS.laLang(p.b.mapId) && p.b.lv >= 20)) ds.add(p);
            }
            if (ds.isEmpty()) return null;
            for (HlvAiNguoiChoi p : ds) if (p.b.mapId == b.mapId) return p;
            return null; // chi bot cung ban do (lang) - khong di theo bot ra bai (lan chay 31/10: nguoi that ket o Viridian)
        }

        boolean gapBot(HlvAiNguoiChoi bot) throws Exception {
            for (int lan = 0; lan < 3; lan++) {
                if (bot.b == null || bot.b.dut) return false;
                if (bot.b.mapId != b.mapId) return false;
                BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
                if (bd == null) return false;
                int[] o = bd.oGan(bot.b.px / 16, bot.b.py / 16, 2);
                if (o != null) b.diDuong(bd, o[0], o[1]);
                b.bom(600);
                if (Math.abs(bot.b.px - b.px) <= 96 && Math.abs(bot.b.py - b.py) <= 96 && bot.b.mapId == b.mapId) return true;
            }
            return false;
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
            b.bom(12000); // bot tra loi ngay khi thay minh dat (goi 66/2); cho them cho cham mang
            b.lamMoi();
            b.bom(12000);
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
        /** Chuan bi (ghi bao cao): cap 45 (kt109: 50 + 100 trieu xu + huy hieu -> lap doi), 300.000 xu, 1 Pokemon trong Hop PC. */
        void chuanBi() throws Exception {
            vao();
            int id = b.charDb;
            b.close();
            Thread.sleep(4000);
            KichBanTinhNangThuong.ganHop(id, 16 + id % 3);
            GHI_CHU.add("SQL: Hop PC " + user + " 1 con (thu trao doi Pokemon voi bot)");
            vao();
            boolean chu = user.equals("kt109") || user.equals("kt116");
            gm("slv " + b.ten + " " + (chu ? 50 : 45) + " 0");
            gm("smn " + b.ten + " 300000 0 0");
            if (chu) {
                gm("smn " + b.ten + " 100000000 0 0");
                gm("spt " + b.ten + " 31 1");
                b.lamMoi();
                int m = b.moc();
                b.gui(-9, new byte[0]);
                b.cho(x -> x.coLenh(m, -9), 4000);
                BotChucNang.SuKien e = b.layLenh(m, -9);
                if (e != null && e.d.length >= 4) {
                    int m2 = b.moc();
                    b.gui(-10, (short) (((e.d[2] & 255) << 8) | (e.d[3] & 255)), "DoiNT" + user); // ten rieng: kt109 + kt116 cung lap
                    chuDoi = b.cho(x -> x.coLenh(m2, -10), 5000);
                }
                if (!chuDoi && b.doiChu(m, 0, "đang ở trong đội") != null) chuDoi = true; // lan chay truoc da lap
                GHI_CHU.add(user + " lap doi (100 trieu xu GM): " + chuDoi + (chuDoi ? "" : " | " + b.chuTu(m)));
            }
            b.lamMoi();
        }

        /**
         * PK bot that: mua + deo khan PK, toi ban do ngoai lang co bot DANG DEO khan, danh; doi chieu xu / mon truoc - sau
         * (PK khong rot gi). Khong bot nao deo khan: danh bot trong lang (may chu chan PK trong lang).
         */
        void pkBot(HlvAiNguoiChoi bot0) throws Exception {
            HlvAiNguoiChoi bot = null;
            for (DichVuHlvAi.Ho h : DichVuHlvAi.HO) {
                HlvAiNguoiChoi q = h.dang;
                if (q != null && q.b != null && !q.b.dut && q.khanMinh >= 14 && !KichBanDS.laLang(q.b.mapId)) bot = q;
            }
            b.lamMoi();
            long xu0 = b.xu;
            int mon0 = b.tongMon();
            if (bot == null) {
                if (bot0.b.mapId == b.mapId && gapBot(bot0)) {
                    for (int i = 0; i < 8; i++) { b.gui(6, (short) bot0.b.actor, (byte) 0); b.bom(500); }
                    b.lamMoi();
                    khaiThac("pk_bot.trong_lang", Math.max(0, b.xu - xu0), false);
                } else {
                    khaiThac("pk_bot.khong_co_bot_deo_khan", 0, false);
                }
                return;
            }
            int khan = -1;
            for (int t = 14; t <= 18; t++) if (b.soThuoc(t) > 0) khan = t;
            if (khan < 0 && KichBanDS2.vaoNha(b, 103)) {
                khan = bot.khanMinh == 14 ? 15 : 14;
                b.gui(24, (byte) 1, (byte) 4, (short) khan, (short) 1);
                b.bom(1200);
                KichBanDS2.raNgoai(b);
            }
            int langCu = b.mapId;
            if (khan < 0 || !toi(bot.b.mapId)) { khaiThac("pk_bot.khong_toi", 0, false); toi(langCu); return; }
            b.gui(22, (byte) khan);
            b.bom(800);
            int don = 0;
            for (int lan = 0; lan < 40 && bot.b != null && !bot.b.dut && bot.b.mapId == b.mapId && b.hp > 0; lan++) {
                int[] vt = b.nguoi.get(bot.b.actor);
                if (vt != null && lan % 6 == 0) {
                    BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
                    int[] o = bd == null ? null : bd.oGan(vt[0] / 16, vt[1] / 16, 2);
                    if (o != null) b.diDuong(bd, o[0], o[1]);
                }
                b.gui(6, (short) bot.b.actor, (byte) 0);
                don++;
                b.bom(500);
            }
            b.bom(11000);
            b.gui(22, (byte) khan);
            if (b.hp <= 0) hoiSinh();
            b.lamMoi();
            khaiThac("pk_bot", Math.max(0, b.xu - xu0) + Math.max(0, b.tongMon() - mon0) * 1000L, false);
            ghi("pk bot " + bot.b.ten + ": " + don + " don");
            toi(langCu);
        }

        /** Thach dau 1-1 cuoc 5.000 xu voi bot cap 40+ cung lang; doi chieu xu (chan khi het han muc ngay). */
        void thachDauBot(HlvAiNguoiChoi bot0) throws Exception {
            HlvAiNguoiChoi bot = null;
            for (DichVuHlvAi.Ho h : DichVuHlvAi.HO) {
                HlvAiNguoiChoi q = h.dang;
                if (q != null && q.b != null && !q.b.dut && q.b.mapId == b.mapId && q.b.lv >= 40) bot = q;
            }
            if (bot == null) { khaiThac("thach_dau_bot.khong_co_bot_40", 0, false); return; }
            b.lamMoi();
            long xu0 = b.xu;
            int m = b.moc();
            b.gui(-31, (short) b.actor, (byte) 5, "5000");
            b.bom(800);
            b.gui(-31, (short) b.actor, (byte) 6, bot.b.ten);
            boolean vao = b.cho(x -> x.mapId == 205 || x.dut, 20000);
            boolean chan = b.doiChu(m, 0, "giới hạn giao dịch") != null;
            if (!vao) { khaiThac("thach_dau_bot.khong_vao", 0, chan); ghi("thach dau: " + b.chuTu(m)); return; }
            long het = System.currentTimeMillis() + 6 * 60000L;
            while (b.mapId == 205 && !b.dut && System.currentTimeMillis() < het) {
                if (b.hp > 0) b.gui(6, (short) bot.b.actor, (byte) 0);
                b.bom(600);
            }
            if (b.hp <= 0) hoiSinh();
            b.bom(3000);
            b.lamMoi();
            khaiThac("thach_dau_bot", Math.max(0, b.xu - xu0), false);
            ghi("thach dau " + bot.b.ten + ": xu " + xu0 + " -> " + b.xu);
            if (!KichBanDS.laLang(b.mapId)) toi(0);
        }

        /** Trao doi Pokemon 1-1 voi bot (nguoi dat truoc, bot dat lai, khoa + xac nhan). */
        void traoDoiBot(HlvAiNguoiChoi bot) throws Exception {
            if (bot.conDeTrao() == null || !gapBot(bot)) { khaiThac("trao_doi_bot.khong_gap", 0, false); return; }
            List<int[]> hop = HlvAiNguoiChoi.docHop(b);
            if (hop == null || hop.isEmpty()) { khaiThac("trao_doi_bot.khong_co_con", 0, false); return; }
            int m = b.moc();
            b.gui(-121, (byte) 6, (byte) 0, (short) bot.b.actor);
            boolean mo = b.cho(x -> HlvAiNguoiChoi.layGoi2(x, m, 2) != null, 15000);
            if (!mo) { khaiThac("trao_doi_bot.khong_mo", 0, b.doiChu(m, 0, "giới hạn") != null); return; }
            b.gui(-121, (byte) 6, (byte) 2, (byte) 1, hop.get(0)[0]);
            b.bom(5000);
            b.gui(-121, (byte) 6, (byte) 3);
            b.bom(4000);
            b.gui(-121, (byte) 6, (byte) 4);
            boolean xong = b.cho(x -> HlvAiNguoiChoi.layGoi2(x, m, 3) != null, 15000);
            if (!xong) b.gui(-121, (byte) 6, (byte) 5);
            khaiThac(xong ? "trao_doi_bot" : "trao_doi_bot.khong_xong", 0, false);
        }

        /** Nguoi that chu doi (kt109) moi bot cung lang vao doi; bot gop quy -> tinh han muc bot -> nguoi (gop_quy). */
        void moiDoi(HlvAiNguoiChoi bot) throws Exception {
            if (!chuDoi) { khaiThac("moi_doi.khong_phai_chu", 0, false); return; }
            if (!gapBot(bot)) { khaiThac("moi_doi.khong_gap", 0, false); return; }
            b.gui(-11, (byte) 0, (short) bot.b.actor);
            b.bom(1500);
            khaiThac("moi_doi", 0, false);
            // quy doi: bot (vua vao doi cua nguoi that) gop 8000 xu > han muc 5000/nguoi/ngay -> may chu phai chan (gop_quy)
            String truoc = BotChucNang.sql1("SELECT CONCAT(COUNT(*),'/',IFNULL(SUM(bi_chan),0),'/',IFNULL(SUM(IF(bi_chan=0,gia_tri,0)),0)) FROM tob_hlv_ai_chuyen WHERE kieu='gop_quy'");
            bot.yeuCauGopQuy = 8000;
            cho(12000);
            String sau = BotChucNang.sql1("SELECT CONCAT(COUNT(*),'/',IFNULL(SUM(bi_chan),0),'/',IFNULL(SUM(IF(bi_chan=0,gia_tri,0)),0)) FROM tob_hlv_ai_chuyen WHERE kieu='gop_quy'");
            String[] t0 = (truoc == null ? "0/0/0" : truoc.trim()).split("/"), t1 = (sau == null ? "0/0/0" : sau.trim()).split("/");
            long lan = Long.parseLong(t1[0]) - Long.parseLong(t0[0]);
            long chan = Long.parseLong(t1[1].split("\\.")[0]) - Long.parseLong(t0[1].split("\\.")[0]);
            long qua = Long.parseLong(t1[2].split("\\.")[0]) - Long.parseLong(t0[2].split("\\.")[0]);
            if (lan == 0) khaiThac("gop_quy_doi_nguoi.khong_ghi", 0, false);
            else khaiThac("gop_quy_doi_nguoi", Math.max(0, qua - 5000), chan > 0);
        }

        boolean chuDoi;

        /** PK canh lang: nguoi that + bot cung deo khan, ra ban do ngoai lang ke ben, nguoi danh bot; doi chieu xu / mon. */
        void pkBotCanh(HlvAiNguoiChoi bot) throws Exception {
            int lang = b.mapId;
            int dich = BotXuyenSuot.duong(lang, 1) != null ? 1 : BotXuyenSuot.duong(lang, 2) != null ? 2 : -1;
            if (dich < 0) { khaiThac("pk_canh.khong_co_bai", 0, false); return; }
            int khan = -1;
            for (int t = 14; t <= 18; t++) if (b.soThuoc(t) > 0) khan = t;
            if (khan < 0 && KichBanDS2.vaoNha(b, 103)) { khan = 15; b.gui(24, (byte) 1, (byte) 4, (short) khan, (short) 1); b.bom(1200); KichBanDS2.raNgoai(b); }
            bot.dungYenKhan = true;
            bot.dungYen = dich;
            b.lamMoi();
            long xu0 = b.xu;
            int mon0 = b.tongMon();
            int don = 0;
            if (khan >= 0 && toi(dich)) {
                b.gui(22, (byte) khan);
                b.bom(800);
                long cho = System.currentTimeMillis() + 90000;
                while (System.currentTimeMillis() < cho && !(bot.b.mapId == dich && bot.khanMinh >= 14 && bot.khanMinh != khan)) b.bom(2000);
                BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
                for (int lan = 0; lan < 40 && bot.b.mapId == b.mapId && b.hp > 0; lan++) {
                    int[] vt = b.nguoi.get(bot.b.actor);
                    if (vt != null && lan % 6 == 0 && bd != null) { int[] o = bd.oGan(vt[0] / 16, vt[1] / 16, 2); if (o != null) b.diDuong(bd, o[0], o[1]); }
                    b.gui(6, (short) bot.b.actor, (byte) 0);
                    don++;
                    b.bom(500);
                }
                b.bom(11000);
                b.gui(22, (byte) khan);
                b.bom(500);
            }
            if (b.hp <= 0) hoiSinh();
            b.lamMoi();
            khaiThac(don > 0 ? "pk_canh" : "pk_canh.khong_danh", Math.max(0, b.xu - xu0) + Math.max(0, b.tongMon() - mon0) * 1000L, false);
            bot.dungYenKhan = false;
            bot.dungYen = lang;
            if (b.mapId != lang) toi(lang);
        }

        void dauTruong() throws Exception {
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

    // ================================================================== khai thac tap trung (-Dhlv.khaiThac=1)
    /**
     * Thu khai thac CO Y, chac chan gap duoc bot: 3 bot (cap 45-55 qua GM) DUNG YEN trong lang canh nguoi that (bot van
     * tu tra loi giao dich / thach dau / trao doi Pokemon / loi moi vao doi trong luong doc goi nhu luc choi). Moi nguoi that
     * lap: giao dich thuoc, thach dau cuoc 5.000, trao doi Pokemon, PK (ca hai deo khan, ra bai canh lang), chu doi moi bot vao
     * doi (bot gop quy). Han muc bot -> moi nguoi dat thap (5.000) tu dau -> moi lan sau lan dau phai bi chan.
     */
    /** Cho + gui nhip; rot ket noi thi vao lai (khong de mot loi mang dung ca phan khai thac). */
    static void choAnToan(NguoiThat n, long ms) {
        try {
            if (n.b == null || n.b.dut) n.vao();
            else n.cho(ms);
        } catch (Exception e) {
            try { if (n.b != null) n.b.close(); Thread.sleep(3000); n.vao(); } catch (Exception x) { System.out.println("[KHAI THAC] vao lai " + n.user + ": " + x); }
        }
    }

    static void khaiThacTapTrung() throws Exception {
        ghi("# Khai thác tập trung - " + new Date());
        Thread.sleep(60000);
        BotChucNang.sql("INSERT INTO cau_hinh (khoa,gia_tri,sua_boi,sua_luc) VALUES ('hlv_ai.chuyen_moi_nguoi_ngay','5000','khai-thac',NOW())"
                + " ON DUPLICATE KEY UPDATE gia_tri='5000'");
        BotChucNang.sql("UPDATE cau_hinh_phien_ban SET phien_ban = phien_ban + 1");
        GHI_CHU.add("SQL: hlv_ai.chuyen_moi_nguoi_ngay = 5000 (moi lan chuyen bot -> nguoi sau lan dau phai bi chan)");
        List<NguoiThat> nt = new ArrayList<>();
        for (int i = 116; i <= 118; i++) {
            NguoiThat n = new NguoiThat(String.format("kt%03d", i));
            n.chuanBi();
            nt.add(n);
        }
        // bot dung yen canh nguoi that
        List<HlvAiNguoiChoi> bots = new ArrayList<>();
        long het = System.currentTimeMillis() + 5 * 60000L;
        while (bots.size() < 3 && System.currentTimeMillis() < het) {
            // uu tien bot da co Pokemon trong Hop PC (de thu trao doi Pokemon voi bot); het 3 phut dau thi lay bat ky
            boolean canHop = System.currentTimeMillis() < het - 2 * 60000L;
            for (DichVuHlvAi.Ho h : DichVuHlvAi.HO) {
                HlvAiNguoiChoi p = h.dang;
                if (canHop && (p == null || p.conDeTrao() == null)) continue;
                if (p != null && p.b != null && !p.b.dut && !bots.contains(p) && bots.size() < 3) {
                    gm("slv " + p.b.ten + " " + (45 + DichVuHlvAi.RD.nextInt(10)) + " 0");
                    p.dungYen = nt.get(bots.size()).b.mapId; // moi bot dung canh mot nguoi that
                    bots.add(p);
                }
            }
            for (NguoiThat n : nt) choAnToan(n, 5000);
        }
        ghi("\nBot đứng yên: " + bots.size() + " ở bản đồ " + nt.get(0).b.mapId + "; người thật: kt116..kt118 (kt116 chủ đội).\n");
        for (int k = 0; k < 18; k++) for (NguoiThat n : nt) choAnToan(n, 1700); // bot ve lang (nguoi that van gui nhip)
        long ketThuc = System.currentTimeMillis() + Long.getLong("hlv.khaiThacPhut", 25L) * 60000L;
        int vong = 0;
        while (System.currentTimeMillis() < ketThuc) {
            for (int i = 0; i < nt.size(); i++) {
                NguoiThat n = nt.get(i);
                if (n.b == null || n.b.dut) { try { n.vao(); } catch (Exception e) { System.out.println("[KHAI THAC] vao lai " + n.user + ": " + e); } continue; }
                for (HlvAiNguoiChoi bot : bots.subList(Math.min(i, bots.size()), bots.size())) {
                    if (bot.b == null || bot.b.dut || bot.b.mapId != n.b.mapId) { bot.dungYen = n.b.mapId; choAnToan(n, 3000); continue; }
                    try {
                        switch (vong % 5) {
                            case 0: n.gdLap(bot, true); break;
                            case 1: n.thachDauBot(bot); break;
                            case 2: n.traoDoiBot(bot); break;
                            case 3: n.moiDoi(bot); break;
                            default: n.pkBotCanh(bot); break;
                        }
                    } catch (Exception e) {
                        khaiThac("loi", 0, false);
                        System.out.println("[KHAI THAC] " + n.user + ": " + e);
                    }
                    choAnToan(n, 5000);
                }
            }
            vong++;
            StringBuilder kt = new StringBuilder("vòng " + vong + ":");
            for (Map.Entry<String, long[]> e : new TreeMap<>(KHAI_THAC).entrySet()) kt.append(' ').append(e.getKey()).append('=').append(e.getValue()[0]).append('/').append(e.getValue()[1]).append('/').append(e.getValue()[2]);
            System.out.println("[KHAI THAC] " + kt);
        }
        for (HlvAiNguoiChoi bot : bots) bot.dungYen = -1;
        ghi("\n| Kiểu | Hướng | Lần | Bị chặn | Giá trị |\n|---|---|---|---|---|");
        for (String d : BotChucNang.sql("SELECT kieu, IF(huong=1,'bot→người','người→bot'), COUNT(*), SUM(bi_chan), SUM(gia_tri) FROM tob_hlv_ai_chuyen"
                + " WHERE nguoi_ten IN ('kt116','kt117','kt118') OR nguoi_id >= 1000000000 GROUP BY kieu, huong")) {
            ghi("| " + d.replace("\t", " | ") + " |");
        }
        ketThuc();
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
        for (int i = 109; i <= 112; i++) { // tai khoan moi tinh moi lan chay
            NguoiThat n = new NguoiThat(String.format("kt%03d", i));
            nt.add(n);
            Thread t = new Thread(() -> {
                try { n.chuanBi(); } catch (Exception e) { System.out.println("[SOAK] chuan bi " + n.user + ": " + e); }
                n.run2();
            }, "soak-" + n.user);
            t.setDaemon(true);
            t.start();
            Thread.sleep(5000);
        }
        int len = 0;
        for (DichVuHlvAi.Ho h : DichVuHlvAi.HO) {
            HlvAiNguoiChoi p = h.dang;
            if (p != null && p.b != null && !p.b.dut && len < DichVuHlvAi.HO.size() / 2 && p.b.lv < 40) {
                gm("slv " + p.b.ten + " " + (40 + DichVuHlvAi.RD.nextInt(16)) + " 0");
                len++;
            }
        }
        ghi("\nChuẩn bị: 4 người thật kt109..kt112 (cấp 45-50, 300.000 xu, 1 Pokémon trong Hộp PC; kt109 lập đội bằng 100 triệu xu GM), " + len + " bot đang online lên cấp 40..55 (GM, giả lập bot chơi lâu: thách đấu, lập đội, phó bản nhóm).\n");
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
