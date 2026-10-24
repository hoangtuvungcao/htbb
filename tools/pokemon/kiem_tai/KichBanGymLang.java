import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Kich ban bot "DANH KHONG GAY SAT THUONG" (bao loi 24/10/2026, nguoi choi J2ME cap 27 o Pewter): nhan vat THUONG (khong
 * GM) cap 27 danh bang duong J2ME (lenh 9 / 106, khong ngam) va - khi chay -Dban=915 - duong PC ngam tay (-113 ban).
 *   gl_quai      Pokemon hoang da o dong Pallet (map 1): lenh 9 lam giam HP; -Dban>=908: phat ban -113 trung quai
 *   gl_gym_lang  Thach dau Gym Pewter (So tay -> Thách đấu Gym) KHI DANG DUNG O PEWTER (lang, mapIDLoadMap 0): boss hien
 *                canh minh; lenh 9 va lenh 106 deu lam giam HP boss (truoc ban sua: HP dung yen - MapDanhNhau bo qua moi
 *                don danh quai trong lang)
 *   gl_gym_ngoai Thach dau Gym o dong Pallet (ngoai thi tran): danh giam HP boss
 *   gl_pvp_lang  Hai nguoi choi thuong o Pewter: lenh 6 khong lam ai mat HP (thi tran van an toan)
 *   gl_mot_tran  Bao loi 24/10/2026 "ha Gym xong quai van xuat hien lai, 2 Truong Gym cung luc": bam Thach dau Gym 2 lan
 *                lien + lan 3 (So tay) -> nguoi choi va nguoi dung canh chi thay MOT boss; ha boss -> ca hai nhan goi 90,
 *                9 giay sau (qua timeReBorn 6 giay) boss KHONG hien lai; danh lai roi ra Pallet -> boss bien mat voi
 *                nguoi o lai; danh lai roi thoat game -> boss bien mat. Chay server -Dhtbb.thu.nhanBoss=1000
 *                (sv.cho_phep_thu_nghiem=1) de ha nhanh.
 *
 *   javac -d /tmp/bot tools/pokemon/kiem_tai/*.java
 *   java -cp /tmp/bot -Ddb=htbb_db -Dtu=1 -Dden=5 -DtuThuong=101 -DdenThuong=110 KichBanGymLang 127.0.0.1 19129 kg
 *   (them -Dban=915 de chay duong client PC / APK)
 */
public final class KichBanGymLang {

    private KichBanGymLang() {
    }

    static final int CAP = 27;
    /** gl_mot_tran: cap cao de ha boss Gym (lang khong qua nhanBoss) trong vai phut. */
    static final int CAP_MOT_TRAN = Integer.getInteger("capMotTran", 90);

    /** Nhan vat THUONG cap {@code lv} (GM slv cong du diem tiem nang / ky nang tu 0.9.13), da cong diem vao chieu, o lang. */
    static BotChucNang.Bot botThuongCap(BotChucNang.Bot gm, int lv) throws Exception {
        BotChucNang.Bot b = BotChucNang.botThuong();
        KichBanDS.veLang(b);
        gm.gm("slv " + b.ten + " " + lv + " 0", 1200);
        b.bom(800);
        b.gui(34, (byte) 0, (short) Math.max(1, 5 * (lv - 1)));
        b.bom(400);
        for (int o = 0; o < 7; o++) for (int k = 0; k < 9; k++) { b.gui(36, (byte) o); b.bom(40); }
        b.lamMoi();
        b.bom(800);
        return b;
    }

    static int hp(BotChucNang.Bot b, int id) throws IOException {
        b.gui(7, (short) id);
        b.bom(350);
        int[] q = b.quai.get(id);
        return q == null ? -1 : q[2];
    }

    static int chieuCo(BotChucNang.Bot b, int lan) {
        List<Integer> co = new ArrayList<>();
        for (int k = 0; k < Math.min(4, b.chieu.length); k++) if (b.chieu[k] > 0) co.add(k);
        return co.isEmpty() ? 0 : co.get(lan % co.size());
    }

