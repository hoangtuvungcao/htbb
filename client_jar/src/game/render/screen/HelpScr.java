package game.render.screen;

import game.model.Command;
import game.model.IAction;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.Res;
import javax.microedition.lcdui.Graphics;

public class HelpScr extends Screen {
    private static HelpScr me;
    public int cmtoY;
    public int cmy;
    public int cmdy;
    public int cmvy;
    int[] font;
    public int cmyLim = 10000;
    String[] stInfo = null;
    private short y = 20;
    private short x = 20;
    private short w = (short) (Canvas.w - (this.x * 2));
    private short h = (short) ((Canvas.h - (this.y * 2)) - Screen.deltaY);

    public static HelpScr gI() {
        if (me != null) {
            return me;
        }
        HelpScr helpScr = new HelpScr();
        me = helpScr;
        return helpScr;
    }

    public HelpScr() {
        this.right = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                Canvas.gameScr.switchToMe();
                HelpScr.me = null;
                HelpScr.this.stInfo = null;
            }
        });
        GameService.gI().requestString(0);
        Canvas.startWaitDlg();
    }

    /** chữ gốc (đoạn "x0 " = tiêu đề, "x1 " = nội dung) - bản vẽ kiểu chung ngắt lại theo bề ngang tấm */
    String goc;

    public void setInfo(String st) {
        this.goc = st;
        // pokemon: ngắt dòng theo bề rộng khung (Canvas.w - 30 cũ rộng hơn khung -> chữ tràn mép phải)
        this.stInfo = Font.normalFont[0].splitFontBStrInLine(st, this.w - 20);
        int c = 0;
        this.font = new int[this.stInfo.length];
        for (int i = 0; i < this.stInfo.length; i++) {
            if (this.stInfo[i].startsWith("x0")) {
                this.stInfo[i] = this.stInfo[i].substring(3);
                c = 0;
            } else if (this.stInfo[i].startsWith("x1")) {
                this.stInfo[i] = this.stInfo[i].substring(3);
                c = 1;
            }
            this.font[i] = c;
        }
        this.cmyLim = (this.stInfo.length * 13) - (this.h - 35);
        Canvas.endDlg();
    }

    @Override
    public boolean keyPress(int keyCode) {
        return super.keyPress(keyCode);
    }

    @Override
    public void paint(Graphics g) {
        Canvas.gameScr.paint(g);
        if (game.pokemon.ManCu.bat()) {
            // 31/10/2026: tấm chung, chữ ngắt theo tấm, cuộn bằng ngón; X = Đóng
            this.anNutLenh = true;
            int[] c = game.pokemon.ManCu.tam(g, this, "Hướng dẫn", game.pokemon.ManCu.viec(this.right), null);
            if (this.goc != null) {
                String[] d = game.render.Util.split(this.goc, "\n");
                int[] k = new int[d.length];
                for (int i = 0; i < d.length; i++) {
                    if (d[i].startsWith("x0 ")) {
                        d[i] = d[i].substring(3);
                        k[i] = 1;
                    } else if (d[i].startsWith("x1 ")) {
                        d[i] = d[i].substring(3);
                    }
                }
                game.pokemon.ManCu.vanBan(g, c, d, k);
            }
            return;
        }
        g.translate(this.x, this.y);
        Res.paintDlgFull(g, 0, 0, this.w, this.h);
        Font.borderFont.drawString(g, "Hướng dẫn", this.w / 2, 6, 2);
        g.translate(0, 30);
        g.setClip(0, -2, this.w - 4, this.h - 32);
        g.translate(0, -this.cmy);
        if (this.stInfo != null) {
            int aa = this.cmy / 13;
            int bb = aa + ((this.h - 35) / 13) + 2;
            if (bb > this.stInfo.length) {
                bb = this.stInfo.length;
            }
            for (int i = aa; i < bb; i++) {
                Font f = this.font[i] == 0 ? Font.arialFontW : Font.normalFont[0];
                f.drawString(g, this.stInfo[i], 7, i * 13, 0);
            }
        }
        super.paint(g);
    }

    @Override
    public void switchToMe() {
        super.switchToMe();
    }

    @Override
    public void update() {
        boolean isMoveCamera = false;
        moveCameraY();
        if (Canvas.keyHold[8]) {
            this.cmtoY += 13;
            isMoveCamera = true;
        }
        if (Canvas.keyHold[2]) {
            this.cmtoY -= 13;
            isMoveCamera = true;
        }
        if (isMoveCamera) {
            if (this.cmtoY > this.cmyLim) {
                this.cmtoY = this.cmyLim;
            }
            if (this.cmtoY < 0) {
                this.cmtoY = 0;
            }
        }
        super.update();
    }

    public void moveCameraY() {
        if (this.cmy != this.cmtoY) {
            this.cmvy = (this.cmtoY - this.cmy) << 2;
            this.cmdy += this.cmvy;
            this.cmy += this.cmdy >> 4;
            this.cmdy &= 15;
        }
    }
}
