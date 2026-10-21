import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.NenPc;
import javax.microedition.midlet.MIDlet;

/**
 * Chụp màn KHÔNG cần cửa sổ (headless) qua đúng đường vẽ của bản PC (NenPc + Graphics + LopPhu) và kiểm cú chạm:
 * với mỗi cỡ cửa sổ (16:9, 19.5:9, 20:9, 21:9, 16:10, 4:3) mở lần lượt các màn trước khi vào game (máy chủ, đăng nhập,
 * đăng ký, tạo nhân vật, tải), lưu ảnh cỡ cửa sổ thật, rồi với MỖI vùng chạm đã đăng ký (game.pokemon.VungCham) bấm
 * giả ở tâm vùng (toạ độ cửa sổ -> bộ đệm -> logic, cùng phép biến đổi với chuột thật) và đòi trúng đúng vùng đó.
 *
 *   sh client_pc/build.sh && javac -cp client_pc/out/build/cls -d $O client_pc/test/ChupTruocGame.java \
 *     && java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O ChupTruocGame <thư mục ảnh>
 * Mã thoát 1 = có vùng bấm không trúng / không phủ kín.
 */
public class ChupTruocGame {
    static int dat, hong;

    static void kiem(boolean ok, String ten) {
        if (ok) dat++;
        else {
            hong++;
            System.out.println("HONG: " + ten);
        }
    }

    static final int[][] CO = {{1280, 720}, {2340, 1080}, {2400, 1080}, {2560, 1080}, {1280, 800}, {1024, 768}};
    static final String[] TEN_CO = {"16x9", "19.5x9", "20x9", "21x9", "16x10", "4x3"};

    static void chay(final Runnable r) throws Exception {
        final Object k = new Object();
        final boolean[] xong = {false};
        NenPc.luong().them(new Runnable() {
            public void run() {
                try {
                    r.run();
                } catch (Throwable e) {
                    e.printStackTrace();
                } finally {
                    synchronized (k) {
                        xong[0] = true;
                        k.notifyAll();
                    }
                }
            }
        });
        synchronized (k) {
            long het = System.currentTimeMillis() + 5000;
            while (!xong[0] && System.currentTimeMillis() < het) k.wait(100);
        }
    }

