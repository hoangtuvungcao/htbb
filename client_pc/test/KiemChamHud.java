import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.microedition.lcdui.NenPc;

/**
 * KIỂM CHẠM HUD màn chơi (HudRong - PC, J2ME màn rộng cảm ứng), headless, không máy chủ (phiên giả ghi gói gửi đi):
 *  1) ở 11 cỡ chuẩn (ChupTrongGame.CO): vùng CHẠM thật của mọi nút HUD (bảng HudRong.cacNutCham = đúng hình trung()
 *     dùng, nới tới VungCham.MIN) không chồng nhau - kể cả viên "1/2", nút ngữ cảnh câu cá, ô "+"; bấm tâm mỗi nút
 *     trúng đúng nút đó (HudRong.trung và VungCham); viên "1/2" có vùng chạm >= cỡ tối thiểu dù hình vẽ nhỏ;
 *  2) chạm từng ô chiêu (mục tiêu là quái) -> đúng MỘT gói đánh với đúng chiêu của ô đó; ô trống "+" -> bảng gán
 *     chiêu (ChonChieuNhanh), chọn một chiêu -> ô được gán;
 *  3) đang nhắm NPC bán hàng (gian hàng) / NPC menu: chạm chiêu KHÔNG mở cửa hàng / menu NPC (không gói 23, không
 *     menu / hộp thoại), mục tiêu chuyển sang quái; nút đánh chính (phím 5) vẫn nói chuyện với NPC;
 *  4) chạm viên "1/2" (tâm và mép vùng chạm) đổi bộ 1 <-> 2, nút chiêu đổi ngay; phím PC "Đổi bộ chiêu" (R) và
 *     phím J2ME 0 cũng đổi.
 * Cỡ điện thoại: HudRong chỉ chạy trên J2ME cảm ứng (MIN = 0; APK có HUD HD riêng - client_jar/tools/KiemCoCham).
 *
 *   javac -cp client_pc/out/build/cls -d $O client_pc/test/ChupTrongGame.java client_pc/test/KiemChamHud.java \
 *     server/src/com/htbb/pokemon/IconPokemon.java server/src/com/htbb/pokemon/IconPokemonBang.java
 *   java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemChamHud <thư mục>
 * Mã thoát 1 = có lỗi.
 */
public class KiemChamHud {
    static int dat, hong;
    static String co = "";
    static final List<Integer> LENH = new ArrayList<Integer>();
    static final List<byte[]> GOI = new ArrayList<byte[]>();

    static void kiem(boolean ok, String ten) {
        if (ok) dat++;
        else {
            hong++;
            System.out.println("HONG [" + co + "]: " + ten);
        }
    }

    static void ngu(long ms) throws Exception {
        Thread.sleep(ms);
    }

    /** Chờ điều kiện (vòng game 40 ms / nhịp) tối đa 1,5 giây. */
    static boolean cho(java.util.function.BooleanSupplier f) throws Exception {
        for (int i = 0; i < 30; i++) {
            if (f.getAsBoolean()) return true;
            ngu(50);
        }
        return f.getAsBoolean();
    }

    static Object tinh(Class<?> c, String ten) throws Exception {
        Field f = c.getDeclaredField(ten);
        f.setAccessible(true);
        return f.get(null);
    }

    static void datTinh(Class<?> c, String ten, Object gt) throws Exception {
        Field f = c.getDeclaredField(ten);
        f.setAccessible(true);
        f.set(null, gt);
    }

