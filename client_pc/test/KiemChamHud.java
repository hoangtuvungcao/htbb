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
 *     phím J2ME 0 cũng đổi;
 *  5) 0.9.17 (kiemDichNut): ở mọi cỡ, không mục tiêu / nhắm quái Ekans (+ thẻ thời tiết mở) / nhắm NPC: chạm mọi nút
 *     HUD và mọi ô Menu cá nhân -> đúng màn / thẻ (tiêu đề) / gói; menu máy chủ trả cho yêu cầu từ Menu mang tiêu đề nơi
 *     gửi, không bao giờ tên quái đang nhắm (lỗi 0.9.16: Menu › Sự kiện ra thẻ "Ekans" = menu Khác). Bảng dich_nut.txt.
 *     -Dchup.j2me = vẽ như J2ME; -Dkiem.dich=0 bỏ phần này.
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

    /**
     * 0.9.7: chạm thẻ người chơi góc trái trên mở màn Nhân vật - HudRong (PC / J2ME màn rộng) và đường J2ME màn nhỏ
     * không HudRong (giả lập bằng HudRong.tat = true: GameScr.updatePoint).
     */
    static void kiemHoSo(boolean j2meNho) throws Exception {
        ChupTrongGame.chay(() -> {
            game.render.Canvas.endDlg();
            game.render.Canvas.menu.showMenu = false;
            game.render.Canvas.gameScr.focusedActor = null;
            game.render.Canvas.gameScr.switchToMe();
        });
        game.render.screen.HudRong.tat = j2meNho;
        ngu(200);
        cham(30, 18);
        boolean mo = cho(() -> game.render.Canvas.currentScreen instanceof game.pokemon.ManNhanVat);
        kiem(mo, "cham the nguoi choi trai tren -> man Nhan vat" + (j2meNho ? " (J2ME man nho, khong HudRong)" : " (HudRong)"));
        game.render.screen.HudRong.tat = false;
        ChupTrongGame.chay(() -> game.render.Canvas.gameScr.switchToMe());
        ngu(150);
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
            "hud-thuoc-hp", "hud-thuoc-pp", "hud-tu-dong", "hud-chat", "hud-tren-3", "hud-tu-dong-cai-dat", "hud-cai-dat"};
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
        kiemMoTuDong();
        kiemCaiDatHud();
    }

    static boolean bangTuDongMo() {
        try {
            return (Boolean) tinh(Class.forName("game.render.screen.BangTuDong"), "mo");
        } catch (Throwable e) {
            return false;
        }
    }

    static void dongTuDong() throws Exception {
        datTinh(Class.forName("game.render.screen.BangTuDong"), "mo", Boolean.FALSE);
        ve();
    }

    /** 0.9.9: mọi lối vào bảng Tự động đánh mở được tấm VÀ tấm vẫn mở sau vài khung (0.9.8 mất menu AUTO). */
    static void kiemMoTuDong() throws Exception {
        kiemMoTuDong("");
        // bản đồ khoá Tự động (gói -108): ổ khoá trên viên AUTO - bảng cài đặt vẫn mở được
        int map = game.render.Canvas.gameScr.map;
        game.pokemon.KhoaMap.dat(map, game.pokemon.KhoaMap.CO_KHONG_TU_DONG, null, "Bản đồ này không cho Tự động đánh");
        kiemMoTuDong(" (map khoa Tu dong)");
        game.pokemon.KhoaMap.xoa();
    }

    /** 0.9.9: nút Cài đặt (bánh răng) trên HUD mở bảng Cài đặt PC (LopPhu) - không chỉ ở Menu. */
    static void kiemCaiDatHud() throws Exception {
        dungCanh(false);
        Field f = htbb.pc.LopPhu.class.getDeclaredField("caiDatMo");
        f.setAccessible(true);
        f.setBoolean(null, false);
        kiem(chamVung("hud-cai-dat"), co + ": co nut Cai dat tren HUD (hud-cai-dat)");
        ngu(300);
        kiem(f.getBoolean(null), co + ": cham nut Cai dat HUD -> mo bang Cai dat");
        f.setBoolean(null, false);
        ve();
    }

    static void kiemMoTuDong(String bt) throws Exception {
        String co = KiemChamHud.co + bt;
        dungCanh(false);
        dongTuDong();
        kiem(chamVung("hud-tu-dong-cai-dat"), co + ": co vung banh rang Tu dong (hud-tu-dong-cai-dat)");
        ngu(300);
        ve();
        kiem(bangTuDongMo(), co + ": cham banh rang -> bang Tu dong danh mo va con mo sau vai khung");
        // 0.9.9: "Trang chiêu dùng khi tự động" (mặc định trang đang dùng), chạm = trang kế, lưu RMS
        Class<?> td = game.render.screen.TuDong.class;
        datTinh(td, "trangChieu", Integer.valueOf(-1));
        ve();
        kiem(vung("td-trang") != null, co + ": bang Tu dong co hang Trang chieu (td-trang)");
        if (vung("td-trang") != null) {
            chamVung("td-trang");
            kiem(((Integer) tinh(td, "trangChieu")) == 0, co + ": cham Trang chieu -> Trang 1 (" + tinh(td, "trangChieu") + ")");
            kiem(bangTuDongMo(), co + ": cham Trang chieu khong dong bang");
            Method dsc = td.getDeclaredMethod("dsChieu", game.model.MainChar.class);
            dsc.setAccessible(true);
            Method ct = td.getDeclaredMethod("chieuTrang", game.model.MainChar.class, int.class);
            ct.setAccessible(true);
            byte[] thuTu = (byte[]) tinh(td, "thuTu");
            byte[] cu = thuTu.clone();
            java.util.Arrays.fill(thuTu, (byte) -1);
            game.model.MainChar mc = game.render.Canvas.gameScr.mainChar;
            byte[] tr0 = (byte[]) ct.invoke(null, mc, 0);
            byte[] ds = (byte[]) dsc.invoke(null, mc);
            kiem(tr0.length == 0 || java.util.Arrays.equals(ds, tr0), co + ": Tu chon + Trang 1 -> chieu cua trang 1 " + java.util.Arrays.toString(tr0) + " = " + java.util.Arrays.toString(ds));
            System.arraycopy(cu, 0, thuTu, 0, cu.length);
            datTinh(td, "trangChieu", Integer.valueOf(-1));
            ve();
        }
        dongTuDong();
        // nhấn giữ viên AUTO >= 0,45 s
        ve();
        int[] v = vung("hud-tu-dong");
        kiem(v != null, co + ": co vung vien AUTO");
        if (v != null) {
            final int x = v[0] + v[2] / 2, y = v[1] + v[3] / 2;
            final boolean dangTruoc = game.render.screen.TuDong.dang;
            ChupTrongGame.chay(() -> goi("pointerPressed", x, y));
            for (int i = 0; i < 8; i++) {
                ve();
                ngu(90);
            }
            ChupTrongGame.chay(() -> goi("pointerReleased", x, y));
            ngu(300);
            ve();
            kiem(bangTuDongMo(), co + ": nhan giu vien AUTO -> bang Tu dong mo");
            kiem(game.render.screen.TuDong.dang == dangTruoc, co + ": nhan giu vien AUTO khong bat / tat tu dong");
            dongTuDong();
        }
        // Menu (ManMenu) -> Tự động đánh: lối vào luôn có
        ChupTrongGame.chay(() -> game.pokemon.ManMenu.mo());
        ve();
        boolean coO = false;
        for (int i = 0; i < game.pokemon.VungCham.dem(); i++) {
            String t = game.pokemon.VungCham.ten(i);
            if ("menu-Tự động đánh".equals(t)) coO = true;
        }
        kiem(coO, co + ": Menu co o 'Tu dong danh'");
        if (coO) {
            chamVung("menu-Tự động đánh");
            ngu(300);
            ve();
            kiem(bangTuDongMo(), co + ": Menu -> Tu dong danh mo bang");
        }
        dongTuDong();
        ChupTrongGame.chay(() -> game.render.Canvas.gameScr.switchToMe());
        ve();
    }


    // ================================================================== 0.9.17: KIỂM ĐÍCH MỌI NÚT HUD + Ô MENU
    // Chủ dự án (PC 0.9.16): nhắm Ekans, thẻ thời tiết đang mở, bấm Menu › "Sự kiện" -> thẻ tiêu đề "Ekans" với Mở Túi Đồ /
    // Bỏ đồ vào túi / Mở Kho / Shop Đặc Biệt... (menu "Khác" -10000 của máy chủ + tiêu đề = tên mục tiêu). Ở mọi cỡ, có /
    // không mục tiêu (quái Ekans, NPC), có / không thẻ thời tiết: chạm từng nút HUD và từng ô Menu -> đúng màn / thẻ / gói,
    // thẻ lệnh không bao giờ mang tên quái đang nhắm; menu máy chủ trả cho yêu cầu từ Menu mang tiêu đề nơi gửi.

    static int soNutDich, soOMenu, soCheThe, soChamLai;
    static final java.util.TreeMap<String, String> BANG_DICH = new java.util.TreeMap<String, String>();

    /** Trạng thái sau một lần chạm. */
    static final class Kq {
        String man, hop, tieu;
        String[] lenh;
        boolean menuChu, caiDat, tuDongBang, theTT, banDoTo, tuDongDang;
        int tab;
        List<Integer> lenhGui = new ArrayList<Integer>();
        List<byte[]> goiGui = new ArrayList<byte[]>();

        public String toString() {
            return "man=" + man + (hop != null ? " hop=" + hop : "") + (tieu != null ? " tieu='" + tieu + "'" : "")
                    + (menuChu ? " menuChu" : "") + (caiDat ? " caiDat" : "") + (tuDongBang ? " bangTuDong" : "")
                    + (theTT ? " theThoiTiet" : "") + (banDoTo ? " banDoTo" : "") + " goi=" + lenhGui;
        }

        boolean coGoi(int c) {
            return lenhGui.contains(c);
        }

        /** gói -30 (menu NPC ảo) đúng (npc, menu, mục) */
        boolean coMenuOption(int npc, int menu, int muc) {
            for (int i = 0; i < lenhGui.size(); i++) {
                byte[] d = goiGui.get(i);
                if (lenhGui.get(i) == -30 && d.length >= 4) {
                    int n = (short) (((d[0] & 0xFF) << 8) | (d[1] & 0xFF));
                    if (n == npc && d[2] == menu && (d[3] & 0xFF) == muc) return true;
                }
            }
            return false;
        }
    }

    static Kq doc() throws Exception {
        final Kq k = new Kq();
        ChupTrongGame.chay(() -> {
            try {
                game.render.screen.Screen s = game.render.Canvas.currentScreen;
                k.man = s == null ? "null" : s == game.render.Canvas.gameScr ? "the_gioi" : s.getClass().getSimpleName();
                Object d = game.render.Canvas.currentDialog;
                k.hop = d == null ? null : d.getClass().getSimpleName();
                if (d instanceof game.pokemon.MenuNguCanh) {
                    k.tieu = game.pokemon.MenuNguCanh.tieuDe();
                    k.lenh = game.pokemon.MenuNguCanh.cacLenh();
                }
                k.menuChu = game.render.Canvas.menu.showMenu;
                Field f = htbb.pc.LopPhu.class.getDeclaredField("caiDatMo");
                f.setAccessible(true);
                k.caiDat = f.getBoolean(null);
                k.tuDongBang = bangTuDongMo();
                k.theTT = game.pokemon.ThoiTiet.moThe;
                k.banDoTo = (Boolean) tinh(game.render.screen.HudRong.class, "banDoTo");
                k.tuDongDang = game.render.screen.TuDong.dang || (Boolean) tinh(game.render.screen.TuDong.class, "choBat");
                k.tab = game.render.screen.GameScr.indexTabSlot;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        synchronized (LENH) {
            k.lenhGui.addAll(LENH);
            k.goiGui.addAll(GOI);
        }
        return k;
    }

    /** Thời tiết + mùa như ảnh chủ dự án: "Quang đãng · còn 4 giờ 49 phút", "Mùa: Hoàng hôn Halloween". */
    static void datThoiTiet() throws Exception {
        ChupTrongGame.chay(() -> {
            game.pokemon.ThoiTiet.thu = true;
            game.pokemon.ThoiTiet.datTrangThai(0, "Quang đãng", "Quang đãng. Trời đẹp, không ảnh hưởng chiêu.", 4 * 3600 + 49 * 60 + 30,
                    0, 3, "Hoàng hôn Halloween", 0, "", 0, false, "");
        });
    }

    /** Về màn chơi sạch, mục tiêu mt, thẻ thời tiết mở / đóng. */
    static void sach(final game.model.Actor mt, final boolean the) throws Exception {
        ChupTrongGame.chay(() -> {
            try {
                game.render.Canvas.endDlg();
                game.render.Canvas.menu.showMenu = false;
                game.render.Canvas.gameScr.switchToMe();
                datTinh(Class.forName("game.render.screen.BangTuDong"), "mo", Boolean.FALSE);
                datTinh(game.render.screen.HudRong.class, "banDoTo", Boolean.FALSE);
                Field f = htbb.pc.LopPhu.class.getDeclaredField("caiDatMo");
                f.setAccessible(true);
                f.setBoolean(null, false);
                if (game.render.screen.TuDong.dang || (Boolean) tinh(game.render.screen.TuDong.class, "choBat")) {
                    game.render.screen.TuDong.tat(false);
                }
                datTinh(game.render.screen.TuDong.class, "choBat", Boolean.FALSE);
                game.render.screen.TuDong.dang = false;
                game.render.screen.GameScr.indexTabSlot = 0;
                game.pokemon.ThoiTiet.moThe = the;
                game.pokemon.MenuNguCanh.boCho();
                // khung giả không tăng NenPc.soKhung: ô nhập của bảng chat vẽ lần cuối sẽ giữ BanPhim.dangGo() mãi
                datTinh(htbb.pc.BanPhim.class, "khungONhap", Long.valueOf(-100));
                game.model.MainChar mc = game.render.Canvas.gameScr.mainChar;   // chiêu không đang hồi / thiếu PP
                mc.mp = mc.maxmp;
                mc.state = 0;
                for (int i = 0; i < mc.timeLastUseSkills.length; i++) {
                    mc.timeLastUseSkills[i] = 0;
                    mc.coolDownSkill[i] = 0;
                }
                mc.x = 200;   // lần chạm trước (Bản đồ, nói NPC, đánh) có thể đã cho nhân vật đi
                mc.y = 200;
                mc.xTo = mc.x;
                mc.yTo = mc.y;
                mc.posTransRoad = null;
                datTinh(game.pokemon.HanhDong.class, "choNoi", null);
                if (quai != null) {
                    quai.x = (short) (mc.x + 16);
                    quai.y = mc.y;
                    quai.hp = 500;
                    quai.setRealHP(500);
                    quai.state = 0;
                }
                game.render.Canvas.gameScr.focusedActor = mt;
                datTinh(game.pokemon.HanhDong.class, "chon", mt);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        ve();
        ChupTrongGame.chay(() -> {
            try {
                game.render.Canvas.gameScr.focusedActor = mt;   // vòng game có thể vừa tự đổi mục tiêu
                datTinh(game.pokemon.HanhDong.class, "chon", mt);
                game.pokemon.ThoiTiet.moThe = the;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        ve();
        xoaGoi();
    }

    static String tenQuai() {
        try {
            return quai.getDisplayName();
        } catch (Throwable e) {
            return "";
        }
    }

    /** Thẻ lệnh đang mở (nếu có) không mang tên quái đang nhắm. */
    static void khongTenQuai(Kq k, String nhan) {
        String q = tenQuai();
        if (k.tieu != null && q != null && q.length() > 0) {
            kiem(!k.tieu.equals(q), nhan + ": the lenh mang ten quai dang nham '" + q + "' (" + k + ")");
        }
    }

    /** Vùng chạm đang vẽ theo tên (null = không có). */
    static int[] vungTen(String ten) throws Exception {
        ve();
        return vung(ten);
    }

    static boolean trongThe(int[] v) {
        int[] t = game.pokemon.ThoiTiet.VUNG_THE;
        int cx = v[0] + v[2] / 2, cy = v[1] + v[3] / 2;
        return game.pokemon.ThoiTiet.moThe && cx >= t[0] && cx < t[0] + t[2] && cy >= t[1] && cy < t[1] + t[3];
    }

    /**
     * Một nút HUD: chạm tâm vùng thật, đọc kết quả, so với đích mong đợi. mt = mục tiêu (null / quai / npc).
     * Trả false khi nút không hiện ở cảnh này.
     */
    static boolean kiemNut(final String ten, game.model.Actor mt, boolean the, String canh) throws Exception {
        sach(mt, the);
        int[] v = vungTen(ten);
        if (v == null) return false;
        String nhan = co + " " + canh + " " + ten;
        final boolean dangTruoc = game.render.screen.TuDong.dang;
        boolean cheThe = !"hud-thoi-tiet".equals(ten) && trongThe(v);
        cham(v[0] + v[2] / 2, v[1] + v[3] / 2);
        ngu(150);
        if (mt == quai && ("hud-danh".equals(ten) || ten.startsWith("hud-chieu"))) {
            cho(() -> demLenh(9) + demLenh(106) > 0 || game.render.Canvas.currentDialog != null);
        }
        Kq k = doc();
        soNutDich++;
        khongTenQuai(k, nhan);
        String ok;
        if (cheThe) {
            // nút nằm dưới thẻ thời tiết: chạm chỉ đóng thẻ, không mở việc khác
            soCheThe++;
            kiem(!k.theTT && "the_gioi".equals(k.man) && k.hop == null && !k.menuChu, nhan + " (duoi the thoi tiet): cham chi dong the (" + k + ")");
            BANG_DICH.put(ten + " | " + canh, "dong the thoi tiet");
            return true;
        }
        boolean q = mt == quai, n = mt == npc;
        switch (ten) {
            case "hud-the":
                kiem("ManNhanVat".equals(k.man), nhan + " -> man Nhan vat (" + k + ")");
                break;
            case "hud-tren-0":
                kiem("ManNhanVat".equals(k.man), nhan + " (Hanh trang) -> man Nhan vat / hanh trang (" + k + ")");
                break;
            case "hud-tren-1":
                kiem("ManNhiemVu".equals(k.man), nhan + " (Nhiem vu) -> so Nhiem vu (" + k + ")");
                break;
            case "hud-tren-2":
                kiem(k.banDoTo || !"the_gioi".equals(k.man), nhan + " (Ban do) -> ban do (" + k + ")");
                kiem(k.hop == null, nhan + " (Ban do) khong mo the lenh (" + k + ")");
                break;
            case "hud-tren-3":
                kiem("ManMenu".equals(k.man) && k.hop == null, nhan + " (Menu) -> Menu ca nhan (" + k + ")");
                break;
            case "hud-cai-dat":
                kiem(k.caiDat && k.hop == null, nhan + " -> bang Cai dat (" + k + ")");
                break;
            case "hud-thoi-tiet":
                kiem(k.theTT != the && k.hop == null && "the_gioi".equals(k.man), nhan + " -> bat / tat the thoi tiet (" + k + ")");
                break;
            case "hud-chat":
                kiem("ManChat".equals(k.man), nhan + " -> bang chat (" + k + ")");
                break;
            case "hud-tu-dong":
                kiem(k.tuDongDang != dangTruoc && k.hop == null && "the_gioi".equals(k.man), nhan + " -> bat / tat Tu dong (" + k + ")");
                break;
            case "hud-tu-dong-cai-dat":
                kiem(k.tuDongBang && k.hop == null, nhan + " -> bang Tu dong danh (" + k + ")");
                break;
            case "hud-doi-bo":
                kiem(k.tab == 1 && k.hop == null && "the_gioi".equals(k.man), nhan + " -> doi bo chieu (" + k + ")");
                break;
            case "hud-danh":
                if (n) {
                    kiem(k.coGoi(23) || k.hop != null || k.menuChu, nhan + " nham NPC -> noi chuyen (" + k + ")");
                } else {
                    kiem("the_gioi".equals(k.man) && k.hop == null && !k.menuChu, nhan + " -> danh, khong mo man / menu (" + k + ")");
                    if (q) kiem(k.coGoi(9) || k.coGoi(106), nhan + " nham quai -> goi danh (" + k + ")");
                }
                break;
            default:
                if (ten.startsWith("hud-chieu")) {
                    boolean gan = "ChonChieuNhanh".equals(k.hop);
                    kiem("the_gioi".equals(k.man) && (k.hop == null || gan) && !k.menuChu && !k.coGoi(23),
                            nhan + " -> danh / bang gan chieu, khong mo NPC / menu (" + k + ")");
                    if (q && !gan) kiem(k.coGoi(9) || k.coGoi(106), nhan + " nham quai -> goi danh (" + k + ")");
                } else {
                    // thuốc / câu cá / ném bóng: không mở màn / thẻ lệnh
                    kiem("the_gioi".equals(k.man) && !"MenuNguCanh".equals(k.hop) && !k.menuChu, nhan + " khong mo man / menu (" + k + ")");
                }
        }
        BANG_DICH.put(ten + " | " + canh, k.toString().replaceAll(" goi=\\[\\]", ""));
        return true;
    }

    /** Mở Menu cá nhân bằng nút HUD (như người chơi). */
    static boolean moMenuHud(game.model.Actor mt, boolean the) throws Exception {
        sach(mt, the);
        ChupTrongGame.chay(() -> game.pokemon.ThoiTiet.moThe = false);   // thẻ thời tiết không che nút Menu khi mở Menu
        int[] v = vungTen("hud-tren-3");
        if (v == null) return false;
        cham(v[0] + v[2] / 2, v[1] + v[3] / 2);
        ChupTrongGame.chay(() -> game.pokemon.ThoiTiet.moThe = the);   // như ảnh: thẻ thời tiết vẫn đang mở
        boolean mo = cho(() -> game.render.Canvas.currentScreen instanceof game.pokemon.ManMenu);
        ngu(350);   // hết hoạt cảnh mở Menu (vòng cung bung ra) rồi mới chạm ô
        ve();
        return mo;
    }

    /** Menu máy chủ (-30) tới như gói thật, rồi vài khung (MenuNguCanh.tiepQuan). */
    static Kq menuMayChu(final int npcId, final String... muc) throws Exception {
        ChupTrongGame.chay(() -> game.render.Canvas.gameScr.onMenuOption(npcId, (byte) 0, muc));
        ve();
        ve();
        cho(() -> game.render.Canvas.currentDialog instanceof game.pokemon.MenuNguCanh || !game.pokemon.MenuNguCanh.bat());
        return doc();
    }

    static final String[] MENU_KHAC = {"Mở Túi Đồ", "Bỏ đồ vào túi", "Mở Kho", "Shop Đặc Biệt", "Tắt lời mời", "Bật tự động bán đồ thường",
        "Bật tự động nghiền bột thường", "Bãi luyện tập", "Bật auto về Map Up"};

    /** Từng ô Menu cá nhân -> đúng màn / thẻ / gói. */
    static void kiemOMenu(game.model.Actor mt, boolean the, String canh) throws Exception {
        String[] ten = (String[]) tinh(game.pokemon.ManMenu.class, "TEN");
        for (int i = 0; i < ten.length; i++) {
            if (!moMenuHud(mt, the)) {
                kiem(false, co + " " + canh + ": nut Menu HUD khong mo Menu ca nhan");
                return;
            }
            xoaGoi();
            String vt = "menu-" + ten[i];
            int[] v = vungTen(vt);
            if (v != null) {
                int j = game.pokemon.VungCham.tim(v[0] + v[2] / 2, v[1] + v[3] / 2);
                kiem(j >= 0 && vt.equals(game.pokemon.VungCham.ten(j)), co + " " + canh + " Menu > " + ten[i] + ": tam o trung vung '"
                        + (j < 0 ? "-" : game.pokemon.VungCham.ten(j)) + "' (the thoi tiet " + java.util.Arrays.toString(game.pokemon.ThoiTiet.VUNG_THE)
                        + ", o " + java.util.Arrays.toString(v) + ")");
            }
            String nhan = co + " " + canh + " Menu > " + ten[i];
            kiem(v != null, nhan + ": co o tren Menu");
            if (v == null) continue;
            cham(v[0] + v[2] / 2, v[1] + v[3] / 2);
            ngu(150);
            Kq k = doc();
            if ("ManMenu".equals(k.man) && k.hop == null && k.lenhGui.isEmpty() && !k.tuDongBang && !k.caiDat) {
                // máy kiểm nặng (hai JVM song song): nhịp game lỡ cú chạm 90 ms - chạm lại một lần, ghi lại số lần
                soChamLai++;
                cham(v[0] + v[2] / 2, v[1] + v[3] / 2);
                ngu(250);
                k = doc();
            }
            soOMenu++;
            khongTenQuai(k, nhan);
            switch (ten[i]) {
                case "Pokémon":
                    kiem("ManNhanVat".equals(k.man), nhan + " -> man Pokemon (" + k + ")");
                    break;
                case "Pokédex":
                    kiem("ManPokedex".equals(k.man), nhan + " -> Pokedex (" + k + ")");
                    break;
                case "Đội/Bạn bè":
                    kiem("ManDoi".equals(k.man), nhan + " -> Doi / Ban be (" + k + ")");
                    break;
                case "Sự kiện": {
                    if (k.coGoi(-76)) {
                        // client cũ (<= 0.9.16): ô gửi -76/-10000, máy chủ trả menu "Khác" -> ghi lại thẻ như ảnh chủ dự án
                        Kq kx = menuMayChu(-516, MENU_KHAC);
                        kiem(false, nhan + ": gui -76/-10000 (menu Khac) -> the '" + kx.tieu + "' " + java.util.Arrays.toString(kx.lenh));
                        khongTenQuai(kx, nhan + " (menu Khac)");
                    }
                    kiem("MenuNguCanh".equals(k.hop) && "Sự kiện".equals(k.tieu), nhan + " -> the 'Su kien' (" + k + ")");
                    kiem(k.lenh != null && k.lenh.length > 0 && "Mùa: Hoàng hôn Halloween".equals(k.lenh[0]), nhan + ": dong dau = mua dang dien ra ("
                            + (k.lenh == null ? null : java.util.Arrays.toString(k.lenh)) + ")");
                    boolean coKhac = false;
                    for (int j = 0; k.lenh != null && j < k.lenh.length; j++) {
                        for (String x : MENU_KHAC) if (x.equals(k.lenh[j])) coKhac = true;
                    }
                    kiem(!coKhac && !k.coGoi(-76), nhan + ": khong con menu 'Khac' (Mo Tui Do / Mo Kho...) / goi -76 (" + k + ")");
                    // chạm "Quà hoạt động hôm nay" -> -527/0/6; máy chủ trả menu -> thẻ vẫn tiêu đề "Sự kiện"
                    int hang = -1;
                    for (int j = 0; k.lenh != null && j < k.lenh.length; j++) if ("Quà hoạt động hôm nay".equals(k.lenh[j])) hang = j;
                    kiem(hang >= 0, nhan + ": co hang 'Qua hoat dong hom nay'");
                    if (hang >= 0 && hang < game.pokemon.MenuNguCanh.TOI_DA) {
                        xoaGoi();
                        kiem(chamVung("lenh-" + hang), nhan + ": cham hang Qua hoat dong");
                        Kq k2 = doc();
                        kiem(k2.coMenuOption(-527, 0, game.render.screen.GameScr.mucSoTay("Quà hoạt động hôm nay")),
                                nhan + " > Qua hoat dong hom nay -> goi -527/0/6 (" + k2 + ")");
                        Kq k3 = menuMayChu(-527, "Nhận quà điểm danh", "Đóng");
                        kiem("Sự kiện".equals(k3.tieu), nhan + " > Qua hoat dong: menu may chu tieu de 'Su kien' (" + k3 + ")");
                        khongTenQuai(k3, nhan + " > menu may chu");
                    }
                    break;
                }
                case "Hệ sinh thái":
                    kiem(!k.lenhGui.isEmpty() && k.hop == null, nhan + " -> gui yeu cau he sinh thai (" + k + ")");
                    break;
                case "Cửa hàng":
                    kiem("ManCuaHang".equals(k.man), nhan + " -> Cua hang (" + k + ")");
                    break;
                case "Thư/Quà":
                    kiem("MenuNguCanh".equals(k.hop) && "Thư / Quà".equals(k.tieu), nhan + " -> the 'Thu / Qua' (" + k + ")");
                    break;
                case "Xếp hạng":
                    kiem("ManXepHang".equals(k.man), nhan + " -> Xep hang (" + k + ")");
                    break;
                case "Cài đặt":
                    kiem(k.caiDat || k.hop != null && !"MenuNguCanh".equals(k.hop) || !"the_gioi".equals(k.man) && !"ManMenu".equals(k.man),
                            nhan + " -> Cai dat (" + k + ")");
                    break;
                case "Tiện ích & PK":
                    kiem("PauseMenu".equals(k.man), nhan + " -> menu chu Tien ich & PK (" + k + ")");
                    break;
                case "Tự động đánh":
                    kiem(k.tuDongBang, nhan + " -> bang Tu dong danh (" + k + ")");
                    break;
                case "Nhiệm vụ của tôi": {
                    kiem(k.coMenuOption(-527, 0, 8), nhan + " -> goi -527/0/8 (" + k + ")");
                    Kq k3 = menuMayChu(-527, "Nhiệm vụ A", "Nhiệm vụ B");
                    kiem("Nhiệm vụ của tôi".equals(k3.tieu), nhan + ": menu may chu tieu de 'Nhiem vu cua toi' (" + k3 + ")");
                    break;
                }
                case "Sổ tay huấn luyện": {
                    kiem("MenuNguCanh".equals(k.hop) && "Sổ tay huấn luyện".equals(k.tieu)
                            && k.lenh != null && k.lenh.length == game.render.screen.GameScr.SO_TAY_MUC.length + 1
                            && java.util.Arrays.equals(java.util.Arrays.copyOf(k.lenh, k.lenh.length - 1), game.render.screen.GameScr.SO_TAY_MUC)
                            && game.render.screen.GameScr.SO_TAY_VE_LANG.equals(k.lenh[k.lenh.length - 1]), nhan + " -> the So tay (" + k + ")");
                    xoaGoi();
                    kiem(chamVung("lenh-0"), nhan + ": cham muc 0");
                    Kq k2 = doc();
                    kiem(k2.coMenuOption(-527, 0, 0), nhan + " > muc 0 -> goi -527/0/0 (" + k2 + ")");
                    Kq k3 = menuMayChu(-527, "Năng lượng", "Tinh thông");
                    kiem("Sổ tay huấn luyện".equals(k3.tieu), nhan + " > muc 0: menu may chu tieu de 'So tay huan luyen' (" + k3 + ")");
                    break;
                }
                default:
                    kiem(false, nhan + ": o Menu moi chua co dich trong KiemChamHud.kiemOMenu (" + k + ")");
            }
            BANG_DICH.put("Menu > " + ten[i] + " | " + canh, k.toString().replaceAll(" goi=\\[\\]", ""));
        }
    }

    /** Menu "Khác" của máy chủ (-10000) ở Tiện ích & PK › Bản thân › Kho & tiện ích; menu máy chủ lạc / NPC. */
    static void kiemMenuMayChuTieuDe(String canh) throws Exception {
        // a) Kho & tiện ích (đường của menu "Khác" cũ) - nhắm Ekans
        sach(quai, true);
        xoaGoi();
        ChupTrongGame.chay(() -> {
            try {
                game.render.Canvas.gameScr.mainChar.hp = Math.max(1, game.render.Canvas.gameScr.mainChar.hp);
                java.lang.reflect.Method m = game.pokemon.ManMenu.class.getDeclaredMethod("lam", int.class);
                m.setAccessible(true);
                m.invoke(null, 9);   // Tiện ích & PK
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        Object p = game.render.Canvas.currentScreen;
        kiem(p instanceof game.render.screen.PauseMenu, co + " " + canh + ": Tien ich & PK mo PauseMenu");
        if (p instanceof game.render.screen.PauseMenu) {
            String[][] ten = (String[][]) tinh2(p, "name");
            int muc = -1;
            for (int j = 1; j < ten[2].length; j++) if ("Kho & tiện ích".equals(ten[2][j])) muc = j - 1;
            kiem(muc >= 0, co + " " + canh + ": Ban than co muc 'Kho & tien ich'");
            java.lang.reflect.Method an = p.getClass().getDeclaredMethod("an", int.class, int.class);
            an.setAccessible(true);
            kiem(muc >= 0 && !(Boolean) an.invoke(p, 2, muc), co + " " + canh + ": 'Kho & tien ich' hien o menu chu gon (loi vao duy nhat cua menu Khac)");
            final int mm = muc;
            ChupTrongGame.chay(() -> {
                try {
                    Field fi = p.getClass().getDeclaredField("index");
                    fi.setAccessible(true);
                    fi.setInt(p, 2);
                    Field fs = p.getClass().getDeclaredField("selected");
                    fs.setAccessible(true);
                    java.lang.reflect.Method vt = p.getClass().getDeclaredMethod("tinhHien");
                    vt.setAccessible(true);
                    vt.invoke(p);
                    java.lang.reflect.Method vtr = p.getClass().getDeclaredMethod("viTri", int.class);
                    vtr.setAccessible(true);
                    fs.setInt(p, (Integer) vtr.invoke(p, mm));
                    java.lang.reflect.Method ds = p.getClass().getDeclaredMethod("doSelect");
                    ds.setAccessible(true);
                    ds.invoke(p);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
            Kq k = doc();
            kiem(k.coGoi(-76), co + " " + canh + ": Kho & tien ich -> goi -76 (-10000) (" + k + ")");
            Kq k3 = menuMayChu(-516, MENU_KHAC);
            kiem("Kho & tiện ích".equals(k3.tieu), co + " " + canh + ": menu Khac may chu tieu de 'Kho & tien ich' (" + k3 + ")");
            khongTenQuai(k3, co + " " + canh + " Kho & tien ich");
            BANG_DICH.put("Tien ich & PK > Ban than > Kho & tien ich | " + canh, k3.toString());
        }
        // b) menu máy chủ không ai hỏi (vd hộp vật phẩm) khi đang nhắm Ekans: không tiêu đề "Ekans"
        sach(quai, true);
        Kq k4 = menuMayChu(-516, MENU_KHAC);
        kiem(k4.tieu == null || !k4.tieu.equals(tenQuai()), co + " " + canh + ": menu may chu khi nham quai khong lay ten quai (" + k4 + ")");
        // c) chạm NPC -> menu máy chủ của NPC: tiêu đề = tên NPC
        sach(null, false);
        ChupTrongGame.chay(() -> {
            game.render.screen.GameScr g = game.render.Canvas.gameScr;
            npc.type = 9;
            npc.name = "Kecleon";
            if (!g.actors.contains(npc)) g.actors.addElement(npc);
            game.pokemon.MenuNguCanh.choTieuDe("Sự kiện");   // chờ cũ còn hạn: chạm NPC phải xoá
        });
        final boolean[] trung = new boolean[1];
        for (int lan = 0; lan < 3 && !trung[0]; lan++) {
            ve();
            ChupTrongGame.chay(() -> {
                game.render.screen.GameScr g = game.render.Canvas.gameScr;
                game.model.Actor a = game.pokemon.HanhDong.actorTai(npc.x, npc.y - 4);
                trung[0] = a == npc && game.pokemon.HanhDong.chamThe(g, npc.x, npc.y - 4);
            });
        }
        kiem(trung[0], co + " " + canh + ": cham trung NPC");
        ngu(600);
        ChupTrongGame.chay(() -> {
            // việc tại chỗ của NPC giả (menu chữ / hộp) không phải đích kiểm: đóng, giữ mục tiêu NPC
            game.render.Canvas.endDlg();
            game.render.Canvas.menu.showMenu = false;
            game.render.Canvas.gameScr.focusedActor = npc;
        });
        kiem(game.pokemon.MenuNguCanh.layChoKiem() == null, co + " " + canh + ": cham NPC xoa tieu de cho");
        Kq k5 = menuMayChu(-24, "Nói chuyện", "Cửa hàng");
        kiem("Kecleon".equals(k5.tieu), co + " " + canh + ": cham NPC -> menu may chu tieu de ten NPC (" + k5 + ")");
        ChupTrongGame.chay(() -> game.render.Canvas.gameScr.actors.removeElement(npc));
    }

    static Object tinh2(Object o, String ten) throws Exception {
        Field f = o.getClass().getDeclaredField(ten);
        f.setAccessible(true);
        return f.get(o);
    }

    static final String[] NUT_HUD = {"hud-the", "hud-tren-0", "hud-tren-1", "hud-tren-2", "hud-tren-3", "hud-cai-dat", "hud-thoi-tiet",
        "hud-chat", "hud-tu-dong", "hud-tu-dong-cai-dat", "hud-thuoc-hp", "hud-thuoc-pp", "hud-danh", "hud-chieu0", "hud-chieu1",
        "hud-chieu2", "hud-chieu3", "hud-doi-bo", "hud-ngu-canh", "hud-nem-bong"};

    /** Mọi nút HUD + ô Menu ở cỡ đang đặt: không mục tiêu / Ekans + thẻ thời tiết / NPC. */
    static void kiemDichNut() throws Exception {
        dungCanh(true);
        datThoiTiet();   // sau dungCanh (ChupTrongGame.moMan xoá thời tiết giả)
        ChupTrongGame.chay(() -> {
            // quái hoang Ekans như ảnh chủ dự án
            if (game.render.Res.monsterTemplates != null && game.render.Res.monsterTemplates[quai.monster_type] != null) {
                game.render.Res.monsterTemplates[quai.monster_type].name = "Ekans";
            }
            if (npc == null) {
                npc = new game.model.NPC();
                try {
                    Field f = game.model.NPC.class.getDeclaredField("img");
                    f.setAccessible(true);
                    f.set(npc, new javax.microedition.lcdui.Image[2]);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            game.render.screen.GameScr g = game.render.Canvas.gameScr;
            npc.type = 9;
            npc.name = "Kecleon";
            npc.catagory = 2;
            npc.ID = 77;
            npc.x = (short) (g.mainChar.x - 20);
            npc.y = (short) (g.mainChar.y + 4);
        });
        kiem("Ekans".equals(tenQuai()), co + ": quai thu ten Ekans (" + tenQuai() + ")");
        Object[][] canh = {{null, Boolean.FALSE, "khong-muc-tieu"}, {quai, Boolean.TRUE, "nham-Ekans+the-thoi-tiet"}, {quai, Boolean.FALSE, "nham-Ekans"},
            {npc, Boolean.FALSE, "nham-NPC"}};
        for (Object[] c : canh) {
            final game.model.Actor mt = (game.model.Actor) c[0];
            boolean the = (Boolean) c[1];
            String tc = (String) c[2];
            if (mt == npc) {
                ChupTrongGame.chay(() -> {
                    if (!game.render.Canvas.gameScr.actors.contains(npc)) game.render.Canvas.gameScr.actors.addElement(npc);
                });
            }
            int hien = 0;
            for (String n : NUT_HUD) {
                boolean h = kiemNut(n, mt, the, tc);
                if (h) hien++;
                if ("hud-thoi-tiet".equals(n) || "hud-tren-3".equals(n) || "hud-cai-dat".equals(n)) {
                    kiem(h, co + " " + tc + ": nut " + n + " hien");
                }
            }
            kiem(hien >= 15, co + " " + tc + ": it nhat 15 nut HUD hien (" + hien + ")");
            if (mt != npc) kiemOMenu(mt, the, tc);
            if (mt == npc) {
                ChupTrongGame.chay(() -> game.render.Canvas.gameScr.actors.removeElement(npc));
            }
        }
        kiemMenuMayChuTieuDe("Ekans");
        sach(null, false);
        ChupTrongGame.chay(() -> game.pokemon.ThoiTiet.moThe = false);
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
        if (System.getProperty("chup.j2me") != null) {
            // 0.9.17: như ChupTrongGame -Dchup.j2me - vẽ như máy J2ME (font ảnh, khối phẳng)
            htbb.pc.GiaoDienHD.bat = false;
            htbb.pc.ChuHD.bat = false;
            game.pokemon.ChuUI.bo = null;
        }
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
            kiemHoSo(false);
            if ("pc720".equals(co) || "dt20x9".equals(co)) {
                kiemHoSo(true);
            }
            if (System.getProperty("kiem.dich") == null || !"0".equals(System.getProperty("kiem.dich"))) {
                kiemDichNut();
            }
        }
        // bảng đích (xem bằng mắt / báo cáo): nút | cảnh -> kết quả, cỡ cuối cùng đã chạy
        java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(new java.io.File(ra, "dich_nut.txt")), "UTF-8"));
        for (java.util.Map.Entry<String, String> e : BANG_DICH.entrySet()) pw.println(e.getKey() + " -> " + e.getValue());
        pw.close();
        System.out.println("KiemChamHud dich nut: " + soNutDich + " lan cham nut HUD, " + soOMenu + " lan cham o Menu, " + soCheThe
                + " nut duoi the thoi tiet, " + soChamLai + " o cham lai -> " + new java.io.File(ra, "dich_nut.txt"));
        System.out.println("KiemChamHud: " + dat + " dat, " + hong + " hong");
        System.exit(hong == 0 ? 0 : 1);
    }
}
