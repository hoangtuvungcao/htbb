package game.pokemon;

import game.model.Quest;
import game.model.Tilemap;
import game.render.Canvas;
import game.render.screen.GameScr;
import javax.microedition.lcdui.Graphics;

/**
 * BẢN ĐỒ VÙNG (Kanto / Johto / Hoenn) vẽ bằng code - dữ liệu + cách vẽ; màn = {@link ManBanDoVung}.
 * <p>
 * Người chơi báo 30/10/2026 "thêm cái mở bản đồ vùng để xem cho tiện": bản đồ lớn cũ chỉ ở Cài đặt → Bản đồ lớn, một
 * vùng 17 điểm toạ độ ảnh kiếm hiệp (New Bark lẫn vào Kanto, đứng ở Johto thì chấm vàng nhảy về Pewter), chỉ ghi tên điểm
 * đang đứng. Nay mỗi vùng là một cụm bản đồ NỐI CỔNG THẬT của máy chủ (real.Map.locationMap / com.htbb.pokemon.BanDoHoenn):
 * đường cát = cổng đi bộ được, đảo = một bản đồ, tên MỌI điểm (nhãn không chồng nhau), chân dung Pokémon ở điểm đang đứng,
 * dấu sao ở bản đồ đích nhiệm vụ (DiemNhiemVu). Không dịch chuyển: chạm / chọn điểm chỉ xem tên + khoá.
 * <p>
 * Cụm Kanto: làng chính (Pewter; người chơi lãnh thổ khác vào Blackthorn / Cianwood - bản sao cùng cổng) → vòng
 * Pallet ... Cinnabar → Cao nguyên Indigo, nhánh hang. Cụm Johto: New Bark → Rừng Viridian / Núi Mặt Trăng / Vùng Safari
 * (tên Kanto nhưng cổng ở cụm này) ... Toạ độ là bố cục tay (đơn vị tuỳ ý, vẽ co giãn vừa khung).
 */
public final class BanDoVung {
    private BanDoVung() {
    }

    public static final int KANTO = 0, JOHTO = 1, HOENN = 2;
    public static final String[] TEN_VUNG = {"Kanto", "Johto", "Hoenn"};

    /** id map từng điểm theo vùng (điểm 0 Kanto = làng chính: Pewter / Blackthorn / Cianwood). */
    static final short[][] ID = {
        {0, 1, 2, 4, 3, 110, 112, 111, 5, 6, 7, 8, 9, 201},
        {10, 13, 20, 19, 18, 14, 11, 114, 15, 24, 118, 23, 113, 26, 28, 29},
        {43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 160, 161, 162, 163, 164, 165, 280, 281, 282},
    };
    /** toạ độ {x, y} song song ID */
    static final short[][] X = {
        {30, 65, 65, 100, 100, 130, 170, 165, 110, 145, 175, 185, 155, 130},
        {185, 160, 90, 25, 40, 150, 150, 120, 95, 75, 100, 125, 128, 70, 45, 15},
        {35, 30, 28, 55, 80, 82, 84, 70, 66, 48, 108, 112, 138, 172, 140, 172, 205, 135, 195, 165, 55, 22, 160, 112, 200, 192, 230, 222, 250},
    };
    static final short[][] Y = {
        {75, 45, 105, 40, 100, 15, 12, 40, 135, 128, 110, 80, 80, 68},
        {70, 18, 15, 75, 135, 135, 70, 40, 75, 100, 112, 118, 90, 50, 25, 115},
        {158, 128, 100, 112, 140, 118, 98, 78, 56, 42, 84, 58, 48, 48, 70, 78, 88, 118, 118, 160, 138, 70, 104, 130, 145, 172, 128, 170, 160},
    };
    /** cổng đi bộ (cặp CHỈ SỐ điểm) - theo locationMap / BanDoHoenn */
    static final byte[][] CANH = {
        // Pewter-Pallet, Pewter-Viridian, Pallet-Cerulean, Viridian-Vermilion, Vermilion-Cerulean, Vermilion-Lavender,
        // Cerulean-Hang động, Hang động-Salamence / Darkrai, Lavender-Celadon-Saffron-Fuchsia-Cinnabar-Indigo
        {0, 1, 0, 2, 1, 3, 2, 4, 4, 3, 4, 8, 3, 5, 5, 6, 5, 7, 8, 9, 9, 10, 10, 11, 11, 12, 12, 13},
        // New Bark-Rừng Viridian / Núi Mặt Trăng / Safari, Rừng Viridian-Cherrygrove / Hang Tối / Hồ Phẫn Nộ, Núi Mặt Trăng-Violet,
        // Safari-Azalea / Goldenrod, Cherrygrove-Ecruteak / Olivine / Rừng Cổ, Hồ Phẫn Nộ-Violet, Ecruteak-Goldenrod / Violet,
        // Đường Victory-Azalea / Olivine
        {0, 6, 0, 1, 0, 5, 6, 8, 6, 12, 6, 7, 1, 2, 5, 11, 5, 4, 8, 3, 8, 9, 8, 13, 7, 2, 3, 4, 3, 2, 10, 11, 10, 9},
        {0, 1, 1, 2, 2, 3, 2, 21, 3, 4, 3, 20, 4, 5, 5, 6, 6, 7, 6, 10, 7, 8, 8, 9, 9, 21, 10, 11, 11, 12, 12, 13, 12, 14,
            13, 15, 15, 16, 15, 22, 16, 18, 17, 19, 17, 22, 17, 23, 18, 24, 19, 25, 24, 26, 25, 27, 27, 28},
    };

