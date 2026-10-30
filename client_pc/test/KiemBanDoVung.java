import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Method;
import javax.imageio.ImageIO;
import javax.microedition.lcdui.NenPc;

/**
 * KIỂM BẢN ĐỒ VÙNG (người chơi 30/10/2026: "thêm cái mở bản đồ vùng để xem cho tiện" - bản đồ lớn chỉ ở Cài đặt › Bản đồ
 * lớn, không ai tìm thấy). Chạm / phím THẬT (Canvas.pointerPressed / keyPressed, vòng game chạy):
 * <ul>
 *   <li>PC 1280x720 + APK apk1080x1920d3 / apk720x1600d2: chạm bản đồ nhỏ HUD -> ManBanDoVung; chạm điểm trên bản đồ = chọn
 *       điểm đó; chạm tab Hoenn = đổi vùng; nút X -> về màn chơi;</li>
 *   <li>Menu cá nhân › "Bản đồ vùng" -> ManBanDoVung;</li>
 *   <li>J2ME bấm phím 240x320 / 320x240 (font ảnh): phím * (không nhắm người chơi) -> ManBanDoVung, 6 = đổi vùng, 8 = chọn
 *       điểm kế, * lần nữa / phím mềm phải -> về màn chơi; bản đồ nhỏ có nhãn "* Vùng"; PC (HUD rộng) phím * vẫn là tổ
 *       đội (không mở bản đồ);</li>
 *   <li>nội dung: mở ở vùng đang đứng (Kanto, điểm Pewter vàng + nhãn), nhiệm vụ có bản đồ đích Cerulean -> dấu sao ở điểm
 *       Cerulean + nhãn + dòng "Nhiệm vụ: ..."; Johto / Hoenn đủ điểm, nhãn không ra ngoài bản đồ.</li>
 * </ul>
 * Ảnh: &lt;thư mục&gt;/bdv_*.png.
 *   APP_IMAGE=0 sh client_pc/build.sh && O=$(mktemp -d)
 *   javac -cp client_pc/out/build/cls -d $O client_pc/test/ChupTrongGame.java client_pc/test/KiemBanDoVung.java \
 *     server/src/com/htbb/pokemon/IconPokemon.java server/src/com/htbb/pokemon/IconPokemonBang.java
 *   java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemBanDoVung &lt;thư mục ảnh&gt;
 * Mã thoát 1 = có phép kiểm hỏng.
 */
public class KiemBanDoVung {
    static int dat, hong;
    static String co = "";

    static void kiem(boolean ok, String ten) {
        if (ok) {
            dat++;
        } else {
            hong++;
            System.out.println("HONG [" + co + "]: " + ten);
        }
    }

    static void ngu(long ms) throws Exception {
        Thread.sleep(ms);
    }

    static boolean cho(java.util.function.BooleanSupplier f) throws Exception {
        for (int i = 0; i < 40; i++) {
            if (f.getAsBoolean()) return true;
            ngu(50);
        }
        return f.getAsBoolean();
    }

    static void ve() throws Exception {
        ChupTrongGame.chay(ChupTrongGame::veThang);
    }

