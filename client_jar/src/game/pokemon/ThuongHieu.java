package game.pokemon;

import game.render.screen.Font;
import javax.microedition.lcdui.Graphics;

/**
 * Ten game va thuong hieu - ban J2ME cua ClientUnity ThuongHieu.cs (viec G7.E1/E2, muc 15).
 * Ban server: HTBB src/com/htbb/pokemon/ThuongHieu.java. Doi ten game: sua ca ba noi.
 * Man tai / chon may chu / dang nhap: ve tieu de Pokemon (ten game + 5 chan dung khoi dau)
 * thay cho logo cua game goc TeaMobi.
 */
public final class ThuongHieu {
    public static final String TEN_GAME = "Huyền Thoại Bảo Bối";
    /** Ten game viet hoa cho bigFont (font nay chi co chu hoa, ke ca chu hoa co dau). */
    public static final String TEN_GAME_HOA = "HUYỀN THOẠI BẢO BỐI";
    public static final String TEN_VIET_TAT = "HTBB";
    public static final String TEN_GAME_CU = "Khí Phách Anh Hùng";
    public static final String GIOI_THIEU = "Game fan Pokémon phi thương mại";
    public static final String GIOI_THIEU_2 = "Pokémon (c) Nintendo / Game Freak";
    /** Ban ngan cho man hep (176 px: hai dong tren ~173/178 px tran khung). */
    private static final String GIOI_THIEU_NGAN = "Game fan phi thương mại";
    private static final String GIOI_THIEU_2_NGAN = "Pokémon: Nintendo/Game Freak";
    public static final int SO_KHOI_DAU = 5;

    private ThuongHieu() {
    }

    public static String tenGame() {
        return PokeBody.CHE_DO_POKEMON ? TEN_GAME : TEN_GAME_CU;
    }

    /**
     * Logo tên game trên nền banner (5 Pokémon khởi đầu đã đứng trên cỏ nên không vẽ hàng chân
     * dung): bảng navy mờ viền vàng, Poké Ball hai bên, chữ tên game, vệt sáng lướt qua mỗi ~4 giây.
     * Trả về đáy logo.
     */
    public static int veLogo(Graphics g, int xTam, int yTren, int rongToiDa) {
        // logo pixel art (client_jar/tools/tao_logo.py - cùng wordmark với PC / web): lớn nhất vừa chỗ trên khung
        // giữa màn (đỉnh khung ~ hh - 50); không đủ chỗ thì kéo logo lên sát mép trên
        int day = game.render.Canvas.hh - 52;
        javax.microedition.lcdui.Image lg = Banner.logo(rongToiDa - 8, day - Math.min(yTren, 8));
        if (lg != null) {
            int y = yTren;
            if (y + lg.getHeight() > day) {
                y = Math.max(1, day - lg.getHeight());
            }
            GiaoDien.moClip(g);
            g.drawImage(lg, xTam, y, Graphics.HCENTER | Graphics.TOP);
            // lấp lánh nhỏ chạy quanh mép trên logo mỗi ~4 giây
            int t = game.render.Canvas.gameTick % 100;
            if (t < 16) {
                int xs = xTam - lg.getWidth() / 2 + lg.getWidth() * t / 16;
                int ys = y + 2 + (t & 3);
                g.setColor(0xFFFFFF);
                g.fillRect(xs, ys - 2, 1, 5);
                g.fillRect(xs - 2, ys, 5, 1);
            }
            return y + lg.getHeight();
        }
        Font chu = Font.bigFont;
        String ten = TEN_GAME_HOA;
        int wMax = rongToiDa - 12;
        if (chu == null || chu.getWidth(ten) + 52 > wMax) {
            chu = Font.borderFont != null ? Font.borderFont : Font.normalFont[0];
            ten = TEN_GAME;
        }
        if (chu == null) {
            return yTren;
        }
        int wChu = chu.getWidth(ten);
        int w = wChu + 52;
        if (w > wMax) {
            w = wMax;
        }
        int h = 26;
        int x = xTam - w / 2;
        GiaoDien.moClip(g);
        KieuO.phu(g, x + 3, yTren + 3, w, h, 0x500B1222);
        GiaoDien.khoi(g, x, yTren, w, h, 7, GiaoDien.VANG_TOI);
        GiaoDien.khoiDoc(g, x + 2, yTren + 2, w - 4, h - 4, 5, 0x2D62AA, GiaoDien.NAVY);
        g.setColor(GiaoDien.VANG);
        g.drawRoundRect(x + 1, yTren + 1, w - 3, h - 3, 10, 10);
        chu.drawString(g, ten, xTam, chu == Font.bigFont ? yTren + 4 : yTren + 6, Font.CENTER);
        GiaoDien.pokeBall(g, x + 11, yTren + h / 2, 6, (game.render.Canvas.gameTick * 3) % 24);
        GiaoDien.pokeBall(g, x + w - 11, yTren + h / 2, 6, 0);
        // vệt sáng chéo lướt qua chữ
        int chuKy = 100;
        int t = game.render.Canvas.gameTick % chuKy;
        if (t < 24) {
            int xs = x + (w + 20) * t / 24 - 10;
            int cx = g.getClipX(), cy = g.getClipY(), cw = g.getClipWidth(), ch = g.getClipHeight();
            g.clipRect(x + 3, yTren + 3, w - 6, h - 6);
            for (int i = 0; i < h; i++) {
                KieuO.phu(g, xs + i / 3, yTren + i, 4, 1, 0x70FFFFFF);
                KieuO.phu(g, xs + 6 + i / 3, yTren + i, 1, 1, 0x50FFFFFF);
            }
            g.setClip(cx, cy, cw, ch);
        }
        return yTren + h;
    }

