import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import game.render.Canvas;
import game.render.screen.GameScr;

/**
 * Kịch bản quay video giới thiệu HTBB (~2 phút) bằng client PC thật + máy chủ thật (xem quay.sh).
 *   java ... KichBanVideo <thư mục ra> <host:cong> <tài khoản mới> <tên nhân vật> [cảnh,cảnh,...]
 * Mỗi cảnh ghi một đoạn <ra>/doan/<cảnh>.mp4 (30 khung/giây, 1920x1080); ghép, chữ đề, nhạc: dung_video.py.
 * Nhân vật chính là GM (tên có trong sv.adminAccounts của máy chủ THỬ) để dàn cảnh bằng lệnh chat GM (slv, mto, smn...) -
 * lệnh GM gửi lúc KHÔNG ghi. Chỉ dùng trên máy chủ thử cục bộ.
 */
public class KichBanVideo {
    static String TK, TEN;
    static Set<String> CANH;

    static boolean co(String c) {
        return CANH == null || CANH.contains(c);
    }

    static GameScr gs() {
        return Canvas.gameScr;
    }

    static game.model.MainChar mc() {
        return Canvas.gameScr == null ? null : Canvas.gameScr.mainChar;
    }

    /** lệnh GM / chat gửi thẳng máy chủ */
    static void chat(final String s) {
        Quay.tren(new Runnable() {
            public void run() {
                game.networklogic.GameService.gI().chat(s);
            }
        });
    }

    /** -DgmBot=<tk>: lệnh GM dàn cảnh do một bot GM gửi (nhân vật chính là người chơi thường) */
    static final String GM_BOT = System.getProperty("gmBot");

    static void gm(String s, double cho) {
        if (GM_BOT != null && !s.startsWith("mto ") && !s.startsWith("mc ")) bot("chat " + GM_BOT + " " + s);
        else chat(s);
        Quay.ngu(cho);
    }

    static boolean trongTheGioi() {
        return Canvas.currentScreen == Canvas.gameScr && Canvas.gameScr != null && Canvas.gameScr.mainChar != null
                && !game.pokemon.ManTai.dangVao;
    }

    /** gõ chữ từng ký tự vào TField */
    static void go(final game.render.screen.TField tf, String chu, double moiKyTu) {
        for (int i = 1; i <= chu.length(); i++) {
            final String s = chu.substring(0, i);
            Quay.tren(new Runnable() {
                public void run() {
                    tf.setText(s);
                }
            });
            Quay.ngu(moiKyTu);
        }
    }

    /** đi bộ (tìm đường của game) tới ô tx,ty; true = tới trong 2 ô */
    static boolean diToi(final int tx, final int ty, double giay) {
        long het = System.currentTimeMillis() + (long) (giay * 1000);
        while (System.currentTimeMillis() < het) {
            int dx = mc().x / 16 - tx, dy = mc().y / 16 - ty;
            if (dx * dx + dy * dy <= 4) return true;
            Quay.tren(() -> gs().findRoad2(tx * 16 + 8, ty * 16 + 8));
            Quay.ngu(1.0);
        }
        System.out.println("[kb] KHONG TOI " + tx + "," + ty + " o " + mc().x / 16 + "," + mc().y / 16);
        return false;
    }

    /** chữ các lệnh của menu ngữ cảnh đang mở */
    static java.util.List<String> menuChu() {
        java.util.List<String> r = new java.util.ArrayList<String>();
        Object m = Quay.lay(game.pokemon.MenuNguCanh.class, "me");
        if (m == null || Canvas.currentDialog != m) return r;
        java.util.Vector v = (java.util.Vector) Quay.lay(m, "lenh");
        for (int i = 0; v != null && i < v.size(); i++) r.add(String.valueOf(Quay.lay(v.elementAt(i), "caption")));
        return r;
    }

    /** bấm lệnh menu ngữ cảnh có chữ chứa chua (chờ tối đa giay) */
    static boolean bamLenh(final String chua, double giay) {
        final int[] k = {-1};
        Quay.cho(giay, () -> {
            java.util.List<String> m = menuChu();
            for (int i = 0; i < m.size(); i++) if (m.get(i).contains(chua)) { k[0] = i; return true; }
            return false;
        });
        System.out.println("[kb] menu " + menuChu() + " -> " + chua + " = " + k[0]);
        if (k[0] < 0) return false;
        int n = menuChu().size();
        int moiTrang = n > game.pokemon.MenuNguCanh.TOI_DA ? game.pokemon.MenuNguCanh.TOI_DA - 1 : n;
        int trang = (Integer) Quay.lay(Quay.lay(game.pokemon.MenuNguCanh.class, "me"), "trang");
        for (int t = trang; t < k[0] / moiTrang; t++) {
            Quay.bamVung("lenh-" + moiTrang);
            Quay.ngu(0.7);
        }
        return Quay.bamVung("lenh-" + (k[0] % moiTrang));
    }

    static int mapId() {
        return game.model.Tilemap.lv;
    }

    // ================= CẢNH 1: logo, tải, đăng nhập, chọn Pokémon khởi đầu =================
    static void canh1() {
        Quay.ghi("c1_mo_dau");
        Quay.cho(30, () -> Canvas.currentScreen instanceof game.render.screen.ServerListScr && !Canvas.isLoad);
        Quay.ngu(1.6);
        Quay.bamVung("chon-may-chu");
        Quay.cho(5, () -> Canvas.currentScreen instanceof game.render.screen.LoginScr);
        Quay.ngu(0.6);
        go(Canvas.loginScr.tfUser, TK, 0.09);
        Quay.ngu(0.2);
        Quay.tren(() -> {
            Canvas.loginScr.tfUser.isFocus = false;
            Canvas.loginScr.tfPass.isFocus = true;
        });
        go(Canvas.loginScr.tfPass, "123456", 0.06);
        Quay.ngu(0.4);
        Quay.bamVung("dang-nhap-0");
        Quay.cho(15, () -> Canvas.currentScreen instanceof game.render.screen.CharSelectScr);
        Quay.ngu(1.2);
        Quay.bamVung("chon-1");
        Quay.cho(5, () -> Canvas.currentScreen instanceof game.render.screen.CreateCharScr);
        Quay.ngu(0.8);
        // lướt qua 5 Pokémon khởi đầu rồi chọn Charmander
        for (String k : new String[]{"khoi-dau-1", "khoi-dau-3", "khoi-dau-4", "khoi-dau-2", "khoi-dau-0"}) {
            Quay.bamVung(k);
            Quay.ngu(0.85);
        }
        Quay.bamVung("o-nhap");
        Quay.ngu(0.2);
        go(game.render.screen.CreateCharScr.gI().tfChat, TEN, 0.09);
        Quay.ngu(0.5);
        Quay.bamVung("tao-1");
        Quay.cho(10, () -> Canvas.currentScreen instanceof game.render.screen.CharSelectScr);
        Quay.ngu(1.2);
        Quay.bamVung("chon-2");
        Quay.cho(20, () -> trongTheGioi());
        Quay.ngu(1.5);
        Quay.dung();
    }