    /** Vẽ một khung như NenPc.ve + ManHinh.trinhBay, trả ảnh cỡ cửa sổ W x H. */
    static BufferedImage ve(int W, int H) throws Exception {
        final BufferedImage dem = new BufferedImage(NenPc.thatW, NenPc.thatH, BufferedImage.TYPE_INT_RGB);
        chay(new Runnable() {
            public void run() {
                Graphics2D g2 = dem.createGraphics();
                g2.setColor(java.awt.Color.MAGENTA);   // chỗ nào game không vẽ sẽ lộ màu này
                g2.fillRect(0, 0, dem.getWidth(), dem.getHeight());
                Graphics2D gg = (Graphics2D) g2.create();
                int k = NenPc.heSo;
                gg.scale(k, k);
                gg.clipRect(0, 0, NenPc.rong, NenPc.cao);
                Graphics g = new Graphics(gg, NenPc.rong, NenPc.cao, k);
                game.render.Canvas c = game.render.Canvas.instance;
                try {
                    java.lang.reflect.Method m = javax.microedition.lcdui.Canvas.class.getDeclaredMethod("paint", Graphics.class);
                    m.setAccessible(true);
                    m.invoke(c, g);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                gg.dispose();
                try {
                    htbb.pc.LopPhu.ve(g2, dem.getWidth(), dem.getHeight());
                } catch (Throwable e) {
                    // lớp phủ cần cửa sổ: bỏ
                }
                g2.dispose();
            }
        });
        BufferedImage ra = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = ra.createGraphics();
        g.setColor(java.awt.Color.BLACK);
        g.fillRect(0, 0, W, H);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(dem, NenPc.hienX, NenPc.hienY, NenPc.hienW, NenPc.hienH, null);
        g.dispose();
        return ra;
    }

    static void doi(int ms) throws InterruptedException {
        Thread.sleep(ms);
    }

    /** Bấm giả mọi vùng chạm đang đăng ký ở tâm (toạ độ cửa sổ) - đòi trúng đúng vùng đó; kiểm cỡ tối thiểu. */
    static void kiemCham(String nhan, int W, int H) {
        int n = game.pokemon.VungCham.dem();
        int top = game.pokemon.VungCham.lopCaoNhat();
        for (int i = 0; i < n; i++) {
            if (game.pokemon.VungCham.lopVung(i) != top) continue;
            int[] v = game.pokemon.VungCham.vungVe(i);
            // tâm vùng (logic) -> cửa sổ thật
            float s = NenPc.hienW / (float) NenPc.rong;
            int wx = Math.round((v[0] + v[2] / 2f) * s), wy = Math.round((v[1] + v[3] / 2f) * s);
            String t = nhan + " vung " + game.pokemon.VungCham.ten(i);
            kiem(wx >= 0 && wy >= 0 && wx < W && wy < H, t + " nam trong cua so");
            int lx = NenPc.logicX(NenPc.vaoBoDemX(wx)), ly = NenPc.logicY(NenPc.vaoBoDemY(wy));
            int j = game.pokemon.VungCham.tim(lx, ly);
            kiem(j == i, t + " bam tam trung dung vung (trung " + (j < 0 ? "-" : game.pokemon.VungCham.ten(j)) + ")");
            int[] r = game.pokemon.VungCham.vung(i);
            // cỡ chạm tối thiểu PC: ~32 px thật
            kiem(r[2] * s >= 31 && r[3] * s >= 31, t + " co cham >= 32 px that (" + Math.round(r[2] * s) + "x" + Math.round(r[3] * s) + ")");
        }
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        File ra = new File(a.length > 0 ? a[0] : "anh_truoc_game");
        ra.mkdirs();
        System.setProperty("htbb.duLieu", new File(ra, "du_lieu").getAbsolutePath());
        System.setProperty("microedition.platform", "HTBB-PC/kiem");
        System.setProperty("microedition.locale", "vi-VN");
        htbb.pc.CaiDat.thuMucDuLieu();
        htbb.pc.CaiDat.nap();
        htbb.pc.ChuHD.bat = true;
        htbb.pc.GiaoDienHD.bat = true;
        htbb.pc.CaiDat.tatTieng = true;
        MIDlet.GHI_DE.put("HTBB-May-Chu", "127.0.0.1:1");   // không có máy chủ: chỉ màn trước game
        MIDlet.GHI_DE.put("HTBB-Ban-Thu", "0");   // chụp như bản phát hành (tên máy chủ chính thức)
        NenPc.tinhKichThuoc(CO[0][0], CO[0][1]);
        chay(new Runnable() {
            public void run() {
                try {
                    game.render.screen.TField.isQwerty = true;
                    MIDlet m = (MIDlet) Class.forName("game.GameMidlet").newInstance();
                    MIDlet.batDau(m);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        // đợi qua màn tải
        for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) doi(100);
        doi(2500);
        game.pokemon.VungCham.MIN = 0;
        String[] man = {"tai", "may_chu", "dang_nhap", "dang_ky", "tao_nhan_vat", "chon_nhan_vat"};
        for (int c = 0; c < CO.length; c++) {
            final int W = CO[c][0], H = CO[c][1];
            NenPc.tinhKichThuoc(W, H);
            chay(new Runnable() {
                public void run() {
                    game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao);
                }
            });
            // PC: cỡ chạm tối thiểu 32 px thật
            game.pokemon.VungCham.MIN = (int) Math.ceil(32f / (NenPc.hienW / (float) NenPc.rong));
            for (int m = 0; m < man.length; m++) {
                final String ten = man[m];
                chay(new Runnable() {
                    public void run() {
                        moMan(ten);
                    }
                });
                doi(700);
                ve(W, H);   // khung đầu (hoạt hình mở) + đăng ký vùng
                doi(300);
                game.pokemon.VeTay.xoa();
                BufferedImage anh = ve(W, H);
                // 0.9.7: mọi icon là ẢNH PNG (logo, Poké Ball, nút) - không vẽ bằng hình
                kiem(game.pokemon.VeTay.dem == 0, ten + " " + TEN_CO[c] + " icon ve bang code (" + game.pokemon.VeTay.cuoi + ")");
                File f = new File(ra, ten + "_" + TEN_CO[c] + "_" + W + "x" + H + ".png");
                ImageIO.write(anh, "png", f);
                // phủ kín: không còn điểm "chưa vẽ" (tím) ở 4 mép
                int tim = 0;
                for (int x = 0; x < W; x += 7) {
                    if ((anh.getRGB(x, 0) & 0xFFFFFF) == 0xFF00FF || (anh.getRGB(x, H - 1) & 0xFFFFFF) == 0xFF00FF) tim++;
                }
                for (int y = 0; y < H; y += 7) {
                    if ((anh.getRGB(0, y) & 0xFFFFFF) == 0xFF00FF || (anh.getRGB(W - 1, y) & 0xFFFFFF) == 0xFF00FF) tim++;
                }
                kiem(tim == 0, ten + " " + W + "x" + H + " phu kin mep (" + tim + " diem trong)");
                if (!"tai".equals(ten)) kiemCham(ten + " " + W + "x" + H, W, H);
            }
        }
        System.out.println("ChupTruocGame: " + dat + " dat, " + hong + " hong -> " + ra.getAbsolutePath());
        System.exit(hong == 0 ? 0 : 1);
    }

    static void moMan(String ten) {
        game.render.Canvas.endDlg();
        game.render.Canvas.isLoad = "tai".equals(ten);
        if ("may_chu".equals(ten)) game.render.screen.ServerListScr.gI().switchToMe();
        else if ("dang_nhap".equals(ten)) game.render.Canvas.loginScr.switchToMe();
        else if ("dang_ky".equals(ten)) game.render.screen.DangKyScr.mo();
        else if ("tao_nhan_vat".equals(ten)) game.render.screen.CreateCharScr.gI().switchToMe();
        else if ("chon_nhan_vat".equals(ten)) {
            try {
                game.render.screen.CharSelectScr s = game.render.screen.CharSelectScr.gI();
                game.model.MainChar[] ds = new game.model.MainChar[1];
                ds[0] = new game.model.MainChar();
                ds[0].name = "Pikachu99";
                ds[0].lastLv = 12;
                ds[0].weaponType = 7;
                ds[0].using = 1;
                ds[0].nationID = -1;
                java.lang.reflect.Field f1 = s.getClass().getDeclaredField("chars");
                f1.setAccessible(true);
                f1.set(s, ds);
                s.charInfos = ds;
                s.cmdSelect = new game.model.Command("Chọn", null);
                s.cmdNew = new game.model.Command("Tạo mới", null);
                s.cmdDel = new game.model.Command("Xóa", null);
                s.center = s.cmdSelect;
                s.switchToMe();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
