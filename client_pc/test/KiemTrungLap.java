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
 *  4) 0.9.9: Menu LUÔN có viên "Tự động đánh" (0.9.6.1-0.9.8 bỏ trên HUD màn rộng -> người chơi báo mất menu AUTO); menu
 *     chữ mở từ "Tiện ích & PK" (PauseMenu) không lặp mục đã có ở Menu / HUD, không hàng rỗng, chọn dòng hiện -> đúng mục.
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

    static Object goi(Object o, String ten, Class<?>[] kieu, Object... thamSo) throws Exception {
        java.lang.reflect.Method m = (o instanceof Class ? (Class<?>) o : o.getClass()).getDeclaredMethod(ten, kieu);
        m.setAccessible(true);
        return m.invoke(o instanceof Class ? null : o, thamSo);
    }

    static Object truong(Object o, String ten) throws Exception {
        Field f = o.getClass().getDeclaredField(ten);
        f.setAccessible(true);
        return f.get(o);
    }

    /** Mở "Tiện ích & PK" như người chơi (ManMenu.lam(I_KHAC)) rồi đọc dòng hiện của PauseMenu. */
    static void kiemPauseMenu(String[] tenMenu) throws Exception {
        final Object[] pm = new Object[1];
        ChupTrongGame.chay(() -> {
            try {
                game.render.Canvas.gameScr.mainChar.hp = Math.max(1, game.render.Canvas.gameScr.mainChar.hp);
                goi(game.pokemon.ManMenu.class, "lam", new Class<?>[]{int.class}, 9);   // I_KHAC
                pm[0] = game.render.Canvas.currentScreen;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        kiem(pm[0] instanceof game.render.screen.PauseMenu, "Tiện ích & PK mở PauseMenu (" + pm[0] + ")");
        if (!(pm[0] instanceof game.render.screen.PauseMenu)) return;
        Object p = pm[0];
        kiem(!game.render.screen.PauseMenu.moTuMenu, "cờ moTuMenu đã trả về false");
        String[][] ten = (String[][]) truong(p, "name");
        int[] hien = (int[]) truong(p, "hien");
        kiem(((Integer) truong(p, "Limit")) == hien.length, "Limit = số dòng hiện");
        // mục đã có lối vào ở Menu GO / HUD: không được hiện trong menu chữ
        String[] trung = {"Tự động đánh", "Bật/tắt auto đánh", "Bản đồ lớn", "Hành trang", "Kỹ năng", "Tiềm năng", "Trang bị",
            "Thông tin", "Trang bị thú", "Túi sinh thái", "Ngoại hình", "Nhiệm vụ", "Cửa hàng", "Tin nhắn", "Bạn bè",
            "Top nhà vô địch", "Top đại gia", "Bảng Top", "Cài đặt"};
        HashSet<String> cam = new HashSet<String>();
        for (String t : trung) cam.add(t.toLowerCase());
        for (String t : tenMenu) cam.add(t.toLowerCase());
        HashSet<String> daThay = new HashSet<String>();
        for (int v = 0; v < hien.length; v++) {
            int g = hien[v];
            String hang = ten[g][0];
            kiem(!cam.contains(String.valueOf(hang).toLowerCase()), "menu chữ: hàng '" + hang + "' trùng Menu / HUD");
            kiem(daThay.add("hang:" + hang), "menu chữ: hàng '" + hang + "' lặp");
            if (ten[g].length > 1) {
                int con = 0;
                for (int j = 0; j < ten[g].length - 1; j++) {
                    if ((Boolean) goi(p, "an", new Class<?>[]{int.class, int.class}, g, j)) continue;
                    con++;
                    String muc = ten[g][j + 1];
                    kiem(!cam.contains(String.valueOf(muc).toLowerCase()), "menu chữ: mục '" + hang + " > " + muc + "' trùng Menu / HUD");
                }
                kiem(con > 0, "menu chữ: hàng '" + hang + "' không rỗng");
            }
        }
        kiem(hien.length >= 3 && hien.length < 9, "menu chữ gọn: " + hien.length + " hàng (đủ là 9)");
        // chọn dòng hiện thứ v -> vào đúng hàng gốc hien[v] (doSelect đổi chỉ số), rồi Đóng về đúng dòng
        for (int v = 0; v < hien.length; v++) {
            final int vv = v, g = hien[v];
            if (ten[g].length <= 1) continue;
            final Object[] kq = new Object[3];
            ChupTrongGame.chay(() -> {
                try {
                    Field fs = p.getClass().getDeclaredField("selected");
                    fs.setAccessible(true);
                    fs.setInt(p, vv);
                    goi(p, "doSelect", new Class<?>[0]);
                    kq[0] = truong(p, "index");
                    kq[1] = truong(p, "Limit");
                    ((game.render.screen.PauseMenu) p).right.action.perform();
                    kq[2] = truong(p, "selected");
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
            kiem(Integer.valueOf(g).equals(kq[0]), "menu chữ: dòng " + v + " vào hàng gốc " + g + " (" + kq[0] + ")");
            kiem(((int[]) truong(p, "hien")).length == hien.length && Integer.valueOf(v).equals(kq[2]), "menu chữ: Đóng về dòng " + v + " (" + kq[2] + ")");
        }
        ChupTrongGame.chay(() -> game.render.Canvas.gameScr.switchToMe());
        // mở bằng phím mềm (không qua Menu GO): đủ 9 hàng như cũ
        ChupTrongGame.chay(() -> game.render.Canvas.gameScr.left.action.perform());
        Object p2 = game.render.Canvas.currentScreen;
        kiem(p2 instanceof game.render.screen.PauseMenu && ((Integer) truong(p2, "Limit")) == 9, "phím mềm trái: menu chữ đủ 9 hàng");
        ChupTrongGame.chay(() -> game.render.Canvas.gameScr.switchToMe());
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
        // 4) Menu GO trên HUD màn rộng + menu chữ "Tiện ích & PK"
        kiem(game.render.screen.HudRong.bat(), "PC 1280x720: HUD màn rộng bật");
        int[] phu = game.pokemon.ManMenu.phu();
        String[] tenMenu = mang(game.pokemon.ManMenu.class, "TEN");
        // 0.9.9 (chủ game: "menu AUTO bị mất" ở 0.9.8): Menu LUÔN có viên "Tự động đánh", cả khi HUD có viên AUTO + bánh răng
        boolean coVien = false;
        for (int i : phu) coVien |= "Tự động đánh".equals(tenMenu[i]);
        kiem(coVien, "Menu (HUD rộng) có viên Tự động đánh");
        ChupTrongGame.chay(() -> ChupTrongGame.moMan("menu"));
        ChupTrongGame.ve(1280, 720);
        boolean coTuDong = false;
        for (int i = 0; i < game.pokemon.VungCham.dem(); i++) {
            if ("menu-Tự động đánh".equals(game.pokemon.VungCham.ten(i))) coTuDong = true;
        }
        kiem(coTuDong, "Menu: có vùng chạm menu-Tự động đánh");
        kiemPauseMenu(tenMenu);
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