    /** Danh {@code lan} don lenh 9 (J2ME / khoa muc tieu). @return {hp truoc, hp sau}. */
    static int[] danh9(BotChucNang.Bot b, int id, int lan) throws IOException {
        int truoc = hp(b, id);
        for (int i = 0; i < lan; i++) {
            b.gui(9, (short) id, (byte) chieuCo(b, i));
            b.bom(700);
        }
        return new int[]{truoc, hp(b, id)};
    }

    /** Danh {@code lan} don lenh 106 (chieu dien rong: chieu, so quai, id...). */
    static int[] danh106(BotChucNang.Bot b, int id, int lan) throws IOException {
        int truoc = hp(b, id);
        for (int i = 0; i < lan; i++) {
            b.gui(106, (byte) chieuCo(b, i), (byte) 1, (short) id);
            b.bom(700);
        }
        return new int[]{truoc, hp(b, id)};
    }

    /** Ban ngam tay -113 (client &gt;= 908) thang vao quai. */
    static int[] ban113(BotChucNang.Bot b, int id, int lan) throws IOException {
        int truoc = hp(b, id);
        b.gui(-113, (byte) 1, (byte) 1);
        for (int i = 0; i < lan; i++) {
            int[] q = b.quai.get(id);
            if (q == null) break;
            short[] h = KichBanNgamTay.huong(new int[]{b.px, b.py}, new int[]{q[0], q[1]}, false);
            b.gui(-113, (byte) 0, (byte) chieuCo(b, i), (short) b.px, (short) b.py, h[0], h[1]);
            b.bom(900);
        }
        return new int[]{truoc, hp(b, id)};
    }

    static String bang(String duong, int[] h, BotChucNang.Bot b) {
        return duong + " hp " + h[0] + " -> " + h[1] + " (map " + b.mapId + ", cap " + b.lv + ", chieu " + java.util.Arrays.toString(b.chieu) + ")";
    }

    /** Ra dong Pallet (map 1) qua cong bac cua lang (khong GM). */
    static boolean raPallet(BotChucNang.Bot b) throws IOException {
        for (int t = 0; t < 3 && b.mapLoad != 1; t++) {
            b.bom(800);
            if (!KichBanHuongDan.quaCong(b, 0) && KichBanDS.laLang(b.mapId)) KichBanDS2.raNgoai(b);
            if (b.mapLoad != 1 && !KichBanDS.laLang(b.mapId)) KichBanHuongDan.quaCong(b, 0);
        }
        return b.mapLoad == 1;
    }

    /** So tay -> Thách đấu Gym -> Gym dau (Pewter, cap 12). @return id boss hoac null. */
    static Integer thachDauGym(BotChucNang.Bot b) throws IOException {
        KichBanDS.timQuai(b, 0, 300);
        Set<Integer> cu = new HashSet<>(b.quai.keySet());
        int m0 = KichBanDS4.chonSoTay(b, 7);
        b.doiMenu(m0, -527, 5, 2500);
        int m = b.moc();
        b.menu(-527, 5, 0);
        String t = b.doiChu(m, 2500, "");
        System.out.println("  thach dau: " + t);
        return KichBanDS.quaiMoi(b, cu, 8000);
    }

