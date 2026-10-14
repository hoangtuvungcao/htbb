package game.render.screen;

import game.model.Sound;
import game.networklogic.GameService;

/**
 * Lớp đệm nền tảng - bản J2ME. Code mod (PauseMenu, AutoVanMay...) dùng chung cho jar và Android:
 * chỗ nào hai nền tảng khác nhau thật thì gọi qua đây. Bản Android: Android277/src/.../Nen.java.
 */
final class Nen {
    private Nen() {
    }

    /** Nút UP nổi trên màn game (AutoGuard): máy J2ME bấm phím, không có màn cảm ứng -> không vẽ. */
    static boolean coNutUp() {
        return false;
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
