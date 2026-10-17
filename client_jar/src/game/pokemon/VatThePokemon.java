package game.pokemon;

import java.util.Hashtable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Do vat phong cach PMD thay hinh cu cua game goc - ban J2ME cua ClientUnity VatThePokemon.cs (G7.B1/B2):
 * 16 quai phan_loai vat_the (theo id template) va NPC su kien la do vat (theo idImg).
 * Anh /pk/vt_&lt;id&gt;.png, /pk/vt_npc&lt;idImg&gt;.png. Chi ve khi PokeBody.CHE_DO_POKEMON;
 * thieu tep thi tra ve false de noi goi ve kieu cu.
 */
public final class VatThePokemon {
    /** {id template quai, ti le %, so khung}. */
    public static final short[][] QUAI = {
        {83, 200, 1}, {85, 200, 1}, {86, 200, 1}, {87, 100, 1}, {88, 200, 1}, {89, 200, 1},
        {119, 200, 1}, {121, 100, 1}, {122, 200, 1}, {123, 200, 1}, {124, 200, 1}, {125, 200, 1},
        {126, 200, 1}, {127, 200, 1}, {128, 200, 1}, {131, 200, 1}
    };

    /** {idImg NPC su kien, ti le %, so khung}. */
    public static final short[][] NPC = {
        {13, 100, 6}, {15, 150, 1}, {25, 250, 1}, {26, 250, 1}
    };

    private static final Object KHONG_CO = new Object();
    private static final Hashtable cache = new Hashtable();

    /** Anh da phong to san theo tiLe: soKhung o ngang nhau. */
    private static final class Anh {
        Image img;
        int soKhung;
        int rong;
        int cao;
    }

    private VatThePokemon() {
    }

    private static short[] tim(short[][] bang, int id) {
        for (int i = 0; i < bang.length; i++) {
            if (bang[i][0] == id) {
                return bang[i];
            }
        }
        return null;
    }

    private static Anh lay(String khoa, short[] dong) {
        if (dong == null || !PokeBody.CHE_DO_POKEMON) {
            return null;
        }
        Object o = cache.get(khoa);
        if (o == KHONG_CO) {
            return null;
        }
        if (o != null) {
            return (Anh) o;
        }
        Anh a = null;
        try {
            Image img = Image.createImage(PokeBody.THU_MUC + khoa + ".png");
            int soKhung = dong[2] > 0 ? dong[2] : 1;
            int tiLe = dong[1] > 0 ? dong[1] : 100;
            int rong = img.getWidth() / soKhung;
            int cao = img.getHeight();
            if (tiLe != 100) {
                img = PokeBody.phongTo(img, rong, cao, soKhung, 1, tiLe);
                rong = rong * tiLe / 100;
                cao = cao * tiLe / 100;
            }
            a = new Anh();
            a.img = img;
            a.soKhung = soKhung;
            a.rong = rong;
            a.cao = cao;
        } catch (OutOfMemoryError e) {
            PokeBody.xoaBoDem();
            return null;
        } catch (Throwable e) {
            a = null;
        }
        cache.put(khoa, a == null ? KHONG_CO : (Object) a);
        return a;
    }

    private static Anh layQuai(int idTemplate) {
        short[] d = tim(QUAI, idTemplate);
        return d == null ? null : lay("vt_" + idTemplate, d);
    }

    private static Anh layNpc(int idImg) {
        short[] d = tim(NPC, idImg);
        return d == null ? null : lay("vt_npc" + idImg, d);
    }

    public static boolean laVatThe(int idTemplate) {
        return layQuai(idTemplate) != null;
    }

    public static boolean laNpcVatThe(int idImg) {
        return layNpc(idImg) != null;
    }

    /** Ve day giua tai (x, y) - diem chan cua actor. tick theo Canvas.gameTick (25/giay). */
    private static void ve(Graphics g, Anh a, int x, int y, int tick) {
        int f = a.soKhung > 1 ? (tick / 3 % a.soKhung) : 0;
        g.drawRegion(a.img, f * a.rong, 0, a.rong, a.cao, 0, x, y, Graphics.BOTTOM | Graphics.HCENTER);
    }

    public static boolean veQuai(Graphics g, int idTemplate, int x, int y, int tick) {
        Anh a = layQuai(idTemplate);
        if (a == null) {
            return false;
        }
        ve(g, a, x, y, tick);
        return true;
    }

    public static int caoQuai(int idTemplate) {
        Anh a = layQuai(idTemplate);
        return a == null ? 0 : a.cao;
    }

    public static boolean veNpc(Graphics g, int idImg, int x, int y, int tick) {
        Anh a = layNpc(idImg);
        if (a == null) {
            return false;
        }
        ve(g, a, x, y, tick);
        return true;
    }

    public static int caoNpc(int idImg) {
        Anh a = layNpc(idImg);
        return a == null ? 0 : a.cao;
    }

    /** Ve khung 0 thu vua o vuong canh x canh (dat sat goc), tam (xTam, yTam): khung muc tieu. */
    private static boolean veGoc(Graphics g, Anh a, int xTam, int yTam, int canh) {
        if (a == null) {
            return false;
        }
        // ban J2ME khong co ve phong to luc chay: cat giua khung cho vua o canh x canh
        int w = a.rong < canh ? a.rong : canh;
        int h = a.cao < canh ? a.cao : canh;
        g.drawRegion(a.img, (a.rong - w) / 2, a.cao - h, w, h, 0, xTam, yTam + canh / 2, Graphics.BOTTOM | Graphics.HCENTER);
        return true;
    }

    public static boolean veGocQuai(Graphics g, int idTemplate, int xTam, int yTam, int canh) {
        return veGoc(g, layQuai(idTemplate), xTam, yTam, canh);
    }

    public static boolean veGocNpc(Graphics g, int idImg, int xTam, int yTam, int canh) {
        return veGoc(g, layNpc(idImg), xTam, yTam, canh);
    }
}
