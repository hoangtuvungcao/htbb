import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.NenPc;
import javax.microedition.midlet.MIDlet;

/**
 * KIỂM NHÃN PET (0.9.6, thẻ đặt tên) bằng đường vẽ THẬT của bản PC, headless, không máy chủ: nhân vật chính + pet có tên
 * riêng ("Sấm Sét · vantron"), một người chơi khác + pet chưa đặt tên (tên loài từ client), rồi cảnh CHẬT (người khác
 * đứng đúng chỗ nhãn pet). Sau mỗi khung đọc kết quả xếp chỗ của game.pokemon.NhanTheGioi (X/Y/W/H/TT/LOAI):
 *  - nhãn pet có mặt, chữ nhỏ hơn tên người chơi, chuỗi "tên pet · tên chủ";
 *  - KHÔNG nhãn pet nào chồng nhãn người chơi / nhãn khác đang hiện (cảnh chật: nhích lên hoặc bỏ, không đè);
 *  - tên người chơi (bản thân) không bị dời chỗ.
 * Ảnh chụp ghi vào thư mục tham số (nhan_pet_*.png).
 *
 *   APP_IMAGE=0 sh client_pc/build.sh && O=$(mktemp -d) && javac -cp client_pc/out/build/cls -d $O client_pc/test/KiemNhanPet.java \
 *     && java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemNhanPet <thư mục ảnh>
 * Mã thoát 1 = hỏng.
 */
public class KiemNhanPet {
    static int dat, hong;