    // ================= CẢNH 2: lời mở đầu, làng, NPC hướng dẫn, "Đi tới" =================
    static void canh2() {
        Quay.ghi("c2_lang");
        // lời ông nội ở sân nhà (bản đồ 105): nghe vài câu rồi lên đường
        for (int i = 0; i < 2; i++) {
            Quay.ngu(1.4);
            Quay.bam(320, 200);
        }
        Quay.ngu(1.0);
        Quay.tren(() -> Quay.dat(gs(), "indexContent", 13));
        Quay.bam(320, 200);
        Quay.ngu(1.2);
        Quay.bam(320, 200);
        Quay.cho(20, () -> mapId() != 105 && trongTheGioi());
        Quay.ngu(2.5);
        Quay.ngu(0.8);
        Quay.bamVung("lenh-0");
        Quay.ngu(2.0);
        Quay.dung();
        // đứng gần trưởng làng (không ghi), đợi thông báo chào mừng tắt
        System.out.println("[kb] lang o " + mc().x / 16 + "," + mc().y / 16);
        diToi(30, 31, 15);
        diToi(42, 30, 10);
        Quay.ngu(3);
        Quay.ghi("c2_di_toi");
        Quay.ngu(1.0);
        Quay.bamVung("nv-di-toi");
        final game.model.Actor xatu = (game.model.Actor) Quay.goiT(game.pokemon.ManNhiemVu.class, "npc", 5);
        // đi bộ tới khi dừng
        final int[] cu = {-1, -1, 0};
        Quay.cho(14, () -> {
            int x = mc().x, y = mc().y;
            if (x == cu[0] && y == cu[1]) cu[2]++; else cu[2] = 0;
            cu[0] = x; cu[1] = y;
            return cu[2] > 12;
        });
        System.out.println("[kb] toi " + mc().x / 16 + "," + mc().y / 16 + " xatu " + xatu.x / 16 + "," + xatu.y / 16 + " q=" + (gs().currQuest != null && gs().currQuest.isShow));
        if (!(gs().currQuest != null && gs().currQuest.isShow)) {
            Quay.tren(() -> {
                gs().focusedActor = xatu;
                Quay.goi(game.pokemon.HanhDong.class, "noiVoi", gs(), xatu);
            });
        }
        Quay.ngu(1.2);
        bamLenh("Chào", 3);
        for (int i = 0; i < 9; i++) {
            Quay.ngu(1.6);
            System.out.println("[kb] " + i + " " + Quay.trangThai() + " q=" + (gs().currQuest != null && gs().currQuest.isShow) + " " + Quay.vung());
            Quay.anh("c2_b" + i);
            if (Canvas.currentDialog != null) System.out.println("[kb] menu " + menuChu());
            if (gs().currQuest != null && gs().currQuest.isShow) Quay.bam(320, 250);
        }
        Quay.dung();
    }

    /** tới bản đồ map ô x,y bằng lệnh GM (không ghi) */
    static void denMap(int map, int x, int y) {
        final int truoc = mapId();
        gm("mto " + map + " " + x + " " + y, 1.0);
        Quay.cho(15, () -> trongTheGioi() && (mapId() != truoc || truoc == map));
        Quay.ngu(3.0);
        System.out.println("[kb] den map " + mapId() + " o " + mc().x / 16 + "," + mc().y / 16);
    }

    static int lv() {
        return mc().level;
    }

    /** đăng nhập nhân vật đã có (không ghi) */
    static void vaoNhanh() {
        Quay.cho(30, () -> Canvas.currentScreen instanceof game.render.screen.ServerListScr && !Canvas.isLoad);
        Quay.ngu(1);
        Quay.bamVung("chon-may-chu");
        Quay.cho(5, () -> Canvas.currentScreen instanceof game.render.screen.LoginScr);
        Quay.tren(() -> {
            Canvas.loginScr.tfUser.setText(TK);
            Canvas.loginScr.tfPass.setText("123456");
        });
        Quay.goiT(Canvas.loginScr, "doLogin");
        Quay.cho(15, () -> Canvas.currentScreen instanceof game.render.screen.CharSelectScr);
        Quay.ngu(1.5);
        Quay.bamVung("chon-2");
        Quay.cho(20, () -> trongTheGioi());
        Quay.ngu(3);
        System.out.println("[kb] vao game map " + mapId() + " lv " + lv());
    }

    // ================= CẢNH 3: đánh quái, tự động đánh, lên cấp, tiến hoá =================
    static void canh3() {
        denMap(1, 30, 30);
        Quay.ghi("c3_danh");
        for (int i = 0; i < 6; i++) {
            Quay.bamVung("hud-danh");
            Quay.ngu(0.7);
        }
        Quay.bamVung("hud-tu-dong");
        Quay.ngu(7);
        Quay.dung();
        gm("slv " + TEN + " 15 40", 3);
        Quay.ghi("c3_tien_hoa");
        Quay.ngu(2.5);
        chat("slv " + TEN + " 16 5");
        Quay.cho(10, () -> lv() >= 16);
        System.out.println("[kb] len cap " + lv());
        for (int i = 0; i < 6; i++) { Quay.ngu(1); Quay.anh("c3_th" + i); }
        Quay.anh("c3_th");
        Quay.dung();
    }

