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

    private final IAction[] bamNut = new IAction[2];
    private static final String[] NUT = {"Đóng", "Xác nhận"};
    private static final int[] KIEU_NUT = {game.pokemon.Bang.TRUNG, game.pokemon.Bang.CHINH};
    private static final String[] TEN_NUT = {"so-dong", "so-ok"};

    /** 0.9.5 (APK / PC): ô nhập CHỮ trong khung hộp chung: tiêu đề, ô nhập, Đóng / Xác nhận. */
    void paintChu(Graphics g) {
        this.anNutLenh = true;
        this.bamNut[0] = this.backAction;
        this.bamNut[1] = this.okAction;
        int nh = game.pokemon.HopThongBao.caoNut(), pad = 8;
        int ho = Math.max(game.render.screen.Screen.ITEM_HEIGHT + 4, game.pokemon.VungCham.MIN);
        int ww = Math.max(220, Math.min(Canvas.w - 24, Math.min(320, game.pokemon.ChuDe.rong(this.title, game.pokemon.ChuDe.TIEU_DE) + 80)));
        String tl = this.title == null ? "" : this.title.toLowerCase();
        if (tl.indexOf("giftcode") >= 0 || tl.indexOf("quà") >= 0) {
            game.pokemon.HopThongBao.datHuyHieu(game.pokemon.BieuTuong.QUA, 0xF2B705, false);
        } else {
            game.pokemon.HopThongBao.datHuyHieu(game.pokemon.BieuTuong.TIN, game.pokemon.ChuDe.XANH, true);
        }
        int[] kh = game.pokemon.HopThongBao.khung(g, ww, ho + pad + nh + game.pokemon.HopThongBao.pad(), this.title, this.backAction, false);
        this.tfInput.x = kh[5];
        this.tfInput.y = kh[4];
        this.tfInput.width = kh[6];
        this.tfInput.height = ho;
        this.tfInput.paint(g);
        g.setClip(0, 0, Canvas.w, Canvas.h);
        game.pokemon.HopThongBao.hangNut(g, kh[0], kh[1] + kh[3] - game.pokemon.HopThongBao.pad() - nh, kh[2], NUT, KIEU_NUT, this.bamNut, TEN_NUT);
    }

    void paintSo(Graphics g) {
        this.anNutLenh = true;
        int k = game.pokemon.Bang.cach();
        int nh = game.pokemon.Bang.caoHang();
        int nhNut = game.pokemon.HopThongBao.caoNut();
        int ww = Math.min(Canvas.w - 24, game.pokemon.ChuDe.dt() ? 340 : 260);
        game.pokemon.HopThongBao.datHuyHieu(game.pokemon.BieuTuong.CUA_HANG, game.pokemon.ChuDe.TEAL, true);
        int[] kh = game.pokemon.HopThongBao.khung(g, ww, nh + k + nh + k + nhNut + game.pokemon.HopThongBao.pad(), this.title, this.backAction, false);
        int x = kh[5] - 10, y = kh[1];
        ww = kh[6] + 20;
        int yy = kh[4];
        // [-] [ô số] [+]
        this.tfInput.x = x + 10 + nh + k;
        this.tfInput.width = ww - 20 - 2 * (nh + k);
        this.tfInput.height = nh;
        this.tfInput.y = yy;
        game.pokemon.Bang.nut(g, x + 10, yy, nh, nh, "", game.pokemon.Bang.TRUNG, new IAction() {
            public void perform() {
                InputDlg.this.datSo(InputDlg.this.giaTri() - 1);
            }
        }, "so-tru");
        game.pokemon.Bang.nut(g, x + ww - 10 - nh, yy, nh, nh, "", game.pokemon.Bang.TRUNG, new IAction() {
            public void perform() {
                InputDlg.this.datSo(InputDlg.this.giaTri() + 1);
            }
        }, "so-cong");
        g.setColor(game.pokemon.ChuDe.CHU);
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
                    game.pokemon.Bang.TRUNG, new IAction() {
                public void perform() {
                    InputDlg.this.datSo(v);
                }
            }, "so-nhanh-" + i);
        }
        this.bamNut[0] = this.backAction;
        this.bamNut[1] = this.okAction;
        game.pokemon.HopThongBao.hangNut(g, kh[0], kh[1] + kh[3] - game.pokemon.HopThongBao.pad() - nhNut, kh[2], NUT, KIEU_NUT, this.bamNut, TEN_NUT);
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
        if (game.pokemon.MenuNguCanh.bat()) {
            paintChu(g);
            return;
        }
        this.anNutLenh = false;
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
