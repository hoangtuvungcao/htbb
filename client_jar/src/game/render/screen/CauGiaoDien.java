package game.render.screen;

import game.render.Canvas;

/**
 * Cầu public cho giao diện mới (game.pokemon.ManMenu / ManNhanVat): mở các bảng package-private của
 * game.render.screen (Menu nhanh / cài đặt, Tự động đánh, ô chat) bằng một chạm.
 */
public final class CauGiaoDien {
    private CauGiaoDien() {
    }

    /** Cài đặt nhanh (âm thanh, nút) - MenuNhanh. */
    public static void moCaiDat() {
        MenuNhanh.moBang();
    }

    /** Bảng Tự động đánh chính thức. */
    public static void moTuDong() {
        BangTuDong.mo();
    }

    /** Mở ô chat thường (như mục Chat của menu phím #), con trỏ gõ ở ô chat. */
    public static void moChat() {
        GameScr gs = Canvas.gameScr;
        if (gs == null) {
            return;
        }
        if (Canvas.currentScreen != gs) {
            gs.switchToMe();
        }
        GameScr.typechat = GameScr.CHAT_NORMAL;
        gs.chatMode = true;
        gs.tfChat.setIputType(0);
        gs.tfChat.isFocus = true;
        try {
            if (!TField.isQwerty) {
                gs.tfChat.doChangeToTextBox();   // máy cảm ứng: bàn phím hệ thống
            }
        } catch (Throwable e) {
            // ô chat jar vẫn hiện, chạm vào để nhập
        }
    }
}