    static void quai() throws Exception {
        try (BotChucNang.Bot g = BotChucNang.botMoi(0); BotChucNang.Bot b = botThuongCap(g, CAP)) {
            if (!raPallet(b)) { BotChucNang.ket("gl.quai", false, "khong ra duoc Pallet, map " + b.mapId); return; }
            b.bom(2500);
            List<Integer> song = KichBanDS.timQuai(b, 0, 300);
            Integer gan = null;
            long d = Long.MAX_VALUE;
            for (int id : song) {
                int[] q = b.quai.get(id);
                long dd = (long) (q[0] - b.px) * (q[0] - b.px) + (long) (q[1] - b.py) * (q[1] - b.py);
                if (q[4] < 20000 && dd < d) { d = dd; gan = id; }
            }
            if (gan == null) { BotChucNang.ket("gl.quai", null, "khong co Pokemon hoang da o Pallet"); return; }
            // GM keo quai lai gan: GM toi canh quai, roi nguoi choi di bo toi GM khong duoc -> dung o quai bang cach doi
            // quai di toi (quai tu dong di quanh); don gian: nguoi choi di bo toi o quai
            int[] q = b.quai.get(gan);
            BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
            KichBanHuongDan.dongBoViTri(b);
            int[] o = bd == null ? null : bd.oGan(q[0] / 16, q[1] / 16, 2);
            if (o != null) b.diDuong(bd, o[0], o[1]);
            b.bom(600);
            if (BotChucNang.BAN >= 908) { // PC / APK: phat ban ngam tay truoc (quai yeu, lenh 9 co the ha luon)
                int[] h2 = ban113(b, gan, 2);
                BotChucNang.ket("gl.quai.ngam113", h2[0] > 0 && h2[1] < h2[0], bang("-113 ban", h2, b));
                b.bom(1500);
            }
            int[] q2 = b.quai.get(gan);
            if (q2 == null || q2[2] <= 0) { // con truoc da guc: chon con khac gan nhat
                gan = null;
                d = Long.MAX_VALUE;
                for (int id : KichBanDS.timQuai(b, 0, 300)) {
                    int[] qq = b.quai.get(id);
                    long dd = (long) (qq[0] - b.px) * (qq[0] - b.px) + (long) (qq[1] - b.py) * (qq[1] - b.py);
                    if (qq[4] < 20000 && dd < d) { d = dd; gan = id; }
                }
                if (gan == null) { BotChucNang.ket("gl.quai.lenh9", null, "het quai"); return; }
                int[] qq = b.quai.get(gan);
                KichBanHuongDan.dongBoViTri(b);
                int[] o2 = bd == null ? null : bd.oGan(qq[0] / 16, qq[1] / 16, 2);
                if (o2 != null) b.diDuong(bd, o2[0], o2[1]);
                b.bom(600);
            }
            int[] h = danh9(b, gan, 6);
            BotChucNang.ket("gl.quai.lenh9", h[0] > 0 && h[1] >= 0 && h[1] < h[0], bang("lenh 9 (J2ME)", h, b));
        }
    }

    static void gymLang() throws Exception {
        try (BotChucNang.Bot g = BotChucNang.botMoi(0); BotChucNang.Bot b = botThuongCap(g, CAP)) {
            KichBanDS.veLang(b);
            if (!KichBanDS.laLang(b.mapId)) { BotChucNang.ket("gl.gym_lang", false, "khong o lang, map " + b.mapId); return; }
            Integer boss = thachDauGym(b);
            if (boss == null) { BotChucNang.ket("gl.gym_lang", false, "khong thay boss Gym: " + b.chuTu(0)); return; }
            int[] q = b.quai.get(boss);
            System.out.println("  boss " + boss + " o " + q[0] + "," + q[1] + " hp " + q[2] + "/" + q[4] + " minh " + b.px + "," + b.py + " map " + b.mapId);
            int[] h = danh9(b, boss, 6);
            BotChucNang.ket("gl.gym_lang.lenh9", h[0] > 0 && h[1] < h[0], bang("lenh 9 (J2ME)", h, b));
            int[] h2 = danh106(b, boss, 4);
            BotChucNang.ket("gl.gym_lang.lenh106", h2[0] > 0 && h2[1] < h2[0], bang("lenh 106", h2, b));
            if (BotChucNang.BAN >= 908) {
                int[] h3 = ban113(b, boss, 4);
                BotChucNang.ket("gl.gym_lang.ngam113", h3[0] > 0 && h3[1] < h3[0], bang("-113 ban", h3, b));
            }
        }
    }

    static void gymNgoai() throws Exception {
        try (BotChucNang.Bot g = BotChucNang.botMoi(0); BotChucNang.Bot b = botThuongCap(g, CAP)) {
            if (!raPallet(b)) { BotChucNang.ket("gl.gym_ngoai", false, "khong ra duoc Pallet, map " + b.mapId); return; }
            Integer boss = thachDauGym(b);
            if (boss == null) { BotChucNang.ket("gl.gym_ngoai", false, "khong thay boss Gym: " + b.chuTu(0)); return; }
            int[] h = danh9(b, boss, 6);
            BotChucNang.ket("gl.gym_ngoai.lenh9", h[0] > 0 && h[1] < h[0], bang("lenh 9", h, b));
        }
    }

