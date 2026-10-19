package com.htbb.pokemon.hd;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;
import game.model.Char;
import game.model.ChatInfo;
import game.model.MainChar;
import game.model.QuickSlot;
import game.render.screen.GameScr;
import game.render.screen.HdCau;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.NenAndroid;

/**
 * HUD cảm ứng HD của bản APK (chỉ Android; bản jar giữ HUD cũ). Vẽ ở độ phân giải thật trên lớp phủ của
 * NenAndroid (vẽ lại 60 hình/giây khi có hoạt hình, độc lập nhịp 25 hình/giây của game), đa chạm:
 *
 * - Cần điều khiển nổi bên trái (hiện đúng chỗ ngón cái chạm) -> phím hướng J2ME (-1..-4) như bàn phím.
 * - Cụm chiến đấu phải dưới: nút đánh lớn = ô phím 5, 4 ô chiêu xếp vòng cung = phím 1/3/7/9 (màu hệ,
 *   vòng hồi chiêu, thiếu PP), đổi bộ ô (phím 0), đổi mục tiêu (phím mềm phải), 2 ô thuốc nhanh HP / PP
 *   (GameScr.doUsePotion như ô thuốc của jar).
 * - Thẻ trạng thái trái trên (chân dung, Lv, thanh HP / PP / EXP dạng viên), bản đồ nhỏ phải trên (vẫn là
 *   bản đồ nhỏ của jar: chạm = tìm đường như jar), hàng nút: Menu (phím mềm trái), Tự động (móc
 *   HdCau.moTuDong), Chat, Thêm (menu phím #), Chỉnh nút (sửa bố cục, lưu theo máy).
 * - Khung chat thu gọn, tin máy chủ dạng toast; không che giữa màn; tránh tai thỏ (NenAndroid.anToan*).
 *
 * Mọi thao tác đi qua đúng phím / hàm của jar -> cùng logic, cùng giao thức. Cú chạm không trúng nút chuyển
 * nguyên cho game (chạm đi, chạm NPC, bản đồ nhỏ...).
 */
public final class Hud implements NenAndroid.LopPhu {
    public static final Hud I = new Hud();
    /** HUD mới bật (bản APK). false = HUD jar. */
    public static boolean BAT = true;

    public static boolean bat() {
        return BAT;
    }

    // ------------------------------------------------------------------ cài đặt (lưu theo máy)
    /** nhóm: 0 cần điều khiển, 1 cụm chiến đấu, 2 ô thuốc, 3 khung chat */
    static final int NHOM = 4;
    static final float[] GX = new float[NHOM];
    static final float[] GY = new float[NHOM];
    static final float[] GS = {1f, 1f, 1f, 1f};
    /** Hiệu ứng cao (hạt, hào quang, chuyển cảnh...) / thấp. */
    public static boolean hieuUngCao = true;
    public static boolean rung = true;
    static boolean chatMo = true;
    private static SharedPreferences pref;

    static void nap() {
        if (pref != null) return;
        try {
            pref = NenAndroid.ungDung.getSharedPreferences("htbb_hud", 0);
            for (int i = 0; i < NHOM; i++) {
                GX[i] = pref.getFloat("g" + i + "x", 0);
                GY[i] = pref.getFloat("g" + i + "y", 0);
                GS[i] = pref.getFloat("g" + i + "s", 1f);
            }
            hieuUngCao = pref.getBoolean("hieuUngCao", true);
            rung = pref.getBoolean("rung", true);
            chatMo = pref.getBoolean("chatMo", true);
        } catch (Throwable e) {
            // dùng mặc định
        }
    }

    static void luu() {
        if (pref == null) return;
        SharedPreferences.Editor e = pref.edit();
        for (int i = 0; i < NHOM; i++) {
            e.putFloat("g" + i + "x", GX[i]);
            e.putFloat("g" + i + "y", GY[i]);
            e.putFloat("g" + i + "s", GS[i]);
        }
        e.putBoolean("hieuUngCao", hieuUngCao);
        e.putBoolean("rung", rung);
        e.remove("zoomTheGioi"); // camera cố định (HdZoom): bỏ tuỳ chọn Phóng cũ
        e.putBoolean("chatMo", chatMo);
        e.apply();
    }

    /** Ô "Cài đặt" của Menu (bản APK): bảng chỉnh âm thanh / hiệu ứng / rung / bố cục nút HUD. */
    public static void moCaiDat() {
        game.render.Canvas.gameScr.switchToMe();
        chinhSua = true;
    }

    public static void khoiTao() {
        nap();
        ChuApk.bat();   // 0.9.5: chữ giao diện + biểu tượng vector (game.pokemon.ChuDe / BieuTuong)
        NenAndroid.ganLopPhu(I);
        // 0.9.3: phép phóng cho giao diện mới (Pokémon to ở màn Nhân vật, icon đồ trong ô lớn)
        game.pokemon.PhongVe.bo = new game.pokemon.PhongVe() {
            public void bat(Graphics g, float z) {
                // giữ vùng cắt của nơi gọi (toạ độ màn) -> quy về toạ độ đã phóng
                int ax = g.getClipX() + g.getTranslateX(), ay = g.getClipY() + g.getTranslateY();
                int aw = g.getClipWidth(), ah = g.getClipHeight();
                game.render.Canvas.resetTrans(g);
                g.datBienDoi(z, 0, 0);
                g.setClip((int) (ax / z), (int) (ay / z), (int) Math.ceil(aw / z) + 1, (int) Math.ceil(ah / z) + 1);
            }

            public void tat(Graphics g) {
                g.datBienDoi(1f, 0, 0);
                game.render.Canvas.resetTrans(g);
            }
        };
    }

    // ------------------------------------------------------------------ bố cục (px logic)
    static int W, H, L, T, R, B;
    static int mmX = -1, mmY, mmW, mmH;

    static void tinhKhung() {
        W = NenAndroid.rong;
        H = NenAndroid.cao;
        L = NenAndroid.anToanTrai + 6;
        // bảng chung (game.pokemon.Bang) tránh tai thỏ / bo góc như HUD
        game.pokemon.Bang.anTrai = NenAndroid.anToanTrai;
        game.pokemon.Bang.anTren = NenAndroid.anToanTren;
        game.pokemon.Bang.anPhai = NenAndroid.anToanPhai;
        game.pokemon.Bang.anDuoi = NenAndroid.anToanDuoi;
        T = NenAndroid.anToanTren + 4;
        R = W - NenAndroid.anToanPhai - 6;
        B = H - NenAndroid.anToanDuoi - 6;
        HudBoTri.dpLogic = dpLogic();
        HudBoTri.L = L;
        HudBoTri.T = T;
        HudBoTri.R = R;
        HudBoTri.B = B;
    }

    /** Thẻ bản đồ nhỏ góc phải trên: dưới hàng nút, có đầu thẻ (tên map / vùng / toạ độ) - không chồng nhau. */
    static final int DAU_MM = 22;
    /** đỉnh thẻ bản đồ nhỏ (dưới hàng nút trên, tính theo cỡ nút >= 44 dp) */
    static int yTheMM() {
        return Math.max(31, HudBoTri.yTheMiniMap());
    }

    /** Vị trí + cỡ bản đồ nhỏ (GameScr.setPosMiniMap): góc phải, dưới hàng nút + đầu thẻ. */
    public static int[] oMiniMap(int wAnh, int hAnh) {
        tinhKhung();
        // vùng bản đồ cố định (thẻ HUD tự phóng vừa ảnh bản đồ); jar dùng cùng ô cho icon buff / kỹ năng bang
        int w = W >= 700 ? 124 : 108;
        int h = H >= 400 ? 76 : 62;
        mmW = w;
        mmH = h;
        mmX = R - 4 - w;
        mmY = T + yTheMM() + DAU_MM;
        return new int[]{mmX, mmY, w, h};
    }

    static int mmTrai() {
        return mmX >= 0 && mmX < R ? mmX : R - 112;
    }

    /** Nền kính bản đồ nhỏ (thay 3 viền màu của jar). g đang dịch tới góc bản đồ. */
    public static void nenMiniMap(Graphics g, int w, int h) {
        Canvas c = g.androidCanvas();
        float l = g.getTranslateX();
        float t = g.getTranslateY();
        // thẻ bản đồ nhỏ HD (Hud.veTheMiniMap) vẽ cả nền + bản đồ ở lớp phủ
    }

    /** Dời khung mục tiêu (paintFocusActorInfo, vốn ở góc phải trên) vào giữa thẻ trạng thái và hàng nút. */
    public static int dxMucTieu(int wKhung) {
        tinhKhung();
        int trai = L + RONG_THE + 6;
        int phai = (int) (xNutTren(SO_NUT_TREN - 1) - HudBoTri.rTren() - 6);
        int x0 = (trai + phai) / 2 - wKhung / 2;
        if (x0 + wKhung > phai) x0 = phai - wKhung;
        if (x0 < trai) x0 = trai;
        return x0 - (game.render.Canvas.w - wKhung);
    }

    public static int yTren() {
        return T;
    }

    public static int yToastTren() {
        return T + 42;
    }

    /** Thanh trạng thái auto của mod (AutoGuard...) đặt dưới thẻ trạng thái. */
    public static int yTrangThai(int macDinh) {
        return T + CAO_THE + 6 + soVienTin * 15;
    }

    /** Màn chơi đang dùng HUD mới: ẩn chữ phím mềm / nút bàn phím ảo của jar. */
    public static boolean anPhimMem() {
        try {
            return tinhHien();
        } catch (Throwable e) {
            return false;
        }
    }

    // ------------------------------------------------------------------ nút
    static final int N_DANH = 0, N_C0 = 1, N_C1 = 2, N_C3 = 3, N_C4 = 4, N_DOI_BO = 5, N_MUC_TIEU = 6,
            N_HP = 7, N_PP = 8, N_MENU = 9, N_TU_DONG = 10, N_CHAT = 11, N_THEM = 12, N_CHINH = 13,
            N_CHAT_BONG = 14, N_MINIMAP = 15, SO_NUT = 16;
    static final int SO_NUT_TREN = 5;
    static final float R_NUT_TREN = 12.5f;
    static final int RONG_THE = 176, CAO_THE = 47;
    static final float[] NX = new float[SO_NUT];
    static final float[] NY = new float[SO_NUT];
    static final float[] NR = new float[SO_NUT];
    static final boolean[] NHIEN = new boolean[SO_NUT];
    static final long[] NNHAN = new long[SO_NUT];      // lúc bấm (hoạt hình nhún)
    static final int[] NPTR = new int[SO_NUT];
    /** ô quickslot tương ứng nút chiêu */
    static final int[] O_CUA_NUT = {2, 0, 1, 3, 4};

    /** Hàng nút trên: canh phải (Menu sát mép phải), trên thẻ bản đồ nhỏ. */
    static float xNutTren(int i) {
        HudBoTri.R = R;
        return HudBoTri.xNutTren(i);
    }

    static float jx0, jy0, jr;   // cần điều khiển (chỗ nghỉ)

    private static float kDpi = -1;

    /** Cỡ nút theo mật độ điểm ảnh: giữ cỡ vật lý ~ như điện thoại 420 dpi (hệ số 3) trên máy khác. */
    static float kDpi() {
        if (kDpi < 0) {
            try {
                float dpi = NenAndroid.ungDung.getResources().getDisplayMetrics().densityDpi;
                kDpi = Math.max(0.8f, Math.min(1.3f, (dpi / Math.max(0.5f, NenAndroid.tiLe)) / 140f));
            } catch (Throwable e) {
                kDpi = 1f;
            }
        }
        return kDpi;
    }

    /** px logic mỗi dp: (dpi / 160) / tiLe - nút HUD >= 44 dp sau phép phóng phủ kín (HudBoTri). */
    private static float dpiMay = -1;

