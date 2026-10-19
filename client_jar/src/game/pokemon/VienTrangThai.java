package game.pokemon;

import game.render.Canvas;
import javax.microedition.lcdui.Graphics;

/**
 * Viên trạng thái NHỎ dưới thẻ hồ sơ góc trái trên (0.9.5): "Auto · 4:41 · đang đánh" thay thanh xanh to cũ
 * (BangTuDong.veThanh đè thẻ), cùng kiểu viên giờ / PK của thẻ (PC htbb.pc.TheTrangThai.vienTin, APK Hud.vienTin):
 * viên trắng viền tối mảnh, chấm màu, chữ tối. Cộng EXP "+N EXP" hiện nhỏ cạnh thanh EXP của thẻ (gộp các lần nhận
 * liền nhau) ngay dưới góc phải thẻ (dưới thanh EXP) thay số bay giữa màn.
 *
 * Vị trí: jar đặt mặc định mỗi khung ({@link #macDinh}, GameScr.paintFocusActorInfo); thẻ HD của PC / APK ghi đè
 * ({@link #datHd}) sau khi vẽ thẻ + viên tin của nó.
 */
public final class VienTrangThai {
    private VienTrangThai() {
    }

    private static int xTrai = 4, yExp = 20, xExp = 120;
    private static int hdX, hdY, hdXExp, hdYExp, hdTick = -1000;
    /** khung trước có vẽ viên Tự động (thẻ HD dời huy hiệu 12+ xuống) */
    public static boolean coAuto;

    /** Jar: thẻ hồ sơ rộng rongThe ở (0, 0). */
    public static void macDinh(int rongThe) {
        xTrai = 4;
        xExp = rongThe - 2;
        yExp = 37;
    }

    /** Thẻ HD (PC / APK): x trái viên, y đáy các viên tin của thẻ, chữ EXP: canh phải xE, đỉnh yE (ngay dưới thẻ). */
    public static void datHd(int x, int yDuoi, int xE, int yE) {
        hdX = x;
        hdY = yDuoi;
        hdXExp = xE;
        hdYExp = yE;
        hdTick = Canvas.gameTick;
    }

    private static boolean hd() {
        int d = Canvas.gameTick - hdTick;
        return d >= 0 && d <= 4;
    }

    /** Cỡ chữ viên (px logic) - khớp viên tin của thẻ HD. */
    static float co() {
        return ChuDe.dt() ? 7.4f : 7.0f;
    }

    /** Viên trắng viền tối + chấm màu + chữ tối tại (x, y); trả bề ngang. */
    public static int vien(Graphics g, int x, int y, String s, int mauCham) {
        float c = co();
        int h = 12;
        int w = NhanTheGioi.rong(s, c, 1) + 17;
        GiaoDien.khoi(g, x, y, w, h, h / 2, ChuDe.VIEN);
        GiaoDien.khoi(g, x + 1, y + 1, w - 2, h - 2, h / 2 - 1, 0xFAFBFD);
        g.setColor(mauCham);
        g.fillArc(x + 4, y + h / 2 - 3, 6, 6, 0, 360);
        ChuUI b = ChuUI.bo;
        if (b != null) {
            b.ve(g, s, x + 12, y + (h - b.cao(c)) / 2, c, 1, ChuDe.CHU, 0);
        } else {
            ChuDe.chuGiua(g, s, x + 12, y, h, ChuDe.PHU, ChuDe.CHU, 0);
        }
        return w;
    }

    // ------------------------------------------------------------------ Tự động đánh
    private static String auto = "";
    private static int autoPhut = -1;
    private static String autoTT;
    private static boolean autoCho;

    /**
     * Chữ viên Tự động: "Auto · 4:41 · đang đánh" (nhớ chuỗi, chỉ dựng lại khi phút / trạng thái đổi).
     * giay &lt; 0 = đang hỏi máy chủ.
     */
    public static String chuAuto(int giay, String trangThai) {
        boolean cho = giay < 0;
        int phut = cho ? -1 : giay / 60;
        if (phut == autoPhut && cho == autoCho && trangThai == autoTT) {
            return auto;
        }
        autoPhut = phut;
        autoCho = cho;
        autoTT = trangThai;
        StringBuffer b = new StringBuffer("Auto");
        if (cho) {
            b.append(" · đang hỏi máy chủ…");
        } else {
            b.append(" · ").append(phut / 60).append(':').append(phut % 60 < 10 ? "0" : "").append(phut % 60);
            if (trangThai != null && trangThai.length() > 0) {
                b.append(" · ").append(trangThai);
            }
        }
        auto = b.toString();
        return auto;
    }

    /** AutoGuard -> TuDong.ve: viên Tự động (s null = không) + chữ EXP; yJar = y mặc định của jar / APK. */
    public static void ve(Graphics g, String s, boolean dang, int yJar) {
        if (Canvas.currentScreen != Canvas.gameScr || Canvas.gameScr == null || Canvas.gameScr.hideGUI == 2) {
            return;
        }
        Canvas.resetTrans(g);
        boolean h = hd();
        coAuto = s != null;
        int xE = h ? hdXExp : xExp, yE = h ? hdYExp : yExp;
        if (s != null) {
            int x = h ? hdX : xTrai, y = h ? hdY : yJar;
            int w = vien(g, x, y, s, dang ? ChuDe.LA : ChuDe.XANH);
            // viên dài chạm chỗ chữ EXP: EXP xuống dưới viên
            if (expChu != null && x + w + 4 > xE - NhanTheGioi.rong(expChu, coExp(), 2) && y + 12 > yE) {
                yE = y + 14;
            }
        }
        veExp(g, xE, yE);
    }

    // ------------------------------------------------------------------ +EXP
    private static long expTong;
    private static int expLuc = -1000;
    private static String expChu;
    /** gộp các lần nhận cách nhau &lt; 1,6 s; hiện 1,8 s */
    static final int GOP = 40, SONG = 45;

    /** SoBay: "+N xp" của nhân vật chính. */
    public static void themExp(int n) {
        int d = Canvas.gameTick - expLuc;
        if (d < 0 || d > GOP) {
            expTong = 0;
        }
        expTong += n;
        expLuc = Canvas.gameTick;
        expChu = new StringBuffer("+").append(expTong).append(" EXP").toString();
    }

    static float coExp() {
        return ChuDe.dt() ? 7.2f : 6.6f;
    }

    static void veExp(Graphics g, int xPhai, int yTren) {
        int d = Canvas.gameTick - expLuc;
        if (expChu == null || d < 0 || d > SONG) {
            return;
        }
        float c = coExp();
        int mau = 0xFFD23A;
        if (d > SONG - 6) {
            mau = GiaoDien.pha(mau, NhanTheGioi.VIEN, d - (SONG - 6), 8);
        }
        int y = yTren + (d < 4 ? 4 - d : 0);
        NhanTheGioi.chuVien(g, expChu, xPhai, y, c, 2, mau, NhanTheGioi.VIEN, 1);
    }
}
