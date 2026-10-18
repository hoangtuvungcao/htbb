package game.render.screen;

import android.graphics.Paint;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * Cầu nối HUD HD của APK (com.htbb.pokemon.hd.Hud) với các phần package-private của code jar trong
 * game.render.screen (lịch sử chat, Menu nhanh, bảng Tự động, ô chat). Chỉ có trong bản APK
 * (client_apk/pokemon/src/main/java); KHÔNG đổi logic - chỉ gọi đúng các hàm mà nút / phím của jar gọi.
 */
public final class HdCau {
    private HdCau() {
    }

    /** Lịch sử chat màn chơi (GameScr.chatHistory: ChatInfo name/content). */
    public static Vector lichSuChat() {
        GameScr gs = game.render.Canvas.gameScr;
        return gs == null ? null : gs.chatHistory;
    }

    /** Mở ô chat như mục "Chat" của menu phím # (GameScr.updateKey) rồi mở luôn bàn phím hệ thống. */
    public static void moChat() {
        GameScr gs = game.render.Canvas.gameScr;
        if (gs == null) return;
        GameScr.typechat = GameScr.CHAT_NORMAL;
        gs.chatMode = true;
        gs.tfChat.setIputType(0);
        gs.tfChat.isFocus = true;
        try {
            gs.tfChat.doChangeToTextBox();
        } catch (Throwable e) {
            // không mở được hộp nhập: ô chat jar vẫn hiện, chạm vào để nhập
        }
    }

    /** Tên vùng (Kanto / Johto) như jar vẽ dưới bản đồ nhỏ; null = không có. */
    public static String tenVung() {
        GameScr gs = game.render.Canvas.gameScr;
        try {
            if (gs != null && gs.mainChar.nationID > -1) return GameScr.nameCountry[gs.mainChar.inCountry];
        } catch (Throwable e) {
            // bỏ qua
        }
        return null;
    }

    /** Bản đồ lớn của vùng (như mục Cài đặt -> Bản đồ lớn của Menu: PauseMenu.doBigMap). */
    public static void moBanDoLon() {
        try {
            java.lang.reflect.Method m = PauseMenu.class.getDeclaredMethod("doBigMap");
            m.setAccessible(true);
            m.invoke(PauseMenu.gI());
        } catch (Throwable e) {
            android.util.Log.w(javax.microedition.lcdui.NenAndroid.TAG, "khong mo duoc ban do lon", e);
        }
    }

    /** Vẽ điểm nhiệm vụ / điểm câu cá / mốc đua của jar lên bản đồ nhỏ HD (g đã phóng theo ô). */
    public static void veDiemMiniJar(Graphics g) {
        GameScr gs = game.render.Canvas.gameScr;
        if (gs == null) return;
        try {
            gs.paintPosNPCQuest(g);
        } catch (Throwable e) {
            // bỏ qua
        }
        try {
            game.pokemon.SinhThai.veMini(g);
        } catch (Throwable e) {
            // bỏ qua
        }
    }

    /** Nhãn độ tuổi 12+: chỉ ở màn tải / máy chủ / đăng nhập / chọn - tạo nhân vật (trong game: bảng Chỉnh nút). */
    public static boolean hien12() {
        Object m = game.render.Canvas.currentScreen;
        return game.render.Canvas.isLoad || m == null || m instanceof LoginScr || m instanceof ServerListScr
                || m instanceof CharSelectScr || m instanceof CreateCharScr || m instanceof DangKyScr;
    }

    /** Menu nhanh (Poké Ball mép trái của jar). */
    public static void moMenuNhanh() {
        MenuNhanh.moBang();
    }

    /** Bảng / Menu nhanh của jar đang mở (HUD ẩn để không che). */
    public static boolean bangMo() {
        return MenuNhanh.mo || bangTuDongMo();
    }

    private static java.lang.reflect.Field fMoTuDong;
    private static java.lang.reflect.Field fDangTuDong;
    private static java.lang.reflect.Method mMoTuDong;
    private static boolean daTim;

