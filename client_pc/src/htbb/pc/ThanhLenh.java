package htbb.pc;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import javax.microedition.lcdui.NenPc;

/**
 * Nút lệnh TRONG NGỮ CẢNH thay thanh phím mềm kiểu J2ME (bản PC): lệnh trái / giữa / phải (game.model.Main
 * left / center / right) của menu, hộp thoại hay màn đang mở vẽ thành nút kính gắn vào khung đó - nút chính (giữa)
 * đỏ Poké ở phải dưới, nút đóng / quay lại bên trái nó, lệnh trái ở trái dưới. Nút chỉ ghi tên lệnh (không nhãn
 * phím; phím tắt Enter / Esc / F3 vẫn chạy và liệt kê ở thẻ "Phím tắt" của Cài đặt - LopPhu).
 * Bấm nút = gửi đúng phím mềm J2ME (-5 / -7 / -6) nên logic game giữ nguyên. port-j2me.py tắt thanh phím mềm
 * cũ (ảnh imgCmdBar, chữ lệnh, nút bàn phím ảo, vùng chạm collisionCmdBar).
 */
public final class ThanhLenh {
    /** bật nút trong ngữ cảnh (tắt thanh phím mềm cũ) */
    public static volatile boolean bat = true;

    private ThanhLenh() {
    }

    private static java.lang.reflect.Field fW, fH, fY;

    private static int truong(Object o, String ten) {
        try {
            java.lang.reflect.Field f;
            if ("wHop".equals(ten)) {
                if (fW == null) {
                    fW = o.getClass().getDeclaredField("wHop");
                    fW.setAccessible(true);
                }
                f = fW;
            } else if ("hHop".equals(ten)) {
                if (fH == null) {
                    fH = o.getClass().getDeclaredField("hHop");
                    fH.setAccessible(true);
                }
                f = fH;
            } else {
                if (fY == null) {
                    fY = o.getClass().getDeclaredField("y");
                    fY.setAccessible(true);
                }
                f = fY;
            }
            return f.getInt(o);
        } catch (Throwable e) {
            return Integer.MIN_VALUE;
        }
    }

    /** Vẽ nút (luồng trình bày). */
    static void ve(Graphics2D g, int W, int H, float u, ArrayList<LopPhu.Vung> vung) {
        if (!bat || game.render.Canvas.isLoad || game.render.Canvas.instance == null) return;
        game.model.Main m;
        boolean laMenu = game.render.Canvas.menu.showMenu;
        boolean laHop = false;
        if (laMenu) {
            m = game.render.Canvas.menu;
        } else if (game.render.Canvas.currentDialog != null) {
            m = game.render.Canvas.currentDialog;
            laHop = true;
        } else {
            m = game.render.Canvas.currentScreen;
        }
        if (m == null || m.anNutLenh) return;
        boolean theGioi = m == game.render.Canvas.gameScr;
        if (theGioi && !laMenu && !laHop) {
            game.render.screen.GameScr gs = game.render.Canvas.gameScr;
            if (gs.chatMode) {   // ô chat: Gửi / Xoá / Đóng ngay trên ô nhập, căn phải
                int k = NenPc.heSo;
                float bh = 26 * u;
                float y = NenPc.lechY + gs.tfChat.y * k - bh - 6 * u;
                Font f = PhongChu.co(true, 12 * u);
                float x = NenPc.lechX + (gs.tfChat.x + gs.tfChat.width) * k;
                // 0.9.5: ô rỗng / chỉ khoảng trắng: nút Gửi mờ, không bấm được
                if (game.networklogic.GameService.chuChat(gs.tfChat.getText()) != null) {
                    x = nut(g, x, y, bh, "Gửi", f, true, -5, vung, u);
                } else {
                    x = nutTat(g, x, y, bh, "Gửi", f, u);
                }
                x = nut(g, x, y, bh, "Xoá", f, false, -7, vung, u);
                nut(g, x, y, bh, "Đóng", f, false, -6, vung, u);
            }
            return; // màn chơi: phím tắt + nút gợi ý (không đè thế giới)
        }
        game.model.Command trai = m.left, giua = m.center, phai = m.right;
        boolean traiLaChinh = false;
        if (!co(giua) && co(trai)) {   // hộp thoại một nút ("OK" là lệnh trái): nút chính + Enter
            giua = trai;
            trai = null;
            traiLaChinh = true;
        }
        if (!co(trai) && !co(giua) && !co(phai)) return;
        int k = NenPc.heSo;
        int lx = NenPc.lechX, ly = NenPc.lechY;
        // khung neo (toạ độ thật): menu / hộp thoại / cả màn
        float ax0 = lx, ax1 = lx + NenPc.rong * k, ay1 = ly + NenPc.cao * k;
        if (laMenu) {
            game.render.screen.Menu mn = game.render.Canvas.menu;
            ax0 = lx + (mn.menuX - 4) * k;
            ax1 = lx + (mn.menuX + mn.menuW + 4) * k;
            ay1 = ly + (mn.menuY + mn.menuH + 4) * k + 6 * u;
        } else if (laHop && m instanceof game.render.screen.MsgDlg) {
            int wh = truong(m, "wHop"), hh = truong(m, "hHop"), yy = truong(m, "y");
            if (wh != Integer.MIN_VALUE && hh != Integer.MIN_VALUE && yy != Integer.MIN_VALUE && !((game.render.screen.MsgDlg) m).isUseArr) {
                int cx = NenPc.rong / 2;
                ax0 = lx + (cx - wh / 2) * k;
                ax1 = lx + (cx + wh / 2) * k;
                ay1 = ly + (yy + 12 + hh) * k + 6 * u;
            }
        } else {
            int[] kn = GiaoDienHD.khungNeo;
            if (kn != null && kn[2] * kn[3] * 6 > NenPc.rong * NenPc.cao / 4 && kn[2] < NenPc.rong - 8) {
                // khung chính của màn (thẻ đăng nhập, bảng chọn Pokémon...): nút ngay dưới khung, căn phải khung
                ax0 = lx + kn[0] * k;
                ax1 = lx + (kn[0] + kn[2]) * k;
                ay1 = ly + (kn[1] + kn[3]) * k + 8 * u;
                laHop = true;
            } else {
                ay1 = ly + NenPc.cao * k - 10 * u;
                ax0 += 12 * u;
                ax1 -= 12 * u;
            }
        }
        float bh = 30 * u;
        float y = Math.min(ay1, H - bh - 6 * u);
        if (!laMenu && !laHop) y = ay1 - bh;
        Font f = PhongChu.co(true, 13 * u);
        float x = ax1;
        if (co(giua)) x = nut(g, x, y, bh, giua.caption, f, true, traiLaChinh ? -6 : -5, vung, u);
        if (co(phai)) x = nut(g, x, y, bh, phai.caption, f, false, -7, vung, u);
        if (co(trai)) nutTrai(g, ax0, y, bh, trai.caption, f, -6, vung, u, x);
    }