    static void pvpLang() throws Exception {
        BotChucNang.Bot c = null;
        try (BotChucNang.Bot g = BotChucNang.botMoi(0); BotChucNang.Bot a = botThuongCap(g, CAP)) {
            c = botThuongCap(g, CAP);
            KichBanDS.veLang(a);
            KichBanDS.veLang(c);
            for (int t = 0; t < 4 && a.mapId != c.mapId; t++) { // lang co ban sao (Pewter 0 / 301): vao lai cho cung ban
                String u = c.user;
                c.close();
                Thread.sleep(2500);
                c = new BotChucNang.Bot(u);
                if (c.dangNhap("123456") == null) c.vaoGame(0);
                c.bom(800);
            }
            if (a.mapId != c.mapId) { BotChucNang.ket("gl.pvp_lang", null, "hai bot khac lang " + a.mapId + " / " + c.mapId); return; }
            a.bom(800);
            c.bom(800);
            int hc = c.hp;
            int ma = a.moc();
            for (int i = 0; i < 4; i++) { a.gui(6, (short) c.actor, (byte) chieuCo(a, i)); a.bom(600); }
            c.bom(800);
            boolean bi = false;
            for (int i = ma; i < a.su.size(); i++) if (a.su.get(i).cmd == 6 && a.su.get(i).d != null && a.su.get(i).d.length >= 13) bi = true;
            BotChucNang.ket("gl.pvp_lang.khong_mat_hp", !bi && c.hp >= hc, "c hp " + hc + " -> " + c.hp + ", goi 6 sat thuong=" + bi + " | " + a.chuTu(ma));
        } finally {
            if (c != null) c.close();
        }
    }


