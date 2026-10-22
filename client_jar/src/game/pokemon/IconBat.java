package game.pokemon;

import java.util.Hashtable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * ICON PNG (pixel-art) cho Hộp PC / trao đổi / Pokédex 0.9.8: tấm /pk/bat_16.png và /pk/bat_32.png (sinh bởi
 * tools/pokemon/sinh_icon_bat.py, 10 ô). Không vẽ icon bằng lệnh: chỉ cắt ô ảnh, phóng theo cỡ (láng giềng gần nhất, giữ
 * nét pixel), nhớ đệm theo cỡ.
 */
public final class IconBat {
    public static final int SAO = 0, SAO_RONG = 1, SAC_MAU = 2, POKE = 3, GREAT = 4, ULTRA = 5, KEO = 6, TRAO_DOI = 7,
            KHOA = 8, TIEN_HOA = 9;
    static final int SO_O = 10;
    static Image t16, t32;
    static boolean daNap;
    static final Hashtable DEM = new Hashtable();
    /** số lần vẽ được (kiểm thử). */
    public static int demVe;

    private IconBat() {
    }

    static void nap() {
        if (daNap) {
            return;
        }
        daNap = true;
        t16 = PokeBody.napAnhChung("/pk/bat_16.png");
        t32 = PokeBody.napAnhChung("/pk/bat_32.png");
    }

    public static boolean coAnh() {
        nap();
        return t16 != null || t32 != null;
    }

    static Image tam(int s) {
        nap();
        if (s == 16 && t16 != null) {
            return t16;
        }
        if (s == 32 && t32 != null) {
            return t32;
        }
        Integer k = new Integer(s);
        Object o = DEM.get(k);
        if (o instanceof Image) {
            return (Image) o;
        }
        Image goc = s >= 24 && t32 != null ? t32 : t16 != null ? t16 : t32;
        if (goc == null) {
            return null;
        }
        int c = goc.getHeight();
        try {
            Image img = PokeBody.phongTo(goc, c, c, SO_O, 1, s * 100 / c);
            if (DEM.size() > 12) {
                DEM.clear();
            }
            DEM.put(k, img);
            return img;
        } catch (Throwable e) {
            return goc;
        }
    }

    /** Vẽ ô {@code o} cạnh {@code s} px, tâm (cx, cy). false = thiếu ảnh (không vẽ gì). */
    public static boolean ve(Graphics g, int o, int cx, int cy, int s) {
        if (o < 0 || o >= SO_O || s <= 0) {
            return false;
        }
        Image t = tam(s);
        if (t == null) {
            return false;
        }
        int c = t.getHeight();
        g.drawRegion(t, o * c, 0, c, c, 0, cx, cy, Graphics.HCENTER | Graphics.VCENTER);
        demVe++;
        return true;
    }

    /** Hàng sao đánh giá (0..3 sao đầy trên 3 ô) bắt đầu tại x, tâm dọc cy; trả bề rộng. */
    public static int sao(Graphics g, int soSao, int x, int cy, int s) {
        int k = Math.max(1, s / 8);
        for (int i = 0; i < 3; i++) {
            ve(g, i < soSao ? SAO : SAO_RONG, x + i * (s + k) + s / 2, cy, s);
        }
        return 3 * s + 2 * k;
    }

    /** Ô bóng theo thứ tự bóng máy chủ (1 Poké, 2 Great, 3 Ultra). */
    public static int oBong(int thuTu) {
        return thuTu == 2 ? GREAT : thuTu == 3 ? ULTRA : POKE;
    }
}
