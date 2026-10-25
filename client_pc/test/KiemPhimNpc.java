import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.microedition.lcdui.NenPc;

/**
 * KIỂM NHẮM NPC (0.9.19, lỗi người chơi J2ME 0.9.18 "sao chọn NPC lại tấn công thế này"), headless, máy chủ giả ghi gói:
 * bản đồ làng thật (Tilemap 0, NPC tĩnh), nhân vật đứng dưới NPC quay lên.
 *  1) CHỌN NPC chỉ nhắm: tự nhắm theo hướng / phím mềm phải - Tab PC (đổi mục tiêu) -> mục tiêu NPC, không đánh, không nói;
 *  2) phím BẮN (-5) / phím 5 khi nhắm NPC (bấm nhanh và giữ 400 ms) -> đúng MỘT lần nói chuyện (một gói 23), không gói
 *     đánh (6 / 9 / 106 / -113), nhân vật không vào thế đánh (Char.A_ATTACK);
 *  3) phím chiêu 1 3 7 9 khi nhắm NPC: không bao giờ đánh NPC - J2ME màn nhỏ (thanh "Giao tiếp") = nói chuyện, HUD có
 *     nút chiêu riêng = nhắm quái gần nhất (một đòn vào quái), không có quái thì không đánh;
 *  4) mỗi lần bấm xử lý ĐÚNG MỘT LẦN (vòng game bước cố định 0.9.18) - nói chuyện NPC và một đòn vào quái, kể cả máy vẽ
 *     chậm 45 ms / khung;
 *  5) chạm NPC (và chạm lại) / nút đánh HUD khi nhắm NPC (J2ME cảm ứng, PC), chuột trái / chuột phải / Enter / Space (PC)
 *     -> nói chuyện đúng một lần, không đánh;
 *  6) Tự động đánh (autoFight + tự đánh; TuDong) không bao giờ nhắm / nói / đánh NPC.
 * Mỗi đường chạy với "Ngắm thủ công" tắt / bật (lỗi gốc: bật thì mọi lối nói chuyện thành phát bắn NgamTay -113 theo hướng
 * đang quay = đánh vào NPC) và máy nhanh / máy vẽ chậm (NenPc.giaLapVeMs 45). Đường: J2ME phím 240x320 + 320x240 (tiến
 * trình con -Dhtbb.khongCham), J2ME cảm ứng màn rộng 640x360 (HudRong, J2ME Loader trên máy tính bảng), PC 1280x720.
 *
 *   javac -cp client_pc/out/build/cls -sourcepath client_pc/test:server/src -d $O client_pc/test/ChupTrongGame.java \
 *     client_pc/test/KiemPhimNpc.java
 *   java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemPhimNpc <thư mục>
 * Mã thoát 1 = có lỗi. -Dkiem.duong=j2me240x320,j2me320x240,j2me640x360,pc720 chạy một phần.
 */
public class KiemPhimNpc {
    static int dat, hong;
    static String duong = "";
    static final List<Integer> LENH = new ArrayList<Integer>();
    static final List<byte[]> GOI = new ArrayList<byte[]>();
    /** NPC cửa hàng: nói chuyện = requestNPCInfo (gói 23) - đếm được từng lần. */
    static final int NPC_LOAI = 9;

    static void kiem(boolean ok, String ten) {
        if (ok) dat++;
        else {
            hong++;
            System.out.println("HONG [" + duong + "]: " + ten);
        }
    }

    static void ngu(long ms) throws Exception {
        Thread.sleep(ms);
    }

    static boolean cho(java.util.function.BooleanSupplier f, long ms) throws Exception {
        long het = System.currentTimeMillis() + ms;
        while (System.currentTimeMillis() < het) {
            if (f.getAsBoolean()) return true;
            ngu(30);
        }
        return f.getAsBoolean();
    }

