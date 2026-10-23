package game.pokemon;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Ve than Pokemon nguyen con (thay paper doll) - ban J2ME cua Unity PokeBody.cs.
 * Tai nguyen trong jar (sinh boi client_jar/tools/DongGoiPokeJ2me.java tu Resources/poke/ cua HTBB):
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
        if (img == null) {
            // 1.0: loai khong co trong jar -> anh trong goi da tai (KhoSprite), khong xin may chu o day
            img = anhTuKho(ten);
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

    /** Than nay la ban sac mau that (khoa "<NNNN>s"), khong phai ban thuong ve thay. */
    boolean laShiny() {
        return this.khoa != null && this.khoa.length() == 5 && this.khoa.charAt(4) == 's';
    }

    private static PokeBody napTuFile(int dex, boolean shiny) {
        return napTuKhoa(tenKhoa(dex, shiny));
    }

    private static PokeBody napTuKhoa(String khoa) {
        byte[] b = docTaiNguyen(THU_MUC + khoa + ".dl");
        if (b == null && (khoa.length() == 4 || khoa.length() == 5 && khoa.charAt(4) == 's')) {
            b = KhoSprite.docTep(khoa, khoa + ".dl");   // 1.0: goi sprite tai theo nhu cau (ca ban sac mau "0016s")
        }
        PokeDuLieu dl = PokeDuLieu.doc(b);
        if (dl == null) {
            return null;
        }
        PokeBody pb = new PokeBody();
        pb.dl = dl;
        pb.khoa = khoa;
        return pb;
    }

    /** Kiem lag 10/2026: lay() chay moi khung cho moi Pokemon tren man - dung lai khoa Integer thay vi tao moi. */
    private static final Integer[] KHOA_DEX = new Integer[2 * 1100];

    private static Integer khoaDex(int v) {
        if (v < 0 || v >= KHOA_DEX.length) {
            return new Integer(v);
        }
        Integer k = KHOA_DEX[v];
        if (k == null) {
            k = new Integer(v);
            KHOA_DEX[v] = k;
        }
        return k;
    }

    /** Than Pokemon (du lieu khung). Shiny thieu tep thi dung ban thuong. null neu khong co sprite. */
    public static PokeBody lay(int dex, boolean shiny) {
        if (dex <= 0) {
            return null;
        }
        xuLyBoLoai();
        Integer k = khoaDex(dex * 2 + (shiny ? 1 : 0));
        Object o = cacheDl.get(k);
        if (o == KHONG_CO) {
            return null;
        }
        if (o != null) {
            PokeBody pb = (PokeBody) o;
            if (!shiny && pb.khoa == KHOA_BONG) {
                KhoSprite.xin(dex);   // dang ve bong: nhac xin (no-op neu da xin / thieu / man Pokedex)
            } else if (shiny && !pb.laShiny()) {
                KhoSprite.xin(dex, true);   // dang ve ban thuong thay cho shiny: nhac xin goi sac mau
            }
            return pb;
        }
        PokeBody pb = napTuFile(dex, shiny);
        if (pb == null && shiny) {
            // jar / kho chua co ban sac mau: xin goi "<NNNN>s" (may chu bao thieu = loai chua co shiny), tam ve ban thuong
            KhoSprite.xin(dex, true);
            pb = lay(dex, false);
        }
        if (pb == null) {
            if (!shiny) {
                KhoSprite.xin(dex);   // 1.0: jar khong co loai nay -> xin may chu, tam ve bong den
            }
            pb = layBong();
        }
        cacheDl.put(k, pb == null ? KHONG_CO : (Object) pb);
        return pb;
    }

    /** Anh trong goi sprite da tai ("/pk/0016_2.png" -> KhoSprite tep "0016_2.png"). */
    private static Image anhTuKho(String ten) {
        String khoa = GoiSprite.khoaTuDuongDan(ten);
        if (khoa == null) {
            return null;
        }
        byte[] b = KhoSprite.docTep(khoa, ten.substring(THU_MUC.length()));
        if (b == null) {
            return null;
        }
        try {
            return Image.createImage(b, 0, b.length);
        } catch (OutOfMemoryError e) {
            xoaBoDem();
            return null;
        } catch (Throwable e) {
            return null;
        }
    }

    private static final Hashtable trongJar = new Hashtable();

    /** Jar co sprite thuong cua loai nay (tep .dl). Nho ket qua. */
    public static boolean coTrongJar(int dex) {
        return coTrongJar(dex, false);
    }

    /** Jar co sprite ban thuong / ban sac mau ("<NNNN>s.dl") cua loai nay. Nho ket qua. */
    public static boolean coTrongJar(int dex, boolean shiny) {
        Integer k = new Integer(shiny ? -dex : dex);
        Object o = trongJar.get(k);
        if (o == null) {
            InputStream is = null;
            try {
                is = new PokeBody().getClass().getResourceAsStream(THU_MUC + tenKhoa(dex, shiny) + ".dl");
            } catch (Throwable e) {
                is = null;
            }
            o = is != null ? Boolean.TRUE : Boolean.FALSE;
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Throwable e) {
                // bo qua
            }
            trongJar.put(k, o);
        }
        return o == Boolean.TRUE;
    }

    /**
     * Goi sprite loai {@code dex} vua tai xong (luong mang): xep hang de luong VE quen than / anh da ghim bong den
     * hoac "khong co" (LRU anh chi sua tren luong ve).
     */
    public static void boLoai(int dex) {
        synchronized (choBo) {
            choBo.addElement(new Integer(dex));
        }
    }

    private static final Vector choBo = new Vector();

    /** Luong ve: ap cac loai vua tai xong (goi dau lay()). */
    static void xuLyBoLoai() {
        if (choBo.isEmpty()) {
            return;
        }
        int[] ds;
        synchronized (choBo) {
            ds = new int[choBo.size()];
            for (int i = 0; i < ds.length; i++) {
                ds[i] = ((Integer) choBo.elementAt(i)).intValue();
            }
            choBo.removeAllElements();
        }
        for (int i = 0; i < ds.length; i++) {
            boLoaiNgay(ds[i]);
        }
    }

    static void boLoaiNgay(int dex) {
        cacheDl.remove(new Integer(dex * 2));
        cacheDl.remove(new Integer(dex * 2 + 1));
        String khoa = tenKhoa(dex, false);
        String p1 = THU_MUC + khoa;
        String p2 = THU_MUC + khoa + "s";
        String h1 = "hh/" + dex + "/";
        String h2 = "hh/" + dex + "s/";
        {
            Vector bo = new Vector();
            Enumeration e = cacheAnh.keys();
            while (e.hasMoreElements()) {
                String t = (String) e.nextElement();
                if (t.startsWith(p1) || t.startsWith(p2) || t.startsWith(h1) || t.startsWith(h2)) {
                    bo.addElement(t);
                }
            }
            for (int i = 0; i < bo.size(); i++) {
                Object o = cacheAnh.remove(bo.elementAt(i));
                if (o instanceof Muc) {
                    Muc m = (Muc) o;
                    boKhoiLru(m);
                    tongPixel -= m.px;
                }
            }
        }
    }

    public static boolean coSprite(int dex, boolean shiny) {
        return lay(dex, shiny) != null;
    }

    // Kiem lag 10/2026: ten tep anh dong tac duoc ghep chuoi MOI LAN ve (moi Pokemon tren man, moi khung).
    // Nho ten theo so anh (khoa co dinh cho moi PokeBody); tra cuu bo dem LRU van y nhu cu.
    private String[] tenSo;
    private String[] tenSoTiLe;
    private int[] tiLeSo;

    private String tenAnh(int so) {
        if (so < 0 || so > 255) {
            return THU_MUC + khoa + "_" + so + ".png";
        }
        if (tenSo == null || so >= tenSo.length) {
            String[] m = new String[so + 4];
            if (tenSo != null) {
                System.arraycopy(tenSo, 0, m, 0, tenSo.length);
            }
            tenSo = m;
        }
        String t = tenSo[so];
        if (t == null) {
            t = THU_MUC + khoa + "_" + so + ".png";
            tenSo[so] = t;
        }
        return t;
    }

    private String tenAnhTiLe(int so, String ten, int tiLe) {
        if (so < 0 || so > 255) {
            return ten + "@" + tiLe;
        }
        if (tenSoTiLe == null || so >= tenSoTiLe.length) {
            int n = Math.max(so + 4, tenSo != null ? tenSo.length : 0);
            String[] m = new String[n];
            int[] q = new int[n];
            if (tenSoTiLe != null) {
                System.arraycopy(tenSoTiLe, 0, m, 0, tenSoTiLe.length);
                System.arraycopy(tiLeSo, 0, q, 0, tiLeSo.length);
            }
            tenSoTiLe = m;
            tiLeSo = q;
        }
        if (tenSoTiLe[so] == null || tiLeSo[so] != tiLe) {
            tenSoTiLe[so] = ten + "@" + tiLe;
            tiLeSo[so] = tiLe;
        }
        return tenSoTiLe[so];
    }

    /** Anh cua dong tac (da phong to theo tiLe neu can). */
    private Image anhDongTac(int dongTac, PokeDongTac d, int tiLe) {
        int so = dl.anhCua(dongTac);
        String ten = tenAnh(so);
        Image goc = napAnh(ten);
        if (goc == null && so != 0) {
            return null;
        }
        if (goc == null || tiLe == 100) {
            return goc;
        }
        String tenTiLe = tenAnhTiLe(so, ten, tiLe);
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
        return paint(g, dex, shiny, x, y, dir, dongTac, tick, tiLe, doSang, 0);
    }

    /**
     * Nhu tren, them trangPhuc (1.. = bang mau trang phuc cua PhuKien, 0 = khong): anh dong tac doi mau
     * mot lan roi nho trong bo dem LRU (khoa anh + "~" + trangPhuc). Shiny giu: doi mau tren nen anh shiny.
     */
    public static boolean paint(Graphics g, int dex, boolean shiny, int x, int y, int dir, int dongTac, int tick, int tiLe, int doSang, int trangPhuc) {
        PokeBody pb = lay(dex, shiny);
        if (pb == null) {
            return false;
        }
        veTrangPhuc = trangPhuc;
        try {
            return pb.ve(g, x, y, dir, dongTac, tick, tiLe, doSang);
        } finally {
            veTrangPhuc = 0;
        }
    }

    private static boolean dac(int[] px, int w, int h, int x, int y) {
        return x >= 0 && y >= 0 && x < w && y < h && (px[y * w + x] >>> 24) >= 128;
    }

    /** Trang phuc cua lan ve dang chay (chi luong ve dung). */
    private static int veTrangPhuc;

    /** Anh dong tac da doi mau trang phuc tp (nho LRU). */
    private Image anhTrangPhuc(Image goc, String ten, int tp) {
        String k = ten + "~" + tp;
        Object o = layDem(k);
        if (o instanceof Image) {
            return (Image) o;
        }
        if (o == KHONG_CO) {
            return goc;
        }
        Image img = null;
        try {
            int w = goc.getWidth(), h = goc.getHeight();
            int[] px = new int[w * h];
            goc.getRGB(px, 0, w, 0, 0, w, h);
            PhuKien.doiMauTrangPhuc(px, tp);
            img = Image.createRGBImage(px, w, h, true);
        } catch (OutOfMemoryError e) {
            xoaBoDem();
            return goc;
        } catch (Throwable e) {
            img = null;
        }
        if (img == null) {
            return goc;
        }
        datDem(k, img);
        return img;
    }

    /**
     * Diem neo phu kien cua khung DANG ve (cung cach chon dong tac / khung nhu ve()), da nhan tiLe, so voi
     * diem chan: out = {dauX, dauY, dinhY, tayX, tayY, tamX, tamY, co}. false = loai khong co neo (ban nhe).
     */
    public static boolean neo(int dex, boolean shiny, int dir, int dongTac, int tick, int tiLe, int[] out) {
        PokeBody pb = lay(dex, shiny);
        if (pb == null || pb.dl.neo == null) {
            return false;
        }
        if (dir < 0 || dir > 3) {
            dir = 0;
        }
        if (tiLe <= 0) {
            tiLe = 100;
        }
        PokeDongTac d = pb.dl.layDongTac(dongTac);
        if (pb.anhDongTac(dongTac, d, 100) == null && dongTac != PokeDuLieu.DUNG) {
            dongTac = PokeDuLieu.DUNG;
            d = pb.dl.layDongTac(dongTac);
        }
        int a = dongTac < 0 || dongTac >= pb.dl.dongTac.length ? 0 : dongTac;
        byte[] nb = pb.dl.neo[a];
        int f = d.khungTai(PokeDuLieu.doiTick(tick), PokeDuLieu.laDongTacLap(dongTac));
        int p = (dir * d.soKhung + f) * PokeDuLieu.BYTE_NEO;
        if (nb == null || p + PokeDuLieu.BYTE_NEO > nb.length) {
            return false;
        }
        for (int i = 0; i < 7; i++) {
            out[i] = nb[p + i] * tiLe / 100;
        }
        out[7] = nb[p + 7] & 0xFF;
        return true;
    }

    /** Bang neo trang phuc da tinh (khoa loai + dong tac + huong + khung -> int[NeoTrangPhuc.SO]). */
    private static final java.util.Hashtable NEO_TP = new java.util.Hashtable();
    private static final int[] NEO_TAM = new int[8];

    /**
     * 0.9.8: neo TRANG PHUC cua khung DANG ve (cung cach chon dong tac / khung nhu ve()), tinh tu chinh diem anh khung
     * (NeoTrangPhuc: dinh so, giua so, mep dau, hang mat, co, lung, eo) - nho theo khung. out[NeoTrangPhuc.SO] so voi
     * diem chan, nhan tiLe. false = loai khong co neo PMD / khong doc duoc anh.
     */
    public static boolean neoTrangPhuc(int dex, boolean shiny, int dir, int dongTac, int tick, int tiLe, int[] out) {
        PokeBody pb = lay(dex, shiny);
        if (pb == null || pb.dl.neo == null) {
            return false;
        }
        if (dir < 0 || dir > 3) {
            dir = 0;
        }
        if (tiLe <= 0) {
            tiLe = 100;
        }
        PokeDongTac d = pb.dl.layDongTac(dongTac);
        Image anh = pb.anhDongTac(dongTac, d, 100);
        if (anh == null && dongTac != PokeDuLieu.DUNG) {
            dongTac = PokeDuLieu.DUNG;
            d = pb.dl.layDongTac(dongTac);
            anh = pb.anhDongTac(dongTac, d, 100);
        }
        if (anh == null) {
            return false;
        }
        int f = d.khungTai(PokeDuLieu.doiTick(tick), PokeDuLieu.laDongTacLap(dongTac));
        String k = pb.khoa + "/" + dongTac + "/" + dir + "/" + f;
        int[] v = (int[]) NEO_TP.get(k);
        if (v == null) {
            if (!neo(dex, shiny, dir, dongTac, tick, 100, NEO_TAM)) {
                return false;
            }
            int w = d.rongKhung, h = d.caoKhung;
            int sx = d.xAtlas + f * w, sy = d.yAtlas + dir * h;
            if (sx + w > anh.getWidth() || sy + h > anh.getHeight()) {
                return false;
            }
            int[] px = new int[w * h];
            try {
                anh.getRGB(px, 0, w, sx, sy, w, h);
            } catch (Throwable e) {
                return false;
            }
            if (pb.laShiny()) {
                // loai-bo-sung: ban sac mau chi doi mau -> neo tinh tren diem anh BAN THUONG cung khung (do mat bang diem
                // toi phu thuoc bang mau); khac khung (jar du khung / goi XA) hoac chua co ban thuong thi dung chinh no
                int[] px0 = pixelCungBong(dex, dongTac, dir, tick, d, px);
                if (px0 != null && neo(dex, false, dir, dongTac, tick, 100, NEO_TAM)) {
                    px = px0;   // cung bong: ca diem anh lan hat giong PMD lay tu ban thuong
                } else if (px0 != null) {
                    neo(dex, shiny, dir, dongTac, tick, 100, NEO_TAM);
                }
            }
            v = new int[NeoTrangPhuc.SO];
            // diem chan trong khung: ve() dat goc khung o (x - w/2 - dxChan, y - h/2 - dyChan)
            NeoTrangPhuc.tinhLoai(dex, px, w, h, w / 2 + d.dxChan[dir], h / 2 + d.dyChan[dir], NEO_TAM, dir, v);
            if (NEO_TP.size() > 3000) {
                NEO_TP.clear();
            }
            NEO_TP.put(k, v);
        }
        for (int i = 0; i < NeoTrangPhuc.SO; i++) {
            out[i] = v[i] == NeoTrangPhuc.KHONG ? NeoTrangPhuc.KHONG : v[i] * tiLe / 100;
        }
        return true;
    }

    /** Diem anh khung (cung tick) cua BAN THUONG neu cung co, cung diem chan va cung bong (alpha) voi px; null neu khong. */
    static int[] pixelCungBong(int dex, int dongTac, int dir, int tick, PokeDongTac d, int[] px) {
        PokeBody p0 = lay(dex, false);
        if (p0 == null || p0.khoa == KHOA_BONG || p0.dl == null) {
            return null;
        }
        PokeDongTac d0 = p0.dl.layDongTac(dongTac);
        if (d0.rongKhung != d.rongKhung || d0.caoKhung != d.caoKhung
                || d0.dxChan[dir] != d.dxChan[dir] || d0.dyChan[dir] != d.dyChan[dir]) {
            return null;
        }
        Image a0 = p0.anhDongTac(dongTac, d0, 100);
        int w = d.rongKhung, h = d.caoKhung;
        int f0 = d0.khungTai(PokeDuLieu.doiTick(tick), PokeDuLieu.laDongTacLap(dongTac));
        int sx = d0.xAtlas + f0 * w, sy = d0.yAtlas + dir * h;
        if (a0 == null || sx + w > a0.getWidth() || sy + h > a0.getHeight()) {
            return null;
        }
        int[] q = new int[w * h];
        try {
            a0.getRGB(q, 0, w, sx, sy, w, h);
        } catch (Throwable e) {
            return null;
        }
        for (int i = 0; i < q.length; i++) {
            if (((q[i] >>> 24) >= 128) != ((px[i] >>> 24) >= 128)) {
                return null;
            }
        }
        return q;
    }

    /** Neo the ban dong hanh da bam than (khoa loai + dong tac + huong + khung -> int[5]). */
    private static final java.util.Hashtable NEO_THE = new java.util.Hashtable();
    /** Ban kinh toi da (px goc) keo diem neo the vao than. */
    public static final int BAM_THAN_TOI_DA = 4;

    /**
     * loai-bo-sung: neo THE BAN DONG HANH (o phu kien cua Pokemon di theo) cua khung dang ve, so voi diem chan, nhan
     * tiLe: out = {dinhX, dinhY, coX, coY, coCo (1 = co diem co)}. Diem dinh dau PMD va diem co (giong TrangPhucVe.diemCo)
     * duoc BAM vao diem anh dac gan nhat cua chinh khung (<= BAM_THAN_TOI_DA px): co dai / ran nhin ngang, diem co noi suy
     * roi vao khoang trong giua dau va than -> the khong lo lung. Nho theo khung. false = khong co neo PMD.
     */
    public static boolean neoThe(int dex, boolean shiny, int dir, int dongTac, int tick, int tiLe, int[] out) {
        PokeBody pb = lay(dex, shiny);
        if (pb == null || pb.dl.neo == null) {
            return false;
        }
        if (dir < 0 || dir > 3) {
            dir = 0;
        }
        if (tiLe <= 0) {
            tiLe = 100;
        }
        PokeDongTac d = pb.dl.layDongTac(dongTac);
        Image anh = pb.anhDongTac(dongTac, d, 100);
        if (anh == null && dongTac != PokeDuLieu.DUNG) {
            dongTac = PokeDuLieu.DUNG;
            d = pb.dl.layDongTac(dongTac);
            anh = pb.anhDongTac(dongTac, d, 100);
        }
        if (anh == null) {
            return false;
        }
        int f = d.khungTai(PokeDuLieu.doiTick(tick), PokeDuLieu.laDongTacLap(dongTac));
        String k = pb.khoa + "/" + dongTac + "/" + dir + "/" + f;
        int[] v = (int[]) NEO_THE.get(k);
        if (v == null) {
            if (!neo(dex, shiny, dir, dongTac, tick, 100, NEO_TAM)) {
                return false;
            }
            int w = d.rongKhung, h = d.caoKhung;
            int sx = d.xAtlas + f * w, sy = d.yAtlas + dir * h;
            v = new int[5];
            v[0] = NEO_TAM[0];
            v[1] = NEO_TAM[2];
            int[] co = TrangPhucVe.diemCo(NEO_TAM, 1);
            if (co != null) {
                v[2] = co[0];
                v[3] = co[1];
                v[4] = 1;
            }
            if (sx + w <= anh.getWidth() && sy + h <= anh.getHeight()) {
                int[] px = new int[w * h];
                try {
                    anh.getRGB(px, 0, w, sx, sy, w, h);
                    int cx = w / 2 + d.dxChan[dir], cy = h / 2 + d.dyChan[dir];
                    bamThan(px, w, h, cx, cy, v, 0);
                    if (v[4] != 0) {
                        bamThan(px, w, h, cx, cy, v, 2);
                    }
                } catch (Throwable e) {
                    // khong doc duoc diem anh: giu neo PMD
                }
            }
            if (NEO_THE.size() > 3000) {
                NEO_THE.clear();
            }
            NEO_THE.put(k, v);
        }
        out[0] = v[0] * tiLe / 100;
        out[1] = v[1] * tiLe / 100;
        out[2] = v[2] * tiLe / 100;
        out[3] = v[3] * tiLe / 100;
        out[4] = v[4];
        return true;
    }

    /** Keo diem (v[i], v[i+1]) (so voi chan) vao diem anh dac gan nhat trong BAM_THAN_TOI_DA px (vong vuong). */
    static void bamThan(int[] px, int w, int h, int cx, int cy, int[] v, int i) {
        int x0 = cx + v[i], y0 = cy + v[i + 1];
        for (int r = 0; r <= BAM_THAN_TOI_DA; r++) {
            int bx = 0, by = 0, tot = Integer.MAX_VALUE;
            for (int dy = -r; dy <= r; dy++) {
                for (int dx = -r; dx <= r; dx++) {
                    if (dx != -r && dx != r && dy != -r && dy != r) {
                        continue;
                    }
                    int x = x0 + dx, y = y0 + dy;
                    if (x >= 0 && y >= 0 && x < w && y < h && (px[y * w + x] >>> 24) >= 128) {
                        int dd = dx * dx + dy * dy;
                        if (dd < tot) {
                            tot = dd;
                            bx = dx;
                            by = dy;
                        }
                    }
                }
            }
            if (tot != Integer.MAX_VALUE) {
                v[i] += bx;
                v[i + 1] += by;
                return;
            }
        }
    }

    /** Anh khung dang ve (ARGB, test / cong cu): {w, h, cx, cy} + px vao out (cap phat moi). null = khong co. */
    public static int[][] khungDangVe(int dex, boolean shiny, int dir, int dongTac, int tick) {
        PokeBody pb = lay(dex, shiny);
        if (pb == null) {
            return null;
        }
        PokeDongTac d = pb.dl.layDongTac(dongTac);
        Image anh = pb.anhDongTac(dongTac, d, 100);
        if (anh == null && dongTac != PokeDuLieu.DUNG) {
            dongTac = PokeDuLieu.DUNG;
            d = pb.dl.layDongTac(dongTac);
            anh = pb.anhDongTac(dongTac, d, 100);
        }
        if (anh == null) {
            return null;
        }
        int f = d.khungTai(PokeDuLieu.doiTick(tick), PokeDuLieu.laDongTacLap(dongTac));
        int w = d.rongKhung, h = d.caoKhung;
        int[] px = new int[w * h];
        anh.getRGB(px, 0, w, d.xAtlas + f * w, d.yAtlas + dir * h, w, h);
        return new int[][]{{w, h, w / 2 + d.dxChan[dir], h / 2 + d.dyChan[dir], f, d.soKhung}, px};
    }

    /** So khung + tong thoi luong (tick PokeDuLieu) cua dong tac (test / cong cu). */
    public static int[] thongTinDongTac(int dex, boolean shiny, int dongTac) {
        PokeBody pb = lay(dex, shiny);
        if (pb == null) {
            return null;
        }
        PokeDongTac d = pb.dl.layDongTac(dongTac);
        int[] t = new int[1 + d.soKhung];
        t[0] = d.soKhung;
        for (int i = 0; i < d.soKhung; i++) {
            t[1 + i] = d.thoiLuong[i];
        }
        return t;
    }

    /**
     * Vien sang 1 px quanh hinh khung dang ve (vang sang cap cuong hoa cao), mau = mau RGB. Anh vien tinh
     * mot lan cho moi (anh, huong, khung, mau) roi nho LRU. Chi ti le 100 (nguoi choi). false = khong ve.
     */
    public static boolean veVien(Graphics g, int dex, boolean shiny, int x, int y, int dir, int dongTac, int tick, int mau) {
        PokeBody pb = lay(dex, shiny);
        if (pb == null) {
            return false;
        }
        if (dir < 0 || dir > 3) {
            dir = 0;
        }
        PokeDongTac d = pb.dl.layDongTac(dongTac);
        Image anh = pb.anhDongTac(dongTac, d, 100);
        if (anh == null && dongTac != PokeDuLieu.DUNG) {
            dongTac = PokeDuLieu.DUNG;
            d = pb.dl.layDongTac(dongTac);
            anh = pb.anhDongTac(dongTac, d, 100);
        }
        if (anh == null) {
            return false;
        }
        int f = d.khungTai(PokeDuLieu.doiTick(tick), PokeDuLieu.laDongTacLap(dongTac));
        int w = d.rongKhung, h = d.caoKhung;
        int sx = d.xAtlas + f * w, sy = d.yAtlas + dir * h;
        if (sx + w > anh.getWidth() || sy + h > anh.getHeight()) {
            return false;
        }
        String k = THU_MUC + pb.khoa + "_" + pb.dl.anhCua(dongTac) + "#v" + dir + "_" + f + "_" + mau;
        Object o = layDem(k);
        Image img = o instanceof Image ? (Image) o : null;
        if (o == KHONG_CO) {
            return false;
        }
        if (img == null) {
            try {
                int[] src = new int[w * h];
                anh.getRGB(src, 0, w, sx, sy, w, h);
                int W = w + 2, H = h + 2;
                int[] dst = new int[W * H];
                int c = 0xFF000000 | mau;
                for (int yy = 0; yy < H; yy++) {
                    for (int xx = 0; xx < W; xx++) {
                        int ox = xx - 1, oy = yy - 1;
                        if (dac(src, w, h, ox, oy)) {
                            continue;   // pixel cua than: de trong
                        }
                        if (dac(src, w, h, ox - 1, oy) || dac(src, w, h, ox + 1, oy)
                                || dac(src, w, h, ox, oy - 1) || dac(src, w, h, ox, oy + 1)) {
                            dst[yy * W + xx] = c;
                        }
                    }
                }
                img = Image.createRGBImage(dst, W, H, true);
            } catch (OutOfMemoryError e) {
                xoaBoDem();
                return false;
            } catch (Throwable e) {
                datDem(k, null);
                return false;
            }
            datDem(k, img);
        }
        int xVe = x - w / 2 - d.dxChan[dir];
        int yVe = y - h / 2 - d.dyChan[dir];
        g.drawImage(img, xVe - 1, yVe - 1, Graphics.TOP | Graphics.LEFT);
        return true;
    }

    public static boolean paint(Graphics g, int dex, boolean shiny, int x, int y, int dir, int dongTac, int tick, int tiLe) {
        return paint(g, dex, shiny, x, y, dir, dongTac, tick, tiLe, 0);
    }

    /** 1.0 Pokedex: than bong den ("Ai day?") cho loai chua gap / chua mo. false neu jar khong co bong. */
    public static boolean paintBong(Graphics g, int x, int y, int dir, int tick, int tiLe) {
        PokeBody pb = layBong();
        return pb != null && pb.ve(g, x, y, dir, PokeDuLieu.DUNG, tick, tiLe, 0);
    }

    /** 1.0 Pokedex: chan dung bong den tam (xTam, yTam) canh {@code canh}. */
    public static boolean paintChanDungBong(Graphics g, int xTam, int yTam, int canh) {
        Image img = napAnh(THU_MUC + KHOA_BONG + "_c.png");
        if (img == null) {
            return false;
        }
        if (canh > 0 && img.getWidth() > 0 && canh != img.getWidth()) {
            String ten = THU_MUC + KHOA_BONG + "_c.png#" + canh;
            Object o = layDem(ten);
            if (o instanceof Image) {
                img = (Image) o;
            } else {
                try {
                    Image nho = phongTo(img, img.getWidth(), img.getHeight(), 1, 1, canh * 100 / img.getWidth());
                    datDem(ten, nho);
                    img = nho;
                } catch (Throwable e) {
                    // co goc
                }
            }
        }
        g.drawImage(img, xTam, yTam, Graphics.HCENTER | Graphics.VCENTER);
        return true;
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
        if (veTrangPhuc > 0 && tiLe == 100) {
            anh = anhTrangPhuc(anh, THU_MUC + khoa + "_" + dl.anhCua(dongTac) + ".png", veTrangPhuc);
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

    /** Bo dem pixel dung lai cho veSang (khong cap phat moi lan chop trang). */
    private static int[] demSang;

    /**
     * Ve mot khung sang hon (mau x (100 + doSang)/100, toi da trang). Dung khi tien hoa va chop
     * trang luc trung don (HieuUngDanh, vai khung hinh).
     */
    private static void veSang(Graphics g, Image anh, int sx, int sy, int w, int h, int x, int y, int doSang) {
        try {
            int n = w * h;
            if (demSang == null || demSang.length < n) {
                demSang = new int[n];
            }
            int[] px = demSang;
            anh.getRGB(px, 0, w, sx, sy, w, h);
            int heSo = 100 + doSang;
            for (int i = 0; i < n; i++) {
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

    /** Be ngang hinh dung yen (khung da cat sat hinh cua ban jar ~ be ngang con Pokemon), 0 neu khong co. */
    public static int rongHinh(int dex, boolean shiny) {
        PokeBody pb = lay(dex, shiny);
        return pb == null ? 0 : pb.dl.layDongTac(PokeDuLieu.DUNG).rongKhung;
    }

    /**
     * Do cao DINH HINH THAT (khung dong tac dung da cat sat hinh) tren diem chan, 0 neu khong co. Khac
     * caoTrenChan (dinh khung goc chua cat, con nhieu khoang trong): dung de dat ten sat tren dau.
     */
    public static int caoHinh(int dex, boolean shiny) {
        PokeBody pb = lay(dex, shiny);
        if (pb == null) {
            return 0;
        }
        PokeDongTac d = pb.dl.layDongTac(PokeDuLieu.DUNG);
        if (d == null || d.caoKhung <= 0) {
            return 0;
        }
        int c = d.caoKhung / 2 + d.dyChan[0];
        return c > 0 ? c : 0;
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

    // ---- dung cho HuyHieuPoke (huy hieu tron: sprite dung yen tren nen mau he)
    static Object demLay(String ten) {
        return layDem(ten);
    }

    static void demDat(String ten, Image img) {
        datDem(ten, img);
    }

    /** Anh dong tac DUNG (ti le goc) + du lieu khung, null neu thieu. */
    Image anhDung() {
        return anhDongTac(PokeDuLieu.DUNG, dl.layDongTac(PokeDuLieu.DUNG), 100);
    }

    /**
     * Ve chan dung voi tam (xTam, yTam), canh = kich thuoc ve (40 = goc).
     * pokemon: ve huy hieu tron (HuyHieuPoke - sprite dung yen trong suot tren nen mau he, vien
     * dam + vet sang) thay anh chan dung PMD co nen troi/co ve san; thieu sprite moi dung anh cu.
     */
    public static boolean paintChanDung(Graphics g, int dex, boolean shiny, int xTam, int yTam, int canh) {
        if (HuyHieuPoke.ve(g, dex, shiny, xTam, yTam, canh)) {
            return true;
        }
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