    static void veTheGioi() {
        Quay.tren(() -> {
            Canvas.currentDialog = null;
            Canvas.menu.showMenu = false;
            Canvas.gameScr.switchToMe();
        });
        Quay.ngu(0.5);
    }

    /** quái hoang dã bậc hiếm (T4+, gói -125) gần nhất */
    static game.model.Monster timHiem() {
        GameScr g = gs();
        for (int i = 0; i < g.actors.size(); i++) {
            Object o = g.actors.elementAt(i);
            if (o instanceof game.model.Monster) {
                game.model.Monster m = (game.model.Monster) o;
                if (m.hp > 0 && !m.wantDestroy && m.pokeBac >= 4) return m;
            }
        }
        return null;
    }

    static game.model.Monster timQuai(String ten) {
        GameScr g = gs();
        for (int i = 0; i < g.actors.size(); i++) {
            Object o = g.actors.elementAt(i);
            if (o instanceof game.model.Monster) {
                game.model.Monster m = (game.model.Monster) o;
                if (m.hp > 0 && !m.wantDestroy && ten.equals(m.getDisplayName())) return m;
            }
        }
        return null;
    }

    static void tuDong(boolean bat) {
        boolean dang = (Boolean) Quay.lay(game.render.screen.TuDong.class, "dang");
        if (dang != bat) Quay.bamVung("hud-tu-dong");
    }

    /** mua Poké Ball ở Chansey (làng), không ghi */
    static void muaBong() {
        gm("smn " + TEN + " 3000000 0 0", 1);
        if (mapId() != 0) denMap(0, 30, 30);
        diToi(30, 31, 15);
        diToi(38, 47, 15);
        for (int i = 0; i < 3; i++) {
            Quay.tren(() -> game.networklogic.GameService.gI().doMenuOption(-581, (byte) 0, 0));
            Quay.ngu(1.0);
        }
        Quay.ngu(1);
        Quay.tren(() -> {
            if (Canvas.currentDialog != null) Canvas.currentDialog = null;
            Canvas.menu.showMenu = false;
        });
    }

    // ================= CẢNH 4: Pokémon hiếm xuất hiện, làm yếu, ném bóng, bắt, Hộp PC =================
    static void canh4() {
        muaBong();
        denMap(Integer.getInteger("mapHiem", 2), 30, 30);
        gm("spt " + TEN + " 6 99", 0.5);
        gm("spt " + TEN + " 3 99", 0.5);
        gm("hst napthegioi", 2);
        java.util.Map<String, Integer> dem = new java.util.TreeMap<String, Integer>();
        for (int i = 0; i < gs().actors.size(); i++) { Object o = gs().actors.elementAt(i); if (o instanceof game.model.Monster) { game.model.Monster m = (game.model.Monster) o; dem.merge(m.getDisplayName() + " lv" + m.level + " hp" + m.maxhp + " t" + m.monster_type, 1, Integer::sum); } }
        System.out.println("[kb] quai " + dem);
        Quay.ghi("c4_hiem");
        tuDong(true);
        Quay.cho(Integer.getInteger("choHiem", 300), () -> timQuai("Vulpix") != null);
        final game.model.Monster q = timQuai("Vulpix");
        Quay.moc("hiem");
        if (q != null) System.out.println("[kb] HIEM " + q.getDisplayName() + " bac " + q.pokeBac + " lv" + q.level + " hp" + q.hp);
        System.out.println("[kb] hiem " + (q == null ? "-" : q.x / 16 + "," + q.y / 16 + " hp " + q.hp + "/" + q.maxhp));
        tuDong(false);
        if (q == null) { Quay.dung(); return; }
        Quay.ngu(1.5);
        // nhắm + đánh thường tới khi yếu
        for (int i = 0; i < 40 && q.hp * 100 > q.maxhp * 30 && q.hp > 0; i++) {
            Quay.tren(() -> gs().focusedActor = q);
            Quay.bamVung("hud-danh");
            Quay.ngu(0.8);
            System.out.println("[kb] danh hp " + q.hp + "/" + q.maxhp);
        }
        System.out.println("[kb] yeu hp " + q.hp + "/" + q.maxhp);
        Quay.ngu(0.8);
        for (int i = 0; i < 4 && gs().actors.contains(q) && !q.wantDestroy; i++) {
            Quay.moc("nem" + i);
            Quay.bamVung("nem-bong", 4);
            Quay.ngu(4.5);
        }
        Quay.moc("xong");
        Quay.ngu(2.5);
        Quay.anh("c4_bat");
        Quay.dung();
        Quay.ngu(1.5);
        Quay.ghi("c4_hop_pc");
        Quay.tren(() -> game.pokemon.ManHopPc.mo(false));
        Quay.ngu(2.5);
        Quay.bamVung("con-0");
        Quay.ngu(3.5);
        Quay.anh("c4_hop");
        System.out.println("[kb] hop " + Quay.vung());
        Quay.dung();
        veTheGioi();
    }

    // ================= CẢNH 5: câu cá ở hồ (cá cắn câu, kéo cần, thẻ kết quả) =================
    static void canh5() {
        denMap(1, 44, 64);
        gm("hst theluc", 1);
        Quay.ghi("c5_cau_ca");
        Quay.ngu(1.0);
        for (int lan = 0; lan < 3; lan++) {
            Quay.moc("tha" + lan);
            if (!Quay.bamVung("hud-ngu-canh", 2)) Quay.tren(() -> game.pokemon.HanhDong.cauCa());
            boolean can = Quay.cho(22, () -> game.pokemon.SinhThai.dangCan());
            Quay.moc(can ? "can" + lan : "khong" + lan);
            if (can) {
                Quay.ngu(0.35);
                if (!Quay.bamVung("hud-ngu-canh", 0.5)) Quay.tren(() -> game.pokemon.HanhDong.cauCa());
            }
            Quay.ngu(4.5);
            Quay.anh("c5_" + lan);
            System.out.println("[kb] cau " + lan + " can=" + can);
        }
        Quay.dung();
    }

