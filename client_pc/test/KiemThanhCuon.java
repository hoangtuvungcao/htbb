import java.awt.image.BufferedImage;
import java.io.File;
import javax.microedition.lcdui.NenPc;
import javax.microedition.midlet.MIDlet;

/**
 * KIỂM HỒI QUY 0.9.11 (headless, không máy chủ): KÉO NÚM THANH CUỘN.
 *
 * Lỗi 0.9.10 (PC, Nhân vật -> Ngoại hình -> Hào quang): kéo núm thanh cuộn LÊN thì danh sách cuộn XUỐNG và ngược lại -
 * game.pokemon.Cuon coi cú kéo trên thanh như kéo nội dung (vuốt ngón). Đúng: núm đi theo con trỏ, nội dung theo tỉ lệ
 * (núm xuống = cuộn xuống = hàng sau), chạm rãnh = nhảy tới chỗ chạm, kéo trên NỘI DUNG giữ chiều tự nhiên.
 *
 * A. game.pokemon.ThanhCuon (phép tính chung): chiều, tỉ lệ, kẹp, chạm rãnh.
 * B. game.pokemon.Cuon qua màn thật (tab Ngoại hình -> Hào quang, 16 món) ở PC 1280x720, điện thoại 20:9 (cỡ chạm
 *    44 dp) và đường vẽ J2ME (tắt HD), mỗi kiểu nhập (cham = Canvas.pointer* như APK / J2ME, chuot = NenPc.cham như
 *    chuột PC): nhấn núm -> kéo xuống từng bước (độ cuộn = đầu + dy * max / (rãnh - núm) ±2, núm theo con trỏ ±2, tăng
 *    dần) -> kéo lên (giảm dần) -> quá đáy = max, quá đỉnh = 0 -> thả: không chạm hàng (không thử đồ / không hộp), không
 *    trôi quán tính; chạm rãnh dưới núm = nhảy tới (tâm núm ở chỗ chạm); kéo NỘI DUNG lên = cuộn xuống 1:1, kéo xuống
 *    = cuộn lên.
 * C. Danh sách cũ có thanh cuộn: Menu (Canvas.menu), màn ghi công (GhiCongScr) - kéo núm cùng chiều, tỉ lệ.
 *
 *   APP_IMAGE=0 sh client_pc/build.sh && javac -cp client_pc/out/build/cls -d $O client_pc/test/ChupTrongGame.java \
 *       client_pc/test/KiemThanhCuon.java server/src/com/htbb/pokemon/IconPokemon.java server/src/com/htbb/pokemon/IconPokemonBang.java
 *   java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemThanhCuon [thư mục ảnh]
 * Mã thoát 1 = có lỗi.
 */
public class KiemThanhCuon {
    static int dat, hong;
    static String KIEU = "cham", CO = "pc720";
    static File ra;
    static int soAnh;

    static void kiem(boolean ok, String ten) {
        if (ok) dat++;
        else {
            hong++;
            System.out.println("HONG [" + CO + " " + KIEU + "]: " + ten);
        }
    }

    static void ngu(long ms) throws Exception {
        Thread.sleep(ms);
    }

