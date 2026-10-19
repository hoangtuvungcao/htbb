package game.pokemon;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Hashtable;
import javax.microedition.lcdui.Image;

/**
 * Du lieu PHU KIEN TRANG BI (res/pk/phukien.bin, sinh boi HTBB tools/pokemon/SinhPhuKien.java - pixel-art tu
 * sinh, khong anh ban quyen): bang mau do hiem, mau co dinh, mau + ten bac, ten do hiem, bang mau trang phuc,
 * hinh tung (o, kieu, huong). Anh phu kien (theo do hiem, lat, ti le, nhip lap lanh) tao mot lan roi nho.
 * Cung dinh dang voi Unity (PhuKien.cs doc Resources/poke/phukien.bytes).
 */
public final class PhuKien {
    /** Mot hinh phu kien (xem SinhPhuKien). */
    public static final class Hinh {
        public int o, kieu, huong, lop, neo, dx, dy, rong, cao, ax, ay, nho;
        public byte[] px;   // chi so mau, rong*cao
        /** anh da tao cho hinh nay: [((h * 2 + lat) * 4 + (k - 1)) * 4 + nhay] (k 1..4), the = lop xoa */
        Image[] anhDa;
        int theAnh;
    }

    public static final int SAU = 0, TRUOC = 1;
    public static final int N_DINH = 0, N_DAU = 1, N_CO = 2, N_TAY = 3, N_TAM = 4;

    private static boolean daNap;
    private static boolean co;
    static int[][] hiem;
    static int[] coDinh;
    static int[] mauBac;
    static String[] tenBac;
    static String[] tenHiem;
    static int[][] trangPhuc;
    static String[] tenTrangPhuc;
    /** hinh[((o * 16 + kieu) * 3 + huong) * 2 + nho] */
    private static final Hinh[] HINH = new Hinh[7 * 16 * 3 * 2];
    /** Loai ngoai le: dex -> co (1 khong do o co, 2 khong mu, 4 khong do cam, 8 khong sau lung). */
    private static final Hashtable NGOAI_LE = new Hashtable();
    public static final int NL_CO = 1, NL_MU = 2, NL_TAY = 4, NL_LUNG = 8;
    private static final Hashtable ANH = new Hashtable();
    /** Mau "sang" lap lanh cua do Hoan my (4 nhip). */
    private static final int[] LAP_LANH = {0xFFF8C8, 0xFFD0F0, 0xD0F0FF, 0xD8FFD0};

    private PhuKien() {
    }