    static void noiNpc(int loai) {
        final game.model.Actor n = (game.model.Actor) Quay.goiT(game.pokemon.ManNhiemVu.class, "npc", loai);
        if (n == null) { System.out.println("[kb] khong thay npc " + loai); return; }
        Quay.tren(() -> {
            gs().focusedActor = n;
            Quay.goi(game.pokemon.HanhDong.class, "noiVoi", gs(), n);
        });
    }

    // ================= CẢNH 6: hành trang NRO + thẻ vật phẩm, lò rèn, tủ đồ thử trang phục =================
    static void canh6() {
        gm("gem " + TEN + " 8 30 0", 0.5);
        gm("smn " + TEN + " 2000000 0 0", 0.5);
        if (!Boolean.getBoolean("khongTui")) {
            for (int id : new int[]{7, 33, 59, 8, 34, 60}) gm("ait " + TEN + " " + id + " -1 0 0", 0.4);
            for (int id : new int[]{81, 82}) gm("ait " + TEN + " " + id + " 0 0 0", 0.4);
            for (int id : new int[]{1, 2, 4, 5}) gm("spt " + TEN + " " + id + " 20", 0.3);
            for (int id : new int[]{0, 1, 2, 5, 6, 9}) gm("gem " + TEN + " " + id + " 5 0", 0.3);
        }
        if (mapId() != 0) denMap(0, 30, 30);
        diToi(30, 31, 15);
        diToi(31, 40, 15);
        Quay.ngu(2);
        // hành trang
        Quay.ghi("c6_tui");
        Quay.ngu(0.6);
        Quay.tren(() -> game.pokemon.ManNhanVat.moHanhTrang());
        Quay.ngu(1.8);
        System.out.println("[kb] tui " + Quay.vung());
        Quay.bamVung("tui-0");
        Quay.ngu(2.5);
        Quay.anh("c6_the");
        System.out.println("[kb] the " + Quay.vung());
        Quay.bamVung("dong", 1);
        Quay.ngu(0.8);
        Quay.bamVung("tab-4", 2);
        Quay.ngu(2.0);
        System.out.println("[kb] tu do " + Quay.vung() + " mon=" + game.pokemon.ManTuDo.MON.size());
        for (int i = 0; i < Math.min(4, game.pokemon.ManTuDo.MON.size()); i++) {
            final game.pokemon.ManTuDo.Mon m = (game.pokemon.ManTuDo.Mon) game.pokemon.ManTuDo.MON.elementAt(i);
            System.out.println("[kb] mon " + m.ma + " " + m.ten + " tt" + m.trangThai);
            Quay.bamVung("thu-" + m.ma, 1);
            Quay.ngu(1.6);
            Quay.anh("c6_thu" + i);
        }
        Quay.dung();
        veTheGioi();
        // lò rèn: Machamp
        Quay.ghi("c6_lo_ren");
        noiNpc(2);
        Quay.ngu(2.5);
        if (!bamLenh("Luyện đồ", 1)) {
            bamLenh("Nói chuyện", 2);
            Quay.ngu(1.5);
            if (!bamLenh("Luyện đồ", 3)) {
                veTheGioi();
                Quay.tren(() -> game.networklogic.GameService.gI().requestImbue(0));
            }
        }
        Quay.ngu(1.2);
        bamLenh("Nguyên liệu thường", 3);
        Quay.ngu(2.0);
        System.out.println("[kb] luyen " + Quay.trangThai() + " " + menuChu() + " " + Quay.vung());
        Quay.bamVung("luyen-tui-0");
        Quay.ngu(1.2);
        Quay.bamVung("tvp-mua");
        Quay.ngu(0.8);
        Quay.bamVung("chip-1");
        Quay.ngu(0.8);
        Quay.bamVung("luyen-ngoc-5");
        Quay.ngu(0.8);
        Quay.bamVung("tvp-mua", 1);
        Quay.ngu(0.8);
        if (Quay.timVung("the-vat-pham") != null) { Quay.bamVung("dong", 1); Quay.ngu(0.5); }
        Quay.bamVung("luyen-lam", 2);
        Quay.ngu(1.0);
        Quay.bamVung("xn-dong-y", 2);
        Quay.moc("luyen");
        Quay.ngu(3.5);
        Quay.anh("c6_kq");
        System.out.println("[kb] luyen3 " + Quay.vung());
        Quay.anh("c6_luyen0");
        Quay.anh("c6_luyen");
        Quay.ngu(2);
        Quay.dung();
        veTheGioi();
    }

    // ---------------- người chơi phụ (BotVideo) ----------------
    static java.io.PrintWriter botVao;
    static final String[] BOT_TK = System.getProperty("bots", "lananh99,namkhoa,thuha2k,ducbeo,mylinh").split(",");