    static void goi(String ten, int x, int y) {
        try {
            Method m = game.render.Canvas.class.getDeclaredMethod(ten, int.class, int.class);
            m.setAccessible(true);
            m.invoke(game.render.Canvas.instance, x, y);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /** Một khung vẽ thật (VungCham đăng ký vùng lúc vẽ). */
    static void ve() throws Exception {
        ChupTrongGame.chay(ChupTrongGame::veThang);
    }

    /** Chạm như ngón tay / chuột: nhấn, giữ ~2 nhịp game, thả; vẽ trước để vùng chạm là của khung đang thấy. */
    static void cham(final int x, final int y) throws Exception {
        ve();
        ChupTrongGame.chay(() -> goi("pointerPressed", x, y));
        ngu(90);
        ChupTrongGame.chay(() -> goi("pointerReleased", x, y));
        ngu(250);
        ve();
    }

    static int[] vung(String ten) {
        for (int i = 0; i < game.pokemon.VungCham.dem(); i++) {
            if (ten.equals(game.pokemon.VungCham.ten(i))) return game.pokemon.VungCham.vungVe(i);
        }
        return null;
    }

    static boolean chamVung(String ten) throws Exception {
        ve();
        int[] v = vung(ten);
        if (v == null) return false;
        cham(v[0] + v[2] / 2, v[1] + v[3] / 2);
        return true;
    }

    static void xoaGoi() {
        synchronized (LENH) {
            LENH.clear();
            GOI.clear();
        }
    }

    static int demLenh(int lenh) {
        int n = 0;
        synchronized (LENH) {
            for (int c : LENH) if (c == lenh) n++;
        }
        return n;
    }

    /** chiêu trong gói đánh (9: short id, byte chiêu; 106: byte chiêu đầu) - -1 = không có. */
    static int chieuDaDanh() {
        synchronized (LENH) {
            for (int i = 0; i < LENH.size(); i++) {
                byte[] d = GOI.get(i);
                if (LENH.get(i) == 9 && d.length >= 3) return d[2];
                if (LENH.get(i) == 106 && d.length >= 1) return d[0];
            }
        }
        return -1;
    }

    static int trung(int x, int y) throws Exception {
        Method m = game.render.screen.HudRong.class.getDeclaredMethod("trung", game.render.screen.GameScr.class, int.class, int.class);
        m.setAccessible(true);
        return (Integer) m.invoke(null, game.render.Canvas.gameScr, x, y);
    }

    static game.model.Monster quai;
    static game.model.NPC npc;

    static game.model.QuickSlot o(int bo, int phim) {
        game.render.screen.GameScr gs = game.render.Canvas.gameScr;
        int[] k = {0, 0, 0, 1, 0, 2, 0, 3, 0, 4};
        return game.model.MainChar.quickSlot[bo][k[phim]];
    }

    /** Màn chơi sạch: đã học 5 chiêu (có bộ 2), bộ 1 đủ chiêu, ô phím 7 trống ("+"), cạnh hồ (nút câu cá). */
    static void dungCanh(final boolean coQuai) throws Exception {
        ChupTrongGame.chay(() -> {
            try {
                ChupTrongGame.moMan("the_gioi");
                game.render.screen.GameScr gs = game.render.Canvas.gameScr;
                game.model.MainChar mc = gs.mainChar;
                game.render.screen.GameScr.indexTabSlot = 0;
                game.model.Char.skillLevelLearnt = new byte[]{1, 2, 1, 1, 0, 0, 1, 1, -1};
                // bảng chiêu (máy chủ gửi lúc vào game): tầm / PP / hồi chiêu / chiêu diện rộng
                if (game.model.skill.SkillManager.SKILL_RANGE == null) {
                    short[][] tam = new short[1][12];
                    short[][][] pp = new short[1][12][11];
                    int[][][] hoi = new int[1][12][11];
                    short[][][] st = new short[1][12][11];
                    for (int k = 0; k < 12; k++) {
                        tam[0][k] = 80;
                        for (int l = 0; l < 11; l++) {
                            pp[0][k][l] = 5;
                            hoi[0][k][l] = 800;
                            st[0][k][l] = 100;
                        }
                    }
                    game.model.skill.SkillManager.SKILL_RANGE = tam;
                    game.model.skill.SkillManager.SKILL_MP = pp;
                    game.model.skill.SkillManager.SKILL_COOLDOWN = hoi;
                    game.model.skill.SkillManager.SKILL_DAM_PERCENT = st;
                    game.model.skill.SkillManager.SKILL_AEO = new byte[5][0];
                }
                if (game.model.MainChar.quickSlot == null) mc.loadQuickSlot();
                for (int b = 0; b < 2; b++) for (int i = 0; i < 5; i++) game.model.MainChar.quickSlot[b][i].setIsNothing();
                int[] chieu = {1, 2, 0, 3, 6};
                for (int i = 0; i < 5; i++) game.model.MainChar.quickSlot[0][i].setIsSkill(chieu[i], mc.clazz, false);
                game.model.MainChar.quickSlot[0][3].setIsNothing();   // phím 7 -> "+"
                mc.x = 200;
                mc.y = 200;
                mc.xTo = mc.x;
                mc.yTo = mc.y;
                mc.mp = mc.maxmp;
                mc.state = 0;
                for (int i = 0; i < mc.timeLastUseSkills.length; i++) {
                    mc.timeLastUseSkills[i] = 0;
                    mc.coolDownSkill[i] = 0;
                }
                gs.actors.removeElement(quai);
                gs.actors.removeElement(npc);
                if (quai == null) {
                    quai = new game.model.Monster();
                    quai.ID = 3001;
                    quai.catagory = 1;
                    quai.setType((short) 0);
                    quai.level = 3;
                }
                quai.maxhp = 500;
                quai.hp = 500;
                quai.setRealHP(500);
                quai.state = 0;
                quai.x = (short) (mc.x + 16);
                quai.y = mc.y;
                quai.xFirst = quai.x;
                quai.yFirst = quai.y;
                if (coQuai) gs.actors.addElement(quai);
                gs.focusedActor = null;
                game.pokemon.HanhDong.boChon();
                if (gs.map < 0) gs.map = 1;   // bản đồ giả lập chưa có số: điểm câu cá theo bản đồ cần map >= 0
                game.pokemon.SinhThai.ganHo();
                game.pokemon.SinhThai.datDiemCau(gs.map, new int[]{mc.x / 16}, new int[]{mc.y / 16});
                // 0.9.6.1: nút câu chỉ hiện khi có Ô NƯỚC hồ trong 2 ô (phao không rơi xuống đất): hồ giả ngay dưới chân
                int wm = game.model.Tilemap.w, hm = game.model.Tilemap.h;
                if (wm > 0 && hm > 0) {
                    boolean[] hoThu = new boolean[wm * hm];
                    int ox = mc.x / 16, oy = mc.y / 16 + 2;
                    for (int yy = oy; yy < oy + 3 && yy < hm; yy++) {
                        for (int xx = ox - 1; xx <= ox + 1; xx++) {
                            if (xx >= 0 && xx < wm && yy >= 0) hoThu[yy * wm + xx] = true;
                        }
                    }
                    game.pokemon.SinhThai.datHoThu(hoThu);
                }
                game.render.Canvas.endDlg();
                game.render.Canvas.menu.showMenu = false;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        ve();
        ve();
    }

    static void nham(final game.model.Actor a) throws Exception {
        ChupTrongGame.chay(() -> {
            try {
                game.render.Canvas.gameScr.focusedActor = a;
                datTinh(game.pokemon.HanhDong.class, "chon", a);   // như người chơi chạm chọn (giữ mục tiêu)
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

    static boolean hinhChong(int[] a, int[] b) {
        if (a.length == 3 && b.length == 3) {
            long dx = a[0] - b[0], dy = a[1] - b[1], r = a[2] + b[2];
            return dx * dx + dy * dy < r * r;
        }
        if (a.length == 4 && b.length == 4) {
            return a[0] < b[0] + b[2] && b[0] < a[0] + a[2] && a[1] < b[1] + b[3] && b[1] < a[1] + a[3];
        }
        int[] t = a.length == 3 ? a : b, h = a.length == 3 ? b : a;
        long nx = Math.max(h[0], Math.min(t[0], h[0] + h[2] - 1)), ny = Math.max(h[1], Math.min(t[1], h[1] + h[3] - 1));
        long dx = t[0] - nx, dy = t[1] - ny;
        return dx * dx + dy * dy < (long) t[2] * t[2];
    }

    /** 1) vùng chạm HUD ở cỡ đang đặt. */
    static void kiemBoCuc(int min) throws Exception {
        game.render.screen.GameScr gs = game.render.Canvas.gameScr;
        final Object[][][] ra = new Object[1][][];
        ChupTrongGame.chay(() -> {
            game.render.Canvas.gameScr.focusedActor = null;   // vòng game có thể vừa tự nhắm một actor của bản đồ
            ChupTrongGame.veThang();
            ra[0] = game.render.screen.HudRong.cacNutCham(game.render.Canvas.gameScr);
        });
        Object[][] v = ra[0];
        kiem(v != null && v.length > 0, "co nut HUD");
        if (v == null) return;
        String[] can = {"hud-chieu0", "hud-chieu1", "hud-chieu2", "hud-chieu3", "hud-danh", "hud-doi-bo", "hud-ngu-canh",
            "hud-thuoc-hp", "hud-thuoc-pp", "hud-tu-dong", "hud-chat", "hud-tren-3"};
        for (String c : can) {
            boolean co1 = false;
            for (Object[] n : v) if (c.equals(n[0])) co1 = true;
            kiem(co1, "nut " + c + " dang hien");
        }
        // hình chạm thật = max(hình trung(), vùng VungCham nới MIN)
        int[][] h = new int[v.length][];
        for (int i = 0; i < v.length; i++) {
            int[] s = (int[]) v[i][2];
            if (s.length == 3) {
                h[i] = new int[]{s[0], s[1], Math.max(s[2], (min + 1) / 2)};
            } else {
                int w = Math.max(s[2], min), hh = Math.max(s[3], min);
                h[i] = new int[]{s[0] - (w - s[2]) / 2, s[1] - (hh - s[3]) / 2, w, hh};
            }
        }
        for (int i = 0; i < v.length; i++) {
            for (int j = i + 1; j < v.length; j++) {
                kiem(!hinhChong(h[i], h[j]), "vung cham " + v[i][0] + " x " + v[j][0] + " chong nhau (" + java.util.Arrays.toString(h[i])
                        + " / " + java.util.Arrays.toString(h[j]) + ", MIN " + min + ")");
            }
            int cx = h[i].length == 3 ? h[i][0] : h[i][0] + h[i][2] / 2, cy = h[i].length == 3 ? h[i][1] : h[i][1] + h[i][3] / 2;
            int ma = trung(cx, cy);
            kiem(ma == (Integer) v[i][1], "bam tam " + v[i][0] + " -> trung ma " + ma + " (can " + v[i][1] + ")");
            if ("hud-doi-bo".equals(v[i][0])) {
                kiem(2 * h[i][2] >= Math.max(min, 26), "vien 1/2: vung cham " + 2 * h[i][2] + " px >= " + Math.max(min, 26));
                kiem(h[i][2] > 9, "vien 1/2: vung cham lon hon hinh ve (r 9)");
            }
        }
        // VungCham (đường chạm thật của Canvas): bấm tâm vùng HUD trúng đúng vùng đó
        for (int i = 0; i < game.pokemon.VungCham.dem(); i++) {
            String t = game.pokemon.VungCham.ten(i);
            if (t == null || !t.startsWith("hud-")) continue;
            int[] r = game.pokemon.VungCham.vungVe(i);
            int j = game.pokemon.VungCham.tim(r[0] + r[2] / 2, r[1] + r[3] / 2);
            kiem(j == i, "VungCham: tam " + t + " trung " + (j < 0 ? "-" : game.pokemon.VungCham.ten(j)));
        }
    }

    static boolean coMenuHayHop() {
        return game.render.Canvas.menu.showMenu || game.render.Canvas.currentDialog != null
                || game.render.Canvas.currentScreen != game.render.Canvas.gameScr;
    }

    /** 2) - 4) chạm thật qua Canvas + vòng game. */
    static void kiemCham() throws Exception {
        game.render.screen.GameScr gs = game.render.Canvas.gameScr;
        // 2) từng ô chiêu, mục tiêu quái
        int[] phim = {1, 3, 7, 9};
        for (int i = 0; i < 4; i++) {
            dungCanh(true);
            nham(quai);
            game.model.QuickSlot qs = o(0, phim[i]);
            boolean trong = qs.quickslotType < 0;
            xoaGoi();
            kiem(chamVung("hud-chieu" + i), "co vung hud-chieu" + i);
            if (trong) {
                kiem(cho(() -> game.pokemon.ChonChieuNhanh.dangMo()), "o trong '+' (chieu" + i + ") mo bang gan chieu");
                kiem(demLenh(9) + demLenh(106) == 0, "o trong '+' khong danh");
                kiem(vung("chon-chieu-0") != null, "bang gan chieu co hang chon-chieu-0");
                chamVung("chon-chieu-0");
                game.model.QuickSlot sau = o(0, phim[i]);
                kiem(sau.quickslotType == game.model.QuickSlot.TYPE_SKILL, "chon chieu trong bang -> o '+' duoc gan (loai " + sau.quickslotType + ")");
                kiem(cho(() -> !game.pokemon.ChonChieuNhanh.dangMo()), "gan xong bang dong (hop " + game.render.Canvas.currentDialog + ", vua trung " + game.pokemon.VungCham.vuaTrung + ")");
            } else {
                int sk = qs.skillType;
                cho(() -> demLenh(9) + demLenh(106) > 0);
                kiem(demLenh(9) + demLenh(106) == 1, "cham chieu" + i + " -> dung 1 goi danh (co " + (demLenh(9) + demLenh(106)) + ")");
                kiem(chieuDaDanh() == sk, "cham chieu" + i + " -> danh chieu " + sk + " (gui " + chieuDaDanh() + ")");
                kiem(demLenh(23) == 0 && !coMenuHayHop(), "cham chieu" + i + " khong mo NPC / menu");
            }
        }
        // 3) nhắm NPC: chiêu không mở cửa hàng / menu NPC
        int[] loaiNpc = {9, 2, 1};   // 9: requestNPCInfo (cửa hàng), 2: menu Mua bán, 1: NPC bán hàng (loại 1)
        for (int k = 0; k < loaiNpc.length; k++) {
            for (int i = 0; i < 4; i++) {
                dungCanh(true);
                if (o(0, phim[i]).quickslotType < 0) continue;
                final int ln = loaiNpc[k];
                ChupTrongGame.chay(() -> {
                    game.render.screen.GameScr g = game.render.Canvas.gameScr;
                    if (npc == null) {
                        npc = new game.model.NPC();
                        try {
                            Field f = game.model.NPC.class.getDeclaredField("img");
                            f.setAccessible(true);
                            f.set(npc, new javax.microedition.lcdui.Image[2]);   // NPC dựng tay: không ảnh cũ
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                    npc.type = (byte) ln;
                    npc.name = "Kecleon";
                    npc.catagory = 2;
                    npc.ID = 77;
                    npc.x = (short) (g.mainChar.x - 20);
                    npc.y = (short) (g.mainChar.y + 4);
                    g.actors.addElement(npc);
                });
                nham(npc);
                xoaGoi();
                chamVung("hud-chieu" + i);
                cho(() -> demLenh(9) + demLenh(106) > 0);
                kiem(demLenh(23) == 0, "NPC loai " + ln + ": cham chieu" + i + " khong gui yeu cau NPC (23) / mo gian hang");
                kiem(!coMenuHayHop(), "NPC loai " + ln + ": cham chieu" + i + " khong mo menu / hop thoai (menu " + game.render.Canvas.menu.showMenu
                        + ", hop " + game.render.Canvas.currentDialog + ")");
                kiem(gs.focusedActor != npc, "NPC loai " + ln + ": cham chieu" + i + " -> bo nham NPC");
                kiem(chieuDaDanh() == o(0, phim[i]).skillType, "NPC loai " + ln + ": cham chieu" + i + " -> danh quai gan nhat bang chieu cua o");
            }
        }
        // nút đánh chính (phím 5) vẫn là nói chuyện khi nhắm NPC
        dungCanh(true);
        ChupTrongGame.chay(() -> {
            game.render.screen.GameScr g = game.render.Canvas.gameScr;
            npc.type = 9;
            g.actors.addElement(npc);
        });
        nham(npc);
        xoaGoi();
        chamVung("hud-danh");
        ngu(200);
        cho(() -> demLenh(23) > 0 || coMenuHayHop());
        kiem(demLenh(23) == 1 || coMenuHayHop(), "nut danh khi nham NPC -> noi chuyen (goi 23: " + demLenh(23) + ")");
        ChupTrongGame.chay(() -> {
            game.render.Canvas.endDlg();
            game.render.Canvas.menu.showMenu = false;
        });
        // 4) viên 1/2
        dungCanh(false);
        kiem(game.render.screen.GameScr.indexTabSlot == 0, "bat dau bo 1");
        kiem(chamVung("hud-doi-bo"), "co vung hud-doi-bo");
        kiem(cho(() -> game.render.screen.GameScr.indexTabSlot == 1), "cham vien 1/2 -> bo 2");
        // nút chiêu đổi ngay: bộ 2 trống -> ô 1 là "+" -> mở bảng gán cho bộ 2
        xoaGoi();
        chamVung("hud-chieu0");
        kiem(cho(() -> game.pokemon.ChonChieuNhanh.dangMo()), "bo 2: o chieu0 la '+' (nut chieu doi ngay) -> bang gan");
        chamVung("chon-chieu-1");
        kiem(game.model.MainChar.quickSlot[1][0].quickslotType == game.model.QuickSlot.TYPE_SKILL, "bo 2: gan chieu vao o 1");
        kiem(game.model.MainChar.quickSlot[0][0].skillType == 1, "bo 1 giu nguyen");
        // mép vùng chạm (ngoài hình vẽ r 9)
        ve();
        int[] v = vung("hud-doi-bo");
        int cx = v[0] + v[2] / 2, cy = v[1] + v[3] / 2, r = v[2] / 2;
        final int truoc = game.render.screen.GameScr.indexTabSlot;
        cham(cx - r + 1, cy);
        kiem(cho(() -> game.render.screen.GameScr.indexTabSlot != truoc), "cham mep vung vien 1/2 (cach tam " + (r - 1) + " px, ngoai hinh ve r 9) -> doi bo");
        ChupTrongGame.chay(() -> game.render.screen.GameScr.indexTabSlot = 0);
        // phím PC "Đổi bộ chiêu" (R) và phím J2ME 0
        Method nh = htbb.pc.BanPhim.class.getDeclaredMethod("nhanPhim", int.class, int.class, boolean.class);
        Method th = htbb.pc.BanPhim.class.getDeclaredMethod("thaPhim", int.class);
        nh.setAccessible(true);
        th.setAccessible(true);
        ChupTrongGame.chay(() -> {
            try {
                nh.invoke(null, htbb.pc.BanPhim.PHIM[htbb.pc.BanPhim.DOI_BO], 0, false);
                th.invoke(null, htbb.pc.BanPhim.PHIM[htbb.pc.BanPhim.DOI_BO]);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        ngu(250);
        kiem(cho(() -> game.render.screen.GameScr.indexTabSlot == 1), "phim PC " + KeyEvent.getKeyText(htbb.pc.BanPhim.PHIM[htbb.pc.BanPhim.DOI_BO]) + " -> bo 2");
        NenPc.phim(0, 48);
        ngu(90);
        NenPc.phim(2, 48);
        ngu(250);
        kiem(cho(() -> game.render.screen.GameScr.indexTabSlot == 0), "phim J2ME 0 -> bo 1");
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        java.io.File ra = new java.io.File(a.length > 0 ? a[0] : "kiem_cham_hud");
        ra.mkdirs();
        System.setProperty("htbb.duLieu", new java.io.File(ra, "du_lieu").getAbsolutePath());
        System.setProperty("microedition.platform", "HTBB-PC/kiem");
        htbb.pc.CaiDat.thuMucDuLieu();
        htbb.pc.CaiDat.nap();
        htbb.pc.CaiDat.tatTieng = true;
        htbb.pc.LopPhu.nap();
        javax.microedition.midlet.MIDlet.GHI_DE.put("HTBB-May-Chu", "127.0.0.1:1");
        NenPc.tinhKichThuoc(1280, 720);
        ChupTrongGame.chay(() -> {
            try {
                javax.microedition.midlet.MIDlet.batDau((javax.microedition.midlet.MIDlet) Class.forName("game.GameMidlet").newInstance());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) ngu(100);
        ngu(2500);
        game.networklogic.GameService.gI().setSession(new network.ISession() {
            public boolean isConnected() {
                return true;
            }

            public void setHandler(network.IMessageHandler h) {
            }

            public void connect(String s, String s2) {
            }

            public void sendMessage(network.Message m) {
                synchronized (LENH) {
                    LENH.add((int) m.command);
                    GOI.add(m.getData());
                }
            }

            public void close() {
            }
        });
        ChupTrongGame.chay(ChupTrongGame::giaLap);
        ngu(500);
        String locCo = System.getProperty("chup.co");
        for (int c = 0; c < ChupTrongGame.CO.length; c++) {
            if (locCo != null && !("," + locCo + ",").contains("," + ChupTrongGame.TEN_CO[c] + ",")) continue;
            final int W = ChupTrongGame.CO[c][0], H = ChupTrongGame.CO[c][1], dpi = ChupTrongGame.CO[c][2];
            co = ChupTrongGame.TEN_CO[c];
            NenPc.tinhKichThuoc(W, H);
            ChupTrongGame.chay(() -> game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao));
            float s = NenPc.hienW / (float) NenPc.rong;
            // PC: 32 px thật; điện thoại: HudRong chỉ chạy trên J2ME (không nới cỡ chạm)
            int min = dpi > 0 ? 0 : (int) Math.ceil(32 / s);
            game.pokemon.VungCham.MIN = min;
            dungCanh(false);
            kiemBoCuc(min);
            ChupTrongGame.chay(() -> game.render.Canvas.gameScr.focusedActor = null);
            javax.imageio.ImageIO.write(ChupTrongGame.ve(W, H), "png", new java.io.File(ra, "hud_" + co + ".png"));   // xem bằng mắt
            if ("pc720".equals(co) || "pc4x3".equals(co) || "dt20x9".equals(co) || "pc21x9".equals(co)) {
                kiemCham();
            }
        }
        System.out.println("KiemChamHud: " + dat + " dat, " + hong + " hong");
        System.exit(hong == 0 ? 0 : 1);
    }
}
