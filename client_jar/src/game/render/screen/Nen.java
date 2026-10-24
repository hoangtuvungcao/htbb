package game.render.screen;

import game.model.Sound;
import game.render.Canvas;

/**
 * Lớp đệm nền tảng - bản J2ME. Code mod (PauseMenu, TuDong...) dùng chung cho jar và Android:
 * chỗ nào hai nền tảng khác nhau thật thì gọi qua đây. Bản Android: Android277/src/.../Nen.java.
 */
final class Nen {
    private Nen() {
    }

    /**
     * pokemon: nút mod nổi = MỘT Poké Ball nhỏ sát mép trái mở "Menu nhanh" (MenuNhanh) thay cho cột
     * nút tròn to đè lên màn chơi. true = đã xử lý (AutoGuard không vẽ / không bắt chạm nút cũ).
     */
    static boolean menuNhanhVe(javax.microedition.lcdui.Graphics g) {
        return MenuNhanh.ve(g);
    }

    /** pokemon: "Bản đồ lớn" vẽ bằng code (game.pokemon.BanDoVung) thay ảnh bản đồ kiếm hiệp. true = đã vẽ. */
    static boolean veBanDoLon(Object g, short[][] diem, int hienTai, int w, int h) {
        final int map = game.render.Canvas.gameScr != null ? game.render.Canvas.gameScr.map : -1;
        if (game.pokemon.BanDoVung.chiSoHoenn(map) >= 0) {
            game.pokemon.BanDoVung.veHoenn((javax.microedition.lcdui.Graphics) g, map, w, h);
            return true;
        }
        game.pokemon.BanDoVung.ve((javax.microedition.lcdui.Graphics) g, diem, hienTai, w, h);
        return true;
    }

    /** Túi sinh thái (hệ sinh thái server, NPC ảo -528, lựa chọn 4 của menu gốc). */
    static void moTuiSinhThai() {
        TuiSinhThaiScr.xin();
    }

    /** pokemon: biểu tượng mục menu (GiaoDien.veHinhMuc) - bề ngang chiếm chỗ (0 = không có). */
    static int rongHinhMuc(String muc) {
        return game.pokemon.GiaoDien.hinhMuc(muc) != null ? 16 : 0;
    }

    static void veHinhMuc(Object g, int x, int y, String muc) {
        game.pokemon.GiaoDien.veHinhMuc((javax.microedition.lcdui.Graphics) g, x, y, muc);
    }

    /** pokemon: dải tiêu đề đỏ trên mép bảng menu chính + thanh cuộn bên phải khi dài hơn khung. */
    static void veKhungMenu(Object g0, int x, int y, int w, int h, String tieuDe, int tong, int tu, int thay) {
        javax.microedition.lcdui.Graphics g = (javax.microedition.lcdui.Graphics) g0;
        game.pokemon.GiaoDien.moClip(g);
        if (tieuDe != null && tieuDe.length() > 0) {
            // 0.9.16: đủ chỗ cho chữ tiêu đề THẬT của GiaoDien.dai (vai TIEU_DE + chừa 2 x 16 + 12) - trước đo bằng
            // borderFont nên phông PC cắt "Nhiệm..." / "Bản th.."
            int wT = Math.max(game.render.screen.Font.borderFont.getWidth(tieuDe) + 34,
                    game.pokemon.ChuDe.rong(game.pokemon.GiaoDien.tieuDe(tieuDe), game.pokemon.ChuDe.TIEU_DE) + 46);
            if (wT > w - 16) {
                wT = w - 16;
            }
            game.pokemon.GiaoDien.khoi(g, x + w / 2 - wT / 2 - 1, y - 9, wT + 2, 19, 6, game.pokemon.GiaoDien.VIEN);
            game.pokemon.GiaoDien.dai(g, x + w / 2 - wT / 2, y - 8, wT, 16, tieuDe);
        }
        if (tong > thay && thay > 0) {
            game.pokemon.KieuO.thanhCuon(g, x + w - 8, y + 12, h - 24, tong, tu, thay);
        }
    }

    /** pokemon: con trỏ ▶ đen của mục đang chọn (menu chính). */
    static void conTroMenu(Object g, int x, int y, int h) {
        game.pokemon.GiaoDien.conTro((javax.microedition.lcdui.Graphics) g, x, y, h);
    }

    /** Cài đặt -> Giấy phép: màn ghi công (GHI_CONG.txt trong jar). */
    static void moGiayPhep() {
        GhiCongScr.mo();
    }

    static boolean menuNhanhTick() {
        return MenuNhanh.tick();
    }

    /** Phím khi Menu nhanh đang mở: true = đã dùng phím. */
    static boolean menuNhanhPhim(int keyCode) {
        return MenuNhanh.phim(keyCode);
    }

    /** Thanh trạng thái Tự động đánh: đặt ngay dưới khung HP góc trái trên, không đè giữa màn. */
    static int yTrangThai(int macDinh) {
        return macDinh + 34;
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

    /** Mở bảng Tự động đánh chính thức (BangTuDong, kiểu Pokémon). */
    static void moBangTuDong() {
        BangTuDong.mo();
    }

    /** Điểm chạm nằm trên nút mở Menu nhanh (Poké Ball) - Tự động đánh không coi là người chơi chạm map. */
    static boolean trongNutNhanh(int x, int y) {
        return MenuNhanh.trongBong(x, y);
    }

    /** Thanh trạng thái Tự động đánh (viên thuốc xanh dưới khung HP). */
    static void veThanhTuDong(javax.microedition.lcdui.Graphics g, String s, int y) {
        BangTuDong.veThanh(g, s, y);
    }

    /** Tiêu đề "Bản đồ lớn": vùng Hoenn (năm 2) có tiêu đề riêng. */
    static String tieuDeBanDoLon(String macDinh) {
        return game.render.Canvas.gameScr != null
                && game.pokemon.BanDoVung.chiSoHoenn(game.render.Canvas.gameScr.map) >= 0 ? "Vùng Hoenn" : macDinh;
    }
}
