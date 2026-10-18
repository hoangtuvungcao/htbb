package game.render.screen;

import game.model.Command;
import game.model.IAction;
import game.pokemon.GiaoDien;
import game.pokemon.PokeBody;
import game.render.Canvas;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * Màn "Ghi công" (bản jar + APK dùng chung code): đọc /GHI_CONG.txt trong jar (client_jar/res, sinh bởi
 * tools/sinh_ghi_cong.py) và cho cuộn. CC BY-NC 4.0 của PMDCollab bắt buộc ghi công trong mọi bản phân phối.
 * Mở từ Menu màn đăng nhập. Cuộn: phím lên/xuống (giữ), trái/phải = lật trang, kéo bằng tay (cảm ứng).
 * Định dạng tệp: "# ..." = tiêu đề mục, dòng trống = cách đoạn.
 */
public class GhiCongScr extends Screen {
    private static GhiCongScr me;

    private Screen truoc;
    private String[] dong;
    private boolean[] laTieuDe;
    private int rongDaNgat = -1;
    private int cmy;
    private int cmtoY;
    private int cmyLim;
    private boolean dangKeo;
    private int pyKeo;
    private int cmyKeo;

    public static GhiCongScr gI() {
        if (me == null) {
            me = new GhiCongScr();
        }
        return me;
    }

    /** Mở màn ghi công, "Đóng" quay về màn đang mở. */
    public static void mo() {
        GhiCongScr s = gI();
        s.truoc = Canvas.currentScreen == s ? s.truoc : Canvas.currentScreen;
        s.cmy = s.cmtoY = 0;
        s.switchToMe();
    }

    private GhiCongScr() {
        this.right = new Command("Đóng", new IAction() {
            public void perform() {
                Screen t = GhiCongScr.this.truoc;
                GhiCongScr.me = null; // nhả bộ nhớ chữ (máy J2ME ít RAM)
                if (t != null) {
                    t.switchToMe();
                } else {
                    Canvas.loginScr.switchToMe();
                }
            }
        });
    }

    /** UTF-8 -> String (không phụ thuộc bảng mã của máy J2ME). */
    private static String utf8(byte[] b) {
        StringBuffer sb = new StringBuffer(b.length);
        int i = 0;
        if (b.length >= 3 && (b[0] & 0xFF) == 0xEF && (b[1] & 0xFF) == 0xBB && (b[2] & 0xFF) == 0xBF) {
            i = 3;
        }
        while (i < b.length) {
            int c = b[i] & 0xFF;
            if (c < 0x80) {
                i++;
            } else if ((c & 0xE0) == 0xC0 && i + 1 < b.length) {
                c = (c & 0x1F) << 6 | (b[i + 1] & 0x3F);
                i += 2;
            } else if ((c & 0xF0) == 0xE0 && i + 2 < b.length) {
                c = (c & 0x0F) << 12 | (b[i + 1] & 0x3F) << 6 | (b[i + 2] & 0x3F);
                i += 3;
            } else {
                c = '?';
                i++;
            }
            if (c != '\r') {
                sb.append((char) c);
            }
        }
        return sb.toString();
    }

    /** MANIFEST "HTBB-Phien-Ban" (build.sh / Gradle ghi từ tools/phat-hanh/phien_ban.conf). */
    static String tenPhienBan() {
        String s = null;
        try {
            s = game.GameMidlet.instance == null ? null : game.GameMidlet.instance.getAppProperty("HTBB-Phien-Ban");
        } catch (Throwable e) {
            s = null;
        }
        return s == null || s.length() == 0 ? "?" : s.trim();
    }

    private int x0() {
        return Canvas.w > 260 ? 12 : 4;
    }

    private int rongKhung() {
        return Canvas.w - 2 * x0();
    }

    private int caoKhung() {
        return Canvas.h - Screen.deltaY - 2 * (Canvas.h > 260 ? 10 : 3);
    }

    private int caoDong() {
        return Font.blackFont.getHeight() + 1;
    }

    private void ngatDong(int rong) {
        this.rongDaNgat = rong;
        byte[] b = PokeBody.docTaiNguyen("/GHI_CONG.txt");
        String s = b == null ? "# Ghi công\nKhông đọc được tệp ghi công trong bản cài đặt.\nXem ghi công đầy đủ trên trang web của game." : utf8(b);
        Vector ra = new Vector();
        Vector tieuDe = new Vector();
        // dòng đầu: phiên bản (người chơi gửi kèm khi báo lỗi)
        String[] pb = Font.blackFont.splitFontBStrInLine(new StringBuffer("Phiên bản ").append(tenPhienBan()).append(" (bản ").append(PhienBan.BAN).append(')').toString(), rong);
        for (int i = 0; i < pb.length; i++) {
            ra.addElement(pb[i]);
            tieuDe.addElement(Boolean.FALSE);
        }
        int dau = 0;
        while (dau <= s.length()) {
            int cuoi = s.indexOf('\n', dau);
            if (cuoi < 0) {
                cuoi = s.length();
            }
            String d = s.substring(dau, cuoi);
            dau = cuoi + 1;
            boolean td = d.startsWith("# ");
            if (td) {
                d = d.substring(2);
                if (ra.size() > 0) {
                    ra.addElement("");
                    tieuDe.addElement(Boolean.FALSE);
                }
            }
            if (d.length() == 0) {
                if (ra.size() > 0 && ((String) ra.lastElement()).length() > 0) {
                    ra.addElement("");
                    tieuDe.addElement(Boolean.FALSE);
                }
                continue;
            }
            String[] phan = Font.blackFont.splitFontBStrInLine(d, rong - (td ? 10 : 0));
            for (int i = 0; i < phan.length; i++) {
                ra.addElement(phan[i]);
                tieuDe.addElement(td ? Boolean.TRUE : Boolean.FALSE);
            }
        }
        this.dong = new String[ra.size()];
        this.laTieuDe = new boolean[ra.size()];
        for (int i = 0; i < this.dong.length; i++) {
            this.dong[i] = (String) ra.elementAt(i);
            this.laTieuDe[i] = ((Boolean) tieuDe.elementAt(i)).booleanValue();
        }
    }

