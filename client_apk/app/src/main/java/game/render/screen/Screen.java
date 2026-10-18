package game.render.screen;

import game.core.j2me.Graphics;
import game.model.Main;
import game.render.GCanvas;

public abstract class Screen extends Main {
    public static int ITEM_HEIGHT = 19;
    public static short deltaY = 17;

    public static int absolute(int i) {
        return i < 0 ? -i : i;
    }

    public void init() {
    }

    public boolean keyPress(int i) {
        return false;
    }

    public void onKeyUp() {
    }

    public void pointerPress(int i, int i2) {
    }

    public void startXayBot(int i, int i2, int i3) {
    }

    public void update() {
    }

    public void switchToMe() {
        GCanvas.currentScreen = this;
    }

    public void switchToMe(Screen screen) {
        GCanvas.currentScreen = this;
    }

    @Override
    public void paint(Graphics graphics) {
        GCanvas.resetTrans(graphics);
        if (GCanvas.currentScreen == this && GCanvas.currentDialog == null && !GCanvas.menu.showMenu) {
            super.paint(graphics);
        }
    }

    @Override
    public void updateKey() {
        if (GCanvas.currentScreen == this) {
            super.updateKey();
        }
    }

    public void onKeyDown() {
        GCanvas.instance.getFocusCavas();
    }

    public static int selectmenu(int i, int i2, int i3, int i4, int i5, int i6, int i7) {
        int i8;
        if (absolute(i - (GCanvas.w / 2)) > i4 || (i8 = (i2 - i5) / i6) < 0 || i8 > i7) {
            return -2;
        }
        if (i8 == i3) {
            return -1;
        }
        return i8;
    }

    public static boolean xacDinhToaDo(int i, int i2, int i3, int i4, int i5, int i6) {
        int i7 = i5 / 2;
        if (i <= i3 - i7 || i >= i3 + i7) {
            return false;
        }
        int i8 = i6 / 2;
        return i2 > i4 - i8 && i2 < i4 + i8;
    }
}
