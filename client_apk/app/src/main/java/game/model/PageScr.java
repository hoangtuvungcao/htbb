package game.model;

import game.core.j2me.Graphics;
import game.render.GCanvas;
import game.render.Res;
import game.render.screen.Font;
import game.render.screen.Screen;

public class PageScr extends Screen {
    public static PageScr me;
    private int h;
    public int indexFont;
    public boolean isBigMap;
    private Screen lastScr;
    public Layer layer;
    private String title;
    private int w;
    private int x;
    private int y;

    public static PageScr gI() {
        PageScr pageScr = me;
        if (pageScr != null) {
            return pageScr;
        }
        PageScr pageScr2 = new PageScr();
        me = pageScr2;
        return pageScr2;
    }

    @Override
    public void switchToMe() {
        if (GCanvas.currentScreen != this) {
            this.lastScr = GCanvas.currentScreen;
        }
        super.switchToMe();
    }

    public void setInfo(int i, int i2, int i3, int i4, String str, Command command) {
        this.indexFont = 0;
        this.x = i;
        this.y = i2;
        this.w = i3;
        this.h = i4;
        this.title = str;
        this.center = command;
        this.left = new Command("Đóng", new IAction() {
            @Override
            public void perform() {
                PageScr.this.lastScr.switchToMe();
            }
        });
    }

    @Override
    public void update() {
        if (this.lastScr != GCanvas.shop) {
            this.lastScr.update();
        }
        Layer layer = this.layer;
        if (layer != null) {
            layer.update();
        }
    }

    @Override
    public boolean keyPress(int i) {
        this.layer.keyPress(i);
        return false;
    }

    @Override
    public void paint(Graphics graphics) {
        this.lastScr.paint(graphics);
        GCanvas.resetTrans(graphics);
        graphics.setColor(0);
        graphics.setAlpha(180);
        graphics.fillRect(0, 0, GCanvas.w, GCanvas.h);
        if (this.isBigMap) {
            graphics.translate(this.x, this.y);
            Res.paintDlgFull(graphics, 0, 0, this.w, this.h);
            if (this.indexFont == 0) {
                Font.bigFont1.drawString(graphics, this.title, this.w / 2, 3, 2);
            } else {
                Font.normalFont[0].drawString(graphics, this.title, this.w / 2, 6, 2);
            }
            Layer layer = this.layer;
            if (layer != null) {
                layer.paint(graphics, 0, 35);
            }
        } else {
            Res.paintDlgFull(graphics, this.x, this.y, this.w, this.h);
            if (this.indexFont == 0) {
                Font.bigFont1.drawString(graphics, this.title, GCanvas.hw, this.y + 3, 2);
            } else {
                Font.normalFont[0].drawString(graphics, this.title, this.w / 2, this.y + 6, 2);
            }
            Layer layer2 = this.layer;
            if (layer2 != null) {
                layer2.paint(graphics, this.x, this.y + 35);
            }
        }
        super.paint(graphics);
    }

    @Override
    public void switchToMe(Screen screen) {
        this.lastScr = screen;
        super.switchToMe(screen);
    }
}