    static float dpLogic() {
        if (dpiMay < 0) {
            try {
                dpiMay = NenAndroid.ungDung.getResources().getDisplayMetrics().densityDpi;
            } catch (Throwable e) {
                return 1f;
            }
        }
        return (dpiMay / 160f) / Math.max(0.5f, NenAndroid.tiLe);
    }

    static void boTri() {
        tinhKhung();
        HudBoTri.dpLogic = dpLogic();
        HudBoTri.boTri(L, T, R, B, kDpi(), GS, GX, GY);
        System.arraycopy(HudBoTri.NX, 0, NX, 0, SO_NUT);
        System.arraycopy(HudBoTri.NY, 0, NY, 0, SO_NUT);
        System.arraycopy(HudBoTri.NR, 0, NR, 0, SO_NUT);
        jr = HudBoTri.jr;
        jx0 = HudBoTri.jx0;
        jy0 = HudBoTri.jy0;
    }

    static void dat(int n, float x, float y, float r) {
        NX[n] = x;
        NY[n] = y;
        NR[n] = r;
    }

    // ------------------------------------------------------------------ trạng thái hiển thị
    static volatile boolean hien;
    static float alpha;
    static long tTruoc;
    static int soVienTin;
    static boolean chinhSua;
    static int nhomChon = 1;

    static boolean tinhHien() {
        if (!BAT) return false;
        GameScr gs = game.render.Canvas.gameScr;
        if (gs == null || game.render.Canvas.currentScreen != gs || !gs.readyGetGameLogic) return false;
        if (game.render.Canvas.currentDialog != null || game.render.Canvas.menu.showMenu || gs.chatMode) return false;
        if (gs.hideGUI == 2 || game.render.Canvas.isVirTualKey || game.render.Canvas.isLoad) return false;
        if (GameScr.currQuest != null && GameScr.currQuest.isShow) return false;
        if (HdCau.bangMo()) return false;
        // hộp thoại NPC đáy màn (HopThoaiDuoi của jar): chạm hộp để tiếp, HUD nhường chỗ
        if (game.pokemon.HopThoaiDuoi.dangBat()) return false;
        return gs.mainChar != null;
    }

    public boolean dangDong() {
        if (HdHieuUng.dangDong() || HdPhong.dangDong()) return true;
        if (!hien && alpha <= 0f) return false;
        if (alpha > 0f && alpha < 1f) return true;
        if (jHoatDong || chinhSua) return true;
        long now = System.currentTimeMillis();
        for (int i = 0; i < SO_NUT; i++) if (now - NNHAN[i] < 250) return true;
        return dangHoiChieu;
    }

    // ------------------------------------------------------------------ vẽ
    static final Paint TXT = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    static final Paint TXV = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    static final Paint P = new Paint(Paint.ANTI_ALIAS_FLAG);
    static final Paint PV = new Paint(Paint.ANTI_ALIAS_FLAG);
    static final Paint PANH = new Paint();
    static final RectF RF = new RectF();
    static final Rect RS = new Rect();
    static final Path PATH = new Path();
    static boolean dangHoiChieu;

    static {
        PV.setStyle(Paint.Style.STROKE);
        PV.setStrokeCap(Paint.Cap.ROUND);
        TXV.setStyle(Paint.Style.STROKE);
        TXV.setStrokeJoin(Paint.Join.ROUND);
        PANH.setFilterBitmap(false);
    }

    public void ve(Graphics g) {
        long now = System.currentTimeMillis();
        float dt = tTruoc == 0 ? 0.016f : Math.min(0.1f, (now - tTruoc) / 1000f);
        tTruoc = now;
        boolean h = false;
        try {
            h = tinhHien();
        } catch (Throwable e) {
            h = false;
        }
        if (h != hien) {
            hien = h;
            if (!h) thaHet();
        }
        HdKiem.lopPhuMoi();
        Canvas cv0 = g.androidCanvas();
        try {
            HdHieuUng.veLopPhu(cv0, NenAndroid.rong, NenAndroid.cao);
        } catch (Throwable e) {
            // bỏ qua
        }
        try {
            if (HdNut.dungPhimMemJar()) veNutChat(cv0);
            HdNut.ve(cv0);
        } catch (Throwable e) {
            // bỏ qua
        }
        alpha += (h ? 1 : -1) * dt * 7f;
        if (alpha < 0) alpha = 0;
        if (alpha > 1) alpha = 1;
        if (!h) {
            // ô chat / hộp thoại NPC: HUD ẩn nhưng bản đồ nhỏ HD vẫn hiện (jar không còn tự vẽ ảnh bản đồ)
            try {
                GameScr gs = game.render.Canvas.gameScr;
                if (gs != null && game.render.Canvas.currentScreen == gs && game.render.Canvas.currentDialog == null
                        && !game.render.Canvas.menu.showMenu && !HdCau.bangMo() && gs.readyGetGameLogic && gs.hideGUI != 2) {
                    tinhKhung();
                    TXT.setTypeface(HdChu.typeface(1));
                    veTheMiniMap(g, cv0, gs);
                }
            } catch (Throwable e) {
                // bỏ qua
            }
        }
        if (alpha <= 0f) {
            HdKiem.lopPhuXong(NenAndroid.rong, NenAndroid.cao);
            return;
        }
        TXT.setTypeface(HdChu.typeface(1));
        TXV.setTypeface(HdChu.typeface(1));
        boTri();
        if (h) lapDanh(now);
        Canvas c = g.androidCanvas();
        int sv = alpha < 1f ? c.saveLayerAlpha(0, 0, W, H, (int) (alpha * 255)) : c.save();
        try {
            GameScr gs = game.render.Canvas.gameScr;
            MainChar mc = gs.mainChar;
            gVe = g;
            veThe(g, c, mc);
            veTheMiniMap(g, c, gs);
            veNutTren(g, c);
            veChat(c);
            veCanDieuKhien(c);
            veCumDanh(g, c, gs, mc, now);
            veTenChieu(g, c, mc, now);
            if (chinhSua) veChinhSua(c);
            if (HdKiem.bat) ghiKiem(gs);
        } catch (Throwable e) {
            // HUD lỗi không được làm hỏng khung game
        }
        c.restoreToCount(sv);
        HdKiem.lopPhuXong(W, H);
    }

    /** Ô chat của jar (Đóng / gửi / Xoá theo vùng phím mềm): vẽ thành 3 nút viên rõ ràng ở đúng vùng chạm. */
    static void veNutChat(Canvas c) {
        tinhKhung();
        int Wl = NenAndroid.rong, Hl = NenAndroid.cao;
        int wz = Math.max(50, Wl * 22 / 100);
        float h = 24, y = Hl - h - 3 - NenAndroid.anToanDuoi;
        String[] ten = {"Đóng", "Gửi", "Xoá"};
        float[] x0 = {Math.max(4, NenAndroid.anToanTrai + 2), Wl / 2f - Math.min(wz, 110) / 2f, 0};
        for (int i = 0; i < 3; i++) {
            TXT.setTextSize(10f);
            float w = i == 1 ? Math.min(wz, 110) : Math.max(60, TXT.measureText(ten[i]) + 26);
            float l = i == 2 ? Wl - Math.max(4, NenAndroid.anToanPhai + 2) - w : x0[i];
            float rr = h / 2;
            HdVe.bong(c, l, y, l + w, y + h, rr, 1f);
            // 0.9.6: Gửi = viên teal, Đóng / Xoá = viên trắng viền kẻ nhạt
            if (i == 1) HdVe.tronDoc(c, l, y, l + w, y + h, rr, 0xFF2FCDBE, 0xFF1FB5A8);
            else HdVe.tronDoc(c, l, y, l + w, y + h, rr, 0xF8FFFFFF, 0xF8F6F9FA);
            if (i != 1) HdVe.vienTron(c, l, y, l + w, y + h, rr, 0.8f, 0xFFDFE6EB);
            chu(c, ten[i], l + w / 2, y + h / 2 + 3.6f, 10f, i == 1 ? 0xFFFFFFFF : 0xFF24323F, 0, 2);
            HdKiem.ghi(1, l, y, l + w, y + h, ten[i]);
        }
    }

    public void khungMoi() {
        tinhKhung();   // vùng an toàn cho bảng chung (game.pokemon.Bang) mỗi khung
        HdPhong.khungMoi();
        HdVe.khungMoi();
        HdKiem.khungMoi();
        HdNut.khungMoi();
    }

    public void khungXong() {
        HdKiem.khungXong();
        try {
            HdNut.khungXong();
        } catch (Throwable e) {
            HdNut.hien = false;
        }
    }

    /** Ghi hình chữ nhật các nút / thẻ HUD cho công cụ kiểm chồng lấn. */
    static void ghiKiem(GameScr gs) {
        HdKiem.ghi(2, L, T, L + RONG_THE, T + CAO_THE, "the");
        for (int n = 0; n < SO_NUT; n++) {
            if (n == N_MINIMAP) continue;
            if (n == N_MUC_TIEU && gs.focusedActor == null && gs.actors.size() <= 1) continue;
            HdKiem.ghi(2, NX[n] - NR[n], NY[n] - NR[n], NX[n] + NR[n], NY[n] + NR[n], "o:nut" + n);
        }
        HdKiem.ghi(2, jx0 - jr, jy0 - jr, jx0 + jr, jy0 + jr, "o:can");

    }

    static void chu(Canvas c, String s, float x, float y, float co, int mau, int vien, int canh) {
        TXT.setTextSize(co);
        TXT.setColor(mau);
        TXT.setTextAlign(canh == 0 ? Paint.Align.LEFT : canh == 1 ? Paint.Align.RIGHT : Paint.Align.CENTER);
        if (vien != 0) {
            TXV.setTextSize(co);
            TXV.setColor(vien);
            TXV.setStrokeWidth(co * 0.2f);
            TXV.setTextAlign(TXT.getTextAlign());
            c.drawText(s, x, y, TXV);
        }
        c.drawText(s, x, y, TXT);
    }

    /** Viên kính tối (toast, tin máy chủ). */
    public static void vienKinh(Graphics g, int x, int y, int w, int h) {
        Canvas c = g.androidCanvas();
        float l = x + g.getTranslateX();
        float t = y + g.getTranslateY();
        HdVe.bong(c, l, t, l + w, t + h, h / 2f, 1f);
        // 0.9.6: viên xám đá trong mờ (không navy)
        HdVe.tronDoc(c, l, t, l + w, t + h, h / 2f, 0xD0323E4A, 0xD8222C36);
    }

    // --- thẻ trạng thái
    static final StringBuilder SB = new StringBuilder();

