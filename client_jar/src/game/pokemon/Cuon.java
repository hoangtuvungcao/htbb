package game.pokemon;

import game.render.Canvas;
import javax.microedition.lcdui.Graphics;

/**
 * Cuộn danh sách bằng ngón / chuột có QUÁN TÍNH (0.9.3): kéo trong khung = cuộn theo ngón, thả = trôi tiếp rồi chậm dần;
 * kéo quá ngưỡng thì cú thả KHÔNG thành cú chạm vào hàng (huỷ điểm nhấn của VungCham). Phím lên / xuống cuộn từng hàng.
 * Gọi khung() + capNhat() mỗi khung vẽ, lech() = số px nội dung đã cuộn.
 * 0.9.11: nhấn lên THANH CUỘN (veThanh) = kéo NÚM (ThanhCuon): núm theo con trỏ, nội dung theo tỉ lệ cùng chiều; chạm
 * rãnh = nhảy tới chỗ chạm. Trước đây cú kéo trên thanh bị coi như kéo nội dung nên ngược chiều (kéo núm lên = cuộn
 * xuống).
 */
public final class Cuon {
    float y, v;
    int max;
    boolean keo, daKeo;
    float yBatDau;
    int pyTruoc;
    int kx, ky, kw, kh;
    /** 0.9.11: đang kéo NÚM thanh cuộn (không phải nội dung) - núm theo con trỏ, nội dung theo tỉ lệ */
    boolean keoNum;
    int pyNumDau, cuonNumDau;
    /** thanh cuộn vẽ ở khung trước (toạ độ màn): cột tx..tx+tw, rãnh ty..ty+th, núm cao tn; th = 0 = không có */
    int tx, tw, ty, th, tn;

    /** Khung nhìn (x, y, w, h theo toạ độ màn) + chiều cao nội dung. */
    public void khung(Graphics g, int x, int y, int w, int h, int noiDung) {
        this.kx = x + g.getTranslateX();
        this.ky = y + g.getTranslateY();
        this.kw = w;
        this.kh = h;
        this.max = Math.max(0, noiDung - h);
        if (this.max <= 0) {
            this.th = 0;
        }
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

    /** Mỗi khung: theo ngón khi đang kéo nội dung, theo núm khi kéo thanh cuộn, quán tính khi đã thả. */
    public void capNhat() {
        if (KeoChieu.chanCuon()) {
            // 0.9.22: đang nắm / kéo chiêu (KeoChieu): danh sách đứng yên, không quán tính
            this.keo = false;
            this.v = 0;
            kep();
            return;
        }
        if (this.keoNum) {
            // kéo núm: theo dangGiu (bảng có thể xoá isPointerDown mỗi nhịp) - núm đi theo con trỏ, CÙNG chiều
            if (Canvas.dangGiu || Canvas.isPointerDown) {
                this.y = ThanhCuon.keo(this.cuonNumDau, this.pyNumDau, Canvas.py, this.th, this.tn, this.max);
                this.v = 0;
                Canvas.pxLast = -9999;   // cú thả không phải cú chạm vào hàng
            } else {
                this.keoNum = false;
            }
            kep();
            return;
        }
        if ((Canvas.isPointerDown || Canvas.dangGiu) && !this.keo && this.th > 0 && this.max > 0
                && ThanhCuon.trung(Canvas.pxLast, Canvas.pyLast, this.tx, this.tw, this.ty, this.th)) {
            // nhấn lên thanh cuộn: trên núm = nắm núm; trên rãnh = nhảy để tâm núm tới chỗ nhấn rồi nắm núm
            int c = (int) this.y;
            if (!ThanhCuon.trenNum(Canvas.pyLast, this.ty, this.th, this.tn, c, this.max)) {
                c = ThanhCuon.toi(Canvas.pyLast, this.ty, this.th, this.tn, this.max);
            }
            this.keoNum = true;
            this.cuonNumDau = c;
            this.pyNumDau = Canvas.pyLast;
            this.y = ThanhCuon.keo(c, this.pyNumDau, Canvas.py, this.th, this.tn, this.max);
            this.v = 0;
            Canvas.pxLast = -9999;
            kep();
            return;
        }
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

    /** Đang kéo núm thanh cuộn (kiểm thử). */
    public boolean dangKeoNum() {
        return this.keoNum;
    }

    /** Thanh cuộn vẽ lần cuối (toạ độ màn): {x, rộng, y rãnh, cao rãnh, cao núm, đỉnh núm}; null = không có thanh. */
    public int[] thanh() {
        if (this.th <= 0 || this.max <= 0) {
            return null;
        }
        return new int[]{this.tx, this.tw, this.ty, this.th, this.tn, ThanhCuon.yNum(this.ty, this.th, this.tn, lech(), this.max)};
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
            this.th = 0;
            return;
        }
        // 0.9.7: rãnh gỗ tối + núm đồng (mảnh CUON / CUON_NUM)
        int hb = ThanhCuon.caoNum(h, h, this.max + h, 16);
        int yb = ThanhCuon.yNum(y, h, hb, this.lech(), this.max);
        // 0.9.11: nhớ chỗ thanh (toạ độ màn) cho kéo núm ở capNhat khung sau
        this.tx = x - 1 + g.getTranslateX();
        this.tw = 6;
        this.ty = y + g.getTranslateY();
        this.th = h;
        this.tn = hb;
        if (KhungNro.ve(g, KhungNroBang.CUON, x - 1, y, 6, h)) {
            KhungNro.ve(g, KhungNroBang.CUON_NUM, x - 1, yb, 6, hb);
            return;
        }
        GiaoDien.khoi(g, x, y, 4, h, 1, 0x3A2414);
        GiaoDien.khoi(g, x, yb, 4, hb, 1, ChuDe.VANG);
    }
}
