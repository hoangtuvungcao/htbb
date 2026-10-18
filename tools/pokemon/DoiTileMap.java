import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * G7.A1: sinh bo tile 16x16 phong cach PMD thay cho tile goc, GIU NGUYEN so tile, thu tu
 * va kich thuoc (map khong phai sua, cMap/*.type van dung).
 *
 * Duong tile cua game (xem bao cao G7.A / docs/pokemon/KE_HOACH_CHUYEN_DOI.md muc 15):
 *   - Client Unity nap tile tu Resources/res/pngtile/{t,t_hang,t_thanh} (Res.loadImgMap),
 *     anh doc 16 x (16*n). Che do Pokemon: Resources/poke_tile/ (tool nay sinh ra).
 *   - Server gui map/t_hang.png + map/t_thanh.png trong goi dang nhap (chi khi client xin,
 *     vsTile == -1 - client Unity khong xin). Che do Pokemon: map/pokemon_tile/.
 *   - Kieu tile (0 di duoc, 2 chan) o cMap/{t,t_hang,t_thanh}.type - KHONG doi.
 *
 * Cach lam ("ve lai theo vat lieu"):
 *   1. Moi bo tile goc co mot danh sach VAT LIEU (co, dat, nuoc, cay, da...). Bang mau cua vat
 *      lieu rut tu cac tile mau (k-means trong khong gian Lab).
 *   2. Moi vat lieu co vai nguon PMD ung vien (TileDtef: khoi tuong/nuoc/nen). Bo so khop chon
 *      nguon gan mau goc nhat, roi chon tile PMD cu the bang dac trung: luoi mau 4x4 (Lab) +
 *      mat na trong suot + mat do canh. Tile PMD 24x24 thu nho 24->16 bang trung binh dien tich
 *      (alpha nhan truoc) roi "bam" ve bang mau cua chinh tile nguon de giu net pixel art.
 *   3. Tile goc thuan mot vat lieu (>= NGUONG_THUAN) -> thay nguyen bang tile PMD khop nhat.
 *      Tile lan nhieu vat lieu (mep co/dat, bo nuoc...) -> tung pixel lay van PMD cua vat lieu
 *      cua no, giu do sang lech (bong, vien) cua goc, nen hinh dang mep giu nguyen va cac tile
 *      ghep lien nhau van khop. Pixel khong thuoc vat lieu nao (mai nha, go, do vat) -> giu mau
 *      goc nhung doi sang bang mau PMD gan nhat.
 *   4. Pixel trong suot cua goc giu trong suot (tile phu "tileTop").
 *
 * Chay (o thu muc HTBB/, can JDK + mang lan dau):
 *   javac -encoding UTF-8 -d build/tools tools/pokemon/DoiTileMap.java
 *   java -cp build/tools DoiTileMap            (tai PMD neu chua co, sinh moi thu)
 *   java -cp build/tools DoiTileMap --khong-tai
 *
 * Ra: map/pokemon_tile/{t,t_hang,t_thanh,t_small,t_hang_s,t_thanh_s}.png,
 *     <client>/Assets/Resources/poke_tile/*.bytes (htbb-client/pc + ClientUnity cu; cung noi dung),
 *     docs/pokemon/bang_tile.csv, docs/pokemon/xem_truoc_tile.png.
 * Nguon PMD tai ve tools/pokemon/pmd_raw_tile/ (khong commit, xem .gitignore).
 */
public final class DoiTileMap {

    static final String GOC_RAW = "https://raw.githubusercontent.com/PMDCollab/RawAsset/master/TileDtef/";
    static final String THU_MUC_RAW = "tools/pokemon/pmd_raw_tile/dtef/";
    static final String THU_MUC_RA = "map/pokemon_tile/";
    /** A2: vat trang tri ban do (cay/nha map/tree, cay trong map/cay, anh dip le) - cung cau truc thu muc. */
    static final String THU_MUC_TRANG_TRI = "map/pokemon_trang_tri/";
    /** Anh dip le (Map.java imgDynamic). */
    static final String[] ANH_DIP_LE = {"dao", "mai", "rong", "rong1", "banhsn", "caythong", "longden"};
    /**
     * Cay ban do co ban PMD tuong duong ro rang (RawAsset/Object): thay han anh, dx/dy tinh lai de
     * giu goc duoi-giua (chan cay dung cho cu). Cac vat khac chi doi mau.
     */
    static final String[][] THAY_VAT_THE = {{"6", "Tree_Town.Flip"}, {"7", "Tree_Town.Flip"}, {"9", "Tree_Town.Flip"}};
    static final String GOC_OBJECT = "https://raw.githubusercontent.com/PMDCollab/RawAsset/master/Object/";
    static final String THU_MUC_OBJECT = "tools/pokemon/pmd_raw_tile/object/";
    /** Tile cho client Unity: Resources/poke_tile/ cua moi client (thuMucClient - htbb-client/pc + ClientUnity cu). */

    /** Khoi trong tep DTEF tileset_N.png (432x192): 3 khoi 6x8 tile 24px canh nhau. */
    static final int TUONG = 0, PHU = 1, NEN = 2;
    static final String[] TEN_KHOI = {"tuong", "phu", "nen"};

    /** Ty le pixel cua mot vat lieu de coi tile la "thuan" va thay nguyen tile. */
    static final double NGUONG_THUAN = 0.93;
    /** Vat lieu chiem it hon ty le nay trong mot tile bi coi la nhieu (tra ve "khac"). */
    static final double NGUONG_NHIEU = 0.06;
    /** Khoang cach Lab toi da de mot pixel duoc xep vao vat lieu. */
    static final double NGUONG_PIXEL = 16.0;
    /** Khoang cach dac trung toi da de o tuong/nuoc khac (1,1) duoc coi la o ben trong. */
    static final double NGUONG_O_TRONG = 5.0;
    /** Giu bao nhieu phan do sang lech cua goc khi ve van PMD (bong, vien). */
    static final double HE_SO_SANG = 0.75;

    /** Nguon PMD: ten bo + khoi. */
    static final class Nguon {
        final String bo;
        final int khoi;
        Nguon(final String bo, final int khoi) {
            this.bo = bo;
            this.khoi = khoi;
        }
        @Override public String toString() {
            return bo + "/" + TEN_KHOI[khoi];
        }
    }

    static Nguon n(final String bo, final int khoi) {
        return new Nguon(bo, khoi);
    }

    /** Vat lieu: ten, tile goc mau, nguon PMD ung vien. */
    static final class VatLieu {
        final String ten;
        final int[] mau;
        final Nguon[] ungVien;
        double[][] bangMau;       // tam cum Lab
        double sangTb;            // L trung binh cua tile mau
        Nguon nguonChon;
        List<TilePmd> tiles = new ArrayList<TilePmd>();
        TilePmd coBan;            // van dung cho tile lan
        /** 0 = giu nguyen mau PMD; 1 = dua trung binh mau ve dung mau goc (chi giu van). */
        double giuMau;
        VatLieu(final String ten, final int[] mau, final Nguon... ungVien) {
            this.ten = ten;
            this.mau = mau;
            this.ungVien = ungVien;
        }
        /** -1: moi kieu; 0/2: chi dung cho tile co kieu nay trong cMap/*.type. */
        int chiKieu = -1;
        VatLieu chiKieu(final int kieu) {
            this.chiKieu = kieu;
            return this;
        }
        /** He so tuong phan do sang cua van PMD (1 = giu; 0.5 = diu di mot nua). */
        double tuongPhan = 1;
        VatLieu tuongPhan(final double heSo) {
            this.tuongPhan = heSo;
            return this;
        }
        /** true: khong bao gio thay nguyen tile; luon ve tung pixel de giu duong vien goc. */
        boolean khongThay;
        VatLieu khongThay() {
            this.khongThay = true;
            return this;
        }
        /** Vat lieu "giu" ma pixel giu NGUYEN mau goc (vd vuc toi ngoai hang), khong doi mau. */
        boolean nguyenMau;
        VatLieu nguyenMau() {
            this.nguyenMau = true;
            return this;
        }
        /** Ty le toi thieu trong mot tile; it hon thi pixel bi coi la nhieu (tra ve "khac"). */
        double toiThieu = NGUONG_NHIEU;
        VatLieu toiThieu(final double tyLe) {
            this.toiThieu = tyLe;
            return this;
        }
        /** true: lam sach MOI tile PMD cua vat lieu (bo hoa/la le) - tile lap lai thanh hang cham. */
        boolean sachHet;
        VatLieu sachHet() {
            this.sachHet = true;
            return this;
        }
        VatLieu giuMau(final double heSo) {
            this.giuMau = heSo;
            return this;
        }
    }

    static int[] r(final int a, final int b) {
        final int[] k = new int[b - a + 1];
        for (int i = a; i <= b; i++) {
            k[i - a] = i;
        }
        return k;
    }

    static int[] g(final int[]... ds) {
        int n = 0;
        for (final int[] d : ds) {
            n += d.length;
        }
        final int[] k = new int[n];
        int j = 0;
        for (final int[] d : ds) {
            for (final int v : d) {
                k[j++] = v;
            }
        }
        return k;
    }

    static int[] c(final int... v) {
        return v;
    }

    static final String[] CO = {"ForestPath", "LushPrairie", "TinyWoods", "AppleWoods"};
    static final String[] NUOC = {"TinyWoods", "ForestPath", "BeachCave", "MtSteel1", "WesternCave1", "DesertRegion", "SidePath"};
    static final String[] DAT = {"TinyWoods", "MtSteel1", "SidePath", "QuicksandCave", "DesertRegion", "NorthernDesert1"};
    static final String[] DA = {"SealedRuin", "MtSteel1", "TemporalTower", "QuicksandCave", "WesternCave1"};
    /** Da hang: van da vun co hat (tuong DTEF), mau keo ve mau hang goc. */
    static final String[] DA_HANG = {"SealedRuin", "QuicksandCave", "WesternCave1"};
    /** Nen hang di duoc. */
    static final String[] NEN_HANG = {"WesternCave1", "MurkyCave", "GoldenChamber", "SealedRuin", "MtSteel1"};
    /*
     * Rang buoc theo KIEU tile: "nuoc" (van nuoc PMD) chi cho o CHAN; o di duoc mau nuoc (nuoc
     * nong, bo bien) la "nuoc_nong" - giu hinh goc, chi doi mau - de nguoi choi khong thay mot o
     * nuoc sau PMD ma van di qua duoc.
     */
    /** Nguong do lech chuan L de coi tile hang co vat noi (xem BoTile.nguongVat). */
    static final double NGUONG_VAT_HANG = 9.0;
    /** Trong tile co vat: pixel lech do sang hon nguong nay so voi vat lieu thi giu (chi doi mau). */
    static final double NGUONG_GIU_PIXEL_VAT = 14.0;
    /** Gia tri trong cMap/*.type (client Tilemap.T_BLOCK = 2). */
    static final int DI_DUOC = 0, CHAN = 2;
    /** Nen lat da trong thanh. */
    static final String[] LAT = {"WesternCave1", "TemporalTower", "SealedRuin"};
    static final String[] TAT_CA = {"AppleWoods", "BeachCave", "DesertRegion", "ForestPath", "GoldenChamber",
        "LushPrairie", "MtSteel1", "MurkyCave", "NorthernDesert1", "QuicksandCave", "SealedRuin", "SidePath",
        "SkyTower", "TemporalTower", "TinyWoods", "WaterfallPond", "WesternCave1"};

    static Nguon[] ds(final String[] bo, final int khoi) {
        final Nguon[] k = new Nguon[bo.length];
        for (int i = 0; i < bo.length; i++) {
            k[i] = n(bo[i], khoi);
        }
        return k;
    }

    static Nguon[] noi(final Nguon[]... ds) {
        final List<Nguon> k = new ArrayList<Nguon>();
        for (final Nguon[] d : ds) {
            k.addAll(Arrays.asList(d));
        }
        return k.toArray(new Nguon[0]);
    }

    /** Mot bo tile goc. */
    static final class BoTile {
        final String ten;           // t, t_hang, t_thanh
        final String tenNho;        // anh 1xN cho ban do nho
        final VatLieu[] vatLieu;
        BufferedImage cu;
        int[] kieu;
        /**
         * > 0: tile co vat noi (nhu da, tang, lua...) - do lech chuan do sang (Lab L) cua phan
         * khong phai vuc vuot nguong nay - khong bi thay nguyen; ve tung pixel, giu bong goc day du
         * va giu nguyen pixel lech manh (vien, diem sang) de vat van doc duoc.
         */
        double nguongVat;
        BoTile nguongVat(final double nguong) {
            this.nguongVat = nguong;
            return this;
        }
        BoTile(final String ten, final String tenNho, final VatLieu... vatLieu) {
            this.ten = ten;
            this.tenNho = tenNho;
            this.vatLieu = vatLieu;
        }
    }

    static BoTile[] boTile() {
        return new BoTile[] {
            // Ban do ngoai troi (id < 110).
            new BoTile("t", "t_small",
                new VatLieu("co", g(r(0, 4), c(31, 41, 42, 43, 44, 45, 144, 145, 146, 147)), ds(CO, NEN)).sachHet(),
                new VatLieu("dat", c(7, 16, 17, 18, 19, 80, 81, 82), ds(DAT, NEN)).giuMau(0.3),
                new VatLieu("nuoc", c(141, 142, 143, 188, 189, 196, 197, 198, 199), ds(NUOC, PHU)).giuMau(0.85).tuongPhan(0.75).sachHet().chiKieu(CHAN),
                new VatLieu("nuoc_nong", c(141, 142, 143, 188, 189, 196, 197, 198, 199)).chiKieu(DI_DUOC),
                // Tan cay goc ghep tu nhieu tile: phan trong cua tuong DTEF la mang phang, thay vao
                // thanh o vuong xanh. Nen giu hinh (chi doi mau), chi chan khong cho bi xep la "co".
                new VatLieu("cay", r(162, 174)).chiKieu(CHAN),
                // Vach dat nau (mat vach) cung giu hinh: thay bang van dat se mat do cao.
                new VatLieu("vach", g(c(11, 12), r(127, 133))).chiKieu(CHAN),
                // Bong toi cua mep co de bi xep nham la "da" (van da nau cam) -> chi nhan khi chiem nhieu.
                new VatLieu("da", r(222, 231), ds(DA, TUONG)).giuMau(0.5).toiThieu(0.35)),
            // Hang / sa mac (110..200).
            new BoTile("t_hang", "t_hang_s",
                // Vuc toi (o 8, chan) GIU den nhu vung "ngoai ban do" cua hang PMD; van da PMD chi
                // nam tren dai vach (o mep) va giu dung duong vien goc (khongThay).
                new VatLieu("vuc", c(8)).nguyenMau().chiKieu(CHAN),
                new VatLieu("nen_hang", c(0, 4, 6, 7, 16, 17, 22, 23), ds(NEN_HANG, NEN)).giuMau(1.0).tuongPhan(0.4).sachHet(),
                new VatLieu("da_hang", g(r(9, 15), r(19, 21), r(24, 30)), ds(DA_HANG, TUONG)).giuMau(0.75)
                    .tuongPhan(0.8).chiKieu(CHAN).khongThay(),
                new VatLieu("cat", g(c(31, 32, 39, 40, 41, 42, 43, 44), r(59, 60), r(82, 84)), ds(DAT, NEN)).giuMau(0.3),
                new VatLieu("nuoc", r(88, 93), ds(NUOC, PHU)).giuMau(0.85).tuongPhan(0.75).sachHet().chiKieu(CHAN),
                new VatLieu("nuoc_nong", r(88, 93)).chiKieu(DI_DUOC)).nguongVat(NGUONG_VAT_HANG),
            // Thanh / kinh thanh (id > 200).
            new BoTile("t_thanh", "t_thanh_s",
                new VatLieu("co", g(r(0, 4), r(27, 31), r(73, 76), c(142)), ds(CO, NEN)).sachHet(),
                new VatLieu("dat", g(c(7, 8), r(36, 42)), ds(DAT, NEN)).giuMau(0.3),
                new VatLieu("nen_lat", g(r(129, 136), c(205)), ds(LAT, NEN)).giuMau(0.5),
                new VatLieu("nuoc", g(c(143, 149, 150), r(154, 157)), ds(NUOC, PHU)).giuMau(0.85).tuongPhan(0.75).sachHet().chiKieu(CHAN),
                new VatLieu("nuoc_nong", g(c(143, 149, 150), r(154, 157))).chiKieu(DI_DUOC),
                new VatLieu("da", r(46, 54), ds(DA, TUONG)).giuMau(0.5)),
        };
    }

    // ------------------------------------------------------------------ mau Lab

    static double tuyenTinh(final double v) {
        final double x = v / 255.0;
        return x <= 0.04045 ? x / 12.92 : Math.pow((x + 0.055) / 1.055, 2.4);
    }

    static double phiTuyen(final double x) {
        final double v = x <= 0.0031308 ? 12.92 * x : 1.055 * Math.pow(x, 1 / 2.4) - 0.055;
        return Math.max(0, Math.min(255, v * 255.0));
    }

    static double fLab(final double t) {
        return t > 0.008856 ? Math.cbrt(t) : 7.787 * t + 16.0 / 116.0;
    }

    static double fLabNguoc(final double t) {
        final double t3 = t * t * t;
        return t3 > 0.008856 ? t3 : (t - 16.0 / 116.0) / 7.787;
    }

    static double[] lab(final int argb) {
        final double rr = tuyenTinh((argb >> 16) & 255);
        final double gg = tuyenTinh((argb >> 8) & 255);
        final double bb = tuyenTinh(argb & 255);
        final double x = (0.4124 * rr + 0.3576 * gg + 0.1805 * bb) / 0.95047;
        final double y = 0.2126 * rr + 0.7152 * gg + 0.0722 * bb;
        final double z = (0.0193 * rr + 0.1192 * gg + 0.9505 * bb) / 1.08883;
        final double fx = fLab(x), fy = fLab(y), fz = fLab(z);
        return new double[] {116 * fy - 16, 500 * (fx - fy), 200 * (fy - fz)};
    }

    static int rgb(final double[] lab) {
        final double fy = (lab[0] + 16) / 116.0;
        final double fx = fy + lab[1] / 500.0;
        final double fz = fy - lab[2] / 200.0;
        final double x = fLabNguoc(fx) * 0.95047, y = fLabNguoc(fy), z = fLabNguoc(fz) * 1.08883;
        final double rr = 3.2406 * x - 1.5372 * y - 0.4986 * z;
        final double gg = -0.9689 * x + 1.8758 * y + 0.0415 * z;
        final double bb = 0.0557 * x - 0.2040 * y + 1.0570 * z;
        return ((int) Math.round(phiTuyen(rr)) << 16) | ((int) Math.round(phiTuyen(gg)) << 8)
            | (int) Math.round(phiTuyen(bb));
    }

    static double kc(final double[] a, final double[] b) {
        final double d0 = a[0] - b[0], d1 = a[1] - b[1], d2 = a[2] - b[2];
        return Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }

    // ------------------------------------------------------------------ tile PMD

    /** Mot tile PMD da thu nho 16x16 (ARGB) kem dac trung. */
    static final class TilePmd {
        String nguon;              // vd "TinyWoods/tileset_1.png nen(2,3)"
        int[] px = new int[256];
        double[] dacTrung;
        double[] labTb;
    }

    /** Thu nho 24 -> 16 trung binh dien tich, alpha nhan truoc; roi bam ve bang mau tile nguon. */
    static int[] thuNho(final BufferedImage anh, final int x0, final int y0) {
        final int[] goc = new int[24 * 24];
        anh.getRGB(x0, y0, 24, 24, goc, 0, 24);
        final List<double[]> bangLab = new ArrayList<double[]>();
        final List<Integer> bang = new ArrayList<Integer>();
        for (final int p : goc) {
            if ((p >>> 24) >= 128 && !bang.contains(p | 0xFF000000)) {
                bang.add(p | 0xFF000000);
                bangLab.add(lab(p));
            }
        }
        final int[] ra = new int[256];
        final double ty = 24.0 / 16.0;
        for (int oy = 0; oy < 16; oy++) {
            for (int ox = 0; ox < 16; ox++) {
                double a = 0, rr = 0, gg = 0, bb = 0, w = 0;
                final double sx0 = ox * ty, sx1 = sx0 + ty, sy0 = oy * ty, sy1 = sy0 + ty;
                for (int sy = (int) sy0; sy < Math.ceil(sy1); sy++) {
                    final double wy = Math.min(sy1, sy + 1) - Math.max(sy0, sy);
                    for (int sx = (int) sx0; sx < Math.ceil(sx1); sx++) {
                        final double wx = Math.min(sx1, sx + 1) - Math.max(sx0, sx);
                        final int p = goc[sy * 24 + sx];
                        final double al = ((p >>> 24) & 255) / 255.0;
                        final double ww = wx * wy;
                        w += ww;
                        a += al * ww;
                        rr += tuyenTinh((p >> 16) & 255) * al * ww;
                        gg += tuyenTinh((p >> 8) & 255) * al * ww;
                        bb += tuyenTinh(p & 255) * al * ww;
                    }
                }
                if (a / w < 0.5) {
                    ra[oy * 16 + ox] = 0;
                    continue;
                }
                final int tb = ((int) Math.round(phiTuyen(rr / a)) << 16) | ((int) Math.round(phiTuyen(gg / a)) << 8)
                    | (int) Math.round(phiTuyen(bb / a));
                final double[] l = lab(tb);
                int tot = tb;
                double ktot = Double.MAX_VALUE;
                for (int i = 0; i < bang.size(); i++) {
                    final double k = kc(l, bangLab.get(i));
                    if (k < ktot) {
                        ktot = k;
                        tot = bang.get(i);
                    }
                }
                ra[oy * 16 + ox] = 0xFF000000 | (tot & 0xFFFFFF);
            }
        }
        return ra;
    }

    /** Dac trung: 16 o 4x4 x (L,a,b,alpha) + mat do canh. */
    static double[] dacTrung(final int[] px) {
        final double[] k = new double[16 * 4 + 1];
        final double[][] l = new double[256][];
        for (int i = 0; i < 256; i++) {
            l[i] = (px[i] >>> 24) >= 128 ? lab(px[i]) : null;
        }
        for (int o = 0; o < 16; o++) {
            final int ox = (o % 4) * 4, oy = (o / 4) * 4;
            double s0 = 0, s1 = 0, s2 = 0;
            int dem = 0;
            for (int y = oy; y < oy + 4; y++) {
                for (int x = ox; x < ox + 4; x++) {
                    final double[] v = l[y * 16 + x];
                    if (v != null) {
                        s0 += v[0];
                        s1 += v[1];
                        s2 += v[2];
                        dem++;
                    }
                }
            }
            k[o * 4] = dem == 0 ? 0 : s0 / dem;
            k[o * 4 + 1] = dem == 0 ? 0 : s1 / dem;
            k[o * 4 + 2] = dem == 0 ? 0 : s2 / dem;
            k[o * 4 + 3] = dem / 16.0;
        }
        double canh = 0;
        int dem = 0;
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 15; x++) {
                if (l[y * 16 + x] != null && l[y * 16 + x + 1] != null) {
                    canh += Math.abs(l[y * 16 + x][0] - l[y * 16 + x + 1][0]);
                    dem++;
                }
                if (l[x * 16 + y] != null && l[(x + 1) * 16 + y] != null) {
                    canh += Math.abs(l[x * 16 + y][0] - l[(x + 1) * 16 + y][0]);
                    dem++;
                }
            }
        }
        k[64] = dem == 0 ? 0 : canh / dem;
        return k;
    }

    static double kcDacTrung(final double[] a, final double[] b) {
        double s = 0;
        for (int o = 0; o < 16; o++) {
            final double w = Math.min(a[o * 4 + 3], b[o * 4 + 3]);
            final double d0 = a[o * 4] - b[o * 4], d1 = a[o * 4 + 1] - b[o * 4 + 1], d2 = a[o * 4 + 2] - b[o * 4 + 2];
            s += w * Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2) + 40 * Math.abs(a[o * 4 + 3] - b[o * 4 + 3]);
        }
        return s / 16 + 1.5 * Math.abs(a[64] - b[64]);
    }

    static double[] labTb(final int[] px) {
        double s0 = 0, s1 = 0, s2 = 0;
        int dem = 0;
        for (final int p : px) {
            if ((p >>> 24) >= 128) {
                final double[] l = lab(p);
                s0 += l[0];
                s1 += l[1];
                s2 += l[2];
                dem++;
            }
        }
        return dem == 0 ? new double[3] : new double[] {s0 / dem, s1 / dem, s2 / dem};
    }

    /**
     * Cac tile "lap day" cua mot khoi DTEF (khong co mep). Khoi nen: ca 48 o. Khoi tuong/phu:
     * o (1,1) cua phong mau 3x3 o goc trai tren la o ben trong hoan toan.
     */
    static List<TilePmd> tileDay(final Nguon ng) throws IOException {
        final List<TilePmd> k = new ArrayList<TilePmd>();
        TilePmd mauTrong = null;
        for (int bien = 0; bien < 3; bien++) {
            final File f = new File(THU_MUC_RAW + ng.bo + "/tileset_" + bien + ".png");
            if (!f.isFile()) {
                continue;
            }
            final BufferedImage anh = ImageIO.read(f);
            final List<int[]> oDung = new ArrayList<int[]>();
            for (int y = 0; y < 8; y++) {
                for (int x = 0; x < 6; x++) {
                    oDung.add(new int[] {x, y});
                }
            }
            for (final int[] xy : oDung) {
                final int x0 = ng.khoi * 144 + xy[0] * 24, y0 = xy[1] * 24;
                boolean trong = false;
                for (int y = y0; y < y0 + 24 && !trong; y++) {
                    for (int x = x0; x < x0 + 24; x++) {
                        if ((anh.getRGB(x, y) >>> 24) < 255) {
                            trong = true;
                            break;
                        }
                    }
                }
                if (trong) {
                    continue;
                }
                final TilePmd t = new TilePmd();
                t.nguon = ng.bo + "/tileset_" + bien + ".png " + TEN_KHOI[ng.khoi] + "@" + xy[0] + "_" + xy[1];
                t.px = thuNho(anh, x0, y0);
                t.dacTrung = dacTrung(t.px);
                t.labTb = labTb(t.px);
                if (ng.khoi != NEN) {
                    // Tuong/nuoc: o (1,1) cua bien 0 la o ben trong hoan toan; o khac chi nhan
                    // khi giong no (cung la o ben trong, khong co mep).
                    if (bien == 0 && xy[0] == 1 && xy[1] == 1) {
                        mauTrong = t;
                    } else if (mauTrong == null || kcDacTrung(mauTrong.dacTrung, t.dacTrung) > NGUONG_O_TRONG) {
                        continue;
                    }
                }
                k.add(t);
            }
        }
        return k;
    }

    // ------------------------------------------------------------------ k-means bang mau

    static double[][] kMeans(final List<double[]> diem, final int k) {
        final List<double[]> s = new ArrayList<double[]>(diem);
        s.sort((a, b) -> Double.compare(a[0], b[0]));
        final int kk = Math.min(k, s.size());
        final double[][] tam = new double[kk][];
        for (int i = 0; i < kk; i++) {
            tam[i] = s.get((int) ((i + 0.5) * s.size() / kk)).clone();
        }
        for (int lap = 0; lap < 15; lap++) {
            final double[][] tong = new double[kk][3];
            final int[] dem = new int[kk];
            for (final double[] p : s) {
                int tot = 0;
                for (int i = 1; i < kk; i++) {
                    if (kc(p, tam[i]) < kc(p, tam[tot])) {
                        tot = i;
                    }
                }
                tong[tot][0] += p[0];
                tong[tot][1] += p[1];
                tong[tot][2] += p[2];
                dem[tot]++;
            }
            for (int i = 0; i < kk; i++) {
                if (dem[i] > 0) {
                    tam[i] = new double[] {tong[i][0] / dem[i], tong[i][1] / dem[i], tong[i][2] / dem[i]};
                }
            }
        }
        return tam;
    }

    static int[] tile(final BufferedImage anh, final int i) {
        final int[] p = new int[256];
        anh.getRGB(0, i * 16, 16, 16, p, 0, 16);
        return p;
    }

    // ------------------------------------------------------------------ chuong trinh chinh

    /** Ket qua cho mot tile goc. */
    static final class KetQua {
        int[] px;
        String vatLieuChinh;
        String cach;
        String nguon;
        double tyLeGiu;
    }

    public static void main(final String[] args) throws Exception {
        final boolean khongTai = Arrays.asList(args).contains("--khong-tai");
        if (!khongTai) {
            taiNguon();
        }
        final BoTile[] bo = boTile();
        final StringBuilder csv = new StringBuilder("bo_tile,chi_so,kieu,vat_lieu_chinh,cach,nguon_pmd,ty_le_pixel_giu\n");
        final List<KetQua[]> tatCa = new ArrayList<KetQua[]>();
        new File(THU_MUC_RA).mkdirs();
        for (final BoTile b : bo) {
            b.cu = ImageIO.read(new File("map/" + b.ten + ".png"));
            b.kieu = docKieu("cMap/" + b.ten + ".type");
            final int soTile = b.cu.getHeight() / 16;
            if (b.cu.getWidth() != 16 || b.cu.getHeight() % 16 != 0 || soTile != b.kieu.length) {
                throw new IllegalStateException(b.ten + ": anh " + b.cu.getWidth() + "x" + b.cu.getHeight()
                    + " khong khop " + b.kieu.length + " kieu");
            }
            chuanBiVatLieu(b);
            final KetQua[] kq = new KetQua[soTile];
            final BufferedImage moi = new BufferedImage(16, soTile * 16, BufferedImage.TYPE_INT_ARGB);
            final BufferedImage nho = new BufferedImage(1, soTile, BufferedImage.TYPE_INT_ARGB);
            for (int i = 0; i < soTile; i++) {
                kq[i] = doiTile(b, tile(b.cu, i), b.kieu[i]);
                moi.setRGB(0, i * 16, 16, 16, kq[i].px, 0, 16);
                nho.setRGB(0, i, mauTb(kq[i].px));
                csv.append(b.ten).append(',').append(i).append(',').append(b.kieu[i]).append(',')
                    .append(kq[i].vatLieuChinh).append(',').append(kq[i].cach).append(',')
                    .append(kq[i].nguon).append(',').append(String.format("%.2f", kq[i].tyLeGiu)).append('\n');
            }
            tatCa.add(kq);
            ghiAnh(moi, b.ten);
            ghiAnh(nho, b.tenNho);
            int thay = 0, tron = 0, giu = 0;
            for (final KetQua k : kq) {
                if (k.cach.equals("thay")) {
                    thay++;
                } else if (k.cach.equals("tron")) {
                    tron++;
                } else {
                    giu++;
                }
            }
            System.out.println(b.ten + ": " + soTile + " tile, thay " + thay + ", tron " + tron + ", doi mau " + giu);
            for (final VatLieu v : b.vatLieu) {
                System.out.println("  " + v.ten + " <- " + v.nguonChon + " (" + v.tiles.size() + " tile day)");
            }
        }
        Files.write(new File("docs/pokemon/bang_tile.csv").toPath(), csv.toString().getBytes(StandardCharsets.UTF_8));
        final List<BufferedImage[]> trangTri = doiTrangTri(bo[0]);
        veXemTruoc(bo, tatCa, trangTri);
        kiemLai(bo);
        System.out.println("Xong.");
    }

    static void ghiAnh(final BufferedImage anh, final String ten) throws IOException {
        ghiPng(anh, new File(THU_MUC_RA + ten + ".png"));
        final File f = new File(THU_MUC_RA + ten + ".png");
        // t_hang/t_thanh di qua goi dang nhap voi do dai writeShort: qua 32767 byte thi doi sang
        // anh bang mau 8 bit (255 mau + 1 trong suot) cho vua.
        if (!ten.equals("t") && f.length() > Short.MAX_VALUE) {
            System.out.println("  " + ten + ".png " + f.length() + " byte > 32767, doi sang bang mau 8 bit");
            ghiPng(bangMau8Bit(anh), f);
        }
        ghiClient("poke_tile", ten + ".bytes", Files.readAllBytes(f.toPath()));
    }

    /** Luong tu hoa 255 mau (k-means trong Lab, trong so theo tan suat) + chi so 0 trong suot. */
    static BufferedImage bangMau8Bit(final BufferedImage anh) {
        final java.util.Map<Integer, Integer> tanSuat = new java.util.HashMap<Integer, Integer>();
        final int w = anh.getWidth(), h = anh.getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                final int p = anh.getRGB(x, y);
                if ((p >>> 24) >= 128) {
                    tanSuat.merge(p | 0xFF000000, 1, Integer::sum);
                }
            }
        }
        final List<Integer> mau = new ArrayList<Integer>(tanSuat.keySet());
        mau.sort((a, b) -> tanSuat.get(b) - tanSuat.get(a) != 0 ? tanSuat.get(b) - tanSuat.get(a) : Integer.compare(a, b));
        final int k = Math.min(255, mau.size());
        final double[][] tam = new double[k][];
        for (int i = 0; i < k; i++) {
            tam[i] = lab(mau.get(i));
        }
        final double[][] lMau = new double[mau.size()][];
        for (int i = 0; i < mau.size(); i++) {
            lMau[i] = lab(mau.get(i));
        }
        final int[] gan = new int[mau.size()];
        for (int lap = 0; lap < 12; lap++) {
            final double[][] tong = new double[k][3];
            final double[] dem = new double[k];
            for (int i = 0; i < mau.size(); i++) {
                int tot = 0;
                double m = Double.MAX_VALUE;
                for (int j = 0; j < k; j++) {
                    final double d = kc(lMau[i], tam[j]);
                    if (d < m) {
                        m = d;
                        tot = j;
                    }
                }
                gan[i] = tot;
                final double ts = tanSuat.get(mau.get(i));
                for (int c = 0; c < 3; c++) {
                    tong[tot][c] += lMau[i][c] * ts;
                }
                dem[tot] += ts;
            }
            for (int j = 0; j < k; j++) {
                if (dem[j] > 0) {
                    tam[j] = new double[] {tong[j][0] / dem[j], tong[j][1] / dem[j], tong[j][2] / dem[j]};
                }
            }
        }
        final byte[] rr = new byte[k + 1], gg = new byte[k + 1], bb = new byte[k + 1], aa = new byte[k + 1];
        for (int j = 0; j < k; j++) {
            final int c = rgb(tam[j]);
            rr[j + 1] = (byte) (c >> 16);
            gg[j + 1] = (byte) (c >> 8);
            bb[j + 1] = (byte) c;
            aa[j + 1] = (byte) 255;
        }
        final java.awt.image.IndexColorModel cm = new java.awt.image.IndexColorModel(8, k + 1, rr, gg, bb, aa);
        final BufferedImage ra = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_INDEXED, cm);
        final java.util.Map<Integer, Integer> chiSo = new java.util.HashMap<Integer, Integer>();
        for (int i = 0; i < mau.size(); i++) {
            chiSo.put(mau.get(i), gan[i] + 1);
        }
        final java.awt.image.WritableRaster r = ra.getRaster();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                final int p = anh.getRGB(x, y);
                r.setSample(x, y, 0, (p >>> 24) >= 128 ? chiSo.get(p | 0xFF000000) : 0);
            }
        }
        return ra;
    }

    static void ghiPng(final BufferedImage anh, final File f) throws IOException {
        // Nen muc cao nhat: t_hang/t_thanh di qua goi dang nhap voi do dai writeShort (< 32768).
        final javax.imageio.ImageWriter viet = ImageIO.getImageWritersByFormatName("png").next();
        final javax.imageio.ImageWriteParam ts = viet.getDefaultWriteParam();
        if (ts.canWriteCompressed()) {
            ts.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
            ts.setCompressionQuality(0f);
        }
        f.delete();
        try (javax.imageio.stream.ImageOutputStream ra = ImageIO.createImageOutputStream(f)) {
            viet.setOutput(ra);
            viet.write(null, new javax.imageio.IIOImage(anh, null, null), ts);
        } finally {
            viet.dispose();
        }
    }

    static int[] docKieu(final String duong) throws IOException {
        final byte[] b = Files.readAllBytes(new File(duong).toPath());
        final int[] k = new int[b.length];
        for (int i = 0; i < b.length; i++) {
            k[i] = b[i] & 255;
        }
        return k;
    }

    static int mauTb(final int[] px) {
        double rr = 0, gg = 0, bb = 0;
        int dem = 0;
        for (final int p : px) {
            if ((p >>> 24) >= 128) {
                rr += (p >> 16) & 255;
                gg += (p >> 8) & 255;
                bb += p & 255;
                dem++;
            }
        }
        if (dem == 0) {
            return 0xFF000000;
        }
        return 0xFF000000 | ((int) (rr / dem) << 16) | ((int) (gg / dem) << 8) | (int) (bb / dem);
    }

    /** Bang mau vat lieu tu tile mau; chon nguon PMD gan mau nhat; chon tile co ban. */
    static void chuanBiVatLieu(final BoTile b) throws IOException {
        for (final VatLieu v : b.vatLieu) {
            final List<double[]> diem = new ArrayList<double[]>();
            final List<double[]> dtMau = new ArrayList<double[]>();
            double sang = 0;
            for (final int i : v.mau) {
                final int[] p = tile(b.cu, i);
                dtMau.add(dacTrung(p));
                for (final int q : p) {
                    if ((q >>> 24) >= 128) {
                        final double[] l = lab(q);
                        diem.add(l);
                        sang += l[0];
                    }
                }
            }
            v.bangMau = kMeans(diem, 6);
            v.sangTb = sang / diem.size();
            final double[] tbMau = new double[3];
            for (final double[] l : diem) {
                tbMau[0] += l[0] / diem.size();
                tbMau[1] += l[1] / diem.size();
                tbMau[2] += l[2] / diem.size();
            }
            if (v.ungVien.length == 0) {
                continue;   // vat lieu "giu": chi de pixel khong bi xep nham sang vat lieu khac
            }
            // Chon nguon: trung binh khoang cach dac trung tu tile mau goc toi tile PMD tot nhat.
            double tot = Double.MAX_VALUE;
            for (final Nguon ng : v.ungVien) {
                final List<TilePmd> ds = tileDay(ng);
                if (ds.isEmpty()) {
                    continue;
                }
                double s = 0;
                for (final double[] dt : dtMau) {
                    double m = Double.MAX_VALUE;
                    for (final TilePmd t : ds) {
                        m = Math.min(m, kcDacTrung(dt, t.dacTrung));
                    }
                    s += m;
                }
                s /= dtMau.size();
                if (s < tot) {
                    tot = s;
                    v.nguonChon = ng;
                    v.tiles = ds;
                }
            }
            if (v.nguonChon == null) {
                throw new IllegalStateException("Khong co nguon PMD cho " + b.ten + "/" + v.ten
                    + " (chay khong co --khong-tai de tai)");
            }
            if (v.sachHet) {
                final List<TilePmd> sach = new ArrayList<TilePmd>();
                for (final TilePmd t : v.tiles) {
                    sach.add(lamSach(t));
                }
                v.tiles = sach;
            }
            if (v.giuMau > 0 || v.tuongPhan != 1) {
                keoMau(v, tbMau);
            }
            // Tile co ban (van cho tile lan) lap lai o MOI tile lan, nen tranh tile co chi tiet noi
            // (hoa, soi): phat them theo ty le pixel lech xa mau trung binh cua chinh no.
            double m = Double.MAX_VALUE;
            for (final TilePmd t : v.tiles) {
                final double k = kc(t.labTb, tbMau) + 100 * tyLeLech(t);
                if (k < m) {
                    m = k;
                    v.coBan = t;
                }
            }
            v.coBan = lamSach(v.coBan);
        }
    }

    /**
     * Ban "sach" cua tile co ban: pixel lech xa mau trung binh (hoa, soi le) thay bang pixel lan
     * can khong lech. Tile co ban lap lai o moi tile lan, nen mot bong hoa se thanh hang cham.
     */
    static TilePmd lamSach(final TilePmd t) {
        final TilePmd k = new TilePmd();
        k.nguon = t.nguon;
        k.px = t.px.clone();
        final boolean[] lech = new boolean[256];
        for (int i = 0; i < 256; i++) {
            lech[i] = (t.px[i] >>> 24) >= 128 && kc(lab(t.px[i]), t.labTb) > 18;
        }
        for (int i = 0; i < 256; i++) {
            if (!lech[i]) {
                continue;
            }
            int thay = 0xFF000000 | rgb(t.labTb);
            for (int r = 1; r < 16 && thay == (0xFF000000 | rgb(t.labTb)); r++) {
                final int[][] huong = {{-r, 0}, {r, 0}, {0, -r}, {0, r}};
                for (final int[] h : huong) {
                    final int x = i % 16 + h[0], y = i / 16 + h[1];
                    if (x >= 0 && x < 16 && y >= 0 && y < 16 && !lech[y * 16 + x]) {
                        thay = t.px[y * 16 + x];
                        break;
                    }
                }
            }
            k.px[i] = thay;
        }
        k.dacTrung = dacTrung(k.px);
        k.labTb = labTb(k.px);
        return k;
    }

    static double tyLeLech(final TilePmd t) {
        int lech = 0;
        for (final int p : t.px) {
            if ((p >>> 24) >= 128 && kc(lab(p), t.labTb) > 25) {
                lech++;
            }
        }
        return lech / 256.0;
    }

    /** Dich mau moi tile PMD cua vat lieu ve phia mau trung binh goc (giu van, giu do tuong phan). */
    static void keoMau(final VatLieu v, final double[] tbMau) {
        final double[] tbPmd = new double[3];
        for (final TilePmd t : v.tiles) {
            for (int j = 0; j < 3; j++) {
                tbPmd[j] += t.labTb[j] / v.tiles.size();
            }
        }
        final double[] dich = new double[3];
        for (int j = 0; j < 3; j++) {
            dich[j] = v.giuMau * (tbMau[j] - tbPmd[j]);
        }
        final double sangPmd = tbPmd[0] + dich[0];
        for (final TilePmd t : v.tiles) {
            for (int i = 0; i < 256; i++) {
                if ((t.px[i] >>> 24) >= 128) {
                    final double[] l = lab(t.px[i]);
                    for (int j = 0; j < 3; j++) {
                        l[j] += dich[j];
                    }
                    // Diu ca do sang lan sac do quanh trung binh (van van con, it lom dom hon).
                    l[0] = sangPmd + v.tuongPhan * (l[0] - sangPmd);
                    l[1] = tbPmd[1] + dich[1] + v.tuongPhan * (l[1] - tbPmd[1] - dich[1]);
                    l[2] = tbPmd[2] + dich[2] + v.tuongPhan * (l[2] - tbPmd[2] - dich[2]);
                    l[0] = Math.max(0, Math.min(100, l[0]));
                    t.px[i] = 0xFF000000 | rgb(l);
                }
            }
            t.dacTrung = dacTrung(t.px);
            t.labTb = labTb(t.px);
        }
    }

    static KetQua doiTile(final BoTile b, final int[] px, final int kieu) {
        final KetQua kq = new KetQua();
        final int nv = b.vatLieu.length;
        final int[] gan = new int[256];
        final int[] dem = new int[nv + 1];
        int duc = 0;
        final double[][] l = new double[256][];
        for (int i = 0; i < 256; i++) {
            gan[i] = -1;
            if ((px[i] >>> 24) < 128) {
                continue;
            }
            duc++;
            l[i] = lab(px[i]);
            double m = NGUONG_PIXEL;
            for (int v = 0; v < nv; v++) {
                if (b.vatLieu[v].chiKieu >= 0 && b.vatLieu[v].chiKieu != kieu) {
                    continue;
                }
                for (final double[] t : b.vatLieu[v].bangMau) {
                    final double k = kc(l[i], t);
                    if (k < m) {
                        m = k;
                        gan[i] = v;
                    }
                }
            }
            dem[gan[i] < 0 ? nv : gan[i]]++;
        }
        // Vat lieu qua it trong tile -> nhieu, coi la "khac".
        for (int v = 0; v < nv; v++) {
            if (dem[v] > 0 && dem[v] < b.vatLieu[v].toiThieu * duc) {
                for (int i = 0; i < 256; i++) {
                    if (gan[i] == v) {
                        gan[i] = -1;
                    }
                }
                dem[nv] += dem[v];
                dem[v] = 0;
            }
        }
        int chinh = -1;
        for (int v = 0; v < nv; v++) {
            if (dem[v] > 0 && (chinh < 0 || dem[v] > dem[chinh])) {
                chinh = v;
            }
        }
        kq.vatLieuChinh = chinh < 0 ? "khac" : b.vatLieu[chinh].ten;
        kq.tyLeGiu = duc == 0 ? 0 : dem[nv] / (double) duc;
        kq.px = new int[256];
        // Tile co vat noi (bo tile co nguongVat): do lech chuan L cua phan khong phai "nguyenMau".
        boolean coVat = false;
        if (b.nguongVat > 0) {
            double s1 = 0, s2 = 0;
            int nn = 0;
            for (int i = 0; i < 256; i++) {
                if (l[i] != null && !(gan[i] >= 0 && b.vatLieu[gan[i]].nguyenMau)) {
                    s1 += l[i][0];
                    s2 += l[i][0] * l[i][0];
                    nn++;
                }
            }
            coVat = nn > 16 && Math.sqrt(Math.max(0, s2 / nn - (s1 / nn) * (s1 / nn))) > b.nguongVat;
        }
        if (duc == 256 && chinh >= 0 && dem[chinh] >= NGUONG_THUAN * duc && b.vatLieu[chinh].coBan != null
            && !b.vatLieu[chinh].khongThay && !coVat) {
            // Tile thuan: thay nguyen bang tile PMD khop nhat cua vat lieu.
            final VatLieu v = b.vatLieu[chinh];
            final double[] dt = dacTrung(px);
            TilePmd tot = v.coBan;
            double m = Double.MAX_VALUE;
            for (final TilePmd t : v.tiles) {
                final double k = kcDacTrung(dt, t.dacTrung);
                if (k < m) {
                    m = k;
                    tot = t;
                }
            }
            System.arraycopy(tot.px, 0, kq.px, 0, 256);
            kq.cach = "thay";
            kq.nguon = tot.nguon;
            return kq;
        }
        final StringBuilder nguon = new StringBuilder();
        for (int v = 0; v < nv; v++) {
            if (dem[v] > 0) {
                if (nguon.length() > 0) {
                    nguon.append(" + ");
                }
                nguon.append(b.vatLieu[v].ten).append('=')
                    .append(b.vatLieu[v].coBan == null ? "giu" : b.vatLieu[v].coBan.nguon);
            }
        }
        for (int i = 0; i < 256; i++) {
            if (l[i] == null) {
                kq.px[i] = 0;
                continue;
            }
            if (gan[i] >= 0 && b.vatLieu[gan[i]].nguyenMau) {
                kq.px[i] = px[i] | 0xFF000000;
            } else if (gan[i] >= 0 && b.vatLieu[gan[i]].coBan != null
                && !(coVat && Math.abs(l[i][0] - b.vatLieu[gan[i]].sangTb) > NGUONG_GIU_PIXEL_VAT)) {
                final VatLieu v = b.vatLieu[gan[i]];
                final double[] lt = lab(v.coBan.px[i]);
                lt[0] = Math.max(0, Math.min(100, lt[0] + (coVat ? 1.0 : HE_SO_SANG) * (l[i][0] - v.sangTb)));
                kq.px[i] = 0xFF000000 | rgb(lt);
            } else {
                kq.px[i] = 0xFF000000 | doiMauKhac(b, l[i]);
            }
        }
        int giu = dem[nv];
        for (int v = 0; v < nv; v++) {
            if (b.vatLieu[v].coBan == null) {
                giu += dem[v];
            }
        }
        kq.tyLeGiu = duc == 0 ? 0 : giu / (double) duc;
        kq.cach = chinh < 0 || giu > 0.5 * duc ? "doi_mau" : "tron";
        kq.nguon = nguon.length() == 0 ? "-" : nguon.toString();
        return kq;
    }

    /**
     * Pixel khong thuoc vat lieu nao (mai, go, do vat): giu hinh, keo mau ve phia bang mau
     * PMD - dich sac do (a,b) theo do lech trung binh giua vat lieu goc va van PMD da chon,
     * co trong so theo khoang cach toi tung vat lieu. Nho hon: giu nguyen mau goc.
     */
    static int doiMauKhac(final BoTile b, final double[] l) {
        double tw = 0, da = 0, db = 0, dl = 0;
        for (final VatLieu v : b.vatLieu) {
            if (v.coBan == null) {
                continue;
            }
            double m = Double.MAX_VALUE;
            for (final double[] t : v.bangMau) {
                m = Math.min(m, kc(l, t));
            }
            final double w = 1.0 / (1 + m * m / 400.0);
            final double[] tbK = tbBang(v.bangMau);
            final double[] tbP = v.coBan.labTb;
            da += w * (tbP[1] - tbK[1]);
            db += w * (tbP[2] - tbK[2]);
            dl += w * (tbP[0] - tbK[0]);
            tw += w;
        }
        final double[] k = l.clone();
        if (tw > 0) {
            final double heSo = Math.min(1, tw);
            k[0] += 0.5 * heSo * dl / tw;
            k[1] += heSo * da / tw;
            k[2] += heSo * db / tw;
        }
        k[0] = Math.max(0, Math.min(100, k[0]));
        return rgb(k);
    }

    static double[] tbBang(final double[][] bang) {
        final double[] k = new double[3];
        for (final double[] t : bang) {
            k[0] += t[0] / bang.length;
            k[1] += t[1] / bang.length;
            k[2] += t[2] / bang.length;
        }
        return k;
    }

    // ------------------------------------------------------------------ tai nguon

    static void taiNguon() throws IOException {
        for (final String[] t : THAY_VAT_THE) {
            final File f = new File(THU_MUC_OBJECT + t[1] + ".png");
            if (!f.isFile()) {
                f.getParentFile().mkdirs();
                taiTep(GOC_OBJECT + t[1] + ".png", f);
            }
        }
        for (final String bo : TAT_CA) {
            for (int bien = 0; bien < 3; bien++) {
                final File f = new File(THU_MUC_RAW + bo + "/tileset_" + bien + ".png");
                if (f.isFile() && f.length() > 0) {
                    continue;
                }
                f.getParentFile().mkdirs();
                taiTep(GOC_RAW + bo + "/tileset_" + bien + ".png", f);
            }
        }
    }

    static void taiTep(final String url, final File f) throws IOException {
        System.out.println("tai " + url);
        final HttpURLConnection kn = (HttpURLConnection) new URL(url).openConnection();
        kn.setConnectTimeout(20000);
        kn.setReadTimeout(60000);
        if (kn.getResponseCode() != 200) {
            System.out.println("  bo qua, HTTP " + kn.getResponseCode());
            return;
        }
        try (InputStream in = kn.getInputStream(); OutputStream out = Files.newOutputStream(f.toPath())) {
            final byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
            }
        }
    }

    // ------------------------------------------------------------------ xem truoc

    static final int[][] BAN_DO_MAU = {{1, 0}, {25, 0}, {110, 1}, {201, 2}};

    // ------------------------------------------------------------------ A2: trang tri

    /** Anh trang tri: doi mau tung pixel nhu tile "giu hinh" cua bo t (cung bang mau PMD), giu alpha. */
    static BufferedImage doiMauAnh(final BufferedImage goc, final BoTile b) {
        final int w = goc.getWidth(), h = goc.getHeight();
        final BufferedImage ra = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        final java.util.Map<Integer, Integer> nho = new java.util.HashMap<Integer, Integer>();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                final int p = goc.getRGB(x, y);
                final int a = p >>> 24;
                if (a == 0) {
                    ra.setRGB(x, y, 0);
                    continue;
                }
                Integer m = nho.get(p & 0xFFFFFF);
                if (m == null) {
                    m = doiMauKhac(b, lab(p));
                    nho.put(p & 0xFFFFFF, m);
                }
                ra.setRGB(x, y, (a << 24) | (m & 0xFFFFFF));
            }
        }
        return ra;
    }

    /**
     * Sinh ban Pokemon cua map/tree/&lt;i&gt; (u16 do dai + PNG + u16 do dai + 6 byte dx,dy,startx,
     * starty,endx,endy - GIU NGUYEN phan du lieu), map/cay/&lt;j&gt;.png va 7 anh dip le.
     * Tra ve cap (cu, moi) cua cay ban do + anh dip le de ve xem truoc.
     */
    static List<BufferedImage[]> doiTrangTri(final BoTile b) throws IOException {
        final List<BufferedImage[]> xem = new ArrayList<BufferedImage[]>();
        new File(THU_MUC_TRANG_TRI + "tree").mkdirs();
        new File(THU_MUC_TRANG_TRI + "cay").mkdirs();
        int soCay = 0, soCayTrong = 0;
        for (int i = 0; i < 256; i++) {
            final File f = new File("map/tree/" + i);
            if (!f.isFile()) {
                continue;
            }
            final byte[] d = Files.readAllBytes(f.toPath());
            final int dai = ((d[0] & 255) << 8) | (d[1] & 255);
            final BufferedImage goc = ImageIO.read(new java.io.ByteArrayInputStream(d, 2, dai));
            BufferedImage moi = doiMauAnh(goc, b);
            final byte[] duLieu = Arrays.copyOfRange(d, 2 + dai, d.length);
            for (final String[] t : THAY_VAT_THE) {
                final File fo = new File(THU_MUC_OBJECT + t[1] + ".png");
                if (t[0].equals(String.valueOf(i)) && fo.isFile() && duLieu.length >= 4) {
                    final BufferedImage pmd = ImageIO.read(fo);
                    moi = new BufferedImage(pmd.getWidth(), pmd.getHeight(), BufferedImage.TYPE_INT_ARGB);
                    moi.getGraphics().drawImage(pmd, 0, 0, null);
                    // duLieu = u16 do dai (=6) + dx, dy (sbyte) + ...
                    duLieu[2] = (byte) (duLieu[2] + (goc.getWidth() - pmd.getWidth()) / 2);
                    duLieu[3] = (byte) (duLieu[3] + goc.getHeight() - pmd.getHeight());
                }
            }
            final java.io.ByteArrayOutputStream png = new java.io.ByteArrayOutputStream();
            final File tam = File.createTempFile("cay", ".png");
            ghiPng(moi, tam);
            final byte[] pngMoi = Files.readAllBytes(tam.toPath());
            tam.delete();
            if (pngMoi.length > Short.MAX_VALUE) {
                throw new IllegalStateException("map/tree/" + i + ": PNG moi qua lon");
            }
            png.write(pngMoi.length >> 8);
            png.write(pngMoi.length);
            png.write(pngMoi);
            png.write(duLieu);
            Files.write(new File(THU_MUC_TRANG_TRI + "tree/" + i).toPath(), png.toByteArray());
            xem.add(new BufferedImage[] {goc, moi});
            soCay++;
        }
        for (int j = 0; j < 256; j++) {
            final File f = new File("map/cay/" + j + ".png");
            if (f.isFile()) {
                ghiPng(doiMauAnh(ImageIO.read(f), b), new File(THU_MUC_TRANG_TRI + "cay/" + j + ".png"));
                soCayTrong++;
            }
        }
        for (final String ten : ANH_DIP_LE) {
            final BufferedImage goc = ImageIO.read(new File("map/" + ten + ".png"));
            final BufferedImage moi = doiMauAnh(goc, b);
            ghiPng(moi, new File(THU_MUC_TRANG_TRI + ten + ".png"));
            xem.add(new BufferedImage[] {goc, moi});
        }
        System.out.println("trang tri: " + soCay + " cay/nha (map/tree), " + soCayTrong + " cay trong (map/cay), "
            + ANH_DIP_LE.length + " anh dip le -> " + THU_MUC_TRANG_TRI);
        return xem;
    }

    /** Doc cay cua mot ban do: {x, y, loai}. */
    static List<int[]> docCay(final byte[] d, int p) {
        final List<int[]> k = new ArrayList<int[]>();
        while (p + 2 < d.length) {
            int x = d[p++] & 255;
            if (x == 254) {
                x = 255;
            }
            if (x == 255) {
                break;
            }
            k.add(new int[] {x, d[p++] & 255, d[p++] & 255});
        }
        return k;
    }

    static void veXemTruoc(final BoTile[] bo, final List<KetQua[]> tatCa, final List<BufferedImage[]> trangTri)
        throws IOException {
        final int phong = 2, o = 16 * phong, cot = 12, rongO = o * 2 + 30;
        int cao = 0;
        final int[] hang = new int[bo.length];
        for (int b = 0; b < bo.length; b++) {
            hang[b] = (tatCa.get(b).length + cot - 1) / cot;
            cao += 20 + hang[b] * (o + 4);
        }
        // Them anh ban do mau (cu | moi) o duoi.
        final List<BufferedImage[]> banDo = new ArrayList<BufferedImage[]>();
        for (final int[] bd : BAN_DO_MAU) {
            final BoTile b = bo[bd[1]];
            final BufferedImage moi = ImageIO.read(new File(THU_MUC_RA + b.ten + ".png"));
            final BufferedImage[] cap = {veBanDo(bd[0], b.cu, "map/tree/"), veBanDo(bd[0], moi, THU_MUC_TRANG_TRI + "tree/")};
            if (cap[0] != null) {
                banDo.add(cap);
                cao += 20 + cap[0].getHeight();
            }
        }
        // Trang tri: cap cu|moi xep theo hang, rong toi da 1450.
        final int rongTT = 1450;
        int xTT = 0, hangTT = 0, caoTT = 20;
        for (final BufferedImage[] c : trangTri) {
            final int rr = c[0].getWidth() * 2 + 8;
            if (xTT + rr > rongTT) {
                caoTT += hangTT + 6;
                xTT = 0;
                hangTT = 0;
            }
            xTT += rr + 12;
            hangTT = Math.max(hangTT, c[0].getHeight());
        }
        caoTT += hangTT + 6;
        cao += caoTT;
        final int rong = Math.max(Math.max(cot * rongO, 2 * 360 * 2 + 10), rongTT);
        final BufferedImage anh = new BufferedImage(rong, cao, BufferedImage.TYPE_INT_RGB);
        final Graphics2D gr = anh.createGraphics();
        gr.setColor(new Color(40, 40, 48));
        gr.fillRect(0, 0, rong, cao);
        gr.setFont(new Font("SansSerif", Font.PLAIN, 11));
        int y = 0;
        for (int b = 0; b < bo.length; b++) {
            gr.setColor(Color.WHITE);
            gr.drawString(bo[b].ten + ".png: moi o = tile cu | tile moi (so = chi so, * = o chan)", 4, y + 14);
            y += 20;
            final BufferedImage moi = ImageIO.read(new File(THU_MUC_RA + bo[b].ten + ".png"));
            final KetQua[] kq = tatCa.get(b);
            for (int i = 0; i < kq.length; i++) {
                final int x = (i % cot) * rongO, yy = y + (i / cot) * (o + 4);
                gr.setColor(new Color(255, 0, 255));
                gr.fillRect(x + 28, yy, o * 2, o);
                gr.drawImage(bo[b].cu.getSubimage(0, i * 16, 16, 16), x + 28, yy, o, o, null);
                gr.drawImage(moi.getSubimage(0, i * 16, 16, 16), x + 28 + o, yy, o, o, null);
                gr.setColor(kq[i].cach.equals("thay") ? new Color(140, 220, 140)
                    : kq[i].cach.equals("tron") ? new Color(140, 190, 255) : new Color(230, 200, 120));
                gr.drawString(i + (bo[b].kieu[i] == 2 ? "*" : ""), x + 2, yy + 14);
            }
            y += hang[b] * (o + 4);
        }
        for (int i = 0; i < banDo.size(); i++) {
            gr.setColor(Color.WHITE);
            gr.drawString("Ban do " + BAN_DO_MAU[i][0] + " (" + bo[BAN_DO_MAU[i][1]].ten + "): cu | moi", 4, y + 14);
            y += 20;
            gr.drawImage(banDo.get(i)[0], 0, y, null);
            gr.drawImage(banDo.get(i)[1], banDo.get(i)[0].getWidth() + 10, y, null);
            y += banDo.get(i)[0].getHeight();
        }
        gr.setColor(Color.WHITE);
        gr.drawString("Trang tri (map/tree, anh dip le): moi cap = cu | moi", 4, y + 14);
        y += 20;
        int x = 0, hangCao = 0;
        for (final BufferedImage[] c : trangTri) {
            final int rr = c[0].getWidth() * 2 + 8;
            if (x + rr > rongTT) {
                y += hangCao + 6;
                x = 0;
                hangCao = 0;
            }
            gr.setColor(new Color(70, 110, 70));
            gr.fillRect(x, y, rr, c[0].getHeight());
            gr.drawImage(c[0], x, y, null);
            gr.drawImage(c[1], x + c[0].getWidth() + 8, y, null);
            x += rr + 12;
            hangCao = Math.max(hangCao, c[0].getHeight());
        }
        gr.dispose();
        ImageIO.write(anh, "png", new File("docs/pokemon/xem_truoc_tile.png"));
    }

    /** Ve mot vung 22x18 tile cua ban do (lop nen + tileTop) phong 2 lan. */
    static BufferedImage veBanDo(final int id, final BufferedImage boAnh, final String thuMucCay) throws IOException {
        final File f = new File("cMap/" + id);
        if (!f.isFile()) {
            return null;
        }
        final byte[] d = Files.readAllBytes(f.toPath());
        int p = 0;
        final int w = d[p++] & 255, h = d[p++] & 255;
        final int[] nen = new int[w * h];
        for (int i = 0; i < w * h; i++) {
            nen[i] = d[p++] & 255;
        }
        final int[] tren = new int[w * h];
        Arrays.fill(tren, 255);
        while (p < d.length) {
            final int x = d[p++] & 255;
            if (x == 255) {
                break;
            }
            final int yy = d[p++] & 255, ww = d[p++] & 255, hh = d[p++] & 255;
            for (int k = yy; k < yy + hh; k++) {
                for (int l = x; l < x + ww; l++) {
                    final int v = d[p++] & 255;
                    if (v != 255 && k < h && l < w) {
                        tren[k * w + l] = v;
                    }
                }
            }
        }
        while (p < d.length) {
            final int x = d[p++] & 255;
            if (x == 255) {
                break;
            }
            final int yy = d[p++] & 255, v = d[p++] & 255;
            if (v != 255 && yy < h && x < w) {
                tren[yy * w + x] = v;
            }
        }
        final List<int[]> cay = docCay(d, p);
        final int soTile = boAnh.getHeight() / 16;
        final int vw = Math.min(w, 22), vh = Math.min(h, 18);
        final int x0 = Math.max(0, (w - vw) / 2), y0 = Math.max(0, (h - vh) / 2);
        final BufferedImage anh = new BufferedImage(vw * 32, vh * 32, BufferedImage.TYPE_INT_RGB);
        final Graphics2D gr = anh.createGraphics();
        for (int y = 0; y < vh; y++) {
            for (int x = 0; x < vw; x++) {
                final int i = (y + y0) * w + x + x0;
                if (nen[i] < soTile) {
                    gr.drawImage(boAnh.getSubimage(0, nen[i] * 16, 16, 16), x * 32, y * 32, 32, 32, null);
                }
                if (tren[i] < soTile) {
                    gr.drawImage(boAnh.getSubimage(0, tren[i] * 16, 16, 16), x * 32, y * 32, 32, 32, null);
                }
            }
        }
        // Cay/nha (Tree.paint: (x<<4)+dx, (y<<4)+dy), ve theo y tang dan nhu client sap actor.
        cay.sort((a, b2) -> Integer.compare(a[1], b2[1]));
        for (final int[] c : cay) {
            final File fc = new File(thuMucCay + c[2]);
            if (!fc.isFile()) {
                continue;
            }
            final byte[] dc = Files.readAllBytes(fc.toPath());
            final int dai = ((dc[0] & 255) << 8) | (dc[1] & 255);
            final BufferedImage ac = ImageIO.read(new java.io.ByteArrayInputStream(dc, 2, dai));
            final int dx = dc[2 + dai + 2], dy = dc[2 + dai + 3];
            final int px = ((c[0] - x0) << 4) + dx, py = ((c[1] - y0) << 4) + dy;
            gr.drawImage(ac, px * 2, py * 2, ac.getWidth() * 2, ac.getHeight() * 2, null);
        }
        gr.dispose();
        return anh;
    }

    // ------------------------------------------------------------------ tu kiem

    /** Doc lai file vua ghi: dung kich thuoc, dung mat na trong suot, goi dang nhap con vua. */
    static void kiemLai(final BoTile[] bo) throws IOException {
        for (final BoTile b : bo) {
            final BufferedImage moi = ImageIO.read(new File(THU_MUC_RA + b.ten + ".png"));
            if (moi.getWidth() != b.cu.getWidth() || moi.getHeight() != b.cu.getHeight()) {
                throw new IllegalStateException(b.ten + ": sai kich thuoc");
            }
            for (int y = 0; y < moi.getHeight(); y++) {
                for (int x = 0; x < 16; x++) {
                    if (((moi.getRGB(x, y) >>> 24) >= 128) != ((b.cu.getRGB(x, y) >>> 24) >= 128)) {
                        throw new IllegalStateException(b.ten + ": lech mat na trong suot tai " + x + "," + y);
                    }
                }
            }
            final long kb = new File(THU_MUC_RA + b.ten + ".png").length();
            // LoginHandler ghi do dai bang writeShort: t_hang/t_thanh phai < 32768 byte.
            if (!b.ten.equals("t") && kb > Short.MAX_VALUE) {
                throw new IllegalStateException(b.ten + ".png " + kb + " byte, vuot writeShort");
            }
            System.out.println("  kiem " + b.ten + ".png: " + kb + " byte, OK");
        }
    }

    /**
     * Thu muc Resources/&lt;con&gt; cua MOI client can ghi: bien HTBB_CLIENT_ASSETS (chi thu muc do), hoac
     * client PC chinh ../htbb-client/pc/Assets (canh HTBB/) + ban cu ClientUnity/Assets neu con
     * (bo bang HTBB_CLIENT_CU=0). Giong tools/pokemon/duong_client.py.
     */
    static java.util.List<File> thuMucClient(final String con) {
        final java.util.List<File> ds = new java.util.ArrayList<File>();
        final String rieng = System.getenv("HTBB_CLIENT_ASSETS");
        if (rieng != null && !rieng.isEmpty()) {
            ds.add(new File(rieng, "Resources/" + con));
            return ds;
        }
        final File pc = new File("../htbb-client/pc/Assets");
        if (pc.isDirectory()) {
            ds.add(new File(pc, "Resources/" + con));
        }
        final File cu = new File("ClientUnity/Assets");
        if (cu.isDirectory() && !"0".equals(System.getenv("HTBB_CLIENT_CU"))) {
            ds.add(new File(cu, "Resources/" + con));
        }
        if (ds.isEmpty()) {
            ds.add(new File(cu, "Resources/" + con));
        }
        return ds;
    }

    /** Ghi tep ten vao Resources/&lt;con&gt; cua moi client (tao thu muc neu thieu). */
    static void ghiClient(final String con, final String ten, final byte[] data) throws java.io.IOException {
        for (final File d : thuMucClient(con)) {
            d.mkdirs();
            java.nio.file.Files.write(new File(d, ten).toPath(), data);
        }
    }
}
