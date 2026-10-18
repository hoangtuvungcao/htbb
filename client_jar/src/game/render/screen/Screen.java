package game.render.screen;

import game.model.Main;
import game.render.Canvas;
import game.render.Res;
import javax.microedition.lcdui.Graphics;

public abstract class Screen extends Main {
    public static short deltaY = 17;
    public static final int ITEM_HEIGHT = Font.normalFont[0].getHeight() + 6;

    public void init() {
    }

    public void switchToMe() {
        Canvas.currentScreen = this;
        Canvas.instance.setFullScreenMode(true);
    }

    public void switchToMe(Screen lastSCR) {
        Canvas.currentScreen = this;
        Canvas.instance.setFullScreenMode(true);
    }

    public boolean keyPress(int keyCode) {
        return false;
    }

    @Override
    public void paint(Graphics g) {
        Canvas.resetTrans(g);
        g.drawImage(Res.imgCmdBar, 0, Canvas.h + 3, 36);
        if (Canvas.currentScreen == this && Canvas.currentDialog == null && !Canvas.menu.showMenu) {
            super.paint(g);
        }
    }

    public void update() {
    }

    @Override
    public void updateKey() {
        if (Canvas.currentScreen == this) {
            super.updateKey();
        }
    }

    public void pointerPress(int dx, int dy) {
    }

    public void startXayBot(int idbot, int type, int sl) {
    }
}