    static void goi(String ten, int x, int y) {
        try {
            Method m = game.render.Canvas.class.getDeclaredMethod(ten, int.class, int.class);
            m.setAccessible(true);
            int k = game.render.Canvas.heSo;
            m.invoke(game.render.Canvas.instance, x * k + k / 2, y * k + k / 2);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    static void phim(int ma) throws Exception {
        ChupTrongGame.chay(() -> {
            try {
                Method m = game.render.Canvas.class.getDeclaredMethod("keyPressed", int.class);
                m.setAccessible(true);
                m.invoke(game.render.Canvas.instance, ma);
                Method r = game.render.Canvas.class.getDeclaredMethod("keyReleased", int.class);
                r.setAccessible(true);
                r.invoke(game.render.Canvas.instance, ma);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        doiNhip();
    }

    static void doiNhip() throws Exception {
        final int n0 = game.pokemon.KhoiDong.soNhip;
        ngu(120);
        for (int i = 0; i < 60 && game.pokemon.KhoiDong.soNhip - n0 < 3; i++) ngu(50);
        ve();
    }

    /** Chạm như ngón tay: nhấn, giữ, thả, chờ ba nhịp logic. */
    static void cham(final int x, final int y) throws Exception {
        ve();
        ChupTrongGame.chay(() -> goi("pointerPressed", x, y));
        ngu(90);
        ChupTrongGame.chay(() -> goi("pointerReleased", x, y));
        doiNhip();
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

    static boolean moMap() {
        return game.render.Canvas.currentScreen instanceof game.pokemon.ManBanDoVung;
    }

    static boolean theGioi() {
        return game.render.Canvas.currentScreen == game.render.Canvas.gameScr;
    }

    static int tab() throws Exception {
        java.lang.reflect.Field f = game.pokemon.ManBang.class.getDeclaredField("tab");
        f.setAccessible(true);
        return f.getInt(game.pokemon.ManBanDoVung.gI());
    }

    static int chon() throws Exception {
        java.lang.reflect.Field f = game.pokemon.ManBanDoVung.class.getDeclaredField("chon");
        f.setAccessible(true);
        return f.getInt(game.pokemon.ManBanDoVung.gI());
    }

    static void veTheGioi() throws Exception {
        ChupTrongGame.chay(() -> {
            game.render.Canvas.endDlg();
            game.render.Canvas.menu.showMenu = false;
            game.render.Canvas.gameScr.focusedActor = null;
            game.render.Canvas.gameScr.switchToMe();
        });
        ve();
    }

    static void chup(File ra, String ten) throws Exception {
        ChupTrongGame.chay(() -> {
        });
        game.pokemon.GhiChu.xoa();
        game.pokemon.GhiChu.bat = true;
        BufferedImage a = ChupTrongGame.ve(NenPc.thatW, NenPc.thatH);
        game.pokemon.GhiChu.bat = false;
        ImageIO.write(a, "png", new File(ra, "bdv_" + ten + ".png"));
    }

    /** Nội dung bản đồ vừa vẽ: điểm đứng / sao / nhãn, mọi điểm trong khung. */
    static void kiemNoiDung(int vung, File ra) throws Exception {
        String n = co + " vung " + vung;
        int sd = game.pokemon.BanDoVung.soDiem(vung);
        kiem(game.pokemon.BanDoVung.soVe == sd, n + ": ve " + game.pokemon.BanDoVung.soVe + " / " + sd + " diem");
        int[] k = game.pokemon.BanDoVung.KHUNG;
        for (int i = 0; i < game.pokemon.BanDoVung.soVe; i++) {
            int x = game.pokemon.BanDoVung.VX[i], y = game.pokemon.BanDoVung.VY[i];
            kiem(x >= k[0] && x < k[0] + k[2] && y >= k[1] && y < k[1] + k[3], n + ": diem " + i + " trong khung");
        }
        int nhan = 0;
        for (int i = 0; i < sd; i++) if (game.pokemon.BanDoVung.CO_NHAN[i]) nhan++;
        // PC / APK: >= 2/3 điểm có nhãn (Kanto / Johto: gần hết; Hoenn 29 điểm dày); J2ME 240 px: >= 1/6 (Hoenn) - điểm không
        // nhãn đọc tên ở dòng dưới khi chạm / chọn bằng 2 / 8
        boolean j2me = game.pokemon.ChuUI.bo == null;
        kiem(j2me ? nhan * 6 >= sd : nhan * 3 >= sd * 2, n + ": nhan ten " + nhan + " / " + sd);
        if (vung != 2) kiem(j2me ? nhan * 2 >= sd : nhan * 10 >= sd * 9, n + ": Kanto / Johto nhan ten " + nhan + " / " + sd);
        if (vung == 0) {
            kiem(game.pokemon.BanDoVung.dungVe == 0, n + ": diem dang dung = Pewter (" + game.pokemon.BanDoVung.dungVe + ")");
            kiem(game.pokemon.BanDoVung.dungVe >= 0 && game.pokemon.BanDoVung.CO_NHAN[game.pokemon.BanDoVung.dungVe], n + ": diem dang dung co nhan");
            int ce = game.pokemon.BanDoVung.diem(0, 4);
            kiem(game.pokemon.BanDoVung.saoVe == ce && ce >= 0, n + ": dau sao nhiem vu o Cerulean (" + game.pokemon.BanDoVung.saoVe + ")");
            kiem(ce >= 0 && game.pokemon.BanDoVung.CO_NHAN[ce], n + ": diem nhiem vu co nhan");
            kiem(coChu("Nhiệm vụ: Cerulean"), n + ": dong 'Nhiem vu: Cerulean'");
            kiem(coChu("Bạn đang ở: Pewter"), n + ": dong 'Ban dang o: Pewter'");
        } else {
            kiem(game.pokemon.BanDoVung.dungVe < 0 && game.pokemon.BanDoVung.saoVe < 0, n + ": vung khac: khong diem dung / sao");
            kiem(coChu("Nhiệm vụ: Cerulean (Kanto)"), n + ": dong nhiem vu ghi vung Kanto");
        }
        ChupTrongGame.kiemChu(n);
    }

    static boolean coChu(String s) {
        for (int i = 0; i < game.pokemon.GhiChu.soChu; i++) {
            String c = game.pokemon.GhiChu.CS[i];
            if (c != null && c.contains(s)) return true;
        }
        return false;
    }

    static void veGhi() throws Exception {
        game.pokemon.GhiChu.xoa();
        game.pokemon.GhiChu.bat = true;
        ve();
        game.pokemon.GhiChu.bat = false;
    }

    /** Màn cảm ứng: chạm bản đồ nhỏ, chạm điểm, tab, X; Menu › Bản đồ vùng. */
    static void kiemCham(File ra) throws Exception {
        veTheGioi();
        int[] mm = vung("hud-ban-do-nho");
        kiem(mm != null, "ban do nho HUD la vung cham hud-ban-do-nho");
        kiem(coChuVe("Bản đồ vùng"), "ban do nho co nhan 'Ban do vung'");
        if (mm == null) return;
        cham(mm[0] + mm[2] / 2, mm[1] + mm[3] / 2);
        kiem(cho(KiemBanDoVung::moMap), "cham ban do nho -> Ban do vung (" + game.render.Canvas.currentScreen + ")");
        if (!moMap()) return;
        kiem(tab() == 0, "mo o vung dang dung (Kanto): tab " + tab());
        veGhi();
        kiemNoiDung(0, ra);
        chup(ra, co + "_kanto");
        // chạm điểm Celadon (chỉ số 9) = chọn
        int i = 9;
        cham(game.pokemon.BanDoVung.VX[i], game.pokemon.BanDoVung.VY[i]);
        kiem(moMap() && chon() == i, "cham diem Celadon -> chon " + chon());
        veGhi();
        kiem(coChu("Celadon"), "dong thong tin ghi Celadon");
        // tab Hoenn
        kiem(chamVung("tab-2"), "co tab Hoenn");
        kiem(tab() == 2, "cham tab Hoenn -> vung Hoenn (" + tab() + ")");
        veGhi();
        kiemNoiDung(2, ra);
        chup(ra, co + "_hoenn");
        kiem(chamVung("tab-1"), "co tab Johto");
        veGhi();
        kiemNoiDung(1, ra);
        chup(ra, co + "_johto");
        // đóng (X)
        kiem(chamVung("dong"), "co nut dong");
        kiem(cho(KiemBanDoVung::theGioi), "nut X -> ve man choi (" + game.render.Canvas.currentScreen + ")");
        // Menu › Bản đồ vùng
        veTheGioi();
        ChupTrongGame.chay(game.pokemon.ManMenu::mo);
        ngu(350);
        kiem(chamVung("menu-Bản đồ vùng"), "Menu co o 'Ban do vung'");
        kiem(cho(KiemBanDoVung::moMap), "Menu > Ban do vung -> Ban do vung (" + game.render.Canvas.currentScreen + ")");
        // Esc / phím mềm phải đóng
        phim(-7);
        kiem(cho(KiemBanDoVung::theGioi), "phim mem phai / Esc -> ve man choi (" + game.render.Canvas.currentScreen + ")");
        // HUD rộng: phím * vẫn là tổ đội (không mở bản đồ)
        veTheGioi();
        phim(42);
        kiem(!moMap(), "HUD rong: phim * khong mo ban do (to doi)");
        veTheGioi();
    }

    static boolean coChuVe(String s) throws Exception {
        veGhi();
        return coChu(s);
    }

    /** Máy J2ME bấm phím. */
    static void kiemPhim(File ra) throws Exception {
        veTheGioi();
        kiem(!game.render.screen.HudRong.bat(), "J2ME " + co + ": khong HUD rong");
        veGhi();
        kiem(coChu("* Vùng"), "ban do nho J2ME co nhan '* Vung'");
        chup(ra, co + "_the_gioi");
        phim(42);
        kiem(cho(KiemBanDoVung::moMap), "phim * -> Ban do vung (" + game.render.Canvas.currentScreen + ")");
        if (!moMap()) return;
        veGhi();
        kiemNoiDung(0, ra);
        kiem(coChu("4 6: vùng"), "dong huong dan phim");
        chup(ra, co + "_kanto");
        int c0 = chon();
        phim(56);   // 8
        kiem(chon() == (c0 + 1) % game.pokemon.BanDoVung.soDiem(0), "phim 8 -> chon diem ke (" + c0 + " -> " + chon() + ")");
        veGhi();
        chup(ra, co + "_kanto_chon");
        phim(54);   // 6
        kiem(tab() == 1, "phim 6 -> Johto (" + tab() + ")");
        veGhi();
        kiemNoiDung(1, ra);
        chup(ra, co + "_johto");
        phim(54);
        veGhi();
        kiemNoiDung(2, ra);
        chup(ra, co + "_hoenn");
        phim(42);
        kiem(cho(KiemBanDoVung::theGioi), "phim * lan nua -> ve man choi (" + game.render.Canvas.currentScreen + ")");
        phim(42);
        kiem(cho(KiemBanDoVung::moMap), "phim * mo lai");
        phim(-7);
        kiem(cho(KiemBanDoVung::theGioi), "phim mem phai -> ve man choi");
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        File ra = new File(a.length > 0 ? a[0] : "kiem_ban_do_vung");
        ra.mkdirs();
        game.pokemon.Bang.dungHieuUng = true;
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
        for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) ngu(100);
        ngu(2500);
        ChupTrongGame.chay(ChupTrongGame::giaLap);
        ngu(800);
        game.networklogic.GameService.gI().setSession(new network.ISession() {
            public boolean isConnected() {
                return true;
            }

            public void setHandler(network.IMessageHandler h) {
            }

            public void connect(String s, String s2) {
            }

            public void sendMessage(network.Message m) {
            }

            public void close() {
            }
        });
        ChupTrongGame.chay(() -> {
            ChupTrongGame.moMan("the_gioi");
            game.render.Canvas.endDlg();
            // nhiệm vụ có bản đồ đích Cerulean (id 4) - như đuôi gói -64 của máy chủ
            game.model.Quest q = new game.model.Quest(14);
            q.stateQuest = 2;
            q.dichMap = 4;
            q.dichNoi = "Cerulean";
            game.render.screen.GameScr.mainQuest = q;
            game.render.screen.GameScr.mapNap = 0;
        });
        // ---- cảm ứng: PC + APK
        co = "pc1280x720";
        game.pokemon.VungCham.MIN = 32 / 2;
        game.pokemon.VungCham.CACH = 4;
        ChupTrongGame.chay(() -> game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao));
        kiemCham(ra);
        int[][] apk = {{1920, 1080, 480, 0}, {1600, 720, 320, 0}};
        String[] tenApk = {"apk1080x1920d3", "apk720x1600d2"};
        for (int c = 0; c < apk.length; c++) {
            co = tenApk[c];
            ChupTrongGame.datCoApk(apk[c][0], apk[c][1], apk[c][2], apk[c][3]);
            ChupTrongGame.chay(() -> game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao));
            kiemCham(ra);
        }
        game.pokemon.Bang.anTrai = 0;
        game.pokemon.Bang.anPhai = 0;
        // ---- J2ME bấm phím (font ảnh)
        htbb.pc.GiaoDienHD.bat = false;
        htbb.pc.ChuHD.bat = false;
        game.pokemon.ChuUI.bo = null;
        game.pokemon.VungCham.MIN = 0;
        game.pokemon.VungCham.CACH = 0;
        int[][] j2 = {{240, 320}, {320, 240}};
        for (int[] c : j2) {
            co = "j2me" + c[0] + "x" + c[1];
            ChupTrongGame.datCoJ2me(c[0], c[1]);
            ChupTrongGame.chay(() -> game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao));
            kiemPhim(ra);
        }
        System.out.println("=== KiemBanDoVung: DAT " + dat + ", HONG " + hong + " ===");
        System.exit(hong == 0 ? 0 : 1);
    }
}
