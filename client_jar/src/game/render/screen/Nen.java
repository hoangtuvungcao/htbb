package game.render.screen;

import game.model.Sound;
import game.render.Canvas;
import game.networklogic.GameService;

/**
 * Lớp đệm nền tảng - bản J2ME. Code mod (PauseMenu, AutoVanMay...) dùng chung cho jar và Android:
 * chỗ nào hai nền tảng khác nhau thật thì gọi qua đây. Bản Android: Android277/src/.../Nen.java.
 */
final class Nen {
    private Nen() {
    }

    /** Cỡ nút mod nổi (px): nhỏ cho màn giả lập J2ME. */
    static final int CO_NUT = 38;

    /** Nút mod nổi trên màn game (AutoGuard): chỉ máy cảm ứng (J2ME Loader, máy Java cảm ứng); máy bấm phím không vẽ. */
    static boolean coNutUp() {
        return Canvas.instance != null && Canvas.instance.hasPointerEvents();
    }

    static boolean coAmThanh() {
        return Sound.me != null;
    }

    static boolean amThanhBat() {
        return Sound.me != null && Sound.me.isPlaying == 1;
    }

    static void datAmThanh(boolean bat) {
        if (Sound.me == null) {
            return;
        }
        Sound.me.isPlaying = bat ? 1 : 0;
        Sound.me.setVolume(bat ? 7 : 0);
    }

    /** Mở hết 5 rương vận may (NPC -10) - server: MapQuaTang.doOpenLuckyBox. */
    static void moRuongVanMay(GameService gameService) {
        gameService.playMiniGame((byte)2, (byte)100);
    }

    /** Màn mở rương (mini game) đang hiện? */
    static boolean laManRuong(Screen screen) {
        return screen == MiniGame.gI();
    }
}
