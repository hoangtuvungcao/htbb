package game.pokemon;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Huy hiệu tròn cho chân dung Pokémon (HUD, khung mục tiêu NPC/quái, chọn/tạo nhân vật, danh sách
 * top, bản đồ lớn...): ảnh chân dung PMD có nền trời/cỏ vẽ sẵn (10-14 màu, không tách được) trông
 * rời rạc trên giao diện, nên vẽ lại bằng sprite ĐỨNG YÊN (đã trong suốt, khung đầu - nửa thân trên)
 * đặt trên đĩa màu hệ chính, viền 1 px đậm hơn + vệt sáng. Ghép một lần thành ảnh canh x canh có
 * alpha (ngoài đĩa trong suốt) và giữ trong bộ đệm LRU của PokeBody.
 */
public final class HuyHieuPoke {
    /** dex -> hệ chính (KieuO.MAU_HE) cho các loài có trong jar; thiếu = Thường. */
    private static final short[] DEX = {
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 13, 18, 19, 23, 24, 25, 26, 27, 29, 35, 36, 37, 41, 42, 43, 44, 45,
        46, 47, 48, 52, 53, 58, 59, 68, 69, 72, 76, 77, 78, 83, 85, 92, 93, 94, 99, 104, 105, 112, 113, 115,
        120, 121, 123, 127, 128, 129, 130, 131, 133, 134, 135, 136, 143, 144, 145, 146, 147, 148, 149, 159,
        160, 163, 164, 165, 168, 169, 172, 173, 178, 179, 185, 190, 194, 195, 196, 197, 214, 217, 219, 221,
        225, 227, 234, 244, 245, 250, 255, 256, 257, 262, 269, 270, 276, 282, 286, 302, 319, 324, 330, 336,
        337, 340, 342, 352, 353, 355, 356, 357, 370, 373, 382, 383, 384, 385, 393, 399, 401, 402, 404, 405,
        417, 423, 428, 436, 437, 448, 451, 453, 454, 460, 470, 471, 473, 477, 491, 496, 497, 498, 510, 536,
        537, 543, 544, 545, 552, 559, 560, 562, 585, 596, 607, 615, 627, 628, 637, 667, 681, 700, 709, 711,
        739, 750, 781, 831, 896, 897, 901
    };
    private static final byte[] HE = {
        4, 4, 4, 1, 1, 1, 2, 2, 2, 11, 11, 11, 0, 0, 7, 7, 3, 3, 8, 7, 17, 17, 1, 7, 7, 4, 4, 4,
        11, 11, 11, 0, 0, 1, 1, 6, 4, 2, 12, 1, 1, 0, 0, 13, 13, 13, 2, 8, 8, 8, 0, 0,
        2, 2, 11, 11, 0, 2, 2, 2, 0, 2, 3, 1, 0, 5, 3, 1, 14, 14, 14, 2,
        2, 0, 0, 11, 11, 7, 3, 17, 10, 3, 12, 0, 2, 2, 10, 15, 11, 0, 1, 5,
        5, 16, 0, 1, 2, 1, 1, 1, 1, 15, 11, 2, 0, 10, 4, 15, 2, 1, 8, 7,
        12, 2, 2, 0, 13, 13, 13, 4, 2, 14, 2, 8, 14, 16, 2, 0, 11, 11, 3, 3,
        3, 2, 0, 16, 16, 6, 7, 7, 7, 4, 4, 5, 5, 13, 15, 4, 4, 1, 15, 2,
        2, 11, 11, 11, 8, 15, 15, 13, 0, 11, 13, 5, 0, 0, 11, 1, 16, 17, 13, 13,
        6, 8, 13, 0, 5, 13, 8
    };

    private HuyHieuPoke() {
    }

    /** Hệ chính của loài (0 = Thường nếu không rõ). */
    public static int he(int dex) {
        int lo = 0;
        int hi = DEX.length - 1;
        while (lo <= hi) {
            int m = (lo + hi) >> 1;
            if (DEX[m] == dex) {
                return m < HE.length ? HE[m] : 0;
            }
            if (DEX[m] < dex) {
                lo = m + 1;
            } else {
                hi = m - 1;
            }
        }
        return 0;
    }

    /** Vẽ huy hiệu tâm (xTam, yTam), đường kính canh. false = không tạo được (nơi gọi vẽ kiểu cũ). */
    public static boolean ve(Graphics g, int dex, boolean shiny, int xTam, int yTam, int canh) {
        if (dex <= 0 || canh < 8) {
            return false;
        }
        String khoa = new StringBuffer("hh/").append(dex).append(shiny ? "s/" : "/").append(canh).toString();
        Object o = PokeBody.demLay(khoa);
        Image img = o instanceof Image ? (Image) o : null;
        if (o == null) {
            try {
                img = tao(dex, shiny, canh);
            } catch (OutOfMemoryError e) {
                PokeBody.xoaBoDem();
                img = null;
            } catch (Throwable e) {
                img = null;
            }
            PokeBody.demDat(khoa, img);
        }
        if (img == null) {
            return false;
        }
        g.drawImage(img, xTam, yTam, Graphics.HCENTER | Graphics.VCENTER);
        return true;
    }

    private static int tron(int a, int b, int t) {
        // pha a -> b theo t/256 (từng kênh)
        int r = (a >> 16 & 0xFF) + (((b >> 16 & 0xFF) - (a >> 16 & 0xFF)) * t >> 8);
        int gg = (a >> 8 & 0xFF) + (((b >> 8 & 0xFF) - (a >> 8 & 0xFF)) * t >> 8);
        int c = (a & 0xFF) + (((b & 0xFF) - (a & 0xFF)) * t >> 8);
        return r << 16 | gg << 8 | c;
    }

