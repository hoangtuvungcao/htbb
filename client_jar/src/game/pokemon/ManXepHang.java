package game.pokemon;

import game.model.Char;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.screen.Font;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * XẾP HẠNG (0.9.3): tab Sức mạnh | Đại gia | Bảng vàng - chạm tab xin danh sách máy chủ (gói top cũ, kiểu 4 / 3 / 7);
 * GameScr.onFriendList giao kết quả cho màn này (nhan) thay vì mở ListScr. Hàng = hạng + chân dung + tên + một số chính.
 */
public final class ManXepHang extends ManBang {
    public static ManXepHang me;
    static final int[] KIEU = {4, 3, 7};
    static final Vector[] DS = new Vector[3];
    static boolean[] cho = new boolean[3];

    public static ManXepHang gI() {
        if (me == null) {
            me = new ManXepHang();
        }
        return me;
    }

    public static void mo() {
        if (Canvas.gameScr == null) {
            return;
        }
        gI().moBang();
        gI().chonTab(0);
    }

    /** GameScr.onFriendList: true = đã nhận (màn này đang mở và chờ kiểu đó) - không mở ListScr. */
    public static boolean nhan(int kieu, Vector ds) {
        for (int i = 0; i < KIEU.length; i++) {
            if (KIEU[i] == kieu && me != null && Canvas.currentScreen == me) {
                DS[i] = ds;
                cho[i] = false;
                return true;
            }
        }
        return false;
    }

    protected void chonTab(int t) {
        super.chonTab(t);
        if (DS[t] == null && !cho[t]) {
            cho[t] = true;
            try {
                GameService.gI().requestTopStronger_Righer(KIEU[t], 0);
            } catch (Throwable e) {
                cho[t] = false;
            }
        }
    }

    protected String tieuDe() {
        return "Xếp hạng";
    }

    protected String[] tabs() {
        return new String[]{"Sức mạnh", "Đại gia", "Bảng vàng"};
    }

    protected void veNoiDung(Graphics g, int x, int y, int w, int h) {
        Bang.the(g, x, y, w, h);
        final Vector v = DS[this.tab];
        if (v == null || v.size() == 0) {
            Bang.trong(g, x, y, w, h, cho[this.tab] ? "Đang tải bảng xếp hạng..." : "Chưa có dữ liệu", null);
            return;
        }
        final int t = this.tab;
        Bang.danhSach(g, this.ds, x + 4, y + 4, w - 8, h - 8, v.size(), Bang.caoHang(), new Bang.Hang() {
            public void ve(Graphics g, int i, int x, int y, int w, int h) {
                Char c = (Char) v.elementAt(i);
                Bang.hang(g, x, y, w, h, i, c.name != null && c.name.equals(Canvas.gameScr.mainChar.name), null, "hang-" + i);
                // hạng: 1-3 huy chương màu
                int r = h / 2 - 4;
                int mau = i == 0 ? GiaoDien.VANG : (i == 1 ? 0xC8D0DC : (i == 2 ? 0xD08A4C : GiaoDien.XANH_NHAT));
                g.setColor(GiaoDien.VIEN);
                g.fillArc(x + 4, y + 3, 2 * r + 2, 2 * r + 2, 0, 360);
                g.setColor(mau);
                g.fillArc(x + 5, y + 4, 2 * r, 2 * r, 0, 360);
                Font.blackFont.drawString(g, String.valueOf(i + 1), x + 5 + r, y + 4 + r - Font.blackFont.getHeight() / 2, Font.CENTER);
                int xs = x + 2 * r + 12;
                int s = h - 6;
                PokeBody.paintChanDung(g, LoaiPokemonClient.khoiDau(Math.max(0, Math.min(4, c.clazz))), false, xs + s / 2, y + 3 + s / 2, s);
                String ten = c.name == null ? "" : c.name;
                if (t == 1) {
                    int wg = TienTe.rongGia(TienTe.XU, c.charXu, Font.blackFont, true);
                    Font.blackFont.drawString(g, GiaoDien.catChu(Font.blackFont, ten, x + w - 16 - wg - (xs + s + 6)), xs + s + 6, y + (h - Font.blackFont.getHeight()) / 2, 0);
                    TienTe.veGia(g, TienTe.XU, c.charXu, x + w - 8 - wg, y + (h - Font.blackFont.getHeight()) / 2, Font.blackFont, true);
                } else {
                    String so = t == 2 ? String.valueOf(c.congTrang) : "Lv." + c.level;
                    int ws = Font.blackFont.getWidth(so);
                    Font.blackFont.drawString(g, GiaoDien.catChu(Font.blackFont, ten, x + w - 20 - ws - (xs + s + 6)), xs + s + 6, y + (h - Font.blackFont.getHeight()) / 2, 0);
                    Font.blackFont.drawString(g, so, x + w - 8, y + (h - Font.blackFont.getHeight()) / 2, 1);
                }
            }
        });
    }
}