    /**
     * Bản đồ không có điểm riêng -> điểm gần nó {id, id điểm}: nhà / cửa hàng / chợ / bến tàu / đấu trường -> làng; nhà
     * Indigo, sân Liên minh, lãnh địa đội -> Cao nguyên Indigo.
     */
    static final short[] CHA = {12, 0, 41, 0, 42, 0, 100, 0, 101, 0, 102, 0, 103, 0, 104, 0, 105, 0, 106, 0, 107, 0, 202, 0,
        70, 0, 72, 0, 73, 0, 74, 0, 78, 0, 79, 0, 80, 0, 82, 0, 83, 0, 84, 0, 88, 0, 89, 0,
        206, 201, 203, 201, 30, 201, 31, 201, 32, 201, 33, 201, 34, 201, 35, 201, 36, 201, 37, 201, 38, 201, 39, 201};

    /** Map nền (id nạp) đang đứng. */
    public static int mapHienTai() {
        if (GameScr.mapNap >= 0) {
            return GameScr.mapNap;
        }
        return Tilemap.lv;
    }

    static int cha(int map) {
        if (map >= 260 && map <= 271) {
            return 201;   // nhà Indigo
        }
        for (int i = 0; i < CHA.length; i += 2) {
            if (CHA[i] == map) {
                return CHA[i + 1];
            }
        }
        return map;
    }

    /** Chỉ số điểm của map trong vùng v (kể cả map con -> điểm cha), -1 = không thuộc vùng. */
    public static int diem(int v, int map) {
        if (v < 0 || v >= ID.length || map < 0) {
            return -1;
        }
        int m = cha(map);
        short[] id = ID[v];
        for (int i = 0; i < id.length; i++) {
            if (id[i] == m) {
                return i;
            }
        }
        return -1;
    }

    /** Vùng (tab) của map: cụm có điểm của nó, không thì nhãn vùng địa lý (VungMap), không rõ = -1. */
    public static int vungCua(int map) {
        for (int v = 0; v < ID.length; v++) {
            if (diem(v, map) >= 0) {
                return v;
            }
        }
        return VungMap.vung(map);
    }

    public static int soDiem(int v) {
        return ID[v].length;
    }

    public static int idDiem(int v, int i) {
        return ID[v][i];
    }

    /** Tên điểm i của vùng v (làng chính: tên làng của lãnh thổ đang đứng). */
    public static String ten(int v, int i) {
        int id = ID[v][i];
        if (v == KANTO && i == 0) {
            int m = mapHienTai();
            if (m >= 70 && m <= 79) {
                id = 70;
            } else if (m >= 80 && m <= 89) {
                id = 80;
            }
        }
        return tenMap(id);
    }

    /** Tên hiện của bản đồ id (bảng SQL), "" = không rõ. */
    public static String tenMap(int id) {
        String t = TenBanDoBang.ten(id);
        if (t == null || t.length() == 0) {
            t = TenMapPokemon.tenHoenn(id);
        }
        return t == null ? "" : t;
    }