    public void paint(Graphics g) {
        Canvas.resetTrans(g);
        if (this.truoc == Canvas.loginScr || this.truoc == null || this.truoc instanceof ServerListScr) {
            ServerListScr.gI().paintBg(g);
        } else if (this.truoc != null) {
            this.truoc.paint(g);
        }
        Canvas.resetTrans(g);
        int x = x0();
        int y = Canvas.h > 260 ? 10 : 3;
        int w = rongKhung();
        int h = caoKhung();
        GiaoDien.hop(g, x, y, w, h);
        GiaoDien.dai(g, x + 4, y + 4, w - 8, 16, "Giấy phép");
        int noiDungY = y + 24;
        int noiDungH = h - 30;
        int rong = w - 26;
        if (this.dong == null || this.rongDaNgat != rong) {
            ngatDong(rong);
        }
        int cd = caoDong();
        this.cmyLim = this.dong.length * cd - noiDungH;
        if (this.cmyLim < 0) {
            this.cmyLim = 0;
        }
        g.setClip(x + 4, noiDungY, w - 8, noiDungH);
        int dau = this.cmy / cd;
        int cuoi = dau + noiDungH / cd + 2;
        if (cuoi > this.dong.length) {
            cuoi = this.dong.length;
        }
        for (int i = dau; i < cuoi; i++) {
            int yd = noiDungY + i * cd - this.cmy;
            try {
                if (this.laTieuDe[i]) {
                    GiaoDien.khoi(g, x + 7, yd + 3, 4, cd - 6, 1, GiaoDien.DO);
                    Font.blackFont.drawString(g, this.dong[i], x + 15, yd, 0);
                    g.setColor(GiaoDien.XAM);
                    g.fillRect(x + 15, yd + cd - 1, Font.blackFont.getWidth(this.dong[i]), 1);
                } else if (this.dong[i].length() > 0) {
                    Font.blackFont.drawString(g, this.dong[i], x + 9, yd, 0);
                }
            } catch (Throwable e) {
                // một ký tự font không vẽ được không được làm mất cả màn (và nút Đóng)
            }
        }
        Canvas.resetTrans(g);
        g.setClip(0, 0, Canvas.w, Canvas.h);
        // thanh cuộn bên phải
        if (this.cmyLim > 0) {
            int tH = noiDungH * noiDungH / (this.cmyLim + noiDungH);
            if (tH < 10) {
                tH = 10;
            }
            int tY = noiDungY + (noiDungH - tH) * this.cmy / this.cmyLim;
            GiaoDien.khoi(g, x + w - 8, noiDungY, 3, noiDungH, 1, GiaoDien.XANH_NHAT);
            GiaoDien.khoi(g, x + w - 8, tY, 3, tH, 1, GiaoDien.XANH);
        }
        super.paint(g);
    }

    public void update() {
        int cd = caoDong();
        int trang = caoKhung() - 30 - cd;
        if (Canvas.keyHold[8]) {
            this.cmtoY += cd;
        }
        if (Canvas.keyHold[2]) {
            this.cmtoY -= cd;
        }
        if (Canvas.isKeyPressed(6)) {
            this.cmtoY += trang;
        }
        if (Canvas.isKeyPressed(4)) {
            this.cmtoY -= trang;
        }
        // kéo bằng tay (Android / máy cảm ứng)
        if (Canvas.isPointerDown && Canvas.py < Canvas.h - Screen.deltaY * 2) {
            if (!this.dangKeo) {
                this.dangKeo = true;
                this.pyKeo = Canvas.py;
                this.cmyKeo = this.cmy;
            }
            this.cmtoY = this.cmyKeo + this.pyKeo - Canvas.py;
            this.cmy = this.cmtoY;
        } else {
            this.dangKeo = false;
        }
        if (this.cmtoY > this.cmyLim) {
            this.cmtoY = this.cmyLim;
        }
        if (this.cmtoY < 0) {
            this.cmtoY = 0;
        }
        if (this.cmy != this.cmtoY) {
            int d = (this.cmtoY - this.cmy) / 3;
            if (d == 0) {
                d = this.cmtoY > this.cmy ? 1 : -1;
            }
            this.cmy += d;
        }
        if (this.cmy > this.cmyLim) {
            this.cmy = this.cmyLim;
        }
        if (this.cmy < 0) {
            this.cmy = 0;
        }
        super.update();
    }
}