    /** Thẻ trạng thái kiểu Pokémon: huy hiệu chân dung + dải Lv, tên + chip hệ, HP / PP có nhãn + số trong thanh, EXP mảnh + %. */
    static void veThe(Graphics g, Canvas c, MainChar mc) {
        float l = L, t = T, r = L + RONG_THE, b = T + CAO_THE;
        // 0.9.5: thẻ SÁNG mờ (trắng ~90 %), viền tối mảnh, chữ tối (game.pokemon.ChuDe)
        HdVe.bong(c, l, t, r, b, 13, 1.5f);
        HdVe.tronDoc(c, l, t, r, b, 13, 0xF2FFFFFF, 0xEAF6F9FA);   // 0.9.6: thẻ trắng, không viền tối
        // huy hiệu chân dung (vòng trắng - đỏ Poké Ball)
        float ax = l + 22, ay = t + 21, ar = 17.5f;
        P.setShader(null);
        P.setColor(0xFFFFFFFF);
        c.drawCircle(ax, ay, ar + 1.6f, P);
        RF.set(ax - ar - 1.6f, ay - ar - 1.6f, ax + ar + 1.6f, ay + ar + 1.6f);
        P.setColor(0xFFE3350D);
        c.drawArc(RF, 180, 180, true, P);
        P.setShader(HdVe.doc(0xFFE6F7F4, 0xFFBFE9E3, ay - ar, ay + ar));
        c.drawCircle(ax, ay, ar, P);
        P.setShader(null);
        int svc = c.save();
        PATH.reset();
        PATH.addCircle(ax, ay, ar, Path.Direction.CW);
        c.clipPath(PATH);
        g.setClip(0, 0, W, H);
        if (mc.pokeDex <= 0 || !game.pokemon.PokeBody.paintChanDung(g, mc.pokeDex, mc.pokeShiny, (int) ax, (int) ay + 2, 28)) {
            try {
                game.render.Res.getCharPartInfo(2, mc.currentHead).paintStatic(g, (short) ax, (short) (ay + 1), 0, mc.frame);
            } catch (Throwable e) {
                // không có ảnh đầu
            }
        }
        c.restoreToCount(svc);
        // dải Lv (ruy băng đỏ dưới huy hiệu)
        SB.setLength(0);
        SB.append("Lv ").append((int) mc.level);
        String lv = SB.toString();
        TXT.setTextSize(7.5f);
        float wl = TXT.measureText(lv) + 10;
        float ry = ay + ar - 3;
        PATH.reset();
        PATH.moveTo(ax - wl / 2 - 3, ry);
        PATH.lineTo(ax + wl / 2 + 3, ry);
        PATH.lineTo(ax + wl / 2, ry + 5);
        PATH.lineTo(ax + wl / 2 + 3, ry + 10);
        PATH.lineTo(ax - wl / 2 - 3, ry + 10);
        PATH.lineTo(ax - wl / 2, ry + 5);
        PATH.close();
        P.setShader(HdVe.doc(0xFFF4613A, 0xFFB8260A, ry, ry + 10));
        c.drawPath(PATH, P);
        P.setShader(null);
        chu(c, lv, ax, ry + 7.8f, 7.5f, 0xFFFFFFFF, 0x90000000, 2);
        // điểm tiềm năng / kỹ năng chưa cộng: chấm nhấp nháy góc huy hiệu
        if ((mc.basePointLeft > 0 || mc.skillPointLeft > 0) && System.currentTimeMillis() % 1000 < 650) {
            P.setColor(mc.basePointLeft > 0 ? 0xFFFF7A1A : 0xFF40C8F8);
            c.drawCircle(ax + ar * 0.75f, ay - ar * 0.75f, 3.4f, P);
            PV.setColor(0xFFFFFFFF);
            PV.setStrokeWidth(0.8f);
            c.drawCircle(ax + ar * 0.75f, ay - ar * 0.75f, 3.4f, PV);
        }
        // tên + chip hệ
        float x0 = l + 45, x1 = r - 7, wB = x1 - x0;
        int he = -1;
        try {
            if (mc.pokeDex > 0) he = game.pokemon.HePoke.he1(mc.pokeDex);
        } catch (Throwable e) {
            he = -1;
        }
        String tenHe = he >= 0 ? game.pokemon.KieuO.tenHe(he) : null;
        TXT.setTextSize(6.6f);
        float wChip = tenHe == null ? 0 : TXT.measureText(tenHe) + 9;
        String ten = mc.name == null ? "" : mc.name;
        String tenGon = catChu(ten, 9f, wB - wChip - 4);
        chu(c, tenGon, x0, t + 11.5f, 9f, 0xFF000000 | game.pokemon.ChuDe.CHU, 0, 0);
        if (tenHe != null) {
            TXT.setTextSize(9f);
            float cx0 = Math.min(x1 - wChip, x0 + TXT.measureText(tenGon) + 4);
            int mh = HdVe.argb(game.pokemon.KieuO.mauHe(he));
            HdVe.tronDoc(c, cx0, t + 4.5f, cx0 + wChip, t + 13, 4.25f, HdVe.pha(mh, 0xFFFFFFFF, 0.25f), HdVe.pha(mh, 0xFF000000, 0.2f));
            HdVe.vienTron(c, cx0, t + 4.5f, cx0 + wChip, t + 13, 4.25f, 0.6f, 0xA0FFFFFF);
            chu(c, tenHe, cx0 + wChip / 2, t + 11.3f, 6.6f, 0xFFFFFFFF, 0x80000000, 2);
        }
        // HP
        veThanhNhan(c, x0, t + 15.5f, wB, 10, "HP", 0xFFFFFFFF, mc.hp, mc.maxhp, HdVe.mauThanh(0, mc.hp, mc.maxhp));
        // PP
        veThanhNhan(c, x0, t + 27.5f, wB, 8, "PP", 0xFFFFFFFF, mc.mp, mc.maxmp, HdVe.argb(game.pokemon.ChuDe.PP));
        // EXP mảnh + %
        String pc = mc.getPercent();
        if (pc == null) pc = "";
        TXT.setTextSize(6.6f);
        float wPc = TXT.measureText(pc) + 3;
        HdVe.veThanh(c, x0, t + 38.5f, wB - wPc, 4, mc.lvpercent / 1000f, HdVe.argb(game.pokemon.ChuDe.EXP));
        chu(c, pc, x1, t + 42.4f, 6.6f, 0xFF000000 | game.pokemon.ChuDe.CHU_PHU, 0, 1);
        // các viên tin dưới thẻ (như jar: số PK, giờ đếm ngược, giờ vàng)
        int n = 0;
        float y = b + 4;
        if (mc.killer > 0) {
            SB.setLength(0);
            SB.append("PK ").append((int) mc.killer);
            vienTin(c, l, y + n * 15, SB.toString(), 0xFFE3350D);
            n++;
        }
        if (mc.delay >= 0) {
            int[] tg = mc.getTime();
            SB.setLength(0);
            SB.append(tg[0]).append(" : ").append(tg[1]);
            vienTin(c, l, y + n * 15, SB.toString(), 0xFF3D7DCA);
            n++;
        }
        String tm = mc.getGoldTime();
        if (tm != null && tm.length() > 0) {
            vienTin(c, l, y + n * 15, tm + " " + Char.goldTime, 0xFFE8A800);
            n++;
        }
        soVienTin = n;
        // 0.9.5: viên Tự động (game.pokemon.VienTrangThai) dưới các viên tin; chữ "+N EXP" dưới góc phải thẻ
        game.pokemon.VienTrangThai.datHd((int) l, (int) (y + n * 15), (int) r - 4, (int) b + 2);
    }

    /** Thanh có nhãn (HP / PP) bên trái trong thanh + số "gt/max" bên phải trong thanh. */
    static void veThanhNhan(Canvas c, float x, float y, float w, float h, String nhan, int mauNhan, long gt, long max, int mau) {
        if (gt < 0) gt = 0;
        HdVe.veThanh(c, x, y, w, h, max <= 0 ? 0 : gt / (float) max, mau);
        float co = h >= 10 ? 7f : 6f;
        chu(c, nhan, x + 3.5f, y + h / 2 + co * 0.36f, co, mauNhan, 0x90000000, 0);
        SB.setLength(0);
        SB.append(gt).append('/').append(max);
        chu(c, SB.toString(), x + w - 3.5f, y + h / 2 + co * 0.36f, co, 0xFF000000 | game.pokemon.ChuDe.CHU, 0, 1);
    }

    /** Thẻ bản đồ nhỏ HD: đầu thẻ (tên map / vùng / toạ độ) + cả bản đồ phóng VỪA khung (giữ tỉ lệ, canh giữa,
     *  bo góc), khung camera, nhân vật (mũi tên theo hướng), cổng, NPC, đồng đội, điểm nhiệm vụ / câu cá của jar.
     *  Chạm = bản đồ lớn của vùng. */
    static void veTheMiniMap(Graphics g, Canvas c, GameScr gs) {
        Image anh = game.model.Tilemap.imgMap;
        if (mmX < 0 || anh == null) return;
        float l = mmX - 4, t = T + yTheMM(), r = mmX + mmW + 4, b = mmY + mmH + 4;
        NX[N_MINIMAP] = (l + r) / 2;
        NY[N_MINIMAP] = (t + b) / 2;
        NR[N_MINIMAP] = 0;
        HdVe.bong(c, l, t, r, b, 9, 1.5f);
        HdVe.tronDoc(c, l, t, r, b, 9, 0xF8FFFFFF, 0xF8F3F6FB);
        // đầu thẻ: tên map (+ vùng) - 0.9.5 bỏ toạ độ ô (trông như chữ gỡ lỗi)
        String ten = GameScr.nameMap == null ? "" : game.pokemon.ChuPokemon.D(GameScr.nameMap);
        chu(c, catChu(ten, 8.6f, r - l - 10), l + 5, t + 10, 8.6f, 0xFF000000 | game.pokemon.ChuDe.CHU, 0, 0);
        String vung = HdCau.tenVung();
        if (vung != null) chu(c, vung, l + 5, t + 19, 6.6f, 0xFF000000 | game.pokemon.ChuDe.CHU_PHU, 0, 0);
        // vùng bản đồ: phóng vừa, canh giữa, bo góc
        float al = mmX, at = mmY, aw = mmW, ah = mmH;
        Bitmap bm = anh.getBitmap();
        int iw = Math.max(1, bm.getWidth()), ih = Math.max(1, bm.getHeight());
        float k = Math.min(aw / iw, ah / ih);
        float dw = iw * k, dh = ih * k;
        float ox = al + (aw - dw) / 2, oy = at + (ah - dh) / 2;
        int sv = c.save();
        PATH.reset();
        RF.set(al, at, al + aw, at + ah);
        PATH.addRoundRect(RF, 6, 6, Path.Direction.CW);
        c.clipPath(PATH);
        P.setShader(null);
        P.setColor(0xFF0A142C);
        c.drawRect(RF, P);
        RF.set(ox, oy, ox + dw, oy + dh);
        PANH.setFilterBitmap(k < 1f);   // thu nhỏ map lớn: lọc mượt; phóng map nhỏ: giữ ô vuông
        c.drawBitmap(bm, null, RF, PANH);
        PANH.setFilterBitmap(false);
        // khung camera
        float s16 = k / 16f;
        PV.setColor(0xB0FFFFFF);
        PV.setStrokeWidth(0.8f);
        float[] vt = HdZoom.vungThay();
        RF.set(ox + vt[0] * s16, oy + vt[1] * s16, ox + (vt[0] + vt[2]) * s16, oy + (vt[1] + vt[3]) * s16);
        c.drawRect(RF, PV);
        // điểm nhiệm vụ / câu cá / mốc đua của jar (vẽ theo ô, g phóng k)
        g.setClip(0, 0, W, H);
        g.datBienDoi(k, ox, oy);
        HdCau.veDiemMiniJar(g);
        g.datBienDoi(1f, 0, 0);
        // cổng
        Vector pp = game.model.Tilemap.pointPop;
        for (int i = 0; pp != null && i < pp.size(); i++) {
            Object o = pp.elementAt(i);
            if (!(o instanceof game.model.PopupName)) continue;
            game.model.PopupName p = (game.model.PopupName) o;
            float x = ox + p.x * s16, y = oy + p.y * s16;
            P.setColor(0xFFFFFFFF);
            c.drawCircle(x, y, 2.4f, P);
            P.setColor(0xFFE3350D);
            c.drawCircle(x, y, 1.5f, P);
        }
        // NPC + đồng đội
        Vector ac = gs.actors;
        for (int i = 0; ac != null && i < ac.size(); i++) {
            Object o = ac.elementAt(i);
            if (!(o instanceof game.model.Actor)) continue;
            game.model.Actor a = (game.model.Actor) o;
            if (a == gs.mainChar) continue;
            if (a.catagory == 2) {
                P.setColor(0xFFFFD23A);
                c.drawCircle(ox + a.x * s16, oy + a.y * s16, 1.4f, P);
            }
        }
        for (int i = 0; Char.party != null && i < Char.party.size(); i++) {
            try {
                game.model.PartyInfo pi = (game.model.PartyInfo) Char.party.elementAt(i);
                game.model.Actor a = gs.findCharByID((short) pi.id);
                if (a != null && a != gs.mainChar) {
                    P.setColor(0xFF40C8F8);
                    c.drawCircle(ox + a.x * s16, oy + a.y * s16, 1.8f, P);
                }
            } catch (Throwable e) {
                // bỏ qua
            }
        }
        // nhân vật: mũi tên theo hướng
        float px = ox + gs.mainChar.x * s16, py = oy + gs.mainChar.y * s16;
        float goc;
        switch (gs.mainChar.dir) {
            case 0: goc = 90; break;    // xuống
            case 1: goc = -90; break;   // lên
            case 2: goc = 180; break;   // trái
            default: goc = 0; break;    // phải
        }
        int sv2 = c.save();
        c.rotate(goc, px, py);
        PATH.reset();
        PATH.moveTo(px + 4.2f, py);
        PATH.lineTo(px - 3f, py - 3.2f);
        PATH.lineTo(px - 1.6f, py);
        PATH.lineTo(px - 3f, py + 3.2f);
        PATH.close();
        P.setColor(0xFF1E2A44);
        PV.setColor(0xFFFFFFFF);
        PV.setStrokeWidth(1.6f);
        c.drawPath(PATH, PV);
        P.setColor(0xFFFFD23A);
        c.drawPath(PATH, P);
        c.restoreToCount(sv2);
        c.restoreToCount(sv);
        // viền thẻ mảnh tối (như thẻ trạng thái)
        PV.setShader(HdVe.doc(0x30000000 | game.pokemon.ChuDe.CHU, 0x30000000 | game.pokemon.ChuDe.CHU, t, b));
        PV.setStrokeWidth(0.8f);
        RF.set(l + 0.65f, t + 0.65f, r - 0.65f, b - 0.65f);
        c.drawRoundRect(RF, 9, 9, PV);
        PV.setShader(null);
        HdKiem.ghi(2, l, t, r, b, "the-minimap");
    }

