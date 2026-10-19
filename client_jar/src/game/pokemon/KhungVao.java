package game.pokemon;

import game.render.Canvas;
import javax.microedition.lcdui.Graphics;

/**
 * Bố cục MÀN TRƯỚC KHI VÀO GAME (0.9.5, bản PC / APK): đăng nhập, đăng ký, chọn máy chủ, tạo / chọn Pokémon. Logo luôn
 * thấy đủ, đội hình 5 Pokémon khởi đầu trên đường banner không bị che: bảng nằm GIỮA logo và đội hình nếu đủ cao, không thì
 * dạt sang cột bên (màn rộng), cùng một kiểu bảng sáng (Bang.khung) + nút thật trong bảng (không còn chip phím mềm).
 * J2ME (ChuUI.bo == null) giữ bố cục / phím mềm cũ.
 */
public final class KhungVao {
    /** đáy logo vừa vẽ (logo()), mép trái / phải logo */
    public static int logoDuoi = 0, logoTrai, logoPhai;

    private KhungVao() {
    }

    public static boolean moi() {
        return ChuUI.bo != null;
    }

    /** Logo cỡ thường (ThuongHieu.veLogo, giữa trên) - đặt vùng logo, CHƯA vẽ. */
    private static javax.microedition.lcdui.Image datLogoThuong() {
        int w = Canvas.w;
        int yTren = Math.max(6, Canvas.hh - 100);
        javax.microedition.lcdui.Image lg = Banner.co() ? Banner.logo(w - 8, Canvas.hh - 52 - Math.min(yTren, 8)) : null;
        int rong = lg == null ? 220 : lg.getWidth();
        int cao = lg == null ? 60 : lg.getHeight();
        int y = yTren;
        if (y + cao > Canvas.hh - 52) {
            y = Math.max(1, Canvas.hh - 52 - cao);
        }
        logoDuoi = y + cao;
        logoTrai = Canvas.hw - rong / 2;
        logoPhai = Canvas.hw + rong / 2;
        return lg;
    }

    /** Logo gọn (ảnh nhỏ nhất, sát mép trên) cho màn thấp - đặt vùng logo, CHƯA vẽ. */
    private static javax.microedition.lcdui.Image datLogoGon() {
        javax.microedition.lcdui.Image lg = Banner.co() ? Banner.logo(Math.min(Canvas.w - 8, 130), 40) : null;
        int rong = lg == null ? 160 : lg.getWidth();
        int cao = lg == null ? 34 : lg.getHeight();
        logoDuoi = 6 + cao;
        logoTrai = Canvas.hw - rong / 2;
        logoPhai = Canvas.hw + rong / 2;
        return lg;
    }

    /** kết quả lần đặt cuối: true = phải lùi bố cục D (không đủ chỗ) */
    private static boolean thieuCho;

    /**
     * Vẽ logo + trả chỗ đặt bảng {x, y, w, h} (xem dat): thử logo cỡ thường, không vừa thì logo gọn sát mép trên.
     */
    public static int[] logoVaBang(Graphics g, int wCan, int hRong, int hHep, int wMin) {
        Canvas.resetTrans(g);
        int[] rr = ngang(g, wCan, hRong, hHep, wMin);
        if (rr != null) {
            return rr;
        }
        javax.microedition.lcdui.Image lg = datLogoThuong();
        int[] r = dat(wCan, hRong, hHep, wMin);
        boolean gon = false;
        if (thieuCho) {
            javax.microedition.lcdui.Image lg2 = datLogoGon();
            int[] r2 = dat(wCan, hRong, hHep, wMin);
            if (!thieuCho || lg == null) {
                r = r2;
                lg = lg2;
                gon = true;
            } else {
                lg = lg2;
                r = r2;
                gon = true;
            }
        }
        if (gon) {
            if (lg != null) {
                GiaoDien.moClip(g);
                g.drawImage(lg, Canvas.hw, 6, Graphics.HCENTER | Graphics.TOP);
            } else {
                ThuongHieu.veTieuDe(g, Canvas.hw, 24, Canvas.w, 40);
            }
        } else if (Banner.co()) {
            ThuongHieu.veLogo(g, Canvas.hw, Math.max(6, Canvas.hh - 100), Canvas.w);
        } else {
            ThuongHieu.veTieuDe(g, Canvas.hw, Math.max(14, Canvas.hh - 125), Canvas.w, Canvas.hh - 54);
        }
        return r;
    }

