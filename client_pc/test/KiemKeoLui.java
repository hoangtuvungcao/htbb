import javax.microedition.lcdui.NenPc;

/**
 * KÉO LÙI KHI ĐÁNH TAY (0.9.18, chủ dự án: "đánh thường bằng tay thì nó lại dịch chuyển về vị trí ban đầu").
 *
 * Vòng game THẬT (Canvas.run, đường vẽ PC không cửa sổ) + MÁY CHỦ GIẢ cho gói đi (lệnh 4): kiểm tốc độ bằng đúng
 * real.TocDoDiChuyen của máy chủ (quỹ quãng đường, ngưỡng 50 px, tốc độ 5) - gói vượt quỹ thì máy chủ trả lệnh 4 vị trí
 * cũ cho chính mình (giao trên luồng riêng như luồng đọc mạng Session_ME) = client bị kéo về. Kịch bản mỗi đường (PC 60,
 * APK 60 + vsync, APK 30 + vsync, J2ME) x chi phí vẽ (0 / 30 / 60 ms): đi thẳng N nhịp -> quái ngay trước mặt, nhắm,
 * chạm nút đánh thường HUD -> chờ 1,5 s. Kiểm: có gói đánh (9), máy chủ KHÔNG từ chối gói đi nào, sau khi đánh nhân vật đứng yên
 * tại chỗ dừng (không lùi > 1 px, không đi tiếp), vị trí máy chủ = vị trí client; trong lúc đi không lùi lần nào.
 *
 *   java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemKeoLui [bao-cao]
 */
public class KiemKeoLui {
    static int dat, hong;

    static void kiem(boolean ok, String ten) {
        if (ok) {
            dat++;
        } else {
            hong++;
            System.out.println("HONG: " + ten);
        }
    }

    // ---------------------------------------------------------------- máy chủ giả (lệnh 4 như CharCapNhat.doPlayerMoveThan)
    static final Object KHOA = new Object();
    static int svX, svY, tuChoi, goiDi, goiDanh;
    static long quy = -1, svLuc;

    static void datMayChu(int x, int y) {
        synchronized (KHOA) {
            svX = x;
            svY = y;
            quy = -1;
            svLuc = System.currentTimeMillis();
            tuChoi = 0;
            goiDi = 0;
            goiDanh = 0;
        }
    }

    static void nhan(network.Message m) {
        int lenh = m.command;
        if (lenh == 9 || lenh == 106 || lenh == 6) {
            synchronized (KHOA) {
                goiDanh++;
            }
            return;
        }
        if (lenh != 4) {
            return;
        }
        byte[] d = m.getData();
        if (d == null || d.length < 4) {
            return;
        }
        int x = (d[0] & 255) << 8 | (d[1] & 255), y = (d[2] & 255) << 8 | (d[3] & 255);
        final int traX, traY;
        synchronized (KHOA) {
            goiDi++;
            if (x == svX && y == svY) {
                return;
            }
            long now = System.currentTimeMillis();
            long q = real.TocDoDiChuyen.nap(quy, now - svLuc, 5, 50);
            long con = real.TocDoDiChuyen.buoc(q, x - svX, y - svY, 50);
            if (con >= 0) {
                svX = x;
                svY = y;
                quy = con;
                svLuc = now;
                return;
            }
            quy = q;
            svLuc = now;
            tuChoi++;
            traX = svX;
            traY = svY;
        }
        // từ chối: máy chủ gửi lệnh 4 vị trí cũ cho chính mình (luồng đọc mạng gọi onActorMove)
        new Thread(new Runnable() {
            public void run() {
                game.render.screen.GameScr gs = game.render.Canvas.gameScr;
                gs.onActorMove((byte) 0, (short) 0, gs.mainChar.ID, (short) traX, (short) traY, (byte) 0, -1, (byte) 0, (byte) 0, true);
            }
        }).start();
    }

    static game.model.Monster quai;

    /** Vùng chạm nút đánh thường (đăng ký ở khung vẽ gần nhất). */
    static int[] vungDanh() throws Exception {
        final int[][] r = new int[1][];
        ChupTrongGame.chay(() -> {
            for (int i = 0; i < game.pokemon.VungCham.dem(); i++) {
                if ("hud-danh".equals(game.pokemon.VungCham.ten(i))) r[0] = game.pokemon.VungCham.vungVe(i);
            }
        });
        return r[0];
    }