    static boolean trongMiniMap(float x, float y) {
        if (mmX < 0 || game.model.Tilemap.imgMap == null) return false;
        return x >= mmX - 4 && x <= mmX + mmW + 4 && y >= T + yTheMM() && y <= mmY + mmH + 4;
    }

    /** Vùng toast đáy (tin chạy chữ): giữa cần điều khiển và cụm nút, không đè nút. [x0, x1, y] */
    public static int[] vungToastDuoi() {
        boTri();
        int x0 = (int) (jx0 + jr + 10), x1 = (int) (NX[N_PP] - NR[N_PP] - 10);
        if (x1 - x0 > 320) {
            int c = (x0 + x1) / 2;
            x0 = c - 160;
            x1 = c + 160;
        }
        if (x1 - x0 < 120) {
            x0 = W / 2 - 60;
            x1 = W / 2 + 60;
        }
        return new int[]{x0, x1, B - 18};
    }

    /** Mờ dần hai đầu viên chạy chữ (chữ không bị cắt cụt giữa chừng). */
    public static void mepMo(Graphics g, int x, int y, int w, int h) {
        Canvas c = g.androidCanvas();
        float l = x + g.getTranslateX(), t = y + g.getTranslateY();
        float m = Math.min(26, w / 5f);
        P.setShader(new android.graphics.LinearGradient(l + 4, 0, l + 4 + m, 0, 0xF0222C36, 0x00222C36, android.graphics.Shader.TileMode.CLAMP));
        c.drawRect(l + 4, t + 1.5f, l + 4 + m, t + h - 1.5f, P);
        P.setShader(new android.graphics.LinearGradient(l + w - 4 - m, 0, l + w - 4, 0, 0x00222C36, 0xF0222C36, android.graphics.Shader.TileMode.CLAMP));
        c.drawRect(l + w - 4 - m, t + 1.5f, l + w - 4, t + h - 1.5f, P);
        P.setShader(null);
    }

    /** 0.9.5: viên trắng viền tối mảnh + chấm màu + chữ tối (cùng kiểu viên Tự động game.pokemon.VienTrangThai). */
    static void vienTin(Canvas c, float x, float y, String s, int mau) {
        TXT.setTextSize(7.2f);
        float w = TXT.measureText(s) + 17;
        HdVe.tronDoc(c, x, y, x + w, y + 12, 6, 0xFAFFFFFF, 0xF2F3F6FB);
        HdVe.vienTron(c, x, y, x + w, y + 12, 6, 0.6f, 0x30000000 | game.pokemon.ChuDe.CHU);
        P.setShader(null);
        P.setColor(mau);
        c.drawCircle(x + 7, y + 6, 3, P);
        chu(c, s, x + 12, y + 8.6f, 7.2f, 0xFF000000 | game.pokemon.ChuDe.CHU, 0, 0);
    }

    static String catChu(String s, float co, float w) {
        TXT.setTextSize(co);
        if (TXT.measureText(s) <= w) return s;
        int n = TXT.breakText(s, true, w - TXT.measureText(".."), null);
        return s.substring(0, Math.max(1, n)) + "..";
    }

    // --- nút tròn kính
    static float nhun(int n, long now) {
        long d = now - NNHAN[n];
        if (NPTR[n] >= 0) return 0.9f;
        if (d < 220) {
            float t = d / 220f;
            return 0.9f + 0.1f * (1 - (1 - t) * (1 - t)) + (float) Math.sin(t * Math.PI) * 0.04f;
        }
        return 1f;
    }

    /** Nút tròn 0.9.5: bóng mềm + viền trắng sáng + thân màu (không còn đĩa navy). */
    static void nenNut(Canvas c, float x, float y, float r, int tren, int duoi, boolean sang) {
        P.setShader(null);
        P.setColor(0x30000000);
        c.drawCircle(x, y + 1.4f, r + 0.8f, P);
        P.setColor(sang ? 0xFFFFCB05 : 0xFFFFFFFF);
        c.drawCircle(x, y, r, P);
        P.setShader(HdVe.doc(tren, duoi, y - r, y + r));
        c.drawCircle(x, y, r - 1.6f, P);
        P.setShader(null);
    }

    /** Nút một màu (nhạt -> màu). */
    static void nenMau(Canvas c, float x, float y, float r, int rgb, boolean sang) {
        int m = HdVe.argb(rgb);
        nenNut(c, x, y, r, HdVe.pha(m, 0xFFFFFFFF, 0.25f), m, sang);
    }

    /**
     * 0.9.6 kiểu GO: hàng trên = đĩa TRẮNG icon màu (Tự động: đĩa teal khi đang chạy); Menu = Poké Ball to giữa đáy.
     * Chấm đỏ theo ChamMoi (Menu: điểm / thư / chiêu mới; Nhiệm vụ: nhiệm vụ mới / đủ để trả).
     */
    static void veNutTren(Graphics g, Canvas c) {
        long now = System.currentTimeMillis();
        int[] tren = {N_MENU, N_TU_DONG, N_CHAT, N_THEM, N_CHINH};
        boolean tuDong = HdCau.tuDongDang();
        for (int k = 0; k < SO_NUT_TREN; k++) {
            int n = tren[k];
            float s = nhun(n, now);
            float x = NX[n], y = NY[n], r = NR[n] * s;
            if (n == N_TU_DONG && tuDong) {
                P.setColor(0x601FB5A8);
                c.drawCircle(x, y, r + 2.5f + (float) Math.sin(now / 180.0) * 0.8f, P);
                nenMau(c, x, y, r, game.pokemon.ChuDe.CHINH, false);
            } else {
                nenNut(c, x, y, r, 0xFFFFFFFF, 0xFFF6F9FA, false);
            }
            veIcon(g, c, n, x, y, r);
            boolean cham = n == N_CHINH ? game.pokemon.ChamMoi.hudNhiemVu() : n == N_MENU && game.pokemon.ManMenu.coChamDo();
            if (cham) {
                P.setColor(0xFFFFFFFF);
                c.drawCircle(x + r * 0.7f, y - r * 0.7f, r * 0.3f + 1f, P);
                P.setColor(0xFFE8412C);
                c.drawCircle(x + r * 0.7f, y - r * 0.7f, r * 0.3f, P);
            }
        }
    }

    /** 0.9.5 (một lối vào mỗi việc): nút trên = Menu (Poké Ball), Tự động, Chat, Hành trang (cam), Nhiệm vụ (vàng).
     *  Nút "Thêm" (#, Menu nhanh) trùng ô Cài đặt của Menu -> thành Hành trang; nút "Chỉnh" -> Nhiệm vụ (chỉnh bố cục
     *  HUD + âm thanh nằm ở ô Cài đặt của Menu). */
    static int MAU_TREN(int n) {
        switch (n) {
            case N_THEM:
                return game.pokemon.ChuDe.NUT_TUI;
            case N_CHINH:
                return game.pokemon.ChuDe.NUT_NV;
            case N_CHAT:
                return game.pokemon.ChuDe.XANH;
            default:
                return game.pokemon.ChuDe.XAM;
        }
    }

    static void veIcon(Graphics g, Canvas c, int n, float x, float y, float r) {
        int bt = n == N_THEM ? game.pokemon.BieuTuong.TUI : n == N_CHINH ? game.pokemon.BieuTuong.NHIEM_VU
                : n == N_MENU ? game.pokemon.BieuTuong.POKEBALL : n == N_CHAT || n == N_CHAT_BONG ? game.pokemon.BieuTuong.CHAT : -1;
        if (bt >= 0) {
            // 0.9.6: icon MÀU trên đĩa trắng (Poké Ball giữ màu thật)
            int s = (int) (r * (n == N_MENU ? 1.45f : 1.25f));
            int nen = 0xFFFFFF;
            int mau = n == N_CHAT_BONG ? game.pokemon.ChuDe.XANH : MAU_TREN(n);
            int tx = g.getTranslateX(), ty = g.getTranslateY();
            g.translate(-tx, -ty);
            game.pokemon.BieuTuong.ve(g, bt, (int) (x - s / 2f), (int) (y - s / 2f), s, mau, nen);
            g.translate(tx, ty);
            return;
        }
        veIcon(c, n, x, y, r);
    }

