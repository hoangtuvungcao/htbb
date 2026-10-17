import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

/**
 * Dung BO TILE MOI HOAN TOAN cho ban do che do Pokemon (thay bo tile cu cua game goc - KHONG dung
 * pixel nao cua tile/cay/nha game goc). Nguon (CC0, xem docs/pokemon/NGUON_ASSET_MAP.md):
 *   - Ninja Adventure / Pixel-boy & AAA (CC0): co (4 bien the + co sang), dat, cat, nuoc, co cao, hoa,
 *     cay 2x2 (3 loai), bui, da, mat vach (TilesetRelief), nha (TilesetHouse), hang rao, bien go, cau go,
 *     san + do trong nha
 *   - Ve bang code (du an nay, CC0): TAT CA tile chuyen tiep autotile (bot nuoc + bo dat, vien co trum
 *     duong, mep mem co sang/toi), mat tren vach, nen + vach hang, bien hoi phuc/cua hang.
 *
 * Autotile: moi cap vat lieu (A tren nen B) co 15 tile theo 4 goc (bit nw ne sw se = goc do la A);
 * vung A = hop cac hinh tron ban kinh 11.5 px tam o cac goc A -> hai tile ke nhau luon khop mep,
 * goc loi/lom tron, vien toi 1 px (nuoc: bot trang; vach: mat da phia nam + bong do).
 *
 * Ra (cung ten tep/so luong toi da nhu bo cu de client khong phai doi):
 *   map/pokemon_tile/{t,t_hang,t_thanh}.png + {t_small,t_hang_s,t_thanh_s}.png   (server gui t_hang/t_thanh)
 *   ClientUnity/Assets/Resources/poke_tile/*.bytes                                 (Unity + goi J2ME res/pt)
 *   cMap_pokemon/{t,t_hang,t_thanh}.type, Resources/poke_map/kieu_*.bytes, tools/pokemon/map_j2me/*.type
 *   docs/pokemon/bang_tile_moi.csv (ten tung tile - VeMapMoi tra theo ten), docs/pokemon/xem_truoc_tile_moi.png
 * Gioi han: so tile &lt;= mang mau ban do nho cua client (t 232, t_hang 116, t_thanh 216); PNG
 * t_hang/t_thanh &lt;= 32767 byte (goi dang nhap ghi do dai bang short).
 *
 * Chay (o HTBB/): javac -d build/tools tools/pokemon/DungBoTileMoi.java &amp;&amp; java -cp build/tools DungBoTileMoi
 * Can tools/pokemon/pmd_raw_tile_moi/ (tai theo docs/pokemon/NGUON_ASSET_MAP.md).
 */
public final class DungBoTileMoi {

    static final String GOC = "tools/pokemon/pmd_raw_tile_moi/";
    static BufferedImage NI;

    static final int[] TOI_DA = {232, 116, 216};

    static final class Bo {
        final String ten;
        final List<BufferedImage> anh = new ArrayList<BufferedImage>();
        final List<Integer> kieu = new ArrayList<Integer>();
        final List<String> tenTile = new ArrayList<String>();
        final List<String> nguon = new ArrayList<String>();

        Bo(final String ten) {
            this.ten = ten;
        }

        int them(final String t, final BufferedImage a, final boolean chan, final String ng) {
            if (tenTile.contains(t)) {
                throw new IllegalStateException("trung ten tile " + t);
            }
            anh.add(a);
            kieu.add(chan ? 2 : 0);
            tenTile.add(t);
            nguon.add(ng);
            return anh.size() - 1;
        }
    }

    // ------------------------------------------------------------ anh nguon
    static BufferedImage o(final BufferedImage sheet, final int cot, final int i) {
        final BufferedImage r = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = r.createGraphics();
        g.drawImage(sheet.getSubimage((i % cot) * 16, (i / cot) * 16, 16, 16), 0, 0, null);
        g.dispose();
        return r;
    }


    static BufferedImage n(final int i) {
        return o(NI, 22, i);
    }

    /** fg chong len nen bg (nen kin). */
    static BufferedImage tren(final BufferedImage bg, final BufferedImage fg) {
        final BufferedImage r = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = r.createGraphics();
        g.drawImage(bg, 0, 0, null);
        g.drawImage(fg, 0, 0, null);
        g.dispose();
        return r;
    }

