package game.model;

import game.render.Canvas;
import game.render.Res;
import game.render.screen.Font;
import game.render.screen.Screen;
import javax.microedition.lcdui.Graphics;

public class PageScr extends Screen {
    public static PageScr me;
    public Layer layer;
    private Screen lastScr;
    private int x;
    private int y;
    private int w;
    private int h;
    public int indexFont;
    private String title;

    public static PageScr gI() {
        if (me != null) {
            return me;
        }
        PageScr pageScr = new PageScr();
        me = pageScr;
        return pageScr;
    }

    @Override
    public void switchToMe() {
        if (Canvas.currentScreen != this) {
            this.lastScr = Canvas.currentScreen;
        }
        super.switchToMe();
    }

    public void setInfo(int x, int y, int w, int h, String title, Command cmd) {
        this.indexFont = 0;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.title = title;
        this.center = cmd;
        this.left = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                PageScr.this.lastScr.switchToMe();
            }
        });
    }

    @Override
    public void update() {
        if (this.lastScr != Canvas.shop) {
            this.lastScr.update();
        }
        if (this.layer != null) {
            this.layer.update();
        }
    }

    @Override
    public boolean keyPress(int keyCode) {
        this.layer.keyPress(keyCode);
        return false;
    }

    @Override
    public void paint(Graphics g) {
        this.lastScr.paint(g);
        Canvas.resetTrans(g);
        g.translate(this.x, this.y);
        Res.paintDlgFull(g, 0, 0, this.w, this.h);
        if (this.indexFont == 0) {
            Font.bigFont.drawString(g, this.title, this.w / 2, 3, 2);
        } else {
            Font.normalFont[0].drawString(g, this.title, this.w / 2, 6, 2);
        }
        if (this.layer != null) {
            this.layer.paint(g, 0, 35);
        }
        super.paint(g);
    }

    @Override
    public void switchToMe(Screen lastSCR) {
        this.lastScr = lastSCR;
        super.switchToMe(lastSCR);
    }
}