    private static boolean co(game.model.Command c) {
        return c != null && c.caption != null && c.caption.length() > 0;
    }

    private static String chu(String s) {
        return game.pokemon.ChuPokemon.D(s);
    }

    private static float rongNut(Graphics2D g, String s, Font f, float bh) {
        g.setFont(f);
        float w = g.getFontMetrics().stringWidth(s);
        return Math.max(bh * 2.4f, w + bh * 1.1f);
    }

    /** Nút căn PHẢI tại xPhai; trả x trái mới (cho nút kế tiếp). */
    /** Nút tắt (mờ, không vùng chạm). */
    private static float nutTat(Graphics2D g, float xPhai, float y, float bh, String cap, Font f, float u) {
        String s = chu(cap);
        float w = rongNut(g, s, f, bh);
        java.awt.Composite cu = g.getComposite();
        g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.4f));
        veNut(g, xPhai - w, y, w, bh, s, f, true, u);
        g.setComposite(cu);
        return xPhai - w - 8 * u;
    }

    private static float nut(Graphics2D g, float xPhai, float y, float bh, String cap, Font f,
                             boolean chinh, final int ma, ArrayList<LopPhu.Vung> vung, float u) {
        String s = chu(cap);
        float w = rongNut(g, s, f, bh);
        float x = xPhai - w;
        veNut(g, x, y, w, bh, s, f, chinh, u);
        vung.add(new LopPhu.Vung(x, y, w, bh, new Runnable() {
            public void run() {
                bam(ma);
            }
        }, null));
        return x - 8 * u;
    }

    private static void nutTrai(Graphics2D g, float x, float y, float bh, String cap, Font f,
                                final int ma, ArrayList<LopPhu.Vung> vung, float u, float gioiHan) {
        String s = chu(cap);
        float w = rongNut(g, s, f, bh);
        if (x + w > gioiHan) {
            y += bh + 6 * u;   // không đủ chỗ cùng hàng: xuống hàng dưới, căn trái khung
        }
        veNut(g, x, y, w, bh, s, f, false, u);
        vung.add(new LopPhu.Vung(x, y, w, bh, new Runnable() {
            public void run() {
                bam(ma);
            }
        }, null));
    }

    private static void veNut(Graphics2D g, float x, float y, float w, float h, String s, Font f,
                              boolean chinh, float u) {
        ManHinh mh = ManHinh.me;
        boolean sang = mh != null && mh.chuotX >= x && mh.chuotX < x + w && mh.chuotY >= y && mh.chuotY < y + h;
        if (sang) {
            g.setColor(new Color(31, 181, 168, 60));   // 0.9.6: quầng teal khi trỏ chuột
            g.fill(new java.awt.geom.RoundRectangle2D.Float(x - 3 * u, y - 3 * u, w + 6 * u, h + 6 * u, h + 6 * u, h + 6 * u));
        }
        Kinh.nut(g, x, y, w, h, null, f, chinh, sang, false);
        // 0.9.6: viên phẳng kiểu GO (bỏ sọc chéo)
        // chỉ tên lệnh, căn giữa nút (không nhãn phím)
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        float tx = x + (w - fm.stringWidth(s)) / 2f;
        float ty = y + (h - fm.getAscent() - fm.getDescent()) / 2f + fm.getAscent();
        Kinh.chu(g, s, tx, ty, chinh ? Color.WHITE : Kinh.CHU);
    }

    /** Bấm nút = phím mềm J2ME (luồng sự kiện). */
    static void bam(int ma) {
        game.render.Canvas c = game.render.Canvas.instance;
        if (c == null) return;
        c.keyPressed(ma);
        c.keyReleased(ma);
    }
}
