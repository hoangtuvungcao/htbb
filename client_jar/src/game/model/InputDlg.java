package game.model;

import game.render.Canvas;
import game.render.Res;
import game.render.screen.Dialog;
import game.render.screen.Font;
import game.render.screen.TField;
import javax.microedition.lcdui.Graphics;

public class InputDlg extends Dialog {
    protected String title;
    public IAction okAction;
    public IAction backAction;
    private int w;
    public boolean isMax = false;
    public TField tfInput = new TField();

    public InputDlg() {
        this.tfInput.height = 21;
        this.tfInput.y = Canvas.h - 56;
        this.tfInput.isFocus = true;
    }

    public void init() {
        this.tfInput.y = Canvas.h - 56;
        this.w = Font.borderFont.getWidth(this.title) + 30;
        if (this.w > Canvas.w) {
            String str = "";
            for (int i = 0; i < this.title.length() - 1; i++) {
                str = new StringBuffer(String.valueOf(str)).append(this.title.substring(i, i + 1)).toString();
                if (Font.borderFont.getWidth(str) >= Canvas.w - 50) {
                    this.title = new StringBuffer(String.valueOf(str)).append("...").toString();
                    this.w = Font.borderFont.getWidth(this.title) + 30;
                    break;
                }
            }
        } else if (!this.isMax) {
            this.w = Canvas.w - 30;
        }
        if (this.w < 110) {
            this.w = 110;
        }
        this.tfInput.width = this.w - 40;
        this.tfInput.x = Canvas.hw - (this.tfInput.width / 2);
    }

    public void setInfo(String title, IAction ok, int type, int num, boolean isMaxW) {
        this.isMax = isMaxW;
        this.soToiDa = 0;
        this.tfInput.setText("");
        this.tfInput.setIputType(type);
        this.tfInput.setMaxTextLenght(num);
        this.title = title;
        this.okAction = ok;
        this.backAction = new IAction() {
            @Override
            public void perform() {
                Canvas.endDlg();
            }
        };
        this.left = new Command("Đóng", this.backAction);
        this.center = new Command("Ok", this.okAction);
        this.right = this.tfInput.cmdClear;
        init();
    }

    /** pokemon 0.9.3: ô nhập SỐ (số lượng mua / bán / dùng) = bộ đếm − / + to + chọn nhanh + nút xác nhận to. */
    boolean laSo() {
        return this.tfInput.getIputType() == TField.INPUT_TYPE_NUMERIC;
    }

    int giaTri() {
        try {
            return Integer.parseInt(this.tfInput.getText().trim());
        } catch (Throwable e) {
            return 0;
        }
    }

    /** trần số lượng của lần hỏi này (0 = theo số chữ số của ô) - đặt lại 0 mỗi setInfo */
    public int soToiDa;

    int toiDa() {
        if (this.soToiDa > 0) {
            return this.soToiDa;
        }
        int n = this.tfInput.getMaxTextLenght();
        int m = 1;
        for (int i = 0; i < n && i < 9; i++) {
            m *= 10;
        }
        return m - 1;
    }

    void datSo(int v) {
        if (v < 0) {
            v = 0;
        }
        if (v > toiDa()) {
            v = toiDa();
        }
        this.tfInput.setText(String.valueOf(v));
    }

