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

    /** Túi sinh thái (hệ sinh thái server, NPC ảo -528, lựa chọn 4 của menu gốc). */
    static void moTuiSinhThai() {
        game.networklogic.GameService.gI().doMenuOption(-528, (byte) 0, 4);
    }

    /** Bản Android: menu chính giữ kiểu cũ (biểu tượng / tiêu đề chỉ bản jar). */
    static int rongHinhMuc(String muc) {
        return 0;
    }

    static void veHinhMuc(Object g, int x, int y, String muc) {
    }

    static void veKhungMenu(Object g, int x, int y, int w, int h, String tieuDe, int tong, int tu, int thay) {
    }

    static void conTroMenu(Object g, int x, int y, int h) {
    }

    /** Cài đặt -> Giấy phép: bản Android cần màn ghi công riêng (GHI_CONG.txt trong APK) - chưa có thì bỏ qua. */
    static void moGiayPhep() {
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

    /** Tự động đánh chính thức: bản Android NQSH cũ mở bằng menu (chưa có bảng kiểu Pokémon). */
    static void moBangTuDong() {
        java.util.Vector v = new java.util.Vector();
        v.addElement(new game.model.Command(TuDong.dang ? "Dừng tự động" : "Bắt đầu tự động", new game.model.IAction() {
            public void perform() {
                TuDong.batTat();
            }
        }));
        PauseMenu.moMenuGiuChon(v, 3, 0);
        TuDong.xinTrangThai();
    }

    static boolean trongNutNhanh(int x, int y) {
        return AutoGuard.trongNut(x, y);
    }

    static void veThanhTuDong(Object g, String s, int y) {
        game.core.j2me.Graphics gr = (game.core.j2me.Graphics) g;
        Font.normalFont[0].drawString(gr, s, 6, y, 0);
    }

    static String tieuDeBanDoLon(String macDinh) {
        return macDinh;
    }
}
