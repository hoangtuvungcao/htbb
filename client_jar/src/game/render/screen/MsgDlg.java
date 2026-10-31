package game.render.screen;

import game.model.Command;
import game.render.Canvas;
import game.render.Res;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

public class MsgDlg extends Dialog {
    protected String[] info;
    long time;
    public byte size;
    private int w;
    private int h;
    private int ySt;
    private int y;
    public Vector listDelay;
    private int selected;
    private int dis;
    private int disLim;
    private int wText;
    public static int cmtoY;
    public static int cmy;
    public static int cmdy;
    public static int cmvy;
    public static int cmyLim;
    public String[] arr;
    public boolean isUseArr;
    public int hArr;
    private static final int[] translate = {0, 7, 3, 6};
    public static int lastSe = 0;
    public int state = 0;
    public boolean isIcon = true;
    public boolean isShowDelay = false;
    int dir = 1;
    int x2 = 0;

    /** pokemon: bề ngang chữ hộp thoại theo màn (máy 240/360 px hộp rộng hơn, ít phải cuộn). */
    static int rongChu() {
        int w = Canvas.w - 64;
        return w > 200 ? 200 : (w < 100 ? 100 : w);
    }

    /** pokemon: kích thước hộp thoại (hộp chờ có Poké Ball giữ 140 x 100). */
    private int wHop = 140;
    private int hHop = 100;

    public void setInfo(String info, Command left, Command center, Command right) {
        this.info = Font.arialFontW.splitFontBStrInLine(info, rongChu());
        this.left = left;
        this.center = center;
        this.right = right;
        this.isShowDelay = false;
        init();
        this.selected = lastSe;
        this.isUseArr = false;
    }

    public void setInfo(String[] info2, Command left, Command center, Command right) {
        this.info = info2;
        this.left = left;
        this.center = center;
        this.right = right;
        this.isShowDelay = false;
        init();
        this.selected = lastSe;
        this.isUseArr = false;
    }

    public void setInfoArr(String info, String[] arr, Command left, Command center, Command right) {
        this.info = Font.arialFontW.splitFontBStrInLine(info, Canvas.w - 60);
        this.left = left;
        this.center = center;
        this.right = right;
        this.isShowDelay = false;
        this.arr = arr;
        init();
        this.selected = lastSe;
        this.hArr = (arr.length * 20) + (20 * this.info.length);
        this.isUseArr = true;
    }

    public void init() {
        this.w = Canvas.w - 40;
        this.h = 20;
        this.dis = 90;
        this.wHop = 140;
        if (!this.isIcon && this.info != null) {
            // pokemon: hộp giãn theo số dòng (tới gần hết màn), rộng theo chữ
            int can = this.info.length * 13;
            int toiDa = Canvas.h - 96;
            if (can > this.dis && toiDa > this.dis) {
                this.dis = can < toiDa ? can : toiDa;
            }
            int rong = 0;
            for (int i = 0; i < this.info.length; i++) {
                int r = Font.arialFont.getWidth(this.info[i]);
                if (r > rong) {
                    rong = r;
                }
            }
            if (rong + 40 > this.wHop) {
                this.wHop = rong + 40 < Canvas.w - 16 ? rong + 40 : Canvas.w - 16;
            }
        }
        this.hHop = this.dis + 18;
        // pokemon: hộp (đỉnh = y + 12, cao hHop) canh giữa vùng trên thanh phím mềm đáy (~28 px), trong màn
        this.y = ((Canvas.h - 28 - this.hHop) / 2) - 12;
        if (this.y < -8) {
            this.y = -8;
        }
        this.ySt = 15;
        if (this.isIcon) {
            this.dis -= 50;
            this.ySt += 50;
        }
        this.disLim = this.dis;
        if (this.info.length * 13 < this.dis) {
            this.ySt += (this.dis / 2) - ((this.info.length * 13) / 2);
            this.disLim -= this.dis - (this.info.length * 13);
        }
        cmyLim = (this.info.length * 13) - this.dis;
        if (cmyLim < 0) {
            cmyLim = 0;
        }
        lastSe = (this.dis / 2) / 13;
    }