    static void kiem(boolean ok, String ten) {
        if (ok) {
            dat++;
        } else {
            hong++;
            System.out.println("HONG: " + ten);
        }
    }

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
            while (!xong[0] && System.currentTimeMillis() < het) {
                k.wait(100);
            }
        }
    }

    static BufferedImage dem;

    /** Một khung đường vẽ thật (Canvas.paint + lớp phủ HD) vào bộ đệm. Gọi trên luồng sự kiện. */
    static void veKhung() {
        if (dem == null || dem.getWidth() != NenPc.thatW || dem.getHeight() != NenPc.thatH) {
            dem = new BufferedImage(NenPc.thatW, NenPc.thatH, BufferedImage.TYPE_INT_RGB);
        }
        Graphics2D g2 = dem.createGraphics();
        Graphics2D gg = (Graphics2D) g2.create();
        int k = NenPc.heSo;
        gg.scale(k, k);
        gg.clipRect(0, 0, NenPc.rong, NenPc.cao);
        Graphics g = new Graphics(gg, NenPc.rong, NenPc.cao, k);
        try {
            java.lang.reflect.Method m = javax.microedition.lcdui.Canvas.class.getDeclaredMethod("paint", Graphics.class);
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

    static Object truong(String ten) throws Exception {
        java.lang.reflect.Field f = Class.forName("game.pokemon.NhanTheGioi").getDeclaredField(ten);
        f.setAccessible(true);
        return f.get(null);
    }

    static game.model.MainChar mc;
    static game.model.Char khac, chat;
    static game.model.AnimalMove petMinh, petKhac;

    static game.model.AnimalMove taoPet(game.model.Char chu, int dx) {
        game.model.AnimalMove p = new game.model.AnimalMove();
        p.catagory = (byte) 12;
        p.myOwner = chu;
        p.idSameMyOwnerID = chu.ID;
        p.x = (short) (chu.x + dx);
        p.y = chu.y;
        p.xTo = p.x;
        p.yTo = p.y;
        p.type = 1;
        p.totalTime = 100000;   // phút còn lại (0 = hết hạn -> mờ dần biến mất)
        p.timeStart = System.currentTimeMillis();
        return p;
    }

    static void giaLap() {
        game.render.Canvas.isLoad = false;
        game.render.Canvas.endDlg();
        try {
            game.model.Tilemap.loadMap(0, null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        game.render.screen.GameScr gs = game.render.Canvas.gameScr;
        gs.actors.removeAllElements();
        mc = gs.mainChar;
        mc.name = "vantron";
        mc.level = 24;
        mc.pokeDex = 25;
        mc.hp = mc.maxhp = 1000;
        mc.x = 200;
        mc.y = 200;
        mc.pokePetDex = 25;
        mc.pokePetTen = "Sấm Sét";
    }

    /** Sau khi bản đồ nạp xong (Tilemap.reset chỉ giữ nhân vật chính): thêm pet + người khác. */
    static void themNguoi() {
        game.render.screen.GameScr gs = game.render.Canvas.gameScr;
        if (!gs.actors.contains(mc)) {
            gs.actors.addElement(mc);
        }
        mc.x = 200;
        mc.y = 200;
        for (int i = gs.actors.size() - 1; i >= 0; i--) {
            Object o = gs.actors.elementAt(i);
            if (o != mc && o instanceof game.model.Char) {
                gs.actors.removeElementAt(i);
            }
        }
        petMinh = taoPet(mc, 26);
        game.pokemon.PetDuyNhat.dat(gs.actors, mc, petMinh);
        khac = new game.model.Char();
        khac.ID = 2;
        khac.name = "ash";
        khac.pokeDex = 4;
        khac.x = 290;
        khac.y = 200;
        khac.pokePetDex = 1;      // Bulbasaur, chưa đặt tên: server gửi tên loài (hoặc client tự biết)
        khac.pokePetTen = null;
        khac.nationID = mc.nationID;
        gs.actors.addElement(khac);
        petKhac = taoPet(khac, 26);
        game.pokemon.PetDuyNhat.dat(gs.actors, khac, petKhac);
        game.render.Util.quickSort(gs.actors);
    }

    /** Người chơi thứ ba đứng ngay trên pet của mình: tên họ nằm đúng chỗ nhãn pet. */
    static void canhChat() {
        game.render.screen.GameScr gs = game.render.Canvas.gameScr;
        chat = new game.model.Char();
        chat.ID = 3;
        chat.name = "misty";
        chat.pokeDex = 7;
        chat.x = petMinh.x;
        chat.y = (short) (petMinh.y + 2);   // tên "misty" rơi đúng chỗ nhãn pet của mình
        chat.nationID = mc.nationID;
        gs.actors.addElement(chat);
        game.render.Util.quickSort(gs.actors);
    }

    static int yPetThuong = Integer.MIN_VALUE;

    static int[] hop(int[] X, int[] Y, int[] W, int[] H, int i) {
        return new int[]{X[i], Y[i], W[i], H[i]};
    }

    static boolean chong(int[] a, int[] b) {
        return a[0] < b[0] + b[2] && b[0] < a[0] + a[2] && a[1] < b[1] + b[3] && b[1] < a[1] + a[3];
    }

    /** Đọc kết quả xếp chỗ khung vừa vẽ và kiểm. */
    static void kiemKhung(String canh, boolean chat) throws Exception {
        int n = ((Integer) truong("soVe")).intValue();
        int[] X = (int[]) truong("X"), Y = (int[]) truong("Y"), W = (int[]) truong("W"), H = (int[]) truong("H");
        int[] TT = (int[]) truong("TT"), LOAI = (int[]) truong("LOAI");
        int PET = 5, BAN_THAN = 2;
        int soPet = 0, soPetHien = 0, hNguoi = 0, hPet = 0;
        kiem(n >= 3, canh + ": co nhan (" + n + ")");
        StringBuffer b = new StringBuffer(canh + ": nhan");
        for (int i = 0; i < n; i++) {
            b.append(" [loai ").append(LOAI[i]).append(" tt ").append(TT[i]).append(" ").append(X[i]).append(',').append(Y[i])
                    .append(' ').append(W[i]).append('x').append(H[i]).append(']');
        }
        System.out.println(b + " actors=" + game.render.Canvas.gameScr.actors.size()
                + (chat ? " misty=" + game.render.Canvas.gameScr.actors.contains(KiemNhanPet.chat) : ""));
        for (int i = 0; i < n; i++) {
            if (LOAI[i] == PET) {
                soPet++;
                hPet = H[i];
                if (TT[i] != 0) {
                    soPetHien++;
                    kiem(TT[i] == 1, canh + ": nhan pet khong ve mo (hien thi thuong hoac bo)");
                    for (int j = 0; j < n; j++) {
                        if (j != i && TT[j] != 0) {
                            kiem(!chong(hop(X, Y, W, H, i), hop(X, Y, W, H, j)), canh + ": nhan pet " + i + " khong chong nhan " + j
                                    + " (loai " + LOAI[j] + ")");
                        }
                    }
                }
            } else if (LOAI[i] != 0) {
                hNguoi = H[i];
                kiem(TT[i] != 0, canh + ": ten nguoi choi " + i + " luon hien");
            }
        }
        kiem(soPet == 2, canh + ": 2 pet co yeu cau nhan (" + soPet + ")");
        kiem(chat ? soPetHien >= 1 : soPetHien == 2, canh + ": pet hien " + soPetHien);
        if (chat) {
            // nhãn pet của mình (loại PET đầu tiên ghi = pet của nhân vật chính) phải đã NHÍCH khỏi chỗ gốc hoặc bị BỎ
            int iPet = -1;
            for (int i = 0; i < n && iPet < 0; i++) {
                if (LOAI[i] == PET) {
                    iPet = i;
                }
            }
            kiem(iPet >= 0 && (TT[iPet] == 0 || Y[iPet] != yPetThuong), canh + ": nhan pet minh nhich / bo khi bi ten 'misty' chiem cho (y "
                    + (iPet < 0 ? -1 : Y[iPet]) + " vs " + yPetThuong + ", tt " + (iPet < 0 ? -1 : TT[iPet]) + ")");
        } else {
            for (int i = 0; i < n; i++) {
                if (LOAI[i] == PET) {
                    yPetThuong = Y[i];
                    break;
                }
            }
        }
        kiem(hPet > 0 && hPet < hNguoi, canh + ": chu nhan pet nho hon ten nguoi choi (" + hPet + " < " + hNguoi + ")");
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        File ra = new File(a.length > 0 ? a[0] : "anh_nhan_pet");
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
        for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) {
            Thread.sleep(100);
        }
        Thread.sleep(2500);
        chay(new Runnable() {
            public void run() {
                giaLap();
                game.render.Canvas.gameScr.switchToMe();
            }
        });
        Thread.sleep(500);
        // dừng vòng game (luồng Canvas.run) để khung kiểm là khung DUY NHẤT vẽ / đọc NhanTheGioi: cảnh không bị cập nhật
        // (gỡ người chơi giả, di chuyển pet) giữa lúc dựng và lúc vẽ
        java.lang.reflect.Field chayVong = game.render.Canvas.class.getDeclaredField("bRun");
        chayVong.setAccessible(true);
        chayVong.set(java.lang.reflect.Modifier.isStatic(chayVong.getModifiers()) ? null : game.render.Canvas.instance, Boolean.FALSE);
        Thread.sleep(300);
        final String[] canh = {"thuong", "chat"};
        for (int c = 0; c < canh.length; c++) {
            final int cc = c;
            final Exception[] loi = {null};
            chay(new Runnable() {
                public void run() {
                    try {
                        themNguoi();   // vòng game có thể đã gỡ người chơi giả giữa hai cảnh: dựng lại
                        if (cc == 1) {
                            canhChat();
                        }
                        game.render.Canvas.endDlg();
                        veKhung();
                        veKhung();
                        kiemKhung(canh[cc], cc == 1);
                    } catch (Exception e) {
                        loi[0] = e;
                    }
                }
            });
            kiem(loi[0] == null, canh[c] + ": khong loi " + loi[0]);
            BufferedImage anh = new BufferedImage(NenPc.hienW, NenPc.hienH, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = anh.createGraphics();
            g.drawImage(dem, 0, 0, NenPc.hienW, NenPc.hienH, null);
            g.dispose();
            ImageIO.write(anh, "png", new File(ra, "nhan_pet_" + canh[c] + ".png"));
        }
        kiem("Sấm Sét · vantron".equals(mc.nhanPet), "nhan pet minh = 'Sấm Sét · vantron' (" + mc.nhanPet + ")");
        kiem("Bulbasaur · ash".equals(khac.nhanPet), "nhan pet nguoi khac = ten loai + chu (" + khac.nhanPet + ")");
        System.out.println("KiemNhanPet: " + dat + " dat, " + hong + " hong -> " + ra.getAbsolutePath());
        System.exit(hong == 0 ? 0 : 1);
    }
}