    /**
     * 0.9.6 bố cục NGANG cho màn rộng thấp (điện thoại 19.5:9 - 21:9): logo canh giữa nửa TRÁI, bảng canh giữa nửa PHẢI,
     * cả hai canh giữa theo chiều dọc trong dải trên đội hình khởi đầu (đội hình không bị che), trong vùng an toàn.
     * null = không dùng (màn không đủ rộng / bảng không vừa) -> bố cục cũ.
     */
    static int[] ngang(Graphics g, int wCan, int hRong, int hHep, int wMin) {
        int W = Canvas.w, H = Canvas.h - game.render.screen.Screen.deltaY;
        if (W * 10 < H * 19 || !Banner.co()) {
            return null;
        }
        int L = le() + Bang.anTrai, R = W - le() - Bang.anPhai;
        int T = le() + Bang.anTren, B = H - le() - Bang.anDuoi;
        int[] k = Banner.vungKhoiDau();
        int yb = k == null ? B : Math.min(B, k[1] - 4);
        int mid = (L + R) / 2;
        int wc = Math.min(wCan, R - mid - 8);
        if (wc < Math.max(wMin, 200)) {
            return null;
        }
        int hc = wc >= 220 ? hRong : hHep;
        int xp = mid + (R - mid - wc) / 2;
        int yTren = T, yDay = yb;
        if (yb - T < hc) {
            // bảng không vừa dải trên đội hình: được xuống cạnh đội hình nếu không che (nằm bên phải đội hình)
            if (k == null || B - T < hc) {
                return null;
            }
            if (xp < k[2] + 6) {
                xp = Math.max(mid, R - wc);
                if (xp < k[2] + 6) {
                    return null;
                }
            }
            yDay = B;
        }
        int xLogoPhai = Math.min(mid, xp - 8);
        javax.microedition.lcdui.Image lg = Banner.logo(xLogoPhai - L - 16, yb - T - 8);
        if (lg == null) {
            return null;
        }
        int cxL = (L + xLogoPhai) / 2;
        int yl = T + (yb - T - lg.getHeight()) / 2;
        logoDuoi = yl + lg.getHeight();
        logoTrai = cxL - lg.getWidth() / 2;
        logoPhai = cxL + lg.getWidth() / 2;
        GiaoDien.moClip(g);
        g.drawImage(lg, cxL, yl, Graphics.HCENTER | Graphics.TOP);
        return new int[]{xp, yTren + (yDay - yTren - hc) / 2, wc, hc};
    }

    static int le() {
        return 10;
    }

