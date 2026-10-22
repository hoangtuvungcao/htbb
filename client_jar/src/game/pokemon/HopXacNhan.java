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
        h.noiDung = noiDung == null ? "" : noiDung;
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
        Canvas.clearKeyPressed();   // 0.9.4: Enter / OK không lọt xuống màn bên dưới
        if (a != null) {
            a.perform();
        }
    }

    /** việc dựng sẵn (không tạo đối tượng mỗi khung) */
    final IAction bamHuy = new IAction() {
        public void perform() {
            HopXacNhan.this.xong(HopXacNhan.this.khong);
        }
    };
    final IAction bamCo = new IAction() {
        public void perform() {
            HopXacNhan.this.xong(HopXacNhan.this.co);
        }
    };
    String khoaNgat;
    int rongNgat;
    String[] dong;
    final String[] nut = new String[2];
    final int[] kieuNut = new int[2];
    final IAction[] bam = new IAction[2];
    final int[] hPhu = new int[1];
    static final String[] TEN_NUT = {"xn-huy", "xn-dong-y"};

    /** 0.9.5: vẽ bằng hộp thông báo chung (HopThongBao.ve) - đầu + X, thân tự co, hai nút. */
    public void paint(Graphics g) {
        Canvas.resetTrans(g);
        this.nut[0] = this.chuPhu;
        this.nut[1] = this.chuChinh;
        int wGia = this.loaiTien >= 0 ? TienTe.rongGia(this.loaiTien, this.gia, Font.blackFont, false) : 0;
        int ww = HopThongBao.rongHop(this.tieuDe == null ? "Xác nhận" : this.tieuDe, this.noiDung, this.nut, wGia);
        if (this.dong == null || !this.noiDung.equals(this.khoaNgat) || this.rongNgat != ww) {
            this.dong = ChuDe.ngat(this.noiDung, ChuDe.THUONG, ww - 2 * HopThongBao.pad(), 40);
            this.khoaNgat = this.noiDung;
            this.rongNgat = ww;
        }
        this.kieuNut[0] = Bang.TRUNG;
        this.kieuNut[1] = this.nguyHiem ? Bang.NGUY : Bang.XAC_NHAN;
        this.bam[0] = this.bamHuy;
        this.bam[1] = this.bamCo;
        this.hPhu[0] = this.loaiTien >= 0 ? TienTe.coTheoChu(Font.blackFont) + 8 : 0;
        String td = this.tieuDe == null ? "" : this.tieuDe;
        if (this.nguyHiem) {
            HopThongBao.datHuyHieu(BieuTuong.CANH_BAO, ChuDe.DO, false);
        } else if (td.startsWith("Mua") || td.startsWith("Bán") || this.loaiTien >= 0) {
            HopThongBao.datHuyHieu(BieuTuong.CUA_HANG, ChuDe.CHINH, true);
        } else {
            HopThongBao.chonHuyHieu(td, this.noiDung, HopThongBao.HOI);
        }
        int[] r = HopThongBao.ve(g, ww, this.tieuDe == null ? "Xác nhận" : this.tieuDe, this.nguyHiem ? ChuDe.DO : ChuDe.CHINH, this.dong,
                this.hPhu, this.nut, this.kieuNut, this.bam, this.bamHuy, true, null, TEN_NUT);
        if (this.loaiTien >= 0) {
            int wg = TienTe.rongGia(this.loaiTien, this.gia, Font.blackFont, false);
            TienTe.veGia(g, this.loaiTien, this.gia, r[0] + r[2] / 2 - wg / 2, r[4] + 4, Font.blackFont, false);
        }
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
