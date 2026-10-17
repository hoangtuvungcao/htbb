package game.pokemon;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Hashtable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Ve than Pokemon nguyen con (thay paper doll) - ban J2ME cua ClientUnity PokeBody.cs.
 * Tai nguyen trong jar (sinh boi j2me/tools/DongGoiPokeJ2me.java tu Resources/poke/ cua KPAH):
 *   /pk/&lt;khoa&gt;.dl          du lieu khung (PokeDuLieu, dinh dang _dl ban 2 + anhDongTac[6])
 *   /pk/&lt;khoa&gt;_&lt;a&gt;.png       anh dong tac a (4 hang huong x soKhung cot), NAP LUOI tung dong tac
 *   /pk/&lt;khoa&gt;_c.png         chan dung 40x40
 * khoa = dex 4 chu so, them "s" neu shiny. Thieu tep thi moi ham ve tra ve false de noi goi
 * ve kieu cu; ket qua nap (ke ca that bai) duoc nho lai.
 *
 * Bo nho: du lieu khung (.dl, vai tram byte) giu het; ANH thi giu trong bo nho dem LRU tinh
 * theo so pixel (ToiDaPixel, mac dinh 700 000 px ~ 2,8 MB ARGB). Anh phong to/thu nho (tiLe != 100,
 * vd boss, pet 70%) va chan dung thu nho cung nam trong bo dem nay.
 */
public class PokeBody {
    public static final String THU_MUC = "/pk/";

    /**
     * Cong tac phia client cua co che_do_pokemon (Q8). Giong Unity: chi dung cho cac man TRUOC
     * khi vao game (tao/chon nhan vat, tieu de) va bang tinh phia client (NPC, pet cu, than thu,
     * vat the, tile). Trong game, Char/quai do server dieu khien qua -90/-91 (dex = 0: kieu cu).
     */
    public static boolean CHE_DO_POKEMON = true;

    /** Gioi han tong pixel anh dang giu (LRU). */
    public static int ToiDaPixel = 700000;

    private static final Object KHONG_CO = new Object();
    private static final Hashtable cacheDl = new Hashtable();
    private static final Hashtable cacheAnh = new Hashtable();
    private static Muc dauLru, cuoiLru;
    private static int tongPixel;

    public PokeDuLieu dl;
    public String khoa;

    /** Mot anh trong bo dem LRU. */
    private static final class Muc {
        String ten;
        Image img;
        int px;
        Muc truoc, sau;
    }

    public static String tenKhoa(int dex, boolean shiny) {
        StringBuffer sb = new StringBuffer(6);
        if (dex < 1000) {
            sb.append('0');
        }
        if (dex < 100) {
            sb.append('0');
        }
        if (dex < 10) {
            sb.append('0');
        }
        sb.append(dex);
        if (shiny) {
            sb.append('s');
        }
        return sb.toString();
    }

