package game.render.screen;

import game.networklogic.GameService;
import game.render.Sound;

/**
 * Lớp đệm nền tảng - bản Android (APK TeaMobi). Bản J2ME: Jar276/src/.../Nen.java.
 * Code mod dùng chung (chép từ Jar276 qua port-android.py) gọi các hàm này ở chỗ hai nền tảng khác nhau.
 */
final class Nen {
    private Nen() {
    }

    static boolean coAmThanh() {
        return true;
    }

    static boolean amThanhBat() {
        return Sound.isSound;
    }

    static void datAmThanh(boolean bat) {
        Sound.isSound = bat;
        if (!bat) {
            Sound.stopAll();
        }
    }

    /** Mở hết 5 rương vận may (NPC -10): APK gọi openLuckyBox, -100 = không gửi byte thứ hai. */
    static void moRuongVanMay(GameService gameService) {
        gameService.openLuckyBox((byte)2, (byte)-100);
    }

    /** Màn mở rương: APK là MoQuaScr (J2ME là MiniGame). */
    static boolean laManRuong(Screen screen) {
        return screen != null && screen == MoQuaScr.me;
    }
}