    static int rgb(final int r, final int g, final int b) {
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    static double bam(final int x, final int y, final int s) {
        long h = s * 0x9E3779B97F4A7C15L + x * 0xC2B2AE3D27D4EB4FL + y * 0x165667B19E3779F9L;
        h ^= h >>> 33;
        h *= 0xff51afd7ed558ccdL;
        h ^= h >>> 33;
        return (h >>> 11) * (1.0 / (1L << 53));
    }

    // ------------------------------------------------------------ van ve bang code
    static final int VIEN = rgb(62, 39, 49);

    static BufferedImage nuoc(final int bien) {
        final BufferedImage r = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                r.setRGB(x, y, rgb(72, 152, 214));
            }
        }
        // song nho lap lai trong o (khop khi lat)
        final int[][] song = {{2, 3}, {9, 6}, {4, 11}, {12, 13}};
        for (final int[] s : song) {
            final int sx = (s[0] + bien * 3) % 16;
            for (int dx = 0; dx < 4; dx++) {
                final int yy = s[1] + (dx == 1 || dx == 2 ? -1 : 0);
                r.setRGB((sx + dx) % 16, (yy + 16) % 16, rgb(146, 206, 240));
            }
        }
        return r;
    }

    static BufferedImage coCao(final BufferedImage co) {
        final BufferedImage r = tren(co, new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB));
        final int toi = rgb(46, 118, 60), giua = rgb(70, 152, 70), sang = rgb(150, 212, 98);
        final int[][] bui = {{0, 1}, {8, 1}, {4, 9}, {12, 9}};
        for (final int[] b : bui) {
            // hinh "W" 7 px: 3 la co
            final int[][] la = {{0, 6}, {1, 4}, {2, 5}, {3, 2}, {4, 5}, {5, 4}, {6, 6}};
            for (final int[] l : la) {
                final int x = (b[0] + l[0]) % 16;
                for (int y = b[1] + l[1]; y <= b[1] + 6; y++) {
                    r.setRGB(x, y % 16, y == b[1] + l[1] ? sang : (y >= b[1] + 5 ? toi : giua));
                }
            }
        }
        return r;
    }

    static BufferedImage da(final int nen, final int vet, final int sang, final int s) {
        final BufferedImage r = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                final double v = bam(x, y, s);
                r.setRGB(x, y, v < 0.12 ? vet : (v > 0.9 ? sang : nen));
            }
        }
        return r;
    }

    static BufferedImage den() {
        final BufferedImage r = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                r.setRGB(x, y, rgb(16, 12, 18));
            }
        }
        return r;
    }

    static BufferedImage cau(final boolean ngang, final BufferedImage nuoc) {
        final BufferedImage r = tren(nuoc, new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB));
        for (int a = 0; a < 16; a++) {
            for (int b = 1; b < 15; b++) {
                final int c = (a % 4 == 0) ? rgb(120, 72, 44) : rgb(176, 118, 70);
                final int lan = (b == 1 || b == 14) ? VIEN : c;
                if (ngang) {
                    r.setRGB(a, b, lan);
                } else {
                    r.setRGB(b, a, lan);
                }
            }
        }
        return r;
    }

    /** Bien hieu nho tren nen: mau nen bien + ky hieu (dau + = hoi phuc, chu M = cua hang). */
    static BufferedImage bien(final BufferedImage nen, final int mau, final boolean cong) {
        final BufferedImage r = tren(nen, new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB));
        for (int y = 2; y < 12; y++) {
            for (int x = 2; x < 14; x++) {
                r.setRGB(x, y, (x == 2 || x == 13 || y == 2 || y == 11) ? VIEN : mau);
            }
        }
        for (int y = 12; y < 15; y++) {
            r.setRGB(7, y, VIEN);
            r.setRGB(8, y, rgb(120, 72, 44));
        }
        final int trang = rgb(250, 250, 250);
        if (cong) {
            for (int i = 4; i <= 9; i++) {
                r.setRGB(7, i, trang);
                r.setRGB(8, i, trang);
                r.setRGB(i - 1, 6, trang);
                r.setRGB(i - 1, 7, trang);
            }
        } else {
            final int[][] m = {{4, 4}, {4, 5}, {4, 6}, {4, 7}, {4, 8}, {4, 9}, {5, 5}, {6, 6}, {7, 7}, {8, 6}, {9, 5},
                    {10, 4}, {10, 5}, {10, 6}, {10, 7}, {10, 8}, {10, 9}, {11, 4}, {11, 9}, {3, 4}, {3, 9}};
            for (final int[] p : m) {
                r.setRGB(p[0], p[1], trang);
            }
        }
        return r;
    }

    // ------------------------------------------------------------ autotile goc
    static final double R = 11.5;

    static boolean trongA(final double px, final double py, final int bit) {
        final double[][] goc = {{0, 0}, {16, 0}, {0, 16}, {16, 16}};
        for (int c = 0; c < 4; c++) {
            if ((bit & (8 >> c)) != 0 && Math.hypot(px - goc[c][0], py - goc[c][1]) <= R) {
                return true;
            }
        }
        return false;
    }

    /** Kieu mep: VIEN (vien 1px phia A), VIEN_CO (vien co ram phia B), NUOC (bot trang + bo dat), MEM (hoa troi lan). */
    static final int VIEN_A = 0, VIEN_CO = 1, NUOC = 2, MEM = 3;

    static boolean trongA2(final double px, final double py, final int bit, final long nhieuS) {
        // mep hoi gon (nhieu nho theo toa do TUYET DOI trong o -> 2 o canh nhau van khop vi chi dung goc chung)
        return trongA(px, py, bit);
    }

    static int khoangMep(final int x, final int y, final int bit, final boolean phiaA, final int toiDa) {
        for (int r = 1; r <= toiDa; r++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dx = -r; dx <= r; dx++) {
                    if (Math.abs(dx) + Math.abs(dy) > r) {
                        continue;
                    }
                    if (trongA(x + 0.5 + dx, y + 0.5 + dy, bit) != phiaA) {
                        return r;
                    }
                }
            }
        }
        return toiDa + 1;
    }

    static BufferedImage ghep(final BufferedImage a, final BufferedImage b, final int bit, final int mau, final int kieuMep) {
        final BufferedImage r = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                final boolean in = trongA(x + 0.5, y + 0.5, bit);
                int c = in ? a.getRGB(x, y) : b.getRGB(x, y);
                if (kieuMep == MEM) {
                    // vung chuyen 2 px: chon diem anh A/B theo mau ca ro (ditter co dinh) -> mep mem, khop nhau
                    final int dA = khoangMep(x, y, bit, in, 2);
                    if (dA <= 2 && ((x + y * 2) % 3 == 0) == in) {
                        c = in ? b.getRGB(x, y) : a.getRGB(x, y);
                    }
                } else if (kieuMep == VIEN_A) {
                    if (in && khoangMep(x, y, bit, true, 1) == 1) {
                        c = mau;
                    }
                } else if (kieuMep == VIEN_CO) {
                    // co trum mep duong dat: vien co dam 1-2 px phia B, rang cua theo vi tri
                    if (!in) {
                        final int d = khoangMep(x, y, bit, false, 2);
                        if (d == 1 || (d == 2 && ((x * 7 + y * 3) % 4 == 0))) {
                            c = mau;
                        }
                    } else if (khoangMep(x, y, bit, true, 1) == 1) {
                        c = toiDi(c, 0.82);
                    }
                } else if (kieuMep == NUOC) {
                    if (in) {
                        final int d = khoangMep(x, y, bit, true, 2);
                        if (d == 1) {
                            c = rgb(244, 250, 252);
                        } else if (d == 2) {
                            c = rgb(150, 220, 236);
                        }
                    } else {
                        final int d = khoangMep(x, y, bit, false, 3);
                        if (d == 1) {
                            c = rgb(120, 66, 52);
                        } else if (d == 2) {
                            c = mau;
                        } else if (d == 3 && ((x + y) % 2 == 0)) {
                            c = toiDi(c, 0.85);
                        }
                    }
                }
                r.setRGB(x, y, c);
            }
        }
        return r;
    }

    static int toiDi(final int c, final double k) {
        return rgb((int) (((c >> 16) & 255) * k), (int) (((c >> 8) & 255) * k), (int) ((c & 255) * k));
    }

    /** 14 tile chuyen tiep A tren B (bit 1..14): ten "A/B_<nw ne sw se>". Kieu chan: A chan va &gt;= 2 goc A. */
    static void cap(final Bo bo, final String ta, final BufferedImage a, final boolean aChan, final String tb, final BufferedImage b,
                    final int mau, final int kieuMep, final String nguon) {
        for (int bit = 1; bit < 15; bit++) {
            final int soA = Integer.bitCount(bit);
            final String ma = "" + ((bit >> 3) & 1) + ((bit >> 2) & 1) + ((bit >> 1) & 1) + (bit & 1);
            bo.them(ta + "/" + tb + "_" + ma, ghep(a, b, bit, mau, kieuMep), aChan && soA >= 2, nguon);
        }
    }

    // ------------------------------------------------------------ nguon Ninja Adventure
    static final Map<String, BufferedImage> NJ = new LinkedHashMap<String, BufferedImage>();

    static BufferedImage nj(final String bang, final int x, final int y) {
        final BufferedImage s = NJ.get(bang);
        final BufferedImage r = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = r.createGraphics();
        g.drawImage(s.getSubimage(x * 16, y * 16, 16, 16), 0, 0, null);
        g.dispose();
        return r;
    }

    static String nn(final String bang, final int x, final int y) {
        return "Ninja Adventure " + bang + " (" + x + "," + y + ")";
    }

    /** Vach (mat da 3 hang cua TilesetRelief) + mat tren (co/da) vien toi: ten "<p>_mat_{l,m,r}{0,2}" va cap "<p>T/<nen>". */
    static void vach(final Bo b, final String p, final int cot, final int hang, final BufferedImage mat, final BufferedImage nen,
                     final String tenNen, final String nguonMat) {
        final String[] lmr = {"l", "m", "r"};
        for (int c = 0; c < 3; c++) {
            for (final int r : new int[]{0, 2}) {
                b.them(p + "_mat_" + lmr[c] + r, tren(nen, nj("TilesetRelief", cot + c, hang + r)), true,
                        nn("TilesetRelief", cot + c, hang + r));
            }
        }
        b.them(p + "T", mat, true, nguonMat);
        cap(b, p + "T", mat, true, tenNen, nen, VIEN, VIEN_A, VE + " + " + nguonMat);
    }

    /** Cay 2x2 (4 tile) tren nen. */
    static void cay2(final Bo b, final String ten, final int x, final int y, final BufferedImage nen) {
        b.them(ten + "_0", tren(nen, nj("TilesetNature", x, y)), true, nn("TilesetNature", x, y));
        b.them(ten + "_1", tren(nen, nj("TilesetNature", x + 1, y)), true, nn("TilesetNature", x + 1, y));
        b.them(ten + "_2", tren(nen, nj("TilesetNature", x, y + 1)), true, nn("TilesetNature", x, y + 1));
        b.them(ten + "_3", tren(nen, nj("TilesetNature", x + 1, y + 1)), true, nn("TilesetNature", x + 1, y + 1));
    }

    /** Nha Ninja (TilesetHouse) rong w x 3 hang, o cua (cx,2) di duoc. Ten "<ten>_<cot>_<hang>". */
    static final int[][] NHA_NJ = {
        // x0, rong, cot cua (tuong doi)
        {0, 4, 1}, {4, 4, 1}, {12, 4, 1}, {16, 3, 1}, {19, 4, 1},
    };
    static final String[] TEN_NHA = {"nha_cam", "nha_be", "trung_tam", "cua_hang", "nha_xanh"};

    static void nhaNinja(final Bo b, final BufferedImage nen) {
        for (int n = 0; n < NHA_NJ.length; n++) {
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < NHA_NJ[n][1]; c++) {
                    final int x = NHA_NJ[n][0] + c;
                    b.them(TEN_NHA[n] + "_" + c + "_" + r, tren(nen, nj("TilesetHouse", x, r)), !(r == 2 && c == NHA_NJ[n][2]),
                            nn("TilesetHouse", x, r));
                }
            }
        }
    }

    static void hangRao(final Bo b, final BufferedImage nen) {
        b.them("rao_trai", tren(nen, nj("TilesetHouse", 10, 5)), true, nn("TilesetHouse", 10, 5));
        b.them("rao_giua", tren(nen, nj("TilesetHouse", 12, 5)), true, nn("TilesetHouse", 12, 5));
        b.them("rao_phai", tren(nen, nj("TilesetHouse", 14, 5)), true, nn("TilesetHouse", 14, 5));
        b.them("rao_doc", tren(nen, nj("TilesetHouse", 11, 4)), true, nn("TilesetHouse", 11, 4));
    }

    // ------------------------------------------------------------ bo tile
    static final String KTD = "Kenney Tiny Dungeon #", NIN = "Ninja Adventure interior_floor #", VE = "ve bang code";

    static Bo boNgoai(final String ten) {
        final Bo b = new Bo(ten);
        final BufferedImage G = nj("TilesetFloor", 11, 12), L = nj("TilesetFloor", 0, 12), D = nj("TilesetFloor", 1, 8);
        final BufferedImage S = nj("TilesetField", 3, 0), W = nj("TilesetWater", 1, 7), N = nj("TilesetFloor", 1, 15);
        final BufferedImage K = soi(), WM = nuocSau(W, 1), WD = nuocSau(W, 2);
        b.them("G", G, false, nn("TilesetFloor", 11, 12));
        for (int i = 1; i <= 3; i++) {
            b.them("G" + i, nj("TilesetFloor", 11 + i, 12), false, nn("TilesetFloor", 11 + i, 12));
        }
        b.them("D", D, false, nn("TilesetFloor", 1, 8));
        b.them("S", S, false, nn("TilesetField", 3, 0));
        b.them("N", N, false, nn("TilesetFloor", 1, 15));
        b.them("K", K, false, VE);
        b.them("W", W, true, nn("TilesetWater", 1, 7));
        b.them("W1", gonSong(W), true, VE + " + " + nn("TilesetWater", 1, 7));
        b.them("WM", WM, true, VE + " + " + nn("TilesetWater", 1, 7));
        b.them("WD", WD, true, VE + " + " + nn("TilesetWater", 1, 7));
        b.them("sen", tren(W, nj("TilesetWater", 11, 3)), true, nn("TilesetWater", 11, 3));
        b.them("X", den(), true, VE);
        cap(b, "D", D, false, "G", G, rgb(64, 108, 48), VIEN_CO, VE + " + " + nn("TilesetFloor", 1, 8));
        cap(b, "S", S, false, "G", G, rgb(64, 108, 48), VIEN_CO, VE + " + " + nn("TilesetField", 3, 0));
        cap(b, "N", N, false, "G", G, rgb(150, 170, 190), VIEN_CO, VE + " + " + nn("TilesetFloor", 1, 15));
        cap(b, "W", W, true, "G", G, rgb(164, 100, 66), NUOC, VE + " + " + nn("TilesetWater", 1, 7));
        cap(b, "W", W, true, "S", S, rgb(214, 150, 96), NUOC, VE + " + " + nn("TilesetWater", 1, 7));
        // san lat da (quang truong chu nhat): chi 8 kieu mep/goc ngoai
        for (final int bit : new int[]{1, 2, 4, 8, 3, 12, 5, 10}) {
            final String ma = "" + ((bit >> 3) & 1) + ((bit >> 2) & 1) + ((bit >> 1) & 1) + (bit & 1);
            b.them("K/G_" + ma, ghep(K, G, bit, rgb(70, 66, 70), VIEN_A), false, VE);
        }
        vach(b, "R", 4, 0, L, G, "G", nn("TilesetFloor", 0, 12));
        b.them("goc_l", go(G, 0), true, VE);
        b.them("goc_m", go(G, 1), true, VE);
        b.them("goc_r", go(G, 2), true, VE);
        b.them("T", tren(G, nj("TilesetNature", 5, 10)), false, nn("TilesetNature", 5, 10));
        b.them("T1", tren(G, nj("TilesetNature", 7, 10)), false, nn("TilesetNature", 7, 10));
        b.them("hoa_vang", tren(G, nj("TilesetFloorDetail", 2, 0)), false, nn("TilesetFloorDetail", 2, 0));
        b.them("hoa_cam", tren(G, nj("TilesetFloorDetail", 4, 0)), false, nn("TilesetFloorDetail", 4, 0));
        b.them("hoa_trang", tren(G, nj("TilesetFloorDetail", 3, 0)), false, nn("TilesetFloorDetail", 3, 0));
        cay2(b, "cayA", 0, 0, G);
        cay2(b, "cayB", 2, 0, G);
        cay2(b, "cayC", 6, 0, G);
        cay2(b, "cayT", 8, 0, N);
        cay2d(b, "dua", 10, 4, S);
        cay2h(b, "tuong", 0, 15, K);
        b.them("bui", tren(G, nj("TilesetNature", 0, 10)), true, nn("TilesetNature", 0, 10));
        b.them("cay_mong", tren(G, nj("TilesetNature", 4, 14)), true, nn("TilesetNature", 4, 14));
        b.them("goc_cay", tren(G, nj("TilesetNature", 1, 13)), true, nn("TilesetNature", 1, 13));
        b.them("da", tren(G, nj("TilesetNature", 4, 12)), true, nn("TilesetNature", 4, 12));
        b.them("soi", tren(G, nj("TilesetNature", 16, 17)), false, nn("TilesetNature", 16, 17));
        b.them("da_tuyet", tren(N, nj("TilesetNature", 5, 12)), true, nn("TilesetNature", 5, 12));
        b.them("da_cat", tren(S, nj("TilesetNature", 4, 12)), true, nn("TilesetNature", 4, 12));
        b.them("thung", tren(G, nj("TilesetHouse", 15, 14)), true, nn("TilesetHouse", 15, 14));
        b.them("den", den(G), true, VE);
        b.them("hom_thu", homThu(G), true, VE);
        b.them("bien_go", tren(G, nj("TilesetElement", 3, 2)), true, nn("TilesetElement", 3, 2));
        b.them("bien_hoi_phuc", bien(G, rgb(214, 64, 64), true), true, VE);
        b.them("bien_cua_hang", bien(G, rgb(64, 110, 214), false), true, VE);
        b.them("cau_ngang", tren(W, nj("TilesetWater", 1, 12)), false, nn("TilesetWater", 1, 12));
        b.them("cau_doc", tren(W, nj("TilesetWater", 4, 12)), false, nn("TilesetWater", 4, 12));
        nhaNinja(b, G);
        hangRao(b, G);
        trongNha(b);
        return b;
    }

    /** Cay 2x2 tu TilesetDesert (dua). */
    static void cay2d(final Bo b, final String ten, final int x, final int y, final BufferedImage nen) {
        for (int i = 0; i < 4; i++) {
            final int xx = x + i % 2, yy = y + i / 2;
            b.them(ten + "_" + i, tren(nen, nj("TilesetDesert", xx, yy)), true, nn("TilesetDesert", xx, yy));
        }
    }

    /** Tuong da 2x2 tu TilesetHouse (giua quang truong). */
    static void cay2h(final Bo b, final String ten, final int x, final int y, final BufferedImage nen) {
        for (int i = 0; i < 4; i++) {
            final int xx = x + i % 2, yy = y + i / 2;
            b.them(ten + "_" + i, tren(nen, nj("TilesetHouse", xx, yy)), true, nn("TilesetHouse", xx, yy));
        }
    }

    /** San dau truong hoa van thoi (o vuong long nhau). */
    static BufferedImage hoaVan(final BufferedImage p) {
        final BufferedImage r = tren(p, new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB));
        final int vien = rgb(196, 140, 110), giua = rgb(232, 196, 160);
        for (int i = 0; i < 16; i++) {
            r.setRGB(i, 0, vien);
            r.setRGB(0, i, vien);
        }
        for (int y = 5; y < 11; y++) {
            for (int x = 5; x < 11; x++) {
                r.setRGB(x, y, (Math.abs(x - 7.5) + Math.abs(y - 7.5) <= 3) ? giua : r.getRGB(x, y));
            }
        }
        return r;
    }

    /** Vien do san dau truong. */
    static BufferedImage hoaVan2(final BufferedImage p) {
        final BufferedImage r = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                final boolean soc = ((x / 4 + y / 4) % 2) == 0;
                r.setRGB(x, y, soc ? rgb(186, 84, 70) : rgb(216, 120, 92));
            }
        }
        return r;
    }

    /** Da lat (soi cuoi xam, vua da toi) - ve bang code. */
    static BufferedImage soi() {
        final BufferedImage r = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        final int vua = rgb(118, 112, 116), da1 = rgb(196, 190, 186), da2 = rgb(176, 170, 168), sang = rgb(222, 218, 212);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                // vien gach lech hang: hang 0-7 khe doc o x=0,8; hang 8-15 khe o x=4,12
                final boolean khe = y % 8 == 7 || (y < 8 ? x % 8 == 7 : (x + 4) % 8 == 7);
                final boolean tren = y % 8 == 0 && !khe;
                r.setRGB(x, y, khe ? vua : (tren ? sang : ((x * 5 + y * 3) % 7 == 0 ? da2 : da1)));
            }
        }
        return r;
    }

    /** Nuoc sau dan: muc 1 = dittér 50%, muc 2 = toi han. */
    static BufferedImage nuocSau(final BufferedImage w, final int muc) {
        final BufferedImage r = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                final int c = w.getRGB(x, y);
                final boolean toi = muc == 2 || ((x + y) % 2 == 0);
                r.setRGB(x, y, toi ? toiDi(c, 0.78) : c);
            }
        }
        return r;
    }

    /** Song lan nho tren nuoc. */
    static BufferedImage gonSong(final BufferedImage w) {
        final BufferedImage r = tren(w, new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB));
        final int sang = rgb(210, 240, 250);
        final int[][] s = {{3, 5}, {4, 4}, {5, 4}, {6, 5}, {10, 11}, {11, 10}, {12, 10}, {13, 11}};
        for (final int[] p : s) {
            r.setRGB(p[0], p[1], sang);
        }
        return r;
    }

    /** Go dat (ledge) 1 chieu kieu Pokemon: co tren, mep dat nho xuong phia nam. 0 trai, 1 giua, 2 phai. */
    static BufferedImage go(final BufferedImage co, final int kieu) {
        final BufferedImage r = tren(co, new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB));
        final int mat = rgb(150, 110, 70), toi = rgb(104, 72, 50), vien = rgb(62, 39, 49), sang = rgb(140, 190, 80);
        for (int x = 0; x < 16; x++) {
            final boolean dau = (kieu == 0 && x < 2) || (kieu == 2 && x > 13);
            if (dau) {
                continue;
            }
            r.setRGB(x, 9, sang);
            r.setRGB(x, 10, vien);
            r.setRGB(x, 11, mat);
            r.setRGB(x, 12, mat);
            r.setRGB(x, 13, toi);
            r.setRGB(x, 14, toiDi(co.getRGB(x, 14), 0.75));
        }
        return r;
    }

    /** Cot den (cot toi + den vang). */
    static BufferedImage den(final BufferedImage nen) {
        final BufferedImage r = tren(nen, new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB));
        for (int y = 5; y < 15; y++) {
            r.setRGB(7, y, VIEN);
            r.setRGB(8, y, rgb(70, 60, 64));
        }
        for (int y = 0; y < 5; y++) {
            for (int x = 5; x < 11; x++) {
                final boolean b = x == 5 || x == 10 || y == 0 || y == 4;
                r.setRGB(x, y, b ? VIEN : rgb(255, 214, 96));
            }
        }
        r.setRGB(6, 15, VIEN);
        r.setRGB(9, 15, VIEN);
        return r;
    }

    /** Hom thu do. */
    static BufferedImage homThu(final BufferedImage nen) {
        final BufferedImage r = tren(nen, new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB));
        for (int y = 3; y < 9; y++) {
            for (int x = 4; x < 12; x++) {
                final boolean b = x == 4 || x == 11 || y == 3 || y == 8;
                r.setRGB(x, y, b ? VIEN : rgb(214, 70, 64));
            }
        }
        r.setRGB(6, 5, rgb(250, 250, 250));
        r.setRGB(7, 5, rgb(250, 250, 250));
        for (int y = 9; y < 15; y++) {
            r.setRGB(7, y, VIEN);
            r.setRGB(8, y, rgb(120, 80, 54));
        }
        return r;
    }

    /** Do trong nha (chung cho t va t_thanh): san Ninja, tuong van go Ninja, do dac Ninja TilesetElement. */
    static void trongNha(final Bo b) {
        final BufferedImage sanGo = n(264), sanSach = nj("TilesetField", 3, 9), sanTiem = n(33);
        b.them("san_go", sanGo, false, NIN + 264);
        b.them("san_sach", sanSach, false, nn("TilesetField", 3, 9));
        b.them("san_tiem", sanTiem, false, NIN + 33);
        b.them("tuong_go", nj("TilesetHouse", 23, 4), true, nn("TilesetHouse", 23, 4));
        b.them("tuong_toi", den(), true, VE);
        final int[][] doDac = {{8, 7}, {3, 7}, {6, 7}, {0, 1}, {13, 1}, {9, 7}};
        for (final int[] d : doDac) {
            b.them("e" + d[0] + "_" + d[1] + "_go", tren(sanGo, nj("TilesetElement", d[0], d[1])), true, nn("TilesetElement", d[0], d[1]));
        }
    }

    static Bo boHang() {
        final Bo b = new Bo("t_hang");
        final BufferedImage C = da(rgb(150, 116, 92), rgb(128, 98, 78), rgb(170, 136, 108), 5), S = nj("TilesetField", 3, 0),
                W = nj("TilesetWater", 1, 7);
        final BufferedImage V = da(rgb(92, 64, 56), rgb(70, 48, 42), rgb(116, 84, 70), 11);
        b.them("C", C, false, VE);
        b.them("C1", tren(C, nj("TilesetFloorDetail", 1, 0)), false, nn("TilesetFloorDetail", 1, 0));
        b.them("S", S, false, nn("TilesetField", 3, 0));
        b.them("W", W, true, nn("TilesetWater", 1, 7));
        b.them("X", den(), true, VE);
        cap(b, "W", W, true, "C", C, rgb(96, 70, 58), NUOC, VE);
        cap(b, "S", S, false, "C", C, rgb(70, 54, 46), VIEN_CO, VE);
        vach(b, "V", 4, 5, V, C, "C", VE);
        b.them("da", tren(C, nj("TilesetNature", 4, 12)), true, nn("TilesetNature", 4, 12));
        b.them("da2", tren(C, nj("TilesetNature", 5, 12)), true, nn("TilesetNature", 5, 12));
        b.them("C2", nuocSau(C, 1), false, VE);
        b.them("C3", nuocSau(C, 2), false, VE);
        b.them("pha_le", tren(C, nj("TilesetNature", 0, 14)), true, nn("TilesetNature", 0, 14));
        b.them("pha_le2", tren(C, nj("TilesetNature", 2, 14)), true, nn("TilesetNature", 2, 14));
        b.them("soi", tren(C, nj("TilesetNature", 16, 17)), false, nn("TilesetNature", 16, 17));
        return b;
    }

    static Bo boThanh() {
        final Bo b = new Bo("t_thanh");
        final BufferedImage P = nj("TilesetField", 3, 9), G = nj("TilesetFloor", 11, 12), W = nj("TilesetWater", 1, 7), A = nj("TilesetField", 3, 0);
        b.them("P", P, false, nn("TilesetField", 3, 9));
        b.them("G", G, false, nn("TilesetFloor", 11, 12));
        b.them("G1", nj("TilesetFloor", 12, 12), false, nn("TilesetFloor", 12, 12));
        b.them("A", A, false, nn("TilesetField", 3, 0));
        b.them("W", W, true, nn("TilesetWater", 1, 7));
        b.them("X", den(), true, VE);
        cap(b, "G", G, false, "P", P, rgb(64, 108, 48), VIEN_A, VE);
        cap(b, "W", W, true, "P", P, rgb(196, 150, 110), NUOC, VE);
        cap(b, "A", A, false, "P", P, rgb(200, 120, 80), MEM, VE);
        cap(b, "W", W, true, "G", G, rgb(164, 100, 66), NUOC, VE);
        cay2(b, "cayA", 0, 0, G);
        cay2(b, "cayB", 2, 0, G);
        b.them("bui", tren(G, nj("TilesetNature", 0, 10)), true, nn("TilesetNature", 0, 10));
        b.them("hoa_vang", tren(G, nj("TilesetFloorDetail", 2, 0)), false, nn("TilesetFloorDetail", 2, 0));
        b.them("bien_go", tren(P, nj("TilesetElement", 3, 2)), true, nn("TilesetElement", 3, 2));
        b.them("P2", hoaVan(P), false, VE + " + " + nn("TilesetField", 3, 9));
        b.them("P3", hoaVan2(P), false, VE + " + " + nn("TilesetField", 3, 9));
        for (int i = 0; i < 4; i++) {
            b.them("co_" + i, tren(P, nj("TilesetHouse", 22 + i, 21)), true, nn("TilesetHouse", 22 + i, 21));
        }
        cay2h(b, "tuong", 0, 15, P);
        b.them("den", den(P), true, VE);
        b.them("bien_hoi_phuc", bien(P, rgb(214, 64, 64), true), true, VE);
        b.them("cau_ngang", tren(W, nj("TilesetWater", 1, 12)), false, nn("TilesetWater", 1, 12));
        b.them("cau_doc", tren(W, nj("TilesetWater", 4, 12)), false, nn("TilesetWater", 4, 12));
        nhaNinja(b, P);
        hangRao(b, P);
        trongNha(b);
        return b;
    }

    // ------------------------------------------------------------ ghi
    public static void main(final String[] a) throws Exception {
        NI = ImageIO.read(new File(GOC + "ninja_adventure_godot/NinjaAdventure-main/content/map/tileset_interior_floor.png"));
        for (final String t : new String[]{"TilesetFloor", "TilesetNature", "TilesetWater", "TilesetHouse", "TilesetRelief", "TilesetElement",
                "TilesetFloorDetail", "TilesetField", "TilesetDesert"}) {
            NJ.put(t, ImageIO.read(new File(GOC + "ninja_full/" + t + ".png")));
        }
        final Bo[] bo = {boNgoai("t"), boHang(), boThanh()};
        final String[] nho = {"t_small", "t_hang_s", "t_thanh_s"};
        new File("map/pokemon_tile").mkdirs();
        new File("ClientUnity/Assets/Resources/poke_tile").mkdirs();
        new File("ClientUnity/Assets/Resources/poke_map").mkdirs();
        new File("cMap_pokemon").mkdirs();
        new File("tools/pokemon/map_j2me").mkdirs();
        final StringBuilder csv = new StringBuilder("bo_tile,chi_so,ten,kieu,nguon\n");
        final Map<String, BufferedImage> xem = new LinkedHashMap<String, BufferedImage>();
        for (int i = 0; i < bo.length; i++) {
            final Bo b = bo[i];
            if (b.anh.size() > TOI_DA[i] || b.anh.size() > 254) {
                throw new IllegalStateException(b.ten + ": " + b.anh.size() + " tile > gioi han " + TOI_DA[i]);
            }
            final BufferedImage dai = new BufferedImage(16, 16 * b.anh.size(), BufferedImage.TYPE_INT_ARGB);
            final BufferedImage dNho = new BufferedImage(1, b.anh.size(), BufferedImage.TYPE_INT_ARGB);
            final byte[] kieu = new byte[b.anh.size()];
            for (int t = 0; t < b.anh.size(); t++) {
                final BufferedImage x = b.anh.get(t);
                long rr = 0, gg = 0, bb = 0;
                for (int y = 0; y < 16; y++) {
                    for (int xx = 0; xx < 16; xx++) {
                        final int c = x.getRGB(xx, y) | 0xFF000000;
                        dai.setRGB(xx, t * 16 + y, c);
                        rr += (c >> 16) & 255;
                        gg += (c >> 8) & 255;
                        bb += c & 255;
                    }
                }
                dNho.setRGB(0, t, rgb((int) (rr / 256), (int) (gg / 256), (int) (bb / 256)));
                kieu[t] = (byte) (int) b.kieu.get(t);
                csv.append(b.ten).append(',').append(t).append(',').append(b.tenTile.get(t)).append(',').append(b.kieu.get(t))
                        .append(',').append(b.nguon.get(t)).append('\n');
            }
            final byte[] png = pngGon(dai);
            final byte[] pngNho = pngGon(dNho);
            if (!b.ten.equals("t") && png.length > Short.MAX_VALUE) {
                throw new IllegalStateException(b.ten + ".png " + png.length + " byte > 32767 (goi dang nhap)");
            }
            Files.write(new File("map/pokemon_tile/" + b.ten + ".png").toPath(), png);
            Files.write(new File("map/pokemon_tile/" + nho[i] + ".png").toPath(), pngNho);
            Files.write(new File("ClientUnity/Assets/Resources/poke_tile/" + b.ten + ".bytes").toPath(), png);
            Files.write(new File("ClientUnity/Assets/Resources/poke_tile/" + nho[i] + ".bytes").toPath(), pngNho);
            Files.write(new File("cMap_pokemon/" + b.ten + ".type").toPath(), kieu);
            Files.write(new File("ClientUnity/Assets/Resources/poke_map/kieu_" + b.ten + ".bytes").toPath(), kieu);
            Files.write(new File("tools/pokemon/map_j2me/" + b.ten + ".type").toPath(), kieu);
            xem.put(b.ten, dai);
            System.out.println(b.ten + ": " + b.anh.size() + " tile, png " + png.length + " byte");
        }
        Files.write(new File("docs/pokemon/bang_tile_moi.csv").toPath(), csv.toString().getBytes(StandardCharsets.UTF_8));
        veXemTruoc(xem, bo);
    }

    /** PNG bang mau (&lt;= 256 mau) neu duoc - nho hon nhieu, client J2ME/Unity deu doc. */
    static byte[] pngGon(final BufferedImage a) throws Exception {
        final Map<Integer, Integer> mau = new LinkedHashMap<Integer, Integer>();
        for (int y = 0; y < a.getHeight(); y++) {
            for (int x = 0; x < a.getWidth(); x++) {
                final int c = a.getRGB(x, y);
                if (!mau.containsKey(c)) {
                    mau.put(c, mau.size());
                }
            }
        }
        BufferedImage ra = a;
        if (mau.size() <= 256) {
            final byte[] r = new byte[mau.size()], g = new byte[mau.size()], b = new byte[mau.size()];
            for (final Map.Entry<Integer, Integer> e : mau.entrySet()) {
                r[e.getValue()] = (byte) (e.getKey() >> 16);
                g[e.getValue()] = (byte) (e.getKey() >> 8);
                b[e.getValue()] = (byte) (int) e.getKey();
            }
            final int bit = mau.size() <= 2 ? 1 : mau.size() <= 4 ? 2 : mau.size() <= 16 ? 4 : 8;
            final IndexColorModel cm = new IndexColorModel(bit, mau.size(), r, g, b);
            ra = new BufferedImage(a.getWidth(), a.getHeight(), bit == 8 ? BufferedImage.TYPE_BYTE_INDEXED : BufferedImage.TYPE_BYTE_BINARY, cm);
            for (int y = 0; y < a.getHeight(); y++) {
                for (int x = 0; x < a.getWidth(); x++) {
                    ra.setRGB(x, y, a.getRGB(x, y));
                }
            }
        } else {
            final BufferedImage rgb = new BufferedImage(a.getWidth(), a.getHeight(), BufferedImage.TYPE_INT_RGB);
            rgb.getGraphics().drawImage(a, 0, 0, null);
            ra = rgb;
        }
        final java.io.ByteArrayOutputStream o = new java.io.ByteArrayOutputStream();
        ImageIO.write(ra, "png", o);
        return o.toByteArray();
    }

    static void veXemTruoc(final Map<String, BufferedImage> xem, final Bo[] bo) throws Exception {
        final int cot = 16, s = 2, o = 16 * s + 2;
        int hang = 0;
        for (final BufferedImage d : xem.values()) {
            hang += (d.getHeight() / 16 + cot - 1) / cot + 1;
        }
        final BufferedImage a = new BufferedImage(cot * o, hang * o, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = a.createGraphics();
        int y0 = 0;
        for (final BufferedImage d : xem.values()) {
            final int n = d.getHeight() / 16;
            for (int t = 0; t < n; t++) {
                g.drawImage(d.getSubimage(0, t * 16, 16, 16), (t % cot) * o, y0 + (t / cot) * o, 16 * s, 16 * s, null);
            }
            y0 += ((n + cot - 1) / cot + 1) * o;
        }
        g.dispose();
        ImageIO.write(a, "png", new File("docs/pokemon/xem_truoc_tile_moi.png"));
    }
}
