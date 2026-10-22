import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.microedition.lcdui.NenPc;
import javax.microedition.midlet.MIDlet;

/**
 * 0.9.8 (dong bo truoc open test): client PC THAT (cung code jar / APK), headless, noi MAY CHU THAT (khong may chu gia) -
 * dang nhap tai khoan GM thu, chon Pokemon dau, roi di qua cac buoc choi (lang, dong co co quai, NPC dich vu, Ho so / Tu
 * do, hanh trang, Hop PC, chat, danh hieu, nhiem vu, ban do, pho ban / cup tu So tay) bang goi tin that; moi buoc CHUP anh
 * va chay kiem cua ChupTrongGame: chu trong khung (ky tu thieu net), icon chi PNG (VeTay.dem = 0), the gioi phu kin man,
 * nhan the gioi khong chong / trong man.
 *
 *   javac -cp client_pc/out/build/cls -d $O client_pc/test/ChupThat.java client_pc/test/ChupTrongGame.java \
 *       client_pc/test/ChupTruocGame.java server/src/com/htbb/pokemon/IconPokemon.java server/src/com/htbb/pokemon/IconPokemonBang.java
 *   cd client_pc && java -Djava.awt.headless=true -cp out/build/cls:out/build/res:$O ChupThat <cong may chu> <tai khoan GM> <thu muc anh> [rong cao]
 *
 * Tai khoan: mat khau 123456, da co Pokemon (BotChucNang tao), ten nhan vat trong sv.adminAccounts (lenh mto / slv).
 * Ket qua: anh &lt;buoc&gt;.png + dong "KET QUA: n buoc, m loi" (ma thoat 1 neu co loi).
 */
public class ChupThat {

    static final List<String> LOI = new ArrayList<String>();
    static File RA;
    static int W = 1280, H = 720;