    /** Nap tep (mot lan). false = jar khong co phu kien. */
    public static boolean nap() {
        if (daNap) {
            return co;
        }
        daNap = true;
        try {
            byte[] b = PokeBody.docTaiNguyen(PokeBody.THU_MUC + "phukien.bin");
            if (b == null) {
                return co = false;
            }
            DataInputStream r = new DataInputStream(new ByteArrayInputStream(b));
            if (r.readUnsignedByte() != 'K') {
                return co = false;
            }
            int pb = r.readUnsignedByte();
            if (pb != 1 && pb != 2) {
                return co = false;
            }
            int n = r.readUnsignedByte();
            hiem = new int[n][5];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < 5; j++) {
                    hiem[i][j] = rgb(r);
                }
            }
            n = r.readUnsignedByte();
            coDinh = new int[n];
            for (int i = 0; i < n; i++) {
                coDinh[i] = rgb(r);
            }
            n = r.readUnsignedByte();
            mauBac = new int[n];
            tenBac = new String[n];
            for (int i = 0; i < n; i++) {
                mauBac[i] = rgb(r);
                tenBac[i] = r.readUTF();
            }
            n = r.readUnsignedByte();
            tenHiem = new String[n];
            for (int i = 0; i < n; i++) {
                tenHiem[i] = r.readUTF();
            }
            n = r.readUnsignedByte();
            trangPhuc = new int[n][5];
            tenTrangPhuc = new String[n];
            for (int i = 0; i < n; i++) {
                trangPhuc[i][0] = r.readShort();
                trangPhuc[i][1] = r.readUnsignedByte();
                trangPhuc[i][2] = r.readUnsignedByte();
                trangPhuc[i][3] = rgb(r);
                trangPhuc[i][4] = r.readUnsignedByte();
                tenTrangPhuc[i] = r.readUTF();
            }
            n = r.readUnsignedShort();
            for (int i = 0; i < n; i++) {
                Hinh h = new Hinh();
                h.o = r.readUnsignedByte();
                h.kieu = r.readUnsignedByte();
                h.huong = r.readUnsignedByte();
                h.lop = r.readUnsignedByte();
                h.neo = r.readUnsignedByte();
                h.dx = r.readByte();
                h.dy = r.readByte();
                h.rong = r.readUnsignedByte();
                h.cao = r.readUnsignedByte();
                h.ax = r.readByte();
                h.ay = r.readByte();
                h.nho = pb >= 2 ? r.readUnsignedByte() & 1 : 0;
                int sp = h.rong * h.cao;
                h.px = new byte[sp];
                for (int k = 0; k < sp; k += 2) {
                    int v = r.readUnsignedByte();
                    h.px[k] = (byte) (v >> 4);
                    if (k + 1 < sp) {
                        h.px[k + 1] = (byte) (v & 0x0F);
                    }
                }
                if (h.o < 7 && h.kieu < 16 && h.huong < 3) {
                    HINH[((h.o * 16 + h.kieu) * 3 + h.huong) * 2 + h.nho] = h;
                }
            }
            if (pb >= 2) {
                int nl = r.readUnsignedShort();
                for (int i = 0; i < nl; i++) {
                    int dex = r.readUnsignedShort();
                    NGOAI_LE.put(new Integer(dex), new Integer(r.readUnsignedByte()));
                }
            }
            co = true;
        } catch (Throwable e) {
            co = false;
        }
        return co;
    }

    private static int rgb(DataInputStream r) throws java.io.IOException {
        return (r.readUnsignedByte() << 16) | (r.readUnsignedByte() << 8) | r.readUnsignedByte();
    }

    /** Hinh (o, kieu, huong); nho = uu tien ban nho (khong co thi tra ban thuong). */
    public static Hinh hinh(int o, int kieu, int huong, boolean nho) {
        if (!nap() || o < 0 || o >= 7 || kieu <= 0 || kieu >= 16 || huong < 0 || huong > 2) {
            return null;
        }
        int k = ((o * 16 + kieu) * 3 + huong) * 2;
        if (nho && HINH[k + 1] != null) {
            return HINH[k + 1];
        }
        return HINH[k];
    }

    /** Co ngoai le cua loai (0 = binh thuong). */
    public static int ngoaiLe(int dex) {
        if (!nap()) {
            return 0;
        }
        Object o = NGOAI_LE.get(new Integer(dex));
        return o == null ? 0 : ((Integer) o).intValue();
    }

    /**
     * Vong sang mem duoi chan: elip rong 2rw x cao 2rh, alpha giam dan tu tam (toi da aMax/255), mau rgb. Mot
     * anh cho moi (rw, mau, aMax); bang rieng nho.
     */
    public static Image elip(int rw, int rh, int mau, int aMax) {
        String khoa = "e" + rw + "," + rh + "," + mau + "," + aMax;
        Object o = ANH.get(khoa);
        if (o instanceof Image) {
            return (Image) o;
        }
        int W = rw * 2 + 1, H = rh * 2 + 1;
        int[] px = new int[W * H];
        int rw2 = rw * rw, rh2 = rh * rh;
        for (int y = 0; y < H; y++) {
            int dy = y - rh;
            for (int x = 0; x < W; x++) {
                int dx = x - rw;
                // d = khoang cach chuan hoa * 256
                long d2 = ((long) dx * dx * 65536) / rw2 + ((long) dy * dy * 65536) / rh2;
                if (d2 >= 65536) {
                    continue;
                }
                int d = 0;
                while ((long) (d + 1) * (d + 1) <= d2) {
                    d++;
                }
                int a = aMax * (256 - d) / 256;
                px[y * W + x] = (a << 24) | (mau & 0xFFFFFF);
            }
        }
        Image img;
        try {
            img = Image.createRGBImage(px, W, H, true);
        } catch (Throwable e) {
            return null;
        }
        if (ANH.size() > 400) {
            ANH.clear();
        }
        ANH.put(khoa, img);
        return img;
    }

    /** Mau vang sang cua bac (0xRRGGBB); bac 10 (toi thuong) doi mau cau vong theo tick. */
    public static int mauBac(int bac, int tick) {
        if (bac >= 10) {
            return cauVong(tick / 3);
        }
        if (!nap() || bac < 0 || bac >= mauBac.length) {
            return 0xFFFFFF;
        }
        return mauBac[bac];
    }

    private static final int[] CAU_VONG = {0xFF5050, 0xFFA030, 0xFFE840, 0x58E070, 0x50B8FF, 0xC070FF};

    public static int cauVong(int i) {
        return CAU_VONG[(i % 6 + 6) % 6];
    }

    public static String tenBac(int b) {
        return nap() && b >= 0 && b < tenBac.length ? tenBac[b] : "";
    }

    public static String tenHiem(int h) {
        return nap() && h >= 0 && h < tenHiem.length ? tenHiem[h] : "";
    }

    public static int mauHiem(int h) {
        return nap() && h >= 0 && h < hiem.length ? hiem[h][2] : 0xFFFFFF;
    }

    public static String tenTrangPhuc(int tp) {
        return nap() && tp >= 1 && tp <= tenTrangPhuc.length ? tenTrangPhuc[tp - 1] : "";
    }

    /**
     * Anh phu kien: do hiem h, lat guong, phong k lan, nhip lap lanh (chi do Hoan my, 0..3). Nho trong bang
     * rieng (vai tram anh nho); qua 400 anh thi xoa het.
     */
    public static Image anh(Hinh hi, int h, boolean lat, int k, int nhay) {
        if (h < 0 || h >= hiem.length) {
            h = 0;
        }
        if (h != hiem.length - 1) {
            nhay = 0;
        }
        // nho theo tung Hinh (khong tao chuoi khoa moi khung; khoa cu thieu hi.nho -> ban nho / thuong lan anh)
        int iDa = -1;
        if (k >= 1 && k <= 4) {
            iDa = ((h * 2 + (lat ? 1 : 0)) * 4 + (k - 1)) * 4 + (nhay & 3);
            if (hi.anhDa == null || hi.theAnh != theAnh) {
                hi.anhDa = new Image[hiem.length * 32];
                hi.theAnh = theAnh;
            }
            if (hi.anhDa[iDa] != null) {
                return hi.anhDa[iDa];
            }
        }
        String khoa = null;
        if (iDa < 0) {
            khoa = new StringBuffer(16).append(hi.o).append(',').append(hi.kieu).append(',').append(hi.huong).append(',').append(hi.nho)
                    .append(',').append(h).append(lat ? 'l' : 'n').append(k).append(',').append(nhay).toString();
            Object o = ANH.get(khoa);
            if (o instanceof Image) {
                return (Image) o;
            }
        }
        if (ANH.size() + soAnhDa > 400) {
            ANH.clear();
            soAnhDa = 0;
            theAnh++;   // bo moi anhDa cu
            if (iDa >= 0) {
                hi.anhDa = new Image[hiem.length * 32];
                hi.theAnh = theAnh;
            }
        }
        int W = hi.rong * k, H = hi.cao * k;
        int[] px = new int[W * H];
        int[] bang = hiem[h];
        for (int y = 0; y < hi.cao; y++) {
            for (int x = 0; x < hi.rong; x++) {
                int c = hi.px[y * hi.rong + (lat ? hi.rong - 1 - x : x)];
                if (c == 0) {
                    continue;
                }
                int argb;
                if (c <= 5) {
                    argb = (c == 4 && h == hiem.length - 1) ? LAP_LANH[nhay & 3] : bang[c - 1];
                } else {
                    argb = coDinh[c - 6 < coDinh.length ? c - 6 : 0];
                }
                argb |= 0xFF000000;
                for (int yy = 0; yy < k; yy++) {
                    for (int xx = 0; xx < k; xx++) {
                        px[(y * k + yy) * W + x * k + xx] = argb;
                    }
                }
            }
        }
        Image img;
        try {
            img = Image.createRGBImage(px, W, H, true);
        } catch (Throwable e) {
            return null;
        }
        if (iDa >= 0) {
            hi.anhDa[iDa] = img;
            soAnhDa++;
        } else {
            ANH.put(khoa, img);
        }
        return img;
    }

    private static int theAnh, soAnhDa;

    private static int cosTp = 256, sinTp, tpDangTinh = -1;

    /**
     * Doi mau trang phuc tp (1..) tai cho tren mang ARGB: xoay mau (do), bao hoa %, do sang %, phu mau %.
     * Chi so nguyen + mot lan Math.cos/sin cho moi trang phuc.
     */
    public static void doiMauTrangPhuc(int[] px, int tp) {
        if (!nap() || tp < 1 || tp > trangPhuc.length) {
            return;
        }
        int[] t = trangPhuc[tp - 1];
        if (tpDangTinh != tp) {
            double rad = t[0] * Math.PI / 180.0;
            cosTp = (int) (Math.cos(rad) * 256);
            sinTp = (int) (Math.sin(rad) * 256);
            tpDangTinh = tp;
        }
        int sat = t[1], val = t[2], tint = t[3], pct = t[4];
        int tr = (tint >> 16) & 0xFF, tg = (tint >> 8) & 0xFF, tb = tint & 0xFF;
        // ma tran xoay mau quanh truc xam (Paeth / Haeberli, he so *256)
        int c = cosTp, s = sinTp;
        int m00 = (77 * 256 + c * (256 - 77) - s * 77) / 256, m01 = (150 * 256 - c * 150 - s * 150) / 256, m02 = (29 * 256 - c * 29 + s * (256 - 29)) / 256;
        int m10 = (77 * 256 - c * 77 + s * 36) / 256, m11 = (150 * 256 + c * (256 - 150) + s * 36) / 256, m12 = (29 * 256 - c * 29 - s * 72) / 256;
        int m20 = (77 * 256 - c * 77 - s * (256 - 77)) / 256, m21 = (150 * 256 - c * 150 + s * 150) / 256, m22 = (29 * 256 + c * (256 - 29) + s * 29) / 256;
        boolean xoay = t[0] != 0;
        for (int i = 0; i < px.length; i++) {
            int p = px[i];
            if ((p >>> 24) == 0) {
                continue;
            }
            int r = (p >> 16) & 0xFF, g = (p >> 8) & 0xFF, b = p & 0xFF;
            if (xoay) {
                int r2 = (m00 * r + m01 * g + m02 * b) >> 8;
                int g2 = (m10 * r + m11 * g + m12 * b) >> 8;
                int b2 = (m20 * r + m21 * g + m22 * b) >> 8;
                r = r2;
                g = g2;
                b = b2;
            }
            int xam = (r * 77 + g * 150 + b * 29) >> 8;
            r = xam + (r - xam) * sat / 100;
            g = xam + (g - xam) * sat / 100;
            b = xam + (b - xam) * sat / 100;
            r = r * val / 100;
            g = g * val / 100;
            b = b * val / 100;
            // phu mau theo do sang (giu bong toi: vung toi it bi phu)
            int w = pct * (xam + 64) / 320;
            r += (tr * xam / 255 - r) * w / 100;
            g += (tg * xam / 255 - g) * w / 100;
            b += (tb * xam / 255 - b) * w / 100;
            r = r < 0 ? 0 : r > 255 ? 255 : r;
            g = g < 0 ? 0 : g > 255 ? 255 : g;
            b = b < 0 ? 0 : b > 255 ? 255 : b;
            px[i] = (p & 0xFF000000) | (r << 16) | (g << 8) | b;
        }
    }
}
