package game.render.screen;

import game.core.j2me.Graphics;
import game.model.Main;
import game.render.GCanvas;

public abstract class Dialog extends Main {
    public void keyPress(int i) {
    }

    @Override
    public void paint(Graphics graphics) {
        super.paint(graphics);
    }

    public void update() {
        super.updateKey();
    }

    public void show() {
        GCanvas.currentDialog = this;
    }
}