    static void gui(final int lenh, final Object... f) throws Exception {
        KiemTaiThat.tren(new Runnable() {
            public void run() {
                try {
                    network.Message m = new network.Message((byte) lenh);
                    for (Object o : f) {
                        if (o instanceof String) m.writer().writeUTF((String) o);
                        else if (o instanceof Byte) m.writer().writeByte(((Byte) o).intValue());
                        else if (o instanceof Short) m.writer().writeShort(((Short) o).intValue());
                        else m.writer().writeInt(((Integer) o).intValue());
                    }
                    game.networklogic.GameService.gI().session.sendMessage(m);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }

    static void chat(String s) throws Exception {
        gui(27, s);
    }

    static int soBuoc;

    /** Chup buoc {@code ten}: anh + kiem chu / icon PNG; theGioi = them kiem phu kin + nhan the gioi. */
    static void chup(String ten, boolean theGioi) throws Exception {
        Thread.sleep(1500);
        KiemTaiThat.tren(new Runnable() {
            public void run() {
                ChupTrongGame.giaMayChuAnh(); // anh icon chua tai xong: nap tu server/map (dung tep may chu gui)
            }
        });
        int loiTruoc = ChupTrongGame.hong;
        BufferedImage a = ChupTrongGame.veKiem(W, H, ten);
        if (theGioi) {
            ChupTrongGame.kiemPhuKin(a, ten);
            ChupTrongGame.kiemNhanTheGioi(ten);
        }
        ImageIO.write(a, "png", new File(RA, String.format("%02d_%s.png", ++soBuoc, ten)));
        for (int i = loiTruoc; i < ChupTrongGame.hong; i++) {
            LOI.add(ten);   // chi tiet: dong "HONG:" ngay tren
        }
        System.out.println("CHUP " + ten + " (" + (ChupTrongGame.hong - loiTruoc) + " loi) " + KiemTaiThat.trangThai());
    }

    static void ve() throws Exception {
        KiemTaiThat.tren(new Runnable() {
            public void run() {
                game.render.Canvas.gameScr.switchToMe();
                game.render.Canvas.currentDialog = null;
            }
        });
    }

    static void tren(Runnable r) throws Exception {
        KiemTaiThat.tren(r);
    }

    public static void main(String[] a) throws Exception {
        final int cong = Integer.parseInt(a[0]);
        final String tk = a[1];
        RA = new File(a[2]);
        RA.mkdirs();
        if (a.length > 4) {
            W = Integer.parseInt(a[3]);
            H = Integer.parseInt(a[4]);
        }
        File du = new File(RA, "du_lieu");
        System.setProperty("java.awt.headless", "true");
        System.setProperty("htbb.duLieu", du.getAbsolutePath());
        System.setProperty("microedition.platform", "HTBB-PC/kiem");
        htbb.pc.CaiDat.thuMucDuLieu();
        htbb.pc.CaiDat.nap();
        htbb.pc.CaiDat.tatTieng = true;
        MIDlet.GHI_DE.put("HTBB-May-Chu", "127.0.0.1:" + cong);
        NenPc.tinhKichThuoc(W, H);
        tren(new Runnable() {
            public void run() {
                try {
                    MIDlet m = (MIDlet) Class.forName("game.GameMidlet").newInstance();
                    MIDlet.batDau(m);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        for (int i = 0; i < 300 && !(game.render.Canvas.currentScreen instanceof game.render.screen.LoginScr); i++) {
            if (i == 30) tren(new Runnable() { public void run() { game.render.Canvas.loginScr.switchToMe(); } });
            Thread.sleep(100);
        }
        chup("dang_nhap", false);
        tren(new Runnable() {
            public void run() {
                try {
                    game.render.screen.ServerListScr.address = new String[]{"127.0.0.1"};
                    game.render.screen.ServerListScr.port = new short[]{(short) cong};
                    game.render.screen.ServerListScr.index = 0;
                    game.render.screen.LoginScr l = game.render.Canvas.loginScr;
                    l.tfUser.setText(tk);
                    l.tfPass.setText("123456");
                    java.lang.reflect.Method m = game.render.screen.LoginScr.class.getDeclaredMethod("doLogin");
                    m.setAccessible(true);
                    m.invoke(l);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        boolean daChon = false, daChupChon = false;
        long het = System.currentTimeMillis() + 120000;
        while (System.currentTimeMillis() < het) {
            Object man = game.render.Canvas.currentScreen;
            if (!daChon && man instanceof game.render.screen.CharSelectScr) {
                if (!daChupChon) {
                    chup("chon_pokemon", false);
                    daChupChon = true;
                }
                final game.render.screen.CharSelectScr cs = (game.render.screen.CharSelectScr) man;
                final boolean[] ok = {false};
                tren(new Runnable() {
                    public void run() {
                        try {
                            java.lang.reflect.Field f = game.render.screen.CharSelectScr.class.getDeclaredField("charInfos");
                            f.setAccessible(true);
                            Object[] ci = (Object[]) f.get(cs);
                            if (ci != null && ci.length > 0 && ci[0] != null) {
                                java.lang.reflect.Field id = ci[0].getClass().getField("IDDB");
                                ok[0] = cs.tuChonLai(((Number) id.get(ci[0])).intValue());
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                });
                daChon = ok[0];
            }
            if (man == game.render.Canvas.gameScr && !game.pokemon.ManTai.dangVao && daChon && game.render.Canvas.gameScr.mainChar != null) {
                break;
            }
            Thread.sleep(50);
        }
        if (game.render.Canvas.currentScreen != game.render.Canvas.gameScr) {
            LOI.add("khong vao duoc the gioi: " + KiemTaiThat.trangThai());
            ket();
            return;
        }
        Thread.sleep(3000);
        ve();
        chup("the_gioi_vao", true);
        // ---- lang khoi dau (Pewter 0): NPC dich vu
        chat("mto 0 26 22");
        Thread.sleep(2500);
        ve();
        chup("lang_pewter", true);
        gui(23, Byte.valueOf((byte) 0));   // Chansey: menu hoi phuc / thuoc / Poke Ball & Hop PC
        chup("npc_chansey", false);
        ve();
        gui(23, Byte.valueOf((byte) 9));   // Kangaskhan: kho do / mo rong hanh trang
        chup("npc_kangaskhan", false);
        ve();
        // ---- ho so: trang bi / hanh trang / tu do (goi -116 that) / danh hieu
        tren(new Runnable() { public void run() { game.pokemon.ManNhanVat.mo(0); } });
        chup("ho_so_trang_bi", false);
        tren(new Runnable() { public void run() { game.pokemon.ManNhanVat.moHanhTrang(); } });
        chup("ho_so_hanh_trang", false);
        tren(new Runnable() { public void run() { game.pokemon.ManNhanVat.mo(4); } });
        Thread.sleep(1500);
        chup("ho_so_tu_do", false);
        tren(new Runnable() { public void run() { game.pokemon.ManDanhHieu.moTab(0); } });
        chup("danh_hieu", false);
        ve();
        tren(new Runnable() { public void run() { game.pokemon.ManHopPc.mo(false); } });
        Thread.sleep(1500);
        chup("hop_pc", false);
        ve();
        tren(new Runnable() { public void run() { game.pokemon.ManChat.mo(); } });
        Thread.sleep(800);
        chup("chat", false);
        ve();
        tren(new Runnable() { public void run() { game.pokemon.ManNhiemVu.mo(); } });
        chup("nhiem_vu", false);
        ve();
        tren(new Runnable() { public void run() { game.pokemon.ManPokedex.mo(); } });
        chup("pokedex", false);
        ve();
        tren(new Runnable() { public void run() { game.pokemon.ManMenu.mo(); } });
        chup("menu", false);
        ve();
        // ---- dong co co quai (Pallet 1), Rung Viridian 11 (cap 29), Dau truong 202
        chat("mto 1 30 30");
        Thread.sleep(3000);
        ve();
        chup("dong_pallet", true);
        chat("slv " + game.render.Canvas.gameScr.mainChar.name + " 35 0");
        Thread.sleep(1500);
        chat("mto 11 30 30");
        Thread.sleep(3000);
        ve();
        chup("rung_viridian", true);
        chat("mto 202 30 30");
        Thread.sleep(3000);
        ve();
        chup("dau_truong", true);
        chat("mto 201 30 30");
        Thread.sleep(3000);
        ve();
        chup("cao_nguyen_indigo", true);
        ket();
    }

    static void ket() {
        System.out.println("KET QUA: " + soBuoc + " buoc chup, " + LOI.size() + " loi");
        for (String l : LOI) {
            System.out.println("  LOI " + l);
        }
        System.exit(LOI.isEmpty() ? 0 : 1);
    }
}