    /**
     * Ve tieu de Pokemon, tam ngang xTam, dong ten game o yTam, hang chan dung ngay duoi.
     * rongToiDa: be ngang man hinh (de thu nho hang chan dung tren may 128/176 px).
     * Tra ve false (de ve logo cu) khi tat che do Pokemon hoac chua co gi de ve.
     */
    public static boolean veTieuDe(Graphics g, int xTam, int yTam, int rongToiDa) {
        return veTieuDe(g, xTam, yTam, rongToiDa, 100000);
    }

    /**
     * Nhu tren; dong gioi thieu nao vuot qua yDay (dinh khung ben duoi) thi bo. Tat ca nam tren mot
     * "bien ten" trang vien toi, dai do chua ten game (GiaoDien.bienTen) de doc ro tren nen ban do.
     */
    public static boolean veTieuDe(Graphics g, int xTam, int yTam, int rongToiDa, int yDay) {
        if (!PokeBody.CHE_DO_POKEMON) {
            return false;
        }
        int khoang = 38;
        int canh = 32;
        if (rongToiDa > 0 && khoang * SO_KHOI_DAU > rongToiDa - 18) {
            khoang = (rongToiDa - 18) / SO_KHOI_DAU;
            canh = khoang - 4;
        }
        int yAnh = yTam + 15;
        if (yAnh + canh + 4 > yDay) {
            canh = yDay - 4 - yAnh;
        }
        boolean coAnh = canh >= 14;
        int yChu = coAnh ? yAnh + canh + 2 : yAnh;
        int soDong = 0;
        if (yChu + 14 <= yDay - 2) {
            soDong = 1;
        }
        if (yChu + 27 <= yDay - 2) {
            soDong = 2;
        }
        int day = yChu + soDong * 13 + 3;
        int wP = SO_KHOI_DAU * khoang + 14;
        if (rongToiDa > 0 && wP > rongToiDa - 6) {
            wP = rongToiDa - 6;
        }
        boolean daVe = false;
        try {
            GiaoDien.hop(g, xTam - wP / 2, yTam - 14, wP, day - yTam + 16);
            GiaoDien.khoiDoc(g, xTam - wP / 2 + 3, yTam - 11, wP - 6, 22, 3, GiaoDien.DO_SANG, GiaoDien.DO_TOI);
            g.setColor(GiaoDien.VIEN);
            g.fillRect(xTam - wP / 2 + 3, yTam + 11, wP - 6, 1);
            Font chu = Font.bigFont;
            String ten = TEN_GAME_HOA;
            if (chu == null || rongToiDa > 0 && chu.getWidth(ten) > wP - 30) {
                chu = Font.borderFont != null ? Font.borderFont : Font.normalFont[0];
                ten = TEN_GAME;
            }
            if (chu != null) {
                chu.drawString(g, ten, xTam, chu == Font.bigFont ? yTam - 9 : yTam - 7, Font.CENTER);
                daVe = true;
            }
            if (chu != null && chu.getWidth(ten) + 34 < wP) {
                GiaoDien.pokeBall(g, xTam - wP / 2 + 13, yTam, 6, 0);
                GiaoDien.pokeBall(g, xTam + wP / 2 - 13, yTam, 6, 0);
            }
        } catch (Throwable e) {
            // font chua nap (man tai dau tien): chi co chan dung cung du
        }
        if (coAnh) {
            int x0 = xTam - (SO_KHOI_DAU - 1) * khoang / 2;
            for (int i = 0; i < SO_KHOI_DAU; i++) {
                int dex = LoaiPokemonClient.khoiDau(i);
                if (dex > 0 && PokeBody.paintChanDung(g, dex, false, x0 + i * khoang, yAnh + canh / 2, canh)) {
                    daVe = true;
                }
            }
        }
        try {
            Font f = Font.arialFont != null ? Font.arialFont : Font.normalFont[0];
            if (soDong >= 1) {
                f.drawString(g, f.getWidth(GIOI_THIEU) > wP - 10 ? GIOI_THIEU_NGAN : GIOI_THIEU, xTam, yChu, Font.CENTER);
            }
            if (soDong >= 2) {
                f.drawString(g, f.getWidth(GIOI_THIEU_2) > wP - 10 ? GIOI_THIEU_2_NGAN : GIOI_THIEU_2, xTam, yChu + 13, Font.CENTER);
            }
        } catch (Throwable e) {
            // font chua nap
        }
        return daVe;
    }
}