    public void paintArr(Graphics g) {
        Canvas.resetTrans(g);
        int ybg0 = (Canvas.hh - (this.hArr / 2)) - 20;
        if (ybg0 <= 10) {
            ybg0 = 10;
        }
        // pokemon: hộp thoại trắng, chữ tối
        game.pokemon.GiaoDien.hop(g, 20, ybg0, Canvas.w - 40, this.hArr + 20);
        for (int i = 0; i < this.arr.length; i++) {
            Font.arialFont.drawString(g, this.arr[i], Canvas.hw, (i * 20) + 10 + ybg0, 2);
        }
        int ybd = 10 + 32;
        for (int i2 = 0; i2 < this.info.length; i2++) {
            Font.blackFont.drawString(g, this.info[i2], Canvas.hw, ((((i2 * 20) + ((this.arr.length - 1) * 20)) + ybd) + ybg0) - 4, 2);
        }
        game.pokemon.GiaoDien.muiXuong(g, Canvas.w - 34, ybg0 + this.hArr + 10);
        g.translate(0, this.y);
    }

    private game.model.IAction huyCho;

    /** 0.9.5 (APK / PC): hộp CHỜ cùng khung hộp chung - vòng quay + chữ, X = huỷ (lệnh phải cũ). */
    private void veCho(Graphics g) {
        this.anNutLenh = true;
        if (this.huyCho == null) {
            this.huyCho = new game.model.IAction() {
                public void perform() {
                    if (MsgDlg.this.right != null && MsgDlg.this.right.action != null) {
                        MsgDlg.this.right.action.perform();
                    } else {
                        Canvas.endDlg();
                    }
                }
            };
        }
        StringBuffer b = new StringBuffer();
        for (int i = 0; this.info != null && i < this.info.length; i++) {
            if (this.info[i] != null && this.info[i].trim().length() > 0) {
                b.append(b.length() > 0 ? " " : "").append(this.info[i].trim());
            }
        }
        String chu = b.length() > 0 ? b.toString() : "Xin chờ...";
        int r = 10;
        int ww = Math.max(140, Math.min(260, game.pokemon.ChuDe.rong(chu, game.pokemon.ChuDe.THUONG) + 2 * r + 40));
        int lh = game.pokemon.ChuDe.cao(game.pokemon.ChuDe.THUONG);
        game.pokemon.HopThongBao.datHuyHieu(game.pokemon.BieuTuong.TIN, game.pokemon.ChuDe.XANH, true);
        int[] k = game.pokemon.HopThongBao.khung(g, ww, Math.max(2 * r + 4, lh), "Đang xử lý", this.huyCho, false);
        game.pokemon.GiaoDien.dangTai(g, k[5] + r, k[4] + r + 2, r);
        game.pokemon.ChuDe.chu(g, game.pokemon.ChuDe.cat(chu, game.pokemon.ChuDe.THUONG, k[6] - 2 * r - 10), k[5] + 2 * r + 10,
                k[4] + r + 2 - lh / 2, game.pokemon.ChuDe.THUONG, game.pokemon.ChuDe.CHU, 0);
    }

