package game.pokemon;

import game.render.Canvas;
import javax.microedition.lcdui.Graphics;

/**
 * Cuộn danh sách bằng ngón / chuột có QUÁN TÍNH (0.9.3): kéo trong khung = cuộn theo ngón, thả = trôi tiếp rồi chậm dần;
 * kéo quá ngưỡng thì cú thả KHÔNG thành cú chạm vào hàng (huỷ điểm nhấn của VungCham). Phím lên / xuống cuộn từng hàng.
 * Gọi khung() + capNhat() mỗi khung vẽ, lech() = số px nội dung đã cuộn.
 */
public final class Cuon {
    float y, v;
    int max;
    boolean keo, daKeo;
    float yBatDau;
    int pyTruoc;
    int kx, ky, kw, kh;

    /** Khung nhìn (x, y, w, h theo toạ độ màn) + chiều cao nội dung. */
    public void khung(Graphics g, int x, int y, int w, int h, int noiDung) {
        this.kx = x + g.getTranslateX();
        this.ky = y + g.getTranslateY();
        this.kw = w;
        this.kh = h;
        this.max = Math.max(0, noiDung - h);
        kep();
    }

    void kep() {
        if (this.y < 0) {
            this.y = 0;
            this.v = 0;
        }
        if (this.y > this.max) {
            this.y = this.max;
            this.v = 0;
        }
    }

    boolean trong(int px, int py) {
        return px >= this.kx && py >= this.ky && px < this.kx + this.kw && py < this.ky + this.kh;
    }

    /** Mỗi khung: theo ngón khi đang kéo, quán tính khi đã thả. */
    public void capNhat() {
        if (Canvas.isPointerDown && (this.keo || trong(Canvas.pxLast, Canvas.pyLast))) {
            if (!this.keo) {
                this.keo = true;
                this.daKeo = false;
                this.yBatDau = this.y;
                this.pyTruoc = Canvas.py;
                this.v = 0;
            }
            int d = Canvas.pyLast - Canvas.py;
            if (d > 6 || d < -6) {
                this.daKeo = true;
            }
            if (this.daKeo) {
                float moi = this.yBatDau + d;
                this.v = this.pyTruoc - Canvas.py;   // px / khung -> quán tính khi thả
                this.pyTruoc = Canvas.py;
                this.y = moi;
                Canvas.pxLast = -9999;   // đã kéo: cú thả không phải cú chạm vào hàng
            }
        } else {
            this.keo = false;
            if (this.v != 0) {
                this.y += this.v;
                this.v = this.v * 0.88f;
                if (this.v < 0.4f && this.v > -0.4f) {
                    this.v = 0;
                }
            }
        }
        kep();
    }

    public int lech() {
        return (int) this.y;
    }

    public int toiDa() {
        return this.max;
    }

    public void den(int y) {
        this.y = y;
        this.v = 0;
        kep();
    }

    public void them(int dy) {
        this.y += dy;
        this.v = 0;
        kep();
    }

    public void veThanh(Graphics g, int x, int y, int h) {
        if (this.max <= 0) {
            return;
        }
        GiaoDien.khoi(g, x, y, 4, h, 2, 0x9AA6BC);
        int tong = this.max + h;
        int hb = Math.max(16, h * h / Math.max(1, tong));
        int yb = y + (int) ((long) (h - hb) * this.lech() / Math.max(1, this.max));
        GiaoDien.khoi(g, x, yb, 4, hb, 2, GiaoDien.XANH);
    }
}