    static void datTinh(Class<?> c, String ten, Object gt) throws Exception {
        Field f = c.getDeclaredField(ten);
        f.setAccessible(true);
        f.set(null, gt);
    }

    static game.render.screen.GameScr gs() {
        return game.render.Canvas.gameScr;
    }

    // ------------------------------------------------------------------ gói

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

    static String dsGoi() {
        synchronized (LENH) {
            return LENH.toString();
        }
    }

    /** Số gói đánh (6 người, 9 quái, 106 nhiều quái, -113 phát bắn ngắm tay). */
    static int demDanh() {
        return demLenh(6) + demLenh(9) + demLenh(106) + demLenh(-113);
    }

    /** Có gói đánh nhắm vào actor id (6 / 9: short id đầu gói; 106: danh sách sau byte chiêu + byte số). */
    static boolean danhVao(int id) {
        synchronized (LENH) {
            for (int i = 0; i < LENH.size(); i++) {
                int c = LENH.get(i);
                byte[] d = GOI.get(i);
                if ((c == 6 || c == 9) && d.length >= 2 && (short) (((d[0] & 0xFF) << 8) | (d[1] & 0xFF)) == id) return true;
                if (c == 106 && d.length >= 2) {
                    for (int k = 2; k + 1 < d.length; k += 2) {
                        if ((short) (((d[k] & 0xFF) << 8) | (d[k + 1] & 0xFF)) == id) return true;
                    }
                }
            }
        }
        return false;
    }

    // ------------------------------------------------------------------ cảnh

    static game.model.Actor npc;
    static game.model.Monster quai;
    static boolean coCham, hudRong, pc;

    static boolean laNpc(game.model.Actor a) {
        return a != null && (a.catagory == 2 || a.isNPC());
    }

    static String moTa(game.model.Actor a) {
        return a == null ? "null" : a.getClass().getSimpleName() + "#" + a.ID + "(" + a.x + "," + a.y + ")";
    }

    /** Lần đầu: chiêu đã học, bảng chiêu, ô nhanh đủ 5 chiêu; NPC tĩnh của làng (mọi NPC trùng chỗ = loại NPC_LOAI). */
    static void chuanBi() throws Exception {
        ChupTrongGame.chay(() -> {
            ChupTrongGame.moMan("the_gioi");
            game.render.screen.GameScr g = gs();
            game.model.MainChar mc = g.mainChar;
            game.model.Char.skillLevelLearnt = new byte[]{1, 2, 1, 1, 0, 0, 1, 1, -1};
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
            int[] chieu = {1, 2, 0, 3, 6};
            for (int i = 0; i < 5; i++) game.model.MainChar.quickSlot[0][i].setIsSkill(chieu[i], mc.clazz, false);
            game.render.screen.GameScr.indexTabSlot = 0;
            for (int i = 0; i < g.actors.size(); i++) {
                game.model.Actor ac = (game.model.Actor) g.actors.elementAt(i);
                if (ac instanceof game.model.NPC && (npc == null || ac.x == 552 && ac.y == 168)) npc = ac;
            }
            for (int i = 0; i < g.actors.size(); i++) {
                game.model.Actor ac = (game.model.Actor) g.actors.elementAt(i);
                if (ac instanceof game.model.NPC && ac.x == npc.x && ac.y == npc.y) ((game.model.NPC) ac).type = NPC_LOAI;
            }
            quai = new game.model.Monster();
            quai.ID = 3001;
            quai.catagory = 1;
            quai.setType((short) 0);
            quai.level = 3;
        });
    }