    static void veIcon(Canvas c, int n, float x, float y, float r) {
        PV.setColor(0xFFFFFFFF);
        PV.setStrokeWidth(r * 0.14f);
        P.setShader(null);
        P.setColor(0xFFFFFFFF);
        float u = r * 0.42f;
        switch (n) {
            case N_MENU:
                for (int i = -1; i <= 1; i++) c.drawLine(x - u, y + i * u * 0.62f, x + u, y + i * u * 0.62f, PV);
                break;
            case N_CHAT:
            case N_CHAT_BONG:
                RF.set(x - u * 1.1f, y - u * 0.85f, x + u * 1.1f, y + u * 0.55f);
                c.drawRoundRect(RF, u * 0.5f, u * 0.5f, P);
                PATH.reset();
                PATH.moveTo(x - u * 0.5f, y + u * 0.4f);
                PATH.lineTo(x - u * 0.75f, y + u * 1.05f);
                PATH.lineTo(x + u * 0.05f, y + u * 0.45f);
                PATH.close();
                c.drawPath(PATH, P);
                P.setColor(0xFF1A3462);
                for (int i = -1; i <= 1; i++) c.drawCircle(x + i * u * 0.5f, y - u * 0.15f, u * 0.14f, P);
                break;
            case N_TU_DONG:
                // tia sét (tự động đánh)
                PATH.reset();
                PATH.moveTo(x + u * 0.25f, y - u * 1.1f);
                PATH.lineTo(x - u * 0.7f, y + u * 0.15f);
                PATH.lineTo(x - u * 0.02f, y + u * 0.15f);
                PATH.lineTo(x - u * 0.3f, y + u * 1.1f);
                PATH.lineTo(x + u * 0.75f, y - u * 0.2f);
                PATH.lineTo(x + u * 0.05f, y - u * 0.2f);
                PATH.close();
                boolean khoaTd = !HdCau.tuDongDang() && game.render.Canvas.gameScr != null
                        && game.pokemon.KhoaMap.tuDongKhoa(game.render.Canvas.gameScr.map);
                P.setColor(HdCau.tuDongDang() ? 0xFFFFFFFF : khoaTd ? HdVe.argb(game.pokemon.ChuDe.CHU_NHAT) : 0xFF138C82);   // 0.9.6: sét teal trên đĩa trắng
                c.drawPath(PATH, P);
                if (khoaTd) {
                    // bản đồ không cho Tự động đánh (gói -108): ổ khoá xám góc dưới phải; chạm = báo lý do (TuDong.bat)
                    float kx = x + u * 0.75f, ky = y + u * 0.7f, kw = u * 0.55f;
                    P.setColor(HdVe.argb(game.pokemon.ChuDe.XAM));
                    c.drawRect(kx - kw, ky - kw * 0.2f, kx + kw, ky + kw * 1.2f, P);
                    PV.setStrokeWidth(kw * 0.35f);
                    PV.setColor(HdVe.argb(game.pokemon.ChuDe.XAM));
                    c.drawArc(kx - kw * 0.65f, ky - kw * 1.1f, kx + kw * 0.65f, ky + kw * 0.3f, 180, 180, false, PV);
                }
                break;
            case N_THEM:
                for (int i = -1; i <= 1; i++) c.drawCircle(x + i * u * 0.75f, y, u * 0.22f, P);
                break;
            case N_CHINH:
                // bánh răng: vòng + 6 răng
                PV.setStrokeWidth(r * 0.17f);
                c.drawCircle(x, y, u * 0.6f, PV);
                for (int i = 0; i < 6; i++) {
                    double a = i * Math.PI / 3;
                    float cs = (float) Math.cos(a), sn = (float) Math.sin(a);
                    c.drawLine(x + cs * u * 0.8f, y + sn * u * 0.8f, x + cs * u * 1.12f, y + sn * u * 1.12f, PV);
                }
                break;
            case N_MUC_TIEU:
                PV.setStrokeWidth(r * 0.12f);
                c.drawCircle(x, y, u * 0.75f, PV);
                c.drawLine(x - u * 1.15f, y, x - u * 0.35f, y, PV);
                c.drawLine(x + u * 0.35f, y, x + u * 1.15f, y, PV);
                c.drawLine(x, y - u * 1.15f, x, y - u * 0.35f, PV);
                c.drawLine(x, y + u * 0.35f, x, y + u * 1.15f, PV);
                break;
            case N_DOI_BO:
                PV.setStrokeWidth(r * 0.16f);
                RF.set(x - u, y - u, x + u, y + u);
                c.drawArc(RF, 200, 250, false, PV);
                PATH.reset();
                PATH.moveTo(x + u * 0.55f, y - u * 1.35f);
                PATH.lineTo(x + u * 1.3f, y - u * 0.55f);
                PATH.lineTo(x + u * 0.25f, y - u * 0.35f);
                PATH.close();
                c.drawPath(PATH, P);
                break;
            default:
                break;
        }
    }

    // --- cụm chiến đấu
    static void veCumDanh(Graphics g, Canvas c, GameScr gs, MainChar mc, long now) {
        dangHoiChieu = false;
        if (MainChar.quickSlot == null) {
            try {
                mc.loadQuickSlot();
            } catch (Throwable e) {
                return;
            }
        }
        game.pokemon.ThanhNhanh.kiem(mc);   // 0.9.5: tự điền chiêu đã học vào ô trống
        QuickSlot[] bo = MainChar.quickSlot[GameScr.indexTabSlot];
        boolean noiNpc = gs.focusedActor != null && gs.focusedActor.catagory == 2;
        // nút đánh lớn (ô phím 5)
        {
            int n = N_DANH;
            float s = nhun(n, now);
            float x = NX[n], y = NY[n], r = NR[n] * s;
            QuickSlot qs = bo[2];
            int mau = 0xFFE3350D;
            if (!noiNpc && qs != null && qs.quickslotType == QuickSlot.TYPE_SKILL) {
                mau = HdVe.argb(game.pokemon.KieuO.mauHe(game.pokemon.PokeChieu.he(mc.clazz, qs.skillType)));
            }
            P.setColor(0x30000000);
            c.drawCircle(x, y + 2, r + 2.5f, P);
            P.setShader(HdVe.doc(0xFFFFFFFF, 0xFFC9D6EA, y - r - 2.5f, y + r + 2.5f));
            c.drawCircle(x, y, r + 2.2f, P);
            P.setShader(null);
            nenNut(c, x, y, r, HdVe.pha(mau, 0xFFFFFFFF, 0.25f), HdVe.pha(mau, 0xFF000000, 0.3f), true);
            if (noiNpc) {
                veIcon(c, N_CHAT, x, y - 3, r * 0.9f);
                chu(c, "Nói", x, y + r * 0.62f, 8f, 0xFFFFFFFF, 0x90000000, 2);
            } else {
                veO(g, c, qs, mc, x, y, r, now, true);
            }
        }
        int[] nut = {N_C0, N_C1, N_C3, N_C4};
        int[] o = {0, 1, 3, 4};
        for (int i = 0; i < 4; i++) {
            int n = nut[i];
            float s = nhun(n, now);
            float x = NX[n], y = NY[n], r = NR[n] * s;
            QuickSlot qs = bo[o[i]];
            int mau = 0xFF8C99A5;   // 0.9.6: ô không chiêu = xám đá (không navy)
            if (qs != null && qs.quickslotType == QuickSlot.TYPE_SKILL) {
                mau = HdVe.argb(game.pokemon.KieuO.mauHe(game.pokemon.PokeChieu.he(mc.clazz, qs.skillType)));
            } else if (qs != null && qs.quickslotType == QuickSlot.TYPE_POTION) {
                mau = 0xFF1FB5A8;
            }
            boolean trong = qs == null || qs.quickslotType < 0;
            if (trong) {
                // ô trống thật: "+" mờ chỉ khi còn chiêu để gán (không thì ẩn hẳn)
                if (!game.pokemon.ThanhNhanh.coGiDeGan(bo)) continue;
                P.setColor(0xB0FFFFFF);
                c.drawCircle(x, y, r, P);
                PV.setColor(0xFF000000 | game.pokemon.ChuDe.CHU_NHAT);
                PV.setStrokeWidth(1.2f);
                c.drawLine(x - 4, y, x + 4, y, PV);
                c.drawLine(x, y - 4, x, y + 4, PV);
                continue;
            }
            nenNut(c, x, y, r, HdVe.pha(mau, 0xFFFFFFFF, 0.3f), HdVe.pha(mau, 0xFF000000, 0.35f), false);
            veO(g, c, qs, mc, x, y, r, now, false);
        }
        veNutNho(c, N_DOI_BO, now);
        // 2 chấm bộ ô đang dùng cạnh nút đổi bộ
        for (int k = 0; k < 2; k++) {
            P.setColor(k == GameScr.indexTabSlot ? 0xFF1FB5A8 : 0x80A3AEB8);
            c.drawCircle(NX[N_DOI_BO] - 3 + k * 6, NY[N_DOI_BO] + NR[N_DOI_BO] + 4, 1.6f, P);
        }
        if (gs.focusedActor != null || gs.actors.size() > 1) veNutNho(c, N_MUC_TIEU, now);
        veBinh(g, c, N_HP, mc, now, true);
        veBinh(g, c, N_PP, mc, now, false);
    }

    static void veNutNho(Canvas c, int n, long now) {
        float s = nhun(n, now);
        nenMau(c, NX[n], NY[n], NR[n] * s, n == N_MUC_TIEU ? game.pokemon.ChuDe.DO : game.pokemon.ChuDe.XAM, false);
        veIcon(c, n, NX[n], NY[n], NR[n] * s);
    }

    /** Nội dung 1 ô quickslot trong nút tròn: hình chiêu / bình thuốc, hồi chiêu, thiếu PP. */
    static void veO(Graphics g, Canvas c, QuickSlot qs, MainChar mc, float x, float y, float r, long now, boolean lon) {
        if (qs == null || qs.quickslotType < 0) return;
        long conLai = 0, tong = 1;
        boolean thieuPP = false;
        if (qs.quickslotType == QuickSlot.TYPE_SKILL) {
            int sk = qs.skillType;
            // 0.9.5: icon PNG riêng từng chiêu (huy hiệu tròn, res/ui/chieu_tron_*.png); thiếu -> hình 12x12 cũ
            int oc = game.pokemon.AnhUI.oChieu(mc.clazz, sk);
            int ri = Math.round(r + 1f);
            boolean coAnh = oc >= 0 && game.pokemon.ChuUI.bo != null
                    && game.pokemon.ChuUI.bo.chieu(g, oc, true, Math.round(x) - ri, Math.round(y) - ri, 2 * ri, false);
            Image a = coAnh ? null : game.pokemon.KieuO.anhHinh(game.pokemon.PokeChieu.hinh(mc.clazz, sk), 2);
            if (a != null) {
                Bitmap bm = a.getBitmap();
                float k = (r * (lon ? 1.15f : 1.2f)) / Math.max(bm.getWidth(), bm.getHeight());
                k = Math.max(0.5f, Math.round(k * 2) / 2f);
                float w = bm.getWidth() * k, h = bm.getHeight() * k;
                RF.set(x - w / 2, y - h / 2, x + w / 2, y + h / 2);
                c.drawBitmap(bm, null, RF, PANH);
            }
            long da = now - mc.timeLastUseSkills[sk];
            tong = mc.coolDownSkill[sk];
            if (da < tong) {
                conLai = tong - da;
            } else {
                int cap = Char.skillLevelLearnt != null && sk < Char.skillLevelLearnt.length ? Char.skillLevelLearnt[sk] : 0;
                int mp = 0;
                try {
                    mp = game.model.skill.SkillManager.getSkillMP(sk, cap, mc.clazz);
                } catch (Throwable e) {
                    mp = 0;
                }
                thieuPP = mp > 0 && mc.mp < mp;
            }
        } else if (qs.quickslotType == QuickSlot.TYPE_POTION) {
            try {
                g.setClip(0, 0, W, H);
                game.render.Res.paintPotion(g, MainChar.listPotion[qs.potionType].index, (int) x, (int) y, 3);
                soLuong(c, x + r * 0.62f, y + r * 0.62f, mc.potions[qs.potionType]);
                long da = now - mc.potionLastTimeUse[qs.potionType];
                tong = MainChar.listPotion[qs.potionType].delay;
                if (da < tong) conLai = tong - da;
            } catch (Throwable e) {
                // chưa có danh sách thuốc
            }
        }
        if (thieuPP) {
            P.setColor(0x902D8FD6);
            c.drawCircle(x, y, r, P);
            chu(c, "PP", x, y + r * 0.75f, Math.max(6f, r * 0.34f), 0xFFFFFFFF, 0xC0000000, 2);
        }
        if (conLai > 0 && tong > 0) {
            dangHoiChieu = true;
            float f = conLai / (float) tong;
            P.setColor(0xA0000000);
            RF.set(x - r, y - r, x + r, y + r);
            c.drawArc(RF, -90, -360 * f, true, P);
            PV.setColor(0xFFFFCB05);
            PV.setStrokeWidth(Math.max(1.4f, r * 0.1f));
            RF.set(x - r + 1, y - r + 1, x + r - 1, y + r - 1);
            c.drawArc(RF, -90, -360 * f, false, PV);
            long giay = (conLai + 999) / 1000;
            chu(c, String.valueOf(giay), x, y + r * 0.3f, Math.max(8f, r * 0.75f), 0xFFFFFFFF, 0xD0000000, 2);
        }
    }