    void paintSo(Graphics g) {
        game.pokemon.Bang.phuNen(g);
        int k = game.pokemon.Bang.cach();
        int nh = game.pokemon.Bang.caoHang();
        int ww = Math.min(Canvas.w - 24, 340);
        int hh = 26 + nh + k + nh + k + nh + 14;
        int x = Canvas.hw - ww / 2, y = Canvas.hh - hh / 2;
        game.pokemon.Bang.the(g, x, y, ww, hh);
        String t = game.pokemon.GiaoDien.catChu(Font.blackFont, this.title, ww - 16);
        Font.blackFont.drawString(g, t, Canvas.hw, y + 6, 2);
        int yy = y + 26;
        // [-] [ô số] [+]
        this.tfInput.x = x + 10 + nh + k;
        this.tfInput.width = ww - 20 - 2 * (nh + k);
        this.tfInput.height = nh;
        this.tfInput.y = yy;
        game.pokemon.Bang.nut(g, x + 10, yy, nh, nh, "", game.pokemon.Bang.PHU, new IAction() {
            public void perform() {
                InputDlg.this.datSo(InputDlg.this.giaTri() - 1);
            }
        }, "so-tru");
        game.pokemon.Bang.nut(g, x + ww - 10 - nh, yy, nh, nh, "", game.pokemon.Bang.PHU, new IAction() {
            public void perform() {
                InputDlg.this.datSo(InputDlg.this.giaTri() + 1);
            }
        }, "so-cong");
        g.setColor(0xFFFFFF);
        int a = nh / 4, cy = yy + nh / 2;
        g.fillRect(x + 10 + nh / 2 - a, cy - 1, 2 * a, 3);
        int cxp = x + ww - 10 - nh / 2;
        g.fillRect(cxp - a, cy - 1, 2 * a, 3);
        g.fillRect(cxp - 1, cy - a, 3, 2 * a);
        this.tfInput.paint(g);
        g.setClip(0, 0, Canvas.w, Canvas.h);
        yy += nh + k;
        // chọn nhanh + Tối đa (soToiDa: nơi gọi đặt theo tiền / chỗ trống; không đặt = theo số chữ số)
        final int[] nhanh = {1, 5, 10, -1};
        int wc = (ww - 20 - 3 * k) / 4;
        for (int i = 0; i < nhanh.length; i++) {
            final int v = nhanh[i] < 0 ? toiDa() : Math.min(nhanh[i], toiDa());
            game.pokemon.Bang.nut(g, x + 10 + i * (wc + k), yy, wc, nh, nhanh[i] < 0 ? "Tối đa" : String.valueOf(v),
                    nhanh[i] < 0 ? game.pokemon.Bang.TRUNG : game.pokemon.Bang.PHU, new IAction() {
                public void perform() {
                    InputDlg.this.datSo(v);
                }
            }, "so-nhanh-" + i);
        }
        yy += nh + k;
        int wb = (ww - 20 - k) / 2;
        game.pokemon.Bang.nut(g, x + 10, yy, wb, nh, "Đóng", game.pokemon.Bang.TRUNG, this.backAction, "so-dong");
        game.pokemon.Bang.nut(g, x + 10 + wb + k, yy, ww - 20 - wb - k, nh, "Xác nhận", game.pokemon.Bang.XAC_NHAN, this.okAction, "so-ok");
    }

    @Override
    public void paint(Graphics g) {
        Canvas.resetTrans(g);
        if (laSo()) {
            if (this.tfInput.getText().length() == 0) {
                this.tfInput.setText("1");
            }
            this.left = null;
            this.center = null;
            this.right = null;
            paintSo(g);
            return;
        }
        Res.paintDlgFull(g, Canvas.hw - (this.w / 2), Canvas.h - 90, this.w, 69);
        Font.borderFont.drawString(g, this.title, Canvas.hw, Canvas.h - 86, 2);
        this.tfInput.paint(g);
        super.paint(g);
    }

    @Override
    public void keyPress(int keyCode) {
        if (laSo()) {
            if (keyCode == -5 || keyCode == 10 || keyCode == 13) {
                if (this.okAction != null) {
                    this.okAction.perform();
                }
                return;
            }
            if (keyCode == -7 || keyCode == -6 || keyCode == 27) {
                if (this.backAction != null) {
                    this.backAction.perform();
                }
                return;
            }
            if (keyCode == -1 || keyCode == -4) {
                datSo(giaTri() + 1);
                return;
            }
            if (keyCode == -2 || keyCode == -3) {
                datSo(giaTri() - 1);
                return;
            }
        }
        boolean swallow = this.tfInput.keyPressed(keyCode);
        if (swallow) {
            Canvas.keyPressed[5] = false;
        }
    }

    @Override
    public void update() {
        this.tfInput.update();
        super.updateKey();
    }

    @Override
    public void updateKey() {
    }
}