    /** Nhiệm vụ đang làm có bản đồ đích: id đích, -1 = không. */
    public static int mapNhiemVu() {
        Quest q = DiemNhiemVu.nhiemVu();
        return q == null ? -1 : q.dichMap;
    }

    // ------------------------------------------------------------------ vẽ

    /** Kiểm thử / chạm: toạ độ màn từng điểm vừa vẽ, số điểm, bán kính đảo. */
    public static final int[] VX = new int[40], VY = new int[40];
    public static int soVe, banKinh;
    /** Kiểm thử: nhãn tên đã vẽ của lần vẽ cuối (chỉ số điểm), chỉ số điểm có dấu sao, điểm đang đứng (-1 = không). */
    public static final boolean[] CO_NHAN = new boolean[40];
    public static int saoVe = -1, dungVe = -1;
    /** Vùng vẽ lần cuối {x, y, w, h}. */
    public static final int[] KHUNG = new int[4];

    private static final int[] NX = new int[40], NY = new int[40], NW = new int[40], NH = new int[40];

    /**
     * Vẽ vùng v vào ô (x, y, w, h): biển, đảo, đường cổng, chấm thị trấn (đỏ / xám khoá), chân dung Pokémon điểm đang
     * đứng, dấu sao điểm nhiệm vụ, vòng điểm đang chọn, nhãn tên (ưu tiên: đang đứng, nhiệm vụ, đang chọn, còn lại theo
     * thứ tự; nhãn chồng nhãn khác thì bỏ - chọn điểm để đọc tên ở dòng dưới).
     */
    public static void ve(Graphics g, int v, int x, int y, int w, int h, int chon) {
        int n = ID[v].length;
        KHUNG[0] = x;
        KHUNG[1] = y;
        KHUNG[2] = w;
        KHUNG[3] = h;
        int hn = ChuDe.cao(ChuDe.NHAN_NHO) + 2;
        // đảo
        int r = Math.max(7, Math.min(16, Math.min(w, h) / 16));
        banKinh = r;
        // co giãn bố cục vừa khung: lề ngang cho nhãn, lề dọc cho đảo + nhãn dưới + chân dung trên
        int minX = 9999, maxX = -9999, minY = 9999, maxY = -9999;
        for (int i = 0; i < n; i++) {
            minX = Math.min(minX, X[v][i]);
            maxX = Math.max(maxX, X[v][i]);
            minY = Math.min(minY, Y[v][i]);
            maxY = Math.max(maxY, Y[v][i]);
        }
        int leX = Math.max(r + 4, Math.min(w / 9, 34)), leTren = r + Math.max(10, r) + 2, leDuoi = r + hn + 4;
        int dw = Math.max(1, w - 2 * leX), dh = Math.max(1, h - leTren - leDuoi);
        for (int i = 0; i < n; i++) {
            VX[i] = x + leX + (X[v][i] - minX) * dw / Math.max(1, maxX - minX);
            VY[i] = y + leTren + (Y[v][i] - minY) * dh / Math.max(1, maxY - minY);
        }
        soVe = n;
        int cur = diem(v, mapHienTai());
        int dich = diem(v, mapNhiemVu());
        dungVe = cur;
        saoVe = dich;
        // biển + sóng
        GiaoDien.khoiDoc(g, x, y, w, h, 3, 0x5FA8E0, 0x2E6FB8);
        int cx0 = g.getClipX(), cy0 = g.getClipY(), cw0 = g.getClipWidth(), ch0 = g.getClipHeight();
        g.clipRect(x, y, w, h);
        g.setColor(0x8CC4F0);
        for (int i = 0; i < 18; i++) {
            int sx = x + (i * 53 + Canvas.gameTick / 4) % Math.max(1, w);
            int sy = y + 6 + (i * 37) % Math.max(1, h - 8);
            g.fillRect(sx, sy, 5, 1);
        }
        // đảo: viền cát rồi cỏ
        for (int i = 0; i < n; i++) {
            GiaoDien.khoi(g, VX[i] - r - 3, VY[i] - r + 1, 2 * r + 6, 2 * r - 2, r - 1, 0xE8D8A0);
        }
        for (int i = 0; i < n; i++) {
            GiaoDien.khoi(g, VX[i] - r - 1, VY[i] - r + 3, 2 * r + 2, 2 * r - 6, r - 3, GiaoDien.LA);
        }
        // đường cổng (cát, dày 3)
        byte[] c = CANH[v];
        for (int k = 0; k + 1 < c.length; k += 2) {
            int a = c[k], b = c[k + 1];
            g.setColor(0x9A7A40);
            g.drawLine(VX[a], VY[a] + 2, VX[b], VY[b] + 2);
            g.setColor(0xF0E2B0);
            g.drawLine(VX[a], VY[a], VX[b], VY[b]);
            g.drawLine(VX[a] + 1, VY[a], VX[b] + 1, VY[b]);
            g.drawLine(VX[a], VY[a] + 1, VX[b], VY[b] + 1);
        }
        // chấm thị trấn (+ khoá, AUTO)
        for (int i = 0; i < n; i++) {
            int id = ID[v][i];
            boolean khoa = KhoaMap.khoa(id);
            KHOA_[i] = khoa;
            GiaoDien.khoi(g, VX[i] - 5, VY[i] - 5, 11, 11, 5, GiaoDien.VIEN);
            GiaoDien.khoi(g, VX[i] - 4, VY[i] - 4, 9, 9, 4, i == cur ? GiaoDien.VANG : khoa ? ChuDe.XAM : GiaoDien.DO);
            g.setColor(GiaoDien.TRANG);
            g.fillRect(VX[i] - 2, VY[i] - 3, 3, 1);
            if (khoa) {
                BieuTuong.ve(g, BieuTuong.KHOA, VX[i] + 3, VY[i] - 13, 10, ChuDe.XAM, GiaoDien.LA);
            }
        }
        // điểm đang chọn: vòng trắng
        if (chon >= 0 && chon < n && chon != cur) {
            g.setColor(GiaoDien.TRANG);
            g.drawArc(VX[chon] - r, VY[chon] - r, 2 * r, 2 * r, 0, 360);
            g.drawArc(VX[chon] - r + 1, VY[chon] - r + 1, 2 * r - 2, 2 * r - 2, 0, 360);
        }
        // dấu sao nhiệm vụ (góc phải trên đảo)
        if (dich >= 0) {
            int sx = VX[dich] + r - 1, sy = VY[dich] - r + 1;
            DiemNhiemVu.sao(g, sx, sy, 9, GiaoDien.VIEN);
            DiemNhiemVu.sao(g, sx, sy, 7, DiemNhiemVu.mau());
        }
        // điểm đang đứng: vòng vàng nhấp nháy + chân dung Pokémon phía trên
        if (cur >= 0) {
            int rr = r + (Canvas.gameTick % 12 < 6 ? 1 : 0);
            g.setColor(GiaoDien.VANG);
            g.drawArc(VX[cur] - rr, VY[cur] - rr, 2 * rr, 2 * rr, 0, 360);
            g.drawArc(VX[cur] - rr + 1, VY[cur] - rr + 1, 2 * rr - 2, 2 * rr - 2, 0, 360);
            int s = Math.max(18, Math.min(30, 2 * r + 2));
            int yCd = VY[cur] - r - s / 2;
            if (yCd - s / 2 < y) {
                yCd = y + s / 2;
            }
            GameScr gs = Canvas.gameScr;
            int dex = gs != null && gs.mainChar != null ? gs.mainChar.pokeDex : 0;
            if (dex <= 0 || !PokeBody.paintChanDung(g, dex, gs.mainChar.pokeShiny, VX[cur], yCd, s)) {
                GiaoDien.pokeBall(g, VX[cur], yCd, s / 3, 0);
            }
        }
        // nhãn tên: thứ tự ưu tiên, bỏ nhãn chồng
        int soNhan = 0;
        for (int i = 0; i < n; i++) {
            CO_NHAN[i] = false;
        }
        for (int lan = 0; lan < n + 3; lan++) {
            int i = lan == 0 ? cur : lan == 1 ? dich : lan == 2 ? chon : lan - 3;
            if (i < 0 || i >= n || CO_NHAN[i] || lan >= 3 && (i == cur || i == dich || i == chon)) {
                continue;
            }
            String t = ten(v, i);
            int tw = Math.min(ChuDe.rongVien(t, ChuDe.NHAN_NHO, 0xF6E6C0), w - 8) + 8;
            t = ChuDe.cat(t, ChuDe.NHAN_NHO, tw - 8);
            // dưới đảo, trên đảo, phải, trái; nhãn không đè chấm điểm khác (điểm ưu tiên: lần hai bỏ luật này)
            boolean uuTien = lan < 3;
            for (int thu = 0; thu < (uuTien ? 8 : 4); thu++) {
                int vt = thu & 3;
                boolean noi = thu >= 4;
                int lx, ly;
                if (vt == 0) {
                    lx = VX[i] - tw / 2;
                    ly = VY[i] + r - 1;
                } else if (vt == 1) {
                    lx = VX[i] - tw / 2;
                    ly = VY[i] - r - hn + 1;
                } else if (vt == 2) {
                    lx = VX[i] + r + 2;
                    ly = VY[i] - hn / 2;
                } else {
                    lx = VX[i] - r - 2 - tw;
                    ly = VY[i] - hn / 2;
                }
                if (lx < x + 1) {
                    lx = x + 1;
                }
                if (lx + tw > x + w - 1) {
                    lx = x + w - 1 - tw;
                }
                if (ly < y + 1 || ly + hn > y + h - 1 || chong(lx, ly, tw, hn, soNhan)) {
                    continue;
                }
                if (!noi && deChamKhac(lx, ly, tw, hn, i, n, r)) {
                    continue;
                }
                // nhãn chồng lên đảo / chân dung điểm đang đứng thì thử chỗ khác
                if (cur >= 0 && i != cur && lx < VX[cur] + r && lx + tw > VX[cur] - r && ly < VY[cur] + r && ly + hn > VY[cur] - r - 30) {
                    continue;
                }
                int nen = i == cur ? 0x6A4A00 : i == dich ? 0x7A3E18 : 0x2B1A10;
                GiaoDien.khoi(g, lx, ly, tw, hn, 4, nen);
                ChuDe.chuVienGiua(g, t, lx + tw / 2, ly, hn, ChuDe.NHAN_NHO, i == cur ? GiaoDien.VANG_NHAT : 0xF6E6C0, ChuDe.VIEN, 2);
                NX[soNhan] = lx;
                NY[soNhan] = ly;
                NW[soNhan] = tw;
                NH[soNhan] = hn;
                soNhan++;
                CO_NHAN[i] = true;
                break;
            }
        }
        g.setClip(cx0, cy0, cw0, ch0);
    }

