package game.pokemon;

import game.render.Canvas;
import game.render.Res;
import game.render.screen.Font;
import javax.microedition.lcdui.Graphics;

/**
 * "Bản đồ lớn" kiểu Pokémon (Cài đặt -> Bản đồ lớn): vẽ bằng code thay ảnh bản đồ kiếm hiệp
 * (địa danh Tứ Xuyên, Giao Châu... in sẵn trong ảnh). Biển xanh, đảo cỏ quanh mỗi địa điểm,
 * đường đi cát nối các điểm gần nhau, chấm đỏ = thị trấn, điểm đang đứng nhấp nháy vàng + chân
 * dung Pokémon của người chơi + tên. Tọa độ điểm giữ như bản gốc (ảnh 197 x 168), co giãn theo
 * khung (w x h) để không tràn màn 176 px.
 */
public final class BanDoVung {
    private BanDoVung() {
    }

    private static int qx(int x, int w) {
        return 6 + (x - 14) * (w - 12) / 172;
    }

    private static int qy(int y, int h) {
        return 34 + (y - 48) * (h - 44) / 122;
    }

    /** Vùng Hoenn (năm 2): tọa độ điểm (cùng hệ ảnh 197 x 168) song song ID_HOENN (id map server). */
    static final short[][] DIEM_HOENN = {{40, 150}, {30, 128}, {28, 100}, {55, 112}, {80, 140}, {82, 118}, {84, 98}, {70, 80}, {60, 64}, {45, 56}, {100, 78}, {108, 58}, {130, 62}, {160, 60}, {140, 82}, {160, 86}, {182, 92}, {135, 118}, {178, 118}, {150, 150}, {55, 140}, {30, 74}, {152, 104}, {118, 128}, {170, 138}, {184, 156}, {130, 158}, {112, 164}, {94, 166}};
    static final int[] ID_HOENN = {43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 160, 161, 162, 163, 164, 165, 280, 281, 282};

    /** Chỉ số map trong vùng Hoenn, -1 nếu không phải Hoenn. */
    public static int chiSoHoenn(int map) {
        for (int i = 0; i < ID_HOENN.length; i++) {
            if (ID_HOENN[i] == map) {
                return i;
            }
        }
        return -1;
    }

    /** Bản đồ vùng Hoenn, điểm đang đứng = map hiện tại. */
    public static void veHoenn(Graphics g, int map, int w, int h) {
        ve(g, DIEM_HOENN, chiSoHoenn(map), w, h, TenMapPokemon.tenHoenn(map));
    }

    /** pts = tọa độ gốc {x, y}; cur = điểm đang đứng; w, h = cỡ khung PageScr (gốc tọa độ = góc khung). */
    public static void ve(Graphics g, short[][] pts, int cur, int w, int h) {
        ve(g, pts, cur, w, h, null);
    }

    /** nhan = tên điểm đang đứng (null = Res.nameBigMap[cur]). */
    public static void ve(Graphics g, short[][] pts, int cur, int w, int h, String nhan) {
        int n = pts.length;
        int[] x = new int[n];
        int[] y = new int[n];
        for (int i = 0; i < n; i++) {
            x[i] = qx(pts[i][0], w);
            y[i] = qy(pts[i][1], h);
        }
        int bx = 4;
        int by = 29;
        int bw = w - 8;
        int bh = h - 33;
        GiaoDien.khoiDoc(g, bx, by, bw, bh, 3, 0x5FA8E0, 0x2E6FB8);
        int cx = g.getClipX();
        int cy = g.getClipY();
        int cw = g.getClipWidth();
        int ch = g.getClipHeight();
        g.clipRect(bx, by, bw, bh);
        // sóng nhỏ
        g.setColor(0x8CC4F0);
        for (int i = 0; i < 14; i++) {
            int sx = bx + (i * 53 + Canvas.gameTick / 4) % bw;
            int sy = by + 6 + (i * 37) % (bh - 8);
            g.fillRect(sx, sy, 4, 1);
        }
        // đảo: viền cát rồi cỏ
        for (int i = 0; i < n; i++) {
            GiaoDien.khoi(g, x[i] - 15, y[i] - 12, 30, 24, 11, 0xE8D8A0);
        }
        for (int i = 0; i < n; i++) {
            GiaoDien.khoi(g, x[i] - 13, y[i] - 10, 26, 20, 9, GiaoDien.LA);
        }
        // đường đi: mỗi điểm nối 2 điểm gần nhất
        g.setColor(0xF0E2B0);
        for (int i = 0; i < n; i++) {
            int a = -1;
            int b = -1;
            int da = Integer.MAX_VALUE;
            int db = Integer.MAX_VALUE;
            for (int j = 0; j < n; j++) {
                if (j == i) {
                    continue;
                }
                int dx = x[j] - x[i];
                int dy = y[j] - y[i];
                int d = dx * dx + dy * dy;
                if (d == 0) {
                    continue;
                }
                if (d < da) {
                    db = da;
                    b = a;
                    da = d;
                    a = j;
                } else if (d < db) {
                    db = d;
                    b = j;
                }
            }
            if (a >= 0) {
                g.drawLine(x[i], y[i], x[a], y[a]);
                g.drawLine(x[i], y[i] + 1, x[a], y[a] + 1);
            }
            if (b >= 0) {
                g.drawLine(x[i], y[i], x[b], y[b]);
            }
        }
        // chấm thị trấn
        for (int i = 0; i < n; i++) {
            GiaoDien.khoi(g, x[i] - 4, y[i] - 4, 9, 9, 4, GiaoDien.VIEN);
            GiaoDien.khoi(g, x[i] - 3, y[i] - 3, 7, 7, 3, i == cur ? GiaoDien.VANG : GiaoDien.DO);
            g.setColor(GiaoDien.TRANG);
            g.fillRect(x[i] - 1, y[i] - 2, 2, 1);
        }
        if (cur >= 0 && cur < n) {
            int r = 6 + (Canvas.gameTick % 12 < 6 ? 1 : 0);
            g.setColor(GiaoDien.VANG);
            g.drawArc(x[cur] - r, y[cur] - r, r * 2, r * 2, 0, 360);
            // chân dung Pokémon người chơi phía trên điểm
            int yCd = y[cur] - 18;
            if (yCd < by + 11) {
                yCd = y[cur] + 18;
            }
            int dex = Canvas.gameScr != null && Canvas.gameScr.mainChar != null ? Canvas.gameScr.mainChar.pokeDex : 0;
            if (dex <= 0 || !PokeBody.paintChanDung(g, dex, Canvas.gameScr.mainChar.pokeShiny, x[cur], yCd, 22)) {
                GiaoDien.pokeBall(g, x[cur], yCd, 7, 0);
            }
        }
        g.setClip(cx, cy, cw, ch);
        // nhãn tên điểm đang đứng (đáy khung)
        if (nhan != null || (cur >= 0 && cur < Res.nameBigMap.length)) {
            String ten = nhan != null ? nhan : TenBanDoBang.ten(cur, Res.nameBigMap[cur]);
            int tw = Font.borderFont.getWidth(ten) + 12;
            GiaoDien.khoi(g, w / 2 - tw / 2, h - 20, tw, 15, 6, GiaoDien.VIEN);
            GiaoDien.khoiDoc(g, w / 2 - tw / 2 + 1, h - 19, tw - 2, 13, 5, GiaoDien.XANH_SANG, GiaoDien.NAVY);
            Font.borderFont.drawString(g, ten, w / 2, h - 20, Font.CENTER);
        }
    }
}