    static void soLuong(Canvas c, float x, float y, int so) {
        String s = game.pokemon.KieuO.soGon(so);
        TXT.setTextSize(6.5f);
        float w = Math.max(9, TXT.measureText(s) + 5);
        HdVe.tron(c, x - w / 2, y - 4.5f, x + w / 2, y + 4.5f, 4.5f, 0xF024323F);
        chu(c, s, x, y + 2.3f, 6.5f, 0xFFFFFFFF, 0, 2);
    }

    static final int[] THUOC_HP = {1, 2, 3, 21, 22, 93, 94};
    static final int[] THUOC_PP = {4, 5, 6, 23, 24, 95, 96};

    /** Loại thuốc HP / PP còn trong túi (thứ tự như GameScr.doUsePotion thay ô), -1 = hết. */
    static int loaiThuoc(MainChar mc, boolean hp) {
        int[] ds = hp ? THUOC_HP : THUOC_PP;
        if (mc.potions == null) return -1;
        for (int i = 0; i < ds.length; i++) {
            if (ds[i] < mc.potions.length && mc.potions[ds[i]] > 0) return ds[i];
        }
        return -1;
    }

    static void veBinh(Graphics g, Canvas c, int n, MainChar mc, long now, boolean hp) {
        float s = nhun(n, now);
        float x = NX[n], y = NY[n], r = NR[n] * s;
        int loai = loaiThuoc(mc, hp);
        int mau = hp ? 0xFFE3350D : 0xFF3FA0F0;
        if (loai < 0) {
            P.setColor(0x60FFFFFF);
            c.drawCircle(x, y, r, P);
            PV.setColor(HdVe.pha(mau, 0x00000000, 0.4f) & 0x80FFFFFF);
            PV.setStrokeWidth(0.9f);
            c.drawCircle(x, y, r - 0.5f, PV);
            chu(c, hp ? "HP" : "PP", x, y + 2.5f, 7f, 0x80FFFFFF, 0, 2);
            return;
        }
        nenMau(c, x, y, r, hp ? game.pokemon.ChuDe.DO : game.pokemon.ChuDe.PP, false);
        try {
            g.setClip(0, 0, W, H);
            game.render.Res.paintPotion(g, MainChar.listPotion[loai].index, (int) x, (int) y, 3);
            long da = now - mc.potionLastTimeUse[loai];
            long tong = MainChar.listPotion[loai].delay;
            if (da < tong && tong > 0) {
                dangHoiChieu = true;
                P.setColor(0x90000000);
                RF.set(x - r, y - r, x + r, y + r);
                c.drawArc(RF, -90, -360f * (tong - da) / tong, true, P);
            }
        } catch (Throwable e) {
            chu(c, hp ? "HP" : "PP", x, y + 2.5f, 7f, 0xFFFFFFFF, 0, 2);
        }
        soLuong(c, x + r * 0.7f, y + r * 0.7f, mc.potions[loai]);
    }

    /** Tên chiêu vừa dùng: viên màu hệ giữa đáy (như jar, không che nhân vật). */
    static void veTenChieu(Graphics g, Canvas c, MainChar mc, long now) {
        QuickSlot[] bo = MainChar.quickSlot == null ? null : MainChar.quickSlot[GameScr.indexTabSlot];
        if (bo == null) return;
        QuickSlot moi = null;
        long tMoi = 0;
        for (int k = 0; k < 5; k++) {
            QuickSlot qs = bo[k];
            if (qs != null && qs.quickslotType == QuickSlot.TYPE_SKILL) {
                long t = mc.timeLastUseSkills[qs.skillType];
                if (t > tMoi && now - t < 1300) {
                    tMoi = t;
                    moi = qs;
                }
            }
        }
        if (moi == null) return;
        String ten = game.pokemon.PokeChieu.ten(mc.clazz, moi.skillType);
        if (ten == null) return;
        ten = game.pokemon.ChuPokemon.D(ten);
        int mau = HdVe.argb(game.pokemon.KieuO.mauHe(game.pokemon.PokeChieu.he(mc.clazz, moi.skillType)));
        float d = (now - tMoi) / 1300f;
        float k = d < 0.12f ? 0.8f + d / 0.12f * 0.2f : 1f;
        int a = d > 0.8f ? (int) ((1 - d) / 0.2f * 255) : 255;
        TXT.setTextSize(10f);
        float w = (TXT.measureText(ten) + 22) * k, h = 18 * k;
        float x = W / 2f, y = B - 52;
        int sv = c.saveLayerAlpha(x - w, y - h, x + w, y + h, Math.max(0, Math.min(255, a)));
        HdVe.bong(c, x - w / 2, y - h / 2, x + w / 2, y + h / 2, h / 2, 1f);
        HdVe.tronDoc(c, x - w / 2, y - h / 2, x + w / 2, y + h / 2, h / 2, HdVe.pha(mau, 0xFFFFFFFF, 0.25f), HdVe.pha(mau, 0xFF000000, 0.3f));
        HdVe.vienTron(c, x - w / 2, y - h / 2, x + w / 2, y + h / 2, h / 2, 0.8f, 0xD0FFFFFF);
        chu(c, ten, x, y + 3.6f * k, 10f * k, 0xFFFFFFFF, 0xA0000000, 2);
        c.restoreToCount(sv);
        dangHoiChieu = true;
    }

    // --- chat thu gọn
    static int soChatDaXem;

    /** Graphics của khung đang vẽ (veChat chỉ nhận Canvas) */
    static Graphics gVe;

    static void veChat(Canvas c) {
        Vector ls = HdCau.lichSuChat();
        int n = ls == null ? 0 : ls.size();
        int bn = N_CHAT_BONG;
        float bx = NX[bn], by = NY[bn], br = NR[bn];
        if (chatMo) soChatDaXem = n;
        nenNut(c, bx, by, br * nhun(bn, System.currentTimeMillis()), 0xFFFFFFFF, 0xFFEEF2F8, chatMo);
        veIcon(gVe, c, N_CHAT_BONG, bx, by, br);
        if (!chatMo) {
            int moi = n - soChatDaXem;
            if (moi > 0) {
                P.setColor(0xFFE3350D);
                c.drawCircle(bx + br * 0.75f, by - br * 0.75f, 4.5f, P);
                chu(c, moi > 9 ? "9+" : String.valueOf(moi), bx + br * 0.75f, by - br * 0.75f + 2.2f, 6f, 0xFFFFFFFF, 0, 2);
            }
            return;
        }
        if (n == 0) return;
        int dong = Math.min(3, n);
        float x0 = bx + br + 5, w = Math.min(230 * GS[3], W * 0.34f), lh = 11.5f * GS[3];
        float h = dong * lh + 6;
        float y0 = by - h / 2;
        HdVe.tronDoc(c, x0, y0, x0 + w, y0 + h, 8, 0x9A222C36, 0xA0222C36);
        float co = 8f * GS[3];
        for (int i = 0; i < dong; i++) {
            Object o = ls.elementAt(n - dong + i);
            if (!(o instanceof ChatInfo)) continue;
            ChatInfo ci = (ChatInfo) o;
            float y = y0 + 3 + (i + 1) * lh - 3;
            float x = x0 + 6;
            String ten = ci.name == null ? "" : ci.name;
            if (ten.length() > 0) {
                String t2 = ten + ": ";
                chu(c, t2, x, y, co, 0xFFFFD23A, 0x90000000, 0);
                TXT.setTextSize(co);
                x += TXT.measureText(t2);
            }
            String nd = ci.content == null ? "" : game.pokemon.ChuPokemon.D(ci.content);
            chu(c, catChu(nd, co, x0 + w - 6 - x), x, y, co, 0xFFFFFFFF, 0x90000000, 0);
        }
    }

    static boolean trongChat(float x, float y) {
        if (!chatMo) return false;
        Vector ls = HdCau.lichSuChat();
        if (ls == null || ls.size() == 0) return false;
        int bn = N_CHAT_BONG;
        float x0 = NX[bn] + NR[bn] + 5, w = Math.min(230 * GS[3], W * 0.34f);
        float h = Math.min(3, ls.size()) * 11.5f * GS[3] + 6;
        return x >= x0 && x <= x0 + w && Math.abs(y - NY[bn]) <= h / 2;
    }

    // --- cần điều khiển
    static boolean jHoatDong;
    static float jcx, jcy, jkx, jky;
    static int jPhim;
    static int jPtr = -1;

    static void veCanDieuKhien(Canvas c) {
        float cx = jHoatDong ? jcx : jx0, cy = jHoatDong ? jcy : jy0;
        int aNen = jHoatDong ? 0x80 : 0x40;
        P.setShader(null);
        P.setColor(aNen << 24 | 0x0E1D38);
        c.drawCircle(cx, cy, jr, P);
        PV.setColor((jHoatDong ? 0xA0 : 0x50) << 24 | 0xA9C8FF);
        PV.setStrokeWidth(1.1f);
        c.drawCircle(cx, cy, jr - 0.6f, PV);
        // 4 mũi hướng (sáng hướng đang đi)
        for (int i = 0; i < 4; i++) {
            int ma = i == 0 ? -1 : i == 1 ? -4 : i == 2 ? -2 : -3;
            float ax = i == 1 ? 1 : i == 3 ? -1 : 0;
            float ay = i == 0 ? -1 : i == 2 ? 1 : 0;
            float d = jr * 0.78f;
            float px = cx + ax * d, py = cy + ay * d;
            PATH.reset();
            PATH.moveTo(px + ax * 3.5f, py + ay * 3.5f);
            PATH.lineTo(px - ay * 4f - ax * 1.5f, py + ax * 4f - ay * 1.5f);
            PATH.lineTo(px + ay * 4f - ax * 1.5f, py - ax * 4f - ay * 1.5f);
            PATH.close();
            P.setColor(jPhim == ma ? 0xFFFFD23A : (jHoatDong ? 0xB0FFFFFF : 0x60FFFFFF));
            c.drawPath(PATH, P);
        }
        float kx = jHoatDong ? jkx : cx, ky = jHoatDong ? jky : cy, kr = jr * 0.42f;
        P.setColor(0x40000000);
        c.drawCircle(kx, ky + 1.5f, kr + 0.5f, P);
        P.setShader(HdVe.doc(jHoatDong ? 0xFFFFE066 : 0x90FFE066, jHoatDong ? 0xFFFF8A1A : 0x90FF8A1A, ky - kr, ky + kr));
        c.drawCircle(kx, ky, kr, P);
        P.setShader(null);
        PV.setColor(jHoatDong ? 0xFFFFFFFF : 0x80FFFFFF);
        PV.setStrokeWidth(1f);
        c.drawCircle(kx, ky, kr - 0.5f, PV);
    }

    static boolean trongVungCan(float x, float y) {
        return x < W * 0.42f && y > H * 0.30f;
    }

    // ------------------------------------------------------------------ chỉnh bố cục
    static final String[] TEN_NHOM = {"Cần điều khiển", "Nút đánh + chiêu", "Ô thuốc", "Khung chat"};
    static final RectF[] NUT_CS = {new RectF(), new RectF(), new RectF(), new RectF(), new RectF(), new RectF()};
    static final String[] TEN_CS = {"−", "+", "Mặc định", "Hiệu ứng", "Rung", "Xong"};

