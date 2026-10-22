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
        // 0.9.6.1: mỗi lần mở xin lại (lần trước có thể đã kẹt chờ / trả lời tới lúc màn đã đóng -> bị bỏ)
        for (int i = 0; i < DS.length; i++) {
            DS[i] = null;
            cho[i] = false;
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
            luc[t] = System.currentTimeMillis();
            try {
                if (KIEU[t] == 7) {
                    // 0.9.6.1 "lỗi tải": xin top kiểu 7 thì máy chủ cũ trả MENU -504 (10 mục top theo vùng) chứ không
                    // trả danh sách -> màn này chờ mãi "Đang tải bảng xếp hạng...". Chọn thẳng mục -504 "Top công trạng"
                    // vùng của mình (3 + vùng): máy chủ trả -19 kiểu 7 (cột công trạng) - đúng với máy chủ cũ lẫn mới.
                    GameService.gI().doMenuOption(NPC_TOP_VUNG, (byte) 0, MUC_CONG_TRANG + vung());
                } else {
                    GameService.gI().requestTopStronger_Righer(KIEU[t], 0);
                }
            } catch (Throwable e) {
                cho[t] = false;
            }
        }
    }

    /** menu top theo vùng của máy chủ (MapDoMenu case -504 -> Map.doSendTopNation) */
    static final int NPC_TOP_VUNG = -504;
    /** mục đầu của "Top công trạng Kanto / Johto / Hoenn" trong menu -504 */
    static final int MUC_CONG_TRANG = 3;
    /** Chờ máy chủ tối đa (ms): quá thì thôi "Đang tải" (hiện "Chưa có dữ liệu"), chạm lại tab / mở lại để xin lại. */
    static final long CHO_TOI_DA = 8000L;
    static final long[] luc = new long[3];

    /** vùng (0 Kanto, 1 Johto, 2 Hoenn) của nhân vật; chưa có vùng -> Kanto */
    static int vung() {
        int v = -1;
        try {
            v = Canvas.gameScr.mainChar.nationID;
        } catch (Throwable e) {
            // chưa vào game
        }
        return v >= 0 && v < 3 ? v : 0;
    }

    /** Hết thời gian chờ máy chủ cho tab t (không treo màn "Đang tải" mãi). */
    static boolean hetCho(int t, long bay) {
        if (cho[t] && bay - luc[t] > CHO_TOI_DA) {
            cho[t] = false;
            return true;
        }
        return false;
    }

    protected String tieuDe() {
        return "Xếp hạng";
    }

    protected String[] tabs() {
        return new String[]{"Sức mạnh", "Đại gia", "Bảng vàng"};
    }

    /** 0.9.6: danh sách gọn (tấm hẹp), cao theo số hàng (tối thiểu 4, tối đa vùng an toàn). */
    protected int rongMuon() {
        return ChuDe.dt() ? 470 : 420;
    }

    protected int caoMuon(int wNoi) {
        Vector v = DS[this.tab];
        int n = v == null ? 0 : v.size();
        return Math.max(4, n) * (Bang.caoHang() + Bang.cach()) + 4;
    }

    protected void veNoiDung(Graphics g, int x, int y, int w, int h) {
        final Vector v = DS[this.tab];
        if (v == null || v.size() == 0) {
            hetCho(this.tab, System.currentTimeMillis());
            Bang.trong(g, x, y, w, h, cho[this.tab] ? "Đang tải bảng xếp hạng..." : "Chưa có dữ liệu", null);
            return;
        }
        final int t = this.tab;
        Bang.danhSach(g, this.ds, x, y + 2, w, h - 2, v.size(), Bang.caoHang(), new Bang.Hang() {
            public void ve(Graphics g, int i, int x, int y, int w, int h) {
                Char c = (Char) v.elementAt(i);
                Bang.hang(g, x, y, w, h, i, c.name != null && c.name.equals(Canvas.gameScr.mainChar.name), null, "hang-" + i);
                // hạng: 1-3 huy chương màu
                int r = h / 2 - 4;
                // 0.9.6: huy chương tròn (vàng / bạc / đồng, còn lại xám xanh nhạt), số đậm
                int mau = i == 0 ? ChuDe.VANG : (i == 1 ? 0xC9D2DA : (i == 2 ? 0xE0A15E : ChuDe.NEN_PHU_TOI));
                // 0.9.7: huy chương = khối pixel (vàng / bạc / đồng / gỗ) số trắng viền
                GiaoDien.khoiPixel(g, x + 5, y + 4, 2 * r, 2 * r, i < 3 ? mau : 0x8E4A1E);
                ChuDe.chuVienGiua(g, String.valueOf(i + 1), x + 5 + r, y + 4, 2 * r, ChuDe.SO, 0xFFFFFF, ChuDe.VIEN, 2);
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
