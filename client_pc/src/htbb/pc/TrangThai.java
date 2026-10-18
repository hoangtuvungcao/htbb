package htbb.pc;

import game.render.Canvas;

/** Gỡ lỗi (-Dhtbb.ghiNhap=true, F9): tóm tắt trạng thái màn / menu của game vào nhật ký. */
final class TrangThai {
    private TrangThai() {
    }

    private static String truong(Object o, String... ten) {
        StringBuilder b = new StringBuilder();
        for (String t : ten) {
            try {
                java.lang.reflect.Field f = o.getClass().getDeclaredField(t);
                f.setAccessible(true);
                b.append(t).append('=').append(f.get(o)).append(' ');
            } catch (Throwable e) {
                b.append(t).append("=? ");
            }
        }
        return b.toString();
    }

    static String mo() {
        return "man=" + (Canvas.currentScreen == null ? null : Canvas.currentScreen.getClass().getSimpleName())
                + " dlg=" + (Canvas.currentDialog == null ? null : Canvas.currentDialog.getClass().getSimpleName())
                + " menu=" + Canvas.menu.showMenu + " p=" + Canvas.px + "," + Canvas.py + " last=" + Canvas.pxLast + "," + Canvas.pyLast
                + " " + truong(Canvas.menu, "menuX", "menuY", "menuW", "menuH", "menuTemY", "cyBag", "ctoyBag", "selected");
    }
}