    /** Goi 90 (actorDie) cho quai {@code id} tu moc {@code tu}. */
    static boolean co90(BotChucNang.Bot b, int tu, int id) {
        for (int i = tu; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd == 90 && e.d != null && e.d.length >= 2 && (short) (((e.d[0] & 255) << 8) | (e.d[1] & 255)) == (short) id) return true;
        }
        return false;
    }

    /** Boss (maxhp &gt; 2000, con song) MOI (khong trong {@code cu}) ma bot thay trong {@code ms}. */
    static List<Integer> bossMoi(BotChucNang.Bot b, Set<Integer> cu, long ms) throws IOException {
        long het = System.currentTimeMillis() + ms;
        Set<Integer> daHoi = new HashSet<>();
        while (System.currentTimeMillis() < het) {
            b.bom(400);
            for (int id : new ArrayList<>(b.quai.keySet())) {
                if (cu.contains(id) || daHoi.contains(id)) continue;
                daHoi.add(id);
                b.gui(7, (short) id);
            }
        }
        b.bom(500);
        List<Integer> kq = new ArrayList<>();
        for (int id : daHoi) {
            int[] q = b.quai.get(id);
            if (q != null && q[4] > 2000 && q[2] > 0) kq.add(id);
        }
        return kq;
    }

    /** Dua {@code c} vao cung ban sao lang voi {@code a} (Pewter 0 / 301): dang nhap lai toi da 4 lan. */
    static BotChucNang.Bot cungLang(BotChucNang.Bot a, BotChucNang.Bot c) throws Exception {
        KichBanDS.veLang(a);
        KichBanDS.veLang(c);
        for (int t = 0; t < 6 && a.mapId != c.mapId; t++) {
            String u = c.user;
            c.close();
            Thread.sleep(2500);
            c = new BotChucNang.Bot(u);
            if (c.dangNhap("123456") == null) c.vaoGame(0);
            c.bom(800);
            KichBanDS.veLang(c);
        }
        return c;
    }

    /** Ha boss {@code id} bang lenh 9 (server thu nhanBoss): @return hp cuoi. */
    static int haBoss(BotChucNang.Bot b, int id) throws IOException {
        int h = hp(b, id);
        for (int i = 0; i < 400 && h > 0; i++) {
            if (i % 25 == 0) {
                int[] q0 = b.quai.get(id);
                System.out.println("  ha boss " + id + " lan " + i + ": hp " + h + (q0 == null ? "" : " o " + q0[0] + "," + q0[1]) + " minh " + b.px + "," + b.py + " hp minh " + b.hp);
            }
            int[] q = b.quai.get(id);
            if (q != null && Math.abs(q[0] - b.px) + Math.abs(q[1] - b.py) > 64) {
                BotChucNang.BanDo bd = BotChucNang.BanDo.nap(b.mapLoad);
                KichBanHuongDan.dongBoViTri(b);
                int[] o = bd == null ? null : bd.oGan(q[0] / 16, q[1] / 16, 1);
                if (o != null) b.diDuong(bd, o[0], o[1]);
            }
            b.gui(9, (short) id, (byte) chieuCo(b, i));
            b.bom(350);
            h = hp(b, id);
        }
        return h;
    }

    /** Mo So tay -> Thach dau Gym roi chon Gym dau {@code lan} lan LIEN (khong cho tra loi - bam dup). */
    static int thachDauLien(BotChucNang.Bot b, int lan) throws IOException {
        int m0 = KichBanDS4.chonSoTay(b, 7);
        b.doiMenu(m0, -527, 5, 2500);
        int m = b.moc();
        for (int i = 0; i < lan; i++) b.menu(-527, 5, 0);
        return m;
    }

    static void motTran() throws Exception {
        BotChucNang.Bot c = null;
        try (BotChucNang.Bot g = BotChucNang.botMoi(0); BotChucNang.Bot b = botThuongCap(g, CAP_MOT_TRAN)) {
            c = botThuongCap(g, CAP);
            c = cungLang(b, c);
            if (b.mapId != c.mapId) { BotChucNang.ket("gl.mot_tran", null, "hai bot khac lang " + b.mapId + " / " + c.mapId); return; }
            KichBanDS.timQuai(b, 0, 300);
            KichBanDS.timQuai(c, 0, 300);
            b.bom(1500);
            c.bom(500);
            Set<Integer> cu = new HashSet<>(b.quai.keySet());
            cu.addAll(c.quai.keySet());
            // ---- 1. bam Thach dau 2 lan lien + lan 3 sau 1,5 giay
            int m = thachDauLien(b, 2);
            b.bom(1500);
            int m3 = b.moc();
            KichBanDS4.chonSoTay(b, 7);
            b.doiMenu(m3, -527, 5, 2500);
            b.menu(-527, 5, 0);
            String lai = b.doiChu(m3, 3000, "dang thach dau");
            List<Integer> bb = bossMoi(b, cu, 5000);
            List<Integer> bc = bossMoi(c, cu, 2000);
            System.out.println("  thach dau: " + b.chuTu(m));
            BotChucNang.ket("gl.mot_tran.mot_boss_chu", bb.size() == 1, "chu thay " + bb.size() + " boss moi " + bb);
            BotChucNang.ket("gl.mot_tran.mot_boss_nguoi_canh", bc.size() == 1, "nguoi canh thay " + bc.size() + " boss moi " + bc);
            BotChucNang.ket("gl.mot_tran.bam_lai_chi_ve", lai != null, String.valueOf(lai));
            if (bb.isEmpty()) return;
            int id = bb.get(0);
            // ---- 2. ha boss: ca hai thay boss bien mat, 9 giay sau khong hien lai
            int mb = b.moc(), mc = c.moc();
            int h = haBoss(b, id);
            BotChucNang.ket("gl.mot_tran.ha_boss", h <= 0, "hp cuoi " + h + " | " + b.chuTu(mb));
            boolean c90 = c.cho(x -> co90(x, mc, id), 6000);
            BotChucNang.ket("gl.mot_tran.thang_nguoi_canh_thay_mat", c90, "goi 90 boss " + id + " toi nguoi canh");
            String thang = b.doiChu(mb, 3000, "Chiến thắng", "Đánh lại Gym thành công");
            BotChucNang.ket("gl.mot_tran.thang_thong_bao", thang != null && thang.contains("ở đâu cũng được"), String.valueOf(thang));
            b.bom(9000);
            c.bom(500);
            b.quai.remove(id);
            c.quai.remove(id);
            b.gui(7, (short) id);
            c.gui(7, (short) id);
            b.bom(1000);
            c.bom(500);
            int[] qb = b.quai.get(id), qc = c.quai.get(id);
            Set<Integer> cu2 = new HashSet<>(cu);
            List<Integer> moiSau = bossMoi(c, cu2, 1500);
            BotChucNang.ket("gl.mot_tran.khong_hoi_sinh", (qb == null || qb[2] <= 0) && (qc == null || qc[2] <= 0) && moiSau.isEmpty(),
                    "9 giay sau: chu " + (qb == null ? "khong co" : "hp " + qb[2]) + ", nguoi canh " + (qc == null ? "khong co" : "hp " + qc[2])
                            + ", boss song moi " + moiSau);
            // ---- 3. danh lai roi ra Pallet: boss bien mat voi nguoi o lai
            Set<Integer> cu3 = new HashSet<>(c.quai.keySet());
            cu3.addAll(b.quai.keySet());
            thachDauLien(b, 1);
            List<Integer> b3 = bossMoi(b, cu3, 4000);
            if (b3.size() == 1) {
                int id3 = b3.get(0);
                int mc3 = c.moc();
                boolean ra = raPallet(b);
                boolean mat = c.cho(x -> co90(x, mc3, id3), 8000);
                BotChucNang.ket("gl.mot_tran.roi_ban_do", ra && mat, "ra Pallet " + ra + " (map " + b.mapId + "), nguoi o lai thay boss " + id3 + " mat " + mat
                        + " | " + b.doiChu(0, 0, "rời khỏi bản đồ"));
            } else {
                BotChucNang.ket("gl.mot_tran.roi_ban_do", false, "danh lai: " + b3.size() + " boss | " + b.chuTu(0));
            }
            // ---- 4. danh lai roi thoat game: boss bien mat
            String u = b.user;
            BotChucNang.Bot b4 = new BotChucNang.Bot(u);
            b.close();
            Thread.sleep(2500);
            if (b4.dangNhap("123456") == null) b4.vaoGame(0);
            b4.bom(800);
            for (int t = 0; t < 3 && !KichBanDS.laLang(b4.mapId); t++) {
                KichBanDS.veLang(b4);
                b4.bom(2500);
            }
            b4 = cungLang(c, b4);
            if (b4.mapId == c.mapId) {
                KichBanDS.timQuai(b4, 0, 300);
                Set<Integer> cu4 = new HashSet<>(c.quai.keySet());
                cu4.addAll(b4.quai.keySet());
                thachDauLien(b4, 1);
                List<Integer> b5 = bossMoi(c, cu4, 4000);
                if (b5.size() == 1) {
                    int id5 = b5.get(0);
                    int mc5 = c.moc();
                    b4.close();
                    boolean mat = c.cho(x -> co90(x, mc5, id5), 8000);
                    BotChucNang.ket("gl.mot_tran.thoat_game", mat, "thoat game: nguoi o lai thay boss " + id5 + " mat " + mat);
                } else {
                    BotChucNang.ket("gl.mot_tran.thoat_game", false, "danh lai lan 3: nguoi canh thay " + b5.size() + " boss");
                    b4.close();
                }
            } else {
                BotChucNang.ket("gl.mot_tran.thoat_game", null, "khong cung lang " + b4.mapId + " / " + c.mapId);
                b4.close();
            }
        } finally {
            if (c != null) c.close();
        }
    }

    public static void main(String[] a) throws Exception {
        BotChucNang.dk("gl_quai", true, KichBanGymLang::quai);
        BotChucNang.dk("gl_gym_lang", true, KichBanGymLang::gymLang);
        BotChucNang.dk("gl_gym_ngoai", true, KichBanGymLang::gymNgoai);
        BotChucNang.dk("gl_pvp_lang", true, KichBanGymLang::pvpLang);
        BotChucNang.dk("gl_mot_tran", true, KichBanGymLang::motTran);
        List<String> chon = new ArrayList<>();
        for (int i = 3; i < a.length; i++) chon.add(a[i]);
        if (chon.isEmpty()) chon.addAll(List.of("gl_gym_lang", "gl_gym_ngoai", "gl_quai", "gl_pvp_lang"));
        String[] b = new String[3 + chon.size()];
        b[0] = a[0];
        b[1] = a[1];
        b[2] = a[2];
        for (int i = 0; i < chon.size(); i++) b[3 + i] = chon.get(i);
        BotChucNang.main(b);
    }
}
