import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.microedition.lcdui.NenPc;

/**
 * KIỂM THẺ PHÒNG RIÊNG (0.9.7, game.pokemon.PhongRieng - Phó bản Hầm ngục / trận Cúp PvP, gói -112): đọc gói đúng thứ
 * tự trường (khớp server com.htbb.pokemon.PhongRieng.viet), gói hỏng không vỡ, ẩn khi đổi bản đồ, đếm ngược tại client,
 * và VẼ ở mọi cỡ chuẩn của ChupTrongGame: thẻ trong vùng an toàn, canh giữa, không chồng nút HUD, vùng chạm >= cỡ tối
 * thiểu, chữ tiêu đề / dòng 1 đã vẽ, chạm thẻ gửi lại -112 (server hỏi xác nhận Rời / Bỏ cuộc).
 *
 *   javac -cp client_pc/out/build/cls -d $O client_pc/test/ChupTrongGame.java client_pc/test/KiemPhongRieng.java \
 *     server/src/com/htbb/pokemon/IconPokemon.java server/src/com/htbb/pokemon/IconPokemonBang.java
 *   java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemPhongRieng <thư mục ảnh>
 * Mã thoát 1 = có phép kiểm hỏng.
 */
public class KiemPhongRieng {
    static int dat, hong;

    /** 0.9.8 (giao diện NRO): đếm icon vẽ bằng code từ lần kiểm đầu (sau khi game đã lên) - phải 0. */
    static boolean daXoaVeTay;

    static void kiem(boolean ok, String ten) {
        if (!daXoaVeTay) {
            daXoaVeTay = true;
            game.pokemon.VeTay.xoa();
        }
        if (ok) dat++;
        else {
            hong++;
            System.out.println("HONG: " + ten);
        }
    }

    static network.Message goi(byte[] du) throws Exception {
        java.lang.reflect.Constructor<network.Message> c = network.Message.class.getDeclaredConstructor(byte.class, byte[].class);
        c.setAccessible(true);
        return c.newInstance((byte) -112, du);
    }

    static byte[] goi112(int loai, String t, String d1, String d2, int giay, int pt) throws Exception {
        java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
        java.io.DataOutputStream o = new java.io.DataOutputStream(b);
        o.writeByte(loai);
        o.writeUTF(t);
        o.writeUTF(d1);
        o.writeUTF(d2);
        o.writeShort(giay);
        o.writeByte(pt);
        return b.toByteArray();
    }

    static void kiemGoi() throws Exception {
        final network.Message m1 = goi(goi112(1, "Phó bản: Hang Băng", "Tầng 2/5 - hạ 3/8", "Đội còn 2/2 - còn 14:05", 845, 27));
        final network.Message m0 = goi(goi112(0, "", "", "", -1, -1));
        final network.Message hongGoi = goi(new byte[]{1, 0});
        ChupTrongGame.chay(() -> {
            kiem(game.pokemon.PhongRieng.nhan(m1), "gói -112 phó bản đọc được");
            kiem(game.pokemon.PhongRieng.dangHien(), "gói -112 loại 1 -> hiện thẻ");
            int c = game.pokemon.PhongRieng.conLai();
            kiem(c >= 844 && c <= 845, "đếm ngược bắt đầu đúng 845 giây (" + c + ")");
            kiem(game.pokemon.PhongRieng.nhan(m0), "gói -112 ẩn đọc được");
            kiem(!game.pokemon.PhongRieng.dangHien(), "gói -112 loại 0 -> ẩn thẻ");
            kiem(game.pokemon.PhongRieng.nhan(hongGoi), "gói -112 hỏng vẫn nuốt (không lọt xuống MessageHandler cũ)");
            kiem(!game.pokemon.PhongRieng.dangHien(), "gói -112 hỏng -> ẩn, không vỡ màn");
            game.pokemon.PhongRieng.datThu(2, "Cúp Nhí - vòng 3", "Ash vs Misty", "Hết giờ: bên còn nhiều % HP hơn thắng", 185, -1);
            short cu = game.render.Canvas.gameScr.map;
            game.render.Canvas.gameScr.map = (short) (cu + 1);
            game.pokemon.PhongRieng.ve(null); // doi ban do: an truoc khi ve (khong dung Graphics)
            kiem(!game.pokemon.PhongRieng.dangHien(), "đổi bản đồ mà không nhận gói ẩn -> tự ẩn thẻ");
            game.render.Canvas.gameScr.map = cu;
        });
    }

    static boolean giao(int[] a, int[] b) {
        return a[0] < b[0] + b[2] && b[0] < a[0] + a[2] && a[1] < b[1] + b[3] && b[1] < a[1] + a[3];
    }

    static final Object[][] MAU = {
        {"pho_ban", 1, "Phó bản: Nhà máy Điện", "Tầng 5/5 - Boss Manectric", "Đội còn 4/4 - còn 07:42", 462, 80},
        {"cup", 2, "Cúp Không trang bị - vòng 2", "Pikachu99 & Bulba vs Squirtle7 & Eevee", "Hết giờ: bên còn nhiều % HP hơn thắng", 120, -1},
        {"ket_qua", 1, "Phó bản: Hang Rồng", "Chiến thắng! Hạ boss tầng 5", "Rời phó bản sau vài giây", -1, 100},
    };