    /**
     * Chỗ đặt bảng: {x, y, w, h}. wCan = bề ngang mong muốn; hRong = cao khi bảng rộng (>= 220), hHep = cao khi bảng hẹp
     * (cột bên); wMin = bề ngang tối thiểu cột bên. Thứ tự: dải giữa logo - đội hình; cột phải; cột trái; cuối cùng dải
     * giữa (cho phép chạm hàng sau đội hình nhưng KHÔNG che logo, thieuCho = true).
     */
    public static int[] dat(int wCan, int hRong, int hHep, int wMin) {
        thieuCho = false;
        int W = Canvas.w, H = Canvas.h - game.render.screen.Screen.deltaY;
        int L = le() + Bang.anTrai, R = W - le() - Bang.anPhai;
        int T = le() + Bang.anTren, B = H - le() - Bang.anDuoi;
        int[] k = Banner.vungKhoiDau();
        int w = Math.min(wCan, R - L);
        int h0 = w >= 220 ? hRong : hHep;
        // A: dải giữa logo và đội hình
        int y0 = Math.max(T, logoDuoi + 6);
        int y1 = k == null ? B : Math.min(B, k[1] - 4);
        if (y1 - y0 >= h0) {
            return new int[]{(L + R) / 2 - w / 2, y0 + (y1 - y0 - h0) / 2, w, h0};
        }
        if (k != null) {
            // B / C: cột phải / trái của đội hình
            for (int ben = 0; ben < 2; ben++) {
                int cx0 = ben == 0 ? k[2] + 6 : L;
                int cx1 = ben == 0 ? R : k[0] - 6;
                int wc = Math.min(wCan, cx1 - cx0);
                if (wc < wMin) {
                    continue;
                }
                int hc = wc >= 220 ? hRong : hHep;
                int x = ben == 0 ? cx1 - wc : cx0;
                boolean duoiLogo = x < logoPhai + 4 && x + wc > logoTrai - 4;
                int top = duoiLogo ? Math.max(T, logoDuoi + 6) : T;
                if (B - top >= hc) {
                    return new int[]{x, top + (B - top - hc) / 2, wc, hc};
                }
            }
        }
        // D: dải giữa, dài xuống quá đội hình (không che logo)
        thieuCho = true;
        int h = Math.min(h0, B - y0);
        return new int[]{(L + R) / 2 - w / 2, y0, w, Math.max(40, h)};
    }

    /** Cao một nút trong bảng. */
    public static int caoNut() {
        return Math.max(26, VungCham.MIN);
    }

    /** Khoảng cách giữa các phần tử. */
    public static int khe() {
        return Math.max(6, Bang.cach());
    }

    /** Cao một ô nhập (TField) trong bảng. */
    public static int caoO() {
        return Math.max(game.render.screen.Screen.ITEM_HEIGHT + 4, Math.min(26, VungCham.MIN));
    }

    /** Cao dòng nhãn trên ô. */
    public static int caoNhan() {
        return ChuDe.cao(ChuDe.PHU) + 2;
    }

    /** Một hàng nút chia đều (x, y, w): chu[i] / kieu[i] / viec[i]; trả y dưới hàng. */
    public static int hangNut(Graphics g, int x, int y, int w, String[] chu, int[] kieu, Object[] viec, String ten) {
        int n = chu.length, k = khe(), hb = caoNut();
        int wn = (w - (n - 1) * k) / n;
        for (int i = 0; i < n; i++) {
            Bang.nut(g, x + i * (wn + k), y, i == n - 1 ? x + w - (x + i * (wn + k)) : wn, hb, chu[i], kieu[i], viec[i], ten + "-" + i);
        }
        return y + hb;
    }

    /** Nhãn + ô nhập tại (x, y, w): đặt toạ độ ô, vẽ; trả y dưới ô. */
    public static int oNhap(Graphics g, game.render.screen.TField t, String nhan, int x, int y, int w, boolean loi) {
        if (nhan.endsWith(":")) {
            nhan = nhan.substring(0, nhan.length() - 1);
        }
        ChuDe.chu(g, nhan, x + 1, y, ChuDe.PHU, ChuDe.CHU_PHU, 0);
        y += caoNhan();
        t.x = x;
        t.y = y;
        t.width = w;
        t.height = caoO();
        if (loi) {
            GiaoDien.khoi(g, x - 2, y - 2, w + 4, t.height + 4, 4, ChuDe.DO);
        }
        t.paint(g);
        g.setClip(0, 0, Canvas.w, Canvas.h);
        return y + t.height;
    }

    /** Mở trang web (trình duyệt) không thoát game. */
    public static void moWeb(String url) {
        try {
            game.GameMidlet.instance.platformRequest(url);
        } catch (Throwable e) {
            Canvas.startOKDlg("Mở trình duyệt và vào: " + url);
        }
    }

    public static final String WEB = "https://huyenthoaibaoboi.fun";
}
