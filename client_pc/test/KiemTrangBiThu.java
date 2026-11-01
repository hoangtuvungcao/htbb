import java.io.File;
import javax.microedition.lcdui.NenPc;

/**
 * KIỂM BẢNG TRANG BỊ THÚ + Ô CUỐC (0.9.30, chủ dự án: "APK và JAR không có mấy cái đó" - không thấy thú đang mặc gì, không
 * tháo / thay được đồ thú, cuốc đang mặc không thấy ở đâu). Đường vẽ bản PC (cùng mã client_jar với APK / J2ME):
 * <ul>
 *   <li>TrangBiThu: đủ 8 hàng (Thú cưỡi, Đi theo, 6 ô đồ thú), món đang mặc hiện đúng ô, ở cỡ APK thật + PC + J2ME 240x320 /
 *   320x240 (ảnh); chọn ô có đồ = Xem / Tháo / Thay; Tháo gửi lệnh 29, hành trang đầy = báo, không gửi; Thay = hành trang
 *   lọc đúng loại, chọn = gửi; món cấp cao = "Cần cấp N để dùng", không gửi; chưa cưỡi = gợi ý cưỡi; phím lên / xuống;</li>
 *   <li>lối vào: nút "Trang bị thú" ở tab Thú + Trang bị › Thú cưỡi, thẻ đồ thú trong hành trang (Mặc cho thú / Đồ thú),
 *   thẻ đồ thú đang mặc (Tháo / Thay);</li>
 *   <li>ô Cuốc trên sân khấu tab Trang bị (cuốc đang mặc) -> hành trang lọc cuốc, thẻ cuốc đang mặc có Tháo.</li>
 * </ul>
 *   javac -cp $PC/build/cls -d $O client_pc/test/ChupTrongGame.java client_pc/test/KiemTrangBiThu.java \
 *     server/src/com/htbb/pokemon/IconPokemon.java server/src/com/htbb/pokemon/IconPokemonBang.java
 *   java -Djava.awt.headless=true -cp $PC/build/cls:$PC/build/res:$O KiemTrangBiThu <thư mục ảnh>
 *   (-Dhtbb.khongCham=true: máy J2ME chỉ phím - menu chữ Canvas.menu thay thẻ lệnh chạm)
 * Mã thoát 1 = hỏng.
 */
public class KiemTrangBiThu {
    static int dat, hong;
    static File ra;
    static int soAnh;
    static final boolean PHIM = Boolean.getBoolean("htbb.khongCham");

    static void kiem(boolean ok, String ten) {
        if (ok) dat++;
        else {
            hong++;
            System.out.println("HONG: " + ten);
        }
    }

    static boolean coVung(String ten) {
        int n = game.pokemon.VungCham.dem();
        for (int i = 0; i < n; i++) {
            if (ten.equals(game.pokemon.VungCham.ten(i))) return true;
        }
        return false;
    }

    static int veW = 1280, veH = 720;

    static void ve(String nhan) throws Exception {
        ChupTrongGame.ve(veW, veH);
        java.awt.image.BufferedImage a = ChupTrongGame.veKiem(veW, veH, nhan);
        if (ra != null && soAnh < 80) {
            javax.imageio.ImageIO.write(a, "png", new File(ra, String.format("%02d_%s.png", soAnh++, nhan)));
        }
    }

    static void chay(Runnable r) throws Exception {
        ChupTrongGame.chay(r);
    }

    /** Có thông báo nhanh (đang hiện hoặc đang chờ) chứa s. */
    static boolean coBao(String s) throws Exception {
        for (int i = 0; i < 8; i++) {
            String c = game.pokemon.DungDo.chuHien(i);
            if (c != null && c.indexOf(s) >= 0) return true;
        }
        java.lang.reflect.Field f = game.pokemon.DungDo.class.getDeclaredField("CHO");
        f.setAccessible(true);
        java.util.Vector v = (java.util.Vector) f.get(null);
        for (int i = 0; i < v.size(); i++) {
            java.lang.reflect.Field fc = v.elementAt(i).getClass().getDeclaredField("chu");
            fc.setAccessible(true);
            String c = (String) fc.get(v.elementAt(i));
            if (c != null && c.indexOf(s) >= 0) return true;
        }
        return false;
    }