    /** {tuChoi, goiDanh, xDanh, xSau1, xCuoi, lui khi di, svX, stateCuoi} */
    static int[] chay1(int fps, boolean vsync, int veMs, int nhipDi, final int khoang) throws Exception {
        final game.render.screen.GameScr gs = game.render.Canvas.gameScr;
        final game.model.MainChar mc = gs.mainChar;
        game.pokemon.NhipVe.fps = fps;
        NenPc.giaLapVsync = vsync;
        NenPc.giaLapVeMs = veMs;
        ChupTrongGame.chay(() -> {
            gs.actors.removeElement(quai);
            gs.focusedActor = null;
            mc.x = 200;
            mc.y = 200;
            mc.xTo = mc.x;
            mc.yTo = mc.y;
            mc.state = 0;
            mc.stop = true;
            mc.mp = mc.maxmp;
            for (int i = 0; i < mc.timeLastUseSkills.length; i++) {
                mc.timeLastUseSkills[i] = 0;
                mc.coolDownSkill[i] = 0;
            }
            datMayChu(200, 200);
        });
        Thread.sleep(600);
        ChupTrongGame.chay(() -> mc.moveTo((short) 2000, mc.y));
        // đi nhipDi nhịp, theo dõi lùi (đọc giữa hai khung)
        final int[] lui = {0};
        final int[] truoc = {200};
        long het = System.currentTimeMillis() + nhipDi * 40L;
        while (System.currentTimeMillis() < het) {
            ChupTrongGame.chay(() -> {
                if (mc.x < truoc[0]) lui[0]++;
                truoc[0] = mc.x;
            });
            Thread.sleep(20);
        }
        final int[] xDanh = new int[1];
        ChupTrongGame.chay(() -> {
            quai.maxhp = 500;
            quai.hp = 500;
            quai.setRealHP(500);
            quai.state = 0;
            quai.x = (short) (mc.x + khoang);
            quai.y = mc.y;
            quai.xFirst = quai.x;
            quai.yFirst = quai.y;
            if (!gs.actors.contains(quai)) gs.actors.addElement(quai);
            gs.focusedActor = quai;
            game.pokemon.HanhDong.boChon();
            gs.focusedActor = quai;
            xDanh[0] = mc.x;
        });
        // chạm nút đánh thường của HUD (đường thật: VungCham -> HudRong.danh -> phím 5 trong nhịp logic)
        final int[] v = vungDanh();
        if (v == null) {
            throw new IllegalStateException("khong thay vung hud-danh");
        }
        ChupTrongGame.chay(() -> KiemChamHud.goi("pointerPressed", v[0] + v[2] / 2, v[1] + v[3] / 2));
        Thread.sleep(90);
        ChupTrongGame.chay(() -> KiemChamHud.goi("pointerReleased", v[0] + v[2] / 2, v[1] + v[3] / 2));
        Thread.sleep(300);
        final int[] x1 = new int[1];
        ChupTrongGame.chay(() -> x1[0] = mc.x);
        Thread.sleep(1200);
        final int[] cuoi = new int[2];
        ChupTrongGame.chay(() -> {
            cuoi[0] = mc.x;
            cuoi[1] = mc.state;
        });
        NenPc.giaLapVeMs = 0;
        NenPc.giaLapVsync = false;
        synchronized (KHOA) {
            return new int[]{tuChoi, goiDanh, xDanh[0], x1[0], cuoi[0], lui[0], svX, cuoi[1]};
        }
    }

