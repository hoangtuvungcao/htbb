package game.pokemon;

import game.render.Canvas;
import game.render.screen.GameScr;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * NHÃN THẾ GIỚI (0.9.5): tên người chơi / "Lv N Tên" của quái vẽ ở LỚP GIAO DIỆN (chữ thật ChuUI cỡ nhỏ, viền tối),
 * không còn vẽ trong lớp thế giới phóng x2 (chữ to gấp đôi, đè nhau, đè hình).
 *
 * Lúc vẽ thế giới, Char.paintName / NhanQuai chỉ GHI yêu cầu (toạ độ thế giới của đáy nhãn = sát đỉnh hình thật) vào
 * mảng cố định; GameScr.paintFocusActorInfo (đã hết lớp thế giới) gọi {@link #ve}: đổi toạ độ thế giới -> giao diện,
 * xếp chỗ theo ưu tiên (mục tiêu đang chọn > bản thân > người chơi khác > pet đi theo > quái vừa trúng > quái), nhãn
 * chồng nhãn đã đặt thì nhích lên một hàng, vẫn chồng thì người chơi vẽ MỜ, pet / quái bỏ; nhãn pet / quái không đè hình
 * nhân vật chính. Nhãn pet (0.9.6, NhanPet) chữ nhỏ hơn + màu dịu hơn tên người chơi. Vẽ
 * ngược thứ tự ưu tiên: nhãn người chơi nằm TRÊN nhãn quái, mục tiêu trên cùng (nền viên tối + thanh HP).
 * Không cấp phát mỗi khung. Máy J2ME (không có ChuUI) giữ cách vẽ cũ trong lớp thế giới (không phóng).
 */
public final class NhanTheGioi {
    private NhanTheGioi() {
    }

    public static final int QUAI = 0, NGUOI = 1, BAN_THAN = 2, TRUM = 3, KHAC_PHE = 4, PET = 5;

    static final int TOI_DA = 64;
    private static final Object[] AI = new Object[TOI_DA];
    private static final String[] CHU = new String[TOI_DA];
    private static final String[] TIEN = new String[TOI_DA];
    /** 0.9.7 danh hiệu đang đeo của người chơi (dòng nhỏ trên tên, màu độ hiếm), null = không */
    private static final String[] DH = new String[TOI_DA];
    private static final int[] DHH = new int[TOI_DA], WCHU = new int[TOI_DA], HDH = new int[TOI_DA];
    private static final int[] LOAI = new int[TOI_DA];
    private static final int[] WX = new int[TOI_DA], WY = new int[TOI_DA], WCHAN = new int[TOI_DA];
    /** HP phần nghìn (-1 = không thanh) */
    private static final int[] HP = new int[TOI_DA];
    private static final boolean[] CHON = new boolean[TOI_DA], NONG = new boolean[TOI_DA];
    private static int so;
    private static int khungGhi = -1;

    // xếp chỗ (toạ độ giao diện)
    private static final int[] X = new int[TOI_DA], Y = new int[TOI_DA], W = new int[TOI_DA], H = new int[TOI_DA];
    private static final int[] WTIEN = new int[TOI_DA];
    /** 0 bỏ, 1 thường, 2 mờ */
    private static final int[] TT = new int[TOI_DA];
    private static final int[] THU = new int[TOI_DA];

    static final int VIEN = 0x121828;

    /** Có vẽ nhãn ở lớp giao diện (bản có chữ thật). */
    public static boolean bat() {
        return ChuUI.bo != null;
    }

    private static boolean ghi(Object ai, int loai, String tien, String chu, int wx, int wyDay, int wyChan, int hp, boolean chon, boolean nong) {
        if (ChuUI.bo == null || chu == null) {
            return false;
        }
        if (GameScr.laNen) {
            return true;
        }
        // khung vẽ mới mà khung trước không tới bước vẽ (lỗi giữa chừng): bỏ hàng đợi cũ
        if (khungGhi != NhipVe.demKhung) {
            xoa();
            khungGhi = NhipVe.demKhung;
        }
        for (int i = 0; i < so; i++) {
            if (AI[i] == ai) {
                return true;   // cùng actor vẽ hai lần trong khung: giữ lần đầu
            }
        }
        if (so >= TOI_DA) {
            return true;
        }
        int i = so++;
        AI[i] = ai;
        LOAI[i] = loai;
        TIEN[i] = tien;
        CHU[i] = chu;
        WX[i] = wx;
        WY[i] = wyDay;
        WCHAN[i] = wyChan;
        HP[i] = hp;
        CHON[i] = chon;
        NONG[i] = nong;
        DH[i] = null;
        if (loai != QUAI && ai instanceof game.model.Char) {
            game.model.Char c = (game.model.Char) ai;
            if (c.danhHieuTen != null && c.danhHieuTen.length() > 0) {
                DH[i] = c.danhHieuTen;
                DHH[i] = c.danhHieuHiem;
            }
        }
        return true;
    }

    private static void xoa() {
        for (int i = 0; i < so; i++) {
            AI[i] = null;
            CHU[i] = null;
            TIEN[i] = null;
            DH[i] = null;
        }
        so = 0;
    }

    /**
     * Char.paintName: tên người chơi; wyDay = đáy nhãn (toạ độ thế giới), wyChan = chân nhân vật. false = bản không
     * có chữ thật, tự vẽ kiểu cũ.
     */
    public static boolean nguoi(Object ai, String ten, int wx, int wyDay, int wyChan, int loai, boolean chon) {
        return ghi(ai, loai, null, ten, wx, wyDay, wyChan, -1, chon, false);
    }

    /** NhanPet: "tên pet · tên chủ"; wyDay = đáy nhãn (sát đỉnh hình pet), wyChan = chân pet. */
    public static boolean pet(Object ai, String chu, int wx, int wyDay, int wyChan) {
        return ghi(ai, PET, null, chu, wx, wyDay, wyChan, -1, false, false);
    }

    /** NhanQuai: "Lv N" (tien) + tên; hp phần nghìn (-1 không thanh). */
    public static boolean quai(Object ai, String tien, String ten, int wx, int wyDay, int wyChan, int hp, boolean chon, boolean nong) {
        return ghi(ai, QUAI, tien, ten, wx, wyDay, wyChan, hp, chon, nong);
    }

    private static final String[] LV = new String[128];

    /** "LvN" nhớ sẵn (không tạo chuỗi mỗi khung). */
    public static String lv(int n) {
        if (n < 0 || n >= LV.length) {
            return new StringBuffer("Lv").append(n).toString();
        }
        String r = LV[n];
        if (r == null) {
            r = new StringBuffer("Lv").append(n).toString();
            LV[n] = r;
        }
        return r;
    }

    private static int hpA = -1, hpB = -1;
    private static String hpS = "";

    /** "a/b" nhớ chuỗi cũ khi số không đổi (khung mục tiêu). */
    public static String hpMax(int a, int b) {
        if (a != hpA || b != hpB) {
            hpA = a;
            hpB = b;
            hpS = new StringBuffer().append(a).append('/').append(b).toString();
        }
        return hpS;
    }

    // ------------------------------------------------------------------ đổi toạ độ
    private static int zx = 1024, zy = 1024;

    /** Tỉ lệ thế giới -> giao diện (x1024) theo vùng nhìn gsw/gsh của camera. */
    static void tinhTiLe() {
        zx = GameScr.gsw > 0 ? (Canvas.w << 10) / GameScr.gsw : 1024;
        zy = GameScr.gsh > 0 ? (Canvas.h << 10) / GameScr.gsh : 1024;
        if (zx < 1024 || zx > 8192) {
            zx = 1024;
        }
        if (zy < 1024 || zy > 8192) {
            zy = zx;
        }
    }

    /** Thế giới -> giao diện (gọi {@link #tinhTiLe} trước). */
    static int sx(int wx) {
        return (wx - GameScr.cmx) * zx >> 10;
    }

    static int sy(int wy) {
        return (wy - GameScr.cmy) * zy >> 10;
    }

    /** Phóng của lớp thế giới (x1024). */
    static int z() {
        return zx;
    }

    // ------------------------------------------------------------------ chữ viền
    /** Chữ thật cỡ co có viền tối 1 px (4 hướng); không có ChuUI thì chữ ảnh ChuDe. */
    public static void chuVien(Graphics g, String s, int x, int y, float co, int dam, int mau, int vien, int canh) {
        ChuUI b = ChuUI.bo;
        if (b == null) {
            ChuDe.chu(g, s, x, y, ChuDe.PHU, mau, canh);
            return;
        }
        b.ve(g, s, x - 1, y, co, dam, vien, canh);
        b.ve(g, s, x + 1, y, co, dam, vien, canh);
        b.ve(g, s, x, y - 1, co, dam, vien, canh);
        b.ve(g, s, x, y + 1, co, dam, vien, canh);
        b.ve(g, s, x, y, co, dam, mau, canh);
    }

    public static int rong(String s, float co, int dam) {
        ChuUI b = ChuUI.bo;
        return b == null ? ChuDe.rong(s, ChuDe.PHU) : b.rong(s, co, dam);
    }

    public static int cao(float co) {
        ChuUI b = ChuUI.bo;
        return b == null ? ChuDe.cao(ChuDe.PHU) : b.cao(co);
    }

    /** Cỡ chữ nhãn thường / mục tiêu. */
    static float coNhan() {
        return ChuDe.dt() ? 7.0f : 6.2f;
    }

    /** Cỡ chữ dòng danh hiệu (nhỏ hơn tên). */
    static float coDanhHieu() {
        return ChuDe.dt() ? 6.2f : 5.6f;
    }

    /**
     * J2ME (không có chữ thật): danh hiệu vẽ trong lớp thế giới - viên màu độ hiếm canh giữa x, đáy yDay (Char.paintName).
     */
    public static void danhHieuJ2me(Graphics g, String ten, int hiem, int x, int yDay) {
        int w = ChuDe.rong(ten, ChuDe.NHAN) + 14;
        ManDanhHieu.vien(g, x - w / 2, yDay - 2 - ManDanhHieu.hVien(), ten, hiem, null, w + 20);
    }

    static float coChon() {
        return ChuDe.dt() ? 7.6f : 6.8f;
    }

    /** Cỡ chữ nhãn pet (nhỏ hơn tên người chơi). */
    static float coPet() {
        return ChuDe.dt() ? 6.2f : 5.6f;
    }

    /** Cỡ chữ của nhãn i. */
    static float co(int i, float co, float coC, float coP) {
        return CHON[i] ? coC : LOAI[i] == PET ? coP : co;
    }

    // ------------------------------------------------------------------ vẽ
    /** GameScr.paintFocusActorInfo (lớp giao diện): vẽ hàng đợi nhãn + số bay; xoá hàng đợi. */
    public static void ve(Graphics g) {
        try {
            Canvas.resetTrans(g);
            tinhTiLe();
            if (so > 0) {
                veNhan(g);
            }
        } catch (Throwable e) {
            // nhãn lỗi không làm hỏng khung
        }
        xoa();
        try {
            Canvas.resetTrans(g);
            SoBay.ve(g);
        } catch (Throwable e) {
            // số bay lỗi bỏ qua
        }
        Canvas.resetTrans(g);
    }

    private static int uuTien(int i) {
        if (CHON[i]) {
            return 0;
        }
        int l = LOAI[i];
        if (l == BAN_THAN) {
            return 1;
        }
        if (l == PET) {
            return 3;
        }
        if (l != QUAI) {
            return 2;
        }
        return NONG[i] ? 4 : 5;
    }

    /** Số nhãn của khung vẽ cuối (kiểm thử client_pc/test/KiemNhanPet đọc cùng X/Y/W/H/TT/LOAI). */
    static int soVe;

    private static void veNhan(Graphics g) {
        soVe = so;
        float co = coNhan(), coC = coChon(), coP = coPet();
        int h = cao(co), hC = cao(coC), hP = cao(coP);
        // hình nhân vật chính (nhãn quái không đè)
        int bx0 = 0, by0 = 0, bx1 = -1, by1 = -1;
        for (int i = 0; i < so; i++) {
            float c = co(i, co, coC, coP);
            int dam = CHON[i] ? 2 : LOAI[i] == PET ? 0 : 1;
            int w = rong(CHU[i], c, dam);
            int wt = 0;
            if (TIEN[i] != null) {
                wt = rong(TIEN[i], c, 2) + 3;
                w += wt;
            }
            WTIEN[i] = wt;
            WCHU[i] = w;
            int hh = CHON[i] ? hC : LOAI[i] == PET ? hP : h;
            int day = sy(WY[i]);
            if (HP[i] >= 0) {
                day -= 5;   // thanh HP dưới chữ
            }
            int cx = sx(WX[i]);
            W[i] = w + 4;
            H[i] = hh + (HP[i] >= 0 ? 5 : 0);
            X[i] = cx - W[i] / 2;
            Y[i] = day - hh;
            HDH[i] = 0;
            if (DH[i] != null) {
                // danh hiệu: dòng nhỏ hơn ngay trên tên, nhãn rộng theo dòng dài hơn (xếp chỗ tính cả hai dòng)
                float cd = coDanhHieu();
                HDH[i] = cao(cd) + 1;
                int wd = rong(DH[i], cd, 2) + 4;
                if (wd > W[i]) {
                    X[i] = cx - wd / 2;
                    W[i] = wd;
                }
                H[i] += HDH[i];
                Y[i] -= HDH[i];
            }
            TT[i] = X[i] + W[i] < 0 || X[i] > Canvas.w || Y[i] + H[i] < 0 || Y[i] > Canvas.h ? 0 : 1;
            if (LOAI[i] == BAN_THAN) {
                int r = 9 * zx >> 10;
                bx0 = cx - r;
                bx1 = cx + r;
                by0 = day;
                by1 = sy(WCHAN[i]);
            }
        }
        // thứ tự ưu tiên (đếm theo 6 bậc, không cấp phát)
        int n = 0;
        for (int p = 0; p <= 5; p++) {
            for (int i = 0; i < so; i++) {
                if (TT[i] != 0 && uuTien(i) == p) {
                    THU[n++] = i;
                }
            }
        }
        // xếp chỗ: so với các nhãn đã đặt (THU[0..k))
        for (int k = 0; k < n; k++) {
            int i = THU[k];
            int p = uuTien(i);
            if (p <= 1) {
                continue;   // mục tiêu / bản thân: luôn đúng chỗ
            }
            // pet / quái: không đè hình nhân vật chính
            boolean laQuai = LOAI[i] == QUAI || LOAI[i] == PET;
            if (!dung(i, k, laQuai, bx0, by0, bx1, by1)) {
                continue;
            }
            int y0 = Y[i];
            Y[i] = y0 - H[i] - 1;
            if (!dung(i, k, laQuai, bx0, by0, bx1, by1)) {
                continue;   // nhích lên một hàng
            }
            Y[i] = y0;
            if (LOAI[i] == PET || laQuai && p == 5) {
                TT[i] = 0;
                THU[k] = -1;
            } else {
                TT[i] = 2;
            }
        }
        // vẽ: ưu tiên thấp trước (người chơi trên quái, mục tiêu trên cùng)
        for (int k = n - 1; k >= 0; k--) {
            int i = THU[k];
            if (i >= 0 && TT[i] != 0) {
                ve1(g, i, co(i, co, coC, coP));
            }
        }
    }

    /** Nhãn i (vị trí X/Y hiện tại) chồng nhãn đã đặt THU[0..k) hoặc (quái) hình nhân vật chính. */
    private static boolean dung(int i, int k, boolean laQuai, int bx0, int by0, int bx1, int by1) {
        int x = X[i], y = Y[i], w = W[i], h = H[i];
        if (laQuai && bx1 > bx0 && x < bx1 && bx0 < x + w && y < by1 && by0 < y + h) {
            return true;
        }
        for (int q = 0; q < k; q++) {
            int j = THU[q];
            if (j < 0 || TT[j] == 0) {
                continue;
            }
            if (x < X[j] + W[j] && X[j] < x + w && y < Y[j] + H[j] && Y[j] < y + h) {
                return true;
            }
        }
        return false;
    }

    private static void ve1(Graphics g, int i, float co) {
        boolean chon = CHON[i];
        boolean mo = TT[i] == 2;
        int l = LOAI[i];
        int x = X[i] + 2, y = Y[i];
        int hh = cao(co);
        if (DH[i] != null) {
            // dòng danh hiệu (màu độ hiếm, token ChuDe) trên tên; tên canh giữa dưới
            chuVien(g, DH[i], X[i] + W[i] / 2, y, coDanhHieu(), 2, mo ? 0xAEB6C4 : ChuDe.nhanDanhHieu(DHH[i]), VIEN, 2);
            y += HDH[i];
            x = X[i] + (W[i] - WCHU[i]) / 2 + 2;
        }
        if (chon) {
            vien(g, x - 4, y, WCHU[i] + 8, hh);
        }
        int dam = chon ? 2 : l == PET ? 0 : 1;
        int mau;
        if (mo) {
            mau = 0xAEB6C4;
        } else if (l == PET) {
            mau = 0xC2CBDA;   // dịu hơn tên người chơi (0xEEF3FF)
        } else if (l == BAN_THAN) {
            mau = 0xFFF1B8;
        } else if (l == TRUM) {
            mau = 0xFFB27A;
        } else if (l == KHAC_PHE) {
            mau = 0xFFA898;
        } else if (l == QUAI) {
            mau = chon || NONG[i] ? 0xFFFFFF : 0xE6EAF2;
        } else {
            mau = chon ? 0xFFFFFF : 0xEEF3FF;
        }
        if (TIEN[i] != null) {
            chuVien(g, TIEN[i], x, y, co, 2, mo ? 0xB8B090 : 0xFFD54A, VIEN, 0);
            x += WTIEN[i];
        }
        chuVien(g, CHU[i], x, y, co, dam, mau, VIEN, 0);
        if (HP[i] >= 0) {
            int w = W[i] - 4 < 28 ? 28 : W[i] - 4;
            int bx = X[i] + W[i] / 2 - w / 2;
            g.setColor(VIEN);
            g.fillRect(bx - 1, y + hh, w + 2, 5);
            GiaoDien.thanh(g, bx, y + hh + 1, w, 3, HP[i], 1000, 0);
        }
    }

    // ------------------------------------------------------------------ viên tối mờ (ảnh nhớ sẵn)
    private static Image anhVien;
    private static int caoVien = -1;
    private static final int RONG_ANH = 64;

    /** Viên tối mờ bo tròn sau nhãn mục tiêu: ảnh RONG_ANH x h dựng một lần, vẽ 2 đầu + thân lặp. */
    static void vien(Graphics g, int x, int y, int w, int h) {
        if (h < 4 || w < h) {
            return;
        }
        if (anhVien == null || caoVien != h) {
            anhVien = dungVien(RONG_ANH, h, 0xA8141B2C);
            caoVien = h;
        }
        Image a = anhVien;
        if (a == null) {
            return;
        }
        int r = h / 2;
        g.drawRegion(a, 0, 0, r, h, 0, x, y, Graphics.TOP | Graphics.LEFT);
        int giua = RONG_ANH - 2 * r;
        for (int xx = x + r; xx < x + w - r; xx += giua) {
            int cw = x + w - r - xx;
            if (cw > giua) {
                cw = giua;
            }
            g.drawRegion(a, r, 0, cw, h, 0, xx, y, Graphics.TOP | Graphics.LEFT);
        }
        g.drawRegion(a, RONG_ANH - r, 0, r, h, 0, x + w - r, y, Graphics.TOP | Graphics.LEFT);
    }

    private static Image dungVien(int w, int h, int argb) {
        try {
            int[] p = new int[w * h];
            int a0 = argb >>> 24, rgb = argb & 0xFFFFFF;
            // toạ độ x2 (tâm điểm ảnh = 2x+1): bán kính h/2 -> h
            int r2 = h * h, trong = (h - 2) * (h - 2);
            for (int yy = 0; yy < h; yy++) {
                for (int xx = 0; xx < w; xx++) {
                    int a = a0;
                    int dx = 0;
                    if (2 * xx + 1 < h) {
                        dx = 2 * xx + 1 - h;
                    } else if (2 * xx + 1 > 2 * w - h) {
                        dx = 2 * xx + 1 - (2 * w - h);
                    }
                    if (dx != 0) {
                        int dy = 2 * yy + 1 - h;
                        int d = dx * dx + dy * dy;
                        if (d > r2) {
                            a = 0;
                        } else if (d > trong) {
                            a = a0 / 2;   // mép khử răng cưa thô
                        }
                    }
                    p[yy * w + xx] = a << 24 | rgb;
                }
            }
            return Image.createRGBImage(p, w, h, true);
        } catch (Throwable e) {
            return null;
        }
    }
}
