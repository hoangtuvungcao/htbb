import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Kich ban bot PHO BAN HAM NGUC + CUP PVP TUAN (0.9.7: real.PhoBanGame / real.CupGame, goi -112).
 * Chay tren MAY CHU THU RIENG (sv.cho_phep_thu_nghiem=1 cho GM "tua") da nap sql/htbb.sql; tai khoan
 * kt001.. co san (account.team_user + 5h_active), ten trong sv.adminAccounts (GM: slv / tua / khu):
 *   pb_vao      len cap 30, So tay -> Ham nguc -> Pho ban -> Vao: ban do 4381, goi -112 loai 1, thong bao tang 1, co quai,
 *               tob_pho_ban_luot = 1, danh quai: HP quai giam
 *   pb_khu      lenh "khu" trong phong -> menu "Rời phó bản" (khong danh sach khu)
 *   pb_ket_noi  ngat ket noi roi dang nhap lai: ve DUNG ban do 4381 cung khu, thong bao "Đã về lại phó bản"
 *   pb_roi      menu Roi -> ra khoi 4381, the -112 an (loai 0)
 *   pb_luot     vao them 2 lan (het 3 luot) -> lan thu 4 bao "hết lượt"
 *   pb_hai_phong bot thu hai vao pho ban rieng: khac khu, khong thay quai cua phong kia
 *   cup_vong    4 bot cap 25 dang ky Cup (lich CSDL dat 'nhi'), tua dong ho toi Chu nhat 20:00: ghep 2 tran, vao san
 *               4382, the -112 loai 2; danh truoc 5 giay chuan bi = khong mat mau; danh sau do: mat mau, HP khong ve
 *               0; tua het gio -> hoa (tob_cup_tran ket_qua 3), ve cho cu; tua het 5 vong -> chot hang + tob_cup_thuong,
 *               phat mot lan (da_nhan), dang nhap lai khong phat lan 2
 *
 *   javac -encoding UTF-8 -d /tmp/bot tools/pokemon/kiem_tai/*.java
 *   java -cp /tmp/bot -Dban=908 -Ddb=htbb_db -Ddbport=3396 -Dtu=1 -Dden=12 KichBanPhoBanCup 127.0.0.1 27829 kt [pb_... cup_vong]
 */
public final class KichBanPhoBanCup {

    private KichBanPhoBanCup() {
    }

    static final int SO_TAY = -527, MENU_GOC = 0, MENU_HAM_NGUC = 3, MENU_PB = 60, MENU_PB_TRONG = 61, MENU_CUP = 70;
    static final int MAP_PB = 4381, MAP_CUP = 4382;