    public static void main(String[] a) throws Exception {
        boolean baoCao = a.length > 0 && "bao-cao".equals(a[0]);
        System.setProperty("java.awt.headless", "true");
        java.io.File ra = java.nio.file.Files.createTempDirectory("kiem_keo_lui").toFile();
        System.setProperty("htbb.duLieu", new java.io.File(ra, "du_lieu").getAbsolutePath());
        System.setProperty("microedition.platform", "HTBB-PC/kiem");
        htbb.pc.CaiDat.thuMucDuLieu();
        htbb.pc.CaiDat.nap();
        htbb.pc.ChuHD.bat = false;
        htbb.pc.GiaoDienHD.bat = false;
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
        for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) {
            Thread.sleep(100);
        }
        Thread.sleep(2500);
        game.networklogic.GameService.gI().setSession(new network.ISession() {
            public boolean isConnected() {
                return true;
            }

            public void setHandler(network.IMessageHandler h) {
            }

            public void connect(String s, String s2) {
            }

            public void sendMessage(network.Message m) {
                nhan(m);
            }

            public void close() {
            }
        });
        ChupTrongGame.chay(ChupTrongGame::giaLap);
        ChupTrongGame.chay(() -> ChupTrongGame.moMan("the_gioi"));
        java.awt.EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                htbb.pc.ManHinh.khongCuaSo();
            }
        });
        final game.render.screen.GameScr gs = game.render.Canvas.gameScr;
        final game.model.MainChar mc = gs.mainChar;
        ChupTrongGame.chay(() -> {
            if (mc.ID == 32001) mc.ID = 4242;   // như KiemTocDoGame: nhân vật đã vào game, trong actors
            if (!gs.actors.contains(mc)) gs.actors.addElement(mc);
            mc.level = 24;
            game.render.screen.GameScr.autoFight = false;
            // bảng chiêu + ô chiêu như KiemChamHud.dungCanh (máy chủ gửi lúc vào game)
            game.render.screen.GameScr.indexTabSlot = 0;
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
            for (int b = 0; b < 2; b++) for (int i = 0; i < 5; i++) game.model.MainChar.quickSlot[b][i].setIsNothing();
            for (int i = 0; i < 5; i++) game.model.MainChar.quickSlot[0][i].setIsSkill(chieu[i], mc.clazz, false);
            quai = new game.model.Monster();
            quai.ID = 3001;
            quai.catagory = 1;
            quai.setType((short) 0);
            quai.level = 3;
        });
        String[] ten = {"PC 60", "APK 60", "APK 30", "J2ME"};
        int[] fps = {60, 60, 30, 0};
        boolean[] vs = {false, true, true, true};
        int[] chiPhi = {0, 30, 60};
        System.out.println("duong  quai | ve ms | tu choi | goi danh | x danh | x +0,3 s | x cuoi | lui khi di | x may chu | state");
        // quái ngay trước mặt (16 px, trong tầm: dừng + đánh) và xa (120 px, ngoài tầm 80: đuổi tới tầm rồi đánh)
        int[] khoang = {16, 120};
        for (int i = 0; i < ten.length; i++) {
            for (int ms : chiPhi) for (int kc : khoang) {
                int[] r = chay1(fps[i], vs[i], ms, 30, kc);
                System.out.printf("%-7s %3d | %5d | %7d | %8d | %6d | %8d | %6d | %10d | %9d | %d%n", ten[i], kc, ms, r[0], r[1], r[2], r[3], r[4], r[5], r[6], r[7]);
                if (baoCao) continue;
                String n = ten[i] + " quai " + kc + " px ve " + ms + " ms";
                // quái xa có thể tự đi (AI quái) trong lúc đuổi -> gói đánh chỉ bắt buộc với quái ngay trước mặt
                if (kc <= 80) kiem(r[1] >= 1, n + ": co goi danh");
                kiem(r[0] == 0, n + ": may chu khong tu choi goi di (" + r[0] + ")");
                kiem(r[5] == 0, n + ": khong lui khi di (" + r[5] + ")");
                kiem(r[4] >= r[2] - 1 && r[4] <= r[2] + (kc > 80 ? kc : 6 * mc.speed), n + ": dung tai cho khi danh (x danh " + r[2] + ", cuoi " + r[4] + ")");
                kiem(r[4] == r[3], n + ": khong di / lui sau khi danh (" + r[3] + " -> " + r[4] + ")");
                kiem(r[7] != 1, n + ": khong con dang di (state " + r[7] + ")");
                kiem(r[6] == r[4], n + ": vi tri may chu = client (" + r[6] + " / " + r[4] + ")");
            }
        }
        if (!baoCao) System.out.println("KiemKeoLui: " + dat + " dat, " + hong + " hong");
        System.exit(hong == 0 ? 0 : 1);
    }
}
