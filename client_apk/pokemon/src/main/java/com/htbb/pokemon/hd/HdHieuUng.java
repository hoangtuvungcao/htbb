package com.htbb.pokemon.hd;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import java.util.HashMap;
import javax.microedition.lcdui.Graphics;

/**
 * Hiệu ứng HD của bản APK (bật / tắt: "Hiệu ứng cao / thấp" trong bảng Chỉnh nút của HUD). Chỉ đổi CÁCH VẼ,
 * không đổi sự kiện: các hạt / vòng / tia của game.pokemon.HieuUngDanh (cùng mảng hạt, cùng tuổi, cùng màu hệ)
 * vẽ lại phát sáng cộng màu (additive), vệt đuôi, vòng va chạm mượt; số sát thương (SoBay) vẽ chữ vector có
 * nhún lò xo; lên cấp / tiến hoá có tia sáng toàn màn; đêm (SinhThai.veDem) chuyển màu mượt + đom đóm sáng;
 * thời tiết theo vùng (tuyết, cát, sương hang, nắng rừng) và chuyển cảnh mờ dần khi đổi màn / đổi map.
 */
public final class HdHieuUng {
    private HdHieuUng() {
    }

    public static boolean batCao() {
        return Hud.BAT && Hud.hieuUngCao;
    }

    private static final Paint P = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint PC = new Paint(Paint.ANTI_ALIAS_FLAG);   // cộng màu
    private static final Paint PV = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint PT = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private static final Paint PTV = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private static final RectF R = new RectF();
    private static final Path PATH = new Path();
    private static Bitmap sang;
    private static final HashMap<Integer, LightingColorFilter> LOC = new HashMap<Integer, LightingColorFilter>();

    static {
        PC.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
        PC.setFilterBitmap(true);
        PV.setStyle(Paint.Style.STROKE);
        PV.setStrokeCap(Paint.Cap.ROUND);
        PV.setStrokeJoin(Paint.Join.ROUND);
        PTV.setStyle(Paint.Style.STROKE);
        PTV.setStrokeJoin(Paint.Join.ROUND);
    }