    static void goi(final String ten, final int x, final int y) throws Exception {
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                try {
                    java.lang.reflect.Method m = game.render.Canvas.class.getDeclaredMethod(ten, int.class, int.class);
                    m.setAccessible(true);
                    m.invoke(game.render.Canvas.instance, x, y);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /** kieu 0 nhấn, 1 kéo, 2 thả - toạ độ logic. Chuột: đi qua NenPc.cham (toạ độ bộ đệm thật) như ManHinh. */
    static void con(int kieu, int x, int y) throws Exception {
        if ("chuot".equals(KIEU)) {
            NenPc.cham(kieu, x * NenPc.heSo + NenPc.heSo / 2, y * NenPc.heSo + NenPc.heSo / 2);
        } else {
            goi(kieu == 0 ? "pointerPressed" : kieu == 1 ? "pointerDragged" : "pointerReleased", x, y);
        }
        if (kieu == 2) ngu(260);   // NenPc: cú thả chuột nhanh được hoãn tới nhịp game sau
    }

    static int W = 1280, H = 720;

    static void ve(String nhan) throws Exception {
        BufferedImage a = ChupTrongGame.ve(W, H);
        if (ra != null && nhan != null && soAnh < 40) {
            javax.imageio.ImageIO.write(a, "png", new File(ra, String.format("cuon_%02d_%s_%s_%s.png", soAnh++, CO, KIEU, nhan)));
        }
    }

    static Object doc(Object o, Class<?> c, String ten) {
        try {
            java.lang.reflect.Field f = c.getDeclaredField(ten);
            f.setAccessible(true);
            return f.get(o);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    static int docInt(Object o, Class<?> c, String ten) {
        return ((Number) doc(o, c, ten)).intValue();
    }

    static Object goiHam(Object o, Class<?> c, String ten) {
        try {
            java.lang.reflect.Method m = c.getDeclaredMethod(ten);
            m.setAccessible(true);
            return m.invoke(o);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    // ------------------------------------------------------------------ A. phép tính chung

    static void kiemPhepTinh() {
        String k = KIEU;
        KIEU = "toan";
        // rãnh 200, núm 50, max 600: 1 px núm = 4 px nội dung
        kiem(game.pokemon.ThanhCuon.keo(100, 50, 80, 200, 50, 600) == 220, "keo: núm xuống 30 px = cuộn xuống 120 px");
        kiem(game.pokemon.ThanhCuon.keo(300, 50, 20, 200, 50, 600) == 180, "keo: núm lên 30 px = cuộn lên 120 px");
        kiem(game.pokemon.ThanhCuon.keo(500, 0, 400, 200, 50, 600) == 600, "keo: quá đáy kẹp max");
        kiem(game.pokemon.ThanhCuon.keo(100, 400, 0, 200, 50, 600) == 0, "keo: quá đỉnh kẹp 0");
        kiem(game.pokemon.ThanhCuon.yNum(10, 200, 50, 600, 600) == 160, "yNum: cuối = đáy rãnh - núm");
        kiem(game.pokemon.ThanhCuon.yNum(10, 200, 50, 0, 600) == 10, "yNum: đầu = đỉnh rãnh");
        kiem(game.pokemon.ThanhCuon.toi(10 + 200, 10, 200, 50, 600) == 600, "toi: chạm đáy rãnh = cuối");
        kiem(game.pokemon.ThanhCuon.toi(10 + 25 + 75, 10, 200, 50, 600) == 300, "toi: chạm giữa rãnh = giữa");
        kiem(game.pokemon.ThanhCuon.trenNum(40, 10, 200, 50, 0, 600) && !game.pokemon.ThanhCuon.trenNum(70, 10, 200, 50, 0, 600),
                "trenNum: núm 10..60");
        kiem(game.pokemon.ThanhCuon.trung(100, 50, 100, 4, 10, 200) && !game.pokemon.ThanhCuon.trung(80, 50, 100, 4, 10, 200)
                && !game.pokemon.ThanhCuon.trung(100, 300, 100, 4, 10, 200), "trung: vùng chạm thanh");
        kiem(game.pokemon.ThanhCuon.keo(7, 0, 100, 50, 50, 0) == 0, "keo: không có chỗ cuộn = 0");
        KIEU = k;
    }

    // ------------------------------------------------------------------ B. Cuon (Ngoại hình -> Hào quang)

    /** Tủ đồ giả: 16 hào quang (danh sách dài hơn khung -> có thanh cuộn ở mọi cỡ). */
    static byte[] goiTuDo() throws java.io.IOException {
        java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
        java.io.DataOutputStream o = new java.io.DataOutputStream(bo);
        String[][] oo = {{"trang_phuc", "Trang phục", "15"}, {"hao_quang", "Hào quang", "10"}};
        o.writeByte(oo.length);
        for (int i = 0; i < oo.length; i++) {
            o.writeUTF(oo[i][0]);
            o.writeUTF(oo[i][1]);
            o.writeShort(Integer.parseInt(oo[i][2]));
            o.writeShort(Integer.parseInt(oo[i][2]));
            o.writeByte(2);
            o.writeUTF("hp");
            o.writeUTF("HP");
            o.writeUTF("tan_cong");
            o.writeUTF("Tấn công");
            o.writeByte(0);
            o.writeUTF("");
            o.writeInt(0);
        }
        o.writeShort(35);
        o.writeShort(50);
        o.writeShort(10);
        String[] ten = {"Băng", "Tâm linh", "Rồng", "Tiên", "Lửa", "Nước", "Cỏ", "Điện", "Đá", "Đất", "Bay", "Bọ", "Độc", "Ma",
            "Thép", "Bóng tối"};
        o.writeShort(ten.length);
        for (int i = 0; i < ten.length; i++) {
            o.writeUTF("hq_" + i);
            o.writeByte(1);
            o.writeByte(i + 1);
            o.writeUTF("Hào quang " + ten[i]);
            o.writeByte(i % 5);
            o.writeShort(15660 + (i % 11));
            o.writeByte(0);
            o.writeUTF(i % 2 == 0 ? "Cửa hàng: 250 lượng" : "Chế tạo ở Machamp");
            o.writeUTF("");
            o.writeByte(i % 2 == 0 ? 3 : 2);   // Mua / Chế tạo
            o.writeUTF("Vòng sáng " + ten[i] + ".");
        }
        return bo.toByteArray();
    }

    static game.pokemon.Cuon cuon() {
        return (game.pokemon.Cuon) doc(null, game.pokemon.ManTuDo.class, "CUON");
    }

    static void moHaoQuang() throws Exception {
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                ChupTrongGame.moMan("nv_tu_do_hao_quang");
                try {
                    game.pokemon.ManTuDo.napThu(goiTuDo());
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                game.pokemon.ManTuDo.xinLuc = Long.MAX_VALUE / 2;
                game.pokemon.ManTuDo.tab = 1;
                game.pokemon.ManTuDo.boThu();
                game.render.Canvas.endDlg();
                cuon().den(0);
            }
        });
        ngu(200);
        ve(null);
        ve(null);
    }

    static void kiemCuon() throws Exception {
        moHaoQuang();
        final game.pokemon.Cuon c = cuon();
        int[] t = c.thanh();
        kiem(t != null, "Hào quang: danh sách dài có thanh cuộn");
        if (t == null) return;
        ve("mo");
        int max = c.toiDa();
        int du = t[3] - t[4];
        kiem(max > 0 && du > 0, "thanh: còn chỗ cuộn (max " + max + ", rãnh - núm " + du + ")");
        int x = t[0] + t[1] / 2;
        int y0 = t[5] + t[4] / 2;
        int l0 = c.lech();
        // --- nhấn núm, kéo XUỐNG từng bước
        con(0, x, y0);
        ngu(80);
        ve(null);
        kiem(c.dangKeoNum(), "nhấn núm = nắm núm (không phải kéo nội dung)");
        kiem(Math.abs(c.lech() - l0) <= 1, "nhấn núm: chưa nhảy (" + l0 + " -> " + c.lech() + ")");
        int truoc = c.lech();
        int buoc = Math.max(3, du / 5);
        int y = y0;
        for (int i = 1; i <= 3; i++) {
            y = y0 + i * buoc;
            con(1, x, y);
            ngu(60);
            ve(i == 2 ? "keo_xuong" : null);
            int l = c.lech();
            int mong = Math.min(max, l0 + (int) ((long) (y - y0) * max / du));
            kiem(l > truoc, "kéo núm XUỐNG lần " + i + ": nội dung cuộn xuống (" + truoc + " -> " + l + ")");
            kiem(Math.abs(l - mong) <= 2, "kéo núm xuống lần " + i + ": tỉ lệ rãnh (" + l + " / mong " + mong + ")");
            int[] t2 = c.thanh();
            kiem(t2 != null && Math.abs((t2[5] - t[5]) - (y - y0)) <= 2, "kéo núm xuống lần " + i + ": núm theo con trỏ ("
                    + (t2 == null ? "-" : String.valueOf(t2[5] - t[5])) + " / " + (y - y0) + ")");
            truoc = l;
        }
        // --- kéo LÊN
        int yDinh = y;
        int lDinh = c.lech();
        for (int i = 1; i <= 2; i++) {
            y = yDinh - i * buoc;
            con(1, x, y);
            ngu(60);
            ve(i == 1 ? "keo_len" : null);
            int l = c.lech();
            int mong = Math.max(0, lDinh - (int) ((long) (yDinh - y) * max / du) - 2);
            kiem(l < truoc, "kéo núm LÊN lần " + i + ": nội dung cuộn lên (" + truoc + " -> " + l + ")");
            kiem(Math.abs(l - (l0 + (long) (y - y0) * max / du)) <= 2, "kéo núm lên lần " + i + ": tỉ lệ rãnh (" + l + ", mong ~" + mong + ")");
            truoc = l;
        }
        // --- quá đáy / quá đỉnh (kể cả lệch ngang ra ngoài thanh)
        con(1, x - 40, t[2] + t[3] + 60);
        ngu(60);
        ve(null);
        kiem(c.lech() == max, "kéo núm quá đáy = cuối (" + c.lech() + " / " + max + ")");
        con(1, x + 30, t[2] - 60);
        ngu(60);
        ve(null);
        kiem(c.lech() == 0, "kéo núm quá đỉnh = đầu (" + c.lech() + ")");
        con(1, x, y0 + buoc);
        ngu(60);
        ve(null);
        int lTha = c.lech();
        con(2, x, y0 + buoc);
        ve(null);
        ve(null);
        kiem(!c.dangKeoNum(), "thả: hết nắm núm");
        kiem(c.lech() == lTha, "thả núm: không trôi quán tính (" + lTha + " -> " + c.lech() + ")");
        kiem(game.render.Canvas.currentDialog == null && game.pokemon.ManTuDo.thuO == -1, "thả núm: không chạm vào hàng (không hộp / thử đồ)");
        // --- chạm rãnh dưới núm: nhảy tới chỗ chạm
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                c.den(0);
            }
        });
        ve(null);
        ve(null);
        int yRanh = t[2] + t[3] * 3 / 4;
        con(0, x, yRanh);
        ngu(80);
        ve(null);
        int mongToi = game.pokemon.ThanhCuon.toi(yRanh, t[2], t[3], t[4], max);
        kiem(c.lech() > 0 && Math.abs(c.lech() - mongToi) <= 2, "chạm rãnh dưới núm: nhảy xuống tới chỗ chạm (" + c.lech() + " / " + mongToi + ")");
        int[] t3 = c.thanh();
        kiem(t3 != null && Math.abs(t3[5] + t3[4] / 2 - yRanh) <= 2, "chạm rãnh: tâm núm ở chỗ chạm");
        con(1, x, yRanh - buoc);
        ngu(60);
        ve(null);
        kiem(c.lech() < mongToi, "chạm rãnh rồi kéo lên: cuộn lên theo núm");
        con(2, x, yRanh - buoc);
        ve(null);
        kiem(game.render.Canvas.currentDialog == null && game.pokemon.ManTuDo.thuO == -1, "chạm rãnh: không chạm vào hàng");
        // --- kéo NỘI DUNG (giữa hàng, trái thanh): chiều tự nhiên theo ngón
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                c.den(0);
            }
        });
        ve(null);
        int xn = t[0] - Math.min(120, (t[0]) / 3);
        int yn = t[2] + t[3] / 2;
        con(0, xn, yn);
        ngu(60);
        ve(null);
        kiem(!c.dangKeoNum(), "nhấn nội dung: không nắm núm");
        con(1, xn, yn - 20);
        ngu(60);
        ve(null);
        con(1, xn, yn - 40);
        ngu(60);
        ve("noi_dung_len");
        int ln = c.lech();
        kiem(ln >= 34 && ln <= 46, "kéo NỘI DUNG lên 40 px: cuộn xuống ~40 px theo ngón (" + ln + ")");
        con(1, xn, yn - 10);
        ngu(60);
        ve(null);
        kiem(c.lech() < ln && Math.abs(c.lech() - 10) <= 6, "kéo nội dung xuống lại: cuộn lên theo ngón (" + c.lech() + ")");
        con(2, xn, yn - 10);
        ve(null);
        kiem(game.render.Canvas.currentDialog == null && game.pokemon.ManTuDo.thuO == -1, "kéo nội dung: thả không chạm hàng");
    }

    // ------------------------------------------------------------------ C. danh sách cũ

    static void kiemMenu() throws Exception {
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                ChupTrongGame.moMan("nv_chi_so");   // menu chữ trên màn chơi do MenuNguCanh tiếp quản (không cuộn)
                java.util.Vector v = new java.util.Vector();
                for (int i = 0; i < 20; i++) {
                    v.addElement(new game.model.Command("Mục " + (i + 1), new game.model.IAction() {
                        public void perform() {
                            daChon++;
                        }
                    }));
                }
                game.render.Canvas.menu.startAt(v, 0);
            }
        });
        daChon = 0;
        ngu(150);
        ve(null);
        game.render.screen.Menu m = game.render.Canvas.menu;
        kiem(m.showMenu, "Menu: đang mở");
        int[] t = (int[]) goiHam(m, game.render.screen.Menu.class, "thanh");
        kiem(t != null, "Menu 20 mục: có thanh cuộn");
        if (t == null) return;
        int max = docInt(m, game.render.screen.Menu.class, "cylim");
        int du = t[3] - t[4];
        int x = t[0] + 1, y0 = t[2] + t[4] / 2;
        con(0, x, y0);
        ngu(100);
        con(1, x, y0 + du / 2);
        ngu(120);
        ve("menu_keo");
        int l = docInt(m, game.render.screen.Menu.class, "ctoyBag");
        int mong = (int) ((long) (du / 2) * max / du);
        kiem(l > 0 && Math.abs(l - mong) <= 2, "Menu: kéo núm xuống nửa rãnh = cuộn xuống nửa (" + l + " / " + mong + ")");
        con(1, x, y0 + du / 4);
        ngu(120);
        int l2 = docInt(m, game.render.screen.Menu.class, "ctoyBag");
        kiem(l2 < l, "Menu: kéo núm lên = cuộn lên (" + l + " -> " + l2 + ")");
        con(2, x, y0 + du / 4);
        ngu(100);
        kiem(daChon == 0 && m.showMenu, "Menu: thả núm không chọn mục");
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                game.render.Canvas.menu.showMenu = false;
            }
        });
    }

    static int daChon;

    static void kiemGhiCong() throws Exception {
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                ChupTrongGame.moMan("the_gioi");
                game.render.screen.GhiCongScr.mo();
            }
        });
        ngu(150);
        ve(null);
        ve(null);
        Object s = game.render.screen.GhiCongScr.gI();
        Class<?> c = game.render.screen.GhiCongScr.class;
        int max = docInt(s, c, "cmyLim");
        int th = docInt(s, c, "thH"), tn = docInt(s, c, "thN"), tx = docInt(s, c, "thX"), ty = docInt(s, c, "thY");
        kiem(max > 0 && th > 0, "Ghi công: có thanh cuộn (max " + max + ")");
        if (max <= 0 || th <= 0) return;
        int du = th - tn;
        int x = tx + 1, y0 = ty + tn / 2;
        con(0, x, y0);
        ngu(100);
        con(1, x, y0 + du / 3);
        ngu(200);
        ve("ghi_cong_keo");
        int l = docInt(s, c, "cmtoY");
        int mong = (int) ((long) (du / 3) * max / du);
        kiem(l > 0 && Math.abs(l - mong) <= 2, "Ghi công: kéo núm xuống = cuộn xuống theo tỉ lệ (" + l + " / " + mong + ")");
        con(1, x, y0 + du / 6);
        ngu(150);
        int l2 = docInt(s, c, "cmtoY");
        kiem(l2 < l, "Ghi công: kéo núm lên = cuộn lên (" + l + " -> " + l2 + ")");
        con(2, x, y0 + du / 6);
        // kéo nội dung: lên = cuộn xuống
        int xn = tx - 100, yn = ty + th / 2;
        int a = docInt(s, c, "cmtoY");
        con(0, xn, yn);
        ngu(100);
        con(1, xn, yn - 30);
        ngu(150);
        int b = docInt(s, c, "cmtoY");
        kiem(b > a, "Ghi công: kéo NỘI DUNG lên = cuộn xuống (" + a + " -> " + b + ")");
        con(2, xn, yn - 30);
    }

    // ------------------------------------------------------------------

    static void doiCo(int w, int h, int dpi) throws Exception {
        W = w;
        H = h;
        NenPc.tinhKichThuoc(w, h);
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao);
            }
        });
        float s = NenPc.hienW / (float) NenPc.rong;
        if (dpi > 0) {
            float dpL = (dpi / 160f) / s;
            game.pokemon.VungCham.MIN = (int) Math.ceil(44 * dpL);
            game.pokemon.VungCham.CACH = (int) Math.floor(8 * dpL);
        } else {
            game.pokemon.VungCham.MIN = (int) Math.ceil(32 / s);
            game.pokemon.VungCham.CACH = (int) Math.ceil(8 / s);
        }
        ngu(200);
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        game.pokemon.Bang.dungHieuUng = true;
        ra = new File(a.length > 0 ? a[0] : "kiem_thanh_cuon");
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
        for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) ngu(100);
        ngu(2500);
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                ChupTrongGame.giaLap();
            }
        });
        ngu(800);

        kiemPhepTinh();
        Object[][] co = {{"pc720", 1280, 720, 0, Boolean.TRUE}, {"dt20x9", 2400, 1080, 420, Boolean.TRUE}, {"j2me", 1280, 720, 0, Boolean.FALSE}};
        for (Object[] c : co) {
            CO = (String) c[0];
            boolean hd = (Boolean) c[4];
            htbb.pc.GiaoDienHD.bat = hd;
            htbb.pc.ChuHD.bat = hd;
            if (!hd) game.pokemon.ChuUI.bo = null;
            doiCo((Integer) c[1], (Integer) c[2], (Integer) c[3]);
            for (String k : new String[]{"cham", "chuot"}) {
                KIEU = k;
                kiemCuon();
                kiemMenu();
                kiemGhiCong();
            }
        }
        System.out.println("KiemThanhCuon: " + dat + " dat, " + hong + " hong -> " + ra.getAbsolutePath());
        System.exit(hong == 0 ? 0 : 1);
    }
}