    static void veChinhSua(Canvas c) {
        // khung quanh từng nhóm
        for (int i = 0; i < NHOM; i++) {
            RectF r = khungNhom(i);
            P.setColor(i == nhomChon ? 0x30FFCB05 : 0x18FFFFFF);
            c.drawRoundRect(r, 10, 10, P);
            PV.setColor(i == nhomChon ? 0xFFFFCB05 : 0x90FFFFFF);
            PV.setStrokeWidth(1f);
            PV.setPathEffect(new android.graphics.DashPathEffect(new float[]{4, 3}, 0));
            c.drawRoundRect(r, 10, 10, PV);
            PV.setPathEffect(null);
            chu(c, TEN_NHOM[i], r.centerX(), r.top - 3, 7.5f, 0xFFFFFFFF, 0xC0000000, 2);
        }
        // bảng nút chỉnh giữa màn
        float w = 340, h = 84;
        float x0 = (W - w) / 2, y0 = H * 0.26f;
        HdVe.bangKinh(c, x0, y0, x0 + w, y0 + h, 12);
        chu(c, "Chỉnh nút: kéo để dời, − / + đổi cỡ (" + TEN_NHOM[nhomChon] + ")", W / 2f, y0 + 13, 8f, 0xFFFFFFFF, 0, 2);
        // nhãn độ tuổi 12+ (jar vẽ đè thẻ trạng thái; trong game HD đặt ở đây)
        HdVe.tronDoc(c, x0 + w - 30, y0 + 5, x0 + w - 8, y0 + 15, 3, 0xFFF4613A, 0xFFB8260A);
        chu(c, "12+", x0 + w - 19, y0 + 12.8f, 7f, 0xFFFFFFFF, 0, 2);
        float bw = (w - 14) / (float) NUT_CS.length;
        for (int i = 0; i < NUT_CS.length; i++) {
            RectF r = NUT_CS[i];
            r.set(x0 + 7 + i * bw + 1.5f, y0 + 20, x0 + 7 + (i + 1) * bw - 1.5f, y0 + 47);
            boolean bat = (i == 3 && hieuUngCao) || (i == 4 && rung);
            HdVe.tronDoc(c, r.left, r.top, r.right, r.bottom, 8, i == 5 ? 0xFF60D070 : bat ? 0xFF6FA6EA : 0xFF2A4C80,
                    i == 5 ? 0xFF2E8A3A : bat ? 0xFF3D7DCA : 0xFF15284A);
            HdVe.vienTron(c, r.left, r.top, r.right, r.bottom, 8, 0.7f, 0x80FFFFFF);
            String t = TEN_CS[i];
            if (i == 3) t = hieuUngCao ? "Hiệu ứng cao" : "Hiệu ứng thấp";
            if (i == 4) t = rung ? "Rung: bật" : "Rung: tắt";
            float co = i < 2 ? 13f : 6.6f;
            chu(c, t, r.centerX(), r.centerY() + co * 0.36f, co, 0xFFFFFFFF, 0, 2);
        }
        // 2 thanh âm lượng riêng: nhạc nền / hiệu ứng (nhân với âm lượng chung của game)
        for (int i = 0; i < 2; i++) {
            RectF r = THANH_AM[i];
            float sx = x0 + 10 + i * (w / 2), sw = w / 2 - 20;
            r.set(sx + 46, y0 + 58, sx + sw, y0 + 72);
            float v = i == 0 ? AmThanhHd.amNhac : AmThanhHd.amHieuUng;
            chu(c, i == 0 ? "Nhạc" : "Hiệu ứng", sx, r.centerY() + 3, 8f, 0xFFFFFFFF, 0, 0);
            HdVe.veThanh(c, r.left, r.centerY() - 3, r.width(), 6, v, i == 0 ? 0xFF6FA6EA : 0xFFFFCB05);
            float kx = r.left + r.width() * v;
            P.setColor(0xFFFFFFFF);
            c.drawCircle(kx, r.centerY(), 5.5f, P);
            PV.setColor(0xFF1E2A44);
            PV.setStrokeWidth(1f);
            c.drawCircle(kx, r.centerY(), 5.5f, PV);
        }
    }

    static final RectF[] THANH_AM = {new RectF(), new RectF()};
    static int keoAm = -1;

    static boolean chamAm(int kieu, int id, float x, float y) {
        if (kieu == 0) {
            for (int i = 0; i < 2; i++) {
                RectF r = THANH_AM[i];
                if (x >= r.left - 8 && x <= r.right + 8 && y >= r.top - 8 && y <= r.bottom + 8) {
                    keoAm = i;
                    keoPtr = id;
                    datAm(x);
                    return true;
                }
            }
            return false;
        }
        if (keoAm < 0 || id != keoPtr) return false;
        datAm(x);
        if (kieu == 2) {
            keoAm = -1;
            keoPtr = -1;
            AmThanhHd.luu();
        }
        return true;
    }

    static void datAm(float x) {
        RectF r = THANH_AM[keoAm];
        float v = Math.max(0f, Math.min(1f, (x - r.left) / r.width()));
        if (keoAm == 0) AmThanhHd.amNhac = v;
        else AmThanhHd.amHieuUng = v;
        AmThanhHd.luu();
    }

    static RectF khungNhom(int i) {
        RectF r = new RectF();
        switch (i) {
            case 0:
                r.set(jx0 - jr - 4, jy0 - jr - 4, jx0 + jr + 4, jy0 + jr + 4);
                break;
            case 1: {
                float l = NX[N_C0] - NR[N_C0] - 4, t = NY[N_MUC_TIEU] - NR[N_MUC_TIEU] - 4;
                r.set(l, t, NX[N_DANH] + NR[N_DANH] + 6, NY[N_DOI_BO] + NR[N_DOI_BO] + 6);
                break;
            }
            case 2:
                r.set(NX[N_PP] - NR[N_PP] - 4, NY[N_HP] - NR[N_HP] - 4, NX[N_HP] + NR[N_HP] + 4, NY[N_HP] + NR[N_HP] + 4);
                break;
            default:
                r.set(NX[N_CHAT_BONG] - NR[N_CHAT_BONG] - 4, NY[N_CHAT_BONG] - 22, NX[N_CHAT_BONG] + 120 * GS[3], NY[N_CHAT_BONG] + 22);
                break;
        }
        return r;
    }

    static int nhomKeo = -1;
    static float keoX, keoY;
    static int keoPtr = -1;

    static void chamChinhSua(int kieu, int id, float x, float y) {
        if (chamAm(kieu, id, x, y)) return;
        if (kieu == 0) {
            for (int i = 0; i < NUT_CS.length; i++) {
                if (NUT_CS[i].contains(x, y)) {
                    bamChinhSua(i);
                    return;
                }
            }
            for (int i = NHOM - 1; i >= 0; i--) {
                if (khungNhom(i).contains(x, y)) {
                    nhomChon = i;
                    nhomKeo = i;
                    keoPtr = id;
                    keoX = x;
                    keoY = y;
                    if (rung) NenAndroid.rungNhe();
                    return;
                }
            }
        } else if (kieu == 1 && id == keoPtr && nhomKeo >= 0) {
            GX[nhomKeo] += x - keoX;
            GY[nhomKeo] += y - keoY;
            keoX = x;
            keoY = y;
        } else if (kieu == 2 && id == keoPtr) {
            nhomKeo = -1;
            keoPtr = -1;
            luu();
        }
    }

    static void bamChinhSua(int i) {
        if (rung) NenAndroid.rungNhe();
        switch (i) {
            case 0:
                GS[nhomChon] = Math.max(0.7f, GS[nhomChon] - 0.1f);
                break;
            case 1:
                GS[nhomChon] = Math.min(1.6f, GS[nhomChon] + 0.1f);
                break;
            case 2:
                for (int k = 0; k < NHOM; k++) {
                    GX[k] = 0;
                    GY[k] = 0;
                    GS[k] = 1f;
                }
                break;
            case 3:
                hieuUngCao = !hieuUngCao;
                break;
            case 4:
                rung = !rung;
                break;
            default:
                chinhSua = false;
                break;
        }
        luu();
    }

    // ------------------------------------------------------------------ chạm (luồng sự kiện)
    /** Con trỏ đang chuyển cho game (J2ME chỉ có 1 con trỏ). */
    static int gamePtr = -1;
    /** con trỏ -> nút đang giữ (-1 không) */
    static final int[] PTR_NUT = new int[16];
    static final float[] PTR_X0 = new float[16];
    static final float[] PTR_Y0 = new float[16];
    static final long[] PTR_T0 = new long[16];
    /** 1 = ứng viên cần điều khiển (chưa kéo), 2 = cần đang chạy */
    static final int[] PTR_CAN = new int[16];
    static long tLapDanh;

    static {
        for (int i = 0; i < 16; i++) PTR_NUT[i] = -1;
        for (int i = 0; i < SO_NUT; i++) NPTR[i] = -1;
    }

    /** Gọi trên luồng UI: chuyển sự kiện chạm (toạ độ màn thật) sang luồng sự kiện game. */
    public static void chamAndroid(MotionEvent e) {
        int a = e.getActionMasked();
        // cùng phép biến đổi khung cố định 640x360 với NenAndroid.cham (hệ số có thể lẻ, có viền)
        if (a == MotionEvent.ACTION_MOVE) {
            for (int i = 0; i < e.getPointerCount(); i++) {
                gui(1, e.getPointerId(i) & 15, NenAndroid.logicX(e.getX(i)), NenAndroid.logicY(e.getY(i)));
            }
            return;
        }
        if (a == MotionEvent.ACTION_CANCEL) {
            for (int i = 0; i < e.getPointerCount(); i++) {
                gui(2, e.getPointerId(i) & 15, NenAndroid.logicX(e.getX(i)), NenAndroid.logicY(e.getY(i)));
            }
            return;
        }
        int i = e.getActionIndex();
        int kieu = a == MotionEvent.ACTION_DOWN || a == MotionEvent.ACTION_POINTER_DOWN ? 0
                : a == MotionEvent.ACTION_UP || a == MotionEvent.ACTION_POINTER_UP ? 2 : -1;
        if (kieu < 0) return;
        gui(kieu, e.getPointerId(i) & 15, NenAndroid.logicX(e.getX(i)), NenAndroid.logicY(e.getY(i)));
    }

    static void gui(final int kieu, final int id, final float x, final float y) {
        NenAndroid.luong().them(new Runnable() {
            public void run() {
                try {
                    cham(kieu, id, x, y);
                } catch (Throwable e) {
                    android.util.Log.e(NenAndroid.TAG, "loi HUD cham", e);
                }
            }
        });
    }

