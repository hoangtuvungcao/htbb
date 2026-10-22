import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import javax.imageio.ImageIO;
import javax.microedition.lcdui.NenPc;
import javax.microedition.midlet.MIDlet;

/**
 * KIỂM THẺ POKÉDEX CÓ MÔ TẢ (loai-bo-sung, bản PC, không máy chủ): gói -121 kiểu 5 có ĐUÔI (UTF mô tả, u8 cờ shiny) đọc
 * đúng, gói cũ không đuôi vẫn đọc (mô tả rỗng); thẻ chi tiết vẽ mô tả + dòng sắc màu ở 4 cỡ màn hình, chữ trong khung.
 *   APP_IMAGE=0 sh client_pc/build.sh && javac -cp client_pc/out/build/cls -d $O client_pc/test/ChupTrongGame.java \
 *       client_pc/test/KiemPokedexMoTa.java server/src/com/htbb/pokemon/IconPokemon.java server/src/com/htbb/pokemon/IconPokemonBang.java
 *   java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemPokedexMoTa [thư mục ảnh]
 */
public class KiemPokedexMoTa {
    static int dat, hong;
    static final String MO_TA = "Rattata hệ Thường gặm mọi thứ bằng đôi răng cửa dài không ngừng mọc, sống thành bầy quanh "
            + "đồng cỏ và khu dân cư. Nó tiến hoá thành Raticate khi đủ cấp.";

    static void kiem(boolean ok, String ten) {
        if (ok) {
            dat++;
        } else {
            hong++;
            System.out.println("HONG: " + ten);
        }
    }

    static boolean daVe(String doan) {
        java.util.Set s = htbb.pc.ChuHD.ghi;
        if (s == null) {
            return false;
        }
        synchronized (s) {
            for (java.util.Iterator it = s.iterator(); it.hasNext(); ) {
                if (String.valueOf(it.next()).indexOf(doan) >= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Thân gói -121 kiểu 5 (sau byte kiểu), có / không đuôi. */
    static byte[] goiLoai(int dex, boolean duoi) throws Exception {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        DataOutputStream o = new DataOutputStream(bo);
        o.writeShort(dex);
        o.writeUTF("Rattata");
        o.writeByte(1);
        o.writeByte(1);
        o.writeUTF("Phổ biến");
        o.writeShort(400);
        o.writeShort(0);
        o.writeShort(20);
        o.writeByte(20);
        o.writeByte(0);
        o.writeUTF("");
        o.writeUTF("Raticate");
        o.writeShort(3);
        o.writeShort(41);
        o.writeByte(1);
        o.writeUTF("Tuyến 1");
        o.writeByte(6);
        for (int i = 0; i < 6; i++) {
            o.writeByte(30 + i * 10);
        }
        if (duoi) {
            o.writeUTF(MO_TA);
            o.writeByte(1);
        }
        o.flush();
        return bo.toByteArray();
    }

    static void docLoai(byte[] b) throws Exception {
        java.lang.reflect.Method m = game.pokemon.BatPokemon.class.getDeclaredMethod("docLoai", DataInputStream.class);
        m.setAccessible(true);
        m.invoke(null, new DataInputStream(new ByteArrayInputStream(b)));
    }

    public static void main(String[] a) throws Exception {
        final File ra = new File(a.length > 0 ? a[0] : "/tmp/kiem_pokedex_mo_ta");
        ra.mkdirs();
        // gói cũ (không đuôi) vẫn đọc, mô tả rỗng
        docLoai(goiLoai(19, false));
        game.pokemon.BatPokemon.Loai cu = game.pokemon.BatPokemon.loai(19);
        kiem(cu != null && cu.moTa.length() == 0 && !cu.coShiny && cu.goc != null, "goi cu khong duoi: doc duoc, mo ta rong");
        docLoai(goiLoai(19, true));
        game.pokemon.BatPokemon.Loai l = game.pokemon.BatPokemon.loai(19);
        kiem(l != null && MO_TA.equals(l.moTa) && l.coShiny && !l.batShiny && l.goc != null && l.goc[5] == 80, "goi co duoi: mo ta + co shiny");

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
        ChupTrongGame.chay(new Runnable() {
            public void run() {
                ChupTrongGame.giaLap();
            }
        });
        Thread.sleep(800);
        htbb.pc.ChuHD.ghi = java.util.Collections.synchronizedSet(new java.util.HashSet<String>());
        int[][] co = {{2400, 1080}, {1280, 720}, {1920, 1080}, {1024, 768}};
        String[] tenCo = {"dt20x9", "pc720", "pc1080", "pc4x3"};
        for (int c = 0; c < co.length; c++) {
            final int W = co[c][0], H = co[c][1];
            NenPc.tinhKichThuoc(W, H);
            htbb.pc.ChuHD.ghi.clear();
            ChupTrongGame.chay(new Runnable() {
                public void run() {
                    try {
                        game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao);
                        game.pokemon.BatPokemon.DEX.clear();
                        game.pokemon.BatPokemon.DEX.put(new Integer(19), new Integer(3));
                        game.pokemon.BatPokemon.coDex = true;
                        game.pokemon.ManPokedex.mo();
                        docLoai(goiLoai(19, true));
                        game.pokemon.ManPokedex p = game.pokemon.ManPokedex.gI();
                        java.lang.reflect.Method loc = game.pokemon.ManPokedex.class.getDeclaredMethod("loc");
                        loc.setAccessible(true);
                        java.util.Vector v = (java.util.Vector) loc.invoke(p);
                        int chon = -1;
                        for (int i = 0; i < v.size(); i++) {
                            if (((int[]) v.elementAt(i))[0] == 19) {
                                chon = i;
                            }
                        }
                        ChupTrongGame.dat(p, "chon", Integer.valueOf(chon));
                        ChupTrongGame.dat(p, "daXin", Integer.valueOf(19));
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            });
            Thread.sleep(150);
            ChupTrongGame.ve(W, H);
            BufferedImage anh = ChupTrongGame.veKiem(W, H, "KiemPokedexMoTa");
            ImageIO.write(anh, "png", new File(ra, "pokedex_mo_ta_" + tenCo[c] + ".png"));
            kiem(daVe("Rattata hệ Thường gặm"), tenCo[c] + ": the Pokedex ve mo ta");
            kiem(daVe("Có bản sắc màu ngoài tự nhiên"), tenCo[c] + ": dong ban sac mau");
            kiem(daVe("Độ hiếm: Phổ biến"), tenCo[c] + ": dong do hiem van con");
        }
        System.out.println("KiemPokedexMoTa: " + dat + " dat, " + hong + " hong -> " + ra);
        System.exit(hong == 0 ? 0 : 1);
    }
}