    /** {loai, tieuDe, dong1, dong2, giay, phanTram} cua goi -112 cuoi tu moc. */
    static Object[] the(BotChucNang.Bot b, int tu) {
        Object[] kq = null;
        for (int i = tu; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd != -112 || e.d == null) continue;
            try {
                DataInputStream r = new DataInputStream(new ByteArrayInputStream(e.d));
                kq = new Object[]{(int) r.readByte(), r.readUTF(), r.readUTF(), r.readUTF(), (int) r.readShort(), (int) r.readByte()};
            } catch (Exception ignored) {
            }
        }
        return kq;
    }

    static String toast(BotChucNang.Bot b, int tu) {
        StringBuilder s = new StringBuilder();
        for (int i = tu; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd == -104 && e.d != null) {
                try {
                    DataInputStream r = new DataInputStream(new ByteArrayInputStream(e.d));
                    r.readByte();
                    s.append(r.readUTF()).append(" | ");
                } catch (Exception ignored) {
                }
            } else if (e.chu != null && (e.cmd == 37 || e.cmd == -26 || e.cmd == 38)) {
                s.append(e.chu).append(" | ");
            }
        }
        return s.toString();
    }

    static String menuChu(BotChucNang.Bot b, int tu) {
        StringBuilder s = new StringBuilder();
        for (int i = tu; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd == -30 && e.chu != null) s.append(e.chu).append(" || ");
        }
        return s.toString();
    }

    static BotChucNang.Bot bot;

    static BotChucNang.Bot a() throws Exception {
        if (bot == null) {
            bot = BotChucNang.botMoi(0);
            bot.gm("slv " + bot.ten + " 30 0", 2500);
            bot.den(2, 20, 20);
            bot.bom(1500);
        }
        return bot;
    }

    static String charDb(BotChucNang.Bot b) {
        return String.valueOf(b.charDb);
    }

    static int luot(BotChucNang.Bot b) {
        String s = BotChucNang.sql1("SELECT IFNULL(SUM(luot),0) FROM tob_pho_ban_luot WHERE char_id = " + b.charDb);
        return s == null ? -1 : Integer.parseInt(s.trim());
    }

    /** So tay -> Ham nguc -> Pho ban -> Vao. */
    static void moVao(BotChucNang.Bot b) throws Exception {
        b.menu(SO_TAY, MENU_GOC, 2);
        b.bom(800);
        b.menu(SO_TAY, MENU_HAM_NGUC, 2);
        b.bom(800);
        b.menu(SO_TAY, MENU_PB, 0);
        b.cho(x -> x.mapId == MAP_PB, 4000);
        b.bom(1500);
    }

    static void vao() throws Exception {
        BotChucNang.Bot b = a();
        int m = b.moc();
        moVao(b);
        Object[] t = the(b, m);
        BotChucNang.ket("pb.vao.ban_do", b.mapId == MAP_PB && b.tenMap.startsWith("Phó bản Hầm ngục"), "map " + b.mapId + " " + b.tenMap + " | " + toast(b, m));
        BotChucNang.ket("pb.vao.the_112", t != null && (Integer) t[0] == 1 && ((String) t[1]).startsWith("Phó bản: ") && ((String) t[2]).contains("Tầng 1/5"),
                t == null ? "khong co -112" : java.util.Arrays.toString(t));
        BotChucNang.ket("pb.vao.thong_bao_tang", toast(b, m).contains("Tầng 1/5"), toast(b, m));
        BotChucNang.ket("pb.vao.luot", luot(b) == 1, "luot=" + luot(b));
        b.bom(2500); // quai cua phong den qua goi vi tri (cmd 4) cua khu
        int soQuai = 0;
        Integer muc = null;
        for (java.util.Map.Entry<Integer, int[]> e : b.quai.entrySet()) {
            soQuai++;
            if (muc == null) muc = e.getKey();
        }
        BotChucNang.ket("pb.vao.co_quai", soQuai >= 1 ? Boolean.TRUE : null, "thay " + soQuai + " quai (bot chi thay quai da di chuyen gan)");
        if (muc != null) {
            int[] q = b.quai.get(muc);
            b.gm("mto " + MAP_PB + " " + (q[0] / 16) + " " + (q[1] / 16), 1200);
            b.gui(7, BotChucNang.goi((short) (int) muc));
            b.bom(500);
            int[] q0 = b.quai.get(muc);
            int hp0 = q0 == null ? -1 : q0[2];
            for (int k = 0; k < 6; k++) {
                b.gui(9, BotChucNang.goi((short) (int) muc, (byte) 0));
                b.bom(700);
            }
            b.gui(7, BotChucNang.goi((short) (int) muc));
            b.bom(600);
            int[] q1 = b.quai.get(muc);
            BotChucNang.ket("pb.vao.danh_quai", q1 != null && hp0 > 0 && q1[2] < hp0 ? Boolean.TRUE : (hp0 <= 0 ? null : Boolean.FALSE),
                    "hp " + hp0 + " -> " + (q1 == null ? "?" : q1[2] + "/" + q1[4]) + " cap " + (q1 == null ? "?" : q1[3]));
        }
    }

    static void khu() throws Exception {
        BotChucNang.Bot b = a();
        if (b.mapId != MAP_PB) moVao(b);
        int m = b.moc();
        b.chat("khu");
        b.bom(1500);
        String mc = menuChu(b, m);
        BotChucNang.ket("pb.khu.menu_roi", mc.contains("Rời phó bản") && !mc.contains("Khu 1"), mc);
    }

    static void ketNoi() throws Exception {
        BotChucNang.Bot b = a();
        if (b.mapId != MAP_PB) moVao(b);
        String ten = b.tenMap;
        String user = b.user;
        b.close();
        bot = null;
        Thread.sleep(5000);
        BotChucNang.Bot c = new BotChucNang.Bot(user);
        String l = c.dangNhap("123456");
        if (l == null) l = c.vaoGame(0);
        int m = 0;
        c.bom(3500);
        BotChucNang.ket("pb.ket_noi.ve_phong", l == null && c.mapId == MAP_PB && ten.equals(c.tenMap), (l == null ? "" : l + " ") + "map " + c.mapId + " " + c.tenMap + " (truoc " + ten + ")");
        Object[] t = the(c, m);
        BotChucNang.ket("pb.ket_noi.the_lai", t != null && (Integer) t[0] == 1 && toast(c, m).contains("Đã về lại phó bản"),
                (t == null ? "khong -112" : java.util.Arrays.toString(t)) + " | " + toast(c, m));
        BotChucNang.ket("pb.ket_noi.khong_mat_luot", luot(c) == 1, "luot=" + luot(c));
        bot = c;
    }

    static void roi() throws Exception {
        BotChucNang.Bot b = a();
        if (b.mapId != MAP_PB) moVao(b);
        int m = b.moc();
        b.menu(SO_TAY, MENU_PB_TRONG, 0);
        b.cho(x -> x.mapId != MAP_PB, 4000);
        b.bom(1000);
        Object[] t = the(b, m);
        BotChucNang.ket("pb.roi.ra_khoi", b.mapId != MAP_PB, "map " + b.mapId);
        BotChucNang.ket("pb.roi.an_the", t != null && (Integer) t[0] == 0, t == null ? "khong -112" : java.util.Arrays.toString(t));
    }

    static void luotNgay() throws Exception {
        BotChucNang.Bot b = a();
        for (int lan = 0; lan < 3 && luot(b) < 3; lan++) {
            if (b.mapId == MAP_PB) {
                b.menu(SO_TAY, MENU_PB_TRONG, 0);
                b.cho(x -> x.mapId != MAP_PB, 4000);
                b.bom(800);
            }
            moVao(b);
        }
        if (b.mapId == MAP_PB) {
            b.menu(SO_TAY, MENU_PB_TRONG, 0);
            b.cho(x -> x.mapId != MAP_PB, 4000);
            b.bom(800);
        }
        BotChucNang.ket("pb.luot.da_3", luot(b) == 3, "luot=" + luot(b));
        int m = b.moc();
        moVao(b);
        BotChucNang.ket("pb.luot.het", b.mapId != MAP_PB && toast(b, m).contains("hết lượt"), "map " + b.mapId + " | " + toast(b, m));
        BotChucNang.ket("pb.luot.khong_cong_them", luot(b) == 3, "luot=" + luot(b));
    }

    static void haiPhong() throws Exception {
        try (BotChucNang.Bot c = BotChucNang.botMoi(1); BotChucNang.Bot d = BotChucNang.botMoi(3)) {
            for (BotChucNang.Bot x : new BotChucNang.Bot[]{c, d}) {
                x.gm("slv " + x.ten + " 30 0", 2000);
                x.den(2, 20, 20);
                x.bom(800);
                moVao(x);
            }
            BotChucNang.ket("pb.hai_phong.ca_hai_vao", c.mapId == MAP_PB && d.mapId == MAP_PB, c.mapId + "/" + d.mapId);
            BotChucNang.ket("pb.hai_phong.khac_khu", !c.tenMap.equals(d.tenMap), c.tenMap + " vs " + d.tenMap);
            c.bom(2000);
            boolean thayNhau = c.nguoi.containsKey(d.actor) || d.nguoi.containsKey(c.actor);
            BotChucNang.ket("pb.hai_phong.khong_thay_nhau", !thayNhau, "c thay " + c.nguoi.keySet() + " d thay " + d.nguoi.keySet());
        }
    }

    // ================================================================== cup

    /** Gio ao cua may chu (ms): GM "tua" khong doi so tra "Đồng hồ thử: lệch N giây". null = khong doc duoc. */
    static Long gioAo(BotChucNang.Bot gm) throws Exception {
        int m = gm.moc();
        gm.chat("tua");
        String t = gm.doiChu(m, 5000, "Đồng hồ thử");
        java.util.regex.Matcher k = java.util.regex.Pattern.compile("lệch (-?\\d+) giây").matcher(t == null ? "" : t);
        return k.find() ? System.currentTimeMillis() + Long.parseLong(k.group(1)) * 1000L : null;
    }

    /** Thu Hai 06:00 (gio Viet Nam) cua tuan cup KE TIEP sau {@code ms} (doi tuan luc thu Hai 05:00). */
    static long thuHaiSau(long ms) {
        java.util.Calendar c = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        c.setTimeInMillis(ms);
        c.set(java.util.Calendar.HOUR_OF_DAY, 6);
        c.set(java.util.Calendar.MINUTE, 0);
        c.set(java.util.Calendar.SECOND, 0);
        c.set(java.util.Calendar.MILLISECOND, 0);
        do {
            c.add(java.util.Calendar.DAY_OF_MONTH, 1);
        } while (c.get(java.util.Calendar.DAY_OF_WEEK) != java.util.Calendar.MONDAY);
        return c.getTimeInMillis();
    }

    /** Gia tri cau_hinh truoc khi kich ban doi (null = khong co dong), tra lai cuoi kich ban. */
    static final java.util.Map<String, String> CAU_HINH_CU = new java.util.LinkedHashMap<>();

    static void datCauHinh(String khoa, String gt) {
        if (!CAU_HINH_CU.containsKey(khoa)) CAU_HINH_CU.put(khoa, BotChucNang.sql1("SELECT gia_tri FROM cau_hinh WHERE khoa = '" + khoa + "'"));
        BotChucNang.sql("INSERT INTO cau_hinh (khoa, gia_tri, sua_boi, sua_luc) VALUES ('" + khoa + "', '" + gt
                + "', 'hoi_quy', NOW()) ON DUPLICATE KEY UPDATE gia_tri = '" + gt + "', sua_luc = NOW()");
        BotChucNang.sql("UPDATE cau_hinh_phien_ban SET phien_ban = phien_ban + 1 WHERE id = 1");
    }

    static void traCauHinh() {
        for (java.util.Map.Entry<String, String> e : CAU_HINH_CU.entrySet()) {
            BotChucNang.sql(e.getValue() == null ? "DELETE FROM cau_hinh WHERE khoa = '" + e.getKey() + "'"
                    : "UPDATE cau_hinh SET gia_tri = '" + e.getValue() + "', sua_luc = NOW() WHERE khoa = '" + e.getKey() + "'");
        }
        if (!CAU_HINH_CU.isEmpty()) BotChucNang.sql("UPDATE cau_hinh_phien_ban SET phien_ban = phien_ban + 1 WHERE id = 1");
        CAU_HINH_CU.clear();
    }

    /**
     * cup_vong. CHUAN BI tat dinh (cong kiem 25/10 tren ban sao prod: "Cúp Không trang bị - tuần 43", kt004..007 khong dang
     * ky duoc): loai cup cua tuan theo lich xoay 16 tuan (data_cup_lich) va may chu nap lich MOT LAN moi tuan (CupGame.tuan,
     * nho trong bo nho) - sua data_cup_lich giua tuan khong doi cup tuan dang chay; dong ho ao staging co the dang o bat ky
     * luc nao (nhom truoc / lan chay truoc da "tua", vd sau Chu nhat 19:55 = het dang ky). Nen: dat lich toan Cup Nhi
     * (cap 20-30, don - bot cap 25 hop le) roi tua dong ho ao sang thu Hai 06:00 cua TUAN KE TIEP (tuan moi -> nap lai lich,
     * chua ai dang ky) va doc luat / gio vong dau / so vong tu chinh menu Cup cua may chu. Tra lai lich + cau hinh cuoi.
     */
    static void cupVong() throws Exception {
        List<String> lichCu = BotChucNang.sql("SELECT CONCAT(vi_tri, ':', ma_loai) FROM data_cup_lich ORDER BY vi_tri");
        try {
            cupVong0();
        } finally {
            for (String l : lichCu) {
                String[] f = l.trim().split(":");
                if (f.length == 2 && f[0].matches("\\d+")) BotChucNang.sql("UPDATE data_cup_lich SET ma_loai = '" + f[1].replace("'", "") + "' WHERE vi_tri = " + f[0]);
            }
            traCauHinh();
        }
    }

    static void cupVong0() throws Exception {
        BotChucNang.sql("UPDATE data_cup_lich SET ma_loai = 'nhi'");
        // cup bat + du so doi toi thieu cho 4 bot (ban sao prod co the dat khac mac dinh 4)
        String soDoi = BotChucNang.sql1("SELECT gia_tri FROM cau_hinh WHERE khoa = 'cup.so_doi_toi_thieu'");
        if (soDoi != null && soDoi.trim().matches("\\d+") && Integer.parseInt(soDoi.trim()) > 4) datCauHinh("cup.so_doi_toi_thieu", "4");
        String bat = BotChucNang.sql1("SELECT gia_tri FROM cau_hinh WHERE khoa = 'cup.bat'");
        if (bat != null && !"1".equals(bat.trim())) datCauHinh("cup.bat", "1");
        List<BotChucNang.Bot> ds = new ArrayList<>();
        int[] lop = {0, 1, 3, 4};
        for (int i = 0; i < 4; i++) {
            BotChucNang.Bot x = BotChucNang.botMoi(lop[i]);
            ds.add(x);
        }
        // gio choi >= 10 gio (cong chong clone) - ghi CSDL roi vao lai (bo nho dem chong clone 5 phut chua co)
        List<String> user = new ArrayList<>();
        for (BotChucNang.Bot x : ds) {
            BotChucNang.sql("UPDATE tob_char SET totalTimePlay = 2000 WHERE id = " + x.charDb);
            user.add(x.user);
            x.close();
        }
        Thread.sleep(4000);
        ds.clear();
        for (String u : user) {
            BotChucNang.Bot x = new BotChucNang.Bot(u);
            String l = x.dangNhap("123456");
            if (l == null) l = x.vaoGame(0);
            BotChucNang.ket("cup.vao_lai." + u, l == null, l == null ? "" : l);
            x.gm("slv " + x.ten + " 25 0", 1500);
            x.den(2, 20 + ds.size(), 20);
            ds.add(x);
        }
        BotChucNang.Bot gm = ds.get(0);
        Long ao = gioAo(gm);
        if (ao == null) {
            BotChucNang.ket("cup.dong_ho_ao", false, "khong doc duoc dong ho ao (GM tua; can sv.cho_phep_thu_nghiem=1): " + toast(gm, 0));
            return;
        }
        long thuHai = thuHaiSau(ao);
        gm.gm("tua " + Math.max(1, (thuHai - ao) / 60000L + 1) + "m", 2500);
        Long ao2 = gioAo(gm);
        BotChucNang.ket("cup.dong_ho_ao", ao2 != null && ao2 >= thuHai, "gio ao " + new java.util.Date(ao) + " -> " + (ao2 == null ? "?" : new java.util.Date(ao2))
                + " (thu Hai tuan sau " + new java.util.Date(thuHai) + ")");
        String tuan = BotChucNang.sql1("SELECT COUNT(*) FROM tob_cup_dang_ky");
        int gioVong = 20, soVong = 5;
        for (BotChucNang.Bot x : ds) {
            int m = x.moc();
            x.menu(SO_TAY, MENU_GOC, 10);
            x.bom(900);
            x.menu(SO_TAY, MENU_CUP, 0);
            x.bom(1200);
            String t = toast(x, m);
            java.util.regex.Matcher k = java.util.regex.Pattern.compile("Chủ nhật (\\d+):00 bắt đầu (\\d+) vòng").matcher(t);
            if (k.find()) { gioVong = Integer.parseInt(k.group(1)); soVong = Integer.parseInt(k.group(2)); }
            BotChucNang.ket("cup.dang_ky." + x.ten, t.contains("Đã đăng ký Cúp Nhí"), t + " " + menuChu(x, m));
        }
        String n = BotChucNang.sql1("SELECT COUNT(*) FROM tob_cup_dang_ky");
        BotChucNang.ket("cup.dang_ky.csdl", n != null && Integer.parseInt(n.trim()) - Integer.parseInt(tuan.trim()) == 4, "truoc " + tuan + " sau " + n);
        int m0 = gm.moc();
        gm.menu(SO_TAY, MENU_GOC, 10);
        gm.bom(900);
        gm.menu(SO_TAY, MENU_CUP, 0);
        gm.bom(1000);
        BotChucNang.ket("cup.dang_ky.lai_bi_chan", menuChu(gm, m0).contains("Huỷ đăng ký") || toast(gm, m0).contains("đã đăng ký"), menuChu(gm, m0) + toast(gm, m0));
        // tua toi Chu nhat (gioVong - 1):56 gio ao, cung tuan cup vua dang ky
        Long bay = gioAo(gm);
        long cn = thuHai + 6L * 86400000L + (gioVong - 6) * 3600000L - 4L * 60000L;
        long phut = bay == null ? -1 : (cn - bay) / 60000L;
        if (phut < 0) {
            BotChucNang.ket("cup.tua_chu_nhat", false, "gio ao " + (bay == null ? "?" : new java.util.Date(bay)) + " da qua " + new java.util.Date(cn));
            return;
        }
        gm.gm("tua " + phut + "m", 2500);
        gm.bom(3000);
        final int[] mv = new int[ds.size()];
        for (int i = 0; i < ds.size(); i++) mv[i] = ds.get(i).moc();
        int mVong = mv[1];
        gm.gm("tua 4m", 1500);
        // vong 1 bat dau dung gioVong:00:00 gio ao (con vai giay sau lan tua): cho toi 40 giay de ca 4 bot vao san
        for (int k = 0; k < 80; k++) {
            boolean du = true;
            for (BotChucNang.Bot x : ds) {
                x.bom(100);
                du &= x.mapId == MAP_CUP;
            }
            if (du) break;
            Thread.sleep(400);
        }
        int vao = 0;
        for (BotChucNang.Bot x : ds) {
            if (x.mapId == MAP_CUP) vao++;
            System.out.println("  " + x.ten + " map " + x.mapId + " " + x.tenMap + " | " + toast(x, mv[ds.indexOf(x)]));
        }
        BotChucNang.ket("cup.vong1.vao_san", vao == 4, vao + "/4 vao san 4382 | " + toast(ds.get(1), mVong));
        Object[] t = the(ds.get(1), mVong);
        BotChucNang.ket("cup.vong1.the_112", t != null && (Integer) t[0] == 2 && ((String) t[1]).contains("vòng 1"), t == null ? "khong -112" : java.util.Arrays.toString(t));
        // tim cap doi thu: cung ten ban do (cung khu)
        BotChucNang.Bot p = ds.get(0), q = null;
        for (BotChucNang.Bot x : ds) if (x != p && x.tenMap.equals(p.tenMap)) q = x;
        if (q != null) {
            final BotChucNang.Bot qq = q;
            // di tung buoc 24 px toi doi thu (server chan dich chuyen xa trong mot goi)
            int x0 = p.px, y0 = p.py;
            for (int k = 1; k <= 20; k++) {
                int nx = x0 + (qq.px - x0) * k / 20, ny = y0 + (qq.py - 8 - y0) * k / 20;
                p.gui(4, BotChucNang.goi((short) nx, (short) ny));
                p.bom(160);
            }
            p.bom(400);
            int hpTruoc = qq.hp;
            // con trong 5 giay chuan bi -> chua danh duoc
            p.gui(6, BotChucNang.goi((short) qq.actor, (byte) 0));
            p.bom(800);
            Thread.sleep(5000);
            int m6 = qq.moc();
            for (int k = 0; k < 10; k++) {
                p.gui(6, BotChucNang.goi((short) qq.actor, (byte) 0));
                p.bom(700);
            }
            qq.bom(800);
            int hpMin = Integer.MAX_VALUE;
            for (int i = m6; i < qq.su.size(); i++) {
                BotChucNang.SuKien e = qq.su.get(i);
                if (e.cmd == 6 && e.d != null && e.d.length >= 13) {
                    try {
                        DataInputStream r = new DataInputStream(new ByteArrayInputStream(e.d));
                        r.readShort();
                        int dich = r.readShort();
                        r.readByte();
                        r.readInt();
                        int hp = r.readInt();
                        if (dich == (short) qq.actor) hpMin = Math.min(hpMin, hp);
                    } catch (Exception ignored) {
                    }
                }
            }
            BotChucNang.ket("cup.tran.danh_duoc", hpMin != Integer.MAX_VALUE ? Boolean.valueOf(hpMin < qq.maxhp && hpMin >= 1) : null,
                    "hp doi thu " + hpTruoc + " -> " + (hpMin == Integer.MAX_VALUE ? "khong co goi 6" : hpMin) + "/" + qq.maxhp);
        } else {
            BotChucNang.ket("cup.tran.danh_duoc", null, "khong tim thay doi thu cung khu");
        }
        // het gio tran: tua 4 phut -> hoa / thang theo HP, ve cho cu
        gm.gm("tua 4m", 6000);
        for (BotChucNang.Bot x : ds) x.cho(y -> y.mapId != MAP_CUP, 10000);
        int ra = 0;
        for (BotChucNang.Bot x : ds) if (x.mapId != MAP_CUP) ra++;
        BotChucNang.ket("cup.vong1.ve_cho_cu", ra == 4, ra + "/4 ra khoi san");
        // chi tuan cup cua 4 bot (ban sao prod / lan chay truoc co tran cua tuan khac)
        String tuanBot = BotChucNang.sql1("SELECT MAX(tuan) FROM tob_cup_dang_ky WHERE char_id = " + ds.get(0).charDb);
        String tran = BotChucNang.sql1("SELECT CONCAT(COUNT(*), '/', SUM(ket_qua > 0)) FROM tob_cup_tran WHERE vong = 0 AND tuan = "
                + (tuanBot == null || !tuanBot.trim().matches("-?\\d+") ? "-1" : tuanBot.trim()));
        BotChucNang.ket("cup.vong1.ghi_tran", "2/2".equals(tran == null ? null : tran.trim()), "tran/co ket qua = " + tran);
        // het 5 vong: moi 10 phut mot vong (tua tung buoc de nhip ghep / ket thuc tran chay)
        for (int v = 1; v < soVong; v++) {
            gm.gm("tua 6m", 2500); // sang vong v
            for (BotChucNang.Bot x : ds) x.cho(y -> y.mapId == MAP_CUP, 6000);
            gm.gm("tua 4m", 7000); // het gio tran
            for (BotChucNang.Bot x : ds) x.cho(y -> y.mapId != MAP_CUP, 8000);
        }
        gm.gm("tua 11m", 8000);
        String hang = BotChucNang.sql1("SELECT CONCAT(SUM(hang > 0), '/', COUNT(*)) FROM tob_cup_dang_ky WHERE char_id IN ("
                + ds.get(0).charDb + "," + ds.get(1).charDb + "," + ds.get(2).charDb + "," + ds.get(3).charDb + ")");
        BotChucNang.ket("cup.chot.hang", "4/4".equals(hang == null ? null : hang.trim()), "co hang/tong = " + hang);
        String th = BotChucNang.sql1("SELECT CONCAT(COUNT(*), '/', SUM(da_nhan)) FROM tob_cup_thuong WHERE char_id IN ("
                + ds.get(0).charDb + "," + ds.get(1).charDb + "," + ds.get(2).charDb + "," + ds.get(3).charDb + ")");
        BotChucNang.ket("cup.chot.thuong_ghi_va_phat", th != null && th.trim().matches("[1-4]/[1-4]") && th.trim().charAt(0) == th.trim().charAt(2), "dong/da_nhan = " + th);
        // dang nhap lai: khong phat lan 2 (luong khoa da luu luc thoat = sau lan dang nhap lai + thoat)
        String u0 = ds.get(0).user;
        for (BotChucNang.Bot x : ds) x.close();
        Thread.sleep(5000);
        String lk1 = BotChucNang.sql1("SELECT luongLock FROM tob_char WHERE id = " + ds.get(0).charDb);
        BotChucNang.Bot lai = new BotChucNang.Bot(u0);
        String l = lai.dangNhap("123456");
        if (l == null) l = lai.vaoGame(0);
        lai.bom(3000);
        String lk2 = BotChucNang.sql1("SELECT luongLock FROM tob_char WHERE id = " + lai.charDb);
        lai.close();
        Thread.sleep(4000);
        String lk3 = BotChucNang.sql1("SELECT luongLock FROM tob_char WHERE id = " + lai.charDb);
        BotChucNang.ket("cup.thuong.mot_lan", lk1 != null && lk1.equals(lk3), "luong khoa sau chot " + lk1 + " / dang nhap lai " + lk2 + " / sau luu " + lk3);
        gm.close();
    }

    public static void main(String[] a) throws Exception {
        BotChucNang.dk("pb_vao", true, KichBanPhoBanCup::vao);
        BotChucNang.dk("pb_khu", true, KichBanPhoBanCup::khu);
        BotChucNang.dk("pb_ket_noi", true, KichBanPhoBanCup::ketNoi);
        BotChucNang.dk("pb_roi", true, KichBanPhoBanCup::roi);
        BotChucNang.dk("pb_luot", true, KichBanPhoBanCup::luotNgay);
        BotChucNang.dk("pb_hai_phong", true, KichBanPhoBanCup::haiPhong);
        BotChucNang.dk("cup_vong", true, KichBanPhoBanCup::cupVong);
        List<String> chon = new ArrayList<>();
        for (int i = 3; i < a.length; i++) chon.add(a[i]);
        if (chon.isEmpty()) chon.addAll(java.util.Arrays.asList("pb_vao", "pb_khu", "pb_ket_noi", "pb_roi", "pb_luot", "pb_hai_phong", "cup_vong"));
        String[] b = new String[3 + chon.size()];
        b[0] = a[0];
        b[1] = a[1];
        b[2] = a[2];
        for (int i = 0; i < chon.size(); i++) b[3 + i] = chon.get(i);
        BotChucNang.main(b);
    }
}