    /**
     * Cảnh sạch: nhân vật 30 px dưới NPC quay lên, không mục tiêu, không menu / hộp thoại; coQuai = quái hoang cách NPC
     * 20 px; ngam = "Ngắm thủ công" bật (máy chủ hỗ trợ -113).
     */
    static void canh(final boolean coQuai, final boolean ngam) throws Exception {
        ChupTrongGame.chay(() -> {
            game.render.Canvas.endDlg();
            game.render.Canvas.menu.showMenu = false;
            game.pokemon.HopThoaiDuoi.tat();
            game.render.screen.GameScr g = gs();
            game.model.MainChar mc = g.mainChar;
            game.render.screen.GameScr.autoFight = false;
            game.render.screen.GameScr.saveAutoFight = false;
            game.render.screen.TuDong.dang = false;
            game.model.Tilemap.lv = 0;
            game.pokemon.NgamTay.mayChu = ngam;
            game.pokemon.NgamTay.pvp = false;
            game.pokemon.NgamTay.datThuCong(ngam);
            game.pokemon.NgamTay.huy();
            mc.x = npc.x;
            mc.y = (short) (npc.y + 30);
            mc.xTo = mc.x;
            mc.yTo = mc.y;
            mc.dir = 1;
            mc.state = 0;
            mc.posTransRoad = null;
            mc.mp = mc.maxmp;
            for (int i = 0; i < mc.timeLastUseSkills.length; i++) {
                mc.timeLastUseSkills[i] = 0;
                mc.coolDownSkill[i] = 0;
            }
            g.duoiMucTieu = null;
            g.actors.removeElement(quai);
            if (coQuai) {
                quai.maxhp = 500;
                quai.hp = 500;
                quai.setRealHP(500);
                quai.state = 0;
                quai.x = (short) (npc.x + 20);
                quai.y = (short) (npc.y + 4);
                quai.xFirst = quai.x;
                quai.yFirst = quai.y;
                g.actors.addElement(quai);
            }
            g.focusedActor = null;
            game.pokemon.HanhDong.boChon();
            game.render.Canvas.clearKeyPressed();
            game.render.Canvas.clearKeyHold();
        });
    }

    static boolean coMenu() {
        return game.render.Canvas.menu.showMenu || game.render.Canvas.currentDialog != null
                || game.render.Canvas.currentScreen != game.render.Canvas.gameScr;
    }