    /** Doc het mot tep trong jar (null neu khong co). */
    public static byte[] docTaiNguyen(String duongDan) {
        InputStream is = null;
        try {
            // lop cua jar (khong dung "".getClass(): tren JDK, String nap boi bootstrap khong thay classpath)
            is = new PokeBody().getClass().getResourceAsStream(duongDan);
            if (is == null) {
                return null;
            }
            ByteArrayOutputStream bo = new ByteArrayOutputStream(1024);
            byte[] buf = new byte[1024];
            int n;
            while ((n = is.read(buf)) > 0) {
                bo.write(buf, 0, n);
            }
            byte[] kq = bo.toByteArray();
            return kq.length == 0 ? null : kq;
        } catch (Throwable e) {
            return null;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Throwable e) {
                // bo qua
            }
        }
    }

    // ---------------------------------------------------------------- bo dem anh (LRU)

    private static void boKhoiLru(Muc m) {
        if (m.truoc != null) {
            m.truoc.sau = m.sau;
        } else {
            dauLru = m.sau;
        }
        if (m.sau != null) {
            m.sau.truoc = m.truoc;
        } else {
            cuoiLru = m.truoc;
        }
        m.truoc = m.sau = null;
    }

    private static void datDauLru(Muc m) {
        m.sau = dauLru;
        m.truoc = null;
        if (dauLru != null) {
            dauLru.truoc = m;
        }
        dauLru = m;
        if (cuoiLru == null) {
            cuoiLru = m;
        }
    }

    /** Anh trong bo dem (null = chua co, KHONG_CO = da thu va khong co tep). */
    private static Object layDem(String ten) {
        Object o = cacheAnh.get(ten);
        if (o instanceof Muc) {
            Muc m = (Muc) o;
            if (m != dauLru) {
                boKhoiLru(m);
                datDauLru(m);
            }
            return m.img;
        }
        return o;
    }

    private static void datDem(String ten, Image img) {
        if (img == null) {
            cacheAnh.put(ten, KHONG_CO);
            return;
        }
        Muc m = new Muc();
        m.ten = ten;
        m.img = img;
        m.px = img.getWidth() * img.getHeight();
        tongPixel += m.px;
        // bo bot anh it dung nhat, chua lai it nhat anh vua nap
        while (tongPixel > ToiDaPixel && cuoiLru != null) {
            Muc cu = cuoiLru;
            boKhoiLru(cu);
            cacheAnh.remove(cu.ten);
            tongPixel -= cu.px;
        }
        cacheAnh.put(ten, m);
        datDauLru(m);
    }

    /** Xoa het anh dang giu (vd khi doi map / thieu bo nho). */
    public static void xoaBoDem() {
        cacheAnh.clear();
        dauLru = cuoiLru = null;
        tongPixel = 0;
    }

    public static int soPixelDangGiu() {
        return tongPixel;
    }

    /** Anh bat ky trong jar qua bo dem LRU chung (vd hieu ung chieu PokeHieuUng). */
    public static Image napAnhChung(String ten) {
        return napAnh(ten);
    }

    private static Image napAnh(String ten) {
        Object o = layDem(ten);
        if (o == KHONG_CO) {
            return null;
        }
        if (o != null) {
            return (Image) o;
        }
        Image img = null;
        try {
            img = Image.createImage(ten);
        } catch (OutOfMemoryError e) {
            xoaBoDem();
            try {
                img = Image.createImage(ten);
            } catch (Throwable e2) {
                return null;   // lan sau thu lai
            }
        } catch (Throwable e) {
            img = null;
        }
        if (img != null && img.getWidth() <= 1 && img.getHeight() <= 1) {
            img = null;
        }
        datDem(ten, img);
        return img;
    }

    // ---------------------------------------------------------------- than Pokemon

    /**
     * Khoa "bong": hinh bong den mot Pokemon ("Ai day?"), dong goi boi DongGoiPokeJ2me. Dung khi
     * loai can ve khong co sprite trong jar (ban nhe bo bot loai, hoac loai chua co sprite PMD):
     * van ra dang Pokemon thay vi hinh kiem hiep cu. Jar khong co bong thi ve kieu cu nhu truoc.
     */
    public static final String KHOA_BONG = "bong";
    private static Object bong;

    private static PokeBody layBong() {
        if (bong == null) {
            PokeBody pb = napTuKhoa(KHOA_BONG);
            bong = pb == null ? KHONG_CO : (Object) pb;
        }
        return bong == KHONG_CO ? null : (PokeBody) bong;
    }

    private static PokeBody napTuFile(int dex, boolean shiny) {
        return napTuKhoa(tenKhoa(dex, shiny));
    }

    private static PokeBody napTuKhoa(String khoa) {
        PokeDuLieu dl = PokeDuLieu.doc(docTaiNguyen(THU_MUC + khoa + ".dl"));
        if (dl == null) {
            return null;
        }
        PokeBody pb = new PokeBody();
        pb.dl = dl;
        pb.khoa = khoa;
        return pb;
    }

    /** Than Pokemon (du lieu khung). Shiny thieu tep thi dung ban thuong. null neu khong co sprite. */
    public static PokeBody lay(int dex, boolean shiny) {
        if (dex <= 0) {
            return null;
        }
        Integer k = new Integer(dex * 2 + (shiny ? 1 : 0));
        Object o = cacheDl.get(k);
        if (o == KHONG_CO) {
            return null;
        }
        if (o != null) {
            return (PokeBody) o;
        }
        PokeBody pb = napTuFile(dex, shiny);
        if (pb == null && shiny) {
            pb = lay(dex, false);
        }
        if (pb == null) {
            pb = layBong();
        }
        cacheDl.put(k, pb == null ? KHONG_CO : (Object) pb);
        return pb;
    }

    public static boolean coSprite(int dex, boolean shiny) {
        return lay(dex, shiny) != null;
    }

    /** Anh cua dong tac (da phong to theo tiLe neu can). */
    private Image anhDongTac(int dongTac, PokeDongTac d, int tiLe) {
        int so = dl.anhCua(dongTac);
        String ten = THU_MUC + khoa + "_" + so + ".png";
        Image goc = napAnh(ten);
        if (goc == null && so != 0) {
            return null;
        }
        if (goc == null || tiLe == 100) {
            return goc;
        }
        String tenTiLe = ten + "@" + tiLe;
        Object o = layDem(tenTiLe);
        if (o instanceof Image) {
            return (Image) o;
        }
        Image img = null;
        try {
            img = phongTo(goc, d.rongKhung, d.caoKhung, d.soKhung, 4, tiLe);
        } catch (OutOfMemoryError e) {
            xoaBoDem();
            return null;
        } catch (Throwable e) {
            img = null;
        }
        if (img == null) {
            return null;
        }
        datDem(tenTiLe, img);
        return img;
    }

    /**
     * Phong to/thu nho anh luoi (cot x hang o, moi o rong x cao) theo tiLe% (gan nhat).
     * Tung o duoc doi rieng de o moi van thang hang: o moi = rong*tiLe/100 x cao*tiLe/100.
     */
    static Image phongTo(Image goc, int rong, int cao, int cot, int hang, int tiLe) {
        int r2 = rong * tiLe / 100, c2 = cao * tiLe / 100;
        if (r2 <= 0) {
            r2 = 1;
        }
        if (c2 <= 0) {
            c2 = 1;
        }
        int w = goc.getWidth(), h = goc.getHeight();
        int[] src = new int[w * h];
        goc.getRGB(src, 0, w, 0, 0, w, h);
        int W = r2 * cot, H = c2 * hang;
        int[] dst = new int[W * H];
        for (int y = 0; y < H; y++) {
            int o = y / c2;
            int sy = o * cao + (y - o * c2) * cao / c2;
            if (sy >= h) {
                sy = h - 1;
            }
            int hangNguon = sy * w;
            int hangDich = y * W;
            for (int x = 0; x < W; x++) {
                int k = x / r2;
                int sx = k * rong + (x - k * r2) * rong / r2;
                if (sx >= w) {
                    sx = w - 1;
                }
                dst[hangDich + x] = src[hangNguon + sx];
            }
        }
        return Image.createRGBImage(dst, W, H, true);
    }

    /**
     * Ve Pokemon sao cho diem chan (tam khung + dxChan/dyChan) trung (x, y) - (x, y) la diem chan
     * cua actor. dir: Char.DOWN/UP/LEFT/RIGHT. dongTac: PokeDuLieu.DUNG..GUC_NGA. tick: so tick
     * game (Canvas.gameTick, 25/giay) ke tu luc bat dau dong tac. tiLe: % (100 = 1x).
     * doSang: 0 = binh thuong, &gt;0 sang hon (100 = gap doi, hieu ung tien hoa).
     */
    public static boolean paint(Graphics g, int dex, boolean shiny, int x, int y, int dir, int dongTac, int tick, int tiLe, int doSang) {
        PokeBody pb = lay(dex, shiny);
        if (pb == null) {
            return false;
        }
        return pb.ve(g, x, y, dir, dongTac, tick, tiLe, doSang);
    }

    public static boolean paint(Graphics g, int dex, boolean shiny, int x, int y, int dir, int dongTac, int tick, int tiLe) {
        return paint(g, dex, shiny, x, y, dir, dongTac, tick, tiLe, 0);
    }

    public boolean ve(Graphics g, int x, int y, int dir, int dongTac, int tick, int tiLe, int doSang) {
        if (dir < 0 || dir > 3) {
            dir = 0;
        }
        if (tiLe <= 0) {
            tiLe = 100;
        }
        PokeDongTac d = dl.layDongTac(dongTac);
        Image anh = anhDongTac(dongTac, d, tiLe);
        if (anh == null && dongTac != PokeDuLieu.DUNG) {
            dongTac = PokeDuLieu.DUNG;
            d = dl.layDongTac(dongTac);
            anh = anhDongTac(dongTac, d, tiLe);
        }
        if (anh == null) {
            return false;
        }
        int f = d.khungTai(PokeDuLieu.doiTick(tick), PokeDuLieu.laDongTacLap(dongTac));
        int rong = d.rongKhung * tiLe / 100;
        int cao = d.caoKhung * tiLe / 100;
        if (rong <= 0 || cao <= 0) {
            return false;
        }
        int xNguon = d.xAtlas + f * rong;
        int yNguon = d.yAtlas + dir * cao;
        int xVe, yVe;
        if (tiLe == 100) {
            xVe = x - rong / 2 - d.dxChan[dir];
            yVe = y - cao / 2 - d.dyChan[dir];
        } else {
            xVe = x - (d.rongKhung / 2 + d.dxChan[dir]) * tiLe / 100;
            yVe = y - (d.caoKhung / 2 + d.dyChan[dir]) * tiLe / 100;
        }
        if (xNguon + rong > anh.getWidth() || yNguon + cao > anh.getHeight()) {
            return false;
        }
        if (doSang > 0) {
            veSang(g, anh, xNguon, yNguon, rong, cao, xVe, yVe, doSang);
            return true;
        }
        g.drawRegion(anh, xNguon, yNguon, rong, cao, 0, xVe, yVe, Graphics.TOP | Graphics.LEFT);
        return true;
    }

    /** Ve mot khung sang hon (mau x (100 + doSang)/100, toi da trang). Chi dung khi tien hoa. */
    private static void veSang(Graphics g, Image anh, int sx, int sy, int w, int h, int x, int y, int doSang) {
        try {
            int[] px = new int[w * h];
            anh.getRGB(px, 0, w, sx, sy, w, h);
            int heSo = 100 + doSang;
            for (int i = 0; i < px.length; i++) {
                int c = px[i];
                if ((c >>> 24) == 0) {
                    continue;
                }
                int r = ((c >> 16) & 0xFF) * heSo / 100 + doSang;
                int gg = ((c >> 8) & 0xFF) * heSo / 100 + doSang;
                int b = (c & 0xFF) * heSo / 100 + doSang;
                px[i] = (c & 0xFF000000) | ((r > 255 ? 255 : r) << 16) | ((gg > 255 ? 255 : gg) << 8) | (b > 255 ? 255 : b);
            }
            g.drawImage(Image.createRGBImage(px, w, h, true), x, y, Graphics.TOP | Graphics.LEFT);
        } catch (Throwable e) {
            g.drawRegion(anh, sx, sy, w, h, 0, x, y, Graphics.TOP | Graphics.LEFT);
        }
    }

    /** Chieu cao phan hinh nam tren diem chan (de dat ten/icon tren dau). */
    public static int caoTrenChan(int dex, boolean shiny, int tiLe) {
        PokeBody pb = lay(dex, shiny);
        if (pb == null) {
            return 0;
        }
        if (tiLe <= 0) {
            tiLe = 100;
        }
        // nhu Unity: do tu dinh KHUNG GOC (chua cat) cua dong tac dung; .dl cu thieu so nay thi
        // do tu dinh khung da cat
        if (pb.dl.caoTrenGoc > 0) {
            return pb.dl.caoTrenGoc * tiLe / 100;
        }
        PokeDongTac d = pb.dl.layDongTac(PokeDuLieu.DUNG);
        return (d.caoKhung / 2 + d.dyChan[0]) * tiLe / 100;
    }

    // ---------------------------------------------------------------- chan dung

    /** Chan dung 40x40 (null neu khong co). Shiny thieu thi dung ban thuong. */
    public static Image chanDung(int dex, boolean shiny) {
        if (dex <= 0) {
            return null;
        }
        Image img = napAnh(THU_MUC + tenKhoa(dex, shiny) + "_c.png");
        if (img == null && shiny) {
            img = chanDung(dex, false);
        }
        if (img == null && !shiny) {
            img = napAnh(THU_MUC + KHOA_BONG + "_c.png");
        }
        return img;
    }

    /** Ve chan dung voi tam (xTam, yTam), canh = kich thuoc ve (40 = goc). */
    public static boolean paintChanDung(Graphics g, int dex, boolean shiny, int xTam, int yTam, int canh) {
        Image img = chanDung(dex, shiny);
        if (img == null) {
            return false;
        }
        if (canh != img.getWidth() && canh > 0 && img.getWidth() > 0) {
            String ten = THU_MUC + tenKhoa(dex, shiny) + "_c.png#" + canh;
            Object o = layDem(ten);
            if (o instanceof Image) {
                img = (Image) o;
            } else {
                try {
                    Image nho = phongTo(img, img.getWidth(), img.getHeight(), 1, 1, canh * 100 / img.getWidth());
                    datDem(ten, nho);
                    img = nho;
                } catch (Throwable e) {
                    // ve co goc
                }
            }
        }
        g.drawImage(img, xTam, yTam, Graphics.HCENTER | Graphics.VCENTER);
        return true;
    }
}
