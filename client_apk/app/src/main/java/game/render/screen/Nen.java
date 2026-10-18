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

    /** Chữ trên nút mod nổi (AutoGuard): bản Android giữ nguyên (bản jar Pokémon đổi từ ngữ). */
    static String nhanNut(String s) {
        return s;
    }

    /** Cỡ nút mod nổi (px). */
    static final int CO_NUT = 52;

    /** Nút UP nổi trên màn game (AutoGuard): bản cảm ứng -> có. */
    static boolean coNutUp() {
        return true;
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

    /** Bản Android giữ cột nút mod cũ (bản jar Pokémon dùng Menu nhanh - xem j2me Nen). */
    static boolean menuNhanhVe(Object g) {
        return false;
    }

    /** Bản Android giữ ảnh bản đồ lớn cũ (bản jar Pokémon vẽ bằng code - xem j2me Nen). */
    static boolean veBanDoLon(Object g, short[][] diem, int hienTai, int w, int h) {
        return false;
    }

    static boolean menuNhanhTick() {
        return false;
    }

    static boolean menuNhanhPhim(int keyCode) {
        return false;
    }

    static int yTrangThai(int macDinh) {
        return macDinh;
    }
}
