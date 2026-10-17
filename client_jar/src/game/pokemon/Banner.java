package game.pokemon;

import game.render.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Nền banner cho màn trước khi vào game (tải, chọn máy chủ, đăng nhập, chọn / tạo nhân vật) thay
 * cho bản đồ game phía sau: cảnh nhiều lớp parallax - trời bình minh (dải màu vẽ bằng code), quầng
 * mặt trời, 2 lớp mây trôi chậm, dãy núi xa, đồi cây, thảm cỏ tiền cảnh (tile Ninja Adventure CC0),
 * đom đóm lấp lánh và 5 Pokémon khởi đầu đứng thở trên cỏ (Pokémon đang chọn: cột sáng + vầng hào
 * quang màu hệ). Ảnh lớp ở /bn/ (j2me/tools/tao_banner.py, nguồn + ảnh 1920x1080 ở shared/banner);
 * bản NHẸ chỉ có dat.png -> trời + một lớp cỏ.
 */
public final class Banner {
    private static boolean daNap;
    private static Image nui, doi, dat, may1, may2, quang, cot;

    /** Dải màu trời: vị trí (phần nghìn của chiều cao trời) + màu. */
    private static final int[] MOC = {0, 350, 620, 820, 1000};
    private static final int[] MAU = {0x26306C, 0x6C4F9C, 0xE07A78, 0xFFB27A, 0xFFE0A6};

    private Banner() {
    }

    private static Image anh(String ten) {
        try {
            return Image.createImage("/bn/" + ten + ".png");
        } catch (Throwable e) {
            return null;
        }
    }

    private static void nap() {
        if (daNap) {
            return;
        }
        daNap = true;
        nui = anh("nui");
        doi = anh("doi");
        dat = anh("dat");
        may1 = anh("may1");
        may2 = anh("may2");
        try {
            // quầng mặt trời: đĩa sáng alpha giảm dần (một lần, 72x72)
            int d = 72;
            int[] p = new int[d * d];
            for (int y = 0; y < d; y++) {
                for (int x = 0; x < d; x++) {
                    int dx = 2 * x + 1 - d;
                    int dy = 2 * y + 1 - d;
                    int r2 = (dx * dx + dy * dy) / 4;
                    int R = d / 2;
                    if (r2 < R * R) {
                        int t = 255 - r2 * 255 / (R * R);
                        p[y * d + x] = (t * t / 255 * 150 / 255) << 24 | 0xFFECBE;
                    }
                }
            }
            quang = Image.createRGBImage(p, d, d, true);
            // cột sáng chiếu xuống Pokémon đang chọn: hình thang, alpha đậm dần xuống dưới
            int cw = 44;
            int ch = 96;
            int[] q = new int[cw * ch];
            for (int y = 0; y < ch; y++) {
                int nua = 6 + (cw / 2 - 6) * y / ch;
                int a = 20 + 70 * y / ch;
                for (int x = 0; x < cw; x++) {
                    int k = x - cw / 2;
                    if (k >= -nua && k < nua) {
                        int mep = nua - (k < 0 ? -k : k);
                        int aa = mep < 4 ? a * mep / 4 : a;
                        q[y * cw + x] = aa << 24 | 0xFFF6D0;
                    }
                }
            }
            cot = Image.createRGBImage(q, cw, ch, true);
        } catch (Throwable e) {
            quang = null;
            cot = null;
        }
    }

    /** Có ảnh banner trong jar không (không có thì nơi gọi vẽ nền cũ). */
    public static boolean co() {
        nap();
        return dat != null;
    }

    private static int mauTroi(int phanNghin) {
        for (int i = 0; i < MOC.length - 1; i++) {
            if (phanNghin <= MOC[i + 1]) {
                return GiaoDien.pha(MAU[i], MAU[i + 1], phanNghin - MOC[i], MOC[i + 1] - MOC[i]);
            }
        }
        return MAU[MAU.length - 1];
    }

    private static void lat(Graphics g, Image a, int dx, int y, int w) {
        if (a == null) {
            return;
        }
        int aw = a.getWidth();
        int x = -(dx % aw);
        if (x > 0) {
            x -= aw;
        }
        for (; x < w; x += aw) {
            g.drawImage(a, x, y, Graphics.TOP | Graphics.LEFT);
        }
    }

    /** Tung độ chân của Pokémon trên thảm cỏ (để màn khác canh chỗ). */
    public static int yCo() {
        return Canvas.h - game.render.screen.Screen.deltaY - 14;
    }

