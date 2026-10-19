package htbb.pc;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Vector;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.NenPc;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

/**
 * Hộp nhập chữ (MIDP TextBox) của bản PC: bảng kính giữa cửa sổ, gõ bằng bàn phím thật (cả tiếng Việt từ bộ gõ),
 * Ctrl+V dán, Enter = OK, Esc = Huỷ (phím không ghi trên nút; xem thẻ "Phím tắt" của Cài đặt). Mọi hàm chạy trên luồng sự kiện NenPc.
 */
public final class HopNhap {
    static volatile boolean mo;
    private static TextBox tb;
    private static StringBuilder chu = new StringBuilder();
    private static Command ok;
    private static Command huy;

    private HopNhap() {
    }

    public static void mo(final TextBox t) {
        NenPc.luong().them(new Runnable() {
            public void run() {
                tb = t;
                chu = new StringBuilder(t.getString());
                ok = null;
                huy = null;
                Vector v = NenPc.lenh(t);
                for (int i = 0; i < v.size(); i++) {
                    Command c = (Command) v.elementAt(i);
                    int k = c.getCommandType();
                    if (k == Command.BACK || k == Command.CANCEL || k == Command.EXIT || k == Command.STOP) {
                        if (huy == null) huy = c;
                    } else if (ok == null) {
                        ok = c;
                    }
                }
                mo = true;
            }
        });
    }

    public static void dong() {
        mo = false;
    }

    private static void xong(boolean dongY) {
        if (!mo) return;
        mo = false;
        TextBox t = tb;
        if (t == null) return;
        if (dongY) t.setString(chu.toString());
        NenPc.goiLenh(t, dongY ? ok : huy);
    }

    private static boolean laMatKhau() {
        return tb != null && (tb.getConstraints() & TextField.PASSWORD) != 0;
    }

    private static boolean chiSo() {
        int c = tb == null ? 0 : tb.getConstraints() & TextField.CONSTRAINT_MASK;
        return c == TextField.NUMERIC || c == TextField.PHONENUMBER || c == TextField.DECIMAL;
    }

    static void phim(int vk, int mod) {
        if (!mo) return;
        if (vk == KeyEvent.VK_ENTER) {
            xong(true);
        } else if (vk == KeyEvent.VK_ESCAPE) {
            xong(false);
        } else if (vk == KeyEvent.VK_BACK_SPACE) {
            if (chu.length() > 0) chu.setLength(chu.length() - 1);
        } else if (vk == KeyEvent.VK_V && (mod & (KeyEvent.CTRL_DOWN_MASK | KeyEvent.META_DOWN_MASK)) != 0) {
            try {
                Object s = Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
                if (s != null) {
                    String d = String.valueOf(s).replace('\n', ' ').replace('\r', ' ');
                    for (int i = 0; i < d.length(); i++) them(d.charAt(i));
                }
            } catch (Throwable e) {
                // khay nhớ trống
            }
        }
    }

    private static void them(char c) {
        if (tb == null || chu.length() >= tb.getMaxSize()) return;
        if (chiSo() && (c < '0' || c > '9') && c != '.' && c != '-' && c != '+') return;
        chu.append(c);
    }

    static void goChu(char c) {
        if (!mo || c < 32 || c == 127) return;
        them(c);
    }

    static void ve(Graphics2D g, int W, int H, float u, ArrayList<LopPhu.Vung> vung) {
        g.setColor(new Color(4, 8, 20, 140));
        g.fillRect(0, 0, W, H);
        vung.add(new LopPhu.Vung(0, 0, W, H, null, null));
        float pw = Math.min(W - 40 * u, 560 * u);
        float ph = 190 * u;
        float px = (W - pw) / 2;
        float py = (H - ph) / 2 - 40 * u;
        Kinh.bang(g, px, py, pw, ph, 20 * u, 255);
        String tieuDe = tb != null && tb.getTitle() != null && tb.getTitle().length() > 0 ? tb.getTitle()
                : laMatKhau() ? "Nhập mật khẩu" : "Nhập chữ";
        g.setFont(PhongChu.co(true, 17 * u));
        Kinh.chu(g, tieuDe, px + 24 * u, py + 38 * u, Kinh.CHU);
        // ô nhập
        float ox = px + 24 * u, oy = py + 56 * u, ow = pw - 48 * u, oh = 44 * u;
        g.setColor(new Color(255, 255, 255, 235));
        g.fill(new RoundRectangle2D.Float(ox, oy, ow, oh, 12 * u, 12 * u));
        g.setColor(Kinh.CHINH);
        g.setStroke(new java.awt.BasicStroke(2f * u));
        g.draw(new RoundRectangle2D.Float(ox, oy, ow, oh, 12 * u, 12 * u));
        String s = chu.toString();
        if (laMatKhau()) {
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < s.length(); i++) b.append('•');
            s = b.toString();
        }
        Font f = PhongChu.co(false, 17 * u);
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        java.awt.Shape cu = g.getClip();
        g.clipRect((int) (ox + 6 * u), (int) oy, (int) (ow - 12 * u), (int) oh);
        float tw = fm.stringWidth(s);
        float tx = ox + 12 * u;
        if (tw > ow - 30 * u) tx = ox + ow - 18 * u - tw;
        float by = oy + oh / 2 + (fm.getAscent() - fm.getDescent()) / 2f;
        g.setColor(new Color(0x1E2A44));
        g.drawString(s, tx, by);
        if (System.currentTimeMillis() / 500 % 2 == 0) {
            g.setColor(Kinh.CHINH_TOI);
            g.fillRect((int) (tx + tw + 2 * u), (int) (oy + 10 * u), Math.max(2, (int) (2 * u)), (int) (oh - 20 * u));
        }
        g.setClip(cu);
        // nút
        float bw = 130 * u, bh = 36 * u, by2 = py + ph - bh - 20 * u;
        float bOk = px + pw - 24 * u - bw, bHuy = bOk - bw - 12 * u;
        Font fb = PhongChu.co(true, 14 * u);
        Kinh.nut(g, bHuy, by2, bw, bh, "Huỷ", fb, false, tro(bHuy, by2, bw, bh), false);
        Kinh.nut(g, bOk, by2, bw, bh, "Xong", fb, true, tro(bOk, by2, bw, bh), false);
        vung.add(new LopPhu.Vung(bHuy, by2, bw, bh, new Runnable() {
            public void run() {
                xong(false);
            }
        }, null));
        vung.add(new LopPhu.Vung(bOk, by2, bw, bh, new Runnable() {
            public void run() {
                xong(true);
            }
        }, null));
        g.setFont(PhongChu.co(false, 11.5f * u));
        Kinh.chu(g, "Gõ tiếng Việt bằng bộ gõ của máy (Unikey, EVKey, ibus...)", px + 24 * u, by2 + bh * 0.62f, Kinh.CHU_MO);
    }

    private static boolean tro(float x, float y, float w, float h) {
        ManHinh m = ManHinh.me;
        return m != null && m.chuotX >= x && m.chuotX < x + w && m.chuotY >= y && m.chuotY < y + h;
    }
}
