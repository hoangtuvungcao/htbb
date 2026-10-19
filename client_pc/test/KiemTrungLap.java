import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import javax.microedition.lcdui.NenPc;

/**
 * KIỂM "MỘT LỐI VÀO MỖI VIỆC" (0.9.5, chủ server: nút / hành động không được trùng nhau):
 *  1) lối tắt HUD (HudRong.TEN_TREN) và ô lưới Menu (ManMenu.TEN) không trùng nhau / không trùng chính nó;
 *  2) ở màn chơi và lúc mở Menu: mọi vùng chạm đã đăng ký (VungCham, lớp trên cùng) có tên riêng - không hai điều
 *     khiển cùng một việc trong cùng ngữ cảnh;
 *  3) phím PC (BanPhim.MAC_DINH): không hai hành động cùng một phím.
 *
 *   javac -cp client_pc/out/build/cls -d $O client_pc/test/ChupTrongGame.java client_pc/test/KiemTrungLap.java \
 *     server/src/com/htbb/pokemon/IconPokemon.java server/src/com/htbb/pokemon/IconPokemonBang.java
 *   java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemTrungLap <thư mục>
 * Mã thoát 1 = có trùng.
 */
public class KiemTrungLap {
    static int dat, hong;

    static void kiem(boolean ok, String ten) {
        if (ok) dat++;
        else {
            hong++;
            System.out.println("HONG: " + ten);
        }
    }

    static String[] mang(Class<?> c, String ten) throws Exception {
        Field f = c.getDeclaredField(ten);
        f.setAccessible(true);
        return (String[]) f.get(null);
    }

    static void vungTrungTen(String nhan) {
        int n = game.pokemon.VungCham.dem();
        int top = game.pokemon.VungCham.lopCaoNhat();
        HashMap<String, Integer> dem = new HashMap<String, Integer>();
        int so = 0;
        for (int i = 0; i < n; i++) {
            if (game.pokemon.VungCham.lopVung(i) != top) continue;
            String t = game.pokemon.VungCham.ten(i);
            if (t == null || t.startsWith("nen")) continue;
            so++;
            Integer c = dem.get(t);
            dem.put(t, c == null ? 1 : c + 1);
        }
        for (java.util.Map.Entry<String, Integer> e : dem.entrySet()) {
            kiem(e.getValue() == 1, nhan + ": vùng '" + e.getKey() + "' đăng ký " + e.getValue() + " lần");
        }
        kiem(so > 0, nhan + ": có vùng chạm");
    }

    public static void main(String[] a) throws Exception {
        // 2) vùng chạm ở màn chơi + Menu (đường vẽ PC thật, nhân vật giả)
        System.setProperty("java.awt.headless", "true");
        java.io.File ra = new java.io.File(a.length > 0 ? a[0] : "kiem_trung_lap");
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
        for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) Thread.sleep(100);
        Thread.sleep(2500);
        ChupTrongGame.chay(ChupTrongGame::giaLap);
        for (String man : new String[]{"the_gioi", "menu", "nv_hanh_trang", "cua_hang_thuoc"}) {
            ChupTrongGame.chay(() -> ChupTrongGame.moMan(man));
            Thread.sleep(250);
            ChupTrongGame.chay(() -> {
                Object d = game.render.Canvas.currentDialog;
                if (d instanceof game.render.screen.MsgDlg || d instanceof game.pokemon.HopThongBao) game.render.Canvas.endDlg();
            });
            ChupTrongGame.ve(1280, 720);
            ChupTrongGame.ve(1280, 720);
            vungTrungTen(man);
        }
        // 1) HUD x lưới Menu
        String[] hud = mang(game.render.screen.HudRong.class, "TEN_TREN");
        String[] luoi = mang(game.pokemon.ManMenu.class, "TEN");
        HashSet<String> da = new HashSet<String>();
        for (String s : hud) kiem(da.add(s.toLowerCase()), "HUD trùng: " + s);
        for (String s : luoi) kiem(da.add(s.toLowerCase()), "Menu trùng HUD / ô khác: " + s);
        kiem(!da.contains("kỹ năng"), "Kỹ năng là tab của màn Pokémon, không có ô riêng");
        kiem(!da.contains("khác"), "ô \"Khác\" đã đổi tên nói rõ nội dung");
        // 3) phím PC
        HashMap<Integer, String> phim = new HashMap<Integer, String>();
        for (int i = 0; i < htbb.pc.BanPhim.MAC_DINH.length; i++) {
            String cu = phim.put(htbb.pc.BanPhim.MAC_DINH[i], htbb.pc.BanPhim.TEN[i]);
            kiem(cu == null, "phím trùng: " + cu + " / " + htbb.pc.BanPhim.TEN[i]);
        }
        System.out.println("KiemTrungLap: " + dat + " dat, " + hong + " hong");
        System.exit(hong == 0 ? 0 : 1);
    }
}