    static void kiemVe(File ra) throws Exception {
        for (int c = 0; c < ChupTrongGame.CO.length; c++) {
            final int W = ChupTrongGame.CO[c][0], H = ChupTrongGame.CO[c][1], dpi = ChupTrongGame.CO[c][2];
            final String co = ChupTrongGame.TEN_CO[c];
            NenPc.tinhKichThuoc(W, H);
            ChupTrongGame.chay(() -> game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao));
            float s = NenPc.hienW / (float) NenPc.rong;
            int min = dpi > 0 ? (int) Math.ceil(44 * ((dpi / 160f) / s)) : (int) Math.ceil(32 / s);
            game.pokemon.VungCham.MIN = min;
            game.pokemon.Bang.anTrai = dpi > 0 ? (int) Math.ceil(100 / s) : 0;
            for (Object[] m : MAU) {
                final String ten = (String) m[0];
                ChupTrongGame.chay(() -> {
                    ChupTrongGame.moMan("the_gioi");
                    game.render.Canvas.endDlg();
                    game.pokemon.DungDo.xoaHet();
                    htbb.pc.ChuHD.ghi.clear();
                    game.pokemon.PhongRieng.datThu((Integer) m[1], (String) m[2], (String) m[3], (String) m[4], (Integer) m[5], (Integer) m[6]);
                });
                ChupTrongGame.ve(W, H);
                BufferedImage anh = ChupTrongGame.veKiem(W, H, "KiemPhongRieng:105");
                ImageIO.write(anh, "png", new File(ra, "phong_rieng_" + ten + "_" + co + ".png"));
                java.util.List<int[]> the = new java.util.ArrayList<>(), hud = new java.util.ArrayList<>();
                int n = game.pokemon.VungCham.dem();
                for (int i = 0; i < n; i++) {
                    String t = game.pokemon.VungCham.ten(i);
                    if (t == null) continue;
                    if (t.equals("phong_rieng")) the.add(game.pokemon.VungCham.vungVe(i));
                    else if (t.startsWith("hud-")) hud.add(game.pokemon.VungCham.vungVe(i));
                }
                String nhan = ten + " " + co;
                kiem(the.size() == 1, nhan + ": đúng 1 thẻ có vùng chạm (thấy " + the.size() + ")");
                int L = game.pokemon.Bang.trai(), T = game.pokemon.Bang.tren(), R = game.pokemon.Bang.phai(), B = game.pokemon.Bang.duoi();
                for (int[] v : the) {
                    kiem(v[0] >= L && v[1] >= T && v[0] + v[2] <= R && v[1] + v[3] <= B, nhan + " thẻ trong vùng an toàn " + java.util.Arrays.toString(v));
                    kiem(v[3] >= min, nhan + " thẻ cao " + v[3] + " >= cỡ chạm " + min);
                    kiem(Math.abs((v[0] + v[2] / 2) - NenPc.rong / 2) <= 2, nhan + " thẻ canh giữa");
                    kiem(v[3] <= (B - T) / 3, nhan + " thẻ không che quá 1/3 chiều cao (" + v[3] + ")");
                    for (int[] h : hud) {
                        kiem(!giao(v, h), nhan + " thẻ không chồng nút HUD " + java.util.Arrays.toString(h));
                    }
                }
                for (int k = 2; k <= 3; k++) {
                    String dau = ((String) m[k]).substring(0, Math.min(10, ((String) m[k]).length()));
                    boolean thay = false;
                    synchronized (htbb.pc.ChuHD.ghi) {
                        for (String x : htbb.pc.ChuHD.ghi) if (x.startsWith(dau)) thay = true;
                    }
                    kiem(thay, nhan + " đã vẽ chữ '" + dau + "…'");
                }
            }
        }
        ChupTrongGame.chay(() -> game.pokemon.PhongRieng.datThu(0, "", "", "", -1, -1));
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        File ra = new File(a.length > 0 ? a[0] : "kiem_phong_rieng");
        ra.mkdirs();
        System.setProperty("htbb.duLieu", new File(ra, "du_lieu").getAbsolutePath());
        System.setProperty("microedition.platform", "HTBB-PC/kiem");
        htbb.pc.CaiDat.thuMucDuLieu();
        htbb.pc.CaiDat.nap();
        htbb.pc.ChuHD.bat = true;
        htbb.pc.GiaoDienHD.bat = true;
        htbb.pc.CaiDat.tatTieng = true;
        htbb.pc.LopPhu.nap();
        javax.microedition.midlet.MIDlet.GHI_DE.put("HTBB-May-Chu", "127.0.0.1:1");
        NenPc.tinhKichThuoc(1280, 720);
        ChupTrongGame.chay(() -> {
            try {
                game.render.screen.TField.isQwerty = true;
                javax.microedition.midlet.MIDlet.batDau((javax.microedition.midlet.MIDlet) Class.forName("game.GameMidlet").newInstance());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) Thread.sleep(100);
        Thread.sleep(2500);
        ChupTrongGame.chay(ChupTrongGame::giaLap);
        Thread.sleep(800);
        htbb.pc.ChuHD.ghi = java.util.Collections.synchronizedSet(new java.util.HashSet<String>());
        game.pokemon.VungCham.MIN = 16;
        ChupTrongGame.chay(() -> {
            ChupTrongGame.moMan("the_gioi");
            game.render.Canvas.endDlg();
        });
        kiemGoi();
        kiemVe(ra);
        kiem(ChupTrongGame.hong == 0, "chu trong khung + icon PNG (ChupTrongGame.veKiem): " + ChupTrongGame.hong + " hong");
        kiem(game.pokemon.VeTay.dem == 0, "icon ve bang code (VeTay) = 0, co " + game.pokemon.VeTay.dem + " lan, cuoi: " + game.pokemon.VeTay.cuoi);
        System.out.println("KiemPhongRieng: " + dat + " dat, " + hong + " hong -> " + ra.getAbsolutePath());
        System.exit(hong == 0 ? 0 : 1);
    }
}