    /** điểm i đang khoá (ổ khoá vẽ ở góc phải trên chấm) - nhãn né ổ khoá (31/10/2026: J2ME "Vermilion" đè khoá) */
    private static final boolean[] KHOA_ = new boolean[64];

    /** Nhãn (x, y, w, h) của điểm i đè chấm / lõi đảo của điểm khác, hoặc đè ổ khoá của bất kỳ điểm nào. */
    private static boolean deChamKhac(int x, int y, int w, int h, int i, int n, int r) {
        int k = Math.max(5, r * 2 / 3);
        for (int j = 0; j < n && j < KHOA_.length; j++) {
            if (KHOA_[j] && x < VX[j] + 13 && x + w > VX[j] + 3 && y < VY[j] - 3 && y + h > VY[j] - 13) {
                return true;
            }
        }
        for (int j = 0; j < n; j++) {
            if (j != i && x < VX[j] + k && x + w > VX[j] - k && y < VY[j] + k && y + h > VY[j] - k) {
                return true;
            }
        }
        return false;
    }

    private static boolean chong(int x, int y, int w, int h, int n) {
        for (int k = 0; k < n; k++) {
            if (x < NX[k] + NW[k] + 2 && x + w + 2 > NX[k] && y < NY[k] + NH[k] && y + h > NY[k]) {
                return true;
            }
        }
        return false;
    }

    /** Điểm gần (px, py) nhất trong lần vẽ cuối (trong bán kính chạm), -1 = không. */
    public static int diemTai(int px, int py) {
        int tot = -1;
        long dTot = Long.MAX_VALUE;
        int rc = Math.max(banKinh * 2, Math.max(16, VungCham.MIN));
        for (int i = 0; i < soVe; i++) {
            long dx = px - VX[i], dy = py - VY[i];
            long d = dx * dx + dy * dy;
            if (d < dTot && d <= (long) rc * rc) {
                dTot = d;
                tot = i;
            }
        }
        return tot;
    }
}
