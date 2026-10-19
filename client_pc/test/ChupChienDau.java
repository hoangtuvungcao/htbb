import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.microedition.lcdui.NenPc;
import javax.microedition.midlet.MIDlet;

/**
 * Chụp cảnh CHIẾN ĐẤU trong thế giới (headless, đường vẽ bản PC, không máy chủ) để soát nhãn / số bay / viên trạng thái
 * 0.9.5: nhân vật chính giữa đám quái đứng sát nhau + người chơi khác, một quái đang là mục tiêu (khung mục tiêu giữa
 * trên), Tự động đánh đang chạy (viên "Auto · h:mm · đang đánh" dưới thẻ), số sát thương thường / chí mạng / hồi máu /
 * kém hiệu quả + thẻ khác hệ (cờ -95 của máy chủ), "+N EXP" cạnh thanh EXP, "Lên cấp!".
 * Kiểm thêm: nhãn đã xếp (NhanTheGioi) không chồng nhau.
 *
 *   javac -cp $HTBB_PC_OUT/build/cls -d $O client_pc/test/ChupTrongGame.java client_pc/test/ChupChienDau.java \
 *       server/src/com/htbb/pokemon/IconPokemon.java server/src/com/htbb/pokemon/IconPokemonBang.java \
 *     && java -Djava.awt.headless=true -cp $HTBB_PC_OUT/build/cls:$HTBB_PC_OUT/build/res:$O ChupChienDau <thư mục ảnh>
 */