    static void cham(int kieu, int id, float x, float y) {
        long now = System.currentTimeMillis();
        if (kieu == 0) {
            PTR_X0[id] = x;
            PTR_Y0[id] = y;
            PTR_T0[id] = now;
            PTR_NUT[id] = -1;
            PTR_CAN[id] = 0;
            if (!hien && HdNut.cham(0, x, y)) {
                PTR_NUT[id] = N_HDNUT;
                return;
            }
            if (hien && chinhSua) {
                chamChinhSua(0, id, x, y);
                return;
            }
            if (hien && alpha > 0.5f) {
                int n = nutTai(x, y);
                if (n >= 0) {
                    batNut(n, id, now);
                    return;
                }
                if (trongChat(x, y)) {
                    PTR_NUT[id] = N_CHAT;
                    NPTR[N_CHAT] = id;
                    return;
                }
                if (trongThe(x, y)) {
                    // thẻ trạng thái trái trên (chân dung): thả trong thẻ = hồ sơ nhân vật (HanhDong.moHoSo, chung)
                    PTR_NUT[id] = N_THE;
                    if (rung) NenAndroid.rungNhe();
                    return;
                }
                if (trongMiniMap(x, y)) {
                    PTR_NUT[id] = N_MINIMAP;
                    NPTR[N_MINIMAP] = id;
                    NNHAN[N_MINIMAP] = now;
                    if (rung) NenAndroid.rungNhe();
                    return;
                }
                if (jPtr < 0 && trongVungCan(x, y)) {
                    PTR_CAN[id] = 1;
                    return;
                }
            }
            if (gamePtr < 0) {
                gamePtr = id;
                chamGame(0, (int) x, (int) y);
            }
            return;
        }
        if (PTR_NUT[id] == N_HDNUT) {
            HdNut.cham(kieu, x, y);
            if (kieu == 2) PTR_NUT[id] = -1;
            return;
        }
        if (kieu == 1) {
            if (chinhSua && keoPtr == id) {
                chamChinhSua(1, id, x, y);
                return;
            }
            if (PTR_CAN[id] == 1) {
                float dx = x - PTR_X0[id], dy = y - PTR_Y0[id];
                if (dx * dx + dy * dy > 36) {
                    PTR_CAN[id] = 2;
                    jPtr = id;
                    jHoatDong = true;
                    jcx = Math.max(L + jr, Math.min(W * 0.45f, PTR_X0[id]));
                    jcy = Math.max(H * 0.25f + jr, Math.min(B - jr * 0.6f, PTR_Y0[id]));
                    if (rung) NenAndroid.rungNhe();
                }
            }
            if (PTR_CAN[id] == 2) {
                capNhatCan(x, y);
                return;
            }
            if (id == gamePtr) chamGame(1, (int) x, (int) y);
            return;
        }
        // thả
        if (chinhSua && keoPtr == id) {
            chamChinhSua(2, id, x, y);
            return;
        }
        if (PTR_CAN[id] == 2) {
            thaCan();
            PTR_CAN[id] = 0;
            return;
        }
        if (PTR_CAN[id] == 1) {
            // chạm nhanh trong vùng cần (không kéo): như chạm thường của game (đi tới / chọn NPC)
            PTR_CAN[id] = 0;
            if (gamePtr < 0) {
                chamGame(0, (int) x, (int) y);
                thaTre((int) x, (int) y, System.currentTimeMillis());
            }
            return;
        }
        int n = PTR_NUT[id];
        if (n == N_THE) {
            PTR_NUT[id] = -1;
            if (trongThe(x, y)) game.pokemon.HanhDong.moHoSo();
            return;
        }
        if (n >= 0) {
            PTR_NUT[id] = -1;
            thaNut(n, x, y, now);
            return;
        }
        if (id == gamePtr) {
            thaTre((int) x, (int) y, PTR_T0[id]);
            gamePtr = -1;
        }
    }

    static final int N_HDNUT = 99;
    /** con trỏ đang giữ thẻ trạng thái (không thuộc mảng nút) */
    static final int N_THE = 98;

    /** Vùng chạm thẻ trạng thái (toạ độ logic, đã tránh vùng an toàn qua L / T). */
    static boolean trongThe(float x, float y) {
        return x >= L - 2 && x <= L + RONG_THE + 2 && y >= T - 2 && y <= T + CAO_THE + 2;
    }

    /**
     * Thả cho game: game đọc chạm theo nhịp 40 ms (isPointerDown rồi isPointerClick) - chạm quá nhanh
     * (nhấn + thả trong cùng một nhịp) bị vài màn bỏ qua; giữ "đang nhấn" tối thiểu ~2 nhịp.
     */
    static void thaTre(final int x, final int y, long t0) {
        long d = System.currentTimeMillis() - t0;
        if (d >= 90) {
            chamGame(2, x, y);
            return;
        }
        final long cho = 90 - d;
        new Thread("HTBB-tha") {
            public void run() {
                try {
                    Thread.sleep(cho);
                } catch (InterruptedException e) {
                    // bỏ qua
                }
                NenAndroid.luong().them(new Runnable() {
                    public void run() {
                        chamGame(2, x, y);
                    }
                });
            }
        }.start();
    }

    /** Chuyển chạm cho game (đổi toạ độ nếu cửa sổ jar đang phóng - HdPhong). */
    static void chamGame(int kieu, int x, int y) {
        NenAndroid.chamNgay(kieu, HdPhong.doiX(x, y), HdPhong.doiY(x, y));
    }

    static int nutTai(float x, float y) {
        int tot = -1;
        float dTot = 1e9f;
        GameScr gs = game.render.Canvas.gameScr;
        for (int n = 0; n < SO_NUT; n++) {
            if (n == N_MINIMAP) continue;
            if (n == N_MUC_TIEU && gs != null && gs.focusedActor == null && gs.actors.size() <= 1) continue;
            float dx = x - NX[n], dy = y - NY[n];
            float d = dx * dx + dy * dy;
            float r = NR[n] + (NR[n] < 15 ? 5 : 3);
            if (d <= r * r && d < dTot) {
                dTot = d;
                tot = n;
            }
        }
        return tot;
    }

    static int phimCua(int n) {
        switch (n) {
            case N_DANH: return 53;
            case N_C0: return 49;
            case N_C1: return 51;
            case N_C3: return 55;
            case N_C4: return 57;
            case N_DOI_BO: return 48;
            case N_MUC_TIEU: return -7;
            default: return 0;
        }
    }

    /** Ô quickslot của nút chiêu nhỏ (-1 = không phải nút chiêu nhỏ). */
    static int oCua(int n) {
        switch (n) {
            case N_C0: return 0;
            case N_C1: return 1;
            case N_C3: return 3;
            case N_C4: return 4;
            default: return -1;
        }
    }

    /** nút chiêu vừa mở bảng gán chiêu lúc nhấn (ô trống "+"): lúc thả không gửi phím */
    static final boolean[] MO_BANG = new boolean[SO_NUT];

    static void batNut(int n, int id, long now) {
        PTR_NUT[id] = n;
        NPTR[n] = id;
        NNHAN[n] = now;
        if (rung) NenAndroid.rungNhe();
        int ma = phimCua(n);
        int o = oCua(n);
        MO_BANG[n] = false;
        GameScr gs0 = game.render.Canvas.gameScr;
        if (n == N_DANH && gs0 != null && !(gs0.focusedActor != null && gs0.focusedActor.catagory == 2)) {
            o = 2;   // nút đánh lớn = ô 5 (giữa); ô trống thì cũng mở bảng gán (giữ lâu = đánh lặp, không chỉnh)
        }
        if (o >= 0 && MainChar.quickSlot != null) {
            QuickSlot qs = MainChar.quickSlot[GameScr.indexTabSlot][o];
            if (qs == null || qs.quickslotType < 0) {
                // ô trống (dấu +): mở bảng gán chiêu (game.pokemon.ChonChieuNhanh, chung mọi bản)
                MO_BANG[n] = game.pokemon.ChonChieuNhanh.chamO(o, false);
                if (MO_BANG[n]) return;
            }
        }
        if (ma != 0) {
            NenAndroid.phimNgay(0, ma);
            if (n == N_DANH) tLapDanh = now;
        } else if (n == N_HP || n == N_PP) {
            GameScr gs = game.render.Canvas.gameScr;
            int loai = loaiThuoc(gs.mainChar, n == N_HP);
            if (loai >= 0) {
                try {
                    gs.doUsePotion(loai);
                } catch (Throwable e) {
                    // bỏ qua
                }
            }
        }
    }

    static void thaNut(int n, float x, float y, long now) {
        long giu = now - NNHAN[n];
        NPTR[n] = -1;
        NNHAN[n] = now;
        float dx = x - NX[n], dy = y - NY[n];
        float r = NR[n] + 10;
        int ma = phimCua(n);
        int o = oCua(n);
        if (MO_BANG[n]) {
            MO_BANG[n] = false;
            return;
        }
        if (ma != 0) {
            NenAndroid.phimNgay(2, ma);
            // giữ lâu nút chiêu nhỏ (vẫn trong nút) = bảng chỉnh ô: đổi chiêu / bỏ trống / dời trái phải
            if (o >= 0 && giu >= game.pokemon.OChieuNhanh.GIU_LAU_MS && dx * dx + dy * dy <= r * r) {
                game.pokemon.ChonChieuNhanh.chamO(o, true);
            }
            return;
        }
        if (n == N_MINIMAP ? !trongMiniMap(x, y) : dx * dx + dy * dy > r * r) return;   // kéo ra ngoài = huỷ
        switch (n) {
            case N_MENU:
                // 0.9.3: menu lưới ô to (mọi việc thường dùng = 2 chạm); ô "Khác" mở menu chữ cũ
                game.pokemon.HanhDong.moMenu();
                break;
            case N_TU_DONG:
                // 0.9.6: chạm ngắn = bật / tắt Tự động; nhấn giữ >= 0,45 s = bảng cài đặt Tự động (cũng ở Menu)
                if (giu >= 450 || !HdCau.batTatTuDong()) {
                    if (!HdCau.moTuDong()) {
                        NenAndroid.phimNgay(0, 35);
                        NenAndroid.phimNgay(2, 35);
                    }
                }
                break;
            case N_CHAT:
                HdCau.moChat();
                break;
            case N_THEM:
                game.pokemon.ManNhanVat.moHanhTrang();
                break;
            case N_CHINH:
                game.pokemon.ManNhiemVu.mo();
                break;
            case N_CHAT_BONG:
                chatMo = !chatMo;
                luu();
                break;
            case N_MINIMAP:
                HdCau.moBanDoLon();
                break;
            default:
                break;
        }
    }

    /** Nút đánh giữ = bấm lặp (như bấm liên tục phím 5). Gọi mỗi khung vẽ. */
    static void lapDanh(long now) {
        if (NPTR[N_DANH] >= 0 && now - tLapDanh > 380) {
            tLapDanh = now;
            NenAndroid.phimNgay(2, 53);
            NenAndroid.phimNgay(0, 53);
        }
    }

    static void capNhatCan(float x, float y) {
        float dx = x - jcx, dy = y - jcy;
        float d = (float) Math.sqrt(dx * dx + dy * dy);
        float m = jr * 0.72f;
        if (d > m) {
            // cần trôi theo ngón khi kéo quá xa (cần nổi)
            float k = (d - m) / d;
            jcx += dx * k * 0.6f;
            jcy += dy * k * 0.6f;
            dx = x - jcx;
            dy = y - jcy;
            d = (float) Math.sqrt(dx * dx + dy * dy);
            if (d > m) {
                dx *= m / d;
                dy *= m / d;
            }
        }
        jkx = jcx + dx;
        jky = jcy + dy;
        int ma = 0;
        if (d > jr * 0.22f) {
            if (Math.abs(dx) > Math.abs(dy)) ma = dx > 0 ? -4 : -3;
            else ma = dy > 0 ? -2 : -1;
        }
        if (ma != jPhim) {
            if (jPhim != 0) NenAndroid.phimNgay(2, jPhim);
            jPhim = ma;
            if (ma != 0) NenAndroid.phimNgay(0, ma);
        }
    }

    static void thaCan() {
        if (jPhim != 0) NenAndroid.phimNgay(2, jPhim);
        jPhim = 0;
        jPtr = -1;
        jHoatDong = false;
    }

    /** HUD bị ẩn (mở menu / hộp thoại): thả mọi phím đang giữ. */
    static void thaHet() {
        if (jHoatDong) thaCan();
        for (int i = 0; i < 16; i++) {
            if (PTR_CAN[i] != 0) PTR_CAN[i] = 0;
            int n = PTR_NUT[i];
            if (n >= 0 && n < SO_NUT) {
                PTR_NUT[i] = -1;
                NPTR[n] = -1;
                int ma = phimCua(n);
                if (ma != 0) NenAndroid.phimNgay(2, ma);
            }
        }
        chinhSua = false;
    }
}