    private static Image tao(int dex, boolean shiny, int canh) {
        PokeBody pb = PokeBody.lay(dex, shiny);
        if (pb == null) {
            return null;
        }
        Image anh = pb.anhDung();
        if (anh == null) {
            return null;
        }
        PokeDongTac d = pb.dl.layDongTac(PokeDuLieu.DUNG);
        int rong = d.rongKhung;
        int cao = d.caoKhung;
        if (rong <= 0 || cao <= 0 || d.xAtlas + rong > anh.getWidth() || d.yAtlas + cao > anh.getHeight()) {
            return null;
        }
        int[] sp = new int[rong * cao];
        anh.getRGB(sp, 0, rong, d.xAtlas, d.yAtlas, rong, cao); // khung 0, hướng xuống
        // cắt sát phần có hình (khung có thể còn viền trong suốt)
        int x0 = rong, y0 = cao, x1 = -1, y1 = -1;
        for (int y = 0; y < cao; y++) {
            for (int x = 0; x < rong; x++) {
                if ((sp[y * rong + x] >>> 24) >= 0x80) {
                    if (x < x0) {
                        x0 = x;
                    }
                    if (x > x1) {
                        x1 = x;
                    }
                    if (y < y0) {
                        y0 = y;
                    }
                    if (y > y1) {
                        y1 = y;
                    }
                }
            }
        }
        if (x1 < 0) {
            return null;
        }
        int cw = x1 - x0 + 1;
        int ch = y1 - y0 + 1;
        // loài cao: lấy đầu + nửa thân trên (vuông theo bề ngang, tối thiểu 3/5 chiều cao)
        int chCat = ch;
        if (ch > cw) {
            chCat = cw > ch * 3 / 5 ? cw : ch * 3 / 5;
            if (chCat > ch) {
                chCat = ch;
            }
        }
        int trong = canh - 4;                       // đường kính lòng đĩa
        int lon = cw > chCat ? cw : chCat;
        int tl = trong * 256 * 92 / 100 / lon;       // tỉ lệ x256, chừa mép
        if (tl > 512) {
            tl = 512;                                 // tối đa x2 (giữ nét pixel)
        }
        int ws = cw * tl >> 8;
        int hs = chCat * tl >> 8;
        if (ws < 1) {
            ws = 1;
        }
        if (hs < 1) {
            hs = 1;
        }
        int ox = (canh - ws) / 2;
        int oy = (canh - hs) / 2 + (chCat < ch ? 1 : 0);
        int mau = KieuO.mauHe(he(dex));
        int sangTren = tron(mau, 0xFFFFFF, 110);
        int dam = tron(mau, 0x000000, 70);
        int vien = tron(mau, 0x101828, 150);
        int[] px = new int[canh * canh];
        int R2 = canh * canh;                         // bán kính^2 x4 (tọa độ nhân đôi)
        int Rv = (canh - 2) * (canh - 2);
        int Rl = (canh - 3) * (canh - 3);
        for (int y = 0; y < canh; y++) {
            int dy = 2 * y + 1 - canh;
            for (int x = 0; x < canh; x++) {
                int dx = 2 * x + 1 - canh;
                int d2 = dx * dx + dy * dy;
                int i = y * canh + x;
                if (d2 >= R2) {
                    px[i] = 0;
                    continue;
                }
                if (d2 >= Rv) {
                    px[i] = 0xFF000000 | vien;
                    continue;
                }
                int c = tron(sangTren, dam, y * 256 / canh);
                if (d2 >= Rl && dy < 0) {
                    c = tron(c, 0xFFFFFF, 90);        // viền sáng trong nửa trên
                }
                // vệt sáng góc trên-trái
                int hx = 2 * x + 1 - canh * 2 / 3;
                int hy = 2 * y + 1 - canh * 2 / 3;
                if (hx * hx + hy * hy < canh * canh / 9 && dy < 0) {
                    c = tron(c, 0xFFFFFF, 70);
                }
                px[i] = 0xFF000000 | c;
            }
        }
        // bóng nhẹ dưới chân sprite
        // sprite (lân cận gần nhất), chỉ trong lòng đĩa
        for (int y = 0; y < hs; y++) {
            int sy = y0 + (y << 8) / tl;
            if (sy > y0 + chCat - 1) {
                sy = y0 + chCat - 1;
            }
            int ty = oy + y;
            if (ty < 0 || ty >= canh) {
                continue;
            }
            int dy = 2 * ty + 1 - canh;
            for (int x = 0; x < ws; x++) {
                int sx = x0 + (x << 8) / tl;
                if (sx > x1) {
                    sx = x1;
                }
                int tx = ox + x;
                if (tx < 0 || tx >= canh) {
                    continue;
                }
                int dx = 2 * tx + 1 - canh;
                if (dx * dx + dy * dy >= Rv) {
                    continue;
                }
                int c = sp[sy * rong + sx];
                int a = c >>> 24;
                if (a < 0x40) {
                    continue;
                }
                int i = ty * canh + tx;
                px[i] = a >= 0xC0 ? (0xFF000000 | c) : (0xFF000000 | tron(px[i] & 0xFFFFFF, c & 0xFFFFFF, a));
            }
        }
        return Image.createRGBImage(px, canh, canh, true);
    }
}