    private static void tim() {
        if (daTim) return;
        daTim = true;
        // "Tự động" chính thức (BangTuDong / TuDong, agent khác đang làm): gọi qua phản chiếu để HUD vẫn
        // build khi lớp đổi tên / chưa có - thiếu thì nút Tự động mở menu phím # (có mục "Tự động đánh")
        try {
            Class<?> c = Class.forName("game.render.screen.BangTuDong");
            mMoTuDong = c.getDeclaredMethod("mo");
            mMoTuDong.setAccessible(true);
            fMoTuDong = c.getDeclaredField("mo");
            fMoTuDong.setAccessible(true);
        } catch (Throwable e) {
            mMoTuDong = null;
        }
        try {
            Class<?> c = Class.forName("game.render.screen.TuDong");
            fDangTuDong = c.getDeclaredField("dang");
            fDangTuDong.setAccessible(true);
        } catch (Throwable e) {
            fDangTuDong = null;
        }
    }

    private static boolean bangTuDongMo() {
        tim();
        try {
            return fMoTuDong != null && fMoTuDong.getBoolean(null);
        } catch (Throwable e) {
            return false;
        }
    }

    /** Móc nút "Tự động" của HUD: true = đã mở bảng Tự động chính thức. */
    public static boolean moTuDong() {
        tim();
        if (mMoTuDong == null) return false;
        try {
            mMoTuDong.invoke(null);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    /** Tự động đánh đang chạy (đèn xanh trên nút). */
    public static boolean tuDongDang() {
        tim();
        try {
            return fDangTuDong != null && fDangTuDong.getBoolean(null);
        } catch (Throwable e) {
            return false;
        }
    }

    // ------------------------------------------------------------------ tin máy chủ / ticker dạng toast

    private static final Paint P = new Paint(Paint.ANTI_ALIAS_FLAG);

    /** Tin chạy chữ máy chủ (paintMsgServer): viên kính giữa đáy màn, chữ chạy trong viên. */
    public static void veTinMayChu(Graphics g, String s, int xChu) {
        if (s == null || game.render.Canvas.currentScreen != game.render.Canvas.gameScr || game.pokemon.HopThoaiDuoi.dangBat()
                || game.render.Canvas.gameScr.chatMode) return;
        int w = game.render.Canvas.w;
        int h = game.render.Canvas.h;
        int[] vung = com.htbb.pokemon.hd.Hud.vungToastDuoi();
        int ww = vung[1] - vung[0];
        int x0 = vung[0];
        int hh = 18;
        int y0 = vung[2];
        // chữ chạy từ mép phải màn sang trái như jar; dời gốc để đi qua viên
        int x = xChu - (w - (x0 + ww));
        int tw = Font.arialFontW.getWidth(s);
        if (x > x0 + ww - 8 || x + tw < x0 + 8) return;   // chữ chưa vào / đã ra khỏi viên
        com.htbb.pokemon.hd.Hud.vienKinh(g, x0, y0, ww, hh);
        g.setClip(x0 + 6, y0, ww - 12, hh);
        Font.arialFontW.drawString(g, s, x, y0 + (hh - Font.arialFontW.getHeight()) / 2, 0);
        g.setClip(0, 0, w, h);
        com.htbb.pokemon.hd.Hud.mepMo(g, x0, y0, ww, hh);
    }

    /** Ticker thông báo máy chủ (GameScr.Ticker): dải kính mảnh trên cùng thay ảnh imgNotice. */
    public static void veTicker(Graphics g, String info, int x, int y) {
        int w = game.render.Canvas.w;
        int ww = Math.min(w * 50 / 100, 380);
        int x0 = (w - ww) / 2;
        int y0 = com.htbb.pokemon.hd.Hud.yToastTren();
        int hh = 18;
        com.htbb.pokemon.hd.Hud.vienKinh(g, x0, y0, ww, hh);
        int cx = g.getClipX(), cy = g.getClipY(), cw = g.getClipWidth(), ch = g.getClipHeight();
        g.setClip(x0 + 8, y0, ww - 16, hh);
        int xx = x - (w - (x0 + ww));
        Font.normalFont[0].drawString(g, info, xx, y0 + (hh - Font.normalFont[0].getHeight()) / 2, 0);
        g.setClip(cx, cy, cw, ch);
    }
}