    /** Nhắm actor như người chơi chọn (giữ mục tiêu). */
    static void nham(final game.model.Actor a) throws Exception {
        ChupTrongGame.chay(() -> {
            try {
                gs().focusedActor = a;
                datTinh(game.pokemon.HanhDong.class, "chon", a);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

    static void phim(int ma) throws Exception {
        NenPc.phim(0, ma);
        ngu(90);
        NenPc.phim(2, ma);
    }

    /** Sau một thao tác: chờ phản hồi rồi thêm 500 ms (lần xử lý thứ hai nếu có cũng phải lọt vào đây). */
    static void choXong() throws Exception {
        cho(() -> demLenh(23) > 0 || demDanh() > 0, 1200);
        ngu(500);
    }

    /** Sau một thao tác "nói chuyện với NPC": đúng một gói 23, không gói đánh, không thế đánh. */
    static void kiemNoi(String ten) {
        kiem(demLenh(23) == 1, ten + " -> dung 1 lan noi chuyen (goi 23: " + demLenh(23) + ", goi " + dsGoi() + ")");
        kiem(demDanh() == 0, ten + " -> khong goi danh (goi " + dsGoi() + ")");
        kiem(gs().mainChar.state != game.model.Char.A_ATTACK, ten + " -> nhan vat khong vao the danh (state " + gs().mainChar.state + ")");
    }

    // ------------------------------------------------------------------ chạm

    /** Toạ độ logic trên màn trúng actor a (HanhDong.actorTai = hình chạm thật, cả thu phóng thế giới PC), ngoài nút HUD. */
    static int[] diemTren(final game.model.Actor a) throws Exception {
        final int[][] ra = new int[1][];
        ChupTrongGame.chay(() -> {
            ChupTrongGame.veThang();
            int best = Integer.MAX_VALUE;
            for (int y = 0; y < NenPc.cao; y += 2) {
                for (int x = 0; x < NenPc.rong; x += 2) {
                    game.model.Actor t = game.pokemon.HanhDong.actorTai(game.pokemon.HanhDong.wx(x), game.pokemon.HanhDong.wy(y));
                    if (t == null || t.x != a.x || t.y != a.y || t.catagory != a.catagory) continue;   // bản đồ làng có NPC trùng chỗ
                    if (game.render.screen.HudRong.trungNut(x, y)) continue;
                    int dx = game.pokemon.HanhDong.wx(x) - a.x, dy = game.pokemon.HanhDong.wy(y) - (a.y - 12);
                    int d = dx * dx + dy * dy;
                    if (d < best) {
                        best = d;
                        ra[0] = new int[]{x, y};
                    }
                }
            }
        });
        return ra[0];
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

    static void cham(final int x, final int y) throws Exception {
        ChupTrongGame.chay(ChupTrongGame::veThang);
        ChupTrongGame.chay(() -> goi("pointerPressed", x, y));
        ngu(90);
        ChupTrongGame.chay(() -> goi("pointerReleased", x, y));
    }

    static void phimPc(final int vk) throws Exception {
        Method nh = htbb.pc.BanPhim.class.getDeclaredMethod("nhanPhim", int.class, int.class, boolean.class);
        Method th = htbb.pc.BanPhim.class.getDeclaredMethod("thaPhim", int.class);
        nh.setAccessible(true);
        th.setAccessible(true);
        ChupTrongGame.chay(() -> {
            try {
                nh.invoke(null, vk, 0, false);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        ngu(90);
        ChupTrongGame.chay(() -> {
            try {
                th.invoke(null, vk);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

    // ------------------------------------------------------------------ các phần kiểm

    /** 1) chọn NPC: tự nhắm theo hướng nhìn + phím mềm phải (J2ME) / Tab (PC) chỉ đổi mục tiêu. */
    static void kiemChon(boolean ngam) throws Exception {
        canh(false, ngam);
        xoaGoi();
        kiem(cho(() -> laNpc(gs().focusedActor), 1500), "dung duoi NPC quay len -> tu nham NPC (" + moTa(gs().focusedActor) + ")");
        if (pc) {
            phimPc(htbb.pc.BanPhim.PHIM[htbb.pc.BanPhim.MUC_TIEU]);
        } else {
            phim(-7);
        }
        ngu(500);
        kiem(laNpc(gs().focusedActor), "doi muc tieu (" + (pc ? "Tab" : "phim mem phai") + ") -> van nham NPC (" + moTa(gs().focusedActor) + ")");
        kiem(demDanh() == 0 && demLenh(23) == 0, "chon NPC chi nham: khong danh, khong noi (goi " + dsGoi() + ")");
        kiem(gs().mainChar.state != game.model.Char.A_ATTACK, "chon NPC: nhan vat khong vao the danh");
        kiem(!coMenu(), "chon NPC: khong mo menu / hop thoai");
    }

    /** 2) + 4) phím bắn / phím 5 khi nhắm NPC: nói chuyện đúng một lần; một lần bấm vào quái = một đòn. */
    static void kiemBan(boolean ngam) throws Exception {
        for (int ma : new int[]{-5, 53}) {
            String ten = ma == -5 ? "BAN (-5)" : "5";
            canh(false, ngam);
            nham(npc);
            xoaGoi();
            phim(ma);
            choXong();
            kiemNoi("phim " + ten + " khi nham NPC");
            kiem(laNpc(gs().focusedActor), "phim " + ten + " khi nham NPC -> van nham NPC (" + moTa(gs().focusedActor) + ")");
            canh(false, ngam);
            nham(npc);
            xoaGoi();
            NenPc.phim(0, ma);
            ngu(400);
            NenPc.phim(2, ma);
            choXong();
            kiemNoi("giu phim " + ten + " 400 ms khi nham NPC");
        }
        canh(true, ngam);
        nham(quai);
        xoaGoi();
        phim(-5);
        choXong();
        kiem(demDanh() == 1, "phim BAN khi nham quai -> dung 1 goi danh (goi " + dsGoi() + ")");
        kiem(demLenh(23) == 0, "phim BAN khi nham quai -> khong noi chuyen");
    }

    /** 3) phím chiêu khi nhắm NPC. */
    static void kiemChieu(boolean ngam) throws Exception {
        boolean chieuRieng = game.pokemon.HanhDong.nutChieuRieng();
        for (int k : new int[]{49, 51, 55, 57}) {
            String ten = "phim chieu " + (k - 48);
            canh(false, ngam);
            nham(npc);
            xoaGoi();
            phim(k);
            choXong();
            kiem(demDanh() == 0, ten + " khi nham NPC (khong quai) -> khong danh (goi " + dsGoi() + ")");
            kiem(gs().mainChar.state != game.model.Char.A_ATTACK, ten + " khi nham NPC (khong quai) -> khong the danh");
            if (!chieuRieng) {
                kiem(demLenh(23) == 1, "J2ME man nho: " + ten + " khi nham NPC = Giao tiep (goi 23: " + demLenh(23) + ")");
            } else {
                kiem(demLenh(23) == 0, "HUD chieu rieng: " + ten + " khi nham NPC khong mo NPC (goi 23: " + demLenh(23) + ")");
            }
            canh(true, ngam);
            nham(npc);
            xoaGoi();
            phim(k);
            choXong();
            kiem(!danhVao(npc.ID), ten + " (co quai) khong danh NPC (goi " + dsGoi() + ")");
            if (!chieuRieng) {
                kiem(demDanh() == 0 && demLenh(23) == 1, "J2ME man nho: " + ten + " (co quai) = Giao tiep, khong danh (goi " + dsGoi() + ")");
            } else {
                kiem(gs().focusedActor == quai, "HUD chieu rieng: " + ten + " -> nham quai gan nhat (" + moTa(gs().focusedActor) + ")");
                kiem(demDanh() == 1 && (ngam || danhVao(quai.ID)), "HUD chieu rieng: " + ten + " -> dung 1 don vao quai (goi " + dsGoi() + ")");
                kiem(demLenh(23) == 0, "HUD chieu rieng: " + ten + " khong mo NPC");
            }
        }
    }

    /** 5) chạm / chuột / Enter / Space. */
    static void kiemCham(boolean ngam) throws Exception {
        if (!coCham) return;
        String cach = pc ? "chuot trai" : "cham";
        canh(false, ngam);
        int[] p = diemTren(npc);
        kiem(p != null, "tim duoc diem cham tren NPC");
        if (p != null) {
            xoaGoi();
            cham(p[0], p[1]);
            choXong();
            kiemNoi(cach + " NPC");
            ChupTrongGame.chay(() -> {
                game.render.Canvas.endDlg();
                game.render.Canvas.menu.showMenu = false;
            });
            xoaGoi();
            cham(p[0], p[1]);
            choXong();
            kiemNoi(cach + " lai NPC dang nham");
        }
        if (hudRong) {
            canh(false, ngam);
            nham(npc);
            ChupTrongGame.chay(ChupTrongGame::veThang);
            int[] v = null;
            for (int i = 0; i < game.pokemon.VungCham.dem(); i++) {
                if ("hud-danh".equals(game.pokemon.VungCham.ten(i))) v = game.pokemon.VungCham.vungVe(i);
            }
            kiem(v != null, "co nut danh HUD (hud-danh)");
            if (v != null) {
                xoaGoi();
                cham(v[0] + v[2] / 2, v[1] + v[3] / 2);
                choXong();
                kiemNoi("nut danh HUD khi nham NPC");
            }
        }
        if (pc) {
            canh(false, ngam);
            final int[] q = diemTren(npc);
            if (q != null) {
                xoaGoi();
                ChupTrongGame.chay(() -> game.pokemon.HanhDong.chuotPhai(q[0], q[1]));
                choXong();
                kiemNoi("chuot phai NPC");
            }
            for (int vk : new int[]{KeyEvent.VK_ENTER, htbb.pc.BanPhim.PHIM[htbb.pc.BanPhim.DANH]}) {
                canh(false, ngam);
                nham(npc);
                xoaGoi();
                phimPc(vk);
                choXong();
                kiemNoi("phim PC " + KeyEvent.getKeyText(vk) + " khi nham NPC");
            }
        }
    }

    /** 6) Tự động đánh không bao giờ nhắm NPC. */
    static void kiemTuDong(boolean ngam) throws Exception {
        for (int c = 0; c < 2; c++) {
            final boolean coQuai = c == 1;
            canh(coQuai, ngam);
            ChupTrongGame.chay(() -> {
                game.model.Tilemap.lv = 1;   // làng (lv 0) tự tắt tự động
                game.render.Canvas.autoTab.isAutoFire = true;
                game.render.screen.GameScr.autoFight = true;
                gs().focusedActor = npc;      // vừa nhắm NPC thì bật tự động
            });
            xoaGoi();
            boolean nhamNpc = false;
            long dau = System.currentTimeMillis();
            while (System.currentTimeMillis() - dau < 2500) {
                ngu(40);
                if (System.currentTimeMillis() - dau > 1200 && laNpc(gs().focusedActor)) nhamNpc = true;   // vòng tự động 1 s / lần
            }
            ChupTrongGame.chay(() -> {
                game.render.screen.GameScr.autoFight = false;
                game.model.Tilemap.lv = 0;
            });
            String bt = coQuai ? " (co quai)" : " (khong quai)";
            kiem(!nhamNpc, "Tu dong danh" + bt + ": khong giu / nham NPC (" + moTa(gs().focusedActor) + ")");
            kiem(demLenh(23) == 0, "Tu dong danh" + bt + ": khong noi chuyen NPC (goi " + dsGoi() + ")");
            kiem(!danhVao(npc.ID), "Tu dong danh" + bt + ": khong danh NPC");
            if (!coQuai) {
                kiem(demDanh() == 0, "Tu dong danh (khong quai): khong goi danh (goi " + dsGoi() + ")");
            }
            kiem(!coMenu(), "Tu dong danh" + bt + ": khong mo menu NPC");
        }
        canh(false, ngam);
        final Object[] ra = new Object[1];
        ChupTrongGame.chay(() -> {
            game.render.screen.TuDong.dang = true;
            ra[0] = gs().findFocusActor();
            game.render.screen.TuDong.dang = false;
        });
        kiem(!laNpc((game.model.Actor) ra[0]), "TuDong dang chay: findFocusActor khong tra NPC (" + moTa((game.model.Actor) ra[0]) + ")");
    }

    static void chayDuong(String ten) throws Exception {
        for (int ve : new int[]{0, 45}) {
            for (int ng = 0; ng < 2; ng++) {
                boolean ngam = ng == 1;
                duong = ten + (ngam ? " ngam-thu-cong" : "") + (ve > 0 ? " ve" + ve + "ms" : "");
                NenPc.giaLapVeMs = ve;
                kiemChon(ngam);
                kiemBan(ngam);
                kiemChieu(ngam);
                kiemCham(ngam);
                if (ve == 0) kiemTuDong(ngam);
            }
        }
        NenPc.giaLapVeMs = 0;
        canh(false, false);
    }

    static void datCo(int W, int H, boolean thatJ2me) throws Exception {
        if (thatJ2me) {
            NenPc.rong = W;
            NenPc.cao = H;
            NenPc.heSo = 1;
            NenPc.thatW = W;
            NenPc.thatH = H;
            NenPc.hienX = 0;
            NenPc.hienY = 0;
            NenPc.hienW = W;
            NenPc.hienH = H;
        } else {
            NenPc.tinhKichThuoc(W, H);
        }
        ChupTrongGame.chay(() -> game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao));
        ngu(200);
        coCham = game.render.Canvas.instance.hasPointerEvents();
        hudRong = game.render.screen.HudRong.bat();
    }

    static boolean chon(String loc, String ten) {
        return loc == null || ("," + loc + ",").contains("," + ten + ",");
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        java.io.File ra = new java.io.File(a.length > 0 ? a[0] : "kiem_phim_npc");
        boolean khongCham = Boolean.getBoolean("htbb.khongCham");
        String loc = System.getProperty("kiem.duong");
        int maCon = 0;
        if (!khongCham && (chon(loc, "j2me240x320") || chon(loc, "j2me320x240"))) {
            // J2ME chỉ có phím: Canvas.hasPointerEvents đọc -Dhtbb.khongCham lúc nạp lớp -> tiến trình con
            String jv = System.getProperty("java.home") + java.io.File.separator + "bin" + java.io.File.separator + "java";
            List<String> lenh = new ArrayList<String>();
            lenh.add(jv);
            lenh.add("-Djava.awt.headless=true");
            lenh.add("-Dhtbb.khongCham=true");
            if (loc != null) lenh.add("-Dkiem.duong=" + loc);
            lenh.add("-cp");
            lenh.add(System.getProperty("java.class.path"));
            lenh.add("KiemPhimNpc");
            lenh.add(new java.io.File(ra, "phim").getPath());
            Process pr = new ProcessBuilder(lenh).inheritIO().start();
            maCon = pr.waitFor();
            if (loc != null && !chon(loc, "pc720") && !chon(loc, "j2me640x360")) System.exit(maCon);
        }
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
        chuanBi();
        int fpsPc = game.pokemon.NhipVe.fps;
        if (khongCham) {
            // máy J2ME chỉ có phím: vẽ như J2ME (phông ảnh, khối phẳng), một khung sau mỗi lượt nhịp
            htbb.pc.GiaoDienHD.bat = false;
            htbb.pc.ChuHD.bat = false;
            game.pokemon.ChuUI.bo = null;
            game.pokemon.NhipVe.fps = 0;
            pc = false;
            int[][] co = {{240, 320}, {320, 240}};
            for (int[] c : co) {
                String ten = "j2me" + c[0] + "x" + c[1];
                if (!chon(loc, ten)) continue;
                datCo(c[0], c[1], true);
                duong = ten;
                kiem(!coCham && !hudRong, "may chi co phim, khong HudRong");
                chayDuong(ten);
            }
        } else {
            if (chon(loc, "pc720")) {
                pc = true;
                game.pokemon.NhipVe.fps = fpsPc > 25 ? fpsPc : 60;
                datCo(1280, 720, false);
                duong = "pc720";
                kiem(coCham && hudRong, "PC: HudRong");
                chayDuong("pc720");
            }
            if (chon(loc, "j2me640x360")) {
                // J2ME cảm ứng màn rộng (J2ME Loader trên máy tính bảng, bàn phím ảo L / R / mũi tên / F): HudRong
                htbb.pc.GiaoDienHD.bat = false;
                htbb.pc.ChuHD.bat = false;
                game.pokemon.ChuUI.bo = null;
                game.pokemon.NhipVe.fps = 0;
                pc = false;
                datCo(640, 360, true);
                duong = "j2me640x360";
                kiem(coCham && hudRong, "J2ME cam ung man rong: HudRong");
                chayDuong("j2me640x360");
            }
        }
        System.out.println("KiemPhimNpc" + (khongCham ? " (J2ME chi phim)" : "") + ": " + dat + " dat, " + hong + " hong");
        System.exit(hong == 0 && maCon == 0 ? 0 : 1);
    }
}
