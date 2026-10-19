package game.pokemon;

import game.model.IAction;
import game.render.Canvas;
import game.render.screen.Dialog;
import game.render.screen.Font;
import javax.microedition.lcdui.Graphics;

/**
 * HỘP XÁC NHẬN chung (0.9.3): giữa màn, tiêu đề + nội dung tự xuống dòng (+ dòng giá tiền tuỳ chọn), HAI nút to:
 * phụ (Huỷ) bên trái màu trung tính, chính bên phải có màu (xanh lá xác nhận / đỏ khi là thao tác mất đồ).
 * Phím: Enter / OK = nút chính, Back / Esc = nút phụ. Không dùng thanh phím mềm J2ME.
 */
public final class HopXacNhan extends Dialog {
    static HopXacNhan me;

    String tieuDe, noiDung, chuChinh, chuPhu;
    IAction co, khong;
    boolean nguyHiem;
    int loaiTien = -1;
    long gia;

    public static HopXacNhan gI() {
        if (me == null) {
            me = new HopXacNhan();
        }
        return me;
    }

    /** Hỏi xác nhận; co / khong có thể null (đóng hộp). */
    public static void hoi(String tieuDe, String noiDung, String chuChinh, IAction co, IAction khong) {
        HopXacNhan h = gI();
        h.tieuDe = tieuDe;
        h.noiDung = noiDung;
        h.chuChinh = chuChinh == null ? "Đồng ý" : chuChinh;
        h.chuPhu = "Huỷ";
        h.co = co;
        h.khong = khong;
        h.nguyHiem = false;
        h.loaiTien = -1;
        h.left = null;
        h.center = null;
        h.right = null;
        h.show();
    }

    /** Như hoi + dòng giá (biểu tượng tiền TienTe). */
    public static void hoiGia(String tieuDe, String noiDung, String chuChinh, int loaiTien, long gia, IAction co) {
        hoi(tieuDe, noiDung, chuChinh, co, null);
        gI().loaiTien = loaiTien;
        gI().gia = gia;
    }

    /** Thao tác mất đồ (bán, bỏ, huỷ nhiệm vụ): nút chính màu đỏ. */
    public static void hoiNguyHiem(String tieuDe, String noiDung, String chuChinh, IAction co) {
        hoi(tieuDe, noiDung, chuChinh, co, null);
        gI().nguyHiem = true;
    }

    public static boolean dangMo() {
        return me != null && Canvas.currentDialog == me;
    }

    void xong(IAction a) {
        if (Canvas.currentDialog == this) {
            Canvas.currentDialog = null;
        }
        if (a != null) {
            a.perform();
        }
    }

    public void paint(Graphics g) {
        Canvas.resetTrans(g);
        Bang.phuNen(g);
        int k = Bang.cach();
        int nh = Bang.caoHang();
        int ww = Math.min(Canvas.w - 24, 380);
        int wt = ww - 24;
        // đo cao nội dung
        int lh = Font.blackFont.getHeight() + 1;
        int soDong = demDong(Font.blackFont, this.noiDung, wt);
        int hGia = this.loaiTien >= 0 ? TienTe.coTheoChu(Font.blackFont) + 6 : 0;
        int hh = 10 + Font.borderFont.getHeight() + 10 + soDong * lh + hGia + 10 + nh + 10;
        if (hh > Canvas.h - 16) {
            hh = Canvas.h - 16;
        }
        int x = Canvas.hw - ww / 2, y = Canvas.hh - hh / 2;
        Bang.the(g, x, y, ww, hh);
        GiaoDien.dai(g, x + 4, y + 4, ww - 8, Font.borderFont.getHeight() + 8, this.tieuDe == null ? "Xác nhận" : this.tieuDe);
        int yy = y + 4 + Font.borderFont.getHeight() + 8 + 8;
        yy = Bang.doan(g, Font.blackFont, this.noiDung, x + 12, yy, wt, true);
        if (this.loaiTien >= 0) {
            int wg = TienTe.rongGia(this.loaiTien, this.gia, Font.blackFont, false);
            TienTe.veGia(g, this.loaiTien, this.gia, Canvas.hw - wg / 2, yy + 2, Font.blackFont, false);
        }
        int yn = y + hh - 10 - nh;
        int wb = (ww - 24 - k) / 2;
        Bang.nut(g, x + 12, yn, wb, nh, this.chuPhu, Bang.TRUNG, new IAction() {
            public void perform() {
                HopXacNhan.this.xong(HopXacNhan.this.khong);
            }
        }, "xn-huy");
        Bang.nut(g, x + 12 + wb + k, yn, ww - 24 - wb - k, nh, this.chuChinh, this.nguyHiem ? Bang.CHINH : Bang.XAC_NHAN, new IAction() {
            public void perform() {
                HopXacNhan.this.xong(HopXacNhan.this.co);
            }
        }, "xn-dong-y");
    }

    static int demDong(Font f, String s, int w) {
        if (s == null || s.length() == 0) {
            return 0;
        }
        int n = 0;
        String con = s;
        while (con.length() > 0 && n < 8) {
            int m = con.length();
            while (m > 1 && f.getWidth(con.substring(0, m)) > w) {
                int sp = con.lastIndexOf(' ', m - 1);
                m = sp > 0 ? sp : m - 1;
            }
            con = con.substring(m).trim();
            n++;
        }
        return n;
    }

    public void keyPress(int keyCode) {
        if (keyCode == -5 || keyCode == 10 || keyCode == 13) {
            xong(this.co);
        } else if (keyCode == -7 || keyCode == -6 || keyCode == 27) {
            xong(this.khong);
        }
    }

    public void update() {
    }
}