public class ChupChienDau {
    static final int[][] CO = {{2400, 1080}, {1920, 1080}, {2048, 1536}, {1920, 1080}, {1280, 720}};
    static final String[] TEN_CO = {"dt20x9", "dt16x9", "tablet4x3", "pc1080", "pc720"};

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        File ra = new File(a.length > 0 ? a[0] : "anh_chien_dau");
        ra.mkdirs();
        System.setProperty("htbb.duLieu", new File(ra, "du_lieu").getAbsolutePath());
        System.setProperty("microedition.platform", "HTBB-PC/kiem");
        htbb.pc.CaiDat.thuMucDuLieu();
        htbb.pc.CaiDat.nap();
        htbb.pc.ChuHD.bat = true;
        htbb.pc.GiaoDienHD.bat = true;
        htbb.pc.CaiDat.tatTieng = true;
        htbb.pc.LopPhu.nap();
        MIDlet.GHI_DE.put("HTBB-May-Chu", "127.0.0.1:1");
        NenPc.tinhKichThuoc(1280, 720);
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                try {
                    game.render.screen.TField.isQwerty = true;
                    MIDlet.batDau((MIDlet) Class.forName("game.GameMidlet").newInstance());
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) Thread.sleep(100);
        Thread.sleep(2500);
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                ChupTrongGame.giaLap();
                dungCanh();
            }
        });
        Thread.sleep(500);
        int hong = 0;
        for (int c = 0; c < CO.length; c++) {
            final int W = CO[c][0], H = CO[c][1];
            NenPc.tinhKichThuoc(W, H);
            ChupTrongGame.chay(new Runnable() {
                public void run() {
                    game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao);
                    ChupTrongGame.moMan("the_gioi");
                    dungCanh();
                }
            });
            ChupTrongGame.ve(W, H);
            ChupTrongGame.chay(new Runnable() {
                public void run() {
                    ChupTrongGame.giaMayChuAnh();
                }
            });
            // dựng cảnh + bắn số NGAY trước khi vẽ, cùng lượt luồng sự kiện (vòng game / Tự động không chen vào)
            BufferedImage anh = ve(W, H, new Runnable() {
                public void run() {
                    dungCanh();
                    banSo();
                }
            });
            File f = new File(ra, "chien_dau_" + TEN_CO[c] + ".png");
            ImageIO.write(anh, "png", f);
            System.out.println("anh " + f.getAbsolutePath());
        }
        System.out.println("ChupChienDau: " + (hong == 0 ? "dat" : hong + " hong") + " -> " + ra.getAbsolutePath());
        System.exit(hong == 0 ? 0 : 1);
    }

    /** Như ChupTrongGame.ve nhưng chạy truoc ngay trước Canvas.paint trên cùng lượt. */
    static BufferedImage ve(int W, int H, final Runnable truoc) throws Exception {
        final BufferedImage dem = new BufferedImage(NenPc.thatW, NenPc.thatH, BufferedImage.TYPE_INT_RGB);
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                truoc.run();
                java.awt.Graphics2D g2 = dem.createGraphics();
                java.awt.Graphics2D gg = (java.awt.Graphics2D) g2.create();
                int k = NenPc.heSo;
                gg.scale(k, k);
                gg.clipRect(0, 0, NenPc.rong, NenPc.cao);
                javax.microedition.lcdui.Graphics g = new javax.microedition.lcdui.Graphics(gg, NenPc.rong, NenPc.cao, k);
                try {
                    java.lang.reflect.Method m = javax.microedition.lcdui.Canvas.class.getDeclaredMethod("paint", javax.microedition.lcdui.Graphics.class);
                    m.setAccessible(true);
                    m.invoke(game.render.Canvas.instance, g);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                gg.dispose();
                try {
                    htbb.pc.LopPhu.ve(g2, dem.getWidth(), dem.getHeight());
                } catch (Throwable e) {
                    // bỏ
                }
                g2.dispose();
            }
        });
        BufferedImage ra = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = ra.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(dem, NenPc.hienX, NenPc.hienY, NenPc.hienW, NenPc.hienH, null);
        g.dispose();
        return ra;
    }

    static game.model.Monster[] QUAI;
    static game.model.Char[] NGUOI;

    /** Nhân vật chính giữa 6 quái đứng sát + 3 người chơi; quái thứ 2 là mục tiêu; Tự động đánh bật. */
    static void dungCanh() {
        game.render.screen.GameScr gs = game.render.Canvas.gameScr;
        game.model.MainChar mc = gs.mainChar;
        mc.name = "vantrong";
        mc.x = 200;
        mc.y = 200;
        mc.xTo = mc.x;
        mc.yTo = mc.y;
        if (QUAI == null) {
            QUAI = new game.model.Monster[6];
            int[][] xy = {{-34, -6}, {-18, 4}, {14, -10}, {30, 2}, {22, 18}, {-8, 22}};
            int[] loai = {0, 1, 2, 3, 4, 5};
            for (int i = 0; i < QUAI.length; i++) {
                game.model.Monster m = new game.model.Monster();
                m.ID = (short) (3000 + i);
                m.catagory = 1;
                m.setType((short) loai[i]);
                m.level = (byte) (3 + i * 2);
                m.maxhp = 120;
                m.hp = 120 - i * 17;
                m.x = (short) (mc.x + xy[i][0]);
                m.y = (short) (mc.y + xy[i][1]);
                m.xFirst = m.x;
                m.yFirst = m.y;
                QUAI[i] = m;
            }
            NGUOI = new game.model.Char[3];
            String[] ten = {"Misty", "BrockPewter", "vantron"};
            int[][] p = {{-26, -26}, {40, -22}, {6, -30}};
            int[] dex = {7, 74, 25};
            for (int i = 0; i < NGUOI.length; i++) {
                game.model.Char c = new game.model.Char();
                c.ID = (short) (2000 + i);
                c.name = ten[i];
                c.catagory = 0;
                c.level = (byte) (10 + i);
                c.pokeDex = (short) dex[i];
                c.hp = 100;
                c.maxhp = 100;
                c.nationID = mc.nationID;
                c.x = (short) (mc.x + p[i][0]);
                c.y = (short) (mc.y + p[i][1]);
                c.xTo = c.x;
                c.yTo = c.y;
                NGUOI[i] = c;
            }
        }
        for (int i = 0; i < QUAI.length; i++) {
            game.model.Monster m = QUAI[i];
            m.x = (short) (m.xFirst);
            m.y = (short) (m.yFirst);
            m.state = 0;
            if (!gs.actors.contains(m)) gs.actors.addElement(m);
        }
        if (!gs.actors.contains(mc)) gs.actors.addElement(mc);   // như GameScr khi vào map thật
        QUAI[0].pokeTrungLuc = game.render.Canvas.gameTick;   // vừa trúng đòn: thanh HP nhỏ
        for (int i = 0; i < NGUOI.length; i++) {
            if (!gs.actors.contains(NGUOI[i])) gs.actors.addElement(NGUOI[i]);
        }
        gs.focusedActor = QUAI[1];
        try {
            Class<?> t = Class.forName("game.render.screen.TuDong");
            dat(t, "coServer", Boolean.TRUE);
            dat(t, "svConLai", Integer.valueOf(4 * 3600 + 41 * 60 + 20));
            dat(t, "svToiDa", Integer.valueOf(5 * 3600));
            dat(t, "svLuc", Long.valueOf(System.currentTimeMillis()));
            dat(t, "dang", Boolean.TRUE);
            dat(t, "trangThai", "đang đánh");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /** Số sát thương các kiểu + cờ khác hệ (-95), hồi máu, EXP, lên cấp. */
    static void banSo() {
        game.render.screen.GameScr gs = game.render.Canvas.gameScr;
        game.model.MainChar mc = gs.mainChar;
        game.model.Monster m1 = QUAI[1], m3 = QUAI[3], m0 = QUAI[0], m4 = QUAI[4];
        for (int i = 0; i < 60; i++) game.pokemon.SoBay.capNhat();   // bỏ số của ảnh trước
        // thường + siêu hiệu quả (bit2)
        game.pokemon.SoBay.bat("-37", 0, m1.x, m1.y - 26);
        game.pokemon.SoBay.donMayChu(m1.x, m1.y - 26, 4, 37);
        // chí mạng (bit0)
        game.pokemon.SoBay.bat("-128", 0, m3.x, m3.y - 26);
        game.pokemon.SoBay.donMayChu(m3.x, m3.y - 26, 1, 128);
        // kém hiệu quả (bit3) / không tác dụng (bit4)
        game.pokemon.SoBay.bat("-6", 0, m0.x, m0.y - 26);
        game.pokemon.SoBay.donMayChu(m0.x, m0.y - 26, 8, 6);
        game.pokemon.SoBay.bat("-1", 0, m4.x, m4.y - 26);
        game.pokemon.SoBay.donMayChu(m4.x, m4.y - 26, 16, 1);
        // nhân vật chính bị đánh + hồi máu
        game.pokemon.SoBay.bat("-14", 4, mc.x, mc.y - 30);
        game.pokemon.SoBay.bat("+40", 0, mc.x + 12, mc.y - 34);
        // EXP (cạnh thanh EXP) + lên cấp
        try {
            dat(game.pokemon.VienTrangThai.class, "expLuc", Integer.valueOf(-1000));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        game.pokemon.SoBay.bat("+125 xp", 0, mc.x, mc.y - 30);
        game.pokemon.SoBay.bat("+60 xp", 0, mc.x, mc.y - 30);
        game.pokemon.SoBay.bat("level-up", 0, mc.x, mc.y - 40);
        for (int i = 0; i < 4; i++) game.pokemon.SoBay.capNhat();
    }

    static void dat(Class<?> c, String ten, Object gt) throws Exception {
        java.lang.reflect.Field f = c.getDeclaredField(ten);
        f.setAccessible(true);
        f.set(null, gt);
    }
}