    static void bot(String lenh) {
        if (botVao == null) {
            try {
                ProcessBuilder pb = new ProcessBuilder("java", "-Dban=908", "-Dnoi=" + System.getProperty("noi", "."), "-cp", System.getProperty("bot.cp"), "BotVideo",
                        System.getProperty("bot.host", "127.0.0.1"), System.getProperty("bot.cong", "19420"));
                pb.redirectErrorStream(true);
                pb.redirectOutput(new File(Quay.raThuMuc, "bot.log"));
                Process pr = pb.start();
                botVao = new java.io.PrintWriter(new java.io.OutputStreamWriter(pr.getOutputStream(), "UTF-8"), true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        botVao.println(lenh);
    }

    static boolean daCoBot;

    static int botLog(String chua) {
        try {
            int n = 0;
            for (String l : java.nio.file.Files.readAllLines(new File(Quay.raThuMuc, "bot.log").toPath())) if (l.contains(chua)) n++;
            return n;
        } catch (Exception e) {
            return 0;
        }
    }

    /** đưa 5 người chơi phụ vào game, cấp 12, có xu (lệnh GM của nhân vật chính), tới bản đồ của nhân vật chính */
    static void chuanBiBot() {
        if (!daCoBot) {
            int[] lop = {4, 0, 3, 1, 2};
            int n = BOT_TK.length + (GM_BOT != null ? 1 : 0);
            if (GM_BOT != null) bot("vao " + GM_BOT + " 2");
            for (int i = 0; i < BOT_TK.length; i++) bot("vao " + BOT_TK[i] + " " + lop[i % lop.length]);
            Quay.cho(120, () -> botLog("vao: ok") >= n);
            System.out.println("[kb] bot vao " + botLog("vao: ok"));
            for (String t : BOT_TK) {
                gm("slv " + t + " 18 10", 0.4);
                gm("smn " + t + " 200000 0 0", 0.4);
            }
            daCoBot = true;
        }
    }

    // ================= CẢNH 7: chat Thế giới / Bạn bè =================
    static void canh7() {
        if (mapId() != 0) denMap(0, 30, 30);
        chuanBiBot();
        // tin riêng tới nhân vật chính -> tab Bạn bè có hội thoại
        bot("kenh mylinh 4 " + TEN + " chiều nay vào hầm ngục không");
        Quay.ngu(1.5);
        Quay.ghi("c7_chat");
        Quay.ngu(0.6);
        Quay.bamVung("hud-chat");
        Quay.ngu(1.0);
        Quay.tren(() -> game.pokemon.ManChat.moTab(game.pokemon.KenhChat.THE_GIOI));
        String[][] tin = {{"lananh99", "ai lập team săn pokémon hiếm ở fuchsia không"}, {"namkhoa", "vừa câu ra goldeen ở hồ pallet nè"},
            {"thuha2k", "bán than lửa +1 giá rẻ ai cần nhắn nha"}, {"ducbeo", "tối nay vào cúp pvp tuần team còn 1 slot"}};
        for (String[] t : tin) {
            bot("kenh " + t[0] + " 1 - " + t[1]);
            Quay.ngu(1.4);
        }
        Quay.ngu(1.0);
        Quay.moc("ban_be");
        Quay.tren(() -> game.pokemon.ManChat.moRieng("mylinh"));
        Quay.ngu(1.5);
        go(game.pokemon.ManChat.gI().o, "ok luôn mang charmeleon theo nè", 0.06);
        Quay.ngu(0.4);
        Quay.tren(() -> game.pokemon.ManChat.gI().gui());
        Quay.ngu(1.2);
        bot("kenh mylinh 4 " + TEN + " hẹn ở pewter nhé");
        Quay.ngu(2.5);
        Quay.anh("c7_rieng");
        Quay.dung();
        veTheGioi();
    }

    static game.model.Char timNguoi(String ten) {
        GameScr g = gs();
        for (int i = 0; i < g.actors.size(); i++) {
            Object o = g.actors.elementAt(i);
            if (o instanceof game.model.Char && o != mc()) {
                game.model.Char c = (game.model.Char) o;
                if (ten.equalsIgnoreCase(c.name)) return c;
            }
        }
        return null;
    }

    /** kéo-ngắm chiêu phím phim về hướng (dx, dy) rồi thả */
    static void ngam(int phim, double dx, double dy) {
        final int[][] n = {null};
        Quay.tren(() -> {
            for (int y = Canvas.h - 1; y >= 0 && n[0] == null; y -= 2) {
                for (int x = Canvas.w - 1; x >= 0; x -= 2) {
                    int[] o = gs().oChieuTai(x, y);
                    if (o != null && o[0] == phim) { n[0] = o; break; }
                }
            }
        });
        if (n[0] == null) { System.out.println("[kb] khong thay nut chieu " + phim); return; }
        double l = Math.max(1, Math.sqrt(dx * dx + dy * dy));
        int kx = (int) (n[0][1] + dx / l * 60), ky = (int) (n[0][2] + dy / l * 60);
        Quay.keo(n[0][1], n[0][2], kx, ky, 0.7);
    }

    // ================= CẢNH 8: PvP ngắm tay, phó bản Hầm ngục, Cúp PvP tuần =================
    static void canh8() {
        chuanBiBot();
        int r0 = botLog(" ra true");
        bot("ra namkhoa");
        Quay.cho(40, () -> botLog(" ra true") > r0);
        gm("mc namkhoa", 1);
        Quay.cho(15, () -> mapId() == 1 && trongTheGioi());
        Quay.ngu(2);
        diToi(mc().x / 16 + 6, mc().y / 16, 8);
        Quay.ngu(1);
        gm("spt " + TEN + " 14 1", 0.5);
        Quay.tren(() -> game.networklogic.GameService.gI().usePotion(14));
        gm("spt namkhoa 15 1", 0.5);
        bot("binh namkhoa 15");
        Quay.ngu(3);
        game.model.Char dt = timNguoi("namkhoa");
        System.out.println("[kb] pvp=" + game.pokemon.NgamTay.pvp + " doi thu " + (dt == null ? "-" : dt.x / 16 + "," + dt.y / 16) + " minh " + mc().x / 16 + "," + mc().y / 16);
        Quay.ghi("c8_pvp");
        Quay.ngu(0.8);
        int[] phim = {1, 3, 7, 1, 3};
        for (int i = 0; i < phim.length; i++) {
            dt = timNguoi("namkhoa");
            if (dt != null) {
                ngam(phim[i], dt.x - mc().x, dt.y - mc().y);
                double ex = mc().x - dt.x, ey = mc().y - dt.y, el = Math.max(1, Math.sqrt(ex * ex + ey * ey));
                bot("ban namkhoa " + (i % 2) + " " + dt.x + " " + dt.y + " " + (int) (ex / el * 1000) + " " + (int) (ey / el * 1000));
            }
            Quay.ngu(1.4);
        }
        Quay.anh("c8_pvp");
        Quay.dung();
        // phó bản Hầm ngục (Sổ tay -> Hầm ngục bí ẩn -> tổ đội 5 tầng -> Vào)
        gm("slv " + TEN + " 30 0", 1.5);
        Quay.tren(() -> game.networklogic.GameService.gI().doMenuOption(-527, (byte) 0, 2));
        Quay.ngu(1.2);
        Quay.tren(() -> game.networklogic.GameService.gI().doMenuOption(-527, (byte) 3, 2));
        Quay.ngu(1.2);
        veTheGioi();
        final int truoc = mapId();
        Quay.tren(() -> game.networklogic.GameService.gI().doMenuOption(-527, (byte) 60, 0));
        Quay.cho(15, () -> mapId() != truoc && trongTheGioi());
        Quay.ngu(1.0);
        Quay.ghi("c8_ham_nguc");
        Quay.ngu(1.0);
        String[] nut = {"hud-danh", "hud-chieu0", "hud-danh", "hud-chieu1", "hud-danh", "hud-chieu2", "hud-danh", "hud-chieu3", "hud-danh"};
        for (String n : nut) {
            Quay.bamVung(n, 1);
            Quay.ngu(0.9);
        }
        Quay.anh("c8_ham");
        Quay.dung();
        Quay.tren(() -> game.networklogic.GameService.gI().doMenuOption(-527, (byte) 61, 0));
        Quay.cho(15, () -> mapId() == truoc || trongTheGioi());
        Quay.ngu(2);
        // Cúp PvP tuần: menu Sổ tay -> Cúp
        Quay.tren(() -> gs().focusedActor = null);
        Quay.ghi("c8_cup");
        Quay.tren(() -> game.networklogic.GameService.gI().doMenuOption(-527, (byte) 0, 10));
        Quay.ngu(2.5);
        bamLenh("Bảng xếp hạng", 2);
        Quay.ngu(3.0);
        System.out.println("[kb] cup " + menuChu() + " " + Quay.trangThai());
        Quay.anh("c8_cup");
        Quay.dung();
        veTheGioi();
    }

    // ================= CẢNH 9: thời tiết + màu mùa, Ngày cộng đồng =================
    static void thoiTiet(int tt) {
        for (int v = 0; v <= 8; v++) chat("hst thoitiet " + v + " " + tt);
    }

    static void canh9() {
        if (mapId() != 1) denMap(1, 30, 30);
        thoiTiet(0);
        Quay.ngu(3);
        Quay.ghi("c9_thoi_tiet");
        int[] ds = {2, 3, 5, 6, 1};
        for (int tt : ds) {
            thoiTiet(tt);
            Quay.moc("tt" + tt);
            Quay.ngu(3.2);
        }
        Quay.bamVung("hud-thoi-tiet", 2);
        Quay.moc("the");
        Quay.ngu(3.5);
        Quay.anh("c9_the");
        Quay.dung();
        Quay.bamVung("hud-thoi-tiet", 1);
        // Ngày cộng đồng: tua đồng hồ ảo (máy thử) tới giờ vàng 14:00
        java.util.Calendar c = java.util.Calendar.getInstance();
        int toi = (24 - c.get(java.util.Calendar.HOUR_OF_DAY) + 14) % 24;
        gm("tua " + toi + "h", 1);
        Quay.ghi("c9_cong_dong");
        Quay.cho(40, () -> game.pokemon.ThoiTiet.congDong == 2);
        Quay.moc("gio_vang");
        Quay.ngu(5);
        Quay.anh("c9_cd");
        Quay.bamVung("hud-thoi-tiet", 2);
        Quay.ngu(3);
        Quay.anh("c9_cd_the");
        Quay.dung();
    }

    /** Thẻ phản hồi riêng cho GM khi dàn cảnh bằng lệnh chat (người chơi thường không bao giờ thấy) - không đưa lên hình. */
    static final String[] PHAN_HOI_GM = {"Đặt thời tiết", "Đã tua", "Đã nạp lại dữ liệu thế giới", "Đã load", "Tong so char"};

    static boolean laPhanHoiGm(Object tin) {
        if (tin == null) return false;
        String c = String.valueOf(Quay.lay(tin, "chu"));
        for (String p : PHAN_HOI_GM) if (c.startsWith(p)) return true;
        return false;
    }

    static void boPhanHoiGm() {
        Object[] hien = (Object[]) Quay.lay(game.pokemon.DungDo.class, "HIEN");
        for (int i = 0; i < hien.length; i++) if (laPhanHoiGm(hien[i])) hien[i] = null;
        for (String v : new String[]{"CHO", "CHO_BIEU"}) {
            java.util.Vector q = (java.util.Vector) Quay.lay(game.pokemon.DungDo.class, v);
            for (int i = q.size() - 1; i >= 0; i--) if (laPhanHoiGm(q.elementAt(i))) q.removeElementAt(i);
        }
    }

    // ================= CẢNH 10: kết - phông nền + logo của game (chữ kết ghép khi dựng) =================
    static void canh10() {
        Quay.veThay = g -> {
            game.pokemon.Banner.ve(g, -1, true);
            javax.microedition.lcdui.Image lg = game.pokemon.Banner.logo(Canvas.w * 2 / 3, Canvas.h * 2 / 5);
            if (lg != null) g.drawImage(lg, Canvas.w / 2, Canvas.h / 14, javax.microedition.lcdui.Graphics.HCENTER | javax.microedition.lcdui.Graphics.TOP);
        };
        Quay.hienCham = false;
        Quay.ghi("c10_ket");
        Quay.ngu(10);
        Quay.anh("c10_ket");
        Quay.dung();
        Quay.veThay = null;
        Quay.hienCham = true;
    }

    // ================= VIDEO PvP =================
    /** đối thủ bắn lại nhân vật chính (gói -113 của bot) */
    static void botBan(String tk, int o) {
        game.model.Char dt = timNguoi(tk);
        if (dt == null) return;
        double ex = mc().x - dt.x, ey = mc().y - dt.y, el = Math.max(1, Math.sqrt(ex * ex + ey * ey));
        bot("ban " + tk + " " + o + " " + dt.x + " " + dt.y + " " + (int) (ex / el * 1000) + " " + (int) (ey / el * 1000));
    }

    /** ngắm giữ lâu (vạch ngắm hiện rõ) rồi thả */
    static void ngamGiu(int phim, double dx, double dy, double giu) {
        final int[][] n = {null};
        Quay.tren(() -> {
            for (int y = Canvas.h - 1; y >= 0 && n[0] == null; y -= 2) {
                for (int x = Canvas.w - 1; x >= 0; x -= 2) {
                    int[] o = gs().oChieuTai(x, y);
                    if (o != null && o[0] == phim) { n[0] = o; break; }
                }
            }
        });
        if (n[0] == null) return;
        double l = Math.max(1, Math.sqrt(dx * dx + dy * dy));
        int k = NenPcK();
        dongHop();
        int x0 = n[0][1], y0 = n[0][2];
        javax.microedition.lcdui.NenPc.cham(0, x0 * k, y0 * k);
        int buoc = 12;
        for (int i = 1; i <= buoc; i++) {
            int x = (int) (x0 + dx / l * 60 * i / buoc), y = (int) (y0 + dy / l * 60 * i / buoc);
            javax.microedition.lcdui.NenPc.cham(1, x * k, y * k);
            Quay.ngu(0.03);
        }
        Quay.ngu(giu);
        int so = ++soNgam;
        if (ANH_NGAM) {
            System.out.println("[kb] ngam " + so + " giu=" + game.pokemon.NgamTay.dangGiu() + " pvp=" + game.pokemon.NgamTay.pvp);
            Quay.anh("ngam_" + so);
        }
        javax.microedition.lcdui.NenPc.cham(2, (int) (x0 + dx / l * 60) * k, (int) (y0 + dy / l * 60) * k);
        if (ANH_NGAM) {
            Quay.ngu(0.2);
            Quay.anh("bay_" + so);
            Quay.ngu(0.35);
            Quay.anh("trung_" + so);
        }
    }

    /** -DanhNgam=true: chụp ảnh tĩnh lúc đang kéo ngắm / đạn đang bay / lúc trúng (bộ ảnh PvP) */
    static final boolean ANH_NGAM = Boolean.getBoolean("anhNgam");
    static int soNgam;

    static int NenPcK() {
        return javax.microedition.lcdui.NenPc.heSo;
    }

    /** hướng tới ô chặn (cây / đá) gần nhất trong 2-6 ô quanh nhân vật chính; null = không có */
    static double[] huongTuong() {
        for (int r = 2; r <= 6; r++) {
            for (int a = 0; a < 16; a++) {
                double g = Math.PI * 2 * a / 16;
                int x = (int) (mc().x + Math.cos(g) * r * 16), y = (int) (mc().y + Math.sin(g) * r * 16);
                if (game.model.Tilemap.tileTypeAtPixel(x, y, 2)) return new double[]{Math.cos(g), Math.sin(g)};
            }
        }
        return null;
    }

    static void dongHop() {
        if (Canvas.currentDialog != null || Canvas.menu.showMenu) {
            Quay.tren(() -> {
                Canvas.currentDialog = null;
                Canvas.menu.showMenu = false;
            });
        }
    }

    /**
     * Đánh đối thủ tk bằng chiêu kéo-ngắm tới khi hạ được (hoặc hết lượt): tới gần nếu xa, đối thủ chạy né quanh chỗ đứng
     * ban đầu và bắn trả, một lần ngắm vào cây / đá cho thấy tường chặn đạn.
     */
    static void danhDoiThu(String tk, int toiDa, boolean tuong) {
        game.model.Char dt0 = timNguoi(tk);
        if (dt0 == null) return;
        final int bx = dt0.x, by = dt0.y;
        for (int i = 0; i < toiDa; i++) {
            dongHop();
            final game.model.Char dt = timNguoi(tk);
            if (dt == null || dt.hp <= 0) break;
            Quay.tren(() -> gs().focusedActor = dt);   // thẻ đối thủ (tên + HP) trên đầu màn
            int dx = dt.x - mc().x, dy = dt.y - mc().y;
            if (dx * dx + dy * dy > 80 * 80) {
                diToi((dt.x - (dx > 0 ? 48 : -48)) / 16, dt.y / 16, 3);
                continue;
            }
            if (tuong && i == 5) {
                double[] h = huongTuong();
                if (h != null) {
                    Quay.moc("tuong");
                    ngamGiu(1, h[0], h[1], 1.0);
                    Quay.ngu(1.2);
                    continue;
                }
            }
            Quay.moc("ban" + i);
            ngamGiu(new int[]{1, 3, 7, 9}[i % 4], dx, dy, i % 4 == 0 ? 0.6 : 0.25);
            Quay.ngu(0.3);
            if (i % 3 == 2) {
                Quay.moc("ne" + i);
                int k = (i / 3) % 4;
                int[][] o = {{32, 16}, {-32, 16}, {-32, -16}, {32, -16}};
                bot("di " + tk + " " + (bx + o[k][0]) + " " + (by + o[k][1]));
            }
            botBan(tk, i % 2);
            Quay.ngu(0.9);
        }
    }

    /** P1: đấu tay đôi ngoài đồng - kéo ngắm, đối thủ chạy né, bắn trả, tường chặn đạn */
    static void pvp1() {
        chuanBiBot();
        if (mapId() != 1 || timNguoi("namkhoa") == null) {
            int r0 = botLog(" ra true");
            bot("ra namkhoa");
            Quay.cho(40, () -> botLog(" ra true") > r0);
            gm("mc namkhoa", 1);
            Quay.cho(15, () -> mapId() == 1 && trongTheGioi());
            Quay.ngu(2);
            diToi(mc().x / 16 + 3, mc().y / 16 + 1, 8);
        }
        thoiTiet(0);
        gm("slv " + TEN + " 30 0", 0.5);
        gm("slv namkhoa 30 0", 0.5);
        gm("spt " + TEN + " 14 1", 0.5);
        Quay.tren(() -> game.networklogic.GameService.gI().usePotion(14));
        gm("spt namkhoa 15 1", 0.5);
        bot("binh namkhoa 15");
        gm("spt " + TEN + " 6 99", 0.3);
        Quay.ngu(3);
        Quay.tren(() -> gs().focusedActor = null);
        Quay.ghi("p1_dau_tay_doi");
        Quay.ngu(0.8);
        int[] phim = {1, 3, 7, 9, 1, 3, 7, 1};
        for (int i = 0; i < phim.length; i++) {
            game.model.Char dt = timNguoi("namkhoa");
            if (dt == null) break;
            Quay.moc("ban" + i);
            ngamGiu(phim[i], dt.x - mc().x, dt.y - mc().y, i % 3 == 0 ? 0.8 : 0.25);
            Quay.ngu(0.25);
            // đối thủ né: chạy ngang 2-3 ô sau khi thấy đạn
            if (i % 2 == 1) bot("di namkhoa " + dt.x + " " + (dt.y + (i % 4 == 1 ? 40 : -40)));
            botBan("namkhoa", i % 2);
            Quay.ngu(1.0);
        }
        double[] h = huongTuong();
        if (h != null) {
            Quay.moc("tuong");
            ngamGiu(1, h[0], h[1], 1.2);
            Quay.ngu(1.2);
        }
        Quay.anh("p1");
        Quay.dung();
    }

    /** P2: đấu xếp hạng - NPC Đấu trường -> vào hàng -> máy chủ ghép -> Đấu trường 202 -> kết quả */
    static void pvp2() {
        chuanBiBot();
        gm("slv namkhoa 30 0", 0.5);
        if (GM_BOT != null) gm("slv " + TEN + " 30 0", 1.5);
        // cờ PK hai phe (bình 14 / 15) như trận ngoài đồng
        gm("spt " + TEN + " 14 1", 0.5);
        Quay.tren(() -> game.networklogic.GameService.gI().usePotion(14));
        gm("spt namkhoa 15 1", 0.5);
        bot("binh namkhoa 15");
        if (mapId() != 0) denMap(0, 30, 30);
        Quay.ngu(3);
        Quay.ghi("p2_xep_hang");
        Quay.tren(() -> game.networklogic.GameService.gI().requestNPCInfo((byte) -31));
        Quay.ngu(1.5);
        System.out.println("[kb] dau truong menu " + menuChu());
        bamLenh("Đấu xếp hạng", 3);
        Quay.ngu(1.5);
        bot("xephang namkhoa");
        Quay.cho(20, () -> mapId() == 202 && trongTheGioi());
        Quay.moc("vao_san");
        System.out.println("[kb] xep hang map " + mapId());
        Quay.ngu(2.5);
        danhDoiThu("namkhoa", Integer.getInteger("soBan", 45), true);
        Quay.moc("ket_qua");
        Quay.ngu(5);
        Quay.anh("p2");
        Quay.dung();
    }

    /** P3: Cúp PvP tuần - 4 người đăng ký, tua đồng hồ ảo (máy thử) tới Chủ nhật 20:00 -> vòng đấu trên sàn Cúp */
    static void pvp3() {
        chuanBiBot();
        String[] dk = System.getProperty("cupBot", "namkhoa,thuha2k,ducbeo,lananh99,mylinh").split(",");
        for (String t : dk) gm("slv " + t + " 30 0", 0.4);
        Quay.ghi("p3_cup");
        Quay.tren(() -> game.networklogic.GameService.gI().doMenuOption(-527, (byte) 0, 10));
        Quay.ngu(1.5);
        bamLenh("Đăng ký", 3);
        Quay.ngu(2);
        Quay.dung();
        for (String t : dk) bot("cup " + t);
        Quay.ngu(4);
        java.util.Calendar c = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        long bay = c.getTimeInMillis();
        while (c.get(java.util.Calendar.DAY_OF_WEEK) != java.util.Calendar.SUNDAY) c.add(java.util.Calendar.DAY_OF_MONTH, 1);
        c.set(java.util.Calendar.HOUR_OF_DAY, 19);
        c.set(java.util.Calendar.MINUTE, 56);
        c.set(java.util.Calendar.SECOND, 0);
        long phut = Math.max(1, (c.getTimeInMillis() - bay) / 60000L);
        gm("tua " + phut + "m", 3);
        gm("tua 4m", 1);
        Quay.ghi("p3_cup_tran");
        Quay.cho(50, () -> mapId() == 4382 && trongTheGioi());
        Quay.moc("vao_san");
        System.out.println("[kb] cup map " + mapId());
        Quay.ngu(6);   // 5 giây chuẩn bị
        String dt0 = null;
        for (String t : dk) if (timNguoi(t) != null) dt0 = t;
        final String dt = dt0;
        System.out.println("[kb] cup doi thu " + dt);
        if (dt != null) {
            danhDoiThu(dt, Integer.getInteger("soBanCup", 22), true);
        }
        // hết giờ vòng (Cúp: HP không về 0, thắng theo % HP còn lại) - tua tới cuối vòng để thấy kết quả
        gm("tua 3m", 0.5);
        Quay.moc("ket_qua");
        Quay.ngu(6);
        Quay.anh("p3");
        Quay.dung();
    }

    public static void main(String[] a) throws Exception {
        File ra = new File(a[0]);
        ra.mkdirs();
        Quay.raThuMuc = ra;
        TK = a[2];
        TEN = a[3];
        if (a.length > 4) CANH = new HashSet<String>(Arrays.asList(a[4].split(",")));
        Quay.truocVe = () -> boPhanHoiGm();
        Quay.khoiDong(a[1], new File(ra, "du_lieu_" + TK));
        try {
            if (co("1")) canh1(); else vaoNhanh();
            if (co("2")) canh2();
            if (co("3")) canh3();
            if (co("4")) canh4();
            if (co("5")) canh5();
            if (co("6")) canh6();
            if (co("7")) canh7();
            if (co("8")) canh8();
            if (co("9")) canh9();
            if (co("10")) canh10();
            if (CANH != null && CANH.contains("p1")) pvp1();
            if (CANH != null && CANH.contains("p3")) pvp3();
            if (CANH != null && CANH.contains("p2")) pvp2();
            if (co("chu") && System.getProperty("chu.ds") != null) {
                javax.microedition.lcdui.Image lg = Quay.tren(() -> game.pokemon.Banner.logo(1000, 480));
                if (lg != null) javax.imageio.ImageIO.write(lg.awt(), "png", new File(ra, "logo_hd.png"));
            }
            if (co("chu") && System.getProperty("chu.ds") != null) ChuDeVideo.sinh(new File(System.getProperty("chu.ds")), new File(ra, "chu_de"), 3, 4);
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            Quay.dung();
            if (botVao != null) botVao.println("thoat");
        }
        System.exit(0);
    }
}