    /** Lệnh của thẻ lệnh / menu chữ đang mở (chữ -> việc). */
    static java.util.Vector lenhMo() throws Exception {
        if (PHIM) {
            return game.render.Canvas.menu.showMenu ? game.render.Canvas.menu.menuItems : new java.util.Vector();
        }
        if (!(game.render.Canvas.currentDialog instanceof game.pokemon.MenuNguCanh)) return new java.util.Vector();
        java.lang.reflect.Field f = game.pokemon.MenuNguCanh.class.getDeclaredField("lenh");
        f.setAccessible(true);
        java.util.Vector v = (java.util.Vector) f.get(game.pokemon.MenuNguCanh.gI());
        return v == null ? new java.util.Vector() : v;
    }

    static String chuLenh() throws Exception {
        java.util.Vector v = lenhMo();
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < v.size(); i++) b.append(i > 0 ? "|" : "").append(((game.model.Command) v.elementAt(i)).caption);
        return b.toString();
    }

    static void bamLenh(final String chu) throws Exception {
        java.util.Vector v = lenhMo();
        for (int i = 0; i < v.size(); i++) {
            final game.model.Command c = (game.model.Command) v.elementAt(i);
            if (chu.equals(c.caption)) {
                chay(() -> {
                    game.render.Canvas.menu.showMenu = false;
                    c.action.perform();
                });
                return;
            }
        }
        kiem(false, "khong co lenh '" + chu + "' (co: " + chuLenh() + ")");
    }

    static void dongLenh() throws Exception {
        chay(() -> {
            game.render.Canvas.menu.showMenu = false;
            game.render.Canvas.endDlg();
        });
    }

    static game.model.ItemInInventory mon(int id, int tpl, int cap) {
        game.model.ItemInInventory it = new game.model.ItemInInventory();
        it.itemID = (short) id;
        it.idTemplate = (short) tpl;
        it.charClazz = 0;
        it.level = (byte) cap;
        it.colorName = (byte) (id % 4);
        it.isEnoughData = true;
        it.allAttribute.addElement(new game.model.InfoAttributeItem((short) 0, (short) 12));
        return it;
    }

    static game.model.ItemInInventory yen, giapA, giapB, muCao, skin, cuocMac, cuocTui;

    /** Mẫu đồ thú + cuốc thêm vào bảng mẫu giả (ChupTrongGame.giaLap) và trạng thái đang cưỡi. */
    static void dungDuLieu() {
        game.model.ItemTemplate[] cu = (game.model.ItemTemplate[]) game.render.Res.itemTemplates.elementAt(0);
        Object[][] them = {{100, 18, "Yên cương Ultra", 10}, {101, 14, "Giáp cưỡi Poké", 5}, {102, 14, "Giáp cưỡi Great", 12},
            {103, 16, "Mũ cưỡi Dusk", 60}, {104, 30, "Skin thú cưỡi Copperajah", 1}, {105, 13, "Cuốc sắt", 5}, {106, 13, "Cuốc vàng", 10}};
        int[] icon = {com.htbb.pokemon.IconTrangBi.goc(1), com.htbb.pokemon.IconTrangBi.goc(0), com.htbb.pokemon.IconTrangBi.goc(0),
            com.htbb.pokemon.IconTrangBi.goc(2), com.htbb.pokemon.IconTrangBi.goc(19), com.htbb.pokemon.IconTrangBi.goc(12),
            com.htbb.pokemon.IconTrangBi.goc(12)};
        game.model.ItemTemplate[] moi = new game.model.ItemTemplate[cu.length + them.length];
        System.arraycopy(cu, 0, moi, 0, cu.length);
        for (int i = 0; i < them.length; i++) {
            game.model.ItemTemplate t = new game.model.ItemTemplate();
            t.id = (short) ((Integer) them[i][0]).intValue();
            t.type = (short) ((Integer) them[i][1]).intValue();
            t.name = (String) them[i][2];
            t.level = (short) ((Integer) them[i][3]).intValue();
            t.idIcon = (short) icon[i];
            moi[cu.length + i] = t;
        }
        game.render.Res.itemTemplates.setElementAt(moi, 0);
        game.model.MainChar mc = game.render.Canvas.gameScr.mainChar;
        yen = mon(600, 100, 10);
        giapA = mon(601, 101, 5);
        giapB = mon(602, 102, 12);
        muCao = mon(603, 103, 60);
        skin = mon(604, 104, 1);
        cuocMac = mon(605, 105, 5);
        cuocTui = mon(606, 106, 10);
        mc.wearingAnimal = new java.util.Vector();
        mc.wearingAnimal.addElement(yen);
        mc.wearingAnimal.addElement(giapA);
        mc.wearingAnimal.addElement(skin);
        game.model.Char.inventory.addElement(giapB);
        game.model.Char.inventory.addElement(muCao);
        game.model.Char.inventory.addElement(cuocTui);
        mc.wearingItems.addElement(cuocMac);
        ChupTrongGame.datCuoi(true);
        game.pokemon.TrangThaiNguoiChoi.oDaMo = 42;
    }

    /** {rộng, cao, dpi (0 = PC, -1 = J2ME x1), tai thỏ (APK; -1 = không phải APK)} */
    static final int[][] CO = {{2400, 1080, 440, 90}, {1920, 1080, 480, 0}, {1600, 720, 320, 0}, {1280, 720, 0, -1}, {1920, 1080, 0, -1},
        {240, 320, -1, -1}, {320, 240, -1, -1}};
    static final String[] TEN_CO = {"apk1080x2400", "apk1080x1920", "apk720x1600", "pc720", "pc1080", "j2me240x320", "j2me320x240"};

    static void datCo(int c) throws Exception {
        final int W = CO[c][0], H = CO[c][1], dpi = CO[c][2];
        veW = W;
        veH = H;
        boolean j2 = dpi < 0;
        htbb.pc.GiaoDienHD.bat = !j2;
        htbb.pc.ChuHD.bat = !j2;
        if (j2) {
            game.pokemon.ChuUI.bo = null;
            ChupTrongGame.datCoJ2me(W, H);
        } else {
            NenPc.tinhKichThuoc(W, H);
        }
        chay(() -> game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao));
        game.pokemon.Bang.anPhai = 0;
        if (CO[c][3] >= 0) {
            ChupTrongGame.datCoApk(W, H, dpi, CO[c][3]);
            chay(() -> game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao));
        } else if (j2) {
            game.pokemon.VungCham.MIN = 0;
            game.pokemon.VungCham.CACH = 0;
            game.pokemon.Bang.anTrai = 0;
        } else {
            float s = NenPc.hienW / (float) NenPc.rong;
            game.pokemon.VungCham.MIN = (int) Math.ceil(32 / s);
            game.pokemon.VungCham.CACH = (int) Math.ceil(8 / s);
            game.pokemon.Bang.anTrai = 0;
        }
    }

    static void moBang() throws Exception {
        chay(() -> {
            ChupTrongGame.moMan("the_gioi");
            ChupTrongGame.datCuoi(true);
            game.pokemon.TrangBiThu.mo();
        });
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        ra = new File(a.length > 0 ? a[0] : "kiem_trang_bi_thu");
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
        chay(() -> {
            try {
                javax.microedition.midlet.MIDlet.batDau((javax.microedition.midlet.MIDlet) Class.forName("game.GameMidlet").newInstance());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) Thread.sleep(100);
        Thread.sleep(2500);
        chay(ChupTrongGame::giaLap);
        Thread.sleep(500);
        chay(KiemTrangBiThu::dungDuLieu);
        final String p = PHIM ? "phim_" : "";

        // ---------------- vẽ ở mọi cỡ
        for (int c = 0; c < CO.length; c++) {
            if (PHIM && CO[c][2] >= 0) continue;   // máy phím: chỉ cỡ J2ME
            datCo(c);
            moBang();
            kiem(game.pokemon.TrangBiThu.dangMo(), TEN_CO[c] + ": TrangBiThu.mo mở bảng");
            ve(p + "bang_" + TEN_CO[c]);
            kiem(game.pokemon.TrangBiThu.soHangVe == 8, TEN_CO[c] + ": 8 hàng (thú cưỡi, đi theo, 6 ô) - " + game.pokemon.TrangBiThu.soHangVe);
            kiem("Trang bị thú".equals(game.pokemon.TrangBiThu.tieuDeVe), TEN_CO[c] + ": tiêu đề Trang bị thú");
            if (!PHIM) {
                kiem(coVung("tbt-0") && coVung("tbt-2"), TEN_CO[c] + ": hàng chạm được (tbt-0, tbt-2)");
            }
            if (c == 0 || CO[c][2] < 0) {
                // màn Thay (hành trang lọc giáp)
                chay(() -> game.pokemon.TrangBiThu.gI().moThay(1));
                ve(p + "thay_giap_" + TEN_CO[c]);
                kiem(game.pokemon.TrangBiThu.soHangVe == 1, TEN_CO[c] + ": Thay giáp: 1 món (Giáp cưỡi Great)");
                chay(() -> game.pokemon.TrangBiThu.gI().lui());
            }
        }
        datCo(PHIM ? 5 : 0);

        // ---------------- dữ liệu ô
        kiem(game.pokemon.TrangBiThu.monO(0) == yen && game.pokemon.TrangBiThu.monO(1) == giapA && game.pokemon.TrangBiThu.monO(5) == skin,
                "món đang mặc đúng ô (Yên cương, Giáp cưỡi, Skin)");
        kiem(game.pokemon.TrangBiThu.monO(2) == null, "ô Mũ cưỡi trống");
        kiem(game.pokemon.TrangBiThu.laDoThu(giapB) && game.pokemon.TrangBiThu.laDoThu(skin) && !game.pokemon.TrangBiThu.laDoThu(cuocTui),
                "laDoThu: giáp / skin có, cuốc không");

        // ---------------- chọn ô có đồ: Xem / Tháo / Thay
        moBang();
        chay(() -> game.pokemon.TrangBiThu.gI().bam(game.pokemon.TrangBiThu.HANG_O + 1));
        kiem("Xem|Tháo|Thay".equals(chuLenh()), "ô Giáp cưỡi có đồ: menu Xem / Tháo / Thay (" + chuLenh() + ")");
        chay(() -> {
            for (int i = 0; i < 30 && game.render.Canvas.menu.showMenu; i++) game.render.Canvas.menu.updateMenu();   // menu chữ trượt vào
        });
        ve(p + "menu_o_giap");
        int gui = game.pokemon.DungDo.soGui;
        bamLenh("Tháo");
        kiem(game.pokemon.DungDo.soGui == gui + 1, "Tháo: gửi lệnh 29 (id món đang mặc)");
        // hành trang đầy: báo, không gửi
        game.pokemon.TrangThaiNguoiChoi.oDaMo = game.render.Canvas.gameScr.mainChar.FullInventory();
        chay(() -> game.pokemon.DungDo.xoaHet());
        chay(() -> game.pokemon.TrangBiThu.gI().bam(game.pokemon.TrangBiThu.HANG_O + 1));
        gui = game.pokemon.DungDo.soGui;
        bamLenh("Tháo");
        kiem(game.pokemon.DungDo.soGui == gui && coBao("Hành trang đầy"), "Tháo khi hành trang đầy: báo, không gửi");
        game.pokemon.TrangThaiNguoiChoi.oDaMo = 42;
        // Thay
        chay(() -> game.pokemon.TrangBiThu.gI().bam(game.pokemon.TrangBiThu.HANG_O + 1));
        bamLenh("Thay");
        kiem(game.pokemon.TrangBiThu.gI().oThay == 1, "Thay: hành trang lọc ô Giáp cưỡi");
        ve(p + "thay_giap");
        gui = game.pokemon.DungDo.soGui;
        chay(() -> game.pokemon.TrangBiThu.gI().bam(0));
        kiem(game.pokemon.DungDo.soGui == gui + 1 && game.pokemon.TrangBiThu.gI().oThay < 0, "Thay: chọn Giáp cưỡi Great = gửi, về danh sách ô");
        // ô trống + món cấp cao
        chay(() -> game.pokemon.TrangBiThu.gI().bam(game.pokemon.TrangBiThu.HANG_O + 2));
        kiem("Trang bị".equals(chuLenh()), "ô Mũ cưỡi trống: menu Trang bị (" + chuLenh() + ")");
        bamLenh("Trang bị");
        kiem(game.pokemon.TrangBiThu.gI().oThay == 2, "Trang bị: hành trang lọc ô Mũ cưỡi");
        ve(p + "thay_mu_cap_cao");
        gui = game.pokemon.DungDo.soGui;
        chay(() -> game.pokemon.DungDo.xoaHet());
        chay(() -> game.pokemon.TrangBiThu.gI().bam(0));
        kiem(game.pokemon.DungDo.soGui == gui && coBao("Cần cấp 60 để dùng"), "Mũ cưỡi cấp 60 (nhân vật 24): báo Cần cấp 60 để dùng, không gửi");
        chay(() -> game.pokemon.TrangBiThu.gI().lui());
        // phím lên / xuống
        chay(() -> {
            game.pokemon.TrangBiThu.gI().chon = 0;
            game.render.Canvas.keyPressed[8] = true;
            game.pokemon.TrangBiThu.gI().update();
            game.render.Canvas.keyPressed[8] = true;
            game.pokemon.TrangBiThu.gI().update();
        });
        kiem(game.pokemon.TrangBiThu.gI().chon == 2, "phím xuống x2: hàng 2 (" + game.pokemon.TrangBiThu.gI().chon + ")");
        chay(() -> {
            game.render.Canvas.keyPressed[2] = true;
            game.pokemon.TrangBiThu.gI().update();
        });
        kiem(game.pokemon.TrangBiThu.gI().chon == 1, "phím lên: hàng 1");
        // hàng thú cưỡi
        chay(() -> game.pokemon.TrangBiThu.gI().bam(game.pokemon.TrangBiThu.HANG_CUOI));
        kiem(chuLenh().startsWith("Đổi thú cưỡi") && chuLenh().indexOf("Xuống thú") > 0, "hàng Thú cưỡi: Đổi thú cưỡi / Xuống thú (" + chuLenh() + ")");
        dongLenh();
        // đóng (Esc / phím mềm phải)
        chay(() -> game.pokemon.TrangBiThu.gI().keyPress(27));
        kiem(!game.pokemon.TrangBiThu.dangMo(), "Esc: đóng bảng");

        // ---------------- chưa cưỡi
        chay(() -> {
            ChupTrongGame.moMan("the_gioi");
            ChupTrongGame.datCuoi(false);
            game.render.Canvas.gameScr.mainChar.wearingAnimal = new java.util.Vector();
            game.pokemon.TrangBiThu.mo();
        });
        ve(p + "chua_cuoi");
        kiem(game.pokemon.TrangBiThu.monO(0) == null, "chưa cưỡi: ô trống");
        chay(() -> game.pokemon.TrangBiThu.gI().bam(game.pokemon.TrangBiThu.HANG_O));
        kiem("Chọn thú cưỡi".equals(chuLenh()), "chưa cưỡi: chọn ô -> Chọn thú cưỡi (" + chuLenh() + ")");
        dongLenh();
        chay(() -> {
            game.model.MainChar mc = game.render.Canvas.gameScr.mainChar;
            mc.wearingAnimal = new java.util.Vector();
            mc.wearingAnimal.addElement(yen);
            mc.wearingAnimal.addElement(giapA);
            mc.wearingAnimal.addElement(skin);
            ChupTrongGame.datCuoi(true);
        });

        if (!PHIM) {
            // ---------------- lối vào (máy chạm)
            datCo(0);
            chay(() -> {
                ChupTrongGame.moMan("the_gioi");
                ChupTrongGame.datCuoi(true);
                game.pokemon.ManNhanVat.mo(game.pokemon.ManNhanVat.TAB_THU);
            });
            ve("tab_thu");
            kiem(coVung("trang-bi-thu"), "Menu › Pokémon › tab Thú: nút Trang bị thú");
            chay(() -> {
                game.pokemon.ManNhanVat.mo(0);
                ChupTrongGame.dat(game.pokemon.ManNhanVat.gI(), "o", 11);
            });
            ve("trang_bi_o_thu");
            kiem(coVung("trang-bi-thu"), "Trang bị › ô Thú cưỡi: nút Trang bị thú");
            chay(() -> game.pokemon.TheVatPham.mo(giapB, game.pokemon.TheVatPham.TUI, false, null));
            ve("the_do_thu_tui");
            kiem(coVung("tvp-mac-thu") && coVung("tvp-do-thu"), "thẻ đồ thú trong hành trang: Mặc cho thú + Đồ thú");
            chay(() -> game.pokemon.TheVatPham.mo(giapA, game.pokemon.TheVatPham.TUI, true, null));
            ve("the_do_thu_dang_mac");
            kiem(coVung("tvp-thao") && coVung("tvp-thay-thu"), "thẻ đồ thú đang mặc: Tháo + Thay");
            dongLenh();

            // ---------------- ô Cuốc
            for (int c : new int[]{0, 2, 4}) {
                datCo(c);
                chay(() -> {
                    ChupTrongGame.moMan("the_gioi");
                    game.pokemon.ManNhanVat.mo(0);
                });
                ve("nv_trang_bi_cuoc_" + TEN_CO[c]);
                kiem(game.pokemon.ManNhanVat.O_CUOC_VE[2] > 0 && coVung("o-Cuốc đào mỏ"), TEN_CO[c] + ": ô Cuốc trên sân khấu Trang bị");
            }
            chay(() -> ChupTrongGame.dat(game.pokemon.ManNhanVat.gI(), "o", game.pokemon.ManNhanVat.O_CUOC));
            ve("nv_o_cuoc_loc");
            chay(() -> game.pokemon.TheVatPham.mo(cuocMac, game.pokemon.TheVatPham.TUI, true, null));
            ve("the_cuoc_dang_mac");
            kiem(coVung("tvp-thao"), "thẻ cuốc đang mặc: Tháo");
            dongLenh();
            chay(() -> game.pokemon.TheVatPham.mo(cuocTui, game.pokemon.TheVatPham.TUI, false, null));
            ve("the_cuoc_tui");
            kiem(coVung("tvp-mac"), "thẻ cuốc trong hành trang: Mặc (thay cuốc cũ ở máy chủ)");
            dongLenh();
        }

        kiem(ChupTrongGame.hong == 0, "chu trong khung + icon PNG (ChupTrongGame.veKiem): " + ChupTrongGame.hong + " hong");
        System.out.println("KiemTrangBiThu" + (PHIM ? " (phim)" : "") + ": " + dat + " dat, " + hong + " hong");
        System.exit(hong == 0 ? 0 : 1);
    }
}