    /**
     * Vẽ toàn màn. chon = Pokémon khởi đầu đang chọn (0..4 theo LoaiPokemonClient.khoiDau), -1 = không;
     * veKhoiDau = có vẽ 5 Pokémon đứng trên cỏ hay không.
     */
    public static boolean ve(Graphics g, int chon, boolean veKhoiDau) {
        nap();
        if (dat == null) {
            return false;
        }
        int w = Canvas.w;
        int h = Canvas.h;
        int tick = Canvas.gameTick;
        g.setClip(0, 0, w, h);
        int yDat = h - game.render.screen.Screen.deltaY - 52;
        int yDoi = yDat - 26;
        int yNui = yDoi - 38;
        boolean du = nui != null;
        int chanTroi = (du ? yNui + 46 : yDat + 24);
        // ---- trời (dải 3 px)
        for (int y = 0; y < chanTroi; y += 3) {
            g.setColor(mauTroi(y * 1000 / chanTroi));
            g.fillRect(0, y, w, 3);
        }
        g.setColor(MAU[MAU.length - 1]);
        g.fillRect(0, chanTroi, w, h - chanTroi);
        // ---- mặt trời
        int xMt = w * 68 / 100;
        int yMt = du ? yNui + 22 : chanTroi - 22;
        if (quang != null) {
            g.drawImage(quang, xMt, yMt, Graphics.HCENTER | Graphics.VCENTER);
        }
        g.setColor(0xFFF4D2);
        g.fillArc(xMt - 9, yMt - 9, 18, 18, 0, 360);
        // ---- mây (2 lớp, trôi chậm khác tốc độ)
        if (may1 != null) {
            int vong = w + 70;
            for (int i = 0; i < 2; i++) {
                int x = (i * vong / 2 + tick / 6) % vong - 60;
                g.drawImage(may1, x, 14 + i * 34, Graphics.TOP | Graphics.LEFT);
            }
        }
        if (may2 != null) {
            int vong = w + 40;
            for (int i = 0; i < 3; i++) {
                int x = (i * vong / 3 + 20 + tick / 10) % vong - 36;
                g.drawImage(may2, x, 38 + (i * 23) % 40, Graphics.TOP | Graphics.LEFT);
            }
        }
        // ---- núi xa, đồi, cỏ (lặp ngang khít)
        if (du) {
            lat(g, nui, w / 5, yNui, w);
        }
        if (doi != null) {
            lat(g, doi, w / 3, yDoi, w);
        }
        lat(g, dat, 0, yDat, w);
        g.setColor(0x4E9A3C);
        g.fillRect(0, yDat + dat.getHeight(), w, h);
        // ---- đom đóm / lấp lánh
        int yTren = du ? yNui + 8 : chanTroi - 30;
        int cao = yDat + 40 - yTren;
        for (int i = 0; i < 14; i++) {
            int pha = (tick + i * 13) % 48;
            if (pha >= 30) {
                continue;
            }
            int x = (i * 53 + tick * (2 + i % 3) / 8) % (w + 20) - 10;
            int y = yTren + (i * 37) % (cao > 1 ? cao : 1) + (pha < 15 ? pha : 30 - pha) / 5;
            boolean to = pha > 8 && pha < 22;
            g.setColor(to ? 0xFFF7B0 : 0xFFE070);
            g.fillRect(x, y, to ? 2 : 1, to ? 2 : 1);
            if (to && (i & 1) == 0) {
                g.setColor(0xFFF0A0);
                g.fillRect(x - 1, y + 0, 1, 1);
                g.fillRect(x + 2, y + 1, 1, 1);
            }
        }
        if (veKhoiDau) {
            veKhoiDau(g, chon, w);
        }
        return true;
    }

    private static void veKhoiDau(Graphics g, int chon, int w) {
        int n = ThuongHieu.SO_KHOI_DAU;
        int yChan = yCo();
        int tick = Canvas.gameTick;
        for (int i = 0; i < n; i++) {
            int dex = LoaiPokemonClient.khoiDau(i);
            if (dex <= 0) {
                continue;
            }
            int x = w * (2 * i + 1) / (2 * n);
            if (i == chon) {
                int mau = KieuO.mauHe(HuyHieuPoke.he(dex));
                if (cot != null) {
                    g.drawImage(cot, x, yChan + 2, Graphics.HCENTER | Graphics.BOTTOM);
                }
                // vầng hào quang màu hệ dưới chân + vòng lan ra
                int r = 13 + (tick % 20) / 4;
                g.setColor(GiaoDien.pha(mau, 0x000000, 1, 3));
                g.fillArc(x - 15, yChan - 4, 30, 9, 0, 360);
                g.setColor(mau);
                g.fillArc(x - 13, yChan - 3, 26, 7, 0, 360);
                g.setColor(GiaoDien.pha(mau, 0xFFFFFF, 1, 2));
                g.drawArc(x - r - 2, yChan - 4 - r / 6, (r + 2) * 2, 9 + r / 3, 0, 360);
            } else {
                g.setColor(0x3B7A30);
                g.fillArc(x - 10, yChan - 3, 20, 6, 0, 360);
            }
            // thở: nhún 1 px theo nhịp, Pokémon đang chọn nhảy nhẹ
            int dy = 0;
            if (i == chon) {
                int t = (tick + i * 7) % 24;
                dy = t < 12 ? -(t < 6 ? t : 12 - t) / 2 : 0;
            }
            PokeBody.paint(g, dex, false, x, yChan + dy, 0, PokeDuLieu.DUNG, tick + i * 9, 100);
        }
    }
}