    /**
     * 31/10/2026 (soi giao diện: hộp "Mất kết nối" thật và mọi hộp chữ cũ ở máy cảm ứng = hộp trắng nhỏ + phím mềm góc, không
     * vùng chạm): tấm chung (Bang.khungTam) - chữ ngắt theo bề ngang tấm, nút khối đủ cỡ chạm theo lệnh trái / giữa / phải.
     */
    private void veHop(Graphics g) {
        this.anNutLenh = true;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; this.info != null && i < this.info.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(this.info[i]);
        }
        game.model.Command[] lenh = new game.model.Command[3];
        int n = 0;
        game.model.Command[] goc = {this.right, this.center, this.left};
        for (int i = 0; i < 3; i++) {
            if (goc[i] != null && goc[i].caption != null && goc[i].caption.length() > 0) {
                lenh[n++] = goc[i];
            }
        }
        int k = game.pokemon.Bang.cach(), hb = game.pokemon.Bang.caoHang();
        int wT = Math.min(game.pokemon.Bang.rongTamToiDa(), game.pokemon.ChuDe.dt() ? 460 : 380);
        String[] d = game.pokemon.ChuDe.ngat(sb.toString(), game.pokemon.ChuDe.THUONG, wT - 2 * game.pokemon.ChuDe.DEM - 16, 10);
        int lh = game.pokemon.ChuDe.cao(game.pokemon.ChuDe.THUONG);
        int hT = game.pokemon.Bang.caoDauTam(false) + d.length * lh + k + (n > 0 ? hb + k : 0) + game.pokemon.ChuDe.DEM;
        int[] c = game.pokemon.Bang.khungTam(g, wT, hT, "Thông báo", null, 0, null, null);
        int y = c[1];
        for (int i = 0; i < d.length; i++) {
            game.pokemon.ChuDe.chu(g, d[i], c[0] + c[2] / 2, y + i * lh, game.pokemon.ChuDe.THUONG, game.pokemon.ChuDe.CHU, 2);
        }
        if (n == 0) {
            return;
        }
        int yb = c[1] + c[3] - hb;
        int wn = (c[2] - (n - 1) * k) / n;
        for (int i = 0; i < n; i++) {
            final game.model.Command cm = lenh[i];
            game.pokemon.Bang.nut(g, c[0] + i * (wn + k), yb, i == n - 1 ? c[2] - i * (wn + k) : wn, hb, cm.caption,
                    i == n - 1 ? game.pokemon.Bang.XAC_NHAN : game.pokemon.Bang.TRUNG, cm.action == null ? null : new game.model.IAction() {
                        public void perform() {
                            cm.action.perform();
                        }
                    }, "hop-cu-" + i);
        }
    }

    public void paint(Graphics g) {
        Canvas.resetTrans(g);
        if (this.isIcon && !this.isUseArr && game.pokemon.MenuNguCanh.bat()) {
            veCho(g);
            return;
        }
        if (!this.isIcon && !this.isUseArr && game.pokemon.MenuNguCanh.bat()) {
            veHop(g);
            return;
        }
        this.anNutLenh = false;
        g.translate(0, this.y);
        if (this.isUseArr) {
            paintArr(g);
        } else {
            // pokemon: hộp thoại Pokémon (trắng, viền tối, ▼ nhấp nháy), chờ = Poké Ball lắc
            game.pokemon.GiaoDien.hop(g, Canvas.hw - (this.wHop / 2), 12, this.wHop, this.hHop);
            if (this.isIcon) {
                game.pokemon.GiaoDien.dangTai(g, Canvas.hw, 45, 13);
            } else {
                game.pokemon.GiaoDien.muiXuong(g, Canvas.hw + (this.wHop / 2) - 14, this.hHop + 4);
            }
            g.translate(0, this.ySt);
            g.setClip(0, 0, Canvas.w, this.disLim);
            g.translate(0, -cmy);
            for (int i = 0; i < this.info.length; i++) {
                Font.arialFont.drawString(g, this.info[i], Canvas.hw, i * 13, 2);
            }
        }
        super.paint(g);
    }

    /** 0.9.5: máy cảm ứng / chuột (APK, PC): hộp chữ -> HỘP THÔNG BÁO CHUNG (game.pokemon.HopThoaiBao); hộp chờ giữ. */
    @Override
    public void show() {
        if (!this.isIcon && !this.isUseArr && game.pokemon.MenuNguCanh.bat()
                && game.pokemon.HopThongBao.tuMsgDlg(this.info, this.left, this.center, this.right)) {
            return;
        }
        super.show();
    }

    /** 0.9.4: hộp THÔNG TIN (chỉ một lệnh "OK" bên trái, không phải hộp chờ). */
    boolean laThongTin() {
        return this.left != null && this.left.action != null && this.center == null && this.right == null && !this.isIcon;
    }

    @Override
    public void update() {
        // 0.9.4: hộp thông tin luôn đóng được - Back / Esc (phím mềm phải), hoặc chạm ra ngoài hộp
        if (laThongTin()) {
            if (Canvas.keyPressed[13]) {
                Canvas.keyPressed[13] = false;
                this.left.action.perform();
                return;
            }
            if (Canvas.isPointerClick && !this.isUseArr) {
                int bx = Canvas.hw - (this.wHop / 2), by = this.y + 12;
                boolean trong = Canvas.px >= bx && Canvas.px < bx + this.wHop && Canvas.py >= by && Canvas.py < by + this.hHop;
                if (!trong && Canvas.collisionCmdBar() < 0) {
                    Canvas.isPointerClick = false;
                    this.left.action.perform();
                    return;
                }
            }
        }
        this.state++;
        if (this.state > 3) {
            this.state = 0;
        }
        boolean changeFocus = false;
        if (Canvas.keyHold[2]) {
            this.selected--;
            if (this.selected < lastSe) {
                this.selected = lastSe;
            }
            changeFocus = true;
        } else if (Canvas.keyHold[8]) {
            changeFocus = true;
            if (cmy < cmyLim) {
                this.selected++;
            }
            if (this.selected > this.info.length - lastSe) {
                this.selected = this.info.length - lastSe;
            }
        }
        if (changeFocus) {
            cmtoY = (this.selected * 13) - (this.dis / 2);
            if (cmtoY < 0) {
                cmtoY = 0;
            }
            if (cmtoY > cmyLim) {
                cmtoY = cmyLim;
            }
        }
        if (cmy != cmtoY) {
            cmvy = (cmtoY - cmy) << 2;
            cmdy += cmvy;
            cmy += cmdy >> 4;
            cmdy &= 15;
        }
        super.update();
    }
}