    /** Ảnh quầng sáng tròn (trắng giữa -> trong suốt) - tô màu bằng ColorFilter, vẽ cộng màu. */
    private static Bitmap anhSang() {
        if (sang == null) {
            int n = 48;
            sang = Bitmap.createBitmap(n, n, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(sang);
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setShader(new RadialGradient(n / 2f, n / 2f, n / 2f, new int[]{0xFFFFFFFF, 0x80FFFFFF, 0x20FFFFFF, 0x00FFFFFF},
                    new float[]{0f, 0.25f, 0.6f, 1f}, Shader.TileMode.CLAMP));
            c.drawCircle(n / 2f, n / 2f, n / 2f, p);
        }
        return sang;
    }

    private static LightingColorFilter loc(int rgb) {
        Integer k = rgb & 0xFFFFFF;
        LightingColorFilter f = LOC.get(k);
        if (f == null) {
            if (LOC.size() > 64) LOC.clear();
            f = new LightingColorFilter(k, 0);
            LOC.put(k, f);
        }
        return f;
    }

    /** Quầng sáng cộng màu tâm (x, y) bán kính r, độ mạnh a (0..1). */
    static void quang(Canvas c, float x, float y, float r, int rgb, float a) {
        if (a <= 0.01f || r <= 0.3f) return;
        PC.setColorFilter(loc(rgb));
        PC.setAlpha((int) (Math.min(1f, a) * 255));
        R.set(x - r, y - r, x + r, y + r);
        c.drawBitmap(anhSang(), null, R, PC);
    }

    // ------------------------------------------------------------------ hạt chiến đấu (HieuUngDanh.veHat)
    // loại hạt (khớp HieuUngDanh.K_*): 0 chấm, 1 tia, 2 vòng, 3 sao, 4 khói, 5 sét, 6 lá
    public static boolean veHat(Graphics g, int[] k, int[] hx, int[] hy, int[] vx, int[] vy, int[] tuoi, int[] max,
                                int[] mau, int[] mau2, int[] co, int n) {
        if (!batCao() || !g.laHd()) return false;
        Canvas c = g.androidCanvas();
        float ox = g.getTranslateX(), oy = g.getTranslateY();
        for (int i = 0; i < n; i++) {
            float x = hx[i] / 16f + ox, y = hy[i] / 16f + oy;
            int t = tuoi[i], T = Math.max(1, max[i]);
            float f = t / (float) T;                 // 0 -> 1
            float song = 1 - f;
            int m = f < 0.5f ? mau[i] : mau2[i];
            int s = co[i];
            switch (k[i]) {
                case 0: {
                    float r = Math.max(0.8f, s * (f > 0.66f ? 0.7f : 1f));
                    quang(c, x, y, r * 3.2f, m, song * 0.9f);
                    P.setColor(0xFF000000 | HdVe.pha(0xFF000000 | m, 0xFFFFFFFF, 0.5f));
                    P.setAlpha((int) (255 * Math.min(1f, song * 1.4f)));
                    c.drawCircle(x, y, r * 0.55f, P);
                    break;
                }
                case 1: {
                    float dx = vx[i] / 8f, dy = vy[i] / 8f;
                    PV.setStrokeWidth(Math.max(0.8f, s * 0.8f));
                    PV.setColor(0xFF000000 | m);
                    PV.setAlpha((int) (230 * song));
                    c.drawLine(x, y, x - dx * 1.6f, y - dy * 1.6f, PV);
                    PV.setColor(0xFFFFFFFF);
                    PV.setStrokeWidth(Math.max(0.5f, s * 0.35f));
                    c.drawLine(x, y, x - dx * 0.7f, y - dy * 0.7f, PV);
                    quang(c, x, y, s * 2.6f, m, song * 0.7f);
                    break;
                }
                case 2: {
                    float r = 4 + t * (3 + s);
                    PV.setColor(0xFF000000 | m);
                    PV.setAlpha((int) (220 * song));
                    PV.setStrokeWidth(Math.max(0.8f, (s + 1) * 0.9f * song + 0.4f));
                    R.set(x - r, y - r * 2 / 3f, x + r, y + r * 2 / 3f);
                    c.drawOval(R, PV);
                    PV.setColor(0xFFFFFFFF);
                    PV.setAlpha((int) (160 * song));
                    PV.setStrokeWidth(0.6f);
                    R.set(x - r * 0.82f, y - r * 0.55f, x + r * 0.82f, y + r * 0.55f);
                    c.drawOval(R, PV);
                    if (t < 3) quang(c, x, y, r * 1.6f, m, 0.8f - t * 0.25f);
                    break;
                }
                case 3: {
                    float r = s * song + 1.2f;
                    quang(c, x, y, r * 2.4f, m, song);
                    PATH.reset();
                    PATH.moveTo(x, y - r * 1.6f);
                    PATH.lineTo(x + r * 0.35f, y - r * 0.35f);
                    PATH.lineTo(x + r * 1.6f, y);
                    PATH.lineTo(x + r * 0.35f, y + r * 0.35f);
                    PATH.lineTo(x, y + r * 1.6f);
                    PATH.lineTo(x - r * 0.35f, y + r * 0.35f);
                    PATH.lineTo(x - r * 1.6f, y);
                    PATH.lineTo(x - r * 0.35f, y - r * 0.35f);
                    PATH.close();
                    P.setColor(0xFFFFFFFF);
                    P.setAlpha((int) (255 * song));
                    c.drawPath(PATH, P);
                    break;
                }
                case 4: {
                    float r = s + t / 3f;
                    if (f > 0.66f) r = s * (1 - f) * 3 + 1;
                    P.setColor(0xFF000000 | m);
                    P.setAlpha((int) (150 * song));
                    c.drawCircle(x, y, r * 1.15f, P);
                    break;
                }
                case 5: {
                    float dx = vx[i] / 16f, dy = vy[i] / 16f;
                    float x0 = (hx[i] - vx[i] * t) / 16f + ox, y0 = (hy[i] - vy[i] * t) / 16f + oy;
                    float mx = (x0 + x) / 2 + ((t & 1) == 0 ? dy / 2 + 2 : -dy / 2 - 2);
                    float my = (y0 + y) / 2 + ((t & 1) == 0 ? -dx / 2 : dx / 2);
                    PATH.reset();
                    PATH.moveTo(x0, y0);
                    PATH.lineTo(mx, my);
                    PATH.lineTo(x, y);
                    PV.setColor(0xFF000000 | m);
                    PV.setAlpha((int) (200 * song));
                    PV.setStrokeWidth(2.2f);
                    c.drawPath(PATH, PV);
                    PV.setColor(0xFFFFFFFF);
                    PV.setAlpha((int) (255 * song));
                    PV.setStrokeWidth(0.8f);
                    c.drawPath(PATH, PV);
                    quang(c, mx, my, 6, m, song * 0.8f);
                    break;
                }
                case 6: {
                    int sv = c.save();
                    c.rotate(t * 24f + i * 37, x, y);
                    PATH.reset();
                    PATH.moveTo(x - s, y);
                    PATH.quadTo(x, y - s * 0.9f, x + s, y);
                    PATH.quadTo(x, y + s * 0.9f, x - s, y);
                    PATH.close();
                    P.setColor(0xFF000000 | m);
                    P.setAlpha((int) (255 * Math.min(1f, song * 1.5f)));
                    c.drawPath(PATH, P);
                    c.restoreToCount(sv);
                    break;
                }
                default:
                    return false;
            }
        }
        return true;
    }

    // ------------------------------------------------------------------ số sát thương có nhún lò xo
    public static boolean veSo(Graphics g, String s, int mau, int coSo, int x, int y, boolean laXu, int t, int tMax) {
        if (!batCao() || !g.laHd() || s == null) return false;
        Canvas c = g.androidCanvas();
        float ox = g.getTranslateX(), oy = g.getTranslateY();
        // lò xo: bật to rồi dao động tắt dần quanh 1
        float tt = t / 2.2f;
        float k = 1f + 0.85f * (float) (Math.exp(-tt * 1.1) * Math.cos(tt * 2.4));
        float co = (6.5f + coSo * 2.2f) * Math.max(0.3f, k);
        PT.setTypeface(HdChu.typeface(2));
        PT.setTextSize(co);
        PT.setTextAlign(Paint.Align.CENTER);
        PTV.setTypeface(PT.getTypeface());
        PTV.setTextSize(co);
        PTV.setTextAlign(Paint.Align.CENTER);
        PTV.setStrokeWidth(co * 0.22f);
        int con = tMax - t;
        int a = con < 6 ? Math.max(0, con * 42) : 255;
        float cx = x + ox, cy = y + oy;
        if (coSo >= 3) quang(c, cx, cy - co * 0.35f, co * 1.2f, mau, a / 255f * 0.55f);
        PTV.setColor(0xFF101420);
        PTV.setAlpha(a);
        c.drawText(s, cx, cy, PTV);
        PT.setShader(HdVe.doc(0xFF000000 | HdVe.pha(0xFF000000 | mau, 0xFFFFFFFF, 0.55f), 0xFF000000 | mau, cy - co * 0.75f, cy));
        PT.setAlpha(a);
        c.drawText(s, cx, cy, PT);
        PT.setShader(null);
        return true;
    }

    // ------------------------------------------------------------------ đêm mượt
    private static float aDem = -1;
    private static Shader demShader;
    private static int demA, demW, demH;
    private static long lucDem;

    /** SinhThai.veDem: a = alpha đích của jar (0..255). */
    public static void veDem(Graphics g, int aDich) {
        long now = System.currentTimeMillis();
        float dt = lucDem == 0 ? 0 : Math.min(0.2f, (now - lucDem) / 1000f);
        lucDem = now;
        if (aDem < 0) aDem = aDich;
        aDem += (aDich - aDem) * Math.min(1f, dt * 1.5f);
        if (aDem < 1) return;
        Canvas c = g.androidCanvas();
        HdZoom.boTam(g);
        int W = game.render.Canvas.w, H = game.render.Canvas.h;
        int a = (int) aDem;
        // tối hơn ở rìa (vignette), sáng hơn quanh nhân vật - shader dùng lại khi alpha / cỡ không đổi
        if (demShader == null || a != demA || W != demW || H != demH) {
            demShader = new RadialGradient(W / 2f, H * 0.55f, Math.max(W, H) * 0.75f,
                    new int[]{(a * 7 / 10) << 24 | 0x0B1440, a << 24 | 0x0B1440, Math.min(255, a * 13 / 10) << 24 | 0x060A24},
                    new float[]{0f, 0.55f, 1f}, Shader.TileMode.CLAMP);
            demA = a;
            demW = W;
            demH = H;
        }
        P.setShader(demShader);
        c.drawRect(-g.getTranslateX() - 4, -g.getTranslateY() - 4, -g.getTranslateX() + W + 4, -g.getTranslateY() + H + 4, P);
        P.setShader(null);
        if (a >= 0x50 && batCao()) {
            float ox = -g.getTranslateX(), oy = -g.getTranslateY();
            for (int i = 0; i < 12; i++) {
                double ph = (now / 1000.0 + i * 1.7) % 6.0;
                float al = (float) Math.max(0, Math.sin(ph / 6.0 * Math.PI));
                float x = (float) ((i * 97 + now / 90.0 + Math.sin(now / 900.0 + i) * 20) % W);
                float y = (float) (30 + (i * 53 + Math.cos(now / 1300.0 + i) * 25 + now / 160.0) % (H - 60));
                quang(c, ox + x, oy + y, 5f, 0xE8F59A, al * 0.9f);
            }
        }
        HdZoom.khoiPhuc(g);
    }

    // ------------------------------------------------------------------ thời tiết theo vùng
    private static int vungMap = -999, kieuTroi;

    /** Gọi cuối phần thế giới (HdZoom.ket): vẽ thời tiết theo kiểu nhạc / vùng của map (AmThanh). */
    public static void veThoiTiet(Graphics g) {
        if (!batCao() || !g.laHd()) return;
        game.render.screen.GameScr gs = game.render.Canvas.gameScr;
        if (gs == null || game.model.Tilemap.isOfflineMap) return;
        if (gs.map != vungMap) {
            vungMap = gs.map;
            int nhac;
            try {
                nhac = game.pokemon.AmThanh.nhacChoMap(gs.map, game.render.screen.GameScr.nameMap);
            } catch (Throwable e) {
                nhac = -1;
            }
            kieuTroi = nhac == game.pokemon.AmThanh.N_TUYET ? 1 : nhac == game.pokemon.AmThanh.N_SA_MAC ? 2
                    : nhac == game.pokemon.AmThanh.N_HANG ? 3 : nhac == game.pokemon.AmThanh.N_RUNG ? 4 : 0;
        }
        if (kieuTroi == 0) return;
        Canvas c = g.androidCanvas();
        HdZoom.boTam(g);
        int W = game.render.Canvas.w, H = game.render.Canvas.h;
        float ox = -g.getTranslateX(), oy = -g.getTranslateY();
        long now = System.currentTimeMillis();
        double ts = now / 1000.0;
        switch (kieuTroi) {
            case 1: // tuyết rơi 2 lớp
                for (int i = 0; i < 70; i++) {
                    float sp = 14 + (i % 5) * 7;
                    float x = (float) ((i * 73.3 + Math.sin(ts * 0.8 + i) * 12 + ts * 6) % W);
                    float y = (float) ((i * 41.7 + ts * sp) % (H + 10)) - 5;
                    float r = 0.7f + (i % 4) * 0.35f;
                    P.setColor(i % 3 == 0 ? 0xE0FFFFFF : 0xA0E8F0FF);
                    c.drawCircle(ox + (x < 0 ? x + W : x), oy + y, r, P);
                }
                break;
            case 2: { // cát: hạt bay ngang + nắng nóng
                P.setShader(HdVe.doc(0x30FFC870, 0x00FFC870, oy, oy + H * 0.5f));
                c.drawRect(ox, oy, ox + W, oy + H * 0.5f, P);
                P.setShader(null);
                for (int i = 0; i < 40; i++) {
                    float x = (float) ((i * 91.1 + ts * (60 + i % 7 * 12)) % (W + 20)) - 10;
                    float y = (float) ((i * 37.3 + Math.sin(ts * 2 + i) * 6) % H);
                    PV.setColor(0x70E8C890);
                    PV.setStrokeWidth(0.8f);
                    c.drawLine(ox + x, oy + y, ox + x - 6, oy + y + 0.6f, PV);
                }
                break;
            }
            case 3: { // hang: sương mờ trôi + bụi lơ lửng
                for (int i = 0; i < 5; i++) {
                    float x = (float) ((i * 211 + ts * (6 + i * 2)) % (W + 240)) - 120;
                    float y = H * (0.2f + 0.15f * i) + (float) Math.sin(ts * 0.4 + i) * 10;
                    quang(c, ox + x, oy + y, 110, 0x384050, 0.35f);
                }
                for (int i = 0; i < 24; i++) {
                    float x = (float) ((i * 67.7 + Math.sin(ts * 0.5 + i) * 15) % W);
                    float y = (float) ((i * 29.3 + ts * 3) % H);
                    P.setColor(0x60D8D0C0);
                    c.drawCircle(ox + x, oy + y, 0.6f, P);
                }
                break;
            }
            default: { // rừng: tia nắng chéo nhẹ
                for (int i = 0; i < 3; i++) {
                    float x = W * (0.2f + 0.3f * i) + (float) Math.sin(ts * 0.15 + i) * 30;
                    float a = 0.10f + 0.05f * (float) Math.sin(ts * 0.7 + i * 2);
                    PATH.reset();
                    PATH.moveTo(ox + x, oy);
                    PATH.lineTo(ox + x + 46, oy);
                    PATH.lineTo(ox + x - 60, oy + H);
                    PATH.lineTo(ox + x - 130, oy + H);
                    PATH.close();
                    P.setShader(HdVe.doc((int) (a * 255) << 24 | 0xFFF2C0, 0x00FFF2C0, oy, oy + H));
                    c.drawPath(PATH, P);
                    P.setShader(null);
                }
                break;
            }
        }
        HdZoom.khoiPhuc(g);
    }

    // ------------------------------------------------------------------ lên cấp / tiến hoá / chuyển cảnh (lớp phủ)
    private static long lucLenCap, lucTienHoa, lucChuyen;
    private static Object manTruoc;
    private static int mapTruoc = -999;

    public static void lenCap(game.model.Char c) {
        if (c == null || game.render.Canvas.gameScr == null || c != game.render.Canvas.gameScr.mainChar) return;
        lucLenCap = System.currentTimeMillis();
    }

    public static void tienHoa(game.model.Char c) {
        if (c == null || game.render.Canvas.gameScr == null || c != game.render.Canvas.gameScr.mainChar) return;
        lucTienHoa = System.currentTimeMillis();
    }

    public static boolean dangDong() {
        long now = System.currentTimeMillis();
        return now - lucLenCap < 1800 || now - lucTienHoa < 2600 || now - lucChuyen < 420;
    }

    /** Lớp phủ (Hud.ve, 60 hình/giây). */
    static void veLopPhu(Canvas c, int W, int H) {
        long now = System.currentTimeMillis();
        Object man = game.render.Canvas.currentScreen;
        game.render.screen.GameScr gs = game.render.Canvas.gameScr;
        int map = gs == null ? -1 : gs.map;
        if (man != manTruoc || map != mapTruoc) {
            if (manTruoc != null || mapTruoc != -999) lucChuyen = now;
            manTruoc = man;
            mapTruoc = map;
        }
        if (!batCao()) return;
        long dc = now - lucChuyen;
        if (dc < 420) {
            float a = 1 - dc / 420f;
            P.setShader(null);
            P.setColor(((int) (a * a * 200)) << 24 | 0x0A142C);
            c.drawRect(0, 0, W, H, P);
        }
        if (gs == null || man != gs) return;
        float px = HdZoom.manX(gs.mainChar.x), py = HdZoom.manY(gs.mainChar.y - 20);
        long dl = now - lucLenCap;
        if (dl < 1800) veNo(c, W, H, px, py, dl / 1800f, 0xFFD23A, "LÊN CẤP!");
        long dt = now - lucTienHoa;
        if (dt < 2600) {
            float f = dt / 2600f;
            if (f < 0.25f) {
                P.setColor(((int) ((1 - f / 0.25f) * 230)) << 24 | 0xFFFFFF);
                c.drawRect(0, 0, W, H, P);
            }
            veNo(c, W, H, px, py, f, 0x9FE3FF, "TIẾN HOÁ!");
        }
    }

    private static void veNo(Canvas c, int W, int H, float px, float py, float f, int mau, String chu) {
        float a = f < 0.75f ? 1f : (1 - f) / 0.25f;
        // tia sáng quay
        int sv = c.save();
        c.rotate(f * 90, px, py);
        float dai = Math.max(W, H) * 0.9f;
        for (int i = 0; i < 12; i++) {
            c.save();
            c.rotate(i * 30, px, py);
            PATH.reset();
            PATH.moveTo(px, py);
            PATH.lineTo(px - 10, py - dai);
            PATH.lineTo(px + 10, py - dai);
            PATH.close();
            P.setShader(HdVe.doc(((int) (a * 120)) << 24 | (mau & 0xFFFFFF), 0x00FFFFFF & mau, py, py - dai));
            c.drawPath(PATH, P);
            P.setShader(null);
            c.restore();
        }
        c.restoreToCount(sv);
        // vòng nở
        for (int k = 0; k < 2; k++) {
            float ff = Math.min(1f, f * 1.6f - k * 0.2f);
            if (ff <= 0) continue;
            PV.setColor(0xFF000000 | mau);
            PV.setAlpha((int) (255 * (1 - ff) * a));
            PV.setStrokeWidth(3f * (1 - ff) + 0.6f);
            c.drawCircle(px, py, 12 + ff * 90, PV);
        }
        quang(c, px, py, 60, mau, a * 0.6f);
        // chữ nhún lò xo giữa trên màn
        float tt = f * 12;
        float k = 1f + 0.6f * (float) (Math.exp(-tt * 0.6) * Math.cos(tt * 1.8));
        float co = 26 * Math.max(0.2f, k);
        PT.setTypeface(HdChu.typeface(2));
        PT.setTextSize(co);
        PT.setTextAlign(Paint.Align.CENTER);
        PTV.setTypeface(PT.getTypeface());
        PTV.setTextSize(co);
        PTV.setTextAlign(Paint.Align.CENTER);
        PTV.setStrokeWidth(co * 0.16f);
        PTV.setColor(0xFF1E2A44);
        PTV.setAlpha((int) (255 * a));
        float y = H * 0.3f;
        c.drawText(chu, W / 2f, y, PTV);
        PT.setShader(HdVe.doc(0xFFFFF4B0, 0xFF000000 | mau, y - co * 0.8f, y));
        PT.setAlpha((int) (255 * a));
        c.drawText(chu, W / 2f, y, PT);
        PT.setShader(null);
    }
}
